package edu.internet2.middleware.grouper.app.ldapProvisioning;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import edu.internet2.middleware.grouper.app.ldapProvisioning.ldapSyncDao.LdapSyncDaoForLdap;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningAttribute;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningGroup;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.GrouperProvisionerDaoCapabilities;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.GrouperProvisionerTargetDaoBase;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoDeleteGroupRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoDeleteGroupResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoInsertGroupRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoInsertGroupResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveAllGroupsRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveAllGroupsResponse;
import edu.internet2.middleware.grouper.ldap.LdapAttribute;
import edu.internet2.middleware.grouper.ldap.LdapEntry;
import edu.internet2.middleware.grouper.ldap.LdapModificationItem;
import edu.internet2.middleware.grouper.ldap.LdapModificationResult;
import edu.internet2.middleware.grouper.ldap.LdapModificationType;
import edu.internet2.middleware.grouper.ldap.LdapSearchScope;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.lang3.StringUtils;


public class LdapProvisioningTargetDao extends GrouperProvisionerTargetDaoBase {
  
  @Override
  public TargetDaoRetrieveAllGroupsResponse retrieveAllGroups(TargetDaoRetrieveAllGroupsRequest targetDaoRetrieveAllGroupsRequest) {
    
    boolean includeAllMembershipsIfApplicable = targetDaoRetrieveAllGroupsRequest == null ? false : targetDaoRetrieveAllGroupsRequest.isIncludeAllMembershipsIfApplicable();
    
    List<ProvisioningGroup> results = new ArrayList<ProvisioningGroup>();
    
    LdapSyncConfiguration ldapSyncConfiguration = (LdapSyncConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
    String ldapConfigId = ldapSyncConfiguration.getLdapExternalSystemConfigId();
    String groupSearchAllFilter = ldapSyncConfiguration.getGroupSearchAllFilter();
    
    if (StringUtils.isEmpty(groupSearchAllFilter)) {
      throw new RuntimeException("Why is groupSearchAllFilter empty?");
    }

    String groupSearchBaseDn = ldapSyncConfiguration.getGroupSearchBaseDn();

    Set<String> groupSearchAttributeNames = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
    groupSearchAttributeNames.addAll(ldapSyncConfiguration.getGroupSearchAttributes());
      
    Set<String> groupAttributesMultivalued = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
    if (ldapSyncConfiguration.getGroupAttributesMultivalued() != null) {
      groupAttributesMultivalued.addAll(ldapSyncConfiguration.getGroupAttributesMultivalued());
    }
    
    groupSearchAttributeNames.add("objectClass");
    groupAttributesMultivalued.add("objectClass");
    
    if (includeAllMembershipsIfApplicable) {
      String groupAttributeNameForMemberships = ldapSyncConfiguration.getGroupAttributeNameForMemberships();
      groupSearchAttributeNames.add(groupAttributeNameForMemberships);
      groupAttributesMultivalued.add(groupAttributeNameForMemberships);
    }
    
    List<LdapEntry> ldapEntries = new LdapSyncDaoForLdap().search(ldapConfigId, groupSearchBaseDn, groupSearchAllFilter, LdapSearchScope.SUBTREE_SCOPE, new ArrayList<String>(groupSearchAttributeNames));
    
    for (LdapEntry ldapEntry : ldapEntries) {
      ProvisioningGroup targetGroup = new ProvisioningGroup();
      targetGroup.setName(ldapEntry.getDn());
      
      for (LdapAttribute ldapAttribute : ldapEntry.getAttributes()) {
        targetGroup.assignAttributeValue(ldapAttribute.getName(), ldapAttribute.getValues());
        Object value = null;
        if (groupAttributesMultivalued.contains(ldapAttribute.getName())) {
          value = new HashSet<Object>(ldapAttribute.getValues());
        } else if (ldapAttribute.getValues().size() == 1) {
          value = ldapAttribute.getValues().iterator().next();
        }
        
        targetGroup.assignAttributeValue(ldapAttribute.getName(), value);
      }
      
      results.add(targetGroup);
    }

    return new TargetDaoRetrieveAllGroupsResponse(results);
  }
  
  @Override
  public TargetDaoInsertGroupResponse insertGroup(TargetDaoInsertGroupRequest targetDaoInsertGroupRequest) {
    ProvisioningGroup targetGroup = targetDaoInsertGroupRequest.getTargetGroup();
    LdapSyncConfiguration ldapSyncConfiguration = (LdapSyncConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
    String ldapConfigId = ldapSyncConfiguration.getLdapExternalSystemConfigId();
    
    if (StringUtils.isBlank(targetGroup.getName())) {
      throw new RuntimeException("Why is targetGroup.getName() blank?");
    }
    
    LdapEntry ldapEntry = new LdapEntry(targetGroup.getName());
    for (String attributeName : targetGroup.getAttributes().keySet()) {
      ProvisioningAttribute targetAttribute = targetGroup.getAttributes().get(attributeName);
      Object value = targetAttribute.getValue();
      
      LdapAttribute ldapAttribute = new LdapAttribute(targetAttribute.getName());

      if (value instanceof String && !StringUtils.isEmpty((String)value)) {
        ldapAttribute.addValue((String)value);
      } else if (value instanceof Collection) {
      @SuppressWarnings("unchecked")
      Collection<Object> values = (Collection<Object>)targetAttribute.getValue();
      if (values.size() > 0) {
        ldapAttribute.addValues(values);
        } 
      }
      
      if (ldapAttribute.getValues().size() > 0) {
        ldapEntry.addAttribute(ldapAttribute);
      }
    }
    
    new LdapSyncDaoForLdap().create(ldapConfigId, ldapEntry);
    return null;
  }
  
  @Override
  public TargetDaoDeleteGroupResponse deleteGroup(TargetDaoDeleteGroupRequest targetDaoDeleteGroupRequest) {
    
    ProvisioningGroup targetGroup = targetDaoDeleteGroupRequest == null ? null : targetDaoDeleteGroupRequest.getTargetGroup();
    LdapSyncConfiguration ldapSyncConfiguration = (LdapSyncConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
    String ldapConfigId = ldapSyncConfiguration.getLdapExternalSystemConfigId();
    
    if (StringUtils.isBlank(targetGroup.getName())) {
      throw new RuntimeException("Why is targetGroup.getName() blank?");
    }
    new LdapSyncDaoForLdap().delete(ldapConfigId, targetGroup.getName());
    return null;
  }

  @SuppressWarnings("unchecked")
  public boolean updateGroupIfNeeded(ProvisioningGroup grouperTranslatedTargetGroup, ProvisioningGroup actualTargetGroup) {
    LdapSyncConfiguration ldapSyncConfiguration = (LdapSyncConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
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
  public void registerGrouperProvisionerDaoCapabilities(
      GrouperProvisionerDaoCapabilities grouperProvisionerDaoCapabilities) {
    grouperProvisionerDaoCapabilities.setCanDeleteGroup(true);
    grouperProvisionerDaoCapabilities.setCanInsertGroup(true);
    grouperProvisionerDaoCapabilities.setCanRetrieveAllGroups(true);
    grouperProvisionerDaoCapabilities.setCanRetrieveGroups(true);
    grouperProvisionerDaoCapabilities.setCanRetrieveGroupWithOrWithoutMembershipAttribute(true);
    grouperProvisionerDaoCapabilities.setCanUpdateGroup(true);
    grouperProvisionerDaoCapabilities.setCanUpdateGroupMembershipAttribute(true);
    
  }

}
