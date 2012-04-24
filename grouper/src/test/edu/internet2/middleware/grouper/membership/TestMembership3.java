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
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * @author Shilen Patel.
 */
public class TestMembership3 extends GrouperTest {

  private static final Log LOG = GrouperUtil.getLog(TestMembership3.class);

  public TestMembership3(String name) {
    super(name);
  }

  public void testEffectiveMemberships() {
    LOG.info("testEffectiveMemberships");
    try {
      GrouperUtil.sleep(100);
      Date            before  = new Date();
      GrouperUtil.sleep(100);

      R       r     = R.populateRegistry(1, 12, 1);
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
      Subject subjA = r.getSubject("a");

      gA.addMember(subjA);
      gB.addMember( gA.toSubject() );
      gB.addMember( gD.toSubject() );
      gD.addMember( gE.toSubject() );
      gD.addMember( gF.toSubject() );
      gF.addMember( gA.toSubject() );
      gI.addMember( gH.toSubject() );
      gJ.addMember( gH.toSubject() );
      gG.addMember( gC.toSubject() );
      gH.addMember( gC.toSubject() );
      gC.addMember( gK.toSubject() );
      gL.addMember( gB.toSubject() );
      gC.addMember( gB.toSubject() );

      // SA -> gA
      MembershipTestHelper.verifyImmediateMembership(r.rs, "SA -> gA", gA, subjA);

      // gA -> gB
      MembershipTestHelper.verifyImmediateMembership(r.rs, "gA -> gB", gB, gA.toSubject());

      // gD -> gB
      MembershipTestHelper.verifyImmediateMembership(r.rs, "gD -> gB", gB, gD.toSubject());

      // gE -> gB (parent: gD -> gB) (depth: 1)
      MembershipTestHelper.verifyEffectiveMembership(r.rs, "gE -> gB - depth 1", gB, gE.toSubject(), gD, 1, gB, gD.toSubject(), null, 0);

      // gF -> gB (parent: gD -> gB) (depth: 1)
      MembershipTestHelper.verifyEffectiveMembership(r.rs, "gF -> gB - depth 1", gB, gF.toSubject(), gD, 1, gB, gD.toSubject(), null, 0);

      // SA -> gB (parent: gA -> gB) (depth: 1)
      MembershipTestHelper.verifyEffectiveMembership(r.rs, "SA -> gB - depth 1", gB, subjA, gA, 1, gB, gA.toSubject(), null, 0);

      // gA -> gB (parent: gF -> gB) (depth: 2)
      MembershipTestHelper.verifyEffectiveMembership(r.rs, "gA -> gB - depth 2", gB, gA.toSubject(), gF, 2, gB, gF.toSubject(), gD, 1);

      // SA -> gB (parent: gA -> gB) (depth: 3)
      MembershipTestHelper.verifyEffectiveMembership(r.rs, "SA -> gB - depth 3", gB, subjA, gA, 3, gB, gA.toSubject(), gF, 2);

      // gB -> gC
      MembershipTestHelper.verifyImmediateMembership(r.rs, "gB -> gC", gC, gB.toSubject());

      // gK -> gC
      MembershipTestHelper.verifyImmediateMembership(r.rs, "gK -> gC", gC, gK.toSubject());

      // gA -> gC (parent: gB -> gC) (depth: 1)
      MembershipTestHelper.verifyEffectiveMembership(r.rs, "gA -> gC - depth 1", gC, gA.toSubject(), gB, 1, gC, gB.toSubject(), null, 0);

      // gD -> gC (parent: gB -> gC) (depth: 1)
      MembershipTestHelper.verifyEffectiveMembership(r.rs, "gD -> gC - depth 1", gC, gD.toSubject(), gB, 1, gC, gB.toSubject(), null, 0);

      // gE -> gC (parent: gD -> gC) (depth: 2)
      MembershipTestHelper.verifyEffectiveMembership(r.rs, "gE -> gC - depth 2", gC, gE.toSubject(), gD, 2, gC, gD.toSubject(), gB, 1);

      // gF -> gC (parent: gD -> gC) (depth: 2)
      MembershipTestHelper.verifyEffectiveMembership(r.rs, "gF -> gC - depth 2", gC, gF.toSubject(), gD, 2, gC, gD.toSubject(), gB, 1);

      // SA -> gC (parent: gA -> gC) (depth: 2)
      MembershipTestHelper.verifyEffectiveMembership(r.rs, "SA -> gC - depth 2", gC, subjA, gA, 2, gC, gA.toSubject(), gB, 1);

      // gA -> gC (parent: gF -> gC) (depth: 3)
      MembershipTestHelper.verifyEffectiveMembership(r.rs, "gA -> gC - depth 3", gC, gA.toSubject(), gF, 3, gC, gF.toSubject(), gD, 2);

      // SA -> gC (parent: gA -> gC) (depth: 4)
      MembershipTestHelper.verifyEffectiveMembership(r.rs, "SA -> gC - depth 4", gC, subjA, gA, 4, gC, gA.toSubject(), gF, 3);

      // gE -> gD
      MembershipTestHelper.verifyImmediateMembership(r.rs, "gE -> gD", gD, gE.toSubject());

      // gF -> gD
      MembershipTestHelper.verifyImmediateMembership(r.rs, "gF -> gD", gD, gF.toSubject());

      // gA -> gD (parent: gF -> gD) (depth: 1)
      MembershipTestHelper.verifyEffectiveMembership(r.rs, "gA -> gD - depth 1", gD, gA.toSubject(), gF, 1, gD, gF.toSubject(), null, 0);

      // SA -> gD (parent: gA -> gD) (depth: 2)
      MembershipTestHelper.verifyEffectiveMembership(r.rs, "SA -> gD - depth 2", gD, subjA, gA, 2, gD, gA.toSubject(), gF, 1);

      // gA -> gF
      MembershipTestHelper.verifyImmediateMembership(r.rs, "gA -> gF", gF, gA.toSubject());

      // SA -> gF (parent: gA -> gF) (depth: 1)
      MembershipTestHelper.verifyEffectiveMembership(r.rs, "SA -> gF - depth 1", gF, subjA, gA, 1, gF, gA.toSubject(), null, 0);

      // gC -> gG
      MembershipTestHelper.verifyImmediateMembership(r.rs, "gC -> gG", gG, gC.toSubject());

      // gB -> gG (parent: gC -> gG) (depth: 1)
      MembershipTestHelper.verifyEffectiveMembership(r.rs, "gB -> gG - depth 1", gG, gB.toSubject(), gC, 1, gG, gC.toSubject(), null, 0);

      // gK -> gG (parent: gC -> gG) (depth: 1)
      MembershipTestHelper.verifyEffectiveMembership(r.rs, "gK -> gG - depth 1", gG, gK.toSubject(), gC, 1, gG, gC.toSubject(), null, 0);

      // gA -> gG (parent: gB -> gG) (depth: 2)
      MembershipTestHelper.verifyEffectiveMembership(r.rs, "gA -> gG - depth 2", gG, gA.toSubject(), gB, 2, gG, gB.toSubject(), gC, 1);

      // gD -> gG (parent: gB -> gG) (depth: 2)
      MembershipTestHelper.verifyEffectiveMembership(r.rs, "gD -> gG - depth 2", gG, gD.toSubject(), gB, 2, gG, gB.toSubject(), gC, 1);

      // gE -> gG (parent: gD -> gG) (depth: 3)
      MembershipTestHelper.verifyEffectiveMembership(r.rs, "gE -> gG - depth 3", gG, gE.toSubject(), gD, 3, gG, gD.toSubject(), gB, 2);

      // gF -> gG (parent: gD -> gG) (depth: 3)
      MembershipTestHelper.verifyEffectiveMembership(r.rs, "gF -> gG - depth 3", gG, gF.toSubject(), gD, 3, gG, gD.toSubject(), gB, 2);

      // SA -> gG (parent: gA -> gG) (depth: 3)
      MembershipTestHelper.verifyEffectiveMembership(r.rs, "SA -> gG - depth 3", gG, subjA, gA, 3, gG, gA.toSubject(), gB, 2);

      // gA -> gG (parent: gF -> gG) (depth: 4)
      MembershipTestHelper.verifyEffectiveMembership(r.rs, "gA -> gG - depth 4", gG, gA.toSubject(), gF, 4, gG, gF.toSubject(), gD, 3);

      // SA -> gG (parent: gA -> gG) (depth: 5)
      MembershipTestHelper.verifyEffectiveMembership(r.rs, "SA -> gG - depth 5", gG, subjA, gA, 5, gG, gA.toSubject(), gF, 4);

      // gC -> gH
      MembershipTestHelper.verifyImmediateMembership(r.rs, "gC -> gH", gH, gC.toSubject());

      // gB -> gH (parent: gC -> gH) (depth: 1)
      MembershipTestHelper.verifyEffectiveMembership(r.rs, "gB -> gH - depth 1", gH, gB.toSubject(), gC, 1, gH, gC.toSubject(), null, 0);

      // gK -> gH (parent: gC -> gH) (depth: 1)
      MembershipTestHelper.verifyEffectiveMembership(r.rs, "gK -> gH - depth 1", gH, gK.toSubject(), gC, 1, gH, gC.toSubject(), null, 0);

      // gA -> gH (parent: gB -> gH) (depth: 2)
      MembershipTestHelper.verifyEffectiveMembership(r.rs, "gA -> gH - depth 2", gH, gA.toSubject(), gB, 2, gH, gB.toSubject(), gC, 1);

      // gD -> gH (parent: gB -> gH) (depth: 2)
      MembershipTestHelper.verifyEffectiveMembership(r.rs, "gD -> gH - depth 2", gH, gD.toSubject(), gB, 2, gH, gB.toSubject(), gC, 1);

      // gE -> gH (parent: gD -> gH) (depth: 3)
      MembershipTestHelper.verifyEffectiveMembership(r.rs, "gE -> gH - depth 3", gH, gE.toSubject(), gD, 3, gH, gD.toSubject(), gB, 2);

      // gF -> gH (parent: gD -> gH) (depth: 3)
      MembershipTestHelper.verifyEffectiveMembership(r.rs, "gF -> gH - depth 3", gH, gF.toSubject(), gD, 3, gH, gD.toSubject(), gB, 2);

      // SA -> gH (parent: gA -> gH) (depth: 3)
      MembershipTestHelper.verifyEffectiveMembership(r.rs, "SA -> gH - depth 3", gH, subjA, gA, 3, gH, gA.toSubject(), gB, 2);

      // gA -> gH (parent: gF -> gH) (depth: 4)
      MembershipTestHelper.verifyEffectiveMembership(r.rs, "gA -> gH - depth 4", gH, gA.toSubject(), gF, 4, gH, gF.toSubject(), gD, 3);

      // SA -> gH (parent: gA -> gH) (depth: 5)
      MembershipTestHelper.verifyEffectiveMembership(r.rs, "SA -> gH - depth 5", gH, subjA, gA, 5, gH, gA.toSubject(), gF, 4);

      // gH -> gI
      MembershipTestHelper.verifyImmediateMembership(r.rs, "gH -> gI", gI, gH.toSubject());

      // gC -> gI (parent: gH -> gI) (depth: 1)
      MembershipTestHelper.verifyEffectiveMembership(r.rs, "gC -> gI - depth 1", gI, gC.toSubject(), gH, 1, gI, gH.toSubject(), null, 0);

      // gB -> gI (parent: gC -> gI) (depth: 2)
      MembershipTestHelper.verifyEffectiveMembership(r.rs, "gB -> gI - depth 2", gI, gB.toSubject(), gC, 2, gI, gC.toSubject(), gH, 1);

      // gK -> gI (parent: gC -> gI) (depth: 2)
      MembershipTestHelper.verifyEffectiveMembership(r.rs, "gK -> gI - depth 2", gI, gK.toSubject(), gC, 2, gI, gC.toSubject(), gH, 1);

      // gA -> gI (parent: gB -> gI) (depth: 3)
      MembershipTestHelper.verifyEffectiveMembership(r.rs, "gA -> gI - depth 3", gI, gA.toSubject(), gB, 3, gI, gB.toSubject(), gC, 2);

      // gD -> gI (parent: gB -> gI) (depth: 3)
      MembershipTestHelper.verifyEffectiveMembership(r.rs, "gD -> gI - depth 3", gI, gD.toSubject(), gB, 3, gI, gB.toSubject(), gC, 2);

      // gE -> gI (parent: gD -> gI) (depth: 4)
      MembershipTestHelper.verifyEffectiveMembership(r.rs, "gE -> gI - depth 4", gI, gE.toSubject(), gD, 4, gI, gD.toSubject(), gB, 3);

      // gF -> gI (parent: gD -> gI) (depth: 4)
      MembershipTestHelper.verifyEffectiveMembership(r.rs, "gF -> gI - depth 4", gI, gF.toSubject(), gD, 4, gI, gD.toSubject(), gB, 3);

      // SA -> gI (parent: gA -> gI) (depth: 4)
      MembershipTestHelper.verifyEffectiveMembership(r.rs, "SA -> gI - depth 4", gI, subjA, gA, 4, gI, gA.toSubject(), gB, 3);

      // gA -> gI (parent: gF -> gI) (depth: 5)
      MembershipTestHelper.verifyEffectiveMembership(r.rs, "gA -> gI - depth 5", gI, gA.toSubject(), gF, 5, gI, gF.toSubject(), gD, 4);

      // SA -> gI (parent: gA -> gI) (depth: 6)
      MembershipTestHelper.verifyEffectiveMembership(r.rs, "SA -> gI - depth 6", gI, subjA, gA, 6, gI, gA.toSubject(), gF, 5);

      // gH -> gJ
      MembershipTestHelper.verifyImmediateMembership(r.rs, "gH -> gJ", gJ, gH.toSubject());

      // gC -> gJ (parent: gH -> gJ) (depth: 1)
      MembershipTestHelper.verifyEffectiveMembership(r.rs, "gC -> gJ - depth 1", gJ, gC.toSubject(), gH, 1, gJ, gH.toSubject(), null, 0);

      // gB -> gJ (parent: gC -> gJ) (depth: 2)
      MembershipTestHelper.verifyEffectiveMembership(r.rs, "gB -> gJ - depth 2", gJ, gB.toSubject(), gC, 2, gJ, gC.toSubject(), gH, 1);

      // gK -> gJ (parent: gC -> gJ) (depth: 2)
      MembershipTestHelper.verifyEffectiveMembership(r.rs, "gK -> gJ - depth 2", gJ, gK.toSubject(), gC, 2, gJ, gC.toSubject(), gH, 1);

      // gA -> gJ (parent: gB -> gJ) (depth: 3)
      MembershipTestHelper.verifyEffectiveMembership(r.rs, "gA -> gJ - depth 3", gJ, gA.toSubject(), gB, 3, gJ, gB.toSubject(), gC, 2);

      // gD -> gJ (parent: gB -> gJ) (depth: 3)
      MembershipTestHelper.verifyEffectiveMembership(r.rs, "gD -> gJ - depth 3", gJ, gD.toSubject(), gB, 3, gJ, gB.toSubject(), gC, 2);

      // gE -> gJ (parent: gD -> gJ) (depth: 4)
      MembershipTestHelper.verifyEffectiveMembership(r.rs, "gE -> gJ - depth 4", gJ, gE.toSubject(), gD, 4, gJ, gD.toSubject(), gB, 3);

      // gF -> gJ (parent: gD -> gJ) (depth: 4)
      MembershipTestHelper.verifyEffectiveMembership(r.rs, "gF -> gJ - depth 4", gJ, gF.toSubject(), gD, 4, gJ, gD.toSubject(), gB, 3);

      // SA -> gJ (parent: gA -> gJ) (depth: 4)
      MembershipTestHelper.verifyEffectiveMembership(r.rs, "SA -> gJ - depth 4", gJ, subjA, gA, 4, gJ, gA.toSubject(), gB, 3);

      // gA -> gJ (parent: gF -> gJ) (depth: 5)
      MembershipTestHelper.verifyEffectiveMembership(r.rs, "gA -> gJ - depth 5", gJ, gA.toSubject(), gF, 5, gJ, gF.toSubject(), gD, 4);

      // SA -> gJ (parent: gA -> gJ) (depth: 6)
      MembershipTestHelper.verifyEffectiveMembership(r.rs, "SA -> gJ - depth 6", gJ, subjA, gA, 6, gJ, gA.toSubject(), gF, 5);

      // gB -> gL
      MembershipTestHelper.verifyImmediateMembership(r.rs, "gB -> gL", gL, gB.toSubject());

      // gA -> gL (parent: gB -> gL) (depth: 1)
      MembershipTestHelper.verifyEffectiveMembership(r.rs, "gA -> gL - depth 1", gL, gA.toSubject(), gB, 1, gL, gB.toSubject(), null, 0);

      // gD -> gL (parent: gB -> gL) (depth: 1)
      MembershipTestHelper.verifyEffectiveMembership(r.rs, "gD -> gL - depth 1", gL, gD.toSubject(), gB, 1, gL, gB.toSubject(), null, 0);

      // gE -> gL (parent: gD -> gL) (depth: 2)
      MembershipTestHelper.verifyEffectiveMembership(r.rs, "gE -> gL - depth 2", gL, gE.toSubject(), gD, 2, gL, gD.toSubject(), gB, 1);

      // gF -> gL (parent: gD -> gL) (depth: 2)
      MembershipTestHelper.verifyEffectiveMembership(r.rs, "gF -> gL - depth 2", gL, gF.toSubject(), gD, 2, gL, gD.toSubject(), gB, 1);

      // SA -> gL (parent: gA -> gL) (depth: 2)
      MembershipTestHelper.verifyEffectiveMembership(r.rs, "SA -> gL - depth 2", gL, subjA, gA, 2, gL, gA.toSubject(), gB, 1);

      // gA -> gL (parent: gF -> gL) (depth: 3)
      MembershipTestHelper.verifyEffectiveMembership(r.rs, "gA -> gL - depth 3", gL, gA.toSubject(), gF, 3, gL, gF.toSubject(), gD, 2);

      // SA -> gL (parent: gA -> gL) (depth: 4)
      MembershipTestHelper.verifyEffectiveMembership(r.rs, "SA -> gL - depth 4", gL, subjA, gA, 4, gL, gA.toSubject(), gF, 3);

      // verify the total number of memberships
      Set<Membership> allMemberships = MembershipFinder.internal_findAllByCreatedAfter(r.rs, before, Group.getDefaultList());
      T.amount("Number of memberships", 73, allMemberships.size());

      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  }
}

