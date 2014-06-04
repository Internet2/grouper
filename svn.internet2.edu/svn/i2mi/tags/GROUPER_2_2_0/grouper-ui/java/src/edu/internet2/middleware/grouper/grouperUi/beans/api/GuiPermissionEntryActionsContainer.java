/*******************************************************************************
 * Copyright 2012 Internet2
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package edu.internet2.middleware.grouper.grouperUi.beans.api;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.keyvalue.MultiKey;

import edu.internet2.middleware.grouper.permissions.PermissionEntry;
import edu.internet2.middleware.grouper.permissions.PermissionEntry.PermissionType;
import edu.internet2.middleware.grouper.permissions.limits.PermissionLimitBean;
import edu.internet2.middleware.grouper.ui.util.MapWrapper;

/**
 * container for permission entry to sets of permissions grouped by action sets.
 * on the screen, the permissions are grouped by which ones have common actions, so the columns of actions
 * can look good (the columns will line up).  This represents a block of rows with common actions.
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
   * @param permissionEntryLimitBeanMap map of permission entry to the set of limits
   */
  public void processRawEntries(Map<PermissionEntry, Set<PermissionLimitBean>> permissionEntryLimitBeanMap) {
    
    this.guiPermissionEntryContainers = new ArrayList<GuiPermissionEntryContainer>();
    
    //we need to group by role and permission resource
    Map<MultiKey, GuiPermissionEntryContainer> roleResourceToContainer = new LinkedHashMap<MultiKey, GuiPermissionEntryContainer>();

    for (PermissionEntry permissionEntry : this.getRawPermissionEntries()) {
      
      //the row key is either the row and resource (if showing role permissions), or the role/resource/memberId (if showing member permissions)
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
        
        //if searching by role this will be null
        if (permissionEntry.getMember() != null) {
          guiPermissionEntryContainer.setGuiSubject(new GuiSubject(permissionEntry.getMember().getSubject()));
        }
        
        this.guiPermissionEntryContainers.add(guiPermissionEntryContainer);
      }
      guiPermissionEntryContainer.getRawPermissionEntries().add(permissionEntry);
    }
    //sort by role, subject (if applicable), and resource
    Collections.sort(this.guiPermissionEntryContainers);
    
    //now lets process the inner objects
    for (GuiPermissionEntryContainer guiPermissionEntryContainer : this.guiPermissionEntryContainers) {
      guiPermissionEntryContainer.processRawEntries(this.actions, permissionEntryLimitBeanMap);
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
