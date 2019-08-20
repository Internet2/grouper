package edu.internet2.middleware.grouper.grouperUi.serviceLogic;

import static edu.internet2.middleware.grouper.app.workflow.GrouperWorkflowConstants.INITIATE_STATE;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.app.workflow.GrouperWorkflowApprovalStates;
import edu.internet2.middleware.grouper.app.workflow.GrouperWorkflowConfig;
import edu.internet2.middleware.grouper.app.workflow.GrouperWorkflowConfigAttributeNames;
import edu.internet2.middleware.grouper.app.workflow.GrouperWorkflowConfigParam;
import edu.internet2.middleware.grouper.app.workflow.GrouperWorkflowConfigParams;
import edu.internet2.middleware.grouper.app.workflow.GrouperWorkflowConfigService;
import edu.internet2.middleware.grouper.app.workflow.GrouperWorkflowConfigValidator;
import edu.internet2.middleware.grouper.app.workflow.GrouperWorkflowInstance;
import edu.internet2.middleware.grouper.app.workflow.GrouperWorkflowInstanceService;
import edu.internet2.middleware.grouper.app.workflow.GrouperWorkflowInstanceValidator;
import edu.internet2.middleware.grouper.app.workflow.GrouperWorkflowSettings;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiResponseJs;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction.GuiMessageType;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GrouperRequestContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GuiGrouperWorkflowConfig;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GuiGrouperWorkflowInstance;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.TextContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.WorkflowContainer;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.subject.Subject;

public class UiV2GrouperWorkflow {
  
  /**
   * view workflows configured on a group
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
          
          List<GrouperWorkflowConfig> configsSubjectCanView = new ArrayList<GrouperWorkflowConfig>();
          
          for (GrouperWorkflowConfig workflowConfig: GrouperWorkflowConfigService.getWorkflowConfigs(GROUP)) {
            if (GrouperWorkflowConfigService.canSubjectViewWorkflow(GROUP, loggedInSubject)) {
              configsSubjectCanView.add(workflowConfig);
            }
          }
          
          workflowContainer.setGuiWorkflowConfigs(GuiGrouperWorkflowConfig.convertFromGrouperWorkflowConfigs(configsSubjectCanView));
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
   * add a new workflow config form
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
          
          workflowConfig.setWorkflowConfigParamsString(GrouperWorkflowConfigParams.getDefaultConfigParamsString());
          workflowConfig.setWorkflowConfigApprovalsString(GrouperWorkflowApprovalStates.getDefaultApprovalStatesString());
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
   * submit a new workflow config form
   * @param request
   * @param response
   */
  public void workflowConfigAddSubmit(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    Group group = null;
    
    try {
      
      grouperSession = GrouperSession.start(loggedInSubject);
      
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
      
      group = UiV2Group.retrieveGroupHelper(request, AccessPrivilege.VIEW).getGroup();
      
      if (group == null) {
        return;
      }
      
      String configForm = request.getParameter("grouperWorkflowConfigForm");
      if (!workflowContainer.isCanEditWorkflowFormField() && 
          !StringUtils.equals(configForm, new GrouperWorkflowConfig().getWorkflowConfigForm())) {
        throw new RuntimeException("Operation not permitted");
      }
      
      final Group GROUP = group;
      
      final GrouperWorkflowConfig workflowConfig = populateGrouperWorkflowConfig(request, response);
      
      @SuppressWarnings("unchecked")
      List<String> errors = (List<String>) GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        @Override
        public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
          
          workflowContainer.setGuiGrouperWorkflowConfig(GuiGrouperWorkflowConfig.convertFromGrouperWorkflowConfig(workflowConfig));
          
          List<String> errors = new GrouperWorkflowConfigValidator().validate(workflowConfig, GROUP, true);
          
          return errors;
        }
      });
      
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
  
  /**
   * view details of an existing workflow config
   * @param request
   * @param response
   */
  public void viewWorkflowConfigDetails(final HttpServletRequest request, final HttpServletResponse response) {
    
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
  
  /**
   * show edit form for an existing workflow config
   * @param request
   * @param response
   */
  public void editWorkflowConfig(final HttpServletRequest request, final HttpServletResponse response) {
        
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    Group group = null;
    
    try {

      grouperSession = GrouperSession.start(loggedInSubject);
      
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
   * edit workflow config form was submitted
   * @param request
   * @param response
   */
  public void workflowConfigEditSubmit(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    Group group = null;
    
    try {
      
      grouperSession = GrouperSession.start(loggedInSubject);
      
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
      
      
      group = UiV2Group.retrieveGroupHelper(request, AccessPrivilege.VIEW).getGroup();
      
      if (group == null) {
        return;
      }
      
      final Group GROUP = group;
      
      final GrouperWorkflowConfig workflowConfig = populateGrouperWorkflowConfig(request, response);
      
      @SuppressWarnings("unchecked")
      List<String> errors = (List<String>)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        @Override
        public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
          
          workflowContainer.setGuiGrouperWorkflowConfig(GuiGrouperWorkflowConfig.convertFromGrouperWorkflowConfig(workflowConfig));
          
          List<String> errors = new GrouperWorkflowConfigValidator().validate(workflowConfig, GROUP, false);
          
          return errors;
        }
      });
      
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
          
          if (!workflowContainer.isCanEditWorkflowFormField() &&
            !StringUtils.equals(config.getWorkflowConfigForm(), request.getParameter("grouperWorkflowConfigForm"))) {
            throw new RuntimeException("Operation not permitted");
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
   * show html form to subject who wants to join the group
   * @param request
   * @param response
   */
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
            
            workflowContainer.setHtmlForm(workflowSubjectCanInitiate.buildInitialHtml(INITIATE_STATE));
           
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
  
  /**
   * subject submitted/initiated a workflow to join the group
   * @param request
   * @param response
   */
  public void workflowInitiateSubmit(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    try {
      
      grouperSession = GrouperSession.start(loggedInSubject);
      
      if (!checkWorkflow()) {
        return;
      }
      
      
      final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
            
      final String attributeAssignId = request.getParameter("workflowAttributeAssignId");
      
      if (StringUtils.isBlank(attributeAssignId)) {
        throw new RuntimeException("workflowAttributeAssignId cannot be blank");
      }
      
      //switch over to admin so attributes work
      @SuppressWarnings("unchecked")
      List<String> errors = (List<String>)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        @Override
        public Object callback(GrouperSession theGrouperSession) throws GrouperSessionException {
          
          try {
           
            GrouperWorkflowConfig grouperWorkflowConfig = GrouperWorkflowConfigService.getWorkflowConfig(attributeAssignId);
            
            if (grouperWorkflowConfig == null) {
              throw new RuntimeException("No config found for attributeAssignId: "+attributeAssignId);
            }
            
            Group group = UiV2Group.retrieveGroupHelper(request, AccessPrivilege.VIEW, true).getGroup();
            
            GrouperWorkflowConfigParams configParams = grouperWorkflowConfig.getConfigParams();
            
            boolean canSubjectInitiateWorkflow = grouperWorkflowConfig.canSubjectInitiateWorkflow(loggedInSubject);
            if (!canSubjectInitiateWorkflow) {
              return Arrays.asList(TextContainer.retrieveFromRequest().getText().get("workflowInitiateNotAllowedError"));
            }
            
            Map<GrouperWorkflowConfigParam, String> paramNamesValues = new LinkedHashMap<GrouperWorkflowConfigParam, String>();
            
            for (GrouperWorkflowConfigParam param: configParams.getParams()) {
              String paramName = param.getParamName();
              paramNamesValues.put(param, request.getParameter(paramName));
            }
            
            List<String> errors = new GrouperWorkflowInstanceValidator().validateFormValues(paramNamesValues, INITIATE_STATE);
            if (errors.size() > 0) {
              return errors;
            }
            
            GrouperWorkflowInstanceService.saveInitiateStateInstance(grouperWorkflowConfig, loggedInSubject, 
                paramNamesValues, group);

          } catch (Exception e) {
            guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error,
                TextContainer.retrieveFromRequest().getText().get("workflowInitiateSubmitErrorMessage")));
          }
          
          return new ArrayList<String>();
        }
      });
      
      if (errors.size() > 0) {
        for (String error: errors) {
          guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, error));
        }
        return;
      }
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/group/viewGroup.jsp"));
      
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success,
          TextContainer.retrieveFromRequest().getText().get("workflowInitiateSubmitSuccessMessage")));
      
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }
  
  /**
   * show forms on misc page
   * @param request
   * @param response
   */
  public void forms(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    try {
      
      grouperSession = GrouperSession.start(loggedInSubject);
      
      if (!checkWorkflow()) {
        return;
      }
      
      final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
           
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/workflow/groupWorkflowMiscForm.jsp"));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
  /**
   * show list of instances waiting for approval for currently logged in subject
   * @param request
   * @param response
   */
  public void formsWaitingForApproval(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    try {
      
      grouperSession = GrouperSession.start(loggedInSubject);
      
      if (!checkWorkflow()) {
        return;
      }
      
      final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      
      final WorkflowContainer workflowContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getWorkflowContainer();
      
      //switch over to admin so attributes work
      GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {

        @Override
        public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
          
          List<GrouperWorkflowInstance> instancesWaitingForApproval = GrouperWorkflowInstanceService.getWorkflowInstancesWaitingForApproval(loggedInSubject);
          List<GuiGrouperWorkflowInstance> guiInstances = GuiGrouperWorkflowInstance.convertFromGrouperWorkflowInstances(instancesWaitingForApproval);
          workflowContainer.setWorkflowInstances(guiInstances);
          return null;
        }
        
      });
     
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/workflow/grouperWorkflowWaitingForApprovals.jsp"));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }
  
  /**
   * view all instances for a given workflow config  
   * @param request
   * @param response
   */
  public void viewInstances(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    try {
      
      grouperSession = GrouperSession.start(loggedInSubject);
      
      if (!checkWorkflow()) {
        return;
      }
      
      
      final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      
      final String workflowConfigId = request.getParameter("workflowConfigId");
      
      if (StringUtils.isBlank(workflowConfigId)) {
        throw new RuntimeException("workflowConfigId cannot be blank");
      }
      
      final WorkflowContainer workflowContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getWorkflowContainer();
      
      final Group group = UiV2Group.retrieveGroupHelper(request, AccessPrivilege.VIEW, false).getGroup();
      
      if (group == null) {
        return;
      }
      
      //switch over to admin so attributes work
      GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {

        @Override
        public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
          
          boolean canSubjectConfigureWorkflow = GrouperWorkflowConfigService.canSubjectConfigureWorkflow(group, loggedInSubject);
          
          List<GrouperWorkflowInstance> workflowInstances = GrouperWorkflowInstanceService.getWorkflowInstances(group, workflowConfigId);
          
          List<GuiGrouperWorkflowInstance> guiInstances = GuiGrouperWorkflowInstance.convertFromGrouperWorkflowInstances(workflowInstances);
          
          if (canSubjectConfigureWorkflow) {
            workflowContainer.setWorkflowInstances(guiInstances);
            return null;
          }
          
          GrouperWorkflowConfig workflowConfig = GrouperWorkflowConfigService.getWorkflowConfig(group, workflowConfigId);
          if (workflowConfig.isSubjectInViewersGroup(loggedInSubject)) {
            workflowContainer.setWorkflowInstances(guiInstances);
            return null;
          }
          
         throw new RuntimeException("Operation not permitted");
         
        }
        
      });
     
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/workflow/grouperWorkflowViewInstances.jsp"));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }
  
  /**
   * view details of a particular instance
   * @param request
   * @param response
   */
  public void viewInstance(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    try {
      
      grouperSession = GrouperSession.start(loggedInSubject);
      
      if (!checkWorkflow()) {
        return;
      }
      
      
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
          
          GrouperWorkflowInstance workfowInstance = GrouperWorkflowInstanceService.getWorkflowInstance(attributeAssignId);
          if (!GrouperWorkflowInstanceService.canInstanceBeViewed(workfowInstance, loggedInSubject)) {
            throw new RuntimeException("Operation not permitted");
          }
          
          GuiGrouperWorkflowInstance guiInstance = GuiGrouperWorkflowInstance.convertFromGrouperWorkflowInstance(workfowInstance);
          workflowContainer.setWorkflowInstance(guiInstance);
          workflowContainer.setHtmlForm(GrouperWorkflowInstanceService.getCurrentHtmlContent(workfowInstance));
          return null;
        }
        
      });
     
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/workflow/grouperWorkflowViewInstance.jsp"));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }
  
  /**
   * show instances currently logged in subject has submitted 
   * @param request
   * @param response
   */
  public void formsUserSubmitted(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    try {
      
      grouperSession = GrouperSession.start(loggedInSubject);
      
      if (!checkWorkflow()) {
        return;
      }
      
      
      final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      
      final WorkflowContainer workflowContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getWorkflowContainer();
      
      //switch over to admin so attributes work
      GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {

        @Override
        public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
          
          List<GrouperWorkflowInstance> instancesSubjectInitiated = GrouperWorkflowInstanceService.getWorkflowInstancesSubmitted(loggedInSubject);
          List<GuiGrouperWorkflowInstance> guiInstances = GuiGrouperWorkflowInstance.convertFromGrouperWorkflowInstances(instancesSubjectInitiated);
          workflowContainer.setWorkflowInstances(guiInstances);
          return null;
        }
        
      });
     
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/workflow/grouperWorkflowInstancesSubjectInitiated.jsp"));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }
  
  
  /**
   * approve button was clicked
   * @param request
   * @param response
   */
  public void workflowApprove(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    try {
      
      grouperSession = GrouperSession.start(loggedInSubject);
      
      if (!checkWorkflow()) {
        return;
      }
      
      
      final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      
      final WorkflowContainer workflowContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getWorkflowContainer();
      
      final String attributeAssignId = request.getParameter("attributeAssignId");
      
      if (StringUtils.isBlank(attributeAssignId)) {
        throw new RuntimeException("attributeAssignId cannot be blank");
      }
      
      //switch over to admin so attributes work
      @SuppressWarnings("unchecked")
      List<String> errors = (List<String>)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {

        @Override
        public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
          
          GrouperWorkflowInstance workfowInstance = GrouperWorkflowInstanceService.getWorkflowInstance(attributeAssignId);
          
          if (!GrouperWorkflowInstanceService.canInstanceBeApproved(workfowInstance, loggedInSubject)) {
            throw new RuntimeException("Operation not permitted");
          }
          
          GrouperWorkflowConfigParams configParams = workfowInstance.getGrouperWorkflowConfig().getConfigParams();
          
          Map<GrouperWorkflowConfigParam, String> paramNamesValues = new LinkedHashMap<GrouperWorkflowConfigParam, String>();
          
          for (GrouperWorkflowConfigParam param: configParams.getParams()) {
            String paramName = param.getParamName();
            paramNamesValues.put(param, request.getParameter(paramName));
          }
          
          List<String> errors = new GrouperWorkflowInstanceValidator().validateFormValues(paramNamesValues, workfowInstance.getWorkflowInstanceState());
          if (errors.size() > 0) {
            return errors;
          }
          
          GrouperWorkflowInstanceService.approveWorkflow(workfowInstance, loggedInSubject, paramNamesValues);
          
          GuiGrouperWorkflowInstance guiInstance = GuiGrouperWorkflowInstance.convertFromGrouperWorkflowInstance(workfowInstance);
          workflowContainer.setWorkflowInstance(guiInstance);
          return new ArrayList<String>();
        }
        
      });
      
      if (errors.size() > 0) {
        for (String error: errors) {
          guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, error));
        }
        return;
      }
      guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2GrouperWorkflow.formsWaitingForApproval')"));
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success,
          TextContainer.retrieveFromRequest().getText().get("grouperWorkflowApproveSuccess")));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }
  
  /**
   * disapprove button was clicked
   * @param request
   * @param response
   */
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
      @SuppressWarnings("unchecked")
      List<String> errors = (List<String>)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {

        @Override
        public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
          
          GrouperWorkflowInstance workfowInstance = GrouperWorkflowInstanceService.getWorkflowInstance(attributeAssignId);
          
          if (!GrouperWorkflowInstanceService.canInstanceBeApproved(workfowInstance, loggedInSubject)) {
            throw new RuntimeException("Operation not permitted");
          }
          
          GrouperWorkflowConfigParams configParams = workfowInstance.getGrouperWorkflowConfig().getConfigParams();
          
          Map<GrouperWorkflowConfigParam, String> paramNamesValues = new LinkedHashMap<GrouperWorkflowConfigParam, String>();
          
          for (GrouperWorkflowConfigParam param: configParams.getParams()) {
            String paramName = param.getParamName();
            paramNamesValues.put(param, request.getParameter(paramName));
          }
          
          List<String> errors = new GrouperWorkflowInstanceValidator().validateFormValues(paramNamesValues, workfowInstance.getWorkflowInstanceState());
          if (errors.size() > 0) {
            return errors;
          }
          
          GrouperWorkflowInstanceService.disapproveWorkflow(workfowInstance, loggedInSubject, paramNamesValues);
          
          GuiGrouperWorkflowInstance guiInstance = GuiGrouperWorkflowInstance.convertFromGrouperWorkflowInstance(workfowInstance);
          workflowContainer.setWorkflowInstance(guiInstance);
          return new ArrayList<String>();
        }
        
      });
      
      if (errors.size() > 0) {
        for (String error: errors) {
          guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, error));
        }
        return;
      }
      
      guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2GrouperWorkflow.formsWaitingForApproval')"));
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success,
          TextContainer.retrieveFromRequest().getText().get("grouperWorkflowApproveSuccess")));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }
  
  
  /**
   * populate workflow config from request parameters
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
  
  /**
   * for a given group, get the first wofklow config subject can initiate
   * @param group
   * @param subject
   * @return
   */
  @SuppressWarnings("unchecked")
  private GrouperWorkflowConfig workflowSubjectCanInitiate(final Group group, final Subject subject) {
    
    List<GrouperWorkflowConfig> workflowConfigs = (List<GrouperWorkflowConfig>) GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      
      public Object callback(GrouperSession theGrouperSession) throws GrouperSessionException {
        return GrouperWorkflowConfigService.getWorkflowConfigs(group);
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
  
  /**
   * make sure workflow settings are enabled and attribute def is there
   * @return
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

}
