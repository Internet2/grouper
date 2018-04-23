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
package edu.internet2.middleware.grouper.ldap.vtldap;

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
import edu.internet2.middleware.grouper.ldap.LdapAttribute;
import edu.internet2.middleware.grouper.ldap.LdapEntry;
import edu.internet2.middleware.grouper.ldap.LdapHandler;
import edu.internet2.middleware.grouper.ldap.LdapHandlerBean;
import edu.internet2.middleware.grouper.ldap.LdapSearchScope;
import edu.internet2.middleware.grouper.ldap.LdapSession;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.morphString.Morph;
import edu.vt.middleware.ldap.Ldap;
import edu.vt.middleware.ldap.LdapConfig;
import edu.vt.middleware.ldap.SearchFilter;
import edu.vt.middleware.ldap.handler.SearchResultHandler;
import edu.vt.middleware.ldap.pool.BlockingLdapPool;
import edu.vt.middleware.ldap.pool.CompareLdapValidator;
import edu.vt.middleware.ldap.pool.ConnectLdapValidator;
import edu.vt.middleware.ldap.pool.DefaultLdapFactory;
import edu.vt.middleware.ldap.pool.LdapPoolConfig;
import edu.vt.middleware.ldap.pool.LdapValidator;

/**
 * will handle the ldap config, and inverse of control for pooling
 * 
 * @author mchyzer
 *
 */
public class VTLdapSessionImpl implements LdapSession {

  /** map of connection name to pool */
  private static Map<String, BlockingLdapPool> poolMap = new HashMap<String, BlockingLdapPool>();
  
  /**
   * get or create the pool based on the server id
   * @param ldapServerId
   * @return the pool
   */
  public static BlockingLdapPool blockingLdapPool(String ldapServerId) {
    
    BlockingLdapPool blockingLdapPool = poolMap.get(ldapServerId);
    
    if (blockingLdapPool == null) {
      synchronized (VTLdapSessionImpl.class) {
        blockingLdapPool = poolMap.get(ldapServerId);
        
        if (blockingLdapPool == null) {
          
          GrouperLoaderLdapServer grouperLoaderLdapServer = retrieveLdapProfile(ldapServerId);
          
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
            SearchResultHandler[] handlers = new SearchResultHandler[grouperLoaderLdapServer.getSearchResultHandlers().length];
            for (int i = 0; i < handlers.length; i++) {
              handlers[i] = (SearchResultHandler)grouperLoaderLdapServer.getSearchResultHandlers()[i];
            }
            
            ldapConfig.setSearchResultHandlers(handlers);
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
          
          if (ldapPoolConfig.isValidateOnCheckIn() || ldapPoolConfig.isValidateOnCheckOut() || ldapPoolConfig.isValidatePeriodically()) {
            LdapValidator<Ldap> validator = null;

            String ldapValidator = GrouperLoaderConfig.retrieveConfig().propertyValueString("ldap." + ldapServerId + ".validator");

            if (StringUtils.equalsIgnoreCase(ldapValidator, CompareLdapValidator.class.getSimpleName())) {
              String validationDn = GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired("ldap." + ldapServerId + ".validatorCompareDn");
              String validationSearchFilterString = GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired("ldap." + ldapServerId + ".validatorCompareSearchFilterString");
              validator = new CompareLdapValidator(validationDn, new SearchFilter(validationSearchFilterString));
            } else if (StringUtils.equalsIgnoreCase(ldapValidator, ConnectLdapValidator.class.getSimpleName())) {
              validator = new ConnectLdapValidator();
            } else if (StringUtils.equalsIgnoreCase(ldapValidator, "SearchValidator")) {
              // ignore - doesn't exist in vt-ldap and we want configuration compatible.
            } else if (!StringUtils.isBlank(ldapValidator)) {
              Class<LdapValidator<Ldap>> validatorClass = GrouperUtil.forName(ldapValidator);
              validator = GrouperUtil.newInstance(validatorClass);
            }
            
            if (validator != null) {
              factory.setLdapValidator(validator);
            }
          }

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
  private static Object callbackLdapSession(
      String ldapServerId, LdapHandler<Ldap> ldapHandler) {
    
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
      
      LdapHandlerBean<Ldap> ldapHandlerBean = new LdapHandlerBean<Ldap>();
      
      ldapHandlerBean.setLdap(ldap);
        
      ret = ldapHandler.callback(ldapHandlerBean);

    } catch (RuntimeException re) {
      GrouperUtil.injectInException(re, "Problem with ldap conection: " + ldapServerId);
      throw re;
    } catch (Exception e) {
      throw new RuntimeException("Problem with ldap conection: " + ldapServerId, e);
    } finally {
      if (blockingLdapPool != null && ldap != null) {
        blockingLdapPool.checkIn(ldap);
      }
    }
    return ret;

  }

  /**
   * @see edu.internet2.middleware.grouper.ldap.LdapSession#list(java.lang.Class, java.lang.String, java.lang.String, edu.internet2.middleware.grouper.ldap.LdapSearchScope, java.lang.String, java.lang.String)
   */
  public <R> List<R> list(final Class<R> returnType, final String ldapServerId, 
      final String searchDn, final LdapSearchScope ldapSearchScope, final String filter, final String attributeName) {
    
    try {
      
      return (List<R>)callbackLdapSession(ldapServerId, new LdapHandler<Ldap>() {
        
        public Object callback(LdapHandlerBean<Ldap> ldapHandlerBean) throws NamingException {

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
  private static final Log LOG = GrouperUtil.getLog(VTLdapSessionImpl.class);

  /**
   * @see edu.internet2.middleware.grouper.ldap.LdapSession#listInObjects(java.lang.Class, java.lang.String, java.lang.String, edu.internet2.middleware.grouper.ldap.LdapSearchScope, java.lang.String, java.lang.String)
   */
  public <R> Map<String, List<R>> listInObjects(final Class<R> returnType, final String ldapServerId, 
      final String searchDn, final LdapSearchScope ldapSearchScope, final String filter, final String attributeName) {
    
    try {
      
      return (Map<String, List<R>>)callbackLdapSession(ldapServerId, new LdapHandler<Ldap>() {
        
        public Object callback(LdapHandlerBean<Ldap> ldapHandlerBean) throws NamingException {
  
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
              String baseDn = GrouperLoaderConfig.parseLdapBaseDnFromUrlConfig(ldapServerId);

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


  /**
   * @see edu.internet2.middleware.grouper.ldap.LdapSession#list(java.lang.String, java.lang.String, edu.internet2.middleware.grouper.ldap.LdapSearchScope, java.lang.String, java.lang.String[], java.lang.Long)
   */
  public List<LdapEntry> list(final String ldapServerId, final String searchDn,
      final LdapSearchScope ldapSearchScope, final String filter, final String[] attributeNames, final Long sizeLimit) {

    try {
      
      return (List<LdapEntry>)callbackLdapSession(ldapServerId, new LdapHandler<Ldap>() {
        
        public Object callback(LdapHandlerBean<Ldap> ldapHandlerBean) throws NamingException {

          Ldap ldap = ldapHandlerBean.getLdap();
          
          Iterator<SearchResult> searchResultIterator = null;
          
          SearchFilter searchFilterObject = new SearchFilter(filter);
          
          SearchControls searchControls = ldap.getLdapConfig().getSearchControls(attributeNames);
          
          if (ldapSearchScope != null) {
            searchControls.setSearchScope(ldapSearchScope.getSeachControlsConstant());
          }

          if (sizeLimit != null) {
            searchControls.setCountLimit(sizeLimit);
          }
          
          if (StringUtils.isBlank(searchDn)) {
            searchResultIterator = ldap.search(
                searchFilterObject, searchControls);
          } else {
            searchResultIterator = ldap.search(searchDn,
                searchFilterObject, searchControls);
          }
          
          List<LdapEntry> results = new ArrayList<LdapEntry>();
          while (searchResultIterator.hasNext()) {
            
            SearchResult searchResult = searchResultIterator.next();

            String nameInNamespace = searchResult.getName();
            //for some reason this returns: cn=test:testGroup,dc=upenn,dc=edu
            // instead of cn=test:testGroup,ou=groups,dc=upenn,dc=edu
            if (!StringUtils.isBlank(searchDn)) {
              String baseDn = GrouperLoaderConfig.parseLdapBaseDnFromUrlConfig(ldapServerId);

              if (!StringUtils.isBlank(baseDn) && nameInNamespace.endsWith("," + baseDn)) {
                
                //sub one to get the comma out of there
                nameInNamespace = nameInNamespace.substring(0, nameInNamespace.length() - (baseDn.length()+1));
                nameInNamespace += "," + searchDn + "," + baseDn;
              }
            }
            
            LdapEntry entry = new LdapEntry(nameInNamespace);
            for (String attributeName : attributeNames) {
              LdapAttribute attribute = new LdapAttribute(attributeName);
              
              Attribute sourceAttribute = searchResult.getAttributes().get(attributeName);
              if (sourceAttribute != null) {
                for (int i = 0; i < sourceAttribute.size(); i++) {
                  Object value = sourceAttribute.get(i);
                  if (value instanceof String) {
                    attribute.addStringValue((String)value);
                  } else if (value instanceof byte[]) {
                    attribute.addBinaryValue((byte[])value);
                  } else {
                    throw new RuntimeException("Unexpected type of value: " + value + ", dn=" + nameInNamespace);
                  }
                }
              }
              
              entry.addAttribute(attribute);
            }
            
            results.add(entry);
          }
          
          return results;
        }
      });
    } catch (RuntimeException re) {
      GrouperUtil.injectInException(re, "Error querying ldap server id: " + ldapServerId + ", searchDn: " + searchDn
          + ", filter: '" + filter + "', returning attributes: " + attributeNames);
      throw re;
    }
  }
  
  /**
   * @see edu.internet2.middleware.grouper.ldap.LdapSession#authenticate(java.lang.String, java.lang.String, java.lang.String)
   */
  public void authenticate(final String ldapServerId, final String userDn, final String password) {
    GrouperLoaderLdapServer grouperLoaderLdapServer = retrieveLdapProfile(ldapServerId);

    LdapConfig ldapConfig = null;
    
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

    if (grouperLoaderLdapServer.isTls()) {
      ldapConfig.setTls(grouperLoaderLdapServer.isTls());
    }

    if (!StringUtils.isBlank(grouperLoaderLdapServer.getSaslAuthorizationId())) {
      ldapConfig.setSaslAuthorizationId(grouperLoaderLdapServer.getSaslAuthorizationId());
    }
    if (!StringUtils.isBlank(grouperLoaderLdapServer.getSaslRealm())) {
      ldapConfig.setSaslRealm(grouperLoaderLdapServer.getSaslRealm());
    }
    
    if (grouperLoaderLdapServer.getBatchSize() != -1) {
      ldapConfig.setBatchSize(grouperLoaderLdapServer.getBatchSize());
    }
    
    if (grouperLoaderLdapServer.getCountLimit() != -1) {
      ldapConfig.setCountLimit(grouperLoaderLdapServer.getCountLimit());
    }
    
    if (grouperLoaderLdapServer.getTimeLimit() != -1) {
      ldapConfig.setTimeLimit(grouperLoaderLdapServer.getTimeLimit());
    }
    
    if (grouperLoaderLdapServer.getTimeout() != -1) {
      ldapConfig.setTimeout(grouperLoaderLdapServer.getTimeout());
    }
    
    if (grouperLoaderLdapServer.getPagedResultsSize() != -1) {
      ldapConfig.setPagedResultsSize(grouperLoaderLdapServer.getPagedResultsSize());
    }

    if (!StringUtils.isBlank(grouperLoaderLdapServer.getReferral())) {
      ldapConfig.setReferral(grouperLoaderLdapServer.getReferral());
    }
    
    
    ldapConfig.setBindDn(userDn);
    ldapConfig.setBindCredential(password);
    
    Ldap ldap = new Ldap(ldapConfig);

    try {
      ldap.connect();
    } catch (NamingException e) {
      throw new RuntimeException (e);
    } finally {
      try {
        ldap.close();
      } catch (Exception e) {
        // ignore
      }
    }
  }
  
  /**
   * get a profile by name from grouper-loader.properties
   * specify the ldap connection with user, pass, url, etc
   * the string after "ldap." is the name of the connection, and it should not have
   * spaces or other special chars in it
   * ldap.personLdap.user
   * ldap.personLdap.pass
   * ldap.personLdap.url
   * @param name
   * @return the db
   */
  private static GrouperLoaderLdapServer retrieveLdapProfile(String name) {
    
    GrouperLoaderLdapServer grouperLoaderLdapServer = new GrouperLoaderLdapServer();

    grouperLoaderLdapServer.setConfigFileFromClasspath(GrouperLoaderConfig.getPropertyString("ldap." + name + ".configFileFromClasspath"));

    {
      //#note the URL should start with ldap: or ldaps: if it is SSL.  
      //#It should contain the server and port (optional if not default), and baseDn, 
      //#e.g. ldaps://ldapserver.school.edu:636/dc=school,dc=edu
      //#ldap.personLdap.url = ldaps://ldapserver.school.edu:636/dc=school,dc=edu
      String url = GrouperLoaderConfig.retrieveConfig().propertyValueString("ldap." + name + ".url");
      
      if (StringUtils.isBlank(url) && StringUtils.isBlank(grouperLoaderLdapServer.getConfigFileFromClasspath())) {
        throw new RuntimeException("Cant find the ldap connection named: '" + name + "' in " +
            "the grouper-loader.properties.  Should have entry: ldap." + name + ".url or ldap." + name + ".configFileFromClasspath");
      }
      
      grouperLoaderLdapServer.setUrl(url);
    }
    
    {
      String user = GrouperLoaderConfig.retrieveConfig().propertyValueString("ldap." + name + ".user");
      //#ldap.personLdap.user = uid=someapp,ou=people,dc=myschool,dc=edu
      if (!StringUtils.isBlank(user)) {
        grouperLoaderLdapServer.setUser(user);
      }
    }
    
    {
      String pass = GrouperLoaderConfig.retrieveConfig().propertyValueString("ldap." + name + ".pass");
      if (!StringUtils.isBlank(pass)) {
        //might be in external file
        pass = Morph.decryptIfFile(pass);
        //#note the password can be stored encrypted in an external file
        //#ldap.personLdap.pass = secret
        grouperLoaderLdapServer.setPass(pass);
      }
    }

    
    //#optional, if you are using tls, set this to TRUE.  Generally you will not be using an SSL URL to use TLS...
    //#ldap.personLdap.tls = true
    grouperLoaderLdapServer.setTls(GrouperLoaderConfig.getPropertyBoolean("ldap." + name + ".tls", false));
    
    //#optional, if using sasl
    //#ldap.personLdap.saslAuthorizationId = 
    //#ldap.personLdap.saslRealm = 
    grouperLoaderLdapServer.setSaslAuthorizationId(GrouperLoaderConfig.retrieveConfig().propertyValueString("ldap." + name + ".saslAuthorizationId"));
    grouperLoaderLdapServer.setSaslRealm(GrouperLoaderConfig.retrieveConfig().propertyValueString("ldap." + name + ".saslRealm"));
    
    //#ldap.personLdap.batchSize = 
    grouperLoaderLdapServer.setBatchSize(GrouperLoaderConfig.retrieveConfig().propertyValueInt("ldap." + name + ".batchSize", -1));
        
    //#ldap.personLdap.countLimit = 
    grouperLoaderLdapServer.setCountLimit(GrouperLoaderConfig.retrieveConfig().propertyValueInt("ldap." + name + ".countLimit", -1));
    
    grouperLoaderLdapServer.setTimeLimit(GrouperLoaderConfig.retrieveConfig().propertyValueInt("ldap." + name + ".timeLimit", -1));

    grouperLoaderLdapServer.setTimeout(GrouperLoaderConfig.retrieveConfig().propertyValueInt("ldap." + name + ".timeout", -1));

    grouperLoaderLdapServer.setMinPoolSize(GrouperLoaderConfig.retrieveConfig().propertyValueInt("ldap." + name + ".minPoolSize", -1));

    grouperLoaderLdapServer.setMaxPoolSize(GrouperLoaderConfig.retrieveConfig().propertyValueInt("ldap." + name + ".maxPoolSize", -1));

    grouperLoaderLdapServer.setValidateOnCheckIn(GrouperLoaderConfig.getPropertyBoolean("ldap." + name + ".validateOnCheckIn", false));
    grouperLoaderLdapServer.setValidateOnCheckOut(GrouperLoaderConfig.getPropertyBoolean("ldap." + name + ".validateOnCheckOut", false));
    grouperLoaderLdapServer.setValidatePeriodically(GrouperLoaderConfig.getPropertyBoolean("ldap." + name + ".validatePeriodically", false));

    grouperLoaderLdapServer.setPagedResultsSize(GrouperLoaderConfig.getPropertyInt("ldap." + name + ".pagedResultsSize", -1));

    grouperLoaderLdapServer.setSearchResultHandlers(GrouperLoaderConfig.retrieveConfig().propertyValueString("ldap." + name + ".searchResultHandlers"));

    grouperLoaderLdapServer.setReferral(GrouperLoaderConfig.getPropertyString("ldap." + name + ".referral"));

    //#validateOnCheckout defaults to true if all other validate methods are false
    if (!grouperLoaderLdapServer.isValidateOnCheckIn() && !grouperLoaderLdapServer.isValidateOnCheckOut() && !grouperLoaderLdapServer.isValidatePeriodically()) {
      grouperLoaderLdapServer.setValidateOnCheckOut(true);
    }
    
    //#ldap.personLdap.validateTimerPeriod = 
    grouperLoaderLdapServer.setValidateTimerPeriod(GrouperLoaderConfig.retrieveConfig().propertyValueInt("ldap." + name + ".validateTimerPeriod", -1));
    
    //#ldap.personLdap.pruneTimerPeriod = 
    grouperLoaderLdapServer.setPruneTimerPeriod(GrouperLoaderConfig.retrieveConfig().propertyValueInt("ldap." + name + ".pruneTimerPeriod", -1));

    //#if connections expire after a certain amount of time, this is it, in millis, defaults to 300000 (5 minutes)
    //#ldap.personLdap.expirationTime = 
    grouperLoaderLdapServer.setExpirationTime(GrouperLoaderConfig.retrieveConfig().propertyValueInt("ldap." + name + ".expirationTime", -1));
    
    if (LOG.isDebugEnabled()) {
      LOG.debug("LDAP config for server id: " + name + ": " + grouperLoaderLdapServer);
    }
    
    return grouperLoaderLdapServer;
  }
}
