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
 * $Id: MemberHooksTest.java,v 1.9 2009-11-09 03:12:18 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks;

import java.util.Set;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.exception.MemberAddException;
import edu.internet2.middleware.grouper.exception.MemberDeleteException;
import edu.internet2.middleware.grouper.exception.MemberNotFoundException;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SessionHelper;
import edu.internet2.middleware.grouper.helper.StemHelper;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.hibernate.GrouperCommitType;
import edu.internet2.middleware.grouper.hibernate.GrouperTransaction;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionHandler;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.hooks.logic.GrouperHookType;
import edu.internet2.middleware.grouper.hooks.logic.HookVeto;
import edu.internet2.middleware.grouper.hooks.logic.VetoTypeGrouper;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.registry.RegistryReset;


/**
 *
 */
public class MemberHooksTest extends GrouperTest {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new MemberHooksTest("testMemberPostInsert"));
    //TestRunner.run(new MemberHooksTest(""));
    //TestRunner.run(MemberHooksTest.class);
  }
  
  /**
   * @param name
   */
  public MemberHooksTest(String name) {
    super(name);
  }

  /**
   * @throws MemberAddException 
   * @throws InsufficientPrivilegeException 
   * @throws MemberNotFoundException 
   * 
   */
  public void testMemberPreInsert() throws MemberAddException, InsufficientPrivilegeException,
      MemberNotFoundException {
    
    MemberHooksImpl.mostRecentPreInsertMemberSubjectId = null;
    
    group.addMember(SubjectTestHelper.SUBJ0);
    Set<Membership> memberships = group.getMemberships();
    assertEquals(SubjectTestHelper.SUBJ0.getId(), ((Membership)memberships.toArray()[0]).getMember().getSubjectId());
    assertEquals(SubjectTestHelper.SUBJ0.getId(), MemberHooksImpl.mostRecentPreInsertMemberSubjectId);
    assertEquals(1, memberships.size());
    
    try {
      group.addMember(SubjectTestHelper.SUBJ1);
      fail("Should veto subj1");
    } catch (HookVeto hookVeto) {
      assertEquals("subjectId cannot be " + SubjectTestHelper.SUBJ1.getId(), hookVeto.getReason());
      assertEquals(VetoTypeGrouper.MEMBER_PRE_INSERT, hookVeto.getVetoType());
    }
    memberships = group.getMemberships();
    assertEquals(1, memberships.size());
    assertEquals(SubjectTestHelper.SUBJ0.getId(), ((Membership)memberships.toArray()[0]).getMember().getSubjectId());
    
  }

  /**
   * @throws MemberAddException 
   * @throws InsufficientPrivilegeException 
   * @throws MemberNotFoundException 
   * 
   */
  public void testMemberPreUpdate() throws MemberAddException, InsufficientPrivilegeException,
      MemberNotFoundException {
    
    group.addMember(SubjectTestHelper.SUBJ0);
    
    Member member0 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ0, true);
    
    MemberHooksImpl.mostRecentPreUpdateMemberSubjectId = null;
    
    member0.setSubjectId("whatever");
    member0.store();
    
    assertEquals("whatever", MemberHooksImpl.mostRecentPreUpdateMemberSubjectId);
    
    group.addMember(SubjectTestHelper.SUBJ2);
    Member member2 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ2, true);
    try {
      member2.setSubjectId("whatever2");
      member2.store();
      
      fail("Should veto subj2");
    } catch (HookVeto hookVeto) {
      assertEquals("subjectId cannot be whatever2", hookVeto.getReason());
      assertEquals(VetoTypeGrouper.MEMBER_PRE_UPDATE, hookVeto.getVetoType());
    }
    member2 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ2, true);
    assertEquals(SubjectTestHelper.SUBJ2.getId(), member2.getSubjectId());
    
  }

  /**
   * @throws MemberAddException 
   * @throws InsufficientPrivilegeException 
   * @throws MemberNotFoundException 
   * 
   */
  public void testMemberPostUpdate() throws MemberAddException, InsufficientPrivilegeException,
      MemberNotFoundException {
    
    group.addMember(SubjectTestHelper.SUBJ0);
    
    Member member0 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ0, true);
    
    MemberHooksImpl.mostRecentPreUpdateMemberSubjectId = null;
    
    member0.setSubjectId("whatever3");
    member0.store();
    
    assertEquals("whatever3", MemberHooksImpl.mostRecentPreUpdateMemberSubjectId);
    
    group.addMember(SubjectTestHelper.SUBJ2);
    Member member2 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ2, true);
    try {
      member2.setSubjectId("whatever4");
      member2.store();
      fail("Should veto subj2");
    } catch (HookVeto hookVeto) {
      assertEquals("subjectId cannot be whatever4", hookVeto.getReason());
      assertEquals(VetoTypeGrouper.MEMBER_POST_UPDATE, hookVeto.getVetoType());
    }
    member2 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ2, true);
    assertEquals(SubjectTestHelper.SUBJ2.getId(), member2.getSubjectId());
    
  }

  /**
   * @throws Exception 
   */
  public void testMemberPostInsert() throws Exception {
    
    MemberHooksImpl.mostRecentPostInsertMemberSubjectId = null;
    
    group.addMember(SubjectTestHelper.SUBJ2);
    Set<Membership> memberships = group.getMemberships();
    assertEquals(SubjectTestHelper.SUBJ2.getId(), ((Membership)memberships.toArray()[0]).getMember().getSubjectId());
    assertEquals(SubjectTestHelper.SUBJ2.getId(), MemberHooksImpl.mostRecentPostInsertMemberSubjectId);
    assertEquals(1, memberships.size());
    
    try {
      group.addMember(SubjectTestHelper.SUBJ3);
      fail("Should veto subj3");
    } catch (HookVeto hookVeto) {
      assertEquals("subjectId cannot be " + SubjectTestHelper.SUBJ3.getId(), hookVeto.getReason());
      assertEquals(VetoTypeGrouper.MEMBER_POST_INSERT, hookVeto.getVetoType());
    }
    assertEquals(1, memberships.size());
    assertEquals(SubjectTestHelper.SUBJ2.getId(), ((Membership)memberships.toArray()[0]).getMember().getSubjectId());
  }

  /**
   * @throws Exception 
   */
  public void testMemberPostDelete() throws Exception {
    
    MemberHooksImpl.mostRecentPostDeleteMemberSubjectId = null;
    
    Member member = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ0, true);
    HibernateSession.byObjectStatic().delete(member);

    assertEquals(SubjectTestHelper.SUBJ0.getId(), MemberHooksImpl.mostRecentPostDeleteMemberSubjectId);
    
    member = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ6, true);
    
    try {
      HibernateSession.byObjectStatic().delete(member);
      fail("Should veto " + SubjectTestHelper.SUBJ6);
    } catch (HookVeto hookVeto) {
      assertEquals("subjectId cannot be " + SubjectTestHelper.SUBJ6.getId(), hookVeto.getReason());
      assertEquals(VetoTypeGrouper.MEMBER_POST_DELETE, hookVeto.getVetoType());
    }
  }


  
  /** grouper sesion */
  private GrouperSession grouperSession;
  /**
   * edu stem 
   */
  private Stem edu;
  /**
   * test group 
   */
  private Group group;
  /**
   * root stem 
   */
  private Stem root; 

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
    GrouperHookType.addHookOverride(GrouperHookType.MEMBER.getPropertyFileKey(), (Class<?>)null);
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.helper.GrouperTest#setUp()
   */
  protected void setUp () {
    overrideHooksAdd();
    RegistryReset.internal_resetRegistryAndAddTestSubjects();
    GrouperTest.initGroupsAndAttributes();

    grouperSession     = SessionHelper.getRootSession();
    root  = StemHelper.findRootStem(grouperSession);
    edu   = StemHelper.addChildStem(root, "edu", "education");
    group = StemHelper.addChildGroup(this.edu, "group", "the group");
  }

  /**
   * 
   */
  private void overrideHooksAdd() {
    //this is the test hook imple
    GrouperHookType.addHookOverride(GrouperHookType.MEMBER.getPropertyFileKey(), 
        MemberHooksImpl.class);
  }

  /**
   * @throws Exception 
   */
  public void testMemberPostCommitInsert() throws Exception {

    MemberHooksImpl.mostRecentPostCommitInsertMemberSubjectId = null;

    GrouperTransaction.callbackGrouperTransaction(new GrouperTransactionHandler() {
  
      public Object callback(GrouperTransaction grouperTransaction)
          throws GrouperDAOException {
        
        try {
          group.addMember(SubjectTestHelper.SUBJ2);
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
        
        assertNull("shouldnt fire yet", MemberHooksImpl.mostRecentPostCommitInsertMemberSubjectId);
        grouperTransaction.commit(GrouperCommitType.COMMIT_NOW);
        
        assertEquals(SubjectTestHelper.SUBJ2.getId(), MemberHooksImpl.mostRecentPostCommitInsertMemberSubjectId);
        return null;
      }
    });    
    
  }

    
  /**
   * @throws MemberAddException 
   * @throws InsufficientPrivilegeException 
   * @throws MemberNotFoundException 
   * 
   */
  public void testMemberPostCommitUpdate() throws MemberAddException, InsufficientPrivilegeException,
      MemberNotFoundException {
    
    group.addMember(SubjectTestHelper.SUBJ0);
  
    final Member member0 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ0, true);
  
    MemberHooksImpl.mostRecentPostCommitUpdateMemberSubjectId = null;
  
    GrouperTransaction.callbackGrouperTransaction(new GrouperTransactionHandler() {
  
      public Object callback(GrouperTransaction grouperTransaction)
          throws GrouperDAOException {
        
        try {
          member0.setSubjectId("whatever3");
          member0.store();
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
        
        assertNull("shouldnt fire yet", MemberHooksImpl.mostRecentPostCommitUpdateMemberSubjectId);
        grouperTransaction.commit(GrouperCommitType.COMMIT_NOW);
        
        assertEquals("whatever3", MemberHooksImpl.mostRecentPostCommitUpdateMemberSubjectId);
        return null;
      }
    });    
  
    
  }

  /**
   * @throws MemberAddException 
   * @throws InsufficientPrivilegeException 
   * @throws MemberNotFoundException 
   * 
   */
  public void testMemberPostCommitDelete() throws MemberAddException, InsufficientPrivilegeException,
      MemberNotFoundException {
    
  
    final Member member0 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ0, true);
  
    MemberHooksImpl.mostRecentPostCommitDeleteMemberSubjectId = null;
  
    GrouperTransaction.callbackGrouperTransaction(new GrouperTransactionHandler() {
  
      public Object callback(GrouperTransaction grouperTransaction)
          throws GrouperDAOException {
        
        HibernateSession.byObjectStatic().delete(member0);
        
        assertNull("shouldnt fire yet", MemberHooksImpl.mostRecentPostCommitDeleteMemberSubjectId);
        grouperTransaction.commit(GrouperCommitType.COMMIT_NOW);
        
        assertEquals(SubjectTestHelper.SUBJ0_ID, MemberHooksImpl.mostRecentPostCommitDeleteMemberSubjectId);
        return null;
      }
    });    
  
    
  }

  /**
   * @throws MemberAddException 
   * @throws InsufficientPrivilegeException 
   * @throws MemberNotFoundException 
   * 
   */
  public void testMemberPostCommitChange() throws MemberAddException, InsufficientPrivilegeException,
      MemberNotFoundException {

    final Member member0 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ0, true);

    MemberHooksImpl.mostRecentPostCommitChangeMemberSubjectId = null;

    GrouperTransaction.callbackGrouperTransaction(new GrouperTransactionHandler() {
  
      public Object callback(GrouperTransaction grouperTransaction)
          throws GrouperDAOException {
        
        member0.changeSubject(SubjectTestHelper.SUBJ1);
        
        assertNull("shouldnt fire yet", MemberHooksImpl.mostRecentPostCommitChangeMemberSubjectId);
        grouperTransaction.commit(GrouperCommitType.COMMIT_NOW);
        
        assertEquals(SubjectTestHelper.SUBJ0_ID, MemberHooksImpl.mostRecentPostCommitChangeMemberSubjectId);
        return null;
      }
    });    
    
  }

  /**
   * @throws MemberAddException 
   * @throws InsufficientPrivilegeException 
   * @throws MemberNotFoundException 
   * 
   */
  public void testMemberPreDelete() throws MemberAddException, InsufficientPrivilegeException,
      MemberNotFoundException {
    
    MemberHooksImpl.mostRecentPreDeleteMemberSubjectId = null;
    
    Member member = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ0, true);
    HibernateSession.byObjectStatic().delete(member);
    
    assertEquals(SubjectTestHelper.SUBJ0.getId(), MemberHooksImpl.mostRecentPreDeleteMemberSubjectId);
    
    member = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ5, true);
    
    try {
      HibernateSession.byObjectStatic().delete(member);
      fail("Should veto subj5");
    } catch (HookVeto hookVeto) {
      assertEquals("subjectId cannot be " + SubjectTestHelper.SUBJ5.getId(), hookVeto.getReason());
      assertEquals(VetoTypeGrouper.MEMBER_PRE_DELETE, hookVeto.getVetoType());
    }
    
  }

  /**
   * @throws MemberAddException 
   * @throws InsufficientPrivilegeException 
   * @throws MemberNotFoundException 
   * @throws MemberDeleteException 
   * 
   */
  public void testMemberPreChange() throws MemberAddException, InsufficientPrivilegeException,
      MemberNotFoundException, MemberDeleteException {
    
    MemberHooksImpl.mostRecentPreChangeMemberSubjectId = null;
    
    group.addMember(SubjectTestHelper.SUBJ0);
    
    Member member = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ0, true);

    member.changeSubject(SubjectTestHelper.SUBJ1);
    
    assertEquals(SubjectTestHelper.SUBJ0.getId(), MemberHooksImpl.mostRecentPreChangeMemberSubjectId);
    
    group.deleteMember(group.getMembers().iterator().next());
    
    group.addMember(SubjectTestHelper.SUBJ8);
    member = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ8, true);
    try {
      member.changeSubject(SubjectTestHelper.SUBJ9);
      fail("Should veto subj8");
    } catch (HookVeto hookVeto) {
      assertEquals("subjectId cannot be " + SubjectTestHelper.SUBJ8.getId(), hookVeto.getReason());
      assertEquals(VetoTypeGrouper.MEMBER_PRE_CHANGE_SUBJECT, hookVeto.getVetoType());
    }
    member = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ8, true);
    assertEquals(SubjectTestHelper.SUBJ8_ID, member.getSubjectId());
    Set<Membership> memberships = group.getMemberships();
    assertEquals(1, memberships.size());
    assertEquals(SubjectTestHelper.SUBJ8_ID, ((Membership)memberships.toArray()[0]).getMember().getSubjectId());
    
  }

  /**
   * @throws MemberAddException 
   * @throws InsufficientPrivilegeException 
   * @throws MemberNotFoundException 
   * @throws MemberDeleteException 
   * 
   */
  public void testMemberPostChange() throws MemberAddException, InsufficientPrivilegeException,
      MemberNotFoundException, MemberDeleteException {
    
    MemberHooksImpl.mostRecentPostChangeMemberSubjectId = null;
    
    group.addMember(SubjectTestHelper.SUBJ0);
    
    Member member = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ0, true);
  
    member.changeSubject(SubjectTestHelper.SUBJ1);
    
    assertEquals(SubjectTestHelper.SUBJ0.getId(), MemberHooksImpl.mostRecentPostChangeMemberSubjectId);
    
    group.deleteMember(group.getMembers().iterator().next());
    
    group.addMember(SubjectTestHelper.SUBJ7);
    member = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ7, true);
    try {
      member.changeSubject(SubjectTestHelper.SUBJ9);
      fail("Should veto subj7");
    } catch (HookVeto hookVeto) {
      assertEquals("subjectId cannot be " + SubjectTestHelper.SUBJ7.getId(), hookVeto.getReason());
      assertEquals(VetoTypeGrouper.MEMBER_POST_CHANGE_SUBJECT, hookVeto.getVetoType());
    }
    member = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ7, true);
    assertEquals(SubjectTestHelper.SUBJ7_ID, member.getSubjectId());
    Set<Membership> memberships = group.getMemberships();
    assertEquals(1, memberships.size());
    assertEquals(SubjectTestHelper.SUBJ7_ID, ((Membership)memberships.toArray()[0]).getMember().getSubjectId());
    
  }

}
