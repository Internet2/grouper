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
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiPermissionEntry;
import edu.internet2.middleware.grouper.grouperUi.beans.attributeUpdate.AttributeUpdateRequestContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiResponseJs;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction;
import edu.internet2.middleware.grouper.grouperUi.beans.permissionUpdate.PermissionUpdateRequestContainer;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.permissions.PermissionEntry;
import edu.internet2.middleware.grouper.permissions.PermissionEntryUtils;
import edu.internet2.middleware.grouper.permissions.PermissionFinder;
import edu.internet2.middleware.grouper.permissions.PermissionHeuristic;
import edu.internet2.middleware.grouper.permissions.PermissionHeuristicBetter;
import edu.internet2.middleware.grouper.permissions.PermissionHeuristics;
import edu.internet2.middleware.grouper.permissions.PermissionEntry.PermissionType;
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
      this.assignmentMenuEditAssignment();
    } else if (StringUtils.equals(menuItemId, "analyzeAssignment")) {
      this.assignmentMenuAnalyzeAssignment();
    } else if (StringUtils.equals(menuItemId, "addLimit")) {
      this.assignmentMenuAddLimit();
    } else {
      throw new RuntimeException("Unexpected menu id: '" + menuItemId + "'");
    }
  
    
  }

  /**
   * add a limit on an assignment
   */
  public void assignmentMenuAddLimit() {
    
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
          guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message("simplePermissionUpdate.errorCantManageRole", false)));
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
          guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message("simplePermissionUpdate.errorCantEditAttributeDef", false)));
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
        guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message("simplePermissionUpdate.noImmediatePermissionFoundForLimit", false)));
        return;
      }

      GuiPermissionEntry guiPermissionEntry = new GuiPermissionEntry();
      guiPermissionEntry.setPermissionEntry(permissionEntry);
      guiPermissionEntry.setPermissionType(permissionType);
      
      permissionUpdateRequestContainer.setGuiPermissionEntry(guiPermissionEntry);

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
   */
  public void assignmentMenuEditAssignment() {
    
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
          guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message("simplePermissionUpdate.errorCantManageRole", false)));
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
          guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message("simplePermissionUpdate.errorCantEditAttributeDef", false)));
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
        guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message("simplePermissionUpdate.noImmediatePermissionFound", false)));
        return;
      }

      GuiPermissionEntry guiPermissionEntry = new GuiPermissionEntry();
      guiPermissionEntry.setPermissionEntry(permissionEntry);
      guiPermissionEntry.setPermissionType(permissionType);
      
      permissionUpdateRequestContainer.setGuiPermissionEntry(guiPermissionEntry);
            
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
   */
  public void assignmentMenuAnalyzeAssignment() {
    
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
          guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message("simplePermissionUpdate.errorCantManageRole", false)));
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
          guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message("simplePermissionUpdate.errorCantEditAttributeDef", false)));
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
        guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message("simplePermissionUpdate.analyzeNoPermissionFound", false)));
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
      
      guiPermissionEntry.setPermissionEntry(permissionEntry);
      guiPermissionEntry.setPermissionType(permissionType);

      List<GuiPermissionEntry> rawGuiPermissionEntries = new ArrayList<GuiPermissionEntry>();
      
      boolean isFirst = true;
      
      PermissionHeuristics firstHeuristics = permissionEntriesList.get(0).getPermissionHeuristics();
      
      String isBetterThan =  GrouperUiUtils.message("simplePermissionAssign.analyzeIsBetterThan", false); 
      
      for (PermissionEntry current : permissionEntriesList) {
        GuiPermissionEntry guiCurrent = new GuiPermissionEntry();
        guiCurrent.setPermissionEntry(current);
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
   * make the structure of the limit menu
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
      this.limitMenuAddValue();
    } else {
      throw new RuntimeException("Unexpected menu id: '" + menuItemId + "'");
    }
  }

  /**
   * add a value
   */
  public void limitMenuAddValue() {
    
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
