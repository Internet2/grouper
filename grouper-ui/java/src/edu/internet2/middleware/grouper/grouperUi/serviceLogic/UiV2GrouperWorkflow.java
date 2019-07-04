package edu.internet2.middleware.grouper.grouperUi.serviceLogic;

import static edu.internet2.middleware.grouper.app.workflow.GrouperWorkflowApprovalState.INITIATE_STATE;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.app.workflow.GrouperWorkflowApprovalState;
import edu.internet2.middleware.grouper.app.workflow.GrouperWorkflowApprovalStates;
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
          
          if (!checkWorkflow()) {
            return null;
          }
            
          GrouperWorkflowConfig workflowConfig = new GrouperWorkflowConfig();
          //TODO: add a constant
          workflowConfig.setWorkflowConfigType("grouper");
          workflowConfig.setConfigParams(GrouperWorkflowConfig.getDefaultConfigParams());
          workflowConfig.setWorkflowApprovalStates(GrouperWorkflowConfig.getDefaultApprovalStates());
          
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
      
      if (!workflowContainer.isCanConfigureWorkflow()) {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("grouperWorkflowNotAllowedToAdd")));
        return;
      }
      
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
        // guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2GrouperWorkflow.formAdd&groupId=" + group.getId() + "')"));
        return;
      }
      
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
      
      if (!workflowContainer.isCanConfigureWorkflow()) {
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
      
      if (!workflowContainer.isCanConfigureWorkflow()) {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("grouperWorkflowNotAllowedToAdd")));
        return;
      }
      
      final GrouperWorkflowConfig workflowConfig = populateGrouperWorkflowConfig(request, response);
      
      if (workflowConfig == null) {
        return;
      }
      
      workflowContainer.setGuiGrouperWorkflowConfig(GuiGrouperWorkflowConfig.convertFromGrouperWorkflowConfig(workflowConfig));
      
      List<String> errors = workflowConfig.validate(GROUP, false);
      if (errors.size() > 0) {
        workflowContainer.setErrors(errors);
        // guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2GrouperWorkflow.editWorkflowConfig&workflowConfigId="+workflowConfigId+"&groupId=" + group.getId() + "')"));
        guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
            "/WEB-INF/grouperUi2/workflow/groupWorkflowConfigEdit.jsp"));
        return;
      }
      
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
  
  public void showJoinGroupForm(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
    try {
      
      GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        @Override
        public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
          Group group = UiV2Group.retrieveGroupHelper(request, AccessPrivilege.VIEW, true).getGroup();
          GrouperWorkflowConfig workflowSubjectIsMemberOf = workflowSubjectIsMemberOf(group, loggedInSubject);
          
          if (workflowSubjectIsMemberOf != null) {
            
            //check if there's already a pending request for this subject, group
            boolean requestAlreadySubmitted = GrouperWorkflowInstanceService.subjectAlreadySubmittedWorkflow(loggedInSubject, group);
            if (requestAlreadySubmitted) {
              guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.info, 
                  TextContainer.retrieveFromRequest().getText().get("workflowJoinGroupRequestAlreadyInProgress")));
              return null;
            }
            
            WorkflowContainer workflowContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getWorkflowContainer();
            workflowContainer.setGuiGrouperWorkflowConfig(GuiGrouperWorkflowConfig.convertFromGrouperWorkflowConfig(workflowSubjectIsMemberOf));
//            if (StringUtils.isNotBlank(workflowSubjectIsMemberOf.getWorkflowConfigForm())) {
//              workflowContainer.setHtmlForm(workflowSubjectIsMemberOf.getWorkflowConfigForm());
//            } else {
//              String htmlForm = workflowSubjectIsMemberOf.buildHtmlFromParams(false, "initiate");
//              workflowContainer.setHtmlForm(htmlForm);
//            }
            String htmlForm = workflowSubjectIsMemberOf.buildHtmlFromParams(false, INITIATE_STATE);
            workflowContainer.setHtmlForm(htmlForm);
            guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId",
                "/WEB-INF/grouperUi2/group/groupJoinInitiateWorkflow.jsp")); //TODO move to workflow folder
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
      grouperSession = GrouperSession.start(loggedInSubject);
      
      final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
            
      final String attributeAssignId = request.getParameter("workflowAttributeAssignId");
      
      if (StringUtils.isBlank(attributeAssignId)) {
        throw new RuntimeException("workflowAttributeAssignId cannot be null.");
      }
      
      //switch over to admin so attributes work
      GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        @Override
        public Object callback(GrouperSession theGrouperSession) throws GrouperSessionException {
          
          try {
            if (!checkWorkflow()) {
              return null;
            }
            
            GrouperWorkflowConfig grouperWorkflowConfig = GrouperWorkflowConfigService.getWorkflowConfig(attributeAssignId);
            
            if (grouperWorkflowConfig == null) {
              throw new RuntimeException("No config found for attributeAssignId: "+attributeAssignId);
            }
            
            Group group = UiV2Group.retrieveGroupHelper(request, AccessPrivilege.VIEW, true).getGroup();
            
            GrouperWorkflowConfigParams configParams = grouperWorkflowConfig.getConfigParams();
            
            boolean subjectInAllowedGroup = grouperWorkflowConfig.isSubjectInInitiateAllowedGroup(loggedInSubject);
            if (!subjectInAllowedGroup) {
              guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error,
                  TextContainer.retrieveFromRequest().getText().get("workflowInitiateNotAllowedError")));
              return null;
            }
            
            Map<String, String> paramNamesValues = new HashMap<String, String>();
            
            for (GrouperWorkflowConfigParam param: configParams.getParams()) {
              String paramName = param.getParamName();
              paramNamesValues.put(param.getParamName(), request.getParameter(paramName));
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
          
          //TODO check if the logged in subject can view this instance
          
          GrouperWorkflowInstance workfowInstance = GrouperWorkflowInstanceService.getWorkfowInstance(attributeAssignId);
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
  
  
  public void workflowApprove(final HttpServletRequest request, final HttpServletResponse response) {
    
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
          
          //TODO check if the logged in subject can approve
          
          GrouperWorkflowInstance workfowInstance = GrouperWorkflowInstanceService.getWorkfowInstance(attributeAssignId);
          
          GrouperWorkflowConfigParams configParams = workfowInstance.getGrouperWorkflowConfig().getConfigParams();
          
          Map<String, String> paramNamesValues = new HashMap<String, String>();
          
          for (GrouperWorkflowConfigParam param: configParams.getParams()) {
            String paramName = param.getParamName();
            paramNamesValues.put(param.getParamName(), request.getParameter(paramName));
          }
          
          GrouperWorkflowInstanceService.approveWorkflow(workfowInstance, loggedInSubject, paramNamesValues);
          
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
    
    final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    GrouperWorkflowConfigParams grouperWorkflowConfigParams;
    try {
      grouperWorkflowConfigParams = GrouperWorkflowConfig.buildParamsFromJsonString(configParams);
    } catch (Exception e) {
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error,
          TextContainer.retrieveFromRequest().getText().get("workflowParamsInvalidJsonError")));
      return null;
    }
    
    GrouperWorkflowApprovalStates approvalStates;
    try {
      approvalStates = GrouperWorkflowConfig.buildApprovalStatesFromJsonString(configApprovals);
    } catch (Exception e) {
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error,
          TextContainer.retrieveFromRequest().getText().get("workflowApprovalsInvalidJsonError")));
      return null;
    }
    
    GrouperWorkflowConfig workflowConfig = new GrouperWorkflowConfig();
    workflowConfig.setWorkflowApprovalStates(approvalStates);
    workflowConfig.setWorkflowConfigId(configId);
    workflowConfig.setWorkflowConfigDescription(configDescription != null ? configDescription.trim(): null);
    workflowConfig.setWorkflowConfigEnabled(workflowEnabled);
    workflowConfig.setWorkflowConfigForm(configForm != null ? configForm.trim(): null);
    workflowConfig.setWorkflowConfigName(configName);
    workflowConfig.setConfigParams(grouperWorkflowConfigParams);
    //workflowConfig.setWorkflowConfigParams(configParams != null ? configParams.trim(): null);
    
    workflowConfig.setWorkflowConfigType(configType);
    workflowConfig.setWorkflowConfigViewersGroupId(viewersGroupId);
    workflowConfig.setWorkflowConfigSendEmail(BooleanUtils.toBoolean(sendEmail));
    
    return workflowConfig;
    
  }
  
  @SuppressWarnings("unchecked")
  private GrouperWorkflowConfig workflowSubjectIsMemberOf(final Group group, final Subject subject) {
    
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
        if (workflowConfig.isSubjectInInitiateAllowedGroup(subject)) {
          return workflowConfig;
        }
      }
    } 
    
    return null;
    
  }

}
