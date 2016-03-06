/*******************************************************************************
 * Copyright 2012 Internet2
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
package edu.internet2.middleware.tierApiAuthzClientExt.edu.internet2.middleware.morphString;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;


/**
 * If you need a property file, this will help cache and
 * retrieve properties.
 * <p />
 * 
 * @version $Id: MorphPropertyFileUtils.java,v 1.1 2008-11-30 10:57:26 mchyzer Exp $
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
  public synchronized static Properties retrieveProperties() {
    
    if (properties == null) {
    
      String propertiesFileLocation = MORPH_STRING_PROPERTIES;
    
      InputStream inputStream = null;
      try {
        inputStream = MorphPropertyFileUtils.class.getResourceAsStream(propertiesFileLocation);
        if (inputStream == null) {
          throw new RuntimeException("Cant find resource file on classpath: " + propertiesFileLocation);
        }
        Properties tempProperties = new Properties();
        tempProperties.load(inputStream);
        properties = tempProperties;
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
  
  /**
   * retrieve a property from the properties file
   * @param filePath 
   * @return the value
   */
  public synchronized static Properties retrievePropertiesFromFile(String filePath) {
    
    InputStream inputStream = null;
    File file = new File(filePath);

    try {
      if (!file.exists()) {
        throw new RuntimeException("Cant find file on classpath: " + MorphStringUtils.fileCanonicalPath(file));
      }
      inputStream = new FileInputStream(file);
      Properties tempProperties = new Properties();
      tempProperties.load(inputStream);
      properties = tempProperties;
    } catch (IOException ioe) {
      throw new RuntimeException("Problem reading file: " + MorphStringUtils.fileCanonicalPath(file), ioe);
    } finally {
      if (inputStream != null) {
        try {
          inputStream.close();
        } catch (IOException e) {
          //swallow
        }
      }
    }
    return properties;
  }

}
