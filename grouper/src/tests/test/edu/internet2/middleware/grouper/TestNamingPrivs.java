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

  // TODO Test on naming groups
/*
  private String stem0  = null;
  private String stem1  = null;
  private String stem2  = null;
  private String stem00 = "stem.0";
  private String extn0  = "stem.0";
  private String extn1  = "stem.1";
  private String extn2  = "stem.2";
  private String extn00 = "stem.0.0";
*/
/*
  private String stem0  = "stem.0";
  private String stem1  = "stem.1";
  private String stem2  = "stem.2";
  private String extn0  = "extn.0";
  private String extn1  = "extn.1";
  private String extn2  = "extn.2";


*/

  private String  klass   = "edu.internet2.middleware.grouper.GrouperGroup";
  private String  naming  = "naming"; 

  private String  stem0   = "";
  private String  extn0   = "stem.0";
  private String  stem00  = "stem.0";
  private String  extn00  = "stem.0.0";
  private String  stem1   = "";
  private String  extn1   = "stem.1";
  private String  stem2   = "";
  private String  extn2   = "stem.2";
 
  private String  m0id    = "blair";
  private String  m1id    = "notblair";
  private String  m0type  = "person";
  private String  m1type  = "person";

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
    Subject         subj  = GrouperSubject.lookup( Grouper.config("member.system"), "person" );
    Assert.assertNotNull(subj);
    s.start(subj);
    // Fetch the namespaces
    // ns0
    GrouperGroup ns0 = GrouperGroup.load(s, stem0, extn0, naming);
    Assert.assertNotNull(ns0);
    Assert.assertTrue( klass.equals( ns0.getClass().getName() ) );
    Assert.assertNotNull( ns0.type() );
    Assert.assertTrue( ns0.type().equals(naming) );
    Assert.assertNotNull( ns0.attribute("stem") );
    Assert.assertTrue( ns0.attribute("stem").value().equals(stem0) );
    Assert.assertNotNull( ns0.attribute("extension") );
    Assert.assertTrue( ns0.attribute("extension").value().equals(extn0) );
    // ns00
    GrouperGroup ns00 = GrouperGroup.load(s, stem00, extn00, naming);
    Assert.assertNotNull(ns00);
    Assert.assertTrue( klass.equals( ns00.getClass().getName() ) );
    Assert.assertNotNull( ns00.type() );
    Assert.assertTrue( ns00.type().equals(naming) );
    Assert.assertNotNull( ns00.attribute("stem") );
    Assert.assertTrue( ns00.attribute("stem").value().equals(stem00) );
    Assert.assertNotNull( ns00.attribute("extension") );
    Assert.assertTrue( ns00.attribute("extension").value().equals(extn00) );
    // ns1
    GrouperGroup ns1 = GrouperGroup.load(s, stem1, extn1, naming);
    Assert.assertNotNull(ns1);
    Assert.assertTrue( klass.equals( ns1.getClass().getName() ) );
    Assert.assertNotNull( ns1.type() );
    Assert.assertTrue( ns1.type().equals(naming) );
    Assert.assertNotNull( ns1.attribute("stem") );
    Assert.assertTrue( ns1.attribute("stem").value().equals(stem1) );
    Assert.assertNotNull( ns1.attribute("extension") );
    Assert.assertTrue( ns1.attribute("extension").value().equals(extn1) );
    // ns2
    GrouperGroup ns2 = GrouperGroup.load(s, stem2, extn2, naming);
    Assert.assertNotNull(ns2);
    Assert.assertTrue( klass.equals( ns2.getClass().getName() ) );
    Assert.assertNotNull( ns2.type() );
    Assert.assertTrue( ns2.type().equals(naming) );
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

  
/*
  public void testHasGrantHasRevokeHas() {
    GrouperSession  s     = new GrouperSession();
    Assert.assertNotNull(s);
    Subject         subj  = GrouperSubject.lookup( Grouper.config("member.system"), "person" );
    Assert.assertNotNull(subj);
    s.start(subj);
    // Fetch ns0
    GrouperGroup ns0 = GrouperGroup.load(s, stem0, extn0);
    // Fetch m0
    GrouperMember m0 = GrouperMember.lookup(m0id, m0type);

    // Assert what privs m0 has on ns0
    List privs0 = Grouper.naming().has(s, ns0, m0);
    Assert.assertNotNull(privs0);
    Assert.assertTrue( privs0.size() == 0 );
    Assert.assertFalse( Grouper.naming().has(s, ns0, m0, "CREATE") );
    Assert.assertFalse( Grouper.naming().has(s, ns0, m0, "STEM") );

    // Take a broader view and see where m0 has each of the privs
    List privs0c  = Grouper.naming().has(s, m0, "CREATE");
    List privs0s  = Grouper.naming().has(s, m0, "STEM");
    Assert.assertNotNull(privs0c);
    Assert.assertTrue( privs0c.size() == 0 );
    Assert.assertNotNull(privs0s);
    Assert.assertTrue( privs0s.size() == 0 );

    // Take a broader view and see who has each of the privs
    List whoHas0c  = Grouper.naming().whoHas(s, ns0, "CREATE");
    List whoHas0s  = Grouper.naming().whoHas(s, ns0, "STEM");
    Assert.assertNotNull(whoHas0c);
    Assert.assertTrue( whoHas0c.size() == 0 );
    Assert.assertNotNull(whoHas0s);
    Assert.assertTrue( whoHas0s.size() == 0 );

    // Grant m0 all privs on ns0
    // We can't grant naming privs on !naming groups
    Assert.assertFalse( Grouper.naming().grant(s, ns0, m0, "CREATE") );
    Assert.assertFalse( Grouper.naming().grant(s, ns0, m0, "STEM") );

    // Assert what privs m0 has on ns0
    List privs1 = Grouper.naming().has(s, ns0, m0);
    Assert.assertNotNull(privs1);
    Assert.assertTrue( privs1.size() == 0 );
    Assert.assertFalse( Grouper.naming().has(s, ns0, m0, "CREATE") );
    Assert.assertFalse( Grouper.naming().has(s, ns0, m0, "STEM") );

    // Take a broader view and see where m0 has each of the privs
    List privs1c  = Grouper.naming().has(s, m0, "CREATE");
    List privs1s  = Grouper.naming().has(s, m0, "STEM");
    Assert.assertNotNull(privs1c);
    Assert.assertTrue( privs1c.size() == 0 );
    Assert.assertNotNull(privs1s);
    Assert.assertTrue( privs1s.size() == 0 );

    // Take a broader view and see who has each of the privs
    List whoHas1c  = Grouper.naming().whoHas(s, ns0, "CREATE");
    List whoHas1s  = Grouper.naming().whoHas(s, ns0, "STEM");
    Assert.assertNotNull(whoHas1c);
    Assert.assertTrue( whoHas1c.size() == 0 );
    Assert.assertNotNull(whoHas1s);
    Assert.assertTrue( whoHas1s.size() == 0 );

    // Revoke all privs m0 has on ns0
    Assert.assertFalse( Grouper.naming().revoke(s, ns0, m0, "CREATE") );
    Assert.assertFalse( Grouper.naming().revoke(s, ns0, m0, "STEM") );

    // Assert what privs m0 has on ns0
    List privs2 = Grouper.naming().has(s, ns0, m0);
    Assert.assertNotNull(privs2);
    Assert.assertTrue( privs2.size() == 0 );
    Assert.assertFalse( Grouper.naming().has(s, ns0, m0, "CREATE") );
    Assert.assertFalse( Grouper.naming().has(s, ns0, m0, "STEM") );

    // Take a broader view and see where m0 has each of the privs
    List privs2c  = Grouper.naming().has(s, m0, "CREATE");
    List privs2s  = Grouper.naming().has(s, m0, "STEM");
    Assert.assertNotNull(privs2c);
    Assert.assertTrue( privs2c.size() == 0 );
    Assert.assertNotNull(privs2s);
    Assert.assertTrue( privs2s.size() == 0 );

    // Take a broader view and see who has each of the privs
    List whoHas2c  = Grouper.naming().whoHas(s, ns0, "CREATE");
    List whoHas2s  = Grouper.naming().whoHas(s, ns0, "STEM");
    Assert.assertNotNull(whoHas2c);
    Assert.assertTrue( whoHas2c.size() == 0 );
    Assert.assertNotNull(whoHas2s);
    Assert.assertTrue( whoHas2s.size() == 0 );

    Assert.assertTrue( privs1s.size() == 0 );

    // We're done
    s.stop();
  }
*/

/*
  public void testHasPrivsCurrentSubject() {
    GrouperSession  s     = new GrouperSession();
    Assert.assertNotNull(s);
    Subject         subj  = GrouperSubject.lookup( Grouper.config("member.system"), "person" );
    Assert.assertNotNull(subj);
    s.start(subj);
    // Fetch the groups
    // ns0
    GrouperGroup    ns0  = GrouperGroup.load(s, stem0, extn0);
    // ns1
    GrouperGroup    ns1  = GrouperGroup.load(s, stem1, extn1);
    // ns2
    GrouperGroup    ns2  = GrouperGroup.load(s, stem2, extn2);
    // Fetch the members
    // Fetch m0
    GrouperMember   m0      = GrouperMember.lookup(m0id, m0type);
    // Fetch m1
    GrouperMember   m1      = GrouperMember.lookup(m1id, m1type);

    // What privs does the current subject have on ns0?
    List privs0 = Grouper.naming().has(s, ns0);
    Assert.assertNotNull(privs0);
    Assert.assertTrue( privs0.size() == 1 );
    Assert.assertTrue( Grouper.naming().has(s, ns0, "CREATE") );
    Assert.assertFalse( Grouper.naming().has(s, ns0, "STEM") );
    Assert.assertFalse( Grouper.naming().has(s, ns0, "OPTOUT") );
    Assert.assertFalse( Grouper.naming().has(s, ns0, "READ") );
    Assert.assertFalse( Grouper.naming().has(s, ns0, "UPDATE") );
    Assert.assertFalse( Grouper.naming().has(s, ns0, "VIEW") );
    
    // What privs does the current subject have on ns1?
    List privs1 = Grouper.naming().has(s, ns1);
    Assert.assertNotNull(privs1);
    Assert.assertTrue( privs1.size() == 1 );
    Assert.assertTrue( Grouper.naming().has(s, ns1, "CREATE") );
    Assert.assertFalse( Grouper.naming().has(s, ns1, "STEM") );
    Assert.assertFalse( Grouper.naming().has(s, ns1, "OPTOUT") );
    Assert.assertFalse( Grouper.naming().has(s, ns1, "READ") );
    Assert.assertFalse( Grouper.naming().has(s, ns1, "UPDATE") );
    Assert.assertFalse( Grouper.naming().has(s, ns1, "VIEW") );
    
    // What privs does the current subject have on ns2?
    List privs2 = Grouper.naming().has(s, ns2);
    Assert.assertNotNull(privs2);
    Assert.assertTrue( privs2.size() == 1 );
    Assert.assertTrue( Grouper.naming().has(s, ns2, "CREATE") );
    Assert.assertFalse( Grouper.naming().has(s, ns2, "STEM") );
    Assert.assertFalse( Grouper.naming().has(s, ns2, "OPTOUT") );
    Assert.assertFalse( Grouper.naming().has(s, ns2, "READ") );
    Assert.assertFalse( Grouper.naming().has(s, ns2, "UPDATE") );
    Assert.assertFalse( Grouper.naming().has(s, ns2, "VIEW") );
   
    // Take a broader view and see where the current subject has each
    // of the privs
    List privs3a  = Grouper.naming().has(s, "CREATE");
    List privs3oi = Grouper.naming().has(s, "STEM");
    List privs3oo = Grouper.naming().has(s, "OPTOUT");
    List privs3r  = Grouper.naming().has(s, "READ");
    List privs3u  = Grouper.naming().has(s, "UPDATE");
    List privs3v  = Grouper.naming().has(s, "VIEW");
    Assert.assertNotNull(privs3a);
    Assert.assertTrue( privs3a.size() == 7 );
    Assert.assertNotNull(privs3oi);
    Assert.assertTrue( privs3oi.size() == 0 );
    Assert.assertNotNull(privs3oo);
    Assert.assertTrue( privs3oo.size() == 0 );
    Assert.assertNotNull(privs3r);
    Assert.assertTrue( privs3r.size() == 0 );
    Assert.assertNotNull(privs3u);
    Assert.assertTrue( privs3u.size() == 0 );
    Assert.assertNotNull(privs3v);
    Assert.assertTrue( privs3v.size() == 0 );

    // We're done
    s.stop();
  }
*/

    // TODO All of the above
    // TODO Do GrouperSystem for below as well?
    // TODO Grouper.naming().has(s, ns0, m0);
    // TODO Grouper.naming().has(s, m0, "CREATE");
    // TODO boolean Grouper.naming().has(s, g0, m0, "CREATE");

    // TODO Grant
    // TODO Revoke

}

