package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.quartz.JobKey;
import org.quartz.Scheduler;

import edu.internet2.middleware.grouper.app.loader.GrouperLoader;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderType;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.dataField.GrouperDataProviderConfiguration;
import edu.internet2.middleware.grouper.dataField.GrouperDataProviderFullSyncJob;
import edu.internet2.middleware.grouper.dataField.GrouperDataProviderIncrementalSyncJob;
import edu.internet2.middleware.grouper.util.GrouperUtil;

public class GuiDataProviderConfiguration {
  
  private GrouperDataProviderConfiguration grouperDataProviderConfiguration;
  
  public GrouperDataProviderConfiguration getGrouperDataProviderConfiguration() {
    return grouperDataProviderConfiguration;
  }

  /**
   * subject source id, or "N/A" if this provider is not a subject source
   * @return the subject source id or N/A
   */
  public String getSubjectSourceId() {
    boolean subjectSource = GrouperUtil.booleanValue(
        this.grouperDataProviderConfiguration.retrieveAttributeValueFromConfig("subjectSource", false), false);
    if (!subjectSource) {
      return "N/A";
    }
    return this.grouperDataProviderConfiguration.retrieveAttributeValueFromConfig("subjectSourceId", false);
  }

  /**
   * number of data provider queries that reference this data provider
   * @return the count
   */
  public int getNumberOfQueries() {
    String configId = this.grouperDataProviderConfiguration.getConfigId();
    int count = 0;
    Pattern queryPattern = Pattern.compile("^grouperDataProviderQuery\\.([^.]+)\\.providerConfigId$");
    Set<String> queryConfigIds = GrouperConfig.retrieveConfig().propertyConfigIds(queryPattern);
    for (String queryConfigId : GrouperUtil.nonNull(queryConfigIds)) {
      String providerConfigId = GrouperConfig.retrieveConfig().propertyValueString(
          "grouperDataProviderQuery." + queryConfigId + ".providerConfigId");
      if (StringUtils.equals(configId, providerConfigId)) {
        count++;
      }
    }
    return count;
  }

  private boolean fullSyncDaemonJobNameResolved = false;
  private String fullSyncDaemonJobName;

  private boolean incrementalSyncDaemonJobNameResolved = false;
  private String incrementalSyncDaemonJobName;

  /**
   * job name of the full sync daemon for this data provider, but only if that daemon is
   * currently scheduled in quartz (so we don't link to a broken view logs url).
   * returns null if there is no scheduled full sync daemon for this provider.
   * @return the job name or null
   */
  public String getFullSyncDaemonJobName() {
    if (!this.fullSyncDaemonJobNameResolved) {
      this.fullSyncDaemonJobName = retrieveScheduledDaemonJobName(
          this.grouperDataProviderConfiguration.getConfigId(), GrouperDataProviderFullSyncJob.class.getName());
      this.fullSyncDaemonJobNameResolved = true;
    }
    return this.fullSyncDaemonJobName;
  }

  /**
   * job name of the incremental sync daemon for this data provider, but only if that daemon is
   * currently scheduled in quartz (so we don't link to a broken view logs url).
   * returns null if there is no scheduled incremental sync daemon for this provider.
   * @return the job name or null
   */
  public String getIncrementalSyncDaemonJobName() {
    if (!this.incrementalSyncDaemonJobNameResolved) {
      this.incrementalSyncDaemonJobName = retrieveScheduledDaemonJobName(
          this.grouperDataProviderConfiguration.getConfigId(), GrouperDataProviderIncrementalSyncJob.class.getName());
      this.incrementalSyncDaemonJobNameResolved = true;
    }
    return this.incrementalSyncDaemonJobName;
  }

  /**
   * find the scheduled daemon (otherJob) of the given class that targets the given data provider.
   * @param providerConfigId data provider config id the daemon must target
   * @param daemonClassName fully qualified class name of the daemon job
   * @return the quartz job name if scheduled, otherwise null
   */
  public static String retrieveScheduledDaemonJobName(String providerConfigId, String daemonClassName) {

    String configId = providerConfigId;

    Pattern daemonPattern = Pattern.compile("^otherJob\\.([^.]+)\\.dataProviderConfigId$");
    Set<String> daemonConfigIds = GrouperLoaderConfig.retrieveConfig().propertyConfigIds(daemonPattern);

    for (String daemonConfigId : GrouperUtil.nonNull(daemonConfigIds)) {

      String daemonProviderConfigId = GrouperLoaderConfig.retrieveConfig().propertyValueString(
          "otherJob." + daemonConfigId + ".dataProviderConfigId");
      if (!StringUtils.equals(configId, daemonProviderConfigId)) {
        continue;
      }

      String className = GrouperLoaderConfig.retrieveConfig().propertyValueString("otherJob." + daemonConfigId + ".class");
      if (!StringUtils.equals(daemonClassName, className)) {
        continue;
      }

      String jobName = GrouperLoaderType.GROUPER_OTHER_JOB_PREFIX + daemonConfigId;

      if (isJobScheduled(jobName)) {
        return jobName;
      }
    }

    return null;
  }

  /**
   * whether a quartz job with this name exists in the scheduler
   * @param jobName the quartz job name
   * @return true if scheduled
   */
  private static boolean isJobScheduled(String jobName) {
    if (StringUtils.isBlank(jobName)) {
      return false;
    }
    try {
      Scheduler scheduler = GrouperLoader.schedulerFactory().getScheduler();
      if (scheduler == null) {
        return false;
      }
      return scheduler.checkExists(new JobKey(jobName));
    } catch (Exception e) {
      return false;
    }
  }
  
  private GuiDataProviderConfiguration(GrouperDataProviderConfiguration grouperDataProviderConfiguration) {
    this.grouperDataProviderConfiguration = grouperDataProviderConfiguration;
  }
  
  public static GuiDataProviderConfiguration convertFromDataProviderConfiguration(GrouperDataProviderConfiguration grouperDataProviderConfiguration) {
    return new GuiDataProviderConfiguration(grouperDataProviderConfiguration);
  }
  
  public static List<GuiDataProviderConfiguration> convertFromDataProviderConfiguration(List<GrouperDataProviderConfiguration> dataProviderConfigurations) {
    
    List<GuiDataProviderConfiguration> guiDataRowConfigs = new ArrayList<GuiDataProviderConfiguration>();
    
    for (GrouperDataProviderConfiguration grouperDataProviderConfiguration: dataProviderConfigurations) {
      guiDataRowConfigs.add(convertFromDataProviderConfiguration(grouperDataProviderConfiguration));
    }
    
    return guiDataRowConfigs;
    
  }

}
