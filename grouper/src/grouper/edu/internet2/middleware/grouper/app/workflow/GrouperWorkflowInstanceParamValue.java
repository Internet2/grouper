package edu.internet2.middleware.grouper.app.workflow;


public class GrouperWorkflowInstanceParamValue {
  
  /**
   * param name
   */
  private String paramName;
  
  /**
   * param value
   */
  private String paramValue;
  
  /**
   * millis when this param was generated
   */
  private Long lastUpdatedMillis;
  
  /**
   * member id that edited this param
   */
  private String editedByMemberId;
  
  /**
   * state in which this param was edited
   */
  private String editedInState;

  /**
   * param name
   * @return
   */
  public String getParamName() {
    return paramName;
  }

  /**
   * param name
   * @param paramName
   */
  public void setParamName(String paramName) {
    this.paramName = paramName;
  }

  /**
   * param value
   * @return
   */
  public String getParamValue() {
    return paramValue;
  }

  /**
   * param value
   * @param paramValue
   */
  public void setParamValue(String paramValue) {
    this.paramValue = paramValue;
  }

  /**
   * millis when this param was generated
   * @return
   */
  public Long getLastUpdatedMillis() {
    return lastUpdatedMillis;
  }

  /**
   * millis when this param was generated
   * @param lastUpdatedMillis
   */
  public void setLastUpdatedMillis(Long lastUpdatedMillis) {
    this.lastUpdatedMillis = lastUpdatedMillis;
  }

  /**
   * member id that edited this param
   * @return
   */
  public String getEditedByMemberId() {
    return editedByMemberId;
  }

  /**
   * member id that edited this param
   * @param editedByMemberId
   */
  public void setEditedByMemberId(String editedByMemberId) {
    this.editedByMemberId = editedByMemberId;
  }

  /**
   * state in which this param was edited
   * @return
   */
  public String getEditedInState() {
    return editedInState;
  }

  /**
   * state in which this param was edited
   * @param editedInState
   */
  public void setEditedInState(String editedInState) {
    this.editedInState = editedInState;
  }
  
}
