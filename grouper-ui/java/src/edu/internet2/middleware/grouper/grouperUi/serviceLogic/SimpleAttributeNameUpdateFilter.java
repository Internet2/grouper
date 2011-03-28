package edu.internet2.middleware.grouper.grouperUi.serviceLogic;

import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignAction;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefFinder;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.grouperUi.beans.attributeNameUpdate.AttributeNameUpdateRequestContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.attributeUpdate.AttributeUpdateRequestContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiResponseJs;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.ui.exceptions.ControllerDone;
import edu.internet2.middleware.grouper.ui.tags.TagUtils;
import edu.internet2.middleware.grouper.ui.util.GrouperUiUtils;
import edu.internet2.middleware.grouper.ui.util.HttpContentType;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * filters on attribute update
 * @author mchyzer
 */
public class SimpleAttributeNameUpdateFilter {

  /** logger */
  private static final Log LOG = LogFactory.getLog(SimpleAttributeNameUpdateFilter.class);

  /**
   * filter attribute defs to pick one to edit
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void filterAttributeDefs(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {

    new SimpleAttributeUpdateFilter().filterAttributeDefs(httpServletRequest, httpServletResponse);

  }

  /**
   * filter attribute def names to pick one to edit
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void filterAttributeDefNames(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {

    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    String searchTerm = httpServletRequest.getParameter("mask");
  
    try {
      grouperSession = GrouperSession.start(loggedInSubject);
      
      String attributeDefIdParam = httpServletRequest.getParameter("simpleAttributeNameUpdatePickAttributeDef");
      
      //filter for hierarchies
      if (StringUtils.isBlank(attributeDefIdParam)) {
        attributeDefIdParam = httpServletRequest.getParameter("attributeDefId");
      }
      String attributeDefId = null;
      
      StringBuilder xmlBuilder = new StringBuilder(GrouperUiUtils.DHTMLX_OPTIONS_START);
      
      boolean foundError = false;
      if (!StringUtils.isBlank(attributeDefIdParam)) {
        
        try {
          AttributeDef attributeDef = AttributeDefFinder.findById(attributeDefIdParam, true);
          attributeDefId = attributeDef.getId();
        } catch (Exception e) {
          //this is ok, just not found
          LOG.debug(e.getMessage(), e);
          GrouperUiUtils.dhtmlxOptionAppend(xmlBuilder, "", 
              GrouperUiUtils.message("simpleAttributeNameUpdate.errorCantFindAttributeDef", false), "bullet_error.png");
          foundError = true;
        }
      }
      
      if (!foundError) {
        Set<AttributeDefName> attributeDefNames = null;
        
        QueryOptions queryOptions = null;
        
        if (StringUtils.defaultString(searchTerm).length() < 2) {
          GrouperUiUtils.dhtmlxOptionAppend(xmlBuilder, "", 
              GrouperUiUtils.message("simpleAttributeNameUpdate.errorNotEnoughChars", false), "bullet_error.png");
        } else {
          queryOptions = new QueryOptions()
            .paging(TagUtils.mediaResourceInt("simpleAttributeNameUpdate.attributeDefNameComboboxResultSize", 200), 1, true)
            .sortAsc("theAttributeDefName.displayNameDb");
          attributeDefNames = GrouperDAOFactory.getFactory().getAttributeDefName().findAllAttributeNamesSplitScopeSecure(
              searchTerm, grouperSession, attributeDefId, loggedInSubject, 
              GrouperUtil.toSet(AccessPrivilege.ADMIN, AccessPrivilege.UPDATE), queryOptions);
          
          if (GrouperUtil.length(attributeDefNames) == 0) {
            GrouperUiUtils.dhtmlxOptionAppend(xmlBuilder, "", 
                GrouperUiUtils.message("simpleAttributeNameUpdate.errorNoAttributeNamesFound", false), "bullet_error.png");
          }
        }
        
        for (AttributeDefName attributeDefName : GrouperUtil.nonNull(attributeDefNames)) {
    
          String value = attributeDefName.getId();
          String label = GrouperUiUtils.escapeHtml(attributeDefName.getDisplayName(), true);
          //application image
          String imageName = GrouperUiUtils.imageFromSubjectSource("g:isa");
    
          GrouperUiUtils.dhtmlxOptionAppend(xmlBuilder, value, label, imageName);
        }
    
        //add one more for more options if we didnt get them all
        if (queryOptions != null && queryOptions.getCount() != null 
            && attributeDefNames != null && queryOptions.getCount() > attributeDefNames.size()) {
          GrouperUiUtils.dhtmlxOptionAppend(xmlBuilder, "", 
              GrouperUiUtils.message("simpleAttributeNameUpdate.errorTooManyAttributeDefNames", false), "bullet_error.png");
        }
      }      
      
      xmlBuilder.append(GrouperUiUtils.DHTMLX_OPTIONS_END);
      
      GrouperUiUtils.printToScreen(xmlBuilder.toString(), HttpContentType.TEXT_XML, false, false);
  
    } catch (Exception se) {
      LOG.error("Error searching for attributeDefName: '" + searchTerm + "', " + se.getMessage(), se);
      
      //dont rethrow or the control will get confused
      StringBuilder xmlBuilder = new StringBuilder(GrouperUiUtils.DHTMLX_OPTIONS_START);
      GrouperUiUtils.dhtmlxOptionAppend(xmlBuilder, null, 
          GrouperUiUtils.escapeHtml("Error searching for attribute names: " + searchTerm + ", " + se.getMessage(), true), null);
      xmlBuilder.append(GrouperUiUtils.DHTMLX_OPTIONS_END);
      GrouperUiUtils.printToScreen(xmlBuilder.toString(), HttpContentType.TEXT_XML, false, false);
    } finally {
      GrouperSession.stopQuietly(grouperSession); 
    }
  
    //dont print the regular JSON
    throw new ControllerDone();


  }

  /**
   * edit an attribute def name
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void editAttributeDefNamesButton(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
  
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
  
    String attributeNameId = httpServletRequest.getParameter("simpleAttributeNameUpdatePickAttributeDefName");
  
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
    if (StringUtils.isBlank(attributeNameId)) {
      guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message("simpleAttributeNameUpdate.errorCantEditAttributeDefName", false)));
      return;
    }
    
    GrouperSession grouperSession = null;
  
    AttributeDefName attributeDefName = null;
    
    try {
      grouperSession = GrouperSession.start(loggedInSubject);
      
      try {
      
        attributeDefName = AttributeDefNameFinder.findById(attributeNameId, true);
      } catch (Exception e) {
        LOG.info("Error searching for attribute def name: " + attributeNameId, e);
        guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message("simpleAttributeNameUpdate.errorCantEditAttributeDefName", false)));
        return;
        
      }  
      
    } finally {
      GrouperSession.stopQuietly(grouperSession); 
    }
    editAttributeDefNamesHelper(httpServletRequest, httpServletResponse, attributeDefName, true);
  }

  /**
   * new attribute def name
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void newAttributeDefNameButton(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
  
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
  
    AttributeNameUpdateRequestContainer attributeNameUpdateRequestContainer = AttributeNameUpdateRequestContainer.retrieveFromRequestOrCreate();
  
    GrouperSession grouperSession = null;
  
    try {
      grouperSession = GrouperSession.start(loggedInSubject);
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      
      String attributeDefId = httpServletRequest.getParameter("simpleAttributeNameUpdatePickAttributeDef");

      if (!StringUtils.isBlank(attributeDefId)) {
        try {
          AttributeDef attributeDef = AttributeDefFinder.findById(attributeDefId, true);
          
          //prepopulate the attribute def combobox
          attributeNameUpdateRequestContainer.setAttributeDefForFilter(attributeDef);
          attributeNameUpdateRequestContainer.setAttributeDef(attributeDef);
          
        } catch (Exception e) {
          LOG.warn("Attribute def not found: " + attributeDefId);
          
          String errorMessage = TagUtils.navResourceString("simpleAttributeNameUpdate.attributeDefNotFound");
          errorMessage = StringUtils.replace(errorMessage, "{0}", GrouperUiUtils.escapeHtml(attributeDefId, true));
          guiResponseJs.addAction(GuiScreenAction.newAlert(errorMessage));

        }
      }
      
      
      AttributeDefName attributeDefName = new AttributeDefName();
      attributeNameUpdateRequestContainer.setAttributeDefNameToEdit(attributeDefName);
      attributeNameUpdateRequestContainer.setCreate(true);
      
  
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#attributeNameEditPanel", 
        "/WEB-INF/grouperUi/templates/simpleAttributeNameUpdate/attributeNameEditPanel.jsp"));
  
      
    } finally {
      GrouperSession.stopQuietly(grouperSession); 
    }
  
  
  }

  /**
   * edit an attribute def name
   * @param httpServletRequest
   * @param httpServletResponse
   * @param attributeDefName 
   * @param checkSecurity 
   * @return true if ok, false if error
   */
  public boolean editAttributeDefNamesHelper(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, AttributeDefName attributeDefName, boolean checkSecurity) {
  
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
  
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
    AttributeNameUpdateRequestContainer attributeNameUpdateRequestContainer = AttributeNameUpdateRequestContainer.retrieveFromRequestOrCreate();
  
    GrouperSession grouperSession = null;
  
    try {
      grouperSession = GrouperSession.start(loggedInSubject);
      
      AttributeDef attributeDef = attributeDefName.getAttributeDef();
      if (checkSecurity && !attributeDef.getPrivilegeDelegate().canAttrAdmin(loggedInSubject)) {
        LOG.error("Subject " + GrouperUtil.subjectToString(loggedInSubject) + " cannot admin attribute definition: " + attributeDefName.getName());
        guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message("simpleAttributeNameUpdate.errorCantEditAttributeDef", false)));
        return false;
      }
      
      attributeNameUpdateRequestContainer.setAttributeDefNameToEdit(attributeDefName);
      
      attributeNameUpdateRequestContainer.setAttributeDef(attributeDef);

      
      attributeNameUpdateRequestContainer.setCreate(false);
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#attributeNameEditPanel", 
        "/WEB-INF/grouperUi/templates/simpleAttributeNameUpdate/attributeNameEditPanel.jsp"));
  
      
    } finally {
      GrouperSession.stopQuietly(grouperSession); 
    }
    return true;
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
   * add an attribute name that implies this
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void addAttributeNameThatImplies(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    AttributeNameUpdateRequestContainer attributeNameUpdateRequestContainer = AttributeNameUpdateRequestContainer.retrieveFromRequestOrCreate();
  
    GrouperSession grouperSession = null;
  
    try {
      grouperSession = GrouperSession.start(loggedInSubject);
      
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
      AttributeDefName attributeDefName = null;
      AttributeDefName attributeDefNameThatImplies = null;
      
      String uuid = httpServletRequest.getParameter("attributeDefNameToEditId");
      
      if (StringUtils.isBlank(uuid)) {
        throw new RuntimeException("Why is uuid blank????");
      }
  
      String attributeDefNameIdThatImplies = httpServletRequest.getParameter("attributeDefNameIdForHierarchy");
      
      if (StringUtils.isBlank(attributeDefNameIdThatImplies)) {
        throw new RuntimeException("Why is attributeDefNameIdThatImplies blank????");
      }
      
      //if editing, then this must be there, or it has been tampered with
      try {
        attributeDefName = AttributeDefNameFinder.findById(uuid, true);
      } catch (Exception e) {
        LOG.info("Error searching for attribute def: " + uuid, e);
        guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message("simpleAttributeNameUpdate.errorCantEditAttributeDefName", false)));
        return;
        
      }
      
      if (!attributeDefName.getAttributeDef().getPrivilegeDelegate().canAttrAdmin(loggedInSubject)) {
        LOG.error("Subject " + GrouperUtil.subjectToString(loggedInSubject) + " cannot admin attribute definition: " + attributeDefName.getAttributeDef().getName());
        guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message("simpleAttributeNameUpdate.errorCantEditAttributeDefName", false)));
        return;
      }
      
      //if editing, then this must be there, or it has been tampered with
      try {
        attributeDefNameThatImplies = AttributeDefNameFinder.findById(attributeDefNameIdThatImplies, true);
      } catch (Exception e) {
        LOG.info("Error searching for attribute def: " + attributeDefNameIdThatImplies, e);
        guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message("simpleAttributeNameUpdate.errorCantEditAttributeDefNameHierarchy", false)));
        return;
        
      }
      
      if (!attributeDefNameThatImplies.getAttributeDef().getPrivilegeDelegate().canAttrAdmin(loggedInSubject)) {
        LOG.error("Subject " + GrouperUtil.subjectToString(loggedInSubject) + " cannot admin attribute definition: " + attributeDefNameThatImplies.getAttributeDef().getName());
        guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message("simpleAttributeNameUpdate.errorCantEditAttributeDefNameHierarchy", false)));
        return;
      }
      
      boolean assigned = attributeDefNameThatImplies.getAttributeDefNameSetDelegate().addToAttributeDefNameSet(attributeDefName);
      
      if (assigned) {
        guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message("simpleAttributeNameUpdate.successAddImpliedByAction", false)));
      }
  
//      setupEditActionPanel(attributeNameUpdateRequestContainer, guiResponseJs, attributeDefName,
//          action);
      
    } finally {
      GrouperSession.stopQuietly(grouperSession); 
    }

  }
  
  
  
}


