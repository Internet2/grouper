package edu.internet2.middleware.grouper.dataField;


public class GrouperDataRowAssignView {

  private String dataRowConfigId;

  private String dataFieldConfigId;

  private Long valueInteger;

  private String valueText;

  /**
   * internal id of the underlying data row assign (grouper_data_row_assign.internal_id).
   * This identifies which physical row a field value belongs to.  It must be carried
   * through so that field values can be grouped by their actual row instead of by
   * position, otherwise a field that is null on one row and populated on another
   * misaligns when rendered.
   */
  private Long dataRowAssignInternalId;


  public GrouperDataRowAssignView(String dataRowConfigId, String dataFieldConfigId, String valueText, Long valueInteger,
      Long dataRowAssignInternalId) {
    super();
    this.dataRowConfigId = dataRowConfigId;
    this.dataFieldConfigId = dataFieldConfigId;
    this.valueText = valueText;
    this.valueInteger = valueInteger;
    this.dataRowAssignInternalId = dataRowAssignInternalId;
  }

  public Long getDataRowAssignInternalId() {
    return dataRowAssignInternalId;
  }

  public void setDataRowAssignInternalId(Long dataRowAssignInternalId) {
    this.dataRowAssignInternalId = dataRowAssignInternalId;
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
