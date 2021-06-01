package edu.internet2.middleware.grouper.app.customUi;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.app.config.GrouperConfigurationModuleAttribute;
import edu.internet2.middleware.grouper.app.config.GrouperConfigurationModuleBase;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefFinder;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileMetadata;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;
import edu.internet2.middleware.grouper.cfg.dbConfig.DbConfigEngine;
import edu.internet2.middleware.grouper.cfg.text.GrouperTextContainer;
import edu.internet2.middleware.grouper.ui.customUi.CustomUiEngine;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.config.ConfigPropertiesCascadeBase;

public class CustomUiConfiguration extends GrouperConfigurationModuleBase {

  @Override
  public ConfigFileName getConfigFileName() {
    return ConfigFileName.GROUPER_PROPERTIES;
  }

  @Override
  public String getConfigItemPrefix() {
    if (StringUtils.isBlank(this.getConfigId())) {
      throw new RuntimeException("Must have configId!");
    }
    return "grouperCustomUI." + this.getConfigId() + ".";
  }

  @Override
  public String getConfigIdRegex() {
    return "^(grouperCustomUI)\\.([^.]+)\\.(.*)$";
  }

  @Override
  protected String getConfigurationTypePrefix() {
    return "grouperCustomUI";
  }
  
  @Override
  public String getConfigIdThatIdentifiesThisConfig() {
    return "testCustomUI";
  }
  
  /**
   * list of configured custom ui configs
   * @return
   */
  public static List<CustomUiConfiguration> retrieveAllCustomUiConfigs() {
   Set<String> classNames = new HashSet<String>();
   classNames.add(CustomUiConfiguration.class.getName());
   return (List<CustomUiConfiguration>) (Object) retrieveAllConfigurations(classNames);
  }
  
  /**
   * is the config enabled or not
   * @return
   */
  @Override
  public boolean isEnabled() {
   try {
     GrouperConfigurationModuleAttribute enabledAttribute = this.retrieveAttributes().get("enabled");
     String enabledString = enabledAttribute.getValue();
     if (StringUtils.isBlank(enabledString)) {
       enabledString = enabledAttribute.getDefaultValue();
     }
     return GrouperUtil.booleanValue(enabledString, true);
   } catch (Exception e) {
     return false;
   }
    
  }
  
  public String getGroupId() {
    try {
      GrouperConfigurationModuleAttribute groupUUIDOrNameAttribute = this.retrieveAttributes().get("groupUUIDOrName");
      String groupUuidOrNameString = groupUUIDOrNameAttribute.getValue();
      if (StringUtils.isBlank(groupUuidOrNameString)) {
        throw new RuntimeException("groupUUIDOrName cannot be blank!!");
      }
      
      Group group = GroupFinder.findByUuid(groupUuidOrNameString, false);
      if (group == null) {
        group = GroupFinder.findByName(groupUuidOrNameString, true);
      }
      return group.getId();
    } catch (Exception e) {
      throw new RuntimeException("could not find group for custom ui configId "+this.getConfigId());
    }
    
  }
  
  /**
   * change status of config to disable/enable
   * @param enable
   * @param message
   * @param errorsToDisplay
   * @param validationErrorsToDisplay
   */
  public void changeStatus(boolean enable, StringBuilder message, List<String> errorsToDisplay, Map<String, String> validationErrorsToDisplay) {
    
    GrouperConfigurationModuleAttribute enabledAttribute = this.retrieveAttributes().get("enabled");
    enabledAttribute.setValue(enable? "true": "false");
    
    ConfigFileName configFileName = this.getConfigFileName();
    ConfigFileMetadata configFileMetadata = configFileName.configFileMetadata();

    DbConfigEngine.configurationFileAddEditHelper2(configFileName, this.getConfigFileName().getConfigFileName(), configFileMetadata,
        enabledAttribute.getFullPropertyName(), 
        enabledAttribute.isExpressionLanguage() ? "true" : "false", 
        enabledAttribute.isExpressionLanguage() ? enabledAttribute.getExpressionLanguageScript() : enabledAttribute.getValue(),
        enabledAttribute.isPassword(), message, new Boolean[] {false},
        new Boolean[] {false}, true, "Custom UI status changed", errorsToDisplay, validationErrorsToDisplay, false);    
    ConfigPropertiesCascadeBase.clearCache();
  }
  
  private void validateAttributeDefIds(Map<String, String> validationErrorsToDisplay) {
    
    Map<String, GrouperConfigurationModuleAttribute> attributes = this.retrieveAttributes();
    
    GrouperConfigurationModuleAttribute numberOfQueriesAttribute = attributes.get("numberOfQueries");
    
    String valueOrExpressionEvaluation = numberOfQueriesAttribute.getValueOrExpressionEvaluation();
    
    int numberOfQueries = GrouperUtil.intValue(valueOrExpressionEvaluation, 0);
    
    for (int i=0; i<numberOfQueries; i++) {
      
      GrouperConfigurationModuleAttribute attributeDefIdAttribute = attributes.get("cuQuery."+i+".attributeDefId");
      
      String attributeDefIdAttributeValue = attributeDefIdAttribute != null ? attributeDefIdAttribute.getValueOrExpressionEvaluation(): null;
      
      if (StringUtils.isNotBlank(attributeDefIdAttributeValue)) {
        AttributeDef attributeDef = AttributeDefFinder.findById(attributeDefIdAttributeValue, false);
        if (attributeDef == null) {
          String error = GrouperTextContainer.textOrNull("customUiConfigSaveErrorAttributeDefNotFound");
          error = GrouperUtil.replace(error, "$$attributeDefId$$", attributeDefIdAttributeValue);
          validationErrorsToDisplay.put(attributeDefIdAttribute.getHtmlForElementIdHandle(), error);
        }
      }
      
    }
    
  } 
  
  private void validateDuplicateTextConfigIndex(Map<String, String> validationErrorsToDisplay) {
    
    Map<String, GrouperConfigurationModuleAttribute> attributes = this.retrieveAttributes();
    
    GrouperConfigurationModuleAttribute numberOfTextConfigsAttribute = attributes.get("numberOfTextConfigs");
    
    String valueOrExpressionEvaluation = numberOfTextConfigsAttribute.getValueOrExpressionEvaluation();
    
    int numberOfTextConfigs = GrouperUtil.intValue(valueOrExpressionEvaluation, 0);
    
    // two text configs that have the same custom ui text types should not have the same index
    Map<String, Set<Integer>> textTypeIndexes = new HashMap<String, Set<Integer>>();
    for (int i=0; i<numberOfTextConfigs; i++) {
      
      GrouperConfigurationModuleAttribute textTypeAttribute = attributes.get("cuTextConfig."+i+".textType");
      
      String textTypeAttributeValue = textTypeAttribute.getValueOrExpressionEvaluation();
      
      GrouperConfigurationModuleAttribute indexAttribute = attributes.get("cuTextConfig."+i+".index");
      
      String indexAttributeValue = indexAttribute.getValueOrExpressionEvaluation();
      int indexAttributeValueInt = GrouperUtil.intValue(indexAttributeValue, 0);
      
      if (textTypeIndexes.containsKey(textTypeAttributeValue)) {
        Set<Integer> existingIndexes = textTypeIndexes.get(textTypeAttributeValue);
        if (existingIndexes.contains(indexAttributeValueInt)) {
          String error = GrouperTextContainer.textOrNull("customUiConfigSaveErrorDuplicateIndex");
          validationErrorsToDisplay.put(indexAttribute.getHtmlForElementIdHandle(), error);
          return;
        } else {
          existingIndexes.add(indexAttributeValueInt);
        }
      } else {
        Set<Integer> indexes = new HashSet<Integer>();
        indexes.add(indexAttributeValueInt);
        textTypeIndexes.put(textTypeAttributeValue, indexes);
      }
    }
  }
  
  @Override
  public void validatePreSave(boolean isInsert, List<String> errorsToDisplay,
      Map<String, String> validationErrorsToDisplay) {
    
    super.validatePreSave(isInsert, errorsToDisplay, validationErrorsToDisplay);
    
    Map<String, GrouperConfigurationModuleAttribute> attributes = this.retrieveAttributes();
    
    GrouperConfigurationModuleAttribute groupUUIDOrNameAttribute = attributes.get("groupUUIDOrName");
    String groupUuidOrName = groupUUIDOrNameAttribute.getValueOrExpressionEvaluation();
    
    Group group = GroupFinder.findByUuid(groupUuidOrName, false);
    if (group == null) {
      group = GroupFinder.findByName(groupUuidOrName, false);
    }
    
    if (group == null) {
      String error = GrouperTextContainer.textOrNull("customUiConfigSaveErrorGroupNotFound");
      error = GrouperUtil.replace(error, "$$groupUUIDOrName$$", groupUuidOrName);
      validationErrorsToDisplay.put(groupUUIDOrNameAttribute.getHtmlForElementIdHandle(), error);
    }
    
    if (isInsert) {
      //Two custom UIs cannot have the same group id/group name
      String configId = CustomUiEngine.retrieveCustomUiConfigurationConfigId(group, false);
      if (StringUtils.isNotBlank(configId)) {
        String error = GrouperTextContainer.textOrNull("customUiConfigSaveErrorGroupAlreadyHasCustomUi");
        error = GrouperUtil.replace(error, "$$groupUUIDOrName$$", groupUuidOrName);
        validationErrorsToDisplay.put(groupUUIDOrNameAttribute.getHtmlForElementIdHandle(), error);
        return;
      }
       
    }
    
    // validateAttributeDefIds(validationErrorsToDisplay);
    
    if (validationErrorsToDisplay.size() == 0) {
      validateDuplicateTextConfigIndex(validationErrorsToDisplay);
    }
    
    GrouperConfigurationModuleAttribute numberOfQueriesAttribute = attributes.get("numberOfQueries");
    
    String valueOrExpressionEvaluation = numberOfQueriesAttribute.getValueOrExpressionEvaluation();
    
    int numberOfQueries = GrouperUtil.intValue(valueOrExpressionEvaluation, 0);
    
    for (int i=0; i<numberOfQueries; i++) {
      GrouperConfigurationModuleAttribute variableToAssignAttribute = attributes.get("cuQuery."+i+".variableToAssign");
      String variableToAssignAttributeValue = variableToAssignAttribute.getValueOrExpressionEvaluation();
      if (StringUtils.isNotBlank(variableToAssignAttributeValue) && !variableToAssignAttributeValue.startsWith("cu_")) {
        String error = GrouperTextContainer.textOrNull("customUiConfigSaveErrorVariableToAssignNotValid"); 
        validationErrorsToDisplay.put(variableToAssignAttribute.getHtmlForElementIdHandle(), error);
        return;
      }
      
      GrouperConfigurationModuleAttribute variableToAssignOnErrorAttribute = attributes.get("cuQuery."+i+".variableToAssignOnError");
      String variableToAssignOnErrorAttributeValue = variableToAssignOnErrorAttribute.getValueOrExpressionEvaluation();
      if (StringUtils.isNotBlank(variableToAssignOnErrorAttributeValue) && !variableToAssignOnErrorAttributeValue.startsWith("cu_")) {
        String error = GrouperTextContainer.textOrNull("customUiConfigSaveErrorVariableToAssignOnErrorNotValid"); 
        validationErrorsToDisplay.put(variableToAssignOnErrorAttribute.getHtmlForElementIdHandle(), error);
        return;
      }
      
    }
  }

}
