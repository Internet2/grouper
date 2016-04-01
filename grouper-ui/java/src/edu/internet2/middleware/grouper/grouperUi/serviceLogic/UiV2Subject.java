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

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.FieldFinder;
import edu.internet2.middleware.grouper.FieldType;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.GrouperSourceAdapter;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.MembershipFinder;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefFinder;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiAttributeDef;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiGroup;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiMembershipSubjectContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiRuleDefinition;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiSubject;
import edu.internet2.middleware.grouper.grouperUi.beans.dojo.DojoComboLogic;
import edu.internet2.middleware.grouper.grouperUi.beans.dojo.DojoComboQueryLogicBase;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiPaging;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiResponseJs;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction.GuiMessageType;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.AttributeDefContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GrouperRequestContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.RulesContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.SubjectContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.TextContainer;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.internal.dao.QuerySort;
import edu.internet2.middleware.grouper.membership.MembershipSubjectContainer;
import edu.internet2.middleware.grouper.membership.MembershipType;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.AttributeDefPrivilege;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
import edu.internet2.middleware.grouper.privs.Privilege;
import edu.internet2.middleware.grouper.rules.RuleDefinition;
import edu.internet2.middleware.grouper.rules.RuleFinder;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.ui.tags.GrouperPagingTag2;
import edu.internet2.middleware.grouper.ui.util.GrouperUiConfig;
import edu.internet2.middleware.grouper.ui.util.GrouperUiUserData;
import edu.internet2.middleware.grouper.ui.util.GrouperUiUtils;
import edu.internet2.middleware.grouper.userData.GrouperUserDataApi;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * logic involving subjects
 * @author mchyzer
 *
 */
public class UiV2Subject {

  /** logger */
  protected static final Log LOG = LogFactory.getLog(UiV2Subject.class);
  
  /**
   * this subjects privileges inherited from folders
   * @param request
   * @param response
   */
  public void thisSubjectsPrivilegesInheritedFromFolders(HttpServletRequest request, HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    Subject subject = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      subject = retrieveSubjectHelper(request);
      
      if (subject == null) {
        return;
      }

      if (!GrouperRequestContainer.retrieveFromRequestOrCreate().getRulesContainer().isCanReadPrivilegeInheritance()) {
        throw new RuntimeException("Not allowed to read privilege inheritance! " + GrouperUtil.subjectToString(loggedInSubject));
      }
 
      //if viewing a subject, and that subject is a group, just show the group screen
      if (GrouperSourceAdapter.groupSourceId().equals(subject.getSourceId())) {
        //hmmm, should we change to the group url?  i guess not... hmmm
        new UiV2Group().inheritedPrivilegesAssignedToThisGroupFromFolders(request, response);
        return;
      }

      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
      RulesContainer rulesContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getRulesContainer();
      
      Set<GuiRuleDefinition> guiRuleDefinitions = new TreeSet<GuiRuleDefinition>();
      {
        Set<RuleDefinition> groupRuleDefinitions  = RuleFinder.findSubjectPrivilegeInheritRules(subject, true);
        for (RuleDefinition ruleDefinition : GrouperUtil.nonNull(groupRuleDefinitions)) {
          guiRuleDefinitions.add(new GuiRuleDefinition(ruleDefinition));
        }
      }
      
      rulesContainer.setGuiRuleDefinitions(guiRuleDefinitions);

      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/subject/thisSubjectsInheritedPrivilegesInvolvement.jsp"));
  
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * search results for add this subject to a stem
   * @param request
   * @param response
   */
  public void addStemSearch(HttpServletRequest request, HttpServletResponse response) {
    new UiV2Stem().stemSearchFormSubmit(request, response);
  }

  /**
   * combobox results for add this subject to a stem
   * @param request
   * @param response
   */
  public void addToStemFilter(HttpServletRequest request, HttpServletResponse response) {
    new UiV2Stem().stemCopyParentFolderFilter(request, response);
  }

  /**
   * modal search form results for add this subject to a attributeDef
   * @param request
   * @param response
   */
  public void addAttributeDefSearch(HttpServletRequest request, HttpServletResponse response) {
    
    GrouperRequestContainer grouperRequestContainer = GrouperRequestContainer.retrieveFromRequestOrCreate();
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
    GrouperSession grouperSession = null;
    
    try {
      grouperSession = GrouperSession.start(loggedInSubject);
  
      Subject subject = retrieveSubjectHelper(request);
  
      if (subject == null) {
        return;
      }
  
      String searchString = request.getParameter("addAttributeDefSubjectSearch");
      
      boolean searchOk = GrouperUiUtils.searchStringValid(searchString);
      if (!searchOk) {
        
        guiResponseJs.addAction(GuiScreenAction.newInnerHtml("#addAttributeDefResults", 
            TextContainer.retrieveFromRequest().getText().get("subjectViewAddToAttributeDefNotEnoughChars")));
        return;
      }

      AttributeDefContainer attributeDefContainer = grouperRequestContainer.getAttributeDefContainer();
      
      GuiPaging guiPaging = attributeDefContainer.getGuiPaging();
      QueryOptions queryOptions = new QueryOptions();

      GrouperPagingTag2.processRequest(request, guiPaging, queryOptions); 

      Set<AttributeDef> attributeDefs = null;
    
    
      AttributeDefFinder attributeDefFinder = new AttributeDefFinder()
        .assignSubject(loggedInSubject)
        .assignPrivileges(AccessPrivilege.UPDATE_PRIVILEGES)
        .assignScope(searchString)
        .assignSplitScope(true).assignQueryOptions(queryOptions);
      
      attributeDefs = attributeDefFinder.findAttributes();
      
      guiPaging.setTotalRecordCount(queryOptions.getQueryPaging().getTotalRecordCount());
      
      if (GrouperUtil.length(attributeDefs) == 0) {

        guiResponseJs.addAction(GuiScreenAction.newInnerHtml("#addAttributeDefResults", 
            TextContainer.retrieveFromRequest().getText().get("subjectViewAddMemberNoSubjectsFound")));
        return;
      }
      
      Set<GuiAttributeDef> guiAttributeDefs = GuiAttributeDef.convertFromAttributeDefs(attributeDefs);
      
      attributeDefContainer.setGuiAttributeDefSearchResults(guiAttributeDefs);
  
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#addAttributeDefResults", 
          "/WEB-INF/grouperUi2/subject/addAttributeDefResults.jsp"));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }
  
  /**
   * modal search form results for add this subject to a group
   * @param request
   * @param response
   */
  public void addGroupSearch(HttpServletRequest request, HttpServletResponse response) {
    
    GrouperRequestContainer grouperRequestContainer = GrouperRequestContainer.retrieveFromRequestOrCreate();
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
    GrouperSession grouperSession = null;
    
    try {
      grouperSession = GrouperSession.start(loggedInSubject);
  
      Subject subject = retrieveSubjectHelper(request);
  
      if (subject == null) {
        return;
      }
  
      SubjectContainer subjectContainer = grouperRequestContainer.getSubjectContainer();
  
      String searchString = request.getParameter("addGroupSubjectSearch");
      
      boolean searchOk = GrouperUiUtils.searchStringValid(searchString);
      if (!searchOk) {
        
        guiResponseJs.addAction(GuiScreenAction.newInnerHtml("#addGroupResults", 
            TextContainer.retrieveFromRequest().getText().get("subjectViewAddToGroupNotEnoughChars")));
        return;
      }

      String matchExactIdString = request.getParameter("matchExactId[]");
      boolean matchExactId = GrouperUtil.booleanValue(matchExactIdString, false);

      GuiPaging guiPaging = subjectContainer.getGuiPagingSearchGroupResults();
      QueryOptions queryOptions = new QueryOptions();

      GrouperPagingTag2.processRequest(request, guiPaging, queryOptions); 

      Set<Group> groups = null;
    
    
      GroupFinder groupFinder = new GroupFinder().assignPrivileges(AccessPrivilege.UPDATE_PRIVILEGES)
        .assignScope(searchString).assignCompositeOwner(false)
        .assignSplitScope(true).assignQueryOptions(queryOptions);
      
      if (matchExactId) {
        groupFinder.assignFindByUuidOrName(true);
      }
      
      groups = groupFinder.findGroups();
      
      guiPaging.setTotalRecordCount(queryOptions.getQueryPaging().getTotalRecordCount());
      
      if (GrouperUtil.length(groups) == 0) {

        guiResponseJs.addAction(GuiScreenAction.newInnerHtml("#addGroupResults", 
            TextContainer.retrieveFromRequest().getText().get("subjectViewAddMemberNoSubjectsFound")));
        return;
      }
      
      Set<GuiGroup> guiGroups = GuiGroup.convertFromGroups(groups);
      
      subjectContainer.setGuiGroupsAddMember(guiGroups);
  
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#addGroupResults", 
          "/WEB-INF/grouperUi2/subject/addGroupResults.jsp"));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }
  
  /**
   * combobox results for add this subject to a stem
   * @param request
   * @param response
   */
  public void addToAttributeDefFilter(HttpServletRequest request, HttpServletResponse response) {

    //run the combo logic
    DojoComboLogic.logic(request, response, new DojoComboQueryLogicBase<AttributeDef>() {

      /**
       * 
       */
      @Override
      public AttributeDef lookup(HttpServletRequest request, GrouperSession grouperSession, String query) {
        Subject loggedInSubject = grouperSession.getSubject();
        
        AttributeDef theAttributeDef = new AttributeDefFinder()
          .assignPrivileges(AttributeDefPrivilege.ATTR_ADMIN_PRIVILEGES).assignSubject(loggedInSubject)
            .assignFindByUuidOrName(true).assignScope(query).findAttribute();
        return theAttributeDef;
      }

      /**
       * 
       */
      @Override
      public Collection<AttributeDef> search(HttpServletRequest request, GrouperSession grouperSession, String query) {

        Subject loggedInSubject = grouperSession.getSubject();
        int attributeDefComboSize = GrouperUiConfig.retrieveConfig().propertyValueInt("uiV2.attributeDefComboboxResultSize", 200);
        QueryOptions queryOptions = QueryOptions.create(null, null, 1, attributeDefComboSize);
        AttributeDefFinder attributeDefFinder = new AttributeDefFinder();

        return attributeDefFinder.addPrivilege(NamingPrivilege.STEM_ADMIN).assignScope(query).assignSubject(loggedInSubject)
            .assignSplitScope(true).assignQueryOptions(queryOptions).findAttributes();
      }

      /**
       * 
       * @param t
       * @return
       */
      @Override
      public String retrieveId(GrouperSession grouperSession, AttributeDef t) {
        return t.getId();
      }
      
      /**
       * 
       */
      @Override
      public String retrieveLabel(GrouperSession grouperSession, AttributeDef t) {
        return t.getDisplayName();
      }

      /**
       * 
       */
      @Override
      public String retrieveHtmlLabel(GrouperSession grouperSession, AttributeDef t) {
        //description could be null?
        String displayName = t.getDisplayName();
        String label = GrouperUiUtils.escapeHtml(displayName, true);
        String htmlLabel = "<img src=\"../../grouperExternal/public/assets/images/cog.png\" /> " + label;
        return htmlLabel;
      }
    });

  }
  
  /**
   * combobox results for add this subject to a group
   * @param request
   * @param response
   */
  public void addToGroupFilter(HttpServletRequest request, HttpServletResponse response) {
    new UiV2Group().groupUpdateFilter(request, response);
  }
  
  /**
   * get the subject from the request
   * @param request
   * @return the subject or null if not found
   */
  private static Subject retrieveSubjectHelper(HttpServletRequest request) {
    return retrieveSubjectHelper(request, true);
  }

  /**
   * get the subject from the request
   * @param request
   * @return the subject or null if not found
   */
  public static Subject retrieveSubjectHelper(HttpServletRequest request, boolean displayErrorIfProblem) {
  
    //initialize the bean
    GrouperRequestContainer grouperRequestContainer = GrouperRequestContainer.retrieveFromRequestOrCreate();
    
    SubjectContainer subjectContainer = grouperRequestContainer.getSubjectContainer();

    Subject subject = null;
  
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();

    String sourceId = request.getParameter("sourceId");
    String subjectId = request.getParameter("subjectId");
    String subjectIdentifier = request.getParameter("subjectIdentifier");
    String subjectIdOrIdentifier = request.getParameter("subjectIdOrIdentifier");
    String memberId = request.getParameter("memberId");
    
    boolean addedError = false;
    
    if (StringUtils.isBlank(subjectId) && StringUtils.isBlank(subjectIdentifier)
        && StringUtils.isBlank(subjectIdOrIdentifier) && StringUtils.isBlank(memberId)) {
      if (!displayErrorIfProblem) {
        return null;
      }
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
          TextContainer.retrieveFromRequest().getText().get("subjectCantFindSubjectId")));
      addedError = true;
    }
    
    SubjectFinder subjectFinder = addedError ? null : new SubjectFinder().assignSourceId(sourceId)
        .assignSubjectId(subjectId).assignSubjectIdentifier(subjectIdentifier)
        .assignSubjectIdOrIdentifier(subjectIdOrIdentifier).assignMemberId(memberId);

    subject = subjectFinder.findSubject();
    
    if (subject != null) {
      subjectContainer.setGuiSubject(new GuiSubject(subject));      

    } else {
      
      if (!addedError) {
        if (!displayErrorIfProblem) {
          return null;
        }
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("subjectCantFindSubject")));
        addedError = true;
      }
      
    }
  
    //go back to the main screen, cant find group
    if (addedError) {
      if (displayErrorIfProblem) {
        guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
            "/WEB-INF/grouperUi2/index/indexMain.jsp"));
      }
    }

    return subject;
  }

  /**
   * view subject
   * @param request
   * @param response
   */
  public void viewSubject(HttpServletRequest request, HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;

    Subject subject = null;

    try {

      grouperSession = GrouperSession.start(loggedInSubject);

      subject = retrieveSubjectHelper(request);

      if (subject == null) {
        return;
      }

      //if viewing a subject, and that subject is a group, just show the group screen
      if (GrouperSourceAdapter.groupSourceId().equals(subject.getSourceId())) {
        //hmmm, should we change to the group url?  i guess not... hmmm
        new UiV2Group().viewGroup(request, response);
        return;
      }
      
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/subject/viewSubject.jsp"));

      filterHelper(request, response, subject);
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }

  /**
   * the filter button was pressed, or paging or sorting, or view Subject or something
   * @param request
   * @param response
   * @param subject
   */
  private void filterHelper(HttpServletRequest request, HttpServletResponse response, Subject subject) {
    
    GrouperRequestContainer grouperRequestContainer = GrouperRequestContainer.retrieveFromRequestOrCreate();
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    String filterText = request.getParameter("filterText");
    SubjectContainer subjectContainer = grouperRequestContainer.getSubjectContainer();
    
    //if filtering by subjects that have a certain type
    String membershipTypeString = request.getParameter("membershipType");
    MembershipType membershipType = null;
    if (!StringUtils.isBlank(membershipTypeString)) {
      membershipType = MembershipType.valueOfIgnoreCase(membershipTypeString, true);
    }
  
    GuiPaging guiPaging = subjectContainer.getGuiPaging();
    QueryOptions queryOptions = new QueryOptions();

    GrouperPagingTag2.processRequest(request, guiPaging, queryOptions); 

    MembershipFinder membershipFinder = new MembershipFinder()
      .addSubject(subject).assignCheckSecurity(true)
      .assignHasFieldForGroup(true)
      .assignEnabled(true)
      .assignHasMembershipTypeForGroup(true)
      .assignQueryOptionsForGroup(queryOptions)
      .assignSplitScopeForGroup(true);

    if (membershipType != null) {
      membershipFinder.assignMembershipType(membershipType);
    }
  
    if (!StringUtils.isBlank(filterText)) {
      membershipFinder.assignScopeForGroup(filterText);
    }
  
    //set of subjects, and what memberships each subject has
    Set<MembershipSubjectContainer> results = membershipFinder
        .findMembershipResult().getMembershipSubjectContainers();

    subjectContainer.setGuiMembershipSubjectContainers(GuiMembershipSubjectContainer.convertFromMembershipSubjectContainers(results));

    guiPaging.setTotalRecordCount(queryOptions.getQueryPaging().getTotalRecordCount());

    guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#subjectFilterResultsId", 
        "/WEB-INF/grouperUi2/subject/subjectContents.jsp"));

  }

  /**
   * 
   * @param request
   * @param response
   */
  public void addToMyFavorites(HttpServletRequest request, HttpServletResponse response) {
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    Subject subject = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      subject = retrieveSubjectHelper(request);
      
      if (subject == null) {
        return;
      }
  
      GrouperUserDataApi.favoriteMemberAdd(GrouperUiUserData.grouperUiGroupNameForUserData(), 
          loggedInSubject, subject);
  
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
          TextContainer.retrieveFromRequest().getText().get("subjectSuccessAddedToMyFavorites")));
  
      //redisplay so the button will change
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#subjectMoreActionsButtonContentsDivId", 
          "/WEB-INF/grouperUi2/subject/subjectMoreActionsButtonContents.jsp"));
  
      GrouperUserDataApi.recentlyUsedMemberAdd(GrouperUiUserData.grouperUiGroupNameForUserData(), 
          loggedInSubject, subject);

    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  
  }

  /**
   * ajax logic to remove from my favorites
   * @param request
   * @param response
   */
  public void removeFromMyFavorites(HttpServletRequest request, HttpServletResponse response) {
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    Subject subject = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      subject = retrieveSubjectHelper(request);
      
      if (subject == null) {
        return;
      }
  
      GrouperUserDataApi.favoriteMemberRemove(GrouperUiUserData.grouperUiGroupNameForUserData(), loggedInSubject, subject);
  
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
          TextContainer.retrieveFromRequest().getText().get("subjectSuccessRemovedFromMyFavorites")));
  
      //redisplay so the button will change
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#subjectMoreActionsButtonContentsDivId", 
          "/WEB-INF/grouperUi2/subject/subjectMoreActionsButtonContents.jsp"));

      GrouperUserDataApi.recentlyUsedMemberAdd(GrouperUiUserData.grouperUiGroupNameForUserData(), 
          loggedInSubject, subject);

    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  
  }

  /**
   * the filter button was pressed, or paging or sorting or something
   * @param request
   * @param response
   */
  public void filter(HttpServletRequest request, HttpServletResponse response) {
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
        
    GrouperSession grouperSession = null;
    
    try {
      grouperSession = GrouperSession.start(loggedInSubject);
  
      Subject subject = retrieveSubjectHelper(request);
  
      if (subject == null) {
        return;
      }
  
      filterHelper(request, response, subject);
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }

  /**
   * remove subject from this one group
   * @param request
   * @param response
   */
  public void removeGroup(HttpServletRequest request, HttpServletResponse response) {

    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();

    GrouperSession grouperSession = null;
  
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
    try {
      grouperSession = GrouperSession.start(loggedInSubject);
  
      final Subject subject = retrieveSubjectHelper(request);
  
      if (subject == null) {
        return;
      }
  
      final String groupId = request.getParameter("groupId");
      
      Group group = (Group)GrouperSession.callbackGrouperSession(GrouperSession.staticGrouperSession().internal_getRootSession(), 
          new GrouperSessionHandler() {
        
        @Override
        public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
          Group group = GroupFinder.findByUuid(grouperSession, groupId, false);
          if (group.hasUpdate(loggedInSubject)) {
            return group;
          }
          return null;
        }
      });
          
      //not sure why this would happen
      if (group == null) {
        
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("subjectDeleteGroupCantFindGroup")));

      } else {

        boolean madeChanges = group.deleteMember(subject, false);

        if (madeChanges) {
    
          guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
              TextContainer.retrieveFromRequest().getText().get("groupDeleteMemberSuccess")));
              
        } else {
          
          //not sure why this would happen (race condition?)
          guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.info, 
              TextContainer.retrieveFromRequest().getText().get("groupDeleteMemberNoChangesSuccess")));
    
        }
      }
      
      filterHelper(request, response, subject);

      GrouperUserDataApi.recentlyUsedMemberAdd(GrouperUiUserData.grouperUiGroupNameForUserData(), 
          loggedInSubject, subject);

    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * the remove groups button was pressed
   * @param request
   * @param response
   */
  public void removeGroups(HttpServletRequest request, HttpServletResponse response) {

    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();

    GrouperSession grouperSession = null;

    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();

    try {
      grouperSession = GrouperSession.start(loggedInSubject);
  
      final Subject subject = retrieveSubjectHelper(request);
  
      if (subject == null) {
        return;
      }
  
      Set<String> membershipsIds = new HashSet<String>();
      
      for (int i=0;i<1000;i++) {
        String membershipId = request.getParameter("membershipRow_" + i + "[]");
        if (!StringUtils.isBlank(membershipId)) {
          membershipsIds.add(membershipId);
        }
      }
  
      if (membershipsIds.size() == 0) {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("subjectMembershipsRemoveNoGroupSelects")));
        return;
      }
      int successes = 0;
      int failures = 0;
      
      for (String membershipId : membershipsIds) {
        try {
          final Membership membership = new MembershipFinder().addMembershipId(membershipId).findMembership(true);
  
          final Group group = membership.getOwnerGroup();

          boolean allowed = (Boolean)GrouperSession.callbackGrouperSession(GrouperSession.staticGrouperSession().internal_getRootSession(), 
              new GrouperSessionHandler() {
            
            @Override
            public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
              if (group.hasUpdate(loggedInSubject)) {
                return true;
              }
              return false;
            }
          });
          
          if (!allowed) {
            failures++;
          } else {
            group.deleteMember(membership.getMember(), false);
          }
          
          successes++;
        } catch (Exception e) {
          LOG.warn("Error with membership: " + membershipId + ", user: " + loggedInSubject, e);
          failures++;
        }
      }
  
      GrouperRequestContainer.retrieveFromRequestOrCreate().getGroupContainer().setSuccessCount(successes);
      GrouperRequestContainer.retrieveFromRequestOrCreate().getGroupContainer().setFailureCount(failures);
  
      if (failures > 0) {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("groupDeleteMembersErrors")));
      } else {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
            TextContainer.retrieveFromRequest().getText().get("groupDeleteMembersSuccesses")));
      }
      
      filterHelper(request, response, subject);
  
      GrouperUserDataApi.recentlyUsedMemberAdd(GrouperUiUserData.grouperUiGroupNameForUserData(), 
          loggedInSubject, subject);

    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
  /**
   * submit button on add stem form pressed
   * @param request
   * @param response
   */
  public void addStemSubmit(final HttpServletRequest request, final HttpServletResponse response) {
  
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
  
    GrouperSession grouperSession = null;
  
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
    try {
      grouperSession = GrouperSession.start(loggedInSubject);
      
      Subject subject = retrieveSubjectHelper(request);
  
      if (subject == null) {
        return;
      }
    
      String tempStemString = request.getParameter("parentFolderComboName");
  
      //just get what they typed in
      if (StringUtils.isBlank(tempStemString)) {
        tempStemString = request.getParameter("parentFolderComboNameDisplay");
      }
      
      final String stemString = tempStemString;

      Stem stem = (Stem)GrouperSession.callbackGrouperSession(grouperSession.internal_getRootSession(), new GrouperSessionHandler() {
        
        @Override
        public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
          Stem theStem = null;
          
          if (!StringUtils.isBlank(stemString)) {
            theStem = new StemFinder().assignScope(stemString).assignFindByUuidOrName(true)
                .assignSubject(loggedInSubject)
                .assignPrivileges(NamingPrivilege.CREATE_PRIVILEGES).findStem();
          }
          return theStem;
        }
      });
        
      if (stem == null) {
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, "#parentFolderComboErrorId", 
            TextContainer.retrieveFromRequest().getText().get("subjectAddMemberCantFindStem")));
        return;
      }      
  
      boolean stemAdminChecked = GrouperUtil.booleanValue(request.getParameter("privileges_stemAdmins[]"), false);
      boolean createChecked = GrouperUtil.booleanValue(request.getParameter("privileges_creators[]"), false);
      boolean attrReadChecked = GrouperUtil.booleanValue(request.getParameter("privileges_stemAttrReaders[]"), false);
      boolean attrUpdateChecked = GrouperUtil.booleanValue(request.getParameter("privileges_stemAttrUpdaters[]"), false);

      if (!stemAdminChecked && !createChecked && !attrReadChecked && !attrUpdateChecked) {
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, "#stemPrivsErrorId", 
            TextContainer.retrieveFromRequest().getText().get("subjectAddMemberStemPrivRequired")));
        return;
      }
      
      boolean madeChanges = stem.grantPrivs(subject, stemAdminChecked, createChecked, attrReadChecked, 
          attrUpdateChecked, false);
      
      if (madeChanges) {
  
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
            TextContainer.retrieveFromRequest().getText().get("stemAddMemberMadeChangesSuccess")));
  
        filterThisSubjectsStemPrivilegesHelper(request, response, subject);
  
  
      } else {
  
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.info, 
            TextContainer.retrieveFromRequest().getText().get("stemAddMemberNoChangesSuccess")));

      }

      //clear out the combo
      guiResponseJs.addAction(GuiScreenAction.newScript(
          "dijit.byId('parentFolderComboId').set('displayedValue', ''); " +
          "dijit.byId('parentFolderComboId').set('value', '');"));

      GrouperUserDataApi.recentlyUsedStemAdd(GrouperUiUserData.grouperUiGroupNameForUserData(), 
          loggedInSubject, stem);
      GrouperUserDataApi.recentlyUsedMemberAdd(GrouperUiUserData.grouperUiGroupNameForUserData(), 
          loggedInSubject, subject);

    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }

  }

  /**
   * submit button on add group form pressed
   * @param request
   * @param response
   */
  public void addGroupSubmit(final HttpServletRequest request, final HttpServletResponse response) {
  
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
  
    GrouperSession grouperSession = null;
  
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
    try {
      grouperSession = GrouperSession.start(loggedInSubject);
      
      final Subject subject = retrieveSubjectHelper(request);
  
      if (subject == null) {
        return;
      }
    
      String tempGroupString = request.getParameter("groupAddMemberComboName");
  
      //just get what they typed in
      if (StringUtils.isBlank(tempGroupString)) {
        tempGroupString = request.getParameter("groupAddMemberComboNameDisplay");
      }
      final String groupString = tempGroupString;
      final boolean[] userHasAdmin = new boolean[]{false};
      Group group = (Group)GrouperSession.callbackGrouperSession(grouperSession.internal_getRootSession(), new GrouperSessionHandler() {
        
        @Override
        public Object callback(GrouperSession theGrouperSession) throws GrouperSessionException {
          Group theGroup = null;
          
          if (!StringUtils.isBlank(groupString)) {
            theGroup = new GroupFinder().assignScope(groupString).assignFindByUuidOrName(true)
                .assignPrivileges(AccessPrivilege.UPDATE_PRIVILEGES).assignSubject(loggedInSubject).findGroup();
            if (theGroup != null) {
              userHasAdmin[0] = theGroup.canHavePrivilege(loggedInSubject, AccessPrivilege.ADMIN.getName(), false);
            }
          }
          return theGroup;
        }
      });
        
      if (group == null) {
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error,
            "#groupAddMemberComboErrorId",
            TextContainer.retrieveFromRequest().getText().get("subjectAddMemberCantFindGroup")));
        return;
      }      
  
      Boolean defaultPrivs = null;
      
      {
        String privilegeOptionsValue = request.getParameter("privilege-options[]");
        
        if (StringUtils.equals(privilegeOptionsValue, "default")) {
          defaultPrivs = true;
        } else if (StringUtils.equals(privilegeOptionsValue, "custom")) {
          defaultPrivs = false;
        } else {
          throw new RuntimeException("For privilege-options expecting default or custom but was: '" + privilegeOptionsValue + "'");
        }
      }
      
      boolean memberChecked = GrouperUtil.booleanValue(request.getParameter("privileges_members[]"), false);
      boolean adminChecked = GrouperUtil.booleanValue(request.getParameter("privileges_admins[]"), false);
      boolean updateChecked = GrouperUtil.booleanValue(request.getParameter("privileges_updaters[]"), false);
      boolean readChecked = GrouperUtil.booleanValue(request.getParameter("privileges_readers[]"), false);
      boolean viewChecked = GrouperUtil.booleanValue(request.getParameter("privileges_viewers[]"), false);
      boolean optinChecked = GrouperUtil.booleanValue(request.getParameter("privileges_optins[]"), false);
      boolean optoutChecked = GrouperUtil.booleanValue(request.getParameter("privileges_optouts[]"), false);
      boolean attrReadChecked = GrouperUtil.booleanValue(request.getParameter("privileges_groupAttrReaders[]"), false);
      boolean attrUpdateChecked = GrouperUtil.booleanValue(request.getParameter("privileges_groupAttrUpdaters[]"), false);

      if (!defaultPrivs && !memberChecked && !adminChecked && !updateChecked && !readChecked
          && !viewChecked && !optinChecked && !optoutChecked && !attrReadChecked && !attrUpdateChecked) {
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error,
            "#groupPrivsErrorId",
            TextContainer.retrieveFromRequest().getText().get("subjectAddMemberPrivRequired")));
        return;
        
      }

      //if any privs are checked, then the user must have ADMIN on the group
      if (!userHasAdmin[0] && (adminChecked || updateChecked || readChecked || viewChecked || optinChecked || optoutChecked || attrReadChecked || attrUpdateChecked)) {
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error,
            "#groupPrivsErrorId",
            TextContainer.retrieveFromRequest().getText().get("subjectAddMemberNotAllowedToAssignPrivs")));
        return;
        
      }
      
      boolean madeChanges = group.addOrEditMember(subject, defaultPrivs, memberChecked, adminChecked, 
          updateChecked, readChecked, viewChecked, optinChecked, optoutChecked, attrReadChecked, 
          attrUpdateChecked, null, null, false);
      
      if (madeChanges) {
  
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
            TextContainer.retrieveFromRequest().getText().get("groupAddMemberMadeChangesSuccess")));
  
        
        //what subscreen are we on?
        String groupRefreshPart = request.getParameter("subjectRefreshPart");
        if (StringUtils.equals(groupRefreshPart, "thisSubjectsGroupPrivileges")) {
          filterThisSubjectsGroupPrivilegesHelper(request, response, subject);
        } else {
          filterHelper(request, response, subject);
        }

        
  
  
      } else {
  
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.info, 
            TextContainer.retrieveFromRequest().getText().get("groupAddMemberNoChangesSuccess")));
  
      }

      //clear out the combo
      guiResponseJs.addAction(GuiScreenAction.newScript(
          "dijit.byId('groupAddMemberComboId').set('displayedValue', ''); " +
          "dijit.byId('groupAddMemberComboId').set('value', '');"));
  
      GrouperUserDataApi.recentlyUsedGroupAdd(GrouperUiUserData.grouperUiGroupNameForUserData(), 
          loggedInSubject, group);
      GrouperUserDataApi.recentlyUsedMemberAdd(GrouperUiUserData.grouperUiGroupNameForUserData(), 
          loggedInSubject, subject);

    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  
  }

  /**
   * the filter button for this subjects privileges was pressed, or paging or sorting or something
   * @param request
   * @param response
   */
  public void filterThisSubjectsGroupPrivileges(HttpServletRequest request, HttpServletResponse response) {
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    try {
      grouperSession = GrouperSession.start(loggedInSubject);
  
  
      Subject subject = retrieveSubjectHelper(request);
      
      if (subject == null) {
        return;
      }
  
      filterThisSubjectsGroupPrivilegesHelper(request, response, subject);
  
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  
  }

  /**
   * the filter button was pressed of this subjects group privileges screen, 
   * or paging or sorting, or view Group or something
   * @param request
   * @param response
   */
  private void filterThisSubjectsGroupPrivilegesHelper(HttpServletRequest request, HttpServletResponse response, Subject subject) {
  
    GrouperRequestContainer grouperRequestContainer = GrouperRequestContainer.retrieveFromRequestOrCreate();
  
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
    //if filtering text in subjects
    String privilegeFilterText = request.getParameter("privilegeFilterText");
    
    String privilegeFieldName = request.getParameter("privilegeField");
    
    Field privilegeField = null;
    if (!StringUtils.isBlank(privilegeFieldName)) {
      privilegeField = FieldFinder.find(privilegeFieldName, true);
    }
    
    //if filtering by subjects that have a certain type
    String membershipTypeString = request.getParameter("privilegeMembershipType");
    MembershipType membershipType = null;
    if (!StringUtils.isBlank(membershipTypeString)) {
      membershipType = MembershipType.valueOfIgnoreCase(membershipTypeString, true);
    }
    
    GuiPaging guiPaging = grouperRequestContainer.getSubjectContainer().getPrivilegeGuiPaging();

    QueryOptions queryOptions = new QueryOptions();

    QuerySort querySort = new QuerySort("g.displayNameDb", true);
    querySort.insertSortToBeginning("g.displayExtensionDb", true);
    queryOptions.sort(querySort);
    
    GrouperPagingTag2.processRequest(request, guiPaging, queryOptions); 
    
    MembershipFinder membershipFinder = new MembershipFinder()
      .addSubject(subject).assignCheckSecurity(true)
      .assignFieldType(FieldType.ACCESS)
      .assignEnabled(true)
      .assignHasFieldForGroup(true)
      .assignHasMembershipTypeForGroup(true)
      .assignQueryOptionsForGroup(queryOptions)
      .assignSplitScopeForGroup(true);
    
    if (membershipType != null) {
      membershipFinder.assignMembershipType(membershipType);
    }
  
    if (privilegeField != null) {
      membershipFinder.assignField(privilegeField);
      membershipFinder.assignIncludeInheritedPrivileges(true);
    }
  
    if (!StringUtils.isBlank(privilegeFilterText)) {
      membershipFinder.assignScopeForGroup(privilegeFilterText);
    }
  
    //set of subjects, and what privs each subject has
    Set<MembershipSubjectContainer> results = membershipFinder
        .findMembershipResult().getMembershipSubjectContainers();
  
    MembershipSubjectContainer.considerAccessPrivilegeInheritance(results);
  
    grouperRequestContainer.getSubjectContainer().setPrivilegeGuiMembershipSubjectContainers(
        GuiMembershipSubjectContainer.convertFromMembershipSubjectContainers(results));
  
    guiPaging.setTotalRecordCount(queryOptions.getQueryPaging().getTotalRecordCount());
  
    guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#thisSubjectsGroupPrivilegesFilterResultsId", 
        "/WEB-INF/grouperUi2/subject/thisSubjectsGroupPrivilegesContents.jsp"));
  
  }

  /**
   * assign or remove a privilege from a subject, on this subjects privileges
   * @param request
   * @param response
   */
  public void thisSubjectsPrivilegesAssignPrivilege(HttpServletRequest request, HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
  
    GrouperSession grouperSession = null;
    
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
    try {
      grouperSession = GrouperSession.start(loggedInSubject);
  
      Subject subject = retrieveSubjectHelper(request);
      
      if (subject == null) {
        return;
      }
  
      //?assign=false&groupId=${grouperRequestContainer.subjectContainer.guiGroup.stem.id}&fieldName=${fieldName}&memberId=${guiMembershipSubjectContainer.guiMember.member.uuid}
      String assignString = request.getParameter("assign");
      boolean assign = GrouperUtil.booleanValue(assignString);
      String fieldName = request.getParameter("fieldName");
      String parentGroupId = request.getParameter("parentGroupId");
  
      Group parentGroup = GroupFinder.findByUuid(grouperSession, parentGroupId, false);
      
      if (parentGroup == null || !parentGroup.canHavePrivilege(loggedInSubject, AccessPrivilege.ADMIN.getName(), false)) {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
            TextContainer.retrieveFromRequest().getText().get("subjectNotAllowedToAdminAnotherGroup")));
        filterThisSubjectsGroupPrivilegesHelper(request, response, subject);
        return;
      }
      
      Privilege privilege = AccessPrivilege.listToPriv(fieldName);
      
      if (privilege == null) {
        throw new RuntimeException("Why is privilege not found???? " + fieldName);
      }
      
      //if someone revoked in the meantime, who cares...
      if (assign) {
        parentGroup.grantPriv(subject, privilege, false);
        
        //set a success message
        //messes up screen
        //guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
        //    TextContainer.retrieveFromRequest().getText().get("stemSuccessGrantedPrivilege")));
        
      } else {
        parentGroup.revokePriv(subject, privilege, false);
        
        //messes up screen
        //set a success message
        //guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
        //    TextContainer.retrieveFromRequest().getText().get("stemSuccessRevokedPrivilege")));
      }
  
      filterThisSubjectsGroupPrivilegesHelper(request, response, subject);
  
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  
  
  }

  /**
   * submit the main form on the this groups privilege screen which can do batch operations on a number of rows
   * @param request
   * @param response
   */
  public void thisSubjectsPrivilegesAssignPrivilegeBatch(HttpServletRequest request, HttpServletResponse response) {
  
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
  
    GrouperSession grouperSession = null;
    
    try {
      grouperSession = GrouperSession.start(loggedInSubject);
  
      Subject subject = retrieveSubjectHelper(request);
  
      if (subject == null) {
        return;
      }
        
      String groupPrivilegeBatchUpdateOperation = request.getParameter("groupPrivilegeBatchUpdateOperation");
      Pattern operationPattern = Pattern.compile("^(assign|revoke)_(.*)$");
      Matcher operationMatcher = operationPattern.matcher(groupPrivilegeBatchUpdateOperation);
      if (!operationMatcher.matches()) {
        throw new RuntimeException("Invalid submission, should have a valid operation: '" + groupPrivilegeBatchUpdateOperation + "'");
      }
      
      String assignOrRevokeString = operationMatcher.group(1);
      boolean assign = StringUtils.equals("assign", assignOrRevokeString);
      if (!assign && !StringUtils.equals("revoke", assignOrRevokeString)) {
        throw new RuntimeException("Cant find assign or revoke: '" + assignOrRevokeString + "'");
      }
      String fieldName = operationMatcher.group(2);
      
      boolean assignAll = StringUtils.equals(fieldName, "all");
      boolean readersUpdaters = StringUtils.equals(fieldName, "readersUpdaters");
      
      //lets see how many are on a page
      int pageSize = GrouperPagingTag2.pageSize(request);
      
      //lets loop and get all the checkboxes
      Set<Group> parentGroups = new LinkedHashSet<Group>();
      
      //loop through all the checkboxes and collect all the groups
      for (int i=0;i<pageSize;i++) {
        String parentGroupId = request.getParameter("privilegeSubjectRow_" + i + "[]");
        if (!StringUtils.isBlank(parentGroupId)) {
          
          Group parentGroup = GroupFinder.findByUuid(grouperSession, parentGroupId, false);
          
          if (parentGroup != null && parentGroup.canHavePrivilege(loggedInSubject, AccessPrivilege.ADMIN.getName(), false)) {
            parentGroups.add(parentGroup);
          }
        }
      }
  
      if (GrouperUtil.length(parentGroups) == 0) {
  
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("thisSubjectsPrivilegesErrorGroupRequired")));
        guiResponseJs.addAction(GuiScreenAction.newScript("guiScrollTop()"));
        filterThisSubjectsGroupPrivilegesHelper(request, response, subject);
        return;
      }
      
      int changes = 0;
      
      Privilege[] privileges = assignAll ? (assign ? new Privilege[]{  
          AccessPrivilege.listToPriv(Field.FIELD_NAME_ADMINS)} : new Privilege[]{  
          AccessPrivilege.listToPriv(Field.FIELD_NAME_ADMINS),
          AccessPrivilege.listToPriv(Field.FIELD_NAME_GROUP_ATTR_READERS),
          AccessPrivilege.listToPriv(Field.FIELD_NAME_GROUP_ATTR_UPDATERS),
          AccessPrivilege.listToPriv(Field.FIELD_NAME_OPTOUTS),
          AccessPrivilege.listToPriv(Field.FIELD_NAME_READERS),
          AccessPrivilege.listToPriv(Field.FIELD_NAME_VIEWERS),
          AccessPrivilege.listToPriv(Field.FIELD_NAME_UPDATERS),
          AccessPrivilege.listToPriv(Field.FIELD_NAME_OPTINS)
          } ) : (readersUpdaters ? new Privilege[]{AccessPrivilege.listToPriv(Field.FIELD_NAME_READERS),
              AccessPrivilege.listToPriv(Field.FIELD_NAME_UPDATERS)
          } : new Privilege[]{AccessPrivilege.listToPriv(fieldName)});
      
      for (Group parentGroup : parentGroups) {
        
        for (Privilege privilege : privileges) {
          if (assign) {
            changes += parentGroup.grantPriv(subject, privilege, false) ? 1 : 0;
          } else {
            changes += parentGroup.revokePriv(subject, privilege, false) ? 1 : 0;
          }
        }
      }
      
      if (changes > 0) {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
            TextContainer.retrieveFromRequest().getText().get(
                assign ? "groupSuccessGrantedPrivileges" : "groupSuccessRevokedPrivileges")));
      } else {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.info, 
            TextContainer.retrieveFromRequest().getText().get(
                assign ? "groupNoteNoGrantedPrivileges" : "groupNoteNoRevokedPrivileges")));
        
      }
      guiResponseJs.addAction(GuiScreenAction.newScript("guiScrollTop()"));
  
      filterThisSubjectsGroupPrivilegesHelper(request, response, subject);
  
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  
  }

  /**
   * this groups group privileges
   * @param request
   * @param response
   */
  public void thisSubjectsGroupPrivileges(HttpServletRequest request, HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    Subject subject = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      subject = retrieveSubjectHelper(request);
      
      if (subject == null) {
        return;
      }

      //if viewing a subject, and that subject is a group, just show the group screen
      if (GrouperSourceAdapter.groupSourceId().equals(subject.getSourceId())) {
        //hmmm, should we change to the group url?  i guess not... hmmm
        new UiV2Group().thisGroupsGroupPrivileges(request, response);
        return;
      }

      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/subject/thisSubjectsGroupPrivileges.jsp"));
      filterThisSubjectsGroupPrivilegesHelper(request, response, subject);
  
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * this subjects attributeDef privileges
   * @param request
   * @param response
   */
  public void thisSubjectsAttributeDefPrivileges(HttpServletRequest request, HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    Subject subject = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      subject = retrieveSubjectHelper(request);
      
      if (subject == null) {
        return;
      }

      //if viewing a subject, and that subject is a group, just show the group screen
      if (GrouperSourceAdapter.groupSourceId().equals(subject.getSourceId())) {
        //hmmm, should we change to the group url?  i guess not... hmmm
        new UiV2Group().thisGroupsAttributeDefPrivileges(request, response);
        return;
      }

      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/subject/thisSubjectsAttributeDefPrivileges.jsp"));
      filterThisSubjectsAttributeDefPrivilegesHelper(request, response, subject);
  
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * assign or remove a privilege from a group, on this groups privileges
   * @param request
   * @param response
   */
  public void thisSubjectsPrivilegesAssignAttributeDefPrivilege(HttpServletRequest request, HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
  
    GrouperSession grouperSession = null;
    
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
    try {
      grouperSession = GrouperSession.start(loggedInSubject);
  
      Subject subject = retrieveSubjectHelper(request);
  
      if (subject == null) {
        return;
      }
  
      //?assign=false&groupId=${grouperRequestContainer.groupContainer.guiGroup.stem.id}&fieldName=${fieldName}&memberId=${guiMembershipSubjectContainer.guiMember.member.uuid}
      String assignString = request.getParameter("assign");
      boolean assign = GrouperUtil.booleanValue(assignString);
      String fieldName = request.getParameter("fieldName");
      String parentAttributeDefId = request.getParameter("parentAttributeDefId");
  
      AttributeDef parentAttributeDef = AttributeDefFinder.findById(parentAttributeDefId, false);
      
      if (parentAttributeDef == null || !parentAttributeDef.getPrivilegeDelegate().canHavePrivilege(loggedInSubject, 
          AttributeDefPrivilege.ATTR_ADMIN.getName(), false)) {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
            TextContainer.retrieveFromRequest().getText().get("subjectNotAllowedToAdminAnotherAttributeDef")));
        filterThisSubjectsAttributeDefPrivilegesHelper(request, response, subject);
        return;
      }
      
      Privilege privilege = AttributeDefPrivilege.listToPriv(fieldName);
      
      if (privilege == null) {
        throw new RuntimeException("Why is privilege not found???? " + fieldName);
      }
      
      //if someone revoked in the meantime, who cares...
      if (assign) {
        parentAttributeDef.getPrivilegeDelegate().grantPriv(subject, privilege, false);
        
        //set a success message
        //messes up screen
        //guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
        //    TextContainer.retrieveFromRequest().getText().get("stemSuccessGrantedPrivilege")));
        
      } else {
        parentAttributeDef.getPrivilegeDelegate().revokePriv(subject, privilege, false);
        
        //messes up screen
        //set a success message
        //guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
        //    TextContainer.retrieveFromRequest().getText().get("stemSuccessRevokedPrivilege")));
      }
  
      filterThisSubjectsAttributeDefPrivilegesHelper(request, response, subject);
  
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  
  
  }

  /**
   * submit the main form on the this subjects privilege screen which can do batch operations on a number of rows
   * @param request
   * @param response
   */
  public void thisSubjectsPrivilegesAssignAttributeDefPrivilegeBatch(HttpServletRequest request, HttpServletResponse response) {
  
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
  
    GrouperSession grouperSession = null;
    
    try {
      grouperSession = GrouperSession.start(loggedInSubject);
  
      Subject subject = retrieveSubjectHelper(request);
  
      if (subject == null) {
        return;
      }
        
      String groupPrivilegeBatchUpdateOperation = request.getParameter("groupPrivilegeBatchUpdateOperation");
      Pattern operationPattern = Pattern.compile("^(assign|revoke)_(.*)$");
      Matcher operationMatcher = operationPattern.matcher(groupPrivilegeBatchUpdateOperation);
      if (!operationMatcher.matches()) {
        throw new RuntimeException("Invalid submission, should have a valid operation: '" + groupPrivilegeBatchUpdateOperation + "'");
      }
      
      String assignOrRevokeString = operationMatcher.group(1);
      boolean assign = StringUtils.equals("assign", assignOrRevokeString);
      if (!assign && !StringUtils.equals("revoke", assignOrRevokeString)) {
        throw new RuntimeException("Cant find assign or revoke: '" + assignOrRevokeString + "'");
      }
      String fieldName = operationMatcher.group(2);
      
      boolean assignAll = StringUtils.equals(fieldName, "all");
      boolean readersUpdaters = StringUtils.equals(fieldName, "readersUpdaters");
      
      //lets see how many are on a page
      int pageSize = GrouperPagingTag2.pageSize(request);
      
      //lets loop and get all the checkboxes
      Set<AttributeDef> parentAttributeDefs = new LinkedHashSet<AttributeDef>();
      
      //loop through all the checkboxes and collect all the groups
      for (int i=0;i<pageSize;i++) {
        String parentAttributeDefId = request.getParameter("privilegeSubjectRow_" + i + "[]");
        if (!StringUtils.isBlank(parentAttributeDefId)) {
          
          AttributeDef parentAttributeDef = AttributeDefFinder.findById(parentAttributeDefId, false);
          
          if (parentAttributeDef != null && parentAttributeDef.getPrivilegeDelegate().canHavePrivilege(loggedInSubject, AttributeDefPrivilege.ATTR_ADMIN.getName(), false)) {
            parentAttributeDefs.add(parentAttributeDef);
          }
        }
      }
  
      if (GrouperUtil.length(parentAttributeDefs) == 0) {
  
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("thisSubjectsPrivilegesErrorAttributeDefRequired")));
        guiResponseJs.addAction(GuiScreenAction.newScript("guiScrollTop()"));
        filterThisSubjectsAttributeDefPrivilegesHelper(request, response, subject);
        return;
      }
      
      int changes = 0;
      
      Privilege[] privileges = assignAll ? (assign ? new Privilege[]{  
          AttributeDefPrivilege.listToPriv(Field.FIELD_NAME_ATTR_ADMINS)} : new Privilege[]{  
            AttributeDefPrivilege.listToPriv(Field.FIELD_NAME_ATTR_ADMINS),
            AttributeDefPrivilege.listToPriv(Field.FIELD_NAME_ATTR_DEF_ATTR_READERS),
            AttributeDefPrivilege.listToPriv(Field.FIELD_NAME_ATTR_DEF_ATTR_UPDATERS),
            AttributeDefPrivilege.listToPriv(Field.FIELD_NAME_ATTR_OPTOUTS),
            AttributeDefPrivilege.listToPriv(Field.FIELD_NAME_ATTR_READERS),
            AttributeDefPrivilege.listToPriv(Field.FIELD_NAME_ATTR_VIEWERS),
            AttributeDefPrivilege.listToPriv(Field.FIELD_NAME_ATTR_UPDATERS),
            AttributeDefPrivilege.listToPriv(Field.FIELD_NAME_ATTR_OPTINS)
          } ) : (readersUpdaters ? new Privilege[]{AttributeDefPrivilege.listToPriv(Field.FIELD_NAME_ATTR_READERS),
              AttributeDefPrivilege.listToPriv(Field.FIELD_NAME_ATTR_UPDATERS)
          } : new Privilege[]{AttributeDefPrivilege.listToPriv(fieldName)});
      
      for (AttributeDef parentAttributeDef : parentAttributeDefs) {
        
        for (Privilege privilege : privileges) {
          if (assign) {
            changes += parentAttributeDef.getPrivilegeDelegate().grantPriv(subject, privilege, false) ? 1 : 0;
          } else {
            changes += parentAttributeDef.getPrivilegeDelegate().revokePriv(subject, privilege, false) ? 1 : 0;
          }
        }
      }
      
      if (changes > 0) {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
            TextContainer.retrieveFromRequest().getText().get(
                assign ? "groupSuccessGrantedPrivileges" : "groupSuccessRevokedPrivileges")));
      } else {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.info, 
            TextContainer.retrieveFromRequest().getText().get(
                assign ? "groupNoteNoGrantedPrivileges" : "groupNoteNoRevokedPrivileges")));
        
      }
      guiResponseJs.addAction(GuiScreenAction.newScript("guiScrollTop()"));
  
      filterThisSubjectsAttributeDefPrivilegesHelper(request, response, subject);
  
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  
  }

  /**
   * assign or remove a privilege from a stem, on this groups privileges
   * @param request
   * @param response
   */
  public void thisSubjectsPrivilegesAssignStemPrivilege(HttpServletRequest request, HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
  
    GrouperSession grouperSession = null;
    
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
    try {
      grouperSession = GrouperSession.start(loggedInSubject);
  
      Subject subject = retrieveSubjectHelper(request);
  
      if (subject == null) {
        return;
      }
  
      //?assign=false&groupId=${grouperRequestContainer.groupContainer.guiGroup.stem.id}&fieldName=${fieldName}&memberId=${guiMembershipSubjectContainer.guiMember.member.uuid}
      String assignString = request.getParameter("assign");
      boolean assign = GrouperUtil.booleanValue(assignString);
      String fieldName = request.getParameter("fieldName");
      String parentStemId = request.getParameter("parentStemId");
  
      Stem parentStem = StemFinder.findByUuid(grouperSession, parentStemId, false);
      
      if (parentStem == null || !parentStem.canHavePrivilege(loggedInSubject, NamingPrivilege.STEM_ADMIN.getName(), false)) {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
            TextContainer.retrieveFromRequest().getText().get("groupNotAllowedToAdminAnotherStem")));
        filterThisSubjectsGroupPrivilegesHelper(request, response, subject);
        return;
      }
      
      Privilege privilege = NamingPrivilege.listToPriv(fieldName);
      
      if (privilege == null) {
        throw new RuntimeException("Why is privilege not found???? " + fieldName);
      }
      
      //if someone revoked in the meantime, who cares...
      if (assign) {
        parentStem.grantPriv(subject, privilege, false);
        
        //set a success message
        //messes up screen
        //guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
        //    TextContainer.retrieveFromRequest().getText().get("stemSuccessGrantedPrivilege")));
        
      } else {
        parentStem.revokePriv(subject, privilege, false);
        
        //messes up screen
        //set a success message
        //guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
        //    TextContainer.retrieveFromRequest().getText().get("stemSuccessRevokedPrivilege")));
      }
  
      filterThisSubjectsStemPrivilegesHelper(request, response, subject);
  
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  
  }

  /**
   * submit the main form on the this groups stem privilege screen which can do batch operations on a number of rows
   * @param request
   * @param response
   */
  public void thisSubjectsPrivilegesAssignStemPrivilegeBatch(HttpServletRequest request, HttpServletResponse response) {
  
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
  
    GrouperSession grouperSession = null;
    
    try {
      grouperSession = GrouperSession.start(loggedInSubject);
  
      Subject subject = retrieveSubjectHelper(request);
  
      if (subject == null) {
        return;
      }
        
      String groupPrivilegeBatchUpdateOperation = request.getParameter("groupPrivilegeBatchUpdateOperation");
      Pattern operationPattern = Pattern.compile("^(assign|revoke)_(.*)$");
      Matcher operationMatcher = operationPattern.matcher(groupPrivilegeBatchUpdateOperation);
      if (!operationMatcher.matches()) {
        throw new RuntimeException("Invalid submission, should have a valid operation: '" + groupPrivilegeBatchUpdateOperation + "'");
      }
      
      String assignOrRevokeString = operationMatcher.group(1);
      boolean assign = StringUtils.equals("assign", assignOrRevokeString);
      if (!assign && !StringUtils.equals("revoke", assignOrRevokeString)) {
        throw new RuntimeException("Cant find assign or revoke: '" + assignOrRevokeString + "'");
      }
      String fieldName = operationMatcher.group(2);
      
      boolean assignAll = StringUtils.equals(fieldName, "all");
      
      //lets see how many are on a page
      int pageSize = GrouperPagingTag2.pageSize(request);
      
      //lets loop and get all the checkboxes
      Set<Stem> parentStems = new LinkedHashSet<Stem>();
      
      //loop through all the checkboxes and collect all the groups
      for (int i=0;i<pageSize;i++) {
        String parentStemId = request.getParameter("privilegeSubjectRow_" + i + "[]");
        if (!StringUtils.isBlank(parentStemId)) {
          
          Stem parentStem = StemFinder.findByUuid(grouperSession, parentStemId, false);
          
          if (parentStem != null && parentStem.canHavePrivilege(loggedInSubject, NamingPrivilege.STEM_ADMIN.getName(), false)) {
            parentStems.add(parentStem);
          }
        }
      }
  
      if (GrouperUtil.length(parentStems) == 0) {
  
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("thisSubjectsPrivilegesErrorStemRequired")));
        guiResponseJs.addAction(GuiScreenAction.newScript("guiScrollTop()"));
        filterThisSubjectsStemPrivilegesHelper(request, response, subject);
        return;
      }
      
      int changes = 0;
      
      Privilege[] privileges = assignAll ? (assign ? new Privilege[]{  
          NamingPrivilege.listToPriv(Field.FIELD_NAME_STEM_ADMINS)} : new Privilege[]{  
            NamingPrivilege.listToPriv(Field.FIELD_NAME_STEM_ADMINS),
            NamingPrivilege.listToPriv(Field.FIELD_NAME_CREATORS),
            NamingPrivilege.listToPriv(Field.FIELD_NAME_STEM_ATTR_READERS),
            NamingPrivilege.listToPriv(Field.FIELD_NAME_STEM_ATTR_UPDATERS)
          } ) : new Privilege[]{NamingPrivilege.listToPriv(fieldName)};
      
      for (Stem parentStem : parentStems) {
        
        for (Privilege privilege : privileges) {
          if (assign) {
            changes += parentStem.grantPriv(subject, privilege, false) ? 1 : 0;
          } else {
            changes += parentStem.revokePriv(subject, privilege, false) ? 1 : 0;
          }
        }
      }
      
      if (changes > 0) {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
            TextContainer.retrieveFromRequest().getText().get(
                assign ? "groupSuccessGrantedPrivileges" : "groupSuccessRevokedPrivileges")));
      } else {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.info, 
            TextContainer.retrieveFromRequest().getText().get(
                assign ? "groupNoteNoGrantedPrivileges" : "groupNoteNoRevokedPrivileges")));
        
      }
      guiResponseJs.addAction(GuiScreenAction.newScript("guiScrollTop()"));
  
      filterThisSubjectsStemPrivilegesHelper(request, response, subject);
  
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  
  }

  /**
   * this groups stem privileges
   * @param request
   * @param response
   */
  public void thisSubjectsStemPrivileges(HttpServletRequest request, HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    Subject subject = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      subject = retrieveSubjectHelper(request);
      
      if (subject == null) {
        return;
      }

      //if viewing a subject, and that subject is a group, just show the group screen
      if (GrouperSourceAdapter.groupSourceId().equals(subject.getSourceId())) {
        //hmmm, should we change to the group url?  i guess not... hmmm
        new UiV2Group().thisGroupsStemPrivileges(request, response);
        return;
      }

      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/subject/thisSubjectsStemPrivileges.jsp"));
      filterThisSubjectsStemPrivilegesHelper(request, response, subject);
  
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * the filter button for this subject attribute def privileges was pressed, or paging or sorting or something
   * @param request
   * @param response
   */
  public void filterThisSubjectsAttributeDefPrivileges(HttpServletRequest request, HttpServletResponse response) {
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    try {
      grouperSession = GrouperSession.start(loggedInSubject);
  
  
      Subject subject = retrieveSubjectHelper(request);
      
      if (subject == null) {
        return;
      }
  
      filterThisSubjectsAttributeDefPrivilegesHelper(request, response, subject);
  
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  
  }

  /**
   * the filter button was pressed of this groups attributeDef privileges screen, 
   * or paging or sorting
   * @param request
   * @param response
   */
  private void filterThisSubjectsAttributeDefPrivilegesHelper(HttpServletRequest request, HttpServletResponse response, Subject subject) {
  
    GrouperRequestContainer grouperRequestContainer = GrouperRequestContainer.retrieveFromRequestOrCreate();
  
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
    //if filtering text in subjects
    String privilegeFilterText = request.getParameter("privilegeFilterText");
    
    String privilegeFieldName = request.getParameter("privilegeField");
    
    Field privilegeField = null;
    if (!StringUtils.isBlank(privilegeFieldName)) {
      privilegeField = FieldFinder.find(privilegeFieldName, true);
    }
    
    //if filtering by subjects that have a certain type
    String membershipTypeString = request.getParameter("privilegeMembershipType");
    MembershipType membershipType = null;
    if (!StringUtils.isBlank(membershipTypeString)) {
      membershipType = MembershipType.valueOfIgnoreCase(membershipTypeString, true);
    }
    
    GuiPaging guiPaging = grouperRequestContainer.getSubjectContainer().getPrivilegeGuiPaging();

    QueryOptions queryOptions = new QueryOptions();

    QuerySort querySort = new QuerySort("a.nameDb", true);
    querySort.insertSortToBeginning("a.extensionDb", true);
    queryOptions.sort(querySort);

    GrouperPagingTag2.processRequest(request, guiPaging, queryOptions); 
    
    MembershipFinder membershipFinder = new MembershipFinder()
      .addSubject(subject).assignCheckSecurity(true)
      .assignFieldType(FieldType.ATTRIBUTE_DEF)
      .assignEnabled(true)
      .assignHasFieldForAttributeDef(true)
      .assignHasMembershipTypeForAttributeDef(true)
      .assignQueryOptionsForAttributeDef(queryOptions)
      .assignSplitScopeForAttributeDef(true);
    
    if (membershipType != null) {
      membershipFinder.assignMembershipType(membershipType);
    }

    if (privilegeField != null) {
      membershipFinder.assignField(privilegeField);
      membershipFinder.assignIncludeInheritedPrivileges(true);
    }

    if (!StringUtils.isBlank(privilegeFilterText)) {
      membershipFinder.assignScopeForAttributeDef(privilegeFilterText);
    }

    //set of subjects, and what privs each subject has
    Set<MembershipSubjectContainer> results = membershipFinder
        .findMembershipResult().getMembershipSubjectContainers();

    //inherit from grouperAll or Groupersystem or privilege inheritance
    MembershipSubjectContainer.considerAttributeDefPrivilegeInheritance(results);

    grouperRequestContainer.getSubjectContainer().setPrivilegeGuiMembershipSubjectContainers(
        GuiMembershipSubjectContainer.convertFromMembershipSubjectContainers(results));

    guiPaging.setTotalRecordCount(queryOptions.getQueryPaging().getTotalRecordCount());
  
    guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#thisSubjectsAttributeDefPrivilegesFilterResultsId", 
        "/WEB-INF/grouperUi2/subject/thisSubjectsAttributeDefPrivilegesContents.jsp"));
  
  }

  /**
   * the filter button for this groups stem privileges was pressed, or paging or sorting or something
   * @param request
   * @param response
   */
  public void filterThisSubjectsStemPrivileges(HttpServletRequest request, HttpServletResponse response) {
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    try {
      grouperSession = GrouperSession.start(loggedInSubject);
  
  
      Subject subject = retrieveSubjectHelper(request);
      
      if (subject == null) {
        return;
      }
  
      filterThisSubjectsStemPrivilegesHelper(request, response, subject);
  
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  
  }

  /**
   * the filter button was pressed of this subjects stem privileges screen, 
   * or paging or sorting, or view Group or something
   * @param request
   * @param response
   */
  private void filterThisSubjectsStemPrivilegesHelper(HttpServletRequest request, HttpServletResponse response, Subject subject) {
  
    GrouperRequestContainer grouperRequestContainer = GrouperRequestContainer.retrieveFromRequestOrCreate();
  
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
    //if filtering text in subjects
    String privilegeFilterText = request.getParameter("privilegeFilterText");
    
    String privilegeFieldName = request.getParameter("privilegeField");
    
    Field privilegeField = null;
    if (!StringUtils.isBlank(privilegeFieldName)) {
      privilegeField = FieldFinder.find(privilegeFieldName, true);
    }
    
    //if filtering by subjects that have a certain type
    String membershipTypeString = request.getParameter("privilegeMembershipType");
    MembershipType membershipType = null;
    if (!StringUtils.isBlank(membershipTypeString)) {
      membershipType = MembershipType.valueOfIgnoreCase(membershipTypeString, true);
    }
    
    GuiPaging guiPaging = grouperRequestContainer.getSubjectContainer().getPrivilegeGuiPaging();
    QueryOptions queryOptions = new QueryOptions();

    QuerySort querySort = new QuerySort("s.displayNameDb", true);
    querySort.insertSortToBeginning("s.displayExtensionDb", true);
    queryOptions.sort(querySort);

    GrouperPagingTag2.processRequest(request, guiPaging, queryOptions); 
    
    MembershipFinder membershipFinder = new MembershipFinder()
      .addSubject(subject).assignCheckSecurity(true)
      .assignFieldType(FieldType.NAMING)
      .assignEnabled(true)
      .assignHasFieldForStem(true)
      .assignHasMembershipTypeForStem(true)
      .assignQueryOptionsForStem(queryOptions)
      .assignSplitScopeForStem(true);
    
    if (membershipType != null) {
      membershipFinder.assignMembershipType(membershipType);
    }
  
    if (privilegeField != null) {
      membershipFinder.assignField(privilegeField);
      membershipFinder.assignIncludeInheritedPrivileges(true);
    }
  
    if (!StringUtils.isBlank(privilegeFilterText)) {
      membershipFinder.assignScopeForStem(privilegeFilterText);
    }
  
    //set of subjects, and what privs each subject has
    Set<MembershipSubjectContainer> results = membershipFinder
        .findMembershipResult().getMembershipSubjectContainers();
  
    //inherit from grouperAll or Groupersystem or privilege inheritance
    MembershipSubjectContainer.considerNamingPrivilegeInheritance(results);
  
    grouperRequestContainer.getSubjectContainer().setPrivilegeGuiMembershipSubjectContainers(
        GuiMembershipSubjectContainer.convertFromMembershipSubjectContainers(results));
  
    guiPaging.setTotalRecordCount(queryOptions.getQueryPaging().getTotalRecordCount());
  
    guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#thisSubjectsStemPrivilegesFilterResultsId", 
        "/WEB-INF/grouperUi2/subject/thisSubjectsStemPrivilegesContents.jsp"));
  
  }

  /**
   * submit button on add attributeDef form pressed
   * @param request
   * @param response
   */
  public void addAttributeDefSubmit(final HttpServletRequest request, final HttpServletResponse response) {
  
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
  
    GrouperSession grouperSession = null;
  
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
    try {
      grouperSession = GrouperSession.start(loggedInSubject);
      
      Subject subject = retrieveSubjectHelper(request);
  
      if (subject == null) {
        return;
      }
    
      String tempAttributeDefString = request.getParameter("attributeDefAddMemberComboName");
  
      //just get what they typed in
      if (StringUtils.isBlank(tempAttributeDefString)) {
        tempAttributeDefString = request.getParameter("attributeDefAddMemberComboNameDisplay");
      }
      
      final String attributeDefString = tempAttributeDefString;
  
      AttributeDef attributeDef = (AttributeDef)GrouperSession.callbackGrouperSession(grouperSession.internal_getRootSession(), new GrouperSessionHandler() {
        
        @Override
        public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
          AttributeDef theAttributeDef = null;
          
          if (!StringUtils.isBlank(attributeDefString)) {
            theAttributeDef = new AttributeDefFinder().assignScope(attributeDefString).assignFindByUuidOrName(true)
                .assignSubject(loggedInSubject)
                .assignPrivileges(AttributeDefPrivilege.ATTR_ADMIN_PRIVILEGES).findAttribute();
          }
          return theAttributeDef;
        }
      });
        
      if (attributeDef == null) {
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, "#attributeDefAddMemberComboId", 
            TextContainer.retrieveFromRequest().getText().get("subjectAddMemberCantFindAttributeDef")));
        return;
      }      
        
      boolean adminChecked = GrouperUtil.booleanValue(request.getParameter("privileges_attrAdmins[]"), false);
      boolean updateChecked = GrouperUtil.booleanValue(request.getParameter("privileges_attrUpdaters[]"), false);
      boolean readChecked = GrouperUtil.booleanValue(request.getParameter("privileges_attrReaders[]"), false);
      boolean viewChecked = GrouperUtil.booleanValue(request.getParameter("privileges_attrViewers[]"), false);
      boolean optinChecked = GrouperUtil.booleanValue(request.getParameter("privileges_attrOptins[]"), false);
      boolean optoutChecked = GrouperUtil.booleanValue(request.getParameter("privileges_attrOptouts[]"), false);
      boolean attrReadChecked = GrouperUtil.booleanValue(request.getParameter("privileges_attrDefAttrReaders[]"), false);
      boolean attrUpdateChecked = GrouperUtil.booleanValue(request.getParameter("privileges_attrDefAttrUpdaters[]"), false);

      if (!adminChecked && !updateChecked && !readChecked
          && !viewChecked && !optinChecked && !optoutChecked && !attrReadChecked && !attrUpdateChecked) {
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error,
            "#attributeDefPrivsErrorId",
            TextContainer.retrieveFromRequest().getText().get("subjectAddMemberAttributeDefPrivRequired")));
        return;
      }
      
      boolean madeChanges = attributeDef.getPrivilegeDelegate().grantPrivs(
          subject, adminChecked, 
          updateChecked, readChecked, viewChecked, optinChecked, optoutChecked, attrReadChecked, 
          attrUpdateChecked, false);
      
      if (madeChanges) {
  
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
            TextContainer.retrieveFromRequest().getText().get("attributeDefAddMemberMadeChangesSuccess")));
  
        filterThisSubjectsAttributeDefPrivilegesHelper(request, response, subject);
  
  
      } else {
  
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.info, 
            TextContainer.retrieveFromRequest().getText().get("attributeDefAddMemberNoChangesSuccess")));
  
      }
  
      //clear out the combo
      guiResponseJs.addAction(GuiScreenAction.newScript(
          "dijit.byId('attributeDefAddMemberComboId').set('displayedValue', ''); " +
          "dijit.byId('attributeDefAddMemberComboId').set('value', '');"));

      GrouperUserDataApi.recentlyUsedMemberAdd(GrouperUiUserData.grouperUiGroupNameForUserData(), 
          loggedInSubject, subject);
      GrouperUserDataApi.recentlyUsedAttributeDefAdd(GrouperUiUserData.grouperUiGroupNameForUserData(), 
          loggedInSubject, attributeDef);

    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  
  }
}
