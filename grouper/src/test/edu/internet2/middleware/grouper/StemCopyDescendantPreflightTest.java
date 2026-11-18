/**
 * Copyright 2026 Internet2
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
package edu.internet2.middleware.grouper;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.Stem.Scope;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.misc.CompositeType;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
import edu.internet2.middleware.grouper.privs.Privilege;
import edu.internet2.middleware.grouper.rules.RuleApi;
import edu.internet2.middleware.subject.Subject;

/**
 * Security advisory M3 (stem copy descendant authorization): with
 * security.stem.copyRequireDescendantPrivileges = true, a folder copy by a non-wheel user must
 * require the privileges to read what each copy option duplicates in every descendant
 * (StemCopyDescendantPreflight), and fail before anything is created.  Default false keeps the
 * documented behavior (GRP-3794): STEM_ADMIN on the top folder is enough.
 * <p>
 * Setup as root: top:source (SUBJ0 STEM_ADMIN) with top:source:privateGroup (member SUBJ2,
 * SUBJ0 has no privilege on it) and top:source:sub (SUBJ0 has no privilege on it).  top:dest
 * gives SUBJ0 CREATE and an inherited group ADMIN rule, which is what turns the copy into a
 * disclosure.  SUBJ1 is in the wheel group in the wheel test.
 * </p>
 * Before the fix the "on" denied cases copy anyway (and the preflight class does not exist).
 */
public class StemCopyDescendantPreflightTest extends GrouperTest {

  /**
   * run one test
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new StemCopyDescendantPreflightTest("testMembershipCopyDeniedWithoutRead"));
  }

  /**
   *
   */
  public StemCopyDescendantPreflightTest() {
    super();
  }

  /**
   * @param name
   */
  public StemCopyDescendantPreflightTest(String name) {
    super(name);
  }

  /** name of the copied private group at the destination */
  private static final String COPIED_GROUP_NAME = "top:dest:source:privateGroup";

  /** root session made in setUp */
  private GrouperSession rootSession;

  /** top folder */
  private Stem top;

  /** source folder, SUBJ0 is STEM_ADMIN */
  private Stem source;

  /** child folder of source, SUBJ0 has no privilege */
  private Stem sourceSub;

  /** group in source SUBJ0 cannot read, SUBJ2 is a member */
  private Group privateGroup;

  /** destination folder, SUBJ0 has CREATE and inherited group ADMIN */
  private Stem dest;

  /**
   * make the folders, group, privileges and the inheritance rule as root
   */
  @Override
  public void setUp() {
    super.setUp();
    // GrouperTest grants READ to GrouperAll on every new group; turn that off so the groups made
    // here are private and the preflight's READ checks are actually exercised
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.create.grant.all.read", "false");
    this.rootSession = GrouperSession.startRootSession();
    Stem root = StemFinder.findRootStem(this.rootSession);
    this.top = root.addChildStem("top", "top");

    this.source = this.top.addChildStem("source", "source");
    this.sourceSub = this.source.addChildStem("sub", "sub");
    this.privateGroup = this.source.addChildGroup("privateGroup", "privateGroup");
    this.privateGroup.addMember(SubjectTestHelper.SUBJ2, false);
    this.source.grantPriv(SubjectTestHelper.SUBJ0, NamingPrivilege.STEM_ADMIN, false);

    this.dest = this.top.addChildStem("dest", "dest");
    this.dest.grantPriv(SubjectTestHelper.SUBJ0, NamingPrivilege.CREATE, false);

    // the destination policy that makes copied groups readable by the copier
    RuleApi.inheritGroupPrivileges(this.dest, Scope.SUB, SubjectTestHelper.SUBJ0, Privilege.getInstances("admin"));
  }

  /**
   * stop sessions
   */
  @Override
  public void tearDown() {
    GrouperSession.stopQuietly(this.rootSession);
    super.tearDown();
  }

  // ---------------------------------------------------------------------------------------
  // config on
  // ---------------------------------------------------------------------------------------

  /**
   * membership copy without READ on a descendant group is denied and nothing is created
   */
  public void testMembershipCopyDeniedWithoutRead() {
    turnOn();
    assertCopyDenied(SubjectTestHelper.SUBJ0, false, false, true, false);
    assertNothingCopied();
  }

  /**
   * membership copy with READ on every descendant group is allowed
   */
  public void testMembershipCopyAllowedWithRead() {
    turnOn();
    this.privateGroup.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.READ, false);

    copyAs(SubjectTestHelper.SUBJ0, false, false, true, false);

    Group copied = GroupFinder.findByName(GrouperSession.startRootSession(), COPIED_GROUP_NAME, true);
    assertTrue(copied.hasMember(SubjectTestHelper.SUBJ2));
  }

  /**
   * group privilege copy or attribute copy needs ADMIN, READ is not enough
   */
  public void testPrivilegeAndAttributeCopyNeedAdmin() {
    turnOn();
    this.privateGroup.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.READ, false);

    assertCopyDenied(SubjectTestHelper.SUBJ0, false, true, false, false);
    assertCopyDenied(SubjectTestHelper.SUBJ0, false, false, false, true);
    assertNothingCopied();

    // the copy attempts stopped their sessions, so act as root again
    GrouperSession.startRootSession();
    this.privateGroup.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.ADMIN, false);
    copyAs(SubjectTestHelper.SUBJ0, false, true, false, true);
    assertNotNull(GroupFinder.findByName(GrouperSession.startRootSession(), COPIED_GROUP_NAME, false));
  }

  /**
   * folder privilege copy needs STEM_ADMIN on every descendant folder, not only the top one
   */
  public void testFolderPrivilegeCopyNeedsStemAdminOnDescendants() {
    turnOn();
    assertCopyDenied(SubjectTestHelper.SUBJ0, true, false, false, false);
    assertNothingCopied();

    GrouperSession.startRootSession();
    this.sourceSub.grantPriv(SubjectTestHelper.SUBJ0, NamingPrivilege.STEM_ADMIN, false);
    copyAs(SubjectTestHelper.SUBJ0, true, false, false, false);
    assertNotNull(StemFinder.findByName(GrouperSession.startRootSession(), "top:dest:source:sub", false));
  }

  /**
   * a structure-only copy (all data options off) needs nothing beyond STEM_ADMIN on the top
   */
  public void testStructureOnlyCopyAllowed() {
    turnOn();
    copyAs(SubjectTestHelper.SUBJ0, false, false, false, false);

    Group copied = GroupFinder.findByName(GrouperSession.startRootSession(), COPIED_GROUP_NAME, true);
    assertFalse(copied.hasMember(SubjectTestHelper.SUBJ2));
  }

  /**
   * a composite whose factors are outside the folder keeps them in the copy, so it reproduces
   * the membership even with membership copy off: READ on the composite is required
   */
  public void testCompositeWithOutsideFactorsNeedsRead() {
    Group left = this.top.addChildGroup("outsideLeft", "outsideLeft");
    Group right = this.top.addChildGroup("outsideRight", "outsideRight");
    left.addMember(SubjectTestHelper.SUBJ3, false);
    Group composite = this.source.addChildGroup("composite", "composite");
    composite.addCompositeMember(CompositeType.UNION, left, right);

    turnOn();
    assertCopyDenied(SubjectTestHelper.SUBJ0, false, false, false, false);
    assertNothingCopied();

    GrouperSession.startRootSession();
    composite.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.READ, false);
    copyAs(SubjectTestHelper.SUBJ0, false, false, false, false);
    assertNotNull(GroupFinder.findByName(GrouperSession.startRootSession(), "top:dest:source:composite", false));
  }

  /**
   * wheel may copy everything with the check on
   */
  public void testWheelAllowed() {
    turnOn();
    configureWheelWithSubj1();
    copyAs(SubjectTestHelper.SUBJ1, true, true, true, true);

    Group copied = GroupFinder.findByName(GrouperSession.startRootSession(), COPIED_GROUP_NAME, true);
    assertTrue(copied.hasMember(SubjectTestHelper.SUBJ2));
  }

  // ---------------------------------------------------------------------------------------
  // config off (default): documented behavior kept
  // ---------------------------------------------------------------------------------------

  /**
   * with the default, the copy works without descendant READ and the destination inheritance
   * rule lets SUBJ0 read the copied membership (same as TestStemApi.test_copy_stem_inherit_privileges)
   */
  public void testDefaultOffKeepsBehavior() {
    copyAs(SubjectTestHelper.SUBJ0, false, false, true, false);

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      Group copied = GroupFinder.findByName(session, COPIED_GROUP_NAME, true);
      assertTrue(copied.hasMember(SubjectTestHelper.SUBJ2));
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  // ---------------------------------------------------------------------------------------
  // helpers
  // ---------------------------------------------------------------------------------------

  /**
   * turn the preflight on (GrouperTest clears the override map after each test)
   */
  private static void turnOn() {
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(
        "security.stem.copyRequireDescendantPrivileges", "true");
  }

  /**
   * copy source to dest as the subject; the "outside" options (group as member / privilege
   * elsewhere) are off since they already run in the caller's session
   * @param subject
   * @param privilegesOfStem
   * @param privilegesOfGroup
   * @param listMembersOfGroup
   * @param attributes
   */
  private void copyAs(Subject subject, boolean privilegesOfStem, boolean privilegesOfGroup,
      boolean listMembersOfGroup, boolean attributes) {
    GrouperSession session = GrouperSession.start(subject);
    try {
      new StemCopy(this.source, this.dest)
          .copyPrivilegesOfStem(privilegesOfStem).copyPrivilegesOfGroup(privilegesOfGroup)
          .copyListMembersOfGroup(listMembersOfGroup).copyAttributes(attributes)
          .copyListGroupAsMember(false).copyGroupAsPrivilege(false).save();
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * the copy as the subject must throw InsufficientPrivilegeException
   * @param subject
   * @param privilegesOfStem
   * @param privilegesOfGroup
   * @param listMembersOfGroup
   * @param attributes
   */
  private void assertCopyDenied(Subject subject, boolean privilegesOfStem, boolean privilegesOfGroup,
      boolean listMembersOfGroup, boolean attributes) {
    try {
      copyAs(subject, privilegesOfStem, privilegesOfGroup, listMembersOfGroup, attributes);
      fail("copy should be denied by the descendant preflight");
    } catch (InsufficientPrivilegeException ipe) {
      assertTrue(ipe.getMessage(), ipe.getMessage().contains("security.stem.copyRequireDescendantPrivileges"));
    }
  }

  /**
   * as root, the destination has no copied folder (the preflight runs before anything is created)
   */
  private static void assertNothingCopied() {
    GrouperSession session = GrouperSession.startRootSession();
    try {
      assertNull(StemFinder.findByName(session, "top:dest:source", false));
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * make an etc:testWheel group with SUBJ1 in it and turn wheel on
   */
  private static void configureWheelWithSubj1() {
    GrouperSession session = GrouperSession.startRootSession();
    try {
      Stem root = StemFinder.findRootStem(session);
      Stem etc = StemFinder.findByName(session, "etc", false);
      if (etc == null) {
        etc = root.addChildStem("etc", "etc");
      }
      Group wheel = etc.addChildGroup("testWheel", "testWheel");
      wheel.addMember(SubjectTestHelper.SUBJ1, false);
      GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.wheel.use", "true");
      GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.wheel.group", wheel.getName());
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }
}
