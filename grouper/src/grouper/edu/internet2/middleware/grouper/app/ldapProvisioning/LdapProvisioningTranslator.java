package edu.internet2.middleware.grouper.app.ldapProvisioning;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfigurationAttribute;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfigurationAttributeType;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningTranslatorBase;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningEntityWrapper;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningGroupWrapper;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * @author shilen
 */
public class LdapProvisioningTranslator extends GrouperProvisioningTranslatorBase {

  @Override
  public Object fieldTranslation(Object currentValue, Map<String, Object> elVariableMap,
      boolean forCreate,
      GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute,
      ProvisioningGroupWrapper provisioningGroupWrapper,
      ProvisioningEntityWrapper provisioningEntityWrapper) {

    Object fieldValue = super.fieldTranslation(currentValue, elVariableMap, forCreate,
        grouperProvisioningConfigurationAttribute, provisioningGroupWrapper,
        provisioningEntityWrapper);

    if (grouperProvisioningConfigurationAttribute != null 
        && grouperProvisioningConfigurationAttribute.getGrouperProvisioningConfigurationAttributeType() 
          == GrouperProvisioningConfigurationAttributeType.group
        && !StringUtils.isBlank(grouperProvisioningConfigurationAttribute.getTranslateFromGrouperProvisioningGroupField())
        && !grouperProvisioningConfigurationAttribute.isAttribute() && "name".equals(grouperProvisioningConfigurationAttribute.getName())) {
      
      String fieldValueString = GrouperUtil.stringValue(fieldValue);

      if (StringUtils.isBlank(fieldValueString)) {
        throw new RuntimeException("Not expecting null DN part! " + provisioningGroupWrapper.getGrouperProvisioningGroup());
      }
      
      LdapSyncConfiguration ldapSyncConfiguration = (LdapSyncConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();

      String dn = null;
      
      if (ldapSyncConfiguration.getGroupDnType() == LdapSyncGroupDnType.bushy) {
        dn = GrouperUtil.ldapBushyDn(fieldValueString, "cn", ldapSyncConfiguration.getFolderRdnAttribute(), true, false) + "," + ldapSyncConfiguration.getGroupSearchBaseDn();
        
      } else if (ldapSyncConfiguration.getGroupDnType() == LdapSyncGroupDnType.flat) {
        dn = GrouperUtil.ldapEscapeRdn("cn=" + fieldValueString) + "," + ldapSyncConfiguration.getGroupSearchBaseDn();
        
      } else {
        throw new RuntimeException("Not expecting group dn type: " + ldapSyncConfiguration.getGroupDnType());
      }
      fieldValue = dn;
    }
    return fieldValue;
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
