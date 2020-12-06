/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.app.ldapProvisioning;

import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfigurationBase;

/**
 * an instance of this class focuses on the configuration for ldap sync
 * create an instance, set the key, and call configure
 */
public class LdapSyncConfiguration extends GrouperProvisioningConfigurationBase {

  private String ldapExternalSystemConfigId;
  private String userSearchBaseDn;
  private String groupSearchBaseDn;
  private LdapSyncGroupDnType groupDnType; // TODO remove?
  
  @Override
  public void configureSpecificSettings() {

    this.ldapExternalSystemConfigId = this.retrieveConfigString("ldapExternalSystemConfigId", true);
    if (!GrouperLoaderConfig.retrieveConfig().assertPropertyValueRequired("ldap." + ldapExternalSystemConfigId + ".url")) {
      throw new RuntimeException("Unable to find ldap." + ldapExternalSystemConfigId + ".url property.  Is ldapExternalSystemConfigId set correctly?");
    }
            
    this.userSearchBaseDn = this.retrieveConfigString("userSearchBaseDn", false);
    this.groupSearchBaseDn = this.retrieveConfigString("groupSearchBaseDn", false);

    {
      /*
      String groupDnTypeString = this.retrieveConfigString("groupDnType", true);
      if (StringUtils.equalsIgnoreCase("flat", groupDnTypeString)) {
        this.groupDnType = LdapSyncGroupDnType.flat;
      } else if (StringUtils.equalsIgnoreCase("bushy", groupDnTypeString)) {
        this.groupDnType = LdapSyncGroupDnType.bushy;
      } else {
        throw new RuntimeException("Invalid groupDnType: '" + groupDnTypeString + "'");
      }
      */
    }
  }

  
  public String getLdapExternalSystemConfigId() {
    return ldapExternalSystemConfigId;
  }

  
  public void setLdapExternalSystemConfigId(String ldapExternalSystemConfigId) {
    this.ldapExternalSystemConfigId = ldapExternalSystemConfigId;
  }

  
  public String getUserSearchBaseDn() {
    return userSearchBaseDn;
  }

  
  public void setUserSearchBaseDn(String userSearchBaseDn) {
    this.userSearchBaseDn = userSearchBaseDn;
  }
  
  public String getGroupSearchBaseDn() {
    return groupSearchBaseDn;
  }

  
  public void setGroupSearchBaseDn(String groupSearchBaseDn) {
    this.groupSearchBaseDn = groupSearchBaseDn;
  }
  
  public LdapSyncGroupDnType getGroupDnType() {
    return groupDnType;
  }

  
  public void setGroupDnType(LdapSyncGroupDnType groupDnType) {
    this.groupDnType = groupDnType;
  }
}
