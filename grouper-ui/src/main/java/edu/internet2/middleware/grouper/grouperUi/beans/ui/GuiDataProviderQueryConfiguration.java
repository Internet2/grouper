package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import java.util.ArrayList;
import java.util.List;

import edu.internet2.middleware.grouper.dataField.GrouperDataProviderQueryConfiguration;

public class GuiDataProviderQueryConfiguration {
  
  private GrouperDataProviderQueryConfiguration grouperDataProviderQueryConfiguration;
  
  public GrouperDataProviderQueryConfiguration getGrouperDataProviderQueryConfiguration() {
    return grouperDataProviderQueryConfiguration;
  }
  
  private GuiDataProviderQueryConfiguration(GrouperDataProviderQueryConfiguration grouperDataProviderQueryConfiguration) {
    this.grouperDataProviderQueryConfiguration = grouperDataProviderQueryConfiguration;
  }
  
  public static GuiDataProviderQueryConfiguration convertFromDataProviderQueryConfiguration(GrouperDataProviderQueryConfiguration grouperDataProviderQueryConfiguration) {
    return new GuiDataProviderQueryConfiguration(grouperDataProviderQueryConfiguration);
  }
  
  public static List<GuiDataProviderQueryConfiguration> convertFromDataProviderQueryConfiguration(List<GrouperDataProviderQueryConfiguration> dataProviderQueryConfigurations) {
    
    List<GuiDataProviderQueryConfiguration> guiDataProviderQueryConfigs = new ArrayList<GuiDataProviderQueryConfiguration>();
    
    for (GrouperDataProviderQueryConfiguration grouperDataProviderConfiguration: dataProviderQueryConfigurations) {
      guiDataProviderQueryConfigs.add(convertFromDataProviderQueryConfiguration(grouperDataProviderConfiguration));
    }
    
    return guiDataProviderQueryConfigs;
    
  }

}
