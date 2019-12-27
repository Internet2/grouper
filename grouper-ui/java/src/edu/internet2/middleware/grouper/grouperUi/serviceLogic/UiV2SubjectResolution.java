package edu.internet2.middleware.grouper.grouperUi.serviceLogic;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.app.usdu.SubjectResolutionAttributeValue;
import edu.internet2.middleware.grouper.app.usdu.USDU;
import edu.internet2.middleware.grouper.app.usdu.UsduAttributeNames;
import edu.internet2.middleware.grouper.app.usdu.UsduJob;
import edu.internet2.middleware.grouper.app.usdu.UsduService;
import edu.internet2.middleware.grouper.attr.finder.AttributeAssignValueFinder;
import edu.internet2.middleware.grouper.attr.finder.AttributeAssignValueFinder.AttributeAssignValueFinderResult;
import edu.internet2.middleware.grouper.audit.AuditEntry;
import edu.internet2.middleware.grouper.audit.AuditTypeBuiltin;
import edu.internet2.middleware.grouper.audit.AuditTypeIdentifier;
import edu.internet2.middleware.grouper.audit.UserAuditQuery;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiSubject;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiSubjectResolutionSubject;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiPaging;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiResponseJs;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction.GuiMessageType;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiSorting;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GrouperRequestContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GuiAuditEntry;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.SubjectResolutionContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.TextContainer;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.internal.dao.QuerySort;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.subj.SubjectHelper;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.ui.tags.GrouperPagingTag2;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

public class UiV2SubjectResolution {
  
  /**
   * 
   * @param request
   * @param response
   */
  public void subjectResolutionMain(HttpServletRequest request, HttpServletResponse response) {
    
    final GrouperRequestContainer grouperRequestContainer = GrouperRequestContainer.retrieveFromRequestOrCreate();
    final SubjectResolutionContainer subjectResolutionContainer = grouperRequestContainer.getSubjectResolutionContainer();
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      subjectResolutionContainer.assertSubjectResolutionEnabledAndAllowed();
      
      final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      
      //switch over to admin so attributes work
      GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {

        @Override
        public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
          subjectResolutionContainer.setSubjectResolutionStats(UsduService.getSubjectResolutionStats()); 
          return null;
        }
      });
            
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/subjectResolution/subjectResolutionMain.jsp"));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }
  
  /**
   * delete unresolved subjects
   * @param request
   * @param response
   */
  public void removeMembers(HttpServletRequest request, HttpServletResponse response) {

    final GrouperRequestContainer grouperRequestContainer = GrouperRequestContainer.retrieveFromRequestOrCreate();
    final SubjectResolutionContainer subjectResolutionContainer = grouperRequestContainer.getSubjectResolutionContainer();
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      subjectResolutionContainer.assertSubjectResolutionEnabledAndAllowed();
      
      final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      
      final Set<String> memberIds = new HashSet<String>();
      
      for (int i=0;i<1000;i++) {
        String memberId = request.getParameter("memberRow_" + i + "[]");
        if (!StringUtils.isBlank(memberId)) {
          memberIds.add(memberId);
        }
      }
  
      if (memberIds.size() == 0) {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("subjectResolutionRemoveNoSubjectSelects")));
        return;
      }

      
      //switch over to admin so attributes work
      GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {

        @Override
        public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
          
          Set<Member> members = new HashSet<Member>(GrouperDAOFactory.getFactory().getMember().findByIds(memberIds, null));
          
          // make sure they are unresolvable
          for (Member member : members) {
            
            if (USDU.isMemberResolvable(GrouperSession.staticGrouperSession(), member)) {
              throw new RuntimeException("Subject was resolvable! " + SubjectHelper.getPretty(member));
            }
          }
          
          int deleteCount = (int)UsduJob.deleteUnresolvableMembers(members, GrouperUtil.length(members));

          subjectResolutionContainer.setDeleteCount(deleteCount);
          
          guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
              TextContainer.retrieveFromRequest().getText().get("subjectResolutionRemoveSuccess")));
          
          return null;
        }
      });
      
      viewUnresolvedSubjects(request, response);
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }

  }
  
  /**
   * view unresolved subjects
   * @param request
   * @param response
   */
  public void viewUnresolvedSubjects(HttpServletRequest request, HttpServletResponse response) {
    
    final GrouperRequestContainer grouperRequestContainer = GrouperRequestContainer.retrieveFromRequestOrCreate();
    final SubjectResolutionContainer subjectResolutionContainer = grouperRequestContainer.getSubjectResolutionContainer();
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      subjectResolutionContainer.assertSubjectResolutionEnabledAndAllowed();
      
      final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      
      GuiPaging guiPaging = subjectResolutionContainer.getGuiPaging();
      final QueryOptions queryOptions = new QueryOptions();
      QuerySort querySort = new QuerySort("sourceId, subjectId", true);
      queryOptions.sort(querySort);
      GrouperPagingTag2.processRequest(request, guiPaging, queryOptions);
      
      // showDeleted, doNotShowDeleted, showAll
      String includeDeleted = request.getParameter("includeDeleted");

      Boolean deleted = null;
      
      if (StringUtils.equals(includeDeleted, "showDeleted")) {
        deleted = true;
        subjectResolutionContainer.setShowDeleted(true);
      } else if (StringUtils.equals(includeDeleted, "doNotShowDeleted") || StringUtils.isBlank(includeDeleted)) {
        deleted = false;
        subjectResolutionContainer.setShowDeleted(false);
      } else if (StringUtils.equals(includeDeleted, "showAll")) {
        deleted = null;
        subjectResolutionContainer.setShowDeleted(null);
      } else {
        throw new RuntimeException("Invalid value for includeDeleted: '" + includeDeleted + "'");
      }
      
      subjectResolutionContainer.setShowDeleted(deleted);
      
      final Boolean DELETED = deleted;
      
      //switch over to admin so attributes work
      GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {

        @Override
        public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
          Set<SubjectResolutionAttributeValue> unresolvedSubjects = UsduService.getUnresolvedSubjects(queryOptions, DELETED);
          subjectResolutionContainer.setUnresolvedSubjects(unresolvedSubjects);
          return null;
        }
      });
      
      guiPaging.setTotalRecordCount(queryOptions.getQueryPaging().getTotalRecordCount());

      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/subjectResolution/unresolvedSubjects.jsp"));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }
  
  /**
   * view subject delete audit filter
   * @param request
   * @param response
   */
  public void viewSubjectDeleteAudits(final HttpServletRequest request, final HttpServletResponse response) {
    
    final GrouperRequestContainer grouperRequestContainer = GrouperRequestContainer.retrieveFromRequestOrCreate();
    final SubjectResolutionContainer subjectResolutionContainer = grouperRequestContainer.getSubjectResolutionContainer();
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      subjectResolutionContainer.assertSubjectResolutionEnabledAndAllowed();
      
      final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/subjectResolution/subjectDeleteAudits.jsp"));
      
      //switch over to admin so attributes work
      GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {

        @Override
        public Object callback(GrouperSession grouperSession) throws GrouperSessionException {

          viewSubjectDeleteAuditsHelper(request, response);

          return null;
        }
      });
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }
  
  /**
   * the audit filter button was pressed, or paging or sorting, or view audits or something
   * @param request
   * @param response
   */
  private void viewSubjectDeleteAuditsHelper(HttpServletRequest request, HttpServletResponse response) {
    
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
    
    SubjectResolutionContainer subjectResolutionContainer = grouperRequestContainer.getSubjectResolutionContainer();
    
    GuiPaging guiPaging = subjectResolutionContainer.getGuiPaging();

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
    subjectResolutionContainer.setGuiSorting(guiSorting);
  
    guiSorting.processRequest(request);
    
    List<AuditTypeIdentifier> auditTypeActionList = new ArrayList<AuditTypeIdentifier>();
    auditTypeActionList.add(AuditTypeBuiltin.USDU_MEMBER_DELETE);
    
    query.setAuditTypeActionList(auditTypeActionList);
    
    List<AuditEntry> auditEntries = query.execute();
    
    Set<GuiAuditEntry> guiAuditEntries = GuiAuditEntry.convertFromAuditEntries(auditEntries);
    subjectResolutionContainer.setGuiAuditEntries(guiAuditEntries);
    
    guiPaging.setTotalRecordCount(queryOptions.getQueryPaging().getTotalRecordCount());
  
    if (GrouperUtil.length(auditEntries) == 0) {
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.info,
          TextContainer.retrieveFromRequest().getText().get("groupAuditLogNoEntriesFound")));
    }
    
    subjectResolutionContainer.setAuditExtendedResults(extendedResults);
    guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#subjectResolutionSubjectDeleteAuditFilterResultsId", 
        "/WEB-INF/grouperUi2/subjectResolution/subjectDeleteAuditsContents.jsp"));
  
  }
  
  /**
   * show search subjects screen
   * @param request
   * @param response
   */
  public void searchSubjects(final HttpServletRequest request, final HttpServletResponse response) {
    
    final GrouperRequestContainer grouperRequestContainer = GrouperRequestContainer.retrieveFromRequestOrCreate();
    final SubjectResolutionContainer subjectResolutionContainer = grouperRequestContainer.getSubjectResolutionContainer();
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      subjectResolutionContainer.assertSubjectResolutionEnabledAndAllowed();
      
      final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/subjectResolution/subjectSearch.jsp"));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }
  
  /**
   * submit button on search subjects page was clicked
   * @param request
   * @param response
   */
  public void searchSubjectsSubmit(final HttpServletRequest request, final HttpServletResponse response) {
    
    final GrouperRequestContainer grouperRequestContainer = GrouperRequestContainer.retrieveFromRequestOrCreate();
    final SubjectResolutionContainer subjectResolutionContainer = grouperRequestContainer.getSubjectResolutionContainer();
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      subjectResolutionContainer.assertSubjectResolutionEnabledAndAllowed();
      
      final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      
      final String subjectId = request.getParameter("subjectId");
      
      if (StringUtils.isBlank(subjectId)) {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error,
            TextContainer.retrieveFromRequest().getText().get("subjectResolutionSubjectSearchSubjectIdBlank")));
        return;
      }
      
      GuiSubjectResolutionSubject guiSubjectResolutionSub = (GuiSubjectResolutionSubject) GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        @Override
        public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
          
          Subject subject = SubjectFinder.findById(subjectId, false);
          
          GuiSubjectResolutionSubject guiSubjectResolutionSubject = new GuiSubjectResolutionSubject();
          
          if (subject != null) {
            guiSubjectResolutionSubject.setGuiSubject(new GuiSubject(subject));
          } else {
            // look for subject in member; maybe it's not resolvable
            Member member = GrouperDAOFactory.getFactory().getMember().findBySubject(subjectId, false);
            
            if (member == null) {
              return null;
            }
            
            AttributeAssignValueFinderResult attributeAssignValueFinderResult = new AttributeAssignValueFinder()
              .addOwnerMemberOfAssignAssign(member).addAttributeDefNameId(UsduAttributeNames.retrieveAttributeDefNameBase().getId())
              .findAttributeAssignValuesResult();

            SubjectResolutionAttributeValue attributeValue = UsduService.getSubjectResolutionAttributeValue(member, attributeAssignValueFinderResult);
            if (attributeValue == null) {
              
              // maybe the UsduJob has not run yet and that's why the attributes are not populated.
              // let's populate right now.
              
              DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
              Date currentDate = new Date();
              String curentDateString = dateFormat.format(currentDate);
              
              SubjectResolutionAttributeValue newValue = new SubjectResolutionAttributeValue();
              newValue.setSubjectResolutionResolvableString(BooleanUtils.toStringTrueFalse(false));
              newValue.setSubjectResolutionDateLastResolvedString(curentDateString);
              newValue.setSubjectResolutionDaysUnresolvedString(String.valueOf(0L));
              newValue.setSubjectResolutionDateLastCheckedString(curentDateString);
              newValue.setMember(member);
              
              UsduService.markMemberAsUnresolved(newValue, member);
              
              guiSubjectResolutionSubject.setSubjectResolutionAttributeValue(newValue);
              
            } else {
              guiSubjectResolutionSubject.setSubjectResolutionAttributeValue(attributeValue);
            }
          }
          
          return guiSubjectResolutionSubject;
        }
      });
      
      if (guiSubjectResolutionSub == null) {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error,
            TextContainer.retrieveFromRequest().getText().get("subjectResolutionSubjectSearchSubjectNotFound")));        
        return;
      } else {
        subjectResolutionContainer.setGuiSubjectResolutionSubject(guiSubjectResolutionSub);
      }
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#searchResultId",
          "/WEB-INF/grouperUi2/subjectResolution/subjectResolutionViewSubject.jsp"));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }

}
