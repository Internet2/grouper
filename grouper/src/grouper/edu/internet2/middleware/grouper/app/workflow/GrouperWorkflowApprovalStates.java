package edu.internet2.middleware.grouper.app.workflow;

import java.util.ArrayList;
import java.util.List;

public class GrouperWorkflowApprovalStates {
  
  private List<GrouperWorkflowApprovalState> states = new ArrayList<GrouperWorkflowApprovalState>();

  
  public List<GrouperWorkflowApprovalState> getStates() {
    return states;
  }

  public void setStates(List<GrouperWorkflowApprovalState> states) {
    this.states = states;
  }
  
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
  
  public GrouperWorkflowApprovalState getStateByName(String stateName) {
    
    for (GrouperWorkflowApprovalState state: states) {
      if (state.getStateName().equals(stateName)) {
        return state;
      }
    }
    
    return null;
    
  }
  
}
