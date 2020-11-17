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
import edu.internet2.middleware.grouper.app.provisioning.targetDao.GrouperProvisionerDaoCapabilities;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.GrouperProvisionerTargetDaoBase;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoDeleteGroupRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoDeleteGroupResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoDeleteMembershipRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoDeleteMembershipResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoInsertGroupRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoInsertGroupResponse;
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


public class SqlProvisioningDao extends GrouperProvisionerTargetDaoBase {

  @Override
  public TargetDaoRetrieveAllMembershipsResponse retrieveAllMemberships(TargetDaoRetrieveAllMembershipsRequest targetDaoRetrieveAllMembershipsRequest) {
    List<ProvisioningMembership> targetMemberships = this.retrieveMemberships(true, null, null, null);
    return new TargetDaoRetrieveAllMembershipsResponse(targetMemberships);
  }

  public List<ProvisioningMembership> retrieveMemberships(boolean retrieveAll, List<ProvisioningGroup> grouperTargetGroups, List<ProvisioningEntity> grouperTargetEntities, List<MultiKey> grouperTargetMembershipMultiKeys) {
    
    if (retrieveAll && (grouperTargetGroups != null || grouperTargetEntities != null || grouperTargetMembershipMultiKeys != null)) {
      throw new RuntimeException("Cant retrieve all and pass in groups to retrieve");
    }
    
    SqlProvisioningConfiguration sqlProvisioningConfiguration = (SqlProvisioningConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
    
    String dbExternalSystemConfigId = sqlProvisioningConfiguration.getDbExternalSystemConfigId();

    String membershipGroupColumn = sqlProvisioningConfiguration.getMembershipGroupColumn();
    String membershipUserColumn = sqlProvisioningConfiguration.getMembershipUserColumn();

    String membershipTableName = sqlProvisioningConfiguration.getMembershipTableName();
    
    List<ProvisioningMembership> result = new ArrayList<ProvisioningMembership>();

    List<ProvisioningMembership> grouperTargetMemberships = new ArrayList<ProvisioningMembership>();
    for (MultiKey grouperTargetMembershipMultiKey : GrouperUtil.nonNull(grouperTargetMembershipMultiKeys)) {
      ProvisioningMembership grouperTargetMembership = (ProvisioningMembership)grouperTargetMembershipMultiKey.getKey(2);
      if (grouperTargetMembership != null) {
        grouperTargetMemberships.add(grouperTargetMembership);
      }
    }
    
    if (!StringUtils.isBlank(membershipTableName)) {

      String commaSeparatedAttributeNames = sqlProvisioningConfiguration.getMembershipAttributeNames();
      
      StringBuilder sqlInitial = new StringBuilder("select " + commaSeparatedAttributeNames + " from "+membershipTableName);
      List<Object[]> membershipAttributeValues = null;

      String[] colNames = GrouperUtil.splitTrim(commaSeparatedAttributeNames, ",");
      

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

  /**
   * @paProvisioningGrouproup
   */
  public void updateGroup(ProvisioningGroup targetGroup) {
    SqlProvisioningConfiguration sqlProvisioningConfiguration = (SqlProvisioningConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
    
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

        String groupAttributeTableAttributeNameIsGroupMatchingId = sqlProvisioningConfiguration.getgroupAttributeTableAttributeNameIsGroupMatchingId();
        
        // we need to lookup the group
        String groupTargetUuid = new GcDbAccess().connectionName(dbExternalSystemConfigId)
            .sql("select " + groupAttributeTableForeignKeyToGroup + " from " + groupAttributeTableName 
                + " where " + groupAttributeTableAttributeNameColumn + " = ? and " + groupAttributeTableAttributeValueColumn + " = ?")
            .addBindVar(groupAttributeTableAttributeNameIsGroupMatchingId).addBindVar(targetGroup.getId()).select(String.class);

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
    SqlProvisioningConfiguration sqlProvisioningConfiguration = (SqlProvisioningConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
    
    String dbExternalSystemConfigId = sqlProvisioningConfiguration.getDbExternalSystemConfigId();
    
    String groupTableName = sqlProvisioningConfiguration.getGroupTableName();
    String groupAttributeTableName = sqlProvisioningConfiguration.getGroupAttributeTableName();
    
    if (!StringUtils.isBlank(groupTableName)) {
  
      // are there attributes?
      if (!StringUtils.isBlank(groupAttributeTableName)) {
  
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
      } else {
        throw new RuntimeException("Not implemented");
      }
    } else {
      throw new RuntimeException("Need group table name");
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
    
    if (retrieveAll && grouperTargetGroups != null) {
      throw new RuntimeException("Cant retrieve all and pass in groups to retrieve");
    }
    
    SqlProvisioningConfiguration sqlProvisioningConfiguration = (SqlProvisioningConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
    
    String dbExternalSystemConfigId = sqlProvisioningConfiguration.getDbExternalSystemConfigId();
    
    String groupTableName = sqlProvisioningConfiguration.getGroupTableName();
    String groupAttributeTableName = sqlProvisioningConfiguration.getGroupAttributeTableName();
    
    List<ProvisioningGroup> result = new ArrayList<ProvisioningGroup>();
    
    if (!retrieveAll && GrouperUtil.isBlank(grouperTargetGroups)) {
      return result;
    }
    
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
              gcDbAccess.addBindVar(grouperTargetGroup.getMatchingId());
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



      } else {

        // just get from group table
        
        String groupTableIdColumn = sqlProvisioningConfiguration.getGroupTableIdColumn();
        
        String commaSeparatedAttributeNames = sqlProvisioningConfiguration.getGroupAttributeNames();
        String[] colNames = GrouperUtil.splitTrim(commaSeparatedAttributeNames, ",");
        
        StringBuilder sqlInitial = new StringBuilder("select " + commaSeparatedAttributeNames + " from " + groupTableName);
        List<Object[]> groupAttributeValues = null;
        
        
        
        if (retrieveAll) {
          GcDbAccess gcDbAccess = new GcDbAccess().connectionName(dbExternalSystemConfigId);
          
          groupAttributeValues = gcDbAccess.sql(sqlInitial.toString()).selectList(Object[].class);
          
          retrieveGroupsAddRecord(result, groupTableIdColumn, colNames,
              groupAttributeValues);
        } else {

          int numberOfBatches = GrouperUtil.batchNumberOfBatches(grouperTargetGroups.size(), 900);
          for (int i = 0; i < numberOfBatches; i++) {
            List<ProvisioningGroup> currentBatchGrouperTargetGroups = GrouperUtil.batchList(grouperTargetGroups, 900, i);
            StringBuilder sql = new StringBuilder(sqlInitial);
            sql.append(" where ").append(groupTableIdColumn).append(" in (");
            GcDbAccess gcDbAccess = new GcDbAccess().connectionName(dbExternalSystemConfigId);
            
            for (int j=0; j<currentBatchGrouperTargetGroups.size();j++) {
              ProvisioningGroup grouperTargetGroup = currentBatchGrouperTargetGroups.get(j);
              gcDbAccess.addBindVar(grouperTargetGroup.getMatchingId());
              if (j>0) {
                sql.append(",");
              }
              sql.append("?");
            }
            sql.append(" ) ");
            groupAttributeValues = gcDbAccess.sql(sql.toString()).selectList(Object[].class);
            retrieveGroupsAddRecord(result, groupTableIdColumn, colNames,
                groupAttributeValues);
            
          }
        }



        
      }
    }
    
    return result;
   
  }

  public void retrieveGroupsAddRecord(List<ProvisioningGroup> result,
      String groupTableIdColumn, String[] colNames, List<Object[]> groupAttributeValues) {
    for (Object[] groupAttributeValue: GrouperUtil.nonNull(groupAttributeValues)) {
      ProvisioningGroup provisioningGroup = new ProvisioningGroup();
 
      for (int i=0; i<colNames.length; i++) {
        String colName = colNames[i];
 
        Object value = groupAttributeValue[i];
        
        // if there is a group id column, then put that value in the "id" field in the group
        if (!StringUtils.isBlank(groupTableIdColumn) && StringUtils.equalsIgnoreCase(groupTableIdColumn, colName)) {
          provisioningGroup.setId(GrouperUtil.stringValue(value));
        } else {
          provisioningGroup.assignAttributeValue(colName, value);
        }
        
      }
            
      result.add(provisioningGroup);
    }
  }

  public void retrieveGroupsByAttributesAddRecord(List<ProvisioningGroup> result, String groupTableIdColumn,
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
  public TargetDaoDeleteMembershipResponse deleteMembership(TargetDaoDeleteMembershipRequest targetDaoDeleteMembershipRequest) {
    
    ProvisioningMembership targetMembership = targetDaoDeleteMembershipRequest == null ? null : targetDaoDeleteMembershipRequest.getTargetMembership();
    
    SqlProvisioningConfiguration sqlProvisioningConfiguration = (SqlProvisioningConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
    
    String dbExternalSystemConfigId = sqlProvisioningConfiguration.getDbExternalSystemConfigId();
    
    String membershipTableName = sqlProvisioningConfiguration.getMembershipTableName();
    
    String commaSeparatedAttributeNames = sqlProvisioningConfiguration.getMembershipAttributeNames();
    
    
    
    GcDbAccess gcDbAccess = new GcDbAccess().connectionName(dbExternalSystemConfigId);
    
    StringBuilder sql = new StringBuilder("delete from "+membershipTableName + " where ");
    
    List<String> columnNames = GrouperUtil.splitTrimToList(commaSeparatedAttributeNames, ",");
    
    boolean isFirst = true;
    for (String columnName : columnNames) {
      
      if (!isFirst) {
        sql.append(" and ");
      }
      
      sql.append(" " + columnName + " = ? ");
      
      gcDbAccess.addBindVar(targetMembership.getAttributes().get(columnName.toLowerCase()));
      
      isFirst = false;
      
    }
    
    gcDbAccess.sql(sql.toString()).executeSql();
    
    return null;
    
  }

  @Override
  public TargetDaoInsertMembershipResponse insertMembership(TargetDaoInsertMembershipRequest targetDaoInsertMembershipRequest) {
    
    ProvisioningMembership targetMembership = targetDaoInsertMembershipRequest.getTargetMembership();
    
    SqlProvisioningConfiguration sqlProvisioningConfiguration = (SqlProvisioningConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
    
    String dbExternalSystemConfigId = sqlProvisioningConfiguration.getDbExternalSystemConfigId();
    
    String membershipTableName = sqlProvisioningConfiguration.getMembershipTableName();
    
    String commaSeparatedAttributeNames = sqlProvisioningConfiguration.getMembershipAttributeNames();
    
    GcDbAccess gcDbAccess = new GcDbAccess().connectionName(dbExternalSystemConfigId);
    
    StringBuilder sql = new StringBuilder("insert into "+membershipTableName + " ( ");
    
    List<String> columnNames = GrouperUtil.splitTrimToList(commaSeparatedAttributeNames, ",");
    
    boolean isFirst = true;
    for (String columnName : columnNames) {
      
      if (!isFirst) {
        sql.append(" , ");
      }
      
      sql.append(" " + columnName + " ");
      
      String valueToInsert = targetMembership.retrieveAttributeValueString(columnName.toLowerCase());
      
      gcDbAccess.addBindVar(valueToInsert);
      
      isFirst = false;
      
    }
    sql.append(" ) values (");
    sql.append(GrouperClientUtils.appendQuestions(GrouperUtil.length(columnNames)));
    sql.append(")");
    gcDbAccess.sql(sql.toString()).executeSql();
    
    return null;
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
    List<ProvisioningGroup> grouperTargetGroups = targetDaoRetrieveMembershipsBulkRequest == null ? null : targetDaoRetrieveMembershipsBulkRequest.getTargetGroups();
    List<ProvisioningEntity> grouperTargetEntities = targetDaoRetrieveMembershipsBulkRequest == null ? null : targetDaoRetrieveMembershipsBulkRequest.getTargetEntities();
    List<MultiKey> grouperTargetMemberships = targetDaoRetrieveMembershipsBulkRequest == null ? null : targetDaoRetrieveMembershipsBulkRequest.getTargetGroupsEntitiesMemberships();
    List<ProvisioningMembership> targetMemberships = this.retrieveMemberships(false, grouperTargetGroups, grouperTargetEntities, grouperTargetMemberships);
    return new TargetDaoRetrieveMembershipsBulkResponse(targetMemberships);
  }


  /**
   * @paProvisioningGrouproup
   */
  @Override
  public TargetDaoInsertGroupResponse insertGroup(TargetDaoInsertGroupRequest targetDaoInsertGroupRequest) {
    ProvisioningGroup targetGroup = targetDaoInsertGroupRequest.getTargetGroup();
    SqlProvisioningConfiguration sqlProvisioningConfiguration = (SqlProvisioningConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
    
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
  
        String groupAttributeTableAttributeNameIsGroupMatchingId = sqlProvisioningConfiguration.getgroupAttributeTableAttributeNameIsGroupMatchingId();

        GcDbAccess gcDbAccess = new GcDbAccess().connectionName(dbExternalSystemConfigId);
        
        String sql = "insert into " + groupTableName + "(" + groupTableIdColumn + ") values (?)";
        // get from matchingId instead?
        Object groupUuid = targetGroup.retrieveAttributeValue(groupAttributeTableAttributeNameIsGroupMatchingId);
        if (groupUuid == null) {
          throw new RuntimeException("Cant find group matching id from attribute: '" + groupAttributeTableAttributeNameIsGroupMatchingId + "': " + targetGroup);
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
    return null;
  }

  @Override
  public void registerGrouperProvisionerDaoCapabilities(
      GrouperProvisionerDaoCapabilities grouperProvisionerDaoCapabilities) {
    grouperProvisionerDaoCapabilities.setCanDeleteGroup(true);
    grouperProvisionerDaoCapabilities.setCanDeleteMembership(true);
    grouperProvisionerDaoCapabilities.setCanInsertGroup(true);
    grouperProvisionerDaoCapabilities.setCanInsertMembership(true);
    grouperProvisionerDaoCapabilities.setCanRetrieveAllEntities(true);
    grouperProvisionerDaoCapabilities.setCanRetrieveAllGroups(true);
    grouperProvisionerDaoCapabilities.setCanRetrieveAllMemberships(true);
    grouperProvisionerDaoCapabilities.setCanRetrieveGroups(true);
    grouperProvisionerDaoCapabilities.setCanRetrieveMemberships(true);
    grouperProvisionerDaoCapabilities.setCanRetrieveMembershipsBulk(true);
    grouperProvisionerDaoCapabilities.setCanUpdateGroup(true);
    grouperProvisionerDaoCapabilities.setCanUpdateGroupMembershipAttribute(true);
  }

}
