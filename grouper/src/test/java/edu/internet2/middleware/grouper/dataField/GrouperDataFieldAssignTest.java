package edu.internet2.middleware.grouper.dataField;

import java.util.List;

import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import junit.textui.TestRunner;


public class GrouperDataFieldAssignTest extends GrouperTest {

  public GrouperDataFieldAssignTest(String name) {
    super(name);
  }

  public static void main(String[] args) {
    TestRunner.run(new GrouperDataFieldAssignTest("testInsert"));
  }

  /**
   * 
   */
  public void testInsert() {
    Long loaderConfigId = GrouperDataProviderDao.findOrAdd("loaderConfig");
    Long dataFieldInternalId = GrouperDataFieldDao.findOrAdd("testField");
    Member member = MemberFinder.internal_findRootMember();

    GrouperDataFieldAssign grouperDataFieldAssign = new GrouperDataFieldAssign();

    grouperDataFieldAssign.setDataProviderInternalId(loaderConfigId);
    grouperDataFieldAssign.setDataFieldInternalId(dataFieldInternalId);
    grouperDataFieldAssign.setMemberInternalId(member.getInternalId());

    GrouperDataFieldAssignDao.store(grouperDataFieldAssign);

    List<GrouperDataFieldAssign> grouperDataFieldAssigns = GrouperDataFieldAssignDao.selectByMarker(member.getInternalId(), dataFieldInternalId);
    
    assertEquals(1, GrouperUtil.length(grouperDataFieldAssigns));
    
  }

}
