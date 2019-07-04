package edu.internet2.middleware.grouper.app.workflow;


public class GrouperWorkflowInstanceParamValue {
  
  private String paramName;
  
  private String paramValue;
  
  private Long lastUpdatedMillis;
  
  private String editedByMemberId;
  
  private String editedInState;

  
  public String getParamName() {
    return paramName;
  }

  
  public void setParamName(String paramName) {
    this.paramName = paramName;
  }

  
  public String getParamValue() {
    return paramValue;
  }

  
  public void setParamValue(String paramValue) {
    this.paramValue = paramValue;
  }

  
  public Long getLastUpdatedMillis() {
    return lastUpdatedMillis;
  }

  
  public void setLastUpdatedMillis(Long lastUpdatedMillis) {
    this.lastUpdatedMillis = lastUpdatedMillis;
  }

  
  public String getEditedByMemberId() {
    return editedByMemberId;
  }

  
  public void setEditedByMemberId(String editedByMemberId) {
    this.editedByMemberId = editedByMemberId;
  }

  
  public String getEditedInState() {
    return editedInState;
  }

  
  public void setEditedInState(String editedInState) {
    this.editedInState = editedInState;
  }
  
}
