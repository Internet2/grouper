package edu.internet2.middleware.grouper.webservices;

import java.util.Set;

import edu.internet2.middleware.grouper.Stem;

/**
 * returned from the stem find query, if none found, return none
 * 
 * @author mchyzer
 * 
 */
public class WsFindStemsResults {

	/**
	 * result code of a request
	 */
	public enum WsFindStemsResultsCode {

		/** found the stem (or not) */
		SUCCESS,

		/** found the subject */
		EXCEPTION,

		/** invalid query (e.g. if everything blank) */
		INVALID_QUERY,

		/** some stems had problems */
		PROBLEM_WITH_STEMS,

		/** if the parent was not found in a search by parent */
		PARENT_STEM_NOT_FOUND,

		/** if query with parent stem, and if that stem not found */
		QUERY_PARENT_STEM_NOT_FOUND;

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
	 * @param wsFindStemsResultsCode the code
	 * 
	 */
	public void assignResultCode(WsFindStemsResultsCode wsFindStemsResultsCode) {
		this.setResultCode(wsFindStemsResultsCode == null ? null
				: wsFindStemsResultsCode.name());
		this.setSuccess(wsFindStemsResultsCode.isSuccess() ? "T" : "F");
	}

	/**
	 * put a stem in the results
	 * 
	 * @param stem
	 */
	public void assignStemResult(Stem stem) {

		WsStemResult wsStemResult = new WsStemResult(stem);

		this.setStemResults(new WsStemResult[] { wsStemResult });
	}

	/**
	 * put a stem in the results
	 * 
	 * @param stemSet
	 */
	public void assignStemResult(Set<Stem> stemSet) {
		if (stemSet == null) {
			this.setStemResults(null);
			return;
		}
		int stemSetSize = stemSet.size();
		WsStemResult[] wsStemResults = new WsStemResult[stemSetSize];
		int index = 0;
		for (Stem stem : stemSet) {
			
			WsStemResult wsStemResult = new WsStemResult(stem);

			wsStemResults[index] = wsStemResult;
			index++;
		}
		this.setStemResults(wsStemResults);
	}

	/**
	 * has 0 to many stems that match the query by example
	 */
	private WsStemResult[] stemResults;
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
	 * has 0 to many stems that match the query by example
	 * 
	 * @return the stemResults
	 */
	public WsStemResult[] getStemResults() {
		return this.stemResults;
	}

	/**
	 * has 0 to many stems that match the query by example
	 * 
	 * @param stemResults1
	 *            the stemResults to set
	 */
	public void setStemResults(WsStemResult[] stemResults1) {
		this.stemResults = stemResults1;
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
