package edu.internet2.middleware.grouper.app.workflow;

import java.util.List;

public class GrouperWorkflowApprovalState {
  
  public static final String INITIATE_STATE = "initiate";
  public static final String COMPLETE_STATE = "complete";
  
  private String stateName;
  
  private String allowedGroupId;
  
  private String approverSubjectId;
  
  private String approverSubjectSourceId;
  
  private String approverNotifyGroupId;
  
  private String approverManagersOfGroupId;
  
  private String approverGroupId;
  
  private List<GrouperWorkflowApprovalAction> actions;

  
  public String getStateName() {
    return stateName;
  }

  
  public void setStateName(String stateName) {
    this.stateName = stateName;
  }

  
  public String getAllowedGroupId() {
    return allowedGroupId;
  }

  
  public void setAllowedGroupId(String allowedGroupId) {
    this.allowedGroupId = allowedGroupId;
  }

  
  public String getApproverSubjectId() {
    return approverSubjectId;
  }

  
  public void setApproverSubjectId(String approverSubjectId) {
    this.approverSubjectId = approverSubjectId;
  }

  
  public String getApproverSubjectSourceId() {
    return approverSubjectSourceId;
  }

  
  public void setApproverSubjectSourceId(String approverSubjectSourceId) {
    this.approverSubjectSourceId = approverSubjectSourceId;
  }

  
  public String getApproverNotifyGroupId() {
    return approverNotifyGroupId;
  }

  
  public void setApproverNotifyGroupId(String approverNotifyGroupId) {
    this.approverNotifyGroupId = approverNotifyGroupId;
  }

  
  public List<GrouperWorkflowApprovalAction> getActions() {
    return actions;
  }

  
  public void setActions(List<GrouperWorkflowApprovalAction> actions) {
    this.actions = actions;
  }


  
  public String getApproverManagersOfGroupId() {
    return approverManagersOfGroupId;
  }


  
  public void setApproverManagersOfGroupId(String approverManagersOfGroupId) {
    this.approverManagersOfGroupId = approverManagersOfGroupId;
  }


  
  public String getApproverGroupId() {
    return approverGroupId;
  }


  
  public void setApproverGroupId(String approverGroupId) {
    this.approverGroupId = approverGroupId;
  }
  
  

}
