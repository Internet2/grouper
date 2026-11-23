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
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.AttributeDefPrivilege;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;
import junit.textui.TestRunner;

/**
 * GRP-7596: rule if/then EL can only be saved by a Grouper sysadmin (wheel/root).  With
 * rules.allowUnrestrictedEl it runs unrestricted (not in the JEXL sandbox) with every EL variable,
 * without rules.accessToApiInEl.group.  Without it, it runs sandboxed, and both a failure and a save
 * that will fail name the property.
 *
 * <ul>
 *   <li>runtime: with the switch on, if-condition EL with a constructor, and then EL with the live
 *   group object, fire; with it off the same rule fails with a hint naming the switch</li>
 *   <li>save time: the rules UI warning and the syntax heuristic</li>
 *   <li>attribute value hook: a non-wheel session cannot write if/then EL, and cannot change any other
 *   value of a rule that has it; a wheel member can</li>
 *   <li>send-email: predefined variables only stay in the sandbox, anything else or a template file
 *   runs unrestricted</li>
 * </ul>
 */
public class RuleElSysadminCheckTest extends GrouperTest {

  /**
   *
   */
  public RuleElSysadminCheckTest() {
    super();
  }

  /**
   * @param name
   */
  public RuleElSysadminCheckTest(String name) {
    super(name);
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new RuleElSysadminCheckTest("testIfElConstructorRunsUnrestricted"));
  }

  // ---------------------------------------------------------------- helpers

  /**
   * root makes a group SUBJ0 is ADMIN of
   * @param name group name
   * @return the group
   */
  private static Group newGroup(String name) {
    GrouperSession rootSession = GrouperSession.startRootSession();
    try {
      Group group = new GroupSave(rootSession).assignName(name).assignCreateParentStemsIfNotExist(true).save();
      group.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.ADMIN, false);
      return group;
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }
  }

  /**
   * SUBJ0 may assign rule attributes (as WS or GSH would): grant the rule attribute defs
   */
  private static void grantRuleAttributeDefs() {
    GrouperSession rootSession = GrouperSession.startRootSession();
    try {
      RuleUtils.ruleTypeAttributeDef().getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_UPDATE, false);
      RuleUtils.ruleAttrAttributeDef().getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_UPDATE, false);
      RuleUtils.ruleTypeAttributeDef().getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_READ, false);
      RuleUtils.ruleAttrAttributeDef().getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_READ, false);
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }
  }

  /**
   * the attribute assign read fresh from the database
   * @param attributeAssign assign
   * @return a fresh copy
   */
  private static AttributeAssign fresh(AttributeAssign attributeAssign) {
    return GrouperDAOFactory.getFactory().getAttributeAssign().findById(attributeAssign.getId(), true, false);
  }

  /**
   * a stored rule value, as root, read fresh
   * @param attributeAssign the rule
   * @param name the attribute name
   * @return the value
   */
  private static String ruleValue(final AttributeAssign attributeAssign, final String name) {
    return (String)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        return fresh(attributeAssign).getAttributeValueDelegate().retrieveValueString(name);
      }
    });
  }

  /**
   * with the current session, start a rule on the owner group: act-as and a membership remove check on
   * the check group (no if or then yet)
   * @param ownerGroup group the rule is on
   * @param checkGroup group whose membership remove fires it
   * @param actAs act-as subject
   * @return the rule
   */
  private static AttributeAssign addRuleStart(Group ownerGroup, Group checkGroup, Subject actAs) {
    AttributeAssign attributeAssign = ownerGroup.getAttributeDelegate().addAttribute(RuleUtils.ruleAttributeDefName()).getAttributeAssign();
    attributeAssign.getAttributeValueDelegate().assignValue(RuleUtils.ruleActAsSubjectSourceIdName(), actAs.getSourceId());
    attributeAssign.getAttributeValueDelegate().assignValue(RuleUtils.ruleActAsSubjectIdName(), actAs.getId());
    attributeAssign.getAttributeValueDelegate().assignValue(RuleUtils.ruleCheckOwnerNameName(), checkGroup.getName());
    attributeAssign.getAttributeValueDelegate().assignValue(RuleUtils.ruleCheckTypeName(), RuleCheckType.membershipRemove.name());
    return attributeAssign;
  }

  /**
   * assign a value and expect the hook to refuse it with the EL error
   * @param attributeAssign rule
   * @param name attribute name
   * @param value value
   */
  private static void assertHookRefuses(AttributeAssign attributeAssign, String name, String value) {
    try {
      fresh(attributeAssign).getAttributeValueDelegate().assignValue(name, value);
    } catch (RuntimeException re) {
      String stack = GrouperUtil.getFullStackTrace(re);
      assertTrue("refused with the EL error: " + stack, stack.contains("expression language (EL)"));
      return;
    }
    fail("expected the hook to refuse " + name + " = " + value);
  }

  // ---------------------------------------------------------------- runtime

  /**
   * If-condition EL with new(...) (refused by the 7.9.1 sandbox) and attributeAssignType (only given
   * with rules.accessToApiInEl.group before), with that property blank and rules.allowUnrestrictedEl
   * on: the rule fires.  Like the
   * documented veto pattern that broke in 7.9.1, 6.5.1 and 4.26.1.
   */
  public void testIfElConstructorRunsUnrestricted() {
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("rules.accessToApiInEl.group", "");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(RuleElSysadminCheck.ALLOW_UNRESTRICTED_EL_PROPERTY, "true");
    GrouperSession grouperSession = GrouperSession.startRootSession();

    Group groupA = new GroupSave(grouperSession).assignName("test:ruleElA").assignCreateParentStemsIfNotExist(true).save();
    Group groupB = new GroupSave(grouperSession).assignName("test:ruleElB").assignCreateParentStemsIfNotExist(true).save();

    // if you are out of test:ruleElB, then remove from test:ruleElA
    AttributeAssign rule = addRuleStart(groupA, groupB, SubjectFinder.findRootSubject());
    rule.getAttributeValueDelegate().assignValue(RuleUtils.ruleIfConditionElName(),
        "${new('java.lang.String', 'x') == 'x' && ruleElUtils.hasMembershipByGroupId(attributeAssignType.getOwnerGroupId(), memberId, null, 'true')}");
    rule.getAttributeValueDelegate().assignValue(RuleUtils.ruleThenElName(),
        "${ruleElUtils.removeMemberFromGroupId(ownerGroupId, memberId)}");

    groupB.addMember(SubjectTestHelper.SUBJ0);
    groupA.addMember(SubjectTestHelper.SUBJ0);

    groupB.deleteMember(SubjectTestHelper.SUBJ0);

    assertFalse("the rule fired and removed SUBJ0", groupA.hasMember(SubjectTestHelper.SUBJ0));
  }

  /**
   * Then EL that uses the live group object (only given with rules.accessToApiInEl.group before) and
   * a class object (refused by the 7.9.1 sandbox), with rules.allowUnrestrictedEl on: the rule fires.
   */
  public void testThenElFullApiRunsUnrestricted() {
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("rules.accessToApiInEl.group", "");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(RuleElSysadminCheck.ALLOW_UNRESTRICTED_EL_PROPERTY, "true");
    GrouperSession grouperSession = GrouperSession.startRootSession();

    Group groupA = new GroupSave(grouperSession).assignName("test:ruleElThenA").assignCreateParentStemsIfNotExist(true).save();
    Group groupB = new GroupSave(grouperSession).assignName("test:ruleElThenB").assignCreateParentStemsIfNotExist(true).save();

    AttributeAssign rule = addRuleStart(groupA, groupB, SubjectFinder.findRootSubject());
    rule.getAttributeValueDelegate().assignValue(RuleUtils.ruleThenElName(),
        "${(group.getName() == 'test:ruleElThenB' && ''.getClass().getName() == 'java.lang.String') ? ruleElUtils.removeMemberFromGroupId(ownerGroupId, memberId) : false}");

    groupB.addMember(SubjectTestHelper.SUBJ0);
    groupA.addMember(SubjectTestHelper.SUBJ0);

    groupB.deleteMember(SubjectTestHelper.SUBJ0);

    assertFalse("the rule fired and removed SUBJ0", groupA.hasMember(SubjectTestHelper.SUBJ0));
  }

  /**
   * With rules.allowUnrestrictedEl off (the default), the same constructor rule is sandboxed: the
   * membership remove fails with an error that names the property, and nothing changes.
   */
  public void testSwitchOffFailsWithHint() {
    GrouperSession grouperSession = GrouperSession.startRootSession();

    Group groupA = new GroupSave(grouperSession).assignName("test:ruleElOffA").assignCreateParentStemsIfNotExist(true).save();
    Group groupB = new GroupSave(grouperSession).assignName("test:ruleElOffB").assignCreateParentStemsIfNotExist(true).save();

    AttributeAssign rule = addRuleStart(groupA, groupB, SubjectFinder.findRootSubject());
    rule.getAttributeValueDelegate().assignValue(RuleUtils.ruleIfConditionElName(), "${new('java.lang.String', 'x') == 'x'}");
    rule.getAttributeValueDelegate().assignValue(RuleUtils.ruleThenElName(),
        "${ruleElUtils.removeMemberFromGroupId(ownerGroupId, memberId)}");

    groupB.addMember(SubjectTestHelper.SUBJ0);
    groupA.addMember(SubjectTestHelper.SUBJ0);

    try {
      groupB.deleteMember(SubjectTestHelper.SUBJ0);
      fail("the sandboxed constructor should fail the rule");
    } catch (RuntimeException re) {
      String stack = GrouperUtil.getFullStackTrace(re);
      assertTrue("names the switch: " + stack, stack.contains(RuleElSysadminCheck.ALLOW_UNRESTRICTED_EL_PROPERTY + " = true"));
      assertTrue("names the rule: " + stack, stack.contains(rule.getId()));
    }
    assertTrue("rolled back", groupA.hasMember(SubjectTestHelper.SUBJ0));
    assertTrue("rolled back", groupB.hasMember(SubjectTestHelper.SUBJ0));
  }

  // ---------------------------------------------------------------- save time

  /**
   * The syntax heuristic: constructors, class objects and the full-API objects are refused; plain
   * variables, safeSubject and ruleElUtils are not.
   */
  public void testUsesSandboxRefusedSyntax() {
    assertFalse(RuleElSysadminCheck.usesSandboxRefusedSyntax(null));
    assertFalse("no expression", RuleElSysadminCheck.usesSandboxRefusedSyntax("new(x) group.getName()"));
    assertFalse(RuleElSysadminCheck.usesSandboxRefusedSyntax("${groupName} ${safeSubject.name}"));
    assertFalse(RuleElSysadminCheck.usesSandboxRefusedSyntax("${ruleElUtils.removeMemberFromGroupId(ownerGroupId, memberId)}"));
    assertTrue(RuleElSysadminCheck.usesSandboxRefusedSyntax("${new('java.lang.String', 'x')}"));
    assertTrue(RuleElSysadminCheck.usesSandboxRefusedSyntax("${''.class.forName('java.lang.String')}"));
    assertTrue(RuleElSysadminCheck.usesSandboxRefusedSyntax("${''.getClass()}"));
    assertTrue(RuleElSysadminCheck.usesSandboxRefusedSyntax("${membership.getAttributeValueDelegate()}"));
    assertTrue(RuleElSysadminCheck.usesSandboxRefusedSyntax("${group.getName()}"));
    assertTrue(RuleElSysadminCheck.usesSandboxRefusedSyntax("${subject.getId()}"));
  }

  /**
   * The save-time warning: given for refused if or then EL, or a sysadmin send-email field, while the
   * switch is off; not when the switch is on, the EL is fine, or the email is predefined-only.
   */
  public void testSaveTimeSandboxWarning() {
    RuleDefinition ifRule = new RuleDefinition();
    ifRule.setIfCondition(new RuleIfCondition("${new('java.lang.String', 'x') == 'x'}", null, null, null, null, null, null));
    String warning = RuleElSysadminCheck.saveTimeSandboxWarning(ifRule);
    assertNotNull(warning);
    assertTrue(warning, warning.contains(RuleElSysadminCheck.ALLOW_UNRESTRICTED_EL_PROPERTY));

    RuleDefinition thenRule = new RuleDefinition();
    thenRule.setThen(new RuleThen("${group.getName()}", null, null, null, null));
    assertNotNull(RuleElSysadminCheck.saveTimeSandboxWarning(thenRule));

    RuleDefinition okRule = new RuleDefinition();
    okRule.setThen(new RuleThen("${ruleElUtils.removeMemberFromGroupId(ownerGroupId, memberId)}", null, null, null, null));
    assertNull(RuleElSysadminCheck.saveTimeSandboxWarning(okRule));

    RuleDefinition emailRule = new RuleDefinition();
    emailRule.setThen(new RuleThen(null, RuleThenEnum.sendEmail.name(), "a@b.c", "subject", "${''.getClass().getName()}"));
    assertNotNull(RuleElSysadminCheck.saveTimeSandboxWarning(emailRule));

    RuleDefinition predefinedEmailRule = new RuleDefinition();
    predefinedEmailRule.setThen(new RuleThen(null, RuleThenEnum.sendEmail.name(), "a@b.c", "${groupName}", "${safeSubject.name}"));
    assertNull(RuleElSysadminCheck.saveTimeSandboxWarning(predefinedEmailRule));

    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(RuleElSysadminCheck.ALLOW_UNRESTRICTED_EL_PROPERTY, "true");
    assertNull("no warning with the switch on", RuleElSysadminCheck.saveTimeSandboxWarning(ifRule));
  }

  // ---------------------------------------------------------------- attribute value hook

  /**
   * A non-wheel session (as WS or GSH) cannot write if-condition or then EL, and the refused value is
   * not stored.
   */
  public void testHookNonWheelCannotWriteEl() {
    Group groupA = newGroup("test:ruleElHookA");
    Group groupB = newGroup("test:ruleElHookB");
    grantRuleAttributeDefs();

    GrouperSession subj0Session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      AttributeAssign rule = addRuleStart(groupA, groupB, SubjectTestHelper.SUBJ0);
      assertHookRefuses(rule, RuleUtils.ruleIfConditionElName(), "${true}");
      assertHookRefuses(rule, RuleUtils.ruleThenElName(), "${ruleElUtils.removeMemberFromGroupId(ownerGroupId, memberId)}");
      assertNull("refused if EL not stored", ruleValue(rule, RuleUtils.ruleIfConditionElName()));
      assertNull("refused then EL not stored", ruleValue(rule, RuleUtils.ruleThenElName()));

      // a value of a rule without EL is fine
      fresh(rule).getAttributeValueDelegate().assignValue(RuleUtils.ruleThenEnumName(), RuleThenEnum.removeMemberFromOwnerGroup.name());
      assertEquals(RuleThenEnum.removeMemberFromOwnerGroup.name(), ruleValue(rule, RuleUtils.ruleThenEnumName()));
    } finally {
      GrouperSession.stopQuietly(subj0Session);
    }
  }

  /**
   * Root writes EL; a non-wheel session then cannot change any other value of that rule (the act-as
   * subject, the check owner), which would point the sysadmin's expression somewhere else.
   */
  public void testHookNonWheelCannotEditWheelElRule() {
    Group groupA = newGroup("test:ruleElEditA");
    Group groupB = newGroup("test:ruleElEditB");
    Group groupC = newGroup("test:ruleElEditC");
    grantRuleAttributeDefs();

    AttributeAssign rule = null;
    GrouperSession rootSession = GrouperSession.startRootSession();
    try {
      rule = addRuleStart(groupA, groupB, SubjectFinder.findRootSubject());
      rule.getAttributeValueDelegate().assignValue(RuleUtils.ruleThenElName(), "${ruleElUtils.removeMemberFromGroupId(ownerGroupId, memberId)}");
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }

    GrouperSession subj0Session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      assertHookRefuses(rule, RuleUtils.ruleActAsSubjectIdName(), SubjectTestHelper.SUBJ0.getId());
      assertHookRefuses(rule, RuleUtils.ruleCheckOwnerNameName(), groupC.getName());
    } finally {
      GrouperSession.stopQuietly(subj0Session);
    }
    assertEquals("refused edit leaves the act-as", "GrouperSystem", ruleValue(rule, RuleUtils.ruleActAsSubjectIdName()));
    assertEquals("refused edit leaves the check owner", groupB.getName(), ruleValue(rule, RuleUtils.ruleCheckOwnerNameName()));
  }

  /**
   * A wheel member (not GrouperSystem) may write EL.
   */
  public void testHookWheelMemberMayWriteEl() {
    GrouperSession rootSession = GrouperSession.startRootSession();
    Group wheel = null;
    try {
      wheel = new GroupSave(rootSession).assignName("etc:ruleElWheel").assignCreateParentStemsIfNotExist(true).save();
      wheel.addMember(SubjectTestHelper.SUBJ1, false);
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.wheel.use", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.wheel.group", wheel.getName());

    Group groupA = newGroup("test:ruleElWheelA");
    Group groupB = newGroup("test:ruleElWheelB");

    GrouperSession subj1Session = GrouperSession.start(SubjectTestHelper.SUBJ1);
    try {
      AttributeAssign rule = addRuleStart(groupA, groupB, SubjectFinder.findRootSubject());
      String thenEl = "${ruleElUtils.removeMemberFromGroupId(ownerGroupId, memberId)}";
      fresh(rule).getAttributeValueDelegate().assignValue(RuleUtils.ruleThenElName(), thenEl);
      assertEquals(thenEl, ruleValue(rule, RuleUtils.ruleThenElName()));
    } finally {
      GrouperSession.stopQuietly(subj1Session);
    }
  }

  /**
   * root makes an EL rule: if EL and then EL, with SUBJ0 able to write rule attributes
   * @param prefix group name prefix
   * @return the rule
   */
  private static AttributeAssign rootElRule(String prefix) {
    Group groupA = newGroup(prefix + "A");
    Group groupB = newGroup(prefix + "B");
    grantRuleAttributeDefs();
    GrouperSession rootSession = GrouperSession.startRootSession();
    try {
      AttributeAssign rule = addRuleStart(groupA, groupB, SubjectFinder.findRootSubject());
      rule.getAttributeValueDelegate().assignValue(RuleUtils.ruleIfConditionElName(), "${memberId != null}");
      rule.getAttributeValueDelegate().assignValue(RuleUtils.ruleThenElName(), "${ruleElUtils.removeMemberFromGroupId(ownerGroupId, memberId)}");
      return rule;
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }
  }

  /**
   * delete a value and expect the hook to refuse it with the EL error
   * @param attributeAssign rule
   * @param name attribute name
   * @param value value to delete
   */
  private static void assertHookRefusesDelete(AttributeAssign attributeAssign, String name, String value) {
    try {
      fresh(attributeAssign).getAttributeValueDelegate().deleteValue(name, value);
    } catch (RuntimeException re) {
      String stack = GrouperUtil.getFullStackTrace(re);
      assertTrue("refused with the EL error: " + stack, stack.contains("expression language (EL)"));
      return;
    }
    fail("expected the hook to refuse deleting " + name + " = " + value);
  }

  /**
   * A non-wheel session cannot delete the if-condition EL of a sysadmin's EL rule (the then EL would
   * fire on every event), nor another value of it (the check owner); the values stay.
   */
  public void testHookNonWheelCannotDeleteFromElRule() {
    AttributeAssign rule = rootElRule("test:ruleElDelete");
    String checkOwner = ruleValue(rule, RuleUtils.ruleCheckOwnerNameName());

    GrouperSession subj0Session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      assertHookRefusesDelete(rule, RuleUtils.ruleIfConditionElName(), "${memberId != null}");
      assertHookRefusesDelete(rule, RuleUtils.ruleCheckOwnerNameName(), checkOwner);
    } finally {
      GrouperSession.stopQuietly(subj0Session);
    }
    assertEquals("refused delete leaves the if EL", "${memberId != null}", ruleValue(rule, RuleUtils.ruleIfConditionElName()));
    assertEquals("refused delete leaves the check owner", checkOwner, ruleValue(rule, RuleUtils.ruleCheckOwnerNameName()));
  }

  /**
   * A wheel member may delete the if-condition EL of an EL rule.
   */
  public void testHookWheelMemberMayDeleteFromElRule() {
    AttributeAssign rule = rootElRule("test:ruleElWheelDelete");
    GrouperSession rootSession = GrouperSession.startRootSession();
    Group wheel = null;
    try {
      wheel = new GroupSave(rootSession).assignName("etc:ruleElDeleteWheel").assignCreateParentStemsIfNotExist(true).save();
      wheel.addMember(SubjectTestHelper.SUBJ1, false);
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.wheel.use", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.wheel.group", wheel.getName());

    GrouperSession subj1Session = GrouperSession.start(SubjectTestHelper.SUBJ1);
    try {
      fresh(rule).getAttributeValueDelegate().deleteValue(RuleUtils.ruleIfConditionElName(), "${memberId != null}");
    } finally {
      GrouperSession.stopQuietly(subj1Session);
    }
    assertNull(ruleValue(rule, RuleUtils.ruleIfConditionElName()));
  }

  /**
   * A non-wheel session that may manage the group can still delete the whole rule (removing it runs
   * nothing); only editing it is refused.
   */
  public void testHookNonWheelMayDeleteWholeElRule() {
    AttributeAssign rule = rootElRule("test:ruleElDeleteWhole");
    Group groupA = (Group)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        return edu.internet2.middleware.grouper.GroupFinder.findByName(grouperSession, "test:ruleElDeleteWholeA", true);
      }
    });

    GrouperSession subj0Session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      groupA.getAttributeDelegate().removeAttribute(RuleUtils.ruleAttributeDefName());
    } finally {
      GrouperSession.stopQuietly(subj0Session);
    }
    assertNull("rule is gone", GrouperDAOFactory.getFactory().getAttributeAssign().findById(rule.getId(), false, false));
  }

  // ---------------------------------------------------------------- send-email decision

  /**
   * Send-email: only predefined variables run in the sandbox; anything else, or a template file, runs
   * unrestricted.
   */
  public void testSendEmailRunsUnrestricted() {
    assertFalse(RuleElSysadminCheck.sendEmailRunsUnrestricted(null));
    assertFalse("predefined only", RuleElSysadminCheck.sendEmailRunsUnrestricted(
        new RuleThen(null, RuleThenEnum.sendEmail.name(), "a@b.c, ${safeSubject.emailAddress}",
            "Added to ${groupDisplayExtension}", "Hello ${safeSubject.name}")));
    assertFalse("no expressions", RuleElSysadminCheck.sendEmailRunsUnrestricted(
        new RuleThen(null, RuleThenEnum.sendEmail.name(), "a@b.c", "subject", "body")));
    assertTrue("helper in the body", RuleElSysadminCheck.sendEmailRunsUnrestricted(
        new RuleThen(null, RuleThenEnum.sendEmail.name(), "a@b.c", "subject", "${grouperUtil.toString()}")));
    assertTrue("unknown variable in the recipient", RuleElSysadminCheck.sendEmailRunsUnrestricted(
        new RuleThen(null, RuleThenEnum.sendEmail.name(), "${subject.getId()}", "subject", "body")));
    assertTrue("template subject", RuleElSysadminCheck.sendEmailRunsUnrestricted(
        new RuleThen(null, RuleThenEnum.sendEmail.name(), "a@b.c", "template: mySubject", "body")));
    assertTrue("template body", RuleElSysadminCheck.sendEmailRunsUnrestricted(
        new RuleThen(null, RuleThenEnum.sendEmail.name(), "a@b.c", "subject", " template: myBody")));
  }

}
