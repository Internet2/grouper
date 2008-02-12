package edu.internet2.middleware.grouper.webservices;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.webservices.WsViewOrEditAttributesResult.WsViewOrEditAttributesResultCode;

/**
 * <pre>
 * results for the viewOrEditAttributes
 * 
 * result code:
 * code of the result for this group overall
 * SUCCESS: means everything ok
 * GROUP_NOT_FOUND: cant find the group
 * GROUP_DUPLICATE: found multiple groups
 * </pre>
 * @author mchyzer
 */
public class WsViewOrEditAttributesResults {

	/**
	 * result code of a request
	 */
	public enum WsViewOrEditAttributesResultsCode {
		
		/** found the subject, assigned or viewed */
		SUCCESS  {
			
			/** convert this code to a result code */
			@Override
			public WsViewOrEditAttributesResultCode convertToResultCode() {
				//note, it isnt a success if converting to resultcode
				return WsViewOrEditAttributesResultCode.EXCEPTION;
			}
			
		}, 
		
		/** found the subject */
		EXCEPTION  {
			
			/** convert this code to a result code */
			@Override
			public WsViewOrEditAttributesResultCode convertToResultCode() {
				return WsViewOrEditAttributesResultCode.EXCEPTION;
			}
			
		}, 
		
		/** problem deleting existing members */
		PROBLEM_WITH_GROUPS {
			
			/** convert this code to a result code */
			@Override
			public WsViewOrEditAttributesResultCode convertToResultCode() {
				return WsViewOrEditAttributesResultCode.EXCEPTION;
			}
			
		}, 
		
		/** invalid query (e.g. if everything blank) */
		INVALID_QUERY {
			
			/** convert this code to a result code */
			@Override
			public WsViewOrEditAttributesResultCode convertToResultCode() {
				return WsViewOrEditAttributesResultCode.INVALID_QUERY;
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
		public abstract WsViewOrEditAttributesResultCode convertToResultCode();

	}
	
	/**
	 * assign the code from the enum
	 * @param viewOrEditAttributesResultCode
	 */
	public void assignResultCode(WsViewOrEditAttributesResultsCode viewOrEditAttributesResultCode) {
		this.setResultCode(viewOrEditAttributesResultCode == null ? null : viewOrEditAttributesResultCode.name());
		this.setSuccess(viewOrEditAttributesResultCode.isSuccess() ? "T" : "F");
	}
	
	/**
	 * convert the result code back to enum
	 * @return the enum code
	 */
	public WsViewOrEditAttributesResultsCode retrieveResultCode() {
		if (StringUtils.isBlank(this.resultCode)) {
			return null;
		}
		return WsViewOrEditAttributesResultsCode.valueOf(this.resultCode);
	}

	/**
	 * error message if there is an error
	 */
	private StringBuilder resultMessage = new StringBuilder();
	
	/**
	 * results for each assignment sent in
	 */
	private WsViewOrEditAttributesResult[] results;

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
	public WsViewOrEditAttributesResult[] getResults() {
		return this.results;
	}

	/**
	 * results for each assignment sent in
	 * @param results1 the results to set
	 */
	public void setResults(WsViewOrEditAttributesResult[] results1) {
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
