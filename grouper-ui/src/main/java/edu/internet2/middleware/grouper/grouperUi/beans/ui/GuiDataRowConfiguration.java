package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import java.util.ArrayList;
import java.util.List;

import edu.internet2.middleware.grouper.dataField.GrouperDataRowConfiguration;

public class GuiDataRowConfiguration {
  
  private GrouperDataRowConfiguration grouperDataRowConfiguration;
  
  public GrouperDataRowConfiguration getGrouperDataRowConfiguration() {
    return grouperDataRowConfiguration;
  }
  
  private GuiDataRowConfiguration(GrouperDataRowConfiguration grouperDataRowConfiguration) {
    this.grouperDataRowConfiguration = grouperDataRowConfiguration;
  }

  public static GuiDataRowConfiguration convertFromDataRowConfiguration(GrouperDataRowConfiguration grouperDataRowConfiguration) {
    return new GuiDataRowConfiguration(grouperDataRowConfiguration);
  }
  
  public static List<GuiDataRowConfiguration> convertFromDataRowConfiguration(List<GrouperDataRowConfiguration> dataRowConfigurations) {
    
    List<GuiDataRowConfiguration> guiDataRowConfigs = new ArrayList<GuiDataRowConfiguration>();
    
    for (GrouperDataRowConfiguration grouperDataRowConfiguration: dataRowConfigurations) {
      guiDataRowConfigs.add(convertFromDataRowConfiguration(grouperDataRowConfiguration));
    }
    
    return guiDataRowConfigs;
    
  }

}
