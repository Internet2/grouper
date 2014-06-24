/*******************************************************************************
 * Copyright 2014 Internet2
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
package edu.internet2.middleware.grouper.ui.util;

import java.util.Enumeration;
import java.util.ResourceBundle;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;

/**
 * do a resource bundle which consults the properties file before 
 * consulting the media.properties resource bundle
 * @author mchyzer
 *
 */
public class ChainedMediaPropertiesResourceBundle extends ResourceBundle {

  /**
   * constructor with the parent resource bundle
   * @param theParentBundle
   */
  public ChainedMediaPropertiesResourceBundle(ResourceBundle theParentBundle) {
    this.setParent(theParentBundle);
  }
  
  /**
   * @see ResourceBundle#getKeys()
   */
  @Override
  public Enumeration<String> getKeys() {
    Vector<String> keys = new Vector<String>();
    
    keys.addAll(GrouperUiConfig.retrieveConfig().propertyNames());
    keys.addAll(this.parent.keySet());
    
    return keys.elements();
    
  }

  /**
   * @see ResourceBundle#handleGetObject()
   */
  @Override
  protected Object handleGetObject(String key) {
    GrouperUiConfig uiConfig = GrouperUiConfig.retrieveConfig();
    if (uiConfig.containsKey(key)) {
      //do a default string since null means go up the chain
      return StringUtils.defaultString(uiConfig.propertyValueString(key));
    }
    return null;
  }

}
