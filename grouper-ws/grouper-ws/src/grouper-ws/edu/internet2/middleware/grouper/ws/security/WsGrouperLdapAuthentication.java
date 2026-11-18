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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.ldaptive.FilterTemplate;

import edu.internet2.middleware.grouper.cache.GrouperCache;
import edu.internet2.middleware.grouper.j2ee.Authentication;
import edu.internet2.middleware.grouper.ldap.LdapEntry;
import edu.internet2.middleware.grouper.ldap.LdapSearchScope;
import edu.internet2.middleware.grouper.ldap.LdapSessionUtils;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ws.GrouperWsConfig;

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
  private static final Log LOG = GrouperUtil.getLog(WsGrouperLdapAuthentication.class);

  /**
   * cache the logins in a hash cache
   */
  private static GrouperCache<String, String> loginCache = new GrouperCache<String, String>(
      WsGrouperLdapAuthentication.class.getName() + ".userCache", 10000, false, 60*1, 60*1, false);

  /**
   * 
   * @see edu.internet2.middleware.grouper.ws.security.WsCustomAuthentication#retrieveLoggedInSubjectId(javax.servlet.http.HttpServletRequest)
   */
  @Override
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
      
      //split based on user/pass
      String user = Authentication.retrieveUsername(authHeader);
      String pass = Authentication.retrievePassword(authHeader);

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

      // refuse a blank user or password before anything goes to the directory.  An LDAP simple bind
      // with a DN and an empty password is an "unauthenticated bind" (RFC 4513 5.1.2), which some
      // directories (e.g. Active Directory by default) answer with success without checking anything
      if (StringUtils.isBlank(principal) || StringUtils.isBlank(password)) {
        if (LOG.isDebugEnabled()) {
          debugMap.put("blankUserOrPassword", "true");
        }
        LOG.warn("Refusing LDAP authentication with a blank user or password, user: '" + principal + "'");
        return false;
      }

      // the user name is returned as the logged in user id, and the web service splits a user id with
      // four colons into a subject source and a subject id.  A password check must not let the caller
      // pick the subject source (e.g. g:isa::::GrouperSystem), so refuse the separator here
      if (principal.contains(GrouperWsConfig.WS_SEPARATOR)) {
        if (LOG.isDebugEnabled()) {
          debugMap.put("userContainsSourceSeparator", "true");
        }
        LOG.warn("Refusing LDAP authentication for a user name containing '" + GrouperWsConfig.WS_SEPARATOR + "': '" + principal + "'");
        return false;
      }

      //# if ldap authn should be used, this is the prefix of the userId when connecting to ldap, e.g. uid=
      //ws.authn.ldap.loginIdPrefix = uid=
      //
      //# if ldap authn should be used, this is the suffix to the userId when connecting to ldap, e.g. ,ou=users,dc=school,dc=edu
      //ws.authn.ldap.loginIdSuffix = ,ou=entities,dc=upenn,dc=edu
      String loginDnPrefix = StringUtils.trimToEmpty(GrouperWsConfig.getPropertyString("ws.authn.ldap.loginDnPrefix"));
      String loginDnSuffix = StringUtils.trimToEmpty(GrouperWsConfig.getPropertyString("ws.authn.ldap.loginDnSuffix"));
      String findUserBase = StringUtils.trimToEmpty(GrouperWsConfig.getPropertyString("ws.authn.ldap.findUserBase"));
      String findUserFilter = StringUtils.trimToEmpty(GrouperWsConfig.getPropertyString("ws.authn.ldap.findUserFilter"));

      String grouperLoaderLdapConfigId = GrouperWsConfig.getPropertyString("ws.authn.ldap.grouperLoaderLdapConfigId");
      if (StringUtils.isBlank(grouperLoaderLdapConfigId)) {
        if (LOG.isDebugEnabled()) {
          debugMap.put("ws.authn.ldap.grouperLoaderLdapConfigName not configured", "true");
        }
        throw new RuntimeException("ws.authn.ldap.grouperLoaderLdapConfigName must be configured in the grouper-ws.properties");
      }
      
      if (!StringUtils.isEmpty(findUserFilter)) {
        FilterTemplate template = new FilterTemplate(findUserFilter);
        template.setParameter("username", principal);
        String filter = template.format();
        List<LdapEntry> ldapEntries = LdapSessionUtils.ldapSession().list(grouperLoaderLdapConfigId, findUserBase, LdapSearchScope.SUBTREE_SCOPE, filter, new String[] { "1.1" }, null);
        if (ldapEntries.size() != 1) {
          LOG.warn("error for principal: " + principal + ", filter " + filter + " returned " + ldapEntries.size() + " results");
          return false;
        }
        
        userDn = ldapEntries.get(0).getDn();
      } else {
        // use previous behavior.  When the prefix ends in an attribute name and equals sign (e.g. uid=)
        // the user name is an RDN value: escape it (RFC 4514) so it cannot add RDNs or change the DN.
        // Other prefixes (e.g. a blank prefix with an @school.edu suffix, an AD user principal name)
        // are not DNs and are left as they were
        String principalInDn = loginDnPrefix.endsWith("=") ? escapeDnAttributeValue(principal) : principal;
        userDn = loginDnPrefix + principalInDn + loginDnSuffix;
      }
      
      if (LOG.isDebugEnabled()) {
        debugMap.put("findUserBase", findUserBase);
        debugMap.put("findUserFilter", findUserFilter);
        debugMap.put("loginDnPrefix", loginDnPrefix);
        debugMap.put("principal", principal);
        debugMap.put("loginDnSuffix", loginDnSuffix);
        debugMap.put("userDn", userDn);
      }
            
      LdapSessionUtils.ldapSession().authenticate(grouperLoaderLdapConfigId, userDn, password);
      
      if (LOG.isDebugEnabled()) {
        debugMap.put("authenticated", "true");
      }
      
      return true;
    } catch (Exception le) {
      if (LOG.isDebugEnabled()) {
        debugMap.put("authenticated", "false");
        debugMap.put("error", ExceptionUtils.getStackTrace(le));
      }

      LOG.warn("error for principal: " + principal + ", dn: " + userDn, le);
    } finally {
      if (LOG.isDebugEnabled()) {
        LOG.debug(GrouperUtil.mapToString(debugMap));
      }
    }


    return false;
  }

  /**
   * escape a string for use as an attribute value in a distinguished name, RFC 4514 section 2.4:
   * a backslash before each comma, plus, double quote, backslash, less than, greater than,
   * semicolon and equals sign, before a leading space or # and before a trailing space, and a
   * backslash and 00 for a NUL character
   * @param value
   * @return the escaped value, or null if null
   */
  public static String escapeDnAttributeValue(String value) {
    if (value == null) {
      return null;
    }
    StringBuilder result = new StringBuilder();
    for (int i = 0; i < value.length(); i++) {
      char c = value.charAt(i);
      if (c == (char)0) {
        result.append("\\00");
        continue;
      }
      boolean escape = c == ',' || c == '+' || c == '"' || c == '\\' || c == '<' || c == '>' || c == ';' || c == '=';
      if (i == 0 && (c == ' ' || c == '#')) {
        escape = true;
      }
      if (i == value.length() - 1 && c == ' ') {
        escape = true;
      }
      if (escape) {
        result.append('\\');
      }
      result.append(c);
    }
    return result.toString();
  }

}
