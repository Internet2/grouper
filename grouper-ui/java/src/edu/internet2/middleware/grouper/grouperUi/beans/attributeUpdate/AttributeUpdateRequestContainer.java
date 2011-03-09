/*
 * @author mchyzer
 * $Id: SimpleMembershipUpdateContainer.java,v 1.4 2009-11-02 08:50:40 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.grouperUi.beans.attributeUpdate;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import edu.internet2.middleware.grouper.attr.AttributeDef;
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
  
  /** gui members */
  private List<GuiMember> privilegeSubjectContainerGuiMembers;

  /** additional gui members to view or assign */
  private List<GuiMember> privilegeAdditionalGuiMembers;
  
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
   * if we should show the privilege header
   * @param rowNumber 0 indexed
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
  
}
