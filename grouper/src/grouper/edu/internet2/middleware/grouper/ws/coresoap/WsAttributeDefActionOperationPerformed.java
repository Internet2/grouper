package edu.internet2.middleware.grouper.ws.coresoap;

import edu.internet2.middleware.grouper.ws.rest.attribute.WsAssignAttributeDefActionsStatus;

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
  
  /**
   * 
   * @param wsAssignAttributeDefActionsStatus
   */
  public void assignStatus(WsAssignAttributeDefActionsStatus wsAssignAttributeDefActionsStatus) {
    this.status = wsAssignAttributeDefActionsStatus == null ? null : wsAssignAttributeDefActionsStatus.name();
  }
}
