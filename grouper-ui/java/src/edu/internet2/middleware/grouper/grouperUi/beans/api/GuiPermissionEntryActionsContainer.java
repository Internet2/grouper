package edu.internet2.middleware.grouper.grouperUi.beans.api;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.keyvalue.MultiKey;

import edu.internet2.middleware.grouper.permissions.PermissionEntry;
import edu.internet2.middleware.grouper.permissions.PermissionEntry.PermissionType;
import edu.internet2.middleware.grouper.ui.util.MapWrapper;

/**
 * container for permission entry to sets of permissions grouped by action sets
 * @author mchyzer
 *
 */
@SuppressWarnings("serial")
public class GuiPermissionEntryActionsContainer implements Serializable {
  
  /** raw permission entries */
  private List<PermissionEntry> rawPermissionEntries = null;
  
  /** permission type */
  private PermissionType permissionType;
  
  /**
   * permission type
   * @return permission type
   */
  public PermissionType getPermissionType() {
    return this.permissionType;
  }

  /**
   * permission type
   * @param permissionType1
   */
  public void setPermissionType(PermissionType permissionType1) {
    this.permissionType = permissionType1;
  }

  /**
   * process raw entries
   */
  public void processRawEntries() {
    
    this.guiPermissionEntryContainers = new ArrayList<GuiPermissionEntryContainer>();
    
    //we need to group by role and permission resource
    Map<MultiKey, GuiPermissionEntryContainer> roleResourceToContainer = new LinkedHashMap<MultiKey, GuiPermissionEntryContainer>();

    for (PermissionEntry permissionEntry : this.getRawPermissionEntries()) {
      MultiKey roleResource = rowKey(permissionEntry);
      GuiPermissionEntryContainer guiPermissionEntryContainer = roleResourceToContainer.get(roleResource);
      
      //if not found, lets create a row there
      if (guiPermissionEntryContainer == null) {
        guiPermissionEntryContainer = new GuiPermissionEntryContainer();
        guiPermissionEntryContainer.setPermissionType(this.permissionType);
        guiPermissionEntryContainer.setRawPermissionEntries(new ArrayList<PermissionEntry>());
        roleResourceToContainer.put(roleResource, guiPermissionEntryContainer);
        
        guiPermissionEntryContainer.setRole(permissionEntry.getRole());
        guiPermissionEntryContainer.setPermissionResource(permissionEntry.getAttributeDefName());
        guiPermissionEntryContainer.setPermissionDefinition(permissionEntry.getAttributeDef());
        guiPermissionEntryContainer.setMemberId(permissionEntry.getMemberId());
        guiPermissionEntryContainer.setGuiSubject(new GuiSubject(permissionEntry.getMember().getSubject()));
        
        this.guiPermissionEntryContainers.add(guiPermissionEntryContainer);
      }
      guiPermissionEntryContainer.getRawPermissionEntries().add(permissionEntry);
    }
    
    Collections.sort(this.guiPermissionEntryContainers);
    
    //now lets process the inner objects
    for (GuiPermissionEntryContainer guiPermissionEntryContainer : this.guiPermissionEntryContainers) {
      guiPermissionEntryContainer.processRawEntries(this.actions);
    }
    
  }

  /**
   * 
   * @param permissionEntry
   * @return the multikey is the unique 
   */
  private MultiKey rowKey(PermissionEntry permissionEntry) {
    //lets see if we are role or role_subject
    switch (this.permissionType) {
      case role:
        return new MultiKey(permissionEntry.getRoleId(), permissionEntry.getAttributeDefNameId());
        
      case role_subject:
        return new MultiKey(permissionEntry.getRoleId(), permissionEntry.getMemberId(), permissionEntry.getAttributeDefNameId());
      default:
        throw new RuntimeException("Why not found? " + this.permissionType);
    }

  }
  
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
  
  /**
   * map wrapper if we should show an action
   */
  private Map<Integer, Boolean> showAction = new MapWrapper<Integer, Boolean>() {

    @Override
    public Boolean get(Object key) {
      return GuiPermissionEntryActionsContainer.this.actions.contains(key);
    }
    
  };
  
  /**
   * if we should show the privilege header
   * @return if we should show the privilege header
   */
  public Map<Integer, Boolean> getShowAction() {
    
    return this.showAction;
    
  }

  
}
