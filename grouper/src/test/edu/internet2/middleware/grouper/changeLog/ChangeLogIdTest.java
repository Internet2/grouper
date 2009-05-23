/*
 * @author mchyzer
 * $Id: ChangeLogIdTest.java,v 1.1 2009-05-23 06:00:29 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.changeLog;

import java.util.HashSet;
import java.util.Set;

import edu.internet2.middleware.grouper.helper.GrouperTest;



/**
 *
 */
public class ChangeLogIdTest extends GrouperTest {

  /**
   * 
   * @param name
   */
  public ChangeLogIdTest(String name) {
    super(name);
  }

  /**
   * 
   * @throws InterruptedException
   */
  public void testStress() throws InterruptedException {
    final long[][] numbers = new long[20][10000];
    Thread[] threads = new Thread[20];
    
    for (int i=0;i<20;i++) {
      final int threadIndex = i;
      threads[i] = new Thread(new Runnable() {

        public void run() {
          for (int j=0;j<10000;j++) {
            long changeLogId = ChangeLogId.changeLogId();
            numbers[threadIndex][j] = changeLogId;
          }
        }
      });
      threads[i].start();
    }
    
    for (int i=0;i<20;i++) {
      threads[i].join();
    }
    
    //collate
    Set<Long> allIds = new HashSet<Long>();
    for (int i=0;i<20;i++) {
      for (int j=0;j<10000;j++) {
        long theId = numbers[i][j];
        if (allIds.contains(theId)) {
          throw new RuntimeException("Already contains " + theId);
        }
        allIds.add(theId);
      }
    }
    //at this point we should have 20*10000 ids
    assertEquals(20*10000, allIds.size());

  }
  
}
