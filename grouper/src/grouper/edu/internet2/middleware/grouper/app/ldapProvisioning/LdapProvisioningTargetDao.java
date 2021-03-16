package edu.internet2.middleware.grouper.app.ldapProvisioning;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.app.ldapProvisioning.ldapSyncDao.LdapSyncDaoForLdap;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfigurationAttribute;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningDiagnosticsContainer;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningEntity;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningGroup;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningObjectChange;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningObjectChangeAction;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.GrouperProvisionerDaoCapabilities;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.GrouperProvisionerTargetDaoBase;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoDeleteEntityRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoDeleteEntityResponse;
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
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoUpdateEntityRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoUpdateEntityResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoUpdateGroupRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoUpdateGroupResponse;
import edu.internet2.middleware.grouper.ldap.LdapAttribute;
import edu.internet2.middleware.grouper.ldap.LdapConfiguration;
import edu.internet2.middleware.grouper.ldap.LdapEntry;
import edu.internet2.middleware.grouper.ldap.LdapModificationAttributeError;
import edu.internet2.middleware.grouper.ldap.LdapModificationItem;
import edu.internet2.middleware.grouper.ldap.LdapModificationResult;
import edu.internet2.middleware.grouper.ldap.LdapModificationType;
import edu.internet2.middleware.grouper.ldap.LdapSearchScope;
import edu.internet2.middleware.grouper.util.GrouperUtil;


public class LdapProvisioningTargetDao extends GrouperProvisionerTargetDaoBase {

  private static final Log LOG = GrouperUtil.getLog(LdapProvisioningTargetDao.class);

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
      groupSearchAttributeNames.addAll(ldapSyncConfiguration.getGroupSelectAttributes());
        
      Set<String> groupAttributesMultivalued = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
      if (ldapSyncConfiguration.getGroupAttributesMultivalued() != null) {
        groupAttributesMultivalued.addAll(ldapSyncConfiguration.getGroupAttributesMultivalued());
      }
      
      groupSearchAttributeNames.add("objectClass");
      groupAttributesMultivalued.add("objectClass");
      
      String groupAttributeNameForMemberships = ldapSyncConfiguration.getGroupAttributeNameForMemberships();
      if (!StringUtils.isBlank(groupAttributeNameForMemberships)) {
        if (includeAllMembershipsIfApplicable) {
          groupSearchAttributeNames.add(groupAttributeNameForMemberships);
          groupAttributesMultivalued.add(groupAttributeNameForMemberships);
        } else {
          groupSearchAttributeNames.remove(groupAttributeNameForMemberships);
          groupAttributesMultivalued.remove(groupAttributeNameForMemberships);
        }
      }
      
      LdapSyncDaoForLdap ldapSyncDaoForLdap = new LdapSyncDaoForLdap();
      GrouperProvisioningDiagnosticsContainer grouperProvisioningDiagnosticsContainer = this.getGrouperProvisioner().retrieveGrouperProvisioningDiagnosticsContainer();
      ldapSyncDaoForLdap.assignDebug(grouperProvisioningDiagnosticsContainer.isInDiagnostics());
      List<LdapEntry> ldapEntries = ldapSyncDaoForLdap.search(ldapConfigId, groupSearchBaseDn, groupSearchAllFilter, LdapSearchScope.SUBTREE_SCOPE, new ArrayList<String>(groupSearchAttributeNames));
      if (grouperProvisioningDiagnosticsContainer.isInDiagnostics()) {
        grouperProvisioningDiagnosticsContainer.appendReportLineIfNotBlank(ldapSyncDaoForLdap.getDebugLog().toString());
      }
      
      for (LdapEntry ldapEntry : ldapEntries) {
        ProvisioningGroup targetGroup = new ProvisioningGroup();
        targetGroup.setName(ldapEntry.getDn());
        
        for (LdapAttribute ldapAttribute : ldapEntry.getAttributes()) {
          if (ldapAttribute.getValues().size() > 0) {
            Object value = null;
            if (groupAttributesMultivalued.contains(ldapAttribute.getName())) {
              value = new HashSet<Object>(ldapAttribute.getValues());
            } else if (ldapAttribute.getValues().size() == 1) {
              value = ldapAttribute.getValues().iterator().next();
            }
            
            targetGroup.assignAttributeValue(ldapAttribute.getName(), value);
          }
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
    ProvisioningGroup targetGroup = targetDaoInsertGroupRequest.getTargetGroup();

    try {
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

        LdapAttribute ldapAttribute = ldapEntry.getAttribute(provisioningObjectChange.getAttributeName());
        if (ldapAttribute == null) {
          ldapAttribute = new LdapAttribute(provisioningObjectChange.getAttributeName());
        }
        
        if (value instanceof byte[]) {
          ldapAttribute.addValue(value);
        } else if (value instanceof Collection) {
          @SuppressWarnings("unchecked")
          Collection<Object> values = (Collection<Object>) provisioningObjectChange.getNewValue();
          for (Object singleValue : values) {
            if (singleValue instanceof byte[]) {
              ldapAttribute.addValue(singleValue);
            } else {
              String singleStringValue = GrouperUtil.stringValue(singleValue);
              if (!StringUtils.isEmpty(singleStringValue)) {
                ldapAttribute.addValue(singleStringValue);
              }
            }
          }
        } else {
          String stringValue = GrouperUtil.stringValue(value);
          if (!StringUtils.isEmpty(stringValue)) {
            ldapAttribute.addValue(stringValue);
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
    } catch (Exception e) {
      targetGroup.setProvisioned(false);
      for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetGroup.getInternal_objectChanges())) {
        provisioningObjectChange.setProvisioned(false);
      }
      
      throw e;
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("insertGroup", startNanos));
    }
  }
  
  @Override
  public TargetDaoDeleteGroupResponse deleteGroup(TargetDaoDeleteGroupRequest targetDaoDeleteGroupRequest) {
    
    long startNanos = System.nanoTime();
    ProvisioningGroup targetGroup = targetDaoDeleteGroupRequest.getTargetGroup();

    try {
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
    } catch (Exception e) {
      targetGroup.setProvisioned(false);
      for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetGroup.getInternal_objectChanges())) {
        provisioningObjectChange.setProvisioned(false);
      }
      throw e;
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
      
      Map<LdapModificationItem, ProvisioningObjectChange> ldapModificationItems = new LinkedHashMap<LdapModificationItem, ProvisioningObjectChange>();
          
      boolean hasRenameFailure = false;
      
      for (ProvisioningObjectChange provisionObjectChange : provisionObjectChanges) {
        
        String attributeName = provisionObjectChange.getAttributeName();
        String fieldName = provisionObjectChange.getFieldName();
        ProvisioningObjectChangeAction action = provisionObjectChange.getProvisioningObjectChangeAction();
        Object newValue = provisionObjectChange.getNewValue();
        Object oldValue = provisionObjectChange.getOldValue();
        
        if (newValue != null && !(newValue instanceof byte[])) {
          newValue = GrouperUtil.stringValue(newValue);
          if (StringUtils.isEmpty((String)newValue)) {
            newValue = null;
          }
        }
        
        if (oldValue != null && !(oldValue instanceof byte[])) {
          oldValue = GrouperUtil.stringValue(oldValue);
          if (StringUtils.isEmpty((String)oldValue)) {
            oldValue = null;
          }
        }
        
        if (attributeName == null && "name".equals(fieldName) && action == ProvisioningObjectChangeAction.update) {
          // this is a rename
          try {
            new LdapSyncDaoForLdap().move(ldapConfigId, (String)oldValue, (String)newValue);
            provisionObjectChange.setProvisioned(true);
          } catch (Exception e) {
            provisionObjectChange.setProvisioned(false);
            provisionObjectChange.setException(e);
            targetGroup.setProvisioned(false);
            hasRenameFailure = true;
          }
        } else if (attributeName == null) {
          throw new RuntimeException("Unexpected update for attributeName=" + attributeName + ", fieldName=" + fieldName + ", action=" + action);
        } else if (action == ProvisioningObjectChangeAction.delete) {
          if (newValue != null) {
            throw new RuntimeException("Deleting value but there's a new value=" + newValue + ", attributeName=" + attributeName);
          }
                  
          if (oldValue == null) {
            // delete the whole attribute
            LdapModificationItem item = new LdapModificationItem(LdapModificationType.REMOVE_ATTRIBUTE, new LdapAttribute(attributeName));
            ldapModificationItems.put(item, provisionObjectChange);
          } else {
            LdapModificationItem item = new LdapModificationItem(LdapModificationType.REMOVE_ATTRIBUTE, new LdapAttribute(attributeName, oldValue));
            ldapModificationItems.put(item, provisionObjectChange);
          }
        } else if (action == ProvisioningObjectChangeAction.update) {
          if (oldValue != null) {
            LdapModificationItem item = new LdapModificationItem(LdapModificationType.REMOVE_ATTRIBUTE, new LdapAttribute(attributeName, oldValue));
            ldapModificationItems.put(item, provisionObjectChange);
          }
          
          if (newValue != null) {
            LdapModificationItem item = new LdapModificationItem(LdapModificationType.ADD_ATTRIBUTE, new LdapAttribute(attributeName, newValue));
            ldapModificationItems.put(item, provisionObjectChange);
          }
        } else if (action == ProvisioningObjectChangeAction.insert) {
          if (oldValue != null) {
            throw new RuntimeException("Inserting value but there's an old value=" + oldValue + ", attributeName=" + attributeName);
          }
          
          if (newValue == null) {
            throw new RuntimeException("Inserting value but there's no new value for attributeName=" + attributeName);
          }
          
          LdapModificationItem item = new LdapModificationItem(LdapModificationType.ADD_ATTRIBUTE, new LdapAttribute(attributeName, newValue));
          ldapModificationItems.put(item, provisionObjectChange);
        } else {
          throw new RuntimeException("Unexpected provisioningObjectChangeAction: " + action);
        }
      }
  
      LdapModificationResult result = new LdapSyncDaoForLdap().modify(ldapConfigId, targetGroup.getName(), new ArrayList<LdapModificationItem>(ldapModificationItems.keySet()));
      
      if (!hasRenameFailure) {
        targetGroup.setProvisioned(true);  // assume true to start with
      }
      
      if (result.isSuccess()) {
        for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetGroup.getInternal_objectChanges())) {
          if (provisioningObjectChange.getProvisioned() == null) {
            provisioningObjectChange.setProvisioned(true);
          }
        }
      } else {        
        // need to see what actually failed
        for (LdapModificationAttributeError attributeError : result.getAttributeErrors()) {
          ProvisioningObjectChange provisionObjectChange = ldapModificationItems.get(attributeError.getLdapModificationItem());
          if (provisionObjectChange == null) {
            // strange?
            targetGroup.setProvisioned(false);
            LOG.warn("Couldn't find provisionObjectChange to add error for attribute: " + attributeError.getLdapModificationItem().getAttribute().getName());
          } else {
            provisionObjectChange.setProvisioned(false);
            provisionObjectChange.setException(attributeError.getError());
            
            // this should go in the framework?
            if (!provisionObjectChange.getAttributeName().equalsIgnoreCase(ldapSyncConfiguration.getGroupAttributeNameForMemberships())) {
              targetGroup.setProvisioned(false);
            }
          }
        }
        
        for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetGroup.getInternal_objectChanges())) {
          if (provisioningObjectChange.getProvisioned() == null && provisioningObjectChange.getException() == null) {
            provisioningObjectChange.setProvisioned(true);
          }
        }
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
    Set<String> groupSearchAttributeNames = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
    groupSearchAttributeNames.addAll(ldapSyncConfiguration.getGroupSelectAttributes());
      
    Set<String> groupAttributesMultivalued = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
    if (ldapSyncConfiguration.getGroupAttributesMultivalued() != null) {
      groupAttributesMultivalued.addAll(ldapSyncConfiguration.getGroupAttributesMultivalued());
    }
    
    groupSearchAttributeNames.add("objectClass");
    groupAttributesMultivalued.add("objectClass");
    
    String groupAttributeNameForMemberships = ldapSyncConfiguration.getGroupAttributeNameForMemberships();
    if (!StringUtils.isBlank(groupAttributeNameForMemberships)) {
      if (includeAllMembershipsIfApplicable) {
        groupSearchAttributeNames.add(groupAttributeNameForMemberships);
        groupAttributesMultivalued.add(groupAttributeNameForMemberships);
      } else {
        groupSearchAttributeNames.remove(groupAttributeNameForMemberships);
        groupAttributesMultivalued.remove(groupAttributeNameForMemberships);
      }
    }
    
    List<ProvisioningGroup> targetGroups = targetDaoRetrieveGroupsRequest.getTargetGroups();
    int batchSize = LdapConfiguration.getConfig(ldapConfigId).getQueryBatchSize();
    int numberOfBatches = GrouperUtil.batchNumberOfBatches(targetGroups.size(), batchSize);

    for (int i = 0; i < numberOfBatches; i++) {
      long startNanos = System.nanoTime();

      try {
        List<ProvisioningGroup> currentBatchTargetGroups = GrouperUtil.batchList(targetGroups, batchSize, i);
        StringBuilder filterBuilder = new StringBuilder();
        for (ProvisioningGroup targetGroup : currentBatchTargetGroups) {
          String searchFilter = targetGroup.getSearchFilter();
          if (StringUtils.isBlank(searchFilter)) {
            List<GrouperProvisioningConfigurationAttribute> grouperProvisioningConfigurationAttributes = ldapSyncConfiguration.getGroupSearchAttributes();
            if (grouperProvisioningConfigurationAttributes.size() > 1) {
              //TODO add this to validation
              throw new RuntimeException("Can currently only have one searchAttribute! " + grouperProvisioningConfigurationAttributes);
            }
            
            if (grouperProvisioningConfigurationAttributes.size() == 1) {
              
              GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute = grouperProvisioningConfigurationAttributes.get(0);
              String value = targetGroup.retrieveFieldOrAttributeValueString(grouperProvisioningConfigurationAttribute);
              searchFilter = "(" + grouperProvisioningConfigurationAttribute.getName() + "=" + GrouperUtil.ldapFilterEscape(value) + ")";

            } else {
              throw new RuntimeException("Why is groupSearchFilter empty?");
            }
          }
          filterBuilder.append(searchFilter);

        }

        String filter = filterBuilder.toString();
        
        if (currentBatchTargetGroups.size() > 1) {
          filter = "(|" + filter + ")";
        }
        
        List<LdapEntry> ldapEntries = new LdapSyncDaoForLdap().search(ldapConfigId, groupSearchBaseDn, filter, LdapSearchScope.SUBTREE_SCOPE, new ArrayList<String>(groupSearchAttributeNames));
        
        for (LdapEntry ldapEntry : ldapEntries) {
          ProvisioningGroup targetGroup = new ProvisioningGroup();
          targetGroup.setName(ldapEntry.getDn());
          
          for (LdapAttribute ldapAttribute : ldapEntry.getAttributes()) {
            if (ldapAttribute.getValues().size() > 0) {
              Object value = null;
              if (groupAttributesMultivalued.contains(ldapAttribute.getName())) {
                value = new HashSet<Object>(ldapAttribute.getValues());
              } else if (ldapAttribute.getValues().size() == 1) {
                value = ldapAttribute.getValues().iterator().next();
              }
              
              targetGroup.assignAttributeValue(ldapAttribute.getName(), value);
            }
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
      String userSearchAllFilter = ldapSyncConfiguration.getEntitySearchAllFilter();
      
      if (StringUtils.isEmpty(userSearchAllFilter)) {
        throw new RuntimeException("Why is userSearchAllFilter empty?");
      }
  
      String userSearchBaseDn = ldapSyncConfiguration.getUserSearchBaseDn();
  
      Set<String> entitySearchAttributeNames = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
      entitySearchAttributeNames.addAll(ldapSyncConfiguration.getEntitySelectAttributes());
        
      Set<String> userAttributesMultivalued = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
      if (ldapSyncConfiguration.getEntityAttributesMultivalued() != null) {
        userAttributesMultivalued.addAll(ldapSyncConfiguration.getEntityAttributesMultivalued());
      }
      
      entitySearchAttributeNames.add("objectClass");
      userAttributesMultivalued.add("objectClass");
      
      String userAttributeNameForMemberships = ldapSyncConfiguration.getEntityAttributeNameForMemberships();
      if (!StringUtils.isBlank(userAttributeNameForMemberships)) {
        if (includeAllMembershipsIfApplicable) {
          entitySearchAttributeNames.add(userAttributeNameForMemberships);
          userAttributesMultivalued.add(userAttributeNameForMemberships);
        } else {
          entitySearchAttributeNames.remove(userAttributeNameForMemberships);
          userAttributesMultivalued.remove(userAttributeNameForMemberships);
        }
      }
      
      LdapSyncDaoForLdap ldapSyncDaoForLdap = new LdapSyncDaoForLdap();
      GrouperProvisioningDiagnosticsContainer grouperProvisioningDiagnosticsContainer = this.getGrouperProvisioner().retrieveGrouperProvisioningDiagnosticsContainer();
      ldapSyncDaoForLdap.assignDebug(grouperProvisioningDiagnosticsContainer.isInDiagnostics());
      List<LdapEntry> ldapEntries = ldapSyncDaoForLdap.search(ldapConfigId, userSearchBaseDn, userSearchAllFilter, LdapSearchScope.SUBTREE_SCOPE, new ArrayList<String>(entitySearchAttributeNames));
      if (grouperProvisioningDiagnosticsContainer.isInDiagnostics()) {
        grouperProvisioningDiagnosticsContainer.appendReportLineIfNotBlank(ldapSyncDaoForLdap.getDebugLog().toString());
      }
      
      for (LdapEntry ldapEntry : ldapEntries) {
        ProvisioningEntity targetEntity = new ProvisioningEntity();
        targetEntity.setName(ldapEntry.getDn());
        
        for (LdapAttribute ldapAttribute : ldapEntry.getAttributes()) {
          if (ldapAttribute.getValues().size() > 0) {
            Object value = null;
            if (userAttributesMultivalued.contains(ldapAttribute.getName())) {
              value = new HashSet<Object>(ldapAttribute.getValues());
            } else if (ldapAttribute.getValues().size() == 1) {
              value = ldapAttribute.getValues().iterator().next();
            }
            
            targetEntity.assignAttributeValue(ldapAttribute.getName(), value);
          }
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
    
    Set<String> entitySelectAttributeNames = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
    entitySelectAttributeNames.addAll(ldapSyncConfiguration.getEntitySelectAttributes());
      
    Set<String> userAttributesMultivalued = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
    if (ldapSyncConfiguration.getEntityAttributesMultivalued() != null) {
      userAttributesMultivalued.addAll(ldapSyncConfiguration.getEntityAttributesMultivalued());
    }
    
    entitySelectAttributeNames.add("objectClass");
    userAttributesMultivalued.add("objectClass");
    
    String userAttributeNameForMemberships = ldapSyncConfiguration.getEntityAttributeNameForMemberships();
    if (!StringUtils.isBlank(userAttributeNameForMemberships)) {
      if (includeAllMembershipsIfApplicable) {
        entitySelectAttributeNames.add(userAttributeNameForMemberships);
        userAttributesMultivalued.add(userAttributeNameForMemberships);
      } else {
        entitySelectAttributeNames.remove(userAttributeNameForMemberships);
        userAttributesMultivalued.remove(userAttributeNameForMemberships);
      }
    }
    
    List<ProvisioningEntity> targetEntities = targetDaoRetrieveEntitiesRequest.getTargetEntities();
    int batchSize = LdapConfiguration.getConfig(ldapConfigId).getQueryBatchSize();
    int numberOfBatches = GrouperUtil.batchNumberOfBatches(targetEntities.size(), batchSize);

    for (int i = 0; i < numberOfBatches; i++) {
      long startNanos = System.nanoTime();

      try {
        List<ProvisioningEntity> currentBatchTargetEntities = GrouperUtil.batchList(targetEntities, batchSize, i);
        StringBuilder filterBuilder = new StringBuilder();
        for (ProvisioningEntity targetEntity : currentBatchTargetEntities) {
          String searchFilter = targetEntity.getSearchFilter();
          if (StringUtils.isBlank(searchFilter)) {
            List<GrouperProvisioningConfigurationAttribute> grouperProvisioningConfigurationAttributes = ldapSyncConfiguration.getEntitySearchAttributes();
            if (grouperProvisioningConfigurationAttributes.size() > 1) {
              //TODO add this to validation
              throw new RuntimeException("Can currently only have one searchAttribute! " + grouperProvisioningConfigurationAttributes);
            }
            
            if (grouperProvisioningConfigurationAttributes.size() == 1) {
              
              GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute = grouperProvisioningConfigurationAttributes.get(0);
              String value = targetEntity.retrieveFieldOrAttributeValueString(grouperProvisioningConfigurationAttribute);
              searchFilter = "(" + grouperProvisioningConfigurationAttribute + "=" + GrouperUtil.ldapFilterEscape(value) + ")";

            } else {
              throw new RuntimeException("Why is groupSearchFilter empty?");
            }

          }
          filterBuilder.append(searchFilter);
        }
        
        String filter = filterBuilder.toString();
        
        if (currentBatchTargetEntities.size() > 1) {
          filter = "(|" + filter + ")";
        }
        
        List<LdapEntry> ldapEntries = new LdapSyncDaoForLdap().search(ldapConfigId, userSearchBaseDn, filter, LdapSearchScope.SUBTREE_SCOPE, new ArrayList<String>(entitySelectAttributeNames));
        
        for (LdapEntry ldapEntry : ldapEntries) {
          ProvisioningEntity targetEntity = new ProvisioningEntity();
          targetEntity.setName(ldapEntry.getDn());
          
          for (LdapAttribute ldapAttribute : ldapEntry.getAttributes()) {
            if (ldapAttribute.getValues().size() > 0) {
              Object value = null;
              if (userAttributesMultivalued.contains(ldapAttribute.getName())) {
                value = new HashSet<Object>(ldapAttribute.getValues());
              } else if (ldapAttribute.getValues().size() == 1) {
                value = ldapAttribute.getValues().iterator().next();
              }
              
              targetEntity.assignAttributeValue(ldapAttribute.getName(), value);
            }
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
    ProvisioningEntity targetEntity = targetDaoInsertEntityRequest.getTargetEntity();

    try {
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
        
        LdapAttribute ldapAttribute = ldapEntry.getAttribute(provisioningObjectChange.getAttributeName());
        if (ldapAttribute == null) {
          ldapAttribute = new LdapAttribute(provisioningObjectChange.getAttributeName());
        }
  
        if (value instanceof byte[]) {
          ldapAttribute.addValue(value);
        } else if (value instanceof Collection) {
          @SuppressWarnings("unchecked")
          Collection<Object> values = (Collection<Object>) provisioningObjectChange.getNewValue();
          for (Object singleValue : values) {
            if (singleValue instanceof byte[]) {
              ldapAttribute.addValue(singleValue);
            } else {
              String singleStringValue = GrouperUtil.stringValue(singleValue);
              if (!StringUtils.isEmpty(singleStringValue)) {
                ldapAttribute.addValue(singleStringValue);
              }
            }
          }
        } else {
          String stringValue = GrouperUtil.stringValue(value);
          if (!StringUtils.isEmpty(stringValue)) {
            ldapAttribute.addValue(stringValue);
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
    } catch (Exception e) {
      targetEntity.setProvisioned(false);
      for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetEntity.getInternal_objectChanges())) {
        provisioningObjectChange.setProvisioned(false);
      }
      
      throw e;
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("insertEntity", startNanos));
    }
  }
  
  @Override
  public void registerGrouperProvisionerDaoCapabilities(
      GrouperProvisionerDaoCapabilities grouperProvisionerDaoCapabilities) {
    grouperProvisionerDaoCapabilities.setCanDeleteEntity(true);
    grouperProvisionerDaoCapabilities.setCanDeleteGroup(true);
    grouperProvisionerDaoCapabilities.setCanInsertGroup(true);
    grouperProvisionerDaoCapabilities.setCanRetrieveAllGroups(true);
    grouperProvisionerDaoCapabilities.setCanRetrieveGroups(true);
    grouperProvisionerDaoCapabilities.setCanRetrieveGroupWithOrWithoutMembershipAttribute(true);
    grouperProvisionerDaoCapabilities.setCanUpdateEntity(true);
    grouperProvisionerDaoCapabilities.setCanUpdateGroup(true);
    grouperProvisionerDaoCapabilities.setCanUpdateGroupMembershipAttribute(true);
    grouperProvisionerDaoCapabilities.setCanRetrieveAllEntities(true);
    grouperProvisionerDaoCapabilities.setCanRetrieveEntities(true);
    grouperProvisionerDaoCapabilities.setCanRetrieveEntityWithOrWithoutMembershipAttribute(true);
    grouperProvisionerDaoCapabilities.setCanInsertEntity(true);

  }

  @Override
  public TargetDaoDeleteEntityResponse deleteEntity(TargetDaoDeleteEntityRequest targetDaoDeleteEntityRequest) {
    
    long startNanos = System.nanoTime();
    ProvisioningEntity targetEntity = targetDaoDeleteEntityRequest.getTargetEntity();
  
    try {
      LdapSyncConfiguration ldapSyncConfiguration = (LdapSyncConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
      String ldapConfigId = ldapSyncConfiguration.getLdapExternalSystemConfigId();
      
      if (StringUtils.isBlank(targetEntity.getName())) {
        throw new RuntimeException("Why is targetEntity.getName() blank?");
      }
      new LdapSyncDaoForLdap().delete(ldapConfigId, targetEntity.getName());
      targetEntity.setProvisioned(true);
      for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetEntity.getInternal_objectChanges())) {
        provisioningObjectChange.setProvisioned(true);
      }
      return new TargetDaoDeleteEntityResponse();
    } catch (Exception e) {
      targetEntity.setProvisioned(false);
      for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetEntity.getInternal_objectChanges())) {
        provisioningObjectChange.setProvisioned(false);
      }
      throw e;
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("deleteEntity", startNanos));
    }
  }

  public TargetDaoUpdateEntityResponse updateEntity(TargetDaoUpdateEntityRequest targetDaoUpdateEntityRequest) {
  
    long startNanos = System.nanoTime();
  
    try {
      ProvisioningEntity targetEntity = targetDaoUpdateEntityRequest.getTargetEntity();
      Set<ProvisioningObjectChange> provisionObjectChanges = targetEntity.getInternal_objectChanges();
  
      LdapSyncConfiguration ldapSyncConfiguration = (LdapSyncConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
      String ldapConfigId = ldapSyncConfiguration.getLdapExternalSystemConfigId();
      
      Map<LdapModificationItem, ProvisioningObjectChange> ldapModificationItems = new LinkedHashMap<LdapModificationItem, ProvisioningObjectChange>();
          
      boolean hasRenameFailure = false;
      
      for (ProvisioningObjectChange provisionObjectChange : provisionObjectChanges) {
        
        String attributeName = provisionObjectChange.getAttributeName();
        String fieldName = provisionObjectChange.getFieldName();
        ProvisioningObjectChangeAction action = provisionObjectChange.getProvisioningObjectChangeAction();
        Object newValue = provisionObjectChange.getNewValue();
        Object oldValue = provisionObjectChange.getOldValue();

        if (newValue != null && !(newValue instanceof byte[])) {
          newValue = GrouperUtil.stringValue(newValue);
          if (StringUtils.isEmpty((String)newValue)) {
            newValue = null;
          }
        }
        
        if (oldValue != null && !(oldValue instanceof byte[])) {
          oldValue = GrouperUtil.stringValue(oldValue);
          if (StringUtils.isEmpty((String)oldValue)) {
            oldValue = null;
          }
        }
        
        if (attributeName == null && "name".equals(fieldName) && action == ProvisioningObjectChangeAction.update) {
          // this is a rename
          try {
            new LdapSyncDaoForLdap().move(ldapConfigId, (String)oldValue, (String)newValue);
            provisionObjectChange.setProvisioned(true);
          } catch (Exception e) {
            provisionObjectChange.setProvisioned(false);
            provisionObjectChange.setException(e);
            targetEntity.setProvisioned(false);
            hasRenameFailure = true;
          }
        } else if (attributeName == null) {
          throw new RuntimeException("Unexpected update for attributeName=" + attributeName + ", fieldName=" + fieldName + ", action=" + action);
        } else if (action == ProvisioningObjectChangeAction.delete) {
          if (newValue != null) {
            throw new RuntimeException("Deleting value but there's a new value=" + newValue + ", attributeName=" + attributeName);
          }
                  
          if (oldValue == null) {
            // delete the whole attribute
            LdapModificationItem item = new LdapModificationItem(LdapModificationType.REMOVE_ATTRIBUTE, new LdapAttribute(attributeName));
            ldapModificationItems.put(item, provisionObjectChange);
          } else {
            LdapModificationItem item = new LdapModificationItem(LdapModificationType.REMOVE_ATTRIBUTE, new LdapAttribute(attributeName, oldValue));
            ldapModificationItems.put(item, provisionObjectChange);
          }
        } else if (action == ProvisioningObjectChangeAction.update) {
          if (oldValue != null) {
            LdapModificationItem item = new LdapModificationItem(LdapModificationType.REMOVE_ATTRIBUTE, new LdapAttribute(attributeName, oldValue));
            ldapModificationItems.put(item, provisionObjectChange);
          }
          
          if (newValue != null) {
            LdapModificationItem item = new LdapModificationItem(LdapModificationType.ADD_ATTRIBUTE, new LdapAttribute(attributeName, newValue));
            ldapModificationItems.put(item, provisionObjectChange);
          }
        } else if (action == ProvisioningObjectChangeAction.insert) {
          if (oldValue != null) {
            throw new RuntimeException("Inserting value but there's an old value=" + oldValue + ", attributeName=" + attributeName);
          }
          
          if (newValue == null) {
            throw new RuntimeException("Inserting value but there's no new value for attributeName=" + attributeName);
          }
          
          LdapModificationItem item = new LdapModificationItem(LdapModificationType.ADD_ATTRIBUTE, new LdapAttribute(attributeName, newValue));
          ldapModificationItems.put(item, provisionObjectChange);
        } else {
          throw new RuntimeException("Unexpected provisioningObjectChangeAction: " + action);
        }
      }
  
      LdapModificationResult result = new LdapSyncDaoForLdap().modify(ldapConfigId, targetEntity.getName(), new ArrayList<LdapModificationItem>(ldapModificationItems.keySet()));
      
      if (!hasRenameFailure) {
        targetEntity.setProvisioned(true);  // assume true to start with
      }
      
      if (result.isSuccess()) {
        for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetEntity.getInternal_objectChanges())) {
          if (provisioningObjectChange.getProvisioned() == null) {
            provisioningObjectChange.setProvisioned(true);
          }
        }
      } else {        
        // need to see what actually failed
        for (LdapModificationAttributeError attributeError : result.getAttributeErrors()) {
          ProvisioningObjectChange provisionObjectChange = ldapModificationItems.get(attributeError.getLdapModificationItem());
          if (provisionObjectChange == null) {
            // strange?
            targetEntity.setProvisioned(false);
            LOG.warn("Couldn't find provisionObjectChange to add error for attribute: " + attributeError.getLdapModificationItem().getAttribute().getName());
          } else {
            provisionObjectChange.setProvisioned(false);
            provisionObjectChange.setException(attributeError.getError());
            
            // this should go in the framework?
            if (!provisionObjectChange.getAttributeName().equalsIgnoreCase(ldapSyncConfiguration.getGroupAttributeNameForMemberships())) {
              targetEntity.setProvisioned(false);
            }
          }
        }
        
        for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetEntity.getInternal_objectChanges())) {
          if (provisioningObjectChange.getProvisioned() == null && provisioningObjectChange.getException() == null) {
            provisioningObjectChange.setProvisioned(true);
          }
        }
      }
      
      return new TargetDaoUpdateEntityResponse();
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("updateEntity", startNanos));
    }
  }

  /*
  public static void main(String[] args) {
    edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioner grouperProvisioner = edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioner.retrieveProvisioner("testLdapProv2");
    grouperProvisioner.retrieveGrouperProvisioningBehavior().setGrouperProvisioningType(edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningType.fullProvisionFull);
    grouperProvisioner.retrieveGrouperProvisioningConfiguration().configureProvisioner();
    grouperProvisioner.retrieveGrouperProvisioningConfiguration().setGroupAttributeNameForMemberships("member");
    ProvisioningGroup targetGroup = new ProvisioningGroup();
    targetGroup.setName("cn=test4:test2,ou=Groups,dc=example,dc=edu");
    
    ProvisioningObjectChange provisioningObjectChange1 = new ProvisioningObjectChange();
    provisioningObjectChange1.setAttributeName("member");
    provisioningObjectChange1.setNewValue("sdfsdf");
    provisioningObjectChange1.setProvisioningObjectChangeAction(ProvisioningObjectChangeAction.insert);
    targetGroup.addInternal_objectChange(provisioningObjectChange1);

    ProvisioningObjectChange provisioningObjectChange2 = new ProvisioningObjectChange();
    provisioningObjectChange2.setAttributeName("member");
    provisioningObjectChange2.setNewValue("uid=test.subject.2,ou=People,dc=example,dc=edu");
    provisioningObjectChange2.setProvisioningObjectChangeAction(ProvisioningObjectChangeAction.insert);
    targetGroup.addInternal_objectChange(provisioningObjectChange2);
    
    LdapProvisioningTargetDao dao = new LdapProvisioningTargetDao();
    dao.setGrouperProvisioner(grouperProvisioner);
    TargetDaoUpdateGroupRequest targetDaoUpdateGroupRequest = new TargetDaoUpdateGroupRequest();
    targetDaoUpdateGroupRequest.setTargetGroup(targetGroup);;
    dao.updateGroup(targetDaoUpdateGroupRequest);
    
    System.out.println("results:");
    System.out.println("1: " + provisioningObjectChange1.getProvisioned() + " - " + (provisioningObjectChange1.getException() == null ? null : provisioningObjectChange1.getException().getMessage()));
    System.out.println("2: " + provisioningObjectChange2.getProvisioned() + " - " + (provisioningObjectChange2.getException() == null ? null : provisioningObjectChange2.getException().getMessage()));
    System.out.println("Target group result: " + targetGroup.getProvisioned());
    System.out.println("results done");
  }
  */
}
