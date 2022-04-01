/**
 * 
 */
package edu.internet2.middleware.grouper.app.duo;

import java.util.ArrayList;
import java.util.List;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningLoader;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningEntity;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 *
 */
public class DuoProvisioningLoader extends GrouperProvisioningLoader {

  @Override
  public String getLoaderEntityTableName() {
    return "grouper_prov_duo_user";
  }

  
  public List<String> getLoaderEntityColumnNames() {
    return GrouperUtil.toList("config_id", "user_id", "aliases", "phones", "is_push_enabled", "email", "first_name", "last_name",
         "is_enrolled", "last_directory_sync", "notes", "real_name", "status", "user_name", "created_at", "last_login_time");
  }
  
  public List<String> getLoaderEntityKeyColumnNames() {
    return GrouperUtil.toList("user_id");
  }
  
  public List<Object[]> retrieveLoaderEntityTableDataFromDataBean() {
    
    List<ProvisioningEntity> targetProvisioningEntities = this.getGrouperProvisioner().retrieveGrouperProvisioningData().retrieveTargetProvisioningEntities();
    
    List<Object[]> result = new ArrayList<>();
    
    for (ProvisioningEntity targetProvisioningEntity: targetProvisioningEntities) {
      
      Object[] row = new Object[this.getLoaderEntityColumnNames().size()];
      
      row[0] = this.getGrouperProvisioner().getConfigId();
      row[1] = targetProvisioningEntity.getId();
      row[2] = targetProvisioningEntity.retrieveAttributeValueString("aliases");
      row[3] = targetProvisioningEntity.retrieveAttributeValueString("phones");
      row[4] = targetProvisioningEntity.retrieveAttributeValueString("isPushEnabled");
      row[5] = targetProvisioningEntity.getEmail();
      row[6] = targetProvisioningEntity.retrieveAttributeValueString("firstName");
      row[7] = targetProvisioningEntity.retrieveAttributeValueString("lastName");
      row[8] = targetProvisioningEntity.retrieveAttributeValueString("isEnrolled");
      row[9] = targetProvisioningEntity.retrieveAttributeValueLong("lastDirectorySync");
      row[10] = targetProvisioningEntity.retrieveAttributeValueString("notes");
      row[11] = targetProvisioningEntity.getName();
      row[12] = targetProvisioningEntity.retrieveAttributeValueString("status");
      row[13] = targetProvisioningEntity.retrieveAttributeValueString("userName");
      row[14] = targetProvisioningEntity.retrieveAttributeValueLong("createdAt");
      row[15] = targetProvisioningEntity.retrieveAttributeValueLong("lastLogin");
      
      result.add(row);
      
    }
    
    return result;
    
  }
  
}
