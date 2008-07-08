/*
 * @author mchyzer
 * $Id: MembershipHooksTest.java,v 1.7 2008-07-08 20:47:42 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks;

import java.util.Set;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.GrouperTest;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.RegistryReset;
import edu.internet2.middleware.grouper.SessionHelper;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemHelper;
import edu.internet2.middleware.grouper.SubjectTestHelper;
import edu.internet2.middleware.grouper.hooks.logic.GrouperHookType;
import edu.internet2.middleware.grouper.hooks.logic.HookVeto;
import edu.internet2.middleware.grouper.hooks.logic.VetoTypeGrouper;
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
    TestRunner.run(new MembershipHooksTest("testMemberPreAddMemberAsync2"));
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
    GrouperHookType.addHookOverride(GrouperHookType.MEMBERSHIP.getPropertyFileKey(), (Class<?>)null);
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.GrouperTest#setUp()
   */
  protected void setUp () {
    RegistryReset.internal_resetRegistryAndAddTestSubjects();
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

    MembershipHooksImpl.mostRecentInsertMemberSubjectId = null;
    
    group.addMember(SubjectTestHelper.SUBJ0);
    Set<Membership> memberships = group.getMemberships();
    assertEquals(SubjectTestHelper.SUBJ0.getId(), ((Membership)memberships.toArray()[0]).getMember().getSubjectId());
    assertEquals(SubjectTestHelper.SUBJ0.getId(), MembershipHooksImpl.mostRecentInsertMemberSubjectId);
    
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
  public void testMemberPreAddMemberAsync() throws Exception {
    
    //this is the test hook imple
    GrouperHookType.addHookOverride(GrouperHookType.MEMBERSHIP.getPropertyFileKey(), MembershipHooksImplAsync.class);

    MembershipHooksImplAsync.mostRecentInsertMemberSubjectId = null;
    
    int currentSleepCount = MembershipHooksImplAsync.preAddMemberHookCountAyncSeconds;
    
    group.addMember(SubjectTestHelper.SUBJ0);
    
    GrouperUtil.sleep(500);
    
    assertEquals("Monitor the progress of the hook in another thread", currentSleepCount+1, MembershipHooksImplAsync.preAddMemberHookCountAyncSeconds);
    
    GrouperUtil.sleep(1000);
    
    assertEquals("Monitor the progress of the hook in another thread", currentSleepCount+2, MembershipHooksImplAsync.preAddMemberHookCountAyncSeconds);
    
    Set<Membership> memberships = group.getMemberships();
    assertEquals(SubjectTestHelper.SUBJ0.getId(), ((Membership)memberships.toArray()[0]).getMember().getSubjectId());
    assertEquals(SubjectTestHelper.SUBJ0.getId(), MembershipHooksImplAsync.mostRecentInsertMemberSubjectId);
    
    GrouperUtil.sleep(1000);
    assertTrue(MembershipHooksImplAsync.done);
    if (MembershipHooksImplAsync.problem != null) {
      throw MembershipHooksImplAsync.problem;
    }
  }

  /**
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

    MembershipHooksImpl3.mostRecentInsertMemberSubjectId = null;
    
    group.addMember(SubjectTestHelper.SUBJ0);
    Set<Membership> memberships = group.getMemberships();
    assertEquals(SubjectTestHelper.SUBJ0.getId(), ((Membership)memberships.toArray()[0]).getMember().getSubjectId());
    assertEquals(SubjectTestHelper.SUBJ0.getId(), MembershipHooksImpl3.mostRecentInsertMemberSubjectId);
    
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
  public void testMemberPostRemoveMember() throws Exception {
    
    //this is the test hook imple
    GrouperHookType.addHookOverride(GrouperHookType.MEMBERSHIP.getPropertyFileKey(), 
        MembershipHooksImpl8.class);

    
    group.addMember(SubjectTestHelper.SUBJ0);
    
    Set<Membership> memberships = group.getMemberships();
    assertEquals(SubjectTestHelper.SUBJ0.getId(), ((Membership)memberships.toArray()[0]).getMember().getSubjectId());

    MembershipHooksImpl8.mostRecentDeleteMemberSubjectId = null;

    group.deleteMember(SubjectTestHelper.SUBJ0);
    
    memberships = group.getMemberships();

    assertEquals(0, memberships.size());

    assertEquals(SubjectTestHelper.SUBJ0.getId(), MembershipHooksImpl8.mostRecentDeleteMemberSubjectId);
    
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
