package edu.internet2.middleware.grouper.webservices;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.webservices.WsViewOrEditPrivilegesResult.WsViewOrEditPrivilegesResultCode;

/**
 * <pre>
 * results for the viewOrEditPrivileges call.
 * 
 * result code:
 * code of the result for this group overall
 * SUCCESS: means everything ok
 * GROUP_NOT_FOUND: cant find the group
 * GROUP_DUPLICATE: found multiple groups
 * </pre>
 * @author mchyzer
 */
public class WsViewOrEditPrivilegesResults {

	/**
	 * result code of a request
	 */
	public enum WsViewOrEditPrivilegesResultsCode {
		
		/** found the subject, assigned or viewed */
		SUCCESS  {
			
			/** convert this code to a result code */
			@Override
			public WsViewOrEditPrivilegesResultCode convertToResultCode() {
				//note, it isnt a success if converting to resultcode
				return WsViewOrEditPrivilegesResultCode.EXCEPTION;
			}
			
		}, 
		
		/** found the subject */
		EXCEPTION  {
			
			/** convert this code to a result code */
			@Override
			public WsViewOrEditPrivilegesResultCode convertToResultCode() {
				return WsViewOrEditPrivilegesResultCode.EXCEPTION;
			}
			
		}, 
		
		/** problem deleting existing members */
		PROBLEM_WITH_MEMBERS {
			
			/** convert this code to a result code */
			@Override
			public WsViewOrEditPrivilegesResultCode convertToResultCode() {
				return WsViewOrEditPrivilegesResultCode.EXCEPTION;
			}
			
		}, 
		
		/** invalid query (e.g. if everything blank) */
		INVALID_QUERY {
			
			/** convert this code to a result code */
			@Override
			public WsViewOrEditPrivilegesResultCode convertToResultCode() {
				return WsViewOrEditPrivilegesResultCode.INVALID_QUERY;
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
		public abstract WsViewOrEditPrivilegesResultCode convertToResultCode();

	}
	
	/**
	 * assign the code from the enum
	 * @param viewOrEditPrivilegesResultCode
	 */
	public void assignResultCode(WsViewOrEditPrivilegesResultsCode viewOrEditPrivilegesResultCode) {
		this.setResultCode(viewOrEditPrivilegesResultCode == null ? null : viewOrEditPrivilegesResultCode.name());
		this.setSuccess(viewOrEditPrivilegesResultCode.isSuccess() ? "T" : "F");
	}
	
	/**
	 * convert the result code back to enum
	 * @return the enum code
	 */
	public WsViewOrEditPrivilegesResultsCode retrieveResultCode() {
		if (StringUtils.isBlank(this.resultCode)) {
			return null;
		}
		return WsViewOrEditPrivilegesResultsCode.valueOf(this.resultCode);
	}

	/**
	 * error message if there is an error
	 */
	private StringBuilder resultMessage = new StringBuilder();
	
	/**
	 * results for each assignment sent in
	 */
	private WsViewOrEditPrivilegesResult[] results;

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
	public WsViewOrEditPrivilegesResult[] getResults() {
		return this.results;
	}

	/**
	 * results for each assignment sent in
	 * @param results1 the results to set
	 */
	public void setResults(WsViewOrEditPrivilegesResult[] results1) {
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
