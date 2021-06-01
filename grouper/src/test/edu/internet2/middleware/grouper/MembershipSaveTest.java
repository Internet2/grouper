package edu.internet2.middleware.grouper;

import org.junit.Assert;

import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.misc.SaveMode;
import edu.internet2.middleware.grouper.misc.SaveResultType;
import junit.textui.TestRunner;

public class MembershipSaveTest extends GrouperTest {
  
  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new MembershipSaveTest("testUpdateMembership"));
  }
  
  /**
   * @param name
   */
  public MembershipSaveTest(String name) {
    super(name);
  }

  /**
   * 
   */
  public MembershipSaveTest() {
    super();
  }
  
  
  public void testSaveMembership() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();

    Group group1 = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:testGroup1").save();
    
    MembershipSave membershipSave = new MembershipSave().assignGroup(group1).assignSubject(SubjectTestHelper.SUBJ0);
    membershipSave.save();
    
    Assert.assertEquals(SaveResultType.INSERT, membershipSave.getSaveResultType());
    Assert.assertTrue(group1.hasMember(SubjectTestHelper.SUBJ0));
    
  }
  
  public void testDeleteMembership() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();

    Group group1 = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:testGroup1").save();
    
    MembershipSave membershipSave = new MembershipSave().assignGroup(group1).assignSubject(SubjectTestHelper.SUBJ0);
    membershipSave.save();
    
    Assert.assertEquals(SaveResultType.INSERT, membershipSave.getSaveResultType());
    Assert.assertTrue(group1.hasMember(SubjectTestHelper.SUBJ0));
    
    membershipSave = new MembershipSave().assignGroup(group1).assignSubject(SubjectTestHelper.SUBJ0).assignSaveMode(SaveMode.DELETE);
    membershipSave.save();
    
    Assert.assertEquals(SaveResultType.DELETE, membershipSave.getSaveResultType());
    Assert.assertFalse(group1.hasMember(SubjectTestHelper.SUBJ0));
    
  }

  
  public void testSaveMembershipNoChange() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();

    Group group1 = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:testGroup1").save();
    
    MembershipSave membershipSave = new MembershipSave().assignGroup(group1).assignSubject(SubjectTestHelper.SUBJ0);
    membershipSave.save();
    
    Assert.assertEquals(SaveResultType.INSERT, membershipSave.getSaveResultType());
    Assert.assertTrue(group1.hasMember(SubjectTestHelper.SUBJ0));
    
    membershipSave = new MembershipSave().assignGroup(group1).assignSubject(SubjectTestHelper.SUBJ0);
    membershipSave.save();
    
    Assert.assertEquals(SaveResultType.NO_CHANGE, membershipSave.getSaveResultType());
    Assert.assertTrue(group1.hasMember(SubjectTestHelper.SUBJ0));
    
  }
  
  public void testUpdateMembership() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();

    Group group1 = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:testGroup1").save();
    
    MembershipSave membershipSave = new MembershipSave().assignGroup(group1).assignSubject(SubjectTestHelper.SUBJ0);
    membershipSave.save();
    
    Assert.assertEquals(SaveResultType.INSERT, membershipSave.getSaveResultType());
    Assert.assertTrue(group1.hasMember(SubjectTestHelper.SUBJ0));
    
    long currentTimeMillis = System.currentTimeMillis();
    membershipSave = new MembershipSave().assignGroup(group1).assignSubject(SubjectTestHelper.SUBJ0)
        .assignImmediateMshipEnabledTime(currentTimeMillis)
        .assignImmediateMshipDisabledTime(currentTimeMillis + 99999);
    membershipSave.save();
    
    Assert.assertEquals(SaveResultType.UPDATE, membershipSave.getSaveResultType());
    Assert.assertTrue(group1.hasMember(SubjectTestHelper.SUBJ0));
    
  }
}
