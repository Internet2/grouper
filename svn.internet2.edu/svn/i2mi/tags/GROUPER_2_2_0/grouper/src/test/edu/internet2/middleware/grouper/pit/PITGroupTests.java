/*******************************************************************************
 * Copyright 2012 Internet2
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
 ******************************************************************************/
package edu.internet2.middleware.grouper.pit;

import java.sql.Timestamp;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;

import edu.internet2.middleware.grouper.FieldFinder;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.changeLog.ChangeLogTempToEntity;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SessionHelper;
import edu.internet2.middleware.grouper.helper.StemHelper;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.pit.finder.PITGroupFinder;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Source;

/**
 * @author shilen
 * $Id$
 */
public class PITGroupTests extends GrouperTest {
  
  /** top level stem */
  private Stem edu;

  /** root session */
  private GrouperSession grouperSession;
  
  /** root stem */
  private Stem root;
  
  /**
   * @param name
   */
  public PITGroupTests(String name) {
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
  public void testHasMemberAtPointInTime() {
    Member member1 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ1, true);
    Member member2 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ2, true);
    Member member3 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ3, true);
    Member member4 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ4, true);
    
    Group group1 = edu.addChildGroup("test1", "test1");
    group1.addMember(member1.getSubject());
    group1.addMember(member2.getSubject());
    ChangeLogTempToEntity.convertRecords();
    
    Timestamp beforeAll = getTimestampWithSleep();
    group1.deleteMember(member1);
    group1.revokePriv(AccessPrivilege.READ);
    ChangeLogTempToEntity.convertRecords();
    Timestamp afterFirst = getTimestampWithSleep();
    group1.deleteMember(member2);
    group1.grantPriv(member1.getSubject(), AccessPrivilege.READ);
    ChangeLogTempToEntity.convertRecords();
    Timestamp afterSecond = getTimestampWithSleep();
    
    group1.addMember(member3.getSubject());
    group1.addMember(member4.getSubject());
    ChangeLogTempToEntity.convertRecords();
    
    group1.deleteMember(member4);
    ChangeLogTempToEntity.convertRecords();
    
    PITGroup pitGroup1 = PITGroupFinder.findMostRecentByName("edu:test1", true);
    String membersfieldId = Group.getDefaultList().getUuid();
    String readersFieldId = FieldFinder.find("readers", true).getUuid();
    
    assertTrue(pitGroup1.hasMember(member1.getSubject(), membersfieldId, beforeAll, beforeAll, null));
    assertTrue(pitGroup1.hasMember(member2.getSubject(), membersfieldId, beforeAll, beforeAll, null));
    assertFalse(pitGroup1.hasMember(member3.getSubject(), membersfieldId, beforeAll, beforeAll, null));
    assertFalse(pitGroup1.hasMember(member4.getSubject(), membersfieldId, beforeAll, beforeAll, null));
    assertTrue(pitGroup1.hasMember(member1.getSubject(), readersFieldId, beforeAll, beforeAll, null));
 
    assertFalse(pitGroup1.hasMember(member1.getSubject(), membersfieldId, afterFirst, afterFirst, null));
    assertTrue(pitGroup1.hasMember(member2.getSubject(), membersfieldId, afterFirst, afterFirst, null));
    assertFalse(pitGroup1.hasMember(member3.getSubject(), membersfieldId, afterFirst, afterFirst, null));
    assertFalse(pitGroup1.hasMember(member4.getSubject(), membersfieldId, afterFirst, afterFirst, null));
    assertFalse(pitGroup1.hasMember(member1.getSubject(), readersFieldId, afterFirst, afterFirst, null));

    assertFalse(pitGroup1.hasMember(member1.getSubject(), membersfieldId, afterSecond, afterSecond, null));
    assertFalse(pitGroup1.hasMember(member2.getSubject(), membersfieldId, afterSecond, afterSecond, null));
    assertFalse(pitGroup1.hasMember(member3.getSubject(), membersfieldId, afterSecond, afterSecond, null));
    assertFalse(pitGroup1.hasMember(member4.getSubject(), membersfieldId, afterSecond, afterSecond, null));
    assertTrue(pitGroup1.hasMember(member1.getSubject(), readersFieldId, afterSecond, afterSecond, null));
  }
  
  /**
   * 
   */
  public void testHasMemberWithFromDate() {
    Member member1 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ1, true);
    Member member2 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ2, true);
    Member member3 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ3, true);
    
    Group group1 = edu.addChildGroup("test1", "test1");
    group1.addMember(member1.getSubject());
    group1.addMember(member2.getSubject());
    ChangeLogTempToEntity.convertRecords();
    
    Timestamp beforeAll = getTimestampWithSleep();
    group1.deleteMember(member1);
    ChangeLogTempToEntity.convertRecords();
    Timestamp afterFirst = getTimestampWithSleep();
    group1.deleteMember(member2);
    ChangeLogTempToEntity.convertRecords();
    Timestamp afterSecond = getTimestampWithSleep();
    
    group1.addMember(member3.getSubject());
    ChangeLogTempToEntity.convertRecords();
    
    PITGroup pitGroup1 = PITGroupFinder.findMostRecentByName("edu:test1", true);
    String membersfieldId = Group.getDefaultList().getUuid();

    assertTrue(pitGroup1.hasMember(member1.getSubject(), membersfieldId, beforeAll, null, null));
    assertTrue(pitGroup1.hasMember(member2.getSubject(), membersfieldId, beforeAll, null, null));
    assertTrue(pitGroup1.hasMember(member3.getSubject(), membersfieldId, beforeAll, null, null));

    assertFalse(pitGroup1.hasMember(member1.getSubject(), membersfieldId, afterFirst, null, null));
    assertTrue(pitGroup1.hasMember(member2.getSubject(), membersfieldId, afterFirst, null, null));
    assertTrue(pitGroup1.hasMember(member3.getSubject(), membersfieldId, afterFirst, null, null));
    
    assertFalse(pitGroup1.hasMember(member1.getSubject(), membersfieldId, afterSecond, null, null));
    assertFalse(pitGroup1.hasMember(member2.getSubject(), membersfieldId, afterSecond, null, null));
    assertTrue(pitGroup1.hasMember(member3.getSubject(), membersfieldId, afterSecond, null, null));
  }
  
  /**
   * 
   */
  public void testHasMemberWithToDate() {
    Member member1 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ1, true);
    Member member2 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ2, true);
    
    Group group1 = edu.addChildGroup("test1", "test1");
    Timestamp beforeAll = getTimestampWithSleep();
    group1.addMember(member1.getSubject());
    Timestamp afterFirst = getTimestampWithSleep();
    group1.addMember(member2.getSubject());
    Timestamp afterSecond = getTimestampWithSleep();
    ChangeLogTempToEntity.convertRecords();
    
    PITGroup pitGroup1 = PITGroupFinder.findMostRecentByName("edu:test1", true);
    String membersfieldId = Group.getDefaultList().getUuid();

    assertFalse(pitGroup1.hasMember(member1.getSubject(), membersfieldId, null, beforeAll, null));
    assertFalse(pitGroup1.hasMember(member2.getSubject(), membersfieldId, null, beforeAll, null));

    assertTrue(pitGroup1.hasMember(member1.getSubject(), membersfieldId, null, afterFirst, null));
    assertFalse(pitGroup1.hasMember(member2.getSubject(), membersfieldId, null, afterFirst, null));
    
    assertTrue(pitGroup1.hasMember(member1.getSubject(), membersfieldId, null, afterSecond, null));
    assertTrue(pitGroup1.hasMember(member2.getSubject(), membersfieldId, null, afterSecond, null));
    
    group1.delete();
    ChangeLogTempToEntity.convertRecords();

    pitGroup1 = PITGroupFinder.findMostRecentByName("edu:test1", true);

    assertFalse(pitGroup1.hasMember(member1.getSubject(), membersfieldId, null, beforeAll, null));
    assertFalse(pitGroup1.hasMember(member2.getSubject(), membersfieldId, null, beforeAll, null));

    assertTrue(pitGroup1.hasMember(member1.getSubject(), membersfieldId, null, afterFirst, null));
    assertFalse(pitGroup1.hasMember(member2.getSubject(), membersfieldId, null, afterFirst, null));
    
    assertTrue(pitGroup1.hasMember(member1.getSubject(), membersfieldId, null, afterSecond, null));
    assertTrue(pitGroup1.hasMember(member2.getSubject(), membersfieldId, null, afterSecond, null));
  }
  
  /**
   * 
   */
  public void testGetMembersAtPointInTime() {
    Member member1 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ1, true);
    Member member2 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ2, true);
    Member member3 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ3, true);
    Member member4 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ4, true);
    
    Group group1 = edu.addChildGroup("test1", "test1");
    group1.addMember(member1.getSubject());
    group1.addMember(member2.getSubject());
    ChangeLogTempToEntity.convertRecords();
    
    Timestamp beforeAll = getTimestampWithSleep();
    group1.deleteMember(member1);
    ChangeLogTempToEntity.convertRecords();
    Timestamp afterFirst = getTimestampWithSleep();
    group1.deleteMember(member2);
    ChangeLogTempToEntity.convertRecords();
    Timestamp afterSecond = getTimestampWithSleep();
    
    group1.addMember(member3.getSubject());
    group1.addMember(member4.getSubject());
    ChangeLogTempToEntity.convertRecords();
    
    group1.deleteMember(member4);
    ChangeLogTempToEntity.convertRecords();
    
    PITGroup pitGroup1 = PITGroupFinder.findMostRecentByName("edu:test1", true);
    
    Set<Member> members = pitGroup1.getMembers(Group.getDefaultList().getUuid(), beforeAll, beforeAll, null, null);
    assertEquals(2, members.size());
    assertTrue(members.contains(member1));
    assertTrue(members.contains(member2));
    
    members = pitGroup1.getMembers(Group.getDefaultList().getUuid(), afterFirst, afterFirst, null, null);
    assertEquals(1, members.size());
    assertTrue(members.contains(member2));
    
    members = pitGroup1.getMembers(Group.getDefaultList().getUuid(), afterSecond, afterSecond, null, null);
    assertEquals(0, members.size());
  }
  
  /**
   * 
   */
  public void testGetMembersWithFromDate() {
    Member member1 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ1, true);
    Member member2 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ2, true);
    Member member3 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ3, true);
    
    Group group1 = edu.addChildGroup("test1", "test1");
    group1.addMember(member1.getSubject());
    group1.addMember(member2.getSubject());
    ChangeLogTempToEntity.convertRecords();
    
    Timestamp beforeAll = getTimestampWithSleep();
    group1.deleteMember(member1);
    ChangeLogTempToEntity.convertRecords();
    Timestamp afterFirst = getTimestampWithSleep();
    group1.deleteMember(member2);
    ChangeLogTempToEntity.convertRecords();
    Timestamp afterSecond = getTimestampWithSleep();
    
    group1.addMember(member3.getSubject());
    ChangeLogTempToEntity.convertRecords();
    
    PITGroup pitGroup1 = PITGroupFinder.findMostRecentByName("edu:test1", true);
    
    Set<Member> members = pitGroup1.getMembers(Group.getDefaultList().getUuid(), beforeAll, null, null, null);
    assertEquals(3, members.size());

    members = pitGroup1.getMembers(Group.getDefaultList().getUuid(), afterFirst, null, null, null);
    assertEquals(2, members.size());
    assertFalse(members.contains(member1));
    assertTrue(members.contains(member2));
    assertTrue(members.contains(member3));
    
    members = pitGroup1.getMembers(Group.getDefaultList().getUuid(), afterSecond, null, null, null);
    assertEquals(1, members.size());
    assertEquals(member3.getUuid(), members.iterator().next().getUuid());
  }
  
  /**
   * 
   */
  public void testGetMembersWithToDate() {
    Member member1 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ1, true);
    Member member2 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ2, true);
    
    Group group1 = edu.addChildGroup("test1", "test1");
    Timestamp beforeAll = getTimestampWithSleep();
    group1.addMember(member1.getSubject());
    Timestamp afterFirst = getTimestampWithSleep();
    group1.addMember(member2.getSubject());
    Timestamp afterSecond = getTimestampWithSleep();
    ChangeLogTempToEntity.convertRecords();
    
    PITGroup pitGroup1 = PITGroupFinder.findMostRecentByName("edu:test1", true);
    
    Set<Member> members = pitGroup1.getMembers(Group.getDefaultList().getUuid(), null, beforeAll, null, null);
    assertEquals(0, members.size());

    members = pitGroup1.getMembers(Group.getDefaultList().getUuid(), null, afterFirst, null, null);
    assertEquals(1, members.size());
    assertEquals(member1.getUuid(), members.iterator().next().getUuid());
    
    members = pitGroup1.getMembers(Group.getDefaultList().getUuid(), null, afterSecond, null, null);
    assertEquals(2, members.size());
    
    group1.delete();
    ChangeLogTempToEntity.convertRecords();

    pitGroup1 = PITGroupFinder.findMostRecentByName("edu:test1", true);

    members = pitGroup1.getMembers(Group.getDefaultList().getUuid(), null, beforeAll, null, null);
    assertEquals(0, members.size());

    members = pitGroup1.getMembers(Group.getDefaultList().getUuid(), null, afterFirst, null, null);
    assertEquals(1, members.size());
    assertEquals(member1.getUuid(), members.iterator().next().getUuid());
    
    members = pitGroup1.getMembers(Group.getDefaultList().getUuid(), null, afterSecond, null, null);
    assertEquals(2, members.size());
  }
  
  /**
   * 
   */
  public void testGetMembersPrivs() {
    Member member0 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ0, true);
    Member member1 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ1, true);
    Member member2 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ2, true);
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.create.grant.all.read", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.create.grant.all.view", "false");
    
    Group group1 = edu.addChildGroup("test1", "test1");
    Group group2 = edu.addChildGroup("test2", "test2");
    group1.grantPriv(member0.getSubject(), AccessPrivilege.VIEW);
    group2.grantPriv(member0.getSubject(), AccessPrivilege.READ);
    group1.addMember(member1.getSubject());
    group2.addMember(member1.getSubject());
    group2.addMember(member2.getSubject());
    ChangeLogTempToEntity.convertRecords();
    
    PITGroup pitGroup1 = PITGroupFinder.findMostRecentByName("edu:test1", true);
    PITGroup pitGroup2 = PITGroupFinder.findMostRecentByName("edu:test2", true);
    
    // root should be able to read members
    assertEquals(1, pitGroup1.getMembers(Group.getDefaultList().getUuid(), null, null, null, null).size());
    assertEquals(2, pitGroup2.getMembers(Group.getDefaultList().getUuid(), null, null, null, null).size());
    
    // subj0 should be able to read members of group2 only
    GrouperSession s = GrouperSession.start(member0.getSubject());
    assertEquals(0, pitGroup1.getMembers(Group.getDefaultList().getUuid(), null, null, null, null).size());
    assertEquals(2, pitGroup2.getMembers(Group.getDefaultList().getUuid(), null, null, null, null).size());
    s.stop();
    
    // delete the groups now
    s = GrouperSession.startRootSession();
    group1.delete();
    group2.delete();
    ChangeLogTempToEntity.convertRecords();

    pitGroup1 = PITGroupFinder.findMostRecentByName("edu:test1", true);
    pitGroup2 = PITGroupFinder.findMostRecentByName("edu:test2", true);
    
    // root should be able to read members
    assertEquals(1, pitGroup1.getMembers(Group.getDefaultList().getUuid(), null, null, null, null).size());
    assertEquals(2, pitGroup2.getMembers(Group.getDefaultList().getUuid(), null, null, null, null).size());
    
    // subj0 should not be able to read members
    s = GrouperSession.start(member0.getSubject());
    assertEquals(0, pitGroup1.getMembers(Group.getDefaultList().getUuid(), null, null, null, null).size());
    assertEquals(0, pitGroup2.getMembers(Group.getDefaultList().getUuid(), null, null, null, null).size());
    s.stop();
  }
  
  /**
   * 
   */
  public void testGetMembersWithSources() {
    Member member1 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ1, true);
    Member member2 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ2, true);
    
    Group group1 = edu.addChildGroup("test1", "test1");
    Group group2 = edu.addChildGroup("test2", "test2");

    group1.addMember(member1.getSubject());
    group2.addMember(member2.getSubject());
    group1.addMember(group2.toSubject());
    ChangeLogTempToEntity.convertRecords();
    
    PITGroup pitGroup1 = PITGroupFinder.findMostRecentByName("edu:test1", true);
    
    assertEquals(3, pitGroup1.getMembers(Group.getDefaultList().getUuid(), null, null, null, null).size());

    Set<Source> sources = new LinkedHashSet<Source>();
    sources.add(member1.getSubjectSource());
    assertEquals(2, pitGroup1.getMembers(Group.getDefaultList().getUuid(), null, null, sources, null).size());

    sources = new LinkedHashSet<Source>();
    sources.add(group2.toSubject().getSource());
    assertEquals(1, pitGroup1.getMembers(Group.getDefaultList().getUuid(), null, null, sources, null).size());
  }
}
