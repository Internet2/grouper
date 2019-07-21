package edu.internet2.middleware.grouper.app.workflow;

import java.util.List;

public class GrouperWorkflowApprovalState {
  
  /**
   * name of the state
   */
  private String stateName;
  
  /**
   * members of allowed group id are able to join the group via workflow.
   * must be populated for initiate state
   */
  private String allowedGroupId;
  
  /**
   * subject id of approver for this state
   */
  private String approverSubjectId;
  
  /**
   * subject source id of approver for this state
   */
  private String approverSubjectSourceId;
  
  /**
   * admins and updaters of this group can approve/disapprove
   */
  private String approverManagersOfGroupId;
  
  /**
   * members of this group can approve/disapprove
   */
  private String approverGroupId;
  
  /**
   * list of actions to be taken
   */
  private List<GrouperWorkflowApprovalAction> actions;

  /**
   * @return name of the state
   */
  public String getStateName() {
    return stateName;
  }

  /**
   * name of the state
   * @param stateName
   */
  public void setStateName(String stateName) {
    this.stateName = stateName;
  }

  /**
   * members of allowed group id are able to join the group via workflow.
   * must be populated for initiate state
   * @return
   */
  public String getAllowedGroupId() {
    return allowedGroupId;
  }

  /**
   * members of allowed group id are able to join the group via workflow.
   * must be populated for initiate state
   * @param allowedGroupId
   */
  public void setAllowedGroupId(String allowedGroupId) {
    this.allowedGroupId = allowedGroupId;
  }

  /**
   * subject id of approver for this state
   * @return
   */
  public String getApproverSubjectId() {
    return approverSubjectId;
  }

  /**
   * subject id of approver for this state
   * @param approverSubjectId
   */
  public void setApproverSubjectId(String approverSubjectId) {
    this.approverSubjectId = approverSubjectId;
  }

  /**
   * subject source id of approver for this state
   * @return
   */
  public String getApproverSubjectSourceId() {
    return approverSubjectSourceId;
  }

  /**
   * subject source id of approver for this state
   * @param approverSubjectSourceId
   */
  public void setApproverSubjectSourceId(String approverSubjectSourceId) {
    this.approverSubjectSourceId = approverSubjectSourceId;
  }

  /**
   * list of actions to be taken
   * @return
   */
  public List<GrouperWorkflowApprovalAction> getActions() {
    return actions;
  }

  /**
   * list of actions to be taken
   * @param actions
   */
  public void setActions(List<GrouperWorkflowApprovalAction> actions) {
    this.actions = actions;
  }


  /**
   * admins and updaters of this group can approve/disapprove
   * @return
   */
  public String getApproverManagersOfGroupId() {
    return approverManagersOfGroupId;
  }


  /**
   * admins and updaters of this group can approve/disapprove
   * @param approverManagersOfGroupId
   */
  public void setApproverManagersOfGroupId(String approverManagersOfGroupId) {
    this.approverManagersOfGroupId = approverManagersOfGroupId;
  }


  /**
   * members of this group can approve/disapprove
   * @return
   */
  public String getApproverGroupId() {
    return approverGroupId;
  }


  /**
   * members of this group can approve/disapprove
   * @param approverGroupId
   */
  public void setApproverGroupId(String approverGroupId) {
    this.approverGroupId = approverGroupId;
  }
  
  

}
