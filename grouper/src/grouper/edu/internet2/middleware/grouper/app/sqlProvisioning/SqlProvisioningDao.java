package edu.internet2.middleware.grouper.app.sqlProvisioning;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisionerTargetDaoBase;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningGroup;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningMembership;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;


public class SqlProvisioningDao extends GrouperProvisionerTargetDaoBase {
  
  @Override
  public List<ProvisioningMembership> retrieveAllMemberships() {
    
    SqlProvisioningConfiguration sqlProvisioningConfiguration = (SqlProvisioningConfiguration) this.getGrouperProvisioner().retrieveProvisioningConfiguration();
    
    String dbExternalSystemConfigId = sqlProvisioningConfiguration.getDbExternalSystemConfigId();
    
    String membershipTableName = sqlProvisioningConfiguration.getMembershipTableName();
    
    List<ProvisioningMembership> result = new ArrayList<ProvisioningMembership>();
    
    if (!StringUtils.isBlank(membershipTableName)) {

      String commaSeparatedAttributeNames = sqlProvisioningConfiguration.getMembershipAttributeNames();
      
      GcDbAccess gcDbAccess = new GcDbAccess().connectionName(dbExternalSystemConfigId);
      
      List<Object[]> membershipAttributeValues = gcDbAccess.sql("select " + commaSeparatedAttributeNames + " from "+membershipTableName).selectList(Object[].class);
      
      String[] colNames = GrouperUtil.splitTrim(commaSeparatedAttributeNames, ",");
      
      for (Object[] membershipAttributeValue: GrouperUtil.nonNull(membershipAttributeValues)) {
        ProvisioningMembership provisioningMembership = new ProvisioningMembership();
              
        for (int i=0; i<colNames.length; i++) {
          String colName = colNames[i];
          
          Object value = membershipAttributeValue[i];
          
          provisioningMembership.assignAttribute(colName, value);
        }
              
        result.add(provisioningMembership);
      }

    }
    
    return result;
   
  }

  @Override
  public List<ProvisioningGroup> retrieveAllGroups() {
    
    SqlProvisioningConfiguration sqlProvisioningConfiguration = (SqlProvisioningConfiguration) this.getGrouperProvisioner().retrieveProvisioningConfiguration();
    
    String dbExternalSystemConfigId = sqlProvisioningConfiguration.getDbExternalSystemConfigId();
    
    String groupTableName = sqlProvisioningConfiguration.getGroupTableName();
    String groupAttributeTableName = sqlProvisioningConfiguration.getGroupAttributeTableName();
    
    List<ProvisioningGroup> result = new ArrayList<ProvisioningGroup>();
    
    if (!StringUtils.isBlank(groupTableName)) {

      // are there attributes?
      if (!StringUtils.isBlank(groupAttributeTableName)) {

        // join group to attribute table
        
        String groupTableIdColumn = sqlProvisioningConfiguration.getGroupTableIdColumn();
        String groupAttributeTableIdColumn = sqlProvisioningConfiguration.getGroupAttributeTableIdColumn();
        
        String groupAttributeTableAttributeNameColumn = sqlProvisioningConfiguration.getGroupAttributeTableAttributeNameColumn();
        String groupAttributeTableAttributeValueColumn = sqlProvisioningConfiguration.getGroupAttributeTableAttributeValueColumn();
        
        String groupAttributeTableForeignKeyToGroup = sqlProvisioningConfiguration.getGroupAttributeTableForeignKeyToGroup();
        
        String commaSeparatedGroupColumnNames = sqlProvisioningConfiguration.getGroupAttributeNames();
        String[] groupColumnNamesArray = GrouperUtil.splitTrim(commaSeparatedGroupColumnNames, ",");

        String commaSeparatedGroupAttributeColumnNames = groupAttributeTableAttributeNameColumn + ", " + groupAttributeTableAttributeValueColumn;
        String[] groupAttributeColumnNamesArray = GrouperUtil.splitTrim(commaSeparatedGroupAttributeColumnNames, ",");

        GcDbAccess gcDbAccess = new GcDbAccess().connectionName(dbExternalSystemConfigId);
        
        StringBuilder sql = new StringBuilder("select ");
        
        for (int i=0;i<groupColumnNamesArray.length; i++) {
          if (i>0) {
            sql.append(", ");
          }
          sql.append("g.").append(groupColumnNamesArray[i]);
        }
        
        for (int i=0;i<groupAttributeColumnNamesArray.length; i++) {
          sql.append(", ");
          sql.append("a.").append(groupAttributeColumnNamesArray[i]);
        }
        
        sql.append(" from ").append(groupTableName).append(" as g left outer join ").append(groupAttributeTableName).append(" as a on g.")
          .append(groupTableIdColumn).append(" = a.").append(groupAttributeTableForeignKeyToGroup);
        
        List<Object[]> groupsAndAttributeValues = gcDbAccess.sql(sql.toString()).selectList(Object[].class);

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
          }

          int columnIndex = 0;
          for (int i=0;i<groupColumnNamesArray.length; i++) {

            String colName = groupColumnNamesArray[i];

            Object value = groupsAndAttributeValue[columnIndex];
            
            if (StringUtils.equalsIgnoreCase(groupTableIdColumn, colName)) {
              provisioningGroup.setId(GrouperUtil.stringValue(value));
            } else {
              provisioningGroup.assignAttribute(colName, value);
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
          result.add(provisioningGroup);
                
        }

      } else {

        // just get from group table
        
        String groupTableIdColumn = sqlProvisioningConfiguration.getGroupTableIdColumn();
        
        String commaSeparatedAttributeNames = sqlProvisioningConfiguration.getGroupAttributeNames();
        
        GcDbAccess gcDbAccess = new GcDbAccess().connectionName(dbExternalSystemConfigId);
        
        List<Object[]> groupAttributeValues = gcDbAccess.sql("select " + commaSeparatedAttributeNames + " from " + groupTableName).selectList(Object[].class);
        
        String[] colNames = GrouperUtil.splitTrim(commaSeparatedAttributeNames, ",");
        
        for (Object[] groupAttributeValue: GrouperUtil.nonNull(groupAttributeValues)) {
          ProvisioningGroup provisioningGroup = new ProvisioningGroup();
  
          for (int i=0; i<colNames.length; i++) {
            String colName = colNames[i];
  
            Object value = groupAttributeValue[i];
            
            // if there is a group id column, then put that value in the "id" field in the group
            if (!StringUtils.isBlank(groupTableIdColumn) && StringUtils.equalsIgnoreCase(groupTableIdColumn, colName)) {
              provisioningGroup.setId(GrouperUtil.stringValue(value));
            } else {
              provisioningGroup.assignAttribute(colName, value);
            }
            
          }
                
          result.add(provisioningGroup);
        }
      }
    }
    
    return result;
   
  }

  @Override
  public void deleteMembership(ProvisioningMembership targetMembership) {
    SqlProvisioningConfiguration sqlProvisioningConfiguration = (SqlProvisioningConfiguration) this.getGrouperProvisioner().retrieveProvisioningConfiguration();
    
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
    
  }

  @Override
  public void insertMembership(ProvisioningMembership targetMembership) {
    SqlProvisioningConfiguration sqlProvisioningConfiguration = (SqlProvisioningConfiguration) this.getGrouperProvisioner().retrieveProvisioningConfiguration();
    
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
    
  }

  
}
