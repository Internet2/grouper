package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import java.util.ArrayList;
import java.util.List;

import edu.internet2.middleware.grouper.dataField.GrouperDataProviderChangeLogQueryConfiguration;
import edu.internet2.middleware.grouper.dataField.GrouperDataProviderIncrementalSyncJob;

public class GuiDataProviderChangeLogQueryConfiguration {
  
  private GrouperDataProviderChangeLogQueryConfiguration grouperDataProviderChangeLogQueryConfiguration;
  
  public GrouperDataProviderChangeLogQueryConfiguration getGrouperDataProviderChangeLogQueryConfiguration() {
    return grouperDataProviderChangeLogQueryConfiguration;
  }

  /**
   * change log attribute that contains the timestamp for when a row was added/changed
   * @return the timestamp attribute
   */
  public String getTimestampAttribute() {
    return this.grouperDataProviderChangeLogQueryConfiguration.retrieveAttributeValueFromConfig("providerChangeLogQueryTimestampAttribute", false);
  }

  private boolean incrementalSyncDaemonJobNameResolved = false;
  private String incrementalSyncDaemonJobName;

  /**
   * job name of the incremental sync daemon that processes this change log query's data provider,
   * but only if that daemon is currently scheduled in quartz (so we don't link to a broken view logs url).
   * returns null if there is no scheduled incremental sync daemon for the provider.
   * @return the job name or null
   */
  public String getIncrementalSyncDaemonJobName() {
    if (!this.incrementalSyncDaemonJobNameResolved) {
      String providerConfigId = this.grouperDataProviderChangeLogQueryConfiguration.retrieveAttributeValueFromConfig("providerConfigId", false);
      this.incrementalSyncDaemonJobName = GuiDataProviderConfiguration.retrieveScheduledDaemonJobName(
          providerConfigId, GrouperDataProviderIncrementalSyncJob.class.getName());
      this.incrementalSyncDaemonJobNameResolved = true;
    }
    return this.incrementalSyncDaemonJobName;
  }
  
  private GuiDataProviderChangeLogQueryConfiguration(GrouperDataProviderChangeLogQueryConfiguration grouperDataProviderChangeLogQueryConfiguration) {
    this.grouperDataProviderChangeLogQueryConfiguration = grouperDataProviderChangeLogQueryConfiguration;
  }
  
  public static GuiDataProviderChangeLogQueryConfiguration convertFromDataProviderChangeLogQueryConfiguration(GrouperDataProviderChangeLogQueryConfiguration grouperDataProviderChangeLogQueryConfiguration) {
    return new GuiDataProviderChangeLogQueryConfiguration(grouperDataProviderChangeLogQueryConfiguration);
  }
  
  public static List<GuiDataProviderChangeLogQueryConfiguration> convertFromDataProviderChangeLogQueryConfiguration(List<GrouperDataProviderChangeLogQueryConfiguration> dataProviderChangeLogQueryConfigurations) {
    
    List<GuiDataProviderChangeLogQueryConfiguration> guiDataProviderChangeLogQueryConfigs = new ArrayList<GuiDataProviderChangeLogQueryConfiguration>();
    
    for (GrouperDataProviderChangeLogQueryConfiguration grouperDataProviderConfiguration: dataProviderChangeLogQueryConfigurations) {
      guiDataProviderChangeLogQueryConfigs.add(convertFromDataProviderChangeLogQueryConfiguration(grouperDataProviderConfiguration));
    }
    
    return guiDataProviderChangeLogQueryConfigs;
    
  }

}
