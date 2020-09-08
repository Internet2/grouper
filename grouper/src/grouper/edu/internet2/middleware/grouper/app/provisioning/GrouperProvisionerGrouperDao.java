package edu.internet2.middleware.grouper.app.provisioning;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.hibernate.type.StringType;
import org.hibernate.type.Type;

import edu.internet2.middleware.grouper.FieldFinder;
import edu.internet2.middleware.grouper.hibernate.HibUtils;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;
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
   * take the sync groups and see which ones do not correspond to a grouper group
   */
  public void calculateProvisioningGroupsToDelete() {

    Map<String, GcGrouperSyncGroup> groupUuidToSyncGroup = this.getGrouperProvisioner().getGrouperProvisioningData().getGroupUuidToSyncGroup();

    Map<String, ProvisioningGroupWrapper> groupUuidToProvisioningGroupWrapper = this.getGrouperProvisioner().getGrouperProvisioningData().getGroupUuidToProvisioningGroupWrapper();

    int provisioningGroupsToDeleteCount = 0;

    List<ProvisioningGroup> grouperProvisioningGroups = this.getGrouperProvisioner().getGrouperProvisioningData().getGrouperProvisioningObjects().getProvisioningGroups();

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
        if (grouperProvisioningGroups == null) {
          grouperProvisioningGroups = new ArrayList<ProvisioningGroup>();
          this.getGrouperProvisioner().getGrouperProvisioningData().getGrouperProvisioningObjects().setProvisioningGroups(grouperProvisioningGroups);
        }
        grouperProvisioningGroups.add(provisioningGroup);
        
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
  
  /**
   * get either all groups or a list of groups
   * @param retrieveAll
   * @param ids
   * @return the groups
   */
  public List<ProvisioningGroup> retrieveGroups(boolean retrieveAll, Collection<String> ids) {

    if (this.grouperProvisioner == null) {
      throw new RuntimeException("grouperProvisioner is not set");
    }
    
    if (retrieveAll && ids != null) {
      throw new RuntimeException("Cant retrieve all and pass in ids to retrieve!");
    }

    List<ProvisioningGroup> results = new ArrayList<ProvisioningGroup>();
    
    String sqlInitial = "select gg.id, gg.name, gg.display_name, gg.description, gg.id_index " + 
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
    
    List<Object> paramsInitial = new ArrayList<Object>();
    paramsInitial.add(GrouperProvisioningSettings.provisioningConfigStemName()+":"+GrouperProvisioningAttributeNames.PROVISIONING_ATTRIBUTE_NAME);
    paramsInitial.add(GrouperProvisioningSettings.provisioningConfigStemName()+":"+GrouperProvisioningAttributeNames.PROVISIONING_ATTRIBUTE_NAME);
    paramsInitial.add(GrouperProvisioningSettings.provisioningConfigStemName()+":"+GrouperProvisioningAttributeNames.PROVISIONING_TARGET);
    paramsInitial.add(GrouperProvisioningSettings.provisioningConfigStemName()+":"+GrouperProvisioningAttributeNames.PROVISIONING_DO_PROVISION);
    paramsInitial.add(this.grouperProvisioner.getConfigId());

    List<Type> typesInitial = new ArrayList<Type>();
    typesInitial.add(StringType.INSTANCE);
    typesInitial.add(StringType.INSTANCE);
    typesInitial.add(StringType.INSTANCE);
    typesInitial.add(StringType.INSTANCE);
    typesInitial.add(StringType.INSTANCE);

    List<String[]> queryResults = null;
    if (retrieveAll) {
      queryResults = HibernateSession.bySqlStatic().listSelect(String[].class, sqlInitial.toString(),
          paramsInitial, typesInitial);
      List<ProvisioningGroup> provisioningGroupsFromGrouper = getTargetGroupMapFromQueryResults(queryResults);
      results.addAll(GrouperUtil.nonNull(provisioningGroupsFromGrouper));
    } else {
      if (GrouperUtil.length(ids) == 0) {
        return results;
      }

      List<String> idsList = GrouperUtil.listFromCollection(ids);
      
      int numberOfBatches = GrouperUtil.batchNumberOfBatches(idsList.size(), 900);
      for (int i = 0; i < numberOfBatches; i++) {
        List<String> currentBatchIds = GrouperUtil.batchList(idsList, 900, i);
        
        List<Object> params = new ArrayList<Object>(paramsInitial);
        params.addAll(currentBatchIds);

        List<Type> types = new ArrayList<Type>(typesInitial);

        for (int j = 0; j < GrouperUtil.length(currentBatchIds); j++) {
          types.add(StringType.INSTANCE);
        }
        
        StringBuilder sql = new StringBuilder(sqlInitial);
        sql.append(" and gg.id in (");
        sql.append(HibUtils.convertToInClauseForSqlStatic(currentBatchIds));
        sql.append(") ");
        
        queryResults = HibernateSession.bySqlStatic().listSelect(String[].class, sql.toString(), params, types);
        
        List<ProvisioningGroup> targetGroupMapFromQueryResults = getTargetGroupMapFromQueryResults(queryResults);
        results.addAll(GrouperUtil.nonNull(targetGroupMapFromQueryResults));
      }      
    }
    
    return results;

  }
  
  public List<ProvisioningEntity> retrieveMembers(boolean retrieveAll, Collection<String> ids) {

    if (this.grouperProvisioner == null) {
      throw new RuntimeException("grouperProvisioner is not set");
    }
    
    if (retrieveAll && ids != null) {
      throw new RuntimeException("Cant retrieve all and pass in ids to retrieve!");
    }

    List<ProvisioningEntity> results = new ArrayList<ProvisioningEntity>();

    StringBuilder sqlInitial = new StringBuilder("select gm.id, gm.subject_id, gm.subject_identifier0, gm.name, gm.description " +
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
    
    List<Object> paramsInitial = new ArrayList<Object>();
    
    paramsInitial.add(GrouperProvisioningSettings.provisioningConfigStemName()+":"+GrouperProvisioningAttributeNames.PROVISIONING_ATTRIBUTE_NAME);
    paramsInitial.add(GrouperProvisioningSettings.provisioningConfigStemName()+":"+GrouperProvisioningAttributeNames.PROVISIONING_ATTRIBUTE_NAME);
    paramsInitial.add(GrouperProvisioningSettings.provisioningConfigStemName()+":"+GrouperProvisioningAttributeNames.PROVISIONING_TARGET);
    paramsInitial.add(GrouperProvisioningSettings.provisioningConfigStemName()+":"+GrouperProvisioningAttributeNames.PROVISIONING_DO_PROVISION);
    
    paramsInitial.add(this.grouperProvisioner.getConfigId());
    paramsInitial.addAll(subjectSources);
    paramsInitial.addAll(fieldIds);
    
    List<Type> typesInitial = new ArrayList<Type>();
    typesInitial.add(StringType.INSTANCE);
    
    typesInitial.add(StringType.INSTANCE);
    typesInitial.add(StringType.INSTANCE);
    typesInitial.add(StringType.INSTANCE);
    typesInitial.add(StringType.INSTANCE);
    
    for (int j = 0; j < (GrouperUtil.length(subjectSources) + GrouperUtil.length(fieldIds)); j++) {
      typesInitial.add(StringType.INSTANCE);
    }
    
    sqlInitial.append(" and gm.subject_source in (");
    sqlInitial.append(HibUtils.convertToInClauseForSqlStatic(subjectSources));
    sqlInitial.append(") ");
    
    sqlInitial.append(" and gmav.field_id in (");
    sqlInitial.append(HibUtils.convertToInClauseForSqlStatic(fieldIds));
    sqlInitial.append(") ");
    
    List<String[]> queryResults = null;
    if (retrieveAll) {

      queryResults = HibernateSession.bySqlStatic().listSelect(String[].class, sqlInitial.toString(), paramsInitial, typesInitial);
    
      List<ProvisioningEntity> grouperProvisioningEntities = getProvisioningEntityMapFromQueryResults(queryResults);
      results.addAll(grouperProvisioningEntities);
    } else {
      if (GrouperUtil.length(ids) == 0) {
        return results;
      }
      List<String> idsList = GrouperUtil.listFromCollection(ids);
      
      int numberOfBatches = GrouperUtil.batchNumberOfBatches(idsList.size(), 900);
      for (int i = 0; i < numberOfBatches; i++) {
        List<String> currentBatchIds = GrouperUtil.batchList(idsList, 900, i);
        
        List<Object> paramsCurrent = new ArrayList<Object>(paramsInitial);
        paramsCurrent.addAll(currentBatchIds);

        List<Type> typesCurrent = new ArrayList<Type>(typesInitial);
        for (int j = 0; j < GrouperUtil.length(currentBatchIds); j++) {
          typesCurrent.add(StringType.INSTANCE);
        }
        
        StringBuilder sql = new StringBuilder(sqlInitial);
        sql.append(" and gm.id in (");
        sql.append(HibUtils.convertToInClauseForSqlStatic(currentBatchIds));
        sql.append(") ");
        
        queryResults = HibernateSession.bySqlStatic().listSelect(String[].class, sql.toString(), paramsCurrent, typesCurrent);
        
        List<ProvisioningEntity> provisioningEntityMapFromQueryResults = getProvisioningEntityMapFromQueryResults(queryResults);
        results.addAll(provisioningEntityMapFromQueryResults);
        
      }

    }
    return results;
  }
  
  public List<ProvisioningMembership> retrieveMemberships(boolean retrieveAll, Collection<MultiKey> ids) {
    //TODO if groups / members dont come back in incremental query, just remove from membership?
    if (this.grouperProvisioner == null) {
      throw new RuntimeException("grouperProvisioner is not set");
    }
    
    if (retrieveAll && ids != null) {
      throw new RuntimeException("Cant retrieve all and pass in ids to retrieve!");
    }
    
    List<ProvisioningMembership> results = new ArrayList<ProvisioningMembership>();
    

    StringBuilder sqlInitial = new StringBuilder("select gmav.membership_id, gg.id, gm.id, gm.subject_id, gm.subject_source, gm.subject_identifier0, "
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
    
    List<Object> paramsInitial = new ArrayList<Object>();
    
    paramsInitial.add(GrouperProvisioningSettings.provisioningConfigStemName()+":"+GrouperProvisioningAttributeNames.PROVISIONING_ATTRIBUTE_NAME);
    paramsInitial.add(GrouperProvisioningSettings.provisioningConfigStemName()+":"+GrouperProvisioningAttributeNames.PROVISIONING_ATTRIBUTE_NAME);
    paramsInitial.add(GrouperProvisioningSettings.provisioningConfigStemName()+":"+GrouperProvisioningAttributeNames.PROVISIONING_TARGET);
    paramsInitial.add(GrouperProvisioningSettings.provisioningConfigStemName()+":"+GrouperProvisioningAttributeNames.PROVISIONING_DO_PROVISION);
    
    paramsInitial.add(this.grouperProvisioner.getConfigId());
    paramsInitial.addAll(subjectSources);
    paramsInitial.addAll(fieldIds);
    
    List<Type> typesInitial = new ArrayList<Type>();
    typesInitial.add(StringType.INSTANCE);
    typesInitial.add(StringType.INSTANCE);
    typesInitial.add(StringType.INSTANCE);
    typesInitial.add(StringType.INSTANCE);
    typesInitial.add(StringType.INSTANCE);
    for (int j = 0; j < (GrouperUtil.length(subjectSources) + GrouperUtil.length(fieldIds)); j++) {
      typesInitial.add(StringType.INSTANCE);
    }
    
    sqlInitial.append(" and gm.subject_source in (");
    sqlInitial.append(HibUtils.convertToInClauseForSqlStatic(subjectSources));
    sqlInitial.append(") ");
    
    sqlInitial.append(" and gmav.field_id in (");
    sqlInitial.append(HibUtils.convertToInClauseForSqlStatic(fieldIds));
    sqlInitial.append(") ");
    
    List<String[]> queryResults = null;
    if (retrieveAll) {
      queryResults = HibernateSession.bySqlStatic().listSelect(String[].class, sqlInitial.toString(), paramsInitial, typesInitial);
  
      List<ProvisioningMembership> grouperProvisioningMemberships = getProvisioningMembershipMapFromQueryResults(queryResults);
      
      results.addAll(grouperProvisioningMemberships);
    } else {
      if (GrouperUtil.length(ids) == 0) {
        return results;
      }
      List<MultiKey> idsList = GrouperUtil.listFromCollection(ids);
      
      int numberOfBatches = GrouperUtil.batchNumberOfBatches(idsList.size(), 450);
      StringBuilder sql = new StringBuilder(sqlInitial);
      
      sql.append(" and ( ");
      for (int i = 0; i < numberOfBatches; i++) {
        List<MultiKey> currentBatchIds = GrouperUtil.batchList(idsList, 450, i);
        
        List<Object> paramsCurrent = new ArrayList<Object>(paramsInitial);

        List<Type> typesCurrent = new ArrayList<Type>(typesInitial);
        for (int j = 0; j < GrouperUtil.length(currentBatchIds); j++) {
          typesCurrent.add(StringType.INSTANCE);
          typesCurrent.add(StringType.INSTANCE);
          paramsCurrent.add(currentBatchIds.get(j).getKey(0));
          paramsCurrent.add(currentBatchIds.get(j).getKey(1));
          if (j>0) {
            sql.append(" or ");
          }
          sql.append("  (gg.id = ? && gm.id = ?) ");
        }
        sql.append(" ) ");
        queryResults = HibernateSession.bySqlStatic().listSelect(String[].class, sql.toString(), paramsCurrent, typesCurrent);
        
        List<ProvisioningMembership> provisioningMembershipMapFromQueryResults = getProvisioningMembershipMapFromQueryResults(queryResults);
        results.addAll(provisioningMembershipMapFromQueryResults);
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

    Map<MultiKey, ProvisioningMembershipWrapper> groupUuidMemberUuidToProvisioningMembershipWrapper
      = this.getGrouperProvisioner().getGrouperProvisioningData().getGroupUuidMemberUuidToProvisioningMembershipWrapper();

    // add wrappers for memberships
    for (ProvisioningMembership provisioningMembership : GrouperUtil.nonNull(grouperProvisioningObjects.getProvisioningMemberships())) {
      ProvisioningMembershipWrapper provisioningMembershipWrapper = new ProvisioningMembershipWrapper();
      provisioningMembershipWrapper.setGrouperProvisioner(this.grouperProvisioner);
      provisioningMembershipWrapper.setGrouperProvisioningMembership(provisioningMembership);
      provisioningMembership.setProvisioningMembershipWrapper(provisioningMembershipWrapper);
      groupUuidMemberUuidToProvisioningMembershipWrapper.put(new MultiKey(provisioningMembership.getProvisioningGroupId(), provisioningMembership.getProvisioningEntityId()), 
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

  public void retrieveGrouperData(GrouperProvisioningType grouperProvisioningType) {
    Map<String, Object> debugMap = this.getGrouperProvisioner().getDebugMap();

    GrouperProvisioningLists grouperProvisioningObjects = 
        this.getGrouperProvisioner().getGrouperProvisioningData().getGrouperProvisioningObjects();
    
    {
      long start = System.currentTimeMillis();
      grouperProvisioningObjects.setProvisioningGroups(grouperProvisioningType.retrieveGrouperGroups(this.grouperProvisioner));
      debugMap.put("retrieveGrouperGroupsMillis", System.currentTimeMillis() - start);
      debugMap.put("grouperGroupCount", GrouperUtil.length(grouperProvisioningObjects.getProvisioningGroups()));
    }
    {
      long start = System.currentTimeMillis();
      grouperProvisioningObjects.setProvisioningEntities(grouperProvisioningType.retrieveGrouperMembers(this.grouperProvisioner));
      debugMap.put("retrieveGrouperEntitiesMillis", System.currentTimeMillis() - start);
      debugMap.put("grouperEntityCount", GrouperUtil.length(grouperProvisioningObjects.getProvisioningEntities()));
    }
    {
      long start = System.currentTimeMillis();
      grouperProvisioningObjects.setProvisioningMemberships(grouperProvisioningType.retrieveGrouperMemberships(this.grouperProvisioner));
      debugMap.put("retrieveGrouperMshipsMillis", System.currentTimeMillis() - start);
      debugMap.put("grouperMshipCount", GrouperUtil.length(grouperProvisioningObjects.getProvisioningMemberships()));
    }
    
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
  
    List<ProvisioningEntity> grouperProvisioningEntities = this.getGrouperProvisioner().getGrouperProvisioningData().getGrouperProvisioningObjects().getProvisioningEntities();
    
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
        if (grouperProvisioningEntities == null) {
          grouperProvisioningEntities = new ArrayList<ProvisioningEntity>();
          this.getGrouperProvisioner().getGrouperProvisioningData().getGrouperProvisioningObjects().setProvisioningEntities(grouperProvisioningEntities);
        }
        grouperProvisioningEntities.add(provisioningEntity);

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
  
    Map<MultiKey, GcGrouperSyncMembership> groupUuidMemberUuidToSyncMembership = this.getGrouperProvisioner().getGrouperProvisioningData().getGroupUuidMemberUuidToSyncMembership();
  
    List<ProvisioningMembership> grouperProvisioningMemberships = this.getGrouperProvisioner().getGrouperProvisioningData()
        .getGrouperProvisioningObjects().getProvisioningMemberships();
    
    Map<MultiKey, ProvisioningMembershipWrapper> groupUuidMemberUuidToProvisioningMembershipWrapper = this.getGrouperProvisioner()
        .getGrouperProvisioningData().getGroupUuidMemberUuidToProvisioningMembershipWrapper();

    Map<String, ProvisioningGroupWrapper> groupUuidToProvisioningGroupWrapper = this.getGrouperProvisioner().getGrouperProvisioningData().getGroupUuidToProvisioningGroupWrapper();
    Map<String, ProvisioningEntityWrapper> memberUuidToProvisioningEntityWrapper = this.getGrouperProvisioner().getGrouperProvisioningData().getMemberUuidToProvisioningEntityWrapper();

    int provisioningMshipsToDelete = 0;
    
    // loop through sync groups
    for (MultiKey groupUuidMemberUuid : GrouperUtil.nonNull(groupUuidMemberUuidToSyncMembership).keySet()) {

      String groupId = (String)groupUuidMemberUuid.getKey(0);
      String memberId = (String)groupUuidMemberUuid.getKey(1);

      GcGrouperSyncMembership gcGrouperSyncMembership = groupUuidMemberUuidToSyncMembership.get(groupUuidMemberUuid);
      
      ProvisioningMembershipWrapper provisioningMembershipWrapper = groupUuidMemberUuidToProvisioningMembershipWrapper.get(groupUuidMemberUuid);
      
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

        if (grouperProvisioningMemberships == null) {
          grouperProvisioningMemberships = new ArrayList<ProvisioningMembership>();
          this.getGrouperProvisioner().getGrouperProvisioningData().getGrouperProvisioningObjects().setProvisioningMemberships(grouperProvisioningMemberships);
        }
        grouperProvisioningMemberships.add(provisioningMembership);

        
        provisioningMembershipWrapper = new ProvisioningMembershipWrapper();
        provisioningMembershipWrapper.setGrouperProvisioner(this.grouperProvisioner);
        provisioningMembership.setProvisioningMembershipWrapper(provisioningMembershipWrapper);
        provisioningMembershipWrapper.setGrouperProvisioningMembershipToDelete(provisioningMembership);
        
        groupUuidMemberUuidToProvisioningMembershipWrapper.put(groupUuidMemberUuid, provisioningMembershipWrapper);
        
      }
      provisioningMembershipWrapper.setGcGrouperSyncMembership(gcGrouperSyncMembership);
    }      
    if (provisioningMshipsToDelete > 0) {
      this.getGrouperProvisioner().getDebugMap().put("provisioningMshipsToDelete", provisioningMshipsToDelete);
    }
  }

  private Set<String> incrementalGroupUuids = null;
  
  public Set<String> incrementalGroupUuids() {
    if (incrementalGroupUuids == null) {
      this.incrementalGroupUuids = new TreeSet<String>();
      // this is coming from provisioning consumer
      if (this.getGrouperProvisioner().getProvisioningConsumer() != null) {
  
        for (String groupIdToSync : GrouperUtil.nonNull(this.getGrouperProvisioner().getProvisioningConsumer().getGroupIdsToGroupSync())) {
          incrementalGroupUuids.add(groupIdToSync);
        }
        for (MultiKey groupIdMemberIdFieldIdToSync : GrouperUtil.nonNull(this.getGrouperProvisioner().getProvisioningConsumer().getMembershipsToSync())) {
          incrementalGroupUuids.add((String)groupIdMemberIdFieldIdToSync.getKey(0));
        }
      }
      this.grouperProvisioner.getDebugMap().put("groupUuidCount", GrouperUtil.length(incrementalGroupUuids));
    }
    return this.incrementalGroupUuids;
  }

  private Set<String> incrementalMemberUuids = null;

  public Set<String> incrementalMemberUuids() {
    this.incrementalMemberUuids = new TreeSet<String>();
    // this is coming from provisioning consumer
    if (this.getGrouperProvisioner().getProvisioningConsumer() != null) {
      for (String memberIdToSync : GrouperUtil.nonNull(this.getGrouperProvisioner().getProvisioningConsumer().getMemberIdsToUserSync())) {
        incrementalMemberUuids.add(memberIdToSync);
      }
      for (MultiKey groupIdMemberIdFieldIdToSync : GrouperUtil.nonNull(this.getGrouperProvisioner().getProvisioningConsumer().getMembershipsToSync())) {
        incrementalMemberUuids.add((String)groupIdMemberIdFieldIdToSync.getKey(1));
      }
    }
    this.grouperProvisioner.getDebugMap().put("memberUuidCount", GrouperUtil.length(incrementalMemberUuids));
    return incrementalMemberUuids;
  }

  private Set<MultiKey> incrementalGroupUuidsMemberUuids = null;

  public Set<MultiKey> incrementalGroupUuidsMemberUuids() {
    this.incrementalGroupUuidsMemberUuids = new TreeSet<MultiKey>();
    // this is coming from provisioning consumer
    if (this.getGrouperProvisioner().getProvisioningConsumer() != null) {
      for (MultiKey groupIdMemberIdFieldIdToSync : GrouperUtil.nonNull(this.getGrouperProvisioner().getProvisioningConsumer().getMembershipsToSync())) {
        incrementalGroupUuidsMemberUuids.add(new MultiKey(groupIdMemberIdFieldIdToSync.getKey(0), groupIdMemberIdFieldIdToSync.getKey(1)));
      }
    }
    this.grouperProvisioner.getDebugMap().put("groupUuidsMemberUuidsCount", GrouperUtil.length(incrementalGroupUuidsMemberUuids));
    return incrementalGroupUuidsMemberUuids;
  }

}
