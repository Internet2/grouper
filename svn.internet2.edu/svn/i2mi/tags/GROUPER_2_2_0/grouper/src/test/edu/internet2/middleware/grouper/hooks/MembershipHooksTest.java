/*******************************************************************************
 * Copyright 2012 Internet2
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
 ******************************************************************************/
/*
 * @author mchyzer
 * $Id: MembershipHooksTest.java,v 1.13 2009-11-09 03:12:18 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks;

import java.util.Set;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SessionHelper;
import edu.internet2.middleware.grouper.helper.StemHelper;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.hibernate.GrouperCommitType;
import edu.internet2.middleware.grouper.hibernate.GrouperTransaction;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionHandler;
import edu.internet2.middleware.grouper.hooks.beans.GrouperContextTypeBuiltIn;
import edu.internet2.middleware.grouper.hooks.beans.HooksContext;
import edu.internet2.middleware.grouper.hooks.logic.GrouperHookType;
import edu.internet2.middleware.grouper.hooks.logic.HookVeto;
import edu.internet2.middleware.grouper.hooks.logic.VetoTypeGrouper;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.registry.RegistryReset;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 *
 */
public class MembershipHooksTest extends GrouperTest {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    //TestRunner.run(new MembershipHooksTest("testMemberPreAddMember"));
    TestRunner.run(MembershipHooksTest.class);
  }
  
  /**
   * @param name
   */
  public MembershipHooksTest(String name) {
    super(name);
  }

  /** edu stem */
  private Stem edu;
  
  /** root stem */
  private Stem root;
  
  /** grouper sesion */
  static GrouperSession grouperSession; 

  /** test group */
  private Group group;
  
  /**
   * @see edu.internet2.middleware.grouper.helper.GrouperTest#tearDown()
   */
  @Override
  protected void tearDown() {
    overrideHooksRemove();
  }

  /**
   * 
   */
  private void overrideHooksRemove() {
    //dont have the test hook imple
    GrouperHookType.addHookOverride(GrouperHookType.MEMBERSHIP.getPropertyFileKey(), (Class<?>)null);
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.helper.GrouperTest#setUp()
   */
  protected void setUp () {
    RegistryReset.internal_resetRegistryAndAddTestSubjects();
    GrouperTest.initGroupsAndAttributes();

    grouperSession     = SessionHelper.getRootSession();
    root  = StemHelper.findRootStem(grouperSession);
    edu   = StemHelper.addChildStem(root, "edu", "education");
    group = StemHelper.addChildGroup(this.edu, "group", "the group");

  }

  /**
   * @throws Exception 
   */
  public void testMemberPreAddMember() throws Exception {
    
    //this is the test hook imple
    GrouperHookType.addHookOverride(GrouperHookType.MEMBERSHIP.getPropertyFileKey(), MembershipHooksImpl.class);
  
    MembershipHooksImpl.mostRecentAddMemberSubjectId = null;
    
    group.addMember(SubjectTestHelper.SUBJ0);
    Set<Membership> memberships = group.getMemberships();
    assertEquals(SubjectTestHelper.SUBJ0.getId(), ((Membership)memberships.toArray()[0]).getMember().getSubjectId());
    assertEquals(SubjectTestHelper.SUBJ0.getId(), MembershipHooksImpl.mostRecentAddMemberSubjectId);
    
    try {
      group.addMember(SubjectTestHelper.SUBJ1);
      fail("Should veto subj1");
    } catch (HookVeto hookVeto) {
      assertEquals("subject cannot be subj1", hookVeto.getReason());
      assertEquals(VetoTypeGrouper.MEMBERSHIP_PRE_ADD_MEMBER, hookVeto.getVetoType());
    }
    
    int hookCount = MembershipHooksImpl.preAddMemberHookCount;
    
    group.deleteMember(SubjectTestHelper.SUBJ0);
    
    assertEquals("This should not fire the add member hook", hookCount, MembershipHooksImpl.preAddMemberHookCount);
  }

  /**
   * @throws Exception 
   */
  public void testMemberPostCommitAddMember() throws Exception {
    
    //this is the test hook imple
    GrouperHookType.addHookOverride(GrouperHookType.MEMBERSHIP.getPropertyFileKey(), MembershipHooksImpl.class);

    MembershipHooksImpl.mostRecentAddCommitMemberSubjectId = null;
    
    GrouperTransaction.callbackGrouperTransaction(new GrouperTransactionHandler() {
  
      public Object callback(GrouperTransaction grouperTransaction)
          throws GrouperDAOException {
        
        try {
          group.addMember(SubjectTestHelper.SUBJ0);
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
        
        assertNull("shouldnt fire yet", MembershipHooksImpl.mostRecentAddCommitMemberSubjectId);
        grouperTransaction.commit(GrouperCommitType.COMMIT_NOW);
        
        assertEquals(SubjectTestHelper.SUBJ0.getId(), MembershipHooksImpl.mostRecentAddCommitMemberSubjectId);
        return null;
      }
    });    
    
  }

  /**
   * test async with callback
   * @throws Exception 
   */
  public void testMemberPreAddMemberAsync() throws Exception {
    
    //this is the test hook imple
    GrouperHookType.addHookOverride(GrouperHookType.MEMBERSHIP.getPropertyFileKey(), MembershipHooksImplAsync.class);

    //clear the override threadlocals, assign stuff
    HooksContext.clearThreadLocal();
    HooksContext.setAttributeThreadLocal("testMemberPreAddMemberAsync", "hey2", false);
    HooksContext.setAttributeThreadLocal("testMemberPreAddMemberAsync2", "there2", true);
    MembershipHooksImplAsync.hooksContextValue = null;
    MembershipHooksImplAsync.hooksContextValue2 = null;
    MembershipHooksImplAsync.hooksGrouperSessionSubject = null;
    MembershipHooksImplAsync.hooksGrouperContext = null;
    
    MembershipHooksImplAsync.mostRecentInsertMemberSubjectId = null;
    
    int currentSleepCount = MembershipHooksImplAsync.preAddMemberHookCountAyncSeconds;
    
    GrouperContextTypeBuiltIn.setDefaultContext(GrouperContextTypeBuiltIn.GROUPER_UI);
    GrouperContextTypeBuiltIn.setThreadLocalContext(GrouperContextTypeBuiltIn.GROUPER_WS);
    
    group.addMember(SubjectTestHelper.SUBJ0);
    
    GrouperUtil.sleep(500);
    
    assertEquals("Monitor the progress of the hook in another thread", currentSleepCount+1, MembershipHooksImplAsync.preAddMemberHookCountAyncSeconds);
    
    GrouperUtil.sleep(1000);
    
    assertEquals("Monitor the progress of the hook in another thread", currentSleepCount+2, MembershipHooksImplAsync.preAddMemberHookCountAyncSeconds);
    
    Set<Membership> memberships = group.getMemberships();
    assertEquals(SubjectTestHelper.SUBJ0.getId(), ((Membership)memberships.toArray()[0]).getMember().getSubjectId());
    assertEquals(SubjectTestHelper.SUBJ0.getId(), MembershipHooksImplAsync.mostRecentInsertMemberSubjectId);
    
    assertEquals("This one is not threadsafe, shouldnt go to next thread", null, 
        MembershipHooksImplAsync.hooksContextValue);
    assertEquals("This one is threadsafe, should go to next thread", "there2", 
        MembershipHooksImplAsync.hooksContextValue2);
    assertEquals("Should propagate subject to new thread", 
        SessionHelper.getRootSession().getSubject().getId(), 
        MembershipHooksImplAsync.hooksGrouperSessionSubject.getId());
    assertEquals("Should get the threadlocal context type", 
        GrouperContextTypeBuiltIn.GROUPER_WS, 
        MembershipHooksImplAsync.hooksGrouperContext);
    
    GrouperUtil.sleep(1000);
    assertTrue(MembershipHooksImplAsync.done);
    if (MembershipHooksImplAsync.problem != null) {
      throw MembershipHooksImplAsync.problem;
    }
  }

  /**
   * test async with interface
   * @throws Exception 
   */
  public void testMemberPreAddMemberAsync2() throws Exception {
    
    //this is the test hook imple
    GrouperHookType.addHookOverride(GrouperHookType.MEMBERSHIP.getPropertyFileKey(), MembershipHooksImplAsync2.class);

    MembershipHooksImplAsync2.mostRecentInsertMemberSubjectId = null;
    
    int currentSleepCount = MembershipHooksImplAsync2.preAddMemberHookCountAyncSeconds;
    
    group.addMember(SubjectTestHelper.SUBJ0);
    
    GrouperUtil.sleep(500);
    
    assertEquals("Monitor the progress of the hook in another thread", currentSleepCount+1, MembershipHooksImplAsync2.preAddMemberHookCountAyncSeconds);
    
    GrouperUtil.sleep(1000);
    
    assertEquals("Monitor the progress of the hook in another thread", currentSleepCount+2, MembershipHooksImplAsync2.preAddMemberHookCountAyncSeconds);
    
    Set<Membership> memberships = group.getMemberships();
    assertEquals(SubjectTestHelper.SUBJ0.getId(), ((Membership)memberships.toArray()[0]).getMember().getSubjectId());
    assertEquals(SubjectTestHelper.SUBJ0.getId(), MembershipHooksImplAsync2.mostRecentInsertMemberSubjectId);
    
    GrouperUtil.sleep(1000);
    assertTrue(MembershipHooksImplAsync2.done);
    if (MembershipHooksImplAsync2.problem != null) {
      throw MembershipHooksImplAsync2.problem;
    }
  }

  /**
   * @throws Exception 
   */
  public void testMemberPreInsert() throws Exception {
    
    //this is the test hook imple
    GrouperHookType.addHookOverride(GrouperHookType.MEMBERSHIP.getPropertyFileKey(), 
        MembershipHooksImpl3.class);

    //clear the override threadlocals, assign stuff
    HooksContext.clearThreadLocal();
    HooksContext.setAttributeThreadLocal("testMemberPreInsert", "hey", false);
    HooksContext.setAttributeThreadLocal("testMemberPreInsert2", "there", true);
    MembershipHooksImpl3.hooksContextValue = null;
    MembershipHooksImpl3.hooksContextValue2 = null;
    
    MembershipHooksImpl3.mostRecentInsertMemberSubjectId = null;
    
    group.addMember(SubjectTestHelper.SUBJ0);
    Set<Membership> memberships = group.getMemberships();
    assertEquals(SubjectTestHelper.SUBJ0.getId(), ((Membership)memberships.toArray()[0]).getMember().getSubjectId());
    assertEquals(SubjectTestHelper.SUBJ0.getId(), MembershipHooksImpl3.mostRecentInsertMemberSubjectId);
    
    assertEquals("hey", MembershipHooksImpl3.hooksContextValue);
    assertEquals("there", MembershipHooksImpl3.hooksContextValue2);
    
    try {
      group.addMember(SubjectTestHelper.SUBJ1);
      fail("Should veto subj1");
    } catch (HookVeto hookVeto) {
      assertEquals("subject cannot be subj1", hookVeto.getReason());
      assertEquals(VetoTypeGrouper.MEMBERSHIP_PRE_INSERT, hookVeto.getVetoType());
    }
    memberships = group.getMemberships();
    assertEquals(1, memberships.size());
    assertEquals(SubjectTestHelper.SUBJ0.getId(), ((Membership)memberships.toArray()[0]).getMember().getSubjectId());
  }

  /**
   * @throws Exception 
   */
  public void testMemberPostInsert() throws Exception {
    
    //this is the test hook imple
    GrouperHookType.addHookOverride(GrouperHookType.MEMBERSHIP.getPropertyFileKey(), 
        MembershipHooksImpl4.class);
  
    MembershipHooksImpl4.mostRecentInsertMemberSubjectId = null;
    
    group.addMember(SubjectTestHelper.SUBJ0);
    Set<Membership> memberships = group.getMemberships();
    assertEquals(SubjectTestHelper.SUBJ0.getId(), ((Membership)memberships.toArray()[0]).getMember().getSubjectId());
    assertEquals(SubjectTestHelper.SUBJ0.getId(), MembershipHooksImpl4.mostRecentInsertMemberSubjectId);
    
    try {
      group.addMember(SubjectTestHelper.SUBJ1);
      fail("Should veto subj1");
    } catch (HookVeto hookVeto) {
      assertEquals("subject cannot be subj1", hookVeto.getReason());
      assertEquals(VetoTypeGrouper.MEMBERSHIP_POST_INSERT, hookVeto.getVetoType());
    }
  }

  /**
   * @throws Exception 
   */
  public void testMemberPostCommitInsert() throws Exception {
    
    //this is the test hook imple
    GrouperHookType.addHookOverride(GrouperHookType.MEMBERSHIP.getPropertyFileKey(), 
        MembershipHooksImpl4.class);

    MembershipHooksImpl4.mostRecentInsertCommitMemberSubjectId = null;
        
    GrouperTransaction.callbackGrouperTransaction(new GrouperTransactionHandler() {
      
      public Object callback(GrouperTransaction grouperTransaction)
          throws GrouperDAOException {
        
        try {
          group.addMember(SubjectTestHelper.SUBJ0);
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
        
        assertNull("shouldnt fire yet", MembershipHooksImpl4.mostRecentInsertCommitMemberSubjectId);
        grouperTransaction.commit(GrouperCommitType.COMMIT_NOW);
        
        assertEquals(SubjectTestHelper.SUBJ0.getId(), MembershipHooksImpl4.mostRecentInsertCommitMemberSubjectId);
        return null;
      }
    });    
  }

  /**
   * @throws Exception 
   */
  public void testMemberPreDelete() throws Exception {
    
    //this is the test hook imple
    GrouperHookType.addHookOverride(GrouperHookType.MEMBERSHIP.getPropertyFileKey(), 
        MembershipHooksImpl5.class);
  
    
    group.addMember(SubjectTestHelper.SUBJ0);
    
    Set<Membership> memberships = group.getMemberships();
    assertEquals(SubjectTestHelper.SUBJ0.getId(), ((Membership)memberships.toArray()[0]).getMember().getSubjectId());
  
    MembershipHooksImpl5.mostRecentDeleteMemberSubjectId = null;
  
    group.deleteMember(SubjectTestHelper.SUBJ0);
    
    memberships = group.getMemberships();
  
    assertEquals(0, memberships.size());
  
    assertEquals(SubjectTestHelper.SUBJ0.getId(), MembershipHooksImpl5.mostRecentDeleteMemberSubjectId);
    
    group.addMember(SubjectTestHelper.SUBJ1);
    
    try {
      group.deleteMember(SubjectTestHelper.SUBJ1);
      fail("Should veto subj1");
    } catch (HookVeto hookVeto) {
      assertEquals("subject cannot be subj1", hookVeto.getReason());
      assertEquals(VetoTypeGrouper.MEMBERSHIP_PRE_DELETE, hookVeto.getVetoType());
    }
    
  }

  /**
   * @throws Exception 
   */
  public void testMemberPreRemove() throws Exception {
    
    //this is the test hook imple
    GrouperHookType.addHookOverride(GrouperHookType.MEMBERSHIP.getPropertyFileKey(), 
        MembershipHooksImpl7.class);

    
    group.addMember(SubjectTestHelper.SUBJ0);
    
    Set<Membership> memberships = group.getMemberships();
    assertEquals(SubjectTestHelper.SUBJ0.getId(), ((Membership)memberships.toArray()[0]).getMember().getSubjectId());

    MembershipHooksImpl7.mostRecentDeleteMemberSubjectId = null;

    group.deleteMember(SubjectTestHelper.SUBJ0);
    
    memberships = group.getMemberships();

    assertEquals(0, memberships.size());

    assertEquals(SubjectTestHelper.SUBJ0.getId(), MembershipHooksImpl7.mostRecentDeleteMemberSubjectId);
    
    group.addMember(SubjectTestHelper.SUBJ1);
    
    try {
      group.deleteMember(SubjectTestHelper.SUBJ1);
      fail("Should veto subj1");
    } catch (HookVeto hookVeto) {
      assertEquals("subject cannot be subj1", hookVeto.getReason());
      assertEquals(VetoTypeGrouper.MEMBERSHIP_PRE_REMOVE_MEMBER, hookVeto.getVetoType());
    }
    
  }

  /**
   * @throws Exception 
   */
  public void testMemberPostDelete() throws Exception {
    
    //this is the test hook imple
    GrouperHookType.addHookOverride(GrouperHookType.MEMBERSHIP.getPropertyFileKey(), 
        MembershipHooksImpl6.class);
  
    
    group.addMember(SubjectTestHelper.SUBJ0);
    
    Set<Membership> memberships = group.getMemberships();
    assertEquals(SubjectTestHelper.SUBJ0.getId(), ((Membership)memberships.toArray()[0]).getMember().getSubjectId());
  
    MembershipHooksImpl6.mostRecentDeleteMemberSubjectId = null;
  
    group.deleteMember(SubjectTestHelper.SUBJ0);
    
    memberships = group.getMemberships();
  
    assertEquals(0, memberships.size());
  
    assertEquals(SubjectTestHelper.SUBJ0.getId(), MembershipHooksImpl6.mostRecentDeleteMemberSubjectId);
    
    group.addMember(SubjectTestHelper.SUBJ1);
    
    try {
      group.deleteMember(SubjectTestHelper.SUBJ1);
      fail("Should veto subj1");
    } catch (HookVeto hookVeto) {
      assertEquals("subject cannot be subj1", hookVeto.getReason());
      assertEquals(VetoTypeGrouper.MEMBERSHIP_POST_DELETE, hookVeto.getVetoType());
    }
    
  }

  /**
   * @throws Exception 
   */
  public void testMemberPostCommitDelete() throws Exception {
    
    //this is the test hook imple
    GrouperHookType.addHookOverride(GrouperHookType.MEMBERSHIP.getPropertyFileKey(), 
        MembershipHooksImpl6.class);
  
    group.addMember(SubjectTestHelper.SUBJ0);
    
    MembershipHooksImpl6.mostRecentDeleteCommitMemberSubjectId = null;

    GrouperTransaction.callbackGrouperTransaction(new GrouperTransactionHandler() {
      
      public Object callback(GrouperTransaction grouperTransaction)
          throws GrouperDAOException {
        
        try {
          group.deleteMember(SubjectTestHelper.SUBJ0);
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
        
        assertNull("shouldnt fire yet", MembershipHooksImpl6.mostRecentDeleteCommitMemberSubjectId);
        grouperTransaction.commit(GrouperCommitType.COMMIT_NOW);
        
        assertEquals(SubjectTestHelper.SUBJ0.getId(), MembershipHooksImpl6.mostRecentDeleteCommitMemberSubjectId);
        return null;
      }
    });    
  }

  /**
   * @throws Exception 
   */
  public void testMemberPostRemoveMember() throws Exception {
    
    //this is the test hook imple
    GrouperHookType.addHookOverride(GrouperHookType.MEMBERSHIP.getPropertyFileKey(), 
        MembershipHooksImpl8.class);
  
    
    group.addMember(SubjectTestHelper.SUBJ0);
    
    Set<Membership> memberships = group.getMemberships();
    assertEquals(SubjectTestHelper.SUBJ0.getId(), ((Membership)memberships.toArray()[0]).getMember().getSubjectId());
  
    MembershipHooksImpl8.mostRecentRemoveMemberSubjectId = null;
  
    group.deleteMember(SubjectTestHelper.SUBJ0);
    
    memberships = group.getMemberships();
  
    assertEquals(0, memberships.size());
  
    assertEquals(SubjectTestHelper.SUBJ0.getId(), MembershipHooksImpl8.mostRecentRemoveMemberSubjectId);
    
    group.addMember(SubjectTestHelper.SUBJ1);
    
    try {
      group.deleteMember(SubjectTestHelper.SUBJ1);
      fail("Should veto subj1");
    } catch (HookVeto hookVeto) {
      assertEquals("subject cannot be subj1", hookVeto.getReason());
      assertEquals(VetoTypeGrouper.MEMBERSHIP_POST_REMOVE_MEMBER, hookVeto.getVetoType());
    }
    
  }

  /**
   * @throws Exception 
   */
  public void testMemberPostCommitRemoveMember() throws Exception {
    
    //this is the test hook imple
    GrouperHookType.addHookOverride(GrouperHookType.MEMBERSHIP.getPropertyFileKey(), 
        MembershipHooksImpl8.class);

    group.addMember(SubjectTestHelper.SUBJ0);

    MembershipHooksImpl8.mostRecentRemoveCommitMemberSubjectId = null;

    GrouperTransaction.callbackGrouperTransaction(new GrouperTransactionHandler() {
      
      public Object callback(GrouperTransaction grouperTransaction)
          throws GrouperDAOException {
        
        try {
          group.deleteMember(SubjectTestHelper.SUBJ0);
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
        
        assertNull("shouldnt fire yet", MembershipHooksImpl8.mostRecentRemoveCommitMemberSubjectId);
        grouperTransaction.commit(GrouperCommitType.COMMIT_NOW);
        
        assertEquals(SubjectTestHelper.SUBJ0.getId(), MembershipHooksImpl8.mostRecentRemoveCommitMemberSubjectId);
        return null;
      }
    });    
  }

  /**
   * @throws Exception 
   */
  public void testMemberPostAddMember() throws Exception {
    
    //this is the test hook imple
    GrouperHookType.addHookOverride(GrouperHookType.MEMBERSHIP.getPropertyFileKey(), MembershipHooksImpl2.class);
    
    MembershipHooksImpl2.mostRecentInsertMemberSubjectId = null;

    group.addMember(SubjectTestHelper.SUBJ0);
    Set<Membership> memberships = group.getMemberships();
    assertEquals(SubjectTestHelper.SUBJ0.getId(), ((Membership)memberships.toArray()[0]).getMember().getSubjectId());
    assertEquals(SubjectTestHelper.SUBJ0.getId(), MembershipHooksImpl2.mostRecentInsertMemberSubjectId);
    
    try {
      group.addMember(SubjectTestHelper.SUBJ1);
      fail("Should veto subj1");
    } catch (HookVeto hookVeto) {
      assertEquals("subject cannot be subj1", hookVeto.getReason());
      assertEquals(VetoTypeGrouper.MEMBERSHIP_POST_ADD_MEMBER, hookVeto.getVetoType());
    }
    
  }


}
