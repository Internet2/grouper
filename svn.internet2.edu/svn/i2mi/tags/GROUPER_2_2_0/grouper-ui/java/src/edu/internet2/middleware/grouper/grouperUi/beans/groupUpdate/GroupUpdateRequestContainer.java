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
package edu.internet2.middleware.grouper.grouperUi.beans.groupUpdate;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiMember;
import edu.internet2.middleware.grouper.permissions.role.Role;
import edu.internet2.middleware.grouper.privs.PrivilegeSubjectContainer;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.ui.util.GrouperUiConfig;
import edu.internet2.middleware.grouper.ui.util.MapWrapper;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * bean for simple group update.  holds all state for this module
 */
@SuppressWarnings("serial")
public class GroupUpdateRequestContainer implements Serializable {

  /** group we are editing */
  private Group groupToEdit = null;
  
  /** if this is a create as opposed to update */
  private boolean create;

  /** if admin should be checked on edit jsp */
  private boolean allowAllAdmin;

  /** if optin should be checked on edit jsp */
  private boolean allowAllOptin;
  
  /** if groupAttrRead should be checked on edit jsp */
  private boolean allowAllGroupAttrRead;
  
  /** if groupAttrUpdate should be checked on edit jsp */
  private boolean allowAllGroupAttrUpdate;

  /** if optout should be checked on edit jsp */
  private boolean allowAllOptout;

  /** if read should be checked on edit jsp */
  private boolean allowAllRead;

  /** if update should be checked on edit jsp */
  private boolean allowAllUpdate;

  /** if view should be checked on edit jsp */
  private boolean allowAllView;

  /** additional gui members to view or assign */
  private List<GuiMember> privilegeAdditionalGuiMembers;

  /** gui members */
  private List<GuiMember> privilegeSubjectContainerGuiMembers;

  /** privilege subject containers */
  private Set<PrivilegeSubjectContainer> privilegeSubjectContainers;

  /** list of roles implied by this role */
  private List<Role> rolesImpliedBy;

  /** list of roles implied by this role immediately */
  private List<Role> rolesImpliedByImmediate;

  /** list of roles that imply this role */
  private List<Role> rolesThatImply;

  /** list of roles that imply this role immediately */
  private List<Role> rolesThatImplyImmediate;

  /**
   * directed graph nodes from
   */
  private List<String> roleGraphNodesFrom;

  /**
   * directed graph nodes to
   */
  private List<String> roleGraphNodesTo;

  /**
   * starting points in graph
   */
  private List<String> roleGraphStartingPoints;

  /**
   * group we are editing
   * @return grouper
   */
  public Group getGroupToEdit() {
    return this.groupToEdit;
  }

  /**
   * 
   * @param groupToEdit1
   */
  public void setGroupToEdit(Group groupToEdit1) {
    this.groupToEdit = groupToEdit1;
  }

  /**
   * store to session scope
   */
  public void storeToRequest() {
    HttpServletRequest httpServletRequest = GrouperUiFilter.retrieveHttpServletRequest();
    httpServletRequest.setAttribute("groupUpdateRequestContainer", this);
  }

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
   * if admin should be checked on edit jsp
   * @return the editGroupAdmin
   */
  public boolean isAllowAllAdmin() {
    return this.allowAllAdmin;
  }

  /**
   * if optin should be checked on edit jsp
   * @return the editGroupOptin
   */
  public boolean isAllowAllOptin() {
    return this.allowAllOptin;
  }
  
  /**
   * if groupAttrRead should be checked on edit jsp
   * @return the editGroupAttrRead
   */
  public boolean isAllowAllGroupAttrRead() {
    return this.allowAllGroupAttrRead;
  }
  
  /**
   * if groupAttrUpdate should be checked on edit jsp
   * @return the editGroupAttrUpdate
   */
  public boolean isAllowAllGroupAttrUpdate() {
    return this.allowAllGroupAttrUpdate;
  }

  /**
   * if optout should be checked on edit jsp
   * @return the editGroupOptout
   */
  public boolean isAllowAllOptout() {
    return this.allowAllOptout;
  }

  /**
   * if read should be checked on edit jsp
   * @return the editGroupRead
   */
  public boolean isAllowAllRead() {
    return this.allowAllRead;
  }

  /**
   * if update should be checked on edit jsp
   * @return the editGroupUpdate
   */
  public boolean isAllowAllUpdate() {
    return this.allowAllUpdate;
  }

  /**
   * if view should be checked on edit jsp
   * @return the editGroupView
   */
  public boolean isAllowAllView() {
    return this.allowAllView;
  }

  /**
   * if admin should be checked on edit jsp
   * @param editGroupAdmin1 the editGroupAdmin to set
   */
  public void setAllowAllAdmin(boolean editGroupAdmin1) {
    this.allowAllAdmin = editGroupAdmin1;
  }

  /**
   * if optin should be checked on edit jsp
   * @param editGroupOptin1 the editGroupOptin to set
   */
  public void setAllowAllOptin(boolean editGroupOptin1) {
    this.allowAllOptin = editGroupOptin1;
  }
  
  /**
   * if groupAttrRead should be checked on edit jsp
   * @param editGroupAttrRead1 the editGroupAttrRead to set
   */
  public void setAllowAllGroupAttrRead(boolean editGroupAttrRead1) {
    this.allowAllGroupAttrRead = editGroupAttrRead1;
  }
  
  /**
   * if groupAttrUpdate should be checked on edit jsp
   * @param editGroupAttrUpdate1 the editGroupAttrUpdate to set
   */
  public void setAllowAllGroupAttrUpdate(boolean editGroupAttrUpdate1) {
    this.allowAllGroupAttrUpdate = editGroupAttrUpdate1;
  }

  /**
   * if optout should be checked on edit jsp
   * @param editGroupOptout1 the editGroupOptout to set
   */
  public void setAllowAllOptout(boolean editGroupOptout1) {
    this.allowAllOptout = editGroupOptout1;
  }

  /**
   * if read should be checked on edit jsp
   * @param editGroupRead1 the editGroupRead to set
   */
  public void setAllowAllRead(boolean editGroupRead1) {
    this.allowAllRead = editGroupRead1;
  }

  /**
   * if update should be checked on edit jsp
   * @param editGroupUpdate1 the editGroupUpdate to set
   */
  public void setAllowAllUpdate(boolean editGroupUpdate1) {
    this.allowAllUpdate = editGroupUpdate1;
  }

  /**
   * if view should be checked on edit jsp
   * @param editGroupView1 the editGroupView to set
   */
  public void setAllowAllView(boolean editGroupView1) {
    this.allowAllView = editGroupView1;
  }

  /**
   * additional gui members to view or assign
   * @return additional gui members to view or assign
   */
  public List<GuiMember> getPrivilegeAdditionalGuiMembers() {
    return this.privilegeAdditionalGuiMembers;
  }

  /**
   * gui members
   * @return gui members
   */
  public List<GuiMember> getPrivilegeSubjectContainerGuiMembers() {
    return this.privilegeSubjectContainerGuiMembers;
  }

  /**
   * privilege subject containers
   * @return privilege subject containers
   */
  public Set<PrivilegeSubjectContainer> getPrivilegeSubjectContainers() {
    return this.privilegeSubjectContainers;
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
   * @param privilegeSubjectContainerGuiMembers1
   */
  public void setPrivilegeSubjectContainerGuiMembers(
      List<GuiMember> privilegeSubjectContainerGuiMembers1) {
    this.privilegeSubjectContainerGuiMembers = privilegeSubjectContainerGuiMembers1;
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
   * if should show indirect privileges
   * @return true if should show
   */
  public boolean isShowIndirectPrivilegesComputed() {
  
    HttpServletRequest httpServletRequest = GrouperUiFilter.retrieveHttpServletRequest();
  
    String showIndirectPrivilegesString = httpServletRequest.getParameter("showIndirectPrivileges");
    
    return GrouperUtil.booleanValue(showIndirectPrivilegesString, false);
  }

  /**
   * retrieveFromSession, cannot be null
   * @return the app state in request scope
   */
  public static GroupUpdateRequestContainer retrieveFromRequestOrCreate() {
    HttpServletRequest httpServletRequest = GrouperUiFilter.retrieveHttpServletRequest();
    GroupUpdateRequestContainer groupUpdateRequestContainer = 
      (GroupUpdateRequestContainer)httpServletRequest.getAttribute("groupUpdateRequestContainer");
    if (groupUpdateRequestContainer == null) {
      groupUpdateRequestContainer = new GroupUpdateRequestContainer();
      groupUpdateRequestContainer.storeToRequest();
    }
    return groupUpdateRequestContainer;
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
        int repeatAfterRows = GrouperUiConfig.retrieveConfig().propertyValueInt("simpleAttributeUpdate.repeatPrivilegeHeaderAfterRows", 20);

        if (theInt % repeatAfterRows == 0) {
          return true;
        }
        return false;
        
      }
      
    };
    
  }

  /**
   * list of roles implied by this role
   * @return roles
   */
  public List<Role> getRolesImpliedBy() {
    return this.rolesImpliedBy;
  }

  /**
   * list of roles implied by this role immediately
   * @return roles
   */
  public List<Role> getRolesImpliedByImmediate() {
    return this.rolesImpliedByImmediate;
  }

  /**
   * list of roles that imply this role
   * @return the roles
   */
  public List<Role> getRolesThatImply() {
    return this.rolesThatImply;
  }

  /**
   * list of roles that imply this role immediately
   * @return roles
   */
  public List<Role> getRolesThatImplyImmediate() {
    return this.rolesThatImplyImmediate;
  }

  /**
   * list of roles implied by this role
   * @param rolesImpliedBy1
   */
  public void setRolesImpliedBy(List<Role> rolesImpliedBy1) {
    this.rolesImpliedBy = rolesImpliedBy1;
  }

  /**
   * list of roles implied by this role immediately
   * @param rolesImpliedByImmediate1
   */
  public void setRolesImpliedByImmediate(List<Role> rolesImpliedByImmediate1) {
    this.rolesImpliedByImmediate = rolesImpliedByImmediate1;
  }

  /**
   * list of roles that imply this role
   * @param rolesThatImply1
   */
  public void setRolesThatImply(List<Role> rolesThatImply1) {
    this.rolesThatImply = rolesThatImply1;
  }

  /**
   * list of roles that imply this role immediately
   * @param rolesThatImplyImmediate1
   */
  public void setRolesThatImplyImmediate(List<Role> rolesThatImplyImmediate1) {
    this.rolesThatImplyImmediate = rolesThatImplyImmediate1;
  }

  /**
   * directed graph nodes from
   * @return directed graph nodes from
   */
  public List<String> getRoleGraphNodesFrom() {
    return this.roleGraphNodesFrom;
  }

  /**
   * directed graph nodes to
   * @return directed graph nodes to
   */
  public List<String> getRoleGraphNodesTo() {
    return this.roleGraphNodesTo;
  }

  /**
   * starting points in graph
   * @return starting points in graph
   */
  public List<String> getRoleGraphStartingPoints() {
    return this.roleGraphStartingPoints;
  }

  /**
   * directed graph nodes from
   * @param attributeNameGraphNodesFrom1
   */
  public void setRoleGraphNodesFrom(List<String> attributeNameGraphNodesFrom1) {
    this.roleGraphNodesFrom = attributeNameGraphNodesFrom1;
  }

  /**
   * directed graph nodes to
   * @param attributeNameGraphNodesTo1
   */
  public void setRoleGraphNodesTo(List<String> attributeNameGraphNodesTo1) {
    this.roleGraphNodesTo = attributeNameGraphNodesTo1;
  }

  /**
   * starting points in graph
   * @param startingPoints1
   */
  public void setRoleGraphStartingPoints(List<String> startingPoints1) {
    this.roleGraphStartingPoints = startingPoints1;
  }

}
