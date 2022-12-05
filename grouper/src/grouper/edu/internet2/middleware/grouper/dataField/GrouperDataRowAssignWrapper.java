package edu.internet2.middleware.grouper.dataField;

import edu.internet2.middleware.grouper.Member;

public class GrouperDataRowAssignWrapper {
  
  public GrouperDataRowAssignWrapper(GrouperDataRowAssign grouperDataRowAssign) {
    this.grouperDataRowAssign = grouperDataRowAssign;
  }

  public GrouperDataRowAssignWrapper() {
  }

  private Member member;
  
  
  public Member getMember() {
    return member;
  }


  
  public void setMember(Member member) {
    this.member = member;
  }

  private GrouperDataRowAssign grouperDataRowAssign;

  
  public GrouperDataRowAssign getGrouperDataRowAssign() {
    return grouperDataRowAssign;
  }

  
  public void setGrouperDataRowAssign(GrouperDataRowAssign grouperDataRowAssign) {
    this.grouperDataRowAssign = grouperDataRowAssign;
  }
  
  private GrouperDataRowWrapper grouperDataRowWrapper;


  
  public GrouperDataRowWrapper getGrouperDataRowWrapper() {
    return grouperDataRowWrapper;
  }


  
  public void setGrouperDataRowWrapper(GrouperDataRowWrapper grouperDataRowWrapper) {
    this.grouperDataRowWrapper = grouperDataRowWrapper;
  }

}
