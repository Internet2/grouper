/**
 * Copyright 2020 Internet2
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
package edu.internet2.middleware.grouper.membership;

import java.sql.Timestamp;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.app.loader.GrouperLoader;
import edu.internet2.middleware.grouper.changeLog.ChangeLogTempToEntity;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.hibernate.AuditControl;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.hibernate.HibernateHandler;
import edu.internet2.middleware.grouper.hibernate.HibernateHandlerBean;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.misc.CompositeType;
import edu.internet2.middleware.grouper.misc.FindBadMemberships;
import junit.textui.TestRunner;

/**
 * @author shilen
 */
public class TestFindBadMembershipsChangeLogConsumer extends GrouperTest {

  public static void main(String[] args) throws Exception {
    TestRunner.run(new TestFindBadMembershipsChangeLogConsumer("testMissingIntersectionCompositeDisabledGroup"));
  }
  
  /**
   * @param name
   */
  public TestFindBadMembershipsChangeLogConsumer(String name) {
    super(name);
  }

  /**
   * 
   */
  public void testMissingIntersectionComposite() throws Exception {
    Stem rootStem = StemFinder.findRootStem(GrouperSession.staticGrouperSession());
    Stem testStem = rootStem.addChildStem("testStem", "testStem");
    Group ownerGroup = testStem.addChildGroup("ownerGroup", "ownerGroup");
    Group ownerGroup2 = testStem.addChildGroup("ownerGroup2", "ownerGroup2");
    Group leftGroup = testStem.addChildGroup("leftGroup", "leftGroup");
    Group rightGroup = testStem.addChildGroup("rightGroup", "rightGroup");
    Group bogusGroup = testStem.addChildGroup("bogusGroup", "bogusGroup");
    
    Group parentOwnerGroup = testStem.addChildGroup("parentOwnerGroup", "parentOwnerGroup");
    Group parentLeftGroup = testStem.addChildGroup("parentLeftGroup", "parentLeftGroup");
    
    ownerGroup.addCompositeMember(CompositeType.INTERSECTION, leftGroup, rightGroup);
    ownerGroup2.addCompositeMember(CompositeType.INTERSECTION, leftGroup, rightGroup);
    parentOwnerGroup.addCompositeMember(CompositeType.INTERSECTION, parentLeftGroup, ownerGroup);
    parentLeftGroup.addMember(SubjectTestHelper.SUBJ0);
    parentLeftGroup.addMember(SubjectTestHelper.SUBJ1);
    parentLeftGroup.addMember(SubjectTestHelper.SUBJ2);
    
    bogusGroup.addMember(SubjectTestHelper.SUBJ3);  // to get a record in grouper_members
    
    // clear change log
    ChangeLogTempToEntity.convertRecords();
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_consumer_findBadMemberships", false);
    
    final String[] state = new String[1];
    state[0] = "1";

    // perform these operations in parallel so it messes up the memberships
    Thread thread1 = new Thread(new Runnable() {

      public void run() {
        HibernateSession.callbackHibernateSession(
            GrouperTransactionType.READ_WRITE_NEW, AuditControl.WILL_NOT_AUDIT, new HibernateHandler() {

          public Object callback(HibernateHandlerBean hibernateHandlerBean)
              throws GrouperDAOException {

            GrouperSession.startRootSession();
            leftGroup.addMember(SubjectTestHelper.SUBJ0);
            leftGroup.addMember(SubjectTestHelper.SUBJ1);
            leftGroup.addMember(SubjectTestHelper.SUBJ2);
            leftGroup.addMember(SubjectTestHelper.SUBJ3);
            
            state[0] = "2";
            
            while (true) {
              if (state[0].equals("2")) {
                try {
                  Thread.sleep(100);
                } catch (InterruptedException e) {
                  // ignore
                }
              } else {
                break;
              }
            }
            return null;
          }
        });
      }
    });
    
    thread1.start();
    
    Thread thread2 = new Thread(new Runnable() {

      public void run() {
        HibernateSession.callbackHibernateSession(
            GrouperTransactionType.READ_WRITE_NEW, AuditControl.WILL_NOT_AUDIT, new HibernateHandler() {

          public Object callback(HibernateHandlerBean hibernateHandlerBean)
              throws GrouperDAOException {

            GrouperSession.startRootSession();
            
            while (true) {
              if (state[0].equals("1")) {
                try {
                  Thread.sleep(100);
                } catch (InterruptedException e) {
                  // ignore
                }
              } else {
                break;
              }
            }
            
            rightGroup.addMember(SubjectTestHelper.SUBJ0);
            rightGroup.addMember(SubjectTestHelper.SUBJ1);
            rightGroup.addMember(SubjectTestHelper.SUBJ2);
            rightGroup.addMember(SubjectTestHelper.SUBJ3);
            state[0] = "3";

            return null;
          }
        });
      }
    });
    
    thread2.start();

    thread1.join();
    thread2.join();
    
    // confirm issue
    assertEquals(8, FindBadMemberships.checkAll());  // subj0-3 for ownerGroup and ownerGroup2
    
    ChangeLogTempToEntity.convertRecords();
    
    // still issue
    assertEquals(8, FindBadMemberships.checkAll());  // subj0-3 for ownerGroup and ownerGroup2
    
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_consumer_findBadMemberships", false);

    // now parent group still needs to be fixed
    assertEquals(3, FindBadMemberships.checkAll());  // subj0-2 for parentOwnerGroup
    
    ChangeLogTempToEntity.convertRecords();
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_consumer_findBadMemberships", false);

    // now good
    assertEquals(0, FindBadMemberships.checkAll());
  }
  
  /**
   * 
   */
  public void testExtraIntersectionComposite() throws Exception {
    Stem rootStem = StemFinder.findRootStem(GrouperSession.staticGrouperSession());
    Stem testStem = rootStem.addChildStem("testStem", "testStem");
    Group ownerGroup = testStem.addChildGroup("ownerGroup", "ownerGroup");
    Group ownerGroup2 = testStem.addChildGroup("ownerGroup2", "ownerGroup2");
    Group leftGroup = testStem.addChildGroup("leftGroup", "leftGroup");
    Group rightGroup = testStem.addChildGroup("rightGroup", "rightGroup");
    
    Group parentOwnerGroup = testStem.addChildGroup("parentOwnerGroup", "parentOwnerGroup");
    Group parentLeftGroup = testStem.addChildGroup("parentLeftGroup", "parentLeftGroup");
    
    ownerGroup.addCompositeMember(CompositeType.INTERSECTION, leftGroup, rightGroup);
    ownerGroup2.addCompositeMember(CompositeType.INTERSECTION, leftGroup, rightGroup);
    parentOwnerGroup.addCompositeMember(CompositeType.INTERSECTION, parentLeftGroup, ownerGroup);
    parentLeftGroup.addMember(SubjectTestHelper.SUBJ0);
    parentLeftGroup.addMember(SubjectTestHelper.SUBJ1);
    parentLeftGroup.addMember(SubjectTestHelper.SUBJ2);
    
    rightGroup.addMember(SubjectTestHelper.SUBJ0);
    rightGroup.addMember(SubjectTestHelper.SUBJ1);
    rightGroup.addMember(SubjectTestHelper.SUBJ2);
    rightGroup.addMember(SubjectTestHelper.SUBJ3);
    
    // clear change log
    ChangeLogTempToEntity.convertRecords();
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_consumer_findBadMemberships", false);
    
    final String[] state = new String[1];
    state[0] = "1";

    // perform these operations in parallel so it messes up the memberships
    Thread thread1 = new Thread(new Runnable() {

      public void run() {
        HibernateSession.callbackHibernateSession(
            GrouperTransactionType.READ_WRITE_NEW, AuditControl.WILL_NOT_AUDIT, new HibernateHandler() {

          public Object callback(HibernateHandlerBean hibernateHandlerBean)
              throws GrouperDAOException {

            GrouperSession.startRootSession();
            leftGroup.addMember(SubjectTestHelper.SUBJ0);
            leftGroup.addMember(SubjectTestHelper.SUBJ1);
            leftGroup.addMember(SubjectTestHelper.SUBJ2);
            leftGroup.addMember(SubjectTestHelper.SUBJ3);
            
            state[0] = "2";
            
            while (true) {
              if (state[0].equals("2")) {
                try {
                  Thread.sleep(100);
                } catch (InterruptedException e) {
                  // ignore
                }
              } else {
                break;
              }
            }
            return null;
          }
        });
      }
    });
    
    thread1.start();
    
    Thread thread2 = new Thread(new Runnable() {

      public void run() {
        HibernateSession.callbackHibernateSession(
            GrouperTransactionType.READ_WRITE_NEW, AuditControl.WILL_NOT_AUDIT, new HibernateHandler() {

          public Object callback(HibernateHandlerBean hibernateHandlerBean)
              throws GrouperDAOException {

            GrouperSession.startRootSession();
            
            while (true) {
              if (state[0].equals("1")) {
                try {
                  Thread.sleep(100);
                } catch (InterruptedException e) {
                  // ignore
                }
              } else {
                break;
              }
            }
            
            rightGroup.deleteMember(SubjectTestHelper.SUBJ0);
            rightGroup.deleteMember(SubjectTestHelper.SUBJ1);
            rightGroup.deleteMember(SubjectTestHelper.SUBJ2);
            rightGroup.deleteMember(SubjectTestHelper.SUBJ3);
            state[0] = "3";

            return null;
          }
        });
      }
    });
    
    thread2.start();

    thread1.join();
    thread2.join();
    
    // confirm issue
    assertEquals(8, FindBadMemberships.checkAll());  // subj0-3 for ownerGroup and ownerGroup2
    
    ChangeLogTempToEntity.convertRecords();
    
    // still issue
    assertEquals(8, FindBadMemberships.checkAll());  // subj0-3 for ownerGroup and ownerGroup2
    
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_consumer_findBadMemberships", false);

    // now parent group still needs to be fixed
    assertEquals(3, FindBadMemberships.checkAll());  // subj0-2 for parentOwnerGroup
    
    ChangeLogTempToEntity.convertRecords();
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_consumer_findBadMemberships", false);

    // now good
    assertEquals(0, FindBadMemberships.checkAll());
  }
  
  /**
   * 
   */
  public void testMissingComplementComposite() throws Exception {
    Stem rootStem = StemFinder.findRootStem(GrouperSession.staticGrouperSession());
    Stem testStem = rootStem.addChildStem("testStem", "testStem");
    Group ownerGroup = testStem.addChildGroup("ownerGroup", "ownerGroup");
    Group ownerGroup2 = testStem.addChildGroup("ownerGroup2", "ownerGroup2");
    Group leftGroup = testStem.addChildGroup("leftGroup", "leftGroup");
    Group rightGroup = testStem.addChildGroup("rightGroup", "rightGroup");
    
    Group parentOwnerGroup = testStem.addChildGroup("parentOwnerGroup", "parentOwnerGroup");
    Group parentLeftGroup = testStem.addChildGroup("parentLeftGroup", "parentLeftGroup");
    
    ownerGroup.addCompositeMember(CompositeType.COMPLEMENT, leftGroup, rightGroup);
    ownerGroup2.addCompositeMember(CompositeType.COMPLEMENT, leftGroup, rightGroup);
    parentOwnerGroup.addCompositeMember(CompositeType.COMPLEMENT, parentLeftGroup, ownerGroup);
    
    // the parent ones will be extra not missing
    parentLeftGroup.addMember(SubjectTestHelper.SUBJ0);
    parentLeftGroup.addMember(SubjectTestHelper.SUBJ1);
    parentLeftGroup.addMember(SubjectTestHelper.SUBJ2);
    
    rightGroup.addMember(SubjectTestHelper.SUBJ0);
    rightGroup.addMember(SubjectTestHelper.SUBJ1);
    rightGroup.addMember(SubjectTestHelper.SUBJ2);
    rightGroup.addMember(SubjectTestHelper.SUBJ3);
    
    // clear change log
    ChangeLogTempToEntity.convertRecords();
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_consumer_findBadMemberships", false);
    
    final String[] state = new String[1];
    state[0] = "1";

    // perform these operations in parallel so it messes up the memberships
    Thread thread1 = new Thread(new Runnable() {

      public void run() {
        HibernateSession.callbackHibernateSession(
            GrouperTransactionType.READ_WRITE_NEW, AuditControl.WILL_NOT_AUDIT, new HibernateHandler() {

          public Object callback(HibernateHandlerBean hibernateHandlerBean)
              throws GrouperDAOException {

            GrouperSession.startRootSession();
            leftGroup.addMember(SubjectTestHelper.SUBJ0);
            leftGroup.addMember(SubjectTestHelper.SUBJ1);
            leftGroup.addMember(SubjectTestHelper.SUBJ2);
            leftGroup.addMember(SubjectTestHelper.SUBJ3);
            
            state[0] = "2";
            
            while (true) {
              if (state[0].equals("2")) {
                try {
                  Thread.sleep(100);
                } catch (InterruptedException e) {
                  // ignore
                }
              } else {
                break;
              }
            }
            return null;
          }
        });
      }
    });
    
    thread1.start();
    
    Thread thread2 = new Thread(new Runnable() {

      public void run() {
        HibernateSession.callbackHibernateSession(
            GrouperTransactionType.READ_WRITE_NEW, AuditControl.WILL_NOT_AUDIT, new HibernateHandler() {

          public Object callback(HibernateHandlerBean hibernateHandlerBean)
              throws GrouperDAOException {

            GrouperSession.startRootSession();
            
            while (true) {
              if (state[0].equals("1")) {
                try {
                  Thread.sleep(100);
                } catch (InterruptedException e) {
                  // ignore
                }
              } else {
                break;
              }
            }
            
            rightGroup.deleteMember(SubjectTestHelper.SUBJ0);
            rightGroup.deleteMember(SubjectTestHelper.SUBJ1);
            rightGroup.deleteMember(SubjectTestHelper.SUBJ2);
            rightGroup.deleteMember(SubjectTestHelper.SUBJ3);
            state[0] = "3";

            return null;
          }
        });
      }
    });
    
    thread2.start();

    thread1.join();
    thread2.join();
    
    // confirm issue
    assertEquals(8, FindBadMemberships.checkAll());  // subj0-3 for ownerGroup and ownerGroup2
    
    ChangeLogTempToEntity.convertRecords();
    
    // still issue
    assertEquals(8, FindBadMemberships.checkAll());  // subj0-3 for ownerGroup and ownerGroup2
    
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_consumer_findBadMemberships", false);

    // now parent group still needs to be fixed
    assertEquals(3, FindBadMemberships.checkAll());  // subj0-2 for parentOwnerGroup
    
    ChangeLogTempToEntity.convertRecords();
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_consumer_findBadMemberships", false);

    // now good
    assertEquals(0, FindBadMemberships.checkAll());
  }
  
  /**
   * 
   */
  public void testExtraComplementComposite() throws Exception {
    Stem rootStem = StemFinder.findRootStem(GrouperSession.staticGrouperSession());
    Stem testStem = rootStem.addChildStem("testStem", "testStem");
    Group ownerGroup = testStem.addChildGroup("ownerGroup", "ownerGroup");
    Group ownerGroup2 = testStem.addChildGroup("ownerGroup2", "ownerGroup2");
    Group leftGroup = testStem.addChildGroup("leftGroup", "leftGroup");
    Group rightGroup = testStem.addChildGroup("rightGroup", "rightGroup");
    Group bogusGroup = testStem.addChildGroup("bogusGroup", "bogusGroup");
    
    Group parentOwnerGroup = testStem.addChildGroup("parentOwnerGroup", "parentOwnerGroup");
    Group parentLeftGroup = testStem.addChildGroup("parentLeftGroup", "parentLeftGroup");
    
    ownerGroup.addCompositeMember(CompositeType.COMPLEMENT, leftGroup, rightGroup);
    ownerGroup2.addCompositeMember(CompositeType.COMPLEMENT, leftGroup, rightGroup);
    parentOwnerGroup.addCompositeMember(CompositeType.COMPLEMENT, parentLeftGroup, ownerGroup);
    
    // the parent ones will be missing not extra
    parentLeftGroup.addMember(SubjectTestHelper.SUBJ0);
    parentLeftGroup.addMember(SubjectTestHelper.SUBJ1);
    parentLeftGroup.addMember(SubjectTestHelper.SUBJ2);
    
    bogusGroup.addMember(SubjectTestHelper.SUBJ3);  // to get a record in grouper_members
    
    // clear change log
    ChangeLogTempToEntity.convertRecords();
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_consumer_findBadMemberships", false);
    
    final String[] state = new String[1];
    state[0] = "1";

    // perform these operations in parallel so it messes up the memberships
    Thread thread1 = new Thread(new Runnable() {

      public void run() {
        HibernateSession.callbackHibernateSession(
            GrouperTransactionType.READ_WRITE_NEW, AuditControl.WILL_NOT_AUDIT, new HibernateHandler() {

          public Object callback(HibernateHandlerBean hibernateHandlerBean)
              throws GrouperDAOException {

            GrouperSession.startRootSession();
            leftGroup.addMember(SubjectTestHelper.SUBJ0);
            leftGroup.addMember(SubjectTestHelper.SUBJ1);
            leftGroup.addMember(SubjectTestHelper.SUBJ2);
            leftGroup.addMember(SubjectTestHelper.SUBJ3);
            
            state[0] = "2";
            
            while (true) {
              if (state[0].equals("2")) {
                try {
                  Thread.sleep(100);
                } catch (InterruptedException e) {
                  // ignore
                }
              } else {
                break;
              }
            }
            return null;
          }
        });
      }
    });
    
    thread1.start();
    
    Thread thread2 = new Thread(new Runnable() {

      public void run() {
        HibernateSession.callbackHibernateSession(
            GrouperTransactionType.READ_WRITE_NEW, AuditControl.WILL_NOT_AUDIT, new HibernateHandler() {

          public Object callback(HibernateHandlerBean hibernateHandlerBean)
              throws GrouperDAOException {

            GrouperSession.startRootSession();
            
            while (true) {
              if (state[0].equals("1")) {
                try {
                  Thread.sleep(100);
                } catch (InterruptedException e) {
                  // ignore
                }
              } else {
                break;
              }
            }
            
            rightGroup.addMember(SubjectTestHelper.SUBJ0);
            rightGroup.addMember(SubjectTestHelper.SUBJ1);
            rightGroup.addMember(SubjectTestHelper.SUBJ2);
            rightGroup.addMember(SubjectTestHelper.SUBJ3);
            state[0] = "3";

            return null;
          }
        });
      }
    });
    
    thread2.start();

    thread1.join();
    thread2.join();
    
    // confirm issue
    assertEquals(8, FindBadMemberships.checkAll());  // subj0-3 for ownerGroup and ownerGroup2
    
    ChangeLogTempToEntity.convertRecords();
    
    // still issue
    assertEquals(8, FindBadMemberships.checkAll());  // subj0-3 for ownerGroup and ownerGroup2
    
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_consumer_findBadMemberships", false);

    // now parent group still needs to be fixed
    assertEquals(3, FindBadMemberships.checkAll());  // subj0-2 for parentOwnerGroup
    
    ChangeLogTempToEntity.convertRecords();
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_consumer_findBadMemberships", false);

    // now good
    assertEquals(0, FindBadMemberships.checkAll());
  }
  
  /**
   * 
   */
  public void testMissingIntersectionCompositeDisabledGroup() throws Exception {
    Stem rootStem = StemFinder.findRootStem(GrouperSession.staticGrouperSession());
    Stem testStem = rootStem.addChildStem("testStem", "testStem");
    Group ownerGroup = testStem.addChildGroup("ownerGroup", "ownerGroup");
    Group ownerGroup2 = testStem.addChildGroup("ownerGroup2", "ownerGroup2");
    Group leftGroup = testStem.addChildGroup("leftGroup", "leftGroup");
    Group rightGroup = testStem.addChildGroup("rightGroup", "rightGroup");
    Group bogusGroup = testStem.addChildGroup("bogusGroup", "bogusGroup");
    
    Group parentOwnerGroup = testStem.addChildGroup("parentOwnerGroup", "parentOwnerGroup");
    Group parentLeftGroup = testStem.addChildGroup("parentLeftGroup", "parentLeftGroup");
    
    ownerGroup.addCompositeMember(CompositeType.INTERSECTION, leftGroup, rightGroup);
    ownerGroup2.addCompositeMember(CompositeType.INTERSECTION, leftGroup, rightGroup);
    parentOwnerGroup.addCompositeMember(CompositeType.INTERSECTION, parentLeftGroup, ownerGroup);
    parentLeftGroup.addMember(SubjectTestHelper.SUBJ0);
    parentLeftGroup.addMember(SubjectTestHelper.SUBJ1);
    parentLeftGroup.addMember(SubjectTestHelper.SUBJ2);
    
    bogusGroup.addMember(SubjectTestHelper.SUBJ3);  // to get a record in grouper_members
    
    // clear change log
    ChangeLogTempToEntity.convertRecords();
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_consumer_findBadMemberships", false);
    
    final String[] state = new String[1];
    state[0] = "1";

    // perform these operations in parallel so it messes up the memberships
    Thread thread1 = new Thread(new Runnable() {

      public void run() {
        HibernateSession.callbackHibernateSession(
            GrouperTransactionType.READ_WRITE_NEW, AuditControl.WILL_NOT_AUDIT, new HibernateHandler() {

          public Object callback(HibernateHandlerBean hibernateHandlerBean)
              throws GrouperDAOException {

            GrouperSession.startRootSession();
            leftGroup.addMember(SubjectTestHelper.SUBJ0);
            leftGroup.addMember(SubjectTestHelper.SUBJ1);
            leftGroup.addMember(SubjectTestHelper.SUBJ2);
            leftGroup.addMember(SubjectTestHelper.SUBJ3);
            
            state[0] = "2";
            
            while (true) {
              if (state[0].equals("2")) {
                try {
                  Thread.sleep(100);
                } catch (InterruptedException e) {
                  // ignore
                }
              } else {
                break;
              }
            }
            return null;
          }
        });
      }
    });
    
    thread1.start();
    
    Thread thread2 = new Thread(new Runnable() {

      public void run() {
        HibernateSession.callbackHibernateSession(
            GrouperTransactionType.READ_WRITE_NEW, AuditControl.WILL_NOT_AUDIT, new HibernateHandler() {

          public Object callback(HibernateHandlerBean hibernateHandlerBean)
              throws GrouperDAOException {

            GrouperSession.startRootSession();
            
            while (true) {
              if (state[0].equals("1")) {
                try {
                  Thread.sleep(100);
                } catch (InterruptedException e) {
                  // ignore
                }
              } else {
                break;
              }
            }
            
            rightGroup.addMember(SubjectTestHelper.SUBJ0);
            rightGroup.addMember(SubjectTestHelper.SUBJ1);
            rightGroup.addMember(SubjectTestHelper.SUBJ2);
            rightGroup.addMember(SubjectTestHelper.SUBJ3);
            state[0] = "3";

            return null;
          }
        });
      }
    });
    
    thread2.start();

    thread1.join();
    thread2.join();
    
    // confirm issue
    assertEquals(8, FindBadMemberships.checkAll());  // subj0-3 for ownerGroup and ownerGroup2
    
    ChangeLogTempToEntity.convertRecords();
    
    // still issue
    assertEquals(8, FindBadMemberships.checkAll());  // subj0-3 for ownerGroup and ownerGroup2
    
    // disable ownerGroup2
    ownerGroup2.setEnabledTime(new Timestamp(System.currentTimeMillis() + 1000000L));
    ownerGroup2.store();
    
    assertEquals(4, FindBadMemberships.checkAll());  // subj0-3 for ownerGroup
    
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_consumer_findBadMemberships", false);

    // now parent group still needs to be fixed
    assertEquals(3, FindBadMemberships.checkAll());  // subj0-2 for parentOwnerGroup
    
    ChangeLogTempToEntity.convertRecords();
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_consumer_findBadMemberships", false);

    // now good
    assertEquals(0, FindBadMemberships.checkAll());
  }
}
