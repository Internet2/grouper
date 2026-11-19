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

import java.util.HashSet;
import java.util.Set;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.Stem.Scope;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
import edu.internet2.middleware.grouper.privs.Privilege;
import edu.internet2.middleware.subject.Subject;

/**
 * <pre>
 * Security test for the global "Rules configurations" UI screen (UiV2Rules.viewAllRules):
 * RuleFinder.retrieveRuleDefinitionsForSubject returns every rule in the root loaded rule engine,
 * and RuleDefinitionVisibility.filterForSubject must cut that down to rules whose owner and
 * referenced objects the subject can see.  Wheel and root still see everything.
 *
 * Setup as root:
 * - test:ruleVis:visible and test:ruleVis:visible2: SUBJ0 has READ
 * - test:ruleVis:hidden: SUBJ0 has nothing, GrouperAll VIEW / READ revoked
 * - rule A: owner visible, check owner visible2 (visible, visible reference)  -> SUBJ0 sees it
 * - rule B: owner hidden, check owner visible                                -> hidden
 * - rule C: owner visible, check owner hidden (visible owner, hidden ref)    -> hidden
 * - rule D: inherited group privileges on folder test:ruleVis:adminFolder, where SUBJ0 is
 *   STEM_ADMIN                                                               -> SUBJ0 sees it
 *
 * Before the fix RuleDefinitionVisibility does not exist, so this does not compile; the UI
 * behavior it guards is the reproduction in the security report.
 * </pre>
 */
public class RuleDefinitionVisibilityTest extends GrouperTest {

  /**
   * run one test
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new RuleDefinitionVisibilityTest("testNormalSubjectSeesOnlyVisibleRules"));
  }

  /**
   *
   */
  public RuleDefinitionVisibilityTest() {
    super();
  }

  /**
   * @param name
   */
  public RuleDefinitionVisibilityTest(String name) {
    super(name);
  }

  /** owner visible, reference visible */
  private Group visible;

  /** second visible group, used as a reference */
  private Group visible2;

  /** group SUBJ0 cannot see */
  private Group hidden;

  /** folder SUBJ0 administers */
  private Stem adminFolder;

  /** rule assignment ids */
  private String ruleAId;
  /** */
  private String ruleBId;
  /** */
  private String ruleCId;
  /** */
  private String ruleDId;

  /**
   * build the groups and rules as root
   */
  private void setupRules() {
    GrouperSession rootSession = GrouperSession.startRootSession();
    try {
      Subject all = SubjectFinder.findAllSubject();

      this.visible = new GroupSave(rootSession).assignName("test:ruleVis:visible")
          .assignCreateParentStemsIfNotExist(true).save();
      this.visible2 = new GroupSave(rootSession).assignName("test:ruleVis:visible2")
          .assignCreateParentStemsIfNotExist(true).save();
      this.hidden = new GroupSave(rootSession).assignName("test:ruleVis:hidden")
          .assignCreateParentStemsIfNotExist(true).save();
      this.adminFolder = new StemSave(rootSession).assignName("test:ruleVis:adminFolder")
          .assignCreateParentStemsIfNotExist(true).save();

      // do not depend on the groups.create.grant.all.* defaults
      for (Group group : new Group[] {this.visible, this.visible2, this.hidden}) {
        group.revokePriv(all, AccessPrivilege.READ, false);
        group.revokePriv(all, AccessPrivilege.VIEW, false);
      }
      this.visible.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.READ, false);
      this.visible2.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.READ, false);
      this.adminFolder.grantPriv(SubjectTestHelper.SUBJ0, NamingPrivilege.STEM_ADMIN, false);

      Subject root = rootSession.getSubject();
      this.ruleAId = RuleApi.groupIntersection(root, this.visible, this.visible2).getId();
      this.ruleBId = RuleApi.groupIntersection(root, this.hidden, this.visible).getId();
      this.ruleCId = RuleApi.groupIntersection(root, this.visible, this.hidden).getId();

      Set<Privilege> privileges = new HashSet<Privilege>();
      privileges.add(AccessPrivilege.READ);
      this.ruleDId = RuleApi.inheritGroupPrivileges(this.adminFolder, Scope.SUB,
          SubjectTestHelper.SUBJ2, privileges).getId();

      // the global screen reads the cached rule engine
      RuleEngine.clearRuleEngineCache();
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }
  }

  /**
   * filter the global rule list for a subject, the way UiV2Rules.viewAllRules does
   * @param subject
   * @param assertNoHidden true to assert no returned rule names the hidden group (not for admins)
   * @return the ids of the rule assignments the subject sees
   */
  private Set<String> visibleRuleIds(Subject subject, boolean assertNoHidden) {
    GrouperSession session = GrouperSession.start(subject);
    try {
      Set<RuleDefinition> ruleDefinitions = RuleDefinitionVisibility.filterForSubject(
          RuleFinder.retrieveRuleDefinitionsForSubject(subject), subject);
      Set<String> ids = new HashSet<String>();
      for (RuleDefinition ruleDefinition : ruleDefinitions) {
        ids.add(ruleDefinition.getAttributeAssignType().getId());
        // nothing shown may name or point at the hidden group
        String text = ruleDefinition.toString();
        assertFalse("rule shown to " + subject.getId() + " names the hidden group: " + text,
            assertNoHidden && text != null && (text.contains(this.hidden.getName()) || text.contains(this.hidden.getId())));
      }
      return ids;
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * a normal subject sees only rules whose owner and references it can see
   */
  public void testNormalSubjectSeesOnlyVisibleRules() {
    setupRules();

    // sanity: the unfiltered finder returns all four (this is the leak)
    GrouperSession rootSession = GrouperSession.startRootSession();
    Set<String> allIds = new HashSet<String>();
    try {
      for (RuleDefinition ruleDefinition : RuleFinder.retrieveRuleDefinitionsForSubject(SubjectTestHelper.SUBJ0)) {
        allIds.add(ruleDefinition.getAttributeAssignType().getId());
      }
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }
    assertTrue(allIds.contains(this.ruleAId));
    assertTrue(allIds.contains(this.ruleBId));
    assertTrue(allIds.contains(this.ruleCId));
    assertTrue(allIds.contains(this.ruleDId));

    Set<String> ids = visibleRuleIds(SubjectTestHelper.SUBJ0, true);
    assertTrue("rule on a visible group with a visible reference", ids.contains(this.ruleAId));
    assertFalse("rule owned by a hidden group", ids.contains(this.ruleBId));
    assertFalse("rule on a visible group that references a hidden group", ids.contains(this.ruleCId));
    assertTrue("rule on a folder the subject administers", ids.contains(this.ruleDId));
  }

  /**
   * a subject with no privileges on anything sees none of these rules
   */
  public void testUnprivilegedSubjectSeesNone() {
    setupRules();

    Set<String> ids = visibleRuleIds(SubjectTestHelper.SUBJ3, true);
    assertFalse(ids.contains(this.ruleAId));
    assertFalse(ids.contains(this.ruleBId));
    assertFalse(ids.contains(this.ruleCId));

    // the folder rule depends on whether folders are visible to everyone in this config
    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ3);
    boolean folderFindable;
    try {
      folderFindable = StemFinder.findByUuid(session, this.adminFolder.getId(), false) != null;
    } finally {
      GrouperSession.stopQuietly(session);
    }
    assertEquals(folderFindable, ids.contains(this.ruleDId));
  }

  /**
   * a null subject or empty input gives an empty set
   */
  public void testFailClosed() {
    setupRules();
    GrouperSession rootSession = GrouperSession.startRootSession();
    try {
      assertEquals(0, RuleDefinitionVisibility.filterForSubject(
          RuleFinder.retrieveRuleDefinitionsForSubject(null), null).size());
      assertEquals(0, RuleDefinitionVisibility.filterForSubject(null, SubjectTestHelper.SUBJ0).size());
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }
  }

  /**
   * root keeps the full inventory
   */
  public void testRootSeesAll() {
    setupRules();

    Set<String> rootIds = visibleRuleIds(SubjectFinder.findRootSubject(), false);
    assertTrue(rootIds.contains(this.ruleAId));
    assertTrue(rootIds.contains(this.ruleBId));
    assertTrue(rootIds.contains(this.ruleCId));
    assertTrue(rootIds.contains(this.ruleDId));
  }

  /**
   * a wheel member keeps the full inventory
   */
  public void testWheelSeesAll() {
    setupRules();

    GrouperSession rootSession = GrouperSession.startRootSession();
    try {
      Group wheel = new GroupSave(rootSession).assignName("etc:testWheelRuleVis")
          .assignCreateParentStemsIfNotExist(true).save();
      wheel.addMember(SubjectTestHelper.SUBJ1, false);
      GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.wheel.use", "true");
      GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.wheel.group", wheel.getName());
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }

    // wheel sees all, so the hidden group check in visibleRuleIds does not apply
    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ1);
    Set<String> ids = new HashSet<String>();
    try {
      for (RuleDefinition ruleDefinition : RuleDefinitionVisibility.filterForSubject(
          RuleFinder.retrieveRuleDefinitionsForSubject(SubjectTestHelper.SUBJ1), SubjectTestHelper.SUBJ1)) {
        ids.add(ruleDefinition.getAttributeAssignType().getId());
      }
    } finally {
      GrouperSession.stopQuietly(session);
    }
    assertTrue(ids.contains(this.ruleAId));
    assertTrue(ids.contains(this.ruleBId));
    assertTrue(ids.contains(this.ruleCId));
    assertTrue(ids.contains(this.ruleDId));
  }

}
