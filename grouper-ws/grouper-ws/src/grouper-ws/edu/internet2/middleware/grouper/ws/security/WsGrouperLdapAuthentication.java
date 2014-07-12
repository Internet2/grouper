/*******************************************************************************
 * Copyright 2012 Internet2
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
 ******************************************************************************/
/*
 * @author mchyzer
 * $Id: WsGrouperKerberosAuthentication.java,v 1.3 2009-04-13 20:24:22 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ws.security;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.naming.AuthenticationException;
import javax.naming.InvalidNameException;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.loader.ldap.GrouperLoaderLdapServer;
import edu.internet2.middleware.grouper.cache.GrouperCache;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ws.GrouperWsConfig;
import edu.vt.middleware.ldap.Ldap;
import edu.vt.middleware.ldap.LdapConfig;


/**
 * <pre>
 * ldap bind authentication for grouper, settings are specified in grouper-ws.properties, and the grouper-loader.properties
 * note: this can be used for rest and soap, though it is not a bastion of security:
 *  1. for soap, ws-security would be better since a ticket is passed instead of user/pass
 *  2. for rest, Im not sure there is another option
 *  3. the user/pass is transmitted in basic auth, so make sure SSL is on
 *  4. if using proxied web services, the password goes to the middle component
 * 
 * </pre>
 */
public class WsGrouperLdapAuthentication implements WsCustomAuthentication {

  /**
   * 
   * @param args
   * @throws Exception 
   */
  public static void main(String[] args) throws Exception {
    String user = GrouperUtil.readFileIntoString(new File("r:/temp/ldapUser.txt"));
    String ldapPass = StringUtils.trimToNull(GrouperUtil.readFileIntoString(new File("r:/temp/ldapPass.txt")));
    System.out.println("Correct? " + authenticateLdap(user, ldapPass));
  }

  /** logger */
  private static final Log LOG = LogFactory.getLog(WsGrouperLdapAuthentication.class);

  /**
   * this should be something like: Basic QWxhZGRabcdefGVuIHNlc2FtZQ==
   * ^\\s*Basic\\s+([^\\s]+)$
   * "^\\s*       Start of string and optional whitespace
   * Basic\\s+    Must be basic auth, and have some whitespace after this
   * ([^\\s]+)$   Capture the next non-whitespace string, then end of input
   */
  private static Pattern regexPattern = Pattern.compile("^\\s*Basic\\s+([^\\s]+)$");
  
  /**
   * cache the logins in a hash cache
   */
  private static GrouperCache<String, String> loginCache = new GrouperCache<String, String>(
      WsGrouperLdapAuthentication.class.getName() + ".userCache", 10000, false, 60*1, 60*1, false);

  /**
   * get or create the pool based on the server id
   * @param ldapServerId
   * @return the pool
   */
  private static LdapConfig ldapConfig(String ldapServerId) {
          
    Map<String, Object> debugMap = LOG.isDebugEnabled() ? new LinkedHashMap<String, Object>() : null;

    try {

      if (LOG.isDebugEnabled()) {
        debugMap.put("method", "ldapConfig()");
      }

      GrouperLoaderLdapServer grouperLoaderLdapServer = GrouperLoaderConfig.retrieveLdapProfile(ldapServerId);

      if (LOG.isDebugEnabled()) {
        debugMap.put("ldapConfig (note, user/pass/pooling will not be used)", grouperLoaderLdapServer.toString());
      }

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
      
      //#optional, if you are using tls, set this to true.  Generally you will not be using an SSL URL to use TLS...
      //#ldap.personLdap.tls = false
      if (grouperLoaderLdapServer.isTls()) {
        ldapConfig.setTls(grouperLoaderLdapServer.isTls());
      }
      
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
  
      //#ldap.personLdap.referral
      if (!StringUtils.isBlank(grouperLoaderLdapServer.getReferral())) {
        ldapConfig.setReferral(grouperLoaderLdapServer.getReferral());
      }
      
      return ldapConfig;
    } finally {
      if (LOG.isDebugEnabled()) {
        LOG.debug(GrouperUtil.mapToString(debugMap));
      }
    }
    
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.ws.security.WsCustomAuthentication#retrieveLoggedInSubjectId(javax.servlet.http.HttpServletRequest)
   */
  public String retrieveLoggedInSubjectId(HttpServletRequest httpServletRequest)
      throws RuntimeException {
    
    Map<String, Object> debugMap = LOG.isDebugEnabled() ? new LinkedHashMap<String, Object>() : null;
    
    try {
      String authHeader = httpServletRequest.getHeader("Authorization");
  
      if (LOG.isDebugEnabled()) {
        debugMap.put("method", "retrieveLoggedInSubjectId()");
      }
      
      //if not header, we cant go to kerberos
      if (StringUtils.isBlank(authHeader)) {
        if (LOG.isDebugEnabled()) {
          debugMap.put("errorAuthz", "No authorization header in HTTP");
        }
        LOG.error("No authorization header in HTTP");
        return null;
      }
      
      //hash the authHeader
      String authHeaderHash = GrouperUtil.encryptSha(authHeader);
      
      boolean isCaching = GrouperWsConfig.getPropertyBoolean("ws.authn.ldap.cacheResults", true);
  
      if (LOG.isDebugEnabled()) {
        debugMap.put("isCaching", isCaching);
      }

      if (isCaching) {
        String cachedLogin = loginCache.get(authHeaderHash);
        if (!StringUtils.isBlank(cachedLogin)) {
          if (LOG.isDebugEnabled()) {
            debugMap.put("Retrieved cached login", true);
          }
          return cachedLogin;
        }
        if (LOG.isDebugEnabled()) {
          debugMap.put("Login not in cache", true);
        }
      }
      
      Matcher matcher = regexPattern.matcher(authHeader);
      String authHeaderBase64Part = null;
      if (matcher.matches()) {
        authHeaderBase64Part = matcher.group(1);
      }
      
      //either not basic or something else wrong
      if (StringUtils.isBlank(authHeaderBase64Part)) {
        //dont log password
        if (LOG.isDebugEnabled()) {
          debugMap.put("Cant find base64 part in auth header", true);
        }
        LOG.error("Cant find base64 part in auth header");
        return null;
      }
      
      //unencrypt this
      byte[] base64Bytes = authHeaderBase64Part.getBytes();
      byte[] unencodedBytes = Base64.decodeBase64(base64Bytes);
      
      String unencodedString = new String(unencodedBytes);
      
      //split based on user/pass
      String user = GrouperUtil.prefixOrSuffix(unencodedString, ":", true);
      String pass = GrouperUtil.prefixOrSuffix(unencodedString, ":", false);

      if (LOG.isDebugEnabled()) {
        debugMap.put("user", user);
      }

      
      if (authenticateLdap(user, pass)) {
        
        if (isCaching) {
          loginCache.put(authHeaderHash, user);
        }
        
        return user;
      }
      
      if (LOG.isDebugEnabled()) {
        debugMap.put("Error authenticating user", true);
      }
      LOG.warn("Error authenticating user: " + user);
      return null;
    } finally {
      if (LOG.isDebugEnabled()) {
        LOG.debug(GrouperUtil.mapToString(debugMap));
      }
    }
  }

  /**
   * see if a user and pass are correct with ldap
   * @param principal
   * @param password
   * @return true for ok, false for not
   */
  public static boolean authenticateLdap(String principal, String password) {

    Map<String, Object> debugMap = LOG.isDebugEnabled() ? new LinkedHashMap<String, Object>() : null;
    String userDn = null;
    try {

      if (LOG.isDebugEnabled()) {
        debugMap.put("method", "authenticateLdap()");
      }

      //# if ldap authn should be used, this is the prefix of the userId when connecting to ldap, e.g. uid=
      //ws.authn.ldap.loginIdPrefix = uid=
      //
      //# if ldap authn should be used, this is the suffix to the userId when connecting to ldap, e.g. ,ou=users,dc=school,dc=edu
      //ws.authn.ldap.loginIdSuffix = ,ou=entities,dc=upenn,dc=edu
      String loginDnPrefix = StringUtils.trimToEmpty(GrouperWsConfig.getPropertyString("ws.authn.ldap.loginDnPrefix"));
      String loginDnSuffix = StringUtils.trimToEmpty(GrouperWsConfig.getPropertyString("ws.authn.ldap.loginDnSuffix"));

      userDn = loginDnPrefix + principal + loginDnSuffix;
      
      if (LOG.isDebugEnabled()) {
        debugMap.put("loginDnPrefix", loginDnPrefix);
        debugMap.put("principal", principal);
        debugMap.put("loginDnSuffix", loginDnSuffix);
        debugMap.put("userDn", userDn);
      }
      
      String grouperLoaderLdapConfigId = GrouperWsConfig.getPropertyString("ws.authn.ldap.grouperLoaderLdapConfigId");
      if (StringUtils.isBlank(grouperLoaderLdapConfigId)) {
        if (LOG.isDebugEnabled()) {
          debugMap.put("ws.authn.ldap.grouperLoaderLdapConfigName not configured", "true");
        }
        throw new RuntimeException("ws.authn.ldap.grouperLoaderLdapConfigName must be configured in the grouper-ws.properties");
      }
      
      LdapConfig ldapConfig = ldapConfig(grouperLoaderLdapConfigId);
      
      //set user/pass
      
      ldapConfig.setBindDn(userDn);
      ldapConfig.setBindCredential(password);
      
      Ldap ldap = new Ldap(ldapConfig);

      ldap.connect();
      
      if (LOG.isDebugEnabled()) {
        debugMap.put("authenticated", "true");
      }
      
      return true;
    } catch (Exception le) {
      if (LOG.isDebugEnabled()) {
        debugMap.put("authenticated", "false");
        debugMap.put("error", ExceptionUtils.getFullStackTrace(le));
      }
      //authn and invalid name exception is when bad user/pass I believe
      if ((!(le instanceof AuthenticationException)) && (!(le instanceof InvalidNameException))) {
        LOG.error("error for principal: " + principal + ", dn: " + userDn, le);
      } else {
        LOG.warn("error for principal: " + principal + ", dn: " + userDn, le);
      }
    } finally {
      if (LOG.isDebugEnabled()) {
        LOG.debug(GrouperUtil.mapToString(debugMap));
      }
    }


    return false;
  }
  
}
