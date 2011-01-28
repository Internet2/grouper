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
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;

import junit.framework.TestCase;
import junit.textui.TestRunner;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.FieldFinder;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.MembershipFinder;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.helper.DateHelper;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.MembershipTestHelper;
import edu.internet2.middleware.grouper.helper.R;
import edu.internet2.middleware.grouper.helper.T;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.registry.RegistryReset;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * @author Shilen Patel.
 */
public class TestMembership7 extends GrouperTest {

  public static void main(String[] args) {
    TestRunner.run(new TestMembership7("testCircularMembershipsWithoutComposites"));
  }
  
  private static final Log LOG = GrouperUtil.getLog(TestMembership7.class);

  Date before;
  R       r;
  Group   gA;
  Group   gB;
  Group   gC;
  Group   gD;
  Group   gE;
  Subject subjA;
  Subject subjB;
  Stem    nsA;

  Field fieldMembers;
  Field fieldUpdaters;
  Field fieldOptIns;

  public TestMembership7(String name) {
    super(name);
  }

  public void testCircularMembershipsWithoutComposites() {
    LOG.info("testCircularMembershipsWithoutComposites");
    try {
      GrouperUtil.sleep(100);
      before  = new Date();
      GrouperUtil.sleep(100);

      r     = R.populateRegistry(1, 5, 1);
      gA    = r.getGroup("a", "a");
      gB    = r.getGroup("a", "b");
      gC    = r.getGroup("a", "c");
      gD    = r.getGroup("a", "d");
      gE    = r.getGroup("a", "e");
      subjA = r.getSubject("a");

      fieldMembers = Group.getDefaultList();
      fieldUpdaters = FieldFinder.find("updaters", true);
      fieldOptIns = FieldFinder.find("optins", true);

      Set<Group> goodGroups = new LinkedHashSet<Group>();
      Set<Group> badGroups = new LinkedHashSet<Group>();

      goodGroups.add(gA);
      goodGroups.add(gB);
      goodGroups.add(gC);
      goodGroups.add(gD);
      goodGroups.add(gE);


      // Test 1:  Test when the last operation is adding update privilege for gC.
      gA.addMember(gB.toSubject());
      gB.addMember(gC.toSubject());
      gC.addMember(gA.toSubject());
      gD.addMember(gC.toSubject());
      gC.addMember(subjA);
      gB.grantPriv(gC.toSubject(), AccessPrivilege.OPTIN);
      gE.addMember(gD.toSubject());
      gD.grantPriv(gC.toSubject(), AccessPrivilege.UPDATE);
      gC.grantPriv(gA.toSubject(), AccessPrivilege.UPDATE);

      verifyMemberships();

      // clear out memberships
      gA.deleteMember(gB.toSubject());
      gB.deleteMember(gC.toSubject());
      gC.deleteMember(gA.toSubject());
      gD.deleteMember(gC.toSubject());
      gC.deleteMember(subjA);
      gB.revokePriv(gC.toSubject(), AccessPrivilege.OPTIN);
      gC.revokePriv(gA.toSubject(), AccessPrivilege.UPDATE);
      gE.deleteMember(gD.toSubject());
      gD.revokePriv(gC.toSubject(), AccessPrivilege.UPDATE);

      Set<Membership> listMemberships = MembershipFinder.internal_findAllByCreatedAfter(r.rs, before, fieldMembers);
      T.amount("Number of list memberships after test 1", 0, listMemberships.size());

      Set<Membership> updateMemberships = MembershipFinder.internal_findAllByCreatedAfter(r.rs, before, fieldUpdaters);
      T.amount("Number of update privileges after test 1", 0, updateMemberships.size());

      Set<Membership> optInMemberships = MembershipFinder.internal_findAllByCreatedAfter(r.rs, before, fieldOptIns);
      T.amount("Number of opt-in privileges after test 1", 0, optInMemberships.size());

      // Test 2:  Test when the last operation is adding gB as a member of gA.
      gB.addMember(gC.toSubject());
      gC.addMember(gA.toSubject());
      gD.addMember(gC.toSubject());
      gC.addMember(subjA);
      gB.grantPriv(gC.toSubject(), AccessPrivilege.OPTIN);
      gE.addMember(gD.toSubject());
      gD.grantPriv(gC.toSubject(), AccessPrivilege.UPDATE);
      gC.grantPriv(gA.toSubject(), AccessPrivilege.UPDATE);
      gA.addMember(gB.toSubject());
  
      verifyMemberships();
  
      // clear out memberships
      gA.deleteMember(gB.toSubject());
      gB.deleteMember(gC.toSubject());
      gC.deleteMember(gA.toSubject());
      gD.deleteMember(gC.toSubject());
      gC.deleteMember(subjA);
      gB.revokePriv(gC.toSubject(), AccessPrivilege.OPTIN);
      gC.revokePriv(gA.toSubject(), AccessPrivilege.UPDATE);
      gE.deleteMember(gD.toSubject());
      gD.revokePriv(gC.toSubject(), AccessPrivilege.UPDATE);
  
      listMemberships = MembershipFinder.internal_findAllByCreatedAfter(r.rs, before, fieldMembers);
      T.amount("Number of list memberships after test 2", 0, listMemberships.size());
    
      updateMemberships = MembershipFinder.internal_findAllByCreatedAfter(r.rs, before, fieldUpdaters);
      T.amount("Number of update privileges after test 2", 0, updateMemberships.size());

      optInMemberships = MembershipFinder.internal_findAllByCreatedAfter(r.rs, before, fieldOptIns);
      T.amount("Number of opt-in privileges after test 2", 0, optInMemberships.size());

      // Test 3:  Test when the last operation is adding gC as a member of gB.
      gC.addMember(gA.toSubject());
      gD.addMember(gC.toSubject());
      gC.addMember(subjA);
      gB.grantPriv(gC.toSubject(), AccessPrivilege.OPTIN);
      gE.addMember(gD.toSubject());
      gD.grantPriv(gC.toSubject(), AccessPrivilege.UPDATE);
      gC.grantPriv(gA.toSubject(), AccessPrivilege.UPDATE);
      gA.addMember(gB.toSubject());
      gB.addMember(gC.toSubject());
  
      verifyMemberships();

      // clear out memberships
      gA.deleteMember(gB.toSubject());
      gB.deleteMember(gC.toSubject());
      gC.deleteMember(gA.toSubject());
      gD.deleteMember(gC.toSubject());
      gC.deleteMember(subjA);
      gB.revokePriv(gC.toSubject(), AccessPrivilege.OPTIN);
      gC.revokePriv(gA.toSubject(), AccessPrivilege.UPDATE);
      gE.deleteMember(gD.toSubject());
      gD.revokePriv(gC.toSubject(), AccessPrivilege.UPDATE);
  
      listMemberships = MembershipFinder.internal_findAllByCreatedAfter(r.rs, before, fieldMembers);
      T.amount("Number of list memberships after test 3", 0, listMemberships.size());
    
      updateMemberships = MembershipFinder.internal_findAllByCreatedAfter(r.rs, before, fieldUpdaters);
      T.amount("Number of update privileges after test 3", 0, updateMemberships.size());

      optInMemberships = MembershipFinder.internal_findAllByCreatedAfter(r.rs, before, fieldOptIns);
      T.amount("Number of opt-in privileges after test 3", 0, optInMemberships.size());

      // Test 4:  Test when the last operation is adding gA as a member of gC.
      gD.addMember(gC.toSubject());
      gC.addMember(subjA);
      gB.grantPriv(gC.toSubject(), AccessPrivilege.OPTIN);
      gE.addMember(gD.toSubject());
      gD.grantPriv(gC.toSubject(), AccessPrivilege.UPDATE);
      gC.grantPriv(gA.toSubject(), AccessPrivilege.UPDATE);
      gA.addMember(gB.toSubject());
      gB.addMember(gC.toSubject());
      gC.addMember(gA.toSubject());
  
      verifyMemberships();
  
      // clear out memberships
      gA.deleteMember(gB.toSubject());
      gB.deleteMember(gC.toSubject());
      gC.deleteMember(gA.toSubject());
      gD.deleteMember(gC.toSubject());
      gC.deleteMember(subjA);
      gB.revokePriv(gC.toSubject(), AccessPrivilege.OPTIN);
      gC.revokePriv(gA.toSubject(), AccessPrivilege.UPDATE);
      gE.deleteMember(gD.toSubject());
      gD.revokePriv(gC.toSubject(), AccessPrivilege.UPDATE);
  
      listMemberships = MembershipFinder.internal_findAllByCreatedAfter(r.rs, before, fieldMembers);
      T.amount("Number of list memberships after test 4", 0, listMemberships.size());
    
      updateMemberships = MembershipFinder.internal_findAllByCreatedAfter(r.rs, before, fieldUpdaters);
      T.amount("Number of update privileges after test 4", 0, updateMemberships.size());

      optInMemberships = MembershipFinder.internal_findAllByCreatedAfter(r.rs, before, fieldOptIns);
      T.amount("Number of opt-in privileges after test 4", 0, optInMemberships.size());

      // Test 5:  Test when the last operation is adding gC as a member of gD.
      gC.addMember(subjA);
      gB.grantPriv(gC.toSubject(), AccessPrivilege.OPTIN);
      gE.addMember(gD.toSubject());
      gD.grantPriv(gC.toSubject(), AccessPrivilege.UPDATE);
      gC.grantPriv(gA.toSubject(), AccessPrivilege.UPDATE);
      gA.addMember(gB.toSubject());
      gB.addMember(gC.toSubject());
      gC.addMember(gA.toSubject());
      gD.addMember(gC.toSubject());
  
      verifyMemberships();
  
      // clear out memberships
      gA.deleteMember(gB.toSubject());
      gB.deleteMember(gC.toSubject());
      gC.deleteMember(gA.toSubject());
      gD.deleteMember(gC.toSubject());
      gC.deleteMember(subjA);
      gB.revokePriv(gC.toSubject(), AccessPrivilege.OPTIN);
      gC.revokePriv(gA.toSubject(), AccessPrivilege.UPDATE);
      gE.deleteMember(gD.toSubject());
      gD.revokePriv(gC.toSubject(), AccessPrivilege.UPDATE);
  
      listMemberships = MembershipFinder.internal_findAllByCreatedAfter(r.rs, before, fieldMembers);
      T.amount("Number of list memberships after test 5", 0, listMemberships.size());
    
      updateMemberships = MembershipFinder.internal_findAllByCreatedAfter(r.rs, before, fieldUpdaters);
      T.amount("Number of update privileges after test 5", 0, updateMemberships.size());

      optInMemberships = MembershipFinder.internal_findAllByCreatedAfter(r.rs, before, fieldOptIns);
      T.amount("Number of opt-in privileges after test 5", 0, optInMemberships.size());

      // Test 6:  Test when the last operation is adding subjA as a member of gC.
      gB.grantPriv(gC.toSubject(), AccessPrivilege.OPTIN);
      gE.addMember(gD.toSubject());
      gD.grantPriv(gC.toSubject(), AccessPrivilege.UPDATE);
      gC.grantPriv(gA.toSubject(), AccessPrivilege.UPDATE);
      gA.addMember(gB.toSubject());
      gB.addMember(gC.toSubject());
      gC.addMember(gA.toSubject());
      gD.addMember(gC.toSubject());
      gC.addMember(subjA);
  
      verifyMemberships();
  
      // clear out memberships
      gA.deleteMember(gB.toSubject());
      gB.deleteMember(gC.toSubject());
      gC.deleteMember(gA.toSubject());
      gD.deleteMember(gC.toSubject());
      gC.deleteMember(subjA);
      gB.revokePriv(gC.toSubject(), AccessPrivilege.OPTIN);
      gC.revokePriv(gA.toSubject(), AccessPrivilege.UPDATE);
      gE.deleteMember(gD.toSubject());
      gD.revokePriv(gC.toSubject(), AccessPrivilege.UPDATE);
  
      listMemberships = MembershipFinder.internal_findAllByCreatedAfter(r.rs, before, fieldMembers);
      T.amount("Number of list memberships after test 6", 0, listMemberships.size());
    
      updateMemberships = MembershipFinder.internal_findAllByCreatedAfter(r.rs, before, fieldUpdaters);
      T.amount("Number of update privileges after test 6", 0, updateMemberships.size());

      optInMemberships = MembershipFinder.internal_findAllByCreatedAfter(r.rs, before, fieldOptIns);
      T.amount("Number of opt-in privileges after test 6", 0, optInMemberships.size());

      // Test 7:  Test when the last operation is adding opt-in privileges for gB.
      gE.addMember(gD.toSubject());
      gD.grantPriv(gC.toSubject(), AccessPrivilege.UPDATE);
      gC.grantPriv(gA.toSubject(), AccessPrivilege.UPDATE);
      gA.addMember(gB.toSubject());
      gB.addMember(gC.toSubject());
      gC.addMember(gA.toSubject());
      gD.addMember(gC.toSubject());
      gC.addMember(subjA);
      gB.grantPriv(gC.toSubject(), AccessPrivilege.OPTIN);
  
      verifyMemberships();
  
      // clear out memberships
      gA.deleteMember(gB.toSubject());
      gB.deleteMember(gC.toSubject());
      gC.deleteMember(gA.toSubject());
      gD.deleteMember(gC.toSubject());
      gC.deleteMember(subjA);
      gB.revokePriv(gC.toSubject(), AccessPrivilege.OPTIN);
      gC.revokePriv(gA.toSubject(), AccessPrivilege.UPDATE);
      gE.deleteMember(gD.toSubject());
      gD.revokePriv(gC.toSubject(), AccessPrivilege.UPDATE);
  
      listMemberships = MembershipFinder.internal_findAllByCreatedAfter(r.rs, before, fieldMembers);
      T.amount("Number of list memberships after test 7", 0, listMemberships.size());
    
      updateMemberships = MembershipFinder.internal_findAllByCreatedAfter(r.rs, before, fieldUpdaters);
      T.amount("Number of update privileges after test 7", 0, updateMemberships.size());

      optInMemberships = MembershipFinder.internal_findAllByCreatedAfter(r.rs, before, fieldOptIns);
      T.amount("Number of opt-in privileges after test 7", 0, optInMemberships.size());

      // Test 8:  Test when the last operation is adding gD as a member of gE.
      gD.grantPriv(gC.toSubject(), AccessPrivilege.UPDATE);
      gC.grantPriv(gA.toSubject(), AccessPrivilege.UPDATE);
      gA.addMember(gB.toSubject());
      gB.addMember(gC.toSubject());
      gC.addMember(gA.toSubject());
      gD.addMember(gC.toSubject());
      gC.addMember(subjA);
      gB.grantPriv(gC.toSubject(), AccessPrivilege.OPTIN);
      gE.addMember(gD.toSubject());
 
      verifyMemberships();
 
      // clear out memberships
      gA.deleteMember(gB.toSubject());
      gB.deleteMember(gC.toSubject());
      gC.deleteMember(gA.toSubject());
      gD.deleteMember(gC.toSubject());
      gC.deleteMember(subjA);
      gB.revokePriv(gC.toSubject(), AccessPrivilege.OPTIN);
      gC.revokePriv(gA.toSubject(), AccessPrivilege.UPDATE);
      gE.deleteMember(gD.toSubject());
      gD.revokePriv(gC.toSubject(), AccessPrivilege.UPDATE);

      listMemberships = MembershipFinder.internal_findAllByCreatedAfter(r.rs, before, fieldMembers);
      T.amount("Number of list memberships after test 8", 0, listMemberships.size());

      updateMemberships = MembershipFinder.internal_findAllByCreatedAfter(r.rs, before, fieldUpdaters);
      T.amount("Number of update privileges after test 8", 0, updateMemberships.size());

      optInMemberships = MembershipFinder.internal_findAllByCreatedAfter(r.rs, before, fieldOptIns);
      T.amount("Number of opt-in privileges after test 8", 0, optInMemberships.size());

      // Test 9:  Test when the last operation is adding update privileges to gD.
      gC.grantPriv(gA.toSubject(), AccessPrivilege.UPDATE);
      gA.addMember(gB.toSubject());
      gB.addMember(gC.toSubject());
      gC.addMember(gA.toSubject());
      gD.addMember(gC.toSubject());
      gC.addMember(subjA);
      gB.grantPriv(gC.toSubject(), AccessPrivilege.OPTIN);
      gE.addMember(gD.toSubject());
      gD.grantPriv(gC.toSubject(), AccessPrivilege.UPDATE);

      verifyMemberships();
    
      // clear out memberships
      gA.deleteMember(gB.toSubject());
      gB.deleteMember(gC.toSubject());
      gC.deleteMember(gA.toSubject());
      gD.deleteMember(gC.toSubject());
      gC.deleteMember(subjA);
      gB.revokePriv(gC.toSubject(), AccessPrivilege.OPTIN);
      gC.revokePriv(gA.toSubject(), AccessPrivilege.UPDATE);
      gE.deleteMember(gD.toSubject());
      gD.revokePriv(gC.toSubject(), AccessPrivilege.UPDATE);
    
      listMemberships = MembershipFinder.internal_findAllByCreatedAfter(r.rs, before, fieldMembers);
      T.amount("Number of list memberships after test 9", 0, listMemberships.size());
    
      updateMemberships = MembershipFinder.internal_findAllByCreatedAfter(r.rs, before, fieldUpdaters);
      T.amount("Number of update privileges after test 9", 0, updateMemberships.size());

      optInMemberships = MembershipFinder.internal_findAllByCreatedAfter(r.rs, before, fieldOptIns);
      T.amount("Number of opt-in privileges after test 9", 0, optInMemberships.size());



      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  }


  public  void verifyMemberships() throws Exception {
    // gB -> gA
    MembershipTestHelper.verifyImmediateMembership(r.rs, "gB -> gA", gA, gB.toSubject(), fieldMembers);

    // gC -> gA (parent: gB -> gA) (depth: 1)
    MembershipTestHelper.verifyEffectiveMembership(r.rs, "gC -> gA", gA, gC.toSubject(), gB, 1, gA, gB.toSubject(), null, 0, fieldMembers);

    // SA -> gA (parent: gC -> gA) (depth: 2)
    MembershipTestHelper.verifyEffectiveMembership(r.rs, "SA -> gA", gA, subjA, gC, 2, gA, gC.toSubject(), gB, 1, fieldMembers);

    // gC -> gB
    MembershipTestHelper.verifyImmediateMembership(r.rs, "gC -> gB", gB, gC.toSubject(), fieldMembers);

    // gC -> gB
    MembershipTestHelper.verifyImmediateMembership(r.rs, "gC -> gB", gB, gC.toSubject(), fieldOptIns);

    // gA -> gB (parent: gC -> gB) (depth: 1)
    MembershipTestHelper.verifyEffectiveMembership(r.rs, "gA -> gB", gB, gA.toSubject(), gC, 1, gB, gC.toSubject(), null, 0, fieldMembers);

    // SA -> gB (parent: gC -> gB) (depth: 1)
    MembershipTestHelper.verifyEffectiveMembership(r.rs, "SA -> gB", gB, subjA, gC, 1, gB, gC.toSubject(), null, 0, fieldMembers);

    // gA -> gB (parent: gC -> gB) (depth: 1)
    MembershipTestHelper.verifyEffectiveMembership(r.rs, "gA -> gB", gB, gA.toSubject(), gC, 1, gB, gC.toSubject(), null, 0, fieldOptIns);

    // SA -> gB (parent: gC -> gB) (depth: 1)
    MembershipTestHelper.verifyEffectiveMembership(r.rs, "SA -> gB", gB, subjA, gC, 1, gB, gC.toSubject(), null, 0, fieldOptIns);

    // gB -> gB (parent: gA -> gB) (depth: 2)
    MembershipTestHelper.verifyEffectiveMembership(r.rs, "gB -> gB", gB, gB.toSubject(), gA, 2, gB, gA.toSubject(), gC, 1, fieldOptIns);

    // gA -> gC
    MembershipTestHelper.verifyImmediateMembership(r.rs, "gA -> gC", gC, gA.toSubject(), fieldMembers);

    // SA -> gC
    MembershipTestHelper.verifyImmediateMembership(r.rs, "SA -> gC", gC, subjA, fieldMembers);

    // gA -> gC
    MembershipTestHelper.verifyImmediateMembership(r.rs, "gA -> gC", gC, gA.toSubject(), fieldUpdaters);

    // gB -> gC (parent: gA -> gC) (depth: 1)
    MembershipTestHelper.verifyEffectiveMembership(r.rs, "gB -> gC", gC, gB.toSubject(), gA, 1, gC, gA.toSubject(), null, 0, fieldMembers);

    // gB -> gC (parent: gA -> gC) (depth: 1)
    MembershipTestHelper.verifyEffectiveMembership(r.rs, "gB -> gC", gC, gB.toSubject(), gA, 1, gC, gA.toSubject(), null, 0, fieldUpdaters);

    // gC -> gC (parent: gB -> gC) (depth: 2)
    MembershipTestHelper.verifyEffectiveMembership(r.rs, "gC -> gC", gC, gC.toSubject(), gB, 2, gC, gB.toSubject(), gA, 1, fieldUpdaters);

    // SA -> gC (parent: gC -> gC) (depth: 3)
    MembershipTestHelper.verifyEffectiveMembership(r.rs, "SA -> gC", gC, subjA, gC, 3, gC, gC.toSubject(), gB, 2, fieldUpdaters);

    // gC -> gD
    MembershipTestHelper.verifyImmediateMembership(r.rs, "gC -> gD", gD, gC.toSubject(), fieldMembers);

    // gA -> gD (parent: gC -> gD) (depth: 1)
    MembershipTestHelper.verifyEffectiveMembership(r.rs, "gA -> gD", gD, gA.toSubject(), gC, 1, gD, gC.toSubject(), null, 0, fieldMembers);

    // SA -> gD (parent: gC -> gD) (depth: 1)
    MembershipTestHelper.verifyEffectiveMembership(r.rs, "SA -> gD", gD, subjA, gC, 1, gD, gC.toSubject(), null, 0, fieldMembers);

    // gB -> gD (parent: gA -> gD) (depth: 2)
    MembershipTestHelper.verifyEffectiveMembership(r.rs, "gB -> gD", gD, gB.toSubject(), gA, 2, gD, gA.toSubject(), gC, 1, fieldMembers);

    // gC -> gD
    MembershipTestHelper.verifyImmediateMembership(r.rs, "gC -> gD", gD, gC.toSubject(), fieldUpdaters);
    
    // gA -> gD (parent: gC -> gD) (depth: 1)
    MembershipTestHelper.verifyEffectiveMembership(r.rs, "gA -> gD", gD, gA.toSubject(), gC, 1, gD, gC.toSubject(), null, 0, fieldUpdaters);
    
    // SA -> gD (parent: gC -> gD) (depth: 1)
    MembershipTestHelper.verifyEffectiveMembership(r.rs, "SA -> gD", gD, subjA, gC, 1, gD, gC.toSubject(), null, 0, fieldUpdaters);

    // gB -> gD (parent: gA -> gD) (depth: 2)
    MembershipTestHelper.verifyEffectiveMembership(r.rs, "gB -> gD", gD, gB.toSubject(), gA, 2, gD, gA.toSubject(), gC, 1, fieldUpdaters);

    // gD -> gE
    MembershipTestHelper.verifyImmediateMembership(r.rs, "gD -> gE", gE, gD.toSubject(), fieldMembers);

    // gC -> gE (parent: gD -> gE) (depth: 1)
    MembershipTestHelper.verifyEffectiveMembership(r.rs, "gC -> gE", gE, gC.toSubject(), gD, 1, gE, gD.toSubject(), null, 0, fieldMembers);

    // gA -> gE (parent: gC -> gE) (depth: 2)
    MembershipTestHelper.verifyEffectiveMembership(r.rs, "gA -> gE", gE, gA.toSubject(), gC, 2, gE, gC.toSubject(), gD, 1, fieldMembers);

    // SA -> gE (parent: gC -> gE) (depth: 2)
    MembershipTestHelper.verifyEffectiveMembership(r.rs, "SA -> gE", gE, subjA, gC, 2, gE, gC.toSubject(), gD, 1, fieldMembers);

    // gB -> gE (parent: gA -> gE) (depth: 3)
    MembershipTestHelper.verifyEffectiveMembership(r.rs, "gB -> gE", gE, gB.toSubject(), gA, 3, gE, gA.toSubject(), gC, 2, fieldMembers);


    // verify the total number of list memberships
    Set<Membership> listMemberships = MembershipFinder.internal_findAllByCreatedAfter(r.rs, before, fieldMembers);
    T.amount("Number of list memberships", 23, listMemberships.size());

    // verify the total number of update privileges
    Set<Membership> updateMemberships = MembershipFinder.internal_findAllByCreatedAfter(r.rs, before, fieldUpdaters);
    T.amount("Number of update privileges", 10, updateMemberships.size());

    // verify the total number of opt-in privileges
    Set<Membership> optInMemberships = MembershipFinder.internal_findAllByCreatedAfter(r.rs, before, fieldOptIns);
    T.amount("Number of opt-in privileges", 5, optInMemberships.size());
  }

}

