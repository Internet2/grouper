/**
 * Copyright 2014 Internet2
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.internet2.middleware.grouper.ldap;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.loader.ldap.GrouperLoaderLdapServer;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.vt.middleware.ldap.Ldap;
import edu.vt.middleware.ldap.LdapConfig;
import edu.vt.middleware.ldap.SearchFilter;
import edu.vt.middleware.ldap.pool.BlockingLdapPool;
import edu.vt.middleware.ldap.pool.DefaultLdapFactory;
import edu.vt.middleware.ldap.pool.LdapPoolConfig;
import edu.vt.middleware.ldap.pool.LdapPoolException;

/**
 * will handle the ldap config, and inverse of control for pooling
 * 
 * @author mchyzer
 *
 */
public class LdapSession {

  /** map of connection name to pool */
  private static Map<String, BlockingLdapPool> poolMap = new HashMap<String, BlockingLdapPool>();
  
  /**
   * get or create the pool based on the server id
   * @param ldapServerId
   * @return the pool
   */
  private static BlockingLdapPool blockingLdapPool(String ldapServerId) {
    
    BlockingLdapPool blockingLdapPool = poolMap.get(ldapServerId);
    
    if (blockingLdapPool == null) {
      synchronized (LdapSession.class) {
        blockingLdapPool = poolMap.get(ldapServerId);
        
        if (blockingLdapPool == null) {
          
          GrouperLoaderLdapServer grouperLoaderLdapServer = GrouperLoaderConfig.retrieveLdapProfile(ldapServerId);
          
          LdapConfig ldapConfig = null;
          
          // load this vt-ldap config file before the configs here.  load from classpath
          if (!StringUtils.isBlank(grouperLoaderLdapServer.getConfigFileFromClasspath())) {

            URL url = GrouperUtil.computeUrl(grouperLoaderLdapServer.getConfigFileFromClasspath(), false);
            try {
              ldapConfig = LdapConfig.createFromProperties(url.openStream());    
            } catch (IOException ioe) {
              throw new RuntimeException("Error processing classpath file: " + grouperLoaderLdapServer.getConfigFileFromClasspath(), ioe);
            }
            
            if (!StringUtils.isBlank(grouperLoaderLdapServer.getUrl())) {
              ldapConfig.setLdapUrl(grouperLoaderLdapServer.getUrl());
            }
          } else {

            ldapConfig = new LdapConfig(grouperLoaderLdapServer.getUrl());

          }
          
          if (!StringUtils.isBlank(grouperLoaderLdapServer.getUser())) {
            ldapConfig.setBindDn(grouperLoaderLdapServer.getUser());
          }
          
          if (!StringUtils.isBlank(grouperLoaderLdapServer.getPass())) {
            ldapConfig.setBindCredential(grouperLoaderLdapServer.getPass());
          }
          
          //#optional, if you are using tls, set this to true.  Generally you will not be using an SSL URL to use TLS...
          //#ldap.personLdap.tls = false
          if (grouperLoaderLdapServer.isTls()) {
            ldapConfig.setTls(grouperLoaderLdapServer.isTls());
          }
          
          LdapPoolConfig ldapPoolConfig = new LdapPoolConfig();
          
          //
          //#optional, if using sasl
          //#ldap.personLdap.saslAuthorizationId = 
          //#ldap.personLdap.saslRealm = 
          if (!StringUtils.isBlank(grouperLoaderLdapServer.getSaslAuthorizationId())) {
            ldapConfig.setSaslAuthorizationId(grouperLoaderLdapServer.getSaslAuthorizationId());
          }
          if (!StringUtils.isBlank(grouperLoaderLdapServer.getSaslRealm())) {
            ldapConfig.setSaslRealm(grouperLoaderLdapServer.getSaslRealm());
          }
          
          //
          //#optional (note, time limit is for search operations, timeout is for connection timeouts), 
          //#most of these default to vt-ldap defaults.  times are in millis
          //#validateOnCheckout defaults to true if all other validate methods are false
          //#ldap.personLdap.batchSize = 
          if (grouperLoaderLdapServer.getBatchSize() != -1) {
            ldapConfig.setBatchSize(grouperLoaderLdapServer.getBatchSize());
          }
          
          //#ldap.personLdap.countLimit = 
          if (grouperLoaderLdapServer.getCountLimit() != -1) {
            ldapConfig.setCountLimit(grouperLoaderLdapServer.getCountLimit());
          }
          
          //#ldap.personLdap.timeLimit = 
          if (grouperLoaderLdapServer.getTimeLimit() != -1) {
            ldapConfig.setTimeLimit(grouperLoaderLdapServer.getTimeLimit());
          }
          
          //#ldap.personLdap.timeout = 
          if (grouperLoaderLdapServer.getTimeout() != -1) {
            ldapConfig.setTimeout(grouperLoaderLdapServer.getTimeout());
          }
          
          //#ldap.personLdap.pagedResultsSize
          if (grouperLoaderLdapServer.getPagedResultsSize() != -1) {
            ldapConfig.setPagedResultsSize(grouperLoaderLdapServer.getPagedResultsSize());
          }

          //#ldap.personLdap.searchResultHandlers
          if (grouperLoaderLdapServer.getSearchResultHandlers() != null) {
            ldapConfig.setSearchResultHandlers(grouperLoaderLdapServer.getSearchResultHandlers());
          }

          //#ldap.personLdap.referral
          if (!StringUtils.isBlank(grouperLoaderLdapServer.getReferral())) {
            ldapConfig.setReferral(grouperLoaderLdapServer.getReferral());
          }

          //#ldap.personLdap.minPoolSize = 
          if (grouperLoaderLdapServer.getMinPoolSize() != -1) {
            ldapPoolConfig.setMinPoolSize(grouperLoaderLdapServer.getMinPoolSize());
          }
          
          //#ldap.personLdap.maxPoolSize = 
          if (grouperLoaderLdapServer.getMaxPoolSize() != -1) {
            ldapPoolConfig.setMaxPoolSize(grouperLoaderLdapServer.getMaxPoolSize());
          }
          
          //#ldap.personLdap.validateOnCheckIn = 
          if (grouperLoaderLdapServer.isValidateOnCheckIn()) {
            ldapPoolConfig.setValidateOnCheckIn(grouperLoaderLdapServer.isValidateOnCheckIn());
          }

          //#ldap.personLdap.validateOnCheckOut = 
          if (grouperLoaderLdapServer.isValidateOnCheckOut()) {
            ldapPoolConfig.setValidateOnCheckOut(grouperLoaderLdapServer.isValidateOnCheckOut());
          }

          //#ldap.personLdap.validatePeriodically = 
          if (grouperLoaderLdapServer.isValidatePeriodically()) {
            ldapPoolConfig.setValidatePeriodically(grouperLoaderLdapServer.isValidatePeriodically());
          }
          
          //#ldap.personLdap.validateTimerPeriod = 
          if (grouperLoaderLdapServer.getValidateTimerPeriod() != -1) {
            ldapPoolConfig.setValidateTimerPeriod(grouperLoaderLdapServer.getValidateTimerPeriod());
          }
          
          //#ldap.personLdap.pruneTimerPeriod = 
          if (grouperLoaderLdapServer.getPruneTimerPeriod() != -1) {
            ldapPoolConfig.setPruneTimerPeriod(grouperLoaderLdapServer.getPruneTimerPeriod());
          }

          //#if connections expire after a certain amount of time, this is it, in millis, defaults to 300000 (5 minutes)
          //#ldap.personLdap.expirationTime = 
          if (grouperLoaderLdapServer.getExpirationTime() != -1) {
            ldapPoolConfig.setExpirationTime(grouperLoaderLdapServer.getExpirationTime());
          }
          
          DefaultLdapFactory factory = new DefaultLdapFactory(ldapConfig);
          factory.setLdapValidator(grouperLoaderLdapServer.getValidator());

          blockingLdapPool = new BlockingLdapPool(ldapPoolConfig, factory);
          blockingLdapPool.initialize();
          poolMap.put(ldapServerId, blockingLdapPool);
        }
      }
    }
    return blockingLdapPool;
  }
  
  
  /**
   * call this to send a callback for the ldap session object.
   * @param ldapServerId is the config id from the grouper-loader.properties
   * @param ldapHandler is the logic of the ldap calls
   * @return the result of the handler
   */
  public static Object callbackLdapSession(
      String ldapServerId, LdapHandler ldapHandler) {
    
    Object ret = null;
    BlockingLdapPool blockingLdapPool = null;
    Ldap ldap = null;
    try {
      
      blockingLdapPool = blockingLdapPool(ldapServerId);

      if (LOG.isDebugEnabled()) {
        LOG.debug("pre-checkout: ldap id: " + ldapServerId + ", pool active: " + blockingLdapPool.activeCount() + ", available: " + blockingLdapPool.availableCount());
      }

      ldap = blockingLdapPool.checkOut();
      
      if (LOG.isDebugEnabled()) {
        LOG.debug("post-checkout: ldap id: " + ldapServerId + ", pool active: " + blockingLdapPool.activeCount() + ", available: " + blockingLdapPool.availableCount());
      }
      
      LdapHandlerBean ldapHandlerBean = new LdapHandlerBean();
      
      ldapHandlerBean.setLdap(ldap);
        
      ret = ldapHandler.callback(ldapHandlerBean);

    } catch (RuntimeException re) {
      GrouperUtil.injectInException(re, "Problem with ldap conection: " + ldapServerId);
      throw re;
    } catch (LdapPoolException lpe) {
      throw new RuntimeException("Problem with ldap conection: " + ldapServerId, lpe);
    } catch (NamingException ne) {
      throw new RuntimeException("Problem with ldap conection: " + ldapServerId, ne);
    } finally {
      if (blockingLdapPool != null && ldap != null) {
        blockingLdapPool.checkIn(ldap);
      }
    }
    return ret;

  }

  /**
   * run a filter, for one attribute, and return a list of that attribute typecast as a certain type
   * note, if it is a multi-valued attributes, and there are multiple object results, it will be flattened into one list
   * @param <R>
   * @param returnType note, only String.class is currently supported
   * @param ldapServerId
   * @param searchDn
   * @param ldapSearchScope 
   * @param filter
   * @param attributeName
   * @return the list of results, never null
   */
  public static <R> List<R> list(final Class<R> returnType, final String ldapServerId, 
      final String searchDn, final LdapSearchScope ldapSearchScope, final String filter, final String attributeName) {
    
    try {
      
      return (List<R>)callbackLdapSession(ldapServerId, new LdapHandler() {
        
        public Object callback(LdapHandlerBean ldapHandlerBean) throws NamingException {

          Ldap ldap = ldapHandlerBean.getLdap();
          
          Iterator<SearchResult> searchResultIterator = null;
          
          SearchFilter searchFilterObject = new SearchFilter(filter);
          String[] attributeArray = new String[]{attributeName};
          
          SearchControls searchControls = ldap.getLdapConfig().getSearchControls(attributeArray);
          
          if (ldapSearchScope != null) {
            searchControls.setSearchScope(ldapSearchScope.getSeachControlsConstant());
          }
          
          if (StringUtils.isBlank(searchDn)) {
            searchResultIterator = ldap.search(
                searchFilterObject, searchControls);
          } else {
            searchResultIterator = ldap.search(searchDn,
                searchFilterObject, searchControls);
          }
          
          List<R> result = new ArrayList<R>();
          while (searchResultIterator.hasNext()) {
            
            SearchResult searchResult = searchResultIterator.next();
            
            Attribute attribute = searchResult.getAttributes().get(attributeName);
            
            if (attribute == null && StringUtils.equals("dn", attributeName)) {
              String nameInNamespace = searchResult.getName();
              Object attributeValue = GrouperUtil.typeCast(nameInNamespace, returnType);
              result.add((R)attributeValue);
            } else {
              
              //GRP-921 - ldap session list throws an error if the attribute does not exist on the server
              if (attribute != null) {
                for (int i=0;i<attribute.size();i++) {
    
                  Object attributeValue = attribute.get(i);
                  attributeValue = GrouperUtil.typeCast(attributeValue, returnType);
                  if (attributeValue != null) {
                    result.add((R)attributeValue);
                  }
                }
              }
            }
          }

          if (LOG.isDebugEnabled()) {
            LOG.debug("Found " + result.size() + " results for serverId: " + ldapServerId + ", searchDn: " + searchDn
              + ", filter: '" + filter + "', returning attribute: " 
              + attributeName + ", some results: " + GrouperUtil.toStringForLog(result, 100) );
          }
          
          return result;
        }
      });
    } catch (RuntimeException re) {
      GrouperUtil.injectInException(re, "Error querying ldap server id: " + ldapServerId + ", searchDn: " + searchDn
          + ", filter: '" + filter + "', returning attribute: " + attributeName);
      throw re;
    }
    
  }

  /**
   * logger 
   */
  private static final Log LOG = GrouperUtil.getLog(LdapSession.class);

  /**
   * run a filter, for one attribute, and return a map of the DN key to the value of list of that attribute typecast as a certain type
   * @param <R>
   * @param returnType note, only String.class is currently supported
   * @param ldapServerId
   * @param searchDn
   * @param ldapSearchScope 
   * @param filter
   * @param attributeName
   * @return the list of results, never null
   */
  public static <R> Map<String, List<R>> listInObjects(final Class<R> returnType, final String ldapServerId, 
      final String searchDn, final LdapSearchScope ldapSearchScope, final String filter, final String attributeName) {
    
    try {
      
      return (Map<String, List<R>>)callbackLdapSession(ldapServerId, new LdapHandler() {
        
        public Object callback(LdapHandlerBean ldapHandlerBean) throws NamingException {
  
          Ldap ldap = ldapHandlerBean.getLdap();
          
          Iterator<SearchResult> searchResultIterator = null;
          
          SearchFilter searchFilterObject = new SearchFilter(filter);
          String[] attributeArray = new String[]{attributeName};
          
          SearchControls searchControls = ldap.getLdapConfig().getSearchControls(attributeArray);
          
          if (ldapSearchScope != null) {
            searchControls.setSearchScope(ldapSearchScope.getSeachControlsConstant());
          }
          
          if (StringUtils.isBlank(searchDn)) {
            searchResultIterator = ldap.search(
                searchFilterObject, searchControls);
          } else {
            searchResultIterator = ldap.search(searchDn,
                searchFilterObject, searchControls);
          }
          
          Map<String, List<R>> result = new HashMap<String, List<R>>();
          int subObjectCount = 0;
          while (searchResultIterator.hasNext()) {

            SearchResult searchResult = searchResultIterator.next();
            
            List<R> valueResults = new ArrayList<R>();
            String nameInNamespace = searchResult.getName();
            //for some reason this returns: cn=test:testGroup,dc=upenn,dc=edu
            // instead of cn=test:testGroup,ou=groups,dc=upenn,dc=edu
            if (nameInNamespace != null && !StringUtils.isBlank(searchDn)) {
              GrouperLoaderLdapServer grouperLoaderLdapServer = GrouperLoaderConfig.retrieveLdapProfile(ldapServerId);
              String baseDn = grouperLoaderLdapServer.getBaseDn();
              if (!StringUtils.isBlank(baseDn) && nameInNamespace.endsWith("," + baseDn)) {
                
                //sub one to get the comma out of there
                nameInNamespace = nameInNamespace.substring(0, nameInNamespace.length() - (baseDn.length()+1));
                nameInNamespace += "," + searchDn + "," + baseDn;
              }
            }
            
            
            result.put(nameInNamespace, valueResults);
            
            Attribute attribute = searchResult.getAttributes().get(attributeName);
            
            if (attribute != null) {
              for (int i=0;i<attribute.size();i++) {
                
                Object attributeValue = attribute.get(i);
                attributeValue = GrouperUtil.typeCast(attributeValue, returnType);
                if (attributeValue != null) {
                  subObjectCount++;
                  valueResults.add((R)attributeValue);
                }
              }
            }
          }
  
          if (LOG.isDebugEnabled()) {
            LOG.debug("Found " + result.size() + " results, (" + subObjectCount + " sub-results) for serverId: " + ldapServerId + ", searchDn: " + searchDn
              + ", filter: '" + filter + "', returning attribute: " 
              + attributeName + ", some results: " + GrouperUtil.toStringForLog(result, 100) );
          }
          
          return result;
        }
      });
    } catch (RuntimeException re) {
      GrouperUtil.injectInException(re, "Error querying ldap server id: " + ldapServerId + ", searchDn: " + searchDn
          + ", filter: '" + filter + "', returning attribute: " + attributeName);
      throw re;
    }
    
  }


  
}
