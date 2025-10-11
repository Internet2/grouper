package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import java.util.ArrayList;
import java.util.List;

import edu.internet2.middleware.grouper.userLifecycle.UserLifecyclePolicyConfiguration;

public class GuiUserLifecyclePolicyConfiguration {
  
  private UserLifecyclePolicyConfiguration userLifecyclePolicyConfiguration;
  
  
  public UserLifecyclePolicyConfiguration getUserLifecyclePolicyConfiguration() {
    return userLifecyclePolicyConfiguration;
  }

  private GuiUserLifecyclePolicyConfiguration(UserLifecyclePolicyConfiguration userLifecyclePolicyConfiguration) {
    this.userLifecyclePolicyConfiguration = userLifecyclePolicyConfiguration;
  }

  public static GuiUserLifecyclePolicyConfiguration convertFromUserLifecyclePolicyConfiguration(UserLifecyclePolicyConfiguration userLifecyclePolicyConfiguration) {
    return new GuiUserLifecyclePolicyConfiguration(userLifecyclePolicyConfiguration);
  }
  
  public static List<GuiUserLifecyclePolicyConfiguration> convertFromUserLifecyclePolicyConfiguration(List<UserLifecyclePolicyConfiguration> userLifecyclePolicyConfigurations) {
    
    List<GuiUserLifecyclePolicyConfiguration> guiConfigs = new ArrayList<>();
    
    for (UserLifecyclePolicyConfiguration userLifecyclePolicyConfiguration: userLifecyclePolicyConfigurations) {
      guiConfigs.add(convertFromUserLifecyclePolicyConfiguration(userLifecyclePolicyConfiguration));
    }
    
    return guiConfigs;
    
  }

}
