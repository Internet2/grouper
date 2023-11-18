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
package edu.internet2.middleware.grouper.ui.util;

import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;

import edu.internet2.middleware.grouperClient.config.ConfigPropertiesCascadeBase;

/**
 * hierarchical config class for grouper-ui.properties
 * @author mchyzer
 *
 */
public class GrouperUiConfig {

  /**
   * 
   */
  private GrouperUiConfig() {
  }
  
  /**
   * retrieve a config from the config file or from cache
   * @return the config object
   */
  public static GrouperUiConfig retrieveConfig() {
    GrouperUiConfig grouperUiConfig = new GrouperUiConfig();
    grouperUiConfig.grouperUiConfigInApi = GrouperUiConfigInApi.retrieveConfig();
    return grouperUiConfig;
  }

  /**
   * delegate
   */
  private GrouperUiConfigInApi grouperUiConfigInApi = null;

  /**
   * @see ConfigPropertiesCascadeBase#clearCachedCalculatedValues()
   */
  public void clearCachedCalculatedValues() {
    this.grouperUiConfigInApi.clearCachedCalculatedValues();
  }

  /**
   * if the key is there, whether or not the value is blank
   * @param key
   * @return true or false
   */
  public boolean containsKey(String key) {
    
    return this.grouperUiConfigInApi.containsKey(key);
    
  }

  /**
   * get the properties object for this config file
   * @return the properties
   */
  public Properties properties() {
    return this.grouperUiConfigInApi.properties();
  }

  /**
   * find all keys/values with a certain pattern in a properties file.
   * return the keys.  if none, will return the empty set, not null set
   * @param pattern
   * @return the keys.  if none, will return the empty set, not null set
   */
  public Map<String, String> propertiesMap(Pattern pattern) {
    return this.grouperUiConfigInApi.propertiesMap(pattern);
  }

  /**
   * override map for properties for testing
   * @return the override map
   */
  public Map<String, String> propertiesOverrideMap() {
    return this.grouperUiConfigInApi.propertiesOverrideMap();
  }

  /**
   * override map for properties in thread local to be used in a web server or the like, based on property class
   * this is static since the properties class can get reloaded, but these shouldnt
   * @return the override map
   */
  public Map<String, String> propertiesThreadLocalOverrideMap() {
    return this.grouperUiConfigInApi.propertiesThreadLocalOverrideMap();
  }

  /**
   * 
   * @return the set of names
   */
  @SuppressWarnings("unchecked")
  public Set<String> propertyNames() {    
    return this.grouperUiConfigInApi.propertyNames();
  }

  /**
   * get a boolean and validate from grouper.client.properties or null if not there
   * @param key
   * @return the boolean or null
   */
  public Boolean propertyValueBoolean(String key) {
    return this.grouperUiConfigInApi.propertyValueBoolean(key);
  }

  /**
   * get a boolean and validate from grouper.client.properties
   * @param key
   * @param defaultValue
   * @return the string
   */
  public boolean propertyValueBoolean(String key, boolean defaultValue) {
    return this.grouperUiConfigInApi.propertyValueBoolean(key, defaultValue);
  }

  /**
   * get a boolean and validate from grouper.client.properties
   * @param key
   * @return the string
   */
  public boolean propertyValueBooleanRequired(String key) {
    return this.grouperUiConfigInApi.propertyValueBooleanRequired(key);
  }

  /**
   * get a boolean and validate from grouper.client.properties
   * @param key
   * @return the int or null if there
   */
  public Integer propertyValueInt(String key ) {
    return this.grouperUiConfigInApi.propertyValueInt(key);
  }

  /**
   * get a boolean and validate from grouper.client.properties
   * @param key
   * @param defaultValue
   * @return the string
   */
  public int propertyValueInt(String key, int defaultValue ) {
    return this.grouperUiConfigInApi.propertyValueInt(key, defaultValue);
  }

  /**
   * get a boolean and validate from grouper.client.properties
   * @param key
   * @return the string
   */
  public int propertyValueIntRequired(String key) {
    return this.grouperUiConfigInApi.propertyValueIntRequired(key);
  }

  /**
   * get the property value as a string or null if not there
   * @param key
   * @return the property value
   */
  public String propertyValueString(String key) {
    return this.grouperUiConfigInApi.propertyValueString(key);
  }

  /**
   * get the property value as a string
   * @param key
   * @param defaultValue
   * @return the property value
   */
  public String propertyValueString(String key, String defaultValue) {
    return this.grouperUiConfigInApi.propertyValueString(key, defaultValue);
  }

  /**
   * get the property value as a string
   * @param key
   * @return the property value
   */
  public String propertyValueStringRequired(String key) {
    return this.grouperUiConfigInApi.propertyValueStringRequired(key);
  }
  

}
