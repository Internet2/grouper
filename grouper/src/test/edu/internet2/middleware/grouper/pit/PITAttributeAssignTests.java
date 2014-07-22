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

import java.sql.Timestamp;
import java.util.Date;
import java.util.Set;

import junit.textui.TestRunner;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.MembershipFinder;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.AttributeDefType;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignAction;
import edu.internet2.middleware.grouper.changeLog.ChangeLogTempToEntity;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SessionHelper;
import edu.internet2.middleware.grouper.helper.StemHelper;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.permissions.role.Role;
import edu.internet2.middleware.grouper.privs.AttributeDefPrivilege;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * @author shilen
 * $Id$
 */
public class PITAttributeAssignTests extends GrouperTest {
  
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
  public PITAttributeAssignTests(String name) {
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
  
  private Date getDateWithSleep() {
    GrouperUtil.sleep(sleepTime);
    Date date = new Date();
    GrouperUtil.sleep(sleepTime);
    return date;
  }
  
  private Timestamp getTimestampWithSleep() {
    GrouperUtil.sleep(100);
    Date date = new Date();
    GrouperUtil.sleep(100);
    return new Timestamp(date.getTime());
  }
  
  /**
   * 
   */
  public void testStartTime() {
    Group group = edu.addChildGroup("testGroup", "testGroup");
    
    AttributeDef attributeDef = edu.addChildAttributeDef("attributeDef", AttributeDefType.attr);
    attributeDef.setAssignToGroup(true);
    attributeDef.store();
    AttributeDefName attributeDefName1 = edu.addChildAttributeDefName(attributeDef, "testAttribute1", "testAttribute1");
    AttributeAssignAction action1 = attributeDef.getAttributeDefActionDelegate().addAction("testAction1");

    Date beforeAddTime = getDateWithSleep();
    group.getAttributeDelegate().assignAttribute(action1.getName(), attributeDefName1);
    Date afterAddTime = getDateWithSleep();

    // populate PIT tables
    ChangeLogTempToEntity.convertRecords();
    
    Set<PITAttributeAssign> results = new PITAttributeAssignQuery()
      .setAttributeDefNameId(attributeDefName1.getId())
      .setActionId(action1.getId())
      .setOwnerGroupId(group.getId())
      .setStartDateAfter(beforeAddTime)
      .execute();
    assertEquals(1, results.size());
    
    results = new PITAttributeAssignQuery()
      .setAttributeDefNameId(attributeDefName1.getId())
      .setOwnerGroupId(group.getId())
      .setActionId(action1.getId())
      .setStartDateAfter(afterAddTime)
      .execute();
    assertEquals(0, results.size());    

    results = new PITAttributeAssignQuery()
      .setAttributeDefNameId(attributeDefName1.getId())
      .setOwnerGroupId(group.getId())
      .setActionId(action1.getId())
      .setStartDateBefore(beforeAddTime)
      .execute();
    assertEquals(0, results.size());

    results = new PITAttributeAssignQuery()
      .setAttributeDefNameId(attributeDefName1.getId())
      .setOwnerGroupId(group.getId())
      .setActionId(action1.getId())
      .setStartDateBefore(afterAddTime)
      .execute();
    assertEquals(1, results.size());

    results = new PITAttributeAssignQuery()
      .setAttributeDefNameId(attributeDefName1.getId())
      .setOwnerGroupId(group.getId())
      .setActionId(action1.getId())
      .execute();
    assertEquals(1, results.size());
  }
  
  /**
   * 
   */
  public void testEndTime() {
    Group group = edu.addChildGroup("testGroup", "testGroup");
    
    AttributeDef attributeDef = edu.addChildAttributeDef("attributeDef", AttributeDefType.attr);
    attributeDef.setAssignToGroup(true);
    attributeDef.store();
    AttributeDefName attributeDefName1 = edu.addChildAttributeDefName(attributeDef, "testAttribute1", "testAttribute1");
    AttributeAssignAction action1 = attributeDef.getAttributeDefActionDelegate().addAction("testAction1");

    AttributeAssign attributeAssign = group.getAttributeDelegate().assignAttribute(action1.getName(), attributeDefName1).getAttributeAssign();
    Date afterAddTime = getDateWithSleep();
    
    // populate PIT tables
    ChangeLogTempToEntity.convertRecords();
    
    Set<PITAttributeAssign> results = new PITAttributeAssignQuery()
      .setAttributeDefNameId(attributeDefName1.getId())
      .setOwnerGroupId(group.getId())
      .setActionId(action1.getId())
      .setEndDateAfter(afterAddTime)
      .execute();
    assertEquals(1, results.size());
    
    results = new PITAttributeAssignQuery()
      .setAttributeDefNameId(attributeDefName1.getId())
      .setOwnerGroupId(group.getId())
      .setActionId(action1.getId())
      .setEndDateBefore(afterAddTime)
      .execute();
    assertEquals(0, results.size());
    
    Date beforeDeleteTime = getDateWithSleep();
    attributeAssign.delete();
    Date afterDeleteTime = getDateWithSleep();

    // populate PIT tables
    ChangeLogTempToEntity.convertRecords();
    
    results = new PITAttributeAssignQuery()
      .setAttributeDefNameId(attributeDefName1.getId())
      .setOwnerGroupId(group.getId())
      .setActionId(action1.getId())
      .setEndDateAfter(afterDeleteTime)
      .execute();
    assertEquals(0, results.size());
    
    results = new PITAttributeAssignQuery()
      .setAttributeDefNameId(attributeDefName1.getId())
      .setOwnerGroupId(group.getId())
      .setActionId(action1.getId())
      .setEndDateAfter(beforeDeleteTime)
      .execute();
    assertEquals(1, results.size());    

    results = new PITAttributeAssignQuery()
      .setAttributeDefNameId(attributeDefName1.getId())
      .setOwnerGroupId(group.getId())
      .setActionId(action1.getId())
      .setEndDateBefore(afterDeleteTime)
      .execute();
    assertEquals(1, results.size());

    results = new PITAttributeAssignQuery()
      .setAttributeDefNameId(attributeDefName1.getId())
      .setOwnerGroupId(group.getId())
      .setActionId(action1.getId())
      .setEndDateBefore(beforeDeleteTime)
      .execute();
    assertEquals(0, results.size());
  }
  
  /**
   * 
   */
  public void testByAttributeDefName() {
    Group group = edu.addChildGroup("testGroup", "testGroup");
    
    AttributeDef attributeDef = edu.addChildAttributeDef("attributeDef", AttributeDefType.attr);
    attributeDef.setAssignToGroup(true);
    attributeDef.store();
    AttributeDefName attributeDefName1 = edu.addChildAttributeDefName(attributeDef, "testAttribute1", "testAttribute1");
    AttributeDefName attributeDefName2 = edu.addChildAttributeDefName(attributeDef, "testAttribute2", "testAttribute2");
    AttributeAssignAction action1 = attributeDef.getAttributeDefActionDelegate().addAction("testAction1");
    group.getAttributeDelegate().assignAttribute(action1.getName(), attributeDefName1);

    // populate PIT tables
    ChangeLogTempToEntity.convertRecords();
    
    Set<PITAttributeAssign> results = new PITAttributeAssignQuery()
      .setAttributeDefNameId(attributeDefName1.getId())
      .setActionId(action1.getId())
      .setOwnerGroupId(group.getId())
      .execute();
    assertEquals(1, results.size());
    
    results = new PITAttributeAssignQuery()
      .setAttributeDefNameId(attributeDefName2.getId())
      .setActionId(action1.getId())
      .setOwnerGroupId(group.getId())
      .execute();
    assertEquals(0, results.size());
  }
  
  /**
   * 
   */
  public void testByAction() {
    Group group = edu.addChildGroup("testGroup", "testGroup");
    
    AttributeDef attributeDef = edu.addChildAttributeDef("attributeDef", AttributeDefType.attr);
    attributeDef.setAssignToGroup(true);
    attributeDef.store();
    AttributeDefName attributeDefName1 = edu.addChildAttributeDefName(attributeDef, "testAttribute1", "testAttribute1");
    AttributeAssignAction action1 = attributeDef.getAttributeDefActionDelegate().addAction("testAction1");
    AttributeAssignAction action2 = attributeDef.getAttributeDefActionDelegate().addAction("testAction2");
    group.getAttributeDelegate().assignAttribute(action1.getName(), attributeDefName1);

    // populate PIT tables
    ChangeLogTempToEntity.convertRecords();
    
    Set<PITAttributeAssign> results = new PITAttributeAssignQuery()
      .setAttributeDefNameId(attributeDefName1.getId())
      .setActionId(action1.getId())
      .setOwnerGroupId(group.getId())
      .execute();
    assertEquals(1, results.size());
    
    results = new PITAttributeAssignQuery()
      .setAttributeDefNameId(attributeDefName1.getId())
      .setActionId(action2.getId())
      .setOwnerGroupId(group.getId())
      .execute();
    assertEquals(0, results.size());
  }
  
  /**
   * 
   */
  public void testByOwnerGroup() {
    Group group = edu.addChildGroup("testGroup", "testGroup");
    
    AttributeDef attributeDef = edu.addChildAttributeDef("attributeDef", AttributeDefType.attr);
    attributeDef.setAssignToGroup(true);
    attributeDef.store();
    AttributeDefName attributeDefName1 = edu.addChildAttributeDefName(attributeDef, "testAttribute1", "testAttribute1");
    AttributeAssignAction action1 = attributeDef.getAttributeDefActionDelegate().addAction("testAction1");
    group.getAttributeDelegate().assignAttribute(action1.getName(), attributeDefName1);

    // populate PIT tables
    ChangeLogTempToEntity.convertRecords();
    
    Set<PITAttributeAssign> results = new PITAttributeAssignQuery()
      .setAttributeDefNameId(attributeDefName1.getId())
      .setActionId(action1.getId())
      .setOwnerGroupId(group.getId())
      .execute();
    assertEquals(1, results.size());
    
    results = new PITAttributeAssignQuery()
      .setAttributeDefNameId(attributeDefName1.getId())
      .setActionId(action1.getId())
      .setOwnerStemId(edu.getUuid())
      .execute();
    assertEquals(0, results.size());
  }
  
  /**
   * 
   */
  public void testByOwnerStem() {
    Stem stem = edu.addChildStem("testStem", "testStem");
    Group group = edu.addChildGroup("testGroup", "testGroup");

    AttributeDef attributeDef = edu.addChildAttributeDef("attributeDef", AttributeDefType.attr);
    attributeDef.setAssignToStem(true);
    attributeDef.store();
    AttributeDefName attributeDefName1 = edu.addChildAttributeDefName(attributeDef, "testAttribute1", "testAttribute1");
    AttributeAssignAction action1 = attributeDef.getAttributeDefActionDelegate().addAction("testAction1");
    stem.getAttributeDelegate().assignAttribute(action1.getName(), attributeDefName1);

    // populate PIT tables
    ChangeLogTempToEntity.convertRecords();
    
    Set<PITAttributeAssign> results = new PITAttributeAssignQuery()
      .setAttributeDefNameId(attributeDefName1.getId())
      .setActionId(action1.getId())
      .setOwnerStemId(stem.getUuid())
      .execute();
    assertEquals(1, results.size());
    
    results = new PITAttributeAssignQuery()
      .setAttributeDefNameId(attributeDefName1.getId())
      .setActionId(action1.getId())
      .setOwnerGroupId(group.getUuid())
      .execute();
    assertEquals(0, results.size());
  }
  
  /**
   * 
   */
  public void testByOwnerAttributeDef() {
    AttributeDef attributeDef0 = edu.addChildAttributeDef("attributeDef0", AttributeDefType.attr);
    Group group = edu.addChildGroup("testGroup", "testGroup");

    AttributeDef attributeDef = edu.addChildAttributeDef("attributeDef", AttributeDefType.attr);
    attributeDef.setAssignToAttributeDef(true);
    attributeDef.store();
    AttributeDefName attributeDefName1 = edu.addChildAttributeDefName(attributeDef, "testAttribute1", "testAttribute1");
    AttributeAssignAction action1 = attributeDef.getAttributeDefActionDelegate().addAction("testAction1");
    attributeDef0.getAttributeDelegate().assignAttribute(action1.getName(), attributeDefName1);

    // populate PIT tables
    ChangeLogTempToEntity.convertRecords();
    
    Set<PITAttributeAssign> results = new PITAttributeAssignQuery()
      .setAttributeDefNameId(attributeDefName1.getId())
      .setActionId(action1.getId())
      .setOwnerAttributeDefId(attributeDef0.getId())
      .execute();
    assertEquals(1, results.size());
    
    results = new PITAttributeAssignQuery()
      .setAttributeDefNameId(attributeDefName1.getId())
      .setActionId(action1.getId())
      .setOwnerGroupId(group.getId())
      .execute();
    assertEquals(0, results.size());
  }
  
  /**
   * 
   */
  public void testByOwnerMember() {
    Member member = MemberFinder.findBySubject(grouperSession, SubjectFinder.findRootSubject(), false);
    Group group = edu.addChildGroup("testGroup", "testGroup");

    AttributeDef attributeDef = edu.addChildAttributeDef("attributeDef", AttributeDefType.attr);
    attributeDef.setAssignToMember(true);
    attributeDef.store();
    AttributeDefName attributeDefName1 = edu.addChildAttributeDefName(attributeDef, "testAttribute1", "testAttribute1");
    AttributeAssignAction action1 = attributeDef.getAttributeDefActionDelegate().addAction("testAction1");
    member.getAttributeDelegate().assignAttribute(action1.getName(), attributeDefName1);

    // populate PIT tables
    ChangeLogTempToEntity.convertRecords();
    
    Set<PITAttributeAssign> results = new PITAttributeAssignQuery()
      .setAttributeDefNameId(attributeDefName1.getId())
      .setActionId(action1.getId())
      .setOwnerMemberId(member.getUuid())
      .execute();
    assertEquals(1, results.size());
    
    results = new PITAttributeAssignQuery()
      .setAttributeDefNameId(attributeDefName1.getId())
      .setActionId(action1.getId())
      .setOwnerGroupId(group.getUuid())
      .execute();
    assertEquals(0, results.size());
  }
  
  /**
   * 
   */
  public void testByOwnerImmediateMembership() {
    Group group = edu.addChildGroup("testGroup", "testGroup");
    group.addMember(SubjectFinder.findRootSubject());
    Membership immediateMembership = MembershipFinder.findImmediateMembership(grouperSession, group, SubjectFinder.findRootSubject(), Group.getDefaultList(), true);

    AttributeDef attributeDef = edu.addChildAttributeDef("attributeDef", AttributeDefType.attr);
    attributeDef.setAssignToImmMembership(true);
    attributeDef.store();
    AttributeDefName attributeDefName1 = edu.addChildAttributeDefName(attributeDef, "testAttribute1", "testAttribute1");
    AttributeAssignAction action1 = attributeDef.getAttributeDefActionDelegate().addAction("testAction1");
    immediateMembership.getAttributeDelegate().assignAttribute(action1.getName(), attributeDefName1);

    // populate PIT tables
    ChangeLogTempToEntity.convertRecords();
    
    Set<PITAttributeAssign> results = new PITAttributeAssignQuery()
      .setAttributeDefNameId(attributeDefName1.getId())
      .setActionId(action1.getId())
      .setOwnerMembershipId(immediateMembership.getImmediateMembershipId())
      .execute();
    assertEquals(1, results.size());
    
    results = new PITAttributeAssignQuery()
      .setAttributeDefNameId(attributeDefName1.getId())
      .setActionId(action1.getId())
      .setOwnerGroupId(group.getUuid())
      .execute();
    assertEquals(0, results.size());
  }
  
  /**
   * 
   */
  public void testByOwnerAttributeAssign() {
    Group group = edu.addChildGroup("testGroup", "testGroup");

    AttributeDef attributeDef0 = edu.addChildAttributeDef("attributeDef0", AttributeDefType.attr);
    attributeDef0.setAssignToGroup(true);
    attributeDef0.store();
    AttributeDefName attributeDefName0 = edu.addChildAttributeDefName(attributeDef0, "testAttribute0", "testAttribute0");
    AttributeAssignAction action0 = attributeDef0.getAttributeDefActionDelegate().addAction("testAction0");
    AttributeAssign attributeAssign0 = group.getAttributeDelegate().assignAttribute(action0.getName(), attributeDefName0).getAttributeAssign();

    AttributeDef attributeDef = edu.addChildAttributeDef("attributeDef", AttributeDefType.attr);
    attributeDef.setAssignToGroupAssn(true);
    attributeDef.store();
    AttributeDefName attributeDefName1 = edu.addChildAttributeDefName(attributeDef, "testAttribute1", "testAttribute1");
    AttributeAssignAction action1 = attributeDef.getAttributeDefActionDelegate().addAction("testAction1");
    attributeAssign0.getAttributeDelegate().assignAttribute(action1.getName(), attributeDefName1);

    // populate PIT tables
    ChangeLogTempToEntity.convertRecords();
    
    Set<PITAttributeAssign> results = new PITAttributeAssignQuery()
      .setAttributeDefNameId(attributeDefName1.getId())
      .setActionId(action1.getId())
      .setOwnerAttributeAssignId(attributeAssign0.getId())
      .execute();
    assertEquals(1, results.size());
    
    results = new PITAttributeAssignQuery()
      .setAttributeDefNameId(attributeDefName1.getId())
      .setActionId(action1.getId())
      .setOwnerGroupId(group.getId())
      .execute();
    assertEquals(0, results.size());
  }

  /**
   * 
   */
  public void testByOwnerEffectiveMembership() {
    Group group = edu.addChildGroup("testGroup", "testGroup");
    Member member = MemberFinder.findBySubject(grouperSession, SubjectFinder.findRootSubject(), false);
    group.addMember(SubjectFinder.findRootSubject());
    Membership immediateMembership = MembershipFinder.findImmediateMembership(grouperSession, group, SubjectFinder.findRootSubject(), Group.getDefaultList(), true);

    AttributeDef attributeDef = edu.addChildAttributeDef("attributeDef", AttributeDefType.attr);
    attributeDef.setAssignToEffMembership(true);
    attributeDef.store();
    AttributeDefName attributeDefName1 = edu.addChildAttributeDefName(attributeDef, "testAttribute1", "testAttribute1");
    AttributeAssignAction action1 = attributeDef.getAttributeDefActionDelegate().addAction("testAction1");
    immediateMembership.getAttributeDelegateEffMship().assignAttribute(action1.getName(), attributeDefName1);

    // populate PIT tables
    ChangeLogTempToEntity.convertRecords();
    
    Set<PITAttributeAssign> results = new PITAttributeAssignQuery()
      .setAttributeDefNameId(attributeDefName1.getId())
      .setActionId(action1.getId())
      .setOwnerMembershipId(immediateMembership.getImmediateMembershipId())
      .execute();
    assertEquals(0, results.size());
    
    results = new PITAttributeAssignQuery()
      .setAttributeDefNameId(attributeDefName1.getId())
      .setActionId(action1.getId())
      .setOwnerGroupId(group.getUuid())
      .execute();
    assertEquals(1, results.size());
    
    results = new PITAttributeAssignQuery()
      .setAttributeDefNameId(attributeDefName1.getId())
      .setActionId(action1.getId())
      .setOwnerMemberId(member.getUuid())
      .execute();
    assertEquals(1, results.size());

    results = new PITAttributeAssignQuery()
      .setAttributeDefNameId(attributeDefName1.getId())
      .setActionId(action1.getId())
      .setOwnerGroupId(group.getUuid())
      .setOwnerMemberId(member.getUuid())
      .execute();
    assertEquals(1, results.size());
  }
  
  /**
   * 
   */
  public void testFindAssignmentsOnAssignmentsWithFromDate() {
    
    Role role1 = edu.addChildRole("testGroup1", "testGroup1");
    Role role2 = edu.addChildRole("testGroup2", "testGroup2");
    
    AttributeDef attributeDef1 = edu.addChildAttributeDef("attributeDef1", AttributeDefType.perm);
    AttributeDef attributeDef2 = edu.addChildAttributeDef("attributeDef2", AttributeDefType.attr);
    attributeDef1.setAssignToGroup(true);
    attributeDef1.store();
    attributeDef2.setAssignToGroupAssn(true);
    attributeDef2.store();
    AttributeDefName attributeDefName1 = edu.addChildAttributeDefName(attributeDef1, "testAttribute1", "testAttribute1");
    AttributeDefName attributeDefName2 = edu.addChildAttributeDefName(attributeDef2, "testAttribute2", "testAttribute2");
    AttributeDefName attributeDefName3 = edu.addChildAttributeDefName(attributeDef2, "testAttribute3", "testAttribute3");
    AttributeAssign attributeAssign1 = role1.getPermissionRoleDelegate().assignRolePermission(attributeDefName1).getAttributeAssign();
    AttributeAssign attributeAssign2 = attributeAssign1.getAttributeDelegate().assignAttribute(attributeDefName2).getAttributeAssign();
    AttributeAssign attributeAssign3 = attributeAssign1.getAttributeDelegate().assignAttribute(attributeDefName3).getAttributeAssign();
    
    AttributeDef attributeDef3 = edu.addChildAttributeDef("attributeDef3", AttributeDefType.perm);
    AttributeDef attributeDef4 = edu.addChildAttributeDef("attributeDef4", AttributeDefType.attr);
    attributeDef3.setAssignToGroup(true);
    attributeDef3.store();
    attributeDef4.setAssignToGroupAssn(true);
    attributeDef4.store();
    AttributeDefName attributeDefName4 = edu.addChildAttributeDefName(attributeDef3, "testAttribute4", "testAttribute4");
    AttributeDefName attributeDefName5 = edu.addChildAttributeDefName(attributeDef4, "testAttribute5", "testAttribute5");
    AttributeDefName attributeDefName6 = edu.addChildAttributeDefName(attributeDef4, "testAttribute6", "testAttribute6");
    AttributeAssign attributeAssign4 = role2.getPermissionRoleDelegate().assignRolePermission(attributeDefName4).getAttributeAssign();
    AttributeAssign attributeAssign5 = attributeAssign4.getAttributeDelegate().assignAttribute(attributeDefName5).getAttributeAssign();
    AttributeAssign attributeAssign6 = attributeAssign4.getAttributeDelegate().assignAttribute(attributeDefName6).getAttributeAssign();
    
    // populate PIT tables
    ChangeLogTempToEntity.convertRecords();
    
    PITAttributeAssign pitAttributeAssign1 = GrouperDAOFactory.getFactory().getPITAttributeAssign().findBySourceIdActive(attributeAssign1.getId(), false);
    PITAttributeAssign pitAttributeAssign2 = GrouperDAOFactory.getFactory().getPITAttributeAssign().findBySourceIdActive(attributeAssign2.getId(), false);
    PITAttributeAssign pitAttributeAssign3 = GrouperDAOFactory.getFactory().getPITAttributeAssign().findBySourceIdActive(attributeAssign3.getId(), false);
    PITAttributeAssign pitAttributeAssign4 = GrouperDAOFactory.getFactory().getPITAttributeAssign().findBySourceIdActive(attributeAssign4.getId(), false);
    PITAttributeAssign pitAttributeAssign5 = GrouperDAOFactory.getFactory().getPITAttributeAssign().findBySourceIdActive(attributeAssign5.getId(), false);
    PITAttributeAssign pitAttributeAssign6 = GrouperDAOFactory.getFactory().getPITAttributeAssign().findBySourceIdActive(attributeAssign6.getId(), false);
    
    Timestamp before = getTimestampWithSleep();
    attributeAssign1.delete();
    attributeAssign4.delete();
    ChangeLogTempToEntity.convertRecords();
    Timestamp after = getTimestampWithSleep();

    
    Set<PITAttributeAssign> assignments = GrouperDAOFactory.getFactory().getPITAttributeAssign().findAssignmentsOnAssignments(
        GrouperUtil.toSet(pitAttributeAssign1), before, null);
    assertEquals(2, assignments.size());  
    assertTrue(assignments.contains(pitAttributeAssign2));
    assertTrue(assignments.contains(pitAttributeAssign3));
    
    assignments = GrouperDAOFactory.getFactory().getPITAttributeAssign().findAssignmentsOnAssignments(
        GrouperUtil.toSet(pitAttributeAssign1, pitAttributeAssign4), before, null);
    assertEquals(4, assignments.size());  
    assertTrue(assignments.contains(pitAttributeAssign2));
    assertTrue(assignments.contains(pitAttributeAssign3));
    assertTrue(assignments.contains(pitAttributeAssign5));
    assertTrue(assignments.contains(pitAttributeAssign6));
    
    assignments = GrouperDAOFactory.getFactory().getPITAttributeAssign().findAssignmentsOnAssignments(
        GrouperUtil.toSet(pitAttributeAssign1), after, null);
    assertEquals(0, assignments.size());  
    
    assignments = GrouperDAOFactory.getFactory().getPITAttributeAssign().findAssignmentsOnAssignments(
        GrouperUtil.toSet(pitAttributeAssign1, pitAttributeAssign4), after, null);
    assertEquals(0, assignments.size());  
  }
  
  /**
   * 
   */
  public void testFindAssignmentsOnAssignmentsWithToDate() {
    
    Role role1 = edu.addChildRole("testGroup1", "testGroup1");
    Role role2 = edu.addChildRole("testGroup2", "testGroup2");
    
    Timestamp before = getTimestampWithSleep();

    AttributeDef attributeDef1 = edu.addChildAttributeDef("attributeDef1", AttributeDefType.perm);
    AttributeDef attributeDef2 = edu.addChildAttributeDef("attributeDef2", AttributeDefType.attr);
    attributeDef1.setAssignToGroup(true);
    attributeDef1.store();
    attributeDef2.setAssignToGroupAssn(true);
    attributeDef2.store();
    AttributeDefName attributeDefName1 = edu.addChildAttributeDefName(attributeDef1, "testAttribute1", "testAttribute1");
    AttributeDefName attributeDefName2 = edu.addChildAttributeDefName(attributeDef2, "testAttribute2", "testAttribute2");
    AttributeDefName attributeDefName3 = edu.addChildAttributeDefName(attributeDef2, "testAttribute3", "testAttribute3");
    AttributeAssign attributeAssign1 = role1.getPermissionRoleDelegate().assignRolePermission(attributeDefName1).getAttributeAssign();
    AttributeAssign attributeAssign2 = attributeAssign1.getAttributeDelegate().assignAttribute(attributeDefName2).getAttributeAssign();
    AttributeAssign attributeAssign3 = attributeAssign1.getAttributeDelegate().assignAttribute(attributeDefName3).getAttributeAssign();
    
    AttributeDef attributeDef3 = edu.addChildAttributeDef("attributeDef3", AttributeDefType.perm);
    AttributeDef attributeDef4 = edu.addChildAttributeDef("attributeDef4", AttributeDefType.attr);
    attributeDef3.setAssignToGroup(true);
    attributeDef3.store();
    attributeDef4.setAssignToGroupAssn(true);
    attributeDef4.store();
    AttributeDefName attributeDefName4 = edu.addChildAttributeDefName(attributeDef3, "testAttribute4", "testAttribute4");
    AttributeDefName attributeDefName5 = edu.addChildAttributeDefName(attributeDef4, "testAttribute5", "testAttribute5");
    AttributeDefName attributeDefName6 = edu.addChildAttributeDefName(attributeDef4, "testAttribute6", "testAttribute6");
    AttributeAssign attributeAssign4 = role2.getPermissionRoleDelegate().assignRolePermission(attributeDefName4).getAttributeAssign();
    AttributeAssign attributeAssign5 = attributeAssign4.getAttributeDelegate().assignAttribute(attributeDefName5).getAttributeAssign();
    AttributeAssign attributeAssign6 = attributeAssign4.getAttributeDelegate().assignAttribute(attributeDefName6).getAttributeAssign();
    
    // populate PIT tables
    ChangeLogTempToEntity.convertRecords();
    Timestamp after = getTimestampWithSleep();
    
    PITAttributeAssign pitAttributeAssign1 = GrouperDAOFactory.getFactory().getPITAttributeAssign().findBySourceIdActive(attributeAssign1.getId(), false);
    PITAttributeAssign pitAttributeAssign2 = GrouperDAOFactory.getFactory().getPITAttributeAssign().findBySourceIdActive(attributeAssign2.getId(), false);
    PITAttributeAssign pitAttributeAssign3 = GrouperDAOFactory.getFactory().getPITAttributeAssign().findBySourceIdActive(attributeAssign3.getId(), false);
    PITAttributeAssign pitAttributeAssign4 = GrouperDAOFactory.getFactory().getPITAttributeAssign().findBySourceIdActive(attributeAssign4.getId(), false);
    PITAttributeAssign pitAttributeAssign5 = GrouperDAOFactory.getFactory().getPITAttributeAssign().findBySourceIdActive(attributeAssign5.getId(), false);
    PITAttributeAssign pitAttributeAssign6 = GrouperDAOFactory.getFactory().getPITAttributeAssign().findBySourceIdActive(attributeAssign6.getId(), false);
   
    
    Set<PITAttributeAssign> assignments = GrouperDAOFactory.getFactory().getPITAttributeAssign().findAssignmentsOnAssignments(
        GrouperUtil.toSet(pitAttributeAssign1), null, before);
    assertEquals(0, assignments.size());
    
    assignments = GrouperDAOFactory.getFactory().getPITAttributeAssign().findAssignmentsOnAssignments(
        GrouperUtil.toSet(pitAttributeAssign1, pitAttributeAssign4), null, before);
    assertEquals(0, assignments.size());
    
    assignments = GrouperDAOFactory.getFactory().getPITAttributeAssign().findAssignmentsOnAssignments(
        GrouperUtil.toSet(pitAttributeAssign1), null, after);
    assertEquals(2, assignments.size());  
    assertTrue(assignments.contains(pitAttributeAssign2));
    assertTrue(assignments.contains(pitAttributeAssign3));
    
    assignments = GrouperDAOFactory.getFactory().getPITAttributeAssign().findAssignmentsOnAssignments(
        GrouperUtil.toSet(pitAttributeAssign1, pitAttributeAssign4), null, after);
    assertEquals(4, assignments.size());
    assertTrue(assignments.contains(pitAttributeAssign2));
    assertTrue(assignments.contains(pitAttributeAssign3));
    assertTrue(assignments.contains(pitAttributeAssign5));
    assertTrue(assignments.contains(pitAttributeAssign6));
    
    attributeAssign1.delete();
    attributeAssign4.delete();
    ChangeLogTempToEntity.convertRecords();
    Timestamp afterDelete = getTimestampWithSleep();
    
    assignments = GrouperDAOFactory.getFactory().getPITAttributeAssign().findAssignmentsOnAssignments(
        GrouperUtil.toSet(pitAttributeAssign1), null, afterDelete);
    assertEquals(2, assignments.size());  
    assertTrue(assignments.contains(pitAttributeAssign2));
    assertTrue(assignments.contains(pitAttributeAssign3));
    
    assignments = GrouperDAOFactory.getFactory().getPITAttributeAssign().findAssignmentsOnAssignments(
        GrouperUtil.toSet(pitAttributeAssign1, pitAttributeAssign4), null, afterDelete);
    assertEquals(4, assignments.size());
    assertTrue(assignments.contains(pitAttributeAssign2));
    assertTrue(assignments.contains(pitAttributeAssign3));
    assertTrue(assignments.contains(pitAttributeAssign5));
    assertTrue(assignments.contains(pitAttributeAssign6));
  }
  
  /**
   * 
   */
  public void testFindAssignmentsOnAssignmentsAtPointInTime() {
    
    Role role1 = edu.addChildRole("testGroup1", "testGroup1");
    Role role2 = edu.addChildRole("testGroup2", "testGroup2");
    
    Timestamp before = getTimestampWithSleep();

    AttributeDef attributeDef1 = edu.addChildAttributeDef("attributeDef1", AttributeDefType.perm);
    AttributeDef attributeDef2 = edu.addChildAttributeDef("attributeDef2", AttributeDefType.attr);
    attributeDef1.setAssignToGroup(true);
    attributeDef1.store();
    attributeDef2.setAssignToGroupAssn(true);
    attributeDef2.store();
    AttributeDefName attributeDefName1 = edu.addChildAttributeDefName(attributeDef1, "testAttribute1", "testAttribute1");
    AttributeDefName attributeDefName2 = edu.addChildAttributeDefName(attributeDef2, "testAttribute2", "testAttribute2");
    AttributeDefName attributeDefName3 = edu.addChildAttributeDefName(attributeDef2, "testAttribute3", "testAttribute3");
    AttributeAssign attributeAssign1 = role1.getPermissionRoleDelegate().assignRolePermission(attributeDefName1).getAttributeAssign();
    AttributeAssign attributeAssign2 = attributeAssign1.getAttributeDelegate().assignAttribute(attributeDefName2).getAttributeAssign();
    AttributeAssign attributeAssign3 = attributeAssign1.getAttributeDelegate().assignAttribute(attributeDefName3).getAttributeAssign();
    
    AttributeDef attributeDef3 = edu.addChildAttributeDef("attributeDef3", AttributeDefType.perm);
    AttributeDef attributeDef4 = edu.addChildAttributeDef("attributeDef4", AttributeDefType.attr);
    attributeDef3.setAssignToGroup(true);
    attributeDef3.store();
    attributeDef4.setAssignToGroupAssn(true);
    attributeDef4.store();
    AttributeDefName attributeDefName4 = edu.addChildAttributeDefName(attributeDef3, "testAttribute4", "testAttribute4");
    AttributeDefName attributeDefName5 = edu.addChildAttributeDefName(attributeDef4, "testAttribute5", "testAttribute5");
    AttributeDefName attributeDefName6 = edu.addChildAttributeDefName(attributeDef4, "testAttribute6", "testAttribute6");
    AttributeAssign attributeAssign4 = role2.getPermissionRoleDelegate().assignRolePermission(attributeDefName4).getAttributeAssign();
    AttributeAssign attributeAssign5 = attributeAssign4.getAttributeDelegate().assignAttribute(attributeDefName5).getAttributeAssign();
    AttributeAssign attributeAssign6 = attributeAssign4.getAttributeDelegate().assignAttribute(attributeDefName6).getAttributeAssign();
    
    // populate PIT tables
    ChangeLogTempToEntity.convertRecords();
    Timestamp after = getTimestampWithSleep();
    
    PITAttributeAssign pitAttributeAssign1 = GrouperDAOFactory.getFactory().getPITAttributeAssign().findBySourceIdActive(attributeAssign1.getId(), false);
    PITAttributeAssign pitAttributeAssign2 = GrouperDAOFactory.getFactory().getPITAttributeAssign().findBySourceIdActive(attributeAssign2.getId(), false);
    PITAttributeAssign pitAttributeAssign3 = GrouperDAOFactory.getFactory().getPITAttributeAssign().findBySourceIdActive(attributeAssign3.getId(), false);
    PITAttributeAssign pitAttributeAssign4 = GrouperDAOFactory.getFactory().getPITAttributeAssign().findBySourceIdActive(attributeAssign4.getId(), false);
    PITAttributeAssign pitAttributeAssign5 = GrouperDAOFactory.getFactory().getPITAttributeAssign().findBySourceIdActive(attributeAssign5.getId(), false);
    PITAttributeAssign pitAttributeAssign6 = GrouperDAOFactory.getFactory().getPITAttributeAssign().findBySourceIdActive(attributeAssign6.getId(), false);
   
    
    Set<PITAttributeAssign> assignments = GrouperDAOFactory.getFactory().getPITAttributeAssign().findAssignmentsOnAssignments(
        GrouperUtil.toSet(pitAttributeAssign1), before, before);
    assertEquals(0, assignments.size());
    
    assignments = GrouperDAOFactory.getFactory().getPITAttributeAssign().findAssignmentsOnAssignments(
        GrouperUtil.toSet(pitAttributeAssign1, pitAttributeAssign4), before, before);
    assertEquals(0, assignments.size());
    
    assignments = GrouperDAOFactory.getFactory().getPITAttributeAssign().findAssignmentsOnAssignments(
        GrouperUtil.toSet(pitAttributeAssign1), after, after);
    assertEquals(2, assignments.size());  
    assertTrue(assignments.contains(pitAttributeAssign2));
    assertTrue(assignments.contains(pitAttributeAssign3));
    
    assignments = GrouperDAOFactory.getFactory().getPITAttributeAssign().findAssignmentsOnAssignments(
        GrouperUtil.toSet(pitAttributeAssign1, pitAttributeAssign4), after, after);
    assertEquals(4, assignments.size());
    assertTrue(assignments.contains(pitAttributeAssign2));
    assertTrue(assignments.contains(pitAttributeAssign3));
    assertTrue(assignments.contains(pitAttributeAssign5));
    assertTrue(assignments.contains(pitAttributeAssign6));
    
    attributeAssign1.delete();
    attributeAssign4.delete();
    ChangeLogTempToEntity.convertRecords();
    Timestamp afterDelete = getTimestampWithSleep();
    
    assignments = GrouperDAOFactory.getFactory().getPITAttributeAssign().findAssignmentsOnAssignments(
        GrouperUtil.toSet(pitAttributeAssign1), afterDelete, afterDelete);
    assertEquals(0, assignments.size());
    
    assignments = GrouperDAOFactory.getFactory().getPITAttributeAssign().findAssignmentsOnAssignments(
        GrouperUtil.toSet(pitAttributeAssign1, pitAttributeAssign4), afterDelete, afterDelete);
    assertEquals(0, assignments.size());
  }
  
  /**
   * 
   */
  public void testFindAssignmentsOnAssignmentsPrivs() {
    
    Timestamp before = getTimestampWithSleep();

    Role role = edu.addChildRole("testGroup", "testGroup");
    Group group = GrouperDAOFactory.getFactory().getGroup().findByUuid(role.getId(), true);
    Member member0 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ0, true);
    Member member1 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ1, true);
    group.addMember(member1.getSubject(), true);
    
    AttributeDef attributeDef1 = edu.addChildAttributeDef("attributeDef1", AttributeDefType.perm);
    AttributeDef attributeDef2 = edu.addChildAttributeDef("attributeDef2", AttributeDefType.attr);
    attributeDef1.setAssignToGroup(true);
    attributeDef1.store();
    attributeDef2.setAssignToGroupAssn(true);
    attributeDef2.store();
    AttributeDefName attributeDefName1 = edu.addChildAttributeDefName(attributeDef1, "testAttribute1", "testAttribute1");
    AttributeDefName attributeDefName2 = edu.addChildAttributeDefName(attributeDef2, "testAttribute2", "testAttribute2");
    AttributeAssign attributeAssign1 = role.getPermissionRoleDelegate().assignRolePermission(attributeDefName1).getAttributeAssign();
    AttributeAssign attributeAssign2 = attributeAssign1.getAttributeDelegate().assignAttribute(attributeDefName2).getAttributeAssign();
    
    // populate PIT tables
    ChangeLogTempToEntity.convertRecords();
    
    PITAttributeAssign pitAttributeAssign1 = GrouperDAOFactory.getFactory().getPITAttributeAssign().findBySourceIdActive(attributeAssign1.getId(), false);
    
    Timestamp after = getTimestampWithSleep();

    Set<PITAttributeAssign> assignments = GrouperDAOFactory.getFactory().getPITAttributeAssign().findAssignmentsOnAssignments(
        GrouperUtil.toSet(pitAttributeAssign1), before, after);
    assertEquals(1, assignments.size());  
    assertEquals(attributeAssign2.getId(), assignments.iterator().next().getSourceId());
    
    GrouperSession s = GrouperSession.start(member0.getSubject());
    assignments = GrouperDAOFactory.getFactory().getPITAttributeAssign().findAssignmentsOnAssignments(
        GrouperUtil.toSet(pitAttributeAssign1), before, after);
    assertEquals(0, assignments.size());
    s.stop();
    
    s = GrouperSession.startRootSession();
    attributeDef2.getPrivilegeDelegate().grantPriv(member0.getSubject(), AttributeDefPrivilege.ATTR_READ, true);
    s = GrouperSession.start(member0.getSubject());
    assignments = GrouperDAOFactory.getFactory().getPITAttributeAssign().findAssignmentsOnAssignments(
        GrouperUtil.toSet(pitAttributeAssign1), before, after);
    assertEquals(1, assignments.size());
    assertEquals(attributeAssign2.getId(), assignments.iterator().next().getSourceId());
    s.stop();
    
    s = GrouperSession.startRootSession();
    attributeAssign2.delete();
    ChangeLogTempToEntity.convertRecords();
    s = GrouperSession.start(member0.getSubject());
    assignments = GrouperDAOFactory.getFactory().getPITAttributeAssign().findAssignmentsOnAssignments(
        GrouperUtil.toSet(pitAttributeAssign1), before, after);
    assertEquals(0, assignments.size());
    s.stop();
    
    s = GrouperSession.startRootSession();
    assignments = GrouperDAOFactory.getFactory().getPITAttributeAssign().findAssignmentsOnAssignments(
        GrouperUtil.toSet(pitAttributeAssign1), before, after);
    assertEquals(1, assignments.size());
    assertEquals(attributeAssign2.getId(), assignments.iterator().next().getSourceId());
  }
  
  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new PITAttributeAssignTests("testFindAssignmentsOnAssignmentsPrivs"));
  }
}
