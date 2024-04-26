package edu.internet2.middleware.grouper.app.scim2Provisioning;

import java.util.Collection;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfigurationValidation;
import edu.internet2.middleware.grouper.util.GrouperUtil;


public class Scim2SyncConfigurationValidation extends GrouperProvisioningConfigurationValidation {

  @Override
  public void validateFromObjectModel() {
    super.validateFromObjectModel();
    
    String scimType = this.getSuffixToConfigValue().get("scimType");
    if (StringUtils.equals("Github", scimType)) {
    }
    
    /**
     * # Select memberships
      # {valueType: "string", readOnly: true, order: 1500, subSection: "membership", showEl: "${operateOnGrouperMemberships}"}
      # provisioner.myScimProvisioner.selectMemberships = false
      
      # Replace memberships
      # {valueType: "boolean", order: 2491, defaultValue: "false", subSection: "membership", showEl: "${operateOnGrouperMemberships}"}
      # provisioner.myScimProvisioner.replaceMemberships = 
      
      # Update groups
      # {valueType: "string", readOnly: true, order: 11500, subSection: "group", showEl: "${operateOnGrouperGroups}"}
      # provisioner.myScimProvisioner.updateGroups = false
     */
    
    //update cannot be true 
    // if they don't edit it to make it true, then provisioner 
    
  }
  
  @Override
  public Collection<String> validateGroupAttributeNamesAllowed() {
    return null;
  }

  @Override
  public Collection<String> validateGroupAttributeNamesRequired() {
    int numberOfAttributes = GrouperUtil.intValue(this.getSuffixToConfigValue().get("numberOfGroupAttributes"), 0);
    if (numberOfAttributes > 0) {

      String scimType = this.getSuffixToConfigValue().get("scimType");
      if (StringUtils.equals("Github", scimType)) {
        return null;
      } else if (StringUtils.equals("AWS", scimType)) {
        return GrouperUtil.toSet("displayName", "id");
      } 
    }
    return null;
  }

  @Override
  public boolean validateGroupAttributesRequireString() {
    return true;
  }

  @Override
  public Collection<String> validateEntityAttributeNamesAllowed() {
    String scimType = this.getSuffixToConfigValue().get("scimType");
    if (StringUtils.equals("Github", scimType)) {
      return null;
    } else if (StringUtils.equals("AWS", scimType)) {
      return GrouperUtil.toSet("active", "externalId", "formattedName", "middleName", "emailValue", "emailType", "userType", "employeeNumber", "costCenter");
    } 
    return null;
  }

  @Override
  public Collection<String> validateEntityAttributeNamesRequired() {
    int numberOfAttributes = GrouperUtil.intValue(this.getSuffixToConfigValue().get("numberOfEntityAttributes"), 0);
    if (numberOfAttributes > 0) {

      String scimType = this.getSuffixToConfigValue().get("scimType");
      if (StringUtils.equals("Github", scimType)) {
        return GrouperUtil.toSet("id", "userName", "familyName", "givenName");
      } else if (StringUtils.equals("AWS", scimType)) {
        return GrouperUtil.toSet("id", "userName", "displayName", "familyName", "givenName");
      } 
    }
    return null;
  }

  @Override
  public boolean validateEntityAttributesRequireString() {
    return true;
  }

  
}
