package edu.internet2.middleware.grouper.app.workflow;

import java.util.ArrayList;
import java.util.List;

public class GrouperWorkflowInstanceLogEntries {

  /**
   * list of log entries objects
   */
  private List<GrouperWorkflowInstanceLogEntry> logEntries = new ArrayList<GrouperWorkflowInstanceLogEntry>();

  /**
   * list of log entries objects
   * @return
   */
  public List<GrouperWorkflowInstanceLogEntry> getLogEntries() {
    return logEntries;
  }

  /**
   * list of log entries objects
   * @param logEntries
   */
  public void setLogEntries(List<GrouperWorkflowInstanceLogEntry> logEntries) {
    this.logEntries = logEntries;
  }
  
  /**
   * get log entry object by action name
   * @param actionName
   * @return
   */
  public GrouperWorkflowInstanceLogEntry getLogEntryByActionName(String actionName) {
    
    for (GrouperWorkflowInstanceLogEntry logEntry: logEntries) {
      if (logEntry.getAction().equals(actionName)) {
        return logEntry;
      }
    }
    
    return null;
  }
  
}
