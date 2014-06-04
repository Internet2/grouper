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

import edu.internet2.middleware.grouper.ui.GrouperUiFilter;

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

}
