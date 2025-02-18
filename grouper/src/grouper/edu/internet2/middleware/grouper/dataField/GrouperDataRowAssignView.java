package edu.internet2.middleware.grouper.dataField;


public class GrouperDataRowAssignView {
  
  private String dataRowConfigId;
  
  private String dataFieldConfigId;
  
  private Long valueInteger;
  
  private String valueText;
  
  
  public GrouperDataRowAssignView(String dataRowConfigId, String dataFieldConfigId, String valueText, Long valueInteger) {
    super();
    this.dataRowConfigId = dataRowConfigId;
    this.dataFieldConfigId = dataFieldConfigId;
    this.valueText = valueText;
    this.valueInteger = valueInteger;
  }
  
  public String getDataRowConfigId() {
    return dataRowConfigId;
  }
  
  public void setDataRowConfigId(String dataRowConfigId) {
    this.dataRowConfigId = dataRowConfigId;
  }
  
  public String getDataFieldConfigId() {
    return dataFieldConfigId;
  }

  public void setDataFieldConfigId(String dataFieldConfigId) {
    this.dataFieldConfigId = dataFieldConfigId;
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
