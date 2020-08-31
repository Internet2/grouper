package edu.internet2.middleware.grouper.app.sqlProvisioning;

import java.util.ArrayList;
import java.util.List;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisionerTargetDaoBase;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningAttribute;
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
    
    String commaSeparatedAttributeNames = sqlProvisioningConfiguration.getMembershipAttributeNames();
    
    
    
    GcDbAccess gcDbAccess = new GcDbAccess().connectionName(dbExternalSystemConfigId);
    
    List<Object[]> membershipAttributeValues = gcDbAccess.sql("select " + commaSeparatedAttributeNames + " from "+membershipTableName).selectList(Object[].class);
    
    List<ProvisioningMembership> result = new ArrayList<ProvisioningMembership>();
    
    String[] colNames = GrouperUtil.splitTrim(commaSeparatedAttributeNames, ",");
    
    for (Object[] membershipAttributeValue: GrouperUtil.nonNull(membershipAttributeValues)) {
      ProvisioningMembership targetMembership = new ProvisioningMembership();
            
      for (int i=0; i<colNames.length; i++) {
        String colName = colNames[i];
        
        Object value = membershipAttributeValue[i];
        
        targetMembership.assignAttribute(colName, value);
      }
            
      result.add(targetMembership);
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
