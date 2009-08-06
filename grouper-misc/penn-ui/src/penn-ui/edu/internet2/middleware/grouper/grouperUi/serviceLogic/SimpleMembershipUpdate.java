/*
 * @author mchyzer
 * $Id: SimpleMembershipUpdate.java,v 1.4 2009-08-06 04:49:40 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.grouperUi.serviceLogic;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.Stem.Scope;
import edu.internet2.middleware.grouper.exception.GroupNotFoundException;
import edu.internet2.middleware.grouper.exception.GroupNotFoundRuntimeException;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.exception.SchemaException;
import edu.internet2.middleware.grouper.grouperUi.GrouperUiJ2ee;
import edu.internet2.middleware.grouper.grouperUi.exceptions.ControllerDone;
import edu.internet2.middleware.grouper.grouperUi.json.AppState;
import edu.internet2.middleware.grouper.grouperUi.json.GuiGroup;
import edu.internet2.middleware.grouper.grouperUi.json.GuiMember;
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
        GuiUtils.dhtmlxOptionAppend(xmlBuilder, "", "Enter 2 or more characters", null);
      } else {
        Stem stem = StemFinder.findRootStem(grouperSession);
        queryOptions = new QueryOptions().paging(200, 1, true).sortAsc("displayName");
        groups = GrouperDAOFactory.getFactory().getGroup().getAllGroupsSecure("%" + searchTerm + "%", grouperSession, loggedInSubject, 
            GrouperUtil.toSet(AccessPrivilege.ADMIN, AccessPrivilege.UPDATE), queryOptions);
        
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
        GuiUtils.dhtmlxOptionAppend(xmlBuilder, "", "Not all results returned, narrow your query", "bullet_error.png");
      }
      
      if (GrouperUtil.length(groups) == 0) {
        GuiUtils.dhtmlxOptionAppend(xmlBuilder, "", "No results found", "bullet_error.png");
        
      }
      
      xmlBuilder.append(GuiUtils.DHTMLX_OPTIONS_END);
      
      GuiUtils.printToScreen(xmlBuilder.toString(), "text/xml", false, false);

    } catch (Exception se) {
      throw new RuntimeException("Error searching for groups: '" + searchTerm + "', " + se.getMessage(), se);
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
    
    final SimpleMembershipUpdateContainer simpleMembershipUpdateContainer = SimpleMembershipUpdateContainer.retrieveFromRequest();
    
    final Subject loggedInSubject = GrouperUiJ2ee.retrieveSubjectLoggedIn();
    
    AppState appState = AppState.retrieveFromRequest();
    
    //lets get the group
    String id = null;
    String name = null;
    if (appState.getUrlArgObjects() != null) {
      id = appState.getUrlArgObjects().get("groupId");
      name = appState.getUrlArgObjects().get("groupName");
    }

    if (StringUtils.isBlank(id) && StringUtils.isBlank(name)) {
      throw new RuntimeException("Need to pass in groupName or groupId in url, e.g. #operation=SimpleMembershipUpdate.init&groupName=some:group:name");
    }
    
    Group group = null;
    GrouperSession grouperSession = null;
    try {
      grouperSession = GrouperSession.start(loggedInSubject);
  
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
      throw new RuntimeException("Problem in simpleMembershipUpdateInit.init : " + id + ", " + name + ", " + e.getMessage(), e);
    }
    
    if (group == null) {
      simpleMembershipUpdateContainer.setCanFindGroup(false);
    } else {
      simpleMembershipUpdateContainer.setCanFindGroup(true);
      if (group.isComposite()) {
        simpleMembershipUpdateContainer.setCompositeGroup(true);
      } else {
        simpleMembershipUpdateContainer.setCompositeGroup(false);
        
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
        
        if (simpleMembershipUpdateContainer.isCanReadGroup() && simpleMembershipUpdateContainer.isCanUpdateGroup()) {
          GuiGroup wsGroup = new GuiGroup(group, false);
          simpleMembershipUpdateContainer.setGroup(wsGroup);
        }
        
      }
    }
    
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
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
    AppState appState = AppState.retrieveFromRequest();
    //lets get the group
    String id = null;
    String name = null;
    if (appState.getUrlArgObjects() != null) {
      id = appState.getUrlArgObjects().get("groupId");
      name = appState.getUrlArgObjects().get("groupName");
    }

    if (StringUtils.isBlank(id) && StringUtils.isBlank(name)) {
      throw new RuntimeException("Need to pass in groupName or groupId in url, e.g. #operation=SimpleMembershipUpdate.init&groupName=some:group:name");
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
      throw new RuntimeException("Problem in simpleMembershipUpdateInit : " + id + ", " + name + ", " + e.getMessage(), e);
    }
    
    if (group == null) {
      throw new RuntimeException("Cant find group: " + id + ", " + name);
    }
    return group;
  }
  
  /**
   * retrieve members
   * @param request
   * @param response
   */
  public void retrieveMembers(HttpServletRequest request, HttpServletResponse response) {
    
    final SimpleMembershipUpdateContainer simpleMembershipUpdateContainer = SimpleMembershipUpdateContainer.retrieveFromRequest();
    
    final Subject loggedInSubject = GrouperUiJ2ee.retrieveSubjectLoggedIn();
    
    
    GrouperSession grouperSession = null;
    Group group = null;
    String groupName = null;
    try {
      grouperSession = GrouperSession.start(loggedInSubject);

      group = this.retrieveGroup(grouperSession);
      groupName = group.getName();
      
      int[] numberOfRecords = new int[1];
      
      //we have the group, now get the members
      
      Set<Membership> allChildren;
      //get the size
      QueryOptions queryOptions = new QueryOptions().retrieveCount(true).retrieveResults(false);
      group.getMembers(Group.getDefaultList(), queryOptions);
      int totalSize = queryOptions.getCount().intValue();
      
      if (GrouperUtil.length(numberOfRecords) > 0) {
        numberOfRecords[0] = totalSize;
      }
      
      //if there are less than the sort limit, then just get all, no problem
      int sortLimit = 200;
  //    if (totalSize <= 200) {
  //      allChildren = group.getMemberships(Group.getDefaultList());
  //    } else {
        QueryPaging queryPaging = new QueryPaging();
        queryPaging.setPageSize(100);
        queryPaging.setFirstIndexOnPage(0);
  
        //.sortAsc("m.subjectIdDb")   this kills performance
        queryOptions = new QueryOptions().paging(queryPaging);
  
        List<Member> members = new ArrayList<Member>(group.getMembers(Group.getDefaultList(), queryOptions));
        //allChildren = group.getMemberships(field, members);
  //    }
  //    return allChildren;
  
        
        GuiMember[] guiMembers = new GuiMember[members.size()];
        for (int i=0;i<guiMembers.length;i++) {
          guiMembers[i] = new GuiMember(members.get(i));
          //TODO update this
          guiMembers[i].setDeletable(true);
          
        }
        simpleMembershipUpdateContainer.setMembers(guiMembers);
      
  //    Set<edu.internet2.middleware.grouper.Membership> memberships  = MembershipFinder.internal_findAllByGroupAndFieldAndPage(
  //        group, Group.getDefaultList(), 0, 100, 200, numberOfRecords);
  
        GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
        guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#simpleMembershipResultsList", 
            "/WEB-INF/grouperUi/templates/simpleMembershipUpdate/simpleMembershipMembershipList.jsp"));
    } catch (Exception e) {
      throw new RuntimeException("Error with group: " + groupName + ", " + e.getMessage(), e);
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
      guiResponseJs.addAction(GuiScreenAction.newAlert("The member was deleted: " + guiMember.getSubject().getDescription()));
      
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
        
        guiResponseJs.addAction(GuiScreenAction.newAlert(
            "Subject already a member: '" + GuiUtils.escapeHtml(subjectLabel, true, false) + "'"));
        return;
      }
      
      group.addMember(subject);
      
      guiResponseJs.addAction(GuiScreenAction.newAlert("Member added: " 
          + GuiUtils.escapeHtml(subjectLabel, true)));


    } catch (SubjectNotFoundException snfe) {
      
      guiResponseJs.addAction(GuiScreenAction.newAlert(
          "Subject not found: '" + GuiUtils.escapeHtml(subjectLabel, true, false) + "'"));
      return;
    } catch (SourceUnavailableException sue) {

      guiResponseJs.addAction(GuiScreenAction.newAlert(
          "Source unavailable"));
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

    String searchTerm = httpServletRequest.getParameter("mask");

    try {
      grouperSession = GrouperSession.start(loggedInSubject);
      
      
      Set<Subject> subjects = null;
      
      StringBuilder xmlBuilder = new StringBuilder(GuiUtils.DHTMLX_OPTIONS_START);
      
      if (StringUtils.defaultString(searchTerm).length() < 2) {
        GuiUtils.dhtmlxOptionAppend(xmlBuilder, "", "Enter 2 or more characters", null);
      } else {
        subjects = SubjectFinder.findAll(searchTerm);
      }
      
      for (Subject subject : GrouperUtil.nonNull(subjects)) {
        String value = GuiUtils.convertSubjectToValue(subject);

        String imageName = GuiUtils.imageFromSubjectSource(subject.getSource().getId());
        String label = GuiUtils.convertSubjectToLabel(subject);

        GuiUtils.dhtmlxOptionAppend(xmlBuilder, value, label, imageName);
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
  
}
