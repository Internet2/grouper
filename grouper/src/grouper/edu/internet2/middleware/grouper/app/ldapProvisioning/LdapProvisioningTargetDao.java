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
import org.ldaptive.LdapException;
import org.ldaptive.ResultCode;

import com.unboundid.ldap.sdk.DN;
import com.unboundid.ldap.sdk.LDAPException;

import edu.internet2.middleware.grouper.app.ldapProvisioning.ldapSyncDao.LdapSyncDaoForLdap;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningBehaviorMembershipType;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfigurationAttribute;
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
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveMembershipRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveMembershipResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveMembershipsByEntitiesRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveMembershipsByEntitiesResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveMembershipsByGroupsRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveMembershipsByGroupsResponse;
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
import edu.internet2.middleware.grouper.ldap.LdapSessionUtils;
import edu.internet2.middleware.grouper.util.GrouperUtil;


public class LdapProvisioningTargetDao extends GrouperProvisionerTargetDaoBase {

  private static final Log LOG = GrouperUtil.getLog(LdapProvisioningTargetDao.class);

  /**
   * start logging the source low level actions
   * @return true if the logging was started (i.e. can be stopped), or false if already started (in which case 
   * somewhere up the stack with stop it so dont stop it)
   */
  @Override
  public boolean loggingStart() {
    return LdapSessionUtils.logStart();
  }

  /**
   * stop logging and return
   */
  @Override
  public String loggingStop() {
    return LdapSessionUtils.logEnd();
  }

  /**
   * look at object classes and the search attribute
   * @return the filter
   */
  public String generateGroupSearchAllFilter() {
    
    LdapSyncConfiguration ldapSyncConfiguration = (LdapSyncConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();

    StringBuilder filterBuilder = new StringBuilder();

    // get the search attribute
    List<GrouperProvisioningConfigurationAttribute> grouperProvisioningConfigurationAttributes = ldapSyncConfiguration.getGroupSearchAttributes();
    
    Collection<String> objectClasses = null;
    // see if there are object classes
    for (GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute : GrouperUtil.nonNull(ldapSyncConfiguration.getTargetGroupAttributeNameToConfig()).values()) {
      if (StringUtils.equalsIgnoreCase("objectclass", grouperProvisioningConfigurationAttribute.getName())) {
        // lets try to evaluate the scriptlet and static values to get the object classes
        if (!StringUtils.isBlank(grouperProvisioningConfigurationAttribute.getTranslateExpression())) {
          Object objectClassResult = this.getGrouperProvisioner().retrieveGrouperProvisioningTranslator()
              .runScript(grouperProvisioningConfigurationAttribute.getTranslateExpression(), null);
          if (objectClassResult instanceof String) {
            objectClasses = GrouperUtil.splitTrimToSet((String)objectClassResult, ",");
          } else {
            objectClasses = (Collection<String>)objectClassResult;
          }
          break;
        } else if (!StringUtils.isBlank(grouperProvisioningConfigurationAttribute.getTranslateFromStaticValues())) {
          objectClasses = GrouperUtil.splitTrimToSet(grouperProvisioningConfigurationAttribute.getTranslateFromStaticValues(), ",");
          break;
        }
      }
    }
    
    int numberOfConditions = GrouperUtil.length(objectClasses) + grouperProvisioningConfigurationAttributes.size();
    
    if (numberOfConditions > 1) {
      filterBuilder.append("(&");
    }

    for (GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute : grouperProvisioningConfigurationAttributes) {
      if (StringUtils.equals(grouperProvisioningConfigurationAttribute.getName(), ldap_dn)) {
        filterBuilder.append("(" + GrouperUtil.ldapFilterEscape(ldapSyncConfiguration.getGroupRdnAttribute()) + "=*)");
      } else {
        filterBuilder.append("(" + GrouperUtil.ldapFilterEscape(grouperProvisioningConfigurationAttribute.getName()) + "=*)");
      }
    }
    for (String objectClass : GrouperUtil.nonNull(objectClasses)) {
      filterBuilder.append("(objectclass=").append(GrouperUtil.ldapFilterEscape(objectClass)).append(")");
    }
    
    if (numberOfConditions > 1) {
      filterBuilder.append(")");
    }
    
    return filterBuilder.toString();
  }
  
  /**
   * ldap dn attribute name
   */
  public static final String ldap_dn = "ldap_dn";
  
  @Override
  public TargetDaoRetrieveAllGroupsResponse retrieveAllGroups(TargetDaoRetrieveAllGroupsRequest targetDaoRetrieveAllGroupsRequest) {
    
    LdapSyncConfiguration ldapSyncConfiguration = (LdapSyncConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
    if (ldapSyncConfiguration.isOnlyLdapGroupDnOverride()) {
      return new TargetDaoRetrieveAllGroupsResponse();
    }
    
    long startNanos = System.nanoTime();

    try {
      boolean includeAllMembershipsIfApplicable = targetDaoRetrieveAllGroupsRequest == null ? false : targetDaoRetrieveAllGroupsRequest.isIncludeAllMembershipsIfApplicable();
      
      List<ProvisioningGroup> results = new ArrayList<ProvisioningGroup>();
      
      String ldapConfigId = ldapSyncConfiguration.getLdapExternalSystemConfigId();
      String groupSearchAllFilter = ldapSyncConfiguration.getGroupSearchAllFilter();
      
      if (StringUtils.isEmpty(groupSearchAllFilter)) {
        groupSearchAllFilter = this.generateGroupSearchAllFilter();
      }
  
      String groupSearchBaseDn = ldapSyncConfiguration.getGroupSearchBaseDn();

      if (!StringUtils.isBlank(groupSearchBaseDn)) {
        Set<String> groupSearchAttributeNames = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
        groupSearchAttributeNames.addAll(ldapSyncConfiguration.getGroupSelectAttributes());
          
        Set<String> groupAttributesMultivalued = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
        if (ldapSyncConfiguration.getGroupAttributesMultivalued() != null) {
          groupAttributesMultivalued.addAll(ldapSyncConfiguration.getGroupAttributesMultivalued());
        }
        
        //groupSearchAttributeNames.add("objectClass");
        groupAttributesMultivalued.add("objectClass");
        
        String groupAttributeNameForMemberships = ldapSyncConfiguration.getGroupMembershipAttributeName();
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
  
        List<LdapEntry> ldapEntries = ldapSyncDaoForLdap.search(ldapConfigId, groupSearchBaseDn, groupSearchAllFilter, LdapSearchScope.SUBTREE_SCOPE, new ArrayList<String>(groupSearchAttributeNames));
        for (LdapEntry ldapEntry : ldapEntries) {
          ProvisioningGroup targetGroup = new ProvisioningGroup();
          targetGroup.assignAttributeValue(ldap_dn, ldapEntry.getDn());
          
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
      
      if (StringUtils.isBlank(targetGroup.retrieveAttributeValueString(ldap_dn))) {
        throw new RuntimeException("Why is targetGroup.retrieveAttributeValueString(ldap_dn) blank?");
      }
      
      LdapEntry ldapEntry = new LdapEntry(targetGroup.retrieveAttributeValueString(ldap_dn));
      for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetGroup.getInternal_objectChanges())) {
        Object value = provisioningObjectChange.getNewValue();
        
        if (LdapProvisioningTargetDao.ldap_dn.equals(provisioningObjectChange.getAttributeName())) {
          // update the ldap entry dn just in case it's different
          ldapEntry.setDn((String)provisioningObjectChange.getNewValue());
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
              if (singleStringValue != null) {
                ldapAttribute.addValue(singleStringValue);
              }
            }
          }
        } else {
          String stringValue = GrouperUtil.stringValue(value);
          if (stringValue != null) {
            ldapAttribute.addValue(stringValue);
          }
        }
        
        if (ldapAttribute.getValues().size() > 0) {
          ldapEntry.addAttribute(ldapAttribute);
        }
      }
      
      LdapSyncDaoForLdap ldapSyncDaoForLdap = new LdapSyncDaoForLdap();
      
      try {
        ldapSyncDaoForLdap.create(ldapConfigId, ldapEntry);
      } catch (Exception e) {
        if (e.getCause() != null && e.getCause() instanceof LdapException && ((LdapException)e.getCause()).getResultCode() == ResultCode.NO_SUCH_OBJECT) {
          createParentFolders(ldapSyncConfiguration, ldapSyncDaoForLdap, ldapEntry.getDn());
          ldapSyncDaoForLdap.create(ldapConfigId, ldapEntry);
        } else {
          throw e;
        }
      }
      
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
      
      if (StringUtils.isBlank(targetGroup.retrieveAttributeValueString(ldap_dn))) {
        throw new RuntimeException("Why is targetGroup.retrieveAttributeValueString(ldap_dn) blank?");
      }
      LdapSyncDaoForLdap ldapSyncDaoForLdap = new LdapSyncDaoForLdap();
      ldapSyncDaoForLdap.delete(ldapConfigId, targetGroup.retrieveAttributeValueString(ldap_dn));
      
      deleteEmptyParentFolders(ldapSyncConfiguration, ldapSyncDaoForLdap, targetGroup.retrieveAttributeValueString(ldap_dn));

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
      List<Exception> exceptions = new ArrayList<Exception>();

      String dn = targetGroup.retrieveAttributeValueString(ldap_dn);
      if (targetGroup.getProvisioningGroupWrapper() != null && targetGroup.getProvisioningGroupWrapper().getTargetProvisioningGroup() != null
          && !GrouperUtil.isBlank(targetGroup.getProvisioningGroupWrapper().getTargetProvisioningGroup().retrieveAttributeValueString(ldap_dn))) {
        dn = targetGroup.getProvisioningGroupWrapper().getTargetProvisioningGroup().retrieveAttributeValueString(ldap_dn);
      }

      boolean movedDn = false;
      for (ProvisioningObjectChange provisionObjectChange : provisionObjectChanges) {
        
        String attributeName = provisionObjectChange.getAttributeName();
        ProvisioningObjectChangeAction action = provisionObjectChange.getProvisioningObjectChangeAction();
        Object newValue = provisionObjectChange.getNewValue();
        Object oldValue = provisionObjectChange.getOldValue();
        
        if (newValue != null && !(newValue instanceof byte[])) {
          newValue = GrouperUtil.stringValue(newValue);
        }
        
        if (oldValue != null && !(oldValue instanceof byte[])) {
          oldValue = GrouperUtil.stringValue(oldValue);
        }
        
        if (attributeName != null && LdapProvisioningTargetDao.ldap_dn.equals(attributeName) && action == ProvisioningObjectChangeAction.update) {
          // this is a rename
          try {
            checkParentFolderCaseChanges(ldapSyncConfiguration, (String)oldValue, (String)newValue);
            
            LdapSyncDaoForLdap ldapSyncDaoForLdap = new LdapSyncDaoForLdap();
            
            try {
              ldapSyncDaoForLdap.move(ldapConfigId, (String)oldValue, (String)newValue);
            } catch (Exception e) {
              if (e.getCause() != null && e.getCause() instanceof LdapException && ((LdapException)e.getCause()).getResultCode() == ResultCode.NO_SUCH_OBJECT) {
                createParentFolders(ldapSyncConfiguration, ldapSyncDaoForLdap, (String)newValue);
                ldapSyncDaoForLdap.move(ldapConfigId, (String)oldValue, (String)newValue);
              } else {
                throw e;
              }
            }

            movedDn = true;
            dn = (String)newValue;
            deleteEmptyParentFolders(ldapSyncConfiguration, ldapSyncDaoForLdap, (String)oldValue);
            
            provisionObjectChange.setProvisioned(true);
          } catch (Exception e) {
            provisionObjectChange.setProvisioned(false);
            provisionObjectChange.setException(e);
            targetGroup.setProvisioned(false);
            hasRenameFailure = true;
            exceptions.add(e);
          }
        } else if (attributeName == null) {
          throw new RuntimeException("Unexpected update for attributeName=" + attributeName + ", action=" + action);
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

//            // keep track of default value so it is not removed first
//            GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute = this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getTargetGroupAttributeNameToConfig().get(attributeName);
//            item.setDefaultAttributeValue(StringUtils.equals(GrouperUtil.stringValue(oldValue), grouperProvisioningConfigurationAttribute.getDefaultValue()));

          }
        } else if (action == ProvisioningObjectChangeAction.update) {

          // the rdn was already changed
          if (movedDn && dn.startsWith(attributeName+"=")) {
            continue;
          }
          
          if (oldValue != null) {
            LdapModificationItem item = new LdapModificationItem(LdapModificationType.REMOVE_ATTRIBUTE, new LdapAttribute(attributeName, oldValue));
            ldapModificationItems.put(item, provisionObjectChange);
            
//            // keep track of default value so it is not removed first
//            GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute = this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getTargetGroupAttributeNameToConfig().get(attributeName);
//            item.setDefaultAttributeValue(StringUtils.equals(GrouperUtil.stringValue(oldValue), grouperProvisioningConfigurationAttribute.getDefaultValue()));
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
  
      LdapSyncDaoForLdap ldapSyncDaoForLdap = new LdapSyncDaoForLdap();
      LdapModificationResult result = ldapSyncDaoForLdap.modify(ldapConfigId, dn, new ArrayList<LdapModificationItem>(ldapModificationItems.keySet()));
      
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
            targetGroup.setProvisioned(false);
            exceptions.add(attributeError.getError());
          }
        }
        
        for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetGroup.getInternal_objectChanges())) {
          if (provisioningObjectChange.getProvisioned() == null && provisioningObjectChange.getException() == null) {
            provisioningObjectChange.setProvisioned(true);
          }
        }
      }
      
      if (exceptions.size() > 0) {
        throw new RuntimeException("There were " + exceptions.size() + " exceptions, throwing first exception", exceptions.get(0));
      }
      
      return new TargetDaoUpdateGroupResponse();
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("updateGroup", startNanos));
    }
  }
  
  /**
   * @param dn
   * @param includeAllMemberships
   * @param exceptionIfNotFound
   * @return provisioning group
   */
  public ProvisioningGroup retrieveGroupByDn(String dn, boolean includeAllMemberships, boolean exceptionIfNotFound) {
    
    LdapSyncConfiguration ldapSyncConfiguration = (LdapSyncConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
    String ldapConfigId = ldapSyncConfiguration.getLdapExternalSystemConfigId();
    
    Set<String> groupSearchAttributeNames = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
    groupSearchAttributeNames.addAll(ldapSyncConfiguration.getGroupSelectAttributes());
      
    Set<String> groupAttributesMultivalued = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
    if (ldapSyncConfiguration.getGroupAttributesMultivalued() != null) {
      groupAttributesMultivalued.addAll(ldapSyncConfiguration.getGroupAttributesMultivalued());
    }
    
    groupAttributesMultivalued.add("objectClass");
    
    String groupAttributeNameForMemberships = ldapSyncConfiguration.getGroupMembershipAttributeName();
    if (!StringUtils.isBlank(groupAttributeNameForMemberships)) {
      if (includeAllMemberships) {
        groupSearchAttributeNames.add(groupAttributeNameForMemberships);
        groupAttributesMultivalued.add(groupAttributeNameForMemberships);
      } else {
        groupSearchAttributeNames.remove(groupAttributeNameForMemberships);
        groupAttributesMultivalued.remove(groupAttributeNameForMemberships);
      }
    }
    
    long startNanos = System.nanoTime();

    try {
      
      LdapSyncDaoForLdap ldapSyncDaoForLdap = new LdapSyncDaoForLdap();
      List<LdapEntry> ldapEntries = new ArrayList<LdapEntry>();
      
      try {
        ldapEntries = ldapSyncDaoForLdap.search(ldapConfigId, dn, "(objectclass=*)", LdapSearchScope.OBJECT_SCOPE, new ArrayList<String>(groupSearchAttributeNames));
      } catch (Exception e) {
        if (exceptionIfNotFound) {
          throw e;
        }
        
        if (e.getCause() != null && e.getCause() instanceof LdapException && ((LdapException)e.getCause()).getResultCode() == ResultCode.NO_SUCH_OBJECT) {
          return null;
        } else {
          throw e;
        }
      }
      
      if (GrouperUtil.length(ldapEntries) == 0) {
        return null;
      }
      if (GrouperUtil.length(ldapEntries) == 1) {
        LdapEntry ldapEntry = ldapEntries.get(0);
        ProvisioningGroup targetGroup = new ProvisioningGroup();
        targetGroup.assignAttributeValue(ldap_dn, ldapEntry.getDn());
        
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

        return targetGroup;

      }

    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("retrieveGroups", startNanos));
    }
    throw new RuntimeException("Why are we here?");

  }
  
  /**
   * @param dn
   * @param includeAllMemberships
   * @param exceptionIfNotFound
   * @return provisioning entity
   */
  public ProvisioningEntity retrieveEntityByDn(String dn, boolean includeAllMemberships, boolean exceptionIfNotFound) {
    
    LdapSyncConfiguration ldapSyncConfiguration = (LdapSyncConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
    String ldapConfigId = ldapSyncConfiguration.getLdapExternalSystemConfigId();
    
    Set<String> entitySearchAttributeNames = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
    entitySearchAttributeNames.addAll(ldapSyncConfiguration.getEntitySelectAttributes());
      
    Set<String> entityAttributesMultivalued = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
    if (ldapSyncConfiguration.getEntityAttributesMultivalued() != null) {
      entityAttributesMultivalued.addAll(ldapSyncConfiguration.getEntityAttributesMultivalued());
    }
    
    entityAttributesMultivalued.add("objectClass");
    
    String entityAttributeNameForMemberships = ldapSyncConfiguration.getEntityMembershipAttributeName();
    if (!StringUtils.isBlank(entityAttributeNameForMemberships)) {
      if (includeAllMemberships) {
        entitySearchAttributeNames.add(entityAttributeNameForMemberships);
        entityAttributesMultivalued.add(entityAttributeNameForMemberships);
      } else {
        entitySearchAttributeNames.remove(entityAttributeNameForMemberships);
        entityAttributesMultivalued.remove(entityAttributeNameForMemberships);
      }
    }
    
    long startNanos = System.nanoTime();

    try {
      
      LdapSyncDaoForLdap ldapSyncDaoForLdap = new LdapSyncDaoForLdap();
      List<LdapEntry> ldapEntries = new ArrayList<LdapEntry>();
      
      try {
        ldapEntries = ldapSyncDaoForLdap.search(ldapConfigId, dn, "(objectclass=*)", LdapSearchScope.OBJECT_SCOPE, new ArrayList<String>(entitySearchAttributeNames));
      } catch (Exception e) {
        if (exceptionIfNotFound) {
          throw e;
        }
        
        if (e.getCause() != null && e.getCause() instanceof LdapException && ((LdapException)e.getCause()).getResultCode() == ResultCode.NO_SUCH_OBJECT) {
          return null;
        } else {
          throw e;
        }
      }
      
      if (GrouperUtil.length(ldapEntries) == 0) {
        return null;
      }
      if (GrouperUtil.length(ldapEntries) == 1) {
        LdapEntry ldapEntry = ldapEntries.get(0);
        ProvisioningEntity targetEntity = new ProvisioningEntity();
        targetEntity.assignAttributeValue(ldap_dn, ldapEntry.getDn());
        
        for (LdapAttribute ldapAttribute : ldapEntry.getAttributes()) {
          if (ldapAttribute.getValues().size() > 0) {
            Object value = null;
            if (entityAttributesMultivalued.contains(ldapAttribute.getName())) {
              value = new HashSet<Object>(ldapAttribute.getValues());
            } else if (ldapAttribute.getValues().size() == 1) {
              value = ldapAttribute.getValues().iterator().next();
            }
            
            targetEntity.assignAttributeValue(ldapAttribute.getName(), value);
          }
        }

        return targetEntity;

      }

    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("retrieveEntities", startNanos));
    }
    throw new RuntimeException("Why are we here?");

  }
  
  public TargetDaoRetrieveGroupsResponse retrieveGroups(TargetDaoRetrieveGroupsRequest targetDaoRetrieveGroupsRequest) {

    List<ProvisioningGroup> results = new ArrayList<ProvisioningGroup>();
    TargetDaoRetrieveGroupsResponse targetDaoRetrieveGroupsResponse = new TargetDaoRetrieveGroupsResponse(results);

    if (targetDaoRetrieveGroupsRequest == null || GrouperUtil.length(targetDaoRetrieveGroupsRequest.getSearchAttributeValues()) == 0) {
      return targetDaoRetrieveGroupsResponse;
    }
    
    List<Object> values = new ArrayList<Object>(targetDaoRetrieveGroupsRequest.getSearchAttributeValues());

    boolean includeAllMembershipsIfApplicable = targetDaoRetrieveGroupsRequest.isIncludeAllMembershipsIfApplicable();
    if (!StringUtils.equals("ldap_dn", targetDaoRetrieveGroupsRequest.getSearchAttribute())) {
      
      LdapSyncConfiguration ldapSyncConfiguration = (LdapSyncConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
      String ldapConfigId = ldapSyncConfiguration.getLdapExternalSystemConfigId();
      
      String groupSearchBaseDn = ldapSyncConfiguration.getGroupSearchBaseDn();
      Set<String> groupSearchAttributeNames = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
      groupSearchAttributeNames.addAll(ldapSyncConfiguration.getGroupSelectAttributes());
        
      Set<String> groupAttributesMultivalued = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
      if (ldapSyncConfiguration.getGroupAttributesMultivalued() != null) {
        groupAttributesMultivalued.addAll(ldapSyncConfiguration.getGroupAttributesMultivalued());
      }
      
      groupAttributesMultivalued.add("objectClass");
      
      String groupAttributeNameForMemberships = ldapSyncConfiguration.getGroupMembershipAttributeName();
      if (!StringUtils.isBlank(groupAttributeNameForMemberships)) {
        if (includeAllMembershipsIfApplicable) {
          groupSearchAttributeNames.add(groupAttributeNameForMemberships);
          groupAttributesMultivalued.add(groupAttributeNameForMemberships);
        } else {
          groupSearchAttributeNames.remove(groupAttributeNameForMemberships);
          groupAttributesMultivalued.remove(groupAttributeNameForMemberships);
        }
      }
      
      int batchSize = LdapConfiguration.getConfig(ldapConfigId).getQueryBatchSize();
      int numberOfBatches = GrouperUtil.batchNumberOfBatches(values, batchSize, false);
      for (int i = 0; i < numberOfBatches; i++) {
        long startNanos = System.nanoTime();

        try {
          List<Object> batchValues = GrouperUtil.batchList(values, batchSize, i);
          StringBuilder filterBuilder = new StringBuilder();
          
          if (!StringUtils.isBlank(targetDaoRetrieveGroupsRequest.getSearchAttribute())) {
            for (Object attributeValueObject : GrouperUtil.nonNull(batchValues)) {
              
              String value = GrouperUtil.stringValue(attributeValueObject);
              String searchFilter = "(" + targetDaoRetrieveGroupsRequest.getSearchAttribute() + "=" + GrouperUtil.ldapFilterEscape(value) + ")";
    
              filterBuilder.append(searchFilter);
    
            }
    
            String filter = filterBuilder.toString();
            
            if (GrouperUtil.length(batchValues) > 1) {
              filter = "(|" + filter + ")";
            }
            
            LdapSyncDaoForLdap ldapSyncDaoForLdap = new LdapSyncDaoForLdap();
            List<LdapEntry> ldapEntries = ldapSyncDaoForLdap.search(ldapConfigId, groupSearchBaseDn, filter, LdapSearchScope.SUBTREE_SCOPE, new ArrayList<String>(groupSearchAttributeNames));
            
            for (LdapEntry ldapEntry : ldapEntries) {
              ProvisioningGroup targetGroup = new ProvisioningGroup();
              targetGroup.assignAttributeValue(ldap_dn, ldapEntry.getDn());
              
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
              
              results.add(targetGroup);
                
            }
          }        
        } finally {
          this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("retrieveGroups", startNanos));
        }
      }
    } else {
      for (Object dnObject : values) {
        
        long startNanos = System.nanoTime();
        try {
          LdapProvisioningTargetDao ldapProvisioningTargetDao = (LdapProvisioningTargetDao)this.getGrouperProvisioner().retrieveGrouperProvisioningTargetDaoAdapter().getWrappedDao();
          ProvisioningGroup provisioningGroup = ldapProvisioningTargetDao.retrieveGroupByDn(GrouperUtil.stringValue(dnObject), includeAllMembershipsIfApplicable, false);
          if (provisioningGroup != null) {
            results.add(provisioningGroup);
          }
        } finally {
          this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("retrieveGroups", startNanos));
        }
      }
    }

    return targetDaoRetrieveGroupsResponse;
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
        userSearchAllFilter = this.generateUserSearchAllFilter();
      }

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
      
      //entitySearchAttributeNames.add("objectClass");
      userAttributesMultivalued.add("objectClass");
      
      String userAttributeNameForMemberships = ldapSyncConfiguration.getEntityMembershipAttributeName();
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
      List<LdapEntry> ldapEntries = ldapSyncDaoForLdap.search(ldapConfigId, userSearchBaseDn, userSearchAllFilter, LdapSearchScope.SUBTREE_SCOPE, new ArrayList<String>(entitySearchAttributeNames));
      
      for (LdapEntry ldapEntry : ldapEntries) {
        ProvisioningEntity targetEntity = new ProvisioningEntity();
        targetEntity.assignAttributeValue(ldap_dn, ldapEntry.getDn());
        
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
    TargetDaoRetrieveEntitiesResponse targetDaoRetrieveEntitiesResponse = new TargetDaoRetrieveEntitiesResponse(results);

    if (targetDaoRetrieveEntitiesRequest == null || GrouperUtil.length(targetDaoRetrieveEntitiesRequest.getSearchAttributeValues()) == 0) {
      return targetDaoRetrieveEntitiesResponse;
    }
    
    List<Object> values = new ArrayList<Object>(targetDaoRetrieveEntitiesRequest.getSearchAttributeValues());

    boolean includeAllMembershipsIfApplicable = targetDaoRetrieveEntitiesRequest.isIncludeAllMembershipsIfApplicable();
    if (!StringUtils.equals("ldap_dn", targetDaoRetrieveEntitiesRequest.getSearchAttribute())) {
      
      LdapSyncConfiguration ldapSyncConfiguration = (LdapSyncConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
      String ldapConfigId = ldapSyncConfiguration.getLdapExternalSystemConfigId();
      
      String entitySearchBaseDn = ldapSyncConfiguration.getUserSearchBaseDn();
      Set<String> entitySearchAttributeNames = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
      entitySearchAttributeNames.addAll(ldapSyncConfiguration.getEntitySelectAttributes());
        
      Set<String> entityAttributesMultivalued = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
      if (ldapSyncConfiguration.getEntityAttributesMultivalued() != null) {
        entityAttributesMultivalued.addAll(ldapSyncConfiguration.getEntityAttributesMultivalued());
      }
      
      entityAttributesMultivalued.add("objectClass");
      
      String entityAttributeNameForMemberships = ldapSyncConfiguration.getEntityMembershipAttributeName();
      if (!StringUtils.isBlank(entityAttributeNameForMemberships)) {
        if (includeAllMembershipsIfApplicable) {
          entitySearchAttributeNames.add(entityAttributeNameForMemberships);
          entityAttributesMultivalued.add(entityAttributeNameForMemberships);
        } else {
          entitySearchAttributeNames.remove(entityAttributeNameForMemberships);
          entityAttributesMultivalued.remove(entityAttributeNameForMemberships);
        }
      }
      
      int batchSize = LdapConfiguration.getConfig(ldapConfigId).getQueryBatchSize();
      int numberOfBatches = GrouperUtil.batchNumberOfBatches(values, batchSize, false);
      for (int i = 0; i < numberOfBatches; i++) {
        long startNanos = System.nanoTime();

        try {
          List<Object> batchValues = GrouperUtil.batchList(values, batchSize, i);
          StringBuilder filterBuilder = new StringBuilder();
          
          if (!StringUtils.isBlank(targetDaoRetrieveEntitiesRequest.getSearchAttribute())) {
            for (Object attributeValueObject : GrouperUtil.nonNull(batchValues)) {
              
              String value = GrouperUtil.stringValue(attributeValueObject);
              String searchFilter = "(" + targetDaoRetrieveEntitiesRequest.getSearchAttribute() + "=" + GrouperUtil.ldapFilterEscape(value) + ")";
    
              filterBuilder.append(searchFilter);
    
            }
    
            String filter = filterBuilder.toString();
            
            if (GrouperUtil.length(batchValues) > 1) {
              filter = "(|" + filter + ")";
            }
            
            LdapSyncDaoForLdap ldapSyncDaoForLdap = new LdapSyncDaoForLdap();
            List<LdapEntry> ldapEntries = ldapSyncDaoForLdap.search(ldapConfigId, entitySearchBaseDn, filter, LdapSearchScope.SUBTREE_SCOPE, new ArrayList<String>(entitySearchAttributeNames));
            
            for (LdapEntry ldapEntry : ldapEntries) {
              ProvisioningEntity targetEntity = new ProvisioningEntity();
              targetEntity.assignAttributeValue(ldap_dn, ldapEntry.getDn());
              
              for (LdapAttribute ldapAttribute : ldapEntry.getAttributes()) {
                if (ldapAttribute.getValues().size() > 0) {
                  Object value = null;
                  if (entityAttributesMultivalued.contains(ldapAttribute.getName())) {
                    value = new HashSet<Object>(ldapAttribute.getValues());
                  } else if (ldapAttribute.getValues().size() == 1) {
                    value = ldapAttribute.getValues().iterator().next();
                  }
                  
                  targetEntity.assignAttributeValue(ldapAttribute.getName(), value);
                }
              }
              
              results.add(targetEntity);
                
            }
          }        
        } finally {
          this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("retrieveEntities", startNanos));
        }
      }
    } else {
      for (Object dnObject : values) {
        
        long startNanos = System.nanoTime();
        try {
          LdapProvisioningTargetDao ldapProvisioningTargetDao = (LdapProvisioningTargetDao)this.getGrouperProvisioner().retrieveGrouperProvisioningTargetDaoAdapter().getWrappedDao();
          ProvisioningEntity provisioningEntity = ldapProvisioningTargetDao.retrieveEntityByDn(GrouperUtil.stringValue(dnObject), includeAllMembershipsIfApplicable, false);
          if (provisioningEntity != null) {
            results.add(provisioningEntity);
          }
        } finally {
          this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("retrieveEntities", startNanos));
        }
      }
    }

    return targetDaoRetrieveEntitiesResponse;
  }
  
  public TargetDaoInsertEntityResponse insertEntity(TargetDaoInsertEntityRequest targetDaoInsertEntityRequest) {
    long startNanos = System.nanoTime();
    ProvisioningEntity targetEntity = targetDaoInsertEntityRequest.getTargetEntity();

    try {
      LdapSyncConfiguration ldapSyncConfiguration = (LdapSyncConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
      String ldapConfigId = ldapSyncConfiguration.getLdapExternalSystemConfigId();
      
      if (StringUtils.isBlank(targetEntity.retrieveAttributeValueString(ldap_dn))) {
        throw new RuntimeException("Why is targetEntity.retrieveAttributeValueString(ldap_dn) blank?");
      }
      
      LdapEntry ldapEntry = new LdapEntry(targetEntity.retrieveAttributeValueString(ldap_dn));
      for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetEntity.getInternal_objectChanges())) {
        Object value = provisioningObjectChange.getNewValue();
        
        if (LdapProvisioningTargetDao.ldap_dn.equals(provisioningObjectChange.getAttributeName())) {
          // update the ldap entry dn just in case it's different
          ldapEntry.setDn((String)provisioningObjectChange.getNewValue());
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
              if (singleStringValue != null) {
                ldapAttribute.addValue(singleStringValue);
              }
            }
          }
        } else {
          String stringValue = GrouperUtil.stringValue(value);
          if (stringValue != null) {
            ldapAttribute.addValue(stringValue);
          }
        }
        
        if (ldapAttribute.getValues().size() > 0) {
          ldapEntry.addAttribute(ldapAttribute);
        }
      }
      LdapSyncDaoForLdap ldapSyncDaoForLdap = new LdapSyncDaoForLdap();
      ldapSyncDaoForLdap.create(ldapConfigId, ldapEntry);
      
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

    LdapSyncConfiguration ldapSyncConfiguration = (LdapSyncConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
    String ldapConfigId = ldapSyncConfiguration.getLdapExternalSystemConfigId();
    int queryBatchSize = LdapConfiguration.getConfig(ldapConfigId).getQueryBatchSize();
    grouperProvisionerDaoCapabilities.setDefaultBatchSize(queryBatchSize);
    
    grouperProvisionerDaoCapabilities.setCanDeleteEntity(true);
    grouperProvisionerDaoCapabilities.setCanDeleteGroup(true);
    grouperProvisionerDaoCapabilities.setCanInsertGroup(true);
    grouperProvisionerDaoCapabilities.setCanRetrieveAllGroups(true);
    
    grouperProvisionerDaoCapabilities.setCanRetrieveGroups(true);
    
    grouperProvisionerDaoCapabilities.setCanUpdateEntity(true);
    grouperProvisionerDaoCapabilities.setCanUpdateGroup(true);
    grouperProvisionerDaoCapabilities.setCanUpdateGroupMembershipAttribute(true);
    grouperProvisionerDaoCapabilities.setCanRetrieveAllEntities(true);

    grouperProvisionerDaoCapabilities.setCanRetrieveEntities(true);

    if (this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().getGrouperProvisioningBehaviorMembershipType() == GrouperProvisioningBehaviorMembershipType.groupAttributes) {
      grouperProvisionerDaoCapabilities.setCanRetrieveMembershipsWithGroup(true);
      grouperProvisionerDaoCapabilities.setCanRetrieveMembershipOneByGroup(true);
    }

    if (this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().getGrouperProvisioningBehaviorMembershipType() == GrouperProvisioningBehaviorMembershipType.entityAttributes) {
      grouperProvisionerDaoCapabilities.setCanRetrieveMembershipsWithEntity(true);
      grouperProvisionerDaoCapabilities.setCanRetrieveMembershipOneByEntity(true);
    }
    
    grouperProvisionerDaoCapabilities.setCanInsertEntity(true);
    grouperProvisionerDaoCapabilities.setCanRetrieveMembership(true);
  }

  @Override
  public TargetDaoRetrieveMembershipResponse retrieveMembership(TargetDaoRetrieveMembershipRequest targetDaoRetrieveMembershipRequest) {
    TargetDaoRetrieveMembershipResponse targetDaoRetrieveMembershipResponse = new TargetDaoRetrieveMembershipResponse();
    
    LdapSyncConfiguration ldapSyncConfiguration = (LdapSyncConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
    String ldapConfigId = ldapSyncConfiguration.getLdapExternalSystemConfigId();
    
    String membershipAttributeName;
    String dn;
    
    Set<?> membershipAttributeValueSet = null;
    
    if (this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().getGrouperProvisioningBehaviorMembershipType() == GrouperProvisioningBehaviorMembershipType.groupAttributes) {
      membershipAttributeName = ldapSyncConfiguration.getGroupMembershipAttributeName();
      dn = (targetDaoRetrieveMembershipRequest.getTargetGroup()).retrieveAttributeValueString(ldap_dn);
      
      if (targetDaoRetrieveMembershipRequest.getTargetGroup() != null) {
        membershipAttributeValueSet = (targetDaoRetrieveMembershipRequest.getTargetGroup()).retrieveAttributeValueSetForMemberships();
      }
      
    } else if (this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().getGrouperProvisioningBehaviorMembershipType() == GrouperProvisioningBehaviorMembershipType.entityAttributes) {
      membershipAttributeName = ldapSyncConfiguration.getEntityMembershipAttributeName();
      dn = (targetDaoRetrieveMembershipRequest.getTargetEntity()).retrieveAttributeValueString(ldap_dn);

      if (targetDaoRetrieveMembershipRequest.getTargetEntity() != null) {
        membershipAttributeValueSet = (targetDaoRetrieveMembershipRequest.getTargetEntity()).retrieveAttributeValueSetForMemberships();
      }

    } else {
      throw new RuntimeException("Unexpected grouperProvisioningBehaviorMembershipType: " + this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().getGrouperProvisioningBehaviorMembershipType());
    }

    if (targetDaoRetrieveMembershipRequest.getTargetMembership() != null && GrouperUtil.length(membershipAttributeValueSet) == 0) {
      membershipAttributeValueSet = (targetDaoRetrieveMembershipRequest.getTargetMembership()).retrieveAttributeValueSetForMemberships();
    }
    
    GrouperUtil.assertion(GrouperUtil.length(membershipAttributeValueSet) == 1, "Should be looking for one membership: " + GrouperUtil.length(membershipAttributeValueSet));
    
    String membershipAttributeValue = (String)membershipAttributeValueSet.iterator().next();
    String filter = "(" + membershipAttributeName + "=" + GrouperUtil.ldapFilterEscape(membershipAttributeValue) + ")";

    LdapSyncDaoForLdap ldapSyncDaoForLdap = new LdapSyncDaoForLdap();
    
    try {
      List<LdapEntry> ldapEntries = ldapSyncDaoForLdap.search(ldapConfigId, dn, filter, LdapSearchScope.OBJECT_SCOPE, new ArrayList<String>());
      
      if (ldapEntries.size() > 0) {
        if (this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().getGrouperProvisioningBehaviorMembershipType() == GrouperProvisioningBehaviorMembershipType.groupAttributes) {
          targetDaoRetrieveMembershipResponse.setTargetGroup(targetDaoRetrieveMembershipRequest.getTargetGroup());
        } else if (this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().getGrouperProvisioningBehaviorMembershipType() == GrouperProvisioningBehaviorMembershipType.entityAttributes) {
          targetDaoRetrieveMembershipResponse.setTargetEntity(targetDaoRetrieveMembershipRequest.getTargetEntity());
        }
      }
    } catch (Exception e) {
      if (e.getCause() != null && e.getCause() instanceof LdapException && ((LdapException)e.getCause()).getResultCode() == ResultCode.NO_SUCH_OBJECT) {
        // ok
      } else {
        throw e;
      }
    }
    
    return targetDaoRetrieveMembershipResponse;
  }

  @Override
  public TargetDaoDeleteEntityResponse deleteEntity(TargetDaoDeleteEntityRequest targetDaoDeleteEntityRequest) {
    
    long startNanos = System.nanoTime();
    ProvisioningEntity targetEntity = targetDaoDeleteEntityRequest.getTargetEntity();
  
    try {
      LdapSyncConfiguration ldapSyncConfiguration = (LdapSyncConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
      String ldapConfigId = ldapSyncConfiguration.getLdapExternalSystemConfigId();
      
      if (StringUtils.isBlank(targetEntity.retrieveAttributeValueString(ldap_dn))) {
        throw new RuntimeException("Why is targetEntity.retrieveAttributeValueString('ldap_dn') blank?");
      }
      LdapSyncDaoForLdap ldapSyncDaoForLdap = new LdapSyncDaoForLdap();
      ldapSyncDaoForLdap.delete(ldapConfigId, targetEntity.retrieveAttributeValueString(ldap_dn));

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
      List<Exception> exceptions = new ArrayList<Exception>();
      
      String dn = targetEntity.retrieveAttributeValueString(ldap_dn);
      if (targetEntity.getProvisioningEntityWrapper() != null && targetEntity.getProvisioningEntityWrapper().getTargetProvisioningEntity() != null
          && !GrouperUtil.isBlank(targetEntity.getProvisioningEntityWrapper().getTargetProvisioningEntity().retrieveAttributeValueString(ldap_dn))) {
        dn = targetEntity.getProvisioningEntityWrapper().getTargetProvisioningEntity().retrieveAttributeValueString(ldap_dn);
      }

      boolean movedDn = false;
      for (ProvisioningObjectChange provisionObjectChange : provisionObjectChanges) {
        
        String attributeName = provisionObjectChange.getAttributeName();
        ProvisioningObjectChangeAction action = provisionObjectChange.getProvisioningObjectChangeAction();
        Object newValue = provisionObjectChange.getNewValue();
        Object oldValue = provisionObjectChange.getOldValue();

        if (newValue != null && !(newValue instanceof byte[])) {
          newValue = GrouperUtil.stringValue(newValue);
        }
        
        if (oldValue != null && !(oldValue instanceof byte[])) {
          oldValue = GrouperUtil.stringValue(oldValue);
        }
        
        if (attributeName == null && LdapProvisioningTargetDao.ldap_dn.equals(attributeName) && action == ProvisioningObjectChangeAction.update) {
          // this is a rename
          try {
            LdapSyncDaoForLdap ldapSyncDaoForLdap = new LdapSyncDaoForLdap();
            newValue = GrouperUtil.stringValue(newValue);
            oldValue = GrouperUtil.stringValue(oldValue);
            ldapSyncDaoForLdap.move(ldapConfigId, (String)oldValue, (String)newValue);
            movedDn = true;
            dn = (String)newValue;
            provisionObjectChange.setProvisioned(true);
          } catch (Exception e) {
            provisionObjectChange.setProvisioned(false);
            provisionObjectChange.setException(e);
            targetEntity.setProvisioned(false);
            hasRenameFailure = true;
            exceptions.add(e);
          }
        } else if (attributeName == null) {
          throw new RuntimeException("Unexpected update for attributeName=" + attributeName + ", action=" + action);
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
          // the rdn was already changed
          if (movedDn && dn.startsWith(attributeName+"=")) {
            continue;
          }

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
  
      LdapSyncDaoForLdap ldapSyncDaoForLdap = new LdapSyncDaoForLdap();
      LdapModificationResult result = ldapSyncDaoForLdap.modify(ldapConfigId, targetEntity.retrieveAttributeValueString(ldap_dn), new ArrayList<LdapModificationItem>(ldapModificationItems.keySet()));
      
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
            targetEntity.setProvisioned(false);
            exceptions.add(attributeError.getError());
          }
        }
        
        for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetEntity.getInternal_objectChanges())) {
          if (provisioningObjectChange.getProvisioned() == null && provisioningObjectChange.getException() == null) {
            provisioningObjectChange.setProvisioned(true);
          }
        }
      }
      
      if (exceptions.size() > 0) {
        throw new RuntimeException("There were " + exceptions.size() + " exceptions, throwing first exception", exceptions.get(0));
      }
      
      return new TargetDaoUpdateEntityResponse();
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("updateEntity", startNanos));
    }
  }
  
  private void deleteEmptyParentFolders(LdapSyncConfiguration ldapSyncConfiguration, LdapSyncDaoForLdap ldapSyncDaoForLdap, String groupDnString) {
    if (ldapSyncConfiguration.getGroupDnType() != LdapSyncGroupDnType.bushy) {
      return;
    }
    
    try {      
      DN groupSearchBaseDn = new DN(ldapSyncConfiguration.getGroupSearchBaseDn());
      
      DN parentDn = new DN(groupDnString);
      
      while (true) {
        parentDn = parentDn.getParent();
        if (parentDn == null) {
          return;
        }
        
        if (parentDn.equals(groupSearchBaseDn)) {
          // we've clearly gone too far up
          return;
        }
        
        String parentDnString = parentDn.toString();
        try {
          List<LdapEntry> childEntries = ldapSyncDaoForLdap.search(ldapSyncConfiguration.getLdapExternalSystemConfigId(), parentDnString, "(objectClass=*)", LdapSearchScope.ONELEVEL_SCOPE, new ArrayList<String>(), 1L);
          
          if (childEntries.size() > 0) {
            // done
            return;
          }
          
          ldapSyncDaoForLdap.delete(ldapSyncConfiguration.getLdapExternalSystemConfigId(), parentDnString);
        } catch (Exception e) {
          if (e.getCause() != null && e.getCause() instanceof LdapException && ((LdapException)e.getCause()).getResultCode() == ResultCode.NO_SUCH_OBJECT) {
            // dn doesn't exist.  continue going up.
            continue;
          } else {
            throw e;
          }
        }
      }
    } catch (LDAPException e) {
      throw new RuntimeException(e);
    }
  }
  
  private void createParentFolders(LdapSyncConfiguration ldapSyncConfiguration, LdapSyncDaoForLdap ldapSyncDaoForLdap, String groupDnString) {
    
    if (ldapSyncConfiguration.getGroupDnType() != LdapSyncGroupDnType.bushy) {
      return;
    }
    
    try {
      List<DN> ldapDnsToCreate = new ArrayList<DN>();
      
      DN groupSearchBaseDn = new DN(ldapSyncConfiguration.getGroupSearchBaseDn());
      
      DN groupDn = new DN(groupDnString);
      DN parentDn = groupDn.getParent();
      if (parentDn.equals(groupSearchBaseDn)) {
        throw new RuntimeException("Group's parent dn is the base dn!");
      }
      
      ldapDnsToCreate.add(parentDn);
      
      while (true) {
        parentDn = parentDn.getParent();
        if (parentDn == null) {
          break;
        }
        
        if (parentDn.equals(groupSearchBaseDn)) {
          // we've clearly gone too far up
          break;
        }
        
        String parentDnString = parentDn.toString();
        try {
          ldapSyncDaoForLdap.search(ldapSyncConfiguration.getLdapExternalSystemConfigId(), parentDnString, "(objectClass=*)", LdapSearchScope.OBJECT_SCOPE, new ArrayList<String>());
          break;
        } catch (Exception e) {
          if (e.getCause() != null && e.getCause() instanceof LdapException && ((LdapException)e.getCause()).getResultCode() == ResultCode.NO_SUCH_OBJECT) {
            ldapDnsToCreate.add(0, parentDn);
          } else {
            throw e;
          }
        }
      }
      
      // now create the DNs
      for (DN ldapDnToCreate : ldapDnsToCreate) {
        LdapEntry folderLdapEntry = new LdapEntry(ldapDnToCreate.toString());
        folderLdapEntry.addAttribute(new LdapAttribute(ldapDnToCreate.getRDN().getAttributeNames()[0], ldapDnToCreate.getRDN().getAttributeValues()[0]));
        
        LdapAttribute objectClassLdapAttribute = new LdapAttribute("objectClass");
        objectClassLdapAttribute.addStringValues(new ArrayList<String>(ldapSyncConfiguration.getFolderObjectClasses()));
        
        folderLdapEntry.addAttribute(objectClassLdapAttribute);
        
        ldapSyncDaoForLdap.create(ldapSyncConfiguration.getLdapExternalSystemConfigId(), folderLdapEntry);
      }
    } catch (LDAPException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * look at object classes and the search attribute
   * @return the filter
   */
  public String generateUserSearchAllFilter() {
    
    LdapSyncConfiguration ldapSyncConfiguration = (LdapSyncConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();

    StringBuilder filterBuilder = new StringBuilder();

    // get the search attribute
    List<GrouperProvisioningConfigurationAttribute> grouperProvisioningConfigurationAttributes = ldapSyncConfiguration.getEntitySearchAttributes();
    
    Collection<String> objectClasses = null;
    // see if there are object classes
    for (GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute : GrouperUtil.nonNull(ldapSyncConfiguration.getTargetEntityAttributeNameToConfig()).values()) {
      if (StringUtils.equalsIgnoreCase("objectclass", grouperProvisioningConfigurationAttribute.getName())) {
        // lets try to evaluate the scriptlet and static values to get the object classes
        if (!StringUtils.isBlank(grouperProvisioningConfigurationAttribute.getTranslateExpression())) {
          Object objectClassResult = this.getGrouperProvisioner().retrieveGrouperProvisioningTranslator()
              .runScript(grouperProvisioningConfigurationAttribute.getTranslateExpression(), null);
          if (objectClassResult instanceof String) {
            objectClasses = GrouperUtil.splitTrimToSet((String)objectClassResult, ",");
          } else {
            objectClasses = (Collection<String>)objectClassResult;
          }
          break;
        } else if (!StringUtils.isBlank(grouperProvisioningConfigurationAttribute.getTranslateFromStaticValues())) {
          objectClasses = GrouperUtil.splitTrimToSet(grouperProvisioningConfigurationAttribute.getTranslateFromStaticValues(), ",");
          break;
        }
      }
    }
    
    int numberOfConditions = GrouperUtil.length(objectClasses) + grouperProvisioningConfigurationAttributes.size();
    
    if (numberOfConditions > 1) {
      filterBuilder.append("(&");
    }

    for (GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute : grouperProvisioningConfigurationAttributes) {
      if (StringUtils.equals(grouperProvisioningConfigurationAttribute.getName(), ldap_dn)) {
        filterBuilder.append("(" + GrouperUtil.ldapFilterEscape(ldapSyncConfiguration.getUserRdnAttribute()) + "=*)");
      } else {
        filterBuilder.append("(" + GrouperUtil.ldapFilterEscape(grouperProvisioningConfigurationAttribute.getName()) + "=*)");
      }
    }
    for (String objectClass : GrouperUtil.nonNull(objectClasses)) {
      filterBuilder.append("(objectclass=").append(GrouperUtil.ldapFilterEscape(objectClass)).append(")");
    }
    
    if (numberOfConditions > 1) {
      filterBuilder.append(")");
    }
    
    return filterBuilder.toString();

  }
  
  private void checkParentFolderCaseChanges(LdapSyncConfiguration ldapSyncConfiguration, String oldDnString, String newDnString) {
    if (ldapSyncConfiguration.getGroupDnType() != LdapSyncGroupDnType.bushy) {
      return;
    }
    
    LdapSyncDaoForLdap ldapSyncDaoForLdap = new LdapSyncDaoForLdap();

    try {      
      DN groupSearchBaseDn = new DN(ldapSyncConfiguration.getGroupSearchBaseDn());

      String oldDnParentString = DN.getParent(oldDnString).toMinimallyEncodedString();
      String newDnParentString = DN.getParent(newDnString).toMinimallyEncodedString();

      if (oldDnParentString.equals(newDnParentString)) {
        return;
      }
      
      if (!oldDnParentString.equalsIgnoreCase(newDnParentString)) {
        return;
      }
      
      DN oldDnParent = new DN(oldDnParentString);
      DN newDnParent = new DN(newDnParentString);
      
      if (groupSearchBaseDn.isDescendantOf(oldDnParent, true)) {
        return;
      }

      while (true) {
        
        String oldValue = oldDnParent.getRDN().getAttributeValues()[0];
        String newValue = newDnParent.getRDN().getAttributeValues()[0];
        
        if (oldValue.equalsIgnoreCase(newValue) && !oldValue.equals(newValue)) {
          ldapSyncDaoForLdap.move(ldapSyncConfiguration.getLdapExternalSystemConfigId(), oldDnParentString, newDnParentString);
        }
        
        oldDnParent = oldDnParent.getParent();
        newDnParent = newDnParent.getParent();
        
        if (oldDnParent == null) {
          break;
        }
        
        if (groupSearchBaseDn.isDescendantOf(oldDnParent, true)) {
          break;
        }
        
        oldDnParentString = oldDnParent.toString();
        newDnParentString = newDnParent.toString();
      }
    } catch (LDAPException e) {
      throw new RuntimeException(e);
    }
  }

  /*
  public static void main(String[] args) {
    edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioner grouperProvisioner = edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioner.retrieveProvisioner("testLdapProv2");
    grouperProvisioner.retrieveGrouperProvisioningBehavior().setGrouperProvisioningType(edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningType.fullProvisionFull);
    grouperProvisioner.retrieveGrouperProvisioningConfiguration().configureProvisioner();
    grouperProvisioner.retrieveGrouperProvisioningConfiguration().setGroupAttributeNameForMemberships("member");
    ProvisioningGroup targetGroup = new ProvisioningGroup();
    targetGroup.("cn=test4:test2,ou=Groups,dc=example,dc=edu");
    
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
