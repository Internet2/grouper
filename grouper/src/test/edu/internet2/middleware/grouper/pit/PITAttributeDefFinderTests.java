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
import edu.internet2.middleware.grouper.attr.AttributeDefType;
import edu.internet2.middleware.grouper.changeLog.ChangeLogTempToEntity;
import edu.internet2.middleware.grouper.exception.AttributeDefNotFoundException;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SessionHelper;
import edu.internet2.middleware.grouper.helper.StemHelper;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.pit.finder.PITAttributeDefFinder;
import edu.internet2.middleware.grouper.privs.AttributeDefPrivilege;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * @author shilen
 * $Id$
 */
public class PITAttributeDefFinderTests extends GrouperTest {
  
  /** top level stem */
  private Stem edu;

  /** root session */
  private GrouperSession grouperSession;
  
  /** root stem */
  private Stem root;
  
  /**
   * @param name
   */
  public PITAttributeDefFinderTests(String name) {
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
    attributeDef2.getPrivilegeDelegate().grantPriv(member1.getSubject(), AttributeDefPrivilege.ATTR_VIEW, true);
    attributeDef3.getPrivilegeDelegate().grantPriv(member1.getSubject(), AttributeDefPrivilege.ATTR_VIEW, true);
    ChangeLogTempToEntity.convertRecords();
    
    attributeDef3.delete();
    ChangeLogTempToEntity.convertRecords();

    PITAttributeDef pitAttributeDef1 = PITAttributeDefFinder.findById(attributeDef1.getId(), true);
    assertNotNull(pitAttributeDef1);
    
    PITAttributeDef pitAttributeDef2 = PITAttributeDefFinder.findById(attributeDef2.getId(), true);
    assertNotNull(pitAttributeDef2);
    
    PITAttributeDef pitAttributeDef3 = PITAttributeDefFinder.findById(attributeDef3.getId(), true);
    assertNotNull(pitAttributeDef3);
    
    // now verify what subj1 can see
    GrouperSession s = GrouperSession.start(member1.getSubject());
    
    try {
      pitAttributeDef1 = PITAttributeDefFinder.findById(attributeDef1.getId(), true);
      fail("Expected AttributeDefNotFoundException.");
    } catch (AttributeDefNotFoundException e) {
      // good
    }
    
    pitAttributeDef2 = PITAttributeDefFinder.findById(attributeDef2.getId(), true);
    assertNotNull(pitAttributeDef2);
    
    try {
      pitAttributeDef3 = PITAttributeDefFinder.findById(attributeDef3.getId(), true);
      fail("Expected AttributeDefNotFoundException.");
    } catch (AttributeDefNotFoundException e) {
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
    
    AttributeDef attributeDef1 = edu.addChildAttributeDef("test", AttributeDefType.attr);
    ChangeLogTempToEntity.convertRecords();
    attributeDef1.delete();
    
    AttributeDef attributeDef2 = edu.addChildAttributeDef("test", AttributeDefType.attr);
    ChangeLogTempToEntity.convertRecords();
    attributeDef2.getPrivilegeDelegate().grantPriv(member1.getSubject(), AttributeDefPrivilege.ATTR_VIEW, true);
    attributeDef2.delete();
    
    AttributeDef attributeDef3 = edu.addChildAttributeDef("test", AttributeDefType.attr);
    ChangeLogTempToEntity.convertRecords();
    attributeDef3.getPrivilegeDelegate().grantPriv(member1.getSubject(), AttributeDefPrivilege.ATTR_VIEW, true);
    ChangeLogTempToEntity.convertRecords();
    
    // root can see all 3
    Set<PITAttributeDef> pitAttributeDefs = PITAttributeDefFinder.findByName("edu:test", true, true);
    assertEquals(3, pitAttributeDefs.size());
    
    // subj1 can only see the current active attribute def
    GrouperSession s = GrouperSession.start(member1.getSubject());
    pitAttributeDefs = PITAttributeDefFinder.findByName("edu:test", true, true);
    assertEquals(1, pitAttributeDefs.size());
    assertEquals(attributeDef3.getId(), pitAttributeDefs.iterator().next().getId());
    
    // revoke subj1 priv on attribute def 3
    s = GrouperSession.startRootSession();
    attributeDef3.getPrivilegeDelegate().revokePriv(member1.getSubject(), AttributeDefPrivilege.ATTR_VIEW, true);
    ChangeLogTempToEntity.convertRecords();

    // root can still see all 3
    pitAttributeDefs = PITAttributeDefFinder.findByName("edu:test", true, true);
    assertEquals(3, pitAttributeDefs.size());
    
    // subj1 can't see anything now
    s = GrouperSession.start(member1.getSubject());
    try {
      pitAttributeDefs = PITAttributeDefFinder.findByName("edu:test", true, true);
      fail("Expected AttributeDefNotFoundException.");
    } catch (AttributeDefNotFoundException e) {
      // good
    }
    
    s.stop();
    
    // delete attribute def 3
    s = GrouperSession.startRootSession();
    attributeDef3.delete();
    ChangeLogTempToEntity.convertRecords();

    // root can still see all 3
    pitAttributeDefs = PITAttributeDefFinder.findByName("edu:test", true, true);
    assertEquals(3, pitAttributeDefs.size());
    
    // subj1 can't see anything still
    s = GrouperSession.start(member1.getSubject());
    try {
      pitAttributeDefs = PITAttributeDefFinder.findByName("edu:test", true, true);
      fail("Expected AttributeDefNotFoundException.");
    } catch (AttributeDefNotFoundException e) {
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
    ChangeLogTempToEntity.convertRecords();

    attributeDef1.delete();
    ChangeLogTempToEntity.convertRecords();

    Timestamp beforeSecond = getTimestampWithSleep();
    AttributeDef attributeDef2 = edu.addChildAttributeDef("test", AttributeDefType.attr);
    ChangeLogTempToEntity.convertRecords();

    Timestamp afterSecond = getTimestampWithSleep();
    attributeDef2.delete();
    ChangeLogTempToEntity.convertRecords();
    
    Timestamp beforeThird = getTimestampWithSleep();
    AttributeDef attributeDef3 = edu.addChildAttributeDef("test", AttributeDefType.attr);
    ChangeLogTempToEntity.convertRecords();
    
    Timestamp afterThird = getTimestampWithSleep();
    
    try {
      PITAttributeDefFinder.findByName("edu:test", null, beforeFirst, true, true);
      fail("Expected AttributeDefNotFoundException.");
    } catch (AttributeDefNotFoundException e) {
      // good
    }
    
    Set<PITAttributeDef> pitAttributeDefs = PITAttributeDefFinder.findByName("edu:test", null, afterSecond, true, true);
    assertEquals(2, pitAttributeDefs.size());
    Iterator<PITAttributeDef> iterator = pitAttributeDefs.iterator();
    assertEquals(attributeDef1.getId(), iterator.next().getId());
    assertEquals(attributeDef2.getId(), iterator.next().getId());
    
    pitAttributeDefs = PITAttributeDefFinder.findByName("edu:test", beforeSecond, null, true, true);
    assertEquals(2, pitAttributeDefs.size());
    iterator = pitAttributeDefs.iterator();
    assertEquals(attributeDef2.getId(), iterator.next().getId());
    assertEquals(attributeDef3.getId(), iterator.next().getId());
    
    pitAttributeDefs = PITAttributeDefFinder.findByName("edu:test", afterThird, null, true, true);
    assertEquals(1, pitAttributeDefs.size());
    iterator = pitAttributeDefs.iterator();
    assertEquals(attributeDef3.getId(), iterator.next().getId());
    
    pitAttributeDefs = PITAttributeDefFinder.findByName("edu:test", beforeSecond, beforeThird, true, true);
    assertEquals(1, pitAttributeDefs.size());
    iterator = pitAttributeDefs.iterator();
    assertEquals(attributeDef2.getId(), iterator.next().getId());
  }
}