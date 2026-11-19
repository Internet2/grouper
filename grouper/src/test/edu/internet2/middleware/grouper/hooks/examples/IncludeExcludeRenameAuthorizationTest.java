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
package edu.internet2.middleware.grouper.hooks.examples;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GroupType;
import edu.internet2.middleware.grouper.GroupTypeFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.misc.GrouperStartup;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
import edu.internet2.middleware.subject.Subject;

/**
 * <pre>
 * Renaming a group of a legacy include/exclude (addIncludeExclude) or requireInGroups family
 * also renames the related groups.  Those related groups were written straight through the
 * DAO, so a caller who was ADMIN only on the stored group could rename the overall group and
 * helper groups (or an unrelated group named like a helper).  The fix requires ADMIN on every
 * related group (wheel / root exempt) and fails the whole store closed.
 *
 * Before the fix the denied renames below succeed and these tests fail.
 * </pre>
 */
public class IncludeExcludeRenameAuthorizationTest extends GrouperTest {

  /**
   * run one test
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new IncludeExcludeRenameAuthorizationTest("testSystemOfRecordAdminCannotRenameFamily"));
  }

  /**
   * @param name
   */
  public IncludeExcludeRenameAuthorizationTest(String name) {
    super(name);
  }

  /** the five groups of an include/exclude family, by suffix (overall is "") */
  private static final String[] FAMILY_SUFFIXES = new String[] {
      "", "_systemOfRecord", "_includes", "_excludes", "_systemOfRecordAndIncludes"};

  /** addIncludeExclude type */
  private GroupType includeExcludeType;

  /** requireInGroups type */
  private GroupType requireGroupsType;

  /**
   * turn on the legacy include/exclude types like TestGroupTypeIncludeExclude does
   * @see edu.internet2.middleware.grouper.helper.GrouperTest#setUp()
   */
  @Override
  protected void setUp() {
    // dont precreate the types during the reset, create them after the config is set
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperIncludeExclude.use", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperIncludeExclude.requireGroups.use", "false");
    super.setUp();
    GrouperSession.startRootSession();
    setupTestConfigForIncludeExclude();
    // propagation is on by default, make it explicit
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouper.rename.includeExcludeRequireEtc.when.name.changes", "true");
    GrouperStartup.initIncludeExcludeType();
    this.includeExcludeType = GroupTypeFinder.find(
        GrouperConfig.retrieveConfig().propertyValueString("grouperIncludeExclude.type.name"), true);
    this.requireGroupsType = GroupTypeFinder.find(
        GrouperConfig.retrieveConfig().propertyValueString("grouperIncludeExclude.requireGroups.type.name"), true);
  }

  // ---------------------------------------------------------------------------------------
  // helpers
  // ---------------------------------------------------------------------------------------

  /**
   * start a session (the static session is what Group.store() checks)
   * @param subject null for root
   * @return the session
   */
  private static GrouperSession startSession(Subject subject) {
    GrouperSession.stopQuietly(GrouperSession.staticGrouperSession(false));
    return subject == null ? GrouperSession.startRootSession() : GrouperSession.start(subject);
  }

  /**
   * find a group as root
   * @param name
   * @return group or null
   */
  private static Group findAsRoot(String name) {
    GrouperSession rootSession = startSession(null);
    return GroupFinder.findByName(rootSession, name, false);
  }

  /**
   * @param throwable
   * @return true if InsufficientPrivilegeException is in the cause chain
   */
  private static boolean isInsufficientPrivilege(Throwable throwable) {
    while (throwable != null) {
      if (throwable instanceof InsufficientPrivilegeException) {
        return true;
      }
      throwable = throwable.getCause();
    }
    return false;
  }

  /**
   * as root make an include/exclude family under the overall group name
   * @param overallName
   */
  private void createFamily(String overallName) {
    GrouperSession rootSession = startSession(null);
    Group overall = new GroupSave(rootSession).assignName(overallName).assignCreateParentStemsIfNotExist(true).save();
    overall.addType(this.includeExcludeType);
    for (String suffix : FAMILY_SUFFIXES) {
      assertNotNull("precondition: family group created: " + overallName + suffix,
          GroupFinder.findByName(rootSession, overallName + suffix, false));
    }
  }

  /**
   * as subject, rename a group (extension and display extension) and store it
   * @param subject
   * @param groupName
   * @param newExtension
   * @param newDisplayExtension
   * @return null if it worked, else the exception
   */
  private static RuntimeException renameAs(Subject subject, String groupName, String newExtension, String newDisplayExtension) {
    GrouperSession session = startSession(subject);
    try {
      Group group = GroupFinder.findByName(session, groupName, true);
      group.setExtension(newExtension);
      group.setDisplayExtension(newDisplayExtension);
      group.store();
      return null;
    } catch (RuntimeException re) {
      return re;
    } finally {
      startSession(null);
    }
  }

  // ---------------------------------------------------------------------------------------
  // tests
  // ---------------------------------------------------------------------------------------

  /**
   * ADMIN on the systemOfRecord group only (READ on the rest): renaming it must not rename the
   * overall group or the other helpers, and the whole store rolls back
   */
  public void testSystemOfRecordAdminCannotRenameFamily() {
    String overallName = "test:ie:protectedPolicy";
    createFamily(overallName);

    GrouperSession rootSession = startSession(null);
    Subject attacker = SubjectTestHelper.SUBJ0;
    for (String suffix : FAMILY_SUFFIXES) {
      Group group = GroupFinder.findByName(rootSession, overallName + suffix, true);
      group.grantPriv(attacker, AccessPrivilege.READ, false);
      if ("_systemOfRecord".equals(suffix)) {
        group.grantPriv(attacker, AccessPrivilege.ADMIN, false);
      }
    }
    assertFalse(GroupFinder.findByName(rootSession, overallName, true).hasAdmin(attacker));

    RuntimeException re = renameAs(attacker, overallName + "_systemOfRecord",
        "attackerRenamed_systemOfRecord", "attackerRenamed system of record");
    assertNotNull("rename must be denied", re);
    assertTrue("expected InsufficientPrivilegeException: " + re, isInsufficientPrivilege(re));

    // nothing renamed, including the stored group itself (rollback)
    for (String suffix : FAMILY_SUFFIXES) {
      assertNotNull("still there: " + overallName + suffix, findAsRoot(overallName + suffix));
      assertNull("not renamed: " + suffix, findAsRoot("test:ie:attackerRenamed" + suffix));
    }
  }

  /**
   * ADMIN on the whole family: the same rename works and moves every group (no regression)
   */
  public void testFamilyAdminCanRenameFamily() {
    String overallName = "test:ie:policyOk";
    createFamily(overallName);

    GrouperSession rootSession = startSession(null);
    Subject admin = SubjectTestHelper.SUBJ1;
    for (String suffix : FAMILY_SUFFIXES) {
      GroupFinder.findByName(rootSession, overallName + suffix, true).grantPriv(admin, AccessPrivilege.ADMIN, false);
    }

    RuntimeException re = renameAs(admin, overallName + "_systemOfRecord",
        "policyRenamed_systemOfRecord", "policyRenamed system of record");
    assertNull("rename should work: " + re, re);
    for (String suffix : FAMILY_SUFFIXES) {
      assertNotNull("renamed: " + suffix, findAsRoot("test:ie:policyRenamed" + suffix));
    }
  }

  /**
   * root (e.g. the loader) still renames the family without explicit privileges
   */
  public void testRootCanRenameFamily() {
    String overallName = "test:ie:policyRoot";
    createFamily(overallName);

    RuntimeException re = renameAs(null, overallName, "policyRootRenamed", "policyRootRenamed");
    assertNull("root rename should work: " + re, re);
    for (String suffix : FAMILY_SUFFIXES) {
      assertNotNull("renamed: " + suffix, findAsRoot("test:ie:policyRootRenamed" + suffix));
    }
  }

  /**
   * name collision: a readable but unrelated group named like a helper (x_includes) must not be
   * renamed when someone renames their own include/exclude group x
   */
  public void testNameCollisionDoesNotRenameUnrelatedGroup() {
    GrouperSession rootSession = startSession(null);
    Stem stem = new StemSave(rootSession).assignName("test:ie2").assignCreateParentStemsIfNotExist(true).save();
    Group protectedGroup = new GroupSave(rootSession).assignName("test:ie2:owned_includes")
        .assignDisplayExtension("owned includes").save();
    String protectedUuid = protectedGroup.getUuid();

    Subject attacker = SubjectTestHelper.SUBJ0;
    protectedGroup.grantPriv(attacker, AccessPrivilege.READ, false);
    stem.grantPriv(attacker, NamingPrivilege.CREATE, false);
    assertFalse(protectedGroup.hasAdmin(attacker));

    // attacker creates their own group and makes it an include/exclude group
    GrouperSession attackerSession = startSession(attacker);
    Group owned = new GroupSave(attackerSession).assignName("test:ie2:owned").save();
    try {
      owned.addType(this.includeExcludeType);
    } catch (RuntimeException re) {
      // the type add may itself be refused (it would touch the protected group); then add it as
      // root so the rename path is still exercised
      startSession(null);
      GroupFinder.findByName(GrouperSession.staticGrouperSession(), "test:ie2:owned", true)
          .addType(this.includeExcludeType);
    }
    startSession(null);

    RuntimeException re = renameAs(attacker, "test:ie2:owned", "renamed", "renamed");
    assertNotNull("rename must be denied", re);
    assertTrue("expected InsufficientPrivilegeException: " + re, isInsufficientPrivilege(re));

    Group protectedAfter = findAsRoot("test:ie2:owned_includes");
    assertNotNull("protected group keeps its name", protectedAfter);
    assertEquals(protectedUuid, protectedAfter.getUuid());
    assertNull(findAsRoot("test:ie2:renamed_includes"));
  }

  /**
   * requireInGroups: ADMIN on the overall group but not on its systemOfRecord group, renaming
   * the overall group must not rename the systemOfRecord group
   */
  public void testRequireInGroupsOverallAdminCannotRenameSystemOfRecord() {
    GrouperSession rootSession = startSession(null);
    new GroupSave(rootSession).assignName("test:rig:required").assignCreateParentStemsIfNotExist(true).save();
    Group overall = new GroupSave(rootSession).assignName("test:rig:policy").assignCreateParentStemsIfNotExist(true).save();
    overall.addType(this.requireGroupsType);
    overall.setAttribute(GrouperConfig.retrieveConfig().propertyValueString(
        "grouperIncludeExclude.requireGroups.attributeName"), "test:rig:required");

    Group systemOfRecord = GroupFinder.findByName(rootSession, "test:rig:policy_systemOfRecord", false);
    assertNotNull("precondition: systemOfRecord group created", systemOfRecord);

    Subject attacker = SubjectTestHelper.SUBJ0;
    overall = GroupFinder.findByName(rootSession, "test:rig:policy", true);
    overall.grantPriv(attacker, AccessPrivilege.ADMIN, false);
    systemOfRecord.grantPriv(attacker, AccessPrivilege.READ, false);
    assertFalse(systemOfRecord.hasAdmin(attacker));

    RuntimeException re = renameAs(attacker, "test:rig:policy", "policyRenamed", "policyRenamed");
    assertNotNull("rename must be denied", re);
    assertTrue("expected InsufficientPrivilegeException: " + re, isInsufficientPrivilege(re));

    assertNotNull(findAsRoot("test:rig:policy"));
    assertNotNull(findAsRoot("test:rig:policy_systemOfRecord"));
    assertNull(findAsRoot("test:rig:policyRenamed_systemOfRecord"));
  }

}
