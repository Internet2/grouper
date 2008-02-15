/**
 * 
 */
package edu.internet2.middleware.grouper.webservices;



/**
 * Result of one save being saved.  The number of
 * these result objects will equal the number of saves sent in to the method
 * to be saved
 * 
 * @author mchyzer
 */
public class WsStemSaveResult {
	
	/** save that was saved */
	private String saveName;
	
	/** save uuid that was saved */
	private String saveUuid;

	/** T or F as to whether it was a successful saved */
	private String success;

	/** 
	 * <pre>
	 * code of the result for this subject
	 * SUCCESS: means everything ok
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
	public enum WsStemSaveResultCode {
		
		/** successful addition */
		SUCCESS, 
		
		/** invalid query, can only happen if simple query */
		INVALID_QUERY,

		/** the save was not found */
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
	 * @param saveSaveResultCode
	 */
	public void assignResultCode(WsStemSaveResultCode saveSaveResultCode) {
		this.setResultCode(saveSaveResultCode == null ? null : saveSaveResultCode.name());
		this.setSuccess(saveSaveResultCode.isSuccess() ? "T" : "F");
	}

	/**
	 * <pre>
	 * code of the result for this save
	 * SUCCESS: means everything ok
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
	 * save that was saved
	 * @return the saveName
	 */
	public String getStemName() {
		return this.saveName;
	}

	/**
	 * save that was saved
	 * @param saveName1 the saveName to set
	 */
	public void setStemName(String saveName1) {
		this.saveName = saveName1;
	}

	/**
	 * save uuid that was saved
	 * @return the saveUuid
	 */
	public String getStemUuid() {
		return this.saveUuid;
	}

	/**
	 * save uuid that was saved
	 * @param saveUuid1 the saveUuid to set
	 */
	public void setStemUuid(String saveUuid1) {
		this.saveUuid = saveUuid1;
	}
}
