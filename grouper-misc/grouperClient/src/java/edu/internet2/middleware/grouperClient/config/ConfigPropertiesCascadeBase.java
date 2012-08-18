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
import java.util.List;
import java.util.Map;

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
   * when this config object was created or last checked for changes
   */
  private long lastCheckedTime = System.currentTimeMillis();
  
  /**
   * when this config object was created or last checked for changes
   * @return created time or last checked
   */
  protected long getLastCheckedTime() {
    return this.lastCheckedTime;
  }
  
  /**
   * when we build the config object, get the time to check config in seconds
   */
  private Long timeToCheckConfigSeconds = null;
  
  /**
   * when we build the config object, get the time to check config in seconds
   * @return the time to check config foe changes (in seconds)
   */
  protected Long getTimeToCheckConfigSeconds() {
    return this.timeToCheckConfigSeconds;
  }
  
  /**
   * config file cache
   */
  private static Map<Class<? extends ConfigPropertiesCascadeBase>, ConfigPropertiesCascadeBase> configFileCache = 
    new HashMap<Class<? extends ConfigPropertiesCascadeBase>, ConfigPropertiesCascadeBase>();
  
  /**
   * log object
   */
  private static final Log LOG = GrouperClientUtils.retrieveLog(ConfigPropertiesCascadeBaseTest.class);

  /**
   * config file type
   */
  protected static enum ConfigFileType {
    
    /**
     * get a config file from the filesystem
     */
    FILE {

      @Override
      public InputStream inputStream(String configFileTypeConfig) {
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

      @Override
      public InputStream inputStream(String configFileTypeConfig) {
        URL url = GrouperClientUtils.computeUrl(configFileTypeConfig, true);
        if (url == null) {
          throw new RuntimeException("Cant find config file from classpath: " + configFileTypeConfig);
        }
        try {
          return url.openStream();
        } catch (Exception e) {
          throw new RuntimeException("Problem reading config file from classpath: " + configFileTypeConfig, e);
        }

      }
    };

    /**
     * get the inputstream to read the config 
     * @param configFileTypeConfig
     * @return the input stream to get this config
     */
    public abstract InputStream inputStream(String configFileTypeConfig);

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
     * @return the contents
     */
    public String retrieveContents() {
      try {
        return GrouperClientUtils.toString(this.configFileType.inputStream(this.configFileTypeConfig), "UTF-8");
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
   * config file
   */
  private List<ConfigFile> configFiles = null;
  
  /**
   * see if there is one in cache, if so, use it, if not, get from config files
   * @return the config from file or cache
   */
  protected ConfigPropertiesCascadeBase retrieveFromConfigFileOrCache() {
    ConfigPropertiesCascadeBase configObject = configFileCache.get(this.getClass());
    boolean calculateConfigFromFiles = configObject == null;
    
    if (configObject == null) {
      
      LOG.debug("Config file has not be created yet, will create now: " + this.getMainConfigClasspath());
      
    } else {
      
      //see if that much time has passed
      if (configObject.needToCheckIfFilesNeedReloading()) {
        
        synchronized (configObject) {
          
          configObject = configFileCache.get(this.getClass());
          
          //check again in case another thread did it
          if (configObject.needToCheckIfFilesNeedReloading()) {
            
            calculateConfigFromFiles = configObject.filesNeedReloadingBasedOnContents();
            
          }
        }
        
      }
      
      
    }
    return null;
  }
  
  /**
   * 
   * @return true if need to reload this config, false if not
   */
  protected boolean needToCheckIfFilesNeedReloading() {
    
    //get the time that this was created
    long lastCheckedTime = this.getLastCheckedTime();
    
    //get the timeToCheckSeconds if different
    long timeToCheckSeconds = this.getTimeToCheckConfigSeconds();
    
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
        if (!GrouperClientUtils.equals(configFile.getContents(), configFile.retrieveContents())) {
          LOG.info("Contents changed for config file, reloading: " + configFile.getOriginalConfig());
          return true;
        }
      }
    } catch (Exception e) {
      //lets log and return the old one
      LOG.error("Error checking for changes in configs (will use previous version): " + this.getMainConfigClasspath(), e);
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
  public abstract String getMainConfigClasspath();
  
  /**
   * get the example config classpath, e.g. grouper.example.properties
   * @return the classpath of the example config file
   */
  public abstract String getMainExampleConfigClasspath();
  
}
