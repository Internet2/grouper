package edu.internet2.middleware.grouperClient.ws.beans;

/**
 * item in the assign attribute def action result
 * @author vsachdeva
 */
public class WsAttributeDefActionOperationPerformed {
	
	/** action name assigned/removed **/
	private String action;
	
	/** status from WsAssignAttributeDefActionsStatus **/
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
   * status from WsAssignAttributeDefActionsStatus
   * @return status
   */
  public String getStatus() {
    return this.status;
  }

  /**
   * status from WsAssignAttributeDefActionsStatus
   * @param status1
   */
  public void setStatus(String status1) {
    this.status = status1;
  }

}
