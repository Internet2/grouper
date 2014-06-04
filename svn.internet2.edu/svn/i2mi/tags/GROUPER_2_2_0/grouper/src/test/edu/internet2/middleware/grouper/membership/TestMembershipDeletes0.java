/*******************************************************************************
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
 ******************************************************************************/
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

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.MembershipFinder;
import edu.internet2.middleware.grouper.helper.DateHelper;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.MembershipTestHelper;
import edu.internet2.middleware.grouper.helper.R;
import edu.internet2.middleware.grouper.helper.T;
import edu.internet2.middleware.grouper.misc.CompositeType;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * @author Shilen Patel.
 */
public class TestMembershipDeletes0 extends GrouperTest {

  private static final Log LOG = GrouperUtil.getLog(TestMembershipDeletes0.class);

  R       r;
  Date    before;

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
  Group   gL;
  Group   gM;
  Group   gN;
  Group   gO;
  Group   gP;
  Group   gQ;
  Group   gR;
  Group   gS;
  Group   gT;
  Group   gUDel;
  Group   gVDel;
  Group   gWDel;
  Group   gXDel;
  Group   gYDel;
  Group   gZDel;
  Subject subjA;
  Subject subjB;
  Subject subjC;
  Subject subjD;
  Subject subjEDel;

  public TestMembershipDeletes0(String name) {
    super(name);
  }

  public void testMembershipDeletes0() {
    LOG.info("testMembershipDeletes0");
    try {
      GrouperUtil.sleep(100);
      before  = new Date();
      GrouperUtil.sleep(100);

      r     = R.populateRegistry(1, 26, 5);
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
      gL    = r.getGroup("a", "l");
      gM    = r.getGroup("a", "m");
      gN    = r.getGroup("a", "n");
      gO    = r.getGroup("a", "o");
      gP    = r.getGroup("a", "p");
      gQ    = r.getGroup("a", "q");
      gR    = r.getGroup("a", "r");
      gS    = r.getGroup("a", "s");
      gT    = r.getGroup("a", "t");
      gUDel = r.getGroup("a", "u");
      gVDel = r.getGroup("a", "v");
      gWDel = r.getGroup("a", "w");
      gXDel = r.getGroup("a", "x");
      gYDel = r.getGroup("a", "y");
      gZDel = r.getGroup("a", "z");
      subjA = r.getSubject("a");
      subjB = r.getSubject("b");
      subjC = r.getSubject("c");
      subjD = r.getSubject("d");
      subjEDel = r.getSubject("e");

      // Add initial data
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
      gR.addMember(gQ.toSubject());

      gUDel.addCompositeMember(CompositeType.UNION, gVDel, gWDel);
      gXDel.addCompositeMember(CompositeType.UNION, gWDel, gYDel);
      gVDel.addMember(gXDel.toSubject());
      gWDel.addMember(gZDel.toSubject());
      gZDel.addMember(subjEDel);

      verifyMembershipAddAndDelete(gA, subjEDel);
      verifyMembershipAddAndDelete(gB, subjEDel);
      verifyMembershipAddAndDelete(gC, subjEDel);
      verifyMembershipAddAndDelete(gF, subjEDel);
      verifyMembershipAddAndDelete(gG, subjEDel);
      verifyMembershipAddAndDelete(gH, subjEDel);
      verifyMembershipAddAndDelete(gJ, subjEDel);
      verifyMembershipAddAndDelete(gK, subjEDel);
      verifyMembershipAddAndDelete(gL, subjEDel);
      verifyMembershipAddAndDelete(gM, subjEDel);
      verifyMembershipAddAndDelete(gN, subjEDel);
      verifyMembershipAddAndDelete(gO, subjEDel);
      verifyMembershipAddAndDelete(gQ, subjEDel);
      verifyMembershipAddAndDelete(gR, subjEDel);
      verifyMembershipAddAndDelete(gS, subjEDel);
      verifyMembershipAddAndDelete(gT, subjEDel);

      verifyMembershipAddAndDelete(gA, gUDel.toSubject());
      verifyMembershipAddAndDelete(gB, gUDel.toSubject());
      verifyMembershipAddAndDelete(gC, gUDel.toSubject());
      verifyMembershipAddAndDelete(gF, gUDel.toSubject());
      verifyMembershipAddAndDelete(gG, gUDel.toSubject());
      verifyMembershipAddAndDelete(gH, gUDel.toSubject());
      verifyMembershipAddAndDelete(gJ, gUDel.toSubject());
      verifyMembershipAddAndDelete(gK, gUDel.toSubject());
      verifyMembershipAddAndDelete(gL, gUDel.toSubject());
      verifyMembershipAddAndDelete(gM, gUDel.toSubject());
      verifyMembershipAddAndDelete(gN, gUDel.toSubject());
      verifyMembershipAddAndDelete(gO, gUDel.toSubject());
      verifyMembershipAddAndDelete(gQ, gUDel.toSubject());
      verifyMembershipAddAndDelete(gR, gUDel.toSubject());
      verifyMembershipAddAndDelete(gS, gUDel.toSubject());
      verifyMembershipAddAndDelete(gT, gUDel.toSubject());

      verifyMembershipAddAndDelete(gA, gVDel.toSubject());
      verifyMembershipAddAndDelete(gB, gVDel.toSubject());
      verifyMembershipAddAndDelete(gC, gVDel.toSubject());
      verifyMembershipAddAndDelete(gF, gVDel.toSubject());
      verifyMembershipAddAndDelete(gG, gVDel.toSubject());
      verifyMembershipAddAndDelete(gH, gVDel.toSubject());
      verifyMembershipAddAndDelete(gJ, gVDel.toSubject());
      verifyMembershipAddAndDelete(gK, gVDel.toSubject());
      verifyMembershipAddAndDelete(gL, gVDel.toSubject());
      verifyMembershipAddAndDelete(gM, gVDel.toSubject());
      verifyMembershipAddAndDelete(gN, gVDel.toSubject());
      verifyMembershipAddAndDelete(gO, gVDel.toSubject());
      verifyMembershipAddAndDelete(gQ, gVDel.toSubject());
      verifyMembershipAddAndDelete(gR, gVDel.toSubject());
      verifyMembershipAddAndDelete(gS, gVDel.toSubject());
      verifyMembershipAddAndDelete(gT, gVDel.toSubject());


      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  }

  public void verifyMembershipAddAndDelete(Group g, Subject s) throws Exception {
    g.addMember(s);
    g.deleteMember(s);
    verifyMemberships();
  }

  public void verifyMemberships() throws Exception {

    // SB -> gA
    MembershipTestHelper.verifyImmediateMembership(r.rs, "SB -> gA", gA, subjB);

    // gA -> gB
    MembershipTestHelper.verifyImmediateMembership(r.rs, "gA -> gB", gB, gA.toSubject());

    // SB -> gB (parent: gA -> gB) (depth: 1)
    MembershipTestHelper.verifyEffectiveMembership(r.rs, "SB -> gB", gB, subjB, gA, 1, gB, gA.toSubject(), null, 0);

    // SB -> gD
    MembershipTestHelper.verifyCompositeMembership(r.rs, "SB -> gD", gD, subjB);

    // SA -> gE
    MembershipTestHelper.verifyCompositeMembership(r.rs, "SA -> gE", gE, subjA);

    // SB -> gE
    MembershipTestHelper.verifyCompositeMembership(r.rs, "SB -> gE", gE, subjB);

    // SD -> gE
    MembershipTestHelper.verifyCompositeMembership(r.rs, "SD -> gE", gE, subjD);

    // gH -> gF
    Membership gHgF = MembershipTestHelper.verifyImmediateMembership(r.rs, "gH -> gF", gF, gH.toSubject());

    // gR -> gF
    Membership gRgF = MembershipTestHelper.verifyImmediateMembership(r.rs, "gR -> gF", gF, gR.toSubject());

    // SA -> gF (parent: gH -> gF) (depth: 1)
    MembershipTestHelper.verifyEffectiveMembership(r.rs, "SA -> gF", gF, subjA, gH, 1, gF, gH.toSubject(), null, 0);

    // gQ -> gF (parent: gH -> gF) (depth: 1)
    Membership gQgFThroughgH = MembershipTestHelper.verifyEffectiveMembership(r.rs, "gQ -> gF", gF, gQ.toSubject(), gH, 1, gHgF);

    // SD -> gF (parent: gQ -> gF) (depth: 2)
    MembershipTestHelper.verifyEffectiveMembership(r.rs, "SD -> gF", gF, subjD, gQ, 2, gQgFThroughgH);

    // gS -> gF (parent: gR -> gF) (depth: 1)
    MembershipTestHelper.verifyEffectiveMembership(r.rs, "gS -> gF", gF, gS.toSubject(), gR, 1, gF, gR.toSubject(), null, 0);

    // gQ -> gF (parent: gR -> gF) (depth: 1)
    Membership gQgFThroughgR = MembershipTestHelper.verifyEffectiveMembership(r.rs, "gQ -> gF", gF, gQ.toSubject(), gR, 1, gRgF);

    // SD -> gF (parent: gQ -> gF) (depth: 2)
    MembershipTestHelper.verifyEffectiveMembership(r.rs, "SD -> gF", gF, subjD, gQ, 2, gQgFThroughgR);

    // gD -> gG
    MembershipTestHelper.verifyImmediateMembership(r.rs, "gD -> gG", gG, gD.toSubject());

    // SB -> gG (parent: gD -> gG) (depth: 1)
    MembershipTestHelper.verifyEffectiveMembership(r.rs, "SB -> gG", gG, subjB, gD, 1, gG, gD.toSubject(), null, 0);

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

    // gE -> gJ
    MembershipTestHelper.verifyImmediateMembership(r.rs, "gE -> gJ", gJ, gE.toSubject());

    // SA -> gJ (parent: gE -> gJ) (depth: 1)
    MembershipTestHelper.verifyEffectiveMembership(r.rs, "SA -> gJ", gJ, subjA, gE, 1, gJ, gE.toSubject(), null, 0);

    // SB -> gJ (parent: gE -> gJ) (depth: 1)
    MembershipTestHelper.verifyEffectiveMembership(r.rs, "SB -> gJ", gJ, subjB, gE, 1, gJ, gE.toSubject(), null, 0);

    // SD -> gJ (parent: gE -> gJ) (depth: 1)
    MembershipTestHelper.verifyEffectiveMembership(r.rs, "SD -> gJ", gJ, subjD, gE, 1, gJ, gE.toSubject(), null, 0);

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

    // gJ -> gM
    MembershipTestHelper.verifyImmediateMembership(r.rs, "gJ -> gM", gM, gJ.toSubject());

    // SA -> gM (parent: gE -> gM) (depth: 2)
    MembershipTestHelper.verifyEffectiveMembership(r.rs, "SA -> gM", gM, subjA, gE, 2, gM, gE.toSubject(), gJ, 1);

    // SB -> gM (parent: gE -> gM) (depth: 2)
    MembershipTestHelper.verifyEffectiveMembership(r.rs, "SB -> gM", gM, subjB, gE, 2, gM, gE.toSubject(), gJ, 1);

    // SD -> gM (parent: gE -> gM) (depth: 2)
    MembershipTestHelper.verifyEffectiveMembership(r.rs, "SD -> gM", gM, subjD, gE, 2, gM, gE.toSubject(), gJ, 1);

    // gE -> gM (parent: gJ -> gM) (depth: 1)
    MembershipTestHelper.verifyEffectiveMembership(r.rs, "gE -> gM", gM, gE.toSubject(), gJ, 1, gM, gJ.toSubject(), null, 0);
    
    // gG -> gN
    MembershipTestHelper.verifyImmediateMembership(r.rs, "gG -> gN", gN, gG.toSubject());

    // SB -> gN (parent: gD -> gN) (depth: 2)
    MembershipTestHelper.verifyEffectiveMembership(r.rs, "SB -> gN", gN, subjB, gD, 2, gN, gD.toSubject(), gG, 1);

    // gD -> gN (parent: gG -> gN) (depth: 1)
    MembershipTestHelper.verifyEffectiveMembership(r.rs, "gD -> gN", gN, gD.toSubject(), gG, 1, gN, gG.toSubject(), null, 0);

    // SB -> gP
    MembershipTestHelper.verifyCompositeMembership(r.rs, "SB -> gP", gP, subjB);

    // SD -> gQ
    MembershipTestHelper.verifyImmediateMembership(r.rs, "SD -> gQ", gQ, subjD);

    // gS -> gR
    MembershipTestHelper.verifyImmediateMembership(r.rs, "gS -> gR", gR, gS.toSubject());

    // gQ -> gR
    MembershipTestHelper.verifyImmediateMembership(r.rs, "gQ -> gR", gR, gQ.toSubject());

    // SD -> gR (parent: gQ -> gR) (depth: 1)
    MembershipTestHelper.verifyEffectiveMembership(r.rs, "SD -> gR", gR, subjD, gQ, 1, gR, gQ.toSubject(), null, 0);

    // gP -> gT
    MembershipTestHelper.verifyImmediateMembership(r.rs, "gP -> gT", gT, gP.toSubject());

    // SB -> gT (parent: gP -> gT) (depth: 1)
    MembershipTestHelper.verifyEffectiveMembership(r.rs, "SB -> gT", gT, subjB, gP, 1, gT, gP.toSubject(), null, 0);


    // verify the total number of memberships
    Set<Membership> allMemberships = MembershipFinder.internal_findAllByCreatedAfter(r.rs, before, Group.getDefaultList());

    T.amount("Number of memberships", 56, allMemberships.size());
  }
}

