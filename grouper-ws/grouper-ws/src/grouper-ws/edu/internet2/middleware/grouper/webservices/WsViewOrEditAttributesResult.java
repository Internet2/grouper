/**
 * 
 */
package edu.internet2.middleware.grouper.webservices;

/**
 * Result of one subject having priveleges updated. The number of subjects will
 * equal the number of subjects sent in to the method
 * 
 * @author mchyzer
 */
public class WsViewOrEditAttributesResult {

	/** groupUuid for this group */
	private String groupUuid;
	
	/** group name for this group */
	private String groupName;
	
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
	 * friendly message that could be audited
	 */
	private String resultMessage;

	/** array of attributes */
	private WsAttribute[] attributes;

	/**
	 * T or F as to whether it was a successful assignment
	 * 
	 * @return the success
	 */
	public String getSuccess() {
		return this.success;
	}

	/**
	 * T or F as to whether it was a successful assignment
	 * 
	 * @param success1
	 *            the success to set
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
	 * 
	 * @return the resultCode
	 */
	public String getResultCode() {
		return this.resultCode;
	}

	/**
	 * result code of a request
	 */
	public enum WsViewOrEditAttributesResultCode {

		/** invalid request */
		INVALID_QUERY,

		/** successful addition */
		SUCCESS,

		/** cant find attribute */
		ATTRIBUTE_NOT_FOUND,

		/** problem with addigion */
		EXCEPTION,

		/** problem with addigion */
		GROUP_NOT_FOUND,

		/** user not allowed */
		INSUFFICIENT_PRIVILEGES;

		/**
		 * if this is a successful result
		 * 
		 * @return true if success
		 */
		public boolean isSuccess() {
			return this == SUCCESS;
		}
	}

	/**
	 * assign the code from the enum
	 * 
	 * @param viewOrEditAttributesResultCode code to assign
	 */
	public void assignResultCode(
			WsViewOrEditAttributesResultCode viewOrEditAttributesResultCode) {
		this.setResultCode(viewOrEditAttributesResultCode == null ? null
				: viewOrEditAttributesResultCode.name());
		this.setSuccess(viewOrEditAttributesResultCode.isSuccess() ? "T" : "F");
	}

	/**
	 * <pre>
	 * code of the result for this subject
	 * SUCCESS: means everything ok
	 * SUBJECT_NOT_FOUND: cant find the subject
	 * SUBJECT_DUPLICATE: found multiple subjects
	 *  
	 * </pre>
	 * 
	 * @param resultCode1
	 *            the resultCode to set
	 */
	public void setResultCode(String resultCode1) {
		this.resultCode = resultCode1;
	}

	/**
	 * friendly message that could be audited
	 * 
	 * @return the errorMessage
	 */
	public String getResultMessage() {
		return this.resultMessage;
	}

	/**
	 * friendly message that could be audited
	 * 
	 * @param errorMessage
	 *            the errorMessage to set
	 */
	public void setResultMessage(String errorMessage) {
		this.resultMessage = errorMessage;
	}

	/**
	 * groupUuid for this group
	 * @return the groupUuid
	 */
	public String getGroupUuid() {
		return this.groupUuid;
	}

	/**
	 * groupUuid for this group
	 * @param groupUuid1 the groupUuid to set
	 */
	public void setGroupUuid(String groupUuid1) {
		this.groupUuid = groupUuid1;
	}

	/**
	 * group name for this group
	 * @return the groupName
	 */
	public String getGroupName() {
		return this.groupName;
	}

	/**
	 * groupUuid for this group
	 * @param groupName1 the groupName to set
	 */
	public void setGroupName(String groupName1) {
		this.groupName = groupName1;
	}

	/**
	 * array of attributes
	 * @return the attributes
	 */
	public WsAttribute[] getAttributes() {
		return this.attributes;
	}

	/**
	 * @param attributes1 the attributes to set
	 */
	public void setAttributes(WsAttribute[] attributes1) {
		this.attributes = attributes1;
	}
}
