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
public class TestMembership4 extends TestCase {

  private static final Log LOG = LogFactory.getLog(TestMembership4.class);

  public TestMembership4(String name) {
    super(name);
  }

  protected void setUp () {
    LOG.debug("setUp");
    RegistryReset.reset();
  }

  protected void tearDown () {
    LOG.debug("tearDown");
  }

  public void testEffectiveMembershipsWithComposites() {
    LOG.info("testEffectiveMembershipsWithComposites");
    try {
      Date    before   = DateHelper.getPastDate();

      R       r     = R.populateRegistry(1, 22, 4);
      Group   gA    = r.getGroup("a", "a");
      Group   gB    = r.getGroup("a", "b");
      Group   gC    = r.getGroup("a", "c");
      Group   gD    = r.getGroup("a", "d");
      Group   gE    = r.getGroup("a", "e");
      Group   gF    = r.getGroup("a", "f");
      Group   gG    = r.getGroup("a", "g");
      Group   gH    = r.getGroup("a", "h");
      Group   gI    = r.getGroup("a", "i");
      Group   gJ    = r.getGroup("a", "j");
      Group   gK    = r.getGroup("a", "k");
      Group   gL    = r.getGroup("a", "l");
      Group   gM    = r.getGroup("a", "m");
      Group   gN    = r.getGroup("a", "n");
      Group   gO    = r.getGroup("a", "o");
      Group   gP    = r.getGroup("a", "p");
      Group   gQ    = r.getGroup("a", "q");
      Group   gR    = r.getGroup("a", "r");
      Group   gS    = r.getGroup("a", "s");
      Group   gT    = r.getGroup("a", "t");
      Subject subjA = r.getSubject("a");
      Subject subjB = r.getSubject("b");
      Subject subjC = r.getSubject("c");
      Subject subjD = r.getSubject("d");

      gA.addMember(subjB);
      gT.addMember(gP.toSubject());
      gD.addCompositeMember(CompositeType.UNION, gB, gC);
      gP.addCompositeMember(CompositeType.UNION, gD, gO);
      gB.addMember(gA.toSubject());
      gN.addMember(gG.toSubject());
      gG.addMember(gD.toSubject());
      gF.addMember(gH.toSubject());
      gH.addMember(subjA);
      gI.addCompositeMember(CompositeType.UNION, gJ, gK);
      gJ.addMember(gE.toSubject());
      gK.addMember(subjC);
      gL.addMember(gI.toSubject());
      gM.addMember(gJ.toSubject());
      gE.addCompositeMember(CompositeType.UNION, gF, gG);
      gQ.addMember(subjD);
      gH.addMember(gQ.toSubject());
      gR.addMember(gS.toSubject());
      gF.addMember(gR.toSubject());

      // SB -> gA
      MembershipTestHelper.verifyImmediateMembership(r.rs, "SB -> gA", gA, subjB);

      // gA -> gB
      MembershipTestHelper.verifyImmediateMembership(r.rs, "gA -> gB", gB, gA.toSubject());

      // SB -> gB (parent: gA -> gB) (depth: 1)
      MembershipTestHelper.verifyEffectiveMembership(r.rs, "SB -> gB", gB, subjB, gA, 1, gB, gA.toSubject(), null, 0);

      // SB -> gD
      MembershipTestHelper.verifyCompositeMembership(r.rs, "SB -> gD", gD, subjB);

      // gA -> gD
      MembershipTestHelper.verifyCompositeMembership(r.rs, "gA -> gD", gD, gA.toSubject());

      // SA -> gE
      MembershipTestHelper.verifyCompositeMembership(r.rs, "SA -> gE", gE, subjA);

      // SB -> gE
      MembershipTestHelper.verifyCompositeMembership(r.rs, "SB -> gE", gE, subjB);

      // SD -> gE
      MembershipTestHelper.verifyCompositeMembership(r.rs, "SD -> gE", gE, subjD);

      // gA -> gE
      MembershipTestHelper.verifyCompositeMembership(r.rs, "gA -> gE", gE, gA.toSubject());

      // gH -> gE
      MembershipTestHelper.verifyCompositeMembership(r.rs, "gH -> gE", gE, gH.toSubject());

      // gD -> gE
      MembershipTestHelper.verifyCompositeMembership(r.rs, "gD -> gE", gE, gD.toSubject());

      // gQ -> gE
      MembershipTestHelper.verifyCompositeMembership(r.rs, "gQ -> gE", gE, gQ.toSubject());

      // gR -> gE
      MembershipTestHelper.verifyCompositeMembership(r.rs, "gR -> gE", gE, gR.toSubject());

      // gS -> gE
      MembershipTestHelper.verifyCompositeMembership(r.rs, "gS -> gE", gE, gS.toSubject());

      // gH -> gF
      MembershipTestHelper.verifyImmediateMembership(r.rs, "gH -> gF", gF, gH.toSubject());

      // gR -> gF
      MembershipTestHelper.verifyImmediateMembership(r.rs, "gR -> gF", gF, gR.toSubject());

      // SA -> gF (parent: gH -> gF) (depth: 1)
      MembershipTestHelper.verifyEffectiveMembership(r.rs, "SA -> gF", gF, subjA, gH, 1, gF, gH.toSubject(), null, 0);

      // SD -> gF (parent: gQ -> gF) (depth: 2)
      MembershipTestHelper.verifyEffectiveMembership(r.rs, "SD -> gF", gF, subjD, gQ, 2, gF, gQ.toSubject(), gH, 1);

      // gQ -> gF (parent: gH -> gF) (depth: 1)
      MembershipTestHelper.verifyEffectiveMembership(r.rs, "gQ -> gF", gF, gQ.toSubject(), gH, 1, gF, gH.toSubject(), null, 0);

      // gS -> gF (parent: gR -> gF) (depth: 1)
      MembershipTestHelper.verifyEffectiveMembership(r.rs, "gS -> gF", gF, gS.toSubject(), gR, 1, gF, gR.toSubject(), null, 0);

      // gD -> gG
      MembershipTestHelper.verifyImmediateMembership(r.rs, "gD -> gG", gG, gD.toSubject());

      // SB -> gG (parent: gD -> gG) (depth: 1)
      MembershipTestHelper.verifyEffectiveMembership(r.rs, "SB -> gG", gG, subjB, gD, 1, gG, gD.toSubject(), null, 0);

      // gA -> gG (parent: gD -> gG) (depth: 1)
      MembershipTestHelper.verifyEffectiveMembership(r.rs, "gA -> gG", gG, gA.toSubject(), gD, 1, gG, gD.toSubject(), null, 0);

      // SA -> gH
      MembershipTestHelper.verifyImmediateMembership(r.rs, "SA -> gH", gH, subjA);

      // gQ -> gH
      MembershipTestHelper.verifyImmediateMembership(r.rs, "gQ -> gH", gH, gQ.toSubject());

      // SD -> gH (parent: gQ -> gH) (depth: 1)
      MembershipTestHelper.verifyEffectiveMembership(r.rs, "SD -> gH", gH, subjD, gQ, 1, gH, gQ.toSubject(), null, 0);

      // SA -> gI
      MembershipTestHelper.verifyCompositeMembership(r.rs, "SA -> gI", gI, subjA);

      // SB -> gI
      MembershipTestHelper.verifyCompositeMembership(r.rs, "SB -> gI", gI, subjB);

      // SC -> gI
      MembershipTestHelper.verifyCompositeMembership(r.rs, "SC -> gI", gI, subjC);

      // SD -> gI
      MembershipTestHelper.verifyCompositeMembership(r.rs, "SD -> gI", gI, subjD);

      // gA -> gI
      MembershipTestHelper.verifyCompositeMembership(r.rs, "gA -> gI", gI, gA.toSubject());

      // gD -> gI
      MembershipTestHelper.verifyCompositeMembership(r.rs, "gD -> gI", gI, gD.toSubject());

      // gE -> gI
      MembershipTestHelper.verifyCompositeMembership(r.rs, "gE -> gI", gI, gE.toSubject());

      // gH -> gI
      MembershipTestHelper.verifyCompositeMembership(r.rs, "gH -> gI", gI, gH.toSubject());

      // gQ -> gI
      MembershipTestHelper.verifyCompositeMembership(r.rs, "gQ -> gI", gI, gQ.toSubject());

      // gR -> gI
      MembershipTestHelper.verifyCompositeMembership(r.rs, "gR -> gI", gI, gR.toSubject());

      // gS -> gI
      MembershipTestHelper.verifyCompositeMembership(r.rs, "gS -> gI", gI, gS.toSubject());

      // gE -> gJ
      MembershipTestHelper.verifyImmediateMembership(r.rs, "gE -> gJ", gJ, gE.toSubject());

      // SA -> gJ (parent: gE -> gJ) (depth: 1)
      MembershipTestHelper.verifyEffectiveMembership(r.rs, "SA -> gJ", gJ, subjA, gE, 1, gJ, gE.toSubject(), null, 0);

      // SB -> gJ (parent: gE -> gJ) (depth: 1)
      MembershipTestHelper.verifyEffectiveMembership(r.rs, "SB -> gJ", gJ, subjB, gE, 1, gJ, gE.toSubject(), null, 0);

      // SD -> gJ (parent: gE -> gJ) (depth: 1)
      MembershipTestHelper.verifyEffectiveMembership(r.rs, "SD -> gJ", gJ, subjD, gE, 1, gJ, gE.toSubject(), null, 0);

      // gA -> gJ (parent: gE -> gJ) (depth: 1)
      MembershipTestHelper.verifyEffectiveMembership(r.rs, "gA -> gJ", gJ, gA.toSubject(), gE, 1, gJ, gE.toSubject(), null, 0);

      // gD -> gJ (parent: gE -> gJ) (depth: 1)
      MembershipTestHelper.verifyEffectiveMembership(r.rs, "gD -> gJ", gJ, gD.toSubject(), gE, 1, gJ, gE.toSubject(), null, 0);

      // gH -> gJ (parent: gE -> gJ) (depth: 1)
      MembershipTestHelper.verifyEffectiveMembership(r.rs, "gH -> gJ", gJ, gH.toSubject(), gE, 1, gJ, gE.toSubject(), null, 0);

      // gQ -> gJ (parent: gE -> gJ) (depth: 1)
      MembershipTestHelper.verifyEffectiveMembership(r.rs, "gQ -> gJ", gJ, gQ.toSubject(), gE, 1, gJ, gE.toSubject(), null, 0);

      // gR -> gJ (parent: gE -> gJ) (depth: 1)
      MembershipTestHelper.verifyEffectiveMembership(r.rs, "gR -> gJ", gJ, gR.toSubject(), gE, 1, gJ, gE.toSubject(), null, 0);

      // gS -> gJ (parent: gE -> gJ) (depth: 1)
      MembershipTestHelper.verifyEffectiveMembership(r.rs, "gS -> gJ", gJ, gS.toSubject(), gE, 1, gJ, gE.toSubject(), null, 0);

      // SC -> gK
      MembershipTestHelper.verifyImmediateMembership(r.rs, "SC -> gK", gK, subjC);

      // gI -> gL
      MembershipTestHelper.verifyImmediateMembership(r.rs, "gI -> gL", gL, gI.toSubject());

      // SA -> gL (parent: gI -> gL) (depth: 1)
      MembershipTestHelper.verifyEffectiveMembership(r.rs, "SA -> gL", gL, subjA, gI, 1, gL, gI.toSubject(), null, 0);

      // SB -> gL (parent: gI -> gL) (depth: 1)
      MembershipTestHelper.verifyEffectiveMembership(r.rs, "SB -> gL", gL, subjB, gI, 1, gL, gI.toSubject(), null, 0);

      // SC -> gL (parent: gI -> gL) (depth: 1)
      MembershipTestHelper.verifyEffectiveMembership(r.rs, "SC -> gL", gL, subjC, gI, 1, gL, gI.toSubject(), null, 0);

      // SD -> gL (parent: gI -> gL) (depth: 1)
      MembershipTestHelper.verifyEffectiveMembership(r.rs, "SD -> gL", gL, subjD, gI, 1, gL, gI.toSubject(), null, 0);

      // gA -> gL (parent: gI -> gL) (depth: 1)
      MembershipTestHelper.verifyEffectiveMembership(r.rs, "gA -> gL", gL, gA.toSubject(), gI, 1, gL, gI.toSubject(), null, 0);

      // gD -> gL (parent: gI -> gL) (depth: 1)
      MembershipTestHelper.verifyEffectiveMembership(r.rs, "gD -> gL", gL, gD.toSubject(), gI, 1, gL, gI.toSubject(), null, 0);

      // gE -> gL (parent: gI -> gL) (depth: 1)
      MembershipTestHelper.verifyEffectiveMembership(r.rs, "gE -> gL", gL, gE.toSubject(), gI, 1, gL, gI.toSubject(), null, 0);

      // gH -> gL (parent: gI -> gL) (depth: 1)
      MembershipTestHelper.verifyEffectiveMembership(r.rs, "gH -> gL", gL, gH.toSubject(), gI, 1, gL, gI.toSubject(), null, 0);

      // gQ -> gL (parent: gI -> gL) (depth: 1)
      MembershipTestHelper.verifyEffectiveMembership(r.rs, "gQ -> gL", gL, gQ.toSubject(), gI, 1, gL, gI.toSubject(), null, 0);

      // gR -> gL (parent: gI -> gL) (depth: 1)
      MembershipTestHelper.verifyEffectiveMembership(r.rs, "gR -> gL", gL, gR.toSubject(), gI, 1, gL, gI.toSubject(), null, 0);

      // gS -> gL (parent: gI -> gL) (depth: 1)
      MembershipTestHelper.verifyEffectiveMembership(r.rs, "gS -> gL", gL, gS.toSubject(), gI, 1, gL, gI.toSubject(), null, 0);

      // gJ -> gM
      MembershipTestHelper.verifyImmediateMembership(r.rs, "gJ -> gM", gM, gJ.toSubject());

      // SA -> gM (parent: gE -> gM) (depth: 2)
      MembershipTestHelper.verifyEffectiveMembership(r.rs, "SA -> gM", gM, subjA, gE, 2, gM, gE.toSubject(), gJ, 1);

      // SB -> gM (parent: gE -> gM) (depth: 2)
      MembershipTestHelper.verifyEffectiveMembership(r.rs, "SB -> gM", gM, subjB, gE, 2, gM, gE.toSubject(), gJ, 1);

      // SD -> gM (parent: gE -> gM) (depth: 2)
      MembershipTestHelper.verifyEffectiveMembership(r.rs, "SD -> gM", gM, subjD, gE, 2, gM, gE.toSubject(), gJ, 1);

      // gA -> gM (parent: gE -> gM) (depth: 2)
      MembershipTestHelper.verifyEffectiveMembership(r.rs, "gA -> gM", gM, gA.toSubject(), gE, 2, gM, gE.toSubject(), gJ, 1);

      // gD -> gM (parent: gE -> gM) (depth: 2)
      MembershipTestHelper.verifyEffectiveMembership(r.rs, "gD -> gM", gM, gD.toSubject(), gE, 2, gM, gE.toSubject(), gJ, 1);

      // gH -> gM (parent: gE -> gM) (depth: 2)
      MembershipTestHelper.verifyEffectiveMembership(r.rs, "gH -> gM", gM, gH.toSubject(), gE, 2, gM, gE.toSubject(), gJ, 1);

      // gQ -> gM (parent: gE -> gM) (depth: 2)
      MembershipTestHelper.verifyEffectiveMembership(r.rs, "gQ -> gM", gM, gQ.toSubject(), gE, 2, gM, gE.toSubject(), gJ, 1);

      // gE -> gM (parent: gJ -> gM) (depth: 1)
      MembershipTestHelper.verifyEffectiveMembership(r.rs, "gE -> gM", gM, gE.toSubject(), gJ, 1, gM, gJ.toSubject(), null, 0);

      // gR -> gM (parent: gE -> gM) (depth: 2)
      MembershipTestHelper.verifyEffectiveMembership(r.rs, "gR -> gM", gM, gR.toSubject(), gE, 2, gM, gE.toSubject(), gJ, 1);

      // gS -> gM (parent: gE -> gM) (depth: 2)
      MembershipTestHelper.verifyEffectiveMembership(r.rs, "gS -> gM", gM, gS.toSubject(), gE, 2, gM, gE.toSubject(), gJ, 1);

      // gG -> gN
      MembershipTestHelper.verifyImmediateMembership(r.rs, "gG -> gN", gN, gG.toSubject());

      // SB -> gN (parent: gD -> gN) (depth: 2)
      MembershipTestHelper.verifyEffectiveMembership(r.rs, "SB -> gN", gN, subjB, gD, 2, gN, gD.toSubject(), gG, 1);

      // gA -> gN (parent: gD -> gN) (depth: 2)
      MembershipTestHelper.verifyEffectiveMembership(r.rs, "gA -> gN", gN, gA.toSubject(), gD, 2, gN, gD.toSubject(), gG, 1);

      // gD -> gN (parent: gG -> gN) (depth: 1)
      MembershipTestHelper.verifyEffectiveMembership(r.rs, "gD -> gN", gN, gD.toSubject(), gG, 1, gN, gG.toSubject(), null, 0);

      // SB -> gP
      MembershipTestHelper.verifyCompositeMembership(r.rs, "SB -> gP", gP, subjB);

      // gA -> gP
      MembershipTestHelper.verifyCompositeMembership(r.rs, "gA -> gP", gP, gA.toSubject());

      // SD -> gQ
      MembershipTestHelper.verifyImmediateMembership(r.rs, "SD -> gQ", gQ, subjD);

      // gS -> gR
      MembershipTestHelper.verifyImmediateMembership(r.rs, "gS -> gR", gR, gS.toSubject());

      // gP -> gT
      MembershipTestHelper.verifyImmediateMembership(r.rs, "gP -> gT", gT, gP.toSubject());

      // gA -> gT (parent: gP -> gT) (depth: 1)
      MembershipTestHelper.verifyEffectiveMembership(r.rs, "gA -> gT", gT, gA.toSubject(), gP, 1, gT, gP.toSubject(), null, 0);

      // SB -> gT (parent: gP -> gT) (depth: 1)
      MembershipTestHelper.verifyEffectiveMembership(r.rs, "SB -> gT", gT, subjB, gP, 1, gT, gP.toSubject(), null, 0);


      // verify the total number of memberships
      Set<Membership> allMemberships = MembershipFinder.internal_findAllByCreatedAfter(r.rs, before, Group.getDefaultList());

/*
      Iterator<Membership> it = allMemberships.iterator();
      java.util.SortedSet set = new java.util.TreeSet();
      while (it.hasNext()) {
        Membership mship = it.next();
        String owner = mship.getGroup().getName();
        String member = mship.getMember().getSubjectId();
        String source = mship.getMember().getSubjectSourceId();
        String type = mship.getType();
        int depth = mship.getDepth();
        String via = "";
        if (source.equals("g:gsa")) {
          try { Group g = GroupFinder.findByUuid(r.rs, member); member = g.getName(); } catch (Exception e) {}
        }
        try { Group g = mship.getViaGroup(); via = g.getName(); } catch (Exception e) {}
        try { Composite c = mship.getViaComposite(); via = c.getOwnerGroup().getName(); } catch (Exception e) {}
        set.add("Owner: " + owner + " Member: " + member + " Type: " + type + " Depth: " + depth + " Via: " + via);
      }
      Iterator it2 = set.iterator();
      while (it2.hasNext()) {
        System.out.println(it2.next());
      }
*/

      T.amount("Number of memberships", 82, allMemberships.size());

      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  }
}

