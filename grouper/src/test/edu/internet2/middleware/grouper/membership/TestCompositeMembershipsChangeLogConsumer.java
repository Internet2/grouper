/**
 * Copyright 2020 Internet2
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
package edu.internet2.middleware.grouper.membership;

import java.sql.Timestamp;
import java.util.Date;

import edu.internet2.middleware.grouper.Composite;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.MembershipFinder;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.app.loader.GrouperLoader;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.changeLog.ChangeLogTempToEntity;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.misc.CompositeType;
import edu.internet2.middleware.grouper.misc.FindBadMemberships;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import junit.textui.TestRunner;

/**
 * @author shilen
 */
public class TestCompositeMembershipsChangeLogConsumer extends GrouperTest {

  public static void main(String[] args) throws Exception {
    TestRunner.run(new TestCompositeMembershipsChangeLogConsumer("testDeleteAndAddComposite"));
  }
  
  /**
   * @param name
   */
  public TestCompositeMembershipsChangeLogConsumer(String name) {
    super(name);
  }
  
  GrouperSession grouperSession = null;
  @Override
  protected void setUp() {
    super.setUp();
    grouperSession = GrouperSession.startRootSession();
  }

  
  
  @Override
  protected void tearDown() {
    GrouperSession.stopQuietly(this.grouperSession);
    super.tearDown();
  }

  /**
   * 
   */
  public void testMissingIntersectionComposite() throws Exception {
    Stem rootStem = StemFinder.findRootStem(GrouperSession.staticGrouperSession());
    Stem testStem = rootStem.addChildStem("testStem", "testStem");
    Group ownerGroup = testStem.addChildGroup("ownerGroup", "ownerGroup");
    Group ownerGroup2 = testStem.addChildGroup("ownerGroup2", "ownerGroup2");
    Group leftGroup = testStem.addChildGroup("leftGroup", "leftGroup");
    Group rightGroup = testStem.addChildGroup("rightGroup", "rightGroup");
    Group bogusGroup = testStem.addChildGroup("bogusGroup", "bogusGroup");
    
    Group parentOwnerGroup = testStem.addChildGroup("parentOwnerGroup", "parentOwnerGroup");
    Group parentLeftGroup = testStem.addChildGroup("parentLeftGroup", "parentLeftGroup");
    
    // clear change log
    ChangeLogTempToEntity.convertRecords();
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_consumer_compositeMemberships", false);
    
    ownerGroup.addCompositeMember(CompositeType.INTERSECTION, leftGroup, rightGroup);
    ownerGroup2.addCompositeMember(CompositeType.INTERSECTION, leftGroup, rightGroup);
    parentOwnerGroup.addCompositeMember(CompositeType.INTERSECTION, parentLeftGroup, ownerGroup);
    
    ChangeLogTempToEntity.convertRecords();
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_consumer_compositeMemberships", false);
    
    parentLeftGroup.addMember(SubjectTestHelper.SUBJ0);
    parentLeftGroup.addMember(SubjectTestHelper.SUBJ1);
    parentLeftGroup.addMember(SubjectTestHelper.SUBJ2);
    
    bogusGroup.addMember(SubjectTestHelper.SUBJ3);  // to get a record in grouper_members
   
    leftGroup.addMember(SubjectTestHelper.SUBJ0);
    leftGroup.addMember(SubjectTestHelper.SUBJ1);
    leftGroup.addMember(SubjectTestHelper.SUBJ2);
    leftGroup.addMember(SubjectTestHelper.SUBJ3);
    
    rightGroup.addMember(SubjectTestHelper.SUBJ0);
    rightGroup.addMember(SubjectTestHelper.SUBJ1);
    rightGroup.addMember(SubjectTestHelper.SUBJ2);
    rightGroup.addMember(SubjectTestHelper.SUBJ3);
    
    // confirm issue
    assertEquals(8, FindBadMemberships.checkAll());  // missing memberships in ownerGroup and ownerGroup2
    
    ChangeLogTempToEntity.convertRecords();
    
    // still issue
    assertEquals(8, FindBadMemberships.checkAll());  // subj0-3 for ownerGroup and ownerGroup2
    
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_consumer_compositeMemberships", false);

    // now parent group still needs to be fixed
    assertEquals(3, FindBadMemberships.checkAll());  // subj0-2 for parentOwnerGroup
    
    ChangeLogTempToEntity.convertRecords();
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_consumer_compositeMemberships", false);

    // now good
    assertEquals(0, FindBadMemberships.checkAll());
  }
  
  /**
   * 
   */
  public void testExtraIntersectionComposite() throws Exception {
    Stem rootStem = StemFinder.findRootStem(GrouperSession.staticGrouperSession());
    Stem testStem = rootStem.addChildStem("testStem", "testStem");
    Group ownerGroup = testStem.addChildGroup("ownerGroup", "ownerGroup");
    Group ownerGroup2 = testStem.addChildGroup("ownerGroup2", "ownerGroup2");
    Group leftGroup = testStem.addChildGroup("leftGroup", "leftGroup");
    Group rightGroup = testStem.addChildGroup("rightGroup", "rightGroup");
    
    Group parentOwnerGroup = testStem.addChildGroup("parentOwnerGroup", "parentOwnerGroup");
    Group parentLeftGroup = testStem.addChildGroup("parentLeftGroup", "parentLeftGroup");
    
    // clear change log
    ChangeLogTempToEntity.convertRecords();
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_consumer_compositeMemberships", false);
    
    ownerGroup.addCompositeMember(CompositeType.INTERSECTION, leftGroup, rightGroup);
    ownerGroup2.addCompositeMember(CompositeType.INTERSECTION, leftGroup, rightGroup);
    parentOwnerGroup.addCompositeMember(CompositeType.INTERSECTION, parentLeftGroup, ownerGroup);
    parentLeftGroup.addMember(SubjectTestHelper.SUBJ0);
    parentLeftGroup.addMember(SubjectTestHelper.SUBJ1);
    parentLeftGroup.addMember(SubjectTestHelper.SUBJ2);
    
    rightGroup.addMember(SubjectTestHelper.SUBJ0);
    rightGroup.addMember(SubjectTestHelper.SUBJ1);
    rightGroup.addMember(SubjectTestHelper.SUBJ2);
    rightGroup.addMember(SubjectTestHelper.SUBJ3);
    
    leftGroup.addMember(SubjectTestHelper.SUBJ0);
    leftGroup.addMember(SubjectTestHelper.SUBJ1);
    leftGroup.addMember(SubjectTestHelper.SUBJ2);
    leftGroup.addMember(SubjectTestHelper.SUBJ3);
    
    ChangeLogTempToEntity.convertRecords();
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_consumer_compositeMemberships", false);
    
    // remove members now
    rightGroup.deleteMember(SubjectTestHelper.SUBJ0);
    rightGroup.deleteMember(SubjectTestHelper.SUBJ1);
    rightGroup.deleteMember(SubjectTestHelper.SUBJ2);
    rightGroup.deleteMember(SubjectTestHelper.SUBJ3);
    
    // confirm issue
    assertEquals(8, FindBadMemberships.checkAll());  // subj0-3 for ownerGroup and ownerGroup2
    
    ChangeLogTempToEntity.convertRecords();
    
    // still issue
    assertEquals(8, FindBadMemberships.checkAll());  // subj0-3 for ownerGroup and ownerGroup2
    
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_consumer_compositeMemberships", false);

    // now parent group still needs to be fixed
    assertEquals(3, FindBadMemberships.checkAll());  // subj0-2 for parentOwnerGroup
    
    ChangeLogTempToEntity.convertRecords();
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_consumer_compositeMemberships", false);

    // now good
    assertEquals(0, FindBadMemberships.checkAll());
  }
  
  /**
   * 
   */
  public void testExtraComplementComposite() throws Exception {
    Stem rootStem = StemFinder.findRootStem(GrouperSession.staticGrouperSession());
    Stem testStem = rootStem.addChildStem("testStem", "testStem");
    Group ownerGroup = testStem.addChildGroup("ownerGroup", "ownerGroup");
    Group ownerGroup2 = testStem.addChildGroup("ownerGroup2", "ownerGroup2");
    Group leftGroup = testStem.addChildGroup("leftGroup", "leftGroup");
    Group rightGroup = testStem.addChildGroup("rightGroup", "rightGroup");
    
    Group parentOwnerGroup = testStem.addChildGroup("parentOwnerGroup", "parentOwnerGroup");
    Group parentLeftGroup = testStem.addChildGroup("parentLeftGroup", "parentLeftGroup");
    
    // clear change log
    ChangeLogTempToEntity.convertRecords();
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_consumer_compositeMemberships", false);
    
    ownerGroup.addCompositeMember(CompositeType.COMPLEMENT, leftGroup, rightGroup);
    ownerGroup2.addCompositeMember(CompositeType.COMPLEMENT, leftGroup, rightGroup);
    parentOwnerGroup.addCompositeMember(CompositeType.COMPLEMENT, parentLeftGroup, ownerGroup);
    
    parentLeftGroup.addMember(SubjectTestHelper.SUBJ0);
    parentLeftGroup.addMember(SubjectTestHelper.SUBJ1);
    parentLeftGroup.addMember(SubjectTestHelper.SUBJ2);
    
    leftGroup.addMember(SubjectTestHelper.SUBJ0);
    leftGroup.addMember(SubjectTestHelper.SUBJ1);
    leftGroup.addMember(SubjectTestHelper.SUBJ2);
    leftGroup.addMember(SubjectTestHelper.SUBJ3);
    
    ChangeLogTempToEntity.convertRecords();
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_consumer_compositeMemberships", false);
    
    rightGroup.addMember(SubjectTestHelper.SUBJ0);
    rightGroup.addMember(SubjectTestHelper.SUBJ1);
    rightGroup.addMember(SubjectTestHelper.SUBJ2);
    rightGroup.addMember(SubjectTestHelper.SUBJ3);
    
    // confirm issue
    assertEquals(8, FindBadMemberships.checkAll());  // subj0-3 for ownerGroup and ownerGroup2
    
    ChangeLogTempToEntity.convertRecords();
    
    // still issue
    assertEquals(8, FindBadMemberships.checkAll());  // subj0-3 for ownerGroup and ownerGroup2
    
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_consumer_compositeMemberships", false);

    // now parent group still needs to be fixed
    assertEquals(3, FindBadMemberships.checkAll());  // subj0-2 for parentOwnerGroup
    
    ChangeLogTempToEntity.convertRecords();
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_consumer_compositeMemberships", false);

    // now good
    assertEquals(0, FindBadMemberships.checkAll());
  }
  
  /**
   * 
   */
  public void testMissingComplementComposite() throws Exception {
    Stem rootStem = StemFinder.findRootStem(GrouperSession.staticGrouperSession());
    Stem testStem = rootStem.addChildStem("testStem", "testStem");
    Group ownerGroup = testStem.addChildGroup("ownerGroup", "ownerGroup");
    Group ownerGroup2 = testStem.addChildGroup("ownerGroup2", "ownerGroup2");
    Group leftGroup = testStem.addChildGroup("leftGroup", "leftGroup");
    Group rightGroup = testStem.addChildGroup("rightGroup", "rightGroup");
    Group bogusGroup = testStem.addChildGroup("bogusGroup", "bogusGroup");
    
    Group parentOwnerGroup = testStem.addChildGroup("parentOwnerGroup", "parentOwnerGroup");
    Group parentLeftGroup = testStem.addChildGroup("parentLeftGroup", "parentLeftGroup");
    
    // clear change log
    ChangeLogTempToEntity.convertRecords();
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_consumer_compositeMemberships", false);
    
    ownerGroup.addCompositeMember(CompositeType.COMPLEMENT, leftGroup, rightGroup);
    ownerGroup2.addCompositeMember(CompositeType.COMPLEMENT, leftGroup, rightGroup);
    parentOwnerGroup.addCompositeMember(CompositeType.COMPLEMENT, parentLeftGroup, ownerGroup);
    
    parentLeftGroup.addMember(SubjectTestHelper.SUBJ0);
    parentLeftGroup.addMember(SubjectTestHelper.SUBJ1);
    parentLeftGroup.addMember(SubjectTestHelper.SUBJ2);
    
    bogusGroup.addMember(SubjectTestHelper.SUBJ3);  // to get a record in grouper_members
    
    ChangeLogTempToEntity.convertRecords();
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_consumer_compositeMemberships", false);
    assertEquals(0, FindBadMemberships.checkAll());
    
    leftGroup.addMember(SubjectTestHelper.SUBJ0);
    leftGroup.addMember(SubjectTestHelper.SUBJ1);
    leftGroup.addMember(SubjectTestHelper.SUBJ2);
    leftGroup.addMember(SubjectTestHelper.SUBJ3);
    
    // confirm first issue
    assertEquals(8, FindBadMemberships.checkAll());  // missing memberships in ownerGroup and ownerGroup2
    
    ChangeLogTempToEntity.convertRecords();
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_consumer_compositeMemberships", false);

    // confirm second issue
    assertEquals(3, FindBadMemberships.checkAll());  // extra memberships in parentOwnerGroup
    
    ChangeLogTempToEntity.convertRecords();
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_consumer_compositeMemberships", false);
    
    // now good
    assertEquals(0, FindBadMemberships.checkAll());
  }
  
  /**
   * 
   */
  public void testMissingIntersectionCompositeDisabledGroup() throws Exception {
    Stem rootStem = StemFinder.findRootStem(GrouperSession.staticGrouperSession());
    Stem testStem = rootStem.addChildStem("testStem", "testStem");
    Group ownerGroup = testStem.addChildGroup("ownerGroup", "ownerGroup");
    Group ownerGroup2 = testStem.addChildGroup("ownerGroup2", "ownerGroup2");
    Group leftGroup = testStem.addChildGroup("leftGroup", "leftGroup");
    Group rightGroup = testStem.addChildGroup("rightGroup", "rightGroup");
    Group bogusGroup = testStem.addChildGroup("bogusGroup", "bogusGroup");
    
    Group parentOwnerGroup = testStem.addChildGroup("parentOwnerGroup", "parentOwnerGroup");
    Group parentLeftGroup = testStem.addChildGroup("parentLeftGroup", "parentLeftGroup");
    
    // clear change log
    ChangeLogTempToEntity.convertRecords();
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_consumer_compositeMemberships", false);
    
    ownerGroup.addCompositeMember(CompositeType.INTERSECTION, leftGroup, rightGroup);
    ownerGroup2.addCompositeMember(CompositeType.INTERSECTION, leftGroup, rightGroup);
    parentOwnerGroup.addCompositeMember(CompositeType.INTERSECTION, parentLeftGroup, ownerGroup);
    
    ChangeLogTempToEntity.convertRecords();
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_consumer_compositeMemberships", false);
    
    parentLeftGroup.addMember(SubjectTestHelper.SUBJ0);
    parentLeftGroup.addMember(SubjectTestHelper.SUBJ1);
    parentLeftGroup.addMember(SubjectTestHelper.SUBJ2);
    
    bogusGroup.addMember(SubjectTestHelper.SUBJ3);  // to get a record in grouper_members
    
    leftGroup.addMember(SubjectTestHelper.SUBJ0);
    leftGroup.addMember(SubjectTestHelper.SUBJ1);
    leftGroup.addMember(SubjectTestHelper.SUBJ2);
    leftGroup.addMember(SubjectTestHelper.SUBJ3);
   
    rightGroup.addMember(SubjectTestHelper.SUBJ0);
    rightGroup.addMember(SubjectTestHelper.SUBJ1);
    rightGroup.addMember(SubjectTestHelper.SUBJ2);
    rightGroup.addMember(SubjectTestHelper.SUBJ3);
    
    // confirm issue
    assertEquals(8, FindBadMemberships.checkAll());  // subj0-3 for ownerGroup and ownerGroup2
    
    ChangeLogTempToEntity.convertRecords();
    
    // still issue
    assertEquals(8, FindBadMemberships.checkAll());  // subj0-3 for ownerGroup and ownerGroup2
    
    // disable ownerGroup2
    ownerGroup2.setEnabledTime(new Timestamp(System.currentTimeMillis() + 1000000L));
    ownerGroup2.store();
    
    assertEquals(4, FindBadMemberships.checkAll());  // subj0-3 for ownerGroup
    
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_consumer_compositeMemberships", false);

    // now parent group still needs to be fixed
    assertEquals(3, FindBadMemberships.checkAll());  // subj0-2 for parentOwnerGroup
    
    ChangeLogTempToEntity.convertRecords();
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_consumer_compositeMemberships", false);

    // now good
    assertEquals(0, FindBadMemberships.checkAll());
  }
  
  /**
   * 
   */
  public void testMissingUnionComposite() throws Exception {
    Stem rootStem = StemFinder.findRootStem(GrouperSession.staticGrouperSession());
    Stem testStem = rootStem.addChildStem("testStem", "testStem");
    Group ownerGroup = testStem.addChildGroup("ownerGroup", "ownerGroup");
    Group ownerGroup2 = testStem.addChildGroup("ownerGroup2", "ownerGroup2");
    Group leftGroup = testStem.addChildGroup("leftGroup", "leftGroup");
    Group rightGroup = testStem.addChildGroup("rightGroup", "rightGroup");
    Group bogusGroup = testStem.addChildGroup("bogusGroup", "bogusGroup");
    
    Group parentOwnerGroup = testStem.addChildGroup("parentOwnerGroup", "parentOwnerGroup");
    Group parentLeftGroup = testStem.addChildGroup("parentLeftGroup", "parentLeftGroup");
    
    // clear change log
    ChangeLogTempToEntity.convertRecords();
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_consumer_compositeMemberships", false);
    
    ownerGroup.addCompositeMember(CompositeType.UNION, leftGroup, rightGroup);
    ownerGroup2.addCompositeMember(CompositeType.UNION, leftGroup, rightGroup);
    parentOwnerGroup.addCompositeMember(CompositeType.UNION, parentLeftGroup, ownerGroup);
    
    ChangeLogTempToEntity.convertRecords();
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_consumer_compositeMemberships", false);
    
    bogusGroup.addMember(SubjectTestHelper.SUBJ3);  // to get a record in grouper_members
   
    leftGroup.addMember(SubjectTestHelper.SUBJ0);
    leftGroup.addMember(SubjectTestHelper.SUBJ1);
    leftGroup.addMember(SubjectTestHelper.SUBJ2);
    leftGroup.addMember(SubjectTestHelper.SUBJ3);
    
    // confirm issue
    assertEquals(8, FindBadMemberships.checkAll());  // missing memberships in ownerGroup and ownerGroup2
    
    ChangeLogTempToEntity.convertRecords();
    
    // still issue
    assertEquals(8, FindBadMemberships.checkAll());  // subj0-3 for ownerGroup and ownerGroup2
    
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_consumer_compositeMemberships", false);

    // now parent group still needs to be fixed
    assertEquals(4, FindBadMemberships.checkAll());  // subj0-3 for parentOwnerGroup
    
    ChangeLogTempToEntity.convertRecords();
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_consumer_compositeMemberships", false);

    // now good
    assertEquals(0, FindBadMemberships.checkAll());
  }
  
  /**
   * 
   */
  public void testExtraUnionComposite() throws Exception {
    Stem rootStem = StemFinder.findRootStem(GrouperSession.staticGrouperSession());
    Stem testStem = rootStem.addChildStem("testStem", "testStem");
    Group ownerGroup = testStem.addChildGroup("ownerGroup", "ownerGroup");
    Group ownerGroup2 = testStem.addChildGroup("ownerGroup2", "ownerGroup2");
    Group leftGroup = testStem.addChildGroup("leftGroup", "leftGroup");
    Group rightGroup = testStem.addChildGroup("rightGroup", "rightGroup");
    
    Group parentOwnerGroup = testStem.addChildGroup("parentOwnerGroup", "parentOwnerGroup");
    Group parentLeftGroup = testStem.addChildGroup("parentLeftGroup", "parentLeftGroup");
    
    // clear change log
    ChangeLogTempToEntity.convertRecords();
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_consumer_compositeMemberships", false);
    
    ownerGroup.addCompositeMember(CompositeType.UNION, leftGroup, rightGroup);
    ownerGroup2.addCompositeMember(CompositeType.UNION, leftGroup, rightGroup);
    parentOwnerGroup.addCompositeMember(CompositeType.UNION, parentLeftGroup, ownerGroup);
    
    leftGroup.addMember(SubjectTestHelper.SUBJ0);
    leftGroup.addMember(SubjectTestHelper.SUBJ1);
    leftGroup.addMember(SubjectTestHelper.SUBJ2);
    leftGroup.addMember(SubjectTestHelper.SUBJ3);
    
    ChangeLogTempToEntity.convertRecords();
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_consumer_compositeMemberships", false);
    
    ChangeLogTempToEntity.convertRecords();
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_consumer_compositeMemberships", false);
    
    // remove members now
    leftGroup.deleteMember(SubjectTestHelper.SUBJ0);
    leftGroup.deleteMember(SubjectTestHelper.SUBJ1);
    leftGroup.deleteMember(SubjectTestHelper.SUBJ2);
    leftGroup.deleteMember(SubjectTestHelper.SUBJ3);
    
    // confirm issue
    assertEquals(8, FindBadMemberships.checkAll());  // subj0-3 for ownerGroup and ownerGroup2
    
    ChangeLogTempToEntity.convertRecords();
    
    // still issue
    assertEquals(8, FindBadMemberships.checkAll());  // subj0-3 for ownerGroup and ownerGroup2
    
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_consumer_compositeMemberships", false);

    // now parent group still needs to be fixed
    assertEquals(4, FindBadMemberships.checkAll());  // subj0-3 for parentOwnerGroup
    
    ChangeLogTempToEntity.convertRecords();
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_consumer_compositeMemberships", false);

    // now good
    assertEquals(0, FindBadMemberships.checkAll());
  }
  
  /**
   * 
   */
  public void testDeleteComposite() throws Exception {
    Stem rootStem = StemFinder.findRootStem(GrouperSession.staticGrouperSession());
    Stem testStem = rootStem.addChildStem("testStem", "testStem");
    Group ownerGroupIntersection = testStem.addChildGroup("ownerGroupIntersection", "ownerGroupIntersection");
    Group leftGroup = testStem.addChildGroup("leftGroup", "leftGroup");
    Group rightGroup = testStem.addChildGroup("rightGroup", "rightGroup");

    leftGroup.addMember(SubjectTestHelper.SUBJ0);
    leftGroup.addMember(SubjectTestHelper.SUBJ1);
    leftGroup.addMember(SubjectTestHelper.SUBJ2);

    rightGroup.addMember(SubjectTestHelper.SUBJ0);
    rightGroup.addMember(SubjectTestHelper.SUBJ1);
    rightGroup.addMember(SubjectTestHelper.SUBJ2);
    
    // clear change log
    ChangeLogTempToEntity.convertRecords();
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_consumer_compositeMemberships", false);
    
    ownerGroupIntersection.addCompositeMember(CompositeType.INTERSECTION, leftGroup, rightGroup);
    
    ChangeLogTempToEntity.convertRecords();
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_consumer_compositeMemberships", false);
   
    ChangeLogTempToEntity.convertRecords();
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_consumer_compositeMemberships", false);
   
    // should be good
    assertEquals(0, FindBadMemberships.checkAll());
    assertEquals(3, ownerGroupIntersection.getMembers().size());
    
    ownerGroupIntersection.deleteCompositeMember();

    assertEquals(3, FindBadMemberships.checkAll());
    assertEquals(3, ownerGroupIntersection.getMembers().size());

    ChangeLogTempToEntity.convertRecords();
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_consumer_compositeMemberships", false);
   
    assertEquals(0, FindBadMemberships.checkAll());
    assertEquals(0, ownerGroupIntersection.getMembers().size());
  }
  
  /**
   * 
   */
  public void testAddComposites() throws Exception {
    Stem rootStem = StemFinder.findRootStem(GrouperSession.staticGrouperSession());
    Stem testStem = rootStem.addChildStem("testStem", "testStem");
    Group ownerGroupUnion = testStem.addChildGroup("ownerGroupUnion", "ownerGroupUnion");
    Group ownerGroupComplement = testStem.addChildGroup("ownerGroupComplement", "ownerGroupComplement");
    Group ownerGroupIntersection = testStem.addChildGroup("ownerGroupIntersection", "ownerGroupIntersection");
    Group leftGroup = testStem.addChildGroup("leftGroup", "leftGroup");
    Group rightGroup = testStem.addChildGroup("rightGroup", "rightGroup");

    Group bogusGroup = testStem.addChildGroup("bogusGroup", "bogusGroup"); // just to get a member entry created
    bogusGroup.addMember(SubjectTestHelper.SUBJ9);
    Member member9 = MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), SubjectTestHelper.SUBJ9, false);

    leftGroup.addMember(SubjectTestHelper.SUBJ0);
    leftGroup.addMember(SubjectTestHelper.SUBJ1);
    leftGroup.addMember(SubjectTestHelper.SUBJ2);

    rightGroup.addMember(SubjectTestHelper.SUBJ1);
    rightGroup.addMember(SubjectTestHelper.SUBJ2);
    rightGroup.addMember(SubjectTestHelper.SUBJ3);
    
    // clear change log
    ChangeLogTempToEntity.convertRecords();
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_consumer_compositeMemberships", false);
    
    Composite compositeUnion = ownerGroupUnion.addCompositeMember(CompositeType.UNION, leftGroup, rightGroup);
    Composite compositeComplement = ownerGroupComplement.addCompositeMember(CompositeType.COMPLEMENT, leftGroup, rightGroup);
    Composite compositeIntersection = ownerGroupIntersection.addCompositeMember(CompositeType.INTERSECTION, leftGroup, rightGroup);
    
    // confirm issue
    assertEquals(7, FindBadMemberships.checkAll()); // union - 4, complement - 1, intersection - 2
    
    assertEquals(0, ownerGroupUnion.getMembers().size());
    assertEquals(0, ownerGroupComplement.getMembers().size());
    assertEquals(0, ownerGroupIntersection.getMembers().size());
    
    // mix in some bad memberships
    Membership ms1 = Composite.internal_createNewCompositeMembershipObject(ownerGroupUnion.getUuid(), member9.getUuid(), compositeUnion.getUuid());
    GrouperDAOFactory.getFactory().getMembership().save(ms1);
    Membership ms2 = Composite.internal_createNewCompositeMembershipObject(ownerGroupComplement.getUuid(), member9.getUuid(), compositeComplement.getUuid());
    GrouperDAOFactory.getFactory().getMembership().save(ms2);
    Membership ms3 = Composite.internal_createNewCompositeMembershipObject(ownerGroupIntersection.getUuid(), member9.getUuid(), compositeIntersection.getUuid());
    GrouperDAOFactory.getFactory().getMembership().save(ms3);
    
    assertEquals(10, FindBadMemberships.checkAll());
    
    ChangeLogTempToEntity.convertRecords();
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_consumer_compositeMemberships", false);
   
    ChangeLogTempToEntity.convertRecords();
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_consumer_compositeMemberships", false);
   
    // now good
    assertEquals(0, FindBadMemberships.checkAll());
    
    assertEquals(4, ownerGroupUnion.getMembers().size());
    assertEquals(1, ownerGroupComplement.getMembers().size());
    assertEquals(2, ownerGroupIntersection.getMembers().size());
  }
  
  /**
   * 
   */
  public void testEnableGroup() throws Exception {
    Stem rootStem = StemFinder.findRootStem(GrouperSession.staticGrouperSession());
    Stem testStem = rootStem.addChildStem("testStem", "testStem");
    Group ownerGroupIntersection = testStem.addChildGroup("ownerGroupIntersection", "ownerGroupIntersection");
    Group leftGroup = testStem.addChildGroup("leftGroup", "leftGroup");
    Group rightGroup = testStem.addChildGroup("rightGroup", "rightGroup");

    leftGroup.addMember(SubjectTestHelper.SUBJ0);
    leftGroup.addMember(SubjectTestHelper.SUBJ1);
    leftGroup.addMember(SubjectTestHelper.SUBJ2);

    rightGroup.addMember(SubjectTestHelper.SUBJ0);
    rightGroup.addMember(SubjectTestHelper.SUBJ1);
    rightGroup.addMember(SubjectTestHelper.SUBJ2);
    
    // clear change log
    ChangeLogTempToEntity.convertRecords();
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_consumer_compositeMemberships", false);
    
    ownerGroupIntersection.addCompositeMember(CompositeType.INTERSECTION, leftGroup, rightGroup);
    
    ChangeLogTempToEntity.convertRecords();
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_consumer_compositeMemberships", false);
   
    ChangeLogTempToEntity.convertRecords();
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_consumer_compositeMemberships", false);
   
    // should be good
    assertEquals(0, FindBadMemberships.checkAll());
    assertEquals(3, ownerGroupIntersection.getMembers().size());
    
    ownerGroupIntersection.setEnabledTime(new Timestamp(new Date().getTime() + 1000000L));
    ownerGroupIntersection.store();
    
    assertEquals(0, FindBadMemberships.checkAll());
    assertEquals(0, ownerGroupIntersection.getMembers().size());
    
    ChangeLogTempToEntity.convertRecords();
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_consumer_compositeMemberships", false);
   
    ChangeLogTempToEntity.convertRecords();
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_consumer_compositeMemberships", false);
   
    assertEquals(0, FindBadMemberships.checkAll());
    assertEquals(0, ownerGroupIntersection.getMembers().size());
    
    leftGroup.deleteMember(SubjectTestHelper.SUBJ0);
    leftGroup.addMember(SubjectTestHelper.SUBJ3);
    leftGroup.addMember(SubjectTestHelper.SUBJ4);
    rightGroup.addMember(SubjectTestHelper.SUBJ3);
    rightGroup.addMember(SubjectTestHelper.SUBJ4);

    ChangeLogTempToEntity.convertRecords();
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_consumer_compositeMemberships", false);
   
    ChangeLogTempToEntity.convertRecords();
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_consumer_compositeMemberships", false);
   
    assertEquals(0, FindBadMemberships.checkAll());
    assertEquals(0, ownerGroupIntersection.getMembers().size());
    
    // now enable and check
    ownerGroupIntersection.setEnabledTime(null);
    ownerGroupIntersection.store();
    
    assertEquals(3, FindBadMemberships.checkAll());
    assertEquals(3, ownerGroupIntersection.getMembers().size());
    

    ChangeLogTempToEntity.convertRecords();
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_consumer_compositeMemberships", false);
   
    ChangeLogTempToEntity.convertRecords();
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_consumer_compositeMemberships", false);
   
    assertEquals(0, FindBadMemberships.checkAll());
    assertEquals(4, ownerGroupIntersection.getMembers().size());
    assertTrue(ownerGroupIntersection.hasMember(SubjectTestHelper.SUBJ1));
    assertTrue(ownerGroupIntersection.hasMember(SubjectTestHelper.SUBJ2));
    assertTrue(ownerGroupIntersection.hasMember(SubjectTestHelper.SUBJ3));
    assertTrue(ownerGroupIntersection.hasMember(SubjectTestHelper.SUBJ4));
  }
  
  /**
   * 
   */
  public void testSynchronousComposites() throws Exception {
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("composites.synchronousCalculationGroupNameRegex", ".*leftGroup2.*|.*rightGroup1.*");

    Stem rootStem = StemFinder.findRootStem(GrouperSession.staticGrouperSession());
    Stem testStem = rootStem.addChildStem("testStem", "testStem");
    Group ownerGroupComplement1 = testStem.addChildGroup("ownerGroupComplement1", "ownerGroupComplement1");
    Group ownerGroupComplement2 = testStem.addChildGroup("ownerGroupComplement2", "ownerGroupComplement2");
    Group leftGroup1 = testStem.addChildGroup("leftGroup1", "leftGroup1");
    Group rightGroup1 = testStem.addChildGroup("rightGroup1", "rightGroup1");
    Group leftGroup2 = testStem.addChildGroup("leftGroup2", "leftGroup2");
    Group rightGroup2 = testStem.addChildGroup("rightGroup2", "rightGroup2");
    
    // clear change log
    ChangeLogTempToEntity.convertRecords();
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_consumer_compositeMemberships", false);
    
    ownerGroupComplement1.addCompositeMember(CompositeType.COMPLEMENT, leftGroup1, rightGroup1);
    ownerGroupComplement2.addCompositeMember(CompositeType.COMPLEMENT, leftGroup2, rightGroup2);
    
    ChangeLogTempToEntity.convertRecords();
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_consumer_compositeMemberships", false);
   
    ChangeLogTempToEntity.convertRecords();
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_consumer_compositeMemberships", false);
   
    assertEquals(0, FindBadMemberships.checkAll());
    
    leftGroup1.addMember(SubjectTestHelper.SUBJ1);
    assertEquals(1, FindBadMemberships.checkAll());

    leftGroup2.addMember(SubjectTestHelper.SUBJ1);
    assertEquals(1, FindBadMemberships.checkAll());

    assertFalse(ownerGroupComplement1.hasMember(SubjectTestHelper.SUBJ1));
    assertTrue(ownerGroupComplement2.hasMember(SubjectTestHelper.SUBJ1));
    
    ChangeLogTempToEntity.convertRecords();
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_consumer_compositeMemberships", false);
   
    ChangeLogTempToEntity.convertRecords();
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_consumer_compositeMemberships", false);
   
    assertEquals(0, FindBadMemberships.checkAll());
    assertTrue(ownerGroupComplement1.hasMember(SubjectTestHelper.SUBJ1));
    assertTrue(ownerGroupComplement2.hasMember(SubjectTestHelper.SUBJ1));
    
    rightGroup1.addMember(SubjectTestHelper.SUBJ1);
    assertEquals(0, FindBadMemberships.checkAll());

    rightGroup2.addMember(SubjectTestHelper.SUBJ1);
    assertEquals(1, FindBadMemberships.checkAll());
    
    assertFalse(ownerGroupComplement1.hasMember(SubjectTestHelper.SUBJ1));
    assertTrue(ownerGroupComplement2.hasMember(SubjectTestHelper.SUBJ1));
    
    ChangeLogTempToEntity.convertRecords();
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_consumer_compositeMemberships", false);
   
    ChangeLogTempToEntity.convertRecords();
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_consumer_compositeMemberships", false);
   
    assertEquals(0, FindBadMemberships.checkAll());
    assertFalse(ownerGroupComplement1.hasMember(SubjectTestHelper.SUBJ1));
    assertFalse(ownerGroupComplement2.hasMember(SubjectTestHelper.SUBJ1));
  }
  
  /**
   * 
   */
  public void testNestedFactor() throws Exception {
    Stem rootStem = StemFinder.findRootStem(GrouperSession.staticGrouperSession());
    Stem testStem = rootStem.addChildStem("testStem", "testStem");
    Group ownerGroupIntersection = testStem.addChildGroup("ownerGroupIntersection", "ownerGroupIntersection");
    Group leftGroupTop = testStem.addChildGroup("leftGroupTop", "leftGroupTop");
    Group rightGroupTop = testStem.addChildGroup("rightGroupTop", "rightGroupTop");
    Group leftGroupSub = testStem.addChildGroup("leftGroupSub", "leftGroupSub");
    Group rightGroupSub = testStem.addChildGroup("rightGroupSub", "rightGroupSub");
    
    leftGroupTop.addMember(leftGroupSub.toSubject());
    rightGroupTop.addMember(rightGroupSub.toSubject());
    
    // clear change log
    ChangeLogTempToEntity.convertRecords();
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_consumer_compositeMemberships", false);
    
    ownerGroupIntersection.addCompositeMember(CompositeType.INTERSECTION, leftGroupTop, rightGroupTop);
    
    ChangeLogTempToEntity.convertRecords();
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_consumer_compositeMemberships", false);
   
    ChangeLogTempToEntity.convertRecords();
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_consumer_compositeMemberships", false);
   
    // should be good
    assertEquals(0, FindBadMemberships.checkAll());
    assertEquals(0, ownerGroupIntersection.getMembers().size());
    
    leftGroupSub.addMember(SubjectTestHelper.SUBJ0);
    rightGroupSub.addMember(SubjectTestHelper.SUBJ0);
    
    assertEquals(1, FindBadMemberships.checkAll());
    assertEquals(0, ownerGroupIntersection.getMembers().size());
    
    ChangeLogTempToEntity.convertRecords();
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_consumer_compositeMemberships", false);
   
    ChangeLogTempToEntity.convertRecords();
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_consumer_compositeMemberships", false);
   
    assertEquals(0, FindBadMemberships.checkAll());
    assertEquals(1, ownerGroupIntersection.getMembers().size());
    
    // remove memberships and verify deletes
    leftGroupSub.deleteMember(SubjectTestHelper.SUBJ0);
    rightGroupSub.deleteMember(SubjectTestHelper.SUBJ0);
    
    assertEquals(1, FindBadMemberships.checkAll());
    assertEquals(1, ownerGroupIntersection.getMembers().size());
    
    ChangeLogTempToEntity.convertRecords();
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_consumer_compositeMemberships", false);
   
    ChangeLogTempToEntity.convertRecords();
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_consumer_compositeMemberships", false);
   
    assertEquals(0, FindBadMemberships.checkAll());
    assertEquals(0, ownerGroupIntersection.getMembers().size());
  }
  
  public void testDeleteAndAddComposite() {
    Stem rootStem = StemFinder.findRootStem(GrouperSession.staticGrouperSession());
    Stem testStem = rootStem.addChildStem("testStem", "testStem");
    Group ownerGroup = testStem.addChildGroup("ownerGroup", "ownerGroup");
    Group leftGroup1 = testStem.addChildGroup("leftGroup1", "leftGroup1");
    Group leftGroup2 = testStem.addChildGroup("leftGroup2", "leftGroup2");
    Group rightGroup = testStem.addChildGroup("rightGroup", "rightGroup");

    leftGroup1.addMember(SubjectTestHelper.SUBJ0);
    leftGroup1.addMember(SubjectTestHelper.SUBJ1);
    leftGroup1.addMember(SubjectTestHelper.SUBJ2);
    
    leftGroup2.addMember(SubjectTestHelper.SUBJ2);
    leftGroup2.addMember(SubjectTestHelper.SUBJ3);
    
    // clear change log
    ChangeLogTempToEntity.convertRecords();
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_consumer_compositeMemberships", false);
    
    ownerGroup.addCompositeMember(CompositeType.COMPLEMENT, leftGroup1, rightGroup);
   
    ChangeLogTempToEntity.convertRecords();
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_consumer_compositeMemberships", false);
    
    ChangeLogTempToEntity.convertRecords();
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_consumer_compositeMemberships", false);
    
    ownerGroup.deleteCompositeMember();
    Composite newComposite = ownerGroup.addCompositeMember(CompositeType.COMPLEMENT, leftGroup2, rightGroup);
    
    ChangeLogTempToEntity.convertRecords();
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_consumer_compositeMemberships", false);
    
    ChangeLogTempToEntity.convertRecords();
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_consumer_compositeMemberships", false);
    
    assertEquals(2, ownerGroup.getMembers().size());
    assertTrue(ownerGroup.hasMember(SubjectTestHelper.SUBJ2));
    assertTrue(ownerGroup.hasMember(SubjectTestHelper.SUBJ3));  
    
    Membership subj2Membership = MembershipFinder.findCompositeMembership(GrouperSession.staticGrouperSession(), ownerGroup, SubjectTestHelper.SUBJ2, true);
    Membership subj3Membership = MembershipFinder.findCompositeMembership(GrouperSession.staticGrouperSession(), ownerGroup, SubjectTestHelper.SUBJ3, true);
    
    assertEquals(newComposite.getUuid(), subj2Membership.getViaCompositeId());
    assertEquals(newComposite.getUuid(), subj3Membership.getViaCompositeId());
  }
}
