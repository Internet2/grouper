package edu.internet2.middleware.grouper.dataField;


public class GrouperDataFieldAssignView {
  
  private String configId;
  
  private Long valueInteger;
  
  private String valueText;
  
  public GrouperDataFieldAssignView(String configId, Long valueInteger, String valueText) {
    this.configId = configId;
    this.valueInteger = valueInteger;
    this.valueText = valueText;
  }


  public String getConfigId() {
    return configId;
  }

  
  public void setConfigId(String configId) {
    this.configId = configId;
  }

  
  public Long getValueInteger() {
    return valueInteger;
  }

  
  public void setValueInteger(Long valueInteger) {
    this.valueInteger = valueInteger;
  }

  
  public String getValueText() {
    return valueText;
  }

  
  public void setValueText(String valueText) {
    this.valueText = valueText;
  }
  
  

}
