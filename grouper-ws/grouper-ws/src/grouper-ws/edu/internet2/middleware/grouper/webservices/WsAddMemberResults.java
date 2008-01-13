package edu.internet2.middleware.grouper.webservices;

/**
 * results for the add member call
 * @author mchyzer
 */
public class WsAddMemberResults {

	/** if success overall or not: T/F */
	private String success;
	
	/**
	 * result code of a request
	 */
	public enum WsAddMemberResultCode {
		
		/** found the subject */
		SUCCESS, 
		
		/** found the subject */
		EXCEPTION, 
		
		/** problem deleting existing members */
		PROBLEM_DELETING_MEMBERS, 
		
		/** invalid query (e.g. if everything blank) */
		INVALID_QUERY,
				
		/** something in one assignment wasnt successful */
		PROBLEM_WITH_ASSIGNMENT;
				
		/**
		 * if this is a successful result
		 * @return true if success
		 */
		public boolean isSuccess() {
			return this == SUCCESS;
		}
	};
	
	/**
	 * assign the code from the enum
	 * @param addMemberResultCode
	 */
	public void assignResultCode(WsAddMemberResultCode addMemberResultCode) {
		this.setResultCode(addMemberResultCode == null ? null : addMemberResultCode.name());
	}
	
	/** 
	 * <pre>
	 * code of the result for this group overall
	 * SUCCESS: means everything ok
	 * GROUP_NOT_FOUND: cant find the group
	 * GROUP_DUPLICATE: found multiple groups
	 *  
	 * </pre>
	 */
	private String resultCode;

	/**
	 * error message if there is an error
	 */
	private StringBuilder errorMessage = new StringBuilder();
	
	/**
	 * results for each assignment sent in
	 */
	private WsAddMemberResult[] results;
	
	/**
	 * if success overall or not: T/F
	 * @return the success
	 */
	public String getSuccess() {
		return success;
	}
	
	

	/**
	 * if success overall or not: T/F
	 * @param success the success to set
	 */
	public void setSuccess(String success) {
		this.success = success;
	}

	/**
	 * <pre>
	 * code of the result for this group overall
	 * SUCCESS: means everything ok
	 * GROUP_NOT_FOUND: cant find the group
	 * GROUP_DUPLICATE: found multiple groups
	 *  
	 * </pre>
	 * @return the resultCode
	 */
	public String getResultCode() {
		return resultCode;
	}

	/**
	 * <pre>
	 * code of the result for this group overall
	 * SUCCESS: means everything ok
	 * GROUP_NOT_FOUND: cant find the group
	 * GROUP_DUPLICATE: found multiple groups
	 *  
	 * </pre>
	 * @param resultCode the resultCode to set
	 */
	public void setResultCode(String resultCode) {
		this.resultCode = resultCode;
	}

	/**
	 * results for each assignment sent in
	 * @return the results
	 */
	public WsAddMemberResult[] getResults() {
		return results;
	}

	/**
	 * results for each assignment sent in
	 * @param results the results to set
	 */
	public void setResults(WsAddMemberResult[] results) {
		this.results = results;
	}

	/**
	 * error message if there is an error
	 * @return the errorMessage
	 */
	public String getErrorMessage() {
		return errorMessage.toString();
	}
	
	/**
	 * append error message to list of error messages
	 * @param errorMessage
	 */
	public void appendErrorMessage(String errorMessage) {
		this.errorMessage.append(errorMessage);
	}
	
	/**
	 * error message if there is an error
	 * @param errorMessage the errorMessage to set
	 */
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = new StringBuilder(errorMessage);
	}

}
