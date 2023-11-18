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
      
      GrouperDuoUser grouperDuoUser = (GrouperDuoUser)targetProvisioningEntity.getProvisioningEntityWrapper().getTargetNativeEntity();
      
      Object[] row = new Object[this.getLoaderEntityColumnNames().size()];
      
      row[0] = this.getGrouperProvisioner().getConfigId();
      row[1] = targetProvisioningEntity.getId();
      row[2] = grouperDuoUser.getAliases();
      row[3] = grouperDuoUser.getPhones();
      row[4] = grouperDuoUser.getPushEnabledDb();
      row[5] = grouperDuoUser.getEmail();
      row[6] = grouperDuoUser.getFirstName();
      row[7] = grouperDuoUser.getLastName();
      row[8] = grouperDuoUser.getEnrolledDb();
      row[9] = grouperDuoUser.getLastDirectorySync();
      row[10] = grouperDuoUser.getNotes();
      row[11] = grouperDuoUser.getRealName();
      row[12] = grouperDuoUser.getStatus();
      row[13] = grouperDuoUser.getUserName();
      row[14] = grouperDuoUser.getCreatedAt();
      row[15] = grouperDuoUser.getLastLogin();
      
      result.add(row);
      
    }
    
    return result;
    
  }
  
}
