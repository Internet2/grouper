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
import java.util.Set;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
import edu.internet2.middleware.subject.Subject;
import junit.textui.TestRunner;

/**
 * SECURITY (GRP-7359) tests for who may add/edit/delete rules through the rules UI helper
 * RuleService.saveOrUpdateRuleAttributes.  Rules always run as GrouperSystem, and the UI invokes
 * that helper inside a root session, so the server-side gate must be enforced against the real
 * (logged in) subject carried on the RuleConfig -- not the current (root) session.
 *
 * Slices covered so far:
 * - EL: a rule that uses expression language runs arbitrary code as GrouperSystem, so only
 *   wheel/root may add, edit, or delete one.
 * - custom: a hand-authored rule (not one of the predefined patterns) can specify an arbitrary
 *   check/if/then, so only wheel/root may add or edit one; non-admins are limited to patterns.
 */
public class RuleSecurityTest extends GrouperTest {

  /** a valid EL "then" for a membershipAdd rule on a group (removes the just-added member) */
  private static final String SAMPLE_THEN_EL = "${ruleElUtils.removeMemberFromGroupId(ownerGroupId, memberId)}";

  /**
   *
   */
  public RuleSecurityTest() {
    super();
  }

  /**
   * @param name
   */
  public RuleSecurityTest(String name) {
    super(name);
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new RuleSecurityTest("testElRuleAddByNonWheelDenied"));
    //TestRunner.run(new RuleSecurityTest("testElRuleAddByRootAllowed"));
  }

  /**
   * SECURITY (GRP-7359): a non-wheel object admin must NOT be able to create an EL rule (which would
   * run arbitrary EL as GrouperSystem).  With the pre-fix code this attempt succeeds and persists a
   * rule (the escalation); with the fix it is rejected before anything is persisted.
   */
  public void testElRuleAddByNonWheelDenied() {

    GrouperSession rootSession = GrouperSession.startRootSession();

    final Group group = new GroupSave(rootSession).assignName("test:testGroupEl")
        .assignCreateParentStemsIfNotExist(true).save();

    // SUBJ0 is a normal (non-wheel) subject; give it ADMIN so the scenario mirrors a real attacker
    // who administers some object.  The EL gate is independent of this, but it makes the case realistic.
    group.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.ADMIN, false);

    final Subject nonWheelAdmin = SubjectTestHelper.SUBJ0;

    // the rules UI runs saveOrUpdateRuleAttributes inside a root session, with the real user on RuleConfig
    @SuppressWarnings("unchecked")
    Map<String, List<String>> result = (Map<String, List<String>>) GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        RuleConfig ruleConfig = new RuleConfig(nonWheelAdmin, group);
        ruleConfig.setCheckType("membershipAdd");
        ruleConfig.setCheckOwner("thisGroup");
        ruleConfig.setThenOption("EL");
        ruleConfig.setThenEl(SAMPLE_THEN_EL);
        return RuleService.saveOrUpdateRuleAttributes(ruleConfig, group, null);
      }
    });

    // the attempt must be rejected with an error
    assertTrue("a non-wheel admin must be denied creating an EL rule", result.containsKey("ERROR"));

    // and nothing may be persisted (the gate runs before any attribute is created)
    Set<RuleDefinition> ruleDefinitions = RuleFinder.retrieveRuleDefinitionsForGrouperObject(group);
    assertEquals("no rule should have been persisted for the denied EL attempt", 0, ruleDefinitions.size());

    GrouperSession.stopQuietly(rootSession);
  }

  /**
   * SECURITY (GRP-7359): a non-wheel object admin must NOT be able to create a custom rule (a
   * hand-authored check/if/then that is not one of the predefined patterns).  This case has NO EL,
   * so it is the custom-rule gate (not the EL gate) that must reject it.  With the pre-fix code the
   * attempt persists a rule; with the fix it is rejected before anything is persisted.
   */
  public void testCustomRuleAddByNonWheelDenied() {

    GrouperSession rootSession = GrouperSession.startRootSession();

    final Group group = new GroupSave(rootSession).assignName("test:testGroupCustom")
        .assignCreateParentStemsIfNotExist(true).save();

    group.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.ADMIN, false);

    final Subject nonWheelAdmin = SubjectTestHelper.SUBJ0;

    @SuppressWarnings("unchecked")
    Map<String, List<String>> result = (Map<String, List<String>>) GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        RuleConfig ruleConfig = new RuleConfig(nonWheelAdmin, group);
        // custom rule: no pattern set, an enum then (NOT EL), so only the custom-rule gate applies
        ruleConfig.setCheckType("membershipAdd");
        ruleConfig.setCheckOwner("thisGroup");
        ruleConfig.setThenOption("addMemberToOwnerGroup");
        return RuleService.saveOrUpdateRuleAttributes(ruleConfig, group, null);
      }
    });

    assertTrue("a non-wheel admin must be denied creating a custom rule", result.containsKey("ERROR"));

    Set<RuleDefinition> ruleDefinitions = RuleFinder.retrieveRuleDefinitionsForGrouperObject(group);
    assertEquals("no rule should have been persisted for the denied custom attempt", 0, ruleDefinitions.size());

    GrouperSession.stopQuietly(rootSession);
  }

  /**
   * SECURITY (GRP-7359): fail closed -- if no configuring subject is supplied on the RuleConfig, the
   * caller cannot be established as a sysadmin, so an EL or custom rule must be rejected rather than
   * silently created as GrouperSystem.  (Every real UI caller sets the subject; this guards a caller
   * that omits it.)
   */
  public void testCustomRuleAddByNullSubjectDenied() {

    GrouperSession rootSession = GrouperSession.startRootSession();

    final Group group = new GroupSave(rootSession).assignName("test:testGroupNullSubj")
        .assignCreateParentStemsIfNotExist(true).save();

    // note: RuleConfig built with a NULL subject
    @SuppressWarnings("unchecked")
    Map<String, List<String>> result = (Map<String, List<String>>) GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        RuleConfig ruleConfig = new RuleConfig(null, group);
        ruleConfig.setCheckType("membershipAdd");
        ruleConfig.setCheckOwner("thisGroup");
        ruleConfig.setThenOption("addMemberToOwnerGroup");
        return RuleService.saveOrUpdateRuleAttributes(ruleConfig, group, null);
      }
    });

    assertTrue("a null-subject custom rule must be denied (fail closed)", result.containsKey("ERROR"));

    Set<RuleDefinition> ruleDefinitions = RuleFinder.retrieveRuleDefinitionsForGrouperObject(group);
    assertEquals("no rule should have been persisted for the null-subject attempt", 0, ruleDefinitions.size());

    GrouperSession.stopQuietly(rootSession);
  }

  /**
   * SECURITY (GRP-7359): wheel/root may create an EL rule -- the gate must not over-block administrators.
   */
  public void testElRuleAddByRootAllowed() {

    GrouperSession rootSession = GrouperSession.startRootSession();

    final Group group = new GroupSave(rootSession).assignName("test:testGroupElRoot")
        .assignCreateParentStemsIfNotExist(true).save();

    final Subject rootSubject = SubjectFinder.findRootSubject();

    @SuppressWarnings("unchecked")
    Map<String, List<String>> result = (Map<String, List<String>>) GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        RuleConfig ruleConfig = new RuleConfig(rootSubject, group);
        ruleConfig.setCheckType("membershipAdd");
        ruleConfig.setCheckOwner("thisGroup");
        ruleConfig.setThenOption("EL");
        ruleConfig.setThenEl(SAMPLE_THEN_EL);
        return RuleService.saveOrUpdateRuleAttributes(ruleConfig, group, null);
      }
    });

    // wheel/root is not blocked by the EL gate
    assertFalse("wheel/root should be allowed to create an EL rule", result.containsKey("ERROR"));

    // the rule reached persistence
    Set<RuleDefinition> ruleDefinitions = RuleFinder.retrieveRuleDefinitionsForGrouperObject(group);
    assertEquals("the EL rule should have been persisted for root", 1, ruleDefinitions.size());

    GrouperSession.stopQuietly(rootSession);
  }

  /**
   * SECURITY (GRP-7359): deleting a rule.  A custom rule (matches no predefined pattern, including
   * EL rules) may be deleted only by wheel/root; a recognized pattern rule may be deleted by a
   * non-admin (object rights are checked elsewhere).  Null subject fails closed.
   */
  public void testAllowedToDeleteRuleGate() {

    GrouperSession rootSession = GrouperSession.startRootSession();

    RuleDefinition patternDefinition = buildInheritedPrivilegesOnGroupsDefinition();
    RuleDefinition customDefinition = buildCustomElDefinition();

    // sanity: our in-memory definitions classify as intended
    assertNotNull("pattern definition should match a recognized pattern", patternDefinition.getPattern());
    assertNull("custom/EL definition should match no pattern", customDefinition.getPattern());

    Subject nonWheelAdmin = SubjectTestHelper.SUBJ0;
    Subject rootSubject = SubjectFinder.findRootSubject();

    // a recognized pattern rule can be deleted by a non-admin
    assertTrue(RuleService.allowedToManageRule(nonWheelAdmin, patternDefinition));
    // a custom rule requires wheel/root
    assertFalse(RuleService.allowedToManageRule(nonWheelAdmin, customDefinition));
    assertTrue(RuleService.allowedToManageRule(rootSubject, customDefinition));
    // fail closed on a null subject
    assertFalse(RuleService.allowedToManageRule(null, patternDefinition));

    GrouperSession.stopQuietly(rootSession);
  }

  /**
   * SECURITY (GRP-7359): a rule must match the pattern the request claims, so a non-admin cannot
   * label a rule as one pattern while building another (or a custom rule).
   */
  public void testRuleMatchesClaimedPattern() {

    GrouperSession rootSession = GrouperSession.startRootSession();

    RuleDefinition patternDefinition = buildInheritedPrivilegesOnGroupsDefinition();
    RuleDefinition customDefinition = buildCustomElDefinition();

    // correct claim matches
    assertTrue(RuleService.ruleMatchesClaimedPattern(patternDefinition, "InheritedPrivilegesOnGroups"));
    // claiming a different pattern does not match
    assertFalse(RuleService.ruleMatchesClaimedPattern(patternDefinition, "VetoInFolderIfNotEligibleDueToGroup"));
    // a custom rule matches no named pattern
    assertFalse(RuleService.ruleMatchesClaimedPattern(customDefinition, "InheritedPrivilegesOnGroups"));
    // a blank or "custom" claim is treated as a match (the custom-rule gate handles that case)
    assertTrue(RuleService.ruleMatchesClaimedPattern(patternDefinition, ""));
    assertTrue(RuleService.ruleMatchesClaimedPattern(patternDefinition, "custom"));
    // an unknown pattern name is never a match
    assertFalse(RuleService.ruleMatchesClaimedPattern(patternDefinition, "NoSuchPatternXyz"));

    GrouperSession.stopQuietly(rootSession);
  }

  /**
   * SECURITY (GRP-7359): the rule service must enforce the selected pattern's own validate() (which
   * checks the caller's privilege on referenced objects and the pattern's input constraints), not
   * trust that the UI submit already did.  Here a non-wheel caller submits a real pattern with an
   * invalid input, so validate() fails and the service must reject before anything is persisted.
   */
  public void testPatternValidateEnforcedByService() {

    GrouperSession rootSession = GrouperSession.startRootSession();

    final Stem ownerFolder = new StemSave(rootSession).assignName("test:ruleOwnerFolder")
        .assignCreateParentStemsIfNotExist(true).save();
    final Group referencedGroup = new GroupSave(rootSession).assignName("test:ruleReferencedGroup")
        .assignCreateParentStemsIfNotExist(true).save();
    // realistic: the non-wheel caller administers the folder where the rule would live
    ownerFolder.grantPriv(SubjectTestHelper.SUBJ0, NamingPrivilege.STEM_ADMIN, false);

    final Subject nonWheelAdmin = SubjectTestHelper.SUBJ0;

    @SuppressWarnings("unchecked")
    Map<String, List<String>> result = (Map<String, List<String>>) GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        RuleConfig ruleConfig = new RuleConfig(nonWheelAdmin, ownerFolder);
        ruleConfig.setPattern("VetoInFolderIfNotEligibleDueToGroup");
        ruleConfig.getPatternPropertiesValues().put("VetoInFolderIfNotEligibleDueToGroup.groupName", referencedGroup.getName());
        // an invalid stem scope makes the pattern's own validate() fail (valid values are SUB/ONE)
        ruleConfig.getPatternPropertiesValues().put("VetoInFolderIfNotEligibleDueToGroup.stemScope", "BOGUS");
        return RuleService.saveOrUpdateRuleAttributes(ruleConfig, ownerFolder, null);
      }
    });

    assertTrue("the service must enforce the pattern's validate()", result.containsKey("ERROR"));

    Set<RuleDefinition> ruleDefinitions = RuleFinder.retrieveRuleDefinitionsForGrouperObject(ownerFolder);
    assertEquals("no rule should be persisted when pattern validation fails", 0, ruleDefinitions.size());

    GrouperSession.stopQuietly(rootSession);
  }

  /**
   * SECURITY (GRP-7359): an explicitly blank pattern is the custom path and must be rejected for a
   * non-wheel caller (pinning the blank case, complementing the null/unset custom tests).
   */
  public void testBlankPatternAddByNonWheelDenied() {

    GrouperSession rootSession = GrouperSession.startRootSession();

    final Group group = new GroupSave(rootSession).assignName("test:testGroupBlankPattern")
        .assignCreateParentStemsIfNotExist(true).save();

    group.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.ADMIN, false);

    final Subject nonWheelAdmin = SubjectTestHelper.SUBJ0;

    @SuppressWarnings("unchecked")
    Map<String, List<String>> result = (Map<String, List<String>>) GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        RuleConfig ruleConfig = new RuleConfig(nonWheelAdmin, group);
        ruleConfig.setPattern("");
        ruleConfig.setCheckType("membershipAdd");
        ruleConfig.setCheckOwner("thisGroup");
        ruleConfig.setThenOption("addMemberToOwnerGroup");
        return RuleService.saveOrUpdateRuleAttributes(ruleConfig, group, null);
      }
    });

    assertTrue("an explicitly blank pattern must be rejected for a non-wheel caller", result.containsKey("ERROR"));

    Set<RuleDefinition> ruleDefinitions = RuleFinder.retrieveRuleDefinitionsForGrouperObject(group);
    assertEquals("no rule should be persisted for the blank-pattern attempt", 0, ruleDefinitions.size());

    GrouperSession.stopQuietly(rootSession);
  }

  /**
   * SECURITY (GRP-7359): a non-wheel object admin must not be able to edit (overwrite) an existing
   * custom/EL rule that a sysadmin created, even by submitting a valid pattern as the new content --
   * the same restriction as delete.  Here root creates an EL rule, then a non-wheel admin tries to
   * edit it; the edit must be rejected and the original rule left untouched.
   */
  public void testEditExistingCustomRuleByNonWheelDenied() {

    GrouperSession rootSession = GrouperSession.startRootSession();

    final Group group = new GroupSave(rootSession).assignName("test:testGroupEditCustom")
        .assignCreateParentStemsIfNotExist(true).save();
    group.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.ADMIN, false);

    final Subject rootSubject = SubjectFinder.findRootSubject();

    // root creates a custom (EL) rule
    @SuppressWarnings("unchecked")
    Map<String, List<String>> createResult = (Map<String, List<String>>) GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        RuleConfig ruleConfig = new RuleConfig(rootSubject, group);
        ruleConfig.setCheckType("membershipAdd");
        ruleConfig.setCheckOwner("thisGroup");
        ruleConfig.setThenOption("EL");
        ruleConfig.setThenEl(SAMPLE_THEN_EL);
        return RuleService.saveOrUpdateRuleAttributes(ruleConfig, group, null);
      }
    });
    assertFalse("root should be allowed to create the custom rule", createResult.containsKey("ERROR"));

    Set<RuleDefinition> ruleDefinitions = RuleFinder.retrieveRuleDefinitionsForGrouperObject(group);
    assertEquals("the custom rule should exist before the edit attempt", 1, ruleDefinitions.size());
    final String existingRuleId = ruleDefinitions.iterator().next().getAttributeAssignType().getId();

    // a non-wheel admin tries to edit that existing custom rule, claiming a valid pattern as new content
    @SuppressWarnings("unchecked")
    Map<String, List<String>> editResult = (Map<String, List<String>>) GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        RuleConfig ruleConfig = new RuleConfig(SubjectTestHelper.SUBJ0, group);
        ruleConfig.setPattern("AddMemberToGroupIfAddedToAnotherGroup");
        return RuleService.saveOrUpdateRuleAttributes(ruleConfig, group, existingRuleId);
      }
    });

    assertTrue("a non-wheel admin must not be able to edit an existing custom/EL rule", editResult.containsKey("ERROR"));

    // the original rule is untouched (still exactly one rule on the group)
    assertEquals("the existing rule should be unchanged after the denied edit", 1,
        RuleFinder.retrieveRuleDefinitionsForGrouperObject(group).size());

    GrouperSession.stopQuietly(rootSession);
  }

  /**
   * SECURITY (GRP-7381): who may create a sendEmail rule -- the default-closed layered gate.
   * both gates blank => wheel/root only; email-sender group set => its members; else rules-UI
   * restrict group set => its members; null subject => denied.
   */
  public void testAllowedToCreateEmailRuleGate() {

    GrouperSession rootSession = GrouperSession.startRootSession();

    Group senders = new GroupSave(rootSession).assignName("test:ruleEmailSenders")
        .assignCreateParentStemsIfNotExist(true).save();
    senders.addMember(SubjectTestHelper.SUBJ0);

    Subject inGroup = SubjectTestHelper.SUBJ0;      // member of the designated group
    Subject notInGroup = SubjectTestHelper.SUBJ1;   // not a member
    Subject rootSubject = SubjectFinder.findRootSubject();

    GrouperConfig config = GrouperConfig.retrieveConfig();
    try {

      // (a) both gates blank -> only wheel/root
      config.propertiesOverrideMap().put("rules.restrictRulesEmailSendersToMembersOfThisGroupName", "");
      config.propertiesOverrideMap().put("rules.restrictRulesUiToMembersOfThisGroupName", "");
      assertTrue("wheel/root always allowed", RuleService.allowedToCreateEmailRule(rootSubject));
      assertFalse("both gates blank -> non-wheel denied", RuleService.allowedToCreateEmailRule(inGroup));
      assertFalse("null subject denied (fail closed)", RuleService.allowedToCreateEmailRule(null));

      // (b) email-sender group set -> only its members
      config.propertiesOverrideMap().put("rules.restrictRulesEmailSendersToMembersOfThisGroupName", senders.getName());
      assertTrue("member of email-sender group allowed", RuleService.allowedToCreateEmailRule(inGroup));
      assertFalse("non-member of email-sender group denied", RuleService.allowedToCreateEmailRule(notInGroup));

      // (c) email gate blank but rules-UI restrict group set -> its members (vetted rule editors)
      config.propertiesOverrideMap().put("rules.restrictRulesEmailSendersToMembersOfThisGroupName", "");
      config.propertiesOverrideMap().put("rules.restrictRulesUiToMembersOfThisGroupName", senders.getName());
      assertTrue("member of rules-UI restrict group allowed", RuleService.allowedToCreateEmailRule(inGroup));
      assertFalse("non-member of rules-UI restrict group denied", RuleService.allowedToCreateEmailRule(notInGroup));

      // (d) both gates blank but the explicit opt-out is set -> non-wheel allowed (previous open behavior)
      config.propertiesOverrideMap().put("rules.restrictRulesUiToMembersOfThisGroupName", "");
      config.propertiesOverrideMap().put("rules.allowSendEmailRulesWhenNoGroupConfigured", "true");
      assertTrue("allowSendEmailRulesWhenNoGroupConfigured=true -> non-wheel allowed", RuleService.allowedToCreateEmailRule(notInGroup));

    } finally {
      config.propertiesOverrideMap().remove("rules.restrictRulesEmailSendersToMembersOfThisGroupName");
      config.propertiesOverrideMap().remove("rules.restrictRulesUiToMembersOfThisGroupName");
      config.propertiesOverrideMap().remove("rules.allowSendEmailRulesWhenNoGroupConfigured");
      GrouperSession.stopQuietly(rootSession);
    }
  }

  /**
   * build an in-memory rule definition that matches the InheritedPrivilegesOnGroups pattern
   * (groupCreate check, blank if-condition, assignGroupPrivilegeToGroupId then).  No persistence.
   * @return the rule definition
   */
  private static RuleDefinition buildInheritedPrivilegesOnGroupsDefinition() {
    RuleDefinition ruleDefinition = new RuleDefinition();
    RuleCheck ruleCheck = new RuleCheck();
    ruleCheck.setCheckType(RuleCheckType.groupCreate.name());
    ruleDefinition.setCheck(ruleCheck);
    ruleDefinition.setIfCondition(new RuleIfCondition());
    RuleThen ruleThen = new RuleThen();
    ruleThen.setThenEnum(RuleThenEnum.assignGroupPrivilegeToGroupId.name());
    ruleDefinition.setThen(ruleThen);
    return ruleDefinition;
  }

  /**
   * build an in-memory custom rule definition (an EL then) that matches no predefined pattern.
   * @return the rule definition
   */
  private static RuleDefinition buildCustomElDefinition() {
    RuleDefinition ruleDefinition = new RuleDefinition();
    RuleCheck ruleCheck = new RuleCheck();
    ruleCheck.setCheckType(RuleCheckType.membershipAdd.name());
    ruleDefinition.setCheck(ruleCheck);
    ruleDefinition.setIfCondition(new RuleIfCondition());
    RuleThen ruleThen = new RuleThen();
    ruleThen.setThenEl(SAMPLE_THEN_EL);
    ruleDefinition.setThen(ruleThen);
    return ruleDefinition;
  }

}
