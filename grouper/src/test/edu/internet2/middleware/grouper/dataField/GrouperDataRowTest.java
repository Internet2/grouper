package edu.internet2.middleware.grouper.dataField;

import edu.internet2.middleware.grouper.helper.GrouperTest;
import junit.textui.TestRunner;


public class GrouperDataRowTest extends GrouperTest {

  public GrouperDataRowTest(String name) {
    super(name);
  }

  public static void main(String[] args) {
    TestRunner.run(new GrouperDataRowTest("testInsert"));
  }

  /**
   * 
   */
  public void testInsert() {
    Long internalId = GrouperDataRowDao.findOrAdd("test");
    GrouperDataRow grouperDataRow = GrouperDataRowDao.selectByConfigId("test");
    assertEquals("test", grouperDataRow.getConfigId());
    assertEquals(internalId.longValue(), grouperDataRow.getInternalId());

  }

}
