/*
 * Copyright (C) 2004 University Corporation for Advanced Internet Development, Inc.
 * Copyright (C) 2004 The University Of Chicago
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
  private String  stem2   = Grouper.NS_ROOT;
  private String  extn2   = "stem.2";
 
  private String  m0id    = "blair";
  private String  m1id    = "notblair";
  private String  m0type  = Grouper.DEF_SUBJ_TYPE;
  private String  m1type  = Grouper.DEF_SUBJ_TYPE;

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
    GrouperSession  s     = new GrouperSession();
    Assert.assertNotNull(s);
    Subject         subj  = GrouperSubject.lookup( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    Assert.assertNotNull(subj);
    s.start(subj);
    // Fetch the namespaces
    // ns0
    GrouperGroup ns0 = GrouperGroup.lookup(s, stem0, extn0, Grouper.NS_TYPE);
    Assert.assertNotNull(ns0);
    Assert.assertTrue( klass.equals( ns0.getClass().getName() ) );
    Assert.assertNotNull( ns0.type() );
    Assert.assertTrue( ns0.type().equals(Grouper.NS_TYPE) );
    Assert.assertNotNull( ns0.attribute("stem") );
    Assert.assertTrue( ns0.attribute("stem").value().equals(stem0) );
    Assert.assertNotNull( ns0.attribute("extension") );
    Assert.assertTrue( ns0.attribute("extension").value().equals(extn0) );
    // ns00
    GrouperGroup ns00 = GrouperGroup.lookup(s, stem00, extn00, Grouper.NS_TYPE);
    Assert.assertNotNull(ns00);
    Assert.assertTrue( klass.equals( ns00.getClass().getName() ) );
    Assert.assertNotNull( ns00.type() );
    Assert.assertTrue( ns00.type().equals(Grouper.NS_TYPE) );
    Assert.assertNotNull( ns00.attribute("stem") );
    Assert.assertTrue( ns00.attribute("stem").value().equals(stem00) );
    Assert.assertNotNull( ns00.attribute("extension") );
    Assert.assertTrue( ns00.attribute("extension").value().equals(extn00) );
    // ns1
    GrouperGroup ns1 = GrouperGroup.lookup(s, stem1, extn1, Grouper.NS_TYPE);
    Assert.assertNotNull(ns1);
    Assert.assertTrue( klass.equals( ns1.getClass().getName() ) );
    Assert.assertNotNull( ns1.type() );
    Assert.assertTrue( ns1.type().equals(Grouper.NS_TYPE) );
    Assert.assertNotNull( ns1.attribute("stem") );
    Assert.assertTrue( ns1.attribute("stem").value().equals(stem1) );
    Assert.assertNotNull( ns1.attribute("extension") );
    Assert.assertTrue( ns1.attribute("extension").value().equals(extn1) );
    // ns2
    GrouperGroup ns2 = GrouperGroup.lookup(s, stem2, extn2, Grouper.NS_TYPE);
    Assert.assertNotNull(ns2);
    Assert.assertTrue( klass.equals( ns2.getClass().getName() ) );
    Assert.assertNotNull( ns2.type() );
    Assert.assertTrue( ns2.type().equals(Grouper.NS_TYPE) );
    Assert.assertNotNull( ns2.attribute("stem") );
    Assert.assertTrue( ns2.attribute("stem").value().equals(stem2) );
    Assert.assertNotNull( ns2.attribute("extension") );
    Assert.assertTrue( ns2.attribute("extension").value().equals(extn2) );
    // Fetch the members
    // Fetch m0
    GrouperMember m0 = GrouperMember.lookup(m0id, m0type);
    Assert.assertNotNull(m0);
    // Fetch m1
    GrouperMember m1 = GrouperMember.lookup(m1id, m1type);
    Assert.assertNotNull(m1);

    // We're done
    s.stop();
  }

  public void testHas0() {
    GrouperSession  s     = new GrouperSession();
    Assert.assertNotNull(s);
    Subject         subj  = GrouperSubject.lookup( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    s.start(subj);
    // Fetch ns
    GrouperGroup ns = GrouperGroup.lookup(s, stem0, extn0, Grouper.NS_TYPE);
    // Assert what privs the current subject has on the ns
    List privs = Grouper.naming().has(s, ns);
    Assert.assertNotNull(privs);
    Assert.assertTrue( privs.size() == 1 );
    Assert.assertFalse( Grouper.naming().has(s, ns, "CREATE") );
    Assert.assertTrue( Grouper.naming().has(s, ns, "STEM") );
    // We're done
    s.stop();
  }

  public void testHas1() {
    GrouperSession  s     = new GrouperSession();
    Assert.assertNotNull(s);
    Subject         subj  = GrouperSubject.lookup( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    s.start(subj);
    // Fetch ns
    GrouperGroup ns = GrouperGroup.lookup(s, stem00, extn00, Grouper.NS_TYPE);
    // Assert what privs the current subject has on the ns
    List privs = Grouper.naming().has(s, ns);
    Assert.assertNotNull(privs);
    Assert.assertTrue( privs.size() == 1 );
    Assert.assertFalse( Grouper.naming().has(s, ns, "CREATE") );
    Assert.assertTrue( Grouper.naming().has(s, ns, "STEM") );
    // We're done
    s.stop();
  }

  public void testHas2() {
    GrouperSession  s     = new GrouperSession();
    Assert.assertNotNull(s);
    Subject         subj  = GrouperSubject.lookup( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    s.start(subj);
    // Fetch ns
    GrouperGroup ns = GrouperGroup.lookup(s, stem1, extn1, Grouper.NS_TYPE);
    // Assert what privs the current subject has on the ns
    List privs = Grouper.naming().has(s, ns);
    Assert.assertNotNull(privs);
    Assert.assertTrue( privs.size() == 1 );
    Assert.assertFalse( Grouper.naming().has(s, ns, "CREATE") );
    Assert.assertTrue( Grouper.naming().has(s, ns, "STEM") );
    // We're done
    s.stop();
  }

  public void testHas3() {
    GrouperSession  s     = new GrouperSession();
    Assert.assertNotNull(s);
    Subject         subj  = GrouperSubject.lookup( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    s.start(subj);
    // Fetch ns
    GrouperGroup ns = GrouperGroup.lookup(s, stem2, extn2, Grouper.NS_TYPE);
    // Assert what privs the current subject has on the ns
    List privs = Grouper.naming().has(s, ns);
    Assert.assertNotNull(privs);
    Assert.assertTrue( privs.size() == 1 );
    Assert.assertFalse( Grouper.naming().has(s, ns, "CREATE") );
    Assert.assertTrue( Grouper.naming().has(s, ns, "STEM") );
    // We're done
    s.stop();
  }

  public void testHas4() {
    GrouperSession  s     = new GrouperSession();
    Assert.assertNotNull(s);
    Subject         subj  = GrouperSubject.lookup( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    s.start(subj);
    // Fetch ns
    GrouperGroup ns = GrouperGroup.lookup(s, stem0, extn0, Grouper.NS_TYPE);
    // Fetch m
    GrouperMember m = GrouperMember.lookup(m0id, m0type);
    // Assert what privs m has on the ns
    List privs = Grouper.naming().has(s, ns, m);
    Assert.assertNotNull(privs);
    Assert.assertTrue( privs.size() == 0 );
    Assert.assertFalse( Grouper.naming().has(s, ns, m, "CREATE") );
    Assert.assertFalse( Grouper.naming().has(s, ns, m, "STEM") );
    // We're done
    s.stop();
  }

  public void testHas5() {
    GrouperSession  s     = new GrouperSession();
    Assert.assertNotNull(s);
    Subject         subj  = GrouperSubject.lookup( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    s.start(subj);
    // Fetch ns
    GrouperGroup ns = GrouperGroup.lookup(s, stem00, extn00, Grouper.NS_TYPE);
    // Fetch m
    GrouperMember m = GrouperMember.lookup(m0id, m0type);
    // Assert what privs m has on the ns
    List privs = Grouper.naming().has(s, ns, m);
    Assert.assertNotNull(privs);
    Assert.assertTrue( privs.size() == 0 );
    Assert.assertFalse( Grouper.naming().has(s, ns, m, "CREATE") );
    Assert.assertFalse( Grouper.naming().has(s, ns, m, "STEM") );
    // We're done
    s.stop();
  }

  public void testHas6() {
    GrouperSession  s     = new GrouperSession();
    Assert.assertNotNull(s);
    Subject         subj  = GrouperSubject.lookup( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    s.start(subj);
    // Fetch ns
    GrouperGroup ns = GrouperGroup.lookup(s, stem1, extn1, Grouper.NS_TYPE);
    // Fetch m
    GrouperMember m = GrouperMember.lookup(m0id, m0type);
    // Assert what privs m has on the ns
    List privs = Grouper.naming().has(s, ns, m);
    Assert.assertNotNull(privs);
    Assert.assertTrue( privs.size() == 0 );
    Assert.assertFalse( Grouper.naming().has(s, ns, m, "CREATE") );
    Assert.assertFalse( Grouper.naming().has(s, ns, m, "STEM") );
    // We're done
    s.stop();
  }

  public void testHas7() {
    GrouperSession  s     = new GrouperSession();
    Assert.assertNotNull(s);
    Subject         subj  = GrouperSubject.lookup( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    s.start(subj);
    // Fetch ns
    GrouperGroup ns = GrouperGroup.lookup(s, stem2, extn2, Grouper.NS_TYPE);
    // Fetch m
    GrouperMember m = GrouperMember.lookup(m0id, m0type);
    // Assert what privs m has on the ns
    List privs = Grouper.naming().has(s, ns, m);
    Assert.assertNotNull(privs);
    Assert.assertTrue( privs.size() == 0 );
    Assert.assertFalse( Grouper.naming().has(s, ns, m, "CREATE") );
    Assert.assertFalse( Grouper.naming().has(s, ns, m, "STEM") );
    // We're done
    s.stop();
  }

  // TODO Granting of privs
  // TODO Revoking of privs
  // TODO Rest of priv interface methods

}

