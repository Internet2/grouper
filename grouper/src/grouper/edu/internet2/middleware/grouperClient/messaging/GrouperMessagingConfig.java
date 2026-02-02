/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperClient.messaging;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.util.ExpirableCache;
import edu.internet2.middleware.grouperClient.util.GrouperClientConfig;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;


/**
 * configs in grouper.client.properties
 * # name of a messaging system.  note, "myAwsMessagingSystem" can be arbitrary
 * # grouper.messaging.system.myAwsMessagingSystem.name = aws
 * 
 * # class that implements edu.internet2.middleware.grouperClient.messaging.GrouperMessagingSystem
 * # grouper.messaging.system.myAwsMessagingSystem.class = 
 *
 */
public class GrouperMessagingConfig {

  /**
   * 
   */
  public GrouperMessagingConfig() {
  }

  /**
   * name of grouper message system configured in grouper.client.properties
   */
  private String name;

  /**
   * theClass of the grouper messaging config.  if null there is a problem
   */
  private Class<GrouperMessagingSystem> theClass;

  
  /**
   * @return the name
   */
  public String getName() {
    return this.name;
  }

  
  /**
   * @param name1 the name to set
   */
  public void setName(String name1) {
    this.name = name1;
  }
  
  /**
   * @return the theClass
   */
  public Class<GrouperMessagingSystem> getTheClass() {
    return this.theClass;
  }

  
  /**
   * @param theClass1 the theClass to set
   */
  public void setTheClass(Class<GrouperMessagingSystem> theClass1) {
    this.theClass = theClass1;
  }
 
  /**
   * default system name
   * default system settings to this messaging system, note, there is only one level of inheritance
   */
  private String defaultSystemName;

  /**
   * pattern for messaging system
   */
  private static Pattern grouperMessagingConfigPattern = Pattern.compile("^grouper.messaging.system.([^.]+).name$");

  /**
   * cache the messaging configs
   */
  private static ExpirableCache<Boolean, Map<String, GrouperMessagingConfig>> grouperMessagingConfigs
    = new ExpirableCache<Boolean, Map<String, GrouperMessagingConfig>>(2);
  
  /**
   * default system name
   * default system settings to this messaging system, note, there is only one level of inheritance
   * @return the defaultSystemName
   */
  public String getDefaultSystemName() {
    return this.defaultSystemName;
  }
  
  /**
   * default system name
   * default system settings to this messaging system, note, there is only one level of inheritance
   * @param defaultSystemName1 the defaultSystemName to set
   */
  public void setDefaultSystemName(String defaultSystemName1) {
    this.defaultSystemName = defaultSystemName1;
  }

  /**
   * 
   * @param grouperClientConfig 
   * @param propertyNameSuffix
   * @param defaultValue
   * @return the value or the override
   */
  public int propertyValueInt(GrouperClientConfig grouperClientConfig, String propertyNameSuffix, int defaultValue) {

    String propertyValueString = this.propertyValueString(grouperClientConfig, propertyNameSuffix);
    
    if (!StringUtils.isBlank(propertyValueString)) {
      try {
        return GrouperClientUtils.intValue(propertyValueString);
      } catch (Exception e) {
        
      }
      throw new RuntimeException("Invalid integer value: '" + propertyValueString + "' for property sufffix: " 
          + propertyNameSuffix + " in messaging system: " + this.name + " in config file: grouper.client.properties file");
    }
    return defaultValue;
  }
  
  /**
   * 
   * @param grouperClientConfig 
   * @param propertyNameSuffix
   * @return the value or the override
   */
  public String propertyValueString(GrouperClientConfig grouperClientConfig, String propertyNameSuffix) {
    
    String directValue = grouperClientConfig.propertyValueString("grouper.messaging.system." + this.name + "." + propertyNameSuffix);
    
    if (!StringUtils.isBlank(directValue)) {
      return directValue;
    }
    
    if (!StringUtils.isBlank(this.defaultSystemName)) {
      String inheritedValue = grouperClientConfig.propertyValueString("grouper.messaging.system." + this.defaultSystemName + "." + propertyNameSuffix);
      if (!StringUtils.isBlank(inheritedValue)) {
        return inheritedValue;
      }
    }
    
    return null;
  }


  /**
   * get a messaging config cant be null
   * @param systemName
   * @return the config
   */
  public static GrouperMessagingConfig retrieveGrouperMessagingConfigNonNull(String systemName) {
    GrouperMessagingConfig grouperMessagingConfig = retrieveGrouperMessagingConfigs().get(systemName);
    if (grouperMessagingConfig == null) {
      throw new RuntimeException("Cant find messaging config for system name: " + systemName);
    }
    
    return grouperMessagingConfig;
    
  }


  /**
   * process configs for messaging and return the map 
   * @return the configs
   */
  public static Map<String, GrouperMessagingConfig> retrieveGrouperMessagingConfigs() {
    
    GrouperClientConfig grouperClientConfig = GrouperClientConfig.retrieveConfig();

    Map<String, GrouperMessagingConfig> instanceGrouperMessagingConfigs = grouperMessagingConfigs.get(Boolean.TRUE);
    
    if (instanceGrouperMessagingConfigs == null) {
      synchronized (GrouperClientConfig.class) {
        
        instanceGrouperMessagingConfigs = grouperMessagingConfigs.get(Boolean.TRUE);
        
        if (instanceGrouperMessagingConfigs == null) {
          Map<String, GrouperMessagingConfig> theGrouperMessagingConfigs = new HashMap<String, GrouperMessagingConfig>();
          
          for (String configName : grouperClientConfig.propertyNames()) {
            
            //  # name of a messaging system.  note, "myAwsMessagingSystem" can be arbitrary
            //  # grouper.messaging.system.myAwsMessagingSystem.name = aws
            //
            //  # class that implements edu.internet2.middleware.grouperClient.messaging.GrouperMessagingSystem
            //  # grouper.messaging.system.myAwsMessagingSystem.class = 
  
            Matcher matcher = grouperMessagingConfigPattern.matcher(configName);
            if (matcher.matches()) {
              String name = matcher.group(1);
              GrouperMessagingConfig grouperMessagingConfig = new GrouperMessagingConfig();
              grouperMessagingConfig.setName(name);
              String defaultMessagingSystemName = grouperClientConfig.propertyValueString("grouper.messaging.system." + name + ".defaultSystemName");
              
              if (!StringUtils.isBlank(defaultMessagingSystemName)) {
                grouperMessagingConfig.setDefaultSystemName(defaultMessagingSystemName);
              }
              
              String theClassName = grouperMessagingConfig.propertyValueString(grouperClientConfig, "class");
              
              try {
                Class<GrouperMessagingSystem> grouperMessagingSystemClass = GrouperClientUtils.forName(theClassName);
                
                //make sure implements interface
                if (!GrouperMessagingSystem.class.isAssignableFrom(grouperMessagingSystemClass)) {
                  throw new RuntimeException(theClassName + " class does not implement " + GrouperMessagingSystem.class.getName());
                }
                grouperMessagingConfig.setTheClass(grouperMessagingSystemClass);
                theGrouperMessagingConfigs.put(name, grouperMessagingConfig);
              } catch (Exception e) {
                LOG.error("Cant instantiate messaging system: " + name + ", " + theClassName, e);
              }
              
            }
          }
          instanceGrouperMessagingConfigs = theGrouperMessagingConfigs;
          grouperMessagingConfigs.put(Boolean.TRUE, theGrouperMessagingConfigs);
          
        }
      }
    }
    return instanceGrouperMessagingConfigs;
  }
  
  /** logger */
  private static final Log LOG = GrouperUtil.getLog(GrouperMessagingConfig.class);

}

