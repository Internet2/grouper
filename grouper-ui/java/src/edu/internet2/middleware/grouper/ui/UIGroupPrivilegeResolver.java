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
 * @see https://bugs.internet2.edu/jira/browse/GRP-72
 * 
 * @author Gary Brown.
 * @version $Id: UIGroupPrivilegeResolver.java,v 1.1 2008-01-09 13:26:18 isgwb Exp $
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
	 * @param g
	 */
	public void setSubject(Subject subj);
	
	/**
	 * Can only be called once, by the factory,
	 * to set the GrouperSession 
	 * @param g
	 */
	public void setGrouperSession(GrouperSession s);
	
	/**
	 * Is the subject allowed to grant / revoke privileges?
	 * @return
	 */
	public boolean canManagePrivileges();
	
	/**
	 * Is the subject allowed to edit core attributes
	 * or delete the group?
	 * @return
	 */
	public boolean canEditGroup();
	
	/**
	 * Is the subject allowed to view the group?
	 * @return
	 */
	public boolean canViewGroup();
	
	/**
	 * Is the subject allowed to view the membership?
	 * @return
	 */
	public boolean canReadGroup();
	
	/**
	 * Is the subject allowed to optin to the group?
	 * @return
	 */
	public boolean canOptinGroup();
	
	/**
	 * Is the subject allowed to optout of the group?
	 * @return
	 */
	public boolean canOptoutGroup();
	

	/**
	 * Can the subject modify the given field?
	 * @param field
	 * @return
	 */
	public boolean canManageField(String field);
	
	/**
	 * Can the subject read the given field?
	 * @param field
	 * @return
	 */
	public boolean canReadField(String field);
	
	
	/**
	 * Can the subject change at least one custom attribute?
	 * @return
	 */
	public boolean canManageAnyCustomField();
	
	
	/**
	 * Can the subject update the membership?
	 * @return
	 */
	public boolean canManageMembers();
	
	/**
	 * Convenience method to provide all possible 'answers' in a
	 * Map - which is strightforward for JSP/JSTL to 'query'.
	 * Keys are method names. canReadField and canWriteField
	 * use nested Maps. In JSTL ${groupPrivilegeResolver.canReadField['field']}
	 * @return
	 */
	public Map asMap();
	
	/**
	 * Called by the factory after group, subject and GrouperSession are set
	 * to allow any initialisation
	 */
	public void init();

}
