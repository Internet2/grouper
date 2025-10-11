package edu.internet2.middleware.grouper.userLifecycle;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.app.config.GrouperConfigurationModuleBase;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;

public class UserLifecyclePolicyPartConfiguration extends GrouperConfigurationModuleBase {
  
  /**
   * some required config to see what the fields are
   */
  public static final Pattern lifecyclePolicyPartConfigIds = Pattern.compile("^grouperUserLifecyclePolicyPart\\.([^.]+)\\.policy$");
  
  @Override
  public ConfigFileName getConfigFileName() {
    return ConfigFileName.GROUPER_PROPERTIES;
  }

  @Override
  public String getConfigItemPrefix() {
    if (StringUtils.isBlank(this.getConfigId())) {
      throw new RuntimeException("Must have configId!");
    }
    return "grouperUserLifecyclePolicyPart." + this.getConfigId() + ".";
  }

  @Override
  public String getConfigIdRegex() {
    return "^(grouperUserLifecyclePolicyPart)\\.([^.]+)\\.(.*)$";
  }

  @Override
  protected String getConfigurationTypePrefix() {
    return "grouperUserLifecyclePolicyPart";
  }
  
  @Override
  public String getConfigIdThatIdentifiesThisConfig() {
    return "userLifecyclePolicyPartConfigId";
  }
  
  /**
   * list of configured user lifecycle policy part configs
   * @return
   */
  public static List<UserLifecyclePolicyPartConfiguration> retrieveAllUserLifecyclePolicyPartConfigurations() {
   Set<String> classNames = new HashSet<String>();
   classNames.add(UserLifecyclePolicyPartConfiguration.class.getName());
   return (List<UserLifecyclePolicyPartConfiguration>) (Object) retrieveAllConfigurations(classNames);
  }

  @Override
  public void validatePreSave(boolean isInsert, List<String> errorsToDisplay, Map<String, String> validationErrorsToDisplay) {
    
    super.validatePreSave(isInsert, errorsToDisplay, validationErrorsToDisplay);
    if (errorsToDisplay.size() > 0 || validationErrorsToDisplay.size() > 0) {
      return;
    }
    
  }
  
}
