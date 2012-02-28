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
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.R;
import edu.internet2.middleware.grouper.helper.SessionHelper;
import edu.internet2.middleware.grouper.internal.util.GrouperUuid;
import edu.internet2.middleware.grouper.misc.CompositeType;
import edu.internet2.middleware.grouper.misc.FindBadMemberships;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * @author shilen
 */
public class TestFindBadMemberships extends GrouperTest {

  @SuppressWarnings("unused")
  private static final Log LOG = GrouperUtil.getLog(TestFindBadMemberships.class);

  private GrouperSession grouperSession;
  private Group owner1;
  private Group owner2;
  private Group owner3;
  private Group owner4;
  private Group owner5;
  private Group owner6;
  private Group owner7;
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
    TestRunner.run(new TestFindBadMemberships("testWithWrongComposite"));
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
    assertEquals(0, FindBadMemberships.checkComposites());
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
    Stem top = root.addChildStem("top", "top");
    
    Group subjEGroup = top.addChildGroup("subjE", "subjE");
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
    assertEquals(0, FindBadMemberships.checkComposites());
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
    
    assertEquals(3, FindBadMemberships.checkComposites());
    String gsh = "import edu.internet2.middleware.grouper.*;\nimport edu.internet2.middleware.grouper.misc.*;\n" + FindBadMemberships.gshScript.toString();
    new Interpreter(new StringReader(gsh), System.out, System.err, false).run();
    assertEquals(0, FindBadMemberships.checkComposites());
    
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

    assertEquals(3, FindBadMemberships.checkComposites());
    String gsh = "import edu.internet2.middleware.grouper.*;\nimport edu.internet2.middleware.grouper.misc.*;\n" + FindBadMemberships.gshScript.toString();
    new Interpreter(new StringReader(gsh), System.out, System.err, false).run();
    assertEquals(0, FindBadMemberships.checkComposites());
    
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
    
    assertEquals(6, FindBadMemberships.checkComposites());
    String gsh = "import edu.internet2.middleware.grouper.*;\nimport edu.internet2.middleware.grouper.misc.*;\n" + FindBadMemberships.gshScript.toString();
    new Interpreter(new StringReader(gsh), System.out, System.err, false).run();
    assertEquals(0, FindBadMemberships.checkComposites());
    
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

    assertEquals(6, FindBadMemberships.checkComposites());
    String gsh = "import edu.internet2.middleware.grouper.*;\nimport edu.internet2.middleware.grouper.misc.*;\n" + FindBadMemberships.gshScript.toString();
    new Interpreter(new StringReader(gsh), System.out, System.err, false).run();
    assertEquals(0, FindBadMemberships.checkComposites());
    
    // verify we don't mess up point in time
    ChangeLogTempToEntity.convertRecords();
    assertEquals(0, new edu.internet2.middleware.grouper.misc.SyncPITTables().showResults(false).syncAllPITTables());
  }
}

