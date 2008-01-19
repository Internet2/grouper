/**
 * 
 */
package edu.internet2.middleware.grouper.webservices;

/**
 * Generic result fields
 * 
 * @author mchyzer
 *
 */
public abstract class WsResult {

	/** T or F as to whether it was a successful assignment */
	private String success;
	/** 
	 * <pre>
	 * code of the result for this subject
	 * SUCCESS: means everything ok
	 * SUBJECT_NOT_FOUND: cant find the subject
	 * SUBJECT_DUPLICATE: found multiple subjects
	 *  
	 * </pre>
	 */
	private String resultCode;
	/**
	 * friendly message that could be audited or sent to a UI
	 */
	private String resultMessage;

	/**
	 * T or F as to whether it was a successful assignment
	 * @return the success
	 */
	public String getSuccess() {
		return success;
	}

	/**
	 * T or F as to whether it was a successful assignment
	 * @param success the success to set
	 */
	public void setSuccess(String success) {
		this.success = success;
	}

	/**
	 * <pre>
	 * code of the result for this subject
	 * SUCCESS: means everything ok
	 * SUBJECT_NOT_FOUND: cant find the subject
	 * SUBJECT_DUPLICATE: found multiple subjects
	 *  
	 * </pre>
	 * @return the resultCode
	 */
	public String getResultCode() {
		return resultCode;
	}

	/**
	 * <pre>
	 * code of the result for this subject
	 * SUCCESS: means everything ok
	 * SUBJECT_NOT_FOUND: cant find the subject
	 * SUBJECT_DUPLICATE: found multiple subjects
	 *  
	 * </pre>
	 * @param resultCode the resultCode to set
	 */
	public void setResultCode(String resultCode) {
		this.resultCode = resultCode;
	}

	/**
	 * friendly message that could be audited or sent to a UI
	 * @return the errorMessage
	 */
	public String getResultMessage() {
		return resultMessage;
	}

	/**
	 * friendly message that could be audited or sent to a UI
	 * @param errorMessage the errorMessage to set
	 */
	public void setResultMessage(String errorMessage) {
		this.resultMessage = errorMessage;
	}

}
