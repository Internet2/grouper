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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.MembershipFinder;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.AttributeDefNameSave;
import edu.internet2.middleware.grouper.attr.AttributeDefSave;
import edu.internet2.middleware.grouper.attr.AttributeDefType;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignAction;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.changeLog.ChangeLogTempToEntity;
import edu.internet2.middleware.grouper.group.TypeOfGroup;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SessionHelper;
import edu.internet2.middleware.grouper.helper.StemHelper;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.permissions.PermissionAllowed;
import edu.internet2.middleware.grouper.permissions.PermissionEntry;
import edu.internet2.middleware.grouper.permissions.PermissionEntryImpl;
import edu.internet2.middleware.grouper.permissions.PermissionFinder;
import edu.internet2.middleware.grouper.permissions.PermissionProcessor;
import edu.internet2.middleware.grouper.permissions.PermissionEntry.PermissionType;
import edu.internet2.middleware.grouper.permissions.role.Role;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.AttributeDefPrivilege;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * @author shilen
 * $Id$
 */
public class PITPermissionTests extends GrouperTest {
  
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
  public PITPermissionTests(String name) {
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
    Role group = edu.addChildRole("testGroup", "testGroup");
    Member newMember1 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ1, true);
    group.addMember(newMember1.getSubject(), true);
    
    AttributeDef attributeDef = edu.addChildAttributeDef("attributeDef", AttributeDefType.perm);
    attributeDef.setAssignToGroup(true);
    attributeDef.store();
    AttributeDefName attributeDefName1 = edu.addChildAttributeDefName(attributeDef, "testAttribute1", "testAttribute1");
    AttributeAssignAction action1 = attributeDef.getAttributeDefActionDelegate().addAction("testAction1");

    Date beforeAddTime = getDateWithSleep();
    group.getPermissionRoleDelegate().assignRolePermission("testAction1", attributeDefName1, PermissionAllowed.ALLOWED).getAttributeAssign();
    Date afterAddTime = getDateWithSleep();

    // populate PIT tables
    ChangeLogTempToEntity.convertRecords();
    
    Set<PITPermissionAllView> results = new PITPermissionAllViewQuery()
      .setAttributeDefNameSourceId(attributeDefName1.getId())
      .setMemberSourceId(newMember1.getUuid())
      .setActionSourceId(action1.getId())
      .setStartDateAfter(beforeAddTime)
      .execute();
    assertEquals(1, results.size());
    
    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameSourceId(attributeDefName1.getId())
      .setMemberSourceId(newMember1.getUuid())
      .setActionSourceId(action1.getId())
      .setStartDateAfter(afterAddTime)
      .execute();
    assertEquals(0, results.size());    

    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameSourceId(attributeDefName1.getId())
      .setMemberSourceId(newMember1.getUuid())
      .setActionSourceId(action1.getId())
      .setStartDateBefore(beforeAddTime)
      .execute();
    assertEquals(0, results.size());

    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameSourceId(attributeDefName1.getId())
      .setMemberSourceId(newMember1.getUuid())
      .setActionSourceId(action1.getId())
      .setStartDateBefore(afterAddTime)
      .execute();
    assertEquals(1, results.size());

    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameSourceId(attributeDefName1.getId())
      .setMemberSourceId(newMember1.getUuid())
      .setActionSourceId(action1.getId())
      .execute();
    assertEquals(1, results.size());
  }
  
  /**
   * 
   */
  public void testEndTime() {
    Role group = edu.addChildRole("testGroup", "testGroup");
    Member newMember1 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ1, true);
    group.addMember(newMember1.getSubject(), true);
    
    AttributeDef attributeDef = edu.addChildAttributeDef("attributeDef", AttributeDefType.perm);
    attributeDef.setAssignToGroup(true);
    attributeDef.store();
    AttributeDefName attributeDefName1 = edu.addChildAttributeDefName(attributeDef, "testAttribute1", "testAttribute1");
    AttributeAssignAction action1 = attributeDef.getAttributeDefActionDelegate().addAction("testAction1");

    AttributeAssign attributeAssign = group.getPermissionRoleDelegate().assignRolePermission("testAction1", attributeDefName1, PermissionAllowed.ALLOWED).getAttributeAssign();
    Date afterAddTime = getDateWithSleep();
    
    // populate PIT tables
    ChangeLogTempToEntity.convertRecords();
    
    Set<PITPermissionAllView> results = new PITPermissionAllViewQuery()
      .setAttributeDefNameSourceId(attributeDefName1.getId())
      .setMemberSourceId(newMember1.getUuid())
      .setActionSourceId(action1.getId())
      .setEndDateAfter(afterAddTime)
      .execute();
    assertEquals(1, results.size());
    
    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameSourceId(attributeDefName1.getId())
      .setMemberSourceId(newMember1.getUuid())
      .setActionSourceId(action1.getId())
      .setEndDateBefore(afterAddTime)
      .execute();
    assertEquals(0, results.size());
    
    Date beforeDeleteTime = getDateWithSleep();
    attributeAssign.delete();
    Date afterDeleteTime = getDateWithSleep();

    // populate PIT tables
    ChangeLogTempToEntity.convertRecords();
    
    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameSourceId(attributeDefName1.getId())
      .setMemberSourceId(newMember1.getUuid())
      .setActionSourceId(action1.getId())
      .setEndDateAfter(afterDeleteTime)
      .execute();
    assertEquals(0, results.size());
    
    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameSourceId(attributeDefName1.getId())
      .setMemberSourceId(newMember1.getUuid())
      .setActionSourceId(action1.getId())
      .setEndDateAfter(beforeDeleteTime)
      .execute();
    assertEquals(1, results.size());    

    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameSourceId(attributeDefName1.getId())
      .setMemberSourceId(newMember1.getUuid())
      .setActionSourceId(action1.getId())
      .setEndDateBefore(afterDeleteTime)
      .execute();
    assertEquals(1, results.size());

    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameSourceId(attributeDefName1.getId())
      .setMemberSourceId(newMember1.getUuid())
      .setActionSourceId(action1.getId())
      .setEndDateBefore(beforeDeleteTime)
      .execute();
    assertEquals(0, results.size());
  }
  
  /**
   * 
   */
  public void testPermissionEnableDisable() {
    Role group = edu.addChildRole("testGroup", "testGroup");
    Member newMember1 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ1, true);
    Member newMember2 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ2, true);
    group.addMember(newMember1.getSubject(), true);
    
    AttributeDef attributeDef = edu.addChildAttributeDef("attributeDef", AttributeDefType.perm);
    attributeDef.setAssignToGroup(true);
    attributeDef.store();
    AttributeDefName attributeDefName1 = edu.addChildAttributeDefName(attributeDef, "testAttribute1", "testAttribute1");
    AttributeDefName attributeDefName2 = edu.addChildAttributeDefName(attributeDef, "testAttribute2", "testAttribute2");
    AttributeAssignAction action1 = attributeDef.getAttributeDefActionDelegate().addAction("testAction1");
    AttributeAssignAction action2 = attributeDef.getAttributeDefActionDelegate().addAction("testAction2");

    Date beforeAddTime = getDateWithSleep();
    
    AttributeAssign attributeAssign = group.getPermissionRoleDelegate().assignRolePermission("testAction1", attributeDefName1, PermissionAllowed.ALLOWED).getAttributeAssign();
    
    Date afterAddTime = getDateWithSleep();
    Date beforeDisableTime = getDateWithSleep();
    
    attributeAssign.setEnabled(false);
    attributeAssign.setEnabledTime(new Timestamp(new Date().getTime() + 100000));
    attributeAssign.saveOrUpdate(true);
    
    Date afterDisableTime = getDateWithSleep();
    Date beforeEnableTime = getDateWithSleep();

    attributeAssign.setEnabled(true);
    attributeAssign.setEnabledTime(new Timestamp(new Date().getTime() - 100000));
    attributeAssign.saveOrUpdate(true);
    
    Date afterEnableTime = getDateWithSleep();
    
    // populate PIT tables
    ChangeLogTempToEntity.convertRecords();
    
    // delete everything to make sure PIT records stay around
    attributeAssign.delete();
    action1.delete();
    attributeDefName1.delete();
    attributeDef.delete();
    group.delete();
    ChangeLogTempToEntity.convertRecords();

    Set<PITPermissionAllView> results = new PITPermissionAllViewQuery()
      .setAttributeDefNameSourceId(attributeDefName1.getId())
      .setMemberSourceId(newMember1.getUuid())
      .setActionSourceId(action1.getId())
      .setActiveDateRange(new Date(beforeAddTime.getTime() - 1), beforeAddTime)
      .execute();
    assertEquals(0, results.size());
    
    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameSourceId(attributeDefName1.getId())
      .setMemberSourceId(newMember1.getUuid())
      .setActionSourceId(action1.getId())
      .setActiveDateRange(null, beforeAddTime)
      .execute();
    assertEquals(0, results.size());
    
    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameSourceId(attributeDefName1.getId())
      .setMemberSourceId(newMember1.getUuid())
      .setActionSourceId(action1.getId())
      .setActiveDateRange(beforeAddTime, afterAddTime)
      .execute();
    assertEquals(1, results.size());
    
    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameSourceId(attributeDefName1.getId())
      .setMemberSourceId(newMember1.getUuid())
      .setActionSourceId(action1.getId())
      .setActiveDateRange(afterAddTime, beforeDisableTime)
      .execute();
    assertEquals(1, results.size());
    
    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameSourceId(attributeDefName1.getId())
      .setMemberSourceId(newMember1.getUuid())
      .setActionSourceId(action1.getId())
      .setActiveDateRange(beforeDisableTime, afterDisableTime)
      .execute();
    assertEquals(1, results.size());
    
    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameSourceId(attributeDefName1.getId())
      .setMemberSourceId(newMember1.getUuid())
      .setActionSourceId(action1.getId())
      .setActiveDateRange(afterDisableTime, beforeEnableTime)
      .execute();
    assertEquals(0, results.size());    
    
    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameSourceId(attributeDefName1.getId())
      .setMemberSourceId(newMember1.getUuid())
      .setActionSourceId(action1.getId())
      .setActiveDateRange(beforeEnableTime, afterEnableTime)
      .execute();
    assertEquals(1, results.size());
    
    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameSourceId(attributeDefName1.getId())
      .setMemberSourceId(newMember1.getUuid())
      .setActionSourceId(action1.getId())
      .setActiveDateRange(afterEnableTime, null)
      .execute();
    assertEquals(1, results.size());
    
    // test ids that weren't used at all
    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameSourceId(attributeDefName2.getId())
      .setMemberSourceId(newMember1.getUuid())
      .setActionSourceId(action1.getId())
      .setActiveDateRange(afterEnableTime, null)
      .execute();
    assertEquals(0, results.size());
    
    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameSourceId(attributeDefName1.getId())
      .setMemberSourceId(newMember2.getUuid())
      .setActionSourceId(action1.getId())
      .setActiveDateRange(afterEnableTime, null)
      .execute();
    assertEquals(0, results.size());
    
    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameSourceId(attributeDefName1.getId())
      .setMemberSourceId(newMember1.getUuid())
      .setActionSourceId(action2.getId())
      .setActiveDateRange(afterEnableTime, null)
      .execute();
    assertEquals(0, results.size());
  }
  
  /**
   * 
   */
  public void testPermissionByRoleAssign() {
    Role group = edu.addChildRole("testGroup", "testGroup");
    Member newMember1 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ1, true);
    Member newMember2 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ2, true);
    group.addMember(newMember1.getSubject(), true);
    
    AttributeDef attributeDef = edu.addChildAttributeDef("attributeDef", AttributeDefType.perm);
    attributeDef.setAssignToGroup(true);
    attributeDef.store();
    AttributeDefName attributeDefName1 = edu.addChildAttributeDefName(attributeDef, "testAttribute1", "testAttribute1");
    AttributeDefName attributeDefName2 = edu.addChildAttributeDefName(attributeDef, "testAttribute2", "testAttribute2");
    AttributeAssignAction action1 = attributeDef.getAttributeDefActionDelegate().addAction("testAction1");
    AttributeAssignAction action2 = attributeDef.getAttributeDefActionDelegate().addAction("testAction2");

    Date beforeAddTime = getDateWithSleep();
    
    group.getPermissionRoleDelegate().assignRolePermission("testAction1", attributeDefName1, PermissionAllowed.ALLOWED).getAttributeAssign();
    
    Date afterAddTime = getDateWithSleep();
    
    // populate PIT tables
    ChangeLogTempToEntity.convertRecords();

    Set<PITPermissionAllView> results = new PITPermissionAllViewQuery()
      .setAttributeDefNameSourceId(attributeDefName1.getId())
      .setMemberSourceId(newMember1.getUuid())
      .setActionSourceId(action1.getId())
      .setActiveDateRange(new Date(beforeAddTime.getTime() - 1), beforeAddTime)
      .execute();
    assertEquals(0, results.size());
    
    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameSourceId(attributeDefName1.getId())
      .setMemberSourceId(newMember1.getUuid())
      .setActionSourceId(action1.getId())
      .setActiveDateRange(null, beforeAddTime)
      .execute();
    assertEquals(0, results.size());
    
    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameSourceId(attributeDefName1.getId())
      .setMemberSourceId(newMember1.getUuid())
      .setActionSourceId(action1.getId())
      .setActiveDateRange(beforeAddTime, afterAddTime)
      .execute();
    assertEquals(1, results.size());
    
    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameSourceId(attributeDefName1.getId())
      .setMemberSourceId(newMember1.getUuid())
      .setActionSourceId(action1.getId())
      .setActiveDateRange(afterAddTime, null)
      .execute();
    assertEquals(1, results.size());
    
    // test ids that weren't used at all
    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameSourceId(attributeDefName2.getId())
      .setMemberSourceId(newMember1.getUuid())
      .setActionSourceId(action1.getId())
      .setActiveDateRange(afterAddTime, null)
      .execute();
    assertEquals(0, results.size());

    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameSourceId(attributeDefName1.getId())
      .setMemberSourceId(newMember2.getUuid())
      .setActionSourceId(action1.getId())
      .setActiveDateRange(afterAddTime, null)
      .execute();
    assertEquals(0, results.size());

    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameSourceId(attributeDefName1.getId())
      .setMemberSourceId(newMember1.getUuid())
      .setActionSourceId(action2.getId())
      .setActiveDateRange(afterAddTime, null)
      .execute();
    assertEquals(0, results.size());
  }
  
  /**
   * 
   */
  public void testPermissionByRoleAssignAndRemove() {
    Role group = edu.addChildRole("testGroup", "testGroup");
    Member newMember1 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ1, true);
    Member newMember2 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ2, true);
    group.addMember(newMember1.getSubject(), true);
    
    AttributeDef attributeDef = edu.addChildAttributeDef("attributeDef", AttributeDefType.perm);
    attributeDef.setAssignToGroup(true);
    attributeDef.store();
    AttributeDefName attributeDefName1 = edu.addChildAttributeDefName(attributeDef, "testAttribute1", "testAttribute1");
    AttributeDefName attributeDefName2 = edu.addChildAttributeDefName(attributeDef, "testAttribute2", "testAttribute2");
    AttributeAssignAction action1 = attributeDef.getAttributeDefActionDelegate().addAction("testAction1");
    AttributeAssignAction action2 = attributeDef.getAttributeDefActionDelegate().addAction("testAction2");

    Date beforeAddTime = getDateWithSleep();
    
    AttributeAssign attributeAssign = group.getPermissionRoleDelegate().assignRolePermission("testAction1", attributeDefName1, PermissionAllowed.ALLOWED).getAttributeAssign();
    
    Date afterAddTime = getDateWithSleep();
    Date beforeRemoveTime = getDateWithSleep();
    
    attributeAssign.delete();
    
    Date afterRemoveTime = getDateWithSleep();
    
    // populate PIT tables
    ChangeLogTempToEntity.convertRecords();
    
    // delete everything to make sure PIT records stay around
    action1.delete();
    attributeDefName1.delete();
    attributeDef.delete();
    group.delete();
    ChangeLogTempToEntity.convertRecords();

    Set<PITPermissionAllView> results = new PITPermissionAllViewQuery()
      .setAttributeDefNameSourceId(attributeDefName1.getId())
      .setMemberSourceId(newMember1.getUuid())
      .setActionSourceId(action1.getId())
      .setActiveDateRange(new Date(beforeAddTime.getTime() - 1), beforeAddTime)
      .execute();
    assertEquals(0, results.size());
    
    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameSourceId(attributeDefName1.getId())
      .setMemberSourceId(newMember1.getUuid())
      .setActionSourceId(action1.getId())
      .setActiveDateRange(null, beforeAddTime)
      .execute();
    assertEquals(0, results.size());
    
    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameSourceId(attributeDefName1.getId())
      .setMemberSourceId(newMember1.getUuid())
      .setActionSourceId(action1.getId())
      .setActiveDateRange(beforeAddTime, afterAddTime)
      .execute();
    assertEquals(1, results.size());
    
    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameSourceId(attributeDefName1.getId())
      .setMemberSourceId(newMember1.getUuid())
      .setActionSourceId(action1.getId())
      .setActiveDateRange(afterAddTime, beforeRemoveTime)
      .execute();
    assertEquals(1, results.size());
    
    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameSourceId(attributeDefName1.getId())
      .setMemberSourceId(newMember1.getUuid())
      .setActionSourceId(action1.getId())
      .setActiveDateRange(beforeRemoveTime, afterRemoveTime)
      .execute();
    assertEquals(1, results.size());
    
    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameSourceId(attributeDefName1.getId())
      .setMemberSourceId(newMember1.getUuid())
      .setActionSourceId(action1.getId())
      .setActiveDateRange(afterRemoveTime, null)
      .execute();
    assertEquals(0, results.size());
    
    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameSourceId(attributeDefName1.getId())
      .setMemberSourceId(newMember1.getUuid())
      .setActionSourceId(action1.getId())
      .setActiveDateRange(beforeRemoveTime, null)
      .execute();
    assertEquals(1, results.size());
    
    // test ids that weren't used at all
    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameSourceId(attributeDefName2.getId())
      .setMemberSourceId(newMember1.getUuid())
      .setActionSourceId(action1.getId())
      .setActiveDateRange(beforeRemoveTime, null)
      .execute();
    assertEquals(0, results.size());
    
    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameSourceId(attributeDefName1.getId())
      .setMemberSourceId(newMember2.getUuid())
      .setActionSourceId(action1.getId())
      .setActiveDateRange(beforeRemoveTime, null)
      .execute();
    assertEquals(0, results.size());
    
    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameSourceId(attributeDefName1.getId())
      .setMemberSourceId(newMember1.getUuid())
      .setActionSourceId(action2.getId())
      .setActiveDateRange(beforeRemoveTime, null)
      .execute();
    assertEquals(0, results.size());
  }
  
  /**
   * 
   */
  public void testPermissionBySubjectRoleAssign() {
    Role group = edu.addChildRole("testGroup", "testGroup");
    Member newMember1 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ1, true);
    Member newMember2 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ2, true);
    group.addMember(newMember1.getSubject(), true);
    group.addMember(newMember2.getSubject(), true);
    
    AttributeDef attributeDef = edu.addChildAttributeDef("attributeDef", AttributeDefType.perm);
    attributeDef.setAssignToEffMembership(true);
    attributeDef.store();
    AttributeDefName attributeDefName1 = edu.addChildAttributeDefName(attributeDef, "testAttribute1", "testAttribute1");
    AttributeDefName attributeDefName2 = edu.addChildAttributeDefName(attributeDef, "testAttribute2", "testAttribute2");
    AttributeAssignAction action1 = attributeDef.getAttributeDefActionDelegate().addAction("testAction1");
    AttributeAssignAction action2 = attributeDef.getAttributeDefActionDelegate().addAction("testAction2");

    Date beforeAddTime = getDateWithSleep();
    
    group.getPermissionRoleDelegate().assignSubjectRolePermission("testAction1", attributeDefName1, newMember1, PermissionAllowed.ALLOWED).getAttributeAssign();
    
    Date afterAddTime = getDateWithSleep();
    
    // populate PIT tables
    ChangeLogTempToEntity.convertRecords();

    Set<PITPermissionAllView> results = new PITPermissionAllViewQuery()
      .setAttributeDefNameSourceId(attributeDefName1.getId())
      .setMemberSourceId(newMember1.getUuid())
      .setActionSourceId(action1.getId())
      .setActiveDateRange(new Date(beforeAddTime.getTime() - 1), beforeAddTime)
      .execute();
    assertEquals(0, results.size());
    
    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameSourceId(attributeDefName1.getId())
      .setMemberSourceId(newMember1.getUuid())
      .setActionSourceId(action1.getId())
      .setActiveDateRange(null, beforeAddTime)
      .execute();
    assertEquals(0, results.size());
    
    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameSourceId(attributeDefName1.getId())
      .setMemberSourceId(newMember1.getUuid())
      .setActionSourceId(action1.getId())
      .setActiveDateRange(beforeAddTime, afterAddTime)
      .execute();
    assertEquals(1, results.size());
    
    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameSourceId(attributeDefName1.getId())
      .setMemberSourceId(newMember1.getUuid())
      .setActionSourceId(action1.getId())
      .setActiveDateRange(afterAddTime, null)
      .execute();
    assertEquals(1, results.size());
    
    // test ids that weren't used at all
    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameSourceId(attributeDefName2.getId())
      .setMemberSourceId(newMember1.getUuid())
      .setActionSourceId(action1.getId())
      .setActiveDateRange(afterAddTime, null)
      .execute();
    assertEquals(0, results.size());

    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameSourceId(attributeDefName1.getId())
      .setMemberSourceId(newMember2.getUuid())
      .setActionSourceId(action1.getId())
      .setActiveDateRange(afterAddTime, null)
      .execute();
    assertEquals(0, results.size());

    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameSourceId(attributeDefName1.getId())
      .setMemberSourceId(newMember1.getUuid())
      .setActionSourceId(action2.getId())
      .setActiveDateRange(afterAddTime, null)
      .execute();
    assertEquals(0, results.size());
  }
  

  /**
   * 
   */
  public void testPermissionByMembership() {
    Role group = edu.addChildRole("testGroup", "testGroup");
    Member newMember1 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ1, true);
    Member newMember2 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ2, true);
    
    AttributeDef attributeDef = edu.addChildAttributeDef("attributeDef", AttributeDefType.perm);
    attributeDef.setAssignToGroup(true);
    attributeDef.store();
    AttributeDefName attributeDefName1 = edu.addChildAttributeDefName(attributeDef, "testAttribute1", "testAttribute1");
    AttributeDefName attributeDefName2 = edu.addChildAttributeDefName(attributeDef, "testAttribute2", "testAttribute2");
    AttributeAssignAction action1 = attributeDef.getAttributeDefActionDelegate().addAction("testAction1");
    AttributeAssignAction action2 = attributeDef.getAttributeDefActionDelegate().addAction("testAction2");
    AttributeAssign attributeAssign = group.getPermissionRoleDelegate().assignRolePermission("testAction1", attributeDefName1, PermissionAllowed.ALLOWED).getAttributeAssign();

    Date beforeAddTime = getDateWithSleep();
    
    group.addMember(newMember1.getSubject(), true);

    Date afterAddTime = getDateWithSleep();
    Date beforeRemoveTime = getDateWithSleep();
    
    group.deleteMember(newMember1.getSubject(), true);
    
    Date afterRemoveTime = getDateWithSleep();
    
    // populate PIT tables
    ChangeLogTempToEntity.convertRecords();
    
    // delete everything to make sure PIT records stay around
    attributeAssign.delete();
    action1.delete();
    attributeDefName1.delete();
    attributeDef.delete();
    group.delete();
    ChangeLogTempToEntity.convertRecords();

    Set<PITPermissionAllView> results = new PITPermissionAllViewQuery()
      .setAttributeDefNameSourceId(attributeDefName1.getId())
      .setMemberSourceId(newMember1.getUuid())
      .setActionSourceId(action1.getId())
      .setActiveDateRange(new Date(beforeAddTime.getTime() - 1), beforeAddTime)
      .execute();
    assertEquals(0, results.size());
    
    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameSourceId(attributeDefName1.getId())
      .setMemberSourceId(newMember1.getUuid())
      .setActionSourceId(action1.getId())
      .setActiveDateRange(null, beforeAddTime)
      .execute();
    assertEquals(0, results.size());
    
    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameSourceId(attributeDefName1.getId())
      .setMemberSourceId(newMember1.getUuid())
      .setActionSourceId(action1.getId())
      .setActiveDateRange(beforeAddTime, afterAddTime)
      .execute();
    assertEquals(1, results.size());
    
    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameSourceId(attributeDefName1.getId())
      .setMemberSourceId(newMember1.getUuid())
      .setActionSourceId(action1.getId())
      .setActiveDateRange(afterAddTime, beforeRemoveTime)
      .execute();
    assertEquals(1, results.size());
    
    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameSourceId(attributeDefName1.getId())
      .setMemberSourceId(newMember1.getUuid())
      .setActionSourceId(action1.getId())
      .setActiveDateRange(beforeRemoveTime, afterRemoveTime)
      .execute();
    assertEquals(1, results.size());
    
    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameSourceId(attributeDefName1.getId())
      .setMemberSourceId(newMember1.getUuid())
      .setActionSourceId(action1.getId())
      .setActiveDateRange(afterRemoveTime, null)
      .execute();
    assertEquals(0, results.size());
    
    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameSourceId(attributeDefName1.getId())
      .setMemberSourceId(newMember1.getUuid())
      .setActionSourceId(action1.getId())
      .setActiveDateRange(beforeRemoveTime, null)
      .execute();
    assertEquals(1, results.size());
    
    // test ids that weren't used at all
    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameSourceId(attributeDefName2.getId())
      .setMemberSourceId(newMember1.getUuid())
      .setActionSourceId(action1.getId())
      .setActiveDateRange(beforeRemoveTime, null)
      .execute();
    assertEquals(0, results.size());
    
    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameSourceId(attributeDefName1.getId())
      .setMemberSourceId(newMember2.getUuid())
      .setActionSourceId(action1.getId())
      .setActiveDateRange(beforeRemoveTime, null)
      .execute();
    assertEquals(0, results.size());
    
    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameSourceId(attributeDefName1.getId())
      .setMemberSourceId(newMember1.getUuid())
      .setActionSourceId(action2.getId())
      .setActiveDateRange(beforeRemoveTime, null)
      .execute();
    assertEquals(0, results.size());
  }
  

  /**
   * 
   */
  public void testPermissionByGroupSet() {
    Role group = edu.addChildRole("testGroup", "testGroup");
    Group group2 = edu.addChildGroup("testGroup2", "testGroup2");
    Member newMember1 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ1, true);
    Member newMember2 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ2, true);
    group2.addMember(newMember1.getSubject());
    
    AttributeDef attributeDef = edu.addChildAttributeDef("attributeDef", AttributeDefType.perm);
    attributeDef.setAssignToGroup(true);
    attributeDef.store();
    AttributeDefName attributeDefName1 = edu.addChildAttributeDefName(attributeDef, "testAttribute1", "testAttribute1");
    AttributeDefName attributeDefName2 = edu.addChildAttributeDefName(attributeDef, "testAttribute2", "testAttribute2");
    AttributeAssignAction action1 = attributeDef.getAttributeDefActionDelegate().addAction("testAction1");
    AttributeAssignAction action2 = attributeDef.getAttributeDefActionDelegate().addAction("testAction2");
    AttributeAssign attributeAssign = group.getPermissionRoleDelegate().assignRolePermission("testAction1", attributeDefName1, PermissionAllowed.ALLOWED).getAttributeAssign();

    Date beforeAddTime = getDateWithSleep();
    
    group.addMember(group2.toSubject(), true);
    
    // populate PIT tables
    ChangeLogTempToEntity.convertRecords();

    Date afterAddTime = getDateWithSleep();
    Date beforeRemoveTime = getDateWithSleep();

    group.deleteMember(group2.toSubject(), true);
    
    Date afterRemoveTime = getDateWithSleep();
    
    // populate PIT tables
    ChangeLogTempToEntity.convertRecords();
    
    // delete everything to make sure PIT records stay around
    attributeAssign.delete();
    action1.delete();
    attributeDefName1.delete();
    attributeDef.delete();
    group.delete();
    ChangeLogTempToEntity.convertRecords();

    Set<PITPermissionAllView> results = new PITPermissionAllViewQuery()
      .setAttributeDefNameSourceId(attributeDefName1.getId())
      .setMemberSourceId(newMember1.getUuid())
      .setActionSourceId(action1.getId())
      .setActiveDateRange(new Date(beforeAddTime.getTime() - 1), beforeAddTime)
      .execute();
    assertEquals(0, results.size());
    
    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameSourceId(attributeDefName1.getId())
      .setMemberSourceId(newMember1.getUuid())
      .setActionSourceId(action1.getId())
      .setActiveDateRange(null, beforeAddTime)
      .execute();
    assertEquals(0, results.size());
    
    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameSourceId(attributeDefName1.getId())
      .setMemberSourceId(newMember1.getUuid())
      .setActionSourceId(action1.getId())
      .setActiveDateRange(beforeAddTime, afterAddTime)
      .execute();
    assertEquals(1, results.size());
    
    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameSourceId(attributeDefName1.getId())
      .setMemberSourceId(newMember1.getUuid())
      .setActionSourceId(action1.getId())
      .setActiveDateRange(afterAddTime, beforeRemoveTime)
      .execute();
    assertEquals(1, results.size());
    
    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameSourceId(attributeDefName1.getId())
      .setMemberSourceId(newMember1.getUuid())
      .setActionSourceId(action1.getId())
      .setActiveDateRange(beforeRemoveTime, afterRemoveTime)
      .execute();
    assertEquals(1, results.size());
    
    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameSourceId(attributeDefName1.getId())
      .setMemberSourceId(newMember1.getUuid())
      .setActionSourceId(action1.getId())
      .setActiveDateRange(afterRemoveTime, null)
      .execute();
    assertEquals(0, results.size());
    
    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameSourceId(attributeDefName1.getId())
      .setMemberSourceId(newMember1.getUuid())
      .setActionSourceId(action1.getId())
      .setActiveDateRange(beforeRemoveTime, null)
      .execute();
    assertEquals(1, results.size());
    
    // test ids that weren't used at all
    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameSourceId(attributeDefName2.getId())
      .setMemberSourceId(newMember1.getUuid())
      .setActionSourceId(action1.getId())
      .setActiveDateRange(beforeRemoveTime, null)
      .execute();
    assertEquals(0, results.size());
    
    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameSourceId(attributeDefName1.getId())
      .setMemberSourceId(newMember2.getUuid())
      .setActionSourceId(action1.getId())
      .setActiveDateRange(beforeRemoveTime, null)
      .execute();
    assertEquals(0, results.size());
    
    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameSourceId(attributeDefName1.getId())
      .setMemberSourceId(newMember1.getUuid())
      .setActionSourceId(action2.getId())
      .setActiveDateRange(beforeRemoveTime, null)
      .execute();
    assertEquals(0, results.size());
  }
  
  /**
   * 
   */
  public void testPermissionByActionSet() {
    Role group = edu.addChildRole("testGroup", "testGroup");
    Member newMember1 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ1, true);
    Member newMember2 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ2, true);
    group.addMember(newMember1.getSubject(), true);
    
    AttributeDef attributeDef = edu.addChildAttributeDef("attributeDef", AttributeDefType.perm);
    attributeDef.setAssignToGroup(true);
    attributeDef.store();
    AttributeDefName attributeDefName1 = edu.addChildAttributeDefName(attributeDef, "testAttribute1", "testAttribute1");
    AttributeDefName attributeDefName2 = edu.addChildAttributeDefName(attributeDef, "testAttribute2", "testAttribute2");
    AttributeAssignAction action1 = attributeDef.getAttributeDefActionDelegate().addAction("testAction1");
    AttributeAssignAction action2 = attributeDef.getAttributeDefActionDelegate().addAction("testAction2");
    AttributeAssignAction action3 = attributeDef.getAttributeDefActionDelegate().addAction("testAction3");
    AttributeAssignAction action4 = attributeDef.getAttributeDefActionDelegate().addAction("testAction4");
    AttributeAssign attributeAssign = group.getPermissionRoleDelegate().assignRolePermission("testAction1", attributeDefName1, PermissionAllowed.ALLOWED).getAttributeAssign();

    action1.getAttributeAssignActionSetDelegate().addToAttributeAssignActionSet(action2);
    
    Date beforeAddTime = getDateWithSleep();
    
    action2.getAttributeAssignActionSetDelegate().addToAttributeAssignActionSet(action3);

    Date afterAddTime = getDateWithSleep();
    Date beforeRemoveTime = getDateWithSleep();

    action2.getAttributeAssignActionSetDelegate().removeFromAttributeAssignActionSet(action3);
    
    Date afterRemoveTime = getDateWithSleep();
    
    // populate PIT tables
    ChangeLogTempToEntity.convertRecords();
    
    // delete everything to make sure PIT records stay around
    attributeAssign.delete();
    action1.delete();
    action2.delete();
    action3.delete();
    action4.delete();
    attributeDefName1.delete();
    attributeDef.delete();
    group.delete();
    ChangeLogTempToEntity.convertRecords();

    Set<PITPermissionAllView> results = new PITPermissionAllViewQuery()
      .setAttributeDefNameSourceId(attributeDefName1.getId())
      .setMemberSourceId(newMember1.getUuid())
      .setActionSourceId(action3.getId())
      .setActiveDateRange(new Date(beforeAddTime.getTime() - 1), beforeAddTime)
      .execute();
    assertEquals(0, results.size());
    
    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameSourceId(attributeDefName1.getId())
      .setMemberSourceId(newMember1.getUuid())
      .setActionSourceId(action3.getId())
      .setActiveDateRange(null, beforeAddTime)
      .execute();
    assertEquals(0, results.size());
    
    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameSourceId(attributeDefName1.getId())
      .setMemberSourceId(newMember1.getUuid())
      .setActionSourceId(action3.getId())
      .setActiveDateRange(beforeAddTime, afterAddTime)
      .execute();
    assertEquals(1, results.size());
    
    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameSourceId(attributeDefName1.getId())
      .setMemberSourceId(newMember1.getUuid())
      .setActionSourceId(action3.getId())
      .setActiveDateRange(afterAddTime, beforeRemoveTime)
      .execute();
    assertEquals(1, results.size());
    
    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameSourceId(attributeDefName1.getId())
      .setMemberSourceId(newMember1.getUuid())
      .setActionSourceId(action3.getId())
      .setActiveDateRange(beforeRemoveTime, afterRemoveTime)
      .execute();
    assertEquals(1, results.size());
    
    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameSourceId(attributeDefName1.getId())
      .setMemberSourceId(newMember1.getUuid())
      .setActionSourceId(action3.getId())
      .setActiveDateRange(afterRemoveTime, null)
      .execute();
    assertEquals(0, results.size());
    
    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameSourceId(attributeDefName1.getId())
      .setMemberSourceId(newMember1.getUuid())
      .setActionSourceId(action3.getId())
      .setActiveDateRange(beforeRemoveTime, null)
      .execute();
    assertEquals(1, results.size());
    
    // test ids that weren't used at all
    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameSourceId(attributeDefName2.getId())
      .setMemberSourceId(newMember1.getUuid())
      .setActionSourceId(action3.getId())
      .setActiveDateRange(beforeRemoveTime, null)
      .execute();
    assertEquals(0, results.size());
    
    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameSourceId(attributeDefName1.getId())
      .setMemberSourceId(newMember2.getUuid())
      .setActionSourceId(action3.getId())
      .setActiveDateRange(beforeRemoveTime, null)
      .execute();
    assertEquals(0, results.size());
    
    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameSourceId(attributeDefName1.getId())
      .setMemberSourceId(newMember1.getUuid())
      .setActionSourceId(action4.getId())
      .setActiveDateRange(beforeRemoveTime, null)
      .execute();
    assertEquals(0, results.size());
  }
  
  /**
   * 
   */
  public void testPermissionByAttributeDefNameSet() {
    Role group = edu.addChildRole("testGroup", "testGroup");
    Member newMember1 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ1, true);
    Member newMember2 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ2, true);
    group.addMember(newMember1.getSubject(), true);
    
    AttributeDef attributeDef = edu.addChildAttributeDef("attributeDef", AttributeDefType.perm);
    attributeDef.setAssignToGroup(true);
    attributeDef.store();
    AttributeDefName attributeDefName1 = edu.addChildAttributeDefName(attributeDef, "testAttribute1", "testAttribute1");
    AttributeDefName attributeDefName2 = edu.addChildAttributeDefName(attributeDef, "testAttribute2", "testAttribute2");
    AttributeDefName attributeDefName3 = edu.addChildAttributeDefName(attributeDef, "testAttribute3", "testAttribute3");
    AttributeDefName attributeDefName4 = edu.addChildAttributeDefName(attributeDef, "testAttribute4", "testAttribute4");
    AttributeAssignAction action1 = attributeDef.getAttributeDefActionDelegate().addAction("testAction1");
    AttributeAssignAction action2 = attributeDef.getAttributeDefActionDelegate().addAction("testAction2");
    AttributeAssign attributeAssign = group.getPermissionRoleDelegate().assignRolePermission("testAction1", attributeDefName1, PermissionAllowed.ALLOWED).getAttributeAssign();

    attributeDefName1.getAttributeDefNameSetDelegate().addToAttributeDefNameSet(attributeDefName2);
    
    Date beforeAddTime = getDateWithSleep();
    
    attributeDefName2.getAttributeDefNameSetDelegate().addToAttributeDefNameSet(attributeDefName3);

    Date afterAddTime = getDateWithSleep();
    Date beforeRemoveTime = getDateWithSleep();

    attributeDefName2.getAttributeDefNameSetDelegate().removeFromAttributeDefNameSet(attributeDefName3);
    
    Date afterRemoveTime = getDateWithSleep();
    
    // populate PIT tables
    ChangeLogTempToEntity.convertRecords();
    
    // delete everything to make sure PIT records stay around
    attributeAssign.delete();
    action1.delete();
    action2.delete();
    attributeDefName1.delete();
    attributeDefName2.delete();
    attributeDefName3.delete();
    attributeDefName4.delete();
    attributeDef.delete();
    group.delete();
    ChangeLogTempToEntity.convertRecords();

    Set<PITPermissionAllView> results = new PITPermissionAllViewQuery()
      .setAttributeDefNameSourceId(attributeDefName3.getId())
      .setMemberSourceId(newMember1.getUuid())
      .setActionSourceId(action1.getId())
      .setActiveDateRange(new Date(beforeAddTime.getTime() - 1), beforeAddTime)
      .execute();
    assertEquals(0, results.size());
    
    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameSourceId(attributeDefName3.getId())
      .setMemberSourceId(newMember1.getUuid())
      .setActionSourceId(action1.getId())
      .setActiveDateRange(null, beforeAddTime)
      .execute();
    assertEquals(0, results.size());
    
    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameSourceId(attributeDefName3.getId())
      .setMemberSourceId(newMember1.getUuid())
      .setActionSourceId(action1.getId())
      .setActiveDateRange(beforeAddTime, afterAddTime)
      .execute();
    assertEquals(1, results.size());
    
    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameSourceId(attributeDefName3.getId())
      .setMemberSourceId(newMember1.getUuid())
      .setActionSourceId(action1.getId())
      .setActiveDateRange(afterAddTime, beforeRemoveTime)
      .execute();
    assertEquals(1, results.size());
    
    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameSourceId(attributeDefName3.getId())
      .setMemberSourceId(newMember1.getUuid())
      .setActionSourceId(action1.getId())
      .setActiveDateRange(beforeRemoveTime, afterRemoveTime)
      .execute();
    assertEquals(1, results.size());
    
    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameSourceId(attributeDefName3.getId())
      .setMemberSourceId(newMember1.getUuid())
      .setActionSourceId(action1.getId())
      .setActiveDateRange(afterRemoveTime, null)
      .execute();
    assertEquals(0, results.size());
    
    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameSourceId(attributeDefName3.getId())
      .setMemberSourceId(newMember1.getUuid())
      .setActionSourceId(action1.getId())
      .setActiveDateRange(beforeRemoveTime, null)
      .execute();
    assertEquals(1, results.size());
    
    // test ids that weren't used at all
    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameSourceId(attributeDefName4.getId())
      .setMemberSourceId(newMember1.getUuid())
      .setActionSourceId(action1.getId())
      .setActiveDateRange(beforeRemoveTime, null)
      .execute();
    assertEquals(0, results.size());
    
    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameSourceId(attributeDefName3.getId())
      .setMemberSourceId(newMember2.getUuid())
      .setActionSourceId(action1.getId())
      .setActiveDateRange(beforeRemoveTime, null)
      .execute();
    assertEquals(0, results.size());
    
    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameSourceId(attributeDefName3.getId())
      .setMemberSourceId(newMember1.getUuid())
      .setActionSourceId(action2.getId())
      .setActiveDateRange(beforeRemoveTime, null)
      .execute();
    assertEquals(0, results.size());
  }
  
  /**
   * 
   */
  public void testPermissionByRoleSet() {
    Role group1 = edu.addChildRole("testGroup1", "testGroup1");
    Role group2 = edu.addChildRole("testGroup2", "testGroup2");
    Role group3 = edu.addChildRole("testGroup3", "testGroup3");
    Member newMember1 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ1, true);
    Member newMember2 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ2, true);
    group1.addMember(newMember1.getSubject(), true);
    
    AttributeDef attributeDef = edu.addChildAttributeDef("attributeDef", AttributeDefType.perm);
    attributeDef.setAssignToGroup(true);
    attributeDef.store();
    AttributeDefName attributeDefName1 = edu.addChildAttributeDefName(attributeDef, "testAttribute1", "testAttribute1");
    AttributeDefName attributeDefName2 = edu.addChildAttributeDefName(attributeDef, "testAttribute2", "testAttribute2");
    AttributeDefName attributeDefName3 = edu.addChildAttributeDefName(attributeDef, "testAttribute3", "testAttribute3");
    AttributeDefName attributeDefName4 = edu.addChildAttributeDefName(attributeDef, "testAttribute4", "testAttribute4");
    AttributeAssignAction action1 = attributeDef.getAttributeDefActionDelegate().addAction("testAction1");
    AttributeAssignAction action2 = attributeDef.getAttributeDefActionDelegate().addAction("testAction2");
    AttributeAssign attributeAssign1 = group1.getPermissionRoleDelegate().assignRolePermission("testAction1", attributeDefName1, PermissionAllowed.ALLOWED).getAttributeAssign();
    AttributeAssign attributeAssign2 = group2.getPermissionRoleDelegate().assignRolePermission("testAction1", attributeDefName2, PermissionAllowed.ALLOWED).getAttributeAssign();
    AttributeAssign attributeAssign3 = group3.getPermissionRoleDelegate().assignRolePermission("testAction1", attributeDefName3, PermissionAllowed.ALLOWED).getAttributeAssign();

    group1.getRoleInheritanceDelegate().addRoleToInheritFromThis(group2);
    
    Date beforeAddTime = getDateWithSleep();
    
    group2.getRoleInheritanceDelegate().addRoleToInheritFromThis(group3);

    Date afterAddTime = getDateWithSleep();
    Date beforeRemoveTime = getDateWithSleep();

    group2.getRoleInheritanceDelegate().removeRoleFromInheritFromThis(group3);
    
    Date afterRemoveTime = getDateWithSleep();
    
    // populate PIT tables
    ChangeLogTempToEntity.convertRecords();
    
    // delete everything to make sure PIT records stay around
    attributeAssign1.delete();
    attributeAssign2.delete();
    attributeAssign3.delete();
    action1.delete();
    action2.delete();
    attributeDefName1.delete();
    attributeDefName2.delete();
    attributeDefName3.delete();
    attributeDefName4.delete();
    attributeDef.delete();
    group1.delete();
    group2.delete();
    group3.delete();
    ChangeLogTempToEntity.convertRecords();

    Set<PITPermissionAllView> results = new PITPermissionAllViewQuery()
      .setAttributeDefNameSourceId(attributeDefName3.getId())
      .setMemberSourceId(newMember1.getUuid())
      .setActionSourceId(action1.getId())
      .setActiveDateRange(new Date(beforeAddTime.getTime() - 1), beforeAddTime)
      .execute();
    assertEquals(0, results.size());
    
    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameSourceId(attributeDefName3.getId())
      .setMemberSourceId(newMember1.getUuid())
      .setActionSourceId(action1.getId())
      .setActiveDateRange(null, beforeAddTime)
      .execute();
    assertEquals(0, results.size());
    
    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameSourceId(attributeDefName3.getId())
      .setMemberSourceId(newMember1.getUuid())
      .setActionSourceId(action1.getId())
      .setActiveDateRange(beforeAddTime, afterAddTime)
      .execute();
    assertEquals(1, results.size());
    
    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameSourceId(attributeDefName3.getId())
      .setMemberSourceId(newMember1.getUuid())
      .setActionSourceId(action1.getId())
      .setActiveDateRange(afterAddTime, beforeRemoveTime)
      .execute();
    assertEquals(1, results.size());
    
    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameSourceId(attributeDefName3.getId())
      .setMemberSourceId(newMember1.getUuid())
      .setActionSourceId(action1.getId())
      .setActiveDateRange(beforeRemoveTime, afterRemoveTime)
      .execute();
    assertEquals(1, results.size());
    
    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameSourceId(attributeDefName3.getId())
      .setMemberSourceId(newMember1.getUuid())
      .setActionSourceId(action1.getId())
      .setActiveDateRange(afterRemoveTime, null)
      .execute();
    assertEquals(0, results.size());
    
    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameSourceId(attributeDefName3.getId())
      .setMemberSourceId(newMember1.getUuid())
      .setActionSourceId(action1.getId())
      .setActiveDateRange(beforeRemoveTime, null)
      .execute();
    assertEquals(1, results.size());
    
    // test ids that weren't used at all
    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameSourceId(attributeDefName4.getId())
      .setMemberSourceId(newMember1.getUuid())
      .setActionSourceId(action1.getId())
      .setActiveDateRange(beforeRemoveTime, null)
      .execute();
    assertEquals(0, results.size());
    
    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameSourceId(attributeDefName3.getId())
      .setMemberSourceId(newMember2.getUuid())
      .setActionSourceId(action1.getId())
      .setActiveDateRange(beforeRemoveTime, null)
      .execute();
    assertEquals(0, results.size());
    
    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameSourceId(attributeDefName3.getId())
      .setMemberSourceId(newMember1.getUuid())
      .setActionSourceId(action2.getId())
      .setActiveDateRange(beforeRemoveTime, null)
      .execute();
    assertEquals(0, results.size());
  }
  
  /**
   * 
   */
  public void testMultiplePermissionsByRoleSet() {
    Role group1 = edu.addChildRole("testGroup1", "testGroup1");
    Role group2 = edu.addChildRole("testGroup2", "testGroup2");
    Role group3 = edu.addChildRole("testGroup3", "testGroup3");
    Member newMember1 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ1, true);
    group1.addMember(newMember1.getSubject(), true);
    
    AttributeDef attributeDef = edu.addChildAttributeDef("attributeDef", AttributeDefType.perm);
    attributeDef.setAssignToGroup(true);
    attributeDef.store();
    AttributeDefName attributeDefName1 = edu.addChildAttributeDefName(attributeDef, "testAttribute1", "testAttribute1");
    AttributeDefName attributeDefName2 = edu.addChildAttributeDefName(attributeDef, "testAttribute2", "testAttribute2");
    AttributeDefName attributeDefName3 = edu.addChildAttributeDefName(attributeDef, "testAttribute3", "testAttribute3");
    AttributeAssignAction action1 = attributeDef.getAttributeDefActionDelegate().addAction("testAction1");
    group1.getPermissionRoleDelegate().assignRolePermission("testAction1", attributeDefName1, PermissionAllowed.ALLOWED).getAttributeAssign();
    group2.getPermissionRoleDelegate().assignRolePermission("testAction1", attributeDefName2, PermissionAllowed.ALLOWED).getAttributeAssign();
    group3.getPermissionRoleDelegate().assignRolePermission("testAction1", attributeDefName3, PermissionAllowed.ALLOWED).getAttributeAssign();

    group1.getRoleInheritanceDelegate().addRoleToInheritFromThis(group2);
    
    Date beforeFirstAddTime = getDateWithSleep();
    
    group2.getRoleInheritanceDelegate().addRoleToInheritFromThis(group3);

    Date afterFirstAddTime = getDateWithSleep();
    Date beforeSecondAddTime = getDateWithSleep();

    group1.getRoleInheritanceDelegate().addRoleToInheritFromThis(group3);
    
    Date afterSecondAddTime = getDateWithSleep();
    
    // populate PIT tables
    ChangeLogTempToEntity.convertRecords();

    Set<PITPermissionAllView> results = new PITPermissionAllViewQuery()
      .setAttributeDefNameSourceId(attributeDefName3.getId())
      .setMemberSourceId(newMember1.getUuid())
      .setActionSourceId(action1.getId())
      .setActiveDateRange(new Date(beforeFirstAddTime.getTime() - 1), beforeFirstAddTime)
      .execute();
    assertEquals(0, results.size());
    
    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameSourceId(attributeDefName3.getId())
      .setMemberSourceId(newMember1.getUuid())
      .setActionSourceId(action1.getId())
      .setActiveDateRange(null, beforeFirstAddTime)
      .execute();
    assertEquals(0, results.size());
    
    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameSourceId(attributeDefName3.getId())
      .setMemberSourceId(newMember1.getUuid())
      .setActionSourceId(action1.getId())
      .setActiveDateRange(beforeFirstAddTime, afterFirstAddTime)
      .execute();
    assertEquals(1, results.size());
    
    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameSourceId(attributeDefName3.getId())
      .setMemberSourceId(newMember1.getUuid())
      .setActionSourceId(action1.getId())
      .setActiveDateRange(afterFirstAddTime, beforeSecondAddTime)
      .execute();
    assertEquals(1, results.size());
    
    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameSourceId(attributeDefName3.getId())
      .setMemberSourceId(newMember1.getUuid())
      .setActionSourceId(action1.getId())
      .setActiveDateRange(beforeSecondAddTime, afterSecondAddTime)
      .execute();
    assertEquals(2, results.size());
    
    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameSourceId(attributeDefName3.getId())
      .setMemberSourceId(newMember1.getUuid())
      .setActionSourceId(action1.getId())
      .setActiveDateRange(afterSecondAddTime, null)
      .execute();
    assertEquals(2, results.size());
  }
  
  /**
   * 
   */
  public void testFindPermissionsWithFromDate() {

    Role role = edu.addChildRole("testGroup", "testGroup");
    Group group = GrouperDAOFactory.getFactory().getGroup().findByUuid(role.getId(), true);
    Member member1 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ1, true);
    group.addMember(member1.getSubject(), true);
    
    AttributeDef attributeDef = edu.addChildAttributeDef("attributeDef", AttributeDefType.perm);
    attributeDef.setAssignToGroup(true);
    attributeDef.store();
    AttributeDefName attributeDefName1 = edu.addChildAttributeDefName(attributeDef, "testAttribute1", "testAttribute1");
    AttributeAssignAction action1 = attributeDef.getAttributeDefActionDelegate().addAction("testAction1");
    AttributeAssignAction action2 = attributeDef.getAttributeDefActionDelegate().addAction("testAction2");
    AttributeAssign attributeAssign1 = role.getPermissionRoleDelegate().assignRolePermission(action1.getName(), attributeDefName1, PermissionAllowed.ALLOWED).getAttributeAssign();
    AttributeAssign attributeAssign2 = role.getPermissionRoleDelegate().assignRolePermission(action2.getName(), attributeDefName1, PermissionAllowed.ALLOWED).getAttributeAssign();

    // populate PIT tables
    ChangeLogTempToEntity.convertRecords();
    
    Timestamp before = getTimestampWithSleep();
    attributeAssign1.delete();
    ChangeLogTempToEntity.convertRecords();
    Timestamp after = getTimestampWithSleep();

    Set<PermissionEntry> perms = new PermissionFinder()
      .addPermissionDefId(attributeDef.getId())
      .addPermissionNameId(attributeDefName1.getId())
      .addRoleId(role.getId())
      .addMemberId(member1.getUuid())
      .assignEnabled(true)
      .assignPointInTimeFrom(before)
      .findPermissions();
    
    assertEquals(2, perms.size());
    
    perms = new PermissionFinder()
      .addPermissionDefId(attributeDef.getId())
      .addPermissionNameId(attributeDefName1.getId())
      .addRoleId(role.getId())
      .addMemberId(member1.getUuid())
      .assignEnabled(true)
      .assignPointInTimeFrom(after)
      .findPermissions();
    
    assertEquals(1, perms.size());
    PITAttributeAssign pitAttributeAssign2 = GrouperDAOFactory.getFactory().getPITAttributeAssign().findBySourceIdActive(attributeAssign2.getId(), true);
    assertEquals(pitAttributeAssign2.getId(), perms.iterator().next().getAttributeAssignId());
  }
  
  /**
   * 
   */
  public void testFindPermissionsWithToDate() {

    Role role = edu.addChildRole("testGroup", "testGroup");
    Group group = GrouperDAOFactory.getFactory().getGroup().findByUuid(role.getId(), true);
    Member member1 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ1, true);
    group.addMember(member1.getSubject(), true);
    
    AttributeDef attributeDef = edu.addChildAttributeDef("attributeDef", AttributeDefType.perm);
    attributeDef.setAssignToGroup(true);
    attributeDef.store();
    AttributeDefName attributeDefName1 = edu.addChildAttributeDefName(attributeDef, "testAttribute1", "testAttribute1");
    AttributeAssignAction action1 = attributeDef.getAttributeDefActionDelegate().addAction("testAction1");
    AttributeAssignAction action2 = attributeDef.getAttributeDefActionDelegate().addAction("testAction2");
    
    // populate PIT tables
    ChangeLogTempToEntity.convertRecords();
    
    Timestamp before = getTimestampWithSleep();
    AttributeAssign attributeAssign1 = role.getPermissionRoleDelegate().assignRolePermission(action1.getName(), attributeDefName1, PermissionAllowed.ALLOWED).getAttributeAssign();
    role.getPermissionRoleDelegate().assignRolePermission(action2.getName(), attributeDefName1, PermissionAllowed.ALLOWED).getAttributeAssign();
    ChangeLogTempToEntity.convertRecords();
    Timestamp after = getTimestampWithSleep();

    Set<PermissionEntry> perms = new PermissionFinder()
      .addPermissionDefId(attributeDef.getId())
      .addPermissionNameId(attributeDefName1.getId())
      .addRoleId(role.getId())
      .addMemberId(member1.getUuid())
      .assignEnabled(true)
      .assignPointInTimeTo(before)
      .findPermissions();

    assertEquals(0, perms.size());
    
    perms = new PermissionFinder()
      .addPermissionDefId(attributeDef.getId())
      .addPermissionNameId(attributeDefName1.getId())
      .addRoleId(role.getId())
      .addMemberId(member1.getUuid())
      .assignEnabled(true)
      .assignPointInTimeTo(after)
      .findPermissions();
      
    assertEquals(2, perms.size());
    
    
    attributeAssign1.delete();
    ChangeLogTempToEntity.convertRecords();
    Timestamp afterDelete = getTimestampWithSleep();

    perms = new PermissionFinder()
      .addPermissionDefId(attributeDef.getId())
      .addPermissionNameId(attributeDefName1.getId())
      .addRoleId(role.getId())
      .addMemberId(member1.getUuid())
      .assignEnabled(true)
      .assignPointInTimeTo(afterDelete)
      .findPermissions();
      
    assertEquals(2, perms.size());
  }
  
  /**
   * 
   */
  public void testFindPermissionsAtPointInTime() {

    Role role = edu.addChildRole("testGroup", "testGroup");
    Group group = GrouperDAOFactory.getFactory().getGroup().findByUuid(role.getId(), true);
    Member member1 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ1, true);
    group.addMember(member1.getSubject(), true);
    
    AttributeDef attributeDef = edu.addChildAttributeDef("attributeDef", AttributeDefType.perm);
    attributeDef.setAssignToGroup(true);
    attributeDef.store();
    AttributeDefName attributeDefName1 = edu.addChildAttributeDefName(attributeDef, "testAttribute1", "testAttribute1");
    AttributeAssignAction action1 = attributeDef.getAttributeDefActionDelegate().addAction("testAction1");
    AttributeAssignAction action2 = attributeDef.getAttributeDefActionDelegate().addAction("testAction2");
    
    // populate PIT tables
    ChangeLogTempToEntity.convertRecords();
    
    Timestamp before = getTimestampWithSleep();
    AttributeAssign attributeAssign1 = role.getPermissionRoleDelegate().assignRolePermission(action1.getName(), attributeDefName1, PermissionAllowed.ALLOWED).getAttributeAssign();
    AttributeAssign attributeAssign2 = role.getPermissionRoleDelegate().assignRolePermission(action2.getName(), attributeDefName1, PermissionAllowed.ALLOWED).getAttributeAssign();
    ChangeLogTempToEntity.convertRecords();
    Timestamp after = getTimestampWithSleep();

    Set<PermissionEntry> perms = new PermissionFinder()
      .addPermissionDefId(attributeDef.getId())
      .addPermissionNameId(attributeDefName1.getId())
      .addRoleId(role.getId())
      .addMemberId(member1.getUuid())
      .assignEnabled(true)
      .assignPointInTimeFrom(before)
      .assignPointInTimeTo(before)
      .findPermissions();
    
    assertEquals(0, perms.size());
    
    perms = new PermissionFinder()
      .addPermissionDefId(attributeDef.getId())
      .addPermissionNameId(attributeDefName1.getId())
      .addRoleId(role.getId())
      .addMemberId(member1.getUuid())
      .assignEnabled(true)
      .assignPointInTimeFrom(after)
      .assignPointInTimeTo(after)
      .findPermissions();
    assertEquals(2, perms.size());
    
    // try with permission processors
    perms = new PermissionFinder()
      .addPermissionDefId(attributeDef.getId())
      .addPermissionNameId(attributeDefName1.getId())
      .addRoleId(role.getId())
      .addMemberId(member1.getUuid())
      .assignEnabled(true)
      .assignPermissionProcessor(PermissionProcessor.FILTER_REDUNDANT_PERMISSIONS)
      .assignPointInTimeFrom(after)
      .assignPointInTimeTo(after)
      .findPermissions();
    assertEquals(2, perms.size());
    
    perms = new PermissionFinder()
      .addPermissionDefId(attributeDef.getId())
      .addPermissionNameId(attributeDefName1.getId())
      .addRoleId(role.getId())
      .addMemberId(member1.getUuid())
      .assignEnabled(true)
      .assignPermissionProcessor(PermissionProcessor.FILTER_REDUNDANT_PERMISSIONS_AND_ROLES)
      .assignPointInTimeFrom(after)
      .assignPointInTimeTo(after)
      .findPermissions();
    assertEquals(2, perms.size());
    
    attributeAssign1.delete();
    ChangeLogTempToEntity.convertRecords();
    Timestamp afterDelete = getTimestampWithSleep();

    perms = new PermissionFinder()
      .addPermissionDefId(attributeDef.getId())
      .addPermissionNameId(attributeDefName1.getId())
      .addRoleId(role.getId())
      .addMemberId(member1.getUuid())
      .assignEnabled(true)
      .assignPointInTimeFrom(afterDelete)
      .assignPointInTimeTo(afterDelete)
      .findPermissions();
    assertEquals(1, perms.size());
    PITAttributeAssign pitAttributeAssign2 = GrouperDAOFactory.getFactory().getPITAttributeAssign().findBySourceIdActive(attributeAssign2.getId(), true);
    assertEquals(pitAttributeAssign2.getId(), perms.iterator().next().getAttributeAssignId());
  }
  
  /**
   * 
   */
  public void testFindPermissionsPrivs() {
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.create.grant.all.read", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.create.grant.all.view", "false");
    
    Timestamp before = getTimestampWithSleep();

    Role role = edu.addChildRole("testGroup", "testGroup");
    Group group = GrouperDAOFactory.getFactory().getGroup().findByUuid(role.getId(), true);
    Member member0 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ0, true);
    Member member1 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ1, true);
    group.addMember(member1.getSubject(), true);
    
    AttributeDef attributeDef = edu.addChildAttributeDef("attributeDef", AttributeDefType.perm);
    attributeDef.setAssignToGroup(true);
    attributeDef.store();
    AttributeDefName attributeDefName1 = edu.addChildAttributeDefName(attributeDef, "testAttribute1", "testAttribute1");
    AttributeAssignAction action1 = attributeDef.getAttributeDefActionDelegate().addAction("testAction1");
    AttributeAssign attributeAssign1 = role.getPermissionRoleDelegate().assignRolePermission("testAction1", attributeDefName1, PermissionAllowed.ALLOWED).getAttributeAssign();

    // populate PIT tables
    ChangeLogTempToEntity.convertRecords();
    
    Timestamp after = getTimestampWithSleep();

    Set<PermissionEntry> perms = new PermissionFinder()
      .addPermissionDefId(attributeDef.getId())
      .addPermissionNameId(attributeDefName1.getId())
      .addRoleId(role.getId())
      .addMemberId(member1.getUuid())
      .addAction(action1.getName())
      .assignEnabled(true)
      .assignPointInTimeFrom(before)
      .assignPointInTimeTo(after)
      .findPermissions();
    assertEquals(1, perms.size());
    
    GrouperSession s = GrouperSession.start(member0.getSubject());
    perms = new PermissionFinder()
      .addPermissionDefId(attributeDef.getId())
      .addPermissionNameId(attributeDefName1.getId())
      .addRoleId(role.getId())
      .addMemberId(member1.getUuid())
      .addAction(action1.getName())
      .assignEnabled(true)
      .assignPointInTimeFrom(before)
      .assignPointInTimeTo(after)
      .findPermissions();
    assertEquals(0, perms.size());
    s.stop();
    
    s = GrouperSession.startRootSession();
    group.grantPriv(member0.getSubject(), AccessPrivilege.READ);
    s = GrouperSession.start(member0.getSubject());
    perms = new PermissionFinder()
      .addPermissionDefId(attributeDef.getId())
      .addPermissionNameId(attributeDefName1.getId())
      .addRoleId(role.getId())
      .addMemberId(member1.getUuid())
      .addAction(action1.getName())
      .assignEnabled(true)
      .assignPointInTimeFrom(before)
      .assignPointInTimeTo(after)
      .findPermissions();
    assertEquals(0, perms.size());
    s.stop();
    
    s = GrouperSession.startRootSession();
    group.revokePriv(member0.getSubject(), AccessPrivilege.READ);
    attributeDef.getPrivilegeDelegate().grantPriv(member0.getSubject(), AttributeDefPrivilege.ATTR_READ, true);
    s = GrouperSession.start(member0.getSubject());
    perms = new PermissionFinder()
      .addPermissionDefId(attributeDef.getId())
      .addPermissionNameId(attributeDefName1.getId())
      .addRoleId(role.getId())
      .addMemberId(member1.getUuid())
      .addAction(action1.getName())
      .assignEnabled(true)
      .assignPointInTimeFrom(before)
      .assignPointInTimeTo(after)
      .findPermissions();
    assertEquals(0, perms.size());
    s.stop();
    
    s = GrouperSession.startRootSession();
    group.grantPriv(member0.getSubject(), AccessPrivilege.GROUP_ATTR_READ);
    s = GrouperSession.start(member0.getSubject());
    perms = new PermissionFinder()
      .addPermissionDefId(attributeDef.getId())
      .addPermissionNameId(attributeDefName1.getId())
      .addRoleId(role.getId())
      .addMemberId(member1.getUuid())
      .addAction(action1.getName())
      .assignEnabled(true)
      .assignPointInTimeFrom(before)
      .assignPointInTimeTo(after)
      .findPermissions();
    assertEquals(1, perms.size());
    s.stop();
    
    s = GrouperSession.startRootSession();
    attributeAssign1.delete();
    ChangeLogTempToEntity.convertRecords();
    s = GrouperSession.start(member0.getSubject());
    perms = new PermissionFinder()
      .addPermissionDefId(attributeDef.getId())
      .addPermissionNameId(attributeDefName1.getId())
      .addRoleId(role.getId())
      .addMemberId(member1.getUuid())
      .addAction(action1.getName())
      .assignEnabled(true)
      .assignPointInTimeFrom(before)
      .assignPointInTimeTo(after)
      .findPermissions();
    assertEquals(0, perms.size());
    s.stop();
    
    s = GrouperSession.startRootSession();
    perms = new PermissionFinder()
      .addPermissionDefId(attributeDef.getId())
      .addPermissionNameId(attributeDefName1.getId())
      .addRoleId(role.getId())
      .addMemberId(member1.getUuid())
      .addAction(action1.getName())
      .assignEnabled(true)
      .assignPointInTimeFrom(before)
      .assignPointInTimeTo(after)
      .findPermissions();
    assertEquals(1, perms.size());
  }
  
  /**
   * 
   */
  public void testFindPermissionsWithDisallowAndRoleInheritance() {
    String readString = "read";
    new StemSave(this.grouperSession).assignName("top").assignDisplayExtension("top display name").save();
    Role adminRole = new GroupSave(this.grouperSession).assignName("top:admin").assignTypeOfGroup(TypeOfGroup.role).save();
    Role seniorAdmin = new GroupSave(this.grouperSession).assignName("top:seniorAdmin").assignTypeOfGroup(TypeOfGroup.role).save();
    
    AttributeDef permissionDef = new AttributeDefSave(this.grouperSession).assignName("top:permissionDef")
      .assignAttributeDefType(AttributeDefType.perm).assignToEffMembership(true).assignToGroup(true).save();
    permissionDef.getAttributeDefActionDelegate().configureActionList("read");

    AttributeDefName artsAndSciences = new AttributeDefNameSave(this.grouperSession, permissionDef).assignName("top:artsAndSciences").assignDisplayExtension("Arts and Sciences").save();
    AttributeDefName all = new AttributeDefNameSave(this.grouperSession, permissionDef).assignName("top:all").assignDisplayExtension("All").save();

    all.getAttributeDefNameSetDelegate().addToAttributeDefNameSet(artsAndSciences);
    
    //senior admin inherits from admin
    seniorAdmin.getRoleInheritanceDelegate().addRoleToInheritFromThis(adminRole);

    //Role<Admin> denies: Action<Read> of Resource<Arts and sciences>
    adminRole.getPermissionRoleDelegate().assignRolePermission(readString, artsAndSciences, PermissionAllowed.DISALLOWED);

    //Role<Senior admin> allows: Action<Read> of Resource<All>
    seniorAdmin.getPermissionRoleDelegate().assignRolePermission(readString, all, PermissionAllowed.ALLOWED);
    ChangeLogTempToEntity.convertRecords();
    
    //User subj0 is assigned Role<Senior admin>
    seniorAdmin.addMember(SubjectTestHelper.SUBJ0, true);
    
    //User subj1 is assigned to Role<Admin>
    adminRole.addMember(SubjectTestHelper.SUBJ1, true);
    ChangeLogTempToEntity.convertRecords();

    Timestamp after = getTimestampWithSleep();

    // now delete everything...
    permissionDef.delete();
    seniorAdmin.getRoleInheritanceDelegate().removeRoleFromInheritFromThis(adminRole);
    adminRole.delete();
    seniorAdmin.delete();
    ChangeLogTempToEntity.convertRecords();
    
    //
    //Result:
    //
    //Overall, subj0 is allowed Action<Read> of Resource<Arts and sciences> since the subject is assigned 
    //directly to Senior admin, it will trump inherited role assignments
    
    //lets get all of the permission assignments
    List<PermissionEntry> permissionEntries = new ArrayList<PermissionEntry>(new PermissionFinder().addSubject(SubjectTestHelper.SUBJ0)
        .addAction(readString)
        .addPermissionName(artsAndSciences)
        .assignEnabled(true)
        .assignPointInTimeFrom(after)
        .assignPointInTimeTo(after)
        .findPermissions());
        
    //there should be two, one should be allow, the other deny
    assertEquals(2, GrouperUtil.length(permissionEntries));
    boolean isDisallowed1 = permissionEntries.get(0).isDisallowed();
    boolean isDisallowed2 = permissionEntries.get(1).isDisallowed();
    assertTrue(isDisallowed1 != isDisallowed2);
    
    //if we filter permissions, should be one
    permissionEntries = new ArrayList<PermissionEntry>(new PermissionFinder().addSubject(SubjectTestHelper.SUBJ0)
        .addAction(readString)
        .assignPermissionProcessor(PermissionProcessor.FILTER_REDUNDANT_PERMISSIONS)
        .addPermissionName(artsAndSciences)
        .assignEnabled(true)
        .assignPointInTimeFrom(after)
        .assignPointInTimeTo(after)
        .findPermissions());
        
    //there should be one, one should be allow
    assertEquals(1, GrouperUtil.length(permissionEntries));

    assertTrue(!permissionEntries.get(0).isDisallowed());

    //if we filter by role, it should find the allow
    permissionEntries = new ArrayList<PermissionEntry>(new PermissionFinder().addSubject(SubjectTestHelper.SUBJ0)
        .addAction(readString)
        .assignPermissionProcessor(PermissionProcessor.FILTER_REDUNDANT_PERMISSIONS_AND_ROLES)
        .addPermissionName(artsAndSciences)
        .assignEnabled(true)
        .assignPointInTimeFrom(after)
        .assignPointInTimeTo(after)
        .findPermissions());
        
    //there should be one, one should be allow
    assertEquals(1, GrouperUtil.length(permissionEntries));
    
    assertTrue(!permissionEntries.get(0).isDisallowed());
    
    // now make sure hasPermission() works...
    boolean result = new PermissionFinder().addSubject(SubjectTestHelper.SUBJ0)
      .addAction(readString)
      .addPermissionName(artsAndSciences)
      .assignEnabled(true)
      .assignPointInTimeFrom(after)
      .assignPointInTimeTo(after)
      .hasPermission();
    assertTrue(result);
    
    result = new PermissionFinder().addSubject(SubjectTestHelper.SUBJ0)
      .addAction(readString)
      .assignPermissionProcessor(PermissionProcessor.FILTER_REDUNDANT_PERMISSIONS_AND_ROLES)
      .addPermissionName(artsAndSciences)
      .assignEnabled(true)
      .assignPointInTimeFrom(after)
      .assignPointInTimeTo(after)
      .hasPermission();
    assertTrue(result);
    
    result = new PermissionFinder().addSubject(SubjectTestHelper.SUBJ1)
      .addAction(readString)
      .addPermissionName(artsAndSciences)
      .assignEnabled(true)
      .assignPointInTimeFrom(after)
      .assignPointInTimeTo(after)
      .hasPermission();
    assertFalse(result);
  }
  
  /**
   * 
   */
  public void testRolePermissionObject() {
    Role role = edu.addChildRole("testGroup", "testGroup");
    Group group = GroupFinder.findByUuid(grouperSession, role.getId(), true);
    Member newMember1 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ1, true);
    role.addMember(newMember1.getSubject(), true);
    
    AttributeDef attributeDef = edu.addChildAttributeDef("attributeDef", AttributeDefType.perm);
    attributeDef.setAssignToGroup(true);
    attributeDef.store();
    AttributeDefName attributeDefName1 = edu.addChildAttributeDefName(attributeDef, "testAttribute1", "testAttribute1");
    AttributeAssignAction action1 = attributeDef.getAttributeDefActionDelegate().addAction("testAction1");

    AttributeAssign assign = role.getPermissionRoleDelegate().assignRolePermission("testAction1", attributeDefName1, PermissionAllowed.ALLOWED).getAttributeAssign();

    // populate PIT tables
    ChangeLogTempToEntity.convertRecords();
    
    PITPermissionAllView perm = (PITPermissionAllView)new PermissionFinder()
      .addPermissionDef(attributeDef)
      .assignPointInTimeFrom(new Timestamp(System.currentTimeMillis() - 100000))
      .assignEnabled(true)
      .findPermissions().iterator().next();
    
    assertEquals(group.getName(), perm.getRoleName());
    assertEquals(newMember1.getSubjectSourceId(), perm.getSubjectSourceId());
    assertEquals(newMember1.getSubjectId(), perm.getSubjectId());
    assertEquals(action1.getName(), perm.getAction());
    assertEquals(attributeDefName1.getName(), perm.getAttributeDefNameName());
    assertEquals(GrouperDAOFactory.getFactory().getPITGroup().findBySourceIdActive(group.getId(), true).getId(), perm.getRoleId());
    assertEquals(GrouperDAOFactory.getFactory().getPITAttributeDef().findBySourceIdActive(attributeDef.getId(), true).getId(), perm.getAttributeDefId());
    assertEquals(GrouperDAOFactory.getFactory().getPITMember().findBySourceIdActive(newMember1.getId(), true).getId(), perm.getMemberId());
    assertEquals(GrouperDAOFactory.getFactory().getPITAttributeDefName().findBySourceIdActive(attributeDefName1.getId(), true).getId(), perm.getAttributeDefNameId());
    assertEquals(GrouperDAOFactory.getFactory().getPITAttributeAssignAction().findBySourceIdActive(action1.getId(), true).getId(), perm.getActionId());
    assertEquals(0, perm.getMembershipDepth());
    assertEquals(0, perm.getRoleSetDepth());
    assertEquals(0, perm.getAttributeDefNameSetDepth());
    assertEquals(0, perm.getAttributeAssignActionSetDepth());
    assertEquals(GrouperDAOFactory.getFactory().getPITAttributeAssign().findBySourceIdActive(assign.getId(), true).getId(), perm.getAttributeAssignId());
    assertEquals(PermissionType.role, perm.getPermissionType());
    assertEquals("T", perm.getGroupSetActiveDb());
    assertNotNull(perm.getGroupSetStartTimeDb());
    assertNull(perm.getGroupSetEndTimeDb());
    assertEquals("T", perm.getMembershipActiveDb());
    assertNotNull(perm.getMembershipStartTimeDb());
    assertNull(perm.getMembershipEndTimeDb());
    assertEquals("T", perm.getRoleSetActiveDb());
    assertNotNull(perm.getRoleSetStartTimeDb());
    assertNull(perm.getRoleSetEndTimeDb());
    assertEquals("T", perm.getActionSetActiveDb());
    assertNotNull(perm.getActionSetStartTimeDb());
    assertNull(perm.getActionSetEndTimeDb());
    assertEquals("T", perm.getAttributeDefNameSetActiveDb());
    assertNotNull(perm.getAttributeDefNameSetStartTimeDb());
    assertNull(perm.getAttributeDefNameSetEndTimeDb());
    assertEquals("T", perm.getAttributeAssignActiveDb());
    assertNotNull(perm.getAttributeAssignStartTimeDb());
    assertNull(perm.getAttributeAssignEndTimeDb());
    assertEquals("F", perm.getDisallowedDb());
    assertEquals(action1.getId(), perm.getActionSourceId());
    assertEquals(group.getId(), perm.getRoleSourceId());
    assertEquals(attributeDefName1.getId(), perm.getAttributeDefNameSourceId());
    assertEquals(attributeDef.getId(), perm.getAttributeDefSourceId());
    assertEquals(newMember1.getId(), perm.getMemberSourceId());
    assertEquals(MembershipFinder.findImmediateMembership(grouperSession, group, SubjectTestHelper.SUBJ1, Group.getDefaultList(), true).getImmediateMembershipId(), perm.getMembershipSourceId());
    assertEquals(assign.getId(), perm.getAttributeAssignSourceId());
    
    PermissionEntryImpl perm2 = (PermissionEntryImpl)new PermissionFinder()
      .addPermissionDef(attributeDef)
      .assignEnabled(true)
      .findPermissions().iterator().next();
    
    assertEquals(role.getName(), perm2.getRoleName());
    assertEquals(newMember1.getSubjectSourceId(), perm2.getSubjectSourceId());
    assertEquals(newMember1.getSubjectId(), perm2.getSubjectId());
    assertEquals(action1.getName(), perm2.getAction());
    assertEquals(attributeDefName1.getName(), perm2.getAttributeDefNameName());
    assertEquals(attributeDefName1.getDisplayName(), perm2.getAttributeDefNameDispName());
    assertEquals(role.getDisplayName(), perm2.getRoleDisplayName());
    assertEquals("FALSE", perm2.getAttributeAssignDelegatableDb());
    assertEquals("T", perm2.getEnabledDb());
    assertNull(perm2.getEnabledTimeDb());
    assertNull(perm2.getDisabledTimeDb());
    assertEquals(role.getId(), perm2.getRoleId());
    assertEquals(attributeDef.getId(), perm2.getAttributeDefId());
    assertEquals(newMember1.getId(), perm2.getMemberId());
    assertEquals(attributeDefName1.getId(), perm2.getAttributeDefNameId());
    assertEquals(action1.getId(), perm2.getActionId());
    assertEquals(0, perm2.getMembershipDepth());
    assertEquals(0, perm2.getRoleSetDepth());
    assertEquals(0, perm2.getAttributeDefNameSetDepth());
    assertEquals(0, perm2.getAttributeAssignActionSetDepth());
    assertEquals(MembershipFinder.findImmediateMembership(grouperSession, group, SubjectTestHelper.SUBJ1, Group.getDefaultList(), true).getUuid(), perm2.getMembershipId());
    assertEquals(assign.getId(), perm2.getAttributeAssignId());
    assertEquals(PermissionType.role, perm2.getPermissionType());
    assertNull(perm2.getAssignmentNotes());
    assertNull(perm2.getImmediateMshipEnabledTimeDb());
    assertNull(perm2.getImmediateMshipDisabledTimeDb());
    assertEquals("F", perm2.getDisallowedDb());
  }
  
  /**
   * 
   */
  public void testSubjectRolePermissionObject() {
    Role role = edu.addChildRole("testGroup", "testGroup");
    Group group = GroupFinder.findByUuid(grouperSession, role.getId(), true);
    Member newMember1 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ1, true);
    role.addMember(newMember1.getSubject(), true);
    
    AttributeDef attributeDef = edu.addChildAttributeDef("attributeDef", AttributeDefType.perm);
    attributeDef.setAssignToEffMembership(true);
    attributeDef.store();
    AttributeDefName attributeDefName1 = edu.addChildAttributeDefName(attributeDef, "testAttribute1", "testAttribute1");
    AttributeAssignAction action1 = attributeDef.getAttributeDefActionDelegate().addAction("testAction1");

    AttributeAssign assign = role.getPermissionRoleDelegate().assignSubjectRolePermission("testAction1", attributeDefName1, newMember1, PermissionAllowed.ALLOWED).getAttributeAssign();

    // populate PIT tables
    ChangeLogTempToEntity.convertRecords();
    
    PITPermissionAllView perm = (PITPermissionAllView)new PermissionFinder()
      .addPermissionDef(attributeDef)
      .assignPointInTimeFrom(new Timestamp(System.currentTimeMillis() - 100000))
      .assignEnabled(true)
      .findPermissions().iterator().next();
    
    assertEquals(group.getName(), perm.getRoleName());
    assertEquals(newMember1.getSubjectSourceId(), perm.getSubjectSourceId());
    assertEquals(newMember1.getSubjectId(), perm.getSubjectId());
    assertEquals(action1.getName(), perm.getAction());
    assertEquals(attributeDefName1.getName(), perm.getAttributeDefNameName());
    assertEquals(GrouperDAOFactory.getFactory().getPITGroup().findBySourceIdActive(group.getId(), true).getId(), perm.getRoleId());
    assertEquals(GrouperDAOFactory.getFactory().getPITAttributeDef().findBySourceIdActive(attributeDef.getId(), true).getId(), perm.getAttributeDefId());
    assertEquals(GrouperDAOFactory.getFactory().getPITMember().findBySourceIdActive(newMember1.getId(), true).getId(), perm.getMemberId());
    assertEquals(GrouperDAOFactory.getFactory().getPITAttributeDefName().findBySourceIdActive(attributeDefName1.getId(), true).getId(), perm.getAttributeDefNameId());
    assertEquals(GrouperDAOFactory.getFactory().getPITAttributeAssignAction().findBySourceIdActive(action1.getId(), true).getId(), perm.getActionId());
    assertEquals(0, perm.getMembershipDepth());
    assertEquals(-1, perm.getRoleSetDepth());
    assertEquals(0, perm.getAttributeDefNameSetDepth());
    assertEquals(0, perm.getAttributeAssignActionSetDepth());
    assertEquals(GrouperDAOFactory.getFactory().getPITAttributeAssign().findBySourceIdActive(assign.getId(), true).getId(), perm.getAttributeAssignId());
    assertEquals(PermissionType.role_subject, perm.getPermissionType());
    assertEquals("T", perm.getGroupSetActiveDb());
    assertNotNull(perm.getGroupSetStartTimeDb());
    assertNull(perm.getGroupSetEndTimeDb());
    assertEquals("T", perm.getMembershipActiveDb());
    assertNotNull(perm.getMembershipStartTimeDb());
    assertNull(perm.getMembershipEndTimeDb());
    assertEquals("T", perm.getRoleSetActiveDb());
    assertNotNull(perm.getRoleSetStartTimeDb());
    assertNull(perm.getRoleSetEndTimeDb());
    assertEquals("T", perm.getActionSetActiveDb());
    assertNotNull(perm.getActionSetStartTimeDb());
    assertNull(perm.getActionSetEndTimeDb());
    assertEquals("T", perm.getAttributeDefNameSetActiveDb());
    assertNotNull(perm.getAttributeDefNameSetStartTimeDb());
    assertNull(perm.getAttributeDefNameSetEndTimeDb());
    assertEquals("T", perm.getAttributeAssignActiveDb());
    assertNotNull(perm.getAttributeAssignStartTimeDb());
    assertNull(perm.getAttributeAssignEndTimeDb());
    assertEquals("F", perm.getDisallowedDb());
    assertEquals(action1.getId(), perm.getActionSourceId());
    assertEquals(group.getId(), perm.getRoleSourceId());
    assertEquals(attributeDefName1.getId(), perm.getAttributeDefNameSourceId());
    assertEquals(attributeDef.getId(), perm.getAttributeDefSourceId());
    assertEquals(newMember1.getId(), perm.getMemberSourceId());
    assertEquals(MembershipFinder.findImmediateMembership(grouperSession, group, SubjectTestHelper.SUBJ1, Group.getDefaultList(), true).getImmediateMembershipId(), perm.getMembershipSourceId());
    assertEquals(assign.getId(), perm.getAttributeAssignSourceId());
    
    PermissionEntryImpl perm2 = (PermissionEntryImpl)new PermissionFinder()
      .addPermissionDef(attributeDef)
      .assignEnabled(true)
      .findPermissions().iterator().next();
    
    assertEquals(role.getName(), perm2.getRoleName());
    assertEquals(newMember1.getSubjectSourceId(), perm2.getSubjectSourceId());
    assertEquals(newMember1.getSubjectId(), perm2.getSubjectId());
    assertEquals(action1.getName(), perm2.getAction());
    assertEquals(attributeDefName1.getName(), perm2.getAttributeDefNameName());
    assertEquals(attributeDefName1.getDisplayName(), perm2.getAttributeDefNameDispName());
    assertEquals(role.getDisplayName(), perm2.getRoleDisplayName());
    assertEquals("FALSE", perm2.getAttributeAssignDelegatableDb());
    assertEquals("T", perm2.getEnabledDb());
    assertNull(perm2.getEnabledTimeDb());
    assertNull(perm2.getDisabledTimeDb());
    assertEquals(role.getId(), perm2.getRoleId());
    assertEquals(attributeDef.getId(), perm2.getAttributeDefId());
    assertEquals(newMember1.getId(), perm2.getMemberId());
    assertEquals(attributeDefName1.getId(), perm2.getAttributeDefNameId());
    assertEquals(action1.getId(), perm2.getActionId());
    assertEquals(0, perm2.getMembershipDepth());
    assertEquals(-1, perm2.getRoleSetDepth());
    assertEquals(0, perm2.getAttributeDefNameSetDepth());
    assertEquals(0, perm2.getAttributeAssignActionSetDepth());
    assertEquals(MembershipFinder.findImmediateMembership(grouperSession, group, SubjectTestHelper.SUBJ1, Group.getDefaultList(), true).getUuid(), perm2.getMembershipId());
    assertEquals(assign.getId(), perm2.getAttributeAssignId());
    assertEquals(PermissionType.role_subject, perm2.getPermissionType());
    assertNull(perm2.getAssignmentNotes());
    assertNull(perm2.getImmediateMshipEnabledTimeDb());
    assertNull(perm2.getImmediateMshipDisabledTimeDb());
    assertEquals("F", perm2.getDisallowedDb());
  }
  
  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new PITPermissionTests("testSubjectRolePermissionObject"));
    //TestRunner.run(PITPermissionTests.class);
  }
}
