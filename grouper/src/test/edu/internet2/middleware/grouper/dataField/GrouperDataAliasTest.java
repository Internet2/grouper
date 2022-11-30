package edu.internet2.middleware.grouper.dataField;

import edu.internet2.middleware.grouper.helper.GrouperTest;
import junit.textui.TestRunner;


public class GrouperDataAliasTest extends GrouperTest {

  public GrouperDataAliasTest(String name) {
    super(name);
  }

  public static void main(String[] args) {
    TestRunner.run(new GrouperDataAliasTest("testInsert"));
  }

  /**
   * 
   */
  public void testInsert() {
    Long dataFieldInternalId = GrouperDataFieldDao.findOrAdd("testField");
    Long internalId = GrouperDataAliasDao.findOrAddFieldAlias(dataFieldInternalId, "testAlias");
  }

}
