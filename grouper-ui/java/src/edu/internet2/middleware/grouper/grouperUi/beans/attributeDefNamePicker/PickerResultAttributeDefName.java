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
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * attributeDefName for attributeDefName picker result
 */
public class PickerResultAttributeDefName implements Serializable, Comparable<PickerResultAttributeDefName> {
  
  /** attributeDefName */
  private AttributeDefName attributeDefName;

  /** picker result javascript attributeDefName */
  private PickerResultJavascriptAttributeDefName pickerResultJavascriptAttributeDefName;
  
  /**
   * @return the pickerResultJavascriptAttributeDefName
   */
  public PickerResultJavascriptAttributeDefName getPickerResultJavascriptAttributeDefName() {
    return this.pickerResultJavascriptAttributeDefName;
  }
  
  /**
   * @param pickerResultJavascriptAttributeDefName1 the pickerResultJavascriptAttributeDefName to set
   */
  public void setPickerResultJavascriptAttributeDefName(
      PickerResultJavascriptAttributeDefName pickerResultJavascriptAttributeDefName1) {
    this.pickerResultJavascriptAttributeDefName = pickerResultJavascriptAttributeDefName1;
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
   * this is either a variable name or null
   */
  private String attributeDefNameObjectName;
  
  
  /**
   * @return the attributeDefNameObjectName
   */
  public String getAttributeDefNameObjectName() {
    return this.attributeDefNameObjectName;
  }

  
  /**
   * @param attributeDefNameObjectName1 the attributeDefNameObjectName to set
   */
  public void setAttributeDefNameObjectName(String attributeDefNameObjectName1) {
    this.attributeDefNameObjectName = attributeDefNameObjectName1;
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
