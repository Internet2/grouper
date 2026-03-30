package edu.internet2.middleware.grouper.app.truefoundry;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningLists;
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
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveAllDataRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveAllDataResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveEntityRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveEntityResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveGroupRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveGroupResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoTimingInfo;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoUpdateEntityRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoUpdateEntityResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoUpdateGroupRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoUpdateGroupResponse;
import edu.internet2.middleware.grouper.util.GrouperHttpClient;
import edu.internet2.middleware.grouper.util.GrouperHttpClientLog;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * TrueFoundry TargetDao — manages users, teams, roles, and team memberships
 * via the TrueFoundry native REST API.
 *
 * Entity ID note: TrueFoundry is email-based, so the provisioning entity ID
 * is the user's email address. The email is also used as the SCIM user identifier
 * for display name updates (PATCH /scim/v2/{tenant}/{sso}/Users/{email}).
 *
 * Team membership note: TrueFoundry has no individual add/remove member endpoints.
 * Team membership is managed by replacing the full member list via PUT /teams.
 * insertMembership and deleteMembership for teams both retrieve the current team
 * state, modify the member list, and PUT the full manifest back.
 *
 * Role membership note: There is no API to read current role assignments.
 * insertMembership assigns a role to a user; deleteMembership assigns the
 * configured default role.  The provisioner always pushes on full sync.
 */
public class TrueFoundryTargetDao extends GrouperProvisionerTargetDaoBase {

  private static final Log LOG = LogFactory.getLog(TrueFoundryTargetDao.class);

  @Override
  public boolean loggingStart() {
    return GrouperHttpClient.logStart(new GrouperHttpClientLog());
  }

  @Override
  public String loggingStop() {
    return GrouperHttpClient.logEnd();
  }

  private TrueFoundryProvisionerConfiguration getTrueFoundryConfiguration() {
    return (TrueFoundryProvisionerConfiguration) this.getGrouperProvisioner()
        .retrieveGrouperProvisioningConfiguration();
  }

  // ============================
  // Retrieve all data: users + teams + roles + team memberships in one call.
  // The subjects endpoint (GET /subjects) returns users AND all teams with their
  // members and managers embedded.  Roles are fetched separately via retrieveRoles().
  // Role memberships are not returned (no read API exists; provisioner always pushes on full sync).
  // ============================

  @Override
  public TargetDaoRetrieveAllDataResponse retrieveAllData(
      TargetDaoRetrieveAllDataRequest targetDaoRetrieveAllDataRequest) {

    TargetDaoRetrieveAllDataResponse response = new TargetDaoRetrieveAllDataResponse();
    GrouperProvisioningLists targetData = new GrouperProvisioningLists();
    response.setTargetData(targetData);

    long startNanos = System.nanoTime();

    try {
      TrueFoundryProvisionerConfiguration config = getTrueFoundryConfiguration();
      String configId = config.getTrueFoundryExternalSystemConfigId();

      Set<String> ignoreEmails = TrueFoundryApiCommands.parseIgnoreSet(config.getTrueFoundryIgnoreUserEmails());
      Set<String> ignoreRoles = TrueFoundryApiCommands.parseIgnoreSet(config.getTrueFoundryIgnoreRoles());

      // one subjects call returns active users + all teams with members/managers
      TrueFoundryApiCommands.SubjectsData subjectsData =
          TrueFoundryApiCommands.retrieveSubjectsData(configId, ignoreEmails);

      // build entities from users
      List<ProvisioningEntity> provisioningEntities = new ArrayList<ProvisioningEntity>();
      for (TrueFoundryUser user : GrouperUtil.nonNull(subjectsData.users)) {
        provisioningEntities.add(user.toProvisioningEntity());
      }
      targetData.setProvisioningEntities(provisioningEntities);

      // build groups: roles + teams
      List<ProvisioningGroup> provisioningGroups = new ArrayList<ProvisioningGroup>();

      List<TrueFoundryGroup> roles = TrueFoundryApiCommands.retrieveRoles(configId, ignoreRoles);
      for (TrueFoundryGroup role : GrouperUtil.nonNull(roles)) {
        provisioningGroups.add(role.toProvisioningGroup());
      }

      for (TrueFoundryGroup team : GrouperUtil.nonNull(subjectsData.teams)) {
        provisioningGroups.add(team.toProvisioningGroup());
      }

      targetData.setProvisioningGroups(provisioningGroups);

      // build team memberships from manifest members/managers
      List<ProvisioningMembership> provisioningMemberships = new ArrayList<ProvisioningMembership>();

      boolean addManagerMetadata = config.isTrueFoundryAddTeamManagerMetadata();
      String managerMetadataName = config.getTrueFoundryTeamManagerMetadataName();

      for (TrueFoundryGroup team : GrouperUtil.nonNull(subjectsData.teams)) {
        String teamId = team.getId();
        Set<String> managerSet = new HashSet<String>(GrouperUtil.nonNull(team.getManagers()));

        for (String memberEmail : GrouperUtil.nonNull(team.getMembers())) {
          if (StringUtils.isBlank(memberEmail)) {
            continue;
          }
          ProvisioningMembership membership = new ProvisioningMembership(false);
          membership.setProvisioningGroupId(teamId);
          membership.setProvisioningEntityId(memberEmail);
          if (addManagerMetadata) {
            membership.assignAttributeValue(managerMetadataName,
                String.valueOf(managerSet.contains(memberEmail)));
          }
          provisioningMemberships.add(membership);
        }
      }

      // role memberships are not returned (no read API; provisioner always pushes on full sync)

      targetData.setProvisioningMemberships(provisioningMemberships);

      return response;
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("retrieveAllData", startNanos));
    }
  }

  // ============================
  // Retrieve single entity (user by email)
  // ============================

  @Override
  public TargetDaoRetrieveEntityResponse retrieveEntity(
      TargetDaoRetrieveEntityRequest targetDaoRetrieveEntityRequest) {

    long startNanos = System.nanoTime();

    try {
      TrueFoundryProvisionerConfiguration config = getTrueFoundryConfiguration();
      String configId = config.getTrueFoundryExternalSystemConfigId();

      String searchAttribute = targetDaoRetrieveEntityRequest.getSearchAttribute();
      String searchValue = GrouperUtil.stringValue(targetDaoRetrieveEntityRequest.getSearchAttributeValue());

      TrueFoundryUser foundUser = null;

      // TrueFoundry supports search by email only (search by ID is not supported)
      if (StringUtils.equals("id", searchAttribute) || StringUtils.equals("email", searchAttribute)) {
        foundUser = TrueFoundryApiCommands.retrieveUserByEmail(configId, searchValue, false);
      }

      ProvisioningEntity targetEntity = foundUser == null ? null : foundUser.toProvisioningEntity();
      return new TargetDaoRetrieveEntityResponse(targetEntity);

    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("retrieveEntity", startNanos));
    }
  }

  // ============================
  // Retrieve single group
  // ============================

  @Override
  public TargetDaoRetrieveGroupResponse retrieveGroup(
      TargetDaoRetrieveGroupRequest targetDaoRetrieveGroupRequest) {

    long startNanos = System.nanoTime();

    try {
      TrueFoundryProvisionerConfiguration config = getTrueFoundryConfiguration();
      String configId = config.getTrueFoundryExternalSystemConfigId();

      ProvisioningGroup grouperTargetGroup = targetDaoRetrieveGroupRequest.getTargetGroup();
      String groupType = grouperTargetGroup == null ? null
          : grouperTargetGroup.retrieveAttributeValueString("groupType");
      String searchAttribute = targetDaoRetrieveGroupRequest.getSearchAttribute();
      String searchValue = GrouperUtil.stringValue(targetDaoRetrieveGroupRequest.getSearchAttributeValue());

      TrueFoundryGroup foundGroup = null;

      if (TrueFoundryGroup.GROUP_TYPE_TEAM.equals(groupType)) {
        if (StringUtils.equals("id", searchAttribute)) {
          foundGroup = TrueFoundryApiCommands.getTeamById(configId, searchValue);
        } else {
          // search by name — retrieve all teams and find by name
          List<TrueFoundryGroup> teams = TrueFoundryApiCommands.retrieveTeams(configId);
          for (TrueFoundryGroup team : GrouperUtil.nonNull(teams)) {
            if (StringUtils.equals("name", searchAttribute)
                && StringUtils.equals(team.getName(), searchValue)) {
              foundGroup = team;
              break;
            }
          }
        }
      } else if (TrueFoundryGroup.GROUP_TYPE_ROLE.equals(groupType)) {
        // retrieve all roles and find by id or name
        Set<String> ignoreRoles = TrueFoundryApiCommands.parseIgnoreSet(config.getTrueFoundryIgnoreRoles());
        List<TrueFoundryGroup> roles = TrueFoundryApiCommands.retrieveRoles(configId, ignoreRoles);
        for (TrueFoundryGroup role : GrouperUtil.nonNull(roles)) {
          if (StringUtils.equals("id", searchAttribute)
              && StringUtils.equals(role.getId(), searchValue)) {
            foundGroup = role;
            break;
          } else if (StringUtils.equals("name", searchAttribute)
              && StringUtils.equals(role.getName(), searchValue)) {
            foundGroup = role;
            break;
          }
        }
      } else {
        throw new RuntimeException("Unknown groupType '" + groupType + "' for retrieveGroup");
      }

      ProvisioningGroup targetGroup = foundGroup == null ? null : foundGroup.toProvisioningGroup();
      return new TargetDaoRetrieveGroupResponse(targetGroup);

    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("retrieveGroup", startNanos));
    }
  }

  // ============================
  // Insert group (teams only — roles are managed in TrueFoundry UI)
  // ============================

  @Override
  public TargetDaoInsertGroupResponse insertGroup(TargetDaoInsertGroupRequest targetDaoInsertGroupRequest) {

    long startNanos = System.nanoTime();
    ProvisioningGroup targetGroup = targetDaoInsertGroupRequest.getTargetGroup();

    try {
      TrueFoundryProvisionerConfiguration config = getTrueFoundryConfiguration();
      String configId = config.getTrueFoundryExternalSystemConfigId();

      TrueFoundryGroup trueFoundryGroup = TrueFoundryGroup.fromProvisioningGroup(targetGroup, null);
      String groupType = trueFoundryGroup.getGroupType();

      TrueFoundryGroup createdGroup;

      if (TrueFoundryGroup.GROUP_TYPE_TEAM.equals(groupType)) {
        // create team — memberships are added via insertMembership after creation
        createdGroup = TrueFoundryApiCommands.createTeam(configId, trueFoundryGroup,
            config.getTrueFoundryDefaultTeamMemberEmail());
      } else if (TrueFoundryGroup.GROUP_TYPE_ROLE.equals(groupType)) {
        // roles are managed in the TrueFoundry UI by administrators
        // creating roles via the provisioner is not supported in normal operation
        createdGroup = TrueFoundryApiCommands.createOrUpdateRole(configId, trueFoundryGroup);
      } else {
        throw new RuntimeException("Invalid groupType: '" + groupType + "', expected 'team' or 'role'");
      }

      if (createdGroup != null && StringUtils.isNotBlank(createdGroup.getId())) {
        targetGroup.setId(createdGroup.getId());
      }
      targetGroup.setProvisioned(true);

      for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(
          targetGroup.getInternal_objectChanges())) {
        provisioningObjectChange.setProvisioned(true);
      }

      return new TargetDaoInsertGroupResponse();
    } catch (Exception e) {
      targetGroup.setProvisioned(false);
      for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(
          targetGroup.getInternal_objectChanges())) {
        provisioningObjectChange.setProvisioned(false);
      }
      throw e;
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("insertGroup", startNanos));
    }
  }

  // ============================
  // Update group (teams: re-PUT manifest; roles: re-PUT manifest)
  // ============================

  @Override
  public TargetDaoUpdateGroupResponse updateGroup(TargetDaoUpdateGroupRequest targetDaoUpdateGroupRequest) {

    long startNanos = System.nanoTime();
    ProvisioningGroup targetGroup = targetDaoUpdateGroupRequest.getTargetGroup();

    try {
      TrueFoundryProvisionerConfiguration config = getTrueFoundryConfiguration();
      String configId = config.getTrueFoundryExternalSystemConfigId();

      Set<String> fieldNamesToUpdate = new HashSet<String>();
      for (ProvisioningObjectChange change : GrouperUtil.nonNull(targetGroup.getInternal_objectChanges())) {
        if (!StringUtils.isBlank(change.getAttributeName())) {
          fieldNamesToUpdate.add(change.getAttributeName());
        }
      }

      TrueFoundryGroup trueFoundryGroup = TrueFoundryGroup.fromProvisioningGroup(targetGroup,
          fieldNamesToUpdate);
      String groupType = trueFoundryGroup.getGroupType();

      if (TrueFoundryGroup.GROUP_TYPE_TEAM.equals(groupType)) {
        // for teams, re-PUT the manifest to update name or other group-level fields
        // membership changes are handled via insertMembership/deleteMembership
        TrueFoundryApiCommands.updateTeam(configId, trueFoundryGroup);
      } else if (TrueFoundryGroup.GROUP_TYPE_ROLE.equals(groupType)) {
        TrueFoundryApiCommands.createOrUpdateRole(configId, trueFoundryGroup);
      } else {
        throw new RuntimeException("Invalid groupType: '" + groupType + "', expected 'team' or 'role'");
      }

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
  // Delete group (teams: DELETE; roles: DELETE — though roles are normally UI-managed)
  // ============================

  @Override
  public TargetDaoDeleteGroupResponse deleteGroup(TargetDaoDeleteGroupRequest targetDaoDeleteGroupRequest) {

    long startNanos = System.nanoTime();
    ProvisioningGroup targetGroup = targetDaoDeleteGroupRequest.getTargetGroup();

    try {
      TrueFoundryProvisionerConfiguration config = getTrueFoundryConfiguration();
      String configId = config.getTrueFoundryExternalSystemConfigId();

      TrueFoundryGroup trueFoundryGroup = TrueFoundryGroup.fromProvisioningGroup(targetGroup, null);
      String groupType = trueFoundryGroup.getGroupType();

      if (TrueFoundryGroup.GROUP_TYPE_TEAM.equals(groupType)) {
        TrueFoundryApiCommands.deleteTeam(configId, trueFoundryGroup.getId());
      } else if (TrueFoundryGroup.GROUP_TYPE_ROLE.equals(groupType)) {
        TrueFoundryApiCommands.deleteRole(configId, trueFoundryGroup.getId());
      } else {
        throw new RuntimeException("Invalid groupType: '" + groupType + "', expected 'team' or 'role'");
      }

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
  // Insert entity (register user, then look up to get ID)
  // ============================

  @Override
  public TargetDaoInsertEntityResponse insertEntity(TargetDaoInsertEntityRequest targetDaoInsertEntityRequest) {

    long startNanos = System.nanoTime();
    ProvisioningEntity targetEntity = targetDaoInsertEntityRequest.getTargetEntity();

    try {
      TrueFoundryProvisionerConfiguration config = getTrueFoundryConfiguration();
      String configId = config.getTrueFoundryExternalSystemConfigId();

      TrueFoundryUser trueFoundryUser = TrueFoundryUser.fromProvisioningEntity(targetEntity, null);
      String email = trueFoundryUser.getEmail();

      if (StringUtils.isBlank(email)) {
        throw new RuntimeException("user email is required for insertEntity");
      }

      // create the user (or reactivate if inactive), set display name if SCIM is configured
      TrueFoundryApiCommands.createUser(configId, trueFoundryUser,
          config.getTrueFoundryScimTenantName(), config.getTrueFoundryScimSsoId());

      // entity ID = email (TrueFoundry is email-based)
      targetEntity.setId(email);
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
  // Update entity (display name via SCIM, active via deactivate/activate)
  // ============================

  @Override
  public TargetDaoUpdateEntityResponse updateEntity(TargetDaoUpdateEntityRequest targetDaoUpdateEntityRequest) {

    long startNanos = System.nanoTime();
    ProvisioningEntity targetEntity = targetDaoUpdateEntityRequest.getTargetEntity();

    try {
      TrueFoundryProvisionerConfiguration config = getTrueFoundryConfiguration();
      String configId = config.getTrueFoundryExternalSystemConfigId();

      Set<String> fieldNamesToUpdate = new LinkedHashSet<String>();
      for (ProvisioningObjectChange change : GrouperUtil.nonNull(targetEntity.getInternal_objectChanges())) {
        if (!StringUtils.isBlank(change.getAttributeName())) {
          fieldNamesToUpdate.add(change.getAttributeName());
        }
      }

      TrueFoundryUser trueFoundryUser = TrueFoundryUser.fromProvisioningEntity(targetEntity,
          fieldNamesToUpdate);

      // update display name via SCIM PATCH (only if SCIM is configured and userId is available)
      if (fieldNamesToUpdate.contains("displayName") && config.isScimDisplayNameConfigured()) {
        String userId = trueFoundryUser.getId();
        if (StringUtils.isNotBlank(userId)) {
          TrueFoundryApiCommands.updateUserDisplayName(configId,
              config.getTrueFoundryScimTenantName(),
              config.getTrueFoundryScimSsoId(),
              userId,
              trueFoundryUser.getDisplayName());
        }
      }

      // handle active state changes
      if (fieldNamesToUpdate.contains("active")) {
        String email = trueFoundryUser.getEmail();
        if (StringUtils.isNotBlank(email)) {
          Boolean active = trueFoundryUser.getActive();
          if (active != null && active) {
            TrueFoundryApiCommands.activateUser(configId, email);
          } else {
            TrueFoundryApiCommands.deactivateUser(configId, email);
          }
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

  // ============================
  // Delete entity (deactivate — hard delete is blocked if user has team memberships)
  // ============================

  @Override
  public TargetDaoDeleteEntityResponse deleteEntity(TargetDaoDeleteEntityRequest targetDaoDeleteEntityRequest) {

    long startNanos = System.nanoTime();
    ProvisioningEntity targetEntity = targetDaoDeleteEntityRequest.getTargetEntity();

    try {
      TrueFoundryProvisionerConfiguration config = getTrueFoundryConfiguration();
      String configId = config.getTrueFoundryExternalSystemConfigId();

      TrueFoundryUser trueFoundryUser = TrueFoundryUser.fromProvisioningEntity(targetEntity, null);
      String email = trueFoundryUser.getEmail();

      if (StringUtils.isBlank(email)) {
        throw new RuntimeException("user email is required for deleteEntity");
      }

      // deactivate instead of hard delete — hard delete is blocked if user has team memberships
      TrueFoundryApiCommands.deactivateUser(configId, email);

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

  // ============================
  // Insert memberships (plural)
  //   teams: group by teamId, one GET + PUT per team for all members being added
  //   roles: assign role to each user via PATCH /users/roles
  // ============================

  @Override
  public TargetDaoInsertMembershipsResponse insertMemberships(
      TargetDaoInsertMembershipsRequest targetDaoInsertMembershipsRequest) {

    long startNanos = System.nanoTime();
    List<ProvisioningMembership> targetMemberships =
        targetDaoInsertMembershipsRequest.getTargetMemberships();

    try {
      TrueFoundryProvisionerConfiguration config = getTrueFoundryConfiguration();
      String configId = config.getTrueFoundryExternalSystemConfigId();

      // group team memberships by teamId: teamId -> (managerEmails, regularMemberEmails, memberships)
      Map<String, List<String>> teamManagerEmails = new LinkedHashMap<String, List<String>>();
      Map<String, List<String>> teamRegularMemberEmails = new LinkedHashMap<String, List<String>>();
      Map<String, List<ProvisioningMembership>> teamMemberships =
          new LinkedHashMap<String, List<ProvisioningMembership>>();

      for (ProvisioningMembership targetMembership : GrouperUtil.nonNull(targetMemberships)) {
        ProvisioningGroup provisioningGroup = targetMembership.getProvisioningGroup();
        String groupType = provisioningGroup == null ? null
            : provisioningGroup.retrieveAttributeValueString("groupType");
        String groupId = targetMembership.getProvisioningGroupId();
        String userEmail = targetMembership.getProvisioningEntityId();

        if (TrueFoundryGroup.GROUP_TYPE_TEAM.equals(groupType)) {
          boolean isManager = false;
          if (config.isTrueFoundryAddTeamManagerMetadata()) {
            String managerMetadata = targetMembership
                .retrieveAttributeValueString(config.getTrueFoundryTeamManagerMetadataName());
            isManager = "true".equalsIgnoreCase(managerMetadata)
                || "T".equalsIgnoreCase(managerMetadata)
                || "1".equals(managerMetadata);
          }

          if (!teamMemberships.containsKey(groupId)) {
            teamManagerEmails.put(groupId, new ArrayList<String>());
            teamRegularMemberEmails.put(groupId, new ArrayList<String>());
            teamMemberships.put(groupId, new ArrayList<ProvisioningMembership>());
          }
          if (isManager) {
            teamManagerEmails.get(groupId).add(userEmail);
          } else {
            teamRegularMemberEmails.get(groupId).add(userEmail);
          }
          teamMemberships.get(groupId).add(targetMembership);

        } else if (TrueFoundryGroup.GROUP_TYPE_ROLE.equals(groupType)) {
          // role assignment — no batch API, process individually
          String roleName = provisioningGroup.retrieveAttributeValueString("name");
          try {
            TrueFoundryApiCommands.assignUserRole(configId, userEmail, roleName);
            targetMembership.setProvisioned(true);
            for (ProvisioningObjectChange change : GrouperUtil.nonNull(
                targetMembership.getInternal_objectChanges())) {
              change.setProvisioned(true);
            }
          } catch (Exception e) {
            targetMembership.setProvisioned(false);
            for (ProvisioningObjectChange change : GrouperUtil.nonNull(
                targetMembership.getInternal_objectChanges())) {
              change.setProvisioned(false);
            }
          }

        } else {
          throw new RuntimeException(
              "Invalid groupType: '" + groupType + "', expected 'team' or 'role'");
        }
      }

      // process each team with a single GET + PUT
      for (String teamId : teamMemberships.keySet()) {
        List<ProvisioningMembership> memberships = teamMemberships.get(teamId);
        try {
          TrueFoundryApiCommands.addTeamMembers(configId, teamId,
              teamManagerEmails.get(teamId), teamRegularMemberEmails.get(teamId));
          for (ProvisioningMembership m : memberships) {
            m.setProvisioned(true);
            for (ProvisioningObjectChange change : GrouperUtil.nonNull(
                m.getInternal_objectChanges())) {
              change.setProvisioned(true);
            }
          }
        } catch (Exception e) {
          for (ProvisioningMembership m : memberships) {
            m.setProvisioned(false);
            for (ProvisioningObjectChange change : GrouperUtil.nonNull(
                m.getInternal_objectChanges())) {
              change.setProvisioned(false);
            }
          }
        }
      }

      return new TargetDaoInsertMembershipsResponse();
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("insertMemberships", startNanos));
    }
  }

  // ============================
  // Delete memberships (plural)
  //   teams: group by teamId, one GET + PUT per team for all members being removed
  //   roles: assign default role to each user via PATCH /users/roles
  // ============================

  @Override
  public TargetDaoDeleteMembershipsResponse deleteMemberships(
      TargetDaoDeleteMembershipsRequest targetDaoDeleteMembershipsRequest) {

    long startNanos = System.nanoTime();
    List<ProvisioningMembership> targetMemberships =
        targetDaoDeleteMembershipsRequest.getTargetMemberships();

    try {
      TrueFoundryProvisionerConfiguration config = getTrueFoundryConfiguration();
      String configId = config.getTrueFoundryExternalSystemConfigId();

      // group team memberships by teamId: teamId -> (emailsToRemove, memberships)
      Map<String, List<String>> teamEmailsToRemove = new LinkedHashMap<String, List<String>>();
      Map<String, List<ProvisioningMembership>> teamMemberships =
          new LinkedHashMap<String, List<ProvisioningMembership>>();

      for (ProvisioningMembership targetMembership : GrouperUtil.nonNull(targetMemberships)) {
        ProvisioningGroup provisioningGroup = targetMembership.getProvisioningGroup();
        String groupType = provisioningGroup == null ? null
            : provisioningGroup.retrieveAttributeValueString("groupType");
        String groupId = targetMembership.getProvisioningGroupId();
        String userEmail = targetMembership.getProvisioningEntityId();

        if (TrueFoundryGroup.GROUP_TYPE_TEAM.equals(groupType)) {
          if (!teamMemberships.containsKey(groupId)) {
            teamEmailsToRemove.put(groupId, new ArrayList<String>());
            teamMemberships.put(groupId, new ArrayList<ProvisioningMembership>());
          }
          teamEmailsToRemove.get(groupId).add(userEmail);
          teamMemberships.get(groupId).add(targetMembership);

        } else if (TrueFoundryGroup.GROUP_TYPE_ROLE.equals(groupType)) {
          // role delete is a no-op — TrueFoundry users always have exactly one role,
          // and assigning a new role (via insertMemberships) replaces the old one.
          // There is no need to explicitly remove a role assignment.
          targetMembership.setProvisioned(true);
          for (ProvisioningObjectChange change : GrouperUtil.nonNull(
              targetMembership.getInternal_objectChanges())) {
            change.setProvisioned(true);
          }

        } else {
          throw new RuntimeException(
              "Invalid groupType: '" + groupType + "' for deleteMemberships"
              + ", provisioningGroup=" + (provisioningGroup == null ? "null" : provisioningGroup.toString())
              + ", groupId='" + groupId + "', userEmail='" + userEmail + "'"
              + ", expected 'team' or 'role'");
        }
      }

      // process each team with a single GET + PUT
      for (String teamId : teamMemberships.keySet()) {
        List<ProvisioningMembership> memberships = teamMemberships.get(teamId);
        try {
          TrueFoundryApiCommands.removeTeamMembers(configId, teamId,
              teamEmailsToRemove.get(teamId), config.getTrueFoundryDefaultTeamMemberEmail());
          for (ProvisioningMembership m : memberships) {
            m.setProvisioned(true);
            for (ProvisioningObjectChange change : GrouperUtil.nonNull(
                m.getInternal_objectChanges())) {
              change.setProvisioned(true);
            }
          }
        } catch (Exception e) {
          for (ProvisioningMembership m : memberships) {
            m.setProvisioned(false);
            for (ProvisioningObjectChange change : GrouperUtil.nonNull(
                m.getInternal_objectChanges())) {
              change.setProvisioned(false);
            }
          }
        }
      }

      return new TargetDaoDeleteMembershipsResponse();
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("deleteMemberships", startNanos));
    }
  }

  // ============================
  // DAO capabilities
  // ============================

  @Override
  public void registerGrouperProvisionerDaoCapabilities(
      GrouperProvisionerDaoCapabilities grouperProvisionerDaoCapabilities) {
    grouperProvisionerDaoCapabilities.setCanDeleteEntity(true);
    grouperProvisionerDaoCapabilities.setCanDeleteGroup(true);
    grouperProvisionerDaoCapabilities.setCanDeleteMemberships(true);
    grouperProvisionerDaoCapabilities.setCanInsertEntity(true);
    grouperProvisionerDaoCapabilities.setCanInsertGroup(true);
    grouperProvisionerDaoCapabilities.setCanInsertMemberships(true);
    grouperProvisionerDaoCapabilities.setCanRetrieveAllData(true);
    grouperProvisionerDaoCapabilities.setCanRetrieveEntity(true);
    grouperProvisionerDaoCapabilities.setCanRetrieveGroup(true);
    grouperProvisionerDaoCapabilities.setCanUpdateEntity(true);
    grouperProvisionerDaoCapabilities.setCanUpdateGroup(true);
  }

}
