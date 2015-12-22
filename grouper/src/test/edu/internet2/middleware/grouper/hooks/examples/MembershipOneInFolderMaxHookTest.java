/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.hooks.examples;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.cache.EhcacheController;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.hooks.logic.GrouperHookType;
import edu.internet2.middleware.grouper.hooks.logic.HookVeto;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.AttributeDefPrivilege;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
import edu.internet2.middleware.grouper.registry.RegistryReset;


/**
 *
 */
public class MembershipOneInFolderMaxHookTest extends GrouperTest {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new MembershipOneInFolderMaxHookTest("testVetoIfNotAllowed"));
  }
  
  /**
   * 
   */
  public MembershipOneInFolderMaxHookTest() {
    
  }
  
  /**
   * 
   * @param testName
   */
  public MembershipOneInFolderMaxHookTest(String testName) {
    super(testName);
  }
  
  /**
   * Test method for {@link edu.internet2.middleware.grouper.hooks.examples.MembershipOneInFolderMaxHook#membershipPreAddMember(edu.internet2.middleware.grouper.hooks.beans.HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksMembershipChangeBean)}.
   */
  public void testGroupRemovesMembership() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();

    //allow all to attribute
    MembershipOneInFolderMaxHook.membershipOneInFolderAttributeDefName().getAttributeDef().getPrivilegeDelegate().grantPriv(SubjectFinder.findAllSubject(), AttributeDefPrivilege.ATTR_READ, false);
    MembershipOneInFolderMaxHook.membershipOneInFolderAttributeDefName().getAttributeDef().getPrivilegeDelegate().grantPriv(SubjectFinder.findAllSubject(), AttributeDefPrivilege.ATTR_UPDATE, false);
    
    //has the attribute
    Stem testStem1 = new StemSave(grouperSession).assignName("test1").save();

    testStem1.grantPriv(SubjectTestHelper.SUBJ0, NamingPrivilege.CREATE);
    testStem1.grantPriv(SubjectTestHelper.SUBJ0, NamingPrivilege.STEM);

    testStem1.grantPriv(SubjectTestHelper.SUBJ1, NamingPrivilege.CREATE);
    testStem1.grantPriv(SubjectTestHelper.SUBJ1, NamingPrivilege.STEM);

    GrouperSession.stopQuietly(grouperSession);

    grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);

    Group group1A = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true)
        .assignName("test1:someGroupA").save();

    group1A.grantPriv(SubjectTestHelper.SUBJ1, AccessPrivilege.ADMIN, false);
    
    Group group1B = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true)
        .assignName("test1:someGroupB").save();

    group1B.grantPriv(SubjectTestHelper.SUBJ1, AccessPrivilege.ADMIN, false);

    //apply the attribute
    MembershipOneInFolderMaxHook.assignMembershipOneInFolderAttributeDefName(testStem1);
    
    group1A.addMember(SubjectTestHelper.SUBJ5);
    group1A.addMember(SubjectTestHelper.SUBJ6);
    group1B.addMember(SubjectTestHelper.SUBJ8);
    group1B.addMember(SubjectTestHelper.SUBJ5);

    assertFalse(group1A.hasMember(SubjectTestHelper.SUBJ5));
    assertTrue(group1B.hasMember(SubjectTestHelper.SUBJ5));
    assertTrue(group1A.hasMember(SubjectTestHelper.SUBJ6));
    assertTrue(group1B.hasMember(SubjectTestHelper.SUBJ8));

    //add same again, should be fine
    group1B.addMember(SubjectTestHelper.SUBJ5, false);
    assertTrue(group1B.hasMember(SubjectTestHelper.SUBJ5));
    
    grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ1);

    //try as another user
    group1A.addMember(SubjectTestHelper.SUBJ5);

    assertFalse(group1B.hasMember(SubjectTestHelper.SUBJ5));
    assertTrue(group1A.hasMember(SubjectTestHelper.SUBJ5));
    assertTrue(group1A.hasMember(SubjectTestHelper.SUBJ6));
    assertTrue(group1B.hasMember(SubjectTestHelper.SUBJ8));

  }

  /**
   * Test method for {@link edu.internet2.middleware.grouper.hooks.examples.MembershipOneInFolderMaxHook#membershipPreAddMember(edu.internet2.middleware.grouper.hooks.beans.HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksMembershipChangeBean)}.
   */
  public void testVetoIfNotAllowed() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();

    //allow all to attribute
    MembershipOneInFolderMaxHook.membershipOneInFolderAttributeDefName().getAttributeDef().getPrivilegeDelegate().grantPriv(SubjectFinder.findAllSubject(), AttributeDefPrivilege.ATTR_READ, false);
    MembershipOneInFolderMaxHook.membershipOneInFolderAttributeDefName().getAttributeDef().getPrivilegeDelegate().grantPriv(SubjectFinder.findAllSubject(), AttributeDefPrivilege.ATTR_UPDATE, false);
    
    //has the attribute
    Stem testStem1 = new StemSave(grouperSession).assignName("test1").save();

    testStem1.grantPriv(SubjectTestHelper.SUBJ0, NamingPrivilege.CREATE);
    testStem1.grantPriv(SubjectTestHelper.SUBJ0, NamingPrivilege.STEM);

    testStem1.grantPriv(SubjectTestHelper.SUBJ1, NamingPrivilege.CREATE);
    testStem1.grantPriv(SubjectTestHelper.SUBJ1, NamingPrivilege.STEM);

    GrouperSession.stopQuietly(grouperSession);

    grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);

    Group group1A = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true)
        .assignName("test1:someGroupA").save();

    Group group1B = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true)
        .assignName("test1:someGroupB").save();

    //apply the attribute
    MembershipOneInFolderMaxHook.assignMembershipOneInFolderAttributeDefName(testStem1);
    
    group1A.addMember(SubjectTestHelper.SUBJ5);
    group1A.addMember(SubjectTestHelper.SUBJ6);
    group1B.addMember(SubjectTestHelper.SUBJ8);
    
    grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ1);

    Group group1C = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true)
        .assignName("test1:someGroupC").save();

    try {
      
      group1C.addMember(SubjectTestHelper.SUBJ5);

    } catch (HookVeto hv) {
      //this is a success, it is supposed to veto  
      assertEquals(hv.getReasonKey(), MembershipOneInFolderMaxHook.HOOK_VETO_MEMBERSHIP_ONE_IN_FOLDER_CANT_DELETE_MEMBER);
    }

    assertFalse(group1B.hasMember(SubjectTestHelper.SUBJ5));
    assertTrue(group1A.hasMember(SubjectTestHelper.SUBJ5));
    assertTrue(group1A.hasMember(SubjectTestHelper.SUBJ6));
    assertTrue(group1B.hasMember(SubjectTestHelper.SUBJ8));

  }

  /**
   * Test method for {@link edu.internet2.middleware.grouper.hooks.examples.MembershipOneInFolderMaxHook#membershipPreAddMember(edu.internet2.middleware.grouper.hooks.beans.HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksMembershipChangeBean)}.
   */
  public void testGroupRemovesMultipleMemberships() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();

    //allow all to attribute
    MembershipOneInFolderMaxHook.membershipOneInFolderAttributeDefName().getAttributeDef().getPrivilegeDelegate().grantPriv(SubjectFinder.findAllSubject(), AttributeDefPrivilege.ATTR_READ, false);
    MembershipOneInFolderMaxHook.membershipOneInFolderAttributeDefName().getAttributeDef().getPrivilegeDelegate().grantPriv(SubjectFinder.findAllSubject(), AttributeDefPrivilege.ATTR_UPDATE, false);
    
    //has the attribute
    Stem testStem1 = new StemSave(grouperSession).assignName("test1").save();

    testStem1.grantPriv(SubjectTestHelper.SUBJ0, NamingPrivilege.CREATE);
    testStem1.grantPriv(SubjectTestHelper.SUBJ0, NamingPrivilege.STEM);

    testStem1.grantPriv(SubjectTestHelper.SUBJ1, NamingPrivilege.CREATE);
    testStem1.grantPriv(SubjectTestHelper.SUBJ1, NamingPrivilege.STEM);

    GrouperSession.stopQuietly(grouperSession);

    grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);

    Group group1A = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true)
        .assignName("test1:someGroupA").save();

    group1A.grantPriv(SubjectTestHelper.SUBJ1, AccessPrivilege.ADMIN, false);
    
    Group group1B = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true)
        .assignName("test1:someGroupB").save();

    group1B.grantPriv(SubjectTestHelper.SUBJ1, AccessPrivilege.ADMIN, false);

    Group group1C = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true)
        .assignName("test1:someGroupC").save();

    group1C.grantPriv(SubjectTestHelper.SUBJ1, AccessPrivilege.ADMIN, false);

    group1A.addMember(SubjectTestHelper.SUBJ5);
    group1A.addMember(SubjectTestHelper.SUBJ6);
    group1B.addMember(SubjectTestHelper.SUBJ8);
    group1B.addMember(SubjectTestHelper.SUBJ5);

    //apply the attribute
    MembershipOneInFolderMaxHook.assignMembershipOneInFolderAttributeDefName(testStem1);

    group1C.addMember(SubjectTestHelper.SUBJ5);

    assertFalse(group1A.hasMember(SubjectTestHelper.SUBJ5));
    assertFalse(group1B.hasMember(SubjectTestHelper.SUBJ5));
    assertTrue(group1A.hasMember(SubjectTestHelper.SUBJ6));
    assertTrue(group1B.hasMember(SubjectTestHelper.SUBJ8));
    assertTrue(group1C.hasMember(SubjectTestHelper.SUBJ5));

    grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ1);

    //try as another user
    group1A.addMember(SubjectTestHelper.SUBJ5);

    assertFalse(group1B.hasMember(SubjectTestHelper.SUBJ5));
    assertFalse(group1C.hasMember(SubjectTestHelper.SUBJ5));
    assertTrue(group1A.hasMember(SubjectTestHelper.SUBJ5));
    assertTrue(group1A.hasMember(SubjectTestHelper.SUBJ6));
    assertTrue(group1B.hasMember(SubjectTestHelper.SUBJ8));

  }
  
  /**
   * 
   * @see edu.internet2.middleware.grouper.helper.GrouperTest#setUp()
   */
  protected void setUp () {
    try {
      
      //this is the test hook imple
      GrouperHookType.addHookOverride(GrouperHookType.MEMBERSHIP.getPropertyFileKey(), 
        MembershipOneInFolderMaxHook.class);
    
      EhcacheController.ehcacheController().flushCache();
      RegistryReset.internal_resetRegistryAndAddTestSubjects();

      MembershipOneInFolderMaxHook.inittedOnce = false;
      
      GrouperTest.initGroupsAndAttributes();

  
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * @see edu.internet2.middleware.grouper.helper.GrouperTest#tearDown()
   */
  @Override
  protected void tearDown() {
    //dont have the test hook imple
    GrouperHookType.addHookOverride(GrouperHookType.MEMBERSHIP.getPropertyFileKey(), (Class<?>)null);
  }

}
