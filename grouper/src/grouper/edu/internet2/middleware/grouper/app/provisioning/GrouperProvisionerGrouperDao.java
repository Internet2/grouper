package edu.internet2.middleware.grouper.app.provisioning;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.type.StringType;
import org.hibernate.type.Type;

import edu.internet2.middleware.grouper.FieldFinder;
import edu.internet2.middleware.grouper.hibernate.HibUtils;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.util.GrouperUtil;

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

  
  public Map<String, ProvisioningGroup> retrieveAllGroups() {
    
    if (this.grouperProvisioner == null) {
      throw new RuntimeException("grouperProvisioner is not set");
    }
    
    StringBuilder sql = new StringBuilder("select gg.id, gg.name, gg.display_name, gg.description, gg.id_index " + 
        "from grouper_sync gs, grouper_sync_group gsg, grouper_groups gg " + 
        "where gs.provisioner_name = ? " + 
        "and gsg.grouper_sync_id = gs.id " + 
        "and gsg.provisionable = 'T' " + 
        "and gsg.group_id = gg.id");
    
    List<String[]> queryResults = HibernateSession.bySqlStatic().listSelect(String[].class, sql.toString(),
        GrouperUtil.toListObject(this.grouperProvisioner.getConfigId()), HibUtils.listType(StringType.INSTANCE));

    Map<String, ProvisioningGroup> results = getTargetGroupMapFromQueryResults(queryResults);
    
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
      
      results.putAll(getTargetGroupMapFromQueryResults(queryResults));
    }
    
    return results;
  }
  
  public Map<String, TargetEntity> retrieveAllMembers() {
    
    if (this.grouperProvisioner == null) {
      throw new RuntimeException("grouperProvisioner is not set");
    }
    
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

    Map<String, TargetEntity> results = getTargetEntityMapFromQueryResults(queryResults);
    
    return results;
  }
  
  public Map<String, TargetMembership> retrieveAllMemberships() {
    
    if (this.grouperProvisioner == null) {
      throw new RuntimeException("grouperProvisioner is not set");
    }
    
    StringBuilder sql = new StringBuilder("select gmav.membership_id, gg.id, gm.id, gm.subject_id, gm.subject_identifier0, gm.name, gm.description " +
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

    Map<String, TargetMembership> results = getTargetMembershipMapFromQueryResults(queryResults);
    
    return results;
  }
  
  public Map<String, TargetEntity> retrieveMembersByIds(Collection<String> ids) {
    
    if (this.grouperProvisioner == null) {
      throw new RuntimeException("grouperProvisioner is not set");
    }
    
    List<String> idsList = GrouperUtil.listFromCollection(ids);
    
    Map<String, TargetEntity> results = new HashMap<String, TargetEntity>();
    
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
      
      results.putAll(getTargetEntityMapFromQueryResults(queryResults));
    }
    
    return results;
  }
  
  private Map<String, ProvisioningGroup> getTargetGroupMapFromQueryResults(List<String[]> queryResults) {
    
    Map<String, ProvisioningGroup> results = new HashMap<String, ProvisioningGroup>();

    for (String[] queryResult : queryResults) {
      String id = queryResult[0];
      String name = queryResult[1];
      String displayName = queryResult[2];
      String description = queryResult[3];
      String idIndex = queryResult[4];
      
      ProvisioningGroup targetGroup = new ProvisioningGroup();
      Map<String, TargetAttribute> attributes = new HashMap<String, TargetAttribute>();
      targetGroup.setId(id);
      targetGroup.setName(name);
      targetGroup.setDisplayName(displayName);
      targetGroup.setIdIndex(Long.parseLong(idIndex));
      TargetAttribute targetAttributeDescription = new TargetAttribute();
      targetAttributeDescription.setName("description");
      targetAttributeDescription.setValue(description);
      attributes.put("description", targetAttributeDescription);
      
      targetGroup.setAttributes(attributes);
      
      results.put(id, targetGroup);
    }
    
    return results;
  }
  
  private Map<String, TargetEntity> getTargetEntityMapFromQueryResults(List<String[]> queryResults) {
    
    Map<String, TargetEntity> results = new HashMap<String, TargetEntity>();

    for (String[] queryResult : queryResults) {
      String id = queryResult[0];
      String subjectId = queryResult[1];
      String subjectIdentifier0 = queryResult[2];
      String name = queryResult[3];
      String description = queryResult[4];
      
      TargetEntity targetEntity = new TargetEntity();
      Map<String, TargetAttribute> attributes = new HashMap<String, TargetAttribute>();
      targetEntity.setId(id);
      targetEntity.setName(name);

      TargetAttribute targetAttributeDescription = new TargetAttribute();
      targetAttributeDescription.setName("description");
      targetAttributeDescription.setValue(description);
      attributes.put("description", targetAttributeDescription);
      
      TargetAttribute targetAttributeSubjectId = new TargetAttribute();
      targetAttributeSubjectId.setName("subjectId");
      targetAttributeSubjectId.setValue(subjectId);
      attributes.put("subjectId", targetAttributeSubjectId);
      
      TargetAttribute targetAttributeSubjectIdentifier0 = new TargetAttribute();
      targetAttributeSubjectIdentifier0.setName("subjectIdentifier0");
      targetAttributeSubjectIdentifier0.setValue(subjectIdentifier0);
      attributes.put("subjectIdentifier0", targetAttributeSubjectIdentifier0);
      
      targetEntity.setAttributes(attributes);
      
      results.put(id, targetEntity);
    }
    
    return results;
  }
  
  private Map<String, TargetMembership> getTargetMembershipMapFromQueryResults(List<String[]> queryResults) {
    
    Map<String, TargetMembership> results = new HashMap<String, TargetMembership>();

    for (String[] queryResult : queryResults) {
      String membershipId = queryResult[0];
      String groupId = queryResult[1];
      String memberId = queryResult[2];
      String subjectId = queryResult[3];
      String subjectIdentifier0 = queryResult[4];
      String name = queryResult[5];
      String description = queryResult[6];
      
      TargetMembership targetMembership = new TargetMembership();
      targetMembership.setId(membershipId);
      
      {
        TargetEntity targetEntity = new TargetEntity();
        Map<String, TargetAttribute> attributes = new HashMap<String, TargetAttribute>();
        targetEntity.setId(memberId);
        targetEntity.setName(name);
  
        TargetAttribute targetAttributeDescription = new TargetAttribute();
        targetAttributeDescription.setName("description");
        targetAttributeDescription.setValue(description);
        attributes.put("description", targetAttributeDescription);
        
        TargetAttribute targetAttributeSubjectId = new TargetAttribute();
        targetAttributeSubjectId.setName("subjectId");
        targetAttributeSubjectId.setValue(subjectId);
        attributes.put("subjectId", targetAttributeSubjectId);
        
        TargetAttribute targetAttributeSubjectIdentifier0 = new TargetAttribute();
        targetAttributeSubjectIdentifier0.setName("subjectIdentifier0");
        targetAttributeSubjectIdentifier0.setValue(subjectIdentifier0);
        attributes.put("subjectIdentifier0", targetAttributeSubjectIdentifier0);
        
        targetEntity.setAttributes(attributes);
        targetMembership.setTargetEntity(targetEntity);
      }
      
      {
        ProvisioningGroup targetGroup = new ProvisioningGroup();
        targetGroup.setId(groupId);
        targetMembership.setProvisioningGroup(targetGroup);
      }
      
      results.put(membershipId, targetMembership);
    }
    
    return results;
  }
}
