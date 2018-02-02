/**
 * 
 */
package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import java.util.Map;
import java.util.Set;

import edu.internet2.middleware.grouper.attr.assign.AttributeAssignAction;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiGroup;
import edu.internet2.middleware.grouper.permissions.PermissionEntry;

/**
 * @author vsachdeva
 */
public class PermissionContainer {
  
  /**
   * group already associated with permission or to which permission needs to be assigned
   */
  private GuiGroup guiGroup;
  
  /**
   * can the permission be assigned by the logged in user
   */
  private boolean canAssignPermission;
  
  /**
   * permissions assigned to role/subject
   */
  private Set<PermissionEntry> permissions;
  
  /**
   * all actions (assigned + unassigned) available for the selected group
   */
  private Set<AttributeAssignAction> allActions;
  
  /**
   * permissions with limits to show on the view permissions screen
   */
  private Map<PermissionDtoKey, Set<ActionWithLimits>> permissionsMap;
  
  /**
   * @return group already associated with permission or to which permission needs to be assigned
   */
  public GuiGroup getGuiGroup() {
    return guiGroup;
  }

  /**
   * group already associated with permission or to which permission needs to be assigned
   * @param guiGroup
   */
  public void setGuiGroup(GuiGroup guiGroup) {
    this.guiGroup = guiGroup;
  }

  /**
   * @return true if the permission can be assigned by the logged in user
   */
  public boolean isCanAssignPermission() {
    return canAssignPermission;
  }

  /**
   * can the permission be assigned by the logged in user
   * @param canAssignPermission
   */
  public void setCanAssignPermission(boolean canAssignPermission) {
    this.canAssignPermission = canAssignPermission;
  }

  /**
   * @return permissions assigned to the selected group
   */
  public Set<PermissionEntry> getPermissions() {
    return permissions;
  }

  /**
   * @param permissions assigned to the selected group
   */
  public void setPermissions(Set<PermissionEntry> permissions) {
    this.permissions = permissions;
  }

  /**
   * @return all actions (assigned + unassigned) available for the selected group
   */
  public Set<AttributeAssignAction> getAllActions() {
    return allActions;
  }

  /**
   * @param allActions: all actions (assigned + unassigned) available for the selected group
   */
  public void setAllActions(Set<AttributeAssignAction> allActions) {
    this.allActions = allActions;
  }

  
  public Map<PermissionDtoKey, Set<ActionWithLimits>> getPermissionsMap() {
    return permissionsMap;
  }

  
  public void setPermissionsMap(
      Map<PermissionDtoKey, Set<ActionWithLimits>> permissionsMap) {
    this.permissionsMap = permissionsMap;
  }
  
  
  
}
