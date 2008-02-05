package edu.internet2.middleware.grouper.webservices;

import org.apache.commons.lang.StringUtils;

/**
 * <pre>
 * results for the add member call.
 * 
 * result code:
 * code of the result for this group overall
 * SUCCESS: means everything ok
 * SUBJECT_NOT_FOUND: cant find the subject
 * SUBJECT_DUPLICATE: found multiple groups
 * EXCEPTION
 * </pre>
 * @author mchyzer
 */
public class WsGetGroupsResults {

	/**
	 * result code of a request
	 */
	public enum WsGetGroupsResultsCode {
		
		/** found the subject */
		SUCCESS, 
		
		/** problem */
		EXCEPTION, 
		
		/** problem */
		INVALID_QUERY, 
		
		/** couldnt find the member to query */
		MEMBER_NOT_FOUND,
		
		/** couldnt find the subject to query */
		SUBJECT_NOT_FOUND,
		
		/** problem querying the subject, was duplicate */
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
	 * @param wsGetGroupsResultsCode
	 */
	public void assignResultCode(WsGetGroupsResultsCode wsGetGroupsResultsCode) {
		this.setResultCode(wsGetGroupsResultsCode == null ? null : wsGetGroupsResultsCode.name());
		this.setSuccess(wsGetGroupsResultsCode.isSuccess() ? "T" : "F");
	}
	
	/**
	 * convert the result code back to enum
	 * @return the enum code
	 */
	public WsGetGroupsResultsCode retrieveResultCode() {
		if (StringUtils.isBlank(this.resultCode)) {
			return null;
		}
		return WsGetGroupsResultsCode.valueOf(this.resultCode);
	}

	/**
	 * error message if there is an error
	 */
	private StringBuilder resultMessage = new StringBuilder();
	
	/**
	 * results for each assignment sent in
	 */
	private WsGetGroupsResult[] results;

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
	 * results for each assignment sent in
	 * @return the results
	 */
	public WsGetGroupsResult[] getResults() {
		return this.results;
	}

	/**
	 * results for each assignment sent in
	 * @param results1 the results to set
	 */
	public void setResults(WsGetGroupsResult[] results1) {
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

}
