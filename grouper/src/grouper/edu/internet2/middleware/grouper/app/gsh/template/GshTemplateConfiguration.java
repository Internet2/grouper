package edu.internet2.middleware.grouper.app.gsh.template;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.app.config.GrouperConfigurationModuleAttribute;
import edu.internet2.middleware.grouper.app.config.GrouperConfigurationModuleBase;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;
import edu.internet2.middleware.grouper.cfg.dbConfig.DbConfigEngine;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.config.ConfigPropertiesCascadeBase;

public class GshTemplateConfiguration extends GrouperConfigurationModuleBase {

  @Override
  public ConfigFileName getConfigFileName() {
    return ConfigFileName.GROUPER_PROPERTIES;
  }

  @Override
  public String getConfigItemPrefix() {
    if (StringUtils.isBlank(this.getConfigId())) {
      throw new RuntimeException("Must have configId!");
    }
    return "grouperGshTemplate." + this.getConfigId() + ".";
  }

  @Override
  public String getConfigIdRegex() {
    return "^(grouperGshTemplate)\\.([^.]+)\\.(.*)$";
  }
  
  @Override
  protected String getConfigurationTypePrefix() {
    return "grouperGshTemplate";
  }
  
  @Override
  public String getConfigIdThatIdentifiesThisConfig() {
    return "testGshTemplate";
  }
  
  /**
   * list of configured gsh template configs
   * @return
   */
  public static List<GshTemplateConfiguration> retrieveAllGshTemplateConfigs() {
   Set<String> classNames = new HashSet<String>();
   classNames.add(GshTemplateConfiguration.class.getName());
   return (List<GshTemplateConfiguration>) (Object) retrieveAllConfigurations(classNames);
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
    
    DbConfigEngine.configurationFileAddEditHelper2(this.getConfigFileName().getConfigFileName(), 
        enabledAttribute.getFullPropertyName(), 
        enabledAttribute.isExpressionLanguage() ? "true" : "false", 
        enabledAttribute.isExpressionLanguage() ? enabledAttribute.getExpressionLanguageScript() : enabledAttribute.getValue(),
        enabledAttribute.isPassword(), message, new Boolean[] {false},
        new Boolean[] {false}, true, "GSH template status changed", errorsToDisplay, validationErrorsToDisplay, false);    
    ConfigPropertiesCascadeBase.clearCache();
  }

  @Override
  public void insertConfig(boolean fromUi, StringBuilder message,
      List<String> errorsToDisplay, Map<String, String> validationErrorsToDisplay) {
    
    Map<String, GrouperConfigurationModuleAttribute> attributes = this.retrieveAttributes();
    GrouperConfigurationModuleAttribute gshTemplateAttribute = attributes.getOrDefault("gshTemplate", null);
    
    normalizeNewLines(gshTemplateAttribute);
    
    super.insertConfig(fromUi, message, errorsToDisplay, validationErrorsToDisplay);
  }
  
  @Override
  public void editConfig(boolean fromUi, StringBuilder message,
      List<String> errorsToDisplay, Map<String, String> validationErrorsToDisplay) {
    
    Map<String, GrouperConfigurationModuleAttribute> attributes = this.retrieveAttributes();
    GrouperConfigurationModuleAttribute gshTemplateAttribute = attributes.getOrDefault("gshTemplate", null);
    
    normalizeNewLines(gshTemplateAttribute);
    
    super.editConfig(fromUi, message, errorsToDisplay, validationErrorsToDisplay);
  }
  
  private void normalizeNewLines(GrouperConfigurationModuleAttribute gshTemplateAttribute) {
    
    if (gshTemplateAttribute != null) {
      String value = gshTemplateAttribute.getValue();
      if (StringUtils.isNotBlank(value)) {
        
        value = value.replaceAll("\r\n", "\n");
        value = value.replaceAll("\r", "\n");
        gshTemplateAttribute.setValue(value);
      }
    }
    
  }

}
