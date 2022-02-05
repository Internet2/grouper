package edu.internet2.middleware.grouper.app.duo.role;

import java.util.Set;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfigurationValidation;
import edu.internet2.middleware.grouper.cfg.text.GrouperTextContainer;
import edu.internet2.middleware.grouper.util.GrouperUtil;

public class DuoRoleProvisoningConfigurationValidation extends GrouperProvisioningConfigurationValidation {
  
  @Override
  public void validateFromSuffixValueMap() {
    
    super.validateFromSuffixValueMap();
    
    validateRequiredFieldsExistForGroup();
    validateAttributeForGroup();
    validateFieldsExistForEntity();
    validateAttributesForEntity();

  }
  
  protected void validateFieldsExistForEntity() {
    
    int countOfFields = 0;
    
    Set<String> fieldsRequired = GrouperUtil.toSet("id", "loginId");
    Set<String> additionalFieldsAllowed = GrouperUtil.toSet("name", "email");
    
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
      
      if (additionalFieldsAllowed.contains(name)) {
        
        additionalFieldsAllowed.remove(name);
        
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
    
    if (countOfFields > 4) {
      String errorMessage = GrouperTextContainer.textOrNull("provisioning.configuration.validation.entityMoreThanFourFields");
      this.addErrorMessage(errorMessage);
    }
    
    if (countOfFields <= 4 && (4 - countOfFields) != additionalFieldsAllowed.size()) {
      String errorMessage = GrouperTextContainer.textOrNull("provisioning.configuration.validation.incorrectEntityFieldsConfigured");
      this.addErrorMessage(errorMessage);
    }
  }
  
  protected void validateRequiredFieldsExistForGroup() {
    
    int countOfFields = 0;
    
    Set<String> fieldsRequired = GrouperUtil.toSet("name", "id");
    
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
  
  protected void validateAttributeForGroup() {
    
    int countOfAttributes = 0;
    
    Set<String> attributesAllowed = GrouperUtil.toSet("description");
    
    for (int i=0; i< 20; i++) {

      Boolean isField = GrouperUtil.booleanObjectValue(this.getSuffixToConfigValue().get("targetGroupAttribute."+i+".isFieldElseAttribute"));
      
      if (isField == null || isField == true) {
        continue;
      }
      
      countOfAttributes++;
      String nameConfigKey = "targetGroupAttribute."+i+".name"; 
      String name = this.getSuffixToConfigValue().get(nameConfigKey);
      String type = this.getSuffixToConfigValue().get("targetGroupAttribute."+i+".valueType");
      
      if (attributesAllowed.contains(name)) {
        
        attributesAllowed.remove(name);
        
        if (StringUtils.isNotBlank(type) && !StringUtils.equalsIgnoreCase(type, "string")) {
          
          String errorMessage = GrouperTextContainer.textOrNull("provisioning.configuration.validation.groupAttributeString");
          errorMessage = errorMessage.replaceAll("$$attributeName$$", name);
          this.addErrorMessageAndJqueryHandle(errorMessage, nameConfigKey);
        } 
        
      }
      
    }
    
    if (countOfAttributes > 1 || (countOfAttributes > 0 && !attributesAllowed.isEmpty())) {
      String errorMessage = GrouperTextContainer.textOrNull("provisioning.configuration.validation.incorrectGroupAttributeConfigured");
      this.addErrorMessage(errorMessage);
    }
  }

  protected void validateAttributesForEntity() {
    
    int countOfAttributes = 0;
    
    Set<String> attributesAllowed = GrouperUtil.toSet("firstname", "lastname");
    
    for (int i=0; i< 20; i++) {

      Boolean isField = GrouperUtil.booleanObjectValue(this.getSuffixToConfigValue().get("targetEnttiyAttribute."+i+".isFieldElseAttribute"));
      
      if (isField == null || isField == true) {
        continue;
      }
      
      countOfAttributes++;
      String nameConfigKey = "targetEntityAttribute."+i+".name"; 
      String name = this.getSuffixToConfigValue().get(nameConfigKey);
      String type = this.getSuffixToConfigValue().get("targetEntityAttribute."+i+".valueType");
      
      if (attributesAllowed.contains(name)) {
        
        attributesAllowed.remove(name);
        
        if (StringUtils.isNotBlank(type) && !StringUtils.equalsIgnoreCase(type, "string")) {
          
          String errorMessage = GrouperTextContainer.textOrNull("provisioning.configuration.validation.entityAttributeString");
          errorMessage = errorMessage.replaceAll("$$attributeName$$", name);
          this.addErrorMessageAndJqueryHandle(errorMessage, nameConfigKey);
        }
        
      }
      
    }
    
    if (countOfAttributes > 2 || (countOfAttributes <= 2 && (2 - countOfAttributes) != attributesAllowed.size())) {
      String errorMessage = GrouperTextContainer.textOrNull("provisioning.configuration.validation.incorrectDuoEntityAttributeConfigured");
      this.addErrorMessage(errorMessage);
    }

  }
}
