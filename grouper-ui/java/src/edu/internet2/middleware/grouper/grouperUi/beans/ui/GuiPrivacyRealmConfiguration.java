package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import java.util.ArrayList;
import java.util.List;

import edu.internet2.middleware.grouper.dataField.GrouperPrivacyRealmConfiguration;

public class GuiPrivacyRealmConfiguration {
  
  private GrouperPrivacyRealmConfiguration grouperPrivacyRealmConfiguration;
  
  public GrouperPrivacyRealmConfiguration getGrouperPrivacyRealmConfiguration() {
    return grouperPrivacyRealmConfiguration;
  }
  
  private GuiPrivacyRealmConfiguration(GrouperPrivacyRealmConfiguration grouperPrivacyRealmConfiguration) {
    this.grouperPrivacyRealmConfiguration = grouperPrivacyRealmConfiguration;
  }
  
  public static GuiPrivacyRealmConfiguration convertFromPrivacyRealmConfiguration(GrouperPrivacyRealmConfiguration grouperPrivacyRealmConfiguration) {
    return new GuiPrivacyRealmConfiguration(grouperPrivacyRealmConfiguration);
  }
  
  public static List<GuiPrivacyRealmConfiguration> convertFromPrivacyRealmConfiguration(List<GrouperPrivacyRealmConfiguration> privacyRealmConfigurations) {
    
    List<GuiPrivacyRealmConfiguration> guiPrivacyRealmConfigs = new ArrayList<GuiPrivacyRealmConfiguration>();
    
    for (GrouperPrivacyRealmConfiguration grouperPrivacyRealmConfiguration: privacyRealmConfigurations) {
      guiPrivacyRealmConfigs.add(convertFromPrivacyRealmConfiguration(grouperPrivacyRealmConfiguration));
    }
    
    return guiPrivacyRealmConfigs;
    
  }

}
