package edu.internet2.middleware.grouper.webservices;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.webservices.WsGroupDeleteResult.WsGroupDeleteResultCode;

/**
 * <pre>
 * results for the groups delete call.
 * 
 * result code:
 * code of the result for this group overall
 * SUCCESS: means everything ok
 * GROUP_NOT_FOUND: cant find the group
 * GROUP_DUPLICATE: found multiple groups
 * </pre>
 * @author mchyzer
 */
public class WsGroupDeleteResults {

	/**
	 * result code of a request
	 */
	public enum WsGroupDeleteResultsCode {
		
		/** found the groups, deleted them */
		SUCCESS  {
			
			/** convert this code to a result code */
			@Override
			public WsGroupDeleteResultCode convertToResultCode() {
				//note, it isnt a success if converting to resultcode
				return WsGroupDeleteResultCode.EXCEPTION;
			}
			
		}, 
		
		/** either overall exception, or one or more groups had exceptions */
		EXCEPTION  {
			
			/** convert this code to a result code */
			@Override
			public WsGroupDeleteResultCode convertToResultCode() {
				return WsGroupDeleteResultCode.EXCEPTION;
			}
			
		}, 
		
		/** problem deleting existing groups */
		PROBLEM_DELETING_GROUPS {
			
			/** convert this code to a result code */
			@Override
			public WsGroupDeleteResultCode convertToResultCode() {
				return WsGroupDeleteResultCode.EXCEPTION;
			}
			
		}, 
		
		/** invalid query (e.g. if everything blank) */
		INVALID_QUERY {
			
			/** convert this code to a result code */
			@Override
			public WsGroupDeleteResultCode convertToResultCode() {
				return WsGroupDeleteResultCode.INVALID_QUERY;
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
		public abstract WsGroupDeleteResultCode convertToResultCode();

	}
	
	/**
	 * assign the code from the enum
	 * @param groupsDeleteResultsCode
	 */
	public void assignResultCode(WsGroupDeleteResultsCode groupsDeleteResultsCode) {
		this.setResultCode(groupsDeleteResultsCode == null ? null : groupsDeleteResultsCode.name());
		this.setSuccess(groupsDeleteResultsCode.isSuccess() ? "T" : "F");
	}
	
	/**
	 * convert the result code back to enum
	 * @return the enum code
	 */
	public WsGroupDeleteResultsCode retrieveResultCode() {
		if (StringUtils.isBlank(this.resultCode)) {
			return null;
		}
		return WsGroupDeleteResultsCode.valueOf(this.resultCode);
	}

	/**
	 * error message if there is an error
	 */
	private StringBuilder resultMessage = new StringBuilder();
	
	/**
	 * results for each deletion sent in
	 */
	private WsGroupDeleteResult[] results;

	/** T or F as to whether it was a successful deletion */
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
	 * results for each deletion sent in
	 * @return the results
	 */
	public WsGroupDeleteResult[] getResults() {
		return this.results;
	}

	/**
	 * results for each deletion sent in
	 * @param results1 the results to set
	 */
	public void setResults(WsGroupDeleteResult[] results1) {
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
	 * T or F as to whether it was a successful deletion
	 * @return the success
	 */
	public String getSuccess() {
		return this.success;
	}

	/**
	 * T or F as to whether it was a successful deletion
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
