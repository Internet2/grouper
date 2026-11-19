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
package edu.internet2.middleware.grouper.rules;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import junit.textui.TestRunner;

/**
 * SECURITY tests for the rules.restrictRulesUiToMembersOfThisGroupName gate
 * (RuleUiRestrictGroup.allowedToEditRulesInUi), which the rules UI beans RulesContainer.isCanAddRule
 * and GuiRuleDefinition.isCanEditRule call before their ADMIN / STEM_ADMIN check.
 *
 * The UI used to look the restrict group up in the logged-in user's session.  The users the
 * restriction targets usually cannot VIEW that group, so the lookup returned null and the gate
 * was skipped (it only logged a warning).  A mistyped or deleted group name failed open the same
 * way.  The gate must resolve the group as root and fail closed.
 */
public class RuleRestrictRulesUiGroupTest extends GrouperTest {

  /** the rules.restrictRulesUiToMembersOfThisGroupName config key */
  private static final String RESTRICT_KEY = "rules.restrictRulesUiToMembersOfThisGroupName";

  /**
   *
   */
  public RuleRestrictRulesUiGroupTest() {
    super();
  }

  /**
   * @param name
   */
  public RuleRestrictRulesUiGroupTest(String name) {
    super(name);
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new RuleRestrictRulesUiGroupTest("testRestrictRulesUiGroupNotViewableFailsClosed"));
  }

  /**
   * A group admin who is not in the restrict group, and cannot VIEW it, must be denied.  With the
   * pre-fix code the same user got through the gate, because the user-session lookup returned null.
   * Also covers: a member is allowed, a missing group fails closed, a blank gate is open, wheel/root
   * is always allowed, and a null subject is denied.
   */
  public void testRestrictRulesUiGroupNotViewableFailsClosed() {

    GrouperSession rootSession = GrouperSession.startRootSession();

    Group rulesEditors = new GroupSave(rootSession).assignName("etc:rulesEditors")
        .assignCreateParentStemsIfNotExist(true).save();

    // the realistic setup, and the one that failed open: the excluded users cannot see the restrict group.
    // GrouperTest grants GrouperAll READ and VIEW on create, so take both away
    rulesEditors.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.READ, false);
    rulesEditors.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.VIEW, false);
    rulesEditors.addMember(SubjectTestHelper.SUBJ1);

    // SUBJ0 is the attacker: ADMIN on a group of its own, so it passes the later ADMIN check in the UI
    Group mine = new GroupSave(rootSession).assignName("test:mine")
        .assignCreateParentStemsIfNotExist(true).save();
    mine.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.ADMIN, false);

    String rulesEditorsName = rulesEditors.getName();
    GrouperSession.stopQuietly(rootSession);

    GrouperConfig config = GrouperConfig.retrieveConfig();
    GrouperSession subj0Session = null;
    try {

      config.propertiesOverrideMap().put(RESTRICT_KEY, rulesEditorsName);

      // run as the excluded user, which is the session the UI beans see
      subj0Session = GrouperSession.start(SubjectTestHelper.SUBJ0);
      assertNull("precondition: SUBJ0 cannot see the restrict group",
          GroupFinder.findByName(subj0Session, rulesEditorsName, false));

      // (a) restrict group exists but is not viewable by the caller -> non-member denied, member allowed
      assertFalse("non-member who cannot VIEW the restrict group must be denied",
          RuleUiRestrictGroup.allowedToEditRulesInUi(SubjectTestHelper.SUBJ0));
      assertTrue("member of the restrict group allowed",
          RuleUiRestrictGroup.allowedToEditRulesInUi(SubjectTestHelper.SUBJ1));

      // (b) configured group does not exist -> fail closed for everyone but wheel/root
      config.propertiesOverrideMap().put(RESTRICT_KEY, "etc:doesNotExist");
      assertFalse("missing restrict group must fail closed",
          RuleUiRestrictGroup.allowedToEditRulesInUi(SubjectTestHelper.SUBJ0));
      assertFalse("missing restrict group must fail closed for former members too",
          RuleUiRestrictGroup.allowedToEditRulesInUi(SubjectTestHelper.SUBJ1));
      assertTrue("wheel/root always allowed",
          RuleUiRestrictGroup.allowedToEditRulesInUi(SubjectFinder.findRootSubject()));

      // (c) gate blank (the default) -> open, the UI then applies its ADMIN / STEM_ADMIN check
      config.propertiesOverrideMap().put(RESTRICT_KEY, "");
      assertTrue("blank gate does not restrict",
          RuleUiRestrictGroup.allowedToEditRulesInUi(SubjectTestHelper.SUBJ0));

      // (d) no subject -> denied
      assertFalse("null subject denied", RuleUiRestrictGroup.allowedToEditRulesInUi(null));

    } finally {
      config.propertiesOverrideMap().remove(RESTRICT_KEY);
      GrouperSession.stopQuietly(subj0Session);
    }
  }

}
