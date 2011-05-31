package edu.internet2.middleware.grouper.grouperUi.beans.api;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import edu.internet2.middleware.grouper.permissions.PermissionEntry;

/**
 * for displaying a permission analyze on the screen
 * @author mchyzer
 *
 */
@SuppressWarnings("serial")
public class GuiPermissionAnalyze implements Serializable {
  
  /**
   * if we should show the analyze screen row about the immediate subject assignment key
   */
  private boolean hasImmediateAssignmentKey;
  
  /**
   * the key in nav.properties of the immediate assignment row on analyze assignment screen
   */
  private String immediateAssignmentKey;

  /**
   * if we should show the analyze screen row about the immediate subject assignment key
   * @return if should
   */
  public boolean isHasImmediateAssignmentKey() {
    return this.hasImmediateAssignmentKey;
  }

  /**
   * if we should show the analyze screen row about the immediate subject assignment key
   * @param hasImmediateAssignmentKey1
   */
  public void setHasImmediateAssignmentKey(boolean hasImmediateAssignmentKey1) {
    this.hasImmediateAssignmentKey = hasImmediateAssignmentKey1;
  }

  /**
   * if we should show the analyze screen row about the immediate subject assignment key
   * @return if should
   */
  public String getImmediateAssignmentKey() {
    return this.immediateAssignmentKey;
  }

  /**
   * if we should show the analyze screen row about the immediate subject assignment key
   * @param immediateAssignmentKey1
   */
  public void setImmediateAssignmentKey(String immediateAssignmentKey1) {
    this.immediateAssignmentKey = immediateAssignmentKey1;
  }

  /**
   * 
   * @param permissionEntriesList
   */
  public void analyze(List<PermissionEntry> permissionEntriesList) {
    
    //see if there is a direct result
    this.hasImmediateAssignmentKey = true;
    
    //lets a get a heuristic...
    
    for (PermissionEntry permissionEntry : permissionEntriesList) {
      if (permissionEntry.isImmediatePermission()) {
        this.immediateAssignmentKey = "permissionUpdateRequestContainer.analyzeSubjectRoleAssignmentImmediate";
      } else {
        //see if it is a 
      }
    }
    
    
    
  }

  
}
