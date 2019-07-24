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

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.ui.text.TextBundleBean;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.config.ConfigPropertiesCascadeBase;
import edu.internet2.middleware.grouperClient.config.ConfigPropertiesCascadeBase.ConfigFile;

/**
 * hierarchical config class for grouper-ui.properties
 * @author mchyzer
 *
 */
public class GrouperUiConfig {

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
   * default bundle
   */
  private TextBundleBean textBundleDefault = null;
  
  
  
  
  /**
   * default bundle
   * @return the textBundleDefault
   */
  public TextBundleBean textBundleDefault() {
    if (this.textBundleDefault == null) {
      this.textBundleFromCountry();
    }
    return this.textBundleDefault;
  }

  /**
   * country to text bundle
   */
  private Map<String, TextBundleBean> textBundleFromCountry  = null;
  
  /**
   * language_country to text bundle
   */
  private Map<String, TextBundleBean> textBundleFromLanguageAndCountry  = null;
  
  /**
   * language to text bundle
   */
  private Map<String, TextBundleBean> textBundleFromLanguage  = null;

  /** logger */
  protected static final Log LOG = GrouperUtil.getLog(GrouperUiConfig.class);
  
  /**
   * country to text bundle
   * @return the map
   */
  public Map<String, TextBundleBean> textBundleFromLanguage() {
    //init
    Map<String, TextBundleBean> theTextBundleFromLanguage = this.textBundleFromLanguage;
    if (theTextBundleFromLanguage == null) {
      //init here
      this.textBundleFromCountry();
      theTextBundleFromLanguage = this.textBundleFromLanguage;
    }
    if (theTextBundleFromLanguage == null) {
      throw new RuntimeException("Why is textBundleFromLanguage map null????");
    }
    return theTextBundleFromLanguage;
  }

  /**
   * country to text bundle
   * @return the map
   */
  public Map<String, TextBundleBean> textBundleFromLanguageAndCountry() {
    //init
    Map<String, TextBundleBean> theTextBundleFromLanguageAndCountry = this.textBundleFromLanguageAndCountry;
    if (theTextBundleFromLanguageAndCountry == null) {
      //init here
      this.textBundleFromCountry();
      theTextBundleFromLanguageAndCountry = this.textBundleFromLanguageAndCountry;
    }
    if (theTextBundleFromLanguageAndCountry == null) {
      throw new RuntimeException("Why is textBundleFromLanguage map null????");
    }
    return theTextBundleFromLanguageAndCountry;
  }

  /**
   * country to text bundle
   * @return the map
   */
  public Map<String, TextBundleBean> textBundleFromCountry() {
    if (this.textBundleFromCountry == null) {
      
      synchronized (this) {
        
        if (this.textBundleFromCountry == null) {
          
          Map<String, TextBundleBean> tempBundleFromCountry = new HashMap<String, TextBundleBean>();
          Map<String, TextBundleBean> tempBundleFromLanguage = new HashMap<String, TextBundleBean>();
          Map<String, TextBundleBean> tempBundleFromLanguageAndCountry = new HashMap<String, TextBundleBean>();
          
          Pattern pattern = Pattern.compile("^grouper\\.text\\.bundle\\.(.*)\\.fileNamePrefix$");
          
          boolean foundDefault = false;
          
          for (Object keyObject : this.properties().keySet()) {
            String key = (String)keyObject;
            Matcher matcher = pattern.matcher(key);
            if (matcher.matches()) {
              
              String bundleKey = matcher.group(1);

              String fileNamePrefix = this.propertyValueString(key);
              String language = StringUtils.defaultString(this.propertyValueString("grouper.text.bundle." + bundleKey + ".language")).toLowerCase();
              String country = StringUtils.defaultString(this.propertyValueString("grouper.text.bundle." + bundleKey + ".country")).toLowerCase();
              
              TextBundleBean textBundleBean = new TextBundleBean();
              
              textBundleBean.setCountry(country);
              textBundleBean.setLanguage(language);
              textBundleBean.setFileNamePrefix(fileNamePrefix);

              if (StringUtils.equals(bundleKey, propertyValueStringRequired("grouper.text.defaultBundleIndex"))) {
                foundDefault = true;
                this.textBundleDefault = textBundleBean;
              }
              
              //first in wins
              if (!tempBundleFromCountry.containsKey(country)) {
                tempBundleFromCountry.put(country, textBundleBean);
              }
              if (!tempBundleFromLanguage.containsKey(language)) {
                tempBundleFromLanguage.put(language, textBundleBean);
              }
              String languageAndCountry = language + "_" + country;
              if (tempBundleFromLanguageAndCountry.containsKey(languageAndCountry)) {
                LOG.error("Language and country already defined! " + languageAndCountry);
              }
              tempBundleFromLanguageAndCountry.put(languageAndCountry, textBundleBean);
            }
          }
          
          if (!foundDefault) {
            throw new RuntimeException("Cant find default bundle index: '" 
                + propertyValueStringRequired("grouper.text.defaultBundleIndex") + "', should have a key: grouper.text.bundle."
                + propertyValueStringRequired("grouper.text.defaultBundleIndex") + ".fileNamePrefix");
          }
          
          this.textBundleFromCountry = Collections.unmodifiableMap(tempBundleFromCountry);
          this.textBundleFromLanguage = Collections.unmodifiableMap(tempBundleFromLanguage);
          this.textBundleFromLanguageAndCountry = Collections.unmodifiableMap(tempBundleFromLanguageAndCountry);
          
        }
      }
    }
    return this.textBundleFromCountry;
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
   * get the underlying properties for the config ui
   * @return the config files
   */
  public List<ConfigFile> internalRetrieveConfigFiles() {
    return this.grouperUiConfigInApi.internalRetrieveConfigFiles();
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
