/**
 * 
 */
package edu.internet2.middleware.grouper.webservices;



/**
 * Result of one group being saved.  The number of
 * these result objects will equal the number of groups sent in to the method
 * to be saved
 * 
 * @author mchyzer
 */
public class WsGroupSaveResult {
	
	/** group that was saved */
	private String groupName;
	
	/** group uuid that was saved */
	private String groupUuid;

	/** T or F as to whether it was a successful saved */
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
	 * friendly message that could be audited 
	 */
	private String resultMessage;
	
	/**
	 * T or F as to whether it was a success
	 * @return the success
	 */
	public String getSuccess() {
		return this.success;
	}

	/**
	 * T or F as to whether it was a success
	 * @param success1 the success to set
	 */
	public void setSuccess(String success1) {
		this.success = success1;
	}

	/**
	 * <pre>
	 * code of the result 
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
	public enum WsGroupSaveResultCode {
		
		/** successful addition */
		SUCCESS, 
		
		/** invalid query, can only happen if simple query */
		INVALID_QUERY,

		/** the group was not found */
		GROUP_NOT_FOUND, 
		
		/** the stem was not found */
		STEM_NOT_FOUND, 
		
		/** problem with saving */
		EXCEPTION, 
		
		/** user not allowed */
		INSUFFICIENT_PRIVILEGES;
		
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
	 * @param groupSaveResultCode
	 */
	public void assignResultCode(WsGroupSaveResultCode groupSaveResultCode) {
		this.setResultCode(groupSaveResultCode == null ? null : groupSaveResultCode.name());
		this.setSuccess(groupSaveResultCode.isSuccess() ? "T" : "F");
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
	 * group that was saved
	 * @return the groupName
	 */
	public String getGroupName() {
		return this.groupName;
	}

	/**
	 * group that was saved
	 * @param groupName1 the groupName to set
	 */
	public void setGroupName(String groupName1) {
		this.groupName = groupName1;
	}

	/**
	 * group uuid that was saved
	 * @return the groupUuid
	 */
	public String getGroupUuid() {
		return this.groupUuid;
	}

	/**
	 * group uuid that was saved
	 * @param groupUuid1 the groupUuid to set
	 */
	public void setGroupUuid(String groupUuid1) {
		this.groupUuid = groupUuid1;
	}
}
