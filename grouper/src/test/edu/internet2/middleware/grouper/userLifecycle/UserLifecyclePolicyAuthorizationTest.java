package edu.internet2.middleware.grouper.userLifecycle;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.cfg.dbConfig.GrouperDbConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.subject.Subject;

/**
 * Security fix for userLifecycleRestrictedPolicyRemoval: rules for saving a user lifecycle policy
 * on a group (UserLifecycleService.savePolicyConfigOnGroup).
 * <ul>
 *   <li>null policy id (form field not submitted, e.g. no radio checked) changes nothing</li>
 *   <li>any group ADMIN may remove a policy, including a restricted one, and the removal is
 *   audited with the real subject</li>
 *   <li>resubmitting the policy already on the group is allowed (the edit screen shows an assigned
 *   policy the user cannot use as a read-only choice)</li>
 *   <li>assigning or replacing needs the right to use the NEW policy</li>
 * </ul>
 * Before the fix, a null policy id removed the policy, resubmitting an assigned restricted policy
 * threw, and no policy audit entry was written, so those tests fail.
 * <p>
 * Setup: restrictedPolicy (not public, its group is test:lifecycle:policyUsers), publicPolicy
 * (public). SUBJ0 is ADMIN of test:lifecycle:protectedGroup and not in policyUsers. SUBJ1 is in
 * policyUsers. SUBJ2 is in the wheel group in the wheel test.
 * </p>
 */
public class UserLifecyclePolicyAuthorizationTest extends GrouperTest {

  /**
   * run one test
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new UserLifecyclePolicyAuthorizationTest("testMissingFieldKeepsPolicy"));
  }

  /**
   * @param name
   */
  public UserLifecyclePolicyAuthorizationTest(String name) {
    super(name);
  }

  /** restricted policy config id */
  private static final String RESTRICTED = "restrictedPolicyProof";

  /** public policy config id */
  private static final String PUBLIC = "publicPolicyProof";

  /** root session made in setUp */
  private GrouperSession rootSession;

  /** group the policy is assigned to */
  private Group protectedGroup;

  /**
   * groups, privileges, policies
   */
  @Override
  public void setUp() {
    super.setUp();
    this.rootSession = GrouperSession.startRootSession();

    Group policyUsers = new GroupSave(this.rootSession).assignName("test:lifecycle:policyUsers").assignCreateParentStemsIfNotExist(true).save();
    policyUsers.addMember(SubjectTestHelper.SUBJ1, false);

    this.protectedGroup = new GroupSave(this.rootSession).assignName("test:lifecycle:protectedGroup").assignCreateParentStemsIfNotExist(true).save();
    this.protectedGroup.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.ADMIN, false);
    this.protectedGroup.grantPriv(SubjectTestHelper.SUBJ1, AccessPrivilege.ADMIN, false);

    configPolicy(RESTRICTED, "Restricted policy proof", "false", policyUsers.getName());
    configPolicy(PUBLIC, "Public policy proof", "true", null);
  }

  /**
   * stop the root session
   */
  @Override
  public void tearDown() {
    GrouperSession.stopQuietly(this.rootSession);
    super.tearDown();
  }

  /**
   * store a policy config
   * @param configId
   * @param name
   * @param isPublic
   * @param groupName
   */
  private static void configPolicy(String configId, String name, String isPublic, String groupName) {
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperUserLifecyclePolicy." + configId + ".name").value(name).store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperUserLifecyclePolicy." + configId + ".description").value(name).store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperUserLifecyclePolicy." + configId + ".isPublic").value(isPublic).store();
    if (groupName != null) {
      new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperUserLifecyclePolicy." + configId + ".groupIdOrName").value(groupName).store();
    }
  }

  /**
   * assign a policy as root
   * @param policyConfigId
   */
  private void assignAsRoot(String policyConfigId) {
    UserLifecycleService.savePolicyConfigOnGroup(this.protectedGroup, policyConfigId, SubjectFinder.findRootSubject());
    assertEquals(policyConfigId, UserLifecycleService.retrieveExistingPolicyConfigId(this.protectedGroup));
  }

  /**
   * save as a subject (in a session of that subject, like the UI)
   * @param subject
   * @param policyConfigId
   * @return true if changed
   */
  private boolean saveAs(Subject subject, String policyConfigId) {
    GrouperSession session = GrouperSession.start(subject);
    try {
      return UserLifecycleService.savePolicyConfigOnGroup(this.protectedGroup, policyConfigId, subject);
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * count audit entries with this description where the subject is the actor
   * @param description
   * @param subject
   * @return count
   */
  private int auditCount(String description, Subject subject) {
    Member member = MemberFinder.findBySubject(this.rootSession, subject, true);
    return new GcDbAccess().sql("select count(*) from grouper_audit_entry where description = ? and (logged_in_member_id = ? or act_as_member_id = ?)")
        .addBindVar(description).addBindVar(member.getId()).addBindVar(member.getId()).select(int.class);
  }

  /**
   * a save that does not submit the field (no radio checked) keeps the restricted policy
   */
  public void testMissingFieldKeepsPolicy() {
    assignAsRoot(RESTRICTED);

    assertTrue(UserLifecycleService.subjectCanSavePolicyConfigOnGroup(this.protectedGroup, null, SubjectTestHelper.SUBJ0));
    assertFalse(saveAs(SubjectTestHelper.SUBJ0, null));
    assertEquals(RESTRICTED, UserLifecycleService.retrieveExistingPolicyConfigId(this.protectedGroup));
  }

  /**
   * resubmitting the restricted policy already on the group (read-only choice) is not a change
   */
  public void testKeepAssignedRestrictedPolicy() {
    assignAsRoot(RESTRICTED);

    assertTrue(UserLifecycleService.subjectCanSavePolicyConfigOnGroup(this.protectedGroup, RESTRICTED, SubjectTestHelper.SUBJ0));
    assertFalse(saveAs(SubjectTestHelper.SUBJ0, RESTRICTED));
    assertEquals(RESTRICTED, UserLifecycleService.retrieveExistingPolicyConfigId(this.protectedGroup));
  }

  /**
   * any group admin may remove a restricted policy; the removal is audited with that subject
   */
  public void testAdminCanRemoveRestrictedPolicyAudited() {
    assignAsRoot(RESTRICTED);

    assertTrue(UserLifecycleService.subjectCanSavePolicyConfigOnGroup(this.protectedGroup, "", SubjectTestHelper.SUBJ0));
    assertTrue(saveAs(SubjectTestHelper.SUBJ0, ""));
    assertEquals("", UserLifecycleService.retrieveExistingPolicyConfigId(this.protectedGroup));

    assertEquals(1, auditCount("Removed user lifecycle policy '" + RESTRICTED + "'", SubjectTestHelper.SUBJ0));
  }

  /**
   * an admin who cannot use the restricted policy cannot assign it
   */
  public void testCannotAssignRestrictedPolicy() {
    assertFalse(UserLifecycleService.subjectCanSavePolicyConfigOnGroup(this.protectedGroup, RESTRICTED, SubjectTestHelper.SUBJ0));
    try {
      saveAs(SubjectTestHelper.SUBJ0, RESTRICTED);
      fail("SUBJ0 should not be able to assign the restricted policy");
    } catch (RuntimeException re) {
      // expected
    }
    assertEquals("", UserLifecycleService.retrieveExistingPolicyConfigId(this.protectedGroup));
  }

  /**
   * an admin who cannot use the restricted policy cannot replace another policy with it
   */
  public void testCannotReplaceWithRestrictedPolicy() {
    assignAsRoot(PUBLIC);

    assertFalse(UserLifecycleService.subjectCanSavePolicyConfigOnGroup(this.protectedGroup, RESTRICTED, SubjectTestHelper.SUBJ0));
    try {
      saveAs(SubjectTestHelper.SUBJ0, RESTRICTED);
      fail("SUBJ0 should not be able to replace with the restricted policy");
    } catch (RuntimeException re) {
      // expected
    }
    assertEquals(PUBLIC, UserLifecycleService.retrieveExistingPolicyConfigId(this.protectedGroup));
  }

  /**
   * replacing a restricted policy with one the admin can use only needs the new one; audited
   */
  public void testReplaceRestrictedWithPublicAudited() {
    assignAsRoot(RESTRICTED);

    assertTrue(saveAs(SubjectTestHelper.SUBJ0, PUBLIC));
    assertEquals(PUBLIC, UserLifecycleService.retrieveExistingPolicyConfigId(this.protectedGroup));

    assertEquals(1, auditCount("Changed user lifecycle policy from '" + RESTRICTED + "' to '" + PUBLIC + "'", SubjectTestHelper.SUBJ0));
  }

  /**
   * a member of the policy group can assign the restricted policy; audited
   */
  public void testPolicyGroupMemberCanAssign() {
    assertTrue(UserLifecycleService.subjectCanSavePolicyConfigOnGroup(this.protectedGroup, RESTRICTED, SubjectTestHelper.SUBJ1));
    assertTrue(saveAs(SubjectTestHelper.SUBJ1, RESTRICTED));
    assertEquals(RESTRICTED, UserLifecycleService.retrieveExistingPolicyConfigId(this.protectedGroup));

    assertEquals(1, auditCount("Assigned user lifecycle policy '" + RESTRICTED + "'", SubjectTestHelper.SUBJ1));
  }

  /**
   * a policy id that is not configured cannot be assigned
   */
  public void testUnknownPolicyDenied() {
    assertFalse(UserLifecycleService.subjectCanSavePolicyConfigOnGroup(this.protectedGroup, "noSuchPolicy", SubjectTestHelper.SUBJ1));
  }

  /**
   * a wheel member can assign, replace and remove
   */
  public void testWheelAllowed() {
    Stem root = StemFinder.findRootStem(this.rootSession);
    Stem etc = StemFinder.findByName(this.rootSession, "etc", false);
    if (etc == null) {
      etc = root.addChildStem("etc", "etc");
    }
    Group wheel = etc.addChildGroup("testWheel", "testWheel");
    wheel.addMember(SubjectTestHelper.SUBJ2, false);
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.wheel.use", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.wheel.group", wheel.getName());

    assertTrue(saveAs(SubjectTestHelper.SUBJ2, RESTRICTED));
    assertEquals(RESTRICTED, UserLifecycleService.retrieveExistingPolicyConfigId(this.protectedGroup));
    assertTrue(saveAs(SubjectTestHelper.SUBJ2, PUBLIC));
    assertEquals(PUBLIC, UserLifecycleService.retrieveExistingPolicyConfigId(this.protectedGroup));
    assertTrue(saveAs(SubjectTestHelper.SUBJ2, ""));
    assertEquals("", UserLifecycleService.retrieveExistingPolicyConfigId(this.protectedGroup));
  }
}
