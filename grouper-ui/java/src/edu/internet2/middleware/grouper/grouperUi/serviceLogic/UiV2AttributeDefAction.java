package edu.internet2.middleware.grouper.grouperUi.serviceLogic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignAction;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefFinder;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiAttributeAssign;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiAttributeDef;
import edu.internet2.middleware.grouper.grouperUi.beans.attributeUpdate.AttributeUpdateRequestContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiResponseJs;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction.GuiMessageType;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.AttributeDefContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GrouperRequestContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.TextContainer;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.privs.AttributeDefPrivilege;
import edu.internet2.middleware.grouper.subj.SubjectHelper;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.ui.util.GrouperUiUtils;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * @author vsachdeva
 */
public class UiV2AttributeDefAction {
  
  /** logger */
  protected static final Log LOG = LogFactory.getLog(UiV2AttributeDefAction.class);
  
  /**
   * save attribute def action from edit screen
   * @param request
   * @param response
   */
  public void attributeDefActionEditSubmit(HttpServletRequest request, HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
  
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
    GrouperSession grouperSession = null;
  
    AttributeAssignAction attributeAssignAction = null;
    
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
      
      String attributeAssignActionId = request.getParameter("attributeDefActionId");
      
      attributeAssignAction = GrouperDAOFactory.getFactory().getAttributeAssignAction().findById(attributeAssignActionId, true);
      
      if (attributeAssignAction == null) {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("attributeDefActionNotFoundError")));
        return;
      }
      
      AttributeDef attributeDef = attributeAssignAction.getAttributeDef();
      
      if (!attributeDef.getPrivilegeDelegate().canAttrAdmin(loggedInSubject)) {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("simpleAttributeUpdate.errorCantEditAttributeDef")));
        return;
      }
      
      String[] actionsThatImply = request.getParameterValues("actionsThatImmediatelyImply[]");
      String[] actionImpliedBy = request.getParameterValues("actionsImpliedByImmediate[]");
      
      if (actionsThatImply != null) {
        for (AttributeAssignAction attributeAssignActionThatImply:  attributeAssignAction.getAttributeAssignActionSetDelegate().getAttributeAssignActionsThatImplyThis()) {        
          attributeAssignActionThatImply.getAttributeAssignActionSetDelegate().removeFromAttributeAssignActionSet(attributeAssignAction);
        }
        
        for (String actionAddImply: actionsThatImply) {
          AttributeAssignAction attributeAssignActionThatImplies = attributeDef.getAttributeDefActionDelegate().allowedAction(actionAddImply, true);
          attributeAssignActionThatImplies.getAttributeAssignActionSetDelegate().addToAttributeAssignActionSet(attributeAssignAction);        
        }
      }
      
      if (actionImpliedBy != null) {
        for (AttributeAssignAction attributeAssignActionImpliedBy:  attributeAssignAction.getAttributeAssignActionSetDelegate().getAttributeAssignActionsImpliedByThis()) {        
          attributeAssignAction.getAttributeAssignActionSetDelegate().removeFromAttributeAssignActionSet(attributeAssignActionImpliedBy);
        }
        
        for (String actionImpliedByAdd: actionImpliedBy) {        
          AttributeAssignAction attributeAssignActionThatImpliedBy = attributeDef.getAttributeDefActionDelegate().allowedAction(actionImpliedByAdd, true);        
          attributeAssignAction.getAttributeAssignActionSetDelegate().addToAttributeAssignActionSet(attributeAssignActionThatImpliedBy);
        }
      }
      
      //go to the view attribute def screen
      guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2AttributeDefAction.attributeDefActions?attributeDefId=" + attributeDef.getId() + "')"));

      //lets show a success message on the new screen
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
          TextContainer.retrieveFromRequest().getText().get("attributeDefActionEditSuccess")));

    } catch (Exception e) {
      LOG.warn("Error editing attribute def action: " + SubjectHelper.getPretty(loggedInSubject) + ", " + attributeAssignAction.getName(), e);
      if (GrouperUiUtils.vetoHandle(guiResponseJs, e)) {
        return;
      }
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
          TextContainer.retrieveFromRequest().getText().get("attributeDefActionEditError") 
          + ": " + GrouperUtil.xmlEscape(e.getMessage(), true)));
      return;
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
  /**
   * show screen to add new attribute def action
   * @param request
   * @param response
   */
  public void newAttributeDefAction(HttpServletRequest request, HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);

      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      
      String attributeDefId = request.getParameter("attributeDefId");
      
      if (StringUtils.isNotBlank(attributeDefId)) {
        AttributeDef attributeDef = AttributeDefFinder.findById(attributeDefId, false);
        if (attributeDef == null) {
          guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
              TextContainer.retrieveFromRequest().getText().get("attributeDefCantFindAttributeDef")));
          return;
        }
        
        GrouperRequestContainer.retrieveFromRequestOrCreate().getAttributeDefContainer()
        .setObjectAttributeDefId(attributeDef.getId());
        
      }
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/attributeDefAction/newAttributeDefAction.jsp"));
  
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
  /**
   * save new attribute def action
   * @param request
   * @param response
   */
  public void newAttributeDefActionSubmit(HttpServletRequest request, HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
  
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
    GrouperSession grouperSession = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
      
      String attributeDefId = request.getParameter("attributeDefComboName");
      String action = request.getParameter("action");
      
      if (StringUtils.isBlank(attributeDefId)) {
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error,
            "#attributeDefComboErrorId",
            TextContainer.retrieveFromRequest().getText().get("attributeDefActionCreateRequiredAttributeDef")));
        return;
      }
      
      if (StringUtils.isBlank(action)) {
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error,
            "#action",
            TextContainer.retrieveFromRequest().getText().get("attributeDefActionCreateRequiredAction")));
        return;
      }
      
      AttributeDef attributeDef = AttributeDefFinder.findById(attributeDefId, false);
      if (attributeDef == null) {
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error,
            "#attributeDefComboErrorId",
            TextContainer.retrieveFromRequest().getText().get("attributeDefActionCreateRequiredAttributeDef")));
        return;
      }
         
      if (!attributeDef.getPrivilegeDelegate().canAttrAdmin(loggedInSubject)) {
        LOG.error("Subject " + GrouperUtil.subjectToString(loggedInSubject) + " cannot admin attribute definition: " + attributeDef.getName());
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, GrouperUiUtils.message("simpleAttributeUpdate.errorCantEditAttributeDef", false)));
        return;
      }
      
      attributeDef.getAttributeDefActionDelegate().addAction(action);
      
      //go to the view attribute def screen
      guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2AttributeDefAction.attributeDefActions?attributeDefId=" + attributeDef.getId() + "')"));

      //lets show a success message on the new screen
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
          TextContainer.retrieveFromRequest().getText().get("attributeDefActionCreateSuccess")));

    } catch (Exception e) {
      LOG.warn("Error creating attribute def actions: " + SubjectHelper.getPretty(loggedInSubject), e);
      if (GrouperUiUtils.vetoHandle(guiResponseJs, e)) {
        return;
      }
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
          TextContainer.retrieveFromRequest().getText().get("attributeDefActionCreateError") 
          + ": " + GrouperUtil.xmlEscape(e.getMessage(), true)));
      return;
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
  /**
   * the filter button on attribute def actions screen was pressed
   * @param request
   * @param response
   */
  public void filterAction(HttpServletRequest request, HttpServletResponse response) {
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
        
    GrouperSession grouperSession = null;

    AttributeDef attributeDef = null;

    try {
      grouperSession = GrouperSession.start(loggedInSubject);
  
      attributeDef = UiV2AttributeDef.retrieveAttributeDefHelper(request, AttributeDefPrivilege.ATTR_VIEW, true).getAttributeDef();
      
      if (attributeDef == null) {
        return;
      }
      
      
      GrouperRequestContainer grouperRequestContainer = GrouperRequestContainer.retrieveFromRequestOrCreate();
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      
      String filterText = request.getParameter("filterText");
      AttributeDefContainer attributeDefContainer = grouperRequestContainer.getAttributeDefContainer();
      
      Set<AttributeAssignAction> allowedActions = attributeDef.getAttributeDefActionDelegate().allowedActions();
      
      Set<AttributeAssignAction> filteredActions = new HashSet<AttributeAssignAction>();
      
      if (StringUtils.isNotBlank(filterText)) {
        String filterValue = filterText.toLowerCase();
        for (AttributeAssignAction attributeAssignAction: allowedActions) {
          if (attributeAssignAction.getName().toLowerCase().contains(filterValue)) {
            filteredActions.add(attributeAssignAction);
          }
        }
      } else {
        filteredActions.addAll(allowedActions);
      }
      
      attributeDefContainer.setAttributeAssignActions(filteredActions);
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#attributeDefFilterResultsId", 
          "/WEB-INF/grouperUi2/attributeDefAction/attributeDefActions.jsp"));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }

  /**
   * view attribute def actions
   * @param request
   * @param response
   */
  public void attributeDefActions(HttpServletRequest request, HttpServletResponse response) {
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
        
    GrouperSession grouperSession = null;

    AttributeDef attributeDef = null;

    try {
      grouperSession = GrouperSession.start(loggedInSubject);
  
      attributeDef = UiV2AttributeDef.retrieveAttributeDefHelper(request, AttributeDefPrivilege.ATTR_VIEW, true).getAttributeDef();
      
      if (attributeDef == null) {
        return;
      }
      
      GrouperRequestContainer grouperRequestContainer = GrouperRequestContainer.retrieveFromRequestOrCreate();
      
      AttributeDefContainer attributeDefContainer = grouperRequestContainer.getAttributeDefContainer();
  
      Set<AttributeAssignAction> allowedActions = attributeDef.getAttributeDefActionDelegate().allowedActions();
      
      attributeDefContainer.setAttributeAssignActions(allowedActions);
      
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/attributeDefAction/attributeDefActionsPage.jsp"));
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#attributeDefFilterResultsId", 
          "/WEB-INF/grouperUi2/attributeDefAction/attributeDefActions.jsp"));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }
  
  /**
   * delete selected attribute def actions button clicked
   * @param request
   * @param response
   */
  public void deleteAttributeDefActions(HttpServletRequest request, HttpServletResponse response) {
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;

    AttributeDef attributeDef = null;

    try {
      grouperSession = GrouperSession.start(loggedInSubject);
  
      attributeDef = UiV2AttributeDef.retrieveAttributeDefHelper(request, AttributeDefPrivilege.ATTR_VIEW, true).getAttributeDef();
      
      if (attributeDef == null) {
        return;
      }
      
      GrouperRequestContainer grouperRequestContainer = GrouperRequestContainer.retrieveFromRequestOrCreate();
      
      AttributeDefContainer attributeDefContainer = grouperRequestContainer.getAttributeDefContainer();
      
      Set<String> attributeAssignActionIds = new HashSet<String>();
      
      for (int i=0;i<1000;i++) {
        String attributeAssignActionId = request.getParameter("attributeDefAction_" + i + "[]");
        if (!StringUtils.isBlank(attributeAssignActionId)) {
          attributeAssignActionIds.add(attributeAssignActionId);
        }
      }
      
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      
      if (attributeAssignActionIds.size() == 0) {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("attributeDefRemoveNoActionsSelects")));
        return;
      }
      
      if (!attributeDef.getPrivilegeDelegate().canAttrAdmin(loggedInSubject)) {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("simpleAttributeUpdate.errorCantEditAttributeDef")));
        return;
      }
      
      if (attributeDef.getAttributeDefActionDelegate().allowedActions().size() == attributeAssignActionIds.size()) {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("simpleAttributeUpdate.cantDeleteAllActions")));
        return;
      }
      
      Set<AttributeAssignAction> allowedActions = attributeDef.getAttributeDefActionDelegate().allowedActions();
      Set<AttributeAssignAction> remaningActions = new HashSet<AttributeAssignAction>();
      for (AttributeAssignAction attributeAssignAction: allowedActions) {
        if (attributeAssignActionIds.contains(attributeAssignAction.getId())) {
          attributeDef.getAttributeDefActionDelegate().removeAction(attributeAssignAction.getName());
        } else {
          remaningActions.add(attributeAssignAction);
        }
      }
      
      attributeDefContainer.setAttributeAssignActions(remaningActions);
      
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
          TextContainer.retrieveFromRequest().getText().get("attributeDefRemoveActionsSuccess")));
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#attributeDefFilterResultsId", 
          "/WEB-INF/grouperUi2/attributeDefAction/attributeDefActions.jsp"));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }
  
  
  /**
   * delete one attribute def action
   * @param request
   * @param response
   */
  public void deleteAttributeDefAction(HttpServletRequest request, HttpServletResponse response) {
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;

    AttributeDef attributeDef = null;

    try {
      grouperSession = GrouperSession.start(loggedInSubject);
  
      attributeDef = UiV2AttributeDef.retrieveAttributeDefHelper(request, AttributeDefPrivilege.ATTR_VIEW, true).getAttributeDef();
      
      if (attributeDef == null) {
        return;
      }
      
      GrouperRequestContainer grouperRequestContainer = GrouperRequestContainer.retrieveFromRequestOrCreate();
      
      AttributeDefContainer attributeDefContainer = grouperRequestContainer.getAttributeDefContainer();
      
      String attributeAssignActionId = request.getParameter("attributeDefActionId");
      
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      
      if (!attributeDef.getPrivilegeDelegate().canAttrAdmin(loggedInSubject)) {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("simpleAttributeUpdate.errorCantEditAttributeDef")));
        return;
      }
      
      if (attributeDef.getAttributeDefActionDelegate().allowedActions().size() == 1) {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("simpleAttributeUpdate.cantDeleteLastAction")));
        return;
      }
      
      Set<AttributeAssignAction> allowedActions = attributeDef.getAttributeDefActionDelegate().allowedActions();
      
      Set<AttributeAssignAction> remaningActions = new HashSet<AttributeAssignAction>();
      
      boolean found = false;
      
      for (AttributeAssignAction attributeAssignAction: allowedActions) {
        if (attributeAssignAction.getId().equals(attributeAssignActionId)) {
          attributeDef.getAttributeDefActionDelegate().removeAction(attributeAssignAction.getName());
          found = true;
        } else {
          remaningActions.add(attributeAssignAction);
        }
      }
      
      if (!found) {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("attributeDefRemoveNoActionNotFound")));
        return;
      }
      
      attributeDefContainer.setAttributeAssignActions(remaningActions);
      
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
          TextContainer.retrieveFromRequest().getText().get("attributeDefRemoveActionSuccess")));
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#attributeDefFilterResultsId", 
          "/WEB-INF/grouperUi2/attributeDefAction/attributeDefActions.jsp"));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }
  
  /**
   * show edit attribute def action screen
   * @param request
   * @param response
   */
  public void editAttributeDefAction(HttpServletRequest request, HttpServletResponse response) {
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    try {
      grouperSession = GrouperSession.start(loggedInSubject);
  
      AttributeDef attributeDef = UiV2AttributeDef.retrieveAttributeDefHelper(request, AttributeDefPrivilege.ATTR_ADMIN, true).getAttributeDef();
      
      if (attributeDef == null) {
        return;
      }
  
      String attributeAssignActionId = request.getParameter("attributeDefActionId");
      
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      
      if (!attributeDef.getPrivilegeDelegate().canAttrAdmin(loggedInSubject)) {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("simpleAttributeUpdate.errorCantEditAttributeDef")));
        return;
      }
      AttributeAssignAction attributeAssignAction = GrouperDAOFactory.getFactory().getAttributeAssignAction().findById(attributeAssignActionId, true);
      
      AttributeUpdateRequestContainer attributeUpdateRequestContainer = AttributeUpdateRequestContainer.retrieveFromRequestOrCreate();
      
      if (attributeDef.getAttributeDefActionDelegate().allowedActions().size() < 2) {
        guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message("simpleAttributeUpdate.cantEditActionIfOnlyOne", false)));
        guiResponseJs.addAction(GuiScreenAction.newInnerHtml("#attributeActionEditPanel", null));
        return;
      }
      
      
      attributeUpdateRequestContainer.setAttributeDefToEdit(attributeDef);
      attributeUpdateRequestContainer.setGuiAttributeDefToEdit(new GuiAttributeDef(attributeDef));
      attributeUpdateRequestContainer.setAction(attributeAssignAction.getName());
      attributeUpdateRequestContainer.setAttributeAssignAction(attributeAssignAction);
      
      attributeUpdateRequestContainer.setGuiAttributeAssign(new GuiAttributeAssign());
      
      attributeUpdateRequestContainer.setActions(new ArrayList<String>(attributeAssignAction.getAttributeDef().getAttributeDefActionDelegate().allowedActionStrings()));
      
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
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/attributeDefAction/attributeDefActionEdit.jsp"));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  

}