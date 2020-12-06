package edu.internet2.middleware.grouper.app.sqlProvisioning;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.app.provisioning.ProvisioningEntity;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningGroup;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningObjectChange;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.GrouperProvisionerDaoCapabilities;
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
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoTimingInfo;
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
 * attribute name is the matching id: groupAttributeTableAttributeNameIsGroupMatchingId (e.g. gidNumber)
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
    long startNanos = System.nanoTime();
    
    try {
      SqlProvisioningConfiguration sqlProvisioningConfiguration = (SqlProvisioningConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
      
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
  
      String groupAttributeTableAttributeNameIsGroupMatchingId = sqlProvisioningConfiguration.getgroupAttributeTableAttributeNameIsGroupMatchingId();
      
  //    // we need to lookup the group
  //    String groupTargetUuid = new GcDbAccess().connectionName(dbExternalSystemConfigId)
  //        .sql("select " + groupAttributeTableForeignKeyToGroup + " from " + groupAttributeTableName 
  //            + " where " + groupAttributeTableAttributeNameColumn + " = ? and " + groupAttributeTableAttributeValueColumn + " = ?")
  //        .addBindVar(groupAttributeTableAttributeNameIsGroupMatchingId).addBindVar(targetGroup.getId()).select(String.class);
  
      // shouldnt happen
      if (StringUtils.isBlank(targetGroup.getId())) {
        throw new RuntimeException("Cant find group from target by " + groupAttributeTableAttributeValueColumn + " = commonId: '" + targetGroup.getId() + "'");
      }
      //TODO batch there
      for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetGroup.getInternal_objectChanges())) {
        try {
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
          provisioningObjectChange.setProvisioned(true);
        } catch (RuntimeException re) {
          provisioningObjectChange.setException(re);
          throw re;
        }
      }
      targetGroup.setProvisioned(true);
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("updateGroup", startNanos));
    }
    return null;
  }

  @Override
  public TargetDaoDeleteGroupResponse deleteGroup(TargetDaoDeleteGroupRequest targetDaoDeleteGroupRequest) {
    ProvisioningGroup targetGroup = targetDaoDeleteGroupRequest == null ? null : targetDaoDeleteGroupRequest.getTargetGroup();
    
    long startNanos = System.nanoTime();
    
    try {

      SqlProvisioningConfiguration sqlProvisioningConfiguration = (SqlProvisioningConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
      
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
      targetGroup.setProvisioned(true);
      for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetGroup.getInternal_objectChanges())) {
        provisioningObjectChange.setProvisioned(true);
      }
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("deleteGroup", startNanos));
    }
    return null;
  }

  @Override
  public TargetDaoRetrieveAllGroupsResponse retrieveAllGroups(TargetDaoRetrieveAllGroupsRequest targetDaoRetrieveAllGroupsRequest) {
    boolean includeAllMembershipsIfApplicable = targetDaoRetrieveAllGroupsRequest == null ? false: targetDaoRetrieveAllGroupsRequest.isIncludeAllMembershipsIfApplicable();
    List<ProvisioningGroup> targetGroups = retrieveGroups(true, null, includeAllMembershipsIfApplicable);
    return new TargetDaoRetrieveAllGroupsResponse(targetGroups);
  }

  public List<ProvisioningGroup> retrieveGroups(boolean retrieveAll, List<ProvisioningGroup> grouperTargetGroups, boolean retrieveAllMembershipsInGroups) {
    
    List<ProvisioningGroup> result = new ArrayList<ProvisioningGroup>();

    if (retrieveAll && grouperTargetGroups != null) {
      throw new RuntimeException("Cant retrieve all and pass in groups to retrieve");
    }
    
    SqlProvisioningConfiguration sqlProvisioningConfiguration = (SqlProvisioningConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
    
    String dbExternalSystemConfigId = sqlProvisioningConfiguration.getDbExternalSystemConfigId();
    
    String groupTableName = sqlProvisioningConfiguration.getGroupTableName();
    String groupAttributeTableName = sqlProvisioningConfiguration.getGroupAttributeTableName();
          
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
      long startNanos = System.nanoTime();
      try {
        GcDbAccess gcDbAccess = new GcDbAccess().connectionName(dbExternalSystemConfigId);
        
        groupsAndAttributeValues = gcDbAccess.sql(sqlInitial.toString()).selectList(Object[].class);
        retrieveGroupsByAttributesAddRecord(result, groupTableIdColumn, groupAttributeTableAttributeNameColumn,
            groupAttributeTableAttributeValueColumn, commaSeparatedGroupColumnNames,
            groupColumnNamesArray, groupAttributeColumnNamesArray,
            groupsAndAttributeValues);
      } finally {
        this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("retrieveAllGroups", startNanos));
      }

    } else {


      int numberOfBatches = GrouperUtil.batchNumberOfBatches(grouperTargetGroups.size(), 900);
      for (int i = 0; i < numberOfBatches; i++) {
        long startNanos = System.nanoTime();
        try {
          List<ProvisioningGroup> currentBatchGrouperTargetGroups = GrouperUtil.batchList(grouperTargetGroups, 900, i);
          StringBuilder sql = new StringBuilder(sqlInitial);
          
          // use the search filter to get the entities, should only be filtering by one attribute
          String searchAttribute = null;
          Set<Object> values = new LinkedHashSet<Object>();
          for (int j=0; j<currentBatchGrouperTargetGroups.size();j++) {
            ProvisioningGroup grouperTargetGroup = currentBatchGrouperTargetGroups.get(j);
            String searchFilter = grouperTargetGroup.getSearchFilter();
            if (StringUtils.isBlank(searchFilter)) {
              throw new RuntimeException("Cant find searchFilter for: " + grouperTargetGroup);
            }
            String attribute = GrouperUtil.trim(GrouperUtil.prefixOrSuffix(searchFilter, "=", true));
            String value = GrouperUtil.trim(GrouperUtil.prefixOrSuffix(searchFilter, "=", false));
            if (searchAttribute == null) {
              searchAttribute = attribute;
            } else {
              if (!StringUtils.equals(attribute, searchAttribute)) {
                throw new RuntimeException("Inconsistent search filters: " + searchAttribute + ", " + attribute);
              }
            }
            if (StringUtils.isBlank(value) || StringUtils.isBlank(searchAttribute)) {
              throw new RuntimeException("Invalid searchFilter: '" + searchFilter + "', " + grouperTargetGroup);
            }
            values.add(value);
          }
          
          //  WHERE a.group_uuid IN (SELECT a2.group_uuid FROM testgrouper_prov_ldap_group_attr AS a2 WHERE a2.attribute_name = 'gidNumber'
          //      AND a2.attribute_value IN ('10007'));

          sql.append(" where a.").append(groupAttributeTableForeignKeyToGroup)
            .append(" IN (SELECT a2.").append(groupAttributeTableForeignKeyToGroup)
            .append(" FROM ").append(groupAttributeTableName).append(" AS a2 WHERE a2.")
            .append(groupAttributeTableAttributeNameColumn).append(" = '")
            .append(searchAttribute)
            .append("' and a2.").append(groupAttributeTableAttributeValueColumn).append(" IN (");

          GcDbAccess gcDbAccess = new GcDbAccess().connectionName(dbExternalSystemConfigId);
          
          boolean first = true;
          for (Object value : values) {
            gcDbAccess.addBindVar(value);
            if (!first) {
              sql.append(",");
            }
            sql.append("?");
            first = false;
          }
          sql.append(" ) ) ");
          groupsAndAttributeValues = gcDbAccess.sql(sql.toString()).selectList(Object[].class);
          retrieveGroupsByAttributesAddRecord(result, groupTableIdColumn, groupAttributeTableAttributeNameColumn,
              groupAttributeTableAttributeValueColumn, commaSeparatedGroupColumnNames,
              groupColumnNamesArray, groupAttributeColumnNamesArray,
              groupsAndAttributeValues);
        } finally {
          this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("retrieveGroups", startNanos));
        }
        
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
        if (attributeName.equals(this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getGroupAttributeNameForMemberships())) {
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
    long startNanos = System.nanoTime();
    
    ProvisioningGroup targetGroup = targetDaoInsertGroupRequest.getTargetGroup();
    try {
      SqlProvisioningConfiguration sqlProvisioningConfiguration = (SqlProvisioningConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
      
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
      // get from matchingId instead?
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
                  // primary key cant be null so do default string
                  .addBindVar(GrouperUtil.defaultString(GrouperUtil.stringValue(provisioningObjectChange.getNewValue()))).executeSql();
  
                break;
                                  
              default:
                throw new RuntimeException("Not implemented");
              
              
              
            }
            
            break;
          default:
            throw new RuntimeException("Not implemented");
            
            
            
        }
        provisioningObjectChange.setProvisioned(true);
      }
      targetGroup.setProvisioned(true);
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("insertGroup", startNanos));
    }
    
    return null;
  }

  @Override
  public TargetDaoRetrieveAllEntitiesResponse retrieveAllEntities(TargetDaoRetrieveAllEntitiesRequest targetDaoRetrieveAllEntitiesRequest) {
    List<ProvisioningEntity> targetEntities = retrieveEntities(true, null);
    return new TargetDaoRetrieveAllEntitiesResponse(targetEntities);
  }

  public List<ProvisioningEntity> retrieveEntities(boolean retrieveAll, List<ProvisioningEntity> grouperTargetEntities) {
    
    List<ProvisioningEntity> result = new ArrayList<ProvisioningEntity>();
    
    if (retrieveAll && grouperTargetEntities != null) {
      throw new RuntimeException("Cant retrieve all and pass in entities to retrieve");
    }
    
    SqlProvisioningConfiguration sqlProvisioningConfiguration = (SqlProvisioningConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
    
    String dbExternalSystemConfigId = sqlProvisioningConfiguration.getDbExternalSystemConfigId();
    
    String entityTableName = sqlProvisioningConfiguration.getEntityTableName();
    String entityAttributeTableName = sqlProvisioningConfiguration.getEntityAttributeTableName();
    
    
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
      long startNanos = System.nanoTime();
      try {
        GcDbAccess gcDbAccess = new GcDbAccess().connectionName(dbExternalSystemConfigId);
        
        groupsAndAttributeValues = gcDbAccess.sql(sqlInitial.toString()).selectList(Object[].class);
        retrieveEntitiesByAttributesAddRecord(result, entityTableIdColumn, entityAttributeTableAttributeNameColumn,
            entityAttributeTableAttributeValueColumn, commaSeparatedEntityColumnNames,
            entityColumnNamesArray, entityAttributeColumnNamesArray,
            groupsAndAttributeValues);
      } finally {
        this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("retrieveAllEntities", startNanos));
      }
    } else {
    
    
      int numberOfBatches = GrouperUtil.batchNumberOfBatches(grouperTargetEntities.size(), 900);
      for (int i = 0; i < numberOfBatches; i++) {
        long startNanos = System.nanoTime();
        try {
          List<ProvisioningEntity> currentBatchGrouperTargetEntities = GrouperUtil.batchList(grouperTargetEntities, 900, i);
          StringBuilder sql = new StringBuilder(sqlInitial);
          
          // use the search filter to get the entities, should only be filtering by one attribute
          String searchAttribute = null;
          Set<Object> values = new LinkedHashSet<Object>();
          for (int j=0; j<currentBatchGrouperTargetEntities.size();j++) {
            ProvisioningEntity grouperTargetEntity = currentBatchGrouperTargetEntities.get(j);
            String searchFilter = grouperTargetEntity.getSearchFilter();
            if (StringUtils.isBlank(searchFilter)) {
              throw new RuntimeException("Cant find searchFilter for: " + grouperTargetEntity);
            }
            String attribute = GrouperUtil.trim(GrouperUtil.prefixOrSuffix(searchFilter, "=", true));
            String value = GrouperUtil.trim(GrouperUtil.prefixOrSuffix(searchFilter, "=", false));
            if (searchAttribute == null) {
              searchAttribute = attribute;
            } else {
              if (!StringUtils.equals(attribute, searchAttribute)) {
                throw new RuntimeException("Inconsistent search filters: " + searchAttribute + ", " + attribute);
              }
            }
            if (StringUtils.isBlank(value) || StringUtils.isBlank(searchAttribute)) {
              throw new RuntimeException("Invalid searchFilter: '" + searchFilter + "', " + grouperTargetEntity);
            }
            values.add(value);
          }

          //  WHERE a.entity_uuid IN (SELECT a2.entity_uuid FROM testgrouper_prov_ldap_entity_attr AS a2 WHERE a2.attribute_name = 'employeeID'
          //      AND a2.attribute_value IN ('10007'));

          sql.append(" where a.").append(entityAttributeTableForeignKeyToEntity)
            .append(" IN (SELECT a2.").append(entityAttributeTableForeignKeyToEntity)
            .append(" FROM ").append(entityAttributeTableName).append(" AS a2 WHERE a2.")
            .append(entityAttributeTableAttributeNameColumn).append(" = '")
            .append(searchAttribute)
            .append("' and a2.").append(entityAttributeTableAttributeValueColumn).append(" IN (");

          GcDbAccess gcDbAccess = new GcDbAccess().connectionName(dbExternalSystemConfigId);
          
          boolean first = true;
          for (Object value : values) {
            gcDbAccess.addBindVar(value);
            if (!first) {
              sql.append(",");
            }
            sql.append("?");
            first = false;
          }
          sql.append(" ) ) ");
          groupsAndAttributeValues = gcDbAccess.sql(sql.toString()).selectList(Object[].class);
          retrieveEntitiesByAttributesAddRecord(result, entityTableIdColumn, entityAttributeTableAttributeNameColumn,
              entityAttributeTableAttributeValueColumn, commaSeparatedEntityColumnNames,
              entityColumnNamesArray, entityAttributeColumnNamesArray,
              groupsAndAttributeValues);
        } finally {
          this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("retrieveEntities", startNanos));
        }        
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

  @Override
  public void registerGrouperProvisionerDaoCapabilities(
      GrouperProvisionerDaoCapabilities grouperProvisionerDaoCapabilities) {
    grouperProvisionerDaoCapabilities.setCanDeleteGroup(true);
    grouperProvisionerDaoCapabilities.setCanInsertGroup(true);
    grouperProvisionerDaoCapabilities.setCanRetrieveAllEntities(true);
    grouperProvisionerDaoCapabilities.setCanRetrieveAllGroups(true);
    grouperProvisionerDaoCapabilities.setCanRetrieveEntities(true);
    grouperProvisionerDaoCapabilities.setCanRetrieveGroups(true);
    grouperProvisionerDaoCapabilities.setCanRetrieveGroupWithOrWithoutMembershipAttribute(true);
    grouperProvisionerDaoCapabilities.setCanUpdateGroup(true);
    grouperProvisionerDaoCapabilities.setCanUpdateGroupMembershipAttribute(true);
    
  }

  
}
