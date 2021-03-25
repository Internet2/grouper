/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.app.ldapProvisioning;

import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfigurationBase;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * an instance of this class focuses on the configuration for ldap sync
 * create an instance, set the key, and call configure
 */
public class LdapSyncConfiguration extends GrouperProvisioningConfigurationBase {

  private String ldapExternalSystemConfigId;
  private String userSearchBaseDn;
  private String groupSearchBaseDn;
  private LdapSyncGroupDnType groupDnType;
  private String folderRdnAttribute;
  private String groupRdnAttribute;
  private Set<String> folderObjectClasses;
  
  @Override
  public void configureSpecificSettings() {

    this.ldapExternalSystemConfigId = this.retrieveConfigString("ldapExternalSystemConfigId", true);
    if (!GrouperLoaderConfig.retrieveConfig().assertPropertyValueRequired("ldap." + ldapExternalSystemConfigId + ".url")) {
      throw new RuntimeException("Unable to find ldap." + ldapExternalSystemConfigId + ".url property.  Is ldapExternalSystemConfigId set correctly?");
    }
            
    this.userSearchBaseDn = this.retrieveConfigString("userSearchBaseDn", false);
    this.groupSearchBaseDn = this.retrieveConfigString("groupSearchBaseDn", false);
    this.groupRdnAttribute = GrouperUtil.defaultIfNull(this.retrieveConfigString("groupRdnAttribute", false), "cn");

    {
      String groupDnTypeString = this.retrieveConfigString("groupDnType", false);
      if (!StringUtils.isBlank(groupDnTypeString)) {
        if (StringUtils.equalsIgnoreCase("flat", groupDnTypeString)) {
          this.groupDnType = LdapSyncGroupDnType.flat;
        } else if (StringUtils.equalsIgnoreCase("bushy", groupDnTypeString)) {
          this.groupDnType = LdapSyncGroupDnType.bushy;
          
          this.folderRdnAttribute = GrouperUtil.defaultIfNull(this.retrieveConfigString("folderRdnAttribute", false), "ou");
          String objectClassesString = GrouperUtil.defaultIfNull(this.retrieveConfigString("folderObjectClasses", false), "top, organizationalUnit");
          this.folderObjectClasses = GrouperUtil.splitTrimToSet(objectClassesString, ",");
        } else {
          throw new RuntimeException("Invalid groupDnType: '" + groupDnTypeString + "'");
        }
      }
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

  
  public String getFolderRdnAttribute() {
    return folderRdnAttribute;
  }

  
  public void setFolderRdnAttribute(String folderRdnAttribute) {
    this.folderRdnAttribute = folderRdnAttribute;
  }

  
  public Set<String> getFolderObjectClasses() {
    return folderObjectClasses;
  }

  
  public void setFolderObjectClasses(Set<String> folderObjectClasses) {
    this.folderObjectClasses = folderObjectClasses;
  }

  
  public String getGroupRdnAttribute() {
    return groupRdnAttribute;
  }

  
  public void setGroupRdnAttribute(String groupRdnAttribute) {
    this.groupRdnAttribute = groupRdnAttribute;
  }
}
