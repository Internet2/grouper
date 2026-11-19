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
package edu.internet2.middleware.grouper.group;

import java.util.List;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * Security advisory M3 (require group / lockout group allowedToUse ignored): the
 * grouper.requireGroup.allowedToUse.N and grouper.lockoutGroup.allowedToUse.N settings must
 * restrict who may use the require or lockout group to members of the allowedToUse group.
 * Before the fix, RequireGroup and LockoutGroup re-read the name.N key instead of the
 * allowedToUse.N key, so membership was checked in the require/lockout group itself: members
 * of that group could use it whether or not they were allowed, and allowed users who were not
 * in it were refused.  A missing allowedToUse group made findByName throw.
 * <p>
 * Setup: ref:active has SUBJ0 and SUBJ1, ref:activeCanUse has SUBJ2 only.  The same layout is
 * used for require groups (grouper.requireGroup.*) and lockout groups (grouper.lockoutGroup.*).
 * </p>
 * Before the fix testMemberOfGroupButNotAllowed, testAllowedButNotMemberOfGroup and
 * testMissingAllowedToUseGroupFailsClosed fail.
 */
public class RequireLockoutGroupAllowedToUseTest extends GrouperTest {

  /**
   * run one test
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new RequireLockoutGroupAllowedToUseTest("testMemberOfGroupButNotAllowed"));
  }

  /**
   *
   */
  public RequireLockoutGroupAllowedToUseTest() {
    super();
  }

  /**
   * @param name
   */
  public RequireLockoutGroupAllowedToUseTest(String name) {
    super(name);
  }

  /** the require / lockout group */
  private static final String ACTIVE = "ref:active";

  /** the allowedToUse group */
  private static final String ACTIVE_CAN_USE = "ref:activeCanUse";

  /** root session made in setUp */
  private GrouperSession rootSession;

  /**
   * make the groups as root
   */
  @Override
  public void setUp() {
    super.setUp();
    this.rootSession = GrouperSession.startRootSession();
    Stem ref = StemFinder.findRootStem(this.rootSession).addChildStem("ref", "ref");

    // SUBJ0 and SUBJ1 are in the require/lockout group but not allowed to use it
    Group active = ref.addChildGroup("active", "active");
    active.addMember(SubjectTestHelper.SUBJ0, false);
    active.addMember(SubjectTestHelper.SUBJ1, false);

    // SUBJ2 is allowed to use it but is not in it
    Group activeCanUse = ref.addChildGroup("activeCanUse", "activeCanUse");
    activeCanUse.addMember(SubjectTestHelper.SUBJ2, false);
  }

  /**
   * clear the config overrides and stop the session
   */
  @Override
  public void tearDown() {
    GrouperConfig.retrieveConfig().propertiesOverrideMap().clear();
    GrouperSession.stopQuietly(this.rootSession);
    super.tearDown();
  }

  /**
   * configure entry 0 for both require and lockout groups
   * @param allowedToUse value for allowedToUse.0, or null to leave it unset
   */
  private void configure(String allowedToUse) {
    for (String prefix : new String[] {"grouper.requireGroup.", "grouper.lockoutGroup."}) {
      GrouperConfig.retrieveConfig().propertiesOverrideMap().put(prefix + "name.0", ACTIVE);
      // blank (not absent) so a local grouper.properties allowedToUse.0 cannot leak in; blank means unset
      GrouperConfig.retrieveConfig().propertiesOverrideMap().put(prefix + "allowedToUse.0",
          allowedToUse == null ? "" : allowedToUse);
      // blank out entries 1..99 so require/lockout groups configured in a local grouper.properties
      // (e.g. name.1 = ref:employee, which does not exist in the test db) do not leak into this test
      for (int i = 1; i < 100; i++) {
        GrouperConfig.retrieveConfig().propertiesOverrideMap().put(prefix + "name." + i, "");
        GrouperConfig.retrieveConfig().propertiesOverrideMap().put(prefix + "allowedToUse." + i, "");
      }
    }
  }

  /**
   * a member of the require/lockout group who is not in allowedToUse gets no entry
   */
  public void testMemberOfGroupButNotAllowed() {
    configure(ACTIVE_CAN_USE);
    assertEquals(0, GrouperUtil.length(RequireGroup.retrieveAllRequireGroups(SubjectTestHelper.SUBJ0)));
    assertEquals(0, GrouperUtil.length(LockoutGroup.retrieveAllLockoutGroups(SubjectTestHelper.SUBJ0)));
  }

  /**
   * a member of allowedToUse who is not in the require/lockout group gets the entry, and the
   * entry records the allowedToUse group (not the require/lockout group)
   */
  public void testAllowedButNotMemberOfGroup() {
    configure(ACTIVE_CAN_USE);

    List<RequireGroup> requireGroups = RequireGroup.retrieveAllRequireGroups(SubjectTestHelper.SUBJ2);
    assertEquals(1, GrouperUtil.length(requireGroups));
    assertEquals(ACTIVE, requireGroups.get(0).getName());
    assertEquals(ACTIVE, requireGroups.get(0).getRequireGroup().getName());
    assertEquals(ACTIVE_CAN_USE, requireGroups.get(0).getAllowedToUseGroup());

    List<LockoutGroup> lockoutGroups = LockoutGroup.retrieveAllLockoutGroups(SubjectTestHelper.SUBJ2);
    assertEquals(1, GrouperUtil.length(lockoutGroups));
    assertEquals(ACTIVE, lockoutGroups.get(0).getName());
    assertEquals(ACTIVE, lockoutGroups.get(0).getLockoutGroup().getName());
    assertEquals(ACTIVE_CAN_USE, lockoutGroups.get(0).getAllowedToUseGroup());
  }

  /**
   * allowedToUse naming a group that does not exist: no entry for a normal subject, and no
   * exception (before the fix, if the check had read the right key, findByName(..., true) would
   * throw and break the whole policy group template screen)
   */
  public void testMissingAllowedToUseGroupFailsClosed() {
    configure("ref:doesNotExist");
    assertEquals(0, GrouperUtil.length(RequireGroup.retrieveAllRequireGroups(SubjectTestHelper.SUBJ0)));
    assertEquals(0, GrouperUtil.length(RequireGroup.retrieveAllRequireGroups(SubjectTestHelper.SUBJ2)));
    assertEquals(0, GrouperUtil.length(LockoutGroup.retrieveAllLockoutGroups(SubjectTestHelper.SUBJ0)));
    assertEquals(0, GrouperUtil.length(LockoutGroup.retrieveAllLockoutGroups(SubjectTestHelper.SUBJ2)));

    // a null subject (list everything, e.g. for config screens) still gets the entry
    assertEquals(1, GrouperUtil.length(RequireGroup.retrieveAllRequireGroups(null)));
    assertEquals(1, GrouperUtil.length(LockoutGroup.retrieveAllLockoutGroups(null)));
  }

  /**
   * with allowedToUse unset, anyone may use the entry (designed behavior, unchanged)
   */
  public void testUnsetAllowedToUseOffersToAnyone() {
    configure(null);
    assertEquals(1, GrouperUtil.length(RequireGroup.retrieveAllRequireGroups(SubjectTestHelper.SUBJ3)));
    assertEquals(1, GrouperUtil.length(LockoutGroup.retrieveAllLockoutGroups(SubjectTestHelper.SUBJ3)));
    assertNull(RequireGroup.retrieveAllRequireGroups(SubjectTestHelper.SUBJ3).get(0).getAllowedToUseGroup());
  }

  /**
   * root (and wheel) is not restricted by allowedToUse
   */
  public void testRootAllowed() {
    configure(ACTIVE_CAN_USE);
    List<RequireGroup> requireGroups = RequireGroup.retrieveAllRequireGroups(this.rootSession.getSubject());
    assertEquals(1, GrouperUtil.length(requireGroups));
    assertEquals(ACTIVE_CAN_USE, requireGroups.get(0).getAllowedToUseGroup());
    assertEquals(1, GrouperUtil.length(LockoutGroup.retrieveAllLockoutGroups(this.rootSession.getSubject())));
  }
}
