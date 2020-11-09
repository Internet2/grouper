package edu.internet2.middleware.grouperBox;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfigurationBase;

public class GrouperBoxConfiguration extends GrouperProvisioningConfigurationBase {

  @Override
  public void configureSpecificSettings() {

    //this.ldapExternalSystemConfigId = this.retrieveConfigString("ldapExternalSystemConfigId", true);
    //if (!GrouperLoaderConfig.retrieveConfig().assertPropertyValueRequired("ldap." + ldapExternalSystemConfigId + ".url")) {
    //  throw new RuntimeException("Unable to find ldap." + ldapExternalSystemConfigId + ".url property.  Is ldapExternalSystemConfigId set correctly?");
    //}
  }
}
