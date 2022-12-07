package edu.internet2.middleware.grouper.dataField;

import edu.internet2.middleware.grouper.Member;

public class GrouperDataRowFieldAssignWrapper {

  public GrouperDataRowFieldAssignWrapper(GrouperDataEngine grouperDataEngine,
      GrouperDataRowFieldAssign grouperDataRowFieldAssign) {
    this.grouperDataEngine = grouperDataEngine;
    this.grouperDataRowFieldAssign = grouperDataRowFieldAssign;
  }


  private GrouperDataEngine grouperDataEngine;
  
  
  
  
  public GrouperDataEngine getGrouperDataEngine() {
    return grouperDataEngine;
  }

  
  public void setGrouperDataEngine(GrouperDataEngine grouperDataEngine) {
    this.grouperDataEngine = grouperDataEngine;
  }

  public GrouperDataRowFieldAssignWrapper() {
  }


  private GrouperDataFieldWrapper grouperDataFieldWrapper;
  private GrouperDataRowFieldAssign grouperDataRowFieldAssign;
  private GrouperDataRowAssignWrapper grouperDataRowAssignWrapper;
  private String textValue;

  public GrouperDataFieldWrapper getGrouperDataFieldWrapper() {
    return grouperDataFieldWrapper;
  }

  public String getTextValue() {
    return textValue;
  }

  public void setGrouperDataFieldWrapper(GrouperDataFieldWrapper grouperDataFieldWrapper) {
    this.grouperDataFieldWrapper = grouperDataFieldWrapper;
  }

  public void setTextValue(String textValue) {
    this.textValue = textValue;
  }

  
  public GrouperDataRowFieldAssign getGrouperDataRowFieldAssign() {
    return grouperDataRowFieldAssign;
  }

  
  public void setGrouperDataRowFieldAssign(
      GrouperDataRowFieldAssign grouperDataRowFieldAssign) {
    this.grouperDataRowFieldAssign = grouperDataRowFieldAssign;
  }

  
  public GrouperDataRowAssignWrapper getGrouperDataRowAssignWrapper() {
    return grouperDataRowAssignWrapper;
  }

  
  public void setGrouperDataRowAssignWrapper(
      GrouperDataRowAssignWrapper grouperDataRowAssignWrapper) {
    this.grouperDataRowAssignWrapper = grouperDataRowAssignWrapper;
  }

  
  
}
