/*
 * Copyright (C) 2004-2007 University Corporation for Advanced Internet Development, Inc.
 * Copyright (C) 2004-2007 The University Of Chicago
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under
 * the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package edu.internet2.middleware.grouper;

import java.util.Set;

import junit.framework.Assert;
import junit.textui.TestRunner;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;

import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

import edu.internet2.middleware.grouper.exception.GrantPrivilegeException;
import edu.internet2.middleware.grouper.exception.GroupNotFoundException;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.exception.MemberAddException;
import edu.internet2.middleware.grouper.exception.QueryException;
import edu.internet2.middleware.grouper.exception.StemNotFoundException;
import edu.internet2.middleware.grouper.filter.GroupAttributeFilter;
import edu.internet2.middleware.grouper.filter.GrouperQuery;
import edu.internet2.middleware.grouper.hibernate.GrouperRollbackType;
import edu.internet2.middleware.grouper.hibernate.GrouperTransaction;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionHandler;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.misc.CompositeType;
import edu.internet2.middleware.grouper.misc.SaveMode;
import edu.internet2.middleware.grouper.misc.SaveResultType;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
import edu.internet2.middleware.grouper.registry.RegistryReset;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectNotFoundException;

/**
 * @author  blair christensen.
 * @version $Id: TestGroup0.java,v 1.20 2009-03-15 06:37:22 mchyzer Exp $
 */
public class TestGroup0 extends GrouperTest {

  /** log */
  private static final Log LOG = GrouperUtil.getLog(TestGroup0.class);

  /**
   * Method main.
   * @param args String[]
   * @throws Exception
   */
  public static void main(String[] args) throws Exception {
    TestRunner.run(new TestGroup0("testMemberRead"));
    //TestRunner.run(TestGroup0.class);
    //runPerfProblem();
    //runPerfProblem2();
  }

  /**
   * ctor
   * @param name
   */
  public TestGroup0(String name) {
    super(name);
  }

  /**
   * setup
   */
  protected void setUp() {
    LOG.debug("setUp");
    RegistryReset.reset();
  }

  /**
   * teardown
   */
  protected void tearDown() {
    LOG.debug("tearDown");
  }

  /**
   * test
   */
  public void testAddGroupAsMemberAndThenDeleteAsMember() {
    LOG.info("testAddGroupAsMemberAndThenDeleteAsMember");
    try {
      R r = R.populateRegistry(1, 2, 0);
      Group a = r.getGroup("a", "a");
      Group b = r.getGroup("a", "b");
      Subject bSubj = b.toSubject();
      Assert.assertFalse("a !has b", a.hasMember(bSubj));
      a.addMember(bSubj);
      Assert.assertTrue("a now has b", a.hasMember(bSubj));
      a.deleteMember(bSubj);
      Assert.assertFalse("a no longer has b", a.hasMember(bSubj));
      r.rs.stop();
    } catch (Exception e) {
      T.e(e);
    }
  } // public void testAddGroupAsMemberAndThenDeleteAsMember()

  /**
   * test
   * @throws Exception if problem
   */
  public void testStaticSaveGroupWithDisplayNames() throws Exception {

    GrouperSession rootSession = SessionHelper.getRootSession();

    String groupNameNotExist = "whatever:whatever1:testing123";
    String groupNameSiblingNotExist = "whatever:whatever1:testing1234";
    String groupDisplayNameNotExist = "what ever:what ever1:testing 123";
    String groupDisplayNameChange = "what ever:what ever1234:testing 123";
    String groupDisplayNameSiblingNotExist = "what ever:what ever234:testing 1234";
    String groupDisplayNameSiblingResult = "what ever:what ever1:testing 1234";

    GrouperTest.deleteGroupIfExists(rootSession, groupNameNotExist);

    GroupSave groupSave = new GroupSave(rootSession);

    groupSave.assignGroupNameToEdit(groupNameNotExist);
    groupSave.assignName(groupNameNotExist).assignDisplayName(groupDisplayNameNotExist);
    groupSave.assignCreateParentStemsIfNotExist(true);
    
    //try with display extension which doesnt match
    groupSave.assignDisplayExtension("abc");
    
    Group group = null;
    
    try {
      group = groupSave.save();
      fail("Should have exception");
    } catch (Exception e) {
      //good
    }
    
    groupSave.assignDisplayExtension(null);
    group = groupSave.save();
    
    assertEquals(SaveResultType.INSERT, groupSave.getSaveResultType());
    assertEquals(groupNameNotExist, group.getName());
    assertEquals(groupDisplayNameNotExist, group.getDisplayName());
    
    //####################################
    //try again but change the name
    
    groupSave = new GroupSave(rootSession);

    groupSave.assignGroupNameToEdit(groupNameNotExist);
    groupSave.assignName(groupNameNotExist).assignDisplayName(groupDisplayNameChange);
    groupSave.assignCreateParentStemsIfNotExist(true);
    group = groupSave.save();
    
    assertEquals(SaveResultType.NO_CHANGE, groupSave.getSaveResultType());
    
    assertEquals(groupNameNotExist, group.getName());

    //should be original one
    assertEquals(groupDisplayNameNotExist, group.getDisplayName());
    
    //###################################
    //try again but do a new group in same stem, and the same name shouldnt change
    
    groupSave = new GroupSave(rootSession);

    groupSave.assignGroupNameToEdit(groupNameSiblingNotExist);
    groupSave.assignName(groupNameSiblingNotExist).assignDisplayName(groupDisplayNameSiblingNotExist);
    groupSave.assignCreateParentStemsIfNotExist(true);
    group = groupSave.save();
    
    assertEquals(SaveResultType.INSERT, groupSave.getSaveResultType());
    
    assertEquals(groupNameSiblingNotExist, group.getName());

    //should be original one
    assertEquals(groupDisplayNameSiblingResult, group.getDisplayName());
    
  }
  
  /**
   * @throws Exception 
   * 
   */
  public void testMemberRead() throws Exception {
    R.populateRegistry(1, 2, 1);
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Group group1 = GroupFinder.findByName(grouperSession, "i2:a:a", true);
    Group group2 = GroupFinder.findByName(grouperSession, "i2:a:b", true);
    //System.out.println(group1);
    
    Stem stem = StemFinder.findByName(grouperSession, "i2", true);
    
    
    Subject subject = SubjectFinder.findById("a", true);
    //System.out.println(GrouperUtil.subjectToString(subject));
    
    group1.grantPriv(subject, AccessPrivilege.ADMIN);
    group2.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.READ, false);
    group2.grantPriv(subject, AccessPrivilege.VIEW, false);
    
    stem.grantPriv(subject, NamingPrivilege.CREATE);
    stem.grantPriv(subject, NamingPrivilege.STEM);
    
    grouperSession.stop();
    grouperSession = GrouperSession.start(subject);
    try {
      group1.addMember(SubjectFinder.findById(group2.getUuid(), true));
      fail("Shouldnt be able to add this member without READ priv");
    } catch (MemberAddException e) {
      //this is ok
    }
    try {
      group1.grantPriv(SubjectFinder.findById(group2.getUuid(), true), AccessPrivilege.VIEW);
      fail("Shouldnt be able to grant this member without READ priv");
    } catch (GrantPrivilegeException e) {
      //this is ok
    }
    try {
      stem.grantPriv(SubjectFinder.findById(group2.getUuid(), true), NamingPrivilege.CREATE);
      fail("Shouldnt be able to grant this member without READ priv");
    } catch (GrantPrivilegeException e) {
      //this is ok
    }
    grouperSession.stop();
    grouperSession = GrouperSession.startRootSession();
    group2.grantPriv(subject, AccessPrivilege.READ);
    grouperSession.stop();
    grouperSession = GrouperSession.start(subject);
    
    //this should work now with read priv
    group1.addMember(SubjectFinder.findById(group2.getUuid(), true));
    group1.grantPriv(SubjectFinder.findById(group2.getUuid(), true), AccessPrivilege.VIEW);
    stem.grantPriv(SubjectFinder.findById(group2.getUuid(), true), NamingPrivilege.CREATE);
  }
  
  /**
   * 
   * @throws Exception 
   */
  public void testCompositeMemberRead() throws Exception {
    R.populateRegistry(1, 3, 1);
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Group group1 = GroupFinder.findByName(grouperSession, "i2:a:a", true);
    Group group2 = GroupFinder.findByName(grouperSession, "i2:a:b", true);
    Group group3 = GroupFinder.findByName(grouperSession, "i2:a:c", true);
    //System.out.println(group1);
    
    Subject subject = SubjectFinder.findById("a", true);
    //System.out.println(GrouperUtil.subjectToString(subject));
    
    group1.grantPriv(subject, AccessPrivilege.ADMIN);
    group2.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.READ, false);
    group2.grantPriv(subject, AccessPrivilege.VIEW, false);
    group3.grantPriv(subject, AccessPrivilege.VIEW, false);
    group3.grantPriv(subject, AccessPrivilege.READ, false);

    grouperSession.stop();
    grouperSession = GrouperSession.start(subject);
    try {
      group1.addCompositeMember(CompositeType.UNION,group2, group3);
      fail("Shouldnt be able to add this member without READ priv");
    } catch (InsufficientPrivilegeException e) {
      //this is ok
    }
    grouperSession.stop();
    grouperSession = GrouperSession.startRootSession();
    group2.grantPriv(subject, AccessPrivilege.READ);
    grouperSession.stop();
    grouperSession = GrouperSession.start(subject);
    
    //this should work now with read priv
    group1.addCompositeMember(CompositeType.UNION,group2, group3);
  }
  
  
  /**
   * test
   * @throws Exception if problem
   */
  public void testStaticSaveGroup() throws Exception {

    R.populateRegistry(1, 2, 0);

    String displayExtension = "testing123 display";
    GrouperSession rootSession = SessionHelper.getRootSession();
    String groupDescription = "description";
    try {
      String groupNameNotExist = "whatever:whatever:testing123";

      GrouperTest.deleteGroupIfExists(rootSession, groupNameNotExist);

      Group.saveGroup(rootSession, groupNameNotExist,null, groupNameNotExist, displayExtension, groupDescription, 
          SaveMode.UPDATE, false);
      fail("this should fail, since stem doesnt exist");
    } catch (StemNotFoundException e) {
      //good, caught an exception
      //e.printStackTrace();
    }


    //////////////////////////////////
    //this should insert
    String groupName = "i2:a:testing123";
    GrouperTest.deleteGroupIfExists(rootSession, groupName);
    Group createdGroup = Group.saveGroup(rootSession, null, null, groupName, displayExtension,
        groupDescription, 
        SaveMode.INSERT, false);

    //now retrieve
    Group foundGroup = GroupFinder.findByName(rootSession, groupName, true);

    assertEquals(groupName, createdGroup.getName());
    assertEquals(groupName, foundGroup.getName());

    assertEquals(displayExtension, createdGroup.getDisplayExtension());
    assertEquals(displayExtension, foundGroup.getDisplayExtension());

    assertEquals(groupDescription, createdGroup.getDescription());
    assertEquals(groupDescription, foundGroup.getDescription());

    ///////////////////////////////////
    //this should update by uuid
    createdGroup = Group.saveGroup(rootSession, groupName,createdGroup.getUuid(), groupName, displayExtension,
        groupDescription + "1", 
        SaveMode.INSERT_OR_UPDATE, false);
    assertEquals("this should update by uuid", groupDescription + "1", createdGroup
        .getDescription());

    //this should update by name
    createdGroup = Group.saveGroup(rootSession, groupName, null, groupName,  displayExtension,
        groupDescription + "2", 
        SaveMode.UPDATE, false);
    assertEquals("this should update by name", groupDescription + "2", createdGroup
        .getDescription());

    /////////////////////////////////////
    //create a group that creates a bunch of stems
    String stemsNotExist = "whatever:heythere:another";
    String groupNameCreateStems = stemsNotExist + ":" + groupName;
    GrouperTest.deleteGroupIfExists(rootSession, groupNameCreateStems);
    GrouperTest.deleteAllStemsIfExists(rootSession, stemsNotExist);
    //lets also delete those stems
    createdGroup = Group.saveGroup(rootSession, groupNameCreateStems, null,groupNameCreateStems, 
        displayExtension, groupDescription, 
        SaveMode.INSERT_OR_UPDATE, true);

    assertEquals(groupDescription, createdGroup.getDescription());

    rootSession.stop();

  }

  /**
   * transaction test.  THIS WILL FAIL WITH HIBERNATE2!!!!!
   * @throws Exception if problem
   */
  public void testStaticSaveGroupTransactions() throws Exception {

    //THIS WILL FAIL WITH HIBERNATE2!!!!!
    
    R.populateRegistry(2, 2, 0);

    final GrouperSession rootSession = SessionHelper.getRootSession();
    final String displayExtension = "testing123 display";
    final String groupDescription = "description";

    //######################################################
    //this should insert
    final String groupName = "i2:a:testing123";
    final String groupName2 = "i2:b:testing124";

    GrouperTest.deleteGroupIfExists(rootSession, groupName);
    GrouperTest.deleteGroupIfExists(rootSession, groupName2);

    //this should work
    GrouperTransaction.callbackGrouperTransaction(GrouperTransactionType.READ_WRITE_NEW,
        new GrouperTransactionHandler() {

          public Object callback(GrouperTransaction grouperTransaction)
              throws GrouperDAOException {

            try {
              Group.saveGroup(rootSession, null, null, groupName,displayExtension, groupDescription, 
                  SaveMode.INSERT, false);
            } catch (Exception e) {
              throw new RuntimeException(e);
            }

            return null;
          }

        });

    //now retrieve
    Group foundGroup = GroupFinder.findByName(rootSession, groupName, true);

    assertEquals("Name should be there", groupName, foundGroup.getName());

    GrouperTest.deleteGroupIfExists(rootSession, groupName);
    GrouperTest.deleteGroupIfExists(rootSession, groupName2);

    //####################################################
    //this should work, same as above, two times is a charm.
    GrouperTransaction.callbackGrouperTransaction(GrouperTransactionType.READ_WRITE_NEW,
        new GrouperTransactionHandler() {

          public Object callback(GrouperTransaction grouperTransaction)
              throws GrouperDAOException {

            try {
              Group.saveGroup(rootSession,null, null, groupName, displayExtension, groupDescription + "1", 
                  SaveMode.INSERT, false);
            } catch (Exception e) {
              throw new RuntimeException(e);
            }

            return null;
          }

        });

    //now retrieve
    foundGroup = GroupFinder.findByName(rootSession, groupName, true);

    assertEquals("Name should be there", groupName, foundGroup.getName());
    assertEquals("Description should be new", groupDescription + "1", foundGroup
        .getDescription());

    //##########################################
    //## test committable work in a readonly tx
    GrouperTest.deleteGroupIfExists(rootSession, groupName);
    GrouperTest.deleteGroupIfExists(rootSession, groupName2);

    //this should fail if using transactions (will not work under hib2)
    //not good since READONLY txs should not commit
    try {
      GrouperTransaction.callbackGrouperTransaction(
          GrouperTransactionType.READONLY_OR_USE_EXISTING,
          new GrouperTransactionHandler() {

            public Object callback(GrouperTransaction grouperTransaction)
                throws GrouperDAOException {

              try {
                Group.saveGroup(rootSession,  groupName, null, 
                   groupName, displayExtension,groupDescription, 
                   SaveMode.INSERT, false);
              } catch (Exception e) {
                throw new RuntimeException(e);
              }

              return null;
            }

          });
      fail("not good since READONLY txs should not commit");
    } catch (Exception e) {
      //good, it failed
      String exception = ExceptionUtils.getFullStackTrace(e).toLowerCase();
      assertTrue(
          "Should be readonly commitable problem, or problem with read/write tx inside readonly...: "
              + exception, exception.contains("read") && exception.contains("only"));
    }

    //now retrieve, shouldnt be there
    try {
      GroupFinder.findByName(rootSession, groupName, true);
      fail("Shouldnt find the group");
    } catch (GroupNotFoundException gnfe) {
      //all good
    }

    //###########################################
    //## test rolling back in the middle of a transaction by throwing exception
    GrouperTest.deleteGroupIfExists(rootSession, groupName);
    GrouperTest.deleteGroupIfExists(rootSession, groupName2);

    //this should fail if using transactions (will not work under hib2)
    //not good since READONLY txs should not commit
    String exception = "";
    try {
      GrouperTransaction.callbackGrouperTransaction(
          GrouperTransactionType.READ_WRITE_OR_USE_EXISTING,
          new GrouperTransactionHandler() {

            public Object callback(GrouperTransaction grouperTransaction)
                throws GrouperDAOException {

              try {
                Group.saveGroup(rootSession, null, null, groupName, displayExtension,
                    groupDescription, 
                    SaveMode.INSERT, false);

                Group.saveGroup(rootSession, null, null, groupName2, 
                    displayExtension, groupDescription, SaveMode.INSERT, false);

                throw new RuntimeException("Just to cause a rollback");

              } catch (Exception e) {
                throw new RuntimeException(e);
              }
            }

          });
    } catch (Exception e) {
      //good, it failed
      exception = ExceptionUtils.getFullStackTrace(e).toLowerCase();
    }
    assertTrue("Should be from inner exception, have that text in there: " + exception,
        StringUtils.contains(exception, "just to cause a rollback"));

    //see if group is there
    //now retrieve, shouldnt be there
    try {
      GroupFinder.findByName(rootSession, groupName, true);
      fail("Shouldnt find the group");
    } catch (GroupNotFoundException gnfe) {
      //all good
    }
    try {
      GroupFinder.findByName(rootSession, groupName2, true);
      fail("Shouldnt find the group");
    } catch (GroupNotFoundException gnfe) {
      //all good
    }

    //###########################################
    //## test rolling back in the middle of a transaction manually
    GrouperTest.deleteGroupIfExists(rootSession, groupName);
    GrouperTest.deleteGroupIfExists(rootSession, groupName2);

    //note the default is READ_WRITE_OR_USE_EXISTING
    GrouperTransaction.callbackGrouperTransaction(new GrouperTransactionHandler() {

      public Object callback(GrouperTransaction grouperTransaction)
          throws GrouperDAOException {

        try {
          Group.saveGroup(rootSession, null,null,groupName,displayExtension,  groupDescription, 
              SaveMode.INSERT, false);

          Group.saveGroup(rootSession, null,null, groupName2,displayExtension, groupDescription, 
              SaveMode.INSERT, false);

          assertTrue("Should be active since not rolled back", grouperTransaction
              .isTransactionActive());
          //this will be new, so it should work
          grouperTransaction.rollback(GrouperRollbackType.ROLLBACK_IF_NEW_TRANSACTION);

          assertFalse("Rolled back, should be inactive", grouperTransaction
              .isTransactionActive());

          return null;
        } catch (Exception e) {
          //note, if specific exceptions need to be handled, do that here
          throw new RuntimeException(e);
        }
      }

    });

    //see if group is there
    //now retrieve, shouldnt be there
    try {
      GroupFinder.findByName(rootSession, groupName, true);
      fail("Shouldnt find the group");
    } catch (GroupNotFoundException gnfe) {
      //all good
    }
    try {
      GroupFinder.findByName(rootSession, groupName2, true);
      fail("Shouldnt find the group");
    } catch (GroupNotFoundException gnfe) {
      //all good
    }

    //###########################################
    //## now do an autonomous transaction... inner commit should not affect outer rollback
    GrouperTest.deleteGroupIfExists(rootSession, groupName);
    GrouperTest.deleteGroupIfExists(rootSession, groupName2);

    //note the default is READ_WRITE_OR_USE_EXISTING
    GrouperTransaction.callbackGrouperTransaction(new GrouperTransactionHandler() {

      public Object callback(GrouperTransaction grouperTransaction)
          throws GrouperDAOException {

        try {
          Group.saveGroup(rootSession,null, null,groupName, displayExtension, groupDescription, 
              SaveMode.INSERT, false);

          //automous transaction
          GrouperTransaction.callbackGrouperTransaction(
              GrouperTransactionType.READ_WRITE_NEW, new GrouperTransactionHandler() {

                public Object callback(GrouperTransaction grouperTransaction)
                    throws GrouperDAOException {

                  try {
                    Group.saveGroup(rootSession, null, null, groupName2, displayExtension,
                        groupDescription, 
                        SaveMode.INSERT, false);
                  } catch (Exception e) {
                    throw new RuntimeException(e);
                  }
                  //this will exit normally and commit
                  return null;
                }

              });

          assertTrue("Should be active since not rolled back", grouperTransaction
              .isTransactionActive());
          //this will be new, so it should work
          grouperTransaction.rollback(GrouperRollbackType.ROLLBACK_NOW);

          assertFalse("Rolled back, should be inactive", grouperTransaction
              .isTransactionActive());

          return null;
        } catch (Exception e) {
          //note, if specific exceptions need to be handled, do that here
          throw new RuntimeException(e);
        }
      }

    });

    //see if group is there
    //now retrieve, shouldnt be there since out tx rolled back
    try {
      GroupFinder.findByName(rootSession, groupName, true);
      fail("Shouldnt find the group");
    } catch (GroupNotFoundException gnfe) {
      //all good
    }
    //this one should be there since inner tx committed
    //now retrieve
    Group foundGroup2 = GroupFinder.findByName(rootSession, groupName2, true);

    assertEquals("Name should be there", groupName2, foundGroup2.getName());

    rootSession.stop();

  }

  /**
   * run multiple logic together
   * @param grouperSession
   * @param groupName
   * @param groupName2
   * @param displayExtension
   * @param groupDescription
   */
  public void runLogic(GrouperSession grouperSession, String groupName,
      String groupName2, String displayExtension, String groupDescription) {
    try {

      //insert a group
      Group.saveGroup(grouperSession, null,null,groupName, displayExtension, groupDescription, 
          SaveMode.INSERT, false);

      //insert another group
      Group.saveGroup(grouperSession,  null,null, groupName2,displayExtension, groupDescription, 
          SaveMode.INSERT, false);
    } catch (StemNotFoundException e) {
      throw new RuntimeException("Stem wasnt found", e);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

  }

  /**
   * show simple transaction
   * @throws RuntimeException if problem
   */
  public void testTransaction() {
    
//    if (GrouperDAOFactory.getFactory() instanceof HibernateDAOFactory) {
//      fail("This doesnt work with hib2 at the moment (only hib3 that I know of)...");
//    }
    
    final GrouperSession rootSession = SessionHelper.getRootSession();
    final String displayExtension = "testing123 display";
    final String groupDescription = "description";
    final String groupName = "i2:a:testing123";
    final String groupName2 = "i2:b:testing124";

    try {
      R.populateRegistry(2, 2, 0);
  
      GrouperTest.deleteGroupIfExists(rootSession, groupName);
      GrouperTest.deleteGroupIfExists(rootSession, groupName2);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    //demonstrate passing back data with final array
    final Integer[] someInt = new Integer[1];

    //you can pass back one object from return
    @SuppressWarnings("unused")
    String anythingString = (String) GrouperTransaction
        .callbackGrouperTransaction(new GrouperTransactionHandler() {

          public Object callback(GrouperTransaction grouperTransaction)
              throws GrouperDAOException {
            
            //everything in here will run in one transaction, note how to access "this"
            TestGroup0.this.runLogic(rootSession, groupName, groupName2,
                displayExtension, groupDescription);
            
            //pass data back from final array (if need more than just return value)
            someInt[0] = 5;

            //if return with no exception, then it will auto-commit.
            //if exception was thrown it will rollback
            //this can be controlled manually with grouperTransaction.commit()
            //but it would be rare to have to do that
            
            //pass data back from return value
            return "anything";
          }

        });

    //System.out.println(anythingString + ", " + someInt[0]);
  }

  /**
   * runt he perf problem.  You should have the DB setup with LoadData.
   * @throws Exception
   */
  public static void runPerfProblem2() throws Exception {
    
    GrouperSession session = null;
    Stem rootStem = null;
    session = SessionHelper.getRootSession();
    rootStem = StemFinder.findRootStem(session);

    String monitorLabel = "runPerfProblem2Helper";

    //init everything
    runPerfProblem2Helper(session, rootStem, false, monitorLabel);
    
    for (int i=0;i<10;i++) {
      runPerfProblem2Helper(session, rootStem, true, monitorLabel);
    }
    
    //print timer report
    Monitor monitor = MonitorFactory.getMonitor(monitorLabel, "ms.");
    System.out.println("Stats:\n" + monitor);
    
  }
    
  /**
   * run the logic of the test
   * @param printResults
   */
  private static void runPerfProblem2Helper(GrouperSession session, Stem rootStem, 
      boolean timeResults, String monitorLabel) throws Exception {
    Monitor mon = null;
    if (timeResults) {
      mon = MonitorFactory.start(monitorLabel);
    }
    GrouperQuery gq = GrouperQuery.createQuery(session, 
        new GroupAttributeFilter("name", "SUBJECT1", rootStem));
    Set queryGroups = gq.getGroups();
    if (timeResults) {
      mon.stop();
    }
  }
  
  /**
   * perf problem
   */
  public static void runPerfProblem() throws Exception {
    GrouperSession rootSession = null;
    Stem rootStem = null;
    rootSession = SessionHelper.getRootSession();
    rootStem = StemFinder.findRootStem(rootSession); 
    for (int i=0;i<100;i++) {
      try {
        SubjectFinder.findById("GrouperSystem"+i, true);
      } catch (SubjectNotFoundException e) {
        
      }
    }
    
    GrouperQuery gq =
      GrouperQuery.createQuery(rootSession, new GroupAttributeFilter("name",
      "i2:b:a", rootStem));
    Set queryGroups = gq.getGroups();
    runPerfProblemLogic(rootSession, rootStem);
  }

  /**
   * run perf problem logic
   * @param rootSession
   * @param rootStem
   * @throws QueryException
   */
  private static void runPerfProblemLogic(final GrouperSession rootSession, final Stem rootStem)
      throws Exception {
    long now = System.currentTimeMillis();
    for (int i=0;i<1000;i++) {
//    HibernateSession.callbackHibernateSession(GrouperTransactionType.READONLY_NEW,
//        new HibernateHandler() {
//
//          public Object callback(HibernateSession hibernateSession)
//              throws GrouperDAOException {
              try {
                GrouperQuery gq =
                  GrouperQuery.createQuery(rootSession, new GroupAttributeFilter("name",
                  "i2:b:a", rootStem));
                Set queryGroups = gq.getGroups();
              } catch (Exception e) {
                throw new RuntimeException(e);
              }
//            return null;
//          }
//      
//    });
    }
    System.out.println("Took: " + (System.currentTimeMillis() - now) + "ms");
  }
}
