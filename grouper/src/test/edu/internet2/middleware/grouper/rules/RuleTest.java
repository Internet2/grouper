/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.rules;

import java.util.Date;
import java.util.Map;
import java.util.Set;

import junit.textui.TestRunner;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.app.loader.GrouperLoader;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderType;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.value.AttributeAssignValueContainer;
import edu.internet2.middleware.grouper.cfg.ApiConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;


/**
 *
 */
public class RuleTest extends GrouperTest {

  /**
   * 
   */
  public RuleTest() {
    super();
    
  }

  /**
   * @param name
   */
  public RuleTest(String name) {
    super(name);
    
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new RuleTest("testRuleLonghandGshFixer"));
  }

  /**
   * 
   */
  public void testRuleLonghand() {
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Group groupA = new GroupSave(grouperSession).assignName("stem:a").assignCreateParentStemsIfNotExist(true).save();
    Group groupB = new GroupSave(grouperSession).assignName("stem:b").assignCreateParentStemsIfNotExist(true).save();
    
    //add a rule on stem:a saying if you are out of stem:b, then remove from stem:a
    AttributeAssign attributeAssign = groupA
      .getAttributeDelegate().assignAttribute(RuleUtils.ruleAttributeDefName()).getAttributeAssign();
    
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleActAsSubjectSourceIdName(), "g:isa");
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleActAsSubjectIdName(), "GrouperSystem");
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleCheckOwnerNameName(), "stem:b");
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleCheckTypeName(), 
        RuleCheckType.membershipRemove.name());
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleIfConditionEnumName(), 
        RuleIfConditionEnum.thisGroupHasImmediateEnabledMembership.name());
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleThenElName(), 
        "${ruleElUtils.removeMemberFromGroupId(ownerGroupId, memberId)}");
    
    groupB.addMember(SubjectTestHelper.SUBJ0);

    //count rule firings
    long initialFirings = RuleEngine.ruleFirings;
    
    //doesnt do anything
    groupB.deleteMember(SubjectTestHelper.SUBJ0);

    assertEquals(initialFirings, RuleEngine.ruleFirings);

    groupB.addMember(SubjectTestHelper.SUBJ0);
    groupA.addMember(SubjectTestHelper.SUBJ0);
    
    //count rule firings
    
    groupB.deleteMember(SubjectTestHelper.SUBJ0);
    
    //should come out of groupA
    assertFalse(groupA.hasMember(SubjectTestHelper.SUBJ0));

    assertEquals(initialFirings+1, RuleEngine.ruleFirings);

    // GrouperSession.startRootSession();
    // addMember("stem:a", "test.subject.0");
    // addMember("stem:b", "test.subject.0");
    // delMember("stem:b", "test.subject.0");
    // hasMember("stem:a", "test.subject.0");
    
  }

  /**
   * 
   */
  public void testRuleLonghandVeto() {
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Group groupA = new GroupSave(grouperSession).assignName("stem:a").assignCreateParentStemsIfNotExist(true).save();
    Group groupB = new GroupSave(grouperSession).assignName("stem:b").assignCreateParentStemsIfNotExist(true).save();
    
    //add a rule on stem:a saying if you are out of stem:b, then remove from stem:a
    AttributeAssign attributeAssign = groupA
      .getAttributeDelegate().assignAttribute(RuleUtils.ruleAttributeDefName()).getAttributeAssign();
    
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleActAsSubjectSourceIdName(), "g:isa");
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleActAsSubjectIdName(), "GrouperSystem");
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleCheckOwnerNameName(), "stem:a");
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleCheckTypeName(), 
        RuleCheckType.membershipAdd.name());
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleIfConditionEnumName(), 
        RuleIfConditionEnum.groupHasNoImmediateEnabledMembership.name());
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleIfOwnerNameName(),
        "stem:b");
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleThenElName(), 
        "${ruleElUtils.veto('rule.entity.must.be.a.member.of.stem.b', 'Entity cannot be a member of stem:a if not a member of stem:b')}");

    //count rule firings
    long initialFirings = RuleEngine.ruleFirings;
    
    try {
      groupA.addMember(SubjectTestHelper.SUBJ0);
      fail("Should be vetoed");
    } catch (RuleVeto rve) {
      //this is good
      String stack = ExceptionUtils.getFullStackTrace(rve);
      assertTrue(stack, stack.contains("Entity cannot be a member of stem:a if not a member of stem:b"));
    }

    assertEquals(initialFirings+1, RuleEngine.ruleFirings);

    groupB.addMember(SubjectTestHelper.SUBJ0);
    groupA.addMember(SubjectTestHelper.SUBJ0);
    
    assertEquals("Didnt fire since is a member", initialFirings+1, RuleEngine.ruleFirings);

    // GrouperSession.startRootSession();
    // addMember("stem:a", "test.subject.0");
    // addMember("stem:b", "test.subject.0");
    // delMember("stem:b", "test.subject.0");
    // hasMember("stem:a", "test.subject.0");
    
  }
  

  
  /**
   * 
   */
  public void testRuleLonghandElCustomClass() {

    
    ApiConfig.testConfig.put("rules.customElClasses", MyRuleUtils.class.getName());
    
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Group groupA = new GroupSave(grouperSession).assignName("stem:a").assignCreateParentStemsIfNotExist(true).save();
    Group groupB = new GroupSave(grouperSession).assignName("stem:b").assignCreateParentStemsIfNotExist(true).save();
    
    //add a rule on stem:a saying if you are out of stem:b, then remove from stem:a
    AttributeAssign attributeAssign = groupA
      .getAttributeDelegate().assignAttribute(RuleUtils.ruleAttributeDefName()).getAttributeAssign();
    
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleActAsSubjectSourceIdName(), "g:isa");
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleActAsSubjectIdName(), "GrouperSystem");
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleCheckOwnerNameName(), "stem:b");
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleCheckTypeName(), 
        RuleCheckType.membershipRemove.name());
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleIfConditionEnumName(), 
        RuleIfConditionEnum.thisGroupHasImmediateEnabledMembership.name());
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleThenElName(), 
        "${myRuleUtils.removeMemberFromGroupId(ownerGroupId, memberId)}");
    
    groupB.addMember(SubjectTestHelper.SUBJ0);

    //count rule firings
    long initialFirings = RuleEngine.ruleFirings;
    
    //doesnt do anything
    groupB.deleteMember(SubjectTestHelper.SUBJ0);

    assertEquals(initialFirings, RuleEngine.ruleFirings);

    groupB.addMember(SubjectTestHelper.SUBJ0);
    groupA.addMember(SubjectTestHelper.SUBJ0);
    
    //count rule firings
    
    groupB.deleteMember(SubjectTestHelper.SUBJ0);
    
    //should come out of groupA
    assertFalse(groupA.hasMember(SubjectTestHelper.SUBJ0));

    assertEquals(initialFirings+1, RuleEngine.ruleFirings);

    // GrouperSession.startRootSession();
    // addMember("stem:a", "test.subject.0");
    // addMember("stem:b", "test.subject.0");
    // delMember("stem:b", "test.subject.0");
    // hasMember("stem:a", "test.subject.0");
    
  }
  
  /**
   * 
   */
  public void testRuleLonghandIfElMoreApi() {
    
    ApiConfig.testConfig.put("rules.accessToApiInEl.group", "etc:rulesAccessToApi");
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    Group rulesAccessToApiGroup = new GroupSave(grouperSession).assignName("etc:rulesAccessToApi").save();
    
    Group groupA = new GroupSave(grouperSession).assignName("stem:a").assignCreateParentStemsIfNotExist(true).save();
    Group groupB = new GroupSave(grouperSession).assignName("stem:b").assignCreateParentStemsIfNotExist(true).save();
    
    //add a rule on stem:a saying if you are out of stem:b, then remove from stem:a
    AttributeAssign attributeAssign = groupA
      .getAttributeDelegate().assignAttribute(RuleUtils.ruleAttributeDefName()).getAttributeAssign();
    
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleActAsSubjectSourceIdName(), "g:isa");
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleActAsSubjectIdName(), "GrouperSystem");

    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleCheckOwnerNameName(), "stem:b");
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleCheckTypeName(), 
        RuleCheckType.membershipRemove.name());

    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleIfConditionElName(), 
        "${ruleElUtils.hasMembershipByGroupId(attributeAssignType.getOwnerGroupId(), memberId, null, 'true')}");

    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleThenElName(), 
        "${ruleElUtils.removeMemberFromGroupId(ownerGroupId, memberId)}");
    
    groupB.addMember(SubjectTestHelper.SUBJ0);
    groupA.addMember(SubjectTestHelper.SUBJ0);
    
    //count rule firings
    try {
      groupB.deleteMember(SubjectTestHelper.SUBJ0);
      fail("should not be allowed to call object (or doesnt exist)");
    } catch (Exception e) {
      //good
    }

    //tx's were rolled back
    assertTrue(groupA.hasMember(SubjectTestHelper.SUBJ0));
    assertTrue(groupB.hasMember(SubjectTestHelper.SUBJ0));

    //now lets put the act as in the full api group
    rulesAccessToApiGroup.addMember(SubjectFinder.findRootSubject(), false);
    RuleEngine.clearSubjectHasAccessToElApi();

    groupB.deleteMember(SubjectTestHelper.SUBJ0);

    assertFalse(groupA.hasMember(SubjectTestHelper.SUBJ0));

    // GrouperSession.startRootSession();
    // addMember("stem:a", "test.subject.0");
    // addMember("stem:b", "test.subject.0");
    // delMember("stem:b", "test.subject.0");
    // hasMember("stem:a", "test.subject.0");
    
  }
  

  
  /**
   * 
   */
  public void testRuleLonghandIfEl() {
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Group groupA = new GroupSave(grouperSession).assignName("stem:a").assignCreateParentStemsIfNotExist(true).save();
    Group groupB = new GroupSave(grouperSession).assignName("stem:b").assignCreateParentStemsIfNotExist(true).save();
    
    //add a rule on stem:a saying if you are out of stem:b, then remove from stem:a
    AttributeAssign attributeAssign = groupA
      .getAttributeDelegate().assignAttribute(RuleUtils.ruleAttributeDefName()).getAttributeAssign();
    
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleActAsSubjectSourceIdName(), "g:isa");
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleActAsSubjectIdName(), "GrouperSystem");

    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleCheckOwnerNameName(), "stem:b");
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleCheckTypeName(), 
        RuleCheckType.membershipRemove.name());

    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleIfConditionElName(), 
        "${ruleElUtils.hasMembershipByGroupId(ownerGroupId, memberId, null, 'true')}");

    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleThenElName(), 
        "${ruleElUtils.removeMemberFromGroupId(ownerGroupId, memberId)}");
    
    groupB.addMember(SubjectTestHelper.SUBJ0);

    //count rule firings
    long initialFirings = RuleEngine.ruleFirings;
    
    //doesnt do anything
    groupB.deleteMember(SubjectTestHelper.SUBJ0);

    assertEquals(initialFirings, RuleEngine.ruleFirings);

    groupB.addMember(SubjectTestHelper.SUBJ0);
    groupA.addMember(SubjectTestHelper.SUBJ0);
    
    //count rule firings
    
    groupB.deleteMember(SubjectTestHelper.SUBJ0);
    
    //should come out of groupA
    assertFalse(groupA.hasMember(SubjectTestHelper.SUBJ0));

    assertEquals(initialFirings+1, RuleEngine.ruleFirings);

    // GrouperSession.startRootSession();
    // addMember("stem:a", "test.subject.0");
    // addMember("stem:b", "test.subject.0");
    // delMember("stem:b", "test.subject.0");
    // hasMember("stem:a", "test.subject.0");
    
  }
  
  /**
   * 
   */
  public void testRuleLonghandStemScopeOne() {
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Group groupA = new GroupSave(grouperSession).assignName("stem1:a").assignCreateParentStemsIfNotExist(true).save();
    Group groupB = new GroupSave(grouperSession).assignName("stem2:b").assignCreateParentStemsIfNotExist(true).save();
    Group groupC = new GroupSave(grouperSession).assignName("stem2:sub:c").assignCreateParentStemsIfNotExist(true).save();
    
    //add a rule on stem:a saying if you are out of stem:b, then remove from stem:a
    AttributeAssign attributeAssign = groupA
      .getAttributeDelegate().assignAttribute(RuleUtils.ruleAttributeDefName()).getAttributeAssign();
    
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleActAsSubjectSourceIdName(), "g:isa");
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleActAsSubjectIdName(), "GrouperSystem");
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleCheckOwnerNameName(), "stem2");
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleCheckTypeName(), 
        RuleCheckType.membershipRemoveInFolder.name());
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleCheckStemScopeName(),
        Stem.Scope.ONE.name());
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleIfConditionEnumName(), 
        RuleIfConditionEnum.thisGroupHasImmediateEnabledMembership.name());
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleThenElName(), 
        "${ruleElUtils.removeMemberFromGroupId(ownerGroupId, memberId)}");
    
    long initialFirings = RuleEngine.ruleFirings;
    
    
    groupB.addMember(SubjectTestHelper.SUBJ0);

    //count rule firings
    
    //doesnt do anything
    groupB.deleteMember(SubjectTestHelper.SUBJ0);

    assertEquals(initialFirings, RuleEngine.ruleFirings);
    
    groupB.addMember(SubjectTestHelper.SUBJ0);
    groupA.addMember(SubjectTestHelper.SUBJ0);
    
    //count rule firings
    
    groupB.deleteMember(SubjectTestHelper.SUBJ0);
    
    //should come out of groupA
    assertFalse(groupA.hasMember(SubjectTestHelper.SUBJ0));

    assertEquals(initialFirings+1, RuleEngine.ruleFirings);

    
    groupC.addMember(SubjectTestHelper.SUBJ0);
    
    //doesnt do anything
    groupC.deleteMember(SubjectTestHelper.SUBJ0);

    //count rule firings
    assertEquals(initialFirings+1, RuleEngine.ruleFirings);

    groupC.addMember(SubjectTestHelper.SUBJ0);
    groupA.addMember(SubjectTestHelper.SUBJ0);

    groupC.deleteMember(SubjectTestHelper.SUBJ0);

    //shouldnt fire from ancestor
    assertTrue(groupA.hasMember(SubjectTestHelper.SUBJ0));

    assertEquals(initialFirings+1, RuleEngine.ruleFirings);

    // GrouperSession.startRootSession();
    // addMember("stem:a", "test.subject.0");
    // addMember("stem:b", "test.subject.0");
    // delMember("stem:b", "test.subject.0");
    // hasMember("stem:a", "test.subject.0");
    
  }

  /**
   * 
   */
  public void testRuleLonghandStemScopeSub() {
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Group groupA = new GroupSave(grouperSession).assignName("stem1:a").assignCreateParentStemsIfNotExist(true).save();
    Group groupB = new GroupSave(grouperSession).assignName("stem2:b").assignCreateParentStemsIfNotExist(true).save();
    Group groupC = new GroupSave(grouperSession).assignName("stem2:sub:c").assignCreateParentStemsIfNotExist(true).save();
    
    //add a rule on stem:a saying if you are out of stem:b, then remove from stem:a
    AttributeAssign attributeAssign = groupA
      .getAttributeDelegate().assignAttribute(RuleUtils.ruleAttributeDefName()).getAttributeAssign();
    
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleActAsSubjectSourceIdName(), "g:isa");
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleActAsSubjectIdName(), "GrouperSystem");
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleCheckOwnerNameName(), "stem2");
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleCheckTypeName(), 
        RuleCheckType.membershipRemoveInFolder.name());
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleCheckStemScopeName(),
        Stem.Scope.SUB.name());
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleIfConditionEnumName(), 
        RuleIfConditionEnum.thisGroupHasImmediateEnabledMembership.name());
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleThenElName(), 
        "${ruleElUtils.removeMemberFromGroupId(ownerGroupId, memberId)}");
    
    long initialFirings = RuleEngine.ruleFirings;
    
    
    groupB.addMember(SubjectTestHelper.SUBJ0);

    //count rule firings
    
    //doesnt do anything
    groupB.deleteMember(SubjectTestHelper.SUBJ0);

    assertEquals(initialFirings, RuleEngine.ruleFirings);
    
    groupB.addMember(SubjectTestHelper.SUBJ0);
    groupA.addMember(SubjectTestHelper.SUBJ0);
    
    //count rule firings
    
    groupB.deleteMember(SubjectTestHelper.SUBJ0);
    
    //should come out of groupA
    assertFalse(groupA.hasMember(SubjectTestHelper.SUBJ0));

    assertEquals(initialFirings+1, RuleEngine.ruleFirings);

    
    groupC.addMember(SubjectTestHelper.SUBJ0);
    
    //doesnt do anything
    groupC.deleteMember(SubjectTestHelper.SUBJ0);

    //count rule firings
    assertEquals(initialFirings+1, RuleEngine.ruleFirings);

    groupC.addMember(SubjectTestHelper.SUBJ0);
    groupA.addMember(SubjectTestHelper.SUBJ0);

    groupC.deleteMember(SubjectTestHelper.SUBJ0);

    //should fire from ancestor
    assertFalse(groupA.hasMember(SubjectTestHelper.SUBJ0));

    assertEquals(initialFirings+2, RuleEngine.ruleFirings);

    // GrouperSession.startRootSession();
    // addMember("stem:a", "test.subject.0");
    // addMember("stem:b", "test.subject.0");
    // delMember("stem:b", "test.subject.0");
    // hasMember("stem:a", "test.subject.0");
    
  }

  /**
   * 
   */
  public void testRuleLonghandStemScopeSubCreateGroup() {
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Stem stem2 = new StemSave(grouperSession).assignName("stem2").assignCreateParentStemsIfNotExist(true).save();

    Group groupA = new GroupSave(grouperSession).assignName("stem1:a").assignCreateParentStemsIfNotExist(true).save();

    groupA.addMember(SubjectTestHelper.SUBJ0);
    
    
    //add a rule on stem2 saying if you create a group underneath, then assign a reader group
    AttributeAssign attributeAssign = stem2
      .getAttributeDelegate().assignAttribute(RuleUtils.ruleAttributeDefName()).getAttributeAssign();
    
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleActAsSubjectSourceIdName(), "g:isa");
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleActAsSubjectIdName(), "GrouperSystem");
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleCheckOwnerNameName(), "stem2");
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleCheckTypeName(), 
        RuleCheckType.groupCreate.name());
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleCheckStemScopeName(),
        Stem.Scope.SUB.name());
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleThenElName(), 
        "${ruleElUtils.assignGroupPrivilege(groupId, 'g:gsa', null, 'stem1:a', 'read,update')}");
    
    long initialFirings = RuleEngine.ruleFirings;
    
    
    Group groupB = new GroupSave(grouperSession).assignName("stem2:b").assignCreateParentStemsIfNotExist(true).save();

    //count rule firings
    assertEquals(initialFirings+1, RuleEngine.ruleFirings);

    //make sure allowed
    assertTrue(groupB.hasUpdate(SubjectTestHelper.SUBJ0));
    assertTrue(groupB.hasRead(SubjectTestHelper.SUBJ0));
    
    
    Group groupD = new GroupSave(grouperSession).assignName("stem3:b").assignCreateParentStemsIfNotExist(true).save();

    assertEquals(initialFirings+1, RuleEngine.ruleFirings);
    
    assertFalse(groupD.hasUpdate(SubjectTestHelper.SUBJ0));
    assertFalse(groupD.hasRead(SubjectTestHelper.SUBJ0));
    
    
    Group groupC = new GroupSave(grouperSession).assignName("stem2:sub:c").assignCreateParentStemsIfNotExist(true).save();

    assertEquals(initialFirings+2, RuleEngine.ruleFirings);

    assertTrue(groupC.hasRead(SubjectTestHelper.SUBJ0));
    assertTrue(groupC.hasUpdate(SubjectTestHelper.SUBJ0));


    // GrouperSession.startRootSession();
    // addMember("stem:a", "test.subject.0");
    // addMember("stem:b", "test.subject.0");
    // delMember("stem:b", "test.subject.0");
    // hasMember("stem:a", "test.subject.0");
    
  }

  /**
   * @see GrouperTest#setUp()
   */
  @Override
  protected void setUp() {
    super.setUp();
    ApiConfig.testConfig.put("groups.create.grant.all.read", "false");
    ApiConfig.testConfig.put("groups.create.grant.all.view", "false");

  }

  /**
   * 
   */
  public void testRuleLonghandStemScopeSubIfNotInFolder() {
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Group groupA = new GroupSave(grouperSession).assignName("stem1:a").assignCreateParentStemsIfNotExist(true).save();
    Group groupB = new GroupSave(grouperSession).assignName("stem2:b").assignCreateParentStemsIfNotExist(true).save();
    Group groupC = new GroupSave(grouperSession).assignName("stem2:sub:c").assignCreateParentStemsIfNotExist(true).save();
    
    //add a rule on stem:a saying if you are out of stem:b, then remove from stem:a
    AttributeAssign attributeAssign = groupA
      .getAttributeDelegate().assignAttribute(RuleUtils.ruleAttributeDefName()).getAttributeAssign();
    
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleActAsSubjectSourceIdName(), "g:isa");
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleActAsSubjectIdName(), "GrouperSystem");
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleCheckOwnerNameName(), "stem2");
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleCheckTypeName(), 
        RuleCheckType.membershipRemoveInFolder.name());
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleCheckStemScopeName(),
        Stem.Scope.SUB.name());
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleIfConditionEnumName(), 
        RuleIfConditionEnum.thisGroupAndNotFolderHasImmediateEnabledMembership.name());
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleThenElName(), 
        "${ruleElUtils.removeMemberFromGroupId(ownerGroupId, memberId)}");
    
    long initialFirings = RuleEngine.ruleFirings;
    
    
    groupB.addMember(SubjectTestHelper.SUBJ0);
  
    //count rule firings
    
    //doesnt do anything
    groupB.deleteMember(SubjectTestHelper.SUBJ0);
  
    assertEquals(initialFirings, RuleEngine.ruleFirings);
    
    groupB.addMember(SubjectTestHelper.SUBJ0);
    groupA.addMember(SubjectTestHelper.SUBJ0);
    
    //count rule firings
    
    groupB.deleteMember(SubjectTestHelper.SUBJ0);
    
    //should come out of groupA
    assertFalse(groupA.hasMember(SubjectTestHelper.SUBJ0));
  
    assertEquals(initialFirings+1, RuleEngine.ruleFirings);
  
    
    groupC.addMember(SubjectTestHelper.SUBJ0);
    
    //doesnt do anything
    groupC.deleteMember(SubjectTestHelper.SUBJ0);
  
    //count rule firings
    assertEquals(initialFirings+1, RuleEngine.ruleFirings);
  
    groupC.addMember(SubjectTestHelper.SUBJ0);
    groupA.addMember(SubjectTestHelper.SUBJ0);
  
    groupC.deleteMember(SubjectTestHelper.SUBJ0);
  
    //should fire from ancestor
    assertFalse(groupA.hasMember(SubjectTestHelper.SUBJ0));
  
    assertEquals(initialFirings+2, RuleEngine.ruleFirings);
  
    // GrouperSession.startRootSession();
    // addMember("stem:a", "test.subject.0");
    // addMember("stem:b", "test.subject.0");
    // delMember("stem:b", "test.subject.0");
    // hasMember("stem:a", "test.subject.0");
    
  }

  /**
   * 
   */
  public void testRuleLonghandDaemon() {
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Group groupA = new GroupSave(grouperSession).assignName("stem:a").assignCreateParentStemsIfNotExist(true).save();
    Group groupB = new GroupSave(grouperSession).assignName("stem:b").assignCreateParentStemsIfNotExist(true).save();
    
    //check the rules
    Map<AttributeAssign, Set<AttributeAssignValueContainer>> attributeAssignValueContainers 
      = RuleEngine.allRulesAttributeAssignValueContainers(new QueryOptions().secondLevelCache(false));
    
    //rule should be there
    assertEquals(0, attributeAssignValueContainers.size());

    
    //add a rule on stem:a saying if you are out of stem:b, then remove from stem:a
    AttributeAssign attributeAssign = groupA
      .getAttributeDelegate().assignAttribute(RuleUtils.ruleAttributeDefName()).getAttributeAssign();
    
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleActAsSubjectSourceIdName(), "g:isa");
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleActAsSubjectIdName(), "GrouperSystem");
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleCheckOwnerNameName(), "stem:b");
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleCheckTypeName(), 
        RuleCheckType.membershipRemove.name());
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleIfConditionEnumName(), 
        RuleIfConditionEnum.thisGroupHasImmediateEnabledMembership.name());
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleThenElName(), 
        "${ruleElUtils.removeMemberFromGroupId(ownerGroupId, memberId)}");
    
    RuleEngine.ruleEngineCache.clear();
    
    //count rule firings
    long initialFirings = RuleEngine.ruleFirings;
    
    groupB.addMember(SubjectTestHelper.SUBJ0);
    groupA.addMember(SubjectTestHelper.SUBJ0);
    
    //count rule firings
    
    groupB.deleteMember(SubjectTestHelper.SUBJ0);
    
    //should come out of groupA
    assertFalse(groupA.hasMember(SubjectTestHelper.SUBJ0));

    assertEquals(initialFirings+1, RuleEngine.ruleFirings);

    //the rule works
    
    //now run the daemon
    String status = GrouperLoader.runOnceByJobName(grouperSession, GrouperLoaderType.GROUPER_RULES);
    
    assertTrue(status.toLowerCase().contains("success"));
    
    //check the rules
    attributeAssignValueContainers 
      = RuleEngine.allRulesAttributeAssignValueContainers(new QueryOptions().secondLevelCache(false));
    
    //rule should be there
    assertEquals(1, attributeAssignValueContainers.size());
    
    //update with sql to make rule invalid, so that the hook doesnt disable
    HibernateSession.bySqlStatic().executeSql("update grouper_attribute_assign_value " +
    		" set value_string = 'thisGroupHasImmediateEnabledMembershipabcksjdf' where value_string = 'thisGroupHasImmediateEnabledMembership'");

    //check the rules
    attributeAssignValueContainers 
      = RuleEngine.allRulesAttributeAssignValueContainers(new QueryOptions().secondLevelCache(false));
    
    //rule should be there
    assertEquals(1, attributeAssignValueContainers.size());
    
    //now run the daemon
    status = GrouperLoader.runOnceByJobName(grouperSession, GrouperLoaderType.GROUPER_RULES);

    assertTrue(status.toLowerCase().contains("success"));

    attributeAssignValueContainers 
      = RuleEngine.allRulesAttributeAssignValueContainers(new QueryOptions().secondLevelCache(false));

    //rule should be there
    assertEquals(0, attributeAssignValueContainers.size());

    // GrouperSession.startRootSession();
    // addMember("stem:a", "test.subject.0");
    // addMember("stem:b", "test.subject.0");
    // delMember("stem:b", "test.subject.0");
    // hasMember("stem:a", "test.subject.0");
    
  }

  /**
   * 
   */
  public void testRuleLonghandRulesRefresh() {
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Group groupA = new GroupSave(grouperSession).assignName("stem:a").assignCreateParentStemsIfNotExist(true).save();
    Group groupB = new GroupSave(grouperSession).assignName("stem:b").assignCreateParentStemsIfNotExist(true).save();
    Group groupC = new GroupSave(grouperSession).assignName("stem:c").assignCreateParentStemsIfNotExist(true).save();
    
    //add a rule on stem:a saying if you are out of stem:b, then remove from stem:a
    AttributeAssign attributeAssign = groupA
      .getAttributeDelegate().assignAttribute(RuleUtils.ruleAttributeDefName()).getAttributeAssign();
    
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleActAsSubjectSourceIdName(), "g:isa");
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleActAsSubjectIdName(), "GrouperSystem");
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleCheckOwnerNameName(), "stem:b");
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleCheckTypeName(), 
        RuleCheckType.membershipRemove.name());
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleIfConditionEnumName(), 
        RuleIfConditionEnum.thisGroupHasImmediateEnabledMembership.name());
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleThenElName(), 
        "${ruleElUtils.removeMemberFromGroupId(ownerGroupId, memberId)}");
    
    groupB.addMember(SubjectTestHelper.SUBJ0);
    groupA.addMember(SubjectTestHelper.SUBJ0);
    groupC.addMember(SubjectTestHelper.SUBJ0);
    
    groupB.deleteMember(SubjectTestHelper.SUBJ0);
    
    //should come out of groupA
    assertFalse(groupA.hasMember(SubjectTestHelper.SUBJ0));

    //lets change the rule
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleCheckOwnerNameName(), "stem:c");

    groupA.addMember(SubjectTestHelper.SUBJ0);
    groupC.deleteMember(SubjectTestHelper.SUBJ0);
    
    //should come out of groupA
    assertFalse(groupA.hasMember(SubjectTestHelper.SUBJ0));

    // GrouperSession.startRootSession();
    // addMember("stem:a", "test.subject.0");
    // addMember("stem:b", "test.subject.0");
    // delMember("stem:b", "test.subject.0");
    // hasMember("stem:a", "test.subject.0");
    
  }

  /**
   * 
   */
  public void testRuleLonghandValidations() {
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Group groupA = new GroupSave(grouperSession).assignName("stem:a").assignCreateParentStemsIfNotExist(true).save();
    Group groupB = new GroupSave(grouperSession).assignName("stem:b").assignCreateParentStemsIfNotExist(true).save();
    
    //add a rule on stem:a saying if you are out of stem:b, then remove from stem:a
    AttributeAssign attributeAssign = groupA
      .getAttributeDelegate().assignAttribute(RuleUtils.ruleAttributeDefName()).getAttributeAssign();
    
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleActAsSubjectSourceIdName(), "g:isa");
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleActAsSubjectIdName(), "GrouperSystem");
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleCheckOwnerNameName(), "stem:b");
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleCheckTypeName(), 
        RuleCheckType.membershipRemove.name());
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleIfConditionEnumName(), 
        RuleIfConditionEnum.thisGroupHasImmediateEnabledMembership.name());
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleThenElName(), 
        "${ruleElUtils.removeMemberFromGroupId(ownerGroupId, memberId)}");
    
    
    //###############################
    //subject not found

    //should be valid
    String isValidString = attributeAssign.getAttributeValueDelegate().retrieveValueString(
        RuleUtils.ruleValidName());
    assertEquals("T", isValidString);

    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleActAsSubjectIdName(), "GrouperSystemAbc");
    isValidString = attributeAssign.getAttributeValueDelegate().retrieveValueString(
        RuleUtils.ruleValidName());
    assertTrue(isValidString, !StringUtils.equals("T", isValidString));
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleActAsSubjectIdName(), "GrouperSystem");

    //######################
    //check el valid
    isValidString = attributeAssign.getAttributeValueDelegate().retrieveValueString(
        RuleUtils.ruleValidName());
    assertEquals("T", isValidString);
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleCheckTypeName(), 
        RuleCheckType.membershipRemove.name() + "abc");
    isValidString = attributeAssign.getAttributeValueDelegate().retrieveValueString(
        RuleUtils.ruleValidName());
    assertTrue(isValidString, !StringUtils.equals("T", isValidString));
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleCheckTypeName(), 
        RuleCheckType.membershipRemove.name());

    //######################
    //check owner not found
    isValidString = attributeAssign.getAttributeValueDelegate().retrieveValueString(
        RuleUtils.ruleValidName());
    assertEquals("T", isValidString);
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleCheckOwnerNameName(), "stem:abc");
    isValidString = attributeAssign.getAttributeValueDelegate().retrieveValueString(
        RuleUtils.ruleValidName());
    assertTrue(isValidString, !StringUtils.equals("T", isValidString));
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleCheckOwnerNameName(), "stem:b");

    //######################
    //check owner id and name entered
    isValidString = attributeAssign.getAttributeValueDelegate().retrieveValueString(
        RuleUtils.ruleValidName());
    assertEquals("T", isValidString);
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleCheckOwnerIdName(), groupB.getId());
    isValidString = attributeAssign.getAttributeValueDelegate().retrieveValueString(
        RuleUtils.ruleValidName());
    assertTrue(isValidString, !StringUtils.equals("T", isValidString));
    attributeAssign.getAttributeValueDelegate().deleteValue(
        RuleUtils.ruleCheckOwnerIdName(), groupB.getId());

    //######################
    //neither check owner id and name entered
    isValidString = attributeAssign.getAttributeValueDelegate().retrieveValueString(
        RuleUtils.ruleValidName());
    assertEquals("T", isValidString);
    attributeAssign.getAttributeValueDelegate().deleteValue(
        RuleUtils.ruleCheckOwnerNameName(), "stem:b");
    isValidString = attributeAssign.getAttributeValueDelegate().retrieveValueString(
        RuleUtils.ruleValidName());
    assertTrue(isValidString, !StringUtils.equals("T", isValidString));
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleCheckOwnerNameName(), "stem:b");

    //######################
    //neither check owner id and name entered
    isValidString = attributeAssign.getAttributeValueDelegate().retrieveValueString(
        RuleUtils.ruleValidName());
    assertEquals("T", isValidString);
    attributeAssign.getAttributeValueDelegate().deleteValue(
        RuleUtils.ruleCheckOwnerNameName(), "stem:b");
    isValidString = attributeAssign.getAttributeValueDelegate().retrieveValueString(
        RuleUtils.ruleValidName());
    assertTrue(isValidString, !StringUtils.equals("T", isValidString));

    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleCheckOwnerIdName(), groupB.getUuid());
    isValidString = attributeAssign.getAttributeValueDelegate().retrieveValueString(
        RuleUtils.ruleValidName());
    assertEquals("T", isValidString);

    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleCheckOwnerIdName(), groupB.getUuid() + "abc");

    isValidString = attributeAssign.getAttributeValueDelegate().retrieveValueString(
        RuleUtils.ruleValidName());
    assertTrue(isValidString, !StringUtils.equals("T", isValidString));

    attributeAssign.getAttributeValueDelegate().deleteValue(
        RuleUtils.ruleCheckOwnerIdName(), groupB.getUuid() + "abc");
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleCheckOwnerNameName(), "stem:b");

    //######################
    //neither if el or enum entered is ok
    isValidString = attributeAssign.getAttributeValueDelegate().retrieveValueString(
        RuleUtils.ruleValidName());
    assertEquals("T", isValidString);
    attributeAssign.getAttributeValueDelegate().deleteValue(
        RuleUtils.ruleIfConditionEnumName(), 
        RuleIfConditionEnum.thisGroupHasImmediateEnabledMembership.name());
    isValidString = attributeAssign.getAttributeValueDelegate().retrieveValueString(
        RuleUtils.ruleValidName());
    assertEquals("T", isValidString);
    //assertTrue(isValidString, !StringUtils.equals("T", isValidString));
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleIfConditionEnumName(), 
        RuleIfConditionEnum.thisGroupHasImmediateEnabledMembership.name());
    
    //######################
    //both if el or enum entered is not ok
    isValidString = attributeAssign.getAttributeValueDelegate().retrieveValueString(
        RuleUtils.ruleValidName());
    assertEquals("T", isValidString);
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleIfConditionElName(),
        "abc");
    isValidString = attributeAssign.getAttributeValueDelegate().retrieveValueString(
        RuleUtils.ruleValidName());
    assertTrue(isValidString, !StringUtils.equals("T", isValidString));
    attributeAssign.getAttributeValueDelegate().deleteValue(
        RuleUtils.ruleIfConditionElName(),
        "abc");
    
    //######################
    //both then el or enum entered is not ok
    isValidString = attributeAssign.getAttributeValueDelegate().retrieveValueString(
        RuleUtils.ruleValidName());
    assertEquals("T", isValidString);
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleThenEnumName(), 
        RuleThenEnum.removeMemberFromOwnerGroup.name());
    isValidString = attributeAssign.getAttributeValueDelegate().retrieveValueString(
        RuleUtils.ruleValidName());
    assertTrue(isValidString, !StringUtils.equals("T", isValidString));
    attributeAssign.getAttributeValueDelegate().deleteValue(
        RuleUtils.ruleThenEnumName(), 
        RuleThenEnum.removeMemberFromOwnerGroup.name());

    //######################
    //neither then el or enum entered is not ok
    isValidString = attributeAssign.getAttributeValueDelegate().retrieveValueString(
        RuleUtils.ruleValidName());
    assertEquals("T", isValidString);
    attributeAssign.getAttributeValueDelegate().deleteValue(
        RuleUtils.ruleThenElName(), 
        "${ruleElUtils.removeMemberFromGroupId(ownerGroupId, memberId)}");
    isValidString = attributeAssign.getAttributeValueDelegate().retrieveValueString(
        RuleUtils.ruleValidName());
    assertTrue(isValidString, !StringUtils.equals("T", isValidString));
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleThenElName(), 
        "${ruleElUtils.removeMemberFromGroupId(ownerGroupId, memberId)}");

    //######################
    //invalid enum is not ok
    isValidString = attributeAssign.getAttributeValueDelegate().retrieveValueString(
        RuleUtils.ruleValidName());
    assertEquals("T", isValidString);
    attributeAssign.getAttributeValueDelegate().deleteValue(
        RuleUtils.ruleThenElName(), 
        "${ruleElUtils.removeMemberFromGroupId(ownerGroupId, memberId)}");
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleThenEnumName(), 
        RuleThenEnum.removeMemberFromOwnerGroup.name());
    isValidString = attributeAssign.getAttributeValueDelegate().retrieveValueString(
        RuleUtils.ruleValidName());
    assertEquals("T", isValidString);
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleThenEnumName(), 
        RuleThenEnum.removeMemberFromOwnerGroup.name() + "abc");
    isValidString = attributeAssign.getAttributeValueDelegate().retrieveValueString(
        RuleUtils.ruleValidName());
    assertTrue(isValidString, !StringUtils.equals("T", isValidString));
    attributeAssign.getAttributeValueDelegate().deleteValue(
        RuleUtils.ruleThenEnumName(), 
        RuleThenEnum.removeMemberFromOwnerGroup.name() + "abc");
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleThenElName(), 
        "${ruleElUtils.removeMemberFromGroupId(ownerGroupId, memberId)}");

    
    //######################
    //daemon not ok
    isValidString = attributeAssign.getAttributeValueDelegate().retrieveValueString(
        RuleUtils.ruleValidName());
    assertEquals("T", isValidString);
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleRunDaemonName(), 
        "A");
    isValidString = attributeAssign.getAttributeValueDelegate().retrieveValueString(
        RuleUtils.ruleValidName());
    assertTrue(isValidString, !StringUtils.equals("T", isValidString));
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleRunDaemonName(), 
        "T");
    isValidString = attributeAssign.getAttributeValueDelegate().retrieveValueString(
        RuleUtils.ruleValidName());
    assertEquals("T", isValidString);
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleRunDaemonName(), 
        "F");
    isValidString = attributeAssign.getAttributeValueDelegate().retrieveValueString(
        RuleUtils.ruleValidName());
    assertEquals("T", isValidString);
    attributeAssign.getAttributeValueDelegate().deleteValue(
        RuleUtils.ruleRunDaemonName(), 
        "F");

    //######################
    //daemon not ok if if is EL
    isValidString = attributeAssign.getAttributeValueDelegate().retrieveValueString(
        RuleUtils.ruleValidName());
    assertEquals("T", isValidString);
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleRunDaemonName(), 
        "T");
    attributeAssign.getAttributeValueDelegate().deleteValue(
        RuleUtils.ruleIfConditionEnumName(), 
        RuleIfConditionEnum.thisGroupHasImmediateEnabledMembership.name());
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleIfConditionElName(), 
        "abc");
    isValidString = attributeAssign.getAttributeValueDelegate().retrieveValueString(
        RuleUtils.ruleValidName());
    assertTrue(isValidString, !StringUtils.equals("T", isValidString));
    attributeAssign.getAttributeValueDelegate().deleteValue(
        RuleUtils.ruleRunDaemonName(), 
        "T");
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleIfConditionEnumName(), 
        RuleIfConditionEnum.thisGroupHasImmediateEnabledMembership.name());
    attributeAssign.getAttributeValueDelegate().deleteValue(
        RuleUtils.ruleIfConditionElName(), 
        "abc");

    
  }

  /**
   * 
   */
  public void testRuleLonghandRemove() {
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Group groupA = new GroupSave(grouperSession).assignName("stem:a").assignCreateParentStemsIfNotExist(true).save();
    Group groupB = new GroupSave(grouperSession).assignName("stem:b").assignCreateParentStemsIfNotExist(true).save();
    
    //add a rule on stem:a saying if you are out of stem:b, then remove from stem:a
    AttributeAssign attributeAssign = groupA
      .getAttributeDelegate().assignAttribute(RuleUtils.ruleAttributeDefName()).getAttributeAssign();
    
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleActAsSubjectSourceIdName(), "g:isa");
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleActAsSubjectIdName(), "GrouperSystem");
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleCheckOwnerNameName(), "stem:b");
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleCheckTypeName(), 
        RuleCheckType.membershipRemove.name());
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleIfConditionEnumName(), 
        RuleIfConditionEnum.thisGroupHasImmediateEnabledMembership.name());
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleThenElName(), 
        "${ruleElUtils.removeMemberFromGroupId(ownerGroupId, memberId)}");
    
    groupB.addMember(SubjectTestHelper.SUBJ0);
    groupA.addMember(SubjectTestHelper.SUBJ0);
    
    //count rule firings
    
    groupB.deleteMember(SubjectTestHelper.SUBJ0);
    
    //should come out of groupA
    assertFalse(groupA.hasMember(SubjectTestHelper.SUBJ0));
  
    groupA.getAttributeDelegate().removeAttribute(RuleUtils.ruleAttributeDefName()).getAttributeAssign();
  
    
    
    // GrouperSession.startRootSession();
    // addMember("stem:a", "test.subject.0");
    // addMember("stem:b", "test.subject.0");
    // delMember("stem:b", "test.subject.0");
    // hasMember("stem:a", "test.subject.0");
    
  }

  /**
   * 
   */
  public void testRuleLonghandDaemonFixer() {
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Group groupA = new GroupSave(grouperSession).assignName("stem:a").assignCreateParentStemsIfNotExist(true).save();
    Group groupB = new GroupSave(grouperSession).assignName("stem:b").assignCreateParentStemsIfNotExist(true).save();
    
    //add a rule on stem:a saying if you are out of stem:b, then remove from stem:a
    AttributeAssign attributeAssign = groupA
      .getAttributeDelegate().assignAttribute(RuleUtils.ruleAttributeDefName()).getAttributeAssign();
    
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleActAsSubjectSourceIdName(), "g:isa");
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleActAsSubjectIdName(), "GrouperSystem");
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleCheckOwnerNameName(), "stem:b");
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleCheckTypeName(), 
        RuleCheckType.membershipRemove.name());
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleIfConditionEnumName(), 
        RuleIfConditionEnum.thisGroupHasImmediateEnabledMembership.name());
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleThenElName(), 
        "${ruleElUtils.removeMemberFromGroupId(ownerGroupId, memberId)}");
    
    //subj 0 should be taken out
    groupA.addMember(SubjectTestHelper.SUBJ0);
    
    //subj 1 should be left alone
    groupA.addMember(SubjectTestHelper.SUBJ1);
    groupB.addMember(SubjectTestHelper.SUBJ1);

    //run the daemon
    String status = GrouperLoader.runOnceByJobName(grouperSession, GrouperLoaderType.GROUPER_RULES);
    
    assertTrue(status.toLowerCase().contains("success"));
    
    //should come out of groupA
    assertFalse(groupA.hasMember(SubjectTestHelper.SUBJ0));
  
    // GrouperSession.startRootSession();
    // addMember("stem:a", "test.subject.0");
    // addMember("stem:b", "test.subject.0");
    // delMember("stem:b", "test.subject.0");
    // hasMember("stem:a", "test.subject.0");
    
  }

  /**
   * 
   */
  public void testRuleLonghandDisabledDate() {
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Group groupA = new GroupSave(grouperSession).assignName("stem:a").assignCreateParentStemsIfNotExist(true).save();
    Group groupB = new GroupSave(grouperSession).assignName("stem:b").assignCreateParentStemsIfNotExist(true).save();
    
    //add a rule on stem:a saying if you are out of stem:b, then remove from stem:a
    AttributeAssign attributeAssign = groupA
      .getAttributeDelegate().assignAttribute(RuleUtils.ruleAttributeDefName()).getAttributeAssign();
    
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleActAsSubjectSourceIdName(), "g:isa");
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleActAsSubjectIdName(), "GrouperSystem");
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleCheckOwnerNameName(), "stem:b");
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleCheckTypeName(), 
        RuleCheckType.membershipRemove.name());
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleIfConditionEnumName(), 
        RuleIfConditionEnum.thisGroupHasImmediateEnabledMembership.name());
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleThenElName(), 
        "${ruleElUtils.assignMembershipDisabledDaysForGroupId(ownerGroupId, memberId, 7)}");
    
    groupB.addMember(SubjectTestHelper.SUBJ0);
  
    //count rule firings
    long initialFirings = RuleEngine.ruleFirings;
    
    //doesnt do anything
    groupB.deleteMember(SubjectTestHelper.SUBJ0);
  
    assertEquals(initialFirings, RuleEngine.ruleFirings);
  
    groupB.addMember(SubjectTestHelper.SUBJ0);
    groupA.addMember(SubjectTestHelper.SUBJ0);
    
    //count rule firings
    
    groupB.deleteMember(SubjectTestHelper.SUBJ0);
    
    //should have a disabled date in groupA
    
    Member member0 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ0, false);
    
    Membership membership = groupA.getImmediateMembership(Group.getDefaultList(), member0, true, true);
    
    assertNotNull(membership.getDisabledTime());
    long disabledTime = membership.getDisabledTime().getTime();
    
    assertTrue("More than 6 days: " + new Date(disabledTime), disabledTime > System.currentTimeMillis() + (6 * 24 * 60 * 60 * 1000));
    assertTrue("Less than 8 days: " + new Date(disabledTime), disabledTime < System.currentTimeMillis() + (8 * 24 * 60 * 60 * 1000));
  
    assertEquals(initialFirings+1, RuleEngine.ruleFirings);
  
    // GrouperSession.startRootSession();
    // addMember("stem:a", "test.subject.0");
    // addMember("stem:b", "test.subject.0");
    // delMember("stem:b", "test.subject.0");
    // hasMember("stem:a", "test.subject.0");
    
  }

  /**
   * 
   */
  public void testRuleLonghandThenEnum() {
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Group groupA = new GroupSave(grouperSession).assignName("stem:a").assignCreateParentStemsIfNotExist(true).save();
    Group groupB = new GroupSave(grouperSession).assignName("stem:b").assignCreateParentStemsIfNotExist(true).save();
    
    //add a rule on stem:a saying if you are out of stem:b, then remove from stem:a
    AttributeAssign attributeAssign = groupA
      .getAttributeDelegate().assignAttribute(RuleUtils.ruleAttributeDefName()).getAttributeAssign();
    
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleActAsSubjectSourceIdName(), "g:isa");
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleActAsSubjectIdName(), "GrouperSystem");
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleCheckOwnerNameName(), "stem:b");
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleCheckTypeName(), 
        RuleCheckType.membershipRemove.name());
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleIfConditionEnumName(), 
        RuleIfConditionEnum.thisGroupHasImmediateEnabledMembership.name());
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleThenEnumName(), 
        RuleThenEnum.removeMemberFromOwnerGroup.name());
    
    groupB.addMember(SubjectTestHelper.SUBJ0);
  
    //count rule firings
    long initialFirings = RuleEngine.ruleFirings;
    
    //doesnt do anything
    groupB.deleteMember(SubjectTestHelper.SUBJ0);
  
    assertEquals(initialFirings, RuleEngine.ruleFirings);
  
    groupB.addMember(SubjectTestHelper.SUBJ0);
    groupA.addMember(SubjectTestHelper.SUBJ0);
    
    //count rule firings
    
    groupB.deleteMember(SubjectTestHelper.SUBJ0);
    
    //should come out of groupA
    assertFalse(groupA.hasMember(SubjectTestHelper.SUBJ0));
  
    assertEquals(initialFirings+1, RuleEngine.ruleFirings);
  
    // GrouperSession.startRootSession();
    // addMember("stem:a", "test.subject.0");
    // addMember("stem:b", "test.subject.0");
    // delMember("stem:b", "test.subject.0");
    // hasMember("stem:a", "test.subject.0");
    
  }

  /**
   * 
   */
  public void testRuleLonghandStemScopeOneCreateGroup() {
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Stem stem2 = new StemSave(grouperSession).assignName("stem2").assignCreateParentStemsIfNotExist(true).save();
  
    Group groupA = new GroupSave(grouperSession).assignName("stem1:a").assignCreateParentStemsIfNotExist(true).save();
  
    groupA.addMember(SubjectTestHelper.SUBJ0);
    
    
    //add a rule on stem2 saying if you create a group underneath, then assign a reader group
    AttributeAssign attributeAssign = stem2
      .getAttributeDelegate().assignAttribute(RuleUtils.ruleAttributeDefName()).getAttributeAssign();
    
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleActAsSubjectSourceIdName(), "g:isa");
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleActAsSubjectIdName(), "GrouperSystem");
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleCheckOwnerNameName(), "stem2");
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleCheckTypeName(), 
        RuleCheckType.groupCreate.name());
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleCheckStemScopeName(),
        Stem.Scope.ONE.name());
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleThenElName(), 
        "${ruleElUtils.assignGroupPrivilege(groupId, 'g:gsa', null, 'stem1:a', 'read,update')}");
    
    long initialFirings = RuleEngine.ruleFirings;
    
    
    Group groupB = new GroupSave(grouperSession).assignName("stem2:b").assignCreateParentStemsIfNotExist(true).save();
  
    //count rule firings
    assertEquals(initialFirings+1, RuleEngine.ruleFirings);
  
    //make sure allowed
    assertTrue(groupB.hasUpdate(SubjectTestHelper.SUBJ0));
    assertTrue(groupB.hasRead(SubjectTestHelper.SUBJ0));
    
    
    Group groupD = new GroupSave(grouperSession).assignName("stem3:b").assignCreateParentStemsIfNotExist(true).save();
  
    assertEquals(initialFirings+1, RuleEngine.ruleFirings);
    
    assertFalse(groupD.hasUpdate(SubjectTestHelper.SUBJ0));
    assertFalse(groupD.hasRead(SubjectTestHelper.SUBJ0));
    
    
    Group groupC = new GroupSave(grouperSession).assignName("stem2:sub:c").assignCreateParentStemsIfNotExist(true).save();

    assertEquals(initialFirings+1, RuleEngine.ruleFirings);

    assertFalse(groupC.hasRead(SubjectTestHelper.SUBJ0));
    assertFalse(groupC.hasUpdate(SubjectTestHelper.SUBJ0));
  
  
    // GrouperSession.startRootSession();
    // addMember("stem:a", "test.subject.0");
    // addMember("stem:b", "test.subject.0");
    // delMember("stem:b", "test.subject.0");
    // hasMember("stem:a", "test.subject.0");
    
  }

  /**
   * 
   */
  public void testRuleLonghandStemScopeSubCreateStem() {
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Stem stem2 = new StemSave(grouperSession).assignName("stem2").assignCreateParentStemsIfNotExist(true).save();
  
    Group groupA = new GroupSave(grouperSession).assignName("stem1:a").assignCreateParentStemsIfNotExist(true).save();
  
    groupA.addMember(SubjectTestHelper.SUBJ0);
    
    
    //add a rule on stem2 saying if you create a group underneath, then assign a reader group
    AttributeAssign attributeAssign = stem2
      .getAttributeDelegate().assignAttribute(RuleUtils.ruleAttributeDefName()).getAttributeAssign();
    
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleActAsSubjectSourceIdName(), "g:isa");
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleActAsSubjectIdName(), "GrouperSystem");
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleCheckOwnerNameName(), "stem2");
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleCheckTypeName(), 
        RuleCheckType.stemCreate.name());
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleCheckStemScopeName(),
        Stem.Scope.SUB.name());
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleThenElName(), 
        "${ruleElUtils.assignStemPrivilege(stemId, 'g:gsa', null, 'stem1:a', 'stem,create')}");
    
    long initialFirings = RuleEngine.ruleFirings;
    
    
    Stem stemB = new StemSave(grouperSession).assignName("stem2:b").assignCreateParentStemsIfNotExist(true).save();
  
    //count rule firings
    assertEquals(initialFirings+1, RuleEngine.ruleFirings);
  
    //make sure allowed
    assertTrue(stemB.hasCreate(SubjectTestHelper.SUBJ0));
    assertTrue(stemB.hasStem(SubjectTestHelper.SUBJ0));
    
    
    Stem stemD = new StemSave(grouperSession).assignName("stem3:b").assignCreateParentStemsIfNotExist(true).save();
  
    assertEquals(initialFirings+1, RuleEngine.ruleFirings);
    
    assertFalse(stemD.hasCreate(SubjectTestHelper.SUBJ0));
    assertFalse(stemD.hasStem(SubjectTestHelper.SUBJ0));
    
    
    Stem stemC = new StemSave(grouperSession).assignName("stem2:sub:c").assignCreateParentStemsIfNotExist(true).save();
  
    //fires for the sub stem and c stem
    assertEquals(initialFirings+3, RuleEngine.ruleFirings);
  
    assertTrue(stemC.hasCreate(SubjectTestHelper.SUBJ0));
    assertTrue(stemC.hasStem(SubjectTestHelper.SUBJ0));
  
  
    // GrouperSession.startRootSession();
    // addMember("stem:a", "test.subject.0");
    // addMember("stem:b", "test.subject.0");
    // delMember("stem:b", "test.subject.0");
    // hasMember("stem:a", "test.subject.0");
    
  }

  /**
   * 
   */
  public void testRuleLonghandStemScopeOneCreateStem() {
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Stem stem2 = new StemSave(grouperSession).assignName("stem2").assignCreateParentStemsIfNotExist(true).save();
  
    Group groupA = new GroupSave(grouperSession).assignName("stem1:a").assignCreateParentStemsIfNotExist(true).save();
  
    groupA.addMember(SubjectTestHelper.SUBJ0);
    
    
    //add a rule on stem2 saying if you create a group underneath, then assign a reader group
    AttributeAssign attributeAssign = stem2
      .getAttributeDelegate().assignAttribute(RuleUtils.ruleAttributeDefName()).getAttributeAssign();
    
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleActAsSubjectSourceIdName(), "g:isa");
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleActAsSubjectIdName(), "GrouperSystem");
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleCheckOwnerNameName(), "stem2");
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleCheckTypeName(), 
        RuleCheckType.stemCreate.name());
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleCheckStemScopeName(),
        Stem.Scope.ONE.name());
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleThenElName(), 
        "${ruleElUtils.assignStemPrivilege(stemId, 'g:gsa', null, 'stem1:a', 'stem,create')}");
    
    long initialFirings = RuleEngine.ruleFirings;
    
    
    Stem stemB = new StemSave(grouperSession).assignName("stem2:b").assignCreateParentStemsIfNotExist(true).save();
  
    //count rule firings
    assertEquals(initialFirings+1, RuleEngine.ruleFirings);
  
    //make sure allowed
    assertTrue(stemB.hasCreate(SubjectTestHelper.SUBJ0));
    assertTrue(stemB.hasStem(SubjectTestHelper.SUBJ0));
    
    
    Stem stemD = new StemSave(grouperSession).assignName("stem3:b").assignCreateParentStemsIfNotExist(true).save();
  
    assertEquals(initialFirings+1, RuleEngine.ruleFirings);
    
    assertFalse(stemD.hasCreate(SubjectTestHelper.SUBJ0));
    assertFalse(stemD.hasStem(SubjectTestHelper.SUBJ0));
    
    
    Stem stemC = new StemSave(grouperSession).assignName("stem2:sub:c").assignCreateParentStemsIfNotExist(true).save();
    Stem stemSub = StemFinder.findByName(grouperSession, "stem2:sub", true);
  
    //fires for the sub stem and not c stem
    assertEquals(initialFirings+2, RuleEngine.ruleFirings);
  
    assertFalse(stemC.hasCreate(SubjectTestHelper.SUBJ0));
    assertFalse(stemC.hasStem(SubjectTestHelper.SUBJ0));
  
    assertTrue(stemSub.hasCreate(SubjectTestHelper.SUBJ0));
    assertTrue(stemSub.hasStem(SubjectTestHelper.SUBJ0));
  
  
    // GrouperSession.startRootSession();
    // addMember("stem:a", "test.subject.0");
    // addMember("stem:b", "test.subject.0");
    // delMember("stem:b", "test.subject.0");
    // hasMember("stem:a", "test.subject.0");
    
  }

  /**
   * 
   */
  public void testRuleLonghandPrint() {
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Group groupA = new GroupSave(grouperSession).assignName("stem:a").assignCreateParentStemsIfNotExist(true).save();
    new GroupSave(grouperSession).assignName("stem:b").assignCreateParentStemsIfNotExist(true).save();
    new GroupSave(grouperSession).assignName("stem:c").assignCreateParentStemsIfNotExist(true).save();
    
    //add a rule on stem:a saying if you are out of stem:b, then remove from stem:a
    AttributeAssign attributeAssign = groupA
      .getAttributeDelegate().assignAttribute(RuleUtils.ruleAttributeDefName()).getAttributeAssign();
    
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleActAsSubjectSourceIdName(), "g:isa");
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleActAsSubjectIdName(), "GrouperSystem");
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleCheckOwnerNameName(), "stem:b");
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleCheckTypeName(), 
        RuleCheckType.membershipRemove.name());
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleIfConditionEnumName(), 
        RuleIfConditionEnum.thisGroupHasImmediateEnabledMembership.name());
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleThenElName(), 
        "${ruleElUtils.removeMemberFromGroupId(ownerGroupId, memberId)}");
    
    
    //add a rule on stem:a saying if you are out of stem:b, then remove from stem:a
    attributeAssign = groupA
      .getAttributeDelegate().addAttribute(RuleUtils.ruleAttributeDefName()).getAttributeAssign();
    
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleActAsSubjectSourceIdName(), "g:isa");
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleActAsSubjectIdName(), "GrouperSystem");
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleCheckOwnerNameName(), "stem:c");
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleCheckTypeName(), 
        RuleCheckType.membershipRemove.name());
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleIfConditionEnumName(), 
        RuleIfConditionEnum.thisGroupHasImmediateEnabledMembership.name());
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleThenElName(), 
        "${ruleElUtils.removeMemberFromGroupId(ownerGroupId, memberId)}");

    System.out.println(RuleApi.rulesToString(groupA));
    
  }

  /**
   * 
   */
  public void testRuleLonghandGshFixer() {
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Group groupA = new GroupSave(grouperSession).assignName("stem:a").assignCreateParentStemsIfNotExist(true).save();
    Group groupB = new GroupSave(grouperSession).assignName("stem:b").assignCreateParentStemsIfNotExist(true).save();
    
    //add a rule on stem:a saying if you are out of stem:b, then remove from stem:a
    AttributeAssign attributeAssign = groupA
      .getAttributeDelegate().assignAttribute(RuleUtils.ruleAttributeDefName()).getAttributeAssign();
    
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleActAsSubjectSourceIdName(), "g:isa");
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleActAsSubjectIdName(), "GrouperSystem");
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleCheckOwnerNameName(), "stem:b");
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleCheckTypeName(), 
        RuleCheckType.membershipRemove.name());
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleIfConditionEnumName(), 
        RuleIfConditionEnum.thisGroupHasImmediateEnabledMembership.name());
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleThenElName(), 
        "${ruleElUtils.removeMemberFromGroupId(ownerGroupId, memberId)}");
    
    //subj 0 should be taken out
    groupA.addMember(SubjectTestHelper.SUBJ0);
    
    //subj 1 should be left alone
    groupA.addMember(SubjectTestHelper.SUBJ1);
    groupB.addMember(SubjectTestHelper.SUBJ1);
  
    //run the daemon
    int ruleCount = RuleApi.runRulesForOwner(groupA);
    
    assertEquals(1, ruleCount);
    
    //should come out of groupA
    assertFalse(groupA.hasMember(SubjectTestHelper.SUBJ0));
  
    // GrouperSession.startRootSession();
    // addMember("stem:a", "test.subject.0");
    // addMember("stem:b", "test.subject.0");
    // delMember("stem:b", "test.subject.0");
    // hasMember("stem:a", "test.subject.0");
    
  }
  
}
