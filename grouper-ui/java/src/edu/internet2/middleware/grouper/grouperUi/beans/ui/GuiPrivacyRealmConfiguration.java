package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.dataField.GrouperPrivacyRealmConfiguration;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiGroup;

public class GuiPrivacyRealmConfiguration {
  
  private GrouperPrivacyRealmConfiguration grouperPrivacyRealmConfiguration;
  
  public GrouperPrivacyRealmConfiguration getGrouperPrivacyRealmConfiguration() {
    return grouperPrivacyRealmConfiguration;
  }

  public String getPrivacyRealmPublic() {
    String value = this.grouperPrivacyRealmConfiguration.retrieveAttributeValueFromConfig("privacyRealmPublic", false);
    return StringUtils.isBlank(value) ? "false" : value;
  }

  public String getPrivacyRealmViewersGroupName() {
    return this.grouperPrivacyRealmConfiguration.retrieveAttributeValueFromConfig("privacyRealmViewersGroupName", false);
  }

  public String getPrivacyRealmReadersGroupName() {
    return this.grouperPrivacyRealmConfiguration.retrieveAttributeValueFromConfig("privacyRealmReadersGroupName", false);
  }

  public String getPrivacyRealmUpdatersGroupName() {
    return this.grouperPrivacyRealmConfiguration.retrieveAttributeValueFromConfig("privacyRealmUpdatersGroupName", false);
  }

  private GuiGroup viewersGuiGroup;
  private boolean viewersGuiGroupResolved = false;

  private GuiGroup readersGuiGroup;
  private boolean readersGuiGroupResolved = false;

  private GuiGroup updatersGuiGroup;
  private boolean updatersGuiGroupResolved = false;

  private GuiGroup buildGuiGroup(String groupName) {
    if (StringUtils.isBlank(groupName)) {
      return null;
    }
    Group group = GroupFinder.findByName(groupName, false);
    if (group == null) {
      return null;
    }
    return new GuiGroup(group);
  }

  public GuiGroup getPrivacyRealmViewersGuiGroup() {
    if (!this.viewersGuiGroupResolved) {
      this.viewersGuiGroup = buildGuiGroup(getPrivacyRealmViewersGroupName());
      this.viewersGuiGroupResolved = true;
    }
    return this.viewersGuiGroup;
  }

  public GuiGroup getPrivacyRealmReadersGuiGroup() {
    if (!this.readersGuiGroupResolved) {
      this.readersGuiGroup = buildGuiGroup(getPrivacyRealmReadersGroupName());
      this.readersGuiGroupResolved = true;
    }
    return this.readersGuiGroup;
  }

  public GuiGroup getPrivacyRealmUpdatersGuiGroup() {
    if (!this.updatersGuiGroupResolved) {
      this.updatersGuiGroup = buildGuiGroup(getPrivacyRealmUpdatersGroupName());
      this.updatersGuiGroupResolved = true;
    }
    return this.updatersGuiGroup;
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
