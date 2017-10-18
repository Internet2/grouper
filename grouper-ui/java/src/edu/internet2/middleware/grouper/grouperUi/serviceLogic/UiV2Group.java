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
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.Composite;
import edu.internet2.middleware.grouper.CompositeFinder;
import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.FieldFinder;
import edu.internet2.middleware.grouper.FieldType;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupCopy;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GroupMove;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.GrouperSourceAdapter;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.MembershipFinder;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.app.loader.GrouperLoader;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderType;
import edu.internet2.middleware.grouper.app.loader.ldap.LoaderLdapUtils;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefFinder;
import edu.internet2.middleware.grouper.audit.AuditEntry;
import edu.internet2.middleware.grouper.audit.UserAuditQuery;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.exception.GroupDeleteException;
import edu.internet2.middleware.grouper.exception.GroupNotFoundException;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.exception.GrouperValidationException;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.group.TypeOfGroup;
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
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiSorting;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GroupContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GrouperRequestContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GuiAuditEntry;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.RulesContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.TextContainer;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.membership.MembershipSubjectContainer;
import edu.internet2.middleware.grouper.membership.MembershipType;
import edu.internet2.middleware.grouper.misc.CompositeType;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.misc.SaveMode;
import edu.internet2.middleware.grouper.misc.SaveResultType;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.AttributeDefPrivilege;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
import edu.internet2.middleware.grouper.privs.Privilege;
import edu.internet2.middleware.grouper.rules.RuleDefinition;
import edu.internet2.middleware.grouper.rules.RuleFinder;
import edu.internet2.middleware.grouper.subj.GrouperSubject;
import edu.internet2.middleware.grouper.subj.SubjectHelper;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.ui.tags.GrouperPagingTag2;
import edu.internet2.middleware.grouper.ui.util.GrouperUiConfig;
import edu.internet2.middleware.grouper.ui.util.GrouperUiUserData;
import edu.internet2.middleware.grouper.ui.util.GrouperUiUtils;
import edu.internet2.middleware.grouper.userData.GrouperUserDataApi;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Source;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectNotUniqueException;
import edu.internet2.middleware.subject.provider.SourceManager;

/**
 * operations in the group screen
 * @author mchyzer
 *
 */
public class UiV2Group {

  
  /**
   * results from retrieving results
   *
   */
  public static class RetrieveGroupHelperResult {
  
    /**
     * group
     */
    private Group group;
  
    /**
     * group
     * @return group
     */
    public Group getGroup() {
      return this.group;
    }
  
    /**
     * group
     * @param group1
     */
    public void setGroup(Group group1) {
      this.group = group1;
    }
    
    /**
     * if added error to screen
     */
    private boolean addedError;
  
    /**
     * if added error to screen
     * @return if error
     */
    public boolean isAddedError() {
      return this.addedError;
    }
  
    /**
     * if added error to screen
     * @param addedError1
     */
    public void setAddedError(boolean addedError1) {
      this.addedError = addedError1;
    }
  }

  
  /**
   * view this groups privileges inherited from folders
   * @param request
   * @param response
   */
  public void thisGroupMembersPrivilegesInheritedFromFolders(HttpServletRequest request, HttpServletResponse response) {
    //TODO 
    
  }
  
  /**
   * view this groups privileges inherited from folders
   * @param request
   * @param response
   */
  public void thisGroupsPrivilegesInheritedFromFolders(HttpServletRequest request, HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    Group group = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      group = retrieveGroupHelper(request, AccessPrivilege.ADMIN).getGroup();
      
      if (group == null) {
        return;
      }
  
      if (!GrouperRequestContainer.retrieveFromRequestOrCreate().getGroupContainer().isCanReadPrivilegeInheritance()) {
        throw new RuntimeException("Not allowed to read privilege inheritance! " + GrouperUtil.subjectToString(loggedInSubject));
      }

      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();

      RulesContainer rulesContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getRulesContainer();
      
      Set<GuiRuleDefinition> guiRuleDefinitions = new TreeSet<GuiRuleDefinition>();
      {
        Set<RuleDefinition> groupRuleDefinitions  = RuleFinder.findGroupPrivilegeInheritRules(group.getParentStem());
        for (RuleDefinition ruleDefinition : GrouperUtil.nonNull(groupRuleDefinitions)) {
          GuiRuleDefinition guiRuleDefinition = new GuiRuleDefinition(ruleDefinition);
          if (guiRuleDefinition.getOwnerGuiStem() != null) {
            guiRuleDefinitions.add(guiRuleDefinition);
          }
        }
      }
      
      for (GuiRuleDefinition guiRuleDefinition : guiRuleDefinitions) {
        if (StringUtils.equals(group.getParentStem().getUuid(), guiRuleDefinition.getOwnerGuiStem().getStem().getUuid())) {
          guiRuleDefinition.setDirect(true);
        }
      }
      rulesContainer.setGuiRuleDefinitions(guiRuleDefinitions);

      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/group/thisGroupsPrivilegesInheritedFromFolders.jsp"));

    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }

 
  /**
   * view group
   * @param request
   * @param response
   */
  public void viewGroup(HttpServletRequest request, HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    Group group = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      group = retrieveGroupHelper(request, AccessPrivilege.VIEW).getGroup();
      
      if (group == null) {
        return;
      }
  
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      
      if (retrieveGroupHelper(request, AccessPrivilege.UPDATE, false).getGroup() != null) {
        UiV2Attestation.setupAttestation(group);            
      }
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/group/viewGroup.jsp"));

      if (GrouperRequestContainer.retrieveFromRequestOrCreate().getGroupContainer().isCanRead()) {
        filterHelper(request, response, group);
      }
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
  
  
      Group group = retrieveGroupHelper(request, AccessPrivilege.READ).getGroup();
      
      if (group == null) {
        return;
      }
  
      filterHelper(request, response, group);
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }

  /**
   * the filter button was pressed, or paging or sorting, or view Group or something
   * @param request
   * @param response
   */
  public void removeMemberForThisGroupsMemberships(HttpServletRequest request, HttpServletResponse response) {
  
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
  
    GrouperSession grouperSession = null;
  
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
    try {
      grouperSession = GrouperSession.start(loggedInSubject);
  
      final Group group = retrieveGroupHelper(request, AccessPrivilege.VIEW).getGroup();
  
      if (group == null) {
        return;
      }

      String ownerGroupId = request.getParameter("ownerGroupId");
      
      Group ownerGroup = GroupFinder.findByUuid(grouperSession, ownerGroupId, false);

      //not sure why this would happen
      if (ownerGroup == null) {
        
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("groupDeleteMemberCantFindOwnerGroup")));
        
      } else {
      
        boolean madeChanges = ownerGroup.deleteMember(group.toSubject(), false);
        
        if (madeChanges) {
    
          guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
              TextContainer.retrieveFromRequest().getText().get("groupDeleteFromOwnerSuccess")));
              
        } else {
          
          //not sure why this would happen (race condition?)
          guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.info, 
              TextContainer.retrieveFromRequest().getText().get("groupDeleteFromOwnerNoChangesSuccess")));
    
        }
      }
      
      filterThisGroupsMembershipsHelper(request, response, group);

      GrouperUserDataApi.recentlyUsedGroupAdd(GrouperUiUserData.grouperUiGroupNameForUserData(), 
          loggedInSubject, group);

    } catch (RuntimeException re) {
      if (GrouperUiUtils.vetoHandle(GuiResponseJs.retrieveGuiResponseJs(), re)) {
        return;
      }
      throw re;
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }


  /**
   * the remove from groups button was pressed
   * @param request
   * @param response
   */
  public void removeMembersForThisGroupsMemberships(HttpServletRequest request, HttpServletResponse response) {
  
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
  
    GrouperSession grouperSession = null;
  
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
    try {
      grouperSession = GrouperSession.start(loggedInSubject);
  
      final Group group = retrieveGroupHelper(request, AccessPrivilege.VIEW).getGroup();
  
      if (group == null) {
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
            TextContainer.retrieveFromRequest().getText().get("thisGroupsMembershipsRemoveNoGroupsSelects")));
        return;
      }
      int successes = 0;
      int failures = 0;
      
      Subject groupSubject = group.toSubject();
      for (String membershipId : membershipsIds) {
        try {
          Membership membership = new MembershipFinder().addMembershipId(membershipId).findMembership(true);
          Group ownerGroup = membership.getOwnerGroup();
          //dont worry about if no change, thats a success
          ownerGroup.deleteMember(groupSubject, false);
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
            TextContainer.retrieveFromRequest().getText().get("groupDeleteFromOwnerErrors")));
      } else {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
            TextContainer.retrieveFromRequest().getText().get("groupDeleteFromOwnerSuccesses")));
      }
      
      filterThisGroupsMembershipsHelper(request, response, group);

      GrouperUserDataApi.recentlyUsedGroupAdd(GrouperUiUserData.grouperUiGroupNameForUserData(), 
          loggedInSubject, group);

    } catch (RuntimeException re) {
      if (GrouperUiUtils.vetoHandle(GuiResponseJs.retrieveGuiResponseJs(), re)) {
        return;
      }
      throw re;
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
  /**
   * remove one member from the group
   * @param request
   * @param response
   */
  public void removeMember(HttpServletRequest request, HttpServletResponse response) {
  
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
  
    GrouperSession grouperSession = null;
  
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
    try {
      grouperSession = GrouperSession.start(loggedInSubject);
  
      final Group group = retrieveGroupHelper(request, AccessPrivilege.UPDATE).getGroup();
  
      if (group == null) {
        return;
      }

      String memberId = request.getParameter("memberId");
      
      Member member = MemberFinder.findByUuid(grouperSession, memberId, false);

      //not sure why this would happen
      if (member == null) {
        
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("groupDeleteMemberCantFindMember")));
        
      } else {
      
        boolean madeChanges = group.deleteMember(member, false);
        
        if (madeChanges) {
    
          guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
              TextContainer.retrieveFromRequest().getText().get("groupDeleteMemberSuccess")));
              
        } else {
          
          //not sure why this would happen (race condition?)
          guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.info, 
              TextContainer.retrieveFromRequest().getText().get("groupDeleteMemberNoChangesSuccess")));
    
        }
      }
      
      filterHelper(request, response, group);

      GrouperUserDataApi.recentlyUsedGroupAdd(GrouperUiUserData.grouperUiGroupNameForUserData(), 
          loggedInSubject, group);

      GrouperUserDataApi.recentlyUsedMemberAdd(GrouperUiUserData.grouperUiGroupNameForUserData(), 
          loggedInSubject, member);

    } catch (RuntimeException re) {
      if (GrouperUiUtils.vetoHandle(GuiResponseJs.retrieveGuiResponseJs(), re)) {
        return;
      }
      throw re;
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * the filter button was pressed, or paging or sorting, or view Group or something
   * @param request
   * @param response
   * @param group
   */
  private void filterHelper(HttpServletRequest request, HttpServletResponse response, Group group) {
    
    GrouperRequestContainer grouperRequestContainer = GrouperRequestContainer.retrieveFromRequestOrCreate();
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    String filterText = request.getParameter("filterText");
    GroupContainer groupContainer = grouperRequestContainer.getGroupContainer();
    
    //if filtering by subjects that have a certain type
    String membershipTypeString = request.getParameter("membershipType");
    MembershipType membershipType = null;
    if (!StringUtils.isBlank(membershipTypeString)) {
      membershipType = MembershipType.valueOfIgnoreCase(membershipTypeString, true);
    }

    GuiPaging guiPaging = groupContainer.getGuiPaging();
    QueryOptions queryOptions = new QueryOptions();

    GrouperPagingTag2.processRequest(request, guiPaging, queryOptions);

    MembershipFinder membershipFinder = new MembershipFinder()
      .addGroupId(group.getId()).assignCheckSecurity(true)
      .assignHasFieldForMember(true)
      .assignEnabled(true)
      .assignHasMembershipTypeForMember(true)
      .assignQueryOptionsForMember(queryOptions)
      .assignSplitScopeForMember(true);
    
    if (membershipType != null) {
      membershipFinder.assignMembershipType(membershipType);
    }

    if (!StringUtils.isBlank(filterText)) {
      membershipFinder.assignScopeForMember(filterText);
    }

    //set of subjects, and what memberships each subject has
    Set<MembershipSubjectContainer> results = membershipFinder
        .findMembershipResult().getMembershipSubjectContainers();

    groupContainer.setGuiMembershipSubjectContainers(GuiMembershipSubjectContainer.convertFromMembershipSubjectContainers(results));
    
    guiPaging.setTotalRecordCount(queryOptions.getQueryPaging().getTotalRecordCount());
    
    guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#groupFilterResultsId", 
        "/WEB-INF/grouperUi2/group/groupContents.jsp"));
  
  }

  /**
   * combo filter
   * @param request
   * @param response
   */
  public void addMemberFilter(HttpServletRequest request, HttpServletResponse response) {

    //run the combo logic
    DojoComboLogic.logic(request, response, new DojoComboQueryLogicBase<Subject>() {

      /**
       */
      @Override
      public Subject lookup(HttpServletRequest localRequest, GrouperSession grouperSession, String query) {

        //when we refer to subjects in the dropdown, we will use a sourceId / subject tuple
        
        Subject subject = null;
            
        try {
          GrouperSourceAdapter.searchForGroupsWithReadPrivilege(true);
          if (query != null && query.contains("||")) {
            String sourceId = GrouperUtil.prefixOrSuffix(query, "||", true);
            String subjectId = GrouperUtil.prefixOrSuffix(query, "||", false);
            subject =  SubjectFinder.findByIdOrIdentifierAndSource(subjectId, sourceId, false);
          } else {
            try { 
              subject = SubjectFinder.findByIdOrIdentifier(query, false);
            } catch (SubjectNotUniqueException snue) {
              //ignore this...
              if (LOG.isDebugEnabled()) {
                LOG.debug("Find by id or identifier not unique: '" + query + "'");
              }
            }
          }
        } finally {
          GrouperSourceAdapter.clearSearchForGroupsWithReadPrivilege();
        }
        
        return subject;
      }

      /**
       * 
       */
      @Override
      public Collection<Subject> search(HttpServletRequest localRequest, GrouperSession grouperSession, String query) {
        
        Group group = UiV2Group.retrieveGroupHelper(localRequest, AccessPrivilege.UPDATE, false).getGroup();
        String stemName = null;
        if (group == null) {
          Stem stem = UiV2Stem.retrieveStemHelper(localRequest, true, false, false).getStem();
          stemName = stem == null ? null : stem.getName();
        } else {
          stemName = group.getParentStemName();
        }
        try {
          GrouperSourceAdapter.searchForGroupsWithReadPrivilege(true);
          Collection<Subject> results = StringUtils.isBlank(stemName) ? 
              SubjectFinder.findPage(query).getResults()
              : SubjectFinder.findPageInStem(stemName, query).getResults();
          return results;
        } finally {
          GrouperSourceAdapter.clearSearchForGroupsWithReadPrivilege();
        }
      
      }

      /**
       * 
       * @param t
       * @return source with id
       */
      @Override
      public String retrieveId(GrouperSession grouperSession, Subject t) {
        return t.getSourceId() + "||" + t.getId();
      }
      
      /**
       * 
       */
      @Override
      public String retrieveLabel(GrouperSession grouperSession, Subject t) {
        return new GuiSubject(t).getScreenLabelLong();
      }

      /**
       * 
       */
      @Override
      public String retrieveHtmlLabel(GrouperSession grouperSession, Subject t) {
        String value = new GuiSubject(t).getScreenLabelLongWithIcon();
        return value;
      }

      /**
       * 
       */
      @Override
      public String initialValidationError(HttpServletRequest localRequest, GrouperSession grouperSession) {

        //MCH 20140316
        //Group group = retrieveGroupHelper(request, AccessPrivilege.UPDATE).getGroup();
        //
        //if (group == null) {
        //  
        //  return "Not allowed to edit group";
        //}
        //
        return null;
      }
    });

              
  }
  
  /**
   * search for a subject to add to the group
   * @param request
   * @param response
   */
  public void addMemberSearch(HttpServletRequest request, HttpServletResponse response) {

    GrouperRequestContainer grouperRequestContainer = GrouperRequestContainer.retrieveFromRequestOrCreate();
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
    GrouperSession grouperSession = null;
    
    try {
      grouperSession = GrouperSession.start(loggedInSubject);
  
  
      GroupContainer groupContainer = grouperRequestContainer.getGroupContainer();
  
      String searchString = request.getParameter("addMemberSubjectSearch");
      
      Group group = retrieveGroupHelper(request, AccessPrivilege.UPDATE, false).getGroup();
      String stemName = null;
      if (group != null) {
        stemName = group.getParentStemName();
      } else {
        Stem stem = UiV2Stem.retrieveStemHelper(request, true, false, false).getStem();
        if (stem != null) {
          stemName = stem.getName();
        } else {
          AttributeDef attributeDef = UiV2AttributeDef.retrieveAttributeDefHelper(request, AttributeDefPrivilege.ATTR_ADMIN, true).getAttributeDef();
          if (attributeDef != null) {
            stemName = attributeDef.getParentStemName();
          }
        }
      }
      
      boolean searchOk = GrouperUiUtils.searchStringValid(searchString);
      if (!searchOk) {
        
        guiResponseJs.addAction(GuiScreenAction.newInnerHtml("#addMemberResults", 
            TextContainer.retrieveFromRequest().getText().get("groupAddMemberNotEnoughChars")));
        return;
      }

      String matchExactIdString = request.getParameter("matchExactId[]");
      boolean matchExactId = GrouperUtil.booleanValue(matchExactIdString, false);

      String sourceId = request.getParameter("sourceId");
      
      Set<Subject> subjects = null;
      if (matchExactId) {
        if (GrouperConfig.retrieveConfig().propertyValueBoolean("grouperQuerySubjectsMultipleQueriesCommaSeparated", true)) {
          Set<String> searchStrings = GrouperUtil.splitTrimToSet(searchString, ",");
          if (StringUtils.equals("all", sourceId)) {
            subjects = new LinkedHashSet<Subject>(GrouperUtil.nonNull(SubjectFinder.findByIdsOrIdentifiers(searchStrings)).values());
          } else {
            subjects = new LinkedHashSet<Subject>(GrouperUtil.nonNull(SubjectFinder.findByIdsOrIdentifiers(searchStrings, sourceId)).values());
          }
        } else {
          Subject subject = null;
          if (StringUtils.equals("all", sourceId)) {
            try {
              subject = SubjectFinder.findByIdOrIdentifier(searchString, false);
            } catch (SubjectNotUniqueException snue) {
              //ignore
            }
          } else {
            subject = SubjectFinder.findByIdOrIdentifierAndSource(searchString, sourceId, false);
          }

          subjects = new LinkedHashSet<Subject>();
          if (subject != null) {
            subjects.add(subject);
          }
        }
      } else {
        if (StringUtils.equals("all", sourceId)) {
          if (group != null) {
            subjects = SubjectFinder.findPageInStem(stemName, searchString).getResults();
          } else {
            subjects = SubjectFinder.findPage(searchString).getResults();
          }
        } else {
          Set<Source> sources = GrouperUtil.toSet(SourceManager.getInstance().getSource(sourceId));
          if (group != null) {
            subjects = SubjectFinder.findPageInStem(stemName, searchString, sources).getResults();
          } else {
            subjects = SubjectFinder.findPage(searchString, sources).getResults();
          }
        }
      }
      
      if (GrouperUtil.length(subjects) == 0) {

        guiResponseJs.addAction(GuiScreenAction.newInnerHtml("#addMemberResults", 
            TextContainer.retrieveFromRequest().getText().get("groupAddMemberNoSubjectsFound")));
        return;
      }
      
      Set<GuiSubject> guiSubjects = GuiSubject.convertFromSubjects(subjects, "uiV2.subjectSearchResults", 30);
      
      groupContainer.setGuiSubjectsAddMember(guiSubjects);
  
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#addMemberResults", 
          "/WEB-INF/grouperUi2/group/addMemberResults.jsp"));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
  /**
   * submit button on add member form pressed
   * @param request
   * @param response
   */
  public void addMemberSubmit(final HttpServletRequest request, final HttpServletResponse response) {

    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();

    GrouperSession grouperSession = null;

    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();

    try {
      grouperSession = GrouperSession.start(loggedInSubject);
  
      final Group group = retrieveGroupHelper(request, AccessPrivilege.UPDATE).getGroup();
  
      if (group == null) {
        return;
      }
    
      String subjectString = request.getParameter("groupAddMemberComboName");
  
      Subject subject = null;
      
      if (subjectString != null && subjectString.contains("||")) {
        String sourceId = GrouperUtil.prefixOrSuffix(subjectString, "||", true);
        String subjectId = GrouperUtil.prefixOrSuffix(subjectString, "||", false);
        subject =  SubjectFinder.findByIdOrIdentifierAndSource(subjectId, sourceId, false);

      } else {
        try {
          subject = SubjectFinder.findByIdOrIdentifier(subjectString, false);
        } catch (SubjectNotUniqueException snue) {
          //ignore
        }
          
      }

      if (subject == null) {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("groupAddMemberCantFindSubject")));
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
      
      if (!defaultPrivs && !memberChecked && !adminChecked && !updateChecked
          && !readChecked && !viewChecked && !optinChecked && !optoutChecked
          && !attrReadChecked && !attrUpdateChecked) {
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error,
            "#groupPrivsErrorId",
            TextContainer.retrieveFromRequest().getText().get("groupAddMemberPrivRequired")));
        return;
        
      }

      boolean madeChanges = group.addOrEditMember(subject, defaultPrivs, memberChecked, adminChecked, 
          updateChecked, readChecked, viewChecked, optinChecked, optoutChecked, attrReadChecked, 
          attrUpdateChecked, null, null, false);
      
      if (madeChanges) {

        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
            TextContainer.retrieveFromRequest().getText().get("groupAddMemberMadeChangesSuccess")));

        //what subscreen are we on?
        String groupRefreshPart = request.getParameter("groupRefreshPart");
        if (StringUtils.equals(groupRefreshPart, "audits")) {
          viewAuditsFilter(request, response);
        } else if (StringUtils.equals(groupRefreshPart, "privileges")) {
          filterPrivilegesHelper(request, response, group);
        } else if (StringUtils.equals(groupRefreshPart, "thisGroupsAttributeDefPrivileges")) {
          //doesnt affect
        } else if (StringUtils.equals(groupRefreshPart, "thisGroupsGroupPrivileges")) {
          //doesnt affect
        } else if (StringUtils.equals(groupRefreshPart, "thisGroupsStemPrivileges")) {
          //doesnt affect
        } else if (StringUtils.equals(groupRefreshPart, "thisGroupsMemberships")) {
          //doesnt affect
        } else {
          filterHelper(request, response, group);
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

    } catch (RuntimeException re) {
      if (GrouperUiUtils.vetoHandle(guiResponseJs, re)) {
        return;
      }
      throw re;
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }

  }
  
  /**
   * assign or remove a privilege from a user, and redraw the filter screen... put a success at top
   * @param request
   * @param response
   */
  public void assignPrivilege(HttpServletRequest request, HttpServletResponse response) {
  
    GrouperRequestContainer grouperRequestContainer = GrouperRequestContainer.retrieveFromRequestOrCreate();
  
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
  
    GrouperSession grouperSession = null;
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    try {
      grouperSession = GrouperSession.start(loggedInSubject);
  
      Group group = retrieveGroupHelper(request, AccessPrivilege.ADMIN).getGroup();
  
      if (group == null) {
        return;
      }
  
      GroupContainer groupContainer = grouperRequestContainer.getGroupContainer();
  
      //?assign=false&groupId=${grouperRequestContainer.groupContainer.guiGroup.stem.id}&fieldName=${fieldName}&memberId=${guiMembershipSubjectContainer.guiMember.member.uuid}
      String assignString = request.getParameter("assign");
      boolean assign = GrouperUtil.booleanValue(assignString);
      String fieldName = request.getParameter("fieldName");
      String memberId = request.getParameter("memberId");
  
      Member member = MemberFinder.findByUuid(grouperSession, memberId, true);
      
      Privilege privilege = AccessPrivilege.listToPriv(fieldName);
      
      if (privilege == null) {
        throw new RuntimeException("Why is privilege not found???? " + fieldName);
      }
      
      //if someone revoked in the meantime, who cares...
      if (assign) {
        group.grantPriv(member.getSubject(), privilege, false);
        
        //set a success message
        //messes up screen
        //guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
        //    TextContainer.retrieveFromRequest().getText().get("stemSuccessGrantedPrivilege")));
        
      } else {
        group.revokePriv(member.getSubject(), privilege, false);
        
        //messes up screen
        //set a success message
        //guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
        //    TextContainer.retrieveFromRequest().getText().get("stemSuccessRevokedPrivilege")));
      }
  
      //reset the data (not really necessary, just in case)
      groupContainer.setPrivilegeGuiMembershipSubjectContainers(null);
      
      
      filterPrivilegesHelper(request, response, group);
  
      GrouperUserDataApi.recentlyUsedGroupAdd(GrouperUiUserData.grouperUiGroupNameForUserData(), 
          loggedInSubject, group);

      GrouperUserDataApi.recentlyUsedMemberAdd(GrouperUiUserData.grouperUiGroupNameForUserData(), 
          loggedInSubject, member);
    } catch (RuntimeException re) {
      if (GrouperUiUtils.vetoHandle(guiResponseJs, re)) {
        return;
      }
      throw re;

    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  
  
  }

  /**
   * join the current group
   * @param request
   * @param response
   */
  public void joinGroup(HttpServletRequest request, HttpServletResponse response) {

    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    Group group = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      group = retrieveGroupHelper(request, AccessPrivilege.OPTIN).getGroup();
      
      if (group == null) {
        return;
      }

      group.addMember(loggedInSubject, false);

      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
          TextContainer.retrieveFromRequest().getText().get("groupJoinSuccess")));

      //redisplay so the button will change, note, this will not change the memberships
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#groupMoreActionsButtonContentsDivId", 
          "/WEB-INF/grouperUi2/group/groupMoreActionsButtonContents.jsp"));

      GrouperUserDataApi.recentlyUsedGroupAdd(GrouperUiUserData.grouperUiGroupNameForUserData(), 
          loggedInSubject, group);

    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }

  }
  
  /**
   * leave the current group
   * @param request
   * @param response
   */
  public void leaveGroup(HttpServletRequest request, HttpServletResponse response) {

    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    Group group = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      group = retrieveGroupHelper(request, AccessPrivilege.OPTOUT).getGroup();
      
      if (group == null) {
        return;
      }

      group.deleteMember(loggedInSubject, false);

      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
          TextContainer.retrieveFromRequest().getText().get("groupLeaveSuccess")));

      //redisplay so the button will change, note, this will not change the memberships
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#groupMoreActionsButtonContentsDivId", 
          "/WEB-INF/grouperUi2/group/groupMoreActionsButtonContents.jsp"));

      GrouperUserDataApi.recentlyUsedGroupAdd(GrouperUiUserData.grouperUiGroupNameForUserData(), 
          loggedInSubject, group);

    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }

  }
  
  /**
   * 
   * @param request
   * @param response
   */
  public void addToMyFavorites(HttpServletRequest request, HttpServletResponse response) {
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    Group group = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      group = retrieveGroupHelper(request, AccessPrivilege.VIEW).getGroup();
      
      if (group == null) {
        return;
      }

      GrouperUserDataApi.favoriteGroupAdd(GrouperUiUserData.grouperUiGroupNameForUserData(), loggedInSubject, group);

      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
          TextContainer.retrieveFromRequest().getText().get("groupSuccessAddedToMyFavorites")));

      //redisplay so the button will change
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#groupMoreActionsButtonContentsDivId", 
          "/WEB-INF/grouperUi2/group/groupMoreActionsButtonContents.jsp"));

    } catch (RuntimeException re) {
      if (GrouperUiUtils.vetoHandle(GuiResponseJs.retrieveGuiResponseJs(), re)) {
        return;
      }
      throw re;
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
  
    Group group = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      group = retrieveGroupHelper(request, AccessPrivilege.VIEW).getGroup();
      
      if (group == null) {
        return;
      }

      GrouperUserDataApi.favoriteGroupRemove(GrouperUiUserData.grouperUiGroupNameForUserData(), loggedInSubject, group);

      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
          TextContainer.retrieveFromRequest().getText().get("groupSuccessRemovedFromMyFavorites")));

      //redisplay so the button will change
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#groupMoreActionsButtonContentsDivId", 
          "/WEB-INF/grouperUi2/group/groupMoreActionsButtonContents.jsp"));

      GrouperUserDataApi.recentlyUsedGroupAdd(GrouperUiUserData.grouperUiGroupNameForUserData(), 
          loggedInSubject, group);

    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }

  }
  
  /**
   * submit the main form on the privilege screen which can do batch operations on a number of rows
   * @param request
   * @param response
   */
  public void assignPrivilegeBatch(HttpServletRequest request, HttpServletResponse response) {
  
    GrouperRequestContainer grouperRequestContainer = GrouperRequestContainer.retrieveFromRequestOrCreate();
  
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();

    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
  
    GrouperSession grouperSession = null;
    
    try {
      grouperSession = GrouperSession.start(loggedInSubject);
  
      Group group = retrieveGroupHelper(request, AccessPrivilege.ADMIN).getGroup();
  
      if (group == null) {
        return;
      }
  
      GroupContainer groupContainer = grouperRequestContainer.getGroupContainer();
  
      //UiV2Group.assignPrivilegeBatch?groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}
      
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
      Set<Member> members = new LinkedHashSet<Member>();
      
      //loop through all the checkboxes and collect all the members
      for (int i=0;i<pageSize;i++) {
        String memberId = request.getParameter("privilegeSubjectRow_" + i + "[]");
        if (!StringUtils.isBlank(memberId)) {
          Member member = MemberFinder.findByUuid(grouperSession, memberId, true);
          members.add(member);
        }
      }
  
      if (GrouperUtil.length(members) == 0) {
  
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("stemErrorEntityRequired")));
        guiResponseJs.addAction(GuiScreenAction.newScript("guiScrollTop()"));
        return;
      }
      
      int changes = 0;

      Privilege[] privileges = null;
      if (assignAll) {
        if (assign) {
          privileges = new Privilege[]{AccessPrivilege.listToPriv(Field.FIELD_NAME_ADMINS)};
        } else {
          privileges = new Privilege[]{
                  AccessPrivilege.listToPriv(Field.FIELD_NAME_GROUP_ATTR_READERS),
                  AccessPrivilege.listToPriv(Field.FIELD_NAME_GROUP_ATTR_UPDATERS),
                  AccessPrivilege.listToPriv(Field.FIELD_NAME_OPTOUTS),
                  AccessPrivilege.listToPriv(Field.FIELD_NAME_READERS),
                  AccessPrivilege.listToPriv(Field.FIELD_NAME_VIEWERS),
                  AccessPrivilege.listToPriv(Field.FIELD_NAME_UPDATERS),
                  AccessPrivilege.listToPriv(Field.FIELD_NAME_OPTINS),
                  AccessPrivilege.listToPriv(Field.FIELD_NAME_ADMINS)
          };
        }
      } else {
        if (readersUpdaters) {
          privileges = new Privilege[]{AccessPrivilege.listToPriv(Field.FIELD_NAME_READERS),
                  AccessPrivilege.listToPriv(Field.FIELD_NAME_UPDATERS)};
        } else {
          privileges = new Privilege[]{AccessPrivilege.listToPriv(fieldName)};
        }
      }
      
      int count = 0;
      for (Member member : members) {
        
        for (Privilege privilege : privileges) {
          if (assign) {
            changes += group.grantPriv(member.getSubject(), privilege, false) ? 1 : 0;
          } else {
            changes += group.revokePriv(member.getSubject(), privilege, false) ? 1 : 0;
          }
        }
        
        if (count++ < 5) {
          GrouperUserDataApi.recentlyUsedMemberAdd(GrouperUiUserData.grouperUiGroupNameForUserData(), 
              loggedInSubject, member);

        }
        
      }
      
      //reset the data (not really necessary, just in case)
      groupContainer.setPrivilegeGuiMembershipSubjectContainers(null);
  
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

      if (group.hasAdmin(loggedInSubject)) {
        filterPrivilegesHelper(request, response, group);
      } else {
        guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2Main.indexMain')"));
      }
  
      GrouperUserDataApi.recentlyUsedGroupAdd(GrouperUiUserData.grouperUiGroupNameForUserData(), 
          loggedInSubject, group);

    } catch (RuntimeException re) {
      if (GrouperUiUtils.vetoHandle(guiResponseJs, re)) {
        return;
      }
      throw re;
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  
  }

  /**
   * the filter button for privileges was pressed, or paging or sorting or something
   * @param request
   * @param response
   */
  public void filterPrivileges(HttpServletRequest request, HttpServletResponse response) {
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    try {
      grouperSession = GrouperSession.start(loggedInSubject);
  
  
      Group group = retrieveGroupHelper(request, AccessPrivilege.ADMIN).getGroup();
      
      if (group == null) {
        return;
      }
  
      filterPrivilegesHelper(request, response, group);
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }

  /**
   * the filter button for this groups memberships was pressed, or paging or sorting or something
   * @param request
   * @param response
   */
  public void filterThisGroupsMemberships(HttpServletRequest request, HttpServletResponse response) {
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    try {
      grouperSession = GrouperSession.start(loggedInSubject);
  
  
      Group group = retrieveGroupHelper(request, AccessPrivilege.UPDATE).getGroup();
      
      if (group == null) {
        return;
      }
  
      filterThisGroupsMembershipsHelper(request, response, group);
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }

  /**
   * the filter button was pressed for privileges, or paging or sorting, or view Group privileges or something
   * @param request
   * @param response
   * @param group 
   */
  private void filterPrivilegesHelper(HttpServletRequest request, HttpServletResponse response, Group group) {
    
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

    GuiPaging guiPaging = grouperRequestContainer.getGroupContainer().getPrivilegeGuiPaging();
    QueryOptions queryOptions = new QueryOptions();
    
    GrouperPagingTag2.processRequest(request, guiPaging, queryOptions);
    
    MembershipFinder membershipFinder = new MembershipFinder()
      .addGroupId(group.getId()).assignCheckSecurity(true)
      .assignFieldType(FieldType.ACCESS)
      .assignEnabled(true)
      .assignHasFieldForMember(true)
      .assignHasMembershipTypeForMember(true)
      .assignQueryOptionsForMember(queryOptions)
      .assignSplitScopeForMember(true);
    
    if (membershipType != null) {
      membershipFinder.assignMembershipType(membershipType);
    }

    if (privilegeField != null) {
      membershipFinder.assignField(privilegeField);
    }

    if (!StringUtils.isBlank(privilegeFilterText)) {
      membershipFinder.assignScopeForMember(privilegeFilterText);
    }

    //set of subjects, and what privs each subject has
    Set<MembershipSubjectContainer> results = membershipFinder
        .findMembershipResult().getMembershipSubjectContainers();
    
    //inherit from grouperAll or Groupersystem or privilege inheritance
    MembershipSubjectContainer.considerAccessPrivilegeInheritance(results);

    grouperRequestContainer.getGroupContainer().setPrivilegeGuiMembershipSubjectContainers(
        GuiMembershipSubjectContainer.convertFromMembershipSubjectContainers(results));

    guiPaging.setTotalRecordCount(queryOptions.getQueryPaging().getTotalRecordCount());

    
    guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#groupPrivilegeFilterResultsId", 
        "/WEB-INF/grouperUi2/group/groupPrivilegeContents.jsp"));
  
  }

  /**
   * view group privileges
   * @param request
   * @param response
   */
  public void groupPrivileges(HttpServletRequest request, HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    Group group = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      group = retrieveGroupHelper(request, AccessPrivilege.ADMIN).getGroup();
      
      if (group == null) {
        return;
      }
  
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/group/groupPrivileges.jsp"));
      filterPrivilegesHelper(request, response, group);

    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * delete group (show confirm screen)
   * @param request
   * @param response
   */
  public void groupDelete(HttpServletRequest request, HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    Group group = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      group = retrieveGroupHelper(request, AccessPrivilege.ADMIN).getGroup();
      
      if (group == null) {
        return;
      }

      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      
      if (GrouperUiConfig.retrieveConfig().propertyValueBoolean("uiV2.group.checkForFactorWhenDeletingGroup", true)) {
        Set<Composite> composites = CompositeFinder.findAsFactor(group);
        
        if (GrouperUtil.length(composites) > 0) {
          StringBuilder result = new StringBuilder();
          result.append(TextContainer.retrieveFromRequest().getText()
              .get("groupProblemDeleteWithCompositeFactor")).append(" ");
          boolean firstLine = true;
          for (Composite composite : composites) {
            
            try {
              if (!firstLine) {
                result.append(", ");
              }
              
              Group theGroup = composite.getOwnerGroup();
    
              GuiGroup guiGroup = new GuiGroup(theGroup);
              result.append(guiGroup.getShortLink());
              
            } catch (GroupNotFoundException gnfe) {
              result.append(TextContainer.retrieveFromRequest().getText().get("groupLabelNotAllowedToViewOwner"));
            }
            
            firstLine = false;
            
          }
          
          guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, result.toString()));
          return;
        }
      }
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/group/groupDelete.jsp"));
  
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * hit submit on the delete group screen
   * @param request
   * @param response
   */
  public void groupDeleteSubmit(HttpServletRequest request, HttpServletResponse response) {
  
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
  
    GrouperSession grouperSession = null;
  
    Group group = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      group = retrieveGroupHelper(request, AccessPrivilege.ADMIN).getGroup();
    
      if (group == null) {
        return;
      }
      
      String stemId = group.getParentUuid();
      
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
      try {
  
        //delete the group
        group.delete();
  
      } catch (InsufficientPrivilegeException ipe) {
        
        LOG.warn("Insufficient privilege exception for group delete: " + SubjectHelper.getPretty(loggedInSubject), ipe);
        
        //go to the view group screen
        guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2Group.viewGroup&groupId=" + group.getId() + "')"));
    
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("groupDeleteInsufficientPrivileges")));
        return;
  
      } catch (GroupDeleteException sde) {
        
        LOG.warn("Error deleting group: " + SubjectHelper.getPretty(loggedInSubject) + ", " + group, sde);
        
        //go to the view group screen
        guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2Group.viewGroup&groupId=" + group.getId() + "')"));
    
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("groupErrorCantDelete")));
  
        return;
  
      }
      
      //go to the view stem screen
      guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2Stem.viewStem&stemId=" + stemId + "')"));
  
      //lets show a success message on the new screen
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
          TextContainer.retrieveFromRequest().getText().get("groupDeleteSuccess")));
      
      GrouperUserDataApi.recentlyUsedGroupRemove(GrouperUiUserData.grouperUiGroupNameForUserData(), 
          loggedInSubject, group);

    } catch (RuntimeException re) {
      if (GrouperUiUtils.vetoHandle(GuiResponseJs.retrieveGuiResponseJs(), re)) {
        return;
      }
      throw re;

    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  
  }

  

  /**
   * new group submit
   * @param request
   * @param response
   */
  public void newGroupSubmit(HttpServletRequest request, HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();

    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();

    GrouperSession grouperSession = null;
  
    Group group = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);

      final boolean editIdChecked = GrouperUtil.booleanValue(request.getParameter("nameDifferentThanId[]"), false);
      final String displayExtension = request.getParameter("displayExtension");
      final String extension = editIdChecked ? request.getParameter("extension") : displayExtension;
      final String description = request.getParameter("description");
      final boolean adminChecked = GrouperUtil.booleanValue(request.getParameter("privileges_admins[]"), false);
      final boolean updateChecked = GrouperUtil.booleanValue(request.getParameter("privileges_updaters[]"), false);
      final boolean readChecked = GrouperUtil.booleanValue(request.getParameter("privileges_readers[]"), false);
      final boolean viewChecked = GrouperUtil.booleanValue(request.getParameter("privileges_viewers[]"), false);
      final boolean optinChecked = GrouperUtil.booleanValue(request.getParameter("privileges_optins[]"), false);
      final boolean optoutChecked = GrouperUtil.booleanValue(request.getParameter("privileges_optouts[]"), false);
      final boolean attrReadChecked = GrouperUtil.booleanValue(request.getParameter("privileges_groupAttrReaders[]"), false);
      final boolean attrUpdateChecked = GrouperUtil.booleanValue(request.getParameter("privileges_groupAttrUpdaters[]"), false);

      String groupType = request.getParameter("groupType[]");
      
      final TypeOfGroup typeOfGroup = TypeOfGroup.valueOfIgnoreCase(groupType, true);
      
      if (typeOfGroup != TypeOfGroup.group && typeOfGroup != TypeOfGroup.role) {
        throw new RuntimeException("Invalid group type, should be group or role: " + typeOfGroup);
      }

      String parentFolderId = request.getParameter("parentFolderComboName");
      
      //just get what they typed in
      if (StringUtils.isBlank(parentFolderId)) {
        parentFolderId = request.getParameter("parentFolderComboNameDisplay");
      }
      
      if (StringUtils.isBlank(parentFolderId)) {
        
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error,
            "#parentFolderComboErrorId",
            TextContainer.retrieveFromRequest().getText().get("groupCreateRequiredParentStemId")));
        return;
      }
      
      final Stem parentFolder = new StemFinder().assignPrivileges(NamingPrivilege.CREATE_PRIVILEGES)
          .assignSubject(loggedInSubject)
          .assignScope(parentFolderId).assignFindByUuidOrName(true).findStem();

      if (parentFolder == null) {
        
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error,
            "#parentFolderComboErrorId",
            TextContainer.retrieveFromRequest().getText().get("groupCreateCantFindParentStemId")));
        return;
        
      }
      
      if (StringUtils.isBlank(displayExtension)) {
        
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, 
            "#groupName",
            TextContainer.retrieveFromRequest().getText().get("groupCreateErrorDisplayExtensionRequired")));
        return;
        
      }

      if (StringUtils.isBlank(extension)) {
        
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, 
            "#groupId",
            TextContainer.retrieveFromRequest().getText().get("groupCreateErrorExtensionRequired")));
        return;
        
      }

      if (parentFolder.isRootStem()) {
        
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, 
            "#parentFolderComboErrorId",
            TextContainer.retrieveFromRequest().getText().get("groupCreateCantCreateInRoot")));
        return;
        
      }

      final String groupName = parentFolder.getName() + ":" + extension;
      
      //search as an admin to see if the group exists
      group = (Group)GrouperSession.callbackGrouperSession(grouperSession.internal_getRootSession(), new GrouperSessionHandler() {
        
        public Object callback(GrouperSession theGrouperSession) throws GrouperSessionException {
          
          return GroupFinder.findByName(theGrouperSession, groupName, false);
        }
      });

      if (group != null) {
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, 
            editIdChecked ? "#groupId" : "#groupName",
            TextContainer.retrieveFromRequest().getText().get("groupCreateCantCreateAlreadyExists")));
        return;
      }
      
      try {

        //create the group
        group = new GroupSave(grouperSession).assignName(groupName).assignSaveMode(SaveMode.INSERT)
            .assignDisplayExtension(displayExtension).assignDescription(description).assignTypeOfGroup(typeOfGroup)
            .assignPrivAllAdmin(adminChecked).assignPrivAllAttrRead(attrReadChecked)
            .assignPrivAllAttrUpdate(attrUpdateChecked).assignPrivAllOptin(optinChecked)
            .assignPrivAllOptout(optoutChecked).assignPrivAllRead(readChecked)
            .assignPrivAllUpdate(updateChecked).assignPrivAllView(viewChecked)
            .save();
  
      } catch (GrouperValidationException gve) {
        handleGrouperValidationException(guiResponseJs, gve);
        return;

        
      } catch (InsufficientPrivilegeException ipe) {
        
        LOG.warn("Insufficient privilege exception for group create: " + SubjectHelper.getPretty(loggedInSubject), ipe);
        
        //dont change screens
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("groupCreateInsufficientPrivileges")));
        return;

        
      } catch (Exception sde) {
        
        LOG.warn("Error creating group: " + SubjectHelper.getPretty(loggedInSubject) + ", " + group, sde);
        
        if (GrouperUiUtils.vetoHandle(guiResponseJs, sde)) {
          return;
        }
        
        //dont change screens
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("groupCreateError") 
            + ": " + GrouperUtil.xmlEscape(sde.getMessage(), true)));

        return;

      }

      //go to the view group screen
      guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2Group.viewGroup&groupId=" + group.getId() + "')"));

      //lets show a success message on the new screen
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
          TextContainer.retrieveFromRequest().getText().get("groupCreateSuccess")));

      GrouperUserDataApi.recentlyUsedGroupAdd(GrouperUiUserData.grouperUiGroupNameForUserData(), 
          loggedInSubject, group);
  
    } catch (RuntimeException re) {
      if (GrouperUiUtils.vetoHandle(GuiResponseJs.retrieveGuiResponseJs(), re)) {
        return;
      }
      throw re;
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * @param guiResponseJs
   * @param gve
   */
  private void handleGrouperValidationException(GuiResponseJs guiResponseJs,
      GrouperValidationException gve) {
    //  groupValidation_groupDescriptionTooLong = Error, group description is too long
    //  groupValidation_groupDisplayExtensionTooLong = Error, group name is too long
    //  groupValidation_groupExtensionTooLong = Error, group ID is too long
    //  groupValidation_groupDisplayNameTooLong = Error, the group name causes the path to be too long, please shorten it
    //  groupValidation_groupNameTooLong = Error, the group ID causes the ID path to be too long, please shorten it
    
    if (StringUtils.equals(Group.VALIDATION_GROUP_DESCRIPTION_TOO_LONG_KEY, gve.getGrouperValidationKey())) {
      
      guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, 
          "#groupDescription",
          TextContainer.retrieveFromRequest().getText().get("groupValidation_" + gve.getGrouperValidationKey())));
      return;
      
    } else if (StringUtils.equals(Group.VALIDATION_GROUP_EXTENSION_TOO_LONG_KEY, gve.getGrouperValidationKey())
        || StringUtils.equals(Group.VALIDATION_GROUP_NAME_TOO_LONG_KEY, gve.getGrouperValidationKey())) {

      guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, 
          "#groupId",
          TextContainer.retrieveFromRequest().getText().get("groupValidation_" + gve.getGrouperValidationKey())));
      return;
      
    } else if (StringUtils.equals(Group.VALIDATION_GROUP_DISPLAY_EXTENSION_TOO_LONG_KEY, gve.getGrouperValidationKey())
        || StringUtils.equals(Group.VALIDATION_GROUP_DISPLAY_NAME_TOO_LONG_KEY, gve.getGrouperValidationKey())) {

      guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, 
          "#groupName",
          TextContainer.retrieveFromRequest().getText().get("groupValidation_" + gve.getGrouperValidationKey())));
      return;
      
    } else {
      LOG.error("Non-fatal error, not expecting GrouperValidationException: " + gve.getGrouperValidationKey(), gve);
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, gve.getMessage()));
      return;
    }
  }

  
  /**
   * new group (show create screen)
   * @param request
   * @param response
   */
  public void newGroup(HttpServletRequest request, HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      //see if there is a stem id for this
      String objectStemId = request.getParameter("objectStemId");
      
      Pattern pattern = Pattern.compile("^[a-zA-Z0-9-_]+$");
      
      if (!StringUtils.isBlank(objectStemId) && pattern.matcher(objectStemId).matches()) {
        
        GrouperRequestContainer.retrieveFromRequestOrCreate().getStemContainer().setObjectStemId(objectStemId);
        
      }
      
      UiV2Stem.retrieveStemHelper(request, false, false, false).getStem();
      
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/group/newGroup.jsp"));
  
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * edit a group, show the edit screen
   * @param request
   * @param response
   */
  public void groupEdit(HttpServletRequest request, HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    Group group = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      group = retrieveGroupHelper(request, AccessPrivilege.ADMIN).getGroup();
      
      if (group == null) {
        return;
      }
  
      GrouperRequestContainer.retrieveFromRequestOrCreate().getGroupContainer().getGuiGroup().setShowBreadcrumbLink(true);
      GrouperRequestContainer.retrieveFromRequestOrCreate().getGroupContainer().getGuiGroup().setShowBreadcrumbLinkSeparator(false);
      
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/group/groupEdit.jsp"));
  
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * edit group submit
   * @param request
   * @param response
   */
  public void groupEditSubmit(HttpServletRequest request, HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
  
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();

    GrouperSession grouperSession = null;

    Group group = null;

    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);

      group = retrieveGroupHelper(request, AccessPrivilege.ADMIN).getGroup();
      
      if (group == null) {
        return;
      }

      final GrouperSession GROUPER_SESSION = grouperSession;
      
      final String extension = request.getParameter("extension");
      final String displayExtension = request.getParameter("displayExtension");
      final String description = request.getParameter("description");
      final boolean adminChecked = GrouperUtil.booleanValue(request.getParameter("privileges_admins[]"), false);
      final boolean updateChecked = GrouperUtil.booleanValue(request.getParameter("privileges_updaters[]"), false);
      final boolean readChecked = GrouperUtil.booleanValue(request.getParameter("privileges_readers[]"), false);
      final boolean viewChecked = GrouperUtil.booleanValue(request.getParameter("privileges_viewers[]"), false);
      final boolean optinChecked = GrouperUtil.booleanValue(request.getParameter("privileges_optins[]"), false);
      final boolean optoutChecked = GrouperUtil.booleanValue(request.getParameter("privileges_optouts[]"), false);
      final boolean attrReadChecked = GrouperUtil.booleanValue(request.getParameter("privileges_groupAttrReaders[]"), false);
      final boolean attrUpdateChecked = GrouperUtil.booleanValue(request.getParameter("privileges_groupAttrUpdaters[]"), false);
  
      String groupType = request.getParameter("groupType[]");
      
      final TypeOfGroup typeOfGroup = TypeOfGroup.valueOfIgnoreCase(groupType, true);
      
      if (typeOfGroup != TypeOfGroup.group && typeOfGroup != TypeOfGroup.role) {
        throw new RuntimeException("Invalid group type, should be group or role: " + typeOfGroup);
      }
      
      
      
      if (StringUtils.isBlank(displayExtension)) {
        
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, 
            "#groupName",
            TextContainer.retrieveFromRequest().getText().get("groupCreateErrorDisplayExtensionRequired")));
        return;
        
      }
  
      if (StringUtils.isBlank(extension)) {
        
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, 
            "#groupId",
            TextContainer.retrieveFromRequest().getText().get("groupCreateErrorExtensionRequired")));
        return;
        
      }
  
      try {
  
        //create the group
        GroupSave groupSave = new GroupSave(GROUPER_SESSION).assignUuid(group.getId())
            .assignSaveMode(SaveMode.UPDATE)
            .assignName(group.getParentStemName() + ":" + extension)
            .assignDisplayExtension(displayExtension).assignDescription(description).assignTypeOfGroup(typeOfGroup)
            .assignPrivAllAdmin(adminChecked).assignPrivAllAttrRead(attrReadChecked)
            .assignPrivAllAttrUpdate(attrUpdateChecked).assignPrivAllOptin(optinChecked)
            .assignPrivAllOptout(optoutChecked).assignPrivAllRead(readChecked)
            .assignPrivAllUpdate(updateChecked).assignPrivAllView(viewChecked);
        group = groupSave.save();
        
        //go to the view group screen
        guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2Group.viewGroup&groupId=" + group.getId() + "')"));
    
        //lets show a success message on the new screen
        if (groupSave.getSaveResultType() == SaveResultType.NO_CHANGE) {
          guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.info, 
              TextContainer.retrieveFromRequest().getText().get("groupEditNoChangeNote")));
        } else {
          guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
              TextContainer.retrieveFromRequest().getText().get("groupEditSuccess")));
        }
      
  
      } catch (GrouperValidationException gve) {
        handleGrouperValidationException(guiResponseJs, gve);
        return;

      } catch (InsufficientPrivilegeException ipe) {
        
        LOG.warn("Insufficient privilege exception for group edit: " + SubjectHelper.getPretty(loggedInSubject), ipe);
        
        //dont change screens
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("groupCreateInsufficientPrivileges")));
        return;
  
      } catch (Exception sde) {
        
        LOG.warn("Error edit group: " + SubjectHelper.getPretty(loggedInSubject) + ", " + group, sde);

        if (GrouperUiUtils.vetoHandle(guiResponseJs, sde)) {
          return;
        }

        //dont change screens
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("groupEditError") 
            + ": " + GrouperUtil.xmlEscape(sde.getMessage(), true)));
  
        return;
  
      }
    
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * copy group
   * @param request
   * @param response
   */
  public void groupCopy(HttpServletRequest request, HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    Group group = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      group = retrieveGroupHelper(request, AccessPrivilege.ADMIN).getGroup();
      
      if (group == null) {
        return;
      }
  
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/group/groupCopy.jsp"));
  
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * view audits for group
   * @param request
   * @param response
   */
  public void viewAudits(HttpServletRequest request, HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    Group group = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      group = retrieveGroupHelper(request, AccessPrivilege.ADMIN).getGroup();
      
      if (group == null) {
        return;
      }
  
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/group/groupViewAudits.jsp"));
  
      viewAuditsHelper(request, response, group);

    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * move group
   * @param request
   * @param response
   */
  public void groupMove(HttpServletRequest request, HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    Group group = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      group = retrieveGroupHelper(request, AccessPrivilege.ADMIN).getGroup();
      
      if (group == null) {
        return;
      }
  
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/group/groupMove.jsp"));
  
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * 
   * @param request
   * @param response
   */
  public void groupMoveSubmit(HttpServletRequest request, HttpServletResponse response) {
  
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
  
    GrouperSession grouperSession = null;
  
    Group group = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      group = retrieveGroupHelper(request, AccessPrivilege.ADMIN).getGroup();
    
      if (group == null) {
        return;
      }
  
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
      String parentFolderId = request.getParameter("parentFolderComboName");
      
      //just get what they typed in
      if (StringUtils.isBlank(parentFolderId)) {
        parentFolderId = request.getParameter("parentFolderComboNameDisplay");
      }
      
      boolean moveChangeAlternateNames = GrouperUtil.booleanValue(request.getParameter("moveChangeAlternateNames[]"), false);
      
      final Stem parentFolder = new StemFinder().assignPrivileges(NamingPrivilege.CREATE_PRIVILEGES)
          .assignSubject(loggedInSubject)
          .assignScope(parentFolderId).assignFindByUuidOrName(true).findStem();
      
      if (parentFolder == null) {
        
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("groupCopyCantFindParentStemId")));
        return;
        
      }
  
      //MCH 20131224: dont need this since we are searching by stemmed folders above
      
      try {
  
        //get the new folder that was created
        new GroupMove(group, parentFolder).assignAlternateName(moveChangeAlternateNames).save();
  
      } catch (InsufficientPrivilegeException ipe) {
        
        LOG.warn("Insufficient privilege exception for group move: " + SubjectHelper.getPretty(loggedInSubject), ipe);
        
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("groupMoveInsufficientPrivileges")));
        return;
  
      }
      
      //go to the view group screen
      guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2Group.viewGroup&groupId=" + group.getId() + "')"));
  
      //lets show a success message on the new screen
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
          TextContainer.retrieveFromRequest().getText().get("groupMoveSuccess")));
      
      GrouperUserDataApi.recentlyUsedGroupAdd(GrouperUiUserData.grouperUiGroupNameForUserData(), 
          loggedInSubject, group);
  
    } catch (RuntimeException re) {
      if (GrouperUiUtils.vetoHandle(GuiResponseJs.retrieveGuiResponseJs(), re)) {
        return;
      }
      throw re;
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  
  }

  /**
   * 
   * @param request
   * @param response
   */
  public void groupCopySubmit(HttpServletRequest request, HttpServletResponse response) {
  
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
  
    GrouperSession grouperSession = null;
  
    Group group = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      group = retrieveGroupHelper(request, AccessPrivilege.ADMIN).getGroup();
    
      if (group == null) {
        return;
      }
  
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
      String displayExtension = request.getParameter("displayExtension");
      String extension = request.getParameter("extension");
  
      boolean copyGroupAttributes = GrouperUtil.booleanValue(request.getParameter("copyGroupAttributes[]"), false);
      boolean copyListMemberships = GrouperUtil.booleanValue(request.getParameter("copyListMemberships[]"), false);
      boolean copyGroupPrivileges = GrouperUtil.booleanValue(request.getParameter("copyGroupPrivileges[]"), false);
      boolean copyListMembershipsInOtherGroups = GrouperUtil.booleanValue(request.getParameter("copyListMembershipsInOtherGroups[]"), false);
      boolean copyPrivsInOtherGroups = GrouperUtil.booleanValue(request.getParameter("copyPrivsInOtherGroups[]"), false);
      
      String parentFolderId = request.getParameter("parentFolderComboName");
      
      //just get what they typed in
      if (StringUtils.isBlank(parentFolderId)) {
        parentFolderId = request.getParameter("parentFolderComboNameDisplay");
      }
      
      if (StringUtils.isBlank(extension)) {
        extension = group.getExtension();
      }
      
      if (StringUtils.isBlank(displayExtension)) {
        displayExtension = group.getDisplayExtension();
      }
      
      final Stem parentFolder = StringUtils.isBlank(parentFolderId) ? null : new StemFinder()
          .assignPrivileges(NamingPrivilege.CREATE_PRIVILEGES)
          .assignSubject(loggedInSubject)
          .assignScope(parentFolderId).assignFindByUuidOrName(true).findStem();
      
      if (parentFolder == null) {
        
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("groupCopyCantFindParentStemId")));
  
        return;
        
      }
  
      Group newGroup = null;
      
      try {
  
        //get the new folder that was created
        newGroup = new GroupCopy(group, parentFolder).copyAttributes(copyGroupAttributes)
            .copyListGroupAsMember(copyListMembershipsInOtherGroups)
            .copyListMembersOfGroup(copyListMemberships)
            .copyPrivilegesOfGroup(copyGroupPrivileges)
            .copyGroupAsPrivilege(copyPrivsInOtherGroups)
            .setDisplayExtension(displayExtension)
            .setExtension(extension).save();
  
      } catch (InsufficientPrivilegeException ipe) {
        
        LOG.warn("Insufficient privilege exception for group copy: " + SubjectHelper.getPretty(loggedInSubject), ipe);
        
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("groupCopyInsufficientPrivileges")));
        return;
  
      }
      
      guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2Group.viewGroup&groupId=" + newGroup.getId() + "')"));
  
      //lets show a success message on the new screen
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
          TextContainer.retrieveFromRequest().getText().get("groupCopySuccess")));
      
      GrouperUserDataApi.recentlyUsedGroupAdd(GrouperUiUserData.grouperUiGroupNameForUserData(), 
          loggedInSubject, group);
      GrouperUserDataApi.recentlyUsedGroupAdd(GrouperUiUserData.grouperUiGroupNameForUserData(), 
          loggedInSubject, newGroup);
  
    } catch (RuntimeException re) {
      if (GrouperUiUtils.vetoHandle(GuiResponseJs.retrieveGuiResponseJs(), re)) {
        return;
      }
      throw re;
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  
  }

  /** logger */
  protected static final Log LOG = LogFactory.getLog(UiV2Group.class);


  /**
   * get the group from the request
   * @param request
   * @param requirePrivilege (view is automatic)
   * @return the group finder result
   */
  public static RetrieveGroupHelperResult retrieveGroupHelper(HttpServletRequest request, Privilege requirePrivilege) {
    
    return retrieveGroupHelper(request, requirePrivilege, true);
    
  }

  /**
   * get the group from the request
   * @param request
   * @param requirePrivilege (view is automatic)
   * @param errorIfNotFound will put an error on the screen if nothing passed in
   * @return the group finder result
   */
  public static RetrieveGroupHelperResult retrieveGroupHelper(HttpServletRequest request, Privilege requirePrivilege, boolean errorIfNotFound) {
  
    //initialize the bean
    GrouperRequestContainer grouperRequestContainer = GrouperRequestContainer.retrieveFromRequestOrCreate();
    
    RetrieveGroupHelperResult result = new RetrieveGroupHelperResult();
  
    GrouperSession grouperSession = GrouperSession.staticGrouperSession();

    GroupContainer groupContainer = grouperRequestContainer.getGroupContainer();

    Group group = null;
  
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();

    
    
    String groupId = request.getParameter("groupId");
    String groupIndex = request.getParameter("groupIndex");
    String groupName = request.getParameter("groupName");
    
    boolean addedError = false;
    
    if (!StringUtils.isBlank(groupId)) {
      group = GroupFinder.findByUuid(grouperSession, groupId, false);
    } else if (!StringUtils.isBlank(groupName)) {
      group = GroupFinder.findByName(grouperSession, groupName, false);
    } else if (!StringUtils.isBlank(groupIndex)) {
      long idIndex = GrouperUtil.longValue(groupIndex);
      group = GroupFinder.findByIdIndexSecure(idIndex, false, null);
    } else {
      
      //if viewing a subject, and that subject is a group, just show the group screen
      Subject subject = UiV2Subject.retrieveSubjectHelper(request, false);
      if (subject != null && GrouperSourceAdapter.groupSourceId().equals(subject.getSourceId())) {
        group = GroupFinder.findByUuid(grouperSession, subject.getId(), false);
      } else {
        if (errorIfNotFound) {
          guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
              TextContainer.retrieveFromRequest().getText().get("groupCantFindGroupId")));
          
          addedError = true;
        }
      }
    }
  
    if (group != null) {
      groupContainer.setGuiGroup(new GuiGroup(group));      
      boolean privsOk = true;

      if (requirePrivilege != null) {
        if (requirePrivilege.equals(AccessPrivilege.ADMIN) && !groupContainer.isCanAdmin()) {
          if (errorIfNotFound) {
            guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
                TextContainer.retrieveFromRequest().getText().get("groupNotAllowedToAdminGroup")));
            addedError = true;
            }
          privsOk = false;
        }
        else if (requirePrivilege.equals(AccessPrivilege.VIEW) && !groupContainer.isCanView()) {
          if (errorIfNotFound) {
            guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
                TextContainer.retrieveFromRequest().getText().get("groupNotAllowedToViewGroup")));
            addedError = true;
          }
          privsOk = false;
        }
        else if (requirePrivilege.equals(AccessPrivilege.READ) && !groupContainer.isCanRead()) {
          if (errorIfNotFound) {
            guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
                TextContainer.retrieveFromRequest().getText().get("groupNotAllowedToReadGroup")));
            addedError = true;
          }
          privsOk = false;
        } else if (requirePrivilege.equals(AccessPrivilege.OPTIN) && !groupContainer.isCanOptin()) {
          if (errorIfNotFound) {
            guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
                TextContainer.retrieveFromRequest().getText().get("groupNotAllowedToOptinGroup")));
            addedError = true;
          }
          privsOk = false;
        } else if (requirePrivilege.equals(AccessPrivilege.OPTOUT) && !groupContainer.isCanOptout()) {
          if (errorIfNotFound) {
            guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
                TextContainer.retrieveFromRequest().getText().get("groupNotAllowedToOptoutGroup")));
            addedError = true;
          }
          privsOk = false;
        } else if (requirePrivilege.equals(AccessPrivilege.UPDATE) && !groupContainer.isCanUpdate()) {
          if (errorIfNotFound) {
            guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
                TextContainer.retrieveFromRequest().getText().get("groupNotAllowedToUpdateGroup")));
            addedError = true;
          }
          privsOk = false;
        }  
      }
      
      if (privsOk) {
        result.setGroup(group);
      }

    } else {
      
      if (!addedError && (!StringUtils.isBlank(groupId) || !StringUtils.isBlank(groupName) || !StringUtils.isBlank(groupIndex))) {
        result.setAddedError(true);
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("groupCantFindGroup")));
        addedError = true;
      }
      
    }
    result.setAddedError(addedError);
  
    //go back to the main screen, cant find group
    if (addedError && errorIfNotFound) {
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/index/indexMain.jsp"));
    }

    return result;
  }

  
  
  /**
   * this groups memberships
   * @param request
   * @param response
   */
  public void thisGroupsMemberships(HttpServletRequest request, HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    Group group = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      group = retrieveGroupHelper(request, AccessPrivilege.VIEW).getGroup();
      
      if (group == null) {
        return;
      }
  
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/group/thisGroupsMemberships.jsp"));
      filterThisGroupsMembershipsHelper(request, response, group);

    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * the filter button was pressed of this groups memberships screen, 
   * or paging or sorting, or view Group or something
   * @param request
   * @param response
   * @param group 
   */
  private void filterThisGroupsMembershipsHelper(HttpServletRequest request, HttpServletResponse response, Group group) {
    
    GrouperRequestContainer grouperRequestContainer = GrouperRequestContainer.retrieveFromRequestOrCreate();
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    String filterText = request.getParameter("filterText");
    GroupContainer groupContainer = grouperRequestContainer.getGroupContainer();
    
    //if filtering by subjects that have a certain type
    String membershipTypeString = request.getParameter("membershipType");
    MembershipType membershipType = null;
    if (!StringUtils.isBlank(membershipTypeString)) {
      membershipType = MembershipType.valueOfIgnoreCase(membershipTypeString, true);
    }

    GuiPaging guiPaging = groupContainer.getGuiPaging();
    QueryOptions queryOptions = new QueryOptions();
    
    GrouperPagingTag2.processRequest(request, guiPaging, queryOptions);
    
    MembershipFinder membershipFinder = new MembershipFinder()
      .addMemberId(group.toMember().getId())
      .assignCheckSecurity(true)
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
  
    groupContainer.setGuiMembershipSubjectContainers(GuiMembershipSubjectContainer.convertFromMembershipSubjectContainers(results));
    
    guiPaging.setTotalRecordCount(queryOptions.getQueryPaging().getTotalRecordCount());
    
    guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#thisGroupsMembershipsFilterResultsId", 
        "/WEB-INF/grouperUi2/group/thisGroupsMembershipsContents.jsp"));
  
  }

  /**
   * the remove members button was pressed
   * @param request
   * @param response
   */
  public void removeMembers(HttpServletRequest request, HttpServletResponse response) {
  
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
  
    GrouperSession grouperSession = null;
  
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
    try {
      grouperSession = GrouperSession.start(loggedInSubject);
  
      final Group group = retrieveGroupHelper(request, AccessPrivilege.UPDATE).getGroup();
  
      if (group == null) {
        return;
      }
  
      final Set<String> membershipsIds = new HashSet<String>();
      
      for (int i=0;i<1000;i++) {
        String membershipId = request.getParameter("membershipRow_" + i + "[]");
        if (!StringUtils.isBlank(membershipId)) {
          membershipsIds.add(membershipId);
        }
      }
  
      if (membershipsIds.size() == 0) {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("groupMembershipsRemoveNoSubjectSelects")));
        return;
      }
      final int[] successes = new int[]{0};
      final int[] failures = new int[]{0};
      
      final int[] count = new int[]{0};
      
      //subject has update, so this operation as root in case removing affects the membership
      GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        public Object callback(GrouperSession grouperSession2) throws GrouperSessionException {
          for (String membershipId : membershipsIds) {
            try {
              Membership membership = new MembershipFinder().addMembershipId(membershipId).findMembership(true);

              Member member = membership.getMember();
              group.deleteMember(member, false);
              
              if (count[0]++ < 5 && group.canHavePrivilege(loggedInSubject, AccessPrivilege.VIEW.getName(), false)) {
                GrouperUserDataApi.recentlyUsedMemberAdd(GrouperUiUserData.grouperUiGroupNameForUserData(), 
                    loggedInSubject, member);

              }
              
              successes[0]++;
            } catch (Exception e) {
              LOG.warn("Error with membership: " + membershipId + ", user: " + loggedInSubject, e);
              failures[0]++;
            }
          }
          
          if (group.canHavePrivilege(loggedInSubject, AccessPrivilege.VIEW.getName(), false)) {
            GrouperUserDataApi.recentlyUsedGroupAdd(GrouperUiUserData.grouperUiGroupNameForUserData(), 
              loggedInSubject, group);
          }
          
          return null;
        }
      });
      
      GrouperRequestContainer.retrieveFromRequestOrCreate().getGroupContainer().setSuccessCount(successes[0]);
      GrouperRequestContainer.retrieveFromRequestOrCreate().getGroupContainer().setFailureCount(failures[0]);


      if (!group.canHavePrivilege(loggedInSubject, AccessPrivilege.UPDATE.getName(), false) 
          || !group.canHavePrivilege(loggedInSubject, AccessPrivilege.READ.getName(), false)) {
        guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2Main.indexMain')"));
      } else {
        filterHelper(request, response, group);
      }
      
      //put this after redirect
      if (failures[0] > 0) {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("groupDeleteMembersErrors")));
      } else {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
            TextContainer.retrieveFromRequest().getText().get("groupDeleteMembersSuccesses")));
      }

    } catch (RuntimeException re) {
      if (GrouperUiUtils.vetoHandle(GuiResponseJs.retrieveGuiResponseJs(), re)) {
        return;
      }
      throw re;
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * this groups group privileges
   * @param request
   * @param response
   */
  public void thisGroupsGroupPrivileges(HttpServletRequest request, HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    Group group = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      group = retrieveGroupHelper(request, AccessPrivilege.VIEW).getGroup();
      
      if (group == null) {
        return;
      }
  
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/group/thisGroupsGroupPrivileges.jsp"));
      filterThisGroupsGroupPrivilegesHelper(request, response, group);
  
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * the filter button for this groups privileges was pressed, or paging or sorting or something
   * @param request
   * @param response
   */
  public void filterThisGroupsGroupPrivileges(HttpServletRequest request, HttpServletResponse response) {
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    try {
      grouperSession = GrouperSession.start(loggedInSubject);
  
  
      Group group = retrieveGroupHelper(request, AccessPrivilege.VIEW).getGroup();
      
      if (group == null) {
        return;
      }
  
      filterThisGroupsGroupPrivilegesHelper(request, response, group);

    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }

  }

  /**
   * the filter button was pressed of this groups group privileges screen, 
   * or paging or sorting, or view Group or something
   * @param request
   * @param response
   * @param group 
   */
  private void filterThisGroupsGroupPrivilegesHelper(HttpServletRequest request, HttpServletResponse response, Group group) {

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
    
    GuiPaging guiPaging = grouperRequestContainer.getGroupContainer().getPrivilegeGuiPaging();
    QueryOptions queryOptions = new QueryOptions();
    
    GrouperPagingTag2.processRequest(request, guiPaging, queryOptions); 
    
    MembershipFinder membershipFinder = new MembershipFinder()
      .addSubject(group.toSubject()).assignCheckSecurity(true)
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

    grouperRequestContainer.getGroupContainer().setPrivilegeGuiMembershipSubjectContainers(
        GuiMembershipSubjectContainer.convertFromMembershipSubjectContainers(results));

    guiPaging.setTotalRecordCount(queryOptions.getQueryPaging().getTotalRecordCount());

    guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#thisGroupsGroupPrivilegesFilterResultsId", 
        "/WEB-INF/grouperUi2/group/thisGroupsGroupPrivilegesContents.jsp"));

  }

  /**
   * assign or remove a privilege from a group, on this groups privileges
   * @param request
   * @param response
   */
  public void thisGroupsPrivilegesAssignPrivilege(HttpServletRequest request, HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
  
    GrouperSession grouperSession = null;
    
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();

    try {
      grouperSession = GrouperSession.start(loggedInSubject);
  
      Group group = retrieveGroupHelper(request, AccessPrivilege.VIEW).getGroup();
  
      if (group == null) {
        return;
      }
  
      //?assign=false&groupId=${grouperRequestContainer.groupContainer.guiGroup.stem.id}&fieldName=${fieldName}&memberId=${guiMembershipSubjectContainer.guiMember.member.uuid}
      String assignString = request.getParameter("assign");
      boolean assign = GrouperUtil.booleanValue(assignString);
      String fieldName = request.getParameter("fieldName");
      String parentGroupId = request.getParameter("parentGroupId");
  
      Group parentGroup = GroupFinder.findByUuid(grouperSession, parentGroupId, false);
      
      if (parentGroup == null || !parentGroup.canHavePrivilege(loggedInSubject, AccessPrivilege.ADMIN.getName(), false)) {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
            TextContainer.retrieveFromRequest().getText().get("groupNotAllowedToAdminAnotherGroup")));
        filterThisGroupsGroupPrivilegesHelper(request, response, group);
        return;
      }
      
      Privilege privilege = AccessPrivilege.listToPriv(fieldName);
      
      if (privilege == null) {
        throw new RuntimeException("Why is privilege not found???? " + fieldName);
      }
      
      //if someone revoked in the meantime, who cares...
      if (assign) {
        parentGroup.grantPriv(group.toSubject(), privilege, false);
        
        //set a success message
        //messes up screen
        //guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
        //    TextContainer.retrieveFromRequest().getText().get("stemSuccessGrantedPrivilege")));
        
      } else {
        parentGroup.revokePriv(group.toSubject(), privilege, false);
        
        //messes up screen
        //set a success message
        //guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
        //    TextContainer.retrieveFromRequest().getText().get("stemSuccessRevokedPrivilege")));
      }
  
      filterThisGroupsGroupPrivilegesHelper(request, response, group);
  
      GrouperUserDataApi.recentlyUsedGroupAdd(GrouperUiUserData.grouperUiGroupNameForUserData(), 
          loggedInSubject, group);

    } catch (RuntimeException re) {
      if (GrouperUiUtils.vetoHandle(GuiResponseJs.retrieveGuiResponseJs(), re)) {
        return;
      }
      throw re;
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  
  
  }

  /**
   * submit the main form on the this groups privilege screen which can do batch operations on a number of rows
   * @param request
   * @param response
   */
  public void thisGroupsPrivilegesAssignPrivilegeBatch(HttpServletRequest request, HttpServletResponse response) {
  
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
  
    GrouperSession grouperSession = null;
    
    try {
      grouperSession = GrouperSession.start(loggedInSubject);
  
      Group group = retrieveGroupHelper(request, AccessPrivilege.VIEW).getGroup();
  
      if (group == null) {
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
            TextContainer.retrieveFromRequest().getText().get("thisGroupsPrivilegesErrorGroupRequired")));
        guiResponseJs.addAction(GuiScreenAction.newScript("guiScrollTop()"));
        filterThisGroupsGroupPrivilegesHelper(request, response, group);
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
            changes += parentGroup.grantPriv(group.toSubject(), privilege, false) ? 1 : 0;
          } else {
            changes += parentGroup.revokePriv(group.toSubject(), privilege, false) ? 1 : 0;
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
  
      filterThisGroupsGroupPrivilegesHelper(request, response, group);
  
      GrouperUserDataApi.recentlyUsedGroupAdd(GrouperUiUserData.grouperUiGroupNameForUserData(), 
          loggedInSubject, group);

    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  
  }

  /**
   * the filter button for this groups stem privileges was pressed, or paging or sorting or something
   * @param request
   * @param response
   */
  public void filterThisGroupsStemPrivileges(HttpServletRequest request, HttpServletResponse response) {
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    try {
      grouperSession = GrouperSession.start(loggedInSubject);
  
  
      Group group = retrieveGroupHelper(request, AccessPrivilege.VIEW).getGroup();
      
      if (group == null) {
        return;
      }
  
      filterThisGroupsStemPrivilegesHelper(request, response, group);
  
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  
  }

  /**
   * the filter button was pressed of this groups stem privileges screen, 
   * or paging or sorting, or view Group or something
   * @param request
   * @param response
   * @param group 
   */
  private void filterThisGroupsStemPrivilegesHelper(HttpServletRequest request, HttpServletResponse response, Group group) {
  
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
    
    GuiPaging guiPaging = grouperRequestContainer.getGroupContainer().getPrivilegeGuiPaging();
    QueryOptions queryOptions = new QueryOptions();
    GrouperPagingTag2.processRequest(request, guiPaging, queryOptions); 
      
    MembershipFinder membershipFinder = new MembershipFinder()
      .addSubject(group.toSubject()).assignCheckSecurity(true)
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
  
    grouperRequestContainer.getGroupContainer().setPrivilegeGuiMembershipSubjectContainers(
        GuiMembershipSubjectContainer.convertFromMembershipSubjectContainers(results));
  
    guiPaging.setTotalRecordCount(queryOptions.getQueryPaging().getTotalRecordCount());
  
    guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#thisGroupsStemPrivilegesFilterResultsId", 
        "/WEB-INF/grouperUi2/group/thisGroupsStemPrivilegesContents.jsp"));
  
  }

  /**
   * this groups stem privileges
   * @param request
   * @param response
   */
  public void thisGroupsStemPrivileges(HttpServletRequest request, HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    Group group = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      group = retrieveGroupHelper(request, AccessPrivilege.VIEW).getGroup();
      
      if (group == null) {
        return;
      }
  
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/group/thisGroupsStemPrivileges.jsp"));
      filterThisGroupsStemPrivilegesHelper(request, response, group);
  
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * assign or remove a privilege from a stem, on this groups privileges
   * @param request
   * @param response
   */
  public void thisGroupsPrivilegesAssignStemPrivilege(HttpServletRequest request, HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
  
    GrouperSession grouperSession = null;
    
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
    try {
      grouperSession = GrouperSession.start(loggedInSubject);
  
      Group group = retrieveGroupHelper(request, AccessPrivilege.VIEW).getGroup();
  
      if (group == null) {
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
        filterThisGroupsGroupPrivilegesHelper(request, response, group);
        return;
      }
      
      Privilege privilege = NamingPrivilege.listToPriv(fieldName);
      
      if (privilege == null) {
        throw new RuntimeException("Why is privilege not found???? " + fieldName);
      }
      
      //if someone revoked in the meantime, who cares...
      if (assign) {
        parentStem.grantPriv(group.toSubject(), privilege, false);
        
        //set a success message
        //messes up screen
        //guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
        //    TextContainer.retrieveFromRequest().getText().get("stemSuccessGrantedPrivilege")));
        
      } else {
        parentStem.revokePriv(group.toSubject(), privilege, false);
        
        //messes up screen
        //set a success message
        //guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
        //    TextContainer.retrieveFromRequest().getText().get("stemSuccessRevokedPrivilege")));
      }
  
      filterThisGroupsStemPrivilegesHelper(request, response, group);
  
      GrouperUserDataApi.recentlyUsedGroupAdd(GrouperUiUserData.grouperUiGroupNameForUserData(), 
          loggedInSubject, group);

    } catch (RuntimeException re) {
      if (GrouperUiUtils.vetoHandle(GuiResponseJs.retrieveGuiResponseJs(), re)) {
        return;
      }
      throw re;
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  
  }

  /**
   * submit the main form on the this groups stem privilege screen which can do batch operations on a number of rows
   * @param request
   * @param response
   */
  public void thisGroupsPrivilegesAssignStemPrivilegeBatch(HttpServletRequest request, HttpServletResponse response) {
  
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
  
    GrouperSession grouperSession = null;
    
    try {
      grouperSession = GrouperSession.start(loggedInSubject);
  
      Group group = retrieveGroupHelper(request, AccessPrivilege.VIEW).getGroup();
  
      if (group == null) {
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
            TextContainer.retrieveFromRequest().getText().get("thisGroupsPrivilegesErrorStemRequired")));
        guiResponseJs.addAction(GuiScreenAction.newScript("guiScrollTop()"));
        filterThisGroupsStemPrivilegesHelper(request, response, group);
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
            changes += parentStem.grantPriv(group.toSubject(), privilege, false) ? 1 : 0;
          } else {
            changes += parentStem.revokePriv(group.toSubject(), privilege, false) ? 1 : 0;
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
  
      filterThisGroupsStemPrivilegesHelper(request, response, group);
  
      GrouperUserDataApi.recentlyUsedGroupAdd(GrouperUiUserData.grouperUiGroupNameForUserData(), 
          loggedInSubject, group);

    } catch (RuntimeException re) {
      if (GrouperUiUtils.vetoHandle(GuiResponseJs.retrieveGuiResponseJs(), re)) {
        return;
      }
      throw re;
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  
  }

  /**
   * the filter button for this groups attribute def privileges was pressed, or paging or sorting or something
   * @param request
   * @param response
   */
  public void filterThisGroupsAttributeDefPrivileges(HttpServletRequest request, HttpServletResponse response) {
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    try {
      grouperSession = GrouperSession.start(loggedInSubject);
  
  
      Group group = retrieveGroupHelper(request, AccessPrivilege.VIEW).getGroup();
      
      if (group == null) {
        return;
      }
  
      filterThisGroupsAttributeDefPrivilegesHelper(request, response, group);
  
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  
  }

  /**
   * the filter button was pressed of this groups attributeDef privileges screen, 
   * or paging or sorting
   * @param request
   * @param response
   * @param group 
   */
  private void filterThisGroupsAttributeDefPrivilegesHelper(HttpServletRequest request, HttpServletResponse response, Group group) {
  
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
    
    GuiPaging guiPaging = grouperRequestContainer.getGroupContainer().getPrivilegeGuiPaging();
    QueryOptions queryOptions = new QueryOptions();

    GrouperPagingTag2.processRequest(request, guiPaging, queryOptions); 
    
    MembershipFinder membershipFinder = new MembershipFinder()
      .addSubject(group.toSubject()).assignCheckSecurity(true)
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
  
    grouperRequestContainer.getGroupContainer().setPrivilegeGuiMembershipSubjectContainers(
        GuiMembershipSubjectContainer.convertFromMembershipSubjectContainers(results));
  
    guiPaging.setTotalRecordCount(queryOptions.getQueryPaging().getTotalRecordCount());
  
    guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#thisGroupsAttributeDefPrivilegesFilterResultsId", 
        "/WEB-INF/grouperUi2/group/thisGroupsAttributeDefPrivilegesContents.jsp"));
  
  }

  /**
   * assign or remove a privilege from a group, on this groups privileges
   * @param request
   * @param response
   */
  public void thisGroupsPrivilegesAssignAttributeDefPrivilege(HttpServletRequest request, HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
  
    GrouperSession grouperSession = null;
    
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
    try {
      grouperSession = GrouperSession.start(loggedInSubject);
  
      Group group = retrieveGroupHelper(request, AccessPrivilege.VIEW).getGroup();
  
      if (group == null) {
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
            TextContainer.retrieveFromRequest().getText().get("groupNotAllowedToAdminAnotherAttributeDef")));
        filterThisGroupsAttributeDefPrivilegesHelper(request, response, group);
        return;
      }
      
      Privilege privilege = AttributeDefPrivilege.listToPriv(fieldName);
      
      if (privilege == null) {
        throw new RuntimeException("Why is privilege not found???? " + fieldName);
      }
      
      //if someone revoked in the meantime, who cares...
      if (assign) {
        parentAttributeDef.getPrivilegeDelegate().grantPriv(group.toSubject(), privilege, false);
        
        //set a success message
        //messes up screen
        //guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
        //    TextContainer.retrieveFromRequest().getText().get("stemSuccessGrantedPrivilege")));
        
      } else {
        parentAttributeDef.getPrivilegeDelegate().revokePriv(group.toSubject(), privilege, false);
        
        //messes up screen
        //set a success message
        //guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
        //    TextContainer.retrieveFromRequest().getText().get("stemSuccessRevokedPrivilege")));
      }
  
      filterThisGroupsAttributeDefPrivilegesHelper(request, response, group);
  
      GrouperUserDataApi.recentlyUsedGroupAdd(GrouperUiUserData.grouperUiGroupNameForUserData(), 
          loggedInSubject, group);

    } catch (RuntimeException re) {
      if (GrouperUiUtils.vetoHandle(GuiResponseJs.retrieveGuiResponseJs(), re)) {
        return;
      }
      throw re;
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  
  
  }

  /**
   * submit the main form on the this groups privilege screen which can do batch operations on a number of rows
   * @param request
   * @param response
   */
  public void thisGroupsPrivilegesAssignAttributeDefPrivilegeBatch(HttpServletRequest request, HttpServletResponse response) {
  
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
  
    GrouperSession grouperSession = null;
    
    try {
      grouperSession = GrouperSession.start(loggedInSubject);
  
      Group group = retrieveGroupHelper(request, AccessPrivilege.VIEW).getGroup();
  
      if (group == null) {
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
            TextContainer.retrieveFromRequest().getText().get("thisGroupsPrivilegesErrorAttributeDefRequired")));
        guiResponseJs.addAction(GuiScreenAction.newScript("guiScrollTop()"));
        filterThisGroupsAttributeDefPrivilegesHelper(request, response, group);
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
            changes += parentAttributeDef.getPrivilegeDelegate().grantPriv(group.toSubject(), privilege, false) ? 1 : 0;
          } else {
            changes += parentAttributeDef.getPrivilegeDelegate().revokePriv(group.toSubject(), privilege, false) ? 1 : 0;
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
  
      filterThisGroupsAttributeDefPrivilegesHelper(request, response, group);
  
      GrouperUserDataApi.recentlyUsedGroupAdd(GrouperUiUserData.grouperUiGroupNameForUserData(), 
          loggedInSubject, group);

    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  
  }

  /**
   * this groups attributeDef privileges
   * @param request
   * @param response
   */
  public void thisGroupsAttributeDefPrivileges(HttpServletRequest request, HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    Group group = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      group = retrieveGroupHelper(request, AccessPrivilege.VIEW).getGroup();
      
      if (group == null) {
        return;
      }
  
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/group/thisGroupsAttributeDefPrivileges.jsp"));
      filterThisGroupsAttributeDefPrivilegesHelper(request, response, group);
  
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * combo filter create group folder.  Note, this cannot be composite (since you cant
   * add members to a composite)
   * @param request
   * @param response
   */
  public void groupUpdateFilter(final HttpServletRequest request, HttpServletResponse response) {
  
    //run the combo logic
    DojoComboLogic.logic(request, response, new DojoComboQueryLogicBase<Group>() {
  
      /**
       * 
       */
      @Override
      public Group lookup(HttpServletRequest localRequest, GrouperSession grouperSession, String query) {
        Subject loggedInSubject = grouperSession.getSubject();
        Group theGroup = new GroupFinder().assignPrivileges(AccessPrivilege.UPDATE_PRIVILEGES)
            .assignSubject(loggedInSubject).assignCompositeOwner(false)
            .assignFindByUuidOrName(true).assignScope(query).findGroup();
        return theGroup;
      }
  
      /**
       * 
       */
      @Override
      public Collection<Group> search(HttpServletRequest localRequest, GrouperSession grouperSession, String query) {
        Subject loggedInSubject = grouperSession.getSubject();
        int groupComboSize = GrouperUiConfig.retrieveConfig().propertyValueInt("uiV2.groupComboboxResultSize", 200);
        QueryOptions queryOptions = QueryOptions.create(null, null, 1, groupComboSize);
        return new GroupFinder().assignPrivileges(AccessPrivilege.UPDATE_PRIVILEGES)
            .assignScope(query).assignSubject(loggedInSubject).assignCompositeOwner(false)
            .assignSplitScope(true).assignQueryOptions(queryOptions).findGroups();
      }
  
      /**
       * 
       * @param t
       * @return id
       */
      @Override
      public String retrieveId(GrouperSession grouperSession, Group t) {
        return t.getId();
      }
      
      /**
       * 
       */
      @Override
      public String retrieveLabel(GrouperSession grouperSession, Group t) {
        return t.getDisplayName();
      }
  
      /**
       * 
       */
      @Override
      public String retrieveHtmlLabel(GrouperSession grouperSession, Group t) {
        //description could be null?
        String label = GrouperUiUtils.escapeHtml(t.getDisplayName(), true);
        String htmlLabel = "<img src=\"../../grouperExternal/public/assets/images/group.gif\" /> " + label;
        return htmlLabel;
      }
  
    });
    
  }

  /**
   * filter audits for group
   * @param request
   * @param response
   */
  public void viewAuditsFilter(HttpServletRequest request, HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    Group group = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      group = retrieveGroupHelper(request, AccessPrivilege.ADMIN).getGroup();
      
      if (group == null) {
        return;
      }
  
      viewAuditsHelper(request, response, group);
  
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * the audit filter button was pressed, or paging or sorting, or view audits or something
   * @param request
   * @param response
   * @param group 
   */
  private void viewAuditsHelper(HttpServletRequest request, HttpServletResponse response, Group group) {
    
    GrouperRequestContainer grouperRequestContainer = GrouperRequestContainer.retrieveFromRequestOrCreate();
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();

    //all, on, before, between, or since
    String filterTypeString = request.getParameter("filterType");
  
    if (StringUtils.isBlank(filterTypeString)) {
      filterTypeString = "all";
    }
    
    String filterFromDateString = request.getParameter("filterFromDate");
    String filterToDateString = request.getParameter("filterToDate");

    //massage dates
    if (StringUtils.equals(filterTypeString, "all")) {
      guiResponseJs.addAction(GuiScreenAction.newFormFieldValue("filterFromDate", ""));
      filterFromDateString = null;
      guiResponseJs.addAction(GuiScreenAction.newFormFieldValue("filterToDate", ""));
      filterToDateString = null;
    } else if (StringUtils.equals(filterTypeString, "on")) {

      guiResponseJs.addAction(GuiScreenAction.newFormFieldValue("filterToDate", ""));
      filterToDateString = null;
    } else if (StringUtils.equals(filterTypeString, "before")) {
      guiResponseJs.addAction(GuiScreenAction.newFormFieldValue("filterToDate", ""));
      filterToDateString = null;
    } else if (StringUtils.equals(filterTypeString, "between")) {
    } else if (StringUtils.equals(filterTypeString, "since")) {
      guiResponseJs.addAction(GuiScreenAction.newFormFieldValue("filterToDate", ""));
      filterToDateString = null;
    } else {
      //should never happen
      throw new RuntimeException("Not expecting filterType string: " + filterTypeString);
    }

    Date filterFromDate = null;
    Date filterToDate = null;

    if (StringUtils.equals(filterTypeString, "on") || StringUtils.equals(filterTypeString, "before")
        || StringUtils.equals(filterTypeString, "between") || StringUtils.equals(filterTypeString, "since")) {
      if (StringUtils.isBlank(filterFromDateString)) {
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error,
            "#from-date",
            TextContainer.retrieveFromRequest().getText().get("groupAuditLogFilterFromDateRequired")));
        return;
      }
      try {
        filterFromDate = GrouperUtil.stringToTimestamp(filterFromDateString);
      } catch (Exception e) {
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error,
            "#from-date",
            TextContainer.retrieveFromRequest().getText().get("groupAuditLogFilterFromDateInvalid")));
        return;
      }
    }
    if (StringUtils.equals(filterTypeString, "between")) {
      if (StringUtils.isBlank(filterToDateString)) {
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error,
            "#to-date",
            TextContainer.retrieveFromRequest().getText().get("groupAuditLogFilterToDateRequired")));
        return;
      }
      try {
        filterToDate = GrouperUtil.stringToTimestamp(filterToDateString);
      } catch (Exception e) {
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error,
            "#to-date",
            TextContainer.retrieveFromRequest().getText().get("groupAuditLogFilterToDateInvalid")));
        return;
      }
    }
    
    boolean extendedResults = false;

    {
      String showExtendedResultsString = request.getParameter("showExtendedResults[]");
      if (!StringUtils.isBlank(showExtendedResultsString)) {
        extendedResults = GrouperUtil.booleanValue(showExtendedResultsString);
      }
    }
    
    GroupContainer groupContainer = grouperRequestContainer.getGroupContainer();
    
    GuiPaging guiPaging = groupContainer.getGuiPaging();
    QueryOptions queryOptions = new QueryOptions();
  
    GrouperPagingTag2.processRequest(request, guiPaging, queryOptions);
  
    UserAuditQuery query = new UserAuditQuery();

    //process dates
    if (StringUtils.equals(filterTypeString, "on")) {

      query.setOnDate(filterFromDate);
    } else  if (StringUtils.equals(filterTypeString, "between")) {
      query.setFromDate(filterFromDate);
      query.setToDate(filterToDate);
    } else  if (StringUtils.equals(filterTypeString, "since")) {
      query.setFromDate(filterFromDate);
    } else  if (StringUtils.equals(filterTypeString, "before")) {
      query.setToDate(filterToDate);
    }
    
    query.setQueryOptions(queryOptions);

    queryOptions.sortDesc("lastUpdatedDb");
    
    GuiSorting guiSorting = new GuiSorting(queryOptions.getQuerySort());
    groupContainer.setGuiSorting(guiSorting);

    guiSorting.processRequest(request);
    
    query.addAuditTypeFieldValue("groupId", group.getId());

    List<AuditEntry> auditEntries = query.execute();

    groupContainer.setGuiAuditEntries(GuiAuditEntry.convertFromAuditEntries(auditEntries));

    guiPaging.setTotalRecordCount(queryOptions.getQueryPaging().getTotalRecordCount());

    if (GrouperUtil.length(auditEntries) == 0) {
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.info,
          TextContainer.retrieveFromRequest().getText().get("groupAuditLogNoEntriesFound")));
    }
    
    groupContainer.setAuditExtendedResults(extendedResults);
    guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#groupAuditFilterResultsId", 
        "/WEB-INF/grouperUi2/group/groupViewAuditsContents.jsp"));
  
  }

  /**
   * remove all members submit
   * @param request
   * @param response
   */
  public void groupRemoveAllMembersSubmit(HttpServletRequest request, HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
  
    GrouperSession grouperSession = null;
  
    Group group = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      group = retrieveGroupHelper(request, AccessPrivilege.UPDATE).getGroup();
    
      if (group == null) {
        return;
      }
      
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
  
      if(group.hasComposite()) {
        
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("groupProblemWithComposite")));
        return;
      }

      Set<Member> members = group.getImmediateMembers();
      for (Member member : members) {
        group.deleteMember(member);
      }
      
      //go to the view group screen
      guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2Group.viewGroup&groupId=" + group.getId() + "')"));
  
      //lets show a success message on the new screen
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
          TextContainer.retrieveFromRequest().getText().get("groupRemoveMembersSuccess")));
  
      GrouperUserDataApi.recentlyUsedGroupAdd(GrouperUiUserData.grouperUiGroupNameForUserData(), 
          loggedInSubject, group);

    } catch (RuntimeException re) {
      if (GrouperUiUtils.vetoHandle(GuiResponseJs.retrieveGuiResponseJs(), re)) {
        return;
      }
      throw re;
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }

  }
  
  /**
   * remove all members from a group (show confirm screen)
   * @param request
   * @param response
   */
  public void groupRemoveAllMembers(HttpServletRequest request, HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    Group group = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      group = retrieveGroupHelper(request, AccessPrivilege.UPDATE).getGroup();
      
      if (group == null) {
        return;
      }
  
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/group/groupRemoveMembers.jsp"));
  
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * modal search form results for left group factor
   * @param request
   * @param response
   */
  public void leftGroupFactorSearch(HttpServletRequest request, HttpServletResponse response) {
    
    GrouperRequestContainer grouperRequestContainer = GrouperRequestContainer.retrieveFromRequestOrCreate();
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
    GrouperSession grouperSession = null;
  
    GroupContainer groupContainer = grouperRequestContainer.getGroupContainer();
    
    try {
      grouperSession = GrouperSession.start(loggedInSubject);
  
      String searchString = request.getParameter("leftFactorSearchName");
      
      boolean searchOk = GrouperUiUtils.searchStringValid(searchString);
      if (!searchOk) {
        
        guiResponseJs.addAction(GuiScreenAction.newInnerHtml("#leftFactorGroupResults", 
            TextContainer.retrieveFromRequest().getText().get("groupCompositeNotEnoughChars")));
        return;
      }

      String matchExactIdString = request.getParameter("matchExactId[]");
      boolean matchExactId = GrouperUtil.booleanValue(matchExactIdString, false);

      GuiPaging guiPaging = groupContainer.getGuiPaging();
      QueryOptions queryOptions = new QueryOptions();

      GrouperPagingTag2.processRequest(request, guiPaging, queryOptions); 

      Set<Group> groups = null;
    
    
      GroupFinder groupFinder = new GroupFinder().assignPrivileges(AccessPrivilege.READ_PRIVILEGES)
        .assignScope(searchString).assignSplitScope(true).assignQueryOptions(queryOptions);
      
      if (matchExactId) {
        groupFinder.assignFindByUuidOrName(true);
      }
      
      groups = groupFinder.findGroups();
      
      guiPaging.setTotalRecordCount(queryOptions.getQueryPaging().getTotalRecordCount());
      
      if (GrouperUtil.length(groups) == 0) {

        guiResponseJs.addAction(GuiScreenAction.newInnerHtml("#leftFactorGroupResults", 
            TextContainer.retrieveFromRequest().getText().get("groupCompositeSearchNoGroupsFound")));
        return;
      }
      
      Set<GuiGroup> guiGroups = GuiGroup.convertFromGroups(groups);
      
      groupContainer.setGuiGroups(guiGroups);
  
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#leftFactorGroupResults", 
          "/WEB-INF/grouperUi2/group/groupCompositeLeftFactorSearchResults.jsp"));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }

  /**
   * combobox results for add composite factor filter search
   * @param request
   * @param response
   */
  public void groupCompositeFactorFilter(HttpServletRequest request, HttpServletResponse response) {
    this.groupReadFilter(request, response);
  }
  
  /**
   * edit a group composite, show the edit composite screen
   * @param request
   * @param response
   */
  public void groupEditComposite(HttpServletRequest request, HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    Group group = null;
  
    try {

      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();

      grouperSession = GrouperSession.start(loggedInSubject);
  
      group = retrieveGroupHelper(request, AccessPrivilege.UPDATE).getGroup();
      
      if (group == null) {
        return;
      }

      if (!group.canHavePrivilege(loggedInSubject, AccessPrivilege.READ.getName(), false)) {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("groupNotAllowedToReadGroup")));
        return;
      }

      if (group.isHasMembers()) {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("groupCompositeErrorCannotHaveMembers")));
        return;
      }

      GroupContainer groupContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getGroupContainer();
      groupContainer.getGuiGroup().setShowBreadcrumbLink(true);
      groupContainer.getGuiGroup().setShowBreadcrumbLinkSeparator(false);
      
      groupContainer.setCompositeOwnerGuiGroup(groupContainer.getGuiGroup());

      Composite composite = group.getComposite(false);
      
      if (composite != null) {
        
        groupContainer.setCompositeLeftFactorGuiGroup(new GuiGroup(composite.getLeftGroup()));
        groupContainer.setCompositeRightFactorGuiGroup(new GuiGroup(composite.getRightGroup()));
        
      }
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/group/groupEditComposite.jsp"));
  
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * combo filter read group folder
   * @param request
   * @param response
   */
  public void groupReadFilter(final HttpServletRequest request, HttpServletResponse response) {
  
    //run the combo logic
    DojoComboLogic.logic(request, response, new DojoComboQueryLogicBase<Group>() {
  
      /**
       * 
       */
      @Override
      public Group lookup(HttpServletRequest localRequest, GrouperSession grouperSession, String query) {
        Subject loggedInSubject = grouperSession.getSubject();
        Group theGroup = new GroupFinder().assignPrivileges(AccessPrivilege.READ_PRIVILEGES)
            .assignSubject(loggedInSubject)
            .assignFindByUuidOrName(true).assignScope(query).findGroup();
        return theGroup;
      }
  
      /**
       * 
       */
      @Override
      public Collection<Group> search(HttpServletRequest localRequest, GrouperSession grouperSession, String query) {
        Subject loggedInSubject = grouperSession.getSubject();
        int groupComboSize = GrouperUiConfig.retrieveConfig().propertyValueInt("uiV2.groupComboboxResultSize", 200);
        QueryOptions queryOptions = QueryOptions.create(null, null, 1, groupComboSize);
        return new GroupFinder().assignPrivileges(AccessPrivilege.READ_PRIVILEGES)
            .assignScope(query).assignSubject(loggedInSubject)
            .assignSplitScope(true).assignQueryOptions(queryOptions).findGroups();
      }
  
      /**
       * 
       * @param t
       * @return id
       */
      @Override
      public String retrieveId(GrouperSession grouperSession, Group t) {
        return t.getId();
      }
      
      /**
       * 
       */
      @Override
      public String retrieveLabel(GrouperSession grouperSession, Group t) {
        return t.getDisplayName();
      }
  
      /**
       * 
       */
      @Override
      public String retrieveHtmlLabel(GrouperSession grouperSession, Group t) {
        //description could be null?
        String label = GrouperUiUtils.escapeHtml(t.getDisplayName(), true);
        String htmlLabel = "<img src=\"../../grouperExternal/public/assets/images/group.gif\" /> " + label;
        return htmlLabel;
      }
  
    });
    
  }

  /**
   * modal search form results for right group factor
   * @param request
   * @param response
   */
  public void rightGroupFactorSearch(HttpServletRequest request, HttpServletResponse response) {
    
    GrouperRequestContainer grouperRequestContainer = GrouperRequestContainer.retrieveFromRequestOrCreate();
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
    GrouperSession grouperSession = null;
  
    GroupContainer groupContainer = grouperRequestContainer.getGroupContainer();
    
    try {
      grouperSession = GrouperSession.start(loggedInSubject);
  
      String searchString = request.getParameter("rightFactorSearchName");
      
      boolean searchOk = GrouperUiUtils.searchStringValid(searchString);
      if (!searchOk) {
        
        guiResponseJs.addAction(GuiScreenAction.newInnerHtml("#rightFactorGroupResults", 
            TextContainer.retrieveFromRequest().getText().get("groupCompositeNotEnoughChars")));
        return;
      }
  
      String matchExactIdString = request.getParameter("matchExactId[]");
      boolean matchExactId = GrouperUtil.booleanValue(matchExactIdString, false);
  
      GuiPaging guiPaging = groupContainer.getGuiPaging();
      QueryOptions queryOptions = new QueryOptions();
  
      GrouperPagingTag2.processRequest(request, guiPaging, queryOptions); 
  
      Set<Group> groups = null;
    
    
      GroupFinder groupFinder = new GroupFinder().assignPrivileges(AccessPrivilege.READ_PRIVILEGES)
        .assignScope(searchString).assignSplitScope(true).assignQueryOptions(queryOptions);
      
      if (matchExactId) {
        groupFinder.assignFindByUuidOrName(true);
      }
      
      groups = groupFinder.findGroups();
      
      guiPaging.setTotalRecordCount(queryOptions.getQueryPaging().getTotalRecordCount());
      
      if (GrouperUtil.length(groups) == 0) {
  
        guiResponseJs.addAction(GuiScreenAction.newInnerHtml("#rightFactorGroupResults", 
            TextContainer.retrieveFromRequest().getText().get("groupCompositeSearchNoGroupsFound")));
        return;
      }
      
      Set<GuiGroup> guiGroups = GuiGroup.convertFromGroups(groups);
      
      groupContainer.setGuiGroups(guiGroups);
  
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#rightFactorGroupResults", 
          "/WEB-INF/grouperUi2/group/groupCompositeRightFactorSearchResults.jsp"));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }

  /**
   * edit group composite submit
   * @param request
   * @param response
   */
  public void groupEditCompositeSubmit(HttpServletRequest request, HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
  
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
    GrouperSession grouperSession = null;
  
    Group group = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      group = retrieveGroupHelper(request, AccessPrivilege.UPDATE).getGroup();
      
      if (group == null) {
        return;
      }
  
      //see if composite, should submit true or false
      boolean userSelectedComposite = GrouperUtil.booleanValue(request.getParameter("groupComposite[]"));
      
      if (!userSelectedComposite) {
        
        if (!group.isHasComposite()) {

          guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.info, 
              TextContainer.retrieveFromRequest().getText().get("groupCompositeNoteNoChangesMade")));
          return;
          
        }
        
        //we need to remove the composite
        group.deleteCompositeMember();

        //go back to view group
        guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2Group.viewGroup&groupId=" + group.getId() + "');"));

        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
            TextContainer.retrieveFromRequest().getText().get("groupCompositeSuccessRemovedComposite")));


        return;
      }

      //this should never happen unless race condition
      if (group.isHasMembers()) {
        //go back to view group
        guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2Group.viewGroup&groupId=" + group.getId() + "');"));

        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("groupCompositeErrorCannotHaveMembers")));
        
      }
      
      Composite composite = group.getComposite(false);

      // Get left group and validate
      String leftFactorGroupId = request.getParameter("groupCompositeLeftFactorComboName");
      
      Group leftFactorGroup = StringUtils.isBlank(leftFactorGroupId) ? null : new GroupFinder()
          .assignScope(leftFactorGroupId).assignPrivileges(AccessPrivilege.READ_PRIVILEGES)
          .assignFindByUuidOrName(true).assignSubject(loggedInSubject).findGroup();
      
      if (leftFactorGroup == null) {
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, 
          "#groupCompositeLeftFactorComboErrorId",
          TextContainer.retrieveFromRequest().getText().get("groupCompositeErrorLeftGroupProblem")));
        return;
        
        
      }
      
      // get operation and validate
      String compositeTypeString = request.getParameter("compositeOperation");
      CompositeType compositeType = StringUtils.isBlank(compositeTypeString) ? null : CompositeType.valueOfIgnoreCase(compositeTypeString);

      //we dont allow new groups to have union, or switch to union.  shouldnt be possible unles someone is hacking...
      if (compositeType != null && compositeType == CompositeType.UNION) {
        if (composite == null || composite.getType() != CompositeType.UNION) {
          compositeType = null;
        }
      }
      
      if (compositeType == null) {
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, 
            "#compositeOperationId",
            TextContainer.retrieveFromRequest().getText().get("groupCompositeErrorOperationRequired")));
          return;
      }

      // get right group and validate
      String rightFactorGroupId = request.getParameter("groupCompositeRightFactorComboName");
      
      Group rightFactorGroup = StringUtils.isBlank(rightFactorGroupId) ? null : new GroupFinder()
          .assignScope(rightFactorGroupId).assignPrivileges(AccessPrivilege.READ_PRIVILEGES)
          .assignFindByUuidOrName(true).assignSubject(loggedInSubject).findGroup();
      
      if (rightFactorGroup == null) {
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, 
          "#groupCompositeRightFactorComboErrorId",
          TextContainer.retrieveFromRequest().getText().get("groupCompositeErrorRightGroupProblem")));
        return;
      }

      //lets see if no changes
      if (composite != null && StringUtils.equals(composite.getLeftFactorUuid(), leftFactorGroup.getId())
          && composite.getType() == compositeType && StringUtils.equals(composite.getRightFactorUuid(), rightFactorGroup.getId())) {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.info, 
            TextContainer.retrieveFromRequest().getText().get("groupCompositeNoteNoChangesMade")));
        return;
        
      }

      //to edit a composite, delete and add
      if (composite != null) {
        group.deleteCompositeMember();
      }
      //create composite
      group.addCompositeMember(compositeType, leftFactorGroup, rightFactorGroup);

      
      //go back to view group
      guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2Group.viewGroup&groupId=" + group.getId() + "');"));

      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
          TextContainer.retrieveFromRequest().getText().get("groupCompositeSuccess")));

      GrouperUserDataApi.recentlyUsedGroupAdd(GrouperUiUserData.grouperUiGroupNameForUserData(), 
          loggedInSubject, group);
      GrouperUserDataApi.recentlyUsedGroupAdd(GrouperUiUserData.grouperUiGroupNameForUserData(), 
          loggedInSubject, leftFactorGroup);
      GrouperUserDataApi.recentlyUsedGroupAdd(GrouperUiUserData.grouperUiGroupNameForUserData(), 
          loggedInSubject, rightFactorGroup);
    
    } catch (RuntimeException re) {
      if (GrouperUiUtils.vetoHandle(GuiResponseJs.retrieveGuiResponseJs(), re)) {
        return;
      }
      throw re;
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * force grouperLoader to update this loader group
   * don't throw exception, display success or error message directly on New Ui screen
   * @param request
   * @param response
   */
  public void updateLoaderGroup(HttpServletRequest request, HttpServletResponse response) {

    if (!GrouperUiConfig.retrieveConfig().propertyValueBoolean("uiV2.group.allowGroupAdminsToRefreshLoaderJobs", true)) {
      throw new RuntimeException("Cant refresh loader groups from UI due to config param uiV2.group.allowGroupAdminsToRefreshLoaderJobs set to false");
    }
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();

    GrouperSession grouperSession = null;

    Group group = null;

    String result = "";

    try {

      grouperSession = GrouperSession.start(loggedInSubject);

      group = retrieveGroupHelper(request, AccessPrivilege.ADMIN).getGroup();

      if (group == null) {
        return;
      }

      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();

      try {
        final Group GROUP = group;
        result = (String)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
          
          public Object callback(GrouperSession rootGrouperSession) throws GrouperSessionException {
            return GrouperLoader.runJobOnceForGroup(rootGrouperSession, GROUP);
          }
        });
         
      } catch (Exception e) {

        LOG.error("Error running loader job from ui for group: " + group.getName(), e);
        
        //lets show an error message on the new screen  
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error,
            TextContainer.retrieveFromRequest().getText().get("loaderGroupUpdateError") + "<br />"
                + e.getMessage()));
        return;
      }

      result = StringUtils.trimToEmpty(result);
      //see if we can translate this
      // loader ran successfully, inserted 2 memberships, deleted 0 memberships, total membership count: 2
      Pattern pattern = Pattern.compile("^loader ran (successfully|with subject problems), inserted (\\d+) memberships, deleted (\\d+) memberships, total membership count: (\\d+), unresolvable subjects: (\\d+)$");
      
      boolean subjectProblems = result.contains("subject problems");
      
      Matcher matcher = pattern.matcher(result);
      
      if (matcher.matches()) {

        GrouperRequestContainer.retrieveFromRequestOrCreate().getGroupContainer().setCountAdded(GrouperUtil.intValue(matcher.group(2)));
        GrouperRequestContainer.retrieveFromRequestOrCreate().getGroupContainer().setCountRemoved(GrouperUtil.intValue(matcher.group(3)));
        GrouperRequestContainer.retrieveFromRequestOrCreate().getGroupContainer().setCountTotal(GrouperUtil.intValue(matcher.group(4)));
        GrouperRequestContainer.retrieveFromRequestOrCreate().getGroupContainer().setCountUnresolvableSubjects(GrouperUtil.intValue(matcher.group(5)));
        
        if (subjectProblems) {
          result = TextContainer.retrieveFromRequest().getText().get("groupRunLoaderProcessResultWithSubjectProblems");
        } else {
          result = TextContainer.retrieveFromRequest().getText().get("groupRunLoaderProcessResult");
        } 
      }
      
      if (subjectProblems) {
        // show an error
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error,
            TextContainer.retrieveFromRequest().getText().get("loaderGroupUpdateError") + "<br />"
                + result));
      } else {
        //lets show a success message on the new screen
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success,
            TextContainer.retrieveFromRequest().getText().get("loaderGroupUpdateSuccess") + "<br />"
                + result));
      }

      filterHelper(request, response, group);

    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * schedule loader job
   * don't throw exception, display success or error message directly on New Ui screen
   * @param request
   * @param response
   */
  public void scheduleLoaderGroup(HttpServletRequest request, HttpServletResponse response) {

    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();

    GrouperSession grouperSession = null;

    Group group = null;

    try {

      grouperSession = GrouperSession.start(loggedInSubject);

      group = retrieveGroupHelper(request, AccessPrivilege.ADMIN).getGroup();

      if (group == null) {
        return;
      }

      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();

      try {
        boolean foundLoaderType = false;
    
        // check sql first
        AttributeDefName grouperLoader = GrouperDAOFactory.getFactory().getAttributeDefName()
            .findByNameSecure(GrouperConfig.retrieveConfig().propertyValueString("grouper.rootStemForBuiltinObjects", "etc") + ":legacy:attribute:legacyGroupType_grouperLoader", false);
    
        if (grouperLoader != null) {
          if (group.getAttributeDelegate().hasAttribute(grouperLoader)) {
            foundLoaderType = true;
            GrouperLoaderType.validateAndScheduleSqlLoad(group, null, false);
          }
        }
        
        // ok now check ldap
        if (!foundLoaderType) {
          AttributeDefName grouperLoaderLdapName = GrouperDAOFactory.getFactory().getAttributeDefName()
              .findByNameSecure(LoaderLdapUtils.grouperLoaderLdapName(), false);
          
          if (grouperLoaderLdapName != null) {
            AttributeAssign assign = group.getAttributeDelegate().retrieveAssignment("assign", grouperLoaderLdapName, true, false);
            if (assign != null) {
              foundLoaderType = true;
              GrouperLoaderType.validateAndScheduleLdapLoad(assign, null, false);
            }
          }
        }
        
        if (!foundLoaderType) {
          throw new RuntimeException("Group is not a loader group.");
        }
      } catch (Exception e) {

        LOG.error("Error scheduling loader job from ui for group: " + group.getName(), e);
        
        //lets show an error message on the new screen  
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error,
            TextContainer.retrieveFromRequest().getText().get("loaderGroupScheduleError") + "<br />"
                + e.getMessage()));
        return;
      }

      //lets show a success message on the new screen
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success,
          TextContainer.retrieveFromRequest().getText().get("loaderGroupScheduleSuccess")));

      filterHelper(request, response, group);

    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * this subjects privileges inherited from folders
   * @param request
   * @param response
   */
  public void inheritedPrivilegesAssignedToThisGroupFromFolders(HttpServletRequest request, HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    Group group = null;
    
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      group = retrieveGroupHelper(request, AccessPrivilege.VIEW).getGroup();
      
      if (group == null) {
        return;
      }
  
      //if viewing a subject, and that subject is a group, just show the group screen
      GrouperSubject grouperSubject = new GrouperSubject(group);

      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
      RulesContainer rulesContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getRulesContainer();
      
      Set<GuiRuleDefinition> guiRuleDefinitions = new TreeSet<GuiRuleDefinition>();
      {
        Set<RuleDefinition> groupRuleDefinitions  = RuleFinder.findSubjectPrivilegeInheritRules(grouperSubject, true);
        for (RuleDefinition ruleDefinition : GrouperUtil.nonNull(groupRuleDefinitions)) {
          GuiRuleDefinition guiRuleDefinition = new GuiRuleDefinition(ruleDefinition);
          if (guiRuleDefinition.getOwnerGuiStem() != null) {
            guiRuleDefinitions.add(guiRuleDefinition);
          }
        }
      }
      
      rulesContainer.setGuiRuleDefinitions(guiRuleDefinitions);
  
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/group/assignedToGroupInheritedPrivilegesInvolvement.jsp"));
  
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

//  /**
//   * this subjects privileges inherited from folders
//   * @param request
//   * @param response
//   */
//  public void provisioning(HttpServletRequest request, HttpServletResponse response) {
//    
//    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
//    
//    GrouperSession grouperSession = null;
//  
//    Group group = null;
//    
//    try {
//  
//      grouperSession = GrouperSession.start(loggedInSubject);
//  
//      group = retrieveGroupHelper(request, AccessPrivilege.ADMIN).getGroup();
//      
//      if (group == null) {
//        return;
//      }
//  
//      ProvisioningContainer provisioningContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getProvisioningContainer();
//      
//      //if viewing a subject, and that subject is a group, just show the group screen
//      GrouperSubject grouperSubject = new GrouperSubject(group);
//
//      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
//  
//  
//      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
//          "/WEB-INF/grouperUi2/group/groupProvisioning.jsp"));
//  
//    } finally {
//      GrouperSession.stopQuietly(grouperSession);
//    }
//  }

}
