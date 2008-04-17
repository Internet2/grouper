/*
Copyright 2004-2007 University Corporation for Advanced Internet Development, Inc.
Copyright 2004-2007 The University Of Bristol

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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.FieldFinder;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupType;
import edu.internet2.middleware.grouper.GrouperHelper;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.subject.Subject;

/**
 * The default implementation of UIGroupPrivilegeResolver - which simply
 * applies the expected privilege resolution. Extend this class to add
 * your own business logic, and configure in media.properties using the key:
 * edu.internet2.middleware.grouper.ui.UIGroupPrivilegeResolver
 * see https://bugs.internet2.edu/jira/browse/GRP-72
 * 
 * @author Gary Brown.
 * @version $Id: DefaultUIGroupPrivilegeResolver.java,v 1.2 2008-04-17 18:59:46 isgwb Exp $
 */
public class DefaultUIGroupPrivilegeResolver implements
		UIGroupPrivilegeResolver {
	protected Group group;
	protected Subject subject;
	protected GrouperSession s;
	protected boolean inited=false;

	Map privs;

	/* (non-Javadoc)
	 * @see edu.internet2.middleware.grouper.ui.UIGroupPrivilegeResolver#init()
	 */
	public void init() {
				
	}

	/* (non-Javadoc)
	 * @see edu.internet2.middleware.grouper.ui.UIGroupPrivilegeResolver#canEditGroup()
	 */
	public boolean canEditGroup() {
		try {
			return group.canWriteField(FieldFinder.find("extension"));
		}catch(Exception e) {
			throw new RuntimeException(e);
		}
	}

	/* (non-Javadoc)
	 * @see edu.internet2.middleware.grouper.ui.UIGroupPrivilegeResolver#canManageMembers()
	 */
	public boolean canManageMembers() {
		try {
			return group.canWriteField(FieldFinder.find("members"));
		}catch(Exception e) {
			throw new RuntimeException(e);
		}
	}

	/* (non-Javadoc)
	 * @see edu.internet2.middleware.grouper.ui.UIGroupPrivilegeResolver#canManagePrivileges()
	 */
	public boolean canManagePrivileges() {
		try {
			return group.canWriteField(FieldFinder.find("extension"));
		}catch(Exception e) {
			throw new RuntimeException(e);
		}
	}

	/* (non-Javadoc)
	 * @see edu.internet2.middleware.grouper.ui.UIGroupPrivilegeResolver#canManageField(java.lang.String)
	 */
	public boolean canManageField(String field) {
		try {
			return group.canWriteField(FieldFinder.find(field));
		}catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/* (non-Javadoc)
	 * @see edu.internet2.middleware.grouper.ui.UIGroupPrivilegeResolver#canReadField(java.lang.String)
	 */
	public boolean canReadField(String field) {
		try {
			return group.canReadField(FieldFinder.find(field));
		}catch(Exception e) {
			throw new RuntimeException(e);
		}
	}

	
	/* (non-Javadoc)
	 * @see edu.internet2.middleware.grouper.ui.UIGroupPrivilegeResolver#canManageAnyCustomField()
	 */
	public boolean canManageAnyCustomField() {
		try {
			return GrouperHelper.canUserEditAnyCustomAttribute(group);
		}catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/* (non-Javadoc)
	 * @see edu.internet2.middleware.grouper.ui.UIGroupPrivilegeResolver#canOptinGroup()
	 */
	public boolean canOptinGroup() {
		return group.hasOptin(subject);
	}

	/* (non-Javadoc)
	 * @see edu.internet2.middleware.grouper.ui.UIGroupPrivilegeResolver#canOptoutGroup()
	 */
	public boolean canOptoutGroup() {
		return group.hasOptout(subject);
	}

	/* (non-Javadoc)
	 * @see edu.internet2.middleware.grouper.ui.UIGroupPrivilegeResolver#canReadGroup()
	 */
	public boolean canReadGroup() {
		try {
			return group.canReadField(FieldFinder.find("members"));
		}catch(Exception e) {
			throw new RuntimeException(e);
		}
	}

	/* (non-Javadoc)
	 * @see edu.internet2.middleware.grouper.ui.UIGroupPrivilegeResolver#canViewGroup()
	 */
	public boolean canViewGroup() {
		try {
			return group.canReadField(FieldFinder.find("displayExtension"));
		}catch(Exception e) {
			throw new RuntimeException(e);
		}
	}

	/* (non-Javadoc)
	 * @see edu.internet2.middleware.grouper.ui.UIGroupPrivilegeResolver#setGroup(edu.internet2.middleware.grouper.Group)
	 */
	public final void setGroup(Group g) {
		if(group!=null) throw new IllegalStateException("setGroup already called");
		group=g;
	}

	/* (non-Javadoc)
	 * @see edu.internet2.middleware.grouper.ui.UIGroupPrivilegeResolver#setGrouperSession(edu.internet2.middleware.grouper.GrouperSession)
	 */
	public void setGrouperSession(GrouperSession s) {
		if(this.s!=null) throw new IllegalStateException("setGrouperSession already called");
		this.s=s;

	}

	/* (non-Javadoc)
	 * @see edu.internet2.middleware.grouper.ui.UIGroupPrivilegeResolver#setSubject(edu.internet2.middleware.subject.Subject)
	 */
	public void setSubject(Subject subj) {
		if(subject!=null) throw new IllegalStateException("setSubject lready called");
		subject=subj;

	}

	/* (non-Javadoc)
	 * @see edu.internet2.middleware.grouper.ui.UIGroupPrivilegeResolver#asMap()
	 */
	public Map asMap() {
		Map map = new HashMap();
		map.put("canEditGroup", canEditGroup());
		map.put("canManageMembers", canManageMembers());
		map.put("canManagePrivileges", canManagePrivileges());
		map.put("canManageAnyCustomField", canManageAnyCustomField());
		map.put("canOptinGroup", canOptinGroup());
		map.put("canOptoutGroup", canOptoutGroup());
		map.put("canViewGroup", canViewGroup());
		map.put("canReadGroup", canReadGroup());
		Map fieldManageMap = new HashMap();
		Map fieldReadMap = new HashMap();
		map.put("canManageField", fieldManageMap);
		map.put("canReadField", fieldReadMap);
		Set<GroupType> types=group.getTypes();
		Set<Field> fields;
		for(GroupType type : types) {
			if(!type.isSystemType()) {
				fields = type.getFields();
				for(Field field : fields) {
					fieldManageMap.put(field.getName(), canManageField(field.getName()));
					fieldReadMap.put(field.getName(), canReadField(field.getName()));
				}
			}
		}
		return map;
	}
}
