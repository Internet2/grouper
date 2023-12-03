package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import java.util.List;

public class GuiDataFieldRowDictionaryTable {
  
  private List<GuiDataFieldRowDictionary> guiDataFieldRowDictionary;
  
  private String dataRowAlias;
  private String description;
  private String dataOwner;
  private String howToGetAccess;
  
  public String getDataRowAlias() {
    return dataRowAlias;
  }
  
  public void setDataRowAlias(String dataRowAlias) {
    this.dataRowAlias = dataRowAlias;
  }
  
  public String getDescription() {
    return description;
  }
  
  public void setDescription(String description) {
    this.description = description;
  }
  
  public String getDataOwner() {
    return dataOwner;
  }
  
  public void setDataOwner(String dataOwner) {
    this.dataOwner = dataOwner;
  }
  
  public String getHowToGetAccess() {
    return howToGetAccess;
  }
  
  public void setHowToGetAccess(String howToGetAccess) {
    this.howToGetAccess = howToGetAccess;
  }

  public List<GuiDataFieldRowDictionary> getGuiDataFieldRowDictionary() {
    return guiDataFieldRowDictionary;
  }

  public void setGuiDataFieldRowDictionary(
      List<GuiDataFieldRowDictionary> guiDataFieldRowDictionary) {
    this.guiDataFieldRowDictionary = guiDataFieldRowDictionary;
  }

}
