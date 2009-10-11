/*
 * @author mchyzer
 * $Id: SimpleMembershipUpdate.java,v 1.3 2009-10-11 07:32:24 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.grouperUi.serviceLogic;

import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.exception.GroupNotFoundException;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.exception.SchemaException;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiGroup;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiMember;
import edu.internet2.middleware.grouper.grouperUi.beans.json.AppState;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiHideShow;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiPaging;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiResponseJs;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction;
import edu.internet2.middleware.grouper.grouperUi.beans.simpleMembershipUpdate.SimpleMembershipUpdateContainer;
import edu.internet2.middleware.grouper.grouperUi.exceptions.ControllerDone;
import edu.internet2.middleware.grouper.grouperUi.exceptions.NoSessionException;
import edu.internet2.middleware.grouper.grouperUi.j2ee.GrouperUiJ2ee;
import edu.internet2.middleware.grouper.grouperUi.tags.TagUtils;
import edu.internet2.middleware.grouper.grouperUi.util.GuiUtils;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.internal.dao.QueryPaging;
import edu.internet2.middleware.grouper.j2ee.GrouperRequestWrapper;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.SourceUnavailableException;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectNotFoundException;
import edu.internet2.middleware.subject.SubjectNotUniqueException;


/**
 * main ajax methods for simple membership update module
 */
public class SimpleMembershipUpdate {

  /**
   * index page of application
   * @param request
   * @param response
   */
  public void index(HttpServletRequest request, HttpServletResponse response) {
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#bodyDiv", 
        "/WEB-INF/grouperUi/templates/simpleMembershipUpdate/simpleMembershipUpdateIndex.jsp"));

  }

  /**
   * 
   * @param request
   * @param response
   * @throws SchemaException 
   * @throws SubjectNotFoundException 
   */
  public void init(HttpServletRequest request, HttpServletResponse response) throws SchemaException, SubjectNotFoundException {
    
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();

    //setup the container
    final SimpleMembershipUpdateContainer simpleMembershipUpdateContainer = new SimpleMembershipUpdateContainer();
    simpleMembershipUpdateContainer.storeToSession();
    
    final Subject loggedInSubject = GrouperUiJ2ee.retrieveSubjectLoggedIn();
    
    //setup a hideShow
    GuiHideShow.init("simpleMembershipUpdateDeleteMultiple", false, 
        "", "", true);
    GuiHideShow.init("simpleMembershipUpdateMemberFilter", false, 
        "", "", true);
    GuiHideShow.init("simpleMembershipUpdateGroupDetails", false, 
        GuiUtils.message("simpleMembershipUpdate.hideGroupDetailsButton", false),
        GuiUtils.message("simpleMembershipUpdate.showGroupDetailsButton", false), true);
    
    
    GuiPaging.init("simpleMemberUpdateMembers");

    Group group = null;
    String groupName = null;
    GrouperSession grouperSession = null;
    try {

      grouperSession = GrouperSession.start(loggedInSubject);
  
      group = retrieveGroup(grouperSession);
      groupName = group.getName();
      
      if (simpleMembershipUpdateContainer.isCanReadGroup() && simpleMembershipUpdateContainer.isCanUpdateGroup()) {
        GuiGroup wsGroup = new GuiGroup(group);
        simpleMembershipUpdateContainer.setGuiGroup(wsGroup);
      }
    } catch (NoSessionException se) {
      throw se;
    } catch (ControllerDone cd) {
      throw cd;
    } catch (Exception e) {
      throw new RuntimeException("Error with group: " + GuiUtils.escapeHtml(groupName, true) + ", " + e.getMessage(), e);
    } finally {
      GrouperSession.stopQuietly(grouperSession); 
    }
    
    guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#bodyDiv", 
        "/WEB-INF/grouperUi/templates/simpleMembershipUpdate/simpleMembershipUpdateMain.jsp"));
    
    retrieveMembers(request, response);
    
  }

  /**
   * get a group from app state
   * @param grouperSession
   * @return the group
   */
  public Group retrieveGroup(GrouperSession grouperSession) {
    
    final SimpleMembershipUpdateContainer simpleMembershipUpdateContainer = SimpleMembershipUpdateContainer.retrieveFromSession();
    
    if (simpleMembershipUpdateContainer.getGuiGroup() != null) {
      return simpleMembershipUpdateContainer.getGuiGroup().getGroup();
    }
    
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();

    AppState appState = AppState.retrieveFromRequest();
    //lets get the group
    String id = null;
    String name = null;
    if (appState.getUrlArgObjects() != null) {
      id = appState.getUrlArgObjects().get("groupId");
      name = appState.getUrlArgObjects().get("groupName");
    }

    HttpServletRequest request = GrouperUiJ2ee.retrieveHttpServletRequest();
    HttpServletResponse response = GrouperUiJ2ee.retrieveHttpServletResponse();
    
    if (StringUtils.isBlank(id) && StringUtils.isBlank(name)) {
      //make sure the URL is ok
      guiResponseJs.addAction(GuiScreenAction.newScript("location.href = 'grouper.html#operation=SimpleMembershipUpdate.index'"));

      if ("".equals(id)) {
        //if the id is there, but empty, maybe they didnt enter anything...
        guiResponseJs.addAction(GuiScreenAction.newAlert(GuiUtils.message("simpleMembershipUpdate.errorGroupSearchNothingEntered", false)));
        
      } else {
        guiResponseJs.addAction(GuiScreenAction.newAlert(GuiUtils.message("simpleMembershipUpdate.errorGroupSearchNoParams", false)));
      }
      index(request, response);
      throw new ControllerDone(true);
    }
    Group group = null;
    try {
      if (!StringUtils.isBlank(id)) {
        group = GroupFinder.findByUuid(grouperSession, id);
      } else if (!StringUtils.isBlank(name)) {
        group = GroupFinder.findByName(grouperSession, name, false);
      }
    } catch (GroupNotFoundException gnfe) {
      //ignore
    } catch (Exception e) {
      throw new RuntimeException("Problem in simpleMembershipUpdateInit : " + GuiUtils.escapeHtml(id, true) 
          + ", " + GuiUtils.escapeHtml(name, true) + ", " + e.getMessage(), e);
    }
    
    if (group == null) {
      guiResponseJs.addAction(GuiScreenAction.newScript("location.href = 'grouper.html#operation=SimpleMembershipUpdate.index'"));
      guiResponseJs.addAction(GuiScreenAction.newAlert(GuiUtils.message("simpleMembershipUpdate.errorGroupSearchCantFindGroup", false)));
      index(request, response);
      throw new ControllerDone(true);
    }
    
    final Subject loggedInSubject = GrouperUiJ2ee.retrieveSubjectLoggedIn();

    final Group GROUP = group;
    
    //do this as root
    GrouperSession.callbackGrouperSession(grouperSession.internal_getRootSession(), new GrouperSessionHandler() {

      public Object callback(GrouperSession theGrouperSession)
          throws GrouperSessionException {
        boolean hasRead = GROUP.hasRead(loggedInSubject);
        boolean hasUpdate = GROUP.hasUpdate(loggedInSubject);
        boolean hasAdmin = GROUP.hasAdmin(loggedInSubject);
        
        simpleMembershipUpdateContainer.setCanReadGroup(hasAdmin || hasRead);
        simpleMembershipUpdateContainer.setCanUpdateGroup(hasAdmin || hasUpdate);
        return null;
      }
      
    });

    if (!simpleMembershipUpdateContainer.isCanReadGroup() && !simpleMembershipUpdateContainer.isCanUpdateGroup()) {
      guiResponseJs.addAction(GuiScreenAction.newScript("location.href = 'grouper.html#operation=SimpleMembershipUpdate.index'"));
      guiResponseJs.addAction(GuiScreenAction.newAlert(GuiUtils.message("simpleMembershipUpdate.errorGroupSearchPermissions", false)));
      index(request, response);
      throw new ControllerDone(true);
    }

    
    if (group.hasComposite()) {
      guiResponseJs.addAction(GuiScreenAction.newScript("location.href = 'grouper.html#operation=SimpleMembershipUpdate.index'"));
      guiResponseJs.addAction(GuiScreenAction.newAlert(GuiUtils.message("simpleMembershipUpdate.errorGroupComposite", false)));
      index(request, response);
      throw new ControllerDone(true);
      
    }        
    //store this in session
    simpleMembershipUpdateContainer.setGuiGroup(new GuiGroup(group));
    return group;
  }
  
  /**
   * retrieve members
   * @param request
   * @param response
   */
  @SuppressWarnings("unchecked")
  public void retrieveMembers(HttpServletRequest request, HttpServletResponse response) {
    
    final SimpleMembershipUpdateContainer simpleMembershipUpdateContainer = SimpleMembershipUpdateContainer.retrieveFromSession();
    
    final Subject loggedInSubject = GrouperUiJ2ee.retrieveSubjectLoggedIn();
    
    //init the pager
    GuiPaging guiPaging = GuiPaging.retrievePaging("simpleMemberUpdateMembers", true);
    
    GrouperSession grouperSession = null;
    Group group = null;
    String groupName = null;
    try {
      grouperSession = GrouperSession.start(loggedInSubject);

      group = this.retrieveGroup(grouperSession);
      groupName = group.getName();

      String simpleMembershipFilterMember = simpleMembershipUpdateContainer.getMemberFilter();

      //we have the group, now get the members
      Set<Member> members = null;
      if (StringUtils.isBlank(simpleMembershipFilterMember)) {
        simpleMembershipUpdateContainer.setMemberFilterForScreen(null);
        members = retrieveMembersNoFilter(guiPaging, group);
        
      } else {
        members = new SimpleMembershipUpdateFilter().retrieveMembersFilter(guiPaging, group, simpleMembershipFilterMember); 
      }      
        
      GuiMember[] guiMembers = new GuiMember[members.size()];
      int i=0;
      for (Member member : members) {
        guiMembers[i] = new GuiMember(member);
        //TODO update this
        guiMembers[i].setDeletable(true);
        i++;
        
      }
      simpleMembershipUpdateContainer.setGuiMembers(guiMembers);
    
//    Set<edu.internet2.middleware.grouper.Membership> memberships  = MembershipFinder.internal_findAllByGroupAndFieldAndPage(
//        group, Group.getDefaultList(), 0, 100, 200, numberOfRecords);

      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#simpleMembershipResultsList", 
          "/WEB-INF/grouperUi/templates/simpleMembershipUpdate/simpleMembershipMembershipList.jsp"));
    } catch (NoSessionException se) {
      throw se;
    } catch (ControllerDone cd) {
      throw cd;
    } catch (Exception e) {
      throw new RuntimeException("Error with group: " + GuiUtils.escapeHtml(groupName, false) + ", " + e.getMessage(), e);
    } finally {
      GrouperSession.stopQuietly(grouperSession); 
    }
      
  
  }

  /**
   * @param guiPaging
   * @param group
   * @return the members
   * @throws SchemaException
   */
  @SuppressWarnings("unchecked")
  Set<Member> retrieveMembersNoFilter(GuiPaging guiPaging, Group group)
      throws SchemaException {
    Set<Member> members;
    //get the size
    QueryOptions queryOptions = new QueryOptions().retrieveCount(true).retrieveResults(false);
    group.getImmediateMembers(Group.getDefaultList(), queryOptions);
    int totalSize = queryOptions.getCount().intValue();
    
    int pageSize = guiPaging.getPageSize();
    
    guiPaging.setTotalRecordCount(totalSize);
    guiPaging.setPageSize(pageSize);
    
    //if there are less than the sort limit, then just get all, no problem
    int sortLimit = TagUtils.mediaResourceInt("comparator.sort.limit", 200);
    QueryPaging queryPaging = new QueryPaging();
    queryPaging.setPageSize(pageSize);
    queryPaging.setPageNumber(guiPaging.getPageNumber());
    
    queryOptions = new QueryOptions().paging(queryPaging);
    if (totalSize <= sortLimit) {
      members = group.getImmediateMembers();
      members = GuiUtils.membersSortedPaged(members, queryPaging);
    } else {
      members = group.getMembers(Group.getDefaultList(), queryOptions);
    }       
    
    guiPaging.setPageNumber(queryPaging.getPageNumber());
    return members;
  }

  /**
   * delete a single member
   * @param request
   * @param response
   */
  public void deleteSingle(HttpServletRequest request, HttpServletResponse response) {
    String memberId = request.getParameter("memberId");
    if (StringUtils.isBlank(memberId)) {
      throw new RuntimeException("memberId is required");
    }
    
    final Subject loggedInSubject = GrouperUiJ2ee.retrieveSubjectLoggedIn();
    
    
    GrouperSession grouperSession = null;
    Group group = null;
    String groupName = null;
    try {
      grouperSession = GrouperSession.start(loggedInSubject);
      group = this.retrieveGroup(grouperSession);
      groupName = group.getName();
      Member member = MemberFinder.findByUuid(grouperSession, memberId);
      group.deleteMember(member);
      
      GuiMember guiMember = new GuiMember(member);
      
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      guiResponseJs.addAction(GuiScreenAction.newAlert(GuiUtils.message("simpleMembershipUpdate.successMemberDeleted",
          false, true, guiMember.getGuiSubject().getScreenLabel())));
      
    } catch (NoSessionException se) {
      throw se;
    } catch (ControllerDone cd) {
      throw cd;
    } catch (Exception se) {
      throw new RuntimeException("Error deleting member: " + memberId + " from group: " + groupName + ", " + se.getMessage(), se);
    } finally {
      GrouperSession.stopQuietly(grouperSession); 
    }

    retrieveMembers(request, response);
    
  }
  
  /**
   * 
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void addMember(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
    
    final Subject loggedInSubject = GrouperUiJ2ee.retrieveSubjectLoggedIn();

    GrouperSession grouperSession = null;

    String comboValue = httpServletRequest.getParameter("simpleMembershipUpdateAddMember");

    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();

    //maybe they dont know how to use it
    if (StringUtils.isBlank(comboValue)) {
      guiResponseJs.addAction(GuiScreenAction.newAlert(
          GuiUtils.message("simpleMembershipUpdate.errorUserSearchNothingEntered", false)));
      return;

    }
    
    Subject subject = null;
    Group group = null;
    String groupName = null;

    String subjectLabel = comboValue;

    try {

      grouperSession = GrouperSession.start(loggedInSubject);

      group = this.retrieveGroup(grouperSession);
      groupName = group.getName();
      
      subject = GuiUtils.findSubject(comboValue);
      subjectLabel = GuiUtils.convertSubjectToLabel(subject);
      if (group.hasImmediateMember(subject)) {
        
        //lets clear out the combobox:
        guiResponseJs.addAction(GuiScreenAction.newFormFieldValue("simpleMembershipUpdateAddMember", null));
        
        guiResponseJs.addAction(GuiScreenAction.newAlert(GuiUtils.message("simpleMembershipUpdate.warningSubjectAlreadyMember",
            false, true, subjectLabel)));

        return;
      }
      
      group.addMember(subject);
      

      //lets clear out the combobox:
      guiResponseJs.addAction(GuiScreenAction.newFormFieldValue("simpleMembershipUpdateAddMember", null));
      
      guiResponseJs.addAction(GuiScreenAction.newAlert(GuiUtils.message("simpleMembershipUpdate.successMemberAdded",
          false, true, subjectLabel)));
      
    } catch (NoSessionException se) {
      throw se;
    } catch (ControllerDone cd) {
      throw cd;
    } catch (SubjectNotFoundException snfe) {
      
      guiResponseJs.addAction(GuiScreenAction.newAlert(
          "Subject not found: '" + GuiUtils.escapeHtml(subjectLabel, true, false) + "'"));
      return;
    } catch (SourceUnavailableException sue) {
      guiResponseJs.addAction(GuiScreenAction.newAlert(
          GuiUtils.message("simpleMembershipUpdate.errorSourceUnavailable", false)));
      
      return;

    } catch (SubjectNotUniqueException snue) {
      
      guiResponseJs.addAction(GuiScreenAction.newAlert(
          "Subject not unique: '" + GuiUtils.escapeHtml(subjectLabel, true, false) + "'"));
      return;

    } catch (Exception se) {
      throw new RuntimeException("Error adding member to group: " + groupName + ", " + subjectLabel + ", " + se.getMessage(), se);
    } finally {
      GrouperSession.stopQuietly(grouperSession); 
    }

    retrieveMembers(httpServletRequest, httpServletResponse);
    
  }
  
  /**
   * delete selected members
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void deleteMultiple(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
    
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    //lets get the selected members
    Set<String> paramNames = ((GrouperRequestWrapper)httpServletRequest).requestParameterNamesByPrefix("deleteMultiple_");
    if (paramNames.size() == 0) {
      guiResponseJs.addAction(GuiScreenAction.newAlert(GuiUtils.message("simpleMembershipUpdate.errorDeleteCheckboxRequired")));
      return;
    }

    final Subject loggedInSubject = GrouperUiJ2ee.retrieveSubjectLoggedIn();

    GrouperSession grouperSession = null;

    Group group = null;
    String groupName = null;

    String currentMemberUuid = null;
    
    try {

      grouperSession = GrouperSession.start(loggedInSubject);

      group = this.retrieveGroup(grouperSession);
      groupName = group.getName();
      
      int deleteCount = 0;
      
      for (String paramName: paramNames) {
        
        currentMemberUuid = GrouperUtil.prefixOrSuffix(paramName, "deleteMultiple_", false);
        
        //this should be found
        Member member = MemberFinder.findByUuid(grouperSession, currentMemberUuid);
        
        if (group.deleteMember(member, false)) {
          deleteCount++;
        }
      }      
      
      guiResponseJs.addAction(GuiScreenAction.newAlert(GuiUtils.message("simpleMembershipUpdate.successMembersDeleted",
          false, true, Integer.toString(deleteCount))));
      
    } catch (NoSessionException se) {
      throw se;
    } catch (ControllerDone cd) {
      throw cd;
    } catch (Exception se) {
      throw new RuntimeException("Error deleting members from group: " + groupName 
          + ", " + currentMemberUuid + ", " + se.getMessage(), se);
    } finally {
      GrouperSession.stopQuietly(grouperSession); 
    }

    retrieveMembers(httpServletRequest, httpServletResponse);

    
  }
  
  /**
   * delete all members.  Not in one transaction so we can make progress here if something bad happens
   * @param httpServletRequest
   * @param httpServletResponse
   */
  @SuppressWarnings("unchecked")
  public void deleteAll(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    final Subject loggedInSubject = GrouperUiJ2ee.retrieveSubjectLoggedIn();

    GrouperSession grouperSession = null;

    Group group = null;
    String groupName = null;

    String currentMemberUuid = null;
    
    try {

      grouperSession = GrouperSession.start(loggedInSubject);

      group = this.retrieveGroup(grouperSession);
      groupName = group.getName();
      
      int deleteCount = 0;

      Set<Member> members = group.getImmediateMembers();
      
      for (Member member : members) {
        currentMemberUuid = member.getUuid();
        
        if (group.deleteMember(member, false)) {
          deleteCount++;
        }
      }      
      
      guiResponseJs.addAction(GuiScreenAction.newAlert(GuiUtils.message("simpleMembershipUpdate.successAllMembersDeleted",
          false, true, Integer.toString(deleteCount))));
      
    } catch (NoSessionException se) {
      throw se;
    } catch (ControllerDone cd) {
      throw cd;
    } catch (Exception se) {
      throw new RuntimeException("Error deleting all members from group: " + groupName 
          + ", " + currentMemberUuid + ", " + se.getMessage(), se);
    } finally {
      GrouperSession.stopQuietly(grouperSession); 
    }

    retrieveMembers(httpServletRequest, httpServletResponse);


  }
  
  /** logger */
  @SuppressWarnings("unused")
  private static final Log LOG = LogFactory.getLog(SimpleMembershipUpdate.class);
  
}
