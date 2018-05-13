package edu.internet2.middleware.grouper.grouperUi.serviceLogic;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.GrouperSourceAdapter;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.app.deprovisioning.GrouperDeprovisioningRealm;
import edu.internet2.middleware.grouper.app.deprovisioning.GrouperDeprovisioningSettings;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiDeprovisioningMembershipSubjectContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiMember;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiSubject;
import edu.internet2.middleware.grouper.grouperUi.beans.dojo.DojoComboLogic;
import edu.internet2.middleware.grouper.grouperUi.beans.dojo.DojoComboQueryLogicBase;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiResponseJs;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction.GuiMessageType;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.DeprovisioningContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GroupContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GrouperRequestContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GuiDeprovisioningRealm;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.TextContainer;
import edu.internet2.middleware.grouper.subj.SubjectHelper;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.ui.util.GrouperUiUtils;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Source;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectNotUniqueException;
import edu.internet2.middleware.subject.provider.SourceManager;

public class UiV2Deprovisioning {
  
  /** logger */
  private static final Log LOG = GrouperUtil.getLog(UiV2Deprovisioning.class);
  
  
  
  /**
   * deprovision a user
   * @param request
   * @param response
   */
  public void deprovisionUser(HttpServletRequest request, HttpServletResponse response) {

    final GrouperRequestContainer grouperRequestContainer = GrouperRequestContainer.retrieveFromRequestOrCreate();
    final DeprovisioningContainer deprovisioningContainer = grouperRequestContainer.getDeprovisioningContainer();
    
    deprovisioningContainer.assertDeprovisioningEnabledAndAllowed();
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
            
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/deprovisioning/deprovisioningUserSearch.jsp"));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }

  }

  /**
   * main deprovisioning link
   * @param request
   * @param response
   */
  public void deprovisioningMain(HttpServletRequest request, HttpServletResponse response) {

    final GrouperRequestContainer grouperRequestContainer = GrouperRequestContainer.retrieveFromRequestOrCreate();
    final DeprovisioningContainer deprovisioningContainer = grouperRequestContainer.getDeprovisioningContainer();
    
    deprovisioningContainer.assertDeprovisioningEnabledAndAllowed();
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      
      Set<GrouperDeprovisioningRealm> realmsForUserManager = new TreeSet<GrouperDeprovisioningRealm>(GrouperDeprovisioningRealm.retrieveRealmsForUserManager(loggedInSubject).values());
      
      Set<GuiDeprovisioningRealm> guiRealms = GuiDeprovisioningRealm.convertFromGrouperDeprovisioningRealms(realmsForUserManager);
      
      deprovisioningContainer.setRealms(guiRealms);
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/deprovisioning/deprovisioningMain.jsp"));
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#deprovisioningUsers", 
          "/WEB-INF/grouperUi2/deprovisioning/deprovisioningSelectRealm.jsp"));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }

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

        //dont do groups or internal
        if (subject != null && !SubjectHelper.inSourceList(GrouperDeprovisioningSettings.retrieveSourcesAllowedToDeprovision(), subject.getSource())) {
          subject = null;
        }
        
        return subject;
      }
  
      /**
       * 
       */
      @Override
      public Collection<Subject> search(HttpServletRequest localRequest, GrouperSession grouperSession, String query) {
        
        try {
          GrouperSourceAdapter.searchForGroupsWithReadPrivilege(true);
          Collection<Subject> results = 
              SubjectFinder.findPage(query, GrouperDeprovisioningSettings.retrieveSourcesAllowedToDeprovision()).getResults();
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
            subjects = new LinkedHashSet<Subject>(GrouperUtil.nonNull(SubjectFinder.findByIdsOrIdentifiers(searchStrings, GrouperDeprovisioningSettings.retrieveSourcesAllowedToDeprovision())).values());
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
        Set<Source> sources = null;
        if (StringUtils.equals("all", sourceId)) {
          sources = GrouperDeprovisioningSettings.retrieveSourcesAllowedToDeprovision();
        } else {
          sources = GrouperUtil.toSet(SourceManager.getInstance().getSource(sourceId));
        }
        subjects = SubjectFinder.findPage(searchString, sources).getResults();
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
   * show user access to deprovision
   * @param request
   * @param response
   */
  public void deprovisionUserSubmit(HttpServletRequest request, HttpServletResponse response) {
    final GrouperRequestContainer grouperRequestContainer = GrouperRequestContainer.retrieveFromRequestOrCreate();
    final DeprovisioningContainer deprovisioningContainer = grouperRequestContainer.getDeprovisioningContainer();
    
    deprovisioningContainer.assertDeprovisioningEnabledAndAllowed();
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      
      String subjectString = request.getParameter("groupAddMemberComboName");
      
      if (StringUtils.isBlank(subjectString)) {
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, 
            "#groupAddMemberComboId",
            TextContainer.retrieveFromRequest().getText().get("deprovisioningNoRealmSelected")));
        return;
      }
      
      GrouperDeprovisioningRealm  deprovisioningRealm = retrieveRealm(request, loggedInSubject);
      
      if (deprovisioningRealm == null) {
        return;
      }
      
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

      // must be in a certain source
      if (subject != null && !SubjectHelper.inSourceList(GrouperDeprovisioningSettings.retrieveSourcesAllowedToDeprovision(), subject.getSource())) {
        subject = null;
      }
      
      if (subject == null) {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("deprovisioningCantFindSubject")));
        return;
      }      
      
      deprovisioningContainer.setGuiDeprovisioningMembershipSubjectContainers(new HashSet<GuiDeprovisioningMembershipSubjectContainer>());
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#deprovisioningUserResultsDivId", 
          "/WEB-INF/grouperUi2/deprovisioning/deprovisioningUserResults.jsp"));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }

  }

  /**
   * deprovision a user
   * @param request
   * @param response
   */
  public void deprovisionUserDeprovisionSubmit(HttpServletRequest request, HttpServletResponse response) {

    final GrouperRequestContainer grouperRequestContainer = GrouperRequestContainer.retrieveFromRequestOrCreate();
    final DeprovisioningContainer deprovisioningContainer = grouperRequestContainer.getDeprovisioningContainer();
    
    deprovisioningContainer.assertDeprovisioningEnabledAndAllowed();

    Set<String> membershipsIds = new HashSet<String>();
    
    for (int i=0;i<10000;i++) {
      String membershipId = request.getParameter("membershipRow_" + i + "[]");
      if (!StringUtils.isBlank(membershipId)) {
        membershipsIds.add(membershipId);
      }
    }

    final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    if (membershipsIds.size() == 0) {
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
          TextContainer.retrieveFromRequest().getText().get("deprovisioningRemoveNoAssignmentsSelects")));
      return;
    }
    int successes = 0;
    int failures = 0;
    
//    Subject groupSubject = group.toSubject();
//    for (String membershipId : membershipsIds) {
//      try {
//        Membership membership = new MembershipFinder().addMembershipId(membershipId).findMembership(true);
//        Group ownerGroup = membership.getOwnerGroup();
//        //dont worry about if no change, thats a success
//        ownerGroup.deleteMember(groupSubject, false);
//        successes++;
//      } catch (Exception e) {
//        LOG.warn("Error with membership: " + membershipId + ", user: " + loggedInSubject, e);
//        failures++;
//      }
//    }

    
    guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2Deprovisioning.deprovisionMain')"));

    guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
        TextContainer.retrieveFromRequest().getText().get("deprovisioningDeprovisionSuccess")));
    
  }
  
  /**
   * @param request
   * @param response
   */
  public void deprovisioningRealmSubmit(HttpServletRequest request, HttpServletResponse response) {
    
    final GrouperRequestContainer grouperRequestContainer = GrouperRequestContainer.retrieveFromRequestOrCreate();
    final DeprovisioningContainer deprovisioningContainer = grouperRequestContainer.getDeprovisioningContainer();
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    try {
      grouperSession = GrouperSession.start(loggedInSubject);
      
      final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      
      GrouperDeprovisioningRealm  deprovisioningRealm = retrieveRealm(request, loggedInSubject);
      
      if (deprovisioningRealm == null) {
        return;
      }
      
      guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2Deprovisioning.viewRecentlyDeprovisionedUsers&realm=" + deprovisioningRealm.getLabel() + "')"));
      
//      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#deprovisioningUsers",
//          "/WEB-INF/grouperUi2/index/deprovisioningMainHelper.jsp"));
      
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }
  
  /**
   * view recently deprovisioned users
   * @param request
   * @param response
   */
  public void viewRecentlyDeprovisionedUsers(HttpServletRequest request, HttpServletResponse response) {
    
    final GrouperRequestContainer grouperRequestContainer = GrouperRequestContainer.retrieveFromRequestOrCreate();
    final DeprovisioningContainer deprovisioningContainer = grouperRequestContainer.getDeprovisioningContainer();
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    try {
      grouperSession = GrouperSession.start(loggedInSubject);
      
      final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      
      GrouperDeprovisioningRealm  deprovisioningRealm = retrieveRealm(request, loggedInSubject);
      
      if (deprovisioningRealm == null) {
        return;
      }
      
      Set<Member> usersWhoHaveBeenDeprovisioned = deprovisioningRealm.getUsersWhoHaveBeenDeprovisioned();
      
      deprovisioningContainer.setDeprovisionedGuiMembers(GuiMember.convertFromMembers(usersWhoHaveBeenDeprovisioned));
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId",
          "/WEB-INF/grouperUi2/deprovisioning/deprovisioningMain.jsp"));
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#deprovisioningUsers",
          "/WEB-INF/grouperUi2/deprovisioning/deprovisioningMainHelper.jsp"));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }
  
  /**
   * deprovision a user
   * @param request
   * @param response
   */
  public void deprovisionUser1(HttpServletRequest request, HttpServletResponse response) {
    
    final GrouperRequestContainer grouperRequestContainer = GrouperRequestContainer.retrieveFromRequestOrCreate();
    final DeprovisioningContainer deprovisioningContainer = grouperRequestContainer.getDeprovisioningContainer();
    
    deprovisioningContainer.assertDeprovisioningEnabledAndAllowed();
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
      
      final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      
      GrouperDeprovisioningRealm  deprovisioningRealm = retrieveRealm(request, loggedInSubject);
      
      if (deprovisioningRealm == null) {
        return;
      }
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#deprovisioningUsers",
          "/WEB-INF/grouperUi2/deprovisioning/deprovisioningUserSearch1.jsp"));
            
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }
  
  private static GrouperDeprovisioningRealm retrieveRealm(HttpServletRequest request, Subject subject) {
    
    final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    final GrouperRequestContainer grouperRequestContainer = GrouperRequestContainer.retrieveFromRequestOrCreate();
    final DeprovisioningContainer deprovisioningContainer = grouperRequestContainer.getDeprovisioningContainer();
    
    String realm = request.getParameter("realm");
    if (StringUtils.isBlank(realm)) {
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
          TextContainer.retrieveFromRequest().getText().get("deprovisioningNoRealmSelected")));
      return null;
    }
    
    GrouperDeprovisioningRealm deprovisioningRealm = GrouperDeprovisioningRealm.retrieveAllRealms().get(realm);
    if (deprovisioningRealm == null) {
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
          TextContainer.retrieveFromRequest().getText().get("deprovisioningNoRealmSelected")));
      return null;
    }
    
    if (!deprovisioningRealm.subjectIsManager(subject)) {
      throw new RuntimeException("User is not manager.");
    }
    
    deprovisioningContainer.setRealm(realm);
    return deprovisioningRealm;
    
  }
  
}