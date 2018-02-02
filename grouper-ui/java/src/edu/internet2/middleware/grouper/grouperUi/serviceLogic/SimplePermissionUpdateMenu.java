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
package edu.internet2.middleware.grouper.grouperUi.serviceLogic;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignType;
import edu.internet2.middleware.grouper.attr.finder.AttributeAssignFinder;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiAttributeAssign;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiAttributeDef;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiAttributeDefName;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiGroup;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiPermissionEntry;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiResponseJs;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction.GuiMessageType;
import edu.internet2.middleware.grouper.grouperUi.beans.permissionUpdate.PermissionUpdateRequestContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.TextContainer;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.permissions.PermissionEntry;
import edu.internet2.middleware.grouper.permissions.PermissionEntry.PermissionType;
import edu.internet2.middleware.grouper.permissions.PermissionEntryUtils;
import edu.internet2.middleware.grouper.permissions.PermissionFinder;
import edu.internet2.middleware.grouper.permissions.PermissionHeuristic;
import edu.internet2.middleware.grouper.permissions.PermissionHeuristicBetter;
import edu.internet2.middleware.grouper.permissions.PermissionHeuristics;
import edu.internet2.middleware.grouper.permissions.role.Role;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.ui.exceptions.ControllerDone;
import edu.internet2.middleware.grouper.ui.exceptions.NoSessionException;
import edu.internet2.middleware.grouper.ui.tags.TagUtils;
import edu.internet2.middleware.grouper.ui.tags.menu.DhtmlxMenu;
import edu.internet2.middleware.grouper.ui.tags.menu.DhtmlxMenuItem;
import edu.internet2.middleware.grouper.ui.util.GrouperUiUtils;
import edu.internet2.middleware.grouper.ui.util.HttpContentType;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * simple permission update menu
 * @author mchyzer
 */
public class SimplePermissionUpdateMenu {

  /**
   * handle a click or select from the assignment menu
   * @param httpServletRequest
   * @param httpServletResponse
   */
  @SuppressWarnings("unused")
  public void assignmentMenu(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
    
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
  
    GrouperSession grouperSession = null;
      
    String menuItemId = httpServletRequest.getParameter("menuItemId");
    String menuHtmlId = httpServletRequest.getParameter("menuIdOfMenuTarget");
  
    if (StringUtils.equals(menuItemId, "editAssignment")) {
      this.assignmentMenuEditAssignment(false);
    } else if (StringUtils.equals(menuItemId, "analyzeAssignment")) {
      this.assignmentMenuAnalyzeAssignment(false);
    } else if (StringUtils.equals(menuItemId, "addLimit")) {
      this.assignmentMenuAddLimit(false);
    } else if (StringUtils.equals(menuItemId, "addLimitNewUi")) {
      this.assignmentMenuAddLimit(true);
    } else if (StringUtils.equals(menuItemId, "analyzeAssignmentNewUi")) {
      this.assignmentMenuAnalyzeAssignment(true);
    } else if (StringUtils.equals(menuItemId, "editAssignmentNewUi")) {
      this.assignmentMenuEditAssignment(true);
    } else {
      throw new RuntimeException("Unexpected menu id: '" + menuItemId + "'");
    }
  
    
  }

  /**
   * add a limit on an assignment
   * @param newUi: true when this method called for new ui and false for lite ui.
   */
  public void assignmentMenuAddLimit(boolean newUi) {
    
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
    PermissionUpdateRequestContainer permissionUpdateRequestContainer = PermissionUpdateRequestContainer.retrieveFromRequestOrCreate();

    //lets see which subject we are dealing with:
    HttpServletRequest httpServletRequest = GrouperUiFilter.retrieveHttpServletRequest();
    String menuIdOfMenuTarget = httpServletRequest.getParameter("menuIdOfMenuTarget");
  
    if (StringUtils.isBlank(menuIdOfMenuTarget)) {
      throw new RuntimeException("Missing id of menu target");
    }
    if (!menuIdOfMenuTarget.startsWith("permissionMenuButton_")) {
      throw new RuntimeException("Invalid id of menu target: '" + menuIdOfMenuTarget + "'");
    }
    String guiPermissionId = GrouperUtil.prefixOrSuffix(menuIdOfMenuTarget, "permissionMenuButton_", false);
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
      
      //<c:set var="guiPermissionId" value="${firstPermissionEntry.roleId}__${firstPermissionEntry.memberId}__${firstPermissionEntry.attributeDefNameId}__${firstPermissionEntry.action}" />
      Pattern pattern = Pattern.compile("^(.*)__(.*)__(.*)__(.*)__(.*)$");
      Matcher matcher = pattern.matcher(guiPermissionId);
      if (!matcher.matches()) {
        throw new RuntimeException("Why does guiPermissionId not match? " + guiPermissionId);
      }

      //get current state
      Role role = null;
      {
        String roleId = matcher.group(1);
        role = GroupFinder.findByUuid(grouperSession, roleId, true);
        if (!((Group)role).hasAdmin(loggedInSubject)) {
          if (newUi) {
            guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error,
                TextContainer.retrieveFromRequest().getText().get("simplePermissionUpdate.errorCantManageRole")));
          } else {            
            guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message("simplePermissionUpdate.errorCantManageRole", false)));
          }
          return;
        }
      }
      
      String permissionTypeString = matcher.group(5);
      PermissionType permissionType = PermissionType.valueOfIgnoreCase(permissionTypeString, true);
      permissionUpdateRequestContainer.setPermissionType(permissionType);
      
      Member member = null;
      { 
        if (permissionType == PermissionType.role_subject) {
          String memberId = matcher.group(2);
          member = MemberFinder.findByUuid(grouperSession, memberId, true);
        }
      }
      AttributeDef attributeDef = null;
      AttributeDefName attributeDefName = null;
      {
        String attributeDefNameId = matcher.group(3);
        attributeDefName = AttributeDefNameFinder.findById(attributeDefNameId, true);
        attributeDef = attributeDefName.getAttributeDef();
        if (!PrivilegeHelper.canAttrUpdate(GrouperSession.staticGrouperSession(), attributeDef, loggedInSubject)) {
          if (newUi) {
            guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error,
                TextContainer.retrieveFromRequest().getText().get("simplePermissionUpdate.errorCantEditAttributeDef")));
          } else {            
            guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message("simplePermissionUpdate.errorCantEditAttributeDef", false)));
          }
          return;
        }
        
      }
      
      String action = matcher.group(4);

      //get the assignment

      PermissionFinder permissionFinder = new PermissionFinder().addAction(action).addRoleId(role.getId()).addPermissionNameId(attributeDefName.getId());
      if (permissionType == PermissionType.role_subject) {
        permissionFinder.addMemberId(member.getUuid());
      }
      permissionFinder.assignPermissionType(permissionType);
      permissionFinder.assignImmediateOnly(true);
      PermissionEntry permissionEntry = permissionFinder.findPermission(false);
      
      if (permissionEntry == null || permissionEntry.isDisallowed()) {
        if (newUi) {
          guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error,
              TextContainer.retrieveFromRequest().getText().get("simplePermissionUpdate.noImmediatePermissionFoundForLimit")));
        } else {          
          guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message("simplePermissionUpdate.noImmediatePermissionFoundForLimit", false)));
        }
        return;
      }

      GuiPermissionEntry guiPermissionEntry = new GuiPermissionEntry();
      String ownerGroupId = permissionEntry.getRole().getId();
      Group ownerGroup = GroupFinder.findByUuid(grouperSession, ownerGroupId, true);
      GuiGroup guiRole = new GuiGroup(ownerGroup);
      guiPermissionEntry.setGuiRole(guiRole);
      guiPermissionEntry.setPermissionEntry(permissionEntry);
      guiPermissionEntry.setPermissionType(permissionType);
      guiPermissionEntry.setGuiAttributeDefName(new GuiAttributeDefName(permissionEntry.getAttributeDefName()));
      
      permissionUpdateRequestContainer.setGuiPermissionEntry(guiPermissionEntry);
      
      if (newUi) {
        guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#groupPermission",
            "/WEB-INF/grouperUi2/permission/permissionAddLimit.jsp"));
        return;
      }

      //set the permissions panel
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#permissionAssignAssignments", 
        "/WEB-INF/grouperUi/templates/simplePermissionUpdate/simplePermissionAddLimit.jsp"));
      
      guiResponseJs.addAction(GuiScreenAction.newScript("guiScrollTo('#permissionAssignAssignments');"));
  
    } catch (ControllerDone cd) {
      throw cd;
    } catch (NoSessionException nse) {
      throw nse;
    } catch (RuntimeException re) {
      throw new RuntimeException("Error addLimit menu item: " + menuIdOfMenuTarget 
          + ", " + re.getMessage(), re);
    } finally {
      GrouperSession.stopQuietly(grouperSession); 
    }
  }

  /**
   * edit the enabled disabled
   * @param newUi: true when this method called for new ui and false for lite ui. 
   */
  public void assignmentMenuEditAssignment(boolean newUi) {
    
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
    PermissionUpdateRequestContainer permissionUpdateRequestContainer = PermissionUpdateRequestContainer.retrieveFromRequestOrCreate();

    //lets see which subject we are dealing with:
    HttpServletRequest httpServletRequest = GrouperUiFilter.retrieveHttpServletRequest();
    String menuIdOfMenuTarget = httpServletRequest.getParameter("menuIdOfMenuTarget");
  
    if (StringUtils.isBlank(menuIdOfMenuTarget)) {
      throw new RuntimeException("Missing id of menu target");
    }
    if (!menuIdOfMenuTarget.startsWith("permissionMenuButton_")) {
      throw new RuntimeException("Invalid id of menu target: '" + menuIdOfMenuTarget + "'");
    }
    String guiPermissionId = GrouperUtil.prefixOrSuffix(menuIdOfMenuTarget, "permissionMenuButton_", false);
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
      
      
      //<c:set var="guiPermissionId" value="${firstPermissionEntry.roleId}__${firstPermissionEntry.memberId}__${firstPermissionEntry.attributeDefNameId}__${firstPermissionEntry.action}" />
      Pattern pattern = Pattern.compile("^(.*)__(.*)__(.*)__(.*)__(.*)$");
      Matcher matcher = pattern.matcher(guiPermissionId);
      if (!matcher.matches()) {
        throw new RuntimeException("Why does guiPermissionId not match? " + guiPermissionId);
      }

      //get current state
      Role role = null;
      {
        String roleId = matcher.group(1);
        role = GroupFinder.findByUuid(grouperSession, roleId, true);
        if (!((Group)role).hasAdmin(loggedInSubject)) {
          if (newUi) {
            guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error,
                TextContainer.retrieveFromRequest().getText().get("simplePermissionUpdate.errorCantManageRole")));
          } else {            
            guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message("simplePermissionUpdate.errorCantManageRole", false)));
          }
          return;
        }
      }
      
      String permissionTypeString = matcher.group(5);
      PermissionType permissionType = PermissionType.valueOfIgnoreCase(permissionTypeString, true);
      permissionUpdateRequestContainer.setPermissionType(permissionType);
      
      Member member = null;
      { 
        if (permissionType == PermissionType.role_subject) {
          String memberId = matcher.group(2);
          member = MemberFinder.findByUuid(grouperSession, memberId, true);
        }
      }
      AttributeDef attributeDef = null;
      AttributeDefName attributeDefName = null;
      {
        String attributeDefNameId = matcher.group(3);
        attributeDefName = AttributeDefNameFinder.findById(attributeDefNameId, true);
        attributeDef = attributeDefName.getAttributeDef();
        if (!PrivilegeHelper.canAttrUpdate(GrouperSession.staticGrouperSession(), attributeDef, loggedInSubject)) {
          if (newUi) {
            guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error,
                TextContainer.retrieveFromRequest().getText().get("simplePermissionUpdate.errorCantEditAttributeDef")));
          } else {            
            guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message("simplePermissionUpdate.errorCantEditAttributeDef", false)));
          }
          return;
        }
        
      }
      
      String action = matcher.group(4);

      //get the assignment

      PermissionFinder permissionFinder = new PermissionFinder().addAction(action).addRoleId(role.getId()).addPermissionNameId(attributeDefName.getId());
      if (permissionType == PermissionType.role_subject) {
        permissionFinder.addMemberId(member.getUuid());
      }
      permissionFinder.assignPermissionType(permissionType);
      permissionFinder.assignImmediateOnly(true);
      PermissionEntry permissionEntry = permissionFinder.findPermission(false);
      
      if (permissionEntry == null) {
        if (newUi) {
          guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error,
              TextContainer.retrieveFromRequest().getText().get("simplePermissionUpdate.noImmediatePermissionFound")));
        } else {          
          guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message("simplePermissionUpdate.noImmediatePermissionFound", false)));
        }
        return;
      }

      Group ownerGroup = GroupFinder.findByUuid(grouperSession, permissionEntry.getRoleId(), true);
      
      GuiPermissionEntry guiPermissionEntry = new GuiPermissionEntry();
      guiPermissionEntry.setPermissionEntry(permissionEntry);
      guiPermissionEntry.setPermissionType(permissionType);
      GuiGroup guiRole = new GuiGroup(ownerGroup);
      guiPermissionEntry.setGuiRole(guiRole);
      guiPermissionEntry.setGuiAttributeDefName(new GuiAttributeDefName(permissionEntry.getAttributeDefName()));
      guiPermissionEntry.setGuiAttributeDef(new GuiAttributeDef(permissionEntry.getAttributeDef()));
      
      permissionUpdateRequestContainer.setGuiPermissionEntry(guiPermissionEntry);
      
      if (newUi) {
        guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#groupPermission", 
            "/WEB-INF/grouperUi2/permission/permissionEdit.jsp"));
        return;
      }
            
      guiResponseJs.addAction(GuiScreenAction.newDialogFromJsp(
        "/WEB-INF/grouperUi/templates/simplePermissionUpdate/simplePermissionEdit.jsp"));

  
    } catch (ControllerDone cd) {
      throw cd;
    } catch (NoSessionException nse) {
      throw nse;
    } catch (RuntimeException re) {
      throw new RuntimeException("Error editAssignment menu item: " + menuIdOfMenuTarget 
          + ", " + re.getMessage(), re);
    } finally {
      GrouperSession.stopQuietly(grouperSession); 
    }
  
  }

  /**
   * analyze assignment
   * @param newUi- true when this method is called for new ui and false for lite ui
   */
  public void assignmentMenuAnalyzeAssignment(boolean newUi) {
    
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
    PermissionUpdateRequestContainer permissionUpdateRequestContainer = PermissionUpdateRequestContainer.retrieveFromRequestOrCreate();

    //lets see which subject we are dealing with:
    HttpServletRequest httpServletRequest = GrouperUiFilter.retrieveHttpServletRequest();
    String menuIdOfMenuTarget = httpServletRequest.getParameter("menuIdOfMenuTarget");
  
    if (StringUtils.isBlank(menuIdOfMenuTarget)) {
      throw new RuntimeException("Missing id of menu target");
    }
    if (!menuIdOfMenuTarget.startsWith("permissionMenuButton_")) {
      throw new RuntimeException("Invalid id of menu target: '" + menuIdOfMenuTarget + "'");
    }
    String guiPermissionId = GrouperUtil.prefixOrSuffix(menuIdOfMenuTarget, "permissionMenuButton_", false);
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
      
      
      //<c:set var="guiPermissionId" value="${firstPermissionEntry.roleId}__${firstPermissionEntry.memberId}__${firstPermissionEntry.attributeDefNameId}__${firstPermissionEntry.action}" />
      Pattern pattern = Pattern.compile("^(.*)__(.*)__(.*)__(.*)__(.*)$");
      Matcher matcher = pattern.matcher(guiPermissionId);
      if (!matcher.matches()) {
        throw new RuntimeException("Why does guiPermissionId not match? " + guiPermissionId);
      }

      //get current state
      Role role = null;
      {
        String roleId = matcher.group(1);
        role = GroupFinder.findByUuid(grouperSession, roleId, true);
        if (!((Group)role).hasAdmin(loggedInSubject)) {
          if (newUi) {            
            guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error,
                TextContainer.retrieveFromRequest().getText().get("simplePermissionUpdate.errorCantManageRole")));
          } else {            
            guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message("simplePermissionUpdate.errorCantManageRole", false)));
          }
          return;
        }
      }
      
      String permissionTypeString = matcher.group(5);
      PermissionType permissionType = PermissionType.valueOfIgnoreCase(permissionTypeString, true);
      permissionUpdateRequestContainer.setPermissionType(permissionType);
      
      Member member = null;
      { 
        if (permissionType == PermissionType.role_subject) {
          String memberId = matcher.group(2);
          member = MemberFinder.findByUuid(grouperSession, memberId, true);
        }
      }
      AttributeDef attributeDef = null;
      AttributeDefName attributeDefName = null;
      {
        String attributeDefNameId = matcher.group(3);
        attributeDefName = AttributeDefNameFinder.findById(attributeDefNameId, true);
        attributeDef = attributeDefName.getAttributeDef();
        if (!PrivilegeHelper.canAttrUpdate(GrouperSession.staticGrouperSession(), attributeDef, loggedInSubject)) {
          if (newUi) {            
            guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error,
                TextContainer.retrieveFromRequest().getText().get("simplePermissionUpdate.errorCantEditAttributeDef")));
          } else {            
            guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message("simplePermissionUpdate.errorCantEditAttributeDef", false)));
          }
          return;
        }
        
      }
      
      String action = matcher.group(4);

      //get all the assignments
      Set<PermissionEntry> permissionEntries = null;
      
      if (permissionType == PermissionType.role_subject) {

        permissionEntries = GrouperDAOFactory.getFactory()
          .getPermissionEntry().findPermissions(null, attributeDefName.getId(), role.getId(), member.getUuid(), action, null);

      } else if (permissionType == PermissionType.role) {
        
        permissionEntries = GrouperDAOFactory.getFactory()
          .getPermissionEntry().findRolePermissions(null, attributeDefName.getId(), role.getId(), action, null);
        
        
      } else {
        throw new RuntimeException("Invalid permissionType: " + permissionType);
      }

      if (GrouperUtil.length(permissionEntries) == 0) {
        if (newUi) {            
          guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error,
              TextContainer.retrieveFromRequest().getText().get("simplePermissionUpdate.analyzeNoPermissionFound")));
        } else {          
          guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message("simplePermissionUpdate.analyzeNoPermissionFound", false)));
        }
        return;
      }
      
      PermissionEntry permissionEntry = null;
      
      List<PermissionEntry> permissionEntriesList = new ArrayList<PermissionEntry>();
      
      Iterator<PermissionEntry> iterator = permissionEntries.iterator();
      while (iterator.hasNext()) {
       
        PermissionEntry current = iterator.next();
        
        //find the immediate one
        if (current.isImmediate(permissionType)) {
          permissionEntry = current;
          iterator.remove();
          //move this to the front of the list
          permissionEntriesList.add(permissionEntry);
          break;
        }
      }
      
      //add the rest
      permissionEntriesList.addAll(permissionEntries);

      PermissionEntryUtils.orderByAndSetFriendlyHeuristic(permissionEntriesList);


      GuiPermissionEntry guiPermissionEntry = new GuiPermissionEntry();
     
      permissionEntry = permissionEntriesList.get(0);
      
      Group ownerGroup = GroupFinder.findByUuid(grouperSession, permissionEntry.getRoleId(), true);
      
      guiPermissionEntry.setPermissionEntry(permissionEntry);
      guiPermissionEntry.setPermissionType(permissionType);
      GuiGroup guiRole = new GuiGroup(ownerGroup);
      guiPermissionEntry.setGuiRole(guiRole);
      guiPermissionEntry.setGuiAttributeDefName(new GuiAttributeDefName(permissionEntry.getAttributeDefName()));
      guiPermissionEntry.setGuiAttributeDef(new GuiAttributeDef(permissionEntry.getAttributeDef()));

      List<GuiPermissionEntry> rawGuiPermissionEntries = new ArrayList<GuiPermissionEntry>();
      
      boolean isFirst = true;
      
      PermissionHeuristics firstHeuristics = permissionEntriesList.get(0).getPermissionHeuristics();
      
      String isBetterThan =  GrouperUiUtils.message("simplePermissionAssign.analyzeIsBetterThan", false); 
      
      for (PermissionEntry current : permissionEntriesList) {

        Group currentOwnerGroup = GroupFinder.findByUuid(grouperSession, current.getRoleId(), true);
        GuiGroup currentGuiRole = new GuiGroup(currentOwnerGroup);
        
        GuiPermissionEntry guiCurrent = new GuiPermissionEntry();
        guiCurrent.setPermissionEntry(current);
        guiCurrent.setGuiRole(currentGuiRole);
        guiCurrent.setGuiAttributeDefName(new GuiAttributeDefName(permissionEntry.getAttributeDefName()));
        guiCurrent.setGuiAttributeDef(new GuiAttributeDef(permissionEntry.getAttributeDef()));
        
        rawGuiPermissionEntries.add(guiCurrent);
        
        if (!isFirst) {
          
          PermissionHeuristics currentHeuristics = current.getPermissionHeuristics();
          
          PermissionHeuristicBetter permissionHeuristicBetter = firstHeuristics.whyBetterThanArg(currentHeuristics);
          String compareWithBest = null;
          if (permissionHeuristicBetter == null) {
            //they are equivalent
            
            compareWithBest = GrouperUiUtils.message("simplePermissionAssign.analyzeType.same", false);
          } else {
            
            PermissionHeuristic firstHeuristic = permissionHeuristicBetter.getThisPermissionHeuristic();
            
            String firstMessage = null;
            
            if (firstHeuristic.getDepth() == 0) {
              firstMessage = GrouperUiUtils.message("simplePermissionAssign.analyzeType." 
                  + firstHeuristic.getPermissionHeuristicType().name() + ".0", false);
            } else {
              firstMessage = GrouperUiUtils.message("simplePermissionAssign.analyzeType." 
                  + firstHeuristic.getPermissionHeuristicType().name(), false, false, Integer.toString(firstHeuristic.getDepth()));
            }
            
            PermissionHeuristic currentHeuristic = permissionHeuristicBetter.getOtherPermissionHeuristic();
            
            String secondMessage = null;
            
            if (currentHeuristic == null) {
              
              //disallow
              secondMessage = GrouperUiUtils.message("simplePermissionAssign.analyzeType.disallow", false);
              
            } else {
              
              if (currentHeuristic.getDepth() == 0) {
                secondMessage = GrouperUiUtils.message("simplePermissionAssign.analyzeType." 
                    + currentHeuristic.getPermissionHeuristicType().name() + ".0", false);
              } else {
                secondMessage = GrouperUiUtils.message("simplePermissionAssign.analyzeType." 
                    + currentHeuristic.getPermissionHeuristicType().name(), false, false, Integer.toString(currentHeuristic.getDepth()));
              }
            }
            
            compareWithBest = firstMessage + " " + isBetterThan + " " + secondMessage;
          }
          
          
          guiCurrent.setCompareWithBest(StringUtils.capitalize(compareWithBest));
        }
        
        isFirst = false;
      }
      
      guiPermissionEntry.setRawGuiPermissionEntries(rawGuiPermissionEntries);
      guiPermissionEntry.processRawEntries();

      permissionUpdateRequestContainer.setGuiPermissionEntry(guiPermissionEntry);
      
      if (newUi) {
        guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#groupPermission", 
            "/WEB-INF/grouperUi2/permission/permissionAnalyze.jsp"));
        return;
      }
            
      guiResponseJs.addAction(GuiScreenAction.newDialogFromJsp(
        "/WEB-INF/grouperUi/templates/simplePermissionUpdate/simplePermissionAnalyze.jsp"));
  
    } catch (ControllerDone cd) {
      throw cd;
    } catch (NoSessionException nse) {
      throw nse;
    } catch (RuntimeException re) {
      throw new RuntimeException("Error editAssignment menu item: " + menuIdOfMenuTarget 
          + ", " + re.getMessage(), re);
    } finally {
      GrouperSession.stopQuietly(grouperSession); 
    }
  
  }

  /**
   * make the structure of the attribute assignment
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void assignmentMenuStructure(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
    
    DhtmlxMenu dhtmlxMenu = new DhtmlxMenu();
    
    {
      DhtmlxMenuItem addLimitMenuItem = new DhtmlxMenuItem();
      addLimitMenuItem.setId("addLimit");
      addLimitMenuItem.setText(TagUtils.navResourceString("simplePermissionAssign.addLimit"));
      addLimitMenuItem.setTooltip(TagUtils.navResourceString("simplePermissionAssign.addLimitTooltip"));
      dhtmlxMenu.addDhtmlxItem(addLimitMenuItem);
    }    
  
    {
      DhtmlxMenuItem analyzeAssignmentMenuItem = new DhtmlxMenuItem();
      analyzeAssignmentMenuItem.setId("analyzeAssignment");
      analyzeAssignmentMenuItem.setText(TagUtils.navResourceString("simplePermissionAssign.assignMenuAnalyzeAssignment"));
      analyzeAssignmentMenuItem.setTooltip(TagUtils.navResourceString("simplePermissionAssign.assignMenuAnalyzeAssignmentTooltip"));
      dhtmlxMenu.addDhtmlxItem(analyzeAssignmentMenuItem);
    }    
  
    {
      DhtmlxMenuItem editAssignmentMenuItem = new DhtmlxMenuItem();
      editAssignmentMenuItem.setId("editAssignment");
      editAssignmentMenuItem.setText(TagUtils.navResourceString("simplePermissionAssign.editAssignment"));
      editAssignmentMenuItem.setTooltip(TagUtils.navResourceString("simplePermissionAssign.editAssignmentTooltip"));
      dhtmlxMenu.addDhtmlxItem(editAssignmentMenuItem);
    }    
  
    GrouperUiUtils.printToScreen("<?xml version=\"1.0\"?>\n" + 
        dhtmlxMenu.toXml(), HttpContentType.TEXT_XML, false, false);
  
    throw new ControllerDone();
  }
  
  /**
   * make the structure of the attribute assignment for new ui
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void assignmentMenuStructureNewUi(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
    
    DhtmlxMenu dhtmlxMenu = new DhtmlxMenu();
    
    {
      DhtmlxMenuItem addLimitMenuItem = new DhtmlxMenuItem();
      addLimitMenuItem.setId("addLimitNewUi");
      addLimitMenuItem.setText(TagUtils.navResourceString("simplePermissionAssign.addLimit"));
      addLimitMenuItem.setTooltip(TagUtils.navResourceString("simplePermissionAssign.addLimitTooltip"));
      dhtmlxMenu.addDhtmlxItem(addLimitMenuItem);
    }    
  
    {
      DhtmlxMenuItem analyzeAssignmentMenuItem = new DhtmlxMenuItem();
      analyzeAssignmentMenuItem.setId("analyzeAssignmentNewUi");
      analyzeAssignmentMenuItem.setText(TagUtils.navResourceString("simplePermissionAssign.assignMenuAnalyzeAssignment"));
      analyzeAssignmentMenuItem.setTooltip(TagUtils.navResourceString("simplePermissionAssign.assignMenuAnalyzeAssignmentTooltip"));
      dhtmlxMenu.addDhtmlxItem(analyzeAssignmentMenuItem);
    }    
  
    {
      DhtmlxMenuItem editAssignmentMenuItem = new DhtmlxMenuItem();
      editAssignmentMenuItem.setId("editAssignmentNewUi");
      editAssignmentMenuItem.setText(TagUtils.navResourceString("simplePermissionAssign.editAssignment"));
      editAssignmentMenuItem.setTooltip(TagUtils.navResourceString("simplePermissionAssign.editAssignmentTooltip"));
      dhtmlxMenu.addDhtmlxItem(editAssignmentMenuItem);
    }    
  
    GrouperUiUtils.printToScreen("<?xml version=\"1.0\"?>\n" + 
        dhtmlxMenu.toXml(), HttpContentType.TEXT_XML, false, false);
  
    throw new ControllerDone();
  }

  /**
   * make the structure of the limit menu for lite ui
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void limitMenuStructure(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
    
    DhtmlxMenu dhtmlxMenu = new DhtmlxMenu();
    
    {
      DhtmlxMenuItem addValueMenuItem = new DhtmlxMenuItem();
      addValueMenuItem.setId("addValue");
      addValueMenuItem.setText(TagUtils.navResourceString("simplePermissionAssign.limitMenuAddValue"));
      addValueMenuItem.setTooltip(TagUtils.navResourceString("simplePermissionAssign.limitMenuAddValueTooltip"));
      dhtmlxMenu.addDhtmlxItem(addValueMenuItem);
    }
    
    GrouperUiUtils.printToScreen("<?xml version=\"1.0\"?>\n" + 
        dhtmlxMenu.toXml(), HttpContentType.TEXT_XML, false, false);
  
    throw new ControllerDone();
  }
  
  /**
   * make the structure of the limit menu for the new ui
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void limitMenuStructureNewUi(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
    
    DhtmlxMenu dhtmlxMenu = new DhtmlxMenu();
    
    {
      DhtmlxMenuItem addValueMenuItem = new DhtmlxMenuItem();
      addValueMenuItem.setId("addValueNewUi");
      addValueMenuItem.setText(TagUtils.navResourceString("simplePermissionAssign.limitMenuAddValue"));
      addValueMenuItem.setTooltip(TagUtils.navResourceString("simplePermissionAssign.limitMenuAddValueTooltip"));
      dhtmlxMenu.addDhtmlxItem(addValueMenuItem);
    }
    
    {
      DhtmlxMenuItem editLimitMenuItem = new DhtmlxMenuItem();
      editLimitMenuItem.setId("editLimitNewUi");
      editLimitMenuItem.setText(TagUtils.navResourceString("simplePermissionAssign.limitMenuEditLimit"));
      editLimitMenuItem.setTooltip(TagUtils.navResourceString("simplePermissionAssign.limitMenuEditLimitTooltip"));
      dhtmlxMenu.addDhtmlxItem(editLimitMenuItem);
    }
    
    {
      DhtmlxMenuItem deleteLimitMenuItem = new DhtmlxMenuItem();
      deleteLimitMenuItem.setId("deleteLimitNewUi");
      deleteLimitMenuItem.setText(TagUtils.navResourceString("simplePermissionAssign.limitMenuDeleteLimit"));
      deleteLimitMenuItem.setTooltip(TagUtils.navResourceString("simplePermissionAssign.limitMenuDeleteLimitTooltip"));
      dhtmlxMenu.addDhtmlxItem(deleteLimitMenuItem);
    }
  
    GrouperUiUtils.printToScreen("<?xml version=\"1.0\"?>\n" + 
        dhtmlxMenu.toXml(), HttpContentType.TEXT_XML, false, false);
  
    throw new ControllerDone();
  }
  
  /**
   * handle a click or select from the limit menu
   * @param httpServletRequest
   * @param httpServletResponse
   */
  @SuppressWarnings("unused")
  public void limitMenu(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
    
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
  
    GrouperSession grouperSession = null;
      
    String menuItemId = httpServletRequest.getParameter("menuItemId");
    String menuHtmlId = httpServletRequest.getParameter("menuIdOfMenuTarget");
  
    if (StringUtils.equals(menuItemId, "addValue")) {
      this.limitMenuAddValue(false);
    } else if (StringUtils.equals(menuItemId, "addValueNewUi")) {
      this.limitMenuAddValue(true);
    } else if (StringUtils.equals(menuItemId, "editLimitNewUi")) {
      httpServletRequest.setAttribute("newUi", true);
      httpServletRequest.setAttribute("limitAssignId", retrieveLimitAssignId(httpServletRequest));
      new SimplePermissionUpdate().assignLimitEdit(httpServletRequest, httpServletResponse);
    } else if (StringUtils.equals(menuItemId, "deleteLimitNewUi")) {
      httpServletRequest.setAttribute("limitAssignId", retrieveLimitAssignId(httpServletRequest));
      new UiV2Permission().limitDelete(httpServletRequest, httpServletResponse);
    }  
    
    else {
      throw new RuntimeException("Unexpected menu id: '" + menuItemId + "'");
    }
  }
  
  /**
   * @param httpServletRequest
   * @return limitAssignId from request
   */
  private String retrieveLimitAssignId(HttpServletRequest httpServletRequest) {
    String menuIdOfMenuTarget = httpServletRequest.getParameter("menuIdOfMenuTarget");
    
    if (StringUtils.isBlank(menuIdOfMenuTarget)) {
      throw new RuntimeException("Missing id of menu target");
    }
    if (!menuIdOfMenuTarget.startsWith("limitMenuButton_")) {
      throw new RuntimeException("Invalid id of menu target: '" + menuIdOfMenuTarget + "'");
    }
    String limitAssignId = GrouperUtil.prefixOrSuffix(menuIdOfMenuTarget, "limitMenuButton_", false);
    return limitAssignId;
  }
  
  /**
   * make the structure of the limit value menu for the new ui
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void limitValueMenuStructure(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
    
    DhtmlxMenu dhtmlxMenu = new DhtmlxMenu();
       
    {
      DhtmlxMenuItem editLimitMenuItem = new DhtmlxMenuItem();
      editLimitMenuItem.setId("editLimitValueNewUi");
      editLimitMenuItem.setText(TagUtils.navResourceString("simplePermissionAssign.limitMenuEditValue"));
      editLimitMenuItem.setTooltip(TagUtils.navResourceString("simplePermissionAssign.limitMenuEditValueTooltip"));
      dhtmlxMenu.addDhtmlxItem(editLimitMenuItem);
    }
    
    {
      DhtmlxMenuItem deleteLimitMenuItem = new DhtmlxMenuItem();
      deleteLimitMenuItem.setId("deleteLimitValueNewUi");
      deleteLimitMenuItem.setText(TagUtils.navResourceString("simplePermissionAssign.limitMenuDeleteValue"));
      deleteLimitMenuItem.setTooltip(TagUtils.navResourceString("simplePermissionAssign.limitMenuDeleteValueTooltip"));
      dhtmlxMenu.addDhtmlxItem(deleteLimitMenuItem);
    }
  
    GrouperUiUtils.printToScreen("<?xml version=\"1.0\"?>\n" + 
        dhtmlxMenu.toXml(), HttpContentType.TEXT_XML, false, false);
  
    throw new ControllerDone();
  }
  
  /**
   * handle a click or select from the limit menu
   * @param httpServletRequest
   * @param httpServletResponse
   */
  @SuppressWarnings("unused")
  public void limitValueMenu(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
    
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
  
    GrouperSession grouperSession = null;
      
    String menuItemId = httpServletRequest.getParameter("menuItemId");
    String menuHtmlId = httpServletRequest.getParameter("menuIdOfMenuTarget");
  
    if (StringUtils.equals(menuItemId, "editLimitValueNewUi")) {
      new UiV2Permission().limitValueEdit(httpServletRequest, httpServletResponse);
    } else if (StringUtils.equals(menuItemId, "deleteLimitValueNewUi")) {
      new UiV2Permission().limitValueDelete(httpServletRequest, httpServletResponse);
    } else {
      throw new RuntimeException("Unexpected menu id: '" + menuItemId + "'");
    }
  }

  
  /**
   * add a value
   * @param newUi: true when this method is called from new ui false for lite ui
   */
  public void limitMenuAddValue(boolean newUi) {
    
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
    //lets see which subject we are dealing with:
    HttpServletRequest httpServletRequest = GrouperUiFilter.retrieveHttpServletRequest();
    String menuIdOfMenuTarget = httpServletRequest.getParameter("menuIdOfMenuTarget");
  
    if (StringUtils.isBlank(menuIdOfMenuTarget)) {
      throw new RuntimeException("Missing id of menu target");
    }
    if (!menuIdOfMenuTarget.startsWith("limitMenuButton_")) {
      throw new RuntimeException("Invalid id of menu target: '" + menuIdOfMenuTarget + "'");
    }
    String limitAssignId = GrouperUtil.prefixOrSuffix(menuIdOfMenuTarget, "limitMenuButton_", false);
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    AttributeAssign limitAssign = null;
    
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
      
      limitAssign = AttributeAssignFinder.findById(limitAssignId, true);
      
      PermissionUpdateRequestContainer permissionUpdateRequestContainer = PermissionUpdateRequestContainer.retrieveFromRequestOrCreate();
  
      AttributeAssignType limitAssignType = limitAssign.getAttributeAssignType();
  
      AttributeAssign underlyingPermissionAssignment = limitAssign.getOwnerAttributeAssign();
      AttributeAssignType underlyingPermissionAssignType = underlyingPermissionAssignment.getAttributeAssignType();
      
      //set the type to underlying, so that the labels are correct
      GuiAttributeAssign guiUnderlyingAttributeAssign = new GuiAttributeAssign();
      guiUnderlyingAttributeAssign.setAttributeAssign(underlyingPermissionAssignment);

      permissionUpdateRequestContainer.setGuiAttributeAssign(guiUnderlyingAttributeAssign);
      
      GuiAttributeAssign guiAttributeAssignAssign = new GuiAttributeAssign();
      guiAttributeAssignAssign.setAttributeAssign(limitAssign);

      permissionUpdateRequestContainer.setGuiAttributeAssignAssign(guiAttributeAssignAssign);
      permissionUpdateRequestContainer.setAttributeAssignType(underlyingPermissionAssignType);
      permissionUpdateRequestContainer.setAttributeAssignAssignType(limitAssignType);
        
      if (newUi) {
        guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#groupPermission", 
            "/WEB-INF/grouperUi2/permission/permissionLimitAddValue.jsp"));
        return;
      }
            
      guiResponseJs.addAction(GuiScreenAction.newDialogFromJsp(
        "/WEB-INF/grouperUi/templates/simplePermissionUpdate/simplePermissionLimitAddValue.jsp"));
  
    } catch (ControllerDone cd) {
      throw cd;
    } catch (NoSessionException nse) {
      throw nse;
    } catch (RuntimeException re) {
      throw new RuntimeException("Error addValue menu item: " + menuIdOfMenuTarget 
          + ", " + re.getMessage(), re);
    } finally {
      GrouperSession.stopQuietly(grouperSession); 
    }
  
  }
  
  
  
}
