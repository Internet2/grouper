package edu.internet2.middleware.grouper.grouperUi.serviceLogic;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignType;
import edu.internet2.middleware.grouper.attr.finder.AttributeAssignFinder;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiAttributeAssign;
import edu.internet2.middleware.grouper.grouperUi.beans.attributeUpdate.AttributeUpdateRequestContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiResponseJs;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction;
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
 * menus and logic for menus of attribute assignments
 * @author mchyzer
 */
public class SimpleAttributeUpdateMenu {

  /**
   * make the structure of the attribute assignment
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void assignmentMenuStructure(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
    
    DhtmlxMenu dhtmlxMenu = new DhtmlxMenu();
    
    {
      DhtmlxMenuItem addValueMenuItem = new DhtmlxMenuItem();
      addValueMenuItem.setId("addValue");
      addValueMenuItem.setText(TagUtils.navResourceString("simpleAttributeAssign.assignMenuAddValue"));
      addValueMenuItem.setTooltip(TagUtils.navResourceString("simpleAttributeAssign.assignMenuAddValueTooltip"));
      dhtmlxMenu.addDhtmlxItem(addValueMenuItem);
    }    

    {
      DhtmlxMenuItem addMetadataAssignmentMenuItem = new DhtmlxMenuItem();
      addMetadataAssignmentMenuItem.setId("addMetadataAssignment");
      addMetadataAssignmentMenuItem.setText(TagUtils.navResourceString("simpleAttributeAssign.assignMenuAddMetadataAssignment"));
      addMetadataAssignmentMenuItem.setTooltip(TagUtils.navResourceString("simpleAttributeAssign.assignMenuAddMetadataAssignmentTooltip"));
      dhtmlxMenu.addDhtmlxItem(addMetadataAssignmentMenuItem);
    }    

    GrouperUiUtils.printToScreen("<?xml version=\"1.0\"?>\n" + 
        dhtmlxMenu.toXml(), HttpContentType.TEXT_XML, false, false);

    throw new ControllerDone();
  }

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

    if (StringUtils.equals(menuItemId, "addValue")) {
      this.assignmentMenuAddValue();
    } else if (StringUtils.equals(menuItemId, "addMetadataAssignment")) {
      this.assignmentMenuAddMetadataAssignment();
    } else {
      throw new RuntimeException("Unexpected menu id: '" + menuItemId + "'");
    }

    
  }

  /**
   * add an assignment on an assignment
   */
  public void assignmentMenuAddMetadataAssignment() {
    
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
    //lets see which subject we are dealing with:
    HttpServletRequest httpServletRequest = GrouperUiFilter.retrieveHttpServletRequest();
    String menuIdOfMenuTarget = httpServletRequest.getParameter("menuIdOfMenuTarget");

    if (StringUtils.isBlank(menuIdOfMenuTarget)) {
      throw new RuntimeException("Missing id of menu target");
    }
    if (!menuIdOfMenuTarget.startsWith("assignmentMenuButton_")) {
      throw new RuntimeException("Invalid id of menu target: '" + menuIdOfMenuTarget + "'");
    }
    String attributeAssignId = GrouperUtil.prefixOrSuffix(menuIdOfMenuTarget, "assignmentMenuButton_", false);
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    AttributeAssign attributeAssign = null;
    
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
      
      attributeAssign = AttributeAssignFinder.findById(attributeAssignId, true);
      
      if (attributeAssign.getAttributeAssignType().isAssignmentOnAssignment()) {
        guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message(
            "simpleAttributeUpdate.assignCantAddMetadataOnAssignmentOfAssignment", false)));
        return;
        
      }
      
      AttributeUpdateRequestContainer attributeUpdateRequestContainer = AttributeUpdateRequestContainer.retrieveFromRequestOrCreate();
      
      attributeUpdateRequestContainer.setAttributeAssignType(attributeAssign.getAttributeAssignType());
      
      GuiAttributeAssign guiAttributeAssign = new GuiAttributeAssign();
      guiAttributeAssign.setAttributeAssign(attributeAssign);
      
      attributeUpdateRequestContainer.setGuiAttributeAssign(guiAttributeAssign);
      
      //the combo boxes cant be shows on a dialog, so just replace the search results with this
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#attributeAssignAssignments",
        "/WEB-INF/grouperUi/templates/simpleAttributeUpdate/simpleAttributeAssignAddMetadataAssignment.jsp"));
  
      guiResponseJs.addAction(GuiScreenAction.newScript("guiScrollTo('#attributeAssignAssignments');"));

    } catch (ControllerDone cd) {
      throw cd;
    } catch (NoSessionException nse) {
      throw nse;
    } catch (RuntimeException re) {
      throw new RuntimeException("Error addMetadataAssignment menu item: " + menuIdOfMenuTarget 
          + ", " + re.getMessage(), re);
    } finally {
      GrouperSession.stopQuietly(grouperSession); 
    }
  
  }

  /**
   * add a value
   */
  public void assignmentMenuAddValue() {
    
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
    //lets see which subject we are dealing with:
    HttpServletRequest httpServletRequest = GrouperUiFilter.retrieveHttpServletRequest();
    String menuIdOfMenuTarget = httpServletRequest.getParameter("menuIdOfMenuTarget");

    if (StringUtils.isBlank(menuIdOfMenuTarget)) {
      throw new RuntimeException("Missing id of menu target");
    }
    if (!menuIdOfMenuTarget.startsWith("assignmentMenuButton_")) {
      throw new RuntimeException("Invalid id of menu target: '" + menuIdOfMenuTarget + "'");
    }
    String attributeAssignId = GrouperUtil.prefixOrSuffix(menuIdOfMenuTarget, "assignmentMenuButton_", false);
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    AttributeAssign attributeAssign = null;
    
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
      
      attributeAssign = AttributeAssignFinder.findById(attributeAssignId, true);
      
      AttributeUpdateRequestContainer attributeUpdateRequestContainer = AttributeUpdateRequestContainer.retrieveFromRequestOrCreate();

      AttributeAssignType attributeAssignType = attributeAssign.getAttributeAssignType();

      if (attributeAssignType.isAssignmentOnAssignment()) {
        AttributeAssign underlyingAssignment = attributeAssign.getOwnerAttributeAssign();
        AttributeAssignType underlyingAttributeAssignType = underlyingAssignment.getAttributeAssignType();
        
        //set the type to underlying, so that the labels are correct
        GuiAttributeAssign guiUnderlyingAttributeAssign = new GuiAttributeAssign();
        guiUnderlyingAttributeAssign.setAttributeAssign(underlyingAssignment);

        attributeUpdateRequestContainer.setGuiAttributeAssign(guiUnderlyingAttributeAssign);
        
        GuiAttributeAssign guiAttributeAssignAssign = new GuiAttributeAssign();
        guiAttributeAssignAssign.setAttributeAssign(attributeAssign);

        attributeUpdateRequestContainer.setGuiAttributeAssignAssign(guiAttributeAssignAssign);
        attributeUpdateRequestContainer.setAttributeAssignType(underlyingAttributeAssignType);
        attributeUpdateRequestContainer.setAttributeAssignAssignType(attributeAssignType);
        
      } else {
        attributeUpdateRequestContainer.setAttributeAssignType(attributeAssignType);
        
        GuiAttributeAssign guiAttributeAssign = new GuiAttributeAssign();
        guiAttributeAssign.setAttributeAssign(attributeAssign);

        attributeUpdateRequestContainer.setGuiAttributeAssign(guiAttributeAssign);
        
      }
            
      guiResponseJs.addAction(GuiScreenAction.newDialogFromJsp(
        "/WEB-INF/grouperUi/templates/simpleAttributeUpdate/simpleAttributeAssignAddValue.jsp"));
  
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
