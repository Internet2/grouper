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
 * $Id: GuiSubject.java,v 1.2 2009-10-11 22:04:17 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.grouperUi.beans.attributeDefNamePicker;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.ui.util.GrouperUiUtils;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * attributeDefName for attributeDefName picker result
 */
public class PickerResultAttributeDefName implements Serializable, Comparable<PickerResultAttributeDefName> {
  
  /** attributeDefName */
  private AttributeDefName attributeDefName;

  /**
   * if attributeDefName is a:b:c, then return b:c
   * @return the parent name and this name
   */
  public String getParentAndName() {
    String name = this.attributeDefName.getName();
    int lastColonIndex = name.lastIndexOf(':');
    int secondToLastColonIndex = lastColonIndex == -1 ? -1 : name.lastIndexOf(':', lastColonIndex-1);
    if (secondToLastColonIndex == -1) {
      return name;
    }
    return name.substring(secondToLastColonIndex+1, name.length());
  }
  
  /**
   * if attributeDefName is a:b:c, then return b:c
   * @return the parent name and this name
   */
  public String getParentAndDisplayName() {
    String displayName = this.attributeDefName.getDisplayName();
    int lastColonIndex = displayName.lastIndexOf(':');
    int secondToLastColonIndex = lastColonIndex == -1 ? -1 : displayName.lastIndexOf(':', lastColonIndex-1);
    if (secondToLastColonIndex == -1) {
      return displayName;
    }
    return displayName.substring(secondToLastColonIndex+1, displayName.length());
  }
  
  /**
   * if attributeDefName is a:b:c:d, then return b:c:d
   * @return the parent name and this name
   */
  public String getGrandParentAndName() {
    String name = this.attributeDefName.getName();
    int lastColonIndex = name.lastIndexOf(':');
    int secondToLastColonIndex = lastColonIndex == -1 ? -1 : name.lastIndexOf(':', lastColonIndex-1);
    int thirdToLastColonIndex = secondToLastColonIndex == -1 ? -1 : name.lastIndexOf(':', secondToLastColonIndex-1);
    if (thirdToLastColonIndex == -1) {
      return name;
    }
    return name.substring(thirdToLastColonIndex+1, name.length());
  }
  
  /**
   * if attributeDefName is a:b:c:d, then return b:c:d
   * @return the parent name and this name
   */
  public String getGrandParentAndDisplayName() {
    
    String displayName = this.attributeDefName.getDisplayName();
    int lastColonIndex = displayName.lastIndexOf(':');
    int secondToLastColonIndex = lastColonIndex == -1 ? -1 : displayName.lastIndexOf(':', lastColonIndex-1);
    int thirdToLastColonIndex = secondToLastColonIndex == -1 ? -1 : displayName.lastIndexOf(':', secondToLastColonIndex-1);
    if (thirdToLastColonIndex == -1) {
      return displayName;
    }
    return displayName.substring(thirdToLastColonIndex+1, displayName.length());
  }

  /**
   * index on page
   */
  private int index = 0;

  /**
   * @return the index
   */
  public int getIndex() {
    return this.index;
  }

  /**
   * @param index1 the index to set
   */
  public void setIndex(int index1) {
    this.index = index1;
  }

  /**
   * construct with attributeDefName
   * @param attributeDefName1
   * @param attributeDefNamePickerContainer 
   */
  public PickerResultAttributeDefName(AttributeDefName attributeDefName1, AttributeDefNamePickerContainer attributeDefNamePickerContainer) {
    this.attributeDefName = attributeDefName1;
    String attributeDefNameEl = attributeDefNamePickerContainer.configValue("attributeDefNameNameEl", false);
    
    if (!StringUtils.isBlank(attributeDefNameEl)) {
      
      //run the screen EL
      Map<String, Object> variableMap = new HashMap<String, Object>();
      variableMap.put("attributeDefName", this.attributeDefName);
      variableMap.put("pickerResultAttributeDefName", this);
      variableMap.put("grouperUiUtils", new GrouperUiUtils());
      this.screenLabel = GrouperUtil.substituteExpressionLanguage(attributeDefNameEl, variableMap);
    }
    
    //make sure there is something there
    if (StringUtils.isBlank(this.screenLabel) || StringUtils.equals("null", this.screenLabel)) {
      this.screenLabel = this.attributeDefName.getName();
    }
    
    //remove a prefix if down the hierarchy
    String removePrefix = attributeDefNamePickerContainer.configValue("removePrefixOnUi", false);
    if (!StringUtils.isBlank(removePrefix) && !StringUtils.isBlank(this.screenLabel) && this.screenLabel.startsWith(removePrefix)) {
      this.screenLabel = this.screenLabel.substring(removePrefix.length());
    }
    
  }

  /**
   * get screen label
   * @return screen label
   */
  public String getScreenLabel() {
    return this.screenLabel;
  }

  /** cache this */
  private String screenLabel;

  /**
   * get attributeDefName id for  caller
   * @return attributeDefName id
   */
  public String getAttributeDefNameId() {
    
    String attributeDefNameId = this.attributeDefName.getId();
    return attributeDefNameId;
  }

  /**
   * get attributeDefName id for  caller
   * @return attributeDefName id
   */
  public String getName() {
    
    String name = this.attributeDefName.getName();
    return name;
  }

  /**
   * attributeDefName
   * @return the attributeDefName
   */
  public AttributeDefName getAttributeDefName() {
    return this.attributeDefName;
  }
  
  /**
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  @Override
  public int compareTo(PickerResultAttributeDefName otherPickerResultAttributeDefName) {
    
    String theScreenLabel = StringUtils.defaultString(this.getScreenLabel());
    String otherScreenLabel = StringUtils.defaultString(otherPickerResultAttributeDefName.getScreenLabel());
    return theScreenLabel.compareTo(otherScreenLabel);
  }


}
