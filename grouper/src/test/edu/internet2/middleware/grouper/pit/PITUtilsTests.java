/**
 * Copyright 2014 Internet2
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
  
  /** top level stems */
  private Stem edu, edu2;

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
    edu2  = StemHelper.addChildStem(root, "edu2", "education2");
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
    AttributeDef attributeDef1 = stem1.addChildAttributeDef("attributeDef1", AttributeDefType.attr);
    attributeDef1.setAssignToGroup(true);
    attributeDef1.setValueType(AttributeDefValueType.string);
    attributeDef1.store();
    AttributeDefName attributeDefName1 = stem1.addChildAttributeDefName(attributeDef1, "testAttribute1", "testAttribute1");
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
    AttributeDef attributeDef2 = stem2.addChildAttributeDef("attributeDef2", AttributeDefType.attr);
    attributeDef2.setAssignToGroup(true);
    attributeDef2.setValueType(AttributeDefValueType.string);
    attributeDef2.store();
    AttributeDefName attributeDefName2 = stem2.addChildAttributeDefName(attributeDef2, "testAttribute2", "testAttribute2");
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
    AttributeDef attributeDef3 = stem3.addChildAttributeDef("attributeDef3", AttributeDefType.attr);
    attributeDef3.setAssignToGroup(true);
    attributeDef3.setValueType(AttributeDefValueType.string);
    attributeDef3.store();
    AttributeDefName attributeDefName3 = stem3.addChildAttributeDefName(attributeDef3, "testAttribute3", "testAttribute3");
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
    AttributeDef attributeDef4 = stem4.addChildAttributeDef("attributeDef4", AttributeDefType.attr);
    attributeDef4.setAssignToGroup(true);
    attributeDef4.setValueType(AttributeDefValueType.string);
    attributeDef4.store();
    AttributeDefName attributeDefName4 = stem4.addChildAttributeDefName(attributeDef4, "testAttribute4", "testAttribute4");
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
    PITUtils.deleteInactiveRecords(cleanupDate, false);

    // verify that data set 1 was not deleted from PIT tables...
    assertNotNull(GrouperDAOFactory.getFactory().getPITAttributeAssign().findBySourceIdUnique(attributeAssign1.getId(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getPITAttributeDef().findBySourceIdUnique(attributeDef1.getId(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getPITAttributeAssignAction().findBySourceIdUnique(action1.getId(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getPITAttributeAssignActionSet().findBySourceIdUnique(actionSet1.getId(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getPITAttributeAssignValue().findBySourceIdUnique(value1.getId(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getPITAttributeDefName().findBySourceIdUnique(attributeDefName1.getId(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getPITAttributeDefNameSet().findBySourceIdUnique(attributeDefNameSet1.getId(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getPITField().findBySourceIdUnique(field1.getUuid(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getPITGroup().findBySourceIdUnique(role1.getUuid(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getPITGroupSet().findBySourceIdUnique(groupSet1.getId(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getPITMember().findBySourceIdUnique(member1.getUuid(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getPITMembership().findBySourceIdUnique(membership1.getImmediateMembershipId(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getPITRoleSet().findBySourceIdUnique(roleSet1.getId(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getPITStem().findBySourceIdUnique(stem1.getUuid(), false));
    
    // now verify that data set 2 was deleted from PIT tables...
    assertNull(GrouperDAOFactory.getFactory().getPITAttributeAssign().findBySourceIdUnique(attributeAssign2.getId(), false));
    assertNull(GrouperDAOFactory.getFactory().getPITAttributeDef().findBySourceIdUnique(attributeDef2.getId(), false));
    assertNull(GrouperDAOFactory.getFactory().getPITAttributeAssignAction().findBySourceIdUnique(action2.getId(), false));
    assertNull(GrouperDAOFactory.getFactory().getPITAttributeAssignActionSet().findBySourceIdUnique(actionSet2.getId(), false));
    assertNull(GrouperDAOFactory.getFactory().getPITAttributeAssignValue().findBySourceIdUnique(value2.getId(), false));
    assertNull(GrouperDAOFactory.getFactory().getPITAttributeDefName().findBySourceIdUnique(attributeDefName2.getId(), false));
    assertNull(GrouperDAOFactory.getFactory().getPITAttributeDefNameSet().findBySourceIdUnique(attributeDefNameSet2.getId(), false));
    assertNull(GrouperDAOFactory.getFactory().getPITField().findBySourceIdUnique(field2.getUuid(), false));
    assertNull(GrouperDAOFactory.getFactory().getPITGroup().findBySourceIdUnique(role2.getUuid(), false));
    assertNull(GrouperDAOFactory.getFactory().getPITGroupSet().findBySourceIdUnique(groupSet2.getId(), false));
    assertNull(GrouperDAOFactory.getFactory().getPITMember().findBySourceIdUnique(member2.getUuid(), false));
    assertNull(GrouperDAOFactory.getFactory().getPITMembership().findBySourceIdUnique(membership2.getImmediateMembershipId(), false));
    assertNull(GrouperDAOFactory.getFactory().getPITRoleSet().findBySourceIdUnique(roleSet2.getId(), false));
    assertNull(GrouperDAOFactory.getFactory().getPITStem().findBySourceIdUnique(stem2.getUuid(), false));

    // now verify that data set 3 was not deleted from PIT tables...
    assertNotNull(GrouperDAOFactory.getFactory().getPITAttributeAssign().findBySourceIdUnique(attributeAssign3.getId(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getPITAttributeDef().findBySourceIdUnique(attributeDef3.getId(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getPITAttributeAssignAction().findBySourceIdUnique(action3.getId(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getPITAttributeAssignActionSet().findBySourceIdUnique(actionSet3.getId(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getPITAttributeAssignValue().findBySourceIdUnique(value3.getId(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getPITAttributeDefName().findBySourceIdUnique(attributeDefName3.getId(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getPITAttributeDefNameSet().findBySourceIdUnique(attributeDefNameSet3.getId(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getPITField().findBySourceIdUnique(field3.getUuid(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getPITGroup().findBySourceIdUnique(role3.getUuid(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getPITGroupSet().findBySourceIdUnique(groupSet3.getId(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getPITMember().findBySourceIdUnique(member3.getUuid(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getPITMembership().findBySourceIdUnique(membership3.getImmediateMembershipId(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getPITRoleSet().findBySourceIdUnique(roleSet3.getId(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getPITStem().findBySourceIdUnique(stem3.getUuid(), false));
    
    // now verify that data set 4 was not deleted from PIT tables...
    assertNotNull(GrouperDAOFactory.getFactory().getPITAttributeAssign().findBySourceIdUnique(attributeAssign4.getId(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getPITAttributeDef().findBySourceIdUnique(attributeDef4.getId(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getPITAttributeAssignAction().findBySourceIdUnique(action4.getId(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getPITAttributeAssignActionSet().findBySourceIdUnique(actionSet4.getId(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getPITAttributeAssignValue().findBySourceIdUnique(value4.getId(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getPITAttributeDefName().findBySourceIdUnique(attributeDefName4.getId(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getPITAttributeDefNameSet().findBySourceIdUnique(attributeDefNameSet4.getId(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getPITField().findBySourceIdUnique(field4.getUuid(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getPITGroup().findBySourceIdUnique(role4.getUuid(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getPITGroupSet().findBySourceIdUnique(groupSet4.getId(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getPITMember().findBySourceIdUnique(member4.getUuid(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getPITMembership().findBySourceIdUnique(membership4.getImmediateMembershipId(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getPITRoleSet().findBySourceIdUnique(roleSet4.getId(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getPITStem().findBySourceIdUnique(stem4.getUuid(), false));
  }
  
  /**
   * 
   */
  public void testDeleteInactiveGroup() {
    GroupType type = GroupType.createType(grouperSession, "testType");

    // data set 1
    Stem stem1 = edu.addChildStem("stem1", "stem1");
    Group role1a = (Group) stem1.addChildRole("role1a", "role1a");
    Group role1b = (Group) stem1.addChildRole("role1b", "role1b");
    Group role1c = (Group) stem1.addChildRole("role1c", "role1c");
    Group role1d = (Group) stem1.addChildRole("role1d", "role1d");
    RoleSet roleSet1b = GrouperDAOFactory.getFactory().getRoleSet().findByIfHasRoleId(role1b.getId()).iterator().next();
    role1a.addMember(role1b.toSubject());
    role1b.addMember(role1c.toSubject());
    role1c.addMember(role1d.toSubject());
    role1a.getRoleInheritanceDelegate().addRoleToInheritFromThis(role1b);
    role1b.getRoleInheritanceDelegate().addRoleToInheritFromThis(role1c);
    role1c.getRoleInheritanceDelegate().addRoleToInheritFromThis(role1d);
    GroupSet groupSet1b = GrouperDAOFactory.getFactory().getGroupSet().findSelfGroup(role1b.getId(), Group.getDefaultList().getUuid());
    GroupSet groupSet1aTo1b = GrouperDAOFactory.getFactory().getGroupSet().findImmediateByOwnerGroupAndMemberGroupAndField(role1a.getId(), role1b.getId(), Group.getDefaultList());
    GroupSet groupSet1cTo1d = GrouperDAOFactory.getFactory().getGroupSet().findImmediateByOwnerGroupAndMemberGroupAndField(role1c.getId(), role1d.getId(), Group.getDefaultList());
    RoleSet roleSet1aTo1b = GrouperDAOFactory.getFactory().getRoleSet().findByIfThenImmediate(role1a.getId(), role1b.getId(), true);
    RoleSet roleSet1cTo1d = GrouperDAOFactory.getFactory().getRoleSet().findByIfThenImmediate(role1c.getId(), role1d.getId(), true);
    AttributeDef attributeDef1 = edu.addChildAttributeDef("attributeDef1", AttributeDefType.attr);
    attributeDef1.setAssignToGroup(true);
    attributeDef1.setValueType(AttributeDefValueType.string);
    attributeDef1.store();
    AttributeDefName attributeDefName1a = edu.addChildAttributeDefName(attributeDef1, "testAttribute1a", "testAttribute1a");
    AttributeDefName attributeDefName1b = edu.addChildAttributeDefName(attributeDef1, "testAttribute1b", "testAttribute1b");
    AttributeDefName attributeDefName1c = edu.addChildAttributeDefName(attributeDef1, "testAttribute1c", "testAttribute1c");
    AttributeDefName attributeDefName1d = edu.addChildAttributeDefName(attributeDef1, "testAttribute1d", "testAttribute1d");
    AttributeDefNameSet attributeDefNameSet1b = GrouperDAOFactory.getFactory().getAttributeDefNameSet().findByIfHasAttributeDefNameId(attributeDefName1b.getId()).iterator().next();
    attributeDefName1a.getAttributeDefNameSetDelegate().addToAttributeDefNameSet(attributeDefName1b);
    attributeDefName1b.getAttributeDefNameSetDelegate().addToAttributeDefNameSet(attributeDefName1c);
    attributeDefName1c.getAttributeDefNameSetDelegate().addToAttributeDefNameSet(attributeDefName1d);
    AttributeDefNameSet attributeDefNameSet1aTo1b = GrouperDAOFactory.getFactory().getAttributeDefNameSet().findByIfThenImmediate(attributeDefName1a.getId(), attributeDefName1b.getId(), true);
    AttributeDefNameSet attributeDefNameSet1cTo1d = GrouperDAOFactory.getFactory().getAttributeDefNameSet().findByIfThenImmediate(attributeDefName1c.getId(), attributeDefName1d.getId(), true);
    AttributeAssignAction action1a = attributeDef1.getAttributeDefActionDelegate().addAction("testAction1a");
    AttributeAssignAction action1b = attributeDef1.getAttributeDefActionDelegate().addAction("testAction1b");
    AttributeAssignAction action1c = attributeDef1.getAttributeDefActionDelegate().addAction("testAction1c");
    AttributeAssignAction action1d = attributeDef1.getAttributeDefActionDelegate().addAction("testAction1d");
    AttributeAssignActionSet actionSet1b = GrouperDAOFactory.getFactory().getAttributeAssignActionSet().findByIfHasAttributeAssignActionId(action1b.getId()).iterator().next();
    action1a.getAttributeAssignActionSetDelegate().addToAttributeAssignActionSet(action1b);
    action1b.getAttributeAssignActionSetDelegate().addToAttributeAssignActionSet(action1c);
    action1c.getAttributeAssignActionSetDelegate().addToAttributeAssignActionSet(action1d);
    AttributeAssignActionSet actionSet1aTo1b = GrouperDAOFactory.getFactory().getAttributeAssignActionSet().findByIfThenImmediate(action1a.getId(), action1b.getId(), true);
    AttributeAssignActionSet actionSet1cTo1d = GrouperDAOFactory.getFactory().getAttributeAssignActionSet().findByIfThenImmediate(action1c.getId(), action1d.getId(), true);
    AttributeAssign attributeAssign1 = role1b.getAttributeDelegate().assignAttribute(action1b.getName(), attributeDefName1b).getAttributeAssign();
    AttributeAssignValue value1 = attributeAssign1.getValueDelegate().assignValue("test1").getAttributeAssignValue();
    Field field1 = type.addList(grouperSession, "list1", AccessPrivilege.READ, AccessPrivilege.UPDATE);
    Member member1 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ1, true);
    role1b.addType(type);
    role1b.addMember(member1.getSubject(), field1);
    Membership membership1 = MembershipFinder.findImmediateMembership(grouperSession, role1b, member1.getSubject(), field1, true);
    Membership membership1aTo1b = MembershipFinder.findImmediateMembership(grouperSession, role1a, role1b.toSubject(), Group.getDefaultList(), true);
    Membership membership1cTo1d = MembershipFinder.findImmediateMembership(grouperSession, role1c, role1d.toSubject(), Group.getDefaultList(), true);
    ChangeLogTempToEntity.convertRecords();

    // data set 2
    Stem stem2 = edu.addChildStem("stem2", "stem2");
    Group role2a = (Group) stem2.addChildRole("role2a", "role2a");
    Group role2b = (Group) stem2.addChildRole("role2b", "role2b");
    Group role2c = (Group) stem2.addChildRole("role2c", "role2c");
    Group role2d = (Group) stem2.addChildRole("role2d", "role2d");
    RoleSet roleSet2b = GrouperDAOFactory.getFactory().getRoleSet().findByIfHasRoleId(role2b.getId()).iterator().next();
    role2a.addMember(role2b.toSubject());
    role2b.addMember(role2c.toSubject());
    role2c.addMember(role2d.toSubject());
    role2a.getRoleInheritanceDelegate().addRoleToInheritFromThis(role2b);
    role2b.getRoleInheritanceDelegate().addRoleToInheritFromThis(role2c);
    role2c.getRoleInheritanceDelegate().addRoleToInheritFromThis(role2d);
    GroupSet groupSet2b = GrouperDAOFactory.getFactory().getGroupSet().findSelfGroup(role2b.getId(), Group.getDefaultList().getUuid());
    GroupSet groupSet2aTo2b = GrouperDAOFactory.getFactory().getGroupSet().findImmediateByOwnerGroupAndMemberGroupAndField(role2a.getId(), role2b.getId(), Group.getDefaultList());
    GroupSet groupSet2cTo2d = GrouperDAOFactory.getFactory().getGroupSet().findImmediateByOwnerGroupAndMemberGroupAndField(role2c.getId(), role2d.getId(), Group.getDefaultList());
    RoleSet roleSet2aTo2b = GrouperDAOFactory.getFactory().getRoleSet().findByIfThenImmediate(role2a.getId(), role2b.getId(), true);
    RoleSet roleSet2cTo2d = GrouperDAOFactory.getFactory().getRoleSet().findByIfThenImmediate(role2c.getId(), role2d.getId(), true);
    AttributeDef attributeDef2 = edu.addChildAttributeDef("attributeDef2", AttributeDefType.attr);
    attributeDef2.setAssignToGroup(true);
    attributeDef2.setValueType(AttributeDefValueType.string);
    attributeDef2.store();
    AttributeDefName attributeDefName2a = edu.addChildAttributeDefName(attributeDef2, "testAttribute2a", "testAttribute2a");
    AttributeDefName attributeDefName2b = edu.addChildAttributeDefName(attributeDef2, "testAttribute2b", "testAttribute2b");
    AttributeDefName attributeDefName2c = edu.addChildAttributeDefName(attributeDef2, "testAttribute2c", "testAttribute2c");
    AttributeDefName attributeDefName2d = edu.addChildAttributeDefName(attributeDef2, "testAttribute2d", "testAttribute2d");
    AttributeDefNameSet attributeDefNameSet2b = GrouperDAOFactory.getFactory().getAttributeDefNameSet().findByIfHasAttributeDefNameId(attributeDefName2b.getId()).iterator().next();
    attributeDefName2a.getAttributeDefNameSetDelegate().addToAttributeDefNameSet(attributeDefName2b);
    attributeDefName2b.getAttributeDefNameSetDelegate().addToAttributeDefNameSet(attributeDefName2c);
    attributeDefName2c.getAttributeDefNameSetDelegate().addToAttributeDefNameSet(attributeDefName2d);
    AttributeDefNameSet attributeDefNameSet2aTo2b = GrouperDAOFactory.getFactory().getAttributeDefNameSet().findByIfThenImmediate(attributeDefName2a.getId(), attributeDefName2b.getId(), true);
    AttributeDefNameSet attributeDefNameSet2cTo2d = GrouperDAOFactory.getFactory().getAttributeDefNameSet().findByIfThenImmediate(attributeDefName2c.getId(), attributeDefName2d.getId(), true);
    AttributeAssignAction action2a = attributeDef2.getAttributeDefActionDelegate().addAction("testAction2a");
    AttributeAssignAction action2b = attributeDef2.getAttributeDefActionDelegate().addAction("testAction2b");
    AttributeAssignAction action2c = attributeDef2.getAttributeDefActionDelegate().addAction("testAction2c");
    AttributeAssignAction action2d = attributeDef2.getAttributeDefActionDelegate().addAction("testAction2d");
    AttributeAssignActionSet actionSet2b = GrouperDAOFactory.getFactory().getAttributeAssignActionSet().findByIfHasAttributeAssignActionId(action2b.getId()).iterator().next();
    action2a.getAttributeAssignActionSetDelegate().addToAttributeAssignActionSet(action2b);
    action2b.getAttributeAssignActionSetDelegate().addToAttributeAssignActionSet(action2c);
    action2c.getAttributeAssignActionSetDelegate().addToAttributeAssignActionSet(action2d);
    AttributeAssignActionSet actionSet2aTo2b = GrouperDAOFactory.getFactory().getAttributeAssignActionSet().findByIfThenImmediate(action2a.getId(), action2b.getId(), true);
    AttributeAssignActionSet actionSet2cTo2d = GrouperDAOFactory.getFactory().getAttributeAssignActionSet().findByIfThenImmediate(action2c.getId(), action2d.getId(), true);
    AttributeAssign attributeAssign2 = role2b.getAttributeDelegate().assignAttribute(action2b.getName(), attributeDefName2b).getAttributeAssign();
    AttributeAssignValue value2 = attributeAssign2.getValueDelegate().assignValue("test2").getAttributeAssignValue();
    Field field2 = type.addList(grouperSession, "list2", AccessPrivilege.READ, AccessPrivilege.UPDATE);
    Member member2 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ2, true);
    role2b.addType(type);
    role2b.addMember(member2.getSubject(), field2);
    Membership membership2 = MembershipFinder.findImmediateMembership(grouperSession, role2b, member2.getSubject(), field2, true);
    Membership membership2aTo2b = MembershipFinder.findImmediateMembership(grouperSession, role2a, role2b.toSubject(), Group.getDefaultList(), true);
    Membership membership2cTo2d = MembershipFinder.findImmediateMembership(grouperSession, role2c, role2d.toSubject(), Group.getDefaultList(), true);
    ChangeLogTempToEntity.convertRecords();
    
    // now delete data from data set 2
    role2b.deleteMember(member2, field2);
    role2b.deleteType(type);
    HibernateSession.byObjectStatic().delete(member2);
    HibernateSession.byObjectStatic().delete(field2);
    value2.delete();
    action2a.delete();
    action2b.delete();
    action2c.delete();
    action2d.delete();
    attributeDefName2a.delete();
    attributeDefName2b.delete();
    attributeDefName2c.delete();
    attributeDefName2d.delete();
    attributeDef2.delete();
    role2a.getRoleInheritanceDelegate().removeRoleFromInheritFromThis(role2b);
    role2b.getRoleInheritanceDelegate().removeRoleFromInheritFromThis(role2c);
    role2c.getRoleInheritanceDelegate().removeRoleFromInheritFromThis(role2d);
    role2a.delete();
    role2b.delete();
    role2c.delete();
    role2d.delete();
    stem2.delete();
    ChangeLogTempToEntity.convertRecords();
    
    // now delete old PIT data
    PITUtils.deleteInactiveGroup(role2b.getName());

    // verify data set 1...
    assertNotNull(GrouperDAOFactory.getFactory().getPITAttributeAssign().findBySourceIdUnique(attributeAssign1.getId(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getPITAttributeDef().findBySourceIdUnique(attributeDef1.getId(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getPITAttributeAssignAction().findBySourceIdUnique(action1a.getId(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getPITAttributeAssignAction().findBySourceIdUnique(action1b.getId(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getPITAttributeAssignAction().findBySourceIdUnique(action1c.getId(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getPITAttributeAssignAction().findBySourceIdUnique(action1d.getId(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getPITAttributeAssignActionSet().findBySourceIdUnique(actionSet1b.getId(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getPITAttributeAssignActionSet().findBySourceIdUnique(actionSet1aTo1b.getId(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getPITAttributeAssignActionSet().findBySourceIdUnique(actionSet1cTo1d.getId(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getPITAttributeAssignValue().findBySourceIdUnique(value1.getId(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getPITAttributeDefName().findBySourceIdUnique(attributeDefName1a.getId(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getPITAttributeDefName().findBySourceIdUnique(attributeDefName1b.getId(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getPITAttributeDefName().findBySourceIdUnique(attributeDefName1c.getId(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getPITAttributeDefName().findBySourceIdUnique(attributeDefName1d.getId(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getPITAttributeDefNameSet().findBySourceIdUnique(attributeDefNameSet1b.getId(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getPITAttributeDefNameSet().findBySourceIdUnique(attributeDefNameSet1aTo1b.getId(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getPITAttributeDefNameSet().findBySourceIdUnique(attributeDefNameSet1cTo1d.getId(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getPITField().findBySourceIdUnique(field1.getUuid(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getPITGroup().findBySourceIdUnique(role1a.getUuid(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getPITGroup().findBySourceIdUnique(role1b.getUuid(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getPITGroup().findBySourceIdUnique(role1c.getUuid(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getPITGroup().findBySourceIdUnique(role1d.getUuid(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getPITGroupSet().findBySourceIdUnique(groupSet1b.getId(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getPITGroupSet().findBySourceIdUnique(groupSet1aTo1b.getId(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getPITGroupSet().findBySourceIdUnique(groupSet1cTo1d.getId(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getPITMember().findBySourceIdUnique(member1.getUuid(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getPITMembership().findBySourceIdUnique(membership1.getImmediateMembershipId(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getPITMembership().findBySourceIdUnique(membership1aTo1b.getImmediateMembershipId(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getPITMembership().findBySourceIdUnique(membership1cTo1d.getImmediateMembershipId(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getPITRoleSet().findBySourceIdUnique(roleSet1b.getId(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getPITRoleSet().findBySourceIdUnique(roleSet1aTo1b.getId(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getPITRoleSet().findBySourceIdUnique(roleSet1cTo1d.getId(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getPITStem().findBySourceIdUnique(stem1.getUuid(), false));
    
    // verify data set 2...
    assertNull(GrouperDAOFactory.getFactory().getPITAttributeAssign().findBySourceIdUnique(attributeAssign2.getId(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getPITAttributeDef().findBySourceIdUnique(attributeDef2.getId(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getPITAttributeAssignAction().findBySourceIdUnique(action2a.getId(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getPITAttributeAssignAction().findBySourceIdUnique(action2b.getId(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getPITAttributeAssignAction().findBySourceIdUnique(action2c.getId(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getPITAttributeAssignAction().findBySourceIdUnique(action2d.getId(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getPITAttributeAssignActionSet().findBySourceIdUnique(actionSet2b.getId(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getPITAttributeAssignActionSet().findBySourceIdUnique(actionSet2aTo2b.getId(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getPITAttributeAssignActionSet().findBySourceIdUnique(actionSet2cTo2d.getId(), false));
    assertNull(GrouperDAOFactory.getFactory().getPITAttributeAssignValue().findBySourceIdUnique(value2.getId(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getPITAttributeDefName().findBySourceIdUnique(attributeDefName2a.getId(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getPITAttributeDefName().findBySourceIdUnique(attributeDefName2b.getId(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getPITAttributeDefName().findBySourceIdUnique(attributeDefName2c.getId(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getPITAttributeDefName().findBySourceIdUnique(attributeDefName2d.getId(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getPITAttributeDefNameSet().findBySourceIdUnique(attributeDefNameSet2b.getId(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getPITAttributeDefNameSet().findBySourceIdUnique(attributeDefNameSet2aTo2b.getId(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getPITAttributeDefNameSet().findBySourceIdUnique(attributeDefNameSet2cTo2d.getId(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getPITField().findBySourceIdUnique(field2.getUuid(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getPITGroup().findBySourceIdUnique(role2a.getUuid(), false));
    assertNull(GrouperDAOFactory.getFactory().getPITGroup().findBySourceIdUnique(role2b.getUuid(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getPITGroup().findBySourceIdUnique(role2c.getUuid(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getPITGroup().findBySourceIdUnique(role2d.getUuid(), false));
    assertNull(GrouperDAOFactory.getFactory().getPITGroupSet().findBySourceIdUnique(groupSet2b.getId(), false));
    assertNull(GrouperDAOFactory.getFactory().getPITGroupSet().findBySourceIdUnique(groupSet2aTo2b.getId(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getPITGroupSet().findBySourceIdUnique(groupSet2cTo2d.getId(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getPITMember().findBySourceIdUnique(member2.getUuid(), false));
    assertNull(GrouperDAOFactory.getFactory().getPITMembership().findBySourceIdUnique(membership2.getImmediateMembershipId(), false));
    assertNull(GrouperDAOFactory.getFactory().getPITMembership().findBySourceIdUnique(membership2aTo2b.getImmediateMembershipId(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getPITMembership().findBySourceIdUnique(membership2cTo2d.getImmediateMembershipId(), false));
    assertNull(GrouperDAOFactory.getFactory().getPITRoleSet().findBySourceIdUnique(roleSet2b.getId(), false));
    assertNull(GrouperDAOFactory.getFactory().getPITRoleSet().findBySourceIdUnique(roleSet2aTo2b.getId(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getPITRoleSet().findBySourceIdUnique(roleSet2cTo2d.getId(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getPITStem().findBySourceIdUnique(stem2.getUuid(), false));
  }
  
  /**
   * 
   */
  public void testDeleteInactiveStem() {
    GroupType type = GroupType.createType(grouperSession, "testType");

    // data set 1
    Stem stem1 = edu.addChildStem("stem1", "stem1");
    Group role1a = (Group) stem1.addChildRole("role1a", "role1a");
    Group role1b = (Group) stem1.addChildRole("role1b", "role1b");
    Group role1c = (Group) stem1.addChildRole("role1c", "role1c");
    Group role1d = (Group) stem1.addChildRole("role1d", "role1d");
    RoleSet roleSet1b = GrouperDAOFactory.getFactory().getRoleSet().findByIfHasRoleId(role1b.getId()).iterator().next();
    role1a.addMember(role1b.toSubject());
    role1b.addMember(role1c.toSubject());
    role1c.addMember(role1d.toSubject());
    role1a.getRoleInheritanceDelegate().addRoleToInheritFromThis(role1b);
    role1b.getRoleInheritanceDelegate().addRoleToInheritFromThis(role1c);
    role1c.getRoleInheritanceDelegate().addRoleToInheritFromThis(role1d);
    GroupSet groupSet1b = GrouperDAOFactory.getFactory().getGroupSet().findSelfGroup(role1b.getId(), Group.getDefaultList().getUuid());
    GroupSet groupSet1aTo1b = GrouperDAOFactory.getFactory().getGroupSet().findImmediateByOwnerGroupAndMemberGroupAndField(role1a.getId(), role1b.getId(), Group.getDefaultList());
    GroupSet groupSet1cTo1d = GrouperDAOFactory.getFactory().getGroupSet().findImmediateByOwnerGroupAndMemberGroupAndField(role1c.getId(), role1d.getId(), Group.getDefaultList());
    RoleSet roleSet1aTo1b = GrouperDAOFactory.getFactory().getRoleSet().findByIfThenImmediate(role1a.getId(), role1b.getId(), true);
    RoleSet roleSet1cTo1d = GrouperDAOFactory.getFactory().getRoleSet().findByIfThenImmediate(role1c.getId(), role1d.getId(), true);
    AttributeDef attributeDef1 = edu.addChildAttributeDef("attributeDef1", AttributeDefType.attr);
    attributeDef1.setAssignToGroup(true);
    attributeDef1.setValueType(AttributeDefValueType.string);
    attributeDef1.store();
    AttributeDefName attributeDefName1a = edu.addChildAttributeDefName(attributeDef1, "testAttribute1a", "testAttribute1a");
    AttributeDefName attributeDefName1b = edu.addChildAttributeDefName(attributeDef1, "testAttribute1b", "testAttribute1b");
    AttributeDefName attributeDefName1c = edu.addChildAttributeDefName(attributeDef1, "testAttribute1c", "testAttribute1c");
    AttributeDefName attributeDefName1d = edu.addChildAttributeDefName(attributeDef1, "testAttribute1d", "testAttribute1d");
    AttributeDefNameSet attributeDefNameSet1b = GrouperDAOFactory.getFactory().getAttributeDefNameSet().findByIfHasAttributeDefNameId(attributeDefName1b.getId()).iterator().next();
    attributeDefName1a.getAttributeDefNameSetDelegate().addToAttributeDefNameSet(attributeDefName1b);
    attributeDefName1b.getAttributeDefNameSetDelegate().addToAttributeDefNameSet(attributeDefName1c);
    attributeDefName1c.getAttributeDefNameSetDelegate().addToAttributeDefNameSet(attributeDefName1d);
    AttributeDefNameSet attributeDefNameSet1aTo1b = GrouperDAOFactory.getFactory().getAttributeDefNameSet().findByIfThenImmediate(attributeDefName1a.getId(), attributeDefName1b.getId(), true);
    AttributeDefNameSet attributeDefNameSet1cTo1d = GrouperDAOFactory.getFactory().getAttributeDefNameSet().findByIfThenImmediate(attributeDefName1c.getId(), attributeDefName1d.getId(), true);
    AttributeAssignAction action1a = attributeDef1.getAttributeDefActionDelegate().addAction("testAction1a");
    AttributeAssignAction action1b = attributeDef1.getAttributeDefActionDelegate().addAction("testAction1b");
    AttributeAssignAction action1c = attributeDef1.getAttributeDefActionDelegate().addAction("testAction1c");
    AttributeAssignAction action1d = attributeDef1.getAttributeDefActionDelegate().addAction("testAction1d");
    AttributeAssignActionSet actionSet1b = GrouperDAOFactory.getFactory().getAttributeAssignActionSet().findByIfHasAttributeAssignActionId(action1b.getId()).iterator().next();
    action1a.getAttributeAssignActionSetDelegate().addToAttributeAssignActionSet(action1b);
    action1b.getAttributeAssignActionSetDelegate().addToAttributeAssignActionSet(action1c);
    action1c.getAttributeAssignActionSetDelegate().addToAttributeAssignActionSet(action1d);
    AttributeAssignActionSet actionSet1aTo1b = GrouperDAOFactory.getFactory().getAttributeAssignActionSet().findByIfThenImmediate(action1a.getId(), action1b.getId(), true);
    AttributeAssignActionSet actionSet1cTo1d = GrouperDAOFactory.getFactory().getAttributeAssignActionSet().findByIfThenImmediate(action1c.getId(), action1d.getId(), true);
    AttributeAssign attributeAssign1 = role1b.getAttributeDelegate().assignAttribute(action1b.getName(), attributeDefName1b).getAttributeAssign();
    AttributeAssignValue value1 = attributeAssign1.getValueDelegate().assignValue("test1").getAttributeAssignValue();
    Field field1 = type.addList(grouperSession, "list1", AccessPrivilege.READ, AccessPrivilege.UPDATE);
    Member member1 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ1, true);
    role1b.addType(type);
    role1b.addMember(member1.getSubject(), field1);
    Membership membership1 = MembershipFinder.findImmediateMembership(grouperSession, role1b, member1.getSubject(), field1, true);
    Membership membership1aTo1b = MembershipFinder.findImmediateMembership(grouperSession, role1a, role1b.toSubject(), Group.getDefaultList(), true);
    Membership membership1cTo1d = MembershipFinder.findImmediateMembership(grouperSession, role1c, role1d.toSubject(), Group.getDefaultList(), true);
    ChangeLogTempToEntity.convertRecords();

    // data set 2
    Stem stem2 = edu2.addChildStem("stem2", "stem2");
    Group role2a = (Group) stem2.addChildRole("role2a", "role2a");
    Group role2b = (Group) stem2.addChildRole("role2b", "role2b");
    Group role2c = (Group) stem2.addChildRole("role2c", "role2c");
    Group role2d = (Group) stem2.addChildRole("role2d", "role2d");
    RoleSet roleSet2b = GrouperDAOFactory.getFactory().getRoleSet().findByIfHasRoleId(role2b.getId()).iterator().next();
    role2a.addMember(role2b.toSubject());
    role2b.addMember(role2c.toSubject());
    role2c.addMember(role2d.toSubject());
    role2a.getRoleInheritanceDelegate().addRoleToInheritFromThis(role2b);
    role2b.getRoleInheritanceDelegate().addRoleToInheritFromThis(role2c);
    role2c.getRoleInheritanceDelegate().addRoleToInheritFromThis(role2d);
    GroupSet groupSet2b = GrouperDAOFactory.getFactory().getGroupSet().findSelfGroup(role2b.getId(), Group.getDefaultList().getUuid());
    GroupSet groupSet2aTo2b = GrouperDAOFactory.getFactory().getGroupSet().findImmediateByOwnerGroupAndMemberGroupAndField(role2a.getId(), role2b.getId(), Group.getDefaultList());
    GroupSet groupSet2cTo2d = GrouperDAOFactory.getFactory().getGroupSet().findImmediateByOwnerGroupAndMemberGroupAndField(role2c.getId(), role2d.getId(), Group.getDefaultList());
    RoleSet roleSet2aTo2b = GrouperDAOFactory.getFactory().getRoleSet().findByIfThenImmediate(role2a.getId(), role2b.getId(), true);
    RoleSet roleSet2cTo2d = GrouperDAOFactory.getFactory().getRoleSet().findByIfThenImmediate(role2c.getId(), role2d.getId(), true);
    AttributeDef attributeDef2 = edu2.addChildAttributeDef("attributeDef2", AttributeDefType.attr);
    attributeDef2.setAssignToGroup(true);
    attributeDef2.setValueType(AttributeDefValueType.string);
    attributeDef2.store();
    AttributeDefName attributeDefName2a = edu2.addChildAttributeDefName(attributeDef2, "testAttribute2a", "testAttribute2a");
    AttributeDefName attributeDefName2b = edu2.addChildAttributeDefName(attributeDef2, "testAttribute2b", "testAttribute2b");
    AttributeDefName attributeDefName2c = edu2.addChildAttributeDefName(attributeDef2, "testAttribute2c", "testAttribute2c");
    AttributeDefName attributeDefName2d = edu2.addChildAttributeDefName(attributeDef2, "testAttribute2d", "testAttribute2d");
    AttributeDefNameSet attributeDefNameSet2b = GrouperDAOFactory.getFactory().getAttributeDefNameSet().findByIfHasAttributeDefNameId(attributeDefName2b.getId()).iterator().next();
    attributeDefName2a.getAttributeDefNameSetDelegate().addToAttributeDefNameSet(attributeDefName2b);
    attributeDefName2b.getAttributeDefNameSetDelegate().addToAttributeDefNameSet(attributeDefName2c);
    attributeDefName2c.getAttributeDefNameSetDelegate().addToAttributeDefNameSet(attributeDefName2d);
    AttributeDefNameSet attributeDefNameSet2aTo2b = GrouperDAOFactory.getFactory().getAttributeDefNameSet().findByIfThenImmediate(attributeDefName2a.getId(), attributeDefName2b.getId(), true);
    AttributeDefNameSet attributeDefNameSet2cTo2d = GrouperDAOFactory.getFactory().getAttributeDefNameSet().findByIfThenImmediate(attributeDefName2c.getId(), attributeDefName2d.getId(), true);
    AttributeAssignAction action2a = attributeDef2.getAttributeDefActionDelegate().addAction("testAction2a");
    AttributeAssignAction action2b = attributeDef2.getAttributeDefActionDelegate().addAction("testAction2b");
    AttributeAssignAction action2c = attributeDef2.getAttributeDefActionDelegate().addAction("testAction2c");
    AttributeAssignAction action2d = attributeDef2.getAttributeDefActionDelegate().addAction("testAction2d");
    AttributeAssignActionSet actionSet2b = GrouperDAOFactory.getFactory().getAttributeAssignActionSet().findByIfHasAttributeAssignActionId(action2b.getId()).iterator().next();
    action2a.getAttributeAssignActionSetDelegate().addToAttributeAssignActionSet(action2b);
    action2b.getAttributeAssignActionSetDelegate().addToAttributeAssignActionSet(action2c);
    action2c.getAttributeAssignActionSetDelegate().addToAttributeAssignActionSet(action2d);
    AttributeAssignActionSet actionSet2aTo2b = GrouperDAOFactory.getFactory().getAttributeAssignActionSet().findByIfThenImmediate(action2a.getId(), action2b.getId(), true);
    AttributeAssignActionSet actionSet2cTo2d = GrouperDAOFactory.getFactory().getAttributeAssignActionSet().findByIfThenImmediate(action2c.getId(), action2d.getId(), true);
    AttributeAssign attributeAssign2 = role2b.getAttributeDelegate().assignAttribute(action2b.getName(), attributeDefName2b).getAttributeAssign();
    AttributeAssignValue value2 = attributeAssign2.getValueDelegate().assignValue("test2").getAttributeAssignValue();
    Field field2 = type.addList(grouperSession, "list2", AccessPrivilege.READ, AccessPrivilege.UPDATE);
    Member member2 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ2, true);
    role2b.addType(type);
    role2b.addMember(member2.getSubject(), field2);
    Membership membership2 = MembershipFinder.findImmediateMembership(grouperSession, role2b, member2.getSubject(), field2, true);
    Membership membership2aTo2b = MembershipFinder.findImmediateMembership(grouperSession, role2a, role2b.toSubject(), Group.getDefaultList(), true);
    Membership membership2cTo2d = MembershipFinder.findImmediateMembership(grouperSession, role2c, role2d.toSubject(), Group.getDefaultList(), true);
    ChangeLogTempToEntity.convertRecords();
    
    // now delete data from data set 2
    role2b.deleteMember(member2, field2);
    role2b.deleteType(type);
    HibernateSession.byObjectStatic().delete(member2);
    HibernateSession.byObjectStatic().delete(field2);
    value2.delete();
    action2a.delete();
    action2b.delete();
    action2c.delete();
    action2d.delete();
    attributeDefName2a.delete();
    attributeDefName2b.delete();
    attributeDefName2c.delete();
    attributeDefName2d.delete();
    attributeDef2.delete();
    role2a.getRoleInheritanceDelegate().removeRoleFromInheritFromThis(role2b);
    role2b.getRoleInheritanceDelegate().removeRoleFromInheritFromThis(role2c);
    role2c.getRoleInheritanceDelegate().removeRoleFromInheritFromThis(role2d);
    role2a.delete();
    role2b.delete();
    role2c.delete();
    role2d.delete();
    stem2.delete();
    edu2.delete();
    ChangeLogTempToEntity.convertRecords();
    
    // now delete old PIT data
    PITUtils.deleteInactiveStem(edu2.getName(), false);

    // verify data set 1...
    assertNotNull(GrouperDAOFactory.getFactory().getPITAttributeAssign().findBySourceIdUnique(attributeAssign1.getId(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getPITAttributeDef().findBySourceIdUnique(attributeDef1.getId(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getPITAttributeAssignAction().findBySourceIdUnique(action1a.getId(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getPITAttributeAssignAction().findBySourceIdUnique(action1b.getId(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getPITAttributeAssignAction().findBySourceIdUnique(action1c.getId(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getPITAttributeAssignAction().findBySourceIdUnique(action1d.getId(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getPITAttributeAssignActionSet().findBySourceIdUnique(actionSet1b.getId(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getPITAttributeAssignActionSet().findBySourceIdUnique(actionSet1aTo1b.getId(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getPITAttributeAssignActionSet().findBySourceIdUnique(actionSet1cTo1d.getId(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getPITAttributeAssignValue().findBySourceIdUnique(value1.getId(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getPITAttributeDefName().findBySourceIdUnique(attributeDefName1a.getId(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getPITAttributeDefName().findBySourceIdUnique(attributeDefName1b.getId(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getPITAttributeDefName().findBySourceIdUnique(attributeDefName1c.getId(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getPITAttributeDefName().findBySourceIdUnique(attributeDefName1d.getId(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getPITAttributeDefNameSet().findBySourceIdUnique(attributeDefNameSet1b.getId(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getPITAttributeDefNameSet().findBySourceIdUnique(attributeDefNameSet1aTo1b.getId(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getPITAttributeDefNameSet().findBySourceIdUnique(attributeDefNameSet1cTo1d.getId(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getPITField().findBySourceIdUnique(field1.getUuid(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getPITGroup().findBySourceIdUnique(role1a.getUuid(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getPITGroup().findBySourceIdUnique(role1b.getUuid(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getPITGroup().findBySourceIdUnique(role1c.getUuid(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getPITGroup().findBySourceIdUnique(role1d.getUuid(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getPITGroupSet().findBySourceIdUnique(groupSet1b.getId(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getPITGroupSet().findBySourceIdUnique(groupSet1aTo1b.getId(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getPITGroupSet().findBySourceIdUnique(groupSet1cTo1d.getId(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getPITMember().findBySourceIdUnique(member1.getUuid(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getPITMembership().findBySourceIdUnique(membership1.getImmediateMembershipId(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getPITMembership().findBySourceIdUnique(membership1aTo1b.getImmediateMembershipId(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getPITMembership().findBySourceIdUnique(membership1cTo1d.getImmediateMembershipId(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getPITRoleSet().findBySourceIdUnique(roleSet1b.getId(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getPITRoleSet().findBySourceIdUnique(roleSet1aTo1b.getId(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getPITRoleSet().findBySourceIdUnique(roleSet1cTo1d.getId(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getPITStem().findBySourceIdUnique(stem1.getUuid(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getPITStem().findBySourceIdUnique(edu.getUuid(), false));
    
    // verify data set 2...
    assertNull(GrouperDAOFactory.getFactory().getPITAttributeAssign().findBySourceIdUnique(attributeAssign2.getId(), false));
    assertNull(GrouperDAOFactory.getFactory().getPITAttributeDef().findBySourceIdUnique(attributeDef2.getId(), false));
    assertNull(GrouperDAOFactory.getFactory().getPITAttributeAssignAction().findBySourceIdUnique(action2a.getId(), false));
    assertNull(GrouperDAOFactory.getFactory().getPITAttributeAssignAction().findBySourceIdUnique(action2b.getId(), false));
    assertNull(GrouperDAOFactory.getFactory().getPITAttributeAssignAction().findBySourceIdUnique(action2c.getId(), false));
    assertNull(GrouperDAOFactory.getFactory().getPITAttributeAssignAction().findBySourceIdUnique(action2d.getId(), false));
    assertNull(GrouperDAOFactory.getFactory().getPITAttributeAssignActionSet().findBySourceIdUnique(actionSet2b.getId(), false));
    assertNull(GrouperDAOFactory.getFactory().getPITAttributeAssignActionSet().findBySourceIdUnique(actionSet2aTo2b.getId(), false));
    assertNull(GrouperDAOFactory.getFactory().getPITAttributeAssignActionSet().findBySourceIdUnique(actionSet2cTo2d.getId(), false));
    assertNull(GrouperDAOFactory.getFactory().getPITAttributeAssignValue().findBySourceIdUnique(value2.getId(), false));
    assertNull(GrouperDAOFactory.getFactory().getPITAttributeDefName().findBySourceIdUnique(attributeDefName2a.getId(), false));
    assertNull(GrouperDAOFactory.getFactory().getPITAttributeDefName().findBySourceIdUnique(attributeDefName2b.getId(), false));
    assertNull(GrouperDAOFactory.getFactory().getPITAttributeDefName().findBySourceIdUnique(attributeDefName2c.getId(), false));
    assertNull(GrouperDAOFactory.getFactory().getPITAttributeDefName().findBySourceIdUnique(attributeDefName2d.getId(), false));
    assertNull(GrouperDAOFactory.getFactory().getPITAttributeDefNameSet().findBySourceIdUnique(attributeDefNameSet2b.getId(), false));
    assertNull(GrouperDAOFactory.getFactory().getPITAttributeDefNameSet().findBySourceIdUnique(attributeDefNameSet2aTo2b.getId(), false));
    assertNull(GrouperDAOFactory.getFactory().getPITAttributeDefNameSet().findBySourceIdUnique(attributeDefNameSet2cTo2d.getId(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getPITField().findBySourceIdUnique(field2.getUuid(), false));
    assertNull(GrouperDAOFactory.getFactory().getPITGroup().findBySourceIdUnique(role2a.getUuid(), false));
    assertNull(GrouperDAOFactory.getFactory().getPITGroup().findBySourceIdUnique(role2b.getUuid(), false));
    assertNull(GrouperDAOFactory.getFactory().getPITGroup().findBySourceIdUnique(role2c.getUuid(), false));
    assertNull(GrouperDAOFactory.getFactory().getPITGroup().findBySourceIdUnique(role2d.getUuid(), false));
    assertNull(GrouperDAOFactory.getFactory().getPITGroupSet().findBySourceIdUnique(groupSet2b.getId(), false));
    assertNull(GrouperDAOFactory.getFactory().getPITGroupSet().findBySourceIdUnique(groupSet2aTo2b.getId(), false));
    assertNull(GrouperDAOFactory.getFactory().getPITGroupSet().findBySourceIdUnique(groupSet2cTo2d.getId(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getPITMember().findBySourceIdUnique(member2.getUuid(), false));
    assertNull(GrouperDAOFactory.getFactory().getPITMembership().findBySourceIdUnique(membership2.getImmediateMembershipId(), false));
    assertNull(GrouperDAOFactory.getFactory().getPITMembership().findBySourceIdUnique(membership2aTo2b.getImmediateMembershipId(), false));
    assertNull(GrouperDAOFactory.getFactory().getPITMembership().findBySourceIdUnique(membership2cTo2d.getImmediateMembershipId(), false));
    assertNull(GrouperDAOFactory.getFactory().getPITRoleSet().findBySourceIdUnique(roleSet2b.getId(), false));
    assertNull(GrouperDAOFactory.getFactory().getPITRoleSet().findBySourceIdUnique(roleSet2aTo2b.getId(), false));
    assertNull(GrouperDAOFactory.getFactory().getPITRoleSet().findBySourceIdUnique(roleSet2cTo2d.getId(), false));
    assertNull(GrouperDAOFactory.getFactory().getPITStem().findBySourceIdUnique(stem2.getUuid(), false));
    assertNull(GrouperDAOFactory.getFactory().getPITStem().findBySourceIdUnique(edu2.getUuid(), false));
  }
  
  /**
   * 
   */
  public void testDeleteInactiveObjectsInStem() {
    GroupType type = GroupType.createType(grouperSession, "testType");

    // data set 1
    Stem stem1 = edu.addChildStem("stem1", "stem1");
    Group role1a = (Group) stem1.addChildRole("role1a", "role1a");
    Group role1b = (Group) stem1.addChildRole("role1b", "role1b");
    Group role1c = (Group) stem1.addChildRole("role1c", "role1c");
    Group role1d = (Group) stem1.addChildRole("role1d", "role1d");
    RoleSet roleSet1b = GrouperDAOFactory.getFactory().getRoleSet().findByIfHasRoleId(role1b.getId()).iterator().next();
    role1a.addMember(role1b.toSubject());
    role1b.addMember(role1c.toSubject());
    role1c.addMember(role1d.toSubject());
    role1a.getRoleInheritanceDelegate().addRoleToInheritFromThis(role1b);
    role1b.getRoleInheritanceDelegate().addRoleToInheritFromThis(role1c);
    role1c.getRoleInheritanceDelegate().addRoleToInheritFromThis(role1d);
    GroupSet groupSet1b = GrouperDAOFactory.getFactory().getGroupSet().findSelfGroup(role1b.getId(), Group.getDefaultList().getUuid());
    GroupSet groupSet1aTo1b = GrouperDAOFactory.getFactory().getGroupSet().findImmediateByOwnerGroupAndMemberGroupAndField(role1a.getId(), role1b.getId(), Group.getDefaultList());
    GroupSet groupSet1cTo1d = GrouperDAOFactory.getFactory().getGroupSet().findImmediateByOwnerGroupAndMemberGroupAndField(role1c.getId(), role1d.getId(), Group.getDefaultList());
    RoleSet roleSet1aTo1b = GrouperDAOFactory.getFactory().getRoleSet().findByIfThenImmediate(role1a.getId(), role1b.getId(), true);
    RoleSet roleSet1cTo1d = GrouperDAOFactory.getFactory().getRoleSet().findByIfThenImmediate(role1c.getId(), role1d.getId(), true);
    AttributeDef attributeDef1 = edu.addChildAttributeDef("attributeDef1", AttributeDefType.attr);
    attributeDef1.setAssignToGroup(true);
    attributeDef1.setValueType(AttributeDefValueType.string);
    attributeDef1.store();
    AttributeDefName attributeDefName1a = edu.addChildAttributeDefName(attributeDef1, "testAttribute1a", "testAttribute1a");
    AttributeDefName attributeDefName1b = edu.addChildAttributeDefName(attributeDef1, "testAttribute1b", "testAttribute1b");
    AttributeDefName attributeDefName1c = edu.addChildAttributeDefName(attributeDef1, "testAttribute1c", "testAttribute1c");
    AttributeDefName attributeDefName1d = edu.addChildAttributeDefName(attributeDef1, "testAttribute1d", "testAttribute1d");
    AttributeDefNameSet attributeDefNameSet1b = GrouperDAOFactory.getFactory().getAttributeDefNameSet().findByIfHasAttributeDefNameId(attributeDefName1b.getId()).iterator().next();
    attributeDefName1a.getAttributeDefNameSetDelegate().addToAttributeDefNameSet(attributeDefName1b);
    attributeDefName1b.getAttributeDefNameSetDelegate().addToAttributeDefNameSet(attributeDefName1c);
    attributeDefName1c.getAttributeDefNameSetDelegate().addToAttributeDefNameSet(attributeDefName1d);
    AttributeDefNameSet attributeDefNameSet1aTo1b = GrouperDAOFactory.getFactory().getAttributeDefNameSet().findByIfThenImmediate(attributeDefName1a.getId(), attributeDefName1b.getId(), true);
    AttributeDefNameSet attributeDefNameSet1cTo1d = GrouperDAOFactory.getFactory().getAttributeDefNameSet().findByIfThenImmediate(attributeDefName1c.getId(), attributeDefName1d.getId(), true);
    AttributeAssignAction action1a = attributeDef1.getAttributeDefActionDelegate().addAction("testAction1a");
    AttributeAssignAction action1b = attributeDef1.getAttributeDefActionDelegate().addAction("testAction1b");
    AttributeAssignAction action1c = attributeDef1.getAttributeDefActionDelegate().addAction("testAction1c");
    AttributeAssignAction action1d = attributeDef1.getAttributeDefActionDelegate().addAction("testAction1d");
    AttributeAssignActionSet actionSet1b = GrouperDAOFactory.getFactory().getAttributeAssignActionSet().findByIfHasAttributeAssignActionId(action1b.getId()).iterator().next();
    action1a.getAttributeAssignActionSetDelegate().addToAttributeAssignActionSet(action1b);
    action1b.getAttributeAssignActionSetDelegate().addToAttributeAssignActionSet(action1c);
    action1c.getAttributeAssignActionSetDelegate().addToAttributeAssignActionSet(action1d);
    AttributeAssignActionSet actionSet1aTo1b = GrouperDAOFactory.getFactory().getAttributeAssignActionSet().findByIfThenImmediate(action1a.getId(), action1b.getId(), true);
    AttributeAssignActionSet actionSet1cTo1d = GrouperDAOFactory.getFactory().getAttributeAssignActionSet().findByIfThenImmediate(action1c.getId(), action1d.getId(), true);
    AttributeAssign attributeAssign1 = role1b.getAttributeDelegate().assignAttribute(action1b.getName(), attributeDefName1b).getAttributeAssign();
    AttributeAssignValue value1 = attributeAssign1.getValueDelegate().assignValue("test1").getAttributeAssignValue();
    Field field1 = type.addList(grouperSession, "list1", AccessPrivilege.READ, AccessPrivilege.UPDATE);
    Member member1 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ1, true);
    role1b.addType(type);
    role1b.addMember(member1.getSubject(), field1);
    Membership membership1 = MembershipFinder.findImmediateMembership(grouperSession, role1b, member1.getSubject(), field1, true);
    Membership membership1aTo1b = MembershipFinder.findImmediateMembership(grouperSession, role1a, role1b.toSubject(), Group.getDefaultList(), true);
    Membership membership1cTo1d = MembershipFinder.findImmediateMembership(grouperSession, role1c, role1d.toSubject(), Group.getDefaultList(), true);
    ChangeLogTempToEntity.convertRecords();

    // data set 2
    Stem stem2 = edu2.addChildStem("stem2", "stem2");
    Group role2a = (Group) stem2.addChildRole("role2a", "role2a");
    Group role2b = (Group) stem2.addChildRole("role2b", "role2b");
    Group role2c = (Group) stem2.addChildRole("role2c", "role2c");
    Group role2d = (Group) stem2.addChildRole("role2d", "role2d");
    RoleSet roleSet2b = GrouperDAOFactory.getFactory().getRoleSet().findByIfHasRoleId(role2b.getId()).iterator().next();
    role2a.addMember(role2b.toSubject());
    role2b.addMember(role2c.toSubject());
    role2c.addMember(role2d.toSubject());
    role2a.getRoleInheritanceDelegate().addRoleToInheritFromThis(role2b);
    role2b.getRoleInheritanceDelegate().addRoleToInheritFromThis(role2c);
    role2c.getRoleInheritanceDelegate().addRoleToInheritFromThis(role2d);
    GroupSet groupSet2b = GrouperDAOFactory.getFactory().getGroupSet().findSelfGroup(role2b.getId(), Group.getDefaultList().getUuid());
    GroupSet groupSet2aTo2b = GrouperDAOFactory.getFactory().getGroupSet().findImmediateByOwnerGroupAndMemberGroupAndField(role2a.getId(), role2b.getId(), Group.getDefaultList());
    GroupSet groupSet2cTo2d = GrouperDAOFactory.getFactory().getGroupSet().findImmediateByOwnerGroupAndMemberGroupAndField(role2c.getId(), role2d.getId(), Group.getDefaultList());
    RoleSet roleSet2aTo2b = GrouperDAOFactory.getFactory().getRoleSet().findByIfThenImmediate(role2a.getId(), role2b.getId(), true);
    RoleSet roleSet2cTo2d = GrouperDAOFactory.getFactory().getRoleSet().findByIfThenImmediate(role2c.getId(), role2d.getId(), true);
    AttributeDef attributeDef2 = edu2.addChildAttributeDef("attributeDef2", AttributeDefType.attr);
    attributeDef2.setAssignToGroup(true);
    attributeDef2.setValueType(AttributeDefValueType.string);
    attributeDef2.store();
    AttributeDefName attributeDefName2a = edu2.addChildAttributeDefName(attributeDef2, "testAttribute2a", "testAttribute2a");
    AttributeDefName attributeDefName2b = edu2.addChildAttributeDefName(attributeDef2, "testAttribute2b", "testAttribute2b");
    AttributeDefName attributeDefName2c = edu2.addChildAttributeDefName(attributeDef2, "testAttribute2c", "testAttribute2c");
    AttributeDefName attributeDefName2d = edu2.addChildAttributeDefName(attributeDef2, "testAttribute2d", "testAttribute2d");
    AttributeDefNameSet attributeDefNameSet2b = GrouperDAOFactory.getFactory().getAttributeDefNameSet().findByIfHasAttributeDefNameId(attributeDefName2b.getId()).iterator().next();
    attributeDefName2a.getAttributeDefNameSetDelegate().addToAttributeDefNameSet(attributeDefName2b);
    attributeDefName2b.getAttributeDefNameSetDelegate().addToAttributeDefNameSet(attributeDefName2c);
    attributeDefName2c.getAttributeDefNameSetDelegate().addToAttributeDefNameSet(attributeDefName2d);
    AttributeDefNameSet attributeDefNameSet2aTo2b = GrouperDAOFactory.getFactory().getAttributeDefNameSet().findByIfThenImmediate(attributeDefName2a.getId(), attributeDefName2b.getId(), true);
    AttributeDefNameSet attributeDefNameSet2cTo2d = GrouperDAOFactory.getFactory().getAttributeDefNameSet().findByIfThenImmediate(attributeDefName2c.getId(), attributeDefName2d.getId(), true);
    AttributeAssignAction action2a = attributeDef2.getAttributeDefActionDelegate().addAction("testAction2a");
    AttributeAssignAction action2b = attributeDef2.getAttributeDefActionDelegate().addAction("testAction2b");
    AttributeAssignAction action2c = attributeDef2.getAttributeDefActionDelegate().addAction("testAction2c");
    AttributeAssignAction action2d = attributeDef2.getAttributeDefActionDelegate().addAction("testAction2d");
    AttributeAssignActionSet actionSet2b = GrouperDAOFactory.getFactory().getAttributeAssignActionSet().findByIfHasAttributeAssignActionId(action2b.getId()).iterator().next();
    action2a.getAttributeAssignActionSetDelegate().addToAttributeAssignActionSet(action2b);
    action2b.getAttributeAssignActionSetDelegate().addToAttributeAssignActionSet(action2c);
    action2c.getAttributeAssignActionSetDelegate().addToAttributeAssignActionSet(action2d);
    AttributeAssignActionSet actionSet2aTo2b = GrouperDAOFactory.getFactory().getAttributeAssignActionSet().findByIfThenImmediate(action2a.getId(), action2b.getId(), true);
    AttributeAssignActionSet actionSet2cTo2d = GrouperDAOFactory.getFactory().getAttributeAssignActionSet().findByIfThenImmediate(action2c.getId(), action2d.getId(), true);
    AttributeAssign attributeAssign2 = role2b.getAttributeDelegate().assignAttribute(action2b.getName(), attributeDefName2b).getAttributeAssign();
    AttributeAssignValue value2 = attributeAssign2.getValueDelegate().assignValue("test2").getAttributeAssignValue();
    Field field2 = type.addList(grouperSession, "list2", AccessPrivilege.READ, AccessPrivilege.UPDATE);
    Member member2 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ2, true);
    role2b.addType(type);
    role2b.addMember(member2.getSubject(), field2);
    Membership membership2 = MembershipFinder.findImmediateMembership(grouperSession, role2b, member2.getSubject(), field2, true);
    Membership membership2aTo2b = MembershipFinder.findImmediateMembership(grouperSession, role2a, role2b.toSubject(), Group.getDefaultList(), true);
    Membership membership2cTo2d = MembershipFinder.findImmediateMembership(grouperSession, role2c, role2d.toSubject(), Group.getDefaultList(), true);
    ChangeLogTempToEntity.convertRecords();
    
    // now delete data from data set 2
    role2b.deleteMember(member2, field2);
    role2b.deleteType(type);
    HibernateSession.byObjectStatic().delete(member2);
    HibernateSession.byObjectStatic().delete(field2);
    value2.delete();
    action2a.delete();
    action2b.delete();
    action2c.delete();
    action2d.delete();
    attributeDefName2a.delete();
    attributeDefName2b.delete();
    attributeDefName2c.delete();
    attributeDefName2d.delete();
    attributeDef2.delete();
    role2a.getRoleInheritanceDelegate().removeRoleFromInheritFromThis(role2b);
    role2b.getRoleInheritanceDelegate().removeRoleFromInheritFromThis(role2c);
    role2c.getRoleInheritanceDelegate().removeRoleFromInheritFromThis(role2d);
    role2a.delete();
    role2b.delete();
    role2c.delete();
    role2d.delete();
    stem2.delete();
    ChangeLogTempToEntity.convertRecords();
    
    // now delete old PIT data
    PITUtils.deleteInactiveObjectsInStem(edu2.getName(), false);

    // verify data set 1...
    assertNotNull(GrouperDAOFactory.getFactory().getPITAttributeAssign().findBySourceIdUnique(attributeAssign1.getId(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getPITAttributeDef().findBySourceIdUnique(attributeDef1.getId(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getPITAttributeAssignAction().findBySourceIdUnique(action1a.getId(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getPITAttributeAssignAction().findBySourceIdUnique(action1b.getId(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getPITAttributeAssignAction().findBySourceIdUnique(action1c.getId(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getPITAttributeAssignAction().findBySourceIdUnique(action1d.getId(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getPITAttributeAssignActionSet().findBySourceIdUnique(actionSet1b.getId(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getPITAttributeAssignActionSet().findBySourceIdUnique(actionSet1aTo1b.getId(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getPITAttributeAssignActionSet().findBySourceIdUnique(actionSet1cTo1d.getId(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getPITAttributeAssignValue().findBySourceIdUnique(value1.getId(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getPITAttributeDefName().findBySourceIdUnique(attributeDefName1a.getId(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getPITAttributeDefName().findBySourceIdUnique(attributeDefName1b.getId(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getPITAttributeDefName().findBySourceIdUnique(attributeDefName1c.getId(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getPITAttributeDefName().findBySourceIdUnique(attributeDefName1d.getId(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getPITAttributeDefNameSet().findBySourceIdUnique(attributeDefNameSet1b.getId(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getPITAttributeDefNameSet().findBySourceIdUnique(attributeDefNameSet1aTo1b.getId(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getPITAttributeDefNameSet().findBySourceIdUnique(attributeDefNameSet1cTo1d.getId(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getPITField().findBySourceIdUnique(field1.getUuid(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getPITGroup().findBySourceIdUnique(role1a.getUuid(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getPITGroup().findBySourceIdUnique(role1b.getUuid(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getPITGroup().findBySourceIdUnique(role1c.getUuid(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getPITGroup().findBySourceIdUnique(role1d.getUuid(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getPITGroupSet().findBySourceIdUnique(groupSet1b.getId(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getPITGroupSet().findBySourceIdUnique(groupSet1aTo1b.getId(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getPITGroupSet().findBySourceIdUnique(groupSet1cTo1d.getId(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getPITMember().findBySourceIdUnique(member1.getUuid(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getPITMembership().findBySourceIdUnique(membership1.getImmediateMembershipId(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getPITMembership().findBySourceIdUnique(membership1aTo1b.getImmediateMembershipId(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getPITMembership().findBySourceIdUnique(membership1cTo1d.getImmediateMembershipId(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getPITRoleSet().findBySourceIdUnique(roleSet1b.getId(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getPITRoleSet().findBySourceIdUnique(roleSet1aTo1b.getId(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getPITRoleSet().findBySourceIdUnique(roleSet1cTo1d.getId(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getPITStem().findBySourceIdUnique(stem1.getUuid(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getPITStem().findBySourceIdUnique(edu.getUuid(), false));

    // verify data set 2...
    assertNull(GrouperDAOFactory.getFactory().getPITAttributeAssign().findBySourceIdUnique(attributeAssign2.getId(), false));
    assertNull(GrouperDAOFactory.getFactory().getPITAttributeDef().findBySourceIdUnique(attributeDef2.getId(), false));
    assertNull(GrouperDAOFactory.getFactory().getPITAttributeAssignAction().findBySourceIdUnique(action2a.getId(), false));
    assertNull(GrouperDAOFactory.getFactory().getPITAttributeAssignAction().findBySourceIdUnique(action2b.getId(), false));
    assertNull(GrouperDAOFactory.getFactory().getPITAttributeAssignAction().findBySourceIdUnique(action2c.getId(), false));
    assertNull(GrouperDAOFactory.getFactory().getPITAttributeAssignAction().findBySourceIdUnique(action2d.getId(), false));
    assertNull(GrouperDAOFactory.getFactory().getPITAttributeAssignActionSet().findBySourceIdUnique(actionSet2b.getId(), false));
    assertNull(GrouperDAOFactory.getFactory().getPITAttributeAssignActionSet().findBySourceIdUnique(actionSet2aTo2b.getId(), false));
    assertNull(GrouperDAOFactory.getFactory().getPITAttributeAssignActionSet().findBySourceIdUnique(actionSet2cTo2d.getId(), false));
    assertNull(GrouperDAOFactory.getFactory().getPITAttributeAssignValue().findBySourceIdUnique(value2.getId(), false));
    assertNull(GrouperDAOFactory.getFactory().getPITAttributeDefName().findBySourceIdUnique(attributeDefName2a.getId(), false));
    assertNull(GrouperDAOFactory.getFactory().getPITAttributeDefName().findBySourceIdUnique(attributeDefName2b.getId(), false));
    assertNull(GrouperDAOFactory.getFactory().getPITAttributeDefName().findBySourceIdUnique(attributeDefName2c.getId(), false));
    assertNull(GrouperDAOFactory.getFactory().getPITAttributeDefName().findBySourceIdUnique(attributeDefName2d.getId(), false));
    assertNull(GrouperDAOFactory.getFactory().getPITAttributeDefNameSet().findBySourceIdUnique(attributeDefNameSet2b.getId(), false));
    assertNull(GrouperDAOFactory.getFactory().getPITAttributeDefNameSet().findBySourceIdUnique(attributeDefNameSet2aTo2b.getId(), false));
    assertNull(GrouperDAOFactory.getFactory().getPITAttributeDefNameSet().findBySourceIdUnique(attributeDefNameSet2cTo2d.getId(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getPITField().findBySourceIdUnique(field2.getUuid(), false));
    assertNull(GrouperDAOFactory.getFactory().getPITGroup().findBySourceIdUnique(role2a.getUuid(), false));
    assertNull(GrouperDAOFactory.getFactory().getPITGroup().findBySourceIdUnique(role2b.getUuid(), false));
    assertNull(GrouperDAOFactory.getFactory().getPITGroup().findBySourceIdUnique(role2c.getUuid(), false));
    assertNull(GrouperDAOFactory.getFactory().getPITGroup().findBySourceIdUnique(role2d.getUuid(), false));
    assertNull(GrouperDAOFactory.getFactory().getPITGroupSet().findBySourceIdUnique(groupSet2b.getId(), false));
    assertNull(GrouperDAOFactory.getFactory().getPITGroupSet().findBySourceIdUnique(groupSet2aTo2b.getId(), false));
    assertNull(GrouperDAOFactory.getFactory().getPITGroupSet().findBySourceIdUnique(groupSet2cTo2d.getId(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getPITMember().findBySourceIdUnique(member2.getUuid(), false));
    assertNull(GrouperDAOFactory.getFactory().getPITMembership().findBySourceIdUnique(membership2.getImmediateMembershipId(), false));
    assertNull(GrouperDAOFactory.getFactory().getPITMembership().findBySourceIdUnique(membership2aTo2b.getImmediateMembershipId(), false));
    assertNull(GrouperDAOFactory.getFactory().getPITMembership().findBySourceIdUnique(membership2cTo2d.getImmediateMembershipId(), false));
    assertNull(GrouperDAOFactory.getFactory().getPITRoleSet().findBySourceIdUnique(roleSet2b.getId(), false));
    assertNull(GrouperDAOFactory.getFactory().getPITRoleSet().findBySourceIdUnique(roleSet2aTo2b.getId(), false));
    assertNull(GrouperDAOFactory.getFactory().getPITRoleSet().findBySourceIdUnique(roleSet2cTo2d.getId(), false));
    assertNull(GrouperDAOFactory.getFactory().getPITStem().findBySourceIdUnique(stem2.getUuid(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getPITStem().findBySourceIdUnique(edu2.getUuid(), false));
  }
}
