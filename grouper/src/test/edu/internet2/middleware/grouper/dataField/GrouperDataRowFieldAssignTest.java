package edu.internet2.middleware.grouper.dataField;

import java.util.List;

import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import junit.textui.TestRunner;


public class GrouperDataRowFieldAssignTest extends GrouperTest {

  public GrouperDataRowFieldAssignTest(String name) {
    super(name);
  }

  public static void main(String[] args) {
    TestRunner.run(new GrouperDataRowFieldAssignTest("testInsert"));
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


    Long dataFieldInternalId = GrouperDataFieldDao.findOrAdd("testField");

    GrouperDataRowFieldAssign grouperDataRowFieldAssign = new GrouperDataRowFieldAssign();

    grouperDataRowFieldAssign.setDataFieldInternalId(dataFieldInternalId);
    grouperDataRowFieldAssign.setDataRowAssignInternalId(grouperDataRowAssign.getInternalId());

    GrouperDataRowFieldAssignDao.store(grouperDataRowFieldAssign);

    List<GrouperDataRowFieldAssign> grouperDataRowFieldAssigns = GrouperDataRowFieldAssignDao.selectByMarker(grouperDataRowAssign.getInternalId(), dataFieldInternalId);
    
    assertEquals(1, GrouperUtil.length(grouperDataRowFieldAssigns));
    
  }

}
