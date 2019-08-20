package edu.internet2.middleware.grouper.app.workflow;

import java.util.Date;

import edu.internet2.middleware.subject.Subject;

public class GrouperWorkflowInstanceLogEntry {
  
  /**
   * subject source id which generated this log entry
   */
  private String subjectSourceId;
  
  /**
   * subject id which generated this log entry
   */
  private String subjectId;
  
  /**
   * action
   */
  private String action;
  
  /**
   * state name
   */
  private String state;
  
  /**
   * millis since 1970 when this log entry was generated
   */
  private Long millisSince1970;

  /**
   * subject source id which generated this log entry
   * @return
   */
  public String getSubjectSourceId() {
    return subjectSourceId;
  }

  /**
   * subject source id which generated this log entry
   * @param subjectSourceId
   */
  public void setSubjectSourceId(String subjectSourceId) {
    this.subjectSourceId = subjectSourceId;
  }

  /**
   * subject id which generated this log entry
   * @return
   */
  public String getSubjectId() {
    return subjectId;
  }

  /**
   * subject id which generated this log entry
   * @param subjectId
   */
  public void setSubjectId(String subjectId) {
    this.subjectId = subjectId;
  }

  /**
   * action
   * @return
   */
  public String getAction() {
    return action;
  }

  /**
   * action
   * @param action
   */
  public void setAction(String action) {
    this.action = action;
  }

  /**
   * state name
   * @return
   */
  public String getState() {
    return state;
  }

  /**
   * state name
   * @param state
   */
  public void setState(String state) {
    this.state = state;
  }

  /**
   * millis since 1970 when this log entry was generated
   * @return
   */
  public Long getMillisSince1970() {
    return millisSince1970;
  }

  /**
   * millis since 1970 when this log entry was generated
   * @param millisSince1970
   */
  public void setMillisSince1970(Long millisSince1970) {
    this.millisSince1970 = millisSince1970;
  }
  
  public static GrouperWorkflowInstanceLogEntry createLogEntry(Subject subject, 
      Date date, String state, String action) {
    GrouperWorkflowInstanceLogEntry logEntry = new GrouperWorkflowInstanceLogEntry();
    logEntry.setState(state);
    logEntry.setAction(action);
    logEntry.setSubjectId(subject != null ? subject.getId(): null);
    logEntry.setSubjectSourceId(subject != null ? subject.getSourceId(): null);
    logEntry.setMillisSince1970(date.getTime());
    return logEntry;
  }
  
}
