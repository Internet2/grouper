package edu.internet2.middleware.grouper.dataField;


public class GrouperDataFieldWrapper {
  
  public GrouperDataFieldWrapper() {
    super();
  }

  public GrouperDataFieldWrapper(GrouperDataField grouperDataField) {
    this.grouperDataField = grouperDataField;
  }


  private GrouperDataField grouperDataField;

  
  public GrouperDataField getGrouperDataField() {
    return grouperDataField;
  }

  
  public void setGrouperDataField(GrouperDataField grouperDataField) {
    this.grouperDataField = grouperDataField;
  }
  
}
