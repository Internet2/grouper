package edu.internet2.middleware.grouper.app.freshServiceAgent;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioner;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningEntity;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningGroup;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningGroupWrapper;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningMembership;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningObjectChange;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningObjectChangeAction;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.GrouperProvisionerDaoCapabilities;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.GrouperProvisionerTargetDaoBase;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoDeleteEntityRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoDeleteEntityResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoDeleteGroupRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoDeleteGroupResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoDeleteMembershipRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoDeleteMembershipResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoInsertEntityRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoInsertEntityResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoInsertGroupRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoInsertGroupResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoInsertMembershipRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoInsertMembershipResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveAllEntitiesRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveAllEntitiesResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveAllGroupsRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveAllGroupsResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveEntityRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveEntityResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveGroupRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveGroupResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveGroupsRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveGroupsResponse;
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


public class FreshAgentTargetDao extends GrouperProvisionerTargetDaoBase {

  /**
   * roles are read-only in Freshservice (Grouper cannot change them), so cache them for this provisioning run
   */
  private List<FreshAgentRole> rolesCache = null;

  /**
   * role memberships are the roles array on each agent, so retrieving the members of a role needs all agents.
   * Cache them for this provisioning run so each role does not retrieve all agents.  Cleared on any write that
   * could change roles
   */
  private List<FreshAgentUser> agentsForRoleMembershipsCache = null;

  /**
   * get the configuration
   * @return the configuration
   */
  private FreshAgentConfiguration freshAgentConfiguration() {
    return (FreshAgentConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
  }

  /**
   * get all roles (cached for this provisioning run)
   * @return the roles
   */
  private List<FreshAgentRole> retrieveRolesCached() {
    if (this.rolesCache == null) {
      this.rolesCache = FreshAgentApiCommands.retrieveAgentRoles(this.freshAgentConfiguration().getFreshserviceExternalSystemConfigId());
    }
    return this.rolesCache;
  }

  /**
   * a Grouper group provisions to an agent group or to an existing role.  The type comes from (in order):
   * a role_ prefixed target id, the groupType target group attribute (if translated), the
   * md_grouper_freshserviceGroupType metadata of the Grouper group, else agent group
   * @param targetGroup
   * @return true if role
   */
  private boolean isRole(ProvisioningGroup targetGroup) {
    if (targetGroup == null || !this.freshAgentConfiguration().isProvisionRoles()) {
      return false;
    }
    if (StringUtils.isNotBlank(targetGroup.getId())) {
      return FreshAgentRole.isRoleTargetId(targetGroup.getId());
    }
    String groupType = targetGroup.retrieveAttributeValueString("groupType");
    if (StringUtils.isBlank(groupType)) {
      ProvisioningGroupWrapper provisioningGroupWrapper = targetGroup.getProvisioningGroupWrapper();
      ProvisioningGroup grouperProvisioningGroup = provisioningGroupWrapper == null ? null : provisioningGroupWrapper.getGrouperProvisioningGroup();
      groupType = grouperProvisioningGroup == null ? null
          : grouperProvisioningGroup.retrieveAttributeValueString(FreshAgentSyncObjectMetadata.METADATA_GROUP_TYPE);
    }
    if (StringUtils.isBlank(groupType) || StringUtils.equals(FreshAgentRole.GROUP_TYPE_GROUP, groupType)) {
      return false;
    }
    if (StringUtils.equals(FreshAgentRole.GROUP_TYPE_ROLE, groupType)) {
      return true;
    }
    throw new RuntimeException("Invalid groupType: '" + groupType + "', expected '" + FreshAgentRole.GROUP_TYPE_GROUP
        + "' or '" + FreshAgentRole.GROUP_TYPE_ROLE + "'");
  }

  /**
   * find an existing role that matches a Grouper group: by id if the target group has one, else by name
   * @param targetGroup
   * @return the role or null if not found
   */
  private FreshAgentRole findRole(ProvisioningGroup targetGroup) {
    String searchAttribute = StringUtils.isNotBlank(targetGroup.getId()) ? "id" : "name";
    Object searchAttributeValue = StringUtils.isNotBlank(targetGroup.getId()) ? targetGroup.getId() : targetGroup.retrieveAttributeValueString("name");
    for (FreshAgentRole freshAgentRole : GrouperUtil.nonNull(this.retrieveRolesCached())) {
      if (freshAgentRole.matches(searchAttribute, searchAttributeValue)) {
        return freshAgentRole;
      }
    }
    return null;
  }

  /**
   * mark a group (and its changes) provisioned, roles are never changed in Freshservice, so a role
   * "update" or "delete" is just marked done
   * @param targetGroup
   */
  private static void markGroupProvisioned(ProvisioningGroup targetGroup) {
    targetGroup.setProvisioned(true);
    for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetGroup.getInternal_objectChanges())) {
      provisioningObjectChange.setProvisioned(true);
    }
  }

  @Override
  public boolean loggingStart() {
    return GrouperHttpClient.logStart(new GrouperHttpClientLog());
  }

  @Override
  public String loggingStop() {
    return GrouperHttpClient.logEnd();
  }
  
  @Override
  public TargetDaoRetrieveAllGroupsResponse retrieveAllGroups(TargetDaoRetrieveAllGroupsRequest targetDaoRetrieveAllGroupsRequest) {
    List<ProvisioningGroup> results = new ArrayList<ProvisioningGroup>();
    long startNanos = System.nanoTime();
    
    try {
      FreshAgentConfiguration freshserviceConfiguration = (FreshAgentConfiguration) this.getGrouperProvisioner()
          .retrieveGrouperProvisioningConfiguration();
      
      List<FreshAgentGroup> agentGroups = FreshAgentApiCommands.retrieveAgentGroups(freshserviceConfiguration.getFreshserviceExternalSystemConfigId());
      
      for (FreshAgentGroup agentGroup : agentGroups) {
        ProvisioningGroup targetGroup = agentGroup.toProvisioningGroup();
        results.add(targetGroup);
        // generic provisioner sync back: the native group is now captured from the raw JSON at the
        // FreshAgentApiCommands read seam (full fidelity, not the lossy typed bean), so no
        // capture is needed here.
      }

      // roles are groups too if a Grouper group can provision to a role.  The target ids are role_ prefixed
      if (freshserviceConfiguration.isProvisionRoles()) {
        for (FreshAgentRole freshAgentRole : GrouperUtil.nonNull(this.retrieveRolesCached())) {
          results.add(freshAgentRole.toProvisioningGroup());
        }
      }
      return new TargetDaoRetrieveAllGroupsResponse(results);
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("retrieveAllGroups", startNanos));
    }
  }
  
  @Override
  public TargetDaoRetrieveGroupResponse retrieveGroup(TargetDaoRetrieveGroupRequest targetDaoRetrieveGroupRequest) {
    long startNanos = System.nanoTime();
    
    try {
      FreshAgentConfiguration freshserviceConfiguration = (FreshAgentConfiguration) this.getGrouperProvisioner()
          .retrieveGrouperProvisioningConfiguration();

      // a role is matched by role_ prefixed id, or by name if the Grouper group is a role
      boolean roleById = StringUtils.equals("id", targetDaoRetrieveGroupRequest.getSearchAttribute())
          && FreshAgentRole.isRoleTargetId(GrouperUtil.stringValue(targetDaoRetrieveGroupRequest.getSearchAttributeValue()));
      if (freshserviceConfiguration.isProvisionRoles() && (roleById || this.isRole(targetDaoRetrieveGroupRequest.getTargetGroup()))) {
        for (FreshAgentRole freshAgentRole : GrouperUtil.nonNull(this.retrieveRolesCached())) {
          if (freshAgentRole.matches(targetDaoRetrieveGroupRequest.getSearchAttribute(), targetDaoRetrieveGroupRequest.getSearchAttributeValue())) {
            return new TargetDaoRetrieveGroupResponse(freshAgentRole.toProvisioningGroup());
          }
        }
        return new TargetDaoRetrieveGroupResponse();
      }
      
      if (StringUtils.equals("id", targetDaoRetrieveGroupRequest.getSearchAttribute())) {
        FreshAgentGroup agentGroup = FreshAgentApiCommands.retrieveAgentGroup(freshserviceConfiguration.getFreshserviceExternalSystemConfigId(),
            GrouperUtil.longValue(targetDaoRetrieveGroupRequest.getSearchAttributeValue()));
        ProvisioningGroup targetGroup = agentGroup == null ? null : agentGroup.toProvisioningGroup();
        // generic provisioner sync back: scoped retrieve path (used by !selectAllGroups and
        // incremental). The native group is now captured from the raw JSON at the
        // FreshAgentApiCommands read seam, so no capture is needed here.
        return new TargetDaoRetrieveGroupResponse(targetGroup);
      } else if (StringUtils.equals("name", targetDaoRetrieveGroupRequest.getSearchAttribute())) {
        List<FreshAgentGroup> agentGroups = FreshAgentApiCommands.retrieveAgentGroups(freshserviceConfiguration.getFreshserviceExternalSystemConfigId());
        for (FreshAgentGroup agentGroup : agentGroups) {
          if (StringUtils.equals(agentGroup.getName(), GrouperUtil.stringValue(targetDaoRetrieveGroupRequest.getSearchAttributeValue()))) {
            ProvisioningGroup targetGroup = agentGroup == null ? null : agentGroup.toProvisioningGroup();
            // generic provisioner sync back: scoped retrieve path (used by !selectAllGroups and
            // incremental). The native group is now captured from the raw JSON at the
            // FreshAgentApiCommands read seam, so no capture is needed here.
            return new TargetDaoRetrieveGroupResponse(targetGroup);
          }
        }
      } else {
        throw new RuntimeException("id or name is required as a group search attribute");
      }
      return new TargetDaoRetrieveGroupResponse();
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("retrieveGroup", startNanos));
    }
  }
  
  @Override
  public TargetDaoInsertGroupResponse insertGroup(TargetDaoInsertGroupRequest targetDaoInsertGroupRequest) {
    long startNanos = System.nanoTime();
    ProvisioningGroup targetGroup = targetDaoInsertGroupRequest.getTargetGroup();

    try {
      FreshAgentConfiguration freshserviceConfiguration = (FreshAgentConfiguration) this.getGrouperProvisioner()
          .retrieveGrouperProvisioningConfiguration();

      // roles cannot be created in Freshservice, so link to the existing role, or fail just this group (the
      // framework records the error on this group, keeps provisioning everything else, and the job ends in error)
      if (this.isRole(targetGroup)) {
        FreshAgentRole freshAgentRole = this.findRole(targetGroup);
        if (freshAgentRole == null) {
          throw new RuntimeException("Freshservice role '" + targetGroup.retrieveAttributeValueString("name")
              + "' does not exist.  Roles cannot be created with the Freshservice API, create the role in Freshservice"
              + " (with the same name), or change the group type of this group to '" + FreshAgentRole.GROUP_TYPE_GROUP + "'");
        }
        targetGroup.setId(FreshAgentRole.targetIdFromRoleId(freshAgentRole.getId()));
        markGroupProvisioned(targetGroup);
        return new TargetDaoInsertGroupResponse();
      }

      FreshAgentGroup grouperAgentGroup = FreshAgentGroup.fromProvisioningGroup(targetGroup, null);

      FreshAgentGroup createdGroup = FreshAgentApiCommands.createAgentGroup(
          freshserviceConfiguration.getFreshserviceExternalSystemConfigId(), grouperAgentGroup);

      targetGroup.setId(String.valueOf(createdGroup.getId()));
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
      FreshAgentConfiguration freshserviceConfiguration = (FreshAgentConfiguration) this.getGrouperProvisioner()
          .retrieveGrouperProvisioningConfiguration();

      // roles are read-only in the Freshservice API, so a name or description change is not sent
      if (this.isRole(targetGroup)) {
        markGroupProvisioned(targetGroup);
        return new TargetDaoUpdateGroupResponse();
      }

      // collect the field names and actions that need to be updated from the provisioning object changes
      Map<String, ProvisioningObjectChangeAction> fieldsToUpdate = new LinkedHashMap<String, ProvisioningObjectChangeAction>();
      for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetGroup.getInternal_objectChanges())) {
        String fieldName = provisioningObjectChange.getAttributeName();
        fieldsToUpdate.put(fieldName, provisioningObjectChange.getProvisioningObjectChangeAction());
      }

      FreshAgentGroup grouperAgentGroup = FreshAgentGroup.fromProvisioningGroup(targetGroup, null);

      FreshAgentApiCommands.updateAgentGroup(
          freshserviceConfiguration.getFreshserviceExternalSystemConfigId(), grouperAgentGroup, fieldsToUpdate);

      targetGroup.setProvisioned(true);

      for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetGroup.getInternal_objectChanges())) {
        provisioningObjectChange.setProvisioned(true);
      }

      // sync-back: FreshAgent captures group OBJECTS on the read path only, so an attribute
      // update does not refresh grouper_prov_group under groups-from-cache. Mark this updated group
      // for the end-of-run sync-back drain re-read so the native mirror converges on the write pass.
      // No-op internally when sync-back (isLoadGroupsToGenericGrouperTable) is off.
      this.getGrouperProvisioner().retrieveGrouperProvisioningTargetNativeSync()
          .recordTargetNativeGroupWrite(targetGroup.getId(), null);

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
      FreshAgentConfiguration freshserviceConfiguration = (FreshAgentConfiguration) this.getGrouperProvisioner()
          .retrieveGrouperProvisioningConfiguration();

      // roles cannot be deleted with the Freshservice API, the role is left there
      if (this.isRole(targetGroup)) {
        markGroupProvisioned(targetGroup);
        return new TargetDaoDeleteGroupResponse();
      }

      FreshAgentGroup grouperAgentGroup = FreshAgentGroup.fromProvisioningGroup(targetGroup, null);

      FreshAgentApiCommands.deleteAgentGroup(
          freshserviceConfiguration.getFreshserviceExternalSystemConfigId(), grouperAgentGroup.getId());

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
  public TargetDaoRetrieveAllEntitiesResponse retrieveAllEntities(TargetDaoRetrieveAllEntitiesRequest targetDaoRetrieveAllEntitiesRequest) {
    List<ProvisioningEntity> results = new ArrayList<ProvisioningEntity>();
    long startNanos = System.nanoTime();
    
    try {
      FreshAgentConfiguration freshserviceConfiguration = (FreshAgentConfiguration) this.getGrouperProvisioner()
          .retrieveGrouperProvisioningConfiguration();
      
      List<FreshAgentUser> agents = FreshAgentApiCommands.retrieveAgentUsers(freshserviceConfiguration.getFreshserviceExternalSystemConfigId(), false);
      for (FreshAgentUser agent : agents) {
        ProvisioningEntity targetEntity = agent.toProvisioningEntity();
        results.add(targetEntity);
        // generic provisioner sync back: the native user is now captured from the raw JSON at the
        // FreshAgentApiCommands read seam (full fidelity, not the lossy typed bean), so no
        // capture is needed here.
      }
      return new TargetDaoRetrieveAllEntitiesResponse(results);
    }
    finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("retrieveAllEntities", startNanos));
    }
  }
  
  @Override
  public TargetDaoRetrieveEntityResponse retrieveEntity(TargetDaoRetrieveEntityRequest targetDaoRetrieveEntityRequest) {
    long startNanos = System.nanoTime();
    
    FreshAgentUser agent = null;
    
    try {
      FreshAgentConfiguration freshserviceConfiguration = (FreshAgentConfiguration) this.getGrouperProvisioner()
          .retrieveGrouperProvisioningConfiguration();
      
      agent = FreshAgentApiCommands.retrieveAgentUserByAttribute(
          freshserviceConfiguration.getFreshserviceExternalSystemConfigId(),
          targetDaoRetrieveEntityRequest.getSearchAttribute(),
          targetDaoRetrieveEntityRequest.getSearchAttributeValue());
      
      ProvisioningEntity targetEntity = agent == null ? null : agent.toProvisioningEntity();

      // generic provisioner sync back: scoped retrieve path (used by !selectAllEntities and
      // incremental). The native user is now captured from the raw JSON at the
      // FreshAgentApiCommands read seam, so no capture is needed here.

      TargetDaoRetrieveEntityResponse targetDaoRetrieveEntityResponse = new TargetDaoRetrieveEntityResponse(targetEntity);
      if (targetDaoRetrieveEntityRequest.isIncludeNativeEntity()) {
        targetDaoRetrieveEntityResponse.setTargetNativeEntity(agent);
      }
      return targetDaoRetrieveEntityResponse;
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("retrieveEntity", startNanos));
    }
  }
  
  @Override
  public TargetDaoInsertEntityResponse insertEntity(TargetDaoInsertEntityRequest targetDaoInsertEntityRequest) {
    long startNanos = System.nanoTime();
    ProvisioningEntity targetEntity = targetDaoInsertEntityRequest.getTargetEntity();

    try {
      FreshAgentConfiguration freshserviceConfiguration = (FreshAgentConfiguration) this.getGrouperProvisioner()
          .retrieveGrouperProvisioningConfiguration();

      FreshAgentUser grouperAgentUser = FreshAgentUser.fromProvisioningEntity(targetEntity, null);

      // Freshservice requires a role when an agent is created, so use the configured default role
      if (freshserviceConfiguration.getDefaultAgentRoleId() != null) {
        grouperAgentUser.assignRole(freshserviceConfiguration.getDefaultAgentRoleId(),
            freshserviceConfiguration.getDefaultAgentRoleAssignmentScope());
      }

      FreshAgentUser createdUser = FreshAgentApiCommands.createAgentUser(
          freshserviceConfiguration.getFreshserviceExternalSystemConfigId(), grouperAgentUser,
          freshserviceConfiguration.isReactivateAsFullTime());

      targetEntity.setId(String.valueOf(createdUser.getId()));
      targetEntity.setProvisioned(true);
      // a new agent gets the default role
      this.agentsForRoleMembershipsCache = null;

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
      FreshAgentConfiguration freshserviceConfiguration = (FreshAgentConfiguration) this.getGrouperProvisioner()
          .retrieveGrouperProvisioningConfiguration();

      // collect the field names that need to be updated from the provisioning object changes
      Set<String> fieldNamesToUpdate = new HashSet<String>();
      for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetEntity.getInternal_objectChanges())) {
        String fieldName = provisioningObjectChange.getAttributeName();
        fieldNamesToUpdate.add(fieldName);
      }

      FreshAgentUser grouperAgentUser = FreshAgentUser.fromProvisioningEntity(targetEntity, null);

      FreshAgentApiCommands.updateAgentUser(
          freshserviceConfiguration.getFreshserviceExternalSystemConfigId(), grouperAgentUser, fieldNamesToUpdate);

      targetEntity.setProvisioned(true);

      for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetEntity.getInternal_objectChanges())) {
        provisioningObjectChange.setProvisioned(true);
      }

      // sync-back: FreshAgent captures user OBJECTS on the read path only, so an attribute
      // update does not refresh grouper_prov_user under users-from-cache. Mark this updated user
      // for the end-of-run sync-back drain re-read so the native mirror converges on the write pass.
      // No-op internally when sync-back (isLoadEntitiesToGenericGrouperTable) is off.
      this.getGrouperProvisioner().retrieveGrouperProvisioningTargetNativeSync()
          .recordTargetNativeUserWrite(targetEntity.getId(), null);

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
      FreshAgentConfiguration freshserviceConfiguration = (FreshAgentConfiguration) this.getGrouperProvisioner()
          .retrieveGrouperProvisioningConfiguration();

      FreshAgentUser grouperAgentUser = FreshAgentUser.fromProvisioningEntity(targetEntity, null);

      // deactivate (soft delete) the agent user
      FreshAgentApiCommands.deactivateAgentUser(
          freshserviceConfiguration.getFreshserviceExternalSystemConfigId(), grouperAgentUser.getId());

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
  public TargetDaoInsertMembershipResponse insertMembership(TargetDaoInsertMembershipRequest targetDaoInsertMembershipRequest) {
    long startNanos = System.nanoTime();
    ProvisioningMembership targetMembership = targetDaoInsertMembershipRequest.getTargetMembership();
    
    try {
      FreshAgentConfiguration freshserviceConfiguration = (FreshAgentConfiguration) this.getGrouperProvisioner()
          .retrieveGrouperProvisioningConfiguration();
      
      if (FreshAgentRole.isRoleTargetId(targetMembership.getProvisioningGroupId())) {
        // role membership: add the role to the roles array of the agent
        this.agentsForRoleMembershipsCache = null;
        FreshAgentApiCommands.addAgentRole(freshserviceConfiguration.getFreshserviceExternalSystemConfigId(),
            GrouperUtil.longValue(targetMembership.getProvisioningEntityId()),
            FreshAgentRole.roleIdFromTargetId(targetMembership.getProvisioningGroupId()),
            freshserviceConfiguration.getDefaultAgentRoleAssignmentScope());
      } else {
        FreshAgentApiCommands.addGroupMembership(freshserviceConfiguration.getFreshserviceExternalSystemConfigId(), 
            GrouperUtil.longValue(targetMembership.getProvisioningGroupId()),GrouperUtil.longValue(targetMembership.getProvisioningEntityId()));
      }

      targetMembership.setProvisioned(true);
      // sync-back: on success only, write-track the added membership into the native mirror so the
      // end-of-run flush keeps grouper_prov_mship current (GRP-7048). Same group + user target ids
      // just passed to the Freshservice add API above.
      FreshAgentProvisioningTargetNativeSync.captureMembershipInsertFromCurrentProvisioner(
          targetMembership.getProvisioningGroupId(), targetMembership.getProvisioningEntityId());
      return new TargetDaoInsertMembershipResponse();
    } catch(Exception e) {
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
      FreshAgentConfiguration freshserviceConfiguration = (FreshAgentConfiguration) this.getGrouperProvisioner()
          .retrieveGrouperProvisioningConfiguration();
      
      if (FreshAgentRole.isRoleTargetId(targetMembership.getProvisioningGroupId())) {
        // role membership: remove the role from the roles array of the agent.  Agents need at least one role,
        // so the last role is replaced with the default role (if configured and not this role)
        this.agentsForRoleMembershipsCache = null;
        FreshAgentApiCommands.removeAgentRole(freshserviceConfiguration.getFreshserviceExternalSystemConfigId(),
            GrouperUtil.longValue(targetMembership.getProvisioningEntityId()),
            FreshAgentRole.roleIdFromTargetId(targetMembership.getProvisioningGroupId()),
            freshserviceConfiguration.getDefaultAgentRoleId(), freshserviceConfiguration.getDefaultAgentRoleAssignmentScope());
      } else {
        FreshAgentApiCommands.removeGroupMembership(freshserviceConfiguration.getFreshserviceExternalSystemConfigId(), 
            GrouperUtil.longValue(targetMembership.getProvisioningGroupId()),GrouperUtil.longValue(targetMembership.getProvisioningEntityId()));
      }

      targetMembership.setProvisioned(true);
      // sync-back: on success only, drop the removed membership from the native mirror so the
      // end-of-run flush deletes its grouper_prov_mship row (GRP-7048). Same group + user target ids
      // just passed to the Freshservice remove API above.
      FreshAgentProvisioningTargetNativeSync.captureMembershipDeleteFromCurrentProvisioner(
          targetMembership.getProvisioningGroupId(), targetMembership.getProvisioningEntityId());
      return new TargetDaoDeleteMembershipResponse();
    } catch(Exception e) {
      targetMembership.setProvisioned(false);
      for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetMembership.getInternal_objectChanges())) {
        provisioningObjectChange.setProvisioned(false);
      }
      throw e;
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("deleteMembership", startNanos));
    }
  }
  
  @Override
  public TargetDaoRetrieveMembershipsByGroupResponse retrieveMembershipsByGroup(TargetDaoRetrieveMembershipsByGroupRequest targetDaoRetrieveMembershipsByGroupRequest) {
    long startNanos = System.nanoTime();
    ProvisioningGroup targetGroup = targetDaoRetrieveMembershipsByGroupRequest.getTargetGroup();
    
    String targetGroupId = resolveTargetGroupId(targetGroup, this.getGrouperProvisioner());
    List<ProvisioningMembership> provisioningMemberships = new ArrayList<ProvisioningMembership>();
    
    if (StringUtils.isBlank(targetGroupId)) {
      return new TargetDaoRetrieveMembershipsByGroupResponse(provisioningMemberships);
    }
    
    try {
      FreshAgentConfiguration freshserviceConfiguration = (FreshAgentConfiguration) this.getGrouperProvisioner()
          .retrieveGrouperProvisioningConfiguration();
      
      List<FreshAgentUser> agents = null;
      if (FreshAgentRole.isRoleTargetId(targetGroupId)) {
        // role members are the agents with the role in their roles array, retrieve all agents once per run
        if (this.agentsForRoleMembershipsCache == null) {
          this.agentsForRoleMembershipsCache = FreshAgentApiCommands.retrieveAgentUsers(freshserviceConfiguration.getFreshserviceExternalSystemConfigId(), false);
        }
        agents = FreshAgentApiCommands.retrieveMembershipsByRole(freshserviceConfiguration.getFreshserviceExternalSystemConfigId(),
            FreshAgentRole.roleIdFromTargetId(targetGroupId), this.agentsForRoleMembershipsCache);
      } else {
        agents = FreshAgentApiCommands.retrieveMembershipsByGroup(freshserviceConfiguration.getFreshserviceExternalSystemConfigId(),
            GrouperUtil.longValue(targetGroupId));
      }
      
      for(FreshAgentUser agent : agents) {
        ProvisioningMembership targetMembership = new ProvisioningMembership();
        targetMembership.setProvisioningGroupId(targetGroupId);
        targetMembership.setProvisioningEntityId(agent.getId() == null ? null : Long.toString(agent.getId()));
        provisioningMemberships.add(targetMembership);
      }

      // generic provisioner sync back: capture memberships for this group while the typed
      // user beans are in scope. memberships in FreshAgent come back per-group via the
      // membership listing endpoint, so capture here is the natural seam.
      FreshAgentProvisioningTargetNativeSync.captureMembershipsForGroupForCurrentProvisioner(
          targetGroupId, agents);

      return new TargetDaoRetrieveMembershipsByGroupResponse(provisioningMemberships);
      
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("retrieveMembershipsByGroup", startNanos));
    }
  }
  
  public String resolveTargetGroupId(ProvisioningGroup targetGroup, GrouperProvisioner grouperProvisioner) {
    if (targetGroup == null) {
      return null;
    }
    
    if (StringUtils.isNotBlank(targetGroup.getId())) {
      return targetGroup.getId();
    }
    
    TargetDaoRetrieveGroupsRequest targetDaoRetrieveGroupsRequest = new TargetDaoRetrieveGroupsRequest();
    targetDaoRetrieveGroupsRequest.setTargetGroups(GrouperUtil.toList(targetGroup));
    targetDaoRetrieveGroupsRequest.setIncludeAllMembershipsIfApplicable(false);
    TargetDaoRetrieveGroupsResponse targetDaoRetrieveGroupsResponse = grouperProvisioner.retrieveGrouperProvisioningTargetDaoAdapter().retrieveGroups(
        targetDaoRetrieveGroupsRequest);

    if (targetDaoRetrieveGroupsResponse == null || GrouperUtil.length(targetDaoRetrieveGroupsResponse.getTargetGroups()) == 0) {
      return null;
    }
    
    return targetDaoRetrieveGroupsResponse.getTargetGroups().get(0).getId();
  }
  

  @Override
  public void registerGrouperProvisionerDaoCapabilities(
      GrouperProvisionerDaoCapabilities grouperProvisionerDaoCapabilities) {
    grouperProvisionerDaoCapabilities.setCanRetrieveAllEntities(true);
    grouperProvisionerDaoCapabilities.setCanRetrieveEntity(true);
    grouperProvisionerDaoCapabilities.setCanInsertEntity(true);
    grouperProvisionerDaoCapabilities.setCanUpdateEntity(true);
    grouperProvisionerDaoCapabilities.setCanDeleteEntity(true);
    grouperProvisionerDaoCapabilities.setCanRetrieveAllGroups(true);
    grouperProvisionerDaoCapabilities.setCanRetrieveGroup(true);
    grouperProvisionerDaoCapabilities.setCanInsertGroup(true);
    grouperProvisionerDaoCapabilities.setCanUpdateGroup(true);
    grouperProvisionerDaoCapabilities.setCanDeleteGroup(true);
    grouperProvisionerDaoCapabilities.setCanInsertMembership(true);
    grouperProvisionerDaoCapabilities.setCanDeleteMembership(true);
    grouperProvisionerDaoCapabilities.setCanRetrieveMembershipsAllByGroup(true);
    // read path captures groups/users from the raw Freshservice JSON at the FreshAgentApiCommands
    // read seams (full fidelity, not the lossy typed bean); memberships are captured group-centric
    // from this DAO via FreshAgentProvisioningTargetNativeSync.captureMembershipsForGroup*
    grouperProvisionerDaoCapabilities.setCanSyncBack(true);

  }

}
