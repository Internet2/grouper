package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import edu.internet2.middleware.grouper.attr.AttributeDefName;

/**
 * attribute that can be attached to group via grouper properties
 */
public class GroupTypeForEdit {

  /**
   * attribute def name
   */
  private String attributeName;
  
  /**
   * where to show this element on the screen relative to other
   */
  private int index;
  
  /**
   * label on the screen
   */
  private String label;

  /**
   * description to show under the html field
   */
  private String description;
  
  /**
   * type of html element e.g checkbox, textfield
   */
  private String formElementType;
  
  /**
   * attribute value
   */
  private String value;
  
  /**
   * attribute def name object. populated from grouper.properties
   */
  private AttributeDefName attributeDefName;

  /**
   * scope for the attribute def. it's used only when there's marker attribute 
   */
  private String scopeString;
  
  /**
   * hide or show the html element on the screen when it renders first
   */
  private boolean initiallyVisible = true;

  /**
   * marker attribute def name for attribute names that are associated with marker attributes. 
   * e.g attestationCalculatedDaysLeft is associated with attestation attribute def name
   */
  private AttributeDefName markerAttributeDefName;
  
  /**
   * config id per groupTypeForEdit 
   */
  private String configId;
  
  /**
   * marker config id
   */
  private String markerConfigId;
  
  
  public AttributeDefName getAttributeDefName() {
    return attributeDefName;
  }

  
  public void setAttributeDefName(AttributeDefName attributeDefName) {
    this.attributeDefName = attributeDefName;
  }



  public String getValue() {
    return value;
  }


  
  public void setValue(String value) {
    this.value = value;
  }


  public String getFormElementType() {
    return formElementType;
  }

  
  public void setFormElementType(String formElementType) {
    this.formElementType = formElementType;
  }

  public String getAttributeName() {
    return attributeName;
  }
  
  public void setAttributeName(String attributeName) {
    this.attributeName = attributeName;
  }

  
  public int getIndex() {
    return index;
  }

  
  public void setIndex(int index) {
    this.index = index;
  }


  
  public String getLabel() {
    return label;
  }


  
  public void setLabel(String label) {
    this.label = label;
  }


  
  public String getDescription() {
    return description;
  }


  
  public void setDescription(String description) {
    this.description = description;
  }




  public void setScopeString(String scopeString) {
   this.scopeString = scopeString;
  }




  
  public String getScopeString() {
    return scopeString;
  }




  public void setMarkerAttributeDefName(AttributeDefName markerAttributeDefName) {
    this.markerAttributeDefName = markerAttributeDefName;
  }


  
  
  public boolean isInitiallyVisible() {
    return initiallyVisible;
  }




  
  public void setInitiallyVisible(boolean initiallyVisible) {
    this.initiallyVisible = initiallyVisible;
  }




  public AttributeDefName getMarkerAttributeDefName() {
    return markerAttributeDefName;
  }




  
  public String getConfigId() {
    return configId;
  }


  
  public void setConfigId(String configId) {
    this.configId = configId;
  }




  
  public String getMarkerConfigId() {
    return markerConfigId;
  }




  
  public void setMarkerConfigId(String markerConfigId) {
    this.markerConfigId = markerConfigId;
  }
  
  
  
}
