package edu.internet2.middleware.grouper.app.workflow;

import static edu.internet2.middleware.grouper.app.workflow.GrouperWorkflowConstants.COMPLETE_STATE;
import static edu.internet2.middleware.grouper.app.workflow.GrouperWorkflowConstants.INITIATE_STATE;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.util.GrouperUtil;

public class GrouperWorkflowApprovalStates {
  
  /**
   * logger 
   */
  private static final Log LOG = GrouperUtil.getLog(GrouperWorkflowApprovalStates.class);
  
  /**
   * list of states
   */
  private List<GrouperWorkflowApprovalState> states = new ArrayList<GrouperWorkflowApprovalState>();

  /**
   * list of states
   * @return
   */
  public List<GrouperWorkflowApprovalState> getStates() {
    return states;
  }

  /**
   * list of states
   * @param states
   */
  public void setStates(List<GrouperWorkflowApprovalState> states) {
    this.states = states;
  }
  
  /**
   * get state after the given state
   * @param state
   * @return
   */
  public GrouperWorkflowApprovalState stateAfter(String state) {
    
    int i = 0;
    for (; i<states.size(); i++) {
      if (states.get(i).getStateName().equals(state)) {
        break;
      }
    }
    
    if (i == states.size() - 1) {
      return null;
    }
    
    return states.get(i+1);
  }
  
  /**
   * get state object by name
   * @param stateName
   * @return
   */
  public GrouperWorkflowApprovalState getStateByName(String stateName) {
    
    for (GrouperWorkflowApprovalState state: states) {
      if (state.getStateName().equals(stateName)) {
        return state;
      }
    }
    
    return null;
    
  }
  
  private static GrouperWorkflowApprovalStates getDefaultApprovalStates(String groupId) {

    GrouperWorkflowApprovalStates states = new GrouperWorkflowApprovalStates();

    List<GrouperWorkflowApprovalState> listOfStates = new ArrayList<GrouperWorkflowApprovalState>();

    GrouperWorkflowApprovalState initiateState = new GrouperWorkflowApprovalState();
    initiateState.setStateName(INITIATE_STATE);
    listOfStates.add(initiateState);

    GrouperWorkflowApprovalState groupManager = new GrouperWorkflowApprovalState();
    groupManager.setStateName("groupManager");
    groupManager.setApproverManagersOfGroupId(groupId);
    listOfStates.add(groupManager);

    GrouperWorkflowApprovalState complete = new GrouperWorkflowApprovalState();
    complete.setStateName(COMPLETE_STATE);

    GrouperWorkflowApprovalAction action = new GrouperWorkflowApprovalAction();
    action.setActionName("assignToGroup");
    action.setActionArg0(groupId);

    List<GrouperWorkflowApprovalAction> actions = new ArrayList<GrouperWorkflowApprovalAction>();
    actions.add(action);

    complete.setActions(actions);

    listOfStates.add(complete);

    states.setStates(listOfStates);

    return states;
  }
  
  /**
   * get default approval states for a given groupId
   * @param groupId
   * @return
   */
  public static String getDefaultApprovalStatesString(String groupId) {
    GrouperWorkflowApprovalStates defaultApprovalStates = getDefaultApprovalStates(groupId);
    try {      
      return GrouperWorkflowSettings.objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(defaultApprovalStates);
    } catch(Exception e) {
      throw new RuntimeException("Could not convert default approval states json into string");
    }
  }
  
  /**
   * build approval states object from json string
   * @param workflowApprovalStates
   * @return
   */
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
  
}
