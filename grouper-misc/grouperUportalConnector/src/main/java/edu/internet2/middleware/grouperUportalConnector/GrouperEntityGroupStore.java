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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.jasig.portal.EntityIdentifier;
import org.jasig.portal.EntityTypes;
import org.jasig.portal.groups.GroupsException;
import org.jasig.portal.groups.IEntity;
import org.jasig.portal.groups.IEntityGroup;
import org.jasig.portal.groups.IEntityGroupStore;
import org.jasig.portal.groups.IEntitySearcher;
import org.jasig.portal.groups.IEntityStore;
import org.jasig.portal.groups.IGroupConstants;
import org.jasig.portal.groups.IGroupMember;
import org.jasig.portal.groups.ILockableEntityGroup;
import org.jasig.portal.security.IPerson;

/**
 * Class used to Store Grouper groups for UPortal
 * 
 * @author Bill Brown
 * 
 */
public class GrouperEntityGroupStore implements IEntityGroupStore,
		IEntityStore, IEntitySearcher {

	/** Logger. */
	private static final Log LOGGER = LogFactory
			.getLog(GrouperEntityGroupStoreFactory.class);

	/* the grouper web service */
	private GrouperWebService grouperWS = null;

	/**
	 * Package protected constructor. Constructor for GrouperEntityGroupStore.
	 */
	GrouperEntityGroupStore() { /* Package protected. */
		this.grouperWS = new GrouperWebService(this);
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(this + " created");
		}
	}

	/**
	 * Answers if <code>group</code> contains <code>member</code>.
	 * 
	 * @return boolean
	 * @param group
	 *            org.jasig.portal.groups.IEntityGroup
	 * @param member
	 *            org.jasig.portal.groups.IGroupMember
	 */
	public boolean contains(IEntityGroup group, IGroupMember member)
			throws GroupsException {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(this);
		}
		return grouperWS.contains(group, member);
	}

	// this currently isn't supported.
	/**
	 * Delete this <code>IEntityGroup</code> from the data store.
	 * 
	 * @param group
	 *            org.jasig.portal.groups.IEntityGroup
	 */
	public void delete(IEntityGroup group) throws GroupsException {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(this);
		}
		grouperWS.deleteGroup(group);
	}

	/**
	 * Returns an instance of the <code>IEntityGroup</code> from the data store.
	 * 
	 * @return org.jasig.portal.groups.IEntityGroup
	 * @param key
	 *            java.lang.String
	 */
	public IEntityGroup find(String key) throws GroupsException {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(this + " with key: " + key);
		}
		return grouperWS.find(key);
	}

	// called by the portal at login to find the groups
	// that this user belongs to.
	/**
	 * Returns an <code>Iterator</code> over the <code>Collection</code> of
	 * <code>IEntityGroups</code> that the <code>IGroupMember</code> belongs to.
	 * 
	 * @return java.util.Iterator
	 * @param gm
	 *            org.jasig.portal.groups.IEntityGroup
	 */
	@SuppressWarnings("unchecked")
	public Iterator findContainingGroups(IGroupMember gm)
			throws GroupsException {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(this + " with key: " + gm.getKey());
		}

		if (gm.isGroup()) {

			return new LinkedList<IEntityGroup>().iterator();

		} else {

			// find the groups from the web service
			Iterator<IEntityGroup> ctnGrps = grouperWS.findContainingGroups(gm);

			List<IEntityGroup> results = new LinkedList<IEntityGroup>();
			while (ctnGrps.hasNext()) {
				results.add(ctnGrps.next());
			}
			return results.iterator();

		}
	}

	/**
	 * Returns an <code>Iterator</code> over the <code>Collection</code> of
	 * <code>IEntities</code> that are members of this <code>IEntityGroup</code>
	 * .
	 * 
	 * @return java.util.Iterator
	 * @param group
	 *            org.jasig.portal.groups.IEntityGroup
	 */
	@SuppressWarnings("unchecked")
	public Iterator findEntitiesForGroup(IEntityGroup group)
			throws GroupsException {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(this + " with key: " + group.getKey()
					+ " and local key: " + group.getLocalKey());
		}

		return grouperWS.findEntitiesForGroup(group.getLocalKey());
	}

	/**
	 * Returns an instance of the <code>ILockableEntityGroup</code> from the
	 * data store.
	 * 
	 * @return org.jasig.portal.groups.IEntityGroup
	 * @param key
	 *            java.lang.String
	 */
	public ILockableEntityGroup findLockable(String key) throws GroupsException {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(this + " with key: " + key);
		}
		return grouperWS.findGroupWithLock(key, null);
	}

	/**
	 * Returns a <code>String[]</code> containing the keys of
	 * <code>IEntityGroups</code> that are members of this
	 * <code>IEntityGroup</code>. In a composite group system, a group may
	 * contain a member group from a different service. This is called a foreign
	 * membership, and is only possible in an internally-managed service. A
	 * group store in such a service can return the key of a foreign member
	 * group, but not the group itself, which can only be returned by its local
	 * store.
	 * 
	 * @return String[]
	 * @param group
	 *            org.jasig.portal.groups.IEntityGroup
	 */
	@SuppressWarnings("unchecked")
	public String[] findMemberGroupKeys(IEntityGroup group)
			throws GroupsException {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(this + " with key: " + group.getKey()
					+ " and local key: " + group.getLocalKey());
		}

		List<String> keys = new ArrayList<String>();
		final Iterator<IEntityGroup> it = findMemberGroups(group);
		while (it.hasNext()) {
			IEntityGroup eg = it.next();
			keys.add(eg.getKey());
		}
		return keys.toArray(new String[keys.size()]);
	}

	/**
	 * Returns an <code>Iterator</code> over the <code>Collection</code> of
	 * <code>IEntityGroups</code> that are members of this
	 * <code>IEntityGroup</code>.
	 * 
	 * @return java.util.Iterator
	 * @param group
	 *            org.jasig.portal.groups.IEntityGroup
	 */
	@SuppressWarnings("unchecked")
	public Iterator findMemberGroups(IEntityGroup group) throws GroupsException {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(this + " with key: " + group.getKey()
					+ " and local key: " + group.getLocalKey());
		}
		return grouperWS.findMembers(group);
	}

	/**
	 * @return org.jasig.portal.groups.IEntityGroup
	 */
	@SuppressWarnings("unchecked")
	public IEntityGroup newInstance(Class entityType) throws GroupsException {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(this + " with entityType: " + entityType);
		}
		return grouperWS.newGroup(entityType);
	}

	/**
	 * Find EntityIdentifiers for groups whose name matches the query string
	 * according to the specified method and matches the provided leaf type
	 * 
	 * @param query
	 *            The part of the name of the group.
	 * @param method
	 *            The method used to perform the comparison.
	 * @param leaftype
	 *            The type of groups.
	 * @return The array of groups descriptions whose name match query according
	 *         to the method of comparison.
	 * @see org.jasig.portal.groups.IEntityGroupStore#searchForGroups(java.lang.String,
	 *      int, java.lang.Class)
	 */
	public EntityIdentifier[] searchForGroups(final String query,
			final int method,
			@SuppressWarnings("unchecked") final Class leaftype) {

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(this + " for: " + query);
		}

		// result groups.
		List<EntityIdentifier> groups = new ArrayList<EntityIdentifier>();

		EntityIdentifier[] ctnGrps = grouperWS.searchForGroups(query, method,
				leaftype);

		// get the method type
		switch (method) {
		case IGroupConstants.IS:// 1

			for (EntityIdentifier ei : ctnGrps) {
				if (ei.getKey().equals(query)) {
					groups.add(ei);
				}
			}

			break;
		case IGroupConstants.STARTS_WITH:// 2

			for (EntityIdentifier ei : ctnGrps) {
				if (ei.getKey().startsWith(query)) {
					groups.add(ei);
				}
			}

			break;
		case IGroupConstants.ENDS_WITH:// 3

			for (EntityIdentifier ei : ctnGrps) {
				if (ei.getKey().endsWith(query)) {
					groups.add(ei);
				}
			}

			break;
		case IGroupConstants.CONTAINS:// 4

			for (EntityIdentifier ei : ctnGrps) {
				if (ei.getKey().indexOf(query) >= 0) {
					groups.add(ei);
				}
			}

			break;
		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("size of results: " + groups.size());
		}

		return (EntityIdentifier[]) groups.toArray(new EntityIdentifier[] {});
	}

	/**
	 * Adds or updates the <code>IEntityGroup</code> AND ITS MEMBERSHIPS to the
	 * data store, as appropriate.
	 * 
	 * @param group
	 *            org.jasig.portal.groups.IEntityGroup
	 */
	public void update(IEntityGroup group) throws GroupsException {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(this + " with key: " + group.getKey()
					+ " and local key: " + group.getLocalKey());
		}
		grouperWS.updateGroup(group);
	}

	/**
	 * Commits the group memberships of the <code>IEntityGroup</code> to the
	 * data store.
	 * 
	 * @param group
	 *            org.jasig.portal.groups.IEntityGroup
	 */
	public void updateMembers(IEntityGroup group) throws GroupsException {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(this + " with key: " + group.getKey()
					+ " and local key: " + group.getLocalKey());
		}
		grouperWS.updateGroupMembers(group);
	}

	/**
	 * @return org.jasig.portal.groups.IEntity
	 * @param key
	 *            java.lang.String
	 */
	public IEntity newInstance(String key) throws GroupsException {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(this + " with key: " + key);
		}
		return new GrouperEntityGroupImpl(key, IPerson.class, key, "",
				grouperWS, EntityTypes.GROUP_ENTITY_TYPE);
	}

	/**
	 * @return org.jasig.portal.groups.IEntity
	 * @param key
	 *            java.lang.String - the entity's key
	 * @param type
	 *            java.lang.Class - the entity's Type
	 */
	@SuppressWarnings("unchecked")
	public IEntity newInstance(String key, Class type) throws GroupsException {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(this + " with key: " + key + " and type: " + type);
		}
		return new GrouperEntityGroupImpl(key, type, key, "", grouperWS,
				EntityTypes.GROUP_ENTITY_TYPE);
	}

	/**
	 * Find EntityIdentifiers for entities whose name matches the query string
	 * according to the specified method and is of the specified type
	 */
	@SuppressWarnings("unchecked")
	public EntityIdentifier[] searchForEntities(String query, int method,
			Class type) throws GroupsException {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(this + " with query: " + query);
		}
		return grouperWS.searchForEntities(query, method, type);
	}
}
