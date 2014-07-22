/**
 * Copyright 2014 Internet2
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
 * @author mchyzer
 * $Id: ChangeLogIdTest.java,v 1.4 2009-06-02 12:33:36 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.changeLog;

import java.util.HashSet;
import java.util.Set;

import org.hibernate.criterion.Restrictions;

import junit.textui.TestRunner;

import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;



/**
 *
 */
public class ChangeLogIdTest extends GrouperTest {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new ChangeLogIdTest("testHibernate"));
  }
  
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
  
  /**
   * 
   */
  public void testHibernate() {
    ChangeLogEntry changeLogEntryTemp = new ChangeLogEntry();
    changeLogEntryTemp.setChangeLogTypeId(ChangeLogTypeBuiltin.GROUP_ADD.getChangeLogType().getId());
    changeLogEntryTemp.setContextId("abc");
    changeLogEntryTemp.setString01("string1");
    changeLogEntryTemp.setString02("string2");
    changeLogEntryTemp.setSequenceNumber(1l);
    
    changeLogEntryTemp.save();
    
    ChangeLogEntry changeLogEntry = HibernateSession.byHqlStatic().createQuery(
        "from ChangeLogEntryTemp where string01 = 'string1'").uniqueResult(ChangeLogEntry.class);
    
    assertEquals("string1", changeLogEntry.getString01());

    //put this in the Change log table, and delete from change log temp
    changeLogEntry.setTempObject(false);
    changeLogEntry.save();
    HibernateSession.byObjectStatic().setEntityName("ChangeLogEntryTemp").delete(changeLogEntry);
    
    //select from change log
    
    changeLogEntry = HibernateSession.byHqlStatic().createQuery(
        "from ChangeLogEntryEntity where string01 = 'string1'").uniqueResult(ChangeLogEntry.class);
    
    assertEquals("string1", changeLogEntry.getString01());
    
    changeLogEntry = HibernateSession.byCriteriaStatic().setEntityName("ChangeLogEntryEntity").uniqueResult(
        ChangeLogEntry.class, Restrictions.eq("string01", "string1"));

    assertEquals("string1", changeLogEntry.getString01());

    //shouldnt be in temp anymore
    changeLogEntry = HibernateSession.byHqlStatic().createQuery(
      "from ChangeLogEntryTemp where string01 = 'string1'").uniqueResult(ChangeLogEntry.class);

    assertNull(changeLogEntry);
  }
  
}
