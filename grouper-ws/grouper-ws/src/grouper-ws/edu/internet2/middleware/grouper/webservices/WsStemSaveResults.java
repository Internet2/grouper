package edu.internet2.middleware.grouper.webservices;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.webservices.WsStemSaveResult.WsStemSaveResultCode;

/**
 * <pre>
 * results for the stems save call.
 * 
 * result code:
 * code of the result for this stem overall
 * SUCCESS: means everything ok
 * STEM_NOT_FOUND: cant find the stem
 * STEM_DUPLICATE: found multiple stems
 * </pre>
 * @author mchyzer
 */
public class WsStemSaveResults {

	/**
	 * result code of a request
	 */
	public enum WsStemSaveResultsCode {
		
		/** found the stems, saved them */
		SUCCESS  {
			
			/** convert this code to a result code */
			@Override
			public WsStemSaveResultCode convertToResultCode() {
				//note, it isnt a success if converting to resultcode
				return WsStemSaveResultCode.EXCEPTION;
			}
			
		}, 
		
		/** either overall exception, or one or more stems had exceptions */
		EXCEPTION  {
			
			/** convert this code to a result code */
			@Override
			public WsStemSaveResultCode convertToResultCode() {
				return WsStemSaveResultCode.EXCEPTION;
			}
			
		}, 
		
		/** problem deleting existing stems */
		PROBLEM_DELETING_STEMS {
			
			/** convert this code to a result code */
			@Override
			public WsStemSaveResultCode convertToResultCode() {
				return WsStemSaveResultCode.EXCEPTION;
			}
			
		}, 
		
		/** invalid query (e.g. if everything blank) */
		INVALID_QUERY {
			
			/** convert this code to a result code */
			@Override
			public WsStemSaveResultCode convertToResultCode() {
				return WsStemSaveResultCode.INVALID_QUERY;
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
		public abstract WsStemSaveResultCode convertToResultCode();

	}
	
	/**
	 * assign the code from the enum
	 * @param stemSaveResultsCode
	 */
	public void assignResultCode(WsStemSaveResultsCode stemSaveResultsCode) {
		this.setResultCode(stemSaveResultsCode == null ? null : stemSaveResultsCode.name());
		this.setSuccess(stemSaveResultsCode.isSuccess() ? "T" : "F");
	}
	
	/**
	 * convert the result code back to enum
	 * @return the enum code
	 */
	public WsStemSaveResultsCode retrieveResultCode() {
		if (StringUtils.isBlank(this.resultCode)) {
			return null;
		}
		return WsStemSaveResultsCode.valueOf(this.resultCode);
	}

	/**
	 * error message if there is an error
	 */
	private StringBuilder resultMessage = new StringBuilder();
	
	/**
	 * results for each deletion sent in
	 */
	private WsStemSaveResult[] results;

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
	public WsStemSaveResult[] getResults() {
		return this.results;
	}

	/**
	 * results for each deletion sent in
	 * @param results1 the results to set
	 */
	public void setResults(WsStemSaveResult[] results1) {
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
