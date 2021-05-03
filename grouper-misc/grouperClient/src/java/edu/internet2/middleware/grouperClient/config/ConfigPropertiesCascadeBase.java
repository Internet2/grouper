/**
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
 */
package edu.internet2.middleware.grouperClient.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.internet2.middleware.grouperClient.config.db.ConfigDatabaseLogic;
import edu.internet2.middleware.grouperClient.util.GrouperClientLog;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.logging.Log;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.logging.LogFactory;


/**
 * Base class for a cascaded config.  Extend this class to have a config
 * based on a certain file. 
 * 
 * @author mchyzer
 *
 */
public abstract class ConfigPropertiesCascadeBase {

  /**
   * assign that things are initted, so database config is ok to use and errors should be thrown
   */
  public static void assignInitted() {
    configSingletonFromClass = null;
    configFileCache = null;
    clearCache();
//    if (LOG.isDebugEnabled()) {
//      LOG.debug("initted called from", new RuntimeException("initted"));
//    }
  }
  
  /**
   * help subclasses manipulate properties.  note, this is only for subclasses...
   * @return properties
   */
  protected Properties internalProperties() {
    return this.properties;
  }
  
  /** if a key ends with this, then it is an EL property */
  private static final String EL_CONFIG_SUFFIX = ".elConfig";

  /**
   * log object
   */
  private static final Log LOG = LogFactory.getLog(ConfigPropertiesCascadeBase.class);

  /**
   * this is used to tell engine where the default and example config is...
   */
  private static Map<Class<? extends ConfigPropertiesCascadeBase>, ConfigPropertiesCascadeBase> configSingletonFromClass = null;

  /**
   * retrieve a config from the config file or from cache
   * @param <T> class which is the return type of config class
   * @param configClass 
   * @return the config object never null
   */
  @SuppressWarnings("unchecked")
  protected static <T extends ConfigPropertiesCascadeBase> T retrieveConfig(Class<T> configClass) {
    
    if (configSingletonFromClass == null) {
      configSingletonFromClass = 
        new HashMap<Class<? extends ConfigPropertiesCascadeBase>, ConfigPropertiesCascadeBase>();
    }
    
    ConfigPropertiesCascadeBase configPropertiesCascadeBase = configSingletonFromClass.get(configClass);
    if (configPropertiesCascadeBase == null) {
      configPropertiesCascadeBase = ConfigPropertiesCascadeUtils.newInstance(configClass, true);
      configSingletonFromClass.put(configClass, configPropertiesCascadeBase);
      
    }
    //from the singleton, get the real config class
    return (T)configPropertiesCascadeBase.retrieveFromConfigFileOrCache();
  }


  /**
   * if its ok to put the config file in the same directory as a jar,
   * then return a class in the jar here
   * @return the class or null if not available
   */
  protected Class<?> getClassInSiblingJar() {
    return null;
  }
  
  /**
   * config key of the time in seconds to check config.  -1 means dont check again
   * @return config key
   */
  protected abstract String getSecondsToCheckConfigKey();
  
  /**
   * 
   */
  public static void clearCache() {
    clearCacheThisOnly();
    ConfigDatabaseLogic.clearCache();
  }

  /**
   * 
   */
  public static void clearCacheThisOnly() {
    if (configFileCache != null) {
      configFileCache.clear();
    }
  }

  /**
   * if there are things that are calculated, clear them out (e.g. if an override is set)
   */
  public abstract void clearCachedCalculatedValues();
  
  /** override map for properties in thread local to be used in a web server or the like */
  private static ThreadLocal<Map<Class<? extends ConfigPropertiesCascadeBase>, Map<String, String>>> propertiesThreadLocalOverrideMap 
    = null;
  
  /**
   * override map for properties in thread local to be used in a web server or the like, based on property class
   * this is static since the properties class can get reloaded, but these shouldnt
   * @return the override map
   */
  public Map<String, String> propertiesThreadLocalOverrideMap() {
    if (propertiesThreadLocalOverrideMap == null) {
      propertiesThreadLocalOverrideMap = new ThreadLocal<Map<Class<? extends ConfigPropertiesCascadeBase>, Map<String, String>>>();
    }

    Map<Class<? extends ConfigPropertiesCascadeBase>, Map<String, String>> overrideMap = propertiesThreadLocalOverrideMap.get();
    if (overrideMap == null) {
      overrideMap = new HashMap<Class<? extends ConfigPropertiesCascadeBase>, Map<String, String>>();
      propertiesThreadLocalOverrideMap.set(overrideMap);
    }
    Map<String, String> propertiesOverrideMapLocal = overrideMap.get(this.getClass());
    if (propertiesOverrideMapLocal == null) {
      propertiesOverrideMapLocal = new HashMap<String, String>();
      overrideMap.put(this.getClass(), propertiesOverrideMapLocal);
    }
    return propertiesOverrideMapLocal;
  }

  /** override map for properties, for testing, put properties in here, based on config class
   * this is static since the properties class can get reloaded, but these shouldnt
   */
  private static Map<Class<? extends ConfigPropertiesCascadeBase>, Map<String, String>> propertiesOverrideMap 
    = null;

  /**
   * 
   * @return the set of names
   */
  @SuppressWarnings("unchecked")
  public Set<String> propertyNames() {    
    
    Set<String> result = new TreeSet<String>();
    result.addAll((Set<String>)(Object)this.propertiesHelper(false).keySet());
    return result;
  }

  /**
   * override map for properties for testing
   * @return the override map
   */
  public Map<String, String> propertiesOverrideMap() {
    if (propertiesOverrideMap == null) {
      propertiesOverrideMap 
        = new LinkedHashMap<Class<? extends ConfigPropertiesCascadeBase>, Map<String, String>>();
    }
    Map<String, String> overrideMap = propertiesOverrideMap.get(this.getClass());
    if (overrideMap == null) {
      overrideMap = new LinkedHashMap<String, String>();
      propertiesOverrideMap.put(this.getClass(), overrideMap);
    }
    return overrideMap;
  }

  /**
   * get the properties object for this config file
   * @return the properties
   */
  public Properties properties() {
    return propertiesHelper(true);
  }
  
  /**
   * 
   * @param properties
   * @param propertyName
   * @param propertyValue
   */
  private static void assignProperty(Properties properties, String propertyName, String propertyValue) {
    //make sure to remove .elConfig if this is not an elconfig
    if (propertyName == null) {
      return;
    }
    if (propertyName.endsWith(".elConfig")) {
      properties.remove(GrouperClientUtils.stripEnd(propertyName, ".elConfig"));
    } else {
      properties.remove(propertyName + ".elConfig");
    }
    properties.put(propertyName, propertyValue);
  }
  
  /**
   * get the properties object for this config file
   * @param setValues if we should set the values for the properties.  
   * if not, the values might not be correct, but this will be more performant
   * depending on how many EL properties there are
   * @return the properties
   */
  @SuppressWarnings("unchecked")
  protected Properties propertiesHelper(boolean setValues) {
    Properties tempResult = new Properties();
    
    tempResult.putAll(this.properties);
    
    Map<String, String> localPropertiesOverrideMap = propertiesOverrideMap();
    
    for (String key: localPropertiesOverrideMap.keySet()) {
      assignProperty(tempResult, key, ConfigPropertiesCascadeUtils.defaultString(localPropertiesOverrideMap.get(key)));
    }
    
    localPropertiesOverrideMap = propertiesThreadLocalOverrideMap();
    
    for (String key: localPropertiesOverrideMap.keySet()) {
      assignProperty(tempResult, key, ConfigPropertiesCascadeUtils.defaultString(localPropertiesOverrideMap.get(key)));
    }

    Properties result = new Properties();

    // first do non el configs
    for (String key : (Set<String>)(Object)tempResult.keySet()) {
      
      String value = setValues ? tempResult.getProperty(key) : "";
      
      //lets look for EL
      if (!key.endsWith(EL_CONFIG_SUFFIX)) {
        
        //cant be null, or hashtable exception
        result.put(key, ConfigPropertiesCascadeUtils.defaultString(value));

      }
    }

    // then do EL
    for (String key : (Set<String>)(Object)tempResult.keySet()) {

      String value = setValues ? tempResult.getProperty(key) : "";
      
      //lets look for EL
      if (key.endsWith(EL_CONFIG_SUFFIX)) {
        
        if (setValues) {
          //process the EL
          value = ConfigPropertiesCascadeUtils.substituteExpressionLanguage(value, null, true, true, true, false);
        }
        
        //change the key name
        key = key.substring(0, key.length() - EL_CONFIG_SUFFIX.length());

        //cant be null, or hashtable exception
        result.put(key, ConfigPropertiesCascadeUtils.defaultString(value));

      }
    }
    
    substituteLocalReferences(result);

    
    return result;
    
  }


  /**
   * you can refer to other properties with $$propertyName$$
   * @param result
   */
  protected void substituteLocalReferences(Properties result) {
    
    //do the references
    
    for (Object propertyName : new LinkedHashSet<Object>(result.keySet())) {
      String value = result.getProperty((String)propertyName);
      String newValue = substituteLocalReferencesOneField(result, value);
      
      //next run, dont do the ones that dont change...
      if (!GrouperClientUtils.equals(value, newValue)) {
        result.put(propertyName, newValue);
      }      
    }
  }

  /** properties from the properties file(s) */
  private Properties properties = new Properties();

  /**
   * get the property value as a string
   * @param key
   * @return the property value
   */
  public String propertyValueStringRequired(String key) {
    return propertyValueString(key, null, true).getTheValue();
  }

  /**
   * get the property value as a string
   * @param key
   * @param defaultValue
   * @return the property value
   */
  public String propertyValueString(String key, String defaultValue) {
    return propertyValueString(key, defaultValue, false).getTheValue();
  }

  /**
   * get the property value as a string or null if not there
   * @param key
   * @return the property value
   */
  public String propertyValueString(String key) {
    return propertyValueString(key, null, false).getTheValue();
  }

  /**
   * result of a property value
   */
  static class PropertyValueResult {

    
    /**
     * 
     * @param theValue1
     * @param hasKey1
     */
    public PropertyValueResult(String theValue1, boolean hasKey1) {
      super();
      this.theValue = theValue1;
      this.hasKey = hasKey1;
    }


    /** value from lookup */
    private String theValue;
    
    /** if there is a key in the properties file */
    private boolean hasKey;

    
    /**
     * value from lookup
     * @return the theValue
     */
    public String getTheValue() {
      return this.theValue;
    }

    
    /**
     * value from lookup
     * @param theValue1 the theValue to set
     */
    public void setTheValue(String theValue1) {
      this.theValue = theValue1;
    }

    
    /**
     * if there is a key in the properties file
     * @return the hasKey
     */
    public boolean isHasKey() {
      return this.hasKey;
    }

    
    /**
     * if there is a key in the properties file
     * @param hasKey1 the hasKey to set
     */
    public void setHasKey(boolean hasKey1) {
      this.hasKey = hasKey1;
    }
    
  }

  /**
   * get the property value as a string
   * @param key
   * @param defaultValue
   * @param required true if required, if doesnt exist, throw exception
   * @return the property value
   */
  protected PropertyValueResult propertyValueString(String key, String defaultValue, boolean required) {
    if (key.endsWith(EL_CONFIG_SUFFIX)) {
      throw new RuntimeException("Why does key end in suffix??? " + EL_CONFIG_SUFFIX + ", " + key);
    }
    return propertyValueStringHelper(key, defaultValue, required);
  }

  /**
   * get the property value as a string
   * @param key
   * @param defaultValue
   * @param required true if required, if doesnt exist, throw exception
   * @return the property value
   */
  protected PropertyValueResult propertyValueStringHelper(String key, String defaultValue, boolean required) {
    
    //lets look for EL
    if (!key.endsWith(EL_CONFIG_SUFFIX)) {
      
      PropertyValueResult elPropertyValueResult = propertyValueStringHelper(key + EL_CONFIG_SUFFIX, null, false);
      
      if (elPropertyValueResult.isHasKey()) {
        
        //process the EL
        String result = ConfigPropertiesCascadeUtils.substituteExpressionLanguage(elPropertyValueResult.getTheValue(), null, true, true, true, false);
        PropertyValueResult propertyValueResult = new PropertyValueResult(result, true);
        return propertyValueResult;
      }
      
    }
    
    //first check threadlocal map
    boolean hasKey = false;
    Map<String, String> overrideMap = propertiesThreadLocalOverrideMap();
    
    hasKey = overrideMap == null ? false : overrideMap.containsKey(key);
    String value = hasKey ? overrideMap.get(key) : null;
    if (!hasKey) {
      
      overrideMap = propertiesOverrideMap();
      
      hasKey = overrideMap == null ? null : overrideMap.containsKey(key);
      value = hasKey ? overrideMap.get(key) : null;
    }
    if (!hasKey) {
      hasKey = this.properties.containsKey(key);
      value = hasKey ? this.properties.getProperty(key) : null;
    }
    if (!required && !hasKey) {
      return new PropertyValueResult(defaultValue, false);
    }
    if (required && !hasKey) {
      String error = "Cant find property: " + key + " in properties file: " + this.getMainConfigClasspath() + ", it is required";
      
      throw new RuntimeException(error);
    }
    value = ConfigPropertiesCascadeUtils.trim(value);
    value = substituteCommonVars(value);

    value = substituteLocalReferencesOneField(this, value);
    
    if (!required && ConfigPropertiesCascadeUtils.isBlank(value)) {
      return new PropertyValueResult(null, true);
    }

    //do the validation if this is required
    if (required && ConfigPropertiesCascadeUtils.isBlank(value)) {
      String error = "Property " + key + " in properties file: " + this.getMainConfigClasspath() + ", has a blank value, it is required";
      
      throw new RuntimeException(error);
    }
    
    return new PropertyValueResult(value, true);
  }

  /**
   * substitute common vars like $space$ and $newline$
   * @param string
   * @return the string
   */
  protected static String substituteCommonVars(String string) {
    if (string == null) {
      return string;
    }
    //short circuit
    if (string.indexOf('$') < 0) {
      return string;
    }
    //might have $space$
    string = ConfigPropertiesCascadeUtils.replace(string, "$space$", " ");
    
    //note, at some point we could be OS specific
    string = ConfigPropertiesCascadeUtils.replace(string, "$newline$", "\n"); 
    return string;
  }

  /**
   * when this config object was created
   */
  private long createdTime = System.currentTimeMillis();

  /**
   * when this config object was created
   * @return the createdTime
   */
  long getCreatedTime() {
    return this.createdTime;
  }

  /**
   * when this config object was created or last checked for changes
   */
  private long lastCheckedTime = System.currentTimeMillis();
  
  /**
   * when this config object was created or last checked for changes
   * @return created time or last checked
   */
  long getLastCheckedTime() {
    return this.lastCheckedTime;
  }
  
  /**
   * when we build the config object, get the time to check config in seconds
   */
  private Integer timeToCheckConfigSeconds = null;
  
  /**
   * when we build the config object, get the time to check config in seconds
   * @return the time to check config foe changes (in seconds)
   */
  protected Integer getTimeToCheckConfigSeconds() {
    return this.timeToCheckConfigSeconds;
  }
  
  /**
   * config file cache
   */
  private static Map<Class<? extends ConfigPropertiesCascadeBase>, ConfigPropertiesCascadeBase> configFileCache = null;
  
  /**
   * 
   */
  private static ThreadLocal<Boolean> inDatabaseConfig = new InheritableThreadLocal<Boolean>();

  /**
   * is in database
   * @return if in database
   */
  public static boolean isInDatabase() {
    Boolean isInDatabase = inDatabaseConfig.get();
    return isInDatabase != null && isInDatabase;
  }

  /**
   * config file type
   */
  protected static enum ConfigFileType {

    /**
     * get a config file from the filesystem
     */
    DATABASE {

      @Override
      public InputStream inputStream(String configFileTypeConfig,
          ConfigPropertiesCascadeBase configPropertiesCascadeBase) {
        
        String mainConfigFileName = configPropertiesCascadeBase.getMainConfigFileName();
        
        return ConfigDatabaseLogic.retrieveConfigInputStream(mainConfigFileName);
      }
    },
    

    /**
     * get a config file from the filesystem
     */
    FILE {

      @Override
      public InputStream inputStream(String configFileTypeConfig,
          ConfigPropertiesCascadeBase configPropertiesCascadeBase) {
        File file = new File(configFileTypeConfig);
        if (!file.exists() || !file.isFile()) {
          throw new RuntimeException("Cant find config file from filesystem path: " + configFileTypeConfig);
        }
        try {
          return new FileInputStream(file);
        } catch (Exception e) {
          throw new RuntimeException("Problem reading config file from filesystem path: " + file.getAbsolutePath(), e);
        }
      }
    },
    
    /**
     * get a config file from the classpath
     */
    CLASSPATH {

      /**
       * 
       */
      @Override
      public InputStream inputStream(String configFileTypeConfig,
          ConfigPropertiesCascadeBase configPropertiesCascadeBase) {
        URL url = ConfigPropertiesCascadeUtils.computeUrl(configFileTypeConfig, true);
        Exception exception = null;
        if (url != null) {
          try {
            return url.openStream();
          } catch (Exception e) {
            exception = e;
          }
        }
        
        //if we didnt get there yet, lets look for a companion jar
        Class<?> classInJar = configPropertiesCascadeBase.getClassInSiblingJar();
        if (classInJar != null) {
          File jarFile = classInJar == null ? null : ConfigPropertiesCascadeUtils.jarFile(classInJar);
          File parentDir = jarFile == null ? null : jarFile.getParentFile();
          String fileName = parentDir == null ? null 
              : (ConfigPropertiesCascadeUtils.stripLastSlashIfExists(ConfigPropertiesCascadeUtils.fileCanonicalPath(parentDir)) + File.separator + configFileTypeConfig);
          File configFile = fileName == null ? null 
              : new File(fileName);
          
          //looks like we have a match
          if (configFile != null && configFile.exists() && configFile.isFile()) {
            try {
              return new FileInputStream(configFile);
            } catch (Exception e) {
              logError("Cant read config file: " + configFile.getAbsolutePath(), e);
            }
          }
        }
        //see if it is next to the jar
        throw new RuntimeException("Cant find config file from classpath: " + configFileTypeConfig, exception);
      }
    };

    /**
     * get the inputstream to read the config 
     * @param configFileTypeConfig
     * @param configPropertiesCascadeBase add the config object in case
     * @return the input stream to get this config
     */
    public abstract InputStream inputStream(String configFileTypeConfig, ConfigPropertiesCascadeBase configPropertiesCascadeBase);

    /**
     * do a case-insensitive matching
     * 
     * @param string
     * @return the enum or null or exception if not found
     */
    public static ConfigFileType valueOfIgnoreCase(String string) {
      return ConfigPropertiesCascadeUtils.enumValueOfIgnoreCase(ConfigFileType.class,string, false );
    }

  }
  
  /**
   * 
   */
  public static class ConfigFile {
    
    /**
     * properties from the config file
     */
    private Properties properties;

    /**
     * properties from the config file
     * @return properties
     */
    public Properties getProperties() {
      return this.properties;
    }

    /**
     * 
     * @param properties1
     */
    public void setProperties(Properties properties1) {
      this.properties = properties1;
    }

    /**
     * keep the original config string for logging purposes, e.g. file:/a/b/c.properties
     */
    private String originalConfig;

    
    /**
     * keep the original config string for logging purposes, e.g. file:/a/b/c.properties
     * @return the originalConfig
     */
    public String getOriginalConfig() {
      return this.originalConfig;
    }

    /**
     * the contents when the config file was read
     */
    private String contents = null;
    
    /**
     * the contents when the config file was read
     * @return the contents
     */
    public String getContents() {
      return this.contents;
    }
    
    /**
     * @param contents1 the contents to set
     */
    public void setContents(String contents1) {
      this.contents = contents1;
    }

    
    
    /**
     * get the contents from the config file
     * @param configPropertiesCascadeBase 
     * @return the contents
     */
    public String retrieveContents(ConfigPropertiesCascadeBase configPropertiesCascadeBase) {
      InputStream inputStream = null;
      try {
        inputStream = this.configFileType.inputStream(this.configFileTypeConfig, configPropertiesCascadeBase);
        if (inputStream != null) {
          return ConfigPropertiesCascadeUtils.toString(inputStream, encoding());
        }
        // dont return contents for DATABASE configs while setting up the database
        return "";
      } catch (Exception e) {
            throw new RuntimeException("Problem reading config: '" + this.originalConfig + "'", e);
      } finally {
            ConfigPropertiesCascadeUtils.closeQuietly(inputStream);
      }
    }

    /**
     * 
     * @param configFileFullConfig
     */
    public ConfigFile(String configFileFullConfig) {
      
      this.originalConfig = configFileFullConfig;
      
      int colonIndex = configFileFullConfig.indexOf(':');
      
      if (colonIndex == -1) {
        throw new RuntimeException("Config file spec needs the type of config and a colon, e.g. file:/some/path/config.properties  '" + configFileFullConfig + "'");
      }
      
      //lets get the type
      String configFileTypeString = ConfigPropertiesCascadeUtils.trim(ConfigPropertiesCascadeUtils.prefixOrSuffix(configFileFullConfig, ":", true));
      
      if (ConfigPropertiesCascadeUtils.isBlank(configFileTypeString)) {
        throw new RuntimeException("Config file spec needs the type of config and a colon, e.g. file:/some/path/config.properties  '" + configFileFullConfig + "'");
      }
      
      try {
        this.configFileType = ConfigFileType.valueOfIgnoreCase(configFileTypeString);
      } catch (Exception e) {
        throw new RuntimeException("Config file spec needs the type of config and a colon, e.g. file:/some/path/config.properties  '" + configFileFullConfig + "', " + e.getMessage(), e);
      }
      
      this.configFileTypeConfig = ConfigPropertiesCascadeUtils.trim(ConfigPropertiesCascadeUtils.prefixOrSuffix(configFileFullConfig, ":", false));
      
    }
    
    /**
     * the type of config file (file path, classpath, etc)
     */
    private ConfigFileType configFileType;
    
    /**
     * the config part which says which file or classpath etc
     */
    private String configFileTypeConfig;

    
    /**
     * the type of config file (file path, classpath, etc)
     * @return the configFileType
     */
    public ConfigFileType getConfigFileType() {
      return this.configFileType;
    }

    
    /**
     * the config part which says which file or classpath etc
     * @return the configFileTypeConfig
     */
    public String getConfigFileTypeConfig() {
      return this.configFileTypeConfig;
    }
    
    
    
  }
  
  /**
   * 
   */
  private static String encoding = null;
  
  /**
   * 
   * @return the encoding
   */
  static String encoding() {
    
    //cache this statically and never re-read this setting.  need to bounce if reset
    if (encoding == null) {

      synchronized (ConfigPropertiesCascadeBase.class) {
      
        if (encoding == null) {
          String configEncodingKey = "grouperClient.config.encoding";
          try {
            
            //first look in grouper.client.properties which is the default override
            Properties properties = propertiesFromResourceName("grouper.client.properties", false, ConfigPropertiesCascadeBase.class);
            if (properties != null && properties.contains(configEncodingKey)) {
              encoding = properties.getProperty(configEncodingKey);
            } else {
              
              //if not there look in the base file which should have this setting
              properties = propertiesFromResourceName("grouper.client.base.properties", false, ConfigPropertiesCascadeBase.class);
              if (properties != null && properties.contains(configEncodingKey)) {
                encoding = properties.getProperty(configEncodingKey);
              }
            }
          } catch (RuntimeException e) {

            //this is a failsafe method since if we fail when reading configs it is bad
            LOG.error("Trouble finding " + configEncodingKey, e);
          }
          if (GrouperClientUtils.isBlank(encoding)) {

            //this is the default if nothing is set
            encoding = "UTF-8";
          }
        }
      }
    }
    return encoding;
  }
  
  /**
   * config files from least specific to more specific
   */
  private List<ConfigFile> configFiles = null;

  /**
   * get the config object from config files
   * @return the config object
   */
  protected ConfigPropertiesCascadeBase retrieveFromConfigFiles() {
    return this.retrieveFromConfigFiles(true);
  }

  /**
   * get the config object from config files.  You should call the method that gets these from cache, 
   * probably shouldnt call this method except for config in UI reasons
   * @param includeBaseConfig true if we include base config
   * @return the config object
   */
  public ConfigPropertiesCascadeBase retrieveFromConfigFiles(boolean includeBaseConfig) {
    
    //lets get the config hierarchy...
    //properties from override first
    Properties mainConfigFile = propertiesFromResourceName(this.getMainConfigClasspath(), false, this.getClassInSiblingJar());

    String secondsToCheckConfigString = null;
    
    String overrideFullConfig = null;
    
    if (mainConfigFile != null) {
      overrideFullConfig = mainConfigFile.getProperty(this.getHierarchyConfigKey());
      secondsToCheckConfigString = mainConfigFile.getProperty(this.getSecondsToCheckConfigKey());
    }
    
    //if couldnt find it from the override, get from example
    if (ConfigPropertiesCascadeUtils.isBlank(overrideFullConfig) || ConfigPropertiesCascadeUtils.isBlank(secondsToCheckConfigString)) {
      
      Properties mainExampleConfigFile = propertiesFromResourceName(this.getMainExampleConfigClasspath(), false, this.getClassInSiblingJar());
      
      if (mainExampleConfigFile != null) {
        
        if (ConfigPropertiesCascadeUtils.isBlank(overrideFullConfig)) {
          overrideFullConfig = mainExampleConfigFile.getProperty(this.getHierarchyConfigKey());
        }
        if (ConfigPropertiesCascadeUtils.isBlank(secondsToCheckConfigString)) {
          secondsToCheckConfigString = mainExampleConfigFile.getProperty(this.getSecondsToCheckConfigKey());
        }

      }
      
    }

    //if hasnt found yet, there is a problem
    if (ConfigPropertiesCascadeUtils.isBlank(overrideFullConfig)) {
      throw new RuntimeException("Cant find the hierarchy config key: " + this.getHierarchyConfigKey() 
          + " in config files: " + this.getMainConfigClasspath()
          + " or " + this.getMainExampleConfigClasspath());
    }
    
    //if hasnt found yet, there is a problem
    if (ConfigPropertiesCascadeUtils.isBlank(secondsToCheckConfigString)) {
      throw new RuntimeException("Cant find the seconds to check config key: " + this.getSecondsToCheckConfigKey() 
          + " in config files: " + this.getMainConfigClasspath()
          + " or " + this.getMainExampleConfigClasspath());
    }

    //make a new return object based on this class
    ConfigPropertiesCascadeBase result = ConfigPropertiesCascadeUtils.newInstance(this.getClass(), true);

    try {
      result.timeToCheckConfigSeconds = ConfigPropertiesCascadeUtils.intValue(secondsToCheckConfigString);
    } catch (Exception e) {
      throw new RuntimeException("Invalid integer seconds to check config config value: " + secondsToCheckConfigString
          + ", key: " + this.getSecondsToCheckConfigKey() 
          + " in config files: " + this.getMainConfigClasspath()
          + " or " + this.getMainExampleConfigClasspath());
      
    }
    
    //ok, we have the config file list...
    //lets get this into a comma separated list
    List<String> overrideConfigStringList = ConfigPropertiesCascadeUtils.splitTrimToList(overrideFullConfig, ",");

    result.configFiles = new ArrayList<ConfigFile>();

    boolean isFirst = true;
    
    for (String overrideConfigString : overrideConfigStringList) {
      
      if (!includeBaseConfig && isFirst && overrideConfigString.toLowerCase().contains("base")) {
        
        isFirst = false;
        continue;
        
      }
      isFirst = false;
      ConfigFile configFile = new ConfigFile(overrideConfigString);
      result.configFiles.add(configFile);
      
      boolean replaceWithBlank = false;
      //lets append the properties
      if (configFile.getConfigFileType() == ConfigFileType.CLASSPATH) {
        if (GrouperClientUtils.equals(overrideConfigString, "classpath:grouper.properties")
            || GrouperClientUtils.equals(overrideConfigString, "classpath:grouper-loader.properties")
            || GrouperClientUtils.equals(overrideConfigString, "classpath:grouper-ui.properties")
            || GrouperClientUtils.equals(overrideConfigString, "classpath:grouper-ws.properties")
            || GrouperClientUtils.equals(overrideConfigString, "classpath:grouper.client.properties")
            || GrouperClientUtils.equals(overrideConfigString, "classpath:grouper.cache.properties")
            || GrouperClientUtils.equals(overrideConfigString, "classpath:subject.properties")
            || GrouperClientUtils.equals(overrideConfigString, "classpath:grouperText/grouper.text.en.us.properties")
            || GrouperClientUtils.equals(overrideConfigString, "classpath:grouperText/grouper.text.fr.fr.properties")
            
            ) {
          String resource = GrouperClientUtils.stripPrefix(overrideConfigString, "classpath:");
          URL url = GrouperClientUtils.computeUrl(resource, true);
          if (url == null) {
            replaceWithBlank = true;
          }
        }
      }
      String configFileContents = replaceWithBlank ? "" : configFile.retrieveContents(this);
      configFile.setContents(configFileContents);
      
      try {
        // keep a copy in here for config screen
        configFile.setProperties(new Properties());
        configFile.getProperties().load(new StringReader(configFileContents));

        // cycle through so we get the right .elConfigs
        for (Object key : configFile.getProperties().keySet()) {
          String keyString = (String)key;
          assignProperty(result.properties, keyString, configFile.getProperties().getProperty(keyString));
          //result.properties.load(new StringReader(configFileContents));
        }

      } catch (Exception e) {
        throw new RuntimeException("Problem loading properties: " + overrideConfigString, e);
      }
    }
    
    return result;
    
  }
    
  /**
   * get the underlying properties for the config ui
   * @return the config files
   */
  public List<ConfigFile> internalRetrieveConfigFiles() {
    return this.configFiles;
  }
  
  /**
   * make sure LOG is there, after things are initialized
   * @param logMessage
   * @param t 
   */
  protected static void logInfo(String logMessage, Throwable t) {
    if (LOG != null && LOG.isInfoEnabled()) {
      LOG.info(logMessage, t); 
    }
  }
  
  /**
   * make sure LOG is there, after things are initialized
   * @param logMessage
   * @param t 
   */
  protected static void logError(String logMessage, Throwable t) {
    if (LOG != null) {
      LOG.error(logMessage, t); 
    } else {
      System.err.println("ERROR: " + logMessage);
      t.printStackTrace();
    }
  }
  
  /**
   * see if there is one in cache, if so, use it, if not, get from config files
   * @return the config from file or cache
   */
  protected ConfigPropertiesCascadeBase retrieveFromConfigFileOrCache() {

    boolean isDebugEnabled = false;
    if (LOG != null) {
      if (LOG instanceof GrouperClientLog) {
        isDebugEnabled = ((GrouperClientLog)LOG).isEnclosedLogDebugEnabled();
      } else {
        isDebugEnabled = LOG.isDebugEnabled();
      }
    }

    Map<String, Object> debugMap = (LOG != null && isDebugEnabled) ? new LinkedHashMap<String, Object>() : null;

    try {

      if (configFileCache == null) {
        if (LOG != null && isDebugEnabled) {
          debugMap.put("configFileCache", null);
        }

        configFileCache = 
            new HashMap<Class<? extends ConfigPropertiesCascadeBase>, ConfigPropertiesCascadeBase>();
      }

      ConfigPropertiesCascadeBase configObject = configFileCache.get(this.getClass());

      if (configObject == null) {

        if (LOG != null && isDebugEnabled) {
          debugMap.put("configObject", null);
        }
        if (LOG != null && isDebugEnabled) {
          debugMap.put("mainConfigClasspath", this.getMainConfigClasspath());
        }

        configObject = retrieveFromConfigFiles();
        configFileCache.put(this.getClass(), configObject);

      } else {

        //see if that much time has passed
        if (configObject.needToCheckIfFilesNeedReloading()) {

          if (LOG != null && isDebugEnabled) {
            debugMap.put("needToCheckIfFilesNeedReloading", true);
          }
          
          // dont synchronize, just sleep a bit to reduce deadlock
          GrouperClientUtils.sleep(Math.round(Math.random() * 100d));

          configObject = configFileCache.get(this.getClass());

          //check again in case another thread did it
          if (configObject.needToCheckIfFilesNeedReloading()) {

            if (LOG != null && isDebugEnabled) {
              debugMap.put("needToCheckIfFilesNeedReloading2", true);
            }
            if (configObject.filesNeedReloadingBasedOnContents()) {
              if (LOG != null && isDebugEnabled) {
                debugMap.put("filesNeedReloadingBasedOnContents", true);
              }
              configObject = retrieveFromConfigFiles();
              configFileCache.put(this.getClass(), configObject);
            }
          }
        }
      }
      //      if (LOG != null && isDebugEnabled) {
      //        Properties theProperties = configObject.properties();
      //        debugMap.put("configObjectPropertyCount", configObject == null ? null 
      //            : (theProperties == null ? "propertiesNull" : theProperties.size()));
      //      }

      return configObject;
    } finally {
      if (LOG != null && isDebugEnabled && debugMap.size() > 0) {
        LOG.debug(ConfigPropertiesCascadeUtils.mapToString(debugMap));
      }
    }
  }
  
  /**
   * 
   * @return true if need to reload this config, false if not
   */
  protected boolean needToCheckIfFilesNeedReloading() {
    
    //get the time that this was created
    long lastCheckedTimeLocal = this.getLastCheckedTime();
    
    //get the timeToCheckSeconds if different
    int timeToCheckSeconds = this.getTimeToCheckConfigSeconds();
    
    //never reload.  0 means reload all the time?
    if (timeToCheckSeconds < 0) {
      return false;
    }
    
    //see if that much time has passed
    if (System.currentTimeMillis() - lastCheckedTimeLocal > timeToCheckSeconds * 1000) {
      return true;
    }
    return false;

  }

  /**
   * 
   * @return true if need to reload this config, false if not
   */
  protected boolean filesNeedReloadingBasedOnContents() {
    try {
      //lets look at all the files and see if they have changed...
      for (ConfigFile configFile : this.configFiles) {
        if (!ConfigPropertiesCascadeUtils.equals(configFile.getContents(), configFile.retrieveContents(this))) {
          logInfo("Contents changed for config file, reloading: " + configFile.getOriginalConfig(), null);
          return true;
        }
      }
    } catch (Exception e) {
      //lets log and return the old one
      logError("Error checking for changes in configs (will use previous version): " + this.getMainConfigClasspath(), e);
    } finally {
      //reset the time so we dont have to check again for a while
      this.lastCheckedTime = System.currentTimeMillis();
    }
    return false;
  }

  /**
   * 
   * @return e.g. grouper.properties
   */
  public String getMainConfigFileName() {
    String configFileClasspath = this.getMainConfigClasspath();
    if (configFileClasspath.contains("/")) {
      configFileClasspath = GrouperClientUtils.substringAfterLast(configFileClasspath, "/");
    }
    return configFileClasspath;
  }
  
  /**
   * get the main config classpath, e.g. grouper.properties
   * @return the classpath of the main config file
   */
  protected abstract String getMainConfigClasspath();
  
  /**
   * config key of the hierarchy value
   * @return the classpath of the main config file
   */
  protected abstract String getHierarchyConfigKey();
  
  /**
   * get the example config classpath, e.g. grouper.example.properties
   * @return the classpath of the example config file
   */
  protected abstract String getMainExampleConfigClasspath();

  /**
   * get a boolean and validate from grouper.client.properties
   * @param key
   * @param defaultValue
   * @return the string
   */
  public boolean propertyValueBoolean(String key, boolean defaultValue) {
    return propertyValueBoolean(key, defaultValue, false);
  }

  /**
   * if the key is there, whether or not the value is blank
   * @param key
   * @return true or false
   */
  public boolean containsKey(String key) {
    
    return propertyValueString(key, null, false).isHasKey();
    
  }
  
  /**
   * get a boolean and validate from grouper.client.properties or null if not there
   * @param key
   * @return the boolean or null
   */
  public Boolean propertyValueBoolean(String key) {
    return propertyValueBoolean(key, null, false);
  }


  /**
   * get a boolean and validate from the config file
   * @param key
   * @param defaultValue
   * @param required
   * @return the string
   */
  protected Boolean propertyValueBoolean(String key, Boolean defaultValue, boolean required) {
    String value = propertyValueString(key, null, false).getTheValue();
    if (ConfigPropertiesCascadeUtils.isBlank(value) && !required) {
      return defaultValue;
    }
    if (ConfigPropertiesCascadeUtils.isBlank(value) && required) {
      throw new RuntimeException("Cant find boolean property " + key + " in properties file: " + this.getMainConfigClasspath() + ", it is required, expecting true or false");
    }
    if ("true".equalsIgnoreCase(value)) {
      return true;
    }
    if ("false".equalsIgnoreCase(value)) {
      return false;
    }
    if ("t".equalsIgnoreCase(value)) {
      return true;
    }
    if ("f".equalsIgnoreCase(value)) {
      return false;
    }
    if ("yes".equalsIgnoreCase(value)) {
      return true;
    }
    if ("no".equalsIgnoreCase(value)) {
      return false;
    }
    if ("y".equalsIgnoreCase(value)) {
      return true;
    }
    if ("n".equalsIgnoreCase(value)) {
      return false;
    }
    throw new RuntimeException("Invalid boolean value: '" + value + "' for property: " + key 
        + " in properties file: " + this.getMainConfigClasspath() + ", expecting true or false");
    
  }

  /**
   * get an int and validate from the config file
   * @param key
   * @param defaultValue
   * @param required
   * @return the string
   */
  protected Integer propertyValueInt(String key, Integer defaultValue, boolean required) {
    String value = propertyValueString(key, null, false).getTheValue();
    if (ConfigPropertiesCascadeUtils.isBlank(value) && !required) {
      return defaultValue;
    }
    if (ConfigPropertiesCascadeUtils.isBlank(value) && required) {
      throw new RuntimeException("Cant find integer property " + key + " in config file: " + this.getMainConfigClasspath() + ", it is required");
    }
    try {
      return ConfigPropertiesCascadeUtils.intValue(value);
    } catch (Exception e) {
      
    }
    throw new RuntimeException("Invalid integer value: '" + value + "' for property: " 
        + key + " in config file: " + this.getMainConfigClasspath() + " in properties file");
    
  }

  /**
   * get a boolean and validate from grouper.client.properties
   * @param key
   * @return the string
   */
  public boolean propertyValueBooleanRequired(String key) {
    
    return propertyValueBoolean(key, false, true);
    
  }
  
  /**
   * get a boolean and validate from grouper.client.properties
   * @param key
   * @return the string
   */
  public int propertyValueIntRequired(String key) {
    
    return propertyValueInt(key, -1, true);
    
  }
  
  /**
   * get a boolean and validate from grouper.client.properties
   * @param key
   * @param defaultValue
   * @return the string
   */
  public int propertyValueInt(String key, int defaultValue ) {

    return propertyValueInt(key, defaultValue, false);
  
  }
  
  /**
   * get a boolean and validate from grouper.client.properties
   * @param key
   * @return the int or null if there
   */
  public Integer propertyValueInt(String key ) {
  
    return propertyValueInt(key, null, false);
  
  }

  /**
   * read properties from a resource, dont modify the properties returned since they are cached
   * @param resourceName
   * @param exceptionIfNotExist 
   * @return the properties or null if not exist
   */
  protected static Properties propertiesFromResourceName(String resourceName, 
      boolean exceptionIfNotExist) {
    return propertiesFromResourceName(resourceName, exceptionIfNotExist, null);
  }

  /**
   * read properties from a resource, dont modify the properties returned since they are cached
   * @param resourceName
   * @param exceptionIfNotExist 
   * @param classInSiblingJar if also looking for config next to a jar
   * @return the properties or null if not exist
   */
  protected static Properties propertiesFromResourceName(String resourceName, 
      boolean exceptionIfNotExist, Class<?> classInSiblingJar) {

    Properties properties = new Properties();

    URL url = null;
    
    try {
      
      url = ConfigPropertiesCascadeUtils.computeUrl(resourceName, true);
      
    } catch (Exception e) {
      //I guess this ok
      if (exceptionIfNotExist) {
        logInfo("Problem loading config file: " + resourceName, e);
      }
      
    }

    if (url == null) {
      File jarFile = classInSiblingJar == null ? null : ConfigPropertiesCascadeUtils.jarFile(classInSiblingJar);
      File parentDir = jarFile == null ? null : jarFile.getParentFile();
      String fileName = parentDir == null ? null 
          : (ConfigPropertiesCascadeUtils.stripLastSlashIfExists(ConfigPropertiesCascadeUtils.fileCanonicalPath(parentDir)) + File.separator + resourceName);
      File configFile = fileName == null ? null 
          : new File(fileName);

      InputStream inputStream = null;
      
      try {
        //looks like we have a match
        if (configFile != null && configFile.exists() && configFile.isFile()) {
          inputStream = new FileInputStream(configFile);
          properties.load(inputStream);
          if ((LOG != null && LOG.isDebugEnabled()) || GrouperClientLog.debugToConsoleByFlag()) {
            String theLog = "Reading resource: " + resourceName + ", from: " + ConfigPropertiesCascadeUtils.fileCanonicalPath(configFile);
            if (LOG != null && LOG.isDebugEnabled()) {
              LOG.debug(theLog);
            }
            if (GrouperClientLog.debugToConsoleByFlag()) {
              System.err.println(theLog);
            }
          }
          return properties;
        }
        
      } catch (Exception e2) {
        if (LOG != null && LOG.isDebugEnabled()) {
          LOG.debug("Error reading from file for resource: " + resourceName + ", file: " + fileName, e2);
        }
      } finally {
        ConfigPropertiesCascadeUtils.closeQuietly(inputStream);
      }
     
      
    }
    
    if (url == null && exceptionIfNotExist) {
      throw new RuntimeException("Problem loading config file: " + resourceName);
    }
    
    if (url == null) {
      return null;
    }
    
    InputStream inputStream = null;
    try {
      inputStream = url.openStream();
      properties.load(inputStream);
      
    } catch (Exception e) {
      
      //why exception at this point?  not good
      throw new RuntimeException("Problem loading config file: " + resourceName, e);
      
    } finally {
      ConfigPropertiesCascadeUtils.closeQuietly(inputStream);
    }
    return properties;
  }

  /**
   * make sure a value exists in properties
   * @param key
   * @return true if ok, false if not
   */
  public boolean assertPropertyValueRequired(String key) {
    String value = propertyValueString(key);
    if (!ConfigPropertiesCascadeUtils.isBlank(value)) {
      return true;
    }
    String error = "Cant find property " + key + " in resource: " + this.getMainConfigClasspath() + ", it is required";
    System.err.println("Grouper error: " + error);
    LOG.error(error);
    return false;
  }

  /**
   * make sure a value is boolean in properties
   * @param key
   * @param required
   * @return true if ok, false if not
   */
  public boolean assertPropertyValueBoolean(String key, boolean required) {
    
    if (required && !assertPropertyValueRequired(key)) {
      return false;
    }
  
    String value = propertyValueString(key);
    //maybe ok not there
    if (!required && ConfigPropertiesCascadeUtils.isBlank(value)) {
      return true;
    }
    try {
      ConfigPropertiesCascadeUtils.booleanValue(value);
      return true;
    } catch (Exception e) {
      
    }
    String error = "Expecting true or false property " + key + " in resource: " + this.getMainConfigClasspath() + ", but is '" + value + "'";
    System.err.println("Grouper error: " + error);
    LOG.error(error);
    return false;
  }

  /**
   * make sure a property is a class of a certain type
   * @param key
   * @param classType
   * @param required 
   * @return true if ok
   */
  public boolean assertPropertyValueClass(
      String key, Class<?> classType, boolean required) {
  
    if (required && !assertPropertyValueRequired(key)) {
      return false;
    }
    String value = propertyValueString(key);
  
    //maybe ok not there
    if (!required && ConfigPropertiesCascadeUtils.isBlank(value)) {
      return true;
    }
    
    String extraError = "";
    for (String classValue : value.split(",")) {
      try {
        
        Class<?> theClass = ConfigPropertiesCascadeUtils.forName(classValue.trim());
        if (classType.isAssignableFrom(theClass)) {
        } else {
          extraError += " does not derive from class: " + classType.getSimpleName();
        }
        
      } catch (Exception e) {
        extraError = ", " + ConfigPropertiesCascadeUtils.getFullStackTrace(e);
      }
    }  

    if (extraError.isEmpty()) {
      return true;
    }

    String error = "Cant process property " + key + " in resource: " + this.getMainConfigClasspath() + ", the current" +
        " value is '" + value + "', which should be of type: " 
        + classType.getName() + extraError;
    System.err.println("Grouper error: " + error);
    LOG.error(error);
    return false;
  }
  
  /**
   * find all keys/values with a certain pattern in a properties file.
   * return the keys.  if none, will return the empty set, not null set
   * @param pattern
   * @return the keys.  if none, will return the empty set, not null set
   */
  public Map<String, String> propertiesMap(Pattern pattern) {
    Map<String, String> result = new TreeMap<String, String>();
    for (String key: propertyNames()) {
      if (pattern.matcher(key).matches()) {
        result.put(key, propertyValueString(key));
      }
    }
    
    return result;
  }
  
  /**
   * pattern to find where the variables are in the textm, e.g. $$something$$
   */
  private static Pattern substitutePattern = Pattern.compile("\\$\\$([^\\s\\$]+?)\\$\\$");

  /**
   * 
   * @param thePropertiesOrConfigPropertiesCascadeBase to get data from
   * @param value 
   * @return the subsituted string
   */
  protected String substituteLocalReferencesOneField(Object thePropertiesOrConfigPropertiesCascadeBase, String value) {

    //lets resolve variables, do a loop in case the substituted value also has variables
    for (int i=0;i<20;i++) {
      
      String newValue = substituteLocalReferencesOneSubstitution(thePropertiesOrConfigPropertiesCascadeBase, value);
        
      //next run, dont do the ones that dont change...
      if (GrouperClientUtils.equals(value, newValue)) {
        break;
      }
      value = newValue;
      
    }
    return value;
  }
  
  /**
   * 
   * @param thePropertiesOrConfigPropertiesCascadeBase to get data from.  either Properties or ConfigPropertiesCascadeBAse
   * @param value 
   * @return the subsituted string
   */
  protected String substituteLocalReferencesOneSubstitution(Object thePropertiesOrConfigPropertiesCascadeBase, String value) {

    if (GrouperClientUtils.isBlank(value) || !value.contains("$$")) {
      return value;
    }
    
    Properties theProperties = null;
    ConfigPropertiesCascadeBase configPropertiesCascadeBase = null;
    
    if (thePropertiesOrConfigPropertiesCascadeBase instanceof Properties) {
      theProperties = (Properties)thePropertiesOrConfigPropertiesCascadeBase;
    } else {
      configPropertiesCascadeBase = (ConfigPropertiesCascadeBase)thePropertiesOrConfigPropertiesCascadeBase;
    }
    
    Matcher matcher = substitutePattern.matcher(value);
    
    StringBuilder result = new StringBuilder();
    
    int index = 0;
    
    //loop through and find each script
    while(matcher.find()) {
      result.append(value.substring(index, matcher.start()));
      
      //here is the script inside the dollars
      String variable = matcher.group(1);
      
      index = matcher.end();

      boolean hasProperty = false;
      String propertyValue = null;
      
      if (theProperties != null) { 
        hasProperty = theProperties.containsKey(variable);
        if (hasProperty) {
          propertyValue = theProperties.getProperty(variable);
        }
      } else {
        PropertyValueResult propertyValueResult = configPropertiesCascadeBase.propertyValueString(variable, null, false);
        hasProperty = propertyValueResult.isHasKey();
        if (hasProperty) {
          propertyValue = propertyValueResult.getTheValue();
        }
      }

      if (!hasProperty) {
        if (!whitelistConfigVariables.contains(variable)) {
          LOG.debug("Cant find text for variable: '" + variable + "'");
        }
        //if we cant find it just keep the variable name
        propertyValue = "$$" + variable + "$$";
      } else {
        propertyValue = GrouperClientUtils.defaultString(propertyValue);
      }
      
      result.append(propertyValue);
    }
    
    result.append(value.substring(index, value.length()));
    return result.toString();
    
  }

  /**
   * variables that are allowed to be in the config file
   */
  private static Set<String> whitelistConfigVariables = GrouperClientUtils.toSet("newline", "subjectName", "reportConfigName", "reportLink");

  public boolean hasExpressionLanguage(String propertyName) {
    
    List<ConfigFile> configFiles = new ArrayList<ConfigFile>(this.internalRetrieveConfigFiles());
    Collections.reverse(configFiles);
     
    String elKey = propertyName + ".elConfig";
    
    //first check threadlocal map
    Map<String, String> overrideMap = propertiesThreadLocalOverrideMap();
    
    if (overrideMap.containsKey(elKey)) {
      return true;
    }
      
    overrideMap = propertiesOverrideMap();
      
    if (overrideMap.containsKey(elKey)) {
      return true;
    }
    
    for (ConfigFile configFile : configFiles) {
      Properties properties = configFile.getProperties();

      if (properties.containsKey(elKey)) {
        return true;
      }
      
    }
    return false;
  }

  public String rawExpressionLanguage(String propertyName) {
    
    List<ConfigFile> configFiles = new ArrayList<ConfigFile>(this.internalRetrieveConfigFiles());
    Collections.reverse(configFiles);
     
    String elKey = propertyName + ".elConfig";
    
    //first check threadlocal map
    Map<String, String> overrideMap = propertiesThreadLocalOverrideMap();
    
    if (overrideMap.containsKey(elKey)) {
      return overrideMap.get(elKey);
    }
      
    overrideMap = propertiesOverrideMap();
      
    if (overrideMap.containsKey(elKey)) {
      return overrideMap.get(elKey);
    }
    
    for (ConfigFile configFile : configFiles) {
      Properties properties = configFile.getProperties();

      if (properties.containsKey(elKey)) {
        return properties.getProperty(elKey);
      }
      
    }
    return null;
  }

}
