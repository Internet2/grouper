/*
 * @author mchyzer
 * $Id: SimpleMembershipUpdate.java,v 1.7 2009-11-13 14:56:25 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.grouperUi.serviceLogic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.keyvalue.MultiKey;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.emory.mathcs.backport.java.util.Collections;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignResult;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefFinder;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiPermissionEntry;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiPermissionEntryActionsContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiPermissionEntryContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiResponseJs;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction;
import edu.internet2.middleware.grouper.grouperUi.beans.permissionUpdate.PermissionUpdateRequestContainer;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.permissions.PermissionEntry;
import edu.internet2.middleware.grouper.permissions.PermissionRoleDelegate;
import edu.internet2.middleware.grouper.permissions.PermissionEntry.PermissionType;
import edu.internet2.middleware.grouper.permissions.role.Role;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.ui.tags.TagUtils;
import edu.internet2.middleware.grouper.ui.util.GrouperUiUtils;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;


/**
 * main ajax methods for simple attribute update module
 */
public class SimplePermissionUpdate {
  
  /**
   * index page of application
   * @param request
   * @param response
   */
  public void assignInit(HttpServletRequest request, HttpServletResponse response) {
  
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
    PermissionUpdateRequestContainer.retrieveFromRequestOrCreate();

    guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#topDiv", 
      "/WEB-INF/grouperUi/templates/common/commonTop.jsp"));

    guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#bodyDiv", 
        "/WEB-INF/grouperUi/templates/simplePermissionUpdate/simplePermissionAssignInit.jsp"));
  
  }

  /**
   * assign a permission
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void assignPermission(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    PermissionUpdateRequestContainer permissionUpdateRequestContainer = PermissionUpdateRequestContainer.retrieveFromRequestOrCreate();
  
    GrouperSession grouperSession = null;
  
    try {
      grouperSession = GrouperSession.start(loggedInSubject);
      
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      
      PermissionType permissionType = retrievePermissionType(httpServletRequest, guiResponseJs, permissionUpdateRequestContainer);
      
      if (permissionType == null) {
        return;
      }
      
      //get the permission name to assign
      String permissionAssignAttributeName = StringUtils.trimToNull(httpServletRequest.getParameter("permissionAssignAttributeName"));

      AttributeDefName attributeDefName = null;
      if (!StringUtils.isBlank(permissionAssignAttributeName)) {
        attributeDefName = AttributeDefNameFinder.findById(permissionAssignAttributeName, false);
      }
  
      if (attributeDefName == null) {
        guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message(
            "simplePermissionUpdate.assignErrorPickPermissionResource", false)));
        return;
      }

      String permissionAssignRoleId = null;
      String attributeAssignMemberId = null;
  
      attributeAssignMemberId = StringUtils.trimToNull(httpServletRequest.getParameter("permissionAssignMemberId"));

      Member member = null;
      
      //member is not required, could assign a permission to a role
      if (!StringUtils.isBlank(attributeAssignMemberId)) {
        Subject subject = GrouperUiUtils.findSubject(attributeAssignMemberId, false);
        member = subject == null ? null : MemberFinder.findBySubject(grouperSession, subject, true);
        if (member == null) {
          guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message(
              "simplePermissionUpdate.assignErrorPickSubject", false)));
          return;
        }            
      }
      
      //why would subject be submitted?  something is wrong
      if (permissionType == PermissionType.role && member != null) {
        throw new RuntimeException("Why is member here if a role assignment????");
      }
      
      //member is required if assigning to a member
      if (permissionType == PermissionType.role_subject && member == null) {
        guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message(
            "simplePermissionUpdate.assignErrorPickSubjectForRoleSubject", false)));
        return;
      }
      
      permissionAssignRoleId = StringUtils.trimToNull(httpServletRequest.getParameter("permissionAssignRoleId"));
      Role role = null;
      if (!StringUtils.isBlank(permissionAssignRoleId)) {
        
        role = GroupFinder.findByUuid(grouperSession, permissionAssignRoleId, false);
      }
      
      //role is required
      if (role == null) {
        guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message(
            "simplePermissionUpdate.assignErrorPickRole", false)));
        return;
      }            
      
      AttributeAssignResult attributeAssignResult = null;
      
      //see if assigning to role
      if (permissionType == PermissionType.role) {
        
        PermissionRoleDelegate permissionRoleDelegate = role.getPermissionRoleDelegate();

        attributeAssignResult = permissionRoleDelegate.assignRolePermission(attributeDefName);
        
      } else if (permissionType == PermissionType.role_subject) {

        Set<Membership> memberships = GrouperUtil.nonNull(((Group)role).getMemberships(Group.getDefaultList(), GrouperUtil.toSet(member)));
        
        //we just need one
        if (GrouperUtil.length(memberships) == 0) {
          guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message(
              "simplePermissionUpdate.assignErrorMembershipRequired", false)));
          return;
        }
        
        PermissionRoleDelegate permissionRoleDelegate = role.getPermissionRoleDelegate();
        
        attributeAssignResult = permissionRoleDelegate.assignSubjectRolePermission(attributeDefName, member);
        
      } else {
        throw new RuntimeException("Permission type not expected: " + permissionType);
      }
      
      if (attributeAssignResult.isChanged()) {
        guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message(
            "simplePermissionUpdate.assignSuccess", false)));
        
      } else {
        guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message(
            "simplePermissionUpdate.errorAssignedAlready", false)));
      }
      
    } finally {
      GrouperSession.stopQuietly(grouperSession); 
    }
  
    assignFilter(httpServletRequest, httpServletResponse);

  
  }

  /**
   * the owner type drop down was changed
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void assignSelectOwnerType(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
  
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    PermissionUpdateRequestContainer permissionUpdateRequestContainer = PermissionUpdateRequestContainer.retrieveFromRequestOrCreate();
    
    if (retrievePermissionType(httpServletRequest, guiResponseJs,
        permissionUpdateRequestContainer) == null) {
      return;
    }

    //put in the generic panel that filters on attribute definitions
    guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#permissionAssignFilter", 
        "/WEB-INF/grouperUi/templates/simplePermissionUpdate/simplePermissionAssignFilter.jsp"));
  }

  /**
   * get the permission type out of the request
   * @param httpServletRequest
   * @param guiResponseJs
   * @param permissionUpdateRequestContainer
   * @return permissiontype if ok, null if not
   */
  private PermissionType retrievePermissionType(HttpServletRequest httpServletRequest,
      GuiResponseJs guiResponseJs,
      PermissionUpdateRequestContainer permissionUpdateRequestContainer) {

    String permissionAssignTypeString = httpServletRequest.getParameter("permissionAssignType");
    
    //clear out the assignments panel
    guiResponseJs.addAction(GuiScreenAction.newInnerHtml("#permissionAssignAssignments", ""));
    
    if (StringUtils.isBlank(permissionAssignTypeString)) {
      guiResponseJs.addAction(GuiScreenAction.newAlert(TagUtils.navResourceString("simplePermissionAssign.requiredOwnerType")));

      //clear out the filter panels for generic and specific
      guiResponseJs.addAction(GuiScreenAction.newInnerHtml("#permissionAssignFilter", ""));
      
      return null;
    }
    
    PermissionType permissionAssignType = PermissionType.valueOfIgnoreCase(permissionAssignTypeString, true);
    
    permissionUpdateRequestContainer.setPermissionType(permissionAssignType);
    return permissionAssignType;
  }
  
  /**
   * filter permission assignments and display the results
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void assignFilter(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    PermissionUpdateRequestContainer permissionUpdateRequestContainer = PermissionUpdateRequestContainer.retrieveFromRequestOrCreate();
  
    GrouperSession grouperSession = null;
  
    try {
      grouperSession = GrouperSession.start(loggedInSubject);
      
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      
      PermissionType permissionType = retrievePermissionType(httpServletRequest, guiResponseJs, permissionUpdateRequestContainer);
  
      if (permissionType == null) {
        return;
      }
      
      //get the permission name to assign
      String permissionAssignAttributeName = StringUtils.trimToNull(httpServletRequest.getParameter("permissionAssignAttributeName"));
      
      //attribute def name is not required
      AttributeDefName attributeDefName = null;
      if (!StringUtils.isBlank(permissionAssignAttributeName)) {

        attributeDefName = AttributeDefNameFinder.findById(permissionAssignAttributeName, false);
        
        //if cant find, but submitted
        if (attributeDefName == null) {
          guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message(
              "simplePermissionUpdate.assignErrorPickPermissionResource", false)));
        }
        
      }
  
      //get the subject id
      String attributeAssignMemberId = StringUtils.trimToNull(httpServletRequest.getParameter("permissionAssignMemberId"));

      Member member = null;
      
      //member is not required, could assign a permission to a role
      if (!StringUtils.isBlank(attributeAssignMemberId)) {
        Subject subject = GrouperUiUtils.findSubject(attributeAssignMemberId, false);
        member = subject == null ? null : MemberFinder.findBySubject(grouperSession, subject, true);
        
        //this is required if a value was submitted
        if (member == null) {
          guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message(
              "simplePermissionUpdate.assignErrorPickSubject", false)));
          return;
        }            
      }


      String permissionAssignAttributeDefId = StringUtils.trimToNull(httpServletRequest.getParameter("permissionAssignAttributeDef"));
      
      AttributeDef attributeDef = null;
      
      if (!StringUtils.isBlank(permissionAssignAttributeDefId)) {
        attributeDef = AttributeDefFinder.findById(permissionAssignAttributeDefId, false);
        if (attributeDef == null) {
          guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message(
              "simplePermissionUpdate.assignErrorPickPermissionDefinition", false)));
          return;
        }
      }

      String permissionAssignRoleId = StringUtils.trimToNull(httpServletRequest.getParameter("permissionAssignRoleId"));

      Role role = null;
      if (!StringUtils.isBlank(permissionAssignRoleId)) {
        
        role = GroupFinder.findByUuid(grouperSession, permissionAssignRoleId, false);

        //role is required if a string was submitted
        if (role == null) {
          guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message(
              "simplePermissionUpdate.assignErrorPickRole", false)));
          return;
        }            

      }
      
      //enabled / disabled
      String enabledDisabledString = httpServletRequest.getParameter("enabledDisabled");
      Boolean enabledDisabledBoolean = true;
      if (!StringUtils.isBlank(enabledDisabledString)) {
        
        if (StringUtils.equals(enabledDisabledString, "enabledOnly")) {
          enabledDisabledBoolean = true;
        } else if (StringUtils.equals(enabledDisabledString, "disabledOnly")) {
          enabledDisabledBoolean = false;
        } else if (StringUtils.equals(enabledDisabledString, "all")) {
          enabledDisabledBoolean = null;
        } else {
          throw new RuntimeException("Not expecting enabledDisabled: " + enabledDisabledString);
        }
        permissionUpdateRequestContainer.setEnabledDisabled(enabledDisabledBoolean);
      }
      
      Set<PermissionEntry> permissionEntriesFromDb = GrouperDAOFactory.getFactory().getPermissionEntry().findPermissions(
          permissionAssignAttributeDefId,  permissionAssignAttributeName, permissionAssignRoleId, 
          member == null ? null : member.getUuid(), enabledDisabledBoolean);
      
      List<GuiPermissionEntryActionsContainer> guiPermissionEntryActionsContainers = new ArrayList<GuiPermissionEntryActionsContainer>();
      
      //lets link the set of actions (alphabetized), to a GuiPermissionEntryActionsContainer
      Map<MultiKey, GuiPermissionEntryActionsContainer> actionsToPermissionsEntryActionsContainer 
        = new HashMap<MultiKey, GuiPermissionEntryActionsContainer>();
      
      //lets link the attribute def id to a GuiPermissionEntryActionsContainer
      Map<String, GuiPermissionEntryActionsContainer> attributeDefIdToPermissionsEntryActionsContainer 
        = new HashMap<String, GuiPermissionEntryActionsContainer>();

      //process the permissions to group up the GuiPermissionEntryActionsContainers
      for (PermissionEntry permissionEntry : permissionEntriesFromDb) {
        String attributeDefId = permissionEntry.getAttributeDefId();
        
        //see if we are all set
        GuiPermissionEntryActionsContainer guiPermissionEntryActionsContainer 
          = attributeDefIdToPermissionsEntryActionsContainer.get(attributeDefId);
        
        if (guiPermissionEntryActionsContainer == null) {
          //if we havent done this id yet
          //see if we have the actions taken care of
          AttributeDef currentAttributeDef = permissionEntry.getAttributeDef();
          
          List<String> actions = new ArrayList<String>(currentAttributeDef.getAttributeDefActionDelegate().allowedActionStrings());

          Collections.sort(actions);
          
          Object[] actionsArray = actions.toArray();
          
          MultiKey actionsKey = new MultiKey(actionsArray);
          
          //lets see if these actions are there
          guiPermissionEntryActionsContainer = actionsToPermissionsEntryActionsContainer.get(actionsKey);
          
          if (guiPermissionEntryActionsContainer == null) {
            
            guiPermissionEntryActionsContainer = new GuiPermissionEntryActionsContainer();
            guiPermissionEntryActionsContainer.setRawPermissionEntries(new ArrayList<PermissionEntry>());
            
            guiPermissionEntryActionsContainer.setActions(actions);
            
            guiPermissionEntryActionsContainers.add(guiPermissionEntryActionsContainer);
            
            actionsToPermissionsEntryActionsContainer.put(actionsKey, guiPermissionEntryActionsContainer);
            
          }
          attributeDefIdToPermissionsEntryActionsContainer.put(attributeDefId, guiPermissionEntryActionsContainer);
        }
        guiPermissionEntryActionsContainer.getRawPermissionEntries().add(permissionEntry);
      }
      
      
      for (GuiPermissionEntryActionsContainer guiPermissionEntryActionsContainer : guiPermissionEntryActionsContainers) {

        for (PermissionEntry permissionEntry : guiPermissionEntryActionsContainer.getRawPermissionEntries()) {
          
          List<GuiPermissionEntryContainer> guiPermissionEntryContainers = new ArrayList<GuiPermissionEntryContainer>();
          guiPermissionEntryActionsContainer.setGuiPermissionEntryContainers(guiPermissionEntryContainers);

          GuiPermissionEntry guiPermissionEntry = new GuiPermissionEntry();
          guiPermissionEntry.setPermissionEntry(permissionEntry);
          List<GuiPermissionEntry> guiPermissionEntries = new ArrayList<GuiPermissionEntry>();
          guiPermissionEntries.add(guiPermissionEntry);
          
          GuiPermissionEntryContainer guiPermissionEntryContainer = new GuiPermissionEntryContainer();
          guiPermissionEntryContainer.setGuiPermissionEntries(guiPermissionEntries);
          guiPermissionEntryContainers.add(guiPermissionEntryContainer);
          
        }
        
      }
      
      permissionUpdateRequestContainer.setGuiPermissionEntryActionsContainers(guiPermissionEntryActionsContainers);
      
      //set the permissions panel
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#permissionAssignAssignments", 
        "/WEB-INF/grouperUi/templates/simplePermissionUpdate/simplePermissionAssignments.jsp"));
      
      guiResponseJs.addAction(GuiScreenAction.newScript("guiScrollTo('#permissionAssignAssignments');"));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession); 
    }
  
  }

  /** logger */
  @SuppressWarnings("unused")
  private static final Log LOG = LogFactory.getLog(SimplePermissionUpdate.class);
}
