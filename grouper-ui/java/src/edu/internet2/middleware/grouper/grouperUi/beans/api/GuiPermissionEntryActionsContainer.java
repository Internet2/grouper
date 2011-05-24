package edu.internet2.middleware.grouper.grouperUi.beans.api;

import java.io.Serializable;
import java.util.List;

import edu.internet2.middleware.grouper.permissions.PermissionEntry;

/**
 * container for permission entry to sets of permissions grouped by action sets
 * @author mchyzer
 *
 */
@SuppressWarnings("serial")
public class GuiPermissionEntryActionsContainer implements Serializable {
  
  /** raw permission entries */
  private List<PermissionEntry> rawPermissionEntries = null;
  
  /**
   * raw permission entries
   * @return raw permission entries
   */
  public List<PermissionEntry> getRawPermissionEntries() {
    return rawPermissionEntries;
  }

  /**
   * raw permission entries
   * @param rawPermissionEntries1
   */
  public void setRawPermissionEntries(List<PermissionEntry> rawPermissionEntries1) {
    this.rawPermissionEntries = rawPermissionEntries1;
  }

  /**
   * list of the gui permission entry containers for this set of actions
   */
  private List<GuiPermissionEntryContainer> guiPermissionEntryContainers;

  /**
   * list of the gui permission entry containers for this set of actions
   * @return the containers
   */
  public List<GuiPermissionEntryContainer> getGuiPermissionEntryContainers() {
    return guiPermissionEntryContainers;
  }

  /**
   * list of the gui permission entry containers for this set of actions
   * @param guiPermissionEntryContainers1
   */
  public void setGuiPermissionEntryContainers(
      List<GuiPermissionEntryContainer> guiPermissionEntryContainers1) {
    this.guiPermissionEntryContainers = guiPermissionEntryContainers1;
  }
  
  /** actions for this set of permission containers */
  private List<String> actions;

  /**
   * actions
   * @return actions
   */
  public List<String> getActions() {
    return this.actions;
  }

  /**
   * actions
   * @param actions1
   */
  public void setActions(List<String> actions1) {
    this.actions = actions1;
  }
  
}
