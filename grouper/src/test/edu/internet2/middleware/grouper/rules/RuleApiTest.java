/**
 * 
 */
package edu.internet2.middleware.grouper.rules;

import java.util.Date;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.misc.SaveMode;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;


/**
 * test rule api
 * @author mchyzer
 */
public class RuleApiTest extends GrouperTest {

  /**
   * @param name
   */
  public RuleApiTest(String name) {
    super(name);
  }

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new RuleApiTest("testGroupIntersectionDate"));
  }

  /**
   * 
   */
  public void testGroupIntersection() {
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Group groupA = new GroupSave(grouperSession).assignSaveMode(SaveMode.INSERT_OR_UPDATE).assignName("stem:a").assignCreateParentStemsIfNotExist(true).save();
    groupA.grantPriv(SubjectTestHelper.SUBJ9, AccessPrivilege.ADMIN, false);

    Group groupB = new GroupSave(grouperSession).assignSaveMode(SaveMode.INSERT_OR_UPDATE).assignName("stem:b").assignCreateParentStemsIfNotExist(true).save();
    groupB.grantPriv(SubjectTestHelper.SUBJ9, AccessPrivilege.READ, false);
    
    RuleApi.groupIntersection(SubjectTestHelper.SUBJ9, groupA, groupB);
    
    groupB.addMember(SubjectTestHelper.SUBJ0);

    //count rule firings
    long initialFirings = RuleEngine.ruleFirings;
    
    //doesnt do anything
    groupB.deleteMember(SubjectTestHelper.SUBJ0);

    assertEquals(initialFirings, RuleEngine.ruleFirings);

    groupB.addMember(SubjectTestHelper.SUBJ0);
    groupA.addMember(SubjectTestHelper.SUBJ0);
    
    //count rule firings
    
    groupB.deleteMember(SubjectTestHelper.SUBJ0);
    
    assertEquals(initialFirings+1, RuleEngine.ruleFirings);

    //should come out of groupA
    assertFalse(groupA.hasMember(SubjectTestHelper.SUBJ0));

    
    // grouperSession = GrouperSession.startRootSession();
    // groupA = new GroupSave(grouperSession).assignName("stem:a").assignCreateParentStemsIfNotExist(true).save();
    // actAsSubject = SubjectFinder.findById("test.subject.9", true);
    // groupA.grantPriv(actAsSubject, AccessPrivilege.ADMIN, false);
    // groupB = new GroupSave(grouperSession).assignName("stem:b").assignCreateParentStemsIfNotExist(true).save();
    // groupB.grantPriv(actAsSubject, AccessPrivilege.READ, false);
    // RuleApi.groupIntersection(actAsSubject, groupA, groupB);
    // addMember("stem:a", "test.subject.0");
    // addMember("stem:b", "test.subject.0");
    // delMember("stem:b", "test.subject.0");
    // hasMember("stem:a", "test.subject.0");

  }

  /**
   * 
   */
  public void testGroupIntersectionDate() {
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Group groupA = new GroupSave(grouperSession).assignSaveMode(SaveMode.INSERT_OR_UPDATE).assignName("stem:a").assignCreateParentStemsIfNotExist(true).save();
    groupA.grantPriv(SubjectTestHelper.SUBJ9, AccessPrivilege.ADMIN, false);
  
    Group groupB = new GroupSave(grouperSession).assignSaveMode(SaveMode.INSERT_OR_UPDATE).assignName("stem:b").assignCreateParentStemsIfNotExist(true).save();
    groupB.grantPriv(SubjectTestHelper.SUBJ9, AccessPrivilege.READ, false);
    
    RuleApi.groupIntersection(SubjectTestHelper.SUBJ9, groupA, groupB, 5);
    
    groupB.addMember(SubjectTestHelper.SUBJ0);
  
    //count rule firings
    long initialFirings = RuleEngine.ruleFirings;
    
    //doesnt do anything
    groupB.deleteMember(SubjectTestHelper.SUBJ0);
  
    assertEquals(initialFirings, RuleEngine.ruleFirings);
  
    groupB.addMember(SubjectTestHelper.SUBJ0);
    groupA.addMember(SubjectTestHelper.SUBJ0);
    
    //count rule firings
    
    groupB.deleteMember(SubjectTestHelper.SUBJ0);
    
    assertEquals(initialFirings+1, RuleEngine.ruleFirings);
  
    //should have a disabled date in group A
    assertTrue(groupA.hasMember(SubjectTestHelper.SUBJ0));

    Member member0 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ0, false);
    
    Membership membership = groupA.getImmediateMembership(Group.getDefaultList(), member0, true, true);
    
    assertNotNull(membership.getDisabledTime());
    long disabledTime = membership.getDisabledTime().getTime();
    
    assertTrue("More than 6 days: " + new Date(disabledTime), disabledTime > System.currentTimeMillis() + (4 * 24 * 60 * 60 * 1000));
    assertTrue("Less than 8 days: " + new Date(disabledTime), disabledTime < System.currentTimeMillis() + (6 * 24 * 60 * 60 * 1000));

    
    // grouperSession = GrouperSession.startRootSession();
    // groupA = new GroupSave(grouperSession).assignName("stem:a").assignCreateParentStemsIfNotExist(true).save();
    // actAsSubject = SubjectFinder.findById("test.subject.9", true);
    // groupA.grantPriv(actAsSubject, AccessPrivilege.ADMIN, false);
    // groupB = new GroupSave(grouperSession).assignName("stem:b").assignCreateParentStemsIfNotExist(true).save();
    // groupB.grantPriv(actAsSubject, AccessPrivilege.READ, false);
    // RuleApi.groupIntersection(actAsSubject, groupA, groupB, 7);
    // addMember("stem:a", "test.subject.0");
    // addMember("stem:b", "test.subject.0");
    // delMember("stem:b", "test.subject.0");
    // subject0 = SubjectFinder.findById("test.subject.0", true);
    // member0 = MemberFinder.findBySubject(grouperSession, subject0, false);
    // groupA.getImmediateMembership(Group.getDefaultList(), member0, true, true).getDisabledTime()
  
  }
  
}
