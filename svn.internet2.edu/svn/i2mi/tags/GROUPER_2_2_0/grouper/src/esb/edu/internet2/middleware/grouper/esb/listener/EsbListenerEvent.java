/*******************************************************************************
 * Copyright 2012 Internet2
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
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
