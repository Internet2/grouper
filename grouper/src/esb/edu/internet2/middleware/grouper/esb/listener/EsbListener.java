/**
 * Copyright 2014 Internet2
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
 */
/*
 * @author Rob Hebron
 */

package edu.internet2.middleware.grouper.esb.listener;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * 
 * Class to process incoming events and convert to operations in Grouper, running with
 * root privileges
 *
 */
public class EsbListener {
	private GrouperSession grouperSession;
	private static final Log LOG = GrouperUtil.getLog(EsbListener.class);
	/**
	 * 
	 * @param the jsonString representation of a populated {@link EsbListenerEvent} class
	 * @param an initialised grouperSession
	 * @return returnMessage - a human readable string containing results of operations
	 * to return to calling client
	 */
	public String processEvent(String jsonString, GrouperSession grouperSession) {
		this.grouperSession = grouperSession;
		String returnMessage="";
		EsbListenerEvents events = (EsbListenerEvents) GrouperUtil.jsonConvertFrom(jsonString, EsbListenerEvents.class);
		for(int i=0;i<events.getEsbListenerEvent().length;i++) {
			EsbListenerEvent event = events.getEsbListenerEvent()[i];
		
			String subjectId = event.getSubjectId();
			if(subjectId==null || subjectId.equals("")) {
				if(LOG.isDebugEnabled()) {
					LOG.debug("SubjectId null or blank");
				}
				return "Fatal error: subject not found";
				
			}
			Subject subject=SubjectFinder.findById(subjectId, false);
			if(subject==null) {
				if(LOG.isDebugEnabled()) {
					LOG.debug("SubjectId " + subjectId + " not found");
				}
				return "Error: subject not found";
			}
			returnMessage = returnMessage + this.processMembershipChanges(event.getAddMembershipGroups(), subject, true);
			returnMessage = returnMessage + this.processMembershipChanges(event.getRemoveMembershipGroups(), subject, false);
		}
		return returnMessage;
		
	}
	/**
	 * Method to add and and remove subject membership to/from groups in registry
	 * @param groups - array of group identifiers + search type arrays
	 * @param subject - the subject to add/remove to/from groups
	 * @param addOp - true is add memberships, false if delete memberships
	 * @return returnMessage - simple human readable result to return to calling client
	 */
	private String processMembershipChanges(String[][] groups, Subject subject, boolean addOp) {
		if(groups==null) return "";
		String returnMessage="";
		if(LOG.isDebugEnabled()) {
			if(addOp) {
				LOG.debug("Adding " + subject.getId() + " to groups");
			} else {
				LOG.debug("Removing " + subject.getId() + " from groups");
			}
		}
		for(int i=0; i<groups.length;i++ ) {
			Group group = null;
			String[] thisGroup = groups[i];
			String groupIdentifer = thisGroup[0];
			String searchType = thisGroup[1];
			if(searchType.equals("id")) {
				if(LOG.isDebugEnabled()) {
					LOG.debug("Finding group by id " + groupIdentifer);
				}
				group = GroupFinder.findByUuid(grouperSession, groupIdentifer, false);
			} else if (searchType.equals("name")) {
				if(LOG.isDebugEnabled()) {
					LOG.debug("Finding group by name " + groupIdentifer);
				}
				group = GroupFinder.findByName(grouperSession, groupIdentifer, false);
			} else {
				if(LOG.isDebugEnabled()) {
					LOG.debug("Finding group by " + searchType + " "+ groupIdentifer);
				}
				group = GroupFinder.findByAttribute(grouperSession, searchType, groupIdentifer, false);
			}
			if(group!=null) {
				if(addOp) {
					if(LOG.isDebugEnabled()) {
						LOG.debug("Adding membership");
					}
					group.addMember(subject);
					returnMessage = returnMessage + "Added " + subject.getId() + " as member of group " + group.getName();
				} else {
					if(LOG.isDebugEnabled()) {
						LOG.debug("Removing membership");
					}
					group.deleteMember(subject);
					returnMessage = returnMessage + "Deleted " + subject.getId() + " as member of group " + group.getName();
				}
			} else {
				if(LOG.isDebugEnabled()) {
					LOG.debug("Group id " + groupIdentifer + " not found");
				}
				return "Group id " + groupIdentifer + " not found\r\n";
			}
		}
		return returnMessage;
	}
}
