package edu.internet2.middleware.grouper.app.azure;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioner;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfigurationAttribute;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfigurationAttributeDbCache;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfigurationAttributeDbCacheSource;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfigurationAttributeDbCacheType;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningTargetNativeMembership;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningEntity;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningGroup;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningGroupWrapper;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningMembership;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningObjectChange;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningObjectChangeAction;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.GrouperProvisionerDaoCapabilities;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.GrouperProvisionerTargetDaoBase;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoDeleteEntitiesRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoDeleteEntitiesResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoDeleteGroupsRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoDeleteGroupsResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoDeleteMembershipsRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoDeleteMembershipsResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoInsertEntitiesRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoInsertEntitiesResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoInsertGroupsRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoInsertGroupsResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoInsertMembershipsRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoInsertMembershipsResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveAllEntitiesRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveAllEntitiesResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveAllGroupsRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveAllGroupsResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveEntitiesByValuesRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveEntitiesByValuesResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveEntitiesRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveEntitiesResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveGroupsRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveGroupsResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveMembershipsByEntityRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveMembershipsByEntityResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveMembershipsByGroupRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveMembershipsByGroupResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoTimingInfo;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoUpdateEntitiesRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoUpdateEntitiesResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoUpdateGroupsRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoUpdateGroupsResponse;
import edu.internet2.middleware.grouper.util.GrouperHttpClient;
import edu.internet2.middleware.grouper.util.GrouperHttpClientLog;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.grouperClient.util.ExpirableCache;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncGroup;

public class GrouperAzureTargetDao extends GrouperProvisionerTargetDaoBase {
  
  /**
   * cache of owner identifier to owner url
   */
  private static ExpirableCache<Object, String> ownerIdentifierToOwnerUrl = new ExpirableCache<Object, String>(60);

  private Map<String, GrouperAzureGroup> exchangeRoutingGroups = new ConcurrentHashMap<String, GrouperAzureGroup>();

  private GrouperAzureConfiguration azureConfiguration() {
    return (GrouperAzureConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
  }

  private GrouperAzureGroup existingGroupForExchangeRouting(String id, ProvisioningGroup group) {
    GrouperAzureGroup result = this.exchangeRoutingGroups.get(id);
    if (result != null) {
      return result;
    }
    ProvisioningGroupWrapper wrapper = group == null ? null : group.getProvisioningGroupWrapper();
    ProvisioningGroup selected = wrapper == null ? group : wrapper.getTargetProvisioningGroup();
    if (selected != null && selected.retrieveAttributeValueBoolean("mailEnabled") != null
        && selected.retrieveAttributeValueBoolean("groupTypeUnified") != null
        && selected.retrieveAttributeValueBoolean("onPremisesSyncEnabled") != null) {
      result = GrouperAzureGroup.fromProvisioningGroup(selected, null);
    } else {
      List<GrouperAzureGroup> groups = GrouperAzureApiCommands.retrieveAzureGroups(
          azureConfiguration().getAzureExternalSystemConfigId(), GrouperUtil.toList(id), "id", false, new HashSet<String>());
      if (groups.size() != 1) {
        throw new RuntimeException("Cannot read Azure group " + id + " to route its write; retry after Graph replication");
      }
      result = groups.get(0);
    }
    this.exchangeRoutingGroups.put(id, result);
    return result;
  }

  private static void groupResult(ProvisioningGroup group, Exception exception) {
    group.setProvisioned(exception == null);
    group.setException(exception);
    for (ProvisioningObjectChange change : GrouperUtil.nonNull(group.getInternal_objectChanges())) {
      change.setProvisioned(exception == null);
    }
  }

  private boolean exchangeGroupWrite(ProvisioningGroup targetGroup, Set<String> fields, boolean create, boolean delete,
      List<GcGrouperSyncGroup> syncGroupsToStore) {
    if (!azureConfiguration().isExchangeOnlineEnabled()) {
      return false;
    }
    GrouperAzureGroup desired = GrouperAzureGroup.fromProvisioningGroup(targetGroup, null);
    try {
      GrouperAzureGroup existing = create ? desired : existingGroupForExchangeRouting(targetGroup.getId(), targetGroup);
      if (!existing.isExchangeOnlineGroup()) {
        return false;
      }
      if (existing.isOnPremisesSyncEnabled()) {
        throw new RuntimeException("Group " + targetGroup.getId() + " is synchronized from on-premises; manage it in AD");
      }
      GrouperAzureExchangeOnline exchange = new GrouperAzureExchangeOnline(azureConfiguration());
      if (create) {
        ProvisioningGroupWrapper wrapper = targetGroup.getProvisioningGroupWrapper();
        GcGrouperSyncGroup sync = wrapper == null ? null : wrapper.getGcGrouperSyncGroup();
        if (recentlyInserted(sync)) {
          throw new RuntimeException("Exchange group was recently created but is not yet readable in Graph; retry later");
        }
        GrouperProvisioningConfigurationAttributeDbCache idCache = exchangeIdCache();
        String id = GrouperAzureExchangeOnline.createdGroupId(exchange.exoInvokeCommand("New-DistributionGroup",
            GrouperAzureExchangeOnline.groupParameters(desired, fields, true)));
        targetGroup.setId(id);
        desired.setId(id);
        this.exchangeRoutingGroups.put(id, desired);
        if (sync != null) {
          // Save the returned id before select-after-insert can fail on Graph's eventual consistency.
          sync.assignField("groupAttributeValueCache" + idCache.getIndex(), id);
          sync.setInTarget(true);
          sync.setInTargetStart(new Timestamp(System.currentTimeMillis()));
          sync.setInTargetInsertOrExists(true);
          syncGroupsToStore.add(sync);
        }
      } else if (delete) {
        Map<String, Object> parameters = new LinkedHashMap<String, Object>();
        parameters.put("Identity", targetGroup.getId());
        parameters.put("BypassSecurityGroupManagerCheck", true);
        parameters.put("Confirm", false);
        exchange.exoInvokeCommand("Remove-DistributionGroup", parameters);
        this.exchangeRoutingGroups.remove(targetGroup.getId());
      } else {
        exchange.exoInvokeCommand("Set-DistributionGroup",
            GrouperAzureExchangeOnline.groupParameters(desired, fields, false));
        this.getGrouperProvisioner().retrieveGrouperProvisioningTargetNativeSync()
            .recordTargetNativeGroupWrite(targetGroup.getId(), null);
      }
      groupResult(targetGroup, null);
    } catch (Exception e) {
      groupResult(targetGroup, e);
    }
    return true;
  }

  private GrouperProvisioningConfigurationAttributeDbCache exchangeIdCache() {
    for (GrouperProvisioningConfigurationAttributeDbCache cache : azureConfiguration().getGroupAttributeDbCaches()) {
      if (cache != null && cache.getSource() == GrouperProvisioningConfigurationAttributeDbCacheSource.target
          && cache.getType() == GrouperProvisioningConfigurationAttributeDbCacheType.attribute
          && StringUtils.equals("id", cache.getAttributeName())) {
        return cache;
      }
    }
    throw new RuntimeException("Exchange Online requires a target group attribute value cache for id");
  }

  static boolean recentlyInserted(GcGrouperSyncGroup sync) {
    return sync != null && sync.isInTargetInsertOrExists() && sync.getInTargetStart() != null
        && System.currentTimeMillis() - sync.getInTargetStart().getTime() < 15 * 60 * 1000;
  }

  private void checkGraphReplication(List<GrouperAzureGroup> groups, List<ProvisioningGroup> requested) {
    if (!azureConfiguration().isExchangeOnlineEnabled() || this.getGrouperProvisioner().getGcGrouperSync() == null) {
      return;
    }
    GrouperProvisioningConfigurationAttributeDbCache cache = exchangeIdCache();
    Set<String> foundIds = new HashSet<String>();
    for (GrouperAzureGroup group : groups) {
      foundIds.add(group.getId());
    }
    List<GcGrouperSyncGroup> syncGroups = new ArrayList<GcGrouperSyncGroup>();
    if (requested == null) {
      syncGroups.addAll(this.getGrouperProvisioner().getGcGrouperSync().getGcGrouperSyncGroupDao().groupRetrieveAll());
    } else {
      for (ProvisioningGroup group : requested) {
        if (group.getProvisioningGroupWrapper() != null && group.getProvisioningGroupWrapper().getGcGrouperSyncGroup() != null) {
          syncGroups.add(group.getProvisioningGroupWrapper().getGcGrouperSyncGroup());
        }
      }
    }
    for (GcGrouperSyncGroup sync : syncGroups) {
      String id = (String) sync.retrieveField("groupAttributeValueCache" + cache.getIndex());
      if (recentlyInserted(sync) && Boolean.TRUE.equals(sync.getInTarget()) && StringUtils.isNotBlank(id)
          && !foundIds.contains(id)) {
        Set<String> extensions = new HashSet<String>();
        for (String attribute : azureConfiguration().getTargetGroupAttributeNameToConfig().keySet()) {
          if (attribute.startsWith("extension_")) {
            extensions.add(attribute);
          }
        }
        List<GrouperAzureGroup> byId = GrouperAzureApiCommands.retrieveAzureGroups(
            azureConfiguration().getAzureExternalSystemConfigId(), GrouperUtil.toList(id), "id",
            azureConfiguration().getTargetGroupAttributeNameToConfig().containsKey("groupOwners"), extensions);
        if (!byId.isEmpty()) {
          if (requested == null) {
            groups.addAll(byId);
            foundIds.add(id);
          }
          continue;
        }
        // Fail before missing-object processing clears the durable id link and schedules another create.
        throw new RuntimeException("Recently created Azure group " + id
            + " is not yet visible in Graph; defer this sync and retry. Creation is protected for 15 minutes.");
      }
    }
  }

  private List<ProvisioningMembership> exchangeMembershipWrites(List<ProvisioningMembership> memberships, boolean insert) {
    if (!azureConfiguration().isExchangeOnlineEnabled()) {
      return memberships;
    }
    List<ProvisioningMembership> graphMemberships = new ArrayList<ProvisioningMembership>();
    for (ProvisioningMembership membership : memberships) {
      try {
        GrouperAzureGroup group = existingGroupForExchangeRouting(membership.getProvisioningGroupId(),
            membership.getProvisioningGroup());
        if (!group.isExchangeOnlineGroup()) {
          graphMemberships.add(membership);
          continue;
        }
        if (group.isOnPremisesSyncEnabled()) {
          throw new RuntimeException("Group is synchronized from on-premises; manage membership in AD");
        }
        new GrouperAzureExchangeOnline(azureConfiguration()).membership(
            membership.getProvisioningGroupId(), membership.getProvisioningEntityId(), insert);
        membership.setProvisioned(true);
        membership.setException(null);
        if (insert) {
          GrouperAzureProvisioningTargetNativeSync.captureMembershipInsertFromCurrentProvisioner(
              membership.getProvisioningGroupId(), membership.getProvisioningEntityId());
        } else {
          GrouperAzureProvisioningTargetNativeSync.captureMembershipDeleteFromCurrentProvisioner(
              membership.getProvisioningGroupId(), membership.getProvisioningEntityId());
        }
      } catch (Exception e) {
        membership.setProvisioned(false);
        membership.setException(e);
      }
      for (ProvisioningObjectChange change : GrouperUtil.nonNull(membership.getInternal_objectChanges())) {
        change.setProvisioned(Boolean.TRUE.equals(membership.getProvisioned()));
      }
    }
    return graphMemberships;
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
  public TargetDaoRetrieveAllGroupsResponse retrieveAllGroups(
      TargetDaoRetrieveAllGroupsRequest targetDaoRetrieveAllGroupsRequest) {

    long startNanos = System.nanoTime();

    try {

      GrouperAzureConfiguration azureConfiguration = (GrouperAzureConfiguration) this
          .getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();

      List<ProvisioningGroup> results = new ArrayList<ProvisioningGroup>();
      
      boolean lookupOwners = azureConfiguration.getTargetGroupAttributeNameToConfig().containsKey("groupOwners");
      
      Map<String,GrouperProvisioningConfigurationAttribute> targetGroupAttributeNameToConfig = azureConfiguration.getTargetGroupAttributeNameToConfig();
      
      Set<String> extensionAttributeNames = new HashSet<String>();
      for (String key: targetGroupAttributeNameToConfig.keySet()) {
        if (StringUtils.startsWith(key, "extension_")) {          
          extensionAttributeNames.add(key);
        }
      }

      List<GrouperAzureGroup> grouperAzureGroups = GrouperAzureApiCommands
          .retrieveAzureGroups(azureConfiguration.getAzureExternalSystemConfigId(), lookupOwners, extensionAttributeNames);
      checkGraphReplication(grouperAzureGroups, null);

      // GRP-7048: marks that the bulk group pull ran this pass; absent when groups are served from
      // the sync-back cache (the adapter skips this call), which the from-cache tests assert on.
      edu.internet2.middleware.grouper.util.GrouperUtil.mapAddValue(
          this.getGrouperProvisioner().getDebugMap(), "azureRetrieveAllGroupsApiCall", 1);

      for (GrouperAzureGroup grouperAzureGroup : grouperAzureGroups) {
        ProvisioningGroup targetGroup = grouperAzureGroup.toProvisioningGroup();
        results.add(targetGroup);
        // generic provisioner sync back: the native group is captured from raw JSON at the
        // GrouperAzureApiCommands.retrieveAzureGroups seam (full fidelity), not from the bean here.
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

      //TODO
      //boolean includeAllMembershipsIfApplicable = targetDaoRetrieveAllGroupsRequest == null ? false : targetDaoRetrieveAllGroupsRequest.isIncludeAllMembershipsIfApplicable();

      GrouperAzureConfiguration azureConfiguration = (GrouperAzureConfiguration) this
          .getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();

      List<ProvisioningEntity> results = new ArrayList<ProvisioningEntity>();

      List<GrouperAzureUser> grouperAzureUsers = GrouperAzureApiCommands
          .retrieveAzureUsers(azureConfiguration.getAzureExternalSystemConfigId());

      // GRP-7048: marks that the bulk user pull ran this pass; absent when users are served from the
      // sync-back cache (the adapter skips this call), which the from-cache tests assert on.
      edu.internet2.middleware.grouper.util.GrouperUtil.mapAddValue(
          this.getGrouperProvisioner().getDebugMap(), "azureRetrieveAllUsersApiCall", 1);

      Map<ProvisioningEntity, Object> targetEntityToNativeEntity = new HashMap<>();
      
      for (GrouperAzureUser grouperAzureUser : grouperAzureUsers) {
        ProvisioningEntity targetEntity = grouperAzureUser.toProvisioningEntity();
        results.add(targetEntity);
        targetEntityToNativeEntity.put(targetEntity, grouperAzureUser);
        // generic provisioner sync back: the native user is captured from raw JSON at the
        // GrouperAzureApiCommands.retrieveAzureUsers seam (full fidelity), not from the bean here.
      }

      TargetDaoRetrieveAllEntitiesResponse response = new TargetDaoRetrieveAllEntitiesResponse(results);
      
      if (targetDaoRetrieveAllEntitiesRequest.isIncludeNativeEntity()) {
      	response.setTargetEntityToTargetNativeEntity(targetEntityToNativeEntity);
      }

      return response;
    } finally {
      this.addTargetDaoTimingInfo(
          new TargetDaoTimingInfo("retrieveAllEntities", startNanos));
    }
  }

  @Override
  public TargetDaoRetrieveEntitiesResponse retrieveEntities(TargetDaoRetrieveEntitiesRequest targetDaoRetrieveEntitiesRequest) {

    long startNanos = System.nanoTime();
    
    List<ProvisioningEntity> targetEntities = targetDaoRetrieveEntitiesRequest.getTargetEntities();

    try {
      GrouperAzureConfiguration azureConfiguration = (GrouperAzureConfiguration) this
          .getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
      
      List<String> fieldValues = new ArrayList<>();
      for(ProvisioningEntity targetEntity: targetEntities) {
        String attributeValue = targetEntity.retrieveAttributeValueString(targetDaoRetrieveEntitiesRequest.getSearchAttribute());
        if (StringUtils.isNotBlank(attributeValue)) {
          fieldValues.add(attributeValue);
        }
      }
      
      List<GrouperAzureUser> azureUsers = GrouperAzureApiCommands.retrieveAzureUsers(
          azureConfiguration.getAzureExternalSystemConfigId(), fieldValues, targetDaoRetrieveEntitiesRequest.getSearchAttribute());
      
      List<ProvisioningEntity> targetEntitiesFromAzure = new ArrayList<>();
      
      Map<ProvisioningEntity, Object> targetEntityToNativeEntity = new HashMap<>();
      
      for (GrouperAzureUser userFromAuzre: azureUsers) {
        targetEntitiesFromAzure.add(userFromAuzre.toProvisioningEntity());
        targetEntityToNativeEntity.put(userFromAuzre.toProvisioningEntity(), userFromAuzre);
        // generic provisioner sync back: the native user is captured from raw JSON at the
        // GrouperAzureApiCommands.retrieveUsersHelper seam (full fidelity), not from the bean here.
        // Scoped retrieve path (used by !selectAllEntities and incremental).
      }
      
      TargetDaoRetrieveEntitiesResponse response = new TargetDaoRetrieveEntitiesResponse();
      response.setTargetEntities(targetEntitiesFromAzure);
      
      if (targetDaoRetrieveEntitiesRequest.isIncludeNativeEntity()) {
    	response.setTargetEntityToTargetNativeEntity(targetEntityToNativeEntity);
      }
      
      return response;
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("retrieveEntities", startNanos));
    }
  }

  @Override
  public TargetDaoRetrieveGroupsResponse retrieveGroups(
      TargetDaoRetrieveGroupsRequest targetDaoRetrieveGroupsRequest) {

    long startNanos = System.nanoTime();

    try {
      GrouperAzureConfiguration azureConfiguration = (GrouperAzureConfiguration) this
          .getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();

      List<ProvisioningGroup> grouperTargetGroups = targetDaoRetrieveGroupsRequest.getTargetGroups();
      
      List<String> fieldValues = new ArrayList<>();
      for(ProvisioningGroup targetGroup: grouperTargetGroups) {
        String attributeValue = targetGroup.retrieveAttributeValueString(targetDaoRetrieveGroupsRequest.getSearchAttribute());
        if (StringUtils.isNotBlank(attributeValue)) {
          fieldValues.add(attributeValue);
        }
      }
      
      boolean lookupOwners = azureConfiguration.getTargetGroupAttributeNameToConfig().containsKey("groupOwners");
      
      Map<String,GrouperProvisioningConfigurationAttribute> targetGroupAttributeNameToConfig = azureConfiguration.getTargetGroupAttributeNameToConfig();
      
      Set<String> extensionAttributeNames = new HashSet<String>();
      for (String key: targetGroupAttributeNameToConfig.keySet()) {
        if (StringUtils.startsWith(key, "extension_")) {          
          extensionAttributeNames.add(key);
        }
      }
      
      // we can retrieve by id or displayName
      List<GrouperAzureGroup> azureGroups = GrouperAzureApiCommands.retrieveAzureGroups(
          azureConfiguration.getAzureExternalSystemConfigId(), fieldValues, 
          targetDaoRetrieveGroupsRequest.getSearchAttribute(), lookupOwners, extensionAttributeNames);
      checkGraphReplication(azureGroups, grouperTargetGroups);
      
      
      List<ProvisioningGroup> targetGroupsFromAzure = new ArrayList<>();

      for (GrouperAzureGroup groupFromAuzre: azureGroups) {
        targetGroupsFromAzure.add(groupFromAuzre.toProvisioningGroup());
        // generic provisioner sync back: the native group is captured from raw JSON at the
        // GrouperAzureApiCommands.retrieveGroupsHelper seam (full fidelity), not from the bean here.
        // Scoped retrieve path (used by !selectAllGroups and incremental).
      }
      
      TargetDaoRetrieveGroupsResponse response = new TargetDaoRetrieveGroupsResponse();
      response.setTargetGroups(targetGroupsFromAzure);
      
      return response;

    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("retrieveGroups", startNanos));
    }
  }

  @Override
  public TargetDaoInsertGroupsResponse insertGroups(TargetDaoInsertGroupsRequest targetDaoInsertGroupsRequest) {
    
    long startNanos = System.nanoTime();
    List<ProvisioningGroup> targetGroups = targetDaoInsertGroupsRequest.getTargetGroups();

    try {
      GrouperAzureConfiguration azureConfiguration = (GrouperAzureConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
      
      List<GrouperAzureGroup> grouperAzureGroups = new ArrayList<>();
      
      Map<GrouperAzureGroup, ProvisioningGroup> azureGroupToTargetGroup = new HashMap<>();
      
      Map<GrouperAzureGroup, Set<String>> groupToFieldNamesToInsert = new HashMap<>();
      List<GcGrouperSyncGroup> exchangeSyncGroups = new ArrayList<GcGrouperSyncGroup>();
      
      for (ProvisioningGroup targetGroup: targetGroups) {
        GrouperAzureGroup grouperAzureGroup = GrouperAzureGroup.fromProvisioningGroup(targetGroup, null);
        grouperAzureGroups.add(grouperAzureGroup);
        azureGroupToTargetGroup.put(grouperAzureGroup, targetGroup);
        
        
        // lets make sure we are doing the right thing
        Set<String> fieldNamesToInsert = new HashSet<String>();

        // Initialize with fields that are required but have defaults and may not be configured explicitly
        fieldNamesToInsert.add("mailEnabled");
        fieldNamesToInsert.add("securityEnabled");
        
        for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetGroup.getInternal_objectChanges())) {
          String fieldName = provisioningObjectChange.getAttributeName();
          if (provisioningObjectChange.getProvisioningObjectChangeAction() == ProvisioningObjectChangeAction.insert) {
            fieldNamesToInsert.add(fieldName);
          }
        }
        
        if (grouperAzureGroup.isGroupTypeUnified()) {
          fieldNamesToInsert.add("groupTypeUnified");
        }

        if (exchangeGroupWrite(targetGroup, fieldNamesToInsert, true, false, exchangeSyncGroups)) {
          continue;
        }
        
        if (GrouperUtil.length(grouperAzureGroup.getOwners()) > 0) {
          // transform it into something that can be ingested by azure
          
          Set<String> ownersToBeSaved = new HashSet<>();
          Set<Object> usersToPrefixUrl = new HashSet<>();
          
          for (String owner: grouperAzureGroup.getOwners()) {
            if (owner.startsWith("http://") || owner.startsWith("https://")) { // if it's already proper url format
              ownersToBeSaved.add(owner);
            } else if (ownerIdentifierToOwnerUrl.get(owner) != null) {
              ownersToBeSaved.add(ownerIdentifierToOwnerUrl.get(owner));
            } else {
              // try to go fetch it and populate the cache
              usersToPrefixUrl.add(owner);
            }
          }
          
          
          if (usersToPrefixUrl.size() > 0) {
            
            TargetDaoRetrieveEntitiesByValuesRequest request = new TargetDaoRetrieveEntitiesByValuesRequest();
            request.setSearchValues(usersToPrefixUrl);
            
            TargetDaoRetrieveEntitiesByValuesResponse valuesResponse = this.getGrouperProvisioner()
                .retrieveGrouperProvisioningTargetDaoAdapter().retrieveEntitiesBySearchValues(request);
            Map<Object, ProvisioningEntity> searchValueToTargetEntity = valuesResponse.getSearchValueToTargetEntity();
            
            for (Object searchValue: searchValueToTargetEntity.keySet()) {
              ProvisioningEntity targetEntity = searchValueToTargetEntity.get(searchValue);
              String id = targetEntity.getId();
              String resourceEndpoint = GrouperLoaderConfig.retrieveConfig().propertyValueString(
                  "grouper.azureConnector."+azureConfiguration.getAzureExternalSystemConfigId()+".resourceEndpoint",
                  "https://graph.microsoft.com/v1.0/");
              String url = GrouperUtil.stripLastSlashIfExists(resourceEndpoint) + "/users/"+id;
              ownersToBeSaved.add(url);
              ownerIdentifierToOwnerUrl.put(searchValue, url);
            }
          }
          
          grouperAzureGroup.setOwners(ownersToBeSaved);
          
        }
        
        groupToFieldNamesToInsert.put(grouperAzureGroup, fieldNamesToInsert);
        
      }
      
      
      if (!exchangeSyncGroups.isEmpty()) {
        this.getGrouperProvisioner().getGcGrouperSync().getGcGrouperSyncGroupDao().internal_groupStore(exchangeSyncGroups);
      }
      Map<GrouperAzureGroup, Exception> groupToMaybeException = GrouperAzureApiCommands.createAzureGroups(azureConfiguration.getAzureExternalSystemConfigId(), groupToFieldNamesToInsert);
      
      for (GrouperAzureGroup grouperAzureGroup: groupToMaybeException.keySet()) {
        
        Exception exception = groupToMaybeException.get(grouperAzureGroup);
        ProvisioningGroup targetGroup = azureGroupToTargetGroup.get(grouperAzureGroup);
        
        if (exception == null) {
          
          targetGroup.setId(grouperAzureGroup.getId());
          targetGroup.setProvisioned(true);

          for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetGroup.getInternal_objectChanges())) {
            provisioningObjectChange.setProvisioned(true);
          }
        } else {
          targetGroup.setProvisioned(false);
          targetGroup.setException(exception);
          for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetGroup.getInternal_objectChanges())) {
            provisioningObjectChange.setProvisioned(false);
          }
        }
        
      }

      return new TargetDaoInsertGroupsResponse();
    } catch (Exception e) {
      for (ProvisioningGroup targetGroup: targetGroups) {
        targetGroup.setProvisioned(false);
        targetGroup.setException(e);
        for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetGroup.getInternal_objectChanges())) {
          provisioningObjectChange.setProvisioned(false);
        }
      }
      throw e;
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("insertGroups", startNanos));
    }
  }

  @Override
  public TargetDaoInsertMembershipsResponse insertMemberships(TargetDaoInsertMembershipsRequest targetDaoInsertMembershipsRequest) {
    long startNanos = System.nanoTime();
    List<ProvisioningMembership> targetMemberships = targetDaoInsertMembershipsRequest.getTargetMemberships();

    try {
      
      GrouperAzureConfiguration azureConfiguration = (GrouperAzureConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();

      targetMemberships = exchangeMembershipWrites(targetMemberships, true);

      // lets collate by group
      Map<String, List<String>> groupIdToUserIds = new LinkedHashMap<String, List<String>>();

      // keep track to mark as complete
      Map<MultiKey, ProvisioningMembership> groupIdUserIdToProvisioningMembership = new HashMap<MultiKey, ProvisioningMembership>();
      
      for (ProvisioningMembership targetMembership : targetMemberships) {

        groupIdUserIdToProvisioningMembership.put(new MultiKey(targetMembership.getProvisioningGroupId(), targetMembership.getProvisioningEntityId()), targetMembership);
        
        List<String> userIds = groupIdToUserIds.get(targetMembership.getProvisioningGroupId());
        if (userIds == null) {
          userIds = new ArrayList<String>();
          groupIdToUserIds.put(targetMembership.getProvisioningGroupId(), userIds);
        }
        userIds.add(targetMembership.getProvisioningEntityId());
      }

      // send batches by group
      for (String groupId : groupIdToUserIds.keySet()) {

        List<String> userIds = groupIdToUserIds.get(groupId);
        
        RuntimeException runtimeException = null;
        try {
          Map<MultiKey, Exception> groupIdUserIdToException = GrouperAzureApiCommands.createAzureMemberships(azureConfiguration.getAzureExternalSystemConfigId(), groupId, userIds);
          
          for (MultiKey groupIduserId : groupIdUserIdToException.keySet()) {
            ProvisioningMembership targetMembership = groupIdUserIdToProvisioningMembership.get(groupIduserId);
            
            targetMembership.setProvisioned(false);
            targetMembership.setException(groupIdUserIdToException.get(groupIduserId));
            for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetMembership.getInternal_objectChanges())) {
              provisioningObjectChange.setProvisioned(false);
            }
          }
          
        } catch (RuntimeException e) {
          runtimeException = e;
        }
        boolean success = runtimeException == null;
        for (String userId : userIds) {
          MultiKey groupIdUserId = new MultiKey(groupId, userId);

          ProvisioningMembership targetMembership = groupIdUserIdToProvisioningMembership.get(groupIdUserId);
          if (targetMembership.getProvisioned() == null) {
            targetMembership.setProvisioned(success);
            targetMembership.setException(runtimeException);
            for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetMembership.getInternal_objectChanges())) {
              provisioningObjectChange.setProvisioned(success);

            }
          }

          // sync-back: mirror each membership that actually landed in Azure into grouper_prov_mship
          // so the full-sync "memberships from sync-back cache" feature stays current. Only capture
          // the true path (a per-membership exception above leaves provisioned=false).
          if (Boolean.TRUE.equals(targetMembership.getProvisioned())) {
            GrouperAzureProvisioningTargetNativeSync.captureMembershipInsertFromCurrentProvisioner(
                targetMembership.getProvisioningGroupId(), targetMembership.getProvisioningEntityId());
          }
        }
      }
      
      return new TargetDaoInsertMembershipsResponse();
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("insertMemberships", startNanos));
    }
  }
  
  @Override
  public TargetDaoDeleteMembershipsResponse deleteMemberships(TargetDaoDeleteMembershipsRequest targetDaoDeleteMembershipsRequest) {
    long startNanos = System.nanoTime();
    
    List<ProvisioningMembership> targetMemberships = targetDaoDeleteMembershipsRequest.getTargetMemberships();

    try {
      GrouperAzureConfiguration azureConfiguration = (GrouperAzureConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();

      targetMemberships = exchangeMembershipWrites(targetMemberships, false);

      Map<ProvisioningMembership, Exception> targetMembershipsWithMaybeException = GrouperAzureApiCommands.deleteAzureMemberships(azureConfiguration.getAzureExternalSystemConfigId(), targetMemberships);

      for (ProvisioningMembership targetMembership: targetMembershipsWithMaybeException.keySet()) {
        
        Exception exception = targetMembershipsWithMaybeException.get(targetMembership);

        if (exception == null) {
          targetMembership.setProvisioned(true);
          for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetMembership.getInternal_objectChanges())) {
            provisioningObjectChange.setProvisioned(true);
          }
          // sync-back: drop each successfully-removed membership from grouper_prov_mship so the
          // full-sync "memberships from sync-back cache" feature no longer reflects it.
          GrouperAzureProvisioningTargetNativeSync.captureMembershipDeleteFromCurrentProvisioner(
              targetMembership.getProvisioningGroupId(), targetMembership.getProvisioningEntityId());
        } else {
          targetMembership.setProvisioned(false);
          targetMembership.setException(exception);
          for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetMembership.getInternal_objectChanges())) {
            provisioningObjectChange.setProvisioned(false);
          }
        }
        
      }

      return new TargetDaoDeleteMembershipsResponse();
    } catch (Exception e) {
      
      for (ProvisioningMembership targetMembership: targetMemberships) {
        targetMembership.setProvisioned(false);
        targetMembership.setException(e);
        for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetMembership.getInternal_objectChanges())) {
          provisioningObjectChange.setProvisioned(false);
        }
      }
      
      throw new RuntimeException("Failed to delete Azure memberships", e);
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("deleteMemberships", startNanos));
    }
  }

  @Override
  public TargetDaoUpdateGroupsResponse updateGroups(TargetDaoUpdateGroupsRequest targetDaoUpdateGroupsRequest) {
    
    long startNanos = System.nanoTime();
    List<ProvisioningGroup> targetGroups = targetDaoUpdateGroupsRequest.getTargetGroups();
    
    GrouperAzureConfiguration azureConfiguration = (GrouperAzureConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
    
    Map<GrouperAzureGroup, ProvisioningGroup> azureGroupToTargetGroup = new HashMap<>();
    Map<GrouperAzureGroup, Set<String>> azureGroupToFieldNamesToUpdate = new HashMap<>();
    
    Map<String, Set<String>> groupIdsToOwnersSave = new HashMap<String, Set<String>>();
    Map<String, Set<String>> groupIdsToOwnersDelete = new HashMap<String, Set<String>>();
    
    for (ProvisioningGroup targetGroup: targetGroups) {
      GrouperAzureGroup grouperAzureGroup = GrouperAzureGroup.fromProvisioningGroup(targetGroup, null);
      if (StringUtils.isBlank(grouperAzureGroup.getId())) {
        continue;
      }
      Set<String> exchangeFields = new HashSet<String>();
      for (ProvisioningObjectChange change : GrouperUtil.nonNull(targetGroup.getInternal_objectChanges())) {
        exchangeFields.add(change.getAttributeName());
      }
      if (exchangeGroupWrite(targetGroup, exchangeFields, false, false, null)) {
        continue;
      }
      azureGroupToTargetGroup.put(grouperAzureGroup, targetGroup);
      
      Set<String> fieldNamesToUpdate = new HashSet<String>();
//      for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetGroup.getInternal_objectChanges())) {
//        String fieldName = provisioningObjectChange.getAttributeName();
//        fieldNamesToUpdate.add(fieldName);
//      }
      
      Set<String> ownersToBeSaved = new HashSet<>();
      Set<String> ownersToBeDeleted = new HashSet<>();
      for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetGroup.getInternal_objectChanges())) {
        String fieldName = provisioningObjectChange.getAttributeName();
        
        // if the field name is visibility and the old value is hiddenMembership or the new value is hiddenMembership, skip it.
        // and if it's an update
        if (provisioningObjectChange.getProvisioningObjectChangeAction() == ProvisioningObjectChangeAction.update) {
          if (StringUtils.equals(fieldName, "visibility")) {
            String oldValue = (String)provisioningObjectChange.getOldValue();
            String newValue = (String)provisioningObjectChange.getNewValue();
            if (StringUtils.equalsIgnoreCase(oldValue, "HiddenMembership")
                || StringUtils.equalsIgnoreCase(newValue, "HiddenMembership")) {
              continue;
            }
          }
        }
        
        if (StringUtils.equals(fieldName, "groupOwners")) {
          boolean manageGroupOwnersInTarget = GrouperUtil.booleanValue(targetGroup.retrieveAttributeValueBoolean("groupOwnersManage"), true);
          if (!manageGroupOwnersInTarget) {
            continue;
          }
          if (provisioningObjectChange.getProvisioningObjectChangeAction() == ProvisioningObjectChangeAction.insert) {
            String groupOwnersToBeInsertedString = (String) provisioningObjectChange.getNewValue();
            if (StringUtils.isBlank(groupOwnersToBeInsertedString)) {
              continue;
            }
            
            Set<String> usersToPrefixUrl = new HashSet<>();
            String[] ownersArray = GrouperUtil.splitTrim(groupOwnersToBeInsertedString, ",");
            for (String owner: ownersArray) {
              if (owner.startsWith("http://") || owner.startsWith("https://")) { // if it's already proper url format
                ownersToBeSaved.add(owner);
              } else {
                usersToPrefixUrl.add(owner);
              }
            }
            for (String userId: usersToPrefixUrl) {
              String resourceEndpoint = GrouperLoaderConfig.retrieveConfig().propertyValueString(
                  "grouper.azureConnector."+azureConfiguration.getAzureExternalSystemConfigId()+".resourceEndpoint",
                  "https://graph.microsoft.com/v1.0/");
              String url = GrouperUtil.stripLastSlashIfExists(resourceEndpoint) + "/users/"+userId;
              ownersToBeSaved.add(url);
            }
            
          } else if (provisioningObjectChange.getProvisioningObjectChangeAction() == ProvisioningObjectChangeAction.delete) {
            String groupOwnersToBeDeletedString = (String) provisioningObjectChange.getOldValue();
            if (StringUtils.isBlank(groupOwnersToBeDeletedString)) {
              continue;
            }
            
            Set<String> usersToPrefixUrl = new HashSet<>();
            String[] ownersArray = GrouperUtil.splitTrim(groupOwnersToBeDeletedString, ",");
            for (String owner: ownersArray) {
              ownersToBeDeleted.add(owner);
            }
            
            for (String userId: usersToPrefixUrl) {
              String resourceEndpoint = GrouperLoaderConfig.retrieveConfig().propertyValueString(
                  "grouper.azureConnector."+azureConfiguration.getAzureExternalSystemConfigId()+".resourceEndpoint",
                  "https://graph.microsoft.com/v1.0/");
              String url = GrouperUtil.stripLastSlashIfExists(resourceEndpoint) + "/users/"+userId;
              ownersToBeDeleted.add(url);
            }
          }
          
        } else {          
          fieldNamesToUpdate.add(fieldName);
        }
      }
      
      groupIdsToOwnersDelete.put(grouperAzureGroup.getId(), ownersToBeDeleted);
      groupIdsToOwnersSave.put(grouperAzureGroup.getId(), ownersToBeSaved);
      azureGroupToFieldNamesToUpdate.put(grouperAzureGroup, fieldNamesToUpdate);
      
    }
    
    try {
      
      Map<GrouperAzureGroup, Exception> groupToMaybeException = GrouperAzureApiCommands.updateAzureGroups(azureConfiguration.getAzureExternalSystemConfigId(), azureGroupToFieldNamesToUpdate);
      
      GrouperAzureApiCommands.addOwnersToGroup(azureConfiguration.getAzureExternalSystemConfigId(), groupIdsToOwnersSave);
      GrouperAzureApiCommands.removeOwnersFromGroup(azureConfiguration.getAzureExternalSystemConfigId(), groupIdsToOwnersDelete);
      
      for (GrouperAzureGroup grouperAzureGroup: groupToMaybeException.keySet()) {
        
        Exception exception = groupToMaybeException.get(grouperAzureGroup);
        ProvisioningGroup targetGroup = azureGroupToTargetGroup.get(grouperAzureGroup);
        
        if (exception == null) {

          targetGroup.setProvisioned(true);

          for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetGroup.getInternal_objectChanges())) {
            provisioningObjectChange.setProvisioned(true);
          }

          // sync-back: Azure captures group OBJECTS only on the read path, so an attribute update is not
          // yet reflected in the native mirror. Mark this group (null native) so the end-of-run sync-back
          // drain re-reads it and captures the new attribute values. This keeps grouper_prov_group current
          // under groups-from-cache (fullSyncGroupsFromSyncBack), where the bulk group read is skipped and
          // there is no other place the updated attributes would land. Same primitive the delete path uses.
          this.getGrouperProvisioner().retrieveGrouperProvisioningTargetNativeSync()
              .recordTargetNativeGroupWrite(targetGroup.getId(), null);
        } else {
          targetGroup.setProvisioned(false);
          targetGroup.setException(exception);
          for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetGroup.getInternal_objectChanges())) {
            provisioningObjectChange.setProvisioned(false);
          }
        }
        
      }
      
      return new TargetDaoUpdateGroupsResponse();
    } catch (Exception e) {
      
      for (ProvisioningGroup targetGroup: targetGroups) {
        targetGroup.setProvisioned(false);
        targetGroup.setException(e);
        for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetGroup.getInternal_objectChanges())) {
          provisioningObjectChange.setProvisioned(false);
        }
      }
      
      throw e;
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("updateEntities", startNanos));
    }
    
    
  }
  
  @Override
  public TargetDaoUpdateEntitiesResponse updateEntities(TargetDaoUpdateEntitiesRequest targetDaoUpdateEntitiesRequest) {
    
    long startNanos = System.nanoTime();
    List<ProvisioningEntity> targetEntities = targetDaoUpdateEntitiesRequest.getTargetEntities();
    
    Map<GrouperAzureUser, ProvisioningEntity> azureUserToTargetEntity = new HashMap<>();
    Map<GrouperAzureUser, Set<String>> azureUserToFieldNamesToUpdate = new HashMap<>();
    
    for (ProvisioningEntity targetEntity: targetEntities) {
      GrouperAzureUser grouperAzureUser = GrouperAzureUser.fromProvisioningEntity(targetEntity, null);
      if (StringUtils.isBlank(grouperAzureUser.getId())) {
        continue;
      }
      azureUserToTargetEntity.put(grouperAzureUser, targetEntity);
      
      Set<String> fieldNamesToUpdate = new HashSet<String>();
      for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetEntity.getInternal_objectChanges())) {
        String fieldName = provisioningObjectChange.getAttributeName();
        fieldNamesToUpdate.add(fieldName);
      }
      
      azureUserToFieldNamesToUpdate.put(grouperAzureUser, fieldNamesToUpdate);
      
    }
    
    try {
      GrouperAzureConfiguration azureConfiguration = (GrouperAzureConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
      
      Map<GrouperAzureUser, Exception> userToMaybeException = GrouperAzureApiCommands.updateAzureUsers(azureConfiguration.getAzureExternalSystemConfigId(), azureUserToFieldNamesToUpdate);
      
      for (GrouperAzureUser grouperAzureUser: userToMaybeException.keySet()) {
        
        Exception exception = userToMaybeException.get(grouperAzureUser);
        ProvisioningEntity targetEntity = azureUserToTargetEntity.get(grouperAzureUser);
        
        if (exception == null) {

          targetEntity.setProvisioned(true);

          for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetEntity.getInternal_objectChanges())) {
            provisioningObjectChange.setProvisioned(true);
          }

          // sync-back: same as updateGroups -- Azure captures user OBJECTS only on the read path, so mark
          // this user (null native) for the end-of-run drain re-read to refresh its attributes in
          // grouper_prov_user under users-from-cache (fullSyncUsersFromSyncBack).
          this.getGrouperProvisioner().retrieveGrouperProvisioningTargetNativeSync()
              .recordTargetNativeUserWrite(targetEntity.getId(), null);
        } else {
          targetEntity.setProvisioned(false);
          targetEntity.setException(exception);
          for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetEntity.getInternal_objectChanges())) {
            provisioningObjectChange.setProvisioned(false);
          }
        }
        
      }
      
      return new TargetDaoUpdateEntitiesResponse();
    } catch (Exception e) {
      
      for (ProvisioningEntity targetEntity: targetEntities) {
        targetEntity.setProvisioned(false);
        targetEntity.setException(e);
        for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetEntity.getInternal_objectChanges())) {
          provisioningObjectChange.setProvisioned(false);
        }
      }
      
      throw e;
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("updateEntities", startNanos));
    }
    
  }

  
  @Override
  public TargetDaoDeleteGroupsResponse deleteGroups(TargetDaoDeleteGroupsRequest targetDaoDeleteGroupsRequest) {
    
    long startNanos = System.nanoTime();
    List<ProvisioningGroup> targetGroups = targetDaoDeleteGroupsRequest.getTargetGroups();

    try {
      GrouperAzureConfiguration azureConfiguration = (GrouperAzureConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();

      
      List<GrouperAzureGroup> grouperAzureGroups = new ArrayList<>();
      
      Map<GrouperAzureGroup, ProvisioningGroup> azureGroupToTargetGroup = new HashMap<>();
      
      for (ProvisioningGroup targetGroup: targetGroups) {
        GrouperAzureGroup grouperAzureGroup = GrouperAzureGroup.fromProvisioningGroup(targetGroup, null);
        if (StringUtils.isBlank(grouperAzureGroup.getId())) {
          continue;
        }
        if (exchangeGroupWrite(targetGroup, new HashSet<String>(), false, true, null)) {
          continue;
        }
        grouperAzureGroups.add(grouperAzureGroup);
        azureGroupToTargetGroup.put(grouperAzureGroup, targetGroup);
      }
      
      Map<GrouperAzureGroup, Exception> azureGroupsToMaybeException = GrouperAzureApiCommands.deleteAzureGroups(azureConfiguration.getAzureExternalSystemConfigId(), grouperAzureGroups);
      
      for (GrouperAzureGroup azureGroup: azureGroupsToMaybeException.keySet()) {
        
        Exception exception = azureGroupsToMaybeException.get(azureGroup);

        ProvisioningGroup targetGroup = azureGroupToTargetGroup.get(azureGroup);
        if (exception == null) {
          targetGroup.setProvisioned(true);
          for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetGroup.getInternal_objectChanges())) {
            provisioningObjectChange.setProvisioned(true);
          }
        } else {
          targetGroup.setProvisioned(false);
          targetGroup.setException(exception);
          for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetGroup.getInternal_objectChanges())) {
            provisioningObjectChange.setProvisioned(false);
          }
        }
        
      }
    
      return new TargetDaoDeleteGroupsResponse();
    } catch (Exception e) {
     
      for (ProvisioningGroup targetGroup: targetGroups) {
        targetGroup.setProvisioned(false);
        targetGroup.setException(e);
        for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetGroup.getInternal_objectChanges())) {
          provisioningObjectChange.setProvisioned(false);
        }
      }
      
      throw e;
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("deleteGroup", startNanos));
    }
  }
    
  @Override
  public TargetDaoRetrieveMembershipsByEntityResponse retrieveMembershipsByEntity(
      TargetDaoRetrieveMembershipsByEntityRequest targetDaoRetrieveMembershipsByEntityRequest) {
    long startNanos = System.nanoTime();
    
    ProvisioningEntity targetEntity = targetDaoRetrieveMembershipsByEntityRequest.getTargetEntity();

    String targetEntityId = resolveTargetEntityId(targetEntity);
    List<ProvisioningMembership> provisioningMemberships = new ArrayList<ProvisioningMembership>();
    
    if (StringUtils.isBlank(targetEntityId)) {
      return new TargetDaoRetrieveMembershipsByEntityResponse(provisioningMemberships);
    }

    try {
      GrouperAzureConfiguration azureConfiguration = (GrouperAzureConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();

      Set<String> groupIds = GrouperAzureApiCommands.retrieveAzureUserGroups(azureConfiguration.getAzureExternalSystemConfigId(), targetEntityId);

      List<GrouperProvisioningTargetNativeMembership> nativeMemberships = new ArrayList<GrouperProvisioningTargetNativeMembership>();
      for (String groupId : groupIds) {

        ProvisioningMembership targetMembership = new ProvisioningMembership(false);
        targetMembership.setProvisioningGroupId(groupId);
        targetMembership.setProvisioningEntityId(targetEntity.getId());
        provisioningMemberships.add(targetMembership);

        // generic provisioner sync back: scoped membership-by-entity path
        GrouperProvisioningTargetNativeMembership nativeMembership = new GrouperProvisioningTargetNativeMembership();
        nativeMembership.setTargetGroupId(groupId);
        nativeMembership.setTargetUserId(targetEntityId);
        nativeMemberships.add(nativeMembership);
      }
      if (!nativeMemberships.isEmpty()) {
        GrouperProvisioner provisioner = GrouperProvisioner.retrieveCurrentGrouperProvisioner();
        if (provisioner != null) {
          provisioner.retrieveGrouperProvisioningTargetNativeSync()
              .recordTargetNativeMemberships(nativeMemberships);
        }
      }

      return new TargetDaoRetrieveMembershipsByEntityResponse(provisioningMemberships);
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("retrieveMembershipsByEntity", startNanos));
    }
  }

  
  public String resolveTargetGroupId(ProvisioningGroup targetGroup) {
    
    if (targetGroup == null) {
      return null;
    }
    
    if (StringUtils.isNotBlank(targetGroup.getId())) {
      return targetGroup.getId();
    }
    
    TargetDaoRetrieveGroupsRequest targetDaoRetrieveGroupsRequest = new TargetDaoRetrieveGroupsRequest();
    targetDaoRetrieveGroupsRequest.setTargetGroups(GrouperUtil.toList(targetGroup));
    targetDaoRetrieveGroupsRequest.setIncludeAllMembershipsIfApplicable(false);
    TargetDaoRetrieveGroupsResponse targetDaoRetrieveGroupsResponse = this.getGrouperProvisioner().retrieveGrouperProvisioningTargetDaoAdapter().retrieveGroups(
        targetDaoRetrieveGroupsRequest);

    if (targetDaoRetrieveGroupsResponse == null || GrouperUtil.length(targetDaoRetrieveGroupsResponse.getTargetGroups()) == 0) {
      return null;
    }
    
    return targetDaoRetrieveGroupsResponse.getTargetGroups().get(0).getId();
    
  }
  
  
  @Override
  public TargetDaoRetrieveMembershipsByGroupResponse retrieveMembershipsByGroup(TargetDaoRetrieveMembershipsByGroupRequest targetDaoRetrieveMembershipsByGroupRequest) {
    long startNanos = System.nanoTime();
    ProvisioningGroup targetGroup = targetDaoRetrieveMembershipsByGroupRequest.getTargetGroup();
    
    String targetGroupId = resolveTargetGroupId(targetGroup);
    List<ProvisioningMembership> provisioningMemberships = new ArrayList<ProvisioningMembership>();
    
    if (StringUtils.isBlank(targetGroupId)) {
      return new TargetDaoRetrieveMembershipsByGroupResponse(provisioningMemberships);
    }
    
    try {
      GrouperAzureConfiguration azureConfiguration = (GrouperAzureConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();

      Set<String> userIds = GrouperAzureApiCommands.retrieveAzureGroupMembers(azureConfiguration.getAzureExternalSystemConfigId(), targetGroupId);

      for (String userId : userIds) {

        ProvisioningMembership targetMembership = new ProvisioningMembership(false);
        targetMembership.setProvisioningGroupId(targetGroup.getId());
        targetMembership.setProvisioningEntityId(userId);
        provisioningMemberships.add(targetMembership);
      }

      // generic provisioner sync back: capture (groupId, userId) pairs for this group.
      // Azure has no retrieveAllMemberships call -- this per-group fetch is the only place
      // both ids are co-located on the read path.
      GrouperAzureProvisioningTargetNativeSync.captureMembershipsForGroupFromCurrentProvisioner(
          targetGroupId, userIds);

      return new TargetDaoRetrieveMembershipsByGroupResponse(provisioningMemberships);
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("retrieveMembershipsByGroup", startNanos));
    }
  }
  
  @Override
  public TargetDaoInsertEntitiesResponse insertEntities(TargetDaoInsertEntitiesRequest targetDaoInsertEntitiesRequest) {
   
    
    long startNanos = System.nanoTime();
    List<ProvisioningEntity> targetEntities = targetDaoInsertEntitiesRequest.getTargetEntityInserts();

    try {
      GrouperAzureConfiguration azureConfiguration = (GrouperAzureConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();

      List<GrouperAzureUser> grouperAzureUsers = new ArrayList<>();
      
      Map<GrouperAzureUser, ProvisioningEntity> azureUserToTargetEntity = new HashMap<>();
      for (ProvisioningEntity targetEntity: targetEntities) {
        GrouperAzureUser grouperAzureUser = GrouperAzureUser.fromProvisioningEntity(targetEntity, null);
        grouperAzureUsers.add(grouperAzureUser);
        azureUserToTargetEntity.put(grouperAzureUser, targetEntity);
      }
      
      Map<GrouperAzureUser, Exception> userToMaybeException = GrouperAzureApiCommands.createAzureUsers(azureConfiguration.getAzureExternalSystemConfigId(), grouperAzureUsers, null);
      
      for (GrouperAzureUser grouperAzureUser: userToMaybeException.keySet()) {
        
        Exception exception = userToMaybeException.get(grouperAzureUser);
        ProvisioningEntity targetEntity = azureUserToTargetEntity.get(grouperAzureUser);
        
        if (exception == null) {
          
          targetEntity.setId(grouperAzureUser.getId());
          targetEntity.setProvisioned(true);

          for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetEntity.getInternal_objectChanges())) {
            provisioningObjectChange.setProvisioned(true);
          }
        } else {
          targetEntity.setProvisioned(false);
          targetEntity.setException(exception);
          for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetEntity.getInternal_objectChanges())) {
            provisioningObjectChange.setProvisioned(false);
          }
        }
        
      }

      return new TargetDaoInsertEntitiesResponse();
    } catch (Exception e) {
      for (ProvisioningEntity targetEntity: targetEntities) {
        targetEntity.setProvisioned(false);
        targetEntity.setException(e);
        for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetEntity.getInternal_objectChanges())) {
          provisioningObjectChange.setProvisioned(false);
        }
      }
      
      throw e;
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("insertEntities", startNanos));
    }
    
    
  }

  @Override
  public void registerGrouperProvisionerDaoCapabilities(
      GrouperProvisionerDaoCapabilities grouperProvisionerDaoCapabilities) {
    
    grouperProvisionerDaoCapabilities.setDefaultBatchSize(20);
    
    grouperProvisionerDaoCapabilities.setCanDeleteGroups(true);
    
    grouperProvisionerDaoCapabilities.setCanDeleteEntities(true);

    grouperProvisionerDaoCapabilities.setCanDeleteMemberships(true);

    grouperProvisionerDaoCapabilities.setCanInsertEntities(true);

    grouperProvisionerDaoCapabilities.setCanInsertGroups(true);

    grouperProvisionerDaoCapabilities.setCanInsertMemberships(true);
    grouperProvisionerDaoCapabilities.setInsertMembershipsBatchSize(400);

    grouperProvisionerDaoCapabilities.setCanRetrieveAllEntities(true);
    grouperProvisionerDaoCapabilities.setCanRetrieveAllGroups(true);
//    grouperProvisionerDaoCapabilities.setCanRetrieveAllMemberships(true);

    grouperProvisionerDaoCapabilities.setCanRetrieveEntities(true);

    grouperProvisionerDaoCapabilities.setCanRetrieveGroups(true);

    grouperProvisionerDaoCapabilities.setCanRetrieveMembershipsAllByEntity(true);
    grouperProvisionerDaoCapabilities.setCanRetrieveMembershipsAllByGroup(true);

    // GRP-7048: full-sync-from-sync-back for all three axes. Azure retrieves per-axis (no combined
    // retrieveAllData), so the generic adapter skips whichever bulk pull is served from the cache:
    // retrieveAllEntities (users), retrieveAllGroups (groups), and the by-group membership retrieval
    // (retrieveAllMemberships -> retrieveMembershipsByGroups). Every write site already keeps the
    // mirror current -- insert/deleteMemberships write-track the (group,user) pair, and
    // update/insertGroups+update/insertEntities mark the object for the end-of-run sync-back drain
    // re-read (which re-fetches owners too, see below) -- so a warm cache stays converged incrementally.
    //
    // USERS: Azure is group-centric (retrieveMembershipsByGroup), so users do not drive membership
    // retrieval; users-from-cache is safe on its own.
    grouperProvisionerDaoCapabilities.setCanFullSyncEntitiesFromSyncBack(true);
    // MEMBERSHIPS: captured group-centrically on read (retrieveMembershipsByGroup) and write-tracked on
    // every add/remove, so the mirror is authoritative and the by-group pull can be skipped.
    grouperProvisionerDaoCapabilities.setCanFullSyncMembershipsFromSyncBack(true);
    // GROUPS: captured from raw Graph JSON on read; group OWNERS (a managed group attribute, groupOwners)
    // are fetched by the separate /owners call and folded into the same native group
    // (GrouperAzureProvisioningTargetNativeSync.captureGroupOwners), so a cache-reconstructed group
    // carries its owners and does not re-push them. Groups-from-cache engages only when memberships are
    // ALSO from the cache (framework both-or-neither coupling), since Azure fetches memberships by
    // iterating groups.
    grouperProvisionerDaoCapabilities.setCanFullSyncGroupsFromSyncBack(true);

    grouperProvisionerDaoCapabilities.setCanUpdateEntities(true);

    grouperProvisionerDaoCapabilities.setCanUpdateGroups(true);

    // read path captures groups/users from raw Graph JSON at the GrouperAzureApiCommands seam, and
    // group-centric memberships during DAO translation, via GrouperAzureProvisioningTargetNativeSync
    grouperProvisionerDaoCapabilities.setCanSyncBack(true);
  }

  public String resolveTargetEntityId(ProvisioningEntity targetEntity) {
    
    if (targetEntity == null) {
      return null;
    }
    
    if (StringUtils.isNotBlank(targetEntity.getId())) {
      return targetEntity.getId();
    }
    
    TargetDaoRetrieveEntitiesRequest targetDaoRetrieveEntitiesRequest = new TargetDaoRetrieveEntitiesRequest();
    targetDaoRetrieveEntitiesRequest.setTargetEntities(GrouperUtil.toList(targetEntity));
    targetDaoRetrieveEntitiesRequest.setIncludeAllMembershipsIfApplicable(false);
    TargetDaoRetrieveEntitiesResponse targetDaoRetrieveEntitiesResponse = this.getGrouperProvisioner().retrieveGrouperProvisioningTargetDaoAdapter().retrieveEntities(
        targetDaoRetrieveEntitiesRequest);

    if (targetDaoRetrieveEntitiesResponse == null || GrouperUtil.length(targetDaoRetrieveEntitiesResponse.getTargetEntities()) == 0) {
      return null;
    }
    
    return targetDaoRetrieveEntitiesResponse.getTargetEntities().get(0).getId();
    
  }

  @Override
  public TargetDaoDeleteEntitiesResponse deleteEntities(TargetDaoDeleteEntitiesRequest targetDaoDeleteEntitiesRequest) {
    
    long startNanos = System.nanoTime();
    List<ProvisioningEntity> targetEntities = targetDaoDeleteEntitiesRequest.getTargetEntities();
  
    try {
      GrouperAzureConfiguration azureConfiguration = (GrouperAzureConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
  
      List<GrouperAzureUser> grouperAzureUsers = new ArrayList<>();
      
      Map<GrouperAzureUser, ProvisioningEntity> azureUserToTargetEntity = new HashMap<>();
      
      for (ProvisioningEntity targetEntity: targetEntities) {
        GrouperAzureUser grouperAzureUser = GrouperAzureUser.fromProvisioningEntity(targetEntity, null);
        if (StringUtils.isBlank(grouperAzureUser.getId())) {
          continue;
        }
        grouperAzureUsers.add(grouperAzureUser);
        azureUserToTargetEntity.put(grouperAzureUser, targetEntity);
      }
      
      Map<GrouperAzureUser, Exception> azureUsersToMaybeException = GrouperAzureApiCommands.deleteAzureUsers(azureConfiguration.getAzureExternalSystemConfigId(), grouperAzureUsers);
      
      for (GrouperAzureUser azureUser: azureUsersToMaybeException.keySet()) {
        
        Exception exception = azureUsersToMaybeException.get(azureUser);

        ProvisioningEntity targetEntity = azureUserToTargetEntity.get(azureUser);
        if (exception == null) {
          targetEntity.setProvisioned(true);
          for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetEntity.getInternal_objectChanges())) {
            provisioningObjectChange.setProvisioned(true);
          }
        } else {
          targetEntity.setProvisioned(false);
          targetEntity.setException(exception);
          for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetEntity.getInternal_objectChanges())) {
            provisioningObjectChange.setProvisioned(false);
          }
        }
        
      }
      
      return new TargetDaoDeleteEntitiesResponse();
    } catch (Exception e) {
      for (ProvisioningEntity targetEntity: targetEntities) {
        targetEntity.setProvisioned(false);
        targetEntity.setException(e);
        for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetEntity.getInternal_objectChanges())) {
          provisioningObjectChange.setProvisioned(false);
        }
      }
      throw e;
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("deleteEntities", startNanos));
    }
  }

}
