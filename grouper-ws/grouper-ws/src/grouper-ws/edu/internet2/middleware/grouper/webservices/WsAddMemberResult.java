/**
 * 
 */
package edu.internet2.middleware.grouper.webservices;


/**
 * Result of one subject being added to a group.  The number of
 * subjects will equal the number of subjects sent in to the method
 * 
 * @author mchyzer
 */
public class WsAddMemberResult {
	
	/** subject that was added */
	private String subjectId;
	
	/** subject identifier (if this is what was passed in) that was added */
	private String subjectIdentifier;
	
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
	 * subject that was added
	 * @return the subjectId
	 */
	public String getSubjectId() {
		return subjectId;
	}

	/**
	 * subject that was added
	 * @param subjectId the subjectId to set
	 */
	public void setSubjectId(String subjectId) {
		this.subjectId = subjectId;
	}

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
	
	/**
	 * subject identifier (if this is what was passed in) that was added
	 * @return the subjectIdentifier
	 */
	public String getSubjectIdentifier() {
		return subjectIdentifier;
	}

	/**
	 * subject identifier (if this is what was passed in) that was added
	 * @param subjectIdentifier the subjectIdentifier to set
	 */
	public void setSubjectIdentifier(String subjectIdentifier) {
		this.subjectIdentifier = subjectIdentifier;
	}
}
