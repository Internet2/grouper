package edu.internet2.middleware.grouper.webservices;

/**
 * <pre>
 * results for the add member call.
 * 
 * result code:
 * code of the result for this group overall
 * SUCCESS: means everything ok
 * GROUP_NOT_FOUND: cant find the group
 * GROUP_DUPLICATE: found multiple groups
 * </pre>
 * @author mchyzer
 */
public class WsAddMemberResults extends WsResult {

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
		this.setSuccess(addMemberResultCode.isSuccess() ? "T" : "F");
	}
	
	/**
	 * error message if there is an error
	 */
	private StringBuilder resultMessage = new StringBuilder();
	
	/**
	 * results for each assignment sent in
	 */
	private WsAddMemberResult[] results;
	
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
	@Override
	public String getResultMessage() {
		return resultMessage.toString();
	}
	
	/**
	 * append error message to list of error messages
	 * @param errorMessage
	 */
	public void appendResultMessage(String errorMessage) {
		this.resultMessage.append(errorMessage);
	}
	
	/**
	 * error message if there is an error
	 * @param errorMessage the errorMessage to set
	 */
	@Override
	public void setResultMessage(String errorMessage) {
		this.resultMessage = new StringBuilder(errorMessage);
	}

}
