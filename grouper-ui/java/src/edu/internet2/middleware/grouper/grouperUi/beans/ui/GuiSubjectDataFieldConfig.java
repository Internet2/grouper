package edu.internet2.middleware.grouper.grouperUi.beans.ui;


public class GuiSubjectDataFieldConfig {
  
  private String uiFriendlyValue;
  
  private String aliases;

  public GuiSubjectDataFieldConfig(String uiFriendlyValue, String aliases) {
    super();
    this.uiFriendlyValue = uiFriendlyValue;
    this.aliases = aliases;
  }

  
  public String getUiFriendlyValue() {
    return uiFriendlyValue;
  }

  
  public void setUiFriendlyValue(String uiFriendlyValue) {
    this.uiFriendlyValue = uiFriendlyValue;
  }

  
  public String getAliases() {
    return aliases;
  }

  
  public void setAliases(String aliases) {
    this.aliases = aliases;
  }
  
  

}
