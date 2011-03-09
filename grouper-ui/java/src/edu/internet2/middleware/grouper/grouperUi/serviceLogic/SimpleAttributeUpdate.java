/*
 * @author mchyzer
 * $Id: SimpleMembershipUpdate.java,v 1.7 2009-11-13 14:56:25 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.grouperUi.serviceLogic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
import edu.internet2.middleware.grouper.attr.AttributeDefType;
import edu.internet2.middleware.grouper.attr.AttributeDefValueType;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignAction;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefFinder;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiMember;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiSubject;
import edu.internet2.middleware.grouper.grouperUi.beans.attributeUpdate.AttributeUpdateRequestContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.attributeUpdate.AttributeUpdateSessionContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiResponseJs;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction;
import edu.internet2.middleware.grouper.internal.dao.QueryPaging;
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
        LOG.info("Subject " + GrouperUtil.subjectToString(loggedInSubject) + " cannot admin attribute definition: " + attributeDef.getName());
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
   * 
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
          LOG.info("Subject " + GrouperUtil.subjectToString(loggedInSubject) + " cannot admin attribute definition: " + attributeDef.getName());
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
    final AttributeUpdateSessionContainer attributeUpdateSessionContainer = new AttributeUpdateSessionContainer();
    attributeUpdateSessionContainer.storeToSession();

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
  
    //setup the container
    final AttributeUpdateSessionContainer attributeUpdateSessionContainer = new AttributeUpdateSessionContainer();
    attributeUpdateSessionContainer.storeToSession();

    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
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
  
    //setup the container
    final AttributeUpdateSessionContainer attributeUpdateSessionContainer = new AttributeUpdateSessionContainer();
    attributeUpdateSessionContainer.storeToSession();
  
    guiResponseJs.addAction(GuiScreenAction.newScript("document.title = '" 
        + GrouperUiUtils.message("simpleAttributeUpdate.addEditTitle", false) + "'"));
    guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#topDiv", 
        "/WEB-INF/grouperUi/templates/common/commonTop.jsp"));

    
    guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#bodyDiv", 
        "/WEB-INF/grouperUi/templates/simpleAttributeUpdate/simpleAttributeCreateEditInit.jsp"));
  
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
        LOG.info("Subject " + GrouperUtil.subjectToString(loggedInSubject) + " cannot admin attribute definition: " + attributeDef.getName());
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
      
      guiResponseJs.addAction(GuiScreenAction.newScript("guiScrollToBottom();"));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession); 
    }
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
        LOG.info("Subject " + GrouperUtil.subjectToString(loggedInSubject) + " cannot admin attribute definition: " + attributeDef.getName());
        guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message("simpleAttributeUpdate.errorCantEditAttributeDef", false)));
        return;
      }
      
      int privilegeListSize = TagUtils.mediaResourceInt("simpleAttributeUpdate.privilegeListSize", 50);
      
      Set<PrivilegeSubjectContainer> privilegeSubjectContainers = grouperSession
        .getAttributeDefResolver().retrievePrivileges(attributeDef, null, 
            null, QueryPaging.page(privilegeListSize, 1, false), null);
      
      attributeUpdateRequestContainer.setAttributeDefToEdit(attributeDef);
      attributeUpdateRequestContainer.setPrivilegeSubjectContainers(privilegeSubjectContainers);
      
      List<GuiMember> guiMembers = new ArrayList<GuiMember>();
      
      for (PrivilegeSubjectContainer privilegeSubjectContainer : privilegeSubjectContainers) {
        GuiMember guiMember = new GuiMember();
        guiMember.setGuiSubject(new GuiSubject(privilegeSubjectContainer.getSubject()));
        guiMembers.add(guiMember);
      }
      
      attributeUpdateRequestContainer.setPrivilegeSubjectContainerGuiMembers(guiMembers);
      
      //set the actions panel
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#attributePrivilegesPanel", 
        "/WEB-INF/grouperUi/templates/simpleAttributeUpdate/attributePrivilegesPanel.jsp"));
      
      guiResponseJs.addAction(GuiScreenAction.newScript("guiScrollToBottom();"));
      
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
        LOG.info("Subject " + GrouperUtil.subjectToString(loggedInSubject) + " cannot admin attribute definition: " + attributeDef.getName());
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
      
      guiResponseJs.addAction(GuiScreenAction.newScript("guiScrollToBottom();"));
      
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
        LOG.info("Subject " + GrouperUtil.subjectToString(loggedInSubject) + " cannot admin attribute definition: " + attributeDef.getName());
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
      
      guiResponseJs.addAction(GuiScreenAction.newScript("guiScrollToBottom();"));
      
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
        LOG.info("Subject " + GrouperUtil.subjectToString(loggedInSubject) + " cannot admin attribute definition: " + attributeDef.getName());
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
        LOG.info("Subject " + GrouperUtil.subjectToString(loggedInSubject) + " cannot admin attribute definition: " + attributeDef.getName());
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

      guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message("simpleAttributeUpdate.successAddImpliesAction", false)));

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
        LOG.info("Subject " + GrouperUtil.subjectToString(loggedInSubject) + " cannot admin attribute definition: " + attributeDef.getName());
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
    
    guiResponseJs.addAction(GuiScreenAction.newScript("guiScrollToBottom();"));
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
        LOG.info("Subject " + GrouperUtil.subjectToString(loggedInSubject) + " cannot admin attribute definition: " + attributeDef.getName());
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
  
      guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message("simpleAttributeUpdate.successAddImpliedByAction", false)));
  
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
        LOG.info("Subject " + GrouperUtil.subjectToString(loggedInSubject) + " cannot admin attribute definition: " + attributeDef.getName());
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
  
}
