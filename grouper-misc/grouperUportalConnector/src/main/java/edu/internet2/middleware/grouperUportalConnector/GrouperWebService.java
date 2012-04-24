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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.naming.CompositeName;
import javax.naming.Name;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.EntityIdentifier;
import org.jasig.portal.EntityTypes;
import org.jasig.portal.groups.CompositeEntityIdentifier;
import org.jasig.portal.groups.EntityImpl;
import org.jasig.portal.groups.GroupsException;
import org.jasig.portal.groups.IEntity;
import org.jasig.portal.groups.IEntityGroup;
import org.jasig.portal.groups.IGroupMember;
import org.jasig.portal.groups.IIndividualGroupService;
import org.jasig.portal.groups.ILockableEntityGroup;
import org.jasig.portal.security.IPerson;

import edu.internet2.middleware.grouperClient.api.GcFindGroups;
import edu.internet2.middleware.grouperClient.api.GcGetGroups;
import edu.internet2.middleware.grouperClient.api.GcGetMembers;
import edu.internet2.middleware.grouperClient.ws.beans.WsFindGroupsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetGroupsResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetGroupsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetMembersResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetMembersResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroup;
import edu.internet2.middleware.grouperClient.ws.beans.WsQueryFilter;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubject;

/**
 * The grouper web service client wrapper.
 * 
 * @author Bill Brown
 * 
 */
public class GrouperWebService implements IIndividualGroupService {

	private static final Log LOGGER = LogFactory
			.getLog(GrouperWebService.class);

	private Name serviceName = null;
	private Map<Name, IIndividualGroupService> componentService = null;
	private GrouperEntityGroupStore grouperEntityGroupStore = null;

	/**
	 * @throws GroupsException
	 *             if the service cannot be created.
	 * 
	 */
	public GrouperWebService() throws GroupsException {
		try {
			this.serviceName = new CompositeName("grouper");
			componentService = new HashMap<Name, IIndividualGroupService>();
			componentService.put(serviceName, this);

		} catch (Exception e) {
			throw new GroupsException(e);
		}
	}

	/**
	 * @param grouperEntityGroupStore
	 *            the group store for this web service.
	 */
	public GrouperWebService(GrouperEntityGroupStore grouperEntityGroupStore) {
		this();
		this.grouperEntityGroupStore = grouperEntityGroupStore;
	}

	GrouperEntityGroupStore getGrouperEntityGroupStore() {
		return grouperEntityGroupStore;
	}

	/**
	 * Find a Group.
	 * 
	 * @param key
	 *            The Full Qualified Name of the group to retrieve the group
	 *            (e:g: etc:wheel)
	 * @return The Group if found null otherwise.
	 * @throws GroupsException
	 *             if there is an error
	 * @see org.jasig.portal.groups.IEntityGroupStore#find(java.lang.String)
	 */
	public IEntityGroup find(String key) {

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("find portal key: " + key);
		}

		GcGetGroups getGroups = new GcGetGroups();
		getGroups.addSubjectIdentifier(key);
		WsGetGroupsResults results = null;
		try {
			results = getGroups.execute();
		} catch (Exception e) {
			LOGGER.error("portal key: " + key + " not found.\n"
					+ e.getMessage());
		}
		IEntityGroup group = null;
		if (results != null && results.getResults() != null
				&& results.getResults().length > 0) {
			WsSubject subject = results.getResults()[0].getWsSubject();
			group = new GrouperEntityGroupImpl(subject.getName(),
					IPerson.class, subject.getName(), "", this,
					IEntityGroup.class);
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("found group: " + group);
			}

		} else {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("portal key: " + key + " not found.\n");
			}
		}

		return group;
	}

	/**
	 * Returns the groups that contain the <code>IGroupMember</code>.
	 * 
	 * @param gm
	 *            IGroupMember
	 */
	@SuppressWarnings("unchecked")
	public Iterator findContainingGroups(IGroupMember gm) {

		GcGetGroups getGroups = new GcGetGroups();

		// the key will be the same for grouper groups and subjects.
		String id = null;
		if (gm instanceof GrouperEntityGroupImpl) {
			id = ((GrouperEntityGroupImpl) gm).getLocalKey();
		} else {
			id = gm.getKey();
		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("group member key: " + id);
		}

		getGroups.addSubjectIdentifier(id);

		WsGetGroupsResults results = null;
		try {
			results = getGroups.execute();
		} catch (Exception e) {
			LOGGER.error("could not get groups for key '" + id + "'.\n"
					+ e.getMessage());
		}

		final List<IEntityGroup> groups = new LinkedList<IEntityGroup>();

		if (results != null && results.getResults() != null) {
			for (WsGetGroupsResult wsg : results.getResults()) {
				if (wsg.getWsGroups() != null) {
					for (WsGroup g : wsg.getWsGroups()) {
						if (LOGGER.isDebugEnabled()) {
							LOGGER.debug("found group: " + g.getName());
						}
						groups.add(new GrouperEntityGroupImpl(g.getName(),
								IPerson.class, g.getName(), "", this,
								IEntityGroup.class));
					}
				}
			}
		} else {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("no groups for portal key: " + id);
			}
		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("final containing groups size: " + groups.size());
		}

		return groups.iterator();
	}

	/**
	 * Gives all the members of this group.
	 * 
	 * @param localKey
	 *            the grouper key to search
	 * 
	 * @return An iterator over all the groups or entities that are direct or
	 *         indirect members of this group.
	 * @throws GroupsException
	 *             if there is an error
	 * @see org.jasig.portal.groups.IGroupMember#getAllMembers()
	 */
	public Iterator<IGroupMember> findAllMembers(String localKey) {

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("findAllMembers for portal key: " + localKey);
		}

		// localKey should be the something like
		// uc:org:nsit:webservices:members.
		GcGetMembers getGroupsMembers = new GcGetMembers();
		getGroupsMembers.addGroupName(localKey);

		WsGetMembersResults results = null;
		try {
			results = getGroupsMembers.execute();
		} catch (Exception e) {
			LOGGER.error("could not get groups for key '" + localKey + "'.\n"
					+ e.getMessage());
		}

		WsSubject[] gInfos = null;
		if (results != null && results.getResults() != null) {
			for (WsGetMembersResult wsg : results.getResults()) {
				if (wsg.getWsSubjects() != null) {
					gInfos = wsg.getWsSubjects();
				}
			}
		} else {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("findAllMembers for portal key: " + localKey
						+ " not found.\n");
			}
		}

		if (gInfos != null) {
			final List<IGroupMember> members = new ArrayList<IGroupMember>(
					gInfos.length);
			for (WsSubject gInfo : gInfos) {

				// at the u of chicago
				// groups have UUID's for there id and people have shorter
				// chicago Id's withough '-' in them

				if (gInfo.getId() != null && !gInfo.getId().equals("")
						&& gInfo.getId().indexOf("-") == -1) {
					if (LOGGER.isDebugEnabled()) {
						LOGGER.debug("creating leaf member: " + gInfo.getId());
					}
					members.add(new GrouperEntityGroupImpl(gInfo.getId(),
							IPerson.class, (gInfo.getName() != null && !gInfo
									.getId().equals("")) ? gInfo.getId() : "",
							"", this, EntityTypes.LEAF_ENTITY_TYPE));
				}
				// attribute 4 is the group name for group subjects
				if (gInfo.getName() != null
						&& gInfo.getName().indexOf(":") > 0) {
					if (LOGGER.isDebugEnabled()) {
						LOGGER.debug("creating group member: "
								+ gInfo.getName());
					}
					members.add(new GrouperEntityGroupImpl(gInfo
							.getAttributeValue(4), IPerson.class, gInfo
							.getAttributeValue(4), "", this,
							EntityTypes.GROUP_ENTITY_TYPE));
				}

			}

			for (IGroupMember mem : members) {
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("found IGroupMember member: " + mem);
				}
			}

			return members.iterator();
		}
		return new ArrayList<IGroupMember>(0).iterator();
	}

	/**
	 * Iterator over the Collection of IEntities that are members of this
	 * IEntityGroup.
	 * 
	 * @param grouperKey
	 *            The considered group key in grouper.
	 * @return An iterator through the members (i.e. leaves, not subgroups) of
	 *         the group and its subgroups.
	 * @throws GroupsException
	 *             if there is an error.
	 * @see org.jasig.portal.groups.IEntityGroupStore#findEntitiesForGroup(org.jasig.portal.groups.IEntityGroup)
	 */
	public Iterator<IEntity> findEntitiesForGroup(String grouperKey) {

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("findEntitiesForGroup for portal key: " + grouperKey);
		}

		// localKey should be the something like
		// uc:org:nsit:webservices:members.
		GcGetMembers getGroupsMembers = new GcGetMembers();
		getGroupsMembers.addGroupName(grouperKey);

		WsGetMembersResults results = null;
		try {
			results = getGroupsMembers.execute();
		} catch (Exception e) {
			LOGGER.error("could not get groups for key '" + grouperKey + "'.\n"
					+ e.getMessage());
		}

		WsSubject[] gInfos = null;
		if (results != null && results.getResults() != null) {
			for (WsGetMembersResult wsg : results.getResults()) {
				if (wsg.getWsSubjects() != null) {
					gInfos = wsg.getWsSubjects();
				}
			}
		} else {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("findEntitiesForGroup for portal key: "
						+ grouperKey + " not found.");
			}
		}

		if (gInfos != null) {
			final List<IEntity> members = new ArrayList<IEntity>(gInfos.length);
			for (WsSubject gInfo : gInfos) {// attribute value 0 is the cnetid
				if (gInfo.getId() != null && gInfo.getId().equals("")) {
					members.add(new EntityImpl(gInfo.getId(),
							EntityTypes.LEAF_ENTITY_TYPE));
				}
			}

			for (IEntity mem : members) {
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("found IEntity member: " + mem);
				}
			}

			return members.iterator();
		}
		return new ArrayList<IEntity>(0).iterator();
	}

	/**
	 * Returns an <code>Iterator</code> over the members of <code>group</code>.
	 * 
	 * @param group
	 *            IEntityGroup
	 */
	@SuppressWarnings("unchecked")
	public Iterator findMembers(IEntityGroup group) {

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("findMemberGroups for portal key: "
					+ group.getLocalKey());
		}

		GcGetGroups getGroups = new GcGetGroups();
		getGroups.addSubjectIdentifier(group.getLocalKey());

		List<String> groupsInfos = new LinkedList<String>();

		WsGetGroupsResults results = null;
		try {
			results = getGroups.execute();
		} catch (Exception e) {
			LOGGER.error("could not get groups for key '" + group.getLocalKey()
					+ "'.\n" + e.getMessage());
		}

		if (results != null && results.getResults() != null) {
			for (WsGetGroupsResult wsg : results.getResults()) {
				if (wsg.getWsGroups() != null) {
					for (WsGroup g : wsg.getWsGroups()) {
						groupsInfos.add(g.getName());
					}
				}
			}
		} else {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("findMemberGroups for portal key: "
						+ group.getLocalKey() + " not found.");
			}
			return new ArrayList<IEntityGroup>(0).iterator();
		}

		final List<IEntityGroup> members = new ArrayList<IEntityGroup>(
				groupsInfos.size());
		for (String groupName : groupsInfos) {
			members.add(new GrouperEntityGroupImpl(groupName,
					EntityTypes.GROUP_ENTITY_TYPE, groupName, "", this,
					IEntityGroup.class));
		}

		for (IEntityGroup mem : members) {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("found IEntityGroup member: " + mem);
			}
		}

		return members.iterator();
	}

	/**
	 * Find EntityIdentifiers for groups whose name matches the query string
	 * according to the specified method and matches the provided leaf type
	 */
	@SuppressWarnings("unchecked")
	public EntityIdentifier[] searchForGroups(String query, int method,
			Class leaftype) {

		// To work around extra web service calls for the PAGS groups at login
		// (and possibly other times),
		// add a test to skip calling the "All Users" key and anything that
		// starts with
		// "UofC " which are all of our PAGS groups.
		// this is a short circuit call.

		String[] patterns = null;
		try {
			Properties props = new Properties();
			props.load(this.getClass().getResourceAsStream(
					"/grouper.lib.properties"));
			if (props.getProperty("version") != null) {
				String patternGroup = props.getProperty("patterns");
				if (patternGroup != null && !patternGroup.equals("")) {
					patterns = patternGroup.split(";");
				} else {
					patterns = new String[0];
				}
			}
			if (patterns != null) {
				for (String pattern : patterns) {
					if (query.startsWith(pattern)) {
						return new EntityIdentifier[0];
					}
				}
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("searchForGroups for: " + query);
		}

		GcFindGroups groups = new GcFindGroups();
		WsQueryFilter filter = new WsQueryFilter();
		filter.setQueryFilterType("FIND_BY_GROUP_NAME_APPROXIMATE");
		filter.setGroupName(query);
		groups.assignQueryFilter(filter);

		WsFindGroupsResults results = null;

		try {
			results = groups.execute();
		} catch (Exception e) {
			LOGGER.error("could not get groups for query '" + query + "'.\n"
					+ e.getMessage());
		}

		EntityIdentifier[] ids = null;
		if (results != null && results.getGroupResults() != null) {
			WsGroup[] wsResults = results.getGroupResults();
			ids = new EntityIdentifier[wsResults.length];
			int index = 0;
			for (WsGroup g : wsResults) {
				ids[index++] = new EntityIdentifier(g.getName(),
						IEntityGroup.class);
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("found result: " + ids[index - 1]);
				}
			}
		} else {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("searchForGroups for portal query: " + query
						+ " not found.");
			}
		}

		if (ids == null) {
			ids = new EntityIdentifier[0];
		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("total number of results: " + ids.length);
		}

		return ids;
	}

	/**
	 * Find EntityIdentifiers for groups whose name matches the query string
	 * according to the specified method, has the provided leaf type and
	 * descends from the specified group
	 */
	@SuppressWarnings("unchecked")
	public EntityIdentifier[] searchForGroups(String query, int method,
			Class leaftype, IEntityGroup ancestor) throws GroupsException {
		return searchForGroups(query, method, leaftype);
	}

	/**
	 * @param localKey
	 *            the key in grouper
	 * @param key
	 *            the key in the portal
	 * @return true if this group exists in grouper, false otherwise.
	 */
	public boolean deepContains(String localKey, String key) {
		Iterator<IGroupMember> members = findAllMembers(localKey);
		if (members != null) {
			while (members.hasNext()) {
				IGroupMember local = members.next();
				if (local.getKey().equals(key)) {
					return true;
				}
			}
		}
		return false;
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
		return deepContains(group.getLocalKey(), member.getKey());
	}

	/**
	 * Removes the <code>IEntityGroup</code> from the store.
	 */
	public void deleteGroup(IEntityGroup group) throws GroupsException {
		throw new GroupsException("deleteGroup currently not supported.");
	}

	/**
	 * Returns a preexisting <code>IEntityGroup</code> from the store.
	 * 
	 * @param ent
	 *            CompositeEntityIdentifier
	 */
	public IEntityGroup findGroup(CompositeEntityIdentifier ent)
			throws GroupsException {
		return find(ent.getLocalKey());
	}

	/**
	 * Answers if the service can be updated by the portal.
	 */
	public boolean isEditable() {
		return false;
	}

	/**
	 * Answers if the group can be updated or deleted in the store.
	 * 
	 * @param group
	 *            IEntityGroup
	 */
	public boolean isEditable(IEntityGroup group) throws GroupsException {
		return false;
	}

	/**
	 * Returns a new <code>IEntityGroup</code> for the given Class with an
	 * unused key.
	 */
	@SuppressWarnings("unchecked")
	public IEntityGroup newGroup(Class type) throws GroupsException {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("creating newGroup of type: " + type);
		}
		return new GrouperEntityGroupImpl("", type, "", "", this,
				EntityTypes.GROUP_ENTITY_TYPE);

	}

	/**
	 * Commits the updated <code>IEntityGroup</code> and its memberships to the
	 * store.
	 * 
	 * @param group
	 *            IEntityGroup
	 */
	public void updateGroup(IEntityGroup group) throws GroupsException {
		throw new GroupsException("updateGroup currently not supported.");
	}

	/**
	 * Commits the updated group memberships to the store.
	 * 
	 * @param group
	 *            IEntityGroup
	 */
	public void updateGroupMembers(IEntityGroup group) throws GroupsException {
		throw new GroupsException("updateGroupMembers currently not supported.");
	}

	/**
	 * Returns a pre-existing <code>IEntityGroup</code> or null if it does not
	 * exist.
	 */
	public IEntityGroup findGroup(String key) throws GroupsException {
		return find(key);
	}

	/**
	 * Returns a pre-existing <code>IEntityGroup</code> or null if it does not
	 * exist.
	 */
	public ILockableEntityGroup findGroupWithLock(String key, String owner)
			throws GroupsException {
		throw new GroupsException("findGroupWithLock currently not supported.");
	}

	/**
	 * Returns an <code>IEntity</code> representing a portal entity. This does
	 * not guarantee that the entity actually exists.
	 */
	@SuppressWarnings("unchecked")
	public IEntity getEntity(String key, Class type) throws GroupsException {
		throw new GroupsException("getEntity currently not supported.");
	}

	/**
	 * Returns an <code>IEntity</code> representing a portal entity. This does
	 * not guarantee that the entity actually exists.
	 */
	@SuppressWarnings("unchecked")
	public IEntity getEntity(String key, Class type, String service)
			throws GroupsException {
		throw new GroupsException("getEntity currently not supported.");
	}

	/**
	 * Returns an <code>IGroupMember</code> representing either a group or a
	 * portal entity, based on the <code>EntityIdentifier</code>, which refers
	 * to the UNDERLYING entity for the <code>IGroupMember</code>.
	 */
	public IGroupMember getGroupMember(
			EntityIdentifier underlyingEntityIdentifier) throws GroupsException {
		return (underlyingEntityIdentifier.getType()
				.equals(EntityTypes.GROUP_ENTITY_TYPE)) ? find(underlyingEntityIdentifier
				.getKey())
				: null;
	}

	/**
	 * Returns an <code>IGroupMember</code> representing either a group or a
	 * portal entity. If the parm <code>type</code> is the group type, the
	 * <code>IGroupMember</code> is an <code>IEntityGroup</code>. Otherwise it
	 * is an <code>IEntity</code>.
	 */
	@SuppressWarnings("unchecked")
	public IGroupMember getGroupMember(String key, Class type)
			throws GroupsException {
		return (type.equals(EntityTypes.GROUP_ENTITY_TYPE)) ? find(key) : null;
	}

	/**
	 * Returns a new <code>IEntityGroup</code> for the given Class with an
	 * unused key from the named service.
	 */
	@SuppressWarnings("unchecked")
	public IEntityGroup newGroup(Class type, Name serviceName)
			throws GroupsException {
		if (serviceName.equals(this.serviceName)) {
			return newGroup(type);
		}
		return null;
	}

	/**
	 * Find EntityIdentifiers for entities whose name matches the query string
	 * according to the specified method and is of the specified type
	 */
	@SuppressWarnings("unchecked")
	public EntityIdentifier[] searchForEntities(String query, int method,
			Class type) throws GroupsException {
		if (LOGGER.isWarnEnabled()) {
			LOGGER.warn("searchForEntities currently not supported.");
		}
		return new EntityIdentifier[0];
	}

	/**
	 * Find EntityIdentifiers for entities whose name matches the query string
	 * according to the specified method, is of the specified type and descends
	 * from the specified group
	 */
	@SuppressWarnings("unchecked")
	public EntityIdentifier[] searchForEntities(String query, int method,
			Class type, IEntityGroup ancestor) throws GroupsException {
		return searchForEntities(query, method, type);
	}

	/**
	 * Returns a <code>Map</code> of the services contained by this component,
	 * keyed on the name of the service WITHIN THIS COMPONENT.
	 */
	public Map<Name, IIndividualGroupService> getComponentServices() {
		return componentService;
	}

	/**
	 * Returns the FULLY-QUALIFIED <code>Name</code> of the service, which may
	 * not be known until the composite service is assembled.
	 */
	public Name getServiceName() {
		return serviceName;
	}

	/**
	 * Sets the name of the service to the new value.
	 */
	public void setServiceName(Name serviceName) {
		this.serviceName = serviceName;
	}

	/**
	 * Answers if this service is a leaf in the composite; a service that
	 * actually operates on groups. this will be false until grouper subjects
	 * are supported (I think).
	 */
	public boolean isLeafService() {
		return false;
	}
}
