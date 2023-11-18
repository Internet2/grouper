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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiResponseJs;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.ui.exceptions.ControllerDone;
import edu.internet2.middleware.grouper.ui.tags.TagUtils;
import edu.internet2.middleware.grouper.ui.tags.menu.DhtmlxMenu;
import edu.internet2.middleware.grouper.ui.tags.menu.DhtmlxMenuItem;
import edu.internet2.middleware.grouper.ui.util.GrouperUiUtils;
import edu.internet2.middleware.grouper.ui.util.HttpContentType;
import edu.internet2.middleware.subject.Subject;

/**
 * simple permission update menu
 * @author mchyzer
 */
public class MiscMenu {

  /**
   * handle a click or select from the assignment menu
   * @param httpServletRequest
   * @param httpServletResponse
   */
  @SuppressWarnings("unused")
  public void miscMenu(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
    
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
  
    GrouperSession grouperSession = null;
      
    String menuItemId = httpServletRequest.getParameter("menuItemId");
    String menuHtmlId = httpServletRequest.getParameter("menuIdOfMenuTarget");
  
    if (StringUtils.equals(menuItemId, "index")) {
      guiResponseJs.addAction(GuiScreenAction.newScript(
        "window.location = 'grouper.html?operation=Misc.index'"));
    } else if (StringUtils.equals(menuItemId, "admin")) {
      guiResponseJs.addAction(GuiScreenAction.newScript(
        "window.location = '../../populateAllGroups.do'"));
    } else if (StringUtils.equals(menuItemId, "new")) {
      guiResponseJs.addAction(GuiScreenAction.newScript(
        "window.location = '../../grouperUi/app/UiV2Main.index?operation=UiV2Main.indexMain'"));
    } else if (StringUtils.equals(menuItemId, "groupsAndRoles")) {
      guiResponseJs.addAction(GuiScreenAction.newScript(
        "window.location = 'grouper.html?operation=SimpleGroupUpdate.createEdit'"));
    } else if (StringUtils.equals(menuItemId, "groupMemberships")) {
      guiResponseJs.addAction(GuiScreenAction.newScript(
        "window.location = 'grouper.html?operation=SimpleMembershipUpdate.index'"));
      
    } else if (StringUtils.equals(menuItemId, "attributesAndPermissionsCreateEdit")) {
      guiResponseJs.addAction(GuiScreenAction.newScript(
        "window.location = 'grouper.html?operation=SimpleAttributeUpdate.createEdit'"));
    } else if (StringUtils.equals(menuItemId, "attributesAndPermissionsCreateEditNames")) {
      guiResponseJs.addAction(GuiScreenAction.newScript(
        "window.location = 'grouper.html?operation=SimpleAttributeNameUpdate.createEditAttributeNames'"));
    } else if (StringUtils.equals(menuItemId, "attributesAndPermissionsAssign")) {
      guiResponseJs.addAction(GuiScreenAction.newScript(
        "window.location = 'grouper.html?operation=SimpleAttributeUpdate.assignInit'"));
    } else if (StringUtils.equals(menuItemId, "attributesAndPermissionsPermAssign")) {
      guiResponseJs.addAction(GuiScreenAction.newScript(
        "window.location = 'grouper.html?operation=SimplePermissionUpdate.assignInit'"));
      
    } else {
      throw new RuntimeException("Unexpected menu id: '" + menuItemId + "'");
    }
    
    
  }

  /**
   * make the structure of the attribute assignment
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void miscMenuStructure(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
    
    DhtmlxMenu dhtmlxMenu = new DhtmlxMenu();
    
    {
      DhtmlxMenuItem indexMenuItem = new DhtmlxMenuItem();
      indexMenuItem.setId("index");
      indexMenuItem.setText(TagUtils.navResourceString("miscMenu.index"));
      indexMenuItem.setTooltip(TagUtils.navResourceString("miscMenu.indexTooltip"));
      dhtmlxMenu.addDhtmlxItem(indexMenuItem);
    }    
  
    {
      DhtmlxMenuItem adminMenuItem = new DhtmlxMenuItem();
      adminMenuItem.setId("admin");
      adminMenuItem.setText(TagUtils.navResourceString("miscMenu.admin"));
      adminMenuItem.setTooltip(TagUtils.navResourceString("miscMenu.adminTooltip"));
      dhtmlxMenu.addDhtmlxItem(adminMenuItem);
    }    
    
    {
      DhtmlxMenuItem adminMenuItem = new DhtmlxMenuItem();
      adminMenuItem.setId("new");
      adminMenuItem.setText(TagUtils.navResourceString("miscMenu.new"));
      adminMenuItem.setTooltip(TagUtils.navResourceString("miscMenu.newTooltip"));
      dhtmlxMenu.addDhtmlxItem(adminMenuItem);
    }    
    
    {
      DhtmlxMenuItem groupsAndRolesMenuItem = new DhtmlxMenuItem();
      groupsAndRolesMenuItem.setId("groupsAndRoles");
      groupsAndRolesMenuItem.setText(TagUtils.navResourceString("miscMenu.groupsAndRoles"));
      groupsAndRolesMenuItem.setTooltip(TagUtils.navResourceString("miscMenu.groupsAndRolesTooltip"));
      dhtmlxMenu.addDhtmlxItem(groupsAndRolesMenuItem);
    }    
    
    {
      DhtmlxMenuItem groupMembershipsMenuItem = new DhtmlxMenuItem();
      groupMembershipsMenuItem.setId("groupMemberships");
      groupMembershipsMenuItem.setText(TagUtils.navResourceString("miscMenu.groupMemberships"));
      groupMembershipsMenuItem.setTooltip(TagUtils.navResourceString("miscMenu.groupMembershipsTooltip"));
      dhtmlxMenu.addDhtmlxItem(groupMembershipsMenuItem);
    }    
    
    {
      DhtmlxMenuItem attributesPermissionsMenuItem = new DhtmlxMenuItem();
      attributesPermissionsMenuItem.setId("attributesPermissions");
      attributesPermissionsMenuItem.setText(TagUtils.navResourceString("miscMenu.attributesAndPermissions"));
      attributesPermissionsMenuItem.setTooltip(TagUtils.navResourceString("miscMenu.attributesAndPermissionsTooltip"));
      dhtmlxMenu.addDhtmlxItem(attributesPermissionsMenuItem);
      
      {
        DhtmlxMenuItem attributesPermissionsCreateMenuItem = new DhtmlxMenuItem();
        attributesPermissionsCreateMenuItem.setId("attributesAndPermissionsCreateEdit");
        attributesPermissionsCreateMenuItem.setText(TagUtils.navResourceString("miscMenu.attributesAndPermissionsCreateEdit"));
        attributesPermissionsCreateMenuItem.setTooltip(TagUtils.navResourceString("miscMenu.attributesAndPermissionsCreateEditTooltip"));
        attributesPermissionsMenuItem.addDhtmlxItem(attributesPermissionsCreateMenuItem);
        
      }
      {
        DhtmlxMenuItem attributesPermissionsCreateNamesMenuItem = new DhtmlxMenuItem();
        attributesPermissionsCreateNamesMenuItem.setId("attributesAndPermissionsCreateEditNames");
        attributesPermissionsCreateNamesMenuItem.setText(TagUtils.navResourceString("miscMenu.attributesAndPermissionsCreateEditNames"));
        attributesPermissionsCreateNamesMenuItem.setTooltip(TagUtils.navResourceString("miscMenu.attributesAndPermissionsCreateEditNamesTooltip"));
        attributesPermissionsMenuItem.addDhtmlxItem(attributesPermissionsCreateNamesMenuItem);
        
      }
      {
        DhtmlxMenuItem attributesPermissionsAssignMenuItem = new DhtmlxMenuItem();
        attributesPermissionsAssignMenuItem.setId("attributesAndPermissionsAssign");
        attributesPermissionsAssignMenuItem.setText(TagUtils.navResourceString("miscMenu.attributesAndPermissionsAssign"));
        attributesPermissionsAssignMenuItem.setTooltip(TagUtils.navResourceString("miscMenu.attributesAndPermissionsAssignTooltip"));
        attributesPermissionsMenuItem.addDhtmlxItem(attributesPermissionsAssignMenuItem);
        
      }
      {
        DhtmlxMenuItem attributesPermissionsPermAssignMenuItem = new DhtmlxMenuItem();
        attributesPermissionsPermAssignMenuItem.setId("attributesAndPermissionsPermAssign");
        attributesPermissionsPermAssignMenuItem.setText(TagUtils.navResourceString("miscMenu.attributesAndPermissionsPermAssign"));
        attributesPermissionsPermAssignMenuItem.setTooltip(TagUtils.navResourceString("miscMenu.attributesAndPermissionsPermAssignTooltip"));
        attributesPermissionsMenuItem.addDhtmlxItem(attributesPermissionsPermAssignMenuItem);
        
      }
      
    }    

    
    
    
    GrouperUiUtils.printToScreen("<?xml version=\"1.0\"?>\n" + 
        dhtmlxMenu.toXml(), HttpContentType.TEXT_XML, false, false);
  
    throw new ControllerDone();
  }
  
  
  
}
