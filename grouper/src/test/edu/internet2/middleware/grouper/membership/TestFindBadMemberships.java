/*
  Copyright (C) 2004-2007 University Corporation for Advanced Internet Development, Inc.
  Copyright (C) 2004-2007 The University Of Chicago

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
*/

package edu.internet2.middleware.grouper.membership;


import java.io.StringReader;

import junit.textui.TestRunner;

import org.apache.commons.logging.Log;

import bsh.Interpreter;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.MembershipFinder;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.changeLog.ChangeLogTempToEntity;
import edu.internet2.middleware.grouper.group.GroupSet;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.R;
import edu.internet2.middleware.grouper.helper.SessionHelper;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.util.GrouperUuid;
import edu.internet2.middleware.grouper.misc.CompositeType;
import edu.internet2.middleware.grouper.misc.FindBadMemberships;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.pit.PITField;
import edu.internet2.middleware.grouper.pit.PITGroup;
import edu.internet2.middleware.grouper.pit.PITGroupSet;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * @author shilen
 */
public class TestFindBadMemberships extends GrouperTest {

  @SuppressWarnings("unused")
  private static final Log LOG = GrouperUtil.getLog(TestFindBadMemberships.class);

  private GrouperSession grouperSession;
  private Stem top;
  private Group owner1;
  private Group owner2;
  private Group owner3;
  private Group owner4;
  private Group owner5;
  private Group owner6;
  private Group owner7;
  private Group subjEGroup;
  private Subject subjA;
  private Subject subjB;
  private Subject subjC;
  private Subject subjD;
  private Subject subjE;
  private Subject subjF;
  
  /**
   * @param name
   */
  public TestFindBadMemberships(String name) {
    super(name);
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new TestFindBadMemberships("testWithBadGroupSetsForComposites"));
  }
  
  protected void setUp () {
    super.setUp();
    FindBadMemberships.clearResults();
    FindBadMemberships.printErrorsToSTOUT(false);
  }

  /**
   * Test bad composites without composites
   */
  public void testWithoutComposites() {
    assertEquals(0, FindBadMemberships.checkAll());
  }
  
  /**
   * @throws Exception
   */
  private void setUpComposites() throws Exception {
    R r = R.populateRegistry(0, 0, 6);
    subjA = r.getSubject("a");
    subjB = r.getSubject("b");
    subjC = r.getSubject("c");
    subjD = r.getSubject("d");
    subjF = r.getSubject("f");

    grouperSession = SessionHelper.getRootSession();
    Stem root = StemFinder.findRootStem(grouperSession);
    top = root.addChildStem("top", "top");
    
    subjEGroup = top.addChildGroup("subjE", "subjE");
    subjEGroup.addMember(subjC);
    subjE = subjEGroup.toSubject();
    
    owner1 = top.addChildGroup("owner1", "owner1");
    owner2 = top.addChildGroup("owner2", "owner2");
    owner3 = top.addChildGroup("owner3", "owner3");
    owner4 = top.addChildGroup("owner4", "owner4");
    owner5 = top.addChildGroup("owner5", "owner5");
    owner6 = top.addChildGroup("owner6", "owner6");
    owner7 = top.addChildGroup("owner7", "owner7");
    
    Group left1 = top.addChildGroup("left1", "left1");
    Group left2 = top.addChildGroup("left2", "left2");
    Group left3 = top.addChildGroup("left3", "left3");
    Group left4 = top.addChildGroup("left4", "left4");
    Group left5 = top.addChildGroup("left5", "left5");
    Group left6 = top.addChildGroup("left6", "left6");
    Group left7 = top.addChildGroup("left7", "left7");

    Group right1 = top.addChildGroup("right1", "right1");
    Group right2 = top.addChildGroup("right2", "right2");
    Group right3 = top.addChildGroup("right3", "right3");
    Group right4 = top.addChildGroup("right4", "right4");
    Group right5 = top.addChildGroup("right5", "right5");
    Group right6 = top.addChildGroup("right6", "right6");
    Group right7 = top.addChildGroup("right7", "right7");
    
    owner1.addCompositeMember(CompositeType.UNION, left1, right1);
    owner2.addCompositeMember(CompositeType.COMPLEMENT, left2, right2);
    owner3.addCompositeMember(CompositeType.INTERSECTION, left3, right3);
    owner4.addCompositeMember(CompositeType.UNION, left4, right4);
    owner5.addCompositeMember(CompositeType.COMPLEMENT, left5, right5);
    owner6.addCompositeMember(CompositeType.INTERSECTION, left6, right6);
    owner7.addCompositeMember(CompositeType.UNION, left7, right7);
    
    left1.addMember(subjA);
    left1.addMember(subjB);
    left1.addMember(subjE);
    left2.addMember(subjA);
    left2.addMember(subjB);
    left2.addMember(subjE);
    left2.addMember(subjF);
    right2.addMember(subjB);
    left3.addMember(subjA);
    right3.addMember(subjA);
    
    left4.addMember(subjA);
    left4.addMember(subjB);
    left4.addMember(subjE);
    left5.addMember(subjA);
    left5.addMember(subjB);
    left5.addMember(subjE);
    right5.addMember(subjB);
    left6.addMember(subjA);
    right6.addMember(subjA);
  }
  
  /**
   * Test bad composites with composites that are okay
   * @throws Exception 
   */
  public void testWithGoodComposites() throws Exception {
    setUpComposites();
    assertEquals(0, FindBadMemberships.checkAll());
  }
  
  /**
   * Test bad composites when there are missing composites
   * @throws Exception
   */
  public void testWithMissingComposite() throws Exception {
    setUpComposites();
    
    Membership ms1 = MembershipFinder.findCompositeMembership(grouperSession, owner1, subjC, true);
    Membership ms2 = MembershipFinder.findCompositeMembership(grouperSession, owner2, subjF, true);
    Membership ms3 = MembershipFinder.findCompositeMembership(grouperSession, owner3, subjA, true);
    
    Membership ms4 = MembershipFinder.findCompositeMembership(grouperSession, owner2, subjE, false);
    assertNull(ms4);
    
    ms1.delete();
    ms2.delete();
    ms3.delete();
    
    ChangeLogTempToEntity.convertRecords();
    
    assertEquals(3, FindBadMemberships.checkAll());
    String gsh = "import edu.internet2.middleware.grouper.*;\nimport edu.internet2.middleware.grouper.misc.*;\n" + FindBadMemberships.gshScript.toString();
    new Interpreter(new StringReader(gsh), System.out, System.err, false).run();
    assertEquals(0, FindBadMemberships.checkAll());
    
    // verify we don't mess up point in time
    ChangeLogTempToEntity.convertRecords();
    assertEquals(0, new edu.internet2.middleware.grouper.misc.SyncPITTables().showResults(false).syncAllPITTables());
  }
  
  /**
   * Test bad composites when there are extra composites
   * @throws Exception
   */
  public void testWithExtraComposite() throws Exception {
    setUpComposites();
    
    Membership ms1 = MembershipFinder.findCompositeMembership(grouperSession, owner1, subjC, true);
    Membership ms2 = MembershipFinder.findCompositeMembership(grouperSession, owner2, subjF, true);
    Membership ms3 = MembershipFinder.findCompositeMembership(grouperSession, owner3, subjA, true);
    
    ms1.setHibernateVersionNumber(-1L);
    ms2.setHibernateVersionNumber(-1L);
    ms3.setHibernateVersionNumber(-1L);
    
    ms1.setImmediateMembershipId(GrouperUuid.getUuid());
    ms2.setImmediateMembershipId(GrouperUuid.getUuid());
    ms3.setImmediateMembershipId(GrouperUuid.getUuid());
    
    Member member = MemberFinder.findBySubject(grouperSession, subjD, true);
    ms1.setMemberUuid(member.getUuid());
    ms2.setMemberUuid(member.getUuid());
    ms3.setMemberUuid(member.getUuid());
    
    GrouperDAOFactory.getFactory().getMembership().save(ms1);
    GrouperDAOFactory.getFactory().getMembership().save(ms2);
    GrouperDAOFactory.getFactory().getMembership().save(ms3);
    
    ChangeLogTempToEntity.convertRecords();

    assertEquals(3, FindBadMemberships.checkAll());
    String gsh = "import edu.internet2.middleware.grouper.*;\nimport edu.internet2.middleware.grouper.misc.*;\n" + FindBadMemberships.gshScript.toString();
    new Interpreter(new StringReader(gsh), System.out, System.err, false).run();
    assertEquals(0, FindBadMemberships.checkAll());
    
    // verify we don't mess up point in time
    ChangeLogTempToEntity.convertRecords();
    assertEquals(0, new edu.internet2.middleware.grouper.misc.SyncPITTables().showResults(false).syncAllPITTables());
  }
  
  /**
   * Test bad composites when composites have the wrong member, but the number of memberships
   * is still the same.
   * @throws Exception
   */
  public void testWithWrongComposite() throws Exception {
    setUpComposites();
    
    Membership ms1 = MembershipFinder.findCompositeMembership(grouperSession, owner1, subjC, true);
    Membership ms2 = MembershipFinder.findCompositeMembership(grouperSession, owner2, subjF, true);
    Membership ms3 = MembershipFinder.findCompositeMembership(grouperSession, owner3, subjA, true);
    
    Member member = MemberFinder.findBySubject(grouperSession, subjD, true);
    ms1.setMemberUuid(member.getUuid());
    ms2.setMemberUuid(member.getUuid());
    ms3.setMemberUuid(member.getUuid());
    
    GrouperDAOFactory.getFactory().getMembership().update(ms1);
    GrouperDAOFactory.getFactory().getMembership().update(ms2);
    GrouperDAOFactory.getFactory().getMembership().update(ms3);
    
    ChangeLogTempToEntity.convertRecords();
    
    assertEquals(6, FindBadMemberships.checkAll());
    String gsh = "import edu.internet2.middleware.grouper.*;\nimport edu.internet2.middleware.grouper.misc.*;\n" + FindBadMemberships.gshScript.toString();
    new Interpreter(new StringReader(gsh), System.out, System.err, false).run();
    assertEquals(0, FindBadMemberships.checkAll());
    
    // verify we don't mess up point in time
    ChangeLogTempToEntity.convertRecords();
    assertEquals(0, new edu.internet2.middleware.grouper.misc.SyncPITTables().showResults(false).syncAllPITTables());
  }
  
  /**
   * Test bad composites when composites have the wrong type.
   * @throws Exception
   */
  public void testWithWrongType() throws Exception {
    setUpComposites();
    
    Membership ms1 = MembershipFinder.findCompositeMembership(grouperSession, owner1, subjC, true);
    Membership ms2 = MembershipFinder.findCompositeMembership(grouperSession, owner2, subjF, true);
    Membership ms3 = MembershipFinder.findCompositeMembership(grouperSession, owner3, subjA, true);
    
    ms1.setType(MembershipType.IMMEDIATE.getTypeString());
    ms2.setType(MembershipType.IMMEDIATE.getTypeString());
    ms3.setType(MembershipType.IMMEDIATE.getTypeString());
    
    ms1.setViaCompositeId(null);
    ms2.setViaCompositeId(null);
    ms3.setViaCompositeId(null);
    
    GrouperDAOFactory.getFactory().getMembership().update(ms1);
    GrouperDAOFactory.getFactory().getMembership().update(ms2);
    GrouperDAOFactory.getFactory().getMembership().update(ms3);
    
    ChangeLogTempToEntity.convertRecords();

    assertEquals(6, FindBadMemberships.checkAll());
    String gsh = "import edu.internet2.middleware.grouper.*;\nimport edu.internet2.middleware.grouper.misc.*;\n" + FindBadMemberships.gshScript.toString();
    new Interpreter(new StringReader(gsh), System.out, System.err, false).run();
    assertEquals(0, FindBadMemberships.checkAll());
    
    // verify we don't mess up point in time
    ChangeLogTempToEntity.convertRecords();
    assertEquals(0, new edu.internet2.middleware.grouper.misc.SyncPITTables().showResults(false).syncAllPITTables());
  }
  
  /**
   * Test bad composites when composites have the wrong type.
   * @throws Exception
   */
  public void testWithWrongType2() throws Exception {
    setUpComposites();
    
    Membership ms1 = MembershipFinder.findImmediateMembership(grouperSession, subjEGroup, subjC, true);
    
    ms1.setType(MembershipType.COMPOSITE.getTypeString());
    ms1.setViaCompositeId(owner1.getComposite(true).getUuid());
    
    GrouperDAOFactory.getFactory().getMembership().update(ms1);
    
    ChangeLogTempToEntity.convertRecords();

    assertEquals(1, FindBadMemberships.checkAll());
    String gsh = "import edu.internet2.middleware.grouper.*;\nimport edu.internet2.middleware.grouper.misc.*;\n" + FindBadMemberships.gshScript.toString();
    new Interpreter(new StringReader(gsh), System.out, System.err, false).run();
    
    // now we have 4 more bad memberships since we just deleted subjEGroup -> subjC
    FindBadMemberships.clearResults();
    assertEquals(4, FindBadMemberships.checkAll());
    gsh = "import edu.internet2.middleware.grouper.*;\nimport edu.internet2.middleware.grouper.misc.*;\n" + FindBadMemberships.gshScript.toString();
    new Interpreter(new StringReader(gsh), System.out, System.err, false).run();
    assertEquals(0, FindBadMemberships.checkAll());

    // verify we don't mess up point in time
    ChangeLogTempToEntity.convertRecords();
    assertEquals(0, new edu.internet2.middleware.grouper.misc.SyncPITTables().showResults(false).syncAllPITTables());
  }
  
  /**
   * Test bad composites when composites have the wrong viaCompositeId.
   * @throws Exception
   */
  public void testWithWrongViaCompositeId() throws Exception {
    setUpComposites();
    
    Membership ms1 = MembershipFinder.findCompositeMembership(grouperSession, owner1, subjC, true);
    
    ms1.setViaCompositeId(owner2.getComposite(true).getUuid());
    
    GrouperDAOFactory.getFactory().getMembership().update(ms1);
    
    ChangeLogTempToEntity.convertRecords();

    assertEquals(1, FindBadMemberships.checkAll());
    String gsh = "import edu.internet2.middleware.grouper.*;\nimport edu.internet2.middleware.grouper.misc.*;\n" + FindBadMemberships.gshScript.toString();
    new Interpreter(new StringReader(gsh), System.out, System.err, false).run();
    
    // now we have 1 more bad membership since we just deleted owner1 -> subjC
    FindBadMemberships.clearResults();
    assertEquals(1, FindBadMemberships.checkAll());
    gsh = "import edu.internet2.middleware.grouper.*;\nimport edu.internet2.middleware.grouper.misc.*;\n" + FindBadMemberships.gshScript.toString();
    new Interpreter(new StringReader(gsh), System.out, System.err, false).run();
    assertEquals(0, FindBadMemberships.checkAll());

    // verify we don't mess up point in time
    ChangeLogTempToEntity.convertRecords();
    assertEquals(0, new edu.internet2.middleware.grouper.misc.SyncPITTables().showResults(false).syncAllPITTables());
  }
  
  /**
   * Test group sets using default field and depth=0 that have the wrong type
   * @throws Exception
   */
  public void testWithWrongGroupSetType() throws Exception {
    setUpComposites();
    
    GroupSet gs1 = GrouperDAOFactory.getFactory().getGroupSet().findSelfGroup(owner1.getId(), Group.getDefaultList().getUuid());
    GroupSet gs2 = GrouperDAOFactory.getFactory().getGroupSet().findSelfGroup(owner2.getId(), Group.getDefaultList().getUuid());
    GroupSet gs3 = GrouperDAOFactory.getFactory().getGroupSet().findSelfGroup(owner3.getId(), Group.getDefaultList().getUuid());
    GroupSet gs4 = GrouperDAOFactory.getFactory().getGroupSet().findSelfGroup(subjEGroup.getId(), Group.getDefaultList().getUuid());
    
    gs1.setType("immediate");
    gs2.setType("immediate");
    gs3.setType("immediate");
    gs4.setType("composite");
    
    GrouperDAOFactory.getFactory().getGroupSet().update(gs1);
    GrouperDAOFactory.getFactory().getGroupSet().update(gs2);
    GrouperDAOFactory.getFactory().getGroupSet().update(gs3);
    GrouperDAOFactory.getFactory().getGroupSet().update(gs4);
    
    ChangeLogTempToEntity.convertRecords();

    assertEquals(4, FindBadMemberships.checkAll());
    String gsh = "importCommands(\"edu.internet2.middleware.grouper.app.gsh\");\nimport edu.internet2.middleware.grouper.*;\nimport edu.internet2.middleware.grouper.misc.*;\n" + FindBadMemberships.gshScript.toString();
    new Interpreter(new StringReader(gsh), System.out, System.err, false).run();
    assertEquals(0, FindBadMemberships.checkAll());
    
    // verify we don't mess up point in time
    ChangeLogTempToEntity.convertRecords();
    assertEquals(0, new edu.internet2.middleware.grouper.misc.SyncPITTables().showResults(false).syncAllPITTables());
  }

  /**
   * Test scenario where a composite group has group sets
   * @throws Exception
   */
  public void testWithBadGroupSetsForComposites() throws Exception {
    setUpComposites();
    
    Group g1 = top.addChildGroup("g1", "g1");
    Group g2 = top.addChildGroup("g2", "g2");
    Group g3 = top.addChildGroup("g3", "g3");
    Group g4 = top.addChildGroup("g4", "g4");
    g1.addMember(g2.toSubject());
    g2.addMember(owner2.toSubject());
    g3.addMember(g4.toSubject());
    owner1.grantPriv(g3.toSubject(), AccessPrivilege.READ); // group sets created by this are good..
    
    ChangeLogTempToEntity.convertRecords();

    int groupSetCount = HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_group_set");
    int pitGroupSetCountTotal = HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_pit_group_set");
    int pitGroupSetCountActive = HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_pit_group_set where active='T'");
    
    // add groupSet owner2 -> g3
    GroupSet gs1 = new GroupSet();
    gs1.setId(GrouperUuid.getUuid());
    gs1.setCreatorId(GrouperSession.staticGrouperSession().getMemberUuid());
    gs1.setDepth(1);
    gs1.setFieldId(Group.getDefaultList().getUuid());
    gs1.setMemberGroupId(g3.getUuid());
    gs1.setType(MembershipType.EFFECTIVE.getTypeString());
    gs1.setOwnerGroupId(owner2.getUuid());
    gs1.setParentId(GrouperDAOFactory.getFactory().getGroupSet().findSelfGroup(owner2.getUuid(), Group.getDefaultList().getUuid()).getId());
    GrouperDAOFactory.getFactory().getGroupSet().save(gs1);
    
    // add pitGroupSet owner2 -> g3
    PITField pitMemberField = GrouperDAOFactory.getFactory().getPITField().findBySourceIdActive(Group.getDefaultList().getUuid(), true);
    PITGroup pitOwner1 = GrouperDAOFactory.getFactory().getPITGroup().findBySourceIdActive(owner2.getUuid(), true);
    PITGroup pitMember1 = GrouperDAOFactory.getFactory().getPITGroup().findBySourceIdActive(g3.getUuid(), true);
    PITGroupSet pitParent1 = GrouperDAOFactory.getFactory().getPITGroupSet().findBySourceIdActive(gs1.getParentId(), true);
    PITGroupSet pitGS1 = new PITGroupSet();
    pitGS1.setOwnerGroupId(pitOwner1.getId());
    pitGS1.setId(GrouperUuid.getUuid());
    pitGS1.setSourceId(gs1.getId());
    pitGS1.setFieldId(pitMemberField.getId());
    pitGS1.setMemberFieldId(pitMemberField.getId());
    pitGS1.setMemberGroupId(pitMember1.getId());
    pitGS1.setDepth(1);
    pitGS1.setParentId(pitParent1.getId());
    pitGS1.setActiveDb("T");
    pitGS1.setStartTimeDb(System.currentTimeMillis() * 1000);
    pitGS1.saveOrUpdate();
    
    int newGroupSetCount = HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_group_set");
    int newPitGroupSetCountTotal = HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_pit_group_set");
    int newPitGroupSetCountActive = HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_pit_group_set where active='T'");
    
    // verify that 8 group sets were added...
    assertEquals(groupSetCount + 6, newGroupSetCount);
    assertEquals(pitGroupSetCountTotal + 6, newPitGroupSetCountTotal);
    assertEquals(pitGroupSetCountActive + 6, newPitGroupSetCountActive);
        
    assertEquals(1, FindBadMemberships.checkAll());
    String gsh = "import edu.internet2.middleware.grouper.*;\nimport edu.internet2.middleware.grouper.misc.*;\n" + FindBadMemberships.gshScript.toString();
    new Interpreter(new StringReader(gsh), System.out, System.err, false).run();
    assertEquals(0, FindBadMemberships.checkAll());
    
    // verify we don't mess up point in time
    ChangeLogTempToEntity.convertRecords();
    assertEquals(0, new edu.internet2.middleware.grouper.misc.SyncPITTables().showResults(false).syncAllPITTables());
    
    // check counts again..
    newGroupSetCount = HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_group_set");
    newPitGroupSetCountTotal = HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_pit_group_set");
    newPitGroupSetCountActive = HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_pit_group_set where active='T'");
    
    assertEquals(groupSetCount, newGroupSetCount);
    assertEquals(pitGroupSetCountTotal + 6, newPitGroupSetCountTotal);
    assertEquals(pitGroupSetCountActive, newPitGroupSetCountActive);
  }
}

