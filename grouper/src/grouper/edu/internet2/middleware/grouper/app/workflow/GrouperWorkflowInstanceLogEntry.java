package edu.internet2.middleware.grouper.app.workflow;


public class GrouperWorkflowInstanceLogEntry {
  
  private String subjectSourceId;
  
  private String subjectId;
  
  private String action;
  
  private String state;
  
  private Long millisSince1970;

  
  public String getSubjectSourceId() {
    return subjectSourceId;
  }

  
  public void setSubjectSourceId(String subjectSourceId) {
    this.subjectSourceId = subjectSourceId;
  }

  
  public String getSubjectId() {
    return subjectId;
  }

  
  public void setSubjectId(String subjectId) {
    this.subjectId = subjectId;
  }

  
  public String getAction() {
    return action;
  }

  
  public void setAction(String action) {
    this.action = action;
  }

  
  public String getState() {
    return state;
  }

  
  public void setState(String state) {
    this.state = state;
  }

  
  public Long getMillisSince1970() {
    return millisSince1970;
  }

  
  public void setMillisSince1970(Long millisSince1970) {
    this.millisSince1970 = millisSince1970;
  }
  
}
