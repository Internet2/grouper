/**
 * 
 */
package edu.internet2.middleware.grouper.webservices;



/**
 * Result of seeing if one subject is a member of a group.  The number of
 * subjects will equal the number of subjects sent in to the method
 * 
 * @author mchyzer
 */
public class WsHasMemberResult {
	
	/**
	 * result code of a request
	 */
	public enum WsHasMemberResultCode {
		
		/** the subject is a member */
		IS_MEMBER, 
		
		/** the subject was found and is a member */
		IS_NOT_MEMBER, 
		
		/** problem with query */
		EXCEPTION, 
		
		/** invalid query (e.g. if everything blank) */
		INVALID_QUERY,
				
		/** cant find the member.  note this is not an error, its a false */
		MEMBER_NOT_FOUND;
				
		/**
		 * if this is a successful result
		 * @return true if success
		 */
		public boolean isSuccess() {
			return this == IS_MEMBER || this == IS_NOT_MEMBER || this == MEMBER_NOT_FOUND;
		}
	}
	
	/**
	 * assign the code from the enum
	 * @param hasMemberResultCode
	 */
	public void assignResultCode(WsHasMemberResultCode hasMemberResultCode) {
		this.setResultCode(hasMemberResultCode == null ? null : hasMemberResultCode.name());
		this.setSuccess(hasMemberResultCode.isSuccess() ? "T" : "F");
	}
	
	/** subject that was queried */
	private String subjectId;
	
	/** subject identifier (if this is what was passed in) that was added */
	private String subjectIdentifier;

	/** T or F as to whether it was a successful query */
	private String success;

	/** 
	 * <pre>
	 * code of the result for this subject
	 * IS_MEMBER: means subject is a member
	 * SUBJECT_NOT_FOUND: cant find the subject
	 * IS_NOT_MEMBER: subject was found and not a member
	 * One of WsHasMemberResult
	 * </pre>
	 */
	private String resultCode;

	/**
	 * friendly message that could be audited or sent to a UI
	 */
	private String resultMessage;
	
	/**
	 * subject that was queried
	 * @return the subjectId
	 */
	public String getSubjectId() {
		return this.subjectId;
	}

	/**
	 * subject that was queried
	 * @param subjectId1 the subjectId to set
	 */
	public void setSubjectId(String subjectId1) {
		this.subjectId = subjectId1;
	}

	/**
	 * subject identifier (if this is what was passed in) that was queried
	 * @return the subjectIdentifier
	 */
	public String getSubjectIdentifier() {
		return this.subjectIdentifier;
	}

	/**
	 * subject identifier (if this is what was passed in) that was queried
	 * @param subjectIdentifier1 the subjectIdentifier to set
	 */
	public void setSubjectIdentifier(String subjectIdentifier1) {
		this.subjectIdentifier = subjectIdentifier1;
	}

	/**
	 * T or F as to whether it was a successful query
	 * @return the success
	 */
	public String getSuccess() {
		return this.success;
	}

	/**
	 * T or F as to whether it was a successful query
	 * @param success1 the success to set
	 */
	public void setSuccess(String success1) {
		this.success = success1;
	}

	/**
	 * <pre>
	 * code of the result for this subject
	 * IS_MEMBER: means subject is a member
	 * SUBJECT_NOT_FOUND: cant find the subject
	 * IS_NOT_MEMBER: subject was found and not a member
	 * One of WsHasMemberResult
	 * </pre>
	 * @return the resultCode
	 */
	public String getResultCode() {
		return this.resultCode;
	}

	/**
	 * <pre>
	 * code of the result for this subject
	 * IS_MEMBER: means subject is a member
	 * SUBJECT_NOT_FOUND: cant find the subject
	 * IS_NOT_MEMBER: subject was found and not a member
	 * One of WsHasMemberResult
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
