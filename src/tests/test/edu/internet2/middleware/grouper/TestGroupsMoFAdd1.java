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

public class TestGroupsMoFAdd1 extends TestCase {

  public TestGroupsMoFAdd1(String name) {
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
  // Add m0 to g0
  // Add m0 to g1
  //
  // m0 -> g0
  //  \--> g1
  //
  public void testMoF() {
    Subject subj = GrouperSubject.load(Constants.rootI, Constants.rootT);
    GrouperSession s = GrouperSession.start(subj);

    // Create ns0
    GrouperGroup ns0 = GrouperGroup.create(
                         s, Constants.ns0s, Constants.ns0e, Grouper.NS_TYPE
                       );
    Assert.assertNotNull("ns0 !null", ns0);
    // Create ns1
    GrouperGroup ns1 = GrouperGroup.create(
                         s, Constants.ns1s, Constants.ns1e, Grouper.NS_TYPE
                       );
    Assert.assertNotNull("ns0 !null", ns0);
    // Create g0
    GrouperGroup g0  = GrouperGroup.create(
                         s, Constants.g0s, Constants.g0e
                       );
    Assert.assertNotNull("g0 !null", g0);
    // Create g1
    GrouperGroup g1  = GrouperGroup.create(
                         s, Constants.g1s, Constants.g1e
                       );
    Assert.assertNotNull("g1 !null", g1);
    // Load m0
    GrouperMember m0 = GrouperMember.load(
                         s, Constants.mem0I, Constants.mem0T
                       );
    Assert.assertNotNull("m0 !null", m0);
    // Add m0 to g0's "members"
    Assert.assertTrue("add m0 to g0", g0.listAddVal(m0));
    // Add m0 to g1's "members"
    Assert.assertTrue("add m0 to g1", g1.listAddVal(m0));

    // Now inspect g0's, resulting list values
    Assert.assertTrue(
      "members == 1", g0.listVals("members").size() == 1
    );
    Assert.assertTrue(
      "imm members == 1", g0.listImmVals("members").size() == 1
    );
    Assert.assertTrue(
      "eff members == 0", g0.listEffVals("members").size() == 0
    );

    // Now inspect g1's, resulting list values
    Assert.assertTrue(
      "members == 1", g1.listVals("members").size() == 1
    );
    Assert.assertTrue(
      "imm members == 1", g1.listImmVals("members").size() == 1
    );
    Assert.assertTrue(
      "eff members == 0", g1.listEffVals("members").size() == 0
    );

    s.stop();
  }

}

