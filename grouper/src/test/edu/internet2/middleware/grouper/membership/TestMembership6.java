/**
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
 */
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
import edu.internet2.middleware.grouper.misc.CompositeType;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * @author Shilen Patel.
 */
public class TestMembership6 extends GrouperTest {

  private static final Log LOG = GrouperUtil.getLog(TestMembership6.class);

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
  Subject subjA;
  Subject subjB;
  Stem    nsA;

  Field fieldMembers;
  Field fieldUpdaters;
  Field fieldCreators;

  public TestMembership6(String name) {
    super(name);
  }

  public void testEffectiveMembershipsWithPrivilegesAndComposites() {
    LOG.info("testEffectiveMembershipsWithPrivilegesAndComposites");
    try {
      GrouperUtil.sleep(100);
      before  = new Date();
      GrouperUtil.sleep(100);

      r     = R.populateRegistry(2, 10, 2);
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
      subjA = r.getSubject("a");
      subjB = r.getSubject("b");
      nsA   = r.getStem("a");

      fieldMembers = Group.getDefaultList();
      fieldUpdaters = FieldFinder.find(Field.FIELD_NAME_UPDATERS, true);
      fieldCreators = FieldFinder.find(Field.FIELD_NAME_CREATORS, true);


      // Test 1:  Test when the last operations are adding update privileges for gH and gJ.
      nsA.grantPriv( gE.toSubject(), NamingPrivilege.CREATE );
      gA.addCompositeMember(CompositeType.UNION, gB, gC);
      gA.grantPriv( subjB, AccessPrivilege.UPDATE );
      gB.addMember( gD.toSubject() );
      gC.addMember( subjA );
      gE.addCompositeMember(CompositeType.UNION, gF, gA);
      gG.grantPriv( gE.toSubject(), AccessPrivilege.UPDATE );
      gI.addMember( gA.toSubject() );
      gH.grantPriv( gI.toSubject(), AccessPrivilege.UPDATE );
      gJ.grantPriv( gA.toSubject(), AccessPrivilege.UPDATE );

      verifyMemberships();

      // clear out memberships
      nsA.revokePriv( gE.toSubject(), NamingPrivilege.CREATE );
      gA.deleteCompositeMember();
      gA.revokePriv( subjB, AccessPrivilege.UPDATE );
      gB.deleteMember( gD.toSubject() );
      gC.deleteMember( subjA );
      gE.deleteCompositeMember();
      gG.revokePriv( gE.toSubject(), AccessPrivilege.UPDATE );
      gI.deleteMember( gA.toSubject() );
      gH.revokePriv( gI.toSubject(), AccessPrivilege.UPDATE );
      gJ.revokePriv( gA.toSubject(), AccessPrivilege.UPDATE );

      Set<Membership> listMemberships = MembershipFinder.internal_findAllByCreatedAfter(r.rs, before, fieldMembers);
      T.amount("Number of list memberships after test 1", 0, listMemberships.size());

      Set<Membership> updateMemberships = MembershipFinder.internal_findAllByCreatedAfter(r.rs, before, fieldUpdaters);
      T.amount("Number of update privileges after test 1", 0, updateMemberships.size());

      Set<Membership> createMemberships = MembershipFinder.internal_findAllByCreatedAfter(r.rs, before, fieldCreators);
      T.amount("Number of create privileges after test 1", 0, createMemberships.size());


      // Test 2:  Test when the last operation is adding the composite for gA.
      nsA.grantPriv( gE.toSubject(), NamingPrivilege.CREATE );
      gA.grantPriv( subjB, AccessPrivilege.UPDATE );
      gB.addMember( gD.toSubject() );
      gC.addMember( subjA );
      gE.addCompositeMember(CompositeType.UNION, gF, gA);
      gG.grantPriv( gE.toSubject(), AccessPrivilege.UPDATE );
      gH.grantPriv( gI.toSubject(), AccessPrivilege.UPDATE );
      gI.addMember( gA.toSubject() );
      gJ.grantPriv( gA.toSubject(), AccessPrivilege.UPDATE );
      gA.addCompositeMember(CompositeType.UNION, gB, gC);

      verifyMemberships();

      // clear out memberships
      nsA.revokePriv( gE.toSubject(), NamingPrivilege.CREATE );
      gA.deleteCompositeMember();
      gA.revokePriv( subjB, AccessPrivilege.UPDATE );
      gB.deleteMember( gD.toSubject() );
      gC.deleteMember( subjA );
      gE.deleteCompositeMember();
      gG.revokePriv( gE.toSubject(), AccessPrivilege.UPDATE );
      gI.deleteMember( gA.toSubject() );
      gH.revokePriv( gI.toSubject(), AccessPrivilege.UPDATE );
      gJ.revokePriv( gA.toSubject(), AccessPrivilege.UPDATE );

      listMemberships = MembershipFinder.internal_findAllByCreatedAfter(r.rs, before, fieldMembers);
      T.amount("Number of list memberships after test 2", 0, listMemberships.size());

      updateMemberships = MembershipFinder.internal_findAllByCreatedAfter(r.rs, before, fieldUpdaters);
      T.amount("Number of update privileges after test 2", 0, updateMemberships.size());

      createMemberships = MembershipFinder.internal_findAllByCreatedAfter(r.rs, before, fieldCreators);
      T.amount("Number of create privileges after test 2", 0, createMemberships.size());


      // Test 3:  Test when the last operations are adding members to gB and gC.
      nsA.grantPriv( gE.toSubject(), NamingPrivilege.CREATE );
      gA.addCompositeMember(CompositeType.UNION, gB, gC);
      gA.grantPriv( subjB, AccessPrivilege.UPDATE );
      gE.addCompositeMember(CompositeType.UNION, gF, gA);
      gG.grantPriv( gE.toSubject(), AccessPrivilege.UPDATE );
      gH.grantPriv( gI.toSubject(), AccessPrivilege.UPDATE );
      gI.addMember( gA.toSubject() );
      gJ.grantPriv( gA.toSubject(), AccessPrivilege.UPDATE );
      gB.addMember( gD.toSubject() );
      gC.addMember( subjA );

      verifyMemberships();

      // clear out memberships
      nsA.revokePriv( gE.toSubject(), NamingPrivilege.CREATE );
      gA.deleteCompositeMember();
      gA.revokePriv( subjB, AccessPrivilege.UPDATE );
      gB.deleteMember( gD.toSubject() );
      gC.deleteMember( subjA );
      gE.deleteCompositeMember();
      gG.revokePriv( gE.toSubject(), AccessPrivilege.UPDATE );
      gI.deleteMember( gA.toSubject() );
      gH.revokePriv( gI.toSubject(), AccessPrivilege.UPDATE );
      gJ.revokePriv( gA.toSubject(), AccessPrivilege.UPDATE );

      listMemberships = MembershipFinder.internal_findAllByCreatedAfter(r.rs, before, fieldMembers);
      T.amount("Number of list memberships after test 3", 0, listMemberships.size());

      updateMemberships = MembershipFinder.internal_findAllByCreatedAfter(r.rs, before, fieldUpdaters);
      T.amount("Number of update privileges after test 3", 0, updateMemberships.size());

      createMemberships = MembershipFinder.internal_findAllByCreatedAfter(r.rs, before, fieldCreators);
      T.amount("Number of create privileges after test 3", 0, createMemberships.size());


      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  }


  public  void verifyMemberships() throws Exception {
    // SA -> gA
    MembershipTestHelper.verifyCompositeMembership(r.rs, "SA -> gA", gA, subjA);

    // SB -> gA
    MembershipTestHelper.verifyImmediateMembership(r.rs, "SB -> gA", gA, subjB, fieldUpdaters);

    // gD -> gB
    MembershipTestHelper.verifyImmediateMembership(r.rs, "gD -> gB", gB, gD.toSubject(), fieldMembers);

    // SA -> gC
    MembershipTestHelper.verifyImmediateMembership(r.rs, "SA -> gC", gC, subjA, fieldMembers);

    // SA -> gE
    MembershipTestHelper.verifyCompositeMembership(r.rs, "SA -> gE", gE, subjA);

    // gE -> gG
    MembershipTestHelper.verifyImmediateMembership(r.rs, "gE -> gG", gG, gE.toSubject(), fieldUpdaters);

    // SA -> gG (parent: gE -> gG) (depth: 1)
    MembershipTestHelper.verifyEffectiveMembership(r.rs, "SA -> gG", gG, subjA, gE, 1, gG, gE.toSubject(), null, 0, fieldUpdaters);

    // gI -> gH
    MembershipTestHelper.verifyImmediateMembership(r.rs, "gI -> gH", gH, gI.toSubject(), fieldUpdaters);

    // gA -> gH (parent: gI -> gH) (depth: 1)
    MembershipTestHelper.verifyEffectiveMembership(r.rs, "gA -> gH", gH, gA.toSubject(), gI, 1, gH, gI.toSubject(), null, 0, fieldUpdaters);

    // SA -> gH (parent: gA -> gH) (depth: 2)
    MembershipTestHelper.verifyEffectiveMembership(r.rs, "SA -> gH", gH, subjA, gA, 2, gH, gA.toSubject(), gI, 1, fieldUpdaters);

    // gA -> gI
    MembershipTestHelper.verifyImmediateMembership(r.rs, "gA -> gI", gI, gA.toSubject(), fieldMembers);

    // SA -> gI (parent: gA -> gI) (depth: 1)
    MembershipTestHelper.verifyEffectiveMembership(r.rs, "SA -> gI", gI, subjA, gA, 1, gI, gA.toSubject(), null, 0, fieldMembers);

    // gA -> gJ
    MembershipTestHelper.verifyImmediateMembership(r.rs, "gA -> gJ", gJ, gA.toSubject(), fieldUpdaters);

    // SA -> gJ (parent: gA -> gJ) (depth: 1)
    MembershipTestHelper.verifyEffectiveMembership(r.rs, "SA -> gJ", gJ, subjA, gA, 1, gJ, gA.toSubject(), null, 0, fieldUpdaters); 

    // gE -> nsA
    MembershipTestHelper.verifyImmediateMembership(r.rs, "gE -> nsA", nsA, gE.toSubject(), fieldCreators);

    // SA -> nsA (parent: gE -> nsA) (depth: 1)
    MembershipTestHelper.verifyEffectiveMembership(r.rs, "SA -> nsA", nsA, subjA, gE, 1, nsA, gE.toSubject(), null, 0, fieldCreators); 


    // verify the total number of list memberships
    Set<Membership> listMemberships = MembershipFinder.internal_findAllByCreatedAfter(r.rs, before, fieldMembers);
    T.amount("Number of list memberships", 6, listMemberships.size());

    // verify the total number of update privileges
    Set<Membership> updateMemberships = MembershipFinder.internal_findAllByCreatedAfter(r.rs, before, fieldUpdaters);
    T.amount("Number of update privileges", 8, updateMemberships.size());

    // verify the total number of create privileges
    Set<Membership> createMemberships = MembershipFinder.internal_findAllByCreatedAfter(r.rs, before, fieldCreators);
    T.amount("Number of create privileges", 2, createMemberships.size());
  }

}

