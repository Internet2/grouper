/**
 * 
 */
package edu.internet2.middleware.grouper.webservices;



/**
 * Result of one stem being deleted.  The number of
 * these result objects will equal the number of stems sent in to the method
 * to be deleted
 * 
 * @author mchyzer
 */
public class WsStemDeleteResult {
	
	/** stem that was deleted */
	private String stemName;
	
	/** stem uuid that was deleted */
	private String stemUuid;

	/** T or F as to whether it was a successful deleted */
	private String success;

	/** 
	 * <pre>
	 * code of the result for this subject
	 * SUCCESS: means everything ok
	 * STEM_NOT_FOUND: cant find the stem
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
	public enum WsStemDeleteResultCode {
		
		/** successful addition */
		SUCCESS, 
		
		/** invalid query, can only happen if simple query */
		INVALID_QUERY,

		/** the stem was not found */
		STEM_NOT_FOUND, 
		
		/** problem with deleting */
		EXCEPTION, 
		
		/** user not allowed */
		INSUFFICIENT_PRIVILEGES;
		
		/**
		 * if this is a successful result
		 * @return true if success
		 */
		public boolean isSuccess() {
			return this == SUCCESS || this == STEM_NOT_FOUND;
		}
	}

	/**
	 * assign the code from the enum
	 * @param stemDeleteResultCode
	 */
	public void assignResultCode(WsStemDeleteResultCode stemDeleteResultCode) {
		this.setResultCode(stemDeleteResultCode == null ? null : stemDeleteResultCode.name());
		this.setSuccess(stemDeleteResultCode.isSuccess() ? "T" : "F");
	}

	/**
	 * <pre>
	 * code of the result for this stem
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
	 * stem that was deleted
	 * @return the stemName
	 */
	public String getStemName() {
		return this.stemName;
	}

	/**
	 * stem that was deleted
	 * @param stemName1 the stemName to set
	 */
	public void setStemName(String stemName1) {
		this.stemName = stemName1;
	}

	/**
	 * stem uuid that was deleted
	 * @return the stemUuid
	 */
	public String getStemUuid() {
		return this.stemUuid;
	}

	/**
	 * stem uuid that was deleted
	 * @param stemUuid1 the stemUuid to set
	 */
	public void setStemUuid(String stemUuid1) {
		this.stemUuid = stemUuid1;
	}
}
