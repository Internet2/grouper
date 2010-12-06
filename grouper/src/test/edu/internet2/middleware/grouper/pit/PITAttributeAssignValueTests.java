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
import edu.internet2.middleware.grouper.attr.AttributeDefValueType;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignAction;
import edu.internet2.middleware.grouper.attr.finder.AttributeAssignFinder;
import edu.internet2.middleware.grouper.attr.value.AttributeAssignValue;
import edu.internet2.middleware.grouper.changeLog.ChangeLogTempToEntity;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SessionHelper;
import edu.internet2.middleware.grouper.helper.StemHelper;
import edu.internet2.middleware.grouper.internal.util.GrouperUuid;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * @author shilen
 * $Id$
 */
public class PITAttributeAssignValueTests extends GrouperTest {
  
  /** top level stem */
  private Stem edu;

  /** root session */
  private GrouperSession grouperSession;
  
  /** root stem */
  private Stem root;
  
  /** amount of time to sleep between operations */
  private long sleepTime = 100;
  
  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new PITAttributeAssignValueTests("testMembershipEnableDisable"));
  }
  
  /**
   * @param name
   */
  public PITAttributeAssignValueTests(String name) {
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
    Group group = edu.addChildGroup("testGroup", "testGroup");
    
    AttributeDef attributeDef = edu.addChildAttributeDef("attributeDef", AttributeDefType.attr);
    attributeDef.setAssignToGroup(true);
    attributeDef.setValueType(AttributeDefValueType.string);
    attributeDef.store();
    AttributeDefName attributeDefName1 = edu.addChildAttributeDefName(attributeDef, "testAttribute1", "testAttribute1");
    AttributeAssignAction action1 = attributeDef.getAttributeDefActionDelegate().addAction("testAction1");
    AttributeAssign attributeAssign = group.getAttributeDelegate().assignAttribute(action1.getName(), attributeDefName1).getAttributeAssign();

    Date beforeAddTime = getDateWithSleep();
    attributeAssign.getValueDelegate().assignValueString("test");
    Date afterAddTime = getDateWithSleep();

    // populate PIT tables
    ChangeLogTempToEntity.convertRecords();
    
    Set<PITAttributeAssignValueView> results = new PITAttributeAssignValueQuery()
      .setAttributeDefNameId(attributeDefName1.getId())
      .setActionId(action1.getId())
      .setOwnerGroupId(group.getId())
      .setStartDateAfter(beforeAddTime)
      .setValueString("test")
      .execute();
    assertEquals(1, results.size());
    
    results = new PITAttributeAssignValueQuery()
      .setAttributeDefNameId(attributeDefName1.getId())
      .setOwnerGroupId(group.getId())
      .setActionId(action1.getId())
      .setStartDateAfter(afterAddTime)
      .setValueString("test")
      .execute();
    assertEquals(0, results.size());    

    results = new PITAttributeAssignValueQuery()
      .setAttributeDefNameId(attributeDefName1.getId())
      .setOwnerGroupId(group.getId())
      .setActionId(action1.getId())
      .setStartDateBefore(beforeAddTime)
      .setValueString("test")
      .execute();
    assertEquals(0, results.size());

    results = new PITAttributeAssignValueQuery()
      .setAttributeDefNameId(attributeDefName1.getId())
      .setOwnerGroupId(group.getId())
      .setActionId(action1.getId())
      .setStartDateBefore(afterAddTime)
      .setValueString("test")
      .execute();
    assertEquals(1, results.size());

    results = new PITAttributeAssignValueQuery()
      .setAttributeDefNameId(attributeDefName1.getId())
      .setOwnerGroupId(group.getId())
      .setActionId(action1.getId())
      .setValueString("test")
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
    attributeDef.setValueType(AttributeDefValueType.string);
    attributeDef.store();
    AttributeDefName attributeDefName1 = edu.addChildAttributeDefName(attributeDef, "testAttribute1", "testAttribute1");
    AttributeAssignAction action1 = attributeDef.getAttributeDefActionDelegate().addAction("testAction1");

    AttributeAssign attributeAssign = group.getAttributeDelegate().assignAttribute(action1.getName(), attributeDefName1).getAttributeAssign();
    AttributeAssignValue attributeAssignValue = attributeAssign.getValueDelegate().assignValueString("test").getAttributeAssignValue();

    Date afterAddTime = getDateWithSleep();
    
    // populate PIT tables
    ChangeLogTempToEntity.convertRecords();
    
    Set<PITAttributeAssignValueView> results = new PITAttributeAssignValueQuery()
      .setAttributeDefNameId(attributeDefName1.getId())
      .setOwnerGroupId(group.getId())
      .setActionId(action1.getId())
      .setEndDateAfter(afterAddTime)
      .setValueString("test")
      .execute();
    assertEquals(1, results.size());
    
    results = new PITAttributeAssignValueQuery()
      .setAttributeDefNameId(attributeDefName1.getId())
      .setOwnerGroupId(group.getId())
      .setActionId(action1.getId())
      .setEndDateBefore(afterAddTime)
      .setValueString("test")
      .execute();
    assertEquals(0, results.size());
    
    Date beforeDeleteTime = getDateWithSleep();
    attributeAssignValue.delete();
    Date afterDeleteTime = getDateWithSleep();

    // populate PIT tables
    ChangeLogTempToEntity.convertRecords();
    
    results = new PITAttributeAssignValueQuery()
      .setAttributeDefNameId(attributeDefName1.getId())
      .setOwnerGroupId(group.getId())
      .setActionId(action1.getId())
      .setEndDateAfter(afterDeleteTime)
      .setValueString("test")
      .execute();
    assertEquals(0, results.size());
    
    results = new PITAttributeAssignValueQuery()
      .setAttributeDefNameId(attributeDefName1.getId())
      .setOwnerGroupId(group.getId())
      .setActionId(action1.getId())
      .setEndDateAfter(beforeDeleteTime)
      .setValueString("test")
      .execute();
    assertEquals(1, results.size());    

    results = new PITAttributeAssignValueQuery()
      .setAttributeDefNameId(attributeDefName1.getId())
      .setOwnerGroupId(group.getId())
      .setActionId(action1.getId())
      .setEndDateBefore(afterDeleteTime)
      .setValueString("test")
      .execute();
    assertEquals(1, results.size());

    results = new PITAttributeAssignValueQuery()
      .setAttributeDefNameId(attributeDefName1.getId())
      .setOwnerGroupId(group.getId())
      .setActionId(action1.getId())
      .setEndDateBefore(beforeDeleteTime)
      .setValueString("test")
      .execute();
    assertEquals(0, results.size());
  }
  
  /**
   * 
   */
  public void testByValueInteger() {
    Group group = edu.addChildGroup("testGroup", "testGroup");
    
    AttributeDef attributeDef = edu.addChildAttributeDef("attributeDef", AttributeDefType.attr);
    attributeDef.setAssignToGroup(true);
    attributeDef.setValueType(AttributeDefValueType.integer);
    attributeDef.store();
    AttributeDefName attributeDefName1 = edu.addChildAttributeDefName(attributeDef, "testAttribute1", "testAttribute1");
    AttributeAssignAction action1 = attributeDef.getAttributeDefActionDelegate().addAction("testAction1");
    AttributeAssign attributeAssign = group.getAttributeDelegate().assignAttribute(action1.getName(), attributeDefName1).getAttributeAssign();
    AttributeAssignValue attributeAssignValue = attributeAssign.getValueDelegate().assignValueInteger(55L).getAttributeAssignValue();

    // populate PIT tables
    ChangeLogTempToEntity.convertRecords();
    
    Set<PITAttributeAssignValueView> results = new PITAttributeAssignValueQuery()
      .setAttributeDefNameId(attributeDefName1.getId())
      .setActionId(action1.getId())
      .setOwnerGroupId(group.getId())
      .setValueInteger(55L)
      .execute();
    assertEquals(1, results.size());
    
    PITAttributeAssignValueView value = results.iterator().next();
    assertEquals(attributeAssign.getId(), value.getAttributeAssignId());
    assertEquals(attributeAssignValue.getId(), value.getAttributeAssignValueId());
    assertEquals(attributeDefName1.getId(), value.getAttributeDefNameId());
    assertEquals(action1.getId(), value.getAttributeAssignActionId());
    assertEquals("group", value.getAttributeAssignTypeDb());
    assertEquals(group.getId(), value.getOwnerGroupId());
    assertEquals(null, value.getOwnerStemId());
    assertEquals(null, value.getOwnerAttributeDefId());
    assertEquals(null, value.getOwnerMembershipId());
    assertEquals(null, value.getOwnerMemberId());
    assertEquals(null, value.getOwnerAttributeAssignId());
    assertEquals(null, value.getValueString());
    assertEquals(55L, value.getValueInteger().longValue());
    assertEquals(null, value.getValueFloating());
    assertEquals(null, value.getValueMemberId());
    assertTrue(value.isActive());
    assertNotNull(value.getStartTime());
    assertNull(value.getEndTime());

    results = new PITAttributeAssignValueQuery()
      .setAttributeDefNameId(attributeDefName1.getId())
      .setActionId(action1.getId())
      .setOwnerGroupId(group.getId())
      .setValueInteger(56L)
      .execute();
    assertEquals(0, results.size());
  }
  
  /**
   * 
   */
  public void testByValueFloating() {
    Group group = edu.addChildGroup("testGroup", "testGroup");
    
    AttributeDef attributeDef = edu.addChildAttributeDef("attributeDef", AttributeDefType.attr);
    attributeDef.setAssignToGroup(true);
    attributeDef.setValueType(AttributeDefValueType.floating);
    attributeDef.store();
    AttributeDefName attributeDefName1 = edu.addChildAttributeDefName(attributeDef, "testAttribute1", "testAttribute1");
    AttributeAssignAction action1 = attributeDef.getAttributeDefActionDelegate().addAction("testAction1");
    AttributeAssign attributeAssign = group.getAttributeDelegate().assignAttribute(action1.getName(), attributeDefName1).getAttributeAssign();
    AttributeAssignValue attributeAssignValue = attributeAssign.getValueDelegate().assignValueFloating(55.55).getAttributeAssignValue();

    // populate PIT tables
    ChangeLogTempToEntity.convertRecords();
    
    Set<PITAttributeAssignValueView> results = new PITAttributeAssignValueQuery()
      .setAttributeDefNameId(attributeDefName1.getId())
      .setActionId(action1.getId())
      .setOwnerGroupId(group.getId())
      .setValueFloating(55.55)
      .execute();
    assertEquals(1, results.size());
    
    PITAttributeAssignValueView value = results.iterator().next();
    assertEquals(attributeAssign.getId(), value.getAttributeAssignId());
    assertEquals(attributeAssignValue.getId(), value.getAttributeAssignValueId());
    assertEquals(attributeDefName1.getId(), value.getAttributeDefNameId());
    assertEquals(action1.getId(), value.getAttributeAssignActionId());
    assertEquals("group", value.getAttributeAssignTypeDb());
    assertEquals(group.getId(), value.getOwnerGroupId());
    assertEquals(null, value.getOwnerStemId());
    assertEquals(null, value.getOwnerAttributeDefId());
    assertEquals(null, value.getOwnerMembershipId());
    assertEquals(null, value.getOwnerMemberId());
    assertEquals(null, value.getOwnerAttributeAssignId());
    assertEquals(null, value.getValueString());
    assertEquals(null, value.getValueInteger());
    assertEquals(55.55, value.getValueFloating().doubleValue());
    assertEquals(null, value.getValueMemberId());
    assertTrue(value.isActive());
    assertNotNull(value.getStartTime());
    assertNull(value.getEndTime());

    results = new PITAttributeAssignValueQuery()
      .setAttributeDefNameId(attributeDefName1.getId())
      .setActionId(action1.getId())
      .setOwnerGroupId(group.getId())
      .setValueFloating(55.56)
      .execute();
    assertEquals(0, results.size());
  }

  /**
   * 
   */
  public void testByValueMemberId() {
    Group group = edu.addChildGroup("testGroup", "testGroup");
    Member member = MemberFinder.findBySubject(grouperSession, SubjectFinder.findRootSubject(), false);
    
    AttributeDef attributeDef = edu.addChildAttributeDef("attributeDef", AttributeDefType.attr);
    attributeDef.setAssignToGroup(true);
    attributeDef.setValueType(AttributeDefValueType.memberId);
    attributeDef.store();
    AttributeDefName attributeDefName1 = edu.addChildAttributeDefName(attributeDef, "testAttribute1", "testAttribute1");
    AttributeAssignAction action1 = attributeDef.getAttributeDefActionDelegate().addAction("testAction1");
    AttributeAssign attributeAssign = group.getAttributeDelegate().assignAttribute(action1.getName(), attributeDefName1).getAttributeAssign();
    AttributeAssignValue attributeAssignValue = attributeAssign.getValueDelegate().assignValueMember(member).getAttributeAssignValue();

    // populate PIT tables
    ChangeLogTempToEntity.convertRecords();
    
    Set<PITAttributeAssignValueView> results = new PITAttributeAssignValueQuery()
      .setAttributeDefNameId(attributeDefName1.getId())
      .setActionId(action1.getId())
      .setOwnerGroupId(group.getId())
      .setValueMemberId(member.getUuid())
      .execute();
    assertEquals(1, results.size());
    
    PITAttributeAssignValueView value = results.iterator().next();
    assertEquals(attributeAssign.getId(), value.getAttributeAssignId());
    assertEquals(attributeAssignValue.getId(), value.getAttributeAssignValueId());
    assertEquals(attributeDefName1.getId(), value.getAttributeDefNameId());
    assertEquals(action1.getId(), value.getAttributeAssignActionId());
    assertEquals("group", value.getAttributeAssignTypeDb());
    assertEquals(group.getId(), value.getOwnerGroupId());
    assertEquals(null, value.getOwnerStemId());
    assertEquals(null, value.getOwnerAttributeDefId());
    assertEquals(null, value.getOwnerMembershipId());
    assertEquals(null, value.getOwnerMemberId());
    assertEquals(null, value.getOwnerAttributeAssignId());
    assertEquals(null, value.getValueString());
    assertEquals(null, value.getValueInteger());
    assertEquals(null, value.getValueFloating());
    assertEquals(member.getUuid(), value.getValueMemberId());
    assertTrue(value.isActive());
    assertNotNull(value.getStartTime());
    assertNull(value.getEndTime());

    results = new PITAttributeAssignValueQuery()
      .setAttributeDefNameId(attributeDefName1.getId())
      .setActionId(action1.getId())
      .setOwnerGroupId(group.getId())
      .setValueMemberId(GrouperUuid.getUuid())
      .execute();
    assertEquals(0, results.size());
  }
  
  /**
   * 
   */
  public void testByValueString() {
    Group group = edu.addChildGroup("testGroup", "testGroup");
    
    AttributeDef attributeDef = edu.addChildAttributeDef("attributeDef", AttributeDefType.attr);
    attributeDef.setAssignToGroup(true);
    attributeDef.setValueType(AttributeDefValueType.string);
    attributeDef.store();
    AttributeDefName attributeDefName1 = edu.addChildAttributeDefName(attributeDef, "testAttribute1", "testAttribute1");
    AttributeAssignAction action1 = attributeDef.getAttributeDefActionDelegate().addAction("testAction1");
    AttributeAssign attributeAssign = group.getAttributeDelegate().assignAttribute(action1.getName(), attributeDefName1).getAttributeAssign();
    AttributeAssignValue attributeAssignValue = attributeAssign.getValueDelegate().assignValueString("test").getAttributeAssignValue();

    // populate PIT tables
    ChangeLogTempToEntity.convertRecords();
    
    Set<PITAttributeAssignValueView> results = new PITAttributeAssignValueQuery()
      .setAttributeDefNameId(attributeDefName1.getId())
      .setActionId(action1.getId())
      .setOwnerGroupId(group.getId())
      .setValueString("test")
      .execute();
    assertEquals(1, results.size());
    
    PITAttributeAssignValueView value = results.iterator().next();
    assertEquals(attributeAssign.getId(), value.getAttributeAssignId());
    assertEquals(attributeAssignValue.getId(), value.getAttributeAssignValueId());
    assertEquals(attributeDefName1.getId(), value.getAttributeDefNameId());
    assertEquals(action1.getId(), value.getAttributeAssignActionId());
    assertEquals("group", value.getAttributeAssignTypeDb());
    assertEquals(group.getId(), value.getOwnerGroupId());
    assertEquals(null, value.getOwnerStemId());
    assertEquals(null, value.getOwnerAttributeDefId());
    assertEquals(null, value.getOwnerMembershipId());
    assertEquals(null, value.getOwnerMemberId());
    assertEquals(null, value.getOwnerAttributeAssignId());
    assertEquals("test", value.getValueString());
    assertEquals(null, value.getValueInteger());
    assertEquals(null, value.getValueFloating());
    assertEquals(null, value.getValueMemberId());
    assertTrue(value.isActive());
    assertNotNull(value.getStartTime());
    assertNull(value.getEndTime());

    results = new PITAttributeAssignValueQuery()
      .setAttributeDefNameId(attributeDefName1.getId())
      .setActionId(action1.getId())
      .setOwnerGroupId(group.getId())
      .setValueString("test2")
      .execute();
    assertEquals(0, results.size());
  }
  
  /**
   * 
   */
  public void testByAttributeAssign() {
    Group group = edu.addChildGroup("testGroup", "testGroup");
    Group group2 = edu.addChildGroup("testGroup2", "testGroup2");
    
    AttributeDef attributeDef = edu.addChildAttributeDef("attributeDef", AttributeDefType.attr);
    attributeDef.setAssignToGroup(true);
    attributeDef.setValueType(AttributeDefValueType.string);
    attributeDef.store();
    AttributeDefName attributeDefName1 = edu.addChildAttributeDefName(attributeDef, "testAttribute1", "testAttribute1");
    AttributeAssignAction action1 = attributeDef.getAttributeDefActionDelegate().addAction("testAction1");
    AttributeAssign attributeAssign = group.getAttributeDelegate().assignAttribute(action1.getName(), attributeDefName1).getAttributeAssign();
    AttributeAssign attributeAssign2 = group2.getAttributeDelegate().assignAttribute(action1.getName(), attributeDefName1).getAttributeAssign();
    attributeAssign.getValueDelegate().assignValueString("test").getAttributeAssignValue();
    attributeAssign2.getValueDelegate().assignValueString("test2").getAttributeAssignValue();

    // populate PIT tables
    ChangeLogTempToEntity.convertRecords();
    
    Set<PITAttributeAssignValueView> results = new PITAttributeAssignValueQuery()
      .setAttributeAssignId(attributeAssign.getId())
      .setValueString("test")
      .execute();
    assertEquals(1, results.size());
    
    results = new PITAttributeAssignValueQuery()
      .setAttributeAssignId(attributeAssign2.getId())
      .setValueString("test")
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
    attributeDef.setValueType(AttributeDefValueType.string);
    attributeDef.store();
    AttributeDefName attributeDefName1 = edu.addChildAttributeDefName(attributeDef, "testAttribute1", "testAttribute1");
    AttributeDefName attributeDefName2 = edu.addChildAttributeDefName(attributeDef, "testAttribute2", "testAttribute2");
    AttributeAssignAction action1 = attributeDef.getAttributeDefActionDelegate().addAction("testAction1");
    AttributeAssign attributeAssign = group.getAttributeDelegate().assignAttribute(action1.getName(), attributeDefName1).getAttributeAssign();
    attributeAssign.getValueDelegate().assignValueString("test").getAttributeAssignValue();

    // populate PIT tables
    ChangeLogTempToEntity.convertRecords();
    
    Set<PITAttributeAssignValueView> results = new PITAttributeAssignValueQuery()
      .setAttributeDefNameId(attributeDefName1.getId())
      .setActionId(action1.getId())
      .setOwnerGroupId(group.getId())
      .setValueString("test")
      .execute();
    assertEquals(1, results.size());
    
    results = new PITAttributeAssignValueQuery()
      .setAttributeDefNameId(attributeDefName2.getId())
      .setActionId(action1.getId())
      .setOwnerGroupId(group.getId())
      .setValueString("test")
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
    attributeDef.setValueType(AttributeDefValueType.string);
    attributeDef.store();
    AttributeDefName attributeDefName1 = edu.addChildAttributeDefName(attributeDef, "testAttribute1", "testAttribute1");
    AttributeAssignAction action1 = attributeDef.getAttributeDefActionDelegate().addAction("testAction1");
    AttributeAssignAction action2 = attributeDef.getAttributeDefActionDelegate().addAction("testAction2");
    AttributeAssign attributeAssign = group.getAttributeDelegate().assignAttribute(action1.getName(), attributeDefName1).getAttributeAssign();
    attributeAssign.getValueDelegate().assignValueString("test").getAttributeAssignValue();

    // populate PIT tables
    ChangeLogTempToEntity.convertRecords();
    
    Set<PITAttributeAssignValueView> results = new PITAttributeAssignValueQuery()
      .setAttributeDefNameId(attributeDefName1.getId())
      .setActionId(action1.getId())
      .setOwnerGroupId(group.getId())
      .setValueString("test")
      .execute();
    assertEquals(1, results.size());
    
    results = new PITAttributeAssignValueQuery()
      .setAttributeDefNameId(attributeDefName1.getId())
      .setActionId(action2.getId())
      .setOwnerGroupId(group.getId())
      .setValueString("test")
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
    attributeDef.setValueType(AttributeDefValueType.string);
    attributeDef.store();
    AttributeDefName attributeDefName1 = edu.addChildAttributeDefName(attributeDef, "testAttribute1", "testAttribute1");
    AttributeAssignAction action1 = attributeDef.getAttributeDefActionDelegate().addAction("testAction1");
    AttributeAssign attributeAssign = group.getAttributeDelegate().assignAttribute(action1.getName(), attributeDefName1).getAttributeAssign();
    attributeAssign.getValueDelegate().assignValueString("test").getAttributeAssignValue();

    // populate PIT tables
    ChangeLogTempToEntity.convertRecords();
    
    Set<PITAttributeAssignValueView> results = new PITAttributeAssignValueQuery()
      .setAttributeDefNameId(attributeDefName1.getId())
      .setActionId(action1.getId())
      .setOwnerGroupId(group.getId())
      .setValueString("test")
      .execute();
    assertEquals(1, results.size());
    
    results = new PITAttributeAssignValueQuery()
      .setAttributeDefNameId(attributeDefName1.getId())
      .setActionId(action1.getId())
      .setOwnerStemId(group.getId())
      .setValueString("test")
      .execute();
    assertEquals(0, results.size());
  }
  
  /**
   * 
   */
  public void testByOwnerStem() {
    Stem stem = edu.addChildStem("testStem", "testStem");
    
    AttributeDef attributeDef = edu.addChildAttributeDef("attributeDef", AttributeDefType.attr);
    attributeDef.setAssignToStem(true);
    attributeDef.setValueType(AttributeDefValueType.string);
    attributeDef.store();
    AttributeDefName attributeDefName1 = edu.addChildAttributeDefName(attributeDef, "testAttribute1", "testAttribute1");
    AttributeAssignAction action1 = attributeDef.getAttributeDefActionDelegate().addAction("testAction1");
    AttributeAssign attributeAssign = stem.getAttributeDelegate().assignAttribute(action1.getName(), attributeDefName1).getAttributeAssign();
    attributeAssign.getValueDelegate().assignValueString("test").getAttributeAssignValue();

    // populate PIT tables
    ChangeLogTempToEntity.convertRecords();
    
    Set<PITAttributeAssignValueView> results = new PITAttributeAssignValueQuery()
      .setAttributeDefNameId(attributeDefName1.getId())
      .setActionId(action1.getId())
      .setOwnerStemId(stem.getUuid())
      .setValueString("test")
      .execute();
    assertEquals(1, results.size());
    
    results = new PITAttributeAssignValueQuery()
      .setAttributeDefNameId(attributeDefName1.getId())
      .setActionId(action1.getId())
      .setOwnerGroupId(stem.getUuid())
      .setValueString("test")
      .execute();
    assertEquals(0, results.size());
  }
  
  /**
   * 
   */
  public void testByOwnerAttributeDef() {
    AttributeDef attributeDef0 = edu.addChildAttributeDef("attributeDef0", AttributeDefType.attr);
    
    AttributeDef attributeDef = edu.addChildAttributeDef("attributeDef", AttributeDefType.attr);
    attributeDef.setAssignToAttributeDef(true);
    attributeDef.setValueType(AttributeDefValueType.string);
    attributeDef.store();
    AttributeDefName attributeDefName1 = edu.addChildAttributeDefName(attributeDef, "testAttribute1", "testAttribute1");
    AttributeAssignAction action1 = attributeDef.getAttributeDefActionDelegate().addAction("testAction1");
    AttributeAssign attributeAssign = attributeDef0.getAttributeDelegate().assignAttribute(action1.getName(), attributeDefName1).getAttributeAssign();
    attributeAssign.getValueDelegate().assignValueString("test").getAttributeAssignValue();

    // populate PIT tables
    ChangeLogTempToEntity.convertRecords();
    
    Set<PITAttributeAssignValueView> results = new PITAttributeAssignValueQuery()
      .setAttributeDefNameId(attributeDefName1.getId())
      .setActionId(action1.getId())
      .setOwnerAttributeDefId(attributeDef0.getId())
      .setValueString("test")
      .execute();
    assertEquals(1, results.size());
    
    results = new PITAttributeAssignValueQuery()
      .setAttributeDefNameId(attributeDefName1.getId())
      .setActionId(action1.getId())
      .setOwnerGroupId(attributeDef0.getId())
      .setValueString("test")
      .execute();
    assertEquals(0, results.size());
  }
  
  /**
   * 
   */
  public void testByOwnerMember() {
    Member member = MemberFinder.findBySubject(grouperSession, SubjectFinder.findRootSubject(), false);
    
    AttributeDef attributeDef = edu.addChildAttributeDef("attributeDef", AttributeDefType.attr);
    attributeDef.setAssignToMember(true);
    attributeDef.setValueType(AttributeDefValueType.string);
    attributeDef.store();
    AttributeDefName attributeDefName1 = edu.addChildAttributeDefName(attributeDef, "testAttribute1", "testAttribute1");
    AttributeAssignAction action1 = attributeDef.getAttributeDefActionDelegate().addAction("testAction1");
    AttributeAssign attributeAssign = member.getAttributeDelegate().assignAttribute(action1.getName(), attributeDefName1).getAttributeAssign();
    attributeAssign.getValueDelegate().assignValueString("test").getAttributeAssignValue();

    // populate PIT tables
    ChangeLogTempToEntity.convertRecords();
    
    Set<PITAttributeAssignValueView> results = new PITAttributeAssignValueQuery()
      .setAttributeDefNameId(attributeDefName1.getId())
      .setActionId(action1.getId())
      .setOwnerMemberId(member.getUuid())
      .setValueString("test")
      .execute();
    assertEquals(1, results.size());
    
    results = new PITAttributeAssignValueQuery()
      .setAttributeDefNameId(attributeDefName1.getId())
      .setActionId(action1.getId())
      .setOwnerGroupId(member.getUuid())
      .setValueString("test")
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
    attributeDef.setValueType(AttributeDefValueType.string);
    attributeDef.store();
    AttributeDefName attributeDefName1 = edu.addChildAttributeDefName(attributeDef, "testAttribute1", "testAttribute1");
    AttributeAssignAction action1 = attributeDef.getAttributeDefActionDelegate().addAction("testAction1");
    AttributeAssign attributeAssign = immediateMembership.getAttributeDelegate().assignAttribute(action1.getName(), attributeDefName1).getAttributeAssign();
    attributeAssign.getValueDelegate().assignValueString("test").getAttributeAssignValue();

    // populate PIT tables
    ChangeLogTempToEntity.convertRecords();
    
    Set<PITAttributeAssignValueView> results = new PITAttributeAssignValueQuery()
      .setAttributeDefNameId(attributeDefName1.getId())
      .setActionId(action1.getId())
      .setOwnerMembershipId(immediateMembership.getImmediateMembershipId())
      .setValueString("test")
      .execute();
    assertEquals(1, results.size());
    
    results = new PITAttributeAssignValueQuery()
      .setAttributeDefNameId(attributeDefName1.getId())
      .setActionId(action1.getId())
      .setOwnerGroupId(immediateMembership.getUuid())
      .setValueString("test")
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
    attributeDef0.setValueType(AttributeDefValueType.string);
    attributeDef0.store();
    AttributeDefName attributeDefName0 = edu.addChildAttributeDefName(attributeDef0, "testAttribute0", "testAttribute0");
    AttributeAssignAction action0 = attributeDef0.getAttributeDefActionDelegate().addAction("testAction0");
    AttributeAssign attributeAssign0 = group.getAttributeDelegate().assignAttribute(action0.getName(), attributeDefName0).getAttributeAssign();

    AttributeDef attributeDef = edu.addChildAttributeDef("attributeDef", AttributeDefType.attr);
    attributeDef.setAssignToGroupAssn(true);
    attributeDef.setValueType(AttributeDefValueType.string);
    attributeDef.store();
    AttributeDefName attributeDefName1 = edu.addChildAttributeDefName(attributeDef, "testAttribute1", "testAttribute1");
    AttributeAssignAction action1 = attributeDef.getAttributeDefActionDelegate().addAction("testAction1");
    AttributeAssign attributeAssign1 = attributeAssign0.getAttributeDelegate().assignAttribute(action1.getName(), attributeDefName1).getAttributeAssign();
    attributeAssign1.getValueDelegate().assignValueString("test").getAttributeAssignValue();

    // populate PIT tables
    ChangeLogTempToEntity.convertRecords();
    
    Set<PITAttributeAssignValueView> results = new PITAttributeAssignValueQuery()
      .setAttributeDefNameId(attributeDefName1.getId())
      .setActionId(action1.getId())
      .setOwnerAttributeAssignId(attributeAssign0.getId())
      .setValueString("test")
      .execute();
    assertEquals(1, results.size());
    
    results = new PITAttributeAssignValueQuery()
      .setAttributeDefNameId(attributeDefName1.getId())
      .setActionId(action1.getId())
      .setOwnerGroupId(attributeAssign0.getId())
      .setValueString("test")
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
    attributeDef.setValueType(AttributeDefValueType.string);
    attributeDef.store();
    AttributeDefName attributeDefName1 = edu.addChildAttributeDefName(attributeDef, "testAttribute1", "testAttribute1");
    AttributeAssignAction action1 = attributeDef.getAttributeDefActionDelegate().addAction("testAction1");
    AttributeAssign attributeAssign = immediateMembership.getAttributeDelegateEffMship().assignAttribute(action1.getName(), attributeDefName1).getAttributeAssign();
    attributeAssign.getValueDelegate().assignValueString("test").getAttributeAssignValue();

    // populate PIT tables
    ChangeLogTempToEntity.convertRecords();
    
    Set<PITAttributeAssignValueView> results = new PITAttributeAssignValueQuery()
      .setAttributeDefNameId(attributeDefName1.getId())
      .setActionId(action1.getId())
      .setOwnerMembershipId(immediateMembership.getImmediateMembershipId())
      .setValueString("test")
      .execute();
    assertEquals(0, results.size());
    
    results = new PITAttributeAssignValueQuery()
      .setAttributeDefNameId(attributeDefName1.getId())
      .setActionId(action1.getId())
      .setOwnerGroupId(group.getUuid())
      .setValueString("test")
      .execute();
    assertEquals(1, results.size());
    
    results = new PITAttributeAssignValueQuery()
      .setAttributeDefNameId(attributeDefName1.getId())
      .setActionId(action1.getId())
      .setOwnerMemberId(member.getUuid())
      .setValueString("test")
      .execute();
    assertEquals(1, results.size());

    results = new PITAttributeAssignValueQuery()
      .setAttributeDefNameId(attributeDefName1.getId())
      .setActionId(action1.getId())
      .setOwnerGroupId(group.getUuid())
      .setOwnerMemberId(member.getUuid())
      .setValueString("test")
      .execute();
    assertEquals(1, results.size());
  }
  
  /**
   * 
   */
  @SuppressWarnings("unused")
  public void testAttributeAssignEnableDisable() {
    Group group = edu.addChildGroup("testGroup", "testGroup");
    
    AttributeDef attributeDef = edu.addChildAttributeDef("attributeDef", AttributeDefType.attr);
    attributeDef.setAssignToGroup(true);
    attributeDef.setAssignToGroupAssn(true);
    attributeDef.setMultiValued(true);
    attributeDef.setValueType(AttributeDefValueType.string);
    attributeDef.store();
    AttributeDefName attributeDefName1 = edu.addChildAttributeDefName(attributeDef, "testAttribute1", "testAttribute1");
    AttributeDefName attributeDefName2 = edu.addChildAttributeDefName(attributeDef, "testAttribute2", "testAttribute2");
    AttributeDefName attributeDefName3 = edu.addChildAttributeDefName(attributeDef, "testAttribute3", "testAttribute3");
    AttributeAssign attributeAssign1 = group.getAttributeDelegate().assignAttribute(attributeDefName1).getAttributeAssign();
    AttributeAssignValue value1 = attributeAssign1.getValueDelegate().addValue("value1").getAttributeAssignValue();    
    AttributeAssignValue value2 = attributeAssign1.getValueDelegate().addValue("value2").getAttributeAssignValue();
    value2.delete();

    AttributeAssign attributeAssign2 = attributeAssign1.getAttributeDelegate().assignAttribute(attributeDefName2).getAttributeAssign();
    AttributeAssign attributeAssign3 = attributeAssign1.getAttributeDelegate().assignAttribute(attributeDefName3).getAttributeAssign();

    AttributeAssignValue value3 = attributeAssign2.getValueDelegate().addValue("value3").getAttributeAssignValue();    
    AttributeAssignValue value4 = attributeAssign2.getValueDelegate().addValue("value4").getAttributeAssignValue();
    AttributeAssignValue value5 = attributeAssign3.getValueDelegate().addValue("value5").getAttributeAssignValue();    
    AttributeAssignValue value6 = attributeAssign3.getValueDelegate().addValue("value6").getAttributeAssignValue();

    attributeAssign1 = AttributeAssignFinder.findById(attributeAssign1.getId(), true);
    attributeAssign3 = AttributeAssignFinder.findById(attributeAssign3.getId(), true);

    value4.delete();
    value6.delete();
    
    // populate PIT tables
    ChangeLogTempToEntity.convertRecords();
    
    Date beforeDisable = getDateWithSleep();
    
    // disable
    attributeAssign1.setEnabled(false);
    attributeAssign1.setEnabledTime(new Timestamp(new Date().getTime() + 100000));
    attributeAssign1.saveOrUpdate();
    
    attributeAssign3.setEnabled(false);
    attributeAssign3.setEnabledTime(new Timestamp(new Date().getTime() + 100000));
    attributeAssign3.saveOrUpdate();
    
    // update PIT tables
    ChangeLogTempToEntity.convertRecords();
    
    Date afterDisable = getDateWithSleep();
    
    // enable
    attributeAssign1.setEnabled(true);
    attributeAssign1.setEnabledTime(null);
    attributeAssign1.saveOrUpdate();
    
    attributeAssign3.setEnabled(true);
    attributeAssign3.setEnabledTime(null);
    attributeAssign3.saveOrUpdate();
    
    // update PIT tables
    ChangeLogTempToEntity.convertRecords();
    
    Date afterEnable = getDateWithSleep();

    
    Set<PITAttributeAssignValueView> results = new PITAttributeAssignValueQuery()
      .setAttributeDefNameId(attributeDefName1.getId())
      .setOwnerGroupId(group.getId())
      .setValueString("value1")
      .setEndDateBefore(afterEnable)
      .execute();
    assertEquals(1, results.size());
    
    results = new PITAttributeAssignValueQuery()
      .setAttributeDefNameId(attributeDefName1.getId())
      .setOwnerGroupId(group.getId())
      .setValueString("value1")
      .setEndDateAfter(afterEnable)
      .setAttributeAssignId(attributeAssign1.getId())
      .execute();
    assertEquals(1, results.size());
    
    results = new PITAttributeAssignValueQuery()
      .setAttributeDefNameId(attributeDefName1.getId())
      .setOwnerGroupId(group.getId())
      .setValueString("value2")
      .setEndDateBefore(beforeDisable)
      .execute();
    assertEquals(1, results.size());
    
    results = new PITAttributeAssignValueQuery()
      .setAttributeDefNameId(attributeDefName1.getId())
      .setOwnerGroupId(group.getId())
      .setValueString("value2")
      .setEndDateAfter(beforeDisable)
      .execute();
    assertEquals(0, results.size());
    
    results = new PITAttributeAssignValueQuery()
      .setAttributeDefNameId(attributeDefName2.getId())
      .setValueString("value3")
      .setEndDateBefore(afterEnable)
      .execute();
    assertEquals(1, results.size());
    
    results = new PITAttributeAssignValueQuery()
      .setAttributeDefNameId(attributeDefName2.getId())
      .setOwnerAttributeAssignId(attributeAssign1.getId())
      .setValueString("value3")
      .setEndDateAfter(afterEnable)
      .setAttributeAssignId(attributeAssign2.getId())
      .execute();
    assertEquals(1, results.size());
    
    results = new PITAttributeAssignValueQuery()
      .setAttributeDefNameId(attributeDefName2.getId())
      .setValueString("value4")
      .setEndDateBefore(beforeDisable)
      .execute();
    assertEquals(1, results.size());
    
    results = new PITAttributeAssignValueQuery()
      .setAttributeDefNameId(attributeDefName2.getId())
      .setValueString("value4")
      .setEndDateAfter(beforeDisable)
      .execute();
    assertEquals(0, results.size());
    
    results = new PITAttributeAssignValueQuery()
      .setAttributeDefNameId(attributeDefName3.getId())
      .setValueString("value5")
      .setEndDateBefore(afterEnable)
      .execute();
    assertEquals(1, results.size());
    
    results = new PITAttributeAssignValueQuery()
      .setAttributeDefNameId(attributeDefName3.getId())
      .setOwnerAttributeAssignId(attributeAssign1.getId())
      .setValueString("value5")
      .setEndDateAfter(afterEnable)
      .setAttributeAssignId(attributeAssign3.getId())
      .execute();
    assertEquals(1, results.size());
    
    results = new PITAttributeAssignValueQuery()
      .setAttributeDefNameId(attributeDefName3.getId())
      .setValueString("value6")
      .setEndDateBefore(beforeDisable)
      .execute();
    assertEquals(1, results.size());
    
    results = new PITAttributeAssignValueQuery()
      .setAttributeDefNameId(attributeDefName3.getId())
      .setValueString("value6")
      .setEndDateAfter(beforeDisable)
      .execute();
    assertEquals(0, results.size());
     
    Set<PITAttributeAssign> results2 = new PITAttributeAssignQuery()
      .setAttributeDefNameId(attributeDefName1.getId())
      .setOwnerGroupId(group.getId())
      .setEndDateBefore(afterDisable)
      .execute();
    assertEquals(1, results2.size());
    
    results2 = new PITAttributeAssignQuery()
      .setAttributeDefNameId(attributeDefName1.getId())
      .setOwnerGroupId(group.getId())
      .setStartDateAfter(afterDisable)
      .setEndDateAfter(afterEnable)
      .execute();
    assertEquals(1, results2.size());     
    
    results2 = new PITAttributeAssignQuery()
      .setAttributeDefNameId(attributeDefName2.getId())
      .setEndDateBefore(afterEnable)
      .execute();
    assertEquals(1, results2.size());
    
    results2 = new PITAttributeAssignQuery()
      .setAttributeDefNameId(attributeDefName2.getId())
      .setOwnerAttributeAssignId(attributeAssign1.getId())
      .setEndDateAfter(afterEnable)
      .execute();
    assertEquals(1, results2.size());
    
    results2 = new PITAttributeAssignQuery()
      .setAttributeDefNameId(attributeDefName3.getId())
      .setEndDateBefore(afterEnable)
      .execute();
    assertEquals(1, results2.size());
    
    results2 = new PITAttributeAssignQuery()
      .setAttributeDefNameId(attributeDefName3.getId())
      .setOwnerAttributeAssignId(attributeAssign1.getId())
      .setEndDateAfter(afterEnable)
      .execute();
    assertEquals(1, results2.size());
  }
  
  /**
   * 
   */
  @SuppressWarnings("unused")
  public void testMembershipEnableDisable() {
    Group group = edu.addChildGroup("testGroup", "testGroup");
    group.addMember(SubjectFinder.findRootSubject());
    Membership immediateMembership = MembershipFinder.findImmediateMembership(grouperSession, group, SubjectFinder.findRootSubject(), Group.getDefaultList(), true);

    AttributeDef attributeDef = edu.addChildAttributeDef("attributeDef", AttributeDefType.attr);
    attributeDef.setAssignToImmMembership(true);
    attributeDef.setAssignToImmMembershipAssn(true);
    attributeDef.setMultiValued(true);
    attributeDef.setValueType(AttributeDefValueType.string);
    attributeDef.store();
    AttributeDefName attributeDefName1 = edu.addChildAttributeDefName(attributeDef, "testAttribute1", "testAttribute1");
    AttributeDefName attributeDefName2 = edu.addChildAttributeDefName(attributeDef, "testAttribute2", "testAttribute2");
    AttributeAssign attributeAssign1 = immediateMembership.getAttributeDelegate().assignAttribute(attributeDefName1).getAttributeAssign();
    AttributeAssignValue value1 = attributeAssign1.getValueDelegate().addValue("value1").getAttributeAssignValue();    
    AttributeAssignValue value2 = attributeAssign1.getValueDelegate().addValue("value2").getAttributeAssignValue();
    value2.delete();

    AttributeAssign attributeAssign2 = attributeAssign1.getAttributeDelegate().assignAttribute(attributeDefName2).getAttributeAssign();

    AttributeAssignValue value3 = attributeAssign2.getValueDelegate().addValue("value3").getAttributeAssignValue();    
    AttributeAssignValue value4 = attributeAssign2.getValueDelegate().addValue("value4").getAttributeAssignValue();

    value4.delete();
    
    // populate PIT tables
    ChangeLogTempToEntity.convertRecords();
    
    Date beforeDisable = getDateWithSleep();
    
    // disable
    immediateMembership.setEnabled(false);
    immediateMembership.setEnabledTime(new Timestamp(new Date().getTime() + 100000));
    immediateMembership.update();
    
    // update PIT tables
    ChangeLogTempToEntity.convertRecords();
    
    Date afterDisable = getDateWithSleep();
    
    // enable
    immediateMembership.setEnabled(true);
    immediateMembership.setEnabledTime(null);
    immediateMembership.update();
    
    
    // update PIT tables
    ChangeLogTempToEntity.convertRecords();
    
    Date afterEnable = getDateWithSleep();

    
    Set<PITAttributeAssignValueView> results = new PITAttributeAssignValueQuery()
      .setAttributeDefNameId(attributeDefName1.getId())
      .setValueString("value1")
      .setEndDateBefore(afterEnable)
      .execute();
    assertEquals(1, results.size());
    
    results = new PITAttributeAssignValueQuery()
      .setAttributeDefNameId(attributeDefName1.getId())
      .setOwnerMembershipId(immediateMembership.getImmediateMembershipId())
      .setValueString("value1")
      .setEndDateAfter(afterEnable)
      .setAttributeAssignId(attributeAssign1.getId())
      .execute();
    assertEquals(1, results.size());
    
    results = new PITAttributeAssignValueQuery()
      .setAttributeDefNameId(attributeDefName1.getId())
      .setValueString("value2")
      .setEndDateBefore(beforeDisable)
      .execute();
    assertEquals(1, results.size());
    
    results = new PITAttributeAssignValueQuery()
      .setAttributeDefNameId(attributeDefName1.getId())
      .setValueString("value2")
      .setEndDateAfter(beforeDisable)
      .execute();
    assertEquals(0, results.size());
    
    results = new PITAttributeAssignValueQuery()
      .setAttributeDefNameId(attributeDefName2.getId())
      .setValueString("value3")
      .setEndDateBefore(afterEnable)
      .execute();
    assertEquals(1, results.size());
    
    results = new PITAttributeAssignValueQuery()
      .setAttributeDefNameId(attributeDefName2.getId())
      .setOwnerAttributeAssignId(attributeAssign1.getId())
      .setValueString("value3")
      .setEndDateAfter(afterEnable)
      .setAttributeAssignId(attributeAssign2.getId())
      .execute();
    assertEquals(1, results.size());
    
    results = new PITAttributeAssignValueQuery()
      .setAttributeDefNameId(attributeDefName2.getId())
      .setValueString("value4")
      .setEndDateBefore(beforeDisable)
      .execute();
    assertEquals(1, results.size());
    
    results = new PITAttributeAssignValueQuery()
      .setAttributeDefNameId(attributeDefName2.getId())
      .setValueString("value4")
      .setEndDateAfter(beforeDisable)
      .execute();
    assertEquals(0, results.size());
     
    Set<PITAttributeAssign> results2 = new PITAttributeAssignQuery()
      .setAttributeDefNameId(attributeDefName1.getId())
      .setEndDateBefore(afterEnable)
      .execute();
    assertEquals(1, results2.size());
    
    results2 = new PITAttributeAssignQuery()
      .setAttributeDefNameId(attributeDefName1.getId())
      .setOwnerMembershipId(immediateMembership.getImmediateMembershipId())
      .setEndDateAfter(afterEnable)
      .execute();
    assertEquals(1, results2.size());     
    
    results2 = new PITAttributeAssignQuery()
      .setAttributeDefNameId(attributeDefName2.getId())
      .setEndDateBefore(afterEnable)
      .execute();
    assertEquals(1, results2.size());
    
    results2 = new PITAttributeAssignQuery()
      .setAttributeDefNameId(attributeDefName2.getId())
      .setOwnerAttributeAssignId(attributeAssign1.getId())
      .setEndDateAfter(afterEnable)
      .execute();
    assertEquals(1, results2.size());
    
    Set<PITMembershipView> results3 = new PITMembershipViewQuery()
      .setOwnerId(group.getId())
      .setMemberId(MemberFinder.findBySubject(grouperSession, SubjectFinder.findRootSubject(), true).getUuid())
      .setFieldId(Group.getDefaultList().getUuid())
      .setEndDateBefore(afterEnable)
      .execute();
    assertEquals(1, results3.size());
    assertFalse(immediateMembership.getImmediateMembershipId().equals(results3.iterator().next().getMembershipId()));

    results3 = new PITMembershipViewQuery()
      .setOwnerId(group.getId())
      .setMemberId(MemberFinder.findBySubject(grouperSession, SubjectFinder.findRootSubject(), true).getUuid())
      .setFieldId(Group.getDefaultList().getUuid())
      .setEndDateAfter(afterEnable)
      .execute();
    assertEquals(1, results3.size());
    assertEquals(immediateMembership.getImmediateMembershipId(), results3.iterator().next().getMembershipId());
  }
}