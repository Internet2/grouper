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
Copyright 2004-2008 University Corporation for Advanced Internet Development, Inc.
Copyright 2004-2008 The University Of Bristol

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package edu.internet2.middleware.grouper.ui;

import java.util.Map;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.subject.Subject;

/**
 * Interface which provides ability to override the default
 * privilege model for Groups in the UI i.e. if a group has been 'loaded'
 * it should probably be maintained by the loader rather than a user -
 * even GrouperSystem/wheel group members.
 * 
 * see https://bugs.internet2.edu/jira/browse/GRP-72
 * 
 * @author Gary Brown.
 * @version $Id: UIGroupPrivilegeResolver.java,v 1.3 2008-04-17 20:48:07 isgwb Exp $
 */
public interface UIGroupPrivilegeResolver {
	/**
	 * Can only be called once, by the factory,
	 * to set the group for which we are resolving privileges
	 * @param g
	 */
	public void setGroup(Group g);
	
	/**
	 * Can only be called once, by the factory,
	 * to set the subject for who we are resolving privileges
	 * @param subj
	 */
	public void setSubject(Subject subj);
	
	/**
	 * Can only be called once, by the factory,
	 * to set the GrouperSession 
	 * @param s
	 */
	public void setGrouperSession(GrouperSession s);
	
	/**
	 * Is the subject allowed to grant / revoke privileges?
	 * @return whether the Subject can manage privileges
	 */
	public boolean canManagePrivileges();
	
	/**
	 * if allowed to invite external people to this group
	 * @return true if allowed to invite external people
	 */
	public boolean canInviteExternalPeople();
	
	/**
	 * Is the subject allowed to edit core attributes
	 * or delete the group?
	 * @return if the Subject can edit group
	 */
	public boolean canEditGroup();
	
	/**
	 * Is the subject allowed to view the group?
	 * @return whether the Subject can view the group
	 */
	public boolean canViewGroup();
	
	/**
	 * Is the subject allowed to view the membership?
	 * @return whether the Subject can read the group
	 */
	public boolean canReadGroup();
	
	/**
	 * Is the subject allowed to optin to the group?
	 * @return whether the Subject can optin to the group
	 */
	public boolean canOptinGroup();
	
	/**
	 * Is the subject allowed to optout of the group?
	 * @return whether the Subject can opt out of the  group
	 */
	public boolean canOptoutGroup();
	

	/**
	 * Can the subject modify the given field?
	 * @param field
	 * @return whether the Subject can manage teh given field
	 */
	public boolean canManageField(String field);
	
	/**
	 * Can the subject read the given field?
	 * @param field
	 * @return whether the Subject can read th egiven field
	 */
	public boolean canReadField(String field);
	
	
	/**
	 * Can the subject change at least one custom attribute?
	 * @return if there are any custom attributes that the Subject can manage
	 */
	public boolean canManageAnyCustomField();
	
	
	/**
	 * Can the subject update the membership?
	 * @return if the Subject can manage members
	 */
	public boolean canManageMembers();
	
	/**
	 * Convenience method to provide all possible 'answers' in a
	 * Map - which is strightforward for JSP/JSTL to 'query'.
	 * Keys are method names. canReadField and canWriteField
	 * use nested Maps. In JSTL ${groupPrivilegeResolver.canReadField['field']}
	 * @return all privilege resolutions
	 */
	public Map asMap();
	
	/**
	 * Called by the factory after group, subject and GrouperSession are set
	 * to allow any initialisation
	 */
	public void init();

}
