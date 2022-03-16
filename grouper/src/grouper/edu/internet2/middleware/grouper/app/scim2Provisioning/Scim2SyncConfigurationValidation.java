package edu.internet2.middleware.grouper.app.scim2Provisioning;

import java.util.Collection;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfigurationValidation;
import edu.internet2.middleware.grouper.cfg.text.GrouperTextContainer;
import edu.internet2.middleware.grouper.util.GrouperUtil;


public class Scim2SyncConfigurationValidation extends GrouperProvisioningConfigurationValidation {

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
      return GrouperUtil.toSet("externalId", "formattedName", "middleName", "emailValue", "emailType", "userType", "employeeNumber", "costCenter");
    } 
    return null;
  }

  @Override
  public Collection<String> validateEntityAttributeNamesRequired() {
    int numberOfAttributes = GrouperUtil.intValue(this.getSuffixToConfigValue().get("numberOfEntityAttributes"), 0);
    if (numberOfAttributes > 0) {

      String scimType = this.getSuffixToConfigValue().get("scimType");
      if (StringUtils.equals("Github", scimType)) {
        return null;
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
