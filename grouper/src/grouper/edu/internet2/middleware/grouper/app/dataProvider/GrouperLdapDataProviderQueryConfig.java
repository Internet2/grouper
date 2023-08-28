package edu.internet2.middleware.grouper.app.dataProvider;

import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.dataField.GrouperDataProviderQueryConfig;

public class GrouperLdapDataProviderQueryConfig extends GrouperDataProviderQueryConfig {

  public GrouperLdapDataProviderQueryConfig() {

  }

  private String providerQueryLdapBaseDn;
  private String providerQueryLdapSearchScope;
  private String providerQueryLdapFilter;
  private String providerQueryLdapConfigId;

  /**
   * LDAP base DN
   * @return
   */
  public String getProviderQueryLdapBaseDn() {
    return providerQueryLdapBaseDn;
  }


  /**
   * LDAP base DN
   * @param providerQueryLdapBaseDn
   */
  public void setProviderQueryLdapBaseDn(String providerQueryLdapBaseDn) {
    this.providerQueryLdapBaseDn = providerQueryLdapBaseDn;
  }


  /**
   * LDAP search scope
   * @return
   */
  public String getProviderQueryLdapSearchScope() {
    return providerQueryLdapSearchScope;
  }


  /**
   * LDAP search scope
   * @param providerQueryLdapSearchScope
   */
  public void setProviderQueryLdapSearchScope(String providerQueryLdapSearchScope) {
    this.providerQueryLdapSearchScope = providerQueryLdapSearchScope;
  }


  /**
   * LDAP filter
   * @return
   */
  public String getProviderQueryLdapFilter() {
    return providerQueryLdapFilter;
  }


  /**
   * LDAP filter
   * @param providerQueryLdapFilter
   */
  public void setProviderQueryLdapFilter(String providerQueryLdapFilter) {
    this.providerQueryLdapFilter = providerQueryLdapFilter;
  }


  /**
   * ldap external system config id
   * @return
   */
  public String getProviderQueryLdapConfigId() {
    return providerQueryLdapConfigId;
  }


  /**
   * ldap external system config id
   * @param providerQueryLdapConfigId
   */
  public void setProviderQueryLdapConfigId(String providerQueryLdapConfigId) {
    this.providerQueryLdapConfigId = providerQueryLdapConfigId;
  }


  @Override
  public void configureSpecificSettings() {
    this.providerQueryLdapConfigId = GrouperConfig.retrieveConfig().propertyValueString("grouperDataProviderQuery." + this.getConfigId() + ".providerQueryLdapConfigId");
    this.providerQueryLdapBaseDn = GrouperConfig.retrieveConfig().propertyValueString("grouperDataProviderQuery." + this.getConfigId() + ".providerQueryLdapBaseDn");
    this.providerQueryLdapSearchScope = GrouperConfig.retrieveConfig().propertyValueString("grouperDataProviderQuery." + this.getConfigId() + ".providerQueryLdapSearchScope");
    this.providerQueryLdapFilter = GrouperConfig.retrieveConfig().propertyValueString("grouperDataProviderQuery." + this.getConfigId() + ".providerQueryLdapFilter");

  }
}
