package edu.internet2.middleware.grouper.app.ldapProvisioning;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfigurationAttribute;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfigurationAttributeType;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningTranslator;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningEntity;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningEntityWrapper;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningGroup;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningGroupWrapper;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * @author shilen
 */
public class LdapProvisioningTranslator extends GrouperProvisioningTranslator {

  /**
   * we need the rdn to be evaled before the dn
   */
  @Override
  public Collection<GrouperProvisioningConfigurationAttribute> entityTargetAttributesInTranslationOrder() {
    
    Collection<GrouperProvisioningConfigurationAttribute> defaultAttributes = super.entityTargetAttributesInTranslationOrder();
    
    List<GrouperProvisioningConfigurationAttribute> result = new ArrayList<GrouperProvisioningConfigurationAttribute>();
    
    GrouperProvisioningConfigurationAttribute dnAttribute = null;
    for (GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute : defaultAttributes) {
      if (LdapProvisioningTargetDao.ldap_dn.equals(grouperProvisioningConfigurationAttribute.getName())) {
        dnAttribute = grouperProvisioningConfigurationAttribute;
      } else {
        result.add(grouperProvisioningConfigurationAttribute);
      }
    }
    if (dnAttribute != null) {
      result.add(dnAttribute);
    }
    return result;
  }

  @Override
  public Collection<GrouperProvisioningConfigurationAttribute> groupAttributesInTranslationOrder() {
    
    Collection<GrouperProvisioningConfigurationAttribute> defaultAttributes = super.groupAttributesInTranslationOrder();

    List<GrouperProvisioningConfigurationAttribute> result = new ArrayList<GrouperProvisioningConfigurationAttribute>();
    GrouperProvisioningConfigurationAttribute dnAttribute = null;
    for (GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute : defaultAttributes) {
      if (LdapProvisioningTargetDao.ldap_dn.equals(grouperProvisioningConfigurationAttribute.getName())) {
        dnAttribute = grouperProvisioningConfigurationAttribute;
      } else {
        result.add(grouperProvisioningConfigurationAttribute);
      }
    }
    if (dnAttribute != null) {
      result.add(dnAttribute);
    }
    return result;
  }

  @Override
  public Object attributeTranslation(Map<String, Object> elVariableMap, boolean forCreate,
      GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute,
      ProvisioningGroupWrapper provisioningGroupWrapper,
      ProvisioningEntityWrapper provisioningEntityWrapper, boolean[] translate, boolean[] shouldRetrieveFromCache) {

    Object attributeValue = super.attributeTranslation(elVariableMap, forCreate,
         grouperProvisioningConfigurationAttribute,
         provisioningGroupWrapper,
         provisioningEntityWrapper, translate, shouldRetrieveFromCache);
    
    String expressionToUse = grouperProvisioningConfigurationAttribute == null ? null : getTargetExpressionToUse(forCreate, grouperProvisioningConfigurationAttribute);
    String translateFromGrouperProvisioningEntityField = grouperProvisioningConfigurationAttribute == null ? null : getTranslateFromGrouperProvisioningEntityField(forCreate, grouperProvisioningConfigurationAttribute);
    String translateFromGrouperProvisioningGroupField = grouperProvisioningConfigurationAttribute == null ? null : getTranslateFromGrouperProvisioningGroupField(forCreate, grouperProvisioningConfigurationAttribute);
    LdapSyncConfiguration ldapSyncConfiguration = (LdapSyncConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
    String groupRdnAttributeName = ldapSyncConfiguration.getGroupRdnAttribute();
    String entityRdnAttributeName = ldapSyncConfiguration.getUserRdnAttribute();


    if (LdapProvisioningTargetDao.ldap_dn.equals(grouperProvisioningConfigurationAttribute.getName()) 
        && grouperProvisioningConfigurationAttribute.getGrouperProvisioningConfigurationAttributeType() == GrouperProvisioningConfigurationAttributeType.entity 
        && StringUtils.isNotBlank(translateFromGrouperProvisioningGroupField)) {
    
      GrouperProvisioningConfigurationAttribute rdnConfigurationAttribute = ldapSyncConfiguration.getTargetEntityAttributeNameToConfig().get(entityRdnAttributeName);
      
      String rdnTranslateFromGrouperProvisioningEntityField = rdnConfigurationAttribute == null ? null : getTranslateFromGrouperProvisioningEntityField(forCreate, rdnConfigurationAttribute);
      
      if (StringUtils.equals(rdnTranslateFromGrouperProvisioningEntityField, translateFromGrouperProvisioningEntityField)) {
        translateFromGrouperProvisioningEntityField = null;
        attributeValue = null;
      } 
    
    }

    
    if (LdapProvisioningTargetDao.ldap_dn.equals(grouperProvisioningConfigurationAttribute.getName()) 
          && grouperProvisioningConfigurationAttribute.getGrouperProvisioningConfigurationAttributeType() == GrouperProvisioningConfigurationAttributeType.group 
          && StringUtils.isNotBlank(translateFromGrouperProvisioningGroupField)) {
      
      GrouperProvisioningConfigurationAttribute rdnConfigurationAttribute = ldapSyncConfiguration.getTargetGroupAttributeNameToConfig().get(groupRdnAttributeName);
      
      String rdnTranslateFromGrouperProvisioningGroupField = rdnConfigurationAttribute == null ? null : getTranslateFromGrouperProvisioningGroupField(forCreate, rdnConfigurationAttribute);
      
      if (StringUtils.equals(rdnTranslateFromGrouperProvisioningGroupField, translateFromGrouperProvisioningGroupField)) {
        translateFromGrouperProvisioningGroupField = null;
        attributeValue = null;
      } else if (ldapSyncConfiguration.getGroupDnType() == LdapSyncGroupDnType.bushy &&
          StringUtils.equals(rdnTranslateFromGrouperProvisioningGroupField, "extension") && 
          StringUtils.equals(translateFromGrouperProvisioningGroupField, "name")) {
        translateFromGrouperProvisioningGroupField = null;
        attributeValue = null;
      }
    
    }
    
    if (grouperProvisioningConfigurationAttribute != null
        && StringUtils.isBlank(translateFromGrouperProvisioningGroupField)
        && grouperProvisioningConfigurationAttribute.getGrouperProvisioningConfigurationAttributeType()
          == GrouperProvisioningConfigurationAttributeType.group
        && StringUtils.isBlank(expressionToUse)
        && StringUtils.isBlank(GrouperUtil.stringValue(attributeValue))
        && LdapProvisioningTargetDao.ldap_dn.equals(grouperProvisioningConfigurationAttribute.getName())) {
      
      String dn = null;
      boolean dnOnly = ldapSyncConfiguration.isOnlyLdapGroupDnOverride();
      if (ldapSyncConfiguration.isAllowLdapGroupDnOverride()) {
        dn = provisioningGroupWrapper.getGrouperProvisioningGroup().retrieveAttributeValueString("md_grouper_ldapGroupDnOverride");
      }

      if (!dnOnly) {
        if (StringUtils.isEmpty(dn)) {        
          if (ldapSyncConfiguration.getGroupDnType() == LdapSyncGroupDnType.bushy) {
            String groupRdnAttributeValue = null;
  
            if (((ProvisioningGroup)elVariableMap.get("grouperTargetGroup")).getAttributes() != null 
                && ((ProvisioningGroup)elVariableMap.get("grouperTargetGroup")).getAttributes().get(groupRdnAttributeName) != null) {
              groupRdnAttributeValue = (String)((ProvisioningGroup)elVariableMap.get("grouperTargetGroup")).getAttributes().get(groupRdnAttributeName).getValue();
            }

            dn = GrouperUtil.ldapBushyDn(provisioningGroupWrapper.getGrouperProvisioningGroup().getName(), groupRdnAttributeName, groupRdnAttributeValue, ldapSyncConfiguration.getFolderRdnAttribute(), true, false) + "," + ldapSyncConfiguration.getGroupSearchBaseDn();
          } else if (ldapSyncConfiguration.getGroupDnType() == LdapSyncGroupDnType.flat) {
            String groupRdnAttributeValue = (String)((ProvisioningGroup)elVariableMap.get("grouperTargetGroup")).getAttributes().get(groupRdnAttributeName).getValue();
            dn = GrouperUtil.ldapEscapeRdn(groupRdnAttributeName + "=" + groupRdnAttributeValue) + "," + ldapSyncConfiguration.getGroupSearchBaseDn();
          } else {
            throw new RuntimeException("Not expecting group dn type: " + ldapSyncConfiguration.getGroupDnType());
          }
        }
      }
      attributeValue = dn;
    }
    
    if (grouperProvisioningConfigurationAttribute != null
        && StringUtils.isBlank(translateFromGrouperProvisioningEntityField)
        && grouperProvisioningConfigurationAttribute.getGrouperProvisioningConfigurationAttributeType()
          == GrouperProvisioningConfigurationAttributeType.entity
        && StringUtils.isBlank(expressionToUse)
        && StringUtils.isBlank(GrouperUtil.stringValue(attributeValue))
        && LdapProvisioningTargetDao.ldap_dn.equals(grouperProvisioningConfigurationAttribute.getName())) {
      
      if (!StringUtils.isBlank(ldapSyncConfiguration.getUserSearchBaseDn())) {
        String dn = null;
  
        String entityRdnAttributeValue = (String)((ProvisioningEntity)elVariableMap.get("grouperTargetEntity")).getAttributes().get(entityRdnAttributeName).getValue();
        dn = GrouperUtil.ldapEscapeRdn(entityRdnAttributeName + "=" + entityRdnAttributeValue) + "," + ldapSyncConfiguration.getUserSearchBaseDn();
        attributeValue = dn;
      }
    }
    
    if (grouperProvisioningConfigurationAttribute != null 
        && grouperProvisioningConfigurationAttribute.getGrouperProvisioningConfigurationAttributeType() 
          == GrouperProvisioningConfigurationAttributeType.group
        && !StringUtils.isBlank(grouperProvisioningConfigurationAttribute.getTranslateFromGrouperProvisioningGroupField())
        && !StringUtils.isEmpty(groupRdnAttributeName) && groupRdnAttributeName.equals(grouperProvisioningConfigurationAttribute.getName())) {
  
      if (ldapSyncConfiguration.isAllowLdapGroupDnOverride()) {
        String overrideDn = provisioningGroupWrapper.getGrouperProvisioningGroup().retrieveAttributeValueString("md_grouper_ldapGroupDnOverride");
        if (!StringUtils.isEmpty(overrideDn)) {
          attributeValue = GrouperUtil.ldapConvertDnToSpecificValue(overrideDn);
        }
      }
    }

    
    return attributeValue;
  }
  
//  @Override
//  public void translateGrouperToCommon() {
//    
//    super.translateGrouperToCommon();
//    
    // TODO this is currently very limited and focused on the trivial use case, only flat, group provisioning, no subject/target links, etc..
    
    //Map<String, ProvisioningGroup> grouperCommonGroups = new HashMap<String, ProvisioningGroup>();
    
      
    
    
//    for (ProvisioningMembership provisioningMembership : grouperProvisioningMemberships) {
//      String targetEntityId = provisioningMembership.getProvisioningEntityId();
//      String targetGroupId = provisioningMembership.getProvisioningGroupId();
//      ProvisioningEntity targetEntity = grouperProvisioningEntities.get(targetEntityId);
//      
//      if (targetEntity == null) {
//        // maybe a race condition
//        continue;
//      }
//      
//      if (grouperProvisioningGroups.get(targetGroupId) == null) {
//        // maybe a race condition
//        continue;
//      }
//      
//      Map<String, Object> elVariableMap = new HashMap<String, Object>();
//      elVariableMap.put("targetEntity", targetEntity);
//      String provisionedAttributeValue = GrouperUtil.substituteExpressionLanguage(ldapSyncConfiguration.getProvisionedAttributeValueFormat(), elVariableMap, true, false, false);
//      
//      if (!StringUtils.isEmpty(provisionedAttributeValue)) {
//        if (allMembershipsByGroupId.get(targetGroupId) == null) {
//          allMembershipsByGroupId.put(targetGroupId, new HashSet<String>());
//        }
//        
//        allMembershipsByGroupId.get(targetGroupId).add(provisionedAttributeValue);
//      }
//    }
//    
//    String provisionedAttributeName = ldapSyncConfiguration.getProvisionedAttributeName();
//
//    for (String groupId : grouperProvisioningGroups.keySet()) {
//      ProvisioningGroup resultTargetGroup = new ProvisioningGroup();
//      resultTargetGroup.setAttributes(new HashMap<String, ProvisioningAttribute>());
//      
//      Map<String, Object> elVariableMap = new HashMap<String, Object>();
//      elVariableMap.put("targetGroup", grouperProvisioningGroups.get(groupId));
//      
//      for (int i = 0; i < ldapSyncConfiguration.getGroupCreationNumberOfAttributes(); i++) {
//        String attributeName = ldapSyncConfiguration.getGroupCreationLdifTemplate_attrs().get(i);
//        String attributeValuesRaw = ldapSyncConfiguration.getGroupCreationLdifTemplate_vals().get(i);
//        Integer maxLength = ldapSyncConfiguration.getGroupCreationLdifTemplate_maxLengths().get(i);
//        Set<String> attributeValuesToAdd = new HashSet<String>();
//        for (String attributeValueRaw : GrouperUtil.splitTrim(attributeValuesRaw, ",")) {
//          String attributeValue = GrouperUtil.substituteExpressionLanguage(attributeValueRaw, elVariableMap, true, false, false);
//          if (!StringUtils.isEmpty(attributeValue)) {
//            if (maxLength != null && maxLength > 0) {
//              attributeValue = GrouperUtil.truncateAscii(attributeValue, maxLength);
//            }
//            attributeValuesToAdd.add(attributeValue);
//          }
//        }
//        
//        if (attributeValuesToAdd.size() > 0) {
//          ProvisioningAttribute targetAttribute = new ProvisioningAttribute();
//          targetAttribute.setName(attributeName);
//          targetAttribute.setValue(attributeValuesToAdd);
//          resultTargetGroup.getAttributes().put(attributeName, targetAttribute);
//        }
//      }
//      
//      if (GrouperUtil.length(allMembershipsByGroupId.get(groupId)) > 0) {
//        ProvisioningAttribute targetAttribute = new ProvisioningAttribute();
//        targetAttribute.setName(provisionedAttributeName);
//        targetAttribute.setValue(allMembershipsByGroupId.get(groupId));
//        resultTargetGroup.getAttributes().put(provisionedAttributeName, targetAttribute);
//      }
//      
//      // TODO assuming dn is based on the cn attribute (rdn)
//      // TODO escaping
//      String cn = (String)((Collection<?>)resultTargetGroup.getAttributes().get("cn").getValue()).iterator().next();
//      String dn = "cn=" + cn + "," + ldapSyncConfiguration.getGroupSearchBaseDn();
//      resultTargetGroup.setId(dn);
//      grouperCommonGroups.put(dn, resultTargetGroup);
//    }
    
//  }
}
