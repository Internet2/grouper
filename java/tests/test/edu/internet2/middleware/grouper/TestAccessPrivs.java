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

/*
 * $Id: TestAccessPrivs.java,v 1.12 2004-11-29 19:05:17 blair Exp $
 */

package test.edu.internet2.middleware.grouper;

import  edu.internet2.middleware.grouper.*;
import  edu.internet2.middleware.subject.*;
import  java.util.*;
import  junit.framework.*;


public class TestAccessPrivs extends TestCase {

  private String extn0  = "extn.0";
  private String extn1  = "extn.1";
  private String extn2  = "extn.2";
  private String klass  = "edu.internet2.middleware.grouper.GrouperGroup";
  private String m0id   = "blair";
  private String m0type = "person";
  private String m1id   = "notblair";
  private String m1type = "person";
  private String stem0  = "stem.0";
  private String stem1  = "stem.1";
  private String stem2  = "stem.2";

  public TestAccessPrivs(String name) {
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
    // Fetch the groups
    // g0
    GrouperGroup    g0  = GrouperGroup.load(s, stem0, extn0);
    Assert.assertNotNull(g0);
    Assert.assertTrue( klass.equals( g0.getClass().getName() ) );
    Assert.assertTrue( g0.exists() );
    Assert.assertNotNull( g0.type() );
    Assert.assertNotNull( g0.attribute("stem") );
    Assert.assertTrue( g0.attribute("stem").value().equals(stem0) );
    Assert.assertNotNull( g0.attribute("extension") );
    Assert.assertTrue( g0.attribute("extension").value().equals(extn0) );
    // g1
    GrouperGroup    g1  = GrouperGroup.load(s, stem1, extn1);
    Assert.assertNotNull(g1);
    Assert.assertTrue( klass.equals( g1.getClass().getName() ) );
    Assert.assertTrue( g1.exists() );
    Assert.assertNotNull( g1.type() );
    Assert.assertNotNull( g1.attribute("stem") );
    Assert.assertTrue( g1.attribute("stem").value().equals(stem1) );
    Assert.assertNotNull( g1.attribute("extension") );
    Assert.assertTrue( g1.attribute("extension").value().equals(extn1) );
    // g2
    GrouperGroup    g2  = GrouperGroup.load(s, stem2, extn2);
    Assert.assertNotNull(g2);
    Assert.assertTrue( klass.equals( g2.getClass().getName() ) );
    Assert.assertTrue( g2.exists() );
    Assert.assertNotNull( g2.type() );
    Assert.assertNotNull( g2.attribute("stem") );
    Assert.assertTrue( g2.attribute("stem").value().equals(stem2) );
    Assert.assertNotNull( g2.attribute("extension") );
    Assert.assertTrue( g1.attribute("extension").value().equals(extn1) );
    // Fetch the members
    // Fetch Member 0
    GrouperMember   m0      = GrouperMember.lookup(m0id, m0type);
    Assert.assertNotNull(m0);
    // Fetch Member 1
    GrouperMember   m1      = GrouperMember.lookup(m1id, m1type);
    Assert.assertNotNull(m1);
    // We're done
    s.stop();
  }

  public void testHasGrantHasRevokeHas() {
    GrouperSession  s     = new GrouperSession();
    Assert.assertNotNull(s);
    Subject         subj  = GrouperSubject.lookup( Grouper.config("member.system"), "person" );
    Assert.assertNotNull(subj);
    s.start(subj);
    // Fetch g0
    GrouperGroup  g0 = GrouperGroup.load(s, stem0, extn0);
    // Fetch Member 0
    GrouperMember m0   = GrouperMember.lookup(m0id, m0type);

    // Assert what privs m0 has on g0
    List privs0 = Grouper.access().has(s, g0, m0);
    Assert.assertNotNull(privs0);
    Assert.assertTrue( privs0.size() == 0 );
    Assert.assertFalse( Grouper.access().has(s, g0, m0, "ADMIN") );
    Assert.assertFalse( Grouper.access().has(s, g0, m0, "OPTIN") );
    Assert.assertFalse( Grouper.access().has(s, g0, m0, "OPTOUT") );
    Assert.assertFalse( Grouper.access().has(s, g0, m0, "READ") );
    Assert.assertFalse( Grouper.access().has(s, g0, m0, "UPDATE") );
    Assert.assertFalse( Grouper.access().has(s, g0, m0, "VIEW") );

    // Take a broader view and see where m0 has each of the privs
    List privs0a  = Grouper.access().has(s, m0, "ADMIN");
    List privs0oi = Grouper.access().has(s, m0, "OPTIN");
    List privs0oo = Grouper.access().has(s, m0, "OPTOUT");
    List privs0r  = Grouper.access().has(s, m0, "READ");
    List privs0u  = Grouper.access().has(s, m0, "UPDATE");
    List privs0v  = Grouper.access().has(s, m0, "VIEW");
    Assert.assertNotNull(privs0a);
    Assert.assertTrue( privs0a.size() == 0 );
    Assert.assertNotNull(privs0oi);
    Assert.assertTrue( privs0oi.size() == 0 );
    Assert.assertNotNull(privs0oo);
    Assert.assertTrue( privs0oo.size() == 0 );
    Assert.assertNotNull(privs0r);
    Assert.assertTrue( privs0r.size() == 0 );
    Assert.assertNotNull(privs0u);
    Assert.assertTrue( privs0u.size() == 0 );
    Assert.assertNotNull(privs0v);
    Assert.assertTrue( privs0v.size() == 0 );

    // Take a broader view and see who has each of the privs
    List whoHas0a   = Grouper.access().whoHas(s, g0, "ADMIN");
    List whoHas0oi  = Grouper.access().whoHas(s, g0, "OPTIN");
    List whoHas0oo  = Grouper.access().whoHas(s, g0, "OPTOUT");
    List whoHas0r   = Grouper.access().whoHas(s, g0, "READ");
    List whoHas0u   = Grouper.access().whoHas(s, g0, "UPDATE");
    List whoHas0v   = Grouper.access().whoHas(s, g0, "VIEW");
    Assert.assertNotNull(whoHas0a);
    Assert.assertTrue( whoHas0a.size() == 1);
    Assert.assertNotNull(whoHas0oi);
    Assert.assertTrue( whoHas0oi.size() == 0);
    Assert.assertNotNull(whoHas0oo);
    Assert.assertTrue( whoHas0oo.size() == 0);
    Assert.assertNotNull(whoHas0r);
    Assert.assertTrue( whoHas0r.size() == 0);
    Assert.assertNotNull(whoHas0u);
    Assert.assertTrue( whoHas0u.size() == 0);
    Assert.assertNotNull(whoHas0v);
    Assert.assertTrue( whoHas0v.size() == 0);

    // Grant m0 all privs on g0
    Assert.assertTrue( Grouper.access().grant(s, g0, m0, "ADMIN") );
    Assert.assertTrue( Grouper.access().grant(s, g0, m0, "OPTIN") );
    Assert.assertTrue( Grouper.access().grant(s, g0, m0, "OPTOUT") );
    Assert.assertTrue( Grouper.access().grant(s, g0, m0, "READ") );
    Assert.assertTrue( Grouper.access().grant(s, g0, m0, "UPDATE") );
    Assert.assertTrue( Grouper.access().grant(s, g0, m0, "VIEW") );

    // Assert what privs m0 has on g0
    List privs1 = Grouper.access().has(s, g0, m0);
    Assert.assertNotNull(privs1);
    Assert.assertTrue( privs1.size() == 6 );
    Assert.assertTrue( Grouper.access().has(s, g0, m0, "ADMIN") );
    Assert.assertTrue( Grouper.access().has(s, g0, m0, "OPTIN") );
    Assert.assertTrue( Grouper.access().has(s, g0, m0, "OPTOUT") );
    Assert.assertTrue( Grouper.access().has(s, g0, m0, "READ") );
    Assert.assertTrue( Grouper.access().has(s, g0, m0, "UPDATE") );
    Assert.assertTrue( Grouper.access().has(s, g0, m0, "VIEW") );

    // Take a broader view and see where m0 has each of the privs
    List privs1a  = Grouper.access().has(s, m0, "ADMIN");
    List privs1oi = Grouper.access().has(s, m0, "OPTIN");
    List privs1oo = Grouper.access().has(s, m0, "OPTOUT");
    List privs1r  = Grouper.access().has(s, m0, "READ");
    List privs1u  = Grouper.access().has(s, m0, "UPDATE");
    List privs1v  = Grouper.access().has(s, m0, "VIEW");
    Assert.assertNotNull(privs1a);
    Assert.assertTrue( privs1a.size() == 1 );
    Assert.assertNotNull(privs1oi);
    Assert.assertTrue( privs1oi.size() == 1 );
    Assert.assertNotNull(privs1oo);
    Assert.assertTrue( privs1oo.size() == 1 );
    Assert.assertNotNull(privs1r);
    Assert.assertTrue( privs1r.size() == 1 );
    Assert.assertNotNull(privs1u);
    Assert.assertTrue( privs1u.size() == 1 );
    Assert.assertNotNull(privs1v);
    Assert.assertTrue( privs1v.size() == 1 );

    // Take a broader view and see who has each of the privs
    List whoHas1a   = Grouper.access().whoHas(s, g0, "ADMIN");
    List whoHas1oi  = Grouper.access().whoHas(s, g0, "OPTIN");
    List whoHas1oo  = Grouper.access().whoHas(s, g0, "OPTOUT");
    List whoHas1r   = Grouper.access().whoHas(s, g0, "READ");
    List whoHas1u   = Grouper.access().whoHas(s, g0, "UPDATE");
    List whoHas1v   = Grouper.access().whoHas(s, g0, "VIEW");
    Assert.assertNotNull(whoHas1a);
    Assert.assertTrue( whoHas1a.size() == 2);
    Assert.assertNotNull(whoHas1oi);
    Assert.assertTrue( whoHas1oi.size() == 1);
    Assert.assertNotNull(whoHas1oo);
    Assert.assertTrue( whoHas1oo.size() == 1);
    Assert.assertNotNull(whoHas1r);
    Assert.assertTrue( whoHas1r.size() == 1);
    Assert.assertNotNull(whoHas1u);
    Assert.assertTrue( whoHas1u.size() == 1);
    Assert.assertNotNull(whoHas1v);
    Assert.assertTrue( whoHas1v.size() == 1);

    // Revoke all privs m0 has on g0
    Assert.assertTrue( Grouper.access().revoke(s, g0, m0, "ADMIN") );
    Assert.assertTrue( Grouper.access().revoke(s, g0, m0, "OPTIN") );
    Assert.assertTrue( Grouper.access().revoke(s, g0, m0, "OPTOUT") );
    Assert.assertTrue( Grouper.access().revoke(s, g0, m0, "READ") );
    Assert.assertTrue( Grouper.access().revoke(s, g0, m0, "UPDATE") );
    Assert.assertTrue( Grouper.access().revoke(s, g0, m0, "VIEW") );

    // Assert what privs m0 has on g0
    List privs2 = Grouper.access().has(s, g0, m0);
    Assert.assertNotNull(privs2);
    Assert.assertTrue( privs2.size() == 0 );
    Assert.assertFalse( Grouper.access().has(s, g0, m0, "ADMIN") );
    Assert.assertFalse( Grouper.access().has(s, g0, m0, "OPTIN") );
    Assert.assertFalse( Grouper.access().has(s, g0, m0, "OPTOUT") );
    Assert.assertFalse( Grouper.access().has(s, g0, m0, "READ") );
    Assert.assertFalse( Grouper.access().has(s, g0, m0, "UPDATE") );
    Assert.assertFalse( Grouper.access().has(s, g0, m0, "VIEW") );

    // Take a broader view and see where m0 has each of the privs
    List privs2a  = Grouper.access().has(s, m0, "ADMIN");
    List privs2oi = Grouper.access().has(s, m0, "OPTIN");
    List privs2oo = Grouper.access().has(s, m0, "OPTOUT");
    List privs2r  = Grouper.access().has(s, m0, "READ");
    List privs2u  = Grouper.access().has(s, m0, "UPDATE");
    List privs2v  = Grouper.access().has(s, m0, "VIEW");
    Assert.assertNotNull(privs2a);
    Assert.assertTrue( privs2a.size() == 0 );
    Assert.assertNotNull(privs2oi);
    Assert.assertTrue( privs2oi.size() == 0 );
    Assert.assertNotNull(privs2oo);
    Assert.assertTrue( privs2oo.size() == 0 );
    Assert.assertNotNull(privs2r);
    Assert.assertTrue( privs2r.size() == 0 );
    Assert.assertNotNull(privs2u);
    Assert.assertTrue( privs2u.size() == 0 );
    Assert.assertNotNull(privs2v);
    Assert.assertTrue( privs2v.size() == 0 );

    // Take a broader view and see who has each of the privs
    List whoHas2a   = Grouper.access().whoHas(s, g0, "ADMIN");
    List whoHas2oi  = Grouper.access().whoHas(s, g0, "OPTIN");
    List whoHas2oo  = Grouper.access().whoHas(s, g0, "OPTOUT");
    List whoHas2r   = Grouper.access().whoHas(s, g0, "READ");
    List whoHas2u   = Grouper.access().whoHas(s, g0, "UPDATE");
    List whoHas2v   = Grouper.access().whoHas(s, g0, "VIEW");
    Assert.assertNotNull(whoHas2a);
    Assert.assertTrue( whoHas2a.size() == 1);
    Assert.assertNotNull(whoHas2oi);
    Assert.assertTrue( whoHas2oi.size() == 0);
    Assert.assertNotNull(whoHas2oo);
    Assert.assertTrue( whoHas2oo.size() == 0);
    Assert.assertNotNull(whoHas2r);
    Assert.assertTrue( whoHas2r.size() == 0);
    Assert.assertNotNull(whoHas2u);
    Assert.assertTrue( whoHas2u.size() == 0);
    Assert.assertNotNull(whoHas2v);
    Assert.assertTrue( whoHas2v.size() == 0);

    // We're done
    s.stop();
  }

  public void testHasPrivsCurrentSubject() {
    GrouperSession  s     = new GrouperSession();
    Assert.assertNotNull(s);
    Subject         subj  = GrouperSubject.lookup( Grouper.config("member.system"), "person" );
    Assert.assertNotNull(subj);
    s.start(subj);
    // Fetch the groups
    // g0
    GrouperGroup    g0  = GrouperGroup.load(s, stem0, extn0);
    // g1
    GrouperGroup    g1  = GrouperGroup.load(s, stem1, extn1);
    // g2
    GrouperGroup    g2  = GrouperGroup.load(s, stem2, extn2);
    // Fetch the members
    // Fetch Member 0
    GrouperMember   m0      = GrouperMember.lookup(m0id, m0type);
    // Fetch Member 1
    GrouperMember   m1      = GrouperMember.lookup(m1id, m1type);

    // What privs does the current subject have on g0?
    List privs0 = Grouper.access().has(s, g0);
    Assert.assertNotNull(privs0);
    Assert.assertTrue( privs0.size() == 1 );
    Assert.assertTrue( Grouper.access().has(s, g0, "ADMIN") );
    Assert.assertFalse( Grouper.access().has(s, g0, "OPTIN") );
    Assert.assertFalse( Grouper.access().has(s, g0, "OPTOUT") );
    Assert.assertFalse( Grouper.access().has(s, g0, "READ") );
    Assert.assertFalse( Grouper.access().has(s, g0, "UPDATE") );
    Assert.assertFalse( Grouper.access().has(s, g0, "VIEW") );
    
    // What privs does the current subject have on g1?
    List privs1 = Grouper.access().has(s, g1);
    Assert.assertNotNull(privs1);
    Assert.assertTrue( privs1.size() == 1 );
    Assert.assertTrue( Grouper.access().has(s, g1, "ADMIN") );
    Assert.assertFalse( Grouper.access().has(s, g1, "OPTIN") );
    Assert.assertFalse( Grouper.access().has(s, g1, "OPTOUT") );
    Assert.assertFalse( Grouper.access().has(s, g1, "READ") );
    Assert.assertFalse( Grouper.access().has(s, g1, "UPDATE") );
    Assert.assertFalse( Grouper.access().has(s, g1, "VIEW") );
    
    // What privs does the current subject have on g2?
    List privs2 = Grouper.access().has(s, g2);
    Assert.assertNotNull(privs2);
    Assert.assertTrue( privs2.size() == 1 );
    Assert.assertTrue( Grouper.access().has(s, g2, "ADMIN") );
    Assert.assertFalse( Grouper.access().has(s, g2, "OPTIN") );
    Assert.assertFalse( Grouper.access().has(s, g2, "OPTOUT") );
    Assert.assertFalse( Grouper.access().has(s, g2, "READ") );
    Assert.assertFalse( Grouper.access().has(s, g2, "UPDATE") );
    Assert.assertFalse( Grouper.access().has(s, g2, "VIEW") );
   
    // Take a broader view and see where the current subject has each
    // of the privs
    List privs3a  = Grouper.access().has(s, "ADMIN");
    List privs3oi = Grouper.access().has(s, "OPTIN");
    List privs3oo = Grouper.access().has(s, "OPTOUT");
    List privs3r  = Grouper.access().has(s, "READ");
    List privs3u  = Grouper.access().has(s, "UPDATE");
    List privs3v  = Grouper.access().has(s, "VIEW");
    Assert.assertNotNull(privs3a);
    Assert.assertTrue( privs3a.size() == 8 );
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

  // TODO g3 || g4
  // TODO All of the above
  // TODO Do GrouperSystem for below as well?
  // TODO Grouper.access().has(s, g0, m0);
  // TODO Grouper.access().has(s, m0, "ADMIN");
  // TODO boolean Grouper.access().has(s, g0, m0, "ADMIN");
  // TODO Grant
  // TODO Revoke

}

