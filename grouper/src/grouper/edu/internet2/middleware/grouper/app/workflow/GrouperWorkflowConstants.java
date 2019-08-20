package edu.internet2.middleware.grouper.app.workflow;

public class GrouperWorkflowConstants {
  
  // config enabled constants
  public static final String WORKFLOW_CONFIG_ENABLED_FALSE = "false";
  public static final String WORKFLOW_CONFIG_ENABLED_TRUE = "true";
  public static final String WORKFLOW_CONFIG_ENABLED_NO_NEW_SUBMISSIONS = "noNewSubmissions";
  
  // workflow state constants
  public static final String INITIATE_STATE = "initiate";
  public static final String COMPLETE_STATE = "complete";
  public static final String EXCEPTION_STATE = "exception";
  public static final String REJECTED_STATE = "rejected";
  
  // log entry constants
  public static final String INITIATE_ACTION = "initiate";
  public static final String APPROVE_ACTION = "approve";
  public static final String DISAPPROVE_ACTION = "disapprove";
  public static final String ADD_SUBJECT_TO_GROUP_ACTION = "addedSubjectToGroup";
  public static final String WORKFLOW_STATE_CHANGE_ACTION = "workflowStateChange";
  
}
