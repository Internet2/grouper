package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import java.util.ArrayList;
import java.util.List;

import edu.internet2.middleware.grouper.app.daemon.GrouperDaemonConfiguration;

public class GuiGrouperDaemonConfiguration {
  
  private GrouperDaemonConfiguration grouperDaemonConfiguration;
  
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

}
