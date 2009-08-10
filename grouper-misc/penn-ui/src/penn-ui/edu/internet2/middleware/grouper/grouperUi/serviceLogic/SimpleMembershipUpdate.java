/*
 * @author mchyzer
 * $Id: SimpleMembershipUpdate.java,v 1.10 2009-08-10 21:35:17 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.grouperUi.serviceLogic;

import java.io.PrintWriter;
import java.util.Properties;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;

import au.com.bytecode.opencsv.CSVWriter;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.exception.GroupNotFoundException;
import edu.internet2.middleware.grouper.exception.GroupNotFoundRuntimeException;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.exception.SchemaException;
import edu.internet2.middleware.grouper.grouperUi.GrouperUiJ2ee;
import edu.internet2.middleware.grouper.grouperUi.exceptions.ControllerDone;
import edu.internet2.middleware.grouper.grouperUi.json.AppState;
import edu.internet2.middleware.grouper.grouperUi.json.GuiGroup;
import edu.internet2.middleware.grouper.grouperUi.json.GuiHideShow;
import edu.internet2.middleware.grouper.grouperUi.json.GuiMember;
import edu.internet2.middleware.grouper.grouperUi.json.GuiPaging;
import edu.internet2.middleware.grouper.grouperUi.json.GuiResponseJs;
import edu.internet2.middleware.grouper.grouperUi.json.GuiScreenAction;
import edu.internet2.middleware.grouper.grouperUi.json.SimpleMembershipUpdateContainer;
import edu.internet2.middleware.grouper.grouperUi.util.GuiUtils;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.internal.dao.QueryPaging;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.SourceUnavailableException;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectNotFoundException;
import edu.internet2.middleware.subject.SubjectNotUniqueException;


/**
 *
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
   * filter groups to pick one to edit
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void filterGroups(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
    final Subject loggedInSubject = GrouperUiJ2ee.retrieveSubjectLoggedIn();
    
    
    GrouperSession grouperSession = null;

    String searchTerm = httpServletRequest.getParameter("mask");

    try {
      grouperSession = GrouperSession.start(loggedInSubject);
      
      
      Set<Group> groups = null;
      
      StringBuilder xmlBuilder = new StringBuilder(GuiUtils.DHTMLX_OPTIONS_START);

      QueryOptions queryOptions = null;
      
      if (StringUtils.defaultString(searchTerm).length() < 2) {
        GuiUtils.dhtmlxOptionAppend(xmlBuilder, "", 
            GuiUtils.message("simpleMembershipUpdate.errorNotEnoughGroupChars", false), "bullet_error.png");
      } else {
        queryOptions = new QueryOptions().paging(200, 1, true).sortAsc("displayName");
        groups = GrouperDAOFactory.getFactory().getGroup().getAllGroupsSecure("%" + searchTerm + "%", grouperSession, loggedInSubject, 
            GrouperUtil.toSet(AccessPrivilege.ADMIN, AccessPrivilege.UPDATE), queryOptions);
        
        if (GrouperUtil.length(groups) == 0) {
          GuiUtils.dhtmlxOptionAppend(xmlBuilder, "", 
              GuiUtils.message("simpleMembershipUpdate.errorNoGroupsFound", false), "bullet_error.png");
        }
      }
      
      for (Group group : GrouperUtil.nonNull(groups)) {

        String value = group.getUuid();
        String label = group.getDisplayName();
        String imageName = GuiUtils.imageFromSubjectSource("g:gsa");

        GuiUtils.dhtmlxOptionAppend(xmlBuilder, value, label, imageName);
      }

      //add one more for more options if we didnt get them all
      if (queryOptions != null && queryOptions.getCount() != null 
          && groups != null && queryOptions.getCount() > groups.size()) {
        GuiUtils.dhtmlxOptionAppend(xmlBuilder, "", 
            GuiUtils.message("simpleMembershipUpdate.errorTooManyGroups", false), "bullet_error.png");
      }
      
      
      xmlBuilder.append(GuiUtils.DHTMLX_OPTIONS_END);
      
      GuiUtils.printToScreen(xmlBuilder.toString(), "text/xml", false, false);

    } catch (Exception se) {
      throw new RuntimeException("Error searching for groups: '" + GuiUtils.escapeHtml(searchTerm, true) + "', " + se.getMessage(), se);
    } finally {
      GrouperSession.stopQuietly(grouperSession); 
    }

    //dont print the regular JSON
    throw new ControllerDone();

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
        guiResponseJs.addAction(GuiScreenAction.newAlert(GuiUtils.message("simpleMembershipUpdate.groupSearchNothingEntered", false)));
        
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
    } catch (GroupNotFoundRuntimeException gnfre) {
      //ignore
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

    
    if (group.isComposite()) {
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
      
      //we have the group, now get the members
      
      Set<Member> members;
      //get the size
      QueryOptions queryOptions = new QueryOptions().retrieveCount(true).retrieveResults(false);
      group.getImmediateMembers(Group.getDefaultList(), queryOptions);
      int totalSize = queryOptions.getCount().intValue();
      
      int pageSize = guiPaging.getPageSize();
      
      guiPaging.setTotalRecordCount(totalSize);
      guiPaging.setPageSize(pageSize);
      
      //if there are less than the sort limit, then just get all, no problem
      int sortLimit = 200;
      QueryPaging queryPaging = new QueryPaging();
      queryPaging.setPageSize(pageSize);
      queryPaging.setPageNumber(guiPaging.getPageNumber());
      
      queryOptions = new QueryOptions().paging(queryPaging);
      if (totalSize <= sortLimit) {
        members = group.getImmediateMembers();
        members = GuiUtils.membersSortedPaged(members, queryPaging);
      } else {
  
        //.sortAsc("m.subjectIdDb")   this kills performance
  
        members = group.getMembers(Group.getDefaultList(), queryOptions);
      }       
      
      guiPaging.setPageNumber(queryPaging.getPageNumber());
      
        //allChildren = group.getMemberships(field, members);
  //    }
  //    return allChildren;
      
        
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
    } catch (ControllerDone cd) {
      throw cd;
    } catch (Exception e) {
      throw new RuntimeException("Error with group: " + GuiUtils.escapeHtml(groupName, false) + ", " + e.getMessage(), e);
    } finally {
      GrouperSession.stopQuietly(grouperSession); 
    }
      
  
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
        
        guiResponseJs.addAction(GuiScreenAction.newAlert(GuiUtils.message("simpleMembershipUpdate.warningSubjectAlreadyMember",
            false, true, subjectLabel)));

        return;
      }
      
      group.addMember(subject);
      
      
      guiResponseJs.addAction(GuiScreenAction.newAlert(GuiUtils.message("simpleMembershipUpdate.successMemberAdded",
          false, true, subjectLabel)));
      
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
   * 
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void filterUsers(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
    
    final Subject loggedInSubject = GrouperUiJ2ee.retrieveSubjectLoggedIn();
    
    
    GrouperSession grouperSession = null;

    Properties propertiesSettings = GuiUtils.propertiesGrouperUiSettings(); 
    
    String searchTerm = httpServletRequest.getParameter("mask");

    try {
      grouperSession = GrouperSession.start(loggedInSubject);
      
      
      Set<Subject> subjects = null;
      
      StringBuilder xmlBuilder = new StringBuilder(GuiUtils.DHTMLX_OPTIONS_START);
      QueryPaging queryPaging = null;
      
      //minimum input length
      if (StringUtils.defaultString(searchTerm).length() < 2) {
        GuiUtils.dhtmlxOptionAppend(xmlBuilder, "", "Enter 2 or more characters", null);
      } else {
        subjects = SubjectFinder.findAll(searchTerm);
        
        String maxSubjectsDropDownString = GrouperUtil.propertiesValue(propertiesSettings, "grouperUi.max.subjects.dropdown");
        int maxSubjectsDropDown = GrouperUtil.intValue(maxSubjectsDropDownString, 50);

        queryPaging = new QueryPaging(maxSubjectsDropDown, 1, true);
        
        //sort and page the results
        subjects = GuiUtils.subjectsSortedPaged(subjects, queryPaging);
        
      }
      
      //convert to XML for DHTMLX
      for (Subject subject : GrouperUtil.nonNull(subjects)) {
        String value = GuiUtils.convertSubjectToValue(subject);

        String imageName = GuiUtils.imageFromSubjectSource(subject.getSource().getId());
        String label = GuiUtils.convertSubjectToLabelConfigured(subject);

        GuiUtils.dhtmlxOptionAppend(xmlBuilder, value, label, imageName);
      }

      //maybe add one more if we hit the limit
      if (queryPaging != null && subjects.size() < queryPaging.getTotalRecordCount()) {
        GuiUtils.dhtmlxOptionAppend(xmlBuilder, null, GuiUtils.message("simpleMembershipUpdate.errorUserSearchTooManyResults", false), 
            "bullet_error.png");
      } else if (subjects.size() == 0) {
        GuiUtils.dhtmlxOptionAppend(xmlBuilder, "", GuiUtils.message("simpleMembershipUpdate.errorUserSearchNoResults", false), "bullet_error.png");
      }

      xmlBuilder.append(GuiUtils.DHTMLX_OPTIONS_END);
      
      GuiUtils.printToScreen(xmlBuilder.toString(), "text/xml", false, false);

    } catch (Exception se) {
      throw new RuntimeException("Error searching for members: '" + searchTerm + "', " + se.getMessage(), se);
    } finally {
      GrouperSession.stopQuietly(grouperSession); 
    }

    //dont print the regular JSON
    throw new ControllerDone();

  }

  /**
   * handle a click or select from the advanced menu
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void advancedMenu(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    String menuItemId = httpServletRequest.getParameter("menuItemId");
    //String menuHtmlId = httpServletRequest.getParameter("menuHtmlId");
    //String menuRadioGroup = httpServletRequest.getParameter("menuRadioGroup");
    String menuCheckboxChecked  = httpServletRequest.getParameter("menuCheckboxChecked");

//    guiResponseJs.addAction(GuiScreenAction.newAlert("Menu action: menuItemId: " + menuItemId
//        + ", menuHtmlId: " + menuHtmlId 
//        + ", menuRadioGroup: " 
//        + menuRadioGroup + ", menuCheckboxChecked: " + menuCheckboxChecked));
    
    if (StringUtils.equals(menuItemId, "showGroupDetails")) {
      if (GrouperUtil.booleanValue(menuCheckboxChecked)) {
        guiResponseJs.addAction(GuiScreenAction.newHideShowNameToShow("simpleMembershipUpdateGroupDetails"));
      } else {
        guiResponseJs.addAction(GuiScreenAction.newHideShowNameToHide("simpleMembershipUpdateGroupDetails"));
      }
    } else if (StringUtils.equals(menuItemId, "multiDelete")) {
      if (GrouperUtil.booleanValue(menuCheckboxChecked)) {
        guiResponseJs.addAction(GuiScreenAction.newHideShowNameToShow("simpleMembershipUpdateDeleteMultiple"));
      } else {
        guiResponseJs.addAction(GuiScreenAction.newHideShowNameToHide("simpleMembershipUpdateDeleteMultiple"));
      }
    } else if (StringUtils.equals(menuItemId, "exportSubjectIds")) {
      guiResponseJs.addAction(GuiScreenAction.newAlertFromJsp(
          "/WEB-INF/grouperUi/templates/simpleMembershipUpdate/simpleMembershipUpdateExportSubjectIds.jsp"));
    } else {
      throw new RuntimeException("Unexpected menu id: '" + menuItemId + "'");
    }
    
  }

  /**
   * make the structure of the advanced menu
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void advancedMenuStructure(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
    
    //get the text to add to html if showing details
    GuiHideShow showGroupDetails = GuiHideShow.retrieveHideShow("simpleMembershipUpdateGroupDetails", true);
    String showGroupDetailsChecked = showGroupDetails.isShowing() ? " checked=\"true\"" : "";
    
    //get the text to add to html if showing multi delete
    GuiHideShow showMultiDelete = GuiHideShow.retrieveHideShow("simpleMembershipUpdateDeleteMultiple", true);
    String showMultiDeleteChecked = showMultiDelete.isShowing() ? " checked=\"true\"" : "";

    GuiUtils.printToScreen(
        "<?xml version=\"1.0\"?>\n"
        + "<menu>\n"
        + "  <item id=\"multiDelete\" text=\"" 
        + GuiUtils.escapeHtml(GuiUtils.message("simpleMembershipUpdate.advancedMenuDeleteMultiple"), true) 
        + "\" type=\"checkbox\" " + showMultiDeleteChecked + "><tooltip>" 
        + GuiUtils.escapeHtml(GuiUtils.message("simpleMembershipUpdate.advancedMenuDeleteMultipleTooltip"), true) + "</tooltip></item>\n"
        + "  <item id=\"showGroupDetails\" text=\"" 
        + GuiUtils.escapeHtml(GuiUtils.message("simpleMembershipUpdate.advancedMenuShowGroupDetails"), true) 
        + "\" type=\"checkbox\" " + showGroupDetailsChecked + "><tooltip>" 
        + GuiUtils.escapeHtml(GuiUtils.message("simpleMembershipUpdate.advancedMenuShowGroupDetailsTooltip"), true) + "</tooltip></item>\n"
        + "  <item id=\"importExport\" text=\"" 
        + GuiUtils.escapeHtml(GuiUtils.message("simpleMembershipUpdate.advancedMenuImportExport"), true) 
        + "\" ><tooltip>" 
        + GuiUtils.escapeHtml(GuiUtils.message("simpleMembershipUpdate.advancedMenuImportExportTooltip"), true) + "</tooltip>\n"
        + "    <item id=\"export\" text=\"" 
        + GuiUtils.escapeHtml(GuiUtils.message("simpleMembershipUpdate.advancedMenuExport"), true) 
        + "\" ><tooltip>" 
        + GuiUtils.escapeHtml(GuiUtils.message("simpleMembershipUpdate.advancedMenuExportTooltip"), true) + "</tooltip>\n"
        + "      <item id=\"exportSubjectIds\" text=\"" 
        + GuiUtils.escapeHtml(GuiUtils.message("simpleMembershipUpdate.advancedMenuExportSubjectIds"), true) 
        + "\" ><tooltip>" 
        + GuiUtils.escapeHtml(GuiUtils.message("simpleMembershipUpdate.advancedMenuExportSubjectIdsTooltip"), true) + "</tooltip></item>\n"
        + "      <item id=\"exportAll\" text=\"" 
        + GuiUtils.escapeHtml(GuiUtils.message("simpleMembershipUpdate.advancedMenuExportAll"), true) 
        + "\" ><tooltip>" 
        + GuiUtils.escapeHtml(GuiUtils.message("simpleMembershipUpdate.advancedMenuExportAllTooltip"), true) + "</tooltip></item>\n"
        //close the export
        + "   </item>\n"
        //close the import/export
        + "  </item>\n"
        //+ "  <item id=\"m3\" text=\"Help\" type=\"checkbox\" checked=\"true\"/>\n"
        //+ "  <item id=\"radio1\" text=\"Radio1\" type=\"radio\" group=\"hlm\"/>\n"
        //+ "  <item id=\"radio2\" text=\"Radio2\" type=\"radio\" group=\"hlm\"/>\n"
        + "</menu>", "text/xml", false, false);
    throw new ControllerDone();
  }
  
  /**
   * delete selected members
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void deleteMultiple(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
    
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    //lets get the selected members
    Set<String> paramNames = GrouperUiJ2ee.requestParameterNamesByPrefix("deleteMultiple_");
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
  
  /**
   * 
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void exportSubjectIdsCsv(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
    
    final Subject loggedInSubject = GrouperUiJ2ee.retrieveSubjectLoggedIn();

    GrouperSession grouperSession = null;

    Group group = null;
    String groupName = null;

    String currentMemberUuid = null;
    
    try {

      grouperSession = GrouperSession.start(loggedInSubject);

      group = this.retrieveGroup(grouperSession);
      groupName = group.getName();
      
      Set<Member> members = group.getMembers();
      
      HttpServletResponse response = GrouperUiJ2ee.retrieveHttpServletResponse(); 
      
      //say it is CSV
      response.setContentType("text/csv");
    
      String groupExtensionFileName = GuiGroup.getExportSubjectIdsFileNameStatic(group);
      
      response.setHeader ("Content-Disposition", "inline;filename=\"" + groupExtensionFileName + "\"");
      
      //just write some stuff
      PrintWriter out = null;
    
      try {
        out = response.getWriter();
      } catch (Exception e) {
        throw new RuntimeException("Cant get response.getWriter: ", e);
      }

      
      CSVWriter writer = new CSVWriter(out);
      writer.writeNext(new String[]{"sourceId", "subjectId"});
      for (Member member : members) {
        // feed in your array (or convert your data to an array)
        String[] entries = new String[]{member.getSubjectSourceId(), member.getSubjectId()};
        writer.writeNext(entries);
      }      
      writer.close();
            

      throw new ControllerDone();
    } catch (ControllerDone cd) {
      throw cd;
    } catch (Exception se) {
      throw new RuntimeException("Error exporting all members from group: " + groupName 
          + ", " + currentMemberUuid + ", " + se.getMessage(), se);
    } finally {
      GrouperSession.stopQuietly(grouperSession); 
    }

  }
  
}
