package edu.internet2.middleware.grouper.webservices;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.webservices.WsHasMemberResult.WsHasMemberResultCode;

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
public class WsHasMemberResults {

	/**
	 * result code of a request
	 */
	public enum WsHasMemberResultsCode {
		
		/** discovered if each was a member of not */
		SUCCESS {
			
			/** convert this code to a result code */
			@Override
			public WsHasMemberResultCode convertToResultCode() {
				//not sure what to do here, its not a success
				return WsHasMemberResultCode.EXCEPTION;
			}
		}, 
		
		/** had an exception while figuring out if the subjects were members */
		EXCEPTION {
			
			/** convert this code to a result code */
			@Override
			public WsHasMemberResultCode convertToResultCode() {
				return WsHasMemberResultCode.EXCEPTION;
			}
		}, 
		
		/** had a problem with one or more of the queries */
		PROBLEM_WITH_QUERY {
			
			/** convert this code to a result code */
			@Override
			public WsHasMemberResultCode convertToResultCode() {
				return WsHasMemberResultCode.INVALID_QUERY;
			}
		}, 
		
		/** invalid query (e.g. if everything blank) */
		INVALID_QUERY {
			
			/** convert this code to a result code */
			@Override
			public WsHasMemberResultCode convertToResultCode() {
				return WsHasMemberResultCode.INVALID_QUERY;
			}
			
		};
				
		/**
		 * if this is a successful result
		 * @return true if success
		 */
		public boolean isSuccess() {
			return this == SUCCESS;
		}
		
		/** convert this code to a result code 
		 * @return the result code
		 */
		public abstract WsHasMemberResultCode convertToResultCode();
	}
	
	/**
	 * assign the code from the enum
	 * @param hasMemberResultsCode
	 */
	public void assignResultCode(WsHasMemberResultsCode hasMemberResultsCode) {
		this.setResultCode(hasMemberResultsCode == null ? null : hasMemberResultsCode.name());
		this.setSuccess(hasMemberResultsCode.isSuccess() ? "T" : "F");
	}
	
	/**
	 * convert the result code back to enum
	 * @return the enum code
	 */
	public WsHasMemberResultsCode retrieveResultCode() {
		if (StringUtils.isBlank(this.resultCode)) {
			return null;
		}
		return WsHasMemberResultsCode.valueOf(this.resultCode);
	}
	
	/**
	 * error message if there is an error
	 */
	private StringBuilder resultMessage = new StringBuilder();
	
	/**
	 * results for each assignment sent in
	 */
	private WsHasMemberResult[] results;

	/** T or F as to whether it was a successful query */
	private String success;

	/** 
	 * <pre>
	 * code of the result for this subject
	 * SUCCESS: means everything ok
	 * INVALID_QUERY
	 * one of WsHasMemberResultsCode
	 *  
	 * </pre>
	 */
	private String resultCode;

	/**
	 * results for each assignment sent in
	 * @return the results
	 */
	public WsHasMemberResult[] getResults() {
		return this.results;
	}

	/**
	 * results for each assignment sent in
	 * @param results1 the results to set
	 */
	public void setResults(WsHasMemberResult[] results1) {
		this.results = results1;
	}

	/**
	 * error message if there is an error
	 * @return the errorMessage
	 */
	public String getResultMessage() {
		return this.resultMessage.toString();
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
	public void setResultMessage(String errorMessage) {
		this.resultMessage = new StringBuilder(errorMessage);
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
	 * INVALID_QUERY
	 * one of WsHasMemberResultsCode
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
	 * INVALID_QUERY
	 * one of WsHasMemberResultsCode
	 *  
	 * </pre>
	 * @param resultCode1 the resultCode to set
	 */
	public void setResultCode(String resultCode1) {
		this.resultCode = resultCode1;
	}

}
