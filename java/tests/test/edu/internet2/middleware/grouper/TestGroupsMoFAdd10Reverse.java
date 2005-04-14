/*
 * Copyright (C) 2004-2005 University Corporation for Advanced Internet Development, Inc.
 * Copyright (C) 2004-2005 The University Of Chicago
 * All Rights Reserved. 
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *  * Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *  * Neither the name of the University of Chicago nor the names
 *    of its contributors nor the University Corporation for Advanced
 *   Internet Development, Inc. may be used to endorse or promote
 *   products derived from this software without explicit prior
 *   written permission.
 *
 * You are under no obligation whatsoever to provide any enhancements
 * to the University of Chicago, its contributors, or the University
 * Corporation for Advanced Internet Development, Inc.  If you choose
 * to provide your enhancements, or if you choose to otherwise publish
 * or distribute your enhancements, in source code form without
 * contemporaneously requiring end users to enter into a separate
 * written license agreement for such enhancements, then you thereby
 * grant the University of Chicago, its contributors, and the University
 * Corporation for Advanced Internet Development, Inc. a non-exclusive,
 * royalty-free, perpetual license to install, use, modify, prepare
 * derivative works, incorporate into the software or other computer
 * software, distribute, and sublicense your enhancements or derivative
 * works thereof, in binary and source code form.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND WITH ALL FAULTS.  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING,
 * BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE, AND NON-INFRINGEMENT ARE DISCLAIMED AND the
 * entire risk of satisfactory quality, performance, accuracy, and effort
 * is with LICENSEE. IN NO EVENT SHALL THE COPYRIGHT OWNER, CONTRIBUTORS,
 * OR THE UNIVERSITY CORPORATION FOR ADVANCED INTERNET DEVELOPMENT, INC.
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
 * BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OR DISTRIBUTION OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package test.edu.internet2.middleware.grouper;

import  edu.internet2.middleware.grouper.*;
import  edu.internet2.middleware.subject.*;
import  java.util.*;
import  junit.framework.*;

public class TestGroupsMoFAdd10Reverse extends TestCase {

  public TestGroupsMoFAdd10Reverse(String name) {
    super(name);
  }

  protected void setUp () {
    DB db = new DB();
    db.emptyTables();
    db.stop();
  }

  protected void tearDown () {
    // Nothing -- Yet
  }


  /*
   * TESTS
   */
  
  //
  // m0 -> gA -> gB 
  //       ^     |
  //       |     v
  //       \---- gC
  //
  // TODO The code passes but I can't map this out quite right.  Troubling.
  /*
   * Add gC to gA
   * [0] gC -> gA 
   */
  /*
   * Add gB to gC
   * [0] gB -> gC
   * [1] gB -> gC -> gA
   *        => gC -> gA
   */
  /*
   * Add gA to gB
   * [0] gA -> gB
   * [1] gA -> gB -> gC
   *        => gB -> gC
   * [1] gA -> gB -> gC -> gA
   *        => gB -> gC
   *        => gC -> gA
   * [2] gC -> gA -> gB
   *        => gA -> gB
   * [2] gB -> gC -> gA -> gB
   *        => gC -> gA
   *        => gA -> gB
   * [3] gC -> gA -> gB -> gC
   *        => gA -> gB
   *        => gB -> gC
   * [3] gC -> gA -> gB -> gC -> gA
   *        => gA -> gB
   *        => gB -> gC
   *        => gC -> gA
   * [3] gB -> gC -> gA -> gB -> gC
   *        => gC -> gA
   *        => gA -> gB
   *        => gB -> gC
   * [3] gB -> gC -> gA -> gB -> gC -> gA
   *        => gC -> gA
   *        => gA -> gB
   *        => gB -> gC
   *        => gC -> gA
   */
  /*
   * Add m0 to gA
   * [0] m0 -> gA
   * [1] m0 -> gA -> gB
   *        => gA -> gB
   * [1] m0 -> gA -> gB -> gC
   *        => gA -> gB
   *        => gA -> gB
   * ???
   */
  public void testMoF() {
    Subject subj = GrouperSubject.load(Constants.rootI, Constants.rootT);
    GrouperSession s = GrouperSession.start(subj);

    // Create ns0
    GrouperStem ns0 = GrouperStem.create(
                         s, Constants.ns0s, Constants.ns0e
                       );
    // Create gA
    GrouperGroup gA  = GrouperGroup.create(
                         s, Constants.gAs, Constants.gAe
                       );
    // Create gB
    GrouperGroup gB  = GrouperGroup.create(
                         s, Constants.gBs, Constants.gBe
                       );
    // Create gC
    GrouperGroup gC  = GrouperGroup.create(
                         s, Constants.gCs, Constants.gCe
                       );
    // Load m0
    GrouperMember m0 = GrouperMember.load(
                         s, Constants.mem0I, Constants.mem0T
                       );
    // Add m0 to gA's "members"
    try {
      gA.listAddVal(m0);
    } catch (RuntimeException e) {
      Assert.fail("add m0 to gA");
    }
    // Add gA to gB's "members"
    try {
      gB.listAddVal(gA.toMember());
    } catch (RuntimeException e) {
      Assert.fail("add gA to gB");
    }
    // Add gB to gC's "members"
    try {
      gC.listAddVal(gB.toMember());
    } catch (RuntimeException e) {
      Assert.fail("add gB to gC");
    }
    // Add gC to gA's "members"
    try {
      gA.listAddVal(gC.toMember());
    } catch (RuntimeException e) {
      Assert.fail("add gC to gA");
    }

    // Now inspect gA's, resulting list values
    /*
     * m0 -> gA
     * gC -> gA
     * gB -> gC -> gA
     *    => gC -> gA
     * gA -> gB -> gC -> gA
     *    => gB -> gC
     *    => gC -> gA
     * m0 -> gA -> gB -> gC -> gA
     *    => gA -> gB 
     *    => gB -> gC
     *    => gC -> gA
     */
    Assert.assertTrue(
      "gA members == 5", gA.listVals("members").size() == 5
    );
    Assert.assertTrue(
      "gA imm members == 2", gA.listImmVals("members").size() == 2
    );
    Assert.assertTrue(
      "gA eff members == 3", gA.listEffVals("members").size() == 3
    );
    Iterator iterAI = gA.listImmVals("members").iterator();
    while (iterAI.hasNext()) {
      GrouperList lv = (GrouperList) iterAI.next();
      Assert.assertTrue("gA empty chain", lv.chain().size() == 0);
      Assert.assertNull("gA null via", lv.via());
      if        (lv.member().equals(m0)) {
        Assert.assertTrue("gA imm == m0", true);
      } else if (lv.member().equals(gC.toMember())) {
        Assert.assertTrue("gA imm == gC", true);
      } else {
        Assert.fail("gA imm != (m0, gC)");
      }
    }
    Iterator iterAE = gA.listEffVals("members").iterator();
    while (iterAE.hasNext()) {
      GrouperList lv = (GrouperList) iterAE.next();
      if        (lv.member().equals(m0)) {
        Assert.assertTrue("gA eff == m0", true);
        Assert.assertTrue(
          "m0 -> gA m == m0", lv.member().equals(m0)
        );
        Assert.assertTrue(
          "m0 -> gA v == gA", lv.via().equals(gA)
        );
        Assert.assertTrue(
          "m0 -> gA chain == 1", lv.chain().size() == 3
        );
        MemberVia mv0 = (MemberVia) lv.chain().get(0);
        Assert.assertTrue(
          "m0 -> gA via[0] g == gB", 
          mv0.toList(s).group().equals(gB)
        );
        Assert.assertTrue(
          "m0 -> gA via[0] m == gA", 
          mv0.toList(s).member().equals(gA.toMember())
        );
        MemberVia mv1 = (MemberVia) lv.chain().get(1);
        Assert.assertTrue(
          "m0 -> gA via[1] g == gC", 
          mv1.toList(s).group().equals(gC)
        );
        Assert.assertTrue(
          "m0 -> gA via[1] m == gB", 
          mv1.toList(s).member().equals(gB.toMember())
        );
        MemberVia mv2 = (MemberVia) lv.chain().get(2);
        Assert.assertTrue(
          "m0 -> gA via[2] g == gA", 
          mv2.toList(s).group().equals(gA)
        );
        Assert.assertTrue(
          "m0 -> gA via[2] m == gC", 
          mv2.toList(s).member().equals(gC.toMember())
        );
      } else if (lv.member().equals(gA.toMember())) {
        Assert.assertTrue("gA eff == gA", true);
        Assert.assertTrue(
          "gA -> gA m == gA", lv.member().equals(gA.toMember())
        );
        Assert.assertTrue(
          "gA -> gA v == gB", lv.via().equals(gB)
        );
        Assert.assertTrue(
          "gA -> gA chain == 1", lv.chain().size() == 2
        );
        MemberVia mv0 = (MemberVia) lv.chain().get(0);
        Assert.assertTrue(
          "gA -> gA via[0] g == gC",
          mv0.toList(s).group().equals(gC)
        );
        Assert.assertTrue(
          "gA -> gA via[0] m == gB",
          mv0.toList(s).member().toGroup().equals(gB)
        );
        MemberVia mv1 = (MemberVia) lv.chain().get(1);
        Assert.assertTrue(
          "gA -> gA via[1] g == gA",
          mv1.toList(s).group().equals(gA)
        );
        Assert.assertTrue(
          "gA -> gA via[1] m == gC",
          mv1.toList(s).member().toGroup().equals(gC)
        );
      } else if (lv.member().equals(gB.toMember())) {
        Assert.assertTrue("gA eff == gB", true);
        Assert.assertTrue(
          "gB -> gA m == gB", lv.member().equals(gB.toMember())
        );
        Assert.assertTrue(
          "gB -> gA v == gC", lv.via().equals(gC)
        );
        Assert.assertTrue(
          "gB -> gA chain == 1", lv.chain().size() == 1
        );
        MemberVia mv0 = (MemberVia) lv.chain().get(0);
        Assert.assertTrue(
          "gB -> gA via[0] g == gA",
          mv0.toList(s).group().equals(gA)
        );
        Assert.assertTrue(
          "gB -> gA via[0] m == gC",
          mv0.toList(s).member().toGroup().equals(gC)
        );
      } else {
        Assert.fail("gA eff != (m0, gA, gB)");
      }
    }

    // Now inspect gB's, resulting list values
    /*
     * gA -> gB
     * m0 -> gA -> gB
     *    => gA -> gB
     * gC -> gA -> gB
     *    => gA -> gB
     * gB -> gC -> gA -> gB
     *    => gC -> gA
     *    => gA -> gB
     * gA -> gB -> gC -> gA -> gB
     *    => gB -> gC
     *    => gC -> gA
     *    => gA -> gB
     * m0 -> gA -> gB -> gC -> gA -> gB
     *    => gA -> gB
     *    => gB -> gC
     *    => gC -> gA
     *    => gA -> gB
     */
    Assert.assertTrue(
      "gB members == 6", gB.listVals("members").size() == 6
    );
    Assert.assertTrue(
      "gB imm members == 1", gB.listImmVals("members").size() == 1
    );
    Assert.assertTrue(
      "gB eff members == 5", gB.listEffVals("members").size() == 5
    );
    Iterator iterBI = gB.listImmVals("members").iterator();
    while (iterBI.hasNext()) {
      GrouperList lv = (GrouperList) iterBI.next();
      Assert.assertTrue("gB empty chain", lv.chain().size() == 0);
      Assert.assertNull("gB null via", lv.via());
      Assert.assertTrue(
        "gB imm == gA", lv.member().equals(gA.toMember())
      );
    }
    Iterator iterBE = gB.listEffVals("members").iterator();
    while (iterBE.hasNext()) {
      GrouperList lv = (GrouperList) iterBE.next();
      if        (lv.member().equals(m0)) {
        Assert.assertTrue("gB eff == m0", true);
        if        (lv.chain().size() == 1) {
          Assert.assertTrue("m0 -> gB chain == 1", true);
          MemberVia mv0 = (MemberVia) lv.chain().get(0);
          Assert.assertTrue(
            "m0 -> gB via[0] g == gB",
            mv0.toList(s).group().equals(gB)
          );
          Assert.assertTrue(
            "m0 -> gB via[0] m == gA",
            mv0.toList(s).member().toGroup().equals(gA)
          );
        } else if (lv.chain().size() == 4) {
          Assert.assertTrue("m0 -> gB chain == 4", true);
          MemberVia mv0 = (MemberVia) lv.chain().get(0);
          Assert.assertTrue(
            "m0 -> gB via[0] g == gB",
            mv0.toList(s).group().equals(gB)
          );
          Assert.assertTrue(
            "m0 -> gB via[0] m == gA",
            mv0.toList(s).member().toGroup().equals(gA)
          );
          MemberVia mv1 = (MemberVia) lv.chain().get(1);
          Assert.assertTrue(
            "m0 -> gB via[1] g == gC",
            mv1.toList(s).group().equals(gC)
          );
          Assert.assertTrue(
            "m0 -> gB via[1] m == gB",
            mv1.toList(s).member().toGroup().equals(gB)
          );
          MemberVia mv2 = (MemberVia) lv.chain().get(2);
          Assert.assertTrue(
            "m0 -> gB via[2] g == gA",
            mv2.toList(s).group().equals(gA)
          );
          Assert.assertTrue(
            "m0 -> gB via[2] m == gC",
            mv2.toList(s).member().toGroup().equals(gC)
          );
          MemberVia mv3 = (MemberVia) lv.chain().get(3);
          Assert.assertTrue(
            "m0 -> gB via[3] g == gB",
            mv3.toList(s).group().equals(gB)
          );
          Assert.assertTrue(
            "m0 -> gB via[3] m == gA",
            mv3.toList(s).member().toGroup().equals(gA)
          );
        } else {
          Assert.fail("m0 -> gB chain != (1,4)");
        }
      } else if (lv.member().equals(gA.toMember())) {
        Assert.assertTrue("gB eff == gA", true);
        Assert.assertTrue(
          "gB gC via == gB", lv.via().equals(gB)
        );
        Assert.assertTrue("gA -> gB chain == 3", lv.chain().size() == 3);
        MemberVia mv0 = (MemberVia) lv.chain().get(0);
        Assert.assertTrue(
          "gA -> gB via[0] g = gC",
          mv0.toList(s).group().equals(gC)
        );
        Assert.assertTrue(
          "gA -> gB via[0] m = gB",
          mv0.toList(s).member().equals(gB.toMember())
        );
        MemberVia mv1 = (MemberVia) lv.chain().get(1);
        Assert.assertTrue(
          "gA -> gB via[1] g = gA",
          mv1.toList(s).group().equals(gA)
        );
        Assert.assertTrue(
          "gA -> gB via[1] m = gC",
          mv1.toList(s).member().equals(gC.toMember())
        );
        MemberVia mv2 = (MemberVia) lv.chain().get(2);
        Assert.assertTrue(
          "gA -> gB via[2] g = gC",
          mv2.toList(s).group().equals(gB)
        );
        Assert.assertTrue(
          "gA -> gB via[2] m = gB",
          mv2.toList(s).member().equals(gA.toMember())
        );
      } else if (lv.member().equals(gB.toMember())) {
        Assert.assertTrue("gB eff == gB", true);
        Assert.assertTrue(
          "gB -> gB via == gC", lv.via().equals(gC)
        );
        MemberVia mv0 = (MemberVia) lv.chain().get(0);
        Assert.assertTrue(
          "gB -> gB via[0] g == gA",
          mv0.toList(s).group().equals(gA)
        );
        Assert.assertTrue(
          "gB -> gB via[0] m == gC",
          mv0.toList(s).member().equals(gC.toMember())
        );
        MemberVia mv1 = (MemberVia) lv.chain().get(1);
        Assert.assertTrue(
          "gB -> gB via[1] g == gB",
          mv1.toList(s).group().equals(gB)
        );
        Assert.assertTrue(
          "gB -> gB via[1] m == gA",
          mv1.toList(s).member().equals(gA.toMember())
        );
      } else if (lv.member().equals(gC.toMember())) {
        Assert.assertTrue("gB eff == gC", true);
        Assert.assertTrue(
          "gB gC via == gA", lv.via().equals(gA)
        );
        Assert.assertTrue("gB chain == 1", lv.chain().size() == 1);
        MemberVia mv0 = (MemberVia) lv.chain().get(0);
        Assert.assertTrue(
          "gc -> gB via[1] g == gB", 
          mv0.toList(s).group().equals(gB)
        );
        Assert.assertTrue(
          "gC -> gB via[1] m == gA", 
          mv0.toList(s).member().equals(gA.toMember())
        );
      } else {
        Assert.fail("gB eff != (m0, gA, gB, gC)");
      }
    }

    // Now inspect gC's, resulting list values
    /*
     * gB -> gC
     * gA -> gB -> gC
     *    => gB -> gC
     * m0 -> gA -> gB -> gC
     *    => gA -> gB
     *    => gB -> gC
     * gC -> gA -> gB -> gC
     *    => gA -> gB
     *    => gB -> gC
     * gB -> gC -> gA -> gB -> gC
     *    => gC -> gA
     *    => gA -> gB
     *    => gB -> gC
     * gA -> gB -> gC -> gA -> gB -> gC
     *    => gB -> gC
     *    => gC -> gA
     *    => gA -> gB
     *    => gB -> gC
     * m0 -> gA -> gB -> gC -> gA -> gB -> gC
     *    => gA -> gB
     *    => gB -> gC
     *    => gC -> gA
     *    => gA -> gB
     *    => gB -> gC
     */
    Assert.assertTrue(
      "gC members == 7", gC.listVals("members").size() == 7
    );
    Assert.assertTrue(
      "gC imm members == 1", gC.listImmVals("members").size() == 1
    );
    Assert.assertTrue(
      "gC eff members == 6", gC.listEffVals("members").size() == 6
    );
    Iterator iterCI = gC.listImmVals("members").iterator();
    while (iterCI.hasNext()) {
      GrouperList lv = (GrouperList) iterCI.next();
      Assert.assertTrue("gC empty chain", lv.chain().size() == 0);
      Assert.assertNull("gC null via", lv.via());
    }
    Iterator iterCE = gC.listEffVals("members").iterator();
    while (iterCE.hasNext()) {
      GrouperList lv = (GrouperList) iterCE.next();
      Assert.assertNotNull("gC !null via", lv.via());
      if        (lv.member().equals(m0)) {
        Assert.assertTrue(
          "m0 -> gc via == gA", lv.via().equals(gA)
        );
        Assert.assertTrue("gC eff == m0", true);
        if        (lv.chain().size() == 2) {
          Assert.assertTrue("m0 -> gC chain == 2", true);
        } else if (lv.chain().size() == 5) {
          Assert.assertTrue("m0 -> gC chain == 5", true);
        } else {
          Assert.fail("m0 -> gC chain != (2,5)");
        }
      } else if (lv.member().equals(gA.toMember())) {
        Assert.assertTrue("gC eff == gA", true);
        Assert.assertTrue(
          "gA -> gC via == gB", lv.via().equals(gB)
        );
        if        (lv.chain().size() == 1) {
          Assert.assertTrue("gA -> gC chain == 1", true);
        } else if (lv.chain().size() == 4) {
          Assert.assertTrue("gA -> gC chain == 4", true);
        } else {
          Assert.fail("gA -> gC chain != (1,4)");
        }
      } else if (lv.member().equals(gB.toMember())) {
        Assert.assertTrue("gC eff == gB", true);
        Assert.assertTrue(
          "gB -> gC via == gC", lv.via().equals(gC)
        );
        Assert.assertTrue(
          "gB -> gC chain == 3", lv.chain().size() == 3
        );
        MemberVia mv0 = (MemberVia) lv.chain().get(0);
        Assert.assertTrue(
          "gB -> gC via[0] g == gA",
          mv0.toList(s).group().equals(gA)
        );
        Assert.assertTrue(
          "gB -> gC via[0] m == gC",
          mv0.toList(s).member().equals(gC.toMember())
        );
        MemberVia mv1 = (MemberVia) lv.chain().get(1);
        Assert.assertTrue(
          "gB -> gC via[1] g == gB",
          mv1.toList(s).group().equals(gB)
        );
        Assert.assertTrue(
          "gB -> gC via[1] m == gA",
          mv1.toList(s).member().equals(gA.toMember())
        );
        MemberVia mv2 = (MemberVia) lv.chain().get(2);
        Assert.assertTrue(
          "gB -> gC via[2] g == gC",
          mv2.toList(s).group().equals(gC)
        );
        Assert.assertTrue(
          "gB -> gC via[2] m == gB",
          mv2.toList(s).member().equals(gB.toMember())
        );
      } else if (lv.member().equals(gC.toMember())) {
        Assert.assertTrue("gC eff == gC", true);
        Assert.assertTrue(
          "gC -> gC via == gA", lv.via().equals(gA)
        );
        Assert.assertTrue(
          "gC -> gC chain == 2", lv.chain().size() == 2
        );
      } else {
        Assert.fail("gC eff != (m0, gA, gB, gC)");
      }
    }

    s.stop();
  }

}

