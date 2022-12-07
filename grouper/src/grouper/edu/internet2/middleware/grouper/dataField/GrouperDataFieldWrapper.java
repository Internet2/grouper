package edu.internet2.middleware.grouper.dataField;


public class GrouperDataFieldWrapper {
  
  public GrouperDataFieldWrapper() {
    super();
  }

  public GrouperDataFieldWrapper(GrouperDataEngine grouperDataEngine, GrouperDataField grouperDataField) {
    this.grouperDataEngine = grouperDataEngine;
    this.grouperDataField = grouperDataField;
  }

  private GrouperDataEngine grouperDataEngine;
  
  

  
  public GrouperDataEngine getGrouperDataEngine() {
    return grouperDataEngine;
  }

  
  public void setGrouperDataEngine(GrouperDataEngine grouperDataEngine) {
    this.grouperDataEngine = grouperDataEngine;
  }

  private GrouperDataField grouperDataField;

  
  public GrouperDataField getGrouperDataField() {
    return grouperDataField;
  }

  
  public void setGrouperDataField(GrouperDataField grouperDataField) {
    this.grouperDataField = grouperDataField;
  }
  
}
