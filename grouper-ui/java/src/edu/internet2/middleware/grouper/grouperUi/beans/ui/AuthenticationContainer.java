package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import edu.internet2.middleware.grouper.authentication.WsTrustedJwtConfiguration;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.subject.Subject;

public class AuthenticationContainer {
  
  
  private List<GuiWsTrustedJwtConfiguration> guiWsTrustedJwtConfigs = new ArrayList<GuiWsTrustedJwtConfiguration>();
  
  
  /**
   * gui ws trusted config user is currently viewing/editing/adding
   */
  private GuiWsTrustedJwtConfiguration guiWsTrustedJwtConfiguration;
  
  /**
   * current grouped config index we are looping through
   */
  private int index;
  
  /**
   * @return true if can view external systems
   */
  public boolean isCanViewAuthentication() {
    
    Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    if (PrivilegeHelper.isWheelOrRoot(loggedInSubject)) {
      return true;
    }
    return false;
  }
  
  public List<GuiWsTrustedJwtConfiguration> getGuiWsTrustedJwtConfigs() {
    return guiWsTrustedJwtConfigs;
  }

  
  public void setGuiWsTrustedJwtConfigs(List<GuiWsTrustedJwtConfiguration> guiWsTrustedJwtConfigs) {
    this.guiWsTrustedJwtConfigs = guiWsTrustedJwtConfigs;
  }

  
  public GuiWsTrustedJwtConfiguration getGuiWsTrustedJwtConfiguration() {
    return guiWsTrustedJwtConfiguration;
  }

  
  public void setGuiWsTrustedJwtConfiguration(GuiWsTrustedJwtConfiguration guiWsTrustedJwtConfiguration) {
    this.guiWsTrustedJwtConfiguration = guiWsTrustedJwtConfiguration;
  }
  
  
  public List<WsTrustedJwtConfiguration> getAllWsTrustedJwtConfigTypes() {
    return Arrays.asList(new WsTrustedJwtConfiguration());
  }
  
  public int getIndex() {
    return index;
  }

  
  public void setIndex(int index) {
    this.index = index;
  }
  
  private String currentConfigSuffix;
  
  public String getCurrentConfigSuffix() {
    return currentConfigSuffix;
  }

  
  public void setCurrentConfigSuffix(String currentConfigSuffix) {
    this.currentConfigSuffix = currentConfigSuffix;
  }

}
