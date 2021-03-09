package edu.internet2.middleware.grouper.app.daemon;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.app.config.GrouperConfigurationModuleAttribute;
import edu.internet2.middleware.grouper.app.config.GrouperConfigurationModuleBase;
import edu.internet2.middleware.grouper.app.ldapToSql.GrouperDaemonOtherJobLdapToSqlConfiguration;
import edu.internet2.middleware.grouper.app.syncToGrouper.GrouperDaemonOtherJobSyncToGrouperFromSqlConfiguration;
import edu.internet2.middleware.grouper.cfg.text.GrouperTextContainer;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.util.ExpirableCache;

public abstract class GrouperDaemonConfiguration extends GrouperConfigurationModuleBase {
  
  public abstract boolean matchesQuartzJobName(String jobName);
  
  public String getDaemonJobPrefix() {
    return null;
  }
  
  public final static Set<String> grouperDaemonConfigClassNames = new LinkedHashSet<String>();
  
  static {
    
    grouperDaemonConfigClassNames.add(GrouperDaemonBuiltInMessagingConfiguration.class.getName());
    grouperDaemonConfigClassNames.add(GrouperDaemonChangeLogConsumerConfiguration.class.getName());
    grouperDaemonConfigClassNames.add(GrouperDaemonChangeLogEsbConfiguration.class.getName());
    grouperDaemonConfigClassNames.add(GrouperDaemonChangeLogEsbToMessagingConfiguration.class.getName());
    grouperDaemonConfigClassNames.add(GrouperDaemonChangeLogRecentMembershipsConfiguration.class.getName());
    grouperDaemonConfigClassNames.add(GrouperDaemonChangeLogRulesConfiguration.class.getName());
    grouperDaemonConfigClassNames.add(GrouperDaemonChangeLogSyncGroupsConfiguration.class.getName());
    grouperDaemonConfigClassNames.add(GrouperDaemonChangeLogTempToChangeLogConfiguration.class.getName());
    grouperDaemonConfigClassNames.add(GrouperDaemonChangeLogToMessagingConfiguration.class.getName());
    grouperDaemonConfigClassNames.add(GrouperDaemonOtherJobCsvReportConfiguration.class.getName());
    grouperDaemonConfigClassNames.add(GrouperDaemonOtherJobConfiguration.class.getName());
    grouperDaemonConfigClassNames.add(GrouperDaemonCleanLogsConfiguration.class.getName());
    grouperDaemonConfigClassNames.add(GrouperDaemonEnabledDisabledConfiguration.class.getName());
    grouperDaemonConfigClassNames.add(GrouperDaemonOtherJobLoaderIncrementalConfiguration.class.getName());
    grouperDaemonConfigClassNames.add(GrouperDaemonMessagingListenerConfiguration.class.getName());
    grouperDaemonConfigClassNames.add(GrouperDaemonMessagingListenerToChangeLogConfiguration.class.getName());
    grouperDaemonConfigClassNames.add(GrouperDaemonOtherJobAttestationConfiguration.class.getName());
    grouperDaemonConfigClassNames.add(GrouperDaemonOtherJobDeprovisioningConfiguration.class.getName());
    grouperDaemonConfigClassNames.add(GrouperDaemonOtherJobFindBadMembershipsConfiguration.class.getName());
    grouperDaemonConfigClassNames.add(GrouperDaemonOtherJobInstrumentationConfiguration.class.getName());
    grouperDaemonConfigClassNames.add(GrouperDaemonOtherJobLdapToSqlConfiguration.class.getName());
    grouperDaemonConfigClassNames.add(GrouperDaemonOtherJobNotificationConfiguration.class.getName());
    grouperDaemonConfigClassNames.add(GrouperDaemonOtherJobObjectTypeConfiguration.class.getName());
    grouperDaemonConfigClassNames.add(GrouperDaemonOtherJobProvisioningConfiguration.class.getName());
    grouperDaemonConfigClassNames.add(GrouperDaemonOtherJobReportClearConfiguration.class.getName());
    grouperDaemonConfigClassNames.add(GrouperDaemonOtherJobScriptConfiguration.class.getName());
    grouperDaemonConfigClassNames.add(GrouperDaemonOtherJobSchedulerCheckConfiguration.class.getName());
    grouperDaemonConfigClassNames.add(GrouperDaemonOtherJobSyncToGrouperFromSqlConfiguration.class.getName());
    grouperDaemonConfigClassNames.add(GrouperDaemonOtherJobTimeConfiguration.class.getName());
    grouperDaemonConfigClassNames.add(GrouperDaemonOtherJobUpgradeTasksConfiguration.class.getName());
    grouperDaemonConfigClassNames.add(GrouperDaemonOtherJobUsduConfiguration.class.getName());
    grouperDaemonConfigClassNames.add(GrouperDaemonOtherJobWorkflowConfiguration.class.getName());
    grouperDaemonConfigClassNames.add(GrouperDaemonOtherJobWorkflowReminderConfiguration.class.getName());
    grouperDaemonConfigClassNames.add(GrouperDaemonOtherJobWsMessagingBridgeConfiguration.class.getName());
    grouperDaemonConfigClassNames.add(GrouperDaemonPspngFullSyncConfiguration.class.getName());
    grouperDaemonConfigClassNames.add(GrouperDaemonReportConfiguration.class.getName());
    grouperDaemonConfigClassNames.add(GrouperDaemonRulesConfiguration.class.getName());
    grouperDaemonConfigClassNames.add(GrouperDaemonProvisioningIncrementalSyncConfiguration.class.getName());
    grouperDaemonConfigClassNames.add(GrouperDaemonOtherJobProvisioningFullSyncConfiguration.class.getName());
    grouperDaemonConfigClassNames.add(GrouperDaemonOtherJobTableSyncConfiguration.class.getName());
    
  }

  /**
   * list of daemon types that can be configured
   * @return
   */
  public static List<GrouperDaemonConfiguration> retrieveAllModuleConfigurationTypes() {
    return (List<GrouperDaemonConfiguration>) (Object) retrieveAllConfigurationTypesHelper(grouperDaemonConfigClassNames);
  }
  
  public Collection<GrouperConfigurationModuleAttribute> getConfigAttributes() {
    return this.retrieveAttributes().values();
  }
  
  
  @Override
  public String retrieveSuffix(Pattern pattern, String propertyName) {
    
    if (!propertyName.startsWith(this.getConfigItemPrefix())) {
      return null;
    }
    
    return StringUtils.replace(propertyName, this.getConfigItemPrefix(), "");
  }
  
  
  @Override
  protected String getConfigurationTypePrefix() {
    return "daemonConfig";
  }

  /**
   * 
   * @param isInsert
   * @param fromUi
   * @param errorsToDisplay
   * @param validationErrorsToDisplay
   */
  public void validatePreSave(boolean isInsert, boolean fromUi, List<String> errorsToDisplay, Map<String, String> validationErrorsToDisplay) {
    super.validatePreSave(isInsert, errorsToDisplay, validationErrorsToDisplay);
    if (!isInsert && isMultiple()) {
      if (!this.retrieveConfigurationConfigIds().contains(this.getConfigId())) {
        validationErrorsToDisplay.put("#configId", GrouperTextContainer.textOrNull("grouperConfigurationValidationConfigIdDoesntExist"));
      }
      Pattern configIdPattern = Pattern.compile("^[a-zA-Z0-9_]+$");
      if (!configIdPattern.matcher(this.getConfigId()).matches()) {
        validationErrorsToDisplay.put("#configId", GrouperTextContainer.textOrNull("grouperConfigurationValidationConfigIdInvalid"));
      }
    }
  }
  
  private static ExpirableCache<String, GrouperDaemonConfiguration> jobNameToGrouperDaemonConfigCache = new ExpirableCache<String, GrouperDaemonConfiguration>(1);

  public static GrouperDaemonConfiguration retrieveImplementationFromJobName(String jobName) {
    
    GrouperDaemonConfiguration result = jobNameToGrouperDaemonConfigCache.get(jobName);
    
    if (result != null) {
      return result;
    }
    
    synchronized (GrouperDaemonConfiguration.class) {
      result = jobNameToGrouperDaemonConfigCache.get(jobName);
      
      if (result != null) {
        return result;
      }
      for (String className: grouperDaemonConfigClassNames) {
        
        Class<GrouperDaemonConfiguration> grouperDaemonConfigurationClass = (Class<GrouperDaemonConfiguration>) GrouperUtil.forName(className);
        GrouperDaemonConfiguration grouperDaemonConfig = GrouperUtil.newInstance(grouperDaemonConfigurationClass);
        if (jobName.startsWith(grouperDaemonConfig.getDaemonJobPrefix())) {
          if (grouperDaemonConfig.isMultiple()) {
            String configId = GrouperUtil.stripPrefix(jobName, grouperDaemonConfig.getDaemonJobPrefix());
            grouperDaemonConfig.setConfigId(configId);
          }
        } else {
            continue;
        }
        if (grouperDaemonConfig instanceof GrouperDaemonOtherJobConfiguration) {
          continue;
        }
        if (grouperDaemonConfig instanceof GrouperDaemonChangeLogConsumerConfiguration) {
          continue;
        }
        if (grouperDaemonConfig instanceof GrouperDaemonMessagingListenerConfiguration) {
          continue;
        }
        if (grouperDaemonConfig instanceof GrouperDaemonChangeLogEsbConfiguration) {
          continue;
        }
        if (grouperDaemonConfig.matchesQuartzJobName(jobName)) {          
          if (result != null) {
            throw new RuntimeException(jobName + " matches "+ grouperDaemonConfig + " and also " + result);
          }
          result = grouperDaemonConfig;
        }
      }
        
      if (result != null) {
        jobNameToGrouperDaemonConfigCache.put(jobName, result);
        return result;
      }

      GrouperDaemonMessagingListenerConfiguration grouperDaemonMessagingListenerConfiguration = new GrouperDaemonMessagingListenerConfiguration();
      String configId = GrouperUtil.stripPrefix(jobName, grouperDaemonMessagingListenerConfiguration.getDaemonJobPrefix());
      grouperDaemonMessagingListenerConfiguration.setConfigId(configId);
      if (grouperDaemonMessagingListenerConfiguration.matchesQuartzJobName(jobName)) {
        jobNameToGrouperDaemonConfigCache.put(jobName, grouperDaemonMessagingListenerConfiguration);
        return grouperDaemonMessagingListenerConfiguration;
      }

      GrouperDaemonOtherJobConfiguration grouperDaemonOtherJobConfiguration = new GrouperDaemonOtherJobConfiguration();
      configId = GrouperUtil.stripPrefix(jobName, grouperDaemonOtherJobConfiguration.getDaemonJobPrefix());
      grouperDaemonOtherJobConfiguration.setConfigId(configId);
      if (grouperDaemonOtherJobConfiguration.matchesQuartzJobName(jobName)) {
        jobNameToGrouperDaemonConfigCache.put(jobName, grouperDaemonOtherJobConfiguration);
        return grouperDaemonOtherJobConfiguration;
      }

      // note ESB needs to be above the generic change log below
      GrouperDaemonChangeLogEsbConfiguration grouperDaemonChangeLogEsbConfiguration = new GrouperDaemonChangeLogEsbConfiguration();
      configId = GrouperUtil.stripPrefix(jobName, grouperDaemonChangeLogEsbConfiguration.getDaemonJobPrefix());
      grouperDaemonChangeLogEsbConfiguration.setConfigId(configId);
      if (grouperDaemonChangeLogEsbConfiguration.matchesQuartzJobName(jobName)) {
        jobNameToGrouperDaemonConfigCache.put(jobName, grouperDaemonChangeLogEsbConfiguration);
        return grouperDaemonChangeLogEsbConfiguration;
      }

      GrouperDaemonChangeLogConsumerConfiguration grouperDaemonChangeLogConsumerConfiguration = new GrouperDaemonChangeLogConsumerConfiguration();
      configId = GrouperUtil.stripPrefix(jobName, grouperDaemonChangeLogConsumerConfiguration.getDaemonJobPrefix());
      grouperDaemonChangeLogConsumerConfiguration.setConfigId(configId);
      if (grouperDaemonChangeLogConsumerConfiguration.matchesQuartzJobName(jobName)) {
        jobNameToGrouperDaemonConfigCache.put(jobName, grouperDaemonChangeLogConsumerConfiguration);
        return grouperDaemonChangeLogConsumerConfiguration;
      }

      throw new RuntimeException("Can't find daemon config for jobName "+jobName);
      
    }
    
  }

  /**
   * 
   */
  public static void clearImplementationJobNameCache() {
    jobNameToGrouperDaemonConfigCache.clear();
    
  }

}
