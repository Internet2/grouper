package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import edu.internet2.middleware.grouper.attr.resolver.GlobalAttributeResolverConfiguration;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.subject.Subject;

public class GlobalAttributeResolverConfigContainer {
  
  private List<GuiGlobalAttributeResolverConfiguration> guiGlobalAttributeResolverConfigs = new ArrayList<GuiGlobalAttributeResolverConfiguration>();
  
  
  /**
   * gui global attribute resolver config user is currently viewing/editing/adding
   */
  private GuiGlobalAttributeResolverConfiguration guiGlobalAttributeResolverConfiguration;
  
  /**
   * current grouped config index we are looping through
   */
  private int index;
  
  /**
   * @return true if can view global attribute resolver configs
   */
  public boolean isCanViewGlobalAttributeResolverConfig() {
    
    Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    if (PrivilegeHelper.isWheelOrRoot(loggedInSubject)) {
      return true;
    }
    return false;
  }
  
  
  public List<GuiGlobalAttributeResolverConfiguration> getGuiGlobalAttributeResolverConfigs() {
    return guiGlobalAttributeResolverConfigs;
  }

  
  public void setGuiGlobalAttributeResolverConfigs(List<GuiGlobalAttributeResolverConfiguration> guiGlobalAttributeResolverConfigs) {
    this.guiGlobalAttributeResolverConfigs = guiGlobalAttributeResolverConfigs;
  }


  public GuiGlobalAttributeResolverConfiguration getGuiGlobalAttributeResolverConfiguration() {
    return guiGlobalAttributeResolverConfiguration;
  }


  
  public void setGuiGlobalAttributeResolverConfiguration(GuiGlobalAttributeResolverConfiguration guiGlobalAttributeResolverConfiguration) {
    this.guiGlobalAttributeResolverConfiguration = guiGlobalAttributeResolverConfiguration;
  }


  public List<GlobalAttributeResolverConfiguration> getAllGlobalAttributeResolverConfigTypes() {
    return Arrays.asList(new GlobalAttributeResolverConfiguration());
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
