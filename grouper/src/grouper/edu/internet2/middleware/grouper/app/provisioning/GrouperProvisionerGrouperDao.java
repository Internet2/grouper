package edu.internet2.middleware.grouper.app.provisioning;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
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
    
    
    
    List<String> subjectSources = new ArrayList<String>(grouperProvisioner.retrieveGrouperProvisioningConfiguration().getSubjectSourcesToProvision());
    
    List<String> fieldIds = new ArrayList<String>();
    GrouperProvisioningMembershipFieldType membershipFieldType = grouperProvisioner.retrieveGrouperProvisioningConfiguration().getGrouperProvisioningMembershipFieldType();
    
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
  
  /**
   * 
   * @param retrieveAll
   * @param ids
   * @return the memberships
   */
  public List<ProvisioningMembership> retrieveMemberships(boolean retrieveAll, Collection<String> groupUuidsForGroupSync, 
      Collection<String> memberUuidsForEntitySync, Collection<MultiKey> groupUuidMemberUuidFieldIds) {
    if (this.grouperProvisioner == null) {
      throw new RuntimeException("grouperProvisioner is not set");
    }
    
    if (retrieveAll && (groupUuidsForGroupSync != null || memberUuidsForEntitySync != null || groupUuidMemberUuidFieldIds != null)) {
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
    
    List<String> subjectSources = new ArrayList<String>(grouperProvisioner.retrieveGrouperProvisioningConfiguration().getSubjectSourcesToProvision());
    
    List<String> fieldIds = new ArrayList<String>();
    GrouperProvisioningMembershipFieldType membershipFieldType = grouperProvisioner.retrieveGrouperProvisioningConfiguration().getGrouperProvisioningMembershipFieldType();
    
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
      if (GrouperUtil.length(groupUuidsForGroupSync) == 0 && 
          GrouperUtil.length(memberUuidsForEntitySync) == 0 && GrouperUtil.length(groupUuidMemberUuidFieldIds) == 0) {
        return results;
      }
      List<MultiKey> groupUuidsMemberUuidsList = GrouperUtil.listFromCollection(GrouperUtil.nonNull(groupUuidMemberUuidFieldIds));
      for (String groupUuid : GrouperUtil.nonNull(groupUuidsForGroupSync)) {
        groupUuidsMemberUuidsList.add(new MultiKey(groupUuid, null));
      }
      for (String memberUuid : GrouperUtil.nonNull(memberUuidsForEntitySync)) {
        groupUuidsMemberUuidsList.add(new MultiKey(null, memberUuid));
      }
      
      int numberOfBatches = GrouperUtil.batchNumberOfBatches(groupUuidsMemberUuidsList.size(), 450);
      StringBuilder sql = new StringBuilder(sqlInitial);
      
      sql.append(" and ( ");
      for (int i = 0; i < numberOfBatches; i++) {
        List<MultiKey> currentBatchIds = GrouperUtil.batchList(groupUuidsMemberUuidsList, 450, i);
        
        List<Object> paramsCurrent = new ArrayList<Object>(paramsInitial);

        List<Type> typesCurrent = new ArrayList<Type>(typesInitial);
        for (int j = 0; j < GrouperUtil.length(currentBatchIds); j++) {
          if (j>0) {
            sql.append(" or ");
          }
          String groupUuid = (String)currentBatchIds.get(j).getKey(0);
          String memberUuid = (String)currentBatchIds.get(j).getKey(1);

          if (!StringUtils.isBlank(groupUuid) && !StringUtils.isBlank(memberUuid)) {
            typesCurrent.add(StringType.INSTANCE);
            typesCurrent.add(StringType.INSTANCE);
            paramsCurrent.add(groupUuid);
            paramsCurrent.add(memberUuid);
            sql.append("  (gg.id = ? && gm.id = ?) ");
          } else if (!StringUtils.isBlank(groupUuid) && StringUtils.isBlank(memberUuid)) {
            typesCurrent.add(StringType.INSTANCE);
            paramsCurrent.add(groupUuid);
            sql.append(" gg.id = ? ");
          } else if (StringUtils.isBlank(groupUuid) && !StringUtils.isBlank(memberUuid)) {
            typesCurrent.add(StringType.INSTANCE);
            paramsCurrent.add(memberUuid);
            sql.append(" gm.id = ? ");
          } else {
            // shouldnt happen
            throw new RuntimeException("Why is groupUuid and memberUuid blank in a grouper membership query?????");
          }
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
        this.getGrouperProvisioner().retrieveGrouperProvisioningData().getGrouperProvisioningObjects();

    Map<String, ProvisioningGroupWrapper> groupUuidToProvisioningGroupWrapper
      = this.getGrouperProvisioner().retrieveGrouperProvisioningData().getGroupUuidToProvisioningGroupWrapper();
    
    // add wrappers for all groups
    for (ProvisioningGroup provisioningGroup : GrouperUtil.nonNull(grouperProvisioningObjects.getProvisioningGroups())) {
      ProvisioningGroupWrapper provisioningGroupWrapper = new ProvisioningGroupWrapper();
      provisioningGroupWrapper.setGrouperProvisioner(this.grouperProvisioner);

      provisioningGroupWrapper.setGrouperProvisioningGroup(provisioningGroup);
      provisioningGroup.setProvisioningGroupWrapper(provisioningGroupWrapper);
      groupUuidToProvisioningGroupWrapper.put(provisioningGroup.getId(), provisioningGroupWrapper);
    }
    
    Map<String, ProvisioningEntityWrapper> memberUuidToProvisioningEntityWrapper
      = this.getGrouperProvisioner().retrieveGrouperProvisioningData().getMemberUuidToProvisioningEntityWrapper();
    
    // add wrappers for all entities
    for (ProvisioningEntity provisioningEntity : GrouperUtil.nonNull(grouperProvisioningObjects.getProvisioningEntities())) {
      ProvisioningEntityWrapper provisioningEntityWrapper = new ProvisioningEntityWrapper();
      provisioningEntityWrapper.setGrouperProvisioner(this.grouperProvisioner);
      provisioningEntityWrapper.setGrouperProvisioningEntity(provisioningEntity);
      provisioningEntity.setProvisioningEntityWrapper(provisioningEntityWrapper);
      memberUuidToProvisioningEntityWrapper.put(provisioningEntity.getId(), provisioningEntityWrapper);
    }

    Map<MultiKey, ProvisioningMembershipWrapper> groupUuidMemberUuidToProvisioningMembershipWrapper
      = this.getGrouperProvisioner().retrieveGrouperProvisioningData().getGroupUuidMemberUuidToProvisioningMembershipWrapper();

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
  
  /**
   * if the membership query has stuff thats not in the group or entity query.
   * and change the membership references to the group and entities retrieved
   */
  public void fixGrouperProvisioningMembershipReferences() {
    
    GrouperProvisioningLists grouperProvisioningObjects = 
        this.getGrouperProvisioner().retrieveGrouperProvisioningData().getGrouperProvisioningObjects();
    
    Map<String, Object> debugMap = this.getGrouperProvisioner().getDebugMap();
    
    int missingGrouperProvisioningMembershipReferencesCount = 0;

    {
      Map<String, ProvisioningGroupWrapper> groupUuidToProvisioningGroupWrapper
      = this.getGrouperProvisioner().retrieveGrouperProvisioningData().getGroupUuidToProvisioningGroupWrapper();
    
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
      = this.getGrouperProvisioner().retrieveGrouperProvisioningData().getMemberUuidToProvisioningEntityWrapper();
    
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
      grouperProvisioningGroup.assignAttributeValue("description", description);
      
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

      grouperProvisioningEntity.assignAttributeValue("description", description);
      grouperProvisioningEntity.assignAttributeValue("subjectId", subjectId);
      grouperProvisioningEntity.assignAttributeValue("subjectIdentifier0", subjectIdentifier0);
      
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
        targetEntity.assignAttributeValue("description", description);
        targetEntity.assignAttributeValue("subjectId", subjectId);
        targetEntity.assignAttributeValue("subjectSourceId", subjectSourceId);
        targetEntity.assignAttributeValue("subjectIdentifier0", subjectIdentifier0);
        
        
        grouperProvisioningMembership.setProvisioningEntity(targetEntity);
        grouperProvisioningMembership.setProvisioningEntityId(memberId);
      }
      {
        ProvisioningGroup targetGroup = new ProvisioningGroup();
        targetGroup.setId(groupId);
        targetGroup.setName(groupName);
        targetGroup.setDisplayName(groupDisplayName);
        targetGroup.assignAttributeValue("description", groupDescription);

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
        this.getGrouperProvisioner().retrieveGrouperProvisioningData().getGrouperProvisioningObjects();
    
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

}
