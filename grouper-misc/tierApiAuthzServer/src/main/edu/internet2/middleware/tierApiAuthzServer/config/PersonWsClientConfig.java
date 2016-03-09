/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.tierApiAuthzServer.config;

import java.util.HashMap;
import java.util.Map;

import edu.internet2.middleware.tierApiAuthzServer.util.ExpirableCache;
import edu.internet2.middleware.tierApiAuthzServer.util.StandardApiServerUtils;



/**
 * configuration for a specific client of the service
 */
public class PersonWsClientConfig {

  /**
   * 
   */
  private PersonWsClientConfig() {
  }

  /**
   * cache to hold client configs, if they are changed on disk, it takes this long to read again
   */
  private static ExpirableCache<String, PersonWsClientConfig> clientConfigCache = new ExpirableCache<String, PersonWsClientConfig>(1);
  
  /**
   * cache to hold which client configs dont exist
   */
  private static ExpirableCache<String, Boolean> clientConfigCacheNotExist = new ExpirableCache<String, Boolean>(1);
  
  /**
   * cache to failsafe hold configs if the configs get corrupted
   */
  private static Map<String, PersonWsClientConfig> clientConfigCacheFailsafe = new HashMap<String, PersonWsClientConfig>();
  
  /**
   * build the configs from the hierarchy from the directory, put them in the cache
   */
  private static void buildConfigs() {
    
  }
  
  
  
  /**
   * flag to tell if we are building configs right now
   */
  private static boolean buildingConfigs = false;
  
  /**
   * flag to say we have at least built the configs once
   */
  private static boolean hasBuiltConfigsOnce = false;
  
  /**
   * retrieve a config by login id
   * @param loginid
   * @return the config (might be cached)
   */
  public static PersonWsClientConfig retrieveClientConfig(final String loginid) {

    //array so we can reference from thread
    final PersonWsClientConfig[] personWsClientConfig = new PersonWsClientConfig[]{clientConfigCache.get(loginid)};

//    {
//      Boolean doesntExist = clientConfigCacheNotExist.get(loginid)
//      //if ( != null) 
//    }
    
    //if didnt find the config in the primary cache
    if (personWsClientConfig[0] == null) {
      
      synchronized(PersonWsClientConfig.class) {

        //if we already have another thread building them, then we might be ok
        if (!buildingConfigs) {
          new Thread(new Runnable() {

            //run in a thread so that clients arent waiting on this
            public void run() {
              synchronized(PersonWsClientConfig.class) {
                if (buildingConfigs) {
                  return;
                }
                
                personWsClientConfig[0] = clientConfigCache.get(loginid);
                
                if (personWsClientConfig[0] != null) {
                  return;
                }
                
                try {
                  buildingConfigs = true;
                  

                  //we have successfully built the configs once
                  hasBuiltConfigsOnce = true;
                } finally {
                  buildingConfigs = false;
                }
              }
            }
            
          }).start();
        }
      }
      
      //cant find the primary, or it is rebuilding, use the secondary
      personWsClientConfig[0] = clientConfigCacheFailsafe.get(loginid);
      
      if (personWsClientConfig[0] == null && !hasBuiltConfigsOnce) {

        //there is no failsafe, we need to wait for the thread, lets wait max 10 seconds
        for (int i=0;i<10;i++) {

          StandardApiServerUtils.sleep(1000);
          
          if (!buildingConfigs) {
            personWsClientConfig[0] = clientConfigCache.get(loginid);
            
            if (personWsClientConfig[0] != null) {
              break;
            }
          }
        }

      }
      
      //if we dont have it by now, we are in trouble
      if (personWsClientConfig[0] == null) {
        throw new RuntimeException("Cant find config by loginid: " + loginid);
      }
    }
    
    return personWsClientConfig[0];
  }
  
}
