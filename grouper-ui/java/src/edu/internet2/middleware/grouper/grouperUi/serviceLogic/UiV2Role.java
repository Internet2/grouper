/**
 * 
 */
package edu.internet2.middleware.grouper.grouperUi.serviceLogic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
      
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      
      if (group.getTypeOfGroup() != TypeOfGroup.role) {
        throw new RuntimeException("Group is not of type role.");
      }
      
      GrouperRequestContainer grouperRequestContainer = GrouperRequestContainer.retrieveFromRequestOrCreate();
      RoleInheritanceContainer roleInheritanceContainer = grouperRequestContainer.getRoleInheritanceContainer();
      
      // get all the roles
      Set<TypeOfGroup> roleTypes = new HashSet<TypeOfGroup>();
      roleTypes.add(TypeOfGroup.role);
      Collection<Group> roles = new GroupFinder().assignTypeOfGroups(roleTypes).assignSubject(loggedInSubject).findGroups();
      roleInheritanceContainer.setAllRoles(castGroupsToRoles(roles));
      
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
      
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }
  
  public void editRoleInheritanceSubmit(HttpServletRequest request, HttpServletResponse response) {
    
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
      
      String[] rolesThatImply = request.getParameterValues("rolesThatImmediatelyImply[]");
      String[] rolesImpliedBy = request.getParameterValues("rolesImpliedByImmediate[]");
      
      if (rolesThatImply != null) {
        for (Role roleThatImply:  group.getRoleInheritanceDelegate().getRolesInheritPermissionsToThis()) {   
          roleThatImply.getRoleInheritanceDelegate().removeRoleFromInheritFromThis(group);
        }
        
        for (String roleId: rolesThatImply) {
          Group roleThatImplies = GroupFinder.findByUuid(grouperSession, roleId, true);
          roleThatImplies.getRoleInheritanceDelegate().addRoleToInheritFromThis(group);
        }
      }
      
      if (rolesImpliedBy != null) {
        
        for (Role roleImpliedBy:  group.getRoleInheritanceDelegate().getRolesInheritPermissionsFromThis()) {
          group.getRoleInheritanceDelegate().removeRoleFromInheritFromThis(roleImpliedBy);
        }
        
        for (String roleId: rolesImpliedBy) {
          Group roleImpliedBy = GroupFinder.findByUuid(grouperSession, roleId, true);
          group.getRoleInheritanceDelegate().addRoleToInheritFromThis(roleImpliedBy);
        }
      }
      
      //go to the view group screen
      guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2Group.viewGroup&groupId=" + group.getId() + "')"));
  
      //lets show a success message on the new screen
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
  
  private List<Role> castGroupsToRoles(Collection<Group> groups) {
    
    List<Role> roles = new ArrayList<Role>();
    
    for (Group group: groups) {
      Role role = (Role)group;
      roles.add(role);
    }
    
    return roles;
    
  }

}
