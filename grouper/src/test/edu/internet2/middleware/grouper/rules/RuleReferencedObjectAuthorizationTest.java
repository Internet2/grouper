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
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.AttributeDefPrivilege;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import junit.textui.TestRunner;

/**
 * SECURITY tests for RuleReferencedObjectAuthorization (called from RuleCheck.validate): a rule's
 * check owner and if owner, when they are not the rule's own owner, must be readable by the rule's
 * act-as subject.
 *
 * Rules evaluate those references as GrouperSystem, so before the fix a group admin could put a
 * rule on their own group that vetoes adds unless the subject is in a private group (a membership
 * oracle), or that copies a private group's new members into their group.
 */
public class RuleReferencedObjectAuthorizationTest extends GrouperTest {

  /**
   *
   */
  public RuleReferencedObjectAuthorizationTest() {
    super();
  }

  /**
   * @param name
   */
  public RuleReferencedObjectAuthorizationTest(String name) {
    super(name);
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new RuleReferencedObjectAuthorizationTest("testIfOwnerMustBeReadable"));
  }

  /**
   * set up: SUBJ0 is ADMIN of test188:mine and may write rule attributes; test188:private hides its
   * members (GrouperAll READ and VIEW revoked) and has SUBJ2 as a member
   */
  private void setUpGroups() {
    GrouperSession rootSession = GrouperSession.startRootSession();
    try {
      Group mine = new GroupSave(rootSession).assignName("test188:mine").assignCreateParentStemsIfNotExist(true).save();
      Group privateGroup = new GroupSave(rootSession).assignName("test188:private").assignCreateParentStemsIfNotExist(true).save();
      privateGroup.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.READ, false);
      privateGroup.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.VIEW, false);
      privateGroup.addMember(SubjectTestHelper.SUBJ2, false);
      mine.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.ADMIN, false);
      RuleUtils.ruleTypeAttributeDef().getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_UPDATE, false);
      RuleUtils.ruleAttrAttributeDef().getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_UPDATE, false);
      RuleUtils.ruleTypeAttributeDef().getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_READ, false);
      RuleUtils.ruleAttrAttributeDef().getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_READ, false);
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }
  }

  /**
   * The membership oracle: veto adds to my group unless the subject is in the private group
   * (RuleApi.vetoMembershipIfNotInGroup, act as self).  Refused while SUBJ0 cannot READ the private
   * group, allowed once it can.  A missing group gets the same message.  Wheel is not checked.
   */
  public void testIfOwnerMustBeReadable() {
    setUpGroups();

    GrouperSession subj0Session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      Group mine = GroupFinder.findByName(subj0Session, "test188:mine", true);
      Group privateGroup = findAsRoot("test188:private");
      try {
        RuleApi.vetoMembershipIfNotInGroup(SubjectTestHelper.SUBJ0, mine, privateGroup, "rule.entity.must.be.in.private", "Must be in private");
        fail("rule referencing a group the act-as subject cannot read must be invalid");
      } catch (RuntimeException re) {
        String stack = GrouperUtil.getFullStackTrace(re);
        assertTrue("refused by the referenced object check: " + stack,
            stack.contains(RuleReferencedObjectAuthorization.NOT_ALLOWED_MESSAGE));
      }
    } finally {
      GrouperSession.stopQuietly(subj0Session);
    }

    // a name that does not exist: same message (no existence oracle)
    subj0Session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      RuleDefinition missing = vetoIfNotInGroupRule("test188:mine", "test188:doesNotExist");
      String error = missing.validate();
      assertTrue("missing reference gets the same message: " + error,
          error != null && error.startsWith(RuleReferencedObjectAuthorization.NOT_ALLOWED_MESSAGE));

      RuleDefinition forbidden = vetoIfNotInGroupRule("test188:mine", "test188:private");
      String forbiddenError = forbidden.validate();
      assertTrue("forbidden reference: " + forbiddenError,
          forbiddenError != null && forbiddenError.startsWith(RuleReferencedObjectAuthorization.NOT_ALLOWED_MESSAGE));
    } finally {
      GrouperSession.stopQuietly(subj0Session);
    }

    // positive control: with READ on the private group the same rule is fine
    GrouperSession rootSession = GrouperSession.startRootSession();
    try {
      findAsRoot("test188:private").grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.READ, false);
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }
    subj0Session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      Group mine = GroupFinder.findByName(subj0Session, "test188:mine", true);
      AttributeAssign rule = RuleApi.vetoMembershipIfNotInGroup(SubjectTestHelper.SUBJ0, mine,
          findAsRoot("test188:private"), "rule.entity.must.be.in.private", "Must be in private");
      assertNotNull(rule);
    } finally {
      GrouperSession.stopQuietly(subj0Session);
    }
  }

  /**
   * The membership copy: when a member is added to the private group (check owner), add it to my
   * group.  Refused for an act-as subject without READ on the check owner; a rule acting as
   * GrouperSystem (wheel / rules UI) is not checked.
   */
  public void testCheckOwnerMustBeReadable() {
    setUpGroups();

    GrouperSession subj0Session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      RuleDefinition copy = copyMembersRule("test188:mine", "test188:private", SubjectTestHelper.SUBJ0.getSourceId(),
          SubjectTestHelper.SUBJ0.getId());
      String error = copy.validate();
      assertTrue("check owner the act-as subject cannot read: " + error,
          error != null && error.startsWith(RuleReferencedObjectAuthorization.NOT_ALLOWED_MESSAGE));

      // the rule's own owner as check owner is not a cross-object reference
      RuleDefinition own = copyMembersRule("test188:mine", "test188:mine", SubjectTestHelper.SUBJ0.getSourceId(),
          SubjectTestHelper.SUBJ0.getId());
      String ownError = own.validate();
      assertTrue("own owner is not checked: " + ownError,
          ownError == null || !ownError.startsWith(RuleReferencedObjectAuthorization.NOT_ALLOWED_MESSAGE));
    } finally {
      GrouperSession.stopQuietly(subj0Session);
    }

    GrouperSession rootSession = GrouperSession.startRootSession();
    try {
      RuleDefinition asRoot = copyMembersRule("test188:mine", "test188:private", SubjectFinder.findRootSubject().getSourceId(),
          SubjectFinder.findRootSubject().getId());
      String error = RuleReferencedObjectAuthorization.validate(asRoot);
      assertNull("act as GrouperSystem is not checked", error);
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }
  }

  /**
   * @param name
   * @return the group, found as root
   */
  private static Group findAsRoot(String name) {
    GrouperSession rootSession = GrouperSession.staticGrouperSession().internal_getRootSession();
    return GroupFinder.findByName(rootSession, name, true);
  }

  /**
   * in-memory rule on the owner group: veto a membership add unless the subject is in the other group,
   * acting as SUBJ0
   * @param ownerName
   * @param mustBeInName
   * @return the rule
   */
  private static RuleDefinition vetoIfNotInGroupRule(String ownerName, String mustBeInName) {
    RuleDefinition ruleDefinition = baseRule(ownerName, SubjectTestHelper.SUBJ0.getSourceId(), SubjectTestHelper.SUBJ0.getId());
    ruleDefinition.getCheck().setCheckType(RuleCheckType.membershipAdd.name());
    ruleDefinition.getIfCondition().setIfConditionEnum(RuleIfConditionEnum.groupHasNoEnabledMembership.name());
    ruleDefinition.getIfCondition().setIfOwnerName(mustBeInName);
    ruleDefinition.getThen().setThenEnum(RuleThenEnum.veto.name());
    ruleDefinition.getThen().setThenEnumArg0("rule.entity.must.be.in.private");
    ruleDefinition.getThen().setThenEnumArg1("Must be in private");
    return ruleDefinition;
  }

  /**
   * in-memory rule on the owner group: when a member is added to the watched group, add it to the owner
   * @param ownerName
   * @param watchedName
   * @param actAsSourceId
   * @param actAsSubjectId
   * @return the rule
   */
  private static RuleDefinition copyMembersRule(String ownerName, String watchedName, String actAsSourceId, String actAsSubjectId) {
    RuleDefinition ruleDefinition = baseRule(ownerName, actAsSourceId, actAsSubjectId);
    ruleDefinition.getCheck().setCheckType(RuleCheckType.membershipAdd.name());
    ruleDefinition.getCheck().setCheckOwnerName(watchedName);
    ruleDefinition.getThen().setThenEnum(RuleThenEnum.addMemberToOwnerGroup.name());
    return ruleDefinition;
  }

  /**
   * in-memory rule shell on a group
   * @param ownerName
   * @param actAsSourceId
   * @param actAsSubjectId
   * @return the rule
   */
  private static RuleDefinition baseRule(String ownerName, String actAsSourceId, String actAsSubjectId) {
    RuleDefinition ruleDefinition = new RuleDefinition();
    AttributeAssign attributeAssign = new AttributeAssign();
    attributeAssign.setOwnerGroupId(findAsRoot(ownerName).getId());
    ruleDefinition.setAttributeAssignType(attributeAssign);

    RuleSubjectActAs actAs = new RuleSubjectActAs();
    actAs.setSourceId(actAsSourceId);
    actAs.setSubjectId(actAsSubjectId);
    ruleDefinition.setActAs(actAs);

    ruleDefinition.setCheck(new RuleCheck());
    ruleDefinition.setIfCondition(new RuleIfCondition());
    ruleDefinition.setThen(new RuleThen());
    return ruleDefinition;
  }

}
