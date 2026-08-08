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

public class UserLifecyclePolicyConfiguration extends GrouperConfigurationModuleBase implements OptionValueDriver {
  
  @Override
  public String getConfigIdElementIdHandle() {
    return "#userLifecyclePolicyConfigId";
  }
  
  /**
   * some required config to see what the fields are
   */
  public static final Pattern lifecyclePolicyConfigIds = Pattern.compile("^grouperUserLifecyclePolicy\\.([^.]+)\\.name$");
  
  @Override
  public ConfigFileName getConfigFileName() {
    return ConfigFileName.GROUPER_PROPERTIES;
  }

  @Override
  public String getConfigItemPrefix() {
    if (StringUtils.isBlank(this.getConfigId())) {
      throw new RuntimeException("Must have configId!");
    }
    return "grouperUserLifecyclePolicy." + this.getConfigId() + ".";
  }

  @Override
  public String getConfigIdRegex() {
    return "^(grouperUserLifecyclePolicy)\\.([^.]+)\\.(.*)$";
  }

  @Override
  protected String getConfigurationTypePrefix() {
    return "grouperUserLifecyclePolicy";
  }
  
  @Override
  public String getConfigIdThatIdentifiesThisConfig() {
    return "userLifecyclePolicyConfigId";
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

  
  /**
   * list of configured user lifecycle action configs
   * @return
   */
  public static List<UserLifecyclePolicyConfiguration> retrieveAllUserLifecyclePolicyConfigurations() {
   Set<String> classNames = new HashSet<String>();
   classNames.add(UserLifecyclePolicyConfiguration.class.getName());
   return (List<UserLifecyclePolicyConfiguration>) (Object) retrieveAllConfigurations(classNames);
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
        
        UserLifecyclePolicyConfiguration.super.insertConfig(fromUi, message, errorsToDisplay, validationErrorsToDisplay,
            actionsPerformed);
        
        if (errorsToDisplay.size() == 0 && validationErrorsToDisplay.size() == 0) {
          GrouperConfig grouperConfig = GrouperConfig.retrieveConfig();

//          GrouperDataEngine.syncDataFields(grouperConfig);
//          GrouperDataEngine.syncDataAliases(grouperConfig);
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
        
        UserLifecyclePolicyConfiguration.super.deleteConfig(fromUi);
        
        GrouperConfig grouperConfig = GrouperConfig.retrieveConfig();
        
//        GrouperDataEngine.syncDataAliases(grouperConfig);
//        GrouperDataEngine.syncDataFields(grouperConfig);
        
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
        
        
        UserLifecyclePolicyConfiguration.super.editConfig(fromUi, message, errorsToDisplay, validationErrorsToDisplay,
            actionsPerformed);
        
        if (errorsToDisplay.size() == 0 && validationErrorsToDisplay.size() == 0) {
          GrouperConfig grouperConfig = GrouperConfig.retrieveConfig();

//          GrouperDataEngine.syncDataFields(grouperConfig);
//          GrouperDataEngine.syncDataAliases(grouperConfig);
        }
        
        return null;
      }
    });
    
  }
  
  
  /**
   * some required config to see what the fields are
   */
  private static Pattern policyConfigIds = Pattern.compile("^grouperUserLifecyclePolicy\\.([^.]+)\\.name$");
  
  @Override
  public List<MultiKey> retrieveKeysAndLabels() {
        
    Set<String> configIds = GrouperConfig.retrieveConfig().propertyConfigIds(policyConfigIds);
    List<MultiKey> results = new ArrayList<>();
    for (String theConfigId : GrouperUtil.nonNull(configIds)) {
      results.add(new MultiKey(theConfigId, theConfigId));
    }
    return results;
  }

}
