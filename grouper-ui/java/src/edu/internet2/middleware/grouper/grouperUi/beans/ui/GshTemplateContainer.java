package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateConfiguration;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateExec;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.subject.Subject;

public class GshTemplateContainer {
  
  /**
   * exec that is running
   */
  private GshTemplateExec gshTemplateExec;
  
  /**
   * exec that is running
   * @return
   */
  public GshTemplateExec getGshTemplateExec() {
    return gshTemplateExec;
  }

  /**
   * exec that is running
   * @param gshTemplateExec
   */
  public void setGshTemplateExec(GshTemplateExec gshTemplateExec) {
    this.gshTemplateExec = gshTemplateExec;
  }

  /**
   * ID ties the run of this to a user
   */
  private String uniqueId;
  
  
  
  /**
   * ID ties the run of this to a user
   * @return
   */
  public String getUniqueId() {
    return uniqueId;
  }

  /**
   * ID ties the run of this to a user
   * @param uniqueId
   */
  public void setUniqueId(String uniqueId) {
    this.uniqueId = uniqueId;
  }


  /**
   * gsh template config user is currently viewing/editing/adding
   */
  private GuiGshTemplateConfiguration guiGshTemplateConfiguration;
  
  /**
   * all gsh template configs
   */
  private List<GuiGshTemplateConfiguration> guiGshTemplateConfigurations = new ArrayList<GuiGshTemplateConfiguration>();
  
  
  /**
   * current grouped config index we are looping through
   */
  private int index;
  
  
  public GuiGshTemplateConfiguration getGuiGshTemplateConfiguration() {
    return guiGshTemplateConfiguration;
  }

  public void setGuiGshTemplateConfiguration(GuiGshTemplateConfiguration guiGshTemplateConfiguration) {
    this.guiGshTemplateConfiguration = guiGshTemplateConfiguration;
  }

  
  public List<GuiGshTemplateConfiguration> getGuiGshTemplateConfigurations() {
    return guiGshTemplateConfigurations;
  }

  
  public void setGuiGshTemplateConfigurations(List<GuiGshTemplateConfiguration> guiGshTemplateConfigurations) {
    this.guiGshTemplateConfigurations = guiGshTemplateConfigurations;
  }

  /**
   * @return true if can view gsh templates
   */
  public boolean isCanViewGshTemplates() {
    
    Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    if (PrivilegeHelper.isWheelOrRoot(loggedInSubject)) {
      return true;
    }
    return false;
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
  
  public List<GshTemplateConfiguration> getAllGshTemplateTypes() {
    return Arrays.asList(new GshTemplateConfiguration());
  }
  
}
