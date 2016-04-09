package edu.internet2.middleware.grouper.ws.soap_v2_3;

import edu.internet2.middleware.grouper.ws.rest.attribute.WsAssignAttributeDefActionsStatus;

/**
 * item in the assign attribute def action result
 * @author vsachdeva
 */
public class WsAttributeDefActionOperationPerformed {

  /** action name assigned/removed **/
  private String action;

  /** status of the action eg: Added, Deleted, Not Found, Already assigned  **/
  private WsAssignAttributeDefActionsStatus status;

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
   */
  public WsAssignAttributeDefActionsStatus getStatus() {
    return this.status;
  }

  /**
   * status of the action eg: Added, Deleted, Not Found, Already assigned
   * @param status1
   */
  public void setStatus(WsAssignAttributeDefActionsStatus status1) {
    this.status = status1;
  }

}
