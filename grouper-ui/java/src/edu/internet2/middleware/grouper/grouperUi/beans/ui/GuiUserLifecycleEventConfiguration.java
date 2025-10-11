package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import java.util.ArrayList;
import java.util.List;

import edu.internet2.middleware.grouper.userLifecycle.UserLifecycleEventConfiguration;

public class GuiUserLifecycleEventConfiguration {
  
  private UserLifecycleEventConfiguration userLifecycleEventConfiguration;
  
  
  public UserLifecycleEventConfiguration getUserLifecycleEventConfiguration() {
    return userLifecycleEventConfiguration;
  }

  private GuiUserLifecycleEventConfiguration(UserLifecycleEventConfiguration userLifecycleEventConfiguration) {
    this.userLifecycleEventConfiguration = userLifecycleEventConfiguration;
  }

  public static GuiUserLifecycleEventConfiguration convertFromUserLifecycleEventConfiguration(UserLifecycleEventConfiguration userLifecycleEventConfiguration) {
    return new GuiUserLifecycleEventConfiguration(userLifecycleEventConfiguration);
  }
  
  public static List<GuiUserLifecycleEventConfiguration> convertFromUserLifecycleEventConfiguration(List<UserLifecycleEventConfiguration> userLifecycleEventConfigurations) {
    
    List<GuiUserLifecycleEventConfiguration> guiConfigs = new ArrayList<>();
    
    for (UserLifecycleEventConfiguration userLifecycleEventConfiguration: userLifecycleEventConfigurations) {
      guiConfigs.add(convertFromUserLifecycleEventConfiguration(userLifecycleEventConfiguration));
    }
    
    return guiConfigs;
    
  }

}
