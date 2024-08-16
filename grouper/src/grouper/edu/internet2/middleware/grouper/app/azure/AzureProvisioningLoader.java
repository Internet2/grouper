package edu.internet2.middleware.grouper.app.azure;

import java.util.ArrayList;
import java.util.List;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningLoader;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningEntity;
import edu.internet2.middleware.grouper.util.GrouperUtil;

public class AzureProvisioningLoader extends GrouperProvisioningLoader {
	
 @Override
  public String getLoaderEntityTableName() {
    return "grouper_prov_azure_user";
  }
  

  public List<String> getLoaderEntityColumnNames() {
    return GrouperUtil.toList("config_id", "account_enabled", "display_name", "id",
         "mail_nickname", "on_premises_immutable_id", "user_principal_name");
  }
  
  @Override
  public List<String> getLoaderEntityKeyColumnNames() {
    return GrouperUtil.toList("config_id", "id");
  }
  
  
  @Override
  public List<Object[]> retrieveLoaderEntityTableDataFromDataBean() {
    
    List<ProvisioningEntity> targetProvisioningEntities = this.getGrouperProvisioner().retrieveGrouperProvisioningData().retrieveTargetProvisioningEntities();
    
    List<Object[]> result = new ArrayList<>();
    
    for (ProvisioningEntity targetProvisioningEntity: targetProvisioningEntities) {
      
      GrouperAzureUser grouperAzureUser = (GrouperAzureUser)targetProvisioningEntity.getProvisioningEntityWrapper().getTargetNativeEntity();
      
      Object[] row = new Object[this.getLoaderEntityColumnNames().size()];
      
      row[0] = this.getGrouperProvisioner().getConfigId();
      row[1] = grouperAzureUser.getAccountEnabledDb();
      row[2] = grouperAzureUser.getDisplayName();
      row[3] = grouperAzureUser.getId();
      row[4] = grouperAzureUser.getMailNickname();
      row[5] = grouperAzureUser.getOnPremisesImmutableId();
      row[6] = grouperAzureUser.getUserPrincipalName();
      
      result.add(row);
      
    }
    
    return result;
    
  }

}
