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
import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.AttributeDefType;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefFinder;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.grouperUi.beans.attributeNameUpdate.AttributeNameUpdateRequestContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiResponseJs;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.ui.util.GrouperUiUtils;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * ajax method logic for the attribute definition name screen
 * @author mchyzer
 *
 */
public class SimpleAttributeNameUpdate {

  
  /**
   * hierarchies button was pressed on the attribute name edit panel
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void attributeNameEditPanelHierarchies(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
  
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
  
    AttributeNameUpdateRequestContainer attributeNameUpdateRequestContainer = AttributeNameUpdateRequestContainer.retrieveFromRequestOrCreate();

    GrouperSession grouperSession = null;

    try {
      grouperSession = GrouperSession.start(loggedInSubject);
      
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
      AttributeDefName attributeDefName = null;
      
      String uuid = httpServletRequest.getParameter("attributeDefNameToEditId");
      
      if (StringUtils.isBlank(uuid)) {
        throw new RuntimeException("Why is uuid blank????");
      }
  
      //if editing, then this must be there, or it has been tampered with
      try {
        attributeDefName = AttributeDefNameFinder.findById(uuid, true);
      } catch (Exception e) {
        LOG.info("Error searching for attribute def name: " + uuid, e);
        guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message("simpleAttributeNameUpdate.errorCantEditAttributeDefName", false)));
        return;
      }
      
      if (!attributeDefName.getAttributeDef().getPrivilegeDelegate().canAttrAdmin(loggedInSubject)) {
        LOG.error("Subject " + GrouperUtil.subjectToString(loggedInSubject) + " cannot admin attribute definition: " + attributeDefName.getName());
        guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message("simpleAttributeUpdate.errorCantEditAttributeDef", false)));
        return;
      }
      if (attributeDefName.getAttributeDef().getAttributeDefType() != AttributeDefType.perm) {
        LOG.error("Why is this not a permission? " + attributeDefName);
        guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message("simpleAttributeNameUpdate.errorNotPermission", false)));
        return;
      }
      
      attributeNameUpdateRequestContainer.setAttributeDefNameToEdit(attributeDefName);
      
      {
        Set<AttributeDefName> attributeDefNamesThatImplyThis = attributeDefName.getAttributeDefNameSetDelegate().getAttributeDefNamesThatImplyThis();
        attributeNameUpdateRequestContainer.setAttributeDefNamesThatImplyThis(attributeDefNamesThatImplyThis);
      }
      {
        Set<AttributeDefName> attributeDefNamesThatImplyThisImmediate = attributeDefName.getAttributeDefNameSetDelegate().getAttributeDefNamesThatImplyThisImmediate();
        attributeNameUpdateRequestContainer.setAttributeDefNamesThatImplyThisImmediate(attributeDefNamesThatImplyThisImmediate);
      }
      {
        Set<AttributeDefName> attributeDefNamesImpliedByThis = attributeDefName.getAttributeDefNameSetDelegate().getAttributeDefNamesImpliedByThis();
        attributeNameUpdateRequestContainer.setAttributeDefNamesImpliedByThis(attributeDefNamesImpliedByThis);
      }
      {
        Set<AttributeDefName> attributeDefNamesImpliedByThisImmediate = attributeDefName.getAttributeDefNameSetDelegate().getAttributeDefNamesImpliedByThisImmediate();
        attributeNameUpdateRequestContainer.setAttributeDefNamesImpliedByThisImmediate(attributeDefNamesImpliedByThisImmediate);
      }
      
      
      //set the actions panel
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#attributeNameHierarchiesPanel", 
        "/WEB-INF/grouperUi/templates/simpleAttributeNameUpdate/attributeNameHierarchies.jsp"));
      
      guiResponseJs.addAction(GuiScreenAction.newScript("guiScrollTo('#attributeNameHierarchiesPanel');"));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession); 
    }
  }

  
  
  /** logger */
  private static final Log LOG = LogFactory.getLog(SimpleAttributeNameUpdate.class);

  /**
   * index page of application
   * @param request
   * @param response
   */
  public void createEditAttributeNames(HttpServletRequest request, HttpServletResponse response) {
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();

    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();

    GrouperSession grouperSession = null;

    try {
      grouperSession = GrouperSession.start(loggedInSubject);
                
      //setup the container
      AttributeNameUpdateRequestContainer attributeNameUpdateRequestContainer = AttributeNameUpdateRequestContainer.retrieveFromRequestOrCreate();
    
      String attributeDefIdForFilter = request.getParameter("attributeDefIdForFilter");
      if (!StringUtils.isBlank(attributeDefIdForFilter)) {
        
        AttributeDef attributeDef = AttributeDefFinder.findById(attributeDefIdForFilter, true);
        
        attributeNameUpdateRequestContainer.setAttributeDefForFilter(attributeDef);
      }
      
      guiResponseJs.addAction(GuiScreenAction.newScript("document.title = '" 
          + GrouperUiUtils.message("simpleAttributeNameUpdate.addEditTitle", false) + "'"));
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#topDiv", 
          "/WEB-INF/grouperUi/templates/common/commonTop.jsp"));
  
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#bodyDiv", 
          "/WEB-INF/grouperUi/templates/simpleAttributeNameUpdate/simpleAttributeNameCreateEditInit.jsp"));
  
    } finally {
      GrouperSession.stopQuietly(grouperSession); 
    }
    
  }

  /**
   * delete button was pressed on the attribute edit panel
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void attributeNameEditPanelDelete(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
  
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
  
    AttributeNameUpdateRequestContainer attributeNameUpdateRequestContainer = AttributeNameUpdateRequestContainer.retrieveFromRequestOrCreate();
  
    GrouperSession grouperSession = null;
  
    try {
      grouperSession = GrouperSession.start(loggedInSubject);
      
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
      AttributeDefName attributeDefName = null;
      
      String uuid = httpServletRequest.getParameter("attributeDefNameToEditId");
      
      if (StringUtils.isBlank(uuid)) {
        throw new RuntimeException("Why is uuid blank????");
      }
  
      //if editing, then this must be there, or it has been tampered with
      try {
        attributeDefName = AttributeDefNameFinder.findById(uuid, true);
      } catch (Exception e) {
        LOG.info("Error searching for attribute def name: " + uuid, e);
        guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message("simpleAttributeNameUpdate.errorCantEditAttributeDefName", false)));
        return;
        
      }
      
      if (!attributeDefName.getAttributeDef().getPrivilegeDelegate().canAttrAdmin(loggedInSubject)) {
        LOG.error("Subject " + GrouperUtil.subjectToString(loggedInSubject) + " cannot admin attribute definition: " + attributeDefName.getName());
        guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message("simpleAttributeUpdate.errorCantEditAttributeDef", false)));
        return;
      }
      
      //delete it
      attributeDefName.delete();
      
      guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message(
        "simpleAttributeNameUpdate.attributeDefNameDeleted", false, true, attributeDefName.getName())));
  
      attributeNameUpdateRequestContainer.setAttributeDefNameToEdit(null);
      
      //clear out whole screen
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#bodyDiv", 
        "/WEB-INF/grouperUi/templates/simpleAttributeNameUpdate/simpleAttributeNameCreateEditInit.jsp"));
      
      
    } finally {
      GrouperSession.stopQuietly(grouperSession); 
    }
  
  
  }

  /**
   * click save button on edit panel
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void attributeNameEditPanelSubmit(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
  
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
  
    AttributeNameUpdateRequestContainer attributeNameUpdateRequestContainer = AttributeNameUpdateRequestContainer.retrieveFromRequestOrCreate();
  
    GrouperSession grouperSession = null;
  
    try {
      grouperSession = GrouperSession.start(loggedInSubject);
      
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
      AttributeDefName attributeDefName = null;
      
      //marshal params
      
      String displayExtension = httpServletRequest.getParameter("attributeDefNameToEditDisplayExtension");
      String extension = httpServletRequest.getParameter("attributeDefNameToEditExtension");
      String uuid = httpServletRequest.getParameter("attributeDefNameToEditId");
      
      boolean isCreate = StringUtils.isBlank(uuid);
  
      String stemName = null;
      Stem stem = null;
      
      if (StringUtils.isBlank(displayExtension)) {
        guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message("simpleAttributeNameUpdate.errorDisplayExtensionRequired", false)));
        return;
      }

      AttributeDef attributeDef = null;
      
      if (!isCreate) {
        //if editing, then this must be there, or it has been tampered with
        try {
          attributeDefName = AttributeDefNameFinder.findById(uuid, true);
        } catch (Exception e) {
          LOG.info("Error searching for attribute def: " + uuid, e);
          guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message("simpleAttributeNameUpdate.errorCantEditAttributeDefName", false)));
          return;
          
        }
        attributeDef = attributeDefName.getAttributeDef();
        
        stemName = attributeDefName.getStem().getName();
        extension = attributeDefName.getExtension();
      } else {
        
        String attributeDefId = httpServletRequest.getParameter("simpleAttributeNameUpdateNewAttributeDef");
        try {
          attributeDef = AttributeDefFinder.findById(attributeDefId, true);
        } catch (Exception e) {
          LOG.info("Error searching for attribute def: " + attributeDefId, e);
          guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message("simpleAttributeNameUpdate.errorCantEditAttributeDef", false)));
          return;
        }

        String stemId = httpServletRequest.getParameter("simpleAttributeNameUpdatePickNamespace");
        
        if (StringUtils.isBlank(stemId)) {
          guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message("simpleAttributeNameUpdate.errorFolderRequired", false)));
          return;
        }
  
        if (StringUtils.isBlank(extension)) {
          guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message("simpleAttributeNameUpdate.errorExtensionRequired", false)));
          return;
        }
        
        stem = StemFinder.findByUuid(grouperSession, stemId, true);
  
      }
    
      if (!attributeDef.getPrivilegeDelegate().canAttrAdmin(loggedInSubject)) {
        LOG.error("Subject " + GrouperUtil.subjectToString(loggedInSubject) + " cannot admin attribute definition: " + attributeDef.getName());
        guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message("simpleAttributeNameUpdate.errorCantEditAttributeDef", false)));
        return;
      }

      //create it... all validation should be done at this point, so we dont create, then give an error, and have a partial create
      if (isCreate) {
        
        stemName = stem.getName();
        String attributeDefNameName = stemName + ":" + extension;
        if (AttributeDefNameFinder.findByName(attributeDefNameName, false) != null) {
          guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message(
              "simpleAttributeNameUpdate.attributeDefNameExists", false, true, attributeDefNameName)));
          return;
        }
          
        attributeDefName = stem.addChildAttributeDefName(attributeDef, extension, displayExtension);
      }
      
      {
        String description = httpServletRequest.getParameter("attributeDefNameToEditDescription");
        
        attributeDefName.setDescription(description);
      }
      
      attributeDefName.setDisplayExtensionDb(displayExtension);
      
      attributeDefName.store();
      
      guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message(
          "simpleAttributeNameUpdate.attributeDefNameSaved", false, true, stemName + ":" + extension)));
  
      attributeNameUpdateRequestContainer.setAttributeDefNameToEdit(attributeDefName);
      attributeNameUpdateRequestContainer.setAttributeDef(attributeDefName.getAttributeDef());
      attributeNameUpdateRequestContainer.setCreate(false);
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#attributeNameEditPanel", 
        "/WEB-INF/grouperUi/templates/simpleAttributeNameUpdate/attributeNameEditPanel.jsp"));
  
    } finally {
      GrouperSession.stopQuietly(grouperSession); 
    }
  
  }

  /**
   * delete an attribute def name that implies the current attribute def name
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void deleteAttributeNameImplies(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
  
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();

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
        throw new RuntimeException("Why is attributeDefNameIdForHierarchy blank????");
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
      
      boolean assigned = attributeDefNameThatImplies.getAttributeDefNameSetDelegate().removeFromAttributeDefNameSet(attributeDefName);
      
      if (assigned) {
        guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message("simpleAttributeNameUpdate.successRemoveImpliesAttributeDefName", false)));
      } else {
        guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message("simpleAttributeNameUpdate.failureRemoveImpliesAttributeDefName", false)));
      }
  
    } finally {
      GrouperSession.stopQuietly(grouperSession); 
    }

    //setup the screen again...
    new SimpleAttributeNameUpdate().attributeNameEditPanelHierarchies(httpServletRequest, httpServletResponse);  

  }

  /**
   * delete an attribute def name thats implied by the current attribute def name
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void deleteAttributeNameImpliedBy(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
  
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
  
    GrouperSession grouperSession = null;
    
    try {
      grouperSession = GrouperSession.start(loggedInSubject);
      
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
      AttributeDefName attributeDefName = null;
      AttributeDefName attributeDefNameImpliedBy = null;
      
      String uuid = httpServletRequest.getParameter("attributeDefNameToEditId");
      
      if (StringUtils.isBlank(uuid)) {
        throw new RuntimeException("Why is uuid blank????");
      }
  
      String attributeDefNameIdImpliedBy = httpServletRequest.getParameter("attributeDefNameIdForHierarchy");
      
      if (StringUtils.isBlank(attributeDefNameIdImpliedBy)) {
        throw new RuntimeException("Why is attributeDefNameIdForHierarchy blank????");
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
        attributeDefNameImpliedBy = AttributeDefNameFinder.findById(attributeDefNameIdImpliedBy, true);
      } catch (Exception e) {
        LOG.info("Error searching for attribute def: " + attributeDefNameIdImpliedBy, e);
        guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message("simpleAttributeNameUpdate.errorCantEditAttributeDefNameHierarchy", false)));
        return;
        
      }
      
      if (!attributeDefNameImpliedBy.getAttributeDef().getPrivilegeDelegate().canAttrAdmin(loggedInSubject)) {
        LOG.error("Subject " + GrouperUtil.subjectToString(loggedInSubject) + " cannot admin attribute definition: " + attributeDefNameImpliedBy.getAttributeDef().getName());
        guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message("simpleAttributeNameUpdate.errorCantEditAttributeDefNameHierarchy", false)));
        return;
      }
      
      boolean assigned = attributeDefName.getAttributeDefNameSetDelegate().removeFromAttributeDefNameSet(attributeDefNameImpliedBy);
      
      if (assigned) {
        guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message("simpleAttributeNameUpdate.successRemoveImpliedByAttributeDefName", false)));
      } else {
        guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message("simpleAttributeNameUpdate.failureRemoveImpliedByAttributeDefName", false)));
      }
  
    } finally {
      GrouperSession.stopQuietly(grouperSession); 
    }
  
    //setup the screen again...
    new SimpleAttributeNameUpdate().attributeNameEditPanelHierarchies(httpServletRequest, httpServletResponse);  
  
  }

  /**
   * hierarchiesGraph button was pressed on the attribute name edit panel
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void attributeNameEditPanelHierarchiesGraph(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
  
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
  
    AttributeNameUpdateRequestContainer attributeNameUpdateRequestContainer = AttributeNameUpdateRequestContainer.retrieveFromRequestOrCreate();
  
    GrouperSession grouperSession = null;
  
    try {
      grouperSession = GrouperSession.start(loggedInSubject);
      
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
      AttributeDefName attributeDefName = null;
      
      String uuid = httpServletRequest.getParameter("attributeDefNameToEditId");
      
      if (StringUtils.isBlank(uuid)) {
        throw new RuntimeException("Why is uuid blank????");
      }
  
      //if editing, then this must be there, or it has been tampered with
      try {
        attributeDefName = AttributeDefNameFinder.findById(uuid, true);
      } catch (Exception e) {
        LOG.info("Error searching for attribute def name: " + uuid, e);
        guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message("simpleAttributeNameUpdate.errorCantEditAttributeDefName", false)));
        return;
      }
      
      if (!attributeDefName.getAttributeDef().getPrivilegeDelegate().canAttrAdmin(loggedInSubject)) {
        LOG.error("Subject " + GrouperUtil.subjectToString(loggedInSubject) + " cannot admin attribute definition: " + attributeDefName.getName());
        guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message("simpleAttributeUpdate.errorCantEditAttributeDef", false)));
        return;
      }
      if (attributeDefName.getAttributeDef().getAttributeDefType() != AttributeDefType.perm) {
        LOG.error("Why is this not a permission? " + attributeDefName);
        guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message("simpleAttributeNameUpdate.errorNotPermission", false)));
        return;
      }
      
      attributeNameUpdateRequestContainer.setAttributeDefNameToEdit(attributeDefName);
      
      attributeNameUpdateRequestContainer.setAttributeNameGraphNodesFrom(new ArrayList<String>());
      attributeNameUpdateRequestContainer.setAttributeNameGraphNodesTo(new ArrayList<String>());
      attributeNameUpdateRequestContainer.setAttributeNameGraphStartingPoints(new ArrayList<String>());
      
      Set<AttributeDefName> allAttributeNamesOnGraph = new HashSet<AttributeDefName>();
      Set<AttributeDefName> attributeDefNamesThatImplyThis = attributeDefName.getAttributeDefNameSetDelegate().getAttributeDefNamesThatImplyThis();
      Set<AttributeDefName> attributeDefNamesImpliedByThis = attributeDefName.getAttributeDefNameSetDelegate().getAttributeDefNamesImpliedByThis();
      allAttributeNamesOnGraph.addAll(attributeDefNamesThatImplyThis);
      allAttributeNamesOnGraph.addAll(attributeDefNamesImpliedByThis);
      allAttributeNamesOnGraph.add(attributeDefName);

      {
        //we are displaying by display extension, make sure there arent two nodes with the same extension
        Set<String> uniqueDisplayExtensions = new HashSet<String>();
        for (AttributeDefName current : allAttributeNamesOnGraph) {
          if (uniqueDisplayExtensions.contains(current.getDisplayExtension())) {
            throw new RuntimeException("Cannot display graph is multiple attribute names have the same display extension!");
          }
          uniqueDisplayExtensions.add(current.getDisplayExtension());
        }
      }
      
      //find out which ones are starting points
      Set<String> startingPoints = new HashSet<String>();
      for (AttributeDefName current : GrouperUtil.nonNull(attributeDefNamesThatImplyThis)) {
        startingPoints.add(current.getDisplayExtension());
      }
      
      //if none then add current
      if (startingPoints.size() == 0) {
        startingPoints.add(attributeDefName.getDisplayExtension());
      }
      
      //find all relevant relationships
      for (AttributeDefName current : GrouperUtil.nonNull(allAttributeNamesOnGraph)) {
        
        Set<AttributeDefName> attributeDefNamesImpliedByCurrentImmediate = current.getAttributeDefNameSetDelegate().getAttributeDefNamesImpliedByThisImmediate();

        for (AttributeDefName impliedBy : GrouperUtil.nonNull(attributeDefNamesImpliedByCurrentImmediate)) {
          
          //make sure it is relevant
          if (!allAttributeNamesOnGraph.contains(impliedBy)) {
            continue;
          }

          //we know which ones arent starting points
          startingPoints.remove(impliedBy.getDisplayExtension());
          
          attributeNameUpdateRequestContainer.getAttributeNameGraphNodesFrom().add(current.getDisplayExtension());
          
          attributeNameUpdateRequestContainer.getAttributeNameGraphNodesTo().add(impliedBy.getDisplayExtension());
        }
        
      }

      if (startingPoints.size() == 0) {
        startingPoints.add(attributeDefName.getDisplayExtension());
      }
      
      attributeNameUpdateRequestContainer.getAttributeNameGraphStartingPoints().addAll(startingPoints);
      
      //set the actions panel
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#attributeNameHierarchiesPanel", 
        "/WEB-INF/grouperUi/templates/simpleAttributeNameUpdate/attributeNameGraph.jsp"));
      
      guiResponseJs.addAction(GuiScreenAction.newScript("guiScrollTo('#attributeNameHierarchiesPanel');"));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession); 
    }
  }

  
  
}
