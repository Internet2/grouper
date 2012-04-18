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
/**
 * Copyright 2010 University of Chicago
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.internet2.middleware.grouperUportalConnector;

import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.jasig.portal.EntityTypes;
import org.jasig.portal.groups.EntityGroupImpl;
import org.jasig.portal.groups.IEntity;
import org.jasig.portal.groups.IIndividualGroupService;

/**
 * Implementation of an IEntityGroup for the Grouper groups.
 * 
 * @author Bill Brown
 * 
 */
public class GrouperEntityGroupImpl extends EntityGroupImpl implements IEntity,
		Serializable {

	/** Serial version UID. */
	private static final long serialVersionUID = -3972774817212994931L;

	/** Logger. */
	private static final Log LOGGER = LogFactory
			.getLog(GrouperEntityGroupImpl.class);

	/* is this entity a leaf or a group */
	private Class<?> leafOrGroup;

	/**
	 * Constructor for GrouperEntityGroupImpl.
	 * 
	 * @param key
	 *            The key of the group or subject.
	 * @param entityType
	 *            The type of the underlying entity.
	 *            EntityTypes.GROUP_ENTITY_TYPE for group.
	 *            EntityTypes.LEAF_ENTITY_TYPE for subject.
	 * @param name
	 *            The name of the group or subject.
	 * @param description
	 *            The description of the group or subject.
	 * @param localGroupService
	 *            the service that created this instance.
	 * @param leafOrGroup
	 *            a leaf or group type for this entity
	 */
	public GrouperEntityGroupImpl(final String key, final Class<?> entityType,
			final String name, final String description,
			IIndividualGroupService localGroupService, Class<?> leafOrGroup) {
		super(key, entityType);
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("creating new group for: " + key + " of type: "
					+ entityType);
		}
		setName(name);
		setDescription(description);
		setLocalGroupService(localGroupService);
		this.leafOrGroup = leafOrGroup;
	}

	/**
	 * Gives the String representation of this instance.
	 * 
	 * @return The String representation of this instance.
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return getClass().getSimpleName() + " (" + getKey() + ") " + getName();
	}

	/**
	 * Answers if Object o is an <code>IGroupMember</code> that refers to the
	 * same underlying entity(ies) as <code>this</code>.
	 * 
	 * @param o
	 *            The object ot test.
	 * @return True if the object is equal to this instance.
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(final Object o) {
		if (o == this) {
			return true;
		}
		if (!(o instanceof GrouperEntityGroupImpl)) {
			return false;
		}
		final GrouperEntityGroupImpl group = (GrouperEntityGroupImpl) o;
		return group.getKey().equals(getKey());
	}

	/**
	 * @return boolean.
	 * @see org.jasig.portal.groups.IGroupMember#isEntity()
	 */
	public boolean isEntity() {
		return this.leafOrGroup.equals(EntityTypes.LEAF_ENTITY_TYPE);
	}

	/**
	 * @return boolean.
	 * @see org.jasig.portal.groups.IGroupMember#isGroup()
	 */
	public boolean isGroup() {
		return this.leafOrGroup.equals(EntityTypes.GROUP_ENTITY_TYPE);
	}
}
