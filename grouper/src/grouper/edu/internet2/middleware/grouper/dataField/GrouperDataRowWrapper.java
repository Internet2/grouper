package edu.internet2.middleware.grouper.dataField;


public class GrouperDataRowWrapper {
  
  public GrouperDataRowWrapper() {
    super();
  }

  public GrouperDataRowWrapper(GrouperDataRow grouperDataRow) {
    this.grouperDataRow = grouperDataRow;
  }


  private GrouperDataRow grouperDataRow;

  
  public GrouperDataRow getGrouperDataRow() {
    return grouperDataRow;
  }

  
  public void setGrouperDataRow(GrouperDataRow grouperDataRow) {
    this.grouperDataRow = grouperDataRow;
  }
  
}
