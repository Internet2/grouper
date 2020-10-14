/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.app.ldapProvisioning;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfigurationBase;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * an instance of this class focuses on the configuration for ldap sync
 * create an instance, set the key, and call configure
 */
public class LdapSyncConfiguration extends GrouperProvisioningConfigurationBase {

  private String ldapExternalSystemConfigId;
  private LdapSyncProvisioningType ldapProvisioningType;
  private String userSearchBaseDn;
  private String userSearchFilter;
  private String userSearchAllFilter;
  
  private String groupSearchBaseDn;
  private String groupSearchFilter;
  private String groupSearchAllFilter;
  private LdapSyncGroupDnType groupDnType; // TODO remove?
  
  @Override
  public void configureSpecificSettings() {

    this.ldapExternalSystemConfigId = this.retrieveConfigString("ldapExternalSystemConfigId", true);
    if (!GrouperLoaderConfig.retrieveConfig().assertPropertyValueRequired("ldap." + ldapExternalSystemConfigId + ".url")) {
      throw new RuntimeException("Unable to find ldap." + ldapExternalSystemConfigId + ".url property.  Is ldapExternalSystemConfigId set correctly?");
    }
        
    {
      String ldapProvisioningTypeString = this.retrieveConfigString("ldapProvisioningType", true);
      if (StringUtils.equalsIgnoreCase("groupMemberships", ldapProvisioningTypeString)) {
        this.ldapProvisioningType = LdapSyncProvisioningType.groupMemberships;
      } else if (StringUtils.equalsIgnoreCase("userAttributes", ldapProvisioningTypeString)) {
        this.ldapProvisioningType = LdapSyncProvisioningType.userAttributes;
      } else {
        throw new RuntimeException("Invalid ldapProvisioningType: '" + ldapProvisioningTypeString + "'");
      }
    }
    
    this.userSearchBaseDn = this.retrieveConfigString("userSearchBaseDn", false);
    this.userSearchFilter = this.retrieveConfigString("userSearchFilter", false);
    this.userSearchAllFilter = this.retrieveConfigString("userSearchAllFilter", false);
    
    this.groupSearchBaseDn = this.retrieveConfigString("groupSearchBaseDn", false);
    this.groupSearchFilter = this.retrieveConfigString("groupSearchFilter", false);
    this.groupSearchAllFilter = GrouperUtil.defaultIfNull(this.retrieveConfigString("groupSearchAllFilter", false), "(&(objectclass=group)(gidNumber=*))");

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

  
  public LdapSyncProvisioningType getLdapProvisioningType() {
    return ldapProvisioningType;
  }

  
  public void setLdapProvisioningType(LdapSyncProvisioningType ldapProvisioningType) {
    this.ldapProvisioningType = ldapProvisioningType;
  }
  
  public String getUserSearchBaseDn() {
    return userSearchBaseDn;
  }

  
  public void setUserSearchBaseDn(String userSearchBaseDn) {
    this.userSearchBaseDn = userSearchBaseDn;
  }

  
  public String getUserSearchFilter() {
    return userSearchFilter;
  }

  
  public void setUserSearchFilter(String userSearchFilter) {
    this.userSearchFilter = userSearchFilter;
  }

  
  public String getUserSearchAllFilter() {
    return userSearchAllFilter;
  }

  
  public void setUserSearchAllFilter(String userSearchAllFilter) {
    this.userSearchAllFilter = userSearchAllFilter;
  }
  
  public String getGroupSearchBaseDn() {
    return groupSearchBaseDn;
  }

  
  public void setGroupSearchBaseDn(String groupSearchBaseDn) {
    this.groupSearchBaseDn = groupSearchBaseDn;
  }

  
  public String getGroupSearchFilter() {
    return groupSearchFilter;
  }

  
  public void setGroupSearchFilter(String groupSearchFilter) {
    this.groupSearchFilter = groupSearchFilter;
  }

  
  public String getGroupSearchAllFilter() {
    return groupSearchAllFilter;
  }

  
  public void setGroupSearchAllFilter(String groupSearchAllFilter) {
    this.groupSearchAllFilter = groupSearchAllFilter;
  }

  
  public LdapSyncGroupDnType getGroupDnType() {
    return groupDnType;
  }

  
  public void setGroupDnType(LdapSyncGroupDnType groupDnType) {
    this.groupDnType = groupDnType;
  }
}
