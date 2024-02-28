/**
 * 
 */
package edu.internet2.middleware.grouper.app.ldapProvisioning;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.app.config.GrouperConfigurationModuleAttribute;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.provisioning.ProvisionerStartWithBase;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningConfiguration;
import edu.internet2.middleware.grouper.cfg.text.GrouperTextContainer;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 */
public class LdapProvisioningMembershipStartWith extends ProvisionerStartWithBase {

  @Override
  public String getPropertyValueThatIdentifiesThisConfig() {
    return "ldapMemberships";
  }

  @Override
  public void populateProvisionerConfigurationValuesFromStartWith(Map<String, String> startWithSuffixToValue,
      Map<String, Object> provisionerSuffixToValue) {
    
    int groupAttributes = 0;
    int entityAttributes = 0;
    
    if (StringUtils.equals(startWithSuffixToValue.get("userAttributesType"), "entityResolver") || StringUtils.equals(startWithSuffixToValue.get("userAttributesType"), "subjectSourceAndEntityResolver")) {
      provisionerSuffixToValue.put("entityResolver.entityAttributesNotInSubjectSource", "true");
    }
    
    if (StringUtils.equals(startWithSuffixToValue.get("userAttributesType"), "subjectSource") 
        || StringUtils.equals(startWithSuffixToValue.get("userAttributesType"), "subjectSourceAndEntityResolver")) {
      provisionerSuffixToValue.put("operateOnGrouperEntities", "true");
      
      String attributesCommaSeparated = startWithSuffixToValue.get("subjectSourceEntityResolverAttributes");
      if (StringUtils.isNotBlank(attributesCommaSeparated)) {
        provisionerSuffixToValue.put("entityAttributeValueCacheHas", "true");
        String[] attributes = GrouperUtil.splitTrim(attributesCommaSeparated, ",");
        // by this time the validation is already done that there are no more than 2 attributes
        for (int i=0; i<attributes.length; i++) {
          int j = i+2;
          provisionerSuffixToValue.put("entityAttributeValueCache"+j+"has", "true");
          provisionerSuffixToValue.put("entityAttributeValueCache"+j+"source", "grouper");
          provisionerSuffixToValue.put("entityAttributeValueCache"+j+"type", "subjectTranslationScript");
          provisionerSuffixToValue.put("entityAttributeValueCache"+j+"translationScript", "${subject.getAttributeValue("+attributes[i]+")}");
        }
        
      }
      
    }
    
    if (StringUtils.equals(startWithSuffixToValue.get("membershipStructure"), "groupAttributes") || 
        StringUtils.equals(startWithSuffixToValue.get("membershipStructure"), "entityAttributes")) {
      
      provisionerSuffixToValue.put("operateOnGrouperMemberships", "true");
      
      provisionerSuffixToValue.put("provisioningType", startWithSuffixToValue.get("membershipStructure"));
    }
    
    if (StringUtils.equals(startWithSuffixToValue.get("membershipStructure"), "groupAttributes")) {
      
      provisionerSuffixToValue.put("operateOnGrouperGroups", "true");
      
      
      //provisionerSuffixToValue.put("groupDnType", startWithSuffixToValue.get("groupDnType"));
    }
    
    
    if (StringUtils.equals(startWithSuffixToValue.get("membershipStructure"), "groupAttributes") || 
        GrouperUtil.booleanValue(startWithSuffixToValue.get("membershipValueDn"), false) ||
        GrouperUtil.booleanValue(startWithSuffixToValue.get("groupLinkForAnotherReason"), false)) {
      
      provisionerSuffixToValue.put("operateOnGrouperGroups", "true");
      
      provisionerSuffixToValue.put("hasTargetGroupLink", "true");
            
      String groupRdnAttribute = startWithSuffixToValue.get("groupRdnAttribute");
      
      provisionerSuffixToValue.put("targetGroupAttribute."+groupAttributes+".name", "ldap_dn");
      
      provisionerSuffixToValue.put("groupAttributeValueCacheHas", "true");
      
      provisionerSuffixToValue.put("groupAttributeValueCache0has", "true");
      provisionerSuffixToValue.put("groupAttributeValueCache0source", "target");
      provisionerSuffixToValue.put("groupAttributeValueCache0type", "groupAttribute");
      provisionerSuffixToValue.put("groupAttributeValueCache0groupAttribute", "ldap_dn");

      groupAttributes++;
      if (!GrouperUtil.booleanValue(startWithSuffixToValue.get("onlyLdapGroupDnOverride"), false)) {
        provisionerSuffixToValue.put("targetGroupAttribute."+groupAttributes+".name", groupRdnAttribute);
      }

      String rdnValueForGroups = startWithSuffixToValue.get("rdnValueForGroups");
      
      if (StringUtils.equalsAny(rdnValueForGroups, "extension", "idIndex", "idIndexString", "name", "id")) {
        provisionerSuffixToValue.put("targetGroupAttribute."+groupAttributes+".translateExpressionType", "grouperProvisioningGroupField");
        provisionerSuffixToValue.put("targetGroupAttribute."+groupAttributes+".translateFromGrouperProvisioningGroupField", rdnValueForGroups);
        
      } else if (StringUtils.equalsAny(rdnValueForGroups, "extensionUnderscoreIdIndex", "nameBackwardsUnderscoreMax64", "script")) {
        provisionerSuffixToValue.put("targetGroupAttribute."+groupAttributes+".translateExpressionType", "translationScript");
        
        if (StringUtils.equals(rdnValueForGroups, "nameBackwardsUnderscoreMax64")) {
          provisionerSuffixToValue.put("targetGroupAttribute."+groupAttributes+".translateExpression", "${"+GrouperUtil.class.getName()+".stringFormatNameReverseReplaceTruncate(grouperProvisioningGroup.name, '_', 64)}");
        } else if (StringUtils.equals(rdnValueForGroups, "extensionUnderscoreIdIndex")) {
          provisionerSuffixToValue.put("targetGroupAttribute."+groupAttributes+".translateExpression", "${grouperProvisioningGroup.extension+'_'+grouperProvisioningGroup.idIndex}");
        } else if (StringUtils.equals(rdnValueForGroups, "script")) {
          provisionerSuffixToValue.put("targetGroupAttribute."+groupAttributes+".translateExpression", startWithSuffixToValue.get("rdnValueForGroupsTranslationScript"));
        }
                
      }
      groupAttributes++;
      
      String idIndexAttribute = startWithSuffixToValue.get("idIndexAttribute");
      if (StringUtils.isNotBlank(idIndexAttribute)) {
        provisionerSuffixToValue.put("targetGroupAttribute."+groupAttributes+".name", idIndexAttribute);
        provisionerSuffixToValue.put("targetGroupAttribute."+groupAttributes+".translateExpressionType", "grouperProvisioningGroupField");
        provisionerSuffixToValue.put("targetGroupAttribute."+groupAttributes+".translateFromGrouperProvisioningGroupField", "idIndex");

        groupAttributes++;
        
      }
            
      String groupAttributeValueCache1Attribute = null;
      
      if (GrouperUtil.booleanValue(startWithSuffixToValue.get("matchingSearchAttributeDifferentThanRdnorIdIndex"), false)) {
        
        String matchingSearchAttributeNameForGroups = startWithSuffixToValue.get("matchingSearchAttributeNameForGroups");
        String matchingSearchAttributeValueForGroups = startWithSuffixToValue.get("matchingSearchAttributeValueForGroups");
        
        
        provisionerSuffixToValue.put("targetGroupAttribute."+groupAttributes+".name", matchingSearchAttributeNameForGroups);
        
        if (StringUtils.equalsAny(matchingSearchAttributeValueForGroups, "extension", "idIndex", "name", "id")) {
          provisionerSuffixToValue.put("targetGroupAttribute."+groupAttributes+".translateExpressionType", "grouperProvisioningGroupField");
          provisionerSuffixToValue.put("targetGroupAttribute."+groupAttributes+".translateFromGrouperProvisioningGroupField", matchingSearchAttributeValueForGroups);
        } else if (StringUtils.equalsAny(matchingSearchAttributeValueForGroups, "script")){
          provisionerSuffixToValue.put("targetGroupAttribute."+groupAttributes+".translateExpressionType", "translationScript");
          provisionerSuffixToValue.put("targetGroupAttribute."+groupAttributes+".translateExpression", startWithSuffixToValue.get("matchingSearchAttributeValueForGroupsTranslationScript"));
        }
        
        groupAttributes++;
        
        provisionerSuffixToValue.put("groupMatchingAttributeCount", "1");
        provisionerSuffixToValue.put("groupMatchingAttribute0name", matchingSearchAttributeNameForGroups);
        
        if (!"ldap_dn".equals(matchingSearchAttributeNameForGroups)) {
          groupAttributeValueCache1Attribute = matchingSearchAttributeNameForGroups;
        }
      } else {
        
        if (GrouperUtil.isBlank(idIndexAttribute)) {
          provisionerSuffixToValue.put("groupMatchingAttributeCount", "1");
          provisionerSuffixToValue.put("groupMatchingAttribute0name", "ldap_dn");
        } else {
          provisionerSuffixToValue.put("groupMatchingAttributeCount", "2");
          provisionerSuffixToValue.put("groupMatchingAttribute0name", idIndexAttribute);
          provisionerSuffixToValue.put("groupMatchingAttribute1name", "ldap_dn");
          
          groupAttributeValueCache1Attribute = idIndexAttribute;
        }
      }
      
      if (!GrouperUtil.isBlank(groupAttributeValueCache1Attribute)) {
        provisionerSuffixToValue.put("groupAttributeValueCache1has", "true");
        provisionerSuffixToValue.put("groupAttributeValueCache1source", "target");
        provisionerSuffixToValue.put("groupAttributeValueCache1type", "groupAttribute");
        provisionerSuffixToValue.put("groupAttributeValueCache1groupAttribute", groupAttributeValueCache1Attribute);
      }
      
      String objectClassesForGroups = startWithSuffixToValue.get("objectClassesForGroups");
      if (StringUtils.isNotBlank(objectClassesForGroups)) {
        provisionerSuffixToValue.put("targetGroupAttribute."+groupAttributes+".name", "objectClass");
        provisionerSuffixToValue.put("targetGroupAttribute."+groupAttributes+".translateExpressionType", "staticValues");
        provisionerSuffixToValue.put("targetGroupAttribute."+groupAttributes+".translateFromStaticValues", objectClassesForGroups);
        
        provisionerSuffixToValue.put("targetGroupAttribute."+groupAttributes+".showAdvancedAttribute", "true");
        provisionerSuffixToValue.put("targetGroupAttribute."+groupAttributes+".showAttributeValueSettings", "true");
        provisionerSuffixToValue.put("targetGroupAttribute."+groupAttributes+".multiValued", "true");

        groupAttributes++;
      }
      
      String otherGroupLdapAttributes = startWithSuffixToValue.get("otherGroupLdapAttributes");
      if (StringUtils.isNotBlank(otherGroupLdapAttributes)) {
        
        Set<String> otherLdapAttributes = GrouperUtil.splitTrimToSet(otherGroupLdapAttributes, ",");
        
        for (String otherLdapAttribute: otherLdapAttributes) {
          provisionerSuffixToValue.put("targetGroupAttribute."+groupAttributes+".name", otherLdapAttribute);

          if (StringUtils.equals("description", otherLdapAttribute)) {
            provisionerSuffixToValue.put("targetGroupAttribute."+groupAttributes+".translateExpressionType", "grouperProvisioningGroupField");
            provisionerSuffixToValue.put("targetGroupAttribute."+groupAttributes+".translateFromGrouperProvisioningGroupField", "description");
          }
          
          groupAttributes++;
        }
        
      }      
    }
    
    if (StringUtils.equals(startWithSuffixToValue.get("membershipStructure"), "entityAttributes") || 
        GrouperUtil.booleanValue(startWithSuffixToValue.get("membershipValueDn"), false) ||
        GrouperUtil.booleanValue(startWithSuffixToValue.get("entityLinkForAnotherReason"), false)) {
      
      provisionerSuffixToValue.put("operateOnGrouperEntities", "true");
      
      provisionerSuffixToValue.put("hasTargetEntityLink", "true");
      
      provisionerSuffixToValue.put("operateOnGrouperEntities", "true");
      
      provisionerSuffixToValue.put("entityAttributeValueCacheHas", "true");
      
      provisionerSuffixToValue.put("entityAttributeValueCache0has", "true");
      provisionerSuffixToValue.put("entityAttributeValueCache0source", "target");
      provisionerSuffixToValue.put("entityAttributeValueCache0type", "entityAttribute");
      provisionerSuffixToValue.put("entityAttributeValueCache0entityAttribute", "ldap_dn");
      

      boolean changeEntitiesInLdap = GrouperUtil.booleanValue(startWithSuffixToValue.get("changeEntitiesInLdap"), false);
      
      provisionerSuffixToValue.put("targetEntityAttribute."+entityAttributes+".name", "ldap_dn");
      
      entityAttributes++;

      if (changeEntitiesInLdap) {
        String userRdnAttribute = startWithSuffixToValue.get("userRdnAttribute");
        String rdnValueForEntities = startWithSuffixToValue.get("rdnValueForEntities");
        
        
        provisionerSuffixToValue.put("targetEntityAttribute."+entityAttributes+".name", userRdnAttribute);
        
        if (StringUtils.equalsAny(rdnValueForEntities, "subjectId", "subjectIdentifier0", "subjectIdentifier1", "subjectIdentifier2", "idIndex")) {
          provisionerSuffixToValue.put("targetEntityAttribute."+entityAttributes+".translateFromGrouperProvisioningEntityField", rdnValueForEntities);
          provisionerSuffixToValue.put("targetEntityAttribute."+entityAttributes+".translateExpressionType", "grouperProvisioningEntityField");
          
        } else if (StringUtils.equalsAny(rdnValueForEntities, "script")) {
          provisionerSuffixToValue.put("targetEntityAttribute."+entityAttributes+".translateExpressionType", "translationScript");
          provisionerSuffixToValue.put("targetEntityAttribute."+entityAttributes+".translateExpression", startWithSuffixToValue.get("rdnValueForEntitiesTranslationScript"));
        }
        
        entityAttributes++;
        
      }
      
      boolean matchingSearchAttributeDifferentThanRDN = false;
      if (changeEntitiesInLdap) {
        matchingSearchAttributeDifferentThanRDN = GrouperUtil.booleanValue(startWithSuffixToValue.get("matchingSearchAttributeDifferentThanRDN"), false);
      }
      
      provisionerSuffixToValue.put("entityMatchingAttributeCount", "1");
      
      if (!changeEntitiesInLdap || matchingSearchAttributeDifferentThanRDN) {
        
        String matchingSearchAttributeNameForEntities = startWithSuffixToValue.get("matchingSearchAttributeNameForEntities");
        String matchingSearchAttributeValueForEntities = startWithSuffixToValue.get("matchingSearchAttributeValueForEntities");
        
        provisionerSuffixToValue.put("targetEntityAttribute."+entityAttributes+".name", matchingSearchAttributeNameForEntities);
        
        if (StringUtils.equalsAny(matchingSearchAttributeValueForEntities, "subjectId", "subjectIdentifier0", "subjectIdentifier1", "subjectIdentifier2", "idIndex")) {
          provisionerSuffixToValue.put("targetEntityAttribute."+entityAttributes+".translateExpressionType", "grouperProvisioningEntityField");
          provisionerSuffixToValue.put("targetEntityAttribute."+entityAttributes+".translateFromGrouperProvisioningEntityField", matchingSearchAttributeValueForEntities);
        } else if (StringUtils.equalsAny(matchingSearchAttributeValueForEntities, "script")){
          provisionerSuffixToValue.put("targetEntityAttribute."+entityAttributes+".translateExpressionType", "translationScript");
          provisionerSuffixToValue.put("targetEntityAttribute."+entityAttributes+".translateExpression", startWithSuffixToValue.get("matchingSearchAttributeValueForEntitiesTranslationScript"));
        }
        
        entityAttributes++;
        
        provisionerSuffixToValue.put("entityMatchingAttribute0name", matchingSearchAttributeNameForEntities);
        
        if (!"ldap_dn".equals(matchingSearchAttributeNameForEntities)) {
          provisionerSuffixToValue.put("entityAttributeValueCache1has", "true");
          provisionerSuffixToValue.put("entityAttributeValueCache1source", "target");
          provisionerSuffixToValue.put("entityAttributeValueCache1type", "entityAttribute");
          provisionerSuffixToValue.put("entityAttributeValueCache1entityAttribute", matchingSearchAttributeNameForEntities);
        }
      }
      
      
      String objectClassesForEntities = startWithSuffixToValue.get("objectClassesForEntities");
      if (StringUtils.isNotBlank(objectClassesForEntities)) {
        
        provisionerSuffixToValue.put("targetEntityAttribute."+entityAttributes+".name", "objectClass");
        provisionerSuffixToValue.put("targetEntityAttribute."+entityAttributes+".translateExpressionType", "staticValues");
        provisionerSuffixToValue.put("targetEntityAttribute."+entityAttributes+".translateFromStaticValues", objectClassesForEntities);
        provisionerSuffixToValue.put("targetEntityAttribute."+entityAttributes+".showAdvancedAttribute", "true");
        provisionerSuffixToValue.put("targetEntityAttribute."+entityAttributes+".showAttributeValueSettings", "true");
        provisionerSuffixToValue.put("targetEntityAttribute."+entityAttributes+".multiValued", "true");
        entityAttributes++;
      }
      
      String otherEntityLdapAttributes = startWithSuffixToValue.get("otherEntityLdapAttributes");
      if (StringUtils.isNotBlank(otherEntityLdapAttributes)) {
        
        Set<String> otherLdapAttributes = GrouperUtil.splitTrimToSet(otherEntityLdapAttributes, ",");
        
        for (String otherLdapAttribute: otherLdapAttributes) {
          provisionerSuffixToValue.put("targetEntityAttribute."+entityAttributes+".name", otherLdapAttribute);
          entityAttributes++;
        }
        
      }
      
      
      
//      if (StringUtils.equals(startWithSuffixToValue.get("membershipStructure"), "entityAttributes") && 
//          !GrouperUtil.booleanValue(startWithSuffixToValue.get("membershipValueDn"), false)) {
//        
//        boolean allowMembershipValueOverride = GrouperUtil.booleanValue(startWithSuffixToValue.get("allowMembershipValueOverride"), false);
//        
//      }
      
      if (GrouperUtil.booleanValue(startWithSuffixToValue.get("addDisabledFullSyncDaemon"), true) || GrouperUtil.booleanValue(startWithSuffixToValue.get("addDisabledIncrementalSyncDaemon"), true)) {
        provisionerSuffixToValue.put("showAdvanced", "true");
      }
    }
    

    if (StringUtils.equals(startWithSuffixToValue.get("membershipStructure"), "entityAttributes")) {
      
      String membershipAttributeNameForEntities = startWithSuffixToValue.get("membershipAttributeNameForEntities");
      
      provisionerSuffixToValue.put("targetEntityAttribute."+entityAttributes+".name", membershipAttributeNameForEntities);
      provisionerSuffixToValue.put("entityMembershipAttributeName", membershipAttributeNameForEntities);
      
      if (!GrouperUtil.booleanValue(startWithSuffixToValue.get("membershipValueDn"), false)) {
        
        // If we haven't done anything with groups yet, then need to add minimal config here
        if (groupAttributes == 0) {
          provisionerSuffixToValue.put("operateOnGrouperGroups", "true");
          provisionerSuffixToValue.put("customizeGroupCrud", "true");
          provisionerSuffixToValue.put("selectGroups", "false");
          provisionerSuffixToValue.put("insertGroups", "false");
          provisionerSuffixToValue.put("deleteGroups", "false");
          provisionerSuffixToValue.put("updateGroups", "false");
        }
        
        String membershipValueForEntities = startWithSuffixToValue.get("membershipValueForEntities");

        provisionerSuffixToValue.put("targetGroupAttribute."+groupAttributes+".name", membershipValueForEntities);

        if (StringUtils.equalsAny(membershipValueForEntities, "extension", "idIndex", "name", "id")) {
          provisionerSuffixToValue.put("targetGroupAttribute."+groupAttributes+".translateExpressionType", "grouperProvisioningGroupField");
          provisionerSuffixToValue.put("targetGroupAttribute."+groupAttributes+".translateFromGrouperProvisioningGroupField", membershipValueForEntities);
        } else if (StringUtils.equalsAny(membershipValueForEntities, "script")) {
          provisionerSuffixToValue.put("targetGroupAttribute."+groupAttributes+".translateExpressionType", "translationScript");
          provisionerSuffixToValue.put("targetGroupAttribute."+groupAttributes+".translateExpression", startWithSuffixToValue.get("membershipValueForEntitiesTranslationScript"));
        }
        
        groupAttributes++;
        
        // cache membership
        int nextAvailableGroupCacheBucketNumber = getNextAvailableGroupCacheBucketNumber(provisionerSuffixToValue);
        provisionerSuffixToValue.put("entityMembershipAttributeValue", "groupAttributeValueCache" + nextAvailableGroupCacheBucketNumber);
        
        provisionerSuffixToValue.put("groupAttributeValueCacheHas", "true");
        provisionerSuffixToValue.put("groupAttributeValueCache" + nextAvailableGroupCacheBucketNumber + "has", "true");
        provisionerSuffixToValue.put("groupAttributeValueCache" + nextAvailableGroupCacheBucketNumber + "source", "grouper");
        provisionerSuffixToValue.put("groupAttributeValueCache" + nextAvailableGroupCacheBucketNumber + "type", "groupAttribute");
        provisionerSuffixToValue.put("groupAttributeValueCache" + nextAvailableGroupCacheBucketNumber + "groupAttribute", membershipValueForEntities);

      } else {
        provisionerSuffixToValue.put("entityMembershipAttributeValue", "groupAttributeValueCache0");
      }
      
      entityAttributes++; 
    }
    
    if (StringUtils.equals(startWithSuffixToValue.get("membershipStructure"), "groupAttributes")) {

      String membershipAttributeNameForGroups = startWithSuffixToValue.get("membershipAttributeNameForGroups");
      
      provisionerSuffixToValue.put("targetGroupAttribute."+groupAttributes+".name", membershipAttributeNameForGroups);
      provisionerSuffixToValue.put("groupMembershipAttributeName", membershipAttributeNameForGroups);
      
      if (!GrouperUtil.booleanValue(startWithSuffixToValue.get("membershipValueDn"), false)) {

        // If we haven't done anything with entities yet, then need to add minimal config here
        if (entityAttributes == 0) {
          provisionerSuffixToValue.put("operateOnGrouperEntities", "true");
          provisionerSuffixToValue.put("customizeEntityCrud", "true");
          provisionerSuffixToValue.put("selectEntities", "false");
        }
        
        String membershipValueForGroups = startWithSuffixToValue.get("membershipValueForGroups");
        provisionerSuffixToValue.put("targetEntityAttribute."+entityAttributes+".name", membershipValueForGroups);

        if (StringUtils.equalsAny(membershipValueForGroups, "email", "idIndex", "name",  "subjectId", "subjectIdentifier0", "subjectIdentifier1", "subjectIdentifier2")) {
          provisionerSuffixToValue.put("targetEntityAttribute."+entityAttributes+".translateExpressionType", "grouperProvisioningEntityField");
          provisionerSuffixToValue.put("targetEntityAttribute."+entityAttributes+".translateFromGrouperProvisioningEntityField", membershipValueForGroups);
        } else if (StringUtils.equalsAny(membershipValueForGroups, "script")) {
          provisionerSuffixToValue.put("targetEntityAttribute."+entityAttributes+".translateExpressionType", "translationScript");
          provisionerSuffixToValue.put("targetEntityAttribute."+entityAttributes+".translateExpression", startWithSuffixToValue.get("membershipValueForGroupsTranslationScript"));
        }
 
        entityAttributes++;
        
        // cache membership
        int nextAvailableEntityCacheBucketNumber = getNextAvailableEntityCacheBucketNumber(provisionerSuffixToValue);
        provisionerSuffixToValue.put("groupMembershipAttributeValue", "entityAttributeValueCache" + nextAvailableEntityCacheBucketNumber);
        
        provisionerSuffixToValue.put("entityAttributeValueCacheHas", "true");
        provisionerSuffixToValue.put("entityAttributeValueCache" + nextAvailableEntityCacheBucketNumber + "has", "true");
        provisionerSuffixToValue.put("entityAttributeValueCache" + nextAvailableEntityCacheBucketNumber + "source", "grouper");
        provisionerSuffixToValue.put("entityAttributeValueCache" + nextAvailableEntityCacheBucketNumber + "type", "entityAttribute");
        provisionerSuffixToValue.put("entityAttributeValueCache" + nextAvailableEntityCacheBucketNumber + "entityAttribute", membershipValueForGroups);


      } else {
        provisionerSuffixToValue.put("groupMembershipAttributeValue", "entityAttributeValueCache0");
        
      }
      
      String membershipAttributeDefaultValue = startWithSuffixToValue.get("membershipAttributeDefaultValue");
      if (!StringUtils.isEmpty(membershipAttributeDefaultValue)) {
        provisionerSuffixToValue.put("targetGroupAttribute."+groupAttributes+".showAdvancedAttribute", true);
        provisionerSuffixToValue.put("targetGroupAttribute."+groupAttributes+".showAttributeValueSettings", true);
        provisionerSuffixToValue.put("targetGroupAttribute."+groupAttributes+".defaultValue", membershipAttributeDefaultValue);
      }
      
      groupAttributes++; 
    }
    
    provisionerSuffixToValue.put("numberOfGroupAttributes", groupAttributes);
    provisionerSuffixToValue.put("numberOfEntityAttributes", entityAttributes);
  }
  
  

  @Override
  public void validatePreSave(boolean isInsert, List<String> errorsToDisplay, Map<String, String> validationErrorsToDisplay) {
    super.validatePreSave(isInsert, errorsToDisplay, validationErrorsToDisplay);
    
    if (errorsToDisplay.size() > 0 || validationErrorsToDisplay.size() > 0) {
      return;
    } 
    
    GrouperConfigurationModuleAttribute ldapPatternAttribute = this.retrieveAttributes().get("ldapPattern");
    
    String ldapPattern = ldapPatternAttribute.getValueOrExpressionEvaluation();
    
    if (StringUtils.equals(ldapPattern, "activeDirectoryGroups")) {
      GrouperConfigurationModuleAttribute ldapExternalSystemConfigId = this.retrieveAttributes().get("ldapExternalSystemConfigId");
      String ldapExternalSystemId = ldapExternalSystemConfigId.getValueOrExpressionEvaluation();
      
      boolean isActiveDirectory = GrouperLoaderConfig.retrieveConfig().propertyValueBoolean("ldap."+ldapExternalSystemId+".isActiveDirectory", false);
      
      if (!isActiveDirectory) {
        String errorMessage = GrouperTextContainer.textOrNull("grouperStartWithLdapConfigurationValidationExternalSystemNotActiveDirectory");
        errorMessage = errorMessage.replace("##externalSystemId##", ldapExternalSystemId);
        validationErrorsToDisplay.put(ldapExternalSystemConfigId.getHtmlForElementIdHandle(), errorMessage);
      }
      
    }
    
    GrouperConfigurationModuleAttribute subjectSourceEntityResoverModuleAttribute = this.retrieveAttributes().get("subjectSourceEntityResolverAttributes");
    if (subjectSourceEntityResoverModuleAttribute != null && StringUtils.isNotBlank(subjectSourceEntityResoverModuleAttribute.getValue())) {
      String commaSeparatedResolverAttributes = subjectSourceEntityResoverModuleAttribute.getValue();
      List<String> list = GrouperUtil.splitTrimToList(commaSeparatedResolverAttributes, ",");
      if (list.size() > 2) {
        String errorMessage = GrouperTextContainer.textOrNull("subjectSourceEntityResolverAttributesTooManyAttributes");
        validationErrorsToDisplay.put(subjectSourceEntityResoverModuleAttribute.getHtmlForElementIdHandle(), errorMessage);
      }
    }
    
    //TODO wait until all source attributes are exposed 
    // list those attributes and validate against subject source (textfield, comma separated attributes, required)
//    GrouperConfigurationModuleAttribute entityResolverAttributes = this.retrieveAttributes().get("subjectSourceEntityResolverAttributes");
//    if (entityResolverAttributes != null) {
//      String subjectAttributesCommaSeparated = entityResolverAttributes.getValueOrExpressionEvaluation();
//      Set<String> sujectAttributes = GrouperUtil.splitTrimToSet(subjectAttributesCommaSeparated, ",");
//      
//      for (Source source: SourceManager.getInstance().getSources()) {
//        Set<String> attributes = source.getInternalAttributes();
//        sujectAttributes.removeAll(attributes);
//      }
//      
//      if (sujectAttributes.size() > 0) {
//        String notFoundAttributes = GrouperUtil.join(sujectAttributes.iterator(), ',');
//        String errorMessage = GrouperTextContainer.textOrNull("grouperStartWithLdapConfigurationValidationSubjectAttributesNotValid");
//        errorMessage = errorMessage.replace("$$subjectAttributes$$", notFoundAttributes);
//        validationErrorsToDisplay.put(entityResolverAttributes.getHtmlForElementIdHandle(), errorMessage);
//      }
//      
//    }
    
    
  }

  @Override
  public Map<String, String> screenRedraw(Map<String, String> suffixToValue, Set<String> suffixesUserJustChanged) {
    
    Map<String, String> result = new HashMap<>();
    
    for (String suffixUserJustChanged: suffixesUserJustChanged) {
      
      if (StringUtils.equals(suffixUserJustChanged, "ldapPattern")) {
        String valueUserEnteredOnScreen = suffixToValue.get(suffixUserJustChanged);
        if (StringUtils.equals(valueUserEnteredOnScreen, "activeDirectoryGroups")) {
          result.put("membershipStructure", "groupAttributes");
          result.put("objectClassesForGroups", "top,group");
          result.put("membershipAttributeNameForGroups", "member");
          result.put("membershipValueDn", "true");
          result.put("groupDnType", "bushy");
        } else if (StringUtils.equals(valueUserEnteredOnScreen, "bushyGroupsWithMembershipDNs")) {
          result.put("membershipStructure", "groupAttributes");
          result.put("membershipValueDn", "true");
          result.put("groupDnType", "bushy");
        } else if (StringUtils.equals(valueUserEnteredOnScreen, "bushyGroupsWithMembershipSubjectIds")) {
          result.put("userAttributesType", "core");
          result.put("membershipStructure", "groupAttributes");
          result.put("membershipValueDn", "false");
          result.put("groupDnType", "bushy");
        } else if (StringUtils.equals(valueUserEnteredOnScreen, "flatGroupsWithMembershipDNs")) {
          result.put("membershipStructure", "groupAttributes");
          result.put("membershipValueDn", "true");
          result.put("groupDnType", "flat");
        } else if (StringUtils.equals(valueUserEnteredOnScreen, "flatGroupsWithMembershipSubjectIds")) {
          result.put("userAttributesType", "core");
          result.put("membershipStructure", "groupAttributes");
          result.put("membershipValueDn", "false");
          result.put("groupDnType", "flat");
        } else if (StringUtils.equals(valueUserEnteredOnScreen, "groupOfNames")) {
          result.put("membershipStructure", "groupAttributes");
          result.put("objectClassesForGroups", "top,groupOfNames");
          result.put("membershipAttributeNameForGroups", "member");
          result.put("membershipValueDn", "true");
        } else if (StringUtils.equals(valueUserEnteredOnScreen, "posixGroups")) {
          result.put("membershipStructure", "groupAttributes");
          result.put("objectClassesForGroups", "top,posixGroup");
          result.put("membershipValueDn", "true");
          result.put("membershipAttributeNameForGroups", "member");
          result.put("idIndexAttribute", "gidNumber");
        } else if (StringUtils.equals(valueUserEnteredOnScreen, "usersWithEduPersonAffiliations")) {
          result.put("membershipStructure", "entityAttributes");
          result.put("membershipValueDn", "false");
          result.put("membershipAttributeNameForEntities", "eduPersonAffiliation");
          result.put("membershipValueForEntities", "extension");
        } else if (StringUtils.equals(valueUserEnteredOnScreen, "usersWithEduPersonEntitlements")) {
          result.put("membershipStructure", "entityAttributes");
          result.put("membershipValueDn", "false");
          result.put("membershipAttributeNameForEntities", "eduPersonEntitlement");
          result.put("membershipValueForEntities", "name");
        } else if (StringUtils.equals(valueUserEnteredOnScreen, "usersWithMembershipGroupExtensions")) {
          result.put("membershipStructure", "entityAttributes");
          result.put("membershipValueDn", "false");
          result.put("membershipValueForEntities", "extension");
        } else if (StringUtils.equals(valueUserEnteredOnScreen, "usersWithMembershipGroupNames")) {
          result.put("membershipStructure", "entityAttributes");
          result.put("membershipValueDn", "false");
          result.put("membershipValueForEntities", "name");
        } else if (StringUtils.equals(valueUserEnteredOnScreen, "other")) {
          result.clear();
        }
      }
      
    }
    
    return result;
  }
  
  @Override
  public Class<? extends ProvisioningConfiguration> getProvisioningConfiguration() {
    return LdapProvisionerConfiguration.class;
  }
  
  private int getNextAvailableGroupCacheBucketNumber(Map<String, Object> provisionerSuffixToValue) {
    if (!provisionerSuffixToValue.containsKey("groupAttributeValueCache0has")) {
      return 0;
    }
    
    if (!provisionerSuffixToValue.containsKey("groupAttributeValueCache1has")) {
      return 1;
    }
    
    if (!provisionerSuffixToValue.containsKey("groupAttributeValueCache2has")) {
      return 2;
    }
    
    if (!provisionerSuffixToValue.containsKey("groupAttributeValueCache3has")) {
      return 3;
    }
    
    throw new RuntimeException("Unable to find an available group cache bucket");
  }
  
  private int getNextAvailableEntityCacheBucketNumber(Map<String, Object> provisionerSuffixToValue) {
    if (!provisionerSuffixToValue.containsKey("entityAttributeValueCache0has")) {
      return 0;
    }
    
    if (!provisionerSuffixToValue.containsKey("entityAttributeValueCache1has")) {
      return 1;
    }
    
    if (!provisionerSuffixToValue.containsKey("entityAttributeValueCache2has")) {
      return 2;
    }
    
    if (!provisionerSuffixToValue.containsKey("entityAttributeValueCache3has")) {
      return 3;
    }
    
    throw new RuntimeException("Unable to find an available entity cache bucket");
  }
}
