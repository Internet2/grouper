package edu.internet2.middleware.grouper.app.workflow;

import static edu.internet2.middleware.grouper.app.workflow.GrouperWorkflowConstants.COMPLETE_STATE;
import static edu.internet2.middleware.grouper.app.workflow.GrouperWorkflowConstants.EXCEPTION_STATE;
import static edu.internet2.middleware.grouper.app.workflow.GrouperWorkflowConstants.INITIATE_STATE;
import static edu.internet2.middleware.grouper.app.workflow.GrouperWorkflowConstants.REJECTED_STATE;
import static edu.internet2.middleware.grouper.app.workflow.GrouperWorkflowConstants.WORKFLOW_CONFIG_ENABLED_FALSE;
import static edu.internet2.middleware.grouper.app.workflow.GrouperWorkflowConstants.WORKFLOW_CONFIG_ENABLED_NO_NEW_SUBMISSIONS;
import static edu.internet2.middleware.grouper.app.workflow.GrouperWorkflowConstants.WORKFLOW_CONFIG_ENABLED_TRUE;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.cfg.text.GrouperTextContainer;
import edu.internet2.middleware.subject.Subject;

public class GrouperWorkflowConfigValidator {
  
  
  /**
   * validate workflow config
   * @param group
   * @param isAddMode
   * @return list of errors
   */
  public List<String> validate(GrouperWorkflowConfig config,
      Group group, boolean isAddMode) {

    final List<String> errors = new ArrayList<String>();

    final Map<String, String> contentKeys = GrouperTextContainer.retrieveFromRequest().getText();
    
    if (StringUtils.isBlank(config.getWorkflowConfigType())) {
      errors.add(contentKeys.get("workflowTypeRequiredError"));
    }
    
    if (!GrouperWorkflowSettings.configTypes().contains(config.getWorkflowConfigType())) {
      String error = contentKeys.get("workflowTypeUnknownError");
      error = error.replace("$$workflowType$$", config.getWorkflowConfigType());
      errors.add(error);
    }
    
    if (StringUtils.isBlank(config.getWorkflowConfigName())) {
      errors.add(contentKeys.get("workflowConfigNameRequiredError"));
    }
    
    if (isAddMode) {
      List<GrouperWorkflowConfig> configs = GrouperWorkflowConfigService.getWorkflowConfigs(group);

      for (GrouperWorkflowConfig aConfig : configs) {
        if (aConfig.getWorkflowConfigName().equals(config.getWorkflowConfigName())) {
          errors.add(contentKeys.get("workflowConfigNameAlreadyInUseError"));
          break;
        }
      }
    }
    
    if (StringUtils.isBlank(config.getWorkflowConfigId())) {
      errors.add(contentKeys.get("workflowConfigIdRequiredError"));
    }

    String configIdRegex = "^[a-zA-Z0-9_-]*$";

    if (StringUtils.isNotBlank(config.getWorkflowConfigId()) 
        && !config.getWorkflowConfigId().matches(configIdRegex)) {
      errors.add(contentKeys.get("workflowConfigIdNotValidError"));
    }

    if (isAddMode && GrouperWorkflowConfigService.workflowIdExists(config.getWorkflowConfigId())) {
      errors.add(contentKeys.get("workflowConfigIdAlreadyInUseError"));
    }

    if (StringUtils.isBlank(config.getWorkflowConfigDescription())) {
      errors.add(contentKeys.get("workflowConfigDescriptionRequiredError"));
    }

    if (config.getWorkflowConfigDescription().length() > 4000) {
      errors.add(contentKeys.get("workflowConfigDescriptionLengthExceedsMaxLengthError"));
    }
    
    GrouperWorkflowApprovalStates approvalStates = validateConfigApprovals(config, errors);
    
    GrouperWorkflowConfigParams configParams = validateConfigParams(config, approvalStates, errors);
    
    validateConfigHtmlForm(config, configParams, errors);

    if (StringUtils.isNotBlank(config.getWorkflowConfigViewersGroupId())) {
      Group workflowViewersGroup = GroupFinder.findByUuid(GrouperSession.staticGrouperSession(), config.getWorkflowConfigViewersGroupId(), false);
      if (workflowViewersGroup == null) {
        workflowViewersGroup = GroupFinder.findByName(GrouperSession.staticGrouperSession(), config.getWorkflowConfigViewersGroupId(), false);
      }
      if (workflowViewersGroup == null) {
        errors.add(contentKeys.get("workflowViewerGroupIdNotFoundError"));
      }
    }

    if (StringUtils.isBlank(config.getWorkflowConfigEnabled())) {
      errors.add(contentKeys.get("workflowConfigEnabledRequiredError"));
    }
    
    List<String> validEnabledValues = Arrays.asList(WORKFLOW_CONFIG_ENABLED_TRUE, WORKFLOW_CONFIG_ENABLED_FALSE, WORKFLOW_CONFIG_ENABLED_NO_NEW_SUBMISSIONS);

    if (!validEnabledValues.contains(config.getWorkflowConfigEnabled())) {
      errors.add(contentKeys.get("workflowConfigEnabledInvalidValueError"));
    }
    
    if(!isAddMode) {
      validateNonEditableFieldsInEditMode(group, config, errors);
    }
    
    return errors;
  }
  
  private void validateNonEditableFieldsInEditMode(Group group, GrouperWorkflowConfig config, List<String> errors) {
    
    final Map<String, String> contentKeys = GrouperTextContainer.retrieveFromRequest().getText();
    
    String configId = config.getWorkflowConfigId();
    GrouperWorkflowConfig existingConfig = GrouperWorkflowConfigService.getWorkflowConfig(group, configId);
    
    if (config == null) {
      throw new RuntimeException("workflow config is null for config id: "+configId);
    }
    
    boolean checkForm = true;
    if (StringUtils.isEmpty(existingConfig.getWorkflowConfigForm()) && StringUtils.isEmpty(config.getWorkflowConfigForm())) {
      checkForm = false;
    }
    
    boolean formChanged = checkForm && !StringUtils.equals(existingConfig.getWorkflowConfigForm(), config.getWorkflowConfigForm());
    boolean paramsChanged = !StringUtils.equals(existingConfig.getWorkflowConfigParamsString(), config.getWorkflowConfigParamsString());
    boolean approvalsChanged = !StringUtils.equals(existingConfig.getWorkflowConfigApprovalsString(), config.getWorkflowConfigApprovalsString());
    
    if (formChanged || paramsChanged || approvalsChanged) {
      
      List<GrouperWorkflowInstance> instances = GrouperWorkflowInstanceService.getWorkflowInstances(group, configId);
      
      List<String> states = Arrays.asList(COMPLETE_STATE, EXCEPTION_STATE, REJECTED_STATE);
      
      for (GrouperWorkflowInstance instance: instances) {
        if ( !states.contains(instance.getWorkflowInstanceState())) {
          errors.add(contentKeys.get("workflowConfigFieldsNotEditableError"));
          break;
        }
      }
      
    }
    
  }
  
  private GrouperWorkflowConfigParams validateConfigParams(GrouperWorkflowConfig config, 
      GrouperWorkflowApprovalStates approvalStates, List<String> errors) {
    
    final Map<String, String> contentKeys = GrouperTextContainer.retrieveFromRequest().getText();
    
    if (StringUtils.isBlank(config.getWorkflowConfigParamsString())) {
      errors.add(contentKeys.get("workflowConfigParamsRequiredError"));
      return null;
    } 
    
    GrouperWorkflowConfigParams configParams;
    
    try {
      configParams = GrouperWorkflowConfigParams.buildParamsFromJsonString(config.getWorkflowConfigParamsString());
      config.setConfigParams(configParams);
    } catch (Exception e) {
      errors.add(contentKeys.get("workflowParamsInvalidJsonError"));
      return null;
    }
    
    final List<String> validParamTypes = Arrays.asList("textarea", "text", "checkbox");
      
    for (int i = 0; i < configParams.getParams().size(); i++) {
      GrouperWorkflowConfigParam param = configParams.getParams().get(i);
      String paramName = param.getParamName();
      String paramType = param.getType();
      List<String> editableStates = param.getEditableInStates();

      if (StringUtils.isBlank(paramName)) {
        String error = contentKeys.get("workflowParamsParamNameMissingError");
        error = error.replace("$$index$$", String.valueOf(i));
        errors.add(error);
      }
      
      if (StringUtils.isBlank(paramType)) {
        String error = contentKeys.get("workflowParamsTypeMissingError");
        error = error.replace("$$index$$", String.valueOf(i));
        errors.add(error);
      }

      if (editableStates == null || editableStates.size() == 0) {
        String error = contentKeys.get("workflowParamsEditableInStatesMissingError");
        error = error.replace("$$index$$", String.valueOf(i));
        errors.add(error);
      }
      
      if (approvalStates != null) {
        for (String editableState: editableStates) {
          GrouperWorkflowApprovalState workflowApprovalState = approvalStates.getStateByName(editableState);
          if (workflowApprovalState == null) {
            String error = contentKeys.get("workflowParamsEditableStateNotFoundInApprovalStates"); 
            error = error.replace("$$editableState$$", editableState);
            errors.add(error);
          }
        }
      }
      
      if (!validParamTypes.contains(paramType)) {
        String error = contentKeys.get("workflowParamsInvalidParamType");
        error = error.replace("$$paramType$$", paramType);
        errors.add(error);
      }
      
    }

    if (configParams.getParams().size() > 10) {
      errors.add(contentKeys.get("workflowParamsExceedsMaxSizeError"));
    }
    
    return configParams;
    
  }
  
  private GrouperWorkflowApprovalStates validateConfigApprovals(GrouperWorkflowConfig config,
      List<String> errors) {
    
    final Map<String, String> contentKeys = GrouperTextContainer.retrieveFromRequest().getText();
    
    if (StringUtils.isBlank(config.getWorkflowConfigApprovalsString())) {
      errors.add(contentKeys.get("workflowApprovalsRequiredError"));
      return null;
    } 
    
    GrouperWorkflowApprovalStates workflowApprovalStates;
    try {
      workflowApprovalStates = GrouperWorkflowApprovalStates.buildApprovalStatesFromJsonString(config.getWorkflowConfigApprovalsString());
      config.setWorkflowApprovalStates(workflowApprovalStates);
    } catch (Exception e) {
      errors.add(contentKeys.get("workflowApprovalsInvalidJsonError"));
      return null;
    }
      
    boolean isInitiateStateAvailable = false;
    boolean isCompleteStateAvailable = false;
         
    for (int i = 0; i < workflowApprovalStates.getStates().size(); i++) {
      
      final GrouperWorkflowApprovalState state = workflowApprovalStates.getStates().get(i);
      
      final String stateName = state.getStateName();
      
      if (StringUtils.isBlank(stateName)) {
        String error = contentKeys.get("workflowApprovalsStateNameMissing");
        error = error.replace("$$$$index$$", String.valueOf(i));
        errors.add(error);
        continue;
      }
      
      // can be only one initiate and one complete state
      if (isInitiateStateAvailable && stateName.equals(INITIATE_STATE)) {
        errors.add(contentKeys.get("workflowApprovalsMultipleInitiateStatesFound"));
        continue;
      }
      
      if (isCompleteStateAvailable && stateName.equals(COMPLETE_STATE)) {
        errors.add(contentKeys.get("workflowApprovalsMultipleCompleteStatesFound"));
        continue;
      }
      
      if (stateName.equals(INITIATE_STATE)) {
        isInitiateStateAvailable = true;
        final String allowedGroupId = state.getAllowedGroupId();
        if (StringUtils.isNotBlank(allowedGroupId)) {
          Group allowedGroup = GroupFinder.findByUuid(GrouperSession.staticGrouperSession(), allowedGroupId, false);
          if (allowedGroup == null) {
            allowedGroup = GroupFinder.findByName(GrouperSession.staticGrouperSession(), allowedGroupId, false);
          }
          if (allowedGroup == null) {
            String error = contentKeys.get("workflowApprovalsStateAllowedGroupNotFound");
            error = error.replace("$$stateName$$", stateName);
            error = error.replace("$$groupId$$", allowedGroupId);
            errors.add(error);
          }
        }
      }
      
      if (stateName.equals(COMPLETE_STATE)) {
        isCompleteStateAvailable = true;
        List<GrouperWorkflowApprovalAction> actions = state.getActions() != null ? state.getActions(): new ArrayList<GrouperWorkflowApprovalAction>();
        for (GrouperWorkflowApprovalAction action: actions) {
          if (action.getActionName().equals("assignToGroup")) {
            String assignToGroupId = action.getActionArg0();
            Group assignToGroup = GroupFinder.findByUuid(GrouperSession.staticGrouperSession(), assignToGroupId, false);
            if (assignToGroup == null) {
              assignToGroup = GroupFinder.findByName(GrouperSession.staticGrouperSession(), assignToGroupId, false);
            }
            if (assignToGroup == null) {
              String error = contentKeys.get("workflowApprovalsStateCompleteStateAssignToGroupIdNotFound");
              error = error.replace("$$assignToGroupId$$", assignToGroupId == null ? "" : assignToGroupId);
              errors.add(error);
            }
          }
        }
        
      }
      
      int approversTypes = 0;
      if (!StringUtils.isBlank(state.getApproverGroupId())) {
        approversTypes++;
      }
      if (!StringUtils.isBlank(state.getApproverManagersOfGroupId())) {
        approversTypes++;
      }
      if (!StringUtils.isBlank(state.getApproverSubjectId())) {
        approversTypes++;
      }
      
      // non initiate and non complete state must have approvers
      if (!Arrays.asList(INITIATE_STATE, COMPLETE_STATE).contains(stateName)) {
        if (approversTypes == 0) {
          String error = contentKeys.get("workflowApprovalsStateApproversMissing");
          error = error.replace("$$stateName$$", stateName);
          errors.add(error);
        } else if (approversTypes > 1) {
          // cannot have more than one type of approvers
          String error = contentKeys.get("workflowApprovalsStateMultipleTypesOfApprovers");
          error = error.replace("$$stateName$$", stateName);
          errors.add(error); 
        }
      }
      
      // if approvers group id is there, make sure it's valid
      if (StringUtils.isNotBlank(state.getApproverGroupId())) {
        Group approverGroup = GroupFinder.findByUuid(GrouperSession.staticGrouperSession(), state.getApproverGroupId(), false);
        if (approverGroup == null) {
          approverGroup = GroupFinder.findByName(GrouperSession.staticGrouperSession(), state.getApproverGroupId(), false);
        }
        if (approverGroup == null) {
          String error = contentKeys.get("workflowApprovalsStateApproverGroupNotFound");
          error = error.replace("$$stateName$$", stateName);
          error = error.replace("$$groupId$$", state.getApproverGroupId());
          errors.add(error);
        }
      }
      
      if (StringUtils.isNotBlank(state.getApproverManagersOfGroupId())) {
        Group approverGroup = GroupFinder.findByUuid(GrouperSession.staticGrouperSession(), state.getApproverManagersOfGroupId(), false);
        if (approverGroup == null) {
          approverGroup = GroupFinder.findByName(GrouperSession.staticGrouperSession(), state.getApproverManagersOfGroupId(), false);
        }
        if (approverGroup == null) {
          String error = contentKeys.get("workflowApprovalsStateApproverGroupNotFound");
          error = error.replace("$$stateName$$", stateName);
          error = error.replace("$$groupId$$", state.getApproverManagersOfGroupId());
          errors.add(error);
        }
      }
      
      if (StringUtils.isNotBlank(state.getApproverSubjectId())) {
        
        if (StringUtils.isBlank(state.getApproverSubjectSourceId())) {
          String error = contentKeys.get("workflowApprovalsStateApproverSubjectSourceIdMissing"); 
          error = error.replace("$$stateName$$", stateName);
          errors.add(error);
        } else {
          Subject subject = SubjectFinder.findByIdAndSource(state.getApproverSubjectId(), state.getApproverSubjectSourceId(), false);
          if (subject == null) {
            String error = contentKeys.get("workflowApprovalsStateApproverSubjectSourceIdMissing"); 
            error = error.replace("$$stateName$$", stateName);
            error = error.replace("$$subjectId$$", state.getApproverSubjectId());
            error = error.replace("$$sourceId$$", state.getApproverSubjectSourceId());
            errors.add(error);
          }
        }
        
      }
                
    }
  
    if (!isInitiateStateAvailable) {
      errors.add(contentKeys.get("workflowApprovalsInitiateStateRequiredError"));
    }

    if (!isCompleteStateAvailable) {
      errors.add(contentKeys.get("workflowApprovalsCompleteStateRequiredError"));
    }
    
    return workflowApprovalStates;
  }
  
  private  List<String> validateConfigHtmlForm(GrouperWorkflowConfig config,
      GrouperWorkflowConfigParams configParams, List<String> errors) {
    
    final Map<String, String> contentKeys = GrouperTextContainer.retrieveFromRequest().getText();
    
    try {
      Document document = Jsoup.parse(config.getWorkflowConfigForm());
      Elements inputElements = document.getElementsByTag("input");
      
      for (Element inputElement: inputElements) {
        String nameOfElement = inputElement.attr("name");
        GrouperWorkflowConfigParam param = configParams.getConfigParamByNameAndType(nameOfElement, inputElement.attr("type"));
        if (param == null) {
          String error = contentKeys.get("workflowConfigFormElementNotFoundInJsonParams");
          error = error.replace("$$elementName$$", nameOfElement);
          error = error.replace("$$elementType$$", inputElement.attr("type"));
          errors.add(error);
        }
      }
      
      Elements textAreas = document.getElementsByTag("textarea");
      for (Element textArea: textAreas) {
        String nameOfElement = textArea.attr("name");
        GrouperWorkflowConfigParam param = configParams.getConfigParamByNameAndType(nameOfElement, "textarea");
        if (param == null) {
          String error = contentKeys.get("workflowConfigFormElementNotFoundInJsonParams");
          error = error.replace("$$elementName$$", nameOfElement);
          error = error.replace("$$elementType$$", "textarea");
          errors.add(error);
        }
      }
    } catch(Exception e) {
      errors.add(contentKeys.get("workflowConfigInvalidConfigForm"));
    }
     
    return errors;
  }
  
}
