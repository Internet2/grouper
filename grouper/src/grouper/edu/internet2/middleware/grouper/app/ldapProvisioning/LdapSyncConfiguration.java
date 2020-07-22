/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.app.ldapProvisioning;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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
  private String provisionedAttributeName;
  private String provisionedAttributeValueFormat;
  private String allProvisionedValuesPrefix;
  private String userSearchBaseDn;
  private String userSearchFilter;
  private String userSearchAllFilter;
  private String userObjectClass;
  private String userCreationParentDn;
  private int userCreationNumberOfAttributes;
  private String userCreationLdifTemplate_attr_0;
  private String userCreationLdifTemplate_val_0;
  private Integer userCreationLdifTemplate_maxLength_0;
  private String userCreationLdifTemplate_attr_1;
  private String userCreationLdifTemplate_val_1;
  private Integer userCreationLdifTemplate_maxLength_1;
  private String userCreationLdifTemplate_attr_2;
  private String userCreationLdifTemplate_val_2;
  private Integer userCreationLdifTemplate_maxLength_2;
  private String userCreationLdifTemplate_attr_3;
  private String userCreationLdifTemplate_val_3;
  private Integer userCreationLdifTemplate_maxLength_3;
  private String userCreationLdifTemplate_attr_4;
  private String userCreationLdifTemplate_val_4;
  private Integer userCreationLdifTemplate_maxLength_4;
  private String userCreationLdifTemplate_attr_5;
  private String userCreationLdifTemplate_val_5;
  private Integer userCreationLdifTemplate_maxLength_5;
  private String userCreationLdifTemplate_attr_6;
  private String userCreationLdifTemplate_val_6;
  private Integer userCreationLdifTemplate_maxLength_6;
  private String userCreationLdifTemplate_attr_7;
  private String userCreationLdifTemplate_val_7;
  private Integer userCreationLdifTemplate_maxLength_7;
  private String userCreationLdifTemplate_attr_8;
  private String userCreationLdifTemplate_val_8;
  private Integer userCreationLdifTemplate_maxLength_8;
  private String userCreationLdifTemplate_attr_9;
  private String userCreationLdifTemplate_val_9;
  private Integer userCreationLdifTemplate_maxLength_9;
  private String groupSearchBaseDn;
  private String groupSearchFilter;
  private String groupsSearchAllFilter;
  private LdapSyncGroupDnType groupDnType;
  private Set<String> groupObjectClass;
  private int groupCreationNumberOfAttributes;
  private String groupCreationLdifTemplate_attr_0;
  private String groupCreationLdifTemplate_val_0;
  private Integer groupCreationLdifTemplate_maxLength_0;
  private String groupCreationLdifTemplate_attr_1;
  private String groupCreationLdifTemplate_val_1;
  private Integer groupCreationLdifTemplate_maxLength_1;
  private String groupCreationLdifTemplate_attr_2;
  private String groupCreationLdifTemplate_val_2;
  private Integer groupCreationLdifTemplate_maxLength_2;
  private String groupCreationLdifTemplate_attr_3;
  private String groupCreationLdifTemplate_val_3;
  private Integer groupCreationLdifTemplate_maxLength_3;
  private String groupCreationLdifTemplate_attr_4;
  private String groupCreationLdifTemplate_val_4;
  private Integer groupCreationLdifTemplate_maxLength_4;
  private String groupCreationLdifTemplate_attr_5;
  private String groupCreationLdifTemplate_val_5;
  private Integer groupCreationLdifTemplate_maxLength_5;
  private String groupCreationLdifTemplate_attr_6;
  private String groupCreationLdifTemplate_val_6;
  private Integer groupCreationLdifTemplate_maxLength_6;
  private String groupCreationLdifTemplate_attr_7;
  private String groupCreationLdifTemplate_val_7;
  private Integer groupCreationLdifTemplate_maxLength_7;
  private String groupCreationLdifTemplate_attr_8;
  private String groupCreationLdifTemplate_val_8;
  private Integer groupCreationLdifTemplate_maxLength_8;
  private String groupCreationLdifTemplate_attr_9;
  private String groupCreationLdifTemplate_val_9;
  private Integer groupCreationLdifTemplate_maxLength_9;
  
  private List<String> userCreationLdifTemplate_attrs = new ArrayList<String>();
  private List<String> userCreationLdifTemplate_vals = new ArrayList<String>();
  private List<Integer> userCreationLdifTemplate_maxLengths = new ArrayList<Integer>();
  
  private List<String> groupCreationLdifTemplate_attrs = new ArrayList<String>();
  private List<String> groupCreationLdifTemplate_vals = new ArrayList<String>();
  private List<Integer> groupCreationLdifTemplate_maxLengths = new ArrayList<Integer>();
  
  @Override
  public void configureSpecificSettings() {

    this.ldapExternalSystemConfigId = this.retrieveConfigString("ldapExternalSystemConfigId", true);
    if (!GrouperLoaderConfig.retrieveConfig().assertPropertyValueRequired("ldap." + ldapExternalSystemConfigId + ".url")) {
      throw new RuntimeException("Unable to find ldap." + ldapExternalSystemConfigId + ".url property.  Is ldapExternalSystemConfigId set correctly?");
    }
    
    this.provisionedAttributeName = this.retrieveConfigString("provisionedAttributeName", false);
    this.provisionedAttributeValueFormat = this.retrieveConfigString("provisionedAttributeValueFormat", false);
    this.allProvisionedValuesPrefix = this.retrieveConfigString("allProvisionedValuesPrefix", false);
    
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
    this.userObjectClass = this.retrieveConfigString("userObjectClass", false);
    this.userCreationParentDn = this.retrieveConfigString("userCreationParentDn", this.isCreateMissingUsers());
    
    this.groupSearchBaseDn = this.retrieveConfigString("groupSearchBaseDn", false);
    this.groupSearchFilter = this.retrieveConfigString("groupSearchFilter", false);
    this.groupsSearchAllFilter = this.retrieveConfigString("groupsSearchAllFilter", false);
    this.groupObjectClass = GrouperUtil.splitTrimToSet(this.retrieveConfigString("groupObjectClass", false), ",");

    {
      String groupDnTypeString = this.retrieveConfigString("groupDnType", true);
      if (StringUtils.equalsIgnoreCase("flat", groupDnTypeString)) {
        this.groupDnType = LdapSyncGroupDnType.flat;
      } else if (StringUtils.equalsIgnoreCase("bushy", groupDnTypeString)) {
        this.groupDnType = LdapSyncGroupDnType.bushy;
      } else {
        throw new RuntimeException("Invalid groupDnType: '" + groupDnTypeString + "'");
      }
    }
    
    this.userCreationNumberOfAttributes = GrouperUtil.defaultIfNull(this.retrieveConfigInt("userCreationNumberOfAttributes", false), 1);
    
    this.userCreationLdifTemplate_attr_0 = this.retrieveConfigString("userCreationLdifTemplate_attr_0", false);
    this.userCreationLdifTemplate_val_0 = this.retrieveConfigString("userCreationLdifTemplate_val_0", false);
    this.userCreationLdifTemplate_maxLength_0 = this.retrieveConfigInt("userCreationLdifTemplate_maxLength_0", false);
    this.userCreationLdifTemplate_attr_1 = this.retrieveConfigString("userCreationLdifTemplate_attr_1", false);
    this.userCreationLdifTemplate_val_1 = this.retrieveConfigString("userCreationLdifTemplate_val_1", false);
    this.userCreationLdifTemplate_maxLength_1 = this.retrieveConfigInt("userCreationLdifTemplate_maxLength_1", false);
    this.userCreationLdifTemplate_attr_2 = this.retrieveConfigString("userCreationLdifTemplate_attr_2", false);
    this.userCreationLdifTemplate_val_2 = this.retrieveConfigString("userCreationLdifTemplate_val_2", false);
    this.userCreationLdifTemplate_maxLength_2 = this.retrieveConfigInt("userCreationLdifTemplate_maxLength_2", false);
    this.userCreationLdifTemplate_attr_3 = this.retrieveConfigString("userCreationLdifTemplate_attr_3", false);
    this.userCreationLdifTemplate_val_3 = this.retrieveConfigString("userCreationLdifTemplate_val_3", false);
    this.userCreationLdifTemplate_maxLength_3 = this.retrieveConfigInt("userCreationLdifTemplate_maxLength_3", false);
    this.userCreationLdifTemplate_attr_4 = this.retrieveConfigString("userCreationLdifTemplate_attr_4", false);
    this.userCreationLdifTemplate_val_4 = this.retrieveConfigString("userCreationLdifTemplate_val_4", false);
    this.userCreationLdifTemplate_maxLength_4 = this.retrieveConfigInt("userCreationLdifTemplate_maxLength_4", false);
    this.userCreationLdifTemplate_attr_5 = this.retrieveConfigString("userCreationLdifTemplate_attr_5", false);
    this.userCreationLdifTemplate_val_5 = this.retrieveConfigString("userCreationLdifTemplate_val_5", false);
    this.userCreationLdifTemplate_maxLength_5 = this.retrieveConfigInt("userCreationLdifTemplate_maxLength_5", false);
    this.userCreationLdifTemplate_attr_6 = this.retrieveConfigString("userCreationLdifTemplate_attr_6", false);
    this.userCreationLdifTemplate_val_6 = this.retrieveConfigString("userCreationLdifTemplate_val_6", false);
    this.userCreationLdifTemplate_maxLength_6 = this.retrieveConfigInt("userCreationLdifTemplate_maxLength_6", false);
    this.userCreationLdifTemplate_attr_7 = this.retrieveConfigString("userCreationLdifTemplate_attr_7", false);
    this.userCreationLdifTemplate_val_7 = this.retrieveConfigString("userCreationLdifTemplate_val_7", false);
    this.userCreationLdifTemplate_maxLength_7 = this.retrieveConfigInt("userCreationLdifTemplate_maxLength_7", false);
    this.userCreationLdifTemplate_attr_8 = this.retrieveConfigString("userCreationLdifTemplate_attr_8", false);
    this.userCreationLdifTemplate_val_8 = this.retrieveConfigString("userCreationLdifTemplate_val_8", false);
    this.userCreationLdifTemplate_maxLength_8 = this.retrieveConfigInt("userCreationLdifTemplate_maxLength_8", false);
    this.userCreationLdifTemplate_attr_9 = this.retrieveConfigString("userCreationLdifTemplate_attr_9", false);
    this.userCreationLdifTemplate_val_9 = this.retrieveConfigString("userCreationLdifTemplate_val_9", false);
    this.userCreationLdifTemplate_maxLength_9 = this.retrieveConfigInt("userCreationLdifTemplate_maxLength_9", false);

    this.groupCreationNumberOfAttributes = GrouperUtil.defaultIfNull(this.retrieveConfigInt("groupCreationNumberOfAttributes ", false), 1);
  
    this.groupCreationLdifTemplate_attr_0 = this.retrieveConfigString("groupCreationLdifTemplate_attr_0", false);
    this.groupCreationLdifTemplate_val_0 = this.retrieveConfigString("groupCreationLdifTemplate_val_0", false);
    this.groupCreationLdifTemplate_maxLength_0 = this.retrieveConfigInt("groupCreationLdifTemplate_maxLength_0", false);
    this.groupCreationLdifTemplate_attr_1 = this.retrieveConfigString("groupCreationLdifTemplate_attr_1", false);
    this.groupCreationLdifTemplate_val_1 = this.retrieveConfigString("groupCreationLdifTemplate_val_1", false);
    this.groupCreationLdifTemplate_maxLength_1 = this.retrieveConfigInt("groupCreationLdifTemplate_maxLength_1", false);
    this.groupCreationLdifTemplate_attr_2 = this.retrieveConfigString("groupCreationLdifTemplate_attr_2", false);
    this.groupCreationLdifTemplate_val_2 = this.retrieveConfigString("groupCreationLdifTemplate_val_2", false);
    this.groupCreationLdifTemplate_maxLength_2 = this.retrieveConfigInt("groupCreationLdifTemplate_maxLength_2", false);
    this.groupCreationLdifTemplate_attr_3 = this.retrieveConfigString("groupCreationLdifTemplate_attr_3", false);
    this.groupCreationLdifTemplate_val_3 = this.retrieveConfigString("groupCreationLdifTemplate_val_3", false);
    this.groupCreationLdifTemplate_maxLength_3 = this.retrieveConfigInt("groupCreationLdifTemplate_maxLength_3", false);
    this.groupCreationLdifTemplate_attr_4 = this.retrieveConfigString("groupCreationLdifTemplate_attr_4", false);
    this.groupCreationLdifTemplate_val_4 = this.retrieveConfigString("groupCreationLdifTemplate_val_4", false);
    this.groupCreationLdifTemplate_maxLength_4 = this.retrieveConfigInt("groupCreationLdifTemplate_maxLength_4", false);
    this.groupCreationLdifTemplate_attr_5 = this.retrieveConfigString("groupCreationLdifTemplate_attr_5", false);
    this.groupCreationLdifTemplate_val_5 = this.retrieveConfigString("groupCreationLdifTemplate_val_5", false);
    this.groupCreationLdifTemplate_maxLength_5 = this.retrieveConfigInt("groupCreationLdifTemplate_maxLength_5", false);
    this.groupCreationLdifTemplate_attr_6 = this.retrieveConfigString("groupCreationLdifTemplate_attr_6", false);
    this.groupCreationLdifTemplate_val_6 = this.retrieveConfigString("groupCreationLdifTemplate_val_6", false);
    this.groupCreationLdifTemplate_maxLength_6 = this.retrieveConfigInt("groupCreationLdifTemplate_maxLength_6", false);
    this.groupCreationLdifTemplate_attr_7 = this.retrieveConfigString("groupCreationLdifTemplate_attr_7", false);
    this.groupCreationLdifTemplate_val_7 = this.retrieveConfigString("groupCreationLdifTemplate_val_7", false);
    this.groupCreationLdifTemplate_maxLength_7 = this.retrieveConfigInt("groupCreationLdifTemplate_maxLength_7", false);
    this.groupCreationLdifTemplate_attr_8 = this.retrieveConfigString("groupCreationLdifTemplate_attr_8", false);
    this.groupCreationLdifTemplate_val_8 = this.retrieveConfigString("groupCreationLdifTemplate_val_8", false);
    this.groupCreationLdifTemplate_maxLength_8 = this.retrieveConfigInt("groupCreationLdifTemplate_maxLength_8", false);
    this.groupCreationLdifTemplate_attr_9 = this.retrieveConfigString("groupCreationLdifTemplate_attr_9", false);
    this.groupCreationLdifTemplate_val_9 = this.retrieveConfigString("groupCreationLdifTemplate_val_9", false);
    this.groupCreationLdifTemplate_maxLength_9 = this.retrieveConfigInt("groupCreationLdifTemplate_maxLength_9", false);
    
    for (int i = 0; i < userCreationNumberOfAttributes; i++) {
      String attr = this.retrieveConfigString("userCreationLdifTemplate_attr_" + i, true);
      userCreationLdifTemplate_attrs.add(attr);
      
      String val = this.retrieveConfigString("userCreationLdifTemplate_val_" + i, true);
      userCreationLdifTemplate_vals.add(val);
      
      Integer maxLength = this.retrieveConfigInt("userCreationLdifTemplate_maxLength_" + i, false);
      userCreationLdifTemplate_maxLengths.add(maxLength);
    }
    
    for (int i = 0; i < groupCreationNumberOfAttributes; i++) {
      String attr = this.retrieveConfigString("groupCreationLdifTemplate_attr_" + i, true);
      groupCreationLdifTemplate_attrs.add(attr);
      
      String val = this.retrieveConfigString("groupCreationLdifTemplate_val_" + i, true);
      groupCreationLdifTemplate_vals.add(val);
      
      Integer maxLength = this.retrieveConfigInt("groupCreationLdifTemplate_maxLength_" + i, false);
      groupCreationLdifTemplate_maxLengths.add(maxLength);
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

  
  public String getProvisionedAttributeName() {
    return provisionedAttributeName;
  }

  
  public void setProvisionedAttributeName(String provisionedAttributeName) {
    this.provisionedAttributeName = provisionedAttributeName;
  }

  
  public String getProvisionedAttributeValueFormat() {
    return provisionedAttributeValueFormat;
  }

  
  public void setProvisionedAttributeValueFormat(String provisionedAttributeValueFormat) {
    this.provisionedAttributeValueFormat = provisionedAttributeValueFormat;
  }

  
  public String getAllProvisionedValuesPrefix() {
    return allProvisionedValuesPrefix;
  }

  
  public void setAllProvisionedValuesPrefix(String allProvisionedValuesPrefix) {
    this.allProvisionedValuesPrefix = allProvisionedValuesPrefix;
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

  
  public String getUserObjectClass() {
    return userObjectClass;
  }

  
  public void setUserObjectClass(String userObjectClass) {
    this.userObjectClass = userObjectClass;
  }

  
  public String getUserCreationParentDn() {
    return userCreationParentDn;
  }

  
  public void setUserCreationParentDn(String userCreationParentDn) {
    this.userCreationParentDn = userCreationParentDn;
  }

  
  public int getUserCreationNumberOfAttributes() {
    return userCreationNumberOfAttributes;
  }

  
  public void setUserCreationNumberOfAttributes(int userCreationNumberOfAttributes) {
    this.userCreationNumberOfAttributes = userCreationNumberOfAttributes;
  }

  
  public String getUserCreationLdifTemplate_attr_0() {
    return userCreationLdifTemplate_attr_0;
  }

  
  public void setUserCreationLdifTemplate_attr_0(String userCreationLdifTemplate_attr_0) {
    this.userCreationLdifTemplate_attr_0 = userCreationLdifTemplate_attr_0;
  }

  
  public String getUserCreationLdifTemplate_val_0() {
    return userCreationLdifTemplate_val_0;
  }

  
  public void setUserCreationLdifTemplate_val_0(String userCreationLdifTemplate_val_0) {
    this.userCreationLdifTemplate_val_0 = userCreationLdifTemplate_val_0;
  }

  
  public Integer getUserCreationLdifTemplate_maxLength_0() {
    return userCreationLdifTemplate_maxLength_0;
  }

  
  public void setUserCreationLdifTemplate_maxLength_0(
      Integer userCreationLdifTemplate_maxLength_0) {
    this.userCreationLdifTemplate_maxLength_0 = userCreationLdifTemplate_maxLength_0;
  }

  
  public String getUserCreationLdifTemplate_attr_1() {
    return userCreationLdifTemplate_attr_1;
  }

  
  public void setUserCreationLdifTemplate_attr_1(String userCreationLdifTemplate_attr_1) {
    this.userCreationLdifTemplate_attr_1 = userCreationLdifTemplate_attr_1;
  }

  
  public String getUserCreationLdifTemplate_val_1() {
    return userCreationLdifTemplate_val_1;
  }

  
  public void setUserCreationLdifTemplate_val_1(String userCreationLdifTemplate_val_1) {
    this.userCreationLdifTemplate_val_1 = userCreationLdifTemplate_val_1;
  }

  
  public Integer getUserCreationLdifTemplate_maxLength_1() {
    return userCreationLdifTemplate_maxLength_1;
  }

  
  public void setUserCreationLdifTemplate_maxLength_1(
      Integer userCreationLdifTemplate_maxLength_1) {
    this.userCreationLdifTemplate_maxLength_1 = userCreationLdifTemplate_maxLength_1;
  }

  
  public String getUserCreationLdifTemplate_attr_2() {
    return userCreationLdifTemplate_attr_2;
  }

  
  public void setUserCreationLdifTemplate_attr_2(String userCreationLdifTemplate_attr_2) {
    this.userCreationLdifTemplate_attr_2 = userCreationLdifTemplate_attr_2;
  }

  
  public String getUserCreationLdifTemplate_val_2() {
    return userCreationLdifTemplate_val_2;
  }

  
  public void setUserCreationLdifTemplate_val_2(String userCreationLdifTemplate_val_2) {
    this.userCreationLdifTemplate_val_2 = userCreationLdifTemplate_val_2;
  }

  
  public Integer getUserCreationLdifTemplate_maxLength_2() {
    return userCreationLdifTemplate_maxLength_2;
  }

  
  public void setUserCreationLdifTemplate_maxLength_2(
      Integer userCreationLdifTemplate_maxLength_2) {
    this.userCreationLdifTemplate_maxLength_2 = userCreationLdifTemplate_maxLength_2;
  }

  
  public String getUserCreationLdifTemplate_attr_3() {
    return userCreationLdifTemplate_attr_3;
  }

  
  public void setUserCreationLdifTemplate_attr_3(String userCreationLdifTemplate_attr_3) {
    this.userCreationLdifTemplate_attr_3 = userCreationLdifTemplate_attr_3;
  }

  
  public String getUserCreationLdifTemplate_val_3() {
    return userCreationLdifTemplate_val_3;
  }

  
  public void setUserCreationLdifTemplate_val_3(String userCreationLdifTemplate_val_3) {
    this.userCreationLdifTemplate_val_3 = userCreationLdifTemplate_val_3;
  }

  
  public Integer getUserCreationLdifTemplate_maxLength_3() {
    return userCreationLdifTemplate_maxLength_3;
  }

  
  public void setUserCreationLdifTemplate_maxLength_3(
      Integer userCreationLdifTemplate_maxLength_3) {
    this.userCreationLdifTemplate_maxLength_3 = userCreationLdifTemplate_maxLength_3;
  }

  
  public String getUserCreationLdifTemplate_attr_4() {
    return userCreationLdifTemplate_attr_4;
  }

  
  public void setUserCreationLdifTemplate_attr_4(String userCreationLdifTemplate_attr_4) {
    this.userCreationLdifTemplate_attr_4 = userCreationLdifTemplate_attr_4;
  }

  
  public String getUserCreationLdifTemplate_val_4() {
    return userCreationLdifTemplate_val_4;
  }

  
  public void setUserCreationLdifTemplate_val_4(String userCreationLdifTemplate_val_4) {
    this.userCreationLdifTemplate_val_4 = userCreationLdifTemplate_val_4;
  }

  
  public Integer getUserCreationLdifTemplate_maxLength_4() {
    return userCreationLdifTemplate_maxLength_4;
  }

  
  public void setUserCreationLdifTemplate_maxLength_4(
      Integer userCreationLdifTemplate_maxLength_4) {
    this.userCreationLdifTemplate_maxLength_4 = userCreationLdifTemplate_maxLength_4;
  }

  
  public String getUserCreationLdifTemplate_attr_5() {
    return userCreationLdifTemplate_attr_5;
  }

  
  public void setUserCreationLdifTemplate_attr_5(String userCreationLdifTemplate_attr_5) {
    this.userCreationLdifTemplate_attr_5 = userCreationLdifTemplate_attr_5;
  }

  
  public String getUserCreationLdifTemplate_val_5() {
    return userCreationLdifTemplate_val_5;
  }

  
  public void setUserCreationLdifTemplate_val_5(String userCreationLdifTemplate_val_5) {
    this.userCreationLdifTemplate_val_5 = userCreationLdifTemplate_val_5;
  }

  
  public Integer getUserCreationLdifTemplate_maxLength_5() {
    return userCreationLdifTemplate_maxLength_5;
  }

  
  public void setUserCreationLdifTemplate_maxLength_5(
      Integer userCreationLdifTemplate_maxLength_5) {
    this.userCreationLdifTemplate_maxLength_5 = userCreationLdifTemplate_maxLength_5;
  }

  
  public String getUserCreationLdifTemplate_attr_6() {
    return userCreationLdifTemplate_attr_6;
  }

  
  public void setUserCreationLdifTemplate_attr_6(String userCreationLdifTemplate_attr_6) {
    this.userCreationLdifTemplate_attr_6 = userCreationLdifTemplate_attr_6;
  }

  
  public String getUserCreationLdifTemplate_val_6() {
    return userCreationLdifTemplate_val_6;
  }

  
  public void setUserCreationLdifTemplate_val_6(String userCreationLdifTemplate_val_6) {
    this.userCreationLdifTemplate_val_6 = userCreationLdifTemplate_val_6;
  }

  
  public Integer getUserCreationLdifTemplate_maxLength_6() {
    return userCreationLdifTemplate_maxLength_6;
  }

  
  public void setUserCreationLdifTemplate_maxLength_6(
      Integer userCreationLdifTemplate_maxLength_6) {
    this.userCreationLdifTemplate_maxLength_6 = userCreationLdifTemplate_maxLength_6;
  }

  
  public String getUserCreationLdifTemplate_attr_7() {
    return userCreationLdifTemplate_attr_7;
  }

  
  public void setUserCreationLdifTemplate_attr_7(String userCreationLdifTemplate_attr_7) {
    this.userCreationLdifTemplate_attr_7 = userCreationLdifTemplate_attr_7;
  }

  
  public String getUserCreationLdifTemplate_val_7() {
    return userCreationLdifTemplate_val_7;
  }

  
  public void setUserCreationLdifTemplate_val_7(String userCreationLdifTemplate_val_7) {
    this.userCreationLdifTemplate_val_7 = userCreationLdifTemplate_val_7;
  }

  
  public Integer getUserCreationLdifTemplate_maxLength_7() {
    return userCreationLdifTemplate_maxLength_7;
  }

  
  public void setUserCreationLdifTemplate_maxLength_7(
      Integer userCreationLdifTemplate_maxLength_7) {
    this.userCreationLdifTemplate_maxLength_7 = userCreationLdifTemplate_maxLength_7;
  }

  
  public String getUserCreationLdifTemplate_attr_8() {
    return userCreationLdifTemplate_attr_8;
  }

  
  public void setUserCreationLdifTemplate_attr_8(String userCreationLdifTemplate_attr_8) {
    this.userCreationLdifTemplate_attr_8 = userCreationLdifTemplate_attr_8;
  }

  
  public String getUserCreationLdifTemplate_val_8() {
    return userCreationLdifTemplate_val_8;
  }

  
  public void setUserCreationLdifTemplate_val_8(String userCreationLdifTemplate_val_8) {
    this.userCreationLdifTemplate_val_8 = userCreationLdifTemplate_val_8;
  }

  
  public Integer getUserCreationLdifTemplate_maxLength_8() {
    return userCreationLdifTemplate_maxLength_8;
  }

  
  public void setUserCreationLdifTemplate_maxLength_8(
      Integer userCreationLdifTemplate_maxLength_8) {
    this.userCreationLdifTemplate_maxLength_8 = userCreationLdifTemplate_maxLength_8;
  }

  
  public String getUserCreationLdifTemplate_attr_9() {
    return userCreationLdifTemplate_attr_9;
  }

  
  public void setUserCreationLdifTemplate_attr_9(String userCreationLdifTemplate_attr_9) {
    this.userCreationLdifTemplate_attr_9 = userCreationLdifTemplate_attr_9;
  }

  
  public String getUserCreationLdifTemplate_val_9() {
    return userCreationLdifTemplate_val_9;
  }

  
  public void setUserCreationLdifTemplate_val_9(String userCreationLdifTemplate_val_9) {
    this.userCreationLdifTemplate_val_9 = userCreationLdifTemplate_val_9;
  }

  
  public Integer getUserCreationLdifTemplate_maxLength_9() {
    return userCreationLdifTemplate_maxLength_9;
  }

  
  public void setUserCreationLdifTemplate_maxLength_9(
      Integer userCreationLdifTemplate_maxLength_9) {
    this.userCreationLdifTemplate_maxLength_9 = userCreationLdifTemplate_maxLength_9;
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

  
  public String getGroupsSearchAllFilter() {
    return groupsSearchAllFilter;
  }

  
  public void setGroupsSearchAllFilter(String groupsSearchAllFilter) {
    this.groupsSearchAllFilter = groupsSearchAllFilter;
  }

  
  public LdapSyncGroupDnType getGroupDnType() {
    return groupDnType;
  }

  
  public void setGroupDnType(LdapSyncGroupDnType groupDnType) {
    this.groupDnType = groupDnType;
  }

  
  public Set<String> getGroupObjectClass() {
    return groupObjectClass;
  }

  
  public void setGroupObjectClass(Set<String> groupObjectClass) {
    this.groupObjectClass = groupObjectClass;
  }

  
  public int getGroupCreationNumberOfAttributes() {
    return groupCreationNumberOfAttributes;
  }

  
  public void setGroupCreationNumberOfAttributes(int groupCreationNumberOfAttributes) {
    this.groupCreationNumberOfAttributes = groupCreationNumberOfAttributes;
  }

  
  public String getGroupCreationLdifTemplate_attr_0() {
    return groupCreationLdifTemplate_attr_0;
  }

  
  public void setGroupCreationLdifTemplate_attr_0(String groupCreationLdifTemplate_attr_0) {
    this.groupCreationLdifTemplate_attr_0 = groupCreationLdifTemplate_attr_0;
  }

  
  public String getGroupCreationLdifTemplate_val_0() {
    return groupCreationLdifTemplate_val_0;
  }

  
  public void setGroupCreationLdifTemplate_val_0(String groupCreationLdifTemplate_val_0) {
    this.groupCreationLdifTemplate_val_0 = groupCreationLdifTemplate_val_0;
  }

  
  public Integer getGroupCreationLdifTemplate_maxLength_0() {
    return groupCreationLdifTemplate_maxLength_0;
  }

  
  public void setGroupCreationLdifTemplate_maxLength_0(
      Integer groupCreationLdifTemplate_maxLength_0) {
    this.groupCreationLdifTemplate_maxLength_0 = groupCreationLdifTemplate_maxLength_0;
  }

  
  public String getGroupCreationLdifTemplate_attr_1() {
    return groupCreationLdifTemplate_attr_1;
  }

  
  public void setGroupCreationLdifTemplate_attr_1(String groupCreationLdifTemplate_attr_1) {
    this.groupCreationLdifTemplate_attr_1 = groupCreationLdifTemplate_attr_1;
  }

  
  public String getGroupCreationLdifTemplate_val_1() {
    return groupCreationLdifTemplate_val_1;
  }

  
  public void setGroupCreationLdifTemplate_val_1(String groupCreationLdifTemplate_val_1) {
    this.groupCreationLdifTemplate_val_1 = groupCreationLdifTemplate_val_1;
  }

  
  public Integer getGroupCreationLdifTemplate_maxLength_1() {
    return groupCreationLdifTemplate_maxLength_1;
  }

  
  public void setGroupCreationLdifTemplate_maxLength_1(
      Integer groupCreationLdifTemplate_maxLength_1) {
    this.groupCreationLdifTemplate_maxLength_1 = groupCreationLdifTemplate_maxLength_1;
  }

  
  public String getGroupCreationLdifTemplate_attr_2() {
    return groupCreationLdifTemplate_attr_2;
  }

  
  public void setGroupCreationLdifTemplate_attr_2(String groupCreationLdifTemplate_attr_2) {
    this.groupCreationLdifTemplate_attr_2 = groupCreationLdifTemplate_attr_2;
  }

  
  public String getGroupCreationLdifTemplate_val_2() {
    return groupCreationLdifTemplate_val_2;
  }

  
  public void setGroupCreationLdifTemplate_val_2(String groupCreationLdifTemplate_val_2) {
    this.groupCreationLdifTemplate_val_2 = groupCreationLdifTemplate_val_2;
  }

  
  public Integer getGroupCreationLdifTemplate_maxLength_2() {
    return groupCreationLdifTemplate_maxLength_2;
  }

  
  public void setGroupCreationLdifTemplate_maxLength_2(
      Integer groupCreationLdifTemplate_maxLength_2) {
    this.groupCreationLdifTemplate_maxLength_2 = groupCreationLdifTemplate_maxLength_2;
  }

  
  public String getGroupCreationLdifTemplate_attr_3() {
    return groupCreationLdifTemplate_attr_3;
  }

  
  public void setGroupCreationLdifTemplate_attr_3(String groupCreationLdifTemplate_attr_3) {
    this.groupCreationLdifTemplate_attr_3 = groupCreationLdifTemplate_attr_3;
  }

  
  public String getGroupCreationLdifTemplate_val_3() {
    return groupCreationLdifTemplate_val_3;
  }

  
  public void setGroupCreationLdifTemplate_val_3(String groupCreationLdifTemplate_val_3) {
    this.groupCreationLdifTemplate_val_3 = groupCreationLdifTemplate_val_3;
  }

  
  public Integer getGroupCreationLdifTemplate_maxLength_3() {
    return groupCreationLdifTemplate_maxLength_3;
  }

  
  public void setGroupCreationLdifTemplate_maxLength_3(
      Integer groupCreationLdifTemplate_maxLength_3) {
    this.groupCreationLdifTemplate_maxLength_3 = groupCreationLdifTemplate_maxLength_3;
  }

  
  public String getGroupCreationLdifTemplate_attr_4() {
    return groupCreationLdifTemplate_attr_4;
  }

  
  public void setGroupCreationLdifTemplate_attr_4(String groupCreationLdifTemplate_attr_4) {
    this.groupCreationLdifTemplate_attr_4 = groupCreationLdifTemplate_attr_4;
  }

  
  public String getGroupCreationLdifTemplate_val_4() {
    return groupCreationLdifTemplate_val_4;
  }

  
  public void setGroupCreationLdifTemplate_val_4(String groupCreationLdifTemplate_val_4) {
    this.groupCreationLdifTemplate_val_4 = groupCreationLdifTemplate_val_4;
  }

  
  public Integer getGroupCreationLdifTemplate_maxLength_4() {
    return groupCreationLdifTemplate_maxLength_4;
  }

  
  public void setGroupCreationLdifTemplate_maxLength_4(
      Integer groupCreationLdifTemplate_maxLength_4) {
    this.groupCreationLdifTemplate_maxLength_4 = groupCreationLdifTemplate_maxLength_4;
  }

  
  public String getGroupCreationLdifTemplate_attr_5() {
    return groupCreationLdifTemplate_attr_5;
  }

  
  public void setGroupCreationLdifTemplate_attr_5(String groupCreationLdifTemplate_attr_5) {
    this.groupCreationLdifTemplate_attr_5 = groupCreationLdifTemplate_attr_5;
  }

  
  public String getGroupCreationLdifTemplate_val_5() {
    return groupCreationLdifTemplate_val_5;
  }

  
  public void setGroupCreationLdifTemplate_val_5(String groupCreationLdifTemplate_val_5) {
    this.groupCreationLdifTemplate_val_5 = groupCreationLdifTemplate_val_5;
  }

  
  public Integer getGroupCreationLdifTemplate_maxLength_5() {
    return groupCreationLdifTemplate_maxLength_5;
  }

  
  public void setGroupCreationLdifTemplate_maxLength_5(
      Integer groupCreationLdifTemplate_maxLength_5) {
    this.groupCreationLdifTemplate_maxLength_5 = groupCreationLdifTemplate_maxLength_5;
  }

  
  public String getGroupCreationLdifTemplate_attr_6() {
    return groupCreationLdifTemplate_attr_6;
  }

  
  public void setGroupCreationLdifTemplate_attr_6(String groupCreationLdifTemplate_attr_6) {
    this.groupCreationLdifTemplate_attr_6 = groupCreationLdifTemplate_attr_6;
  }

  
  public String getGroupCreationLdifTemplate_val_6() {
    return groupCreationLdifTemplate_val_6;
  }

  
  public void setGroupCreationLdifTemplate_val_6(String groupCreationLdifTemplate_val_6) {
    this.groupCreationLdifTemplate_val_6 = groupCreationLdifTemplate_val_6;
  }

  
  public Integer getGroupCreationLdifTemplate_maxLength_6() {
    return groupCreationLdifTemplate_maxLength_6;
  }

  
  public void setGroupCreationLdifTemplate_maxLength_6(
      Integer groupCreationLdifTemplate_maxLength_6) {
    this.groupCreationLdifTemplate_maxLength_6 = groupCreationLdifTemplate_maxLength_6;
  }

  
  public String getGroupCreationLdifTemplate_attr_7() {
    return groupCreationLdifTemplate_attr_7;
  }

  
  public void setGroupCreationLdifTemplate_attr_7(String groupCreationLdifTemplate_attr_7) {
    this.groupCreationLdifTemplate_attr_7 = groupCreationLdifTemplate_attr_7;
  }

  
  public String getGroupCreationLdifTemplate_val_7() {
    return groupCreationLdifTemplate_val_7;
  }

  
  public void setGroupCreationLdifTemplate_val_7(String groupCreationLdifTemplate_val_7) {
    this.groupCreationLdifTemplate_val_7 = groupCreationLdifTemplate_val_7;
  }

  
  public Integer getGroupCreationLdifTemplate_maxLength_7() {
    return groupCreationLdifTemplate_maxLength_7;
  }

  
  public void setGroupCreationLdifTemplate_maxLength_7(
      Integer groupCreationLdifTemplate_maxLength_7) {
    this.groupCreationLdifTemplate_maxLength_7 = groupCreationLdifTemplate_maxLength_7;
  }

  
  public String getGroupCreationLdifTemplate_attr_8() {
    return groupCreationLdifTemplate_attr_8;
  }

  
  public void setGroupCreationLdifTemplate_attr_8(String groupCreationLdifTemplate_attr_8) {
    this.groupCreationLdifTemplate_attr_8 = groupCreationLdifTemplate_attr_8;
  }

  
  public String getGroupCreationLdifTemplate_val_8() {
    return groupCreationLdifTemplate_val_8;
  }

  
  public void setGroupCreationLdifTemplate_val_8(String groupCreationLdifTemplate_val_8) {
    this.groupCreationLdifTemplate_val_8 = groupCreationLdifTemplate_val_8;
  }

  
  public Integer getGroupCreationLdifTemplate_maxLength_8() {
    return groupCreationLdifTemplate_maxLength_8;
  }

  
  public void setGroupCreationLdifTemplate_maxLength_8(
      Integer groupCreationLdifTemplate_maxLength_8) {
    this.groupCreationLdifTemplate_maxLength_8 = groupCreationLdifTemplate_maxLength_8;
  }

  
  public String getGroupCreationLdifTemplate_attr_9() {
    return groupCreationLdifTemplate_attr_9;
  }

  
  public void setGroupCreationLdifTemplate_attr_9(String groupCreationLdifTemplate_attr_9) {
    this.groupCreationLdifTemplate_attr_9 = groupCreationLdifTemplate_attr_9;
  }

  
  public String getGroupCreationLdifTemplate_val_9() {
    return groupCreationLdifTemplate_val_9;
  }

  
  public void setGroupCreationLdifTemplate_val_9(String groupCreationLdifTemplate_val_9) {
    this.groupCreationLdifTemplate_val_9 = groupCreationLdifTemplate_val_9;
  }

  
  public Integer getGroupCreationLdifTemplate_maxLength_9() {
    return groupCreationLdifTemplate_maxLength_9;
  }

  
  public void setGroupCreationLdifTemplate_maxLength_9(
      Integer groupCreationLdifTemplate_maxLength_9) {
    this.groupCreationLdifTemplate_maxLength_9 = groupCreationLdifTemplate_maxLength_9;
  }


  
  public List<String> getUserCreationLdifTemplate_attrs() {
    return userCreationLdifTemplate_attrs;
  }


  
  public void setUserCreationLdifTemplate_attrs(
      List<String> userCreationLdifTemplate_attrs) {
    this.userCreationLdifTemplate_attrs = userCreationLdifTemplate_attrs;
  }


  
  public List<String> getUserCreationLdifTemplate_vals() {
    return userCreationLdifTemplate_vals;
  }


  
  public void setUserCreationLdifTemplate_vals(List<String> userCreationLdifTemplate_vals) {
    this.userCreationLdifTemplate_vals = userCreationLdifTemplate_vals;
  }


  
  public List<Integer> getUserCreationLdifTemplate_maxLengths() {
    return userCreationLdifTemplate_maxLengths;
  }


  
  public void setUserCreationLdifTemplate_maxLengths(
      List<Integer> userCreationLdifTemplate_maxLengths) {
    this.userCreationLdifTemplate_maxLengths = userCreationLdifTemplate_maxLengths;
  }


  
  public List<String> getGroupCreationLdifTemplate_attrs() {
    return groupCreationLdifTemplate_attrs;
  }


  
  public void setGroupCreationLdifTemplate_attrs(
      List<String> groupCreationLdifTemplate_attrs) {
    this.groupCreationLdifTemplate_attrs = groupCreationLdifTemplate_attrs;
  }


  
  public List<String> getGroupCreationLdifTemplate_vals() {
    return groupCreationLdifTemplate_vals;
  }


  
  public void setGroupCreationLdifTemplate_vals(
      List<String> groupCreationLdifTemplate_vals) {
    this.groupCreationLdifTemplate_vals = groupCreationLdifTemplate_vals;
  }


  
  public List<Integer> getGroupCreationLdifTemplate_maxLengths() {
    return groupCreationLdifTemplate_maxLengths;
  }


  
  public void setGroupCreationLdifTemplate_maxLengths(
      List<Integer> groupCreationLdifTemplate_maxLengths) {
    this.groupCreationLdifTemplate_maxLengths = groupCreationLdifTemplate_maxLengths;
  }
}
