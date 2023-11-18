package edu.internet2.middleware.grouper.app.messagingProvisioning;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.internet2.middleware.grouper.app.config.GrouperConfigurationModuleAttribute;
import edu.internet2.middleware.grouper.app.google.GoogleProvisionerConfiguration;
import edu.internet2.middleware.grouper.app.provisioning.ProvisionerStartWithBase;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningConfiguration;
import edu.internet2.middleware.grouper.cfg.text.GrouperTextContainer;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.lang3.StringUtils;

public class MessagingProvisioningStartWith extends ProvisionerStartWithBase {
  
  @Override
  public String getPropertyValueThatIdentifiesThisConfig() {
    return "messagingCommon";
  }
  
  @Override
  public void populateProvisionerConfigurationValuesFromStartWith(
      Map<String, String> startWithSuffixToValue,
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
        // by this time the validation is already done that there are no more than 2 attributes
        for (int i=0; i<attributes.length; i++) {
          int j = i+2;
          provisionerSuffixToValue.put("entityAttributeValueCache"+j+"has", "true");
          provisionerSuffixToValue.put("entityAttributeValueCache"+j+"source", "grouper");
          provisionerSuffixToValue.put("entityAttributeValueCache"+j+"type", "subjectTranslationScript");
          provisionerSuffixToValue.put("entityAttributeValueCache"+j+"translationScript", "${subject.getAttributeValue('"+attributes[i]+"')}");
        }
        
      }
      
    }
    
    provisionerSuffixToValue.put("messagingType", startWithSuffixToValue.get("messagingType"));
    provisionerSuffixToValue.put("messagingActiveMqExternalSystemConfigId", startWithSuffixToValue.get("messagingActiveMqExternalSystemConfigId"));
    provisionerSuffixToValue.put("messagingAwsSqsExternalSystemConfigId", startWithSuffixToValue.get("messagingAwsSqsExternalSystemConfigId"));
    provisionerSuffixToValue.put("messagingRabbitMqExternalSystemConfigId", startWithSuffixToValue.get("messagingRabbitMqExternalSystemConfigId"));
    provisionerSuffixToValue.put("queueType", startWithSuffixToValue.get("queueType"));
    provisionerSuffixToValue.put("queueOrTopicName", startWithSuffixToValue.get("queueOrTopicName"));
    provisionerSuffixToValue.put("routingKey", startWithSuffixToValue.get("routingKey"));
    provisionerSuffixToValue.put("exchangeType", startWithSuffixToValue.get("exchangeType"));
    
    provisionerSuffixToValue.put("deleteEntities", "true");
    provisionerSuffixToValue.put("deleteEntitiesIfGrouperDeleted", "true");
    provisionerSuffixToValue.put("deleteGroups", "true");
    
    provisionerSuffixToValue.put("deleteGroupsIfGrouperDeleted", "true");
    provisionerSuffixToValue.put("deleteMemberships", "true");
    provisionerSuffixToValue.put("deleteMembershipsIfGrouperDeleted", "true");
    provisionerSuffixToValue.put("insertEntities", "true");
    provisionerSuffixToValue.put("customizeEntityCrud", "true");
    provisionerSuffixToValue.put("makeChangesToEntities", "true");
    provisionerSuffixToValue.put("customizeGroupCrud", "true");
    provisionerSuffixToValue.put("insertGroups", "true");
    provisionerSuffixToValue.put("customizeMembershipCrud", "true");
    provisionerSuffixToValue.put("insertMemberships", "true");
    
    provisionerSuffixToValue.put("updateEntities", "true");
    provisionerSuffixToValue.put("updateGroups", "true");
    
    provisionerSuffixToValue.put("operateOnGrouperEntities", "true");
    provisionerSuffixToValue.put("operateOnGrouperGroups", "true");
    provisionerSuffixToValue.put("operateOnGrouperMemberships", "true");
    
    provisionerSuffixToValue.put("messagingFormatType", "EsbEventJson");
    provisionerSuffixToValue.put("operateOnGrouperMemberships", "true");
    provisionerSuffixToValue.put("provisioningType", "membershipObjects");
    provisionerSuffixToValue.put("class", "edu.internet2.middleware.grouper.app.messagingProvisioning.GrouperMessagingProvisioner");
    
    if (GrouperUtil.booleanValue(startWithSuffixToValue.get("addDisabledFullSyncDaemon"), true) || GrouperUtil.booleanValue(startWithSuffixToValue.get("addDisabledIncrementalSyncDaemon"), true)) {
      provisionerSuffixToValue.put("showAdvanced", "true");
    }

  }

  @Override
  public Map<String, String> screenRedraw(Map<String, String> suffixToValue,
      Set<String> suffixesUserJustChanged) {
    
    Map<String, String> result = new HashMap<>();
    return result;
  }

  @Override
  public void validatePreSave(boolean isInsert, List<String> errorsToDisplay, Map<String, String> validationErrorsToDisplay) {
    
    super.validatePreSave(isInsert, errorsToDisplay, validationErrorsToDisplay);
    
    if (errorsToDisplay.size() > 0 || validationErrorsToDisplay.size() > 0) {
      return;
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
    
  }

  @Override
  public Class<? extends ProvisioningConfiguration> getProvisioningConfiguration() {
    return MessagingProvisionerConfiguration.class;
  }


}
