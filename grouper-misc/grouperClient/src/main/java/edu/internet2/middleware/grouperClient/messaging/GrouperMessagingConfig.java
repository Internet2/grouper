/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperClient.messaging;

import edu.internet2.middleware.grouperClient.config.ConfigPropertiesCascadeUtils;
import edu.internet2.middleware.grouperClient.util.GrouperClientConfig;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.lang3.StringUtils;


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
        return ConfigPropertiesCascadeUtils.intValue(propertyValueString);
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
  
}

