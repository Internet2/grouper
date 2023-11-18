package edu.internet2.middleware.grouper.app.daemon;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.app.config.GrouperConfigurationModuleAttribute;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderType;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;
import edu.internet2.middleware.grouper.cfg.text.GrouperTextContainer;
import edu.internet2.middleware.grouper.dataField.GrouperDataProviderFullSyncJob;

public class GrouperDaemonOtherJobDataProviderFullSyncConfiguration extends GrouperDaemonConfiguration {

  @Override
  public ConfigFileName getConfigFileName() {
    return ConfigFileName.GROUPER_LOADER_PROPERTIES;
  }
  
  @Override
  public String getConfigIdRegex() {
    return "^(otherJob)\\.([^.]+)\\.(.+)$";
  }
  
  @Override
  public String getConfigItemPrefix() {
    if (StringUtils.isBlank(this.getConfigId())) {
      throw new RuntimeException("Must have configId!");
    }
    return "otherJob." + this.getConfigId() + ".";
  }

  @Override
  public boolean isMultiple() {
    return true;
  }
  
  @Override
  public String getPropertySuffixThatIdentifiesThisConfig() {
    return "class";
  }
  
  @Override
  public String getPropertyValueThatIdentifiesThisConfig() {
    return GrouperDataProviderFullSyncJob.class.getName();
  }
  
  @Override
  public String getDaemonJobPrefix() {
    return GrouperLoaderType.GROUPER_OTHER_JOB_PREFIX;
  }
  

  @Override
  public boolean matchesQuartzJobName(String jobName) {
    if (jobName != null && jobName.startsWith(GrouperLoaderType.GROUPER_OTHER_JOB_PREFIX)) {
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
    
    if (isInsert) {
      Pattern pattern = Pattern.compile("^otherJob\\.(.*)\\.dataProviderConfigId$");
      Set<String> matchingConfigIds = GrouperLoaderConfig.retrieveConfig().propertyConfigIds(pattern);
      
      GrouperConfigurationModuleAttribute dataProviderConfigIdAttribute = this.retrieveAttributes().get("dataProviderConfigId");
      
      for (String configId: matchingConfigIds) {
        String className = "otherJob."+configId+".class";
        String dataProviderConfigId = "otherJob."+configId+".dataProviderConfigId";
        if (StringUtils.equals(GrouperLoaderConfig.retrieveConfig().propertyValueString(className), GrouperDataProviderFullSyncJob.class.getName()) && 
            StringUtils.equals(GrouperLoaderConfig.retrieveConfig().propertyValueString(dataProviderConfigId), dataProviderConfigIdAttribute.getValueOrExpressionEvaluation())) {
          
          String errorMessage = GrouperTextContainer.textOrNull("grouperDaemonDataProviderConfigurationNoDuplicateDaemonsAllowed");
          errorsToDisplay.add(errorMessage);
          
          return;
        }
      }
    }
  }

}