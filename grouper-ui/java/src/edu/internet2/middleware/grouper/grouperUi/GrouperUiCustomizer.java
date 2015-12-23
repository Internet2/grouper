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
 * @author mchyzer
 * $Id: GrouperUiCustomizer.java,v 1.3 2009-10-11 22:04:17 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.grouperUi;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.ui.util.GrouperUiConfig;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * Extend this class and configure to customize the UI
 */
public class GrouperUiCustomizer {
  
  /** grouper ui customizer instance */
  private static GrouperUiCustomizer grouperUiCustomizer = null;
  
  /**
   * get the instance configured.  Note this is cached, any changes require a restart
   * @return the customizer
   */
  @SuppressWarnings("unchecked")
  public static GrouperUiCustomizer instance() {
    
    if (grouperUiCustomizer == null) {
    
      String grouperUiCustomizerClassname = GrouperUiConfig.retrieveConfig().propertyValueString("grouperUiCustomizerClassname");
      grouperUiCustomizerClassname = StringUtils.defaultIfEmpty(grouperUiCustomizerClassname, 
          GrouperUiCustomizer.class.getName());
      Class<GrouperUiCustomizer> grouperUiCustomizerClass = GrouperUtil.forName(grouperUiCustomizerClassname);
      grouperUiCustomizer = GrouperUtil.newInstance(grouperUiCustomizerClass);
    }
    return grouperUiCustomizer;
    
  }
  
  /**
   * logout callback, if you want to do logic on callback
   */
  public void logout() {
    //logout logic
  }
  
}
