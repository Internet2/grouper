package edu.internet2.middleware.grouper.grouperUi.serviceLogic;

import static edu.internet2.middleware.grouper.app.workflow.GrouperWorkflowApprovalState.INITIATE_STATE;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.app.workflow.GrouperWorkflowConfig;
import edu.internet2.middleware.grouper.app.workflow.GrouperWorkflowConfigAttributeNames;
import edu.internet2.middleware.grouper.app.workflow.GrouperWorkflowConfigParam;
import edu.internet2.middleware.grouper.app.workflow.GrouperWorkflowConfigParams;
import edu.internet2.middleware.grouper.app.workflow.GrouperWorkflowConfigService;
import edu.internet2.middleware.grouper.app.workflow.GrouperWorkflowInstance;
import edu.internet2.middleware.grouper.app.workflow.GrouperWorkflowInstanceService;
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

    
    //switch over to admin so attributes work
    Boolean workflowSetup = (Boolean)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      
      @Override
      public Boolean callback(GrouperSession theGrouperSession) throws GrouperSessionException {
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
    });
    
    
    return workflowSetup;
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
      
      if (!checkWorkflow()) {
        return;
      }
  
      group = UiV2Group.retrieveGroupHelper(request, AccessPrivilege.VIEW).getGroup();
     
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
  
    try {
      grouperSession = GrouperSession.start(loggedInSubject);
      
      if (!checkWorkflow()) {
        return;
      }
      
      Group group = UiV2Group.retrieveGroupHelper(request, AccessPrivilege.VIEW).getGroup();
      
      if (group == null) {
        return;
      }
      
      final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
            
      final WorkflowContainer workflowContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getWorkflowContainer();
      
      if (!workflowContainer.isCanConfigureWorkflow()) {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("grouperWorkflowNotAllowedToAdd")));
        return;
      }
      
      //switch over to admin so attributes work
      GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        @Override
        public Object callback(GrouperSession theGrouperSession) throws GrouperSessionException {
            
          GrouperWorkflowConfig workflowConfig = new GrouperWorkflowConfig();
          
          String workflowConfigType = request.getParameter("grouperWorkflowConfigType");
          
          if (StringUtils.isNotBlank(workflowConfigType)) {
            if (workflowContainer.getAllConfigTypes().contains(workflowConfigType)) {
              workflowConfig.setWorkflowConfigType(workflowConfigType);
            } else {
              throw new RuntimeException("Invalid workflow config type");
            }
          }
          
//          workflowConfig.setConfigParams(GrouperWorkflowConfig.getDefaultConfigParams());
//          workflowConfig.setWorkflowApprovalStates(GrouperWorkflowConfig.getDefaultApprovalStates());
          workflowConfig.setWorkflowConfigParamsString(GrouperWorkflowConfig.getDefaultConfigParamsString());
          workflowConfig.setWorkflowConfigApprovalsString(GrouperWorkflowConfig.getDefaultApprovalStatesString());
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
      
      if (!checkWorkflow()) {
        return;
      }

      grouperSession = GrouperSession.start(loggedInSubject);
      
      final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      
      final WorkflowContainer workflowContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getWorkflowContainer();
      
      if (!workflowContainer.isCanConfigureWorkflow()) {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("grouperWorkflowNotAllowedToAdd")));
        return;
      }
      
      group = UiV2Group.retrieveGroupHelper(request, AccessPrivilege.VIEW).getGroup();
      
      if (group == null) {
        return;
      }
      
      final Group GROUP = group;
      
      final GrouperWorkflowConfig workflowConfig = populateGrouperWorkflowConfig(request, response);
      
      if (workflowConfig == null) {
        return;
      }
      
      workflowContainer.setGuiGrouperWorkflowConfig(GuiGrouperWorkflowConfig.convertFromGrouperWorkflowConfig(workflowConfig));
      
      List<String> errors = workflowConfig.validate(GROUP, true);
      
      if (errors.size() > 0) {
        workflowContainer.setErrors(errors);
        guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
            "/WEB-INF/grouperUi2/workflow/groupWorkflowConfigAdd.jsp"));
        return;
      }
      
      //switch over to admin so attributes work
      GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        @Override
        public Object callback(GrouperSession theGrouperSession) throws GrouperSessionException {
          
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
      
      if (!checkWorkflow()) {
        return;
      }
      
      grouperSession = GrouperSession.start(loggedInSubject);
      
      group = UiV2Group.retrieveGroupHelper(request, AccessPrivilege.VIEW).getGroup();
      
      if (group == null) {
        return;
      }
      
      final Group GROUP = group;
      
      final String workflowConfigId = request.getParameter("workflowConfigId");
      
      if (StringUtils.isBlank(workflowConfigId)) {
        throw new RuntimeException("workflowConfigId cannot be blank");
      }
      
      final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
            
      final WorkflowContainer workflowContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getWorkflowContainer();
      
      //switch over to admin so attributes work
      GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        @Override
        public Object callback(GrouperSession theGrouperSession) throws GrouperSessionException {
            
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

      if (!checkWorkflow()) {
        return;
      }
      
      final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      
      final WorkflowContainer workflowContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getWorkflowContainer();
      
      if (!workflowContainer.isCanConfigureWorkflow()) {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("grouperWorkflowNotAllowedToAdd")));
        return;
      }
      
      grouperSession = GrouperSession.start(loggedInSubject);
      
      group = UiV2Group.retrieveGroupHelper(request, AccessPrivilege.VIEW).getGroup();
      
      if (group == null) {
        return;
      }
      
      final Group GROUP = group;
      
      final String workflowConfigId = request.getParameter("workflowConfigId");
      
      if (StringUtils.isBlank(workflowConfigId)) {
        throw new RuntimeException("workflowConfigId cannot be blank");
      }
      
      //switch over to admin so attributes work
      GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        @Override
        public Object callback(GrouperSession theGrouperSession) throws GrouperSessionException {
          
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
      
      if (!checkWorkflow()) {
        return;
      }
      
      final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      
      final WorkflowContainer workflowContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getWorkflowContainer();
      
      if (!workflowContainer.isCanConfigureWorkflow()) {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("grouperWorkflowNotAllowedToAdd")));
        return;
      }
      
      grouperSession = GrouperSession.start(loggedInSubject);
      
      group = UiV2Group.retrieveGroupHelper(request, AccessPrivilege.VIEW).getGroup();
      
      if (group == null) {
        return;
      }
      
      final Group GROUP = group;
      
      final GrouperWorkflowConfig workflowConfig = populateGrouperWorkflowConfig(request, response);
      
      if (workflowConfig == null) {
        return;
      }
      
      workflowContainer.setGuiGrouperWorkflowConfig(GuiGrouperWorkflowConfig.convertFromGrouperWorkflowConfig(workflowConfig));
      
      List<String> errors = workflowConfig.validate(GROUP, false);
      if (errors.size() > 0) {
        workflowContainer.setErrors(errors);
        guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
            "/WEB-INF/grouperUi2/workflow/groupWorkflowConfigEdit.jsp"));
        return;
      }
      
      //switch over to admin so attributes work
      GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        @Override
        public Object callback(GrouperSession theGrouperSession) throws GrouperSessionException {
                    
          GrouperWorkflowConfig config = GrouperWorkflowConfigService.getWorkflowConfig(GROUP, workflowConfig.getWorkflowConfigId());
          if (config == null) {
            throw new RuntimeException("workflow config is null for config id: "+workflowConfig.getWorkflowConfigId());
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
  
  public void showJoinGroupForm(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
    try {
      
      GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        @Override
        public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
          Group group = UiV2Group.retrieveGroupHelper(request, AccessPrivilege.VIEW, true).getGroup();
          GrouperWorkflowConfig workflowSubjectCanInitiate = workflowSubjectCanInitiate(group, loggedInSubject);
          
          if (workflowSubjectCanInitiate != null) {
            
            //check if there's already a pending request for this subject, group
            boolean requestAlreadySubmitted = GrouperWorkflowInstanceService.subjectAlreadySubmittedWorkflow(loggedInSubject, group);
            if (requestAlreadySubmitted) {
              guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.info, 
                  TextContainer.retrieveFromRequest().getText().get("workflowJoinGroupRequestAlreadyInProgress")));
              return null;
            }
            
            WorkflowContainer workflowContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getWorkflowContainer();
            workflowContainer.setGuiGrouperWorkflowConfig(GuiGrouperWorkflowConfig.convertFromGrouperWorkflowConfig(workflowSubjectCanInitiate));
            
            if (StringUtils.isNotBlank(workflowSubjectCanInitiate.getWorkflowConfigForm())) {
              workflowContainer.setHtmlForm(workflowSubjectCanInitiate.buildHtmlFromConfigForm(INITIATE_STATE));
            } else {
              String htmlForm = workflowSubjectCanInitiate.buildHtmlFromParams(false, INITIATE_STATE);
              workflowContainer.setHtmlForm(htmlForm);
            }
           
            guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId",
                "/WEB-INF/grouperUi2/workflow/groupJoinInitiateWorkflow.jsp"));
            return null;
          } else {
            throw new RuntimeException("trying to join group that's not accessible");
          }
          
        }
      });
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }
  
  public void workflowInitiateSubmit(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    try {
      
      if (!checkWorkflow()) {
        return;
      }
      
      grouperSession = GrouperSession.start(loggedInSubject);
      
      final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
            
      final String attributeAssignId = request.getParameter("workflowAttributeAssignId");
      
      if (StringUtils.isBlank(attributeAssignId)) {
        throw new RuntimeException("workflowAttributeAssignId cannot be blank");
      }
      
      //switch over to admin so attributes work
      GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        @Override
        public Object callback(GrouperSession theGrouperSession) throws GrouperSessionException {
          
          try {
           
            GrouperWorkflowConfig grouperWorkflowConfig = GrouperWorkflowConfigService.getWorkflowConfig(attributeAssignId);
            
            if (grouperWorkflowConfig == null) {
              throw new RuntimeException("No config found for attributeAssignId: "+attributeAssignId);
            }
            
            Group group = UiV2Group.retrieveGroupHelper(request, AccessPrivilege.VIEW, true).getGroup();
            
            GrouperWorkflowConfigParams configParams = grouperWorkflowConfig.getConfigParams();
            
            boolean subjectInAllowedGroup = grouperWorkflowConfig.canSubjectInitiateWorkflow(loggedInSubject);
            if (!subjectInAllowedGroup) {
              guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error,
                  TextContainer.retrieveFromRequest().getText().get("workflowInitiateNotAllowedError")));
              return null;
            }
            
            Map<GrouperWorkflowConfigParam, String> paramNamesValues = new LinkedHashMap<GrouperWorkflowConfigParam, String>();
            
            for (GrouperWorkflowConfigParam param: configParams.getParams()) {
              String paramName = param.getParamName();
              paramNamesValues.put(param, request.getParameter(paramName));
            }
            
            List<String> errors = GrouperWorkflowInstanceService.validateInitiateFormValues(paramNamesValues);
            if (errors.size() > 0) {
              //TODO move the following lines to a separate function and reuse
              WorkflowContainer workflowContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getWorkflowContainer();
              workflowContainer.setGuiGrouperWorkflowConfig(GuiGrouperWorkflowConfig.convertFromGrouperWorkflowConfig(grouperWorkflowConfig));
              
              if (StringUtils.isNotBlank(grouperWorkflowConfig.getWorkflowConfigForm())) {
                workflowContainer.setHtmlForm(grouperWorkflowConfig.buildHtmlFromConfigForm(INITIATE_STATE));
              } else {
                String htmlForm = grouperWorkflowConfig.buildHtmlFromParams(false, INITIATE_STATE);
                workflowContainer.setHtmlForm(htmlForm);
              }
              workflowContainer.setErrors(errors);
              guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId",
                  "/WEB-INF/grouperUi2/workflow/groupJoinInitiateWorkflow.jsp"));
              return null;
            }
            
            GrouperWorkflowInstanceService.saveInitiateStateInstance(grouperWorkflowConfig, loggedInSubject, 
                paramNamesValues, group);

            guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
                "/WEB-INF/grouperUi2/group/viewGroup.jsp"));
            
            guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success,
                TextContainer.retrieveFromRequest().getText().get("workflowInitiateSubmitSuccessMessage")));
          } catch (Exception e) {
            guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error,
                TextContainer.retrieveFromRequest().getText().get("workflowInitiateSubmitErrorMessage")));
          }
          
          return null;
        }
      });
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }
  
  public void forms(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    try {
      
      if (!checkWorkflow()) {
        return;
      }
      
      grouperSession = GrouperSession.start(loggedInSubject);
      
      final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
           
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/workflow/groupWorkflowMiscForm.jsp"));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
  public void formsWaitingForApproval(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    try {
      
      if (!checkWorkflow()) {
        return;
      }
      
      grouperSession = GrouperSession.start(loggedInSubject);
      
      final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      
      final WorkflowContainer workflowContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getWorkflowContainer();
      
      //switch over to admin so attributes work
      GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {

        @Override
        public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
          
          List<GrouperWorkflowInstance> instancesWaitingForApproval = GrouperWorkflowInstanceService.getWorkflowInstancesWaitingForApproval(loggedInSubject);
          workflowContainer.setInstancesWaitingForApproval(instancesWaitingForApproval);
          return null;
        }
        
      });
     
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/workflow/grouperWorkflowWaitingForApprovals.jsp"));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }
  
  public void viewInstance(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    try {
      
      if (!checkWorkflow()) {
        return;
      }
      
      grouperSession = GrouperSession.start(loggedInSubject);
      
      final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      
      final WorkflowContainer workflowContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getWorkflowContainer();
      
      final String attributeAssignId = request.getParameter("attributeAssignId");
      
      if (StringUtils.isBlank(attributeAssignId)) {
        throw new RuntimeException("attributeAssignId cannot be blank");
      }
      
      //switch over to admin so attributes work
      GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {

        @Override
        public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
          
          GrouperWorkflowInstance workfowInstance = GrouperWorkflowInstanceService.getWorkfowInstance(attributeAssignId);
          if (!GrouperWorkflowInstanceService.canInstanceBeViewed(workfowInstance, loggedInSubject)) {
            throw new RuntimeException("Operation not allowed");
          }
          
          workflowContainer.setWorkflowInstance(workfowInstance);
          workflowContainer.setHtmlForm(workfowInstance.htmlFormWithValues());
          return null;
        }
        
      });
     
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/workflow/grouperWorkflowViewInstance.jsp"));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }
  
  public void formsUserSubmitted(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    try {
      
      if (!checkWorkflow()) {
        return;
      }
      
      grouperSession = GrouperSession.start(loggedInSubject);
      
      final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      
      final WorkflowContainer workflowContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getWorkflowContainer();
      
      //switch over to admin so attributes work
      GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {

        @Override
        public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
          
          List<GrouperWorkflowInstance> instancesSubjectInitiated = GrouperWorkflowInstanceService.getWorkflowInstancesSubmitted(loggedInSubject);
          workflowContainer.setInstancesSubjectInitiated(instancesSubjectInitiated);
          return null;
        }
        
      });
     
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/workflow/grouperWorkflowInstancesSubjectInitiated.jsp"));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }
  
  
  public void workflowApprove(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    try {
      
      if (!checkWorkflow()) {
        return;
      }
      
      grouperSession = GrouperSession.start(loggedInSubject);
      
      final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      
      final WorkflowContainer workflowContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getWorkflowContainer();
      
      final String attributeAssignId = request.getParameter("attributeAssignId");
      
      if (StringUtils.isBlank(attributeAssignId)) {
        throw new RuntimeException("attributeAssignId cannot be blank");
      }
      
      //switch over to admin so attributes work
      GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {

        @Override
        public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
          
          //TODO check if the logged in subject can approve
          
          GrouperWorkflowInstance workfowInstance = GrouperWorkflowInstanceService.getWorkfowInstance(attributeAssignId);
          
          GrouperWorkflowConfigParams configParams = workfowInstance.getGrouperWorkflowConfig().getConfigParams();
          
          Map<GrouperWorkflowConfigParam, String> paramNamesValues = new HashMap<GrouperWorkflowConfigParam, String>();
          
          for (GrouperWorkflowConfigParam param: configParams.getParams()) {
            String paramName = param.getParamName();
            paramNamesValues.put(param, request.getParameter(paramName));
          }
          
          GrouperWorkflowInstanceService.approveWorkflow(workfowInstance, loggedInSubject, paramNamesValues);
          
          workflowContainer.setWorkflowInstance(workfowInstance);
          workflowContainer.setHtmlForm(workfowInstance.htmlFormWithValues());
          return null;
        }
        
      });
      
      guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2GrouperWorkflow.formsWaitingForApproval')"));
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success,
          TextContainer.retrieveFromRequest().getText().get("grouperWorkflowApproveSuccess")));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }
  
  public void workflowDisapprove(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    try {
      grouperSession = GrouperSession.start(loggedInSubject);
      
      final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      
      final WorkflowContainer workflowContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getWorkflowContainer();
      
      final String attributeAssignId = request.getParameter("attributeAssignId");
      
      if (StringUtils.isBlank(attributeAssignId)) {
        throw new RuntimeException("attributeAssignId cannot be blank");
      }
      
      //switch over to admin so attributes work
      GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {

        @Override
        public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
          
          //TODO check if the logged in subject can disapprove
          
          GrouperWorkflowInstance workfowInstance = GrouperWorkflowInstanceService.getWorkfowInstance(attributeAssignId);
          
          GrouperWorkflowConfigParams configParams = workfowInstance.getGrouperWorkflowConfig().getConfigParams();
          
          Map<String, String> paramNamesValues = new HashMap<String, String>();
          
          for (GrouperWorkflowConfigParam param: configParams.getParams()) {
            String paramName = param.getParamName();
            paramNamesValues.put(param.getParamName(), request.getParameter(paramName));
          }
          
          GrouperWorkflowInstanceService.disapproveWorkflow(workfowInstance, loggedInSubject, paramNamesValues);
          
          workflowContainer.setWorkflowInstance(workfowInstance);
          workflowContainer.setHtmlForm(workfowInstance.htmlFormWithValues());
          return null;
        }
        
      });
      
      guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2GrouperWorkflow.formsWaitingForApproval')"));
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success,
          TextContainer.retrieveFromRequest().getText().get("grouperWorkflowApproveSuccess")));
      
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
    workflowConfig.setWorkflowConfigParamsString(configParams);
    workflowConfig.setWorkflowConfigApprovalsString(configApprovals);
    workflowConfig.setWorkflowConfigId(configId);
    workflowConfig.setWorkflowConfigDescription(configDescription != null ? configDescription.trim(): null);
    workflowConfig.setWorkflowConfigEnabled(workflowEnabled);
    workflowConfig.setWorkflowConfigForm(configForm != null ? configForm.trim(): null);
    workflowConfig.setWorkflowConfigName(configName);
    workflowConfig.setWorkflowConfigType(configType);
    workflowConfig.setWorkflowConfigViewersGroupId(viewersGroupId);
    workflowConfig.setWorkflowConfigSendEmail(BooleanUtils.toBoolean(sendEmail));
    
    return workflowConfig;
    
  }
  
  @SuppressWarnings("unchecked")
  private GrouperWorkflowConfig workflowSubjectCanInitiate(final Group group, final Subject subject) {
    
    List<GrouperWorkflowConfig> workflowConfigs = (List<GrouperWorkflowConfig>) GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      
      public Object callback(GrouperSession theGrouperSession) throws GrouperSessionException {
        
        List<GrouperWorkflowConfig> workflowConfigs = GrouperWorkflowConfigService.getWorkflowConfigs(group);
        
        return workflowConfigs;
      }
    });
    
    if (workflowConfigs.size() > 0) {
      
      Collections.sort(workflowConfigs, new Comparator<GrouperWorkflowConfig>() {
        @Override
        public int compare(GrouperWorkflowConfig o1, GrouperWorkflowConfig o2) {
          return o1.getWorkflowConfigName().compareTo(o2.getWorkflowConfigName());
        }
      });
      
      for (GrouperWorkflowConfig workflowConfig: workflowConfigs) {
        if (workflowConfig.canSubjectInitiateWorkflow(subject)) {
          return workflowConfig;
        }
      }
    } 
    
    return null;
    
  }

}
