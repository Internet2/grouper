package edu.internet2.middleware.grouperClient.util;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

/**
 * utility methods specific to grouper client
 */
public class GrouperClientUtils extends GrouperClientCommonUtils {

  /** override map for properties */
  private static Map<String, String> grouperClientOverrideMap = new LinkedHashMap<String, String>();
  
  /**
   * override map for properties for testing
   * @return the override map
   */
  public static Map<String, String> grouperClientOverrideMap() {
    return grouperClientOverrideMap;
  }
  
  /**
   * grouper client properties
   * @return the properties
   */
  public static Properties grouperClientProperties() {
    Properties properties = null;
    try {
      properties = propertiesFromResourceName(
        "grouper.client.properties", true, true, GrouperClientCommonUtils.class);
    } catch (Exception e) {
      throw new RuntimeException("Error accessing file: grouper.client.properties  " +
          "This properties file needs to be in the same directory as grouperClient.jar, or on your Java classpath", e);
    }
    return properties;
  }

  /**
   * get a property and validate required from grouper.client.properties
   * @param key 
   * @param required 
   * @return the value
   */
  public static String propertiesValue(String key, boolean required) {
    return GrouperClientUtils.propertiesValue("grouper.client.properties", 
        grouperClientProperties(), 
        GrouperClientUtils.grouperClientOverrideMap(), key, required);
  }


  /**
   * get a boolean and validate from grouper.client.properties
   * @param key
   * @param defaultValue
   * @param required
   * @return the string
   */
  public static boolean propertiesValueBoolean(String key, boolean defaultValue, boolean required ) {
    return GrouperClientUtils.propertiesValueBoolean(
        "grouper.client.properties", grouperClientProperties(), 
        GrouperClientUtils.grouperClientOverrideMap(), 
        key, defaultValue, required);
  }

  
}