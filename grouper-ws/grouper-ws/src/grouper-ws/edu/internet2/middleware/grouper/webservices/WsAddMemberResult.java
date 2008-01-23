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
		return this.subjectId;
	}

	/**
	 * subject that was added
	 * @param subjectId1 the subjectId to set
	 */
	public void setSubjectId(String subjectId1) {
		this.subjectId = subjectId1;
	}

	/**
	 * subject identifier (if this is what was passed in) that was added
	 * @return the subjectIdentifier
	 */
	public String getSubjectIdentifier() {
		return this.subjectIdentifier;
	}

	/**
	 * subject identifier (if this is what was passed in) that was added
	 * @param subjectIdentifier1 the subjectIdentifier to set
	 */
	public void setSubjectIdentifier(String subjectIdentifier1) {
		this.subjectIdentifier = subjectIdentifier1;
	}

	/**
	 * T or F as to whether it was a successful assignment
	 * @return the success
	 */
	public String getSuccess() {
		return this.success;
	}

	/**
	 * T or F as to whether it was a successful assignment
	 * @param success1 the success to set
	 */
	public void setSuccess(String success1) {
		this.success = success1;
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
		return this.resultCode;
	}

	/**
	 * <pre>
	 * code of the result for this subject
	 * SUCCESS: means everything ok
	 * SUBJECT_NOT_FOUND: cant find the subject
	 * SUBJECT_DUPLICATE: found multiple subjects
	 *  
	 * </pre>
	 * @param resultCode1 the resultCode to set
	 */
	public void setResultCode(String resultCode1) {
		this.resultCode = resultCode1;
	}

	/**
	 * friendly message that could be audited or sent to a UI
	 * @return the errorMessage
	 */
	public String getResultMessage() {
		return this.resultMessage;
	}

	/**
	 * friendly message that could be audited or sent to a UI
	 * @param errorMessage the errorMessage to set
	 */
	public void setResultMessage(String errorMessage) {
		this.resultMessage = errorMessage;
	}
}
