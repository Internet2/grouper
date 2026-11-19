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
import edu.internet2.middleware.grouper.Stem.Scope;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.AttributeDefNameSave;
import edu.internet2.middleware.grouper.attr.AttributeDefSave;
import edu.internet2.middleware.grouper.attr.AttributeDefType;
import edu.internet2.middleware.grouper.attr.AttributeDefValueType;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.finder.AttributeAssignFinder;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.misc.GrouperObject;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
import edu.internet2.middleware.grouper.privs.Privilege;
import edu.internet2.middleware.subject.Subject;
import junit.textui.TestRunner;

/**
 * SECURITY: a rule edit must be of a rule on the group or folder the request is about.
 *
 * The rules UI checks the user's privilege on the group or folder in the request and then calls
 * RuleService.saveOrUpdateRuleAttributes in a root session with the submitted rule id.  Before the
 * fix, a rule id from another object was overwritten as root, so a user who administers group A
 * could turn group V's rule into "added to A -> added to V" (runs as GrouperSystem).
 *
 * Each save here runs in a root session with the real user on the RuleConfig, as the UI does.
 * Before the fix this class does not compile (RuleAssignOwnerCheck does not exist), and the
 * foreign-id saves succeed and overwrite the victim rule.
 */
public class RuleEditForeignRuleIdTest extends GrouperTest {

  /** the predefined pattern used by the attack and by the legitimate edit */
  private static final String PATTERN = "AddMemberToGroupIfAddedToAnotherGroup";

  /**
   *
   */
  public RuleEditForeignRuleIdTest() {
    super();
  }

  /**
   * @param name
   */
  public RuleEditForeignRuleIdTest(String name) {
    super(name);
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new RuleEditForeignRuleIdTest("testForeignGroupRuleIdRefused"));
  }

  // ---------------------------------------------------------------- helpers

  /**
   * Save the AddMemberToGroupIfAddedToAnotherGroup pattern on an object, as the UI does.
   * @param subject the logged in user
   * @param owner the group or folder in the request
   * @param checkGroup the group whose membership adds trigger the rule
   * @param attributeAssignId the rule id from the request, null for an add
   * @return the result map from the save
   */
  @SuppressWarnings("unchecked")
  private static Map<String, List<String>> savePattern(final Subject subject, final GrouperObject owner,
      final Group checkGroup, final String attributeAssignId) {
    return (Map<String, List<String>>)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        RuleConfig ruleConfig = new RuleConfig(subject, owner);
        ruleConfig.setPattern(PATTERN);
        // the property key was renamed after 5.8.x, set both so the same test works everywhere
        ruleConfig.getPatternPropertiesValues().put(PATTERN + ".groupName", checkGroup.getName());
        ruleConfig.getPatternPropertiesValues().put(PATTERN + ".targetGroupName", checkGroup.getName());
        return RulePattern.valueOf(PATTERN).save(ruleConfig, attributeAssignId);
      }
    });
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
   * Whether the rule's check owner is this group (stored by name, or by uuid on newer releases).
   * @param ruleDefinition the rule
   * @param group the group
   * @return true if the check owner is the group
   */
  private static boolean checkOwnerIs(RuleDefinition ruleDefinition, Group group) {
    RuleCheck ruleCheck = ruleDefinition.getCheck();
    return ruleCheck != null && (StringUtils.equals(group.getName(), ruleCheck.getCheckOwnerName())
        || StringUtils.equals(group.getId(), ruleCheck.getCheckOwnerId()));
  }

  /**
   * Root makes groups and rules for the scenario.  SUBJ0 is ADMIN of mine and can VIEW source and
   * source2, but has no privilege on victim.
   */
  private Group mine;
  /** victim group, has a rule written by root */
  private Group victim;
  /** the check group of the victim rule */
  private Group source;
  /** another group SUBJ0 can view, for the legitimate edit */
  private Group source2;
  /** victim's rule id */
  private String victimRuleId;

  /**
   * set up the scenario
   * @param rootSession root
   */
  private void setupGroups(GrouperSession rootSession) {
    this.mine = new GroupSave(rootSession).assignName("test:ruleForeignMine").assignCreateParentStemsIfNotExist(true).save();
    this.victim = new GroupSave(rootSession).assignName("test2:ruleForeignVictim").assignCreateParentStemsIfNotExist(true).save();
    this.source = new GroupSave(rootSession).assignName("test2:ruleForeignSource").assignCreateParentStemsIfNotExist(true).save();
    this.source2 = new GroupSave(rootSession).assignName("test2:ruleForeignSource2").assignCreateParentStemsIfNotExist(true).save();

    this.mine.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.ADMIN, false);
    this.source.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.READ, false);
    this.source.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.VIEW, false);
    this.source2.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.READ, false);
    this.source2.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.VIEW, false);

    // root writes the victim rule: added to source -> added to victim
    Map<String, List<String>> result = savePattern(SubjectFinder.findRootSubject(), this.victim, this.source, null);
    assertFalse("root should be able to add the victim rule: " + result, result.containsKey("ERROR"));
    RuleDefinition victimRule = ownedRule(this.victim);
    assertNotNull("victim rule should exist", victimRule);
    this.victimRuleId = victimRule.getAttributeAssignType().getId();
    assertTrue(checkOwnerIs(victimRule, this.source));
  }

  /**
   * the victim rule is still owned by victim and still checks source
   */
  private void assertVictimRuleUnchanged() {
    RuleDefinition victimRule = ownedRule(this.victim);
    assertNotNull("victim rule should still exist", victimRule);
    assertEquals(this.victimRuleId, victimRule.getAttributeAssignType().getId());
    assertEquals(this.victim.getId(), victimRule.getAttributeAssignType().getOwnerGroupId());
    assertTrue("victim rule must still check source", checkOwnerIs(victimRule, this.source));
    assertFalse("victim rule must not have been repointed at mine", checkOwnerIs(victimRule, this.mine));
  }

  // ---------------------------------------------------------------- tests

  /**
   * The attack: SUBJ0 (ADMIN of mine only) saves a rule on mine with victim's rule id, to make
   * "added to mine -> added to victim".  Must be refused and victim's rule left alone.
   */
  public void testForeignGroupRuleIdRefused() {
    GrouperSession rootSession = GrouperSession.startRootSession();
    setupGroups(rootSession);

    // the helper binds the id to its owner
    assertNotNull(RuleAssignOwnerCheck.ruleAssignOwnedBy(this.victim, this.victimRuleId));
    assertNull(RuleAssignOwnerCheck.ruleAssignOwnedBy(this.mine, this.victimRuleId));

    Map<String, List<String>> result = savePattern(SubjectTestHelper.SUBJ0, this.mine, this.mine, this.victimRuleId);
    assertTrue("a rule id from another group must be refused: " + result, result.containsKey("ERROR"));
    assertTrue(result.get("ERROR").contains(RuleAssignOwnerCheck.invalidRuleIdError()));

    assertVictimRuleUnchanged();
    assertNull("nothing should have been added to mine either", ownedRule(this.mine));

    GrouperSession.stopQuietly(rootSession);
  }

  /**
   * Folder variants: a folder rule id given for a group save, and a group rule id given for a
   * folder save.  Both refused.
   */
  public void testForeignFolderRuleIdRefused() {
    GrouperSession rootSession = GrouperSession.startRootSession();
    setupGroups(rootSession);

    // a folder rule SUBJ0 has nothing to do with
    Stem otherFolder = new StemSave(rootSession).assignName("test3:ruleForeignFolder").assignCreateParentStemsIfNotExist(true).save();
    AttributeAssign folderRuleAssign = RuleApi.inheritGroupPrivileges(otherFolder, Scope.SUB, SubjectTestHelper.SUBJ9, Privilege.getInstances("read"));
    String folderRuleId = folderRuleAssign.getId();
    assertNotNull(RuleAssignOwnerCheck.ruleAssignOwnedBy(otherFolder, folderRuleId));
    assertNull(RuleAssignOwnerCheck.ruleAssignOwnedBy(this.mine, folderRuleId));
    RuleDefinition folderRule = ownedRule(otherFolder);
    assertNotNull("folder rule should exist", folderRule);
    String folderRuleCheckType = folderRule.getCheck().getCheckType();
    String folderRuleThen = folderRule.getThen().getThenEnum();

    // folder rule id, group save
    Map<String, List<String>> result = savePattern(SubjectTestHelper.SUBJ0, this.mine, this.source, folderRuleId);
    assertTrue("a folder rule id must be refused for a group save: " + result, result.containsKey("ERROR"));
    folderRule = ownedRule(otherFolder);
    assertNotNull("folder rule should still exist", folderRule);
    assertEquals(otherFolder.getUuid(), folderRule.getAttributeAssignType().getOwnerStemId());
    assertEquals("folder rule check type unchanged", folderRuleCheckType, folderRule.getCheck().getCheckType());
    assertEquals("folder rule then unchanged", folderRuleThen, folderRule.getThen().getThenEnum());
    assertFalse("folder rule must not have been repointed", checkOwnerIs(folderRule, this.source));

    // group rule id, folder save, on a folder SUBJ0 administers
    Stem myFolder = new StemSave(rootSession).assignName("test:ruleForeignMyFolder").assignCreateParentStemsIfNotExist(true).save();
    myFolder.grantPriv(SubjectTestHelper.SUBJ0, NamingPrivilege.STEM_ADMIN, false);
    assertNull(RuleAssignOwnerCheck.ruleAssignOwnedBy(myFolder, this.victimRuleId));
    result = savePattern(SubjectTestHelper.SUBJ0, myFolder, this.mine, this.victimRuleId);
    assertTrue("a group rule id must be refused for a folder save: " + result, result.containsKey("ERROR"));
    assertVictimRuleUnchanged();

    GrouperSession.stopQuietly(rootSession);
  }

  /**
   * An attribute assign on the user's own group that is not a rule must not be writable through a
   * rule save.
   */
  public void testNonRuleAssignIdRefused() {
    GrouperSession rootSession = GrouperSession.startRootSession();
    setupGroups(rootSession);

    AttributeDef attributeDef = new AttributeDefSave(rootSession).assignName("test:ruleForeignAttrDef")
        .assignCreateParentStemsIfNotExist(true).assignToGroup(true)
        .assignAttributeDefType(AttributeDefType.attr).assignMultiAssignable(false)
        .assignValueType(AttributeDefValueType.marker).save();
    AttributeDefName attributeDefName = new AttributeDefNameSave(rootSession, attributeDef)
        .assignName("test:ruleForeignAttrDefName").assignCreateParentStemsIfNotExist(true).save();
    AttributeAssign nonRuleAssign = this.mine.getAttributeDelegate().assignAttribute(attributeDefName).getAttributeAssign();

    assertNull("a non-rule assign is not a rule", RuleAssignOwnerCheck.ruleAssignOwnedBy(this.mine, nonRuleAssign.getId()));

    Map<String, List<String>> result = savePattern(SubjectTestHelper.SUBJ0, this.mine, this.source, nonRuleAssign.getId());
    assertTrue("a non-rule attribute assign id must be refused: " + result, result.containsKey("ERROR"));
    assertNull("no rule should exist on mine", ownedRule(this.mine));

    AttributeAssign reloaded = AttributeAssignFinder.findById(nonRuleAssign.getId(), true);
    assertEquals(attributeDefName.getId(), reloaded.getAttributeDefNameId());

    // a made up id
    assertNull(RuleAssignOwnerCheck.ruleAssignOwnedBy(this.mine, "abc123notAnId"));
    result = savePattern(SubjectTestHelper.SUBJ0, this.mine, this.source, "abc123notAnId");
    assertTrue("an unknown id must be refused: " + result, result.containsKey("ERROR"));

    GrouperSession.stopQuietly(rootSession);
  }

  /**
   * Editing a rule on the user's own group with its own id still works.
   */
  public void testOwnRuleEditAllowed() {
    GrouperSession rootSession = GrouperSession.startRootSession();
    setupGroups(rootSession);

    // root puts a pattern rule on mine: added to source -> added to mine
    Map<String, List<String>> result = savePattern(SubjectFinder.findRootSubject(), this.mine, this.source, null);
    assertFalse("root add on mine: " + result, result.containsKey("ERROR"));
    RuleDefinition mineRule = ownedRule(this.mine);
    assertNotNull(mineRule);
    String mineRuleId = mineRule.getAttributeAssignType().getId();
    assertNotNull(RuleAssignOwnerCheck.ruleAssignOwnedBy(this.mine, mineRuleId));

    // SUBJ0 edits it to check source2 instead
    result = savePattern(SubjectTestHelper.SUBJ0, this.mine, this.source2, mineRuleId);
    assertFalse("editing your own group's rule should work: " + result, result.containsKey("ERROR"));

    mineRule = ownedRule(this.mine);
    assertNotNull(mineRule);
    assertEquals("same rule assign is edited in place", mineRuleId, mineRule.getAttributeAssignType().getId());
    assertTrue("rule now checks source2", checkOwnerIs(mineRule, this.source2));

    assertVictimRuleUnchanged();

    GrouperSession.stopQuietly(rootSession);
  }

  /**
   * Wheel/root is refused too: a foreign id is never a legitimate edit.
   */
  public void testWheelForeignRuleIdRefused() {
    GrouperSession rootSession = GrouperSession.startRootSession();
    setupGroups(rootSession);

    Map<String, List<String>> result = savePattern(SubjectFinder.findRootSubject(), this.mine, this.mine, this.victimRuleId);
    assertTrue("root must also be refused a foreign rule id: " + result, result.containsKey("ERROR"));
    assertVictimRuleUnchanged();

    GrouperSession.stopQuietly(rootSession);
  }

}
