package edu.internet2.middleware.grouper.webservices;

import edu.internet2.middleware.grouper.Group;


/**
 * returned from the group find query
 * @author mchyzer
 *
 */
public class WsFindGroupsResults extends WsResult {
	
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
	
	
	
}
