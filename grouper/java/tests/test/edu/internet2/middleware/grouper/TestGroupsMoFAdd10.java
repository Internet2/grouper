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

public class TestGroupsMoFAdd10 extends TestCase {

  public TestGroupsMoFAdd10(String name) {
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
  // Add gA to gB
  // Add gB to gC
  // Add gC to gD
  //
  // m0 -> gA -> gB 
  //       ^     |
  //       |     v
  //       \---- gC
  //
  public void testMoF() {
    Subject subj = GrouperSubject.load(Constants.rootI, Constants.rootT);
    GrouperSession s = GrouperSession.start(subj);

    // Create ns0
    GrouperGroup ns0 = GrouperGroup.create(
                         s, Constants.ns0s, Constants.ns0e, Grouper.NS_TYPE
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
    Assert.assertTrue("add m0 to gA", gA.listAddVal(m0));
    // Add gA to gB's "members"
    Assert.assertTrue("add gA to gB", gB.listAddVal(gA.toMember()));
    // Add gB to gC's "members"
    Assert.assertTrue("add gB to gC", gC.listAddVal(gB.toMember()));
    // Add gC to gA's "members"
    Assert.assertTrue("add gC to gA", gA.listAddVal(gC.toMember()));

    // Now inspect gA's, resulting list values
    Assert.assertTrue(
      "gA members == 5", gA.listVals("members").size() == 5
    );
    Assert.assertTrue(
      "gA imm members == 2", gA.listImmVals("members").size() == 2
    );
    Assert.assertTrue(
      "gA eff members == 3", gA.listEffVals("members").size() == 3
    );

    // Now inspect gB's, resulting list values
    Assert.assertTrue(
      "gB members == 6", gB.listVals("members").size() == 6
    );
    Assert.assertTrue(
      "gB imm members == 1", gB.listImmVals("members").size() == 1
    );
    Assert.assertTrue(
      "gB eff members == 5", gB.listEffVals("members").size() == 5
    );

    // Now inspect gC's, resulting list values
    Assert.assertTrue(
      "gC members == 7", gC.listVals("members").size() == 7
    );
    Assert.assertTrue(
      "gC imm members == 1", gC.listImmVals("members").size() == 1
    );
    Assert.assertTrue(
      "gC eff members == 6", gC.listEffVals("members").size() == 6
    );

    s.stop();
  }

}

