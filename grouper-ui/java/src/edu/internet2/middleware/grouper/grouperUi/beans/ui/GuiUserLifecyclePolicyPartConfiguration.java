package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import java.util.ArrayList;
import java.util.List;

import edu.internet2.middleware.grouper.userLifecycle.UserLifecyclePolicyPartConfiguration;

public class GuiUserLifecyclePolicyPartConfiguration {
  
  private UserLifecyclePolicyPartConfiguration userLifecyclePolicyPartConfiguration;
  
  
  public UserLifecyclePolicyPartConfiguration getUserLifecyclePolicyPartConfiguration() {
    return userLifecyclePolicyPartConfiguration;
  }

  private GuiUserLifecyclePolicyPartConfiguration(UserLifecyclePolicyPartConfiguration userLifecyclePolicyPartConfiguration) {
    this.userLifecyclePolicyPartConfiguration = userLifecyclePolicyPartConfiguration;
  }

  public static GuiUserLifecyclePolicyPartConfiguration convertFromUserLifecyclePolicyPartConfiguration(UserLifecyclePolicyPartConfiguration userLifecyclePolicyPartConfiguration) {
    return new GuiUserLifecyclePolicyPartConfiguration(userLifecyclePolicyPartConfiguration);
  }
  
  public static List<GuiUserLifecyclePolicyPartConfiguration> convertFromUserLifecyclePolicyPartConfiguration(List<UserLifecyclePolicyPartConfiguration> userLifecyclePolicyPartConfigurations) {
    
    List<GuiUserLifecyclePolicyPartConfiguration> guiConfigs = new ArrayList<>();
    
    for (UserLifecyclePolicyPartConfiguration userLifecyclePolicyPartConfiguration: userLifecyclePolicyPartConfigurations) {
      guiConfigs.add(convertFromUserLifecyclePolicyPartConfiguration(userLifecyclePolicyPartConfiguration));
    }
    
    return guiConfigs;
    
  }

}
