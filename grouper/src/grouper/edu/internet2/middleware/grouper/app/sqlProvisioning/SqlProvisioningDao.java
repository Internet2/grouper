package edu.internet2.middleware.grouper.app.sqlProvisioning;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisionerTargetDaoBase;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningAttribute;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningMembership;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;


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
      
      Map<String, ProvisioningAttribute> attributes = new HashMap<String, ProvisioningAttribute>();
      
      for (int i=0; i<colNames.length; i++) {
        String colName = colNames[i];
        
        Object value = membershipAttributeValue[i];
        ProvisioningAttribute provisioningAttribute = new ProvisioningAttribute();
        provisioningAttribute.setName(colName);
        provisioningAttribute.setValue(value);
        
        attributes.put(colName, provisioningAttribute);
      }
      
      targetMembership.setAttributes(attributes);
      
      result.add(targetMembership);
    }
    
    return result;
   
  }

  @Override
  protected void sendChangesToTarget() {
    // TODO Auto-generated method stub
    
  }

}
