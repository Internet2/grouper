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
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.hooks.examples.GrouperAttributeAssignValueRulesConfigHook;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.AttributeDefPrivilege;
import edu.internet2.middleware.subject.Subject;
import junit.textui.TestRunner;

/**
 * SECURITY tests for RuleEngine: a rule that failed validation must not fire, and a rule whose
 * act-as subject cannot be found must not fire as GrouperSystem.
 *
 * From 5.8.0 the rule DAO no longer drops rules whose ruleValid is not T, and RuleEngine.indexData
 * indexed every rule until 7.6.0 (GRP-7283), so on 5.8.0 - 7.5.x a rule marked INVALID (e.g.
 * "Subject X cannot act as subject GrouperSystem") still fired as its act-as subject.  In every
 * release RuleEngine.fireRule ran a rule as GrouperSystem when its act-as subject was not found.
 *
 * Both cases run on every release.  On 2.6.19, 4.x and 5.0.0 - 5.7.1 the rule DAO still drops
 * rules whose ruleValid is not T, so testInvalidRuleDoesNotFire passes there with or without the
 * patch (it guards against a regression); testMissingActAsSubjectDoesNotFireAsRoot is the case the
 * patch fixes on those releases.
 */
public class RuleEngineInvalidRuleNotFiredTest extends GrouperTest {

  /**
   *
   */
  public RuleEngineInvalidRuleNotFiredTest() {
    super();
  }

  /**
   * @param name
   */
  public RuleEngineInvalidRuleNotFiredTest(String name) {
    super(name);
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new RuleEngineInvalidRuleNotFiredTest("testInvalidRuleDoesNotFire"));
  }

  /**
   * A non-wheel rule author (attribute privileges on the rules attribute defs, ADMIN on two groups)
   * writes a rule on its group acting as GrouperSystem.  Validation marks it INVALID; adding a
   * member to the watched group must not run the rule.  Control: the same rule written by root is
   * valid and fires, so the test can see a firing.
   */
  public void testInvalidRuleDoesNotFire() {

    GrouperSession rootSession = GrouperSession.startRootSession();
    Group owner = null;
    Group watched = null;
    Group controlOwner = null;
    Group controlWatched = null;
    try {
      owner = newGroup(rootSession, "test179:owner");
      watched = newGroup(rootSession, "test179:watched");
      controlOwner = newGroup(rootSession, "test179:controlOwner");
      controlWatched = newGroup(rootSession, "test179:controlWatched");
      owner.grantPriv(SubjectTestHelper.SUBJ1, AccessPrivilege.ADMIN, false);
      watched.grantPriv(SubjectTestHelper.SUBJ1, AccessPrivilege.ADMIN, false);
      grantRuleAttributeDefs(SubjectTestHelper.SUBJ1);

      // control: root may act as GrouperSystem, so this rule is valid
      AttributeAssign controlRule = addRule(controlOwner, controlWatched, SubjectFinder.findRootSubject());
      assertEquals("control rule is valid", "T", ruleValid(controlRule));
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }

    // the author is not wheel and not in rules.act.as.group, so it may not act as GrouperSystem
    GrouperSession subj1Session = GrouperSession.start(SubjectTestHelper.SUBJ1);
    AttributeAssign rule = null;
    try {
      rule = addRule(owner, watched, SubjectFinder.findRootSubject());
    } finally {
      GrouperSession.stopQuietly(subj1Session);
    }
    String valid = ruleValid(rule);
    assertTrue("rule written by a non-wheel author acting as GrouperSystem is invalid: " + valid,
        valid != null && valid.startsWith("INVALID"));

    rootSession = GrouperSession.startRootSession();
    try {
      RuleEngine.clearRuleEngineCache();

      controlWatched = GroupFinder.findByName(rootSession, "test179:controlWatched", true);
      controlWatched.addMember(SubjectTestHelper.SUBJ3, false);
      controlOwner = GroupFinder.findByName(rootSession, "test179:controlOwner", true);
      assertTrue("control: a valid rule fires", controlOwner.hasImmediateMember(SubjectTestHelper.SUBJ3));

      watched = GroupFinder.findByName(rootSession, "test179:watched", true);
      watched.addMember(SubjectTestHelper.SUBJ2, false);
      owner = GroupFinder.findByName(rootSession, "test179:owner", true);
      assertFalse("an INVALID rule must not fire", owner.hasImmediateMember(SubjectTestHelper.SUBJ2));
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }
  }

  /**
   * A rule marked valid whose act-as subject does not resolve (e.g. the subject was deleted or its
   * source is down) must be skipped, not run as GrouperSystem.  The rule values are written with
   * the rules hook turned off so the stored ruleValid stays T, as it would for a rule validated
   * before its act-as subject went away.
   */
  public void testMissingActAsSubjectDoesNotFireAsRoot() {

    GrouperSession rootSession = GrouperSession.startRootSession();
    try {
      final Group owner = newGroup(rootSession, "test179:missingOwner");
      Group watched = newGroup(rootSession, "test179:missingWatched");

      ThreadLocal<Boolean> inValidate = GrouperAttributeAssignValueRulesConfigHook.getThreadLocalInValidateRule();
      inValidate.set(true);
      try {
        AttributeAssign attributeAssign = owner.getAttributeDelegate().addAttribute(RuleUtils.ruleAttributeDefName()).getAttributeAssign();
        attributeAssign.getAttributeValueDelegate().assignValue(RuleUtils.ruleActAsSubjectSourceIdName(), SubjectTestHelper.SUBJ0.getSourceId());
        attributeAssign.getAttributeValueDelegate().assignValue(RuleUtils.ruleActAsSubjectIdName(), "noSuchSubject179");
        attributeAssign.getAttributeValueDelegate().assignValue(RuleUtils.ruleCheckTypeName(), RuleCheckType.membershipAdd.name());
        attributeAssign.getAttributeValueDelegate().assignValue(RuleUtils.ruleCheckOwnerNameName(), watched.getName());
        attributeAssign.getAttributeValueDelegate().assignValue(RuleUtils.ruleThenEnumName(), RuleThenEnum.addMemberToOwnerGroup.name());
        attributeAssign.getAttributeValueDelegate().assignValue(RuleUtils.ruleValidName(), "T");
      } finally {
        inValidate.set(false);
      }

      RuleEngine.clearRuleEngineCache();

      watched.addMember(SubjectTestHelper.SUBJ4, false);
      Group ownerAgain = GroupFinder.findByName(rootSession, owner.getName(), true);
      assertFalse("a rule whose act-as subject is missing must not fire as GrouperSystem",
          ownerAgain.hasImmediateMember(SubjectTestHelper.SUBJ4));
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }
  }

  /**
   * add a rule on the owner group: when a member is added to the watched group, add it to the owner
   * group.  Written in the current session (so the rules hook validates it as that subject)
   * @param owner
   * @param watched
   * @param actAs
   * @return the rule assignment
   */
  private static AttributeAssign addRule(Group owner, Group watched, Subject actAs) {
    AttributeAssign attributeAssign = owner.getAttributeDelegate().addAttribute(RuleUtils.ruleAttributeDefName()).getAttributeAssign();
    attributeAssign.getAttributeValueDelegate().assignValue(RuleUtils.ruleActAsSubjectSourceIdName(), actAs.getSourceId());
    attributeAssign.getAttributeValueDelegate().assignValue(RuleUtils.ruleActAsSubjectIdName(), actAs.getId());
    attributeAssign.getAttributeValueDelegate().assignValue(RuleUtils.ruleCheckTypeName(), RuleCheckType.membershipAdd.name());
    attributeAssign.getAttributeValueDelegate().assignValue(RuleUtils.ruleCheckOwnerNameName(), watched.getName());
    attributeAssign.getAttributeValueDelegate().assignValue(RuleUtils.ruleThenEnumName(), RuleThenEnum.addMemberToOwnerGroup.name());
    return attributeAssign;
  }

  /**
   * the stored ruleValid value, read as root
   * @param attributeAssign
   * @return the value
   */
  private static String ruleValid(final AttributeAssign attributeAssign) {
    return (String)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        return attributeAssign.getAttributeValueDelegate().retrieveValueString(RuleUtils.ruleValidName());
      }
    });
  }

  /**
   * grant the subject read and update on the rules attribute defs (the site grant that lets
   * a non-wheel subject manage rules outside the rules UI)
   * @param subject
   */
  private static void grantRuleAttributeDefs(Subject subject) {
    RuleUtils.ruleTypeAttributeDef().getPrivilegeDelegate().grantPriv(subject, AttributeDefPrivilege.ATTR_UPDATE, false);
    RuleUtils.ruleAttrAttributeDef().getPrivilegeDelegate().grantPriv(subject, AttributeDefPrivilege.ATTR_UPDATE, false);
    RuleUtils.ruleTypeAttributeDef().getPrivilegeDelegate().grantPriv(subject, AttributeDefPrivilege.ATTR_READ, false);
    RuleUtils.ruleAttrAttributeDef().getPrivilegeDelegate().grantPriv(subject, AttributeDefPrivilege.ATTR_READ, false);
  }

  /**
   * @param rootSession
   * @param name
   * @return a new group
   */
  private static Group newGroup(GrouperSession rootSession, String name) {
    return new GroupSave(rootSession).assignName(name).assignCreateParentStemsIfNotExist(true).save();
  }

}
