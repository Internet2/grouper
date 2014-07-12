/*******************************************************************************
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
 ******************************************************************************/
package edu.internet2.middleware.grouperClient.config;


/**
 * 
 * @author mchyzer
 *
 */
public class ConfigPropertiesOriginalHasHierarchy extends ConfigPropertiesCascadeBase {

  /**
   * retrieve a config from the config file or from cache
   * @return the config object
   */
  public static ConfigPropertiesOriginalHasHierarchy retrieveConfig() {
    return retrieveConfig(ConfigPropertiesOriginalHasHierarchy.class);
  }

  /**
   * @see ConfigPropertiesCascadeBase#getMainConfigClasspath()
   */
  @Override
  protected String getMainConfigClasspath() {
    return "testCascadeConfig2.properties";
  }

  /**
   * @see ConfigPropertiesCascadeBase#getMainExampleConfigClasspath()
   */
  @Override
  protected String getMainExampleConfigClasspath() {
    return "testCascadeConfig-example2.properties";
  }

  /**
   * @see ConfigPropertiesCascadeBase#clearCachedCalculatedValues()
   */
  @Override
  public void clearCachedCalculatedValues() {
    
  }

  /**
   * @see ConfigPropertiesCascadeBase#getHierarchyConfigKey()
   */
  @Override
  protected String getHierarchyConfigKey() {
    return "config.hierarchy2";
  }

  /**
   * @see ConfigPropertiesCascadeBase#getSecondsToCheckConfigKey
   */
  @Override
  protected String getSecondsToCheckConfigKey() {
    return "config.checkConfigEverySeconds2";
  }
  
  
}
