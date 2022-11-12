package edu.internet2.middleware.grouper.app.sqlProvisioning;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningBehaviorMembershipType;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfigurationAttribute;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningEntity;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningGroup;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningMembership;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningObjectChange;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningObjectChangeAction;
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
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveMembershipsRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveMembershipsResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoUpdateEntitiesRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoUpdateEntitiesResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoUpdateGroupsRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoUpdateGroupsResponse;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;

/**
 * 
 * @author mchyzer
 *
 */
public class SqlProvisioningDao extends GrouperProvisionerTargetDaoBase {

  /**
   * 
   */
  @Override
  public TargetDaoRetrieveAllMembershipsResponse retrieveAllMemberships(TargetDaoRetrieveAllMembershipsRequest targetDaoRetrieveAllMembershipsRequest) {
    SqlProvisioningConfiguration sqlProvisioningConfiguration = (SqlProvisioningConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
    
    String dbExternalSystemConfigId = sqlProvisioningConfiguration.getDbExternalSystemConfigId();

    List<String> membershipTablePrimaryColNamesList = new ArrayList<String>();

    String membershipTableName = sqlProvisioningConfiguration.getMembershipTableName();
    
    List<ProvisioningMembership> result = new ArrayList<ProvisioningMembership>();

    Map<String, GrouperProvisioningConfigurationAttribute> membershipAttributeNameToConfigAttribute = sqlProvisioningConfiguration.getTargetMembershipAttributeNameToConfig();
    
    for (String attributeName: membershipAttributeNameToConfigAttribute.keySet()) {
      GrouperProvisioningConfigurationAttribute configurationAttribute = membershipAttributeNameToConfigAttribute.get(attributeName);
      if (!configurationAttribute.isSelect()) {
        continue;
      }

      membershipTablePrimaryColNamesList.add(attributeName);
    }

    GrouperUtil.assertion(GrouperUtil.length(membershipTablePrimaryColNamesList) > 0, "Cannot find any membership columns to select from");
    
    List<Object[]> membershipPrimaryAttributeValues = null;

    membershipPrimaryAttributeValues = SqlProvisionerCommands.retrieveObjectsNoFilter(dbExternalSystemConfigId, membershipTablePrimaryColNamesList, membershipTableName);

    retrieveMembershipsAddRecord(result, membershipPrimaryAttributeValues, membershipTablePrimaryColNamesList);
            
    return new TargetDaoRetrieveAllMembershipsResponse(result);
  }

  /**
   * bulk retrieve target provisioning Memberships, generally use the matching Ids in the targetMemberships
   * @param targetDaoRetrieveMembershipsRequest
   * @return the target provisioning Memberships
   */
  public TargetDaoRetrieveMembershipsResponse retrieveMemberships(TargetDaoRetrieveMembershipsRequest targetDaoRetrieveMembershipsRequest) {
    TargetDaoRetrieveMembershipsResponse targetDaoRetrieveMembershipsResponse = new TargetDaoRetrieveMembershipsResponse();
    if (GrouperUtil.length(targetDaoRetrieveMembershipsRequest.getTargetMemberships()) == 0) {
      return targetDaoRetrieveMembershipsResponse;
    }
    
    List<ProvisioningGroup> grouperTargetGroups = new ArrayList<ProvisioningGroup>();
    List<ProvisioningEntity> grouperTargetEntities = new ArrayList<ProvisioningEntity>();
    List<ProvisioningMembership> grouperTargetMemberships = new ArrayList<ProvisioningMembership>();
    
    for (Object object : targetDaoRetrieveMembershipsRequest.getTargetMemberships()) {
      if (object instanceof ProvisioningGroup) {
        grouperTargetGroups.add((ProvisioningGroup)object);
      }
      if (object instanceof ProvisioningEntity) {
        grouperTargetEntities.add((ProvisioningEntity)object);
      }
      if (object instanceof ProvisioningMembership) {
        grouperTargetMemberships.add((ProvisioningMembership)object);
      }
    }
    
    List<ProvisioningMembership> memberships = retrieveMemberships(grouperTargetGroups, grouperTargetEntities, (List<Object>)(Object)grouperTargetMemberships);
    targetDaoRetrieveMembershipsResponse.setTargetMemberships((List<Object>)(Object)memberships);
    return targetDaoRetrieveMembershipsResponse;
  }

  /**
   * 
   * @param grouperTargetGroups
   * @param grouperTargetEntities
   * @param grouperTargetMembershipsInput
   * @return memberships
   */
  public List<ProvisioningMembership> retrieveMemberships(List<ProvisioningGroup> grouperTargetGroups, 
      List<ProvisioningEntity> grouperTargetEntities, List<Object> grouperTargetMembershipsInput) {
    
    SqlProvisioningConfiguration sqlProvisioningConfiguration = (SqlProvisioningConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
    
    String dbExternalSystemConfigId = sqlProvisioningConfiguration.getDbExternalSystemConfigId();

    String membershipGroupColumn = sqlProvisioningConfiguration.getMembershipGroupMatchingIdAttribute();
    String membershipEntityColumn = sqlProvisioningConfiguration.getMembershipEntityMatchingIdAttribute();
    
    String entityTableIdColumn = sqlProvisioningConfiguration.getEntityTableIdColumn();
    String groupTableIdColumn = sqlProvisioningConfiguration.getGroupTableIdColumn();

    String membershipTableName = sqlProvisioningConfiguration.getMembershipTableName();
    
    List<ProvisioningMembership> overallResult = new ArrayList<ProvisioningMembership>();

    Map<String, GrouperProvisioningConfigurationAttribute> entityAttributeNameToConfigAttribute = sqlProvisioningConfiguration.getTargetMembershipAttributeNameToConfig();

    List<String> membershipColumnNames = new ArrayList<String>();
    
    for (String attributeName: entityAttributeNameToConfigAttribute.keySet()) {
      SqlGrouperProvisioningConfigurationAttribute configurationAttribute = (SqlGrouperProvisioningConfigurationAttribute) entityAttributeNameToConfigAttribute.get(attributeName);
      GrouperUtil.assertion(configurationAttribute!=null, "Configuration attribute is null: '" + attributeName + "'");
      if (!configurationAttribute.isSelect()) {
        continue;
      }
      membershipColumnNames.add(attributeName);
    }
    
    GrouperUtil.assertion(membershipColumnNames.contains(membershipEntityColumn), "Entity id col in membership table is not selectable!  Configure as select true! " + entityTableIdColumn);
    GrouperUtil.assertion(membershipColumnNames.contains(membershipGroupColumn), "Group id col in membership table is not selectable!  Configure as select true! " + groupTableIdColumn);

    List<Object> grouperTargetMemberships = new ArrayList<Object>();

    for (Object grouperTargetMembershipMultiKey : GrouperUtil.nonNull(grouperTargetMembershipsInput)) {
      ProvisioningMembership grouperTargetMembership = (ProvisioningMembership)grouperTargetMembershipMultiKey;
      if (grouperTargetMembership != null) {
        Object groupId = grouperTargetMembership.retrieveAttributeValue(membershipGroupColumn);
        Object entityId = grouperTargetMembership.retrieveAttributeValue(membershipEntityColumn);
        grouperTargetMemberships.add(new Object[] {groupId, entityId});
      }
    }
    
    if (StringUtils.isNotBlank(membershipTableName)) {

      if (GrouperUtil.length(grouperTargetMemberships) > 0) {
        List<Object[]> membershipAttributeValues = SqlProvisionerCommands.retrieveObjectsColumnFilter(
            dbExternalSystemConfigId, membershipColumnNames, membershipTableName, null, null, 
            GrouperUtil.toList(membershipGroupColumn, membershipEntityColumn), grouperTargetMemberships, 
            sqlProvisioningConfiguration.getSqlDeletedColumnName(), false);
        
        retrieveMembershipsAddRecord(overallResult, membershipAttributeValues, membershipColumnNames);
      }

      if (GrouperUtil.length(grouperTargetGroups) > 0) {
        
        List<Object> idsToRetrieve = new ArrayList<Object>();
        for (ProvisioningGroup provisioningGroup : grouperTargetGroups) {
          idsToRetrieve.add(provisioningGroup.retrieveAttributeValue(groupTableIdColumn));
        }
        
        List<Object[]> membershipAttributeValues = SqlProvisionerCommands.retrieveObjectsColumnFilter(
            dbExternalSystemConfigId, membershipColumnNames, membershipTableName, null, null, 
            GrouperUtil.toList(membershipGroupColumn), idsToRetrieve, sqlProvisioningConfiguration.getSqlDeletedColumnName(), false);
        
        retrieveMembershipsAddRecord(overallResult, membershipAttributeValues, membershipColumnNames);
      }

      if (GrouperUtil.length(grouperTargetEntities) > 0) { 
        
        List<Object> idsToRetrieve = new ArrayList<Object>();
        for (ProvisioningEntity provisioningEntity : grouperTargetEntities) {
          idsToRetrieve.add(provisioningEntity.retrieveAttributeValue(entityTableIdColumn));
        }
        
        List<Object[]> membershipAttributeValues = SqlProvisionerCommands.retrieveObjectsColumnFilter(
            dbExternalSystemConfigId, membershipColumnNames, membershipTableName, null, null, 
            GrouperUtil.toList(membershipGroupColumn), idsToRetrieve, sqlProvisioningConfiguration.getSqlDeletedColumnName(), false);
        
        retrieveMembershipsAddRecord(overallResult, membershipAttributeValues, membershipColumnNames);
      }

    }
    
    return overallResult;
  }
  
  /**
   * 
   * @param result
   * @param membershipPrimaryAttributeValues
   * @param membershipTablePrimaryColNamesList
   */
  protected void retrieveMembershipsAddRecord(List<ProvisioningMembership> result, 
      List<Object[]> membershipPrimaryAttributeValues, 
      List<String> membershipTablePrimaryColNamesList) {
    
    for (Object[] membershipAttributeValue: GrouperUtil.nonNull(membershipPrimaryAttributeValues)) {
      ProvisioningMembership provisioningMembership = new ProvisioningMembership();
 
      for (int i=0; i<membershipTablePrimaryColNamesList.size(); i++) {

        String colName = membershipTablePrimaryColNamesList.get(i);
        
        Object value = membershipAttributeValue[i];
         
        provisioningMembership.assignAttributeValue(colName, value);
                
      }
            
      result.add(provisioningMembership);
    }
    
  }

  /**
   * 
   */
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

    String sqlLastModifiedColumn = sqlProvisioningConfiguration.getSqlLastModifiedColumnName();
    
    String sqlDeletedColumn = sqlProvisioningConfiguration.getSqlDeletedColumnName();
    
    String sqlLastModifiedColumnType = sqlProvisioningConfiguration.getSqlLastModifiedColumnType();
    
    Map<String, GrouperProvisioningConfigurationAttribute> attributeNameToConfig = sqlProvisioningConfiguration.getTargetGroupAttributeNameToConfig();
    
    List<ProvisioningGroup> targetGroups = targetDaoUpdateGroupsRequest.getTargetGroups();
    
    List<String> primaryColumnsWhereClause = new ArrayList<String>();
    primaryColumnsWhereClause.add(groupTableIdColumn);
    List<String> primaryColumnsToUpdate = new ArrayList<String>();
    Map<String, Integer> primaryColumnNameToIndex = new HashMap<String, Integer>();
    
    for (String attributeName: attributeNameToConfig.keySet()) {
      SqlGrouperProvisioningConfigurationAttribute configurationAttribute = (SqlGrouperProvisioningConfigurationAttribute) attributeNameToConfig.get(attributeName);
      GrouperUtil.assertion(configurationAttribute!=null, "Configuration attribute is null: '" + attributeName + "'");
      if (!configurationAttribute.isUpdate()) {
        continue;
      }
      
      if (StringUtils.isBlank(configurationAttribute.getStorageType()) || StringUtils.equals(configurationAttribute.getStorageType(), "groupTableColumn")) {
        primaryColumnNameToIndex.put(attributeName, GrouperUtil.length(primaryColumnsToUpdate));
        primaryColumnsToUpdate.add(attributeName);
      }
      
    }

    List<String> attributeColumnsToDelete = GrouperUtil.toList(groupAttributesGroupForeignKeyColumn, 
        groupAttributesAttributeNameColumn, groupAttributesAttributeValueColumn);

    List<String> attributeColumnsToInsert = GrouperUtil.toList(groupAttributesGroupForeignKeyColumn, 
        groupAttributesAttributeNameColumn, groupAttributesAttributeValueColumn);

    List<String> attributeColumnsWhereClause = GrouperUtil.toList(groupAttributesGroupForeignKeyColumn, 
        groupAttributesAttributeNameColumn, groupAttributesAttributeValueColumn);
    
    if (!StringUtils.isBlank(sqlLastModifiedColumn)) {
      attributeColumnsToInsert.add(sqlLastModifiedColumn);
    }
    
    if (!StringUtils.isBlank(sqlDeletedColumn)) {
      attributeColumnsToInsert.add(sqlDeletedColumn);
    }
    
    // owner id, attribute name, old value, new value, last updated
    List<String> attributeColumnsToUpdate = GrouperUtil.toList(groupAttributesGroupForeignKeyColumn, 
        groupAttributesAttributeNameColumn, groupAttributesAttributeValueColumn);
    if (!StringUtils.isBlank(sqlLastModifiedColumn)) {
      attributeColumnsToUpdate.add(sqlLastModifiedColumn);
    }
    if (!StringUtils.isBlank(sqlDeletedColumn)) {
      attributeColumnsToUpdate.add(sqlDeletedColumn);
    }

    List<Object[]> primaryTableUpdates = new ArrayList<Object[]>();
    List<Object[]> primaryTableWhereClause = new ArrayList<Object[]>();
    List<Object[]> attributeTableUpdates = new ArrayList<Object[]>();
    List<Object[]> attributeTableUpdatesWhereClause = new ArrayList<Object[]>();
    List<Object[]> attributeTableInserts = new ArrayList<Object[]>();
    List<Object[]> attributeTableDeletes = new ArrayList<Object[]>();
    List<Object[]> attributeTableInsertDeletes = new ArrayList<Object[]>();
    List<Object[]> attributeTableDeletesWhereClause = new ArrayList<Object[]>();
    
    for (ProvisioningGroup targetGroup: targetGroups) {
      
      Object groupIdentifierValueNew = targetGroup.retrieveAttributeValue(groupTableIdColumn);
      Object groupIdentifierValueOld = groupIdentifierValueNew;
      if (targetGroup.getProvisioningGroupWrapper() != null && targetGroup.getProvisioningGroupWrapper().getTargetProvisioningGroup() != null) {
        groupIdentifierValueOld = targetGroup.getProvisioningGroupWrapper().getTargetProvisioningGroup().retrieveAttributeValue(groupTableIdColumn);
        if (GrouperUtil.isBlank(groupIdentifierValueOld)) {
          groupIdentifierValueOld = groupIdentifierValueNew;
        }
      }
      
      if (GrouperUtil.isBlank(groupIdentifierValueNew)) {
        throw new RuntimeException("Unable to retrieve entitiy identifier value");
      }
      Object[] mainData = new Object[primaryColumnsToUpdate.size()];
      
      // set all the current values and the new values will overlay
      for (String mainAttributeName : primaryColumnsToUpdate) {
        mainData[primaryColumnNameToIndex.get(mainAttributeName)] = targetGroup.retrieveAttributeValue(mainAttributeName);
      }
      boolean hasPrimaryUpdate = false;
      for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetGroup.getInternal_objectChanges())) {
        
        String attributeName = provisioningObjectChange.getAttributeName();
        
        if (StringUtils.equals(attributeName, groupTableIdColumn) && provisioningObjectChange.getProvisioningObjectChangeAction() == ProvisioningObjectChangeAction.update 
            && !GrouperUtil.isBlank(provisioningObjectChange.getOldValue()) 
            && !GrouperUtil.equals(groupIdentifierValueNew, provisioningObjectChange.getOldValue())) {
          groupIdentifierValueOld = GrouperUtil.stringValue(provisioningObjectChange.getOldValue());
        }
        
        SqlGrouperProvisioningConfigurationAttribute configurationAttribute = (SqlGrouperProvisioningConfigurationAttribute) attributeNameToConfig.get(attributeName);
        GrouperUtil.assertion(configurationAttribute!=null, "Configuration attribute is null: '" + attributeName + "'");
        if (StringUtils.equals(configurationAttribute.getStorageType(), "separateAttributesTable")) {
          
          if (provisioningObjectChange.getProvisioningObjectChangeAction() == ProvisioningObjectChangeAction.update) {
            
            Object[] updateData = new Object[attributeColumnsToUpdate.size()];
            attributeTableUpdates.add(updateData);
            updateData[0] = groupIdentifierValueNew;
            updateData[1] = attributeName;
            updateData[2] = provisioningObjectChange.getNewValue();
            if (StringUtils.isNotBlank(sqlLastModifiedColumn)) {
              if (StringUtils.equals(sqlLastModifiedColumnType, "timestamp")) {
                updateData[3] = new Timestamp(lastModified());
              } else if (StringUtils.equals(sqlLastModifiedColumnType, "long")) {
                updateData[3] = lastModified();
              } else {
                throw new RuntimeException("Invalid sqlLastModifiedColumnType: '"+sqlLastModifiedColumnType+"'");
              }
            }
            if (StringUtils.isNotBlank(sqlDeletedColumn)) {
              int columnIndex = StringUtils.isNotBlank(sqlLastModifiedColumn) ? 4: 3;
              updateData[columnIndex] = "F";
            }
            attributeTableUpdatesWhereClause.add(new Object[] {groupIdentifierValueNew, attributeName, provisioningObjectChange.getOldValue()});
            
          } else if (provisioningObjectChange.getProvisioningObjectChangeAction() == ProvisioningObjectChangeAction.insert) {
            
            Object[] insertDelete = new Object[attributeColumnsToDelete.size()];
            attributeTableInsertDeletes.add(insertDelete);
            insertDelete[0] = groupIdentifierValueNew;
            insertDelete[1] = attributeName;
            insertDelete[2] = provisioningObjectChange.getNewValue();
            
            int attributeColumnsToInsertSize = attributeColumnsToInsert.size();
            Object[] insertData = new Object[attributeColumnsToInsertSize];
            attributeTableInserts.add(insertData);
            insertData[0] = groupIdentifierValueNew;
            insertData[1] = attributeName;
            insertData[2] = provisioningObjectChange.getNewValue();
            if (StringUtils.isNotBlank(sqlLastModifiedColumn)) {
              if (StringUtils.equals(sqlLastModifiedColumnType, "timestamp")) {
                insertData[3] = new Timestamp(lastModified());
              } else if (StringUtils.equals(sqlLastModifiedColumnType, "long")) {
                insertData[3] = lastModified();
              } else {
                throw new RuntimeException("Invalid sqlLastModifiedColumnType: '"+sqlLastModifiedColumnType+"'");
              }
            }
            
            if (StringUtils.isNotBlank(sqlDeletedColumn)) {
              int columnIndex = StringUtils.isNotBlank(sqlLastModifiedColumn) ? 4: 3;
              insertData[columnIndex] = "F";
            }

          } else if (provisioningObjectChange.getProvisioningObjectChangeAction() == ProvisioningObjectChangeAction.delete) {
            Object[] deleteData = new Object[attributeColumnsToDelete.size()];
            attributeTableDeletes.add(deleteData);
            deleteData[0] = groupIdentifierValueNew;
            deleteData[1] = attributeName;
            deleteData[2] = provisioningObjectChange.getOldValue();
            attributeTableDeletesWhereClause.add(new Object[] {groupIdentifierValueNew});
          }
          
        } else {
          
          Integer columnIndex = primaryColumnNameToIndex.get(attributeName);
          GrouperUtil.assertion(columnIndex!=null, "Cant find column: " + attributeName);
          mainData[columnIndex] = provisioningObjectChange.getNewValue();
          hasPrimaryUpdate = true;
        }
        
      }
      if (hasPrimaryUpdate) {
        
        if (StringUtils.isNotBlank(sqlLastModifiedColumn)) {
          Integer columnIndex = primaryColumnNameToIndex.get(sqlLastModifiedColumn);
          if (columnIndex == null) {
            throw new RuntimeException("You must have an attribute in the groups table called: "+sqlLastModifiedColumn + " because"
                + " you set the configuration last modified sql column name so it must exist on all provisioned tables");
          }
          if (StringUtils.equals("long", sqlLastModifiedColumnType)) {
            mainData[columnIndex] = lastModified(); 
          } else if (StringUtils.equals("timestamp", sqlLastModifiedColumnType)) {
            mainData[columnIndex] = new Timestamp(lastModified());
          } else {
            throw new RuntimeException("Invalid sqlLastModifiedColumnType: '"+sqlLastModifiedColumnType+"'");
          } 
        }
        
        if (StringUtils.isNotBlank(sqlProvisioningConfiguration.getSqlDeletedColumnName())) {
          int columnIndex = primaryColumnNameToIndex.get(sqlProvisioningConfiguration.getSqlDeletedColumnName());
          mainData[columnIndex] = "F";
        }
        
        primaryTableUpdates.add(mainData);
        primaryTableWhereClause.add(new Object[] {groupIdentifierValueOld});
      }

      // if the groupIdentiferNew and groupIdentifierOld do not match...
      if (!GrouperUtil.equals(groupIdentifierValueNew, groupIdentifierValueOld) && sqlProvisioningConfiguration.isUseSeparateTableForGroupAttributes()) {

        List<Object[]> groupAttributesGroupForeignKeyColumnOld = new ArrayList<Object[]>();
        groupAttributesGroupForeignKeyColumnOld.add(new Object[] {groupIdentifierValueOld});
        List<Object[]> groupAttributesGroupForeignKeyColumnNew = new ArrayList<Object[]>();
        groupAttributesGroupForeignKeyColumnNew.add(new Object[] {groupIdentifierValueNew});
        

        SqlProvisionerCommands.updateObjects(dbExternalSystemConfigId, groupAttributesTableName, 
           GrouperUtil.toList(groupAttributesGroupForeignKeyColumn), groupAttributesGroupForeignKeyColumnNew, 
           GrouperUtil.toList(groupAttributesGroupForeignKeyColumn), groupAttributesGroupForeignKeyColumnOld);
      }
    }      
    if (primaryTableUpdates.size() > 0) {
      
      SqlProvisionerCommands.updateObjects(dbExternalSystemConfigId, groupTableName, primaryColumnsToUpdate, primaryTableUpdates,
          primaryColumnsWhereClause, primaryTableWhereClause);
      
    }
    
    if (attributeTableUpdates.size() > 0 ) {
      
      SqlProvisionerCommands.updateObjects(dbExternalSystemConfigId, groupAttributesTableName, attributeColumnsToUpdate, 
          attributeTableUpdates, attributeColumnsWhereClause, attributeTableUpdatesWhereClause);
      
    }
    
    if (attributeTableInserts.size() > 0 ) {
      
      SqlProvisionerCommands.deleteObjects(attributeTableInsertDeletes, dbExternalSystemConfigId, groupAttributesTableName, 
          attributeColumnsToDelete, null, null, 
          sqlDeletedColumn, false, true);
      
      SqlProvisionerCommands.insertObjects(dbExternalSystemConfigId, groupAttributesTableName, attributeColumnsToInsert, attributeTableInserts); 
      
    }
    
    if (attributeTableDeletes.size() > 0 ) {
      SqlProvisionerCommands.deleteObjects(attributeTableDeletes, dbExternalSystemConfigId, groupAttributesTableName, 
          attributeColumnsToDelete, null, null, sqlProvisioningConfiguration.getSqlDeletedColumnName(), false, 
          StringUtils.isBlank(sqlProvisioningConfiguration.getSqlDeletedColumnName())); 
      
    }
    
    for (ProvisioningGroup targetGroup: targetGroups) {

      targetGroup.setProvisioned(true);
      for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetGroup.getInternal_objectChanges())) {
        provisioningObjectChange.setProvisioned(true);
      }

    }

    
    return new TargetDaoUpdateGroupsResponse();
  }
  
  /**
   * 
   */
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

    String sqlLastModifiedColumn = sqlProvisioningConfiguration.getSqlLastModifiedColumnName();
    String sqlDeletedColumn = sqlProvisioningConfiguration.getSqlDeletedColumnName();
    
    String sqlLastModifiedColumnType = sqlProvisioningConfiguration.getSqlLastModifiedColumnType();
    
    Map<String, GrouperProvisioningConfigurationAttribute> attributeNameToConfig = sqlProvisioningConfiguration.getTargetEntityAttributeNameToConfig();
    
    List<ProvisioningEntity> targetEntities = targetDaoUpdateEntitiesRequest.getTargetEntities();
    
    List<String> primaryColumnsWhereClause = new ArrayList<String>();
    primaryColumnsWhereClause.add(entityTableIdColumn);
    List<String> primaryColumnsToUpdate = new ArrayList<String>();
    Map<String, Integer> primaryColumnNameToIndex = new HashMap<String, Integer>();
    
    for (String attributeName: attributeNameToConfig.keySet()) {
      SqlGrouperProvisioningConfigurationAttribute configurationAttribute = (SqlGrouperProvisioningConfigurationAttribute) attributeNameToConfig.get(attributeName);
      GrouperUtil.assertion(configurationAttribute!=null, "Configuration attribute is null: '" + attributeName + "'");
      if (!configurationAttribute.isUpdate()) {
        continue;
      }
      
      if (StringUtils.isBlank(configurationAttribute.getStorageType()) || StringUtils.equals(configurationAttribute.getStorageType(), "entityTableColumn")) {
        primaryColumnNameToIndex.put(attributeName, GrouperUtil.length(primaryColumnsToUpdate));
        primaryColumnsToUpdate.add(attributeName);
      }
      
    }

    List<String> attributeColumnsToDelete = GrouperUtil.toList(entityAttributesEntityForeignKeyColumn, 
        entityAttributesAttributeNameColumn, entityAttributesAttributeValueColumn);

    List<String> attributeColumnsToInsert = GrouperUtil.toList(entityAttributesEntityForeignKeyColumn, 
        entityAttributesAttributeNameColumn, entityAttributesAttributeValueColumn);

    List<String> attributeColumnsWhereClause = GrouperUtil.toList(entityAttributesEntityForeignKeyColumn, 
        entityAttributesAttributeNameColumn, entityAttributesAttributeValueColumn);
    
    if (!StringUtils.isBlank(sqlLastModifiedColumn)) {
      attributeColumnsToInsert.add(sqlLastModifiedColumn);
    }

    if (!StringUtils.isBlank(sqlDeletedColumn)) {
      attributeColumnsToInsert.add(sqlDeletedColumn);
    }

    // owner id, attribute name, old value, new value, last updated
    List<String> attributeColumnsToUpdate = GrouperUtil.toList(entityAttributesEntityForeignKeyColumn, 
        entityAttributesAttributeNameColumn, entityAttributesAttributeValueColumn);
    if (!StringUtils.isBlank(sqlLastModifiedColumn)) {
      attributeColumnsToUpdate.add(sqlLastModifiedColumn);
    }
    if (!StringUtils.isBlank(sqlDeletedColumn)) {
      attributeColumnsToUpdate.add(sqlDeletedColumn);
    }

    List<Object[]> primaryTableUpdates = new ArrayList<Object[]>();
    List<Object[]> primaryTableWhereClause = new ArrayList<Object[]>();
    List<Object[]> attributeTableUpdates = new ArrayList<Object[]>();
    List<Object[]> attributeTableUpdatesWhereClause = new ArrayList<Object[]>();
    List<Object[]> attributeTableInserts = new ArrayList<Object[]>();
    List<Object[]> attributeTableDeletes = new ArrayList<Object[]>();
    List<Object[]> attributeTableInsertDeletes = new ArrayList<Object[]>();
    List<Object[]> attributeTableDeletesWhereClause = new ArrayList<Object[]>();
    
    for (ProvisioningEntity targetEntity: targetEntities) {
      
      Object entityIdentifierValueNew = targetEntity.retrieveAttributeValue(entityTableIdColumn);
      Object entityIdentifierValueOld = entityIdentifierValueNew;
      if (targetEntity.getProvisioningEntityWrapper() != null && targetEntity.getProvisioningEntityWrapper().getTargetProvisioningEntity() != null) {
        entityIdentifierValueOld = targetEntity.getProvisioningEntityWrapper().getTargetProvisioningEntity().retrieveAttributeValue(entityTableIdColumn);
        if (GrouperUtil.isBlank(entityIdentifierValueOld)) {
          entityIdentifierValueOld = entityIdentifierValueNew;
        }
      }

      if (GrouperUtil.isBlank(entityIdentifierValueNew)) {
        throw new RuntimeException("Unable to retrieve entitiy identifier value");
      }
      Object[] mainData = new Object[primaryColumnsToUpdate.size()];
      
      // set all the current values and the new values will overlay
      for (String mainAttributeName : primaryColumnsToUpdate) {
        mainData[primaryColumnNameToIndex.get(mainAttributeName)] = targetEntity.retrieveAttributeValue(mainAttributeName);
      }
      boolean hasPrimaryUpdate = false;
      for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetEntity.getInternal_objectChanges())) {
        
        String attributeName = provisioningObjectChange.getAttributeName();
        
        if (StringUtils.equals(attributeName, entityTableIdColumn) && provisioningObjectChange.getProvisioningObjectChangeAction() == ProvisioningObjectChangeAction.update 
            && !GrouperUtil.isBlank(provisioningObjectChange.getOldValue()) 
            && !GrouperUtil.equals(entityIdentifierValueNew, provisioningObjectChange.getOldValue())) {
          entityIdentifierValueOld = GrouperUtil.stringValue(provisioningObjectChange.getOldValue());
        }

        SqlGrouperProvisioningConfigurationAttribute configurationAttribute = (SqlGrouperProvisioningConfigurationAttribute) attributeNameToConfig.get(attributeName);
        GrouperUtil.assertion(configurationAttribute!=null, "Configuration attribute is null: '" + attributeName + "'");
        if (StringUtils.equals(configurationAttribute.getStorageType(), "separateAttributesTable")) {
          
          if (provisioningObjectChange.getProvisioningObjectChangeAction() == ProvisioningObjectChangeAction.update) {
            
            Object[] updateData = new Object[attributeColumnsToUpdate.size()];
            attributeTableUpdates.add(updateData);
            updateData[0] = entityIdentifierValueNew;
            updateData[1] = attributeName;
            updateData[2] = provisioningObjectChange.getNewValue();
            if (StringUtils.isNotBlank(sqlLastModifiedColumn)) {
              if (StringUtils.equals(sqlLastModifiedColumnType, "timestamp")) {
                updateData[3] = new Timestamp(lastModified());
              } else if (StringUtils.equals(sqlLastModifiedColumnType, "long")) {
                updateData[3] = lastModified();
              } else {
                throw new RuntimeException("Invalid entityAttributesLastModifiedColumnType: '"+sqlLastModifiedColumnType+"'");
              }
            }
            
            if (StringUtils.isNotBlank(sqlDeletedColumn)) {
              int columnIndex = StringUtils.isNotBlank(sqlLastModifiedColumn) ? 4: 3;
              updateData[columnIndex] = "F";
            }
            
            attributeTableUpdatesWhereClause.add(new Object[] {entityIdentifierValueNew, attributeName, provisioningObjectChange.getOldValue()});
            
          } else if (provisioningObjectChange.getProvisioningObjectChangeAction() == ProvisioningObjectChangeAction.insert) {
            
            Object[] insertDelete = new Object[attributeColumnsToDelete.size()];
            attributeTableInsertDeletes.add(insertDelete);
            insertDelete[0] = entityIdentifierValueNew;
            insertDelete[1] = attributeName;
            insertDelete[2] = provisioningObjectChange.getNewValue();
            
            int attributeColumnsToInsertSize = attributeColumnsToInsert.size();
            if (StringUtils.isNotBlank(sqlLastModifiedColumn)) {
              attributeColumnsToInsertSize++;
            }
            if (StringUtils.isNotBlank(sqlDeletedColumn)) {
              attributeColumnsToInsertSize++;
            }
            Object[] insertData = new Object[attributeColumnsToInsertSize];
            attributeTableInserts.add(insertData);
            insertData[0] = entityIdentifierValueNew;
            insertData[1] = attributeName;
            insertData[2] = provisioningObjectChange.getNewValue();
            
            if (StringUtils.isNotBlank(sqlLastModifiedColumn)) {
              if (StringUtils.equals(sqlLastModifiedColumnType, "timestamp")) {
                insertData[3] = new Timestamp(lastModified());
              } else if (StringUtils.equals(sqlLastModifiedColumnType, "long")) {
                insertData[3] = lastModified();
              } else {
                throw new RuntimeException("Invalid sqlLastModifiedColumnType: '"+sqlLastModifiedColumnType+"'");
              }
            }
            
            if (StringUtils.isNotBlank(sqlDeletedColumn)) {
              int columnIndex = StringUtils.isNotBlank(sqlLastModifiedColumn) ? 4: 3;
              insertData[columnIndex] = "F";
            }

          } else if (provisioningObjectChange.getProvisioningObjectChangeAction() == ProvisioningObjectChangeAction.delete) {
            Object[] deleteData = new Object[attributeColumnsToDelete.size()];
            attributeTableDeletes.add(deleteData);
            deleteData[0] = entityIdentifierValueNew;
            deleteData[1] = attributeName;
            deleteData[2] = provisioningObjectChange.getOldValue();
            attributeTableDeletesWhereClause.add(new Object[] {entityIdentifierValueNew});
          }
          
        } else {
          Integer columnIndex = primaryColumnNameToIndex.get(attributeName);
          GrouperUtil.assertion(columnIndex!=null, "Cant find column: " + attributeName);
          mainData[columnIndex] = provisioningObjectChange.getNewValue();
          hasPrimaryUpdate = true;
        }
        
      }
      if (hasPrimaryUpdate) {
        
        if (StringUtils.isNotBlank(sqlLastModifiedColumn)) {
          Integer columnIndex = primaryColumnNameToIndex.get(sqlLastModifiedColumn);
          if (columnIndex == null) {
            throw new RuntimeException("You must have an attribute in the entities table called: "+sqlLastModifiedColumn + " because"
                + " you set the configuration last modified sql column name so it must exist on all provisioned tables");
          }
          if (StringUtils.equals("long", sqlLastModifiedColumnType)) {
            mainData[columnIndex] = lastModified(); 
          } else if (StringUtils.equals("timestamp", sqlLastModifiedColumnType)) {
            mainData[columnIndex] = new Timestamp(lastModified());
          } else {
            throw new RuntimeException("Invalid sqlLastModifiedColumnType: '"+sqlLastModifiedColumnType+"'");
          } 
        }
        
        if (StringUtils.isNotBlank(sqlProvisioningConfiguration.getSqlDeletedColumnName())) {
          int columnIndex = primaryColumnNameToIndex.get(sqlProvisioningConfiguration.getSqlDeletedColumnName());
          mainData[columnIndex] = "F";
        }
        
        primaryTableUpdates.add(mainData);
        primaryTableWhereClause.add(new Object[] {entityIdentifierValueOld});
      }
      // if the groupIdentiferNew and groupIdentifierOld do not match...
      if (!GrouperUtil.equals(entityIdentifierValueNew, entityIdentifierValueOld) && sqlProvisioningConfiguration.isUseSeparateTableForGroupAttributes()) {

        List<Object[]> entityAttributesEntityForeignKeyColumnOld = new ArrayList<Object[]>();
        entityAttributesEntityForeignKeyColumnOld.add(new Object[] {entityIdentifierValueOld});
        List<Object[]> entityAttributesEntityForeignKeyColumnNew = new ArrayList<Object[]>();
        entityAttributesEntityForeignKeyColumnNew.add(new Object[] {entityIdentifierValueNew});
        

        SqlProvisionerCommands.updateObjects(dbExternalSystemConfigId, entityAttributesTableName, 
           GrouperUtil.toList(entityAttributesEntityForeignKeyColumn), entityAttributesEntityForeignKeyColumnNew, 
           GrouperUtil.toList(entityAttributesEntityForeignKeyColumn), entityAttributesEntityForeignKeyColumnOld);
      }

    }

    if (primaryTableUpdates.size() > 0) {
      SqlProvisionerCommands.updateObjects(dbExternalSystemConfigId, entityTableName, primaryColumnsToUpdate, primaryTableUpdates,
          primaryColumnsWhereClause, primaryTableWhereClause);
    }
    
    if (attributeTableUpdates.size() > 0 ) {
      SqlProvisionerCommands.updateObjects(dbExternalSystemConfigId, entityAttributesTableName, attributeColumnsToUpdate, 
          attributeTableUpdates, attributeColumnsWhereClause, attributeTableUpdatesWhereClause);
    }
    
    if (attributeTableInserts.size() > 0 ) {
      
      SqlProvisionerCommands.deleteObjects(attributeTableInsertDeletes, dbExternalSystemConfigId, entityAttributesTableName, 
          attributeColumnsToDelete, null, null, 
          sqlDeletedColumn, false, true);
      
      SqlProvisionerCommands.insertObjects(dbExternalSystemConfigId, entityAttributesTableName, attributeColumnsToInsert, attributeTableInserts); 
      
    }
    
    if (attributeTableDeletes.size() > 0 ) {
      SqlProvisionerCommands.deleteObjects(attributeTableDeletes, dbExternalSystemConfigId, entityAttributesTableName, 
          attributeColumnsToDelete, null, null, sqlProvisioningConfiguration.getSqlDeletedColumnName(), false, 
          StringUtils.isBlank(sqlProvisioningConfiguration.getSqlDeletedColumnName())); 
    }
    
    for (ProvisioningEntity targetEntity: targetEntities) {
      
      targetEntity.setProvisioned(true);
      for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetEntity.getInternal_objectChanges())) {
        provisioningObjectChange.setProvisioned(true);
      }
 
    }

    
    return new TargetDaoUpdateEntitiesResponse();

  }

  
  /**
   * 
   */
  @Override
  public TargetDaoDeleteGroupsResponse deleteGroups(TargetDaoDeleteGroupsRequest targetDaoDeleteGroupRequest) {
    
    List<ProvisioningGroup> targetGroups = targetDaoDeleteGroupRequest.getTargetGroups();
    
    SqlProvisioningConfiguration sqlProvisioningConfiguration = (SqlProvisioningConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
    
    String dbExternalSystemConfigId = sqlProvisioningConfiguration.getDbExternalSystemConfigId();
    
    String objectTableName = sqlProvisioningConfiguration.getGroupTableName();
    String objectTableIdColumn = sqlProvisioningConfiguration.getGroupTableIdColumn();
    String objectAttributesTableName = sqlProvisioningConfiguration.getGroupAttributesTableName();
    String objectAttributesGroupForeignKeyColumn = sqlProvisioningConfiguration.getGroupAttributesGroupForeignKeyColumn();

    List<Object[]> ownerIds = new ArrayList<Object[]>();
    
    for (ProvisioningGroup targetGroup: targetGroups) {
      Object groupIdValue = targetGroup.retrieveAttributeValue(objectTableIdColumn);
      ownerIds.add(new Object[] {groupIdValue});
    }
    
    if (StringUtils.isBlank(sqlProvisioningConfiguration.getSqlDeletedColumnName())) {
      SqlProvisionerCommands.deleteObjects(ownerIds, dbExternalSystemConfigId, objectTableName, GrouperUtil.toList(objectTableIdColumn),
          objectAttributesTableName, objectAttributesGroupForeignKeyColumn, null, false, true);
    } else {
      
      List<String> columnsToUpdate = new ArrayList<>();
      List<Object[]> valuesToUpdate = new ArrayList<>();
      columnsToUpdate.add(sqlProvisioningConfiguration.getSqlDeletedColumnName());
      if (StringUtils.isNotBlank(sqlProvisioningConfiguration.getSqlLastModifiedColumnName())) {
        columnsToUpdate.add(sqlProvisioningConfiguration.getSqlLastModifiedColumnName());
      }
      
      for (ProvisioningGroup targetGroup: targetGroups) {
        Object[] singleRowValues = new Object[columnsToUpdate.size()];
        singleRowValues[0] = "T";
        if (StringUtils.isNotBlank(sqlProvisioningConfiguration.getSqlLastModifiedColumnName())) {
          if (StringUtils.equals("long", sqlProvisioningConfiguration.getSqlLastModifiedColumnType())) {
            singleRowValues[1] = lastModified(); 
          } else if (StringUtils.equals("timestamp", sqlProvisioningConfiguration.getSqlLastModifiedColumnType())) {
            singleRowValues[1] = new Timestamp(lastModified());
          } else {
            throw new RuntimeException("Invalid sqlLastModifiedColumnType: '"+sqlProvisioningConfiguration.getSqlLastModifiedColumnType()+"'");
          } 
          
        }
        valuesToUpdate.add(singleRowValues);
      }
      
      SqlProvisionerCommands.updateObjects(dbExternalSystemConfigId, objectTableName, columnsToUpdate, valuesToUpdate, GrouperUtil.toList(objectTableIdColumn), ownerIds);

      SqlProvisionerCommands.updateObjects(dbExternalSystemConfigId, objectAttributesTableName, columnsToUpdate, valuesToUpdate, GrouperUtil.toList(objectAttributesGroupForeignKeyColumn), ownerIds);
      
    }
    
    for (ProvisioningGroup targetGroup: targetGroups) {
      
      targetGroup.setProvisioned(true);
      
      for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetGroup.getInternal_objectChanges())) {
        provisioningObjectChange.setProvisioned(true);
      }

    }
  
    return new TargetDaoDeleteGroupsResponse();
  }
  

  /**
   * 
   */
  @Override
  public TargetDaoDeleteEntitiesResponse deleteEntities(TargetDaoDeleteEntitiesRequest targetDaoDeleteEntitiesRequest) {
    
    List<ProvisioningEntity> targetEntities = targetDaoDeleteEntitiesRequest.getTargetEntities();
    
    SqlProvisioningConfiguration sqlProvisioningConfiguration = (SqlProvisioningConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
    
    String dbExternalSystemConfigId = sqlProvisioningConfiguration.getDbExternalSystemConfigId();
    
    String entityTableName = sqlProvisioningConfiguration.getEntityTableName();
    String entityTableIdColumn = sqlProvisioningConfiguration.getEntityTableIdColumn();
    String entityAttributesTableName = sqlProvisioningConfiguration.getEntityAttributesTableName();
    String entityAttributesGroupForeignKeyColumn = sqlProvisioningConfiguration.getEntityAttributesEntityForeignKeyColumn();

    List<Object[]> ownerIds = new ArrayList<Object[]>();

    for (ProvisioningEntity targetEntity: targetEntities) {
      Object entityIdValue = targetEntity.retrieveAttributeValue(entityTableIdColumn);
      ownerIds.add(new Object[] {entityIdValue});
    }
    
    if (StringUtils.isBlank(sqlProvisioningConfiguration.getSqlDeletedColumnName())) {
      SqlProvisionerCommands.deleteObjects(ownerIds, dbExternalSystemConfigId, entityTableName, GrouperUtil.toList(entityTableIdColumn),
          entityAttributesTableName, entityAttributesGroupForeignKeyColumn, null, false, true);
    } else {
      
      List<String> columnsToUpdate = new ArrayList<>();
      List<Object[]> valuesToUpdate = new ArrayList<>();
      columnsToUpdate.add(sqlProvisioningConfiguration.getSqlDeletedColumnName());
      if (StringUtils.isNotBlank(sqlProvisioningConfiguration.getSqlLastModifiedColumnName())) {
        columnsToUpdate.add(sqlProvisioningConfiguration.getSqlLastModifiedColumnName());
      }
      
      for (ProvisioningEntity targetEntity: targetEntities) {
        Object[] singleRowValues = new Object[columnsToUpdate.size()];
        singleRowValues[0] = "T";
        if (StringUtils.isNotBlank(sqlProvisioningConfiguration.getSqlLastModifiedColumnName())) {
          if (StringUtils.equals("long", sqlProvisioningConfiguration.getSqlLastModifiedColumnType())) {
            singleRowValues[1] = lastModified(); 
          } else if (StringUtils.equals("timestamp", sqlProvisioningConfiguration.getSqlLastModifiedColumnType())) {
            singleRowValues[1] = new Timestamp(lastModified());
          } else {
            throw new RuntimeException("Invalid sqlLastModifiedColumnType: '"+sqlProvisioningConfiguration.getSqlLastModifiedColumnType()+"'");
          } 
          
        }
        valuesToUpdate.add(singleRowValues);
      }
      
      SqlProvisionerCommands.updateObjects(dbExternalSystemConfigId, entityTableName, columnsToUpdate, valuesToUpdate, GrouperUtil.toList(entityTableIdColumn), ownerIds);

      SqlProvisionerCommands.updateObjects(dbExternalSystemConfigId, entityAttributesTableName, columnsToUpdate, valuesToUpdate, GrouperUtil.toList(entityAttributesGroupForeignKeyColumn), ownerIds);
      
    }
    
    for (ProvisioningEntity targetEntity: targetEntities) {
      
      targetEntity.setProvisioned(true);
      
      for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetEntity.getInternal_objectChanges())) {
        provisioningObjectChange.setProvisioned(true);
      }

    }
    
    return new TargetDaoDeleteEntitiesResponse();
    
  }

  /**
   * 
   */
  @Override
  public TargetDaoDeleteMembershipsResponse deleteMemberships(TargetDaoDeleteMembershipsRequest targetDaoDeleteMembershipsRequest) {
    
    List<ProvisioningMembership> targetMemberships = targetDaoDeleteMembershipsRequest.getTargetMemberships();
    
    SqlProvisioningConfiguration sqlProvisioningConfiguration = (SqlProvisioningConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
    
    String dbExternalSystemConfigId = sqlProvisioningConfiguration.getDbExternalSystemConfigId();

    String objectTableName = sqlProvisioningConfiguration.getMembershipTableName();
    
    GrouperUtil.assertion(!StringUtils.isBlank(objectTableName), "Need membership table name");
    
    String groupIdColumn = sqlProvisioningConfiguration.getMembershipGroupMatchingIdAttribute();
    String entityIdColumn = sqlProvisioningConfiguration.getMembershipEntityMatchingIdAttribute();

    List<Object[]> ownerIds = new ArrayList<Object[]>();
    
    for (ProvisioningMembership targetMembership: targetMemberships) {
      MultiKey membershipMatchingId = new MultiKey(targetMembership.retrieveAttributeValue(groupIdColumn), targetMembership.retrieveAttributeValue(entityIdColumn));
      ownerIds.add(membershipMatchingId.getKeys());
    }
    
    if (StringUtils.isBlank(sqlProvisioningConfiguration.getSqlDeletedColumnName())) {
      
      SqlProvisionerCommands.deleteObjects(ownerIds, dbExternalSystemConfigId, objectTableName, 
          GrouperUtil.toList(groupIdColumn, entityIdColumn), null, null, null, false, true);
      
    } else {
      
      List<String> columnsToUpdate = new ArrayList<>();
      List<Object[]> valuesToUpdate = new ArrayList<>();
      columnsToUpdate.add(sqlProvisioningConfiguration.getSqlDeletedColumnName());
      if (StringUtils.isNotBlank(sqlProvisioningConfiguration.getSqlLastModifiedColumnName())) {
        columnsToUpdate.add(sqlProvisioningConfiguration.getSqlLastModifiedColumnName());
      }
      
      for (ProvisioningMembership targetMembership: targetMemberships) {
        Object[] singleRowValues = new Object[columnsToUpdate.size()];
        singleRowValues[0] = "T";
        if (StringUtils.isNotBlank(sqlProvisioningConfiguration.getSqlLastModifiedColumnName())) {
          if (StringUtils.equals("long", sqlProvisioningConfiguration.getSqlLastModifiedColumnType())) {
            singleRowValues[1] = lastModified(); 
          } else if (StringUtils.equals("timestamp", sqlProvisioningConfiguration.getSqlLastModifiedColumnType())) {
            singleRowValues[1] = new Timestamp(lastModified());
          } else {
            throw new RuntimeException("Invalid sqlLastModifiedColumnType: '"+sqlProvisioningConfiguration.getSqlLastModifiedColumnType()+"'");
          }
          
        }
        
        valuesToUpdate.add(singleRowValues);
      }
      
      SqlProvisionerCommands.updateObjects(dbExternalSystemConfigId, objectTableName, columnsToUpdate, valuesToUpdate, GrouperUtil.toList(groupIdColumn, entityIdColumn), ownerIds);
      
    }
    
    for (ProvisioningMembership targetMembership: targetMemberships) {
      
      targetMembership.setProvisioned(true);
      for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetMembership.getInternal_objectChanges())) {
        provisioningObjectChange.setProvisioned(true);
      }

    }

    return new TargetDaoDeleteMembershipsResponse();

  }
  
  /**
   * 
   */
  @Override
  public TargetDaoInsertMembershipsResponse insertMemberships(TargetDaoInsertMembershipsRequest targetDaoInsertMembershipsRequest) {
    
    List<ProvisioningMembership> targetMemberships = targetDaoInsertMembershipsRequest.getTargetMemberships();
    
    SqlProvisioningConfiguration sqlProvisioningConfiguration = (SqlProvisioningConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
    
    String dbExternalSystemConfigId = sqlProvisioningConfiguration.getDbExternalSystemConfigId();
    
    String objectTableName = sqlProvisioningConfiguration.getMembershipTableName();
    
    String sqlLastModifiedColumn = sqlProvisioningConfiguration.getSqlLastModifiedColumnName();
    String sqlLastModifiedColumnType = sqlProvisioningConfiguration.getSqlLastModifiedColumnType();

    String sqlDeletedColumn = sqlProvisioningConfiguration.getSqlDeletedColumnName();
    
    
    Map<String, GrouperProvisioningConfigurationAttribute> attributeNameToConfig = sqlProvisioningConfiguration.getTargetMembershipAttributeNameToConfig();
    
    List<String> columnsToInsertInPrimaryTable = new ArrayList<String>();
    Map<String, Integer> columnsToInsertInPrimaryTableToIndex = new HashMap<String, Integer>();
    
    for (String attributeName: attributeNameToConfig.keySet()) {
      SqlGrouperProvisioningConfigurationAttribute configurationAttribute = (SqlGrouperProvisioningConfigurationAttribute) attributeNameToConfig.get(attributeName);
      GrouperUtil.assertion(configurationAttribute!=null, "Configuration attribute is null: '" + attributeName + "'");
      if (configurationAttribute.isInsert()) {
        columnsToInsertInPrimaryTableToIndex.put(attributeName, GrouperUtil.length(columnsToInsertInPrimaryTable));
        columnsToInsertInPrimaryTable.add(attributeName);
      }
    }
    
    List<Object[]> attributeValuesPrimaryTable = new ArrayList<Object[]>();
    
    for (ProvisioningMembership targetMembership: targetMemberships) {
      
      Object[] attributeValuePrimaryTable = new Object[columnsToInsertInPrimaryTable.size()];
      attributeValuesPrimaryTable.add(attributeValuePrimaryTable);
      for (ProvisioningObjectChange provisioningObjectChange: GrouperUtil.nonNull(targetMembership.getInternal_objectChanges())) {
        if (provisioningObjectChange.getProvisioningObjectChangeAction() == ProvisioningObjectChangeAction.insert) {
          String attributeName = provisioningObjectChange.getAttributeName();
          
          Integer columnIndex = columnsToInsertInPrimaryTableToIndex.get(attributeName);
          GrouperUtil.assertion(columnIndex!=null, "Cant find column: " + attributeName);

          attributeValuePrimaryTable[columnIndex] = provisioningObjectChange.getNewValue();
        }
      }
      
      if (StringUtils.isNotBlank(sqlLastModifiedColumn)) {
        Integer columnIndex = columnsToInsertInPrimaryTableToIndex.get(sqlLastModifiedColumn);
        if (columnIndex == null) {
          throw new RuntimeException("You must have an attribute in the memberships table called: "+sqlLastModifiedColumn + " because"
              + " you set the configuration last modified sql column name so it must exist on all provisioned tables");
        }
        if (StringUtils.equals("long", sqlLastModifiedColumnType)) {
          attributeValuePrimaryTable[columnIndex] = lastModified(); 
        } else if (StringUtils.equals("timestamp", sqlLastModifiedColumnType)) {
          attributeValuePrimaryTable[columnIndex] = new Timestamp(lastModified());
        } else {
          throw new RuntimeException("Invalid sqlLastModifiedColumnType: '"+sqlLastModifiedColumnType+"'");
        } 
      }
      
      if (StringUtils.isNotBlank(sqlDeletedColumn)) {
        Integer columnIndex = columnsToInsertInPrimaryTableToIndex.get(sqlDeletedColumn);
        if (columnIndex == null) {
          throw new RuntimeException("You must have an attribute in the memberships table called: "+sqlDeletedColumn + " because"
              + " you set the configuration deleted sql column name so it must exist on all provisioned tables");
        }
        attributeValuePrimaryTable[columnIndex] = "F";
      }
      
    }

    if (StringUtils.isNotBlank(sqlDeletedColumn)) {
      
      String groupIdColumn = sqlProvisioningConfiguration.getMembershipGroupMatchingIdAttribute();
      String entityIdColumn = sqlProvisioningConfiguration.getMembershipEntityMatchingIdAttribute();

      List<Object[]> ownerIds = new ArrayList<Object[]>();
      
      for (ProvisioningMembership targetMembership: targetMemberships) {
        MultiKey membershipMatchingId = new MultiKey(targetMembership.retrieveAttributeValue(groupIdColumn), targetMembership.retrieveAttributeValue(entityIdColumn));
        ownerIds.add(membershipMatchingId.getKeys());
      }
      
      SqlProvisionerCommands.deleteObjects(ownerIds, dbExternalSystemConfigId, objectTableName, 
          GrouperUtil.toList(groupIdColumn, entityIdColumn), null, null, sqlDeletedColumn, true, true);
    }
    
    
    SqlProvisionerCommands.insertObjects(dbExternalSystemConfigId, objectTableName, columnsToInsertInPrimaryTable, attributeValuesPrimaryTable);
    
    for (ProvisioningMembership targetMembership : targetMemberships) {
      targetMembership.setProvisioned(true);
      for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetMembership.getInternal_objectChanges())) {
        provisioningObjectChange.setProvisioned(true);
      }

    }
    
    return new TargetDaoInsertMembershipsResponse();
    
  }
  
  /**
   * 
   */
  @Override
  public TargetDaoInsertGroupsResponse insertGroups(TargetDaoInsertGroupsRequest targetDaoInsertGroupsRequest) {
    
    List<ProvisioningGroup> targetGroups = targetDaoInsertGroupsRequest.getTargetGroups();
    
    SqlProvisioningConfiguration sqlProvisioningConfiguration = (SqlProvisioningConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
    
    String dbExternalSystemConfigId = sqlProvisioningConfiguration.getDbExternalSystemConfigId();
    
    String objectTableName = sqlProvisioningConfiguration.getGroupTableName();
    String attributeTableName = sqlProvisioningConfiguration.getGroupAttributesTableName();

    String objectIdColumnName = sqlProvisioningConfiguration.getGroupTableIdColumn();
    
    String attributeForeignKeyColumn = sqlProvisioningConfiguration.getGroupAttributesGroupForeignKeyColumn();
    
    String attributesAttributeNameColumn = sqlProvisioningConfiguration.getGroupAttributesAttributeNameColumn();

    String attributesAttributeValueColumn = sqlProvisioningConfiguration.getGroupAttributesAttributeValueColumn();

    String sqlLastModifiedColumn = sqlProvisioningConfiguration.getSqlLastModifiedColumnName();
    String sqlLastModifiedColumnType = sqlProvisioningConfiguration.getSqlLastModifiedColumnType();

    String sqlDeletedColumn = sqlProvisioningConfiguration.getSqlDeletedColumnName();

    Map<String, GrouperProvisioningConfigurationAttribute> attributeNameToConfig = sqlProvisioningConfiguration.getTargetGroupAttributeNameToConfig();
    
    List<String> columnsToInsertInPrimaryTable = new ArrayList<String>();
    Map<String, Integer> columnsToInsertInPrimaryTableToIndex = new HashMap<String, Integer>();
    
    for (String attributeName: attributeNameToConfig.keySet()) {
      SqlGrouperProvisioningConfigurationAttribute configurationAttribute = (SqlGrouperProvisioningConfigurationAttribute) attributeNameToConfig.get(attributeName);
      GrouperUtil.assertion(configurationAttribute!=null, "Configuration attribute is null: '" + attributeName + "'");
      if (configurationAttribute.isInsert()) {
        if ( StringUtils.isBlank(configurationAttribute.getStorageType()) || StringUtils.equals(configurationAttribute.getStorageType(), "groupTableColumn")) {
          columnsToInsertInPrimaryTableToIndex.put(attributeName, GrouperUtil.length(columnsToInsertInPrimaryTable));
          columnsToInsertInPrimaryTable.add(attributeName);
        }
      }
    }

    List<String> columnsToInsertInAttributeTable = GrouperUtil.toList(attributeForeignKeyColumn, attributesAttributeNameColumn, attributesAttributeValueColumn);
    if (!StringUtils.isBlank(sqlLastModifiedColumn)) {
      columnsToInsertInAttributeTable.add(sqlLastModifiedColumn);
    }
    if (!StringUtils.isBlank(sqlDeletedColumn)) {
      columnsToInsertInAttributeTable.add(sqlDeletedColumn);
    }
    
    List<Object[]> attributeValuesAttributeTable = new ArrayList<Object[]>();
    List<Object[]> attributeValuesPrimaryTable = new ArrayList<Object[]>();
    for (ProvisioningGroup targetGroup: targetGroups) {
      
      Object[] attributeValuePrimaryTable = new Object[columnsToInsertInPrimaryTable.size()];
      attributeValuesPrimaryTable.add(attributeValuePrimaryTable);
          
      for (ProvisioningObjectChange provisioningObjectChange: GrouperUtil.nonNull(targetGroup.getInternal_objectChanges())) {
        if (provisioningObjectChange.getProvisioningObjectChangeAction() == ProvisioningObjectChangeAction.insert) {
          String attributeName = provisioningObjectChange.getAttributeName();
          
          SqlGrouperProvisioningConfigurationAttribute configurationAttribute = (SqlGrouperProvisioningConfigurationAttribute) attributeNameToConfig.get(attributeName);
          GrouperUtil.assertion(configurationAttribute!=null, "Configuration attribute is null: '" + attributeName + "'");
          if (StringUtils.equals(configurationAttribute.getStorageType(), "separateAttributesTable")) {
            GrouperUtil.assertion(!StringUtils.isBlank(attributeTableName), "Attribute table name is blank!");
            if (GrouperUtil.isEmpty(provisioningObjectChange.getNewValue())) {
              continue;
            }
            Object[] groupIdAttributeNameAttributeValue = new Object[columnsToInsertInAttributeTable.size()];
            groupIdAttributeNameAttributeValue[0] = targetGroup.retrieveAttributeValue(objectIdColumnName);
            groupIdAttributeNameAttributeValue[1] = attributeName;
            groupIdAttributeNameAttributeValue[2] = provisioningObjectChange.getNewValue();
            if (StringUtils.isNotBlank(sqlLastModifiedColumn)) {
              if (StringUtils.equals(sqlLastModifiedColumnType, "timestamp")) {
                groupIdAttributeNameAttributeValue[3] = new Timestamp(lastModified());
              } else if (StringUtils.equals(sqlLastModifiedColumnType, "long")) {
                groupIdAttributeNameAttributeValue[3] = lastModified();
              } else {
                throw new RuntimeException("Invalid sqlLastModifiedColumnType: '"+sqlLastModifiedColumnType+"'");
              }
            }
            
            if (StringUtils.isNotBlank(sqlDeletedColumn)) {
              int columnIndex = StringUtils.isNotBlank(sqlLastModifiedColumn) ? 4: 3;
              groupIdAttributeNameAttributeValue[columnIndex] = "F";
            }

            attributeValuesAttributeTable.add(groupIdAttributeNameAttributeValue);
          } else {
            Integer columnIndex = columnsToInsertInPrimaryTableToIndex.get(attributeName);
            GrouperUtil.assertion(columnIndex!=null, "Cant find column: " + attributeName);
            attributeValuePrimaryTable[columnIndex] = provisioningObjectChange.getNewValue();
          }
        }
      }
      if (StringUtils.isNotBlank(sqlLastModifiedColumn)) {
        Integer columnIndex = columnsToInsertInPrimaryTableToIndex.get(sqlLastModifiedColumn);
        if (columnIndex == null) {
          throw new RuntimeException("You must have an attribute in the groups table called: "+sqlLastModifiedColumn + " because"
              + " you set the configuration last modified sql column name so it must exist on all provisioned tables");
        }
        if (StringUtils.equals("long", sqlLastModifiedColumnType)) {
          attributeValuePrimaryTable[columnIndex] = lastModified(); 
        } else if (StringUtils.equals("timestamp", sqlLastModifiedColumnType)) {
          attributeValuePrimaryTable[columnIndex] = new Timestamp(lastModified());
        } else {
          throw new RuntimeException("Invalid sqlLastModifiedColumnType: '"+sqlLastModifiedColumnType+"'");
        } 
      }
      
      if (StringUtils.isNotBlank(sqlDeletedColumn)) {
        Integer columnIndex = columnsToInsertInPrimaryTableToIndex.get(sqlDeletedColumn);
        if (columnIndex == null) {
          throw new RuntimeException("You must have an attribute in the groups table called: "+sqlDeletedColumn + " because"
              + " you set the configuration deleted sql column name so it must exist on all provisioned tables");
        }
        attributeValuePrimaryTable[columnIndex] = "F";
      }
    }

    if (StringUtils.isNotBlank(sqlDeletedColumn)) {
      String objectTableIdColumn = sqlProvisioningConfiguration.getGroupTableIdColumn();
      String objectAttributesTableName = sqlProvisioningConfiguration.getGroupAttributesTableName();
      String objectAttributesGroupForeignKeyColumn = sqlProvisioningConfiguration.getGroupAttributesGroupForeignKeyColumn();

      List<Object[]> ownerIds = new ArrayList<Object[]>();
      
      for (ProvisioningGroup targetGroup: targetGroups) {
        Object groupIdValue = targetGroup.retrieveAttributeValue(objectTableIdColumn);
        ownerIds.add(new Object[] {groupIdValue});
      }
      SqlProvisionerCommands.deleteObjects(ownerIds, dbExternalSystemConfigId, objectTableName, GrouperUtil.toList(objectTableIdColumn), 
          objectAttributesTableName, objectAttributesGroupForeignKeyColumn, sqlDeletedColumn, true, true);
    }
    
    // TODO put in transaction (along with other operations)
    SqlProvisionerCommands.insertObjects(dbExternalSystemConfigId, objectTableName, columnsToInsertInPrimaryTable, attributeValuesPrimaryTable);
    
    SqlProvisionerCommands.insertObjects(dbExternalSystemConfigId, attributeTableName, columnsToInsertInAttributeTable, attributeValuesAttributeTable);
 
    for (ProvisioningGroup targetGroup : targetGroups) {
      targetGroup.setProvisioned(true);
      for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetGroup.getInternal_objectChanges())) {
        provisioningObjectChange.setProvisioned(true);
      }
      
    }
    
    return new TargetDaoInsertGroupsResponse();
    
  }
  
  private static long lastModifiedUsed = -1;
  
  /**
   * to avoid race conditions, we don't want the same last modified date appearing twice. 
   * @return 
   */
  private static synchronized long lastModified() {
    long lastModified = System.currentTimeMillis();
    if (lastModified <= lastModifiedUsed) {
      lastModified = lastModifiedUsed + 1 ;
    }
    lastModifiedUsed = lastModified;
    return lastModified;
  }
  
  /**
   * 
   */
  @Override
  public TargetDaoInsertEntitiesResponse insertEntities(TargetDaoInsertEntitiesRequest targetDaoInsertEntitiesRequest) {
    
    List<ProvisioningEntity> targetEntities = targetDaoInsertEntitiesRequest.getTargetEntityInserts();
    
    SqlProvisioningConfiguration sqlProvisioningConfiguration = (SqlProvisioningConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
    
    String dbExternalSystemConfigId = sqlProvisioningConfiguration.getDbExternalSystemConfigId();
    
    String objectTableName = sqlProvisioningConfiguration.getEntityTableName();
    String attributeTableName = sqlProvisioningConfiguration.getEntityAttributesTableName();

    String objectIdColumnName = sqlProvisioningConfiguration.getEntityTableIdColumn();
    
    String attributeForeignKeyColumn = sqlProvisioningConfiguration.getEntityAttributesEntityForeignKeyColumn();
    
    String attributesAttributeNameColumn = sqlProvisioningConfiguration.getEntityAttributesAttributeNameColumn();

    String attributesAttributeValueColumn = sqlProvisioningConfiguration.getEntityAttributesAttributeValueColumn();

    String sqlLastModifiedColumn = sqlProvisioningConfiguration.getSqlLastModifiedColumnName();
    String sqlDeletedColumn = sqlProvisioningConfiguration.getSqlDeletedColumnName();
    String sqlLastModifiedColumnType = sqlProvisioningConfiguration.getSqlLastModifiedColumnType();

    Map<String, GrouperProvisioningConfigurationAttribute> attributeNameToConfig = sqlProvisioningConfiguration.getTargetEntityAttributeNameToConfig();
    
    List<String> columnsToInsertInPrimaryTable = new ArrayList<String>();
    Map<String, Integer> columnsToInsertInPrimaryTableToIndex = new HashMap<String, Integer>();
    
    for (String attributeName: attributeNameToConfig.keySet()) {
      SqlGrouperProvisioningConfigurationAttribute configurationAttribute = (SqlGrouperProvisioningConfigurationAttribute) attributeNameToConfig.get(attributeName);
      GrouperUtil.assertion(configurationAttribute!=null, "Configuration attribute is null: '" + attributeName + "'");
      if (configurationAttribute.isInsert()) {
        if ( StringUtils.isBlank(configurationAttribute.getStorageType()) || StringUtils.equals(configurationAttribute.getStorageType(), "entityTableColumn")) {
          columnsToInsertInPrimaryTableToIndex.put(attributeName, GrouperUtil.length(columnsToInsertInPrimaryTable));
          columnsToInsertInPrimaryTable.add(attributeName);
        }
      }
    }
    
    List<String> columnsToInsertInAttributeTable = GrouperUtil.toList(attributeForeignKeyColumn, attributesAttributeNameColumn, attributesAttributeValueColumn);
    if (!StringUtils.isBlank(sqlLastModifiedColumn)) {
      columnsToInsertInAttributeTable.add(sqlLastModifiedColumn);
    }
    if (!StringUtils.isBlank(sqlDeletedColumn)) {
      columnsToInsertInAttributeTable.add(sqlDeletedColumn);
    }
    
    List<Object[]> attributeValuesAttributeTable = new ArrayList<Object[]>();
    List<Object[]> attributeValuesPrimaryTable = new ArrayList<Object[]>();
    
    for (ProvisioningEntity targetEntity: targetEntities) {
      
      Object[] attributeValuePrimaryTable = new Object[columnsToInsertInPrimaryTable.size()];
      attributeValuesPrimaryTable.add(attributeValuePrimaryTable);
          
      for (ProvisioningObjectChange provisioningObjectChange: GrouperUtil.nonNull(targetEntity.getInternal_objectChanges())) {
        if (provisioningObjectChange.getProvisioningObjectChangeAction() == ProvisioningObjectChangeAction.insert) {
          String attributeName = provisioningObjectChange.getAttributeName();
          
          SqlGrouperProvisioningConfigurationAttribute configurationAttribute = (SqlGrouperProvisioningConfigurationAttribute) attributeNameToConfig.get(attributeName);
          GrouperUtil.assertion(configurationAttribute!=null, "Configuration attribute is null: '" + attributeName + "'");
          if (StringUtils.equals(configurationAttribute.getStorageType(), "separateAttributesTable")) {
            
            if (GrouperUtil.isEmpty(provisioningObjectChange.getNewValue())) {
              continue;
            }
            Object[] entityIdAttributeNameAttributeValue = new Object[columnsToInsertInAttributeTable.size()];
            entityIdAttributeNameAttributeValue[0] = targetEntity.retrieveAttributeValue(objectIdColumnName);
            entityIdAttributeNameAttributeValue[1] = attributeName;
            entityIdAttributeNameAttributeValue[2] = provisioningObjectChange.getNewValue();
            if (StringUtils.isNotBlank(sqlLastModifiedColumn)) {
              if (StringUtils.equals(sqlLastModifiedColumnType, "timestamp")) {
                entityIdAttributeNameAttributeValue[3] = new Timestamp(lastModified());
              } else if (StringUtils.equals(sqlLastModifiedColumnType, "long")) {
                entityIdAttributeNameAttributeValue[3] = lastModified();
              } else {
                throw new RuntimeException("Invalid sqlLastModifiedColumnType: '"+sqlLastModifiedColumnType+"'");
              }
            }
            
            if (StringUtils.isNotBlank(sqlDeletedColumn)) {
              int columnIndex = StringUtils.isNotBlank(sqlLastModifiedColumn) ? 4: 3;
              entityIdAttributeNameAttributeValue[columnIndex] = "F";
            }

            attributeValuesAttributeTable.add(entityIdAttributeNameAttributeValue);
          } else {
            Integer columnIndex = columnsToInsertInPrimaryTableToIndex.get(attributeName);
            GrouperUtil.assertion(columnIndex!=null, "Cant find column: " + attributeName);
            attributeValuePrimaryTable[columnIndex] = provisioningObjectChange.getNewValue();
          }
        }
      }
      if (StringUtils.isNotBlank(sqlLastModifiedColumn)) {
        Integer columnIndex = columnsToInsertInPrimaryTableToIndex.get(sqlLastModifiedColumn);
        if (columnIndex == null) {
          throw new RuntimeException("You must have an attribute in the entities table called: "+sqlLastModifiedColumn + " because"
              + " you set the configuration last modified sql column name so it must exist on all provisioned tables");
        }
        if (StringUtils.equals("long", sqlLastModifiedColumnType)) {
          attributeValuePrimaryTable[columnIndex] = lastModified(); 
        } else if (StringUtils.equals("timestamp", sqlLastModifiedColumnType)) {
          attributeValuePrimaryTable[columnIndex] = new Timestamp(lastModified());
        } else {
          throw new RuntimeException("Invalid sqlLastModifiedColumnType: '"+sqlLastModifiedColumnType+"'");
        } 
      }
      
      if (StringUtils.isNotBlank(sqlDeletedColumn)) {
        Integer columnIndex = columnsToInsertInPrimaryTableToIndex.get(sqlDeletedColumn);
        if (columnIndex == null) {
          throw new RuntimeException("You must have an attribute in the entities table called: "+sqlDeletedColumn + " because"
              + " you set the configuration deleted sql column name so it must exist on all provisioned tables");
        }
        attributeValuePrimaryTable[columnIndex] = "F";
      }
    }

    if (StringUtils.isNotBlank(sqlDeletedColumn)) {
      String objectTableIdColumn = sqlProvisioningConfiguration.getEntityTableIdColumn();
      String objectAttributesTableName = sqlProvisioningConfiguration.getEntityAttributesTableName();
      String objectAttributesEntityForeignKeyColumn = sqlProvisioningConfiguration.getEntityAttributesEntityForeignKeyColumn();

      List<Object[]> ownerIds = new ArrayList<Object[]>();
      
      for (ProvisioningEntity targetEntity: targetEntities) {
        Object entityIdValue = targetEntity.retrieveAttributeValue(objectTableIdColumn);
        ownerIds.add(new Object[] {entityIdValue});
      }
      SqlProvisionerCommands.deleteObjects(ownerIds, dbExternalSystemConfigId, objectTableName, GrouperUtil.toList(objectTableIdColumn), 
          objectAttributesTableName, objectAttributesEntityForeignKeyColumn, sqlDeletedColumn, true, true);
    }
    
    // TODO put in transaction (along with other operations)
    SqlProvisionerCommands.insertObjects(dbExternalSystemConfigId, objectTableName, columnsToInsertInPrimaryTable, attributeValuesPrimaryTable);
    
    SqlProvisionerCommands.insertObjects(dbExternalSystemConfigId, attributeTableName, columnsToInsertInAttributeTable, attributeValuesAttributeTable);
 

    for (ProvisioningEntity targetEntity : targetEntities) {
      
      targetEntity.setProvisioned(true);
      for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetEntity.getInternal_objectChanges())) {
        provisioningObjectChange.setProvisioned(true);
      }
    }
    
    return new TargetDaoInsertEntitiesResponse();
    
  }
  
  
  
  /**
   * 
   */
  @Override
  public TargetDaoRetrieveAllGroupsResponse retrieveAllGroups(TargetDaoRetrieveAllGroupsRequest targetDaoRetrieveAllGroupsRequest) {
    
    boolean includeMemberships = targetDaoRetrieveAllGroupsRequest.isIncludeAllMembershipsIfApplicable();

    GrouperProvisioningBehaviorMembershipType membershipType = this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().getGrouperProvisioningBehaviorMembershipType();
    
    boolean isGroupAttributes = membershipType == GrouperProvisioningBehaviorMembershipType.groupAttributes;
    
    SqlProvisioningConfiguration sqlProvisioningConfiguration = (SqlProvisioningConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
    
    String dbExternalSystemConfigId = sqlProvisioningConfiguration.getDbExternalSystemConfigId();
    
    String groupTableName = sqlProvisioningConfiguration.getGroupTableName();
    
    String groupTableIdColumn = sqlProvisioningConfiguration.getGroupTableIdColumn();
    int groupTableIdColumnIndex = -1;
    
    String groupAttributesTableName  = sqlProvisioningConfiguration.getGroupAttributesTableName();

    String groupAttributesEntityForeignKeyColumn = sqlProvisioningConfiguration.getGroupAttributesGroupForeignKeyColumn();
    
    String groupAttributesAttributeNameColumn = sqlProvisioningConfiguration.getGroupAttributesAttributeNameColumn();

    String groupAttributesAttributeValueColumn = sqlProvisioningConfiguration.getGroupAttributesAttributeValueColumn();
    
    List<ProvisioningGroup> result = new ArrayList<ProvisioningGroup>();
    
    List<String> groupTablePrimaryColNamesList = new ArrayList<String>();
    List<String> attributeTableAttributesNamesList = new ArrayList<String>();
    
    List<String> groupTableAttributesColNamesList = null;

    Map<String, GrouperProvisioningConfigurationAttribute> groupAttributeNameToConfigAttribute = sqlProvisioningConfiguration.getTargetGroupAttributeNameToConfig();
    
    for (String attributeName: groupAttributeNameToConfigAttribute.keySet()) {
      SqlGrouperProvisioningConfigurationAttribute configurationAttribute = (SqlGrouperProvisioningConfigurationAttribute) groupAttributeNameToConfigAttribute.get(attributeName);
      GrouperUtil.assertion(configurationAttribute!=null, "Configuration attribute is null: '" + attributeName + "'");
      if (!configurationAttribute.isSelect()) {
        continue;
      }

      boolean isMembershipAttribute = false;
      if (isGroupAttributes && !StringUtils.isBlank(sqlProvisioningConfiguration.getGroupMembershipAttributeName()) && StringUtils.equals(sqlProvisioningConfiguration.getGroupMembershipAttributeName(), attributeName)) {
        isMembershipAttribute = true;
      }
      
      // maybe we dont want memberships
      if (StringUtils.equals(configurationAttribute.getStorageType(), "separateAttributesTable")) {
        
        if (!isMembershipAttribute || (isGroupAttributes && includeMemberships)) {

          attributeTableAttributesNamesList.add(attributeName);

        }
        
      } else {
        if (StringUtils.equals(groupTableIdColumn,  attributeName)) {
          groupTableIdColumnIndex = GrouperUtil.length(groupTablePrimaryColNamesList);
        }
        groupTablePrimaryColNamesList.add(attributeName);
      }
    }
        
    if (attributeTableAttributesNamesList.size() > 0 ) {
      GrouperUtil.assertion(!StringUtils.isBlank(groupAttributesEntityForeignKeyColumn), "entity attributes foreign key column must be configured");
      GrouperUtil.assertion(!StringUtils.isBlank(groupAttributesAttributeNameColumn), "entity attributes attribute name column must be configured");
      GrouperUtil.assertion(!StringUtils.isBlank(groupAttributesAttributeValueColumn), "entity attributes attribute value column must be configured");
      groupTableAttributesColNamesList = GrouperUtil.toList(groupAttributesEntityForeignKeyColumn, groupAttributesAttributeNameColumn, groupAttributesAttributeValueColumn);
    }
     
     
    List<Object[]> groupPrimaryAttributeValues = null;

    List<Object[]> attributeValuesSeparateTable = null;
    
    groupPrimaryAttributeValues = SqlProvisionerCommands.retrieveObjectsNoFilter(dbExternalSystemConfigId, groupTablePrimaryColNamesList, groupTableName);
        
    // look up attributes if we should and if we got results
    if (attributeTableAttributesNamesList.size() > 0 && GrouperUtil.length(groupPrimaryAttributeValues) > 0 ) {
      
      List<Object> mainTableIdsFound = new ArrayList<Object>();
      for (Object[] groupPrimaryAttributeValue : groupPrimaryAttributeValues) {
        Object mainTableId = groupPrimaryAttributeValue[groupTableIdColumnIndex];
        GrouperUtil.assertion(!GrouperUtil.isBlank(mainTableId), "Why is main table ID blank?");
        mainTableIdsFound.add(mainTableId);
      }
      
      attributeValuesSeparateTable = SqlProvisionerCommands.retrieveObjectsColumnFilter(dbExternalSystemConfigId, groupTableAttributesColNamesList, 
          groupAttributesTableName, GrouperUtil.toList(groupAttributesAttributeNameColumn),  (List<Object>)(Object)attributeTableAttributesNamesList, 
          GrouperUtil.toList(groupAttributesEntityForeignKeyColumn), mainTableIdsFound, sqlProvisioningConfiguration.getSqlDeletedColumnName(), false);
          
    }
     
    retrieveGroupsAddRecord(result, 
        groupPrimaryAttributeValues, attributeValuesSeparateTable, 
        groupTablePrimaryColNamesList, attributeTableAttributesNamesList,
        groupTableIdColumn, groupAttributeNameToConfigAttribute);
            
    return new TargetDaoRetrieveAllGroupsResponse(result);
    
  }

  /**
   * 
   * @param result
   * @param groupPrimaryAttributeValues
   * @param attributeValuesSeparateTable
   * @param groupTablePrimaryColNamesList
   * @param attributeTableAttributesNamesList
   * @param groupTableIdColumn
   * @param groupAttributeNameToConfigAttribute
   */
  protected void retrieveGroupsAddRecord(List<ProvisioningGroup> result, 
      List<Object[]> groupPrimaryAttributeValues, List<Object[]> attributeValuesSeparateTable, 
      List<String> groupTablePrimaryColNamesList, List<String> attributeTableAttributesNamesList,
      String groupTableIdColumn, Map<String, GrouperProvisioningConfigurationAttribute> groupAttributeNameToConfigAttribute) {
    
    Map<Object, List<Object[]>> groupIdentifierToAttributes = new HashMap<Object, List<Object[]>>();
    
    if (GrouperUtil.length(attributeTableAttributesNamesList) > 0) {
      
      for (Object[] obj: GrouperUtil.nonNull(attributeValuesSeparateTable)) {
        Object groupIdentifier = obj[0];
        
        List<Object[]> list = groupIdentifierToAttributes.get(groupIdentifier);

        if (list == null) {
          list = new ArrayList<Object[]>();
          groupIdentifierToAttributes.put(groupIdentifier, list);
        }
        list.add(obj);
        
      }
    }
    
    for (Object[] groupAttributeValue: GrouperUtil.nonNull(groupPrimaryAttributeValues)) {
      ProvisioningGroup provisioningGroup = new ProvisioningGroup();
 
      for (int i=0; i<groupTablePrimaryColNamesList.size(); i++) {

        String colName = groupTablePrimaryColNamesList.get(i);
        
        Object value = groupAttributeValue[i];
        
        GrouperProvisioningConfigurationAttribute configurationAttribute = groupAttributeNameToConfigAttribute.get(colName);

        if (configurationAttribute.isMultiValued()) {
          throw new RuntimeException("An attribute that's a column of the primary table to be provisioned cannot be multivalued. "+colName);
        }

        if (StringUtils.equalsIgnoreCase(groupTableIdColumn, colName) && GrouperUtil.length(attributeTableAttributesNamesList) > 0) {
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


  /**
   * 
   * @param result
   * @param entityPrimaryAttributeValues
   * @param attributeValuesSeparateTable
   * @param entityTablePrimaryColNamesList
   * @param attributeTableAttributesNamesList
   * @param entityTableIdColumn
   * @param entityAttributeNameToConfigAttribute
   */
  protected void retrieveEntitiesAddRecord(List<ProvisioningEntity> result, 
      List<Object[]> entityPrimaryAttributeValues, List<Object[]> attributeValuesSeparateTable, 
      List<String> entityTablePrimaryColNamesList, List<String> attributeTableAttributesNamesList,
      String entityTableIdColumn, Map<String, GrouperProvisioningConfigurationAttribute> entityAttributeNameToConfigAttribute) {
    
    Map<Object, List<Object[]>> entityIdentifierToAttributes = new HashMap<Object, List<Object[]>>();
    
    if (GrouperUtil.length(attributeTableAttributesNamesList) > 0) {
      
      for (Object[] obj: GrouperUtil.nonNull(attributeValuesSeparateTable)) {
        Object entityIdentifier = obj[0];
        
        List<Object[]> list = entityIdentifierToAttributes.get(entityIdentifier);

        if (list == null) {
          list = new ArrayList<Object[]>();
          entityIdentifierToAttributes.put(entityIdentifier, list);
        }
        list.add(obj);
        
      }
    }
    
    for (Object[] entityAttributeValue: GrouperUtil.nonNull(entityPrimaryAttributeValues)) {
      ProvisioningEntity provisioningEntity = new ProvisioningEntity();
 
      for (int i=0; i<entityTablePrimaryColNamesList.size(); i++) {

        String colName = entityTablePrimaryColNamesList.get(i);
        
        Object value = entityAttributeValue[i];
        
        GrouperProvisioningConfigurationAttribute configurationAttribute = entityAttributeNameToConfigAttribute.get(colName);

        if (configurationAttribute.isMultiValued()) {
          throw new RuntimeException("An attribute that's a column of the primary table to be provisioned cannot be multivalued. "+colName);
        }

        if (StringUtils.equalsIgnoreCase(entityTableIdColumn, colName) && GrouperUtil.length(attributeTableAttributesNamesList) > 0) {
          List<Object[]> moreAttributesFromSeparateAttributesTable = entityIdentifierToAttributes.get(value);
          
          for (Object[] namesValues: GrouperUtil.nonNull(moreAttributesFromSeparateAttributesTable)) {
            
            String attributeName = namesValues[1].toString();
            Object attributeValue = namesValues[2];
            
            GrouperProvisioningConfigurationAttribute configurationAttributeForSeparateTable = this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration()
                .getTargetEntityAttributeNameToConfig().get(attributeName);
            
            if(configurationAttributeForSeparateTable.isMultiValued()) {
              provisioningEntity.addAttributeValue(attributeName, attributeValue);
            } else {
              provisioningEntity.assignAttributeValue(attributeName, attributeValue);
            }
            
          }
          
        }
 
        provisioningEntity.assignAttributeValue(colName, value);
                
      }
            
      result.add(provisioningEntity);
    }
    
  }

  /**
   * 
   */
  @Override
  public TargetDaoRetrieveGroupsResponse retrieveGroups(TargetDaoRetrieveGroupsRequest targetDaoRetrieveGroupsRequest) {
    
    boolean includeMemberships = targetDaoRetrieveGroupsRequest.isIncludeAllMembershipsIfApplicable();

    GrouperProvisioningBehaviorMembershipType membershipType = this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().getGrouperProvisioningBehaviorMembershipType();
    
    boolean isGroupAttributes = membershipType == GrouperProvisioningBehaviorMembershipType.groupAttributes;
    
    SqlProvisioningConfiguration sqlProvisioningConfiguration = (SqlProvisioningConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
    
    String dbExternalSystemConfigId = sqlProvisioningConfiguration.getDbExternalSystemConfigId();
    
    String groupTableName = sqlProvisioningConfiguration.getGroupTableName();
    
    String groupTableIdColumn = sqlProvisioningConfiguration.getGroupTableIdColumn();
    int groupTableIdColumnIndex = -1;
    String groupAttributesTableName  = sqlProvisioningConfiguration.getGroupAttributesTableName();

    String groupAttributesGroupForeignKeyColumn = sqlProvisioningConfiguration.getGroupAttributesGroupForeignKeyColumn();
    
    String groupAttributesAttributeNameColumn = sqlProvisioningConfiguration.getGroupAttributesAttributeNameColumn();

    String groupAttributesAttributeValueColumn = sqlProvisioningConfiguration.getGroupAttributesAttributeValueColumn();
    
    List<ProvisioningGroup> result = new ArrayList<ProvisioningGroup>();
    
    List<String> groupTablePrimaryColNamesList = new ArrayList<String>();
    List<String> attributeTableAttributesNamesList = new ArrayList<String>();
    
    List<String> groupTableAttributesColNamesList = null;

    Map<String, GrouperProvisioningConfigurationAttribute> groupAttributeNameToConfigAttribute = sqlProvisioningConfiguration.getTargetGroupAttributeNameToConfig();

    for (String attributeName: groupAttributeNameToConfigAttribute.keySet()) {
      SqlGrouperProvisioningConfigurationAttribute configurationAttribute = (SqlGrouperProvisioningConfigurationAttribute) groupAttributeNameToConfigAttribute.get(attributeName);
      GrouperUtil.assertion(configurationAttribute!=null, "Configuration attribute is null: '" + attributeName + "'");
      if (!configurationAttribute.isSelect() && !StringUtils.equals(targetDaoRetrieveGroupsRequest.getSearchAttribute(), attributeName)) {
        continue;
      }
      
      boolean isMembershipAttribute = false;
      if (isGroupAttributes && !StringUtils.isBlank(sqlProvisioningConfiguration.getGroupMembershipAttributeName()) && StringUtils.equals(sqlProvisioningConfiguration.getGroupMembershipAttributeName(), attributeName)) {
        isMembershipAttribute = true;
      }

      // maybe we dont want memberships
      if (StringUtils.equals(configurationAttribute.getStorageType(), "separateAttributesTable")) {
        
        if (!isMembershipAttribute || (isGroupAttributes && includeMemberships)) {

          attributeTableAttributesNamesList.add(attributeName);

        }
        
      } else {
        if (StringUtils.equals(groupTableIdColumn,  attributeName)) {
          groupTableIdColumnIndex = GrouperUtil.length(groupTablePrimaryColNamesList);
        }
        groupTablePrimaryColNamesList.add(attributeName);
      }
    }

    boolean filterByColumn = groupTablePrimaryColNamesList.contains(targetDaoRetrieveGroupsRequest.getSearchAttribute());
    
    boolean filterByAttribute = attributeTableAttributesNamesList.contains(targetDaoRetrieveGroupsRequest.getSearchAttribute());
    
    GrouperUtil.assertion(filterByAttribute || filterByColumn, "Must filter by attribute or column");

    if (attributeTableAttributesNamesList.size() > 0 ) {
      GrouperUtil.assertion(!StringUtils.isBlank(groupAttributesGroupForeignKeyColumn), "group attributes foreign key column must be configured");
      GrouperUtil.assertion(!StringUtils.isBlank(groupAttributesAttributeNameColumn), "group attributes attribute name column must be configured");
      GrouperUtil.assertion(!StringUtils.isBlank(groupAttributesAttributeValueColumn), "group attributes attribute value column must be configured");
      groupTableAttributesColNamesList = GrouperUtil.toList(groupAttributesGroupForeignKeyColumn, groupAttributesAttributeNameColumn, groupAttributesAttributeValueColumn);
    }
     
    if (GrouperUtil.length(targetDaoRetrieveGroupsRequest.getSearchAttributeValues()) > 0) {
      
      List<Object> idsToRetrieve = new ArrayList<Object>(targetDaoRetrieveGroupsRequest.getSearchAttributeValues());
      
      List<Object[]> groupPrimaryAttributeValues = null;
  

      if (filterByColumn) {
        
        List<String> columnsToFilterOn = GrouperUtil.toList(targetDaoRetrieveGroupsRequest.getSearchAttribute());
        
        groupPrimaryAttributeValues = SqlProvisionerCommands.retrieveObjectsColumnFilter(
            dbExternalSystemConfigId, groupTablePrimaryColNamesList, groupTableName, null, null, 
            columnsToFilterOn, idsToRetrieve, sqlProvisioningConfiguration.getSqlDeletedColumnName(), false);
        
      } else if (filterByAttribute) {
        groupPrimaryAttributeValues = SqlProvisionerCommands.retrieveObjectsAttributeFilter(dbExternalSystemConfigId, 
            groupTablePrimaryColNamesList, groupTableName, groupTableIdColumn, groupAttributesTableName, groupAttributesGroupForeignKeyColumn, 
            groupAttributesAttributeNameColumn, groupAttributesAttributeValueColumn, targetDaoRetrieveGroupsRequest.getSearchAttribute(), 
            idsToRetrieve, sqlProvisioningConfiguration.getSqlDeletedColumnName(), false);
            
      }
      
      List<Object[]> attributeValuesSeparateTable = null;

      // look up attributes if we should and if we got results
      if (attributeTableAttributesNamesList.size() > 0 && GrouperUtil.length(groupPrimaryAttributeValues) > 0 ) {
        
        List<Object> mainTableIdsFound = new ArrayList<Object>();
        for (Object[] groupPrimaryAttributeValue : groupPrimaryAttributeValues) {
          Object mainTableId = groupPrimaryAttributeValue[groupTableIdColumnIndex];
          GrouperUtil.assertion(!GrouperUtil.isBlank(mainTableId), "Why is main table ID blank?");
          mainTableIdsFound.add(mainTableId);
        }
        
        attributeValuesSeparateTable = SqlProvisionerCommands.retrieveObjectsColumnFilter(dbExternalSystemConfigId, groupTableAttributesColNamesList, 
            groupAttributesTableName, GrouperUtil.toList(groupAttributesAttributeNameColumn),  (List<Object>)(Object)attributeTableAttributesNamesList, 
            GrouperUtil.toList(groupAttributesGroupForeignKeyColumn), mainTableIdsFound, sqlProvisioningConfiguration.getSqlDeletedColumnName(), false);

      }

      retrieveGroupsAddRecord(result, 
          groupPrimaryAttributeValues, attributeValuesSeparateTable, 
          groupTablePrimaryColNamesList, attributeTableAttributesNamesList,
          groupTableIdColumn, groupAttributeNameToConfigAttribute);
    }
    
    return new TargetDaoRetrieveGroupsResponse(result);
  }

  /**
   * 
   */
  @Override
  public TargetDaoRetrieveMembershipsBulkResponse retrieveMembershipsBulk(TargetDaoRetrieveMembershipsBulkRequest targetDaoRetrieveMembershipsBulkRequest) {
    List<ProvisioningGroup> grouperTargetGroups = targetDaoRetrieveMembershipsBulkRequest == null ? null : targetDaoRetrieveMembershipsBulkRequest.getTargetGroupsForAllMemberships();
    List<ProvisioningEntity> grouperTargetEntities = targetDaoRetrieveMembershipsBulkRequest == null ? null : targetDaoRetrieveMembershipsBulkRequest.getTargetEntitiesForAllMemberships();
    List<Object> grouperTargetMemberships = targetDaoRetrieveMembershipsBulkRequest == null ? null : targetDaoRetrieveMembershipsBulkRequest.getTargetMemberships();
    List<ProvisioningMembership> targetMemberships = this.retrieveMemberships(grouperTargetGroups, grouperTargetEntities, grouperTargetMemberships);
    List<Object> targetMembershipsObjects = new ArrayList<Object>();
    targetMembershipsObjects.addAll(targetMemberships);
    return new TargetDaoRetrieveMembershipsBulkResponse(targetMembershipsObjects);
  }

  /**
   * 
   */
  @Override
  public TargetDaoRetrieveAllEntitiesResponse retrieveAllEntities(TargetDaoRetrieveAllEntitiesRequest targetDaoRetrieveAllEntitiesRequest) {
    
    boolean includeMemberships = targetDaoRetrieveAllEntitiesRequest.isIncludeAllMembershipsIfApplicable();

    GrouperProvisioningBehaviorMembershipType membershipType = this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().getGrouperProvisioningBehaviorMembershipType();
    
    boolean isEntityAttributes = membershipType == GrouperProvisioningBehaviorMembershipType.entityAttributes;
    
    SqlProvisioningConfiguration sqlProvisioningConfiguration = (SqlProvisioningConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
    
    String dbExternalSystemConfigId = sqlProvisioningConfiguration.getDbExternalSystemConfigId();
    
    String entityTableName = sqlProvisioningConfiguration.getEntityTableName();
    
    GrouperUtil.assertion(!StringUtils.isBlank(entityTableName), "Entity table name is required!");
    
    String entityTableIdColumn = sqlProvisioningConfiguration.getEntityTableIdColumn();

    GrouperUtil.assertion(!StringUtils.isBlank(entityTableIdColumn), "Entity table ID column is required!");

    String entityAttributesTableName  = sqlProvisioningConfiguration.getEntityAttributesTableName();

    String entityAttributesEntityForeignKeyColumn = sqlProvisioningConfiguration.getEntityAttributesEntityForeignKeyColumn();
    
    String entityAttributesAttributeNameColumn = sqlProvisioningConfiguration.getEntityAttributesAttributeNameColumn();

    String entityAttributesAttributeValueColumn = sqlProvisioningConfiguration.getEntityAttributesAttributeValueColumn();
    
    int entityTableIdColumnIndex = -1;
    
    List<ProvisioningEntity> result = new ArrayList<ProvisioningEntity>();
    
    List<String> entityTablePrimaryColNamesList = new ArrayList<String>();
    List<String> attributeTableAttributesNamesList = new ArrayList<String>();
    
    List<String> entityTableAttributesColNamesList = null;
    

    Map<String, GrouperProvisioningConfigurationAttribute> entityAttributeNameToConfigAttribute = sqlProvisioningConfiguration.getTargetEntityAttributeNameToConfig();
    
    for (String attributeName: entityAttributeNameToConfigAttribute.keySet()) {
      SqlGrouperProvisioningConfigurationAttribute configurationAttribute = (SqlGrouperProvisioningConfigurationAttribute) entityAttributeNameToConfigAttribute.get(attributeName);
      GrouperUtil.assertion(configurationAttribute!=null, "Configuration attribute is null: '" + attributeName + "'");
      if (!configurationAttribute.isSelect()) {
        continue;
      }
      
      boolean isMembershipAttribute = false;
      if (isEntityAttributes && !StringUtils.isBlank(sqlProvisioningConfiguration.getGroupMembershipAttributeName()) && StringUtils.equals(sqlProvisioningConfiguration.getGroupMembershipAttributeName(), attributeName)) {
        isMembershipAttribute = true;
      }

      // maybe we dont want memberships
      if (StringUtils.equals(configurationAttribute.getStorageType(), "separateAttributesTable")) {
        
        if (!isMembershipAttribute || (isEntityAttributes && includeMemberships)) {

          attributeTableAttributesNamesList.add(attributeName);

        }
        
      } else {
        if (StringUtils.equals(entityTableIdColumn,  attributeName)) {
          entityTableIdColumnIndex = GrouperUtil.length(entityTablePrimaryColNamesList);
        }

        entityTablePrimaryColNamesList.add(attributeName);
      }
    }
        
    if (attributeTableAttributesNamesList.size() > 0 ) {
      GrouperUtil.assertion(!StringUtils.isBlank(entityAttributesEntityForeignKeyColumn), "entity attributes foreign key column must be configured");
      GrouperUtil.assertion(!StringUtils.isBlank(entityAttributesAttributeNameColumn), "entity attributes attribute name column must be configured");
      GrouperUtil.assertion(!StringUtils.isBlank(entityAttributesAttributeValueColumn), "entity attributes attribute value column must be configured");
      entityTableAttributesColNamesList = GrouperUtil.toList(entityAttributesEntityForeignKeyColumn, 
          entityAttributesAttributeNameColumn, entityAttributesAttributeValueColumn);
    }
     
     
    List<Object[]> entityPrimaryAttributeValues = null;

    List<Object[]> attributeValuesSeparateTable = null;
    
    entityPrimaryAttributeValues = SqlProvisionerCommands.retrieveObjectsNoFilter(dbExternalSystemConfigId, entityTablePrimaryColNamesList, entityTableName);
    
    // look up attributes if we should and if we got results
    if (attributeTableAttributesNamesList.size() > 0 && GrouperUtil.length(entityPrimaryAttributeValues) > 0 ) {
      
      List<Object> mainTableIdsFound = new ArrayList<Object>();
      for (Object[] entityPrimaryAttributeValue : entityPrimaryAttributeValues) {
        Object mainTableId = entityPrimaryAttributeValue[entityTableIdColumnIndex];
        GrouperUtil.assertion(!GrouperUtil.isBlank(mainTableId), "Why is main table ID blank?");
        mainTableIdsFound.add(mainTableId);
      }
      
      attributeValuesSeparateTable = SqlProvisionerCommands.retrieveObjectsColumnFilter(dbExternalSystemConfigId, entityTableAttributesColNamesList, 
          entityAttributesTableName, GrouperUtil.toList(entityAttributesAttributeNameColumn),  (List<Object>)(Object)attributeTableAttributesNamesList, 
          GrouperUtil.toList(entityAttributesEntityForeignKeyColumn), mainTableIdsFound, sqlProvisioningConfiguration.getSqlDeletedColumnName(), false);
          
    }
    
    retrieveEntitiesAddRecord(result, 
        entityPrimaryAttributeValues, attributeValuesSeparateTable, 
        entityTablePrimaryColNamesList, attributeTableAttributesNamesList,
        entityTableIdColumn, entityAttributeNameToConfigAttribute);
            
    return new TargetDaoRetrieveAllEntitiesResponse(result);
    
  }

  /**
   * 
   */
  @Override
  public TargetDaoRetrieveEntitiesResponse retrieveEntities(TargetDaoRetrieveEntitiesRequest targetDaoRetrieveEntitiesRequest) {
    
    boolean includeMemberships = targetDaoRetrieveEntitiesRequest.isIncludeAllMembershipsIfApplicable();

    GrouperProvisioningBehaviorMembershipType membershipType = this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().getGrouperProvisioningBehaviorMembershipType();
    
    boolean isEntityAttributes = membershipType == GrouperProvisioningBehaviorMembershipType.entityAttributes;
    
    SqlProvisioningConfiguration sqlProvisioningConfiguration = (SqlProvisioningConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
    
    String dbExternalSystemConfigId = sqlProvisioningConfiguration.getDbExternalSystemConfigId();
    
    String entityTableName = sqlProvisioningConfiguration.getEntityTableName();
    
    String entityTableIdColumn = sqlProvisioningConfiguration.getEntityTableIdColumn();
    int entityTableIdColumnIndex = -1;
    String entityAttributesTableName  = sqlProvisioningConfiguration.getEntityAttributesTableName();

    String entityAttributesEntityForeignKeyColumn = sqlProvisioningConfiguration.getEntityAttributesEntityForeignKeyColumn();
    
    String entityAttributesAttributeNameColumn = sqlProvisioningConfiguration.getEntityAttributesAttributeNameColumn();

    String entityAttributesAttributeValueColumn = sqlProvisioningConfiguration.getEntityAttributesAttributeValueColumn();
    
    List<ProvisioningEntity> result = new ArrayList<ProvisioningEntity>();
    
    List<String> entityTablePrimaryColNamesList = new ArrayList<String>();
    List<String> attributeTableAttributesNamesList = new ArrayList<String>();
    
    List<String> entityTableAttributesColNamesList = null;

    Map<String, GrouperProvisioningConfigurationAttribute> entityAttributeNameToConfigAttribute = sqlProvisioningConfiguration.getTargetEntityAttributeNameToConfig();

    for (String attributeName: entityAttributeNameToConfigAttribute.keySet()) {
      SqlGrouperProvisioningConfigurationAttribute configurationAttribute = (SqlGrouperProvisioningConfigurationAttribute) entityAttributeNameToConfigAttribute.get(attributeName);
      GrouperUtil.assertion(configurationAttribute!=null, "Configuration attribute is null: '" + attributeName + "'");
      if (!configurationAttribute.isSelect() && !StringUtils.equals(targetDaoRetrieveEntitiesRequest.getSearchAttribute(), attributeName)) {
        continue;
      }
      
      boolean isMembershipAttribute = false;
      if (isEntityAttributes && !StringUtils.isBlank(sqlProvisioningConfiguration.getEntityMembershipAttributeName()) && StringUtils.equals(sqlProvisioningConfiguration.getEntityMembershipAttributeName(), attributeName)) {
        isMembershipAttribute = true;
      }

      // maybe we dont want memberships
      if (StringUtils.equals(configurationAttribute.getStorageType(), "separateAttributesTable")) {
        
        if (!isMembershipAttribute || (isEntityAttributes && includeMemberships)) {

          attributeTableAttributesNamesList.add(attributeName);

        }
        
      } else {
        if (StringUtils.equals(entityTableIdColumn,  attributeName)) {
          entityTableIdColumnIndex = GrouperUtil.length(entityTablePrimaryColNamesList);
        }
        entityTablePrimaryColNamesList.add(attributeName);
      }
    }

    boolean filterByColumn = entityTablePrimaryColNamesList.contains(targetDaoRetrieveEntitiesRequest.getSearchAttribute());
    
    boolean filterByAttribute = attributeTableAttributesNamesList.contains(targetDaoRetrieveEntitiesRequest.getSearchAttribute());
    
    GrouperUtil.assertion(filterByAttribute || filterByColumn, "Must filter by attribute or column");

    if (attributeTableAttributesNamesList.size() > 0 ) {
      GrouperUtil.assertion(!StringUtils.isBlank(entityAttributesEntityForeignKeyColumn), "entity attributes foreign key column must be configured");
      GrouperUtil.assertion(!StringUtils.isBlank(entityAttributesAttributeNameColumn), "entity attributes attribute name column must be configured");
      GrouperUtil.assertion(!StringUtils.isBlank(entityAttributesAttributeValueColumn), "entity attributes attribute value column must be configured");
      entityTableAttributesColNamesList = GrouperUtil.toList(entityAttributesEntityForeignKeyColumn, entityAttributesAttributeNameColumn, entityAttributesAttributeValueColumn);
    }
     
    if (GrouperUtil.length(targetDaoRetrieveEntitiesRequest.getSearchAttributeValues()) > 0) {
      
      List<Object> idsToRetrieve = new ArrayList<Object>(targetDaoRetrieveEntitiesRequest.getSearchAttributeValues());
      
      List<Object[]> entityPrimaryAttributeValues = null;

      if (filterByColumn) {
        entityPrimaryAttributeValues = SqlProvisionerCommands.retrieveObjectsColumnFilter(
            dbExternalSystemConfigId, entityTablePrimaryColNamesList, entityTableName, null, null, 
            GrouperUtil.toList(targetDaoRetrieveEntitiesRequest.getSearchAttribute()), idsToRetrieve, sqlProvisioningConfiguration.getSqlDeletedColumnName(), false);
        
      } else if (filterByAttribute) {
        entityPrimaryAttributeValues = SqlProvisionerCommands.retrieveObjectsAttributeFilter(dbExternalSystemConfigId, 
            entityTablePrimaryColNamesList, entityTableName, entityTableIdColumn, entityAttributesTableName, entityAttributesEntityForeignKeyColumn, 
            entityAttributesAttributeNameColumn, entityAttributesAttributeValueColumn, targetDaoRetrieveEntitiesRequest.getSearchAttribute(),
            idsToRetrieve, sqlProvisioningConfiguration.getSqlDeletedColumnName(), false);
            
      }
      
      List<Object[]> attributeValuesSeparateTable = null;

      // look up attributes if we should and if we got results
      if (attributeTableAttributesNamesList.size() > 0 && GrouperUtil.length(entityPrimaryAttributeValues) > 0 ) {
        
        List<Object> mainTableIdsFound = new ArrayList<Object>();
        for (Object[] entityPrimaryAttributeValue : entityPrimaryAttributeValues) {
          Object mainTableId = entityPrimaryAttributeValue[entityTableIdColumnIndex];
          GrouperUtil.assertion(!GrouperUtil.isBlank(mainTableId), "Why is main table ID blank?");
          mainTableIdsFound.add(mainTableId);
        }
        
        attributeValuesSeparateTable = SqlProvisionerCommands.retrieveObjectsColumnFilter(dbExternalSystemConfigId, entityTableAttributesColNamesList, 
            entityAttributesTableName, GrouperUtil.toList(entityAttributesAttributeNameColumn),  (List<Object>)(Object)attributeTableAttributesNamesList, 
            GrouperUtil.toList(entityAttributesEntityForeignKeyColumn), mainTableIdsFound, sqlProvisioningConfiguration.getSqlDeletedColumnName(), false);

      }

      retrieveEntitiesAddRecord(result, 
          entityPrimaryAttributeValues, attributeValuesSeparateTable, 
          entityTablePrimaryColNamesList, attributeTableAttributesNamesList,
          entityTableIdColumn, entityAttributeNameToConfigAttribute);
    }
    
    return new TargetDaoRetrieveEntitiesResponse(result);
  }


  @Override
  public void registerGrouperProvisionerDaoCapabilities(GrouperProvisionerDaoCapabilities grouperProvisionerDaoCapabilities) {
    
    grouperProvisionerDaoCapabilities.setDefaultBatchSize(1000);
    
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

    grouperProvisionerDaoCapabilities.setCanUpdateGroups(true);
    grouperProvisionerDaoCapabilities.setCanUpdateEntities(true);
    
  }
}
