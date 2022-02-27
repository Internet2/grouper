package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.ui.tags.ConfigFormElement;

public class GroupTypeForEdit {

  private String attributeName;
  
  private int index;
  
  private String label;

  private String description;
  
  private String formElementType;
  
  private String value;
  
  private AttributeDefName attributeDefName;
  
  
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

  
  


}
