/*******************************************************************************
 * Copyright 2014 Internet2
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
package edu.internet2.middleware.grouper.grouperUi.serviceLogic;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.FieldType;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.MembershipFinder;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiGroup;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiMembershipSubjectContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiPaging;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiResponseJs;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction.GuiMessageType;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GrouperRequestContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.MyGroupsContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.TextContainer;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.membership.MembershipContainer;
import edu.internet2.middleware.grouper.membership.MembershipSubjectContainer;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.ui.tags.GrouperPagingTag2;
import edu.internet2.middleware.grouper.ui.util.GrouperUiUserData;
import edu.internet2.middleware.grouper.userData.GrouperUserDataApi;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;


public class UiV2MyGroups {

  /**
   * join group
   * @param request
   * @param response
   */
  public void joinGroup(HttpServletRequest request, HttpServletResponse response) {

    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();

    GrouperSession grouperSession = null;
  
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
    try {
      grouperSession = GrouperSession.start(loggedInSubject);
  
      final String groupId = request.getParameter("groupId");
      
      Group group = GroupFinder.findByUuid(grouperSession, groupId, false);
      if (!group.canHavePrivilege(loggedInSubject, AccessPrivilege.OPTIN.getName(), false)) {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("myGroupsJoinGroupCantFindGroup")));
      } else {
        boolean madeChanges = group.addMember(loggedInSubject, false);
        
        if (madeChanges) {
          
          guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
              TextContainer.retrieveFromRequest().getText().get("myGroupsJoinGroupSuccess")));
              
        } else {
          
          //not sure why this would happen (race condition?)
          guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.info, 
              TextContainer.retrieveFromRequest().getText().get("myGroupsJoinGroupNoChangesSuccess")));
    
        }
      }
          
      
      myGroupsJoinHelper(request, response);
  
      GrouperUserDataApi.recentlyUsedGroupAdd(GrouperUiUserData.grouperUiGroupNameForUserData(), 
          loggedInSubject, group);

    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }

  }
  
  /**
   * my groups
   * @param request
   * @param response
   */
  public void myGroups(HttpServletRequest request, HttpServletResponse response) {
  
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
  
    GrouperSession grouperSession = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/myGroups/myGroups.jsp"));
  
      
      myGroupsHelper(request, response);
  
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
     * the filter button was pressed on the my groups page, or paging or sorting, or something
     * @param request
     * @param response
     */
    private void myGroupsHelper(HttpServletRequest request, HttpServletResponse response) {
  
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
      String myGroupsFilter = StringUtils.trimToEmpty(request.getParameter("myGroupsFilter"));
      
      MyGroupsContainer myGroupContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getMyGroupsContainer();
      
      //dont give an error if 0
      if (myGroupsFilter.length() == 1) {
    
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, "#myGroupsFilterId",
            TextContainer.retrieveFromRequest().getText().get("myGroupsErrorNotEnoughChars")));
        
        //clear out the results
        guiResponseJs.addAction(GuiScreenAction.newInnerHtml("#myGroupsResultsId", ""));
    
        return;
      }
      
      GuiPaging guiPaging = myGroupContainer.getMyGroupsGuiPaging();
      QueryOptions queryOptions = QueryOptions.create("displayName", true, null, null);
      
      GrouperPagingTag2.processRequest(request, guiPaging, queryOptions); 
      
      GroupFinder groupFinder = new GroupFinder()
        .assignSubject(GrouperSession.staticGrouperSession().getSubject())
        .assignPrivileges(AccessPrivilege.MANAGE_PRIVILEGES)
        .assignQueryOptions(queryOptions);
  
      if (!StringUtils.isBlank(myGroupsFilter)) {
        groupFinder.assignSplitScope(true);
        groupFinder.assignScope(myGroupsFilter);
      }
    
      Set<Group> results = groupFinder.findGroups();
      
      //this shouldnt be null, but make sure
      if (results == null) {
        results = new HashSet<Group>();
      }
  
      if (GrouperUtil.length(results) == 0) {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
          TextContainer.retrieveFromRequest().getText().get("myGroupsNoResultsFound")));
      }
      
      myGroupContainer.setGuiGroupsUserManages(GuiGroup.convertFromGroups(results));
      
      guiPaging.setTotalRecordCount(queryOptions.getQueryPaging().getTotalRecordCount());
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#myGroupsResultsId", 
          "/WEB-INF/grouperUi2/myGroups/myGroupsContents.jsp"));
  }

  /**
   * my groups reset button
   * @param request
   * @param response
   */
  public void myGroupsReset(HttpServletRequest request, HttpServletResponse response) {
  
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
  
    GrouperSession grouperSession = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
      //clear out form
      guiResponseJs.addAction(GuiScreenAction.newFormFieldValue("myGroupsFilter", ""));
      
      //get the unfiltered groups
      myGroupsHelper(request, response);
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * my groups
   * @param request
   * @param response
   */
  public void myGroupsSubmit(HttpServletRequest request, HttpServletResponse response) {
  
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
  
    GrouperSession grouperSession = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      myGroupsHelper(request, response);
  
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * my groups join
   * @param request
   * @param response
   */
  public void myGroupsJoin(HttpServletRequest request, HttpServletResponse response) {
  
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
  
    GrouperSession grouperSession = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/myGroups/myGroupsJoin.jsp"));
  
      
      myGroupsJoinHelper(request, response);
  
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
     * the filter button was pressed on the my groups join page, or paging or sorting, or something
     * @param request
     * @param response
     */
    private void myGroupsJoinHelper(HttpServletRequest request, HttpServletResponse response) {

      final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();

      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
      String myGroupsFilter = StringUtils.trimToEmpty(request.getParameter("myGroupsFilter"));
      
      MyGroupsContainer myGroupsContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getMyGroupsContainer();
      
      //dont give an error if 0
      if (myGroupsFilter.length() == 1) {
    
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, "#myGroupsFilterId",
            TextContainer.retrieveFromRequest().getText().get("myGroupsErrorNotEnoughChars")));
        
        //clear out the results
        guiResponseJs.addAction(GuiScreenAction.newInnerHtml("#myGroupsResultsId", ""));
    
        return;
      }
      
      GuiPaging guiPaging = myGroupsContainer.getMyGroupsGuiPaging();
      QueryOptions queryOptions = QueryOptions.create("displayName", true, null, null);
      
      GrouperPagingTag2.processRequest(request, guiPaging, queryOptions); 
      
      GroupFinder groupFinder = new GroupFinder()
        .assignSubject(GrouperSession.staticGrouperSession().getSubject())
        .assignPrivileges(AccessPrivilege.OPTIN_PRIVILEGES)
        .assignSubjectNotInGroup(loggedInSubject)
        .assignQueryOptions(queryOptions);
  
      if (!StringUtils.isBlank(myGroupsFilter)) {
        groupFinder.assignSplitScope(true);
        groupFinder.assignScope(myGroupsFilter);
      }
    
      //  Set<Group> results = (Set<Group>)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      //    
      //    @Override
      //    public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
      //      return groupFinder.findGroups();
      //    }
      //  });
      Set<Group> results = groupFinder.findGroups();
      
      //this shouldnt be null, but make sure
      if (results == null) {
        results = new HashSet<Group>();
      }
  
      if (GrouperUtil.length(results) == 0) {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
          TextContainer.retrieveFromRequest().getText().get("myGroupsNoResultsFound")));
      }
      
      myGroupsContainer.setGuiGroupsUserManages(GuiGroup.convertFromGroups(results));
      
      guiPaging.setTotalRecordCount(queryOptions.getQueryPaging().getTotalRecordCount());
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#myGroupsResultsId", 
          "/WEB-INF/grouperUi2/myGroups/myGroupsJoinContents.jsp"));
        
  }

  /**
   * my groups reset button
   * @param request
   * @param response
   */
  public void myGroupsJoinReset(HttpServletRequest request, HttpServletResponse response) {
  
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
  
    GrouperSession grouperSession = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
      //clear out form
      guiResponseJs.addAction(GuiScreenAction.newFormFieldValue("myGroupsFilter", ""));
      
      //get the unfiltered groups
      myGroupsJoinHelper(request, response);
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * my groups join
   * @param request
   * @param response
   */
  public void myGroupsJoinSubmit(HttpServletRequest request, HttpServletResponse response) {
  
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
  
    GrouperSession grouperSession = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      myGroupsJoinHelper(request, response);
  
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * leave group
   * @param request
   * @param response
   */
  public void leaveGroup(HttpServletRequest request, HttpServletResponse response) {
  
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
  
    GrouperSession grouperSession = null;
  
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
    try {
      grouperSession = GrouperSession.start(loggedInSubject);
  
      final String groupId = request.getParameter("groupId");
      
      Group group = GroupFinder.findByUuid(grouperSession, groupId, false);
      if (!group.canHavePrivilege(loggedInSubject, AccessPrivilege.OPTOUT.getName(), false)) {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("myGroupsMembershipsCantFindGroup")));
      } else {
        boolean madeChanges = group.deleteMember(loggedInSubject, false);
        
        if (madeChanges) {
          
          guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
              TextContainer.retrieveFromRequest().getText().get("myGroupsMembershipsLeftSuccess")));
              
        } else {
          
          //not sure why this would happen (race condition?)
          guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.info, 
              TextContainer.retrieveFromRequest().getText().get("myGroupsMembershipsLeftNoChangesSuccess")));
    
        }
      }
      
      myGroupsMembershipsHelper(request, response);
  
      GrouperUserDataApi.recentlyUsedGroupAdd(GrouperUiUserData.grouperUiGroupNameForUserData(), 
          loggedInSubject, group);
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  
  }

  /**
   * my groups memberships
   * @param request
   * @param response
   */
  public void myGroupsMemberships(HttpServletRequest request, HttpServletResponse response) {
  
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
  
    GrouperSession grouperSession = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/myGroups/myGroupsMemberships.jsp"));
  
      
      myGroupsMembershipsHelper(request, response);
  
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
     * the filter button was pressed on the my groups memberships page, or paging or sorting, or something
     * @param request
     * @param response
     */
    private void myGroupsMembershipsHelper(HttpServletRequest request, HttpServletResponse response) {
  
      final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
      final String myGroupsFilter = StringUtils.trimToEmpty(request.getParameter("myGroupsFilter"));
      
      MyGroupsContainer myGroupsContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getMyGroupsContainer();
      
      //dont give an error if 0
      if (myGroupsFilter.length() == 1) {
    
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, "#myGroupsFilterId",
            TextContainer.retrieveFromRequest().getText().get("myGroupsErrorNotEnoughChars")));
        
        //clear out the results
        guiResponseJs.addAction(GuiScreenAction.newInnerHtml("#myGroupsResultsId", ""));
    
        return;
      }
      
      final GuiPaging guiPaging = myGroupsContainer.getMyGroupsGuiPaging();
      final QueryOptions queryOptions = new QueryOptions();
      
      GrouperPagingTag2.processRequest(request, guiPaging, queryOptions); 

      MembershipFinder membershipFinder = new MembershipFinder()
        .assignSubjectHasMembershipForGroup(loggedInSubject)
        .addSubject(loggedInSubject)
        .assignCheckSecurity(true)
        .assignPrivilegesTheUserHas(AccessPrivilege.OPT_OR_READ_PRIVILEGES)
        .assignEnabled(true)
        .assignQueryOptionsForGroup(queryOptions);
      if (!StringUtils.isBlank(myGroupsFilter)) {
        membershipFinder.assignSplitScopeForGroup(true);
        membershipFinder.assignScopeForGroup(myGroupsFilter);
      }
            
      Set<MembershipSubjectContainer> results = membershipFinder
                  .findMembershipResult().getMembershipSubjectContainers();
      
      // we need to get user privs too for optout
      // which groups
      Set<String> groupIds = new HashSet<String>();
      for (MembershipSubjectContainer membershipSubjectContainer : results) {
        groupIds.add(membershipSubjectContainer.getGroupOwner().getId());
      }
      membershipFinder = new MembershipFinder()
          .assignGroupIds(groupIds).assignCheckSecurity(false)
          .addSubject(loggedInSubject)
          .assignFieldType(FieldType.ACCESS)
          .assignEnabled(true)
          .assignHasFieldForMember(true)
          .assignHasMembershipTypeForMember(true);
      Set<MembershipSubjectContainer> privilegeResults = membershipFinder
          .findMembershipResult().getMembershipSubjectContainers();
      
      Map<String, MembershipSubjectContainer> privilegeGroupIdToMembershipSubjectContainer = new HashMap<String, MembershipSubjectContainer>();
      for (MembershipSubjectContainer membershipSubjectContainer : GrouperUtil.nonNull(privilegeResults)) {
        privilegeGroupIdToMembershipSubjectContainer.put(membershipSubjectContainer.getGroupOwner().getId(), membershipSubjectContainer);
      }
      
      for (MembershipSubjectContainer membershipSubjectContainer : results) {
        Group group = membershipSubjectContainer.getGroupOwner();
        MembershipSubjectContainer privilegeMembershipSubjectContainer = privilegeGroupIdToMembershipSubjectContainer.get(group.getId());
        if (privilegeMembershipSubjectContainer != null) {
          for (String fieldName : GrouperUtil.nonNull(privilegeMembershipSubjectContainer.getMembershipContainers()).keySet()) {
            MembershipContainer membershipContainer = privilegeMembershipSubjectContainer.getMembershipContainers().get(fieldName);
            // lets get these privs in the original membership subject container
            membershipSubjectContainer.addMembership(fieldName, membershipContainer.getMembershipAssignType());
          }
        }
      }
            
      MembershipSubjectContainer.considerAccessPrivilegeInheritance(results);
      
      myGroupsContainer.setGuiMembershipSubjectContainers(GuiMembershipSubjectContainer.convertFromMembershipSubjectContainers(results));
      
      if (GrouperUtil.length(results) == 0) {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
          TextContainer.retrieveFromRequest().getText().get("myGroupsNoResultsFound")));
      }
      
      guiPaging.setTotalRecordCount(queryOptions.getQueryPaging().getTotalRecordCount());
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#myGroupsResultsId", 
          "/WEB-INF/grouperUi2/myGroups/myGroupsMembershipsContents.jsp"));
        
  }

  /**
   * my groups memberships reset button
   * @param request
   * @param response
   */
  public void myGroupsMembershipsReset(HttpServletRequest request, HttpServletResponse response) {
  
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
  
    GrouperSession grouperSession = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
      //clear out form
      guiResponseJs.addAction(GuiScreenAction.newFormFieldValue("myGroupsFilter", ""));
      
      //get the unfiltered groups
      myGroupsMembershipsHelper(request, response);
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * my groups membership
   * @param request
   * @param response
   */
  public void myGroupsMembershipsSubmit(HttpServletRequest request, HttpServletResponse response) {
  
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
  
    GrouperSession grouperSession = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      myGroupsMembershipsHelper(request, response);
  
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

}
