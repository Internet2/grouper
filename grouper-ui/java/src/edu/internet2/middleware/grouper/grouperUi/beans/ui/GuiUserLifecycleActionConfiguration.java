package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import java.util.ArrayList;
import java.util.List;

import edu.internet2.middleware.grouper.userLifecycle.UserLifecycleActionConfiguration;

public class GuiUserLifecycleActionConfiguration {
  
  private UserLifecycleActionConfiguration userLifecycleActionConfiguration;
  
  
  public UserLifecycleActionConfiguration getUserLifecycleActionConfiguration() {
    return userLifecycleActionConfiguration;
  }

  private GuiUserLifecycleActionConfiguration(UserLifecycleActionConfiguration userLifecycleActionConfiguration) {
    this.userLifecycleActionConfiguration = userLifecycleActionConfiguration;
  }

  public static GuiUserLifecycleActionConfiguration convertFromUserLifecycleActionConfiguration(UserLifecycleActionConfiguration userLifecycleActionConfiguration) {
    return new GuiUserLifecycleActionConfiguration(userLifecycleActionConfiguration);
  }
  
  public static List<GuiUserLifecycleActionConfiguration> convertFromUserLifecycleActionConfiguration(List<UserLifecycleActionConfiguration> userLifecycleActionConfigurations) {
    
    List<GuiUserLifecycleActionConfiguration> guiConfigs = new ArrayList<>();
    
    for (UserLifecycleActionConfiguration userLifecycleActionConfiguration: userLifecycleActionConfigurations) {
      guiConfigs.add(convertFromUserLifecycleActionConfiguration(userLifecycleActionConfiguration));
    }
    
    return guiConfigs;
    
  }

}
