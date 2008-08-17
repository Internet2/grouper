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
import  edu.internet2.middleware.subject.*;
import  java.util.*;
import  junit.framework.*;
import  org.apache.commons.logging.*;

/**
 * @author Shilen Patel.
 */
public class TestMembership5 extends TestCase {

  private static final Log LOG = LogFactory.getLog(TestMembership5.class);

  Date before;
  R       r;
  Group   gA;
  Group   gB;
  Group   gC;
  Group   gD;
  Group   gE;
  Group   gF;
  Group   gG;
  Group   gH;
  Group   gI;
  Group   gJ;
  Group   gK;
  Subject subjA;
  Subject subjB;
  Subject subjC;
  Subject subjD;

  Field fieldMembers;
  Field fieldUpdaters;

  public TestMembership5(String name) {
    super(name);
  }

  protected void setUp () {
    LOG.debug("setUp");
    RegistryReset.reset();
  }

  protected void tearDown () {
    LOG.debug("tearDown");
  }

  public void testEffectiveMemberships() {
    LOG.info("testEffectiveMembershipsWithPrivileges");
    try {
      before   = DateHelper.getPastDate();

      r     = R.populateRegistry(1, 11, 4);
      gA    = r.getGroup("a", "a");
      gB    = r.getGroup("a", "b");
      gC    = r.getGroup("a", "c");
      gD    = r.getGroup("a", "d");
      gE    = r.getGroup("a", "e");
      gF    = r.getGroup("a", "f");
      gG    = r.getGroup("a", "g");
      gH    = r.getGroup("a", "h");
      gI    = r.getGroup("a", "i");
      gJ    = r.getGroup("a", "j");
      gK    = r.getGroup("a", "k");
      subjA = r.getSubject("a");
      subjB = r.getSubject("b");
      subjC = r.getSubject("c");
      subjD = r.getSubject("d");

      fieldMembers = Group.getDefaultList();
      fieldUpdaters = FieldFinder.find("updaters");


      // Test 1:  Test when the last operations are adding update privileges for gA.
      gB.addMember( subjB );
      gB.addMember( gD.toSubject() );
      gB.addMember( gE.toSubject() );
      gB.grantPriv( gC.toSubject(), AccessPrivilege.UPDATE );
      gD.addMember( subjD );
      gD.addMember( gF.toSubject() );
      gE.grantPriv( subjC, AccessPrivilege.UPDATE );
      gE.grantPriv( gG.toSubject(), AccessPrivilege.UPDATE );
      gH.addMember( gB.toSubject() );
      gI.grantPriv( gH.toSubject(), AccessPrivilege.UPDATE );
      gJ.addMember( gA.toSubject() );
      gK.grantPriv( gA.toSubject(), AccessPrivilege.UPDATE );
      gA.grantPriv( subjA, AccessPrivilege.UPDATE );
      gA.grantPriv( gB.toSubject(), AccessPrivilege.UPDATE );

      verifyMemberships();


      // clear out memberships
      gB.deleteMember( subjB );
      gB.deleteMember( gD.toSubject() );
      gB.deleteMember( gE.toSubject() );
      gB.revokePriv( gC.toSubject(), AccessPrivilege.UPDATE );
      gD.deleteMember( subjD );
      gD.deleteMember( gF.toSubject() );
      gE.revokePriv( subjC, AccessPrivilege.UPDATE );
      gE.revokePriv( gG.toSubject(), AccessPrivilege.UPDATE );
      gH.deleteMember( gB.toSubject() );
      gI.revokePriv( gH.toSubject(), AccessPrivilege.UPDATE );
      gJ.deleteMember( gA.toSubject() );
      gK.revokePriv( gA.toSubject(), AccessPrivilege.UPDATE );
      gA.revokePriv( subjA, AccessPrivilege.UPDATE );
      gA.revokePriv( gB.toSubject(), AccessPrivilege.UPDATE );

      Set<Membership> listMemberships = MembershipFinder.internal_findAllByCreatedAfter(r.rs, before, fieldMembers);
      T.amount("Number of list memberships", 0, listMemberships.size());

      Set<Membership> updateMemberships = MembershipFinder.internal_findAllByCreatedAfter(r.rs, before, fieldUpdaters);
      T.amount("Number of update privileges", 0, updateMemberships.size());



      // Test 2:  Test when the last operations are adding list memberships to gB.
      gA.grantPriv( subjA, AccessPrivilege.UPDATE );
      gA.grantPriv( gB.toSubject(), AccessPrivilege.UPDATE );
      gB.grantPriv( gC.toSubject(), AccessPrivilege.UPDATE );
      gD.addMember( subjD );
      gD.addMember( gF.toSubject() );
      gE.grantPriv( subjC, AccessPrivilege.UPDATE );
      gE.grantPriv( gG.toSubject(), AccessPrivilege.UPDATE );
      gH.addMember( gB.toSubject() );
      gI.grantPriv( gH.toSubject(), AccessPrivilege.UPDATE );
      gJ.addMember( gA.toSubject() );
      gK.grantPriv( gA.toSubject(), AccessPrivilege.UPDATE );
      gB.addMember( subjB );
      gB.addMember( gD.toSubject() );
      gB.addMember( gE.toSubject() );

      verifyMemberships();

      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  }


  public  void verifyMemberships() throws Exception {
    // SA -> gA
    verifyImmediateMembership(r.rs, "SA -> gA", gA, subjA, fieldUpdaters);

    // gB -> gA
    verifyImmediateMembership(r.rs, "gB -> gA", gA, gB.toSubject(), fieldUpdaters);

    // gD -> gA (parent: gB -> gA) (depth: 1)
    verifyEffectiveMembership(r.rs, "gD -> gA", gA, gD.toSubject(), gB, 1, gA, gB.toSubject(), null, 0, fieldUpdaters);

    // gE -> gA (parent: gB -> gA) (depth: 1)
    verifyEffectiveMembership(r.rs, "gE -> gA", gA, gE.toSubject(), gB, 1, gA, gB.toSubject(), null, 0, fieldUpdaters);

    // SB -> gA (parent: gB -> gA) (depth: 1)
    verifyEffectiveMembership(r.rs, "SB -> gA", gA, subjB, gB, 1, gA, gB.toSubject(), null, 0, fieldUpdaters);

    // SD -> gA (parent: gD -> gA) (depth: 2)
    verifyEffectiveMembership(r.rs, "SD -> gA", gA, subjD, gD, 2, gA, gD.toSubject(), gB, 1, fieldUpdaters);

    // gF -> gA (parent: gD -> gA) (depth: 2)
    verifyEffectiveMembership(r.rs, "gF -> gA", gA, gF.toSubject(), gD, 2, gA, gD.toSubject(), gB, 1, fieldUpdaters);

    // gC -> gB
    verifyImmediateMembership(r.rs, "gC -> gB", gB, gC.toSubject(), fieldUpdaters);

    // gD -> gB
    verifyImmediateMembership(r.rs, "gD -> gB", gB, gD.toSubject(), fieldMembers);

    // gE -> gB
    verifyImmediateMembership(r.rs, "gE -> gB", gB, gE.toSubject(), fieldMembers);

    // SB -> gB
    verifyImmediateMembership(r.rs, "SB -> gB", gB, subjB, fieldMembers);

    // gF -> gB (parent: gD -> gB) (depth: 1)
    verifyEffectiveMembership(r.rs, "gF -> gB", gB, gF.toSubject(), gD, 1, gB, gD.toSubject(), null, 0, fieldMembers);

    // SD -> gB (parent: gD -> gB) (depth: 1)
    verifyEffectiveMembership(r.rs, "SD -> gB", gB, subjD, gD, 1, gB, gD.toSubject(), null, 0, fieldMembers);

    // gF -> gD
    verifyImmediateMembership(r.rs, "gF -> gD", gD, gF.toSubject(), fieldMembers);

    // SD -> gD
    verifyImmediateMembership(r.rs, "SD -> gD", gD, subjD, fieldMembers);

    // SC -> gE
    verifyImmediateMembership(r.rs, "SC -> gE", gE, subjC, fieldUpdaters);

    // gG -> gE
    verifyImmediateMembership(r.rs, "gG -> gE", gE, gG.toSubject(), fieldUpdaters);

    // gB -> gH
    verifyImmediateMembership(r.rs, "gB -> gH", gH, gB.toSubject(), fieldMembers);

    // gD -> gH (parent: gB -> gH) (depth: 1)
    verifyEffectiveMembership(r.rs, "gD -> gH", gH, gD.toSubject(), gB, 1, gH, gB.toSubject(), null, 0, fieldMembers);

    // gE -> gH (parent: gB -> gH) (depth: 1)
    verifyEffectiveMembership(r.rs, "gE -> gH", gH, gE.toSubject(), gB, 1, gH, gB.toSubject(), null, 0, fieldMembers);

    // SB -> gH (parent: gB -> gH) (depth: 1)
    verifyEffectiveMembership(r.rs, "SB -> gH", gH, subjB, gB, 1, gH, gB.toSubject(), null, 0, fieldMembers);

    // SD -> gH (parent: gD -> gH) (depth: 2)
    verifyEffectiveMembership(r.rs, "SD -> gH", gH, subjD, gD, 2, gH, gD.toSubject(), gB, 1, fieldMembers);

    // gF -> gH (parent: gD -> gH) (depth: 2)
    verifyEffectiveMembership(r.rs, "gF -> gH", gH, gF.toSubject(), gD, 2, gH, gD.toSubject(), gB, 1, fieldMembers);

    // gH -> gI
    verifyImmediateMembership(r.rs, "gH -> gI", gI, gH.toSubject(), fieldUpdaters);

    // gB -> gI (parent: gH -> gI) (depth: 1)
    verifyEffectiveMembership(r.rs, "gB -> gI", gI, gB.toSubject(), gH, 1, gI, gH.toSubject(), null, 0, fieldUpdaters);

    // gD -> gI (parent: gB -> gI) (depth: 2)
    verifyEffectiveMembership(r.rs, "gD -> gI", gI, gD.toSubject(), gB, 2, gI, gB.toSubject(), gH, 1, fieldUpdaters);

    // gE -> gI (parent: gB -> gI) (depth: 2)
    verifyEffectiveMembership(r.rs, "gE -> gI", gI, gE.toSubject(), gB, 2, gI, gB.toSubject(), gH, 1, fieldUpdaters);

    // SB -> gI (parent: gB -> gI) (depth: 2)
    verifyEffectiveMembership(r.rs, "SB -> gI", gI, subjB, gB, 2, gI, gB.toSubject(), gH, 1, fieldUpdaters);

    // SD -> gI (parent: gD -> gI) (depth: 3)
    verifyEffectiveMembership(r.rs, "SD -> gI", gI, subjD, gD, 3, gI, gD.toSubject(), gB, 2, fieldUpdaters);

    // gF -> gI (parent: gD -> gI) (depth: 3)
    verifyEffectiveMembership(r.rs, "gF -> gI", gI, gF.toSubject(), gD, 3, gI, gD.toSubject(), gB, 2, fieldUpdaters);

    // gA -> gJ
    verifyImmediateMembership(r.rs, "gA -> gJ", gJ, gA.toSubject(), fieldMembers);

    // gA -> gK
    verifyImmediateMembership(r.rs, "gA -> gK", gK, gA.toSubject(), fieldUpdaters);


    // verify the total number of list memberships
    Set<Membership> listMemberships = MembershipFinder.internal_findAllByCreatedAfter(r.rs, before, fieldMembers);
    T.amount("Number of list memberships", 14, listMemberships.size());

    // verify the total number of update privileges
    Set<Membership> updateMemberships = MembershipFinder.internal_findAllByCreatedAfter(r.rs, before, fieldUpdaters);
    T.amount("Number of update privileges", 18, updateMemberships.size());
  }

  private Membership findImmediateMembership(GrouperSession s, Group g, Subject subj, Field f) {

    Membership mship = null;

    try {
      mship = MembershipFinder.findImmediateMembership(s, g, subj, f);
    } catch (Exception e) {
      // do nothing
    }

    return mship;
  }

  private Membership findEffectiveMembership(GrouperSession s, Group g, Subject subj, Group via, int depth, Field f) {
    Membership mship = null;
    try {
      Set<Membership> memberships = MembershipFinder.findEffectiveMemberships(s, g, subj, 
        f, via, depth);

      Iterator<Membership> it = memberships.iterator();
      if (it.hasNext()) {
        mship = it.next();
      }
    } catch (Exception e) {
      // do nothing
    }

    return mship;
  }

  private void verifyImmediateMembership(GrouperSession s, String comment, Group childGroup, Subject childSubject, Field f) {

    // first verify that the membership exists
    Membership childMembership = findImmediateMembership(s, childGroup, childSubject, f);
    Assert.assertNotNull(comment + ": find membership", childMembership);

    
    // second verify that there's no via_id
    Group viaGroup = null;
    try {
      viaGroup = childMembership.getViaGroup();
    } catch (GroupNotFoundException e) {
      // do nothing
    }

    Assert.assertNull(comment + ": find via group", viaGroup);


    // third verify that there's no parent membership
    Membership parentMembership = null;
    try {
      parentMembership = childMembership.getParentMembership();
    } catch (MembershipNotFoundException e) {
      // do nothing
    }

    Assert.assertNull(comment + ": find parent membership", parentMembership);
  }

  private void verifyEffectiveMembership(GrouperSession s, String comment, Group childGroup, Subject childSubject, Group childVia, 
    int childDepth, Group parentGroup, Subject parentSubject, Group parentVia, int parentDepth, Field f) {

    // first verify that the membership exists
    Membership childMembership = findEffectiveMembership(s, childGroup, childSubject, childVia, childDepth, f);
    Assert.assertNotNull(comment + ": find membership", childMembership);


    // second verify that the parent membership exists based on data from the method parameters
    Membership parentMembership = null;
    if (parentDepth == 0) {
      parentMembership = findImmediateMembership(s, parentGroup, parentSubject, f);
    } else {
      parentMembership = findEffectiveMembership(s, parentGroup, parentSubject, parentVia, parentDepth, f);
    }

    Assert.assertNotNull(comment + ": find parent membership", parentMembership);


    // third verify that the parent membership of the child is correct
    boolean parentCheck = false;
    try {
      parentCheck = parentMembership.equals(childMembership.getParentMembership());
    } catch (MembershipNotFoundException e) {
      // do nothing
    }
    Assert.assertTrue(comment + ": verify parent membership", parentCheck);
  }

}

