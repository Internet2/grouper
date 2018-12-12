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

import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 *
 */
public enum ConfigFileName {

  /**
   * grouper.properties
   */
  GROUPER_PROPERTIES("grouper.properties"), 
  
  /**
   * grouper.hibernate.properties
   */
  GROUPER_HIBERNATE_PROPERTIES("grouper.hibernate.properties"), 
  
  /**
   * grouper-loader.properties
   */
  GROUPER_LOADER_PROPERTIES("grouper-loader.properties"), 
  
  /**
   * grouper.cache.properties
   */
  GROUPER_CACHE_PROPERTIES("grouper.cache.properties"),
  
  /**
   * subject.properties
   */
  SUBJECT_PROPERTIES("subject.properties"),
  
  /**
   * grouper-ui.properties
   */
  GROUPER_UI_PROPERTIES("grouper-ui.properties"),
  
  /**
   * grouper-ws.properties
   */
  GROUPER_WS_PROPERTIES("grouper-ws.properties"),
  
  /**
   * grouper.client.properties
   */
  GROUPER_CLIENT_PROPERTIES("grouper.client.properties");
  
  /**
   * order the config gets loaded
   * the lower the number the first it is read
   */
  private String configFileName;
  
  /**
   * construct
   * @param theConfigFileName
   */
  private ConfigFileName(String theConfigFileName) {
    this.configFileName = theConfigFileName;
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

}
