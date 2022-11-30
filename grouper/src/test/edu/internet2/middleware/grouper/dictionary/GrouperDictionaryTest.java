package edu.internet2.middleware.grouper.dictionary;

import edu.internet2.middleware.grouper.helper.GrouperTest;
import junit.textui.TestRunner;


public class GrouperDictionaryTest extends GrouperTest {

  protected void setUp() {
    super.setUp();
  }

  protected void tearDown() {
    super.tearDown();
  }

  public GrouperDictionaryTest() {
    super();
  }

  public GrouperDictionaryTest(String name) {
    super(name);
  }

  public static void main(String[] args) {
    TestRunner.run(new GrouperDictionaryTest("testAddRemove"));
  }
  
  public void testAddRemove() {
    Long internalId = GrouperDictionaryDao.findOrAdd("test");
    System.out.println(internalId);
  }
  
}
