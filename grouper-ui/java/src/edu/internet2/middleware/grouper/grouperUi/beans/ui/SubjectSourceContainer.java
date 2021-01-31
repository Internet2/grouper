package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import java.util.ArrayList;
import java.util.List;

import edu.internet2.middleware.grouper.app.subectSource.SubjectSourceConfiguration;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.subject.Source;
import edu.internet2.middleware.subject.Subject;

public class SubjectSourceContainer {
  
  private List<Source> sources = new ArrayList<>();
  
  /**
   * subject source id that is being edited
   */
  private String subjectSourceId;
  
  /**
   * subject source configuration user is currently viewing/editing/adding
   */
  private GuiSubjectSourceConfiguration guiSubjectSourceConfiguration;
  
  /**
   * all configured subject source configurations
   */
  private List<GuiSubjectSourceConfiguration> guiSubjectSourceConfigurations = new ArrayList<GuiSubjectSourceConfiguration>();

  
  /**
   * current grouped config index we are looping through
   */
  private int index;
  
  
  public List<Source> getSources() {
    return sources;
  }

  
  public void setSources(List<Source> sources) {
    this.sources = sources;
  }

  /**
   * @return true if can view subject sources
   */
  public boolean isCanViewSubjectSources() {
    
    Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    return PrivilegeHelper.isWheelOrRoot(loggedInSubject);
  }
  
  public List<SubjectSourceConfiguration> getAllSubjectSourceConfigurationTypes() {
    return SubjectSourceConfiguration.retrieveAllSubjectSourceConfigurationTypes();
  }


  
  public GuiSubjectSourceConfiguration getGuiSubjectSourceConfiguration() {
    return guiSubjectSourceConfiguration;
  }


  
  public void setGuiSubjectSourceConfiguration(
      GuiSubjectSourceConfiguration guiSubjectSourceConfiguration) {
    this.guiSubjectSourceConfiguration = guiSubjectSourceConfiguration;
  }


  
  public List<GuiSubjectSourceConfiguration> getGuiSubjectSourceConfigurations() {
    return guiSubjectSourceConfigurations;
  }


  
  public void setGuiSubjectSourceConfigurations(
      List<GuiSubjectSourceConfiguration> guiSubjectSourceConfigurations) {
    this.guiSubjectSourceConfigurations = guiSubjectSourceConfigurations;
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


  
  public String getSubjectSourceId() {
    return subjectSourceId;
  }


  
  public void setSubjectSourceId(String subjectSourceId) {
    this.subjectSourceId = subjectSourceId;
  }
  
  
  
}
