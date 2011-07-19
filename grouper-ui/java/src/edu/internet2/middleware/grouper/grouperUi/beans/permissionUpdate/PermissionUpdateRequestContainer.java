/*
 * @author mchyzer
 * $Id: SimpleMembershipUpdateContainer.java,v 1.4 2009-11-02 08:50:40 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.grouperUi.beans.permissionUpdate;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignType;
import edu.internet2.middleware.grouper.attr.value.AttributeAssignValue;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiAttributeAssign;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiPermissionEntry;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiPermissionEntryActionsContainer;
import edu.internet2.middleware.grouper.permissions.PermissionEntry.PermissionType;
import edu.internet2.middleware.grouper.permissions.limits.PermissionLimitDocumentation;
import edu.internet2.middleware.grouper.permissions.limits.PermissionLimitInterface;
import edu.internet2.middleware.grouper.permissions.limits.PermissionLimitUtils;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.ui.tags.TagUtils;
import edu.internet2.middleware.grouper.ui.util.MapWrapper;
import edu.internet2.middleware.grouper.util.GrouperUtil;



/**
 * bean for simple attribute update.  holds all state for this module
 */
@SuppressWarnings("serial")
public class PermissionUpdateRequestContainer implements Serializable {

  /** all limits on screen for documentation */
  private Set<AttributeDefName> allLimitsOnScreen = new LinkedHashSet<AttributeDefName>();
  
  /**
   * all limits on screen for documentation, these should be ordered by displayExtension
   * @return all limits on screen for documentation
   */
  public Set<AttributeDefName> getAllLimitsOnScreen() {
    return this.allLimitsOnScreen;
  }

  /**
   * limit documentation map, name of attributedef name, to string or documentation, could have HTML
   * @return limit map
   */
  public Map<String, String> getLimitDocumentation() {
    return new MapWrapper<String, String>() {

      @Override
      public String get(Object key) {
        String attributeDefName = (String)key;
        
        //this wont return null
        PermissionLimitInterface permissionLimitInterface = PermissionLimitUtils.logicInstance(attributeDefName);
        
        PermissionLimitDocumentation permissionLimitDocumentation = permissionLimitInterface.documentation();
        
        String documentationKey = permissionLimitDocumentation == null ? null : permissionLimitDocumentation.getDocumentationKey();
        
        if (StringUtils.isBlank(documentationKey)) {
          documentationKey = "simplePermissionUpdate.noLimitDocumentationConfigured";
        }
        
        String documentation = TagUtils.navResourceString(documentationKey);
        
        for (int i=0; i<GrouperUtil.length(permissionLimitDocumentation.getArgs()); i++) {
          documentation = StringUtils.replace(documentation, "{" + 0 + "}", permissionLimitDocumentation.getArgs().get(i));
        }
        
        return documentation;
        
      }
      
    };
  }
  
  /** if the panel to simulate limits should display */
  private boolean simulateLimits = false;
  
  /**
   * label for type
   * @return label for type
   */
  public String getAttributeAssignTypeLabelKey() {
    
    switch (this.getAttributeAssignType()) {

      case group:
      {
        return "simplePermissionUpdate.assignHeaderOwnerRole";
      }
      case any_mem:
      {
        return "simplePermissionUpdate.assignHeaderOwnerRoleMembership";
      }
      
      default:
        throw new RuntimeException("Not expecting attribute assign type: " + this.getAttributeAssignType());
    }
    
  }

  
  /**
   * if the panel to simulate limits should display
   * @return if the panel to simulate limits should display
   */
  public boolean isSimulateLimits() {
    return this.simulateLimits;
  }

  /**
   * if the panel to simulate limits should display
   * @param simulateLimits1
   */
  public void setSimulateLimits(boolean simulateLimits1) {
    this.simulateLimits = simulateLimits1;
  }

  /** default attribute def id */
  private String defaultAttributeDefId = null;
  
  
  
  /**
   * default attribute def id
   * @return default attribute def id
   */
  public String getDefaultAttributeDefId() {
    return this.defaultAttributeDefId;
  }

  /**
   * default attribute def id
   * @param defaultAttributeDefId1
   */
  public void setDefaultAttributeDefId(String defaultAttributeDefId1) {
    this.defaultAttributeDefId = defaultAttributeDefId1;
  }

  /**
   * default attribute def display name
   * @return default attribute def display name
   */
  public String getDefaultAttributeDefDisplayName() {
    return this.defaultAttributeDefDisplayName;
  }

  /**
   * default attribute def display name
   * @param defaultAttributeDefDisplayName1
   */
  public void setDefaultAttributeDefDisplayName(String defaultAttributeDefDisplayName1) {
    this.defaultAttributeDefDisplayName = defaultAttributeDefDisplayName1;
  }

  /** default attribute def display name */
  private String defaultAttributeDefDisplayName = null;
  
  /** default attribute name display name */
  private String defaultAttributeNameDisplayName = null;
  
  /**
   * default attribute name display name 
   * @return default attribute name display name
   */
  public String getDefaultAttributeNameDisplayName() {
    return this.defaultAttributeNameDisplayName;
  }

  /**
   * default attribute name display name
   * @param defaultAttributeNameDisplayName1
   */
  public void setDefaultAttributeNameDisplayName(String defaultAttributeNameDisplayName1) {
    this.defaultAttributeNameDisplayName = defaultAttributeNameDisplayName1;
  }

  /** gui permission entry */
  private GuiPermissionEntry guiPermissionEntry = null;
  
  /**
   * gui permission entry
   * @return gui permission entry
   */
  public GuiPermissionEntry getGuiPermissionEntry() {
    return this.guiPermissionEntry;
  }

  /**
   * gui permission entry
   * @param guiPermissionEntry1
   */
  public void setGuiPermissionEntry(GuiPermissionEntry guiPermissionEntry1) {
    this.guiPermissionEntry = guiPermissionEntry1;
  }

  /**
   * so we dont have a popup and extra click
   */
  private String assignmentStatusMessage;
  
  
  /**
   * so we dont have a popup
   * @return message
   */
  public String getAssignmentStatusMessage() {
    return this.assignmentStatusMessage;
  }

  /**
   * so we dont have a popup
   * @param assignmentStatusMessage1
   */
  public void setAssignmentStatusMessage(String assignmentStatusMessage1) {
    this.assignmentStatusMessage = assignmentStatusMessage1;
  }

  /**
   * aggregate of all actions of all attribute defs
   */
  private List<String> allActions = null;
  
  /**
   * aggregate of all actions of all attribute defs
   * @return actions
   */
  public List<String> getAllActions() {
    return this.allActions;
  }

  /**
   * aggregate of all actions of all attribute defs, number of them
   * @return actions size
   */
  public int getAllActionsSize() {
    return GrouperUtil.length(this.allActions);
  }

  /**
   * aggregate of all actions of all attribute defs
   * @param allActions1
   */
  public void setAllActions(List<String> allActions1) {
    this.allActions = allActions1;
  }

  /** type of permission, role or role_subject */
  private PermissionType permissionType;

  /**
   * type of permission, role or role_subject
   * @return type of permission
   */
  public PermissionType getPermissionType() {
    return this.permissionType;
  }

  /**
   * type of permission, role or role_subject
   * @param permissionType1
   */
  public void setPermissionType(PermissionType permissionType1) {
    this.permissionType = permissionType1;
  }

  /**
   * attribute assign value we are editing 
   */
  private AttributeAssignValue attributeAssignValue = null;
  
  /**
   * 
   * @return value
   */
  public AttributeAssignValue getAttributeAssignValue() {
    return this.attributeAssignValue;
  }

  /**
   * 
   * @param attributeAssignValue1
   */
  public void setAttributeAssignValue(AttributeAssignValue attributeAssignValue1) {
    this.attributeAssignValue = attributeAssignValue1;
  }

  /** gui attribute assign */
  private GuiAttributeAssign guiAttributeAssign = null;
  
  /** gui attribute assign on assignment */
  private GuiAttributeAssign guiAttributeAssignAssign = null;
  
  
  
  /**
   * gui attribute assign on assignment
   * @return gui attribute assign on assignment
   */
  public GuiAttributeAssign getGuiAttributeAssignAssign() {
    return this.guiAttributeAssignAssign;
  }

  /**
   * gui attribute assign on assignment
   * @param guiAttributeAssignAssign1
   */
  public void setGuiAttributeAssignAssign(GuiAttributeAssign guiAttributeAssignAssign1) {
    this.guiAttributeAssignAssign = guiAttributeAssignAssign1;
  }

  /**
   * enabledOnly, disabledOnly, or all (null)
   */
  private Boolean enabledDisabled = Boolean.TRUE;

  /** 
   * return enabledOnly, disabledOnly, or all (null)
   * @return enabledOnly, disabledOnly, or all (null)
   */
  public Boolean getEnabledDisabled() {
    return this.enabledDisabled;
  }

  /**
   * enabledOnly, disabledOnly, or all (null)
   * @param theEnabledDisabled
   */
  public void setEnabledDisabled(Boolean theEnabledDisabled) {
    this.enabledDisabled = theEnabledDisabled;
  }

  
  /**
   * list of sets of rows which have common actions
   */
  private List<GuiPermissionEntryActionsContainer> guiPermissionEntryActionsContainers;
  
  
  
  /**
   * list of sets of rows which have common actions
   * @return list of sets of rows which have common actions
   */
  public List<GuiPermissionEntryActionsContainer> getGuiPermissionEntryActionsContainers() {
    return guiPermissionEntryActionsContainers;
  }

  /**
   * list of sets of rows which have common actions
   * @param guiPermissionEntryActionsContainers1
   */
  public void setGuiPermissionEntryActionsContainers(
      List<GuiPermissionEntryActionsContainer> guiPermissionEntryActionsContainers1) {
    
    this.guiPermissionEntryActionsContainers = guiPermissionEntryActionsContainers1;
    
  }

  /** if this is a create as opposed to update */
  private boolean create;
  
  /**
   * if this is a create as opposed to update
   * @return if create
   */
  public boolean isCreate() {
    return this.create;
  }

  /**
   * if this is a create as opposed to update
   * @param create1
   */
  public void setCreate(boolean create1) {
    this.create = create1;
  }

  /**
   * store to request scope
   */
  public void storeToRequest() {
    HttpServletRequest httpServletRequest = GrouperUiFilter.retrieveHttpServletRequest();
    httpServletRequest.setAttribute("permissionUpdateRequestContainer", this);
  }

  /**
   * retrieveFromSession, cannot be null
   * @return the app state in request scope
   */
  public static PermissionUpdateRequestContainer retrieveFromRequestOrCreate() {
    HttpServletRequest httpServletRequest = GrouperUiFilter.retrieveHttpServletRequest();
    PermissionUpdateRequestContainer permissionUpdateRequestContainer = 
      (PermissionUpdateRequestContainer)httpServletRequest.getAttribute("permissionUpdateRequestContainer");
    if (permissionUpdateRequestContainer == null) {
      permissionUpdateRequestContainer = new PermissionUpdateRequestContainer();
      permissionUpdateRequestContainer.storeToRequest();
    }
    return permissionUpdateRequestContainer;
  }

  /**
   * gui attribute assign e.g. for edit screen
   * @return gui attribute assign
   */
  public GuiAttributeAssign getGuiAttributeAssign() {
    return this.guiAttributeAssign;
  }

  /**
   * gui attribute assignment e.g. for edit screen
   * @param guiAttributeAssign1
   */
  public void setGuiAttributeAssign(GuiAttributeAssign guiAttributeAssign1) {
    this.guiAttributeAssign = guiAttributeAssign1;
  }
  
  /**
   * map wrapper
   */
  private Map<Integer, Boolean> showHeader = new MapWrapper<Integer, Boolean>() {

    @Override
    public Boolean get(Object key) {
      Integer theInt = GrouperUtil.intObjectValue(key, false);
      int repeatAfterRows = TagUtils.mediaResourceInt("simplePermissionUpdate.repeatPermissionHeaderAfterRows", 20);

      boolean result = theInt % repeatAfterRows == 0;
      //System.out.println("Row: " + theInt + ": " + result);
      return result;
    }
    
  };

  /** default attribute name id */
  private String defaultAttributeNameId = null;

  /** default role display name */
  private String defaultRoleDisplayName = null;

  /** default role id */
  private String defaultRoleId = null;

  /** default member display name */
  private String defaultMemberDisplayName = null;

  /**
   * default member id
   * @return default member id
   */
  public String getDefaultMemberDisplayName() {
    return this.defaultMemberDisplayName;
  }

  /**
   * default member id
   * @param defaultMemberDisplayName1
   */
  public void setDefaultMemberDisplayName(String defaultMemberDisplayName1) {
    this.defaultMemberDisplayName = defaultMemberDisplayName1;
  }

  /**
   * default member id
   * @return default member id
   */
  public String getDefaultMemberId() {
    return this.defaultMemberId;
  }

  /**
   * default member id
   * @param defaultMemberId1
   */
  public void setDefaultMemberId(String defaultMemberId1) {
    this.defaultMemberId = defaultMemberId1;
  }

  /** default member id */
  private String defaultMemberId = null;

  /** default action */
  private String defaultAction = null;

  /** if we are assigning to a group, folder, etc, this is the non underlying assign type */
  private AttributeAssignType attributeAssignAssignType;

  /** if we are assigning to a group, folder, etc */
  private AttributeAssignType attributeAssignType;
  
  /**
   * default action 
   * @return default action
   */
  public String getDefaultAction() {
    return this.defaultAction;
  }

  /**
   * default action
   * @param defaultAction1
   */
  public void setDefaultAction(String defaultAction1) {
    this.defaultAction = defaultAction1;
  }

  /**
   * default role display name
   * @return default role display name
   */
  public String getDefaultRoleDisplayName() {
    return this.defaultRoleDisplayName;
  }

  /**
   * default role dislpay name
   * @param defaultRoleDisplayName1
   */
  public void setDefaultRoleDisplayName(String defaultRoleDisplayName1) {
    this.defaultRoleDisplayName = defaultRoleDisplayName1;
  }

  /**
   * default role id
   * @return default role id
   */
  public String getDefaultRoleId() {
    return this.defaultRoleId;
  }

  /**
   * default role id
   * @param defaultRoleId1
   */
  public void setDefaultRoleId(String defaultRoleId1) {
    this.defaultRoleId = defaultRoleId1;
  }

  /**
   * default attribute name id
   * @return default attribute name id
   */
  public String getDefaultAttributeNameId() {
    return this.defaultAttributeNameId;
  }

  /**
   * default attribute name id
   * @param defaultAttributeNameId1
   */
  public void setDefaultAttributeNameId(String defaultAttributeNameId1) {
    this.defaultAttributeNameId = defaultAttributeNameId1;
  }

  /**
   * if we should show the privilege header
   * @return if we should show the privilege header
   */
  public Map<Integer, Boolean> getShowHeader() {
    
    return this.showHeader;
    
  }

  /**
   * if we are assigning to a group, folder, etc, this is the non underlying assign type
   * @return assign type
   */
  public AttributeAssignType getAttributeAssignAssignType() {
    return this.attributeAssignAssignType;
  }

  /**
   * if we are assigning to a group, folder, etc
   * @return type
   */
  public AttributeAssignType getAttributeAssignType() {
    return this.attributeAssignType;
  }

  /**
   * if we are assigning to a group, folder, etc, this is the non underlying assign type
   * @param attributeAssignAssignType1
   */
  public void setAttributeAssignAssignType(AttributeAssignType attributeAssignAssignType1) {
    this.attributeAssignAssignType = attributeAssignAssignType1;
  }

  /**
   * if we are assigning to a group, folder, etc
   * @param attributeAssignType1
   */
  public void setAttributeAssignType(AttributeAssignType attributeAssignType1) {
    this.attributeAssignType = attributeAssignType1;
  }


}
