package edu.internet2.middleware.grouper.webservices;

import edu.internet2.middleware.grouper.Group;


/**
 * returned from the group find query
 * @author mchyzer
 *
 */
public class WsFindGroupsResults {
	
	/**
	 * result code of a request
	 */
	public enum WsFindGroupsResultCode {
		
		/** found the subject */
		SUCCESS, 
		
		/** found the subject */
		EXCEPTION, 
		
		/** invalid query (e.g. if everything blank) */
		INVALID_QUERY;
				
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
	 * @param wsFindGroupsResultCode
	 */
	public void assignResultCode(WsFindGroupsResultCode wsFindGroupsResultCode) {
		this.setResultCode(wsFindGroupsResultCode == null ? null : wsFindGroupsResultCode.name());
		this.setSuccess(wsFindGroupsResultCode.isSuccess() ? "T" : "F");
	}

	/**
	 * put a group in the results
	 * @param group
	 */
	public void assignGroupResult(Group group) {
		
		WsGroupResult wsGroupResult = new WsGroupResult(group);
		
		this.setGroupResults(new WsGroupResult[] {wsGroupResult});
	}
	
	/**
	 * has 0 to many groups that match the query by example
	 */
	private WsGroupResult[] groupResults;
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
	 * friendly message that could be audited or sent to a UI
	 */
	private String resultMessage;

	/**
	 * has 0 to many groups that match the query by example
	 * @return the groupResults
	 */
	public WsGroupResult[] getGroupResults() {
		return groupResults;
	}

	/**
	 * has 0 to many groups that match the query by example
	 * @param groupResults the groupResults to set
	 */
	public void setGroupResults(WsGroupResult[] groupResults) {
		this.groupResults = groupResults;
	}

	/**
	 * T or F as to whether it was a successful assignment
	 * @return the success
	 */
	public String getSuccess() {
		return success;
	}

	/**
	 * T or F as to whether it was a successful assignment
	 * @param success the success to set
	 */
	public void setSuccess(String success) {
		this.success = success;
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
		return resultCode;
	}

	/**
	 * <pre>
	 * code of the result for this subject
	 * SUCCESS: means everything ok
	 * SUBJECT_NOT_FOUND: cant find the subject
	 * SUBJECT_DUPLICATE: found multiple subjects
	 *  
	 * </pre>
	 * @param resultCode the resultCode to set
	 */
	public void setResultCode(String resultCode) {
		this.resultCode = resultCode;
	}

	/**
	 * friendly message that could be audited or sent to a UI
	 * @return the errorMessage
	 */
	public String getResultMessage() {
		return resultMessage;
	}

	/**
	 * friendly message that could be audited or sent to a UI
	 * @param errorMessage the errorMessage to set
	 */
	public void setResultMessage(String errorMessage) {
		this.resultMessage = errorMessage;
	}
	
	
	
}
