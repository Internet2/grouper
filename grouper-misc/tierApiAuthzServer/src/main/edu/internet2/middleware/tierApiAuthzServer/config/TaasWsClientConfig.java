/*******************************************************************************
 * Copyright 2016 Internet2
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
 *******************************************************************************/
/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.tierApiAuthzServer.config;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import edu.internet2.middleware.tierApiAuthzServer.j2ee.TaasFilterJ2ee;
import edu.internet2.middleware.tierApiAuthzServer.util.ExpirableCache;
import edu.internet2.middleware.tierApiAuthzServer.util.StandardApiServerConfig;
import edu.internet2.middleware.tierApiAuthzServer.util.StandardApiServerUtils;
import edu.internet2.middleware.tierApiAuthzServerExt.org.apache.commons.lang.StringUtils;
import edu.internet2.middleware.tierApiAuthzServerExt.org.apache.commons.logging.Log;
import edu.internet2.middleware.tierApiAuthzServerExt.org.apache.commons.logging.LogFactory;



/**
 * configuration for a specific client of the service
 */
public class TaasWsClientConfig extends AsasConfigPropertiesCascadeBase {

  /**
   * classpath of main config
   */
  private String mainConfigPath;

  /** logger */
  private static final Log LOG = LogFactory.getLog(TaasWsClientConfig.class);

  /**
   * threadlocal for the main config
   */
  private static ThreadLocal<String> mainConfigPathThreadLocal = new InheritableThreadLocal<String>();
  
  /**
   * construct, note, needs a config path in threadlocal
   */
  private TaasWsClientConfig() {
    this(mainConfigPathThreadLocal.get());
  }

  /**
   * construct, note, needs a config path in threadlocal
   * @param theMainConfigPath
   */
  private TaasWsClientConfig(String theMainConfigPath) {
    if (StringUtils.isBlank(theMainConfigPath)) {
      throw new RuntimeException("Need a mainConfigPath in threadlocal to instantiate this object");
    }
    this.mainConfigPath = theMainConfigPath;
  }

  /**
   * cache to hold client configs, if they are changed on disk, it takes this long to read again
   */
  private static ExpirableCache<String, TaasWsClientConfig> clientConfigCache = new ExpirableCache<String, TaasWsClientConfig>(1);
  
  /**
   * cache to hold which client configs dont exist
   */
  private static ExpirableCache<String, Boolean> clientConfigCacheNotExist = new ExpirableCache<String, Boolean>(1);
  
  /**
   * cache to failsafe hold configs if the configs get corrupted (does this need to be concurrent?)
   */
  private static Map<String, TaasWsClientConfig> clientConfigCacheFailsafe = new HashMap<String, TaasWsClientConfig>();
    
  /**
   * config file for logged in user
   * @return config file
   */
  public static TaasWsClientConfig retrieveClientConfigForLoggedInUser() {
    String loggedInUser = TaasFilterJ2ee.retrieveUserPrincipalNameFromRequest();
    return retrieveClientConfig(loggedInUser);
  }
  
  /**
   * get a boolean from a list of properties, if not in any, then return default.
   * If a value is blank or "inherit", then go up the chain
   * @param defaultValue
   * @param configKeysHierarchical are config keys to try, in order, could be true, false, or inherit
   * @return true or false
   */
  public boolean propertyValueBoolean(boolean defaultValue, String... configKeysHierarchical) {
    
    for (String configKey : configKeysHierarchical) {
      String propertyValue = propertyValueString(configKey);
      if (StringUtils.equalsIgnoreCase(propertyValue, "inherit") || StringUtils.isBlank(propertyValue)) {
        continue;
      }
      return this.booleanValueOfString(propertyValue, configKey);
    }
    return defaultValue;
  }

  /**
   * retrieve a config by login id
   * @param loginid
   * @return the config (might be cached)
   */
  public static TaasWsClientConfig retrieveClientConfig(final String loginid) {

    TaasWsClientConfig taasWsClientConfig = clientConfigCache.get(loginid);

    Boolean configForLoginIdDoesntExist = clientConfigCacheNotExist.get(loginid);
    if (configForLoginIdDoesntExist != null && configForLoginIdDoesntExist) {
      throw new RuntimeException("Cant find config for loginid");
    }

    //if didnt find the config in the primary cache
    if (taasWsClientConfig == null) {
      
      synchronized(TaasWsClientConfig.class) {

        taasWsClientConfig = clientConfigCache.get(loginid);
              
        if (taasWsClientConfig == null) {
            
          File configDir = new File(StandardApiServerConfig.retrieveConfig().propertyValueString("tierApiAuthzServer.clientConfigDir"));
          List<File> files = StandardApiServerUtils.listFilesByExtensionRecursive(configDir, ".properties");
          Set<String> users = new HashSet<String>();
          for (File file : files) {
            Properties properties = StandardApiServerUtils.propertiesFromFile(file);
            String usersForThisConfig = (String)properties.get("tierClient.users");
            if (!StringUtils.isBlank(usersForThisConfig)) {
              
              //we have a user properties file
              TaasWsClientConfig newTaasWsClientConfig = new TaasWsClientConfig("file:" + file.getAbsolutePath());
              for (String user : StandardApiServerUtils.splitTrim(usersForThisConfig, ",")) {
                
                //if we have the same user in multiple files
                if (users.contains(user)) {
                  TaasWsClientConfig taasWsClientConfigInMap = clientConfigCache.get(user);

                  String configMapPath = taasWsClientConfigInMap == null ? null : taasWsClientConfigInMap.mainConfigPath;
                  LOG.error("Multiple configs with user: " + user + ", (ignoring): " + newTaasWsClientConfig.mainConfigPath 
                      + ", (using): " + configMapPath);
                  continue;

                }

                clientConfigCache.put(user, newTaasWsClientConfig);
              }
            }
          }
        }
      }

      if (taasWsClientConfig == null) {
        taasWsClientConfig = clientConfigCache.get(loginid);
      }

      if (taasWsClientConfig == null) {

        //cant find the primary, or it is rebuilding, use the secondary
        taasWsClientConfig = clientConfigCacheFailsafe.get(loginid);
        
      }
      
      //if we dont have it by now, we are in trouble
      if (taasWsClientConfig == null) {
        clientConfigCacheNotExist.put(loginid, true);
        throw new RuntimeException("Cant find config by loginid: " + loginid);
      }
    }
    
    return (TaasWsClientConfig)taasWsClientConfig.retrieveFromConfigFileOrCache();
  }

  /**
   * @see edu.internet2.middleware.tierApiAuthzServer.config.AsasConfigPropertiesCascadeBase#getSecondsToCheckConfigKey()
   */
  @Override
  protected String getSecondsToCheckConfigKey() {
    return "tierClient.config.secondsBetweenUpdateChecks";
  }

  /**
   * @see edu.internet2.middleware.tierApiAuthzServer.config.AsasConfigPropertiesCascadeBase#clearCachedCalculatedValues()
   */
  @Override
  public void clearCachedCalculatedValues() {
  }

  /**
   * @see edu.internet2.middleware.tierApiAuthzServer.config.AsasConfigPropertiesCascadeBase#getMainConfigClasspath()
   */
  @Override
  protected String getMainConfigClasspath() {
    return this.mainConfigPath;
  }

  /**
   * @see edu.internet2.middleware.tierApiAuthzServer.config.AsasConfigPropertiesCascadeBase#getHierarchyConfigKey()
   */
  @Override
  protected String getHierarchyConfigKey() {
    return "tierClient.config.hierarchy";
  }

  /**
   * @see edu.internet2.middleware.tierApiAuthzServer.config.AsasConfigPropertiesCascadeBase#getMainExampleConfigClasspath()
   */
  @Override
  protected String getMainExampleConfigClasspath() {
    return null;
  }

  /**
   * @see edu.internet2.middleware.tierApiAuthzServer.config.AsasConfigPropertiesCascadeBase#cacheSuffix()
   */
  @Override
  protected String cacheSuffix() {
    return this.mainConfigPath;
  }

  /**
   * @see edu.internet2.middleware.tierApiAuthzServer.config.AsasConfigPropertiesCascadeBase#retrieveFromConfigFiles()
   */
  @Override
  protected AsasConfigPropertiesCascadeBase retrieveFromConfigFiles() {
    mainConfigPathThreadLocal.set(this.mainConfigPath);
    try {
      return super.retrieveFromConfigFiles();
    } finally {
      mainConfigPathThreadLocal.remove();
    }
  }
  
}
