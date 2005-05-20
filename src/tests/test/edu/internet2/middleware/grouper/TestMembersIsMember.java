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

public class TestMembersIsMember extends TestCase {

  public TestMembersIsMember(String name) {
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
  

  // m0 -> gA -> gB -> gC
  //
  public void testMoF() {
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
    // Create gC
    GrouperGroup gC  = GrouperGroup.create(
                         s, Constants.gCs, Constants.gCe
                       );
    // Load m0
    GrouperMember m0 = GrouperMember.load(
                         s, Constants.mem0I, Constants.mem0T
                       );
    // Load m1
    GrouperMember m1 = GrouperMember.load(
                         s, Constants.mem1I, Constants.mem1T
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

    // Now test for membership
    Assert.assertTrue("m0 isMember of gA",   m0.isMember(gA));
    Assert.assertTrue("m0 isMember of gB",   m0.isMember(gB));
    Assert.assertTrue("m0 isMember of gC",   m0.isMember(gC));
    Assert.assertFalse("m1 !isMember of gA", m1.isMember(gA));
    Assert.assertFalse("m1 !isMember of gB", m1.isMember(gB));
    Assert.assertFalse("m1 !isMember of gC", m1.isMember(gC));
    Assert.assertFalse("gA !isMember of gA", gA.toMember().isMember(gA));
    Assert.assertTrue("gA isMember of gB",   gA.toMember().isMember(gB));
    Assert.assertTrue("gA isMember of gC",   gA.toMember().isMember(gC));
    Assert.assertFalse("gB !isMember of gA", gB.toMember().isMember(gA));
    Assert.assertFalse("gB !isMember of gB", gB.toMember().isMember(gB));
    Assert.assertTrue("gB isMember of gC",   gB.toMember().isMember(gC));
    Assert.assertFalse("gC !isMember of gA", gC.toMember().isMember(gA));
    Assert.assertFalse("gC !isMember of gB", gC.toMember().isMember(gB));
    Assert.assertFalse("gC !isMember of gC", gC.toMember().isMember(gC));

    s.stop();
  }

}

