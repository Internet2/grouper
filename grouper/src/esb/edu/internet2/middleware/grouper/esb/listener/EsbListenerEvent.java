/*
 * @author Rob Hebron
 */

package edu.internet2.middleware.grouper.esb.listener;

/**
 * 
 * Simple class to hold incoming data sent by an ESB. Currently only direct
 * membership add and delete events are supported
 *
 */
public class EsbListenerEvent {
	private String subjectId;
	private String[][] addMembershipGroups;
	private String[][] removeMembershipGroups;
	/**
	 * 
	 * @return subjectId
	 */
	public String getSubjectId() {
		return subjectId;
	}
	/**
	 * 
	 * @param subjectId for which memberships are being changed
	 */
	public void setSubjectId(String subjectId) {
		this.subjectId = subjectId;
	}
	/**
	 * 
	 * @return addMembershipGroups
	 */
	public String[][] getAddMembershipGroups() {
		return addMembershipGroups;
	}
	/**
	 * 
	 * @param addMembershipGroups - an array of arrays, each having two elements
	 * 1. The group identifier
	 * 2. The search method by which the group can be found by its identifier - id, 
	 * name or attribute name (e.g. extension)
	 */
	public void setAddMembershipGroups(String[][] addMembershipGroups) {
		this.addMembershipGroups = addMembershipGroups;
	}
	/**
	 * 
	 * @return removeMembershipGroups
	 */
	public String[][] getRemoveMembershipGroups() {
		return removeMembershipGroups;
	}
	/**
	 * 
	 * @param removeMembershipGroups - an array of arrays, each having two elements
	 * 1. The group identifier
	 * 2. The search method by which the group can be found by its identifier - id, 
	 * name or attribute name (e.g. extension)
	 */
	public void setRemoveMembershipGroups(String[][] removeMembershipGroups) {
		this.removeMembershipGroups = removeMembershipGroups;
	}

}
