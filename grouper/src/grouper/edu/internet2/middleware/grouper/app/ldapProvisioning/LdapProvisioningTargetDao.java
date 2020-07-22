package edu.internet2.middleware.grouper.app.ldapProvisioning;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.internet2.middleware.grouper.app.ldapProvisioning.ldapSyncDao.LdapSyncDaoForLdap;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisionerTargetDaoBase;
import edu.internet2.middleware.grouper.app.provisioning.TargetAttribute;
import edu.internet2.middleware.grouper.app.provisioning.TargetGroup;
import edu.internet2.middleware.grouper.ldap.LdapAttribute;
import edu.internet2.middleware.grouper.ldap.LdapEntry;
import edu.internet2.middleware.grouper.ldap.LdapSearchScope;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.lang3.StringUtils;


public class LdapProvisioningTargetDao extends GrouperProvisionerTargetDaoBase {

  /**
   * reference back to provisioner, must be set whenn created
   */
  private LdapSync ldapSync;


  /**
   * reference back to provisioner, must be set whenn created
   * @return
   */
  public LdapSync getLdapSync() {
    return ldapSync;
  }


  /**
   * reference back to provisioner, must be set whenn created
   * @param ldapSync
   */
  public void setLdapSync(LdapSync ldapSync) {
    this.ldapSync = ldapSync;
  }

  
  @Override
  public Map<String, TargetGroup> retrieveAllGroups() {
    
    Map<String, TargetGroup> results = new HashMap<String, TargetGroup>();
    
    LdapSyncConfiguration ldapSyncConfiguration = (LdapSyncConfiguration) ldapSync.retrieveProvisioningConfiguration();
    String ldapConfigId = ldapSyncConfiguration.getLdapExternalSystemConfigId();
    String groupSearchAllFilter = ldapSyncConfiguration.getGroupsSearchAllFilter();
    
    if (StringUtils.isEmpty(groupSearchAllFilter)) {
      throw new RuntimeException("Why is groupsSearchAllFilter empty?");
    }

    String groupSearchBaseDn = ldapSyncConfiguration.getGroupSearchBaseDn();

    boolean attributeNamesContainsObjectClass = false;
    boolean attributeNamesContainsProvisionedAttributeName = false;
    Set<String> attributeNames = new HashSet<String>();
    for (String attributeName : ldapSyncConfiguration.getGroupCreationLdifTemplate_attrs()) {
      if (attributeName.equalsIgnoreCase("objectClass")) {
        attributeNamesContainsObjectClass = true;
      } else if (attributeName.equalsIgnoreCase(ldapSyncConfiguration.getProvisionedAttributeName())) {
        attributeNamesContainsProvisionedAttributeName = true;
      }
      
      attributeNames.add(attributeName);
    }
    
    if (!attributeNamesContainsObjectClass) {
      attributeNames.add("objectClass");
    }
    
    if (!attributeNamesContainsProvisionedAttributeName && !StringUtils.isEmpty(ldapSyncConfiguration.getProvisionedAttributeName())) {
      attributeNames.add(ldapSyncConfiguration.getProvisionedAttributeName());
    }
    
    List<LdapEntry> ldapEntries = new LdapSyncDaoForLdap().search(ldapConfigId, groupSearchBaseDn, groupSearchAllFilter, LdapSearchScope.SUBTREE_SCOPE, new ArrayList<String>(attributeNames));
    
    for (LdapEntry ldapEntry : ldapEntries) {
      TargetGroup targetGroup = new TargetGroup();
      targetGroup.setId(ldapEntry.getDn());

      Map<String, TargetAttribute> targetAttributes = new HashMap<String, TargetAttribute>();
      
      for (LdapAttribute ldapAttribute : ldapEntry.getAttributes()) {
        TargetAttribute targetAttribute = new TargetAttribute();
        targetAttribute.setName(ldapAttribute.getName());
        targetAttribute.setValue(ldapAttribute.getValues());
        targetAttributes.put(targetAttribute.getName(), targetAttribute);
      }
      
      targetGroup.setAttributes(targetAttributes);
      results.put(ldapEntry.getDn(), targetGroup);
    }

    return results;
  }
  
  
}
