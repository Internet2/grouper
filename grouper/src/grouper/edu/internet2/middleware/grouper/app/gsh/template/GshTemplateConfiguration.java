package edu.internet2.middleware.grouper.app.gsh.template;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.app.config.GrouperConfigurationModuleAttribute;
import edu.internet2.middleware.grouper.app.config.GrouperConfigurationModuleBase;
import edu.internet2.middleware.grouper.audit.AuditEntry;
import edu.internet2.middleware.grouper.audit.AuditTypeBuiltin;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;
import edu.internet2.middleware.grouper.cfg.dbConfig.DbConfigEngine;
import edu.internet2.middleware.grouper.cfg.text.GrouperTextContainer;
import edu.internet2.middleware.grouper.hibernate.AuditControl;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.hibernate.HibernateHandler;
import edu.internet2.middleware.grouper.hibernate.HibernateHandlerBean;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
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
  public void validatePreSave(boolean isInsert, List<String> errorsToDisplay,
      Map<String, String> validationErrorsToDisplay) {
    
    super.validatePreSave(isInsert, errorsToDisplay, validationErrorsToDisplay);
    
    Map<String, GrouperConfigurationModuleAttribute> attributes = this.retrieveAttributes();
    GrouperConfigurationModuleAttribute numberOfInputsAttribute = attributes.get("numberOfInputs");
    
    String valueOrExpressionEvaluation = numberOfInputsAttribute.getValueOrExpressionEvaluation();
    
    int numberOfInputs = GrouperUtil.intValue(valueOrExpressionEvaluation, 0);
    
    for (int i=0; i<numberOfInputs; i++) {
      GrouperConfigurationModuleAttribute nameAttribute = attributes.get("input."+i+".name");
      String nameAttributeValue = nameAttribute.getValueOrExpressionEvaluation();
      if (!nameAttributeValue.startsWith("gsh_input_") || !nameAttributeValue.matches("^[a-zA-Z0-9_]+$")) {
        String error = GrouperTextContainer.textOrNull("gshTemplateSaveErrorInputNotValidFormat");
        validationErrorsToDisplay.put(nameAttribute.getHtmlForElementIdHandle(), error);
      }
    }
    
  }
  
  @Override
  public void insertConfig(boolean fromUi, StringBuilder message,
      List<String> errorsToDisplay, Map<String, String> validationErrorsToDisplay) {
    
    final String configId = this.getConfigId();
    HibernateSession.callbackHibernateSession(GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_NOT_AUDIT,
       new HibernateHandler() {

      public Object callback(HibernateHandlerBean hibernateHandlerBean)
          throws GrouperDAOException {
        
        GshTemplateConfiguration.super.insertConfig(fromUi, message, errorsToDisplay, validationErrorsToDisplay);
        if (errorsToDisplay.size() == 0 && validationErrorsToDisplay.size() == 0) { 
          AuditEntry auditEntry = new AuditEntry(AuditTypeBuiltin.GSH_TEMPLATE_ADD,
              "gshTemplateConfigId", configId);
          auditEntry.setDescription("Add gsh template with configId: " + configId); 
          auditEntry.saveOrUpdate(true);
          
        }
        return null;
       
      }
      
    });
  }
  
  @Override
  public void editConfig(boolean fromUi, StringBuilder message,
      List<String> errorsToDisplay, Map<String, String> validationErrorsToDisplay) {
    
      final String configId = this.getConfigId();
      HibernateSession.callbackHibernateSession(GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_NOT_AUDIT,
         new HibernateHandler() {
  
        public Object callback(HibernateHandlerBean hibernateHandlerBean)
            throws GrouperDAOException {
          
          GshTemplateConfiguration.super.editConfig(fromUi, message, errorsToDisplay, validationErrorsToDisplay);
          if (errorsToDisplay.size() == 0 && validationErrorsToDisplay.size() == 0) { 
            AuditEntry auditEntry = new AuditEntry(AuditTypeBuiltin.GSH_TEMPLATE_UPDATE,
                "gshTemplateConfigId", configId);
            auditEntry.setDescription("Update gsh template with configId: " + configId); 
            auditEntry.saveOrUpdate(true);
            
          }
          return null;
          
        }
        
      });
    
  }
  
  @Override
  public void deleteConfig(boolean fromUi) {
    
    final String configId = this.getConfigId();
    HibernateSession.callbackHibernateSession(GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_NOT_AUDIT,
       new HibernateHandler() {

      public Object callback(HibernateHandlerBean hibernateHandlerBean)
          throws GrouperDAOException {
        GshTemplateConfiguration.super.deleteConfig(fromUi);
        AuditEntry auditEntry = new AuditEntry(AuditTypeBuiltin.GSH_TEMPLATE_DELETE,
            "gshTemplateConfigId", configId);
        auditEntry.setDescription("Delete gsh template with configId: " + configId); 
        auditEntry.saveOrUpdate(true);
        return null;
        
      }
      
    });
    
  }

}
