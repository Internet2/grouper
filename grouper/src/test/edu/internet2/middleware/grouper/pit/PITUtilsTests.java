package edu.internet2.middleware.grouper.pit;

import java.util.Date;

import junit.textui.TestRunner;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupType;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.MembershipFinder;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.AttributeDefNameSet;
import edu.internet2.middleware.grouper.attr.AttributeDefType;
import edu.internet2.middleware.grouper.attr.AttributeDefValueType;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignAction;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignActionSet;
import edu.internet2.middleware.grouper.attr.value.AttributeAssignValue;
import edu.internet2.middleware.grouper.changeLog.ChangeLogTempToEntity;
import edu.internet2.middleware.grouper.group.GroupSet;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SessionHelper;
import edu.internet2.middleware.grouper.helper.StemHelper;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.permissions.role.RoleSet;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * @author shilen
 * $Id$
 */
public class PITUtilsTests extends GrouperTest {
  
  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new PITUtilsTests("testDeleteInactiveRecords"));
  }
  
  /** top level stem */
  private Stem edu;

  /** root session */
  private GrouperSession grouperSession;
  
  /** root stem */
  private Stem root;
  
  /** amount of time to sleep between operations */
  private long sleepTime = 100;
  
  /**
   * @param name
   */
  public PITUtilsTests(String name) {
    super(name);
  }
  
  /**
   * @see edu.internet2.middleware.grouper.helper.GrouperTest#setUp()
   */
  @Override
  protected void setUp() {
    super.setUp();
    
    grouperSession     = SessionHelper.getRootSession();
    root  = StemHelper.findRootStem(grouperSession);
    edu   = StemHelper.addChildStem(root, "edu", "education");
  }

  /**
   * @see edu.internet2.middleware.grouper.helper.GrouperTest#tearDown()
   */
  @Override
  protected void tearDown() {
    super.tearDown();
  }
  
  /**
   * 
   * @return the date
   */
  private Date getDateWithSleep() {
    GrouperUtil.sleep(sleepTime);
    Date date = new Date();
    GrouperUtil.sleep(sleepTime);
    return date;
  }
  
  /**
   * 
   */
  public void testDeleteInactiveRecords() {
    GroupType type = GroupType.createType(grouperSession, "testType");

    // data set 1: before cleanup time but we will not delete these objects
    Stem stem1 = edu.addChildStem("stem1", "stem1");
    Group role1 = (Group) stem1.addChildRole("role1", "role1");
    GroupSet groupSet1 = GrouperDAOFactory.getFactory().getGroupSet().findSelfGroup(role1.getId(), Group.getDefaultList().getUuid());
    RoleSet roleSet1 = GrouperDAOFactory.getFactory().getRoleSet().findByIfHasRoleId(role1.getId()).iterator().next();
    AttributeDef attributeDef1 = edu.addChildAttributeDef("attributeDef1", AttributeDefType.attr);
    attributeDef1.setAssignToGroup(true);
    attributeDef1.setValueType(AttributeDefValueType.string);
    attributeDef1.store();
    AttributeDefName attributeDefName1 = edu.addChildAttributeDefName(attributeDef1, "testAttribute1", "testAttribute1");
    AttributeDefNameSet attributeDefNameSet1 = GrouperDAOFactory.getFactory().getAttributeDefNameSet().findByIfHasAttributeDefNameId(attributeDefName1.getId()).iterator().next();
    AttributeAssignAction action1 = attributeDef1.getAttributeDefActionDelegate().addAction("testAction1");
    AttributeAssignActionSet actionSet1 = GrouperDAOFactory.getFactory().getAttributeAssignActionSet().findByIfHasAttributeAssignActionId(action1.getId()).iterator().next();
    AttributeAssign attributeAssign1 = role1.getAttributeDelegate().assignAttribute(action1.getName(), attributeDefName1).getAttributeAssign();
    AttributeAssignValue value1 = attributeAssign1.getValueDelegate().assignValue("test1").getAttributeAssignValue();
    Field field1 = type.addList(grouperSession, "list1", AccessPrivilege.READ, AccessPrivilege.UPDATE);
    Member member1 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ1, true);
    role1.addType(type);
    role1.addMember(member1.getSubject(), field1);
    Membership membership1 = MembershipFinder.findImmediateMembership(grouperSession, role1, member1.getSubject(), field1, true);
    ChangeLogTempToEntity.convertRecords();

    
    // data set 2: before cleanup time and we will delete these objects
    Stem stem2 = edu.addChildStem("stem2", "stem2");
    Group role2 = (Group) stem2.addChildRole("role2", "role2");
    GroupSet groupSet2 = GrouperDAOFactory.getFactory().getGroupSet().findSelfGroup(role2.getId(), Group.getDefaultList().getUuid());
    RoleSet roleSet2 = GrouperDAOFactory.getFactory().getRoleSet().findByIfHasRoleId(role2.getId()).iterator().next();
    AttributeDef attributeDef2 = edu.addChildAttributeDef("attributeDef2", AttributeDefType.attr);
    attributeDef2.setAssignToGroup(true);
    attributeDef2.setValueType(AttributeDefValueType.string);
    attributeDef2.store();
    AttributeDefName attributeDefName2 = edu.addChildAttributeDefName(attributeDef2, "testAttribute2", "testAttribute2");
    AttributeDefNameSet attributeDefNameSet2 = GrouperDAOFactory.getFactory().getAttributeDefNameSet().findByIfHasAttributeDefNameId(attributeDefName2.getId()).iterator().next();
    AttributeAssignAction action2 = attributeDef2.getAttributeDefActionDelegate().addAction("testAction2");
    AttributeAssignActionSet actionSet2 = GrouperDAOFactory.getFactory().getAttributeAssignActionSet().findByIfHasAttributeAssignActionId(action2.getId()).iterator().next();
    AttributeAssign attributeAssign2 = role2.getAttributeDelegate().assignAttribute(action2.getName(), attributeDefName2).getAttributeAssign();
    AttributeAssignValue value2 = attributeAssign2.getValueDelegate().assignValue("test2").getAttributeAssignValue();
    Field field2 = type.addList(grouperSession, "list2", AccessPrivilege.READ, AccessPrivilege.UPDATE);
    Member member2 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ2, true);
    role2.addType(type);
    role2.addMember(member2.getSubject(), field2);
    Membership membership2 = MembershipFinder.findImmediateMembership(grouperSession, role2, member2.getSubject(), field2, true);
    ChangeLogTempToEntity.convertRecords();

    
    // now delete data set 2
    role2.deleteMember(member2, field2);
    role2.deleteType(type);
    HibernateSession.byObjectStatic().delete(member2);
    HibernateSession.byObjectStatic().delete(field2);
    value2.delete();
    action2.delete();
    attributeDefName2.delete();
    attributeDef2.delete();
    role2.delete();
    stem2.delete();
    ChangeLogTempToEntity.convertRecords();

    
    // grab the time
    Date cleanupDate = getDateWithSleep();
    

    // data set 3: after cleanup time and we will not delete these objects
    Stem stem3 = edu.addChildStem("stem3", "stem3");
    Group role3 = (Group) stem3.addChildRole("role3", "role3");
    GroupSet groupSet3 = GrouperDAOFactory.getFactory().getGroupSet().findSelfGroup(role3.getId(), Group.getDefaultList().getUuid());
    RoleSet roleSet3 = GrouperDAOFactory.getFactory().getRoleSet().findByIfHasRoleId(role3.getId()).iterator().next();
    AttributeDef attributeDef3 = edu.addChildAttributeDef("attributeDef3", AttributeDefType.attr);
    attributeDef3.setAssignToGroup(true);
    attributeDef3.setValueType(AttributeDefValueType.string);
    attributeDef3.store();
    AttributeDefName attributeDefName3 = edu.addChildAttributeDefName(attributeDef3, "testAttribute3", "testAttribute3");
    AttributeDefNameSet attributeDefNameSet3 = GrouperDAOFactory.getFactory().getAttributeDefNameSet().findByIfHasAttributeDefNameId(attributeDefName3.getId()).iterator().next();
    AttributeAssignAction action3 = attributeDef3.getAttributeDefActionDelegate().addAction("testAction3");
    AttributeAssignActionSet actionSet3 = GrouperDAOFactory.getFactory().getAttributeAssignActionSet().findByIfHasAttributeAssignActionId(action3.getId()).iterator().next();
    AttributeAssign attributeAssign3 = role3.getAttributeDelegate().assignAttribute(action3.getName(), attributeDefName3).getAttributeAssign();
    AttributeAssignValue value3 = attributeAssign3.getValueDelegate().assignValue("test3").getAttributeAssignValue();
    Field field3 = type.addList(grouperSession, "list3", AccessPrivilege.READ, AccessPrivilege.UPDATE);
    Member member3 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ3, true);
    role3.addType(type);
    role3.addMember(member3.getSubject(), field3);
    Membership membership3 = MembershipFinder.findImmediateMembership(grouperSession, role3, member3.getSubject(), field3, true);
    ChangeLogTempToEntity.convertRecords();

    
    // data set 4: after cleanup time and we will delete these objects
    Stem stem4 = edu.addChildStem("stem4", "stem4");
    Group role4 = (Group) stem4.addChildRole("role4", "role4");
    GroupSet groupSet4 = GrouperDAOFactory.getFactory().getGroupSet().findSelfGroup(role4.getId(), Group.getDefaultList().getUuid());
    RoleSet roleSet4 = GrouperDAOFactory.getFactory().getRoleSet().findByIfHasRoleId(role4.getId()).iterator().next();
    AttributeDef attributeDef4 = edu.addChildAttributeDef("attributeDef4", AttributeDefType.attr);
    attributeDef4.setAssignToGroup(true);
    attributeDef4.setValueType(AttributeDefValueType.string);
    attributeDef4.store();
    AttributeDefName attributeDefName4 = edu.addChildAttributeDefName(attributeDef4, "testAttribute4", "testAttribute4");
    AttributeDefNameSet attributeDefNameSet4 = GrouperDAOFactory.getFactory().getAttributeDefNameSet().findByIfHasAttributeDefNameId(attributeDefName4.getId()).iterator().next();
    AttributeAssignAction action4 = attributeDef4.getAttributeDefActionDelegate().addAction("testAction4");
    AttributeAssignActionSet actionSet4 = GrouperDAOFactory.getFactory().getAttributeAssignActionSet().findByIfHasAttributeAssignActionId(action4.getId()).iterator().next();
    AttributeAssign attributeAssign4 = role4.getAttributeDelegate().assignAttribute(action4.getName(), attributeDefName4).getAttributeAssign();
    AttributeAssignValue value4 = attributeAssign4.getValueDelegate().assignValue("test4").getAttributeAssignValue();
    Field field4 = type.addList(grouperSession, "list4", AccessPrivilege.READ, AccessPrivilege.UPDATE);
    Member member4 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ4, true);
    role4.addType(type);
    role4.addMember(member4.getSubject(), field4);
    Membership membership4 = MembershipFinder.findImmediateMembership(grouperSession, role4, member4.getSubject(), field4, true);
    ChangeLogTempToEntity.convertRecords();
    
    
    // now delete data set 4
    role4.deleteMember(member4, field4);
    role4.deleteType(type);
    HibernateSession.byObjectStatic().delete(member4);
    HibernateSession.byObjectStatic().delete(field4);
    value4.delete();
    action4.delete();
    attributeDefName4.delete();
    attributeDef4.delete();
    role4.delete();
    stem4.delete();
    ChangeLogTempToEntity.convertRecords();
    
    
    // sleep
    GrouperUtil.sleep(sleepTime);
    
    // now delete old PIT records
    PITUtils.deleteInactiveRecords(cleanupDate);

    // verify that data set 1 was not deleted from PIT tables...
    assertNotNull(GrouperDAOFactory.getFactory().getPITAttributeAssign().findById(attributeAssign1.getId()));
    assertNotNull(GrouperDAOFactory.getFactory().getPITAttributeDef().findById(attributeDef1.getId()));
    assertNotNull(GrouperDAOFactory.getFactory().getPITAttributeAssignAction().findById(action1.getId()));
    assertNotNull(GrouperDAOFactory.getFactory().getPITAttributeAssignActionSet().findById(actionSet1.getId()));
    assertNotNull(GrouperDAOFactory.getFactory().getPITAttributeAssignValue().findById(value1.getId()));
    assertNotNull(GrouperDAOFactory.getFactory().getPITAttributeDefName().findById(attributeDefName1.getId()));
    assertNotNull(GrouperDAOFactory.getFactory().getPITAttributeDefNameSet().findById(attributeDefNameSet1.getId()));
    assertNotNull(GrouperDAOFactory.getFactory().getPITField().findById(field1.getUuid()));
    assertNotNull(GrouperDAOFactory.getFactory().getPITGroup().findById(role1.getUuid()));
    assertNotNull(GrouperDAOFactory.getFactory().getPITGroupSet().findById(groupSet1.getId()));
    assertNotNull(GrouperDAOFactory.getFactory().getPITMember().findById(member1.getUuid()));
    assertNotNull(GrouperDAOFactory.getFactory().getPITMembership().findById(membership1.getImmediateMembershipId()));
    assertNotNull(GrouperDAOFactory.getFactory().getPITRoleSet().findById(roleSet1.getId()));
    assertNotNull(GrouperDAOFactory.getFactory().getPITStem().findById(stem1.getUuid()));
    
    // now verify that data set 2 was deleted from PIT tables...
    assertNull(GrouperDAOFactory.getFactory().getPITAttributeAssign().findById(attributeAssign2.getId()));
    assertNull(GrouperDAOFactory.getFactory().getPITAttributeDef().findById(attributeDef2.getId()));
    assertNull(GrouperDAOFactory.getFactory().getPITAttributeAssignAction().findById(action2.getId()));
    assertNull(GrouperDAOFactory.getFactory().getPITAttributeAssignActionSet().findById(actionSet2.getId()));
    assertNull(GrouperDAOFactory.getFactory().getPITAttributeAssignValue().findById(value2.getId()));
    assertNull(GrouperDAOFactory.getFactory().getPITAttributeDefName().findById(attributeDefName2.getId()));
    assertNull(GrouperDAOFactory.getFactory().getPITAttributeDefNameSet().findById(attributeDefNameSet2.getId()));
    assertNull(GrouperDAOFactory.getFactory().getPITField().findById(field2.getUuid()));
    assertNull(GrouperDAOFactory.getFactory().getPITGroup().findById(role2.getUuid()));
    assertNull(GrouperDAOFactory.getFactory().getPITGroupSet().findById(groupSet2.getId()));
    assertNull(GrouperDAOFactory.getFactory().getPITMember().findById(member2.getUuid()));
    assertNull(GrouperDAOFactory.getFactory().getPITMembership().findById(membership2.getImmediateMembershipId()));
    assertNull(GrouperDAOFactory.getFactory().getPITRoleSet().findById(roleSet2.getId()));
    assertNull(GrouperDAOFactory.getFactory().getPITStem().findById(stem2.getUuid()));

    // now verify that data set 3 was not deleted from PIT tables...
    assertNotNull(GrouperDAOFactory.getFactory().getPITAttributeAssign().findById(attributeAssign3.getId()));
    assertNotNull(GrouperDAOFactory.getFactory().getPITAttributeDef().findById(attributeDef3.getId()));
    assertNotNull(GrouperDAOFactory.getFactory().getPITAttributeAssignAction().findById(action3.getId()));
    assertNotNull(GrouperDAOFactory.getFactory().getPITAttributeAssignActionSet().findById(actionSet3.getId()));
    assertNotNull(GrouperDAOFactory.getFactory().getPITAttributeAssignValue().findById(value3.getId()));
    assertNotNull(GrouperDAOFactory.getFactory().getPITAttributeDefName().findById(attributeDefName3.getId()));
    assertNotNull(GrouperDAOFactory.getFactory().getPITAttributeDefNameSet().findById(attributeDefNameSet3.getId()));
    assertNotNull(GrouperDAOFactory.getFactory().getPITField().findById(field3.getUuid()));
    assertNotNull(GrouperDAOFactory.getFactory().getPITGroup().findById(role3.getUuid()));
    assertNotNull(GrouperDAOFactory.getFactory().getPITGroupSet().findById(groupSet3.getId()));
    assertNotNull(GrouperDAOFactory.getFactory().getPITMember().findById(member3.getUuid()));
    assertNotNull(GrouperDAOFactory.getFactory().getPITMembership().findById(membership3.getImmediateMembershipId()));
    assertNotNull(GrouperDAOFactory.getFactory().getPITRoleSet().findById(roleSet3.getId()));
    assertNotNull(GrouperDAOFactory.getFactory().getPITStem().findById(stem3.getUuid()));
    
    // now verify that data set 4 was not deleted from PIT tables...
    assertNotNull(GrouperDAOFactory.getFactory().getPITAttributeAssign().findById(attributeAssign4.getId()));
    assertNotNull(GrouperDAOFactory.getFactory().getPITAttributeDef().findById(attributeDef4.getId()));
    assertNotNull(GrouperDAOFactory.getFactory().getPITAttributeAssignAction().findById(action4.getId()));
    assertNotNull(GrouperDAOFactory.getFactory().getPITAttributeAssignActionSet().findById(actionSet4.getId()));
    assertNotNull(GrouperDAOFactory.getFactory().getPITAttributeAssignValue().findById(value4.getId()));
    assertNotNull(GrouperDAOFactory.getFactory().getPITAttributeDefName().findById(attributeDefName4.getId()));
    assertNotNull(GrouperDAOFactory.getFactory().getPITAttributeDefNameSet().findById(attributeDefNameSet4.getId()));
    assertNotNull(GrouperDAOFactory.getFactory().getPITField().findById(field4.getUuid()));
    assertNotNull(GrouperDAOFactory.getFactory().getPITGroup().findById(role4.getUuid()));
    assertNotNull(GrouperDAOFactory.getFactory().getPITGroupSet().findById(groupSet4.getId()));
    assertNotNull(GrouperDAOFactory.getFactory().getPITMember().findById(member4.getUuid()));
    assertNotNull(GrouperDAOFactory.getFactory().getPITMembership().findById(membership4.getImmediateMembershipId()));
    assertNotNull(GrouperDAOFactory.getFactory().getPITRoleSet().findById(roleSet4.getId()));
    assertNotNull(GrouperDAOFactory.getFactory().getPITStem().findById(stem4.getUuid()));
  }
}