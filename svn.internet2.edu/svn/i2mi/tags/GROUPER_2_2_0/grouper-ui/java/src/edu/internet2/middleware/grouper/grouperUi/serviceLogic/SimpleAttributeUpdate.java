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
/*
 * @author mchyzer
 * $Id: SimpleMembershipUpdate.java,v 1.7 2009-11-13 14:56:25 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.grouperUi.serviceLogic;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.AttributeDefType;
import edu.internet2.middleware.grouper.attr.AttributeDefValueType;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignAction;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignBaseDelegate;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignEffMshipDelegate;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignType;
import edu.internet2.middleware.grouper.attr.finder.AttributeAssignFinder;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefFinder;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.attr.value.AttributeAssignValue;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiAttributeAssign;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiMember;
import edu.internet2.middleware.grouper.grouperUi.beans.attributeUpdate.AttributeUpdateRequestContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiPaging;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiResponseJs;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction;
import edu.internet2.middleware.grouper.internal.dao.QueryPaging;
import edu.internet2.middleware.grouper.membership.MembershipType;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.privs.AttributeDefPrivilege;
import edu.internet2.middleware.grouper.privs.Privilege;
import edu.internet2.middleware.grouper.privs.PrivilegeAssignType;
import edu.internet2.middleware.grouper.privs.PrivilegeContainer;
import edu.internet2.middleware.grouper.privs.PrivilegeContainerImpl;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.privs.PrivilegeSubjectContainer;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.ui.tags.TagUtils;
import edu.internet2.middleware.grouper.ui.util.GrouperUiUtils;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;


/**
 * main ajax methods for simple attribute update module
 */
public class SimpleAttributeUpdate {
  
  /**
   * delete button was pressed on the attribute edit panel
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void attributeEditPanelDelete(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {

    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();

    AttributeUpdateRequestContainer attributeUpdateRequestContainer = AttributeUpdateRequestContainer.retrieveFromRequestOrCreate();

    GrouperSession grouperSession = null;

    try {
      grouperSession = GrouperSession.start(loggedInSubject);
      
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();

      AttributeDef attributeDef = null;
      
      String uuid = httpServletRequest.getParameter("attributeDefToEditId");
      
      if (StringUtils.isBlank(uuid)) {
        throw new RuntimeException("Why is uuid blank????");
      }

      //if editing, then this must be there, or it has been tampered with
      try {
        attributeDef = AttributeDefFinder.findById(uuid, true);
      } catch (Exception e) {
        LOG.info("Error searching for attribute def: " + uuid, e);
        guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message("simpleAttributeUpdate.errorCantEditAttributeDef", false)));
        return;
        
      }
      
      if (!attributeDef.getPrivilegeDelegate().canAttrAdmin(loggedInSubject)) {
        LOG.error("Subject " + GrouperUtil.subjectToString(loggedInSubject) + " cannot admin attribute definition: " + attributeDef.getName());
        guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message("simpleAttributeUpdate.errorCantEditAttributeDef", false)));
        return;
      }
      
      //delete it
      attributeDef.delete();
      
      guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message(
        "simpleAttributeUpdate.attributeDefDeleted", false, true, attributeDef.getName())));

      attributeUpdateRequestContainer.setAttributeDefToEdit(null);
      
      //clear out whole screen
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#bodyDiv", 
        "/WEB-INF/grouperUi/templates/simpleAttributeUpdate/simpleAttributeCreateEditInit.jsp"));
      
      
    } finally {
      GrouperSession.stopQuietly(grouperSession); 
    }


  }  

  
  /**
   * click save button on edit panel
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void attributeEditPanelSubmit(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {

    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();

    AttributeUpdateRequestContainer attributeUpdateRequestContainer = AttributeUpdateRequestContainer.retrieveFromRequestOrCreate();

    GrouperSession grouperSession = null;

    try {
      grouperSession = GrouperSession.start(loggedInSubject);
      
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();

      AttributeDef attributeDef = null;
      
      //marshal params
      String extension = httpServletRequest.getParameter("attributeDefToEditExtension");
      String uuid = httpServletRequest.getParameter("attributeDefToEditId");

      String attributeDefToEditValueType = httpServletRequest.getParameter("attributeDefToEditValueType");
      AttributeDefValueType attributeDefValueType = AttributeDefValueType.valueOfIgnoreCase(attributeDefToEditValueType, true);
      
      String attributeDefToEditMultiValued = httpServletRequest.getParameter("attributeDefToEditMultiValued");
      boolean multiValued = GrouperUtil.booleanValue(attributeDefToEditMultiValued, false);
      
      if (attributeDefValueType == AttributeDefValueType.marker && multiValued) {
        guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message("simpleAttributeUpdate.errorNoMultiValuedIfMarker", false)));
        return;
      }

      String attributeDefToEditAssignToAttributeDef = httpServletRequest.getParameter("attributeDefToEditAssignToAttributeDef");
      boolean attributeDefToEditAssignToAttributeDefBoolean = GrouperUtil.booleanValue(attributeDefToEditAssignToAttributeDef, false);

      String attributeDefToEditAssignToAttributeDefAssign = httpServletRequest.getParameter("attributeDefToEditAssignToAttributeDefAssign");
      boolean attributeDefToEditAssignToAttributeDefAssignBoolean = GrouperUtil.booleanValue(attributeDefToEditAssignToAttributeDefAssign, false);

      String attributeDefToEditAssignToStem = httpServletRequest.getParameter("attributeDefToEditAssignToStem");
      boolean attributeDefToEditAssignToStemBoolean = GrouperUtil.booleanValue(attributeDefToEditAssignToStem, false);

      String attributeDefToEditAssignToStemAssign = httpServletRequest.getParameter("attributeDefToEditAssignToStemAssign");
      boolean attributeDefToEditAssignToStemAssignBoolean = GrouperUtil.booleanValue(attributeDefToEditAssignToStemAssign, false);

      String attributeDefToEditAssignToGroup = httpServletRequest.getParameter("attributeDefToEditAssignToGroup");
      boolean attributeDefToEditAssignToGroupBoolean = GrouperUtil.booleanValue(attributeDefToEditAssignToGroup, false);

      String attributeDefToEditAssignToGroupAssign = httpServletRequest.getParameter("attributeDefToEditAssignToGroupAssign");
      boolean attributeDefToEditAssignToGroupAssignBoolean = GrouperUtil.booleanValue(attributeDefToEditAssignToGroupAssign, false);

      String attributeDefToEditAssignToMember = httpServletRequest.getParameter("attributeDefToEditAssignToMember");
      boolean attributeDefToEditAssignToMemberBoolean = GrouperUtil.booleanValue(attributeDefToEditAssignToMember, false);

      String attributeDefToEditAssignToMemberAssign = httpServletRequest.getParameter("attributeDefToEditAssignToMemberAssign");
      boolean attributeDefToEditAssignToMemberAssignBoolean = GrouperUtil.booleanValue(attributeDefToEditAssignToMemberAssign, false);

      String attributeDefToEditAssignToMembership = httpServletRequest.getParameter("attributeDefToEditAssignToMembership");
      boolean attributeDefToEditAssignToMembershipBoolean = GrouperUtil.booleanValue(attributeDefToEditAssignToMembership, false);

      String attributeDefToEditAssignToMembershipAssign = httpServletRequest.getParameter("attributeDefToEditAssignToMembershipAssign");
      boolean attributeDefToEditAssignToMembershipAssignBoolean = GrouperUtil.booleanValue(attributeDefToEditAssignToMembershipAssign, false);

      String attributeDefToEditAssignToImmediateMembership = httpServletRequest.getParameter("attributeDefToEditAssignToImmediateMembership");
      boolean attributeDefToEditAssignToImmediateMembershipBoolean = GrouperUtil.booleanValue(attributeDefToEditAssignToImmediateMembership, false);

      String attributeDefToEditAssignToImmediateMembershipAssign = httpServletRequest.getParameter("attributeDefToEditAssignToImmediateMembershipAssign");
      boolean attributeDefToEditAssignToImmediateMembershipAssignBoolean = GrouperUtil.booleanValue(attributeDefToEditAssignToImmediateMembershipAssign, false);
      
      //validate that at least one assign to is selected
      if (!attributeDefToEditAssignToAttributeDefBoolean && !attributeDefToEditAssignToAttributeDefAssignBoolean
          && !attributeDefToEditAssignToStemBoolean && !attributeDefToEditAssignToStemAssignBoolean
          && !attributeDefToEditAssignToGroupBoolean && !attributeDefToEditAssignToGroupAssignBoolean
          && !attributeDefToEditAssignToMemberBoolean && !attributeDefToEditAssignToMemberAssignBoolean
          && !attributeDefToEditAssignToMembershipBoolean && !attributeDefToEditAssignToMembershipAssignBoolean
          && !attributeDefToEditAssignToImmediateMembershipBoolean && !attributeDefToEditAssignToImmediateMembershipAssignBoolean) {
        
        guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message("simpleAttributeUpdate.errorAssignToRequired", false)));
        return;
      }
      
      
      
      boolean isCreate = StringUtils.isBlank(uuid);

      String stemName = null;
      Stem stem = null;
      AttributeDefType attributeDefType = null;
      
      
      
      if (!isCreate) {
        //if editing, then this must be there, or it has been tampered with
        try {
          attributeDef = AttributeDefFinder.findById(uuid, true);
        } catch (Exception e) {
          LOG.info("Error searching for attribute def: " + uuid, e);
          guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message("simpleAttributeUpdate.errorCantEditAttributeDef", false)));
          return;
          
        }
      
        if (!attributeDef.getPrivilegeDelegate().canAttrAdmin(loggedInSubject)) {
          LOG.error("Subject " + GrouperUtil.subjectToString(loggedInSubject) + " cannot admin attribute definition: " + attributeDef.getName());
          guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message("simpleAttributeUpdate.errorCantEditAttributeDef", false)));
          return;
        }
        
        stemName = attributeDef.getStem().getName();
        extension = attributeDef.getExtension();
        attributeDefType = attributeDef.getAttributeDefType();
      } else {
        
        String stemId = httpServletRequest.getParameter("simpleAttributeUpdatePickNamespace");
        
        if (StringUtils.isBlank(stemId)) {
          guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message("simpleAttributeUpdate.errorFolderRequired", false)));
          return;
        }

        if (StringUtils.isBlank(extension)) {
          guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message("simpleAttributeUpdate.errorExtensionRequired", false)));
          return;
        }
       
        
        String attributeDefTypeString = httpServletRequest.getParameter("attributeDefToEditType");
        
        if (StringUtils.isBlank(attributeDefTypeString)) {
          guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message("simpleAttributeUpdate.errorAttributeTypeRequired", false)));
          return;
        }

        attributeDefType = AttributeDefType.valueOfIgnoreCase(attributeDefTypeString, true);

        stem = StemFinder.findByUuid(grouperSession, stemId, true);

      }

      //if a permission, can only be assigned to group or membership (not immediate)
      if (attributeDefType == AttributeDefType.perm &&
          (attributeDefToEditAssignToAttributeDefBoolean || attributeDefToEditAssignToAttributeDefAssignBoolean
          || attributeDefToEditAssignToStemBoolean || attributeDefToEditAssignToStemAssignBoolean
          || attributeDefToEditAssignToGroupAssignBoolean
          || attributeDefToEditAssignToMemberBoolean || attributeDefToEditAssignToMemberAssignBoolean
          || attributeDefToEditAssignToMembershipAssignBoolean
          || attributeDefToEditAssignToImmediateMembershipBoolean || attributeDefToEditAssignToImmediateMembershipAssignBoolean )
      ) {
        guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message("simpleAttributeUpdate.errorPermissionAssignToGroupOrMshipOnly", false)));
        return;
      }

      String attributeDefToEditMultiAssignable = httpServletRequest.getParameter("attributeDefToEditMultiAssignable");
      
      boolean multiAssignable = GrouperUtil.booleanValue(attributeDefToEditMultiAssignable, false);

      //invalid entry: permission type attributes cannot be multi-assignable
      if (attributeDefType == AttributeDefType.perm && multiAssignable) {
        guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message("simpleAttributeUpdate.errorPermissionSingleAssignRequired", false)));
        return;
      }

      //Invalid entry: permission type attributes must have no-value value type
      if (attributeDefType == AttributeDefType.perm && attributeDefValueType != AttributeDefValueType.marker) {
        guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message("simpleAttributeUpdate.errorPermissionNoValueRequired", false)));
        return;
      }

      //create it... all validation should be done at this point, so we dont create, then give an error, and have a partial create
      if (isCreate) {
        
        attributeDef = stem.addChildAttributeDef(extension, attributeDefType);

        stemName = stem.getName();

      }
      
      {
        String description = httpServletRequest.getParameter("attributeDefToEditDescription");
        
        attributeDef.setDescription(description);
      }
      
      attributeDef.setMultiAssignable(multiAssignable);

      attributeDef.setValueType(attributeDefValueType);

      attributeDef.setMultiValued(multiValued);

      attributeDef.setAssignToAttributeDef(attributeDefToEditAssignToAttributeDefBoolean);
      attributeDef.setAssignToAttributeDefAssn(attributeDefToEditAssignToAttributeDefAssignBoolean);
      attributeDef.setAssignToStem(attributeDefToEditAssignToStemBoolean);
      attributeDef.setAssignToStemAssn(attributeDefToEditAssignToStemAssignBoolean);
      attributeDef.setAssignToGroup(attributeDefToEditAssignToGroupBoolean);
      attributeDef.setAssignToGroupAssn(attributeDefToEditAssignToGroupAssignBoolean);
      attributeDef.setAssignToMember(attributeDefToEditAssignToMemberBoolean);
      attributeDef.setAssignToMemberAssn(attributeDefToEditAssignToMemberAssignBoolean);
      attributeDef.setAssignToEffMembership(attributeDefToEditAssignToMembershipBoolean);
      attributeDef.setAssignToEffMembershipAssn(attributeDefToEditAssignToMembershipAssignBoolean);
      attributeDef.setAssignToImmMembership(attributeDefToEditAssignToImmediateMembershipBoolean);
      attributeDef.setAssignToImmMembershipAssn(attributeDefToEditAssignToImmediateMembershipAssignBoolean);
      
      attributeDef.store();

      Subject everyEntitySubject = SubjectFinder.findAllSubject();
      
      {
        boolean attributeDefToEditAllowAllOptin = GrouperUtil.booleanValue(httpServletRequest.getParameter("attributeDefToEditAllowAllOptin"), false);
        attributeUpdateRequestContainer.setAllowAllOptin(attributeDefToEditAllowAllOptin);
        if (attributeDefToEditAllowAllOptin != attributeDef.getPrivilegeDelegate().hasAttrOptin(everyEntitySubject)) {
          if (attributeDefToEditAllowAllOptin) {
            attributeDef.getPrivilegeDelegate().grantPriv(everyEntitySubject, AttributeDefPrivilege.ATTR_OPTIN, false);
          } else{
            attributeDef.getPrivilegeDelegate().revokePriv(everyEntitySubject, AttributeDefPrivilege.ATTR_OPTIN, false);
          }
        }
      }
      {
        boolean attributeDefToEditAllowAllAttrDefAttrRead = GrouperUtil.booleanValue(httpServletRequest.getParameter("attributeDefToEditAllowAllAttrDefAttrRead"), false);
        attributeUpdateRequestContainer.setAllowAllAttrDefAttrRead(attributeDefToEditAllowAllAttrDefAttrRead);
        if (attributeDefToEditAllowAllAttrDefAttrRead != attributeDef.getPrivilegeDelegate().hasAttrDefAttrRead(everyEntitySubject)) {
          if (attributeDefToEditAllowAllAttrDefAttrRead) {
            attributeDef.getPrivilegeDelegate().grantPriv(everyEntitySubject, AttributeDefPrivilege.ATTR_DEF_ATTR_READ, false);
          } else{
            attributeDef.getPrivilegeDelegate().revokePriv(everyEntitySubject, AttributeDefPrivilege.ATTR_DEF_ATTR_READ, false);
          }
        }
      }
      {
        boolean attributeDefToEditAllowAllAttrDefAttrUpdate = GrouperUtil.booleanValue(httpServletRequest.getParameter("attributeDefToEditAllowAllAttrDefAttrUpdate"), false);
        attributeUpdateRequestContainer.setAllowAllAttrDefAttrUpdate(attributeDefToEditAllowAllAttrDefAttrUpdate);
        if (attributeDefToEditAllowAllAttrDefAttrUpdate != attributeDef.getPrivilegeDelegate().hasAttrDefAttrUpdate(everyEntitySubject)) {
          if (attributeDefToEditAllowAllAttrDefAttrUpdate) {
            attributeDef.getPrivilegeDelegate().grantPriv(everyEntitySubject, AttributeDefPrivilege.ATTR_DEF_ATTR_UPDATE, false);
          } else{
            attributeDef.getPrivilegeDelegate().revokePriv(everyEntitySubject, AttributeDefPrivilege.ATTR_DEF_ATTR_UPDATE, false);
          }
        }
      }
      {
        boolean attributeDefToEditAllowAllOptout = GrouperUtil.booleanValue(httpServletRequest.getParameter("attributeDefToEditAllowAllOptout"), false);
        attributeUpdateRequestContainer.setAllowAllOptout(attributeDefToEditAllowAllOptout);
        if (attributeDefToEditAllowAllOptout != attributeDef.getPrivilegeDelegate().hasAttrOptout(everyEntitySubject)) {
          if (attributeDefToEditAllowAllOptout) {
            attributeDef.getPrivilegeDelegate().grantPriv(everyEntitySubject, AttributeDefPrivilege.ATTR_OPTOUT, false);
          } else{
            attributeDef.getPrivilegeDelegate().revokePriv(everyEntitySubject, AttributeDefPrivilege.ATTR_OPTOUT, false);
          }
        }
      }
      {
        boolean attributeDefToEditAllowAllAdmin = GrouperUtil.booleanValue(httpServletRequest.getParameter("attributeDefToEditAllowAllAdmin"), false);
        attributeUpdateRequestContainer.setAllowAllAdmin(attributeDefToEditAllowAllAdmin);
        if (attributeDefToEditAllowAllAdmin != attributeDef.getPrivilegeDelegate().hasAttrAdmin(everyEntitySubject)) {
          if (attributeDefToEditAllowAllAdmin) {
            attributeUpdateRequestContainer.setAllowAllAdmin(true);
            attributeDef.getPrivilegeDelegate().grantPriv(everyEntitySubject, AttributeDefPrivilege.ATTR_ADMIN, false);
          } else{
            attributeDef.getPrivilegeDelegate().revokePriv(everyEntitySubject, AttributeDefPrivilege.ATTR_ADMIN, false);
          }
        }
      }
      {
        boolean attributeDefToEditAllowAllUpdate = GrouperUtil.booleanValue(httpServletRequest.getParameter("attributeDefToEditAllowAllUpdate"), false);
        attributeUpdateRequestContainer.setAllowAllUpdate(attributeDefToEditAllowAllUpdate);
        if (attributeDefToEditAllowAllUpdate != attributeDef.getPrivilegeDelegate().hasAttrUpdate(everyEntitySubject)) {
          if (attributeDefToEditAllowAllUpdate) {
            attributeDef.getPrivilegeDelegate().grantPriv(everyEntitySubject, AttributeDefPrivilege.ATTR_UPDATE, false);
          } else{
            attributeDef.getPrivilegeDelegate().revokePriv(everyEntitySubject, AttributeDefPrivilege.ATTR_UPDATE, false);
          }
        }
      }
      {
        boolean attributeDefToEditAllowAllRead = GrouperUtil.booleanValue(httpServletRequest.getParameter("attributeDefToEditAllowAllRead"), false);
        attributeUpdateRequestContainer.setAllowAllRead(attributeDefToEditAllowAllRead);
        if (attributeDefToEditAllowAllRead != attributeDef.getPrivilegeDelegate().hasAttrRead(everyEntitySubject)) {
          if (attributeDefToEditAllowAllRead) {
            attributeDef.getPrivilegeDelegate().grantPriv(everyEntitySubject, AttributeDefPrivilege.ATTR_READ, false);
          } else{
            attributeDef.getPrivilegeDelegate().revokePriv(everyEntitySubject, AttributeDefPrivilege.ATTR_READ, false);
          }
        }
      }
      {
        boolean attributeDefToEditAllowAllView = GrouperUtil.booleanValue(httpServletRequest.getParameter("attributeDefToEditAllowAllView"), false);
        attributeUpdateRequestContainer.setAllowAllView(attributeDefToEditAllowAllView);
        if (attributeDefToEditAllowAllView != attributeDef.getPrivilegeDelegate().hasAttrView(everyEntitySubject)) {
          if (attributeDefToEditAllowAllView) {
            attributeDef.getPrivilegeDelegate().grantPriv(everyEntitySubject, AttributeDefPrivilege.ATTR_VIEW, false);
          } else{
            attributeDef.getPrivilegeDelegate().revokePriv(everyEntitySubject, AttributeDefPrivilege.ATTR_VIEW, false);
          }
        }
      }
      
      
      
      guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message(
          "simpleAttributeUpdate.attributeDefSaved", false, true, stemName + ":" + extension)));

      attributeUpdateRequestContainer.setAttributeDefToEdit(attributeDef);
      attributeUpdateRequestContainer.setCreate(false);
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#attributeEditPanel", 
        "/WEB-INF/grouperUi/templates/simpleAttributeUpdate/attributeEditPanel.jsp"));

    } finally {
      GrouperSession.stopQuietly(grouperSession); 
    }


  }  
  

  /**
   * index page of application
   * @param request
   * @param response
   */
  public void index(HttpServletRequest request, HttpServletResponse response) {
    
    //setup the container
    AttributeUpdateRequestContainer.retrieveFromRequestOrCreate();

    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();

    guiResponseJs.addAction(GuiScreenAction.newScript("document.title = '" 
        + GrouperUiUtils.message("simpleAttributeUpdate.title", false) + "'"));
    guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#topDiv", 
        "/WEB-INF/grouperUi/templates/common/commonTop.jsp"));

    guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#bodyDiv", 
        "/WEB-INF/grouperUi/templates/simpleAttributeUpdate/simpleAttributeUpdateIndex.jsp"));

  }

  /**
   * index page of application
   * @param request
   * @param response
   */
  public void assignInit(HttpServletRequest request, HttpServletResponse response) {
  
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
    AttributeUpdateRequestContainer.retrieveFromRequestOrCreate();

    guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#topDiv", 
      "/WEB-INF/grouperUi/templates/common/commonTop.jsp"));

    guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#bodyDiv", 
        "/WEB-INF/grouperUi/templates/simpleAttributeUpdate/simpleAttributeAssignInit.jsp"));
  
  }

  /**
   * index page of application
   * @param request
   * @param response
   */
  public void createEdit(HttpServletRequest request, HttpServletResponse response) {
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();

    GrouperSession grouperSession = null;

    AttributeDef attributeDef = null;
    
    try {
      grouperSession = GrouperSession.start(loggedInSubject);

      //setup the container
      AttributeUpdateRequestContainer attributeUpdateRequestContainer = AttributeUpdateRequestContainer.retrieveFromRequestOrCreate();
    
      guiResponseJs.addAction(GuiScreenAction.newScript("document.title = '" 
          + GrouperUiUtils.message("simpleAttributeUpdate.addEditTitle", false) + "'"));
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#topDiv", 
          "/WEB-INF/grouperUi/templates/common/commonTop.jsp"));
  
      
      String attributeDefId = request.getParameter("attributeDefId");
      
      if (!StringUtils.isBlank(attributeDefId)) {
        
        //if editing, then this must be there, or it has been tampered with
        try {
          attributeDef = AttributeDefFinder.findById(attributeDefId, true);
        } catch (Exception e) {
          LOG.info("Error searching for attribute def: " + attributeDefId, e);
          guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message("simpleAttributeUpdate.errorCantEditAttributeDef", false)));
          return;
          
        }
        
        if (!attributeDef.getPrivilegeDelegate().canAttrAdmin(loggedInSubject)) {
          LOG.error("Subject " + GrouperUtil.subjectToString(loggedInSubject) + " cannot admin attribute definition: " + attributeDef.getName());
          guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message("simpleAttributeUpdate.errorCantEditAttributeDefPriv", false)));
          return;
        }
  
        
        attributeUpdateRequestContainer.setAttributeDefToEdit(attributeDef);
      }
    
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#bodyDiv", 
        "/WEB-INF/grouperUi/templates/simpleAttributeUpdate/simpleAttributeCreateEditInit.jsp"));
  
    } finally {
      GrouperSession.stopQuietly(grouperSession); 
    }

    if (attributeDef != null) {
      new SimpleAttributeUpdateFilter().editAttributeDefsHelper(request, response, attributeDef, true);
    }
  
  }

  /**
   * actions button was pressed on the attribute edit panel
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void attributeEditPanelActions(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
  
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
  
    AttributeUpdateRequestContainer attributeUpdateRequestContainer = AttributeUpdateRequestContainer.retrieveFromRequestOrCreate();

    GrouperSession grouperSession = null;

    try {
      grouperSession = GrouperSession.start(loggedInSubject);
      
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
      AttributeDef attributeDef = null;
      
      String uuid = httpServletRequest.getParameter("attributeDefToEditId");
      
      if (StringUtils.isBlank(uuid)) {
        throw new RuntimeException("Why is uuid blank????");
      }
  
      //if editing, then this must be there, or it has been tampered with
      try {
        attributeDef = AttributeDefFinder.findById(uuid, true);
      } catch (Exception e) {
        LOG.info("Error searching for attribute def: " + uuid, e);
        guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message("simpleAttributeUpdate.errorCantEditAttributeDef", false)));
        return;
        
      }
      
      if (!attributeDef.getPrivilegeDelegate().canAttrAdmin(loggedInSubject)) {
        LOG.error("Subject " + GrouperUtil.subjectToString(loggedInSubject) + " cannot admin attribute definition: " + attributeDef.getName());
        guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message("simpleAttributeUpdate.errorCantEditAttributeDef", false)));
        return;
      }
      
      Set<String> actionStrings = GrouperUtil.nonNull(attributeDef.getAttributeDefActionDelegate().allowedActionStrings());
      
      List<String> actionStringList = new ArrayList<String>(actionStrings);
      
      Collections.sort(actionStringList);

      attributeUpdateRequestContainer.setAttributeDefToEdit(attributeDef);
      attributeUpdateRequestContainer.setActions(actionStringList);
      
      //set the actions panel
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#attributeActionsPanel", 
        "/WEB-INF/grouperUi/templates/simpleAttributeUpdate/attributeActionsPanel.jsp"));
      
      guiResponseJs.addAction(GuiScreenAction.newScript("guiScrollTo('#attributeActionsPanel');"));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession); 
    }
  }
  
  /**
   * cancel privilege button was pressed on the attribute edit panel
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void privilegeCancel(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
  
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();

    //set the privilege panel
    guiResponseJs.addAction(GuiScreenAction.newInnerHtml("#attributePrivilegesPanel", ""));
    
  }

  
  
  /**
   * privilege image button was pressed on the privilege edit panel
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void privilegePanelImageClick(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
  
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
  
    AttributeUpdateRequestContainer attributeUpdateRequestContainer = AttributeUpdateRequestContainer.retrieveFromRequestOrCreate();

    GrouperSession grouperSession = null;

    AttributeDef attributeDef = null;
    
    try {
      grouperSession = GrouperSession.start(loggedInSubject);
      
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
      String uuid = httpServletRequest.getParameter("attributeDefToEditId");
      
      if (StringUtils.isBlank(uuid)) {
        throw new RuntimeException("Why is uuid blank????");
      }

      String memberId = httpServletRequest.getParameter("memberId");

      if (StringUtils.isBlank(memberId)) {
        throw new RuntimeException("Why is memberId blank????");
      }

      String privilegeName = httpServletRequest.getParameter("privilegeName");

      if (StringUtils.isBlank(privilegeName)) {
        throw new RuntimeException("Why is privilegeName blank????");
      }

      String allowString = httpServletRequest.getParameter("allow");

      if (StringUtils.isBlank(allowString)) {
        throw new RuntimeException("Why is allow blank????");
      }
      boolean allow = GrouperUtil.booleanValue(allowString);
      
      //if editing, then this must be there, or it has been tampered with
      try {
        attributeDef = AttributeDefFinder.findById(uuid, true);
      } catch (Exception e) {
        LOG.info("Error searching for attribute def: " + uuid, e);
        guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message("simpleAttributeUpdate.errorCantEditAttributeDef", false)));
        return;
        
      }
      
      if (!attributeDef.getPrivilegeDelegate().canAttrAdmin(loggedInSubject)) {
        LOG.error("Subject " + GrouperUtil.subjectToString(loggedInSubject) + " cannot admin attribute definition: " + attributeDef.getName());
        guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message("simpleAttributeUpdate.errorCantEditAttributeDef", false)));
        return;
      }
      
      attributeUpdateRequestContainer.setAttributeDefToEdit(attributeDef);
      
      StringBuilder alert = new StringBuilder();
      
      Member member = MemberFinder.findByUuid(grouperSession, memberId, true);
      Subject subject = member.getSubject();
      Privilege privilege = Privilege.getInstance(privilegeName);
      String privilegeGuiLabel = GrouperUiUtils.message("priv." + privilege.getName(), false);

      GuiMember guiMember = new GuiMember(member);
      String subjectScreenLabel = guiMember.getGuiSubject().getScreenLabel();

      if (allow) {
        boolean result = attributeDef.getPrivilegeDelegate().grantPriv(
            subject, privilege, false);
        
        if (result) {
          
          alert.append(GrouperUiUtils.message("simpleAttributeUpdate.privilegeGrant", false, true, new Object[]{privilegeGuiLabel, subjectScreenLabel}));
          
        } else {
          
          alert.append(GrouperUiUtils.message("simpleAttributeUpdate.privilegeGrantWarn", false, true, new Object[]{privilegeGuiLabel, subjectScreenLabel}));
        }              
      } else {
        boolean result = attributeDef.getPrivilegeDelegate().revokePriv(
            subject, privilege, false);
        
        if (result) {
          
          alert.append(GrouperUiUtils.message("simpleAttributeUpdate.privilegeRevoke", false, true, new Object[]{privilegeGuiLabel, subjectScreenLabel}));
          
        } else {
          
          alert.append(GrouperUiUtils.message("simpleAttributeUpdate.privilegeRevokeWarn", false, true, new Object[]{privilegeGuiLabel, subjectScreenLabel}));
        }              
      }
      
      guiResponseJs.addAction(GuiScreenAction.newAlert(alert.toString()));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession); 
    }
    
    //show the edit panel since it might change due to everyentity privilege changing
    if (!new SimpleAttributeUpdateFilter().editAttributeDefsHelper(httpServletRequest, httpServletResponse, attributeDef, false)) {
      return;
    }
    
    attributeEditPanelPrivileges(httpServletRequest, httpServletResponse);
    
  }

  /**
   * submit privileges button was pressed on the privilege edit panel
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void privilegePanelSubmit(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
  
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
  
    AttributeUpdateRequestContainer attributeUpdateRequestContainer = AttributeUpdateRequestContainer.retrieveFromRequestOrCreate();

    GrouperSession grouperSession = null;

    AttributeDef attributeDef = null;
    
    try {
      grouperSession = GrouperSession.start(loggedInSubject);
      
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
      String uuid = httpServletRequest.getParameter("attributeDefToEditId");
      
      if (StringUtils.isBlank(uuid)) {
        throw new RuntimeException("Why is uuid blank????");
      }
  
      //if editing, then this must be there, or it has been tampered with
      try {
        attributeDef = AttributeDefFinder.findById(uuid, true);
      } catch (Exception e) {
        LOG.info("Error searching for attribute def: " + uuid, e);
        guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message("simpleAttributeUpdate.errorCantEditAttributeDef", false)));
        return;
        
      }
      
      if (!attributeDef.getPrivilegeDelegate().canAttrAdmin(loggedInSubject)) {
        LOG.error("Subject " + GrouperUtil.subjectToString(loggedInSubject) + " cannot admin attribute definition: " + attributeDef.getName());
        guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message("simpleAttributeUpdate.errorCantEditAttributeDef", false)));
        return;
      }
      
      attributeUpdateRequestContainer.setAttributeDefToEdit(attributeDef);
      
      //lets see what to do...
      //<input  name="previousState__${guiMember.member.uuid}__${privilegeName}"
      //  type="hidden" value="${privilegeSubjectContainer.privilegeContainers[privilegeName].privilegeAssignType.immediate ? 'true' : 'false'}" />
      //<%-- note, too much space between elements, move it over 3px --%>
      //<input  style="margin-right: -3px" name="privilegeCheckbox__${guiMember.member.uuid}__${privilegeName}"
      //  type="checkbox" ${privilegeSubjectContainer.privilegeContainers[privilegeName].privilegeAssignType.immediate ? 'checked="checked"' : '' } 
      ///><c:choose>
      
      StringBuilder alert = new StringBuilder();
      
      Pattern pattern = Pattern.compile("^previousState__(.*)__(.*)$");
      Enumeration<?> enumeration = httpServletRequest.getParameterNames();
      while (enumeration != null && enumeration.hasMoreElements()) {
        String paramName = (String)enumeration.nextElement();
        Matcher matcher = pattern.matcher(paramName);
        if (matcher.matches()) {
          
          //lets get the previous state
          boolean previousChecked = GrouperUtil.booleanValue(httpServletRequest.getParameter(paramName));
          
          //get current state
          String memberId = matcher.group(1);
          String privilegeName = matcher.group(2);
          String currentStateString = httpServletRequest.getParameter("privilegeCheckbox__" + memberId + "__" + privilegeName);
          boolean currentChecked = GrouperUtil.booleanValue(currentStateString, false);
          
          //if they dont match, do something about it
          if (previousChecked != currentChecked) {
            
            Member member = MemberFinder.findByUuid(grouperSession, memberId, true);
            Subject subject = member.getSubject();
            Privilege privilege = Privilege.getInstance(privilegeName);
            String privilegeGuiLabel = GrouperUiUtils.message("priv." + privilege.getName(), false);

            if (alert.length() > 0) {
              alert.append("<br />");
            }

            GuiMember guiMember = new GuiMember(member);
            String subjectScreenLabel = guiMember.getGuiSubject().getScreenLabel();

            if (currentChecked) {
              boolean result = attributeDef.getPrivilegeDelegate().grantPriv(
                  subject, privilege, false);
              
              if (result) {
                
                alert.append(GrouperUiUtils.message("simpleAttributeUpdate.privilegeGrant", false, true, new Object[]{privilegeGuiLabel, subjectScreenLabel}));
                
              } else {
                
                alert.append(GrouperUiUtils.message("simpleAttributeUpdate.privilegeGrantWarn", false, true, new Object[]{privilegeGuiLabel, subjectScreenLabel}));
              }              
            } else {
              boolean result = attributeDef.getPrivilegeDelegate().revokePriv(
                  subject, privilege, false);
              
              if (result) {
                
                alert.append(GrouperUiUtils.message("simpleAttributeUpdate.privilegeRevoke", false, true, new Object[]{privilegeGuiLabel, subjectScreenLabel}));
                
              } else {
                
                alert.append(GrouperUiUtils.message("simpleAttributeUpdate.privilegeRevokeWarn", false, true, new Object[]{privilegeGuiLabel, subjectScreenLabel}));
              }              
            }
          }
        }
      }
      
      if (alert.length() > 0) {
        guiResponseJs.addAction(GuiScreenAction.newAlert(alert.toString()));
      } else {
        guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message("simpleAttributeUpdate.noPrivilegeChangesDetected", false)));
      }
      
    } finally {
      GrouperSession.stopQuietly(grouperSession); 
    }
    
    //show the edit panel since it might change due to everyentity privilege changing
    if (!new SimpleAttributeUpdateFilter().editAttributeDefsHelper(httpServletRequest, httpServletResponse, attributeDef, false)) {
      return;
    }
    
    attributeEditPanelPrivileges(httpServletRequest, httpServletResponse);
    
  }

  /**
   * privileges button was pressed on the attribute edit panel
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void attributeEditPanelPrivilegesClearPaging(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
    
    //we need to set page number to 1, in general a good thing to do, and specifically, if reducing the numbers
    //and on a later page, then no members will be shown...
    GuiPaging guiPaging = GuiPaging.retrievePaging("simpleAttributeUpdatePrivileges", false);
    if (guiPaging != null) {
      guiPaging.setPageNumber(1);
    }
    attributeEditPanelPrivileges(httpServletRequest, httpServletResponse);
  }

  /**
   * privileges button was pressed on the attribute edit panel
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void attributeEditPanelPrivileges(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
  
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
  
    AttributeUpdateRequestContainer attributeUpdateRequestContainer = AttributeUpdateRequestContainer.retrieveFromRequestOrCreate();

    GrouperSession grouperSession = null;

    try {
      grouperSession = GrouperSession.start(loggedInSubject);
      
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
      AttributeDef attributeDef = attributeUpdateRequestContainer.getAttributeDefToEdit();
      
      if (attributeDef == null) {
      
        String uuid = httpServletRequest.getParameter("attributeDefToEditId");
        
        if (StringUtils.isBlank(uuid)) {
          throw new RuntimeException("Why is uuid blank????");
        }
    
        //if editing, then this must be there, or it has been tampered with
        try {
          attributeDef = AttributeDefFinder.findById(uuid, true);
        } catch (Exception e) {
          LOG.info("Error searching for attribute def: " + uuid, e);
          guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message("simpleAttributeUpdate.errorCantEditAttributeDef", false)));
          return;
          
        }
        
        if (!attributeDef.getPrivilegeDelegate().canAttrAdmin(loggedInSubject)) {
          LOG.error("Subject " + GrouperUtil.subjectToString(loggedInSubject) + " cannot admin attribute definition: " + attributeDef.getName());
          guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message("simpleAttributeUpdate.errorCantEditAttributeDef", false)));
          return;
        }
      }
      
      //might be additional members at top of list
      List<GuiMember> additionalGuiMembers = attributeUpdateRequestContainer.privilegeAdditionalGuiMembers();
      
      Set<Member> additionalMembers = null;
      
      if (GrouperUtil.length(additionalGuiMembers) > 0) {
        
        //needs to be ordered
        additionalMembers = new LinkedHashSet<Member>();
        for (GuiMember guiMember : additionalGuiMembers) {
          additionalMembers.add(guiMember.getMember());
        }
        
      }
      
      boolean showIndirectPrivileges = attributeUpdateRequestContainer.isShowIndirectPrivilegesComputed();
      
      GuiPaging guiPaging = GuiPaging.retrievePaging("simpleAttributeUpdatePrivileges", false);
      if (guiPaging == null) {
        GuiPaging.init("simpleAttributeUpdatePrivileges");
        guiPaging = GuiPaging.retrievePaging("simpleAttributeUpdatePrivileges", true);
      }
      
      
      QueryPaging queryPaging = guiPaging.queryPaging();
      //this could have messed up some stuff
      queryPaging.setPageNumber(guiPaging.getPageNumber());
      queryPaging.setDoTotalCount(true);
      
      Set<PrivilegeSubjectContainer> privilegeSubjectContainers = grouperSession
        .getAttributeDefResolver().retrievePrivileges(attributeDef, null, 
            showIndirectPrivileges ? null : MembershipType.IMMEDIATE, queryPaging, additionalMembers);
      
      //set back the total record count
      guiPaging.setTotalRecordCount(queryPaging.getTotalRecordCount());
      
      Subject everyEntitySubject = SubjectFinder.findAllSubject();
      boolean allowAllAdmin = attributeDef.getPrivilegeDelegate().hasAttrAdmin(everyEntitySubject);
      boolean allowAllUpdate = attributeDef.getPrivilegeDelegate().hasAttrUpdate(everyEntitySubject);
      boolean allowAllRead = attributeDef.getPrivilegeDelegate().hasAttrRead(everyEntitySubject);
      boolean allowAllView = attributeDef.getPrivilegeDelegate().hasAttrView(everyEntitySubject);
      boolean allowAllOptin = attributeDef.getPrivilegeDelegate().hasAttrOptin(everyEntitySubject);
      boolean allowAllOptout = attributeDef.getPrivilegeDelegate().hasAttrOptout(everyEntitySubject);
      boolean allowAllAttrDefAttrRead = attributeDef.getPrivilegeDelegate().hasAttrDefAttrRead(everyEntitySubject);
      boolean allowAllAttrDefAttrUpdate = attributeDef.getPrivilegeDelegate().hasAttrDefAttrUpdate(everyEntitySubject);

      //lets setup the hierarchies, inheritance, from allow all, and actions which imply other actions
      for (PrivilegeSubjectContainer privilegeSubjectContainer : GrouperUtil.nonNull(privilegeSubjectContainers)) {
        if (privilegeSubjectContainer.getPrivilegeContainers() == null) {
          privilegeSubjectContainer.setPrivilegeContainers(new HashMap<String, PrivilegeContainer>());
        }
        //go through each
        boolean canAdmin = false;
        {
          PrivilegeContainer attrAdmin = privilegeSubjectContainer.getPrivilegeContainers().get(AttributeDefPrivilege.ATTR_ADMIN.getName());
          canAdmin = attrAdmin != null || allowAllAdmin;
          //see if inheriting
          if (allowAllAdmin) {
            if (attrAdmin == null) {
              attrAdmin = new PrivilegeContainerImpl();
              attrAdmin.setPrivilegeName(AttributeDefPrivilege.ATTR_ADMIN.getName());
              privilegeSubjectContainer.getPrivilegeContainers().put(AttributeDefPrivilege.ATTR_ADMIN.getName(), attrAdmin);
              attrAdmin.setPrivilegeAssignType(PrivilegeAssignType.EFFECTIVE);
            } else {
              attrAdmin.setPrivilegeAssignType(PrivilegeAssignType.convert(attrAdmin.getPrivilegeAssignType(), PrivilegeAssignType.EFFECTIVE));
            }
          }
        }
        boolean canUpdate = false;
        {
          PrivilegeContainer attrUpdate = privilegeSubjectContainer.getPrivilegeContainers().get("attrUpdate");
          canUpdate = attrUpdate != null || allowAllUpdate || canAdmin;
          //see if inheriting
          if (allowAllUpdate || canAdmin) {
            if (attrUpdate == null) {
              attrUpdate = new PrivilegeContainerImpl();
              attrUpdate.setPrivilegeName(AttributeDefPrivilege.ATTR_UPDATE.getName());
              privilegeSubjectContainer.getPrivilegeContainers().put(AttributeDefPrivilege.ATTR_UPDATE.getName(), attrUpdate);
              attrUpdate.setPrivilegeAssignType(PrivilegeAssignType.EFFECTIVE);
            } else {
              attrUpdate.setPrivilegeAssignType(PrivilegeAssignType.convert(attrUpdate.getPrivilegeAssignType(), PrivilegeAssignType.EFFECTIVE));
            }
          }
        }
        boolean canRead = false;
        {
          PrivilegeContainer attrRead = privilegeSubjectContainer.getPrivilegeContainers().get("attrRead");
          canRead = attrRead != null || allowAllRead || canAdmin;
          //see if inheriting
          if (allowAllRead || canAdmin) {
            if (attrRead == null) {
              attrRead = new PrivilegeContainerImpl();
              attrRead.setPrivilegeName(AttributeDefPrivilege.ATTR_READ.getName());
              privilegeSubjectContainer.getPrivilegeContainers().put(AttributeDefPrivilege.ATTR_READ.getName(), attrRead);
              attrRead.setPrivilegeAssignType(PrivilegeAssignType.EFFECTIVE);
            } else {
              attrRead.setPrivilegeAssignType(PrivilegeAssignType.convert(attrRead.getPrivilegeAssignType(), PrivilegeAssignType.EFFECTIVE));
            }
          }
        }
        boolean canOptin = false;
        {
          PrivilegeContainer attrOptin = privilegeSubjectContainer.getPrivilegeContainers().get("attrOptin");
          canOptin = attrOptin != null || allowAllOptin || canAdmin || canUpdate;
          //see if inheriting
          if (allowAllOptin || canAdmin || canUpdate) {
            if (attrOptin == null) {
              attrOptin = new PrivilegeContainerImpl();
              attrOptin.setPrivilegeName(AttributeDefPrivilege.ATTR_OPTIN.getName());
              privilegeSubjectContainer.getPrivilegeContainers().put(AttributeDefPrivilege.ATTR_OPTIN.getName(), attrOptin);
              attrOptin.setPrivilegeAssignType(PrivilegeAssignType.EFFECTIVE);
            } else {
              attrOptin.setPrivilegeAssignType(PrivilegeAssignType.convert(attrOptin.getPrivilegeAssignType(), PrivilegeAssignType.EFFECTIVE));
            }
          }
        }
        boolean canAttrDefAttrRead = false;
        {
          PrivilegeContainer attrDefAttrRead = privilegeSubjectContainer.getPrivilegeContainers().get("attrDefAttrRead");
          canAttrDefAttrRead = attrDefAttrRead != null || allowAllAttrDefAttrRead || canAdmin;
          //see if inheriting
          if (allowAllAttrDefAttrRead || canAdmin) {
            if (attrDefAttrRead == null) {
              attrDefAttrRead = new PrivilegeContainerImpl();
              attrDefAttrRead.setPrivilegeName(AttributeDefPrivilege.ATTR_DEF_ATTR_READ.getName());
              privilegeSubjectContainer.getPrivilegeContainers().put(AttributeDefPrivilege.ATTR_DEF_ATTR_READ.getName(), attrDefAttrRead);
              attrDefAttrRead.setPrivilegeAssignType(PrivilegeAssignType.EFFECTIVE);
            } else {
              attrDefAttrRead.setPrivilegeAssignType(PrivilegeAssignType.convert(attrDefAttrRead.getPrivilegeAssignType(), PrivilegeAssignType.EFFECTIVE));
            }
          }
        }
        boolean canAttrDefAttrUpdate = false;
        {
          PrivilegeContainer attrDefAttrUpdate = privilegeSubjectContainer.getPrivilegeContainers().get("attrDefAttrUpdate");
          canAttrDefAttrUpdate = attrDefAttrUpdate != null || allowAllAttrDefAttrUpdate || canAdmin;
          //see if inheriting
          if (allowAllAttrDefAttrUpdate || canAdmin) {
            if (attrDefAttrUpdate == null) {
              attrDefAttrUpdate = new PrivilegeContainerImpl();
              attrDefAttrUpdate.setPrivilegeName(AttributeDefPrivilege.ATTR_DEF_ATTR_UPDATE.getName());
              privilegeSubjectContainer.getPrivilegeContainers().put(AttributeDefPrivilege.ATTR_DEF_ATTR_UPDATE.getName(), attrDefAttrUpdate);
              attrDefAttrUpdate.setPrivilegeAssignType(PrivilegeAssignType.EFFECTIVE);
            } else {
              attrDefAttrUpdate.setPrivilegeAssignType(PrivilegeAssignType.convert(attrDefAttrUpdate.getPrivilegeAssignType(), PrivilegeAssignType.EFFECTIVE));
            }
          }
        }
        boolean canOptout = false;
        {
          PrivilegeContainer attrOptout = privilegeSubjectContainer.getPrivilegeContainers().get("attrOptout");
          canOptout = attrOptout != null || allowAllOptout || canAdmin || canUpdate;
          //see if inheriting
          if (allowAllOptout || canAdmin || canUpdate) {
            if (attrOptout == null) {
              attrOptout = new PrivilegeContainerImpl();
              attrOptout.setPrivilegeName(AttributeDefPrivilege.ATTR_OPTOUT.getName());
              privilegeSubjectContainer.getPrivilegeContainers().put(AttributeDefPrivilege.ATTR_OPTOUT.getName(), attrOptout);
              attrOptout.setPrivilegeAssignType(PrivilegeAssignType.EFFECTIVE);
            } else {
              attrOptout.setPrivilegeAssignType(PrivilegeAssignType.convert(attrOptout.getPrivilegeAssignType(), PrivilegeAssignType.EFFECTIVE));
            }
          }
        }
        {
          PrivilegeContainer attrView = privilegeSubjectContainer.getPrivilegeContainers().get("attrView");
          //boolean canView = attrView != null || allowAllView || canAdmin || canUpdate || canRead || canOptin || canOptout;
          //see if inheriting
          if (allowAllView || canAdmin || canUpdate || canRead || canOptin || canOptout || canAttrDefAttrRead || canAttrDefAttrUpdate) {
            if (attrView == null) {
              attrView = new PrivilegeContainerImpl();
              attrView.setPrivilegeName(AttributeDefPrivilege.ATTR_VIEW.getName());
              privilegeSubjectContainer.getPrivilegeContainers().put(AttributeDefPrivilege.ATTR_VIEW.getName(), attrView);
              attrView.setPrivilegeAssignType(PrivilegeAssignType.EFFECTIVE);
            } else {
              attrView.setPrivilegeAssignType(PrivilegeAssignType.convert(attrView.getPrivilegeAssignType(), PrivilegeAssignType.EFFECTIVE));
            }
          }
        }
        
        
      }
      
      attributeUpdateRequestContainer.setAttributeDefToEdit(attributeDef);
      attributeUpdateRequestContainer.setPrivilegeSubjectContainers(privilegeSubjectContainers);
      
      List<GuiMember> guiMembers = new ArrayList<GuiMember>();
      
      for (PrivilegeSubjectContainer privilegeSubjectContainer : privilegeSubjectContainers) {
        //guiMember.setGuiSubject(new GuiSubject(privilegeSubjectContainer.getSubject()));
        //note, probably better not to do this in loop, oh well
        Member member = MemberFinder.findBySubject(grouperSession, privilegeSubjectContainer.getSubject(), true);
        GuiMember guiMember = new GuiMember(member);
        guiMembers.add(guiMember);
      }
      
      attributeUpdateRequestContainer.setPrivilegeSubjectContainerGuiMembers(guiMembers);
      
      //set the privilege panel
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#attributePrivilegesPanel", 
        "/WEB-INF/grouperUi/templates/simpleAttributeUpdate/attributePrivilegesPanel.jsp"));
      
      guiResponseJs.addAction(GuiScreenAction.newScript("guiScrollTo('#attributePrivilegesPanel');"));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession); 
    }
  }
  
  /**
   * delete an action
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void deleteAction(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
  
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
  
    AttributeUpdateRequestContainer attributeUpdateRequestContainer = AttributeUpdateRequestContainer.retrieveFromRequestOrCreate();

    GrouperSession grouperSession = null;

    try {
      grouperSession = GrouperSession.start(loggedInSubject);
      
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
      AttributeDef attributeDef = null;
      
      String uuid = httpServletRequest.getParameter("attributeDefToEditId");
      
      if (StringUtils.isBlank(uuid)) {
        throw new RuntimeException("Why is uuid blank????");
      }
  
      String action = httpServletRequest.getParameter("action");
      
      if (StringUtils.isBlank(action)) {
        throw new RuntimeException("Why is action blank????");
      }
      
      //if editing, then this must be there, or it has been tampered with
      try {
        attributeDef = AttributeDefFinder.findById(uuid, true);
      } catch (Exception e) {
        LOG.info("Error searching for attribute def: " + uuid, e);
        guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message("simpleAttributeUpdate.errorCantEditAttributeDef", false)));
        return;
        
      }
      
      if (!attributeDef.getPrivilegeDelegate().canAttrAdmin(loggedInSubject)) {
        LOG.error("Subject " + GrouperUtil.subjectToString(loggedInSubject) + " cannot admin attribute definition: " + attributeDef.getName());
        guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message("simpleAttributeUpdate.errorCantEditAttributeDef", false)));
        return;
      }
      
      if (attributeDef.getAttributeDefActionDelegate().allowedActions().size() == 1) {
        guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message("simpleAttributeUpdate.cantDeleteLastAction", false)));
        return;
      }
      
      attributeDef.getAttributeDefActionDelegate().removeAction(action);
      
      Set<String> actionStrings = GrouperUtil.nonNull(attributeDef.getAttributeDefActionDelegate().allowedActionStrings());
      
      List<String> actionStringList = new ArrayList<String>(actionStrings);
      
      Collections.sort(actionStringList);

      attributeUpdateRequestContainer.setActions(actionStringList);
      attributeUpdateRequestContainer.setAttributeDefToEdit(attributeDef);
      
      String successMessage = TagUtils.navResourceString("simpleAttributeUpdate.successDeleteAction");
      successMessage = StringUtils.replace(successMessage, "{0}", GrouperUiUtils.escapeHtml(action, true));
      guiResponseJs.addAction(GuiScreenAction.newAlert(successMessage));

      //set the actions panel
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#attributeActionsPanel", 
        "/WEB-INF/grouperUi/templates/simpleAttributeUpdate/attributeActionsPanel.jsp"));
      
      guiResponseJs.addAction(GuiScreenAction.newScript("guiScrollTo('#attributeActionsPanel');"));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession); 
    }
  }
  
  /**
   * new actions
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void newActions(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
    changeActionsHelper(httpServletRequest, httpServletResponse, true);
  }

  /**
   * replace actions
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void replaceActions(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
    changeActionsHelper(httpServletRequest, httpServletResponse, false);
  }

  /**
   * change actions
   * @param httpServletRequest
   * @param httpServletResponse
   * @param isAdd true for add, false for replace
   */
  private void changeActionsHelper(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, boolean isAdd) {
  
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
  
    AttributeUpdateRequestContainer attributeUpdateRequestContainer = AttributeUpdateRequestContainer.retrieveFromRequestOrCreate();
  
    GrouperSession grouperSession = null;
  
    try {
      grouperSession = GrouperSession.start(loggedInSubject);
      
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
      AttributeDef attributeDef = null;
      
      String uuid = httpServletRequest.getParameter("attributeDefToEditId");
      
      if (StringUtils.isBlank(uuid)) {
        throw new RuntimeException("Why is uuid blank????");
      }
  
      String changeActions = httpServletRequest.getParameter("changeActions");
      
      if (StringUtils.isBlank(changeActions)) {
        guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message("simpleAttributeUpdate.errorChangeActions", false)));
        return;
      }
      
      //if editing, then this must be there, or it has been tampered with
      try {
        attributeDef = AttributeDefFinder.findById(uuid, true);
      } catch (Exception e) {
        LOG.info("Error searching for attribute def: " + uuid, e);
        guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message("simpleAttributeUpdate.errorCantEditAttributeDef", false)));
        return;
        
      }
      
      if (!attributeDef.getPrivilegeDelegate().canAttrAdmin(loggedInSubject)) {
        LOG.error("Subject " + GrouperUtil.subjectToString(loggedInSubject) + " cannot admin attribute definition: " + attributeDef.getName());
        guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message("simpleAttributeUpdate.errorCantEditAttributeDef", false)));
        return;
      }
      
      //this will split trim by comma or semi or whitespace
      changeActions = GrouperUtil.normalizeEmailAddresses(changeActions);
      
      //note, we dont really care if it is indeed a new action or not, just set it
      Set<String> changeActionsSet = GrouperUtil.splitTrimToSet(changeActions, ";");

      if (GrouperUtil.length(changeActionsSet) == 0) {
        guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message("simpleAttributeUpdate.errorChangeActions", false)));
        return;
      }

      if (isAdd) {
        for (String action : changeActionsSet) {
          attributeDef.getAttributeDefActionDelegate().addAction(action);
        }
      } else {
        //else replace
        attributeDef.getAttributeDefActionDelegate().configureActionList(changeActionsSet);
      }

      guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message("simpleAttributeUpdate.successChangeActions", false)));

      Set<String> actionStrings = GrouperUtil.nonNull(attributeDef.getAttributeDefActionDelegate().allowedActionStrings());
      
      List<String> actionStringList = new ArrayList<String>(actionStrings);
      
      Collections.sort(actionStringList);
      
      attributeUpdateRequestContainer.setActions(actionStringList);
      attributeUpdateRequestContainer.setAttributeDefToEdit(attributeDef);

      //set the actions panel
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#attributeActionsPanel", 
        "/WEB-INF/grouperUi/templates/simpleAttributeUpdate/attributeActionsPanel.jsp"));
      
      guiResponseJs.addAction(GuiScreenAction.newScript("guiScrollTo('#attributeActionsPanel');"));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession); 
    }
  }

  /**
   * delete an action that implies the current action
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void deleteActionImplies(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
  
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
  
    AttributeUpdateRequestContainer attributeUpdateRequestContainer = AttributeUpdateRequestContainer.retrieveFromRequestOrCreate();

    GrouperSession grouperSession = null;

    try {
      grouperSession = GrouperSession.start(loggedInSubject);

      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();

      AttributeDef attributeDef = null;

      String uuid = httpServletRequest.getParameter("attributeDefToEditId");

      if (StringUtils.isBlank(uuid)) {
        throw new RuntimeException("Why is uuid blank????");
      }
  
      String action = httpServletRequest.getParameter("action");
      
      if (StringUtils.isBlank(action)) {
        throw new RuntimeException("Why is action blank????");
      }
      
      //if editing, then this must be there, or it has been tampered with
      try {
        attributeDef = AttributeDefFinder.findById(uuid, true);
      } catch (Exception e) {
        LOG.info("Error searching for attribute def: " + uuid, e);
        guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message("simpleAttributeUpdate.errorCantEditAttributeDef", false)));
        return;
        
      }
      
      if (!attributeDef.getPrivilegeDelegate().canAttrAdmin(loggedInSubject)) {
        LOG.error("Subject " + GrouperUtil.subjectToString(loggedInSubject) + " cannot admin attribute definition: " + attributeDef.getName());
        guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message("simpleAttributeUpdate.errorCantEditAttributeDef", false)));
        return;
      }
      
      String actionImplies = httpServletRequest.getParameter("actionImplies");
      
      if (StringUtils.isBlank(actionImplies)) {
        throw new RuntimeException("Why is actionImplies blank?");
      }
      
      AttributeAssignAction attributeAssignAction = attributeDef.getAttributeDefActionDelegate().allowedAction(action, true);
      AttributeAssignAction attributeAssignActionThatImplies = attributeDef.getAttributeDefActionDelegate().allowedAction(actionImplies, true);
      attributeAssignActionThatImplies.getAttributeAssignActionSetDelegate().removeFromAttributeAssignActionSet(attributeAssignAction);

      guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message("simpleAttributeUpdate.successRemoveImpliesAction", false)));

      setupEditActionPanel(attributeUpdateRequestContainer, guiResponseJs, attributeDef,
          action);
      
    } finally {
      GrouperSession.stopQuietly(grouperSession); 
    }
  }
  
  /**
   * add an action that implies the current action
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void addActionEditImplies(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
  
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
  
    AttributeUpdateRequestContainer attributeUpdateRequestContainer = AttributeUpdateRequestContainer.retrieveFromRequestOrCreate();
  
    GrouperSession grouperSession = null;
  
    try {
      grouperSession = GrouperSession.start(loggedInSubject);
      
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
      AttributeDef attributeDef = null;
      
      String uuid = httpServletRequest.getParameter("attributeDefToEditId");
      
      if (StringUtils.isBlank(uuid)) {
        throw new RuntimeException("Why is uuid blank????");
      }
  
      String action = httpServletRequest.getParameter("action");
      
      if (StringUtils.isBlank(action)) {
        throw new RuntimeException("Why is action blank????");
      }
      
      //if editing, then this must be there, or it has been tampered with
      try {
        attributeDef = AttributeDefFinder.findById(uuid, true);
      } catch (Exception e) {
        LOG.info("Error searching for attribute def: " + uuid, e);
        guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message("simpleAttributeUpdate.errorCantEditAttributeDef", false)));
        return;
        
      }
      
      if (!attributeDef.getPrivilegeDelegate().canAttrAdmin(loggedInSubject)) {
        LOG.error("Subject " + GrouperUtil.subjectToString(loggedInSubject) + " cannot admin attribute definition: " + attributeDef.getName());
        guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message("simpleAttributeUpdate.errorCantEditAttributeDef", false)));
        return;
      }
      
      String actionAddImply = httpServletRequest.getParameter("actionAddImply");
      
      if (StringUtils.isBlank(actionAddImply)) {
        guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message("simpleAttributeUpdate.mustSelectAnActionToImply", false)));
        return;
      }
      
      AttributeAssignAction attributeAssignAction = attributeDef.getAttributeDefActionDelegate().allowedAction(action, true);
      AttributeAssignAction attributeAssignActionThatImplies = attributeDef.getAttributeDefActionDelegate().allowedAction(actionAddImply, true);
      attributeAssignActionThatImplies.getAttributeAssignActionSetDelegate().addToAttributeAssignActionSet(attributeAssignAction);

      guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message("simpleAttributeNameUpdate.successAddImpliesAttributeDefName", false)));

      setupEditActionPanel(attributeUpdateRequestContainer, guiResponseJs, attributeDef,
          action);
      
    } finally {
      GrouperSession.stopQuietly(grouperSession); 
    }
  }
  
  /**
   * edit an action
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void editAction(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
  
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
  
    AttributeUpdateRequestContainer attributeUpdateRequestContainer = AttributeUpdateRequestContainer.retrieveFromRequestOrCreate();
  
    GrouperSession grouperSession = null;
  
    try {
      grouperSession = GrouperSession.start(loggedInSubject);
      
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
      AttributeDef attributeDef = null;
      
      String uuid = httpServletRequest.getParameter("attributeDefToEditId");
      
      if (StringUtils.isBlank(uuid)) {
        throw new RuntimeException("Why is uuid blank????");
      }
  
      String action = httpServletRequest.getParameter("action");
      
      if (StringUtils.isBlank(action)) {
        throw new RuntimeException("Why is action blank????");
      }
      
      //if editing, then this must be there, or it has been tampered with
      try {
        attributeDef = AttributeDefFinder.findById(uuid, true);
      } catch (Exception e) {
        LOG.info("Error searching for attribute def: " + uuid, e);
        guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message("simpleAttributeUpdate.errorCantEditAttributeDef", false)));
        return;
        
      }
      
      if (!attributeDef.getPrivilegeDelegate().canAttrAdmin(loggedInSubject)) {
        LOG.error("Subject " + GrouperUtil.subjectToString(loggedInSubject) + " cannot admin attribute definition: " + attributeDef.getName());
        guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message("simpleAttributeUpdate.errorCantEditAttributeDef", false)));
        return;
      }
      
      setupEditActionPanel(attributeUpdateRequestContainer, guiResponseJs, attributeDef,
          action);
      
    } finally {
      GrouperSession.stopQuietly(grouperSession); 
    }
  }

  /**
   * setup the edit action panel
   * @param attributeUpdateRequestContainer
   * @param guiResponseJs
   * @param attributeDef
   * @param action
   */
  private void setupEditActionPanel(
      AttributeUpdateRequestContainer attributeUpdateRequestContainer,
      GuiResponseJs guiResponseJs, AttributeDef attributeDef, String action) {
    
    if (attributeDef.getAttributeDefActionDelegate().allowedActions().size() < 2) {
      guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message("simpleAttributeUpdate.cantEditActionIfOnlyOne", false)));
      guiResponseJs.addAction(GuiScreenAction.newInnerHtml("#attributeActionEditPanel", null));
      return;
    }
    
    attributeUpdateRequestContainer.setAttributeDefToEdit(attributeDef);
    attributeUpdateRequestContainer.setAction(action);
    
    //find list which can imply
    AttributeAssignAction attributeAssignAction = attributeDef.getAttributeDefActionDelegate().findAction(action, true);
    {
      List<String> actionsWhichCanImply = new ArrayList<String>(attributeAssignAction.getAttributeAssignActionSetDelegate().getNewAttributeAssignActionNamesThatCanImplyThis());
      Collections.sort(actionsWhichCanImply);
      attributeUpdateRequestContainer.setNewActionsCanImply(actionsWhichCanImply);
    }
    
    {
      List<String> actionsWhichCanBeImplied = new ArrayList<String>(attributeAssignAction.getAttributeAssignActionSetDelegate().getNewAttributeAssignActionNamesThatCanBeImpliedByThis());
      Collections.sort(actionsWhichCanBeImplied);
      attributeUpdateRequestContainer.setNewActionsCanImpliedBy(actionsWhichCanBeImplied);
    }
    
    {
      List<String> actionsThatImplyThis = new ArrayList<String>(attributeAssignAction.getAttributeAssignActionSetDelegate().getAttributeAssignActionNamesThatImplyThis());
      Collections.sort(actionsThatImplyThis);
      attributeUpdateRequestContainer.setActionsThatImply(actionsThatImplyThis);
    }
    
    {
      List<String> actionsThatImplyThisImmediate = new ArrayList<String>(attributeAssignAction.getAttributeAssignActionSetDelegate().getAttributeAssignActionNamesThatImplyThisImmediate());
      Collections.sort(actionsThatImplyThisImmediate);
      attributeUpdateRequestContainer.setActionsThatImplyImmediate(actionsThatImplyThisImmediate);
    }
    
    {
      List<String> actionsImpliedByThis = new ArrayList<String>(attributeAssignAction.getAttributeAssignActionSetDelegate().getAttributeAssignActionNamesImpliedByThis());
      Collections.sort(actionsImpliedByThis);
      attributeUpdateRequestContainer.setActionsImpliedBy(actionsImpliedByThis);
    }
    
    {
      List<String> actionsImpliedByThisImmediate = new ArrayList<String>(attributeAssignAction.getAttributeAssignActionSetDelegate().getAttributeAssignActionNamesImpliedByThisImmediate());
      Collections.sort(actionsImpliedByThisImmediate);
      attributeUpdateRequestContainer.setActionsImpliedByImmediate(actionsImpliedByThisImmediate);
    }
    
    //set the actions panel
    guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#attributeActionEditPanel", 
      "/WEB-INF/grouperUi/templates/simpleAttributeUpdate/attributeActionEditPanel.jsp"));
    
    guiResponseJs.addAction(GuiScreenAction.newScript("guiScrollTo('#attributeActionsPanel');"));
  }

  /**
   * add an action that implied by the current action
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void addActionEditImpliedBy(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
  
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
  
    AttributeUpdateRequestContainer attributeUpdateRequestContainer = AttributeUpdateRequestContainer.retrieveFromRequestOrCreate();
  
    GrouperSession grouperSession = null;
  
    try {
      grouperSession = GrouperSession.start(loggedInSubject);
      
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
      AttributeDef attributeDef = null;
      
      String uuid = httpServletRequest.getParameter("attributeDefToEditId");
      
      if (StringUtils.isBlank(uuid)) {
        throw new RuntimeException("Why is uuid blank????");
      }
  
      String action = httpServletRequest.getParameter("action");
      
      if (StringUtils.isBlank(action)) {
        throw new RuntimeException("Why is action blank????");
      }
      
      //if editing, then this must be there, or it has been tampered with
      try {
        attributeDef = AttributeDefFinder.findById(uuid, true);
      } catch (Exception e) {
        LOG.info("Error searching for attribute def: " + uuid, e);
        guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message("simpleAttributeUpdate.errorCantEditAttributeDef", false)));
        return;
        
      }
      
      if (!attributeDef.getPrivilegeDelegate().canAttrAdmin(loggedInSubject)) {
        LOG.error("Subject " + GrouperUtil.subjectToString(loggedInSubject) + " cannot admin attribute definition: " + attributeDef.getName());
        guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message("simpleAttributeUpdate.errorCantEditAttributeDef", false)));
        return;
      }
      
      String actionAddImpliedBy = httpServletRequest.getParameter("actionAddImpliedBy");
      
      if (StringUtils.isBlank(actionAddImpliedBy)) {
        guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message("simpleAttributeUpdate.mustSelectAnActionToImpliedBy", false)));
        return;
      }
      
      AttributeAssignAction attributeAssignAction = attributeDef.getAttributeDefActionDelegate().allowedAction(action, true);
      AttributeAssignAction attributeAssignActionThatImpliedBy = attributeDef.getAttributeDefActionDelegate().allowedAction(actionAddImpliedBy, true);
      attributeAssignAction.getAttributeAssignActionSetDelegate().addToAttributeAssignActionSet(attributeAssignActionThatImpliedBy);
  
      guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message("simpleAttributeNameUpdate.successAddImpliedByAttributeDefName", false)));
  
      setupEditActionPanel(attributeUpdateRequestContainer, guiResponseJs, attributeDef,
          action);
      
    } finally {
      GrouperSession.stopQuietly(grouperSession); 
    }
  }

  /**
   * delete an action that is implied by the current action
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void deleteActionImpliedBy(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
  
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
  
    AttributeUpdateRequestContainer attributeUpdateRequestContainer = AttributeUpdateRequestContainer.retrieveFromRequestOrCreate();
  
    GrouperSession grouperSession = null;
  
    try {
      grouperSession = GrouperSession.start(loggedInSubject);
  
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
      AttributeDef attributeDef = null;
  
      String uuid = httpServletRequest.getParameter("attributeDefToEditId");
  
      if (StringUtils.isBlank(uuid)) {
        throw new RuntimeException("Why is uuid blank????");
      }
  
      String action = httpServletRequest.getParameter("action");
      
      if (StringUtils.isBlank(action)) {
        throw new RuntimeException("Why is action blank????");
      }
      
      //if editing, then this must be there, or it has been tampered with
      try {
        attributeDef = AttributeDefFinder.findById(uuid, true);
      } catch (Exception e) {
        LOG.info("Error searching for attribute def: " + uuid, e);
        guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message("simpleAttributeUpdate.errorCantEditAttributeDef", false)));
        return;
        
      }
      
      if (!attributeDef.getPrivilegeDelegate().canAttrAdmin(loggedInSubject)) {
        LOG.error("Subject " + GrouperUtil.subjectToString(loggedInSubject) + " cannot admin attribute definition: " + attributeDef.getName());
        guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message("simpleAttributeUpdate.errorCantEditAttributeDef", false)));
        return;
      }
      
      String actionImpliedBy = httpServletRequest.getParameter("actionImpliedBy");
      
      if (StringUtils.isBlank(actionImpliedBy)) {
        throw new RuntimeException("Why is actionImpliedBy blank?");
      }
      
      AttributeAssignAction attributeAssignAction = attributeDef.getAttributeDefActionDelegate().allowedAction(action, true);
      AttributeAssignAction attributeAssignActionImpliedBy = attributeDef.getAttributeDefActionDelegate().allowedAction(actionImpliedBy, true);
      attributeAssignAction.getAttributeAssignActionSetDelegate().removeFromAttributeAssignActionSet(attributeAssignActionImpliedBy);
  
      guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message("simpleAttributeUpdate.successRemoveImpliedByAction", false)));
  
      setupEditActionPanel(attributeUpdateRequestContainer, guiResponseJs, attributeDef,
          action);
      
    } finally {
      GrouperSession.stopQuietly(grouperSession); 
    }
  }

  /** logger */
  private static final Log LOG = LogFactory.getLog(SimpleAttributeUpdate.class);
  
  /**
   * the owner type drop down was changed
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void assignSelectOwnerType(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
  
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    AttributeUpdateRequestContainer attributeUpdateRequestContainer = AttributeUpdateRequestContainer.retrieveFromRequestOrCreate();
    
    String attributeAssignTypeString = httpServletRequest.getParameter("attributeAssignType");
    
    //clear out the assignments panel
    guiResponseJs.addAction(GuiScreenAction.newInnerHtml("#attributeAssignAssignments", ""));
    
    
    if (StringUtils.isBlank(attributeAssignTypeString)) {
      guiResponseJs.addAction(GuiScreenAction.newAlert(TagUtils.navResourceString("simpleAttributeAssign.requiredOwnerType")));

      //clear out the filter panels for generic and specific
      guiResponseJs.addAction(GuiScreenAction.newInnerHtml("#attributeAssignFilter", ""));
      
      return;
    }
    
    AttributeAssignType attributeAssignType = AttributeAssignType.valueOfIgnoreCase(attributeAssignTypeString, true);
    
    if (attributeAssignType.isAssignmentOnAssignment()) {
      throw new RuntimeException("Why is this an assignment on an assignment?");
    }
    
    attributeUpdateRequestContainer.setAttributeAssignType(attributeAssignType);

    //put in the generic panel that filters on attribute definitions
    guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#attributeAssignFilter", 
        "/WEB-INF/grouperUi/templates/simpleAttributeUpdate/simpleAttributeAssignFilter.jsp"));

    
  }

  /**
   * assign attribute and display the results
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void assignAttribute(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    AttributeUpdateRequestContainer attributeUpdateRequestContainer = AttributeUpdateRequestContainer.retrieveFromRequestOrCreate();

    GrouperSession grouperSession = null;

    try {
      grouperSession = GrouperSession.start(loggedInSubject);
      
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
      String attributeAssignTypeString = httpServletRequest.getParameter("attributeAssignType");
      
      if (StringUtils.isBlank(attributeAssignTypeString)) {
        throw new RuntimeException("Why is attributeAssignType blank???");
      }
      
      AttributeAssignType attributeAssignType = AttributeAssignType.valueOfIgnoreCase(attributeAssignTypeString, true);
      
      if (attributeAssignType.isAssignmentOnAssignment()) {
        throw new RuntimeException("Why is this an assignment on an assignment?");
      }
      
      attributeUpdateRequestContainer.setAttributeAssignType(attributeAssignType);

      String attributeAssignAttributeName = StringUtils.trimToNull(httpServletRequest.getParameter("attributeAssignAttributeName"));

      if (StringUtils.isBlank(attributeAssignAttributeName)) {
        
        guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message(
            "simpleAttributeUpdate.assignErrorAttributeNameRequired", false)));
        return;
      }
      AttributeDefName attributeDefName = AttributeDefNameFinder.findById(attributeAssignAttributeName, false);

      if (attributeDefName == null) {
        guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message(
            "simpleAttributeUpdate.assignErrorPickAttributeName", false)));
        return;
        
      }

      String attributeAssignGroup = null;
      String attributeAssignStem = null;
      String attributeAssignMember = null;
      String attributeAssignOwnerAttributeDef = null;

      AttributeAssignBaseDelegate attributeDelegate = null;

      Group group = null;
      Member member = null;
      if (attributeAssignType == AttributeAssignType.group 
          || attributeAssignType == AttributeAssignType.any_mem
          || attributeAssignType == AttributeAssignType.imm_mem) {
        
        attributeAssignGroup = attributeAssignType == AttributeAssignType.group
            ? StringUtils.trimToNull(httpServletRequest.getParameter("attributeAssignGroup"))
            : StringUtils.trimToNull(httpServletRequest.getParameter("attributeAssignMembershipGroup"));

        if (StringUtils.isBlank(attributeAssignGroup)) {
          
          guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message(
              "simpleAttributeUpdate.assignErrorGroupRequired", false)));
          return;
        }
        group = GroupFinder.findByUuid(grouperSession, attributeAssignGroup, false);
        if (group == null) {
          guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message(
              "simpleAttributeUpdate.assignErrorPickGroup", false)));
          return;
          
        }
      }
      
      if (attributeAssignType == AttributeAssignType.member
          || attributeAssignType == AttributeAssignType.imm_mem
          || attributeAssignType == AttributeAssignType.any_mem) {
        attributeAssignMember = attributeAssignType == AttributeAssignType.member 
          ? StringUtils.trimToNull(httpServletRequest.getParameter("attributeAssignMember"))
          : StringUtils.trimToNull(httpServletRequest.getParameter("attributeAssignMembershipSubject"));

        if (StringUtils.isBlank(attributeAssignMember)) {

          guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message(
              "simpleAttributeUpdate.assignErrorEntityRequired", false)));
          return;
        }

        Subject subject = GrouperUiUtils.findSubject(attributeAssignMember, false);
        if (subject == null) {
          guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message(
              "simpleAttributeUpdate.assignErrorPickSubject", false)));
          return;
        } else {
          member = MemberFinder.findBySubject(grouperSession, subject, true);
        }            

      }
      
      switch(attributeAssignType) {
        case group:
          
          attributeDelegate = group.getAttributeDelegate();
         
          break;
        case stem:
          attributeAssignStem = StringUtils.trimToNull(httpServletRequest.getParameter("attributeAssignStem"));
          if (StringUtils.isBlank(attributeAssignStem)) {
            
            guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message(
                "simpleAttributeUpdate.assignErrorStemRequired", false)));
            return;
          }
          Stem stem = StemFinder.findByUuid(grouperSession, attributeAssignStem, false);
          if (stem == null) {
            guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message(
                "simpleAttributeUpdate.assignErrorPickFolder", false)));
            return;
            
          }
          attributeDelegate = stem.getAttributeDelegate();
          break;
        case member:
          attributeDelegate = member.getAttributeDelegate();
          
          break;
        case attr_def:
          attributeAssignOwnerAttributeDef = StringUtils.trimToNull(httpServletRequest.getParameter("attributeAssignOwnerAttributeDef"));
          
          if (StringUtils.isBlank(attributeAssignOwnerAttributeDef)) {
            
            guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message(
                "simpleAttributeUpdate.assignErrorAttrDefRequired", false)));
            return;
          }
          AttributeDef attributeDef = AttributeDefFinder.findById(attributeAssignOwnerAttributeDef, false);
          if (attributeDef == null) {
            guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message(
                "simpleAttributeUpdate.assignErrorPickOwnerAttributeDef", false)));
            return;
            
          }
          attributeDelegate = attributeDef.getAttributeDelegate();

          break;
        case any_mem:

          {
            Set<Membership> memberships = GrouperUtil.nonNull(group.getMemberships(Group.getDefaultList(), GrouperUtil.toSet(member)));
            
            //we just need one
            if (GrouperUtil.length(memberships) == 0) {
              guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message(
                  "simpleAttributeUpdate.assignErrorMembershipRequired", false)));
              return;
            }
            
            attributeDelegate = new AttributeAssignEffMshipDelegate(group, member);
            
            break;
          }
        case imm_mem:
          {
            Set<Membership> memberships = GrouperUtil.nonNull(group.getMemberships(Group.getDefaultList(), GrouperUtil.toSet(member)));
            Membership membership = null;
            
            //if we are looking for an immediate membership, then look through for immediate
            for (Membership current : memberships) {
              if (current.isImmediate()) {
                membership = current;
                break;
              }
            }
            if (membership == null) {
              guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message(
                  "simpleAttributeUpdate.assignErrorMembershipRequired", false)));
              return;
              
            }
            attributeDelegate = membership.getAttributeDelegate();
            
            break;
          }
        default:
          throw new RuntimeException("Not expecting attributeAssignType: " + attributeAssignType);
      }
      boolean multiAssignable = attributeDefName.getAttributeDef().isMultiAssignable();
      if (!multiAssignable) {
        if (GrouperUtil.length(attributeDelegate.retrieveAssignments(attributeDefName)) > 0) {
          guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message(
              "simpleAttributeUpdate.assignErrorNotMultiAssign", false)));
          return;
        }
        
      }
      if (multiAssignable) {
        attributeDelegate.addAttribute(attributeDefName);
      } else {
        attributeDelegate.assignAttribute(attributeDefName);
      }
      guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message(
          "simpleAttributeUpdate.assignSuccess", false)));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession); 
    }

    assignFilter(httpServletRequest, httpServletResponse);
  }
  
   
  
  /**
   * submit the add metadata screen
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void assignMetadataAddSubmit(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();

    GrouperSession grouperSession = null;

    try {
      grouperSession = GrouperSession.start(loggedInSubject);
      
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();

      String attributeAssignId = httpServletRequest.getParameter("attributeAssignId");
      
      if (StringUtils.isBlank(attributeAssignId)) {
        throw new RuntimeException("Why is attributeAssignId blank???");
      }

      AttributeAssign attributeAssign = GrouperDAOFactory.getFactory().getAttributeAssign().findById(attributeAssignId, true, false);
      
      //now we need to check security
      if (!PrivilegeHelper.canAttrUpdate(grouperSession, attributeAssign.getAttributeDef(), loggedInSubject)) {
        
        String notAllowed = TagUtils.navResourceString("simpleAttributeAssign.assignEditNotAllowed");
        notAllowed = GrouperUiUtils.escapeHtml(notAllowed, true);
        guiResponseJs.addAction(GuiScreenAction.newAlert(notAllowed));
        return;
      }
      
      //todo check more security, e.g. where it is assigned
      
      {
        String attributeAssignAssignAttributeNameId = httpServletRequest.getParameter("attributeAssignAssignAttributeName");
        
        AttributeDefName attributeDefName = null;

        if (!StringUtils.isBlank(attributeAssignAssignAttributeNameId) ) {
          attributeDefName = AttributeDefNameFinder.findById(attributeAssignAssignAttributeNameId, false);
          
        }
        
        if (attributeDefName == null) {
          String required = TagUtils.navResourceString("simpleAttributeUpdate.assignMetadataAttributeNameRequired");
          required = GrouperUiUtils.escapeHtml(required, true);
          guiResponseJs.addAction(GuiScreenAction.newAlert(required));
          return;
          
        }
        
        if (attributeDefName.getAttributeDef().isMultiAssignable()) {
          
          attributeAssign.getAttributeDelegate().addAttribute(attributeDefName);
          
        } else {
          
          if (attributeAssign.getAttributeDelegate().hasAttribute(attributeDefName)) {
            
            String alreadyAssigned = TagUtils.navResourceString("simpleAttributeUpdate.assignMetadataAlreadyAssigned");
            alreadyAssigned = GrouperUiUtils.escapeHtml(alreadyAssigned, true);
            guiResponseJs.addAction(GuiScreenAction.newAlert(alreadyAssigned));
            return;
          }
          
          attributeAssign.getAttributeDelegate().assignAttribute(attributeDefName);

        }
        
      }
      
      String successMessage = TagUtils.navResourceString("simpleAttributeUpdate.assignMetadataAddSuccess");
      successMessage = GrouperUiUtils.escapeHtml(successMessage, true);
      guiResponseJs.addAction(GuiScreenAction.newAlert(successMessage));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession); 
    }

    assignFilter(httpServletRequest, httpServletResponse);
    
  }

  /**
   * submit the add value screen
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void assignAddValueSubmit(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();

    GrouperSession grouperSession = null;

    try {
      grouperSession = GrouperSession.start(loggedInSubject);
      
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();

      String attributeAssignId = httpServletRequest.getParameter("attributeAssignId");
      
      if (StringUtils.isBlank(attributeAssignId)) {
        throw new RuntimeException("Why is attributeAssignId blank???");
      }

      AttributeAssign attributeAssign = GrouperDAOFactory.getFactory().getAttributeAssign().findById(attributeAssignId, true, false);
      
      //now we need to check security
      if (!PrivilegeHelper.canAttrUpdate(grouperSession, attributeAssign.getAttributeDef(), loggedInSubject)) {
        //we are in a modal dialog, so we need to put up a native javascript alert
        String notAllowed = TagUtils.navResourceString("simpleAttributeAssign.assignEditNotAllowed");
        notAllowed = GrouperUiUtils.escapeJavascript(notAllowed, true);
        guiResponseJs.addAction(GuiScreenAction.newScript("alert('" + notAllowed + "');"));
        return;
      }
      
      //todo check more security, e.g. where it is assigned
      
      {
        String valueToAdd = httpServletRequest.getParameter("valueToAdd");
        
        if (StringUtils.isBlank(valueToAdd) ) {
          //we are in a modal dialog, so we need to put up a native javascript alert
          String required = TagUtils.navResourceString("simpleAttributeUpdate.addValueRequired");
          required = GrouperUiUtils.escapeJavascript(required, true);
          guiResponseJs.addAction(GuiScreenAction.newScript("alert('" + required + "');"));
          return;
          
        }
        
        
        attributeAssign.getValueDelegate().addValue(valueToAdd);
        
      }
      
      //close the modal dialog
      guiResponseJs.addAction(GuiScreenAction.newCloseModal());

      String successMessage = TagUtils.navResourceString("simpleAttributeUpdate.assignAddValueSuccess");
      successMessage = GrouperUiUtils.escapeHtml(successMessage, true);
      guiResponseJs.addAction(GuiScreenAction.newAlert(successMessage));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession); 
    }

    assignFilter(httpServletRequest, httpServletResponse);
    
  }

  /**
   * submit the edit value screen
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void assignValueEditSubmit(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();

    GrouperSession grouperSession = null;

    try {
      grouperSession = GrouperSession.start(loggedInSubject);
      
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();

      String attributeAssignId = httpServletRequest.getParameter("attributeAssignId");
      
      if (StringUtils.isBlank(attributeAssignId)) {
        throw new RuntimeException("Why is attributeAssignId blank???");
      }

      AttributeAssign attributeAssign = GrouperDAOFactory.getFactory().getAttributeAssign().findById(attributeAssignId, true, false);

      //now we need to check security
      try {
        attributeAssign.retrieveAttributeAssignable().getAttributeDelegate().assertCanUpdateAttributeDefName(attributeAssign.getAttributeDefName());
      } catch (InsufficientPrivilegeException e) {
        String notAllowed = TagUtils.navResourceString("simpleAttributeAssign.assignEditNotAllowed");
        notAllowed = GrouperUiUtils.escapeHtml(notAllowed, true);
        guiResponseJs.addAction(GuiScreenAction.newAlert(notAllowed));
        return;
      }
      
      String attributeAssignValueId = httpServletRequest.getParameter("attributeAssignValueId");
      
      if (StringUtils.isBlank(attributeAssignValueId)) {
        throw new RuntimeException("Why is attributeAssignValueId blank???");
      }

      AttributeAssignValue attributeAssignValue = GrouperDAOFactory.getFactory().getAttributeAssignValue().findById(attributeAssignValueId, true);
      

      
      {
        String valueToEdit = httpServletRequest.getParameter("valueToEdit");
        
        if (StringUtils.isBlank(valueToEdit) ) {
          String required = TagUtils.navResourceString("simpleAttributeUpdate.editValueRequired");
          required = GrouperUiUtils.escapeHtml(required, true);
          guiResponseJs.addAction(GuiScreenAction.newAlert(required));
          return;
          
        }
        
        
        attributeAssignValue.assignValue(valueToEdit);
        
        attributeAssignValue.saveOrUpdate();
        
      }
      
      //close the modal dialog
      guiResponseJs.addAction(GuiScreenAction.newCloseModal());

      String successMessage = TagUtils.navResourceString("simpleAttributeUpdate.assignEditValueSuccess");
      successMessage = GrouperUiUtils.escapeHtml(successMessage, true);
      guiResponseJs.addAction(GuiScreenAction.newAlert(successMessage));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession); 
    }

    assignFilter(httpServletRequest, httpServletResponse);
    
  }


  /**
   * submit the assign edit screen
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void assignEditSubmit(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();

    GrouperSession grouperSession = null;

    try {
      grouperSession = GrouperSession.start(loggedInSubject);
      
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();

      String attributeAssignId = httpServletRequest.getParameter("attributeAssignId");
      
      if (StringUtils.isBlank(attributeAssignId)) {
        throw new RuntimeException("Why is attributeAssignId blank???");
      }

      AttributeAssign attributeAssign = GrouperDAOFactory.getFactory().getAttributeAssign().findById(attributeAssignId, true, false);
      
      //now we need to check security
      if (!PrivilegeHelper.canAttrUpdate(grouperSession, attributeAssign.getAttributeDef(), loggedInSubject)) {
        
        String notAllowed = TagUtils.navResourceString("simpleAttributeAssign.assignEditNotAllowed");
        notAllowed = GrouperUiUtils.escapeHtml(notAllowed, true);
        guiResponseJs.addAction(GuiScreenAction.newAlert(notAllowed));
        return;
      }

      {
        String enabledDate = httpServletRequest.getParameter("enabledDate");
        
        if (StringUtils.isBlank(enabledDate) ) {
          attributeAssign.setEnabledTime(null);
        } else {
          //must be yyyy/mm/dd
          Timestamp enabledTimestamp = GrouperUtil.toTimestamp(enabledDate);
          attributeAssign.setEnabledTime(enabledTimestamp);
        }
      }
      
      {
        String disabledDate = httpServletRequest.getParameter("disabledDate");
  
        if (StringUtils.isBlank(disabledDate) ) {
          attributeAssign.setDisabledTime(null);
        } else {
          //must be yyyy/mm/dd
          Timestamp disabledTimestamp = GrouperUtil.toTimestamp(disabledDate);
          attributeAssign.setDisabledTime(disabledTimestamp);
        }
      }
      
      attributeAssign.saveOrUpdate();
      
      //close the modal dialog
      guiResponseJs.addAction(GuiScreenAction.newCloseModal());

      String successMessage = TagUtils.navResourceString("simpleAttributeUpdate.assignEditSuccess");
      successMessage = GrouperUiUtils.escapeHtml(successMessage, true);
      guiResponseJs.addAction(GuiScreenAction.newAlert(successMessage));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession); 
    }

    assignFilter(httpServletRequest, httpServletResponse);
    
  }

  
  
  /**
   * delete an attribute assignment value
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void assignValueDelete(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();

    GrouperSession grouperSession = null;

    try {
      grouperSession = GrouperSession.start(loggedInSubject);
      
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();

      String attributeAssignId = httpServletRequest.getParameter("attributeAssignId");
      
      if (StringUtils.isBlank(attributeAssignId)) {
        throw new RuntimeException("Why is attributeAssignId blank???");
      }

      AttributeAssign attributeAssign = AttributeAssignFinder.findById(attributeAssignId, true);

      //now we need to check security
      if (!PrivilegeHelper.canAttrUpdate(grouperSession, attributeAssign.getAttributeDef(), loggedInSubject)) {
        
        String notAllowed = TagUtils.navResourceString("simpleAttributeAssign.assignEditNotAllowed");
        notAllowed = GrouperUiUtils.escapeHtml(notAllowed, true);
        guiResponseJs.addAction(GuiScreenAction.newAlert(notAllowed));
        return;
      }
      
      //todo check more security, e.g. where it is assigned

      String attributeAssignValueId = httpServletRequest.getParameter("attributeAssignValueId");
      
      if (StringUtils.isBlank(attributeAssignValueId)) {
        throw new RuntimeException("Why is attributeAssignValueId blank???");
      }

      AttributeAssignValue attributeAssignValue = GrouperDAOFactory.getFactory().getAttributeAssignValue().findById(attributeAssignValueId, true);
      
      attributeAssign.getValueDelegate().deleteValue(attributeAssignValue);
      
      String successMessage = TagUtils.navResourceString("simpleAttributeUpdate.assignValueSuccessDelete");
      successMessage = GrouperUiUtils.escapeHtml(successMessage, true);
      guiResponseJs.addAction(GuiScreenAction.newAlert(successMessage));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession); 
    }

    assignFilter(httpServletRequest, httpServletResponse);
    
  }

  /**
   * delete an attribute assignment
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void assignDelete(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();

    GrouperSession grouperSession = null;

    try {
      grouperSession = GrouperSession.start(loggedInSubject);
      
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();

      String attributeAssignId = httpServletRequest.getParameter("attributeAssignId");
      
      if (StringUtils.isBlank(attributeAssignId)) {
        throw new RuntimeException("Why is attributeAssignId blank???");
      }

      AttributeAssign attributeAssign = AttributeAssignFinder.findById(attributeAssignId, true);
      
      // check security
      try {
        attributeAssign.retrieveAttributeAssignable().getAttributeDelegate().assertCanUpdateAttributeDefName(attributeAssign.getAttributeDefName());
      } catch (InsufficientPrivilegeException e) {
        String notAllowed = TagUtils.navResourceString("simpleAttributeAssign.assignEditNotAllowed");
        notAllowed = GrouperUiUtils.escapeHtml(notAllowed, true);
        guiResponseJs.addAction(GuiScreenAction.newAlert(notAllowed));
        return;
      }
      
      attributeAssign.delete();
      
      String successMessage = TagUtils.navResourceString("simpleAttributeUpdate.assignSuccessDelete");
      successMessage = GrouperUiUtils.escapeHtml(successMessage, true);
      guiResponseJs.addAction(GuiScreenAction.newAlert(successMessage));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession); 
    }

    assignFilter(httpServletRequest, httpServletResponse);
    
  }

  /**
   * edit an attribute assignment
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void assignEdit(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();

    AttributeUpdateRequestContainer attributeUpdateRequestContainer = AttributeUpdateRequestContainer.retrieveFromRequestOrCreate();

    GrouperSession grouperSession = null;

    try {
      grouperSession = GrouperSession.start(loggedInSubject);
      
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();

      //this is the attribute we are editing
      String attributeAssignId = httpServletRequest.getParameter("attributeAssignId");
      
      if (StringUtils.isBlank(attributeAssignId)) {
        throw new RuntimeException("Why is attributeAssignId blank???");
      }

      AttributeAssign attributeAssign = AttributeAssignFinder.findById(attributeAssignId, true);

      //we need the type so we know how to display it
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
        "/WEB-INF/grouperUi/templates/simpleAttributeUpdate/simpleAttributeAssignEdit.jsp"));

      
    } finally {
      GrouperSession.stopQuietly(grouperSession); 
    }

  }

  /**
   * filter attribute assignments and display the results
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void assignFilter(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    AttributeUpdateRequestContainer attributeUpdateRequestContainer = AttributeUpdateRequestContainer.retrieveFromRequestOrCreate();

    GrouperSession grouperSession = null;

    try {
      grouperSession = GrouperSession.start(loggedInSubject);
      
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
      String attributeAssignTypeString = httpServletRequest.getParameter("attributeAssignType");
      
      if (StringUtils.isBlank(attributeAssignTypeString)) {
        throw new RuntimeException("Why is attributeAssignType blank???");
      }
      
      AttributeAssignType attributeAssignType = AttributeAssignType.valueOfIgnoreCase(attributeAssignTypeString, true);
      
      if (attributeAssignType.isAssignmentOnAssignment()) {
        throw new RuntimeException("Why is this an assignment on an assignment?");
      }
      
      attributeUpdateRequestContainer.setAttributeAssignType(attributeAssignType);

      String attributeAssignAttributeDef = StringUtils.trimToNull(httpServletRequest.getParameter("attributeAssignAttributeDef"));
      String attributeAssignAttributeName = StringUtils.trimToNull(httpServletRequest.getParameter("attributeAssignAttributeName"));
      String attributeAssignGroup = null;
      String attributeAssignStem = null;
      String attributeAssignMember = null;
      String attributeAssignMemberId = null;
      String attributeAssignOwnerAttributeDef = null;

      if (attributeAssignType == AttributeAssignType.member 
          || attributeAssignType == AttributeAssignType.imm_mem 
          || attributeAssignType == AttributeAssignType.any_mem) {

        attributeAssignMember = attributeAssignType == AttributeAssignType.member 
          ? StringUtils.trimToNull(httpServletRequest.getParameter("attributeAssignMember"))
          : StringUtils.trimToNull(httpServletRequest.getParameter("attributeAssignMembershipSubject"));
        
        if (!StringUtils.isBlank(attributeAssignMember)) {
          
          Subject subject = GrouperUiUtils.findSubject(attributeAssignMember, false);
          if (subject == null) {
            guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message(
                "simpleAttributeUpdate.assignErrorPickSubject", false)));
            return;
          } else {
            Member member = MemberFinder.findBySubject(grouperSession, subject, true);
            attributeAssignMemberId = member.getUuid();
          }            
        }
        
      }
      
      switch(attributeAssignType) {
        case group:
          attributeAssignGroup = StringUtils.trimToNull(httpServletRequest.getParameter("attributeAssignGroup"));
          break;
        case stem:
          attributeAssignStem = StringUtils.trimToNull(httpServletRequest.getParameter("attributeAssignStem"));
          break;
        case member:
          
          break;
        case attr_def:
          attributeAssignOwnerAttributeDef = StringUtils.trimToNull(httpServletRequest.getParameter("attributeAssignOwnerAttributeDef"));
          break;
        case any_mem:
        case imm_mem:
          attributeAssignGroup = StringUtils.trimToNull(httpServletRequest.getParameter("attributeAssignMembershipGroup"));
          break;
        default:
          throw new RuntimeException("Not expecting attributeAssignType: " + attributeAssignType);
      }
      String attributeAssignMembershipId = null;
      
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
        attributeUpdateRequestContainer.setEnabledDisabled(enabledDisabledBoolean);
      }
      
      Set<AttributeAssign> attributeAssigns = GrouperDAOFactory.getFactory().getAttributeAssign().findAttributeAssignments(
          attributeAssignType, 
          attributeAssignAttributeDef, attributeAssignAttributeName, attributeAssignGroup, 
          attributeAssignStem, attributeAssignMemberId, attributeAssignOwnerAttributeDef, 
          attributeAssignMembershipId, 
          enabledDisabledBoolean, false);
      
      List<GuiAttributeAssign> guiAttributeAssigns = new ArrayList<GuiAttributeAssign>();
      
      for (AttributeAssign attributeAssign : attributeAssigns) {
        GuiAttributeAssign guiAttributeAssign = new GuiAttributeAssign();
        guiAttributeAssign.setAttributeAssign(attributeAssign);
        guiAttributeAssigns.add(guiAttributeAssign);
      }
      
      attributeUpdateRequestContainer.setGuiAttributeAssigns(guiAttributeAssigns);
      
      //set the privilege panel
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#attributeAssignAssignments", 
        "/WEB-INF/grouperUi/templates/simpleAttributeUpdate/simpleAttributeAssignments.jsp"));
      
      guiResponseJs.addAction(GuiScreenAction.newScript("guiScrollTo('#attributeAssignAssignments');"));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession); 
    }

  }


  /**
   * edit an attribute assignment value
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void assignValueEdit(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
  
    GrouperSession grouperSession = null;
  
    try {
      grouperSession = GrouperSession.start(loggedInSubject);
      
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
      String attributeAssignId = httpServletRequest.getParameter("attributeAssignId");
      
      if (StringUtils.isBlank(attributeAssignId)) {
        throw new RuntimeException("Why is attributeAssignId blank???");
      }

      AttributeAssign attributeAssign = AttributeAssignFinder.findById(attributeAssignId, true);
  
      //now we need to check security
      try {
        attributeAssign.retrieveAttributeAssignable().getAttributeDelegate().assertCanUpdateAttributeDefName(attributeAssign.getAttributeDefName());
      } catch (InsufficientPrivilegeException e) {
        String notAllowed = TagUtils.navResourceString("simpleAttributeAssign.assignEditNotAllowed");
        notAllowed = GrouperUiUtils.escapeHtml(notAllowed, true);
        guiResponseJs.addAction(GuiScreenAction.newAlert(notAllowed));
        return;
      }

      String attributeAssignValueId = httpServletRequest.getParameter("attributeAssignValueId");
      
      if (StringUtils.isBlank(attributeAssignValueId)) {
        throw new RuntimeException("Why is attributeAssignValueId blank???");
      }
  
      AttributeAssignValue attributeAssignValue = GrouperDAOFactory.getFactory().getAttributeAssignValue().findById(attributeAssignValueId, true);
      
      AttributeUpdateRequestContainer attributeUpdateRequestContainer = AttributeUpdateRequestContainer.retrieveFromRequestOrCreate();
      
      attributeUpdateRequestContainer.setAttributeAssignValue(attributeAssignValue);
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
          "/WEB-INF/grouperUi/templates/simpleAttributeUpdate/simpleAttributeAssignValueEdit.jsp"));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession); 
    }
  
  }


  /**
   * action graph
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void actionGraph(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
  
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
  
    AttributeUpdateRequestContainer attributeUpdateRequestContainer = AttributeUpdateRequestContainer.retrieveFromRequestOrCreate();
  
    GrouperSession grouperSession = null;
  
    try {
      grouperSession = GrouperSession.start(loggedInSubject);
      
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
      AttributeDef attributeDef = null;
      
      String uuid = httpServletRequest.getParameter("attributeDefToEditId");
      
      if (StringUtils.isBlank(uuid)) {
        throw new RuntimeException("Why is uuid blank????");
      }
  
      String action = httpServletRequest.getParameter("action");
      
      if (StringUtils.isBlank(action)) {
        throw new RuntimeException("Why is action blank????");
      }
      
      //if editing, then this must be there, or it has been tampered with
      try {
        attributeDef = AttributeDefFinder.findById(uuid, true);
      } catch (Exception e) {
        LOG.info("Error searching for attribute def: " + uuid, e);
        guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message("simpleAttributeUpdate.errorCantEditAttributeDef", false)));
        return;
        
      }
      
      if (!attributeDef.getPrivilegeDelegate().canAttrAdmin(loggedInSubject)) {
        LOG.error("Subject " + GrouperUtil.subjectToString(loggedInSubject) + " cannot admin attribute definition: " + attributeDef.getName());
        guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message("simpleAttributeUpdate.errorCantEditAttributeDef", false)));
        return;
      }
      
      attributeUpdateRequestContainer.setAttributeDefToEdit(attributeDef);
      attributeUpdateRequestContainer.setAction(action);
      
      //find list which can imply
      AttributeAssignAction attributeAssignAction = attributeDef.getAttributeDefActionDelegate().findAction(action, true);
      
      attributeUpdateRequestContainer.setActionGraphNodesFrom(new ArrayList<String>());
      attributeUpdateRequestContainer.setActionGraphNodesTo(new ArrayList<String>());
      attributeUpdateRequestContainer.setActionGraphStartingPoints(new ArrayList<String>());
      
      Set<AttributeAssignAction> allActionsOnGraph = new HashSet<AttributeAssignAction>();
      Set<AttributeAssignAction> actionsThatImplyThis = attributeAssignAction.getAttributeAssignActionSetDelegate().getAttributeAssignActionsThatImplyThis();
      Set<AttributeAssignAction> actionsImpliedByThis = attributeAssignAction.getAttributeAssignActionSetDelegate().getAttributeAssignActionsImpliedByThis();
      allActionsOnGraph.addAll(actionsThatImplyThis);
      allActionsOnGraph.addAll(actionsImpliedByThis);
      allActionsOnGraph.add(attributeAssignAction);

      //find out which ones are starting points
      Set<String> startingPoints = new HashSet<String>();
      for (AttributeAssignAction current : GrouperUtil.nonNull(actionsThatImplyThis)) {
        startingPoints.add(current.getName());
      }
      
      //if none then add current
      if (startingPoints.size() == 0) {
        startingPoints.add(attributeAssignAction.getName());
      }
      
      //find all relevant relationships
      for (AttributeAssignAction current : GrouperUtil.nonNull(allActionsOnGraph)) {
        
        Set<AttributeAssignAction> actionsImpliedByCurrentImmediate = current.getAttributeAssignActionSetDelegate().getAttributeAssignActionsImpliedByThisImmediate();

        for (AttributeAssignAction impliedBy : GrouperUtil.nonNull(actionsImpliedByCurrentImmediate)) {
          
          //make sure it is relevant
          if (!allActionsOnGraph.contains(impliedBy)) {
            continue;
          }

          //we know which ones arent starting points
          startingPoints.remove(impliedBy.getName());
          
          attributeUpdateRequestContainer.getActionGraphNodesFrom().add(current.getName());
          
          attributeUpdateRequestContainer.getActionGraphNodesTo().add(impliedBy.getName());
        }
        
      }

      if (startingPoints.size() == 0) {
        startingPoints.add(attributeAssignAction.getName());
      }
      
      attributeUpdateRequestContainer.getActionGraphStartingPoints().addAll(startingPoints);

      
      
      //set the actions panel
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#attributeActionEditPanel", 
        "/WEB-INF/grouperUi/templates/simpleAttributeUpdate/attributeActionGraphPanel.jsp"));
      
      guiResponseJs.addAction(GuiScreenAction.newScript("guiScrollTo('#attributeActionsPanel');"));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession); 
    }
  }
}
