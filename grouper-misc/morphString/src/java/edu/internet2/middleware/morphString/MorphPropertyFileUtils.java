package edu.internet2.middleware.morphString;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;


/**
 * If you need a property file, this will help cache and
 * retrieve properties.
 * <p />
 * 
 * @version $Id: MorphPropertyFileUtils.java,v 1.1 2008-09-13 18:51:48 mchyzer Exp $
 * @author mchyzer
 */
public class MorphPropertyFileUtils {

  /**
   * property file resource name
   */
  public static final String MORPH_STRING_PROPERTIES = "/morphString.properties";

  /** cached properties, DONT USE THIS VAR, JUST USE retrieveProperties() <br />
   * this is property file location (String) -> PropertiesBean object
   */
  private static Properties properties = null;
  
  /**
   * 
   */
  public MorphPropertyFileUtils() {
    super();
  }

  /**
   * retrieve a property, it must exist in the properties file
   * @param key
   * @param defaultValue 
   * @return the property value
   */
  public static boolean retrievePropertyBoolean(String key, boolean defaultValue) {
    String booleanValue = retrievePropertyString(key);
    
    if (booleanValue == null || "".equals(booleanValue)) {
      return defaultValue;
    }
    
    if (booleanValue.equalsIgnoreCase("true")) {
      return true;
    }
    if (booleanValue.equalsIgnoreCase("false")) {
      return false;
    }
    
    String logMessage = "Illegal value for boolean property, must be true of false: " 
      + key + ", " + booleanValue;
    
    //dont log since might cause endless loop
    
    throw new RuntimeException(logMessage);
  }
  
  /**
   * retrieve a property, it must exist in the properties file
   * @param key
   * @return the property value or null if not found
   */
  public static String retrievePropertyString(String key) {
    Properties localProperties = retrieveProperties();
    String value = localProperties.getProperty(key);
    return value;
  }
  
  
  
  /**
   * retrieve a property from the properties file
   * @return the value
   */
  private synchronized static Properties retrieveProperties() {
    
    if (properties == null) {
    
      String propertiesFileLocation = MORPH_STRING_PROPERTIES;
    
      InputStream inputStream = null;
      try {
        inputStream = MorphPropertyFileUtils.class.getResourceAsStream(propertiesFileLocation);
        if (inputStream == null) {
          throw new RuntimeException("Cant find resource file on classpath: " + propertiesFileLocation);
        }
        properties = new Properties();
        properties.load(inputStream);
      } catch (IOException ioe) {
        throw new RuntimeException(ioe);
      } finally {
        if (inputStream != null) {
          try {
            inputStream.close();
          } catch (IOException e) {
            //swallow
          }
        }
      }
      
    }
    
    return properties;
  }
  

}
