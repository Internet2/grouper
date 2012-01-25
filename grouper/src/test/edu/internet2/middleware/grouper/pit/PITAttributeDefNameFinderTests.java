package edu.internet2.middleware.grouper.pit;

import java.sql.Timestamp;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.AttributeDefType;
import edu.internet2.middleware.grouper.changeLog.ChangeLogTempToEntity;
import edu.internet2.middleware.grouper.exception.AttributeDefNameNotFoundException;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SessionHelper;
import edu.internet2.middleware.grouper.helper.StemHelper;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.pit.finder.PITAttributeDefNameFinder;
import edu.internet2.middleware.grouper.privs.AttributeDefPrivilege;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * @author shilen
 * $Id$
 */
public class PITAttributeDefNameFinderTests extends GrouperTest {
  
  /** top level stem */
  private Stem edu;

  /** root session */
  private GrouperSession grouperSession;
  
  /** root stem */
  private Stem root;
  
  /**
   * @param name
   */
  public PITAttributeDefNameFinderTests(String name) {
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
  
  private Timestamp getTimestampWithSleep() {
    GrouperUtil.sleep(100);
    Date date = new Date();
    GrouperUtil.sleep(100);
    return new Timestamp(date.getTime());
  }
  
  /**
   * 
   */
  public void testFindById() {
    Member member1 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ1, true);
    
    AttributeDef attributeDef1 = edu.addChildAttributeDef("test1", AttributeDefType.attr);
    AttributeDef attributeDef2 = edu.addChildAttributeDef("test2", AttributeDefType.attr);
    AttributeDef attributeDef3 = edu.addChildAttributeDef("test3", AttributeDefType.attr);
    AttributeDefName attributeDefName1 = edu.addChildAttributeDefName(attributeDef1, "test1", "test1");
    AttributeDefName attributeDefName2 = edu.addChildAttributeDefName(attributeDef2, "test2", "test2");
    AttributeDefName attributeDefName3 = edu.addChildAttributeDefName(attributeDef3, "test3", "test3");
    attributeDef2.getPrivilegeDelegate().grantPriv(member1.getSubject(), AttributeDefPrivilege.ATTR_VIEW, true);
    attributeDef3.getPrivilegeDelegate().grantPriv(member1.getSubject(), AttributeDefPrivilege.ATTR_VIEW, true);
    ChangeLogTempToEntity.convertRecords();
    
    attributeDefName3.delete();
    ChangeLogTempToEntity.convertRecords();

    PITAttributeDefName pitAttributeDefName1 = PITAttributeDefNameFinder.findBySourceId(attributeDefName1.getId(), true).iterator().next();
    assertNotNull(pitAttributeDefName1);
    
    PITAttributeDefName pitAttributeDefName2 = PITAttributeDefNameFinder.findBySourceId(attributeDefName2.getId(), true).iterator().next();
    assertNotNull(pitAttributeDefName2);
    
    PITAttributeDefName pitAttributeDefName3 = PITAttributeDefNameFinder.findBySourceId(attributeDefName3.getId(), true).iterator().next();
    assertNotNull(pitAttributeDefName3);
    
    // now verify what subj1 can see
    GrouperSession s = GrouperSession.start(member1.getSubject());
    
    try {
      pitAttributeDefName1 = PITAttributeDefNameFinder.findBySourceId(attributeDefName1.getId(), true).iterator().next();
      fail("Expected AttributeDefNameNotFoundException.");
    } catch (AttributeDefNameNotFoundException e) {
      // good
    }
    
    pitAttributeDefName2 = PITAttributeDefNameFinder.findBySourceId(attributeDefName2.getId(), true).iterator().next();
    assertNotNull(pitAttributeDefName2);
    
    try {
      pitAttributeDefName3 = PITAttributeDefNameFinder.findBySourceId(attributeDefName3.getId(), true).iterator().next();
      fail("Expected AttributeDefNameNotFoundException.");
    } catch (AttributeDefNameNotFoundException e) {
      // good
    }
    
    s.stop();
  }
  
  /**
   * 
   */
  public void testFindByName() {
    Member member1 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ1, true);
    
    edu.addChildGroup("bogus", "bogus");
    
    AttributeDef attributeDef1 = edu.addChildAttributeDef("test1", AttributeDefType.attr);
    AttributeDefName attributeDefName1 = edu.addChildAttributeDefName(attributeDef1, "test", "test");
    ChangeLogTempToEntity.convertRecords();
    attributeDefName1.delete();
    
    AttributeDef attributeDef2 = edu.addChildAttributeDef("test2", AttributeDefType.attr);
    AttributeDefName attributeDefName2 = edu.addChildAttributeDefName(attributeDef2, "test", "test");
    ChangeLogTempToEntity.convertRecords();
    attributeDef2.getPrivilegeDelegate().grantPriv(member1.getSubject(), AttributeDefPrivilege.ATTR_VIEW, true);
    attributeDefName2.delete();
    
    AttributeDef attributeDef3 = edu.addChildAttributeDef("test3", AttributeDefType.attr);
    AttributeDefName attributeDefName3 = edu.addChildAttributeDefName(attributeDef3, "test", "test");
    ChangeLogTempToEntity.convertRecords();
    attributeDef3.getPrivilegeDelegate().grantPriv(member1.getSubject(), AttributeDefPrivilege.ATTR_VIEW, true);
    ChangeLogTempToEntity.convertRecords();
    
    // root can see all 3
    Set<PITAttributeDefName> pitAttributeDefNames = PITAttributeDefNameFinder.findByName("edu:test", true, true);
    assertEquals(3, pitAttributeDefNames.size());
    
    // subj1 can only see the current active attribute def name
    GrouperSession s = GrouperSession.start(member1.getSubject());
    pitAttributeDefNames = PITAttributeDefNameFinder.findByName("edu:test", true, true);
    assertEquals(1, pitAttributeDefNames.size());
    assertEquals(attributeDefName3.getId(), pitAttributeDefNames.iterator().next().getSourceId());
    
    // revoke subj1 priv on attribute def 3
    s = GrouperSession.startRootSession();
    attributeDef3.getPrivilegeDelegate().revokePriv(member1.getSubject(), AttributeDefPrivilege.ATTR_VIEW, true);
    ChangeLogTempToEntity.convertRecords();

    // root can still see all 3
    pitAttributeDefNames = PITAttributeDefNameFinder.findByName("edu:test", true, true);
    assertEquals(3, pitAttributeDefNames.size());
    
    // subj1 can't see anything now
    s = GrouperSession.start(member1.getSubject());
    try {
      pitAttributeDefNames = PITAttributeDefNameFinder.findByName("edu:test", true, true);
      fail("Expected AttributeDefNameNotFoundException.");
    } catch (AttributeDefNameNotFoundException e) {
      // good
    }
    
    s.stop();
    
    // delete attribute def name 3
    s = GrouperSession.startRootSession();
    attributeDefName3.delete();
    ChangeLogTempToEntity.convertRecords();

    // root can still see all 3
    pitAttributeDefNames = PITAttributeDefNameFinder.findByName("edu:test", true, true);
    assertEquals(3, pitAttributeDefNames.size());
    
    // subj1 can't see anything still
    s = GrouperSession.start(member1.getSubject());
    try {
      pitAttributeDefNames = PITAttributeDefNameFinder.findByName("edu:test", true, true);
      fail("Expected AttributeDefNameNotFoundException.");
    } catch (AttributeDefNameNotFoundException e) {
      // good
    }
    
    s.stop();    
  }
  
  /**
   * 
   */
  public void testFindByNameInDateRange() {
    
    Timestamp beforeFirst = getTimestampWithSleep();
    AttributeDef attributeDef1 = edu.addChildAttributeDef("test", AttributeDefType.attr);
    AttributeDefName attributeDefName1 = edu.addChildAttributeDefName(attributeDef1, "test", "test");
    ChangeLogTempToEntity.convertRecords();

    attributeDefName1.delete();
    attributeDef1.delete();
    ChangeLogTempToEntity.convertRecords();

    Timestamp beforeSecond = getTimestampWithSleep();
    AttributeDef attributeDef2 = edu.addChildAttributeDef("test", AttributeDefType.attr);
    AttributeDefName attributeDefName2 = edu.addChildAttributeDefName(attributeDef2, "test", "test");
    ChangeLogTempToEntity.convertRecords();

    Timestamp afterSecond = getTimestampWithSleep();
    attributeDefName2.delete();
    attributeDef2.delete();
    ChangeLogTempToEntity.convertRecords();
    
    Timestamp beforeThird = getTimestampWithSleep();
    AttributeDef attributeDef3 = edu.addChildAttributeDef("test", AttributeDefType.attr);
    AttributeDefName attributeDefName3 = edu.addChildAttributeDefName(attributeDef3, "test", "test");
    ChangeLogTempToEntity.convertRecords();
    
    Timestamp afterThird = getTimestampWithSleep();
    
    try {
      PITAttributeDefNameFinder.findByName("edu:test", null, beforeFirst, true, true);
      fail("Expected AttributeDefNameNotFoundException.");
    } catch (AttributeDefNameNotFoundException e) {
      // good
    }
    
    Set<PITAttributeDefName> pitAttributeDefNames = PITAttributeDefNameFinder.findByName("edu:test", null, afterSecond, true, true);
    assertEquals(2, pitAttributeDefNames.size());
    Iterator<PITAttributeDefName> iterator = pitAttributeDefNames.iterator();
    assertEquals(attributeDefName1.getId(), iterator.next().getSourceId());
    assertEquals(attributeDefName2.getId(), iterator.next().getSourceId());
    
    pitAttributeDefNames = PITAttributeDefNameFinder.findByName("edu:test", beforeSecond, null, true, true);
    assertEquals(2, pitAttributeDefNames.size());
    iterator = pitAttributeDefNames.iterator();
    assertEquals(attributeDefName2.getId(), iterator.next().getSourceId());
    assertEquals(attributeDefName3.getId(), iterator.next().getSourceId());
    
    pitAttributeDefNames = PITAttributeDefNameFinder.findByName("edu:test", afterThird, null, true, true);
    assertEquals(1, pitAttributeDefNames.size());
    iterator = pitAttributeDefNames.iterator();
    assertEquals(attributeDefName3.getId(), iterator.next().getSourceId());
    
    pitAttributeDefNames = PITAttributeDefNameFinder.findByName("edu:test", beforeSecond, beforeThird, true, true);
    assertEquals(1, pitAttributeDefNames.size());
    iterator = pitAttributeDefNames.iterator();
    assertEquals(attributeDefName2.getId(), iterator.next().getSourceId());
  }
}