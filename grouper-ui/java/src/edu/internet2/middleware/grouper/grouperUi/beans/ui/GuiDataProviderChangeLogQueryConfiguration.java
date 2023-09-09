package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import java.util.ArrayList;
import java.util.List;

import edu.internet2.middleware.grouper.dataField.GrouperDataProviderChangeLogQueryConfiguration;

public class GuiDataProviderChangeLogQueryConfiguration {
  
  private GrouperDataProviderChangeLogQueryConfiguration grouperDataProviderChangeLogQueryConfiguration;
  
  public GrouperDataProviderChangeLogQueryConfiguration getGrouperDataProviderChangeLogQueryConfiguration() {
    return grouperDataProviderChangeLogQueryConfiguration;
  }
  
  private GuiDataProviderChangeLogQueryConfiguration(GrouperDataProviderChangeLogQueryConfiguration grouperDataProviderChangeLogQueryConfiguration) {
    this.grouperDataProviderChangeLogQueryConfiguration = grouperDataProviderChangeLogQueryConfiguration;
  }
  
  public static GuiDataProviderChangeLogQueryConfiguration convertFromDataProviderChangeLogQueryConfiguration(GrouperDataProviderChangeLogQueryConfiguration grouperDataProviderChangeLogQueryConfiguration) {
    return new GuiDataProviderChangeLogQueryConfiguration(grouperDataProviderChangeLogQueryConfiguration);
  }
  
  public static List<GuiDataProviderChangeLogQueryConfiguration> convertFromDataProviderChangeLogQueryConfiguration(List<GrouperDataProviderChangeLogQueryConfiguration> dataProviderChangeLogQueryConfigurations) {
    
    List<GuiDataProviderChangeLogQueryConfiguration> guiDataProviderChangeLogQueryConfigs = new ArrayList<GuiDataProviderChangeLogQueryConfiguration>();
    
    for (GrouperDataProviderChangeLogQueryConfiguration grouperDataProviderConfiguration: dataProviderChangeLogQueryConfigurations) {
      guiDataProviderChangeLogQueryConfigs.add(convertFromDataProviderChangeLogQueryConfiguration(grouperDataProviderConfiguration));
    }
    
    return guiDataProviderChangeLogQueryConfigs;
    
  }

}
