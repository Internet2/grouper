package edu.internet2.middleware.grouper.pit;

import java.util.Date;
import java.util.Set;

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
      .setOwnerStemId(group.getId())
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
      .setOwnerGroupId(stem.getUuid())
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
      .setOwnerGroupId(attributeDef0.getId())
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
      .setOwnerGroupId(member.getUuid())
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
      .setOwnerGroupId(immediateMembership.getUuid())
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
      .setOwnerGroupId(attributeAssign0.getId())
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
}