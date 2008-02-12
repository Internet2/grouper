package edu.internet2.middleware.grouper.webservices;

import java.util.Set;

import edu.internet2.middleware.grouper.Group;

/**
 * returned from the group find query
 * 
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
		INVALID_QUERY,

		/** cant find the stem in a stem search */
		STEM_NOT_FOUND;

		/**
		 * if this is a successful result
		 * 
		 * @return true if success
		 */
		public boolean isSuccess() {
			return this == SUCCESS;
		}
	}

	/**
	 * assign the code from the enum
	 * 
	 * @param wsFindGroupsResultCode
	 */
	public void assignResultCode(WsFindGroupsResultCode wsFindGroupsResultCode) {
		this.setResultCode(wsFindGroupsResultCode == null ? null
				: wsFindGroupsResultCode.name());
		this.setSuccess(wsFindGroupsResultCode.isSuccess() ? "T" : "F");
	}

	/**
	 * put a group in the results
	 * 
	 * @param group
	 */
	public void assignGroupResult(Group group) {

		WsGroupResult wsGroupResult = new WsGroupResult(group);

		this.setGroupResults(new WsGroupResult[] { wsGroupResult });
	}

	/**
	 * put a group in the results
	 * 
	 * @param groupSet
	 */
	public void assignGroupResult(Set<Group> groupSet) {
		if (groupSet == null) {
			this.setGroupResults(null);
			return;
		}
		int groupSetSize = groupSet.size();
		WsGroupResult[] wsGroupResults = new WsGroupResult[groupSetSize];
		int index = 0;
		for (Group group : groupSet) {
			WsGroupResult wsGroupResult = new WsGroupResult(group);
			wsGroupResults[index] = wsGroupResult;
			index++;
		}
		this.setGroupResults(wsGroupResults);
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
	 * friendly message that could be audited
	 */
	private String resultMessage;

	/**
	 * has 0 to many groups that match the query by example
	 * 
	 * @return the groupResults
	 */
	public WsGroupResult[] getGroupResults() {
		return this.groupResults;
	}

	/**
	 * has 0 to many groups that match the query by example
	 * 
	 * @param groupResults1
	 *            the groupResults to set
	 */
	public void setGroupResults(WsGroupResult[] groupResults1) {
		this.groupResults = groupResults1;
	}

	/**
	 * T or F as to whether it was a successful assignment
	 * 
	 * @return the success
	 */
	public String getSuccess() {
		return this.success;
	}

	/**
	 * T or F as to whether it was a successful assignment
	 * 
	 * @param success1
	 *            the success to set
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
	 * 
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
	 * 
	 * @param resultCode1
	 *            the resultCode to set
	 */
	public void setResultCode(String resultCode1) {
		this.resultCode = resultCode1;
	}

	/**
	 * friendly message that could be audited
	 * 
	 * @return the errorMessage
	 */
	public String getResultMessage() {
		return this.resultMessage;
	}

	/**
	 * friendly message that could be audited
	 * 
	 * @param errorMessage
	 *            the errorMessage to set
	 */
	public void setResultMessage(String errorMessage) {
		this.resultMessage = errorMessage;
	}

}
