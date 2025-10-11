package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import java.util.Arrays;
import java.util.List;

import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.userLifecycle.UserLifecycleActionConfiguration;
import edu.internet2.middleware.grouper.userLifecycle.UserLifecycleEventConfiguration;
import edu.internet2.middleware.grouper.userLifecycle.UserLifecyclePolicyConfiguration;
import edu.internet2.middleware.grouper.userLifecycle.UserLifecyclePolicyPartConfiguration;
import edu.internet2.middleware.subject.Subject;

public class UserLifecycleContainer {
  
  private int userLifecycleEventCount;
  
  private int userLifecycleActionCount;

  private int userLifecyclePolicyCount;
  
  private int userLifecyclePolicyPartCount;

  private GuiUserLifecycleEventConfiguration guiUserLifecycleEventConfiguration;

  private List<GuiUserLifecycleEventConfiguration> guiUserLifecycleEventConfigurations;
  
  private GuiUserLifecycleActionConfiguration guiUserLifecycleActionConfiguration;

  private List<GuiUserLifecycleActionConfiguration> guiUserLifecycleActionConfigurations;
  
  private GuiUserLifecyclePolicyConfiguration guiUserLifecyclePolicyConfiguration;

  private List<GuiUserLifecyclePolicyConfiguration> guiUserLifecyclePolicyConfigurations;
  
  private GuiUserLifecyclePolicyPartConfiguration guiUserLifecyclePolicyPartConfiguration;

  private List<GuiUserLifecyclePolicyPartConfiguration> guiUserLifecyclePolicyPartConfigurations;

  
  public int getUserLifecycleEventCount() {
    return userLifecycleEventCount;
  }

  
  public void setUserLifecycleEventCount(int userLifecycleEventCount) {
    this.userLifecycleEventCount = userLifecycleEventCount;
  }

  public int getUserLifecycleActionCount() {
    return userLifecycleActionCount;
  }
  
  public void setUserLifecycleActionCount(int userLifecycleActionCount) {
    this.userLifecycleActionCount = userLifecycleActionCount;
  }
  
  public int getUserLifecyclePolicyCount() {
    return userLifecyclePolicyCount;
  }

  public void setUserLifecyclePolicyCount(int userLifecyclePolicyCount) {
    this.userLifecyclePolicyCount = userLifecyclePolicyCount;
  }
  
  
  public int getUserLifecyclePolicyPartCount() {
    return userLifecyclePolicyPartCount;
  }


  
  public void setUserLifecyclePolicyPartCount(int userLifecyclePolicyPartCount) {
    this.userLifecyclePolicyPartCount = userLifecyclePolicyPartCount;
  }


  /**
   * @return true if can operate on user lifecycle
   */
  public boolean isCanOperateOnUserLifecycleConfigs() {
    
    Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    if (PrivilegeHelper.isWheelOrRoot(loggedInSubject)) {
      return true;
    }
    return false;
  }


  
  
  public GuiUserLifecycleEventConfiguration getGuiUserLifecycleEventConfiguration() {
    return guiUserLifecycleEventConfiguration;
  }


  
  public void setGuiUserLifecycleEventConfiguration(
      GuiUserLifecycleEventConfiguration guiUserLifecycleEventConfiguration) {
    this.guiUserLifecycleEventConfiguration = guiUserLifecycleEventConfiguration;
  }


  
  public List<GuiUserLifecycleEventConfiguration> getGuiUserLifecycleEventConfigurations() {
    return guiUserLifecycleEventConfigurations;
  }


  
  public void setGuiUserLifecycleEventConfigurations(
      List<GuiUserLifecycleEventConfiguration> guiUserLifecycleEventConfigurations) {
    this.guiUserLifecycleEventConfigurations = guiUserLifecycleEventConfigurations;
  }


  public List<UserLifecycleEventConfiguration> getAllUserLifecycleEventTypes() {
    return Arrays.asList(new UserLifecycleEventConfiguration());
  }
  
  public List<UserLifecycleActionConfiguration> getAllUserLifecycleActionTypes() {
    return Arrays.asList(new UserLifecycleActionConfiguration());
  }
  
  public List<UserLifecyclePolicyConfiguration> getAllUserLifecyclePolicyTypes() {
    return Arrays.asList(new UserLifecyclePolicyConfiguration());
  }

  public List<UserLifecyclePolicyPartConfiguration> getAllUserLifecyclePolicyPartTypes() {
    return Arrays.asList(new UserLifecyclePolicyPartConfiguration());
  }
  
  public GuiUserLifecycleActionConfiguration getGuiUserLifecycleActionConfiguration() {
    return guiUserLifecycleActionConfiguration;
  }


  
  public void setGuiUserLifecycleActionConfiguration(
      GuiUserLifecycleActionConfiguration guiUserLifecycleActionConfiguration) {
    this.guiUserLifecycleActionConfiguration = guiUserLifecycleActionConfiguration;
  }


  
  public List<GuiUserLifecycleActionConfiguration> getGuiUserLifecycleActionConfigurations() {
    return guiUserLifecycleActionConfigurations;
  }


  
  public void setGuiUserLifecycleActionConfigurations(
      List<GuiUserLifecycleActionConfiguration> guiUserLifecycleActionConfigurations) {
    this.guiUserLifecycleActionConfigurations = guiUserLifecycleActionConfigurations;
  }


  
  public GuiUserLifecyclePolicyConfiguration getGuiUserLifecyclePolicyConfiguration() {
    return guiUserLifecyclePolicyConfiguration;
  }


  
  public void setGuiUserLifecyclePolicyConfiguration(
      GuiUserLifecyclePolicyConfiguration guiUserLifecyclePolicyConfiguration) {
    this.guiUserLifecyclePolicyConfiguration = guiUserLifecyclePolicyConfiguration;
  }


  
  public List<GuiUserLifecyclePolicyConfiguration> getGuiUserLifecyclePolicyConfigurations() {
    return guiUserLifecyclePolicyConfigurations;
  }


  
  public void setGuiUserLifecyclePolicyConfigurations(
      List<GuiUserLifecyclePolicyConfiguration> guiUserLifecyclePolicyConfigurations) {
    this.guiUserLifecyclePolicyConfigurations = guiUserLifecyclePolicyConfigurations;
  }


  
  public GuiUserLifecyclePolicyPartConfiguration getGuiUserLifecyclePolicyPartConfiguration() {
    return guiUserLifecyclePolicyPartConfiguration;
  }


  
  public void setGuiUserLifecyclePolicyPartConfiguration(
      GuiUserLifecyclePolicyPartConfiguration guiUserLifecyclePolicyPartConfiguration) {
    this.guiUserLifecyclePolicyPartConfiguration = guiUserLifecyclePolicyPartConfiguration;
  }


  public List<GuiUserLifecyclePolicyPartConfiguration> getGuiUserLifecyclePolicyPartConfigurations() {
    return guiUserLifecyclePolicyPartConfigurations;
  }

  
  public void setGuiUserLifecyclePolicyPartConfigurations(
      List<GuiUserLifecyclePolicyPartConfiguration> guiUserLifecyclePolicyPartConfigurations) {
    this.guiUserLifecyclePolicyPartConfigurations = guiUserLifecyclePolicyPartConfigurations;
  }
  

}
