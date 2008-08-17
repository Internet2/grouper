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
import  edu.internet2.middleware.grouper.internal.dto.MembershipDTO;
import  java.util.*;
import  junit.framework.*;
import  org.apache.commons.logging.*;

/**
 * @author Shilen Patel.
 */
public class TestMembership6 extends TestCase {

  private static final Log LOG = LogFactory.getLog(TestMembership6.class);

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

  protected void setUp () {
    LOG.debug("setUp");
    RegistryReset.reset();
  }

  protected void tearDown () {
    LOG.debug("tearDown");
  }

  public void testEffectiveMemberships() {
    LOG.info("testEffectiveMembershipsWithPrivilegesAndComposites");
    try {
      before   = DateHelper.getPastDate();

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
      fieldUpdaters = FieldFinder.find("updaters");
      fieldCreators = FieldFinder.find("creators");


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
    verifyCompositeMembership(r.rs, "SA -> gA", gA, subjA);

    // gD -> gA
    verifyCompositeMembership(r.rs, "gD -> gA", gA, gD.toSubject());

    // SB -> gA
    verifyImmediateMembership(r.rs, "SB -> gA", gA, subjB, fieldUpdaters);

    // gD -> gB
    verifyImmediateMembership(r.rs, "gD -> gB", gB, gD.toSubject(), fieldMembers);

    // SA -> gC
    verifyImmediateMembership(r.rs, "SA -> gC", gC, subjA, fieldMembers);

    // SA -> gE
    verifyCompositeMembership(r.rs, "SA -> gE", gE, subjA);

    // gD -> gE
    verifyCompositeMembership(r.rs, "gD -> gE", gE, gD.toSubject());

    // gE -> gG
    verifyImmediateMembership(r.rs, "gE -> gG", gG, gE.toSubject(), fieldUpdaters);

    // SA -> gG (parent: gE -> gG) (depth: 1)
    verifyEffectiveMembership(r.rs, "SA -> gG", gG, subjA, gE, 1, gG, gE.toSubject(), null, 0, fieldUpdaters);

    // gD -> gG (parent: gE -> gG) (depth: 1)
    verifyEffectiveMembership(r.rs, "gD -> gG", gG, gD.toSubject(), gE, 1, gG, gE.toSubject(), null, 0, fieldUpdaters);

    // gI -> gH
    verifyImmediateMembership(r.rs, "gI -> gH", gH, gI.toSubject(), fieldUpdaters);

    // gA -> gH (parent: gI -> gH) (depth: 1)
    verifyEffectiveMembership(r.rs, "gA -> gH", gH, gA.toSubject(), gI, 1, gH, gI.toSubject(), null, 0, fieldUpdaters);

    // SA -> gH (parent: gA -> gH) (depth: 2)
    verifyEffectiveMembership(r.rs, "SA -> gH", gH, subjA, gA, 2, gH, gA.toSubject(), gI, 1, fieldUpdaters);

    // gD -> gH (parent: gA -> gH) (depth: 2)
    verifyEffectiveMembership(r.rs, "gD -> gH", gH, gD.toSubject(), gA, 2, gH, gA.toSubject(), gI, 1, fieldUpdaters);

    // gA -> gI
    verifyImmediateMembership(r.rs, "gA -> gI", gI, gA.toSubject(), fieldMembers);

    // SA -> gI (parent: gA -> gI) (depth: 1)
    verifyEffectiveMembership(r.rs, "SA -> gI", gI, subjA, gA, 1, gI, gA.toSubject(), null, 0, fieldMembers);

    // gD -> gI (parent: gA -> gI) (depth: 1)
    verifyEffectiveMembership(r.rs, "gD -> gI", gI, gD.toSubject(), gA, 1, gI, gA.toSubject(), null, 0, fieldMembers);

    // gA -> gJ
    verifyImmediateMembership(r.rs, "gA -> gJ", gJ, gA.toSubject(), fieldUpdaters);

    // SA -> gJ (parent: gA -> gJ) (depth: 1)
    verifyEffectiveMembership(r.rs, "SA -> gJ", gJ, subjA, gA, 1, gJ, gA.toSubject(), null, 0, fieldUpdaters); 

    // gD -> gJ (parent: gA -> gJ) (depth: 1)
    verifyEffectiveMembership(r.rs, "gD -> gJ", gJ, gD.toSubject(), gA, 1, gJ, gA.toSubject(), null, 0, fieldUpdaters); 

    // gE -> nsA
    verifyImmediateMembership(r.rs, "gE -> nsA", nsA, gE.toSubject(), fieldCreators);

    // SA -> nsA (parent: gE -> nsA) (depth: 1)
    verifyEffectiveMembership(r.rs, "SA -> nsA", nsA, subjA, gE, 1, nsA, gE.toSubject(), null, 0, fieldCreators); 

    // gD -> nsA (parent: gE -> nsA) (depth: 1)
    verifyEffectiveMembership(r.rs, "gD -> nsA", nsA, gD.toSubject(), gE, 1, nsA, gE.toSubject(), null, 0, fieldCreators); 


    // verify the total number of list memberships
    Set<Membership> listMemberships = MembershipFinder.internal_findAllByCreatedAfter(r.rs, before, fieldMembers);
    T.amount("Number of list memberships", 9, listMemberships.size());

    // verify the total number of update privileges
    Set<Membership> updateMemberships = MembershipFinder.internal_findAllByCreatedAfter(r.rs, before, fieldUpdaters);
    T.amount("Number of update privileges", 11, updateMemberships.size());

    // verify the total number of create privileges
    Set<Membership> createMemberships = MembershipFinder.internal_findAllByCreatedAfter(r.rs, before, fieldCreators);
    T.amount("Number of create privileges", 3, createMemberships.size());
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

  private Membership findImmediateMembership(GrouperSession s, Stem stem, Subject subj, Field f) {

    Membership mship = null;

    try {
      Member m = MemberFinder.findBySubject(s, subj);
      mship = new Membership();
      mship.setDTO(
        GrouperDAOFactory.getFactory().getMembership().findByOwnerAndMemberAndFieldAndType(
          stem.getUuid(), m.getUuid(), f, Membership.IMMEDIATE
        )
      );
      mship.setSession(s);
    } catch (Exception e) {
      mship = null;
    }

    return mship;
  }

  private Membership findCompositeMembership(GrouperSession s, Group g, Subject subj) {

    Membership mship = null;
      
    try {
      mship = MembershipFinder.findCompositeMembership(s, g, subj);
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

  private Membership findEffectiveMembership(GrouperSession s, Stem stem, Subject subj, Group via, int depth, Field f) {

    Membership mship = null;

    try {
      Member m = MemberFinder.findBySubject(s, subj);
      Iterator<MembershipDTO> it = GrouperDAOFactory.getFactory().getMembership().findAllEffective(
        stem.getUuid(), m.getUuid(), f, via.getUuid(), depth).iterator();

      if (it.hasNext()) {
        mship = new Membership();
        mship.setDTO(it.next());
        mship.setSession(s);
      }
    } catch (Exception e) {
      mship = null;
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


  private void verifyImmediateMembership(GrouperSession s, String comment, Stem childStem, Subject childSubject, Field f) {

    // first verify that the membership exists
    Membership childMembership = findImmediateMembership(s, childStem, childSubject, f);
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


  private void verifyCompositeMembership(GrouperSession s, String comment, Group childGroup, Subject childSubject) {

    // first verify that the membership exists
    Membership childMembership = findCompositeMembership(s, childGroup, childSubject);
    Assert.assertNotNull(comment + ": find membership", childMembership);


    // second verify the via_id
    Composite c = null;
    try {
      c = childMembership.getViaComposite();
    } catch (CompositeNotFoundException e) {
      // do nothing
    }

    Assert.assertNotNull(comment + ": find via_id", c);

    Group viaGroup = null;
    try {
      viaGroup = c.getOwnerGroup();
    } catch (GroupNotFoundException e) {
      // do nothing
    }

    Assert.assertNotNull(comment + ": find owner group of via_id", viaGroup);

    Assert.assertEquals(comment + ": verify via_id", childGroup.getName(), viaGroup.getName());


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

  private void verifyEffectiveMembership(GrouperSession s, String comment, Stem childStem, Subject childSubject, Group childVia,
    int childDepth, Stem parentStem, Subject parentSubject, Group parentVia, int parentDepth, Field f) {

    // first verify that the membership exists
    Membership childMembership = findEffectiveMembership(s, childStem, childSubject, childVia, childDepth, f);
    Assert.assertNotNull(comment + ": find membership", childMembership);


    // second verify that the parent membership exists based on data from the method parameters
    Membership parentMembership = null;
    if (parentDepth == 0) {
      parentMembership = findImmediateMembership(s, parentStem, parentSubject, f);
    } else {
      parentMembership = findEffectiveMembership(s, parentStem, parentSubject, parentVia, parentDepth, f);
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

