package edu.internet2.middleware.grouper.app.workflow;


public class GrouperWorkflowConfig {
  
  private String workflowConfigType;
  
  private String workflowConfigApprovals;
  
  private String workflowConfigName;
  
  private String workflowConfigId;
  
  private String workflowConfigDescription;
  
  private String workflowConfigParams;
  
  private String workflowConfigForm;
  
  private String workflowConfigViewersGroupId;
  
  private boolean workflowConfigSendEmail;
  
  private String workflowConfigEnabled = "true";
  
  
  public String getWorkflowConfigType() {
    return workflowConfigType;
  }

  
  public void setWorkflowConfigType(String workflowConfigType) {
    this.workflowConfigType = workflowConfigType;
  }

  
  public String getWorkflowConfigApprovals() {
    return workflowConfigApprovals;
  }

  
  public void setWorkflowConfigApprovals(String workflowConfigApprovals) {
    this.workflowConfigApprovals = workflowConfigApprovals;
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

  
  public String getWorkflowConfigParams() {
    return workflowConfigParams;
  }

  
  public void setWorkflowConfigParams(String workflowConfigParams) {
    this.workflowConfigParams = workflowConfigParams;
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

}
