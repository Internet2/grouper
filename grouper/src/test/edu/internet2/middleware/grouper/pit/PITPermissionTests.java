package edu.internet2.middleware.grouper.pit;

import java.sql.Timestamp;
import java.util.Date;
import java.util.Set;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Stem;
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
import edu.internet2.middleware.grouper.permissions.role.Role;
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
    group.getPermissionRoleDelegate().assignRolePermission("testAction1", attributeDefName1).getAttributeAssign();
    Date afterAddTime = getDateWithSleep();

    // populate PIT tables
    ChangeLogTempToEntity.convertRecords();
    
    Set<PITPermissionAllView> results = new PITPermissionAllViewQuery()
      .setAttributeDefNameId(attributeDefName1.getId())
      .setMemberId(newMember1.getUuid())
      .setActionId(action1.getId())
      .setStartDateAfter(beforeAddTime)
      .execute();
    assertEquals(1, results.size());
    
    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameId(attributeDefName1.getId())
      .setMemberId(newMember1.getUuid())
      .setActionId(action1.getId())
      .setStartDateAfter(afterAddTime)
      .execute();
    assertEquals(0, results.size());    

    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameId(attributeDefName1.getId())
      .setMemberId(newMember1.getUuid())
      .setActionId(action1.getId())
      .setStartDateBefore(beforeAddTime)
      .execute();
    assertEquals(0, results.size());

    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameId(attributeDefName1.getId())
      .setMemberId(newMember1.getUuid())
      .setActionId(action1.getId())
      .setStartDateBefore(afterAddTime)
      .execute();
    assertEquals(1, results.size());

    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameId(attributeDefName1.getId())
      .setMemberId(newMember1.getUuid())
      .setActionId(action1.getId())
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

    AttributeAssign attributeAssign = group.getPermissionRoleDelegate().assignRolePermission("testAction1", attributeDefName1).getAttributeAssign();
    Date afterAddTime = getDateWithSleep();
    
    // populate PIT tables
    ChangeLogTempToEntity.convertRecords();
    
    Set<PITPermissionAllView> results = new PITPermissionAllViewQuery()
      .setAttributeDefNameId(attributeDefName1.getId())
      .setMemberId(newMember1.getUuid())
      .setActionId(action1.getId())
      .setEndDateAfter(afterAddTime)
      .execute();
    assertEquals(1, results.size());
    
    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameId(attributeDefName1.getId())
      .setMemberId(newMember1.getUuid())
      .setActionId(action1.getId())
      .setEndDateBefore(afterAddTime)
      .execute();
    assertEquals(0, results.size());
    
    Date beforeDeleteTime = getDateWithSleep();
    attributeAssign.delete();
    Date afterDeleteTime = getDateWithSleep();

    // populate PIT tables
    ChangeLogTempToEntity.convertRecords();
    
    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameId(attributeDefName1.getId())
      .setMemberId(newMember1.getUuid())
      .setActionId(action1.getId())
      .setEndDateAfter(afterDeleteTime)
      .execute();
    assertEquals(0, results.size());
    
    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameId(attributeDefName1.getId())
      .setMemberId(newMember1.getUuid())
      .setActionId(action1.getId())
      .setEndDateAfter(beforeDeleteTime)
      .execute();
    assertEquals(1, results.size());    

    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameId(attributeDefName1.getId())
      .setMemberId(newMember1.getUuid())
      .setActionId(action1.getId())
      .setEndDateBefore(afterDeleteTime)
      .execute();
    assertEquals(1, results.size());

    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameId(attributeDefName1.getId())
      .setMemberId(newMember1.getUuid())
      .setActionId(action1.getId())
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
    
    AttributeAssign attributeAssign = group.getPermissionRoleDelegate().assignRolePermission("testAction1", attributeDefName1).getAttributeAssign();
    
    Date afterAddTime = getDateWithSleep();
    Date beforeDisableTime = getDateWithSleep();
    
    attributeAssign.setEnabled(false);
    attributeAssign.setEnabledTime(new Timestamp(new Date().getTime() + 100000));
    attributeAssign.saveOrUpdate();
    
    Date afterDisableTime = getDateWithSleep();
    Date beforeEnableTime = getDateWithSleep();

    attributeAssign.setEnabled(true);
    attributeAssign.setEnabledTime(new Timestamp(new Date().getTime() - 100000));
    attributeAssign.saveOrUpdate();
    
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
      .setAttributeDefNameId(attributeDefName1.getId())
      .setMemberId(newMember1.getUuid())
      .setActionId(action1.getId())
      .setActiveDateRange(new Date(beforeAddTime.getTime() - 1), beforeAddTime)
      .execute();
    assertEquals(0, results.size());
    
    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameId(attributeDefName1.getId())
      .setMemberId(newMember1.getUuid())
      .setActionId(action1.getId())
      .setActiveDateRange(null, beforeAddTime)
      .execute();
    assertEquals(0, results.size());
    
    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameId(attributeDefName1.getId())
      .setMemberId(newMember1.getUuid())
      .setActionId(action1.getId())
      .setActiveDateRange(beforeAddTime, afterAddTime)
      .execute();
    assertEquals(1, results.size());
    
    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameId(attributeDefName1.getId())
      .setMemberId(newMember1.getUuid())
      .setActionId(action1.getId())
      .setActiveDateRange(afterAddTime, beforeDisableTime)
      .execute();
    assertEquals(1, results.size());
    
    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameId(attributeDefName1.getId())
      .setMemberId(newMember1.getUuid())
      .setActionId(action1.getId())
      .setActiveDateRange(beforeDisableTime, afterDisableTime)
      .execute();
    assertEquals(1, results.size());
    
    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameId(attributeDefName1.getId())
      .setMemberId(newMember1.getUuid())
      .setActionId(action1.getId())
      .setActiveDateRange(afterDisableTime, beforeEnableTime)
      .execute();
    assertEquals(0, results.size());    
    
    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameId(attributeDefName1.getId())
      .setMemberId(newMember1.getUuid())
      .setActionId(action1.getId())
      .setActiveDateRange(beforeEnableTime, afterEnableTime)
      .execute();
    assertEquals(1, results.size());
    
    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameId(attributeDefName1.getId())
      .setMemberId(newMember1.getUuid())
      .setActionId(action1.getId())
      .setActiveDateRange(afterEnableTime, null)
      .execute();
    assertEquals(1, results.size());
    
    // test ids that weren't used at all
    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameId(attributeDefName2.getId())
      .setMemberId(newMember1.getUuid())
      .setActionId(action1.getId())
      .setActiveDateRange(afterEnableTime, null)
      .execute();
    assertEquals(0, results.size());
    
    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameId(attributeDefName1.getId())
      .setMemberId(newMember2.getUuid())
      .setActionId(action1.getId())
      .setActiveDateRange(afterEnableTime, null)
      .execute();
    assertEquals(0, results.size());
    
    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameId(attributeDefName1.getId())
      .setMemberId(newMember1.getUuid())
      .setActionId(action2.getId())
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
    
    group.getPermissionRoleDelegate().assignRolePermission("testAction1", attributeDefName1).getAttributeAssign();
    
    Date afterAddTime = getDateWithSleep();
    
    // populate PIT tables
    ChangeLogTempToEntity.convertRecords();

    Set<PITPermissionAllView> results = new PITPermissionAllViewQuery()
      .setAttributeDefNameId(attributeDefName1.getId())
      .setMemberId(newMember1.getUuid())
      .setActionId(action1.getId())
      .setActiveDateRange(new Date(beforeAddTime.getTime() - 1), beforeAddTime)
      .execute();
    assertEquals(0, results.size());
    
    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameId(attributeDefName1.getId())
      .setMemberId(newMember1.getUuid())
      .setActionId(action1.getId())
      .setActiveDateRange(null, beforeAddTime)
      .execute();
    assertEquals(0, results.size());
    
    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameId(attributeDefName1.getId())
      .setMemberId(newMember1.getUuid())
      .setActionId(action1.getId())
      .setActiveDateRange(beforeAddTime, afterAddTime)
      .execute();
    assertEquals(1, results.size());
    
    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameId(attributeDefName1.getId())
      .setMemberId(newMember1.getUuid())
      .setActionId(action1.getId())
      .setActiveDateRange(afterAddTime, null)
      .execute();
    assertEquals(1, results.size());
    
    // test ids that weren't used at all
    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameId(attributeDefName2.getId())
      .setMemberId(newMember1.getUuid())
      .setActionId(action1.getId())
      .setActiveDateRange(afterAddTime, null)
      .execute();
    assertEquals(0, results.size());

    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameId(attributeDefName1.getId())
      .setMemberId(newMember2.getUuid())
      .setActionId(action1.getId())
      .setActiveDateRange(afterAddTime, null)
      .execute();
    assertEquals(0, results.size());

    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameId(attributeDefName1.getId())
      .setMemberId(newMember1.getUuid())
      .setActionId(action2.getId())
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
    
    AttributeAssign attributeAssign = group.getPermissionRoleDelegate().assignRolePermission("testAction1", attributeDefName1).getAttributeAssign();
    
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
      .setAttributeDefNameId(attributeDefName1.getId())
      .setMemberId(newMember1.getUuid())
      .setActionId(action1.getId())
      .setActiveDateRange(new Date(beforeAddTime.getTime() - 1), beforeAddTime)
      .execute();
    assertEquals(0, results.size());
    
    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameId(attributeDefName1.getId())
      .setMemberId(newMember1.getUuid())
      .setActionId(action1.getId())
      .setActiveDateRange(null, beforeAddTime)
      .execute();
    assertEquals(0, results.size());
    
    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameId(attributeDefName1.getId())
      .setMemberId(newMember1.getUuid())
      .setActionId(action1.getId())
      .setActiveDateRange(beforeAddTime, afterAddTime)
      .execute();
    assertEquals(1, results.size());
    
    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameId(attributeDefName1.getId())
      .setMemberId(newMember1.getUuid())
      .setActionId(action1.getId())
      .setActiveDateRange(afterAddTime, beforeRemoveTime)
      .execute();
    assertEquals(1, results.size());
    
    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameId(attributeDefName1.getId())
      .setMemberId(newMember1.getUuid())
      .setActionId(action1.getId())
      .setActiveDateRange(beforeRemoveTime, afterRemoveTime)
      .execute();
    assertEquals(1, results.size());
    
    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameId(attributeDefName1.getId())
      .setMemberId(newMember1.getUuid())
      .setActionId(action1.getId())
      .setActiveDateRange(afterRemoveTime, null)
      .execute();
    assertEquals(0, results.size());
    
    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameId(attributeDefName1.getId())
      .setMemberId(newMember1.getUuid())
      .setActionId(action1.getId())
      .setActiveDateRange(beforeRemoveTime, null)
      .execute();
    assertEquals(1, results.size());
    
    // test ids that weren't used at all
    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameId(attributeDefName2.getId())
      .setMemberId(newMember1.getUuid())
      .setActionId(action1.getId())
      .setActiveDateRange(beforeRemoveTime, null)
      .execute();
    assertEquals(0, results.size());
    
    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameId(attributeDefName1.getId())
      .setMemberId(newMember2.getUuid())
      .setActionId(action1.getId())
      .setActiveDateRange(beforeRemoveTime, null)
      .execute();
    assertEquals(0, results.size());
    
    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameId(attributeDefName1.getId())
      .setMemberId(newMember1.getUuid())
      .setActionId(action2.getId())
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
    
    group.getPermissionRoleDelegate().assignSubjectRolePermission("testAction1", attributeDefName1, newMember1).getAttributeAssign();
    
    Date afterAddTime = getDateWithSleep();
    
    // populate PIT tables
    ChangeLogTempToEntity.convertRecords();

    Set<PITPermissionAllView> results = new PITPermissionAllViewQuery()
      .setAttributeDefNameId(attributeDefName1.getId())
      .setMemberId(newMember1.getUuid())
      .setActionId(action1.getId())
      .setActiveDateRange(new Date(beforeAddTime.getTime() - 1), beforeAddTime)
      .execute();
    assertEquals(0, results.size());
    
    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameId(attributeDefName1.getId())
      .setMemberId(newMember1.getUuid())
      .setActionId(action1.getId())
      .setActiveDateRange(null, beforeAddTime)
      .execute();
    assertEquals(0, results.size());
    
    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameId(attributeDefName1.getId())
      .setMemberId(newMember1.getUuid())
      .setActionId(action1.getId())
      .setActiveDateRange(beforeAddTime, afterAddTime)
      .execute();
    assertEquals(1, results.size());
    
    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameId(attributeDefName1.getId())
      .setMemberId(newMember1.getUuid())
      .setActionId(action1.getId())
      .setActiveDateRange(afterAddTime, null)
      .execute();
    assertEquals(1, results.size());
    
    // test ids that weren't used at all
    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameId(attributeDefName2.getId())
      .setMemberId(newMember1.getUuid())
      .setActionId(action1.getId())
      .setActiveDateRange(afterAddTime, null)
      .execute();
    assertEquals(0, results.size());

    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameId(attributeDefName1.getId())
      .setMemberId(newMember2.getUuid())
      .setActionId(action1.getId())
      .setActiveDateRange(afterAddTime, null)
      .execute();
    assertEquals(0, results.size());

    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameId(attributeDefName1.getId())
      .setMemberId(newMember1.getUuid())
      .setActionId(action2.getId())
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
    AttributeAssign attributeAssign = group.getPermissionRoleDelegate().assignRolePermission("testAction1", attributeDefName1).getAttributeAssign();

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
      .setAttributeDefNameId(attributeDefName1.getId())
      .setMemberId(newMember1.getUuid())
      .setActionId(action1.getId())
      .setActiveDateRange(new Date(beforeAddTime.getTime() - 1), beforeAddTime)
      .execute();
    assertEquals(0, results.size());
    
    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameId(attributeDefName1.getId())
      .setMemberId(newMember1.getUuid())
      .setActionId(action1.getId())
      .setActiveDateRange(null, beforeAddTime)
      .execute();
    assertEquals(0, results.size());
    
    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameId(attributeDefName1.getId())
      .setMemberId(newMember1.getUuid())
      .setActionId(action1.getId())
      .setActiveDateRange(beforeAddTime, afterAddTime)
      .execute();
    assertEquals(1, results.size());
    
    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameId(attributeDefName1.getId())
      .setMemberId(newMember1.getUuid())
      .setActionId(action1.getId())
      .setActiveDateRange(afterAddTime, beforeRemoveTime)
      .execute();
    assertEquals(1, results.size());
    
    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameId(attributeDefName1.getId())
      .setMemberId(newMember1.getUuid())
      .setActionId(action1.getId())
      .setActiveDateRange(beforeRemoveTime, afterRemoveTime)
      .execute();
    assertEquals(1, results.size());
    
    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameId(attributeDefName1.getId())
      .setMemberId(newMember1.getUuid())
      .setActionId(action1.getId())
      .setActiveDateRange(afterRemoveTime, null)
      .execute();
    assertEquals(0, results.size());
    
    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameId(attributeDefName1.getId())
      .setMemberId(newMember1.getUuid())
      .setActionId(action1.getId())
      .setActiveDateRange(beforeRemoveTime, null)
      .execute();
    assertEquals(1, results.size());
    
    // test ids that weren't used at all
    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameId(attributeDefName2.getId())
      .setMemberId(newMember1.getUuid())
      .setActionId(action1.getId())
      .setActiveDateRange(beforeRemoveTime, null)
      .execute();
    assertEquals(0, results.size());
    
    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameId(attributeDefName1.getId())
      .setMemberId(newMember2.getUuid())
      .setActionId(action1.getId())
      .setActiveDateRange(beforeRemoveTime, null)
      .execute();
    assertEquals(0, results.size());
    
    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameId(attributeDefName1.getId())
      .setMemberId(newMember1.getUuid())
      .setActionId(action2.getId())
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
    AttributeAssign attributeAssign = group.getPermissionRoleDelegate().assignRolePermission("testAction1", attributeDefName1).getAttributeAssign();

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
      .setAttributeDefNameId(attributeDefName1.getId())
      .setMemberId(newMember1.getUuid())
      .setActionId(action1.getId())
      .setActiveDateRange(new Date(beforeAddTime.getTime() - 1), beforeAddTime)
      .execute();
    assertEquals(0, results.size());
    
    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameId(attributeDefName1.getId())
      .setMemberId(newMember1.getUuid())
      .setActionId(action1.getId())
      .setActiveDateRange(null, beforeAddTime)
      .execute();
    assertEquals(0, results.size());
    
    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameId(attributeDefName1.getId())
      .setMemberId(newMember1.getUuid())
      .setActionId(action1.getId())
      .setActiveDateRange(beforeAddTime, afterAddTime)
      .execute();
    assertEquals(1, results.size());
    
    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameId(attributeDefName1.getId())
      .setMemberId(newMember1.getUuid())
      .setActionId(action1.getId())
      .setActiveDateRange(afterAddTime, beforeRemoveTime)
      .execute();
    assertEquals(1, results.size());
    
    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameId(attributeDefName1.getId())
      .setMemberId(newMember1.getUuid())
      .setActionId(action1.getId())
      .setActiveDateRange(beforeRemoveTime, afterRemoveTime)
      .execute();
    assertEquals(1, results.size());
    
    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameId(attributeDefName1.getId())
      .setMemberId(newMember1.getUuid())
      .setActionId(action1.getId())
      .setActiveDateRange(afterRemoveTime, null)
      .execute();
    assertEquals(0, results.size());
    
    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameId(attributeDefName1.getId())
      .setMemberId(newMember1.getUuid())
      .setActionId(action1.getId())
      .setActiveDateRange(beforeRemoveTime, null)
      .execute();
    assertEquals(1, results.size());
    
    // test ids that weren't used at all
    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameId(attributeDefName2.getId())
      .setMemberId(newMember1.getUuid())
      .setActionId(action1.getId())
      .setActiveDateRange(beforeRemoveTime, null)
      .execute();
    assertEquals(0, results.size());
    
    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameId(attributeDefName1.getId())
      .setMemberId(newMember2.getUuid())
      .setActionId(action1.getId())
      .setActiveDateRange(beforeRemoveTime, null)
      .execute();
    assertEquals(0, results.size());
    
    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameId(attributeDefName1.getId())
      .setMemberId(newMember1.getUuid())
      .setActionId(action2.getId())
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
    AttributeAssign attributeAssign = group.getPermissionRoleDelegate().assignRolePermission("testAction1", attributeDefName1).getAttributeAssign();

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
      .setAttributeDefNameId(attributeDefName1.getId())
      .setMemberId(newMember1.getUuid())
      .setActionId(action3.getId())
      .setActiveDateRange(new Date(beforeAddTime.getTime() - 1), beforeAddTime)
      .execute();
    assertEquals(0, results.size());
    
    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameId(attributeDefName1.getId())
      .setMemberId(newMember1.getUuid())
      .setActionId(action3.getId())
      .setActiveDateRange(null, beforeAddTime)
      .execute();
    assertEquals(0, results.size());
    
    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameId(attributeDefName1.getId())
      .setMemberId(newMember1.getUuid())
      .setActionId(action3.getId())
      .setActiveDateRange(beforeAddTime, afterAddTime)
      .execute();
    assertEquals(1, results.size());
    
    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameId(attributeDefName1.getId())
      .setMemberId(newMember1.getUuid())
      .setActionId(action3.getId())
      .setActiveDateRange(afterAddTime, beforeRemoveTime)
      .execute();
    assertEquals(1, results.size());
    
    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameId(attributeDefName1.getId())
      .setMemberId(newMember1.getUuid())
      .setActionId(action3.getId())
      .setActiveDateRange(beforeRemoveTime, afterRemoveTime)
      .execute();
    assertEquals(1, results.size());
    
    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameId(attributeDefName1.getId())
      .setMemberId(newMember1.getUuid())
      .setActionId(action3.getId())
      .setActiveDateRange(afterRemoveTime, null)
      .execute();
    assertEquals(0, results.size());
    
    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameId(attributeDefName1.getId())
      .setMemberId(newMember1.getUuid())
      .setActionId(action3.getId())
      .setActiveDateRange(beforeRemoveTime, null)
      .execute();
    assertEquals(1, results.size());
    
    // test ids that weren't used at all
    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameId(attributeDefName2.getId())
      .setMemberId(newMember1.getUuid())
      .setActionId(action3.getId())
      .setActiveDateRange(beforeRemoveTime, null)
      .execute();
    assertEquals(0, results.size());
    
    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameId(attributeDefName1.getId())
      .setMemberId(newMember2.getUuid())
      .setActionId(action3.getId())
      .setActiveDateRange(beforeRemoveTime, null)
      .execute();
    assertEquals(0, results.size());
    
    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameId(attributeDefName1.getId())
      .setMemberId(newMember1.getUuid())
      .setActionId(action4.getId())
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
    AttributeAssign attributeAssign = group.getPermissionRoleDelegate().assignRolePermission("testAction1", attributeDefName1).getAttributeAssign();

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
      .setAttributeDefNameId(attributeDefName3.getId())
      .setMemberId(newMember1.getUuid())
      .setActionId(action1.getId())
      .setActiveDateRange(new Date(beforeAddTime.getTime() - 1), beforeAddTime)
      .execute();
    assertEquals(0, results.size());
    
    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameId(attributeDefName3.getId())
      .setMemberId(newMember1.getUuid())
      .setActionId(action1.getId())
      .setActiveDateRange(null, beforeAddTime)
      .execute();
    assertEquals(0, results.size());
    
    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameId(attributeDefName3.getId())
      .setMemberId(newMember1.getUuid())
      .setActionId(action1.getId())
      .setActiveDateRange(beforeAddTime, afterAddTime)
      .execute();
    assertEquals(1, results.size());
    
    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameId(attributeDefName3.getId())
      .setMemberId(newMember1.getUuid())
      .setActionId(action1.getId())
      .setActiveDateRange(afterAddTime, beforeRemoveTime)
      .execute();
    assertEquals(1, results.size());
    
    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameId(attributeDefName3.getId())
      .setMemberId(newMember1.getUuid())
      .setActionId(action1.getId())
      .setActiveDateRange(beforeRemoveTime, afterRemoveTime)
      .execute();
    assertEquals(1, results.size());
    
    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameId(attributeDefName3.getId())
      .setMemberId(newMember1.getUuid())
      .setActionId(action1.getId())
      .setActiveDateRange(afterRemoveTime, null)
      .execute();
    assertEquals(0, results.size());
    
    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameId(attributeDefName3.getId())
      .setMemberId(newMember1.getUuid())
      .setActionId(action1.getId())
      .setActiveDateRange(beforeRemoveTime, null)
      .execute();
    assertEquals(1, results.size());
    
    // test ids that weren't used at all
    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameId(attributeDefName4.getId())
      .setMemberId(newMember1.getUuid())
      .setActionId(action1.getId())
      .setActiveDateRange(beforeRemoveTime, null)
      .execute();
    assertEquals(0, results.size());
    
    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameId(attributeDefName3.getId())
      .setMemberId(newMember2.getUuid())
      .setActionId(action1.getId())
      .setActiveDateRange(beforeRemoveTime, null)
      .execute();
    assertEquals(0, results.size());
    
    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameId(attributeDefName3.getId())
      .setMemberId(newMember1.getUuid())
      .setActionId(action2.getId())
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
    AttributeAssign attributeAssign1 = group1.getPermissionRoleDelegate().assignRolePermission("testAction1", attributeDefName1).getAttributeAssign();
    AttributeAssign attributeAssign2 = group2.getPermissionRoleDelegate().assignRolePermission("testAction1", attributeDefName2).getAttributeAssign();
    AttributeAssign attributeAssign3 = group3.getPermissionRoleDelegate().assignRolePermission("testAction1", attributeDefName3).getAttributeAssign();

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
      .setAttributeDefNameId(attributeDefName3.getId())
      .setMemberId(newMember1.getUuid())
      .setActionId(action1.getId())
      .setActiveDateRange(new Date(beforeAddTime.getTime() - 1), beforeAddTime)
      .execute();
    assertEquals(0, results.size());
    
    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameId(attributeDefName3.getId())
      .setMemberId(newMember1.getUuid())
      .setActionId(action1.getId())
      .setActiveDateRange(null, beforeAddTime)
      .execute();
    assertEquals(0, results.size());
    
    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameId(attributeDefName3.getId())
      .setMemberId(newMember1.getUuid())
      .setActionId(action1.getId())
      .setActiveDateRange(beforeAddTime, afterAddTime)
      .execute();
    assertEquals(1, results.size());
    
    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameId(attributeDefName3.getId())
      .setMemberId(newMember1.getUuid())
      .setActionId(action1.getId())
      .setActiveDateRange(afterAddTime, beforeRemoveTime)
      .execute();
    assertEquals(1, results.size());
    
    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameId(attributeDefName3.getId())
      .setMemberId(newMember1.getUuid())
      .setActionId(action1.getId())
      .setActiveDateRange(beforeRemoveTime, afterRemoveTime)
      .execute();
    assertEquals(1, results.size());
    
    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameId(attributeDefName3.getId())
      .setMemberId(newMember1.getUuid())
      .setActionId(action1.getId())
      .setActiveDateRange(afterRemoveTime, null)
      .execute();
    assertEquals(0, results.size());
    
    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameId(attributeDefName3.getId())
      .setMemberId(newMember1.getUuid())
      .setActionId(action1.getId())
      .setActiveDateRange(beforeRemoveTime, null)
      .execute();
    assertEquals(1, results.size());
    
    // test ids that weren't used at all
    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameId(attributeDefName4.getId())
      .setMemberId(newMember1.getUuid())
      .setActionId(action1.getId())
      .setActiveDateRange(beforeRemoveTime, null)
      .execute();
    assertEquals(0, results.size());
    
    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameId(attributeDefName3.getId())
      .setMemberId(newMember2.getUuid())
      .setActionId(action1.getId())
      .setActiveDateRange(beforeRemoveTime, null)
      .execute();
    assertEquals(0, results.size());
    
    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameId(attributeDefName3.getId())
      .setMemberId(newMember1.getUuid())
      .setActionId(action2.getId())
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
    group1.getPermissionRoleDelegate().assignRolePermission("testAction1", attributeDefName1).getAttributeAssign();
    group2.getPermissionRoleDelegate().assignRolePermission("testAction1", attributeDefName2).getAttributeAssign();
    group3.getPermissionRoleDelegate().assignRolePermission("testAction1", attributeDefName3).getAttributeAssign();

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
      .setAttributeDefNameId(attributeDefName3.getId())
      .setMemberId(newMember1.getUuid())
      .setActionId(action1.getId())
      .setActiveDateRange(new Date(beforeFirstAddTime.getTime() - 1), beforeFirstAddTime)
      .execute();
    assertEquals(0, results.size());
    
    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameId(attributeDefName3.getId())
      .setMemberId(newMember1.getUuid())
      .setActionId(action1.getId())
      .setActiveDateRange(null, beforeFirstAddTime)
      .execute();
    assertEquals(0, results.size());
    
    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameId(attributeDefName3.getId())
      .setMemberId(newMember1.getUuid())
      .setActionId(action1.getId())
      .setActiveDateRange(beforeFirstAddTime, afterFirstAddTime)
      .execute();
    assertEquals(1, results.size());
    
    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameId(attributeDefName3.getId())
      .setMemberId(newMember1.getUuid())
      .setActionId(action1.getId())
      .setActiveDateRange(afterFirstAddTime, beforeSecondAddTime)
      .execute();
    assertEquals(1, results.size());
    
    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameId(attributeDefName3.getId())
      .setMemberId(newMember1.getUuid())
      .setActionId(action1.getId())
      .setActiveDateRange(beforeSecondAddTime, afterSecondAddTime)
      .execute();
    assertEquals(2, results.size());
    
    results = new PITPermissionAllViewQuery()
      .setAttributeDefNameId(attributeDefName3.getId())
      .setMemberId(newMember1.getUuid())
      .setActionId(action1.getId())
      .setActiveDateRange(afterSecondAddTime, null)
      .execute();
    assertEquals(2, results.size());
  }
}