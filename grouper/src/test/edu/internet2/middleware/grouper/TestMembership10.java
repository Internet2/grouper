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

package edu.internet2.middleware.grouper;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;

import junit.framework.Assert;
import junit.framework.TestCase;
import junit.textui.TestRunner;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.misc.CompositeType;
import edu.internet2.middleware.grouper.misc.FindBadMemberships;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
import edu.internet2.middleware.grouper.registry.RegistryReset;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * @author Shilen Patel.
 */
public class TestMembership10 extends TestCase {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new TestMembership10("testComplementComposite"));
  }
  
  private static final Log LOG = GrouperUtil.getLog(TestMembership10.class);

  Date before;
  R       r;
  Group   gA;
  Group   gB;
  Group   gC;
  Group   gD;
  Group   gE;
  Group   gF;
  Group   gG;
  Subject subjA;
  Subject subjB;
  Subject subjC;
  Stem    nsA;

  Field fieldMembers;
  Field fieldUpdaters;
  Field fieldCreators;

  public TestMembership10(String name) {
    super(name);
  }

  protected void setUp () {
    LOG.debug("setUp");
    RegistryReset.reset();
  }

  protected void tearDown () {
    LOG.debug("tearDown");
  }

  public void testComplementComposite() {
    LOG.info("testComplementComposite");
    try {
      before   = DateHelper.getPastDate();

      r     = R.populateRegistry(2, 7, 3);
      gA    = r.getGroup("a", "a");
      gB    = r.getGroup("a", "b");
      gC    = r.getGroup("a", "c");
      gD    = r.getGroup("a", "d");
      gE    = r.getGroup("a", "e");
      gF    = r.getGroup("a", "f");
      gG    = r.getGroup("a", "g");
      subjA = r.getSubject("a");
      subjB = r.getSubject("b");
      subjC = r.getSubject("c");
      nsA   = r.getStem("a");

      fieldMembers = Group.getDefaultList();
      fieldUpdaters = FieldFinder.find("updaters");
      fieldCreators = FieldFinder.find("creators");

      Set<Membership> listMemberships;
      Set<Membership> updateMemberships;
      Set<Membership> createMemberships;

      Set<Group> goodGroups = new LinkedHashSet<Group>();
      Set<Group> badGroups = new LinkedHashSet<Group>();

      goodGroups.add(gA);
      goodGroups.add(gB);
      goodGroups.add(gC);
      goodGroups.add(gD);
      goodGroups.add(gE);
      goodGroups.add(gF);
      goodGroups.add(gG);


      // Test 1
      gB.addMember(gD.toSubject());
      gD.addMember(subjA);
      gD.addMember(subjB);
      gC.addMember(gE.toSubject());
      gE.addMember(subjA);
      gE.addMember(subjC);
      gA.addCompositeMember(CompositeType.COMPLEMENT , gB, gC);
      gF.addMember(gA.toSubject());
      gG.grantPriv(gA.toSubject(), AccessPrivilege.UPDATE);
      nsA.grantPriv(gA.toSubject(), NamingPrivilege.CREATE);

      verifyMemberships();

      MembershipTestHelper.checkBadGroupMemberships("All should be good", goodGroups, badGroups);
      Assert.assertEquals("There should not be any invalid memberships", 0, FindBadMemberships.checkMembershipsWithInvalidOwners());

      // clear out memberships
      gB.deleteMember(gD.toSubject());
      gD.deleteMember(subjA);
      gD.deleteMember(subjB);
      gC.deleteMember(gE.toSubject());
      gE.deleteMember(subjA);
      gE.deleteMember(subjC);
      gA.deleteCompositeMember();
      gF.deleteMember(gA.toSubject());
      gG.revokePriv(gA.toSubject(), AccessPrivilege.UPDATE);
      nsA.revokePriv(gA.toSubject(), NamingPrivilege.CREATE);

      listMemberships = MembershipFinder.internal_findAllByCreatedAfter(r.rs, before, fieldMembers);
      T.amount("Number of list memberships", 0, listMemberships.size());

      updateMemberships = MembershipFinder.internal_findAllByCreatedAfter(r.rs, before, fieldUpdaters);
      T.amount("Number of update privileges", 0, updateMemberships.size());

      createMemberships = MembershipFinder.internal_findAllByCreatedAfter(r.rs, before, fieldCreators);
      T.amount("Number of create privileges", 0, createMemberships.size());

      // Test 2
      gD.addMember(subjA);
      gD.addMember(subjB);
      gC.addMember(gE.toSubject());
      gE.addMember(subjA);
      gE.addMember(subjC);
      gA.addCompositeMember(CompositeType.COMPLEMENT , gB, gC);
      gF.addMember(gA.toSubject());
      gG.grantPriv(gA.toSubject(), AccessPrivilege.UPDATE);
      nsA.grantPriv(gA.toSubject(), NamingPrivilege.CREATE);
      gB.addMember(gD.toSubject());

      verifyMemberships();

      MembershipTestHelper.checkBadGroupMemberships("All should be good", goodGroups, badGroups);
      Assert.assertEquals("There should not be any invalid memberships", 0, FindBadMemberships.checkMembershipsWithInvalidOwners());

      // clear out memberships
      gB.deleteMember(gD.toSubject());
      gD.deleteMember(subjA);
      gD.deleteMember(subjB);
      gC.deleteMember(gE.toSubject());
      gE.deleteMember(subjA);
      gE.deleteMember(subjC);
      gA.deleteCompositeMember();
      gF.deleteMember(gA.toSubject());
      gG.revokePriv(gA.toSubject(), AccessPrivilege.UPDATE);
      nsA.revokePriv(gA.toSubject(), NamingPrivilege.CREATE);

      listMemberships = MembershipFinder.internal_findAllByCreatedAfter(r.rs, before, fieldMembers);
      T.amount("Number of list memberships", 0, listMemberships.size());

      updateMemberships = MembershipFinder.internal_findAllByCreatedAfter(r.rs, before, fieldUpdaters);
      T.amount("Number of update privileges", 0, updateMemberships.size());

      createMemberships = MembershipFinder.internal_findAllByCreatedAfter(r.rs, before, fieldCreators);
      T.amount("Number of create privileges", 0, createMemberships.size());

      // Test 3
      gD.addMember(subjB);
      gC.addMember(gE.toSubject());
      gE.addMember(subjA);
      gE.addMember(subjC);
      gA.addCompositeMember(CompositeType.COMPLEMENT , gB, gC);
      gF.addMember(gA.toSubject());
      gG.grantPriv(gA.toSubject(), AccessPrivilege.UPDATE);
      nsA.grantPriv(gA.toSubject(), NamingPrivilege.CREATE);
      gB.addMember(gD.toSubject());
      gD.addMember(subjA);

      verifyMemberships();

      MembershipTestHelper.checkBadGroupMemberships("All should be good", goodGroups, badGroups);
      Assert.assertEquals("There should not be any invalid memberships", 0, FindBadMemberships.checkMembershipsWithInvalidOwners());

      // clear out memberships
      gB.deleteMember(gD.toSubject());
      gD.deleteMember(subjA);
      gD.deleteMember(subjB);
      gC.deleteMember(gE.toSubject());
      gE.deleteMember(subjA);
      gE.deleteMember(subjC);
      gA.deleteCompositeMember();
      gF.deleteMember(gA.toSubject());
      gG.revokePriv(gA.toSubject(), AccessPrivilege.UPDATE);
      nsA.revokePriv(gA.toSubject(), NamingPrivilege.CREATE);

      listMemberships = MembershipFinder.internal_findAllByCreatedAfter(r.rs, before, fieldMembers);
      T.amount("Number of list memberships", 0, listMemberships.size());

      updateMemberships = MembershipFinder.internal_findAllByCreatedAfter(r.rs, before, fieldUpdaters);
      T.amount("Number of update privileges", 0, updateMemberships.size());

      createMemberships = MembershipFinder.internal_findAllByCreatedAfter(r.rs, before, fieldCreators);
      T.amount("Number of create privileges", 0, createMemberships.size());

      // Test 4
      gC.addMember(gE.toSubject());
      gE.addMember(subjA);
      gE.addMember(subjC);
      gA.addCompositeMember(CompositeType.COMPLEMENT , gB, gC);
      gF.addMember(gA.toSubject());
      gG.grantPriv(gA.toSubject(), AccessPrivilege.UPDATE);
      nsA.grantPriv(gA.toSubject(), NamingPrivilege.CREATE);
      gB.addMember(gD.toSubject());
      gD.addMember(subjA);
      gD.addMember(subjB);

      verifyMemberships();

      MembershipTestHelper.checkBadGroupMemberships("All should be good", goodGroups, badGroups);
      Assert.assertEquals("There should not be any invalid memberships", 0, FindBadMemberships.checkMembershipsWithInvalidOwners());

      // clear out memberships
      gB.deleteMember(gD.toSubject());
      gD.deleteMember(subjA);
      gD.deleteMember(subjB);
      gC.deleteMember(gE.toSubject());
      gE.deleteMember(subjA);
      gE.deleteMember(subjC);
      gA.deleteCompositeMember();
      gF.deleteMember(gA.toSubject());
      gG.revokePriv(gA.toSubject(), AccessPrivilege.UPDATE);
      nsA.revokePriv(gA.toSubject(), NamingPrivilege.CREATE);

      listMemberships = MembershipFinder.internal_findAllByCreatedAfter(r.rs, before, fieldMembers);
      T.amount("Number of list memberships", 0, listMemberships.size());

      updateMemberships = MembershipFinder.internal_findAllByCreatedAfter(r.rs, before, fieldUpdaters);
      T.amount("Number of update privileges", 0, updateMemberships.size());

      createMemberships = MembershipFinder.internal_findAllByCreatedAfter(r.rs, before, fieldCreators);
      T.amount("Number of create privileges", 0, createMemberships.size());

      // Test 5
      gE.addMember(subjA);
      gE.addMember(subjC);
      gA.addCompositeMember(CompositeType.COMPLEMENT , gB, gC);
      gF.addMember(gA.toSubject());
      gG.grantPriv(gA.toSubject(), AccessPrivilege.UPDATE);
      nsA.grantPriv(gA.toSubject(), NamingPrivilege.CREATE);
      gB.addMember(gD.toSubject());
      gD.addMember(subjA);
      gD.addMember(subjB);
      gC.addMember(gE.toSubject());

      verifyMemberships();

      MembershipTestHelper.checkBadGroupMemberships("All should be good", goodGroups, badGroups);
      Assert.assertEquals("There should not be any invalid memberships", 0, FindBadMemberships.checkMembershipsWithInvalidOwners());

      // clear out memberships
      gB.deleteMember(gD.toSubject());
      gD.deleteMember(subjA);
      gD.deleteMember(subjB);
      gC.deleteMember(gE.toSubject());
      gE.deleteMember(subjA);
      gE.deleteMember(subjC);
      gA.deleteCompositeMember();
      gF.deleteMember(gA.toSubject());
      gG.revokePriv(gA.toSubject(), AccessPrivilege.UPDATE);
      nsA.revokePriv(gA.toSubject(), NamingPrivilege.CREATE);

      listMemberships = MembershipFinder.internal_findAllByCreatedAfter(r.rs, before, fieldMembers);
      T.amount("Number of list memberships", 0, listMemberships.size());

      updateMemberships = MembershipFinder.internal_findAllByCreatedAfter(r.rs, before, fieldUpdaters);
      T.amount("Number of update privileges", 0, updateMemberships.size());

      createMemberships = MembershipFinder.internal_findAllByCreatedAfter(r.rs, before, fieldCreators);
      T.amount("Number of create privileges", 0, createMemberships.size());

      // Test 6
      gE.addMember(subjC);
      gA.addCompositeMember(CompositeType.COMPLEMENT , gB, gC);
      gF.addMember(gA.toSubject());
      gG.grantPriv(gA.toSubject(), AccessPrivilege.UPDATE);
      nsA.grantPriv(gA.toSubject(), NamingPrivilege.CREATE);
      gB.addMember(gD.toSubject());
      gD.addMember(subjA);
      gD.addMember(subjB);
      gC.addMember(gE.toSubject());
      gE.addMember(subjA);

      verifyMemberships();

      MembershipTestHelper.checkBadGroupMemberships("All should be good", goodGroups, badGroups);
      Assert.assertEquals("There should not be any invalid memberships", 0, FindBadMemberships.checkMembershipsWithInvalidOwners());

      // clear out memberships
      gB.deleteMember(gD.toSubject());
      gD.deleteMember(subjA);
      gD.deleteMember(subjB);
      gC.deleteMember(gE.toSubject());
      gE.deleteMember(subjA);
      gE.deleteMember(subjC);
      gA.deleteCompositeMember();
      gF.deleteMember(gA.toSubject());
      gG.revokePriv(gA.toSubject(), AccessPrivilege.UPDATE);
      nsA.revokePriv(gA.toSubject(), NamingPrivilege.CREATE);

      listMemberships = MembershipFinder.internal_findAllByCreatedAfter(r.rs, before, fieldMembers);
      T.amount("Number of list memberships", 0, listMemberships.size());

      updateMemberships = MembershipFinder.internal_findAllByCreatedAfter(r.rs, before, fieldUpdaters);
      T.amount("Number of update privileges", 0, updateMemberships.size());

      createMemberships = MembershipFinder.internal_findAllByCreatedAfter(r.rs, before, fieldCreators);
      T.amount("Number of create privileges", 0, createMemberships.size());

      // Test 7
      gA.addCompositeMember(CompositeType.COMPLEMENT , gB, gC);
      gF.addMember(gA.toSubject());
      gG.grantPriv(gA.toSubject(), AccessPrivilege.UPDATE);
      nsA.grantPriv(gA.toSubject(), NamingPrivilege.CREATE);
      gB.addMember(gD.toSubject());
      gD.addMember(subjA);
      gD.addMember(subjB);
      gC.addMember(gE.toSubject());
      gE.addMember(subjA);
      gE.addMember(subjC);

      verifyMemberships();

      MembershipTestHelper.checkBadGroupMemberships("All should be good", goodGroups, badGroups);
      Assert.assertEquals("There should not be any invalid memberships", 0, FindBadMemberships.checkMembershipsWithInvalidOwners());

      // clear out memberships
      gB.deleteMember(gD.toSubject());
      gD.deleteMember(subjA);
      gD.deleteMember(subjB);
      gC.deleteMember(gE.toSubject());
      gE.deleteMember(subjA);
      gE.deleteMember(subjC);
      gA.deleteCompositeMember();
      gF.deleteMember(gA.toSubject());
      gG.revokePriv(gA.toSubject(), AccessPrivilege.UPDATE);
      nsA.revokePriv(gA.toSubject(), NamingPrivilege.CREATE);

      listMemberships = MembershipFinder.internal_findAllByCreatedAfter(r.rs, before, fieldMembers);
      T.amount("Number of list memberships", 0, listMemberships.size());

      updateMemberships = MembershipFinder.internal_findAllByCreatedAfter(r.rs, before, fieldUpdaters);
      T.amount("Number of update privileges", 0, updateMemberships.size());

      createMemberships = MembershipFinder.internal_findAllByCreatedAfter(r.rs, before, fieldCreators);
      T.amount("Number of create privileges", 0, createMemberships.size());

      // Test 8
      gF.addMember(gA.toSubject());
      gG.grantPriv(gA.toSubject(), AccessPrivilege.UPDATE);
      nsA.grantPriv(gA.toSubject(), NamingPrivilege.CREATE);
      gB.addMember(gD.toSubject());
      gD.addMember(subjA);
      gD.addMember(subjB);
      gC.addMember(gE.toSubject());
      gE.addMember(subjA);
      gE.addMember(subjC);
      gA.addCompositeMember(CompositeType.COMPLEMENT , gB, gC);

      verifyMemberships();

      MembershipTestHelper.checkBadGroupMemberships("All should be good", goodGroups, badGroups);
      Assert.assertEquals("There should not be any invalid memberships", 0, FindBadMemberships.checkMembershipsWithInvalidOwners());

      // clear out memberships
      gB.deleteMember(gD.toSubject());
      gD.deleteMember(subjA);
      gD.deleteMember(subjB);
      gC.deleteMember(gE.toSubject());
      gE.deleteMember(subjA);
      gE.deleteMember(subjC);
      gA.deleteCompositeMember();
      gF.deleteMember(gA.toSubject());
      gG.revokePriv(gA.toSubject(), AccessPrivilege.UPDATE);
      nsA.revokePriv(gA.toSubject(), NamingPrivilege.CREATE);

      listMemberships = MembershipFinder.internal_findAllByCreatedAfter(r.rs, before, fieldMembers);
      T.amount("Number of list memberships", 0, listMemberships.size());

      updateMemberships = MembershipFinder.internal_findAllByCreatedAfter(r.rs, before, fieldUpdaters);
      T.amount("Number of update privileges", 0, updateMemberships.size());

      createMemberships = MembershipFinder.internal_findAllByCreatedAfter(r.rs, before, fieldCreators);
      T.amount("Number of create privileges", 0, createMemberships.size());

      // Test 9
      gG.grantPriv(gA.toSubject(), AccessPrivilege.UPDATE);
      nsA.grantPriv(gA.toSubject(), NamingPrivilege.CREATE);
      gB.addMember(gD.toSubject());
      gD.addMember(subjA);
      gD.addMember(subjB);
      gC.addMember(gE.toSubject());
      gE.addMember(subjA);
      gE.addMember(subjC);
      gA.addCompositeMember(CompositeType.COMPLEMENT , gB, gC);
      gF.addMember(gA.toSubject());

      verifyMemberships();

      MembershipTestHelper.checkBadGroupMemberships("All should be good", goodGroups, badGroups);
      Assert.assertEquals("There should not be any invalid memberships", 0, FindBadMemberships.checkMembershipsWithInvalidOwners());

      // clear out memberships
      gB.deleteMember(gD.toSubject());
      gD.deleteMember(subjA);
      gD.deleteMember(subjB);
      gC.deleteMember(gE.toSubject());
      gE.deleteMember(subjA);
      gE.deleteMember(subjC);
      gA.deleteCompositeMember();
      gF.deleteMember(gA.toSubject());
      gG.revokePriv(gA.toSubject(), AccessPrivilege.UPDATE);
      nsA.revokePriv(gA.toSubject(), NamingPrivilege.CREATE);

      listMemberships = MembershipFinder.internal_findAllByCreatedAfter(r.rs, before, fieldMembers);
      T.amount("Number of list memberships", 0, listMemberships.size());

      updateMemberships = MembershipFinder.internal_findAllByCreatedAfter(r.rs, before, fieldUpdaters);
      T.amount("Number of update privileges", 0, updateMemberships.size());

      createMemberships = MembershipFinder.internal_findAllByCreatedAfter(r.rs, before, fieldCreators);
      T.amount("Number of create privileges", 0, createMemberships.size());

      // Test 10
      nsA.grantPriv(gA.toSubject(), NamingPrivilege.CREATE);
      gB.addMember(gD.toSubject());
      gD.addMember(subjA);
      gD.addMember(subjB);
      gC.addMember(gE.toSubject());
      gE.addMember(subjA);
      gE.addMember(subjC);
      gA.addCompositeMember(CompositeType.COMPLEMENT , gB, gC);
      gF.addMember(gA.toSubject());
      gG.grantPriv(gA.toSubject(), AccessPrivilege.UPDATE);

      verifyMemberships();

      MembershipTestHelper.checkBadGroupMemberships("All should be good", goodGroups, badGroups);
      Assert.assertEquals("There should not be any invalid memberships", 0, FindBadMemberships.checkMembershipsWithInvalidOwners());

      // clear out memberships
      gB.deleteMember(gD.toSubject());
      gD.deleteMember(subjA);
      gD.deleteMember(subjB);
      gC.deleteMember(gE.toSubject());
      gE.deleteMember(subjA);
      gE.deleteMember(subjC);
      gA.deleteCompositeMember();
      gF.deleteMember(gA.toSubject());
      gG.revokePriv(gA.toSubject(), AccessPrivilege.UPDATE);
      nsA.revokePriv(gA.toSubject(), NamingPrivilege.CREATE);

      listMemberships = MembershipFinder.internal_findAllByCreatedAfter(r.rs, before, fieldMembers);
      T.amount("Number of list memberships", 0, listMemberships.size());

      updateMemberships = MembershipFinder.internal_findAllByCreatedAfter(r.rs, before, fieldUpdaters);
      T.amount("Number of update privileges", 0, updateMemberships.size());

      createMemberships = MembershipFinder.internal_findAllByCreatedAfter(r.rs, before, fieldCreators);
      T.amount("Number of create privileges", 0, createMemberships.size());


      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  }


  public  void verifyMemberships() throws Exception {

    // gA should have two members only
    T.amount("Verify number of memberships for gA", 2, gA.getCompositeMemberships().size());
    Assert.assertTrue("Verify SB -> gA", gA.hasMember(subjB));
    Assert.assertTrue("Verify gD -> gA", gA.hasMember(gD.toSubject()));

    // verify the total number of list memberships
    Set<Membership> listMemberships = MembershipFinder.internal_findAllByCreatedAfter(r.rs, before, fieldMembers);
    T.amount("Number of list memberships", 15, listMemberships.size());

    // verify the total number of update privileges
    Set<Membership> updateMemberships = MembershipFinder.internal_findAllByCreatedAfter(r.rs, before, fieldUpdaters);
    T.amount("Number of update privileges", 3, updateMemberships.size());

    // verify the total number of create privileges
    Set<Membership> createMemberships = MembershipFinder.internal_findAllByCreatedAfter(r.rs, before, fieldCreators);
    T.amount("Number of create privileges", 3, createMemberships.size());
  }

}

