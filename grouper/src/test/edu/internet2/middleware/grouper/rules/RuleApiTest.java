/**
 * 
 */
package edu.internet2.middleware.grouper.rules;

import java.util.Date;
import java.util.Set;

import junit.textui.TestRunner;

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
import edu.internet2.middleware.grouper.Stem.Scope;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.AttributeDefNameSave;
import edu.internet2.middleware.grouper.attr.AttributeDefSave;
import edu.internet2.middleware.grouper.attr.AttributeDefType;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.cfg.ApiConfig;
import edu.internet2.middleware.grouper.group.TypeOfGroup;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.SaveMode;
import edu.internet2.middleware.grouper.permissions.PermissionEntry;
import edu.internet2.middleware.grouper.permissions.role.Role;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.Privilege;
import edu.internet2.middleware.subject.Subject;


/**
 * test rule api
 * @author mchyzer
 */
public class RuleApiTest extends GrouperTest {

  /**
   * @param name
   */
  public RuleApiTest(String name) {
    super(name);
  }

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new RuleApiTest("testRuleLonghandPermissionAssignment"));
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
  public void testGroupIntersection() {
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Group groupA = new GroupSave(grouperSession).assignSaveMode(SaveMode.INSERT_OR_UPDATE).assignName("stem:a").assignCreateParentStemsIfNotExist(true).save();
    groupA.grantPriv(SubjectTestHelper.SUBJ9, AccessPrivilege.ADMIN, false);

    Group groupB = new GroupSave(grouperSession).assignSaveMode(SaveMode.INSERT_OR_UPDATE).assignName("stem:b").assignCreateParentStemsIfNotExist(true).save();
    groupB.grantPriv(SubjectTestHelper.SUBJ9, AccessPrivilege.READ, false);
    
    RuleApi.groupIntersection(SubjectTestHelper.SUBJ9, groupA, groupB);
    
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

    //should come out of groupA
    assertFalse(groupA.hasMember(SubjectTestHelper.SUBJ0));

    
    // grouperSession = GrouperSession.startRootSession();
    // groupA = new GroupSave(grouperSession).assignName("stem:a").assignCreateParentStemsIfNotExist(true).save();
    // actAsSubject = SubjectFinder.findById("test.subject.9", true);
    // groupA.grantPriv(actAsSubject, AccessPrivilege.ADMIN, false);
    // groupB = new GroupSave(grouperSession).assignName("stem:b").assignCreateParentStemsIfNotExist(true).save();
    // groupB.grantPriv(actAsSubject, AccessPrivilege.READ, false);
    // RuleApi.groupIntersection(actAsSubject, groupA, groupB);
    // addMember("stem:a", "test.subject.0");
    // addMember("stem:b", "test.subject.0");
    // delMember("stem:b", "test.subject.0");
    // hasMember("stem:a", "test.subject.0");

  }

  /**
   * 
   */
  public void testRuleApiEmailTemplate() {
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
        RuleUtils.ruleCheckTypeName(), 
        RuleCheckType.membershipAdd.name());
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleThenEnumName(), RuleThenEnum.sendEmail.name());
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleThenEnumArg0Name(), "mchyzer@isc.upenn.edu, ${safeSubject.emailAddress}"); // ${subjectEmail}
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
  public void testGroupIntersectionDate() {
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Group groupA = new GroupSave(grouperSession).assignSaveMode(SaveMode.INSERT_OR_UPDATE).assignName("stem:a").assignCreateParentStemsIfNotExist(true).save();
    groupA.grantPriv(SubjectTestHelper.SUBJ9, AccessPrivilege.ADMIN, false);
  
    Group groupB = new GroupSave(grouperSession).assignSaveMode(SaveMode.INSERT_OR_UPDATE).assignName("stem:b").assignCreateParentStemsIfNotExist(true).save();
    groupB.grantPriv(SubjectTestHelper.SUBJ9, AccessPrivilege.READ, false);
    
    RuleApi.groupIntersection(SubjectTestHelper.SUBJ9, groupA, groupB, 5);
    
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
  
    //should have a disabled date in group A
    assertTrue(groupA.hasMember(SubjectTestHelper.SUBJ0));

    Member member0 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ0, false);
    
    Membership membership = groupA.getImmediateMembership(Group.getDefaultList(), member0, true, true);
    
    assertNotNull(membership.getDisabledTime());
    long disabledTime = membership.getDisabledTime().getTime();
    
    assertTrue("More than 6 days: " + new Date(disabledTime), disabledTime > System.currentTimeMillis() + (4 * 24 * 60 * 60 * 1000));
    assertTrue("Less than 8 days: " + new Date(disabledTime), disabledTime < System.currentTimeMillis() + (6 * 24 * 60 * 60 * 1000));

    
    // grouperSession = GrouperSession.startRootSession();
    // groupA = new GroupSave(grouperSession).assignName("stem:a").assignCreateParentStemsIfNotExist(true).save();
    // actAsSubject = SubjectFinder.findById("test.subject.9", true);
    // groupA.grantPriv(actAsSubject, AccessPrivilege.ADMIN, false);
    // groupB = new GroupSave(grouperSession).assignName("stem:b").assignCreateParentStemsIfNotExist(true).save();
    // groupB.grantPriv(actAsSubject, AccessPrivilege.READ, false);
    // RuleApi.groupIntersection(actAsSubject, groupA, groupB, 7);
    // addMember("stem:a", "test.subject.0");
    // addMember("stem:b", "test.subject.0");
    // delMember("stem:b", "test.subject.0");
    // subject0 = SubjectFinder.findById("test.subject.0", true);
    // member0 = MemberFinder.findBySubject(grouperSession, subject0, false);
    // groupA.getImmediateMembership(Group.getDefaultList(), member0, true, true).getDisabledTime()
  
  }
  
  /**
   * 
   */
  public void testRuleVeto() {
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Group ruleGroup = new GroupSave(grouperSession).assignName("stem:a").assignCreateParentStemsIfNotExist(true).save();
    Group mustBeInGroup = new GroupSave(grouperSession).assignName("stem:b").assignCreateParentStemsIfNotExist(true).save();
    Subject actAsSubject = SubjectFinder.findByIdAndSource("GrouperSystem", "g:isa", true);
    
    RuleApi.vetoMembershipIfNotIfGroup(actAsSubject, ruleGroup, mustBeInGroup, 
        "rule.entity.must.be.a.member.of.stem.b", "Entity cannot be a member of stem:a if not a member of stem:b");
    
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
  public void testInheritGroupPrivileges() {
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Stem stem2 = new StemSave(grouperSession).assignName("stem2").assignCreateParentStemsIfNotExist(true).save();

    Group groupA = new GroupSave(grouperSession).assignName("stem1:admins").assignCreateParentStemsIfNotExist(true).save();

    groupA.addMember(SubjectTestHelper.SUBJ0);
    
    RuleApi.inheritGroupPrivileges(SubjectFinder.findRootSubject(), stem2, Scope.SUB, groupA.toSubject(), Privilege.getInstances("read, update"));

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
   * 
   */
  public void testInheritFolderPrivileges() {
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Stem stem2 = new StemSave(grouperSession).assignName("stem2").assignCreateParentStemsIfNotExist(true).save();
  
    Group groupA = new GroupSave(grouperSession).assignName("stem1:admins").assignCreateParentStemsIfNotExist(true).save();
  
    groupA.addMember(SubjectTestHelper.SUBJ0);
    
    RuleApi.inheritFolderPrivileges(SubjectFinder.findRootSubject(), stem2, Scope.SUB, groupA.toSubject(), Privilege.getInstances("stem, create"));
  
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
  public void testGroupIntersectionFolder() {
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Group groupA = new GroupSave(grouperSession).assignName("stem1:a").assignCreateParentStemsIfNotExist(true).save();
    Group groupB = new GroupSave(grouperSession).assignName("stem2:b").assignCreateParentStemsIfNotExist(true).save();
    Group groupC = new GroupSave(grouperSession).assignName("stem2:sub:c").assignCreateParentStemsIfNotExist(true).save();
    
    Stem stem = StemFinder.findByName(grouperSession, "stem2", true);
    
    RuleApi.groupIntersectionWithFolder(SubjectFinder.findRootSubject(), groupA, stem, Scope.SUB);
    
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
  public void testInheritAttributeDefPrivileges() {
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Stem stem2 = new StemSave(grouperSession).assignName("stem2").assignCreateParentStemsIfNotExist(true).save();
  
    Group groupA = new GroupSave(grouperSession).assignName("stem1:admins").assignCreateParentStemsIfNotExist(true).save();
  
    groupA.addMember(SubjectTestHelper.SUBJ0);
    
    RuleApi.inheritAttributeDefPrivileges(SubjectFinder.findRootSubject(), stem2, Scope.SUB, groupA.toSubject(), Privilege.getInstances("attrRead, attrUpdate"));
  
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
    
    RuleApi.permissionGroupIntersection(SubjectFinder.findRootSubject(), permissionDef, groupEmployee);

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

}
