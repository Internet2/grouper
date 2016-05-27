/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper;

import java.util.Set;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 *
 */
public class MembershipFinderTest extends GrouperTest {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new MembershipFinderTest("testFindMembership"));
  }
  
  /**
   * @param name
   */
  public MembershipFinderTest(String name) {
    super(name);
  }

  /**
   * 
   */
  public MembershipFinderTest() {
    super();
    
  }

  /**
   * 
   */
  public void testMembershipSize() {
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Group group1 = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:testGroup1").save();
    Group group2 = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:testGroup2").save();
    group1.addMember(SubjectTestHelper.SUBJ0);
    group2.addMember(SubjectTestHelper.SUBJ1);
    group1.addMember(group2.toSubject());
    
    QueryOptions queryOptions = new QueryOptions().retrieveResults(false).retrieveCount(true);
    
    Set<Member> members = group1.getMembers(Group.getDefaultList(), queryOptions);

    
    //MembershipResult membershipResult = new MembershipFinder().addGroup(group1).addField(Group.getDefaultList()).assignQueryOptionsForMember(queryOptions).findMembershipResult();
    assertEquals(0, GrouperUtil.length(members));
    assertEquals(3, queryOptions.getCount().intValue());
  }  
  
  /**
   * 
   */
  public void testFindMembership() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Group group1 = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:testGroup1").save();
    Group group2 = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:testGroup2").save();
    group1.addMember(SubjectTestHelper.SUBJ0);
    group2.addMember(SubjectTestHelper.SUBJ1);
    group1.addMember(group2.toSubject());
    
    Membership membership1 = new MembershipFinder().addGroup(group1).addSubject(SubjectTestHelper.SUBJ0).findMembership(true);
    Membership membership2 = new MembershipFinder().addGroup(group2).addSubject(SubjectTestHelper.SUBJ1).findMembership(true);
    
    assertNotNull(membership1);
    assertEquals(membership1.getGroupName(), group1.getName());
    assertEquals(membership1.getMemberSubjectId(), SubjectTestHelper.SUBJ0_ID);
    
    membership1 = new MembershipFinder().addMembershipId(membership1.getUuid()).findMembership(true);

    assertNotNull(membership1);
    assertEquals(membership1.getGroupName(), group1.getName());
    assertEquals(membership1.getMemberSubjectId(), SubjectTestHelper.SUBJ0_ID);
    
    
    assertEquals(2, new MembershipFinder().addMembershipId(membership1.getUuid()).addMembershipId(membership2.getUuid()).findMembershipsMembers().size());
    
  }
  
  
}
