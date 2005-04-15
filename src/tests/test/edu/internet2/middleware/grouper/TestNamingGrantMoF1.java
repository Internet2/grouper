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

public class TestNamingGrantMoF1 extends TestCase {

  public TestNamingGrantMoF1(String name) {
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
  

  public void testMoF() {
    Subject subj = GrouperSubject.load(Constants.rootI, Constants.rootT);
    GrouperSession s = GrouperSession.start(subj);

    // Create ns0
    GrouperStem ns0 = GrouperStem.create(
                         s, Constants.ns0s, Constants.ns0e
                       );
    Assert.assertNotNull("ns0 !null", ns0);
    // Create ns1
    GrouperStem ns1 = GrouperStem.create(
                         s, Constants.ns1s, Constants.ns1e
                       );
    Assert.assertNotNull("ns0 !null", ns0);
    // Load m0
    GrouperMember m0 = GrouperMember.load(
                         s, Constants.mem0I, Constants.mem0T
                       );
    Assert.assertNotNull("m0 !null", m0);
    // Load m1
    GrouperMember m1 = GrouperMember.load(
                         s, Constants.mem1I, Constants.mem1T
                       );
    Assert.assertNotNull("m0 !null", m0);


    // Grant m0 STEM on ns0
    Assert.assertTrue(
      "grant m0 STEM on ns0", 
      s.naming().grant(s, ns0, m0, Grouper.PRIV_STEM)
    );
    // Grant m0 STEM on ns1
    Assert.assertTrue(
      "grant m0 STEM on ns1", 
      s.naming().grant(s, ns1, m0, Grouper.PRIV_STEM)
    );

    // Assert privileges
    Assert.assertTrue(
      "ns0 has == 0 privs on ns0", 
      s.naming().has(s, ns0, ns0.toMember()).size() == 0
    );
    Assert.assertFalse( 
      "ns0 !STEM on ns0",
      s.naming().has(s, ns0, ns0.toMember(), Grouper.PRIV_STEM)
    );
    Assert.assertFalse( 
      "ns0 !CREATE on ns0",
      s.naming().has(s, ns0, ns0.toMember(), Grouper.PRIV_CREATE)
    );

    Assert.assertTrue(
      "ns1 has == 0 privs on ns0", 
      s.naming().has(s, ns0, ns1.toMember()).size() == 0
    );
    Assert.assertFalse( 
      "ns1 !STEM on ns0",
      s.naming().has(s, ns0, ns1.toMember(), Grouper.PRIV_STEM)
    );
    Assert.assertFalse( 
      "ns1 !CREATE on ns0",
      s.naming().has(s, ns0, ns1.toMember(), Grouper.PRIV_CREATE)
    );

    Assert.assertTrue(
      "root has == 2 privs on ns0", 
      s.naming().has(s, ns0).size() == 2
    );
    Assert.assertTrue(
      "root STEM on ns0",
      s.naming().has(s, ns0, Grouper.PRIV_STEM)
    );
    Assert.assertTrue(
      "root CREATE on ns0",
      s.naming().has(s, ns0, Grouper.PRIV_CREATE)
    );

    Assert.assertTrue(
      "m0 has == 1 privs on ns0", 
      s.naming().has(s, ns0, m0).size() == 1
    );
    Assert.assertTrue(
      "m0 STEM on ns0", 
      s.naming().has(s, ns0, m0, Grouper.PRIV_STEM)
    );
    Assert.assertFalse(
      "m0 !CREATE on ns0", 
      s.naming().has(s, ns0, m0, Grouper.PRIV_CREATE)
    );

    Assert.assertTrue(
      "m1 has == 0 privs on ns0", 
      s.naming().has(s, ns0, m1).size() == 0
    );
    Assert.assertFalse(
      "m1 !STEM on ns0", 
      s.naming().has(s, ns0, m1, Grouper.PRIV_STEM)
    );
    Assert.assertFalse( 
      "m1 !CREATE on ns0",
      s.naming().has(s, ns0, m1, Grouper.PRIV_CREATE)
    );

    Assert.assertTrue(
      "ns0 has == 0 privs on ns1", 
      s.naming().has(s, ns1, ns0.toMember()).size() == 0
    );
    Assert.assertFalse( 
      "ns0 !STEM on ns1",
      s.naming().has(s, ns1, ns0.toMember(), Grouper.PRIV_STEM)
    );
    Assert.assertFalse( 
      "ns0 !CREATE on ns1",
      s.naming().has(s, ns1, ns0.toMember(), Grouper.PRIV_CREATE)
    );

    Assert.assertTrue(
      "ns1 has == 0 privs on ns1", 
      s.naming().has(s, ns1, ns1.toMember()).size() == 0
    );
    Assert.assertFalse( 
      "ns1 !STEM on ns1",
      s.naming().has(s, ns1, ns1.toMember(), Grouper.PRIV_STEM)
    );
    Assert.assertFalse( 
      "ns1 !CREATE on ns1",
      s.naming().has(s, ns1, ns1.toMember(), Grouper.PRIV_CREATE)
    );

    Assert.assertTrue(
      "root has == 2 privs on ns1", 
      s.naming().has(s, ns1).size() == 2
    );
    Assert.assertTrue(
      "root STEM on ns1",
      s.naming().has(s, ns1, Grouper.PRIV_STEM)
    );
    Assert.assertTrue(
      "root CREATE on ns1",
      s.naming().has(s, ns1, Grouper.PRIV_CREATE)
    );

    Assert.assertTrue(
      "m0 has == 1 privs on ns1", 
      s.naming().has(s, ns1, m0).size() == 1
    );
    Assert.assertTrue(
      "m0 STEM on ns1", 
      s.naming().has(s, ns1, m0, Grouper.PRIV_STEM)
    );
    Assert.assertFalse(
      "m0 !CREATE on ns1", 
      s.naming().has(s, ns1, m0, Grouper.PRIV_CREATE)
    );

    Assert.assertTrue(
      "m1 has == 0 privs on ns1", 
      s.naming().has(s, ns1, m1).size() == 0
    );
    Assert.assertFalse(
      "m1 !STEM on ns1", 
      s.naming().has(s, ns1, m1, Grouper.PRIV_STEM)
    );
    Assert.assertFalse( 
      "m1 !CREATE on ns1",
      s.naming().has(s, ns1, m1, Grouper.PRIV_CREATE)
    );

    s.stop();
  }

}

