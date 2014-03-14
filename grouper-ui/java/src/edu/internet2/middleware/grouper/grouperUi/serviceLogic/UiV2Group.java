package edu.internet2.middleware.grouper.grouperUi.serviceLogic;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefFinder;
import edu.internet2.middleware.grouper.audit.AuditEntry;
import edu.internet2.middleware.grouper.audit.UserAuditQuery;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.exception.GroupDeleteException;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.group.TypeOfGroup;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiGroup;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiMembershipSubjectContainer;
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
import edu.internet2.middleware.grouper.grouperUi.beans.ui.TextContainer;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.j2ee.GrouperRequestWrapper;
import edu.internet2.middleware.grouper.j2ee.GrouperUiRestServlet;
import edu.internet2.middleware.grouper.membership.MembershipSubjectContainer;
import edu.internet2.middleware.grouper.membership.MembershipType;
import edu.internet2.middleware.grouper.misc.SaveMode;
import edu.internet2.middleware.grouper.misc.SaveResultType;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.AttributeDefPrivilege;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
import edu.internet2.middleware.grouper.privs.Privilege;
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

    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * the filter button was pressed, or paging or sorting, or view Group or something
   * @param request
   * @param response
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
       * 
       */
      @Override
      public Subject lookup(HttpServletRequest request, GrouperSession grouperSession, String query) {

        //when we refer to subjects in the dropdown, we will use a sourceId / subject tuple
        
        Subject subject = null;
            
        if (query != null && query.contains("||")) {
          String sourceId = GrouperUtil.prefixOrSuffix(query, "||", true);
          String subjectId = GrouperUtil.prefixOrSuffix(query, "||", false);
          subject =  SubjectFinder.findByIdOrIdentifierAndSource(subjectId, sourceId, false);

        } else {
          subject = SubjectFinder.findByIdOrIdentifier(query, false);
        }
        
        return subject;
      }

      /**
       * 
       */
      @Override
      public Collection<Subject> search(HttpServletRequest request, GrouperSession grouperSession, String query) {
        
        Group group = UiV2Group.retrieveGroupHelper(request, AccessPrivilege.UPDATE).getGroup();
        String stemName = group.getParentStemName();
        return SubjectFinder.findPageInStem(stemName, query).getResults();
      
      }

      /**
       * 
       * @param t
       * @return
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
      public String initialValidationError(HttpServletRequest request, GrouperSession grouperSession) {

        Group group = retrieveGroupHelper(request, AccessPrivilege.UPDATE).getGroup();
        
        if (group == null) {
          
          return "Not allowed to edit group";
        }
        
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
  
      Group group = retrieveGroupHelper(request, AccessPrivilege.UPDATE).getGroup();
  
      if (group == null) {
        return;
      }
  
      GroupContainer groupContainer = grouperRequestContainer.getGroupContainer();
  
      String searchString = request.getParameter("addMemberSubjectSearch");
      
      
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
            subject = SubjectFinder.findByIdOrIdentifier(searchString, false);
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
          subjects = SubjectFinder.findPageInStem(group.getParentStemName(), searchString).getResults();
        } else {
          Set<Source> sources = GrouperUtil.toSet(SourceManager.getInstance().getSource(sourceId));
          subjects = SubjectFinder.findPageInStem(group.getParentStemName(), searchString, sources).getResults();
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
        subject = SubjectFinder.findByIdOrIdentifier(subjectString, false);
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
      
      boolean madeChanges = group.addMember(subject, defaultPrivs, memberChecked, adminChecked, 
          updateChecked, readChecked, viewChecked, optinChecked, optoutChecked, attrReadChecked, 
          attrUpdateChecked);
      
      if (madeChanges) {

        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
            TextContainer.retrieveFromRequest().getText().get("groupAddMemberMadeChangesSuccess")));

        //what subscreen are we on?
        String groupRefreshPart = request.getParameter("groupRefreshPart");
        if (StringUtils.equals(groupRefreshPart, "audits")) {
          viewAuditsFilter(request, response);
        } else if (StringUtils.equals(groupRefreshPart, "privileges")) {
          filterPrivileges(request, response);
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
      
      for (Member member : members) {
        
        for (Privilege privilege : privileges) {
          if (assign) {
            changes += group.grantPriv(member.getSubject(), privilege, false) ? 1 : 0;
          } else {
            changes += group.revokePriv(member.getSubject(), privilege, false) ? 1 : 0;
          }
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
  
      filterPrivilegesHelper(request, response, group);
  
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
      
      final Stem parentFolder = new StemFinder().addPrivilege(NamingPrivilege.CREATE)
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
        
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("groupCreateCantCreateInRoot")));
        return;
        
      }
      
      try {

        //create the group
        group = new GroupSave(grouperSession).assignName(parentFolder.getName() + ":" + extension)
            .assignDisplayExtension(displayExtension).assignDescription(description).assignTypeOfGroup(typeOfGroup)
            .assignPrivAllAdmin(adminChecked).assignPrivAllAttrRead(attrReadChecked)
            .assignPrivAllAttrUpdate(attrUpdateChecked).assignPrivAllOptin(optinChecked)
            .assignPrivAllOptout(optoutChecked).assignPrivAllRead(readChecked)
            .assignPrivAllUpdate(updateChecked).assignPrivAllView(viewChecked)
            .save();
  
      } catch (InsufficientPrivilegeException ipe) {
        
        LOG.warn("Insufficient privilege exception for group create: " + SubjectHelper.getPretty(loggedInSubject), ipe);
        
        //dont change screens
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("groupCreateInsufficientPrivileges")));
        return;
  
      } catch (Exception sde) {
        
        LOG.warn("Error creating group: " + SubjectHelper.getPretty(loggedInSubject) + ", " + group, sde);
        
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

  
    } finally {
      GrouperSession.stopQuietly(grouperSession);
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
      
  
      } catch (InsufficientPrivilegeException ipe) {
        
        LOG.warn("Insufficient privilege exception for group edit: " + SubjectHelper.getPretty(loggedInSubject), ipe);
        
        //dont change screens
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("groupCreateInsufficientPrivileges")));
        return;
  
      } catch (Exception sde) {
        
        LOG.warn("Error edit group: " + SubjectHelper.getPretty(loggedInSubject) + ", " + group, sde);
        
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
      
      final Stem parentFolder = new StemFinder().addPrivilege(NamingPrivilege.CREATE)
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
      
      final Stem parentFolder = StringUtils.isBlank(parentFolderId) ? null : new StemFinder().addPrivilege(NamingPrivilege.CREATE)
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
            .copyGroupAsPrivilege(copyPrivsInOtherGroups).save();
  
      } catch (InsufficientPrivilegeException ipe) {
        
        LOG.warn("Insufficient privilege exception for group copy: " + SubjectHelper.getPretty(loggedInSubject), ipe);
        
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("groupCopyInsufficientPrivileges")));
        return;
  
      }
  
      boolean changed = false;
      
      //see if we are changing the extension
      if (!StringUtils.equals(newGroup.getExtension(), extension)) {
        newGroup.setExtension(extension, false);
        changed = true;
      }
      
      //see if we are changing the display extension
      if (!StringUtils.equals(newGroup.getDisplayExtension(), displayExtension)) {
        newGroup.setDisplayExtension(displayExtension);
        changed = true;
      }
  
      //save it if we need to
      if (changed) {
        newGroup.store();
      }
      
      guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2Group.viewGroup&groupId=" + newGroup.getId() + "')"));
  
      //lets show a success message on the new screen
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
          TextContainer.retrieveFromRequest().getText().get("groupCopySuccess")));
      
  
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
        if (requirePrivilege.equals(AccessPrivilege.ADMIN)) {
          if (!groupContainer.isCanAdmin()) {
            guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
                TextContainer.retrieveFromRequest().getText().get("groupNotAllowedToAdminGroup")));
            addedError = true;
            privsOk = false;
          }
        } else if (requirePrivilege.equals(AccessPrivilege.VIEW)) {
          if (!groupContainer.isCanView()) {
            guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
                TextContainer.retrieveFromRequest().getText().get("groupNotAllowedToViewGroup")));
            addedError = true;
            privsOk = false;
          }
        } else if (requirePrivilege.equals(AccessPrivilege.READ)) {
          if (!groupContainer.isCanRead()) {
            guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
                TextContainer.retrieveFromRequest().getText().get("groupNotAllowedToReadGroup")));
            addedError = true;
            privsOk = false;
          }
        } else if (requirePrivilege.equals(AccessPrivilege.OPTIN)) {
          if (!groupContainer.isCanOptin()) {
            guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
                TextContainer.retrieveFromRequest().getText().get("groupNotAllowedToOptinGroup")));
            addedError = true;
            privsOk = false;
          }
        } else if (requirePrivilege.equals(AccessPrivilege.OPTOUT)) {
          if (!groupContainer.isCanOptout()) {
            guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
                TextContainer.retrieveFromRequest().getText().get("groupNotAllowedToOptoutGroup")));
            addedError = true;
            privsOk = false;
          }
        } else if (requirePrivilege.equals(AccessPrivilege.UPDATE)) {
          if (!groupContainer.isCanUpdate()) {
            guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
                TextContainer.retrieveFromRequest().getText().get("groupNotAllowedToUpdateGroup")));
            addedError = true;
            privsOk = false;
          }
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
  
      group = retrieveGroupHelper(request, AccessPrivilege.UPDATE).getGroup();
      
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
  
      Set<String> membershipsIds = new HashSet<String>();
      
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
      int successes = 0;
      int failures = 0;
      
      for (String membershipId : membershipsIds) {
        try {
          Membership membership = new MembershipFinder().addMembershipId(membershipId).findMembership(true);

          group.deleteMember(membership.getMember(), false);

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
      
      filterHelper(request, response, group);
  
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
      
      if (parentStem == null || !parentStem.canHavePrivilege(loggedInSubject, NamingPrivilege.STEM.getName(), false)) {
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
          
          if (parentStem != null && parentStem.canHavePrivilege(loggedInSubject, NamingPrivilege.STEM.getName(), false)) {
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
          NamingPrivilege.listToPriv(Field.FIELD_NAME_STEMMERS)} : new Privilege[]{  
            NamingPrivilege.listToPriv(Field.FIELD_NAME_STEMMERS),
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
   * combo filter create group folder
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
      public Group lookup(HttpServletRequest request, GrouperSession grouperSession, String query) {
        Subject loggedInSubject = grouperSession.getSubject();
        Group theGroup = new GroupFinder().assignPrivileges(AccessPrivilege.UPDATE_PRIVILEGES)
            .assignSubject(loggedInSubject)
            .assignFindByUuidOrName(true).assignScope(query).findGroup();
        return theGroup;
      }
  
      /**
       * 
       */
      @Override
      public Collection<Group> search(HttpServletRequest request, GrouperSession grouperSession, String query) {
        Subject loggedInSubject = grouperSession.getSubject();
        int groupComboSize = GrouperUiConfig.retrieveConfig().propertyValueInt("uiV2.groupComboboxResultSize", 200);
        QueryOptions queryOptions = QueryOptions.create(null, null, 1, groupComboSize);
        return new GroupFinder().assignPrivileges(AccessPrivilege.UPDATE_PRIVILEGES)
            .assignScope(query).assignSubject(loggedInSubject)
            .assignSplitScope(true).assignQueryOptions(queryOptions).findGroups();
      }
  
      /**
       * 
       * @param t
       * @return
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
   * export a group
   * @param request
   * @param response
   */
  public void groupExportSubmit(HttpServletRequest request, HttpServletResponse response) {

    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    Group group = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);

      List<String> urlStrings = GrouperUiRestServlet.extractUrlStrings(request);
      
      //groupId=721e4e8ae6e54c4087db092f0a6372f7
      String groupIdString = urlStrings.get(2);
      
      String groupId = GrouperUtil.prefixOrSuffix(groupIdString, "=", false);
      
      group = GroupFinder.findByUuid(grouperSession, groupId, false);

      if (group == null) {
        throw new RuntimeException("Cant find group by id: " + groupId);
      }
      
      GroupContainer groupContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getGroupContainer();
      
      groupContainer.setGuiGroup(new GuiGroup(group));
      
      if (!groupContainer.isCanRead()) {
        throw new RuntimeException("Cant read group: " + group.getName());
      }
      
      //ids
      String groupExportOptions = urlStrings.get(3);
      
      boolean exportAll = false;
      if (StringUtils.equals("all", groupExportOptions)) {
        groupContainer.setExportAll(true);
        exportAll = true;
      } else if (StringUtils.equals("ids", groupExportOptions)) {
        groupContainer.setExportAll(false);
      } else {
        throw new RuntimeException("Not expecting group-export-options value: '" + groupExportOptions + "'");
      }

      
      //groupExportSubjectIds_removeAllMembers.csv
      @SuppressWarnings("unused")
      String fileName = urlStrings.get(4);
      
      if (exportAll) {
        String headersCommaSeparated = GrouperUiConfig.retrieveConfig().propertyValueString(
            "uiV2.group.exportAllSubjectFields");
        
        String exportAllSortField = GrouperUiConfig.retrieveConfig().propertyValueString(
            "uiV2.group.exportAllSortField");
  
        SimpleMembershipUpdateImportExport.exportGroupAllFieldsToBrowser(group, headersCommaSeparated, exportAllSortField);
      } else {
        
        SimpleMembershipUpdateImportExport.exportGroupSubjectIdsCsv(group);
        
      }
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }

  }
  
  /**
   * export group members screen
   * @param request
   * @param response
   */
  public void groupExport(HttpServletRequest request, HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    Group group = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      group = retrieveGroupHelper(request, AccessPrivilege.READ).getGroup();
      
      if (group == null) {
        return;
      }
  
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/group/groupExport.jsp"));
  
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }


  /**
   * export group members screen change the type of export
   * @param request
   * @param response
   */
  public void groupExportTypeChange(HttpServletRequest request, HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    Group group = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      group = retrieveGroupHelper(request, AccessPrivilege.READ).getGroup();
      
      if (group == null) {
        return;
      }
  
      String groupExportOptions = request.getParameter("group-export-options[]");
      
      GroupContainer groupContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getGroupContainer();
      
      if (StringUtils.equals("all", groupExportOptions)) {
        groupContainer.setExportAll(true);
      } else if (StringUtils.equals("ids", groupExportOptions)) {
        groupContainer.setExportAll(false);
      } else {
        throw new RuntimeException("Not expecting group-export-options value: '" + groupExportOptions + "'");
      }
      
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#formActionsDivId", 
          "/WEB-INF/grouperUi2/group/groupExportButtons.jsp"));
  
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * setup the extra groups (other than combobox), and maybe move the combobox down
   * @param loggedInSubject
   * @param request
   * @param removeGroupId if removing one
   * @param includeCombobox
   * @return all groups including combobox one
   */
  private Set<Group> groupImportSetupExtraGroups(Subject loggedInSubject, 
      HttpServletRequest request, GuiResponseJs guiResponseJs, boolean considerRemoveGroupId, boolean includeCombobox) {

    Set<GuiGroup> extraGuiGroups = new LinkedHashSet<GuiGroup>();
    
    GrouperRequestContainer.retrieveFromRequestOrCreate().getGroupContainer().setGroupImportExtraGuiGroups(extraGuiGroups);
    
    Set<Group> allGroups = new LinkedHashSet<Group>();

    String removeGroupId = null;

    //if removing a group id
    if (considerRemoveGroupId) {
      removeGroupId = request.getParameter("removeGroupId");
      if (StringUtils.isBlank(removeGroupId)) {
        throw new RuntimeException("Why would removeGroupId be empty????");
      }
    }

    //if moving combobox down to extra list or getting all groups
    String comboValue = request.getParameter("groupImportGroupComboName");
    
    if (StringUtils.isBlank(comboValue)) {
      //if didnt pick one from results
      comboValue = request.getParameter("groupImportGroupComboNameDisplay");
    }
    
    Group theGroup = StringUtils.isBlank(comboValue) ? null : new GroupFinder()
        .assignPrivileges(AccessPrivilege.UPDATE_PRIVILEGES)
        .assignSubject(loggedInSubject)
        .assignFindByUuidOrName(true).assignScope(comboValue).findGroup();

    if (theGroup == null) {
      if (includeCombobox) {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("groupImportGroupNotFound")));
      }
      
    } else {
      if (includeCombobox) {
        extraGuiGroups.add(new GuiGroup(theGroup));
      }
      //always add to all groups
      allGroups.add(theGroup);
    }

    //loop through all the hidden fields (max 100)
    for (int i=0;i<100;i++) {
      String extraGroupId = request.getParameter("extraGroupId_" + i);
      
      //we are at the end
      if (StringUtils.isBlank(extraGroupId)) {
        break;
      }
      
      //might be removing this one
      if (considerRemoveGroupId && StringUtils.equals(removeGroupId, extraGroupId)) {
        continue;
      }
      
      theGroup = new GroupFinder()
        .assignPrivileges(AccessPrivilege.UPDATE_PRIVILEGES)
        .assignSubject(loggedInSubject)
        .assignFindByUuidOrName(true).assignScope(extraGroupId).findGroup();
      
      extraGuiGroups.add(new GuiGroup(theGroup));

      //always add to all groups
      allGroups.add(theGroup);
      
    }
    return allGroups;
  }
  
  /**
   * submit a group import
   * @param request
   * @param response
   */
  public void groupImportSubmit(HttpServletRequest request, HttpServletResponse response) {
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();

    GrouperRequestContainer grouperRequestContainer = GrouperRequestContainer.retrieveFromRequestOrCreate();
    GroupContainer groupContainer = grouperRequestContainer.getGroupContainer();

    try {
      grouperSession = GrouperSession.start(loggedInSubject);
      
      {
        Group group = retrieveGroupHelper(request, AccessPrivilege.UPDATE, false).getGroup();
        if (group != null) {
          groupContainer.setImportFromGroup(true);
        }
      }
      {
        Subject subject = UiV2Subject.retrieveSubjectHelper(request, false);
        if (subject != null) {
          groupContainer.setImportFromSubject(true);
        }
      }
      
      
      GrouperRequestWrapper grouperRequestWrapper = (GrouperRequestWrapper)request;
      
      FileItem importCsvFile = grouperRequestWrapper.getParameterFileItem("importCsvFile");

      Reader reader = null;
      reader = new InputStreamReader(importCsvFile.getInputStream());
      
      String contents = IOUtils.toString(reader);
      
      //not sure why this would happen (race condition?)
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.info, 
          "in business: " + contents));
    } catch (Exception e) {
      throw new RuntimeException("error", e);
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }

  }
  
  /**
   * modal search form results for add group to import
   * @param request
   * @param response
   */
  public void groupImportGroupSearch(HttpServletRequest request, HttpServletResponse response) {
    
    GrouperRequestContainer grouperRequestContainer = GrouperRequestContainer.retrieveFromRequestOrCreate();
    
    GroupContainer groupContainer = grouperRequestContainer.getGroupContainer();
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
    GrouperSession grouperSession = null;
    
    try {
      grouperSession = GrouperSession.start(loggedInSubject);
  
      String searchString = request.getParameter("addGroupSearch");
      
      boolean searchOk = GrouperUiUtils.searchStringValid(searchString);
      if (!searchOk) {
        
        guiResponseJs.addAction(GuiScreenAction.newInnerHtml("#addGroupResults", 
            TextContainer.retrieveFromRequest().getText().get("groupImportAddToGroupNotEnoughChars")));
        return;
      }

      String matchExactIdString = request.getParameter("matchExactId[]");
      boolean matchExactId = GrouperUtil.booleanValue(matchExactIdString, false);

      GuiPaging guiPaging = groupContainer.getGuiPaging();
      QueryOptions queryOptions = new QueryOptions();

      GrouperPagingTag2.processRequest(request, guiPaging, queryOptions); 

      Set<Group> groups = null;
    
    
      GroupFinder groupFinder = new GroupFinder().assignPrivileges(AccessPrivilege.UPDATE_PRIVILEGES)
        .assignScope(searchString).assignSplitScope(true).assignQueryOptions(queryOptions);
      
      if (matchExactId) {
        groupFinder.assignFindByUuidOrName(true);
      }
      
      groups = groupFinder.findGroups();
      
      guiPaging.setTotalRecordCount(queryOptions.getQueryPaging().getTotalRecordCount());
      
      if (GrouperUtil.length(groups) == 0) {

        guiResponseJs.addAction(GuiScreenAction.newInnerHtml("#addGroupResults", 
            TextContainer.retrieveFromRequest().getText().get("groupImportAddGroupNotFound")));
        return;
      }
      
      Set<GuiGroup> guiGroups = GuiGroup.convertFromGroups(groups);
      
      groupContainer.setGuiGroups(guiGroups);
  
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#addGroupResults", 
          "/WEB-INF/grouperUi2/group/groupImportAddGroupResults.jsp"));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }

  
  /**
   * import group members screen remove group from list
   * @param request
   * @param response
   */
  public void groupImportRemoveGroup(HttpServletRequest request, HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    try {
      
      grouperSession = GrouperSession.start(loggedInSubject);

      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();

      groupImportSetupExtraGroups(loggedInSubject, request, guiResponseJs, true, false);
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#groupImportExtraGroupsDivId", 
          "/WEB-INF/grouperUi2/group/groupImportExtraGroups.jsp"));

    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * import group members screen add group to list
   * @param request
   * @param response
   */
  public void groupImportAddGroup(HttpServletRequest request, HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    try {
      
      grouperSession = GrouperSession.start(loggedInSubject);

      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();

      groupImportSetupExtraGroups(loggedInSubject, request, guiResponseJs, false, true);

      //clear out combobox
      guiResponseJs.addAction(GuiScreenAction.newScript(
          "dijit.byId('groupImportGroupComboId').set('displayedValue', ''); " +
          "dijit.byId('groupImportGroupComboId').set('value', '');"));

      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#groupImportExtraGroupsDivId", 
          "/WEB-INF/grouperUi2/group/groupImportExtraGroups.jsp"));

    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * import group members screen
   * @param request
   * @param response
   */
  public void groupImport(HttpServletRequest request, HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);

      GrouperRequestContainer grouperRequestContainer = new GrouperRequestContainer();
      GroupContainer groupContainer = grouperRequestContainer.getGroupContainer();
      
      String backTo = request.getParameter("backTo");
      
      {
        //this will also put the group in the group container so it can populate the combobox
        Group group = retrieveGroupHelper(request, AccessPrivilege.UPDATE, false).getGroup();
        if (group != null && StringUtils.equals("group", backTo)) {
          groupContainer.setImportFromGroup(true);
        }
      }
      {
        Subject subject = UiV2Subject.retrieveSubjectHelper(request, false);
        if (subject != null && StringUtils.equals("subject", backTo)) {
          groupContainer.setImportFromSubject(true);
        }
      }

      
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/group/groupImport.jsp"));
  
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

}
