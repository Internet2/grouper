package edu.internet2.middleware.grouper.app.datadog;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.app.provisioning.ProvisioningEntity;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningObjectChangeAction;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningGroup;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningGroupWrapper;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningMembership;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningObjectChange;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.GrouperProvisionerDaoCapabilities;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.GrouperProvisionerTargetDaoBase;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoDeleteGroupRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoDeleteGroupResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoDeleteMembershipRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoDeleteMembershipResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoInsertGroupRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoInsertGroupResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoInsertMembershipRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoInsertMembershipResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveAllEntitiesRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveAllEntitiesResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveEntityRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveEntityResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveAllGroupsRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveAllGroupsResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveGroupRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveGroupResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveMembershipsByGroupRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveMembershipsByGroupResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoTimingInfo;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoInsertEntityRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoInsertEntityResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoUpdateEntityRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoUpdateEntityResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoDeleteEntityRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoDeleteEntityResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoUpdateGroupRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoUpdateGroupResponse;
import edu.internet2.middleware.grouper.util.GrouperHttpClient;
import edu.internet2.middleware.grouper.util.GrouperHttpClientLog;
import edu.internet2.middleware.grouper.util.GrouperUtil;

public class DatadogTargetDao extends GrouperProvisionerTargetDaoBase {

  @Override
  public boolean loggingStart() {
    return GrouperHttpClient.logStart(new GrouperHttpClientLog());
  }

  @Override
  public String loggingStop() {
    return GrouperHttpClient.logEnd();
  }

  private DatadogProvisionerConfiguration getDatadogConfiguration() {
    return (DatadogProvisionerConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
  }

  private DatadogSettings buildDatadogSettings() {
    DatadogSettings datadogSettings = new DatadogSettings();
    datadogSettings.loadFromDatadogProvisionerConfiguration(getDatadogConfiguration());
    return datadogSettings;
  }

  @Override
  public TargetDaoRetrieveAllGroupsResponse retrieveAllGroups(
      TargetDaoRetrieveAllGroupsRequest targetDaoRetrieveAllGroupsRequest) {

    long startNanos = System.nanoTime();

    try {
      DatadogProvisionerConfiguration datadogConfiguration = getDatadogConfiguration();
      String configId = datadogConfiguration.getDatadogExternalSystemConfigId();
      DatadogSettings datadogSettings = buildDatadogSettings();

      List<ProvisioningGroup> results = new ArrayList<ProvisioningGroup>();

      // retrieve roles
      List<DatadogGroup> roles = DatadogApiCommands.retrieveRoles(configId, datadogSettings);
      for (DatadogGroup role : GrouperUtil.nonNull(roles)) {
        role.setGroupType("role");
        ProvisioningGroup targetGroup = role.toProvisioningGroup();
        results.add(targetGroup);
      }

      // retrieve teams
      boolean lookupAdmins = datadogConfiguration.isDatadogAddTeamAdminMetadata();
      List<DatadogGroup> teams = DatadogApiCommands.retrieveTeams(configId, datadogSettings);
      for (DatadogGroup team : GrouperUtil.nonNull(teams)) {
        team.setGroupType("team");
        if (lookupAdmins) {
          List<DatadogMembership> memberships = DatadogApiCommands.getTeamMemberships(configId, datadogSettings, team.getId());
          Set<String> adminUserIds = new LinkedHashSet<String>();
          for (DatadogMembership membership : GrouperUtil.nonNull(memberships)) {
            if ("admin".equals(membership.getRole())) {
              adminUserIds.add(membership.getUserId());
            }
          }
          team.setAdmins(adminUserIds);
        }
        ProvisioningGroup targetGroup = team.toProvisioningGroup();
        results.add(targetGroup);
      }

      return new TargetDaoRetrieveAllGroupsResponse(results);
    } finally {
      this.addTargetDaoTimingInfo(
          new TargetDaoTimingInfo("retrieveAllGroups", startNanos));
    }
  }

  @Override
  public TargetDaoRetrieveAllEntitiesResponse retrieveAllEntities(
      TargetDaoRetrieveAllEntitiesRequest targetDaoRetrieveAllEntitiesRequest) {

    long startNanos = System.nanoTime();

    try {
      DatadogProvisionerConfiguration datadogConfiguration = getDatadogConfiguration();
      String configId = datadogConfiguration.getDatadogExternalSystemConfigId();
      DatadogSettings datadogSettings = buildDatadogSettings();

      List<ProvisioningEntity> results = new ArrayList<ProvisioningEntity>();

      List<DatadogUser> datadogUsers = DatadogApiCommands.retrieveUsers(configId, datadogSettings);

      for (DatadogUser datadogUser : GrouperUtil.nonNull(datadogUsers)) {
        ProvisioningEntity targetEntity = datadogUser.toProvisioningEntity();
        results.add(targetEntity);
      }

      return new TargetDaoRetrieveAllEntitiesResponse(results);
    } finally {
      this.addTargetDaoTimingInfo(
          new TargetDaoTimingInfo("retrieveAllEntities", startNanos));
    }
  }

  @Override
  public TargetDaoRetrieveEntityResponse retrieveEntity(TargetDaoRetrieveEntityRequest targetDaoRetrieveEntityRequest) {

    long startNanos = System.nanoTime();

    try {
      DatadogProvisionerConfiguration datadogConfiguration = getDatadogConfiguration();
      String configId = datadogConfiguration.getDatadogExternalSystemConfigId();
      DatadogSettings datadogSettings = buildDatadogSettings();

      String searchAttribute = targetDaoRetrieveEntityRequest.getSearchAttribute();
      String searchAttributeValue = GrouperUtil.stringValue(targetDaoRetrieveEntityRequest.getSearchAttributeValue());

      DatadogUser foundUser = null;

      List<DatadogUser> datadogUsers = DatadogApiCommands.retrieveUsers(configId, datadogSettings);
      for (DatadogUser datadogUser : GrouperUtil.nonNull(datadogUsers)) {
        if (StringUtils.equals("id", searchAttribute) && StringUtils.equals(datadogUser.getId(), searchAttributeValue)) {
          foundUser = datadogUser;
          break;
        } else if (StringUtils.equals("email", searchAttribute) && StringUtils.equals(datadogUser.getEmail(), searchAttributeValue)) {
          foundUser = datadogUser;
          break;
        }
      }

      ProvisioningEntity targetEntity = foundUser == null ? null : foundUser.toProvisioningEntity();

      return new TargetDaoRetrieveEntityResponse(targetEntity);

    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("retrieveEntity", startNanos));
    }
  }

  @Override
  public TargetDaoRetrieveGroupResponse retrieveGroup(TargetDaoRetrieveGroupRequest targetDaoRetrieveGroupRequest) {

    long startNanos = System.nanoTime();

    try {
      DatadogProvisionerConfiguration datadogConfiguration = getDatadogConfiguration();
      String configId = datadogConfiguration.getDatadogExternalSystemConfigId();
      DatadogSettings datadogSettings = buildDatadogSettings();

      String searchAttribute = targetDaoRetrieveGroupRequest.getSearchAttribute();
      String searchAttributeValue = GrouperUtil.stringValue(targetDaoRetrieveGroupRequest.getSearchAttributeValue());

      // determine groupType from the target group
      ProvisioningGroup grouperTargetGroup = targetDaoRetrieveGroupRequest.getTargetGroup();
      String groupType = grouperTargetGroup == null ? null : grouperTargetGroup.retrieveAttributeValueString("groupType");

      DatadogGroup foundGroup = null;

      if ("team".equals(groupType)) {
        List<DatadogGroup> teams = DatadogApiCommands.retrieveTeams(configId, datadogSettings);
        for (DatadogGroup team : GrouperUtil.nonNull(teams)) {
          team.setGroupType("team");
          if (StringUtils.equals("id", searchAttribute) && StringUtils.equals(team.getId(), searchAttributeValue)) {
            foundGroup = team;
            break;
          } else if (StringUtils.equals("name", searchAttribute) && StringUtils.equals(team.getName(), searchAttributeValue)) {
            foundGroup = team;
            break;
          }
        }
      } else if ("role".equals(groupType)) {
        List<DatadogGroup> roles = DatadogApiCommands.retrieveRoles(configId, datadogSettings);
        for (DatadogGroup role : GrouperUtil.nonNull(roles)) {
          role.setGroupType("role");
          if (StringUtils.equals("id", searchAttribute) && StringUtils.equals(role.getId(), searchAttributeValue)) {
            foundGroup = role;
            break;
          } else if (StringUtils.equals("name", searchAttribute) && StringUtils.equals(role.getName(), searchAttributeValue)) {
            foundGroup = role;
            break;
          }
        }
      } else {
        throw new RuntimeException("Not expecting groupType '" + groupType + "' for retrieveGroup search");
      }

      ProvisioningGroup targetGroup = foundGroup == null ? null : foundGroup.toProvisioningGroup();

      return new TargetDaoRetrieveGroupResponse(targetGroup);

    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("retrieveGroup", startNanos));
    }
  }

  @Override
  public TargetDaoInsertGroupResponse insertGroup(TargetDaoInsertGroupRequest targetDaoInsertGroupRequest) {

    long startNanos = System.nanoTime();
    ProvisioningGroup targetGroup = targetDaoInsertGroupRequest.getTargetGroup();

    try {
      DatadogProvisionerConfiguration datadogConfiguration = getDatadogConfiguration();
      String configId = datadogConfiguration.getDatadogExternalSystemConfigId();
      DatadogSettings datadogSettings = buildDatadogSettings();

      DatadogGroup datadogGroup = DatadogGroup.fromProvisioningGroup(targetGroup, null);

      DatadogGroup createdGroup;
      if ("team".equals(datadogGroup.getGroupType())) {
        createdGroup = DatadogApiCommands.createTeam(configId, datadogSettings, datadogGroup);
      } else if ("role".equals(datadogGroup.getGroupType())) {
        createdGroup = DatadogApiCommands.createRole(configId, datadogSettings, datadogGroup);
      } else {
        throw new RuntimeException("Invalid groupType: '" + datadogGroup.getGroupType() + "', expected 'team' or 'role'");
      }

      targetGroup.setId(createdGroup.getId());
      targetGroup.setProvisioned(true);

      for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetGroup.getInternal_objectChanges())) {
        provisioningObjectChange.setProvisioned(true);
      }

      return new TargetDaoInsertGroupResponse();
    } catch (Exception e) {
      targetGroup.setProvisioned(false);
      for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetGroup.getInternal_objectChanges())) {
        provisioningObjectChange.setProvisioned(false);
      }
      throw e;
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("insertGroup", startNanos));
    }
  }

  @Override
  public TargetDaoUpdateGroupResponse updateGroup(TargetDaoUpdateGroupRequest targetDaoUpdateGroupRequest) {

    long startNanos = System.nanoTime();
    ProvisioningGroup targetGroup = targetDaoUpdateGroupRequest.getTargetGroup();

    try {
      DatadogProvisionerConfiguration datadogConfiguration = getDatadogConfiguration();
      String configId = datadogConfiguration.getDatadogExternalSystemConfigId();
      DatadogSettings datadogSettings = buildDatadogSettings();

      Set<String> fieldNamesToUpdate = new HashSet<String>();
      for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetGroup.getInternal_objectChanges())) {
        String fieldName = provisioningObjectChange.getAttributeName();
        fieldNamesToUpdate.add(fieldName);
      }

      DatadogGroup datadogGroup = DatadogGroup.fromProvisioningGroup(targetGroup, fieldNamesToUpdate);

      if ("team".equals(datadogGroup.getGroupType())) {
        DatadogApiCommands.updateTeam(configId, datadogSettings, datadogGroup, fieldNamesToUpdate);
      } else if ("role".equals(datadogGroup.getGroupType())) {
        DatadogApiCommands.updateRole(configId, datadogSettings, datadogGroup, fieldNamesToUpdate);
      } else {
        throw new RuntimeException("Invalid groupType: '" + datadogGroup.getGroupType() + "', expected 'team' or 'role'");
      }

      targetGroup.setProvisioned(true);

      // handle admins attribute changes (Google-style: admins as multi-valued group attribute)
      if ("team".equals(datadogGroup.getGroupType()) && datadogConfiguration.isDatadogAddTeamAdminMetadata()) {
        for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetGroup.getInternal_objectChanges())) {
          String fieldName = provisioningObjectChange.getAttributeName();
          if (StringUtils.equals(fieldName, "admins")) {
            if (provisioningObjectChange.getProvisioningObjectChangeAction() == ProvisioningObjectChangeAction.insert) {
              String userId = (String) provisioningObjectChange.getNewValue();
              if (StringUtils.isNotBlank(userId)) {
                DatadogApiCommands.updateTeamMembershipRole(configId, datadogSettings, datadogGroup.getId(), userId, "admin");
              }
            } else if (provisioningObjectChange.getProvisioningObjectChangeAction() == ProvisioningObjectChangeAction.delete) {
              String userId = (String) provisioningObjectChange.getOldValue();
              if (StringUtils.isNotBlank(userId)) {
                DatadogApiCommands.updateTeamMembershipRole(configId, datadogSettings, datadogGroup.getId(), userId, null);
              }
            }
          }
        }
      }

      for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetGroup.getInternal_objectChanges())) {
        provisioningObjectChange.setProvisioned(true);
      }

      return new TargetDaoUpdateGroupResponse();
    } catch (Exception e) {
      targetGroup.setProvisioned(false);
      for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetGroup.getInternal_objectChanges())) {
        provisioningObjectChange.setProvisioned(false);
      }
      throw e;
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("updateGroup", startNanos));
    }
  }

  @Override
  public TargetDaoDeleteGroupResponse deleteGroup(TargetDaoDeleteGroupRequest targetDaoDeleteGroupRequest) {

    long startNanos = System.nanoTime();
    ProvisioningGroup targetGroup = targetDaoDeleteGroupRequest.getTargetGroup();

    try {
      DatadogProvisionerConfiguration datadogConfiguration = getDatadogConfiguration();
      String configId = datadogConfiguration.getDatadogExternalSystemConfigId();
      DatadogSettings datadogSettings = buildDatadogSettings();

      DatadogGroup datadogGroup = DatadogGroup.fromProvisioningGroup(targetGroup, null);

      if ("team".equals(datadogGroup.getGroupType())) {
        DatadogApiCommands.deleteTeam(configId, datadogSettings, datadogGroup);
      } else if ("role".equals(datadogGroup.getGroupType())) {
        DatadogApiCommands.deleteRole(configId, datadogSettings, datadogGroup);
      } else {
        throw new RuntimeException("Invalid groupType: '" + datadogGroup.getGroupType() + "', expected 'team' or 'role'");
      }

      targetGroup.setProvisioned(true);
      for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetGroup.getInternal_objectChanges())) {
        provisioningObjectChange.setProvisioned(true);
      }

      return new TargetDaoDeleteGroupResponse();
    } catch (Exception e) {
      targetGroup.setProvisioned(false);
      for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetGroup.getInternal_objectChanges())) {
        provisioningObjectChange.setProvisioned(false);
      }
      throw e;
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("deleteGroup", startNanos));
    }
  }

  @Override
  public TargetDaoInsertMembershipResponse insertMembership(TargetDaoInsertMembershipRequest targetDaoInsertMembershipRequest) {

    long startNanos = System.nanoTime();
    ProvisioningMembership targetMembership = targetDaoInsertMembershipRequest.getTargetMembership();

    try {
      DatadogProvisionerConfiguration datadogConfiguration = getDatadogConfiguration();
      String configId = datadogConfiguration.getDatadogExternalSystemConfigId();
      DatadogSettings datadogSettings = buildDatadogSettings();

      String groupId = targetMembership.getProvisioningGroupId();
      String userId = targetMembership.getProvisioningEntityId();

      // determine if role or team membership based on group's groupType attribute
      ProvisioningGroup provisioningGroup = targetMembership.getProvisioningGroup();
      String groupType = provisioningGroup == null ? null : provisioningGroup.retrieveAttributeValueString("groupType");

      if ("team".equals(groupType)) {
        DatadogApiCommands.addUserToTeam(configId, datadogSettings, groupId, userId);
      } else if ("role".equals(groupType)) {
        DatadogApiCommands.addUserToRole(configId, datadogSettings, groupId, userId);
      } else {
        throw new RuntimeException("Invalid groupType: '" + groupType + "', expected 'team' or 'role'");
      }

      targetMembership.setProvisioned(true);
      for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetMembership.getInternal_objectChanges())) {
        provisioningObjectChange.setProvisioned(true);
      }

      return new TargetDaoInsertMembershipResponse();
    } catch (Exception e) {
      targetMembership.setProvisioned(false);
      for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetMembership.getInternal_objectChanges())) {
        provisioningObjectChange.setProvisioned(false);
      }
      throw e;
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("insertMembership", startNanos));
    }
  }

  @Override
  public TargetDaoDeleteMembershipResponse deleteMembership(TargetDaoDeleteMembershipRequest targetDaoDeleteMembershipRequest) {

    long startNanos = System.nanoTime();
    ProvisioningMembership targetMembership = targetDaoDeleteMembershipRequest.getTargetMembership();

    try {
      DatadogProvisionerConfiguration datadogConfiguration = getDatadogConfiguration();
      String configId = datadogConfiguration.getDatadogExternalSystemConfigId();
      DatadogSettings datadogSettings = buildDatadogSettings();

      String groupId = targetMembership.getProvisioningGroupId();
      String userId = targetMembership.getProvisioningEntityId();

      ProvisioningGroup provisioningGroup = targetMembership.getProvisioningGroup();
      String groupType = provisioningGroup == null ? null : provisioningGroup.retrieveAttributeValueString("groupType");

      if ("team".equals(groupType)) {
        DatadogApiCommands.removeUserFromTeam(configId, datadogSettings, groupId, userId);
      } else if ("role".equals(groupType)) {
        DatadogApiCommands.removeUserFromRole(configId, datadogSettings, groupId, userId);
      } else {
        throw new RuntimeException("Invalid groupType: '" + groupType + "', expected 'team' or 'role'");
      }

      targetMembership.setProvisioned(true);
      for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetMembership.getInternal_objectChanges())) {
        provisioningObjectChange.setProvisioned(true);
      }

      return new TargetDaoDeleteMembershipResponse();
    } catch (Exception e) {
      targetMembership.setProvisioned(false);
      for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetMembership.getInternal_objectChanges())) {
        provisioningObjectChange.setProvisioned(false);
      }
      throw new RuntimeException("Failed to delete Datadog membership (groupId '" + targetMembership.getProvisioningGroupId() + "', userId '" + targetMembership.getProvisioningEntityId() + "')", e);
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("deleteMembership", startNanos));
    }
  }

  @Override
  public TargetDaoRetrieveMembershipsByGroupResponse retrieveMembershipsByGroup(
      TargetDaoRetrieveMembershipsByGroupRequest targetDaoRetrieveMembershipsByGroupRequest) {

    long startNanos = System.nanoTime();
    ProvisioningGroup targetGroup = targetDaoRetrieveMembershipsByGroupRequest.getTargetGroup();

    try {
      DatadogProvisionerConfiguration datadogConfiguration = getDatadogConfiguration();
      String configId = datadogConfiguration.getDatadogExternalSystemConfigId();
      DatadogSettings datadogSettings = buildDatadogSettings();

      String groupId = targetGroup.getId();

      if (StringUtils.isBlank(groupId)) {
        return new TargetDaoRetrieveMembershipsByGroupResponse(new ArrayList<ProvisioningMembership>());
      }

      String groupType = targetGroup.retrieveAttributeValueString("groupType");

      List<ProvisioningMembership> provisioningMemberships = new ArrayList<ProvisioningMembership>();

      if ("team".equals(groupType)) {
        List<DatadogMembership> teamMemberships = DatadogApiCommands.getTeamMemberships(configId, datadogSettings, groupId);
        for (DatadogMembership membership : GrouperUtil.nonNull(teamMemberships)) {
          ProvisioningMembership targetMembership = new ProvisioningMembership(false);
          targetMembership.setProvisioningGroupId(groupId);
          targetMembership.setProvisioningEntityId(membership.getUserId());
          provisioningMemberships.add(targetMembership);
        }
      } else if ("role".equals(groupType)) {
        List<DatadogUser> roleUsers = DatadogApiCommands.getRoleUsers(configId, datadogSettings, groupId);
        for (DatadogUser user : GrouperUtil.nonNull(roleUsers)) {
          ProvisioningMembership targetMembership = new ProvisioningMembership(false);
          targetMembership.setProvisioningGroupId(groupId);
          targetMembership.setProvisioningEntityId(user.getId());
          provisioningMemberships.add(targetMembership);
        }
      } else {
        throw new RuntimeException("Invalid groupType: '" + groupType + "', expected 'team' or 'role'");
      }

      return new TargetDaoRetrieveMembershipsByGroupResponse(provisioningMemberships);
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("retrieveMembershipsByGroup", startNanos));
    }
  }

  @Override
  public TargetDaoInsertEntityResponse insertEntity(TargetDaoInsertEntityRequest targetDaoInsertEntityRequest) {

    long startNanos = System.nanoTime();
    ProvisioningEntity targetEntity = targetDaoInsertEntityRequest.getTargetEntity();

    try {
      DatadogProvisionerConfiguration datadogConfiguration = getDatadogConfiguration();
      String configId = datadogConfiguration.getDatadogExternalSystemConfigId();
      DatadogSettings datadogSettings = buildDatadogSettings();

      DatadogUser datadogUser = DatadogUser.fromProvisioningEntity(targetEntity, null);

      DatadogUser createdUser = DatadogApiCommands.createUser(configId, datadogSettings, datadogUser);

      targetEntity.setId(createdUser.getId());
      targetEntity.setProvisioned(true);

      for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetEntity.getInternal_objectChanges())) {
        provisioningObjectChange.setProvisioned(true);
      }

      return new TargetDaoInsertEntityResponse();
    } catch (Exception e) {
      targetEntity.setProvisioned(false);
      for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetEntity.getInternal_objectChanges())) {
        provisioningObjectChange.setProvisioned(false);
      }
      throw e;
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("insertEntity", startNanos));
    }
  }

  @Override
  public TargetDaoUpdateEntityResponse updateEntity(TargetDaoUpdateEntityRequest targetDaoUpdateEntityRequest) {

    long startNanos = System.nanoTime();
    ProvisioningEntity targetEntity = targetDaoUpdateEntityRequest.getTargetEntity();

    try {
      DatadogProvisionerConfiguration datadogConfiguration = getDatadogConfiguration();
      String configId = datadogConfiguration.getDatadogExternalSystemConfigId();
      DatadogSettings datadogSettings = buildDatadogSettings();

      Set<String> fieldNamesToUpdate = new LinkedHashSet<String>();
      for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetEntity.getInternal_objectChanges())) {
        String fieldName = provisioningObjectChange.getAttributeName();
        if (!StringUtils.isBlank(fieldName)) {
          fieldNamesToUpdate.add(fieldName);
        }
      }

      DatadogUser datadogUser = DatadogUser.fromProvisioningEntity(targetEntity, fieldNamesToUpdate);

      DatadogApiCommands.updateUser(configId, datadogSettings, datadogUser, fieldNamesToUpdate);

      targetEntity.setProvisioned(true);

      for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetEntity.getInternal_objectChanges())) {
        provisioningObjectChange.setProvisioned(true);
      }

      return new TargetDaoUpdateEntityResponse();
    } catch (Exception e) {
      targetEntity.setProvisioned(false);
      for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetEntity.getInternal_objectChanges())) {
        provisioningObjectChange.setProvisioned(false);
      }
      throw e;
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("updateEntity", startNanos));
    }
  }

  @Override
  public TargetDaoDeleteEntityResponse deleteEntity(TargetDaoDeleteEntityRequest targetDaoDeleteEntityRequest) {

    long startNanos = System.nanoTime();
    ProvisioningEntity targetEntity = targetDaoDeleteEntityRequest.getTargetEntity();

    try {
      DatadogProvisionerConfiguration datadogConfiguration = getDatadogConfiguration();
      String configId = datadogConfiguration.getDatadogExternalSystemConfigId();
      DatadogSettings datadogSettings = buildDatadogSettings();

      DatadogUser datadogUser = DatadogUser.fromProvisioningEntity(targetEntity, null);

      DatadogApiCommands.disableUser(configId, datadogSettings, datadogUser);

      targetEntity.setProvisioned(true);

      for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetEntity.getInternal_objectChanges())) {
        provisioningObjectChange.setProvisioned(true);
      }

      return new TargetDaoDeleteEntityResponse();
    } catch (Exception e) {
      targetEntity.setProvisioned(false);
      for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetEntity.getInternal_objectChanges())) {
        provisioningObjectChange.setProvisioned(false);
      }
      throw e;
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("deleteEntity", startNanos));
    }
  }

  @Override
  public void registerGrouperProvisionerDaoCapabilities(
      GrouperProvisionerDaoCapabilities grouperProvisionerDaoCapabilities) {
    grouperProvisionerDaoCapabilities.setCanDeleteEntity(true);
    grouperProvisionerDaoCapabilities.setCanDeleteGroup(true);
    grouperProvisionerDaoCapabilities.setCanDeleteMembership(true);
    grouperProvisionerDaoCapabilities.setCanInsertEntity(true);
    grouperProvisionerDaoCapabilities.setCanInsertGroup(true);
    grouperProvisionerDaoCapabilities.setCanInsertMembership(true);
    grouperProvisionerDaoCapabilities.setCanRetrieveAllEntities(true);
    grouperProvisionerDaoCapabilities.setCanRetrieveAllGroups(true);
    grouperProvisionerDaoCapabilities.setCanRetrieveEntity(true);
    grouperProvisionerDaoCapabilities.setCanRetrieveGroup(true);
    grouperProvisionerDaoCapabilities.setCanRetrieveMembershipsAllByGroup(true);
    grouperProvisionerDaoCapabilities.setCanUpdateEntity(true);
    grouperProvisionerDaoCapabilities.setCanUpdateGroup(true);
  }

}
