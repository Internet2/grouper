/*
 * @author mchyzer
 * $Id: SimpleMembershipUpdate.java,v 1.7 2009-11-13 14:56:25 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.grouperUi.serviceLogic;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
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
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.MembershipFinder;
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
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.internal.dao.QueryPaging;
import edu.internet2.middleware.grouper.j2ee.GrouperRequestWrapper;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.ui.exceptions.ControllerDone;
import edu.internet2.middleware.grouper.ui.exceptions.NoSessionException;
import edu.internet2.middleware.grouper.ui.tags.TagUtils;
import edu.internet2.middleware.grouper.ui.util.GrouperUiUtils;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Source;
import edu.internet2.middleware.subject.SourceUnavailableException;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectNotFoundException;
import edu.internet2.middleware.subject.SubjectNotUniqueException;


/**
 * main ajax methods for simple membership update module
 */
public class SimpleMembershipUpdate {

  /**
   * submit the enabled / disabled dates popup
   * @param request
   * @param response
   */
  public void enabledDisabledSubmit(HttpServletRequest request, HttpServletResponse response) {
    
    String enabledDate = request.getParameter("enabledDate");
    String disabledDate = request.getParameter("disabledDate");
    SimpleMembershipUpdateContainer simpleMembershipUpdateContainer = SimpleMembershipUpdateContainer.retrieveFromSession();
    
    Membership membership = simpleMembershipUpdateContainer.getEnabledDisabledMember().getMembership();
    
    if (StringUtils.isBlank(enabledDate) ) {
      membership.setEnabledTime(null);
    } else {
      //must be yyyy/mm/dd
      Timestamp enabledTimestamp = GrouperUtil.toTimestamp(enabledDate);
      membership.setEnabledTime(enabledTimestamp);
    }
    
    if (StringUtils.isBlank(disabledDate) ) {
      membership.setDisabledTime(null);
    } else {
      //must be yyyy/mm/dd
      Timestamp disabledTimestamp = GrouperUtil.toTimestamp(disabledDate);
      membership.setDisabledTime(disabledTimestamp);
    }
    
    membership.update();
    
    
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    guiResponseJs.addAction(GuiScreenAction.newCloseModal());
    guiResponseJs.addAction(GuiScreenAction.newAlert(simpleMembershipUpdateContainer.getText().getEnabledDisabledSuccess()));

    //refresh this list
    retrieveMembers(request, response);

  }
  
  /**
   * index page of application
   * @param request
   * @param response
   */
  public void index(HttpServletRequest request, HttpServletResponse response) {
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();

    guiResponseJs.addAction(GuiScreenAction.newScript("document.title = '" 
        + GrouperUiUtils.message("simpleMembershipUpdate.title", false) + "'"));
    guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#topDiv", 
        "/WEB-INF/grouperUi/templates/common/commonTop.jsp"));

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
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    //setup a hideShow
    GuiHideShow.init("simpleMembershipUpdateMemberFilter", false, 
        "", "", true);
    GuiHideShow.init("simpleMembershipUpdateGroupDetails", false, null, null, true);
    
    
    GuiPaging.init("simpleMemberUpdateMembers");


    
    Group group = null;
    String groupName = null;
    GrouperSession grouperSession = null;
    try {

      grouperSession = GrouperSession.start(loggedInSubject);
  
      group = retrieveGroup(grouperSession);

      boolean defaultDeleteMultiple = simpleMembershipUpdateContainer.configValueBoolean("simpleMembershipUpdate.defaultDeleteMultiple", false);
      
      GuiHideShow.init("simpleMembershipUpdateDeleteMultiple", defaultDeleteMultiple, 
          "", "", true);

      boolean defaultImportFile = simpleMembershipUpdateContainer.configValueBoolean("simpleMembershipUpdate.defaultImportFile", true);
      
      GuiHideShow.init("membershipLiteImportFile", defaultImportFile, 
          GrouperUiUtils.message("simpleMembershipUpdate.membershipLiteImportFileButton", false), 
          GrouperUiUtils.message("simpleMembershipUpdate.membershipLiteImportTextfieldButton", false), true);

      //init css if needed
      String extraCssString = simpleMembershipUpdateContainer.configValue("simpleMembershipUpdate.extraCss", false);
      if (!StringUtils.isBlank(extraCssString)) {
        String[] extraCssArray = GrouperUtil.splitTrim(extraCssString, ",");
        for (String extraCss : extraCssArray) {
          guiResponseJs.addAction(GuiScreenAction.newScript("guiAddCss('" + GrouperUiUtils.escapeJavascript(extraCss, true) + "');"));
        }
      }

      
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
      throw new RuntimeException("Error with group: " + GrouperUiUtils.escapeHtml(groupName, true) + ", " + e.getMessage(), e);
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
    Group group = retrieveGroupHelper(grouperSession);
//    final SimpleMembershipUpdateContainer simpleMembershipUpdateContainer = SimpleMembershipUpdateContainer.retrieveFromSession();
//    System.out.println("testKey: " + simpleMembershipUpdateContainer.configValue("testKey"));
//    System.out.println("testKey2: " + simpleMembershipUpdateContainer.configValue("testKey2"));
    return group;
  }

  /**
   * get a group from app state
   * @param grouperSession
   * @return the group
   */
  private Group retrieveGroupHelper(GrouperSession grouperSession) {

    final SimpleMembershipUpdateContainer simpleMembershipUpdateContainer = SimpleMembershipUpdateContainer.retrieveFromSession();
    
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();

    AppState appState = AppState.retrieveFromRequest();

    //lets get the group
    String id = null;
    String name = null;

    HttpServletResponse response = GrouperUiFilter.retrieveHttpServletResponse();
    HttpServletRequest request = GrouperUiFilter.retrieveHttpServletRequest();

    if (appState.getUrlArgObjects() != null) {
      id = appState.getUrlArgObjects().get("groupId");
      if (StringUtils.isBlank(id)) {
        id = request.getParameter("groupId");
      }
      name = appState.getUrlArgObjects().get("groupName");
      if (StringUtils.isBlank(name)) {
        name = request.getParameter("groupName");
      }
    }

    GuiGroup guiGroup = simpleMembershipUpdateContainer.getGuiGroup();
    if (guiGroup != null && guiGroup.getGroup() != null) {

      //make sure two browser windows in the same session arent editing the same group
      if ((!StringUtils.isBlank(id) && !StringUtils.equals(id, guiGroup.getGroup().getId()))
          || (!StringUtils.isBlank(name) && !StringUtils.equals(name, guiGroup.getGroup().getName()))) {
        guiResponseJs.addAction(GuiScreenAction.newAlert(simpleMembershipUpdateContainer.getText().getErrorTooManyBrowsers()));
        init(request, response);
        throw new ControllerDone(true);
      }
      
      return guiGroup.getGroup();
    }
    
    if (StringUtils.isBlank(id) && StringUtils.isBlank(name)) {
      //make sure the URL is ok
      guiResponseJs.addAction(GuiScreenAction.newScript("location.href = 'grouper.html#operation=SimpleMembershipUpdate.index'"));

      if ("".equals(id)) {
        //if the id is there, but empty, maybe they didnt enter anything...
        guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message("simpleMembershipUpdate.errorGroupSearchNothingEntered", false)));
        
      } else {
        guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message("simpleMembershipUpdate.errorGroupSearchNoParams", false)));
      }
      index(request, response);
      throw new ControllerDone(true);
    }
    Group group = null;
    try {
      if (!StringUtils.isBlank(id)) {
        group = GroupFinder.findByUuid(grouperSession, id, true);
      } else if (!StringUtils.isBlank(name)) {
        group = GroupFinder.findByName(grouperSession, name, false);
      }
    } catch (GroupNotFoundException gnfe) {
      //ignore
    } catch (Exception e) {
      throw new RuntimeException("Problem in simpleMembershipUpdateInit : " + GrouperUiUtils.escapeHtml(id, true) 
          + ", " + GrouperUiUtils.escapeHtml(name, true) + ", " + e.getMessage(), e);
    }
    
    if (group == null) {
      guiResponseJs.addAction(GuiScreenAction.newScript("location.href = 'grouper.html#operation=SimpleMembershipUpdate.index'"));
      guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message("simpleMembershipUpdate.errorGroupSearchCantFindGroup", false)));
      index(request, response);
      throw new ControllerDone(true);
    }
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();

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
      guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message("simpleMembershipUpdate.errorGroupSearchPermissions", false)));
      index(request, response);
      throw new ControllerDone(true);
    }

    
    if (group.hasComposite()) {
      guiResponseJs.addAction(GuiScreenAction.newScript("location.href = 'grouper.html#operation=SimpleMembershipUpdate.index'"));
      guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message("simpleMembershipUpdate.errorGroupComposite", false)));
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
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
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
        members = new SimpleMembershipUpdateFilter().retrieveMembersFilter(guiPaging, group, simpleMembershipFilterMember, null); 
      }      
        
      Set<Membership> memberships = group.getImmediateMemberships(Group.getDefaultList(), members, true);
      Map<String, Membership> membershipMapByMemberId = new HashMap<String, Membership>();
      for (Membership membership : memberships) {
        //only add due to disabled date
        if (membership.getDisabledTime() != null ) {
          membershipMapByMemberId.put(membership.getMemberUuid(), membership);
        }
      }
      
      GuiMember[] guiMembers = new GuiMember[members.size()];
      int i=0;
      Membership membership = null;
      for (Member member : members) {
        guiMembers[i] = new GuiMember(member);
        //TODO update this
        guiMembers[i].setDeletable(true);
        membership = membershipMapByMemberId.get(member.getUuid());
        if (membership != null) {
          guiMembers[i].setMembership(membership);
        }
        
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
      throw new RuntimeException("Error with group: " + GrouperUiUtils.escapeHtml(groupName, false) + ", " + e.getMessage(), e);
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
    int totalSize = queryOptions.getCount() == null ? 0 : queryOptions.getCount().intValue();
    
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
      members = GrouperUiUtils.membersSortedPaged(members, queryPaging);
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
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    SimpleMembershipUpdateContainer simpleMembershipUpdateContainer = SimpleMembershipUpdateContainer.retrieveFromSession();
    GrouperSession grouperSession = null;
    Group group = null;
    String groupName = null;
    try {
      grouperSession = GrouperSession.start(loggedInSubject);
      group = this.retrieveGroup(grouperSession);
      groupName = group.getName();
      Member member = MemberFinder.findByUuid(grouperSession, memberId, true);
      group.deleteMember(member);
      
      GuiMember guiMember = new GuiMember(member);
      
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      guiResponseJs.addAction(GuiScreenAction.newAlert(
          simpleMembershipUpdateContainer.getText().getSuccessMemberDeleted(
              guiMember.getGuiSubject().getScreenLabel())));
      
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
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();

    GrouperSession grouperSession = null;

    String comboValue = httpServletRequest.getParameter("simpleMembershipUpdateAddMember");

    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    SimpleMembershipUpdateContainer simpleMembershipUpdateContainer = SimpleMembershipUpdateContainer.retrieveFromSession();
    
    //maybe they dont know how to use it
    if (StringUtils.isBlank(comboValue)) {
      guiResponseJs.addAction(GuiScreenAction.newAlert(
          simpleMembershipUpdateContainer.getText().getErrorUserSearchNothingEntered()));
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
      
      subject = GrouperUiUtils.findSubject(comboValue);
      
      final String requireGroup = simpleMembershipUpdateContainer.configValue(
          "simpleMembershipUpdate.subjectSearchRequireGroup", false);
      
      final String requireSources = simpleMembershipUpdateContainer.configValue(
          "simpleMembershipUpdate.subjectSearchRequireSources", false);
      
      if (!StringUtils.isBlank(requireSources)) {
        Set<Source> sources = GrouperUtil.convertSources(requireSources);
        String subjectSourceId = subject.getSourceId();
        boolean containsSource = false;
        for (Source source : sources) {
          if (StringUtils.equals(source.getId(), subjectSourceId)) {
            
            containsSource = true;
            
          }
        }
        if (!containsSource) {
          guiResponseJs.addAction(
              
              GuiScreenAction.newAlert(simpleMembershipUpdateContainer.getText().getErrorSubjectNotFound(subjectLabel)));
        }
      }

      //this would only happen if they didnt select one from the drop down...
      if (!StringUtils.isBlank(requireGroup)) {

        final Subject SUBJECT = subject;
        
        boolean hasMember = (Boolean)GrouperSession.callbackGrouperSession(
            grouperSession.internal_getRootSession(), new GrouperSessionHandler() {
          
          @Override
          public Object callback(GrouperSession rootGrouperSession) throws GrouperSessionException {
            
            Group groupFilter = GroupFinder.findByName(rootGrouperSession, requireGroup, true);
            return groupFilter.hasMember(SUBJECT);
          }});
        
        if (!hasMember) {

          guiResponseJs.addAction(
              
              GuiScreenAction.newAlert(simpleMembershipUpdateContainer.getText().getErrorSubjectNotFound(subjectLabel)));

          return;
          
        }
      }
      
      
      subjectLabel = GrouperUiUtils.convertSubjectToLabel(subject);
      Membership membership = MembershipFinder.findImmediateMembership(GrouperSession.staticGrouperSession(), group, subject, Group.getDefaultList(), false);
      if (membership != null) {
        
        //lets clear out the combobox:
        guiResponseJs.addAction(GuiScreenAction.newFormFieldValue("simpleMembershipUpdateAddMember", null));
        
        guiResponseJs.addAction(
            
            GuiScreenAction.newAlert(simpleMembershipUpdateContainer.getText().getWarningSubjectAlreadyMember(subjectLabel)));

        return;
      }
      
      group.addMember(subject);
      

      //lets clear out the combobox:
      guiResponseJs.addAction(GuiScreenAction.newFormFieldValue("simpleMembershipUpdateAddMember", null));
      
      guiResponseJs.addAction(GuiScreenAction.newAlert(
          simpleMembershipUpdateContainer.getText().getSuccessMemberAdded(subjectLabel)));
      
    } catch (NoSessionException se) {
      throw se;
    } catch (ControllerDone cd) {
      throw cd;
    } catch (SubjectNotFoundException snfe) {
      
      guiResponseJs.addAction(GuiScreenAction.newAlert(
          simpleMembershipUpdateContainer.getText().getErrorSubjectNotFound(subjectLabel)));

      return;
    } catch (SourceUnavailableException sue) {
      guiResponseJs.addAction(GuiScreenAction.newAlert(
          simpleMembershipUpdateContainer.getText().getErrorSourceUnavailable()));
      
      return;

    } catch (SubjectNotUniqueException snue) {
      
      guiResponseJs.addAction(GuiScreenAction.newAlert(
          simpleMembershipUpdateContainer.getText().getErrorSubjectNotUnique(subjectLabel)));
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

    SimpleMembershipUpdateContainer simpleMembershipUpdateContainer = SimpleMembershipUpdateContainer.retrieveFromSession();
    
    //lets get the selected members
    Set<String> paramNames = ((GrouperRequestWrapper)httpServletRequest).requestParameterNamesByPrefix("deleteMultiple_");
    if (paramNames.size() == 0) {
      guiResponseJs.addAction(GuiScreenAction.newAlert(simpleMembershipUpdateContainer.getText().getErrorDeleteCheckboxRequired()));
      return;
    }

    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();

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
        Member member = MemberFinder.findByUuid(grouperSession, currentMemberUuid, true);
        
        if (group.deleteMember(member, false)) {
          deleteCount++;
        }
      }      
      
      guiResponseJs.addAction(GuiScreenAction.newAlert(
          simpleMembershipUpdateContainer.getText().getSuccessMembersDeleted(deleteCount)));

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
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();

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
      
      guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message("simpleMembershipUpdate.successAllMembersDeleted",
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
