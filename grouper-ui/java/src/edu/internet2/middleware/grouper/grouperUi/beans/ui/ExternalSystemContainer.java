package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import java.util.ArrayList;
import java.util.List;

import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.subject.Subject;

/**
 * 
 */
public class ExternalSystemContainer {
  
  /**
   * grouper external system user is currently viewing/editing 
   */
  private GuiGrouperExternalSystem guiGrouperExternalSystem;
  
  /**
   * all external systems
   */
  private List<GuiGrouperExternalSystem> guiGrouperExternalSystems = new ArrayList<GuiGrouperExternalSystem>();
  
  
  public List<GuiGrouperExternalSystem> getGuiGrouperExternalSystems() {
    return guiGrouperExternalSystems;
  }

  
  public void setGuiGrouperExternalSystems(
      List<GuiGrouperExternalSystem> guiGrouperExternalSystems) {
    this.guiGrouperExternalSystems = guiGrouperExternalSystems;
  }
  
  
  public GuiGrouperExternalSystem getGuiGrouperExternalSystem() {
    return guiGrouperExternalSystem;
  }


  
  public void setGuiGrouperExternalSystem(
      GuiGrouperExternalSystem guiGrouperExternalSystem) {
    this.guiGrouperExternalSystem = guiGrouperExternalSystem;
  }


  /**
   * @return true if can view external systems
   */
  public boolean isCanViewExternalSystems() {
    
    Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    if (PrivilegeHelper.isWheelOrRoot(loggedInSubject)) {
      return true;
    }
    return false;
  }

}
