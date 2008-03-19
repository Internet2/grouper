/*
 * @author mchyzer
 * $Id: GrouperUtilTest.java,v 1.2 2008-03-19 20:43:24 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.util;

import java.util.List;

import junit.framework.TestCase;


/**
 *
 */
public class GrouperUtilTest extends TestCase {
  
  /**
   * 
   * @param args
   * @throws Exception
   */
  public static void main(String[] args) throws Exception {
    //TestRunner.run(new TestGroup0("testStaticSaveGroupTransactions"));
    //TestRunner.run(TestGroup0.class);
    //runPerfProblem();
  }
  /**
   * @param name
   */
  public GrouperUtilTest(String name) {
    super(name);
  }

  /**
   * 
   */
  public void testBatching() {
    List<Integer> list = GrouperUtil.toList(0, 1, 2, 3, 4);
    assertEquals("3 batches, 0 and 1, 2 and 3, and 4", 3, GrouperUtil.batchNumberOfBatches(list, 2));
    List<Integer> listBatch = GrouperUtil.batchList(list, 2, 0);
    assertEquals(2, listBatch.size());
    assertEquals(0, (int)listBatch.get(0));
    assertEquals(1, (int)listBatch.get(1));
    
    listBatch = GrouperUtil.batchList(list, 2, 1);
    assertEquals(2, listBatch.size());
    assertEquals(2, (int)listBatch.get(0));
    assertEquals(3, (int)listBatch.get(1));
    
    listBatch = GrouperUtil.batchList(list, 2, 2);
    assertEquals(1, listBatch.size());
    assertEquals(4, (int)listBatch.get(0));
  }
  
}
