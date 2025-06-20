package edu.internet2.middleware.grouper.dataField;

import edu.internet2.middleware.grouper.cfg.GrouperConfig;

/**
 * 
 * @author mchyzer
 *
 */
public class GrouperDataProviderConfig {

  /**
   * 
   */
  public GrouperDataProviderConfig() {
  }

  /**
   * 
   * @param configId
   */
  public void readFromConfig(String configId) {
    
    this.configId = configId;
    
    //  # data provider name, not really needed or used, but there to setup the provider
    //  # {valueType: "string", required: true, multiple: true, regex: "^grouperDataProvider\\.[^.]+\\.name$"}
    //  # grouperDataProvider.dataProviderConfigId.name = 

    this.name = GrouperConfig.retrieveConfig().propertyValueString("grouperDataProvider." + configId + ".name");
    
    this.subjectSource = GrouperConfig.retrieveConfig().propertyValueBoolean("grouperDataProvider." + configId + ".subjectSource", false);
    this.subjectSourceId = GrouperConfig.retrieveConfig().propertyValueString("grouperDataProvider." + configId + ".subjectSourceId");
  }
  
  private String configId;
  
  
  public String getConfigId() {
    return configId;
  }

  
  public void setConfigId(String configId) {
    this.configId = configId;
  }

  /**
   * data provider name, not really needed or used, but there to setup the provider
   * {valueType: "string", required: true, multiple: true, regex: "^grouperDataProvider\\.[^.]+\\.name$"}
   * grouperDataProvider.dataProviderConfigId.name = 
   */
  private String name;


  /**
   * data provider name, not really needed or used, but there to setup the provider
   * {valueType: "string", required: true, multiple: true, regex: "^grouperDataProvider\\.[^.]+\\.name$"}
   * grouperDataProvider.dataProviderConfigId.name = 
   * @return
   */
  public String getName() {
    return name;
  }

  /**
   * data provider name, not really needed or used, but there to setup the provider
   * {valueType: "string", required: true, multiple: true, regex: "^grouperDataProvider\\.[^.]+\\.name$"}
   * grouperDataProvider.dataProviderConfigId.name = 
   * @param name
   */
  public void setName(String name) {
    this.name = name;
  }
  
  private boolean subjectSource;


  
  public boolean isSubjectSource() {
    return subjectSource;
  }

  
  public void setSubjectSource(boolean subjectSource) {
    this.subjectSource = subjectSource;
  }
  
  
  private String subjectSourceId;


  
  public String getSubjectSourceId() {
    return subjectSourceId;
  }

  
  public void setSubjectSourceId(String subjectSourceId) {
    this.subjectSourceId = subjectSourceId;
  }
}
