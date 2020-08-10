package edu.internet2.middleware.grouper.app.ldapProvisioning;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningTranslatorBase;
import edu.internet2.middleware.grouper.app.provisioning.TargetAttribute;
import edu.internet2.middleware.grouper.app.provisioning.TargetEntity;
import edu.internet2.middleware.grouper.app.provisioning.TargetGroup;
import edu.internet2.middleware.grouper.app.provisioning.TargetMembership;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.lang3.StringUtils;

/**
 * @author shilen
 */
public class LdapProvisioningTranslator extends GrouperProvisioningTranslatorBase {

  @Override
  public Map<String, TargetGroup> translateToTarget(Map<String, TargetGroup> targetGroups, Map<String, TargetEntity> targetEntities, Map<String, TargetMembership> targetMemberships) {
    
    // TODO this is currently very limited and focused on the trivial use case, only flat, group provisioning, no subject/target links, etc..
    
    Map<String, TargetGroup> resultTargetGroups = new HashMap<String, TargetGroup>();

    LdapSyncConfiguration ldapSyncConfiguration = (LdapSyncConfiguration)this.getGrouperProvisioner().retrieveProvisioningConfiguration();
    
    Map<String, Set<String>> allMembershipsByGroupId = new HashMap<String, Set<String>>();
    
    for (String membershipId : targetMemberships.keySet()) {
      TargetMembership targetMembership = targetMemberships.get(membershipId);
      String targetEntityId = targetMembership.getTargetEntity().getId();
      String targetGroupId = targetMembership.getTargetGroup().getId();
      TargetEntity targetEntity = targetEntities.get(targetEntityId);
      
      if (targetEntity == null) {
        // maybe a race condition
        continue;
      }
      
      if (targetGroups.get(targetGroupId) == null) {
        // maybe a race condition
        continue;
      }
      
      Map<String, Object> elVariableMap = new HashMap<String, Object>();
      elVariableMap.put("targetEntity", targetEntity);
      String provisionedAttributeValue = GrouperUtil.substituteExpressionLanguage(ldapSyncConfiguration.getProvisionedAttributeValueFormat(), elVariableMap, true, false, false);
      
      if (!StringUtils.isEmpty(provisionedAttributeValue)) {
        if (allMembershipsByGroupId.get(targetGroupId) == null) {
          allMembershipsByGroupId.put(targetGroupId, new HashSet<String>());
        }
        
        allMembershipsByGroupId.get(targetGroupId).add(provisionedAttributeValue);
      }
    }
    
    String provisionedAttributeName = ldapSyncConfiguration.getProvisionedAttributeName();

    for (String groupId : targetGroups.keySet()) {
      TargetGroup resultTargetGroup = new TargetGroup();
      resultTargetGroup.setAttributes(new HashMap<String, TargetAttribute>());
      
      Map<String, Object> elVariableMap = new HashMap<String, Object>();
      elVariableMap.put("targetGroup", targetGroups.get(groupId));
      
      for (int i = 0; i < ldapSyncConfiguration.getGroupCreationNumberOfAttributes(); i++) {
        String attributeName = ldapSyncConfiguration.getGroupCreationLdifTemplate_attrs().get(i);
        String attributeValuesRaw = ldapSyncConfiguration.getGroupCreationLdifTemplate_vals().get(i);
        Integer maxLength = ldapSyncConfiguration.getGroupCreationLdifTemplate_maxLengths().get(i);
        Set<String> attributeValuesToAdd = new HashSet<String>();
        for (String attributeValueRaw : GrouperUtil.splitTrim(attributeValuesRaw, ",")) {
          String attributeValue = GrouperUtil.substituteExpressionLanguage(attributeValueRaw, elVariableMap, true, false, false);
          if (!StringUtils.isEmpty(attributeValue)) {
            if (maxLength != null && maxLength > 0) {
              attributeValue = GrouperUtil.truncateAscii(attributeValue, maxLength);
            }
            attributeValuesToAdd.add(attributeValue);
          }
        }
        
        if (attributeValuesToAdd.size() > 0) {
          TargetAttribute targetAttribute = new TargetAttribute();
          targetAttribute.setName(attributeName);
          targetAttribute.setValue(attributeValuesToAdd);
          resultTargetGroup.getAttributes().put(attributeName, targetAttribute);
        }
      }
      
      if (GrouperUtil.length(allMembershipsByGroupId.get(groupId)) > 0) {
        TargetAttribute targetAttribute = new TargetAttribute();
        targetAttribute.setName(provisionedAttributeName);
        targetAttribute.setValue(allMembershipsByGroupId.get(groupId));
        resultTargetGroup.getAttributes().put(provisionedAttributeName, targetAttribute);
      }
      
      // TODO assuming dn is based on the cn attribute (rdn)
      // TODO escaping
      String cn = (String)((Collection<?>)resultTargetGroup.getAttributes().get("cn").getValue()).iterator().next();
      String dn = "cn=" + cn + "," + ldapSyncConfiguration.getGroupSearchBaseDn();
      resultTargetGroup.setId(dn);
      resultTargetGroups.put(dn, resultTargetGroup);
    }
    
    return resultTargetGroups;
  }
}
