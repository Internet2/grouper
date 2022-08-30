package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningFullSyncJob;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningConfiguration;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningConsumer;

public class GuiProvisionerConfiguration {
  
  /**
   * provisioner configuration this instance is wrapping
   */
  private ProvisioningConfiguration provisionerConfiguration;
  
  private String lastFullSyncTimestamp;
  private String lastIncrementalSyncTimestamp;
  private int groupCount;
  private int userCount;
  private int membershipCount;
  
  private String fullSyncJobName;
  private String incrementalSyncJobName;
  
  private GuiProvisionerConfiguration() {}
  
  public ProvisioningConfiguration getProvisionerConfiguration() {
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

  
  public int getMembershipCount() {
    return membershipCount;
  }

  /**
   * convert from provisioner configuration to gui provisioner configuration
   * @param provisionerConfiguration
   * @return
   */
  public static GuiProvisionerConfiguration convertFromProvisioningConfiguration(ProvisioningConfiguration provisioningConfiguration) {
    
    GuiProvisionerConfiguration guiProvisioningConfig = new GuiProvisionerConfiguration();
    guiProvisioningConfig.provisionerConfiguration = provisioningConfiguration;
    return guiProvisioningConfig;
  }
  
  /**
   * convert from list of provisioner configurations to gui provisioner configurations
   * @param provisioningConfigurations
   * @return
   */
  public static List<GuiProvisionerConfiguration> convertFromProvisioningConfiguration(List<ProvisioningConfiguration> provisioningConfigurations) {
    
    List<GuiProvisionerConfiguration> guiProvisioningConfigurations = new ArrayList<GuiProvisionerConfiguration>();
    
    Pattern fullSyncPattern = Pattern.compile("^otherJob\\.(.*)\\.provisionerConfigId$");
    Set<String> fullSyncMatchingConfigIds = GrouperLoaderConfig.retrieveConfig().propertyConfigIds(fullSyncPattern);
    
    Pattern incrementalSyncPattern = Pattern.compile("^changeLog\\.consumer\\.(.*)\\.provisionerConfigId$");
    Set<String> incrementalSyncMatchingConfigIds = GrouperLoaderConfig.retrieveConfig().propertyConfigIds(incrementalSyncPattern);
    
    for (ProvisioningConfiguration provisioningConfiguration: provisioningConfigurations) {
      
      GuiProvisionerConfiguration guiProvisionerConfiguration = convertFromProvisioningConfiguration(provisioningConfiguration);
      guiProvisioningConfigurations.add(guiProvisionerConfiguration);
      
      List<String> fullSyncConfigIds = new ArrayList<>();
      
      for (String configId: fullSyncMatchingConfigIds) {
        String className = "otherJob."+configId+".class";
        String provisionerConfigId = "otherJob."+configId+".provisionerConfigId";
        if (StringUtils.equals(GrouperLoaderConfig.retrieveConfig().propertyValueString(className), GrouperProvisioningFullSyncJob.class.getName()) && 
            StringUtils.equals(GrouperLoaderConfig.retrieveConfig().propertyValueString(provisionerConfigId), provisioningConfiguration.getConfigId())) {
          fullSyncConfigIds.add(configId);
        }
      }
      
      if (fullSyncConfigIds.size() == 1) {
        
        String fullSyncJobName = "OTHER_JOB_"+fullSyncConfigIds.get(0);
        guiProvisionerConfiguration.setFullSyncJobName(fullSyncJobName);
      }
      
      List<String> incrementalSyncConfigIds = new ArrayList<>();
      
      for (String configId: incrementalSyncMatchingConfigIds) {
        String className = "changeLog.consumer."+configId+".publisher.class";
        String provisionerConfigId = "changeLog.consumer."+configId+".provisionerConfigId";
        if (StringUtils.equals(GrouperLoaderConfig.retrieveConfig().propertyValueString(className), ProvisioningConsumer.class.getName()) && 
            StringUtils.equals(GrouperLoaderConfig.retrieveConfig().propertyValueString(provisionerConfigId), provisioningConfiguration.getConfigId() )) {
          incrementalSyncConfigIds.add(configId);
        }
      }
      
      if (incrementalSyncConfigIds.size() == 1) {
        String incrementalSyncJobName = "CHANGE_LOG_consumer_"+incrementalSyncConfigIds.get(0);
        guiProvisionerConfiguration.setIncrementalSyncJobName(incrementalSyncJobName);
      }
      
    }
    
    return guiProvisioningConfigurations;
    
  }

  
  public String getFullSyncJobName() {
    return fullSyncJobName;
  }

  
  public void setFullSyncJobName(String fullSyncJobName) {
    this.fullSyncJobName = fullSyncJobName;
  }

  
  public String getIncrementalSyncJobName() {
    return incrementalSyncJobName;
  }

  
  public void setIncrementalSyncJobName(String incrementalSyncJobName) {
    this.incrementalSyncJobName = incrementalSyncJobName;
  }
  
  
  
  

}
