package edu.internet2.middleware.grouper.app.ldapProvisioning;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.internet2.middleware.grouper.app.ldapProvisioning.ldapSyncDao.LdapSyncDaoForLdap;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisionerTargetDaoBase;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningAttribute;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningGroup;
import edu.internet2.middleware.grouper.ldap.LdapAttribute;
import edu.internet2.middleware.grouper.ldap.LdapEntry;
import edu.internet2.middleware.grouper.ldap.LdapModificationItem;
import edu.internet2.middleware.grouper.ldap.LdapModificationResult;
import edu.internet2.middleware.grouper.ldap.LdapModificationType;
import edu.internet2.middleware.grouper.ldap.LdapSearchScope;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.lang3.StringUtils;


public class LdapProvisioningTargetDao extends GrouperProvisionerTargetDaoBase {
  
  @Override
  public List<ProvisioningGroup> retrieveAllGroups(boolean includeAllMembershipsIfApplicable) {
    
    List<ProvisioningGroup> results = new ArrayList<ProvisioningGroup>();
    
    LdapSyncConfiguration ldapSyncConfiguration = (LdapSyncConfiguration) this.getGrouperProvisioner().retrieveProvisioningConfiguration();
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
      ProvisioningGroup targetGroup = new ProvisioningGroup();
      targetGroup.setId(ldapEntry.getDn());
      
      for (LdapAttribute ldapAttribute : ldapEntry.getAttributes()) {
        targetGroup.assignAttribute(ldapAttribute.getName(), ldapAttribute.getValues());
      }
      
      results.add(targetGroup);
    }

    return results;
  }
  
  public boolean createGroup(ProvisioningGroup targetGroup) {
    LdapSyncConfiguration ldapSyncConfiguration = (LdapSyncConfiguration) this.getGrouperProvisioner().retrieveProvisioningConfiguration();
    String ldapConfigId = ldapSyncConfiguration.getLdapExternalSystemConfigId();
    
    LdapEntry ldapEntry = new LdapEntry(targetGroup.getId());
    for (String attributeName : targetGroup.getAttributes().keySet()) {
      ProvisioningAttribute targetAttribute = targetGroup.getAttributes().get(attributeName);
      Collection<Object> values = (Collection<Object>)targetAttribute.getValue();
      if (values.size() > 0) {
        LdapAttribute ldapAttribute = new LdapAttribute(targetAttribute.getName());
        ldapAttribute.addValues(values);
        ldapEntry.addAttribute(ldapAttribute);
      }
    }
    
    return new LdapSyncDaoForLdap().create(ldapConfigId, ldapEntry);
  }
  
  public void deleteGroup(ProvisioningGroup targetGroup) {
    LdapSyncConfiguration ldapSyncConfiguration = (LdapSyncConfiguration) this.getGrouperProvisioner().retrieveProvisioningConfiguration();
    String ldapConfigId = ldapSyncConfiguration.getLdapExternalSystemConfigId();
    
    new LdapSyncDaoForLdap().delete(ldapConfigId, targetGroup.getId());
   
  }

  public boolean updateGroupIfNeeded(ProvisioningGroup grouperTranslatedTargetGroup, ProvisioningGroup actualTargetGroup) {
    LdapSyncConfiguration ldapSyncConfiguration = (LdapSyncConfiguration) this.getGrouperProvisioner().retrieveProvisioningConfiguration();
    String ldapConfigId = ldapSyncConfiguration.getLdapExternalSystemConfigId();
    
    List<LdapModificationItem> ldapModificationItems = new ArrayList<LdapModificationItem>();
    
    Map<String, ProvisioningAttribute> grouperTranslatedProvisioningAttributes = grouperTranslatedTargetGroup.getAttributes();
    Map<String, ProvisioningAttribute> actualProvisioningAttributes = actualTargetGroup.getAttributes();
    
    Set<String> allAttributes = new HashSet<String>(grouperTranslatedProvisioningAttributes.keySet());
    allAttributes.addAll(actualProvisioningAttributes.keySet());
    
    for (String attributeName : allAttributes) {
      Set<Object> grouperValues = new HashSet<Object>();
      Set<Object> targetValues = new HashSet<Object>();
      
      if (grouperTranslatedProvisioningAttributes.containsKey(attributeName) && grouperTranslatedProvisioningAttributes.get(attributeName).getValue() != null) {
        grouperValues = new HashSet<Object>((Collection<Object>)grouperTranslatedProvisioningAttributes.get(attributeName).getValue());
      }
      
      if (actualProvisioningAttributes.containsKey(attributeName) && actualProvisioningAttributes.get(attributeName).getValue() != null) {
        targetValues = new HashSet<Object>((Collection<Object>)actualProvisioningAttributes.get(attributeName).getValue());
      }
      
      if (grouperValues.size() == 0 && targetValues.size() > 0) {
        // delete attribute here
        LdapModificationItem item = new LdapModificationItem(LdapModificationType.REPLACE_ATTRIBUTE, new LdapAttribute(attributeName));
        ldapModificationItems.add(item);
      } else {
        Set<Object> valuesToAdd = new HashSet<Object>(grouperValues);
        valuesToAdd.removeAll(targetValues);
        
        if (valuesToAdd.size() > 0) {
          LdapAttribute ldapAttribute = new LdapAttribute(attributeName);
          ldapAttribute.addValues(valuesToAdd);
          LdapModificationItem item = new LdapModificationItem(LdapModificationType.ADD_ATTRIBUTE, ldapAttribute);
          ldapModificationItems.add(item);
        }
        
        Set<Object> valuesToRemove = new HashSet<Object>(targetValues);
        valuesToRemove.removeAll(grouperValues);
        
        if (valuesToRemove.size() > 0) {
          LdapAttribute ldapAttribute = new LdapAttribute(attributeName);
          ldapAttribute.addValues(valuesToRemove);
          LdapModificationItem item = new LdapModificationItem(LdapModificationType.REMOVE_ATTRIBUTE, ldapAttribute);
          ldapModificationItems.add(item);
        }
      }
    }
    
    if (ldapModificationItems.size() > 0) {
      LdapModificationResult result = new LdapSyncDaoForLdap().modify(ldapConfigId, actualTargetGroup.getId(), ldapModificationItems);
      
      // TODO what to do about partial errors
      return true;
    }
    
    return false;
  }

  @Override
  public void sendChangesToTarget() {
    // TODO Auto-generated method stub
    
  }
}
