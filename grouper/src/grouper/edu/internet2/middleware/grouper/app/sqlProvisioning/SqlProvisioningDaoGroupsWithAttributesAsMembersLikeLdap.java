package edu.internet2.middleware.grouper.app.sqlProvisioning;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.app.provisioning.ProvisioningEntity;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningGroup;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningObjectChange;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.GrouperProvisionerTargetDaoBase;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoDeleteGroupRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoDeleteGroupResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoInsertGroupRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoInsertGroupResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveAllEntitiesRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveAllEntitiesResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveAllGroupsRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveAllGroupsResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveEntitiesRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveEntitiesResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveGroupsRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveGroupsResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoUpdateGroupRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoUpdateGroupResponse;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.jdbc.GcTransactionCallback;

/**
 * group table: groupTableName
 * group table id col: groupTableIdColumn   (this will be the group "id" similar to the dn)
 * attribute table: groupAttributeTableName
 * attribute table foreign key col: groupAttributeTableForeignKeyToGroup
 * attribute name col: groupAttributeTableAttributeNameColumn
 * attribute value col: groupAttributeTableAttributeValueColumn
 * attribute name is the target id: groupAttributeTableAttributeNameIsGroupTargetId (e.g. gidNumber)
 * 
 * @author mchyzer-local
 *
 */
public class SqlProvisioningDaoGroupsWithAttributesAsMembersLikeLdap extends GrouperProvisionerTargetDaoBase {

  /**
   * @paProvisioningGrouproup
   */
  @Override
  public TargetDaoUpdateGroupResponse updateGroup(TargetDaoUpdateGroupRequest targetDaoUpdateGroupRequest) {
    ProvisioningGroup targetGroup = targetDaoUpdateGroupRequest.getTargetGroup();
    SqlProvisioningConfiguration sqlProvisioningConfiguration = (SqlProvisioningConfiguration) this.getGrouperProvisioner().retrieveProvisioningConfiguration();
    
    String dbExternalSystemConfigId = sqlProvisioningConfiguration.getDbExternalSystemConfigId();
    
    String groupTableName = sqlProvisioningConfiguration.getGroupTableName();
    String groupAttributeTableName = sqlProvisioningConfiguration.getGroupAttributeTableName();
    
    // join group to attribute table
    
    String groupTableIdColumn = sqlProvisioningConfiguration.getGroupTableIdColumn();
    
    String groupAttributeTableAttributeNameColumn = sqlProvisioningConfiguration.getGroupAttributeTableAttributeNameColumn();
    String groupAttributeTableAttributeValueColumn = sqlProvisioningConfiguration.getGroupAttributeTableAttributeValueColumn();
    
    String groupAttributeTableForeignKeyToGroup = sqlProvisioningConfiguration.getGroupAttributeTableForeignKeyToGroup();
    
    String commaSeparatedGroupColumnNames = sqlProvisioningConfiguration.getGroupAttributeNames();
    String[] groupColumnNamesArray = GrouperUtil.splitTrim(commaSeparatedGroupColumnNames, ",");

    String commaSeparatedGroupAttributeColumnNames = groupAttributeTableForeignKeyToGroup + ", " + groupAttributeTableAttributeNameColumn + ", " + groupAttributeTableAttributeValueColumn;
    String[] groupAttributeColumnNamesArray = GrouperUtil.splitTrim(commaSeparatedGroupAttributeColumnNames, ",");

    String groupAttributeTableAttributeNameIsGroupTargetId = sqlProvisioningConfiguration.getgroupAttributeTableAttributeNameIsGroupTargetId();
    
//    // we need to lookup the group
//    String groupTargetUuid = new GcDbAccess().connectionName(dbExternalSystemConfigId)
//        .sql("select " + groupAttributeTableForeignKeyToGroup + " from " + groupAttributeTableName 
//            + " where " + groupAttributeTableAttributeNameColumn + " = ? and " + groupAttributeTableAttributeValueColumn + " = ?")
//        .addBindVar(groupAttributeTableAttributeNameIsGroupTargetId).addBindVar(targetGroup.getId()).select(String.class);

    // shouldnt happen
    if (StringUtils.isBlank(targetGroup.getId())) {
      throw new RuntimeException("Cant find group from target by " + groupAttributeTableAttributeValueColumn + " = commonId: '" + targetGroup.getId() + "'");
    }
    //TODO batch there
    for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetGroup.getInternal_objectChanges())) {

      switch (provisioningObjectChange.getProvisioningObjectChangeDataType()) {
        case field:
          throw new RuntimeException("Not implemented");
        case attribute:
          
          switch (provisioningObjectChange.getProvisioningObjectChangeAction()) {
            
            case insert:
              
              GcDbAccess gcDbAccess = new GcDbAccess().connectionName(dbExternalSystemConfigId);
              
              String sql = "insert into " + groupAttributeTableName + "(" + commaSeparatedGroupAttributeColumnNames + ") values (?, ?, ?)";
              gcDbAccess.sql(sql).addBindVar(targetGroup.getId()).addBindVar(provisioningObjectChange.getAttributeName())
                .addBindVar(provisioningObjectChange.getNewValue()).executeSql();

              break;
              
              
            case delete:

              gcDbAccess = new GcDbAccess().connectionName(dbExternalSystemConfigId);
              
              sql = "delete from " + groupAttributeTableName + " where " + groupAttributeTableForeignKeyToGroup + " = ? and "
                  + groupAttributeTableAttributeNameColumn + " = ? and " + groupAttributeTableAttributeValueColumn + " = ?";
              gcDbAccess.sql(sql).addBindVar(targetGroup.getId()).addBindVar(provisioningObjectChange.getAttributeName())
                .addBindVar(provisioningObjectChange.getOldValue()).executeSql();

              break;
              
            case update:

              gcDbAccess = new GcDbAccess().connectionName(dbExternalSystemConfigId);
              
              sql = "update " + groupAttributeTableName + " set " + groupAttributeTableAttributeValueColumn 
                  + " = ? where " + groupAttributeTableForeignKeyToGroup + " = ? and "
                  + groupAttributeTableAttributeNameColumn + " = ? and " + groupAttributeTableAttributeValueColumn + " = ?";
              gcDbAccess.sql(sql).addBindVar(provisioningObjectChange.getNewValue())
                .addBindVar(targetGroup.getId()).addBindVar(provisioningObjectChange.getAttributeName()).addBindVar(provisioningObjectChange.getOldValue())
                .executeSql();

              break;
              
            default:
              throw new RuntimeException("Not implemented");
            
            
            
          }
          
          break;
        default:
          throw new RuntimeException("Not implemented");
          
          
          
      }
    }

    return null;
  }

  @Override
  public TargetDaoDeleteGroupResponse deleteGroup(TargetDaoDeleteGroupRequest targetDaoDeleteGroupRequest) {
    ProvisioningGroup targetGroup = targetDaoDeleteGroupRequest == null ? null : targetDaoDeleteGroupRequest.getTargetGroup();
    SqlProvisioningConfiguration sqlProvisioningConfiguration = (SqlProvisioningConfiguration) this.getGrouperProvisioner().retrieveProvisioningConfiguration();
    
    String dbExternalSystemConfigId = sqlProvisioningConfiguration.getDbExternalSystemConfigId();
    
    String groupTableName = sqlProvisioningConfiguration.getGroupTableName();
    String groupAttributeTableName = sqlProvisioningConfiguration.getGroupAttributeTableName();

    //both of those must exist
    
    // join group to attribute table
    
    String groupTableIdColumn = sqlProvisioningConfiguration.getGroupTableIdColumn();
            
    String groupAttributeTableForeignKeyToGroup = sqlProvisioningConfiguration.getGroupAttributeTableForeignKeyToGroup();
    
    GcDbAccess gcDbAccess = new GcDbAccess().connectionName(dbExternalSystemConfigId);
    
    int records = gcDbAccess.callbackTransaction(new GcTransactionCallback<Integer>() {

      @Override
      public Integer callback(GcDbAccess dbAccess) {
        // delete attributes
        String sql = "delete from  " + groupAttributeTableName + " where " + groupAttributeTableForeignKeyToGroup + " = ?";
 
        GcDbAccess gcDbAccess = new GcDbAccess().connectionName(dbExternalSystemConfigId);
        
        int count = gcDbAccess.sql(sql).addBindVar(targetGroup.getId()).executeSql();

        gcDbAccess = new GcDbAccess().connectionName(dbExternalSystemConfigId);
        
        // delete attributes
        sql = "delete from  " + groupTableName + " where " + groupTableIdColumn + " = ?";

        count += gcDbAccess.sql(sql).addBindVar(targetGroup.getId()).executeSql();
        
        return count;
      }
      
    });
    return null;
  }

  @Override
  public TargetDaoRetrieveAllGroupsResponse retrieveAllGroups(TargetDaoRetrieveAllGroupsRequest targetDaoRetrieveAllGroupsRequest) {
    boolean includeAllMembershipsIfApplicable = targetDaoRetrieveAllGroupsRequest == null ? false: targetDaoRetrieveAllGroupsRequest.isIncludeAllMembershipsIfApplicable();
    List<ProvisioningGroup> targetGroups = retrieveGroups(true, null, includeAllMembershipsIfApplicable);
    return new TargetDaoRetrieveAllGroupsResponse(targetGroups);
  }

  public List<ProvisioningGroup> retrieveGroups(boolean retrieveAll, List<ProvisioningGroup> grouperTargetGroups, boolean retrieveAllMembershipsInGroups) {
    
    if (retrieveAll && grouperTargetGroups != null) {
      throw new RuntimeException("Cant retrieve all and pass in groups to retrieve");
    }
    
    SqlProvisioningConfiguration sqlProvisioningConfiguration = (SqlProvisioningConfiguration) this.getGrouperProvisioner().retrieveProvisioningConfiguration();
    
    String dbExternalSystemConfigId = sqlProvisioningConfiguration.getDbExternalSystemConfigId();
    
    String groupTableName = sqlProvisioningConfiguration.getGroupTableName();
    String groupAttributeTableName = sqlProvisioningConfiguration.getGroupAttributeTableName();
    
    List<ProvisioningGroup> result = new ArrayList<ProvisioningGroup>();
    
    if (!retrieveAll && GrouperUtil.isBlank(grouperTargetGroups)) {
      return result;
    }
    
    // join group to attribute table
    
    String groupTableIdColumn = sqlProvisioningConfiguration.getGroupTableIdColumn();
    
    String groupAttributeTableAttributeNameColumn = sqlProvisioningConfiguration.getGroupAttributeTableAttributeNameColumn();
    String groupAttributeTableAttributeValueColumn = sqlProvisioningConfiguration.getGroupAttributeTableAttributeValueColumn();
    
    String groupAttributeTableForeignKeyToGroup = sqlProvisioningConfiguration.getGroupAttributeTableForeignKeyToGroup();
    
    String commaSeparatedGroupColumnNames = sqlProvisioningConfiguration.getGroupAttributeNames();
    String[] groupColumnNamesArray = GrouperUtil.splitTrim(commaSeparatedGroupColumnNames, ",");

    String commaSeparatedGroupAttributeColumnNames = groupAttributeTableAttributeNameColumn + ", " + groupAttributeTableAttributeValueColumn;
    String[] groupAttributeColumnNamesArray = GrouperUtil.splitTrim(commaSeparatedGroupAttributeColumnNames, ",");

    String groupAttributeNameForMemberships = sqlProvisioningConfiguration.getGroupAttributeNameForMemberships();
        
    StringBuilder sqlInitial = new StringBuilder("select ");
    
    for (int i=0;i<groupColumnNamesArray.length; i++) {
      if (i>0) {
        sqlInitial.append(", ");
      }
      sqlInitial.append("g.").append(groupColumnNamesArray[i]);
    }
    
    for (int i=0;i<groupAttributeColumnNamesArray.length; i++) {
      sqlInitial.append(", ");
      sqlInitial.append("a.").append(groupAttributeColumnNamesArray[i]);
    }
    
    sqlInitial.append(" from ").append(groupTableName).append(" as g left outer join ").append(groupAttributeTableName).append(" as a on g.")
      .append(groupTableIdColumn).append(" = a.").append(groupAttributeTableForeignKeyToGroup);
    
    if (!retrieveAllMembershipsInGroups) {
      sqlInitial.append(" and a." + groupAttributeTableAttributeNameColumn + " != '" + groupAttributeNameForMemberships + "' ");
    }
    
    List<Object[]> groupsAndAttributeValues = null;
    
    if (retrieveAll) {
      GcDbAccess gcDbAccess = new GcDbAccess().connectionName(dbExternalSystemConfigId);
      
      groupsAndAttributeValues = gcDbAccess.sql(sqlInitial.toString()).selectList(Object[].class);
      retrieveGroupsByAttributesAddRecord(result, groupTableIdColumn, groupAttributeTableAttributeNameColumn,
          groupAttributeTableAttributeValueColumn, commaSeparatedGroupColumnNames,
          groupColumnNamesArray, groupAttributeColumnNamesArray,
          groupsAndAttributeValues);
    } else {


      int numberOfBatches = GrouperUtil.batchNumberOfBatches(grouperTargetGroups.size(), 900);
      for (int i = 0; i < numberOfBatches; i++) {
        List<ProvisioningGroup> currentBatchGrouperTargetGroups = GrouperUtil.batchList(grouperTargetGroups, 900, i);
        StringBuilder sql = new StringBuilder(sqlInitial);
        sql.append(" where g.").append(groupTableIdColumn).append(" in (");
        GcDbAccess gcDbAccess = new GcDbAccess().connectionName(dbExternalSystemConfigId);
        
        for (int j=0; j<currentBatchGrouperTargetGroups.size();j++) {
          ProvisioningGroup grouperTargetGroup = currentBatchGrouperTargetGroups.get(j);
          gcDbAccess.addBindVar(grouperTargetGroup.getId());
          if (j>0) {
            sql.append(",");
          }
          sql.append("?");
        }
        sql.append(" ) ");
        groupsAndAttributeValues = gcDbAccess.sql(sql.toString()).selectList(Object[].class);
        retrieveGroupsByAttributesAddRecord(result, groupTableIdColumn, groupAttributeTableAttributeNameColumn,
            groupAttributeTableAttributeValueColumn, commaSeparatedGroupColumnNames,
            groupColumnNamesArray, groupAttributeColumnNamesArray,
            groupsAndAttributeValues);
        
      }
    }

    
    return result;
   
  }

  private void retrieveGroupsByAttributesAddRecord(List<ProvisioningGroup> result, String groupTableIdColumn,
      String groupAttributeTableAttributeNameColumn,
      String groupAttributeTableAttributeValueColumn,
      String commaSeparatedGroupColumnNames, String[] groupColumnNamesArray,
      String[] groupAttributeColumnNamesArray, List<Object[]> groupsAndAttributeValues) {
    Map<String, ProvisioningGroup> uuidToProvisioningGroup = new HashMap<String, ProvisioningGroup>();
    
    // find the group id col
    int columnIndexOfGroupId = -1;
    for (int i=0;i<groupColumnNamesArray.length; i++) {

      String colName = groupColumnNamesArray[i];

      if (StringUtils.equalsIgnoreCase(groupTableIdColumn, colName)) {
        columnIndexOfGroupId = i;
        break;
      }
    }

    if (columnIndexOfGroupId == -1) {
      throw new RuntimeException("Cant find id of group table! '" + commaSeparatedGroupColumnNames + "', '" + groupTableIdColumn + "'");
    }
    
    for (Object[] groupsAndAttributeValue: GrouperUtil.nonNull(groupsAndAttributeValues)) {
      
      String groupId = GrouperUtil.stringValue(groupsAndAttributeValue[columnIndexOfGroupId]);

      if (StringUtils.isBlank(groupId)) {
        throw new RuntimeException("Blank group id!!!! " + GrouperUtil.toStringForLog(groupsAndAttributeValue));
      }
      
      // link this row to an existing group that was retrieved (since each row has an attribute)
      ProvisioningGroup provisioningGroup = uuidToProvisioningGroup.get(groupId);
      
      if (provisioningGroup == null) {
        provisioningGroup = new ProvisioningGroup();
        result.add(provisioningGroup);
        uuidToProvisioningGroup.put(groupId, provisioningGroup);
      }

      int columnIndex = 0;
      for (int i=0;i<groupColumnNamesArray.length; i++) {

        String colName = groupColumnNamesArray[i];

        Object value = groupsAndAttributeValue[columnIndex];
        
        if (StringUtils.equalsIgnoreCase(groupTableIdColumn, colName)) {
          provisioningGroup.setId(GrouperUtil.stringValue(value));
        } else {
          provisioningGroup.assignAttributeValue(colName, value);
        }
        columnIndex++;
      }
      String attributeName = null;
      Object attributeValue = null;
      
      for (int i=0;i<groupAttributeColumnNamesArray.length; i++) {

        String colName = groupAttributeColumnNamesArray[i];
        Object value = groupsAndAttributeValue[columnIndex];

        if (StringUtils.equalsIgnoreCase(groupAttributeTableAttributeNameColumn, colName)) {
          attributeName = GrouperUtil.stringValue(value);
        } else if (StringUtils.equalsIgnoreCase(groupAttributeTableAttributeValueColumn, colName)) {
          attributeValue = GrouperUtil.stringValue(value);
        }
        columnIndex++;
      }
      if (!StringUtils.isBlank(attributeName)) {
        if (attributeName.equals(this.getGrouperProvisioner().retrieveProvisioningConfiguration().getGroupAttributeNameForMemberships())) {
          provisioningGroup.addAttributeValue(attributeName, attributeValue);
        } else {
          provisioningGroup.assignAttributeValue(attributeName, attributeValue);
        }
      }
            
    }
  }

  @Override
  public TargetDaoRetrieveGroupsResponse retrieveGroups(TargetDaoRetrieveGroupsRequest targetDaoRetrieveGroupsRequest) {
    List<ProvisioningGroup> grouperTargetGroups = targetDaoRetrieveGroupsRequest == null ? null : targetDaoRetrieveGroupsRequest.getTargetGroups();
    boolean retrieveAllMembershipsInGroups = targetDaoRetrieveGroupsRequest == null ? false : targetDaoRetrieveGroupsRequest.isIncludeAllMembershipsIfApplicable();
    List<ProvisioningGroup> targetGroups = this.retrieveGroups(false, grouperTargetGroups, retrieveAllMembershipsInGroups);
    return new TargetDaoRetrieveGroupsResponse(targetGroups);
  }

  /**
   * @paProvisioningGrouproup
   */
  @Override
  public TargetDaoInsertGroupResponse insertGroup(TargetDaoInsertGroupRequest targetDaoInsertGroupRequest) {
    ProvisioningGroup targetGroup = targetDaoInsertGroupRequest.getTargetGroup();
    SqlProvisioningConfiguration sqlProvisioningConfiguration = (SqlProvisioningConfiguration) this.getGrouperProvisioner().retrieveProvisioningConfiguration();
    
    String dbExternalSystemConfigId = sqlProvisioningConfiguration.getDbExternalSystemConfigId();
    
    String groupTableName = sqlProvisioningConfiguration.getGroupTableName();
    String groupAttributeTableName = sqlProvisioningConfiguration.getGroupAttributeTableName();
    
    // join group to attribute table
    
    String groupTableIdColumn = sqlProvisioningConfiguration.getGroupTableIdColumn();
    
    String groupAttributeTableAttributeNameColumn = sqlProvisioningConfiguration.getGroupAttributeTableAttributeNameColumn();
    String groupAttributeTableAttributeValueColumn = sqlProvisioningConfiguration.getGroupAttributeTableAttributeValueColumn();
    
    String groupAttributeTableForeignKeyToGroup = sqlProvisioningConfiguration.getGroupAttributeTableForeignKeyToGroup();

    String commaSeparatedGroupAttributeColumnNames = groupAttributeTableForeignKeyToGroup + ", " + groupAttributeTableAttributeNameColumn + ", " + groupAttributeTableAttributeValueColumn;

    GcDbAccess gcDbAccess = new GcDbAccess().connectionName(dbExternalSystemConfigId);
    
    String sql = "insert into " + groupTableName + "(" + groupTableIdColumn + ") values (?)";
    // get from targetId instead?
    Object groupUuid = targetGroup.getId();
    if (groupUuid == null) {
      throw new RuntimeException("Cant find group id: " + targetGroup);
    }
    gcDbAccess.sql(sql).addBindVar(groupUuid).executeSql();
    
    for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetGroup.getInternal_objectChanges())) {

      switch (provisioningObjectChange.getProvisioningObjectChangeDataType()) {
        case field:
          if (!"id".equals(provisioningObjectChange.getFieldName())) {
            throw new RuntimeException("Not implemented");
          }
          break;
        case attribute:
          
          switch (provisioningObjectChange.getProvisioningObjectChangeAction()) {
            
            case insert:
              
              gcDbAccess = new GcDbAccess().connectionName(dbExternalSystemConfigId);
              GrouperUtil.assertion(!StringUtils.isBlank(provisioningObjectChange.getAttributeName()), "attribute name is null");
              sql = "insert into " + groupAttributeTableName + "(" + commaSeparatedGroupAttributeColumnNames + ") values (?, ?, ?)";
              gcDbAccess.sql(sql).addBindVar(groupUuid).addBindVar(provisioningObjectChange.getAttributeName())
                .addBindVar(provisioningObjectChange.getNewValue()).executeSql();

              break;
                                
            default:
              throw new RuntimeException("Not implemented");
            
            
            
          }
          
          break;
        default:
          throw new RuntimeException("Not implemented");
          
          
          
      }
    }
    
    return null;
  }

  @Override
  public TargetDaoRetrieveAllEntitiesResponse retrieveAllEntities(TargetDaoRetrieveAllEntitiesRequest targetDaoRetrieveAllEntitiesRequest) {
    List<ProvisioningEntity> targetEntities = retrieveEntities(true, null);
    return new TargetDaoRetrieveAllEntitiesResponse(targetEntities);
  }

  public List<ProvisioningEntity> retrieveEntities(boolean retrieveAll, List<ProvisioningEntity> grouperTargetEntities) {
    
    if (retrieveAll && grouperTargetEntities != null) {
      throw new RuntimeException("Cant retrieve all and pass in entities to retrieve");
    }
    
    SqlProvisioningConfiguration sqlProvisioningConfiguration = (SqlProvisioningConfiguration) this.getGrouperProvisioner().retrieveProvisioningConfiguration();
    
    String dbExternalSystemConfigId = sqlProvisioningConfiguration.getDbExternalSystemConfigId();
    
    String entityTableName = sqlProvisioningConfiguration.getEntityTableName();
    String entityAttributeTableName = sqlProvisioningConfiguration.getEntityAttributeTableName();
    
    List<ProvisioningEntity> result = new ArrayList<ProvisioningEntity>();
    
    if (!retrieveAll && GrouperUtil.isBlank(grouperTargetEntities)) {
      return result;
    }
    
    // join entity to attribute table
    
    String entityTableIdColumn = sqlProvisioningConfiguration.getEntityTableIdColumn();
    
    String entityAttributeTableAttributeNameColumn = sqlProvisioningConfiguration.getEntityAttributeTableAttributeNameColumn();
    String entityAttributeTableAttributeValueColumn = sqlProvisioningConfiguration.getEntityAttributeTableAttributeValueColumn();
    
    String entityAttributeTableForeignKeyToEntity = sqlProvisioningConfiguration.getEntityAttributeTableForeignKeyToEntity();
    
    String commaSeparatedEntityColumnNames = sqlProvisioningConfiguration.getEntityAttributeNames();
    String[] entityColumnNamesArray = GrouperUtil.splitTrim(commaSeparatedEntityColumnNames, ",");
  
    String commaSeparatedEntityAttributeColumnNames = entityAttributeTableAttributeNameColumn + ", " + entityAttributeTableAttributeValueColumn;
    String[] entityAttributeColumnNamesArray = GrouperUtil.splitTrim(commaSeparatedEntityAttributeColumnNames, ",");
  
    StringBuilder sqlInitial = new StringBuilder("select ");
    
    for (int i=0;i<entityColumnNamesArray.length; i++) {
      if (i>0) {
        sqlInitial.append(", ");
      }
      sqlInitial.append("e.").append(entityColumnNamesArray[i]);
    }
    
    for (int i=0;i<entityAttributeColumnNamesArray.length; i++) {
      sqlInitial.append(", ");
      sqlInitial.append("a.").append(entityAttributeColumnNamesArray[i]);
    }
    
    sqlInitial.append(" from ").append(entityTableName).append(" as e left outer join ").append(entityAttributeTableName).append(" as a on e.")
      .append(entityTableIdColumn).append(" = a.").append(entityAttributeTableForeignKeyToEntity);
    
    List<Object[]> groupsAndAttributeValues = null;
    
    if (retrieveAll) {
      GcDbAccess gcDbAccess = new GcDbAccess().connectionName(dbExternalSystemConfigId);
      
      groupsAndAttributeValues = gcDbAccess.sql(sqlInitial.toString()).selectList(Object[].class);
      retrieveEntitiesByAttributesAddRecord(result, entityTableIdColumn, entityAttributeTableAttributeNameColumn,
          entityAttributeTableAttributeValueColumn, commaSeparatedEntityColumnNames,
          entityColumnNamesArray, entityAttributeColumnNamesArray,
          groupsAndAttributeValues);
    } else {
  
  
      int numberOfBatches = GrouperUtil.batchNumberOfBatches(grouperTargetEntities.size(), 900);
      for (int i = 0; i < numberOfBatches; i++) {
        List<ProvisioningEntity> currentBatchGrouperTargetEntities = GrouperUtil.batchList(grouperTargetEntities, 900, i);
        StringBuilder sql = new StringBuilder(sqlInitial);
        sql.append(" where e.").append(entityTableIdColumn).append(" in (");
        GcDbAccess gcDbAccess = new GcDbAccess().connectionName(dbExternalSystemConfigId);
        
        for (int j=0; j<currentBatchGrouperTargetEntities.size();j++) {
          ProvisioningEntity grouperTargetEntity = currentBatchGrouperTargetEntities.get(j);
          gcDbAccess.addBindVar(grouperTargetEntity.getId());
          if (j>0) {
            sql.append(",");
          }
          sql.append("?");
        }
        sql.append(" ) ");
        groupsAndAttributeValues = gcDbAccess.sql(sql.toString()).selectList(Object[].class);
        retrieveEntitiesByAttributesAddRecord(result, entityTableIdColumn, entityAttributeTableAttributeNameColumn,
            entityAttributeTableAttributeValueColumn, commaSeparatedEntityColumnNames,
            entityColumnNamesArray, entityAttributeColumnNamesArray,
            groupsAndAttributeValues);
        
      }
    }
  
    
    return result;
  
  }

  private void retrieveEntitiesByAttributesAddRecord(List<ProvisioningEntity> result, String entityTableIdColumn,
      String entityAttributeTableAttributeNameColumn,
      String entityAttributeTableAttributeValueColumn,
      String commaSeparatedEntityColumnNames, String[] entityColumnNamesArray,
      String[] entityAttributeColumnNamesArray, List<Object[]> entitiesAndAttributeValues) {
    Map<String, ProvisioningEntity> uuidToProvisioningEntity = new HashMap<String, ProvisioningEntity>();
    
    // find the group id col
    int columnIndexOfEntityId = -1;
    for (int i=0;i<entityColumnNamesArray.length; i++) {
  
      String colName = entityColumnNamesArray[i];
  
      if (StringUtils.equalsIgnoreCase(entityTableIdColumn, colName)) {
        columnIndexOfEntityId = i;
        break;
      }
    }
  
    if (columnIndexOfEntityId == -1) {
      throw new RuntimeException("Cant find id of entity table! '" + commaSeparatedEntityColumnNames + "', '" + entityTableIdColumn + "'");
    }
    
    for (Object[] entitiesAndAttributeValue: GrouperUtil.nonNull(entitiesAndAttributeValues)) {
      
      String entityId = GrouperUtil.stringValue(entitiesAndAttributeValue[columnIndexOfEntityId]);
  
      if (StringUtils.isBlank(entityId)) {
        throw new RuntimeException("Blank entity id!!!! " + GrouperUtil.toStringForLog(entitiesAndAttributeValue));
      }
      
      // link this row to an existing group that was retrieved (since each row has an attribute)
      ProvisioningEntity provisioningEntity = uuidToProvisioningEntity.get(entityId);
      
      if (provisioningEntity == null) {
        provisioningEntity = new ProvisioningEntity();
        result.add(provisioningEntity);
        uuidToProvisioningEntity.put(entityId, provisioningEntity);
      }
  
      int columnIndex = 0;
      for (int i=0;i<entityColumnNamesArray.length; i++) {
  
        String colName = entityColumnNamesArray[i];
  
        Object value = entitiesAndAttributeValue[columnIndex];
        
        if (StringUtils.equalsIgnoreCase(entityTableIdColumn, colName)) {
          provisioningEntity.setId(GrouperUtil.stringValue(value));
        } else {
          provisioningEntity.assignAttributeValue(colName, value);
        }
        columnIndex++;
      }
      String attributeName = null;
      Object attributeValue = null;
      
      for (int i=0;i<entityAttributeColumnNamesArray.length; i++) {
  
        String colName = entityAttributeColumnNamesArray[i];
        Object value = entitiesAndAttributeValue[columnIndex];
  
        if (StringUtils.equalsIgnoreCase(entityAttributeTableAttributeNameColumn, colName)) {
          attributeName = GrouperUtil.stringValue(value);
        } else if (StringUtils.equalsIgnoreCase(entityAttributeTableAttributeValueColumn, colName)) {
          attributeValue = GrouperUtil.stringValue(value);
        }
        columnIndex++;
      }
      if (!StringUtils.isBlank(attributeName)) {
        provisioningEntity.assignAttributeValue(attributeName, attributeValue);
      }
    }
  }

  @Override
  public TargetDaoRetrieveEntitiesResponse retrieveEntities(TargetDaoRetrieveEntitiesRequest targetDaoRetrieveEntitiesRequest) {
    List<ProvisioningEntity> grouperTargetEntities = targetDaoRetrieveEntitiesRequest == null ? null : targetDaoRetrieveEntitiesRequest.getTargetEntities();
    List<ProvisioningEntity> targetEntities = this.retrieveEntities(false, grouperTargetEntities);
    return new TargetDaoRetrieveEntitiesResponse(targetEntities);
  }

  
}
