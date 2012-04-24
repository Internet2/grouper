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
/*
  Copyright (C) 2004-2007 University Corporation for Advanced Internet Development, Inc.
  Copyright (C) 2004-2007 The University Of Pennsylvania

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
*/
package edu.internet2.middleware.grouper.ui.tags;

import java.util.ResourceBundle;

import javax.servlet.jsp.jstl.fmt.LocalizationContext;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * Utility methods for tags
 * @author mchyzer
 *
 */
public class TagUtils {

  /**
   * based on request get a nav string
   * @param key
   * @return value
   */
  public static String navResourceString(String key) {
    
    LocalizationContext localizationContext = (LocalizationContext)GrouperUiFilter
      .retrieveHttpServletRequest().getSession().getAttribute("nav");
    ResourceBundle nav = localizationContext.getResourceBundle();
    String value = nav.getString(key);
    return value;
  }
  
  /**
   * based on request get a nav string
   * @param key
   * @return if contains
   */
  public static boolean navResourceContainsKey(String key) {
    
    LocalizationContext localizationContext = (LocalizationContext)GrouperUiFilter
      .retrieveHttpServletRequest().getSession().getAttribute("nav");
    ResourceBundle nav = localizationContext.getResourceBundle();
    return nav.containsKey(key);
  }
  
  /**
   * based on request get a nav string
   * @param key 
   * @return value
   */
  public static String mediaResourceString(String key) {
    
    LocalizationContext localizationContext = (LocalizationContext)GrouperUiFilter
      .retrieveHttpServletRequest().getSession().getAttribute("media");
//    if (localizationContext == null) {
//      //we must be before the init session phase...
//      Properties properties = GrouperUtil.propertiesFromResourceName("resources/grouper/media.properties");
//      return properties.getProperty(key);
//    }
    ResourceBundle media = localizationContext.getResourceBundle();
    String value = media.getString(key);
    return value;
  }
  
  /**
   * based on request get a media boolean
   * @param key 
   * @param defaultValue if key isnt there, this is the default value
   * @return true if true, false if false
   */
  public static boolean mediaResourceBoolean(
      String key, boolean defaultValue) {
    
    String valueString = mediaResourceString(key);
    
    //handle if not in file
    if (StringUtils.isBlank(valueString)) {
      return defaultValue;
    }
    
    if (StringUtils.equalsIgnoreCase(valueString, "true") || StringUtils.equalsIgnoreCase(valueString, "t")) {
      return true;
    }
    
    if (StringUtils.equalsIgnoreCase(valueString, "false") || StringUtils.equalsIgnoreCase(valueString, "f")) {
      return false;
    }
    //throw descriptive exception
    throw new RuntimeException("Invalid value: '" + valueString + "' for key '" + key + "' in media properties" +
        " (or local or locale).  Should be true or false");
  }

  /**
   * based on request get a media int
   * @param key 
   * @param defaultValue if key isnt there, this is the default value
   * @return true if true, false if false
   */
  public static int mediaResourceInt(
      String key, int defaultValue) {
    
    String valueString = mediaResourceString(key);
    
    //handle if not in file
    if (StringUtils.isBlank(valueString)) {
      return defaultValue;
    }
    try {
      return GrouperUtil.intValue(valueString, defaultValue);
    } catch (Exception e) {
      //throw descriptive exception
      throw new RuntimeException("Invalid value: '" + valueString + "' for key '" + key + "' in media properties" +
          " (or local or locale).  Should be an int", e);
    }
  }

}
