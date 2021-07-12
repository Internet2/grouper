package edu.internet2.middleware.grouper.app.scim2Provisioning;

import java.util.Set;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfigurationValidation;
import edu.internet2.middleware.grouper.cfg.text.GrouperTextContainer;
import edu.internet2.middleware.grouper.util.GrouperUtil;


public class Scim2SyncConfigurationValidation extends GrouperProvisioningConfigurationValidation {

  
  @Override
  public void validateFromSuffixValueMap() {
    super.validateFromSuffixValueMap();
    
    
    int numberOfAttributes = GrouperUtil.intValue(this.getSuffixToConfigValue().get("numberOfGroupAttributes"), 0);
    if (numberOfAttributes > 0) {
      validateRequiredFieldsExistForGroup(GrouperUtil.toSet("id"));
    }
    
    numberOfAttributes = GrouperUtil.intValue(this.getSuffixToConfigValue().get("numberOfEntityAttributes"), 0);
    if (numberOfAttributes > 0) {
      validateRequiredFieldsExistForEntity(GrouperUtil.toSet("id"));
    }
    
    String scimType = this.getSuffixToConfigValue().get("scimType");
    if (StringUtils.equals("Github", scimType)) {
     
    } else if (StringUtils.equals("AWS", scimType)) {
      validateRequiredFieldsExistForGroup(GrouperUtil.toSet("displayName", "id"));
      validateRequiredFieldsExistForEntity(GrouperUtil.toSet("id"));
      validateAWSAttributesForEntity();
    }

  }
  
  protected void validateRequiredFieldsExistForGroup(Set<String> fieldsRequired) {
    
    int countOfFields = 0;
    
    for (int i=0; i< 20; i++) {

      Boolean isField = GrouperUtil.booleanObjectValue(this.getSuffixToConfigValue().get("targetGroupAttribute."+i+".isFieldElseAttribute"));
      if (isField == null || isField == false) {
        continue;
      }
      
      countOfFields++;
      String nameConfigKey = "targetGroupAttribute."+i+".fieldName"; 
      String name = this.getSuffixToConfigValue().get(nameConfigKey);
      String type = this.getSuffixToConfigValue().get("targetGroupAttribute."+i+".valueType");
      
      if (fieldsRequired.contains(name)) {
        
        fieldsRequired.remove(name);
        
        if (StringUtils.isNotBlank(type) && !StringUtils.equalsIgnoreCase(type, "string")) {
          
          String errorMessage = GrouperTextContainer.textOrNull("provisioning.configuration.validation.groupFieldString");
          errorMessage = errorMessage.replaceAll("$$fieldName$$", name);
          this.addErrorMessageAndJqueryHandle(errorMessage, nameConfigKey);
        } 
        
      }
      
    }
    
    for (String fieldRequired: fieldsRequired) {
      String errorMessage = GrouperTextContainer.textOrNull("provisioning.configuration.validation.groupFieldRequired");
      errorMessage = errorMessage.replaceAll("$$fieldName$$", fieldRequired);
      this.addErrorMessage(errorMessage);
    }
    
    if (countOfFields > 2) {
      String errorMessage = GrouperTextContainer.textOrNull("provisioning.configuration.validation.groupMoreThanTwoFields");
      this.addErrorMessage(errorMessage);
    }
  }
  
  protected void validateRequiredFieldsExistForEntity(Set<String> fieldsRequired) {
    
    int countOfFields = 0;
    
    //Set<String> fieldsRequired = GrouperUtil.toSet("id");
    
    for (int i=0; i< 20; i++) {

      Boolean isField = GrouperUtil.booleanObjectValue(this.getSuffixToConfigValue().get("targetEntityAttribute."+i+".isFieldElseAttribute"));
      if (isField == null || isField == false) {
        continue;
      }
      
      countOfFields++;
      String nameConfigKey = "targetEntityAttribute."+i+".fieldName"; 
      String name = this.getSuffixToConfigValue().get(nameConfigKey);
      String type = this.getSuffixToConfigValue().get("targetEntityAttribute."+i+".valueType");
      
      if (fieldsRequired.contains(name)) {
        
        fieldsRequired.remove(name);
        
        if (StringUtils.isNotBlank(type) && !StringUtils.equalsIgnoreCase(type, "string")) {
          
          String errorMessage = GrouperTextContainer.textOrNull("provisioning.configuration.validation.entityFieldString");
          errorMessage = errorMessage.replaceAll("$$fieldName$$", name);
          this.addErrorMessageAndJqueryHandle(errorMessage, nameConfigKey);
        } 
        
      }
      
    }
    
    for (String fieldRequired: fieldsRequired) {
      String errorMessage = GrouperTextContainer.textOrNull("provisioning.configuration.validation.entityFieldRequired");
      errorMessage = errorMessage.replaceAll("$$fieldName$$", fieldRequired);
      this.addErrorMessage(errorMessage);
    }
    
    if (countOfFields > 1) { 
      String errorMessage = GrouperTextContainer.textOrNull("provisioning.configuration.validation.entityMoreThanOneField");
      this.addErrorMessage(errorMessage);
    }
    
  }
  
  protected void validateAWSAttributesForEntity() {
    
    int countOfAttributes = 0;
    
    Set<String> attributesRequired = GrouperUtil.toSet("userName", "displayName", "familyName", "givenName");
    Set<String> additionalAttributesAllowed = GrouperUtil.toSet("externalId", "formattedName", "middleName", "emailValue", "emailType", "userType", "employeeNumber", "costCenter");
    
    
    for (int i=0; i< 20; i++) {

      Boolean isField = GrouperUtil.booleanObjectValue(this.getSuffixToConfigValue().get("targetEntityAttribute."+i+".isFieldElseAttribute"));
      
      if (isField == null || isField == true) {
        continue;
      }
      
      countOfAttributes++;
      String nameConfigKey = "targetEntityAttribute."+i+".name"; 
      String name = this.getSuffixToConfigValue().get(nameConfigKey);
      String type = this.getSuffixToConfigValue().get("targetEntityAttribute."+i+".valueType");
      
      if (attributesRequired.contains(name)) {
        
        attributesRequired.remove(name);
        
        if (StringUtils.isNotBlank(type) && !StringUtils.equalsIgnoreCase(type, "string")) {
          
          String errorMessage = GrouperTextContainer.textOrNull("provisioning.configuration.validation.entityAttributeString");
          errorMessage = errorMessage.replaceAll("$$attributeName$$", name);
          this.addErrorMessageAndJqueryHandle(errorMessage, nameConfigKey);
        }
        
      }
      
      if (additionalAttributesAllowed.contains(name)) {
        
        additionalAttributesAllowed.remove(name);
        
        if (StringUtils.isNotBlank(type) && !StringUtils.equalsIgnoreCase(type, "string")) {
          
          String errorMessage = GrouperTextContainer.textOrNull("provisioning.configuration.validation.entityAttributeString");
          errorMessage = errorMessage.replaceAll("$$attributeName$$", name);
          this.addErrorMessageAndJqueryHandle(errorMessage, nameConfigKey);
        } 
        
      }
      
    }
    
    for (String attributeRequired: attributesRequired) {
      String errorMessage = GrouperTextContainer.textOrNull("provisioning.configuration.validation.entityAttributeRequired");
      errorMessage = errorMessage.replaceAll("$$attributeName$$", attributeRequired);
      this.addErrorMessage(errorMessage);
    }
    
    if (countOfAttributes > 12) {
      String errorMessage = GrouperTextContainer.textOrNull("provisioning.configuration.validation.entityMoreThanTwelveAttributes");
      this.addErrorMessage(errorMessage);
    }
    
    if (countOfAttributes <= 12 && (12 - countOfAttributes) != additionalAttributesAllowed.size()) {
      String errorMessage = GrouperTextContainer.textOrNull("provisioning.configuration.validation.incorrectAWSEntityAttributeConfigured");
      this.addErrorMessage(errorMessage);
    }
    
  }
  
}
