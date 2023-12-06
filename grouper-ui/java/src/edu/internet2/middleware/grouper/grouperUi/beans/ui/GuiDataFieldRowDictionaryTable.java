package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import java.util.List;

public class GuiDataFieldRowDictionaryTable {
  
  private List<GuiDataFieldRowDictionary> guiDataFieldRowDictionary;
  
  private String title;
  
  private String description;
  
  private String documentation;
  
  private boolean canAccess;
  
  private boolean isDataField;
  
  public List<GuiDataFieldRowDictionary> getGuiDataFieldRowDictionary() {
    return guiDataFieldRowDictionary;
  }

  public void setGuiDataFieldRowDictionary(
      List<GuiDataFieldRowDictionary> guiDataFieldRowDictionary) {
    this.guiDataFieldRowDictionary = guiDataFieldRowDictionary;
  }

  
  public String getTitle() {
    return title;
  }

  
  public void setTitle(String title) {
    this.title = title;
  }

  
  public String getDescription() {
    return description;
  }

  
  public void setDescription(String description) {
    this.description = description;
  }

  
  public String getDocumentation() {
    return documentation;
  }

  
  public void setDocumentation(String documentation) {
    this.documentation = documentation;
  }

  
  public boolean isCanAccess() {
    return canAccess;
  }

  
  public void setCanAccess(boolean canAccess) {
    this.canAccess = canAccess;
  }

  
  public boolean isDataField() {
    return isDataField;
  }

  
  public void setDataField(boolean isDataField) {
    this.isDataField = isDataField;
  }
  
  
}
