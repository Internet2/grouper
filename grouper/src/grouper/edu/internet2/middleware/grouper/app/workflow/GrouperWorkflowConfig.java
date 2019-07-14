package edu.internet2.middleware.grouper.app.workflow;

import static edu.internet2.middleware.grouper.app.workflow.GrouperWorkflowApprovalState.COMPLETE_STATE;
import static edu.internet2.middleware.grouper.app.workflow.GrouperWorkflowApprovalState.INITIATE_STATE;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.cfg.text.GrouperTextContainer;
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
    
    final List<String> validParamTypes = Arrays.asList("textarea", "text", "checkbox");
    
    if (StringUtils.isBlank(workflowConfigType)) {
      errors.add(contentKeys.get("workflowTypeRequiredError"));
    }
    
    if (!GrouperWorkflowSettings.configTypes().contains(workflowConfigType)) {
      String error = contentKeys.get("workflowTypeUnknownError");
      error = error.replace("$$workflowType$$", workflowConfigType);
      errors.add(error);
    }
    
    if (StringUtils.isBlank(workflowConfigApprovalsString)) {
      errors.add(contentKeys.get("workflowApprovalsRequiredError"));
    } else {
      try {
        workflowApprovalStates = GrouperWorkflowConfig.buildApprovalStatesFromJsonString(workflowConfigApprovalsString);
      } catch (Exception e) {
        errors.add(contentKeys.get("workflowApprovalsInvalidJsonError"));
      }
      
      if (workflowApprovalStates != null) {
          
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
                String error = contentKeys.get("workflowApprovalsStateAllowedGroupNotFound");
                error = error.replace("$$stateName$$", stateName);
                error = error.replace("$$groupId$$", allowedGroupId);
                errors.add(error);
              }
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
          
          // if approvers group id is there, make sure it's valid
          if (StringUtils.isNotEmpty(state.getApproverGroupId())) {
            Group approverGroup = GroupFinder.findByUuid(GrouperSession.staticGrouperSession(), state.getApproverGroupId(), false);
            if (approverGroup == null) {
              String error = contentKeys.get("workflowApprovalsStateApproverGroupNotFound");
              error = error.replace("$$stateName$$", stateName);
              error = error.replace("$$groupId$$", state.getApproverGroupId());
              errors.add(error);
            }
          }
          
          if (StringUtils.isNotEmpty(state.getApproverManagersOfGroupId())) {
            Group approverGroup = GroupFinder.findByUuid(GrouperSession.staticGrouperSession(), state.getApproverManagersOfGroupId(), false);
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
            Group groupToNotify = GroupFinder.findByUuid(GrouperSession.staticGrouperSession(), state.getApproverNotifyGroupId(), false);
            if (groupToNotify == null) {
              String error = contentKeys.get("workflowApprovalsStateGroupToNotifyNotFound");
              error = error.replace("$$stateName$$", stateName);
              error = error.replace("$$groupId$$", state.getApproverNotifyGroupId());
              errors.add(error);
            }
          }
          
        }
  
        if (!isInitiateStateAvailable) {
          errors.add(contentKeys.get("workflowApprovalsInitiateStateRequiredError"));
        }
  
        if (!isCompleteStateAvailable) {
          errors.add(contentKeys.get("workflowApprovalsCompleteStateRequiredError"));
        }
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
    
    
    if (StringUtils.isBlank(workflowConfigParamsString)) {
      errors.add(contentKeys.get("workflowConfigParamsRequiredError"));
    } else {
      try {
        configParams = GrouperWorkflowConfig.buildParamsFromJsonString(workflowConfigParamsString);
      } catch (Exception e) {
        errors.add(contentKeys.get("workflowParamsInvalidJsonError"));
      }
      
      if (configParams != null) {
        
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
          
          for (String editableState: editableStates) {
            GrouperWorkflowApprovalState workflowApprovalState = workflowApprovalStates.getStateByName(editableState);
            if (workflowApprovalState == null) {
              String error = contentKeys.get("workflowParamsEditableStateNotFoundInApprovalStates"); 
              error = error.replace("$$editableState$$", editableState);
              errors.add(error);
            }
          }
          
          if (!validParamTypes.contains(paramType)) {
            String error = contentKeys.get("workflowParamsInvalidParamType");
            error = error.replace("$$paramType$$", paramType);
            errors.add(error);
          }
          
        }
      }

      if (configParams.getParams().size() > 10) {
        errors.add(contentKeys.get("workflowParamsExceedsMaxSizeError"));
      }
    }
    
    errors.addAll(validateConfigHtmlForm(configParams));

    if (StringUtils.isNotBlank(workflowConfigViewersGroupId)) {
      
      Group workflowViewersGroup = GroupFinder.findByUuid(GrouperSession.staticGrouperSession(), workflowConfigViewersGroupId, false);
      if (workflowViewersGroup == null) {
        errors.add(contentKeys.get("workflowViewerGroupIdNotFoundError"));
      }
    }

    if (StringUtils.isBlank(workflowConfigEnabled)) {
      errors.add(contentKeys.get("workflowConfigEnabledRequiredError"));
    }

    //TODO move to constants
    List<String> validEnabledValues = Arrays.asList("true", "false", "noNewSubmissions");

    if (!validEnabledValues.contains(workflowConfigEnabled)) {
      errors.add(contentKeys.get("workflowConfigEnabledInvalidValueError"));
    }

    return errors;

  }
  
  private  List<String> validateConfigHtmlForm(GrouperWorkflowConfigParams configParams) {
    
    final List<String> errors = new ArrayList<String>();
    final Map<String, String> contentKeys = GrouperTextContainer.retrieveFromRequest().getText();
    
    try {
      Document document = Jsoup.parse(workflowConfigForm);
      Elements inputElements = document.getElementsByTag("input");
      
      for (Element inputElement: inputElements) {
        String nameOfElement = inputElement.attr("name");
        GrouperWorkflowConfigParam param = configParams.getConfigParamByNameAndType(nameOfElement, inputElement.attr("type"));
        if (param == null) {
          String error = contentKeys.get("workflowConfigFormElementNotFoundInJsonParams");
          error = error.replace("$$elementName$$", nameOfElement);
          error = error.replace("$$elementType$$", inputElement.attr("type"));
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
        }
      }
    } catch(Exception e) {
      errors.add(contentKeys.get("workflowConfigInvalidConfigForm"));
    }
     
    return errors;
  }
  
  public boolean canSubjectInitiateWorkflow(final Subject subject) {
   
    List<String> configTypesToIgnore = Arrays.asList("false", "noNewSubmissions");
    
    if (configTypesToIgnore.contains(workflowConfigEnabled)) {
      return false;
    }
     
    GrouperWorkflowApprovalState initiateState = workflowApprovalStates.getStateByName(INITIATE_STATE);
    final String allowedGroupId = initiateState.getAllowedGroupId();
    if (StringUtils.isBlank(allowedGroupId)) {
      return true;
    }
    
    Group group = GroupFinder.findByUuid(GrouperSession.staticGrouperSession(), allowedGroupId, false);
    if (group == null) {
      LOG.error("allowed group id " + allowedGroupId + " not found in workflow id "
              + workflowConfigId + " Was the group delted??");
      return false;
    }
    
    Member member = MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), subject, false);
    return member != null && member.isMember(group);
        
  }
  
  public boolean isSubjectInViewersGroup(Subject subject) {
    
    if (StringUtils.isNotBlank(workflowConfigViewersGroupId)) {
      Group viewersGroup = GroupFinder.findByUuid(GrouperSession.staticGrouperSession(), workflowConfigViewersGroupId, false);
      if (viewersGroup == null) {
        LOG.error("viewers group for workflow config "+workflowConfigName +" is not found.");
        return false;
      }
      return viewersGroup.hasMember(subject);
    }
    
    return false;
  } 

  public String buildHtmlFromConfigForm(String state) {
    
    Document document = Jsoup.parse(workflowConfigForm);
    
    for (GrouperWorkflowConfigParam param : configParams.getParams()) {

      String elementName = param.getParamName();
      Element element = document.selectFirst("[name="+elementName+"]");
      List<String> editableInStates = param.getEditableInStates();
      
      if (state == null || !editableInStates.contains(state)) {
        element.attr("disabled", "disabled");
      }
     
    }
    
    return document.html();
  }

  public String buildHtmlFromParams(boolean addPlaceholderForValues, String state) {

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
      html.append(buildInputField(param, addPlaceholderForValues, state));
      html.append("</td></tr>");
    }
    html.append("</table>");
    return html.toString();
  }

  private String buildInputField(GrouperWorkflowConfigParam param,
      boolean addPlaceholderForValues, String currentState) {

    StringBuilder field = new StringBuilder();

    List<String> editableInStates = param.getEditableInStates();
    String disabled = "";

    //TODO editable in states should be a string with comma separated items
    if (currentState == null || !editableInStates.contains(currentState)) {
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

  // TODO move to GrouperWorkflowConfigParams
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

  private static GrouperWorkflowConfigParams getDefaultConfigParams() {

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
  
  public static String getDefaultConfigParamsString() {
    GrouperWorkflowConfigParams defaultConfigParams = getDefaultConfigParams();
    try {      
      return GrouperWorkflowSettings.objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(defaultConfigParams);
    } catch(Exception e) {
      throw new RuntimeException("Could not convert default config params json into string");
    }
  }

  private static GrouperWorkflowApprovalStates getDefaultApprovalStates() {

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
  
  public static String getDefaultApprovalStatesString() {
    GrouperWorkflowApprovalStates defaultApprovalStates = getDefaultApprovalStates();
    try {      
      return GrouperWorkflowSettings.objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(defaultApprovalStates);
    } catch(Exception e) {
      throw new RuntimeException("Could not convert default approval states json into string");
    }
  }

}
