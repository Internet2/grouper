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
import  junit.framework.*;


public class TestNamespaces extends TestCase {

  private String  klass   = "edu.internet2.middleware.grouper.GrouperGroup";

  private String  stem0   = Grouper.NS_ROOT;
  private String  extn0   = "stem.0";
  private String  stem00  = "stem.0";
  private String  extn00  = "stem.0.0";
  private String  stem1   = Grouper.NS_ROOT;
  private String  extn1   = "stem.1";
  private String  stem2   = Grouper.NS_ROOT;
  private String  extn2   = "stem.2";
  

  public TestNamespaces(String name) {
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
  

  // Fetch a non-existent namespaces
  public void testGroupsExistFalse() {
    GrouperSession  s     = new GrouperSession();
    Subject         subj  = GrouperSubject.lookup( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    s.start(subj);
    // Confirm that namespaces don't exist
    GrouperGroup    ns0   = GrouperGroup.lookup(s, stem0, extn0, Grouper.NS_TYPE);
    Assert.assertNull(ns0);
    GrouperGroup    ns00  = GrouperGroup.lookup(s, stem00, extn00, Grouper.NS_TYPE);
    Assert.assertNull(ns00);
    GrouperGroup    ns1   = GrouperGroup.lookup(s, stem1, extn1, Grouper.NS_TYPE);
    Assert.assertNull(ns1);
    GrouperGroup    ns2   = GrouperGroup.lookup(s, stem2, extn2, Grouper.NS_TYPE);
    Assert.assertNull(ns2);
    // We're done
    s.stop();
  }

  public void testCreateNS0() {
    GrouperSession  s     = new GrouperSession();
    Subject         subj  = GrouperSubject.lookup( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    s.start(subj);
    // Create ns0
    String stem = stem0;
    String extn = extn0;
    GrouperGroup ns = GrouperGroup.create(s, stem, extn, Grouper.NS_TYPE);
    Assert.assertNotNull(ns);
    Assert.assertTrue( klass.equals( ns.getClass().getName() ) );
    Assert.assertNotNull( ns.type() );
    Assert.assertTrue( ns.type().equals(Grouper.NS_TYPE) ); 
    Assert.assertNotNull( ns.attribute("stem") );
    Assert.assertTrue( ns.attribute("stem").value().equals(stem) );
    Assert.assertNotNull( ns.attribute("extension") );
    Assert.assertTrue( ns.attribute("extension").value().equals(extn) );
    s.stop();
  }

  public void testCreateNS1() {
    GrouperSession  s     = new GrouperSession();
    Subject         subj  = GrouperSubject.lookup( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    s.start(subj);
    // Create ns00
    String stem = stem00;
    String extn = extn00;
    GrouperGroup ns = GrouperGroup.create(s, stem, extn, Grouper.NS_TYPE);
    Assert.assertNotNull(ns);
    Assert.assertTrue( klass.equals( ns.getClass().getName() ) );
    Assert.assertNotNull( ns.type() );
    Assert.assertTrue( ns.type().equals(Grouper.NS_TYPE) ); 
    Assert.assertNotNull( ns.attribute("stem") );
    Assert.assertTrue( ns.attribute("stem").value().equals(stem) );
    Assert.assertNotNull( ns.attribute("extension") );
    Assert.assertTrue( ns.attribute("extension").value().equals(extn) );
    s.stop();
  }

  public void testCreateNS2() {
    GrouperSession  s     = new GrouperSession();
    Subject         subj  = GrouperSubject.lookup( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    s.start(subj);
    // Create ns1
    String stem = stem1;
    String extn = extn1;
    GrouperGroup ns = GrouperGroup.create(s, stem, extn, Grouper.NS_TYPE);
    Assert.assertNotNull(ns);
    Assert.assertTrue( klass.equals( ns.getClass().getName() ) );
    Assert.assertNotNull( ns.type() );
    Assert.assertTrue( ns.type().equals(Grouper.NS_TYPE) ); 
    Assert.assertNotNull( ns.attribute("stem") );
    Assert.assertTrue( ns.attribute("stem").value().equals(stem) );
    Assert.assertNotNull( ns.attribute("extension") );
    Assert.assertTrue( ns.attribute("extension").value().equals(extn) );
    s.stop();
  }

  public void testCreateNS3() {
    GrouperSession  s     = new GrouperSession();
    Subject         subj  = GrouperSubject.lookup( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    s.start(subj);
    // Create ns2
    String stem = stem2;
    String extn = extn2;
    GrouperGroup ns = GrouperGroup.create(s, stem, extn, Grouper.NS_TYPE);
    Assert.assertNotNull(ns);
    Assert.assertTrue( klass.equals( ns.getClass().getName() ) );
    Assert.assertNotNull( ns.type() );
    Assert.assertTrue( ns.type().equals(Grouper.NS_TYPE) ); 
    Assert.assertNotNull( ns.attribute("stem") );
    Assert.assertTrue( ns.attribute("stem").value().equals(stem) );
    Assert.assertNotNull( ns.attribute("extension") );
    Assert.assertTrue( ns.attribute("extension").value().equals(extn) );
    s.stop();
  }

  public void testFetchNS0() {
    GrouperSession  s     = new GrouperSession();
    Subject         subj  = GrouperSubject.lookup( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    s.start(subj);
    // Fetch ns0
    String stem = stem0;
    String extn = extn0;
    GrouperGroup ns = GrouperGroup.lookup(s, stem, extn, Grouper.NS_TYPE);
    Assert.assertNotNull(ns);
    Assert.assertTrue( klass.equals( ns.getClass().getName() ) );
    Assert.assertNotNull( ns.type() );
    Assert.assertTrue( ns.type().equals(Grouper.NS_TYPE) ); 
    Assert.assertNotNull( ns.attribute("stem") );
    //Assert.assertTrue( ns.attribute("stem").value().equals(stem) );
    Assert.assertNotNull( ns.attribute("extension") );
    //Assert.assertTrue( ns.attribute("extension").value().equals(extn) );
    // We're done
    s.stop();
  }

  public void testFetchNS1() {
    GrouperSession  s     = new GrouperSession();
    Subject         subj  = GrouperSubject.lookup( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    s.start(subj);
    // Fetch ns00
    String stem = stem00;
    String extn = extn00;
    GrouperGroup ns = GrouperGroup.lookup(s, stem, extn, Grouper.NS_TYPE);
    Assert.assertNotNull(ns);
    Assert.assertTrue( klass.equals( ns.getClass().getName() ) );
    Assert.assertNotNull( ns.type() );
    Assert.assertTrue( ns.type().equals(Grouper.NS_TYPE) ); 
    Assert.assertNotNull( ns.attribute("stem") );
    Assert.assertTrue( ns.attribute("stem").value().equals(stem) );
    Assert.assertNotNull( ns.attribute("extension") );
    Assert.assertTrue( ns.attribute("extension").value().equals(extn) );
    // We're done
    s.stop();
  }

  public void testFetchNS2() {
    GrouperSession  s     = new GrouperSession();
    Subject         subj  = GrouperSubject.lookup( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    s.start(subj);
    // Fetch ns1
    String stem = stem1;
    String extn = extn1;
    GrouperGroup ns = GrouperGroup.lookup(s, stem, extn, Grouper.NS_TYPE);
    Assert.assertNotNull(ns);
    Assert.assertTrue( klass.equals( ns.getClass().getName() ) );
    Assert.assertNotNull( ns.type() );
    Assert.assertTrue( ns.type().equals(Grouper.NS_TYPE) ); 
    Assert.assertNotNull( ns.attribute("stem") );
    Assert.assertTrue( ns.attribute("stem").value().equals(stem) );
    Assert.assertNotNull( ns.attribute("extension") );
    Assert.assertTrue( ns.attribute("extension").value().equals(extn) );
    // We're done
    s.stop();
  }

  public void testFetchNS3() {
    GrouperSession  s     = new GrouperSession();
    Subject         subj  = GrouperSubject.lookup( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    s.start(subj);
    // Fetch ns2
    String stem = stem2;
    String extn = extn2;
    GrouperGroup ns = GrouperGroup.lookup(s, stem, extn, Grouper.NS_TYPE);
    Assert.assertNotNull(ns);
    Assert.assertTrue( klass.equals( ns.getClass().getName() ) );
    Assert.assertNotNull( ns.type() );
    Assert.assertTrue( ns.type().equals(Grouper.NS_TYPE) ); 
    Assert.assertNotNull( ns.attribute("stem") );
    Assert.assertTrue( ns.attribute("stem").value().equals(stem) );
    Assert.assertNotNull( ns.attribute("extension") );
    Assert.assertTrue( ns.attribute("extension").value().equals(extn) );
    // We're done
    s.stop();
  }

}

