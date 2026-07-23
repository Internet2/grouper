package edu.internet2.middleware.grouper.app.ccure;

import com.fasterxml.jackson.databind.JsonNode;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningLists;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningAttribute;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningEntity;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningGroup;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningMembership;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningMembershipWrapper;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningObjectChange;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningObjectChangeAction;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.GrouperProvisionerDaoCapabilities;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.GrouperProvisionerTargetDaoBase;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoDeleteMembershipsRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoDeleteMembershipsResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoInsertMembershipsRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoInsertMembershipsResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveAllDataRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveAllDataResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveAllEntitiesRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveAllEntitiesResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveAllGroupsRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveAllGroupsResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveAllMembershipsRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveAllMembershipsResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveEntitiesRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveEntitiesResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveEntityRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveEntityResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveGroupRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveGroupResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveMembershipsByEntityRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveMembershipsByEntityResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveMembershipsByGroupRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveMembershipsByGroupResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoTimingInfo;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoUpdateEntitiesRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoUpdateEntitiesResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoUpdateEntityRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoUpdateEntityResponse;
import edu.internet2.middleware.grouper.util.GrouperHttpClient;
import edu.internet2.middleware.grouper.util.GrouperHttpClientLog;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncErrorCode;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import org.apache.commons.lang3.StringUtils;

import java.lang.ref.Cleaner;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class CCureTargetDao extends GrouperProvisionerTargetDaoBase {

  public static final int DEFAULT_BATCH_SIZE = 1000;
  public static final int RETRIEVE_ENTITIES_BATCH_SIZE = 1000;
  public static final int RETRIEVE_GROUPS_BATCH_SIZE = 1000;
  public static final int RETRIEVE_MEMBERSHIPS_BATCH_SIZE = 1000;
  public static final int INSERT_DELETE_MEMBERSHIP_BATCH_SIZE = 1000;

  /* implement lifecycle finalization: close the external system connection */
  private CCureExternalSystem ccureExternalSystem;
  private static final Cleaner cleaner = Cleaner.create();
  //private Cleaner.Cleanable cleanable;

  static class State implements Runnable {
    private final CCureExternalSystem ccureExternalSystem;

    State(CCureExternalSystem ccureExternalSystem) {
      this.ccureExternalSystem = ccureExternalSystem;
    }

    @Override
    public void run() {
      if (ccureExternalSystem != null && ccureExternalSystem.getSessionId() != null) {
        ccureExternalSystem.logout();
      }
    }
  }

  /* GRP-6448 - ProvisioningConsumer should call and implement disconnect so TargetDao can close connections */
  @Override
  public void finalize() {
    if (ccureExternalSystem != null && ccureExternalSystem.getSessionId() != null) {
      ccureExternalSystem.logout();
    }
  }

public CCureExternalSystem retrieveCcureExternalSystem() {
    if (this.ccureExternalSystem == null) {
      CCureConfiguration ccureConfiguration = (CCureConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
      CCureExternalSystem externalSystem = new CCureExternalSystem();
      externalSystem.setConfigId(ccureConfiguration.getExternalSystemConfigId());
      externalSystem.authenticate();
      this.ccureExternalSystem = externalSystem;
      //this.cleanable = cleaner.register(this, new State(externalSystem));
      cleaner.register(this, new State(externalSystem));
    }

    return this.ccureExternalSystem;
  }

  public record CCureMembership(int personnelId, int clearanceId, int objectId) {
    public static CCureMembership fromJson(JsonNode node) {

      if (node == null || !node.has("ObjectID")) {
        throw new  RuntimeException("Invalid JSON node, no data or is missing ObjectID: "
                +  (node == null ? "null" : node.toString()));
      }

      return new CCureMembership(
              GrouperUtil.jsonJacksonGetInteger(node, "PersonnelID"),
              GrouperUtil.jsonJacksonGetInteger(node, "ClearanceID"),
              GrouperUtil.jsonJacksonGetInteger(node, "ObjectID")
      );
    }

    public static CCureMembership fromProvisioningMembership(ProvisioningMembership targetMship, Set<String> fieldNamesToSet) {
      return new CCureMembership(
              targetMship.retrieveAttributeValueInteger("PersonnelID"),
              targetMship.retrieveAttributeValueInteger("ClearanceID"),
              targetMship.retrieveAttributeValueInteger("ObjectID")
      );
    }

    public ProvisioningMembership toProvisioningMembership() {
      ProvisioningMembership targetMembership = new ProvisioningMembership(false);
      targetMembership.assignAttributeValue("PersonnelID", this.personnelId);
      targetMembership.assignAttributeValue("ClearanceID", this.clearanceId);
      targetMembership.assignAttributeValue("ObjectID", this.objectId);
      return targetMembership;
    }

  }

  public record CCureUser(int personnelId, String guid, String name, String int1) {

    public ProvisioningEntity toProvisioningEntity() {
      ProvisioningEntity targetEntity = new ProvisioningEntity(false);
      targetEntity.assignAttributeValue("PersonnelID", this.personnelId);
      targetEntity.assignAttributeValue("GUID", this.guid);
      targetEntity.assignAttributeValue("Name", this.name);
      targetEntity.assignAttributeValue("Int1", this.int1);

      return targetEntity;
    }

    public static CCureUser fromProvisioningEntity(ProvisioningEntity targetEntity, Set<String> fieldNamesToSet) {
      return new CCureUser(
              targetEntity.retrieveAttributeValueInteger("PersonnelID"),
              targetEntity.retrieveAttributeValueString("GUID"),
              targetEntity.retrieveAttributeValueString("Name"),
              targetEntity.retrieveAttributeValueString("Int1")
      );
    }


    public static CCureUser fromJson(JsonNode node) {

      if (node == null || !node.has("GUID")) {
        throw new  RuntimeException("Invalid JSON node, no data or is missing GUID: "
                +  (node == null ? "null" : node.toString()));
      }

      Integer personnelId;
      if (node.has("PersonnelID")) {
        personnelId = GrouperUtil.jsonJacksonGetInteger(node, "PersonnelID");
      } else if (node.has("ObjectID")) {
        personnelId = GrouperUtil.jsonJacksonGetInteger(node, "ObjectID");
      } else {
        throw new RuntimeException("CCure user object missing either PersonnelID or ObjectID");
      }

      return new CCureUser(
              personnelId,
              GrouperUtil.jsonJacksonGetString(node, "GUID"),
              GrouperUtil.jsonJacksonGetString(node, "Name"),
              GrouperUtil.jsonJacksonGetString(node, "Int1")
      );
    }
  }

  public record CCureGroup(int objectId, String guid, String name, String partitionKey) {

    /* Called during phase retrieveTargetData */
    public ProvisioningGroup toProvisioningGroup() {
      ProvisioningGroup provisioningGroup = new ProvisioningGroup(false);
      provisioningGroup.assignAttributeValue("ObjectID", this.objectId);
      provisioningGroup.assignAttributeValue("GUID", this.guid);
      provisioningGroup.assignAttributeValue("Name", this.name);
      provisioningGroup.assignAttributeValue("PartitionID", this.partitionKey);
      return provisioningGroup;
    }

    public static CCureGroup fromProvisioningGroup(ProvisioningGroup targetGroup, Set<String> fieldNamesToSet) {
      return new CCureGroup(
              targetGroup.retrieveAttributeValueInteger("ObjectID"),
              targetGroup.retrieveAttributeValueString("GUID"),
              targetGroup.retrieveAttributeValueString("Name"),
              targetGroup.retrieveAttributeValueString("PartitionID")
      );
    }

    public static CCureGroup fromJson(JsonNode node) {

      if (node == null || !node.has("GUID")) {
        throw new  RuntimeException("Invalid JSON node, no data or is missing GUID: "
                +  (node == null ? "null" : node.toString()));
      }

      return new CCureGroup(
              GrouperUtil.jsonJacksonGetInteger(node, "ObjectID"),
              GrouperUtil.jsonJacksonGetString(node, "GUID"),
              GrouperUtil.jsonJacksonGetString(node, "Name"),
              GrouperUtil.jsonJacksonGetString(node, "PartitionID")
      );
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
  public void registerGrouperProvisionerDaoCapabilities(
      GrouperProvisionerDaoCapabilities grouperProvisionerDaoCapabilities) {
    
    grouperProvisionerDaoCapabilities.setDefaultBatchSize(DEFAULT_BATCH_SIZE);
    grouperProvisionerDaoCapabilities.setRetrieveEntitiesBatchSize(RETRIEVE_ENTITIES_BATCH_SIZE);
    grouperProvisionerDaoCapabilities.setRetrieveGroupsBatchSize(RETRIEVE_GROUPS_BATCH_SIZE);
    grouperProvisionerDaoCapabilities.setRetrieveMembershipsBatchSize(RETRIEVE_MEMBERSHIPS_BATCH_SIZE);
    grouperProvisionerDaoCapabilities.setInsertMembershipsBatchSize(INSERT_DELETE_MEMBERSHIP_BATCH_SIZE);
    grouperProvisionerDaoCapabilities.setDeleteMembershipsBatchSize(INSERT_DELETE_MEMBERSHIP_BATCH_SIZE);

    grouperProvisionerDaoCapabilities.setCanRetrieveAllData(true);
    grouperProvisionerDaoCapabilities.setCanRetrieveAllGroups(true);
    grouperProvisionerDaoCapabilities.setCanRetrieveAllEntities(true);
    grouperProvisionerDaoCapabilities.setCanRetrieveAllMemberships(true);
    grouperProvisionerDaoCapabilities.setCanRetrieveEntity(true);
    grouperProvisionerDaoCapabilities.setCanRetrieveGroup(true);
    grouperProvisionerDaoCapabilities.setCanRetrieveMembershipsAllByEntity(true);
    grouperProvisionerDaoCapabilities.setCanRetrieveMembershipsAllByGroup(true);
    grouperProvisionerDaoCapabilities.setCanInsertMemberships(true);
    grouperProvisionerDaoCapabilities.setCanDeleteMemberships(true);
    //grouperProvisionerDaoCapabilities.setCanUpdateEntity(true);
    //grouperProvisionerDaoCapabilities.setCanUpdateEntities(true);
  }

  @Override
  public TargetDaoRetrieveAllDataResponse retrieveAllData(TargetDaoRetrieveAllDataRequest targetDaoRetrieveAllDataRequest) {
    TargetDaoRetrieveAllDataResponse targetDaoRetrieveAllDataResponse = new TargetDaoRetrieveAllDataResponse();
    GrouperProvisioningLists targetData = new GrouperProvisioningLists();
    targetDaoRetrieveAllDataResponse.setTargetData(targetData);

    long startNanos = System.nanoTime();

    try {
      CCureConfiguration ccureConfiguration = (CCureConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();

      List<ProvisioningGroup> targetGroups = new ArrayList<>();

      CCureExternalSystem externalSystem = retrieveCcureExternalSystem();

      List<CCureGroup> ccureGroups = CCureApiCommands.retrieveGroups(externalSystem);

      for (CCureGroup ccureGroup : ccureGroups) {
        ProvisioningGroup targetGroup = ccureGroup.toProvisioningGroup();
        targetGroups.add(targetGroup);
      }
      targetData.setProvisioningGroups(targetGroups);

      List<ProvisioningEntity> targetEntities = new ArrayList();

      List<CCureUser> ccureUsers = CCureApiCommands.retrieveUsers(externalSystem);

      for (CCureUser ccureUser : ccureUsers) {
        ProvisioningEntity targetEntity = ccureUser.toProvisioningEntity();
        targetEntities.add(targetEntity);
      }

      targetData.setProvisioningEntities(targetEntities);

      List<ProvisioningMembership> targetMships = new ArrayList();

      List<CCureMembership> ccureMemberships = CCureApiCommands.retrieveMemberships(externalSystem);

      for (CCureMembership ccureMembership : ccureMemberships) {
        ProvisioningMembership targetMembership = ccureMembership.toProvisioningMembership();
        targetMembership.setProvisioningGroupId(String.valueOf(ccureMembership.clearanceId));
        targetMembership.setProvisioningEntityId(String.valueOf(ccureMembership.personnelId));

        targetMships.add(targetMembership);
      }

      targetData.setProvisioningMemberships(targetMships);


      return targetDaoRetrieveAllDataResponse;
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("retrieveAllData", startNanos));
    }
  }

  @Override
  public TargetDaoRetrieveAllGroupsResponse retrieveAllGroups(
      TargetDaoRetrieveAllGroupsRequest targetDaoRetrieveAllGroupsRequest) {

    long startNanos = System.nanoTime();

    try {

      CCureExternalSystem externalSystem = retrieveCcureExternalSystem();

      List<ProvisioningGroup> results = new ArrayList<ProvisioningGroup>();

      List<CCureGroup> ccureGroups = CCureApiCommands
          .retrieveGroups(externalSystem);

      for (CCureGroup ccureGroup : ccureGroups) {
        ProvisioningGroup targetGroup = ccureGroup.toProvisioningGroup();
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
      CCureExternalSystem externalSystem = retrieveCcureExternalSystem();

      List<ProvisioningEntity> results = new ArrayList<ProvisioningEntity>();

      List<CCureUser> ccureUsers = CCureApiCommands
          .retrieveUsers(externalSystem);

      for (CCureUser ccureUser : ccureUsers) {
        ProvisioningEntity targetEntity = ccureUser.toProvisioningEntity();
        results.add(targetEntity);
      }

      return new TargetDaoRetrieveAllEntitiesResponse(results);
    } finally {
      this.addTargetDaoTimingInfo(
          new TargetDaoTimingInfo("retrieveAllEntities", startNanos));
    }
  }

  @Override
  public TargetDaoRetrieveEntityResponse retrieveEntity(
      TargetDaoRetrieveEntityRequest targetDaoRetrieveEntityRequest) {

    long startNanos = System.nanoTime();

    try {
      CCureExternalSystem externalSystem = retrieveCcureExternalSystem();

      CCureUser ccureUser;

      if (StringUtils.equals("PersonnelID", targetDaoRetrieveEntityRequest.getSearchAttribute())) {
        ccureUser = CCureApiCommands.retrieveEntityByObjectId(externalSystem,
                GrouperUtil.stringValue(targetDaoRetrieveEntityRequest.getSearchAttributeValue()));
      } else if (StringUtils.equals("Int1", targetDaoRetrieveEntityRequest.getSearchAttribute())) {
        ccureUser = CCureApiCommands.retrieveEntityByMatchField(
                externalSystem,
                "Int1",
                GrouperUtil.stringValue(targetDaoRetrieveEntityRequest.getSearchAttributeValue()));
      } else {
        throw new RuntimeException("Not expecting search attribute '" + targetDaoRetrieveEntityRequest.getSearchAttribute() + "'");
      }

      ProvisioningEntity targetEntity = ccureUser == null ? null
              : ccureUser.toProvisioningEntity();

      TargetDaoRetrieveEntityResponse targetDaoRetrieveEntityResponse = new TargetDaoRetrieveEntityResponse(targetEntity);
      if (targetDaoRetrieveEntityRequest.isIncludeNativeEntity()) {
        targetDaoRetrieveEntityResponse.setTargetNativeEntity(ccureUser);
      }
      return targetDaoRetrieveEntityResponse;
    } catch (Exception e) {
      ProvisioningEntity targetEntity = targetDaoRetrieveEntityRequest.getTargetEntity();
      this.getGrouperProvisioner().retrieveGrouperProvisioningValidation().assignEntityError(targetEntity.getProvisioningEntityWrapper(), GcGrouperSyncErrorCode.ERR, e.getMessage());
      return new TargetDaoRetrieveEntityResponse(targetEntity);
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("retrieveEntity", startNanos));
    }
  }

  @Override
  public TargetDaoRetrieveGroupResponse retrieveGroup(TargetDaoRetrieveGroupRequest targetDaoRetrieveGroupRequest) {

    long startNanos = System.nanoTime();

    try {
      CCureExternalSystem externalSystem = retrieveCcureExternalSystem();

      ProvisioningGroup grouperTargetGroup = targetDaoRetrieveGroupRequest.getTargetGroup();

      CCureGroup ccureGroup = null;

      if (StringUtils.equals("ObjectID", targetDaoRetrieveGroupRequest.getSearchAttribute())) {
        ccureGroup = CCureApiCommands.retrieveGroupByObjectId(
                externalSystem,
                GrouperUtil.stringValue(targetDaoRetrieveGroupRequest.getSearchAttributeValue()));
      } else if (StringUtils.equals("Name", targetDaoRetrieveGroupRequest.getSearchAttribute())) {
        String name = GrouperUtil.stringValue(targetDaoRetrieveGroupRequest.getSearchAttributeValue());
        if (StringUtils.isNotBlank(name)) {
          ccureGroup = CCureApiCommands.retrieveGroupByName(
              externalSystem,
              GrouperUtil.stringValue(targetDaoRetrieveGroupRequest.getSearchAttributeValue()));
        }
        
      } else {
        throw new RuntimeException("Not expecting search attribute '" + targetDaoRetrieveGroupRequest.getSearchAttribute() + "'");
      }

      ProvisioningGroup targetGroup = ccureGroup == null ? null : ccureGroup.toProvisioningGroup();

      return new TargetDaoRetrieveGroupResponse(targetGroup);
    } catch (Exception e) {
      ProvisioningGroup targetGroup = targetDaoRetrieveGroupRequest.getTargetGroup();
      this.getGrouperProvisioner().retrieveGrouperProvisioningValidation().assignGroupError(targetGroup.getProvisioningGroupWrapper(), GcGrouperSyncErrorCode.ERR, e.getMessage());
      return new TargetDaoRetrieveGroupResponse(targetGroup);

    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("retrieveGroup", startNanos));
    }
  }
  
  private String resolveTargetEntityId(ProvisioningEntity targetEntity) {
    
    if (targetEntity == null) {
      return null;
    }
    
    if (StringUtils.isNotBlank(targetEntity.getId())) {
      return targetEntity.getId();
    }
    
    TargetDaoRetrieveEntitiesRequest targetDaoRetrieveEntitiesRequest = new TargetDaoRetrieveEntitiesRequest();
    targetDaoRetrieveEntitiesRequest.setTargetEntities(GrouperUtil.toList(targetEntity));
    targetDaoRetrieveEntitiesRequest.setIncludeAllMembershipsIfApplicable(false);
    TargetDaoRetrieveEntitiesResponse targetDaoRetrieveEntitiesResponse = this.getGrouperProvisioner()
            .retrieveGrouperProvisioningTargetDaoAdapter()
            .retrieveEntities(targetDaoRetrieveEntitiesRequest);

    if (targetDaoRetrieveEntitiesResponse == null || GrouperUtil.length(targetDaoRetrieveEntitiesResponse.getTargetEntities()) == 0) {
      return null;
    }
    
    return targetDaoRetrieveEntitiesResponse.getTargetEntities().get(0).getId();
    
  }

  @Override
  public TargetDaoRetrieveAllMembershipsResponse retrieveAllMemberships(TargetDaoRetrieveAllMembershipsRequest targetDaoRetrieveAllMembershipsRequest) {
    long startNanos = System.nanoTime();

    try {
      CCureExternalSystem externalSystem = retrieveCcureExternalSystem();

      List<ProvisioningMembership> results = new ArrayList<>();

      List<CCureMembership> ccureMships = CCureApiCommands.retrieveMemberships(externalSystem);

      for (CCureMembership ccureMship : ccureMships) {
        ProvisioningMembership targetMship = ccureMship.toProvisioningMembership();
        targetMship.setProvisioningGroupId(String.valueOf(ccureMship.clearanceId));
        targetMship.setProvisioningEntityId(String.valueOf(ccureMship.personnelId));
        results.add(targetMship);
      }

      return new TargetDaoRetrieveAllMembershipsResponse(results);
    } finally {
      this.addTargetDaoTimingInfo(
              new TargetDaoTimingInfo("retrieveAllEntities", startNanos));
    }
  }

  @Override
  public TargetDaoRetrieveMembershipsByEntityResponse retrieveMembershipsByEntity(TargetDaoRetrieveMembershipsByEntityRequest targetDaoRetrieveMembershipsByEntityRequest) {
    long startNanos = System.nanoTime();
    ProvisioningEntity targetEntity = targetDaoRetrieveMembershipsByEntityRequest.getTargetEntity();

    try {
      CCureExternalSystem externalSystem = retrieveCcureExternalSystem();

      //String targetEntityId = resolveTargetEntityId(targetEntity);
      String targetEntityId = targetEntity.retrieveAttributeValueString("PersonnelID");

      if (StringUtils.isBlank(targetEntityId)) {
        return new TargetDaoRetrieveMembershipsByEntityResponse(new ArrayList<ProvisioningMembership>());
      }

      List<CCureMembership> ccureMships = CCureApiCommands.retrieveMembershipsForUser(externalSystem, targetEntityId);
      
      List<ProvisioningMembership> provisioningMemberships = new ArrayList<ProvisioningMembership>();
      
      for (CCureMembership ccureMship : ccureMships) {

        ProvisioningMembership targetMembership = ccureMship.toProvisioningMembership();
        // targetMembership.setProvisioningGroupId(String.valueOf(ccureGroup.objectId()));
        // targetMembership.setProvisioningEntityId(targetEntity.getId());
        provisioningMemberships.add(targetMembership);
      }
  
      return new TargetDaoRetrieveMembershipsByEntityResponse(provisioningMemberships);
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("retrieveMembershipsByEntity", startNanos));
    }
  }
  
  @Override
  public TargetDaoRetrieveMembershipsByGroupResponse retrieveMembershipsByGroup(TargetDaoRetrieveMembershipsByGroupRequest targetDaoRetrieveMembershipsByGroupRequest) {
    ProvisioningGroup targetGroup = targetDaoRetrieveMembershipsByGroupRequest.getTargetGroup();
    long startNanos = System.nanoTime();

    try {
      CCureExternalSystem externalSystem = retrieveCcureExternalSystem();

      //String targetGroupId = resolveTargetGroupId(targetGroup);
      String targetGroupId = targetGroup.retrieveAttributeValueString("ObjectID");

      if (StringUtils.isBlank(targetGroupId)) {
        return new TargetDaoRetrieveMembershipsByGroupResponse(new ArrayList<ProvisioningMembership>());
      }

      List<CCureMembership> ccureMships = CCureApiCommands.retrieveMembershipsForGroup(externalSystem, targetGroupId);

      List<ProvisioningMembership> provisioningMemberships = new ArrayList<ProvisioningMembership>();

      for (CCureMembership ccureMship : ccureMships) {

        ProvisioningMembership targetMembership = ccureMship.toProvisioningMembership();
        // targetMembership.setProvisioningGroupId(String.valueOf(ccureGroup.objectId()));
        // targetMembership.setProvisioningEntityId(targetEntity.getId());
        provisioningMemberships.add(targetMembership);
      }

      return new TargetDaoRetrieveMembershipsByGroupResponse(provisioningMemberships);
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("retrieveMembershipsByEntity", startNanos));
    }  }

  @Override
  public TargetDaoUpdateEntityResponse updateEntity(TargetDaoUpdateEntityRequest targetDaoUpdateEntityRequest) {
    throw new UnsupportedOperationException();
  }

  @Override
  @Deprecated
  public TargetDaoUpdateEntitiesResponse updateEntities(TargetDaoUpdateEntitiesRequest targetDaoUpdateEntitiesRequest) {
    long startNanos = System.nanoTime();

    Map<String, Object> debugMap = new LinkedHashMap<>();
    debugMap.put("method", "updateEntities");

    for (ProvisioningEntity targetEntity: targetDaoUpdateEntitiesRequest.getTargetEntities()) {
      String entityId = (String) targetEntity.retrieveAttributeValue("PersonnelID");

      List<ProvisioningObjectChange> insertChanges = new ArrayList<>();
      List<ProvisioningObjectChange> deleteChanges = new ArrayList<>();

      for (ProvisioningObjectChange change : targetEntity.getInternal_objectChanges()) {
        if ("PersonnelClearancePair".equals(change.getAttributeName())) {
          if (change.getProvisioningObjectChangeAction() == ProvisioningObjectChangeAction.insert) {
            insertChanges.add(change);
          } else if (change.getProvisioningObjectChangeAction() == ProvisioningObjectChangeAction.delete) {
            deleteChanges.add(change);
          }
        }
      }

      try {
        Exception insertException = insertOrDeleteMembershipsForUser(entityId, insertChanges, MembershipChangeType.INSERT);
        if (insertException != null) {
          targetEntity.setException(insertException);
          setExceptionForMembershipsWhenEntityAttributes(targetEntity, insertChanges, insertException);
          throw insertException;
        } else {
          for (ProvisioningObjectChange change : insertChanges) {
            change.setProvisioned(true);
          }
        }

        Exception deleteException = insertOrDeleteMembershipsForUser(entityId, deleteChanges, MembershipChangeType.DELETE);
        if (deleteException != null) {
          targetEntity.setException(deleteException);
          setExceptionForMembershipsWhenEntityAttributes(targetEntity, deleteChanges, deleteException);
          throw deleteException;
        } else {
          for (ProvisioningObjectChange change : deleteChanges) {
            change.setProvisioned(true);
          }
        }

        targetEntity.setProvisioned(true);
      } catch (Exception e) {
        targetEntity.setProvisioned(false);
        targetEntity.setException(e);
        GrouperUtil.injectInException(e, GrouperUtil.mapToString(debugMap));
        throw new RuntimeException(e);
      }
    }

    this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("updateEntities", startNanos));

    return new TargetDaoUpdateEntitiesResponse();
  }

  @Override
  public TargetDaoDeleteMembershipsResponse deleteMemberships(TargetDaoDeleteMembershipsRequest targetDaoDeleteMembershipsRequest) {
    long startNanos = System.nanoTime();

    Map<String, Object> debugMap = new LinkedHashMap<>();
    debugMap.put("method", "deleteMemberships");

    CCureExternalSystem externalSystem = retrieveCcureExternalSystem();

    List<ProvisioningMembership> targetMemberships = targetDaoDeleteMembershipsRequest.getTargetMemberships();

    // CCure can't handle bundled clearanceIds, call to insert/delete individually
    for (ProvisioningMembership targetMembership : targetMemberships) {
      try {
        String personnelId = targetMembership.retrieveAttributeValueString("PersonnelID");

        // ObjectID is needed, but is missing for incrementals. We need to look it up in that case
        String objectId = targetMembership.retrieveAttributeValueString("ObjectID");
        if (GrouperUtil.isBlank(objectId)) {
          CCureMembership cCureMembership = CCureApiCommands.retrieveMembershipByGroupAndUser(externalSystem,
                  targetMembership.retrieveAttributeValueString("ClearanceID"),
                  personnelId
          );
          objectId = String.valueOf(cCureMembership.objectId);
        }

        Exception exception = CCureApiCommands.deleteMembershipsForUser(
                externalSystem,
                debugMap,
                personnelId,
                List.of(objectId));

        targetMembership.setException(exception);
        targetMembership.setProvisioned(exception == null);
      } catch (Exception e) {
        targetMembership.setException(e);
        targetMembership.setProvisioned(false);
      }
    }

    this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("deleteMemberships", startNanos));

    return new TargetDaoDeleteMembershipsResponse();
  }

  @Override
  public TargetDaoInsertMembershipsResponse insertMemberships(TargetDaoInsertMembershipsRequest targetDaoInsertMembershipsRequest) {
    long startNanos = System.nanoTime();

    Map<String, Object> debugMap = new LinkedHashMap<>();
    debugMap.put("method", "insertMemberships");

    CCureExternalSystem externalSystem = retrieveCcureExternalSystem();

    List<ProvisioningMembership> targetMemberships = targetDaoInsertMembershipsRequest.getTargetMemberships();

    // CCure can't handle bundled clearanceIds, call to insert/delete individually
    for (ProvisioningMembership targetMembership : targetMemberships) {
      try {
        String personnelId = targetMembership.retrieveAttributeValueString("PersonnelID");
        String clearanceId = targetMembership.retrieveAttributeValueString("ClearanceID");

        Exception exception = CCureApiCommands.insertMembershipsForUser(
                externalSystem,
                debugMap,
                personnelId,
                List.of(clearanceId));

        targetMembership.setException(exception);
        targetMembership.setProvisioned(exception == null);
      } catch (Exception e) {
        targetMembership.setException(e);
        targetMembership.setProvisioned(false);
      }
    }

    this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("deleteMemberships", startNanos));

    return new TargetDaoInsertMembershipsResponse();
  }

  @Deprecated
  enum MembershipChangeType {INSERT, DELETE}

  @Deprecated
  private Exception insertOrDeleteMembershipsForUser(String personnelId, List<ProvisioningObjectChange> changes, MembershipChangeType changeType ) {
    if (GrouperUtil.isBlank(changes)) {
      return null;
    }

    Map<String, Object> debugMap = new LinkedHashMap<>();

    debugMap.put("method", "insertOrDeleteMembershipsForUser (" + changeType.name() + ")");

    long startTime = System.nanoTime();

    Exception entityOverallException = null;

    try {
      int numberOfHttpRequests = GrouperUtil.batchNumberOfBatches(
              changes,
              CCureTargetDao.INSERT_DELETE_MEMBERSHIP_BATCH_SIZE,
              false);

      debugMap.put("numberOfHttpRequests", numberOfHttpRequests);

      for (int httpRequestIndex = 0; httpRequestIndex < numberOfHttpRequests; httpRequestIndex++) {
        List<ProvisioningObjectChange> changesInOneList = GrouperUtil.batchList(
                changes,
                CCureTargetDao.INSERT_DELETE_MEMBERSHIP_BATCH_SIZE,
                httpRequestIndex);

        List<String> clearanceIdsInOneList;
        Exception exception;

        CCureExternalSystem externalSystem = retrieveCcureExternalSystem();

        if (changeType == MembershipChangeType.INSERT) {
          clearanceIdsInOneList = changesInOneList.stream().map(provisioningObjectChange ->
                  (String)provisioningObjectChange.getNewValue()
          ).collect(Collectors.toList());

          exception = CCureApiCommands.insertMembershipsForUser(
                  externalSystem,
                  debugMap,
                  personnelId,
                  clearanceIdsInOneList);
        } else {
          clearanceIdsInOneList = changesInOneList.stream().map(provisioningObjectChange ->
                  (String)provisioningObjectChange.getOldValue()
          ).collect(Collectors.toList());

          exception = CCureApiCommands.deleteMembershipsForUser(
                  externalSystem,
                  debugMap,
                  personnelId,
                  clearanceIdsInOneList);
        }

        if (exception == null) {
          for (ProvisioningObjectChange change: changesInOneList) {
            change.setProvisioned(true);
          }
        } else {
          for (ProvisioningObjectChange change: changesInOneList) {
            change.setProvisioned(false);
            change.setException(exception);
            entityOverallException = exception;
          }
        }
      }
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      return re;
    } finally {
      CCureLog.log(debugMap, startTime);
    }

    return entityOverallException;
  }

  /**
   * This duplicates some of the functionality of the same method in the TargetDaoAdapter. However, that method
   * sets the error message but not the error code (GRP-6173), so membership errors get missed
   */
  public void setExceptionForMembershipsWhenEntityAttributes(ProvisioningEntity targetEntity, List<ProvisioningObjectChange> changes, Exception e) {
    Collection<ProvisioningObjectChange> provisionObjectChanges = targetEntity.getInternal_objectChanges();
    for (ProvisioningObjectChange provisioningObjectChange: provisionObjectChanges) {
      if (provisioningObjectChange.getException() != null) {

        ProvisioningAttribute provisioningAttribute = targetEntity.retrieveProvisioningAttribute(provisioningObjectChange.getAttributeName());

        if (provisioningAttribute != null) {
          Map<Object, ProvisioningMembershipWrapper> valueToProvisioningMembershipWrapper = provisioningAttribute.getValueToProvisioningMembershipWrapper();

          if (valueToProvisioningMembershipWrapper != null) {


            ProvisioningMembershipWrapper provisioningMembershipWrapper;
            if (provisioningObjectChange.getProvisioningObjectChangeAction() == ProvisioningObjectChangeAction.insert ||
                    provisioningObjectChange.getProvisioningObjectChangeAction() == ProvisioningObjectChangeAction.update) {

              provisioningMembershipWrapper = valueToProvisioningMembershipWrapper.get(provisioningObjectChange.getNewValue());
            } else {
              provisioningMembershipWrapper = valueToProvisioningMembershipWrapper.get(provisioningObjectChange.getOldValue());
            }

            if (provisioningMembershipWrapper.getGrouperTargetMembership() != null) {
              provisioningMembershipWrapper.getGrouperTargetMembership().setException(e);
              provisioningMembershipWrapper.getGrouperTargetMembership().setProvisioned(false);
            } else {
              if (provisioningMembershipWrapper.getGcGrouperSyncMembership() != null) {
                provisioningMembershipWrapper.getGcGrouperSyncMembership().setErrorMessage(GrouperUtil.getFullStackTrace(e));
                provisioningMembershipWrapper.getGcGrouperSyncMembership().setErrorCode(GcGrouperSyncErrorCode.ERR);
                Timestamp membershipErrorTimestamp = this.getGrouperProvisioner().retrieveGrouperProvisioningSyncDao().membershipErrorTimestamp(provisioningMembershipWrapper.getGcGrouperSyncMembership());
                provisioningMembershipWrapper.getGcGrouperSyncMembership().setErrorTimestamp(membershipErrorTimestamp);
              }
            }
          }
        }
      }
    }
  }
}
