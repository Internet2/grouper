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


public class TestNamingPrivs extends TestCase {

  public TestNamingPrivs(String name) {
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
  

  public void testHas0() {
    Subject subj = GrouperSubject.load(Constants.rootI, Constants.rootT);
    GrouperSession s = GrouperSession.start(subj);

    // Create ns0
    GrouperGroup ns0 = GrouperGroup.create(
                         s, Constants.ns0s, Constants.ns0e, Grouper.NS_TYPE
                       );

    // Assert current privs
    List privs = s.naming().has(s, ns0);
    Assert.assertTrue("privs == 1", privs.size() == 1);
    // Because we are connected as root, everything will return true
    Assert.assertTrue(
      "has CREATE",  s.naming().has(s, ns0, Grouper.PRIV_CREATE) 
    );
    Assert.assertTrue(
      "has STEM", s.naming().has(s, ns0, Grouper.PRIV_STEM) 
    );

    // We're done
    s.stop();
  }

  public void testHas1() {
    Subject subj = GrouperSubject.load(Constants.rootI, Constants.rootT);
    GrouperSession s = GrouperSession.start(subj);

    // Create ns0
    GrouperGroup ns0 = GrouperGroup.create(
                         s, Constants.ns0s, Constants.ns0e, Grouper.NS_TYPE
                       );
    // Create g0
    GrouperGroup g0  = GrouperGroup.create(
                         s, Constants.g0s, Constants.g0e
                       );

    // Assert current privs
    List privs = s.naming().has(s, g0);
    Assert.assertTrue("privs == 0", privs.size() == 0);
    // Because we are connected as root, everything will return true
    Assert.assertTrue(
      "has CREATE",  s.naming().has(s, g0, Grouper.PRIV_CREATE) 
    );
    Assert.assertTrue(
      "has STEM", s.naming().has(s, g0, Grouper.PRIV_STEM) 
    );

    // We're done
    s.stop();
  }

/*
  public void testHas1() {
    Subject         subj  = GrouperSubject.load(Constants.rootI, Constants.rootT);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // Fetch ns
    GrouperGroup ns = GrouperGroup.load(s, stem00, extn00, Grouper.NS_TYPE);
    // Assert what privs the current subject has on the ns
    List privs = s.naming().has(s, ns);
    Assert.assertNotNull(privs);
    Assert.assertTrue( privs.size() == 1 );
    Assert.assertTrue( s.naming().has(s, ns, Grouper.PRIV_CREATE) );
    Assert.assertTrue( s.naming().has(s, ns, Grouper.PRIV_STEM) );
    // We're done
    s.stop();
  }

  public void testHas2() {
    Subject         subj  = GrouperSubject.load(Constants.rootI, Constants.rootT);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // Fetch ns
    GrouperGroup ns = GrouperGroup.load(s, stem1, extn1, Grouper.NS_TYPE);
    // Assert what privs the current subject has on the ns
    List privs = s.naming().has(s, ns);
    Assert.assertNotNull(privs);
    Assert.assertTrue( privs.size() == 1 );
    Assert.assertTrue( s.naming().has(s, ns, Grouper.PRIV_CREATE) );
    Assert.assertTrue( s.naming().has(s, ns, Grouper.PRIV_STEM) );
    // We're done
    s.stop();
  }

  public void testHas3() {
    Subject         subj  = GrouperSubject.load(Constants.rootI, Constants.rootT);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // Fetch ns
    GrouperGroup ns = GrouperGroup.load(s, Constants.ns2s, Constants.ns2e, Grouper.NS_TYPE);
    // Assert what privs the current subject has on the ns
    List privs = s.naming().has(s, ns);
    Assert.assertNotNull(privs);
    Assert.assertTrue( privs.size() == 1 );
    Assert.assertTrue( s.naming().has(s, ns, Grouper.PRIV_CREATE) );
    Assert.assertTrue( s.naming().has(s, ns, Grouper.PRIV_STEM) );
    // We're done
    s.stop();
  }

  public void testHas4() {
    Subject         subj  = GrouperSubject.load(Constants.rootI, Constants.rootT);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // Fetch ns
    GrouperGroup ns = GrouperGroup.load(s, stem0, extn0, Grouper.NS_TYPE);
    // Fetch m
    GrouperMember m = GrouperMember.load(s, Constants.mem0I, Constants.mem0T);
    // Assert what privs m has on the ns
    List privs = s.naming().has(s, ns, m);
    Assert.assertNotNull(privs);
    Assert.assertTrue( privs.size() == 0 );
    Assert.assertFalse( s.naming().has(s, ns, m, Grouper.PRIV_CREATE) );
    Assert.assertFalse( s.naming().has(s, ns, m, Grouper.PRIV_STEM) );
    // We're done
    s.stop();
  }

  public void testHas5() {
    Subject         subj  = GrouperSubject.load(Constants.rootI, Constants.rootT);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // Fetch ns
    GrouperGroup ns = GrouperGroup.load(s, stem00, extn00, Grouper.NS_TYPE);
    // Fetch m
    GrouperMember m = GrouperMember.load(s, Constants.mem0I, Constants.mem0T);
    // Assert what privs m has on the ns
    List privs = s.naming().has(s, ns, m);
    Assert.assertNotNull(privs);
    Assert.assertTrue( privs.size() == 0 );
    Assert.assertFalse( s.naming().has(s, ns, m, Grouper.PRIV_CREATE) );
    Assert.assertFalse( s.naming().has(s, ns, m, Grouper.PRIV_STEM) );
    // We're done
    s.stop();
  }

  public void testHas6() {
    Subject         subj  = GrouperSubject.load(Constants.rootI, Constants.rootT);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // Fetch ns
    GrouperGroup ns = GrouperGroup.load(s, stem1, extn1, Grouper.NS_TYPE);
    // Fetch m
    GrouperMember m = GrouperMember.load(s, Constants.mem0I, Constants.mem0T);
    // Assert what privs m has on the ns
    List privs = s.naming().has(s, ns, m);
    Assert.assertNotNull(privs);
    Assert.assertTrue( privs.size() == 0 );
    Assert.assertFalse( s.naming().has(s, ns, m, Grouper.PRIV_CREATE) );
    Assert.assertFalse( s.naming().has(s, ns, m, Grouper.PRIV_STEM) );
    // We're done
    s.stop();
  }

  public void testHas7() {
    Subject         subj  = GrouperSubject.load(Constants.rootI, Constants.rootT);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // Fetch ns
    GrouperGroup ns = GrouperGroup.load(s, Constants.ns2s, Constants.ns2e, Grouper.NS_TYPE);
    // Fetch m
    GrouperMember m = GrouperMember.load(s, Constants.mem0I, Constants.mem0T);
    // Assert what privs m has on the ns
    List privs = s.naming().has(s, ns, m);
    Assert.assertNotNull(privs);
    Assert.assertTrue( privs.size() == 0 );
    Assert.assertFalse( s.naming().has(s, ns, m, Grouper.PRIV_CREATE) );
    Assert.assertFalse( s.naming().has(s, ns, m, Grouper.PRIV_STEM) );
    // We're done
    s.stop();
  }

  public void testGrant0() {
    Subject subj  = GrouperSubject.load(Constants.rootI, Constants.rootT);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // Fetch ns
    GrouperGroup g = GrouperGroup.load(s, Constants.ns2s, Constants.ns2e, Grouper.NS_TYPE);
    // Fetch m
    GrouperMember m = GrouperMember.load(s, Constants.mem0I, Constants.mem0T);
    // Grant STEM to m
    Assert.assertTrue( s.naming().grant(s, g, m, Grouper.PRIV_STEM) );
    // We're done
    s.stop();
  }

  public void testHas8() {
    Subject subj  = GrouperSubject.load(Constants.rootI, Constants.rootT);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // Fetch ns
    GrouperGroup g = GrouperGroup.load(s, Constants.ns2s, Constants.ns2e, Grouper.NS_TYPE);
    // Fetch m
    GrouperMember m = GrouperMember.load(s, Constants.mem0I, Constants.mem0T);
    // Assert what privs m has on the ns
    List privs = s.naming().has(s, g, m);
    Assert.assertNotNull(privs);
    Assert.assertTrue( privs.size() == 1 );
    Assert.assertFalse( s.naming().has(s, g, m, Grouper.PRIV_CREATE) );
    Assert.assertTrue( s.naming().has(s, g, m, Grouper.PRIV_STEM) );
    // We're done
    s.stop();
  }

  public void testGrant1() {
    Subject subj  = GrouperSubject.load(Constants.mem0I, Constants.mem0T);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // Fetch ns
    GrouperGroup g = GrouperGroup.load(s, Constants.ns2s, Constants.ns2e, Grouper.NS_TYPE);
    // Fetch m
    GrouperMember m = GrouperMember.load(s, Constants.mem0I, Constants.mem0T);
    // Grant CREATE to m
    Assert.assertTrue( s.naming().grant(s, g, m, Grouper.PRIV_CREATE) );
    // We're done
    s.stop();
  }

  public void testHas9() {
    Subject subj  = GrouperSubject.load(Constants.rootI, Constants.rootT);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // Fetch ns
    GrouperGroup g = GrouperGroup.load(s, Constants.ns2s, Constants.ns2e, Grouper.NS_TYPE);
    // Fetch m
    GrouperMember m = GrouperMember.load(s, Constants.mem0I, Constants.mem0T);
    // Assert what privs m has on the ns
    List privs = s.naming().has(s, g, m);
    Assert.assertNotNull(privs);
    Assert.assertTrue( privs.size() == 2 );
    Assert.assertTrue( s.naming().has(s, g, m, Grouper.PRIV_CREATE) );
    Assert.assertTrue( s.naming().has(s, g, m, Grouper.PRIV_STEM) );
    // We're done
    s.stop();
  }

  public void testGrant2() {
    Subject subj  = GrouperSubject.load(Constants.mem0I, Constants.mem0T);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // Fetch ns
    GrouperGroup g = GrouperGroup.load(s, Constants.ns2s, Constants.ns2e, Grouper.NS_TYPE);
    // Fetch m
    GrouperMember m = GrouperMember.load(s, Constants.mem1I, Constants.mem1T);
    // Grant CREATE to m
    Assert.assertTrue( s.naming().grant(s, g, m, Grouper.PRIV_CREATE) );
    // We're done
    s.stop();
  }

  public void testHas10() {
    Subject subj  = GrouperSubject.load(Constants.rootI, Constants.rootT);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // Fetch ns
    GrouperGroup g = GrouperGroup.load(s, Constants.ns2s, Constants.ns2e, Grouper.NS_TYPE);
    // Fetch m
    GrouperMember m = GrouperMember.load(s, Constants.mem1I, Constants.mem1T);
    // Assert what privs m has on the ns
    List privs = s.naming().has(s, g, m);
    Assert.assertNotNull(privs);
    Assert.assertTrue( privs.size() == 1 );
    Assert.assertTrue( s.naming().has(s, g, m, Grouper.PRIV_CREATE) );
    Assert.assertFalse( s.naming().has(s, g, m, Grouper.PRIV_STEM) );
    // We're done
    s.stop();
  }

  public void testGrant3() {
    Subject subj  = GrouperSubject.load(Constants.mem1I, Constants.mem1T);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // Fetch ns
    GrouperGroup g = GrouperGroup.load(s, Constants.ns2s, Constants.ns2e, Grouper.NS_TYPE);
    // Fetch m
    GrouperMember m = GrouperMember.load(s, Constants.mem1I, Constants.mem1T);
    // Fail to grant STEM to self
    Assert.assertFalse( s.naming().grant(s, g, m, Grouper.PRIV_STEM) );
    // We're done
    s.stop();
  }

  public void testHas11() {
    Subject subj  = GrouperSubject.load(Constants.rootI, Constants.rootT);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // Fetch ns
    GrouperGroup g = GrouperGroup.load(s, Constants.ns2s, Constants.ns2e, Grouper.NS_TYPE);
    // Fetch m
    GrouperMember m = GrouperMember.load(s, Constants.mem1I, Constants.mem1T);
    // Assert what privs m has on the ns
    List privs = s.naming().has(s, g, m);
    Assert.assertNotNull(privs);
    Assert.assertTrue( privs.size() == 1 );
    Assert.assertTrue( s.naming().has(s, g, m, Grouper.PRIV_CREATE) );
    Assert.assertFalse( s.naming().has(s, g, m, Grouper.PRIV_STEM) );
    // We're done
    s.stop();
  }

  public void testRevoke0() {
    Subject subj  = GrouperSubject.load(Constants.mem1I, Constants.mem1T);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // Fetch ns
    GrouperGroup g = GrouperGroup.load(s, Constants.ns2s, Constants.ns2e, Grouper.NS_TYPE);
    // Fetch m
    GrouperMember m = GrouperMember.load(s, Constants.mem1I, Constants.mem1T);
    // Fail to revoke CREATE from self
    Assert.assertFalse( s.naming().revoke(s, g, m, Grouper.PRIV_CREATE) );
    // We're done
    s.stop();
  }

  public void testHas12() {
    Subject subj  = GrouperSubject.load(Constants.rootI, Constants.rootT);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // Fetch ns
    GrouperGroup g = GrouperGroup.load(s, Constants.ns2s, Constants.ns2e, Grouper.NS_TYPE);
    // Fetch m
    GrouperMember m = GrouperMember.load(s, Constants.mem1I, Constants.mem1T);
    // Assert what privs m has on the ns
    List privs = s.naming().has(s, g, m);
    Assert.assertNotNull(privs);
    Assert.assertTrue( privs.size() == 1 );
    Assert.assertTrue( s.naming().has(s, g, m, Grouper.PRIV_CREATE) );
    Assert.assertFalse( s.naming().has(s, g, m, Grouper.PRIV_STEM) );
    // We're done
    s.stop();
  }

  public void testRevoke1() {
    Subject subj  = GrouperSubject.load(Constants.mem1I, Constants.mem1T);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // Fetch ns
    GrouperGroup g = GrouperGroup.load(s, Constants.ns2s, Constants.ns2e, Grouper.NS_TYPE);
    // Fetch m
    GrouperMember m = GrouperMember.load(s, Constants.mem0I, Constants.mem0T);
    // Fail to revoke CREATE from m0
    Assert.assertFalse( s.naming().revoke(s, g, m, Grouper.PRIV_CREATE) );
    // We're done
    s.stop();
  }

  public void testHas13() {
    Subject subj  = GrouperSubject.load(Constants.rootI, Constants.rootT);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // Fetch ns
    GrouperGroup g = GrouperGroup.load(s, Constants.ns2s, Constants.ns2e, Grouper.NS_TYPE);
    // Fetch m
    GrouperMember m = GrouperMember.load(s, Constants.mem0I, Constants.mem0T);
    // Assert what privs m has on the ns
    List privs = s.naming().has(s, g, m);
    Assert.assertNotNull(privs);
    Assert.assertTrue( privs.size() == 2 );
    Assert.assertTrue( s.naming().has(s, g, m, Grouper.PRIV_CREATE) );
    Assert.assertTrue( s.naming().has(s, g, m, Grouper.PRIV_STEM) );
    // We're done
    s.stop();
  }

  public void testRevoke2() {
    Subject subj  = GrouperSubject.load(Constants.mem1I, Constants.mem1T);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // Fetch ns
    GrouperGroup g = GrouperGroup.load(s, Constants.ns2s, Constants.ns2e, Grouper.NS_TYPE);
    // Fetch m
    GrouperMember m = GrouperMember.load(s, Constants.mem0I, Constants.mem0T);
    // m1 !revoke m0, STEM
    Assert.assertFalse( s.naming().revoke(s, g, m, Grouper.PRIV_STEM) );
    // We're done
    s.stop();
  }

  public void testHas14() {
    Subject subj  = GrouperSubject.load(Constants.rootI, Constants.rootT);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // Fetch ns
    GrouperGroup g = GrouperGroup.load(s, Constants.ns2s, Constants.ns2e, Grouper.NS_TYPE);
    // Fetch m
    GrouperMember m = GrouperMember.load(s, Constants.mem0I, Constants.mem0T);
    // Assert what privs m has on the ns
    List privs = s.naming().has(s, g, m);
    Assert.assertNotNull(privs);
    Assert.assertTrue( privs.size() == 2 );
    Assert.assertTrue( s.naming().has(s, g, m, Grouper.PRIV_CREATE) );
    Assert.assertTrue( s.naming().has(s, g, m, Grouper.PRIV_STEM) );
    // We're done
    s.stop();
  }


  public void testRevoke3() {
    Subject subj  = GrouperSubject.load(Constants.mem0I, Constants.mem0T);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // Fetch ns
    GrouperGroup g = GrouperGroup.load(s, Constants.ns2s, Constants.ns2e, Grouper.NS_TYPE);
    // Fetch m
    GrouperMember m = GrouperMember.load(s, Constants.mem1I, Constants.mem1T);
    // m0 revoke m1, CREATE
    Assert.assertTrue( s.naming().revoke(s, g, m, Grouper.PRIV_CREATE) );
    // We're done
    s.stop();
  }

  public void testHas15() {
    Subject subj  = GrouperSubject.load(Constants.rootI, Constants.rootT);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // Fetch ns
    GrouperGroup g = GrouperGroup.load(s, Constants.ns2s, Constants.ns2e, Grouper.NS_TYPE);
    // Fetch m
    GrouperMember m = GrouperMember.load(s, Constants.mem1I, Constants.mem1T);
    // Assert what privs m has on the ns
    List privs = s.naming().has(s, g, m);
    Assert.assertNotNull(privs);
    Assert.assertTrue( privs.size() == 0 );
    Assert.assertFalse( s.naming().has(s, g, m, Grouper.PRIV_CREATE) );
    Assert.assertFalse( s.naming().has(s, g, m, Grouper.PRIV_STEM) );
    // We're done
    s.stop();
  }


  public void testRevoke4() {
    Subject subj  = GrouperSubject.load(Constants.rootI, Constants.rootT);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // Fetch ns
    GrouperGroup g = GrouperGroup.load(s, Constants.ns2s, Constants.ns2e, Grouper.NS_TYPE);
    // Fetch m
    GrouperMember m = GrouperMember.load(s, Constants.mem0I, Constants.mem0T);
    // root revoke m0, CREATE
    Assert.assertTrue( s.naming().revoke(s, g, m, Grouper.PRIV_CREATE) );
    // We're done
    s.stop();
  }

  public void testHas16() {
    Subject subj  = GrouperSubject.load(Constants.rootI, Constants.rootT);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // Fetch ns
    GrouperGroup g = GrouperGroup.load(s, Constants.ns2s, Constants.ns2e, Grouper.NS_TYPE);
    // Fetch m
    GrouperMember m = GrouperMember.load(s, Constants.mem0I, Constants.mem0T);
    // Assert what privs m has on the ns
    List privs = s.naming().has(s, g, m);
    Assert.assertNotNull(privs);
    Assert.assertTrue( privs.size() == 1 );
    Assert.assertFalse( s.naming().has(s, g, m, Grouper.PRIV_CREATE) );
    Assert.assertTrue( s.naming().has(s, g, m, Grouper.PRIV_STEM) );
    // We're done
    s.stop();
  }


  public void testRevoke5() {
    Subject subj  = GrouperSubject.load(Constants.rootI, Constants.rootT);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // Fetch ns
    GrouperGroup g = GrouperGroup.load(s, Constants.ns2s, Constants.ns2e, Grouper.NS_TYPE);
    // Fetch m
    GrouperMember m = GrouperMember.load(s, Constants.mem0I, Constants.mem0T);
    // root revoke m0, STEM
    Assert.assertTrue( s.naming().revoke(s, g, m, Grouper.PRIV_STEM) );
    // We're done
    s.stop();
  }

  public void testHas17() {
    Subject subj  = GrouperSubject.load(Constants.rootI, Constants.rootT);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // Fetch ns
    GrouperGroup g = GrouperGroup.load(s, Constants.ns2s, Constants.ns2e, Grouper.NS_TYPE);
    // Fetch m
    GrouperMember m = GrouperMember.load(s, Constants.mem0I, Constants.mem0T);
    // Assert what privs m has on the ns
    List privs = s.naming().has(s, g, m);
    Assert.assertNotNull(privs);
    Assert.assertTrue( privs.size() == 0 );
    Assert.assertFalse( s.naming().has(s, g, m, Grouper.PRIV_CREATE) );
    Assert.assertFalse( s.naming().has(s, g, m, Grouper.PRIV_STEM) );
    // We're done
    s.stop();
  }

  // root grant CREATE on ns0 to m0
  public void testGrant4() {
    Subject subj  = GrouperSubject.load(Constants.rootI, Constants.rootT);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);
    GrouperGroup g = GrouperGroup.load(s, Constants.ns0s, Constants.ns0e, Grouper.NS_TYPE);
    GrouperMember m = GrouperMember.load(s, Constants.mem0I, Constants.mem0T);
    Assert.assertTrue( s.naming().grant(s, g, m, Grouper.PRIV_CREATE) );
    s.stop();
  }

  // m0 create base group within ns0
  public void testCreate0() {
    Subject subj  = GrouperSubject.load(Constants.mem0I, Constants.mem0T);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);
    GrouperGroup g = GrouperGroup.create(s, Constants.stem4, Constants.extn4);
    Assert.assertNotNull(g);
    s.stop();
  }

  // m0 delete base group within ns0
  public void testDelete0() {
    Subject subj  = GrouperSubject.load(Constants.mem0I, Constants.mem0T);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);
    GrouperGroup g = GrouperGroup.load(s, Constants.stem4, Constants.extn4);
    Assert.assertNotNull(g);
    Assert.assertTrue( GrouperGroup.delete(s, g) );
    s.stop();
  }

  // m0 !create naming group within ns0
  public void testCreate1() {
    Subject subj  = GrouperSubject.load(Constants.mem0I, Constants.mem0T);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);
    GrouperGroup g = GrouperGroup.create(s, Constants.stem4, Constants.extn4, Grouper.NS_TYPE);
    Assert.assertNull(g);
    s.stop();
  }

  // root revoke CREATE on ns0 from m0
  public void testRevoke6() {
    Subject subj  = GrouperSubject.load(Constants.rootI, Constants.rootT);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);
    GrouperGroup g = GrouperGroup.load(s, Constants.ns0s, Constants.ns0e, Grouper.NS_TYPE);
    GrouperMember m = GrouperMember.load(s, Constants.mem0I, Constants.mem0T);
    Assert.assertTrue( s.naming().revoke(s, g, m, Grouper.PRIV_CREATE) );
    s.stop();
  }
*/

}

