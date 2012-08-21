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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.cache.GrouperCache;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ws.GrouperWsConfig;
import edu.internet2.middleware.grouper.ws.util.GrouperServiceUtils;
import edu.internet2.middleware.morphString.Morph;


/**
 * <pre>
 * basic kerberos authentication for grouper, settings are specified in grouper-ws.properties
 * note: this can be used for rest and soap, though it is not a bastion of security:
 *  1. for soap, ws-security would be better since a ticket is passed instead of user/pass
 *  2. for rest, Im not sure there is another option
 *  3. the user/pass is transmitted in basic auth, so make sure SSL is on
 *  4. passing the user/pass is not how kerberos should work since kerberos passes tickets and not passes
 *  5. the user is authenticated to the kdc, but an ssl service is not invoked, which would be the next
 *  level of verification since it might be possible for the kdc to be spoofed to the grouper-ws
 * 
 * </pre>
 */
public class WsGrouperKerberosAuthentication implements WsCustomAuthentication {

  /**
   * 
   * @param args
   * @throws Exception 
   */
  public static void main(String[] args) throws Exception {
    GrouperUtil.waitForInput();
    for (int i=0; i<2; i++) {
      for (int j=0;j<100;j++) {
        //TODO  put this in external file
        if (!authenticateKerberos("penngroups/medley.isc-seo.upenn.edu", 
            Morph.decryptIfFile("R:/home/appadmin/pass/pennGroups/pennGroupsMedley.pass"))) {
          throw new RuntimeException("Problem!");
        }
        System.gc();
        System.out.println(j + ":" + i + ", " 
            + ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())/(double)(1024*1024)) + " megs used");
        Thread.sleep(100);
      }
      GrouperUtil.waitForInput();
    }
  }

  /** logger */
  private static final Log LOG = LogFactory.getLog(WsGrouperKerberosAuthentication.class);

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
      WsGrouperKerberosAuthentication.class.getName() + ".userCache", 10000, false, 60*1, 60*1, false);

  /**
   * 
   * @see edu.internet2.middleware.grouper.ws.security.WsCustomAuthentication#retrieveLoggedInSubjectId(javax.servlet.http.HttpServletRequest)
   */
  public String retrieveLoggedInSubjectId(HttpServletRequest httpServletRequest)
      throws RuntimeException {
    
    String authHeader = httpServletRequest.getHeader("Authorization");

    //if not header, we cant go to kerberos
    if (StringUtils.isBlank(authHeader)) {
      LOG.error("No authorization header in HTTP");
      return null;
    }
    
    //hash the authHeader
    String authHeaderHash = GrouperUtil.encryptSha(authHeader);
    
    String cachedLogin = loginCache.get(authHeaderHash);
    if (!StringUtils.isBlank(cachedLogin)) {
      LOG.debug("Retrieved cached login");
      return cachedLogin;
    }
    LOG.debug("Login not in cache");
    
    Matcher matcher = regexPattern.matcher(authHeader);
    String authHeaderBase64Part = null;
    if (matcher.matches()) {
      authHeaderBase64Part = matcher.group(1);
    }
    
    //either not basic or something else wrong
    if (StringUtils.isBlank(authHeaderBase64Part)) {
      //dont log password
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
    
    if (authenticateKerberos(user, pass)) {
      
      loginCache.put(authHeaderHash, user);
      
      return user;
    }
    
    LOG.error("Error authenticating user: " + user);
    return null;
  }

  /**
   * see if a user and pass are correct with berberos
   * @param principal
   * @param password
   * @return true for ok, false for not
   */
  public static boolean authenticateKerberos(String principal, String password) {

    // Obtain a LoginContext, needed for authentication. Tell it 
    // to use the LoginModule implementation specified by the 
    // entry named "JaasSample" in the JAAS login configuration 
    // file and to also use the specified CallbackHandler.

    File jaasConf = GrouperServiceUtils.fileFromResourceName("jaas.conf");

    if (jaasConf == null) {
      throw new RuntimeException("Cant find jaas.conf!");
    }

    System.setProperty("java.security.krb5.realm", GrouperWsConfig.retrieveConfig().propertyValueString("kerberos.realm"));
    System.setProperty("java.security.krb5.kdc", GrouperWsConfig.retrieveConfig().propertyValueString("kerberos.kdc.address"));
    System.setProperty("java.security.auth.login.config", jaasConf.getAbsolutePath());
    //System.setProperty("sun.security.krb5.debug", "true");
    
    LoginContext lc = null;
    try {
      lc = new LoginContext("JaasSample", new GrouperWsKerberosHandler(principal, password));
      

    } catch (LoginException le) {
      LOG.error("Cannot create LoginContext. ", le);
      return false;
    } catch (SecurityException se) {
      LOG.error("Cannot create LoginContext. " , se);
      return false;
    }

    try {

      // attempt authentication
      lc.login();

      try {
        lc.logout();
      } catch (Exception e) {
        LOG.warn(e);
      }
      return true;
    } catch (LoginException le) {
      
      LOG.warn(le);
    }

    return false;
  }

  
}
