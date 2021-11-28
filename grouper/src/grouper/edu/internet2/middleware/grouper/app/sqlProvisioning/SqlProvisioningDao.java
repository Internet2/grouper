package edu.internet2.middleware.grouper.app.sqlProvisioning;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfigurationAttribute;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningAttribute;
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
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoUpdateEntitiesRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoUpdateEntitiesResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoUpdateEntityRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoUpdateEntityResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoUpdateGroupRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoUpdateGroupResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoUpdateGroupsRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoUpdateGroupsResponse;
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
  public TargetDaoUpdateGroupsResponse updateGroups(TargetDaoUpdateGroupsRequest targetDaoUpdateGroupsRequest) {
    
    SqlProvisioningConfiguration sqlProvisioningConfiguration = (SqlProvisioningConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
    
    String dbExternalSystemConfigId = sqlProvisioningConfiguration.getDbExternalSystemConfigId();
    
    String groupTableName = sqlProvisioningConfiguration.getGroupTableName();

    String groupTableIdColumn = sqlProvisioningConfiguration.getGroupTableIdColumn();
    
    String groupAttributesTableName = sqlProvisioningConfiguration.getGroupAttributesTableName();

    String groupAttributesGroupForeignKeyColumn = sqlProvisioningConfiguration.getGroupAttributesGroupForeignKeyColumn();
    
    String groupAttributesAttributeNameColumn = sqlProvisioningConfiguration.getGroupAttributesAttributeNameColumn();

    String groupAttributesAttributeValueColumn = sqlProvisioningConfiguration.getGroupAttributesAttributeValueColumn();

    String groupAttributesLastModifiedColumn = sqlProvisioningConfiguration.getGroupAttributesLastModifiedColumn();
    
    Map<String, GrouperProvisioningConfigurationAttribute> attributeNameToConfig = sqlProvisioningConfiguration.getTargetGroupAttributeNameToConfig();
    
    List<ProvisioningGroup> targetGroups = targetDaoUpdateGroupsRequest.getTargetGroups();
    
    GcDbAccess gcDbAccess = new GcDbAccess().connectionName(dbExternalSystemConfigId);
    
    List<String> primaryColumnsToUpdate = new ArrayList<String>();
    
    for (String attributeName: attributeNameToConfig.keySet()) {
      SqlGrouperProvisioningConfigurationAttribute configurationAttribute = (SqlGrouperProvisioningConfigurationAttribute) attributeNameToConfig.get(attributeName);
      
      configurationAttribute.getTranslateToGroupSyncField();
      
      if (configurationAttribute.isUpdate()) {
        if (StringUtils.isBlank(configurationAttribute.getStorageType()) || StringUtils.equals(configurationAttribute.getStorageType(), "groupTableColumn")) {
          primaryColumnsToUpdate.add(attributeName);
        }
      }
      
    }
    
    List<List<Object>> batchBindVarsForPrimaryTable = new ArrayList<List<Object>>();
    List<List<Object>> batchUpdateBindVarsForAttributesTable = new ArrayList<List<Object>>();
    List<List<Object>> batchInsertBindVarsForAttributesTable = new ArrayList<List<Object>>();
    List<List<Object>> batchDeleteBindVarsForAttributesTable = new ArrayList<List<Object>>();
    
    for (ProvisioningGroup targetGroup: targetGroups) {
      
      Map<String, ProvisioningAttribute> targetGroupAttributes = targetGroup.getAttributes();
      
      Map<String, Object> attributeNamesToValues = new HashMap<String, Object>();
      
      String oldGroupIdentifier = null;
      
      // we are filling in the old values here and later replacing the ones that have changed with the new ones.
      for (String attributeName: targetGroupAttributes.keySet()) {
        if (primaryColumnsToUpdate.contains(attributeName)) {
          Object oldValue = targetGroupAttributes.get(attributeName).getValue();
          attributeNamesToValues.put(attributeName, oldValue);
        }
        
        if (StringUtils.equals(attributeName, groupTableIdColumn)) {
          // Because the translator always puts new uuid even when updating the same group
          // we need to get the old id through the sync field
          SqlGrouperProvisioningConfigurationAttribute configurationAttribute = (SqlGrouperProvisioningConfigurationAttribute) attributeNameToConfig.get(attributeName);
          String syncField = configurationAttribute.getTranslateToGroupSyncField(); // e.g. groupToId2
          oldGroupIdentifier = targetGroup.getProvisioningGroupWrapper().getGcGrouperSyncGroup().retrieveField(syncField);
        }
        
      }
      
      if (StringUtils.isBlank(oldGroupIdentifier)) {
        throw new RuntimeException("Unable to retrieve old identifier value from sync group");
      }
      
      for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetGroup.getInternal_objectChanges())) {
        
        String attributeName = provisioningObjectChange.getAttributeName();
        SqlGrouperProvisioningConfigurationAttribute configurationAttribute = (SqlGrouperProvisioningConfigurationAttribute) attributeNameToConfig.get(attributeName);
        
        if (StringUtils.equals(configurationAttribute.getStorageType(), "separateAttributesTable")) {
          
          if (provisioningObjectChange.getProvisioningObjectChangeAction() == ProvisioningObjectChangeAction.update) {
            if (GrouperUtil.isEmpty(provisioningObjectChange.getOldValue())) {
              //attributeNamesToValuesNeedingInsert.put(attributeName, provisioningObjectChange.getNewValue());
              
              List<Object> bindInsertVarsForAttributesTable = new ArrayList<Object>();
              batchInsertBindVarsForAttributesTable.add(bindInsertVarsForAttributesTable);
              
              bindInsertVarsForAttributesTable.add(oldGroupIdentifier);
              bindInsertVarsForAttributesTable.add(attributeName);
              bindInsertVarsForAttributesTable.add(provisioningObjectChange.getNewValue());
              
              if (StringUtils.isNotBlank(groupAttributesLastModifiedColumn)) {
                bindInsertVarsForAttributesTable.add(new Timestamp(System.currentTimeMillis()));
              }
              
            } else if (GrouperUtil.isEmpty(provisioningObjectChange.getNewValue())) {
              //attributeNamesToValuesNeedingDelete.put(attributeName, provisioningObjectChange.getOldValue());
              
              List<Object> bindDeleteVarsForAttributesTable = new ArrayList<Object>();
              batchDeleteBindVarsForAttributesTable.add(bindDeleteVarsForAttributesTable);
              
              bindDeleteVarsForAttributesTable.add(oldGroupIdentifier);
              bindDeleteVarsForAttributesTable.add(attributeName);
              bindDeleteVarsForAttributesTable.add(provisioningObjectChange.getOldValue());
              
            } else {
              
              List<Object> bindUpdateVarsForAttributesTable = new ArrayList<Object>();
              batchUpdateBindVarsForAttributesTable.add(bindUpdateVarsForAttributesTable);
              
              bindUpdateVarsForAttributesTable.add(provisioningObjectChange.getNewValue());
              if (StringUtils.isNotBlank(groupAttributesLastModifiedColumn)) {
                bindUpdateVarsForAttributesTable.add(new Timestamp(System.currentTimeMillis()));
              }
              
              bindUpdateVarsForAttributesTable.add(oldGroupIdentifier);
              bindUpdateVarsForAttributesTable.add(attributeName);
              bindUpdateVarsForAttributesTable.add(provisioningObjectChange.getOldValue());
              
              //attributeNamesToValuesNeedingUpdate.put(attributeName, new MultiKey(provisioningObjectChange.getOldValue(), provisioningObjectChange.getNewValue()));
            }
          } else if (provisioningObjectChange.getProvisioningObjectChangeAction() == ProvisioningObjectChangeAction.insert) {
            
            List<Object> bindInsertVarsForAttributesTable = new ArrayList<Object>();
            batchInsertBindVarsForAttributesTable.add(bindInsertVarsForAttributesTable);
            
            bindInsertVarsForAttributesTable.add(oldGroupIdentifier);
            bindInsertVarsForAttributesTable.add(attributeName);
            bindInsertVarsForAttributesTable.add(provisioningObjectChange.getNewValue());
            
            if (StringUtils.isNotBlank(groupAttributesLastModifiedColumn)) {
              bindInsertVarsForAttributesTable.add(new Timestamp(System.currentTimeMillis()));
            }
            
            // attributeNamesToValuesNeedingInsert.put(attributeName, provisioningObjectChange.getNewValue());
          } else if (provisioningObjectChange.getProvisioningObjectChangeAction() == ProvisioningObjectChangeAction.delete) {
            //attributeNamesToValuesNeedingDelete.put(attributeName, provisioningObjectChange.getOldValue());
            List<Object> bindDeleteVarsForAttributesTable = new ArrayList<Object>();
            batchDeleteBindVarsForAttributesTable.add(bindDeleteVarsForAttributesTable);
            
            bindDeleteVarsForAttributesTable.add(oldGroupIdentifier);
            bindDeleteVarsForAttributesTable.add(attributeName);
            bindDeleteVarsForAttributesTable.add(provisioningObjectChange.getOldValue());
          }
          
        } else {
          //primaryColumnsToUpdate.add(attributeName);
          
          attributeNamesToValues.put(attributeName, provisioningObjectChange.getNewValue());
          
//          primaryValuesToUpdate.add(provisioningObjectChange.getNewValue());
          
//          if (i>0) {
//            commaSeparatedColumnNames.append(", ");
//            commaSeparatedQuestionMarks.append(", ");
//          }
//          commaSeparatedColumnNames.append(attributeName);
//          commaSeparatedQuestionMarks.append(" ? ");
//          i++;
        }
        
      }
      
      List<Object> bindVarsForPrimaryTable = new ArrayList<Object>();
      batchBindVarsForPrimaryTable.add(bindVarsForPrimaryTable);
      
      for (String colName: primaryColumnsToUpdate) {
        // add where clause bind variable in there
        bindVarsForPrimaryTable.add(attributeNamesToValues.get(colName));
      }
      
      // This always gives new uuid
      // bindVarsForPrimaryTable.add(targetGroupAttributes.get(groupTableIdColumn).getValue());
       bindVarsForPrimaryTable.add(oldGroupIdentifier);
      
      //TODO set it only after running the sql statement
      targetGroup.setProvisioned(true);
      
    }
    
    if (primaryColumnsToUpdate.size() > 0) {
      StringBuilder sqlPrimary = new StringBuilder("update "+groupTableName + " set ");
      
      for (int j = 0; j<primaryColumnsToUpdate.size(); j++) {
        
        if (j > 0) {
          sqlPrimary.append(", ");
        }
        
        sqlPrimary.append(primaryColumnsToUpdate.get(j));
        sqlPrimary.append(" = ");
        sqlPrimary.append(" ? ");
        
      }
      
      sqlPrimary.append(" where "+ groupTableIdColumn + " = ? ");
      
      
      gcDbAccess.sql(sqlPrimary.toString());
      
      gcDbAccess.batchBindVars(batchBindVarsForPrimaryTable);

      int[] executeBatchSql = gcDbAccess.executeBatchSql();
      
      for (int j=0; j<executeBatchSql.length; j++) {
        
        if (executeBatchSql[j] == 1) {
          targetGroups.get(j).setProvisioned(true);
        }
        
      }
      
    }
    
    if (batchUpdateBindVarsForAttributesTable.size() > 0 ) {
      
      gcDbAccess = new GcDbAccess().connectionName(dbExternalSystemConfigId);
      
      StringBuilder sqlAttributes = new StringBuilder("update "+groupAttributesTableName + " set " +groupAttributesAttributeValueColumn +" = ? ");
      
      if (StringUtils.isNotBlank(groupAttributesLastModifiedColumn)) {
        sqlAttributes.append(", "+groupAttributesLastModifiedColumn+ " = ? ");
      }
      
      sqlAttributes.append(" where "+ groupAttributesGroupForeignKeyColumn + " = ? and "+ groupAttributesAttributeNameColumn + " = ? and "+ groupAttributesAttributeValueColumn + " = ? ");
      
      gcDbAccess.sql(sqlAttributes.toString());
      int[] executeBatchSql = gcDbAccess.batchBindVars(batchUpdateBindVarsForAttributesTable).executeBatchSql();
      
      // {g1, g1, g2, g3}
      // { 0, 1, 1, 1 }
      
      /**
       * 
       * 0 -> g1
       * 1 -> g1
       * 2 -> g2
       * 3 -> g3
       * 
       * 
       */
      
//      for (String attributeName: attributeNamesToValuesNeedingUpdate.keySet()) {
//        MultiKey oldAndNewValue = attributeNamesToValuesNeedingUpdate.get(attributeName);
//        gcDbAccess.addBindVar(oldAndNewValue.getKey(1));
//        if (StringUtils.isNotBlank(groupAttributesLastModifiedColumn)) { 
//          gcDbAccess.addBindVar(new Timestamp(System.currentTimeMillis()));
//        }
//        gcDbAccess.addBindVar(targetGroups.getAttributes().get(groupIdColumnName).getValue());
//        gcDbAccess.addBindVar(attributeName);
//        gcDbAccess.addBindVar(oldAndNewValue.getKey(0));
//        
//        gcDbAccess.executeSql();
//      }
      
    }
    
    if (batchInsertBindVarsForAttributesTable.size() > 0 ) {
      
      List<String> columnsToInsertInAttributesTable = new ArrayList<String>();
      columnsToInsertInAttributesTable.add(groupAttributesGroupForeignKeyColumn);
      columnsToInsertInAttributesTable.add(groupAttributesAttributeNameColumn);
      columnsToInsertInAttributesTable.add(groupAttributesAttributeValueColumn);
      if (StringUtils.isNotBlank(groupAttributesLastModifiedColumn)) {
        columnsToInsertInAttributesTable.add(groupAttributesLastModifiedColumn);
      }
      
      String commaSeparatedColNamesAttributesTable = StringUtils.join(columnsToInsertInAttributesTable, ",");
      String commaSeparatedQuestionMarksAttributesTable = GrouperClientUtils.appendQuestions(columnsToInsertInAttributesTable.size());
      
      String sqlForAttributesTable = "insert into " + groupAttributesTableName + "(" + commaSeparatedColNamesAttributesTable + ") values ("+commaSeparatedQuestionMarksAttributesTable+")";
      
      //List<List<Object>> batchBindVarsForAttributesTable = new ArrayList<List<Object>>();
      
//      for (String attributeName: attributeNamesToValuesNeedingInsert.keySet()) {
//        
//        List<Object> bindVars = new ArrayList<Object>();
//        batchBindVarsForAttributesTable.add(bindVars);
//        
//        Object newValue = attributeNamesToValuesNeedingInsert.get(attributeName);
//        
//        bindVars.add(targetGroups.getAttributes().get(groupIdColumnName).getValue());
//        bindVars.add(attributeName);
//        bindVars.add(newValue);
//        if (StringUtils.isNotBlank(groupAttributesLastModifiedColumn)) { 
//          bindVars.add(new Timestamp(System.currentTimeMillis()));
//        }
//        
//      }
      
      int[] countsAttributesTable = new GcDbAccess().connectionName(dbExternalSystemConfigId).sql(sqlForAttributesTable.toString()).batchBindVars(batchInsertBindVarsForAttributesTable).executeBatchSql();
      
    }
    
    if (batchDeleteBindVarsForAttributesTable.size() > 0 ) {
      
      String sqlForAttributesTable = " delete from " + groupAttributesTableName + " where " + groupAttributesGroupForeignKeyColumn + " = ? and " 
          + groupAttributesAttributeNameColumn + " = ? and "
          + groupAttributesAttributeValueColumn + " = ? " ;
      
//      List<List<Object>> batchBindVarsForAttributesTable = new ArrayList<List<Object>>();
//      
//      for (String attributeName: attributeNamesToValuesNeedingDelete.keySet()) {
//        
//        List<Object> bindVars = new ArrayList<Object>();
//        batchBindVarsForAttributesTable.add(bindVars);
//        
//        Object oldValue = attributeNamesToValuesNeedingDelete.get(attributeName);
//        
//        bindVars.add(targetGroups.getAttributes().get(groupIdColumnName).getValue());
//        bindVars.add(attributeName);
//        bindVars.add(oldValue);
//      }
      
      int[] countsAttributesTable = new GcDbAccess().connectionName(dbExternalSystemConfigId).sql(sqlForAttributesTable.toString()).batchBindVars(batchDeleteBindVarsForAttributesTable).executeBatchSql();
      
    }
    
    return new TargetDaoUpdateGroupsResponse();

  }
  
  @Override
  public TargetDaoUpdateEntitiesResponse updateEntities(TargetDaoUpdateEntitiesRequest targetDaoUpdateEntitiesRequest) {
    
    SqlProvisioningConfiguration sqlProvisioningConfiguration = (SqlProvisioningConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
    
    String dbExternalSystemConfigId = sqlProvisioningConfiguration.getDbExternalSystemConfigId();
    
    String entityTableName = sqlProvisioningConfiguration.getEntityTableName();

    String entityTableIdColumn = sqlProvisioningConfiguration.getEntityTableIdColumn();
    
    String entityAttributesTableName = sqlProvisioningConfiguration.getEntityAttributesTableName();

    String entityAttributesEntityForeignKeyColumn = sqlProvisioningConfiguration.getEntityAttributesEntityForeignKeyColumn();
    
    String entityAttributesAttributeNameColumn = sqlProvisioningConfiguration.getEntityAttributesAttributeNameColumn();

    String entityAttributesAttributeValueColumn = sqlProvisioningConfiguration.getEntityAttributesAttributeValueColumn();

    String entityAttributesLastModifiedColumn = sqlProvisioningConfiguration.getEntityAttributesLastModifiedColumn();
    
    Map<String, GrouperProvisioningConfigurationAttribute> attributeNameToConfig = sqlProvisioningConfiguration.getTargetEntityAttributeNameToConfig();
    
    List<ProvisioningEntity> targetEntities = targetDaoUpdateEntitiesRequest.getTargetEntities();
    
    GcDbAccess gcDbAccess = new GcDbAccess().connectionName(dbExternalSystemConfigId);
    
    List<String> primaryColumnsToUpdate = new ArrayList<String>();
    
    for (String attributeName: attributeNameToConfig.keySet()) {
      SqlGrouperProvisioningConfigurationAttribute configurationAttribute = (SqlGrouperProvisioningConfigurationAttribute) attributeNameToConfig.get(attributeName);
      
      configurationAttribute.getTranslateToMemberSyncField();
      
      if (configurationAttribute.isUpdate()) {
        if (StringUtils.isBlank(configurationAttribute.getStorageType()) || StringUtils.equals(configurationAttribute.getStorageType(), "entityTableColumn")) {
          primaryColumnsToUpdate.add(attributeName);
        }
      }
      
    }
    
    List<List<Object>> batchBindVarsForPrimaryTable = new ArrayList<List<Object>>();
    List<List<Object>> batchUpdateBindVarsForAttributesTable = new ArrayList<List<Object>>();
    List<List<Object>> batchInsertBindVarsForAttributesTable = new ArrayList<List<Object>>();
    List<List<Object>> batchDeleteBindVarsForAttributesTable = new ArrayList<List<Object>>();
    
    for (ProvisioningEntity targetEntity: targetEntities) {
      
      Map<String, ProvisioningAttribute> targetEntityAttributes = targetEntity.getAttributes();
      
      Map<String, Object> attributeNamesToValues = new HashMap<String, Object>();
      
      String oldEntityIdentifier = null;
      
      // we are filling in the old values here and later replacing the ones that have changed with the new ones.
      for (String attributeName: targetEntityAttributes.keySet()) {
        if (primaryColumnsToUpdate.contains(attributeName)) {
          Object oldValue = targetEntityAttributes.get(attributeName).getValue();
          attributeNamesToValues.put(attributeName, oldValue);
        }
        
        if (StringUtils.equals(attributeName, entityTableIdColumn)) {
          // Because the translator always puts new uuid even when updating the same group
          // we need to get the old id through the sync field
          SqlGrouperProvisioningConfigurationAttribute configurationAttribute = (SqlGrouperProvisioningConfigurationAttribute) attributeNameToConfig.get(attributeName);
          String syncField = configurationAttribute.getTranslateToMemberSyncField(); // e.g. memberToId2
          oldEntityIdentifier = targetEntity.getProvisioningEntityWrapper().getGcGrouperSyncMember().retrieveField(syncField);
        }
        
      }
      
      if (StringUtils.isBlank(oldEntityIdentifier)) {
        throw new RuntimeException("Unable to retrieve old identifier value from sync member");
      }
      
      for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetEntity.getInternal_objectChanges())) {
        
        String attributeName = provisioningObjectChange.getAttributeName();
        SqlGrouperProvisioningConfigurationAttribute configurationAttribute = (SqlGrouperProvisioningConfigurationAttribute) attributeNameToConfig.get(attributeName);
        
        if (StringUtils.equals(configurationAttribute.getStorageType(), "separateAttributesTable")) {
          
          if (provisioningObjectChange.getProvisioningObjectChangeAction() == ProvisioningObjectChangeAction.update) {
            if (GrouperUtil.isEmpty(provisioningObjectChange.getOldValue())) {
              //attributeNamesToValuesNeedingInsert.put(attributeName, provisioningObjectChange.getNewValue());
              
              List<Object> bindInsertVarsForAttributesTable = new ArrayList<Object>();
              batchInsertBindVarsForAttributesTable.add(bindInsertVarsForAttributesTable);
              
              bindInsertVarsForAttributesTable.add(oldEntityIdentifier);
              bindInsertVarsForAttributesTable.add(attributeName);
              bindInsertVarsForAttributesTable.add(provisioningObjectChange.getNewValue());
              
              if (StringUtils.isNotBlank(entityAttributesLastModifiedColumn)) {
                bindInsertVarsForAttributesTable.add(new Timestamp(System.currentTimeMillis()));
              }
              
            } else if (GrouperUtil.isEmpty(provisioningObjectChange.getNewValue())) {
              //attributeNamesToValuesNeedingDelete.put(attributeName, provisioningObjectChange.getOldValue());
              
              List<Object> bindDeleteVarsForAttributesTable = new ArrayList<Object>();
              batchDeleteBindVarsForAttributesTable.add(bindDeleteVarsForAttributesTable);
              
              bindDeleteVarsForAttributesTable.add(oldEntityIdentifier);
              bindDeleteVarsForAttributesTable.add(attributeName);
              bindDeleteVarsForAttributesTable.add(provisioningObjectChange.getOldValue());
              
            } else {
              
              List<Object> bindUpdateVarsForAttributesTable = new ArrayList<Object>();
              batchUpdateBindVarsForAttributesTable.add(bindUpdateVarsForAttributesTable);
              
              bindUpdateVarsForAttributesTable.add(provisioningObjectChange.getNewValue());
              if (StringUtils.isNotBlank(entityAttributesLastModifiedColumn)) {
                bindUpdateVarsForAttributesTable.add(new Timestamp(System.currentTimeMillis()));
              }
              
              bindUpdateVarsForAttributesTable.add(oldEntityIdentifier);
              bindUpdateVarsForAttributesTable.add(attributeName);
              bindUpdateVarsForAttributesTable.add(provisioningObjectChange.getOldValue());
              
              //attributeNamesToValuesNeedingUpdate.put(attributeName, new MultiKey(provisioningObjectChange.getOldValue(), provisioningObjectChange.getNewValue()));
            }
          } else if (provisioningObjectChange.getProvisioningObjectChangeAction() == ProvisioningObjectChangeAction.insert) {
            
            List<Object> bindInsertVarsForAttributesTable = new ArrayList<Object>();
            batchInsertBindVarsForAttributesTable.add(bindInsertVarsForAttributesTable);
            
            bindInsertVarsForAttributesTable.add(oldEntityIdentifier);
            bindInsertVarsForAttributesTable.add(attributeName);
            bindInsertVarsForAttributesTable.add(provisioningObjectChange.getNewValue());
            
            if (StringUtils.isNotBlank(entityAttributesLastModifiedColumn)) {
              bindInsertVarsForAttributesTable.add(new Timestamp(System.currentTimeMillis()));
            }
            
            // attributeNamesToValuesNeedingInsert.put(attributeName, provisioningObjectChange.getNewValue());
          } else if (provisioningObjectChange.getProvisioningObjectChangeAction() == ProvisioningObjectChangeAction.delete) {
            //attributeNamesToValuesNeedingDelete.put(attributeName, provisioningObjectChange.getOldValue());
            List<Object> bindDeleteVarsForAttributesTable = new ArrayList<Object>();
            batchDeleteBindVarsForAttributesTable.add(bindDeleteVarsForAttributesTable);
            
            bindDeleteVarsForAttributesTable.add(oldEntityIdentifier);
            bindDeleteVarsForAttributesTable.add(attributeName);
            bindDeleteVarsForAttributesTable.add(provisioningObjectChange.getOldValue());
          }
          
        } else {
          //primaryColumnsToUpdate.add(attributeName);
          
          attributeNamesToValues.put(attributeName, provisioningObjectChange.getNewValue());
          
//          primaryValuesToUpdate.add(provisioningObjectChange.getNewValue());
          
//          if (i>0) {
//            commaSeparatedColumnNames.append(", ");
//            commaSeparatedQuestionMarks.append(", ");
//          }
//          commaSeparatedColumnNames.append(attributeName);
//          commaSeparatedQuestionMarks.append(" ? ");
//          i++;
        }
        
      }
      
      List<Object> bindVarsForPrimaryTable = new ArrayList<Object>();
      batchBindVarsForPrimaryTable.add(bindVarsForPrimaryTable);
      
      for (String colName: primaryColumnsToUpdate) {
        // add where clause bind variable in there
        bindVarsForPrimaryTable.add(attributeNamesToValues.get(colName));
      }
      
      // This always gives new uuid
      // bindVarsForPrimaryTable.add(targetGroupAttributes.get(groupTableIdColumn).getValue());
       bindVarsForPrimaryTable.add(oldEntityIdentifier);
      
      //TODO set it only after running the sql statement
      targetEntity.setProvisioned(true);
      
    }
    
    if (primaryColumnsToUpdate.size() > 0) {
      StringBuilder sqlPrimary = new StringBuilder("update "+entityTableName + " set ");
      
      for (int j = 0; j<primaryColumnsToUpdate.size(); j++) {
        
        if (j > 0) {
          sqlPrimary.append(", ");
        }
        
        sqlPrimary.append(primaryColumnsToUpdate.get(j));
        sqlPrimary.append(" = ");
        sqlPrimary.append(" ? ");
        
      }
      
      sqlPrimary.append(" where "+ entityTableIdColumn + " = ? ");
      
      
      gcDbAccess.sql(sqlPrimary.toString());
      
      gcDbAccess.batchBindVars(batchBindVarsForPrimaryTable);

      int[] executeBatchSql = gcDbAccess.executeBatchSql();
      
      for (int j=0; j<executeBatchSql.length; j++) {
        
        if (executeBatchSql[j] == 1) {
          targetEntities.get(j).setProvisioned(true);
        }
        
      }
      
    }
    
    if (batchUpdateBindVarsForAttributesTable.size() > 0 ) {
      
      gcDbAccess = new GcDbAccess().connectionName(dbExternalSystemConfigId);
      
      StringBuilder sqlAttributes = new StringBuilder("update "+entityAttributesTableName + " set " +entityAttributesAttributeValueColumn +" = ? ");
      
      if (StringUtils.isNotBlank(entityAttributesLastModifiedColumn)) {
        sqlAttributes.append(", "+entityAttributesLastModifiedColumn+ " = ? ");
      }
      
      sqlAttributes.append(" where "+ entityAttributesEntityForeignKeyColumn + " = ? and "+ entityAttributesAttributeNameColumn + " = ? and "+ entityAttributesAttributeValueColumn + " = ? ");
      
      gcDbAccess.sql(sqlAttributes.toString());
      int[] executeBatchSql = gcDbAccess.batchBindVars(batchUpdateBindVarsForAttributesTable).executeBatchSql();
      
//      for (String attributeName: attributeNamesToValuesNeedingUpdate.keySet()) {
//        MultiKey oldAndNewValue = attributeNamesToValuesNeedingUpdate.get(attributeName);
//        gcDbAccess.addBindVar(oldAndNewValue.getKey(1));
//        if (StringUtils.isNotBlank(groupAttributesLastModifiedColumn)) { 
//          gcDbAccess.addBindVar(new Timestamp(System.currentTimeMillis()));
//        }
//        gcDbAccess.addBindVar(targetGroups.getAttributes().get(groupIdColumnName).getValue());
//        gcDbAccess.addBindVar(attributeName);
//        gcDbAccess.addBindVar(oldAndNewValue.getKey(0));
//        
//        gcDbAccess.executeSql();
//      }
      
    }
    
    if (batchInsertBindVarsForAttributesTable.size() > 0 ) {
      
      List<String> columnsToInsertInAttributesTable = new ArrayList<String>();
      columnsToInsertInAttributesTable.add(entityAttributesEntityForeignKeyColumn);
      columnsToInsertInAttributesTable.add(entityAttributesAttributeNameColumn);
      columnsToInsertInAttributesTable.add(entityAttributesAttributeValueColumn);
      if (StringUtils.isNotBlank(entityAttributesLastModifiedColumn)) {
        columnsToInsertInAttributesTable.add(entityAttributesLastModifiedColumn);
      }
      
      String commaSeparatedColNamesAttributesTable = StringUtils.join(columnsToInsertInAttributesTable, ",");
      String commaSeparatedQuestionMarksAttributesTable = GrouperClientUtils.appendQuestions(columnsToInsertInAttributesTable.size());
      
      String sqlForAttributesTable = "insert into " + entityAttributesTableName + "(" + commaSeparatedColNamesAttributesTable + ") values ("+commaSeparatedQuestionMarksAttributesTable+")";
      
      //List<List<Object>> batchBindVarsForAttributesTable = new ArrayList<List<Object>>();
      
//      for (String attributeName: attributeNamesToValuesNeedingInsert.keySet()) {
//        
//        List<Object> bindVars = new ArrayList<Object>();
//        batchBindVarsForAttributesTable.add(bindVars);
//        
//        Object newValue = attributeNamesToValuesNeedingInsert.get(attributeName);
//        
//        bindVars.add(targetGroups.getAttributes().get(groupIdColumnName).getValue());
//        bindVars.add(attributeName);
//        bindVars.add(newValue);
//        if (StringUtils.isNotBlank(groupAttributesLastModifiedColumn)) { 
//          bindVars.add(new Timestamp(System.currentTimeMillis()));
//        }
//        
//      }
      
      int[] countsAttributesTable = new GcDbAccess().connectionName(dbExternalSystemConfigId).sql(sqlForAttributesTable.toString()).batchBindVars(batchInsertBindVarsForAttributesTable).executeBatchSql();
      
    }
    
    if (batchDeleteBindVarsForAttributesTable.size() > 0 ) {
      
      String sqlForAttributesTable = " delete from " + entityAttributesTableName + " where " + entityAttributesEntityForeignKeyColumn + " = ? and " 
          + entityAttributesAttributeNameColumn + " = ? and "
          + entityAttributesAttributeValueColumn + " = ? " ;
      
//      List<List<Object>> batchBindVarsForAttributesTable = new ArrayList<List<Object>>();
//      
//      for (String attributeName: attributeNamesToValuesNeedingDelete.keySet()) {
//        
//        List<Object> bindVars = new ArrayList<Object>();
//        batchBindVarsForAttributesTable.add(bindVars);
//        
//        Object oldValue = attributeNamesToValuesNeedingDelete.get(attributeName);
//        
//        bindVars.add(targetGroups.getAttributes().get(groupIdColumnName).getValue());
//        bindVars.add(attributeName);
//        bindVars.add(oldValue);
//      }
      
      int[] countsAttributesTable = new GcDbAccess().connectionName(dbExternalSystemConfigId).sql(sqlForAttributesTable.toString()).batchBindVars(batchDeleteBindVarsForAttributesTable).executeBatchSql();
      
    }
    
    return new TargetDaoUpdateEntitiesResponse();

  }

  
  //TODO make db statements in a transaction
  //TODO handle exceptions
  //TODO if the batch fails, try individually
//  @Override
//  public TargetDaoUpdateGroupResponse updateGroup(TargetDaoUpdateGroupRequest targetDaoUpdateGroupRequest) {
//    
//    SqlProvisioningConfiguration sqlProvisioningConfiguration = (SqlProvisioningConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
//    
//    String dbExternalSystemConfigId = sqlProvisioningConfiguration.getDbExternalSystemConfigId();
//    
//    String groupTableName = sqlProvisioningConfiguration.getGroupTableName();
//
//    String groupTableIdColumn = sqlProvisioningConfiguration.getGroupTableIdColumn();
//    
//    String groupIdColumnName = sqlProvisioningConfiguration.getGroupTableIdColumn();
//    
//    String groupAttributesTableName = sqlProvisioningConfiguration.getGroupAttributesTableName();
//
//    String groupAttributesGroupForeignKeyColumn = sqlProvisioningConfiguration.getGroupAttributesGroupForeignKeyColumn();
//    
//    String groupAttributesAttributeNameColumn = sqlProvisioningConfiguration.getGroupAttributesAttributeNameColumn();
//
//    String groupAttributesAttributeValueColumn = sqlProvisioningConfiguration.getGroupAttributesAttributeValueColumn();
//
//    String groupAttributesLastModifiedColumn = sqlProvisioningConfiguration.getGroupAttributesLastModifiedColumn();
//    
//    Map<String, GrouperProvisioningConfigurationAttribute> attributeNameToConfig = sqlProvisioningConfiguration.getTargetGroupAttributeNameToConfig();
//    
//    ProvisioningGroup targetGroup = targetDaoUpdateGroupRequest.getTargetGroup();
//    
//    GcDbAccess gcDbAccess = new GcDbAccess().connectionName(dbExternalSystemConfigId);
//    
//    List<String> primaryColumnsToUpdate = new ArrayList<String>();
//    List<Object> primaryValuesToUpdate = new ArrayList<Object>();
//    
//    StringBuilder commaSeparatedColumnNames = new StringBuilder();
//    StringBuilder commaSeparatedQuestionMarks = new StringBuilder();
//
//    int i = 0;
//    
//    // attribute name to old & new value
//    Map<String, MultiKey> attributeNamesToValuesNeedingUpdate = new HashMap<String, MultiKey>();
//    Map<String, Object> attributeNamesToValuesNeedingInsert = new HashMap<String, Object>();
//    Map<String, Object> attributeNamesToValuesNeedingDelete = new HashMap<String, Object>();
//    
//    for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetGroup.getInternal_objectChanges())) {
//      
//      String attributeName = provisioningObjectChange.getAttributeName();
//      SqlGrouperProvisioningConfigurationAttribute configurationAttribute = (SqlGrouperProvisioningConfigurationAttribute) attributeNameToConfig.get(attributeName);
//      
//      if (StringUtils.equals(configurationAttribute.getStorageType(), "separateAttributesTable")) {
//        
//        if (provisioningObjectChange.getProvisioningObjectChangeAction() == ProvisioningObjectChangeAction.update) {
//          if (GrouperUtil.isEmpty(provisioningObjectChange.getOldValue())) {
//            attributeNamesToValuesNeedingInsert.put(attributeName, provisioningObjectChange.getNewValue());
//          } else if (GrouperUtil.isEmpty(provisioningObjectChange.getNewValue())) {
//            attributeNamesToValuesNeedingDelete.put(attributeName, provisioningObjectChange.getOldValue());
//          } else {
//            attributeNamesToValuesNeedingUpdate.put(attributeName, new MultiKey(provisioningObjectChange.getOldValue(), provisioningObjectChange.getNewValue()));
//          }
//        } else if (provisioningObjectChange.getProvisioningObjectChangeAction() == ProvisioningObjectChangeAction.insert) {
//          attributeNamesToValuesNeedingInsert.put(attributeName, provisioningObjectChange.getNewValue());
//        } else if (provisioningObjectChange.getProvisioningObjectChangeAction() == ProvisioningObjectChangeAction.delete) { 
//          attributeNamesToValuesNeedingDelete.put(attributeName, provisioningObjectChange.getOldValue());
//        }
//        
//      } else {
//        primaryColumnsToUpdate.add(attributeName);
//        
//        primaryValuesToUpdate.add(provisioningObjectChange.getNewValue());
//        
//        if (i>0) {
//          commaSeparatedColumnNames.append(", ");
//          commaSeparatedQuestionMarks.append(", ");
//        }
//        commaSeparatedColumnNames.append(attributeName);
//        commaSeparatedQuestionMarks.append(" ? ");
//        i++;
//      }
//      
//    }
//    
//    if (primaryColumnsToUpdate.size() != 0) {
//      StringBuilder sqlPrimary = new StringBuilder("update "+groupTableName + " set ");
//      
//      for (int j = 0; j<primaryColumnsToUpdate.size(); j++) {
//        
//        if (j > 0) {
//          sqlPrimary.append(", ");
//        }
//        
//        sqlPrimary.append(primaryColumnsToUpdate.get(j));
//        sqlPrimary.append(" = ");
//        sqlPrimary.append(" ? ");
//        
//      }
//      
//      sqlPrimary.append(" where "+ groupTableIdColumn + " = ? ");
//      
//      
//      gcDbAccess.sql(sqlPrimary.toString());
//      
//      for (Object valueToUpdate: primaryValuesToUpdate) {
//        gcDbAccess.addBindVar(valueToUpdate);
//      }
//      
//      gcDbAccess.addBindVar(targetGroup.getId());
//
//      gcDbAccess.executeSql();
//      
//    }
//    
//    if (attributeNamesToValuesNeedingUpdate.size() > 0 ) {
//      
//      StringBuilder sqlAttributes = new StringBuilder("update "+groupAttributesTableName + " set " +groupAttributesAttributeValueColumn +" = ? ");
//      
//      if (StringUtils.isNotBlank(groupAttributesLastModifiedColumn)) {
//        sqlAttributes.append(", "+groupAttributesLastModifiedColumn+ " = ? ");
//      }
//      
//      sqlAttributes.append(" where "+ groupAttributesGroupForeignKeyColumn + " = ? and "+ groupAttributesAttributeNameColumn + " = ? and "+ groupAttributesAttributeValueColumn + " = ? ");
//      
//      gcDbAccess.sql(sqlAttributes.toString());
//      
//      for (String attributeName: attributeNamesToValuesNeedingUpdate.keySet()) {
//        MultiKey oldAndNewValue = attributeNamesToValuesNeedingUpdate.get(attributeName);
//        gcDbAccess.addBindVar(oldAndNewValue.getKey(1));
//        if (StringUtils.isNotBlank(groupAttributesLastModifiedColumn)) { 
//          gcDbAccess.addBindVar(new Timestamp(System.currentTimeMillis()));
//        }
//        gcDbAccess.addBindVar(targetGroup.getAttributes().get(groupIdColumnName).getValue());
//        gcDbAccess.addBindVar(attributeName);
//        gcDbAccess.addBindVar(oldAndNewValue.getKey(0));
//        
//        gcDbAccess.executeSql();
//      }
//      
//    }
//    
//    if (attributeNamesToValuesNeedingInsert.size() > 0 ) {
//      
//      List<String> columnsToInsertInAttributesTable = new ArrayList<String>();
//      columnsToInsertInAttributesTable.add(groupAttributesGroupForeignKeyColumn);
//      columnsToInsertInAttributesTable.add(groupAttributesAttributeNameColumn);
//      columnsToInsertInAttributesTable.add(groupAttributesAttributeValueColumn);
//      if (StringUtils.isNotBlank(groupAttributesLastModifiedColumn)) {
//        columnsToInsertInAttributesTable.add(groupAttributesLastModifiedColumn);
//      }
//      
//      String commaSeparatedColNamesAttributesTable = StringUtils.join(columnsToInsertInAttributesTable, ",");
//      String commaSeparatedQuestionMarksAttributesTable = GrouperClientUtils.appendQuestions(columnsToInsertInAttributesTable.size());
//      
//      String sqlForAttributesTable = "insert into " + groupAttributesTableName + "(" + commaSeparatedColNamesAttributesTable + ") values ("+commaSeparatedQuestionMarksAttributesTable+")";
//      
//      List<List<Object>> batchBindVarsForAttributesTable = new ArrayList<List<Object>>();
//      
//      for (String attributeName: attributeNamesToValuesNeedingInsert.keySet()) {
//        
//        List<Object> bindVars = new ArrayList<Object>();
//        batchBindVarsForAttributesTable.add(bindVars);
//        
//        Object newValue = attributeNamesToValuesNeedingInsert.get(attributeName);
//        
//        bindVars.add(targetGroup.getAttributes().get(groupIdColumnName).getValue());
//        bindVars.add(attributeName);
//        bindVars.add(newValue);
//        if (StringUtils.isNotBlank(groupAttributesLastModifiedColumn)) { 
//          bindVars.add(new Timestamp(System.currentTimeMillis()));
//        }
//        
//      }
//      
//      int[] countsAttributesTable = new GcDbAccess().connectionName(dbExternalSystemConfigId).sql(sqlForAttributesTable.toString()).batchBindVars(batchBindVarsForAttributesTable).executeBatchSql();
//      
//    }
//    
//    if (attributeNamesToValuesNeedingDelete.size() > 0 ) {
//      
//      String sqlForAttributesTable = " delete from " + groupAttributesTableName + " where " + groupAttributesGroupForeignKeyColumn + " = ? and " 
//          + groupAttributesAttributeNameColumn + " = ? and "
//          + groupAttributesAttributeValueColumn + " = ? " ;
//      
//      List<List<Object>> batchBindVarsForAttributesTable = new ArrayList<List<Object>>();
//      
//      for (String attributeName: attributeNamesToValuesNeedingDelete.keySet()) {
//        
//        List<Object> bindVars = new ArrayList<Object>();
//        batchBindVarsForAttributesTable.add(bindVars);
//        
//        Object oldValue = attributeNamesToValuesNeedingDelete.get(attributeName);
//        
//        bindVars.add(targetGroup.getAttributes().get(groupIdColumnName).getValue());
//        bindVars.add(attributeName);
//        bindVars.add(oldValue);
//      }
//      
//      int[] countsAttributesTable = new GcDbAccess().connectionName(dbExternalSystemConfigId).sql(sqlForAttributesTable.toString()).batchBindVars(batchBindVarsForAttributesTable).executeBatchSql();
//      
//    }
//    
//    targetGroup.setProvisioned(true);
//      
//    return new TargetDaoUpdateGroupResponse();
//
//  }
  
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
    String groupTableIdColumn = sqlProvisioningConfiguration.getGroupTableIdColumn();
    String groupAttributesTableName = sqlProvisioningConfiguration.getGroupAttributesTableName();
    String groupAttributesGroupForeignKeyColumn = sqlProvisioningConfiguration.getGroupAttributesGroupForeignKeyColumn();
    
    StringBuilder sqlAttributes = new StringBuilder();
    if (StringUtils.isNotBlank(groupAttributesTableName)) {
      sqlAttributes.append("delete from "+groupAttributesTableName + " where " + groupAttributesGroupForeignKeyColumn + "  = ? ");
    }
    
    StringBuilder sqlPrimary = new StringBuilder("delete from  " + groupTableName);
    List<List<Object>> batchPrimaryBindVars = new ArrayList<List<Object>>();
    List<List<Object>> batchAttributesBindVars = new ArrayList<List<Object>>();
    
    List<GrouperProvisioningConfigurationAttribute> grouperProvisioningConfigurationAttributes = sqlProvisioningConfiguration.getGroupSearchAttributes();
    if (grouperProvisioningConfigurationAttributes.size() > 1) {
      //TODO add this to validation
      throw new RuntimeException("Can currently only have one searchAttribute! " + grouperProvisioningConfigurationAttributes);
    }
    
    if (grouperProvisioningConfigurationAttributes.size() == 1) {
      
      GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute = grouperProvisioningConfigurationAttributes.get(0);
      sqlPrimary.append(" where "+ grouperProvisioningConfigurationAttribute.getName() + " = ? ");
      
      for (ProvisioningGroup targetGroup: targetGroups) {
        String value = targetGroup.retrieveFieldOrAttributeValueString(grouperProvisioningConfigurationAttribute);
        batchPrimaryBindVars.add(GrouperUtil.toListObject(value));
        
        if (StringUtils.isNotBlank(groupAttributesTableName)) {
          ProvisioningAttribute idAttribute = targetGroup.getAttributes().get(groupTableIdColumn);
          if (idAttribute != null && idAttribute.getValue() != null) {
            batchAttributesBindVars.add(GrouperUtil.toListObject(idAttribute.getValue()));
          }
          
        }
        
      }
      
    } else {
      throw new RuntimeException("Why is groupSearchFilter empty?");
    }
    
    int[] counts = new GcDbAccess().connectionName(dbExternalSystemConfigId).sql(sqlPrimary.toString()).batchBindVars(batchPrimaryBindVars).executeBatchSql();

    for (int i=0; i<counts.length; i++) {
      
      if(counts[i] == 1) {
        targetGroups.get(i).setProvisioned(true);
      }
    }
  
    if (batchAttributesBindVars.size() > 0) {
      new GcDbAccess().connectionName(dbExternalSystemConfigId).sql(sqlAttributes.toString()).batchBindVars(batchAttributesBindVars).executeBatchSql();
    }
    
    return new TargetDaoDeleteGroupsResponse();
  }
  

  @Override
  public TargetDaoDeleteEntitiesResponse deleteEntities(TargetDaoDeleteEntitiesRequest targetDaoDeleteEntitiesRequest) {
    
    List<ProvisioningEntity> targetEntities = targetDaoDeleteEntitiesRequest.getTargetEntities();
    
    SqlProvisioningConfiguration sqlProvisioningConfiguration = (SqlProvisioningConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
    
    String dbExternalSystemConfigId = sqlProvisioningConfiguration.getDbExternalSystemConfigId();
    
    String entityTableName = sqlProvisioningConfiguration.getEntityTableName();
    String entityTableIdColumn = sqlProvisioningConfiguration.getEntityTableIdColumn();
    String entityAttributesTableName = sqlProvisioningConfiguration.getEntityAttributesTableName();
    String entityAttributesGroupForeignKeyColumn = sqlProvisioningConfiguration.getEntityAttributesEntityForeignKeyColumn();
    
    StringBuilder sqlAttributes = new StringBuilder();
    if (StringUtils.isNotBlank(entityAttributesTableName)) {
      sqlAttributes.append("delete from "+entityAttributesTableName + " where " + entityAttributesGroupForeignKeyColumn + "  = ? ");
    }
    
    StringBuilder sqlPrimary = new StringBuilder("delete from  " + entityTableName);
    List<List<Object>> batchPrimaryBindVars = new ArrayList<List<Object>>();
    List<List<Object>> batchAttributesBindVars = new ArrayList<List<Object>>();
    
    List<GrouperProvisioningConfigurationAttribute> grouperProvisioningConfigurationAttributes = sqlProvisioningConfiguration.getEntitySearchAttributes();
    if (grouperProvisioningConfigurationAttributes.size() > 1) {
      //TODO add this to validation
      throw new RuntimeException("Can currently only have one searchAttribute! " + grouperProvisioningConfigurationAttributes);
    }
    
    if (grouperProvisioningConfigurationAttributes.size() == 1) {
      
      GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute = grouperProvisioningConfigurationAttributes.get(0);
      sqlPrimary.append(" where "+ grouperProvisioningConfigurationAttribute.getName() + " = ? ");
      
      for (ProvisioningEntity targetEntity: targetEntities) {
        String value = targetEntity.retrieveFieldOrAttributeValueString(grouperProvisioningConfigurationAttribute);
        batchPrimaryBindVars.add(GrouperUtil.toListObject(value));
        
        if (StringUtils.isNotBlank(entityAttributesTableName)) {
          ProvisioningAttribute idAttribute = targetEntity.getAttributes().get(entityTableIdColumn);
          if (idAttribute != null && idAttribute.getValue() != null) {
            batchAttributesBindVars.add(GrouperUtil.toListObject(idAttribute.getValue()));
          }
          
        }
        
      }
      
    } else {
      throw new RuntimeException("Why is groupSearchFilter empty?");
    }
    
    int[] counts = new GcDbAccess().connectionName(dbExternalSystemConfigId).sql(sqlPrimary.toString()).batchBindVars(batchPrimaryBindVars).executeBatchSql();

    for (int i=0; i<counts.length; i++) {
      
      if(counts[i] == 1) {
        targetEntities.get(i).setProvisioned(true);
      }
    }
  
    if (batchAttributesBindVars.size() > 0) {
      new GcDbAccess().connectionName(dbExternalSystemConfigId).sql(sqlAttributes.toString()).batchBindVars(batchAttributesBindVars).executeBatchSql();
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

    String groupIdColumnName = sqlProvisioningConfiguration.getGroupTableIdColumn();
    
    String groupAttributesTableName = sqlProvisioningConfiguration.getGroupAttributesTableName();

    String groupAttributesGroupForeignKeyColumn = sqlProvisioningConfiguration.getGroupAttributesGroupForeignKeyColumn();
    
    String groupAttributesAttributeNameColumn = sqlProvisioningConfiguration.getGroupAttributesAttributeNameColumn();

    String groupAttributesAttributeValueColumn = sqlProvisioningConfiguration.getGroupAttributesAttributeValueColumn();

    String groupAttributesLastModifiedColumn = sqlProvisioningConfiguration.getGroupAttributesLastModifiedColumn();
    String groupAttributesLastModifiedColumnType = sqlProvisioningConfiguration.getGroupAttributesLastModifiedColumnType();
    
    Map<String, GrouperProvisioningConfigurationAttribute> attributeNameToConfig = sqlProvisioningConfiguration.getTargetGroupAttributeNameToConfig();
    
    List<String> columnsToInsertInPrimaryTable = new ArrayList<String>();
    
    for (String attributeName: attributeNameToConfig.keySet()) {
      SqlGrouperProvisioningConfigurationAttribute configurationAttribute = (SqlGrouperProvisioningConfigurationAttribute) attributeNameToConfig.get(attributeName);
      
      if (configurationAttribute.isInsert()) {
        if ( StringUtils.isBlank(configurationAttribute.getStorageType()) || StringUtils.equals(configurationAttribute.getStorageType(), "groupTableColumn")) {
          columnsToInsertInPrimaryTable.add(attributeName);
        }
      }
      
    }
    
    String commaSeparatedColNamesPrimaryTable = StringUtils.join(columnsToInsertInPrimaryTable, ",");
    String commaSeparatedQuestionMarksPrimaryTable = GrouperClientUtils.appendQuestions(columnsToInsertInPrimaryTable.size());
    
    List<List<Object>> batchBindVarsForPrimaryTable = new ArrayList<List<Object>>();

    List<List<Object>> batchBindVarsForAttributesTable = new ArrayList<List<Object>>();
    
    String sqlForPrimaryTable = "insert into " + groupTableName + "(" + commaSeparatedColNamesPrimaryTable + ") values ("+commaSeparatedQuestionMarksPrimaryTable+")";

    for (ProvisioningGroup targetGroup: targetGroups) {
      
      Map<String, Object> attributeNameToValueForPrimaryTable = new HashMap<String, Object>();

      Map<String, Object> attributeNameToValueForAttributesTable = new HashMap<String, Object>();
      
      for (ProvisioningObjectChange provisioningObjectChange: GrouperUtil.nonNull(targetGroup.getInternal_objectChanges())) {
        if (provisioningObjectChange.getProvisioningObjectChangeAction() == ProvisioningObjectChangeAction.insert) {
          String attributeName = provisioningObjectChange.getAttributeName();
          
          SqlGrouperProvisioningConfigurationAttribute configurationAttribute = (SqlGrouperProvisioningConfigurationAttribute) attributeNameToConfig.get(attributeName);
          if (StringUtils.equals(configurationAttribute.getStorageType(), "separateAttributesTable")) {
            
            //TODO add ability to insert null values into the attributes table.
            if (GrouperUtil.isEmpty(provisioningObjectChange.getNewValue())) {
              continue;
            }
            
            List<Object> bindVarsForSecondaryTable = new ArrayList<Object>();
            bindVarsForSecondaryTable.add(targetGroup.getAttributes().get(groupIdColumnName).getValue());
            bindVarsForSecondaryTable.add(attributeName);
            bindVarsForSecondaryTable.add(provisioningObjectChange.getNewValue());
            if (StringUtils.isNotBlank(groupAttributesLastModifiedColumn)) {
              if (StringUtils.equals(groupAttributesLastModifiedColumnType, "timestamp")) {
                bindVarsForSecondaryTable.add(new Timestamp(System.currentTimeMillis()));
              } else if (StringUtils.equals(groupAttributesLastModifiedColumnType, "long")) {
                bindVarsForSecondaryTable.add(System.currentTimeMillis());
              } else {
                throw new RuntimeException("Invalid groupAttributesLastModifiedColumnType: '"+groupAttributesLastModifiedColumnType+"'");
              }
            }
            
            batchBindVarsForAttributesTable.add(bindVarsForSecondaryTable);
            
            attributeNameToValueForAttributesTable.put(attributeName, provisioningObjectChange.getNewValue());
          } else {
            attributeNameToValueForPrimaryTable.put(attributeName, provisioningObjectChange.getNewValue());
          }
          
        }
      }
      
      List<Object> bindVarsForPrimaryTable = new ArrayList<Object>();
      batchBindVarsForPrimaryTable.add(bindVarsForPrimaryTable);
      
      for (String colName: columnsToInsertInPrimaryTable) {
        bindVarsForPrimaryTable.add(attributeNameToValueForPrimaryTable.get(colName));
      }
      
    }
    
    int[] countsPrimaryTable = new GcDbAccess().connectionName(dbExternalSystemConfigId).sql(sqlForPrimaryTable.toString()).batchBindVars(batchBindVarsForPrimaryTable).executeBatchSql();
    
    if (StringUtils.isNotBlank(groupAttributesTableName) && batchBindVarsForAttributesTable.size() > 0) {
      List<String> columnsToInsertInAttributesTable = new ArrayList<String>();
      columnsToInsertInAttributesTable.add(groupAttributesGroupForeignKeyColumn);
      columnsToInsertInAttributesTable.add(groupAttributesAttributeNameColumn);
      columnsToInsertInAttributesTable.add(groupAttributesAttributeValueColumn);
      if (StringUtils.isNotBlank(groupAttributesLastModifiedColumn)) {
        columnsToInsertInAttributesTable.add(groupAttributesLastModifiedColumn);
      }
      
      String commaSeparatedColNamesAttributesTable = StringUtils.join(columnsToInsertInAttributesTable, ",");
      String commaSeparatedQuestionMarksAttributesTable = GrouperClientUtils.appendQuestions(columnsToInsertInAttributesTable.size());
      
      String sqlForAttributesTable = "insert into " + groupAttributesTableName + "(" + commaSeparatedColNamesAttributesTable + ") values ("+commaSeparatedQuestionMarksAttributesTable+")";
      
      int[] countsAttributesTable = new GcDbAccess().connectionName(dbExternalSystemConfigId).sql(sqlForAttributesTable.toString()).batchBindVars(batchBindVarsForAttributesTable).executeBatchSql();
      
    }

    for (int i=0; i<countsPrimaryTable.length; i++) {
      
      if(countsPrimaryTable[i] == 1) {
        targetGroups.get(i).setProvisioned(true);
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

    String entityIdColumnName = sqlProvisioningConfiguration.getEntityTableIdColumn();
    
    String entityAttributesTableName = sqlProvisioningConfiguration.getEntityAttributesTableName();

    String entityAttributesEntityForeignKeyColumn = sqlProvisioningConfiguration.getEntityAttributesEntityForeignKeyColumn();
    
    String entityAttributesAttributeNameColumn = sqlProvisioningConfiguration.getEntityAttributesAttributeNameColumn();

    String entityAttributesAttributeValueColumn = sqlProvisioningConfiguration.getEntityAttributesAttributeValueColumn();

    String entityAttributesLastModifiedColumn = sqlProvisioningConfiguration.getEntityAttributesLastModifiedColumn();
    
    Map<String, GrouperProvisioningConfigurationAttribute> attributeNameToConfig = sqlProvisioningConfiguration.getTargetEntityAttributeNameToConfig();
    
    List<String> columnsToInsertInPrimaryTable = new ArrayList<String>();
    
    for (String attributeName: attributeNameToConfig.keySet()) {
      SqlGrouperProvisioningConfigurationAttribute configurationAttribute = (SqlGrouperProvisioningConfigurationAttribute) attributeNameToConfig.get(attributeName);
      
      if (configurationAttribute.isInsert()) {
        if ( StringUtils.isBlank(configurationAttribute.getStorageType()) || StringUtils.equals(configurationAttribute.getStorageType(), "entityTableColumn")) {
          columnsToInsertInPrimaryTable.add(attributeName);
        }
      }
      
    }
    
    String commaSeparatedColNamesPrimaryTable = StringUtils.join(columnsToInsertInPrimaryTable, ",");
    String commaSeparatedQuestionMarksPrimaryTable = GrouperClientUtils.appendQuestions(columnsToInsertInPrimaryTable.size());
    
    List<List<Object>> batchBindVarsForPrimaryTable = new ArrayList<List<Object>>();

    List<List<Object>> batchBindVarsForAttributesTable = new ArrayList<List<Object>>();
    
    String sqlForPrimaryTable = "insert into " + entityTableName + "(" + commaSeparatedColNamesPrimaryTable + ") values ("+commaSeparatedQuestionMarksPrimaryTable+")";

    for (ProvisioningEntity targetEntity: targetEntities) {
      
      Map<String, Object> attributeNameToValueForPrimaryTable = new HashMap<String, Object>();

      Map<String, Object> attributeNameToValueForAttributesTable = new HashMap<String, Object>();
      
      for (ProvisioningObjectChange provisioningObjectChange: GrouperUtil.nonNull(targetEntity.getInternal_objectChanges())) {
        if (provisioningObjectChange.getProvisioningObjectChangeAction() == ProvisioningObjectChangeAction.insert) {
          String attributeName = provisioningObjectChange.getAttributeName();
          
          SqlGrouperProvisioningConfigurationAttribute configurationAttribute = (SqlGrouperProvisioningConfigurationAttribute) attributeNameToConfig.get(attributeName);
          if (StringUtils.equals(configurationAttribute.getStorageType(), "separateAttributesTable")) {
            
            //TODO add ability to insert null values into the attributes table.
            if (GrouperUtil.isEmpty(provisioningObjectChange.getNewValue())) {
              continue;
            }
            
            List<Object> bindVarsForSecondaryTable = new ArrayList<Object>();
            bindVarsForSecondaryTable.add(targetEntity.getAttributes().get(entityIdColumnName).getValue());
            bindVarsForSecondaryTable.add(attributeName);
            bindVarsForSecondaryTable.add(provisioningObjectChange.getNewValue());
            if (StringUtils.isNotBlank(entityAttributesLastModifiedColumn)) {
              bindVarsForSecondaryTable.add(new Timestamp(System.currentTimeMillis()));
            }
            
            batchBindVarsForAttributesTable.add(bindVarsForSecondaryTable);
            
            attributeNameToValueForAttributesTable.put(attributeName, provisioningObjectChange.getNewValue());
          } else {
            attributeNameToValueForPrimaryTable.put(attributeName, provisioningObjectChange.getNewValue());
          }
          
        }
      }
      
      List<Object> bindVarsForPrimaryTable = new ArrayList<Object>();
      batchBindVarsForPrimaryTable.add(bindVarsForPrimaryTable);
      
      for (String colName: columnsToInsertInPrimaryTable) {
        bindVarsForPrimaryTable.add(attributeNameToValueForPrimaryTable.get(colName));
      }
      
    }
    
    int[] countsPrimaryTable = new GcDbAccess().connectionName(dbExternalSystemConfigId).sql(sqlForPrimaryTable.toString()).batchBindVars(batchBindVarsForPrimaryTable).executeBatchSql();
    
    if (StringUtils.isNotBlank(entityAttributesTableName) && batchBindVarsForAttributesTable.size() > 0) {
      List<String> columnsToInsertInAttributesTable = new ArrayList<String>();
      columnsToInsertInAttributesTable.add(entityAttributesEntityForeignKeyColumn);
      columnsToInsertInAttributesTable.add(entityAttributesAttributeNameColumn);
      columnsToInsertInAttributesTable.add(entityAttributesAttributeValueColumn);
      if (StringUtils.isNotBlank(entityAttributesLastModifiedColumn)) {
        columnsToInsertInAttributesTable.add(entityAttributesLastModifiedColumn);
      }
      
      String commaSeparatedColNamesAttributesTable = StringUtils.join(columnsToInsertInAttributesTable, ",");
      String commaSeparatedQuestionMarksAttributesTable = GrouperClientUtils.appendQuestions(columnsToInsertInAttributesTable.size());
      
      String sqlForAttributesTable = "insert into " + entityAttributesTableName + "(" + commaSeparatedColNamesAttributesTable + ") values ("+commaSeparatedQuestionMarksAttributesTable+")";
      
      int[] countsAttributesTable = new GcDbAccess().connectionName(dbExternalSystemConfigId).sql(sqlForAttributesTable.toString()).batchBindVars(batchBindVarsForAttributesTable).executeBatchSql();
      
    }

    for (int i=0; i<countsPrimaryTable.length; i++) {
      
      if(countsPrimaryTable[i] == 1) {
        targetEntities.get(i).setProvisioned(true);
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
    
    String groupTableIdColumn = sqlProvisioningConfiguration.getGroupTableIdColumn();
    
    String groupAttributesTableName  = sqlProvisioningConfiguration.getGroupAttributesTableName();

    String groupAttributesGroupForeignKeyColumn = sqlProvisioningConfiguration.getGroupAttributesGroupForeignKeyColumn();
    
    String groupAttributesAttributeNameColumn = sqlProvisioningConfiguration.getGroupAttributesAttributeNameColumn();

    String groupAttributesAttributeValueColumn = sqlProvisioningConfiguration.getGroupAttributesAttributeValueColumn();
    
    List<ProvisioningGroup> result = new ArrayList<ProvisioningGroup>();
    
    if (!retrieveAll && GrouperUtil.isBlank(grouperTargetGroups)) {
      return result;
    }

    List<String> groupTablePrimaryColNamesList = new ArrayList<String>();
    List<String> groupTableAttributesColNamesList = new ArrayList<String>();
    
    Map<String, GrouperProvisioningConfigurationAttribute> groupAttributeNameToConfigAttribute = sqlProvisioningConfiguration.getTargetGroupAttributeNameToConfig();
    
    for (String attributeName: groupAttributeNameToConfigAttribute.keySet()) {
      SqlGrouperProvisioningConfigurationAttribute configurationAttribute = (SqlGrouperProvisioningConfigurationAttribute) groupAttributeNameToConfigAttribute.get(attributeName);
      
      if (StringUtils.equals(configurationAttribute.getStorageType(), "separateAttributesTable")) {
        groupTableAttributesColNamesList.add(attributeName);
      } else {
        groupTablePrimaryColNamesList.add(attributeName);
      }
    }
    
    StringBuilder commaSeparatedPrimaryColumnNames = new StringBuilder();
    for (int i=0; i<groupTablePrimaryColNamesList.size(); i++) {
      if (i>0) {
        commaSeparatedPrimaryColumnNames.append(", ");
      }
      commaSeparatedPrimaryColumnNames.append(groupTablePrimaryColNamesList.get(i));
    }
    
    String[] colNames = GrouperUtil.splitTrim(commaSeparatedPrimaryColumnNames.toString(), ",");
    
    StringBuilder sqlInitialPrimary = new StringBuilder("select " + commaSeparatedPrimaryColumnNames.toString() + " from " + groupTableName);

    StringBuilder sqlInitialAttributes = new StringBuilder();
    if (groupTableAttributesColNamesList.size() > 0 ) {
      sqlInitialAttributes = new StringBuilder("select " + groupAttributesGroupForeignKeyColumn + ", " + groupAttributesAttributeNameColumn + ", " +  groupAttributesAttributeValueColumn 
          + " from " + groupAttributesTableName);
    }
    
    List<Object[]> groupPrimaryAttributeValues = null;

    List<Object[]> groupAttributeValues = null;
    
    if (retrieveAll) {
      GcDbAccess gcDbAccess = new GcDbAccess().connectionName(dbExternalSystemConfigId);
      
      groupPrimaryAttributeValues = gcDbAccess.sql(sqlInitialPrimary.toString()).selectList(Object[].class);
      
      Map<Object, List<Object[]>> groupIdentifierToAttributes = new HashMap<Object, List<Object[]>>();
      
      if (StringUtils.isNotBlank(sqlInitialAttributes.toString())) {
        groupAttributeValues = gcDbAccess.sql(sqlInitialAttributes.toString()).selectList(Object[].class);
        
        for (Object[] obj: GrouperUtil.nonNull(groupAttributeValues)) {
          Object groupIdentifier = obj[0];
          
          if (groupIdentifierToAttributes.containsKey(groupIdentifier)) {
            groupIdentifierToAttributes.get(groupIdentifier).add(obj);
          } else {
            List<Object[]> list = new ArrayList<Object[]>();
            list.add(obj);
            groupIdentifierToAttributes.put(groupIdentifier, list);
          }
          
        }
        
      }
      
      retrieveGroupsAddRecord(result, colNames, groupPrimaryAttributeValues, groupIdentifierToAttributes, groupTableIdColumn);
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
          StringBuilder sql = new StringBuilder(sqlInitialPrimary);
          
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
          groupPrimaryAttributeValues = gcDbAccess.sql(sql.toString()).selectList(Object[].class);
          
          Map<Object, List<Object[]>> groupIdentifierToAttributes = new HashMap<Object, List<Object[]>>();
          
          if (StringUtils.isNotBlank(sqlInitialAttributes.toString())) {
            gcDbAccess = new GcDbAccess().connectionName(dbExternalSystemConfigId);
            sqlInitialAttributes.append(" where "+ groupAttributesGroupForeignKeyColumn + " in ( ");
            int p = 0;
            for (Object[] groupPrimaryAttributes: groupPrimaryAttributeValues) {
              
              for (int k=0; k<colNames.length; k++) {
                
                if (StringUtils.equalsIgnoreCase(colNames[k], groupTableIdColumn)) {
                  Object object = groupPrimaryAttributes[k];
                  
                  gcDbAccess.addBindVar(object);
                  if (p>0) {
                    sqlInitialAttributes.append(",");
                  }
                  sqlInitialAttributes.append("?");
                  p++;
                  
                }
                
              }
              
            }
            sqlInitialAttributes.append(" ) ");
            
            groupAttributeValues = gcDbAccess.sql(sqlInitialAttributes.toString()).selectList(Object[].class);
            
            for (Object[] obj: GrouperUtil.nonNull(groupAttributeValues)) {
              Object groupIdentifier = obj[0];
              
              if (groupIdentifierToAttributes.containsKey(groupIdentifier)) {
                groupIdentifierToAttributes.get(groupIdentifier).add(obj);
              } else {
                List<Object[]> list = new ArrayList<Object[]>();
                list.add(obj);
                groupIdentifierToAttributes.put(groupIdentifier, list);
              }
              
            }
            
          }
          
          retrieveGroupsAddRecord(result, colNames, groupPrimaryAttributeValues, groupIdentifierToAttributes, groupTableIdColumn);
          
        }
        
      } else {
        throw new RuntimeException("Why is groupSearchFilter empty?");
      }
      
    }

    return result;
   
  }
  
  private List<ProvisioningEntity> retrieveEntities(boolean retrieveAll, List<ProvisioningEntity> grouperTargetEntities) {
    
    if (retrieveAll && grouperTargetEntities != null) {
      throw new RuntimeException("Cant retrieve all and pass in entities to retrieve");
    }
    
    SqlProvisioningConfiguration sqlProvisioningConfiguration = (SqlProvisioningConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
    
    String dbExternalSystemConfigId = sqlProvisioningConfiguration.getDbExternalSystemConfigId();
    
    String entityTableName = sqlProvisioningConfiguration.getEntityTableName();
    
    String entityTableIdColumn = sqlProvisioningConfiguration.getEntityTableIdColumn();
    
    String entityAttributesTableName  = sqlProvisioningConfiguration.getEntityAttributesTableName();

    String entityAttributesEntityForeignKeyColumn = sqlProvisioningConfiguration.getEntityAttributesEntityForeignKeyColumn();
    
    String entityAttributesAttributeNameColumn = sqlProvisioningConfiguration.getEntityAttributesAttributeNameColumn();

    String entityAttributesAttributeValueColumn = sqlProvisioningConfiguration.getEntityAttributesAttributeValueColumn();
    
    List<ProvisioningEntity> result = new ArrayList<ProvisioningEntity>();
    
    if (!retrieveAll && GrouperUtil.isBlank(grouperTargetEntities)) {
      return result;
    }

    List<String> entityTablePrimaryColNamesList = new ArrayList<String>();
    List<String> entityTableAttributesColNamesList = new ArrayList<String>();
    
    Map<String, GrouperProvisioningConfigurationAttribute> entityAttributeNameToConfigAttribute = sqlProvisioningConfiguration.getTargetEntityAttributeNameToConfig();
    
    for (String attributeName: entityAttributeNameToConfigAttribute.keySet()) {
      SqlGrouperProvisioningConfigurationAttribute configurationAttribute = (SqlGrouperProvisioningConfigurationAttribute) entityAttributeNameToConfigAttribute.get(attributeName);
      
      if (StringUtils.equals(configurationAttribute.getStorageType(), "separateAttributesTable")) {
        entityTableAttributesColNamesList.add(attributeName);
      } else {
        entityTablePrimaryColNamesList.add(attributeName);
      }
    }
    
    StringBuilder commaSeparatedPrimaryColumnNames = new StringBuilder();
    for (int i=0; i<entityTablePrimaryColNamesList.size(); i++) {
      if (i>0) {
        commaSeparatedPrimaryColumnNames.append(", ");
      }
      commaSeparatedPrimaryColumnNames.append(entityTablePrimaryColNamesList.get(i));
    }
    
    String[] colNames = GrouperUtil.splitTrim(commaSeparatedPrimaryColumnNames.toString(), ",");
    
    StringBuilder sqlInitialPrimary = new StringBuilder("select " + commaSeparatedPrimaryColumnNames.toString() + " from " + entityTableName);

    StringBuilder sqlInitialAttributes = new StringBuilder();
    if (entityTableAttributesColNamesList.size() > 0 ) {
      sqlInitialAttributes = new StringBuilder("select " + entityAttributesEntityForeignKeyColumn + ", " + entityAttributesAttributeNameColumn + ", " +  entityAttributesAttributeValueColumn 
          + " from " + entityAttributesTableName);
    }
    
    List<Object[]> entityPrimaryAttributeValues = null;

    List<Object[]> entityAttributeValues = null;
    
    if (retrieveAll) {
      GcDbAccess gcDbAccess = new GcDbAccess().connectionName(dbExternalSystemConfigId);
      
      entityPrimaryAttributeValues = gcDbAccess.sql(sqlInitialPrimary.toString()).selectList(Object[].class);
      
      Map<Object, List<Object[]>> entityIdentifierToAttributes = new HashMap<Object, List<Object[]>>();
      
      if (StringUtils.isNotBlank(sqlInitialAttributes.toString())) {
        entityAttributeValues = gcDbAccess.sql(sqlInitialAttributes.toString()).selectList(Object[].class);
        
        for (Object[] obj: GrouperUtil.nonNull(entityAttributeValues)) {
          Object entityIdentifier = obj[0];
          
          if (entityIdentifierToAttributes.containsKey(entityIdentifier)) {
            entityIdentifierToAttributes.get(entityIdentifier).add(obj);
          } else {
            List<Object[]> list = new ArrayList<Object[]>();
            list.add(obj);
            entityIdentifierToAttributes.put(entityIdentifier, list);
          }
          
        }
        
      }
      
      retrieveEntitiesAddRecord(result, colNames, entityPrimaryAttributeValues, entityIdentifierToAttributes, entityTableIdColumn);
    } else {
      
      List<GrouperProvisioningConfigurationAttribute> grouperProvisioningConfigurationAttributes = sqlProvisioningConfiguration.getGroupSearchAttributes();
      if (grouperProvisioningConfigurationAttributes.size() > 1) {
        //TODO add this to validation
        throw new RuntimeException("Can currently only have one searchAttribute! " + grouperProvisioningConfigurationAttributes);
      }
      
      if (grouperProvisioningConfigurationAttributes.size() == 1) {
        
        int numberOfBatches = GrouperUtil.batchNumberOfBatches(grouperTargetEntities.size(), 900);
        GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute = grouperProvisioningConfigurationAttributes.get(0);
        
        for (int i = 0; i < numberOfBatches; i++) {
          
          List<ProvisioningEntity> currentBatchGrouperTargetEntities = GrouperUtil.batchList(grouperTargetEntities, 900, i);
          StringBuilder sql = new StringBuilder(sqlInitialPrimary);
          
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
          entityPrimaryAttributeValues = gcDbAccess.sql(sql.toString()).selectList(Object[].class);
          
          Map<Object, List<Object[]>> entityIdentifierToAttributes = new HashMap<Object, List<Object[]>>();
          
          if (StringUtils.isNotBlank(sqlInitialAttributes.toString())) {
            gcDbAccess = new GcDbAccess().connectionName(dbExternalSystemConfigId);
            sqlInitialAttributes.append(" where "+ entityAttributesEntityForeignKeyColumn + " in ( ");
            int p = 0;
            for (Object[] entityPrimaryAttributes: entityPrimaryAttributeValues) {
              
              for (int k=0; k<colNames.length; k++) {
                
                if (StringUtils.equalsIgnoreCase(colNames[k], entityTableIdColumn)) {
                  Object object = entityPrimaryAttributes[k];
                  
                  gcDbAccess.addBindVar(object);
                  if (p>0) {
                    sqlInitialAttributes.append(",");
                  }
                  sqlInitialAttributes.append("?");
                  p++;
                  
                }
                
              }
              
            }
            sqlInitialAttributes.append(" ) ");
            
            entityAttributeValues = gcDbAccess.sql(sqlInitialAttributes.toString()).selectList(Object[].class);
            
            for (Object[] obj: GrouperUtil.nonNull(entityAttributeValues)) {
              Object groupIdentifier = obj[0];
              
              if (entityIdentifierToAttributes.containsKey(groupIdentifier)) {
                entityIdentifierToAttributes.get(groupIdentifier).add(obj);
              } else {
                List<Object[]> list = new ArrayList<Object[]>();
                list.add(obj);
                entityIdentifierToAttributes.put(groupIdentifier, list);
              }
              
            }
            
          }
          
          retrieveEntitiesAddRecord(result, colNames, entityPrimaryAttributeValues, entityIdentifierToAttributes, entityTableIdColumn);
          
        }
        
      } else {
        throw new RuntimeException("Why is entitySearchFilter empty?");
      }
      
    }

    return result;
   
  }

  public void retrieveGroupsAddRecord(List<ProvisioningGroup> result, String[] colNames, List<Object[]> groupAttributeValues, 
      Map<Object, List<Object[]>> groupIdentifierToAttributes, String groupTableIdColumn) {
    
    for (Object[] groupAttributeValue: GrouperUtil.nonNull(groupAttributeValues)) {
      ProvisioningGroup provisioningGroup = new ProvisioningGroup();
 
      for (int i=0; i<colNames.length; i++) {
        String colName = colNames[i];
        
        GrouperProvisioningConfigurationAttribute configurationAttribute = this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration()
            .getTargetGroupAttributeNameToConfig().get(colName);
        if (configurationAttribute.isMultiValued()) {
          throw new RuntimeException("An attribute that's a column of the primary table to be provisioned cannot be multivalued. "+colName);
        }
        
        Object value = groupAttributeValue[i];

        if (StringUtils.equalsIgnoreCase(groupTableIdColumn, colName)) {
          List<Object[]> moreAttributesFromSeparateAttributesTable = groupIdentifierToAttributes.get(value);
          
          for (Object[] namesValues: GrouperUtil.nonNull(moreAttributesFromSeparateAttributesTable)) {
            String attributeName = namesValues[1].toString();
            Object attributeValue = namesValues[2];
            
            GrouperProvisioningConfigurationAttribute configurationAttributeForSeparateTable = this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration()
                .getTargetGroupAttributeNameToConfig().get(attributeName);
            
            if(configurationAttributeForSeparateTable.isMultiValued()) {
              provisioningGroup.addAttributeValue(attributeName, attributeValue);
            } else {
              provisioningGroup.assignAttributeValue(attributeName, attributeValue);
            }
            
          }
          
        }
 
        provisioningGroup.assignAttributeValue(colName, value);
                
      }
            
      result.add(provisioningGroup);
    }
  }
  
  public void retrieveEntitiesAddRecord(List<ProvisioningEntity> result, String[] colNames, List<Object[]> entityAttributeValues,
      Map<Object, List<Object[]>> entityIdentifierToAttributes, String entityTableIdColumn) {
    
    
    for (Object[] entityAttributeValue: GrouperUtil.nonNull(entityAttributeValues)) {
      ProvisioningEntity provisioningEntity = new ProvisioningEntity();
 
      for (int i=0; i<colNames.length; i++) {
        String colName = colNames[i];
        
        Object value = entityAttributeValue[i];

        if (StringUtils.equalsIgnoreCase(entityTableIdColumn, colName)) {
          List<Object[]> moreAttributesFromSeparateAttributesTable = entityIdentifierToAttributes.get(value);
          
          for (Object[] namesValues: GrouperUtil.nonNull(moreAttributesFromSeparateAttributesTable)) {
            //TODO Vivek - handle multivalued attributes 
            provisioningEntity.assignAttributeValue(namesValues[1].toString(), namesValues[2]);
          }
          
        }
 
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
    
    List<ProvisioningEntity> targetEntities = retrieveEntities(true, null);
    return new TargetDaoRetrieveAllEntitiesResponse(targetEntities);
    
  }
  

  @Override
  public TargetDaoRetrieveEntitiesResponse retrieveEntities(TargetDaoRetrieveEntitiesRequest targetDaoRetrieveEntitiesRequest) {
    
    List<ProvisioningEntity> grouperTargetEntities = targetDaoRetrieveEntitiesRequest == null ? null : targetDaoRetrieveEntitiesRequest.getTargetEntities();
    List<ProvisioningEntity> targetEntities = this.retrieveEntities(false, grouperTargetEntities);
    return new TargetDaoRetrieveEntitiesResponse(targetEntities);
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
//    grouperProvisionerDaoCapabilities.setCanUpdateGroup(true);
    grouperProvisionerDaoCapabilities.setCanUpdateGroups(true);
    grouperProvisionerDaoCapabilities.setCanUpdateEntity(true);
    
  }

}
