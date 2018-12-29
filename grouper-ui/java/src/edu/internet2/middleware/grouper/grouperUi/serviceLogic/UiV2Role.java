/**
 * 
 */
package edu.internet2.middleware.grouper.grouperUi.serviceLogic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.group.TypeOfGroup;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiResponseJs;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction.GuiMessageType;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GrouperRequestContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.RoleInheritanceContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.TextContainer;
import edu.internet2.middleware.grouper.permissions.role.Role;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.ui.util.GrouperUiUtils;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

public class UiV2Role {
  
  /**
   * show role edit inheritance screen
   * @param request
   * @param response
   */
  public void roleEditInheritance(HttpServletRequest request, HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    Group group = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      group = UiV2Group.retrieveGroupHelper(request, AccessPrivilege.ADMIN).getGroup();
      
      if (group == null) {
        return;
      }
      
      if (group.getTypeOfGroup() != TypeOfGroup.role) {
        throw new RuntimeException("Group is not of type role.");
      }
      
      editRoleInheritanceHelper(group);
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }
  
  
  @SuppressWarnings("unchecked")
  private void editRoleInheritanceHelper(Group group) {
    
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    GrouperRequestContainer grouperRequestContainer = GrouperRequestContainer.retrieveFromRequestOrCreate();
    RoleInheritanceContainer roleInheritanceContainer = grouperRequestContainer.getRoleInheritanceContainer();
    
    {
      List<Role> rolesThatImplyThis = new ArrayList<Role>(group.getRoleInheritanceDelegate().getRolesInheritPermissionsToThis());
      Collections.sort(rolesThatImplyThis);        
      roleInheritanceContainer.setRolesThatImplyThis(rolesThatImplyThis);
    }
    
    {
      List<Role> rolesThatImplyThisImmediate = new ArrayList<Role>(group.getRoleInheritanceDelegate().getRolesInheritPermissionsToThisImmediate());
      Collections.sort(rolesThatImplyThisImmediate);
      roleInheritanceContainer.setRolesThatImplyThisImmediate(rolesThatImplyThisImmediate);
    }
    
    {
      List<Role> rolesImpliedByThis = new ArrayList<Role>(group.getRoleInheritanceDelegate().getRolesInheritPermissionsFromThis());
      Collections.sort(rolesImpliedByThis);
      roleInheritanceContainer.setRolesImpliedByThis(rolesImpliedByThis);
    }
    
    {
      List<Role> rolesImpliedByThisImmediate = new ArrayList<Role>(group.getRoleInheritanceDelegate().getRolesInheritPermissionsFromThisImmediate());
      Collections.sort(rolesImpliedByThisImmediate);
      roleInheritanceContainer.setRolesImpliedByThisImmediate(rolesImpliedByThisImmediate);
    }
    
    guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
        "/WEB-INF/grouperUi2/role/roleInheritance.jsp"));
    
  }
  
  /**
   * add a role that implies the current role
   * @param request
   * @param response
   */
  public void addRoleImplies(HttpServletRequest request, HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    Group group = null;
    
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      group = UiV2Group.retrieveGroupHelper(request, AccessPrivilege.ADMIN).getGroup();
      
      if (group == null) {
        return;
      }
      
      if (group.getTypeOfGroup() != TypeOfGroup.role) {
        throw new RuntimeException("Group is not of type role.");
      }
      
      String roleThatImplies = request.getParameter("roleInhertianceRoleComboName");
      
      if (StringUtils.isNotBlank(roleThatImplies)) {
        Group groupThatImplies = GroupFinder.findByUuid(grouperSession, roleThatImplies, true);
        
        if (groupThatImplies.getId().equals(group.getId())) {
          guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
              TextContainer.retrieveFromRequest().getText().get("roleInheritanceEditSelfError")));
          return;
        }
        
        if (groupThatImplies.getTypeOfGroup() == TypeOfGroup.role) {
          boolean success = groupThatImplies.getRoleInheritanceDelegate().addRoleToInheritFromThis(group);
          if (!success) {
            guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.info, 
                TextContainer.retrieveFromRequest().getText().get("roleInheritanceAddRelationshipAlreadyThereInfo")));
            return;
          }
        } else {
          throw new RuntimeException("Group is not of type role.");
        }
      }
      
      editRoleInheritanceHelper(group);
  
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
          TextContainer.retrieveFromRequest().getText().get("roleInheritanceEditSuccess")));
      
    } catch (Exception e) {
      if (GrouperUiUtils.vetoHandle(guiResponseJs, e)) {
        return;
      }
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
          TextContainer.retrieveFromRequest().getText().get("roleInheritanceEditError") 
          + ": " + GrouperUtil.xmlEscape(e.getMessage(), true)));
      return;
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }
  
  /**
   * add a role that is implied by the current role
   * @param request
   * @param response
   */
  public void addRoleImpliedBy(HttpServletRequest request, HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    Group group = null;
    
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      group = UiV2Group.retrieveGroupHelper(request, AccessPrivilege.ADMIN).getGroup();
      
      if (group == null) {
        return;
      }
      
      if (group.getTypeOfGroup() != TypeOfGroup.role) {
        throw new RuntimeException("Group is not of type role.");
      }
      
      String roleImpliedById = request.getParameter("roleInhertianceRoleComboName");
      
      if (StringUtils.isNotBlank(roleImpliedById)) {
        
        Group roleImpliedBy = GroupFinder.findByUuid(grouperSession, roleImpliedById, true);
        
        if (roleImpliedBy.getId().equals(group.getId())) {
          guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
              TextContainer.retrieveFromRequest().getText().get("roleInheritanceEditSelfError")));
          return;
        }
        
        if (roleImpliedBy.getTypeOfGroup() == TypeOfGroup.role) {
          boolean success = group.getRoleInheritanceDelegate().addRoleToInheritFromThis(roleImpliedBy);
          if (!success) {
            guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.info, 
                TextContainer.retrieveFromRequest().getText().get("roleInheritanceAddRelationshipAlreadyThereInfo")));
            return;
          }
        } else {
          throw new RuntimeException("Group is not of type role.");
        }
      }
      
      editRoleInheritanceHelper(group);
  
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
          TextContainer.retrieveFromRequest().getText().get("roleInheritanceEditSuccess")));
      
    } catch (Exception e) {
      if (GrouperUiUtils.vetoHandle(guiResponseJs, e)) {
        return;
      }
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
          TextContainer.retrieveFromRequest().getText().get("roleInheritanceEditError") 
          + ": " + GrouperUtil.xmlEscape(e.getMessage(), true)));
      return;
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }
  
  /**
   * delete a role that implies the current role
   * @param request
   * @param response
   */
  public void deleteRoleImplies(HttpServletRequest request, HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    Group group = null;
    
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      group = UiV2Group.retrieveGroupHelper(request, AccessPrivilege.ADMIN).getGroup();
      
      if (group == null) {
        return;
      }
      
      if (group.getTypeOfGroup() != TypeOfGroup.role) {
        throw new RuntimeException("Group is not of type role.");
      }
      
      String roleId = request.getParameter("roleId");
      
      if (StringUtils.isNotBlank(roleId)) {
        
        Group roleSubmitted = GroupFinder.findByUuid(grouperSession, roleId, true);
        
        if (roleSubmitted.getTypeOfGroup() == TypeOfGroup.role) {
          boolean success = roleSubmitted.getRoleInheritanceDelegate().removeRoleFromInheritFromThis(group);
          if (!success) {
            guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.info, 
                TextContainer.retrieveFromRequest().getText().get("roleInheritanceDeleteRelationshipAlreadyThereInfo")));
            return;
          }
        } else {
          throw new RuntimeException("Group is not of type role.");
        }
      }
      
      editRoleInheritanceHelper(group);
  
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
          TextContainer.retrieveFromRequest().getText().get("roleInheritanceEditSuccess")));
      
    } catch (Exception e) {
      if (GrouperUiUtils.vetoHandle(guiResponseJs, e)) {
        return;
      }
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
          TextContainer.retrieveFromRequest().getText().get("roleInheritanceEditError") 
          + ": " + GrouperUtil.xmlEscape(e.getMessage(), true)));
      return;
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }
  
  /**
   * delete a role that is implied by the current role
   * @param request
   * @param response
   */
  public void deleteRoleImpliedBy(HttpServletRequest request, HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    Group group = null;
    
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      group = UiV2Group.retrieveGroupHelper(request, AccessPrivilege.ADMIN).getGroup();
      
      if (group == null) {
        return;
      }
      
      if (group.getTypeOfGroup() != TypeOfGroup.role) {
        throw new RuntimeException("Group is not of type role.");
      }
      
      String roleId = request.getParameter("roleId");
      
      if (StringUtils.isNotBlank(roleId)) {
        
        Group roleSubmitted = GroupFinder.findByUuid(grouperSession, roleId, true);
        
        if (roleSubmitted.getTypeOfGroup() == TypeOfGroup.role) {
          boolean success = group.getRoleInheritanceDelegate().removeRoleFromInheritFromThis(roleSubmitted);
          if (!success) {
            guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.info, 
                TextContainer.retrieveFromRequest().getText().get("roleInheritanceDeleteRelationshipAlreadyThereInfo")));
            return;
          }
        } else {
          throw new RuntimeException("Group is not of type role.");
        }
      }
    
      editRoleInheritanceHelper(group);
  
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
          TextContainer.retrieveFromRequest().getText().get("roleInheritanceEditSuccess")));
      
    } catch (Exception e) {
      if (GrouperUiUtils.vetoHandle(guiResponseJs, e)) {
        return;
      }
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
          TextContainer.retrieveFromRequest().getText().get("roleInheritanceEditError") 
          + ": " + GrouperUtil.xmlEscape(e.getMessage(), true)));
      return;
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }
  
}
