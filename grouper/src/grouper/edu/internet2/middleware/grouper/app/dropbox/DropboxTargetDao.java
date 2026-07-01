package edu.internet2.middleware.grouper.app.dropbox;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningEntity;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningGroup;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningMembership;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningObjectChange;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.GrouperProvisionerDaoCapabilities;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.GrouperProvisionerTargetDaoBase;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoDeleteEntityRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoDeleteEntityResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoDeleteGroupRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoDeleteGroupResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoDeleteMembershipsRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoDeleteMembershipsResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoInsertEntityRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoInsertEntityResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoInsertGroupRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoInsertGroupResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoInsertMembershipsRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoInsertMembershipsResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveAllEntitiesRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveAllEntitiesResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveAllGroupsRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveAllGroupsResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveAllMembershipsRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveAllMembershipsResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveEntityRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveEntityResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveGroupRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveGroupResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveMembershipsByGroupRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveMembershipsByGroupResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoTimingInfo;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoUpdateEntityRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoUpdateEntityResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoUpdateGroupRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoUpdateGroupResponse;
import edu.internet2.middleware.grouper.util.GrouperHttpClient;
import edu.internet2.middleware.grouper.util.GrouperHttpClientLog;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * Dropbox TargetDao -- manages Dropbox Business team groups, team members (entities), and group
 * memberships through the Dropbox Team API (via {@link DropboxApiCommands}).
 *
 * <p><b>Object model.</b> This DAO uses the "membership objects" model (like the Remedy DAO), not
 * the group-centric {@code retrieveAllData} model TrueFoundry uses. Groups, entities, and
 * memberships are retrieved through separate calls -- {@link #retrieveAllGroups},
 * {@link #retrieveAllEntities}, and {@link #retrieveMembershipsByGroup} (with
 * {@link #retrieveAllMemberships} looping all groups). Memberships are first-class objects whose
 * matching id is the native {@code group_id} + {@code team_member_id} pair: a Dropbox membership has
 * no id of its own. Plumbing/style (logging, timing in finally blocks, per-object
 * {@code setProvisioned}) mirrors {@code TrueFoundryTargetDao}.</p>
 *
 * <p><b>Identity.</b> The provisioning group id is the native Dropbox {@code group_id} (e.g.
 * "g:abc123"); the provisioning entity id is the native {@code team_member_id} (e.g. "dbmid:abc").
 * Matching against Grouper is configured on the {@code externalId} attribute of each. Memberships
 * therefore reference their group and entity purely by these native ids.</p>
 *
 * <p><b>Admin roles.</b> Dropbox admin roles are an optional overlay. The translator
 * ({@link DropboxProvisioningTranslator}) removes admin-role-folder groups from the set of target
 * groups (so they are never created as Dropbox groups) and instead stamps each entity with an
 * {@code adminRole} attribute (the highest tier the member earns, or {@link
 * DropboxProvisioningTranslator#MEMBER_ONLY}). This DAO only ever reads, compares, or writes the
 * {@code adminRole} dimension when {@link DropboxProvisionerConfiguration#isManageAdminRoles()} is
 * true (an admin-role folder is configured). When it is false, the {@code adminRole} attribute is
 * stripped from retrieved entities (so it is not treated as a target attribute) and the admin-role
 * API is never called. Dropbox has no "list all roles" endpoint, so role NAME -&gt; {@code role_id}
 * resolution goes through {@link DropboxApiCommands#retrieveAdminRoleNameToId(String)}, whose
 * catalog is harvested while members are read.</p>
 *
 * <p><b>Ignore filtering.</b> Members whose email is in {@code dropboxIgnoreUserEmails} and groups
 * whose name is in {@code dropboxIgnoreGroupNames} are filtered out of the retrieve methods (the
 * same way TrueFoundry filters), so they are never created, updated, or deleted.</p>
 */
public class DropboxTargetDao extends GrouperProvisionerTargetDaoBase {

  /** logger */
  private static final Log LOG = LogFactory.getLog(DropboxTargetDao.class);

  // ============================
  // Low-level command logging (mirrors TrueFoundry)
  // ============================

  @Override
  public boolean loggingStart() {
    return GrouperHttpClient.logStart(new GrouperHttpClientLog());
  }

  @Override
  public String loggingStop() {
    return GrouperHttpClient.logEnd();
  }

  // ============================
  // Config accessors
  // ============================

  /**
   * @return the typed Dropbox provisioner configuration
   */
  private DropboxProvisionerConfiguration getDropboxConfiguration() {
    return (DropboxProvisionerConfiguration) this.getGrouperProvisioner()
        .retrieveGrouperProvisioningConfiguration();
  }

  /**
   * The external system config id resolves the Dropbox bearer token + base URL inside
   * {@link DropboxApiCommands}. Every command call takes it as the first argument.
   * @return the Dropbox external system config id
   */
  private String getConfigId() {
    return getDropboxConfiguration().getDropboxExternalSystemConfigId();
  }

  // ============================
  // Retrieve all groups
  // ============================

  /**
   * Retrieve every Dropbox team group, filtering out ignored group names. The native {@code group_id}
   * becomes the provisioning group id; matching is on {@code externalId}.
   */
  @Override
  public TargetDaoRetrieveAllGroupsResponse retrieveAllGroups(
      TargetDaoRetrieveAllGroupsRequest targetDaoRetrieveAllGroupsRequest) {

    long startNanos = System.nanoTime();

    try {
      String configId = getConfigId();
      DropboxProvisionerConfiguration config = getDropboxConfiguration();

      // build the ignore set once, the same way TrueFoundry does in its retrieve paths
      Set<String> ignoreGroupNames = DropboxApiCommands.parseIgnoreSet(config.getDropboxIgnoreGroupNames());

      List<ProvisioningGroup> results = new ArrayList<ProvisioningGroup>();

      for (DropboxGroup dropboxGroup : GrouperUtil.nonNull(DropboxApiCommands.retrieveDropboxGroups(configId))) {
        if (dropboxGroup == null) {
          continue;
        }
        // skip ignored groups so they are never created/updated/deleted by Grouper
        if (DropboxApiCommands.isIgnored(dropboxGroup.getName(), ignoreGroupNames)) {
          continue;
        }
        results.add(dropboxGroup.toProvisioningGroup());
        // generic-provisioner sync-back capture already happened at the DropboxApiCommands read seam
      }

      return new TargetDaoRetrieveAllGroupsResponse(results);
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("retrieveAllGroups", startNanos));
    }
  }

  // ============================
  // Retrieve single group
  // ============================

  /**
   * Retrieve a single Dropbox group by search attribute. Dropbox can fetch directly by native
   * {@code group_id} (the "id" attribute) via get_info; any other attribute (e.g. {@code externalId}
   * or {@code name}) requires listing all groups and matching, mirroring how Remedy/TrueFoundry
   * handle non-native lookups.
   */
  @Override
  public TargetDaoRetrieveGroupResponse retrieveGroup(
      TargetDaoRetrieveGroupRequest targetDaoRetrieveGroupRequest) {

    long startNanos = System.nanoTime();

    try {
      String configId = getConfigId();
      DropboxProvisionerConfiguration config = getDropboxConfiguration();
      Set<String> ignoreGroupNames = DropboxApiCommands.parseIgnoreSet(config.getDropboxIgnoreGroupNames());

      String searchAttribute = targetDaoRetrieveGroupRequest.getSearchAttribute();
      String searchValue = GrouperUtil.stringValue(targetDaoRetrieveGroupRequest.getSearchAttributeValue());

      DropboxGroup foundGroup = null;

      if (StringUtils.equals("id", searchAttribute)) {
        // native group_id -- direct get_info lookup
        foundGroup = DropboxApiCommands.retrieveDropboxGroup(configId, searchValue);
      } else {
        // non-native attribute -- list and match (Dropbox has no search-by-external-id endpoint)
        for (DropboxGroup candidate : GrouperUtil.nonNull(DropboxApiCommands.retrieveDropboxGroups(configId))) {
          if (candidate == null) {
            continue;
          }
          String candidateValue = null;
          if (StringUtils.equals("externalId", searchAttribute)) {
            candidateValue = candidate.getExternalId();
          } else if (StringUtils.equals("name", searchAttribute)) {
            candidateValue = candidate.getName();
          } else {
            throw new RuntimeException("Not expecting search attribute '" + searchAttribute + "'");
          }
          if (StringUtils.equals(candidateValue, searchValue)) {
            foundGroup = candidate;
            break;
          }
        }
      }

      // honor the ignore list even on a single-group lookup
      if (foundGroup != null && DropboxApiCommands.isIgnored(foundGroup.getName(), ignoreGroupNames)) {
        foundGroup = null;
      }

      ProvisioningGroup targetGroup = foundGroup == null ? null : foundGroup.toProvisioningGroup();
      return new TargetDaoRetrieveGroupResponse(targetGroup);
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("retrieveGroup", startNanos));
    }
  }

  // ============================
  // Retrieve all entities
  // ============================

  /**
   * Retrieve every Dropbox team member, filtering out ignored emails. As a side effect of the
   * underlying members/list_v2 read, the admin-role catalog (name -&gt; role_id) is harvested into
   * {@link DropboxApiCommands}.
   */
  @Override
  public TargetDaoRetrieveAllEntitiesResponse retrieveAllEntities(
      TargetDaoRetrieveAllEntitiesRequest targetDaoRetrieveAllEntitiesRequest) {

    long startNanos = System.nanoTime();

    try {
      String configId = getConfigId();
      DropboxProvisionerConfiguration config = getDropboxConfiguration();
      Set<String> ignoreUserEmails = DropboxApiCommands.parseIgnoreSet(config.getDropboxIgnoreUserEmails());

      List<ProvisioningEntity> results = new ArrayList<ProvisioningEntity>();

      for (DropboxUser dropboxUser : GrouperUtil.nonNull(DropboxApiCommands.retrieveDropboxUsers(configId))) {
        if (dropboxUser == null) {
          continue;
        }
        if (DropboxApiCommands.isIgnored(dropboxUser.getEmail(), ignoreUserEmails)) {
          continue;
        }
        results.add(toProvisioningEntityWithAdminRoleGating(dropboxUser, config));
      }

      return new TargetDaoRetrieveAllEntitiesResponse(results);
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("retrieveAllEntities", startNanos));
    }
  }

  // ============================
  // Retrieve single entity
  // ============================

  /**
   * Retrieve a single Dropbox member by search attribute, using the Dropbox get_info_v2 selector
   * union. The Grouper attribute name is mapped to the matching Dropbox selector tag
   * ({@code externalId -> external_id}, {@code email -> email}, {@code id -> team_member_id}).
   */
  @Override
  public TargetDaoRetrieveEntityResponse retrieveEntity(
      TargetDaoRetrieveEntityRequest targetDaoRetrieveEntityRequest) {

    long startNanos = System.nanoTime();

    try {
      String configId = getConfigId();
      DropboxProvisionerConfiguration config = getDropboxConfiguration();
      Set<String> ignoreUserEmails = DropboxApiCommands.parseIgnoreSet(config.getDropboxIgnoreUserEmails());

      String searchAttribute = targetDaoRetrieveEntityRequest.getSearchAttribute();
      String searchValue = GrouperUtil.stringValue(targetDaoRetrieveEntityRequest.getSearchAttributeValue());

      // map the Grouper attribute to the Dropbox UserSelectorArg union tag
      String selectorTag;
      if (StringUtils.equals("externalId", searchAttribute)) {
        selectorTag = "external_id";
      } else if (StringUtils.equals("email", searchAttribute)) {
        selectorTag = "email";
      } else if (StringUtils.equals("id", searchAttribute)) {
        selectorTag = "team_member_id";
      } else {
        throw new RuntimeException("Not expecting search attribute '" + searchAttribute + "'");
      }

      DropboxUser foundUser = DropboxApiCommands.retrieveDropboxUser(configId, selectorTag, searchValue);

      // honor the ignore list even on a single-entity lookup
      if (foundUser != null && DropboxApiCommands.isIgnored(foundUser.getEmail(), ignoreUserEmails)) {
        foundUser = null;
      }

      ProvisioningEntity targetEntity = foundUser == null ? null
          : toProvisioningEntityWithAdminRoleGating(foundUser, config);
      return new TargetDaoRetrieveEntityResponse(targetEntity);
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("retrieveEntity", startNanos));
    }
  }

  /**
   * Convert a Dropbox member to a target entity, applying the admin-role gating rule.
   *
   * <p>When admin roles are NOT managed, the {@code adminRole} parsed from the native roles[] is
   * cleared so it is never presented as a target attribute. When admin roles ARE managed, a member
   * with no admin role is normalized to {@link DropboxProvisioningTranslator#MEMBER_ONLY} so it
   * compares equal to the value the translator stamps on the Grouper side (the translator marks
   * members in no admin-role group as {@code member_only}, not null).</p>
   *
   * @param dropboxUser the native member
   * @param config the provisioner configuration (for the manage-admin-roles flag)
   * @return the target entity
   */
  private ProvisioningEntity toProvisioningEntityWithAdminRoleGating(DropboxUser dropboxUser,
      DropboxProvisionerConfiguration config) {
    if (config.isManageAdminRoles()) {
      // normalize "no admin role" to member_only so it matches the translator's stamp
      if (StringUtils.isBlank(dropboxUser.getAdminRole())) {
        dropboxUser.setAdminRole(DropboxProvisioningTranslator.MEMBER_ONLY);
      }
    } else {
      // admin roles not managed -- do not surface adminRole as a target attribute
      dropboxUser.setAdminRole(null);
    }
    return dropboxUser.toProvisioningEntity();
  }

  // ============================
  // Insert group
  // ============================

  /**
   * Create a Dropbox team group, then capture its assigned native {@code group_id} back onto the
   * target group so memberships can reference it.
   */
  @Override
  public TargetDaoInsertGroupResponse insertGroup(TargetDaoInsertGroupRequest targetDaoInsertGroupRequest) {

    long startNanos = System.nanoTime();
    ProvisioningGroup targetGroup = targetDaoInsertGroupRequest.getTargetGroup();

    try {
      String configId = getConfigId();

      DropboxGroup dropboxGroup = DropboxGroup.fromProvisioningGroup(targetGroup, null);
      DropboxGroup createdGroup = DropboxApiCommands.createDropboxGroup(configId, dropboxGroup);

      if (createdGroup != null && StringUtils.isNotBlank(createdGroup.getId())) {
        targetGroup.setId(createdGroup.getId());
      }
      targetGroup.setProvisioned(true);
      for (ProvisioningObjectChange change : GrouperUtil.nonNull(targetGroup.getInternal_objectChanges())) {
        change.setProvisioned(true);
      }

      return new TargetDaoInsertGroupResponse();
    } catch (Exception e) {
      targetGroup.setProvisioned(false);
      for (ProvisioningObjectChange change : GrouperUtil.nonNull(targetGroup.getInternal_objectChanges())) {
        change.setProvisioned(false);
      }
      throw e;
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("insertGroup", startNanos));
    }
  }

  // ============================
  // Update group
  // ============================

  /**
   * Update the changed fields of a Dropbox group (selected by native {@code group_id}).
   */
  @Override
  public TargetDaoUpdateGroupResponse updateGroup(TargetDaoUpdateGroupRequest targetDaoUpdateGroupRequest) {

    long startNanos = System.nanoTime();
    ProvisioningGroup targetGroup = targetDaoUpdateGroupRequest.getTargetGroup();

    try {
      String configId = getConfigId();

      // collect only the attributes that actually changed so update sends just those new_* fields
      Set<String> fieldNamesToUpdate = new LinkedHashSet<String>();
      for (ProvisioningObjectChange change : GrouperUtil.nonNull(targetGroup.getInternal_objectChanges())) {
        if (!StringUtils.isBlank(change.getAttributeName())) {
          fieldNamesToUpdate.add(change.getAttributeName());
        }
      }

      DropboxGroup dropboxGroup = DropboxGroup.fromProvisioningGroup(targetGroup, fieldNamesToUpdate);
      DropboxApiCommands.updateDropboxGroup(configId, dropboxGroup);

      targetGroup.setProvisioned(true);
      for (ProvisioningObjectChange change : GrouperUtil.nonNull(targetGroup.getInternal_objectChanges())) {
        change.setProvisioned(true);
      }

      return new TargetDaoUpdateGroupResponse();
    } catch (Exception e) {
      targetGroup.setProvisioned(false);
      for (ProvisioningObjectChange change : GrouperUtil.nonNull(targetGroup.getInternal_objectChanges())) {
        change.setProvisioned(false);
      }
      throw e;
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("updateGroup", startNanos));
    }
  }

  // ============================
  // Delete group
  // ============================

  /**
   * Delete a Dropbox group by native {@code group_id} (the underlying command polls for async
   * completion if Dropbox defers the delete).
   */
  @Override
  public TargetDaoDeleteGroupResponse deleteGroup(TargetDaoDeleteGroupRequest targetDaoDeleteGroupRequest) {

    long startNanos = System.nanoTime();
    ProvisioningGroup targetGroup = targetDaoDeleteGroupRequest.getTargetGroup();

    try {
      String configId = getConfigId();

      DropboxGroup dropboxGroup = DropboxGroup.fromProvisioningGroup(targetGroup, null);
      DropboxApiCommands.deleteDropboxGroup(configId, dropboxGroup.getId());

      targetGroup.setProvisioned(true);
      for (ProvisioningObjectChange change : GrouperUtil.nonNull(targetGroup.getInternal_objectChanges())) {
        change.setProvisioned(true);
      }

      return new TargetDaoDeleteGroupResponse();
    } catch (Exception e) {
      targetGroup.setProvisioned(false);
      for (ProvisioningObjectChange change : GrouperUtil.nonNull(targetGroup.getInternal_objectChanges())) {
        change.setProvisioned(false);
      }
      throw e;
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("deleteGroup", startNanos));
    }
  }

  // ============================
  // Insert entity
  // ============================

  /**
   * Invite/create a Dropbox team member, then capture the assigned native {@code team_member_id}
   * onto the target entity. If admin roles are managed and the entity already carries a resolved
   * {@code adminRole}, the role is applied to the freshly created member as well.
   */
  @Override
  public TargetDaoInsertEntityResponse insertEntity(TargetDaoInsertEntityRequest targetDaoInsertEntityRequest) {

    long startNanos = System.nanoTime();
    ProvisioningEntity targetEntity = targetDaoInsertEntityRequest.getTargetEntity();

    try {
      String configId = getConfigId();
      DropboxProvisionerConfiguration config = getDropboxConfiguration();

      DropboxUser dropboxUser = DropboxUser.fromProvisioningEntity(targetEntity, null);
      if (StringUtils.isBlank(dropboxUser.getEmail())) {
        throw new RuntimeException("user email is required for insertEntity");
      }

      DropboxUser createdUser = DropboxApiCommands.createDropboxUser(configId, dropboxUser);

      // entity id = native team_member_id
      String teamMemberId = createdUser == null ? null : createdUser.getId();
      if (StringUtils.isNotBlank(teamMemberId)) {
        targetEntity.setId(teamMemberId);
      }

      // apply the resolved admin role on create (only when admin roles are managed). fromProvisioningEntity
      // with null fieldNames already populated dropboxUser.adminRole from the entity attribute.
      if (config.isManageAdminRoles() && StringUtils.isNotBlank(teamMemberId)) {
        applyAdminRole(configId, teamMemberId, dropboxUser.getAdminRole());
      }

      // lifecycle: a brand-new member is active; suspend it if the desired state is suspended
      if (config.isManageLifecycle() && StringUtils.isNotBlank(teamMemberId)
          && dropboxUser.isLifecycleSuspended()) {
        DropboxApiCommands.suspendDropboxUser(configId, teamMemberId);
      }

      targetEntity.setProvisioned(true);
      for (ProvisioningObjectChange change : GrouperUtil.nonNull(targetEntity.getInternal_objectChanges())) {
        change.setProvisioned(true);
      }

      return new TargetDaoInsertEntityResponse();
    } catch (Exception e) {
      targetEntity.setProvisioned(false);
      for (ProvisioningObjectChange change : GrouperUtil.nonNull(targetEntity.getInternal_objectChanges())) {
        change.setProvisioned(false);
      }
      throw e;
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("insertEntity", startNanos));
    }
  }

  // ============================
  // Update entity
  // ============================

  /**
   * Update a Dropbox member's profile and (when managed and changed) its admin role.
   *
   * <p>Profile fields ({@code email}, {@code externalId}, {@code givenName}, {@code surname}) that
   * changed are pushed via set_profile_v2. Separately, when admin roles are managed and the
   * {@code adminRole} attribute changed, the role NAME is resolved to a Dropbox {@code role_id} and
   * applied via set_admin_permissions_v2 ({@code member_only}/blank means an empty role list, i.e.
   * demote to a regular member).</p>
   */
  @Override
  public TargetDaoUpdateEntityResponse updateEntity(TargetDaoUpdateEntityRequest targetDaoUpdateEntityRequest) {

    long startNanos = System.nanoTime();
    ProvisioningEntity targetEntity = targetDaoUpdateEntityRequest.getTargetEntity();

    try {
      String configId = getConfigId();
      DropboxProvisionerConfiguration config = getDropboxConfiguration();

      // collect the changed attribute names
      Set<String> fieldNamesToUpdate = new LinkedHashSet<String>();
      for (ProvisioningObjectChange change : GrouperUtil.nonNull(targetEntity.getInternal_objectChanges())) {
        if (!StringUtils.isBlank(change.getAttributeName())) {
          fieldNamesToUpdate.add(change.getAttributeName());
        }
      }

      DropboxUser dropboxUser = DropboxUser.fromProvisioningEntity(targetEntity, fieldNamesToUpdate);
      String teamMemberId = targetEntity.getId();

      // push profile changes only if at least one set_profile-managed field changed
      boolean profileChanged = fieldNamesToUpdate.contains("email")
          || fieldNamesToUpdate.contains("externalId")
          || fieldNamesToUpdate.contains("givenName")
          || fieldNamesToUpdate.contains("surname");
      if (profileChanged && StringUtils.isNotBlank(teamMemberId)) {
        DropboxApiCommands.updateDropboxUser(configId, dropboxUser, fieldNamesToUpdate);
      }

      // apply admin role only when managed AND the adminRole attribute actually changed
      if (config.isManageAdminRoles() && fieldNamesToUpdate.contains("adminRole")
          && StringUtils.isNotBlank(teamMemberId)) {
        applyAdminRole(configId, teamMemberId, dropboxUser.getAdminRole());
      }

      // lifecycle: suspend / unsuspend when managed and the lifecycleState changed
      if (config.isManageLifecycle() && fieldNamesToUpdate.contains("lifecycleState")
          && StringUtils.isNotBlank(teamMemberId)) {
        if (dropboxUser.isLifecycleSuspended()) {
          DropboxApiCommands.suspendDropboxUser(configId, teamMemberId);
        } else {
          DropboxApiCommands.unsuspendDropboxUser(configId, teamMemberId);
        }
      }

      targetEntity.setProvisioned(true);
      for (ProvisioningObjectChange change : GrouperUtil.nonNull(targetEntity.getInternal_objectChanges())) {
        change.setProvisioned(true);
      }

      return new TargetDaoUpdateEntityResponse();
    } catch (Exception e) {
      targetEntity.setProvisioned(false);
      for (ProvisioningObjectChange change : GrouperUtil.nonNull(targetEntity.getInternal_objectChanges())) {
        change.setProvisioned(false);
      }
      throw e;
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("updateEntity", startNanos));
    }
  }

  /**
   * Resolve an admin-role NAME to a Dropbox {@code role_id} and set it on a member.
   *
   * <p>A blank role, {@link DropboxProvisioningTranslator#MEMBER_ONLY}, or
   * {@link DropboxProvisioningTranslator#ADMIN_ROLE_NONE} all mean "no admin rights" -- an empty
   * role-id list is sent, which demotes the member to a regular member. Otherwise the role name is
   * looked up in the catalog harvested from the members read; an unknown name is an error because we
   * cannot fabricate a {@code role_id}. Callers must only invoke this when
   * {@link DropboxProvisionerConfiguration#isManageAdminRoles()} is true.</p>
   *
   * @param configId the external system config id
   * @param teamMemberId the native team_member_id
   * @param adminRoleName the desired admin-role name (may be blank / member_only / none)
   */
  private void applyAdminRole(String configId, String teamMemberId, String adminRoleName) {

    List<String> roleIds = new ArrayList<String>();

    boolean noAdminRights = StringUtils.isBlank(adminRoleName)
        || DropboxProvisioningTranslator.MEMBER_ONLY.equalsIgnoreCase(adminRoleName)
        || DropboxProvisioningTranslator.ADMIN_ROLE_NONE.equalsIgnoreCase(adminRoleName);

    if (!noAdminRights) {
      // resolve role NAME -> role_id from the assignable-role catalog for this member
      // (members/list_member_roles), which is populated even on a fresh team
      Map<String, String> nameToId = DropboxApiCommands.retrieveAdminRoleNameToId(configId, teamMemberId);
      String roleId = nameToId == null ? null : nameToId.get(adminRoleName);
      if (StringUtils.isBlank(roleId)) {
        throw new RuntimeException("Could not resolve Dropbox admin role '" + adminRoleName
            + "' to a role_id for teamMemberId '" + teamMemberId + "'. Known roles: " + nameToId);
      }
      roleIds.add(roleId);
    }

    // empty roleIds => member_only (no admin rights)
    DropboxApiCommands.setDropboxAdminRoles(configId, teamMemberId, roleIds);
  }

  // ============================
  // Delete entity
  // ============================

  /**
   * Remove a Dropbox member from the team, honoring the configured wipe-data and keep-account flags.
   */
  @Override
  public TargetDaoDeleteEntityResponse deleteEntity(TargetDaoDeleteEntityRequest targetDaoDeleteEntityRequest) {

    long startNanos = System.nanoTime();
    ProvisioningEntity targetEntity = targetDaoDeleteEntityRequest.getTargetEntity();

    try {
      String configId = getConfigId();
      DropboxProvisionerConfiguration config = getDropboxConfiguration();

      String teamMemberId = targetEntity.getId();
      if (StringUtils.isBlank(teamMemberId)) {
        throw new RuntimeException("team_member_id is required for deleteEntity");
      }

      // lifecycle downgrade: if the departing member's subject is in the <lifecycleFolder>:Downgrade
      // marker group, convert the account to a free Basic account (keep_account=true) instead of
      // deleting it -- updating the account email first (e.g. an alumni address) so the member can
      // still sign in to the converted account afterward.
      Member downgradeMember = config.isManageLifecycle()
          ? resolveDowngradeMember(config, targetEntity) : null;
      if (downgradeMember != null) {
        // downgrade: set the alumni / forwarding email (read from the member's subject at delete time)
        // before converting the account to a free Basic account
        String downgradeEmail = resolveDowngradeEmail(config, downgradeMember);
        if (StringUtils.isNotBlank(downgradeEmail)) {
          DropboxUser emailUpdate = new DropboxUser();
          emailUpdate.setId(teamMemberId);
          emailUpdate.setEmail(downgradeEmail);
          DropboxApiCommands.updateDropboxUser(configId, emailUpdate, GrouperUtil.toSet("email"));
        }
        // keep_account=true downgrades; wipe_data stays false so the member keeps their files
        DropboxApiCommands.removeDropboxUser(configId, teamMemberId, false, true);
      } else {
        DropboxApiCommands.removeDropboxUser(configId, teamMemberId,
            config.isDropboxWipeDataOnRemove(), config.isDropboxKeepAccountOnRemove());
      }

      targetEntity.setProvisioned(true);
      for (ProvisioningObjectChange change : GrouperUtil.nonNull(targetEntity.getInternal_objectChanges())) {
        change.setProvisioned(true);
      }

      return new TargetDaoDeleteEntityResponse();
    } catch (Exception e) {
      targetEntity.setProvisioned(false);
      for (ProvisioningObjectChange change : GrouperUtil.nonNull(targetEntity.getInternal_objectChanges())) {
        change.setProvisioned(false);
      }
      throw e;
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("deleteEntity", startNanos));
    }
  }

  /**
   * If a departing member's subject is in the {@code <lifecycleFolder>:Downgrade} marker group, return
   * the matching {@link Member} (so the caller can read its subject for the downgrade email); otherwise
   * null.  Resolved directly from Grouper (via GroupFinder), since a deprovisioned entity is no longer
   * translated, using the entity's Grouper memberId from its wrapper.
   * @param config the provisioner configuration
   * @param targetEntity the entity being removed
   * @return the downgrade Member, or null if the member should be deleted normally
   */
  private Member resolveDowngradeMember(DropboxProvisionerConfiguration config, ProvisioningEntity targetEntity) {

    String memberId = targetEntity.getProvisioningEntityWrapper() == null ? null
        : targetEntity.getProvisioningEntityWrapper().getMemberId();
    if (StringUtils.isBlank(memberId)) {
      return null;
    }

    String downgradeGroupName = config.getDropboxLifecycleFolderName() + ":" + DropboxUser.LIFECYCLE_MARKER_DOWNGRADE;

    GrouperSession grouperSession = GrouperSession.staticGrouperSession(false);
    boolean startedSession = false;
    if (grouperSession == null) {
      try {
        grouperSession = GrouperSession.startRootSession();
      } catch (edu.internet2.middleware.grouper.exception.SessionException se) {
        throw new RuntimeException("Could not start a Grouper session to resolve the Dropbox downgrade group", se);
      }
      startedSession = true;
    }

    try {
      Group downgradeGroup = GroupFinder.findByName(grouperSession, downgradeGroupName, false);
      if (downgradeGroup == null) {
        return null;
      }
      for (Member member : GrouperUtil.nonNull(downgradeGroup.getMembers())) {
        if (memberId.equals(member.getUuid())) {
          return member;
        }
      }
      return null;
    } finally {
      if (startedSession) {
        GrouperSession.stopQuietly(grouperSession);
      }
    }
  }

  /**
   * Read the downgrade (alumni / forwarding) email from the member's subject, using the configured
   * subject attribute name. Returns null if no attribute is configured or the value is absent.
   * @param config the provisioner configuration
   * @param member the downgrade member
   * @return the downgrade email, or null
   */
  private String resolveDowngradeEmail(DropboxProvisionerConfiguration config, Member member) {
    String subjectAttribute = config.getDropboxDowngradeEmailSubjectAttribute();
    if (StringUtils.isBlank(subjectAttribute) || member == null) {
      return null;
    }
    Subject subject = member.getSubject();
    return subject == null ? null : subject.getAttributeValue(subjectAttribute);
  }

  // ============================
  // Retrieve memberships of a group
  // ============================

  /**
   * Retrieve all memberships of one Dropbox group. The membership's provisioning group id is the
   * native {@code group_id}; its provisioning entity id is the native {@code team_member_id};
   * {@code accessType} (member/owner) is carried as an attribute.
   */
  @Override
  public TargetDaoRetrieveMembershipsByGroupResponse retrieveMembershipsByGroup(
      TargetDaoRetrieveMembershipsByGroupRequest targetDaoRetrieveMembershipsByGroupRequest) {

    long startNanos = System.nanoTime();
    ProvisioningGroup targetGroup = targetDaoRetrieveMembershipsByGroupRequest.getTargetGroup();

    try {
      String configId = getConfigId();
      String groupId = targetGroup == null ? null : targetGroup.getId();

      List<ProvisioningMembership> results = retrieveMembershipsForGroupId(configId, groupId);

      return new TargetDaoRetrieveMembershipsByGroupResponse(results);
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("retrieveMembershipsByGroup", startNanos));
    }
  }

  // ============================
  // Retrieve all memberships
  // ============================

  /**
   * Retrieve every membership across all (non-ignored) Dropbox groups by listing groups and then
   * listing each group's members. Dropbox has no single "all memberships" endpoint, so this loops
   * the per-group endpoint -- the same data {@link #retrieveMembershipsByGroup} returns, aggregated.
   */
  @Override
  public TargetDaoRetrieveAllMembershipsResponse retrieveAllMemberships(
      TargetDaoRetrieveAllMembershipsRequest targetDaoRetrieveAllMembershipsRequest) {

    long startNanos = System.nanoTime();

    try {
      String configId = getConfigId();
      DropboxProvisionerConfiguration config = getDropboxConfiguration();
      Set<String> ignoreGroupNames = DropboxApiCommands.parseIgnoreSet(config.getDropboxIgnoreGroupNames());

      List<ProvisioningMembership> results = new ArrayList<ProvisioningMembership>();

      for (DropboxGroup dropboxGroup : GrouperUtil.nonNull(DropboxApiCommands.retrieveDropboxGroups(configId))) {
        if (dropboxGroup == null || StringUtils.isBlank(dropboxGroup.getId())) {
          continue;
        }
        if (DropboxApiCommands.isIgnored(dropboxGroup.getName(), ignoreGroupNames)) {
          continue;
        }
        results.addAll(retrieveMembershipsForGroupId(configId, dropboxGroup.getId()));
      }

      return new TargetDaoRetrieveAllMembershipsResponse(results);
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("retrieveAllMemberships", startNanos));
    }
  }

  /**
   * Shared helper: list one group's Dropbox memberships and convert them to provisioning
   * memberships keyed by native ids.
   * @param configId the external system config id
   * @param groupId the native group_id (blank yields an empty list)
   * @return the provisioning memberships for the group
   */
  private List<ProvisioningMembership> retrieveMembershipsForGroupId(String configId, String groupId) {
    List<ProvisioningMembership> results = new ArrayList<ProvisioningMembership>();
    if (StringUtils.isBlank(groupId)) {
      return results;
    }
    for (DropboxMembership dropboxMembership : GrouperUtil.nonNull(
        DropboxApiCommands.retrieveDropboxGroupMemberships(configId, groupId))) {
      if (dropboxMembership == null || StringUtils.isBlank(dropboxMembership.getTeamMemberId())) {
        continue;
      }
      // Dropbox memberships have no id; match on group_id + team_member_id
      ProvisioningMembership targetMembership = new ProvisioningMembership(false);
      targetMembership.setProvisioningGroupId(dropboxMembership.getGroupId());
      targetMembership.setProvisioningEntityId(dropboxMembership.getTeamMemberId());
      if (StringUtils.isNotBlank(dropboxMembership.getAccessType())) {
        targetMembership.assignAttributeValue("accessType", dropboxMembership.getAccessType());
      }
      results.add(targetMembership);
    }
    return results;
  }

  // ============================
  // Insert memberships (batched by group)
  // ============================

  /**
   * Add memberships, batched by native {@code group_id} so each group is hit with a single
   * groups/members/add call. The member is referenced by native {@code team_member_id}; the
   * optional {@code accessType} attribute selects member vs owner (defaults to member).
   */
  @Override
  public TargetDaoInsertMembershipsResponse insertMemberships(
      TargetDaoInsertMembershipsRequest targetDaoInsertMembershipsRequest) {

    long startNanos = System.nanoTime();
    List<ProvisioningMembership> targetMemberships =
        targetDaoInsertMembershipsRequest.getTargetMemberships();

    try {
      String configId = getConfigId();

      // groupId -> the Dropbox membership payloads to add for that group
      Map<String, List<DropboxMembership>> groupIdToDropboxMemberships =
          new LinkedHashMap<String, List<DropboxMembership>>();
      // groupId -> the provisioning memberships, so we can flag provisioned per group after the call
      Map<String, List<ProvisioningMembership>> groupIdToProvisioningMemberships =
          new LinkedHashMap<String, List<ProvisioningMembership>>();

      for (ProvisioningMembership targetMembership : GrouperUtil.nonNull(targetMemberships)) {
        String groupId = targetMembership.getProvisioningGroupId();
        String teamMemberId = targetMembership.getProvisioningEntityId();
        if (StringUtils.isBlank(groupId) || StringUtils.isBlank(teamMemberId)) {
          continue;
        }

        DropboxMembership dropboxMembership = new DropboxMembership();
        dropboxMembership.setGroupId(groupId);
        dropboxMembership.setTeamMemberId(teamMemberId);
        // access_type is an optional attribute; default to a regular member
        String accessType = targetMembership.retrieveAttributeValueString("accessType");
        dropboxMembership.setAccessType(
            StringUtils.defaultIfBlank(accessType, DropboxMembership.ACCESS_TYPE_MEMBER));

        if (!groupIdToDropboxMemberships.containsKey(groupId)) {
          groupIdToDropboxMemberships.put(groupId, new ArrayList<DropboxMembership>());
          groupIdToProvisioningMemberships.put(groupId, new ArrayList<ProvisioningMembership>());
        }
        groupIdToDropboxMemberships.get(groupId).add(dropboxMembership);
        groupIdToProvisioningMemberships.get(groupId).add(targetMembership);
      }

      // one groups/members/add call per group; flag the whole batch as (un)provisioned together
      for (String groupId : groupIdToDropboxMemberships.keySet()) {
        List<ProvisioningMembership> memberships = groupIdToProvisioningMemberships.get(groupId);
        try {
          DropboxApiCommands.addDropboxGroupMembers(configId, groupId, groupIdToDropboxMemberships.get(groupId));
          markProvisioned(memberships, true);
          // sync-back: write-track each added membership (memberships are tracked from our writes,
          // never re-read), so an incremental add adjusts grouper_prov_mship immediately
          for (DropboxMembership dropboxMembership : groupIdToDropboxMemberships.get(groupId)) {
            DropboxProvisioningTargetNativeSync.captureMembershipInsertFromCurrentProvisioner(
                groupId, dropboxMembership.getTeamMemberId());
          }
        } catch (Exception e) {
          LOG.warn("Dropbox: failed to add members to group '" + groupId + "'", e);
          markProvisioned(memberships, false);
        }
      }

      return new TargetDaoInsertMembershipsResponse();
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("insertMemberships", startNanos));
    }
  }

  // ============================
  // Delete memberships (batched by group)
  // ============================

  /**
   * Remove memberships, batched by native {@code group_id} so each group is hit with a single
   * groups/members/remove call referencing the native {@code team_member_id}s.
   */
  @Override
  public TargetDaoDeleteMembershipsResponse deleteMemberships(
      TargetDaoDeleteMembershipsRequest targetDaoDeleteMembershipsRequest) {

    long startNanos = System.nanoTime();
    List<ProvisioningMembership> targetMemberships =
        targetDaoDeleteMembershipsRequest.getTargetMemberships();

    try {
      String configId = getConfigId();

      // groupId -> team_member_ids to remove
      Map<String, List<String>> groupIdToTeamMemberIds = new LinkedHashMap<String, List<String>>();
      // groupId -> provisioning memberships, to flag provisioned per group after the call
      Map<String, List<ProvisioningMembership>> groupIdToProvisioningMemberships =
          new LinkedHashMap<String, List<ProvisioningMembership>>();

      for (ProvisioningMembership targetMembership : GrouperUtil.nonNull(targetMemberships)) {
        String groupId = targetMembership.getProvisioningGroupId();
        String teamMemberId = targetMembership.getProvisioningEntityId();
        if (StringUtils.isBlank(groupId) || StringUtils.isBlank(teamMemberId)) {
          continue;
        }
        if (!groupIdToTeamMemberIds.containsKey(groupId)) {
          groupIdToTeamMemberIds.put(groupId, new ArrayList<String>());
          groupIdToProvisioningMemberships.put(groupId, new ArrayList<ProvisioningMembership>());
        }
        groupIdToTeamMemberIds.get(groupId).add(teamMemberId);
        groupIdToProvisioningMemberships.get(groupId).add(targetMembership);
      }

      for (String groupId : groupIdToTeamMemberIds.keySet()) {
        List<ProvisioningMembership> memberships = groupIdToProvisioningMemberships.get(groupId);
        try {
          DropboxApiCommands.removeDropboxGroupMembers(configId, groupId, groupIdToTeamMemberIds.get(groupId));
          markProvisioned(memberships, true);
          // sync-back: write-track each removed membership so an incremental remove drops its
          // grouper_prov_mship row immediately
          for (String removedTeamMemberId : groupIdToTeamMemberIds.get(groupId)) {
            DropboxProvisioningTargetNativeSync.captureMembershipDeleteFromCurrentProvisioner(
                groupId, removedTeamMemberId);
          }
        } catch (Exception e) {
          LOG.warn("Dropbox: failed to remove members from group '" + groupId + "'", e);
          markProvisioned(memberships, false);
        }
      }

      return new TargetDaoDeleteMembershipsResponse();
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("deleteMemberships", startNanos));
    }
  }

  /**
   * Flag a batch of memberships (and their object changes) provisioned or not, the way the
   * single-object paths do.
   * @param memberships the memberships to flag
   * @param provisioned the provisioned state
   */
  private static void markProvisioned(List<ProvisioningMembership> memberships, boolean provisioned) {
    for (ProvisioningMembership membership : GrouperUtil.nonNull(memberships)) {
      membership.setProvisioned(provisioned);
      for (ProvisioningObjectChange change : GrouperUtil.nonNull(membership.getInternal_objectChanges())) {
        change.setProvisioned(provisioned);
      }
    }
  }

  // ============================
  // DAO capabilities
  // ============================

  /**
   * Declare exactly the operations this DAO implements so the provisioning framework drives the
   * Dropbox target through the supported paths only.
   */
  @Override
  public void registerGrouperProvisionerDaoCapabilities(
      GrouperProvisionerDaoCapabilities grouperProvisionerDaoCapabilities) {

    // retrieves
    grouperProvisionerDaoCapabilities.setCanRetrieveAllGroups(true);
    grouperProvisionerDaoCapabilities.setCanRetrieveGroup(true);
    grouperProvisionerDaoCapabilities.setCanRetrieveAllEntities(true);
    grouperProvisionerDaoCapabilities.setCanRetrieveEntity(true);
    grouperProvisionerDaoCapabilities.setCanRetrieveAllMemberships(true);
    grouperProvisionerDaoCapabilities.setCanRetrieveMembershipsAllByGroup(true);

    // group CRUD
    grouperProvisionerDaoCapabilities.setCanInsertGroup(true);
    grouperProvisionerDaoCapabilities.setCanUpdateGroup(true);
    grouperProvisionerDaoCapabilities.setCanDeleteGroup(true);

    // entity CRUD
    grouperProvisionerDaoCapabilities.setCanInsertEntity(true);
    grouperProvisionerDaoCapabilities.setCanUpdateEntity(true);
    grouperProvisionerDaoCapabilities.setCanDeleteEntity(true);

    // membership CRUD (batched)
    grouperProvisionerDaoCapabilities.setCanInsertMemberships(true);
    grouperProvisionerDaoCapabilities.setCanDeleteMemberships(true);

    // read path captures native groups/users from the raw JSON at the DropboxApiCommands seam, and
    // memberships from the group_id + team_member_id pairs while the nodes are in scope
    grouperProvisionerDaoCapabilities.setCanSyncBack(true);
  }

}
