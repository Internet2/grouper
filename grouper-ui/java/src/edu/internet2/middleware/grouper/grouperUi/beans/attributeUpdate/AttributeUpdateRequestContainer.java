/*
 * @author mchyzer
 * $Id: SimpleMembershipUpdateContainer.java,v 1.4 2009-11-02 08:50:40 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.grouperUi.beans.attributeUpdate;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignType;
import edu.internet2.middleware.grouper.attr.value.AttributeAssignValue;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiAttributeAssign;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiMember;
import edu.internet2.middleware.grouper.privs.PrivilegeSubjectContainer;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.ui.tags.TagUtils;
import edu.internet2.middleware.grouper.ui.util.MapWrapper;
import edu.internet2.middleware.grouper.util.GrouperUtil;



/**
 * bean for simple attribute update.  holds all state for this module
 */
@SuppressWarnings("serial")
public class AttributeUpdateRequestContainer implements Serializable {

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

  /** if we are assigning to a group, folder, etc */
  private AttributeAssignType attributeAssignType;
  
  /** if we are assigning to a group, folder, etc, this is the non underlying assign type */
  private AttributeAssignType attributeAssignAssignType;
  
  /**
   * if we are assigning to a group, folder, etc, this is the non underlying assign type
   * @return assign type
   */
  public AttributeAssignType getAttributeAssignAssignType() {
    return this.attributeAssignAssignType;
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
   * @return type
   */
  public AttributeAssignType getAttributeAssignType() {
    return this.attributeAssignType;
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
   * label for type
   * @return label for type
   */
  public String getAttributeAssignTypeLabelKey() {
    
    switch (this.getAttributeAssignType()) {

      case group:
      {
        return "simpleAttributeUpdate.assignHeaderOwnerGroup";
      }
      case stem:
      {
        return "simpleAttributeUpdate.assignHeaderOwnerFolder";
      }
      case member:
      {
        return "simpleAttributeUpdate.assignHeaderOwnerMember";
      }
      case imm_mem:
      {
        return "simpleAttributeUpdate.assignHeaderOwnerMembership";
      }
      case any_mem:
      {
        return "simpleAttributeUpdate.assignHeaderOwnerMembership";
      }
      case attr_def:
      {
        return "simpleAttributeUpdate.assignHeaderOwnerAttributeDefinition";
      }
      
      default:
        throw new RuntimeException("Not expecting attribute assign type: " + this.getAttributeAssignType());
    }
    
  }
  
  /**
   * if we are assigning to a group, folder, etc
   * @param attributeAssignType1
   */
  public void setAttributeAssignType(AttributeAssignType attributeAssignType1) {
    this.attributeAssignType = attributeAssignType1;
  }

  /** attribute def we are editing */
  private AttributeDef attributeDefToEdit;
  
  /** list of actions for the attribute def */
  private List<String> actions;

  /** list of actions which can be added as an action which implies */
  private List<String> newActionsCanImply;

  /** list of actions which can be added as an action which impliedBy */
  private List<String> newActionsCanImpliedBy;

  /** list of actions that imply this action */
  private List<String> actionsThatImply;
  
  /** list of actions implied by this action */
  private List<String> actionsImpliedBy;
  
  /** privilege subject containers */
  private Set<PrivilegeSubjectContainer> privilegeSubjectContainers;
  
  /** attribute assigns */
  private List<GuiAttributeAssign> guiAttributeAssigns;
  
  /**
   * attribute assigns
   * @return the attribute assigns
   */
  public List<GuiAttributeAssign> getGuiAttributeAssigns() {
    return this.guiAttributeAssigns;
  }

  /**
   * attribute assigns
   * @param attributeAssigns1
   */
  public void setGuiAttributeAssigns(List<GuiAttributeAssign> attributeAssigns1) {
    this.guiAttributeAssigns = attributeAssigns1;
  }

  /** gui members */
  private List<GuiMember> privilegeSubjectContainerGuiMembers;

  /** additional gui members to view or assign */
  private List<GuiMember> privilegeAdditionalGuiMembers;
  
  /**
   * additional gui members to view or assign
   * @return additional gui members to view or assign
   */
  public List<GuiMember> getPrivilegeAdditionalGuiMembers() {
    return this.privilegeAdditionalGuiMembers;
  }

  /**
   * calculate based on request object, and return list, unless it is already calculated
   * @return the list
   */
  public List<GuiMember> privilegeAdditionalGuiMembers() {
    if (this.privilegeAdditionalGuiMembers == null) {
      this.privilegeAdditionalGuiMembers = new ArrayList<GuiMember>();
      
      HttpServletRequest httpServletRequest = GrouperUiFilter.retrieveHttpServletRequest();
      
      //max of 200, no endless loops
      for (int i=0;i<200;i++) {
        
        String memberId = httpServletRequest.getParameter("additionalMemberId_" + i);
        if (StringUtils.isBlank(memberId)) {
          break;
        }
        
        Member member = MemberFinder.findByUuid(GrouperSession.staticGrouperSession(), memberId, true);
        
        this.privilegeAdditionalGuiMembers.add(new GuiMember(member));
        
      }
    }
    
    return this.privilegeAdditionalGuiMembers;
    
  }
  
  /**
   * additional gui members to view or assign
   * @param privilegeAdditionalGuiMembers1
   */
  public void setPrivilegeAdditionalGuiMembers(List<GuiMember> privilegeAdditionalGuiMembers1) {
    this.privilegeAdditionalGuiMembers = privilegeAdditionalGuiMembers1;
  }

  /**
   * gui members
   * @return gui members
   */
  public List<GuiMember> getPrivilegeSubjectContainerGuiMembers() {
    return this.privilegeSubjectContainerGuiMembers;
  }

  /**
   * gui members
   * @param privilegeSubjectContainerGuiMembers1
   */
  public void setPrivilegeSubjectContainerGuiMembers(
      List<GuiMember> privilegeSubjectContainerGuiMembers1) {
    this.privilegeSubjectContainerGuiMembers = privilegeSubjectContainerGuiMembers1;
  }

  /**
   * privilege subject containers
   * @return privilege subject containers
   */
  public Set<PrivilegeSubjectContainer> getPrivilegeSubjectContainers() {
    return this.privilegeSubjectContainers;
  }

  /**
   * privilege subject containers
   * @param privilegeSubjectContainers1
   */
  public void setPrivilegeSubjectContainers(
      Set<PrivilegeSubjectContainer> privilegeSubjectContainers1) {
    this.privilegeSubjectContainers = privilegeSubjectContainers1;
  }

  /**
   * list of actions implied by this action
   * @return actions
   */
  public List<String> getActionsImpliedBy() {
    return this.actionsImpliedBy;
  }

  /**
   * list of actions implied by this action
   * @param actionsImpliedBy1
   */
  public void setActionsImpliedBy(List<String> actionsImpliedBy1) {
    this.actionsImpliedBy = actionsImpliedBy1;
  }

  /** list of actions implied by this action immediately */
  private List<String> actionsImpliedByImmediate;
  
  /**
   * list of actions implied by this action immediately
   * @return actions
   */
  public List<String> getActionsImpliedByImmediate() {
    return this.actionsImpliedByImmediate;
  }

  /**
   * list of actions implied by this action immediately
   * @param actionsImpliedByImmediate1
   */
  public void setActionsImpliedByImmediate(List<String> actionsImpliedByImmediate1) {
    this.actionsImpliedByImmediate = actionsImpliedByImmediate1;
  }

  /** list of actions that imply this action immediately */
  private List<String> actionsThatImplyImmediate;
  
  /**
   * list of actions that imply this action immediately
   * @return actions
   */
  public List<String> getActionsThatImplyImmediate() {
    return this.actionsThatImplyImmediate;
  }

  /**
   * list of actions that imply this action immediately
   * @param actionsThatImplyImmediate1
   */
  public void setActionsThatImplyImmediate(List<String> actionsThatImplyImmediate1) {
    this.actionsThatImplyImmediate = actionsThatImplyImmediate1;
  }

  /**
   * list of actions that imply this action
   * @return the actions
   */
  public List<String> getActionsThatImply() {
    return this.actionsThatImply;
  }

  /**
   * list of actions that imply this action
   * @param actionsThatImply1
   */
  public void setActionsThatImply(List<String> actionsThatImply1) {
    this.actionsThatImply = actionsThatImply1;
  }

  /**
   * list of actions which can be added as an action which impliedBy
   * @return actions
   */
  public List<String> getNewActionsCanImpliedBy() {
    return this.newActionsCanImpliedBy;
  }

  /**
   * list of actions which can be added as an action which impliedBy
   * @param newActionsCanImpliedBy1
   */
  public void setNewActionsCanImpliedBy(List<String> newActionsCanImpliedBy1) {
    this.newActionsCanImpliedBy = newActionsCanImpliedBy1;
  }

  /**
   * list of actions which can be added as an action which implies
   * @return actions
   */
  public List<String> getNewActionsCanImply() {
    return this.newActionsCanImply;
  }

  /**
   * list of actions which can be added as an action which implies
   * @param newActionsCanImply1
   */
  public void setNewActionsCanImply(List<String> newActionsCanImply1) {
    this.newActionsCanImply = newActionsCanImply1;
  }

  /** action we are editing */
  private String action;
  
  /**
   * action we are editing
   * @return the action
   */
  public String getAction() {
    return this.action;
  }
  
  /**
   * action we are editing
   * @param action1 the action to set
   */
  public void setAction(String action1) {
    this.action = action1;
  }

  /**
   * list of actions for the attribute def
   * @return actions
   */
  public List<String> getActions() {
    return this.actions;
  }

  /**
   * list of actions for the attribute def
   * @param actions1
   */
  public void setActions(List<String> actions1) {
    this.actions = actions1;
  }

  /**
   * attribute def we are editing
   * @return the attribute def
   */
  public AttributeDef getAttributeDefToEdit() {
    return this.attributeDefToEdit;
  }

  /**
   * attribute def we are editing
   * @param attributeDefToEdit1
   */
  public void setAttributeDefToEdit(AttributeDef attributeDefToEdit1) {
    this.attributeDefToEdit = attributeDefToEdit1;
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
   * store to session scope
   */
  public void storeToRequest() {
    HttpServletRequest httpServletRequest = GrouperUiFilter.retrieveHttpServletRequest();
    httpServletRequest.setAttribute("attributeUpdateRequestContainer", this);
  }

  /**
   * retrieveFromSession, cannot be null
   * @return the app state in request scope
   */
  public static AttributeUpdateRequestContainer retrieveFromRequestOrCreate() {
    HttpServletRequest httpServletRequest = GrouperUiFilter.retrieveHttpServletRequest();
    AttributeUpdateRequestContainer attributeUpdateRequestContainer = 
      (AttributeUpdateRequestContainer)httpServletRequest.getAttribute("attributeUpdateRequestContainer");
    if (attributeUpdateRequestContainer == null) {
      attributeUpdateRequestContainer = new AttributeUpdateRequestContainer();
      attributeUpdateRequestContainer.storeToRequest();
    }
    return attributeUpdateRequestContainer;
  }

  /** 
   * text bean
   * @return text bean
   */
  public AttributeUpdateText getText() {
    return AttributeUpdateText.retrieveSingleton();
  }

  /**
   * if should show indirect privileges
   * @return true if should show
   */
  public boolean isShowIndirectPrivilegesComputed() {

    HttpServletRequest httpServletRequest = GrouperUiFilter.retrieveHttpServletRequest();

    String showIndirectPrivilegesString = httpServletRequest.getParameter("showIndirectPrivileges");
    
    return GrouperUtil.booleanValue(showIndirectPrivilegesString, false);
  }
  
  /**
   * if this is public
   * @return if we should show the privilege header
   */
  public Map<String, Boolean> getAllowAll() {
    
    return new MapWrapper<String, Boolean>() {

      @Override
      public Boolean get(Object key) {
        String priv = (String)key;
        if (StringUtils.equals(priv, "attrView")) {
          return AttributeUpdateRequestContainer.this.isAllowAllView();
        }
        if (StringUtils.equals(priv, "attrRead")) {
          return AttributeUpdateRequestContainer.this.isAllowAllRead();
        }
        if (StringUtils.equals(priv, "attrAdmin")) {
          return AttributeUpdateRequestContainer.this.isAllowAllAdmin();
        }
        if (StringUtils.equals(priv, "attrUpdate")) {
          return AttributeUpdateRequestContainer.this.isAllowAllUpdate();
        }
        if (StringUtils.equals(priv, "attrOptin")) {
          return AttributeUpdateRequestContainer.this.isAllowAllOptin();
        }
        if (StringUtils.equals(priv, "attrOptout")) {
          return AttributeUpdateRequestContainer.this.isAllowAllOptout();
        }
        throw new RuntimeException("Not expecting string");
      }
      
    };
    
  }

  /**
   * if we should show the privilege header
   * @return if we should show the privilege header
   */
  public Map<Integer, Boolean> getShowPrivilegeHeader() {
    
    return new MapWrapper<Integer, Boolean>() {

      @Override
      public Boolean get(Object key) {
        Integer theInt = GrouperUtil.intObjectValue(key, false);
        int repeatAfterRows = TagUtils.mediaResourceInt("simpleAttributeUpdate.repeatPrivilegeHeaderAfterRows", 20);

        if (theInt % repeatAfterRows == 0) {
          return true;
        }
        return false;
        
      }
      
    };
    
  }

  /** if optin should be checked on edit jsp */
  private boolean allowAllOptin;
  
  /** if optout should be checked on edit jsp */
  private boolean allowAllOptout;
  
  /** if read should be checked on edit jsp */
  private boolean allowAllRead;
  
  /** if view should be checked on edit jsp */
  private boolean allowAllView;
  
  /** if admin should be checked on edit jsp */
  private boolean allowAllAdmin;
  
  /** if update should be checked on edit jsp */
  private boolean allowAllUpdate;
  
  /**
   * if optin should be checked on edit jsp
   * @return the editAttributeDefOptin
   */
  public boolean isAllowAllOptin() {
    return this.allowAllOptin;
  }
  
  /**
   * if optin should be checked on edit jsp
   * @param editAttributeDefOptin1 the editAttributeDefOptin to set
   */
  public void setAllowAllOptin(boolean editAttributeDefOptin1) {
    this.allowAllOptin = editAttributeDefOptin1;
  }

  
  /**
   * if optout should be checked on edit jsp
   * @return the editAttributeDefOptout
   */
  public boolean isAllowAllOptout() {
    return this.allowAllOptout;
  }

  
  /**
   * if optout should be checked on edit jsp
   * @param editAttributeDefOptout1 the editAttributeDefOptout to set
   */
  public void setAllowAllOptout(boolean editAttributeDefOptout1) {
    this.allowAllOptout = editAttributeDefOptout1;
  }

  
  /**
   * if read should be checked on edit jsp
   * @return the editAttributeDefRead
   */
  public boolean isAllowAllRead() {
    return this.allowAllRead;
  }

  
  /**
   * if read should be checked on edit jsp
   * @param editAttributeDefRead1 the editAttributeDefRead to set
   */
  public void setAllowAllRead(boolean editAttributeDefRead1) {
    this.allowAllRead = editAttributeDefRead1;
  }

  
  /**
   * if view should be checked on edit jsp
   * @return the editAttributeDefView
   */
  public boolean isAllowAllView() {
    return this.allowAllView;
  }

  
  /**
   * if view should be checked on edit jsp
   * @param editAttributeDefView1 the editAttributeDefView to set
   */
  public void setAllowAllView(boolean editAttributeDefView1) {
    this.allowAllView = editAttributeDefView1;
  }

  
  /**
   * if admin should be checked on edit jsp
   * @return the editAttributeDefAdmin
   */
  public boolean isAllowAllAdmin() {
    return this.allowAllAdmin;
  }

  
  /**
   * if admin should be checked on edit jsp
   * @param editAttributeDefAdmin1 the editAttributeDefAdmin to set
   */
  public void setAllowAllAdmin(boolean editAttributeDefAdmin1) {
    this.allowAllAdmin = editAttributeDefAdmin1;
  }

  
  /**
   * if update should be checked on edit jsp
   * @return the editAttributeDefUpdate
   */
  public boolean isAllowAllUpdate() {
    return this.allowAllUpdate;
  }

  
  /**
   * if update should be checked on edit jsp
   * @param editAttributeDefUpdate1 the editAttributeDefUpdate to set
   */
  public void setAllowAllUpdate(boolean editAttributeDefUpdate1) {
    this.allowAllUpdate = editAttributeDefUpdate1;
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
  


}
