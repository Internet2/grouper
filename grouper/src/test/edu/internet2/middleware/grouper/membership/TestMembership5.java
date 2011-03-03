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
import java.util.Set;

import junit.textui.TestRunner;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.FieldFinder;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.MembershipFinder;
import edu.internet2.middleware.grouper.exception.RevokePrivilegeException;
import edu.internet2.middleware.grouper.helper.DateHelper;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.MembershipTestHelper;
import edu.internet2.middleware.grouper.helper.R;
import edu.internet2.middleware.grouper.helper.T;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * @author Shilen Patel.
 */
public class TestMembership5 extends GrouperTest {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new TestMembership5("testEffectiveMembershipsWithPrivileges"));
  }
  
  /** logger */
  private static final Log LOG = GrouperUtil.getLog(TestMembership5.class);

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

  public void testEffectiveMembershipsWithPrivileges() {
    LOG.info("testEffectiveMembershipsWithPrivileges");
    try {
      GrouperUtil.sleep(100);
      before  = new Date();
      GrouperUtil.sleep(100);

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
      fieldUpdaters = FieldFinder.find("updaters", true);


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
      

      try {
        gB.revokePriv( gC.toSubject(), AccessPrivilege.UPDATE );
        fail("Should throw an exception about already existing");
      } catch (RevokePrivilegeException rpe) {
        //good
      }

//      gB.revokePriv( gC.toSubject(), AccessPrivilege.UPDATE );
      //shouldnt throw an exception
      
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
    MembershipTestHelper.verifyImmediateMembership(r.rs, "SA -> gA", gA, subjA, fieldUpdaters);

    // gB -> gA
    MembershipTestHelper.verifyImmediateMembership(r.rs, "gB -> gA", gA, gB.toSubject(), fieldUpdaters);

    // gD -> gA (parent: gB -> gA) (depth: 1)
    MembershipTestHelper.verifyEffectiveMembership(r.rs, "gD -> gA", gA, gD.toSubject(), gB, 1, gA, gB.toSubject(), null, 0, fieldUpdaters);

    // gE -> gA (parent: gB -> gA) (depth: 1)
    MembershipTestHelper.verifyEffectiveMembership(r.rs, "gE -> gA", gA, gE.toSubject(), gB, 1, gA, gB.toSubject(), null, 0, fieldUpdaters);

    // SB -> gA (parent: gB -> gA) (depth: 1)
    MembershipTestHelper.verifyEffectiveMembership(r.rs, "SB -> gA", gA, subjB, gB, 1, gA, gB.toSubject(), null, 0, fieldUpdaters);

    // SD -> gA (parent: gD -> gA) (depth: 2)
    MembershipTestHelper.verifyEffectiveMembership(r.rs, "SD -> gA", gA, subjD, gD, 2, gA, gD.toSubject(), gB, 1, fieldUpdaters);

    // gF -> gA (parent: gD -> gA) (depth: 2)
    MembershipTestHelper.verifyEffectiveMembership(r.rs, "gF -> gA", gA, gF.toSubject(), gD, 2, gA, gD.toSubject(), gB, 1, fieldUpdaters);

    // gC -> gB
    MembershipTestHelper.verifyImmediateMembership(r.rs, "gC -> gB", gB, gC.toSubject(), fieldUpdaters);

    // gD -> gB
    MembershipTestHelper.verifyImmediateMembership(r.rs, "gD -> gB", gB, gD.toSubject(), fieldMembers);

    // gE -> gB
    MembershipTestHelper.verifyImmediateMembership(r.rs, "gE -> gB", gB, gE.toSubject(), fieldMembers);

    // SB -> gB
    MembershipTestHelper.verifyImmediateMembership(r.rs, "SB -> gB", gB, subjB, fieldMembers);

    // gF -> gB (parent: gD -> gB) (depth: 1)
    MembershipTestHelper.verifyEffectiveMembership(r.rs, "gF -> gB", gB, gF.toSubject(), gD, 1, gB, gD.toSubject(), null, 0, fieldMembers);

    // SD -> gB (parent: gD -> gB) (depth: 1)
    MembershipTestHelper.verifyEffectiveMembership(r.rs, "SD -> gB", gB, subjD, gD, 1, gB, gD.toSubject(), null, 0, fieldMembers);

    // gF -> gD
    MembershipTestHelper.verifyImmediateMembership(r.rs, "gF -> gD", gD, gF.toSubject(), fieldMembers);

    // SD -> gD
    MembershipTestHelper.verifyImmediateMembership(r.rs, "SD -> gD", gD, subjD, fieldMembers);

    // SC -> gE
    MembershipTestHelper.verifyImmediateMembership(r.rs, "SC -> gE", gE, subjC, fieldUpdaters);

    // gG -> gE
    MembershipTestHelper.verifyImmediateMembership(r.rs, "gG -> gE", gE, gG.toSubject(), fieldUpdaters);

    // gB -> gH
    MembershipTestHelper.verifyImmediateMembership(r.rs, "gB -> gH", gH, gB.toSubject(), fieldMembers);

    // gD -> gH (parent: gB -> gH) (depth: 1)
    MembershipTestHelper.verifyEffectiveMembership(r.rs, "gD -> gH", gH, gD.toSubject(), gB, 1, gH, gB.toSubject(), null, 0, fieldMembers);

    // gE -> gH (parent: gB -> gH) (depth: 1)
    MembershipTestHelper.verifyEffectiveMembership(r.rs, "gE -> gH", gH, gE.toSubject(), gB, 1, gH, gB.toSubject(), null, 0, fieldMembers);

    // SB -> gH (parent: gB -> gH) (depth: 1)
    MembershipTestHelper.verifyEffectiveMembership(r.rs, "SB -> gH", gH, subjB, gB, 1, gH, gB.toSubject(), null, 0, fieldMembers);

    // SD -> gH (parent: gD -> gH) (depth: 2)
    MembershipTestHelper.verifyEffectiveMembership(r.rs, "SD -> gH", gH, subjD, gD, 2, gH, gD.toSubject(), gB, 1, fieldMembers);

    // gF -> gH (parent: gD -> gH) (depth: 2)
    MembershipTestHelper.verifyEffectiveMembership(r.rs, "gF -> gH", gH, gF.toSubject(), gD, 2, gH, gD.toSubject(), gB, 1, fieldMembers);

    // gH -> gI
    MembershipTestHelper.verifyImmediateMembership(r.rs, "gH -> gI", gI, gH.toSubject(), fieldUpdaters);

    // gB -> gI (parent: gH -> gI) (depth: 1)
    MembershipTestHelper.verifyEffectiveMembership(r.rs, "gB -> gI", gI, gB.toSubject(), gH, 1, gI, gH.toSubject(), null, 0, fieldUpdaters);

    // gD -> gI (parent: gB -> gI) (depth: 2)
    MembershipTestHelper.verifyEffectiveMembership(r.rs, "gD -> gI", gI, gD.toSubject(), gB, 2, gI, gB.toSubject(), gH, 1, fieldUpdaters);

    // gE -> gI (parent: gB -> gI) (depth: 2)
    MembershipTestHelper.verifyEffectiveMembership(r.rs, "gE -> gI", gI, gE.toSubject(), gB, 2, gI, gB.toSubject(), gH, 1, fieldUpdaters);

    // SB -> gI (parent: gB -> gI) (depth: 2)
    MembershipTestHelper.verifyEffectiveMembership(r.rs, "SB -> gI", gI, subjB, gB, 2, gI, gB.toSubject(), gH, 1, fieldUpdaters);

    // SD -> gI (parent: gD -> gI) (depth: 3)
    MembershipTestHelper.verifyEffectiveMembership(r.rs, "SD -> gI", gI, subjD, gD, 3, gI, gD.toSubject(), gB, 2, fieldUpdaters);

    // gF -> gI (parent: gD -> gI) (depth: 3)
    MembershipTestHelper.verifyEffectiveMembership(r.rs, "gF -> gI", gI, gF.toSubject(), gD, 3, gI, gD.toSubject(), gB, 2, fieldUpdaters);

    // gA -> gJ
    MembershipTestHelper.verifyImmediateMembership(r.rs, "gA -> gJ", gJ, gA.toSubject(), fieldMembers);

    // gA -> gK
    MembershipTestHelper.verifyImmediateMembership(r.rs, "gA -> gK", gK, gA.toSubject(), fieldUpdaters);


    // verify the total number of list memberships
    Set<Membership> listMemberships = MembershipFinder.internal_findAllByCreatedAfter(r.rs, before, fieldMembers);
    T.amount("Number of list memberships", 14, listMemberships.size());

    // verify the total number of update privileges
    Set<Membership> updateMemberships = MembershipFinder.internal_findAllByCreatedAfter(r.rs, before, fieldUpdaters);
    T.amount("Number of update privileges", 18, updateMemberships.size());
  }

}

