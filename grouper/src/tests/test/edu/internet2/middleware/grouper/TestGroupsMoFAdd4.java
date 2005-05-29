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

public class TestGroupsMoFAdd4 extends TestCase {

  public TestGroupsMoFAdd4(String name) {
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
  // Add m0 to gA
  // Add m0 to gB
  // Add gA to gC
  // Add gB to gC
  //
  public void testMoF() {
    Subject subj = null;
    try {
      subj = SubjectFactory.getSubject(Constants.rootI, Constants.rootT);
    } catch (SubjectNotFoundException e1) {
      // TODO Auto-generated catch block
      e1.printStackTrace();
    }
    GrouperSession s = GrouperSession.start(subj);

    // Create ns0
    GrouperStem ns0 = GrouperStem.create(
                         s, Constants.ns0s, Constants.ns0e
                       );
    // Create ns1
    GrouperStem ns1 = GrouperStem.create(
                         s, Constants.ns1s, Constants.ns1e
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
    GrouperMember m0 = Common.loadMember(
                         s, Constants.mem0I, Constants.mem0T
                       );
    // Add m0 to gA's "members"
    try {
      gA.listAddVal(m0);
    } catch (RuntimeException e) {
      Assert.fail("add m0 to gA");
    }
    // Add m0 to gB's "members"
    try {
      gB.listAddVal(m0);
    } catch (RuntimeException e) {
      Assert.fail("add m0 to gB");
    }
    // Add gA to gC's "members"
    try {
      gC.listAddVal(gA.toMember());
    } catch (RuntimeException e) {
      Assert.fail("add gA to gC");
    }
    // Add gB to gC's "members"
    try {
      gC.listAddVal(gB.toMember());
    } catch (RuntimeException e) {
      Assert.fail("add gB to gC");
    }

    /*
     * m0 -> gA
     */
    // Now inspect gA's, resulting list values
    Assert.assertTrue(
      "gA members == 1", gA.listVals("members").size() == 1
    );
    Assert.assertTrue(
      "gA imm members == 1", gA.listImmVals("members").size() == 1
    );
    Assert.assertTrue(
      "gA eff members == 0", gA.listEffVals("members").size() == 0
    );

    /*
     * m0 -> gB
     */
    // Now inspect gB's, resulting list values
    Assert.assertTrue(
      "gB members == 1", gB.listVals("members").size() == 1
    );
    Assert.assertTrue(
      "gB imm members == 1", gB.listImmVals("members").size() == 1
    );
    Assert.assertTrue(
      "gB eff members == 0", gB.listEffVals("members").size() == 0
    );

    /*
     * gA -> gC
     * gB -> gC
     * m0 -> gA -> gC
     *    => gA -> gC
     * m0 -> gB -> gC
     *    => gB -> gC
     */
    // Now inspect gC's, resulting list values
    Assert.assertTrue(
      "gC members == 4", gC.listVals("members").size() == 4
    );
    Assert.assertTrue(
      "gC imm members == 2", gC.listImmVals("members").size() == 2
    );
    Assert.assertTrue(
      "gC eff members == 2", gC.listEffVals("members").size() == 2
    );
    Iterator iterCE = gC.listEffVals().iterator();
    while (iterCE.hasNext()) {
      GrouperList lv = (GrouperList) iterCE.next();
      if (lv.chain().size() == 1) {
        Assert.assertTrue("gC chain == 1", true);
        Assert.assertEquals("gC member() == m0", m0, lv.member());
        if        (lv.via().equals(gA)) {
          Assert.assertTrue("gC via() == gA", true);
          Iterator iterVia = lv.chain().iterator();
          while (iterVia.hasNext()) {
            MemberVia   mv  = (MemberVia) iterVia.next();
            GrouperList lvv = mv.toList(s);
            Assert.assertTrue("gC via g == gC", lvv.group().equals(gC));
            Assert.assertTrue(
              "gC via m == gA", lvv.member().toGroup().equals(gA)
            );
          }
        } else if (lv.via().equals(gB)) {
          Assert.assertTrue("gC via() == gB", true);
          Iterator iterVia = lv.chain().iterator();
          while (iterVia.hasNext()) {
            MemberVia   mv  = (MemberVia) iterVia.next();
            GrouperList lvv = mv.toList(s);
            Assert.assertTrue("gC via g == gC", lvv.group().equals(gC));
            Assert.assertTrue(
              "gC via m == gB", lvv.member().toGroup().equals(gB)
            );
          }
        } else {
          Assert.fail("gC via != (gA, gB)");
        }
      } else {
        Assert.fail("gC chain != 1");
      }
    }

    s.stop();
  }

}

