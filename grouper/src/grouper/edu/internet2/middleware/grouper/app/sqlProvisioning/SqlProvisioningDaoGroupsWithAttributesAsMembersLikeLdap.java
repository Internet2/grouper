package edu.internet2.middleware.grouper.app.sqlProvisioning;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.app.provisioning.ProvisioningEntity;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningGroup;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningMembership;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningObjectChange;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningUpdatable;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.GrouperProvisionerTargetDaoBase;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoDeleteGroupRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoDeleteGroupResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoDeleteMembershipRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoDeleteMembershipResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoInsertMembershipRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoInsertMembershipResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveAllGroupsRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveAllGroupsResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveAllMembershipsRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveAllMembershipsResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveGroupsRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveGroupsResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveMembershipsBulkRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveMembershipsBulkResponse;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.jdbc.GcTransactionCallback;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;


public class SqlProvisioningDaoGroupsWithAttributesAsMembersLikeLdap extends GrouperProvisionerTargetDaoBase {

  /**
   * @paProvisioningGrouproup
   */
  public void updateGroup(ProvisioningGroup targetGroup) {
    SqlProvisioningConfiguration sqlProvisioningConfiguration = (SqlProvisioningConfiguration) this.getGrouperProvisioner().retrieveProvisioningConfiguration();
    
    String dbExternalSystemConfigId = sqlProvisioningConfiguration.getDbExternalSystemConfigId();
    
    String groupTableName = sqlProvisioningConfiguration.getGroupTableName();
    String groupAttributeTableName = sqlProvisioningConfiguration.getGroupAttributeTableName();
    
    if (!StringUtils.isBlank(groupTableName)) {

      // are there attributes?
      if (!StringUtils.isBlank(groupAttributeTableName)) {

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
        
        // we need to lookup the group
        String groupTargetUuid = new GcDbAccess().connectionName(dbExternalSystemConfigId)
            .sql("select " + groupAttributeTableForeignKeyToGroup + " from " + groupAttributeTableName 
                + " where " + groupAttributeTableAttributeNameColumn + " = ? and " + groupAttributeTableAttributeValueColumn + " = ?")
            .addBindVar(groupAttributeTableAttributeNameIsGroupTargetId).addBindVar(targetGroup.getId()).select(String.class);

        // shouldnt happen
        if (StringUtils.isBlank(groupTargetUuid)) {
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
                  gcDbAccess.sql(sql).addBindVar(groupTargetUuid).addBindVar(provisioningObjectChange.getAttributeName())
                    .addBindVar(provisioningObjectChange.getNewValue()).executeSql();

                  break;
                  
                  
                case delete:

                  gcDbAccess = new GcDbAccess().connectionName(dbExternalSystemConfigId);
                  
                  sql = "delete from " + groupAttributeTableName + " where " + groupAttributeTableForeignKeyToGroup + " = ? and "
                      + groupAttributeTableAttributeNameColumn + " = ? and " + groupAttributeTableAttributeValueColumn + " = ?";
                  gcDbAccess.sql(sql).addBindVar(groupTargetUuid).addBindVar(provisioningObjectChange.getAttributeName())
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
              
      } else {
        throw new RuntimeException("Not implemented");
      }
    }

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
          gcDbAccess.addBindVar(grouperTargetGroup.getTargetId());
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
        provisioningGroup.addAttributeValue(attributeName, attributeValue);
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
  public void insertGroup(ProvisioningGroup targetGroup) {
    SqlProvisioningConfiguration sqlProvisioningConfiguration = (SqlProvisioningConfiguration) this.getGrouperProvisioner().retrieveProvisioningConfiguration();
    
    String dbExternalSystemConfigId = sqlProvisioningConfiguration.getDbExternalSystemConfigId();
    
    String groupTableName = sqlProvisioningConfiguration.getGroupTableName();
    String groupAttributeTableName = sqlProvisioningConfiguration.getGroupAttributeTableName();
    
    if (!StringUtils.isBlank(groupTableName)) {
  
      // are there attributes?
      if (!StringUtils.isBlank(groupAttributeTableName)) {
  
        // join group to attribute table
        
        String groupTableIdColumn = sqlProvisioningConfiguration.getGroupTableIdColumn();
        
        String groupAttributeTableAttributeNameColumn = sqlProvisioningConfiguration.getGroupAttributeTableAttributeNameColumn();
        String groupAttributeTableAttributeValueColumn = sqlProvisioningConfiguration.getGroupAttributeTableAttributeValueColumn();
        
        String groupAttributeTableForeignKeyToGroup = sqlProvisioningConfiguration.getGroupAttributeTableForeignKeyToGroup();
  
        String commaSeparatedGroupAttributeColumnNames = groupAttributeTableForeignKeyToGroup + ", " + groupAttributeTableAttributeNameColumn + ", " + groupAttributeTableAttributeValueColumn;
  
        String groupAttributeTableAttributeNameIsGroupTargetId = sqlProvisioningConfiguration.getgroupAttributeTableAttributeNameIsGroupTargetId();

        GcDbAccess gcDbAccess = new GcDbAccess().connectionName(dbExternalSystemConfigId);
        
        String sql = "insert into " + groupTableName + "(" + groupTableIdColumn + ") values (?)";
        // get from targetId instead?
        Object groupUuid = targetGroup.retrieveAttributeValue(groupAttributeTableAttributeNameIsGroupTargetId);
        if (groupUuid == null) {
          throw new RuntimeException("Cant find group target id from attribute: '" + groupAttributeTableAttributeNameIsGroupTargetId + "': " + targetGroup);
        }
        gcDbAccess.sql(sql).addBindVar(groupUuid).executeSql();
        
        //TODO batch there
        for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetGroup.getInternal_objectChanges())) {
  
          switch (provisioningObjectChange.getProvisioningObjectChangeDataType()) {
            case field:
              throw new RuntimeException("Not implemented");
            case attribute:
              
              switch (provisioningObjectChange.getProvisioningObjectChangeAction()) {
                
                case insert:
                  
                  gcDbAccess = new GcDbAccess().connectionName(dbExternalSystemConfigId);
                  
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
              
      } else {
        throw new RuntimeException("Not implemented");
      }
    }
  
  }

  
}
