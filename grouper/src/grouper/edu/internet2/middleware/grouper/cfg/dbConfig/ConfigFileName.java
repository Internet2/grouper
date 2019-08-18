/**
 * Copyright 2018 Internet2
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

package edu.internet2.middleware.grouper.cfg.dbConfig;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.cfg.GrouperCacheConfig;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.misc.GrouperStartup;
import edu.internet2.middleware.grouper.ui.util.GrouperUiConfigInApi;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ws.GrouperWsConfigInApi;
import edu.internet2.middleware.grouperClient.config.ConfigPropertiesCascadeBase;
import edu.internet2.middleware.grouperClient.util.GrouperClientConfig;
import edu.internet2.middleware.subject.config.SubjectConfig;


/**
 *
 */
public enum ConfigFileName {

//  lets hold off on this one since it will be a circular dependency when running the config overlay
//  the other properties files need to read this one to get to the database, but if this one has
//  to get to the database to get its own config...   hmm...
//  /**
//   * grouper.hibernate.properties
//   */
//  GROUPER_HIBERNATE_PROPERTIES("grouper.hibernate.properties"), 
//  
  /**
   * grouper.cache.properties
   */
  GROUPER_CACHE_PROPERTIES("grouper.cache.properties", "grouper.cache.base.properties") {

    @Override
    public ConfigPropertiesCascadeBase getConfig() {
      return GrouperCacheConfig.retrieveConfig();
    }
    
  },
  
  /**
   * grouper.client.properties
   */
  GROUPER_CLIENT_PROPERTIES("grouper.client.properties", "grouper.client.base.properties") {

    @Override
    public ConfigPropertiesCascadeBase getConfig() {
      return GrouperClientConfig.retrieveConfig();
    }
  },
  
  /**
   * grouper-loader.properties
   */
  GROUPER_LOADER_PROPERTIES("grouper-loader.properties", "grouper-loader.base.properties") {

    @Override
    public ConfigPropertiesCascadeBase getConfig() {
      return GrouperLoaderConfig.retrieveConfig();
    }
  }, 
  
  /**
   * grouper.properties
   */
  GROUPER_PROPERTIES("grouper.properties", "grouper.base.properties") {

    @Override
    public ConfigPropertiesCascadeBase getConfig() {
      return GrouperConfig.retrieveConfig();
    }
  }, 

  /**
   * grouper-ui.properties
   */
  GROUPER_UI_PROPERTIES("grouper-ui.properties", "grouper-ui-ng.base.properties") {

    @Override
    public ConfigPropertiesCascadeBase getConfig() {
      return GrouperUiConfigInApi.retrieveConfig();
    }
    
  },
  
  /**
   * grouper-ws.properties
   */
  GROUPER_WS_PROPERTIES("grouper-ws.properties", "grouper-ws-ng.base.properties") {

    @Override
    public ConfigPropertiesCascadeBase getConfig() {
      return GrouperWsConfigInApi.retrieveConfig();
    }
  },
  
  /**
   * subject.properties
   */
  SUBJECT_PROPERTIES("subject.properties", "subject.base.properties") {

    @Override
    public ConfigPropertiesCascadeBase getConfig() {
      return SubjectConfig.retrieveConfig();
    }
    
  };
  
  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    GrouperStartup.startup();
    edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName.GROUPER_WS_PROPERTIES.configFileMetadata();
  }
  
  /**
   * 
   * @param key
   * @return config item metadata related to this key
   */
  public static ConfigItemMetadata findConfigItemMetdata(String key) {
    if (key == null) {
      return null;
    }
    for (ConfigFileName configFileName : ConfigFileName.values()) {
      
      ConfigFileMetadata configFileMetadata = configFileName.configFileMetadata();
      for (ConfigSectionMetadata configSectionMetadata : configFileMetadata.getConfigSectionMetadataList()) {
        for (ConfigItemMetadata configItemMetadata : configSectionMetadata.getConfigItemMetadataList()) {
          
          if (StringUtils.equals(key, configItemMetadata.getKey())) {
            
            return configItemMetadata;
            
          }
        }
      }
    }
    return null;
  }
  
  /**
   * order the config gets loaded
   * the lower the number the first it is read
   */
  private String configFileName;
  
  /**
   * where is this file on the classpath
   */
  private String classpath;
  
  /**
   * construct
   * @param theConfigFileName
   * @param theClasspath
   */
  private ConfigFileName(String theConfigFileName, String theClasspath) {
    this.configFileName = theConfigFileName;
    this.classpath = theClasspath;
  }
  
  /**
   * 
   * @return the contents or null if not on classpath
   */
  public String fileContents() {
    String contents = GrouperUtil.readResourceIntoString(this.classpath, true);
    return contents;
  }

  /**
   * 
   * @return config file metadata
   */
  public ConfigFileMetadata configFileMetadata() {
    String contents = this.fileContents();
    if (StringUtils.isBlank(contents)) {
      return null;
    }
    ConfigFileMetadata configFileMetadata = ConfigFileMetadata.generateMetadataForConfigFile(this, contents);
    return configFileMetadata;
  }
  
  /**
   * @return the classpath
   */
  public String getClasspath() {
    return this.classpath;
  }

  /**
   * order the config gets loaded
   * the lower the number the first it is read
   * @return the order
   */
  public String getConfigFileName() {
    return this.configFileName;
  }
  
  /**
   * do a case-insensitive matching
   * 
   * @param string
   * @param exceptionOnNull will not allow null or blank entries
   * @return the enum or null or exception if not found
   */
  public static ConfigFileName valueOfIgnoreCase(String string, boolean exceptionOnNull) {
    
    // match the config file name
    for (ConfigFileName configFileName : ConfigFileName.values()) {
      if (StringUtils.equalsIgnoreCase(string, configFileName.getConfigFileName())) {
        return configFileName;
      }
    }
    
    return GrouperUtil.enumValueOfIgnoreCase(ConfigFileName.class, 
        string, exceptionOnNull);

  }

  /**
   * get a recent instance of the config
   * @return a recent instance
   */
  public abstract ConfigPropertiesCascadeBase getConfig();
  
}
