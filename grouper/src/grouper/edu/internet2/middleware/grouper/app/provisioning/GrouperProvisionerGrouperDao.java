package edu.internet2.middleware.grouper.app.provisioning;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.hibernate.type.StringType;
import org.hibernate.type.Type;

import edu.internet2.middleware.grouper.FieldFinder;
import edu.internet2.middleware.grouper.hibernate.HibUtils;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSync;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncDao;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncGroup;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncMember;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncMembership;

public class GrouperProvisionerGrouperDao {

  /**
   * reference back up to the provisioner
   */
  private GrouperProvisioner grouperProvisioner = null;

  /**
   * reference back up to the provisioner
   * @return the provisioner
   */
  public GrouperProvisioner getGrouperProvisioner() {
    return this.grouperProvisioner;
  }

  /**
   * reference back up to the provisioner
   * @param grouperProvisioner1
   */
  public void setGrouperProvisioner(GrouperProvisioner grouperProvisioner1) {
    this.grouperProvisioner = grouperProvisioner1;
  }

  
  public List<ProvisioningGroup> retrieveAllGroups() {
    
    if (this.grouperProvisioner == null) {
      throw new RuntimeException("grouperProvisioner is not set");
    }
    
    List<ProvisioningGroup> results = new ArrayList<ProvisioningGroup>();
    
    StringBuilder sql = new StringBuilder("select gg.id, gg.name, gg.display_name, gg.description, gg.id_index " + 
        "from grouper_sync gs, grouper_sync_group gsg, grouper_groups gg " + 
        "where gs.provisioner_name = ? " + 
        "and gsg.grouper_sync_id = gs.id " + 
        "and gsg.provisionable = 'T' " + 
        "and gsg.group_id = gg.id");

    List<String[]> queryResults = HibernateSession.bySqlStatic().listSelect(String[].class, sql.toString(),
        GrouperUtil.toListObject(this.grouperProvisioner.getConfigId()), HibUtils.listType(StringType.INSTANCE));

    List<ProvisioningGroup> provisioningGroupsFromGrouper = getTargetGroupMapFromQueryResults(queryResults);
    results.addAll(provisioningGroupsFromGrouper);
    
    Map<String, ProvisioningGroup> grouperGroupIdToGrouperProvisioningGroup = new HashMap<String, ProvisioningGroup>();
    
    for (ProvisioningGroup grouperProvisioningGroup: provisioningGroupsFromGrouper) {
      grouperGroupIdToGrouperProvisioningGroup.put(grouperProvisioningGroup.getId(), grouperProvisioningGroup);
    }
    
    GcGrouperSync gcGrouperSync = GcGrouperSyncDao.retrieveOrCreateByProvisionerName(null, this.grouperProvisioner.getConfigId());
    
    List<GcGrouperSyncGroup> gcGrouperSyncGroups = gcGrouperSync.getGcGrouperSyncGroupDao().groupRetrieveAll();
    
    Iterator<GcGrouperSyncGroup> iterator = GrouperUtil.nonNull(gcGrouperSyncGroups).iterator();

    Map<String, ProvisioningGroupWrapper> groupUuidToProvisioningGroupWrapper = this.getGrouperProvisioner().getGrouperProvisioningData().getGroupUuidToProvisioningGroupWrapper();

    while (iterator.hasNext()) {
      
      GcGrouperSyncGroup gcGrouperSyncGroup = iterator.next();
      this.getGrouperProvisioner().getGrouperProvisioningData().getGroupUuidToSyncGroup()
        .put(gcGrouperSyncGroup.getGroupId(), gcGrouperSyncGroup);
      
      // if a group has been deleted in grouper_groups table but copy still exists in grouper_sync_group
      // we are sending the copy over to the target so that target can also delete
      if (!grouperGroupIdToGrouperProvisioningGroup.containsKey(gcGrouperSyncGroup.getId())) {
        
        ProvisioningGroup provisioningGroup = new ProvisioningGroup();
        provisioningGroup.setId(gcGrouperSyncGroup.getGroupId());
        provisioningGroup.setName(gcGrouperSyncGroup.getGroupName());
        provisioningGroup.setIdIndex(gcGrouperSyncGroup.getGroupIdIndex());
        results.add(provisioningGroup);
        
        ProvisioningGroupWrapper provisioningGroupWrapper = new ProvisioningGroupWrapper();
        provisioningGroup.setProvisioningGroupWrapper(provisioningGroupWrapper);
        provisioningGroupWrapper.setGrouperProvisioningGroup(provisioningGroup);
        provisioningGroupWrapper.setGcGrouperSyncGroup(gcGrouperSyncGroup);
        
        groupUuidToProvisioningGroupWrapper.put(gcGrouperSyncGroup.getGroupId(), provisioningGroupWrapper);
        
      } else {
        ProvisioningGroup grouperProvisioningGroup = grouperGroupIdToGrouperProvisioningGroup.get(gcGrouperSyncGroup.getId());
        grouperProvisioningGroup.getProvisioningGroupWrapper().setGcGrouperSyncGroup(gcGrouperSyncGroup);
      }
      
    }
    
    return results;
  }
  
  public Map<String, ProvisioningGroup> retrieveGroupsByIds(Collection<String> ids) {
    
    if (this.grouperProvisioner == null) {
      throw new RuntimeException("grouperProvisioner is not set");
    }
    
    List<String> idsList = GrouperUtil.listFromCollection(ids);

    Map<String, ProvisioningGroup> results = new HashMap<String, ProvisioningGroup>();
    
    if (GrouperUtil.length(ids) == 0) {
      return results;
    }
    
    String sqlPrefix = "select gg.id, gg.name, gg.display_name, gg.description, gg.id_index " + 
        "from grouper_sync gs, grouper_sync_group gsg, grouper_groups gg " + 
        "where gs.provisioner_name = ? " + 
        "and gsg.grouper_sync_id = gs.id " + 
        "and gsg.provisionable = 'T' " + 
        "and gsg.group_id = gg.id";
    
    int numberOfBatches = GrouperUtil.batchNumberOfBatches(ids.size(), 900);
    for (int i = 0; i < numberOfBatches; i++) {
      List<String> currentBatchIds = GrouperUtil.batchList(idsList, 900, i);
      
      List<Object> params = new ArrayList<Object>();
      params.add(this.grouperProvisioner.getConfigId());
      params.addAll(currentBatchIds);

      List<Type> types = new ArrayList<Type>();
      types.add(StringType.INSTANCE);
      for (int j = 0; j < GrouperUtil.length(currentBatchIds); j++) {
        types.add(StringType.INSTANCE);
      }
      
      StringBuilder sql = new StringBuilder(sqlPrefix);
      sql.append(" and gsg.group_id in (");
      sql.append(HibUtils.convertToInClauseForSqlStatic(currentBatchIds));
      sql.append(") ");
      
      List<String[]> queryResults = HibernateSession.bySqlStatic().listSelect(String[].class, sql.toString(), params, types);
      
      for (ProvisioningGroup provisioningGroup: getTargetGroupMapFromQueryResults(queryResults)) {
        results.put(provisioningGroup.getId(), provisioningGroup);
      }
      
    }
    
    return results;
  }
  
  public List<ProvisioningEntity> retrieveAllMembers() {
    
    if (this.grouperProvisioner == null) {
      throw new RuntimeException("grouperProvisioner is not set");
    }
    
    List<ProvisioningEntity> results = new ArrayList<ProvisioningEntity>();

    StringBuilder sql = new StringBuilder("select gm.id, gm.subject_id, gm.subject_identifier0, gm.name, gm.description " +
        "from grouper_sync gs, grouper_sync_member gsm, grouper_members gm " + 
        "where gs.provisioner_name = ? " +
        "and gsm.grouper_sync_id = gs.id " +
        "and gsm.provisionable = 'T' " +
        "and gsm.member_id = gm.id");
    
    List<String> subjectSources = new ArrayList<String>(grouperProvisioner.retrieveProvisioningConfiguration().getSubjectSourcesToProvision());
    
    List<Object> params = new ArrayList<Object>();
    params.add(this.grouperProvisioner.getConfigId());
    params.addAll(subjectSources);
    
    List<Type> types = new ArrayList<Type>();
    types.add(StringType.INSTANCE);
    for (int j = 0; j < GrouperUtil.length(subjectSources); j++) {
      types.add(StringType.INSTANCE);
    }
    
    sql.append(" and gm.subject_source in (");
    sql.append(HibUtils.convertToInClauseForSqlStatic(subjectSources));
    sql.append(") ");
    
    List<String[]> queryResults = HibernateSession.bySqlStatic().listSelect(String[].class, sql.toString(), params, types);

    List<ProvisioningEntity> grouperProvisioningEntities = getProvisioningEntityMapFromQueryResults(queryResults);
    results.addAll(grouperProvisioningEntities);
    
    Map<String, ProvisioningEntity> grouperMemberIdToProvisioningEntity = new HashMap<String, ProvisioningEntity>();
    
    for (ProvisioningEntity grouperProvisioningEntity: grouperProvisioningEntities) {
      grouperMemberIdToProvisioningEntity.put(grouperProvisioningEntity.getId(), grouperProvisioningEntity);
    }
    
    GcGrouperSync gcGrouperSync = GcGrouperSyncDao.retrieveOrCreateByProvisionerName(null, this.grouperProvisioner.getConfigId());
    
    List<GcGrouperSyncMember> gcGrouperSyncMembers = gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveAll();
    Map<String, ProvisioningEntityWrapper> memberUuidToProvisioningEntityWrapper = this.getGrouperProvisioner().getGrouperProvisioningData().getMemberUuidToProvisioningEntityWrapper();

    for (GcGrouperSyncMember gcGrouperSyncMember: GrouperUtil.nonNull(gcGrouperSyncMembers)) {
      
      this.getGrouperProvisioner().getGrouperProvisioningData().getMemberUuidToSyncMember()
        .put(gcGrouperSyncMember.getMemberId(), gcGrouperSyncMember);
      
      // if a member has been deleted in grouper_members table but copy still exists in grouper_sync_member
      // we are sending the copy over to the target so that target can also delete
      if (!grouperMemberIdToProvisioningEntity.containsKey(gcGrouperSyncMember.getId())) {
        ProvisioningEntity provisioningEntity = new ProvisioningEntity();
        provisioningEntity.setId(gcGrouperSyncMember.getMemberId());

        provisioningEntity.assignAttribute("subjectId", gcGrouperSyncMember.getSubjectId());
        provisioningEntity.assignAttribute("subjectIdentifier0", gcGrouperSyncMember.getSubjectIdentifier());

        results.add(provisioningEntity);
        
        ProvisioningEntityWrapper provisioningEntityWrapper = new ProvisioningEntityWrapper();
        provisioningEntity.setProvisioningEntityWrapper(provisioningEntityWrapper);
        provisioningEntityWrapper.setGrouperProvisioningEntity(provisioningEntity);
        provisioningEntityWrapper.setGcGrouperSyncMember(gcGrouperSyncMember);
        
        memberUuidToProvisioningEntityWrapper.put(provisioningEntity.getId(), provisioningEntityWrapper);

        
      } else {
        ProvisioningEntity grouperProvisioningEntity = grouperMemberIdToProvisioningEntity.get(gcGrouperSyncMember.getId());
        grouperProvisioningEntity.getProvisioningEntityWrapper().setGcGrouperSyncMember(gcGrouperSyncMember);
      }
      
    }
    
    
    
    return results;
  }
  
  public List<ProvisioningMembership> retrieveAllMemberships() {
    
    if (this.grouperProvisioner == null) {
      throw new RuntimeException("grouperProvisioner is not set");
    }
    
    List<ProvisioningMembership> results = new ArrayList<ProvisioningMembership>();
    

    StringBuilder sql = new StringBuilder("select gmav.membership_id, gg.id, gm.id, gm.subject_id, gm.subject_identifier0, gm.name, gm.description, gg.name, gg.display_name, gg.description, gg.id_index " +
        "from grouper_sync gs, grouper_sync_group gsg, grouper_groups gg, grouper_memberships_all_v gmav, grouper_sync_member gsm, grouper_members gm " + 
        "where gs.provisioner_name = ? " +
        "and gsm.grouper_sync_id = gs.id " +
        "and gsg.grouper_sync_id = gs.id " +
        "and gsg.group_id = gg.id " +
        "and gmav.owner_group_id = gg.id " +
        "and gmav.member_id = gm.id " +
        "and gsm.provisionable = 'T' " +
        "and gsg.provisionable = 'T' " +
        "and gsm.member_id = gm.id");
    
    List<String> subjectSources = new ArrayList<String>(grouperProvisioner.retrieveProvisioningConfiguration().getSubjectSourcesToProvision());
    
    List<String> fieldIds = new ArrayList<String>();
    GrouperProvisioningMembershipFieldType membershipFieldType = grouperProvisioner.retrieveProvisioningConfiguration().getGrouperProvisioningMembershipFieldType();
    
    if (membershipFieldType == GrouperProvisioningMembershipFieldType.members) {
      fieldIds.add(FieldFinder.find("members", true).getId());
    } else if (membershipFieldType == GrouperProvisioningMembershipFieldType.admin) {
      fieldIds.add(FieldFinder.find("admins", true).getId());
    } else if (membershipFieldType == GrouperProvisioningMembershipFieldType.readAdmin) {
      fieldIds.add(FieldFinder.find("admins", true).getId());
      fieldIds.add(FieldFinder.find("readers", true).getId());
    } else if (membershipFieldType == GrouperProvisioningMembershipFieldType.updateAdmin) {
      fieldIds.add(FieldFinder.find("admins", true).getId());
      fieldIds.add(FieldFinder.find("updaters", true).getId());
    } else {
      throw new RuntimeException("Unexpected field type: " + membershipFieldType.name());
    }
    
    List<Object> params = new ArrayList<Object>();
    params.add(this.grouperProvisioner.getConfigId());
    params.addAll(subjectSources);
    params.addAll(fieldIds);
    
    List<Type> types = new ArrayList<Type>();
    types.add(StringType.INSTANCE);
    for (int j = 0; j < (GrouperUtil.length(subjectSources) + GrouperUtil.length(fieldIds)); j++) {
      types.add(StringType.INSTANCE);
    }
    
    sql.append(" and gm.subject_source in (");
    sql.append(HibUtils.convertToInClauseForSqlStatic(subjectSources));
    sql.append(") ");
    
    sql.append(" and gmav.field_id in (");
    sql.append(HibUtils.convertToInClauseForSqlStatic(fieldIds));
    sql.append(") ");
    
    List<String[]> queryResults = HibernateSession.bySqlStatic().listSelect(String[].class, sql.toString(), params, types);

    List<ProvisioningMembership> grouperProvisioningMemberships = getProvisioningMembershipMapFromQueryResults(queryResults);
    results.addAll(grouperProvisioningMemberships);
    
    
    // grouper_group_id, member_id to grouper provisioning membership
    Map<MultiKey, ProvisioningMembership> grouperGroupIdMemberIdToGrouperProvisioningMembership = new HashMap<MultiKey, ProvisioningMembership>();
    
    for (ProvisioningMembership grouperProvisioningMembership: grouperProvisioningMemberships) {
      grouperGroupIdMemberIdToGrouperProvisioningMembership.put(
          new MultiKey(grouperProvisioningMembership.getProvisioningGroupId(), grouperProvisioningMembership.getProvisioningEntityId()),
          grouperProvisioningMembership);
    }

    
    Map<String, ProvisioningGroupWrapper> groupUuidToProvisioningGroupWrapper = this.getGrouperProvisioner().getGrouperProvisioningData().getGroupUuidToProvisioningGroupWrapper();
    Map<String, ProvisioningEntityWrapper> memberUuidToProvisioningEntityWrapper = this.getGrouperProvisioner().getGrouperProvisioningData().getMemberUuidToProvisioningEntityWrapper();

    for (ProvisioningMembership provisioningMembership : GrouperUtil.nonNull(grouperProvisioningMemberships)) {
      
      String groupUuid = provisioningMembership.getProvisioningGroupId();
      ProvisioningGroupWrapper provisioningGroupWrapper = groupUuidToProvisioningGroupWrapper.get(groupUuid);
      
      if (provisioningGroupWrapper == null || provisioningGroupWrapper.getGrouperProvisioningGroup() != null) {
        ProvisioningGroup provisioningGroup = provisioningMembership.getProvisioningGroup();
        provisioningGroupWrapper = new ProvisioningGroupWrapper();
        provisioningGroupWrapper.setGrouperProvisioningGroup(provisioningGroup);
        provisioningGroup.setProvisioningGroupWrapper(provisioningGroupWrapper);
        groupUuidToProvisioningGroupWrapper.put(provisioningGroup.getId(), provisioningGroupWrapper);
        this.getGrouperProvisioner().getGrouperProvisioningData().getGrouperProvisioningObjects().getProvisioningGroups().add(provisioningGroup);
      } else {
        provisioningMembership.setProvisioningGroup(provisioningGroupWrapper.getGrouperProvisioningGroup());
      }
      
      String memberUuid = provisioningMembership.getProvisioningEntityId();
      ProvisioningEntityWrapper provisioningEntityWrapper = memberUuidToProvisioningEntityWrapper.get(memberUuid);

      if (provisioningEntityWrapper == null || provisioningEntityWrapper.getGrouperProvisioningEntity() != null) {
        ProvisioningEntity provisioningEntity = provisioningMembership.getProvisioningEntity();
        provisioningEntityWrapper = new ProvisioningEntityWrapper();
        provisioningEntityWrapper.setGrouperProvisioningEntity(provisioningEntity);
        provisioningEntity.setProvisioningEntityWrapper(provisioningEntityWrapper);
        memberUuidToProvisioningEntityWrapper.put(provisioningEntity.getId(), provisioningEntityWrapper);
        this.getGrouperProvisioner().getGrouperProvisioningData().getGrouperProvisioningObjects().getProvisioningEntities().add(provisioningEntity);
      } else {
        provisioningMembership.setProvisioningEntity(provisioningEntityWrapper.getGrouperProvisioningEntity());
      }
      
    }
    
    
    
    GcGrouperSync gcGrouperSync = GcGrouperSyncDao.retrieveOrCreateByProvisionerName(null, this.grouperProvisioner.getConfigId());
    
    List<GcGrouperSyncMembership> gcGrouperSyncMemberships = gcGrouperSync.getGcGrouperSyncMembershipDao().membershipRetrieveAll();
    
    Iterator<GcGrouperSyncMembership> iterator = GrouperUtil.nonNull(gcGrouperSyncMemberships).iterator();
    
    
    Map<String, GcGrouperSyncGroup> grouperSyncGroupIdToGrouperSyncGroup = new HashMap<String, GcGrouperSyncGroup>();
    Map<String, GcGrouperSyncMember> grouperSyncMemberIdToGrouperSyncMember = new HashMap<String, GcGrouperSyncMember>();
    
    for (GcGrouperSyncGroup gcGrouperSyncGroup: 
      GrouperUtil.nonNull(this.getGrouperProvisioner().getGrouperProvisioningData().getGroupUuidToSyncGroup()).values()) {
      
      grouperSyncGroupIdToGrouperSyncGroup.put(gcGrouperSyncGroup.getId(), gcGrouperSyncGroup);
      
    }
    
    for (GcGrouperSyncMember gcGrouperSyncMember: 
      GrouperUtil.nonNull(this.getGrouperProvisioner().getGrouperProvisioningData().getMemberUuidToSyncMember()).values()) {
      
      grouperSyncMemberIdToGrouperSyncMember.put(gcGrouperSyncMember.getId(), gcGrouperSyncMember);
      
    }
    
    Map<MultiKey, ProvisioningMembershipWrapper> groupIdMemberIdToProvisioningMembershipWrapper = this.getGrouperProvisioner().getGrouperProvisioningData().getGroupIdMemberIdToProvisioningMembershipWrapper();
    
    while (iterator.hasNext()) {
      
      GcGrouperSyncMembership gcGrouperSyncMembership = iterator.next();
      
      GcGrouperSyncGroup gcGrouperSyncGroup = grouperSyncGroupIdToGrouperSyncGroup.get(gcGrouperSyncMembership.getGrouperSyncGroupId());
      if (gcGrouperSyncGroup == null) {
        iterator.remove();
        continue;
      }
      
      GcGrouperSyncMember gcGrouperSyncMember = grouperSyncMemberIdToGrouperSyncMember.get(gcGrouperSyncMembership.getGrouperSyncMemberId());
      
      if (gcGrouperSyncMember == null) {
        iterator.remove();
        continue;
      }
      
      this.getGrouperProvisioner().getGrouperProvisioningData().getGroupIdMemberIdToSyncMembership()
        .put(new MultiKey(gcGrouperSyncMembership.getGrouperSyncGroupId(), gcGrouperSyncMembership.getGrouperSyncMemberId()), 
            gcGrouperSyncMembership);
      
      
      MultiKey groupIdMemberId = new MultiKey(gcGrouperSyncGroup.getGroupId(), gcGrouperSyncMember.getMemberId());
      
      // if a membership has been deleted in grouper_memberships_all_v table but copy still exists in grouper_sync_member
      // we are sending the copy over to the target so that target can also delete
      if (!grouperGroupIdMemberIdToGrouperProvisioningMembership.containsKey(groupIdMemberId)) {
        
        ProvisioningMembership provisioningMembership = new ProvisioningMembership();
        
        provisioningMembership.setProvisioningGroupId(gcGrouperSyncGroup.getId());
        provisioningMembership.setProvisioningEntityId(gcGrouperSyncMember.getId());

        results.add(provisioningMembership);
        
        ProvisioningMembershipWrapper provisioningMembershipWrapper = new ProvisioningMembershipWrapper();
        provisioningMembership.setProvisioningMembershipWrapper(provisioningMembershipWrapper);
        provisioningMembershipWrapper.setGrouperProvisioningMembership(provisioningMembership);
        provisioningMembershipWrapper.setGcGrouperSyncMembership(gcGrouperSyncMembership);
        
        groupIdMemberIdToProvisioningMembershipWrapper.put(new MultiKey(provisioningMembership.getProvisioningGroupId(), provisioningMembership.getProvisioningEntityId()), provisioningMembershipWrapper);

      } else {
        ProvisioningMembership grouperProvisioningMembership = grouperGroupIdMemberIdToGrouperProvisioningMembership.get(groupIdMemberId);
        grouperProvisioningMembership.getProvisioningMembershipWrapper().setGcGrouperSyncMembership(gcGrouperSyncMembership);
      }
      
    }
    
    return results;
  }
  
  public Map<String, ProvisioningEntity> retrieveMembersByIds(Collection<String> ids) {
    
    if (this.grouperProvisioner == null) {
      throw new RuntimeException("grouperProvisioner is not set");
    }
    
    List<String> idsList = GrouperUtil.listFromCollection(ids);
    
    Map<String, ProvisioningEntity> results = new HashMap<String, ProvisioningEntity>();
    
    if (GrouperUtil.length(ids) == 0) {
      return results;
    }
    
    StringBuilder sqlPrefix = new StringBuilder("select gm.id, gm.subject_id, gm.subject_identifier0, gm.name, gm.description " +
        "from grouper_sync gs, grouper_sync_member gsm, grouper_members gm " + 
        "where gs.provisioner_name = ? " +
        "and gsm.grouper_sync_id = gs.id " +
        "and gsm.provisionable = 'T' " +
        "and gsm.member_id = gm.id");
    
    List<String> subjectSources = new ArrayList<String>(grouperProvisioner.retrieveProvisioningConfiguration().getSubjectSourcesToProvision());
    
    List<Object> params = new ArrayList<Object>();
    params.add(this.grouperProvisioner.getConfigId());
    params.addAll(subjectSources);
    
    List<Type> types = new ArrayList<Type>();
    types.add(StringType.INSTANCE);
    for (int j = 0; j < GrouperUtil.length(subjectSources); j++) {
      types.add(StringType.INSTANCE);
    }
    
    sqlPrefix.append(" and gm.subject_source in (");
    sqlPrefix.append(HibUtils.convertToInClauseForSqlStatic(subjectSources));
    sqlPrefix.append(") ");
    
    int numberOfBatches = GrouperUtil.batchNumberOfBatches(ids.size(), 900);
    for (int i = 0; i < numberOfBatches; i++) {
      List<String> currentBatchIds = GrouperUtil.batchList(idsList, 900, i);
      
      List<Object> paramsCurrent = new ArrayList<Object>(params);
      paramsCurrent.addAll(currentBatchIds);

      List<Type> typesCurrent = new ArrayList<Type>(types);
      for (int j = 0; j < GrouperUtil.length(currentBatchIds); j++) {
        typesCurrent.add(StringType.INSTANCE);
      }
      
      StringBuilder sql = new StringBuilder(sqlPrefix);
      sql.append(" and gsm.member_id in (");
      sql.append(HibUtils.convertToInClauseForSqlStatic(currentBatchIds));
      sql.append(") ");
      
      List<String[]> queryResults = HibernateSession.bySqlStatic().listSelect(String[].class, sql.toString(), paramsCurrent, typesCurrent);
      
      for (ProvisioningEntity provisioningEntity: getProvisioningEntityMapFromQueryResults(queryResults)) {
        results.put(provisioningEntity.getId(), provisioningEntity);
      }
      
    }
    
    return results;
  }
  
  private List<ProvisioningGroup> getTargetGroupMapFromQueryResults(List<String[]> queryResults) {
    
    List<ProvisioningGroup> results = new ArrayList<ProvisioningGroup>();

    Map<String, ProvisioningGroupWrapper> groupUuidToProvisioningGroupWrapper = this.getGrouperProvisioner().getGrouperProvisioningData().getGroupUuidToProvisioningGroupWrapper();

    for (String[] queryResult : queryResults) {
      String id = queryResult[0];
      String name = queryResult[1];
      String displayName = queryResult[2];
      String description = queryResult[3];
      String idIndex = queryResult[4];
      
      ProvisioningGroup grouperProvisioningGroup = new ProvisioningGroup();
      Map<String, ProvisioningAttribute> attributes = new HashMap<String, ProvisioningAttribute>();
      grouperProvisioningGroup.setId(id);
      grouperProvisioningGroup.setName(name);
      grouperProvisioningGroup.setDisplayName(displayName);
      grouperProvisioningGroup.setIdIndex(Long.parseLong(idIndex));
      grouperProvisioningGroup.assignAttribute("description", description);
      
      ProvisioningGroupWrapper provisioningGroupWrapper = new ProvisioningGroupWrapper();
      grouperProvisioningGroup.setProvisioningGroupWrapper(provisioningGroupWrapper);
      provisioningGroupWrapper.setGrouperProvisioningGroup(grouperProvisioningGroup);
      
      groupUuidToProvisioningGroupWrapper.put(grouperProvisioningGroup.getId(), provisioningGroupWrapper);

      results.add(grouperProvisioningGroup);
    }
    
    return results;
  }
  
  private List<ProvisioningEntity> getProvisioningEntityMapFromQueryResults(List<String[]> queryResults) {
    
    Map<String, ProvisioningEntityWrapper> memberUuidToProvisioningEntityWrapper = this.getGrouperProvisioner().getGrouperProvisioningData().getMemberUuidToProvisioningEntityWrapper();

    List<ProvisioningEntity> results = new ArrayList<ProvisioningEntity>();

    for (String[] queryResult : queryResults) {
      String id = queryResult[0];
      String subjectId = queryResult[1];
      String subjectIdentifier0 = queryResult[2];
      String name = queryResult[3];
      String description = queryResult[4];
      
      ProvisioningEntity grouperProvisioningEntity = new ProvisioningEntity();
      grouperProvisioningEntity.setId(id);
      grouperProvisioningEntity.setName(name);

      grouperProvisioningEntity.assignAttribute("description", description);
      grouperProvisioningEntity.assignAttribute("subjectId", subjectId);
      grouperProvisioningEntity.assignAttribute("subjectIdentifier0", subjectIdentifier0);
      
      
      ProvisioningEntityWrapper provisioningEntityWrapper = new ProvisioningEntityWrapper();
      grouperProvisioningEntity.setProvisioningEntityWrapper(provisioningEntityWrapper);
      provisioningEntityWrapper.setGrouperProvisioningEntity(grouperProvisioningEntity);
      
      memberUuidToProvisioningEntityWrapper.put(grouperProvisioningEntity.getId(), provisioningEntityWrapper);

      results.add(grouperProvisioningEntity);
    }
    
    return results;
  }
  
  private List<ProvisioningMembership> getProvisioningMembershipMapFromQueryResults(List<String[]> queryResults) {
    
    Map<MultiKey, ProvisioningMembershipWrapper> groupIdMemberIdToProvisioningMembershipWrapper = this.getGrouperProvisioner().getGrouperProvisioningData().getGroupIdMemberIdToProvisioningMembershipWrapper();

    List<ProvisioningMembership> results = new ArrayList<ProvisioningMembership>();

    for (String[] queryResult : queryResults) {
      String membershipId = queryResult[0];
      String groupId = queryResult[1];
      String memberId = queryResult[2];
      String subjectId = queryResult[3];
      String subjectIdentifier0 = queryResult[4];
      String name = queryResult[5];
      String description = queryResult[6];
      String groupName = queryResult[7];
      String groupDisplayName = queryResult[8];
      String groupDescription = queryResult[9];
      Long groupIdIndex = GrouperUtil.longObjectValue(queryResult[10], false);
      
      ProvisioningMembership grouperProvisioningMembership = new ProvisioningMembership();
      grouperProvisioningMembership.setId(membershipId);
      
      {
        ProvisioningEntity targetEntity = new ProvisioningEntity();
        targetEntity.setId(memberId);
        targetEntity.setName(name);
        targetEntity.assignAttribute("description", description);
        targetEntity.assignAttribute("subjectId", subjectId);
        targetEntity.assignAttribute("subjectIdentifier0", subjectIdentifier0);

        grouperProvisioningMembership.setProvisioningEntity(targetEntity);
      }
      {
        ProvisioningGroup targetGroup = new ProvisioningGroup();
        targetGroup.setId(groupId);
        targetGroup.setName(groupName);
        targetGroup.setDisplayName(groupDisplayName);
        targetGroup.assignAttribute("description", description);

        targetGroup.setIdIndex(groupIdIndex);
        grouperProvisioningMembership.setProvisioningGroup(targetGroup);
      }
      
      ProvisioningMembershipWrapper provisioningMembershipWrapper = new ProvisioningMembershipWrapper();
      grouperProvisioningMembership.setProvisioningMembershipWrapper(provisioningMembershipWrapper);
      provisioningMembershipWrapper.setGrouperProvisioningMembership(grouperProvisioningMembership);
      
      groupIdMemberIdToProvisioningMembershipWrapper.put(new MultiKey(grouperProvisioningMembership.getProvisioningGroupId(), grouperProvisioningMembership.getProvisioningEntityId()), provisioningMembershipWrapper);

      results.add(grouperProvisioningMembership);
    }
    
    return results;
  }

  public void retrieveAllData() {
    Map<String, Object> debugMap = this.getGrouperProvisioner().getDebugMap();

    GrouperProvisioningLists grouperProvisioningObjects = 
        this.getGrouperProvisioner().getGrouperProvisioningData().getGrouperProvisioningObjects();
    
    {
      long start = System.currentTimeMillis();
      grouperProvisioningObjects.setProvisioningGroups(grouperProvisioner.retrieveGrouperDao().retrieveAllGroups());
      debugMap.put("retrieveGrouperGroupsMillis", System.currentTimeMillis() - start);
      debugMap.put("grouperGroupCount", GrouperUtil.length(this.getGrouperProvisioner().getGrouperProvisioningData().getGrouperProvisioningObjects().getProvisioningGroups()));
    }
    {
      long start = System.currentTimeMillis();
      grouperProvisioningObjects.setProvisioningEntities(grouperProvisioner.retrieveGrouperDao().retrieveAllMembers());
      debugMap.put("retrieveGrouperEntitiesMillis", System.currentTimeMillis() - start);
      debugMap.put("grouperEntityCount", GrouperUtil.length(this.getGrouperProvisioner().getGrouperProvisioningData().getGrouperProvisioningObjects().getProvisioningEntities()));
    }
    {
      long start = System.currentTimeMillis();
      grouperProvisioningObjects.setProvisioningMemberships(grouperProvisioner.retrieveGrouperDao().retrieveAllMemberships());
      debugMap.put("retrieveGrouperMshipsMillis", System.currentTimeMillis() - start);
      debugMap.put("grouperMshipCount", GrouperUtil.length(this.getGrouperProvisioner().getGrouperProvisioningData().getGrouperProvisioningObjects().getProvisioningMemberships()));
    }
    
  }
}
