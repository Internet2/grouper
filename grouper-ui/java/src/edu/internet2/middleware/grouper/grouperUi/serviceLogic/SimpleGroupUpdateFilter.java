package edu.internet2.middleware.grouper.grouperUi.serviceLogic;

import java.util.List;
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
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.group.TypeOfGroup;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiMember;
import edu.internet2.middleware.grouper.grouperUi.beans.groupUpdate.GroupUpdateRequestContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiResponseJs;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.internal.dao.QueryPaging;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.ui.exceptions.ControllerDone;
import edu.internet2.middleware.grouper.ui.tags.TagUtils;
import edu.internet2.middleware.grouper.ui.util.GrouperUiUtils;
import edu.internet2.middleware.grouper.ui.util.HttpContentType;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectTooManyResults;

/**
 * filters on group update
 * @author mchyzer
 */
public class SimpleGroupUpdateFilter {

  /** logger */
  private static final Log LOG = LogFactory.getLog(SimpleGroupUpdateFilter.class);

  /**
   * edit a group
   * @param httpServletRequest
   * @param httpServletResponse
   * @param group 
   * @param checkSecurity 
   * @return true if ok, false if error
   */
  public boolean editGroupHelper(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Group group, boolean checkSecurity) {

    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();

    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();

    GroupUpdateRequestContainer groupUpdateRequestContainer = GroupUpdateRequestContainer.retrieveFromRequestOrCreate();

    GrouperSession grouperSession = null;

    try {
      grouperSession = GrouperSession.start(loggedInSubject);
      
      if (checkSecurity && !group.hasAdmin(loggedInSubject)) {
        LOG.error("Subject " + GrouperUtil.subjectToString(loggedInSubject) + " cannot admin group: " + group.getName());
        guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message("simpleGroupUpdate.errorCantEditGroup", false)));
        return false;
      }
      
      groupUpdateRequestContainer.setGroupToEdit(group);
      groupUpdateRequestContainer.setCreate(false);
      
      Subject everyEntity = SubjectFinder.findAllSubject();
      
      groupUpdateRequestContainer.setAllowAllAdmin(
          group.hasAdmin(everyEntity));
      groupUpdateRequestContainer.setAllowAllUpdate(
          group.hasUpdate(everyEntity));
      groupUpdateRequestContainer.setAllowAllView(
          group.hasView(everyEntity));
      groupUpdateRequestContainer.setAllowAllRead(
          group.hasRead(everyEntity));
      groupUpdateRequestContainer.setAllowAllOptin(
          group.hasOptin(everyEntity));
      groupUpdateRequestContainer.setAllowAllOptout(
          group.hasOptout(everyEntity));

      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#groupEditPanel", 
        "/WEB-INF/grouperUi/templates/groupUpdate/groupEditPanel.jsp"));
  
      
    } finally {
      GrouperSession.stopQuietly(grouperSession); 
    }
    return true;
  }

  /**
   * filter roles to pick one to edit
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void filterRoles(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    String searchTerm = httpServletRequest.getParameter("mask");
  
    try {
      grouperSession = GrouperSession.start(loggedInSubject);
      
      
      Set<Group> groups = null;
      
      StringBuilder xmlBuilder = new StringBuilder(GrouperUiUtils.DHTMLX_OPTIONS_START);
  
      QueryOptions queryOptions = null;
      
      if (StringUtils.defaultString(searchTerm).length() < 2) {
        GrouperUiUtils.dhtmlxOptionAppend(xmlBuilder, "", 
            GrouperUiUtils.message("simpleGroupUpdate.errorNotEnoughChars", false), "bullet_error.png");
      } else {
        queryOptions = new QueryOptions().paging(TagUtils.mediaResourceInt("simpleGroupUpdate.groupComboboxResultSize", 200), 1, true).sortAsc("theGroup.displayNameDb");
        groups = GrouperDAOFactory.getFactory().getGroup().getAllGroupsSplitScopeSecure(searchTerm, grouperSession, loggedInSubject, 
            GrouperUtil.toSet(AccessPrivilege.ADMIN, AccessPrivilege.UPDATE), queryOptions, TypeOfGroup.role);
        
        if (GrouperUtil.length(groups) == 0) {
          GrouperUiUtils.dhtmlxOptionAppend(xmlBuilder, "", 
              GrouperUiUtils.message("simpleGroupUpdate.errorNoRolesFound", false), "bullet_error.png");
        }
      }
      
      for (Group group : GrouperUtil.nonNull(groups)) {
  
        String value = group.getId();
        String label = GrouperUiUtils.escapeHtml(group.getDisplayName(), true);
        String imageName = GrouperUiUtils.imageFromSubjectSource("g:rsa");
        GrouperUiUtils.dhtmlxOptionAppend(xmlBuilder, value, label, imageName);
      }
  
      //add one more for more options if we didnt get them all
      if (queryOptions != null && queryOptions.getCount() != null 
          && groups != null && queryOptions.getCount() > groups.size()) {
        GrouperUiUtils.dhtmlxOptionAppend(xmlBuilder, "", 
            GrouperUiUtils.message("simpleGroupUpdate.errorTooManyRoles", false), "bullet_error.png");
      }
      
      
      xmlBuilder.append(GrouperUiUtils.DHTMLX_OPTIONS_END);
      
      GrouperUiUtils.printToScreen(xmlBuilder.toString(), HttpContentType.TEXT_XML, false, false);
  
    } catch (Exception se) {
      LOG.error("Error searching for role: '" + searchTerm + "', " + se.getMessage(), se);
      
      //dont rethrow or the control will get confused
      StringBuilder xmlBuilder = new StringBuilder(GrouperUiUtils.DHTMLX_OPTIONS_START);
      GrouperUiUtils.dhtmlxOptionAppend(xmlBuilder, null, 
          GrouperUiUtils.escapeHtml("Error searching for roles: " + searchTerm + ", " + se.getMessage(), true), null);
      xmlBuilder.append(GrouperUiUtils.DHTMLX_OPTIONS_END);
      GrouperUiUtils.printToScreen(xmlBuilder.toString(), HttpContentType.TEXT_XML, false, false);
    } finally {
      GrouperSession.stopQuietly(grouperSession); 
    }
  
    //dont print the regular JSON
    throw new ControllerDone();
  
  }

  /**
   * edit a group
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void editGroupButton(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
  
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
  
    String groupId = httpServletRequest.getParameter("simpleGroupUpdatePickGroup");
  
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
    if (StringUtils.isBlank(groupId)) {
      guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message("simpleGroupUpdate.errorCantEditGroup", false)));
      return;
    }
    
    GrouperSession grouperSession = null;
  
    Group group = null;
    
    try {
      grouperSession = GrouperSession.start(loggedInSubject);
      
      try {
      
        group = GroupFinder.findByUuid(grouperSession, groupId, true);
      } catch (Exception e) {
        LOG.info("Error searching for group: " + groupId, e);
        guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message("simpleGroupUpdate.errorCantEditGroup", false)));
        return;
        
      }  
      
    } finally {
      GrouperSession.stopQuietly(grouperSession); 
    }
    editGroupHelper(httpServletRequest, httpServletResponse, group, true);
  }

  /**
   * new group
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void newGroupButton(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
  
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
  
    GroupUpdateRequestContainer groupUpdateRequestContainer = GroupUpdateRequestContainer.retrieveFromRequestOrCreate();
  
    GrouperSession grouperSession = null;
  
    try {
      grouperSession = GrouperSession.start(loggedInSubject);
      
      Group group = new Group();
      groupUpdateRequestContainer.setGroupToEdit(group);
      groupUpdateRequestContainer.setCreate(true);
      
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
      groupUpdateRequestContainer.setAllowAllAdmin(
          GrouperConfig.getPropertyBoolean("groups.create.grant.all.admin", false));
      groupUpdateRequestContainer.setAllowAllUpdate(
          GrouperConfig.getPropertyBoolean("groups.create.grant.all.update", false));
      groupUpdateRequestContainer.setAllowAllRead(
          GrouperConfig.getPropertyBoolean("groups.create.grant.all.read", false));
      groupUpdateRequestContainer.setAllowAllView(
          GrouperConfig.getPropertyBoolean("groups.create.grant.all.view", false));
      groupUpdateRequestContainer.setAllowAllOptin(
          GrouperConfig.getPropertyBoolean("groups.create.grant.all.optin", false));
      groupUpdateRequestContainer.setAllowAllOptout(
          GrouperConfig.getPropertyBoolean("groups.create.grant.all.optout", false));
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#groupEditPanel", 
        "/WEB-INF/grouperUi/templates/groupUpdate/groupEditPanel.jsp"));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession); 
    }
  
  
  }

  /**
   * filter creatable folders
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void filterCreatableNamespace(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
    
    new SimpleAttributeUpdateFilter().filterCreatableNamespace(httpServletRequest, httpServletResponse);
    
  }

  /**
   * add a subject to the panel below
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void addPrivilegeSubject(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
  
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
  
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    GroupUpdateRequestContainer groupUpdateRequestContainer = GroupUpdateRequestContainer.retrieveFromRequestOrCreate();
  
    GrouperSession grouperSession = null;
  
    try {
      grouperSession = GrouperSession.start(loggedInSubject);
            
      String additionalSubjectString = httpServletRequest.getParameter("simpleGroupUpdatePrivilegeSubject");
  
      if (StringUtils.isBlank(additionalSubjectString)) {
        LOG.error("Why is subject blank?");
        guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message("simpleGroupUpdate.additionalPrivilegeSubjectNotFound", false)));
        return;
      }
  
      Subject additionalSubject = null;

      try {
        additionalSubject = GrouperUiUtils.findSubject(additionalSubjectString, true); 
      } catch (Exception e) {
        LOG.error("Error finding subject: " + additionalSubjectString, e);
        guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message("simpleGroupUpdate.additionalPrivilegeSubjectNotFound", false)));
        return;
      }
      
      List<GuiMember> guiMembers = groupUpdateRequestContainer.privilegeAdditionalGuiMembers();
      
      Member additionalMember = MemberFinder.findBySubject(grouperSession, additionalSubject, true);
      guiMembers.add(0,new GuiMember(additionalMember));
    } finally {
      GrouperSession.stopQuietly(grouperSession); 
    }
    
    //show the whole panel again
    new SimpleGroupUpdate().groupEditPanelPrivileges(httpServletRequest, httpServletResponse);  
      
  
  }

  /**
   * called in the combobox to list the users
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void filterPrivilegeUsers(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
  
    String uuid = httpServletRequest.getParameter("groupToEditId");
    
    GroupUpdateRequestContainer groupUpdateRequestContainer = GroupUpdateRequestContainer.retrieveFromRequestOrCreate();
  
    GrouperSession grouperSession = null;
  
    String searchTerm = httpServletRequest.getParameter("mask");
    Group group = null;
    boolean error = false;
    
    try {
  
      StringBuilder xmlBuilder = new StringBuilder(GrouperUiUtils.DHTMLX_OPTIONS_START);

      //if editing, then this must be there, or it has been tampered with
      if (!error && StringUtils.isBlank(uuid)) {
        GrouperUiUtils.dhtmlxOptionAppend(xmlBuilder, "", 
            GrouperUiUtils.message("simpleGroupUpdate.errorCantEditGroup", false), null);
        error = true;
      }
  
      grouperSession = GrouperSession.start(loggedInSubject);
      
      if (!error) {
        try {
          group = GroupFinder.findByUuid(grouperSession, uuid, true);
        } catch (Exception e) {
          LOG.info("Error searching for group: " + uuid, e);
          GrouperUiUtils.dhtmlxOptionAppend(xmlBuilder, "", 
              GrouperUiUtils.message("simpleGroupUpdate.errorCantEditGroup", false), null);
          error = true;
          
        }
    
        if (!error && !group.hasAdmin(loggedInSubject)) {
          LOG.error("Subject " + GrouperUtil.subjectToString(loggedInSubject) + " cannot admin group: " + group.getName());
          GrouperUiUtils.dhtmlxOptionAppend(xmlBuilder, "", 
              GrouperUiUtils.message("simpleGroupUpdate.errorCantEditGroup", false), null);
          error = true;
        }
      }

      
      if (!error) {
  
        groupUpdateRequestContainer.setGroupToEdit(group);
        
        Set<Subject> subjects = null;
        
        QueryPaging queryPaging = null;
        
        //minimum input length
        boolean tooManyResults = false;
        if (StringUtils.defaultString(searchTerm).length() < 2) {
          GrouperUiUtils.dhtmlxOptionAppend(xmlBuilder, "", 
              GrouperUiUtils.message("simpleGroupUpdate.errorNotEnoughChars", false), null);
        } else {
          try {
            
            subjects = SubjectFinder.findPageInStem(group.getParentStemName(), searchTerm).getResults();            
            
            int maxSubjectsDropDown = TagUtils.mediaResourceInt("simpleGroupUpdate.groupPrivilegeUserComboboxResultSize", 50);
  
            queryPaging = new QueryPaging(maxSubjectsDropDown, 1, true);
          
            //sort and page the results
            subjects = GrouperUiUtils.subjectsSortedPaged(subjects, queryPaging);
  
          } catch (SubjectTooManyResults stmr) {
            tooManyResults = true;
          }
        }
        
        //convert to XML for DHTMLX
        for (Subject subject : GrouperUtil.nonNull(subjects)) {
          String value = GrouperUiUtils.convertSubjectToValue(subject);
    
          String imageName = GrouperUiUtils.imageFromSubjectSource(subject.getSource().getId());
          String label = GrouperUiUtils.escapeHtml(GrouperUiUtils.convertSubjectToLabelConfigured(subject), true);
    
          GrouperUiUtils.dhtmlxOptionAppend(xmlBuilder, value, label, imageName);
        }
    
        //maybe add one more if we hit the limit
        if (tooManyResults || queryPaging != null && GrouperUtil.length(subjects) < queryPaging.getTotalRecordCount()) {
          GrouperUiUtils.dhtmlxOptionAppend(xmlBuilder, null, 
              GrouperUiUtils.message("simpleGroupUpdate.errorTooManyPrivilegeSubjects", false), 
              "bullet_error.png");
        } else if (GrouperUtil.length(subjects) == 0) {
          GrouperUiUtils.dhtmlxOptionAppend(xmlBuilder, "", 
              GrouperUiUtils.message("simpleGroupUpdate.errorPrivilegeUserSearchNoResults", false), 
              "bullet_error.png");
        }
  
      }
  
      xmlBuilder.append(GrouperUiUtils.DHTMLX_OPTIONS_END);
      
      GrouperUiUtils.printToScreen(xmlBuilder.toString(), HttpContentType.TEXT_XML, false, false);
  
    } catch (Exception se) {
      LOG.error("Error searching for subjects: '" + searchTerm + "', " + se.getMessage(), se);
  
      //dont rethrow or the control will get confused
      StringBuilder xmlBuilder = new StringBuilder(GrouperUiUtils.DHTMLX_OPTIONS_START);
      GrouperUiUtils.dhtmlxOptionAppend(xmlBuilder, null, 
          GrouperUiUtils.escapeHtml("Error searching for subjects: " + searchTerm + ", " + se.getMessage(), true), null);
      xmlBuilder.append(GrouperUiUtils.DHTMLX_OPTIONS_END);
      GrouperUiUtils.printToScreen(xmlBuilder.toString(), HttpContentType.TEXT_XML, false, false);
    } finally {
      GrouperSession.stopQuietly(grouperSession); 
    }
  
    //dont print the regular JSON
    throw new ControllerDone();
  
  }

  /**
   * filter groups to pick one to edit
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void filterGroups(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    String searchTerm = httpServletRequest.getParameter("mask");
  
    try {
      grouperSession = GrouperSession.start(loggedInSubject);
      
      
      Set<Group> groups = null;
      
      StringBuilder xmlBuilder = new StringBuilder(GrouperUiUtils.DHTMLX_OPTIONS_START);
  
      QueryOptions queryOptions = null;
      
      if (StringUtils.defaultString(searchTerm).length() < 2) {
        GrouperUiUtils.dhtmlxOptionAppend(xmlBuilder, "", 
            GrouperUiUtils.message("simpleGroupUpdate.errorNotEnoughChars", false), "bullet_error.png");
      } else {
        queryOptions = new QueryOptions().paging(TagUtils.mediaResourceInt("simpleGroupUpdate.groupComboboxResultSize", 200), 1, true).sortAsc("theGroup.displayNameDb");
        groups = GrouperDAOFactory.getFactory().getGroup().getAllGroupsSplitScopeSecure(searchTerm, grouperSession, loggedInSubject, 
            GrouperUtil.toSet(AccessPrivilege.ADMIN), queryOptions, null);
        
        if (GrouperUtil.length(groups) == 0) {
          GrouperUiUtils.dhtmlxOptionAppend(xmlBuilder, "", 
              GrouperUiUtils.message("simpleGroupUpdate.errorNoGroupsFound", false), "bullet_error.png");
        }
      }
      
      for (Group group : GrouperUtil.nonNull(groups)) {
  
        String value = group.getId();
        String label = GrouperUiUtils.escapeHtml(group.getDisplayName(), true);
        String imageName = null;
        if (group.getTypeOfGroup() == TypeOfGroup.role) {
          imageName = GrouperUiUtils.imageFromSubjectSource("g:rsa");
        } else {
          imageName = GrouperUiUtils.imageFromSubjectSource("g:gsa");
        }
        GrouperUiUtils.dhtmlxOptionAppend(xmlBuilder, value, label, imageName);
      }
  
      //add one more for more options if we didnt get them all
      if (queryOptions != null && queryOptions.getCount() != null 
          && groups != null && queryOptions.getCount() > groups.size()) {
        GrouperUiUtils.dhtmlxOptionAppend(xmlBuilder, "", 
            GrouperUiUtils.message("simpleGroupUpdate.errorTooManyGroups", false), "bullet_error.png");
      }
      
      
      xmlBuilder.append(GrouperUiUtils.DHTMLX_OPTIONS_END);
      
      GrouperUiUtils.printToScreen(xmlBuilder.toString(), HttpContentType.TEXT_XML, false, false);
  
    } catch (Exception se) {
      LOG.error("Error searching for group: '" + searchTerm + "', " + se.getMessage(), se);
      
      //dont rethrow or the control will get confused
      StringBuilder xmlBuilder = new StringBuilder(GrouperUiUtils.DHTMLX_OPTIONS_START);
      GrouperUiUtils.dhtmlxOptionAppend(xmlBuilder, null, 
          GrouperUiUtils.escapeHtml("Error searching for groups: " + searchTerm + ", " + se.getMessage(), true), null);
      xmlBuilder.append(GrouperUiUtils.DHTMLX_OPTIONS_END);
      GrouperUiUtils.printToScreen(xmlBuilder.toString(), HttpContentType.TEXT_XML, false, false);
    } finally {
      GrouperSession.stopQuietly(grouperSession); 
    }
  
    //dont print the regular JSON
    throw new ControllerDone();
  
  }
  
}


