package edu.internet2.middleware.grouper.app.provisioning;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.hibernate.type.StringType;
import org.hibernate.type.Type;

import edu.internet2.middleware.grouper.FieldFinder;
import edu.internet2.middleware.grouper.hibernate.HibUtils;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;

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

    StringBuilder sqlInitial = new StringBuilder("select gm.id, gm.subject_source, gm.subject_id, gm.subject_identifier0, gm.name, gm.description " +
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
      Collection<String> memberUuidsForEntitySync, Collection<MultiKey> groupUuidMemberUuids) {
    if (this.grouperProvisioner == null) {
      throw new RuntimeException("grouperProvisioner is not set");
    }
    
    if (retrieveAll && (groupUuidsForGroupSync != null || memberUuidsForEntitySync != null || groupUuidMemberUuids != null)) {
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
          GrouperUtil.length(memberUuidsForEntitySync) == 0 && GrouperUtil.length(groupUuidMemberUuids) == 0) {
        return results;
      }
      List<MultiKey> groupUuidsMemberUuidsList = GrouperUtil.listFromCollection(GrouperUtil.nonNull(groupUuidMemberUuids));
      for (String groupUuid : GrouperUtil.nonNull(groupUuidsForGroupSync)) {
        groupUuidsMemberUuidsList.add(new MultiKey(groupUuid, null));
      }
      for (String memberUuid : GrouperUtil.nonNull(memberUuidsForEntitySync)) {
        groupUuidsMemberUuidsList.add(new MultiKey(null, memberUuid));
      }
      
      int numberOfBatches = GrouperUtil.batchNumberOfBatches(groupUuidsMemberUuidsList.size(), 450);
      
      for (int i = 0; i < numberOfBatches; i++) {
        List<MultiKey> currentBatchIds = GrouperUtil.batchList(groupUuidsMemberUuidsList, 450, i);
        StringBuilder sql = new StringBuilder(sqlInitial);
        sql.append(" and ( ");

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
            sql.append("  (gg.id = ? and gm.id = ?) ");
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
  
  /**
   * add wrappers to grouper data and put in grouper uuid maps, and list of wrappers
   * put wrappers on the grouper objects and put in the grouper uuid maps in data object
   * put these wrapper in the GrouperProvisioningData and GrouperProvisioningDataIndex
   */
  public void processWrappers() {
    
    GrouperProvisioningLists grouperProvisioningObjects = 
        this.getGrouperProvisioner().retrieveGrouperProvisioningDataGrouper().getGrouperProvisioningObjects();

    Map<String, ProvisioningGroupWrapper> groupUuidToProvisioningGroupWrapper
      = this.getGrouperProvisioner().retrieveGrouperProvisioningDataIndex().getGroupUuidToProvisioningGroupWrapper();

    Set<ProvisioningGroupWrapper> provisioningGroupWrappers = this.getGrouperProvisioner().retrieveGrouperProvisioningData().getProvisioningGroupWrappers();
    
    // add wrappers for all groups
    for (ProvisioningGroup provisioningGroup : GrouperUtil.nonNull(grouperProvisioningObjects.getProvisioningGroups())) {
      ProvisioningGroupWrapper provisioningGroupWrapper = groupUuidToProvisioningGroupWrapper.get(provisioningGroup.getId());
      if (provisioningGroupWrapper == null) {
        provisioningGroupWrapper = new ProvisioningGroupWrapper();
        provisioningGroupWrapper.setGrouperProvisioner(this.grouperProvisioner);
        provisioningGroupWrappers.add(provisioningGroupWrapper);
        groupUuidToProvisioningGroupWrapper.put(provisioningGroup.getId(), provisioningGroupWrapper);
      }
      provisioningGroupWrapper.setGrouperProvisioningGroup(provisioningGroup);
    }
    
    Map<String, ProvisioningEntityWrapper> memberUuidToProvisioningEntityWrapper
      = this.getGrouperProvisioner().retrieveGrouperProvisioningDataIndex().getMemberUuidToProvisioningEntityWrapper();
    
    Set<ProvisioningEntityWrapper> provisioningEntityWrappers = this.getGrouperProvisioner().retrieveGrouperProvisioningData().getProvisioningEntityWrappers();

    // add wrappers for all entities
    for (ProvisioningEntity provisioningEntity : GrouperUtil.nonNull(grouperProvisioningObjects.getProvisioningEntities())) {
      ProvisioningEntityWrapper provisioningEntityWrapper = memberUuidToProvisioningEntityWrapper.get(provisioningEntity.getId());
      if (provisioningEntityWrapper == null) {
        provisioningEntityWrapper = new ProvisioningEntityWrapper();
        provisioningEntityWrapper.setGrouperProvisioner(this.grouperProvisioner);
        provisioningEntityWrappers.add(provisioningEntityWrapper);
        memberUuidToProvisioningEntityWrapper.put(provisioningEntity.getId(), provisioningEntityWrapper);
      }      
      provisioningEntityWrapper.setGrouperProvisioningEntity(provisioningEntity);
    }

    Map<MultiKey, ProvisioningMembershipWrapper> groupUuidMemberUuidToProvisioningMembershipWrapper
      = this.getGrouperProvisioner().retrieveGrouperProvisioningDataIndex().getGroupUuidMemberUuidToProvisioningMembershipWrapper();

    Set<ProvisioningMembershipWrapper> provisioningMembershipWrappers = this.getGrouperProvisioner().retrieveGrouperProvisioningData().getProvisioningMembershipWrappers();

    // add wrappers for memberships
    for (ProvisioningMembership provisioningMembership : GrouperUtil.nonNull(grouperProvisioningObjects.getProvisioningMemberships())) {

      MultiKey groupIdMemberId = new MultiKey(provisioningMembership.getProvisioningGroupId(), provisioningMembership.getProvisioningEntityId());
      ProvisioningMembershipWrapper provisioningMembershipWrapper = groupUuidMemberUuidToProvisioningMembershipWrapper.get(groupIdMemberId);
      if (provisioningMembershipWrapper == null) {
        provisioningMembershipWrapper = new ProvisioningMembershipWrapper();
        provisioningMembershipWrapper.setGrouperProvisioner(this.grouperProvisioner);
        provisioningMembershipWrappers.add(provisioningMembershipWrapper);
        groupUuidMemberUuidToProvisioningMembershipWrapper.put(groupIdMemberId, 
            provisioningMembershipWrapper);
      }      
      provisioningMembershipWrapper.setGrouperProvisioningMembership(provisioningMembership);
    }
  }
  
  /**
   * if the membership query has stuff thats not in the group or entity query.
   * and change the membership references to the group and entities retrieved
   * point the membership pointers to groups and entities to what they should point to
   * and fix data problems (for instance race conditions as data was retrieved)
   */
  public void fixGrouperProvisioningMembershipReferences() {
    
    Map<String, Object> debugMap = this.getGrouperProvisioner().getDebugMap();
    
    int missingGrouperProvisioningMembershipReferencesCount = 0;

    {
      Map<String, ProvisioningGroupWrapper> groupUuidToProvisioningGroupWrapper
      = this.getGrouperProvisioner().retrieveGrouperProvisioningDataIndex().getGroupUuidToProvisioningGroupWrapper();
    
      // add wrappers for groups
      for (ProvisioningMembershipWrapper provisioningMembershipWrapper : this.grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningMembershipWrappers()) {
  
        ProvisioningMembership provisioningMembership = provisioningMembershipWrapper.getGrouperProvisioningMembership();

        if (provisioningMembership == null) {
          continue;
        }
        // pull up the existing group
        ProvisioningGroupWrapper provisioningGroupWrapper = groupUuidToProvisioningGroupWrapper.get(provisioningMembership.getProvisioningGroupId());
        
        ProvisioningGroup grouperProvisioningGroup = provisioningGroupWrapper == null ? null : provisioningGroupWrapper.getGrouperProvisioningGroup();
        
        // if its not there (e.g. membership added after group query and before membership query?
        if (grouperProvisioningGroup == null) {
          missingGrouperProvisioningMembershipReferencesCount++;
          if (provisioningGroupWrapper == null) {
            provisioningGroupWrapper = new ProvisioningGroupWrapper();
            provisioningGroupWrapper.setGrouperProvisioner(this.grouperProvisioner);
            this.grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningGroupWrappers().add(provisioningGroupWrapper);
            groupUuidToProvisioningGroupWrapper.put(provisioningMembership.getProvisioningGroupId(), provisioningGroupWrapper);
          }
          // all the data is in the membership query
          provisioningGroupWrapper.setGrouperProvisioningGroup(provisioningMembership.getProvisioningGroup());
        } else {
          provisioningMembership.setProvisioningGroup(grouperProvisioningGroup);
        }
        
      }
    }

    {
      Map<String, ProvisioningEntityWrapper> memberUuidToProvisioningEntityWrapper
      = this.getGrouperProvisioner().retrieveGrouperProvisioningDataIndex().getMemberUuidToProvisioningEntityWrapper();
    
      // add wrappers for entities
      for (ProvisioningMembershipWrapper provisioningMembershipWrapper : this.grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningMembershipWrappers()) {
        
        ProvisioningMembership provisioningMembership = provisioningMembershipWrapper.getGrouperProvisioningMembership();
  
        if (provisioningMembership == null) {
          continue;
        }
        
        // pull up the existing group
        ProvisioningEntityWrapper provisioningEntityWrapper = memberUuidToProvisioningEntityWrapper.get(provisioningMembership.getProvisioningEntityId());

        ProvisioningEntity grouperProvisioningEntity = provisioningEntityWrapper == null ? null : provisioningEntityWrapper.getGrouperProvisioningEntity();
        
        // if its not there (e.g. membership added after group query and before membership query?
        if (grouperProvisioningEntity == null) {

          if (provisioningEntityWrapper == null) {
            missingGrouperProvisioningMembershipReferencesCount++;
            provisioningEntityWrapper = new ProvisioningEntityWrapper();
            provisioningEntityWrapper.setGrouperProvisioner(this.grouperProvisioner);
            
            this.grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningEntityWrappers().add(provisioningEntityWrapper);
            memberUuidToProvisioningEntityWrapper.put(provisioningMembership.getProvisioningEntityId(), provisioningEntityWrapper);
          }
          // all the data is in the membership query
          provisioningEntityWrapper.setGrouperProvisioningEntity(provisioningMembership.getProvisioningEntity());
        } else {
          provisioningMembership.setProvisioningEntity(grouperProvisioningEntity);
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
      String subjectSource = queryResult[1];
      String subjectId = queryResult[2];
      String subjectIdentifier0 = queryResult[3];
      String name = queryResult[4];
      String description = queryResult[5];
      
      ProvisioningEntity grouperProvisioningEntity = new ProvisioningEntity();
      grouperProvisioningEntity.setId(id);
      grouperProvisioningEntity.setName(name);
      grouperProvisioningEntity.setSubjectId(subjectId);
      //TODO do something with email?
      grouperProvisioningEntity.assignAttributeValue("subjectSourceId", subjectSource);
      grouperProvisioningEntity.assignAttributeValue("description", description);
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
        targetEntity.setSubjectId(subjectId);
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
  
  public void retrieveGrouperDataFull() {
    Map<String, Object> debugMap = this.getGrouperProvisioner().getDebugMap();

    GrouperProvisioningLists grouperProvisioningObjects = 
        this.getGrouperProvisioner().retrieveGrouperProvisioningDataGrouper().getGrouperProvisioningObjects();
    
    {
      long start = System.currentTimeMillis();
      grouperProvisioningObjects.setProvisioningGroups(retrieveGroups(true, null));
      debugMap.put("retrieveGrouperGroupsMillis", System.currentTimeMillis() - start);
      debugMap.put("grouperGroupCount", GrouperUtil.length(grouperProvisioningObjects.getProvisioningGroups()));
    }
    {
      long start = System.currentTimeMillis();
      grouperProvisioningObjects.setProvisioningEntities(retrieveMembers(true, null));
      debugMap.put("retrieveGrouperEntitiesMillis", System.currentTimeMillis() - start);
      debugMap.put("grouperEntityCount", GrouperUtil.length(grouperProvisioningObjects.getProvisioningEntities()));
    }
    {
      long start = System.currentTimeMillis();
      grouperProvisioningObjects.setProvisioningMemberships(retrieveMemberships(true, null, null, null));
      debugMap.put("retrieveGrouperMshipsMillis", System.currentTimeMillis() - start);
      debugMap.put("grouperMshipCount", GrouperUtil.length(grouperProvisioningObjects.getProvisioningMemberships()));
    }
    
  }

  
  public void retrieveGrouperDataIncremental() {

    Map<String, Object> debugMap = this.getGrouperProvisioner().getDebugMap();

    GrouperProvisioningLists grouperProvisioningObjects = 
        this.getGrouperProvisioner().retrieveGrouperProvisioningDataGrouper().getGrouperProvisioningObjects();

    Set<String> groupIdsToRetrieve = new HashSet<String>();
    Set<String> memberIdsToRetrieve = new HashSet<String>();

    GrouperIncrementalDataToProcess grouperIncrementalDataToProcessWithoutRecalc = grouperProvisioner.retrieveGrouperProvisioningDataIncrementalInput().getGrouperIncrementalDataToProcessWithoutRecalc();
    GrouperIncrementalDataToProcess grouperIncrementalDataToProcessWithRecalc = grouperProvisioner.retrieveGrouperProvisioningDataIncrementalInput().getGrouperIncrementalDataToProcessWithRecalc();
    GrouperIncrementalDataToProcess[] grouperIncrementalDataToProcesses = new GrouperIncrementalDataToProcess[] {
        grouperIncrementalDataToProcessWithoutRecalc, grouperIncrementalDataToProcessWithRecalc };
    
    {
      long start = System.currentTimeMillis();
      
      Set<String> groupIdsToRetrieveForMemberships = new HashSet<String>();
      Set<String> memberIdsToRetrieveForMemberships = new HashSet<String>();
      Set<MultiKey> groupIdsMemberIdsToRetrieveForMemberships = new HashSet<MultiKey>();
      
      //go from the actions that happened to what we need to retrieve from Grouper
      //retrieve everything whether recalc or not
      for (GrouperIncrementalDataToProcess grouperIncrementalDataToProcess : grouperIncrementalDataToProcesses) {
        for (GrouperIncrementalDataItem grouperIncrementalDataItem : 
            grouperIncrementalDataToProcess.getGroupUuidsForGroupMembershipSync()) {
          groupIdsToRetrieveForMemberships.add((String)grouperIncrementalDataItem.getItem());
        }
        for (GrouperIncrementalDataItem grouperIncrementalDataItem : 
            grouperIncrementalDataToProcess.getMemberUuidsForEntityMembershipSync()) {
          memberIdsToRetrieveForMemberships.add((String)grouperIncrementalDataItem.getItem());
        }
        for (GrouperIncrementalDataItem grouperIncrementalDataItem : 
            grouperIncrementalDataToProcess.getGroupUuidsMemberUuidsForMembershipSync()) {
          MultiKey groupIdMemberId = (MultiKey)grouperIncrementalDataItem.getItem();
          groupIdsMemberIdsToRetrieveForMemberships.add(groupIdMemberId);
          groupIdsToRetrieve.add((String)groupIdMemberId.getKey(0));
          memberIdsToRetrieve.add((String)groupIdMemberId.getKey(1));
        }
      }
      groupIdsToRetrieve.addAll(groupIdsToRetrieveForMemberships);
      memberIdsToRetrieve.addAll(memberIdsToRetrieveForMemberships);


      grouperProvisioningObjects.setProvisioningMemberships(retrieveMemberships(false, groupIdsToRetrieveForMemberships, 
          memberIdsToRetrieveForMemberships, groupIdsMemberIdsToRetrieveForMemberships));
      debugMap.put("retrieveGrouperMshipsMillis", System.currentTimeMillis() - start);
      debugMap.put("grouperMshipCount", GrouperUtil.length(grouperProvisioningObjects.getProvisioningMemberships()));
      
      // maybe more data came back?  fill in some pieces
      for (ProvisioningMembership provisioningMembership : grouperProvisioningObjects.getProvisioningMemberships()) {
        groupIdsToRetrieve.add(provisioningMembership.getProvisioningGroupId());
        memberIdsToRetrieve.add(provisioningMembership.getProvisioningEntityId());
      }
      
    }
    {
      long start = System.currentTimeMillis();
      grouperProvisioningObjects.setProvisioningGroups(retrieveGroups(false, groupIdsToRetrieve));
      debugMap.put("retrieveGrouperGroupsMillis", System.currentTimeMillis() - start);
      debugMap.put("grouperGroupCount", GrouperUtil.length(grouperProvisioningObjects.getProvisioningGroups()));
    }
    {
      long start = System.currentTimeMillis();
      grouperProvisioningObjects.setProvisioningEntities(retrieveMembers(false, memberIdsToRetrieve));
      debugMap.put("retrieveGrouperEntitiesMillis", System.currentTimeMillis() - start);
      debugMap.put("grouperEntityCount", GrouperUtil.length(grouperProvisioningObjects.getProvisioningEntities()));
    }
    
  }

}
