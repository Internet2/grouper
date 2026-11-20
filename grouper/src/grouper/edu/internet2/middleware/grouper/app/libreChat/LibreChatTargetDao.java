package edu.internet2.middleware.grouper.app.libreChat;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningLists;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningEntity;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningGroup;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningMembership;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningObjectChange;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningUpdatable;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.GrouperProvisionerDaoCapabilities;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.GrouperProvisionerTargetDaoBase;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoDeleteGroupRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoDeleteGroupResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoDeleteMembershipsRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoDeleteMembershipsResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoInsertGroupRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoInsertGroupResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoInsertMembershipsRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoInsertMembershipsResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveAllDataRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveAllDataResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveEntityRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveEntityResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveGroupRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveGroupResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveMembershipsByGroupRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveMembershipsByGroupResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoTimingInfo;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoUpdateGroupRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoUpdateGroupResponse;
import edu.internet2.middleware.grouper.util.GrouperHttpClient;
import edu.internet2.middleware.grouper.util.GrouperHttpClientLog;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * LibreChat target DAO.
 *
 * <ul>
 *   <li>Groups: retrieve, insert, update (name, description), delete.  Only groups with source local and
 *       an idOnTheSource starting with the configured prefix are ever read or changed; every other group
 *       (made by hand, by an admin panel, by Entra sync) is invisible to Grouper.  Deleting a group in
 *       LibreChat also deletes its sharing grants, so a setting can empty the group instead.</li>
 *   <li>Entities (users): retrieve only.  LibreChat creates users at first login.</li>
 *   <li>Memberships: insert and delete, one call per member (the API has no bulk replace).</li>
 * </ul>
 *
 * GRP-7594
 */
public class LibreChatTargetDao extends GrouperProvisionerTargetDaoBase {

  @Override
  public boolean loggingStart() {
    return GrouperHttpClient.logStart(new GrouperHttpClientLog());
  }

  @Override
  public String loggingStop() {
    return GrouperHttpClient.logEnd();
  }

  /**
   * @return the provisioner configuration
   */
  private LibreChatProvisionerConfiguration getLibreChatConfiguration() {
    return (LibreChatProvisionerConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
  }

  // ============================
  // Sync-back (GRP-7595): cached objects get the same rules as a live read
  // ============================

  /**
   * ids of the managed groups this run treats as present, from the live read or from the cache.
   * Memberships are only kept for these groups.
   */
  private Set<String> managedGroupIds = new HashSet<String>();

  /**
   * Cached groups: keep only managed ones (source local, idOnTheSource with the prefix, valid id), and
   * remove the prefix from idOnTheSource, exactly as a live read does
   */
  @Override
  public List<ProvisioningGroup> filterTargetGroupsFromSyncBack(List<ProvisioningGroup> targetGroups) {
    String prefix = getLibreChatConfiguration().getLibreChatGroupIdOnTheSourcePrefix();
    List<ProvisioningGroup> result = new ArrayList<ProvisioningGroup>();
    for (ProvisioningGroup targetGroup : GrouperUtil.nonNull(targetGroups)) {
      if (targetGroup == null) {
        continue;
      }
      LibreChatGroup cached = new LibreChatGroup();
      cached.setId(targetGroup.getId());
      cached.setSource(targetGroup.retrieveAttributeValueString("source"));
      cached.setIdOnTheSource(targetGroup.retrieveAttributeValueString("idOnTheSource"));
      if (!cached.isManaged(prefix)) {
        continue;
      }
      targetGroup.assignAttributeValue("idOnTheSource", StringUtils.removeStart(cached.getIdOnTheSource(), prefix));
      result.add(targetGroup);
      this.managedGroupIds.add(targetGroup.getId());
    }
    return result;
  }

  /**
   * Cached memberships: keep only those of managed groups, like a live read
   */
  @Override
  public List<ProvisioningMembership> filterTargetMembershipsFromSyncBack(List<ProvisioningMembership> targetMemberships) {
    List<ProvisioningMembership> result = new ArrayList<ProvisioningMembership>();
    for (ProvisioningMembership targetMembership : GrouperUtil.nonNull(targetMemberships)) {
      if (targetMembership != null && !StringUtils.isBlank(targetMembership.getProvisioningEntityId())
          && this.managedGroupIds.contains(targetMembership.getProvisioningGroupId())) {
        result.add(targetMembership);
      }
    }
    return result;
  }

  // ============================
  // Retrieve all data: users, managed groups, and each managed group's members
  // ============================

  @Override
  public TargetDaoRetrieveAllDataResponse retrieveAllData(TargetDaoRetrieveAllDataRequest targetDaoRetrieveAllDataRequest) {

    long startNanos = System.nanoTime();

    TargetDaoRetrieveAllDataResponse response = new TargetDaoRetrieveAllDataResponse();
    GrouperProvisioningLists targetData = new GrouperProvisioningLists();
    response.setTargetData(targetData);

    try {
      LibreChatProvisionerConfiguration config = getLibreChatConfiguration();
      String configId = config.getLibreChatExternalSystemConfigId();
      String prefix = config.getLibreChatGroupIdOnTheSourcePrefix();

      // GRP-7595: when a full sync is served from the sync-back cache the framework asks to skip some reads
      boolean retrieveEntities = targetDaoRetrieveAllDataRequest == null || targetDaoRetrieveAllDataRequest.isRetrieveEntities();
      boolean retrieveGroups = targetDaoRetrieveAllDataRequest == null || targetDaoRetrieveAllDataRequest.isRetrieveGroups();
      boolean retrieveMemberships = targetDaoRetrieveAllDataRequest == null || targetDaoRetrieveAllDataRequest.isRetrieveMemberships();

      // a fresh view for this run, filled by the live group read below or by filterTargetGroupsFromSyncBack
      this.managedGroupIds.clear();

      List<ProvisioningEntity> provisioningEntities = new ArrayList<ProvisioningEntity>();
      if (retrieveEntities) {
        // counter so tests and diagnostics can see whether the user pull ran
        GrouperUtil.mapAddValue(this.getGrouperProvisioner().getDebugMap(), "libreChatRetrieveAllUsersApiCall", 1);
        for (LibreChatUser user : LibreChatApiCommands.retrieveUsers(configId)) {
          provisioningEntities.add(user.toProvisioningEntity());
        }
      }
      targetData.setProvisioningEntities(provisioningEntities);

      List<ProvisioningGroup> provisioningGroups = new ArrayList<ProvisioningGroup>();
      List<ProvisioningMembership> provisioningMemberships = new ArrayList<ProvisioningMembership>();

      // the managed group list is needed for groups, and to know whose members to read
      List<LibreChatGroup> managedGroups = new ArrayList<LibreChatGroup>();
      if (retrieveGroups || retrieveMemberships) {
        GrouperUtil.mapAddValue(this.getGrouperProvisioner().getDebugMap(), "libreChatRetrieveAllGroupsApiCall", 1);
        managedGroups = LibreChatApiCommands.retrieveManagedGroups(configId, prefix);
        for (LibreChatGroup group : managedGroups) {
          this.managedGroupIds.add(group.getId());
        }
      }
      if (retrieveGroups) {
        for (LibreChatGroup group : managedGroups) {
          provisioningGroups.add(group.toProvisioningGroup(prefix));
        }
      }
      if (retrieveMemberships) {
        // one members call per managed group; this is what fullSyncMembershipsFromSyncBack saves
        GrouperUtil.mapAddValue(this.getGrouperProvisioner().getDebugMap(), "libreChatRetrieveMembershipsApiCall", 1);
        for (LibreChatGroup group : managedGroups) {
          for (String userId : LibreChatApiCommands.retrieveGroupMemberIds(configId, group.getId())) {
            ProvisioningMembership membership = new ProvisioningMembership(false);
            membership.setProvisioningGroupId(group.getId());
            membership.setProvisioningEntityId(userId);
            provisioningMemberships.add(membership);
          }
        }
      }
      targetData.setProvisioningGroups(provisioningGroups);
      targetData.setProvisioningMemberships(provisioningMemberships);

      return response;
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("retrieveAllData", startNanos));
    }
  }

  // ============================
  // Retrieve the members of one managed group.  Declaring this (canRetrieveMembershipsAllByGroup) is what
  // makes memberships "selected in general", which full sync of memberships from sync-back requires (GRP-7595)
  // ============================

  @Override
  public TargetDaoRetrieveMembershipsByGroupResponse retrieveMembershipsByGroup(
      TargetDaoRetrieveMembershipsByGroupRequest targetDaoRetrieveMembershipsByGroupRequest) {

    long startNanos = System.nanoTime();

    try {
      List<ProvisioningMembership> result = new ArrayList<ProvisioningMembership>();
      ProvisioningGroup targetGroup = targetDaoRetrieveMembershipsByGroupRequest.getTargetGroup();
      String groupId = targetGroup == null ? null : targetGroup.getId();
      if (StringUtils.isBlank(groupId)) {
        return new TargetDaoRetrieveMembershipsByGroupResponse(result);
      }

      LibreChatProvisionerConfiguration config = getLibreChatConfiguration();
      String configId = config.getLibreChatExternalSystemConfigId();

      // only managed groups are visible, the same as retrieveAllData
      LibreChatGroup current = LibreChatApiCommands.retrieveGroup(configId, groupId);
      if (current == null || !current.isManaged(config.getLibreChatGroupIdOnTheSourcePrefix())) {
        return new TargetDaoRetrieveMembershipsByGroupResponse(result);
      }

      // the member read captures the full member list for sync-back in LibreChatApiCommands
      for (String userId : LibreChatApiCommands.retrieveGroupMemberIds(configId, groupId)) {
        ProvisioningMembership membership = new ProvisioningMembership(false);
        membership.setProvisioningGroupId(groupId);
        membership.setProvisioningEntityId(userId);
        result.add(membership);
      }
      return new TargetDaoRetrieveMembershipsByGroupResponse(result);
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("retrieveMembershipsByGroup", startNanos));
    }
  }

  // ============================
  // Retrieve one user: by username (the match key) or by id
  // ============================

  @Override
  public TargetDaoRetrieveEntityResponse retrieveEntity(TargetDaoRetrieveEntityRequest targetDaoRetrieveEntityRequest) {

    long startNanos = System.nanoTime();

    try {
      String configId = getLibreChatConfiguration().getLibreChatExternalSystemConfigId();
      String searchAttribute = targetDaoRetrieveEntityRequest.getSearchAttribute();
      String searchValue = GrouperUtil.stringValue(targetDaoRetrieveEntityRequest.getSearchAttributeValue());

      LibreChatUser user = null;
      if (StringUtils.equals("id", searchAttribute)) {
        user = LibreChatApiCommands.retrieveUserById(configId, searchValue);
      } else if (StringUtils.equals("username", searchAttribute)) {
        user = LibreChatApiCommands.retrieveUserByUsername(configId, searchValue);
      } else {
        throw new RuntimeException("LibreChat users can be looked up by 'username' or 'id', not '" + searchAttribute + "'");
      }

      return new TargetDaoRetrieveEntityResponse(user == null ? null : user.toProvisioningEntity());
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("retrieveEntity", startNanos));
    }
  }

  // ============================
  // Retrieve one managed group: by id, idOnTheSource, or name
  // ============================

  @Override
  public TargetDaoRetrieveGroupResponse retrieveGroup(TargetDaoRetrieveGroupRequest targetDaoRetrieveGroupRequest) {

    long startNanos = System.nanoTime();

    try {
      LibreChatProvisionerConfiguration config = getLibreChatConfiguration();
      String configId = config.getLibreChatExternalSystemConfigId();
      String prefix = config.getLibreChatGroupIdOnTheSourcePrefix();
      String searchAttribute = targetDaoRetrieveGroupRequest.getSearchAttribute();
      String searchValue = GrouperUtil.stringValue(targetDaoRetrieveGroupRequest.getSearchAttributeValue());

      LibreChatGroup group = null;
      if (StringUtils.equals("id", searchAttribute)) {
        group = LibreChatApiCommands.retrieveGroup(configId, searchValue);
        // a group this provisioner does not manage is not found
        if (group != null && !group.isManaged(prefix)) {
          group = null;
        }
      } else if (StringUtils.equals("idOnTheSource", searchAttribute) || StringUtils.equals("name", searchAttribute)) {
        for (LibreChatGroup candidate : LibreChatApiCommands.retrieveManagedGroups(configId, prefix)) {
          String candidateValue = StringUtils.equals("name", searchAttribute) ? candidate.getName()
              : StringUtils.removeStart(candidate.getIdOnTheSource(), prefix);
          if (StringUtils.equals(searchValue, candidateValue)) {
            group = candidate;
            break;
          }
        }
      } else {
        throw new RuntimeException("LibreChat groups can be looked up by 'id', 'idOnTheSource' or 'name', not '" + searchAttribute + "'");
      }

      return new TargetDaoRetrieveGroupResponse(group == null ? null : group.toProvisioningGroup(prefix));
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("retrieveGroup", startNanos));
    }
  }

  // ============================
  // Insert group: source local, idOnTheSource = prefix + value
  // ============================

  @Override
  public TargetDaoInsertGroupResponse insertGroup(TargetDaoInsertGroupRequest targetDaoInsertGroupRequest) {

    long startNanos = System.nanoTime();
    ProvisioningGroup targetGroup = targetDaoInsertGroupRequest.getTargetGroup();

    try {
      LibreChatProvisionerConfiguration config = getLibreChatConfiguration();
      LibreChatGroup group = LibreChatGroup.fromProvisioningGroup(targetGroup, config.getLibreChatGroupIdOnTheSourcePrefix());
      if (StringUtils.isBlank(group.getName())) {
        throw new RuntimeException("LibreChat group 'name' is required");
      }
      // without idOnTheSource the group could never be found again, and could be confused with others
      if (StringUtils.isBlank(group.getIdOnTheSource())) {
        throw new RuntimeException("LibreChat group 'idOnTheSource' is required (e.g. the Grouper group uuid)");
      }
      LibreChatGroup created = LibreChatApiCommands.createGroup(config.getLibreChatExternalSystemConfigId(), group);
      targetGroup.setId(created.getId());
      // sync-back: the created group is known, no re-read needed (GRP-7595)
      LibreChatProvisioningTargetNativeSync.captureGroupWriteFromCurrentProvisioner(created.getId(), created);
      markProvisioned(targetGroup, true);
      return new TargetDaoInsertGroupResponse();
    } catch (RuntimeException e) {
      markProvisioned(targetGroup, false);
      throw e;
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("insertGroup", startNanos));
    }
  }

  // ============================
  // Update group: name and description only
  // ============================

  @Override
  public TargetDaoUpdateGroupResponse updateGroup(TargetDaoUpdateGroupRequest targetDaoUpdateGroupRequest) {

    long startNanos = System.nanoTime();
    ProvisioningGroup targetGroup = targetDaoUpdateGroupRequest.getTargetGroup();

    try {
      LibreChatProvisionerConfiguration config = getLibreChatConfiguration();
      // the id may come from the sync table cache, so make sure the group is still one this provisioner manages
      requireManagedGroup(config, targetGroup.getId());
      Map<String, String> fieldToValue = new LinkedHashMap<String, String>();
      for (ProvisioningObjectChange change : GrouperUtil.nonNull(targetGroup.getInternal_objectChanges())) {
        String attributeName = change.getAttributeName();
        if (StringUtils.equals("name", attributeName) || StringUtils.equals("description", attributeName)) {
          fieldToValue.put(attributeName, targetGroup.retrieveAttributeValueString(attributeName));
        }
      }
      LibreChatApiCommands.updateGroup(config.getLibreChatExternalSystemConfigId(), targetGroup.getId(), fieldToValue);
      // sync-back: the end-of-run drain re-reads the updated group (GRP-7595)
      LibreChatProvisioningTargetNativeSync.captureGroupWriteFromCurrentProvisioner(targetGroup.getId(), null);
      markProvisioned(targetGroup, true);
      return new TargetDaoUpdateGroupResponse();
    } catch (RuntimeException e) {
      markProvisioned(targetGroup, false);
      throw e;
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("updateGroup", startNanos));
    }
  }

  // ============================
  // Delete group (or empty it), only if this provisioner manages it
  // ============================

  @Override
  public TargetDaoDeleteGroupResponse deleteGroup(TargetDaoDeleteGroupRequest targetDaoDeleteGroupRequest) {

    long startNanos = System.nanoTime();
    ProvisioningGroup targetGroup = targetDaoDeleteGroupRequest.getTargetGroup();

    try {
      LibreChatProvisionerConfiguration config = getLibreChatConfiguration();
      String configId = config.getLibreChatExternalSystemConfigId();

      // re-read the group so a group this provisioner does not manage is never deleted or emptied
      LibreChatGroup current = LibreChatApiCommands.retrieveGroup(configId, targetGroup.getId());
      if (current != null) {
        if (!current.isManaged(config.getLibreChatGroupIdOnTheSourcePrefix())) {
          throw new RuntimeException("Will not delete LibreChat group '" + current.getName() + "' ("
              + current.getId() + "): it is not managed by this provisioner (idOnTheSource prefix '"
              + config.getLibreChatGroupIdOnTheSourcePrefix() + "')");
        }
        if (config.isLibreChatEmptyGroupsInsteadOfDelete()) {
          // keeps the group and the agents shared with it, but nobody gets access through it
          for (String userId : LibreChatApiCommands.retrieveGroupMemberIds(configId, current.getId())) {
            LibreChatApiCommands.removeGroupMember(configId, current.getId(), userId);
            LibreChatProvisioningTargetNativeSync.captureMembershipDeleteFromCurrentProvisioner(current.getId(), userId);
          }
        } else {
          LibreChatApiCommands.deleteGroup(configId, current.getId());
          // sync-back: drop the group from the mirror (GRP-7595)
          LibreChatProvisioningTargetNativeSync.captureGroupDeleteFromCurrentProvisioner(current.getId());
        }
      }
      markProvisioned(targetGroup, true);
      return new TargetDaoDeleteGroupResponse();
    } catch (RuntimeException e) {
      markProvisioned(targetGroup, false);
      throw e;
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("deleteGroup", startNanos));
    }
  }

  // ============================
  // Memberships: one call per member
  // ============================

  @Override
  public TargetDaoInsertMembershipsResponse insertMemberships(TargetDaoInsertMembershipsRequest targetDaoInsertMembershipsRequest) {
    long startNanos = System.nanoTime();
    try {
      changeMemberships(targetDaoInsertMembershipsRequest.getTargetMemberships(), true);
      return new TargetDaoInsertMembershipsResponse();
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("insertMemberships", startNanos));
    }
  }

  @Override
  public TargetDaoDeleteMembershipsResponse deleteMemberships(TargetDaoDeleteMembershipsRequest targetDaoDeleteMembershipsRequest) {
    long startNanos = System.nanoTime();
    try {
      changeMemberships(targetDaoDeleteMembershipsRequest.getTargetMemberships(), false);
      return new TargetDaoDeleteMembershipsResponse();
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("deleteMemberships", startNanos));
    }
  }

  /**
   * Add or remove each membership with its own call.  A failure is recorded on that membership and the
   * rest still run.
   * @param targetMemberships memberships
   * @param add true to add, false to remove
   */
  private void changeMemberships(List<ProvisioningMembership> targetMemberships, boolean add) {
    LibreChatProvisionerConfiguration config = getLibreChatConfiguration();
    String configId = config.getLibreChatExternalSystemConfigId();

    // group ids may come from the sync table cache, so check each group is still managed, once per call
    Set<String> managedGroupIds = new HashSet<String>();

    for (ProvisioningMembership membership : GrouperUtil.nonNull(targetMemberships)) {
      try {
        String groupId = membership.getProvisioningGroupId();
        String userId = membership.getProvisioningEntityId();
        if (!managedGroupIds.contains(groupId)) {
          requireManagedGroup(config, groupId);
          managedGroupIds.add(groupId);
        }
        if (add) {
          LibreChatApiCommands.addGroupMember(configId, groupId, userId);
          LibreChatProvisioningTargetNativeSync.captureMembershipInsertFromCurrentProvisioner(groupId, userId);
        } else {
          LibreChatApiCommands.removeGroupMember(configId, groupId, userId);
          LibreChatProvisioningTargetNativeSync.captureMembershipDeleteFromCurrentProvisioner(groupId, userId);
        }
        markProvisioned(membership, true);
      } catch (RuntimeException e) {
        markProvisioned(membership, false);
        membership.setException(e);
      }
    }
  }

  /**
   * Throw unless the LibreChat group exists and is managed by this provisioner (source local, idOnTheSource
   * with the prefix).  Guards writes whose group id came from the sync table cache: if a LibreChat admin
   * changed the group's idOnTheSource or source, it is no longer Grouper's and must not be renamed or have
   * members changed.
   * @param config provisioner config
   * @param groupId LibreChat group id
   */
  private static void requireManagedGroup(LibreChatProvisionerConfiguration config, String groupId) {
    LibreChatGroup current = LibreChatApiCommands.retrieveGroup(config.getLibreChatExternalSystemConfigId(), groupId);
    if (current == null) {
      throw new RuntimeException("LibreChat group '" + groupId + "' not found");
    }
    if (!current.isManaged(config.getLibreChatGroupIdOnTheSourcePrefix())) {
      throw new RuntimeException("Will not change LibreChat group '" + current.getName() + "' (" + groupId
          + "): it is not managed by this provisioner (idOnTheSource prefix '"
          + config.getLibreChatGroupIdOnTheSourcePrefix() + "')");
    }
  }

  /**
   * Mark a provisioning object and its object changes as provisioned or not
   * @param provisioningObject group or membership
   * @param provisioned true if the write worked
   */
  private static void markProvisioned(ProvisioningUpdatable provisioningObject, boolean provisioned) {
    if (provisioningObject == null) {
      return;
    }
    provisioningObject.setProvisioned(provisioned);
    for (ProvisioningObjectChange change : GrouperUtil.nonNull(provisioningObject.getInternal_objectChanges())) {
      change.setProvisioned(provisioned);
    }
  }

  @Override
  public void registerGrouperProvisionerDaoCapabilities(GrouperProvisionerDaoCapabilities grouperProvisionerDaoCapabilities) {
    grouperProvisionerDaoCapabilities.setCanRetrieveAllData(true);
    grouperProvisionerDaoCapabilities.setCanRetrieveGroup(true);
    grouperProvisionerDaoCapabilities.setCanRetrieveEntity(true);
    // members of one managed group (GRP-7595: needed so memberships can be served from sync-back)
    grouperProvisionerDaoCapabilities.setCanRetrieveMembershipsAllByGroup(true);

    // groups: full CRUD (only groups with the idOnTheSource prefix)
    grouperProvisionerDaoCapabilities.setCanInsertGroup(true);
    grouperProvisionerDaoCapabilities.setCanUpdateGroup(true);
    grouperProvisionerDaoCapabilities.setCanDeleteGroup(true);

    // users are read only (LibreChat creates them at first login)

    // memberships: add and remove one at a time
    grouperProvisionerDaoCapabilities.setCanInsertMemberships(true);
    grouperProvisionerDaoCapabilities.setCanDeleteMemberships(true);

    // sync-back (GRP-7595): reads are captured in LibreChatApiCommands, writes here.  Groups and memberships
    // can be served from the cache.  Users cannot: LibreChat creates them at first login, outside Grouper,
    // so a cached user list would miss new users (and the user list is a single paged read anyway)
    grouperProvisionerDaoCapabilities.setCanSyncBack(true);
    grouperProvisionerDaoCapabilities.setCanFullSyncGroupsFromSyncBack(true);
    grouperProvisionerDaoCapabilities.setCanFullSyncMembershipsFromSyncBack(true);
  }

}
