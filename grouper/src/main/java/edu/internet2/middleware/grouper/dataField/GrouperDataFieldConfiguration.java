package edu.internet2.middleware.grouper.dataField;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.app.config.GrouperConfigurationModuleBase;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;
import edu.internet2.middleware.grouper.hibernate.GrouperTransaction;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionHandler;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;

public class GrouperDataFieldConfiguration extends GrouperConfigurationModuleBase {

  @Override
  public ConfigFileName getConfigFileName() {
    return ConfigFileName.GROUPER_PROPERTIES;
  }

  @Override
  public String getConfigItemPrefix() {
    if (StringUtils.isBlank(this.getConfigId())) {
      throw new RuntimeException("Must have configId!");
    }
    return "grouperDataField." + this.getConfigId() + ".";
  }

  @Override
  public String getConfigIdRegex() {
    return "^(grouperDataField)\\.([^.]+)\\.(.*)$";
  }

  @Override
  protected String getConfigurationTypePrefix() {
    return "grouperDataField";
  }
  
  @Override
  public String getConfigIdThatIdentifiesThisConfig() {
    return "dataFieldConfigId";
  }
  
  /**
   * list of configured data field configs
   * @return
   */
  public static List<GrouperDataFieldConfiguration> retrieveAllDataFieldConfigurations() {
   Set<String> classNames = new HashSet<String>();
   classNames.add(GrouperDataFieldConfiguration.class.getName());
   return (List<GrouperDataFieldConfiguration>) (Object) retrieveAllConfigurations(classNames);
  }

  @Override
  public void insertConfig(boolean fromUi, StringBuilder message,
      List<String> errorsToDisplay, Map<String, String> validationErrorsToDisplay,
      List<String> actionsPerformed) {
   
    GrouperTransaction.callbackGrouperTransaction(new GrouperTransactionHandler() {
      
      @Override
      public Object callback(GrouperTransaction grouperTransaction)
          throws GrouperDAOException {
        
        GrouperDataFieldConfiguration.super.insertConfig(fromUi, message, errorsToDisplay, validationErrorsToDisplay,
            actionsPerformed);
        
        GrouperConfig grouperConfig = GrouperConfig.retrieveConfig();

        GrouperDataEngine.syncDataFields(grouperConfig);
        GrouperDataEngine.syncDataAliases(grouperConfig);
        
        return null;
      }
    });
    
    
  }

  @Override
  public void deleteConfig(boolean fromUi) {
    super.deleteConfig(fromUi);
    
    GrouperConfig grouperConfig = GrouperConfig.retrieveConfig();

    GrouperDataEngine.syncDataAliases(grouperConfig);
    GrouperDataEngine.syncDataFields(grouperConfig);
  }
  
}
