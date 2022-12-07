package edu.internet2.middleware.grouper.dataField;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.internet2.middleware.grouper.Member;

public class GrouperDataRowAssignWrapper {
  
  public GrouperDataRowAssignWrapper(GrouperDataEngine grouperDataEngine, GrouperDataRowAssign grouperDataRowAssign) {
    this.grouperDataEngine = grouperDataEngine;
    this.grouperDataRowAssign = grouperDataRowAssign;
  }

  /**
   * for a user, get the row field assign wrappers by field internal id
   */
  private Map<Long, List<GrouperDataRowFieldAssignWrapper>> rowFieldAssignWrappersByFieldInternalId = new HashMap<>();

  
  public Map<Long, List<GrouperDataRowFieldAssignWrapper>> getRowFieldAssignWrappersByFieldInternalId() {
    return rowFieldAssignWrappersByFieldInternalId;
  }


  public GrouperDataRowAssignWrapper() {
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
