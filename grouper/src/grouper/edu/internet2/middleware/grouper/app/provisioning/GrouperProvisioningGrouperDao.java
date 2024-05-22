package edu.internet2.middleware.grouper.app.provisioning;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.hibernate.type.StringType;
import org.hibernate.type.Type;

import com.fasterxml.jackson.databind.JsonNode;

import edu.internet2.middleware.grouper.FieldFinder;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.app.grouperTypes.GrouperObjectTypesAttributeNames;
import edu.internet2.middleware.grouper.app.grouperTypes.GrouperObjectTypesSettings;
import edu.internet2.middleware.grouper.app.loader.GrouperDaemonUtils;
import edu.internet2.middleware.grouper.ddl.GrouperDdlUtils;
import edu.internet2.middleware.grouper.hibernate.HibUtils;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;

public class GrouperProvisioningGrouperDao {

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
  
  public List<ProvisioningGroup> retrieveGroupsFromNames(Collection<String> names) {
    if (this.grouperProvisioner == null) {
      throw new RuntimeException("grouperProvisioner is not set");
    }
    
    List<ProvisioningGroup> results = new ArrayList<ProvisioningGroup>();
    
    if (GrouperUtil.length(names) == 0) {
      return results;
    }
    
    String sqlInitial = "select " + 
        "    gg.id, " + 
        "    gg.name, " + 
        "    gg.display_name, " + 
        "    gg.description, " + 
        "    gg.id_index, " +
        "    gsg.metadata_json " +
        "from " + 
        "    grouper_groups gg, " + 
        "    grouper_sync_group gsg " + 
        "where " + 
        "    gsg.grouper_sync_id = ? " +
        "    and gg.id = gsg.group_id " +
        "    and gg.type_of_group != 'entity' " + 
        "    and gsg.provisionable = 'T' ";
    
    List<Object> paramsInitial = new ArrayList<Object>();
    paramsInitial.add(this.grouperProvisioner.getGcGrouperSync().getId());
    
    List<Type> typesInitial = new ArrayList<Type>();
    typesInitial.add(StringType.INSTANCE);
    
    List<String[]> queryResults = null;
    List<String> namesList = GrouperUtil.listFromCollection(names);
    
    int numberOfBatches = GrouperUtil.batchNumberOfBatches(namesList.size(), 900);
    for (int i = 0; i < numberOfBatches; i++) {
      List<String> currentBatchNames = GrouperUtil.batchList(namesList, 900, i);
      
      List<Object> params = new ArrayList<Object>(paramsInitial);
      params.addAll(currentBatchNames);

      List<Type> types = new ArrayList<Type>(typesInitial);

      for (int j = 0; j < GrouperUtil.length(currentBatchNames); j++) {
        types.add(StringType.INSTANCE);
      }
      
      StringBuilder sql = new StringBuilder(sqlInitial);
      sql.append(" and gg.name in (");
      sql.append(HibUtils.convertToInClauseForSqlStatic(currentBatchNames));
      sql.append(") ");
      
      queryResults = HibernateSession.bySqlStatic().listSelect(String[].class, sql.toString(), params, types);
      
      List<ProvisioningGroup> targetGroupMapFromQueryResults = getTargetGroupMapFromQueryResults(queryResults);
      results.addAll(GrouperUtil.nonNull(targetGroupMapFromQueryResults));
    }      

    return results;
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
    
    String sqlInitial = "select " + 
        "    gg.id, " + 
        "    gg.name, " + 
        "    gg.display_name, " + 
        "    gg.description, " + 
        "    gg.id_index, " +
        "    gsg.metadata_json " +
        "from " + 
        "    grouper_groups gg, " + 
        "    grouper_sync_group gsg " + 
        "where " + 
        "    gsg.grouper_sync_id = ? " +
        "    and gg.id = gsg.group_id " +
        "    and gg.type_of_group != 'entity' " + 
        "    and gsg.provisionable = 'T' ";
    
    List<Object> paramsInitial = new ArrayList<Object>();
    paramsInitial.add(this.grouperProvisioner.getGcGrouperSync().getId());
    
    List<Type> typesInitial = new ArrayList<Type>();
    typesInitial.add(StringType.INSTANCE);
    
    // TODO
    // if (this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().isGroupRequireMembers()) {
    //   sqlInitial += "";
    // }
    
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
  
  /**
   * 
   * @param retrieveAll
   * @param ids - list of member uuids
   * @return
   */
  public List<ProvisioningEntity> retrieveMembers(boolean retrieveAll, Collection<String> ids) {

    if (this.grouperProvisioner == null) {
      throw new RuntimeException("grouperProvisioner is not set");
    }
    
    if (retrieveAll && ids != null) {
      throw new RuntimeException("Cant retrieve all and pass in ids to retrieve!");
    }

    List<ProvisioningEntity> results = new ArrayList<ProvisioningEntity>();
    
    String groupIdOfUsersToProvision = this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getGroupIdOfUsersToProvision();
    boolean restrictUsersByGroupId = !StringUtils.isBlank(groupIdOfUsersToProvision);

    if (restrictUsersByGroupId) {
      if (groupIdOfUsersToProvision.contains(":")) {
        groupIdOfUsersToProvision = GroupFinder.findByName(GrouperSession.staticGrouperSession(), groupIdOfUsersToProvision, true).getId();
      } else {
        groupIdOfUsersToProvision = GroupFinder.findByUuid(GrouperSession.staticGrouperSession(), groupIdOfUsersToProvision, true).getId();
      }
    }

    String groupIdOfUsersToExclude = this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getGroupIdOfUsersNotToProvision();
    boolean excludeUsersByGroupId = !StringUtils.isBlank(groupIdOfUsersToExclude);

    if (excludeUsersByGroupId) {
      if (groupIdOfUsersToExclude.contains(":")) {
        groupIdOfUsersToExclude = GroupFinder.findByName(GrouperSession.staticGrouperSession(), groupIdOfUsersToExclude, true).getId();
      } else {
        groupIdOfUsersToExclude = GroupFinder.findByUuid(GrouperSession.staticGrouperSession(), groupIdOfUsersToExclude, true).getId();
      }
    }
    
    StringBuilder sqlInitial = null;
    List<Object> paramsInitial = new ArrayList<Object>();
    List<Type> typesInitial = new ArrayList<Type>();
    
    if (restrictUsersByGroupId) {
            
      sqlInitial = new StringBuilder("select " + 
          "    gm.id, " +
          "    gm.subject_source, " + 
          "    gm.subject_id, " + 
          "    gm.subject_identifier0, " + 
          "    gm.name, " + 
          "    gm.description, " + 
          "    gsm.metadata_json, " +
          "    gm.email0, " +
          "    gm.subject_identifier1, " +
          "    gm.subject_identifier2, " +
          "    gm.id_index, " +
          "    gm.subject_resolution_resolvable " +
          "from " + 
          "    grouper_members gm " +
          "    left join grouper_sync_member gsm on  gsm.member_id = gm.id " + 
          "where " +
          "    gm.subject_resolution_deleted='F' " +
          "    and exists ( select 1 from grouper_memberships ms join grouper_group_set gs on ms.owner_id = gs.member_id " +
          "    where ms.member_id = gm.id and  ms.field_id = gs.member_field_id " +
          "    and ms.enabled='T' " +
          "    and gs.field_id = ? " +
          "    and gs.owner_group_id = ? ");
    } else {
      
      sqlInitial = new StringBuilder("select " + 
          "    gm.id, " +
          "    gm.subject_source, " + 
          "    gm.subject_id, " + 
          "    gm.subject_identifier0, " + 
          "    gm.name, " + 
          "    gm.description, " + 
          "    gsm.metadata_json, " +
          "    gm.email0, " +
          "    gm.subject_identifier1, " +
          "    gm.subject_identifier2, " +
          "    gm.id_index, " +
          "    gm.subject_resolution_resolvable " +
          "from " + 
          "    grouper_members gm " +
          "    left join grouper_sync_member gsm on  gsm.member_id = gm.id " + 
          "where " +
          "    gm.subject_resolution_deleted='F' " + 
          "    and exists ( select 1 from grouper_memberships ms join grouper_group_set gs on ms.owner_id = gs.member_id " +
          "    join grouper_sync_group gsg on gs.owner_group_id = gsg.group_id " +
          "    where ms.member_id = gm.id and  gsg.grouper_sync_id = ? " +
          "    and ms.field_id = gs.member_field_id " +
          "    and gsg.provisionable = 'T' " +
          "    and ms.enabled='T' ");
      
      paramsInitial.add(this.grouperProvisioner.getGcGrouperSync().getId());
      typesInitial.add(StringType.INSTANCE);
    }
    
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
    
    if (restrictUsersByGroupId) {
      paramsInitial.add(Group.getDefaultList().getId());
      paramsInitial.add(groupIdOfUsersToProvision);
    }
    
    paramsInitial.addAll(fieldIds);
    paramsInitial.addAll(subjectSources);
    
    if (restrictUsersByGroupId) {
      typesInitial.add(StringType.INSTANCE);
      typesInitial.add(StringType.INSTANCE);
    }

    for (int j = 0; j < (GrouperUtil.length(subjectSources) + GrouperUtil.length(fieldIds)); j++) {
      typesInitial.add(StringType.INSTANCE);
    }
    
    // exists above does not close because it closes here
    sqlInitial.append(" and gs.field_id in (");
    sqlInitial.append(HibUtils.convertToInClauseForSqlStatic(fieldIds));
    sqlInitial.append(") ) ");
    
    if (GrouperUtil.length(subjectSources) > 0) {
      sqlInitial.append(" and gm.subject_source in (");
      sqlInitial.append(HibUtils.convertToInClauseForSqlStatic(subjectSources));
      sqlInitial.append(") ");
    }
    
    if (excludeUsersByGroupId) {
      sqlInitial.append(" and not exists (select 1 from grouper_memberships gmship_to_exclude, grouper_group_set gs_to_exclude " +
            " where gmship_to_exclude.owner_id = gs_to_exclude.member_id " +
            " and gmship_to_exclude.enabled='T' " +
            " and gmship_to_exclude.field_id = gs_to_exclude.member_field_id " +
            " and gs_to_exclude.field_id = ? " +
            " and gmship_to_exclude.member_id = gm.id " +
            " and gs_to_exclude.owner_group_id = ? ) ");
      typesInitial.add(StringType.INSTANCE);
      paramsInitial.add(Group.getDefaultList().getId());
      typesInitial.add(StringType.INSTANCE);
      paramsInitial.add(groupIdOfUsersToExclude);
    }

    
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
   * retrieve member objects and it doesn't need to be in a provisionable group. It's for either entities or memberships to delete.
   * @param ids - list of member uuids
   * @return
   */
  public List<ProvisioningEntity> retrieveMembersNonProvisionable(Collection<String> ids) {

    if (this.grouperProvisioner == null) {
      throw new RuntimeException("grouperProvisioner is not set");
    }
    
    if (GrouperUtil.length(ids) == 0) {
      throw new RuntimeException("Must pass in member ids");
    }

    List<ProvisioningEntity> results = new ArrayList<ProvisioningEntity>();
    
    StringBuilder sqlInitial = null;
    List<Object> paramsInitial = new ArrayList<Object>();
    List<Type> typesInitial = new ArrayList<Type>();
    
    sqlInitial = new StringBuilder("select " + 
        "    gm.id, " +
        "    gm.subject_source, " + 
        "    gm.subject_id, " + 
        "    gm.subject_identifier0, " + 
        "    gm.name, " + 
        "    gm.description, " + 
        "    gsm.metadata_json, " +
        "    gm.email0, " +
        "    gm.subject_identifier1, " +
        "    gm.subject_identifier2, " +
        "    gm.id_index, " +
        "    gm.subject_resolution_resolvable " +
        "from " + 
        "    grouper_members gm  " +      
        "    left join grouper_sync_member gsm on  gsm.member_id = gm.id " + 
        "where " +
        "    gsm.grouper_sync_id = ? " + 
        "    and gm.subject_resolution_deleted='F' ");
    paramsInitial.add(this.grouperProvisioner.getGcGrouperSync().getId());
    typesInitial.add(StringType.INSTANCE);
    
    List<String[]> queryResults = null;
  
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
    
    String groupIdOfUsersToProvision = this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getGroupIdOfUsersToProvision();
    boolean restrictUsersByGroupId = !StringUtils.isBlank(groupIdOfUsersToProvision);

    if (restrictUsersByGroupId) {
      if (groupIdOfUsersToProvision.contains(":")) {
        groupIdOfUsersToProvision = GroupFinder.findByName(GrouperSession.staticGrouperSession(), groupIdOfUsersToProvision, true).getId();
      } else {
        groupIdOfUsersToProvision = GroupFinder.findByUuid(GrouperSession.staticGrouperSession(), groupIdOfUsersToProvision, true).getId();
      }
    }

    String groupIdOfUsersToExclude = this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getGroupIdOfUsersNotToProvision();
    boolean excludeUsersByGroupId = !StringUtils.isBlank(groupIdOfUsersToExclude);

    if (excludeUsersByGroupId) {
      if (groupIdOfUsersToExclude.contains(":")) {
        groupIdOfUsersToExclude = GroupFinder.findByName(GrouperSession.staticGrouperSession(), groupIdOfUsersToExclude, true).getId();
      } else {
        groupIdOfUsersToExclude = GroupFinder.findByUuid(GrouperSession.staticGrouperSession(), groupIdOfUsersToExclude, true).getId();
      }
    }

    StringBuilder sqlInitial = new StringBuilder(" from " + 
        "    grouper_groups gg, " +
        "    grouper_members gm, " + 
        "    grouper_memberships ms, " +
        "    grouper_group_set gs, " +
        "    grouper_sync_group gsg " +
        (restrictUsersByGroupId ? ", grouper_memberships gmship_to_provision, grouper_group_set gs_to_provision " : "") +
        "where " +
        "    gsg.grouper_sync_id = ? " +
        "    and ms.owner_id = gs.member_id " +
        "    and ms.field_id = gs.member_field_id " +
        "    and gs.owner_group_id = gg.id " +
        "    and ms.member_id = gm.id " +
        "    and gg.id = gsg.group_id " + 
        "    and gsg.provisionable = 'T' " +
        "    and ms.enabled='T' " +
        "    and gm.subject_resolution_deleted='F' " +
        (restrictUsersByGroupId ? (" and gmship_to_provision.owner_id = gs_to_provision.member_id " +
            " and gmship_to_provision.enabled='T' " +
            " and gmship_to_provision.field_id = gs_to_provision.member_field_id " +
            " and gs_to_provision.field_id = ? " +
            " and gmship_to_provision.member_id = gm.id " +
            " and gs_to_provision.owner_group_id = ? "): ""));

    
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
    paramsInitial.add(this.grouperProvisioner.getGcGrouperSync().getId());

    if (restrictUsersByGroupId) {
      paramsInitial.add(Group.getDefaultList().getId());
      paramsInitial.add(groupIdOfUsersToProvision);
    }

    paramsInitial.addAll(subjectSources);
    paramsInitial.addAll(fieldIds);
    
    List<Type> typesInitial = new ArrayList<Type>();
    typesInitial.add(StringType.INSTANCE);

    if (restrictUsersByGroupId) {
      typesInitial.add(StringType.INSTANCE);
      typesInitial.add(StringType.INSTANCE);
    }

    for (int j = 0; j < (GrouperUtil.length(subjectSources) + GrouperUtil.length(fieldIds)); j++) {
      typesInitial.add(StringType.INSTANCE);
    }
    
    if (GrouperUtil.length(subjectSources) > 0) {
      sqlInitial.append(" and gm.subject_source in (");
      sqlInitial.append(HibUtils.convertToInClauseForSqlStatic(subjectSources));
      sqlInitial.append(") ");
    }
    
    sqlInitial.append(" and gs.field_id in (");
    sqlInitial.append(HibUtils.convertToInClauseForSqlStatic(fieldIds));
    sqlInitial.append(") ");
    
    if (excludeUsersByGroupId) {
      sqlInitial.append(" and not exists (select 1 from grouper_memberships gmship_to_exclude, grouper_group_set gs_to_exclude " +
            " where gmship_to_exclude.owner_id = gs_to_exclude.member_id " +
            " and gmship_to_exclude.enabled='T' " +
            " and gmship_to_exclude.field_id = gs_to_exclude.member_field_id " +
            " and gs_to_exclude.field_id = ? " +
            " and gmship_to_exclude.member_id = gm.id " +
            " and gs_to_exclude.owner_group_id = ? ) ");
      typesInitial.add(StringType.INSTANCE);
      paramsInitial.add(Group.getDefaultList().getId());
      typesInitial.add(StringType.INSTANCE);
      paramsInitial.add(groupIdOfUsersToExclude);
    }
    
    if (retrieveAll) {
      
      String theSql = sqlInitial.toString();
      
      String selectMemberships = "select " + 
      GrouperDdlUtils.sqlConcatenation("ms.id", "gs.id", Membership.membershipIdSeparator) + " as membership_id, gg.id, gm.id ";
      
      String theSqlMemberships = selectMemberships + theSql;
      
      List<String[]> queryResultsMemberships = HibernateSession.bySqlStatic().listSelect(String[].class, theSqlMemberships, paramsInitial, typesInitial);
  
      List<ProvisioningMembership> grouperProvisioningMemberships = getProvisioningMembershipMapFromQueryResultsFull(queryResultsMemberships);
      
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
      
      sqlInitial.insert(0, "select " + 
              GrouperDdlUtils.sqlConcatenation("ms.id", "gs.id", Membership.membershipIdSeparator) + " as membership_id, " +
              "    gg.id, " + 
              "    gm.id, " + 
              "    gm.subject_id, " + 
              "    gm.subject_source, " + 
              "    gm.subject_identifier0, " + 
              "    gm.name, " +
              "    gm.description, " +
              "    gg.name, " + 
              "    gg.display_name, " +
              "    gg.description, " + 
              "    gg.id_index, " + 
              "    gm.subject_identifier1, " + 
              "    gm.subject_identifier2, " + 
              "    gm.id_index, " + 
              "    gm.subject_resolution_resolvable ");
      
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
        List<String[]> queryResults = HibernateSession.bySqlStatic().listSelect(String[].class, sql.toString(), paramsCurrent, typesCurrent);
        
        List<ProvisioningMembership> provisioningMembershipMapFromQueryResults = getProvisioningMembershipMapFromQueryResultsIncremental(queryResults);
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
  public void processWrappers(GrouperProvisioningLists grouperProvisioningObjects) {
    
    Map<String, ProvisioningGroupWrapper> groupUuidToProvisioningGroupWrapper
      = this.getGrouperProvisioner().retrieveGrouperProvisioningDataIndex().getGroupUuidToProvisioningGroupWrapper();

    // add wrappers for all groups
    for (ProvisioningGroup provisioningGroup : GrouperUtil.nonNull(grouperProvisioningObjects.getProvisioningGroups())) {
      ProvisioningGroupWrapper provisioningGroupWrapper = groupUuidToProvisioningGroupWrapper.get(provisioningGroup.getId());
      if (provisioningGroupWrapper == null) {
        provisioningGroupWrapper = new ProvisioningGroupWrapper();
        provisioningGroupWrapper.setGrouperProvisioner(this.grouperProvisioner);
        provisioningGroupWrapper.setGrouperProvisioningGroup(provisioningGroup);
        this.getGrouperProvisioner().retrieveGrouperProvisioningData().addAndIndexGroupWrapper(provisioningGroupWrapper);
      } else {
        provisioningGroupWrapper.setGrouperProvisioningGroup(provisioningGroup);
      }
    }
    
    Map<String, ProvisioningEntityWrapper> memberUuidToProvisioningEntityWrapper
      = this.getGrouperProvisioner().retrieveGrouperProvisioningDataIndex().getMemberUuidToProvisioningEntityWrapper();

    // add wrappers for all entities
    for (ProvisioningEntity provisioningEntity : GrouperUtil.nonNull(grouperProvisioningObjects.getProvisioningEntities())) {
      ProvisioningEntityWrapper provisioningEntityWrapper = memberUuidToProvisioningEntityWrapper.get(provisioningEntity.getId());
      if (provisioningEntityWrapper == null) {
        provisioningEntityWrapper = new ProvisioningEntityWrapper();
        provisioningEntityWrapper.setGrouperProvisioner(this.grouperProvisioner);
        provisioningEntityWrapper.setGrouperProvisioningEntity(provisioningEntity);
        this.getGrouperProvisioner().retrieveGrouperProvisioningData().addAndIndexEntityWrapper(provisioningEntityWrapper);
      } else {
        provisioningEntityWrapper.setGrouperProvisioningEntity(provisioningEntity);
      }
      provisioningEntityWrapper.getProvisioningStateEntity().setUnresolvable(GrouperUtil.booleanValue(provisioningEntity.retrieveAttributeValueBoolean("grouperSubjectUnresolvable"), false));
    }

    Map<MultiKey, ProvisioningMembershipWrapper> groupUuidMemberUuidToProvisioningMembershipWrapper
      = this.getGrouperProvisioner().retrieveGrouperProvisioningDataIndex().getGroupUuidMemberUuidToProvisioningMembershipWrapper();

//    GrouperProvisioningDataIncrementalInput retrieveGrouperProvisioningDataIncrementalInput = grouperProvisioner.retrieveGrouperProvisioningDataIncrementalInput();
//    Set<String> groupUuidsToRetrieveMemberships = null;
//    Set<String> memberUuidsToRetrieveMemberships = null;
//    if (retrieveGrouperProvisioningDataIncrementalInput != null) {
//      groupUuidsToRetrieveMemberships = retrieveGrouperProvisioningDataIncrementalInput.getGroupUuidsToRetrieveMemberships();
//      memberUuidsToRetrieveMemberships = retrieveGrouperProvisioningDataIncrementalInput.getMemberUuidsToRetrieveMemberships();
//    }
//    groupUuidsToRetrieveMemberships = GrouperUtil.nonNull(groupUuidsToRetrieveMemberships);
//    memberUuidsToRetrieveMemberships = GrouperUtil.nonNull(memberUuidsToRetrieveMemberships);
    
    // add wrappers for memberships
    for (ProvisioningMembership provisioningMembership : GrouperUtil.nonNull(grouperProvisioningObjects.getProvisioningMemberships())) {

      MultiKey groupIdMemberId = new MultiKey(provisioningMembership.getProvisioningGroupId(), provisioningMembership.getProvisioningEntityId());
      ProvisioningMembershipWrapper provisioningMembershipWrapper = groupUuidMemberUuidToProvisioningMembershipWrapper.get(groupIdMemberId);
      if (provisioningMembershipWrapper == null) {
        provisioningMembershipWrapper = new ProvisioningMembershipWrapper();
        provisioningMembershipWrapper.setGrouperProvisioner(this.grouperProvisioner);
        provisioningMembershipWrapper.setGrouperProvisioningMembership(provisioningMembership);
        this.getGrouperProvisioner().retrieveGrouperProvisioningData().addAndIndexMembershipWrapper(provisioningMembershipWrapper);
      } else {
        provisioningMembershipWrapper.setGrouperProvisioningMembership(provisioningMembership);
      }
//      // if the group retrieved all memberships incrementally, then mark the memberships as recalc
//      if (!StringUtils.isBlank(provisioningMembership.getProvisioningGroupId()) 
//          && groupUuidsToRetrieveMemberships.contains(provisioningMembership.getProvisioningGroupId())) {
//        provisioningMembershipWrapper.setRecalcObject(true);
//      }
//      if (!StringUtils.isBlank(provisioningMembership.getProvisioningEntityId()) 
//          && groupUuidsToRetrieveMemberships.contains(provisioningMembership.getProvisioningEntityId())) {
//        provisioningMembershipWrapper.setRecalcObject(true);
//      }
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
            if (provisioningMembershipWrapper.getGroupIdMemberId() != null) {
              String groupId  = (String)provisioningMembershipWrapper.getGroupIdMemberId().getKey(0);
              provisioningGroupWrapper.setGroupId(groupId);
            }
            provisioningGroupWrapper.setGrouperProvisioner(this.grouperProvisioner);
            this.grouperProvisioner.retrieveGrouperProvisioningData().addAndIndexGroupWrapper(provisioningGroupWrapper);
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
            if (provisioningMembershipWrapper.getGroupIdMemberId() != null) {
              String memberId = (String) provisioningMembershipWrapper.getGroupIdMemberId().getKey(1);
              provisioningEntityWrapper.setMemberId(memberId);
            }
            provisioningEntityWrapper.setGrouperProvisioner(this.grouperProvisioner);
            
            this.grouperProvisioner.retrieveGrouperProvisioningData().addAndIndexEntityWrapper(provisioningEntityWrapper);
          }
          // all the data is in the membership query
          provisioningEntityWrapper.setGrouperProvisioningEntity(provisioningMembership.getProvisioningEntity());
        } else {
          provisioningMembership.setProvisioningEntity(grouperProvisioningEntity);
        }
        
      }
    }
    if (missingGrouperProvisioningMembershipReferencesCount > 0) {
      GrouperUtil.mapAddValue(debugMap, "missingGrouperProvisioningMembershipReferencesCount", missingGrouperProvisioningMembershipReferencesCount);
    }
  }
  
  /**
   * If there is no match to a grouper membership, see if we can match the group or entity
   */
  public void fixGrouperTargetMembershipReferences() {
    
    Map<String, Object> debugMap = this.getGrouperProvisioner().getDebugMap();
    
    int missingTargetMembershipReferencesFixedCount = 0;

    // index the new target groups by matching id, and pick the best one if multiple
    Map<String, ProvisioningGroupWrapper> groupTargetMatchingAttributeToGroupWrapper = new HashMap<>();
    
    boolean indexesInitialized = false;
    
    Map<String, ProvisioningEntityWrapper> entityTargetMatchingAttributeToEntityWrapper = new HashMap<>();

    // add wrappers for groups
    for (ProvisioningMembershipWrapper provisioningMembershipWrapper : this.grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningMembershipWrappers()) {

      ProvisioningMembership targetProvisioningMembership = provisioningMembershipWrapper.getTargetProvisioningMembership();

      // only if there is a target and it is not matched
      if (targetProvisioningMembership == null || provisioningMembershipWrapper.getGrouperTargetMembership() != null) {
        continue;
      }

      if (!indexesInitialized) {
        String membershipGroupMatchingIdAttribute = this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getMembershipGroupMatchingIdGrouperAttribute();
        
        if (!StringUtils.isBlank(membershipGroupMatchingIdAttribute)) {
          for (ProvisioningGroup grouperProvisioningGroup : GrouperUtil.nonNull(this.getGrouperProvisioner().retrieveGrouperProvisioningData().retrieveGrouperProvisioningGroups())) {
            
            String value = GrouperUtil.stringValue(this.getGrouperProvisioner().retrieveGrouperProvisioningTranslator()
                .translateFromGrouperProvisioningGroupField(grouperProvisioningGroup.getProvisioningGroupWrapper(), membershipGroupMatchingIdAttribute));

            if (!StringUtils.isBlank(value)) {
              groupTargetMatchingAttributeToGroupWrapper.put(value, grouperProvisioningGroup.getProvisioningGroupWrapper());
            }
          }
        }

        String membershipEntityMatchingIdAttribute = this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getMembershipEntityMatchingIdGrouperAttribute();
        
        if (!StringUtils.isBlank(membershipEntityMatchingIdAttribute)) {
          for (ProvisioningEntity grouperProvisioningEntity : GrouperUtil.nonNull(this.getGrouperProvisioner().retrieveGrouperProvisioningData().retrieveGrouperProvisioningEntities())) {
            
            String value = GrouperUtil.stringValue(this.getGrouperProvisioner().retrieveGrouperProvisioningTranslator()
                .translateFromGrouperProvisioningEntityField(grouperProvisioningEntity.getProvisioningEntityWrapper(), membershipEntityMatchingIdAttribute));

            if (!StringUtils.isBlank(value)) {
              entityTargetMatchingAttributeToEntityWrapper.put(value, grouperProvisioningEntity.getProvisioningEntityWrapper());
            }
          }
        }
        
        indexesInitialized = true;
      }
      // if the matching id is a multikey with two parts (group and entity)
      for (ProvisioningUpdatableAttributeAndValue provisioningUpdatableAttributeAndValue : GrouperUtil.nonNull(targetProvisioningMembership.getMatchingIdAttributeNameToValues())) {
        Object attributeValue = provisioningUpdatableAttributeAndValue.getAttributeValue();
        if (attributeValue instanceof MultiKey) {
          MultiKey attributeValueMultiKey = (MultiKey)attributeValue;
          if (attributeValueMultiKey.size() == 2) {
            String groupAttributeValue = GrouperUtil.stringValue(attributeValueMultiKey.getKey(0));
            String entityAttributeValue = GrouperUtil.stringValue(attributeValueMultiKey.getKey(1));
            
            ProvisioningGroupWrapper provisioningGroupWrapper = groupTargetMatchingAttributeToGroupWrapper.get(groupAttributeValue);
            ProvisioningEntityWrapper provisioningEntityWrapper = entityTargetMatchingAttributeToEntityWrapper.get(entityAttributeValue);

            if (provisioningGroupWrapper != null || provisioningEntityWrapper != null) {
              missingTargetMembershipReferencesFixedCount++;
              MultiKey groupIdMemberId = new MultiKey(new Object[] {
                  provisioningGroupWrapper == null ? null : provisioningGroupWrapper.getGroupId(),
                  provisioningEntityWrapper == null ? null : provisioningEntityWrapper.getMemberId()
              });
              provisioningMembershipWrapper.setGroupIdMemberId(groupIdMemberId);
            }
            
          }
        }
      }
      
    }

    if (missingTargetMembershipReferencesFixedCount > 0) {
      GrouperUtil.mapAddValue(debugMap, "missingTargetMembershipReferencesFixedCount", missingTargetMembershipReferencesFixedCount);
    }
  }
  
  private List<ProvisioningGroup> getTargetGroupMapFromQueryResults(List<String[]> queryResults) {
    
    List<GrouperProvisioningObjectMetadataItem> grouperProvisioningObjectMetadataItems = 
        this.grouperProvisioner.retrieveGrouperProvisioningObjectMetadata().getGrouperProvisioningObjectMetadataItems();
    
    
    List<ProvisioningGroup> results = new ArrayList<ProvisioningGroup>();

    for (String[] queryResult : queryResults) {
      String id = queryResult[0];
      String name = queryResult[1];
      String displayName = queryResult[2];
      String description = queryResult[3];
      String idIndex = queryResult[4];
      String jsonMetadata = queryResult[5];
      
      ProvisioningGroup grouperProvisioningGroup = new ProvisioningGroup(true);
      grouperProvisioningGroup.setId(id);
      grouperProvisioningGroup.setName(name);
      grouperProvisioningGroup.setDisplayName(displayName);
      grouperProvisioningGroup.setIdIndex(Long.parseLong(idIndex));
      grouperProvisioningGroup.assignAttributeValue("description", description);
      
      if (GrouperUtil.length(grouperProvisioningObjectMetadataItems) > 0) {
        if (!StringUtils.isBlank(jsonMetadata) && !StringUtils.equals("{}", jsonMetadata)) {
          JsonNode jsonNode = GrouperUtil.jsonJacksonNode(jsonMetadata);
          for (GrouperProvisioningObjectMetadataItem grouperProvisioningObjectMetadataItem : grouperProvisioningObjectMetadataItems) {
            if (grouperProvisioningObjectMetadataItem.isShowForGroup() || grouperProvisioningObjectMetadataItem.isShowForFolder()) {
              
              String metadataItemName = grouperProvisioningObjectMetadataItem.getName();
              if (metadataItemName.startsWith("md_")) {
                if (jsonNode.has(metadataItemName)) {
                  GrouperProvisioningObjectMetadataItemValueType grouperProvisioningObjectMetadataItemValueType = 
                      GrouperUtil.defaultIfNull(grouperProvisioningObjectMetadataItem.getValueType(), GrouperProvisioningObjectMetadataItemValueType.STRING);
                  Object value = GrouperUtil.jsonJacksonGetNode(jsonNode, metadataItemName);
                  grouperProvisioningGroup.assignAttributeValue(metadataItemName, grouperProvisioningObjectMetadataItemValueType.convert(value));
                }
              }
            }
          }
        }
      }
      
      results.add(grouperProvisioningGroup);
    }
    
    return results;
  }
  
  private List<ProvisioningEntity> getProvisioningEntityMapFromQueryResults(List<String[]> queryResults) {
    
    List<GrouperProvisioningObjectMetadataItem> grouperProvisioningObjectMetadataItems = 
        this.grouperProvisioner.retrieveGrouperProvisioningObjectMetadata().getGrouperProvisioningObjectMetadataItems();
    
    List<ProvisioningEntity> results = new ArrayList<ProvisioningEntity>();

    for (String[] queryResult : queryResults) {
      String id = queryResult[0];
      String subjectSource = queryResult[1];
      String subjectId = queryResult[2];
      String subjectIdentifier0 = queryResult[3];
      String name = queryResult[4];
      String description = queryResult[5];
      String jsonMetadata = queryResult[6];
      String email = queryResult[7];
      String subjectIdentifier1 = queryResult[8];
      String subjectIdentifier2 = queryResult[9];
      String idIndex = queryResult[10];
      Boolean subjectResolutionResolvable = GrouperUtil.booleanObjectValue(queryResult[11]);
      
      // check if skipping unresolvable subjects
      
      ProvisioningEntity grouperProvisioningEntity = new ProvisioningEntity(true);
      grouperProvisioningEntity.setId(id);
      grouperProvisioningEntity.setName(name);
      grouperProvisioningEntity.setSubjectId(subjectId);
      grouperProvisioningEntity.setEmail(email);
      grouperProvisioningEntity.setIdIndex(Long.parseLong(idIndex));
      grouperProvisioningEntity.setSubjectResolutionResolvable(subjectResolutionResolvable);
      grouperProvisioningEntity.assignAttributeValue("subjectSourceId", subjectSource);
      grouperProvisioningEntity.assignAttributeValue("description", description);
      grouperProvisioningEntity.assignAttributeValue("subjectIdentifier0", subjectIdentifier0);
      grouperProvisioningEntity.assignAttributeValue("subjectIdentifier1", subjectIdentifier1);
      grouperProvisioningEntity.assignAttributeValue("subjectIdentifier2", subjectIdentifier2);
      if (!subjectResolutionResolvable) {
        grouperProvisioningEntity.assignAttributeValue("grouperSubjectUnresolvable", true);
      }
      
      if (GrouperUtil.length(grouperProvisioningObjectMetadataItems) > 0) {
        if (!StringUtils.isBlank(jsonMetadata) && !StringUtils.equals("{}", jsonMetadata)) {
          JsonNode jsonNode = GrouperUtil.jsonJacksonNode(jsonMetadata);
          for (GrouperProvisioningObjectMetadataItem grouperProvisioningObjectMetadataItem : grouperProvisioningObjectMetadataItems) {
            if (grouperProvisioningObjectMetadataItem.isShowForMember()) {
              
              String metadataItemName = grouperProvisioningObjectMetadataItem.getName();
              if (metadataItemName.startsWith("md_")) {
                if (jsonNode.has(metadataItemName)) {
                  GrouperProvisioningObjectMetadataItemValueType grouperProvisioningObjectMetadataItemValueType = 
                      GrouperUtil.defaultIfNull(grouperProvisioningObjectMetadataItem.getValueType(), GrouperProvisioningObjectMetadataItemValueType.STRING);
                  String value = GrouperUtil.jsonJacksonGetString(jsonNode, metadataItemName);
                  grouperProvisioningEntity.assignAttributeValue(metadataItemName, grouperProvisioningObjectMetadataItemValueType.convert(value));
                }
              }
            }
          }
        }
      }
      
      results.add(grouperProvisioningEntity);
    }
    
    return results;
  }
  
  private List<ProvisioningMembership> getProvisioningMembershipMapFromQueryResultsIncremental(List<String[]> queryResults) {
    
    List<ProvisioningMembership> results = new ArrayList<ProvisioningMembership>();

    int count=0;
    for (String[] queryResult : queryResults) {
      
      // conserve memory
      queryResults.set(count, null);
      count++;

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
      String subjectIdentifier1 = queryResult[12];
      String subjectIdentifier2 = queryResult[13];
      Long memberIdIndex = GrouperUtil.longObjectValue(queryResult[14], false);
      Boolean subjectResolutionResolvable = GrouperUtil.booleanObjectValue(queryResult[15]);
      
      // check if skipping unresolvable subjects
      if (this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().isUnresolvableSubjectsRemove() && !subjectResolutionResolvable) {
        continue;
      }

      ProvisioningMembership grouperProvisioningMembership = new ProvisioningMembership(true);
      grouperProvisioningMembership.setId(membershipId);
      
      {
        ProvisioningEntity targetEntity = new ProvisioningEntity(true);
        targetEntity.setId(memberId);
        targetEntity.setName(name);
        targetEntity.assignAttributeValue("description", description);
        targetEntity.setSubjectId(subjectId);
        targetEntity.setIdIndex(memberIdIndex);
        targetEntity.setSubjectResolutionResolvable(subjectResolutionResolvable);
        targetEntity.assignAttributeValue("subjectSourceId", subjectSourceId);
        targetEntity.assignAttributeValue("subjectIdentifier0", subjectIdentifier0);
        targetEntity.assignAttributeValue("subjectIdentifier1", subjectIdentifier1);
        targetEntity.assignAttributeValue("subjectIdentifier2", subjectIdentifier2);
        
        
        grouperProvisioningMembership.setProvisioningEntity(targetEntity);
        grouperProvisioningMembership.setProvisioningEntityId(memberId);
      }
      {
        ProvisioningGroup targetGroup = new ProvisioningGroup(true);
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
  
  private List<ProvisioningMembership> getProvisioningMembershipMapFromQueryResultsFull(List<String[]> queryResultsMemberships) {
    
    List<ProvisioningMembership> results = new ArrayList<ProvisioningMembership>();

    Map<String, ProvisioningGroupWrapper> groupUuidToProvisioningGroupWrapper = this.getGrouperProvisioner().retrieveGrouperProvisioningDataIndex().getGroupUuidToProvisioningGroupWrapper();
    Map<String, ProvisioningEntityWrapper> memberUuidToProvisioningEntityWrapper = this.getGrouperProvisioner().retrieveGrouperProvisioningDataIndex().getMemberUuidToProvisioningEntityWrapper();
            
    int count=0;
    for (String[] queryResult : queryResultsMemberships) {
      
      // conserve memory
      queryResultsMemberships.set(count, null);
      count++;
      
      String membershipId = queryResult[0];
      String groupId = queryResult[1];
      String memberId = queryResult[2];
      
      ProvisioningGroup targetGroup = groupUuidToProvisioningGroupWrapper.get(groupId) == null ? null : groupUuidToProvisioningGroupWrapper.get(groupId).getGrouperProvisioningGroup();
      ProvisioningEntity targetEntity = memberUuidToProvisioningEntityWrapper.get(memberId) == null ? null : memberUuidToProvisioningEntityWrapper.get(memberId).getGrouperProvisioningEntity();
      
      if (targetGroup == null || targetEntity == null) {
        // skip due to race condition
        continue;
      }

      // check if skipping unresolvable subjects
      if (this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().isUnresolvableSubjectsRemove() && !targetEntity.getSubjectResolutionResolvable()) {
        continue;
      }

      ProvisioningMembership grouperProvisioningMembership = new ProvisioningMembership(true);
      grouperProvisioningMembership.setId(membershipId);
      
      { 
        grouperProvisioningMembership.setProvisioningEntity(targetEntity);
        grouperProvisioningMembership.setProvisioningEntityId(memberId);
      }
      {
        grouperProvisioningMembership.setProvisioningGroup(targetGroup);
        grouperProvisioningMembership.setProvisioningGroupId(groupId);
      }
      
      results.add(grouperProvisioningMembership);
    }
    
    return results;
  }
  
  public GrouperProvisioningLists retrieveGrouperDataFull() {
    Map<String, Object> debugMap = this.getGrouperProvisioner().getDebugMap();

    GrouperProvisioningLists grouperProvisioningObjects = new GrouperProvisioningLists();
    
    {
      long start = System.currentTimeMillis();
      grouperProvisioningObjects.setProvisioningGroups(retrieveGroups(true, null));
      debugMap.put("retrieveGrouperGroupsMillis", System.currentTimeMillis() - start);
      debugMap.put("grouperGroupCount", GrouperUtil.length(grouperProvisioningObjects.getProvisioningGroups()));
    }
    GrouperDaemonUtils.stopProcessingIfJobPaused();

    {
      long start = System.currentTimeMillis();
      grouperProvisioningObjects.setProvisioningEntities(retrieveMembers(true, null));
      debugMap.put("retrieveGrouperEntitiesMillis", System.currentTimeMillis() - start);
      debugMap.put("grouperEntityCount", GrouperUtil.length(grouperProvisioningObjects.getProvisioningEntities()));
    }
    GrouperDaemonUtils.stopProcessingIfJobPaused();
    
    // call wrappers so that they are available when querying memberships
    this.grouperProvisioner.retrieveGrouperDao().processWrappers(grouperProvisioningObjects);

    {
      long start = System.currentTimeMillis();
      grouperProvisioningObjects.setProvisioningMemberships(retrieveMemberships(true, null, null, null));
      debugMap.put("retrieveGrouperMshipsMillis", System.currentTimeMillis() - start);
      debugMap.put("grouperMshipCount", GrouperUtil.length(grouperProvisioningObjects.getProvisioningMemberships()));
    }
    GrouperDaemonUtils.stopProcessingIfJobPaused();

    return grouperProvisioningObjects;
  }

  
  public GrouperProvisioningLists retrieveGrouperDataIncrementalGroupsEntities(String logLabel) {

    GrouperProvisioningLists grouperProvisioningLists = new GrouperProvisioningLists();
    
    Map<String, Object> debugMap = this.getGrouperProvisioner().getDebugMap();

    Set<String> groupIdsToRetrieve = new HashSet<String>();
    Set<String> memberIdsToRetrieve = new HashSet<String>();
    GrouperProvisioningData grouperProvisioningData = this.getGrouperProvisioner().retrieveGrouperProvisioningData();
    {
      long start = System.currentTimeMillis();
      
      //go from the actions that happened to what we need to retrieve from Grouper
      //retrieve everything whether recalc or not
      for (ProvisioningGroupWrapper provisioningGroupWrapper : this.getGrouperProvisioner().retrieveGrouperProvisioningData().getProvisioningGroupWrappers()) {
        // for incremental pass 2, when we are retrieving groups that have not been retrieved yet.
        if (!grouperProvisioningData.getGroupIdsSelectedFromGrouper().contains(provisioningGroupWrapper.getGroupId())) {
          groupIdsToRetrieve.add(provisioningGroupWrapper.getGroupId());
        }
      }
      for (ProvisioningEntityWrapper provisioningEntityWrapper : this.getGrouperProvisioner().retrieveGrouperProvisioningData().getProvisioningEntityWrappers()) {
        // for incremental pass 2, when we are retrieving entities that have not been retrieved yet.
        if (!grouperProvisioningData.getMemberIdsSelectedFromGrouper().contains(provisioningEntityWrapper.getMemberId())) {
          memberIdsToRetrieve.add(provisioningEntityWrapper.getMemberId());
        }
        
      }
      
      
      for (ProvisioningMembershipWrapper provisioningMembershipWrapper : this.getGrouperProvisioner().retrieveGrouperProvisioningData().getProvisioningMembershipWrappers()) {
        if (provisioningMembershipWrapper.getGroupIdMemberId() != null) {
          String groupId = (String)provisioningMembershipWrapper.getGroupIdMemberId().getKey(0);
          String memberId = (String)provisioningMembershipWrapper.getGroupIdMemberId().getKey(1);
          if (!grouperProvisioningData.getGroupIdsSelectedFromGrouper().contains(groupId)) {
            groupIdsToRetrieve.add(groupId);
          }
          
          if (!grouperProvisioningData.getMemberIdsSelectedFromGrouper().contains(memberId)) {
            memberIdsToRetrieve.add(memberId);
          }
          
        }
      }

    }
    {
      long start = System.currentTimeMillis();
      grouperProvisioningLists.setProvisioningGroups(retrieveGroups(false, groupIdsToRetrieve));
      GrouperDaemonUtils.stopProcessingIfJobPaused();

      grouperProvisioningData.getGroupIdsSelectedFromGrouper().addAll(groupIdsToRetrieve);
      debugMap.put("retrieveGrouperGroupsMillis_"+logLabel, System.currentTimeMillis() - start);
      debugMap.put("grouperGroupCount_"+logLabel, GrouperUtil.length(grouperProvisioningLists.getProvisioningGroups()));
    }
    {
      long start = System.currentTimeMillis();
      grouperProvisioningLists.setProvisioningEntities(retrieveMembers(false, memberIdsToRetrieve));
      GrouperDaemonUtils.stopProcessingIfJobPaused();

      grouperProvisioningData.getMemberIdsSelectedFromGrouper().addAll(memberIdsToRetrieve);
      debugMap.put("retrieveGrouperEntitiesMillis_"+logLabel, System.currentTimeMillis() - start);
      debugMap.put("grouperEntityCount_"+logLabel, GrouperUtil.length(grouperProvisioningLists.getProvisioningEntities()));
    }
    return grouperProvisioningLists;
  }

  public GrouperProvisioningLists retrieveGrouperDataIncrementalMemberships() {

    GrouperProvisioningLists grouperProvisioningLists = new GrouperProvisioningLists();
    
    Map<String, Object> debugMap = this.getGrouperProvisioner().getDebugMap();

    Set<String> groupIdsToRetrieve = new HashSet<String>();
    Set<String> memberIdsToRetrieve = new HashSet<String>();

    {
      long start = System.currentTimeMillis();
      
      Set<String> groupIdsToRetrieveForMemberships = new HashSet<String>();
      Set<String> memberIdsToRetrieveForMemberships = new HashSet<String>();
      Set<MultiKey> groupIdsMemberIdsToRetrieveForMemberships = new HashSet<MultiKey>();
      
      //go from the actions that happened to what we need to retrieve from Grouper
      //retrieve everything whether recalc or not
      for (ProvisioningGroupWrapper provisioningGroupWrapper : this.getGrouperProvisioner().retrieveGrouperProvisioningData().getProvisioningGroupWrappers()) {
        if (provisioningGroupWrapper.getProvisioningStateGroup().isRecalcGroupMemberships()) {
          groupIdsToRetrieveForMemberships.add(provisioningGroupWrapper.getGroupId());
        }
      }
      for (ProvisioningEntityWrapper provisioningEntityWrapper : this.getGrouperProvisioner().retrieveGrouperProvisioningData().getProvisioningEntityWrappers()) {
        if (provisioningEntityWrapper.getProvisioningStateEntity().isRecalcEntityMemberships()) {
          memberIdsToRetrieveForMemberships.add(provisioningEntityWrapper.getMemberId());
        }
      }
      for (ProvisioningMembershipWrapper provisioningMembershipWrapper : this.getGrouperProvisioner().retrieveGrouperProvisioningData().getProvisioningMembershipWrappers()) {
        if (provisioningMembershipWrapper.getGroupIdMemberId() != null) {
          groupIdsMemberIdsToRetrieveForMemberships.add(provisioningMembershipWrapper.getGroupIdMemberId());
        }
      }

      grouperProvisioningLists.setProvisioningMemberships(retrieveMemberships(false, groupIdsToRetrieveForMemberships, 
          memberIdsToRetrieveForMemberships, groupIdsMemberIdsToRetrieveForMemberships));
      
      debugMap.put("retrieveGrouperMshipsMillis", System.currentTimeMillis() - start);
      debugMap.put("grouperMshipCount", GrouperUtil.length(grouperProvisioningLists.getProvisioningMemberships()));
      GrouperDaemonUtils.stopProcessingIfJobPaused();

      // maybe more data came back?  fill in some pieces.  this is for instance if retrieving memberships for group or entity, get those that are retruned if not already there
      for (ProvisioningMembership provisioningMembership : grouperProvisioningLists.getProvisioningMemberships()) {
        {
          String groupId = provisioningMembership.getProvisioningGroupId();
          boolean retrieveGroup = true;
          ProvisioningGroupWrapper provisioningGroupWrapper = this.getGrouperProvisioner().retrieveGrouperProvisioningDataIndex().getGroupUuidToProvisioningGroupWrapper().get(groupId);
          
          // see if weve already got it
          if (provisioningGroupWrapper != null) {
            if (provisioningGroupWrapper.getGrouperProvisioningGroup() != null) {
              retrieveGroup = false;
            }
          }
          if (retrieveGroup) {
            groupIdsToRetrieve.add(groupId);
          }
        }
        {
          String memberId = provisioningMembership.getProvisioningEntityId();
          boolean retrieveEntity = true;
          ProvisioningEntityWrapper provisioningEntityWrapper = this.getGrouperProvisioner().retrieveGrouperProvisioningDataIndex().getMemberUuidToProvisioningEntityWrapper().get(memberId);
          
          // see if weve already got it
          if (provisioningEntityWrapper != null) {
            if (provisioningEntityWrapper.getGrouperProvisioningEntity() != null) {
              retrieveEntity = false;
            }
          }
          if (retrieveEntity) {
            memberIdsToRetrieve.add(memberId);
          }
        }
      }
      
    }
    {
      long start = System.currentTimeMillis();
      grouperProvisioningLists.setProvisioningGroups(retrieveGroups(false, groupIdsToRetrieve));
      debugMap.put("retrieveGrouperGroups2Millis", System.currentTimeMillis() - start);
      debugMap.put("grouperGroup2Count", GrouperUtil.length(grouperProvisioningLists.getProvisioningGroups()));
      GrouperDaemonUtils.stopProcessingIfJobPaused();
    }
    {
      long start = System.currentTimeMillis();
      grouperProvisioningLists.setProvisioningEntities(retrieveMembers(false, memberIdsToRetrieve));
      debugMap.put("retrieveGrouperEntities2Millis", System.currentTimeMillis() - start);
      debugMap.put("grouperEntity2Count", GrouperUtil.length(grouperProvisioningLists.getProvisioningEntities()));
      GrouperDaemonUtils.stopProcessingIfJobPaused();
    }
    return grouperProvisioningLists;
  }

  /**
   * get provisioning attributes for all folders
   * @return the attributes
   */
  public Map<String, GrouperProvisioningObjectAttributes> retrieveAllProvisioningFolderAttributes() {

    if (this.grouperProvisioner == null) {
      throw new RuntimeException("grouperProvisioner is not set");
    }
      
    Map<String, GrouperProvisioningObjectAttributes> results = new HashMap<String, GrouperProvisioningObjectAttributes>();

    {
      String sql = "SELECT " + 
          "    gaa_marker.id, " +
          "    gs.id, " + 
          "    gs.name, " + 
          "    gs.id_index, " +
          "    gadn_config.name, " + 
          "    gaav_config.value_string " + 
          "FROM " + 
          "    grouper_stems gs, " + 
          "    grouper_attribute_assign gaa_marker, " + 
          "    grouper_attribute_assign gaa_target, " + 
          "    grouper_attribute_assign gaa_config, " + 
          "    grouper_attribute_assign_value gaav_target, " + 
          "    grouper_attribute_assign_value gaav_config, " + 
          "    grouper_attribute_def_name gadn_marker, " + 
          "    grouper_attribute_def_name gadn_target, " + 
          "    grouper_attribute_def_name gadn_config " + 
          "WHERE " + 
          "    gs.id = gaa_marker.owner_stem_id " + 
          "    AND gaa_marker.attribute_def_name_id = gadn_marker.id " + 
          "    AND gadn_marker.name = ? " + 
          "    AND gaa_marker.id = gaa_target.owner_attribute_assign_id " + 
          "    AND gaa_target.attribute_def_name_id = gadn_target.id " + 
          "    AND gadn_target.name = ? " + 
          "    AND gaav_target.attribute_assign_id = gaa_target.id " + 
          "    AND gaav_target.value_string = ? " + 
          "    AND gaa_marker.id = gaa_config.owner_attribute_assign_id " + 
          "    AND gaav_config.attribute_assign_id = gaa_config.id " + 
          "    AND gadn_config.id = gaa_config.attribute_def_name_id " + 
          "    AND gaa_marker.enabled = 'T' " + 
          "    AND gaa_target.enabled = 'T' " + 
          "    AND gaa_config.enabled = 'T' ";
      
      List<Object> paramsInitial = new ArrayList<Object>();
      paramsInitial.add(GrouperProvisioningSettings.provisioningConfigStemName()+":"+GrouperProvisioningAttributeNames.PROVISIONING_ATTRIBUTE_NAME);
      paramsInitial.add(GrouperProvisioningSettings.provisioningConfigStemName()+":"+GrouperProvisioningAttributeNames.PROVISIONING_TARGET);
      paramsInitial.add(this.grouperProvisioner.getConfigId());
  
      List<Type> typesInitial = new ArrayList<Type>();
      typesInitial.add(StringType.INSTANCE);
      typesInitial.add(StringType.INSTANCE);
      typesInitial.add(StringType.INSTANCE);
  
      List<String[]> queryResults = HibernateSession.bySqlStatic().listSelect(String[].class, sql, paramsInitial, typesInitial);
      for (String[] queryResult : queryResults) {
        String markerAttributeAssignId = queryResult[0];
        String stemId = queryResult[1];
        String stemName = queryResult[2];
        Long idIndex = Long.parseLong(queryResult[3]);
        String configName = queryResult[4];
        String configValue = queryResult[5];
        
        if (results.get(stemName) == null) {
          results.put(stemName, new GrouperProvisioningObjectAttributes(stemId, stemName, idIndex, markerAttributeAssignId));
          results.get(stemName).setOwnedByStem(true);
        }
        
        if (configName.equals(GrouperProvisioningSettings.provisioningConfigStemName()+":"+GrouperProvisioningAttributeNames.PROVISIONING_DIRECT_ASSIGNMENT)) {
          results.get(stemName).setProvisioningDirectAssign(configValue);
        } else if (configName.equals(GrouperProvisioningSettings.provisioningConfigStemName()+":"+GrouperProvisioningAttributeNames.PROVISIONING_DO_PROVISION)) {
          results.get(stemName).setProvisioningDoProvision(configValue);
        } else if (configName.equals(GrouperProvisioningSettings.provisioningConfigStemName()+":"+GrouperProvisioningAttributeNames.PROVISIONING_METADATA_JSON)) {
          results.get(stemName).setProvisioningMetadataJson(configValue);
        } else if (configName.equals(GrouperProvisioningSettings.provisioningConfigStemName()+":"+GrouperProvisioningAttributeNames.PROVISIONING_OWNER_STEM_ID)) {
          results.get(stemName).setProvisioningOwnerStemId(configValue);
        } else if (configName.equals(GrouperProvisioningSettings.provisioningConfigStemName()+":"+GrouperProvisioningAttributeNames.PROVISIONING_STEM_SCOPE)) {
          results.get(stemName).setProvisioningStemScope(configValue);
        } else if (configName.equals(GrouperProvisioningSettings.provisioningConfigStemName()+":"+GrouperProvisioningAttributeNames.PROVISIONING_TARGET)) {
          results.get(stemName).setProvisioningTarget(configValue);
        }
      }
    }
    
    // now see if there are any other folders to add to the map that don't have attributes but are under a parent folder that has a direct assign
    // they won't necessarily need attributes but they'll need to be checked.

    {
      String sql = "SELECT distinct " + 
          "    gs_if_has_stem.id, " + 
          "    gs_if_has_stem.name, " +
          "    gs_if_has_stem.id_index " +
          "FROM " + 
          "    grouper_stems gs, " + 
          "    grouper_attribute_assign gaa_marker, " + 
          "    grouper_attribute_assign gaa_target, " + 
          "    grouper_attribute_assign gaa_direct, " + 
          "    grouper_attribute_assign_value gaav_target, " + 
          "    grouper_attribute_assign_value gaav_direct, " + 
          "    grouper_attribute_def_name gadn_marker, " + 
          "    grouper_attribute_def_name gadn_target, " + 
          "    grouper_attribute_def_name gadn_direct, " + 
          "    grouper_stem_set gss, " + 
          "    grouper_stems gs_if_has_stem " + 
          "WHERE " + 
          "    gs.id = gaa_marker.owner_stem_id " + 
          "    AND gaa_marker.attribute_def_name_id = gadn_marker.id " + 
          "    AND gadn_marker.name = ? " + 
          "    AND gaa_marker.id = gaa_target.owner_attribute_assign_id " + 
          "    AND gaa_target.attribute_def_name_id = gadn_target.id " + 
          "    AND gadn_target.name = ? " + 
          "    AND gaav_target.attribute_assign_id = gaa_target.id " + 
          "    AND gaav_target.value_string = ? " + 
          "    AND gaa_marker.id = gaa_direct.owner_attribute_assign_id " + 
          "    AND gaa_direct.attribute_def_name_id = gadn_direct.id " + 
          "    AND gadn_direct.name = ? " + 
          "    AND gaav_direct.attribute_assign_id = gaa_direct.id " + 
          "    AND gaav_direct.value_string = 'true' " + 
          "    AND gs.id = gss.then_has_stem_id " + 
          "    AND gss.if_has_stem_id = gs_if_has_stem.id " + 
          "    AND gaa_marker.enabled = 'T' " + 
          "    AND gaa_target.enabled = 'T' " + 
          "    AND gaa_direct.enabled = 'T' ";
      
      List<Object> paramsInitial = new ArrayList<Object>();
      paramsInitial.add(GrouperProvisioningSettings.provisioningConfigStemName()+":"+GrouperProvisioningAttributeNames.PROVISIONING_ATTRIBUTE_NAME);
      paramsInitial.add(GrouperProvisioningSettings.provisioningConfigStemName()+":"+GrouperProvisioningAttributeNames.PROVISIONING_TARGET);
      paramsInitial.add(this.grouperProvisioner.getConfigId());
      paramsInitial.add(GrouperProvisioningSettings.provisioningConfigStemName()+":"+GrouperProvisioningAttributeNames.PROVISIONING_DIRECT_ASSIGNMENT);
  
      List<Type> typesInitial = new ArrayList<Type>();
      typesInitial.add(StringType.INSTANCE);
      typesInitial.add(StringType.INSTANCE);
      typesInitial.add(StringType.INSTANCE);
      typesInitial.add(StringType.INSTANCE);
  
      List<String[]> queryResults = HibernateSession.bySqlStatic().listSelect(String[].class, sql, paramsInitial, typesInitial);
      for (String[] queryResult : queryResults) {
        String stemId = queryResult[0];
        String stemName = queryResult[1];
        Long idIndex = Long.parseLong(queryResult[2]);

        if (results.get(stemName) == null) {
          results.put(stemName, new GrouperProvisioningObjectAttributes(stemId, stemName, idIndex, null));
          results.get(stemName).setOwnedByStem(true);
        }
      }
    }
    
    return results;
  }
  
  /**
   * get provisioning attributes for all groups
   * @return the attributes
   */
  public Map<String, GrouperProvisioningObjectAttributes> retrieveAllProvisioningGroupAttributes() {

    if (this.grouperProvisioner == null) {
      throw new RuntimeException("grouperProvisioner is not set");
    }
      
    Map<String, GrouperProvisioningObjectAttributes> results = new HashMap<String, GrouperProvisioningObjectAttributes>();

    {
      String sql = "SELECT " + 
          "    gaa_marker.id, " +
          "    gg.id, " + 
          "    gg.name, " + 
          "    gg.id_index, " +
          "    gadn_config.name, " + 
          "    gaav_config.value_string " + 
          "FROM " + 
          "    grouper_groups gg, " + 
          "    grouper_attribute_assign gaa_marker, " + 
          "    grouper_attribute_assign gaa_target, " + 
          "    grouper_attribute_assign gaa_config, " + 
          "    grouper_attribute_assign_value gaav_target, " + 
          "    grouper_attribute_assign_value gaav_config, " + 
          "    grouper_attribute_def_name gadn_marker, " + 
          "    grouper_attribute_def_name gadn_target, " + 
          "    grouper_attribute_def_name gadn_config " + 
          "WHERE " + 
          "    gg.id = gaa_marker.owner_group_id " + 
          "    AND gaa_marker.attribute_def_name_id = gadn_marker.id " + 
          "    AND gadn_marker.name = ? " + 
          "    AND gaa_marker.id = gaa_target.owner_attribute_assign_id " + 
          "    AND gaa_target.attribute_def_name_id = gadn_target.id " + 
          "    AND gadn_target.name = ? " + 
          "    AND gaav_target.attribute_assign_id = gaa_target.id " + 
          "    AND gaav_target.value_string = ? " + 
          "    AND gaa_marker.id = gaa_config.owner_attribute_assign_id " + 
          "    AND gaav_config.attribute_assign_id = gaa_config.id " + 
          "    AND gadn_config.id = gaa_config.attribute_def_name_id " + 
          "    AND gg.type_of_group != 'entity' " + 
          "    AND gaa_marker.enabled = 'T' " + 
          "    AND gaa_target.enabled = 'T' " + 
          "    AND gaa_config.enabled = 'T' ";
      
      List<Object> paramsInitial = new ArrayList<Object>();
      paramsInitial.add(GrouperProvisioningSettings.provisioningConfigStemName()+":"+GrouperProvisioningAttributeNames.PROVISIONING_ATTRIBUTE_NAME);
      paramsInitial.add(GrouperProvisioningSettings.provisioningConfigStemName()+":"+GrouperProvisioningAttributeNames.PROVISIONING_TARGET);
      paramsInitial.add(this.grouperProvisioner.getConfigId());
  
      List<Type> typesInitial = new ArrayList<Type>();
      typesInitial.add(StringType.INSTANCE);
      typesInitial.add(StringType.INSTANCE);
      typesInitial.add(StringType.INSTANCE);
  
      List<String[]> queryResults = HibernateSession.bySqlStatic().listSelect(String[].class, sql, paramsInitial, typesInitial);
      for (String[] queryResult : queryResults) {
        String markerAttributeAssignId = queryResult[0];
        String groupId = queryResult[1];
        String groupName = queryResult[2];
        Long idIndex = Long.parseLong(queryResult[3]);
        String configName = queryResult[4];
        String configValue = queryResult[5];
        
        if (results.get(groupName) == null) {
          results.put(groupName, new GrouperProvisioningObjectAttributes(groupId, groupName, idIndex, markerAttributeAssignId));
          results.get(groupName).setOwnedByGroup(true);
        }
        
        if (configName.equals(GrouperProvisioningSettings.provisioningConfigStemName()+":"+GrouperProvisioningAttributeNames.PROVISIONING_DIRECT_ASSIGNMENT)) {
          results.get(groupName).setProvisioningDirectAssign(configValue);
        } else if (configName.equals(GrouperProvisioningSettings.provisioningConfigStemName()+":"+GrouperProvisioningAttributeNames.PROVISIONING_DO_PROVISION)) {
          results.get(groupName).setProvisioningDoProvision(configValue);
        } else if (configName.equals(GrouperProvisioningSettings.provisioningConfigStemName()+":"+GrouperProvisioningAttributeNames.PROVISIONING_METADATA_JSON)) {
          results.get(groupName).setProvisioningMetadataJson(configValue);
        } else if (configName.equals(GrouperProvisioningSettings.provisioningConfigStemName()+":"+GrouperProvisioningAttributeNames.PROVISIONING_OWNER_STEM_ID)) {
          results.get(groupName).setProvisioningOwnerStemId(configValue);
        } else if (configName.equals(GrouperProvisioningSettings.provisioningConfigStemName()+":"+GrouperProvisioningAttributeNames.PROVISIONING_TARGET)) {
          results.get(groupName).setProvisioningTarget(configValue);
        }
      }
    }
    
    // now see if there are any other group to add to the map that don't have attributes but are under a parent folder that has a direct assign
    // they won't necessarily need attributes but they'll need to be checked.

    {
      String sql = "SELECT distinct " + 
          "    gg.id, " + 
          "    gg.name, " + 
          "    gg.id_index " +
          "FROM " + 
          "    grouper_stems gs, " + 
          "    grouper_attribute_assign gaa_marker, " + 
          "    grouper_attribute_assign gaa_target, " + 
          "    grouper_attribute_assign gaa_direct, " + 
          "    grouper_attribute_assign_value gaav_target, " + 
          "    grouper_attribute_assign_value gaav_direct, " + 
          "    grouper_attribute_def_name gadn_marker, " + 
          "    grouper_attribute_def_name gadn_target, " + 
          "    grouper_attribute_def_name gadn_direct, " + 
          "    grouper_stem_set gss, " + 
          "    grouper_groups gg " + 
          "WHERE " + 
          "    gs.id = gaa_marker.owner_stem_id " + 
          "    AND gaa_marker.attribute_def_name_id = gadn_marker.id " + 
          "    AND gadn_marker.name = ? " + 
          "    AND gaa_marker.id = gaa_target.owner_attribute_assign_id " + 
          "    AND gg.type_of_group != 'entity' " + 
          "    AND gaa_target.attribute_def_name_id = gadn_target.id " + 
          "    AND gadn_target.name = ? " + 
          "    AND gaav_target.attribute_assign_id = gaa_target.id " + 
          "    AND gaav_target.value_string = ? " + 
          "    AND gaa_marker.id = gaa_direct.owner_attribute_assign_id " + 
          "    AND gaa_direct.attribute_def_name_id = gadn_direct.id " + 
          "    AND gadn_direct.name = ? " + 
          "    AND gaav_direct.attribute_assign_id = gaa_direct.id " + 
          "    AND gaav_direct.value_string = 'true' " + 
          "    AND gs.id = gss.then_has_stem_id " + 
          "    AND gss.if_has_stem_id = gg.parent_stem " + 
          "    AND gaa_marker.enabled = 'T' " + 
          "    AND gaa_target.enabled = 'T' " + 
          "    AND gaa_direct.enabled = 'T' ";
      
      List<Object> paramsInitial = new ArrayList<Object>();
      paramsInitial.add(GrouperProvisioningSettings.provisioningConfigStemName()+":"+GrouperProvisioningAttributeNames.PROVISIONING_ATTRIBUTE_NAME);
      paramsInitial.add(GrouperProvisioningSettings.provisioningConfigStemName()+":"+GrouperProvisioningAttributeNames.PROVISIONING_TARGET);
      paramsInitial.add(this.grouperProvisioner.getConfigId());
      paramsInitial.add(GrouperProvisioningSettings.provisioningConfigStemName()+":"+GrouperProvisioningAttributeNames.PROVISIONING_DIRECT_ASSIGNMENT);
  
      List<Type> typesInitial = new ArrayList<Type>();
      typesInitial.add(StringType.INSTANCE);
      typesInitial.add(StringType.INSTANCE);
      typesInitial.add(StringType.INSTANCE);
      typesInitial.add(StringType.INSTANCE);
  
      List<String[]> queryResults = HibernateSession.bySqlStatic().listSelect(String[].class, sql, paramsInitial, typesInitial);
      for (String[] queryResult : queryResults) {
        String groupId = queryResult[0];
        String groupName = queryResult[1];
        Long idIndex = Long.parseLong(queryResult[2]);

        if (results.get(groupName) == null) {
          results.put(groupName, new GrouperProvisioningObjectAttributes(groupId, groupName, idIndex, null));
          results.get(groupName).setOwnedByGroup(true);
        }
      }
    }
    
    return results;
  }
  
  
  
  /**
   * get provisioning attributes for given member ids
   * @return the attributes
   */
  public Map<String, GrouperProvisioningObjectAttributes> retrieveProvisioningMemberAttributes(boolean retrieveAll, List<String> memberIds) {

    if (this.grouperProvisioner == null) {
      throw new RuntimeException("grouperProvisioner is not set");
    }
      
    Map<String, GrouperProvisioningObjectAttributes> results = new HashMap<String, GrouperProvisioningObjectAttributes>();

    {
      String sqlInitial = "SELECT " + 
          "    gaa_marker.id, " +
          "    gm.id, " +
          "    gadn_config.name, " + 
          "    gaav_config.value_string, " + 
          "    gm.subject_id, " + 
          "    gm.subject_source, " + 
          "    gm.subject_identifier0, " + 
          "    gm.subject_identifier1, " + 
          "    gm.subject_identifier2, " + 
          "    gm.id_index " + 
          "FROM " + 
          "    grouper_members gm, " + 
          "    grouper_attribute_assign gaa_marker, " + 
          "    grouper_attribute_assign gaa_target, " + 
          "    grouper_attribute_assign gaa_config, " + 
          "    grouper_attribute_assign_value gaav_target, " + 
          "    grouper_attribute_assign_value gaav_config, " + 
          "    grouper_attribute_def_name gadn_marker, " + 
          "    grouper_attribute_def_name gadn_target, " + 
          "    grouper_attribute_def_name gadn_config " + 
          "WHERE " + 
          "    gm.id = gaa_marker.owner_member_id " + 
          "    AND gaa_marker.attribute_def_name_id = gadn_marker.id " + 
          "    AND gadn_marker.name = ? " + 
          "    AND gaa_marker.id = gaa_target.owner_attribute_assign_id " + 
          "    AND gaa_target.attribute_def_name_id = gadn_target.id " + 
          "    AND gadn_target.name = ? " + 
          "    AND gaav_target.attribute_assign_id = gaa_target.id " + 
          "    AND gaav_target.value_string = ? " + 
          "    AND gaa_marker.id = gaa_config.owner_attribute_assign_id " + 
          "    AND gaav_config.attribute_assign_id = gaa_config.id " + 
          "    AND gadn_config.id = gaa_config.attribute_def_name_id " + 
          "    AND gaa_marker.enabled = 'T' " + 
          "    AND gaa_target.enabled = 'T' " + 
          "    AND gaa_config.enabled = 'T' " + 
          "    AND gm.subject_resolution_deleted='F' ";
      
      List<Object> paramsInitial = new ArrayList<Object>();
      paramsInitial.add(GrouperProvisioningSettings.provisioningConfigStemName()+":"+GrouperProvisioningAttributeNames.PROVISIONING_ATTRIBUTE_NAME);
      paramsInitial.add(GrouperProvisioningSettings.provisioningConfigStemName()+":"+GrouperProvisioningAttributeNames.PROVISIONING_TARGET);
      paramsInitial.add(this.grouperProvisioner.getConfigId());
  
      List<Type> typesInitial = new ArrayList<Type>();
      typesInitial.add(StringType.INSTANCE);
      typesInitial.add(StringType.INSTANCE);
      typesInitial.add(StringType.INSTANCE);
      
      
      List<String[]> queryResults = null;
      if (retrieveAll) {
        queryResults = HibernateSession.bySqlStatic().listSelect(String[].class, sqlInitial, paramsInitial, typesInitial);
      } else {
        if (GrouperUtil.length(memberIds) == 0) {
          return results;
        }

        List<String> idsList = GrouperUtil.listFromCollection(memberIds);
        queryResults = new ArrayList<String[]>();
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
          sql.append(" and gm.id in (");
          sql.append(HibUtils.convertToInClauseForSqlStatic(currentBatchIds));
          sql.append(") ");
          
          queryResults.addAll(GrouperUtil.nonNull(HibernateSession.bySqlStatic().listSelect(String[].class, sql.toString(), params, types)));
          
        }      
      }
      
      for (String[] queryResult : queryResults) {
        String markerAttributeAssignId = queryResult[0];
        String memberId = queryResult[1];
        String configName = queryResult[2];
        String configValue = queryResult[3];
        
        String subjectId = queryResult[4]; 
        String sourceId = queryResult[5]; 
        String subjectIdentifier0 = queryResult[6]; 
        String subjectIdentifier1 = queryResult[7]; 
        String subjectIdentifier2 = queryResult[8]; 
        String idIndex = queryResult[9]; 
        
        GrouperProvisioningObjectAttributes provisioningObjectAttributes = new GrouperProvisioningObjectAttributes(memberId, null, null, markerAttributeAssignId);
        provisioningObjectAttributes.setSubjectId(subjectId);
        provisioningObjectAttributes.setSourceId(sourceId);
        provisioningObjectAttributes.setSubjectIdentifier0(subjectIdentifier0);
        provisioningObjectAttributes.setSubjectIdentifier1(subjectIdentifier1);
        provisioningObjectAttributes.setSubjectIdentifier2(subjectIdentifier2);
        provisioningObjectAttributes.setIdIndex(Long.parseLong(idIndex));
        
        if (results.get(memberId) == null) {
          results.put(memberId, provisioningObjectAttributes);
        }
        
        if (configName.equals(GrouperProvisioningSettings.provisioningConfigStemName()+":"+GrouperProvisioningAttributeNames.PROVISIONING_METADATA_JSON)) {
          results.get(memberId).setProvisioningMetadataJson(configValue);
        } else if (configName.equals(GrouperProvisioningSettings.provisioningConfigStemName()+":"+GrouperProvisioningAttributeNames.PROVISIONING_TARGET)) {
          results.get(memberId).setProvisioningTarget(configValue);
        }
      }
    }
    
    return results;
  }
  
  /**
   * get provisioning attributes for a single group
   * @param groupId
   * @return the attributes
   */
  public GrouperProvisioningObjectAttributes retrieveProvisioningGroupAttributesByGroup(String groupId) {

    if (this.grouperProvisioner == null) {
      throw new RuntimeException("grouperProvisioner is not set");
    }
      
    GrouperProvisioningObjectAttributes result = null;

    String sql = "SELECT " + 
        "    gaa_marker.id, " +
        "    gg.name, " + 
        "    gg.id_index, " +
        "    gadn_config.name, " + 
        "    gaav_config.value_string " + 
        "FROM " + 
        "    grouper_groups gg, " + 
        "    grouper_attribute_assign gaa_marker, " + 
        "    grouper_attribute_assign gaa_target, " + 
        "    grouper_attribute_assign gaa_config, " + 
        "    grouper_attribute_assign_value gaav_target, " + 
        "    grouper_attribute_assign_value gaav_config, " + 
        "    grouper_attribute_def_name gadn_marker, " + 
        "    grouper_attribute_def_name gadn_target, " + 
        "    grouper_attribute_def_name gadn_config " + 
        "WHERE " + 
        "    gg.id = ? " +
        "    AND gg.id = gaa_marker.owner_group_id " + 
        "    AND gg.type_of_group != 'entity' " + 
        "    AND gaa_marker.attribute_def_name_id = gadn_marker.id " + 
        "    AND gadn_marker.name = ? " + 
        "    AND gaa_marker.id = gaa_target.owner_attribute_assign_id " + 
        "    AND gaa_target.attribute_def_name_id = gadn_target.id " + 
        "    AND gadn_target.name = ? " + 
        "    AND gaav_target.attribute_assign_id = gaa_target.id " + 
        "    AND gaav_target.value_string = ? " + 
        "    AND gaa_marker.id = gaa_config.owner_attribute_assign_id " + 
        "    AND gaav_config.attribute_assign_id = gaa_config.id " + 
        "    AND gadn_config.id = gaa_config.attribute_def_name_id " + 
        "    AND gaa_marker.enabled = 'T' " + 
        "    AND gaa_target.enabled = 'T' " + 
        "    AND gaa_config.enabled = 'T' ";
    
    List<Object> paramsInitial = new ArrayList<Object>();
    paramsInitial.add(groupId);
    paramsInitial.add(GrouperProvisioningSettings.provisioningConfigStemName()+":"+GrouperProvisioningAttributeNames.PROVISIONING_ATTRIBUTE_NAME);
    paramsInitial.add(GrouperProvisioningSettings.provisioningConfigStemName()+":"+GrouperProvisioningAttributeNames.PROVISIONING_TARGET);
    paramsInitial.add(this.grouperProvisioner.getConfigId());

    List<Type> typesInitial = new ArrayList<Type>();
    typesInitial.add(StringType.INSTANCE);
    typesInitial.add(StringType.INSTANCE);
    typesInitial.add(StringType.INSTANCE);
    typesInitial.add(StringType.INSTANCE);

    List<String[]> queryResults = HibernateSession.bySqlStatic().listSelect(String[].class, sql, paramsInitial, typesInitial);
    for (String[] queryResult : queryResults) {
      String markerAttributeAssignId = queryResult[0];
      String groupName = queryResult[1];
      Long idIndex = Long.parseLong(queryResult[2]);
      String configName = queryResult[3];
      String configValue = queryResult[4];
      
      if (result == null) {
        result = new GrouperProvisioningObjectAttributes(groupId, groupName, idIndex, markerAttributeAssignId);
        result.setOwnedByGroup(true);
      }
      
      if (configName.equals(GrouperProvisioningSettings.provisioningConfigStemName()+":"+GrouperProvisioningAttributeNames.PROVISIONING_DIRECT_ASSIGNMENT)) {
        result.setProvisioningDirectAssign(configValue);
      } else if (configName.equals(GrouperProvisioningSettings.provisioningConfigStemName()+":"+GrouperProvisioningAttributeNames.PROVISIONING_DO_PROVISION)) {
        result.setProvisioningDoProvision(configValue);
      } else if (configName.equals(GrouperProvisioningSettings.provisioningConfigStemName()+":"+GrouperProvisioningAttributeNames.PROVISIONING_METADATA_JSON)) {
        result.setProvisioningMetadataJson(configValue);
      } else if (configName.equals(GrouperProvisioningSettings.provisioningConfigStemName()+":"+GrouperProvisioningAttributeNames.PROVISIONING_OWNER_STEM_ID)) {
        result.setProvisioningOwnerStemId(configValue);
      } else if (configName.equals(GrouperProvisioningSettings.provisioningConfigStemName()+":"+GrouperProvisioningAttributeNames.PROVISIONING_TARGET)) {
        result.setProvisioningTarget(configValue);
      }
    }
    
    return result;
  }
  
  /**
   * get provisioning attributes for a folder and its parents
   * @param childStemId
   * @return the attributes
   */
  public Map<String, GrouperProvisioningObjectAttributes> retrieveAncestorProvisioningAttributesByFolder(String childStemId) {

    if (this.grouperProvisioner == null) {
      throw new RuntimeException("grouperProvisioner is not set");
    }
      
    Map<String, GrouperProvisioningObjectAttributes> results = new HashMap<String, GrouperProvisioningObjectAttributes>();

    String sql = "SELECT " + 
        "    gaa_marker.id, " +
        "    gs_then_has_stem.id, " + 
        "    gs_then_has_stem.name, " +
        "    gs_then_has_stem.id_index, " +
        "    gadn_config.name, " + 
        "    gaav_config.value_string " + 
        "FROM " + 
        "    grouper_stems gs, " + 
        "    grouper_stem_set gss, " +
        "    grouper_stems gs_then_has_stem, " +
        "    grouper_attribute_assign gaa_marker, " + 
        "    grouper_attribute_assign gaa_target, " + 
        "    grouper_attribute_assign gaa_config, " + 
        "    grouper_attribute_assign_value gaav_target, " + 
        "    grouper_attribute_assign_value gaav_config, " + 
        "    grouper_attribute_def_name gadn_marker, " + 
        "    grouper_attribute_def_name gadn_target, " + 
        "    grouper_attribute_def_name gadn_config " + 
        "WHERE " + 
        "    gs.id = ? " +
        "    AND gs.id = gss.if_has_stem_id " +
        "    AND gss.then_has_stem_id = gs_then_has_stem.id " + 
        "    AND gss.then_has_stem_id = gaa_marker.owner_stem_id " + 
        "    AND gaa_marker.attribute_def_name_id = gadn_marker.id " + 
        "    AND gadn_marker.name = ? " + 
        "    AND gaa_marker.id = gaa_target.owner_attribute_assign_id " + 
        "    AND gaa_target.attribute_def_name_id = gadn_target.id " + 
        "    AND gadn_target.name = ? " + 
        "    AND gaav_target.attribute_assign_id = gaa_target.id " + 
        "    AND gaav_target.value_string = ? " + 
        "    AND gaa_marker.id = gaa_config.owner_attribute_assign_id " + 
        "    AND gaav_config.attribute_assign_id = gaa_config.id " + 
        "    AND gadn_config.id = gaa_config.attribute_def_name_id " + 
        "    AND gaa_marker.enabled = 'T' " + 
        "    AND gaa_target.enabled = 'T' " + 
        "    AND gaa_config.enabled = 'T' ";
    
    List<Object> paramsInitial = new ArrayList<Object>();
    paramsInitial.add(childStemId);
    paramsInitial.add(GrouperProvisioningSettings.provisioningConfigStemName()+":"+GrouperProvisioningAttributeNames.PROVISIONING_ATTRIBUTE_NAME);
    paramsInitial.add(GrouperProvisioningSettings.provisioningConfigStemName()+":"+GrouperProvisioningAttributeNames.PROVISIONING_TARGET);
    paramsInitial.add(this.grouperProvisioner.getConfigId());

    List<Type> typesInitial = new ArrayList<Type>();
    typesInitial.add(StringType.INSTANCE);
    typesInitial.add(StringType.INSTANCE);
    typesInitial.add(StringType.INSTANCE);
    typesInitial.add(StringType.INSTANCE);

    List<String[]> queryResults = HibernateSession.bySqlStatic().listSelect(String[].class, sql, paramsInitial, typesInitial);
    for (String[] queryResult : queryResults) {
      String markerAttributeAssignId = queryResult[0];
      String stemId = queryResult[1];
      String stemName = queryResult[2];
      Long idIndex = Long.parseLong(queryResult[3]);
      String configName = queryResult[4];
      String configValue = queryResult[5];
      
      if (results.get(stemName) == null) {
        results.put(stemName, new GrouperProvisioningObjectAttributes(stemId, stemName, idIndex, markerAttributeAssignId));
        results.get(stemName).setOwnedByStem(true);
      }
      
      if (configName.equals(GrouperProvisioningSettings.provisioningConfigStemName()+":"+GrouperProvisioningAttributeNames.PROVISIONING_DIRECT_ASSIGNMENT)) {
        results.get(stemName).setProvisioningDirectAssign(configValue);
      } else if (configName.equals(GrouperProvisioningSettings.provisioningConfigStemName()+":"+GrouperProvisioningAttributeNames.PROVISIONING_DO_PROVISION)) {
        results.get(stemName).setProvisioningDoProvision(configValue);
      } else if (configName.equals(GrouperProvisioningSettings.provisioningConfigStemName()+":"+GrouperProvisioningAttributeNames.PROVISIONING_METADATA_JSON)) {
        results.get(stemName).setProvisioningMetadataJson(configValue);
      } else if (configName.equals(GrouperProvisioningSettings.provisioningConfigStemName()+":"+GrouperProvisioningAttributeNames.PROVISIONING_OWNER_STEM_ID)) {
        results.get(stemName).setProvisioningOwnerStemId(configValue);
      } else if (configName.equals(GrouperProvisioningSettings.provisioningConfigStemName()+":"+GrouperProvisioningAttributeNames.PROVISIONING_STEM_SCOPE)) {
        results.get(stemName).setProvisioningStemScope(configValue);
      } else if (configName.equals(GrouperProvisioningSettings.provisioningConfigStemName()+":"+GrouperProvisioningAttributeNames.PROVISIONING_TARGET)) {
        results.get(stemName).setProvisioningTarget(configValue);
      }
    }
    
    return results;
  }
  
  /**
   * get provisioning attributes for a folder and its children
   * @param childStemId
   * @return the attributes
   */
  public Map<String, GrouperProvisioningObjectAttributes> retrieveChildProvisioningFolderAttributesByFolder(String parentStemId) {

    if (this.grouperProvisioner == null) {
      throw new RuntimeException("grouperProvisioner is not set");
    }
      
    Map<String, GrouperProvisioningObjectAttributes> results = new HashMap<String, GrouperProvisioningObjectAttributes>();

    {
      String sql = "SELECT " + 
          "    gaa_marker.id, " +
          "    gs_if_has_stem.id, " + 
          "    gs_if_has_stem.name, " +
          "    gs_if_has_stem.id_index, " +
          "    gadn_config.name, " + 
          "    gaav_config.value_string " + 
          "FROM " + 
          "    grouper_stems gs, " + 
          "    grouper_stem_set gss, " +
          "    grouper_stems gs_if_has_stem, " +
          "    grouper_attribute_assign gaa_marker, " + 
          "    grouper_attribute_assign gaa_target, " + 
          "    grouper_attribute_assign gaa_config, " + 
          "    grouper_attribute_assign_value gaav_target, " + 
          "    grouper_attribute_assign_value gaav_config, " + 
          "    grouper_attribute_def_name gadn_marker, " + 
          "    grouper_attribute_def_name gadn_target, " + 
          "    grouper_attribute_def_name gadn_config " + 
          "WHERE " + 
          "    gs.id = ? " +
          "    AND gs.id = gss.then_has_stem_id " +
          "    AND gss.if_has_stem_id = gs_if_has_stem.id " + 
          "    AND gss.if_has_stem_id = gaa_marker.owner_stem_id " + 
          "    AND gaa_marker.attribute_def_name_id = gadn_marker.id " + 
          "    AND gadn_marker.name = ? " + 
          "    AND gaa_marker.id = gaa_target.owner_attribute_assign_id " + 
          "    AND gaa_target.attribute_def_name_id = gadn_target.id " + 
          "    AND gadn_target.name = ? " + 
          "    AND gaav_target.attribute_assign_id = gaa_target.id " + 
          "    AND gaav_target.value_string = ? " + 
          "    AND gaa_marker.id = gaa_config.owner_attribute_assign_id " + 
          "    AND gaav_config.attribute_assign_id = gaa_config.id " + 
          "    AND gadn_config.id = gaa_config.attribute_def_name_id " + 
          "    AND gaa_marker.enabled = 'T' " + 
          "    AND gaa_target.enabled = 'T' " + 
          "    AND gaa_config.enabled = 'T' ";
      
      List<Object> paramsInitial = new ArrayList<Object>();
      paramsInitial.add(parentStemId);
      paramsInitial.add(GrouperProvisioningSettings.provisioningConfigStemName()+":"+GrouperProvisioningAttributeNames.PROVISIONING_ATTRIBUTE_NAME);
      paramsInitial.add(GrouperProvisioningSettings.provisioningConfigStemName()+":"+GrouperProvisioningAttributeNames.PROVISIONING_TARGET);
      paramsInitial.add(this.grouperProvisioner.getConfigId());
  
      List<Type> typesInitial = new ArrayList<Type>();
      typesInitial.add(StringType.INSTANCE);
      typesInitial.add(StringType.INSTANCE);
      typesInitial.add(StringType.INSTANCE);
      typesInitial.add(StringType.INSTANCE);
  
      List<String[]> queryResults = HibernateSession.bySqlStatic().listSelect(String[].class, sql, paramsInitial, typesInitial);
      for (String[] queryResult : queryResults) {
        String markerAttributeAssignId = queryResult[0];
        String stemId = queryResult[1];
        String stemName = queryResult[2];
        Long idIndex = Long.parseLong(queryResult[3]);
        String configName = queryResult[4];
        String configValue = queryResult[5];
        
        if (results.get(stemName) == null) {
          results.put(stemName, new GrouperProvisioningObjectAttributes(stemId, stemName, idIndex, markerAttributeAssignId));
          results.get(stemName).setOwnedByStem(true);
        }
        
        if (configName.equals(GrouperProvisioningSettings.provisioningConfigStemName()+":"+GrouperProvisioningAttributeNames.PROVISIONING_DIRECT_ASSIGNMENT)) {
          results.get(stemName).setProvisioningDirectAssign(configValue);
        } else if (configName.equals(GrouperProvisioningSettings.provisioningConfigStemName()+":"+GrouperProvisioningAttributeNames.PROVISIONING_DO_PROVISION)) {
          results.get(stemName).setProvisioningDoProvision(configValue);
        } else if (configName.equals(GrouperProvisioningSettings.provisioningConfigStemName()+":"+GrouperProvisioningAttributeNames.PROVISIONING_METADATA_JSON)) {
          results.get(stemName).setProvisioningMetadataJson(configValue);
        } else if (configName.equals(GrouperProvisioningSettings.provisioningConfigStemName()+":"+GrouperProvisioningAttributeNames.PROVISIONING_OWNER_STEM_ID)) {
          results.get(stemName).setProvisioningOwnerStemId(configValue);
        } else if (configName.equals(GrouperProvisioningSettings.provisioningConfigStemName()+":"+GrouperProvisioningAttributeNames.PROVISIONING_STEM_SCOPE)) {
          results.get(stemName).setProvisioningStemScope(configValue);
        } else if (configName.equals(GrouperProvisioningSettings.provisioningConfigStemName()+":"+GrouperProvisioningAttributeNames.PROVISIONING_TARGET)) {
          results.get(stemName).setProvisioningTarget(configValue);
        }
      }
    }
    
    {
      String sql = "SELECT " + 
          "    gs.id, " + 
          "    gs.name, " +
          "    gs.id_index " +
          "FROM " + 
          "    grouper_stem_set gss, " +
          "    grouper_stems gs " + 
          "WHERE " + 
          "    gss.then_has_stem_id = ?" +
          "    AND gss.if_has_stem_id = gs.id ";
      
      List<Object> paramsInitial = new ArrayList<Object>();
      paramsInitial.add(parentStemId);
  
      List<Type> typesInitial = new ArrayList<Type>();
      typesInitial.add(StringType.INSTANCE);
  
      List<String[]> queryResults = HibernateSession.bySqlStatic().listSelect(String[].class, sql, paramsInitial, typesInitial);
      for (String[] queryResult : queryResults) {
        String stemId = queryResult[0];
        String stemName = queryResult[1];
        Long idIndex = Long.parseLong(queryResult[2]);

        if (results.get(stemName) == null) {
          results.put(stemName, new GrouperProvisioningObjectAttributes(stemId, stemName, idIndex, null));
          results.get(stemName).setOwnedByStem(true);
        }
      }
    }
    
    return results;
  }
  
  /**
   * get provisioning attributes for groups under a folder
   * @param childStemId
   * @return the attributes
   */
  public Map<String, GrouperProvisioningObjectAttributes> retrieveChildProvisioningGroupAttributesByFolder(String parentStemId) {

    if (this.grouperProvisioner == null) {
      throw new RuntimeException("grouperProvisioner is not set");
    }
      
    Map<String, GrouperProvisioningObjectAttributes> results = new HashMap<String, GrouperProvisioningObjectAttributes>();

    {
      String sql = "SELECT " + 
          "    gaa_marker.id, " +
          "    gg.id, " + 
          "    gg.name, " +
          "    gg.id_index, " +
          "    gadn_config.name, " + 
          "    gaav_config.value_string " + 
          "FROM " + 
          "    grouper_stems gs, " + 
          "    grouper_stem_set gss, " +
          "    grouper_groups gg, " +
          "    grouper_attribute_assign gaa_marker, " + 
          "    grouper_attribute_assign gaa_target, " + 
          "    grouper_attribute_assign gaa_config, " + 
          "    grouper_attribute_assign_value gaav_target, " + 
          "    grouper_attribute_assign_value gaav_config, " + 
          "    grouper_attribute_def_name gadn_marker, " + 
          "    grouper_attribute_def_name gadn_target, " + 
          "    grouper_attribute_def_name gadn_config " + 
          "WHERE " + 
          "    gs.id = ? " +
          "    AND gs.id = gss.then_has_stem_id " +
          "    AND gss.if_has_stem_id = gg.parent_stem " + 
          "    AND gg.id = gaa_marker.owner_group_id " + 
          "    AND gg.type_of_group != 'entity' " + 
          "    AND gaa_marker.attribute_def_name_id = gadn_marker.id " + 
          "    AND gadn_marker.name = ? " + 
          "    AND gaa_marker.id = gaa_target.owner_attribute_assign_id " + 
          "    AND gaa_target.attribute_def_name_id = gadn_target.id " + 
          "    AND gadn_target.name = ? " + 
          "    AND gaav_target.attribute_assign_id = gaa_target.id " + 
          "    AND gaav_target.value_string = ? " + 
          "    AND gaa_marker.id = gaa_config.owner_attribute_assign_id " + 
          "    AND gaav_config.attribute_assign_id = gaa_config.id " + 
          "    AND gadn_config.id = gaa_config.attribute_def_name_id " + 
          "    AND gaa_marker.enabled = 'T' " + 
          "    AND gaa_target.enabled = 'T' " + 
          "    AND gaa_config.enabled = 'T' ";
      
      List<Object> paramsInitial = new ArrayList<Object>();
      paramsInitial.add(parentStemId);
      paramsInitial.add(GrouperProvisioningSettings.provisioningConfigStemName()+":"+GrouperProvisioningAttributeNames.PROVISIONING_ATTRIBUTE_NAME);
      paramsInitial.add(GrouperProvisioningSettings.provisioningConfigStemName()+":"+GrouperProvisioningAttributeNames.PROVISIONING_TARGET);
      paramsInitial.add(this.grouperProvisioner.getConfigId());
  
      List<Type> typesInitial = new ArrayList<Type>();
      typesInitial.add(StringType.INSTANCE);
      typesInitial.add(StringType.INSTANCE);
      typesInitial.add(StringType.INSTANCE);
      typesInitial.add(StringType.INSTANCE);
  
      List<String[]> queryResults = HibernateSession.bySqlStatic().listSelect(String[].class, sql, paramsInitial, typesInitial);
      for (String[] queryResult : queryResults) {
        String markerAttributeAssignId = queryResult[0];
        String groupId = queryResult[1];
        String groupName = queryResult[2];
        Long idIndex = Long.parseLong(queryResult[3]);
        String configName = queryResult[4];
        String configValue = queryResult[5];
        
        if (results.get(groupId) == null) {
          results.put(groupId, new GrouperProvisioningObjectAttributes(groupId, groupName, idIndex, markerAttributeAssignId));
          results.get(groupId).setOwnedByGroup(true);
        }
        
        if (configName.equals(GrouperProvisioningSettings.provisioningConfigStemName()+":"+GrouperProvisioningAttributeNames.PROVISIONING_DIRECT_ASSIGNMENT)) {
          results.get(groupId).setProvisioningDirectAssign(configValue);
        } else if (configName.equals(GrouperProvisioningSettings.provisioningConfigStemName()+":"+GrouperProvisioningAttributeNames.PROVISIONING_DO_PROVISION)) {
          results.get(groupId).setProvisioningDoProvision(configValue);
        } else if (configName.equals(GrouperProvisioningSettings.provisioningConfigStemName()+":"+GrouperProvisioningAttributeNames.PROVISIONING_METADATA_JSON)) {
          results.get(groupId).setProvisioningMetadataJson(configValue);
        } else if (configName.equals(GrouperProvisioningSettings.provisioningConfigStemName()+":"+GrouperProvisioningAttributeNames.PROVISIONING_OWNER_STEM_ID)) {
          results.get(groupId).setProvisioningOwnerStemId(configValue);
        } else if (configName.equals(GrouperProvisioningSettings.provisioningConfigStemName()+":"+GrouperProvisioningAttributeNames.PROVISIONING_STEM_SCOPE)) {
          results.get(groupId).setProvisioningStemScope(configValue);
        } else if (configName.equals(GrouperProvisioningSettings.provisioningConfigStemName()+":"+GrouperProvisioningAttributeNames.PROVISIONING_TARGET)) {
          results.get(groupId).setProvisioningTarget(configValue);
        }
      }
    }
    
    {
      String sql = "SELECT " + 
          "    gg.id, " + 
          "    gg.name, " +
          "    gg.id_index " +
          "FROM " + 
          "    grouper_stem_set gss, " +
          "    grouper_groups gg " + 
          "WHERE " + 
          "    gss.then_has_stem_id = ?" +
          "    AND gg.type_of_group != 'entity' " + 
          "    AND gss.if_has_stem_id = gg.parent_stem ";
      
      List<Object> paramsInitial = new ArrayList<Object>();
      paramsInitial.add(parentStemId);
  
      List<Type> typesInitial = new ArrayList<Type>();
      typesInitial.add(StringType.INSTANCE);
  
      List<String[]> queryResults = HibernateSession.bySqlStatic().listSelect(String[].class, sql, paramsInitial, typesInitial);
      for (String[] queryResult : queryResults) {
        String groupId = queryResult[0];
        String groupName = queryResult[1];
        Long idIndex = Long.parseLong(queryResult[2]);

        if (results.get(groupId) == null) {
          results.put(groupId, new GrouperProvisioningObjectAttributes(groupId, groupName, idIndex, null));
          results.get(groupId).setOwnedByGroup(true);
        }
      }
    }
    
    return results;
  }
  
  /**
   * @return stem id if is/was a direct folder assignment for this provisioner
   */
  public String getStemIdIfDirectStemAssignmentByPITMarkerAttributeAssignId(String markerAttributeAssignId) {

    if (this.grouperProvisioner == null) {
      throw new RuntimeException("grouperProvisioner is not set");
    }
    
    if (StringUtils.isBlank(markerAttributeAssignId)) {
      return null;
    }
    
    String sql = "SELECT gps.source_id " +
        "FROM " + 
        "    grouper_pit_stems gps, " +
        "    grouper_pit_attribute_assign gpaa_marker, " + 
        "    grouper_pit_attribute_assign gpaa_target, " + 
        "    grouper_pit_attribute_assign gpaa_direct, " + 
        "    grouper_pit_attr_assn_value gpaav_target, " + 
        "    grouper_pit_attr_assn_value gpaav_direct, " + 
        "    grouper_pit_attr_def_name gpadn_marker, " + 
        "    grouper_pit_attr_def_name gpadn_target, " + 
        "    grouper_pit_attr_def_name gpadn_direct " + 
        "WHERE " + 
        "    gpaa_marker.id = ? " +
        "    AND gpaa_marker.owner_stem_id = gps.id " +
        "    AND gpaa_marker.attribute_def_name_id = gpadn_marker.id " + 
        "    AND gpadn_marker.name = ? " + 
        "    AND gpaa_marker.id = gpaa_target.owner_attribute_assign_id " + 
        "    AND gpaa_target.attribute_def_name_id = gpadn_target.id " + 
        "    AND gpadn_target.name = ? " + 
        "    AND gpaav_target.attribute_assign_id = gpaa_target.id " + 
        "    AND gpaav_target.value_string = ? " + 
        "    AND gpaa_marker.id = gpaa_direct.owner_attribute_assign_id " + 
        "    AND gpaa_direct.attribute_def_name_id = gpadn_direct.id " + 
        "    AND gpadn_direct.name = ? " + 
        "    AND gpaav_direct.attribute_assign_id = gpaa_direct.id " + 
        "    AND gpaav_direct.value_string = 'true' ";
    
    List<Object> paramsInitial = new ArrayList<Object>();
    paramsInitial.add(markerAttributeAssignId);
    paramsInitial.add(GrouperProvisioningSettings.provisioningConfigStemName()+":"+GrouperProvisioningAttributeNames.PROVISIONING_ATTRIBUTE_NAME);
    paramsInitial.add(GrouperProvisioningSettings.provisioningConfigStemName()+":"+GrouperProvisioningAttributeNames.PROVISIONING_TARGET);
    paramsInitial.add(this.grouperProvisioner.getConfigId());
    paramsInitial.add(GrouperProvisioningSettings.provisioningConfigStemName()+":"+GrouperProvisioningAttributeNames.PROVISIONING_DIRECT_ASSIGNMENT);

    List<Type> typesInitial = new ArrayList<Type>();
    typesInitial.add(StringType.INSTANCE);
    typesInitial.add(StringType.INSTANCE);
    typesInitial.add(StringType.INSTANCE);
    typesInitial.add(StringType.INSTANCE);
    typesInitial.add(StringType.INSTANCE);

    List<String> stemIds = HibernateSession.bySqlStatic().listSelect(String.class, sql, paramsInitial, typesInitial);
    if (stemIds.size() > 0) {
      return stemIds.get(0);
    }
    
    return null;
  }
  
  
  /**
   * @return member id if is/was a direct member assignment for this provisioner
   */
  public String getMemberIdIfDirectMemberAssignmentByPITMarkerAttributeAssignId(String markerAttributeAssignId) {

    if (this.grouperProvisioner == null) {
      throw new RuntimeException("grouperProvisioner is not set");
    }
    
    if (StringUtils.isBlank(markerAttributeAssignId)) {
      return null;
    }
    
    String sql = "SELECT gpm.source_id " +
        "FROM " + 
        "    grouper_pit_members gpm, " +
        "    grouper_pit_attribute_assign gpaa_marker, " + 
        "    grouper_pit_attribute_assign gpaa_target, " + 
        "    grouper_pit_attr_assn_value gpaav_target, " + 
        "    grouper_pit_attr_def_name gpadn_marker, " + 
        "    grouper_pit_attr_def_name gpadn_target " + 
        "WHERE " + 
        "    gpaa_marker.id = ? " +
        "    AND gpaa_marker.owner_member_id = gpm.id " +
        "    AND gpaa_marker.attribute_def_name_id = gpadn_marker.id " + 
        "    AND gpadn_marker.name = ? " + 
        "    AND gpaa_marker.id = gpaa_target.owner_attribute_assign_id " + 
        "    AND gpaa_target.attribute_def_name_id = gpadn_target.id " + 
        "    AND gpadn_target.name = ? " + 
        "    AND gpaav_target.attribute_assign_id = gpaa_target.id " + 
        "    AND gpaav_target.value_string = ? ";
    
    List<Object> paramsInitial = new ArrayList<Object>();
    paramsInitial.add(markerAttributeAssignId);
    paramsInitial.add(GrouperProvisioningSettings.provisioningConfigStemName()+":"+GrouperProvisioningAttributeNames.PROVISIONING_ATTRIBUTE_NAME);
    paramsInitial.add(GrouperProvisioningSettings.provisioningConfigStemName()+":"+GrouperProvisioningAttributeNames.PROVISIONING_TARGET);
    paramsInitial.add(this.grouperProvisioner.getConfigId());

    List<Type> typesInitial = new ArrayList<Type>();
    typesInitial.add(StringType.INSTANCE);
    typesInitial.add(StringType.INSTANCE);
    typesInitial.add(StringType.INSTANCE);
    typesInitial.add(StringType.INSTANCE);

    List<String> memberIds = HibernateSession.bySqlStatic().listSelect(String.class, sql, paramsInitial, typesInitial);
    if (memberIds.size() > 0) {
      return memberIds.get(0);
    }
    
    return null;
  }
  
  /**
   * @return group id if is/was a direct group assignment for this provisioner
   */
  public String getGroupIdIfDirectGroupAssignmentByPITMarkerAttributeAssignId(String markerAttributeAssignId) {

    if (this.grouperProvisioner == null) {
      throw new RuntimeException("grouperProvisioner is not set");
    }
    
    if (StringUtils.isBlank(markerAttributeAssignId)) {
      return null;
    }
    
    String sql = "SELECT gpg.source_id " +
        "FROM " + 
        "    grouper_pit_groups gpg, " +
        "    grouper_pit_attribute_assign gpaa_marker, " + 
        "    grouper_pit_attribute_assign gpaa_target, " + 
        "    grouper_pit_attribute_assign gpaa_direct, " + 
        "    grouper_pit_attr_assn_value gpaav_target, " + 
        "    grouper_pit_attr_assn_value gpaav_direct, " + 
        "    grouper_pit_attr_def_name gpadn_marker, " + 
        "    grouper_pit_attr_def_name gpadn_target, " + 
        "    grouper_pit_attr_def_name gpadn_direct " + 
        "WHERE " + 
        "    gpaa_marker.id = ? " +
        "    AND gpaa_marker.owner_group_id = gpg.id " +
        "    AND gpaa_marker.attribute_def_name_id = gpadn_marker.id " + 
        "    AND gpadn_marker.name = ? " + 
        "    AND gpaa_marker.id = gpaa_target.owner_attribute_assign_id " + 
        "    AND gpaa_target.attribute_def_name_id = gpadn_target.id " + 
        "    AND gpadn_target.name = ? " + 
        "    AND gpaav_target.attribute_assign_id = gpaa_target.id " + 
        "    AND gpaav_target.value_string = ? " + 
        "    AND gpaa_marker.id = gpaa_direct.owner_attribute_assign_id " + 
        "    AND gpaa_direct.attribute_def_name_id = gpadn_direct.id " + 
        "    AND gpadn_direct.name = ? " + 
        "    AND gpaav_direct.attribute_assign_id = gpaa_direct.id " + 
        "    AND gpaav_direct.value_string = 'true' ";
    
    List<Object> paramsInitial = new ArrayList<Object>();
    paramsInitial.add(markerAttributeAssignId);
    paramsInitial.add(GrouperProvisioningSettings.provisioningConfigStemName()+":"+GrouperProvisioningAttributeNames.PROVISIONING_ATTRIBUTE_NAME);
    paramsInitial.add(GrouperProvisioningSettings.provisioningConfigStemName()+":"+GrouperProvisioningAttributeNames.PROVISIONING_TARGET);
    paramsInitial.add(this.grouperProvisioner.getConfigId());
    paramsInitial.add(GrouperProvisioningSettings.provisioningConfigStemName()+":"+GrouperProvisioningAttributeNames.PROVISIONING_DIRECT_ASSIGNMENT);

    List<Type> typesInitial = new ArrayList<Type>();
    typesInitial.add(StringType.INSTANCE);
    typesInitial.add(StringType.INSTANCE);
    typesInitial.add(StringType.INSTANCE);
    typesInitial.add(StringType.INSTANCE);
    typesInitial.add(StringType.INSTANCE);

    List<String> stemIds = HibernateSession.bySqlStatic().listSelect(String.class, sql, paramsInitial, typesInitial);
    if (stemIds.size() > 0) {
      return stemIds.get(0);
    }
    
    return null;
  }
  
  /**
   * get all group ids that are policy groups
   * @return group ids
   */
  public Set<String> retrieveAllProvisioningGroupIdsThatArePolicyGroups() {

    if (this.grouperProvisioner == null) {
      throw new RuntimeException("grouperProvisioner is not set");
    }
      
    Set<String> groupIds = new HashSet<String>();
    
    {
      String sql = "SELECT distinct " + 
          "    gg.id " + 
          "FROM " + 
          "    grouper_stems gs, " + 
          "    grouper_attribute_assign gaa_marker, " + 
          "    grouper_attribute_assign gaa_target, " + 
          "    grouper_attribute_assign gaa_direct, " + 
          "    grouper_attribute_assign gaa_type_marker, " + 
          "    grouper_attribute_assign gaa_type_name, " + 
          "    grouper_attribute_assign_value gaav_target, " + 
          "    grouper_attribute_assign_value gaav_direct, " + 
          "    grouper_attribute_assign_value gaav_type_name, " + 
          "    grouper_attribute_def_name gadn_marker, " + 
          "    grouper_attribute_def_name gadn_target, " + 
          "    grouper_attribute_def_name gadn_direct, " + 
          "    grouper_attribute_def_name gadn_type_marker, " + 
          "    grouper_attribute_def_name gadn_type_name, " + 
          "    grouper_stem_set gss, " + 
          "    grouper_groups gg " + 
          "WHERE " + 
          "    gs.id = gaa_marker.owner_stem_id " + 
          "    AND gaa_marker.attribute_def_name_id = gadn_marker.id " + 
          "    AND gadn_marker.name = ? " + 
          "    AND gaa_marker.id = gaa_target.owner_attribute_assign_id " + 
          "    AND gaa_target.attribute_def_name_id = gadn_target.id " + 
          "    AND gadn_target.name = ? " + 
          "    AND gaav_target.attribute_assign_id = gaa_target.id " + 
          "    AND gaav_target.value_string = ? " + 
          "    AND gaa_marker.id = gaa_direct.owner_attribute_assign_id " + 
          "    AND gaa_direct.attribute_def_name_id = gadn_direct.id " + 
          "    AND gadn_direct.name = ? " + 
          "    AND gaav_direct.attribute_assign_id = gaa_direct.id " + 
          "    AND gaav_direct.value_string = 'true' " + 
          "    AND gs.id = gss.then_has_stem_id " + 
          "    AND gss.if_has_stem_id = gg.parent_stem " +          
          "    AND gg.id = gaa_type_marker.owner_group_id " +
          "    AND gg.type_of_group != 'entity' " + 
          "    AND gaa_type_marker.attribute_def_name_id = gadn_type_marker.id " +
          "    AND gadn_type_marker.name = ? " +
          "    AND gaa_type_marker.id = gaa_type_name.owner_attribute_assign_id " + 
          "    AND gaa_type_name.attribute_def_name_id = gadn_type_name.id " + 
          "    AND gadn_type_name.name = ? " + 
          "    AND gaav_type_name.attribute_assign_id = gaa_type_name.id " + 
          "    AND gaav_type_name.value_string = 'policy' " + 
          "    AND gaa_type_marker.enabled = 'T' " +
          "    AND gaa_type_name.enabled = 'T' " +
          "    AND gaa_marker.enabled = 'T' " + 
          "    AND gaa_target.enabled = 'T' " + 
          "    AND gaa_direct.enabled = 'T' ";
      
      List<Object> paramsInitial = new ArrayList<Object>();
      paramsInitial.add(GrouperProvisioningSettings.provisioningConfigStemName()+":"+GrouperProvisioningAttributeNames.PROVISIONING_ATTRIBUTE_NAME);
      paramsInitial.add(GrouperProvisioningSettings.provisioningConfigStemName()+":"+GrouperProvisioningAttributeNames.PROVISIONING_TARGET);
      paramsInitial.add(this.grouperProvisioner.getConfigId());
      paramsInitial.add(GrouperProvisioningSettings.provisioningConfigStemName()+":"+GrouperProvisioningAttributeNames.PROVISIONING_DIRECT_ASSIGNMENT);
      paramsInitial.add(GrouperObjectTypesSettings.objectTypesStemName()+":"+GrouperObjectTypesAttributeNames.GROUPER_OBJECT_TYPE_ATTRIBUTE_NAME);
      paramsInitial.add(GrouperObjectTypesSettings.objectTypesStemName()+":"+GrouperObjectTypesAttributeNames.GROUPER_OBJECT_TYPE_NAME);

      List<Type> typesInitial = new ArrayList<Type>();
      typesInitial.add(StringType.INSTANCE);
      typesInitial.add(StringType.INSTANCE);
      typesInitial.add(StringType.INSTANCE);
      typesInitial.add(StringType.INSTANCE);
      typesInitial.add(StringType.INSTANCE);
      typesInitial.add(StringType.INSTANCE);
  
      List<String[]> queryResults = HibernateSession.bySqlStatic().listSelect(String[].class, sql, paramsInitial, typesInitial);
      for (String[] queryResult : queryResults) {
        String groupId = queryResult[0];
        groupIds.add(groupId);
      }
    }
    
    return groupIds;
  }
  
  /**
   * get all group ids that are policy groups from the list passed in
   * @return group ids
   */
  public Set<String> retrieveProvisioningGroupIdsThatArePolicyGroups(Set<String> groupIds) {

    if (this.grouperProvisioner == null) {
      throw new RuntimeException("grouperProvisioner is not set");
    }
      
    Set<String> policyGroupIds = new HashSet<String>();
    
    if (GrouperUtil.length(groupIds) == 0) {
      return policyGroupIds;
    }
    
    String sqlInitial = "SELECT " + 
        "    gaa_type_marker.owner_group_id " + 
        "FROM " + 
        "    grouper_attribute_assign gaa_type_marker, " + 
        "    grouper_attribute_assign gaa_type_name, " + 
        "    grouper_attribute_assign_value gaav_type_name, " + 
        "    grouper_attribute_def_name gadn_type_marker, " + 
        "    grouper_attribute_def_name gadn_type_name " + 
        "WHERE " +      
        "    gaa_type_marker.attribute_def_name_id = gadn_type_marker.id " +
        "    AND gadn_type_marker.name = ? " +
        "    AND gaa_type_marker.id = gaa_type_name.owner_attribute_assign_id " + 
        "    AND gaa_type_name.attribute_def_name_id = gadn_type_name.id " + 
        "    AND gadn_type_name.name = ? " + 
        "    AND gaav_type_name.attribute_assign_id = gaa_type_name.id " + 
        "    AND gaav_type_name.value_string = 'policy' " + 
        "    AND gaa_type_marker.enabled = 'T' " +
        "    AND gaa_type_name.enabled = 'T' ";
    
    List<Object> paramsInitial = new ArrayList<Object>();
    paramsInitial.add(GrouperObjectTypesSettings.objectTypesStemName()+":"+GrouperObjectTypesAttributeNames.GROUPER_OBJECT_TYPE_ATTRIBUTE_NAME);
    paramsInitial.add(GrouperObjectTypesSettings.objectTypesStemName()+":"+GrouperObjectTypesAttributeNames.GROUPER_OBJECT_TYPE_NAME);

    List<Type> typesInitial = new ArrayList<Type>();
    typesInitial.add(StringType.INSTANCE);
    typesInitial.add(StringType.INSTANCE);
    
    List<String[]> queryResults = null;

    List<String> idsList = GrouperUtil.listFromCollection(groupIds);
    
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
      sql.append(" AND gaa_type_marker.owner_group_id in (");
      sql.append(HibUtils.convertToInClauseForSqlStatic(currentBatchIds));
      sql.append(") ");
      
      queryResults = HibernateSession.bySqlStatic().listSelect(String[].class, sql.toString(), params, types);
      
      for (String[] queryResult : queryResults) {
        String groupId = queryResult[0];
        policyGroupIds.add(groupId);
      }
    }
    
    return policyGroupIds;
  }

  public int retrieveMembershipCountForGroup(ProvisioningGroupWrapper provisioningGroupWrapper) {
    
    if (this.grouperProvisioner == null) {
      throw new RuntimeException("grouperProvisioner is not set");
    }
    
    if (StringUtils.isBlank(provisioningGroupWrapper.getSyncGroupId())) {
      return 0;
    }
      
    String sqlInitial = "SELECT " + 
        "    count(sync_membership.id) " + 
        "FROM " + 
        "    grouper_sync_group sync_group, " + 
        "    grouper_sync_membership sync_membership " +  
        "WHERE " +      
        "    sync_membership.grouper_sync_group_id = sync_group.id " +
        "    AND sync_group.id = ? " +
        "    AND sync_group.provisionable = 'T' " +
        "    AND sync_membership.in_target = 'T' ";
    
    List<Object> paramsInitial = new ArrayList<Object>();
    paramsInitial.add(provisioningGroupWrapper.getSyncGroupId());

    List<Type> typesInitial = new ArrayList<Type>();
    typesInitial.add(StringType.INSTANCE);
    
    return HibernateSession.bySqlStatic().select(Integer.class, sqlInitial, paramsInitial, typesInitial);
    
  }
  
  public int retrieveMembershipCountForEntity(ProvisioningEntityWrapper provisioningEntityWrapper) {
    
    if (this.grouperProvisioner == null) {
      throw new RuntimeException("grouperProvisioner is not set");
    }
      
    String sqlInitial = "SELECT " + 
        "    count(sync_membership.id) " + 
        "FROM " + 
        "    grouper_sync_member sync_member, " + 
        "    grouper_sync_membership sync_membership " +  
        "WHERE " +      
        "    sync_membership.grouper_sync_member_id = sync_member.id " +
        "    AND sync_member.provisionable = 'T' " +
        "    AND sync_member.id = ? " +
        "    AND sync_membership.in_target = 'T' ";
    
    List<Object> paramsInitial = new ArrayList<Object>();
    paramsInitial.add(provisioningEntityWrapper.getSyncMemberId());

    List<Type> typesInitial = new ArrayList<Type>();
    typesInitial.add(StringType.INSTANCE);
    
    return HibernateSession.bySqlStatic().select(Integer.class, sqlInitial, paramsInitial, typesInitial);
    
  }
}
