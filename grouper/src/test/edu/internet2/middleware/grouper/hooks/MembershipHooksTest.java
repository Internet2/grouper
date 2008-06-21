/*
 * @author mchyzer
 * $Id: MembershipHooksTest.java,v 1.2 2008-06-21 04:16:13 mchyzer Exp $
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


/**
 *
 */
public class MembershipHooksTest extends GrouperTest {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new MembershipHooksTest("testMemberPreInsert"));
  }
  
  /**
   * @param name
   */
  public MembershipHooksTest(String name) {
    super(name);
  }

  /**
   * @throws Exception 
   */
  public void testMemberPreInsert() throws Exception {
    
    group.addMember(SubjectTestHelper.SUBJ0);
    Set<Membership> memberships = group.getMemberships();
    assertEquals(SubjectTestHelper.SUBJ0.getId(), ((Membership)memberships.toArray()[0]).getMember().getSubjectId());
    assertEquals(SubjectTestHelper.SUBJ0.getId(), MembershipHooksImpl.getMostRecentInsertMemberSubjectId());
    
    try {
      group.addMember(SubjectTestHelper.SUBJ1);
      fail("Should veto subj1");
    } catch (HookVeto hookVeto) {
      assertEquals("subject cannot be subj1", hookVeto.getReason());
      assertEquals(VetoTypeGrouper.MEMBERSHIP_PRE_INSERT, hookVeto.getVetoType());
    }
    
  }

  /** edu stem */
  private Stem edu;
  
  /** root stem */
  private Stem root;
  
  /** grouper sesion */
  private GrouperSession grouperSession; 

  /** test group */
  private Group group;
  
  /**
   * @see edu.internet2.middleware.grouper.GrouperTest#tearDown()
   */
  @Override
  protected void tearDown() {
    //dont have the test hook imple
    GrouperHookType.addHookOverride(GrouperHookType.MEMBERSHIP.getPropertyFileKey(), null);
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.GrouperTest#setUp()
   */
  protected void setUp () {
    //this is the test hook imple
    GrouperHookType.addHookOverride(GrouperHookType.MEMBERSHIP.getPropertyFileKey(), MembershipHooksImpl.class);
    RegistryReset.internal_resetRegistryAndAddTestSubjects();
    grouperSession     = SessionHelper.getRootSession();
    root  = StemHelper.findRootStem(grouperSession);
    edu   = StemHelper.addChildStem(root, "edu", "education");
    group = StemHelper.addChildGroup(this.edu, "group", "the group");

  }

}
