/**
 * 
 */
package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiGroup;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiMember;

/**
 * @author vsachdeva
 */
public class PermissionContainer {
  
  /**
   * group already associated with permission or to which permission needs to be assigned
   */
  private GuiGroup guiGroup;
  
  /**
   * member who already has permission or to who permission needs to be assigned
   */
  private GuiMember guiMember;
  
  /**
   * can the permission be assigned by the logged in user
   */
  private boolean canAssignPermission;
  
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
   * member who already has permission or to who permission needs to be assigned
   * @return
   */
  public GuiMember getGuiMember() {
    return guiMember;
  }

  /**
   * member who already has permission or to who permission needs to be assigned
   * @param guiMember1
   */
  public void setGuiMember(GuiMember guiMember1) {
    this.guiMember = guiMember1;
  }
  
  
}
