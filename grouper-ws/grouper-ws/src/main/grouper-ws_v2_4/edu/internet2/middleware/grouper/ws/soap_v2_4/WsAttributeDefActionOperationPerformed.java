package edu.internet2.middleware.grouper.ws.soap_v2_4;


/**
 * item in the assign attribute def action result
 * @author vsachdeva
 */
public class WsAttributeDefActionOperationPerformed {

  /** action name assigned/removed **/
  private String action;

  /** status of the action eg: Added, Deleted, Not Found, Already assigned  **/
  private String status;

  /**
   * @return action name
   */
  public String getAction() {
    return this.action;
  }

  /**
   * @param action1
   */
  public void setAction(String action1) {
    this.action = action1;
  }

  /**
   * @return status of the action eg: Added, Deleted, Not Found, Already assigned
   * WsAssignAttributeDefActionsStatus
   */
  public String getStatus() {
    return this.status;
  }

  /**
   * status of the action eg: Added, Deleted, Not Found, Already assigned
   * WsAssignAttributeDefActionsStatus
   * @param status1
   */
  public void setStatus(String status1) {
    this.status = status1;
  }

}
