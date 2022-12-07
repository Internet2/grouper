package edu.internet2.middleware.grouper.dataField;

import edu.internet2.middleware.grouper.Member;

public class GrouperDataFieldAssignWrapper {

  
  
  public GrouperDataFieldAssignWrapper() {
  }


  public GrouperDataFieldAssignWrapper(GrouperDataEngine grouperDataEngine,
      GrouperDataFieldAssign grouperDataFieldAssign) {
    this.grouperDataEngine = grouperDataEngine;
    this.grouperDataFieldAssign = grouperDataFieldAssign;
  }

  private GrouperDataEngine grouperDataEngine;
  
  
  
  
  public GrouperDataEngine getGrouperDataEngine() {
    return grouperDataEngine;
  }

  
  public void setGrouperDataEngine(GrouperDataEngine grouperDataEngine) {
    this.grouperDataEngine = grouperDataEngine;
  }

  private GrouperDataMemberWrapper memberWrapper;
  
  public GrouperDataMemberWrapper getMemberWrapper() {
    return memberWrapper;
  }
  
  public void setMemberWrapper(GrouperDataMemberWrapper memberWrapper) {
    this.memberWrapper = memberWrapper;
  }

  private GrouperDataFieldAssign grouperDataFieldAssign;

  
  public GrouperDataFieldAssign getGrouperDataFieldAssign() {
    return grouperDataFieldAssign;
  }

  
  public void setGrouperDataFieldAssign(GrouperDataFieldAssign grouperDataFieldAssign) {
    this.grouperDataFieldAssign = grouperDataFieldAssign;
  }
  
  private GrouperDataFieldWrapper grouperDataFieldWrapper;


  
  public GrouperDataFieldWrapper getGrouperDataFieldWrapper() {
    return grouperDataFieldWrapper;
  }


  
  public void setGrouperDataFieldWrapper(GrouperDataFieldWrapper grouperDataFieldWrapper) {
    this.grouperDataFieldWrapper = grouperDataFieldWrapper;
  }
  
  private String textValue;


  
  public String getTextValue() {
    return textValue;
  }


  
  public void setTextValue(String textValue) {
    this.textValue = textValue;
  }
  
  
  
}
