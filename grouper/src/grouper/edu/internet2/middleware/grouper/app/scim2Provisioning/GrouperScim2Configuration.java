package edu.internet2.middleware.grouper.app.scim2Provisioning;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.app.config.GrouperConfigurationModuleAttribute;
import edu.internet2.middleware.grouper.app.provisioning.ProvisionerStartWithBase;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningConfiguration;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;
import edu.internet2.middleware.grouper.cfg.text.GrouperTextContainer;
import edu.internet2.middleware.grouper.util.GrouperUtil;

public class GrouperScim2Configuration extends ProvisioningConfiguration {
  
  public final static Set<String> startWithConfigClassNames = new LinkedHashSet<String>();
  
  static {
    startWithConfigClassNames.add(ScimProvisioningStartWith.class.getName());
  }
  
  @Override
  public List<ProvisionerStartWithBase> getStartWithConfigClasses() {
    
    List<ProvisionerStartWithBase> result = new ArrayList<ProvisionerStartWithBase>();
    
    for (String className: startWithConfigClassNames) {
      try {
        Class<ProvisionerStartWithBase> configClass = (Class<ProvisionerStartWithBase>) GrouperUtil.forName(className);
        ProvisionerStartWithBase config = GrouperUtil.newInstance(configClass);
        result.add(config);
      } catch (Exception e) {
        //TODO
      }
    }
    
    return result;
    
  }

  @Override
  public ConfigFileName getConfigFileName() {
    return ConfigFileName.GROUPER_LOADER_PROPERTIES;
  }

  @Override
  public String getConfigItemPrefix() {
    if (StringUtils.isBlank(this.getConfigId())) {
      throw new RuntimeException("Must have configId!");
    }
    return "provisioner." + this.getConfigId() + ".";
  }

  @Override
  public String getConfigIdRegex() {
    return "^(provisioner)\\.([^.]+)\\.(.*)$";
  }
  
  @Override
  public String getPropertySuffixThatIdentifiesThisConfig() {
    return "class";
  }

  @Override
  public String getPropertyValueThatIdentifiesThisConfig() {
    return GrouperScim2Provisioner.class.getName();
  }

  private void assignCacheConfig() {
    
  }
  
  @Override
  public void validatePreSave(boolean isInsert, List<String> errorsToDisplay, Map<String, String> validationErrorsToDisplay) {
    
    super.validatePreSave(isInsert, errorsToDisplay, validationErrorsToDisplay);
    
    if (errorsToDisplay.size() > 0 || validationErrorsToDisplay.size() > 0) {
      return;
    } 
    
    GrouperConfigurationModuleAttribute numberOfEntityAttributes = this.retrieveAttributes().get("numberOfEntityAttributes");
    
    int numberOfEntityAttributesLength = 0;
    
    if (numberOfEntityAttributes != null) {
      
      numberOfEntityAttributesLength = GrouperUtil.intValue(numberOfEntityAttributes.getValueOrExpressionEvaluation(), 0);
      
      Set<String> predefinedAttributes = GrouperUtil.toSet("active", "costCenter", "department", "displayName", "division", "emailType", "emailValue", "emailType2",
          "emailValue2", "employeeNumber", "externalId", "familyName", "formattedName", "givenName", "id", "middleName", "phoneNumber",
          "phoneNumberType", "phoneNumber2", "phoneNumberType2", "schemas", "title", "userName", "userType");
      
      for (int i=0; i<numberOfEntityAttributesLength; i++) {
        
        GrouperConfigurationModuleAttribute jsonPointerAttribute = this.retrieveAttributes().get("targetEntityAttribute."+i+".entityAttributeJsonPointer");
        if (jsonPointerAttribute != null && StringUtils.isNotBlank(jsonPointerAttribute.getValueOrExpressionEvaluation())) {
          GrouperConfigurationModuleAttribute attributeName = this.retrieveAttributes().get("targetEntityAttribute."+i+".name");
          String attributeNameString = attributeName.getValueOrExpressionEvaluation();
          if (predefinedAttributes.contains(attributeNameString)) {
            String errorMessage = GrouperTextContainer.textOrNull("scim2InvalidAttributeName");
            errorsToDisplay.add(errorMessage);
          }
        }
      }
    }
    
    GrouperConfigurationModuleAttribute numberOfGroupAttributes = this.retrieveAttributes().get("numberOfGroupAttributes");
    
    int numberOfGroupAttributesLength = 0;
    
    if (numberOfGroupAttributes != null) {
      
      numberOfGroupAttributesLength = GrouperUtil.intValue(numberOfGroupAttributes.getValueOrExpressionEvaluation(), 0);
      
      Set<String> predefinedAttributes = GrouperUtil.toSet("displayName", "id");
      
      for (int i=0; i<numberOfGroupAttributesLength; i++) {
        
        GrouperConfigurationModuleAttribute jsonPointerAttribute = this.retrieveAttributes().get("targetGroupAttribute."+i+".groupAttributeJsonPointer");
        if (jsonPointerAttribute != null && StringUtils.isNotBlank(jsonPointerAttribute.getValueOrExpressionEvaluation())) {
          GrouperConfigurationModuleAttribute attributeName = this.retrieveAttributes().get("targetGroupAttribute."+i+".name");
          String attributeNameString = attributeName.getValueOrExpressionEvaluation();
          if (predefinedAttributes.contains(attributeNameString)) {
            String errorMessage = GrouperTextContainer.textOrNull("scim2InvalidAttributeName");
            errorsToDisplay.add(errorMessage);
          }
        }
      }
    }
    
  }
  
  @Override
  public void insertConfig(boolean fromUi, StringBuilder message,
      List<String> errorsToDisplay, Map<String, String> validationErrorsToDisplay, List<String> actionsPerformed) {
    assignCacheConfig();
    super.insertConfig(fromUi, message, errorsToDisplay, validationErrorsToDisplay, actionsPerformed);
  }

  @Override
  public void editConfig(boolean fromUi, StringBuilder message,
      List<String> errorsToDisplay, Map<String, String> validationErrorsToDisplay, List<String> actionsPerformed) {
    assignCacheConfig();
    super.editConfig(fromUi, message, errorsToDisplay, validationErrorsToDisplay, actionsPerformed);
  }
  
  

}
