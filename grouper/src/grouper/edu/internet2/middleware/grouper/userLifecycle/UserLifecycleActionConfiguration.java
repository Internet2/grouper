package edu.internet2.middleware.grouper.userLifecycle;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.app.config.GrouperConfigurationModuleAttribute;
import edu.internet2.middleware.grouper.app.config.GrouperConfigurationModuleBase;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;
import edu.internet2.middleware.grouper.cfg.dbConfig.OptionValueDriver;
import edu.internet2.middleware.grouper.hibernate.GrouperTransaction;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionHandler;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;

public class UserLifecycleActionConfiguration extends GrouperConfigurationModuleBase implements OptionValueDriver {
  
  @Override
  public String getConfigIdElementIdHandle() {
    return "#userLifecycleActionConfigId";
  }
  
  /**
   * some required config to see what the fields are
   */
  public static final Pattern lifecycleActionConfigIds = Pattern.compile("^grouperUserLifecycleAction\\.([^.]+)\\.name$");
  
  @Override
  public List<MultiKey> retrieveKeysAndLabels() {
        
    Set<String> configIds = GrouperConfig.retrieveConfig().propertyConfigIds(lifecycleActionConfigIds);
    List<MultiKey> results = new ArrayList<>();
    for (String theConfigId : GrouperUtil.nonNull(configIds)) {
      results.add(new MultiKey(theConfigId, theConfigId));
    }
    return results;
  }
  
  @Override
  public ConfigFileName getConfigFileName() {
    return ConfigFileName.GROUPER_PROPERTIES;
  }

  @Override
  public String getConfigItemPrefix() {
    if (StringUtils.isBlank(this.getConfigId())) {
      throw new RuntimeException("Must have configId!");
    }
    return "grouperUserLifecycleAction." + this.getConfigId() + ".";
  }

  @Override
  public String getConfigIdRegex() {
    return "^(grouperUserLifecycleAction)\\.([^.]+)\\.(.*)$";
  }

  @Override
  protected String getConfigurationTypePrefix() {
    return "grouperUserLifecycleAction";
  }
  
  @Override
  public String getConfigIdThatIdentifiesThisConfig() {
    return "userLifecycleActionConfigId";
  }
  
  public String getName() {
    
    Map<String, GrouperConfigurationModuleAttribute> attributes = this.retrieveAttributes();
    
    GrouperConfigurationModuleAttribute nameAttribute = attributes.get("name");
    if (nameAttribute != null) {      
      String name = nameAttribute.getValueOrExpressionEvaluationValue();
      return name;
    }
    return null;
  }
  
  public String getActionType() {
    
    Map<String, GrouperConfigurationModuleAttribute> attributes = this.retrieveAttributes();
    
    GrouperConfigurationModuleAttribute attribute = attributes.get("actionType");
    if (attribute != null) {      
      String actionType = attribute.getValueOrExpressionEvaluationValue();
      return actionType;
    }
    return null;
  }
  
  /**
   * list of configured user lifecycle action configs
   * @return
   */
  public static List<UserLifecycleActionConfiguration> retrieveAllUserLifecycleActionConfigurations() {
   Set<String> classNames = new HashSet<String>();
   classNames.add(UserLifecycleActionConfiguration.class.getName());
   return (List<UserLifecycleActionConfiguration>) (Object) retrieveAllConfigurations(classNames);
  }

  @Override
  public void validatePreSave(boolean isInsert, List<String> errorsToDisplay, Map<String, String> validationErrorsToDisplay) {
    
    super.validatePreSave(isInsert, errorsToDisplay, validationErrorsToDisplay);
    if (errorsToDisplay.size() > 0 || validationErrorsToDisplay.size() > 0) {
      return;
    }
    
  }
  
  
  @Override
  public void insertConfig(boolean fromUi, StringBuilder message,
      List<String> errorsToDisplay, Map<String, String> validationErrorsToDisplay,
      List<String> actionsPerformed) {
    
    GrouperTransaction.callbackGrouperTransaction(new GrouperTransactionHandler() {
      
      @Override
      public Object callback(GrouperTransaction grouperTransaction)
          throws GrouperDAOException {
        
        UserLifecycleActionConfiguration.super.insertConfig(fromUi, message, errorsToDisplay, validationErrorsToDisplay,
            actionsPerformed);
        
        if (errorsToDisplay.size() == 0 && validationErrorsToDisplay.size() == 0) {
          GrouperConfig grouperConfig = GrouperConfig.retrieveConfig();
//          UserLifecycleEngine.syncUserLifecycleEventConfigs(grouperConfig);
        }
        
        return null;
      }
    });
    
  }

  @Override
  public void deleteConfig(boolean fromUi) {
    
    GrouperTransaction.callbackGrouperTransaction(new GrouperTransactionHandler() {
      
      @Override
      public Object callback(GrouperTransaction grouperTransaction)
          throws GrouperDAOException {
        
        UserLifecycleActionConfiguration.super.deleteConfig(fromUi);
        
        GrouperConfig grouperConfig = GrouperConfig.retrieveConfig();
        
//        UserLifecycleEngine.syncUserLifecycleEventConfigs(grouperConfig);
        
        return null;
      }
    });
  
  }

  @Override
  public void editConfig(boolean fromUi, StringBuilder message, List<String> errorsToDisplay, 
      Map<String, String> validationErrorsToDisplay, List<String> actionsPerformed) {
    
    
    GrouperTransaction.callbackGrouperTransaction(new GrouperTransactionHandler() {
      
      @Override
      public Object callback(GrouperTransaction grouperTransaction)
          throws GrouperDAOException {
        
        
        UserLifecycleActionConfiguration.super.editConfig(fromUi, message, errorsToDisplay, validationErrorsToDisplay,
            actionsPerformed);
        
        if (errorsToDisplay.size() == 0 && validationErrorsToDisplay.size() == 0) {
          GrouperConfig grouperConfig = GrouperConfig.retrieveConfig();
//          UserLifecycleEngine.syncUserLifecycleEventConfigs(grouperConfig);
        }
        
        return null;
      }
    });
    
  }

}
