package edu.internet2.middleware.grouper.pit;

import java.util.Set;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.cfg.ApiConfig;
import edu.internet2.middleware.grouper.changeLog.ChangeLogTempToEntity;
import edu.internet2.middleware.grouper.exception.GroupNotFoundException;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SessionHelper;
import edu.internet2.middleware.grouper.helper.StemHelper;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.pit.finder.PITGroupFinder;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;

/**
 * @author shilen
 * $Id$
 */
public class PITGroupFinderTests extends GrouperTest {
  
  /** top level stem */
  private Stem edu;

  /** root session */
  private GrouperSession grouperSession;
  
  /** root stem */
  private Stem root;
  
  /**
   * @param name
   */
  public PITGroupFinderTests(String name) {
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
   */
  public void testFindById() {
    Member member1 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ1, true);
    ApiConfig.testConfig.put("groups.create.grant.all.read", "false");
    ApiConfig.testConfig.put("groups.create.grant.all.view", "false");
    
    Group group1 = edu.addChildGroup("test1", "test1");
    Group group2 = edu.addChildGroup("test2", "test2");
    Group group3 = edu.addChildGroup("test3", "test3");
    group2.grantPriv(member1.getSubject(), AccessPrivilege.VIEW);
    group3.grantPriv(member1.getSubject(), AccessPrivilege.VIEW);
    ChangeLogTempToEntity.convertRecords();
    
    group3.delete();
    ChangeLogTempToEntity.convertRecords();

    PITGroup pitGroup1 = PITGroupFinder.findById(group1.getId(), true);
    assertNotNull(pitGroup1);
    
    PITGroup pitGroup2 = PITGroupFinder.findById(group2.getId(), true);
    assertNotNull(pitGroup2);
    
    PITGroup pitGroup3 = PITGroupFinder.findById(group3.getId(), true);
    assertNotNull(pitGroup3);
    
    // now verify what subj1 can see
    GrouperSession s = GrouperSession.start(member1.getSubject());
    
    try {
      pitGroup1 = PITGroupFinder.findById(group1.getId(), true);
      fail("Expected GroupNotFoundException.");
    } catch (GroupNotFoundException e) {
      // good
    }
    
    pitGroup2 = PITGroupFinder.findById(group2.getId(), true);
    assertNotNull(pitGroup2);
    
    try {
      pitGroup3 = PITGroupFinder.findById(group3.getId(), true);
      fail("Expected GroupNotFoundException.");
    } catch (GroupNotFoundException e) {
      // good
    }
    
    s.stop();
  }
  
  /**
   * 
   */
  public void testFindByName() {
    Member member1 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ1, true);
    ApiConfig.testConfig.put("groups.create.grant.all.read", "false");
    ApiConfig.testConfig.put("groups.create.grant.all.view", "false");
    
    edu.addChildGroup("bogus", "bogus");
    
    Group group1 = edu.addChildGroup("test", "test");
    ChangeLogTempToEntity.convertRecords();
    group1.delete();
    
    Group group2 = edu.addChildGroup("test", "test");
    ChangeLogTempToEntity.convertRecords();
    group2.grantPriv(member1.getSubject(), AccessPrivilege.VIEW);
    group2.delete();
    
    Group group3 = edu.addChildGroup("test", "test");
    ChangeLogTempToEntity.convertRecords();
    group3.grantPriv(member1.getSubject(), AccessPrivilege.VIEW);
    ChangeLogTempToEntity.convertRecords();
    
    // root can see all 3 groups
    Set<PITGroup> pitGroups = PITGroupFinder.findByName("edu:test", true);
    assertEquals(3, pitGroups.size());
    
    PITGroup pitGroup = PITGroupFinder.findMostRecentByName("edu:test", true);
    assertNotNull(pitGroup);
    assertEquals(group3.getId(), pitGroup.getId());
    
    // subj1 can only see the current active group
    GrouperSession s = GrouperSession.start(member1.getSubject());
    pitGroups = PITGroupFinder.findByName("edu:test", true);
    assertEquals(1, pitGroups.size());
    assertEquals(group3.getId(), pitGroups.iterator().next().getId());
    
    pitGroup = PITGroupFinder.findMostRecentByName("edu:test", true);
    assertNotNull(pitGroup);
    assertEquals(group3.getId(), pitGroup.getId());
    s.stop();
    
    // revoke subj1 priv on group3
    s = GrouperSession.startRootSession();
    group3.revokePriv(member1.getSubject(), AccessPrivilege.VIEW);
    ChangeLogTempToEntity.convertRecords();

    // root can still see all 3 groups
    pitGroups = PITGroupFinder.findByName("edu:test", true);
    assertEquals(3, pitGroups.size());
    
    pitGroup = PITGroupFinder.findMostRecentByName("edu:test", true);
    assertNotNull(pitGroup);
    assertEquals(group3.getId(), pitGroup.getId());
    
    // subj1 can't see anything now
    s = GrouperSession.start(member1.getSubject());
    try {
      pitGroups = PITGroupFinder.findByName("edu:test", true);
      fail("Expected GroupNotFoundException.");
    } catch (GroupNotFoundException e) {
      // good
    }
    
    try {
      pitGroup = PITGroupFinder.findMostRecentByName("edu:test", true);
      fail("Expected GroupNotFoundException.");
    } catch (GroupNotFoundException e) {
      // good
    }
    
    s.stop();
    
    // delete group3
    s = GrouperSession.startRootSession();
    group3.delete();
    ChangeLogTempToEntity.convertRecords();

    // root can still see all 3 groups
    pitGroups = PITGroupFinder.findByName("edu:test", true);
    assertEquals(3, pitGroups.size());
    
    pitGroup = PITGroupFinder.findMostRecentByName("edu:test", true);
    assertNotNull(pitGroup);
    assertEquals(group3.getId(), pitGroup.getId());
    
    // subj1 can't see anything still
    s = GrouperSession.start(member1.getSubject());
    try {
      pitGroups = PITGroupFinder.findByName("edu:test", true);
      fail("Expected GroupNotFoundException.");
    } catch (GroupNotFoundException e) {
      // good
    }
    
    try {
      pitGroup = PITGroupFinder.findMostRecentByName("edu:test", true);
      fail("Expected GroupNotFoundException.");
    } catch (GroupNotFoundException e) {
      // good
    }
    
    s.stop();    
  }
}
