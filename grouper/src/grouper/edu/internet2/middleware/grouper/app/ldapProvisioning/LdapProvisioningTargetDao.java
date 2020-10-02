package edu.internet2.middleware.grouper.app.ldapProvisioning;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import edu.internet2.middleware.grouper.app.ldapProvisioning.ldapSyncDao.LdapSyncDaoForLdap;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningEntity;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningGroup;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningObjectChange;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningObjectChangeAction;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.GrouperProvisionerDaoCapabilities;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.GrouperProvisionerTargetDaoBase;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoDeleteGroupRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoDeleteGroupResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoInsertEntityRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoInsertEntityResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoInsertGroupRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoInsertGroupResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveAllEntitiesRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveAllEntitiesResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveAllGroupsRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveAllGroupsResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveEntitiesRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveEntitiesResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveGroupsRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveGroupsResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoTimingInfo;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoUpdateGroupRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoUpdateGroupResponse;
import edu.internet2.middleware.grouper.ldap.LdapAttribute;
import edu.internet2.middleware.grouper.ldap.LdapConfiguration;
import edu.internet2.middleware.grouper.ldap.LdapEntry;
import edu.internet2.middleware.grouper.ldap.LdapModificationItem;
import edu.internet2.middleware.grouper.ldap.LdapModificationType;
import edu.internet2.middleware.grouper.ldap.LdapSearchScope;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.lang3.StringUtils;


// TODO exception handling per wiki

public class LdapProvisioningTargetDao extends GrouperProvisionerTargetDaoBase {


  @Override
  public TargetDaoRetrieveAllGroupsResponse retrieveAllGroups(TargetDaoRetrieveAllGroupsRequest targetDaoRetrieveAllGroupsRequest) {
    
    long startNanos = System.nanoTime();

    try {
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
        
        if (!StringUtils.isBlank(groupAttributeNameForMemberships)) {
          groupSearchAttributeNames.add(groupAttributeNameForMemberships);
          groupAttributesMultivalued.add(groupAttributeNameForMemberships);
        }
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
        
        //targetGroup.assignAttributeValue("dn", ldapEntry.getDn());
        results.add(targetGroup);
      }
  
      return new TargetDaoRetrieveAllGroupsResponse(results);
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("retrieveAllGroups", startNanos));
    }
  }
  
  @Override
  public TargetDaoInsertGroupResponse insertGroup(TargetDaoInsertGroupRequest targetDaoInsertGroupRequest) {
    long startNanos = System.nanoTime();

    try {
      ProvisioningGroup targetGroup = targetDaoInsertGroupRequest.getTargetGroup();
      LdapSyncConfiguration ldapSyncConfiguration = (LdapSyncConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
      String ldapConfigId = ldapSyncConfiguration.getLdapExternalSystemConfigId();
      
      if (StringUtils.isBlank(targetGroup.getName())) {
        throw new RuntimeException("Why is targetGroup.getName() blank?");
      }
      
      LdapEntry ldapEntry = new LdapEntry(targetGroup.getName());
      for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetGroup.getInternal_objectChanges())) {
        Object value = provisioningObjectChange.getNewValue();
        
        if (StringUtils.isEmpty(provisioningObjectChange.getAttributeName())) {
          if ("name".equals(provisioningObjectChange.getFieldName())) {
            // update the ldap entry dn just in case it's different
            ldapEntry.setDn((String)provisioningObjectChange.getNewValue());
          }
          
          continue;
        }
        
        LdapAttribute ldapAttribute = new LdapAttribute(provisioningObjectChange.getAttributeName());
  
        if (value instanceof String && !StringUtils.isEmpty((String)value)) {
          ldapAttribute.addValue((String)value);
        } else if (value instanceof Collection) {
          @SuppressWarnings("unchecked")
          Collection<Object> values = (Collection<Object>) provisioningObjectChange.getNewValue();
          if (values.size() > 0) {
            ldapAttribute.addValues(values);
          }
        }
        
        if (ldapAttribute.getValues().size() > 0) {
          ldapEntry.addAttribute(ldapAttribute);
        }
      }
      
      new LdapSyncDaoForLdap().create(ldapConfigId, ldapEntry);
      targetGroup.setProvisioned(true);
      for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetGroup.getInternal_objectChanges())) {
        provisioningObjectChange.setProvisioned(true);
      }

      return new TargetDaoInsertGroupResponse();
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("insertGroup", startNanos));
    }
  }
  
  @Override
  public TargetDaoDeleteGroupResponse deleteGroup(TargetDaoDeleteGroupRequest targetDaoDeleteGroupRequest) {
    
    long startNanos = System.nanoTime();

    try {
      ProvisioningGroup targetGroup = targetDaoDeleteGroupRequest == null ? null : targetDaoDeleteGroupRequest.getTargetGroup();
      LdapSyncConfiguration ldapSyncConfiguration = (LdapSyncConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
      String ldapConfigId = ldapSyncConfiguration.getLdapExternalSystemConfigId();
      
      if (StringUtils.isBlank(targetGroup.getName())) {
        throw new RuntimeException("Why is targetGroup.getName() blank?");
      }
      new LdapSyncDaoForLdap().delete(ldapConfigId, targetGroup.getName());
      targetGroup.setProvisioned(true);
      for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetGroup.getInternal_objectChanges())) {
        provisioningObjectChange.setProvisioned(true);
      }
      return new TargetDaoDeleteGroupResponse();
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("deleteGroup", startNanos));
    }
  }

  public TargetDaoUpdateGroupResponse updateGroup(TargetDaoUpdateGroupRequest targetDaoUpdateGroupRequest) {

    long startNanos = System.nanoTime();

    try {
      ProvisioningGroup targetGroup = targetDaoUpdateGroupRequest.getTargetGroup();
      Set<ProvisioningObjectChange> provisionObjectChanges = targetGroup.getInternal_objectChanges();
  
      LdapSyncConfiguration ldapSyncConfiguration = (LdapSyncConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
      String ldapConfigId = ldapSyncConfiguration.getLdapExternalSystemConfigId();
      
      List<LdapModificationItem> ldapModificationItems = new ArrayList<LdapModificationItem>();
          
      for (ProvisioningObjectChange provisionObjectChange : provisionObjectChanges) {
        
        String attributeName = provisionObjectChange.getAttributeName();
        ProvisioningObjectChangeAction action = provisionObjectChange.getProvisioningObjectChangeAction();
        Object newValue = provisionObjectChange.getNewValue();
        Object oldValue = provisionObjectChange.getOldValue();
        
        if (action == ProvisioningObjectChangeAction.delete) {
          if (newValue != null) {
            throw new RuntimeException("Deleting value but there's a new value=" + newValue + ", attributeName=" + attributeName);
          }
                  
          if (oldValue == null) {
            // delete the whole attribute
            LdapModificationItem item = new LdapModificationItem(LdapModificationType.REMOVE_ATTRIBUTE, new LdapAttribute(attributeName));
            ldapModificationItems.add(item);
          } else {
            LdapModificationItem item = new LdapModificationItem(LdapModificationType.REMOVE_ATTRIBUTE, new LdapAttribute(attributeName, oldValue));
            ldapModificationItems.add(item);
          }
        } else if (action == ProvisioningObjectChangeAction.update) {
          if (oldValue != null) {
            LdapModificationItem item = new LdapModificationItem(LdapModificationType.REMOVE_ATTRIBUTE, new LdapAttribute(attributeName, oldValue));
            ldapModificationItems.add(item);
          }
          
          if (newValue != null) {
            LdapModificationItem item = new LdapModificationItem(LdapModificationType.ADD_ATTRIBUTE, new LdapAttribute(attributeName, newValue));
            ldapModificationItems.add(item);
          }
        } else if (action == ProvisioningObjectChangeAction.insert) {
          if (oldValue != null) {
            throw new RuntimeException("Inserting value but there's an old value=" + oldValue + ", attributeName=" + attributeName);
          }
          
          if (newValue == null) {
            throw new RuntimeException("Inserting value but there's no new value for attributeName=" + attributeName);
          }
          
          LdapModificationItem item = new LdapModificationItem(LdapModificationType.ADD_ATTRIBUTE, new LdapAttribute(attributeName, newValue));
          ldapModificationItems.add(item);
        } else {
          throw new RuntimeException("Unexpected provisioningObjectChangeAction: " + action);
        }
      }
  
      if (ldapModificationItems.size() > 0) {
        new LdapSyncDaoForLdap().modify(ldapConfigId, targetGroup.getName(), ldapModificationItems); // TODO partial errors?
      }
      
      targetGroup.setProvisioned(true);
      for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetGroup.getInternal_objectChanges())) {
        provisioningObjectChange.setProvisioned(true);
      }
      return new TargetDaoUpdateGroupResponse();
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("updateGroup", startNanos));
    }
  }
  
  public TargetDaoRetrieveGroupsResponse retrieveGroups(TargetDaoRetrieveGroupsRequest targetDaoRetrieveGroupsRequest) {

    List<ProvisioningGroup> results = new ArrayList<ProvisioningGroup>();

    boolean includeAllMembershipsIfApplicable = targetDaoRetrieveGroupsRequest.isIncludeAllMembershipsIfApplicable();
    
    LdapSyncConfiguration ldapSyncConfiguration = (LdapSyncConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
    String ldapConfigId = ldapSyncConfiguration.getLdapExternalSystemConfigId();
    
    String groupSearchBaseDn = ldapSyncConfiguration.getGroupSearchBaseDn();
    String groupSearchFilter = ldapSyncConfiguration.getGroupSearchFilter();
    if (StringUtils.isEmpty(groupSearchFilter)) {
      throw new RuntimeException("Why is groupSearchFilter empty?");
    }
    
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
      
      if (!StringUtils.isBlank(groupAttributeNameForMemberships)) {
        groupSearchAttributeNames.add(groupAttributeNameForMemberships);
        groupAttributesMultivalued.add(groupAttributeNameForMemberships);
      }
    }
    
    List<ProvisioningGroup> targetGroups = targetDaoRetrieveGroupsRequest.getTargetGroups();
    int batchSize = LdapConfiguration.getConfig(ldapConfigId).getQueryBatchSize();;
    int numberOfBatches = GrouperUtil.batchNumberOfBatches(targetGroups.size(), batchSize);

    for (int i = 0; i < numberOfBatches; i++) {
      long startNanos = System.nanoTime();

      try {
        List<ProvisioningGroup> currentBatchTargetGroups = GrouperUtil.batchList(targetGroups, batchSize, i);
        String filter = "";
        for (ProvisioningGroup targetGroup : currentBatchTargetGroups) {
          filter += targetGroup.getSearchFilter();
        }
        
        if (currentBatchTargetGroups.size() > 1) {
          filter = "(|" + filter + ")";
        }
        
        List<LdapEntry> ldapEntries = new LdapSyncDaoForLdap().search(ldapConfigId, groupSearchBaseDn, filter, LdapSearchScope.SUBTREE_SCOPE, new ArrayList<String>(groupSearchAttributeNames));
        
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
          
          //targetGroup.assignAttributeValue("dn", ldapEntry.getDn());
          results.add(targetGroup);
        }    
      } finally {
        this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("retrieveGroups", startNanos));
      }
    }
    
    return new TargetDaoRetrieveGroupsResponse(results);
  }
  
  @Override
  public TargetDaoRetrieveAllEntitiesResponse retrieveAllEntities(TargetDaoRetrieveAllEntitiesRequest targetDaoRetrieveAllEntitiesRequest) {
    
    long startNanos = System.nanoTime();

    try {
      boolean includeAllMembershipsIfApplicable = targetDaoRetrieveAllEntitiesRequest == null ? false : targetDaoRetrieveAllEntitiesRequest.isIncludeAllMembershipsIfApplicable();
      
      List<ProvisioningEntity> results = new ArrayList<ProvisioningEntity>();
      
      LdapSyncConfiguration ldapSyncConfiguration = (LdapSyncConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
      String ldapConfigId = ldapSyncConfiguration.getLdapExternalSystemConfigId();
      String userSearchAllFilter = ldapSyncConfiguration.getUserSearchAllFilter();
      
      if (StringUtils.isEmpty(userSearchAllFilter)) {
        throw new RuntimeException("Why is userSearchAllFilter empty?");
      }
  
      String userSearchBaseDn = ldapSyncConfiguration.getUserSearchBaseDn();
  
      Set<String> userSearchAttributeNames = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
      userSearchAttributeNames.addAll(ldapSyncConfiguration.getUserSearchAttributes());
        
      Set<String> userAttributesMultivalued = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
      if (ldapSyncConfiguration.getUserAttributesMultivalued() != null) {
        userAttributesMultivalued.addAll(ldapSyncConfiguration.getUserAttributesMultivalued());
      }
      
      userSearchAttributeNames.add("objectClass");
      userAttributesMultivalued.add("objectClass");
      
      if (includeAllMembershipsIfApplicable) {
        String userAttributeNameForMemberships = ldapSyncConfiguration.getUserAttributeNameForMemberships();
        
        if (!StringUtils.isBlank(userAttributeNameForMemberships)) {
          userSearchAttributeNames.add(userAttributeNameForMemberships);
          userAttributesMultivalued.add(userAttributeNameForMemberships);
        }
      }
      
      List<LdapEntry> ldapEntries = new LdapSyncDaoForLdap().search(ldapConfigId, userSearchBaseDn, userSearchAllFilter, LdapSearchScope.SUBTREE_SCOPE, new ArrayList<String>(userSearchAttributeNames));
      
      for (LdapEntry ldapEntry : ldapEntries) {
        ProvisioningEntity targetEntity = new ProvisioningEntity();
        targetEntity.setName(ldapEntry.getDn());
        
        for (LdapAttribute ldapAttribute : ldapEntry.getAttributes()) {
          targetEntity.assignAttributeValue(ldapAttribute.getName(), ldapAttribute.getValues());
          Object value = null;
          if (userAttributesMultivalued.contains(ldapAttribute.getName())) {
            value = new HashSet<Object>(ldapAttribute.getValues());
          } else if (ldapAttribute.getValues().size() == 1) {
            value = ldapAttribute.getValues().iterator().next();
          }
          
          targetEntity.assignAttributeValue(ldapAttribute.getName(), value);
        }
        
        //targetEntity.assignAttributeValue("dn", ldapEntry.getDn());
        results.add(targetEntity);
      }
  
      return new TargetDaoRetrieveAllEntitiesResponse(results);
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("retrieveAllEntities", startNanos));
    }
  }
  
  public TargetDaoRetrieveEntitiesResponse retrieveEntities(TargetDaoRetrieveEntitiesRequest targetDaoRetrieveEntitiesRequest) {

    List<ProvisioningEntity> results = new ArrayList<ProvisioningEntity>();

    boolean includeAllMembershipsIfApplicable = targetDaoRetrieveEntitiesRequest.isIncludeAllMembershipsIfApplicable();
    
    LdapSyncConfiguration ldapSyncConfiguration = (LdapSyncConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
    String ldapConfigId = ldapSyncConfiguration.getLdapExternalSystemConfigId();
    
    String userSearchBaseDn = ldapSyncConfiguration.getUserSearchBaseDn();
    String userSearchFilter = ldapSyncConfiguration.getUserSearchFilter();
    if (StringUtils.isEmpty(userSearchFilter)) {
      throw new RuntimeException("Why is userSearchFilter empty?");
    }
    
    Set<String> userSearchAttributeNames = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
    userSearchAttributeNames.addAll(ldapSyncConfiguration.getUserSearchAttributes());
      
    Set<String> userAttributesMultivalued = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
    if (ldapSyncConfiguration.getUserAttributesMultivalued() != null) {
      userAttributesMultivalued.addAll(ldapSyncConfiguration.getUserAttributesMultivalued());
    }
    
    userSearchAttributeNames.add("objectClass");
    userAttributesMultivalued.add("objectClass");
    
    if (includeAllMembershipsIfApplicable) {
      String userAttributeNameForMemberships = ldapSyncConfiguration.getUserAttributeNameForMemberships();
      
      if (!StringUtils.isBlank(userAttributeNameForMemberships)) {
        userSearchAttributeNames.add(userAttributeNameForMemberships);
        userAttributesMultivalued.add(userAttributeNameForMemberships);
      }
    }
    
    List<ProvisioningEntity> targetEntities = targetDaoRetrieveEntitiesRequest.getTargetEntities();
    int batchSize = LdapConfiguration.getConfig(ldapConfigId).getQueryBatchSize();;
    int numberOfBatches = GrouperUtil.batchNumberOfBatches(targetEntities.size(), batchSize);

    for (int i = 0; i < numberOfBatches; i++) {
      long startNanos = System.nanoTime();

      try {
        List<ProvisioningEntity> currentBatchTargetEntities = GrouperUtil.batchList(targetEntities, batchSize, i);
        String filter = "";
        for (ProvisioningEntity targetEntity : currentBatchTargetEntities) {
          filter += targetEntity.getSearchFilter();
        }
        
        if (currentBatchTargetEntities.size() > 1) {
          filter = "(|" + filter + ")";
        }
        
        List<LdapEntry> ldapEntries = new LdapSyncDaoForLdap().search(ldapConfigId, userSearchBaseDn, filter, LdapSearchScope.SUBTREE_SCOPE, new ArrayList<String>(userSearchAttributeNames));
        
        for (LdapEntry ldapEntry : ldapEntries) {
          ProvisioningEntity targetEntity = new ProvisioningEntity();
          targetEntity.setName(ldapEntry.getDn());
          
          for (LdapAttribute ldapAttribute : ldapEntry.getAttributes()) {
            targetEntity.assignAttributeValue(ldapAttribute.getName(), ldapAttribute.getValues());
            Object value = null;
            if (userAttributesMultivalued.contains(ldapAttribute.getName())) {
              value = new HashSet<Object>(ldapAttribute.getValues());
            } else if (ldapAttribute.getValues().size() == 1) {
              value = ldapAttribute.getValues().iterator().next();
            }
            
            targetEntity.assignAttributeValue(ldapAttribute.getName(), value);
          }
          
          //targetEntity.assignAttributeValue("dn", ldapEntry.getDn());
          results.add(targetEntity);
        }    
      } finally {
        this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("retrieveEntities", startNanos));
      }
    }
    
    return new TargetDaoRetrieveEntitiesResponse(results);
  }
  
  public TargetDaoInsertEntityResponse insertEntity(TargetDaoInsertEntityRequest targetDaoInsertEntityRequest) {
    long startNanos = System.nanoTime();

    try {
      ProvisioningEntity targetEntity = targetDaoInsertEntityRequest.getTargetEntity();
      LdapSyncConfiguration ldapSyncConfiguration = (LdapSyncConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
      String ldapConfigId = ldapSyncConfiguration.getLdapExternalSystemConfigId();
      
      if (StringUtils.isBlank(targetEntity.getName())) {
        throw new RuntimeException("Why is targetEntity.getName() blank?");
      }
      
      LdapEntry ldapEntry = new LdapEntry(targetEntity.getName());
      for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetEntity.getInternal_objectChanges())) {
        Object value = provisioningObjectChange.getNewValue();
        
        if (StringUtils.isEmpty(provisioningObjectChange.getAttributeName())) {
          if ("name".equals(provisioningObjectChange.getFieldName())) {
            // update the ldap entry dn just in case it's different
            ldapEntry.setDn((String)provisioningObjectChange.getNewValue());
          }

          continue;
        }
        
        LdapAttribute ldapAttribute = new LdapAttribute(provisioningObjectChange.getAttributeName());
  
        if (value instanceof String && !StringUtils.isEmpty((String)value)) {
          ldapAttribute.addValue((String)value);
        } else if (value instanceof Collection) {
          @SuppressWarnings("unchecked")
          Collection<Object> values = (Collection<Object>) provisioningObjectChange.getNewValue();
          if (values.size() > 0) {
            ldapAttribute.addValues(values);
          } 
        }
        
        if (ldapAttribute.getValues().size() > 0) {
          ldapEntry.addAttribute(ldapAttribute);
        }
      }
      
      new LdapSyncDaoForLdap().create(ldapConfigId, ldapEntry);
      targetEntity.setProvisioned(true);
      for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetEntity.getInternal_objectChanges())) {
        provisioningObjectChange.setProvisioned(true);
      }

      return new TargetDaoInsertEntityResponse();
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("insertEntity", startNanos));
    }
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
    grouperProvisionerDaoCapabilities.setCanRetrieveAllEntities(true);
    grouperProvisionerDaoCapabilities.setCanRetrieveEntities(true);
    grouperProvisionerDaoCapabilities.setCanRetrieveEntityWithOrWithoutMembershipAttribute(true);
    grouperProvisionerDaoCapabilities.getCanInsertEntity();
  }

}
