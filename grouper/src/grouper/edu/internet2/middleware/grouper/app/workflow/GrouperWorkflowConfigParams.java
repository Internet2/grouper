package edu.internet2.middleware.grouper.app.workflow;

import java.util.ArrayList;
import java.util.List;

public class GrouperWorkflowConfigParams {

  private List<GrouperWorkflowConfigParam> params = new ArrayList<GrouperWorkflowConfigParam>();

  
  public List<GrouperWorkflowConfigParam> getParams() {
    return params;
  }

  
  public void setParams(List<GrouperWorkflowConfigParam> params) {
    this.params = params;
  }
  
  
}
