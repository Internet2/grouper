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

import java.util.Set;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.Stem.Scope;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
import edu.internet2.middleware.grouper.privs.Privilege;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;
import junit.textui.TestRunner;

/**
 * SECURITY tests for RuleFinder.findSubjectPrivilegeInheritRules(subject, true), which backs the UI
 * screens "privileges inherited from folders assigned to this entity" (UiV2Subject and UiV2Group).
 *
 * The secure filter was inverted: a caller who was NOT STEM_ADMIN on the rule's folder was let
 * through, so any user saw every inheritance rule naming the chosen subject (folder, scope and
 * privileges).  A caller now sees a rule if wheel, STEM_ADMIN on its folder, or an admin of an
 * object in the rule's scope (same as findPrivilegeInheritRules).
 */
public class RuleFinderSubjectPrivilegeInheritSecurityTest extends GrouperTest {

  /**
   *
   */
  public RuleFinderSubjectPrivilegeInheritSecurityTest() {
    super();
  }

  /**
   * @param name
   */
  public RuleFinderSubjectPrivilegeInheritSecurityTest(String name) {
    super(name);
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new RuleFinderSubjectPrivilegeInheritSecurityTest("testSubjectPrivilegeInheritRulesSecure"));
  }

  /**
   * A folder rule grants SUBJ1 READ on new groups.  SUBJ0 (no privileges) must not see it; root,
   * the folder's STEM_ADMIN (SUBJ3) and the ADMIN of a group in the folder (SUBJ4) see it, and
   * secure=false sees it.
   */
  public void testSubjectPrivilegeInheritRulesSecure() {

    String stemId = null;
    GrouperSession rootSession = GrouperSession.startRootSession();
    try {
      Stem folder = new StemSave(rootSession).assignName("test166:folder").assignCreateParentStemsIfNotExist(true).save();
      stemId = folder.getUuid();
      Set<Privilege> read = GrouperUtil.toSet(AccessPrivilege.READ);
      RuleApi.inheritGroupPrivileges(folder, Scope.SUB, SubjectTestHelper.SUBJ1, read);

      folder.grantPriv(SubjectTestHelper.SUBJ3, NamingPrivilege.STEM_ADMIN, false);
      Group group = new GroupSave(rootSession).assignName("test166:folder:someGroup").save();
      group.grantPriv(SubjectTestHelper.SUBJ4, AccessPrivilege.ADMIN, false);

      assertTrue("root sees the rule", hasRuleOnStem(SubjectTestHelper.SUBJ1, true, stemId));
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }

    assertFalse("a user with no privilege on the folder must not see the rule", seesRule(SubjectTestHelper.SUBJ0, stemId, true));
    assertTrue("insecure lookup still finds it", seesRule(SubjectTestHelper.SUBJ0, stemId, false));
    assertTrue("folder admin sees the rule", seesRule(SubjectTestHelper.SUBJ3, stemId, true));
    assertTrue("admin of a group in the rule scope sees the rule", seesRule(SubjectTestHelper.SUBJ4, stemId, true));
  }

  /**
   * @param caller
   * @param stemId
   * @param secure
   * @return if the caller finds the rule naming SUBJ1 on the stem
   */
  private static boolean seesRule(Subject caller, String stemId, boolean secure) {
    GrouperSession session = GrouperSession.start(caller);
    try {
      return hasRuleOnStem(SubjectTestHelper.SUBJ1, secure, stemId);
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * @param subject
   * @param secure
   * @param stemId
   * @return true if the lookup returns a rule owned by the stem
   */
  private static boolean hasRuleOnStem(Subject subject, boolean secure, String stemId) {
    for (RuleDefinition ruleDefinition : GrouperUtil.nonNull(RuleFinder.findSubjectPrivilegeInheritRules(subject, secure))) {
      if (stemId.equals(ruleDefinition.getAttributeAssignType().getOwnerStemId())) {
        return true;
      }
    }
    return false;
  }

}
