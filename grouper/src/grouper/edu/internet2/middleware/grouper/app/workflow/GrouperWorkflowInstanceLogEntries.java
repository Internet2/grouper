package edu.internet2.middleware.grouper.app.workflow;

import java.util.ArrayList;
import java.util.List;

public class GrouperWorkflowInstanceLogEntries {

  private List<GrouperWorkflowInstanceLogEntry> logEntries = new ArrayList<GrouperWorkflowInstanceLogEntry>();

  
  public List<GrouperWorkflowInstanceLogEntry> getLogEntries() {
    return logEntries;
  }

  
  public void setLogEntries(List<GrouperWorkflowInstanceLogEntry> logEntries) {
    this.logEntries = logEntries;
  }
  
  public GrouperWorkflowInstanceLogEntry getLogEntryByActionName(String actionName) {
    
    for (GrouperWorkflowInstanceLogEntry logEntry: logEntries) {
      if (logEntry.getAction().equals(actionName)) {
        return logEntry;
      }
    }
    
    return null;
  }
  
}
