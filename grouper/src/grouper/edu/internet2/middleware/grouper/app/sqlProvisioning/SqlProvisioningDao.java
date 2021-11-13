package edu.internet2.middleware.grouper.app.sqlProvisioning;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfigurationAttribute;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningEntity;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningGroup;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningMembership;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningObjectChange;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningObjectChangeAction;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningUpdatable;
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
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveAllMembershipsRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveAllMembershipsResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveEntitiesRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveEntitiesResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveGroupsRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveGroupsResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveMembershipsBulkRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveMembershipsBulkResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoUpdateEntityRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoUpdateEntityResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoUpdateGroupRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoUpdateGroupResponse;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;


public class SqlProvisioningDao extends GrouperProvisionerTargetDaoBase {

  @Override
  public TargetDaoRetrieveAllMembershipsResponse retrieveAllMemberships(TargetDaoRetrieveAllMembershipsRequest targetDaoRetrieveAllMembershipsRequest) {
    List<ProvisioningMembership> targetMemberships = this.retrieveMemberships(true, null, null, null);
    return new TargetDaoRetrieveAllMembershipsResponse(targetMemberships);
  }

  public List<ProvisioningMembership> retrieveMemberships(boolean retrieveAll, List<ProvisioningGroup> grouperTargetGroups, 
      List<ProvisioningEntity> grouperTargetEntities, List<Object> grouperTargetMembershipsInput) {
    
    if (retrieveAll && (grouperTargetGroups != null || grouperTargetEntities != null || grouperTargetMembershipsInput != null)) {
      throw new RuntimeException("Cant retrieve all and pass in groups to retrieve");
    }
    
    SqlProvisioningConfiguration sqlProvisioningConfiguration = (SqlProvisioningConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
    
    String dbExternalSystemConfigId = sqlProvisioningConfiguration.getDbExternalSystemConfigId();

    String membershipGroupColumn = sqlProvisioningConfiguration.getMembershipGroupForeignKeyColumn();
    String membershipUserColumn = sqlProvisioningConfiguration.getMembershipEntityForeignKeyColumn();

    String membershipTableName = sqlProvisioningConfiguration.getMembershipTableName();
    
    List<ProvisioningMembership> result = new ArrayList<ProvisioningMembership>();

    List<ProvisioningMembership> grouperTargetMemberships = new ArrayList<ProvisioningMembership>();
    for (Object grouperTargetMembershipMultiKey : GrouperUtil.nonNull(grouperTargetMembershipsInput)) {
      ProvisioningMembership grouperTargetMembership = (ProvisioningMembership)grouperTargetMembershipMultiKey;
      if (grouperTargetMembership != null) {
        grouperTargetMemberships.add(grouperTargetMembership);
      }
    }
    
    if (StringUtils.isNotBlank(membershipTableName)) {

      Set<String> membershipAttributeNames = sqlProvisioningConfiguration.getTargetMembershipAttributeNameToConfig().keySet();
      
      List<String> membershipAttributeNamesList = new ArrayList<String>(membershipAttributeNames);
      
      StringBuilder commaSeparatedColumnNames = new StringBuilder();
      for (int i=0; i<membershipAttributeNamesList.size(); i++) {
        if (i>0) {
          commaSeparatedColumnNames.append(", ");
        }
        commaSeparatedColumnNames.append(membershipAttributeNamesList.get(i));
      }
      
//      String commaSeparatedAttributeNames = null;// TODO sqlProvisioningConfiguration.getMembershipAttributeNames();
      
      StringBuilder sqlInitial = new StringBuilder("select " + commaSeparatedColumnNames.toString() + " from "+membershipTableName);
      List<Object[]> membershipAttributeValues = null;

      String[] colNames = GrouperUtil.splitTrim(commaSeparatedColumnNames.toString(), ",");

      if (retrieveAll) {
        GcDbAccess gcDbAccess = new GcDbAccess().connectionName(dbExternalSystemConfigId);
        
        membershipAttributeValues = gcDbAccess.sql(sqlInitial.toString()).selectList(Object[].class);
        retrieveMembershipsAddRecord(result, membershipAttributeValues, colNames);
      } else {

        List<ProvisioningUpdatable> grouperTargetUpdatables = new ArrayList<ProvisioningUpdatable>();
        
        grouperTargetUpdatables.addAll(GrouperUtil.nonNull(grouperTargetGroups));
        grouperTargetUpdatables.addAll(GrouperUtil.nonNull(grouperTargetEntities));
        grouperTargetUpdatables.addAll(GrouperUtil.nonNull(grouperTargetMemberships));

        if (!retrieveAll && GrouperUtil.isBlank(grouperTargetMemberships)) {
          return result;
        }

        int numberOfBatches = GrouperUtil.batchNumberOfBatches(grouperTargetMemberships.size(), 450);
        for (int i = 0; i < numberOfBatches; i++) {
          List<ProvisioningMembership> currentBatchGrouperTargetMemberships = GrouperUtil.batchList(grouperTargetMemberships, 450, i);
          StringBuilder sql = new StringBuilder(sqlInitial);
          sql.append(" where ( ");
          GcDbAccess gcDbAccess = new GcDbAccess().connectionName(dbExternalSystemConfigId);
          
          for (int j=0; j<currentBatchGrouperTargetMemberships.size();j++) {
            ProvisioningMembership grouperTargetMembership = currentBatchGrouperTargetMemberships.get(j);
            gcDbAccess.addBindVar(((MultiKey)grouperTargetMembership.getMatchingId()).getKey(0));
            gcDbAccess.addBindVar(((MultiKey)grouperTargetMembership.getMatchingId()).getKey(1));
            if (j>0) {
              sql.append(" or ");
            }
            sql.append("  (" + membershipGroupColumn + " = ? && " + membershipUserColumn + " = ?) ");
          }
          sql.append(" ) ");
          membershipAttributeValues = gcDbAccess.sql(sql.toString()).selectList(Object[].class);
          retrieveMembershipsAddRecord(result, membershipAttributeValues, colNames);
          
        }
      }
    }
    
    return result;
   
  }

  protected void retrieveMembershipsAddRecord(List<ProvisioningMembership> result,
      List<Object[]> membershipAttributeValues, String[] colNames) {
    for (Object[] membershipAttributeValue: GrouperUtil.nonNull(membershipAttributeValues)) {
      ProvisioningMembership provisioningMembership = new ProvisioningMembership();
            
      for (int i=0; i<colNames.length; i++) {
        String colName = colNames[i];
        
        Object value = membershipAttributeValue[i];
        
        provisioningMembership.assignAttributeValue(colName, value);
      }
            
      result.add(provisioningMembership);
    }
  }

  @Override
  public TargetDaoUpdateGroupResponse updateGroup(TargetDaoUpdateGroupRequest targetDaoUpdateGroupRequest) {
    SqlProvisioningConfiguration sqlProvisioningConfiguration = (SqlProvisioningConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
    
    String dbExternalSystemConfigId = sqlProvisioningConfiguration.getDbExternalSystemConfigId();
    
    String groupTableName = sqlProvisioningConfiguration.getGroupTableName();

    String groupTableIdColumn = sqlProvisioningConfiguration.getGroupTableIdColumn();
    
    ProvisioningGroup targetGroup = targetDaoUpdateGroupRequest.getTargetGroup();
    
    if (!StringUtils.isBlank(groupTableName)) {
      
      GcDbAccess gcDbAccess = new GcDbAccess().connectionName(dbExternalSystemConfigId);
      
      List<String> columnsToUpdate = new ArrayList<String>();
      List<Object> valuesToUpdate = new ArrayList<Object>();
      
      StringBuilder commaSeparatedColumnNames = new StringBuilder();
      StringBuilder commaSeparatedQuestionMarks = new StringBuilder();

      int i = 0;
      
      for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetGroup.getInternal_objectChanges())) {
        if (provisioningObjectChange.getProvisioningObjectChangeAction() == ProvisioningObjectChangeAction.update) {
          String attributeName = provisioningObjectChange.getAttributeName();
          columnsToUpdate.add(attributeName);
          valuesToUpdate.add(provisioningObjectChange.getNewValue());
          
          if (i>0) {
            commaSeparatedColumnNames.append(", ");
            commaSeparatedQuestionMarks.append(", ");
          }
          commaSeparatedColumnNames.append(attributeName);
          commaSeparatedQuestionMarks.append(" ? ");
          i++;
          
        }
      }
      
      if (columnsToUpdate.size() == 0) {
        return new TargetDaoUpdateGroupResponse();
      }
      
      StringBuilder sql = new StringBuilder("update "+groupTableName + " set ");
      
      for (int j = 0; j<columnsToUpdate.size(); j++) {
        
        if (j > 0) {
          sql.append(", ");
        }
        
        sql.append(columnsToUpdate.get(j));
        sql.append(" = ");
        sql.append(" ? ");
        
      }
      
      sql.append(" where "+ groupTableIdColumn + " = ? ");
      
      
      gcDbAccess.sql(sql.toString());
      
      for (Object valueToUpdate: valuesToUpdate) {
        gcDbAccess.addBindVar(valueToUpdate);
      }
      
      gcDbAccess.addBindVar(targetGroup.getId());

      gcDbAccess.executeSql();
      
    }  
    
    return new TargetDaoUpdateGroupResponse();

  }
  
  @Override
  public TargetDaoUpdateEntityResponse updateEntity(TargetDaoUpdateEntityRequest targetDaoUpdateEntityRequest) {
    SqlProvisioningConfiguration sqlProvisioningConfiguration = (SqlProvisioningConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
    
    String dbExternalSystemConfigId = sqlProvisioningConfiguration.getDbExternalSystemConfigId();
    
    String entityTableName = sqlProvisioningConfiguration.getEntityTableName();
    
    ProvisioningEntity targetEntity = targetDaoUpdateEntityRequest.getTargetEntity();

    String entityTableIdColumn = sqlProvisioningConfiguration.getEntityTableIdColumn();
    
    if (!StringUtils.isBlank(entityTableName)) {
      
      GcDbAccess gcDbAccess = new GcDbAccess().connectionName(dbExternalSystemConfigId);
      
      List<String> columnsToUpdate = new ArrayList<String>();
      List<Object> valuesToUpdate = new ArrayList<Object>();
      
      StringBuilder commaSeparatedColumnNames = new StringBuilder();
      StringBuilder commaSeparatedQuestionMarks = new StringBuilder();

      int i = 0;
      
      for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetEntity.getInternal_objectChanges())) {
        if (provisioningObjectChange.getProvisioningObjectChangeAction() == ProvisioningObjectChangeAction.update) {
          String attributeName = provisioningObjectChange.getAttributeName();
          columnsToUpdate.add(attributeName);
          valuesToUpdate.add(provisioningObjectChange.getNewValue());
          
          if (i>0) {
            commaSeparatedColumnNames.append(", ");
            commaSeparatedQuestionMarks.append(", ");
          }
          commaSeparatedColumnNames.append(attributeName);
          commaSeparatedQuestionMarks.append(" ? ");
          i++;
          
        }
      }
      
      if (columnsToUpdate.size() == 0) {
        return new TargetDaoUpdateEntityResponse();
      }
      
      StringBuilder sql = new StringBuilder("update "+entityTableName + " set ");
      
      for (int j = 0; j<columnsToUpdate.size(); j++) {
        
        if (j > 0) {
          sql.append(", ");
        }
        
        sql.append(columnsToUpdate.get(j));
        sql.append(" = ");
        sql.append(" ? ");
        
      }
      
      sql.append(" where "+ entityTableIdColumn + " = ? ");
      
      gcDbAccess.sql(sql.toString());
      
      for (Object valueToUpdate: valuesToUpdate) {
        gcDbAccess.addBindVar(valueToUpdate);
      }
      
      gcDbAccess.addBindVar(targetEntity.getId());

      gcDbAccess.executeSql();
      
    }  
    
    return new TargetDaoUpdateEntityResponse();

  }

  @Override
  public TargetDaoDeleteGroupsResponse deleteGroups(TargetDaoDeleteGroupsRequest targetDaoDeleteGroupRequest) {
    
    List<ProvisioningGroup> targetGroups = targetDaoDeleteGroupRequest.getTargetGroups();
    
    SqlProvisioningConfiguration sqlProvisioningConfiguration = (SqlProvisioningConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
    
    String dbExternalSystemConfigId = sqlProvisioningConfiguration.getDbExternalSystemConfigId();
    
    String groupTableName = sqlProvisioningConfiguration.getGroupTableName();
    
    if (!StringUtils.isBlank(groupTableName)) {
      
      StringBuilder sql = new StringBuilder("delete from  " + groupTableName);
      List<List<Object>> batchBindVars = new ArrayList<List<Object>>();
      
      List<GrouperProvisioningConfigurationAttribute> grouperProvisioningConfigurationAttributes = sqlProvisioningConfiguration.getGroupSearchAttributes();
      if (grouperProvisioningConfigurationAttributes.size() > 1) {
        //TODO add this to validation
        throw new RuntimeException("Can currently only have one searchAttribute! " + grouperProvisioningConfigurationAttributes);
      }
      
      if (grouperProvisioningConfigurationAttributes.size() == 1) {
        
        GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute = grouperProvisioningConfigurationAttributes.get(0);
        sql.append(" where "+ grouperProvisioningConfigurationAttribute.getName() + " = ? ");
        
        for (ProvisioningGroup targetGroup: targetGroups) {
          String value = targetGroup.retrieveFieldOrAttributeValueString(grouperProvisioningConfigurationAttribute);
          batchBindVars.add(GrouperUtil.toListObject(value));
        }
        
      } else {
        throw new RuntimeException("Why is groupSearchFilter empty?");
      }
      
      int[] counts = new GcDbAccess().connectionName(dbExternalSystemConfigId).sql(sql.toString()).batchBindVars(batchBindVars).executeBatchSql();

      for (int i=0; i<counts.length; i++) {
        
        if(counts[i] == 1) {
          targetGroups.get(i).setProvisioned(true);
        }
      }
  
    } else {
      throw new RuntimeException("Need group table name");
    }
    return new TargetDaoDeleteGroupsResponse();
  }
  

  @Override
  public TargetDaoDeleteEntitiesResponse deleteEntities(TargetDaoDeleteEntitiesRequest targetDaoDeleteEntitiesRequest) {

    List<ProvisioningEntity> targetEntities = targetDaoDeleteEntitiesRequest.getTargetEntities();
    
    SqlProvisioningConfiguration sqlProvisioningConfiguration = (SqlProvisioningConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
    
    String dbExternalSystemConfigId = sqlProvisioningConfiguration.getDbExternalSystemConfigId();
    
    String entityTableName = sqlProvisioningConfiguration.getEntityTableName();
    
    if (!StringUtils.isBlank(entityTableName)) {
      
      StringBuilder sql = new StringBuilder("delete from  " + entityTableName);
      List<List<Object>> batchBindVars = new ArrayList<List<Object>>();
      
      List<GrouperProvisioningConfigurationAttribute> grouperProvisioningConfigurationAttributes = sqlProvisioningConfiguration.getEntitySearchAttributes();
      if (grouperProvisioningConfigurationAttributes.size() > 1) {
        //TODO add this to validation
        throw new RuntimeException("Can currently only have one searchAttribute! " + grouperProvisioningConfigurationAttributes);
      }
      
      if (grouperProvisioningConfigurationAttributes.size() == 1) {
        
        GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute = grouperProvisioningConfigurationAttributes.get(0);
        sql.append(" where "+ grouperProvisioningConfigurationAttribute.getName() + " = ? ");
        
        for (ProvisioningEntity targetEntity: targetEntities) {
          String value = targetEntity.retrieveFieldOrAttributeValueString(grouperProvisioningConfigurationAttribute);
          batchBindVars.add(GrouperUtil.toListObject(value));
        }
        
      } else {
        throw new RuntimeException("Why is entitySearchFilter empty?");
      }
      
      int[] counts = new GcDbAccess().connectionName(dbExternalSystemConfigId).sql(sql.toString()).batchBindVars(batchBindVars).executeBatchSql();

      for (int i=0; i<counts.length; i++) {
        
        if(counts[i] == 1) {
          targetEntities.get(i).setProvisioned(true);
        }
      }
  
    } else {
      throw new RuntimeException("Need entity table name");
    }
    return new TargetDaoDeleteEntitiesResponse();
    
  }

  @Override
  public TargetDaoDeleteMembershipsResponse deleteMemberships(TargetDaoDeleteMembershipsRequest targetDaoDeleteMembershipsRequest) {
    
    List<ProvisioningMembership> targetMemberships = targetDaoDeleteMembershipsRequest.getTargetMemberships();
    
    SqlProvisioningConfiguration sqlProvisioningConfiguration = (SqlProvisioningConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
    
    String dbExternalSystemConfigId = sqlProvisioningConfiguration.getDbExternalSystemConfigId();
    
    String membershipTableName = sqlProvisioningConfiguration.getMembershipTableName();
    
    String entityIdForeignKeyColumn = sqlProvisioningConfiguration.getMembershipEntityForeignKeyColumn();
    
    String groupIdForeignKeyColumn = sqlProvisioningConfiguration.getMembershipGroupForeignKeyColumn();
    
    if (!StringUtils.isBlank(membershipTableName)) {
      
      List<List<Object>> batchBindVars = new ArrayList<List<Object>>();
      
      StringBuilder sql = new StringBuilder("delete from "+membershipTableName + " where "+ entityIdForeignKeyColumn + " = ? and "+ groupIdForeignKeyColumn + " = ? ");
      
      for (ProvisioningMembership targetMembership: targetMemberships) {
        
        batchBindVars.add(GrouperUtil.toListObject(targetMembership.retrieveAttributeValue(entityIdForeignKeyColumn), targetMembership.retrieveAttributeValue(groupIdForeignKeyColumn)));
      }
        
      int[] counts = new GcDbAccess().connectionName(dbExternalSystemConfigId).sql(sql.toString()).batchBindVars(batchBindVars).executeBatchSql();

      for (int i=0; i<counts.length; i++) {
        
        if(counts[i] == 1) {
          targetMemberships.get(i).setProvisioned(true);
        }
      }
  
    } else {
      throw new RuntimeException("Need membership table name");
    }
    return new TargetDaoDeleteMembershipsResponse();
  }
  
  @Override
  public TargetDaoInsertMembershipsResponse insertMemberships(TargetDaoInsertMembershipsRequest targetDaoInsertMembershipsRequest) {
    
    List<ProvisioningMembership> targetMemberships = targetDaoInsertMembershipsRequest.getTargetMemberships();
    
    SqlProvisioningConfiguration sqlProvisioningConfiguration = (SqlProvisioningConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
    
    String dbExternalSystemConfigId = sqlProvisioningConfiguration.getDbExternalSystemConfigId();
    
    String membershipTableName = sqlProvisioningConfiguration.getMembershipTableName();
    
    Map<String, GrouperProvisioningConfigurationAttribute> attributeNameToConfig = sqlProvisioningConfiguration.getTargetMembershipAttributeNameToConfig();
    
    
    if (!StringUtils.isBlank(membershipTableName)) {
      
      List<String> columnsToInsert = new ArrayList<String>();
      
      for (String attributeName: attributeNameToConfig.keySet()) {
        GrouperProvisioningConfigurationAttribute configurationAttribute = attributeNameToConfig.get(attributeName);
        if (configurationAttribute.isInsert()) {
          columnsToInsert.add(attributeName);
        }
      }
      
      String commaSeparatedColNames = StringUtils.join(columnsToInsert, ",");
      String commaSeparatedQuestionMarks = GrouperClientUtils.appendQuestions(columnsToInsert.size());
      
      
      List<List<Object>> batchBindVars = new ArrayList<List<Object>>();
      
      
      String sql = "insert into " + membershipTableName + "(" + commaSeparatedColNames + ") values ("+commaSeparatedQuestionMarks+")";
      
      for (ProvisioningMembership targetMembership: targetMemberships) {
        
        Map<String, Object> attributeNameToValue = new HashMap<String, Object>();
        
        for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetMembership.getInternal_objectChanges())) {
          if (provisioningObjectChange.getProvisioningObjectChangeAction() == ProvisioningObjectChangeAction.insert) {
            String attributeName = provisioningObjectChange.getAttributeName();
          
            attributeNameToValue.put(attributeName, provisioningObjectChange.getNewValue());
            
          }
        }
        
        List<Object> bindVars = new ArrayList<Object>();
        batchBindVars.add(bindVars);
        
        for (String colName: columnsToInsert) {
          bindVars.add(attributeNameToValue.get(colName));
        }
        
      }
      
      int[] counts = new GcDbAccess().connectionName(dbExternalSystemConfigId).sql(sql.toString()).batchBindVars(batchBindVars).executeBatchSql();

      for (int i=0; i<counts.length; i++) {
        
        if(counts[i] == 1) {
          targetMemberships.get(i).setProvisioned(true);
        }
      }
      
    }
    
    return new TargetDaoInsertMembershipsResponse();
    

  }
  
  @Override
  public TargetDaoInsertGroupsResponse insertGroups(TargetDaoInsertGroupsRequest targetDaoInsertGroupsRequest) {
    
    List<ProvisioningGroup> targetGroups = targetDaoInsertGroupsRequest.getTargetGroups();
    
    SqlProvisioningConfiguration sqlProvisioningConfiguration = (SqlProvisioningConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
    
    String dbExternalSystemConfigId = sqlProvisioningConfiguration.getDbExternalSystemConfigId();
    
    String groupTableName = sqlProvisioningConfiguration.getGroupTableName();
    
    Map<String, GrouperProvisioningConfigurationAttribute> attributeNameToConfig = sqlProvisioningConfiguration.getTargetGroupAttributeNameToConfig();
    
    
    if (!StringUtils.isBlank(groupTableName)) {
      
      List<String> columnsToInsert = new ArrayList<String>();
      
      for (String attributeName: attributeNameToConfig.keySet()) {
        GrouperProvisioningConfigurationAttribute configurationAttribute = attributeNameToConfig.get(attributeName);
        if (configurationAttribute.isInsert()) {
          columnsToInsert.add(attributeName);
        }
      }
      
      String commaSeparatedColNames = StringUtils.join(columnsToInsert, ",");
      String commaSeparatedQuestionMarks = GrouperClientUtils.appendQuestions(columnsToInsert.size());
      
      
      List<List<Object>> batchBindVars = new ArrayList<List<Object>>();
      
      
      String sql = "insert into " + groupTableName + "(" + commaSeparatedColNames + ") values ("+commaSeparatedQuestionMarks+")";
      
      for (ProvisioningGroup targetGroup: targetGroups) {
        
        Map<String, Object> attributeNameToValue = new HashMap<String, Object>();
        
        for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetGroup.getInternal_objectChanges())) {
          if (provisioningObjectChange.getProvisioningObjectChangeAction() == ProvisioningObjectChangeAction.insert) {
            String attributeName = provisioningObjectChange.getAttributeName();
          
            attributeNameToValue.put(attributeName, provisioningObjectChange.getNewValue());
            
          }
        }
        
        List<Object> bindVars = new ArrayList<Object>();
        batchBindVars.add(bindVars);
        
        for (String colName: columnsToInsert) {
          bindVars.add(attributeNameToValue.get(colName));
        }
        
      }
      
      int[] counts = new GcDbAccess().connectionName(dbExternalSystemConfigId).sql(sql.toString()).batchBindVars(batchBindVars).executeBatchSql();

      for (int i=0; i<counts.length; i++) {
        
        if(counts[i] == 1) {
          targetGroups.get(i).setProvisioned(true);
        }
      }
      
    }
    
    return new TargetDaoInsertGroupsResponse();
    
  }
  
  @Override
  public TargetDaoInsertEntitiesResponse insertEntities(TargetDaoInsertEntitiesRequest targetDaoInsertEntitiesRequest) {
    
    List<ProvisioningEntity> targetEntities = targetDaoInsertEntitiesRequest.getTargetEntityInserts();
    
    SqlProvisioningConfiguration sqlProvisioningConfiguration = (SqlProvisioningConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
    
    String dbExternalSystemConfigId = sqlProvisioningConfiguration.getDbExternalSystemConfigId();
    
    String entityTableName = sqlProvisioningConfiguration.getEntityTableName();
    
    Map<String, GrouperProvisioningConfigurationAttribute> attributeNameToConfig = sqlProvisioningConfiguration.getTargetEntityAttributeNameToConfig();
    
    
    if (!StringUtils.isBlank(entityTableName)) {
      
      List<String> columnsToInsert = new ArrayList<String>();
      
      for (String attributeName: attributeNameToConfig.keySet()) {
        GrouperProvisioningConfigurationAttribute configurationAttribute = attributeNameToConfig.get(attributeName);
        if (configurationAttribute.isInsert()) {
          columnsToInsert.add(attributeName);
        }
      }
      
      String commaSeparatedColNames = StringUtils.join(columnsToInsert, ",");
      String commaSeparatedQuestionMarks = GrouperClientUtils.appendQuestions(columnsToInsert.size());
      
      
      List<List<Object>> batchBindVars = new ArrayList<List<Object>>();
      
      
      String sql = "insert into " + entityTableName + "(" + commaSeparatedColNames + ") values ("+commaSeparatedQuestionMarks+")";
      
      for (ProvisioningEntity targetEntity: targetEntities) {
        
        Map<String, Object> attributeNameToValue = new HashMap<String, Object>();
        
        for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetEntity.getInternal_objectChanges())) {
          if (provisioningObjectChange.getProvisioningObjectChangeAction() == ProvisioningObjectChangeAction.insert) {
            String attributeName = provisioningObjectChange.getAttributeName();
          
            attributeNameToValue.put(attributeName, provisioningObjectChange.getNewValue());
            
          }
        }
        
        List<Object> bindVars = new ArrayList<Object>();
        batchBindVars.add(bindVars);
        
        for (String colName: columnsToInsert) {
          bindVars.add(attributeNameToValue.get(colName));
        }
        
      }
      
      int[] counts = new GcDbAccess().connectionName(dbExternalSystemConfigId).sql(sql.toString()).batchBindVars(batchBindVars).executeBatchSql();

      for (int i=0; i<counts.length; i++) {
        
        if(counts[i] == 1) {
          targetEntities.get(i).setProvisioned(true);
        }
      }
      
    }
    
    return new TargetDaoInsertEntitiesResponse();
    

  }

  @Override
  public TargetDaoRetrieveAllGroupsResponse retrieveAllGroups(TargetDaoRetrieveAllGroupsRequest targetDaoRetrieveAllGroupsRequest) {
    boolean includeAllMembershipsIfApplicable = targetDaoRetrieveAllGroupsRequest == null ? false: targetDaoRetrieveAllGroupsRequest.isIncludeAllMembershipsIfApplicable();
    List<ProvisioningGroup> targetGroups = retrieveGroups(true, null, includeAllMembershipsIfApplicable);
    return new TargetDaoRetrieveAllGroupsResponse(targetGroups);
  }

  private List<ProvisioningGroup> retrieveGroups(boolean retrieveAll, List<ProvisioningGroup> grouperTargetGroups,
      boolean retrieveAllMembershipsInGroups) {
    
    if (retrieveAll && grouperTargetGroups != null) {
      throw new RuntimeException("Cant retrieve all and pass in groups to retrieve");
    }
    
    SqlProvisioningConfiguration sqlProvisioningConfiguration = (SqlProvisioningConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
    
    String dbExternalSystemConfigId = sqlProvisioningConfiguration.getDbExternalSystemConfigId();
    
    String groupTableName = sqlProvisioningConfiguration.getGroupTableName();
    
    List<ProvisioningGroup> result = new ArrayList<ProvisioningGroup>();
    
    if (!retrieveAll && GrouperUtil.isBlank(grouperTargetGroups)) {
      return result;
    }
    
    if (!StringUtils.isBlank(groupTableName)) {

        Set<String> groupTableColumnNames = sqlProvisioningConfiguration.getTargetGroupAttributeNameToConfig().keySet();
        
        List<String> groupTableColNamesList = new ArrayList<String>(groupTableColumnNames);
        
        StringBuilder commaSeparatedColumnNames = new StringBuilder();
        for (int i=0; i<groupTableColNamesList.size(); i++) {
          if (i>0) {
            commaSeparatedColumnNames.append(", ");
          }
          commaSeparatedColumnNames.append(groupTableColNamesList.get(i));
        }
        
        String[] colNames = GrouperUtil.splitTrim(commaSeparatedColumnNames.toString(), ",");
        
        StringBuilder sqlInitial = new StringBuilder("select " + commaSeparatedColumnNames.toString() + " from " + groupTableName);
        List<Object[]> groupAttributeValues = null;
        
        if (retrieveAll) {
          GcDbAccess gcDbAccess = new GcDbAccess().connectionName(dbExternalSystemConfigId);
          
          groupAttributeValues = gcDbAccess.sql(sqlInitial.toString()).selectList(Object[].class);
          
          retrieveGroupsAddRecord(result, colNames, groupAttributeValues);
        } else {
          
          List<GrouperProvisioningConfigurationAttribute> grouperProvisioningConfigurationAttributes = sqlProvisioningConfiguration.getGroupSearchAttributes();
          if (grouperProvisioningConfigurationAttributes.size() > 1) {
            //TODO add this to validation
            throw new RuntimeException("Can currently only have one searchAttribute! " + grouperProvisioningConfigurationAttributes);
          }
          
          if (grouperProvisioningConfigurationAttributes.size() == 1) {
            
            int numberOfBatches = GrouperUtil.batchNumberOfBatches(grouperTargetGroups.size(), 900);
            GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute = grouperProvisioningConfigurationAttributes.get(0);
            
            for (int i = 0; i < numberOfBatches; i++) {
              
              List<ProvisioningGroup> currentBatchGrouperTargetGroups = GrouperUtil.batchList(grouperTargetGroups, 900, i);
              StringBuilder sql = new StringBuilder(sqlInitial);
              
              sql.append(" where "+ grouperProvisioningConfigurationAttribute.getName() + " in ( ");
              
              GcDbAccess gcDbAccess = new GcDbAccess().connectionName(dbExternalSystemConfigId);
              
              for (int j=0; j<currentBatchGrouperTargetGroups.size();j++) {
                ProvisioningGroup grouperTargetGroup = currentBatchGrouperTargetGroups.get(j);
                String value = grouperTargetGroup.retrieveFieldOrAttributeValueString(grouperProvisioningConfigurationAttribute);
                gcDbAccess.addBindVar(value);
                if (j>0) {
                  sql.append(",");
                }
                sql.append("?");
              }
              sql.append(" ) ");
              groupAttributeValues = gcDbAccess.sql(sql.toString()).selectList(Object[].class);
              retrieveGroupsAddRecord(result, colNames, groupAttributeValues);
              
            }
            
          } else {
            throw new RuntimeException("Why is groupSearchFilter empty?");
          }
          
        }

    }
    
    return result;
   
  }

  public void retrieveGroupsAddRecord(List<ProvisioningGroup> result, String[] colNames, List<Object[]> groupAttributeValues) {
    for (Object[] groupAttributeValue: GrouperUtil.nonNull(groupAttributeValues)) {
      ProvisioningGroup provisioningGroup = new ProvisioningGroup();
 
      for (int i=0; i<colNames.length; i++) {
        String colName = colNames[i];
 
        Object value = groupAttributeValue[i];
        provisioningGroup.assignAttributeValue(colName, value);
                
      }
            
      result.add(provisioningGroup);
    }
  }
  
  public void retrieveEntitiesAddRecord(List<ProvisioningEntity> result, String[] colNames, List<Object[]> entityAttributeValues) {
    
    for (Object[] entityAttributeValue: GrouperUtil.nonNull(entityAttributeValues)) {
      ProvisioningEntity provisioningEntity = new ProvisioningEntity();
 
      for (int i=0; i<colNames.length; i++) {
        String colName = colNames[i];
 
        Object value = entityAttributeValue[i];
        provisioningEntity.assignAttributeValue(colName, value);
        
      }
            
      result.add(provisioningEntity);
    }
  }

  @Override
  public TargetDaoRetrieveGroupsResponse retrieveGroups(TargetDaoRetrieveGroupsRequest targetDaoRetrieveGroupsRequest) {
    List<ProvisioningGroup> grouperTargetGroups = targetDaoRetrieveGroupsRequest == null ? null : targetDaoRetrieveGroupsRequest.getTargetGroups();
    boolean retrieveAllMembershipsInGroups = targetDaoRetrieveGroupsRequest == null ? false : targetDaoRetrieveGroupsRequest.isIncludeAllMembershipsIfApplicable();
    List<ProvisioningGroup> targetGroups = this.retrieveGroups(false, grouperTargetGroups, retrieveAllMembershipsInGroups);
    return new TargetDaoRetrieveGroupsResponse(targetGroups);
  }

  @Override
  public TargetDaoRetrieveMembershipsBulkResponse retrieveMembershipsBulk(TargetDaoRetrieveMembershipsBulkRequest targetDaoRetrieveMembershipsBulkRequest) {
    List<ProvisioningGroup> grouperTargetGroups = targetDaoRetrieveMembershipsBulkRequest == null ? null : targetDaoRetrieveMembershipsBulkRequest.getTargetGroupsForAllMemberships();
    List<ProvisioningEntity> grouperTargetEntities = targetDaoRetrieveMembershipsBulkRequest == null ? null : targetDaoRetrieveMembershipsBulkRequest.getTargetEntitiesForAllMemberships();
    List<Object> grouperTargetMemberships = targetDaoRetrieveMembershipsBulkRequest == null ? null : targetDaoRetrieveMembershipsBulkRequest.getTargetMemberships();
    List<ProvisioningMembership> targetMemberships = this.retrieveMemberships(false, grouperTargetGroups, grouperTargetEntities, grouperTargetMemberships);
    List<Object> targetMembershipsObjects = new ArrayList<Object>();
    targetMembershipsObjects.addAll(targetMemberships);
    return new TargetDaoRetrieveMembershipsBulkResponse(targetMembershipsObjects);
  }

  @Override
  public TargetDaoRetrieveAllEntitiesResponse retrieveAllEntities(TargetDaoRetrieveAllEntitiesRequest targetDaoRetrieveAllEntitiesRequest) {
    
    SqlProvisioningConfiguration sqlProvisioningConfiguration = (SqlProvisioningConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
    
    String dbExternalSystemConfigId = sqlProvisioningConfiguration.getDbExternalSystemConfigId();
    
    String entityTableName = sqlProvisioningConfiguration.getEntityTableName();
    
    List<ProvisioningEntity> result = new ArrayList<ProvisioningEntity>();
    
    if (StringUtils.isNotBlank(entityTableName)) {

        String entityTableIdColumn = sqlProvisioningConfiguration.getEntityTableIdColumn();
        
        Set<String> entityTableColumnNames = sqlProvisioningConfiguration.getTargetEntityAttributeNameToConfig().keySet();
        
        List<String> entityTableColNamesList = new ArrayList<String>(entityTableColumnNames);
        
        StringBuilder commaSeparatedColumnNames = new StringBuilder();
        for (int i=0; i<entityTableColNamesList.size(); i++) {
          if (i>0) {
            commaSeparatedColumnNames.append(", ");
          }
          commaSeparatedColumnNames.append(entityTableColNamesList.get(i));
        }
        
        String[] colNames = GrouperUtil.splitTrim(commaSeparatedColumnNames.toString(), ",");
        
        StringBuilder sqlInitial = new StringBuilder("select " + commaSeparatedColumnNames.toString() + " from " + entityTableName);
        List<Object[]> entityAttributeValues = null;
        
        GcDbAccess gcDbAccess = new GcDbAccess().connectionName(dbExternalSystemConfigId);
        
        entityAttributeValues = gcDbAccess.sql(sqlInitial.toString()).selectList(Object[].class);
        
        retrieveEntitiesAddRecord(result, colNames, entityAttributeValues);

    }
    
    return new TargetDaoRetrieveAllEntitiesResponse(result);
    
  }
  

  @Override
  public TargetDaoRetrieveEntitiesResponse retrieveEntities(TargetDaoRetrieveEntitiesRequest targetDaoRetrieveEntitiesRequest) {
    
    SqlProvisioningConfiguration sqlProvisioningConfiguration = (SqlProvisioningConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
    
    String dbExternalSystemConfigId = sqlProvisioningConfiguration.getDbExternalSystemConfigId();
    
    String entityTableName = sqlProvisioningConfiguration.getEntityTableName();
    
    List<ProvisioningEntity> result = new ArrayList<ProvisioningEntity>();
    
    List<ProvisioningEntity> targetEntities = targetDaoRetrieveEntitiesRequest.getTargetEntities();
    
    if (!StringUtils.isBlank(entityTableName)) {

        Set<String> entityTableColumnNames = sqlProvisioningConfiguration.getTargetEntityAttributeNameToConfig().keySet();
        
        List<String> entityTableColNamesList = new ArrayList<String>(entityTableColumnNames);
        
        StringBuilder commaSeparatedColumnNames = new StringBuilder();
        for (int i=0; i<entityTableColNamesList.size(); i++) {
          if (i>0) {
            commaSeparatedColumnNames.append(", ");
          }
          commaSeparatedColumnNames.append(entityTableColNamesList.get(i));
        }
        
        String[] colNames = GrouperUtil.splitTrim(commaSeparatedColumnNames.toString(), ",");
        
        StringBuilder sqlInitial = new StringBuilder("select " + commaSeparatedColumnNames.toString() + " from " + entityTableName);
        List<Object[]> entityAttributeValues = null;
          
        List<GrouperProvisioningConfigurationAttribute> grouperProvisioningConfigurationAttributes = sqlProvisioningConfiguration.getEntitySearchAttributes();
        if (grouperProvisioningConfigurationAttributes.size() > 1) {
          //TODO add this to validation
          throw new RuntimeException("Can currently only have one searchAttribute! " + grouperProvisioningConfigurationAttributes);
        }
        
        if (grouperProvisioningConfigurationAttributes.size() == 1) {
          
          int numberOfBatches = GrouperUtil.batchNumberOfBatches(targetEntities.size(), 900);
          GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute = grouperProvisioningConfigurationAttributes.get(0);
          
          for (int i = 0; i < numberOfBatches; i++) {
            
            List<ProvisioningEntity> currentBatchGrouperTargetEntities = GrouperUtil.batchList(targetEntities, 900, i);
            StringBuilder sql = new StringBuilder(sqlInitial);
            
            sql.append(" where "+ grouperProvisioningConfigurationAttribute.getName() + " in ( ");
            
            GcDbAccess gcDbAccess = new GcDbAccess().connectionName(dbExternalSystemConfigId);
            
            for (int j=0; j<currentBatchGrouperTargetEntities.size();j++) {
              ProvisioningEntity grouperTargetEntity = currentBatchGrouperTargetEntities.get(j);
              String value = grouperTargetEntity.retrieveFieldOrAttributeValueString(grouperProvisioningConfigurationAttribute);
              gcDbAccess.addBindVar(value);
              if (j>0) {
                sql.append(",");
              }
              sql.append("?");
            }
            sql.append(" ) ");
            entityAttributeValues = gcDbAccess.sql(sql.toString()).selectList(Object[].class);
            retrieveEntitiesAddRecord(result, colNames, entityAttributeValues);
            
          }
          
        } else {
          throw new RuntimeException("Why is groupSearchFilter empty?");
        }
          
    }
    
    return new TargetDaoRetrieveEntitiesResponse(result);
    
  }


  @Override
  public void registerGrouperProvisionerDaoCapabilities(GrouperProvisionerDaoCapabilities grouperProvisionerDaoCapabilities) {
    
    grouperProvisionerDaoCapabilities.setCanDeleteGroups(true);
    grouperProvisionerDaoCapabilities.setCanDeleteMemberships(true);
    grouperProvisionerDaoCapabilities.setCanDeleteEntities(true);
    
    grouperProvisionerDaoCapabilities.setCanInsertEntities(true);
    grouperProvisionerDaoCapabilities.setCanInsertGroups(true);
    grouperProvisionerDaoCapabilities.setCanInsertMemberships(true);
    
    grouperProvisionerDaoCapabilities.setCanRetrieveAllEntities(true);
    grouperProvisionerDaoCapabilities.setCanRetrieveAllGroups(true);
    grouperProvisionerDaoCapabilities.setCanRetrieveAllMemberships(true);
    
    grouperProvisionerDaoCapabilities.setCanRetrieveEntities(true);
    grouperProvisionerDaoCapabilities.setCanRetrieveGroups(true);
    grouperProvisionerDaoCapabilities.setCanRetrieveMemberships(true);
    grouperProvisionerDaoCapabilities.setCanUpdateGroup(true);
    grouperProvisionerDaoCapabilities.setCanUpdateEntity(true);
    
  }

}
