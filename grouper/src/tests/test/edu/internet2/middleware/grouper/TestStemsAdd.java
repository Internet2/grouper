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
  private String  naming  = "naming";

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
    Subject         subj  = GrouperSubject.lookup( Grouper.config("member.system"), "person" );
    s.start(subj);
    // Confirm that namespaces don't exist
    GrouperGroup    ns0   = GrouperGroup.lookup(s, stem0, extn0, naming);
    Assert.assertNull(ns0);
    GrouperGroup    ns00  = GrouperGroup.lookup(s, stem00, extn00, naming);
    Assert.assertNull(ns00);
    GrouperGroup    ns1   = GrouperGroup.lookup(s, stem1, extn1, naming);
    Assert.assertNull(ns1);
    GrouperGroup    ns2   = GrouperGroup.lookup(s, stem2, extn2, naming);
    Assert.assertNull(ns2);
    // We're done
    s.stop();
  }

  // Create namespaces
  public void testCreateGroups() {
    GrouperSession  s     = new GrouperSession();
    Subject         subj  = GrouperSubject.lookup( Grouper.config("member.system"), "person" );
    s.start(subj);
    // Create the namespaces
    // ns0
    GrouperGroup ns0 = GrouperGroup.create(s, stem0, extn0, naming);
    Assert.assertNotNull(ns0);
    Assert.assertTrue( klass.equals( ns0.getClass().getName() ) );
    Assert.assertNotNull( ns0.type() );
    Assert.assertTrue( ns0.type().equals(naming) ); 
    Assert.assertNotNull( ns0.attribute("stem") );
    Assert.assertTrue( ns0.attribute("stem").value().equals(stem0) );
    Assert.assertNotNull( ns0.attribute("extension") );
    Assert.assertTrue( ns0.attribute("extension").value().equals(extn0) );

    // ns1
    GrouperGroup ns1 = GrouperGroup.create(s, stem1, extn1, naming);
    Assert.assertNotNull(ns1);
    Assert.assertTrue( klass.equals( ns1.getClass().getName() ) );
    Assert.assertNotNull( ns1.type() );
    Assert.assertTrue( ns1.type().equals(naming) ); 
    Assert.assertNotNull( ns1.attribute("stem") );
    Assert.assertTrue( ns1.attribute("stem").value().equals(stem1) );
    Assert.assertNotNull( ns1.attribute("extension") );
    Assert.assertTrue( ns1.attribute("extension").value().equals(extn1) );

    // ns2
    GrouperGroup ns2 = GrouperGroup.create(s, stem2, extn2, naming);
    Assert.assertNotNull(ns2);
    Assert.assertTrue( klass.equals( ns2.getClass().getName() ) );
    Assert.assertNotNull( ns2.type() );
    Assert.assertTrue( ns2.type().equals(naming) ); 
    Assert.assertNotNull( ns2.attribute("stem") );
    Assert.assertTrue( ns2.attribute("stem").value().equals(stem2) );
    Assert.assertNotNull( ns2.attribute("extension") );
    Assert.assertTrue( ns2.attribute("extension").value().equals(extn2) );

    // ns00
    GrouperGroup ns00 = GrouperGroup.create(s, stem00, extn00, naming);
    Assert.assertNotNull(ns00);
    Assert.assertTrue( klass.equals( ns00.getClass().getName() ) );
    Assert.assertNotNull( ns00.type() );
    Assert.assertTrue( ns00.type().equals(naming) ); 
    Assert.assertNotNull( ns00.attribute("stem") );
    Assert.assertTrue( ns00.attribute("stem").value().equals(stem00) );
    Assert.assertNotNull( ns00.attribute("extension") );
    Assert.assertTrue( ns00.attribute("extension").value().equals(extn00) );

    // We're done
    s.stop();
  }

  public void testFetchNS0() {
    GrouperSession  s     = new GrouperSession();
    Subject         subj  = GrouperSubject.lookup( Grouper.config("member.system"), "person" );
    s.start(subj);
    // Fetch ns0
    GrouperGroup ns = GrouperGroup.lookup(s, stem0, extn0, naming);
    Assert.assertNotNull(ns);
    Assert.assertTrue( klass.equals( ns.getClass().getName() ) );
    Assert.assertNotNull( ns.type() );
    Assert.assertTrue( ns.type().equals(naming) ); 
    Assert.assertNotNull( ns.attribute("stem") );
    Assert.assertTrue( ns.attribute("stem").value().equals(stem0) );
    Assert.assertNotNull( ns.attribute("extension") );
    Assert.assertTrue( ns.attribute("extension").value().equals(extn0) );
    // We're done
    s.stop();
  }

  public void testFetchNS1() {
    GrouperSession  s     = new GrouperSession();
    Subject         subj  = GrouperSubject.lookup( Grouper.config("member.system"), "person" );
    s.start(subj);
    // Fetch ns00
    GrouperGroup ns = GrouperGroup.lookup(s, stem00, extn00, naming);
    Assert.assertNotNull(ns);
    Assert.assertTrue( klass.equals( ns.getClass().getName() ) );
    Assert.assertNotNull( ns.type() );
    Assert.assertTrue( ns.type().equals(naming) ); 
    Assert.assertNotNull( ns.attribute("stem") );
    Assert.assertTrue( ns.attribute("stem").value().equals(stem00) );
    Assert.assertNotNull( ns.attribute("extension") );
    Assert.assertTrue( ns.attribute("extension").value().equals(extn00) );
    // We're done
    s.stop();
  }

  public void testFetchNS2() {
    GrouperSession  s     = new GrouperSession();
    Subject         subj  = GrouperSubject.lookup( Grouper.config("member.system"), "person" );
    s.start(subj);
    // Fetch ns1
    GrouperGroup ns = GrouperGroup.lookup(s, stem1, extn1, naming);
    Assert.assertNotNull(ns);
    Assert.assertTrue( klass.equals( ns.getClass().getName() ) );
    Assert.assertNotNull( ns.type() );
    Assert.assertTrue( ns.type().equals(naming) ); 
    Assert.assertNotNull( ns.attribute("stem") );
    Assert.assertTrue( ns.attribute("stem").value().equals(stem1) );
    Assert.assertNotNull( ns.attribute("extension") );
    Assert.assertTrue( ns.attribute("extension").value().equals(extn1) );
    // We're done
    s.stop();
  }

  public void testFetchNS3() {
    GrouperSession  s     = new GrouperSession();
    Subject         subj  = GrouperSubject.lookup( Grouper.config("member.system"), "person" );
    s.start(subj);
    // Fetch ns2
    GrouperGroup ns = GrouperGroup.lookup(s, stem2, extn2, naming);
    Assert.assertNotNull(ns);
    Assert.assertTrue( klass.equals( ns.getClass().getName() ) );
    Assert.assertNotNull( ns.type() );
    Assert.assertTrue( ns.type().equals(naming) ); 
    Assert.assertNotNull( ns.attribute("stem") );
    Assert.assertTrue( ns.attribute("stem").value().equals(stem2) );
    Assert.assertNotNull( ns.attribute("extension") );
    Assert.assertTrue( ns.attribute("extension").value().equals(extn2) );
    // We're done
    s.stop();
  }

}

