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
import java.util.Iterator;
import java.util.Set;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.changeLog.ChangeLogTempToEntity;
import edu.internet2.middleware.grouper.exception.GroupNotFoundException;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SessionHelper;
import edu.internet2.middleware.grouper.helper.StemHelper;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.pit.finder.PITGroupFinder;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.util.GrouperUtil;

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
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.create.grant.all.read", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.create.grant.all.view", "false");
    
    Group group1 = edu.addChildGroup("test1", "test1");
    Group group2 = edu.addChildGroup("test2", "test2");
    Group group3 = edu.addChildGroup("test3", "test3");
    group2.grantPriv(member1.getSubject(), AccessPrivilege.VIEW);
    group3.grantPriv(member1.getSubject(), AccessPrivilege.VIEW);
    ChangeLogTempToEntity.convertRecords();
    
    group3.delete();
    ChangeLogTempToEntity.convertRecords();

    PITGroup pitGroup1 = PITGroupFinder.findBySourceId(group1.getId(), true).iterator().next();
    assertNotNull(pitGroup1);
    
    PITGroup pitGroup2 = PITGroupFinder.findBySourceId(group2.getId(), true).iterator().next();
    assertNotNull(pitGroup2);
    
    PITGroup pitGroup3 = PITGroupFinder.findBySourceId(group3.getId(), true).iterator().next();
    assertNotNull(pitGroup3);
    
    // now verify what subj1 can see
    GrouperSession s = GrouperSession.start(member1.getSubject());
    
    try {
      pitGroup1 = PITGroupFinder.findBySourceId(group1.getId(), true).iterator().next();
      fail("Expected GroupNotFoundException.");
    } catch (GroupNotFoundException e) {
      // good
    }
    
    pitGroup2 = PITGroupFinder.findBySourceId(group2.getId(), true).iterator().next();
    assertNotNull(pitGroup2);
    
    try {
      pitGroup3 = PITGroupFinder.findBySourceId(group3.getId(), true).iterator().next();
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
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.create.grant.all.read", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.create.grant.all.view", "false");
    
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
    Set<PITGroup> pitGroups = PITGroupFinder.findByName("edu:test", true, true);
    assertEquals(3, pitGroups.size());
    
    PITGroup pitGroup = PITGroupFinder.findMostRecentByName("edu:test", true);
    assertNotNull(pitGroup);
    assertEquals(group3.getId(), pitGroup.getSourceId());
    
    // subj1 can only see the current active group
    GrouperSession s = GrouperSession.start(member1.getSubject());
    pitGroups = PITGroupFinder.findByName("edu:test", true, true);
    assertEquals(1, pitGroups.size());
    assertEquals(group3.getId(), pitGroups.iterator().next().getSourceId());
    
    pitGroup = PITGroupFinder.findMostRecentByName("edu:test", true);
    assertNotNull(pitGroup);
    assertEquals(group3.getId(), pitGroup.getSourceId());
    s.stop();
    
    // revoke subj1 priv on group3
    s = GrouperSession.startRootSession();
    group3.revokePriv(member1.getSubject(), AccessPrivilege.VIEW);
    ChangeLogTempToEntity.convertRecords();

    // root can still see all 3 groups
    pitGroups = PITGroupFinder.findByName("edu:test", true, true);
    assertEquals(3, pitGroups.size());
    
    pitGroup = PITGroupFinder.findMostRecentByName("edu:test", true);
    assertNotNull(pitGroup);
    assertEquals(group3.getId(), pitGroup.getSourceId());
    
    // subj1 can't see anything now
    s = GrouperSession.start(member1.getSubject());
    try {
      pitGroups = PITGroupFinder.findByName("edu:test", true, true);
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
    pitGroups = PITGroupFinder.findByName("edu:test", true, true);
    assertEquals(3, pitGroups.size());
    
    pitGroup = PITGroupFinder.findMostRecentByName("edu:test", true);
    assertNotNull(pitGroup);
    assertEquals(group3.getId(), pitGroup.getSourceId());
    
    // subj1 can't see anything still
    s = GrouperSession.start(member1.getSubject());
    try {
      pitGroups = PITGroupFinder.findByName("edu:test", true, true);
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
  
  /**
   * 
   */
  public void testFindByNameInDateRange() {
    
    Timestamp beforeFirst = getTimestampWithSleep();
    Group group1 = edu.addChildGroup("test", "test");
    ChangeLogTempToEntity.convertRecords();

    group1.delete();
    ChangeLogTempToEntity.convertRecords();

    Timestamp beforeSecond = getTimestampWithSleep();
    Group group2 = edu.addChildGroup("test", "test");
    ChangeLogTempToEntity.convertRecords();

    Timestamp afterSecond = getTimestampWithSleep();
    group2.delete();
    ChangeLogTempToEntity.convertRecords();
    
    Timestamp beforeThird = getTimestampWithSleep();
    Group group3 = edu.addChildGroup("test", "test");
    ChangeLogTempToEntity.convertRecords();
    
    Timestamp afterThird = getTimestampWithSleep();
    
    try {
      PITGroupFinder.findByName("edu:test", null, beforeFirst, true, true);
      fail("Expected GroupNotFoundException.");
    } catch (GroupNotFoundException e) {
      // good
    }
    
    Set<PITGroup> pitGroups = PITGroupFinder.findByName("edu:test", null, afterSecond, true, true);
    assertEquals(2, pitGroups.size());
    Iterator<PITGroup> iterator = pitGroups.iterator();
    assertEquals(group1.getId(), iterator.next().getSourceId());
    assertEquals(group2.getId(), iterator.next().getSourceId());
    
    pitGroups = PITGroupFinder.findByName("edu:test", beforeSecond, null, true, true);
    assertEquals(2, pitGroups.size());
    iterator = pitGroups.iterator();
    assertEquals(group2.getId(), iterator.next().getSourceId());
    assertEquals(group3.getId(), iterator.next().getSourceId());
    
    pitGroups = PITGroupFinder.findByName("edu:test", afterThird, null, true, true);
    assertEquals(1, pitGroups.size());
    iterator = pitGroups.iterator();
    assertEquals(group3.getId(), iterator.next().getSourceId());
    
    pitGroups = PITGroupFinder.findByName("edu:test", beforeSecond, beforeThird, true, true);
    assertEquals(1, pitGroups.size());
    iterator = pitGroups.iterator();
    assertEquals(group2.getId(), iterator.next().getSourceId());
  }
}
