package edu.internet2.middleware.grouper.dataField;

import edu.internet2.middleware.grouper.Member;

public class GrouperDataFieldAssignWrapper {

  private Member member;
  
  public Member getMember() {
    return member;
  }
  
  public void setMember(Member member) {
    this.member = member;
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
