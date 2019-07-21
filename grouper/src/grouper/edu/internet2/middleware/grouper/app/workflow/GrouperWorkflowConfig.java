package edu.internet2.middleware.grouper.app.workflow;

import static edu.internet2.middleware.grouper.app.workflow.GrouperWorkflowConstants.INITIATE_STATE;
import static edu.internet2.middleware.grouper.app.workflow.GrouperWorkflowConstants.WORKFLOW_CONFIG_ENABLED_FALSE;
import static edu.internet2.middleware.grouper.app.workflow.GrouperWorkflowConstants.WORKFLOW_CONFIG_ENABLED_NO_NEW_SUBMISSIONS;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.cfg.text.GrouperTextContainer;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

public class GrouperWorkflowConfig {

  /**
   * logger 
   */
  private static final Log LOG = GrouperUtil.getLog(GrouperWorkflowConfig.class);

  /**
   * workflow config type
   */
  private String workflowConfigType;

  /**
   * approval states
   */
  private GrouperWorkflowApprovalStates workflowApprovalStates;

  /**
   * json formatted approval states string
   */
  private String workflowConfigApprovalsString;

  /**
   * name of the config
   */
  private String workflowConfigName;

  /**
   * id of the config
   */
  private String workflowConfigId;

  /**
   * description of the config
   */
  private String workflowConfigDescription;

  /**
   * params to generate and validate the form
   */
  private GrouperWorkflowConfigParams configParams;

  /**
   * json formatted params
   */
  private String workflowConfigParamsString;

  /**
   * html form
   */
  private String workflowConfigForm;

  /**
   * members of this group can view the config and all instances
   */
  private String workflowConfigViewersGroupId;

  /**
   * send waiting for approval email setting
   */
  private boolean workflowConfigSendEmail = true;

  /**
   * workflow config enabled setting
   */
  private String workflowConfigEnabled = "true";

  /**
   * attribute assignment id
   */
  private String attributeAssignmentMarkerId;

  /**
   * owner group
   */
  private Group ownerGroup;
  
  
  public GrouperWorkflowConfig() {
    workflowConfigForm = GrouperTextContainer.retrieveFromRequest().getText().get("workflowConfigDefaultHtmlForm");
  }

  /**
   * workflow config type
   * @return
   */
  public String getWorkflowConfigType() {
    return workflowConfigType;
  }

  /**
   * workflow config type
   * @param workflowConfigType
   */
  public void setWorkflowConfigType(String workflowConfigType) {
    this.workflowConfigType = workflowConfigType;
  }

  /**
   * name of the config
   * @return
   */
  public String getWorkflowConfigName() {
    return workflowConfigName;
  }

  /**
   * name of the config
   * @param workflowConfigName
   */
  public void setWorkflowConfigName(String workflowConfigName) {
    this.workflowConfigName = workflowConfigName;
  }

  /**
   * id of the config
   * @return
   */
  public String getWorkflowConfigId() {
    return workflowConfigId;
  }

  /**
   * id of the config
   * @param workflowConfigId
   */
  public void setWorkflowConfigId(String workflowConfigId) {
    this.workflowConfigId = workflowConfigId;
  }

  /**
   * description of the config
   * @return
   */
  public String getWorkflowConfigDescription() {
    return workflowConfigDescription;
  }

  /**
   * description of the config
   * @param workflowConfigDescription
   */
  public void setWorkflowConfigDescription(String workflowConfigDescription) {
    this.workflowConfigDescription = workflowConfigDescription;
  }

  /**
   * html form
   * @return
   */
  public String getWorkflowConfigForm() {
    return workflowConfigForm;
  }

  /**
   * html form
   * @param workflowConfigForm
   */
  public void setWorkflowConfigForm(String workflowConfigForm) {
    this.workflowConfigForm = workflowConfigForm;
  }

  /**
   * members of this group can view the config and all instances
   * @return
   */
  public String getWorkflowConfigViewersGroupId() {
    return workflowConfigViewersGroupId;
  }

  /**
   * members of this group can view the config and all instances
   * @param workflowConfigViewersGroupId
   */
  public void setWorkflowConfigViewersGroupId(String workflowConfigViewersGroupId) {
    this.workflowConfigViewersGroupId = workflowConfigViewersGroupId;
  }

  /**
   * send waiting for approval email setting
   * @return
   */
  public boolean isWorkflowConfigSendEmail() {
    return workflowConfigSendEmail;
  }

  /**
   * send waiting for approval email setting
   * @param workflowConfigSendEmail
   */
  public void setWorkflowConfigSendEmail(boolean workflowConfigSendEmail) {
    this.workflowConfigSendEmail = workflowConfigSendEmail;
  }

  /**
   * workflow config enabled setting
   * @return
   */
  public String getWorkflowConfigEnabled() {
    return workflowConfigEnabled;
  }

  /**
   * workflow config enabled setting
   * @param workflowConfigEnabled
   */
  public void setWorkflowConfigEnabled(String workflowConfigEnabled) {
    this.workflowConfigEnabled = workflowConfigEnabled;
  }

  /**
   * attribute assignment id
   * @return
   */
  public String getAttributeAssignmentMarkerId() {
    return attributeAssignmentMarkerId;
  }

  /**
   * attribute assignment id
   * @param attributeAssignmentMarkerId
   */
  public void setAttributeAssignmentMarkerId(String attributeAssignmentMarkerId) {
    this.attributeAssignmentMarkerId = attributeAssignmentMarkerId;
  }

  /**
   * params to generate and validate the form
   * @return
   */
  public GrouperWorkflowConfigParams getConfigParams() {
    return configParams;
  }

  /**
   * params to generate and validate the form
   * @param configParams
   */
  public void setConfigParams(GrouperWorkflowConfigParams configParams) {
    this.configParams = configParams;
  }

  /**
   * approval states
   * @return
   */
  public GrouperWorkflowApprovalStates getWorkflowApprovalStates() {
    return workflowApprovalStates;
  }

  /**
   * approval states
   * @param workflowApprovalStates
   */
  public void setWorkflowApprovalStates(
      GrouperWorkflowApprovalStates workflowApprovalStates) {
    this.workflowApprovalStates = workflowApprovalStates;
  }

  /**
   * json formatted approval states string
   * @return
   */
  public String getWorkflowConfigApprovalsString() {
    return workflowConfigApprovalsString;
  }

  /**
   * json formatted approval states string
   * @param workflowConfigApprovalsString
   */
  public void setWorkflowConfigApprovalsString(String workflowConfigApprovalsString) {
    this.workflowConfigApprovalsString = workflowConfigApprovalsString;
  }

  /**
   * json formatted params
   * @return
   */
  public String getWorkflowConfigParamsString() {
    return workflowConfigParamsString;
  }

  
  /**
   * json formatted params
   * @param workflowConfigParamsString
   */
  public void setWorkflowConfigParamsString(String workflowConfigParamsString) {
    this.workflowConfigParamsString = workflowConfigParamsString;
  }

  /**
   * Can the given subject initiate this workflow
   * @param subject
   * @return
   */
  public boolean canSubjectInitiateWorkflow(final Subject subject) {
   
    List<String> configTypesToIgnore = Arrays.asList(WORKFLOW_CONFIG_ENABLED_FALSE, WORKFLOW_CONFIG_ENABLED_NO_NEW_SUBMISSIONS);
    
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
      group = GroupFinder.findByName(GrouperSession.staticGrouperSession(), allowedGroupId, false);
    }
    if (group == null) {
      LOG.error("allowed group id " + allowedGroupId + " not found in workflow id "
              + workflowConfigId + " Was the group deleted??");
      return false;
    }
    
    Member member = MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), subject, false);
    return member != null && member.isMember(group);
        
  }
  
  /**
   * check if given subject is a member of viewers group for this workflow config
   * @param subject
   * @return
   */
  public boolean isSubjectInViewersGroup(Subject subject) {
    
    if (StringUtils.isNotBlank(workflowConfigViewersGroupId)) {
      Group viewersGroup = GroupFinder.findByUuid(GrouperSession.staticGrouperSession(), workflowConfigViewersGroupId, false);
      if (viewersGroup == null) {
        viewersGroup = GroupFinder.findByName(GrouperSession.staticGrouperSession(), workflowConfigViewersGroupId, false);
      }
      if (viewersGroup == null) {
        LOG.error("viewers group for workflow config "+workflowConfigName +" is not found.");
        return false;
      }
      return viewersGroup.hasMember(subject);
    }
    
    return false;
  } 
  
  public String buildInitialHtml(String state) {
    
    if (StringUtils.isNotBlank(workflowConfigForm)) {
      return buildHtmlFromConfigForm(state);
    }
    return buildHtmlFromParams(state);

  }

  /**
   * build html from form for this workflow config
   * @param state
   * @return
   */
  private String buildHtmlFromConfigForm(String state) {
    
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

  /**
   * build html form using only params json
   * @param state
   * @return
   */
  private String buildHtmlFromParams(String state) {

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
      html.append(buildInputField(param, state));
      html.append("</td></tr>");
    }
    html.append("</table>");
    return html.toString();
  }

  private String buildInputField(GrouperWorkflowConfigParam param, String currentState) {

    StringBuilder field = new StringBuilder();

    List<String> editableInStates = param.getEditableInStates();
    String disabled = "";

    if (currentState == null || !editableInStates.contains(currentState)) {
      disabled = "disabled";
    }

    if (param.getType().equals("textarea")) {
      field.append("<textarea cols='20' rows='3' " + disabled + " name="
          + param.getParamName() + ">");
      field.append("</textarea>");
    } else if (param.getType().equals("textfield")) {
      field.append(
          "<input type='text'" + disabled + " name=" + param.getParamName() + ">");
      field.append("</input>");
    } else if (param.getType().equals("checkbox")) {
      field.append(
          "<input type='checkbox'" + disabled + " name=" + param.getParamName() + " ");
      field.append("></input>");
    } else {
      throw new RuntimeException("Invalid type: " + param.getType());
    }

    return field.toString();
  }

  /**
   * owner group
   * @param ownerGroup
   */
  public void setOwnerGroup(Group ownerGroup) {
    this.ownerGroup = ownerGroup;
  }
  
  /**
   * owner group
   * @return
   */
  public Group getOwnerGroup() {
    return ownerGroup;
  }

}
