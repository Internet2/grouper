package edu.internet2.middleware.grouper.app.provisioning;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.type.StringType;
import org.hibernate.type.Type;

import edu.internet2.middleware.grouper.FieldFinder;
import edu.internet2.middleware.grouper.app.tableSync.ProvisioningSyncIntegration;
import edu.internet2.middleware.grouper.app.tableSync.ProvisioningSyncResult;
import edu.internet2.middleware.grouper.hibernate.HibUtils;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSync;
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

  /**
   * get sync objects from the database
   */
  public Map<String, GcGrouperSyncGroup> retrieveAllSyncGroups() {
    GcGrouperSync gcGrouperSync = this.getGrouperProvisioner().getGcGrouperSync();
    
    List<GcGrouperSyncGroup> gcGrouperSyncGroups = gcGrouperSync.getGcGrouperSyncGroupDao().groupRetrieveAll();
    
    Map<String, GcGrouperSyncGroup> groupUuidToSyncGroup = new HashMap<String, GcGrouperSyncGroup>();

    // save these in the data object
    for (GcGrouperSyncGroup gcGrouperSyncGroup : gcGrouperSyncGroups) {

      groupUuidToSyncGroup.put(gcGrouperSyncGroup.getGroupId(), gcGrouperSyncGroup);
    }
    return groupUuidToSyncGroup;
  }

  /**
   * take the sync groups and see which ones do not correspond to a grouper group
   */
  public void calculateProvisioningGroupsToDelete() {

    Map<String, GcGrouperSyncGroup> groupUuidToSyncGroup = this.getGrouperProvisioner().getGrouperProvisioningData().getGroupUuidToSyncGroup();

    List<ProvisioningGroup> grouperProvisioningGroupsToDelete = this.getGrouperProvisioner().getGrouperProvisioningData().getGrouperProvisioningObjectsToDelete().getProvisioningGroups();
    
    Map<String, ProvisioningGroupWrapper> groupUuidToProvisioningGroupWrapper = this.getGrouperProvisioner().getGrouperProvisioningData().getGroupUuidToProvisioningGroupWrapper();

    int provisioningGroupsToDeleteCount = 0;

    // loop through sync groups
    for (GcGrouperSyncGroup gcGrouperSyncGroup : groupUuidToSyncGroup.values()) {

      ProvisioningGroupWrapper provisioningGroupWrapper = groupUuidToProvisioningGroupWrapper.get(gcGrouperSyncGroup.getGroupId());
      
      // if a group has been deleted in grouper_groups table but copy still exists in grouper_sync_group
      // we are sending the copy over to the target so that target can also delete
      if (provisioningGroupWrapper == null) {
        
        provisioningGroupsToDeleteCount++;
        
        // create a provisioning group to delete
        ProvisioningGroup provisioningGroup = new ProvisioningGroup();
        provisioningGroup.setId(gcGrouperSyncGroup.getGroupId());
        provisioningGroup.setName(gcGrouperSyncGroup.getGroupName());
        provisioningGroup.setIdIndex(gcGrouperSyncGroup.getGroupIdIndex());
        grouperProvisioningGroupsToDelete.add(provisioningGroup);
        
        provisioningGroupWrapper = new ProvisioningGroupWrapper();
        provisioningGroupWrapper.setGrouperProvisioner(this.grouperProvisioner);

        provisioningGroup.setProvisioningGroupWrapper(provisioningGroupWrapper);
        provisioningGroupWrapper.setGrouperProvisioningGroupToDelete(provisioningGroup);
        
        groupUuidToProvisioningGroupWrapper.put(gcGrouperSyncGroup.getGroupId(), provisioningGroupWrapper);
        
      }
      provisioningGroupWrapper.setGcGrouperSyncGroup(gcGrouperSyncGroup);
      
    }
    if (provisioningGroupsToDeleteCount > 0) {
      this.getGrouperProvisioner().getDebugMap().put("provisioningGroupsToDeleteCount", provisioningGroupsToDeleteCount);
    }

  }
  
  public List<ProvisioningGroup> retrieveAllGroups() {
    
    if (this.grouperProvisioner == null) {
      throw new RuntimeException("grouperProvisioner is not set");
    }
    
    List<ProvisioningGroup> results = new ArrayList<ProvisioningGroup>();
    
    String sql = "select gg.id, gg.name, gg.display_name, gg.description, gg.id_index " + 
        "        from grouper_groups gg, grouper_aval_asn_asn_group_v gaaagv_target, " + 
        "        grouper_aval_asn_asn_group_v gaaagv_do_provision" + 
        "         where gg.id = gaaagv_do_provision.group_id and " + 
        "          gg.id = gaaagv_target.group_id and " + 
        "         gaaagv_do_provision.enabled2 = 'T' and gaaagv_target.enabled2 = 'T'" + 
        "         and gaaagv_target.attribute_def_name_name1 = ? " + 
        "         and gaaagv_do_provision.attribute_def_name_name1 = ?" + 
        "         and gaaagv_target.attribute_def_name_name2 = ?" + 
        "         and gaaagv_do_provision.attribute_def_name_name2 = ?" + 
        "         and gaaagv_target.value_string = ?" + 
        "         and gaaagv_do_provision.value_string = 'true'" + 
        "         ";
    

    List<String[]> queryResults = HibernateSession.bySqlStatic().listSelect(String[].class, sql.toString(),
        GrouperUtil.toListObject(
            GrouperProvisioningAttributeNames.retrieveAttributeDefNameBase().getName(),
            GrouperProvisioningAttributeNames.retrieveAttributeDefNameBase().getName(),
            GrouperProvisioningAttributeNames.retrieveAttributeDefNameTarget().getName(),
            GrouperProvisioningAttributeNames.retrieveAttributeDefNameDoProvision().getName(),
            this.grouperProvisioner.getConfigId()), HibUtils.listType(StringType.INSTANCE, StringType.INSTANCE, 
                StringType.INSTANCE, 
                StringType.INSTANCE, StringType.INSTANCE));

    List<ProvisioningGroup> provisioningGroupsFromGrouper = getTargetGroupMapFromQueryResults(queryResults);
    results.addAll(GrouperUtil.nonNull(provisioningGroupsFromGrouper));
    
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
        "from grouper_members gm, grouper_memberships_all_v gmav," +  
     " grouper_aval_asn_asn_group_v gaaagv_target, " + 
    "        grouper_aval_asn_asn_group_v gaaagv_do_provision" + 
    "         where gmav.member_id = gm.id and gmav.owner_group_id = gaaagv_do_provision.group_id and " + 
    "          gmav.owner_group_id = gaaagv_target.group_id and " + 
    "         gaaagv_do_provision.enabled2 = 'T' and gaaagv_target.enabled2 = 'T'" + 
    "         and gaaagv_target.attribute_def_name_name1 = ? " + 
    "         and gaaagv_do_provision.attribute_def_name_name1 = ?" + 
    "         and gaaagv_target.attribute_def_name_name2 = ?" + 
    "         and gaaagv_do_provision.attribute_def_name_name2 = ?" + 
    "         and gaaagv_target.value_string = ?" + 
    "         and gaaagv_do_provision.value_string = 'true'");
    
    
    
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
    
    params.add(GrouperProvisioningAttributeNames.retrieveAttributeDefNameBase().getName());
    params.add(GrouperProvisioningAttributeNames.retrieveAttributeDefNameBase().getName());
    params.add(GrouperProvisioningAttributeNames.retrieveAttributeDefNameTarget().getName());
    params.add(GrouperProvisioningAttributeNames.retrieveAttributeDefNameDoProvision().getName());
    
    params.add(this.grouperProvisioner.getConfigId());
    params.addAll(subjectSources);
    params.addAll(fieldIds);
    
    List<Type> types = new ArrayList<Type>();
    types.add(StringType.INSTANCE);
    
    types.add(StringType.INSTANCE);
    types.add(StringType.INSTANCE);
    types.add(StringType.INSTANCE);
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

    List<ProvisioningEntity> grouperProvisioningEntities = getProvisioningEntityMapFromQueryResults(queryResults);
    results.addAll(grouperProvisioningEntities);
    
    return results;
  }
  
  public List<ProvisioningMembership> retrieveAllMemberships() {
    
    if (this.grouperProvisioner == null) {
      throw new RuntimeException("grouperProvisioner is not set");
    }
    
    List<ProvisioningMembership> results = new ArrayList<ProvisioningMembership>();
    

    StringBuilder sql = new StringBuilder("select gmav.membership_id, gg.id, gm.id, gm.subject_id, gm.subject_source, gm.subject_identifier0, "
        + "gm.name, gm.description, gg.name, gg.display_name, gg.description, gg.id_index " +
        "from grouper_groups gg, grouper_memberships_all_v gmav, grouper_members gm, grouper_aval_asn_asn_group_v gaaagv_target, grouper_aval_asn_asn_group_v gaaagv_do_provision " + 
        "where " +
        " gmav.owner_group_id = gg.id " +
        "and gmav.member_id = gm.id " +
    
    "         and gg.id = gaaagv_do_provision.group_id and " + 
    "          gg.id = gaaagv_target.group_id and " + 
    "         gaaagv_do_provision.enabled2 = 'T' and gaaagv_target.enabled2 = 'T'" + 
    "         and gaaagv_target.attribute_def_name_name1 = ? " + 
    "         and gaaagv_do_provision.attribute_def_name_name1 = ?" + 
    "         and gaaagv_target.attribute_def_name_name2 = ?" + 
    "         and gaaagv_do_provision.attribute_def_name_name2 = ?" + 
    "         and gaaagv_target.value_string = ?" + 
    "         and gaaagv_do_provision.value_string = 'true' ");
    
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
    
    params.add(GrouperProvisioningAttributeNames.retrieveAttributeDefNameBase().getName());
    params.add(GrouperProvisioningAttributeNames.retrieveAttributeDefNameBase().getName());
    params.add(GrouperProvisioningAttributeNames.retrieveAttributeDefNameTarget().getName());
    params.add(GrouperProvisioningAttributeNames.retrieveAttributeDefNameDoProvision().getName());
    
    params.add(this.grouperProvisioner.getConfigId());
    params.addAll(subjectSources);
    params.addAll(fieldIds);
    
    List<Type> types = new ArrayList<Type>();
    types.add(StringType.INSTANCE);
    types.add(StringType.INSTANCE);
    types.add(StringType.INSTANCE);
    types.add(StringType.INSTANCE);
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
  
  public void processWrappers() {
    
    GrouperProvisioningLists grouperProvisioningObjects = 
        this.getGrouperProvisioner().getGrouperProvisioningData().getGrouperProvisioningObjects();

    Map<String, ProvisioningGroupWrapper> groupUuidToProvisioningGroupWrapper
      = this.getGrouperProvisioner().getGrouperProvisioningData().getGroupUuidToProvisioningGroupWrapper();
    
    // add wrappers for all groups
    for (ProvisioningGroup provisioningGroup : GrouperUtil.nonNull(grouperProvisioningObjects.getProvisioningGroups())) {
      ProvisioningGroupWrapper provisioningGroupWrapper = new ProvisioningGroupWrapper();
      provisioningGroupWrapper.setGrouperProvisioner(this.grouperProvisioner);

      provisioningGroupWrapper.setGrouperProvisioningGroup(provisioningGroup);
      provisioningGroup.setProvisioningGroupWrapper(provisioningGroupWrapper);
      groupUuidToProvisioningGroupWrapper.put(provisioningGroup.getId(), provisioningGroupWrapper);
    }
    
    Map<String, ProvisioningEntityWrapper> memberUuidToProvisioningEntityWrapper
      = this.getGrouperProvisioner().getGrouperProvisioningData().getMemberUuidToProvisioningEntityWrapper();
    
    // add wrappers for all entities
    for (ProvisioningEntity provisioningEntity : GrouperUtil.nonNull(grouperProvisioningObjects.getProvisioningEntities())) {
      ProvisioningEntityWrapper provisioningEntityWrapper = new ProvisioningEntityWrapper();
      provisioningEntityWrapper.setGrouperProvisioner(this.grouperProvisioner);
      provisioningEntityWrapper.setGrouperProvisioningEntity(provisioningEntity);
      provisioningEntity.setProvisioningEntityWrapper(provisioningEntityWrapper);
      memberUuidToProvisioningEntityWrapper.put(provisioningEntity.getId(), provisioningEntityWrapper);
    }

    Map<MultiKey, ProvisioningMembershipWrapper> groupIdMemberIdToProvisioningMembershipWrapper
      = this.getGrouperProvisioner().getGrouperProvisioningData().getGroupIdMemberIdToProvisioningMembershipWrapper();

    // add wrappers for memberships
    for (ProvisioningMembership provisioningMembership : GrouperUtil.nonNull(grouperProvisioningObjects.getProvisioningMemberships())) {
      ProvisioningMembershipWrapper provisioningMembershipWrapper = new ProvisioningMembershipWrapper();
      provisioningMembershipWrapper.setGrouperProvisioner(this.grouperProvisioner);
      provisioningMembershipWrapper.setGrouperProvisioningMembership(provisioningMembership);
      provisioningMembership.setProvisioningMembershipWrapper(provisioningMembershipWrapper);
      groupIdMemberIdToProvisioningMembershipWrapper.put(new MultiKey(provisioningMembership.getProvisioningGroupId(), provisioningMembership.getProvisioningEntityId()), 
          provisioningMembershipWrapper);
    }
  }
  
  public void fixGrouperProvisioningMembershipReferences() {
    
    GrouperProvisioningLists grouperProvisioningObjects = 
        this.getGrouperProvisioner().getGrouperProvisioningData().getGrouperProvisioningObjects();
    
    Map<String, Object> debugMap = this.getGrouperProvisioner().getDebugMap();
    
    int missingGrouperProvisioningMembershipReferencesCount = 0;

    {
      Map<String, ProvisioningGroupWrapper> groupUuidToProvisioningGroupWrapper
      = this.getGrouperProvisioner().getGrouperProvisioningData().getGroupUuidToProvisioningGroupWrapper();
    
      // add wrappers for memberships
      for (ProvisioningMembership provisioningMembership : GrouperUtil.nonNull(grouperProvisioningObjects.getProvisioningMemberships())) {
  
        // pull up the existing group
        ProvisioningGroupWrapper provisioningGroupWrapper = groupUuidToProvisioningGroupWrapper.get(provisioningMembership.getProvisioningGroupId());
        
        // if its not there (e.g. membership added after group query and before membership query?
        if (provisioningGroupWrapper == null) {
          missingGrouperProvisioningMembershipReferencesCount++;
          provisioningGroupWrapper = new ProvisioningGroupWrapper();
          provisioningGroupWrapper.setGrouperProvisioner(this.grouperProvisioner);

          
          // all the data is in the membership query
          provisioningGroupWrapper.setGrouperProvisioningGroup(provisioningMembership.getProvisioningGroup());
          provisioningMembership.getProvisioningGroup().setProvisioningGroupWrapper(provisioningGroupWrapper);
          if (grouperProvisioningObjects.getProvisioningGroups() == null) {
            grouperProvisioningObjects.setProvisioningGroups(new ArrayList<ProvisioningGroup>());
          }
          grouperProvisioningObjects.getProvisioningGroups().add(provisioningMembership.getProvisioningGroup());
          groupUuidToProvisioningGroupWrapper.put(provisioningMembership.getProvisioningGroupId(), provisioningGroupWrapper);
        } else {
          provisioningMembership.setProvisioningGroup(provisioningGroupWrapper.getGrouperProvisioningGroup());
        }
        
      }
    }

    {
      Map<String, ProvisioningEntityWrapper> memberUuidToProvisioningEntityWrapper
      = this.getGrouperProvisioner().getGrouperProvisioningData().getMemberUuidToProvisioningEntityWrapper();
    
      // add wrappers for memberships
      for (ProvisioningMembership provisioningMembership : GrouperUtil.nonNull(grouperProvisioningObjects.getProvisioningMemberships())) {
  
        // pull up the existing group
        ProvisioningEntityWrapper provisioningEntityWrapper = memberUuidToProvisioningEntityWrapper.get(provisioningMembership.getProvisioningEntityId());
        
        // if its not there (e.g. membership added after group query and before membership query?
        if (provisioningEntityWrapper == null) {
          missingGrouperProvisioningMembershipReferencesCount++;
          provisioningEntityWrapper = new ProvisioningEntityWrapper();
          provisioningEntityWrapper.setGrouperProvisioner(this.grouperProvisioner);
          
          // all the data is in the membership query
          provisioningEntityWrapper.setGrouperProvisioningEntity(provisioningMembership.getProvisioningEntity());
          provisioningMembership.getProvisioningEntity().setProvisioningEntityWrapper(provisioningEntityWrapper);
          if (grouperProvisioningObjects.getProvisioningEntities() == null) {
            grouperProvisioningObjects.setProvisioningEntities(new ArrayList<ProvisioningEntity>());
          }
          grouperProvisioningObjects.getProvisioningEntities().add(provisioningMembership.getProvisioningEntity());
          memberUuidToProvisioningEntityWrapper.put(provisioningMembership.getProvisioningEntityId(), provisioningEntityWrapper);
        } else {
          provisioningMembership.setProvisioningEntity(provisioningEntityWrapper.getGrouperProvisioningEntity());
        }
        
      }
    }
    if (missingGrouperProvisioningMembershipReferencesCount > 0) {
      debugMap.put("missingGrouperProvisioningMembershipReferencesCount", missingGrouperProvisioningMembershipReferencesCount);
    }
  }
  
  private List<ProvisioningGroup> getTargetGroupMapFromQueryResults(List<String[]> queryResults) {
    
    List<ProvisioningGroup> results = new ArrayList<ProvisioningGroup>();

    for (String[] queryResult : queryResults) {
      String id = queryResult[0];
      String name = queryResult[1];
      String displayName = queryResult[2];
      String description = queryResult[3];
      String idIndex = queryResult[4];
      
      ProvisioningGroup grouperProvisioningGroup = new ProvisioningGroup();
      grouperProvisioningGroup.setId(id);
      grouperProvisioningGroup.setName(name);
      grouperProvisioningGroup.setDisplayName(displayName);
      grouperProvisioningGroup.setIdIndex(Long.parseLong(idIndex));
      grouperProvisioningGroup.assignAttribute("description", description);
      
      results.add(grouperProvisioningGroup);
    }
    
    return results;
  }
  
  private List<ProvisioningEntity> getProvisioningEntityMapFromQueryResults(List<String[]> queryResults) {
    
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
      
      results.add(grouperProvisioningEntity);
    }
    
    return results;
  }
  
  private List<ProvisioningMembership> getProvisioningMembershipMapFromQueryResults(List<String[]> queryResults) {
    
    List<ProvisioningMembership> results = new ArrayList<ProvisioningMembership>();

    for (String[] queryResult : queryResults) {
      String membershipId = queryResult[0];
      String groupId = queryResult[1];
      String memberId = queryResult[2];
      String subjectId = queryResult[3];
      String subjectSourceId = queryResult[4];
      String subjectIdentifier0 = queryResult[5];
      String name = queryResult[6];
      String description = queryResult[7];
      String groupName = queryResult[8];
      String groupDisplayName = queryResult[9];
      String groupDescription = queryResult[10];
      Long groupIdIndex = GrouperUtil.longObjectValue(queryResult[11], false);
      
      ProvisioningMembership grouperProvisioningMembership = new ProvisioningMembership();
      grouperProvisioningMembership.setId(membershipId);
      
      {
        ProvisioningEntity targetEntity = new ProvisioningEntity();
        targetEntity.setId(memberId);
        targetEntity.setName(name);
        targetEntity.assignAttribute("description", description);
        targetEntity.assignAttribute("subjectId", subjectId);
        targetEntity.assignAttribute("subjectSourceId", subjectSourceId);
        targetEntity.assignAttribute("subjectIdentifier0", subjectIdentifier0);
        
        
        grouperProvisioningMembership.setProvisioningEntity(targetEntity);
        grouperProvisioningMembership.setProvisioningEntityId(memberId);
      }
      {
        ProvisioningGroup targetGroup = new ProvisioningGroup();
        targetGroup.setId(groupId);
        targetGroup.setName(groupName);
        targetGroup.setDisplayName(groupDisplayName);
        targetGroup.assignAttribute("description", groupDescription);

        targetGroup.setIdIndex(groupIdIndex);
        grouperProvisioningMembership.setProvisioningGroup(targetGroup);
        grouperProvisioningMembership.setProvisioningGroupId(groupId);
      }
      
      results.add(grouperProvisioningMembership);
    }
    
    return results;
  }

  public void retrieveAllGrouperData() {
    Map<String, Object> debugMap = this.getGrouperProvisioner().getDebugMap();

    GrouperProvisioningLists grouperProvisioningObjects = 
        this.getGrouperProvisioner().getGrouperProvisioningData().getGrouperProvisioningObjects();
    
    {
      long start = System.currentTimeMillis();
      grouperProvisioningObjects.setProvisioningGroups(this.retrieveAllGroups());
      debugMap.put("retrieveGrouperGroupsMillis", System.currentTimeMillis() - start);
      debugMap.put("grouperGroupCount", GrouperUtil.length(grouperProvisioningObjects.getProvisioningGroups()));
    }
    {
      long start = System.currentTimeMillis();
      grouperProvisioningObjects.setProvisioningEntities(this.retrieveAllMembers());
      debugMap.put("retrieveGrouperEntitiesMillis", System.currentTimeMillis() - start);
      debugMap.put("grouperEntityCount", GrouperUtil.length(grouperProvisioningObjects.getProvisioningEntities()));
    }
    {
      long start = System.currentTimeMillis();
      grouperProvisioningObjects.setProvisioningMemberships(this.retrieveAllMemberships());
      debugMap.put("retrieveGrouperMshipsMillis", System.currentTimeMillis() - start);
      debugMap.put("grouperMshipCount", GrouperUtil.length(grouperProvisioningObjects.getProvisioningMemberships()));
    }
    
  }

  public void retrieveAllSyncData() {
    Map<String, Object> debugMap = this.getGrouperProvisioner().getDebugMap();

    {
      long start = System.currentTimeMillis();
      Map<String, GcGrouperSyncGroup> retrieveAllSyncGroups = this.retrieveAllSyncGroups();
      this.getGrouperProvisioner().getGrouperProvisioningData().setGroupUuidToSyncGroup(retrieveAllSyncGroups);
      this.getGrouperProvisioner().getGrouperProvisioningData().setGroupUuidToSyncGroupIncludeRemoved(
          new HashMap<String, GcGrouperSyncGroup>(retrieveAllSyncGroups));

      debugMap.put("retrieveSyncGroupsMillis", System.currentTimeMillis() - start);
      debugMap.put("syncGroupCount", GrouperUtil.length(this.getGrouperProvisioner().getGrouperProvisioningData()
          .getGroupUuidToSyncGroup()));
    }
    {
      long start = System.currentTimeMillis();
      Map<String, GcGrouperSyncMember> retrieveAllSyncMembers = this.retrieveAllSyncMembers();
      this.getGrouperProvisioner().getGrouperProvisioningData().setMemberUuidToSyncMember(retrieveAllSyncMembers);
      this.getGrouperProvisioner().getGrouperProvisioningData().setMemberUuidToSyncMemberIncludeRemoved(
          new HashMap<String, GcGrouperSyncMember>(retrieveAllSyncMembers));
      debugMap.put("retrieveSyncEntitiesMillis", System.currentTimeMillis() - start);
      debugMap.put("syncEntityCount", GrouperUtil.length(this.getGrouperProvisioner().getGrouperProvisioningData().getMemberUuidToSyncMember()));
    }
    {
      long start = System.currentTimeMillis();
      Map<MultiKey, GcGrouperSyncMembership> retrieveAllSyncMemberships = this.retrieveAllSyncMemberships();
      this.getGrouperProvisioner().getGrouperProvisioningData().setGroupIdMemberIdToSyncMembership(
          retrieveAllSyncMemberships);
      this.getGrouperProvisioner().getGrouperProvisioningData().setGroupIdMemberIdtoSyncMembershipIncludeRemoved(
          new HashMap<MultiKey, GcGrouperSyncMembership>(retrieveAllSyncMemberships));
      debugMap.put("retrieveSyncMshipsMillis", System.currentTimeMillis() - start);
      debugMap.put("syncMshipCount", GrouperUtil.length(this.getGrouperProvisioner().getGrouperProvisioningData().getGroupIdMemberIdToSyncMembership()));
    }
    
  }

  /**
   * get sync objects from the database
   */
  public Map<String, GcGrouperSyncMember> retrieveAllSyncMembers() {
    GcGrouperSync gcGrouperSync = this.getGrouperProvisioner().getGcGrouperSync();
    
    List<GcGrouperSyncMember> gcGrouperSyncMembers = gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveAll();
    
    Map<String, GcGrouperSyncMember> memberUuidToSyncMember = new HashMap<String, GcGrouperSyncMember>();
  
    // save these in the data object
    for (GcGrouperSyncMember gcGrouperSyncMember : gcGrouperSyncMembers) {
  
      memberUuidToSyncMember.put(gcGrouperSyncMember.getMemberId(), gcGrouperSyncMember);
    }
    return memberUuidToSyncMember;
  }

  /**
   * get sync objects from the database.  all records correspond to a sync group and sync member or its skipped
   */
  public Map<MultiKey, GcGrouperSyncMembership> retrieveAllSyncMemberships() {
    GcGrouperSync gcGrouperSync = this.getGrouperProvisioner().getGcGrouperSync();
    
    List<GcGrouperSyncMembership> gcGrouperSyncMemberships = gcGrouperSync.getGcGrouperSyncMembershipDao().membershipRetrieveAll();
    
    Map<MultiKey, GcGrouperSyncMembership> groupIdMemberIdToSyncMembership = new HashMap<MultiKey, GcGrouperSyncMembership>();

    
    int syncMembershipReferenceMissing = 0;
    

    for (GcGrouperSyncMembership gcGrouperSyncMembership : GrouperUtil.nonNull(gcGrouperSyncMemberships)) {
      
      // data is not consistent just ignore for now
      GcGrouperSyncGroup gcGrouperSyncGroup = gcGrouperSync.getGcGrouperSyncGroupDao().groupRetrieveById(gcGrouperSyncMembership.getGrouperSyncGroupId());
      if (gcGrouperSyncGroup == null) {
        syncMembershipReferenceMissing++;
        continue;
      }
      
      GcGrouperSyncMember gcGrouperSyncMember = gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveById(gcGrouperSyncMembership.getGrouperSyncMemberId());
      
      if (gcGrouperSyncMember == null) {
        syncMembershipReferenceMissing++;
        continue;
      }
  
      groupIdMemberIdToSyncMembership.put(new MultiKey(gcGrouperSyncGroup.getGroupId(),
          gcGrouperSyncMember.getMemberId()), gcGrouperSyncMembership);
    }
    if (syncMembershipReferenceMissing > 0) {
      this.getGrouperProvisioner().getDebugMap().put("syncMembershipReferenceMissing", syncMembershipReferenceMissing);
    }

    return groupIdMemberIdToSyncMembership;
  }

  public void calculateProvisioningDataToDelete() {
    this.calculateProvisioningGroupsToDelete();
    this.calculateProvisioningEntitiesToDelete();
    this.calculateProvisioningMembershipsToDelete();
    
  }

  /**
   * take the sync members and see which ones do not correspond to a grouper member
   */
  public void calculateProvisioningEntitiesToDelete() {
  
    Map<String, GcGrouperSyncMember> memberUuidToSyncMember = this.getGrouperProvisioner().getGrouperProvisioningData().getMemberUuidToSyncMember();
  
    List<ProvisioningEntity> grouperProvisioningEntitiesToDelete = this.getGrouperProvisioner().getGrouperProvisioningData().getGrouperProvisioningObjectsToDelete().getProvisioningEntities();
    
    Map<String, ProvisioningEntityWrapper> memberUuidToProvisioningMemberWrapper = this.getGrouperProvisioner().getGrouperProvisioningData().getMemberUuidToProvisioningEntityWrapper();
  
    int provisioningEntitiesToDelete = 0;

    // loop through sync groups
    for (GcGrouperSyncMember gcGrouperSyncMember : memberUuidToSyncMember.values()) {
  
      ProvisioningEntityWrapper provisioningEntityWrapper = memberUuidToProvisioningMemberWrapper.get(gcGrouperSyncMember.getMemberId());
      
      // if a entity has been deleted in grouper_members table but copy still exists in grouper_sync_member
      // we are sending the copy over to the target so that target can also delete
      if (provisioningEntityWrapper == null) {
        
        provisioningEntitiesToDelete++;
        
        ProvisioningEntity provisioningEntity = new ProvisioningEntity();
        provisioningEntity.setId(gcGrouperSyncMember.getMemberId());

        provisioningEntity.assignAttribute("subjectId", gcGrouperSyncMember.getSubjectId());
        provisioningEntity.assignAttribute("subjectIdentifier0", gcGrouperSyncMember.getSubjectIdentifier());
        grouperProvisioningEntitiesToDelete.add(provisioningEntity);

        provisioningEntityWrapper = new ProvisioningEntityWrapper();
        provisioningEntityWrapper.setGrouperProvisioner(this.grouperProvisioner);
        provisioningEntity.setProvisioningEntityWrapper(provisioningEntityWrapper);
        provisioningEntityWrapper.setGrouperProvisioningEntityToDelete(provisioningEntity);
        provisioningEntityWrapper.setGcGrouperSyncMember(gcGrouperSyncMember);
        
        memberUuidToProvisioningMemberWrapper.put(provisioningEntity.getId(), provisioningEntityWrapper);
      }
        
      provisioningEntityWrapper.setGcGrouperSyncMember(gcGrouperSyncMember);
      
    }
    
    if (provisioningEntitiesToDelete > 0) {
      this.getGrouperProvisioner().getDebugMap().put("provisioningEntitiesToDelete", provisioningEntitiesToDelete);
    }

  }

  /**
   * take the sync groups and see which ones do not correspond to a grouper group
   */
  public void calculateProvisioningMembershipsToDelete() {
  
    Map<MultiKey, GcGrouperSyncMembership> groupIdMemberIdToSyncMembership = this.getGrouperProvisioner().getGrouperProvisioningData().getGroupIdMemberIdToSyncMembership();
  
    List<ProvisioningMembership> grouperProvisioningMembershipsToDelete = this.getGrouperProvisioner().getGrouperProvisioningData()
        .getGrouperProvisioningObjectsToDelete().getProvisioningMemberships();
    
    Map<MultiKey, ProvisioningMembershipWrapper> groupIdMemberIdToProvisioningMembershipWrapper = this.getGrouperProvisioner()
        .getGrouperProvisioningData().getGroupIdMemberIdToProvisioningMembershipWrapper();

    Map<String, ProvisioningGroupWrapper> groupUuidToProvisioningGroupWrapper = this.getGrouperProvisioner().getGrouperProvisioningData().getGroupUuidToProvisioningGroupWrapper();
    Map<String, ProvisioningEntityWrapper> memberUuidToProvisioningEntityWrapper = this.getGrouperProvisioner().getGrouperProvisioningData().getMemberUuidToProvisioningEntityWrapper();

    int provisioningMshipsToDelete = 0;
    
    // loop through sync groups
    for (MultiKey groupIdMemberId : GrouperUtil.nonNull(groupIdMemberIdToSyncMembership).keySet()) {

      String groupId = (String)groupIdMemberId.getKey(0);
      String memberId = (String)groupIdMemberId.getKey(1);

      GcGrouperSyncMembership gcGrouperSyncMembership = groupIdMemberIdToSyncMembership.get(groupIdMemberId);
      
      ProvisioningMembershipWrapper provisioningMembershipWrapper = groupIdMemberIdToProvisioningMembershipWrapper.get(groupIdMemberId);
      
      // if a group has been deleted in grouper_groups table but copy still exists in grouper_sync_group
      // we are sending the copy over to the target so that target can also delete
      if (provisioningMembershipWrapper == null) {
        
        provisioningMshipsToDelete++;
        
        ProvisioningGroupWrapper provisioningGroupWrapper = groupUuidToProvisioningGroupWrapper.get(groupId);
        if (provisioningGroupWrapper == null) {
          throw new RuntimeException("Cant find groupId: '" + groupId + "'");
        }
        ProvisioningEntityWrapper provisioningEntityWrapper = memberUuidToProvisioningEntityWrapper.get(memberId);
        if (provisioningEntityWrapper == null) {
          throw new RuntimeException("Cant find entityId: '" + memberId + "'");
        }
        
        // create a provisioning group to delete
        ProvisioningMembership provisioningMembership = new ProvisioningMembership();
        provisioningMembership.setProvisioningGroupId(groupId);
        provisioningMembership.setProvisioningEntityId(memberId);
        
        // the group is either the provisioning group or provisioning group to delete
        if (provisioningGroupWrapper.getGrouperProvisioningGroup() != null) {
          provisioningMembership.setProvisioningGroup(provisioningGroupWrapper.getGrouperProvisioningGroup());
        } else if (provisioningGroupWrapper.getGrouperProvisioningGroupToDelete() != null) {
            provisioningMembership.setProvisioningGroup(provisioningGroupWrapper.getGrouperProvisioningGroupToDelete());
        } else {
          throw new RuntimeException("Cant find provisioning group: '" + groupId + "'");
        }

        // the group is either the provisioning group or provisioning group to delete
        if (provisioningEntityWrapper.getGrouperProvisioningEntity() != null) {
          provisioningMembership.setProvisioningEntity(provisioningEntityWrapper.getGrouperProvisioningEntity());
        } else if (provisioningEntityWrapper.getGrouperProvisioningEntityToDelete() != null) {
            provisioningMembership.setProvisioningEntity(provisioningEntityWrapper.getGrouperProvisioningEntityToDelete());
        } else {
          throw new RuntimeException("Cant find provisioning entity: '" + memberId + "'");
        }

        grouperProvisioningMembershipsToDelete.add(provisioningMembership);
        
        provisioningMembershipWrapper = new ProvisioningMembershipWrapper();
        provisioningMembershipWrapper.setGrouperProvisioner(this.grouperProvisioner);
        provisioningMembership.setProvisioningMembershipWrapper(provisioningMembershipWrapper);
        provisioningMembershipWrapper.setGrouperProvisioningMembershipToDelete(provisioningMembership);
        
        groupIdMemberIdToProvisioningMembershipWrapper.put(groupIdMemberId, provisioningMembershipWrapper);
        
      }
      provisioningMembershipWrapper.setGcGrouperSyncMembership(gcGrouperSyncMembership);
    }      
    if (provisioningMshipsToDelete > 0) {
      this.getGrouperProvisioner().getDebugMap().put("provisioningMshipsToDelete", provisioningMshipsToDelete);
    }
  }

  public void fixSyncObjects() {
    ProvisioningSyncResult provisioningSyncResult = new ProvisioningSyncResult();
    this.grouperProvisioner.setProvisioningSyncResult(provisioningSyncResult);
    ProvisioningSyncIntegration.fullSyncGroups(provisioningSyncResult, this.getGrouperProvisioner().getGcGrouperSync(),
        this.getGrouperProvisioner().getGrouperProvisioningData().getGroupUuidToProvisioningGroupWrapper(),
        this.getGrouperProvisioner().getGrouperProvisioningData().getGroupUuidToSyncGroup());
    ProvisioningSyncIntegration.fullSyncMembers(provisioningSyncResult, this.getGrouperProvisioner().getGcGrouperSync(),
        this.getGrouperProvisioner().getGrouperProvisioningData().getMemberUuidToProvisioningEntityWrapper(),
        this.getGrouperProvisioner().getGrouperProvisioningData().getMemberUuidToSyncMember());
    ProvisioningSyncIntegration.fullSyncMemberships(provisioningSyncResult, this.getGrouperProvisioner().getGcGrouperSync(),
        this.getGrouperProvisioner().getGrouperProvisioningData().getGroupIdMemberIdToProvisioningMembershipWrapper(),
        this.getGrouperProvisioner().getGrouperProvisioningData().getGroupIdMemberIdToSyncMembership());
    int objectStoreCount = this.getGrouperProvisioner().getGcGrouperSync().getGcGrouperSyncDao().storeAllObjects();
    provisioningSyncResult.setSyncObjectStoreCount(objectStoreCount);
    
    this.grouperProvisioner.getDebugMap().put("fixSyncObjectStoreCount", objectStoreCount);
    
  }

}
