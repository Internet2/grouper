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
import  junit.framework.*;

public class TestBug355 extends TestCase {

  public TestBug355(String name) {
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
  // Add m0 -> gB
  // Add gB -> gA
  // Add gA -> gA/R
  // Del gB -> gA
  // 
  public void testBug355() {
    Subject subj = SubjectFactory.load(Constants.rootI, Constants.rootT);
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
    // Load m0
    GrouperMember m0 = GrouperMember.load(
                         s, Constants.mem0I, Constants.mem0T
                       );
    
    // Make m0 a member of gB
    try {
      gB.listAddVal(m0);
      Assert.assertTrue("add m0 to gB", true);
    } catch (RuntimeException e) {
      Assert.fail("add m0 to gB");
    };
    // Inspect list values
    // gA
    Assert.assertTrue(
      "gA.0 members == 0", gA.listVals().size() == 0
    );
    Assert.assertTrue(
      "gA.0 imm members == 0", gA.listImmVals().size() == 0
    );
    Assert.assertTrue(
      "gA.0 eff members == 0", gA.listEffVals().size() == 0
    );
    Assert.assertTrue(
      "gA.0 members/R == 0", gA.listVals("readers").size() == 0
    );
    Assert.assertTrue(
      "gA.0 imm members/R == 0", gA.listImmVals("readers").size() == 0
    );
    Assert.assertTrue(
      "gA.0 eff members/R == 0", gA.listEffVals("readers").size() == 0
    );
    // gB
    Assert.assertTrue(
      "gB.0 members == 1", gB.listVals().size() == 1
    );
    Assert.assertTrue(
      "gB.0 imm members == 1", gB.listImmVals().size() == 1
    );
    Assert.assertTrue(
      "gB.0 eff members == 0", gB.listEffVals().size() == 0
    );


    // Make gB a member of gA
    try {
      gA.listAddVal(gB.toMember());
      Assert.assertTrue("add gB to gA", true);
    } catch (RuntimeException e) {
      Assert.fail("add gB to gA");
    };
    // Inspect list values
    // gA
    Assert.assertTrue(
      "gA.1 members == 2", gA.listVals().size() == 2
    );
    Assert.assertTrue(
      "gA.1 imm members == 1", gA.listImmVals().size() == 1
    );
    Assert.assertTrue(
      "gA.1j eff members == 1", gA.listEffVals().size() == 1
    );
    Assert.assertTrue(
      "gA.1 members/R == 0", gA.listVals("readers").size() == 0
    );
    Assert.assertTrue(
      "gA.1 imm members/R == 0", gA.listImmVals("readers").size() == 0
    );
    Assert.assertTrue(
      "gA.1 eff members/R == 0", gA.listEffVals("readers").size() == 0
    );
    // gB
    Assert.assertTrue(
      "gB.1 members == 1", gB.listVals().size() == 1
    );
    Assert.assertTrue(
      "gB.1 imm members == 1", gB.listImmVals().size() == 1
    );
    Assert.assertTrue(
      "gB.1 eff members == 0", gB.listEffVals().size() == 0
    );


    // Grant gA READ on gA
    Assert.assertTrue(
      "grant gA READ on gA", 
      s.access().grant(s, gA, gA.toMember(), Grouper.PRIV_READ)
    );
    // Inspect list values
    // gA
    Assert.assertTrue(
      "gA.2 members == 2", gA.listVals().size() == 2
    );
    Assert.assertTrue(
      "gA.2 imm members == 1", gA.listImmVals().size() == 1
    );
    Assert.assertTrue(
      "gA.2 eff members == 1", gA.listEffVals().size() == 1
    );
    Assert.assertTrue(
      "gA.2 members/R == 3", gA.listVals("readers").size() == 3
    );
    Assert.assertTrue(
      "gA.2 imm members/R == 1", gA.listImmVals("readers").size() == 1
    );
    Assert.assertTrue(
      "gA.2 eff members/R == 2", gA.listEffVals("readers").size() == 2
    );
    // gB
    Assert.assertTrue(
      "gB.2 members == 1", gB.listVals().size() == 1
    );
    Assert.assertTrue(
      "gB.2 imm members == 1", gB.listImmVals().size() == 1
    );
    Assert.assertTrue(
      "gB.2 eff members == 0", gB.listEffVals().size() == 0
    );


    // Remove gB from gA
    try {
      gA.listDelVal(gB.toMember());
      Assert.assertTrue("remove gB from gA", true);
    } catch (RuntimeException e) {
      Assert.fail("remove gB from gA");
    };
    // Inspect list values
    // gA
    Assert.assertTrue(
      "gA.3 members == 0", gA.listVals().size() == 0
    );
    Assert.assertTrue(
      "gA.3 imm members == 0", gA.listImmVals().size() == 0
    );
    Assert.assertTrue(
      "gA.3 eff members == 0", gA.listEffVals().size() == 0
    );
    Assert.assertTrue(
      "gA.3 members/R == 1", gA.listVals("readers").size() == 1
    );
    Assert.assertTrue(
      "gA.3 imm members/R == 1", gA.listImmVals("readers").size() == 1
    );
    Assert.assertTrue(
      "gA.3 eff members/R == 0", gA.listEffVals("readers").size() == 0
    );
    // gB
    Assert.assertTrue(
      "gB.3 members == 1", gB.listVals().size() == 1
    );
    Assert.assertTrue(
      "gB.3 imm members == 1", gB.listImmVals().size() == 1
    );
    Assert.assertTrue(
      "gB.3 eff members == 0", gB.listEffVals().size() == 0
    );

    s.stop();
  }

}

