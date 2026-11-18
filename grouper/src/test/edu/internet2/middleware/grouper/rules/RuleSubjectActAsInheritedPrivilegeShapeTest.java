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

import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.privs.AttributeDefPrivilege;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
import edu.internet2.middleware.subject.Subject;
import junit.textui.TestRunner;

/**
 * SECURITY tests for the inherited-privilege exemption in RuleSubjectActAs.allowedToActAs
 * (rules.allowActAsGrouperSystemForInheritedStemPrivileges, default true): a folder rule written
 * by a non-wheel subject may act as GrouperSystem only if it is a plain inherited-privilege rule.
 *
 * The exemption checked the check type, the owner and the then enum, but not the if condition or
 * the then EL, so a folder admin could save a valid groupCreate rule acting as GrouperSystem with
 * an ifConditionEl calling ruleElUtils, which RuleEngine.fireRule evaluates in a GrouperSystem
 * session when a group is created in the folder.
 */
public class RuleSubjectActAsInheritedPrivilegeShapeTest extends GrouperTest {

  /** an EL a non-wheel author must not get evaluated as GrouperSystem */
  private static final String RULE_EL = "${ruleElUtils.addMemberToGroupName('test187:protected', 'jdbc', 'test.subject.0', null)}";

  /**
   *
   */
  public RuleSubjectActAsInheritedPrivilegeShapeTest() {
    super();
  }

  /**
   * @param name
   */
  public RuleSubjectActAsInheritedPrivilegeShapeTest(String name) {
    super(name);
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new RuleSubjectActAsInheritedPrivilegeShapeTest("testInheritedPrivilegeExemptionShape"));
  }

  /**
   * The exemption covers the plain rule and the folder name pattern condition, and nothing else:
   * not an if EL, not another if condition, not a then EL, not a then enum for another check type.
   */
  public void testInheritedPrivilegeExemptionShape() {

    GrouperSession rootSession = GrouperSession.startRootSession();
    Stem folder = null;
    try {
      folder = new StemSave(rootSession).assignName("test187:folder").assignCreateParentStemsIfNotExist(true).save();
      new GroupSave(rootSession).assignName("test187:protected").assignCreateParentStemsIfNotExist(true).save();
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }

    GrouperSession subj0Session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      Subject root = SubjectFinder.findRootSubject();

      // positive controls: the shapes the privilege inheritance screens and RuleApi save
      RuleDefinition plain = inheritedRule(folder, RuleCheckType.groupCreate, RuleThenEnum.assignGroupPrivilegeToGroupId);
      assertTrue("plain inherited group privilege rule", RuleSubjectActAs.allowedToActAs(plain, SubjectTestHelper.SUBJ0, root));
      assertNull("plain rule validates", plain.validate());

      RuleDefinition sqlLike = inheritedRule(folder, RuleCheckType.groupCreate, RuleThenEnum.assignGroupPrivilegeToGroupId);
      sqlLike.getIfCondition().setIfConditionEnum(RuleIfConditionEnum.nameMatchesSqlLikeString.name());
      sqlLike.getIfCondition().setIfConditionEnumArg0("test187:folder:%");
      assertTrue("name pattern condition", RuleSubjectActAs.allowedToActAs(sqlLike, SubjectTestHelper.SUBJ0, root));

      RuleDefinition stemRule = inheritedRule(folder, RuleCheckType.stemCreate, RuleThenEnum.assignStemPrivilegeToStemId);
      assertTrue("plain inherited folder privilege rule", RuleSubjectActAs.allowedToActAs(stemRule, SubjectTestHelper.SUBJ0, root));

      // the escalation: an if EL evaluated as GrouperSystem
      RuleDefinition ifEl = inheritedRule(folder, RuleCheckType.groupCreate, RuleThenEnum.assignGroupPrivilegeToGroupId);
      ifEl.getIfCondition().setIfConditionEl(RULE_EL);
      assertFalse("if EL is not an inherited privilege rule", RuleSubjectActAs.allowedToActAs(ifEl, SubjectTestHelper.SUBJ0, root));
      String error = ifEl.validate();
      assertTrue("validate refuses the act as: " + error, error != null && error.contains("cannot act as"));

      // another if condition (it reads another group as GrouperSystem)
      RuleDefinition otherIf = inheritedRule(folder, RuleCheckType.groupCreate, RuleThenEnum.assignGroupPrivilegeToGroupId);
      otherIf.getIfCondition().setIfConditionEnum(RuleIfConditionEnum.groupHasNoImmediateEnabledMembership.name());
      otherIf.getIfCondition().setIfOwnerName("test187:protected");
      assertFalse("other if condition", RuleSubjectActAs.allowedToActAs(otherIf, SubjectTestHelper.SUBJ0, root));

      // a then EL next to the then enum
      RuleDefinition thenEl = inheritedRule(folder, RuleCheckType.groupCreate, RuleThenEnum.assignGroupPrivilegeToGroupId);
      thenEl.getThen().setThenEl(RULE_EL);
      assertFalse("then EL", RuleSubjectActAs.allowedToActAs(thenEl, SubjectTestHelper.SUBJ0, root));

      // a then enum that does not belong to the check type
      RuleDefinition mismatch = inheritedRule(folder, RuleCheckType.stemCreate, RuleThenEnum.assignGroupPrivilegeToGroupId);
      assertFalse("then enum of another check type", RuleSubjectActAs.allowedToActAs(mismatch, SubjectTestHelper.SUBJ0, root));

    } finally {
      GrouperSession.stopQuietly(subj0Session);
    }
  }

  /**
   * The rules hook path (WS assignAttributes, GSH): a folder admin with the rules attribute defs
   * writes the escalation rule; it is stored INVALID, not T.
   */
  public void testHookMarksIfElRuleInvalid() {

    GrouperSession rootSession = GrouperSession.startRootSession();
    Stem folder = null;
    try {
      folder = new StemSave(rootSession).assignName("test187:hookFolder").assignCreateParentStemsIfNotExist(true).save();
      new GroupSave(rootSession).assignName("test187:protected").assignCreateParentStemsIfNotExist(true).save();
      folder.grantPriv(SubjectTestHelper.SUBJ0, NamingPrivilege.STEM_ADMIN, false);
      RuleUtils.ruleTypeAttributeDef().getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_UPDATE, false);
      RuleUtils.ruleAttrAttributeDef().getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_UPDATE, false);
      RuleUtils.ruleTypeAttributeDef().getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_READ, false);
      RuleUtils.ruleAttrAttributeDef().getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_READ, false);
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }

    AttributeAssign rule = null;
    GrouperSession subj0Session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      Subject root = SubjectFinder.findRootSubject();
      rule = folder.getAttributeDelegate().addAttribute(RuleUtils.ruleAttributeDefName()).getAttributeAssign();
      rule.getAttributeValueDelegate().assignValue(RuleUtils.ruleActAsSubjectSourceIdName(), root.getSourceId());
      rule.getAttributeValueDelegate().assignValue(RuleUtils.ruleActAsSubjectIdName(), root.getId());
      rule.getAttributeValueDelegate().assignValue(RuleUtils.ruleCheckTypeName(), RuleCheckType.groupCreate.name());
      rule.getAttributeValueDelegate().assignValue(RuleUtils.ruleCheckStemScopeName(), Stem.Scope.SUB.name());
      rule.getAttributeValueDelegate().assignValue(RuleUtils.ruleThenEnumName(), RuleThenEnum.assignGroupPrivilegeToGroupId.name());
      rule.getAttributeValueDelegate().assignValue(RuleUtils.ruleThenEnumArg0Name(),
          SubjectTestHelper.SUBJ1.getSourceId() + " :::: " + SubjectTestHelper.SUBJ1.getId());
      rule.getAttributeValueDelegate().assignValue(RuleUtils.ruleThenEnumArg1Name(), "read");
      rule.getAttributeValueDelegate().assignValue(RuleUtils.ruleRunDaemonName(), "F");

      assertEquals("the plain rule is valid for the folder admin", "T", ruleValue(rule, RuleUtils.ruleValidName()));

      rule.getAttributeValueDelegate().assignValue(RuleUtils.ruleIfConditionElName(), RULE_EL);
    } finally {
      GrouperSession.stopQuietly(subj0Session);
    }

    String valid = ruleValue(rule, RuleUtils.ruleValidName());
    assertTrue("rule with an if EL acting as GrouperSystem is invalid: " + valid, valid != null && valid.startsWith("INVALID"));
  }

  /**
   * an in-memory inherited-privilege rule on the folder, acting as GrouperSystem
   * @param folder
   * @param checkType
   * @param thenEnum
   * @return the rule
   */
  private static RuleDefinition inheritedRule(Stem folder, RuleCheckType checkType, RuleThenEnum thenEnum) {
    RuleDefinition ruleDefinition = new RuleDefinition();

    AttributeAssign attributeAssign = new AttributeAssign();
    attributeAssign.setOwnerStemId(folder.getUuid());
    ruleDefinition.setAttributeAssignType(attributeAssign);

    Subject root = SubjectFinder.findRootSubject();
    RuleSubjectActAs actAs = new RuleSubjectActAs();
    actAs.setSourceId(root.getSourceId());
    actAs.setSubjectId(root.getId());
    ruleDefinition.setActAs(actAs);

    RuleCheck ruleCheck = new RuleCheck();
    ruleCheck.setCheckType(checkType.name());
    ruleCheck.setCheckStemScope(Stem.Scope.SUB.name());
    ruleDefinition.setCheck(ruleCheck);

    ruleDefinition.setIfCondition(new RuleIfCondition());

    RuleThen ruleThen = new RuleThen();
    ruleThen.setThenEnum(thenEnum.name());
    ruleThen.setThenEnumArg0(SubjectTestHelper.SUBJ1.getSourceId() + " :::: " + SubjectTestHelper.SUBJ1.getId());
    ruleThen.setThenEnumArg1(thenEnum == RuleThenEnum.assignStemPrivilegeToStemId ? NamingPrivilege.STEM_ADMIN.getName() : "read");
    ruleDefinition.setThen(ruleThen);

    return ruleDefinition;
  }

  /**
   * a rule value, read as root
   * @param attributeAssign
   * @param name
   * @return the value
   */
  private static String ruleValue(final AttributeAssign attributeAssign, final String name) {
    return (String)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        return attributeAssign.getAttributeValueDelegate().retrieveValueString(name);
      }
    });
  }
}
