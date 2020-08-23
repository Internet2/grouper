package edu.internet2.middleware.grouper.app.sqlProvisioning;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisionerTargetDaoBase;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningGroup;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;


public class SqlProvisioningDao extends GrouperProvisionerTargetDaoBase {
  
  @Override
  public Map<String, ProvisioningGroup> retrieveAllGroups() {
    
    SqlProvisioningConfiguration sqlProvisioningConfiguration = (SqlProvisioningConfiguration) this.getGrouperProvisioner().retrieveProvisioningConfiguration();
    
    String dbExternalSystemConfigId = sqlProvisioningConfiguration.getDbExternalSystemConfigId();
    
    String membershipTableName = sqlProvisioningConfiguration.getMembershipTableName();
    String membershipGroupColumn = sqlProvisioningConfiguration.getMembershipGroupColumn();
    
    
    GcDbAccess gcDbAccess = new GcDbAccess().connectionName(dbExternalSystemConfigId);
    
    List<String> groupNames = gcDbAccess.sql("select distinct " + membershipGroupColumn + " from "+membershipTableName).selectList(String.class);
    
    Map<String, ProvisioningGroup> result = new HashMap<String, ProvisioningGroup>();
    
    for (String groupName: GrouperUtil.nonNull(groupNames)) {
      ProvisioningGroup targetGroup = new ProvisioningGroup();
      targetGroup.setName(groupName);
      targetGroup.setId(groupName);
      
      result.put(groupName, targetGroup);
    }
    
    return result;
   
  }

}
