/**
 * 
 */
package edu.internet2.middleware.grouper.app.ldapProvisioning;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.internet2.middleware.grouper.app.config.GrouperConfigurationModuleAttribute;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.provisioning.ProvisionerStartWithBase;
import edu.internet2.middleware.grouper.cfg.text.GrouperTextContainer;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import org.apache.commons.lang3.StringUtils;

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
        // by this time the validation is already done that there are no more than 3 attributes
        for (int i=0; i<attributes.length; i++) {
          int j = i+1;
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
      
      int groupAttributes = 0;
      
      String groupRdnAttribute = startWithSuffixToValue.get("groupRdnAttribute");
      
      provisionerSuffixToValue.put("targetGroupAttribute."+groupAttributes+".name", "ldap_dn");
      
      provisionerSuffixToValue.put("groupAttributeValueCacheHas", "true");
      provisionerSuffixToValue.put("groupAttributeValueCache0has", "true");
      provisionerSuffixToValue.put("groupAttributeValueCache0source", "target");
      provisionerSuffixToValue.put("groupAttributeValueCache0type", "groupAttribute");
      
      provisionerSuffixToValue.put("groupAttributeValueCache0groupAttribute", "ldap_dn");

      groupAttributes++;
      
      provisionerSuffixToValue.put("targetGroupAttribute."+groupAttributes+".name", groupRdnAttribute);

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
        }
                
      }
      groupAttributes++;

      if (StringUtils.equals(startWithSuffixToValue.get("membershipStructure"), "groupAttributes")) {

        String membershipAttributeNameForGroups = startWithSuffixToValue.get("membershipAttributeNameForGroups");
        
        provisionerSuffixToValue.put("targetGroupAttribute."+groupAttributes+".name", membershipAttributeNameForGroups);
        provisionerSuffixToValue.put("groupMembershipAttributeName", membershipAttributeNameForGroups);
        
        if (!GrouperUtil.booleanValue(startWithSuffixToValue.get("membershipValueDn"), false)) {
          
          // groupMembershipAttributeValue will be copied over
        } else {
          provisionerSuffixToValue.put("groupMembershipAttributeValue", "entityAttributeValueCache0");
          
        }
        
        groupAttributes++;
        
      }
      
      String idIndexAttribute = startWithSuffixToValue.get("idIndexAttribute");
      if (StringUtils.isNotBlank(idIndexAttribute)) {
        provisionerSuffixToValue.put("targetGroupAttribute."+groupAttributes+".name", idIndexAttribute);
        provisionerSuffixToValue.put("targetGroupAttribute."+groupAttributes+".translateExpressionType", "grouperProvisioningGroupField");
        provisionerSuffixToValue.put("targetGroupAttribute."+groupAttributes+".translateFromGrouperProvisioningGroupField", "idIndex");

        groupAttributes++;
        
      }
      
      provisionerSuffixToValue.put("groupMatchingAttributeCount", "1");
      
      if (GrouperUtil.booleanValue(startWithSuffixToValue.get("matchingSearchAttributeDifferentThanRdnorIdIndex"), false)) {
        
        String matchingSearchAttributeNameForGroups = startWithSuffixToValue.get("matchingSearchAttributeNameForGroups");
        String matchingSearchAttributeValueForGroups = startWithSuffixToValue.get("matchingSearchAttributeValueForGroups");
        
        
        provisionerSuffixToValue.put("targetGroupAttribute."+groupAttributes+".name", matchingSearchAttributeNameForGroups);
        
        if (StringUtils.equalsAny(matchingSearchAttributeValueForGroups, "extension", "idIndex", "name", "id")) {
          provisionerSuffixToValue.put("targetGroupAttribute."+groupAttributes+".translateExpressionType", "grouperProvisioningGroupField");
          provisionerSuffixToValue.put("targetGroupAttribute."+groupAttributes+".translateFromGrouperProvisioningGroupField", matchingSearchAttributeValueForGroups);
        } else if (StringUtils.equalsAny(matchingSearchAttributeValueForGroups, "script")){
          provisionerSuffixToValue.put("targetGroupAttribute."+groupAttributes+".translateExpressionType", "translationScript");
        }
        
        groupAttributes++;
        
        provisionerSuffixToValue.put("groupMatchingAttribute0name", matchingSearchAttributeNameForGroups);
        
      } else {
        
        provisionerSuffixToValue.put("groupMatchingAttribute0name", idIndexAttribute);
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
      
      provisionerSuffixToValue.put("numberOfGroupAttributes", groupAttributes);
      
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
      
      int entityAttributes = 0;

      provisionerSuffixToValue.put("targetEntityAttribute."+entityAttributes+".name", "ldap_dn");
      
      entityAttributes++;

      if (changeEntitiesInLdap) {
        String userRdnAttribute = startWithSuffixToValue.get("userRdnAttribute");
        String rdnValueForEntities = startWithSuffixToValue.get("rdnValueForEntities");
        
        
        provisionerSuffixToValue.put("targetEntityAttribute."+entityAttributes+".name", userRdnAttribute);
        
        if (StringUtils.equalsAny(rdnValueForEntities, "subjectId", "subjectIdentifier0", "subjectIdentifier1", "subjectIdentifier2")) {
          provisionerSuffixToValue.put("targetEntityAttribute."+entityAttributes+".translateFromGrouperProvisioningEntityField", rdnValueForEntities);
          provisionerSuffixToValue.put("targetEntityAttribute."+entityAttributes+".translateExpressionType", "grouperProvisioningEntityField");
          
        } else if (StringUtils.equalsAny(rdnValueForEntities, "script")) {
          provisionerSuffixToValue.put("targetEntityAttribute."+entityAttributes+".translateExpressionType", "translationScript");
        }
        
        entityAttributes++;
        
      }
      
      if (StringUtils.equals(startWithSuffixToValue.get("membershipStructure"), "entityAttributes")) {
        
        String membershipAttributeNameForEntities = startWithSuffixToValue.get("membershipAttributeNameForEntities");
        
        provisionerSuffixToValue.put("targetEntityAttribute."+entityAttributes+".name", membershipAttributeNameForEntities);
        provisionerSuffixToValue.put("entityMembershipAttributeName", membershipAttributeNameForEntities);
        
        if (!GrouperUtil.booleanValue(startWithSuffixToValue.get("membershipValueDn"), false)) {
          
          String membershipValueForEntities = startWithSuffixToValue.get("membershipValueForEntities");
          
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
        
        if (StringUtils.equalsAny(matchingSearchAttributeValueForEntities, "subjectId", "subjectIdentifier0", "subjectIdentifier1", "subjectIdentifier2")) {
          provisionerSuffixToValue.put("targetEntityAttribute."+entityAttributes+".translateExpressionType", "grouperProvisioningEntityField");
          provisionerSuffixToValue.put("targetEntityAttribute."+entityAttributes+".translateFromGrouperProvisioningEntityField", matchingSearchAttributeValueForEntities);
        } else if (StringUtils.equalsAny(matchingSearchAttributeValueForEntities, "script")){
          provisionerSuffixToValue.put("targetEntityAttribute."+entityAttributes+".translateExpressionType", "translationScript");
        }
        
        entityAttributes++;
        
        provisionerSuffixToValue.put("entityMatchingAttribute0name", matchingSearchAttributeNameForEntities);
        
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
      
      
      provisionerSuffixToValue.put("numberOfEntityAttributes", entityAttributes);
      
//      if (StringUtils.equals(startWithSuffixToValue.get("membershipStructure"), "entityAttributes") && 
//          !GrouperUtil.booleanValue(startWithSuffixToValue.get("membershipValueDn"), false)) {
//        
//        boolean allowMembershipValueOverride = GrouperUtil.booleanValue(startWithSuffixToValue.get("allowMembershipValueOverride"), false);
//        
//      }
      
      if (GrouperUtil.booleanValue(startWithSuffixToValue.get("addDisabledFullSyncDaemon"), false) || GrouperUtil.booleanValue(startWithSuffixToValue.get("addDisabledIncrementalSyncDaemon"), false)) {
        provisionerSuffixToValue.put("showAdvanced", "true");
      }

      
    }
    
    
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
        errorMessage = errorMessage.replace("$$externalSystemId$$", ldapExternalSystemId);
        validationErrorsToDisplay.put(ldapExternalSystemConfigId.getHtmlForElementIdHandle(), errorMessage);
      }
      
    }
    
    GrouperConfigurationModuleAttribute subjectSourceEntityResoverModuleAttribute = this.retrieveAttributes().get("subjectSourceEntityResolverAttributes");
    if (subjectSourceEntityResoverModuleAttribute != null && StringUtils.isNotBlank(subjectSourceEntityResoverModuleAttribute.getValue())) {
      String commaSeparatedResolverAttributes = subjectSourceEntityResoverModuleAttribute.getValue();
      List<String> list = GrouperUtil.splitTrimToList(commaSeparatedResolverAttributes, ",");
      if (list.size() > 3) {
        String errorMessage = GrouperTextContainer.textOrNull("subjectSourceEntityResolverAttributesMoreThanThreeAttributes");
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
          result.put("groupMembershipAttributeValue", "subjectId");
        } else if (StringUtils.equals(valueUserEnteredOnScreen, "flatGroupsWithMembershipDNs")) {
          result.put("membershipStructure", "groupAttributes");
          result.put("membershipValueDn", "true");
          result.put("groupDnType", "flat");
        } else if (StringUtils.equals(valueUserEnteredOnScreen, "flatGroupsWithMembershipSubjectIds")) {
          result.put("userAttributesType", "core");
          result.put("membershipStructure", "groupAttributes");
          result.put("membershipValueDn", "false");
          result.put("groupDnType", "flat");
          result.put("groupMembershipAttributeValue", "subjectId");
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
  
  
  
}
