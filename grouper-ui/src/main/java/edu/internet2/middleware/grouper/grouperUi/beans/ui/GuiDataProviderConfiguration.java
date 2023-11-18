package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import java.util.ArrayList;
import java.util.List;

import edu.internet2.middleware.grouper.dataField.GrouperDataProviderConfiguration;

public class GuiDataProviderConfiguration {
  
  private GrouperDataProviderConfiguration grouperDataProviderConfiguration;
  
  public GrouperDataProviderConfiguration getGrouperDataProviderConfiguration() {
    return grouperDataProviderConfiguration;
  }
  
  private GuiDataProviderConfiguration(GrouperDataProviderConfiguration grouperDataProviderConfiguration) {
    this.grouperDataProviderConfiguration = grouperDataProviderConfiguration;
  }
  
  public static GuiDataProviderConfiguration convertFromDataProviderConfiguration(GrouperDataProviderConfiguration grouperDataProviderConfiguration) {
    return new GuiDataProviderConfiguration(grouperDataProviderConfiguration);
  }
  
  public static List<GuiDataProviderConfiguration> convertFromDataProviderConfiguration(List<GrouperDataProviderConfiguration> dataProviderConfigurations) {
    
    List<GuiDataProviderConfiguration> guiDataRowConfigs = new ArrayList<GuiDataProviderConfiguration>();
    
    for (GrouperDataProviderConfiguration grouperDataProviderConfiguration: dataProviderConfigurations) {
      guiDataRowConfigs.add(convertFromDataProviderConfiguration(grouperDataProviderConfiguration));
    }
    
    return guiDataRowConfigs;
    
  }

}
