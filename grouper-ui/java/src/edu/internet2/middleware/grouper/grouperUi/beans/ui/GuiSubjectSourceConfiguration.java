package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import java.util.ArrayList;
import java.util.List;

import edu.internet2.middleware.grouper.app.subectSource.SubjectSourceConfiguration;

public class GuiSubjectSourceConfiguration {
  
  private SubjectSourceConfiguration subjectSourceConfiguration;
  
  public SubjectSourceConfiguration getSubjectSourceConfiguration() {
    return subjectSourceConfiguration;
  }

  
  public void setSubjectSourceConfiguration(SubjectSourceConfiguration subjectSourceConfiguration) {
    this.subjectSourceConfiguration = subjectSourceConfiguration;
  }
  
  
  /**
   * convert from subject source configuration to gui subject source configuration
   * @param subjectSourceConfiguration
   * @return
   */
  public static GuiSubjectSourceConfiguration convertFromSubjectSourceConfiguration(SubjectSourceConfiguration subjectSourceConfiguration) {
    
    GuiSubjectSourceConfiguration guiSubjectSourceConfiguration = new GuiSubjectSourceConfiguration();
    guiSubjectSourceConfiguration.subjectSourceConfiguration = subjectSourceConfiguration;
    return guiSubjectSourceConfiguration;
  }
  
  /**
   * convert from list of subject source configurations to gui subject source configurations
   * @param subjectSourceConfigurations
   * @return
   */
  public static List<GuiSubjectSourceConfiguration> convertFromSubjectSourceConfiguration(List<SubjectSourceConfiguration> subjectSourceConfigurations) {
    
    List<GuiSubjectSourceConfiguration> guiSubjectSourceConfigurations = new ArrayList<GuiSubjectSourceConfiguration>();
    
    for (SubjectSourceConfiguration subjectSourceConfiguration: subjectSourceConfigurations) {
      guiSubjectSourceConfigurations.add(convertFromSubjectSourceConfiguration(subjectSourceConfiguration));
    }
    
    return guiSubjectSourceConfigurations;
    
  }
  
}
