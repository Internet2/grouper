package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import java.util.ArrayList;
import java.util.List;

import edu.internet2.middleware.grouper.app.provisioning.ProvisionerConfiguration;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSync;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncDao;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncJob;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncLog;

public class GuiProvisionerConfiguration {
  
  /**
   * provisioner configuration this instance is wrapping
   */
  private ProvisionerConfiguration provisionerConfiguration;
  
  private String lastFullSyncTimestamp;
  private String lastIncrementalSyncTimestamp;
  private int groupCount;
  private int userCount;
  private int recordsCount;
  
  private GuiProvisionerConfiguration() {}
  
  public ProvisionerConfiguration getProvisionerConfiguration() {
    return this.provisionerConfiguration;
  }
  
  
  public String getLastFullSyncTimestamp() {
    return lastFullSyncTimestamp;
  }

  
  public String getLastIncrementalSyncTimestamp() {
    return lastIncrementalSyncTimestamp;
  }

  
  public int getGroupCount() {
    return groupCount;
  }

  
  public int getUserCount() {
    return userCount;
  }

  
  public int getRecordsCount() {
    return recordsCount;
  }

  /**
   * convert from provisioner configuration to gui provisioner configuration
   * @param provisionerConfiguration
   * @return
   */
  public static GuiProvisionerConfiguration convertFromProvisionerConfiguration(ProvisionerConfiguration provisionerConfiguration) {
    
    GuiProvisionerConfiguration guiProvisionerConfig = new GuiProvisionerConfiguration();
    guiProvisionerConfig.provisionerConfiguration = provisionerConfiguration;
    
    //String configId = provisionerConfiguration.getConfigId();
//    GcGrouperSync grouperSync = GcGrouperSyncDao.retrieveByProvisionerName(null, configId);
//    
//    if (grouperSync != null) {
//      if (grouperSync.getLastFullSyncRun() != null) {
//        guiProvisionerConfig.lastFullSyncTimestamp = GrouperUtil.dateStringValue(grouperSync.getLastFullSyncRun());
//      }
//      
//      if (grouperSync.getIncrementalTimestamp() != null) {
//        guiProvisionerConfig.lastIncrementalSyncTimestamp = GrouperUtil.dateStringValue(grouperSync.getIncrementalTimestamp());
//      }
//      
//      guiProvisionerConfig.groupCount = grouperSync.getGroupCount();
//      guiProvisionerConfig.userCount = grouperSync.getUserCount();
//      guiProvisionerConfig.recordsCount = grouperSync.getRecordsCount();
//      
//      List<GcGrouperSyncJob> grouperSyncJobs = grouperSync.getGcGrouperSyncJobDao().jobRetrieveAll();
//      for (GcGrouperSyncJob grouperSyncJob: grouperSyncJobs) {
//        GcGrouperSyncLog grouperSyncLog = grouperSync.getGcGrouperSyncLogDao().logRetrieveMostRecent(grouperSyncJob.getId());
//      }
//      
//      
//    }

    return guiProvisionerConfig;
  }
  
  /**
   * convert from list of provisioner configurations to gui provisioner configurations
   * @param provisionerConfigurations
   * @return
   */
  public static List<GuiProvisionerConfiguration> convertFromProvisionerConfiguration(List<ProvisionerConfiguration> provisionerConfigurations) {
    
    List<GuiProvisionerConfiguration> guiProvisionerConfigurations = new ArrayList<GuiProvisionerConfiguration>();
    
    for (ProvisionerConfiguration provisionerConfiguration: provisionerConfigurations) {
      guiProvisionerConfigurations.add(convertFromProvisionerConfiguration(provisionerConfiguration));
    }
    
    return guiProvisionerConfigurations;
    
  }

}
