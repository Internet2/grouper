package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiObjectBase;

public class GuiProvisioningAssignment {
  
  private String type; // group or folder
  
  private GuiObjectBase guiGroupOrFolder;

  
  public GuiProvisioningAssignment(String type, GuiObjectBase guiGroupOrFolder) {
    super();
    this.type = type;
    this.guiGroupOrFolder = guiGroupOrFolder;
  }


  public String getType() {
    return type;
  }

  
  public void setType(String type) {
    this.type = type;
  }

  
  public GuiObjectBase getGuiGroupOrFolder() {
    return guiGroupOrFolder;
  }

  
  public void setGuiGroupOrFolder(GuiObjectBase guiGroupOrFolder) {
    this.guiGroupOrFolder = guiGroupOrFolder;
  }
  

}
