package edu.internet2.middleware.grouper.dataField;

import java.util.List;

import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import junit.textui.TestRunner;


public class GrouperDataRowAssignTest extends GrouperTest {

  public GrouperDataRowAssignTest(String name) {
    super(name);
  }

  public static void main(String[] args) {
    TestRunner.run(new GrouperDataRowAssignTest("testInsert"));
  }

  /**
   * 
   */
  public void testInsert() {
    Long loaderConfigId = GrouperDataProviderDao.findOrAdd("loaderConfig");
    Long dataRowInternalId = GrouperDataRowDao.findOrAdd("testRow");
    Member member = MemberFinder.internal_findRootMember();

    GrouperDataRowAssign grouperDataRowAssign = new GrouperDataRowAssign();

    grouperDataRowAssign.setDataProviderInternalId(loaderConfigId);
    grouperDataRowAssign.setDataRowInternalId(dataRowInternalId);
    grouperDataRowAssign.setMemberInternalId(member.getInternalId());

    GrouperDataRowAssignDao.store(grouperDataRowAssign);

    List<GrouperDataRowAssign> grouperDataRowAssigns = GrouperDataRowAssignDao.selectByMemberAndRow(member.getInternalId(), dataRowInternalId);
    
    assertEquals(1, GrouperUtil.length(grouperDataRowAssigns));
    
  }

}
