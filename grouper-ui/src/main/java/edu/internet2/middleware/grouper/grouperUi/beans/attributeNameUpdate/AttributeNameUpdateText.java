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
/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.grouperUi.beans.attributeNameUpdate;

import edu.internet2.middleware.grouper.ui.tags.TagUtils;


/**
 * get text in external URL (if applicable), and from nav.properties
 */
public class AttributeNameUpdateText {

  /** singleton */
  private static AttributeNameUpdateText attributeNameUpdateText = new AttributeNameUpdateText();
  
  /**
   * edit panel submit
   * @return submit button text
   */
  public String getFilterAttributeDefNameButton() {
    return text("simpleAttributeNameUpdate.filterAttributeDefNameButton");
  }
  
  /**
   * edit panel submit
   * @return submit button text
   */
  public String getNewAttributeDefNameButton() {
    return text("simpleAttributeNameUpdate.newAttributeDefNameButton");
  }
  
  /**
   * edit panel submit
   * @return submit button text
   */
  public String getEditPanelSubmit() {
    return text("simpleAttributeNameUpdate.editPanelSubmit");
  }
  
  /**
   * edit panel hierarchies
   * @return hierarchies button text
   */
  public String getEditPanelHierarchies() {
    return text("simpleAttributeNameUpdate.editPanelHierarchies");
  }
  
  /**
   * edit panel cancel
   * @return cancel button text
   */
  public String getEditPanelCancel() {
    return text("simpleAttributeNameUpdate.editPanelCancel");
  }
  
  /**
   * edit panel delete
   * @return delete button text
   */
  public String getEditPanelDelete() {
    return text("simpleAttributeNameUpdate.editPanelDelete");
  }
  
  /**
   * title of main screen
   * @return title of main screen
   */
  public String getCreateEditIndexTitle() {
    return text("simpleAttributeNameUpdate.createEditIndexTitle");
  }
  
  /**
   * infodot of title of main screen
   * @return infodot of title of main screen
   */
  public String getCreateEditIndexTitleInfodot() {
    return text("simpleAttributeNameUpdate.createEditIndexTitleInfodot");
  }
  
  /**
   * get singleton
   * @return singleton
   */
  public static AttributeNameUpdateText retrieveSingleton() {
    return attributeNameUpdateText;
  }

  
  /**
   * get text based on key
   * @param key
   * @return text
   */
  public String text(String key) {
    
    //finally, just go to nav.propreties
    return TagUtils.navResourceString(key);
    
  }
  
  
}
