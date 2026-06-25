package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import java.util.ArrayList;
import java.util.List;

import edu.internet2.middleware.grouper.dataField.GrouperDataProviderQueryConfiguration;

public class GuiDataProviderQueryConfiguration {
  
  private GrouperDataProviderQueryConfiguration grouperDataProviderQueryConfiguration;
  
  public GrouperDataProviderQueryConfiguration getGrouperDataProviderQueryConfiguration() {
    return grouperDataProviderQueryConfiguration;
  }

  /**
   * query type, e.g. sql or ldap
   * @return the query type
   */
  public String getQueryType() {
    return this.grouperDataProviderQueryConfiguration.retrieveAttributeValueFromConfig("providerQueryType", false);
  }

  /**
   * query data structure, e.g. attribute or row
   * @return the query data structure
   */
  public String getQueryDataStructure() {
    return this.grouperDataProviderQueryConfiguration.retrieveAttributeValueFromConfig("providerQueryDataStructure", false);
  }

  /**
   * number of data fields configured in this query
   * @return the number of data fields
   */
  public String getNumberOfDataFields() {
    return this.grouperDataProviderQueryConfiguration.retrieveAttributeValueFromConfig("providerQueryNumberOfDataFields", false);
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
