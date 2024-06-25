package edu.internet2.middleware.grouper.app.scim2Provisioning;

import java.util.ArrayList;
import java.util.List;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningLoader;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningEntity;
import edu.internet2.middleware.grouper.util.GrouperUtil;

public class ScimProvisioningLoader extends GrouperProvisioningLoader {
  
  @Override
  public String getLoaderEntityTableName() {
    return "grouper_prov_scim_user";
  }
  
  @Override
  public String getLoaderEntityAttributesTableName() {
    return "grouper_prov_scim_user_attr";
  }

  @Override
  public List<String> getLoaderEntityColumnNames() {
    return GrouperUtil.toList("config_id", "active", "cost_center", "department", "display_name", "division", "email_type", "email_value",
         "email_type2", "email_value2", "employee_number", "external_id", "family_name", "formatted_name", "given_name", "id",
         "middle_name", "phone_number", "phone_number_type", "phone_number2", "phone_number_type2", "the_schemas", "title", "user_name", "user_type");
  }
  
  @Override
  public List<String> getLoaderEntityAttributesColumnNames() {
    return GrouperUtil.toList("config_id", "id", "attribute_name", "attribute_value");
  }
  
  @Override
  public List<String> getLoaderEntityKeyColumnNames() {
    return GrouperUtil.toList("config_id", "id");
  }
  
  @Override
  public List<String> getLoaderEntityAttributesKeyColumnNames() {
    return GrouperUtil.toList("config_id", "id", "attribute_name", "attribute_value");
  }
  
  @Override
  public List<Object[]> retrieveLoaderEntityAttrTableDataFromDataBean() {
    
    List<ProvisioningEntity> targetProvisioningEntities = this.getGrouperProvisioner().retrieveGrouperProvisioningData().retrieveTargetProvisioningEntities();
    
    List<Object[]> result = new ArrayList<>();
    
    for (ProvisioningEntity targetProvisioningEntity: targetProvisioningEntities) {
      
      GrouperScim2User grouperScimUser = (GrouperScim2User)targetProvisioningEntity.getProvisioningEntityWrapper().getTargetNativeEntity();
      
      if (grouperScimUser.getCustomAttributes() == null) {
        continue;
      }
      
      for (String attributeName: grouperScimUser.getCustomAttributes().keySet()) {
          
        Object object = grouperScimUser.getCustomAttributes().get(attributeName);
        if (GrouperUtil.isBlank(object)) {
          continue;
        }
        
        Object[] row = new Object[this.getLoaderEntityAttributesColumnNames().size()];
        
        row[0] = this.getGrouperProvisioner().getConfigId();
        row[1] = grouperScimUser.getId();
        row[2] = attributeName;
        row[3] = GrouperUtil.stringValue(object);
        result.add(row);
      }
    }
    
    return result;
    
  }
  
  public List<Object[]> retrieveLoaderEntityTableDataFromDataBean() {
    
    List<ProvisioningEntity> targetProvisioningEntities = this.getGrouperProvisioner().retrieveGrouperProvisioningData().retrieveTargetProvisioningEntities();
    
    List<Object[]> result = new ArrayList<>();
    
    for (ProvisioningEntity targetProvisioningEntity: targetProvisioningEntities) {
      
      GrouperScim2User grouperScimUser = (GrouperScim2User)targetProvisioningEntity.getProvisioningEntityWrapper().getTargetNativeEntity();
      
      Object[] row = new Object[this.getLoaderEntityColumnNames().size()];
      
      row[0] = this.getGrouperProvisioner().getConfigId();
      row[1] = grouperScimUser.getActiveDb();
      row[2] = grouperScimUser.getCostCenter();
      row[3] = grouperScimUser.getDepartment();
      row[4] = grouperScimUser.getDisplayName();
      row[5] = grouperScimUser.getDivision();
      row[6] = grouperScimUser.getEmailType();
      row[7] = grouperScimUser.getEmailValue();
      row[8] = grouperScimUser.getEmailType2();
      row[9] = grouperScimUser.getEmailValue2();
      row[10] = grouperScimUser.getEmployeeNumber();
      row[11] = grouperScimUser.getExternalId();
      row[12] = grouperScimUser.getFamilyName();
      row[13] = grouperScimUser.getFormattedName();
      row[14] = grouperScimUser.getGivenName();
      row[15] = grouperScimUser.getId();
      row[16] = grouperScimUser.getMiddleName();
      row[17] = grouperScimUser.getPhoneNumber();
      row[18] = grouperScimUser.getPhoneNumberType();
      row[19] = grouperScimUser.getPhoneNumber2();
      row[20] = grouperScimUser.getPhoneNumberType2();
      row[21] = grouperScimUser.getSchemas();
      row[22] = grouperScimUser.getTitle();
      row[23] = grouperScimUser.getUserName();
      row[24] = grouperScimUser.getUserType();      
      
      result.add(row);
      
    }
    
    return result;
    
  }

}
