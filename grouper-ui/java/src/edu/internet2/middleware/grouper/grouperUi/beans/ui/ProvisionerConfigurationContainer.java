package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import java.util.ArrayList;
import java.util.List;

import edu.internet2.middleware.grouper.app.provisioning.ProvisionerConfiguration;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.subject.Subject;

public class ProvisionerConfigurationContainer {
  
  /**
   * provisioner configuration user is currently viewing/editing/adding
   */
  private GuiProvisionerConfiguration guiProvisionerConfiguration;
  
  /**
   * all configured provisioning configurations
   */
  private List<GuiProvisionerConfiguration> guiProvisionerConfigurations = new ArrayList<GuiProvisionerConfiguration>();
  
  /**
   * 
   * @return all configured provisioning configurations
   */
  public List<GuiProvisionerConfiguration> getGuiProvisionerConfigurations() {
    return guiProvisionerConfigurations;
  }

  /**
   * all configured provisioning configurations
   * @param guiProvisionerConfigurations
   */
  public void setGuiProvisionerConfigurations(List<GuiProvisionerConfiguration> guiProvisionerConfigurations) {
    this.guiProvisionerConfigurations = guiProvisionerConfigurations;
  }
  

  /**
   * @return provisioner configuration user is currently viewing/editing/adding
   */
  public GuiProvisionerConfiguration getGuiProvisionerConfiguration() {
    return guiProvisionerConfiguration;
  }

  /**
   * provisioner configuration user is currently viewing/editing/adding
   * @param guiProvisionerConfiguration
   */
  public void setGuiProvisionerConfiguration(GuiProvisionerConfiguration guiProvisionerConfiguration) {
    this.guiProvisionerConfiguration = guiProvisionerConfiguration;
  }

  /**
   * @return true if can view provisioner configurations
   */
  public boolean isCanViewProvisionerConfiguration() {
    
    Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    if (PrivilegeHelper.isWheelOrRoot(loggedInSubject)) {
      return true;
    }
    return false;
  }
  
  public List<ProvisionerConfiguration> getAllProvisionerConfigurationTypes() {
    return ProvisionerConfiguration.retrieveAllProvisionerConfigurationTypes();
  }

}
