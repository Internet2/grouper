package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import java.util.ArrayList;
import java.util.List;

import edu.internet2.middleware.grouper.app.externalSystem.GrouperExternalSystem;

public class GuiGrouperExternalSystem {
  
  private GrouperExternalSystem grouperExternalSystem;
  
  private GuiGrouperExternalSystem(GrouperExternalSystem grouperExternalSystem) {
    this.grouperExternalSystem = grouperExternalSystem;
  }
  
  public GrouperExternalSystem getGrouperExternalSystem() {
    return this.grouperExternalSystem;
  }
  
  public static List<GuiGrouperExternalSystem> convertFromGrouperExternalSystem(List<GrouperExternalSystem> grouperExternalSystems) {
    
    List<GuiGrouperExternalSystem> guiGrouperExternalSystems = new ArrayList<GuiGrouperExternalSystem>();
    
    for (GrouperExternalSystem grouperExternalSystem: grouperExternalSystems) {
      guiGrouperExternalSystems.add(new GuiGrouperExternalSystem(grouperExternalSystem));
    }
    
    return guiGrouperExternalSystems;
    
  }

}
