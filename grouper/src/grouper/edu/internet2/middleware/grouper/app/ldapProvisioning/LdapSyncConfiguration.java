/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.app.ldapProvisioning;

import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfiguration;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfigurationAttribute;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * an instance of this class focuses on the configuration for ldap sync
 * create an instance, set the key, and call configure
 */
public class LdapSyncConfiguration extends GrouperProvisioningConfiguration {

  private String ldapExternalSystemConfigId;
  private String userSearchBaseDn;
  private String groupSearchBaseDn;
  private LdapSyncGroupDnType groupDnType;
  private String folderRdnAttribute;
  private String groupRdnAttribute;
  private Set<String> folderObjectClasses;
  private String userRdnAttribute;
  
  public String getUserRdnAttribute() {
    return userRdnAttribute;
  }

  public void setUserRdnAttribute(String userRdnAttribute) {
    this.userRdnAttribute = userRdnAttribute;
  }

  @Override
  public void configureSpecificSettings() {

    this.ldapExternalSystemConfigId = this.retrieveConfigString("ldapExternalSystemConfigId", true);
    if (!GrouperLoaderConfig.retrieveConfig().assertPropertyValueRequired("ldap." + ldapExternalSystemConfigId + ".url")) {
      throw new RuntimeException("Unable to find ldap." + ldapExternalSystemConfigId + ".url property.  Is ldapExternalSystemConfigId set correctly?");
    }
            
    this.userSearchBaseDn = this.retrieveConfigString("userSearchBaseDn", false);
    this.groupSearchBaseDn = this.retrieveConfigString("groupSearchBaseDn", false);
    this.groupRdnAttribute = GrouperUtil.defaultIfNull(this.retrieveConfigString("groupRdnAttribute", false), "cn");
    this.userRdnAttribute = this.retrieveConfigString("userRdnAttribute", false);

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
    this.allowLdapGroupDnOverride = GrouperUtil.booleanValue(this.retrieveConfigString("allowLdapGroupDnOverride", false), false);
    
    {
      GrouperProvisioningConfigurationAttribute groupDnAttributeConfig = this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getTargetGroupAttributeNameToConfig().get(LdapProvisioningTargetDao.ldap_dn);
      GrouperProvisioningConfigurationAttribute groupRdnAttributeConfig = this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getTargetGroupAttributeNameToConfig().get(this.groupRdnAttribute);
      if (groupRdnAttributeConfig != null && groupDnAttributeConfig != null
          && StringUtils.isBlank(groupDnAttributeConfig.getTranslateFromStaticValues())
          && StringUtils.isBlank(groupDnAttributeConfig.getTranslateFromStaticValuesCreateOnly())
          && StringUtils.isBlank(groupDnAttributeConfig.getTranslateExpression())
          && StringUtils.isBlank(groupDnAttributeConfig.getTranslateExpressionCreateOnly())
          && StringUtils.isBlank(groupDnAttributeConfig.getTranslateFromGrouperProvisioningGroupField())
          && StringUtils.isBlank(groupDnAttributeConfig.getTranslateFromGrouperProvisioningGroupFieldCreateOnly())) {
        for (boolean createOnly : new boolean[] {false, true}) {
          String groupAttribute = null;
          String rdnTranslateFromGrouperProvisioningGroupField = createOnly ?  groupRdnAttributeConfig.getTranslateFromGrouperProvisioningGroupFieldCreateOnly() 
              : groupRdnAttributeConfig.getTranslateFromGrouperProvisioningGroupField();
          if (StringUtils.isBlank(rdnTranslateFromGrouperProvisioningGroupField)) {
            continue;
          }
          if (StringUtils.equals("name", rdnTranslateFromGrouperProvisioningGroupField)) {
            groupAttribute = "name";
          }
          if (this.getGroupDnType() == LdapSyncGroupDnType.bushy && StringUtils.equals("extension", rdnTranslateFromGrouperProvisioningGroupField)) {
            groupAttribute = "name";
          }
          if (this.getGroupDnType() == LdapSyncGroupDnType.flat && StringUtils.equals("extension", rdnTranslateFromGrouperProvisioningGroupField)) {
            groupAttribute = "extension";
          }
  
          if (!StringUtils.isBlank(groupAttribute)) {
            if (createOnly) {
              groupDnAttributeConfig.setTranslateFromGrouperProvisioningGroupFieldCreateOnly(groupAttribute);
            } else {
              groupDnAttributeConfig.setTranslateFromGrouperProvisioningGroupField(groupAttribute);
            }
          }
        }
      }
    }
    
    if (!StringUtils.isBlank(this.userRdnAttribute)) {
      GrouperProvisioningConfigurationAttribute userDnAttributeConfig = this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getTargetEntityAttributeNameToConfig().get(LdapProvisioningTargetDao.ldap_dn);
      GrouperProvisioningConfigurationAttribute userRdnAttributeConfig = this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getTargetEntityAttributeNameToConfig().get(this.userRdnAttribute);
      if (userRdnAttributeConfig != null && userDnAttributeConfig != null
          && StringUtils.isBlank(userDnAttributeConfig.getTranslateFromStaticValues())
          && StringUtils.isBlank(userDnAttributeConfig.getTranslateFromStaticValuesCreateOnly())
          && StringUtils.isBlank(userDnAttributeConfig.getTranslateExpression())
          && StringUtils.isBlank(userDnAttributeConfig.getTranslateExpressionCreateOnly())
          && StringUtils.isBlank(userDnAttributeConfig.getTranslateFromGrouperProvisioningEntityField())
          && StringUtils.isBlank(userDnAttributeConfig.getTranslateFromGrouperProvisioningEntityFieldCreateOnly())) {
        for (boolean createOnly : new boolean[] {false, true}) {          String userAttribute = null;
          String rdnTranslateFromGrouperProvisioningEntityField = createOnly ?  userRdnAttributeConfig.getTranslateFromGrouperProvisioningEntityFieldCreateOnly() 
              : userRdnAttributeConfig.getTranslateFromGrouperProvisioningEntityField();
          if (StringUtils.isBlank(rdnTranslateFromGrouperProvisioningEntityField)) {
            continue;
          }
          if (createOnly) {
            userDnAttributeConfig.setTranslateFromGrouperProvisioningEntityFieldCreateOnly(rdnTranslateFromGrouperProvisioningEntityField);
          } else {
            userDnAttributeConfig.setTranslateFromGrouperProvisioningEntityField(rdnTranslateFromGrouperProvisioningEntityField);
          }
        }
      }
    }
    
  }
  
  /**
   * If you want a metadata item on groups to allow a DN override
   */
  private boolean allowLdapGroupDnOverride;
  
  /**
   * If you want a metadata item on groups to allow a DN override
   * @return override
   */
  public boolean isAllowLdapGroupDnOverride() {
    return this.allowLdapGroupDnOverride;
  }

  /**
   * If you want a metadata item on groups to allow a DN override
   * @param allowLdapGroupDnOverride1
   */
  public void setAllowLdapGroupDnOverride(boolean allowLdapGroupDnOverride1) {
    this.allowLdapGroupDnOverride = allowLdapGroupDnOverride1;
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
