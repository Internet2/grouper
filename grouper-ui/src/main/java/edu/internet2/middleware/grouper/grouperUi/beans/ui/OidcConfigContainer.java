package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import edu.internet2.middleware.grouper.app.oidc.OidcConfiguration;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.subject.Subject;

public class OidcConfigContainer {
  
  private List<GuiOidcConfiguration> guiOidcConfigs = new ArrayList<GuiOidcConfiguration>();
  
  /**
   * gui oidc config user is currently viewing/editing/adding
   */
  private GuiOidcConfiguration guiOidcConfiguration;
  
  /**
   * current grouped config index we are looping through
   */
  private int index;
  
  /**
   * @return true if can view oidc config
   */
  public boolean isCanViewOidcConfig() {
    
    Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    if (PrivilegeHelper.isWheelOrRoot(loggedInSubject)) {
      return true;
    }
    return false;
  }
  
  
  public List<GuiOidcConfiguration> getGuiOidcConfigs() {
    return guiOidcConfigs;
  }

  
  public void setGuiOidcConfigs(List<GuiOidcConfiguration> guiOidcConfigs) {
    this.guiOidcConfigs = guiOidcConfigs;
  }


  
  public GuiOidcConfiguration getGuiOidcConfiguration() {
    return guiOidcConfiguration;
  }

  public void setGuiOidcConfiguration(GuiOidcConfiguration guiOidcConfiguration) {
    this.guiOidcConfiguration = guiOidcConfiguration;
  }


  public List<OidcConfiguration> getAllOidcConfigTypes() {
    return Arrays.asList(new OidcConfiguration());
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
