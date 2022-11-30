package edu.internet2.middleware.grouper.dataField;

import edu.internet2.middleware.grouper.helper.GrouperTest;
import junit.textui.TestRunner;


public class GrouperDataFieldTest extends GrouperTest {

  public GrouperDataFieldTest(String name) {
    super(name);
  }

  public static void main(String[] args) {
    TestRunner.run(new GrouperDataFieldTest("testInsert"));
  }

  /**
   * 
   */
  public void testInsert() {
    Long internalId = GrouperDataFieldDao.findOrAdd("Test");
    GrouperDataAlias grouperDataAlias = GrouperDataAliasDao.selectByLowerName("test");
    assertEquals((Long)internalId, (Long)grouperDataAlias.getDataFieldInternalId());
  }

}
