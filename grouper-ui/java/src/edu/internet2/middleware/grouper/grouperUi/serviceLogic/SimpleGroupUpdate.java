/*******************************************************************************
 * Copyright 2012 Internet2
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
/*
 * @author mchyzer
 * $Id: SimpleMembershipUpdate.java,v 1.7 2009-11-13 14:56:25 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.grouperUi.serviceLogic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.group.TypeOfGroup;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiMember;
import edu.internet2.middleware.grouper.grouperUi.beans.groupUpdate.GroupUpdateRequestContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiPaging;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiResponseJs;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.internal.dao.QueryPaging;
import edu.internet2.middleware.grouper.membership.MembershipType;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.permissions.role.Role;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.Privilege;
import edu.internet2.middleware.grouper.privs.PrivilegeAssignType;
import edu.internet2.middleware.grouper.privs.PrivilegeContainer;
import edu.internet2.middleware.grouper.privs.PrivilegeContainerImpl;
import edu.internet2.middleware.grouper.privs.PrivilegeSubjectContainer;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.ui.util.GrouperUiUtils;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;


/**
 * main ajax methods for simple group update module
 */
public class SimpleGroupUpdate {
  
  /**
   * index page of application
   * @param request
   * @param response
   */
  public void index(HttpServletRequest request, HttpServletResponse response) {
    
    //setup the container
    GroupUpdateRequestContainer.retrieveFromRequestOrCreate();

    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();

    guiResponseJs.addAction(GuiScreenAction.newScript("document.title = '" 
        + GrouperUiUtils.message("simpleGroupUpdate.title", false) + "'"));
    guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#topDiv", 
        "/WEB-INF/grouperUi/templates/common/commonTop.jsp"));

    guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#bodyDiv", 
        "/WEB-INF/grouperUi/templates/groupUpdate/simpleGroupUpdateIndex.jsp"));

  }

  /**
   * index page of application
   * @param request
   * @param response
   */
  public void createEdit(HttpServletRequest request, HttpServletResponse response) {
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
  
    GrouperSession grouperSession = null;
  
    Group group = null;
    
    try {
      grouperSession = GrouperSession.start(loggedInSubject);
  
      //setup the container
      GroupUpdateRequestContainer groupUpdateRequestContainer = GroupUpdateRequestContainer.retrieveFromRequestOrCreate();
    
      guiResponseJs.addAction(GuiScreenAction.newScript("document.title = '" 
          + GrouperUiUtils.message("simpleGroupUpdate.addEditTitle", false) + "'"));
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#topDiv", 
          "/WEB-INF/grouperUi/templates/common/commonTop.jsp"));
  
      
      String groupId = request.getParameter("groupId");
      
      if (!StringUtils.isBlank(groupId)) {
        
        //if editing, then this must be there, or it has been tampered with
        try {
          group = GroupFinder.findByUuid(grouperSession, groupId, true);
        } catch (Exception e) {
          LOG.info("Error searching for group: " + groupId, e);
          guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message("simpleGroupUpdate.errorCantEditGroup", false)));
          return;
          
        }
      }
      
      String groupName = request.getParameter("groupName");
      
      if (!StringUtils.isBlank(groupName)) {
        
        //if editing, then this must be there, or it has been tampered with
        try {
          group = GroupFinder.findByName(grouperSession, groupName, true);
        } catch (Exception e) {
          LOG.info("Error searching for group: " + groupName, e);
          guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message("simpleGroupUpdate.errorCantEditGroup", false)));
          return;
          
        }
      }
      
      if (group != null && !group.hasAdmin(loggedInSubject)) {
        LOG.error("Subject " + GrouperUtil.subjectToString(loggedInSubject) + " cannot admin group: " + group.getName());
        guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message("simpleGroupUpdate.errorCantEditGroup", false)));
        return;
      }
        
      groupUpdateRequestContainer.setGroupToEdit(group);
    
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#bodyDiv", 
        "/WEB-INF/grouperUi/templates/groupUpdate/simpleGroupCreateEditInit.jsp"));
  
    } finally {
      GrouperSession.stopQuietly(grouperSession); 
    }
  
    if (group != null) {
      new SimpleGroupUpdateFilter().editGroupHelper(request, response, group, true);
    }
  
  }

  /**
   * click save button on edit panel
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void groupEditPanelSubmit(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
  
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
  
    GroupUpdateRequestContainer groupUpdateRequestContainer = GroupUpdateRequestContainer.retrieveFromRequestOrCreate();
  
    GrouperSession grouperSession = null;
  
    try {
      grouperSession = GrouperSession.start(loggedInSubject);
      
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
      Group group = null;
      
      //marshal params
      String extension = httpServletRequest.getParameter("groupToEditExtension");
      String uuid = httpServletRequest.getParameter("groupToEditId");
      
      boolean isCreate = StringUtils.isBlank(uuid);
  
      String stemName = null;
      Stem stem = null;      
      
      if (!isCreate) {
        //if editing, then this must be there, or it has been tampered with
        try {
          group = GroupFinder.findByUuid(grouperSession, uuid, true);
        } catch (Exception e) {
          LOG.info("Error searching for group: " + uuid, e);
          guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message("simpleGroupUpdate.errorCantEditGroup", false)));
          return;
          
        }
      
        if (group == null || !group.hasAdmin(loggedInSubject)) {
          LOG.error("Subject " + GrouperUtil.subjectToString(loggedInSubject) + " cannot admin group: " + group.getName());
          guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message("simpleGroupUpdate.errorCantEditGroup", false)));
          return;
        }
        
        stemName = group.getParentStem().getName();
        extension = group.getExtension();

      } else {
        
        String stemId = httpServletRequest.getParameter("simpleGroupUpdatePickNamespace");
        
        if (StringUtils.isBlank(stemId)) {
          guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message("simpleGroupUpdate.errorFolderRequired", false)));
          return;
        }
  
        if (StringUtils.isBlank(extension)) {
          guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message("simpleGroupUpdate.errorExtensionRequired", false)));
          return;
        }
       
        
        stem = StemFinder.findByUuid(grouperSession, stemId, true);
        final String GROUP_NAME = stem.getName() + ":" + extension;
        //see if the group exists, do this as admin, since the user might not be able to see it
        boolean exists = (Boolean)GrouperSession.callbackGrouperSession(grouperSession.internal_getRootSession(), new GrouperSessionHandler() {
          
          @Override
          public Object callback(GrouperSession grouperSessionSystem) throws GrouperSessionException {

            Group group = GroupFinder.findByName(grouperSessionSystem, GROUP_NAME, false, new QueryOptions().secondLevelCache(false));
            return group != null;
          }
        });
        
        if (exists) {
          guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message("simpleGroupUpdate.errorInsertGroupExists", false)));
          return;
        }
        
      }

      String displayExtension = httpServletRequest.getParameter("groupToEditDisplayExtension");

      if (StringUtils.isBlank(displayExtension)) {
        guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message("simpleGroupUpdate.errorDisplayExtensionRequired", false)));
        return;
      }
      TypeOfGroup typeOfGroup = null;
      {
        String groupTypeString = httpServletRequest.getParameter("groupToEditType");
        
        if (StringUtils.isBlank(groupTypeString)) {
          throw new RuntimeException("Why is groupType null?");
        }
  
        typeOfGroup = TypeOfGroup.valueOfIgnoreCase(groupTypeString, true);
      }

      //create it... all validation should be done at this point, so we dont create, then give an error, and have a partial create
      if (isCreate) {
        
        stemName = stem.getName();
        group = new GroupSave(grouperSession).assignName(stemName + ":" + extension).assignDisplayExtension(displayExtension).assignTypeOfGroup(typeOfGroup).save();
        
  
      } else {
        group.setDisplayExtension(displayExtension);
      }
      
      {
        String description = httpServletRequest.getParameter("groupToEditDescription");
        
        group.setDescription(description);
      }
      
      group.setTypeOfGroup(typeOfGroup);
      
      group.store();
  
      Subject everyEntitySubject = SubjectFinder.findAllSubject();
      
      //entities only have VIEW or ADMIN
      if (group.getTypeOfGroup() != TypeOfGroup.entity) {


        {
          boolean groupToEditAllowAllOptin = GrouperUtil.booleanValue(httpServletRequest.getParameter("groupToEditAllowAllOptin"), false);
          groupUpdateRequestContainer.setAllowAllOptin(groupToEditAllowAllOptin);
          if (groupToEditAllowAllOptin != group.hasOptin(everyEntitySubject)) {
            if (groupToEditAllowAllOptin) {
              group.grantPriv(everyEntitySubject, AccessPrivilege.OPTIN, false);
            } else{
              group.revokePriv(everyEntitySubject, AccessPrivilege.OPTIN, false);
            }
          }
        }
        {
          boolean groupToEditAllowAllOptout = GrouperUtil.booleanValue(httpServletRequest.getParameter("groupToEditAllowAllOptout"), false);
          groupUpdateRequestContainer.setAllowAllOptout(groupToEditAllowAllOptout);
          if (groupToEditAllowAllOptout != group.hasOptout(everyEntitySubject)) {
            if (groupToEditAllowAllOptout) {
              group.grantPriv(everyEntitySubject, AccessPrivilege.OPTOUT, false);
            } else{
              group.revokePriv(everyEntitySubject, AccessPrivilege.OPTOUT, false);
            }
          }
        }
        {
          boolean groupToEditAllowAllUpdate = GrouperUtil.booleanValue(httpServletRequest.getParameter("groupToEditAllowAllUpdate"), false);
          groupUpdateRequestContainer.setAllowAllUpdate(groupToEditAllowAllUpdate);
          if (groupToEditAllowAllUpdate != group.hasUpdate(everyEntitySubject)) {
            if (groupToEditAllowAllUpdate) {
              group.grantPriv(everyEntitySubject, AccessPrivilege.UPDATE, false);
            } else{
              group.revokePriv(everyEntitySubject, AccessPrivilege.UPDATE, false);
            }
          }
        }
        {
          boolean groupToEditAllowAllRead = GrouperUtil.booleanValue(httpServletRequest.getParameter("groupToEditAllowAllRead"), false);
          groupUpdateRequestContainer.setAllowAllRead(groupToEditAllowAllRead);
          if (groupToEditAllowAllRead != group.hasRead(everyEntitySubject)) {
            if (groupToEditAllowAllRead) {
              group.grantPriv(everyEntitySubject, AccessPrivilege.READ, false);
            } else{
              group.revokePriv(everyEntitySubject, AccessPrivilege.READ, false);
            }
          }
        }
      }      
      {
        boolean groupToEditAllowAllView = GrouperUtil.booleanValue(httpServletRequest.getParameter("groupToEditAllowAllView"), false);
        groupUpdateRequestContainer.setAllowAllView(groupToEditAllowAllView);
        if (groupToEditAllowAllView != group.hasView(everyEntitySubject)) {
          if (groupToEditAllowAllView) {
            group.grantPriv(everyEntitySubject, AccessPrivilege.VIEW, false);
          } else{
            group.revokePriv(everyEntitySubject, AccessPrivilege.VIEW, false);
          }
        }
      }
        
      {
        boolean groupToEditAllowAllAdmin = GrouperUtil.booleanValue(httpServletRequest.getParameter("groupToEditAllowAllAdmin"), false);
        groupUpdateRequestContainer.setAllowAllAdmin(groupToEditAllowAllAdmin);
        if (groupToEditAllowAllAdmin != group.hasAdmin(everyEntitySubject)) {
          if (groupToEditAllowAllAdmin) {
            groupUpdateRequestContainer.setAllowAllAdmin(true);
            group.grantPriv(everyEntitySubject, AccessPrivilege.ADMIN, false);
          } else{
            group.revokePriv(everyEntitySubject, AccessPrivilege.ADMIN, false);
          }
        }
      }
      
      guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message(
          "simpleGroupUpdate.groupSaved", false, true, group.getName())));
  
      groupUpdateRequestContainer.setGroupToEdit(group);
      groupUpdateRequestContainer.setCreate(false);
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#groupEditPanel", 
        "/WEB-INF/grouperUi/templates/groupUpdate/groupEditPanel.jsp"));
  
    } finally {
      GrouperSession.stopQuietly(grouperSession); 
    }
  
  
  }
  
  
  
  /**
   * delete button was pressed on the group edit panel
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void groupEditPanelDelete(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
  
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
  
    GroupUpdateRequestContainer groupUpdateRequestContainer = GroupUpdateRequestContainer.retrieveFromRequestOrCreate();
  
    GrouperSession grouperSession = null;
  
    try {
      grouperSession = GrouperSession.start(loggedInSubject);
      
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
      Group group = null;
      
      String uuid = httpServletRequest.getParameter("groupToEditId");
      
      if (StringUtils.isBlank(uuid)) {
        throw new RuntimeException("Why is uuid blank????");
      }
  
      //if editing, then this must be there, or it has been tampered with
      try {
        group = GroupFinder.findByUuid(grouperSession, uuid, true);
      } catch (Exception e) {
        LOG.info("Error searching for group: " + uuid, e);
        guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message("simpleGroupUpdate.errorCantEditGroup", false)));
        return;
        
      }
    
      if (group == null || !group.hasAdmin(loggedInSubject)) {
        LOG.error("Subject " + GrouperUtil.subjectToString(loggedInSubject) + " cannot admin group: " + group.getName());
        guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message("simpleGroupUpdate.errorCantEditGroup", false)));
        return;
      }
      
      //delete it
      group.delete();
      
      guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message(
        "simpleGroupUpdate.groupDeleted", false, true, group.getName())));
  
      groupUpdateRequestContainer.setGroupToEdit(null);
      
      //clear out whole screen
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#bodyDiv", 
        "/WEB-INF/grouperUi/templates/groupUpdate/simpleGroupCreateEditInit.jsp"));
      
      
    } finally {
      GrouperSession.stopQuietly(grouperSession); 
    }
  }

  /**
   * privileges button was pressed on the group edit panel
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void groupEditPanelPrivileges(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
  
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
  
    GroupUpdateRequestContainer groupUpdateRequestContainer = GroupUpdateRequestContainer.retrieveFromRequestOrCreate();
  
    GrouperSession grouperSession = null;
  
    try {
      grouperSession = GrouperSession.start(loggedInSubject);
      
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
      Group group = groupUpdateRequestContainer.getGroupToEdit();
      
      if (group == null) {
      
        String uuid = httpServletRequest.getParameter("groupToEditId");
        
        if (StringUtils.isBlank(uuid)) {
          throw new RuntimeException("Why is uuid blank????");
        }
    
        //if editing, then this must be there, or it has been tampered with
        try {
          group = GroupFinder.findByUuid(grouperSession, uuid, true);
        } catch (Exception e) {
          LOG.info("Error searching for group: " + uuid, e);
          guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message("simpleGroupUpdate.errorCantEditGroup", false)));
          return;
          
        }
      
        if (group == null || !group.hasAdmin(loggedInSubject)) {
          LOG.error("Subject " + GrouperUtil.subjectToString(loggedInSubject) + " cannot admin group: " + group.getName());
          guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message("simpleGroupUpdate.errorCantEditGroup", false)));
          return;
        }
      }
      
      //might be additional members at top of list
      List<GuiMember> additionalGuiMembers = groupUpdateRequestContainer.privilegeAdditionalGuiMembers();
      
      Set<Member> additionalMembers = null;
      
      if (GrouperUtil.length(additionalGuiMembers) > 0) {
        
        //needs to be ordered
        additionalMembers = new LinkedHashSet<Member>();
        for (GuiMember guiMember : additionalGuiMembers) {
          additionalMembers.add(guiMember.getMember());
        }
        
      }
      
      boolean showIndirectPrivileges = groupUpdateRequestContainer.isShowIndirectPrivilegesComputed();
      
      GuiPaging guiPaging = GuiPaging.retrievePaging("simpleGroupUpdatePrivileges", false);
      if (guiPaging == null) {
        GuiPaging.init("simpleGroupUpdatePrivileges");
        guiPaging = GuiPaging.retrievePaging("simpleGroupUpdatePrivileges", true);
      }
      
      
      QueryPaging queryPaging = guiPaging.queryPaging();
      //this could have messed up some stuff
      queryPaging.setPageNumber(guiPaging.getPageNumber());
      queryPaging.setDoTotalCount(true);
      
      Set<PrivilegeSubjectContainer> privilegeSubjectContainers = grouperSession
        .getAccessResolver().retrievePrivileges(group, null, 
            showIndirectPrivileges ? null : MembershipType.IMMEDIATE, queryPaging, additionalMembers);
      
      //set back the total record count
      guiPaging.setTotalRecordCount(queryPaging.getTotalRecordCount());
      
      Subject everyEntitySubject = SubjectFinder.findAllSubject();
      boolean allowAllAdmin = group.hasAdmin(everyEntitySubject);
      boolean allowAllUpdate = group.hasUpdate(everyEntitySubject);
      boolean allowAllRead = group.hasRead(everyEntitySubject);
      boolean allowAllView = group.hasView(everyEntitySubject);
      boolean allowAllOptin = group.hasOptin(everyEntitySubject);
      boolean allowAllOptout = group.hasOptout(everyEntitySubject);
      
      //lets setup the hierarchies, inheritance, from allow all, and actions which imply other actions
      for (PrivilegeSubjectContainer privilegeSubjectContainer : GrouperUtil.nonNull(privilegeSubjectContainers)) {
        if (privilegeSubjectContainer.getPrivilegeContainers() == null) {
          privilegeSubjectContainer.setPrivilegeContainers(new HashMap<String, PrivilegeContainer>());
        }
        //go through each
        boolean canAdmin = false;
        {
          PrivilegeContainer admin = privilegeSubjectContainer.getPrivilegeContainers().get(AccessPrivilege.ADMIN.getName());
          canAdmin = admin != null || allowAllAdmin;
          //see if inheriting
          if (allowAllAdmin) {
            if (admin == null) {
              admin = new PrivilegeContainerImpl();
              admin.setPrivilegeName(AccessPrivilege.ADMIN.getName());
              privilegeSubjectContainer.getPrivilegeContainers().put(AccessPrivilege.ADMIN.getName(), admin);
              admin.setPrivilegeAssignType(PrivilegeAssignType.EFFECTIVE);
            } else {
              admin.setPrivilegeAssignType(PrivilegeAssignType.convert(admin.getPrivilegeAssignType(), PrivilegeAssignType.EFFECTIVE));
            }
          }
        }
        boolean canUpdate = false;
        {
          PrivilegeContainer update = privilegeSubjectContainer.getPrivilegeContainers().get(AccessPrivilege.UPDATE.getName());
          canUpdate = update != null || allowAllUpdate || canAdmin;
          //see if inheriting
          if (allowAllUpdate || canAdmin) {
            if (update == null) {
              update = new PrivilegeContainerImpl();
              update.setPrivilegeName(AccessPrivilege.UPDATE.getName());
              privilegeSubjectContainer.getPrivilegeContainers().put(AccessPrivilege.UPDATE.getName(), update);
              update.setPrivilegeAssignType(PrivilegeAssignType.EFFECTIVE);
            } else {
              update.setPrivilegeAssignType(PrivilegeAssignType.convert(update.getPrivilegeAssignType(), PrivilegeAssignType.EFFECTIVE));
            }
          }
        }
        boolean canRead = false;
        {
          PrivilegeContainer read = privilegeSubjectContainer.getPrivilegeContainers().get(AccessPrivilege.READ.getName());
          canRead = read != null || allowAllRead || canAdmin;
          //see if inheriting
          if (allowAllRead || canAdmin) {
            if (read == null) {
              read = new PrivilegeContainerImpl();
              read.setPrivilegeName(AccessPrivilege.READ.getName());
              privilegeSubjectContainer.getPrivilegeContainers().put(AccessPrivilege.READ.getName(), read);
              read.setPrivilegeAssignType(PrivilegeAssignType.EFFECTIVE);
            } else {
              read.setPrivilegeAssignType(PrivilegeAssignType.convert(read.getPrivilegeAssignType(), PrivilegeAssignType.EFFECTIVE));
            }
          }
        }
        boolean canOptin = false;
        {
          PrivilegeContainer optin = privilegeSubjectContainer.getPrivilegeContainers().get(AccessPrivilege.OPTIN.getName());
          canOptin = optin != null || allowAllOptin || canAdmin || canUpdate;
          //see if inheriting
          if (allowAllOptin || canAdmin || canUpdate) {
            if (optin == null) {
              optin = new PrivilegeContainerImpl();
              optin.setPrivilegeName(AccessPrivilege.OPTIN.getName());
              privilegeSubjectContainer.getPrivilegeContainers().put(AccessPrivilege.OPTIN.getName(), optin);
              optin.setPrivilegeAssignType(PrivilegeAssignType.EFFECTIVE);
            } else {
              optin.setPrivilegeAssignType(PrivilegeAssignType.convert(optin.getPrivilegeAssignType(), PrivilegeAssignType.EFFECTIVE));
            }
          }
        }
        boolean canOptout = false;
        {
          PrivilegeContainer optout = privilegeSubjectContainer.getPrivilegeContainers().get(AccessPrivilege.OPTOUT.getName());
          canOptout = optout != null || allowAllOptout || canAdmin || canUpdate;
          //see if inheriting
          if (allowAllOptout || canAdmin || canUpdate) {
            if (optout == null) {
              optout = new PrivilegeContainerImpl();
              optout.setPrivilegeName(AccessPrivilege.OPTOUT.getName());
              privilegeSubjectContainer.getPrivilegeContainers().put(AccessPrivilege.OPTOUT.getName(), optout);
              optout.setPrivilegeAssignType(PrivilegeAssignType.EFFECTIVE);
            } else {
              optout.setPrivilegeAssignType(PrivilegeAssignType.convert(optout.getPrivilegeAssignType(), PrivilegeAssignType.EFFECTIVE));
            }
          }
        }
        {
          PrivilegeContainer view = privilegeSubjectContainer.getPrivilegeContainers().get(AccessPrivilege.VIEW.getName());
          //see if inheriting
          if (allowAllView || canAdmin || canUpdate || canRead || canOptin || canOptout) {
            if (view == null) {
              view = new PrivilegeContainerImpl();
              view.setPrivilegeName(AccessPrivilege.VIEW.getName());
              privilegeSubjectContainer.getPrivilegeContainers().put(AccessPrivilege.VIEW.getName(), view);
              view.setPrivilegeAssignType(PrivilegeAssignType.EFFECTIVE);
            } else {
              view.setPrivilegeAssignType(PrivilegeAssignType.convert(view.getPrivilegeAssignType(), PrivilegeAssignType.EFFECTIVE));
            }
          }
        }
      }
      
      groupUpdateRequestContainer.setGroupToEdit(group);
      groupUpdateRequestContainer.setPrivilegeSubjectContainers(privilegeSubjectContainers);
      
      List<GuiMember> guiMembers = new ArrayList<GuiMember>();
      
      for (PrivilegeSubjectContainer privilegeSubjectContainer : privilegeSubjectContainers) {
        //guiMember.setGuiSubject(new GuiSubject(privilegeSubjectContainer.getSubject()));
        //note, probably better not to do this in loop, oh well
        Member member = MemberFinder.findBySubject(grouperSession, privilegeSubjectContainer.getSubject(), true);
        GuiMember guiMember = new GuiMember(member);
        guiMembers.add(guiMember);
      }
      
      groupUpdateRequestContainer.setPrivilegeSubjectContainerGuiMembers(guiMembers);
      
      //set the privilege panel
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#groupPrivilegesPanel", 
        "/WEB-INF/grouperUi/templates/groupUpdate/groupPrivilegesPanel.jsp"));
      
      guiResponseJs.addAction(GuiScreenAction.newScript("guiScrollTo('#groupPrivilegesPanel');"));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession); 
    }
  }

  /**
   * privileges button was pressed on the group edit panel
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void groupEditPanelPrivilegesClearPaging(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
    
    //we need to set page number to 1, in general a good thing to do, and specifically, if reducing the numbers
    //and on a later page, then no members will be shown...
    GuiPaging guiPaging = GuiPaging.retrievePaging("simpleGroupUpdatePrivileges", false);
    if (guiPaging != null) {
      guiPaging.setPageNumber(1);
    }
    groupEditPanelPrivileges(httpServletRequest, httpServletResponse);
  }

  /**
   * cancel privilege button was pressed on the group edit panel
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void privilegeCancel(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
  
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
    //set the privilege panel
    guiResponseJs.addAction(GuiScreenAction.newInnerHtml("#groupPrivilegesPanel", null));
    
  }

  /**
   * privilege image button was pressed on the privilege edit panel
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void privilegePanelImageClick(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
  
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
  
    GroupUpdateRequestContainer groupUpdateRequestContainer = GroupUpdateRequestContainer.retrieveFromRequestOrCreate();
  
    GrouperSession grouperSession = null;
  
    Group group = null;
    
    try {
      grouperSession = GrouperSession.start(loggedInSubject);
      
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
      String memberId = httpServletRequest.getParameter("memberId");
  
      if (StringUtils.isBlank(memberId)) {
        throw new RuntimeException("Why is memberId blank????");
      }
  
      String privilegeName = httpServletRequest.getParameter("privilegeName");
  
      if (StringUtils.isBlank(privilegeName)) {
        throw new RuntimeException("Why is privilegeName blank????");
      }
  
      String allowString = httpServletRequest.getParameter("allow");
  
      if (StringUtils.isBlank(allowString)) {
        throw new RuntimeException("Why is allow blank????");
      }
      boolean allow = GrouperUtil.booleanValue(allowString);
      
      String uuid = httpServletRequest.getParameter("groupToEditId");
      
      if (StringUtils.isBlank(uuid)) {
        throw new RuntimeException("Why is uuid blank????");
      }
  
      //if editing, then this must be there, or it has been tampered with
      try {
        group = GroupFinder.findByUuid(grouperSession, uuid, true);
      } catch (Exception e) {
        LOG.info("Error searching for group: " + uuid, e);
        guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message("simpleGroupUpdate.errorCantEditGroup", false)));
        return;
        
      }
    
      if (group == null || !group.hasAdmin(loggedInSubject)) {
        LOG.error("Subject " + GrouperUtil.subjectToString(loggedInSubject) + " cannot admin group: " + group.getName());
        guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message("simpleGroupUpdate.errorCantEditGroup", false)));
        return;
      }
      
      groupUpdateRequestContainer.setGroupToEdit(group);
      
      StringBuilder alert = new StringBuilder();
      
      Member member = MemberFinder.findByUuid(grouperSession, memberId, true);
      Subject subject = member.getSubject();
      Privilege privilege = Privilege.getInstance(privilegeName);
      String privilegeGuiLabel = GrouperUiUtils.message("priv." + privilege.getName(), false);
  
      GuiMember guiMember = new GuiMember(member);
      String subjectScreenLabel = guiMember.getGuiSubject().getScreenLabel();
  
      if (allow) {
        boolean result = group.grantPriv(
            subject, privilege, false);
        
        if (result) {
          
          alert.append(GrouperUiUtils.message("simpleGroupUpdate.privilegeGrant", false, true, new Object[]{privilegeGuiLabel, subjectScreenLabel}));
          
        } else {
          
          alert.append(GrouperUiUtils.message("simpleGroupUpdate.privilegeGrantWarn", false, true, new Object[]{privilegeGuiLabel, subjectScreenLabel}));
        }              
      } else {
        boolean result = group.revokePriv(
            subject, privilege, false);
        
        if (result) {
          
          alert.append(GrouperUiUtils.message("simpleGroupUpdate.privilegeRevoke", false, true, new Object[]{privilegeGuiLabel, subjectScreenLabel}));
          
        } else {
          
          alert.append(GrouperUiUtils.message("simpleGroupUpdate.privilegeRevokeWarn", false, true, new Object[]{privilegeGuiLabel, subjectScreenLabel}));
        }              
      }
      
      guiResponseJs.addAction(GuiScreenAction.newAlert(alert.toString()));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession); 
    }
    
    //show the edit panel since it might change due to everyentity privilege changing
    if (!new SimpleGroupUpdateFilter().editGroupHelper(httpServletRequest, httpServletResponse, group, false)) {
      return;
    }
    
    groupEditPanelPrivileges(httpServletRequest, httpServletResponse);
    
  }

  /**
   * submit privileges button was pressed on the privilege edit panel
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void privilegePanelSubmit(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
  
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
  
    GroupUpdateRequestContainer groupUpdateRequestContainer = GroupUpdateRequestContainer.retrieveFromRequestOrCreate();
  
    GrouperSession grouperSession = null;
  
    Group group = null;
    
    try {
      grouperSession = GrouperSession.start(loggedInSubject);
      
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
      String uuid = httpServletRequest.getParameter("groupToEditId");
      
      if (StringUtils.isBlank(uuid)) {
        throw new RuntimeException("Why is uuid blank????");
      }
  
      //if editing, then this must be there, or it has been tampered with
      try {
        group = GroupFinder.findByUuid(grouperSession, uuid, true);
      } catch (Exception e) {
        LOG.info("Error searching for group: " + uuid, e);
        guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message("simpleGroupUpdate.errorCantEditGroup", false)));
        return;
        
      }
    
      if (group == null || !group.hasAdmin(loggedInSubject)) {
        LOG.error("Subject " + GrouperUtil.subjectToString(loggedInSubject) + " cannot admin group: " + group.getName());
        guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message("simpleGroupUpdate.errorCantEditGroup", false)));
        return;
      }
    
      groupUpdateRequestContainer.setGroupToEdit(group);
      
      //lets see what to do...
      //<input  name="previousState__${guiMember.member.uuid}__${privilegeName}"
      //  type="hidden" value="${privilegeSubjectContainer.privilegeContainers[privilegeName].privilegeAssignType.immediate ? 'true' : 'false'}" />
      //<%-- note, too much space between elements, move it over 3px --%>
      //<input  style="margin-right: -3px" name="privilegeCheckbox__${guiMember.member.uuid}__${privilegeName}"
      //  type="checkbox" ${privilegeSubjectContainer.privilegeContainers[privilegeName].privilegeAssignType.immediate ? 'checked="checked"' : '' } 
      ///><c:choose>
      
      StringBuilder alert = new StringBuilder();
      
      Pattern pattern = Pattern.compile("^previousState__(.*)__(.*)$");
      Enumeration<?> enumeration = httpServletRequest.getParameterNames();
      while (enumeration != null && enumeration.hasMoreElements()) {
        String paramName = (String)enumeration.nextElement();
        Matcher matcher = pattern.matcher(paramName);
        if (matcher.matches()) {
          
          //lets get the previous state
          boolean previousChecked = GrouperUtil.booleanValue(httpServletRequest.getParameter(paramName));
          
          //get current state
          String memberId = matcher.group(1);
          String privilegeName = matcher.group(2);
          String currentStateString = httpServletRequest.getParameter("privilegeCheckbox__" + memberId + "__" + privilegeName);
          boolean currentChecked = GrouperUtil.booleanValue(currentStateString, false);
          
          //if they dont match, do something about it
          if (previousChecked != currentChecked) {
            
            Member member = MemberFinder.findByUuid(grouperSession, memberId, true);
            Subject subject = member.getSubject();
            Privilege privilege = Privilege.getInstance(privilegeName);
            String privilegeGuiLabel = GrouperUiUtils.message("priv." + privilege.getName(), false);
  
            if (alert.length() > 0) {
              alert.append("<br />");
            }
  
            GuiMember guiMember = new GuiMember(member);
            String subjectScreenLabel = guiMember.getGuiSubject().getScreenLabel();
  
            if (currentChecked) {
              boolean result = group.grantPriv(
                  subject, privilege, false);
              
              if (result) {
                
                alert.append(GrouperUiUtils.message("simpleGroupUpdate.privilegeGrant", false, true, new Object[]{privilegeGuiLabel, subjectScreenLabel}));
                
              } else {
                
                alert.append(GrouperUiUtils.message("simpleGroupUpdate.privilegeGrantWarn", false, true, new Object[]{privilegeGuiLabel, subjectScreenLabel}));
              }              
            } else {
              boolean result = group.revokePriv(
                  subject, privilege, false);
              
              if (result) {
                
                alert.append(GrouperUiUtils.message("simpleGroupUpdate.privilegeRevoke", false, true, new Object[]{privilegeGuiLabel, subjectScreenLabel}));
                
              } else {
                
                alert.append(GrouperUiUtils.message("simpleGroupUpdate.privilegeRevokeWarn", false, true, new Object[]{privilegeGuiLabel, subjectScreenLabel}));
              }              
            }
          }
        }
      }
      
      if (alert.length() > 0) {
        guiResponseJs.addAction(GuiScreenAction.newAlert(alert.toString()));
      } else {
        guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message("simpleGroupUpdate.noPrivilegeChangesDetected", false)));
      }
      
    } finally {
      GrouperSession.stopQuietly(grouperSession); 
    }
    
    //show the edit panel since it might change due to everyentity privilege changing
    if (!new SimpleGroupUpdateFilter().editGroupHelper(httpServletRequest, httpServletResponse, group, false)) {
      return;
    }
    
    groupEditPanelPrivileges(httpServletRequest, httpServletResponse);
    
  }

  /**
   * add a role that is implied by the current role
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void addRoleImpliedBy(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
  
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
  
    GroupUpdateRequestContainer groupUpdateRequestContainer = GroupUpdateRequestContainer.retrieveFromRequestOrCreate();
  
    GrouperSession grouperSession = null;
  
    try {
      grouperSession = GrouperSession.start(loggedInSubject);
      
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
      Group group = null;
      
      String uuid = httpServletRequest.getParameter("groupToEditId");
      
      if (StringUtils.isBlank(uuid)) {
        throw new RuntimeException("Why is uuid blank????");
      }
  
      String roleIdForHierarchy = httpServletRequest.getParameter("roleIdForHierarchy");
      
      if (StringUtils.isBlank(roleIdForHierarchy)) {
        guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message("simpleGroupUpdate.errorCantEditGroup", false)));
        return;
      }
      
      //if editing, then this must be there, or it has been tampered with
      try {
        group = GroupFinder.findByUuid(grouperSession, uuid, true);
      } catch (Exception e) {
        LOG.info("Error searching for group: " + uuid, e);
        guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message("simpleGroupUpdate.errorCantEditGroup", false)));
        return;
        
      }
    
      if (group == null || !group.hasAdmin(loggedInSubject)) {
        LOG.error("Subject " + GrouperUtil.subjectToString(loggedInSubject) + " cannot admin group: " + group.getName());
        guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message("simpleGroupUpdate.errorCantEditGroup", false)));
        return;
      }
    
      groupUpdateRequestContainer.setGroupToEdit(group);

      Group roleForHierarchy = null;

      //if editing, then this must be there, or it has been tampered with
      try {
        roleForHierarchy = GroupFinder.findByUuid(grouperSession, roleIdForHierarchy, true);
      } catch (Exception e) {
        LOG.info("Error searching for group: " + roleIdForHierarchy, e);
        guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message("simpleGroupUpdate.errorCantEditGroup", false)));
        return;
        
      }
    
      if (roleForHierarchy != null && !roleForHierarchy.hasAdmin(loggedInSubject)) {
        LOG.error("Subject " + GrouperUtil.subjectToString(loggedInSubject) + " cannot admin role: " + roleForHierarchy.getName());
        guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message("simpleGroupUpdate.errorCantEditGroup", false)));
        return;
      }
      
      if (StringUtils.equals(roleIdForHierarchy, uuid)) {
        
        guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message("simpleGroupUpdate.impliesSameRole", false)));
        
      } else {      
        boolean success = group.getRoleInheritanceDelegate().addRoleToInheritFromThis(roleForHierarchy);
    
        if (success) {
          
          guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message("simpleGroupUpdate.successAddImpliedByRole", false)));
  
        } else {
          
          guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message("simpleGroupUpdate.failureAddImpliedByRole", false)));
  
        }
  
        setupRoleHierarchiesPanel(groupUpdateRequestContainer, guiResponseJs, group);
      }
      
    } finally {
      GrouperSession.stopQuietly(grouperSession); 
    }
  }

  /**
   * add a role that implies the current role
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void addRoleImplies(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
  
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
  
    GroupUpdateRequestContainer groupUpdateRequestContainer = GroupUpdateRequestContainer.retrieveFromRequestOrCreate();
  
    GrouperSession grouperSession = null;
  
    try {
      grouperSession = GrouperSession.start(loggedInSubject);
      
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
      Group group = null;
      
      String uuid = httpServletRequest.getParameter("groupToEditId");
      
      if (StringUtils.isBlank(uuid)) {
        throw new RuntimeException("Why is uuid blank????");
      }
  
      String roleIdForHierarchy = httpServletRequest.getParameter("roleIdForHierarchy");
      
      if (StringUtils.isBlank(roleIdForHierarchy)) {
        guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message("simpleGroupUpdate.errorCantEditGroup", false)));
        return;
      }
      
      //if editing, then this must be there, or it has been tampered with
      try {
        group = GroupFinder.findByUuid(grouperSession, uuid, true);
      } catch (Exception e) {
        LOG.info("Error searching for group: " + uuid, e);
        guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message("simpleGroupUpdate.errorCantEditGroup", false)));
        return;
        
      }
    
      if (group == null || !group.hasAdmin(loggedInSubject)) {
        LOG.error("Subject " + GrouperUtil.subjectToString(loggedInSubject) + " cannot admin group: " + group.getName());
        guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message("simpleGroupUpdate.errorCantEditGroup", false)));
        return;
      }
    
      groupUpdateRequestContainer.setGroupToEdit(group);

      Group roleForHierarchy = null;

      //if editing, then this must be there, or it has been tampered with
      try {
        roleForHierarchy = GroupFinder.findByUuid(grouperSession, roleIdForHierarchy, true);
      } catch (Exception e) {
        LOG.info("Error searching for group: " + roleIdForHierarchy, e);
        guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message("simpleGroupUpdate.errorCantEditGroup", false)));
        return;
        
      }
    
      if (roleForHierarchy != null && !roleForHierarchy.hasAdmin(loggedInSubject)) {
        LOG.error("Subject " + GrouperUtil.subjectToString(loggedInSubject) + " cannot admin role: " + roleForHierarchy.getName());
        guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message("simpleGroupUpdate.errorCantEditGroup", false)));
        return;
      }
      
      if (StringUtils.equals(roleIdForHierarchy, uuid)) {
        
        guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message("simpleGroupUpdate.impliesSameRole", false)));
        
      } else {
      
        boolean success = roleForHierarchy.getRoleInheritanceDelegate().addRoleToInheritFromThis(group);
    
        if (success) {
          
          guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message("simpleGroupUpdate.successAddImpliesRole", false)));
  
        } else {
          
          guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message("simpleGroupUpdate.failureAddImpliesRole", false)));
  
        }
  
        setupRoleHierarchiesPanel(groupUpdateRequestContainer, guiResponseJs, group);
      }
    } finally {
      GrouperSession.stopQuietly(grouperSession); 
    }
  }

  /**
   * delete a role that is implied by the current role
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void deleteRoleImpliedBy(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
  
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
  
    GroupUpdateRequestContainer groupUpdateRequestContainer = GroupUpdateRequestContainer.retrieveFromRequestOrCreate();
  
    GrouperSession grouperSession = null;
  
    try {
      grouperSession = GrouperSession.start(loggedInSubject);
  
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
      Group group = null;
  
      String uuid = httpServletRequest.getParameter("groupToEditId");
  
      if (StringUtils.isBlank(uuid)) {
        throw new RuntimeException("Why is uuid blank????");
      }
  
      String roleIdSubmitted = httpServletRequest.getParameter("roleIdSubmitted");
      
      if (StringUtils.isBlank(roleIdSubmitted)) {
        throw new RuntimeException("Why is roleIdSubmitted blank????");
      }
      
      //if editing, then this must be there, or it has been tampered with
      try {
        group = GroupFinder.findByUuid(grouperSession, uuid, true);
      } catch (Exception e) {
        LOG.info("Error searching for group: " + uuid, e);
        guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message("simpleGroupUpdate.errorCantEditGroup", false)));
        return;
        
      }
    
      if (group == null || !group.hasAdmin(loggedInSubject)) {
        LOG.error("Subject " + GrouperUtil.subjectToString(loggedInSubject) + " cannot admin group: " + group.getName());
        guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message("simpleGroupUpdate.errorCantEditGroup", false)));
        return;
      }
    
      groupUpdateRequestContainer.setGroupToEdit(group);

      Group roleSubmitted = null;

      //if editing, then this must be there, or it has been tampered with
      try {
        roleSubmitted = GroupFinder.findByUuid(grouperSession, roleIdSubmitted, true);
      } catch (Exception e) {
        LOG.info("Error searching for group: " + roleIdSubmitted, e);
        guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message("simpleGroupUpdate.errorCantEditGroup", false)));
        return;
        
      }
    
      if (roleSubmitted != null && !roleSubmitted.hasAdmin(loggedInSubject)) {
        LOG.error("Subject " + GrouperUtil.subjectToString(loggedInSubject) + " cannot admin role: " + roleSubmitted.getName());
        guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message("simpleGroupUpdate.errorCantEditGroup", false)));
        return;
      }
      
      boolean success = group.getRoleInheritanceDelegate().removeRoleFromInheritFromThis(roleSubmitted);
  
      if (success) {
        
        guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message("simpleGroupUpdate.successRemoveImpliedByRole", false)));

      } else {
        
        guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message("simpleGroupUpdate.failureRemoveImpliedByRole", false)));
      }
  
      setupRoleHierarchiesPanel(groupUpdateRequestContainer, guiResponseJs, group);
      
    } finally {
      GrouperSession.stopQuietly(grouperSession); 
    }
  }

  /**
   * delete a role that implies the current role
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void deleteRoleImplies(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
  
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
  
    GroupUpdateRequestContainer groupUpdateRequestContainer = GroupUpdateRequestContainer.retrieveFromRequestOrCreate();
  
    GrouperSession grouperSession = null;

    try {
      grouperSession = GrouperSession.start(loggedInSubject);
  
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
      Group group = null;
  
      String uuid = httpServletRequest.getParameter("groupToEditId");
  
      if (StringUtils.isBlank(uuid)) {
        throw new RuntimeException("Why is uuid blank????");
      }
  
      String roleIdSubmitted = httpServletRequest.getParameter("roleIdSubmitted");
      
      if (StringUtils.isBlank(roleIdSubmitted)) {
        throw new RuntimeException("Why is roleIdSubmitted blank????");
      }
      
      //if editing, then this must be there, or it has been tampered with
      try {
        group = GroupFinder.findByUuid(grouperSession, uuid, true);
      } catch (Exception e) {
        LOG.info("Error searching for group: " + uuid, e);
        guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message("simpleGroupUpdate.errorCantEditGroup", false)));
        return;
        
      }
    
      if (group == null || !group.hasAdmin(loggedInSubject)) {
        LOG.error("Subject " + GrouperUtil.subjectToString(loggedInSubject) + " cannot admin group: " + group.getName());
        guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message("simpleGroupUpdate.errorCantEditGroup", false)));
        return;
      }
    
      groupUpdateRequestContainer.setGroupToEdit(group);

      Group roleSubmitted = null;

      //if editing, then this must be there, or it has been tampered with
      try {
        roleSubmitted = GroupFinder.findByUuid(grouperSession, roleIdSubmitted, true);
      } catch (Exception e) {
        LOG.info("Error searching for group: " + roleIdSubmitted, e);
        guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message("simpleGroupUpdate.errorCantEditGroup", false)));
        return;
        
      }
    
      if (roleSubmitted != null && !roleSubmitted.hasAdmin(loggedInSubject)) {
        LOG.error("Subject " + GrouperUtil.subjectToString(loggedInSubject) + " cannot admin role: " + roleSubmitted.getName());
        guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message("simpleGroupUpdate.errorCantEditGroup", false)));
        return;
      }
      
      boolean success = roleSubmitted.getRoleInheritanceDelegate().removeRoleFromInheritFromThis(group);
  
      if (success) {
        
        guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message("simpleGroupUpdate.successRemoveImpliesRole", false)));

      } else {

        guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message("simpleGroupUpdate.failureRemoveImpliesRole", false)));
  
      }
  
      setupRoleHierarchiesPanel(groupUpdateRequestContainer, guiResponseJs, group);
      
    } finally {
      GrouperSession.stopQuietly(grouperSession); 
    }
  }

  /**
   * edit a role hierarchy
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void roleHierarchies(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
  
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
  
    GroupUpdateRequestContainer groupUpdateRequestContainer = GroupUpdateRequestContainer.retrieveFromRequestOrCreate();
  
    GrouperSession grouperSession = null;
  
    try {
      grouperSession = GrouperSession.start(loggedInSubject);
      
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
      Group group = null;
      
      String uuid = httpServletRequest.getParameter("groupToEditId");
      
      if (StringUtils.isBlank(uuid)) {
        throw new RuntimeException("Why is uuid blank????");
      }
  
      //if editing, then this must be there, or it has been tampered with
      try {
        group = GroupFinder.findByUuid(grouperSession, uuid, true);
      } catch (Exception e) {
        LOG.info("Error searching for group: " + uuid, e);
        guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message("simpleGroupUpdate.errorCantEditGroup", false)));
        return;
        
      }
    
      if (group == null || !group.hasAdmin(loggedInSubject)) {
        LOG.error("Subject " + GrouperUtil.subjectToString(loggedInSubject) + " cannot admin group: " + group.getName());
        guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message("simpleGroupUpdate.errorCantEditGroup", false)));
        return;
      }
    
      groupUpdateRequestContainer.setGroupToEdit(group);

      setupRoleHierarchiesPanel(groupUpdateRequestContainer, guiResponseJs, group);
      
    } finally {
      GrouperSession.stopQuietly(grouperSession); 
    }
  }

  /**
   * edit a role hierarchy
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void roleHierarchyGraph(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GroupUpdateRequestContainer groupUpdateRequestContainer = GroupUpdateRequestContainer.retrieveFromRequestOrCreate();
  
    GrouperSession grouperSession = null;
  
    try {
      grouperSession = GrouperSession.start(loggedInSubject);
      
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
      Group group = null;
      
      String uuid = httpServletRequest.getParameter("groupToEditId");
      
      if (StringUtils.isBlank(uuid)) {
        throw new RuntimeException("Why is uuid blank????");
      }
  
      //if editing, then this must be there, or it has been tampered with
      try {
        group = GroupFinder.findByUuid(grouperSession, uuid, true);
      } catch (Exception e) {
        LOG.info("Error searching for group: " + uuid, e);
        guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message("simpleGroupUpdate.errorCantEditGroup", false)));
        return;
        
      }
    
      if (group == null || !group.hasAdmin(loggedInSubject)) {
        LOG.error("Subject " + GrouperUtil.subjectToString(loggedInSubject) + " cannot admin group: " + group.getName());
        guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message("simpleGroupUpdate.errorCantEditGroup", false)));
        return;
      }
    
      groupUpdateRequestContainer.setGroupToEdit(group);
      
      groupUpdateRequestContainer.setRoleGraphNodesFrom(new ArrayList<String>());
      groupUpdateRequestContainer.setRoleGraphNodesTo(new ArrayList<String>());
      groupUpdateRequestContainer.setRoleGraphStartingPoints(new ArrayList<String>());
      
      Set<Role> allRolesOnGraph = new HashSet<Role>();
      Set<Role> rolesThatImplyThis = group.getRoleInheritanceDelegate().getRolesInheritPermissionsToThis();
      Set<Role> rolesImpliedByThis = group.getRoleInheritanceDelegate().getRolesInheritPermissionsFromThis();
      allRolesOnGraph.addAll(rolesThatImplyThis);
      allRolesOnGraph.addAll(rolesImpliedByThis);
      allRolesOnGraph.add(group);

      {
        //we are displaying by display extension, make sure there arent two nodes with the same extension
        Set<String> uniqueDisplayExtensions = new HashSet<String>();
        for (Role current : allRolesOnGraph) {
          if (uniqueDisplayExtensions.contains(current.getDisplayExtension())) {
            throw new RuntimeException("Cannot display graph is multiple roles have the same display extension!");
          }
          uniqueDisplayExtensions.add(current.getDisplayExtension());
        }
      }
      
      //find out which ones are starting points
      Set<String> startingPoints = new HashSet<String>();
      for (Role current : GrouperUtil.nonNull(rolesThatImplyThis)) {
        startingPoints.add(current.getDisplayExtension());
      }
      
      //if none then add current
      if (startingPoints.size() == 0) {
        startingPoints.add(group.getDisplayExtension());
      }
      
      //find all relevant relationships
      for (Role current : GrouperUtil.nonNull(allRolesOnGraph)) {
        
        Set<Role> rolesImpliedByCurrentImmediate = current.getRoleInheritanceDelegate().getRolesInheritPermissionsFromThisImmediate();

        for (Role impliedBy : GrouperUtil.nonNull(rolesImpliedByCurrentImmediate)) {
          
          //make sure it is relevant
          if (!allRolesOnGraph.contains(impliedBy)) {
            continue;
          }

          //we know which ones arent starting points
          startingPoints.remove(impliedBy.getDisplayExtension());
          
          groupUpdateRequestContainer.getRoleGraphNodesFrom().add(current.getDisplayExtension());
          
          groupUpdateRequestContainer.getRoleGraphNodesTo().add(impliedBy.getDisplayExtension());
        }
        
      }

      if (startingPoints.size() == 0) {
        startingPoints.add(group.getDisplayExtension());
      }
      
      groupUpdateRequestContainer.getRoleGraphStartingPoints().addAll(startingPoints);
      
      //set the hierarchies panel
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#roleHierarchyPanel", 
        "/WEB-INF/grouperUi/templates/groupUpdate/roleHierarchyGraph.jsp"));
      
      guiResponseJs.addAction(GuiScreenAction.newScript("guiScrollTo('#roleHierarchyPanel');"));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession); 
    }

  }
  
  /**
   * setup the role hierarchies panel
   * @param groupUpdateRequestContainer
   * @param guiResponseJs
   * @param group
   */
  @SuppressWarnings("unchecked")
  private void setupRoleHierarchiesPanel(
      GroupUpdateRequestContainer groupUpdateRequestContainer,
      GuiResponseJs guiResponseJs, Group group) {
    
    groupUpdateRequestContainer.setGroupToEdit(group);
    
    {
      List<Role> rolesThatImplyThis = new ArrayList<Role>(group.getRoleInheritanceDelegate().getRolesInheritPermissionsToThis());
      Collections.sort(rolesThatImplyThis);
      groupUpdateRequestContainer.setRolesThatImply(rolesThatImplyThis);
    }
    
    {
      List<Role> rolesThatImplyThisImmediate = new ArrayList<Role>(group.getRoleInheritanceDelegate().getRolesInheritPermissionsToThisImmediate());
      Collections.sort(rolesThatImplyThisImmediate);
      groupUpdateRequestContainer.setRolesThatImplyImmediate(rolesThatImplyThisImmediate);
    }
    
    {
      List<Role> rolesImpliedByThis = new ArrayList<Role>(group.getRoleInheritanceDelegate().getRolesInheritPermissionsFromThis());
      Collections.sort(rolesImpliedByThis);
      groupUpdateRequestContainer.setRolesImpliedBy(rolesImpliedByThis);
    }
    
    {
      List<Role> rolesImpliedByThisImmediate = new ArrayList<Role>(group.getRoleInheritanceDelegate().getRolesInheritPermissionsFromThisImmediate());
      Collections.sort(rolesImpliedByThisImmediate);
      groupUpdateRequestContainer.setRolesImpliedByImmediate(rolesImpliedByThisImmediate);
    }
    
    //set the hierarchies panel
    guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#roleHierarchyPanel", 
      "/WEB-INF/grouperUi/templates/groupUpdate/roleHierarchies.jsp"));
    
    guiResponseJs.addAction(GuiScreenAction.newScript("guiScrollTo('#roleHierarchyPanel');"));
  }

  /** logger */
  private static final Log LOG = LogFactory.getLog(SimpleGroupUpdate.class);
  
  
}
