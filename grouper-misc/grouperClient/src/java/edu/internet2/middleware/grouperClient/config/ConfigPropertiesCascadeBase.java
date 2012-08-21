/**
 * 
 */
package edu.internet2.middleware.grouperClient.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.logging.Log;


/**
 * Base class for a cascaded config.  Extend this class to have a config
 * based on a certain file. 
 * 
 * @author mchyzer
 *
 */
public abstract class ConfigPropertiesCascadeBase {

  /**
   * log object
   */
  private static final Log LOG = GrouperClientUtils.retrieveLog(ConfigPropertiesCascadeBase.class);

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
    
    ConfigPropertiesCascadeBase configPropertiesCascadeBase = (ConfigPropertiesCascadeBase)configSingletonFromClass.get(configClass);
    if (configPropertiesCascadeBase == null) {
      configPropertiesCascadeBase = GrouperClientUtils.newInstance(configClass);
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
   * if there are things that are calculated, clear them out (e.g. if an override is set)
   */
  public abstract void clearCachedCalculatedValues();
  
  /** override map for properties in thread local to be used in a web server or the like */
  private static ThreadLocal<Map<Class<? extends ConfigPropertiesCascadeBase>, Map<String, String>>> propertiesThreadLocalOverrideMap 
    = null;
  
  /**
   * override map for properties in thread local to be used in a web server or the like, based on property class
   * this is static since the properties class can get reloaded, but these shouldnt
   * @param configClass 
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
    Map<String, String> propertiesOverrideMap = overrideMap.get(this.getClass());
    if (propertiesOverrideMap == null) {
      propertiesOverrideMap = new HashMap<String, String>();
      overrideMap.put(this.getClass(), propertiesOverrideMap);
    }
    return propertiesOverrideMap;
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
    
    Set<String> result = new LinkedHashSet<String>();
    result.addAll((Set<String>)(Object)this.properties().keySet());
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
    Properties result = new Properties();
    
    result.putAll(this.properties);

    Map<String, String> localPropertiesOverrideMap = propertiesOverrideMap();
    
    for (String key: localPropertiesOverrideMap.keySet()) {
      result.put(key, GrouperClientUtils.defaultString(localPropertiesOverrideMap.get(key)));
    }
    
    localPropertiesOverrideMap = propertiesThreadLocalOverrideMap();
    
    for (String key: localPropertiesOverrideMap.keySet()) {
      result.put(key, GrouperClientUtils.defaultString(localPropertiesOverrideMap.get(key)));
    }
    
    return result;
    
  }

  /** properties from the properties file(s) */
  private Properties properties = new Properties();

  /**
   * get the property value as a string
   * @param key
   * @param defaultValue
   * @param required true if required, if doesnt exist, throw exception
   * @return the property value
   */
  public String propertyValueStringRequired(String key) {
    return propertyValueString(key, null, true).getTheValue();
  }

  /**
   * get the property value as a string
   * @param key
   * @param defaultValue
   * @param required true if required, if doesnt exist, throw exception
   * @return the property value
   */
  public String propertyValueString(String key, String defaultValue) {
    return propertyValueString(key, defaultValue, false).getTheValue();
  }

  /**
   * get the property value as a string or null if not there
   * @param key
   * @param required true if required, if doesnt exist, throw exception
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
    value = GrouperClientUtils.trim(value);
    value = substituteCommonVars(value);

    if (!required && GrouperClientUtils.isBlank(value)) {
      return new PropertyValueResult(null, true);
    }

    //do the validation if this is required
    if (required && GrouperClientUtils.isBlank(value)) {
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
    string = GrouperClientUtils.replace(string, "$space$", " ");
    
    //note, at some point we could be OS specific
    string = GrouperClientUtils.replace(string, "$newline$", "\n"); 
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
   * config file type
   */
  protected static enum ConfigFileType {
    
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
        URL url = GrouperClientUtils.computeUrl(configFileTypeConfig, true);
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
          File jarFile = classInJar == null ? null : GrouperClientUtils.jarFile(classInJar);
          File parentDir = jarFile == null ? null : jarFile.getParentFile();
          String fileName = parentDir == null ? null 
              : (GrouperClientUtils.stripLastSlashIfExists(GrouperClientUtils.fileCanonicalPath(parentDir)) + File.separator + configFileTypeConfig);
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
      return GrouperClientUtils.enumValueOfIgnoreCase(ConfigFileType.class,string, false );
    }

  }
  
  /**
   * 
   */
  protected static class ConfigFile {
    
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
      try {
        return GrouperClientUtils.toString(this.configFileType.inputStream(this.configFileTypeConfig, configPropertiesCascadeBase), "UTF-8");
      } catch (Exception e) {
        throw new RuntimeException("Problem reading config: '" + this.originalConfig + "'", e);
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
      String configFileTypeString = GrouperClientUtils.trim(GrouperClientUtils.prefixOrSuffix(configFileFullConfig, ":", true));
      
      if (GrouperClientUtils.isBlank(configFileTypeString)) {
        throw new RuntimeException("Config file spec needs the type of config and a colon, e.g. file:/some/path/config.properties  '" + configFileFullConfig + "'");
      }
      
      try {
        this.configFileType = ConfigFileType.valueOfIgnoreCase(configFileTypeString);
      } catch (Exception e) {
        throw new RuntimeException("Config file spec needs the type of config and a colon, e.g. file:/some/path/config.properties  '" + configFileFullConfig + "', " + e.getMessage(), e);
      }
      
      this.configFileTypeConfig = GrouperClientUtils.trim(GrouperClientUtils.prefixOrSuffix(configFileFullConfig, ":", false));
      
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
   * config files from least specific to more specific
   */
  private List<ConfigFile> configFiles = null;
  
  /**
   * get the config object from config files
   * @return the config object
   */
  protected ConfigPropertiesCascadeBase retrieveFromConfigFiles() {
    
    //lets get the config hierarchy...
    //properties from override first
    Properties mainConfigFile = propertiesFromResourceName(this.getMainConfigClasspath(), false);

    String secondsToCheckConfigString = null;
    
    String overrideFullConfig = null;
    
    if (mainConfigFile != null) {
      overrideFullConfig = mainConfigFile.getProperty(this.getHierarchyConfigKey());
      secondsToCheckConfigString = mainConfigFile.getProperty(this.getSecondsToCheckConfigKey());
    }
    
    //if couldnt find it from the override, get from example
    if (GrouperClientUtils.isBlank(overrideFullConfig) || GrouperClientUtils.isBlank(secondsToCheckConfigString)) {
      
      Properties mainExampleConfigFile = propertiesFromResourceName(this.getMainExampleConfigClasspath(), false);
      
      if (mainExampleConfigFile != null) {
        
        if (GrouperClientUtils.isBlank(overrideFullConfig)) {
          overrideFullConfig = mainExampleConfigFile.getProperty(this.getHierarchyConfigKey());
        }
        if (GrouperClientUtils.isBlank(secondsToCheckConfigString)) {
          secondsToCheckConfigString = mainExampleConfigFile.getProperty(this.getSecondsToCheckConfigKey());
        }

      }
      
    }

    //if hasnt found yet, there is a problem
    if (GrouperClientUtils.isBlank(overrideFullConfig)) {
      throw new RuntimeException("Cant find the hierarchy config key: " + this.getHierarchyConfigKey() 
          + " in config files: " + this.getMainConfigClasspath()
          + " or " + this.getMainExampleConfigClasspath());
    }
    
    //if hasnt found yet, there is a problem
    if (GrouperClientUtils.isBlank(secondsToCheckConfigString)) {
      throw new RuntimeException("Cant find the seconds to check config key: " + this.getSecondsToCheckConfigKey() 
          + " in config files: " + this.getMainConfigClasspath()
          + " or " + this.getMainExampleConfigClasspath());
    }

    //make a new return object based on this class
    ConfigPropertiesCascadeBase result = GrouperClientUtils.newInstance(this.getClass());

    try {
      result.timeToCheckConfigSeconds = GrouperClientUtils.intValue(secondsToCheckConfigString);
    } catch (Exception e) {
      throw new RuntimeException("Invalid integer seconds to check config config value: " + secondsToCheckConfigString
          + ", key: " + this.getSecondsToCheckConfigKey() 
          + " in config files: " + this.getMainConfigClasspath()
          + " or " + this.getMainExampleConfigClasspath());
      
    }
    
    //ok, we have the config file list...
    //lets get this into a comma separated list
    List<String> overrideConfigStringList = GrouperClientUtils.splitTrimToList(overrideFullConfig, ",");

    result.configFiles = new ArrayList<ConfigFile>();

    for (String overrideConfigString : overrideConfigStringList) {
      
      ConfigFile configFile = new ConfigFile(overrideConfigString);
      result.configFiles.add(configFile);
      
      //lets append the properties
      InputStream inputStream = configFile.getConfigFileType().inputStream(configFile.getConfigFileTypeConfig(), this);
      
      try {
        result.properties.load(inputStream);
      } catch (Exception e) {
        throw new RuntimeException("Problem loading properties: " + overrideConfigString, e);
      }
    }
    
    return result;
    
  }
    
  /**
   * make sure LOG is there, after things are initialized
   * @param logMessage
   */
  protected static void logDebug(String logMessage) {
    if (LOG != null && LOG.isDebugEnabled()) {
      LOG.debug(logMessage); 
    }
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
    
    if (configFileCache == null) {
      configFileCache = 
        new HashMap<Class<? extends ConfigPropertiesCascadeBase>, ConfigPropertiesCascadeBase>();
    }
    
    ConfigPropertiesCascadeBase configObject = configFileCache.get(this.getClass());
    
    if (configObject == null) {
      
      logDebug("Config file has not be created yet, will create now: " + this.getMainConfigClasspath());
      
      configObject = retrieveFromConfigFiles();
      configFileCache.put(this.getClass(), configObject);
      
    } else {
      
      //see if that much time has passed
      if (configObject.needToCheckIfFilesNeedReloading()) {
        
        synchronized (configObject) {
          
          configObject = configFileCache.get(this.getClass());
          
          //check again in case another thread did it
          if (configObject.needToCheckIfFilesNeedReloading()) {
            
            if (configObject.filesNeedReloadingBasedOnContents()) {
              configObject = retrieveFromConfigFiles();
              configFileCache.put(this.getClass(), configObject);
            }
          }
        }
      }
    }
    
    return configObject;
  }
  
  /**
   * 
   * @return true if need to reload this config, false if not
   */
  protected boolean needToCheckIfFilesNeedReloading() {
    
    //get the time that this was created
    long lastCheckedTime = this.getLastCheckedTime();
    
    //get the timeToCheckSeconds if different
    int timeToCheckSeconds = this.getTimeToCheckConfigSeconds();
    
    //never reload.  0 means reload all the time?
    if (timeToCheckSeconds < 0) {
      return false;
    }
    
    //see if that much time has passed
    if (System.currentTimeMillis() - lastCheckedTime > timeToCheckSeconds * 1000) {
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
        if (!GrouperClientUtils.equals(configFile.getContents(), configFile.retrieveContents(this))) {
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
   * @param required
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
   * @param defaultValue
   * @param required
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
    if (GrouperClientUtils.isBlank(value) && !required) {
      return defaultValue;
    }
    if (GrouperClientUtils.isBlank(value) && required) {
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
    if (GrouperClientUtils.isBlank(value) && !required) {
      return defaultValue;
    }
    if (GrouperClientUtils.isBlank(value) && required) {
      throw new RuntimeException("Cant find integer property " + key + " in config file: " + this.getMainConfigClasspath() + ", it is required");
    }
    try {
      return GrouperClientUtils.intValue(value);
    } catch (Exception e) {
      
    }
    throw new RuntimeException("Invalid integer value: '" + value + "' for property: " 
        + key + " in config file: " + this.getMainConfigClasspath() + " in properties file");
    
  }

  /**
   * get a boolean and validate from grouper.client.properties
   * @param key
   * @param defaultValue
   * @param required
   * @return the string
   */
  public boolean propertyValueBooleanRequired(String key) {
    
    return propertyValueBoolean(key, false, true);
    
  }
  
  /**
   * get a boolean and validate from grouper.client.properties
   * @param key
   * @param defaultValue
   * @param required
   * @return the string
   */
  public int propertyValueIntRequired(String key) {
    
    return propertyValueInt(key, -1, true);
    
  }
  
  /**
   * get a boolean and validate from grouper.client.properties
   * @param key
   * @param defaultValue
   * @param required
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
   * @param useCache 
   * @param exceptionIfNotExist 
   * @param classInJar if not null, then look for the jar where this file is, and look in the same dir
   * @param callingLog 
   * @return the properties or null if not exist
   */
  protected static Properties propertiesFromResourceName(String resourceName, 
      boolean exceptionIfNotExist) {

    Properties properties = new Properties();

    URL url = null;
    
    try {
      
      url = GrouperClientUtils.computeUrl(resourceName, true);
      
    } catch (Exception e) {
      
      //I guess this ok
      logInfo("Problem loading config file: " + resourceName, e); 
      
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
      
    }
    return properties;
  }


}
