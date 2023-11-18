package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.app.daemon.GrouperDaemonConfiguration;
import edu.internet2.middleware.grouperClient.config.GrouperUiApiTextConfig;

public class GuiGrouperDaemonConfiguration {
  
  private GrouperDaemonConfiguration grouperDaemonConfiguration;
  
  private String jobName;
  
  private boolean enabled = true;

  private String currentConfigSuffix;

  /**
   * current grouped config index we are looping through
   */
  private int index;
  
  private GuiGrouperDaemonConfiguration(GrouperDaemonConfiguration grouperDaemonConfiguration) {
    this.grouperDaemonConfiguration = grouperDaemonConfiguration;
  }
  
  public GrouperDaemonConfiguration getGrouperDaemonConfiguration() {
    return this.grouperDaemonConfiguration;
  }
  
  public static GuiGrouperDaemonConfiguration convertFromGrouperDaemonConfiguration(GrouperDaemonConfiguration grouperDaemonConfiguration) {
    return new GuiGrouperDaemonConfiguration(grouperDaemonConfiguration);
  }
  
  public static List<GuiGrouperDaemonConfiguration> convertFromGrouperDaemonConfiguration(List<GrouperDaemonConfiguration> grouperDaemonConfigurations) {
    
    List<GuiGrouperDaemonConfiguration> guiGrouperDaemonConfigs = new ArrayList<GuiGrouperDaemonConfiguration>();
    
    for (GrouperDaemonConfiguration grouperDaemonConfig: grouperDaemonConfigurations) {
      guiGrouperDaemonConfigs.add(convertFromGrouperDaemonConfiguration(grouperDaemonConfig));
    }
    
    return guiGrouperDaemonConfigs;
    
  }

  
  public String getJobName() {
    return jobName;
  }

  public String getJobDescription() {
    String description = GrouperUiApiTextConfig.retrieveTextConfig().propertyValueString("config." + this.getGrouperDaemonConfiguration().getClass().getSimpleName() + ".description");
    if (StringUtils.isBlank(description)) {
      return "";
    }
    return description;
  }

  
  public void setJobName(String jobName) {
    this.jobName = jobName;
  }

  
  public boolean isEnabled() {
    return enabled;
  }

  
  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public String getCurrentConfigSuffix() {
    return currentConfigSuffix;
  }

  public void setCurrentConfigSuffix(String currentConfigSuffix) {
    this.currentConfigSuffix = currentConfigSuffix;
  }

  public int getIndex() {
    return index;
  }

  public void setIndex(int index) {
    this.index = index;
  }
  
  
}
