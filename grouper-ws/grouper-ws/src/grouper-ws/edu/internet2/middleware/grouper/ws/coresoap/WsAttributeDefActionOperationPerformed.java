package edu.internet2.middleware.grouper.ws.coresoap;

/**
 * item in the assign attribute def action result
 * @author vsachdeva
 */
public class WsAttributeDefActionOperationPerformed {
	
	/** action name assigned/removed **/
	private String action;
	
	/** message eg: ADDED, DELETED, NOT_THERE, ALREADY_THERE **/
    private String message;
	
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
	 * @return message eg: ADDED, DELETED, NOT_THERE, ALREADY_THERE
	 */
	public String getMessage() {
		return this.message;
	}

	/**
	 * @param message1 eg: ADDED, DELETED, NOT_THERE, ALREADY_THERE
	 */
	public void setMessage(String message1) {
		this.message = message1;
	}

}
