/**
 * 
 */
package edu.internet2.middleware.grouper.webservices;


/**
 * Result of one subject being added to a group.  The number of
 * subjects will equal the number of subjects sent in to the method
 * 
 * @author mchyzer
 */
public class WsAddMemberResult extends WsResult {
	
	/** subject that was added */
	private String subjectId;
	
	/** subject identifier (if this is what was passed in) that was added */
	private String subjectIdentifier;
	
	/**
	 * subject that was added
	 * @return the subjectId
	 */
	public String getSubjectId() {
		return subjectId;
	}

	/**
	 * subject that was added
	 * @param subjectId the subjectId to set
	 */
	public void setSubjectId(String subjectId) {
		this.subjectId = subjectId;
	}

	/**
	 * subject identifier (if this is what was passed in) that was added
	 * @return the subjectIdentifier
	 */
	public String getSubjectIdentifier() {
		return subjectIdentifier;
	}

	/**
	 * subject identifier (if this is what was passed in) that was added
	 * @param subjectIdentifier the subjectIdentifier to set
	 */
	public void setSubjectIdentifier(String subjectIdentifier) {
		this.subjectIdentifier = subjectIdentifier;
	}
}
