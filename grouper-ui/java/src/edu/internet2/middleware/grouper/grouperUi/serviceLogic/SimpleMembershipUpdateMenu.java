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
/**
 * @author mchyzer
 * $Id: SimpleMembershipUpdateMenu.java,v 1.6 2009-11-13 07:32:39 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.grouperUi.serviceLogic;

import java.util.LinkedHashSet;
import java.util.MissingResourceException;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiMember;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiSubject;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiHideShow;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiResponseJs;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction;
import edu.internet2.middleware.grouper.grouperUi.beans.simpleMembershipUpdate.SimpleMembershipUpdateContainer;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.ui.UIGroupPrivilegeResolver;
import edu.internet2.middleware.grouper.ui.UIGroupPrivilegeResolverFactory;
import edu.internet2.middleware.grouper.ui.exceptions.ControllerDone;
import edu.internet2.middleware.grouper.ui.exceptions.NoSessionException;
import edu.internet2.middleware.grouper.ui.tags.TagUtils;
import edu.internet2.middleware.grouper.ui.tags.menu.DhtmlxMenu;
import edu.internet2.middleware.grouper.ui.tags.menu.DhtmlxMenuItem;
import edu.internet2.middleware.grouper.ui.util.GrouperUiConfig;
import edu.internet2.middleware.grouper.ui.util.GrouperUiUtils;
import edu.internet2.middleware.grouper.ui.util.HttpContentType;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;


/**
 * ajax methods for the menus in the simple membership update module
 */
public class SimpleMembershipUpdateMenu {

  /**
     * handle a click or select from the advanced menu
     * @param httpServletRequest
     * @param httpServletResponse
     */
    public void advancedMenu(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      
      final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();

      GrouperSession grouperSession = null;

      Group group = null;

      try {

        grouperSession = GrouperSession.start(loggedInSubject);
        
        //make sure we are on the right group
        group = new SimpleMembershipUpdate().retrieveGroup(grouperSession);      
        
      } catch (NoSessionException se) {
        throw se;
      } catch (ControllerDone cd) {
        throw cd;
      } catch (Exception se) {
        throw new RuntimeException("Error advanced menu: " + group + ", " + se.getMessage(), se);
      } finally {
        GrouperSession.stopQuietly(grouperSession); 
      }
      
      
      String menuItemId = httpServletRequest.getParameter("menuItemId");
      String menuEvent = httpServletRequest.getParameter("menuEvent");
      boolean isOnClick = StringUtils.equals("onClick", menuEvent);
      //String menuHtmlId = httpServletRequest.getParameter("menuHtmlId");
      //String menuRadioGroup = httpServletRequest.getParameter("menuRadioGroup");
      String menuCheckboxChecked  = httpServletRequest.getParameter("menuCheckboxChecked");
  
  //    guiResponseJs.addAction(GuiScreenAction.newAlert("Menu action: menuItemId: " + menuItemId
  //        + ", menuHtmlId: " + menuHtmlId 
  //        + ", menuRadioGroup: " 
  //        + menuRadioGroup + ", menuCheckboxChecked: " + menuCheckboxChecked));
      
        

      if (StringUtils.equals(menuItemId, "inviteLink")) {
        guiResponseJs.addAction(GuiScreenAction.newScript(
            "window.location = 'grouper.html?operation=InviteExternalSubjects.inviteExternalSubject&groupId=" 
            + group.getUuid() + "'"));
      } else if (StringUtils.equals(menuItemId, "showGroupDetails")) {
        if (!isOnClick) {
          if (GrouperUtil.booleanValue(menuCheckboxChecked)) {
            guiResponseJs.addAction(GuiScreenAction.newHideShowNameToShow("simpleMembershipUpdateGroupDetails"));
          } else {
            guiResponseJs.addAction(GuiScreenAction.newHideShowNameToHide("simpleMembershipUpdateGroupDetails"));
          }
        }
      } else if (StringUtils.equals(menuItemId, "multiDelete")) {
        if (!isOnClick) {
          if (GrouperUtil.booleanValue(menuCheckboxChecked)) {
            guiResponseJs.addAction(GuiScreenAction.newHideShowNameToShow("simpleMembershipUpdateDeleteMultiple"));
          } else {
            guiResponseJs.addAction(GuiScreenAction.newHideShowNameToHide("simpleMembershipUpdateDeleteMultiple"));
          }
        }
      } else if (StringUtils.equals(menuItemId, "showMemberFilter")) {
        if (!isOnClick) {
          if (GrouperUtil.booleanValue(menuCheckboxChecked)) {
            guiResponseJs.addAction(GuiScreenAction.newHideShowNameToShow("simpleMembershipUpdateMemberFilter"));
          } else {
            guiResponseJs.addAction(GuiScreenAction.newHideShowNameToHide("simpleMembershipUpdateMemberFilter"));
          }
        }
      } else if (StringUtils.equals(menuItemId, "exportSubjectIds")) {
        guiResponseJs.addAction(GuiScreenAction.newAlertFromJsp(
            "/WEB-INF/grouperUi/templates/simpleMembershipUpdate/simpleMembershipUpdateExportSubjectIds.jsp"));
      } else if (StringUtils.equals(menuItemId, "exportAll")) {
        guiResponseJs.addAction(GuiScreenAction.newAlertFromJsp(
            "/WEB-INF/grouperUi/templates/simpleMembershipUpdate/simpleMembershipUpdateExportAll.jsp"));
        
      } else if (StringUtils.equals(menuItemId, "import")) {
        guiResponseJs.addAction(GuiScreenAction.newDialogFromJsp(
            "/WEB-INF/grouperUi/templates/simpleMembershipUpdate/simpleMembershipUpdateImport.jsp"));
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
    
    //get the text to add to html if showing multi delete
    GuiHideShow showMultiDelete = GuiHideShow.retrieveHideShow("simpleMembershipUpdateDeleteMultiple", true);
  
    //get the text to add to html if showing member filter
    GuiHideShow showMemberFilter = GuiHideShow.retrieveHideShow("simpleMembershipUpdateMemberFilter", true);
  
    SimpleMembershipUpdateContainer simpleMembershipUpdateContainer = SimpleMembershipUpdateContainer.retrieveFromSession();
    
    DhtmlxMenu dhtmlxMenu = new DhtmlxMenu();

    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    Group group = null;
    GrouperSession grouperSession = null;
    boolean canInviteOthers = false;
    try {

      grouperSession = GrouperSession.start(loggedInSubject);
  
      group = new SimpleMembershipUpdate().retrieveGroup(grouperSession);

      UIGroupPrivilegeResolver resolver = 
        UIGroupPrivilegeResolverFactory.getInstance(grouperSession, 
            GrouperUiFilter.retrieveSessionMediaResourceBundle(), 
                                                group, grouperSession.getSubject());
      canInviteOthers = resolver.canInviteExternalPeople();
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }

    {
      DhtmlxMenuItem multiDeleteMenuItem = new DhtmlxMenuItem();
      multiDeleteMenuItem.setId("multiDelete");
      multiDeleteMenuItem.setType("checkbox");
      if (showMultiDelete.isShowing()) {
        multiDeleteMenuItem.setChecked(showMultiDelete.isShowing());
      }
      multiDeleteMenuItem.setText(simpleMembershipUpdateContainer.getText().getAdvancedMenuDeleteMultiple());
      multiDeleteMenuItem.setTooltip(simpleMembershipUpdateContainer.getText().getAdvancedMenuDeleteMultipleTooltip());
      dhtmlxMenu.addDhtmlxItem(multiDeleteMenuItem);
    }    
    
    {
      //see if we can invite
      if (canInviteOthers && GrouperUiConfig.retrieveConfig().propertyValueBoolean("inviteExternalPeople.link-from-lite-ui", false)) {
        DhtmlxMenuItem memberInviteMenuItem = new DhtmlxMenuItem();
        memberInviteMenuItem.setId("inviteLink");
        memberInviteMenuItem.setText(TagUtils.navResourceString("ui-lite.invite-menu"));
        memberInviteMenuItem.setTooltip(TagUtils.navResourceString("ui-lite.invite-menuTooltip"));
        //memberInviteMenuItem.setHref("grouper.html?operation=InviteExternalSubjects.inviteExternalSubject&groupId=" + group.getUuid());
        //memberInviteMenuItem.setHref("http://localhost:8091/grouper/grouperUi/appHtml/grouper.html?operation=InviteExternalSubjects.inviteExternalSubject&groupId=0e0262d9be924774914052c12f0e7fd2");
        dhtmlxMenu.addDhtmlxItem(memberInviteMenuItem);
      }
    }    

    {
      DhtmlxMenuItem groupDetailsMenuItem = new DhtmlxMenuItem();
      groupDetailsMenuItem.setId("showGroupDetails");
      groupDetailsMenuItem.setType("checkbox");
      if (showGroupDetails.isShowing()) {
        groupDetailsMenuItem.setChecked(showGroupDetails.isShowing());
      }
      groupDetailsMenuItem.setText(simpleMembershipUpdateContainer.getText().getAdvancedMenuShowGroupDetails());
      groupDetailsMenuItem.setTooltip(simpleMembershipUpdateContainer.getText().getAdvancedMenuShowGroupDetailsTooltip());
      dhtmlxMenu.addDhtmlxItem(groupDetailsMenuItem);
    }    

    {
      DhtmlxMenuItem memberFilterMenuItem = new DhtmlxMenuItem();
      memberFilterMenuItem.setId("showMemberFilter");
      memberFilterMenuItem.setType("checkbox");
      if (showMemberFilter.isShowing()) {
        memberFilterMenuItem.setChecked(showMemberFilter.isShowing());
      }
      memberFilterMenuItem.setText(simpleMembershipUpdateContainer.getText().getAdvancedMenuShowMemberFilter());
      memberFilterMenuItem.setTooltip(simpleMembershipUpdateContainer.getText().getAdvancedMenuShowMemberFilterTooltip());
      dhtmlxMenu.addDhtmlxItem(memberFilterMenuItem);
    }    

    DhtmlxMenuItem importExportMenuItem = new DhtmlxMenuItem();
    importExportMenuItem.setId("importExport");
    importExportMenuItem.setText(simpleMembershipUpdateContainer.getText().getAdvancedMenuImportExport());
    importExportMenuItem.setTooltip(simpleMembershipUpdateContainer.getText().getAdvancedMenuImportExportTooltip());
    dhtmlxMenu.addDhtmlxItem(importExportMenuItem);

    DhtmlxMenuItem exportMenuItem = new DhtmlxMenuItem();
    exportMenuItem.setId("export");
    exportMenuItem.setText(simpleMembershipUpdateContainer.getText().getAdvancedMenuExport());
    exportMenuItem.setTooltip(simpleMembershipUpdateContainer.getText().getAdvancedMenuExportTooltip());
    importExportMenuItem.addDhtmlxItem(exportMenuItem);
    
    {
      DhtmlxMenuItem exportSubjectIdsMenuItem = new DhtmlxMenuItem();
      exportSubjectIdsMenuItem.setId("exportSubjectIds");
      exportSubjectIdsMenuItem.setText(simpleMembershipUpdateContainer.getText().getAdvancedMenuExportSubjectIds());
      exportSubjectIdsMenuItem.setTooltip(simpleMembershipUpdateContainer.getText().getAdvancedMenuExportSubjectIdsTooltip());
      exportMenuItem.addDhtmlxItem(exportSubjectIdsMenuItem);
    }    
    
    {
      DhtmlxMenuItem exportAllMenuItem = new DhtmlxMenuItem();
      exportAllMenuItem.setId("exportAll");
      exportAllMenuItem.setText(simpleMembershipUpdateContainer.getText().getAdvancedMenuExportAll());
      exportAllMenuItem.setTooltip(simpleMembershipUpdateContainer.getText().getAdvancedMenuExportAllTooltip());
      exportMenuItem.addDhtmlxItem(exportAllMenuItem);
    }    
    
    {
      DhtmlxMenuItem importMenuItem = new DhtmlxMenuItem();
      importMenuItem.setId("import");
      importMenuItem.setText(simpleMembershipUpdateContainer.getText().getAdvancedMenuImport());
      importMenuItem.setTooltip(simpleMembershipUpdateContainer.getText().getAdvancedMenuImportTooltip());
      importExportMenuItem.addDhtmlxItem(importMenuItem);
    }    

    GrouperUiUtils.printToScreen("<?xml version=\"1.0\"?>\n" + dhtmlxMenu.toXml(), 
        HttpContentType.TEXT_XML, false, false);
    throw new ControllerDone();

  }

  /**
     * handle a click or select from the member menu
     * @param httpServletRequest
     * @param httpServletResponse
     */
    @SuppressWarnings("unused")
    public void memberMenu(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
      
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();

      final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();

      GrouperSession grouperSession = null;

      Group group = null;

      try {

        grouperSession = GrouperSession.start(loggedInSubject);
        
        //make sure we are on the right group
        group = new SimpleMembershipUpdate().retrieveGroup(grouperSession);      
        
      } catch (NoSessionException se) {
        throw se;
      } catch (ControllerDone cd) {
        throw cd;
      } catch (Exception se) {
        throw new RuntimeException("Error member menu: " + group + ", " + se.getMessage(), se);
      } finally {
        GrouperSession.stopQuietly(grouperSession); 
      }
        
      String menuItemId = httpServletRequest.getParameter("menuItemId");
      String menuHtmlId = httpServletRequest.getParameter("menuHtmlId");
      //String menuRadioGroup = httpServletRequest.getParameter("menuRadioGroup");
      //String menuCheckboxChecked  = httpServletRequest.getParameter("menuCheckboxChecked");
  
      menuHtmlId = httpServletRequest.getParameter("menuIdOfMenuTarget");
      if (StringUtils.equals(menuItemId, "memberDetails")) {
  //        guiResponseJs.addAction(GuiScreenAction.newAlert("Menu action: menuItemId: " + menuItemId
  //            + ", menuHtmlId: " + menuHtmlId));
        this.memberMenuSubjectDetails();
      } else if (StringUtils.equals(menuItemId, "enabledDisabled")) {
        this.memberMenuEnabledDisabled();
      } else {
        throw new RuntimeException("Unexpected menu id: '" + menuItemId + "'");
      }

      
    }

  /**
   * make the structure of the advanced menu
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void memberMenuStructure(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
    
    DhtmlxMenu dhtmlxMenu = new DhtmlxMenu();

    SimpleMembershipUpdateContainer simpleMembershipUpdateContainer = SimpleMembershipUpdateContainer.retrieveFromSession();
    
    {
      DhtmlxMenuItem memberDetailsMenuItem = new DhtmlxMenuItem();
      memberDetailsMenuItem.setId("memberDetails");
      memberDetailsMenuItem.setText(simpleMembershipUpdateContainer.getText().getMemberMenuDetailsLabel());
      memberDetailsMenuItem.setTooltip(simpleMembershipUpdateContainer.getText().getMemberMenuDetailsTooltip());
      dhtmlxMenu.addDhtmlxItem(memberDetailsMenuItem);
    }    

    {
      DhtmlxMenuItem memberEnabledMenuItem = new DhtmlxMenuItem();
      memberEnabledMenuItem.setId("enabledDisabled");
      memberEnabledMenuItem.setText(simpleMembershipUpdateContainer.getText().getMemberMenuEnabledDisabled());
      memberEnabledMenuItem.setTooltip(simpleMembershipUpdateContainer.getText().getMemberMenuEnabledDisabledTooltip());
      dhtmlxMenu.addDhtmlxItem(memberEnabledMenuItem);
    }    

    GrouperUiUtils.printToScreen("<?xml version=\"1.0\"?>\n" + 
        dhtmlxMenu.toXml(), HttpContentType.TEXT_XML, false, false);

    throw new ControllerDone();
  }

  /**
   * show subject details
   */
  @SuppressWarnings("unchecked")
  public void memberMenuSubjectDetails() {
    
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
    //lets see which subject we are dealing with:
    HttpServletRequest httpServletRequest = GrouperUiFilter.retrieveHttpServletRequest();
    String menuIdOfMenuTarget = httpServletRequest.getParameter("menuIdOfMenuTarget");
    if (StringUtils.isBlank(menuIdOfMenuTarget)) {
      throw new RuntimeException("Missing id of menu target");
    }
    if (!menuIdOfMenuTarget.startsWith("memberMenuButton_")) {
      throw new RuntimeException("Invalid id of menu target: '" + menuIdOfMenuTarget + "'");
    }
    String memberId = GrouperUtil.prefixOrSuffix(menuIdOfMenuTarget, "memberMenuButton_", false);
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      Member member = MemberFinder.findByUuid(grouperSession, memberId, true);
      Subject subject = member.getSubject();
      String order = null;
      
      try {
        order = GrouperUiConfig.retrieveConfig().propertyValueString( 
            "subject.attributes.order." + subject.getSource().getId());
      } catch (MissingResourceException mre) {
        //thats ok, go with default
      }
      
      if (StringUtils.isBlank(order)) {
        Set<String> attributeNames = new LinkedHashSet<String>();
        attributeNames.add("screenLabel");
        attributeNames.addAll(GrouperUtil.nonNull(subject.getAttributes()).keySet());
        
        //lets add subjectId, typeName, sourceId, sourceName, memberId
        attributeNames.add("subjectId");
        attributeNames.add("name");
        attributeNames.add("description");
        attributeNames.add("typeName");
        attributeNames.add("sourceId");
        attributeNames.add("sourceName");
        
        order = GrouperUtil.join(attributeNames.iterator(), ',');
      }
  
      String[] attrNames = GrouperUtil.splitTrim(order, ",");
      
      SimpleMembershipUpdateContainer simpleMembershipUpdateContainer = SimpleMembershipUpdateContainer.retrieveFromSession();
      simpleMembershipUpdateContainer.setSubjectForDetails(subject);
      simpleMembershipUpdateContainer.getSubjectDetails().clear();
  
      //lookup each attribute
      for (String attrName: attrNames) {
        
        //sometimes group have blank attributes???
        if (StringUtils.isBlank(attrName)) {
          continue;
        }
        String attributeValue = GuiSubject.attributeValue(subject, attrName);
        simpleMembershipUpdateContainer.getSubjectDetails().put(attrName, attributeValue);
      }
      guiResponseJs.addAction(GuiScreenAction.newAlertFromJsp(
        "/WEB-INF/grouperUi/templates/simpleMembershipUpdate/simpleMembershipUpdateSubjectDetails.jsp"));
  
    } catch (ControllerDone cd) {
      throw cd;
    } catch (NoSessionException nse) {
      throw nse;
    } catch (Exception se) {
      throw new RuntimeException("Error listing member details: " + menuIdOfMenuTarget 
          + ", " + se.getMessage(), se);
    } finally {
      GrouperSession.stopQuietly(grouperSession); 
    }
  
  }

  /**
   * edit the enabled disabled
   */
  @SuppressWarnings("unchecked")
  public void memberMenuEnabledDisabled() {
    
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
    //lets see which subject we are dealing with:
    HttpServletRequest httpServletRequest = GrouperUiFilter.retrieveHttpServletRequest();
    String menuIdOfMenuTarget = httpServletRequest.getParameter("menuIdOfMenuTarget");
    if (StringUtils.isBlank(menuIdOfMenuTarget)) {
      throw new RuntimeException("Missing id of menu target");
    }
    if (!menuIdOfMenuTarget.startsWith("memberMenuButton_")) {
      throw new RuntimeException("Invalid id of menu target: '" + menuIdOfMenuTarget + "'");
    }
    String memberId = GrouperUtil.prefixOrSuffix(menuIdOfMenuTarget, "memberMenuButton_", false);
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    Group group = null;
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      Member member = MemberFinder.findByUuid(grouperSession, memberId, true);
      
      group = new SimpleMembershipUpdate().retrieveGroup(grouperSession);  
      
      Membership membership = group.getImmediateMembership(Group.getDefaultList(), member, false, true);
      
      SimpleMembershipUpdateContainer simpleMembershipUpdateContainer = SimpleMembershipUpdateContainer.retrieveFromSession();
      
      GuiMember guiMember = new GuiMember(member);
      simpleMembershipUpdateContainer.setEnabledDisabledMember(guiMember);
      
      guiMember.setMembership(membership);
      
      guiResponseJs.addAction(GuiScreenAction.newDialogFromJsp(
        "/WEB-INF/grouperUi/templates/simpleMembershipUpdate/simpleMembershipUpdateEnabledDisabled.jsp"));
  
    } catch (ControllerDone cd) {
      throw cd;
    } catch (NoSessionException nse) {
      throw nse;
    } catch (Exception se) {
      throw new RuntimeException("Error listing member details: " + menuIdOfMenuTarget 
          + ", " + se.getMessage(), se);
    } finally {
      GrouperSession.stopQuietly(grouperSession); 
    }
  
  }
}
