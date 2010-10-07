/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.rules;

import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
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
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.AttributeDefNameSave;
import edu.internet2.middleware.grouper.attr.AttributeDefSave;
import edu.internet2.middleware.grouper.attr.AttributeDefType;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.value.AttributeAssignValueContainer;
import edu.internet2.middleware.grouper.attr.value.AttributeValueDelegate;
import edu.internet2.middleware.grouper.cache.GrouperCacheUtils;
import edu.internet2.middleware.grouper.cfg.ApiConfig;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.group.TypeOfGroup;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.permissions.PermissionEntry;
import edu.internet2.middleware.grouper.permissions.role.Role;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.AttributeDefPrivilege;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
import edu.internet2.middleware.grouper.subj.SafeSubject;
import edu.internet2.middleware.grouper.util.GrouperEmail;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;


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
    TestRunner.run(new RuleTest("testRuleLonghandStemScopeSubCreateGroupNamePattern"));
    //TestRunner.run(RuleTest.class);
  }

  /**
   * 
   */
  public void testElOnSafeSubject() {
    SafeSubject safeSubject = new SafeSubject(SubjectTestHelper.SUBJ0);
    String script = "Hello ${subject.name}, ${subject.getAttributeValue('loginid')}";
    Map<String, Object> variableMap = new HashMap<String, Object>();
    variableMap.put("subject", safeSubject);
    String result = GrouperUtil.substituteExpressionLanguage(script, variableMap);
    assertEquals("Hello my name is test.subject.0, id.test.subject.0", result);
    
    GrouperCacheUtils.clearAllCaches();
    
    script = "Email ${subject.emailAddress}";
    result = GrouperUtil.substituteExpressionLanguage(script, variableMap);
    assertEquals("Email test.subject.0@somewhere.someSchool.edu", result);
    
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
      .getAttributeDelegate().addAttribute(RuleUtils.ruleAttributeDefName()).getAttributeAssign();
    
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

    // grouperSession = GrouperSession.startRootSession();
    // addMember("stem:a", "test.subject.0");
    // addMember("stem:b", "test.subject.0");
    // delMember("stem:b", "test.subject.0");
    // hasMember("stem:a", "test.subject.0");
    
  }

  /**
   * 
   */
  public void testRuleLonghandPermissionAssignment() {

    GrouperSession grouperSession = GrouperSession.startRootSession();
    AttributeDef permissionDef = new AttributeDefSave(grouperSession)
      .assignName("stem:permissionDef").assignCreateParentStemsIfNotExist(true)
      .assignAttributeDefType(AttributeDefType.perm)
      .save();
    
    permissionDef.setAssignToEffMembership(true);
    permissionDef.setAssignToGroup(true);
    permissionDef.store();
    
    Group groupEmployee = new GroupSave(grouperSession).assignName("stem:employee").assignCreateParentStemsIfNotExist(true).save();

    //make a role
    Role payrollUser = new GroupSave(grouperSession).assignName("apps:payroll:roles:payrollUser").assignTypeOfGroup(TypeOfGroup.role).assignCreateParentStemsIfNotExist(true).save();
    Role payrollGuest = new GroupSave(grouperSession).assignName("apps:payroll:roles:payrollGuest").assignTypeOfGroup(TypeOfGroup.role).assignCreateParentStemsIfNotExist(true).save();

    //assign a user to a role
    payrollUser.addMember(SubjectTestHelper.SUBJ0, false);
    payrollGuest.addMember(SubjectTestHelper.SUBJ1, false);
    
    //create a permission, assign to role
    AttributeDefName canLogin = new AttributeDefNameSave(grouperSession, permissionDef).assignName("apps:payroll:permissions:canLogin").assignCreateParentStemsIfNotExist(true).save();
    
    payrollUser.getPermissionRoleDelegate().assignRolePermission(canLogin);
    
    //assign the permission to another user directly, not due to a role
    payrollGuest.getPermissionRoleDelegate().assignSubjectRolePermission(canLogin, SubjectTestHelper.SUBJ1);
    
    //see that they both have the permission
    Member member0 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ0, false);
    Member member1 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ1, false);
    
    Set<PermissionEntry> permissions = GrouperDAOFactory.getFactory().getPermissionEntry().findByMemberId(member0.getUuid());
    assertEquals(1, permissions.size());
    assertEquals("apps:payroll:permissions:canLogin", permissions.iterator().next().getAttributeDefNameName());
    
    permissions = GrouperDAOFactory.getFactory().getPermissionEntry().findByMemberId(member1.getUuid());
    assertEquals(1, permissions.size());
    assertEquals("apps:payroll:permissions:canLogin", permissions.iterator().next().getAttributeDefNameName());
    
    //add a rule on stem:permission saying if you are out of stem:employee, 
    //then remove assignments to permission, or from roles which have the permission
    AttributeAssign attributeAssign = permissionDef
      .getAttributeDelegate().addAttribute(RuleUtils.ruleAttributeDefName()).getAttributeAssign();
    
    AttributeValueDelegate attributeValueDelegate = attributeAssign.getAttributeValueDelegate();
    attributeValueDelegate.assignValue(
        RuleUtils.ruleActAsSubjectSourceIdName(), "g:isa");
    attributeValueDelegate.assignValue(
        RuleUtils.ruleActAsSubjectIdName(), "GrouperSystem");
    attributeValueDelegate.assignValue(
        RuleUtils.ruleCheckOwnerNameName(), "stem:employee");
    attributeValueDelegate.assignValue(
        RuleUtils.ruleCheckTypeName(), 
        RuleCheckType.membershipRemove.name());
    attributeValueDelegate.assignValue(
        RuleUtils.ruleIfConditionEnumName(), 
        RuleIfConditionEnum.thisPermissionDefHasAssignment.name());
    attributeValueDelegate.assignValue(
        RuleUtils.ruleThenEnumName(), 
        RuleThenEnum.removeMemberFromOwnerPermissionDefAssignments.name());

    //should be valid
    String isValidString = attributeValueDelegate.retrieveValueString(
        RuleUtils.ruleValidName());

    if (!StringUtils.equals("T", isValidString)) {
      throw new RuntimeException(isValidString);
    }

    groupEmployee.addMember(SubjectTestHelper.SUBJ0);
    groupEmployee.addMember(SubjectTestHelper.SUBJ1);
    groupEmployee.addMember(SubjectTestHelper.SUBJ2);
    
    //count rule firings
    long initialFirings = RuleEngine.ruleFirings;
    
    //doesnt do anything
    groupEmployee.deleteMember(SubjectTestHelper.SUBJ2);

    assertEquals(initialFirings, RuleEngine.ruleFirings);

    groupEmployee.deleteMember(SubjectTestHelper.SUBJ0);
    
    //should come out of groupA
    assertFalse(payrollUser.hasMember(SubjectTestHelper.SUBJ0));

    assertEquals(initialFirings+1, RuleEngine.ruleFirings);

    permissions = GrouperDAOFactory.getFactory().getPermissionEntry().findByMemberId(member0.getUuid());
    assertEquals(0, permissions.size());
    
    permissions = GrouperDAOFactory.getFactory().getPermissionEntry().findByMemberId(member1.getUuid());
    assertEquals(1, permissions.size());
    assertEquals("apps:payroll:permissions:canLogin", permissions.iterator().next().getAttributeDefNameName());
    
    //take out second user
    groupEmployee.deleteMember(SubjectTestHelper.SUBJ1);

    assertTrue(payrollGuest.hasMember(SubjectTestHelper.SUBJ1));

    permissions = GrouperDAOFactory.getFactory().getPermissionEntry().findByMemberId(member1.getUuid());
    assertEquals(0, permissions.size());
    
    // grouperSession = GrouperSession.startRootSession();
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
    Group ruleGroup = new GroupSave(grouperSession).assignName("stem:a").assignCreateParentStemsIfNotExist(true).save();
    Group mustBeInGroup = new GroupSave(grouperSession).assignName("stem:b").assignCreateParentStemsIfNotExist(true).save();
    
    //add a rule on stem:a saying if not in stem:b, then dont allow add to stem:a
    AttributeAssign attributeAssign = ruleGroup
      .getAttributeDelegate().addAttribute(RuleUtils.ruleAttributeDefName()).getAttributeAssign();
    
    AttributeValueDelegate attributeValueDelegate = attributeAssign.getAttributeValueDelegate();

    attributeValueDelegate.assignValue(
        RuleUtils.ruleActAsSubjectSourceIdName(), "g:isa");
    attributeValueDelegate.assignValue(
        RuleUtils.ruleActAsSubjectIdName(), "GrouperSystem");
    attributeValueDelegate.assignValue(
        RuleUtils.ruleCheckOwnerNameName(), "stem:a");
    attributeValueDelegate.assignValue(
        RuleUtils.ruleCheckTypeName(), RuleCheckType.membershipAdd.name());
    attributeValueDelegate.assignValue(
        RuleUtils.ruleIfConditionEnumName(), RuleIfConditionEnum.groupHasNoImmediateEnabledMembership.name());
    attributeValueDelegate.assignValue(
        RuleUtils.ruleIfOwnerNameName(), "stem:b");
    attributeValueDelegate.assignValue(
        RuleUtils.ruleThenEnumName(), RuleThenEnum.veto.name());
    
    //key which would be used in UI messages file if applicable
    attributeValueDelegate.assignValue(
        RuleUtils.ruleThenEnumArg0Name(), "rule.entity.must.be.a.member.of.stem.b");
    
    //error message (if key in UI messages file not there)
    attributeValueDelegate.assignValue(
        RuleUtils.ruleThenEnumArg1Name(), "Entity cannot be a member of stem:a if not a member of stem:b");

    //should be valid
    String isValidString = attributeValueDelegate.retrieveValueString(
        RuleUtils.ruleValidName());

    if (!StringUtils.equals("T", isValidString)) {
      throw new RuntimeException(isValidString);
    }

    //count rule firings
    long initialFirings = RuleEngine.ruleFirings;
    
    try {
      ruleGroup.addMember(SubjectTestHelper.SUBJ0);
      fail("Should be vetoed");
    } catch (RuleVeto rve) {
      //this is good
      String stack = ExceptionUtils.getFullStackTrace(rve);
      assertTrue(stack, stack.contains("Entity cannot be a member of stem:a if not a member of stem:b"));
    }

    assertEquals(initialFirings+1, RuleEngine.ruleFirings);

    mustBeInGroup.addMember(SubjectTestHelper.SUBJ0);
    ruleGroup.addMember(SubjectTestHelper.SUBJ0);
    
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
      .getAttributeDelegate().addAttribute(RuleUtils.ruleAttributeDefName()).getAttributeAssign();
    
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
      .getAttributeDelegate().addAttribute(RuleUtils.ruleAttributeDefName()).getAttributeAssign();
    
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
      .getAttributeDelegate().addAttribute(RuleUtils.ruleAttributeDefName()).getAttributeAssign();
    
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
      .getAttributeDelegate().addAttribute(RuleUtils.ruleAttributeDefName()).getAttributeAssign();
    
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
    
    //add a rule on stem:a saying if you are out of stem2, then remove from stem:a
    AttributeAssign attributeAssign = groupA
      .getAttributeDelegate().addAttribute(RuleUtils.ruleAttributeDefName()).getAttributeAssign();
    
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

    Group groupA = new GroupSave(grouperSession).assignName("stem1:admins").assignCreateParentStemsIfNotExist(true).save();

    groupA.addMember(SubjectTestHelper.SUBJ0);
    
    
    //add a rule on stem2 saying if you create a group underneath, then assign a reader and updater group
    AttributeAssign attributeAssign = stem2
      .getAttributeDelegate().addAttribute(RuleUtils.ruleAttributeDefName()).getAttributeAssign();
    
    AttributeValueDelegate attributeValueDelegate = attributeAssign.getAttributeValueDelegate();
    
    attributeValueDelegate.assignValue(
        RuleUtils.ruleActAsSubjectSourceIdName(), "g:isa");
    attributeValueDelegate.assignValue(
        RuleUtils.ruleActAsSubjectIdName(), "GrouperSystem");
    attributeValueDelegate.assignValue(
        RuleUtils.ruleCheckTypeName(), RuleCheckType.groupCreate.name());
    
    //can be SUB or ONE for if in this folder, or in this and all subfolders
    attributeValueDelegate.assignValue(
        RuleUtils.ruleCheckStemScopeName(), Stem.Scope.SUB.name());
    attributeValueDelegate.assignValue(
        RuleUtils.ruleThenEnumName(), RuleThenEnum.assignGroupPrivilegeToGroupId.name());
    
    //this is the subject string for the subject to assign to
    //e.g. sourceId :::::: subjectIdentifier
    //or sourceId :::: subjectId
    //or :::: subjectId
    //or sourceId ::::::: subjectIdOrIdentifier
    //etc
    attributeValueDelegate.assignValue(
        RuleUtils.ruleThenEnumArg0Name(), "g:gsa :::::: stem1:admins");
    
    //privileges to assign: read, admin, update, view, optin, optout
    attributeValueDelegate.assignValue(
        RuleUtils.ruleThenEnumArg1Name(), "read, update");
    
    //should be valid
    String isValidString = attributeValueDelegate.retrieveValueString(
        RuleUtils.ruleValidName());

    if (!StringUtils.equals("T", isValidString)) {
      throw new RuntimeException(isValidString);
    }

    long initialFirings = RuleEngine.ruleFirings;
    
    
    Group groupB = new GroupSave(grouperSession).assignName("stem2:b").assignCreateParentStemsIfNotExist(true).save();

    //count rule firings
    assertEquals(initialFirings+1, RuleEngine.ruleFirings);

    //make sure allowed
    assertTrue(groupB.hasUpdate(SubjectTestHelper.SUBJ0));
    assertTrue(groupB.hasRead(SubjectTestHelper.SUBJ0));
    
    
    Group groupD = new GroupSave(grouperSession).assignName("stem3:d").assignCreateParentStemsIfNotExist(true).save();

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
      .getAttributeDelegate().addAttribute(RuleUtils.ruleAttributeDefName()).getAttributeAssign();
    
    AttributeValueDelegate attributeValueDelegate = attributeAssign.getAttributeValueDelegate();
    
    attributeValueDelegate.assignValue(
        RuleUtils.ruleActAsSubjectSourceIdName(), "g:isa");
    attributeValueDelegate.assignValue(
        RuleUtils.ruleActAsSubjectIdName(), "GrouperSystem");
    
    //folder where membership was removed
    attributeValueDelegate.assignValue(
        RuleUtils.ruleCheckOwnerNameName(), "stem2");
    attributeValueDelegate.assignValue(
        RuleUtils.ruleCheckTypeName(), 
        RuleCheckType.membershipRemoveInFolder.name());

    //SUB for all descendants, ONE for just children
    attributeValueDelegate.assignValue(
        RuleUtils.ruleCheckStemScopeName(),
        Stem.Scope.SUB.name());
    
    //if there is no more membership in the folder, and there is a membership in the group
    attributeValueDelegate.assignValue(
        RuleUtils.ruleIfConditionEnumName(), 
        RuleIfConditionEnum.thisGroupAndNotFolderHasImmediateEnabledMembership.name());
    attributeValueDelegate.assignValue(
        RuleUtils.ruleThenEnumName(),
        RuleThenEnum.removeMemberFromOwnerGroup.name());

    //should be valid
    String isValidString = attributeValueDelegate.retrieveValueString(
        RuleUtils.ruleValidName());

    if (!StringUtils.equals("T", isValidString)) {
      throw new RuntimeException(isValidString);
    }

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
      .getAttributeDelegate().addAttribute(RuleUtils.ruleAttributeDefName()).getAttributeAssign();
    
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

    //rule should not be there
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
      .getAttributeDelegate().addAttribute(RuleUtils.ruleAttributeDefName()).getAttributeAssign();
    
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

    groupA.grantPriv(SubjectTestHelper.SUBJ9, AccessPrivilege.ADMIN, false);
    
    groupB.grantPriv(SubjectTestHelper.SUBJ9, AccessPrivilege.READ, false);

    RuleUtils.ruleTypeAttributeDef().getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ9, AttributeDefPrivilege.ATTR_UPDATE, false);
    RuleUtils.ruleAttrAttributeDef().getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ9, AttributeDefPrivilege.ATTR_UPDATE, false);
    RuleUtils.ruleTypeAttributeDef().getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ9, AttributeDefPrivilege.ATTR_READ, false);
    RuleUtils.ruleAttrAttributeDef().getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ9, AttributeDefPrivilege.ATTR_READ, false);
    
    //add a rule on stem:a saying if you are out of stem:b, then remove from stem:a
    AttributeAssign attributeAssign = groupA
      .getAttributeDelegate().addAttribute(RuleUtils.ruleAttributeDefName()).getAttributeAssign();
    
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

//    //######################
//    //neither check owner id and name entered
//    isValidString = attributeAssign.getAttributeValueDelegate().retrieveValueString(
//        RuleUtils.ruleValidName());
//    assertEquals("T", isValidString);
//    attributeAssign.getAttributeValueDelegate().deleteValue(
//        RuleUtils.ruleCheckOwnerNameName(), "stem:b");
//    isValidString = attributeAssign.getAttributeValueDelegate().retrieveValueString(
//        RuleUtils.ruleValidName());
//    assertTrue(isValidString, !StringUtils.equals("T", isValidString));
//    attributeAssign.getAttributeValueDelegate().assignValue(
//        RuleUtils.ruleCheckOwnerNameName(), "stem:b");

//    //######################
//    //neither check owner id and name entered
//    isValidString = attributeAssign.getAttributeValueDelegate().retrieveValueString(
//        RuleUtils.ruleValidName());
//    assertEquals("T", isValidString);
//    attributeAssign.getAttributeValueDelegate().deleteValue(
//        RuleUtils.ruleCheckOwnerNameName(), "stem:b");
//    isValidString = attributeAssign.getAttributeValueDelegate().retrieveValueString(
//        RuleUtils.ruleValidName());
//    assertTrue(isValidString, !StringUtils.equals("T", isValidString));

    isValidString = attributeAssign.getAttributeValueDelegate().retrieveValueString(
        RuleUtils.ruleValidName());
    assertEquals("T", isValidString);
    attributeAssign.getAttributeValueDelegate().deleteValue(
        RuleUtils.ruleCheckOwnerNameName(), "stem:b");
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

    //######################
    //not ok to act as grouper system if not already
    isValidString = attributeAssign.getAttributeValueDelegate().retrieveValueString(
        RuleUtils.ruleValidName());
    assertEquals("T", isValidString);
    
    GrouperSession.stopQuietly(grouperSession);
    grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ9);
    
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleRunDaemonName(), 
        "F");
    attributeAssign.getAttributeValueDelegate().deleteValue(
        RuleUtils.ruleRunDaemonName(), 
        "F");
    isValidString = attributeAssign.getAttributeValueDelegate().retrieveValueString(
        RuleUtils.ruleValidName());
    assertTrue(isValidString, !StringUtils.equals("T", isValidString));
    
    GrouperSession.stopQuietly(grouperSession);
    grouperSession = GrouperSession.startRootSession();

    
    
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
      .getAttributeDelegate().addAttribute(RuleUtils.ruleAttributeDefName()).getAttributeAssign();
    
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
      .getAttributeDelegate().addAttribute(RuleUtils.ruleAttributeDefName()).getAttributeAssign();
    
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
    
    //subj 0 should be taken out after daemon
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
      .getAttributeDelegate().addAttribute(RuleUtils.ruleAttributeDefName()).getAttributeAssign();
    
    AttributeValueDelegate attributeValueDelegate = attributeAssign.getAttributeValueDelegate();
    
    attributeValueDelegate.assignValue(
        RuleUtils.ruleActAsSubjectSourceIdName(), "g:isa");
    attributeValueDelegate.assignValue(
        RuleUtils.ruleActAsSubjectIdName(), "GrouperSystem");
    attributeValueDelegate.assignValue(
        RuleUtils.ruleCheckOwnerNameName(), "stem:b");
    attributeValueDelegate.assignValue(
        RuleUtils.ruleCheckTypeName(), RuleCheckType.membershipRemove.name());
    attributeValueDelegate.assignValue(
        RuleUtils.ruleIfConditionEnumName(), 
        RuleIfConditionEnum.thisGroupHasImmediateEnabledMembership.name());
    attributeValueDelegate.assignValue(
        RuleUtils.ruleThenEnumName(), RuleThenEnum.assignMembershipDisabledDaysForOwnerGroupId.name());
    attributeValueDelegate.assignValue(
        RuleUtils.ruleThenEnumArg0Name(), "7");
    attributeValueDelegate.assignValue(
        RuleUtils.ruleThenEnumArg1Name(), "F");
    
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
      .getAttributeDelegate().addAttribute(RuleUtils.ruleAttributeDefName()).getAttributeAssign();
    
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
  public void testRuleLonghandStemScopeOneCreateGroupEl() {
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Stem stem2 = new StemSave(grouperSession).assignName("stem2").assignCreateParentStemsIfNotExist(true).save();
  
    Group groupA = new GroupSave(grouperSession).assignName("stem1:a").assignCreateParentStemsIfNotExist(true).save();
  
    groupA.addMember(SubjectTestHelper.SUBJ0);
    
    
    //add a rule on stem2 saying if you create a group underneath, then assign a reader group
    AttributeAssign attributeAssign = stem2
      .getAttributeDelegate().addAttribute(RuleUtils.ruleAttributeDefName()).getAttributeAssign();
    
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleActAsSubjectSourceIdName(), "g:isa");
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleActAsSubjectIdName(), "GrouperSystem");
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
  
    Group groupA = new GroupSave(grouperSession).assignName("stem1:admins").assignCreateParentStemsIfNotExist(true).save();
  
    groupA.addMember(SubjectTestHelper.SUBJ0);
    
    
    //add a rule on stem2 saying if you create a group underneath, then assign a reader group
    AttributeAssign attributeAssign = stem2
      .getAttributeDelegate().addAttribute(RuleUtils.ruleAttributeDefName()).getAttributeAssign();
    
    AttributeValueDelegate attributeValueDelegate = attributeAssign.getAttributeValueDelegate();

    attributeValueDelegate.assignValue(
        RuleUtils.ruleActAsSubjectSourceIdName(), "g:isa");
    attributeValueDelegate.assignValue(
        RuleUtils.ruleActAsSubjectIdName(), "GrouperSystem");
    attributeValueDelegate.assignValue(
        RuleUtils.ruleCheckTypeName(), RuleCheckType.stemCreate.name());
    
    //can be SUB or ONE for if should be in all descendants or just on children
    attributeValueDelegate.assignValue(
        RuleUtils.ruleCheckStemScopeName(), Stem.Scope.SUB.name());
    attributeValueDelegate.assignValue(
        RuleUtils.ruleThenEnumName(), RuleThenEnum.assignStemPrivilegeToStemId.name());
    
    //this is the subject string for the subject to assign to
    //e.g. sourceId :::::: subjectIdentifier
    //or sourceId :::: subjectId
    //or :::: subjectId
    //or sourceId ::::::: subjectIdOrIdentifier
    //etc
    attributeValueDelegate.assignValue(
        RuleUtils.ruleThenEnumArg0Name(), "g:gsa :::::: stem1:admins");
    
    //possible privileges are stem and create
    attributeValueDelegate.assignValue(
        RuleUtils.ruleThenEnumArg1Name(), "stem, create");

    //should be valid
    String isValidString = attributeValueDelegate.retrieveValueString(
        RuleUtils.ruleValidName());

    if (!StringUtils.equals("T", isValidString)) {
      throw new RuntimeException(isValidString);
    }

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
      .getAttributeDelegate().addAttribute(RuleUtils.ruleAttributeDefName()).getAttributeAssign();
    
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleActAsSubjectSourceIdName(), "g:isa");
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleActAsSubjectIdName(), "GrouperSystem");
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleCheckTypeName(), 
        RuleCheckType.stemCreate.name());
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleCheckStemScopeName(),
        Stem.Scope.ONE.name());
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleThenEnumName(), 
        RuleThenEnum.assignStemPrivilegeToStemId.name());
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleThenEnumArg0Name(), 
        "g:gsa :::::: stem1:a");
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleThenEnumArg1Name(), 
        "stem, create");
    
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
      .getAttributeDelegate().addAttribute(RuleUtils.ruleAttributeDefName()).getAttributeAssign();
    
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
      .getAttributeDelegate().addAttribute(RuleUtils.ruleAttributeDefName()).getAttributeAssign();
    
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

  /**
   * 
   */
  public void testRuleLonghandStemScopeSubCreateGroupAsGrouperSystem() {
    
    ApiConfig.testConfig.put("rules.allowActAsGrouperSystemForInheritedStemPrivileges", "true");

    GrouperSession grouperSession = GrouperSession.startRootSession();
    Stem stem2 = new StemSave(grouperSession).assignName("stem2").assignCreateParentStemsIfNotExist(true).save();
  
    Group groupA = new GroupSave(grouperSession).assignName("stem1:a").assignCreateParentStemsIfNotExist(true).save();
  
    groupA.addMember(SubjectTestHelper.SUBJ0);
    
    //assign privs to a subject so we can act as that subject
    stem2.grantPriv(SubjectTestHelper.SUBJ9, NamingPrivilege.CREATE, false);
    stem2.grantPriv(SubjectTestHelper.SUBJ9, NamingPrivilege.STEM, false);
    
    RuleUtils.ruleTypeAttributeDef().getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ9, AttributeDefPrivilege.ATTR_UPDATE, false);
    RuleUtils.ruleAttrAttributeDef().getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ9, AttributeDefPrivilege.ATTR_UPDATE, false);
    RuleUtils.ruleTypeAttributeDef().getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ9, AttributeDefPrivilege.ATTR_READ, false);
    RuleUtils.ruleAttrAttributeDef().getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ9, AttributeDefPrivilege.ATTR_READ, false);

    groupA.grantPriv(SubjectTestHelper.SUBJ9, AccessPrivilege.READ, false);

    Stem rootStem = StemFinder.findRootStem(grouperSession);

    rootStem.grantPriv(SubjectTestHelper.SUBJ9, NamingPrivilege.CREATE, false);
    rootStem.grantPriv(SubjectTestHelper.SUBJ9, NamingPrivilege.STEM, false);

    GrouperSession.stopQuietly(grouperSession);
    
    grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ9);
    
    //add a rule on stem2 saying if you create a group underneath, then assign a reader group
    AttributeAssign attributeAssign = stem2
      .getAttributeDelegate().addAttribute(RuleUtils.ruleAttributeDefName()).getAttributeAssign();
    
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleActAsSubjectSourceIdName(), "g:isa");
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleActAsSubjectIdName(), "GrouperSystem");
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleCheckTypeName(), 
        RuleCheckType.groupCreate.name());
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleCheckStemScopeName(),
        Stem.Scope.SUB.name());
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleThenEnumName(), 
        RuleThenEnum.assignGroupPrivilegeToGroupId.name());
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleThenEnumArg0Name(), 
        "g:gsa :::::: stem1:a");
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleThenEnumArg1Name(), 
        "read, update");
    
    String isValidString = attributeAssign.getAttributeValueDelegate().retrieveValueString(
        RuleUtils.ruleValidName());
    assertEquals("T", isValidString);

    long initialFirings = RuleEngine.ruleFirings;
    
//  # If the CHECK, IF, and THEN are all exactly what is needed for managing inherited stem privileges
//  # Then allow an actAs GrouperSystem in source g:isa
//  rules.allowActAsGrouperSystemForInheritedStemPrivileges = true
    
    Group groupB = new GroupSave(grouperSession).assignName("stem2:b").assignCreateParentStemsIfNotExist(true).save();
  
    //count rule firings
    assertEquals(initialFirings+1, RuleEngine.ruleFirings);
  
    //make sure allowed
    assertTrue(groupB.hasUpdate(SubjectTestHelper.SUBJ0));
    assertTrue(groupB.hasRead(SubjectTestHelper.SUBJ0));
    
    Stem stem3 = new StemSave(grouperSession).assignName("stem3").save();
    stem3.grantPriv(SubjectTestHelper.SUBJ9, NamingPrivilege.CREATE, false);
    stem3.grantPriv(SubjectTestHelper.SUBJ9, NamingPrivilege.STEM, false);
    Group groupD = new GroupSave(grouperSession).assignName("stem3:b").assignCreateParentStemsIfNotExist(true).save();
  
    assertEquals(initialFirings+1, RuleEngine.ruleFirings);
    
    assertFalse(groupD.hasUpdate(SubjectTestHelper.SUBJ0));
    assertFalse(groupD.hasRead(SubjectTestHelper.SUBJ0));
    
    Stem stem2sub = new StemSave(grouperSession).assignName("stem2:sub").save();
    stem2sub.grantPriv(SubjectTestHelper.SUBJ9, NamingPrivilege.CREATE, false);
    stem2sub.grantPriv(SubjectTestHelper.SUBJ9, NamingPrivilege.STEM, false);
    
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
   * 
   */
  public void testRuleLonghandStemScopeSubCreateStemAsGrouperSystem() {
    
    ApiConfig.testConfig.put("rules.allowActAsGrouperSystemForInheritedStemPrivileges", "true");

    GrouperSession grouperSession = GrouperSession.startRootSession();
    Stem stem2 = new StemSave(grouperSession).assignName("stem2").assignCreateParentStemsIfNotExist(true).save();
  
    Group groupA = new GroupSave(grouperSession).assignName("stem1:a").assignCreateParentStemsIfNotExist(true).save();
  
    groupA.addMember(SubjectTestHelper.SUBJ0);
    
    //assign privs to a subject so we can act as that subject
    stem2.grantPriv(SubjectTestHelper.SUBJ9, NamingPrivilege.CREATE, false);
    stem2.grantPriv(SubjectTestHelper.SUBJ9, NamingPrivilege.STEM, false);
    
    RuleUtils.ruleTypeAttributeDef().getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ9, AttributeDefPrivilege.ATTR_UPDATE, false);
    RuleUtils.ruleAttrAttributeDef().getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ9, AttributeDefPrivilege.ATTR_UPDATE, false);
    RuleUtils.ruleTypeAttributeDef().getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ9, AttributeDefPrivilege.ATTR_READ, false);
    RuleUtils.ruleAttrAttributeDef().getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ9, AttributeDefPrivilege.ATTR_READ, false);

    groupA.grantPriv(SubjectTestHelper.SUBJ9, AccessPrivilege.READ, false);

    Stem rootStem = StemFinder.findRootStem(grouperSession);

    rootStem.grantPriv(SubjectTestHelper.SUBJ9, NamingPrivilege.CREATE, false);
    rootStem.grantPriv(SubjectTestHelper.SUBJ9, NamingPrivilege.STEM, false);

    GrouperSession.stopQuietly(grouperSession);
    
    grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ9);
    
    //add a rule on stem2 saying if you create a group underneath, then assign a reader group
    AttributeAssign attributeAssign = stem2
      .getAttributeDelegate().addAttribute(RuleUtils.ruleAttributeDefName()).getAttributeAssign();
    
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleActAsSubjectSourceIdName(), "g:isa");
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleActAsSubjectIdName(), "GrouperSystem");
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleCheckTypeName(), 
        RuleCheckType.stemCreate.name());
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleCheckStemScopeName(),
        Stem.Scope.SUB.name());
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleThenEnumName(), 
        RuleThenEnum.assignStemPrivilegeToStemId.name());
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleThenEnumArg0Name(), 
        "g:gsa :::::: stem1:a");
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleThenEnumArg1Name(), 
        "stem, create");
    
    String isValidString = attributeAssign.getAttributeValueDelegate().retrieveValueString(
        RuleUtils.ruleValidName());
    assertEquals("T", isValidString);

    long initialFirings = RuleEngine.ruleFirings;
    
//  # If the CHECK, IF, and THEN are all exactly what is needed for managing inherited stem privileges
//  # Then allow an actAs GrouperSystem in source g:isa
//  rules.allowActAsGrouperSystemForInheritedStemPrivileges = true
    
    Stem stemB = new StemSave(grouperSession).assignName("stem2:b").assignCreateParentStemsIfNotExist(true).save();
  
    //count rule firings
    assertEquals(initialFirings+1, RuleEngine.ruleFirings);
  
    //make sure allowed
    assertTrue(stemB.hasStem(SubjectTestHelper.SUBJ0));
    assertTrue(stemB.hasCreate(SubjectTestHelper.SUBJ0));
    
    Stem stem3 = new StemSave(grouperSession).assignName("stem3").save();
    stem3.grantPriv(SubjectTestHelper.SUBJ9, NamingPrivilege.CREATE, false);
    stem3.grantPriv(SubjectTestHelper.SUBJ9, NamingPrivilege.STEM, false);
    Stem stemD = new StemSave(grouperSession).assignName("stem3:d").assignCreateParentStemsIfNotExist(true).save();
  
    assertEquals(initialFirings+1, RuleEngine.ruleFirings);
    
    assertFalse(stemD.hasCreate(SubjectTestHelper.SUBJ0));
    assertFalse(stemD.hasStem(SubjectTestHelper.SUBJ0));
    
    Stem stem2sub = new StemSave(grouperSession).assignName("stem2:sub").save();
    stem2sub.grantPriv(SubjectTestHelper.SUBJ9, NamingPrivilege.CREATE, false);
    stem2sub.grantPriv(SubjectTestHelper.SUBJ9, NamingPrivilege.STEM, false);
    
    Stem stemC = new StemSave(grouperSession).assignName("stem2:sub:c").assignCreateParentStemsIfNotExist(true).save();
  
    //fires for both stems
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
  public void testRuleLonghandStemScopeSubCreateAttributeDef() {
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Stem stem2 = new StemSave(grouperSession).assignName("stem2").assignCreateParentStemsIfNotExist(true).save();
  
    Group groupA = new GroupSave(grouperSession).assignName("stem1:admins").assignCreateParentStemsIfNotExist(true).save();
  
    groupA.addMember(SubjectTestHelper.SUBJ0);
    
    
    //add a rule on stem2 saying if you create a group underneath, then assign a reader group
    AttributeAssign attributeAssign = stem2
      .getAttributeDelegate().addAttribute(RuleUtils.ruleAttributeDefName()).getAttributeAssign();
    
    AttributeValueDelegate attributeValueDelegate = attributeAssign.getAttributeValueDelegate();
    
    attributeValueDelegate.assignValue(
        RuleUtils.ruleActAsSubjectSourceIdName(), "g:isa");
    attributeValueDelegate.assignValue(
        RuleUtils.ruleActAsSubjectIdName(), "GrouperSystem");
    attributeValueDelegate.assignValue(
        RuleUtils.ruleCheckTypeName(), RuleCheckType.attributeDefCreate.name());

    //can be SUB or ONE for if in this folder, or in this and all subfolders
    attributeValueDelegate.assignValue(
        RuleUtils.ruleCheckStemScopeName(), Stem.Scope.SUB.name());
    attributeValueDelegate.assignValue(
        RuleUtils.ruleThenEnumName(), RuleThenEnum.assignAttributeDefPrivilegeToAttributeDefId.name());

    //this is the subject string for the subject to assign to
    //e.g. sourceId :::::: subjectIdentifier
    //or sourceId :::: subjectId
    //or :::: subjectId
    //or sourceId ::::::: subjectIdOrIdentifier
    //etc
    attributeValueDelegate.assignValue(
        RuleUtils.ruleThenEnumArg0Name(), "g:gsa :::::: stem1:admins");

    //can be: attrRead, attrUpdate, attrView, attrAdmin, attrOptin, attrOptout
    attributeValueDelegate.assignValue(
        RuleUtils.ruleThenEnumArg1Name(), "attrRead,attrUpdate");

    //should be valid
    String isValidString = attributeValueDelegate.retrieveValueString(
        RuleUtils.ruleValidName());

    if (!StringUtils.equals("T", isValidString)) {
      throw new RuntimeException(isValidString);
    }

    long initialFirings = RuleEngine.ruleFirings;
     
    
    AttributeDef attributeDefB = new AttributeDefSave(grouperSession).assignName("stem2:b").assignCreateParentStemsIfNotExist(true).save();
  
    //count rule firings
    assertEquals(initialFirings+1, RuleEngine.ruleFirings);
  
    //make sure allowed
    assertTrue(attributeDefB.getPrivilegeDelegate().hasAttrUpdate(SubjectTestHelper.SUBJ0));
    assertTrue(attributeDefB.getPrivilegeDelegate().hasAttrRead(SubjectTestHelper.SUBJ0));
    
    
    AttributeDef attributeDefD = new AttributeDefSave(grouperSession).assignName("stem3:b").assignCreateParentStemsIfNotExist(true).save();
  
    assertEquals(initialFirings+1, RuleEngine.ruleFirings);
    
    assertFalse(attributeDefD.getPrivilegeDelegate().hasAttrUpdate(SubjectTestHelper.SUBJ0));
    assertFalse(attributeDefD.getPrivilegeDelegate().hasAttrRead(SubjectTestHelper.SUBJ0));
    
    
    AttributeDef attributeDefC = new AttributeDefSave(grouperSession).assignName("stem2:sub:c").assignCreateParentStemsIfNotExist(true).save();
  
    assertEquals(initialFirings+2, RuleEngine.ruleFirings);
  
    assertTrue(attributeDefC.getPrivilegeDelegate().hasAttrRead(SubjectTestHelper.SUBJ0));
    assertTrue(attributeDefC.getPrivilegeDelegate().hasAttrUpdate(SubjectTestHelper.SUBJ0));
  
  
    // GrouperSession.startRootSession();
    // addMember("stem:a", "test.subject.0");
    // addMember("stem:b", "test.subject.0");
    // delMember("stem:b", "test.subject.0");
    // hasMember("stem:a", "test.subject.0");
    
  }

  /**
     * 
     */
    public void testRuleLonghandStemScopeSubCreateAttributeDefAsGrouperSystem() {
      
      ApiConfig.testConfig.put("rules.allowActAsGrouperSystemForInheritedStemPrivileges", "true");
  
      GrouperSession grouperSession = GrouperSession.startRootSession();
      Stem stem2 = new StemSave(grouperSession).assignName("stem2").assignCreateParentStemsIfNotExist(true).save();
    
      Group groupA = new GroupSave(grouperSession).assignName("stem1:a").assignCreateParentStemsIfNotExist(true).save();
    
      groupA.addMember(SubjectTestHelper.SUBJ0);
      
      //assign privs to a subject so we can act as that subject
      stem2.grantPriv(SubjectTestHelper.SUBJ9, NamingPrivilege.CREATE, false);
      stem2.grantPriv(SubjectTestHelper.SUBJ9, NamingPrivilege.STEM, false);
      
      RuleUtils.ruleTypeAttributeDef().getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ9, AttributeDefPrivilege.ATTR_UPDATE, false);
      RuleUtils.ruleAttrAttributeDef().getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ9, AttributeDefPrivilege.ATTR_UPDATE, false);
      RuleUtils.ruleTypeAttributeDef().getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ9, AttributeDefPrivilege.ATTR_READ, false);
      RuleUtils.ruleAttrAttributeDef().getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ9, AttributeDefPrivilege.ATTR_READ, false);
  
      groupA.grantPriv(SubjectTestHelper.SUBJ9, AccessPrivilege.READ, false);
  
      Stem rootStem = StemFinder.findRootStem(grouperSession);
  
      rootStem.grantPriv(SubjectTestHelper.SUBJ9, NamingPrivilege.CREATE, false);
      rootStem.grantPriv(SubjectTestHelper.SUBJ9, NamingPrivilege.STEM, false);
  
      GrouperSession.stopQuietly(grouperSession);
      
      grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ9);
      
      //add a rule on stem2 saying if you create a group underneath, then assign a reader group
      AttributeAssign attributeAssign = stem2
        .getAttributeDelegate().addAttribute(RuleUtils.ruleAttributeDefName()).getAttributeAssign();
      
      attributeAssign.getAttributeValueDelegate().assignValue(
          RuleUtils.ruleActAsSubjectSourceIdName(), "g:isa");
      attributeAssign.getAttributeValueDelegate().assignValue(
          RuleUtils.ruleActAsSubjectIdName(), "GrouperSystem");
      attributeAssign.getAttributeValueDelegate().assignValue(
          RuleUtils.ruleCheckTypeName(), 
          RuleCheckType.attributeDefCreate.name());
      attributeAssign.getAttributeValueDelegate().assignValue(
          RuleUtils.ruleCheckStemScopeName(),
          Stem.Scope.SUB.name());
      attributeAssign.getAttributeValueDelegate().assignValue(
          RuleUtils.ruleThenEnumName(), 
          RuleThenEnum.assignAttributeDefPrivilegeToAttributeDefId.name());
      attributeAssign.getAttributeValueDelegate().assignValue(
          RuleUtils.ruleThenEnumArg0Name(), 
          "g:gsa :::::: stem1:a");
      attributeAssign.getAttributeValueDelegate().assignValue(
          RuleUtils.ruleThenEnumArg1Name(), 
          "attrRead,attrUpdate");

      String isValidString = attributeAssign.getAttributeValueDelegate().retrieveValueString(
          RuleUtils.ruleValidName());
      assertEquals("T", isValidString);
  
      long initialFirings = RuleEngine.ruleFirings;
      
  //  # If the CHECK, IF, and THEN are all exactly what is needed for managing inherited stem privileges
  //  # Then allow an actAs GrouperSystem in source g:isa
  //  rules.allowActAsGrouperSystemForInheritedStemPrivileges = true
      
      AttributeDef attributeDefB = new AttributeDefSave(grouperSession).assignName("stem2:b").assignCreateParentStemsIfNotExist(true).save();
    
      //count rule firings
      assertEquals(initialFirings+1, RuleEngine.ruleFirings);
    
      //make sure allowed
      assertTrue(attributeDefB.getPrivilegeDelegate().hasAttrUpdate(SubjectTestHelper.SUBJ0));
      assertTrue(attributeDefB.getPrivilegeDelegate().hasAttrRead(SubjectTestHelper.SUBJ0));
      
      Stem stem3 = new StemSave(grouperSession).assignName("stem3").save();
      stem3.grantPriv(SubjectTestHelper.SUBJ9, NamingPrivilege.CREATE, false);
      stem3.grantPriv(SubjectTestHelper.SUBJ9, NamingPrivilege.STEM, false);
      AttributeDef attributeDefD = new AttributeDefSave(grouperSession).assignName("stem3:d").assignCreateParentStemsIfNotExist(true).save();
    
      assertEquals(initialFirings+1, RuleEngine.ruleFirings);
      
      assertFalse(attributeDefD.getPrivilegeDelegate().hasAttrUpdate(SubjectTestHelper.SUBJ0));
      assertFalse(attributeDefD.getPrivilegeDelegate().hasAttrRead(SubjectTestHelper.SUBJ0));
      
      Stem stem2sub = new StemSave(grouperSession).assignName("stem2:sub").save();
      stem2sub.grantPriv(SubjectTestHelper.SUBJ9, NamingPrivilege.CREATE, false);
      stem2sub.grantPriv(SubjectTestHelper.SUBJ9, NamingPrivilege.STEM, false);
      
      AttributeDef attributeDefC = new AttributeDefSave(grouperSession).assignName("stem2:sub:c").assignCreateParentStemsIfNotExist(true).save();
    
      assertEquals(initialFirings+2, RuleEngine.ruleFirings);
    
      assertTrue(attributeDefC.getPrivilegeDelegate().hasAttrRead(SubjectTestHelper.SUBJ0));
      assertTrue(attributeDefC.getPrivilegeDelegate().hasAttrUpdate(SubjectTestHelper.SUBJ0));
    
    
      // GrouperSession.startRootSession();
      // addMember("stem:a", "test.subject.0");
      // addMember("stem:b", "test.subject.0");
      // delMember("stem:b", "test.subject.0");
      // hasMember("stem:a", "test.subject.0");
      
    }

  /**
   * 
   */
  public void testRuleLonghandStemScopeSubCreateGroupDaemon() {
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Stem stem2 = new StemSave(grouperSession).assignName("stem2").assignCreateParentStemsIfNotExist(true).save();
  
    Group groupA = new GroupSave(grouperSession).assignName("stem1:a").assignCreateParentStemsIfNotExist(true).save();
  
    groupA.addMember(SubjectTestHelper.SUBJ0);
    
    Group groupB = new GroupSave(grouperSession).assignName("stem2:b").assignCreateParentStemsIfNotExist(true).save();
    Group groupD = new GroupSave(grouperSession).assignName("stem3:d").assignCreateParentStemsIfNotExist(true).save();
    Group groupC = new GroupSave(grouperSession).assignName("stem2:sub:c").assignCreateParentStemsIfNotExist(true).save();
    
    //add a rule on stem2 saying if you create a group underneath, then assign a reader group
    AttributeAssign attributeAssign = stem2
      .getAttributeDelegate().addAttribute(RuleUtils.ruleAttributeDefName()).getAttributeAssign();
    
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleActAsSubjectSourceIdName(), "g:isa");
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleActAsSubjectIdName(), "GrouperSystem");
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleCheckTypeName(), 
        RuleCheckType.groupCreate.name());
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleCheckStemScopeName(),
        Stem.Scope.SUB.name());
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleThenEnumName(), 
        RuleThenEnum.assignGroupPrivilegeToGroupId.name());
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleThenEnumArg0Name(), 
        "g:gsa :::::: stem1:a");
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleThenEnumArg1Name(), 
        "read, update");

    assertFalse(groupB.hasUpdate(SubjectTestHelper.SUBJ0));
    assertFalse(groupB.hasRead(SubjectTestHelper.SUBJ0));
    
    assertFalse(groupD.hasUpdate(SubjectTestHelper.SUBJ0));
    assertFalse(groupD.hasRead(SubjectTestHelper.SUBJ0));
    
    assertFalse(groupC.hasRead(SubjectTestHelper.SUBJ0));
    assertFalse(groupC.hasUpdate(SubjectTestHelper.SUBJ0));

    //run the daemon job
    int ruleCount = RuleApi.runRulesForOwner(stem2);

    assertEquals(1, ruleCount);

    //this happens in a different session and doesnt get flushed
    grouperSession.getAccessResolver().flushCache();
    
    //make sure allowed
    assertTrue(groupB.hasUpdate(SubjectTestHelper.SUBJ0));
    assertTrue(groupB.hasRead(SubjectTestHelper.SUBJ0));
    
    assertFalse(groupD.hasUpdate(SubjectTestHelper.SUBJ0));
    assertFalse(groupD.hasRead(SubjectTestHelper.SUBJ0));
    
    assertTrue(groupC.hasRead(SubjectTestHelper.SUBJ0));
    assertTrue(groupC.hasUpdate(SubjectTestHelper.SUBJ0));
    
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
      .getAttributeDelegate().addAttribute(RuleUtils.ruleAttributeDefName()).getAttributeAssign();
    
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleActAsSubjectSourceIdName(), "g:isa");
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleActAsSubjectIdName(), "GrouperSystem");
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleCheckTypeName(), 
        RuleCheckType.groupCreate.name());
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleCheckStemScopeName(),
        Stem.Scope.ONE.name());
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleThenEnumName(), 
        RuleThenEnum.assignGroupPrivilegeToGroupId.name());
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleThenEnumArg0Name(), 
        "g:gsa :::::: stem1:a");
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleThenEnumArg1Name(), 
        "read, update");
    
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
  public void testRuleLonghandStemScopeSubCreateStemDaemon() {
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Stem stem2 = new StemSave(grouperSession).assignName("stem2").assignCreateParentStemsIfNotExist(true).save();
  
    Group groupA = new GroupSave(grouperSession).assignName("stem1:a").assignCreateParentStemsIfNotExist(true).save();
  
    groupA.addMember(SubjectTestHelper.SUBJ0);
    
    Stem stemB = new StemSave(grouperSession).assignName("stem2:b").assignCreateParentStemsIfNotExist(true).save();
    Stem stemD = new StemSave(grouperSession).assignName("stem3:d").assignCreateParentStemsIfNotExist(true).save();
    Stem stemC = new StemSave(grouperSession).assignName("stem2:sub:c").assignCreateParentStemsIfNotExist(true).save();
    
    //add a rule on stem2 saying if you create a group underneath, then assign a reader group
    AttributeAssign attributeAssign = stem2
      .getAttributeDelegate().addAttribute(RuleUtils.ruleAttributeDefName()).getAttributeAssign();
    
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleActAsSubjectSourceIdName(), "g:isa");
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleActAsSubjectIdName(), "GrouperSystem");
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleCheckTypeName(), 
        RuleCheckType.stemCreate.name());
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleCheckStemScopeName(),
        Stem.Scope.SUB.name());
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleThenEnumName(), 
        RuleThenEnum.assignStemPrivilegeToStemId.name());
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleThenEnumArg0Name(), 
        "g:gsa :::::: stem1:a");
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleThenEnumArg1Name(), 
        "stem, create");
  
    assertFalse(stemB.hasCreate(SubjectTestHelper.SUBJ0));
    assertFalse(stemB.hasStem(SubjectTestHelper.SUBJ0));
    
    assertFalse(stemD.hasCreate(SubjectTestHelper.SUBJ0));
    assertFalse(stemD.hasStem(SubjectTestHelper.SUBJ0));
    
    assertFalse(stemC.hasCreate(SubjectTestHelper.SUBJ0));
    assertFalse(stemC.hasStem(SubjectTestHelper.SUBJ0));
  
    //run the daemon job
    int ruleCount = RuleApi.runRulesForOwner(stem2);
  
    assertEquals(1, ruleCount);
  
    //this happens in a different session and doesnt get flushed
    grouperSession.getNamingResolver().flushCache();
    
    //make sure allowed
    assertTrue(stemB.hasCreate(SubjectTestHelper.SUBJ0));
    assertTrue(stemB.hasStem(SubjectTestHelper.SUBJ0));
    
    assertFalse(stemD.hasCreate(SubjectTestHelper.SUBJ0));
    assertFalse(stemD.hasStem(SubjectTestHelper.SUBJ0));
    
    assertTrue(stemC.hasCreate(SubjectTestHelper.SUBJ0));
    assertTrue(stemC.hasStem(SubjectTestHelper.SUBJ0));
    
  }

  /**
   * 
   */
  public void testRuleLonghandStemScopeSubCreateAttributeDefDaemon() {
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Stem stem2 = new StemSave(grouperSession).assignName("stem2").assignCreateParentStemsIfNotExist(true).save();
  
    Group groupA = new GroupSave(grouperSession).assignName("stem1:a").assignCreateParentStemsIfNotExist(true).save();
  
    groupA.addMember(SubjectTestHelper.SUBJ0);
    
    AttributeDef attributeDefB = new AttributeDefSave(grouperSession).assignName("stem2:b").assignCreateParentStemsIfNotExist(true).save();
    AttributeDef attributeDefD = new AttributeDefSave(grouperSession).assignName("stem3:d").assignCreateParentStemsIfNotExist(true).save();
    AttributeDef attributeDefC = new AttributeDefSave(grouperSession).assignName("stem2:sub:c").assignCreateParentStemsIfNotExist(true).save();
    
    //add a rule on stem2 saying if you create a group underneath, then assign a reader group
    AttributeAssign attributeAssign = stem2
      .getAttributeDelegate().addAttribute(RuleUtils.ruleAttributeDefName()).getAttributeAssign();
    
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleActAsSubjectSourceIdName(), "g:isa");
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleActAsSubjectIdName(), "GrouperSystem");
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleCheckTypeName(), 
        RuleCheckType.attributeDefCreate.name());
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleCheckStemScopeName(),
        Stem.Scope.SUB.name());
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleThenEnumName(), 
        RuleThenEnum.assignAttributeDefPrivilegeToAttributeDefId.name());
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleThenEnumArg0Name(), 
        "g:gsa :::::: stem1:a");
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleThenEnumArg1Name(), 
        "attrRead, attrUpdate");
  
    assertFalse(attributeDefB.getPrivilegeDelegate().hasAttrUpdate(SubjectTestHelper.SUBJ0));
    assertFalse(attributeDefB.getPrivilegeDelegate().hasAttrRead(SubjectTestHelper.SUBJ0));
    
    assertFalse(attributeDefD.getPrivilegeDelegate().hasAttrUpdate(SubjectTestHelper.SUBJ0));
    assertFalse(attributeDefD.getPrivilegeDelegate().hasAttrRead(SubjectTestHelper.SUBJ0));
    
    assertFalse(attributeDefC.getPrivilegeDelegate().hasAttrRead(SubjectTestHelper.SUBJ0));
    assertFalse(attributeDefC.getPrivilegeDelegate().hasAttrUpdate(SubjectTestHelper.SUBJ0));
  
    //run the daemon job
    int ruleCount = RuleApi.runRulesForOwner(stem2);
  
    assertEquals(1, ruleCount);
  
    //this happens in a different session and doesnt get flushed
    grouperSession.getAttributeDefResolver().flushCache();
    
    //make sure allowed
    assertTrue(attributeDefB.getPrivilegeDelegate().hasAttrUpdate(SubjectTestHelper.SUBJ0));
    assertTrue(attributeDefB.getPrivilegeDelegate().hasAttrRead(SubjectTestHelper.SUBJ0));
    
    assertFalse(attributeDefD.getPrivilegeDelegate().hasAttrUpdate(SubjectTestHelper.SUBJ0));
    assertFalse(attributeDefD.getPrivilegeDelegate().hasAttrRead(SubjectTestHelper.SUBJ0));
    
    assertTrue(attributeDefC.getPrivilegeDelegate().hasAttrRead(SubjectTestHelper.SUBJ0));
    assertTrue(attributeDefC.getPrivilegeDelegate().hasAttrUpdate(SubjectTestHelper.SUBJ0));
    
  }

  /**
   * 
   */
  public void testRuleLonghandPrintAll() {
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Group groupA = new GroupSave(grouperSession).assignName("stem:a").assignCreateParentStemsIfNotExist(true).save();
    new GroupSave(grouperSession).assignName("stem:b").assignCreateParentStemsIfNotExist(true).save();
    new GroupSave(grouperSession).assignName("stem:c").assignCreateParentStemsIfNotExist(true).save();
    
    //add a rule on stem:a saying if you are out of stem:b, then remove from stem:a
    AttributeAssign attributeAssign = groupA
      .getAttributeDelegate().addAttribute(RuleUtils.ruleAttributeDefName()).getAttributeAssign();
    
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
  
    System.out.println(RuleApi.rulesToString());
    
  }

  /**
   * 
   */
  public void testRuleLonghandEmail() {
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Group groupA = new GroupSave(grouperSession).assignName("stem:a").assignCreateParentStemsIfNotExist(true).save();
    Group groupB = new GroupSave(grouperSession).assignName("stem:b").assignCreateParentStemsIfNotExist(true).save();
    
    //add a rule on stem:a saying if you are out of stem:b, then remove from stem:a
    AttributeAssign attributeAssign = groupA
      .getAttributeDelegate().addAttribute(RuleUtils.ruleAttributeDefName()).getAttributeAssign();
    
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
        RuleUtils.ruleThenEnumName(), RuleThenEnum.sendEmail.name());
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleThenEnumArg0Name(), "a@b.c, ${safeSubject.emailAddress}"); // ${subjectEmail}
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleThenEnumArg1Name(), "You will be removed from group: ${groupDisplayExtension}"); //${groupId}
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleThenEnumArg2Name(), "Hello ${safeSubject.name},\n\nJust letting you know you were removed from " +
        		"group ${groupDisplayExtension} in the central Groups management system.  Please do not respond to this email.\n\nRegards."); //emailTemplate: testEmailGroupBody
    
    //should be valid
    String isValidString = attributeAssign.getAttributeValueDelegate().retrieveValueString(
        RuleUtils.ruleValidName());
    assertEquals("T", isValidString);
    
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
  public void testRuleLonghandEmailTemplate() {
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Group groupA = new GroupSave(grouperSession).assignName("stem:a").assignCreateParentStemsIfNotExist(true).save();
    Group groupB = new GroupSave(grouperSession).assignName("stem:b").assignCreateParentStemsIfNotExist(true).save();
    
    //add a rule on stem:a saying if you are out of stem:b, then remove from stem:a
    AttributeAssign attributeAssign = groupA
      .getAttributeDelegate().addAttribute(RuleUtils.ruleAttributeDefName()).getAttributeAssign();
    
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleActAsSubjectSourceIdName(), "g:isa");
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleActAsSubjectIdName(), "GrouperSystem");
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleCheckTypeName(), 
        RuleCheckType.membershipAdd.name());
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleThenEnumName(), RuleThenEnum.sendEmail.name());
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleThenEnumArg0Name(), "a@b.c, ${safeSubject.emailAddress}"); // ${subjectEmail}
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleThenEnumArg1Name(), "template: testTemplateSubject"); //${groupId}
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleThenEnumArg2Name(), "template: testTemplateBody"); 
    
    //should be valid
    String isValidString = attributeAssign.getAttributeValueDelegate().retrieveValueString(
        RuleUtils.ruleValidName());
    assertEquals("T", isValidString);
    
    //count rule firings
    long initialFirings = RuleEngine.ruleFirings;
    
    groupB.addMember(SubjectTestHelper.SUBJ0);
  
    assertEquals(initialFirings, RuleEngine.ruleFirings);
    
    groupA.addMember(SubjectTestHelper.SUBJ0);
    
    //count rule firings
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
  public void testRuleLonghandPermissionAssignmentIntersectFolder() {
  
    GrouperSession grouperSession = GrouperSession.startRootSession();
    AttributeDef permissionDef = new AttributeDefSave(grouperSession)
      .assignName("stem:permissionDef").assignCreateParentStemsIfNotExist(true)
      .assignAttributeDefType(AttributeDefType.perm)
      .save();
    
    permissionDef.setAssignToEffMembership(true);
    permissionDef.setAssignToGroup(true);
    permissionDef.store();
    
    Group groupProgrammers = new GroupSave(grouperSession).assignName("stem:orgs:itEmployee:programmers").assignCreateParentStemsIfNotExist(true).save();
    Group groupSysadmins = new GroupSave(grouperSession).assignName("stem:orgs:itEmployee:sysadmins").assignCreateParentStemsIfNotExist(true).save();
  
    //make a role
    Role payrollUser = new GroupSave(grouperSession).assignName("apps:payroll:roles:payrollUser").assignTypeOfGroup(TypeOfGroup.role).assignCreateParentStemsIfNotExist(true).save();
    Role payrollGuest = new GroupSave(grouperSession).assignName("apps:payroll:roles:payrollGuest").assignTypeOfGroup(TypeOfGroup.role).assignCreateParentStemsIfNotExist(true).save();
  
    //assign a user to a role
    payrollUser.addMember(SubjectTestHelper.SUBJ0, false);
    payrollGuest.addMember(SubjectTestHelper.SUBJ1, false);
    
    //create a permission, assign to role
    AttributeDefName canLogin = new AttributeDefNameSave(grouperSession, permissionDef).assignName("apps:payroll:permissions:canLogin").assignCreateParentStemsIfNotExist(true).save();
    
    payrollUser.getPermissionRoleDelegate().assignRolePermission(canLogin);
    
    //assign the permission to another user directly, not due to a role
    payrollGuest.getPermissionRoleDelegate().assignSubjectRolePermission(canLogin, SubjectTestHelper.SUBJ1);
    
    //see that they both have the permission
    Member member0 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ0, false);
    Member member1 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ1, false);
    
    Set<PermissionEntry> permissions = GrouperDAOFactory.getFactory().getPermissionEntry().findByMemberId(member0.getUuid());
    assertEquals(1, permissions.size());
    assertEquals("apps:payroll:permissions:canLogin", permissions.iterator().next().getAttributeDefNameName());
    
    permissions = GrouperDAOFactory.getFactory().getPermissionEntry().findByMemberId(member1.getUuid());
    assertEquals(1, permissions.size());
    assertEquals("apps:payroll:permissions:canLogin", permissions.iterator().next().getAttributeDefNameName());
    
    //add a rule on stem:permission saying if you are out of stem:employee, 
    //then remove assignments to permission, or from roles which have the permission
    AttributeAssign attributeAssign = permissionDef
      .getAttributeDelegate().addAttribute(RuleUtils.ruleAttributeDefName()).getAttributeAssign();

    
    AttributeValueDelegate attributeValueDelegate = attributeAssign.getAttributeValueDelegate();

    attributeValueDelegate.assignValue(
        RuleUtils.ruleActAsSubjectSourceIdName(), "g:isa");
    attributeValueDelegate.assignValue(
        RuleUtils.ruleActAsSubjectIdName(), "GrouperSystem");

    //folder where membership was removed
    attributeValueDelegate.assignValue(
        RuleUtils.ruleCheckOwnerNameName(), "stem:orgs:itEmployee");
    attributeValueDelegate.assignValue(
        RuleUtils.ruleCheckTypeName(), 
        RuleCheckType.membershipRemoveInFolder.name());

    //SUB for all descendants, ONE for just children
    attributeValueDelegate.assignValue(
        RuleUtils.ruleCheckStemScopeName(),
        Stem.Scope.SUB.name());
    
    //if there is no more membership in the folder, and there is a membership in the group
    attributeValueDelegate.assignValue(
        RuleUtils.ruleIfConditionEnumName(), 
        RuleIfConditionEnum.thisPermissionDefHasAssignmentAndNotFolder.name());
    attributeValueDelegate.assignValue(
        RuleUtils.ruleThenEnumName(), 
        RuleThenEnum.removeMemberFromOwnerPermissionDefAssignments.name());
  
    //should be valid
    String isValidString = attributeValueDelegate.retrieveValueString(
        RuleUtils.ruleValidName());
  
    if (!StringUtils.equals("T", isValidString)) {
      throw new RuntimeException(isValidString);
    }
  
    groupProgrammers.addMember(SubjectTestHelper.SUBJ0);
    groupSysadmins.addMember(SubjectTestHelper.SUBJ0);
    groupProgrammers.addMember(SubjectTestHelper.SUBJ1);
    groupSysadmins.addMember(SubjectTestHelper.SUBJ1);
    groupProgrammers.addMember(SubjectTestHelper.SUBJ2);
    groupSysadmins.addMember(SubjectTestHelper.SUBJ2);
    
    //count rule firings
    long initialFirings = RuleEngine.ruleFirings;
    
    //doesnt do anything
    groupProgrammers.deleteMember(SubjectTestHelper.SUBJ2);
    groupSysadmins.deleteMember(SubjectTestHelper.SUBJ2);
  
    assertEquals(initialFirings, RuleEngine.ruleFirings);
  
    groupProgrammers.deleteMember(SubjectTestHelper.SUBJ0);

    assertEquals(initialFirings, RuleEngine.ruleFirings);

    groupSysadmins.deleteMember(SubjectTestHelper.SUBJ0);

    //should come out of groupA
    assertEquals(initialFirings+1, RuleEngine.ruleFirings);
    
    assertFalse(payrollUser.hasMember(SubjectTestHelper.SUBJ0));
  
    permissions = GrouperDAOFactory.getFactory().getPermissionEntry().findByMemberId(member0.getUuid());
    assertEquals(0, permissions.size());
    
    permissions = GrouperDAOFactory.getFactory().getPermissionEntry().findByMemberId(member1.getUuid());
    assertEquals(1, permissions.size());
    assertEquals("apps:payroll:permissions:canLogin", permissions.iterator().next().getAttributeDefNameName());
    
    //take out second user
    groupSysadmins.deleteMember(SubjectTestHelper.SUBJ1);

    assertEquals(initialFirings+1, RuleEngine.ruleFirings);

    groupProgrammers.deleteMember(SubjectTestHelper.SUBJ1);

    assertEquals(initialFirings+2, RuleEngine.ruleFirings);

    assertTrue(payrollGuest.hasMember(SubjectTestHelper.SUBJ1));
  
    permissions = GrouperDAOFactory.getFactory().getPermissionEntry().findByMemberId(member1.getUuid());
    assertEquals(0, permissions.size());
    
    // grouperSession = GrouperSession.startRootSession();
    // addMember("stem:a", "test.subject.0");
    // addMember("stem:b", "test.subject.0");
    // delMember("stem:b", "test.subject.0");
    // hasMember("stem:a", "test.subject.0");
    
  }

  /**
   * 
   */
  public void testRuleLonghandPermissionAssignmentDisabledDate() {
  
    GrouperSession grouperSession = GrouperSession.startRootSession();
    AttributeDef permissionDef = new AttributeDefSave(grouperSession)
      .assignName("stem:permissionDef").assignCreateParentStemsIfNotExist(true)
      .assignAttributeDefType(AttributeDefType.perm)
      .save();
    
    permissionDef.setAssignToEffMembership(true);
    permissionDef.setAssignToGroup(true);
    permissionDef.store();
    
    Group groupEmployee = new GroupSave(grouperSession).assignName("stem:employee").assignCreateParentStemsIfNotExist(true).save();
  
    //make a role
    Role payrollUser = new GroupSave(grouperSession).assignName("apps:payroll:roles:payrollUser").assignTypeOfGroup(TypeOfGroup.role).assignCreateParentStemsIfNotExist(true).save();
    Role payrollGuest = new GroupSave(grouperSession).assignName("apps:payroll:roles:payrollGuest").assignTypeOfGroup(TypeOfGroup.role).assignCreateParentStemsIfNotExist(true).save();
  
    //assign a user to a role
    payrollUser.addMember(SubjectTestHelper.SUBJ0, false);
    payrollGuest.addMember(SubjectTestHelper.SUBJ1, false);
    
    //create a permission, assign to role
    AttributeDefName canLogin = new AttributeDefNameSave(grouperSession, permissionDef).assignName("apps:payroll:permissions:canLogin").assignCreateParentStemsIfNotExist(true).save();
    
    payrollUser.getPermissionRoleDelegate().assignRolePermission(canLogin);
    
    //assign the permission to another user directly, not due to a role
    payrollGuest.getPermissionRoleDelegate().assignSubjectRolePermission(canLogin, SubjectTestHelper.SUBJ1);
    
    //see that they both have the permission
    Member member0 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ0, false);
    Member member1 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ1, false);
    
    Set<PermissionEntry> permissions = GrouperDAOFactory.getFactory().getPermissionEntry().findByMemberId(member0.getUuid());
    assertEquals(1, permissions.size());
    assertEquals("apps:payroll:permissions:canLogin", permissions.iterator().next().getAttributeDefNameName());
    
    permissions = GrouperDAOFactory.getFactory().getPermissionEntry().findByMemberId(member1.getUuid());
    assertEquals(1, permissions.size());
    assertEquals("apps:payroll:permissions:canLogin", permissions.iterator().next().getAttributeDefNameName());
    
    //add a rule on stem:permission saying if you are out of stem:employee, 
    //then put disabled date on assignments to permission, or from roles which have the permission
    AttributeAssign attributeAssign = permissionDef
      .getAttributeDelegate().addAttribute(RuleUtils.ruleAttributeDefName()).getAttributeAssign();
    
    AttributeValueDelegate attributeValueDelegate = attributeAssign.getAttributeValueDelegate();
    attributeValueDelegate.assignValue(
        RuleUtils.ruleActAsSubjectSourceIdName(), "g:isa");
    attributeValueDelegate.assignValue(
        RuleUtils.ruleActAsSubjectIdName(), "GrouperSystem");
    attributeValueDelegate.assignValue(
        RuleUtils.ruleCheckOwnerNameName(), "stem:employee");
    attributeValueDelegate.assignValue(
        RuleUtils.ruleCheckTypeName(), 
        RuleCheckType.membershipRemove.name());
    attributeValueDelegate.assignValue(
        RuleUtils.ruleIfConditionEnumName(), 
        RuleIfConditionEnum.thisPermissionDefHasAssignment.name());
    attributeValueDelegate.assignValue(
        RuleUtils.ruleThenEnumName(), 
        RuleThenEnum.assignDisabledDaysToOwnerPermissionDefAssignments.name());
    attributeValueDelegate.assignValue(
        RuleUtils.ruleThenEnumArg0Name(), "7");

    //should be valid
    String isValidString = attributeValueDelegate.retrieveValueString(
        RuleUtils.ruleValidName());
  
    if (!StringUtils.equals("T", isValidString)) {
      throw new RuntimeException(isValidString);
    }
  
    groupEmployee.addMember(SubjectTestHelper.SUBJ0);
    groupEmployee.addMember(SubjectTestHelper.SUBJ1);
    groupEmployee.addMember(SubjectTestHelper.SUBJ2);
    
    //count rule firings
    long initialFirings = RuleEngine.ruleFirings;
    
    //doesnt do anything
    groupEmployee.deleteMember(SubjectTestHelper.SUBJ2);
  
    assertEquals(initialFirings, RuleEngine.ruleFirings);
  
    groupEmployee.deleteMember(SubjectTestHelper.SUBJ0);
    
    assertEquals(initialFirings+1, RuleEngine.ruleFirings);
    
    //should come out of groupA in 7 days
    Membership membership = ((Group)payrollUser).getImmediateMembership(Group.getDefaultList(), member0, true, true);
    
    assertNotNull(membership.getDisabledTime());
    long disabledTime = membership.getDisabledTime().getTime();
    
    assertTrue("More than 6 days: " + new Date(disabledTime), disabledTime > System.currentTimeMillis() + (6 * 24 * 60 * 60 * 1000));
    assertTrue("Less than 8 days: " + new Date(disabledTime), disabledTime < System.currentTimeMillis() + (8 * 24 * 60 * 60 * 1000));

    
    
    permissions = GrouperDAOFactory.getFactory().getPermissionEntry().findByMemberId(member0.getUuid());
    assertEquals(1, permissions.size());
    assertEquals("apps:payroll:permissions:canLogin", permissions.iterator().next().getAttributeDefNameName());
    
    permissions = GrouperDAOFactory.getFactory().getPermissionEntry().findByMemberId(member1.getUuid());
    assertEquals(1, permissions.size());
    assertEquals("apps:payroll:permissions:canLogin", permissions.iterator().next().getAttributeDefNameName());
    
    //take out second user
    groupEmployee.deleteMember(SubjectTestHelper.SUBJ1);

    assertEquals(initialFirings+2, RuleEngine.ruleFirings);

    assertTrue(payrollGuest.hasMember(SubjectTestHelper.SUBJ1));

    //should come out of groupA in 7 days
    membership = ((Group)payrollGuest).getImmediateMembership(Group.getDefaultList(), member1, true, true);
    
    assertNull(membership.getDisabledTime());

    
    permissions = GrouperDAOFactory.getFactory().getPermissionEntry().findByMemberId(member1.getUuid());
    assertEquals(1, permissions.size());
    assertEquals("apps:payroll:permissions:canLogin", permissions.iterator().next().getAttributeDefNameName());

    Timestamp timestamp = permissions.iterator().next().getDisabledTime();

    disabledTime = timestamp.getTime();
    
    assertTrue("More than 6 days: " + new Date(disabledTime), disabledTime > System.currentTimeMillis() + (6 * 24 * 60 * 60 * 1000));
    assertTrue("Less than 8 days: " + new Date(disabledTime), disabledTime < System.currentTimeMillis() + (8 * 24 * 60 * 60 * 1000));

    
    // grouperSession = GrouperSession.startRootSession();
    // addMember("stem:a", "test.subject.0");
    // addMember("stem:b", "test.subject.0");
    // delMember("stem:b", "test.subject.0");
    // hasMember("stem:a", "test.subject.0");
    
  }

  /**
   * 
   */
  public void testRuleLonghandVetoInOrg() {
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Group ruleGroup = new GroupSave(grouperSession).assignName("stem:a").assignCreateParentStemsIfNotExist(true).save();

    Group groupProgrammers = new GroupSave(grouperSession).assignName("stem:orgs:itEmployee:programmers").assignCreateParentStemsIfNotExist(true).save();
    Group groupSysadmins = new GroupSave(grouperSession).assignName("stem:orgs:itEmployee:sysadmins").assignCreateParentStemsIfNotExist(true).save();

    //Stem mustBeInStem = StemFinder.findByName(grouperSession, "stem:orgs:itEmployee", true);
    
    //add a rule on stem:a saying if not in stem:b, then dont allow add to stem:a
    AttributeAssign attributeAssign = ruleGroup
      .getAttributeDelegate().addAttribute(RuleUtils.ruleAttributeDefName()).getAttributeAssign();
    
    AttributeValueDelegate attributeValueDelegate = attributeAssign.getAttributeValueDelegate();
  
    attributeValueDelegate.assignValue(
        RuleUtils.ruleActAsSubjectSourceIdName(), "g:isa");
    attributeValueDelegate.assignValue(
        RuleUtils.ruleActAsSubjectIdName(), "GrouperSystem");
    attributeValueDelegate.assignValue(
        RuleUtils.ruleCheckTypeName(), RuleCheckType.membershipAdd.name());
    attributeValueDelegate.assignValue(
        RuleUtils.ruleIfConditionEnumName(), RuleIfConditionEnum.noGroupInFolderHasImmediateEnabledMembership.name());
    attributeValueDelegate.assignValue(
        RuleUtils.ruleIfOwnerNameName(), "stem:orgs:itEmployee");
    attributeValueDelegate.assignValue(
        RuleUtils.ruleIfStemScopeName(), "SUB");
    attributeValueDelegate.assignValue(
        RuleUtils.ruleThenEnumName(), RuleThenEnum.veto.name());
    
    //key which would be used in UI messages file if applicable
    attributeValueDelegate.assignValue(
        RuleUtils.ruleThenEnumArg0Name(), "rule.entity.must.be.in.IT.employee.to.be.in.group");
    
    //error message (if key in UI messages file not there)
    attributeValueDelegate.assignValue(
        RuleUtils.ruleThenEnumArg1Name(), "Entity cannot be a member of group if not in the IT department org");
  
    //should be valid
    String isValidString = attributeValueDelegate.retrieveValueString(
        RuleUtils.ruleValidName());
  
    if (!StringUtils.equals("T", isValidString)) {
      throw new RuntimeException(isValidString);
    }
    
    Subject subject0 = SubjectTestHelper.SUBJ0;

    //count rule firings
    long initialFirings = RuleEngine.ruleFirings;
    
    try {
      ruleGroup.addMember(subject0);
      fail("Should be vetoed");
    } catch (RuleVeto rve) {
      //this is good
      String stack = ExceptionUtils.getFullStackTrace(rve);
      assertTrue(stack, stack.contains("Entity cannot be a member of group if not in the IT department org"));
    }
  
    assertEquals(initialFirings+1, RuleEngine.ruleFirings);
  
    groupProgrammers.addMember(subject0);
    ruleGroup.addMember(subject0);
    
    assertEquals("Didnt fire since is a member", initialFirings+1, RuleEngine.ruleFirings);

    assertTrue(ruleGroup.hasMember(subject0));

    ruleGroup.deleteMember(subject0);
    groupProgrammers.deleteMember(subject0);
    groupSysadmins.addMember(subject0);
 
    ruleGroup.addMember(subject0);

    assertEquals("Didnt fire since is a member", initialFirings+1, RuleEngine.ruleFirings);

    assertTrue(ruleGroup.hasMember(subject0));

    // GrouperSession.startRootSession();
    // addMember("stem:a", "test.subject.0");
    // addMember("stem:b", "test.subject.0");
    // delMember("stem:b", "test.subject.0");
    // hasMember("stem:a", "test.subject.0");
    
  }

  /**
   * 
   */
  public void testRuleLonghandVetoPermissions() {

    GrouperSession grouperSession = GrouperSession.startRootSession();
    AttributeDef permissionDef = new AttributeDefSave(grouperSession)
      .assignName("stem:permissionDef").assignCreateParentStemsIfNotExist(true)
      .assignAttributeDefType(AttributeDefType.perm)
      .save();
    
    permissionDef.setAssignToEffMembership(true);
    permissionDef.setAssignToGroup(true);
    permissionDef.store();
    
    Group groupEmployee = new GroupSave(grouperSession).assignName("stem:employee").assignCreateParentStemsIfNotExist(true).save();

    //make a role
    Role payrollUser = new GroupSave(grouperSession).assignName("apps:payroll:roles:payrollUser").assignTypeOfGroup(TypeOfGroup.role).assignCreateParentStemsIfNotExist(true).save();

    Subject subject0 = SubjectFinder.findByIdAndSource("test.subject.0", "jdbc", true);
    
    //assign a user to a role
    payrollUser.addMember(subject0, false);
    
    //create a permission, assign to role
    AttributeDefName canLogin = new AttributeDefNameSave(grouperSession, permissionDef).assignName("apps:payroll:permissions:canLogin").assignCreateParentStemsIfNotExist(true).save();
       
    //add a rule on stem:a saying if not in stem:b, then dont allow add to stem:a
    AttributeAssign attributeAssign = permissionDef
      .getAttributeDelegate().addAttribute(RuleUtils.ruleAttributeDefName()).getAttributeAssign();
    
    AttributeValueDelegate attributeValueDelegate = attributeAssign.getAttributeValueDelegate();
  
    attributeValueDelegate.assignValue(
        RuleUtils.ruleActAsSubjectSourceIdName(), "g:isa");
    attributeValueDelegate.assignValue(
        RuleUtils.ruleActAsSubjectIdName(), "GrouperSystem");
    attributeValueDelegate.assignValue(
        RuleUtils.ruleCheckTypeName(), RuleCheckType.permissionAssignToSubject.name());
    attributeValueDelegate.assignValue(
        RuleUtils.ruleIfConditionEnumName(), RuleIfConditionEnum.groupHasNoImmediateEnabledMembership.name());
    attributeValueDelegate.assignValue(
        RuleUtils.ruleIfOwnerNameName(), "stem:employee");
    attributeValueDelegate.assignValue(
        RuleUtils.ruleThenEnumName(), RuleThenEnum.veto.name());
    
    //key which would be used in UI messages file if applicable
    attributeValueDelegate.assignValue(
        RuleUtils.ruleThenEnumArg0Name(), "rule.entity.must.be.an.employee");
    
    //error message (if key in UI messages file not there)
    attributeValueDelegate.assignValue(
        RuleUtils.ruleThenEnumArg1Name(), "Entity cannot be assigned these permissions unless they are an employee");
  
    //should be valid
    String isValidString = attributeValueDelegate.retrieveValueString(
        RuleUtils.ruleValidName());
  
    if (!StringUtils.equals("T", isValidString)) {
      throw new RuntimeException(isValidString);
    }

    //count rule firings
    long initialFirings = RuleEngine.ruleFirings;
    

    try {
      //assign the permission to another user directly, not due to a role
      payrollUser.getPermissionRoleDelegate().assignSubjectRolePermission(canLogin, subject0);
    } catch (RuleVeto rve) {
      //this is good
      String stack = ExceptionUtils.getFullStackTrace(rve);
      assertTrue(stack, stack.contains("Entity cannot be assigned these permissions unless they are an employee"));
    }

    assertEquals(initialFirings+1, RuleEngine.ruleFirings);

    //see that not have the permission
    Member member0 = MemberFinder.findBySubject(grouperSession, subject0, false);
    
    Set<PermissionEntry> permissions = GrouperDAOFactory.getFactory().getPermissionEntry().findByMemberId(member0.getUuid());
    assertEquals(0, permissions.size());
    
    groupEmployee.addMember(subject0);
    
    payrollUser.getPermissionRoleDelegate().assignSubjectRolePermission(canLogin, subject0);

    assertEquals("Didnt fire since is a member", initialFirings+1, RuleEngine.ruleFirings);

    permissions = GrouperDAOFactory.getFactory().getPermissionEntry().findByMemberId(member0.getUuid());
    assertEquals(1, permissions.size());
    assertEquals("apps:payroll:permissions:canLogin", permissions.iterator().next().getAttributeDefNameName());
    
    // GrouperSession.startRootSession();
    // addMember("stem:a", "test.subject.0");
    // addMember("stem:b", "test.subject.0");
    // delMember("stem:b", "test.subject.0");
    // hasMember("stem:a", "test.subject.0");
    
  }
  
  /**
   * 
   */
  public void testRuleLonghandEmailFlattenedRemove() {
    GrouperSession grouperSession = GrouperSession.startRootSession();

    //run at first so the consumer is initted
    GrouperLoader.runOnceByJobName(grouperSession, GrouperLoaderType.GROUPER_CHANGE_LOG_TEMP_TO_CHANGE_LOG);
    GrouperLoader.runOnceByJobName(grouperSession, GrouperLoaderType.GROUPER_CHANGE_LOG_CONSUMER_PREFIX + "grouperRules");

    Group groupEmployee = new GroupSave(grouperSession).assignName("stem:employee").assignCreateParentStemsIfNotExist(true).save();
    Group groupProgrammer = new GroupSave(grouperSession).assignName("stem:programmer").assignCreateParentStemsIfNotExist(true).save();
    Group groupResearcher = new GroupSave(grouperSession).assignName("stem:researcher").assignCreateParentStemsIfNotExist(true).save();

    groupEmployee.addMember(groupProgrammer.toSubject());
    groupEmployee.addMember(groupResearcher.toSubject());

    Subject subject0 = SubjectFinder.findByIdAndSource("test.subject.0", "jdbc", true);
    
    //subject0 is an employee by two paths
    groupProgrammer.addMember(subject0, false);
    groupResearcher.addMember(subject0, false);
    
    //add a rule on stem:a saying if you are out of the group by all paths (flattened), then send an email
    AttributeAssign attributeAssign = groupEmployee
      .getAttributeDelegate().addAttribute(RuleUtils.ruleAttributeDefName()).getAttributeAssign();
    
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleActAsSubjectSourceIdName(), "g:isa");
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleActAsSubjectIdName(), "GrouperSystem");
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleCheckTypeName(), 
        RuleCheckType.flattenedMembershipRemove.name());
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleThenEnumName(), RuleThenEnum.sendEmail.name());
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleThenEnumArg0Name(), "a@b.c, ${safeSubject.emailAddress}");
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleThenEnumArg1Name(), "You will be removed from group: ${groupDisplayExtension}");
    
    //the to, subject, or body could be text with EL variables, or could be a template.  If template, it is
    //read from the classpath from package: grouperRulesEmailTemplates/theTemplateName.txt
    //or you could configure grouper.properties to keep them in an external folder, not in the classpath
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleThenEnumArg2Name(), "template: testEmailGroupBodyFlattenedRemove");
    
    //should be valid
    String isValidString = attributeAssign.getAttributeValueDelegate().retrieveValueString(
        RuleUtils.ruleValidName());
    assertEquals("T", isValidString);
    
    //count rule firings
    long initialFirings = RuleEngine.ruleFirings;
    
    long initialEmailCount = GrouperEmail.testingEmailCount;
    
    //doesnt do anything, still in the group by another path
    groupProgrammer.deleteMember(subject0);
  
    GrouperLoader.runOnceByJobName(grouperSession, GrouperLoaderType.GROUPER_CHANGE_LOG_TEMP_TO_CHANGE_LOG);
    GrouperLoader.runOnceByJobName(grouperSession, GrouperLoaderType.GROUPER_CHANGE_LOG_CONSUMER_PREFIX + "grouperRules");

    assertEquals(initialFirings, RuleEngine.ruleFirings);
    assertEquals(initialEmailCount, GrouperEmail.testingEmailCount);

    groupResearcher.deleteMember(subject0);

    //run the change log to change log temp and rules consumer
    GrouperLoader.runOnceByJobName(grouperSession, GrouperLoaderType.GROUPER_CHANGE_LOG_TEMP_TO_CHANGE_LOG);
    GrouperLoader.runOnceByJobName(grouperSession, GrouperLoaderType.GROUPER_CHANGE_LOG_CONSUMER_PREFIX + "grouperRules");

    assertEquals(initialFirings+1, RuleEngine.ruleFirings);
    assertEquals(initialEmailCount+1, GrouperEmail.testingEmailCount);

    //should send an email...
    
  }

  /**
   * 
   */
  public void testRuleLonghandEmailFlattenedAddFromStem() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    Group groupEmployee = new GroupSave(grouperSession).assignName("stem:employee").assignCreateParentStemsIfNotExist(true).save();
    Group groupProgrammer = new GroupSave(grouperSession).assignName("stem:programmer").assignCreateParentStemsIfNotExist(true).save();
    Group groupResearcher = new GroupSave(grouperSession).assignName("stem:researcher").assignCreateParentStemsIfNotExist(true).save();
  
    groupEmployee.addMember(groupProgrammer.toSubject());
    groupEmployee.addMember(groupResearcher.toSubject());
  
    Stem stem = StemFinder.findByName(grouperSession, "stem", true);
    
    Subject subject0 = SubjectFinder.findByIdAndSource("test.subject.0", "jdbc", true);
    
    //add a rule on stem:a saying if you are added to a group in the stem by a new paths (flattened), then send an email
    AttributeAssign attributeAssign = stem
      .getAttributeDelegate().addAttribute(RuleUtils.ruleAttributeDefName()).getAttributeAssign();
    
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleActAsSubjectSourceIdName(), "g:isa");
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleActAsSubjectIdName(), "GrouperSystem");
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleCheckTypeName(), 
        RuleCheckType.flattenedMembershipAddInFolder.name());
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleCheckStemScopeName(),
        Stem.Scope.SUB.name());
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleThenEnumName(), RuleThenEnum.sendEmail.name());
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleThenEnumArg0Name(), "a@b.c, ${safeSubject.emailAddress}");

    //the to, subject, or body could be text with EL variables, or could be a template.  If template, it is
    //read from the classpath from package: grouperRulesEmailTemplates/theTemplateName.txt
    //or you could configure grouper.properties to keep them in an external folder, not in the classpath
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleThenEnumArg1Name(), "template: testEmailGroupSubjectFlattenedAddInFolder");
    
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleThenEnumArg2Name(), "Hello ${safeSubject.name},\n\nJust letting you know you were removed from group ${groupDisplayExtension} in the central Groups management system.  Please do not respond to this email.\n\nRegards.");
    
    //should be valid
    String isValidString = attributeAssign.getAttributeValueDelegate().retrieveValueString(
        RuleUtils.ruleValidName());
    assertEquals("T", isValidString);

    //run at first so the consumer is initted
    GrouperLoader.runOnceByJobName(grouperSession, GrouperLoaderType.GROUPER_CHANGE_LOG_TEMP_TO_CHANGE_LOG);
    GrouperLoader.runOnceByJobName(grouperSession, GrouperLoaderType.GROUPER_CHANGE_LOG_CONSUMER_PREFIX + "grouperRules");

    //count rule firings
    long initialFirings = RuleEngine.ruleFirings;
    
    long initialEmailCount = GrouperEmail.testingEmailCount;
    
    //subject0 is an employee by two paths
    groupProgrammer.addMember(subject0, false);
    
    GrouperLoader.runOnceByJobName(grouperSession, GrouperLoaderType.GROUPER_CHANGE_LOG_TEMP_TO_CHANGE_LOG);
    GrouperLoader.runOnceByJobName(grouperSession, GrouperLoaderType.GROUPER_CHANGE_LOG_CONSUMER_PREFIX + "grouperRules");
  
    assertEquals(initialFirings+2, RuleEngine.ruleFirings);
    assertEquals(initialEmailCount+2, GrouperEmail.testingEmailCount);

    groupResearcher.addMember(subject0, false);

    GrouperLoader.runOnceByJobName(grouperSession, GrouperLoaderType.GROUPER_CHANGE_LOG_TEMP_TO_CHANGE_LOG);
    GrouperLoader.runOnceByJobName(grouperSession, GrouperLoaderType.GROUPER_CHANGE_LOG_CONSUMER_PREFIX + "grouperRules");
  
    assertEquals(initialFirings+3, RuleEngine.ruleFirings);
    assertEquals(initialEmailCount+3, GrouperEmail.testingEmailCount);
    
    groupEmployee.addMember(subject0);
  
    //run the change log to change log temp and rules consumer
    GrouperLoader.runOnceByJobName(grouperSession, GrouperLoaderType.GROUPER_CHANGE_LOG_TEMP_TO_CHANGE_LOG);
    GrouperLoader.runOnceByJobName(grouperSession, GrouperLoaderType.GROUPER_CHANGE_LOG_CONSUMER_PREFIX + "grouperRules");
  
    //should not send an email...
    assertEquals(initialFirings+3, RuleEngine.ruleFirings);
    assertEquals(initialEmailCount+3, GrouperEmail.testingEmailCount);
  
    
  }

  /**
   * 
   */
  public void testRuleLonghandEmailDisabledDate() {
    GrouperSession grouperSession = GrouperSession.startRootSession();

    Group groupEmployee = new GroupSave(grouperSession).assignName("stem:employee").assignCreateParentStemsIfNotExist(true).save();
    Group groupProgrammer = new GroupSave(grouperSession).assignName("stem:programmer").assignCreateParentStemsIfNotExist(true).save();
  
    groupEmployee.addMember(groupProgrammer.toSubject());

    Subject subject0 = SubjectFinder.findByIdAndSource("test.subject.0", "jdbc", true);
    
    //add a rule on stem:a saying if you are about to be out of the group by all paths (flattened), then send an email
    AttributeAssign attributeAssign = groupEmployee
      .getAttributeDelegate().addAttribute(RuleUtils.ruleAttributeDefName()).getAttributeAssign();
    
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleActAsSubjectSourceIdName(), "g:isa");
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleActAsSubjectIdName(), "GrouperSystem");
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleCheckTypeName(), 
        RuleCheckType.membershipDisabledDate.name());
    
    //will find memberships with a disabled date at least 6 days from now.  blank means no min
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleCheckArg0Name(), "6");

    //will find memberships with a disabled date at most 8 days from now.  blank means no max
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleCheckArg1Name(), "8");

    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleThenEnumName(), RuleThenEnum.sendEmail.name());
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleThenEnumArg0Name(), GrouperConfig.getProperty("mail.test.address") + ", ${safeSubject.emailAddress}");
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleThenEnumArg1Name(), "You will be removed from group: ${groupDisplayExtension} on ${ruleElUtils.formatDate(membershipDisabledTimestamp, 'yyyy/MM/dd')}");
 
    //the to, subject, or body could be text with EL variables, or could be a template.  If template, it is
    //read from the classpath from package: grouperRulesEmailTemplates/theTemplateName.txt
    //or you could configure grouper.properties to keep them in an external folder, not in the classpath
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleThenEnumArg2Name(), "Hello ${safeSubject.name},\n\nJust letting you know you will be removed from group ${groupDisplayExtension} on ${ruleElUtils.formatDate(membershipDisabledTimestamp, 'yyyy/MM/dd')} in the central Groups management system.  Please do not respond to this email.\n\nRegards.");
    
    //should be valid
    String isValidString = attributeAssign.getAttributeValueDelegate().retrieveValueString(
        RuleUtils.ruleValidName());
    assertEquals("T", isValidString);
    
    //count rule firings
    long initialEmailCount = GrouperEmail.testingEmailCount;
    
    groupEmployee.addMember(subject0, false);
    
    GrouperLoader.runOnceByJobName(grouperSession, "MAINTENANCE__rules");

    Member member0 = MemberFinder.findBySubject(grouperSession, subject0, false);
    
    Membership membership = groupEmployee.getImmediateMembership(Group.getDefaultList(), member0, true, true);
    
    //set disabled 7 days in the future
    membership.setDisabledTime(new Timestamp(System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000)));
    membership.update();
    
    GrouperLoader.runOnceByJobName(grouperSession, "MAINTENANCE__rules");
    
    //should fire
    assertEquals(initialEmailCount + 1, GrouperEmail.testingEmailCount);

    membership.setDisabledTime(new Timestamp(System.currentTimeMillis() + (5 * 24 * 60 * 60 * 1000)));
    membership.update();
    
    GrouperLoader.runOnceByJobName(grouperSession, "MAINTENANCE__rules");
    
    //should not fire
    assertEquals(initialEmailCount + 1, GrouperEmail.testingEmailCount);

    membership.setDisabledTime(new Timestamp(System.currentTimeMillis() + (9 * 24 * 60 * 60 * 1000)));
    membership.update();
    
    GrouperLoader.runOnceByJobName(grouperSession, "MAINTENANCE__rules");
    
    //should not fire
    assertEquals(initialEmailCount + 1, GrouperEmail.testingEmailCount);

    membership.setDisabledTime(new Timestamp(System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000)));
    membership.update();
    
    GrouperLoader.runOnceByJobName(grouperSession, "MAINTENANCE__rules");
    
    //should fire
    assertEquals(initialEmailCount + 2, GrouperEmail.testingEmailCount);

    groupProgrammer.addMember(subject0);

    GrouperLoader.runOnceByJobName(grouperSession, "MAINTENANCE__rules");
    
    //should not fire
    assertEquals(initialEmailCount + 2, GrouperEmail.testingEmailCount);

  }

  /**
   * note, this doesnt work until permissions change log notifications are working (and wired up correctly,
   * currently assumptions are made about what exists)
   */
  public void testRuleLonghandEmailFlattenedPermissionAssign() {
  
    GrouperSession grouperSession = GrouperSession.startRootSession();
  
    //run at first so the consumer is initted
    GrouperLoader.runOnceByJobName(grouperSession, GrouperLoaderType.GROUPER_CHANGE_LOG_TEMP_TO_CHANGE_LOG);
    GrouperLoader.runOnceByJobName(grouperSession, GrouperLoaderType.GROUPER_CHANGE_LOG_CONSUMER_PREFIX + "grouperRules");
    
    AttributeDef permissionDef = new AttributeDefSave(grouperSession)
      .assignName("stem:permissionDef").assignCreateParentStemsIfNotExist(true)
      .assignAttributeDefType(AttributeDefType.perm)
      .save();
    
    permissionDef.setAssignToEffMembership(true);
    permissionDef.setAssignToGroup(true);
    permissionDef.store();
    
    //make a role
    Role payrollUser = new GroupSave(grouperSession).assignName("apps:payroll:roles:payrollUser").assignTypeOfGroup(TypeOfGroup.role).assignCreateParentStemsIfNotExist(true).save();
    Role payrollGuest = new GroupSave(grouperSession).assignName("apps:payroll:roles:payrollGuest").assignTypeOfGroup(TypeOfGroup.role).assignCreateParentStemsIfNotExist(true).save();
  
    payrollGuest.addMember(SubjectTestHelper.SUBJ0, false);
    payrollGuest.addMember(SubjectTestHelper.SUBJ1, false);
    
    //create a permission, assign to role
    AttributeDefName canLogin = new AttributeDefNameSave(grouperSession, permissionDef).assignName("apps:payroll:permissions:canLogin").assignCreateParentStemsIfNotExist(true).save();
    
    payrollUser.getPermissionRoleDelegate().assignRolePermission(canLogin);
    
    //add a rule on stem:permission saying that if a permission is flat added, then send an email out
    AttributeAssign attributeAssign = permissionDef
      .getAttributeDelegate().addAttribute(RuleUtils.ruleAttributeDefName()).getAttributeAssign();
    
    AttributeValueDelegate attributeValueDelegate = attributeAssign.getAttributeValueDelegate();
    attributeValueDelegate.assignValue(
        RuleUtils.ruleActAsSubjectSourceIdName(), "g:isa");
    attributeValueDelegate.assignValue(
        RuleUtils.ruleActAsSubjectIdName(), "GrouperSystem");
    attributeValueDelegate.assignValue(
        RuleUtils.ruleCheckTypeName(), 
        RuleCheckType.flattenedPermissionAssignToSubject.name());
    attributeValueDelegate.assignValue(
        RuleUtils.ruleThenEnumName(), RuleThenEnum.sendEmail.name());
    
    //send an email to a hardcoded address, and to the user being assigned
    attributeValueDelegate.assignValue(
        RuleUtils.ruleThenEnumArg0Name(), "a@b.c, ${safeSubject.emailAddress}");
    attributeValueDelegate.assignValue(
        RuleUtils.ruleThenEnumArg1Name(), "You were assigned permission: ${attributeDefNameDisplayExtension} in role ${roleDisplayExtension}");
    
    //the to, subject, or body could be text with EL variables, or could be a template.  If template, it is
    //read from the classpath from package: grouperRulesEmailTemplates/theTemplateName.txt
    //or you could configure grouper.properties to keep them in an external folder, not in the classpath
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleThenEnumArg2Name(), "Hello ${safeSubject.name},\n\nJust letting you know you were assigned permission ${attributeDefNameDisplayExtension} in role ${roleDisplayExtension} in the central Groups/Permissions management system.  Please do not respond to this email.\n\nRegards.");
    
    //should be valid
    String isValidString = attributeValueDelegate.retrieveValueString(
        RuleUtils.ruleValidName());
  
    if (!StringUtils.equals("T", isValidString)) {
      throw new RuntimeException(isValidString);
    }
  
    //count rule firings
    long initialFirings = RuleEngine.ruleFirings;
    long initialEmailCount = GrouperEmail.testingEmailCount;

    //assign a permission
    payrollUser.addMember(SubjectTestHelper.SUBJ0, false);

    //should fire and send email
    GrouperLoader.runOnceByJobName(grouperSession, GrouperLoaderType.GROUPER_CHANGE_LOG_TEMP_TO_CHANGE_LOG);
    GrouperLoader.runOnceByJobName(grouperSession, GrouperLoaderType.GROUPER_CHANGE_LOG_CONSUMER_PREFIX + "grouperRules");

    assertEquals(initialFirings+1, RuleEngine.ruleFirings);
    assertEquals(initialEmailCount+1, GrouperEmail.testingEmailCount);

    //assign by a different path
    payrollGuest.getPermissionRoleDelegate().assignSubjectRolePermission(canLogin, SubjectTestHelper.SUBJ0);
    
    //shouldnt fire or send email
    GrouperLoader.runOnceByJobName(grouperSession, GrouperLoaderType.GROUPER_CHANGE_LOG_TEMP_TO_CHANGE_LOG);
    GrouperLoader.runOnceByJobName(grouperSession, GrouperLoaderType.GROUPER_CHANGE_LOG_CONSUMER_PREFIX + "grouperRules");

    assertEquals(initialFirings+1, RuleEngine.ruleFirings);
    assertEquals(initialEmailCount+1, GrouperEmail.testingEmailCount);

    //assign a new user directly
    payrollGuest.getPermissionRoleDelegate().assignSubjectRolePermission(canLogin, SubjectTestHelper.SUBJ1);
    
    //should fire and send email
    GrouperLoader.runOnceByJobName(grouperSession, GrouperLoaderType.GROUPER_CHANGE_LOG_TEMP_TO_CHANGE_LOG);
    GrouperLoader.runOnceByJobName(grouperSession, GrouperLoaderType.GROUPER_CHANGE_LOG_CONSUMER_PREFIX + "grouperRules");

    assertEquals(initialFirings+2, RuleEngine.ruleFirings);
    assertEquals(initialEmailCount+2, GrouperEmail.testingEmailCount);

    //assign by a different path
    payrollUser.addMember(SubjectTestHelper.SUBJ1, false);

    //shouldnt fire or send email
    GrouperLoader.runOnceByJobName(grouperSession, GrouperLoaderType.GROUPER_CHANGE_LOG_TEMP_TO_CHANGE_LOG);
    GrouperLoader.runOnceByJobName(grouperSession, GrouperLoaderType.GROUPER_CHANGE_LOG_CONSUMER_PREFIX + "grouperRules");

    assertEquals(initialFirings+2, RuleEngine.ruleFirings);
    assertEquals(initialEmailCount+2, GrouperEmail.testingEmailCount);
    
  }

  /**
   * 
   */
  public void testRuleLonghandEmailPermissionsDisabledDate() {

    GrouperSession grouperSession = GrouperSession.startRootSession();

    AttributeDef permissionDef = new AttributeDefSave(grouperSession)
      .assignName("stem:permissionDef").assignCreateParentStemsIfNotExist(true)
      .assignAttributeDefType(AttributeDefType.perm)
      .save();
    
    permissionDef.setAssignToEffMembership(true);
    permissionDef.setAssignToGroup(true);
    permissionDef.store();
    
    //add a rule on the permission definition saying if you are about to lose a permission by all paths (flattened), then send an email
    AttributeAssign attributeAssign = permissionDef
      .getAttributeDelegate().addAttribute(RuleUtils.ruleAttributeDefName()).getAttributeAssign();
    
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleActAsSubjectSourceIdName(), "g:isa");
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleActAsSubjectIdName(), "GrouperSystem");
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleCheckTypeName(), 
        RuleCheckType.permissionDisabledDate.name());
    
    //will find permissions with a disabled date at least 6 days from now.  blank means no min
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleCheckArg0Name(), "6");
  
    //will find permissions with a disabled date at most 8 days from now.  blank means no max
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleCheckArg1Name(), "8");
  
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleThenEnumName(), RuleThenEnum.sendEmail.name());
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleThenEnumArg0Name(), GrouperConfig.getProperty("mail.test.address") + ", ${safeSubject.emailAddress}");
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleThenEnumArg1Name(), "You will have this permission unassigned: ${attributeDefNameDisplayExtension} in role ${roleDisplayExtension}, removed on ${ruleElUtils.formatDate(permissionDisabledTimestamp, 'yyyy/MM/dd')}");
  
    //the to, subject, or body could be text with EL variables, or could be a template.  If template, it is
    //read from the classpath from package: grouperRulesEmailTemplates/theTemplateName.txt
    //or you could configure grouper.properties to keep them in an external folder, not in the classpath
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleThenEnumArg2Name(), "Hello ${safeSubject.name},\n\nJust letting you know you will have this permission removed ${attributeDefNameDisplayExtension} in role ${roleDisplayExtension}, removed on ${ruleElUtils.formatDate(permissionDisabledTimestamp, 'yyyy/MM/dd')} in the central Groups / Permissions management system.  Please do not respond to this email.\n\nRegards.");
    
    //should be valid
    String isValidString = attributeAssign.getAttributeValueDelegate().retrieveValueString(
        RuleUtils.ruleValidName());
    assertEquals("T", isValidString);
    
    //count rule firings
    long initialEmailCount = GrouperEmail.testingEmailCount;

    //make a role
    Role payrollUser = new GroupSave(grouperSession).assignName("apps:payroll:roles:payrollUser").assignTypeOfGroup(TypeOfGroup.role).assignCreateParentStemsIfNotExist(true).save();
    Role payrollGuest = new GroupSave(grouperSession).assignName("apps:payroll:roles:payrollGuest").assignTypeOfGroup(TypeOfGroup.role).assignCreateParentStemsIfNotExist(true).save();

    Subject subject0 = SubjectFinder.findByIdAndSource("test.subject.0", "jdbc", true);

    //subject 1,2 is just more data in the mix
    Subject subject1 = SubjectFinder.findByIdAndSource("test.subject.1", "jdbc", true);
    Subject subject2 = SubjectFinder.findByIdAndSource("test.subject.2", "jdbc", true);
    

    payrollUser.addMember(subject1, false);
    payrollGuest.addMember(subject0, false);
    payrollGuest.addMember(subject2, false);
    
    //create a permission, assign to role
    AttributeDefName canLogin = new AttributeDefNameSave(grouperSession, permissionDef).assignName("apps:payroll:permissions:canLogin").assignCreateParentStemsIfNotExist(true).save();
    
    payrollUser.getPermissionRoleDelegate().assignRolePermission(canLogin);

    payrollGuest.getPermissionRoleDelegate().assignSubjectRolePermission(canLogin, subject2);

    try {
      GrouperDAOFactory.getFactory().getPermissionEntry().findPermissionsByAttributeDefDisabledRange(permissionDef.getId(),
          null, null);

      fail("should need either disabled from or to");
    } catch (Exception e) {
      //good
    }

    attributeAssign = payrollGuest.getPermissionRoleDelegate().assignSubjectRolePermission(canLogin, subject0).getAttributeAssign();

    GrouperLoader.runOnceByJobName(grouperSession, "MAINTENANCE__rules");
    
    //should not fire
    assertEquals(initialEmailCount, GrouperEmail.testingEmailCount);

    //set disabled 7 days in the future
    attributeAssign.setDisabledTime(new Timestamp(System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000)));
    attributeAssign.saveOrUpdate();
    
    GrouperLoader.runOnceByJobName(grouperSession, "MAINTENANCE__rules");
    
    //should fire
    assertEquals(initialEmailCount + 1, GrouperEmail.testingEmailCount);
  
    attributeAssign.setDisabledTime(new Timestamp(System.currentTimeMillis() + (5 * 24 * 60 * 60 * 1000)));
    attributeAssign.saveOrUpdate();
    
    GrouperLoader.runOnceByJobName(grouperSession, "MAINTENANCE__rules");
    
    //should not fire
    assertEquals(initialEmailCount + 1, GrouperEmail.testingEmailCount);
  
    attributeAssign.setDisabledTime(new Timestamp(System.currentTimeMillis() + (9 * 24 * 60 * 60 * 1000)));
    attributeAssign.saveOrUpdate();
    
    GrouperLoader.runOnceByJobName(grouperSession, "MAINTENANCE__rules");
    
    //should not fire
    assertEquals(initialEmailCount + 1, GrouperEmail.testingEmailCount);
  
    attributeAssign.setDisabledTime(new Timestamp(System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000)));
    attributeAssign.saveOrUpdate();
    
    GrouperLoader.runOnceByJobName(grouperSession, "MAINTENANCE__rules");
    
    //should fire
    assertEquals(initialEmailCount + 2, GrouperEmail.testingEmailCount);
  
    payrollUser.addMember(subject0, false);
  
    GrouperLoader.runOnceByJobName(grouperSession, "MAINTENANCE__rules");
    
    //should not fire
    assertEquals(initialEmailCount + 2, GrouperEmail.testingEmailCount);
  
  }

  /**
   * 
   */
  public void testRuleLonghandPermissionAssignmentDaemon() {
  
    GrouperSession grouperSession = GrouperSession.startRootSession();

    AttributeDef permissionDef = new AttributeDefSave(grouperSession)
      .assignName("stem:permissionDef").assignCreateParentStemsIfNotExist(true)
      .assignAttributeDefType(AttributeDefType.perm)
      .save();
    
    permissionDef.setAssignToEffMembership(true);
    permissionDef.setAssignToGroup(true);
    permissionDef.store();
    
    new GroupSave(grouperSession).assignName("stem:employee").assignCreateParentStemsIfNotExist(true).save();
  
    //make a role
    Role payrollUser = new GroupSave(grouperSession).assignName("apps:payroll:roles:payrollUser").assignTypeOfGroup(TypeOfGroup.role).assignCreateParentStemsIfNotExist(true).save();
    Role payrollGuest = new GroupSave(grouperSession).assignName("apps:payroll:roles:payrollGuest").assignTypeOfGroup(TypeOfGroup.role).assignCreateParentStemsIfNotExist(true).save();
  
    //assign a user to a role
    payrollUser.addMember(SubjectTestHelper.SUBJ0, false);
    payrollGuest.addMember(SubjectTestHelper.SUBJ1, false);
    
    //create a permission, assign to role
    AttributeDefName canLogin = new AttributeDefNameSave(grouperSession, permissionDef).assignName("apps:payroll:permissions:canLogin").assignCreateParentStemsIfNotExist(true).save();
    
    payrollUser.getPermissionRoleDelegate().assignRolePermission(canLogin);
    
    //assign the permission to another user directly, not due to a role
    payrollGuest.getPermissionRoleDelegate().assignSubjectRolePermission(canLogin, SubjectTestHelper.SUBJ1);
    
    //see that they both have the permission
    Member member0 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ0, false);
    Member member1 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ1, false);
    
    Set<PermissionEntry> permissions = GrouperDAOFactory.getFactory().getPermissionEntry().findByMemberId(member0.getUuid());
    assertEquals(1, permissions.size());
    assertEquals("apps:payroll:permissions:canLogin", permissions.iterator().next().getAttributeDefNameName());
    
    permissions = GrouperDAOFactory.getFactory().getPermissionEntry().findByMemberId(member1.getUuid());
    assertEquals(1, permissions.size());
    assertEquals("apps:payroll:permissions:canLogin", permissions.iterator().next().getAttributeDefNameName());
    
    //add a rule on stem:permission saying if you are out of stem:employee, 
    //then remove assignments to permission, or from roles which have the permission
    AttributeAssign attributeAssign = permissionDef
      .getAttributeDelegate().addAttribute(RuleUtils.ruleAttributeDefName()).getAttributeAssign();
    
    AttributeValueDelegate attributeValueDelegate = attributeAssign.getAttributeValueDelegate();
    attributeValueDelegate.assignValue(
        RuleUtils.ruleActAsSubjectSourceIdName(), "g:isa");
    attributeValueDelegate.assignValue(
        RuleUtils.ruleActAsSubjectIdName(), "GrouperSystem");
    attributeValueDelegate.assignValue(
        RuleUtils.ruleCheckOwnerNameName(), "stem:employee");
    attributeValueDelegate.assignValue(
        RuleUtils.ruleCheckTypeName(), 
        RuleCheckType.membershipRemove.name());
    attributeValueDelegate.assignValue(
        RuleUtils.ruleIfConditionEnumName(), 
        RuleIfConditionEnum.thisPermissionDefHasAssignment.name());
    attributeValueDelegate.assignValue(
        RuleUtils.ruleThenEnumName(), 
        RuleThenEnum.removeMemberFromOwnerPermissionDefAssignments.name());
  
    //should be valid
    String isValidString = attributeValueDelegate.retrieveValueString(
        RuleUtils.ruleValidName());
  
    if (!StringUtils.equals("T", isValidString)) {
      throw new RuntimeException(isValidString);
    }
  
    //run the daemon
    String status = GrouperLoader.runOnceByJobName(grouperSession, GrouperLoaderType.GROUPER_RULES);
    
    assertTrue(status.toLowerCase().contains("success"));

    //should come out of groupA
    assertFalse(payrollUser.hasMember(SubjectTestHelper.SUBJ0));
  
    permissions = GrouperDAOFactory.getFactory().getPermissionEntry().findByMemberId(member0.getUuid());
    assertEquals(0, permissions.size());
    permissions = GrouperDAOFactory.getFactory().getPermissionEntry().findByMemberId(member1.getUuid());
    assertEquals(0, permissions.size());
    
    assertTrue(payrollGuest.hasMember(SubjectTestHelper.SUBJ1));
  
    
  }

  /**
   * 
   */
  public void testRuleLonghandStemScopeSubDaemon() {

    GrouperSession grouperSession = GrouperSession.startRootSession();
    Group groupA = new GroupSave(grouperSession).assignName("stem1:a").assignCreateParentStemsIfNotExist(true).save();
    
    //add a rule on stem:a saying if you are out of stem2, then remove from stem:a
    AttributeAssign attributeAssign = groupA
      .getAttributeDelegate().addAttribute(RuleUtils.ruleAttributeDefName()).getAttributeAssign();
    
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
    
    
    groupA.addMember(SubjectTestHelper.SUBJ0);
    
    //run the daemon
    String status = GrouperLoader.runOnceByJobName(grouperSession, GrouperLoaderType.GROUPER_RULES);
    
    assertTrue(status.toLowerCase().contains("success"));
    
    //count rule firings
    //should come out of groupA
    assertFalse(groupA.hasMember(SubjectTestHelper.SUBJ0));
  
    assertEquals(initialFirings+1, RuleEngine.ruleFirings);
  
    
  }

  /**
   * 
   */
  public void testRuleLonghandStemScopeSubCreateGroupNamePattern() {

    GrouperSession grouperSession = GrouperSession.startRootSession();
    Stem stem2 = new StemSave(grouperSession).assignName("stem2").assignCreateParentStemsIfNotExist(true).save();
  
    Group groupA = new GroupSave(grouperSession).assignName("stem1:admins").assignCreateParentStemsIfNotExist(true).save();
  
    groupA.addMember(SubjectTestHelper.SUBJ0);
    
    
    //add a rule on stem2 saying if you create a group underneath, then assign a reader and updater group
    AttributeAssign attributeAssign = stem2
      .getAttributeDelegate().addAttribute(RuleUtils.ruleAttributeDefName()).getAttributeAssign();
    
    AttributeValueDelegate attributeValueDelegate = attributeAssign.getAttributeValueDelegate();
    
    attributeValueDelegate.assignValue(
        RuleUtils.ruleActAsSubjectSourceIdName(), "g:isa");
    attributeValueDelegate.assignValue(
        RuleUtils.ruleActAsSubjectIdName(), "GrouperSystem");
    attributeValueDelegate.assignValue(
        RuleUtils.ruleCheckTypeName(), RuleCheckType.groupCreate.name());
    
    //can be SUB or ONE for if in this folder, or in this and all subfolders
    attributeValueDelegate.assignValue(
        RuleUtils.ruleCheckStemScopeName(), Stem.Scope.SUB.name());
    
    //if matches a certain name (db like string)

    attributeValueDelegate.assignValue(
        RuleUtils.ruleIfConditionEnumName(), RuleIfConditionEnum.nameMatchesSqlLikeString.name());
    attributeValueDelegate.assignValue(
        RuleUtils.ruleIfConditionEnumArg0Name(), "stem2:%someGroup");
    
    attributeValueDelegate.assignValue(
        RuleUtils.ruleThenEnumName(), RuleThenEnum.assignGroupPrivilegeToGroupId.name());
    
    //this is the subject string for the subject to assign to
    //e.g. sourceId :::::: subjectIdentifier
    //or sourceId :::: subjectId
    //or :::: subjectId
    //or sourceId ::::::: subjectIdOrIdentifier
    //etc
    attributeValueDelegate.assignValue(
        RuleUtils.ruleThenEnumArg0Name(), "g:gsa :::::: stem1:admins");
    
    //privileges to assign: read, admin, update, view, optin, optout
    attributeValueDelegate.assignValue(
        RuleUtils.ruleThenEnumArg1Name(), "read, update");
    
    //should be valid
    String isValidString = attributeValueDelegate.retrieveValueString(
        RuleUtils.ruleValidName());
  
    if (!StringUtils.equals("T", isValidString)) {
      throw new RuntimeException(isValidString);
    }
  
    long initialFirings = RuleEngine.ruleFirings;
    
    
    new GroupSave(grouperSession).assignName("stem2:b").assignCreateParentStemsIfNotExist(true).save();
  
    //count rule firings
    assertEquals(initialFirings, RuleEngine.ruleFirings);

    Group someGroup = new GroupSave(grouperSession).assignName("stem2:whatever:me_someGroup").assignCreateParentStemsIfNotExist(true).save();
    
    //count rule firings
    assertEquals(initialFirings+1, RuleEngine.ruleFirings);

    
    //make sure allowed
    assertTrue(someGroup.hasUpdate(SubjectTestHelper.SUBJ0));
    assertTrue(someGroup.hasRead(SubjectTestHelper.SUBJ0));
    
    
    Group groupD = new GroupSave(grouperSession).assignName("stem3:d").assignCreateParentStemsIfNotExist(true).save();
  
    assertEquals(initialFirings+1, RuleEngine.ruleFirings);
    
    assertFalse(groupD.hasUpdate(SubjectTestHelper.SUBJ0));
    assertFalse(groupD.hasRead(SubjectTestHelper.SUBJ0));
    
    
    new GroupSave(grouperSession).assignName("stem2:sub:c").assignCreateParentStemsIfNotExist(true).save();
  
    assertEquals(initialFirings+1, RuleEngine.ruleFirings);
  
    
  }
}
