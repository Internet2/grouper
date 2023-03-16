package edu.internet2.middleware.grouper.dataField;

import java.util.ArrayList;
import java.util.List;

public class GrouperDataProviderData {



  private GrouperDataEngine grouperDataEngine;
  
  
  
  
  public GrouperDataEngine getGrouperDataEngine() {
    return grouperDataEngine;
  }



  
  public void setGrouperDataEngine(GrouperDataEngine grouperDataEngine) {
    this.grouperDataEngine = grouperDataEngine;
  }



  private List<GrouperDataField> grouperDataFields = null;
  
  
  private List<GrouperDataRow> grouperDataRows = null;

  
  public List<GrouperDataField> getGrouperDataFields() {
    return grouperDataFields;
  }


  
  public void setGrouperDataFields(List<GrouperDataField> grouperDataFields) {
    this.grouperDataFields = grouperDataFields;
  }


  
  public List<GrouperDataRow> getGrouperDataRows() {
    return grouperDataRows;
  }


  
  public void setGrouperDataRows(List<GrouperDataRow> grouperDataRows) {
    this.grouperDataRows = grouperDataRows;
  }
  
  

}
