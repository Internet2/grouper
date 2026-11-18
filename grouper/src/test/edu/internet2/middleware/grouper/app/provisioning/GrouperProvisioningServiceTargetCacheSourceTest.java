package edu.internet2.middleware.grouper.app.provisioning;

import java.util.Map;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.app.ldapProvisioning.LdapSync;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.subject.Subject;
import junit.textui.TestRunner;

/**
 * <p>Security advisory M1: GrouperProvisioningService cached the groupAllowedToView and
 * groupAllowedToAssign membership decisions under (authorization group, subject id), without the
 * subject source.  A decision made for one subject was reused for a different subject with the
 * same id in another source for up to five minutes, both for viewing a target and for assigning
 * it (ProvisionableGroupSave).</p>
 *
 * <p>Two subjects share the id test.subject.1: source A is a group whose uuid is test.subject.1
 * (g:gsa||test.subject.1), source B is the jdbc test subject SUBJ1 (jdbc||test.subject.1).</p>
 *
 * <p>The decision caches are static and keyed by the authorization group name, so every test
 * uses its own uniquely named viewers and assigners groups (and its own target).</p>
 */
public class GrouperProvisioningServiceTargetCacheSourceTest extends GrouperTest {

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new GrouperProvisioningServiceTargetCacheSourceTest("testSaveDeniedForOtherSourceAfterPrime"));
  }

  /**
   *
   */
  public GrouperProvisioningServiceTargetCacheSourceTest() {
    super();
  }

  /**
   * @param name
   */
  public GrouperProvisioningServiceTargetCacheSourceTest(String name) {
    super(name);
  }

  /** shared subject id */
  private static final String SHARED_ID = "test.subject.1";

  /** provisioner config id, unique per test */
  private String targetName;

  /** viewers group name, unique per test (the static cache is keyed by it) */
  private String viewersName;

  /** assigners group name, unique per test */
  private String assignersName;

  /** source A: g:gsa||test.subject.1 */
  private Subject sourceA;

  /** source B: jdbc||test.subject.1 */
  private Subject sourceB;

  /** viewers group */
  private Group viewers;

  /** assigners group */
  private Group assigners;

  // ---------------------------------------------------------------------------------------------
  // setup
  // ---------------------------------------------------------------------------------------------

  /**
   * config, groups and the two same-id subjects, as root
   */
  @Override
  protected void setUp() {
    super.setUp();

    String suffix = this.getName() + "_" + (System.currentTimeMillis() % 1000000);
    this.targetName = "junitProv85_" + suffix;
    this.viewersName = "test:provisioning85:viewers_" + suffix;
    this.assignersName = "test:provisioning85:assigners_" + suffix;

    // an ldap provisioner that is never run, only used for its view / assign settings
    Map<String, String> loader = GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap();
    loader.put("ldap.personLdap.url", "ldap://localhost:389");
    loader.put("ldap.personLdap.user", "cn=admin,dc=example,dc=edu");
    loader.put("ldap.personLdap.pass", "secret");
    String p = "provisioner." + this.targetName + ".";
    loader.put(p + "class", LdapSync.class.getName());
    loader.put(p + "ldapExternalSystemConfigId", "personLdap");
    loader.put(p + "groupAllowedToView", this.viewersName);
    loader.put(p + "groupAllowedToAssign", this.assignersName);

    GrouperSession grouperSession = GrouperSession.startRootSession();
    try {
      // a group whose uuid is the jdbc subject's id gives a second subject with the same id
      new GroupSave().assignName("test:provisioning85:sameId").assignUuid(SHARED_ID)
        .assignCreateParentStemsIfNotExist(true).save();
      this.sourceA = SubjectFinder.findByIdAndSource(SHARED_ID, "g:gsa", true);
      this.sourceB = SubjectFinder.findByIdAndSource(SHARED_ID, "jdbc", true);
      assertEquals(SHARED_ID, SubjectTestHelper.SUBJ1.getId());

      this.viewers = new GroupSave().assignName(this.viewersName).assignCreateParentStemsIfNotExist(true).save();
      this.assigners = new GroupSave().assignName(this.assignersName).assignCreateParentStemsIfNotExist(true).save();
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * remove config overrides
   */
  @Override
  protected void tearDown() {
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().keySet().removeIf(
        key -> key.startsWith("provisioner." + this.targetName + ".") || key.startsWith("ldap.personLdap."));
    super.tearDown();
  }

  /**
   * @return the target for this test
   */
  private GrouperProvisioningTarget target() {
    GrouperProvisioningTarget target = GrouperProvisioningSettings.getTargets(true).get(this.targetName);
    assertNotNull(target);
    return target;
  }

  /**
   * add a member as root
   * @param group
   * @param subject
   */
  private static void addMember(final Group group, final Subject subject) {
    GrouperSession grouperSession = GrouperSession.startRootSession();
    try {
      group.addMember(subject, false);
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * isTargetViewable for a subject, in a root session: PrivilegeHelper.isWheelOrRoot needs an open
   * session, and without one the result would depend on a session an earlier test left open
   * @param subject
   * @return if viewable
   */
  private boolean viewable(Subject subject) {
    GrouperSession grouperSession = GrouperSession.startRootSession();
    try {
      return GrouperProvisioningService.isTargetViewable(target(), subject, null);
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * isTargetEditable for a subject, in a root session (see viewable)
   * @param subject
   * @return if editable
   */
  private boolean editable(Subject subject) {
    GrouperSession grouperSession = GrouperSession.startRootSession();
    try {
      return GrouperProvisioningService.isTargetEditable(target(), subject, null);
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  // ---------------------------------------------------------------------------------------------
  // view decision
  // ---------------------------------------------------------------------------------------------

  /**
   * A is a viewer, B is not: A's cached true must not answer for B
   */
  public void testViewSourceAThenB() {
    addMember(this.viewers, this.sourceA);
    assertTrue(viewable(this.sourceA));
    assertFalse("source B must not reuse source A's view decision",
        viewable(this.sourceB));
  }

  /**
   * B's cached false must not deny A
   */
  public void testViewSourceBThenA() {
    addMember(this.viewers, this.sourceA);
    assertFalse(viewable(this.sourceB));
    assertTrue("source A must not reuse source B's view denial",
        viewable(this.sourceA));
  }

  // ---------------------------------------------------------------------------------------------
  // edit decision
  // ---------------------------------------------------------------------------------------------

  /**
   * A is an assigner, B is not: A's cached true must not answer for B
   */
  public void testEditSourceAThenB() {
    addMember(this.assigners, this.sourceA);
    assertTrue(editable(this.sourceA));
    assertFalse("source B must not reuse source A's assign decision",
        editable(this.sourceB));
  }

  /**
   * B's cached false must not deny A
   */
  public void testEditSourceBThenA() {
    addMember(this.assigners, this.sourceA);
    assertFalse(editable(this.sourceB));
    assertTrue("source A must not reuse source B's assign denial",
        editable(this.sourceA));
  }

  // ---------------------------------------------------------------------------------------------
  // real write
  // ---------------------------------------------------------------------------------------------

  /**
   * B administers the group but is not an assigner.  After A (an assigner) primes the cache, a
   * ProvisionableGroupSave as B must be denied and must not write a provisioning assignment.
   */
  public void testSaveDeniedForOtherSourceAfterPrime() {
    addMember(this.assigners, this.sourceA);

    Group targetGroup;
    GrouperSession rootSession = GrouperSession.startRootSession();
    try {
      targetGroup = new GroupSave().assignName("test:provisioning85:targetGroup").assignCreateParentStemsIfNotExist(true).save();
      targetGroup.grantPriv(this.sourceB, AccessPrivilege.ADMIN, false);
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }

    // prime the assign cache with source A
    assertTrue(editable(this.sourceA));

    GrouperSession sourceBSession = GrouperSession.start(this.sourceB);
    try {
      new ProvisionableGroupSave().assignTargetName(this.targetName).assignGroup(targetGroup)
        .assignProvision(true).save();
      fail("source B is not in groupAllowedToAssign and must not assign the target");
    } catch (RuntimeException re) {
      // expected: Not allowed to edit target
    } finally {
      GrouperSession.stopQuietly(sourceBSession);
    }

    rootSession = GrouperSession.startRootSession();
    try {
      GrouperProvisioningAttributeValue value = GrouperProvisioningService.getProvisioningAttributeValue(targetGroup, this.targetName);
      assertTrue("no direct provisioning assignment may be written",
          value == null || !value.isDirectAssignment());
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }
  }

  /**
   * control: the correctly authorized source (B in the assigners group, ADMIN on the group) can
   * still assign the target
   */
  public void testSaveAllowedForAuthorizedSource() {
    addMember(this.assigners, this.sourceB);

    Group targetGroup;
    GrouperSession rootSession = GrouperSession.startRootSession();
    try {
      targetGroup = new GroupSave().assignName("test:provisioning85:allowedGroup").assignCreateParentStemsIfNotExist(true).save();
      targetGroup.grantPriv(this.sourceB, AccessPrivilege.ADMIN, false);
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }

    // a cached denial for source A must not deny B
    assertFalse(editable(this.sourceA));

    GrouperSession sourceBSession = GrouperSession.start(this.sourceB);
    try {
      new ProvisionableGroupSave().assignTargetName(this.targetName).assignGroup(targetGroup)
        .assignProvision(true).save();
    } finally {
      GrouperSession.stopQuietly(sourceBSession);
    }

    rootSession = GrouperSession.startRootSession();
    try {
      GrouperProvisioningAttributeValue value = GrouperProvisioningService.getProvisioningAttributeValue(targetGroup, this.targetName);
      assertNotNull(value);
      assertTrue(value.isDirectAssignment());
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }
  }
}
