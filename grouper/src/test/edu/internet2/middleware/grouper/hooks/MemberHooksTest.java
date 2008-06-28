/*
 * @author mchyzer
 * $Id: MemberHooksTest.java,v 1.1 2008-06-28 06:55:47 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks;

import java.util.Set;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.GrouperTest;
import edu.internet2.middleware.grouper.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberAddException;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.MemberNotFoundException;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.RegistryReset;
import edu.internet2.middleware.grouper.SessionHelper;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemHelper;
import edu.internet2.middleware.grouper.SubjectTestHelper;
import edu.internet2.middleware.grouper.hooks.logic.GrouperHookType;
import edu.internet2.middleware.grouper.hooks.logic.HookVeto;
import edu.internet2.middleware.grouper.hooks.logic.VetoTypeGrouper;


/**
 *
 */
public class MemberHooksTest extends GrouperTest {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new MemberHooksTest("testMemberPostUpdate"));
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
    
    Member member0 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ0);
    
    MemberHooksImpl.mostRecentPreUpdateMemberSubjectId = null;
    
    member0.setSubjectId("whatever");
    
    assertEquals("whatever", MemberHooksImpl.mostRecentPreUpdateMemberSubjectId);
    
    group.addMember(SubjectTestHelper.SUBJ2);
    Member member2 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ2);
    try {
      member2.setSubjectId("whatever2");
      fail("Should veto subj2");
    } catch (HookVeto hookVeto) {
      assertEquals("subjectId cannot be whatever2", hookVeto.getReason());
      assertEquals(VetoTypeGrouper.MEMBER_PRE_UPDATE, hookVeto.getVetoType());
    }
    member2 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ2);
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
    
    Member member0 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ0);
    
    MemberHooksImpl.mostRecentPreUpdateMemberSubjectId = null;
    
    member0.setSubjectId("whatever3");
    
    assertEquals("whatever3", MemberHooksImpl.mostRecentPreUpdateMemberSubjectId);
    
    group.addMember(SubjectTestHelper.SUBJ2);
    Member member2 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ2);
    try {
      member2.setSubjectId("whatever4");
      fail("Should veto subj2");
    } catch (HookVeto hookVeto) {
      assertEquals("subjectId cannot be whatever4", hookVeto.getReason());
      assertEquals(VetoTypeGrouper.MEMBER_POST_UPDATE, hookVeto.getVetoType());
    }
    member2 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ2);
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
   * @see edu.internet2.middleware.grouper.GrouperTest#tearDown()
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
   * @see edu.internet2.middleware.grouper.GrouperTest#setUp()
   */
  protected void setUp () {
    overrideHooksAdd();
    RegistryReset.internal_resetRegistryAndAddTestSubjects();
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

}
