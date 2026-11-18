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

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.misc.GrouperObject;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
import edu.internet2.middleware.subject.Subject;
import junit.textui.TestRunner;

/**
 * SECURITY: the rules UI "Run daemon" setting must be enforced on the server.
 *
 * The UI shows the run daemon control only when RuleConfig.isCanSetDaemon() is true, but the group
 * and folder submit actions read grouperRuleRunDaemon from the request regardless, and
 * RuleService.saveOrUpdateRuleAttributes stored it as root.  A non-admin could forge runDaemon=true
 * on a pattern reserved for admins, or on a rule whose check type has no daemon (which then throws
 * every time the rule is processed for its owner).
 *
 * Fixed behavior (2026-11-13): when the user may not set run daemon, the submitted value (true and
 * false) is ignored with no error: a new rule stores it blank, an edit keeps the stored value.
 * Wheel/root and non-admins on a daemon-assignable pattern behave as before, and true on a rule with
 * no daemon is refused for everyone.
 *
 * Each save here runs in a root session with the real user on the RuleConfig, as the UI does.
 * Before the fix this class does not compile (RuleDaemonSettingCheck does not exist), and the forged
 * saves succeed with runDaemon "T".
 *
 * The pattern property keys are the 7.x ones; this test was compiled against 7.x only.
 */
public class RuleRunDaemonForgedFlagTest extends GrouperTest {

  /**
   *
   */
  public RuleRunDaemonForgedFlagTest() {
    super();
  }

  /**
   * @param name
   */
  public RuleRunDaemonForgedFlagTest(String name) {
    super(name);
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new RuleRunDaemonForgedFlagTest("testForgedDaemonOnFolderRuleIgnored"));
  }

  // ---------------------------------------------------------------- scenario

  /** folder SUBJ0 is STEM_ADMIN of */
  private Stem folder;
  /** group SUBJ0 is ADMIN of */
  private Group grp;
  /** another group SUBJ0 is ADMIN of */
  private Group grp2;
  /** group SUBJ0 can READ, used as an eligibility group */
  private Group eligible;

  /**
   * root makes the folder and groups
   * @param rootSession root
   */
  private void setupObjects(GrouperSession rootSession) {
    this.folder = new StemSave(rootSession).assignName("test:ruleDaemonFolder").assignCreateParentStemsIfNotExist(true).save();
    this.grp = new GroupSave(rootSession).assignName("test:ruleDaemonFolder:grp").assignCreateParentStemsIfNotExist(true).save();
    this.grp2 = new GroupSave(rootSession).assignName("test:ruleDaemonFolder:grp2").assignCreateParentStemsIfNotExist(true).save();
    this.eligible = new GroupSave(rootSession).assignName("test2:ruleDaemonEligible").assignCreateParentStemsIfNotExist(true).save();

    this.folder.grantPriv(SubjectTestHelper.SUBJ0, NamingPrivilege.STEM_ADMIN, false);
    this.grp.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.ADMIN, false);
    this.grp2.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.ADMIN, false);
    this.eligible.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.READ, false);
    this.eligible.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.VIEW, false);
  }

  // ---------------------------------------------------------------- helpers

  /**
   * Save a predefined pattern on an object, as the UI does (root session, real user on RuleConfig).
   * @param subject the logged in user
   * @param owner the group or folder in the request
   * @param pattern the pattern name
   * @param properties pattern property name/value pairs (name without the pattern prefix)
   * @param runDaemon the forged or real grouperRuleRunDaemon value
   * @param attributeAssignId the rule id for an edit, null for an add
   * @return the result map from the save
   */
  @SuppressWarnings("unchecked")
  private static Map<String, List<String>> savePattern(final Subject subject, final GrouperObject owner,
      final String pattern, final String[] properties, final Boolean runDaemon, final String attributeAssignId) {
    return (Map<String, List<String>>)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        RuleConfig ruleConfig = new RuleConfig(subject, owner);
        ruleConfig.setPattern(pattern);
        for (int i = 0; i < properties.length; i += 2) {
          ruleConfig.getPatternPropertiesValues().put(pattern + "." + properties[i], properties[i + 1]);
        }
        ruleConfig.setRunDaemon(runDaemon);
        return RulePattern.valueOf(pattern).save(ruleConfig, attributeAssignId);
      }
    });
  }

  /**
   * AddSelfPrivilegesToNewGroups: folder rule, not daemon applicable (same shape as the reproduced
   * AddDisabledDateOnMembershipInFolder, which only exists from 7.4/7.5; this one exists in every
   * release the fix covers)
   */
  private static final String FOLDER_PATTERN = "AddSelfPrivilegesToNewGroups";

  /** properties for FOLDER_PATTERN */
  private static final String[] FOLDER_PATTERN_PROPS = {"privilegesToAdd", "admin", "stemScope", "SUB"};

  /** RemoveInvalidMembershipDueToFolder: group rule, daemon applicable, admin only */
  private static final String ADMIN_ONLY_PATTERN = "RemoveInvalidMembershipDueToFolder";

  /**
   * properties for ADMIN_ONLY_PATTERN
   * @return the properties
   */
  private String[] adminOnlyPatternProps() {
    return new String[] {"folder", this.folder.getName(), "stemScope", "SUB"};
  }

  /** RemoveInvalidMembershipDueToGroup: daemon applicable and assignable by a non-admin */
  private static final String NON_ADMIN_PATTERN = "RemoveInvalidMembershipDueToGroup";

  /**
   * properties for NON_ADMIN_PATTERN
   * @return the properties
   */
  private String[] nonAdminPatternProps() {
    return new String[] {"group", this.eligible.getName(), "checkIfRemovedFromGroup", "T"};
  }

  /**
   * The rule assigned directly on this group or folder (not rules that only reference it).
   * @param owner the group or folder
   * @return the rule, or null if none
   */
  private static RuleDefinition ownedRule(GrouperObject owner) {
    RuleEngine.clearRuleEngineCache();
    RuleDefinition result = null;
    for (RuleDefinition ruleDefinition : RuleFinder.retrieveRuleDefinitionsForGrouperObject(owner)) {
      AttributeAssign attributeAssign = ruleDefinition.getAttributeAssignType();
      if (attributeAssign != null && (StringUtils.equals(owner.getId(), attributeAssign.getOwnerGroupId())
          || StringUtils.equals(owner.getId(), attributeAssign.getOwnerStemId()))) {
        assertNull("expected at most one rule owned by " + owner.getName(), result);
        result = ruleDefinition;
      }
    }
    return result;
  }

  /**
   * assert the save was refused with the run daemon error
   * @param message message
   * @param result the save result
   */
  private static void assertDaemonRefused(String message, Map<String, List<String>> result) {
    assertTrue(message + ": " + result, result.containsKey("ERROR"));
    assertTrue(message + ": " + result, result.get("ERROR").contains(RuleDaemonSettingCheck.cannotSetDaemonError()));
  }

  // ---------------------------------------------------------------- tests

  /**
   * The reproduced case: a folder admin forges runDaemon=true on a folder pattern that has no
   * daemon.  The forged value is ignored with no error: the add stores run daemon blank and edits
   * keep the stored value, for true and for false.  Root is not ignored: true is refused (the pattern
   * has no daemon at all) and false is stored.
   */
  public void testForgedDaemonOnFolderRuleIgnored() {
    GrouperSession rootSession = GrouperSession.startRootSession();
    setupObjects(rootSession);

    // add with the forged flag: saved, run daemon blank
    Map<String, List<String>> result = savePattern(SubjectTestHelper.SUBJ0, this.folder, FOLDER_PATTERN,
        FOLDER_PATTERN_PROPS, Boolean.TRUE, null);
    assertFalse("forged run daemon on a folder rule must be ignored, not an error: " + result, result.containsKey("ERROR"));
    RuleDefinition folderRule = ownedRule(this.folder);
    assertNotNull("the rule is saved", folderRule);
    assertTrue("forged run daemon is not stored", StringUtils.isBlank(folderRule.getRunDaemon()));
    String folderRuleId = folderRule.getAttributeAssignType().getId();

    // edit with the forged flag: saved, still blank
    result = savePattern(SubjectTestHelper.SUBJ0, this.folder, FOLDER_PATTERN, FOLDER_PATTERN_PROPS, Boolean.TRUE, folderRuleId);
    assertFalse("forged run daemon on an edit must be ignored: " + result, result.containsKey("ERROR"));
    folderRule = ownedRule(this.folder);
    assertEquals(folderRuleId, folderRule.getAttributeAssignType().getId());
    assertTrue("run daemon still not set", StringUtils.isBlank(folderRule.getRunDaemon()));

    // a forged false is ignored too
    result = savePattern(SubjectTestHelper.SUBJ0, this.folder, FOLDER_PATTERN, FOLDER_PATTERN_PROPS, Boolean.FALSE, folderRuleId);
    assertFalse("forged false must be ignored: " + result, result.containsKey("ERROR"));
    assertTrue("run daemon still not set", StringUtils.isBlank(ownedRule(this.folder).getRunDaemon()));

    // root: the pattern has no daemon, so true is refused for everyone
    result = savePattern(SubjectFinder.findRootSubject(), this.folder, FOLDER_PATTERN, FOLDER_PATTERN_PROPS, Boolean.TRUE, folderRuleId);
    assertDaemonRefused("run daemon on a pattern without a daemon must be refused for root", result);
    assertTrue("refused save changes nothing", StringUtils.isBlank(ownedRule(this.folder).getRunDaemon()));

    // root may store false
    result = savePattern(SubjectFinder.findRootSubject(), this.folder, FOLDER_PATTERN, FOLDER_PATTERN_PROPS, Boolean.FALSE, folderRuleId);
    assertFalse("root false should be saved: " + result, result.containsKey("ERROR"));
    assertEquals("F", ownedRule(this.folder).getRunDaemon());

    // a later forged true or absent by the folder admin keeps root's F
    result = savePattern(SubjectTestHelper.SUBJ0, this.folder, FOLDER_PATTERN, FOLDER_PATTERN_PROPS, Boolean.TRUE, folderRuleId);
    assertFalse("forged true must be ignored: " + result, result.containsKey("ERROR"));
    assertEquals("stored value kept", "F", ownedRule(this.folder).getRunDaemon());
    result = savePattern(SubjectTestHelper.SUBJ0, this.folder, FOLDER_PATTERN, FOLDER_PATTERN_PROPS, null, folderRuleId);
    assertFalse("normal edit should work: " + result, result.containsKey("ERROR"));
    assertEquals("stored value kept", "F", ownedRule(this.folder).getRunDaemon());

    GrouperSession.stopQuietly(rootSession);
  }

  /**
   * A group admin forges run daemon on a daemon pattern reserved for admins: ignored, no error.  A
   * new rule stores it blank; an edit keeps the stored T or F.  Root may set T and F.
   */
  public void testForgedDaemonOnAdminOnlyPatternIgnored() {
    GrouperSession rootSession = GrouperSession.startRootSession();
    setupObjects(rootSession);

    // forged true on a new rule: saved blank
    Map<String, List<String>> result = savePattern(SubjectTestHelper.SUBJ0, this.grp, ADMIN_ONLY_PATTERN,
        adminOnlyPatternProps(), Boolean.TRUE, null);
    assertFalse("forged run daemon on an admin only pattern must be ignored: " + result, result.containsKey("ERROR"));
    RuleDefinition rule = ownedRule(this.grp);
    assertNotNull("the rule is saved", rule);
    assertTrue("forged run daemon is not stored", StringUtils.isBlank(rule.getRunDaemon()));
    String ruleId = rule.getAttributeAssignType().getId();

    // forged false on the blank rule: still blank
    result = savePattern(SubjectTestHelper.SUBJ0, this.grp, ADMIN_ONLY_PATTERN, adminOnlyPatternProps(), Boolean.FALSE, ruleId);
    assertFalse("forged false must be ignored: " + result, result.containsKey("ERROR"));
    assertTrue("blank kept", StringUtils.isBlank(ownedRule(this.grp).getRunDaemon()));

    // root may set true on the same rule
    result = savePattern(SubjectFinder.findRootSubject(), this.grp, ADMIN_ONLY_PATTERN, adminOnlyPatternProps(), Boolean.TRUE, ruleId);
    assertFalse("root may set run daemon on an admin only pattern: " + result, result.containsKey("ERROR"));
    assertEquals("T", ownedRule(this.grp).getRunDaemon());

    // forged false, and absent, on the T rule: still T
    result = savePattern(SubjectTestHelper.SUBJ0, this.grp, ADMIN_ONLY_PATTERN, adminOnlyPatternProps(), Boolean.FALSE, ruleId);
    assertFalse("forged false must be ignored: " + result, result.containsKey("ERROR"));
    assertEquals("T kept", "T", ownedRule(this.grp).getRunDaemon());
    result = savePattern(SubjectTestHelper.SUBJ0, this.grp, ADMIN_ONLY_PATTERN, adminOnlyPatternProps(), null, ruleId);
    assertFalse("normal edit should work: " + result, result.containsKey("ERROR"));
    assertEquals("T kept", "T", ownedRule(this.grp).getRunDaemon());

    // root may set false (on another group); a forged true then keeps F
    result = savePattern(SubjectFinder.findRootSubject(), this.grp2, ADMIN_ONLY_PATTERN, adminOnlyPatternProps(), Boolean.FALSE, null);
    assertFalse("root may set run daemon false: " + result, result.containsKey("ERROR"));
    RuleDefinition rule2 = ownedRule(this.grp2);
    assertEquals("F", rule2.getRunDaemon());
    result = savePattern(SubjectTestHelper.SUBJ0, this.grp2, ADMIN_ONLY_PATTERN, adminOnlyPatternProps(), Boolean.TRUE,
        rule2.getAttributeAssignType().getId());
    assertFalse("forged true must be ignored: " + result, result.containsKey("ERROR"));
    assertEquals("F kept", "F", ownedRule(this.grp2).getRunDaemon());

    GrouperSession.stopQuietly(rootSession);
  }

  /**
   * RemoveInvalidMembershipDueToGroup is daemon-assignable by a non-admin, so a group admin may set it.
   */
  public void testDaemonAssignableByNonAdminAllowed() {
    GrouperSession rootSession = GrouperSession.startRootSession();
    setupObjects(rootSession);

    Map<String, List<String>> result = savePattern(SubjectTestHelper.SUBJ0, this.grp, NON_ADMIN_PATTERN,
        nonAdminPatternProps(), Boolean.TRUE, null);
    assertFalse("non-admin may set run daemon on this pattern: " + result, result.containsKey("ERROR"));
    RuleDefinition rule = ownedRule(this.grp);
    assertEquals("T", rule.getRunDaemon());

    // the control is shown, so false is used too
    result = savePattern(SubjectTestHelper.SUBJ0, this.grp, NON_ADMIN_PATTERN, nonAdminPatternProps(), Boolean.FALSE,
        rule.getAttributeAssignType().getId());
    assertFalse("non-admin may set run daemon false on this pattern: " + result, result.containsKey("ERROR"));
    assertEquals("F", ownedRule(this.grp).getRunDaemon());

    GrouperSession.stopQuietly(rootSession);
  }

  /**
   * A refused save must change nothing that is stored.  Reproduced case: an existing
   * RemoveInvalidMembershipDueToGroup rule fires on removal with the daemon on; an edit to "fires on
   * add" (a check type with no daemon) with the daemon still on is refused.  Before the fix the
   * refused edit still switched the trigger to "add" and only turned the daemon off.  Also: a
   * refused add leaves no rule behind, and a valid edit still saves.
   */
  public void testRefusedEditLeavesRuleUnchanged() {
    GrouperSession rootSession = GrouperSession.startRootSession();
    setupObjects(rootSession);

    // the existing rule: fires on removal, daemon on
    Map<String, List<String>> result = savePattern(SubjectTestHelper.SUBJ0, this.grp, NON_ADMIN_PATTERN,
        nonAdminPatternProps(), Boolean.TRUE, null);
    assertFalse("setup add should work: " + result, result.containsKey("ERROR"));
    RuleDefinition rule = ownedRule(this.grp);
    assertNotNull(rule);
    String ruleId = rule.getAttributeAssignType().getId();
    String checkTypeBefore = rule.getCheck().getCheckType();
    String checkOwnerBefore = rule.getCheck().getCheckOwnerName();
    String thenEnumBefore = rule.getThen().getThenEnum();
    assertEquals("T", rule.getRunDaemon());

    // the refused edit: fires on add, daemon still on
    String[] addTriggerProps = {"group", this.eligible.getName(), "checkIfRemovedFromGroup", "F"};
    result = savePattern(SubjectTestHelper.SUBJ0, this.grp, NON_ADMIN_PATTERN, addTriggerProps, Boolean.TRUE, ruleId);
    assertTrue("edit to a trigger with no daemon, daemon on, must be refused: " + result, result.containsKey("ERROR"));

    // every stored part of the rule is as it was
    rule = ownedRule(this.grp);
    assertNotNull("the refused edit must not delete the rule", rule);
    assertEquals(ruleId, rule.getAttributeAssignType().getId());
    assertEquals("trigger must not change on a refused edit", checkTypeBefore, rule.getCheck().getCheckType());
    assertEquals(checkOwnerBefore, rule.getCheck().getCheckOwnerName());
    assertEquals(thenEnumBefore, rule.getThen().getThenEnum());
    assertEquals("run daemon must not change on a refused edit", "T", rule.getRunDaemon());

    // a refused add leaves nothing behind
    result = savePattern(SubjectTestHelper.SUBJ0, this.grp2, NON_ADMIN_PATTERN, addTriggerProps, Boolean.TRUE, null);
    assertTrue("add with a trigger with no daemon, daemon on, must be refused: " + result, result.containsKey("ERROR"));
    assertNull("a refused add must leave no rule", ownedRule(this.grp2));

    // the same trigger change with the daemon off is a valid edit and is saved
    result = savePattern(SubjectTestHelper.SUBJ0, this.grp, NON_ADMIN_PATTERN, addTriggerProps, Boolean.FALSE, ruleId);
    assertFalse("valid edit should work: " + result, result.containsKey("ERROR"));
    rule = ownedRule(this.grp);
    assertEquals(ruleId, rule.getAttributeAssignType().getId());
    assertFalse("valid edit changes the trigger", StringUtils.equals(checkTypeBefore, rule.getCheck().getCheckType()));
    assertEquals("F", rule.getRunDaemon());

    GrouperSession.stopQuietly(rootSession);
  }

  /**
   * The helper class on its own: the subject check, and the capability check that the old code
   * ignored.
   */
  public void testRuleDaemonSettingCheck() {
    GrouperSession rootSession = GrouperSession.startRootSession();
    setupObjects(rootSession);

    // who may set it: ignored unless wheel/root or the UI shows the control
    RuleConfig ruleConfig = new RuleConfig(SubjectTestHelper.SUBJ0, this.grp);
    ruleConfig.setPattern(ADMIN_ONLY_PATTERN);
    assertTrue("non-admin on an admin only pattern is ignored", RuleDaemonSettingCheck.isSubmittedDaemonSettingIgnored(ruleConfig));

    ruleConfig.setPattern(NON_ADMIN_PATTERN);
    assertFalse("non-admin on a non-admin pattern is used", RuleDaemonSettingCheck.isSubmittedDaemonSettingIgnored(ruleConfig));

    ruleConfig.setPattern(FOLDER_PATTERN);
    assertTrue("non-admin on a pattern with no daemon is ignored", RuleDaemonSettingCheck.isSubmittedDaemonSettingIgnored(ruleConfig));

    ruleConfig.setPattern("notARealPattern");
    assertTrue("unknown pattern fails closed", RuleDaemonSettingCheck.isSubmittedDaemonSettingIgnored(ruleConfig));

    RuleConfig rootConfig = new RuleConfig(SubjectFinder.findRootSubject(), this.grp);
    rootConfig.setPattern(ADMIN_ONLY_PATTERN);
    assertFalse("root on an admin only pattern is used", RuleDaemonSettingCheck.isSubmittedDaemonSettingIgnored(rootConfig));
    rootConfig.setPattern(FOLDER_PATTERN);
    assertFalse("root on a pattern with no daemon is used (and checked)", RuleDaemonSettingCheck.isSubmittedDaemonSettingIgnored(rootConfig));

    RuleConfig noSubjectConfig = new RuleConfig(null, this.grp);
    noSubjectConfig.setPattern(ADMIN_ONLY_PATTERN);
    assertTrue("no configuring subject fails closed", RuleDaemonSettingCheck.isSubmittedDaemonSettingIgnored(noSubjectConfig));

    // the value that will be stored
    rootConfig.setRunDaemon(Boolean.TRUE);
    assertEquals("not ignored: the submitted value", Boolean.TRUE, RuleDaemonSettingCheck.effectiveRunDaemon(rootConfig, false, null));
    assertNull("ignored on a new rule: blank", RuleDaemonSettingCheck.effectiveRunDaemon(rootConfig, true, null));

    // capability check: flattenedMembershipAddInFolder never runs a daemon
    RuleDefinition ruleDefinition = new RuleDefinition();
    RuleCheck ruleCheck = new RuleCheck();
    ruleCheck.setCheckType(RuleCheckType.flattenedMembershipAddInFolder.name());
    ruleDefinition.setCheck(ruleCheck);
    ruleDefinition.setIfCondition(new RuleIfCondition());
    ruleDefinition.setThen(new RuleThen());
    assertNotNull("true on a check type without a daemon", RuleDaemonSettingCheck.daemonCapabilityError(Boolean.TRUE, ruleDefinition));
    assertNull("false is not checked", RuleDaemonSettingCheck.daemonCapabilityError(Boolean.FALSE, ruleDefinition));
    assertNull("absent is not checked", RuleDaemonSettingCheck.daemonCapabilityError((Boolean)null, ruleDefinition));

    GrouperSession.stopQuietly(rootSession);
  }

}
