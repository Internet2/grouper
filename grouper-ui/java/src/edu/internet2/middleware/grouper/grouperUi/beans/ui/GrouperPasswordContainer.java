package edu.internet2.middleware.grouper.grouperUi.beans.ui;

public class GrouperPasswordContainer {
  
  /**
   * gui grouper password user is currently viewing/editing/adding
   */
  private GuiGrouperPassword guiGrouperPassword;
  
  
  private String privateKey;
  
  
  public String getPrivateKey() {
    return privateKey;
  }


  
  public void setPrivateKey(String privateKey) {
    this.privateKey = privateKey;
  }


  public GuiGrouperPassword getGuiGrouperPassword() {
    return guiGrouperPassword;
  }

  
  public void setGuiGrouperPassword(GuiGrouperPassword guiGrouperPassword) {
    this.guiGrouperPassword = guiGrouperPassword;
  }
  

}
