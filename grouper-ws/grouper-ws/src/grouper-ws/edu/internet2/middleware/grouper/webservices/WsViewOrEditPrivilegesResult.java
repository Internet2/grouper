/**
 * 
 */
package edu.internet2.middleware.grouper.webservices;



/**
 * Result of one subject having priveleges updated.  The number of
 * subjects will equal the number of subjects sent in to the method
 * 
 * @author mchyzer
 */
public class WsViewOrEditPrivilegesResult {
	
	/** subject that was added */
	private String subjectId;
	
	/** subject identifier (if this is what was passed in) that was added */
	private String subjectIdentifier;

	/** T or F as to whether it was a successful assignment */
	private String success;
	
	/** T or F as to whether admin privilege is allowed */
	private String adminAllowed;

	/** T or F as to whether optin privilege is allowed */
	private String optinAllowed;
	
	/** T or F as to whether optout privilege is allowed */
	private String optoutAllowed;
	
	/** T or F as to whether read privilege is allowed */
	private String readAllowed;
	
	/** T or F as to whether system privilege is allowed */
	private String systemAllowed;

	/** T or F as to whether update privilege is allowed */
	private String updateAllowed;
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
	 * friendly message that could be audited 
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
	 * result code of a request
	 */
	public enum WsViewOrEditPrivilegesResultCode {
		
		/** invalid request */
		INVALID_QUERY,
		
		/** successful addition */
		SUCCESS, 
				
		/** the subject was not found */
		SUBJECT_NOT_FOUND, 
		
		/** problem with addigion */
		EXCEPTION, 
		
		/** user not allowed */
		INSUFFICIENT_PRIVILEGES, 
		
		/** subject duplicate found */
		SUBJECT_DUPLICATE;				
				
		/**
		 * if this is a successful result
		 * @return true if success
		 */
		public boolean isSuccess() {
			return this == SUCCESS;
		}
	}

	/**
	 * assign the code from the enum
	 * @param addMemberResultCode
	 */
	public void assignResultCode(WsViewOrEditPrivilegesResultCode addMemberResultCode) {
		this.setResultCode(addMemberResultCode == null ? null : addMemberResultCode.name());
		this.setSuccess(addMemberResultCode.isSuccess() ? "T" : "F");
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
	 * friendly message that could be audited 
	 * @return the errorMessage
	 */
	public String getResultMessage() {
		return this.resultMessage;
	}

	/**
	 * friendly message that could be audited 
	 * @param errorMessage the errorMessage to set
	 */
	public void setResultMessage(String errorMessage) {
		this.resultMessage = errorMessage;
	}

	/**
	 * T or F as to whether admin privilege is allowed
	 * @return the adminAllowed
	 */
	public String getAdminAllowed() {
		return this.adminAllowed;
	}

	/**
	 * T or F as to whether admin privilege is allowed
	 * @param adminAllowed1 the adminAllowed to set
	 */
	public void setAdminAllowed(String adminAllowed1) {
		this.adminAllowed = adminAllowed1;
	}

	/**
	 * T or F as to whether optin privilege is allowed
	 * @return the optinAllowed
	 */
	public String getOptinAllowed() {
		return this.optinAllowed;
	}

	/**
	 * T or F as to whether optin privilege is allowed
	 * @param optinAllowed1 the optinAllowed to set
	 */
	public void setOptinAllowed(String optinAllowed1) {
		this.optinAllowed = optinAllowed1;
	}

	/**
	 * T or F as to whether optout privilege is allowed
	 * @return the optoutAllowed
	 */
	public String getOptoutAllowed() {
		return this.optoutAllowed;
	}

	/**
	 * T or F as to whether optout privilege is allowed
	 * @param optoutAllowed1 the optoutAllowed to set
	 */
	public void setOptoutAllowed(String optoutAllowed1) {
		this.optoutAllowed = optoutAllowed1;
	}

	/**
	 * T or F as to whether read privilege is allowed
	 * @return the readAllowed
	 */
	public String getReadAllowed() {
		return this.readAllowed;
	}

	/**
	 * T or F as to whether read privilege is allowed
	 * @param readAllowed1 the readAllowed to set
	 */
	public void setReadAllowed(String readAllowed1) {
		this.readAllowed = readAllowed1;
	}

	/**
	 * T or F as to whether system privilege is allowed
	 * @return the systemAllowed
	 */
	public String getSystemAllowed() {
		return this.systemAllowed;
	}

	/**
	 * T or F as to whether system privilege is allowed
	 * @param systemAllowed1 the systemAllowed to set
	 */
	public void setSystemAllowed(String systemAllowed1) {
		this.systemAllowed = systemAllowed1;
	}

	/**
	 * T or F as to whether update privilege is allowed
	 * @return the updateAllowed
	 */
	public String getUpdateAllowed() {
		return this.updateAllowed;
	}

	/**
	 * T or F as to whether update privilege is allowed
	 * @param updateAllowed1 the updateAllowed to set
	 */
	public void setUpdateAllowed(String updateAllowed1) {
		this.updateAllowed = updateAllowed1;
	}
}
