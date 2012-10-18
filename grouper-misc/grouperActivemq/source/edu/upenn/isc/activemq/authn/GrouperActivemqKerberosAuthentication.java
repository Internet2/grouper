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
 * $Id: GrouperActivemqKerberosAuthentication.java,v 1.3 2009-04-13 20:24:22 mchyzer Exp $
 */
package edu.upenn.isc.activemq.authn;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouperClient.failover.FailoverClient;
import edu.internet2.middleware.grouperClient.failover.FailoverConfig;
import edu.internet2.middleware.grouperClient.failover.FailoverLogic;
import edu.internet2.middleware.grouperClient.failover.FailoverLogicBean;
import edu.internet2.middleware.grouperClient.failover.FailoverConfig.FailoverStrategy;
import edu.internet2.middleware.grouperClient.util.ExpirableCache;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.grouperClientExt.edu.internet2.middleware.morphString.Morph;
import edu.upenn.isc.activemq.config.GrouperActivemqConfig;
import edu.upenn.isc.activemq.utils.GrouperActivemqUtils;


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
public class GrouperActivemqKerberosAuthentication {

  /**
   * 
   */
  private static final String GROUPER_FAILOVER_CLIENT_KERB_NAME = "grouperActivemqKerberos";

  /**
   * 
   */
  private static final String USER_PASS_SEPARATOR = "|-|-|-|-|-|-|";

  /**
   * 
   * @param args
   * @throws Exception 
   */
  public static void main(String[] args) throws Exception {
//    GrouperActivemqUtils.waitForInput();
//    for (int i=0; i<2; i++) {
//      for (int j=0;j<100;j++) {
//        if (!authenticateKerberos("", 
//            Morph.decryptIfFile(""))) {
//          throw new RuntimeException("Problem!");
//        }
//        System.gc();
//        System.out.println(j + ":" + i + ", " 
//            + ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())/(double)(1024*1024)) + " megs used");
//        Thread.sleep(100);
//      }
//      GrouperActivemqUtils.waitForInput();
//    }
    
    for (int i=0;i<10;i++) {
      System.out.println(authenticateKerberosHelper("mchyzer", "fcmpp9THfcmpp9TH"));
    }
    
  }

  /** logger */
  private static final Log LOG = LogFactory.getLog(GrouperActivemqKerberosAuthentication.class);

  /**
   * cache the logins in a hash cache
   */
  private static ExpirableCache<String, Boolean> loginSuccessCache = new ExpirableCache<String, Boolean>(
    GrouperActivemqConfig.retrieveConfig().propertyValueInt("kerberos.login.cache.success.minutes", 5));

  /**
   * cache the logins in a hash cache
   */
  private static ExpirableCache<String, Boolean> loginFailedCache = new ExpirableCache<String, Boolean>(
    GrouperActivemqConfig.retrieveConfig().propertyValueInt("kerberos.login.cache.failed.minutes", 1));


  /**
   * see if a user and pass are correct with berberos
   * @param principal
   * @param password
   * @return true for ok, false for not
   */
  public static boolean authenticateKerberos(String principal, String password) {
    //hash the authHeader
    String authHeaderHash = GrouperActivemqUtils.encryptSha(principal + USER_PASS_SEPARATOR + password);

    {
      Boolean cachedFailedLogin = loginFailedCache.get(authHeaderHash);
      if (cachedFailedLogin != null) {
        LOG.debug("Retrieved cached failed login");
        return false;
      }
    }
    
    {
      Boolean cachedSuccessLogin = loginSuccessCache.get(authHeaderHash);
      if (cachedSuccessLogin != null) {
        LOG.debug("Retrieved cached success login");
        return true;
      }
    }
    
    LOG.debug("Login not in cache");

    //do the kerberos
    boolean result = authenticateKerberosHelper(principal, password);
    
    //add to cache
    if (result) {
      loginSuccessCache.put(authHeaderHash, true);
    } else {
      loginFailedCache.put(authHeaderHash, true);
    }
    
    return result;
  }
  
  /**
   * map from kerberos address to an object which can synchronized on
   */
  private static Map<String, Object> synchronizedObjects = new HashMap<String, Object>();

  /**
   * see if a user and pass are correct with kerberos
   * @param principal
   * @param password
   * @return true for ok, false for not
   */
  private static boolean authenticateKerberosHelper(final String principal, final String password) {
    String addresses = GrouperActivemqConfig.retrieveConfig().propertyValueString("kerberos.kdc.address");
    if (GrouperClientUtils.isBlank(addresses)) {
      throw new RuntimeException("Why is kerberos kdc address blank???");
    }
    
    if (!addresses.contains(",")) {
      //dont use with failover client so we dont have that dependency
      return authenticateKerberosHelper(principal, password, addresses);
    } 


    initFailoverClient(addresses);
    
    //we need to rotate through and call with failover client
    String result = null;
    try {
      result = FailoverClient.failoverLogic(GROUPER_FAILOVER_CLIENT_KERB_NAME, new FailoverLogic<String>() {
        
        /**
         * @see FailoverLogic#logic
         */
        @Override
        public String logic(FailoverLogicBean failoverLogicBean) {
          boolean authenticated = authenticateKerberosHelper(principal, password, failoverLogicBean.getConnectionName());
          if (!authenticated) {
            throw new RuntimeException("Not authenticated");
          }
          return "" + authenticated;
        }
      });
    } catch(Exception e) {
      //since all tries throw exception even if bad password, just assume bad password at this point
      LOG.warn("error", e);
      return false;
    }
    return GrouperClientUtils.booleanValue(result, false);
    
  }


  /**
   * init failover client, note, it does this each time since the address list can change
   * @param addresses
   */
  private static void initFailoverClient(String addresses) {
    FailoverConfig failoverConfig = new FailoverConfig();
     
    //if there are no errors in connections, then use the same connection for 30 minutes
    failoverConfig.setAffinitySeconds(2400);
     
    List<String> addressList = GrouperClientUtils.splitTrimToList(addresses, ",");
    
    //if there are no errors in connections, it will use the 1st tier connection names before the 2nd tier
    //note that this configuration example will only work for readonly queries, if it is a readwrite query,
    //then there would not be any readonlyConnections available
    failoverConfig.setConnectionNames(addressList);
     
    //this is a label to identify this "pool" of connections
    failoverConfig.setConnectionType(GROUPER_FAILOVER_CLIENT_KERB_NAME);
     
    //if there are no errors in connections, and if there is no affinity, this is the strategy to pick which connection
    //active/active will pick one at random from the 1st tier connections, active/standby will use the connections in order
    failoverConfig.setFailoverStrategy(FailoverStrategy.activeStandby);
     
    //this is how long it will try on connection and wait for a response until giving up and trying another connection
    failoverConfig.setTimeoutSeconds(100);
     
    //after you have cycled through all the connections you can wait a little longer for one of the connections to finish
    failoverConfig.setExtraTimeoutSeconds(10);
     
    //this is how much time to remember that a connection had errors.  If an error hasnt occurred in a certain amount of time,
    //it will be forgotten
    failoverConfig.setMinutesToKeepErrors(5);
     
    //if you have a lot of hibernate mapped classes or something, it will give a buffer for the first X seconds for the JVM
    //to load and initialize
    failoverConfig.setSecondsForClassesToLoad(20);
     
    //register this configuration
    FailoverClient.initFailoverClient(failoverConfig);
  }

  
  /**
   * see if a user and pass are correct with kerberos
   * @param principal
   * @param password
   * @param address kdc address, e.g. kerberos1.upenn.edu
   * @return true for ok, false for not
   */
  private static boolean authenticateKerberosHelper(String principal, String password, String address) {

    if (GrouperClientUtils.isBlank(address)) {
      throw new RuntimeException("Why is kerberos kdc address blank???");
    }
    
    //translate from map so that we dont get no instances of strings
    Object synchronizedObject = synchronizedObjects.get(address);
    
    if (synchronizedObject == null) {
      synchronized (GrouperActivemqKerberosAuthentication.class) {
        synchronizedObject = synchronizedObjects.get(address);
        
        if (synchronizedObject == null) {
          synchronizedObject = new Object();
          synchronizedObjects.put(address, synchronizedObject);
        }
      }
    }
    
    //synchronize on the address object since we are dealing with system properties
    synchronized (synchronizedObject) {
      // Obtain a LoginContext, needed for authentication. Tell it 
      // to use the LoginModule implementation specified by the 
      // entry named "JaasSample" in the JAAS login configuration 
      // file and to also use the specified CallbackHandler.

      File jaasConf = GrouperClientUtils.fileFromResourceName("jaas.conf");

      if (jaasConf == null) {
        throw new RuntimeException("Cant find jaas.conf!");
      }

      System.setProperty("java.security.krb5.realm", GrouperActivemqConfig.retrieveConfig()
          .propertyValueString("kerberos.realm"));
      System.setProperty("java.security.krb5.kdc", address);
      System.setProperty("java.security.auth.login.config", jaasConf.getAbsolutePath());
      //System.setProperty("sun.security.krb5.debug", "true");
      
      LoginContext lc = null;
      try {
        lc = new LoginContext("JaasSample_" + address, new GrouperActivemqKerberosHandler(principal, password));
        

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

  
}
