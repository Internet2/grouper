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

  private String  klass   = "edu.internet2.middleware.grouper.GrouperGroup";

  private String  stem0   = Grouper.NS_ROOT;
  private String  extn0   = "stem.0";
  private String  stem00  = "stem.0";
  private String  extn00  = "stem.0.0";
  private String  stem1   = Grouper.NS_ROOT;
  private String  extn1   = "stem.1";
 
public TestNamingPrivs(String name) {
    super(name);
  }

  protected void setUp () {
    // Nothing -- Yet
  }

  protected void tearDown () {
    // Nothing -- Yet
  }


  /*
   * TESTS
   */
  

  // Test requirements for other *real* tests
  public void testRequirements() {
    Subject         subj  = GrouperSubject.load( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    Assert.assertNotNull(subj);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // Fetch the namespaces
    // ns0
    GrouperGroup ns0 = GrouperGroup.load(s, stem0, extn0, Grouper.NS_TYPE);
    Assert.assertNotNull(ns0);
    Assert.assertTrue( klass.equals( ns0.getClass().getName() ) );
    Assert.assertNotNull( ns0.type() );
    Assert.assertTrue( ns0.type().equals(Grouper.NS_TYPE) );
    Assert.assertNotNull( ns0.attribute("stem") );
    Assert.assertTrue( ns0.attribute("stem").value().equals(stem0) );
    Assert.assertNotNull( ns0.attribute("extension") );
    Assert.assertTrue( ns0.attribute("extension").value().equals(extn0) );
    // ns00
    GrouperGroup ns00 = GrouperGroup.load(s, stem00, extn00, Grouper.NS_TYPE);
    Assert.assertNotNull(ns00);
    Assert.assertTrue( klass.equals( ns00.getClass().getName() ) );
    Assert.assertNotNull( ns00.type() );
    Assert.assertTrue( ns00.type().equals(Grouper.NS_TYPE) );
    Assert.assertNotNull( ns00.attribute("stem") );
    Assert.assertTrue( ns00.attribute("stem").value().equals(stem00) );
    Assert.assertNotNull( ns00.attribute("extension") );
    Assert.assertTrue( ns00.attribute("extension").value().equals(extn00) );
    // ns1
    GrouperGroup ns1 = GrouperGroup.load(s, stem1, extn1, Grouper.NS_TYPE);
    Assert.assertNotNull(ns1);
    Assert.assertTrue( klass.equals( ns1.getClass().getName() ) );
    Assert.assertNotNull( ns1.type() );
    Assert.assertTrue( ns1.type().equals(Grouper.NS_TYPE) );
    Assert.assertNotNull( ns1.attribute("stem") );
    Assert.assertTrue( ns1.attribute("stem").value().equals(stem1) );
    Assert.assertNotNull( ns1.attribute("extension") );
    Assert.assertTrue( ns1.attribute("extension").value().equals(extn1) );
    // ns2
    GrouperGroup ns2 = GrouperGroup.load(s, Util.ns2s, Util.ns2e, Grouper.NS_TYPE);
    Assert.assertNotNull(ns2);
    Assert.assertTrue( klass.equals( ns2.getClass().getName() ) );
    Assert.assertNotNull( ns2.type() );
    Assert.assertTrue( ns2.type().equals(Grouper.NS_TYPE) );
    Assert.assertNotNull( ns2.attribute("stem") );
    Assert.assertTrue( ns2.attribute("stem").value().equals(Util.ns2s) );
    Assert.assertNotNull( ns2.attribute("extension") );
    Assert.assertTrue( ns2.attribute("extension").value().equals(Util.ns2e) );
    // Fetch the members
    // Fetch m0
    GrouperMember m0 = GrouperMember.load(s, Util.m0i, Util.m0t);
    Assert.assertNotNull(m0);
    // Fetch m1
    GrouperMember m1 = GrouperMember.load(s, Util.m1i, Util.m1t);
    Assert.assertNotNull(m1);

    // We're done
    s.stop();
  }

  public void testHas0() {
    Subject         subj  = GrouperSubject.load( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // Fetch ns
    GrouperGroup ns = GrouperGroup.load(s, stem0, extn0, Grouper.NS_TYPE);
    // Assert what privs the current subject has on the ns
    List privs = Grouper.naming().has(s, ns);
    Assert.assertNotNull(privs);
    Assert.assertTrue( privs.size() == 1 );
    Assert.assertTrue( Grouper.naming().has(s, ns, Grouper.PRIV_CREATE) );
    Assert.assertTrue( Grouper.naming().has(s, ns, Grouper.PRIV_STEM) );
    // We're done
    s.stop();
  }

  public void testHas1() {
    Subject         subj  = GrouperSubject.load( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // Fetch ns
    GrouperGroup ns = GrouperGroup.load(s, stem00, extn00, Grouper.NS_TYPE);
    // Assert what privs the current subject has on the ns
    List privs = Grouper.naming().has(s, ns);
    Assert.assertNotNull(privs);
    Assert.assertTrue( privs.size() == 1 );
    Assert.assertTrue( Grouper.naming().has(s, ns, Grouper.PRIV_CREATE) );
    Assert.assertTrue( Grouper.naming().has(s, ns, Grouper.PRIV_STEM) );
    // We're done
    s.stop();
  }

  public void testHas2() {
    Subject         subj  = GrouperSubject.load( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // Fetch ns
    GrouperGroup ns = GrouperGroup.load(s, stem1, extn1, Grouper.NS_TYPE);
    // Assert what privs the current subject has on the ns
    List privs = Grouper.naming().has(s, ns);
    Assert.assertNotNull(privs);
    Assert.assertTrue( privs.size() == 1 );
    Assert.assertTrue( Grouper.naming().has(s, ns, Grouper.PRIV_CREATE) );
    Assert.assertTrue( Grouper.naming().has(s, ns, Grouper.PRIV_STEM) );
    // We're done
    s.stop();
  }

  public void testHas3() {
    Subject         subj  = GrouperSubject.load( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // Fetch ns
    GrouperGroup ns = GrouperGroup.load(s, Util.ns2s, Util.ns2e, Grouper.NS_TYPE);
    // Assert what privs the current subject has on the ns
    List privs = Grouper.naming().has(s, ns);
    Assert.assertNotNull(privs);
    Assert.assertTrue( privs.size() == 1 );
    Assert.assertTrue( Grouper.naming().has(s, ns, Grouper.PRIV_CREATE) );
    Assert.assertTrue( Grouper.naming().has(s, ns, Grouper.PRIV_STEM) );
    // We're done
    s.stop();
  }

  public void testHas4() {
    Subject         subj  = GrouperSubject.load( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // Fetch ns
    GrouperGroup ns = GrouperGroup.load(s, stem0, extn0, Grouper.NS_TYPE);
    // Fetch m
    GrouperMember m = GrouperMember.load(s, Util.m0i, Util.m0t);
    // Assert what privs m has on the ns
    List privs = Grouper.naming().has(s, ns, m);
    Assert.assertNotNull(privs);
    Assert.assertTrue( privs.size() == 0 );
    Assert.assertFalse( Grouper.naming().has(s, ns, m, Grouper.PRIV_CREATE) );
    Assert.assertFalse( Grouper.naming().has(s, ns, m, Grouper.PRIV_STEM) );
    // We're done
    s.stop();
  }

  public void testHas5() {
    Subject         subj  = GrouperSubject.load( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // Fetch ns
    GrouperGroup ns = GrouperGroup.load(s, stem00, extn00, Grouper.NS_TYPE);
    // Fetch m
    GrouperMember m = GrouperMember.load(s, Util.m0i, Util.m0t);
    // Assert what privs m has on the ns
    List privs = Grouper.naming().has(s, ns, m);
    Assert.assertNotNull(privs);
    Assert.assertTrue( privs.size() == 0 );
    Assert.assertFalse( Grouper.naming().has(s, ns, m, Grouper.PRIV_CREATE) );
    Assert.assertFalse( Grouper.naming().has(s, ns, m, Grouper.PRIV_STEM) );
    // We're done
    s.stop();
  }

  public void testHas6() {
    Subject         subj  = GrouperSubject.load( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // Fetch ns
    GrouperGroup ns = GrouperGroup.load(s, stem1, extn1, Grouper.NS_TYPE);
    // Fetch m
    GrouperMember m = GrouperMember.load(s, Util.m0i, Util.m0t);
    // Assert what privs m has on the ns
    List privs = Grouper.naming().has(s, ns, m);
    Assert.assertNotNull(privs);
    Assert.assertTrue( privs.size() == 0 );
    Assert.assertFalse( Grouper.naming().has(s, ns, m, Grouper.PRIV_CREATE) );
    Assert.assertFalse( Grouper.naming().has(s, ns, m, Grouper.PRIV_STEM) );
    // We're done
    s.stop();
  }

  public void testHas7() {
    Subject         subj  = GrouperSubject.load( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // Fetch ns
    GrouperGroup ns = GrouperGroup.load(s, Util.ns2s, Util.ns2e, Grouper.NS_TYPE);
    // Fetch m
    GrouperMember m = GrouperMember.load(s, Util.m0i, Util.m0t);
    // Assert what privs m has on the ns
    List privs = Grouper.naming().has(s, ns, m);
    Assert.assertNotNull(privs);
    Assert.assertTrue( privs.size() == 0 );
    Assert.assertFalse( Grouper.naming().has(s, ns, m, Grouper.PRIV_CREATE) );
    Assert.assertFalse( Grouper.naming().has(s, ns, m, Grouper.PRIV_STEM) );
    // We're done
    s.stop();
  }

  public void testGrant0() {
    Subject subj  = GrouperSubject.load(Util.rooti, Util.roott);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // Fetch ns
    GrouperGroup g = GrouperGroup.load(s, Util.ns2s, Util.ns2e, Grouper.NS_TYPE);
    // Fetch m
    GrouperMember m = GrouperMember.load(s, Util.m0i, Util.m0t);
    // Grant STEM to m
    Assert.assertTrue( Grouper.naming().grant(s, g, m, Grouper.PRIV_STEM) );
    // We're done
    s.stop();
  }

  public void testHas8() {
    Subject subj  = GrouperSubject.load(Util.rooti, Util.roott);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // Fetch ns
    GrouperGroup g = GrouperGroup.load(s, Util.ns2s, Util.ns2e, Grouper.NS_TYPE);
    // Fetch m
    GrouperMember m = GrouperMember.load(s, Util.m0i, Util.m0t);
    // Assert what privs m has on the ns
    List privs = Grouper.naming().has(s, g, m);
    Assert.assertNotNull(privs);
    Assert.assertTrue( privs.size() == 1 );
    Assert.assertFalse( Grouper.naming().has(s, g, m, Grouper.PRIV_CREATE) );
    Assert.assertTrue( Grouper.naming().has(s, g, m, Grouper.PRIV_STEM) );
    // We're done
    s.stop();
  }

  public void testGrant1() {
    Subject subj  = GrouperSubject.load(Util.m0i, Util.m0t);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // Fetch ns
    GrouperGroup g = GrouperGroup.load(s, Util.ns2s, Util.ns2e, Grouper.NS_TYPE);
    // Fetch m
    GrouperMember m = GrouperMember.load(s, Util.m0i, Util.m0t);
    // Grant CREATE to m
    Assert.assertTrue( Grouper.naming().grant(s, g, m, Grouper.PRIV_CREATE) );
    // We're done
    s.stop();
  }

  public void testHas9() {
    Subject subj  = GrouperSubject.load(Util.rooti, Util.roott);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // Fetch ns
    GrouperGroup g = GrouperGroup.load(s, Util.ns2s, Util.ns2e, Grouper.NS_TYPE);
    // Fetch m
    GrouperMember m = GrouperMember.load(s, Util.m0i, Util.m0t);
    // Assert what privs m has on the ns
    List privs = Grouper.naming().has(s, g, m);
    Assert.assertNotNull(privs);
    Assert.assertTrue( privs.size() == 2 );
    Assert.assertTrue( Grouper.naming().has(s, g, m, Grouper.PRIV_CREATE) );
    Assert.assertTrue( Grouper.naming().has(s, g, m, Grouper.PRIV_STEM) );
    // We're done
    s.stop();
  }

  public void testGrant2() {
    Subject subj  = GrouperSubject.load(Util.m0i, Util.m0t);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // Fetch ns
    GrouperGroup g = GrouperGroup.load(s, Util.ns2s, Util.ns2e, Grouper.NS_TYPE);
    // Fetch m
    GrouperMember m = GrouperMember.load(s, Util.m1i, Util.m1t);
    // Grant CREATE to m
    Assert.assertTrue( Grouper.naming().grant(s, g, m, Grouper.PRIV_CREATE) );
    // We're done
    s.stop();
  }

  public void testHas10() {
    Subject subj  = GrouperSubject.load(Util.rooti, Util.roott);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // Fetch ns
    GrouperGroup g = GrouperGroup.load(s, Util.ns2s, Util.ns2e, Grouper.NS_TYPE);
    // Fetch m
    GrouperMember m = GrouperMember.load(s, Util.m1i, Util.m1t);
    // Assert what privs m has on the ns
    List privs = Grouper.naming().has(s, g, m);
    Assert.assertNotNull(privs);
    Assert.assertTrue( privs.size() == 1 );
    Assert.assertTrue( Grouper.naming().has(s, g, m, Grouper.PRIV_CREATE) );
    Assert.assertFalse( Grouper.naming().has(s, g, m, Grouper.PRIV_STEM) );
    // We're done
    s.stop();
  }

  public void testGrant3() {
    Subject subj  = GrouperSubject.load(Util.m1i, Util.m1t);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // Fetch ns
    GrouperGroup g = GrouperGroup.load(s, Util.ns2s, Util.ns2e, Grouper.NS_TYPE);
    // Fetch m
    GrouperMember m = GrouperMember.load(s, Util.m1i, Util.m1t);
    // Fail to grant STEM to self
    Assert.assertFalse( Grouper.naming().grant(s, g, m, Grouper.PRIV_STEM) );
    // We're done
    s.stop();
  }

  public void testHas11() {
    Subject subj  = GrouperSubject.load(Util.rooti, Util.roott);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // Fetch ns
    GrouperGroup g = GrouperGroup.load(s, Util.ns2s, Util.ns2e, Grouper.NS_TYPE);
    // Fetch m
    GrouperMember m = GrouperMember.load(s, Util.m1i, Util.m1t);
    // Assert what privs m has on the ns
    List privs = Grouper.naming().has(s, g, m);
    Assert.assertNotNull(privs);
    Assert.assertTrue( privs.size() == 1 );
    Assert.assertTrue( Grouper.naming().has(s, g, m, Grouper.PRIV_CREATE) );
    Assert.assertFalse( Grouper.naming().has(s, g, m, Grouper.PRIV_STEM) );
    // We're done
    s.stop();
  }

  public void testRevoke0() {
    Subject subj  = GrouperSubject.load(Util.m1i, Util.m1t);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // Fetch ns
    GrouperGroup g = GrouperGroup.load(s, Util.ns2s, Util.ns2e, Grouper.NS_TYPE);
    // Fetch m
    GrouperMember m = GrouperMember.load(s, Util.m1i, Util.m1t);
    // Fail to revoke CREATE from self
    Assert.assertFalse( Grouper.naming().revoke(s, g, m, Grouper.PRIV_CREATE) );
    // We're done
    s.stop();
  }

  public void testHas12() {
    Subject subj  = GrouperSubject.load(Util.rooti, Util.roott);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // Fetch ns
    GrouperGroup g = GrouperGroup.load(s, Util.ns2s, Util.ns2e, Grouper.NS_TYPE);
    // Fetch m
    GrouperMember m = GrouperMember.load(s, Util.m1i, Util.m1t);
    // Assert what privs m has on the ns
    List privs = Grouper.naming().has(s, g, m);
    Assert.assertNotNull(privs);
    Assert.assertTrue( privs.size() == 1 );
    Assert.assertTrue( Grouper.naming().has(s, g, m, Grouper.PRIV_CREATE) );
    Assert.assertFalse( Grouper.naming().has(s, g, m, Grouper.PRIV_STEM) );
    // We're done
    s.stop();
  }

  public void testRevoke1() {
    Subject subj  = GrouperSubject.load(Util.m1i, Util.m1t);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // Fetch ns
    GrouperGroup g = GrouperGroup.load(s, Util.ns2s, Util.ns2e, Grouper.NS_TYPE);
    // Fetch m
    GrouperMember m = GrouperMember.load(s, Util.m0i, Util.m0t);
    // Fail to revoke CREATE from m0
    Assert.assertFalse( Grouper.naming().revoke(s, g, m, Grouper.PRIV_CREATE) );
    // We're done
    s.stop();
  }

  public void testHas13() {
    Subject subj  = GrouperSubject.load(Util.rooti, Util.roott);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // Fetch ns
    GrouperGroup g = GrouperGroup.load(s, Util.ns2s, Util.ns2e, Grouper.NS_TYPE);
    // Fetch m
    GrouperMember m = GrouperMember.load(s, Util.m0i, Util.m0t);
    // Assert what privs m has on the ns
    List privs = Grouper.naming().has(s, g, m);
    Assert.assertNotNull(privs);
    Assert.assertTrue( privs.size() == 2 );
    Assert.assertTrue( Grouper.naming().has(s, g, m, Grouper.PRIV_CREATE) );
    Assert.assertTrue( Grouper.naming().has(s, g, m, Grouper.PRIV_STEM) );
    // We're done
    s.stop();
  }

  public void testRevoke2() {
    Subject subj  = GrouperSubject.load(Util.m1i, Util.m1t);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // Fetch ns
    GrouperGroup g = GrouperGroup.load(s, Util.ns2s, Util.ns2e, Grouper.NS_TYPE);
    // Fetch m
    GrouperMember m = GrouperMember.load(s, Util.m0i, Util.m0t);
    // m1 !revoke m0, STEM
    Assert.assertFalse( Grouper.naming().revoke(s, g, m, Grouper.PRIV_STEM) );
    // We're done
    s.stop();
  }

  public void testHas14() {
    Subject subj  = GrouperSubject.load(Util.rooti, Util.roott);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // Fetch ns
    GrouperGroup g = GrouperGroup.load(s, Util.ns2s, Util.ns2e, Grouper.NS_TYPE);
    // Fetch m
    GrouperMember m = GrouperMember.load(s, Util.m0i, Util.m0t);
    // Assert what privs m has on the ns
    List privs = Grouper.naming().has(s, g, m);
    Assert.assertNotNull(privs);
    Assert.assertTrue( privs.size() == 2 );
    Assert.assertTrue( Grouper.naming().has(s, g, m, Grouper.PRIV_CREATE) );
    Assert.assertTrue( Grouper.naming().has(s, g, m, Grouper.PRIV_STEM) );
    // We're done
    s.stop();
  }


  public void testRevoke3() {
    Subject subj  = GrouperSubject.load(Util.m0i, Util.m0t);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // Fetch ns
    GrouperGroup g = GrouperGroup.load(s, Util.ns2s, Util.ns2e, Grouper.NS_TYPE);
    // Fetch m
    GrouperMember m = GrouperMember.load(s, Util.m1i, Util.m1t);
    // m0 revoke m1, CREATE
    Assert.assertTrue( Grouper.naming().revoke(s, g, m, Grouper.PRIV_CREATE) );
    // We're done
    s.stop();
  }

  public void testHas15() {
    Subject subj  = GrouperSubject.load(Util.rooti, Util.roott);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // Fetch ns
    GrouperGroup g = GrouperGroup.load(s, Util.ns2s, Util.ns2e, Grouper.NS_TYPE);
    // Fetch m
    GrouperMember m = GrouperMember.load(s, Util.m1i, Util.m1t);
    // Assert what privs m has on the ns
    List privs = Grouper.naming().has(s, g, m);
    Assert.assertNotNull(privs);
    Assert.assertTrue( privs.size() == 0 );
    Assert.assertFalse( Grouper.naming().has(s, g, m, Grouper.PRIV_CREATE) );
    Assert.assertFalse( Grouper.naming().has(s, g, m, Grouper.PRIV_STEM) );
    // We're done
    s.stop();
  }


  public void testRevoke4() {
    Subject subj  = GrouperSubject.load(Util.rooti, Util.roott);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // Fetch ns
    GrouperGroup g = GrouperGroup.load(s, Util.ns2s, Util.ns2e, Grouper.NS_TYPE);
    // Fetch m
    GrouperMember m = GrouperMember.load(s, Util.m0i, Util.m0t);
    // root revoke m0, CREATE
    Assert.assertTrue( Grouper.naming().revoke(s, g, m, Grouper.PRIV_CREATE) );
    // We're done
    s.stop();
  }

  public void testHas16() {
    Subject subj  = GrouperSubject.load(Util.rooti, Util.roott);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // Fetch ns
    GrouperGroup g = GrouperGroup.load(s, Util.ns2s, Util.ns2e, Grouper.NS_TYPE);
    // Fetch m
    GrouperMember m = GrouperMember.load(s, Util.m0i, Util.m0t);
    // Assert what privs m has on the ns
    List privs = Grouper.naming().has(s, g, m);
    Assert.assertNotNull(privs);
    Assert.assertTrue( privs.size() == 1 );
    Assert.assertFalse( Grouper.naming().has(s, g, m, Grouper.PRIV_CREATE) );
    Assert.assertTrue( Grouper.naming().has(s, g, m, Grouper.PRIV_STEM) );
    // We're done
    s.stop();
  }


  public void testRevoke5() {
    Subject subj  = GrouperSubject.load(Util.rooti, Util.roott);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // Fetch ns
    GrouperGroup g = GrouperGroup.load(s, Util.ns2s, Util.ns2e, Grouper.NS_TYPE);
    // Fetch m
    GrouperMember m = GrouperMember.load(s, Util.m0i, Util.m0t);
    // root revoke m0, STEM
    Assert.assertTrue( Grouper.naming().revoke(s, g, m, Grouper.PRIV_STEM) );
    // We're done
    s.stop();
  }

  public void testHas17() {
    Subject subj  = GrouperSubject.load(Util.rooti, Util.roott);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // Fetch ns
    GrouperGroup g = GrouperGroup.load(s, Util.ns2s, Util.ns2e, Grouper.NS_TYPE);
    // Fetch m
    GrouperMember m = GrouperMember.load(s, Util.m0i, Util.m0t);
    // Assert what privs m has on the ns
    List privs = Grouper.naming().has(s, g, m);
    Assert.assertNotNull(privs);
    Assert.assertTrue( privs.size() == 0 );
    Assert.assertFalse( Grouper.naming().has(s, g, m, Grouper.PRIV_CREATE) );
    Assert.assertFalse( Grouper.naming().has(s, g, m, Grouper.PRIV_STEM) );
    // We're done
    s.stop();
  }

  // root grant CREATE on ns0 to m0
  public void testGrant4() {
    Subject subj  = GrouperSubject.load(Util.rooti, Util.roott);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);
    GrouperGroup g = GrouperGroup.load(s, Util.ns0s, Util.ns0e, Grouper.NS_TYPE);
    GrouperMember m = GrouperMember.load(s, Util.m0i, Util.m0t);
    Assert.assertTrue( Grouper.naming().grant(s, g, m, Grouper.PRIV_CREATE) );
    s.stop();
  }

  // m0 create base group within ns0
  public void testCreate0() {
    Subject subj  = GrouperSubject.load(Util.m0i, Util.m0t);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);
    GrouperGroup g = GrouperGroup.create(s, Util.stem4, Util.extn4);
    Assert.assertNotNull(g);
    s.stop();
  }

  // m0 delete base group within ns0
  public void testDelete0() {
    Subject subj  = GrouperSubject.load(Util.m0i, Util.m0t);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);
    GrouperGroup g = GrouperGroup.load(s, Util.stem4, Util.extn4);
    Assert.assertNotNull(g);
    Assert.assertTrue( GrouperGroup.delete(s, g) );
    s.stop();
  }

  // m0 !create naming group within ns0
  public void testCreate1() {
    Subject subj  = GrouperSubject.load(Util.m0i, Util.m0t);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);
    GrouperGroup g = GrouperGroup.create(s, Util.stem4, Util.extn4, Grouper.NS_TYPE);
    Assert.assertNull(g);
    s.stop();
  }

  // root revoke CREATE on ns0 from m0
  public void testRevoke6() {
    Subject subj  = GrouperSubject.load(Util.rooti, Util.roott);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);
    GrouperGroup g = GrouperGroup.load(s, Util.ns0s, Util.ns0e, Grouper.NS_TYPE);
    GrouperMember m = GrouperMember.load(s, Util.m0i, Util.m0t);
    Assert.assertTrue( Grouper.naming().revoke(s, g, m, Grouper.PRIV_CREATE) );
    s.stop();
  }

}

