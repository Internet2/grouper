package edu.internet2.middleware.grouper.dataField;

import edu.internet2.middleware.grouper.Member;

public class GrouperDataRowFieldAssignWrapper {

  public GrouperDataRowFieldAssignWrapper(
      GrouperDataRowFieldAssign grouperDataRowFieldAssign) {
    this.grouperDataRowFieldAssign = grouperDataRowFieldAssign;
  }


  public GrouperDataRowFieldAssignWrapper() {
  }


  private GrouperDataFieldWrapper grouperDataFieldWrapper;
  private GrouperDataRowFieldAssign grouperDataRowFieldAssign;
  private GrouperDataRowAssignWrapper grouperDataRowAssignWrapper;
  private Member member;
  private String textValue;

  public GrouperDataFieldWrapper getGrouperDataFieldWrapper() {
    return grouperDataFieldWrapper;
  }

  public Member getMember() {
    return member;
  }

  public String getTextValue() {
    return textValue;
  }

  public void setGrouperDataFieldWrapper(GrouperDataFieldWrapper grouperDataFieldWrapper) {
    this.grouperDataFieldWrapper = grouperDataFieldWrapper;
  }

  public void setMember(Member member) {
    this.member = member;
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
