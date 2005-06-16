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

public class TestAccessRevoke1 extends TestCase {

  public TestAccessRevoke1(String name) {
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
  //
  // Revoke ADMIN from m0 on gA
  // m0 -> gA
  // gA (A)-> gB
  // m0 (A)!-> gB
  // gA (A)!-> gB
  //
  public void testRevoke() {
    try {
      Subject subj = SubjectFactory.getSubject(Constants.rootI, Constants.rootT);
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
      GrouperMember m0 = Common.loadMember(
        s, Constants.mem0I, Constants.mem0T
      );
      // Make m0 a member of gA
      gA.listAddVal(m0);
      // Grant ADMIN to gA on gB
      Assert.assertTrue(
        "grant ADMIn gA -> gB",
        s.access().grant(s, gB, gA.toMember(), Grouper.PRIV_ADMIN)
      );
      // Assert privs
      Assert.assertTrue(
        "m0 has == 0 privs on gA", 
        s.access().has(s, gA, m0).size() == 0
      );
      Assert.assertFalse(
        "m0 !ADMIN on gA", 
        s.access().has(s, gA, m0, Grouper.PRIV_ADMIN)
      );
      Assert.assertFalse(
        "m0 !UPDATE on gA", 
        s.access().has(s, gA, m0, Grouper.PRIV_UPDATE)
      );
      Assert.assertTrue(
        "m0 has == 1 privs on gB", 
        s.access().has(s, gB, m0).size() == 1
      );
      Assert.assertTrue(
        "m0 ADMIN on gB", 
        s.access().has(s, gB, m0, Grouper.PRIV_ADMIN)
      );
      Assert.assertFalse(
        "m0 !UPDATE on gB", 
        s.access().has(s, gB, m0, Grouper.PRIV_UPDATE)
      );
      Assert.assertTrue(
        "gA has == 1 privs on gB", 
        s.access().has(s, gB, gA.toMember()).size() == 1
      );
      Assert.assertTrue(
        "gA ADMIN on gB", 
        s.access().has(s, gB, gA.toMember(), Grouper.PRIV_ADMIN)
      );
      Assert.assertFalse(
        "gA !UPDATE on gB", 
        s.access().has(s, gB, gA.toMember(), Grouper.PRIV_UPDATE)
      );
      // Revoke ADMIN gA -> gB
      Assert.assertTrue(
        "revoke gA ADMIN on gB", 
        s.access().revoke(s, gB, gA.toMember(), Grouper.PRIV_ADMIN)
      );
      // Assert privs
      Assert.assertTrue(
        "m0 has == 0 privs on gA", 
        s.access().has(s, gA, m0).size() == 0
      );
      Assert.assertFalse(
        "m0 !ADMIN on gA", 
        s.access().has(s, gA, m0, Grouper.PRIV_ADMIN)
      );
      Assert.assertFalse(
        "m0 !UPDATE on gA", 
        s.access().has(s, gA, m0, Grouper.PRIV_UPDATE)
      );
      Assert.assertTrue(
        "m0 has == 0 privs on gB", 
        s.access().has(s, gB, m0).size() == 0
      );
      Assert.assertFalse(
        "m0 !ADMIN on gB", 
        s.access().has(s, gB, m0, Grouper.PRIV_ADMIN)
      );
      Assert.assertFalse(
        "m0 !UPDATE on gB", 
        s.access().has(s, gB, m0, Grouper.PRIV_UPDATE)
      );
      Assert.assertTrue(
        "gA has == 0 privs on gB", 
        s.access().has(s, gB, gA.toMember()).size() == 0
      );
      Assert.assertFalse(
        "gA !ADMIN on gB", 
        s.access().has(s, gB, gA.toMember(), Grouper.PRIV_ADMIN)
      );
      Assert.assertFalse(
        "gA !UPDATE on gB", 
        s.access().has(s, gB, gA.toMember(), Grouper.PRIV_UPDATE)
      );
      s.stop();
    } catch (SubjectNotFoundException e) {
      Assert.fail("unable to get subject");
    }
  }

  //
  //
  // Revoke ADMIN from ALL on gA
  // m0 -> gA
  // gA (A)-> gB
  // m0 (A)!-> gB
  // gA (A)!-> gB
  //
  // grouperzilla#361
  //
  public void testRevokeAll() {
    try {
      Subject subj = SubjectFactory.getSubject(Constants.rootI, Constants.rootT);
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
      GrouperMember m0 = Common.loadMember(
        s, Constants.mem0I, Constants.mem0T
      );
      // Make m0 a member of gA
      gA.listAddVal(m0);
      // Grant ADMIN to gA on gB
      Assert.assertTrue(
        "grant ADMIn gA -> gB",
        s.access().grant(s, gB, gA.toMember(), Grouper.PRIV_ADMIN)
      );
      // Assert privs
      Assert.assertTrue(
        "m0 has == 0 privs on gA", 
        s.access().has(s, gA, m0).size() == 0
      );
      Assert.assertFalse(
        "m0 !ADMIN on gA", 
        s.access().has(s, gA, m0, Grouper.PRIV_ADMIN)
      );
      Assert.assertFalse(
        "m0 !UPDATE on gA", 
        s.access().has(s, gA, m0, Grouper.PRIV_UPDATE)
      );
      Assert.assertTrue(
        "m0 has == 1 privs on gB", 
        s.access().has(s, gB, m0).size() == 1
      );
      Assert.assertTrue(
        "m0 ADMIN on gB", 
        s.access().has(s, gB, m0, Grouper.PRIV_ADMIN)
      );
      Assert.assertFalse(
        "m0 !UPDATE on gB", 
        s.access().has(s, gB, m0, Grouper.PRIV_UPDATE)
      );
      Assert.assertTrue(
        "gA has == 1 privs on gB", 
        s.access().has(s, gB, gA.toMember()).size() == 1
      );
      Assert.assertTrue(
        "gA ADMIN on gB", 
        s.access().has(s, gB, gA.toMember(), Grouper.PRIV_ADMIN)
      );
      Assert.assertFalse(
        "gA !UPDATE on gB", 
        s.access().has(s, gB, gA.toMember(), Grouper.PRIV_UPDATE)
      );
      // Revoke ADMIN ALL -> gB
      Assert.assertTrue(
        "revoke ALL ADMIN on gB", 
        s.access().revoke(s, gB, Grouper.PRIV_ADMIN)
      );
      // Assert privs
      Assert.assertTrue(
        "m0 has == 0 privs on gA", 
        s.access().has(s, gA, m0).size() == 0
      );
      Assert.assertFalse(
        "m0 !ADMIN on gA", 
        s.access().has(s, gA, m0, Grouper.PRIV_ADMIN)
      );
      Assert.assertFalse(
        "m0 !UPDATE on gA", 
        s.access().has(s, gA, m0, Grouper.PRIV_UPDATE)
      );
      Assert.assertTrue(
        "m0 has == 0 privs on gB", 
        s.access().has(s, gB, m0).size() == 0
      );
      Assert.assertFalse(
        "m0 !ADMIN on gB", 
        s.access().has(s, gB, m0, Grouper.PRIV_ADMIN)
      );
      Assert.assertFalse(
        "m0 !UPDATE on gB", 
        s.access().has(s, gB, m0, Grouper.PRIV_UPDATE)
      );
      Assert.assertTrue(
        "gA has == 0 privs on gB", 
        s.access().has(s, gB, gA.toMember()).size() == 0
      );
      Assert.assertFalse(
        "gA !ADMIN on gB", 
        s.access().has(s, gB, gA.toMember(), Grouper.PRIV_ADMIN)
      );
      Assert.assertFalse(
        "gA !UPDATE on gB", 
        s.access().has(s, gB, gA.toMember(), Grouper.PRIV_UPDATE)
      );
      s.stop();
    } catch (SubjectNotFoundException e) {
      Assert.fail("unable to get subject");
    }
  }
}

