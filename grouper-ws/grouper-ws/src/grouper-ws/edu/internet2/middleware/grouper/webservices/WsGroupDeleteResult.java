/**
 * 
 */
package edu.internet2.middleware.grouper.webservices;



/**
 * Result of one group being deleted.  The number of
 * these result objects will equal the number of groups sent in to the method
 * to be deleted
 * 
 * @author mchyzer
 */
public class WsGroupDeleteResult {
	
	/** group that was deleted */
	private String groupName;
	
	/** group uuid that was deleted */
	private String groupUuid;

	/** T or F as to whether it was a successful deleted */
	private String success;

	/** 
	 * <pre>
	 * code of the result for this subject
	 * SUCCESS: means everything ok
	 * GROUP_NOT_FOUND: cant find the subject
	 * SUBJECT_DUPLICATE: found multiple subjects
	 * etc
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
	public enum WsGroupDeleteResultCode {
		
		/** successful addition */
		SUCCESS, 
		
		/** invalid query, can only happen if simple query */
		INVALID_QUERY,

		/** the subject was found */
		GROUP_NOT_FOUND, 
		
		/** problem with deleting */
		EXCEPTION, 
		
		/** user not allowed */
		INSUFFICIENT_PRIVILEGES;
		
		/**
		 * if this is a successful result
		 * @return true if success
		 */
		public boolean isSuccess() {
			return this == SUCCESS || this == GROUP_NOT_FOUND;
		}
	}

	/**
	 * assign the code from the enum
	 * @param groupDeleteResultCode
	 */
	public void assignResultCode(WsGroupDeleteResultCode groupDeleteResultCode) {
		this.setResultCode(groupDeleteResultCode == null ? null : groupDeleteResultCode.name());
		this.setSuccess(groupDeleteResultCode.isSuccess() ? "T" : "F");
	}

	/**
	 * <pre>
	 * code of the result for this group
	 * SUCCESS: means everything ok
	 * GROUP_NOT_FOUND: cant find the subject
	 * etc
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

	/**
	 * group that was deleted
	 * @return the groupName
	 */
	public String getGroupName() {
		return this.groupName;
	}

	/**
	 * group that was deleted
	 * @param groupName1 the groupName to set
	 */
	public void setGroupName(String groupName1) {
		this.groupName = groupName1;
	}

	/**
	 * group uuid that was deleted
	 * @return the groupUuid
	 */
	public String getGroupUuid() {
		return this.groupUuid;
	}

	/**
	 * group uuid that was deleted
	 * @param groupUuid1 the groupUuid to set
	 */
	public void setGroupUuid(String groupUuid1) {
		this.groupUuid = groupUuid1;
	}
}
