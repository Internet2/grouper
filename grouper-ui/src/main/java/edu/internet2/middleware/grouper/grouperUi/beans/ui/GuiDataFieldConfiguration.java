package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import java.util.ArrayList;
import java.util.List;

import edu.internet2.middleware.grouper.dataField.GrouperDataFieldConfiguration;

public class GuiDataFieldConfiguration {
  
  private GrouperDataFieldConfiguration grouperDataFieldConfiguration;
  
  public GrouperDataFieldConfiguration getGrouperDataFieldConfiguration() {
    return grouperDataFieldConfiguration;
  }
  
  private GuiDataFieldConfiguration(GrouperDataFieldConfiguration grouperDataFieldConfiguration) {
    this.grouperDataFieldConfiguration = grouperDataFieldConfiguration;
  }
  
  public static GuiDataFieldConfiguration convertFromDataFieldConfiguration(GrouperDataFieldConfiguration dataFieldConfiguration) {
    return new GuiDataFieldConfiguration(dataFieldConfiguration);
  }
  
  public static List<GuiDataFieldConfiguration> convertFromDataFieldConfiguration(List<GrouperDataFieldConfiguration> dataFieldConfigurations) {
    
    List<GuiDataFieldConfiguration> guiDataFieldConfigs = new ArrayList<GuiDataFieldConfiguration>();
    
    for (GrouperDataFieldConfiguration dataFieldConfiguration: dataFieldConfigurations) {
      guiDataFieldConfigs.add(convertFromDataFieldConfiguration(dataFieldConfiguration));
    }
    
    return guiDataFieldConfigs;
    
  }

}
