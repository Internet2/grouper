package edu.internet2.middleware.grouper.app.daemon;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.app.config.GrouperConfigurationModuleAttribute;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderType;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningConsumer;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;
import edu.internet2.middleware.grouper.cfg.text.GrouperTextContainer;

public class GrouperDaemonProvisioningIncrementalSyncConfiguration extends GrouperDaemonConfiguration {

  @Override
  public ConfigFileName getConfigFileName() {
    return ConfigFileName.GROUPER_LOADER_PROPERTIES;
  }
      
  @Override
  public String getConfigIdRegex() {
    return "^(changeLog\\.consumer)\\.([^.]+)\\.(.+)$";
  }

  @Override
  public String getConfigItemPrefix() {
    if (StringUtils.isBlank(this.getConfigId())) {
      throw new RuntimeException("Must have configId.");
    }
    return "changeLog.consumer." + this.getConfigId() + ".";
  }
    
  @Override
  public String getPropertySuffixThatIdentifiesThisConfig() {
    return "publisher.class";
  }

  @Override
  public String getPropertyValueThatIdentifiesThisConfig() {
    return ProvisioningConsumer.class.getName();
  }

  @Override
  public String getDaemonJobPrefix() {
    return GrouperLoaderType.GROUPER_CHANGE_LOG_CONSUMER_PREFIX;
  }

  @Override
  public boolean isMultiple() {
    return true;
  }

  @Override
  public boolean matchesQuartzJobName(String jobName) {
    if (jobName != null && jobName.startsWith(GrouperLoaderType.GROUPER_CHANGE_LOG_CONSUMER_PREFIX)) {
      if (StringUtils.equals(this.getPropertyValueThatIdentifiesThisConfig(),
          GrouperLoaderConfig.retrieveConfig().propertyValueString(this.getConfigItemPrefix() 
              + this.getPropertySuffixThatIdentifiesThisConfig()))) {
        return true;
      }
    }
    return false;
  }

  
  @Override
  public void validatePreSave(boolean isInsert, List<String> errorsToDisplay,
      Map<String, String> validationErrorsToDisplay) {
   
    super.validatePreSave(isInsert, errorsToDisplay, validationErrorsToDisplay);
    
    if (errorsToDisplay.size() > 0 || validationErrorsToDisplay.size() > 0) {
      return;
    } 
    
    Pattern incremntalSyncPattern = Pattern.compile("^changeLog\\.consumer\\.(.*)\\.provisionerConfigId$");
    Set<String> incrementalSyncMatchingConfigIds = GrouperLoaderConfig.retrieveConfig().propertyConfigIds(incremntalSyncPattern);
    
    GrouperConfigurationModuleAttribute provisionerConfigIdAttribute = this.retrieveAttributes().get("provisionerConfigId");
    
    for (String configId: incrementalSyncMatchingConfigIds) {
      String className = "changeLog.consumer."+configId+".publisher.class";
      String provisionerConfigId = "changeLog.consumer."+configId+".provisionerConfigId";
      if (StringUtils.equals(GrouperLoaderConfig.retrieveConfig().propertyValueString(className), ProvisioningConsumer.class.getName()) && 
          StringUtils.equals(GrouperLoaderConfig.retrieveConfig().propertyValueString(provisionerConfigId), provisionerConfigIdAttribute.getValueOrExpressionEvaluation())) {
        
        String errorMessage = GrouperTextContainer.textOrNull("grouperDaemonProvisioningConfigurationNoDuplicateDaemonsAllowed");
        errorsToDisplay.add(errorMessage);
        
        return;
      }
    }
    
  }
}
