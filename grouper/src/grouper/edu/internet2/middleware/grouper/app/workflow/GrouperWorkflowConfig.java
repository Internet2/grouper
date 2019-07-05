package edu.internet2.middleware.grouper.app.workflow;

import static edu.internet2.middleware.grouper.app.workflow.GrouperWorkflowApprovalState.COMPLETE_STATE;
import static edu.internet2.middleware.grouper.app.workflow.GrouperWorkflowApprovalState.INITIATE_STATE;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.cfg.text.GrouperTextContainer;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

public class GrouperWorkflowConfig {

  /**
   * logger 
   */
  private static final Log LOG = GrouperUtil.getLog(GrouperWorkflowConfig.class);

  private String workflowConfigType;

  private GrouperWorkflowApprovalStates workflowApprovalStates;

  private String workflowConfigApprovalsString;

  private String workflowConfigName;

  private String workflowConfigId;

  private String workflowConfigDescription;

  private GrouperWorkflowConfigParams configParams;

  private String workflowConfigParamsString;

  private String workflowConfigForm = "Submit this form to be added to this group.<br /><br />\n"
      + "The managers of the group will be notified to approve this request.<br /><br />\n"
      + "Notes (optional): <textarea rows=\"4\" cols=\"50\" name=\"notes\" id=\"notesId\"></textarea><br /><br />\n"
      + "Notes for approvers: <textarea rows=\"4\" cols=\"50\" name=\"notesForApprovers\" id=\"notesForApproversId\"></textarea><br /><br />";

  private String workflowConfigViewersGroupId;

  private boolean workflowConfigSendEmail = true;

  private String workflowConfigEnabled = "true";

  private String attributeAssignmentMarkerId;

  public String getWorkflowConfigType() {
    return workflowConfigType;
  }

  public void setWorkflowConfigType(String workflowConfigType) {
    this.workflowConfigType = workflowConfigType;
  }

  public String getWorkflowConfigName() {
    return workflowConfigName;
  }

  public void setWorkflowConfigName(String workflowConfigName) {
    this.workflowConfigName = workflowConfigName;
  }

  public String getWorkflowConfigId() {
    return workflowConfigId;
  }

  public void setWorkflowConfigId(String workflowConfigId) {
    this.workflowConfigId = workflowConfigId;
  }

  public String getWorkflowConfigDescription() {
    return workflowConfigDescription;
  }

  public void setWorkflowConfigDescription(String workflowConfigDescription) {
    this.workflowConfigDescription = workflowConfigDescription;
  }

  public String getWorkflowConfigForm() {
    return workflowConfigForm;
  }

  public void setWorkflowConfigForm(String workflowConfigForm) {
    this.workflowConfigForm = workflowConfigForm;
  }

  public String getWorkflowConfigViewersGroupId() {
    return workflowConfigViewersGroupId;
  }

  public void setWorkflowConfigViewersGroupId(String workflowConfigViewersGroupId) {
    this.workflowConfigViewersGroupId = workflowConfigViewersGroupId;
  }

  public boolean isWorkflowConfigSendEmail() {
    return workflowConfigSendEmail;
  }

  public void setWorkflowConfigSendEmail(boolean workflowConfigSendEmail) {
    this.workflowConfigSendEmail = workflowConfigSendEmail;
  }

  public String getWorkflowConfigEnabled() {
    return workflowConfigEnabled;
  }

  public void setWorkflowConfigEnabled(String workflowConfigEnabled) {
    this.workflowConfigEnabled = workflowConfigEnabled;
  }

  public String getAttributeAssignmentMarkerId() {
    return attributeAssignmentMarkerId;
  }

  public void setAttributeAssignmentMarkerId(String attributeAssignmentMarkerId) {
    this.attributeAssignmentMarkerId = attributeAssignmentMarkerId;
  }

  public GrouperWorkflowConfigParams getConfigParams() {
    return configParams;
  }

  public void setConfigParams(GrouperWorkflowConfigParams configParams) {
    this.configParams = configParams;
  }

  public GrouperWorkflowApprovalStates getWorkflowApprovalStates() {
    return workflowApprovalStates;
  }

  public void setWorkflowApprovalStates(
      GrouperWorkflowApprovalStates workflowApprovalStates) {
    this.workflowApprovalStates = workflowApprovalStates;
  }

  public String getWorkflowConfigApprovalsString() {
    return workflowConfigApprovalsString;
  }

  public void setWorkflowConfigApprovalsString(String workflowConfigApprovalsString) {
    this.workflowConfigApprovalsString = workflowConfigApprovalsString;
  }

  public String getWorkflowConfigParamsString() {
    return workflowConfigParamsString;
  }

  public void setWorkflowConfigParamsString(String workflowConfigParamsString) {
    this.workflowConfigParamsString = workflowConfigParamsString;
  }

  public List<String> validate(Group group, boolean checkForDuplicateConfig) {

    final List<String> errors = new ArrayList<String>();

    final Map<String, String> contentKeys = GrouperTextContainer.retrieveFromRequest().getText();
    
    if (StringUtils.isBlank(workflowConfigType)) {
      errors.add(contentKeys.get("workflowTypeRequiredError"));
    }

    //TODO if workflow type is not one of the workflow types we know of, error

    if (workflowApprovalStates == null || workflowApprovalStates.getStates().size() == 0) {
      errors.add(contentKeys.get("workflowApprovalsRequiredError"));
    } else {
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
          if (StringUtils.isBlank(allowedGroupId)) {
            //TODO waiting for Chris response if error is needed
          } else {
            GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
              
              @Override
              public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
                Group group = GroupFinder.findByUuid(grouperSession, allowedGroupId, false);
                if (group == null) {
                  String error = contentKeys.get("workflowApprovalsStateAllowedGroupNotFound");
                  error = error.replace("$$stateName$$", stateName);
                  error = error.replace("$$groupId$$", allowedGroupId);
                  errors.add(error);
                }
                return null;
              }
            });
          }
        }
        
        if (stateName.equals(COMPLETE_STATE)) {
          isCompleteStateAvailable = true;
        }
        
        int approversTypes = 0;
        if (!StringUtils.isEmpty(state.getApproverGroupId())) {
          approversTypes++;
        }
        if (!StringUtils.isEmpty(state.getApproverManagersOfGroupId())) {
          approversTypes++;
        }
        if (!StringUtils.isEmpty(state.getApproverSubjectId())) {
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
        
        GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
          
          @Override
          public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
            
            // if approvers group id is there, make sure it's valid
            if (StringUtils.isNotEmpty(state.getApproverGroupId())) {
              Group approverGroup = GroupFinder.findByUuid(grouperSession, state.getApproverGroupId(), false);
              if (approverGroup == null) {
                String error = contentKeys.get("workflowApprovalsStateApproverGroupNotFound");
                error = error.replace("$$stateName$$", stateName);
                error = error.replace("$$groupId$$", state.getApproverGroupId());
                errors.add(error);
              }
            }
            
            if (StringUtils.isNotEmpty(state.getApproverManagersOfGroupId())) {
              Group approverGroup = GroupFinder.findByUuid(grouperSession, state.getApproverManagersOfGroupId(), false);
              if (approverGroup == null) {
                String error = contentKeys.get("workflowApprovalsStateApproverGroupNotFound");
                error = error.replace("$$stateName$$", stateName);
                error = error.replace("$$groupId$$", state.getApproverManagersOfGroupId());
                errors.add(error);
              }
            }
            
            if (StringUtils.isNotEmpty(state.getApproverSubjectId())) {
              
              if (StringUtils.isEmpty(state.getApproverSubjectSourceId())) {
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
            
            if (StringUtils.isNotEmpty(state.getApproverNotifyGroupId())) {
              Group groupToNotify = GroupFinder.findByUuid(grouperSession, state.getApproverNotifyGroupId(), false);
              if (groupToNotify == null) {
                String error = contentKeys.get("workflowApprovalsStateGroupToNotifyNotFound");
                error = error.replace("$$stateName$$", stateName);
                error = error.replace("$$groupId$$", state.getApproverNotifyGroupId());
                errors.add(error);
              }
            }
            
            return null;
          }
        });
        
      }

      if (!isInitiateStateAvailable) {
        errors.add(contentKeys.get("workflowApprovalsInitiateStateRequiredError"));
      }

      if (!isCompleteStateAvailable) {
        errors.add(contentKeys.get("workflowApprovalsCompleteStateRequiredError"));
      }
    }

    if (StringUtils.isBlank(workflowConfigName)) {
      errors.add(contentKeys.get("workflowConfigNameRequiredError"));
    }

    if (checkForDuplicateConfig) {
      List<GrouperWorkflowConfig> configs = GrouperWorkflowConfigService.getWorkflowConfigs(group);

      for (GrouperWorkflowConfig config : configs) {
        if (config.getWorkflowConfigName().equals(workflowConfigName)) {
          errors.add(contentKeys.get("workflowConfigNameAlreadyInUseError"));
          break;
        }
      }
    }

    if (StringUtils.isBlank(workflowConfigId)) {
      errors.add(contentKeys.get("workflowConfigIdRequiredError"));
    }

    String configIdRegex = "^[a-zA-Z0-9_-]*$";

    if (!workflowConfigId.matches(configIdRegex)) {
      errors.add(contentKeys.get("workflowConfigIdNotValidError"));
    }

    if (checkForDuplicateConfig && GrouperWorkflowConfigService.workflowIdExists(workflowConfigId)) {
      errors.add(contentKeys.get("workflowConfigIdAlreadyInUseError"));
    }

    if (StringUtils.isBlank(workflowConfigDescription)) {
      errors.add(contentKeys.get("workflowConfigDescriptionRequiredError"));
    }

    if (workflowConfigDescription.length() > 4000) {
      errors.add(contentKeys.get("workflowConfigDescriptionLengthExceedsMaxLengthError"));
    }

    if (configParams == null || configParams.getParams().size() == 0) {
      errors.add(contentKeys.get("workflowConfigParamsRequiredError"));
    } else {
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
      }

      if (configParams.getParams().size() > 10) {
        errors.add(contentKeys.get("workflowParamsExceedsMaxSizeError"));
      }
    }

    if (StringUtils.isNotBlank(workflowConfigViewersGroupId)) {
      
      Group workflowViewersGroupId  = (Group) GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        @Override
        public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
          return GroupFinder.findByUuid(grouperSession, workflowConfigViewersGroupId, false);
        }
      });
      
      if (workflowViewersGroupId == null) {
        errors.add(contentKeys.get("workflowViewerGroupIdNotFoundError"));
      }
    }

    if (StringUtils.isBlank(workflowConfigEnabled)) {
      errors.add(contentKeys.get("workflowConfigEnabledRequiredError"));
    }

    List<String> validEnabledValues = Arrays.asList("true", "false", "noNewSubmissions");

    if (!validEnabledValues.contains(workflowConfigEnabled)) {
      errors.add(contentKeys.get("workflowConfigEnabledInvalidValueError"));
    }

    return errors;

  }

  public boolean isSubjectInInitiateAllowedGroup(final Subject subject) {

    for (GrouperWorkflowApprovalState state : workflowApprovalStates.getStates()) {
      if (INITIATE_STATE.equals(state.getStateName())) {
        final String allowedGroupId = state.getAllowedGroupId();
        if (StringUtils.isBlank(allowedGroupId)) {
          return false;
        }

        Boolean isMember = (Boolean) GrouperSession
            .internal_callbackRootGrouperSession(new GrouperSessionHandler() {

              @Override
              public Object callback(GrouperSession grouperSession)
                  throws GrouperSessionException {

                Group group = GroupFinder.findByUuid(grouperSession, allowedGroupId,
                    false);
                if (group == null) {
                  LOG.error(
                      "allowed group id " + allowedGroupId + " not found in workflow id "
                          + workflowConfigId + " Was the group delted??");
                  return false;
                }

                Member member = MemberFinder.findBySubject(grouperSession, subject,
                    false);
                return member != null && member.isMember(group);
              }

            });

        return isMember;

      }
    }

    return false;
  }

  public String buildHtmlFromParams(boolean addPlaceholderForValues, String currentState) {

    StringBuilder html = new StringBuilder();

    html.append("<div style='margin-bottom: 10px'>");
    html.append("<span style='font-weight: bold'>");
    html.append(GrouperTextContainer.retrieveFromRequest().getText()
        .get("workflowElectronicFormNameLabel"));
    html.append("</span>");
    html.append("<span style='padding-left: 15px'>");
    html.append(this.getWorkflowConfigName());
    html.append("</span>");
    html.append("</div>");
    
    html.append("<div>");
    html.append("<span style='font-weight: bold'>");
    html.append(GrouperTextContainer.retrieveFromRequest().getText()
        .get("workflowElectronicFormDescriptionLabel"));
    html.append("</span>");
    html.append("<span style='padding-left: 15px'>");
    html.append(this.getWorkflowConfigDescription());
    html.append("</span>");
    html.append("</div>");
    
    html.append("<table class='table table-condensed table-striped'>");

    for (GrouperWorkflowConfigParam param : configParams.getParams()) {

      String label = param.getLabel();
      html.append("<tr>");
      html.append("<td style='vertical-align: top; white-space: nowrap;'>");
      html.append("<strong><label>");
      html.append(label);
      html.append("</label></strong></td>");
      html.append("<td>");
      html.append(buildInputField(param, addPlaceholderForValues, currentState));
      html.append("</td></tr>");
    }
    html.append("</table>");
    return html.toString();
  }

  private String buildInputField(GrouperWorkflowConfigParam param,
      boolean addPlaceholderForValues, String currentState) {

    StringBuilder field = new StringBuilder();

    List<String> editabledInStates = param.getEditableInStates();
    String disabled = "";

    //TODO editable in states should be a string with comma separated items
    if (currentState == null || !editabledInStates.contains(currentState)) {
      disabled = "disabled";
    }

    if (param.getType().equals("textarea")) {
      field.append("<textarea cols='20' rows='3' " + disabled + " name="
          + param.getParamName() + ">");
      field.append(addPlaceholderForValues ? "~~" + param.getParamName() + "~~" : "");
      field.append("</textarea>");
    } else if (param.getType().equals("textfield")) {
      field.append(
          "<input type='text'" + disabled + " name=" + param.getParamName() + ">");
      field.append(addPlaceholderForValues ? "~~" + param.getParamName() + "~~" : "");
      field.append("</input>");
    } else if (param.getType().equals("checkbox")) {
      field.append(
          "<input type='checkbox'" + disabled + " name=" + param.getParamName() + " ");
      field.append(addPlaceholderForValues ? "~~" + param.getParamName() + "~~" : "");
      field.append("></input>");
    } else {
      throw new RuntimeException("Invalid type: " + param.getType());
    }

    return field.toString();
  }

  public static GrouperWorkflowConfigParams buildParamsFromJsonString(String params) {

    try {
      GrouperWorkflowConfigParams configParams = GrouperWorkflowSettings.objectMapper
          .readValue(params, GrouperWorkflowConfigParams.class);
      return configParams;
    } catch (Exception e) {
      LOG.error(
          "could not convert: " + params + " to GrouperWorkflowConfigParams object");
      throw new RuntimeException(
          "could not convert json string to GrouperWorkflowConfigParams object", e);
    }

  }

  public static GrouperWorkflowApprovalStates buildApprovalStatesFromJsonString(
      String workflowApprovalStates) {
    try {
      GrouperWorkflowApprovalStates approvalStates = GrouperWorkflowSettings.objectMapper
          .readValue(workflowApprovalStates, GrouperWorkflowApprovalStates.class);
      return approvalStates;
    } catch (Exception e) {
      LOG.error("could not convert: " + workflowApprovalStates
          + " to GrouperWorkflowApprovalStates object");
      throw new RuntimeException(
          "could not convert json string to GrouperWorkflowApprovalStates object", e);
    }

  }

  public static GrouperWorkflowConfigParams getDefaultConfigParams() {

    GrouperWorkflowConfigParams configParams = new GrouperWorkflowConfigParams();

    List<GrouperWorkflowConfigParam> params = new ArrayList<GrouperWorkflowConfigParam>();
    GrouperWorkflowConfigParam param1 = new GrouperWorkflowConfigParam();
    param1.setEditableInStates(Arrays.asList(INITIATE_STATE));
    param1.setParamName("notes");
    param1.setType("textarea");
    params.add(param1);

    GrouperWorkflowConfigParam param2 = new GrouperWorkflowConfigParam();
    param2.setEditableInStates(Arrays.asList("supervisor", "dataOwner"));
    param2.setParamName("notesForApprovers");
    param2.setType("textarea");
    params.add(param2);

    configParams.setParams(params);

    return configParams;
  }

  public static GrouperWorkflowApprovalStates getDefaultApprovalStates() {

    GrouperWorkflowApprovalStates states = new GrouperWorkflowApprovalStates();

    List<GrouperWorkflowApprovalState> listOfStates = new ArrayList<GrouperWorkflowApprovalState>();

    GrouperWorkflowApprovalState initiateState = new GrouperWorkflowApprovalState();
    initiateState.setStateName(INITIATE_STATE);
    listOfStates.add(initiateState);

    GrouperWorkflowApprovalState groupManager = new GrouperWorkflowApprovalState();
    groupManager.setStateName("groupManager");
    groupManager.setApproverManagersOfGroupId("sdgf76gdf87");
    listOfStates.add(groupManager);

    GrouperWorkflowApprovalState complete = new GrouperWorkflowApprovalState();
    complete.setStateName(COMPLETE_STATE);

    GrouperWorkflowApprovalAction action = new GrouperWorkflowApprovalAction();
    action.setActionName("assignToGroup");
    action.setActionArg0("sgk234kh234");

    List<GrouperWorkflowApprovalAction> actions = new ArrayList<GrouperWorkflowApprovalAction>();
    actions.add(action);

    complete.setActions(actions);

    listOfStates.add(complete);

    states.setStates(listOfStates);

    return states;
  }

}
