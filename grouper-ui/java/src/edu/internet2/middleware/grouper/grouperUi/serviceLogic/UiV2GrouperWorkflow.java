package edu.internet2.middleware.grouper.grouperUi.serviceLogic;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.app.workflow.GrouperWorkflowConfig;
import edu.internet2.middleware.grouper.app.workflow.GrouperWorkflowConfigAttributeNames;
import edu.internet2.middleware.grouper.app.workflow.GrouperWorkflowConfigService;
import edu.internet2.middleware.grouper.app.workflow.GrouperWorkflowSettings;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiResponseJs;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction.GuiMessageType;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GrouperRequestContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GuiGrouperWorkflowConfig;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.TextContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.WorkflowContainer;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.subject.Subject;

public class UiV2GrouperWorkflow {
  
  /*
   * make sure attribute def is there and workflow is enabled
   */
  private boolean checkWorkflow() {
    
    final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();

    if (!GrouperWorkflowSettings.workflowEnabled()) {
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
          TextContainer.retrieveFromRequest().getText().get("workflowNotEnabledError")));
      return false;
    }

    AttributeDef attributeDefBase = null;
    try {
      
      attributeDefBase = GrouperWorkflowConfigAttributeNames.retrieveAttributeDefBaseDef();

    } catch (RuntimeException e) {
      if (attributeDefBase == null) {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("workflowAttributeNotFoundError")));
        return false;
      }
      throw e;
    }
    
    return true;
  }
  
  /**
   * view electronics form
   * @param request
   * @param response
   */
  public void viewForms(final HttpServletRequest request, final HttpServletResponse response) {

    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    Group group = null;
    
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      group = UiV2Group.retrieveGroupHelper(request, AccessPrivilege.READ).getGroup();
     
      if (group == null) {
        return;
      }
      
      final Group GROUP = group;
      
      final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
            
      final WorkflowContainer workflowContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getWorkflowContainer();
      
      //switch over to admin so attributes work
      GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        @Override
        public Object callback(GrouperSession theGrouperSession) throws GrouperSessionException {
          
          if (!checkWorkflow()) {
            return null;
          }
            
          List<GrouperWorkflowConfig> configs = GrouperWorkflowConfigService.getWorkflowConfigs(GROUP);
          
          workflowContainer.setGuiWorkflowConfigs(GuiGrouperWorkflowConfig.convertFromGrouperWorkflowConfigs(configs));
          guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
              "/WEB-INF/grouperUi2/workflow/groupWorkflowConfig.jsp"));
          
          return null;
        }
      });
      
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }
  
  /**
   * add form
   * @param request
   * @param response
   */
  public void formAdd(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    Group group = null;
    
    try {
      grouperSession = GrouperSession.start(loggedInSubject);
      
      group = UiV2Group.retrieveGroupHelper(request, AccessPrivilege.ADMIN).getGroup();
      
      if (group == null) {
        return;
      }
      
      final Group GROUP = group;
      
      final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
            
      final WorkflowContainer workflowContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getWorkflowContainer();
      
      if (!workflowContainer.isCanWrite()) {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("grouperWorkflowNotAllowedToAdd")));
        return;
      }
      
      //switch over to admin so attributes work
      GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        @Override
        public Object callback(GrouperSession theGrouperSession) throws GrouperSessionException {
          
          if (!checkWorkflow()) {
            return null;
          }
            
          GrouperWorkflowConfig workflowConfig = new GrouperWorkflowConfig();
          //TODO: add a constant
          workflowConfig.setWorkflowConfigType("grouper");
          workflowContainer.setGuiGrouperWorkflowConfig(GuiGrouperWorkflowConfig.convertFromGrouperWorkflowConfig(workflowConfig));
          
          guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
              "/WEB-INF/grouperUi2/workflow/groupWorkflowConfigAdd.jsp"));
          
          return null;
        }
      });
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }
  
  /**
   * add form submit
   * @param request
   * @param response
   */
  public void workflowConfigAddSubmit(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    Group group = null;
    
    try {
      grouperSession = GrouperSession.start(loggedInSubject);
      
      group = UiV2Group.retrieveGroupHelper(request, AccessPrivilege.ADMIN).getGroup();
      
      if (group == null) {
        return;
      }
      
      final Group GROUP = group;
      
      final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
            
      final WorkflowContainer workflowContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getWorkflowContainer();
      
      if (!workflowContainer.isCanWrite()) {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("grouperWorkflowNotAllowedToAdd")));
        return;
      }
      
      final GrouperWorkflowConfig workflowConfig = populateGrouperWorkflowConfig(request, response);
      
      //TODO validate workflow config
      
      //switch over to admin so attributes work
      GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        @Override
        public Object callback(GrouperSession theGrouperSession) throws GrouperSessionException {
          
          if (!checkWorkflow()) {
            return null;
          }
            
          GrouperWorkflowConfigService.saveOrUpdateGrouperWorkflowConfig(workflowConfig, GROUP);
          
          return null;
        }
      });
      
      guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2GrouperWorkflow.viewForms&groupId=" + group.getId() + "')"));
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success,
          TextContainer.retrieveFromRequest().getText().get("grouperWorkflowConfigSaveSuccess")));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }
  
  public void viewWorkflowConfigDetails(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    Group group = null;
    
    try {
      grouperSession = GrouperSession.start(loggedInSubject);
      
      group = UiV2Group.retrieveGroupHelper(request, AccessPrivilege.READ).getGroup();
      
      if (group == null) {
        return;
      }
      
      final Group GROUP = group;
      
      final String workflowConfigId = request.getParameter("workflowConfigId");
      
      if (StringUtils.isBlank(workflowConfigId)) {
        //TODO: show error message
      }
      
      final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
            
      final WorkflowContainer workflowContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getWorkflowContainer();
      
      //switch over to admin so attributes work
      GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        @Override
        public Object callback(GrouperSession theGrouperSession) throws GrouperSessionException {
          
          if (!checkWorkflow()) {
            return null;
          }
            
          GrouperWorkflowConfig workflowConfig = GrouperWorkflowConfigService.getWorkflowConfig(GROUP, workflowConfigId);
          workflowContainer.setGuiGrouperWorkflowConfig(GuiGrouperWorkflowConfig.convertFromGrouperWorkflowConfig(workflowConfig));
          
          return null;
        }
      });
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/workflow/grouperWorkflowConfigDetails.jsp"));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
  public void editWorkflowConfig(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    Group group = null;
    
    try {
      grouperSession = GrouperSession.start(loggedInSubject);
      
      group = UiV2Group.retrieveGroupHelper(request, AccessPrivilege.ADMIN).getGroup();
      
      if (group == null) {
        return;
      }
      
      final Group GROUP = group;
      
      final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
            
      final WorkflowContainer workflowContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getWorkflowContainer();
      
      if (!workflowContainer.isCanWrite()) {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("grouperWorkflowNotAllowedToAdd")));
        return;
      }
      
      final String workflowConfigId = request.getParameter("workflowConfigId");
      
      if (StringUtils.isBlank(workflowConfigId)) {
        //TODO: show error message
      }
      
      //switch over to admin so attributes work
      GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        @Override
        public Object callback(GrouperSession theGrouperSession) throws GrouperSessionException {
          
          if (!checkWorkflow()) {
            return null;
          }
            
          GrouperWorkflowConfig workflowConfig = GrouperWorkflowConfigService.getWorkflowConfig(GROUP, workflowConfigId);
          workflowContainer.setGuiGrouperWorkflowConfig(GuiGrouperWorkflowConfig.convertFromGrouperWorkflowConfig(workflowConfig));
          
          guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
              "/WEB-INF/grouperUi2/workflow/groupWorkflowConfigEdit.jsp"));
          
          return null;
        }
      });
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }
  
  /**
   * edit form submit
   * @param request
   * @param response
   */
  public void workflowConfigEditSubmit(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    Group group = null;
    
    try {
      grouperSession = GrouperSession.start(loggedInSubject);
      
      group = UiV2Group.retrieveGroupHelper(request, AccessPrivilege.ADMIN).getGroup();
      
      if (group == null) {
        return;
      }
      
      final Group GROUP = group;
      
      final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
            
      final WorkflowContainer workflowContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getWorkflowContainer();
      
      if (!workflowContainer.isCanWrite()) {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("grouperWorkflowNotAllowedToAdd")));
        return;
      }
      
      final GrouperWorkflowConfig workflowConfig = populateGrouperWorkflowConfig(request, response);
      
      //TODO validate workflow config
      
      //switch over to admin so attributes work
      GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        @Override
        public Object callback(GrouperSession theGrouperSession) throws GrouperSessionException {
          
          if (!checkWorkflow()) {
            return null;
          }
          
          GrouperWorkflowConfig config = GrouperWorkflowConfigService.getWorkflowConfig(GROUP, workflowConfig.getWorkflowConfigId());
          if (config == null) {
            throw new RuntimeException("workflow config id null.");
          }
            
          GrouperWorkflowConfigService.saveOrUpdateGrouperWorkflowConfig(workflowConfig, GROUP);
          
          return null;
        }
      });
      
      guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2GrouperWorkflow.viewForms&groupId=" + group.getId() + "')"));
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success,
          TextContainer.retrieveFromRequest().getText().get("grouperWorkflowConfigSaveSuccess")));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }
  
  /**
   * 
   */
  private GrouperWorkflowConfig populateGrouperWorkflowConfig(final HttpServletRequest request, final HttpServletResponse response) {
    
    String configType = request.getParameter("grouperWorkflowConfigType");
    String configName = request.getParameter("grouperWorkflowConfigName");
    String configId = request.getParameter("grouperWorkflowConfigId");
    String configDescription = request.getParameter("grouperWorkflowConfigDescription");
    String configApprovals = request.getParameter("grouperWorkflowConfigApprovals");
    String configParams = request.getParameter("grouperWorkflowConfigParams");
    String configForm = request.getParameter("grouperWorkflowConfigForm");
    String viewersGroupId = request.getParameter("grouperWorkflowConfigViewersGroupComboName");
    String sendEmail = request.getParameter("grouperWorkflowConfigSendEmail");
    String workflowEnabled = request.getParameter("grouperWorkflowConfigEnabled");
    
    GrouperWorkflowConfig workflowConfig = new GrouperWorkflowConfig();
    workflowConfig.setWorkflowConfigApprovals(configApprovals);
    workflowConfig.setWorkflowConfigId(configId);
    workflowConfig.setWorkflowConfigDescription(configDescription);
    workflowConfig.setWorkflowConfigEnabled(workflowEnabled);
    workflowConfig.setWorkflowConfigForm(configForm);
    workflowConfig.setWorkflowConfigName(configName);
    workflowConfig.setWorkflowConfigParams(configParams);
    workflowConfig.setWorkflowConfigType(configType);
    workflowConfig.setWorkflowConfigViewersGroupId(viewersGroupId);
    workflowConfig.setWorkflowConfigSendEmail(BooleanUtils.toBoolean(sendEmail));
    
    return workflowConfig;
    
  }

}
