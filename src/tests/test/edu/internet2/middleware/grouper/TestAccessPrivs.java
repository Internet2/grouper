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
 * $Id: TestAccessPrivs.java,v 1.19 2004-12-05 19:20:10 blair Exp $
 */

package test.edu.internet2.middleware.grouper;

import  edu.internet2.middleware.grouper.*;
import  edu.internet2.middleware.subject.*;
import  java.util.*;
import  junit.framework.*;


public class TestAccessPrivs extends TestCase {

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
    Subject         subj  = GrouperSubject.lookup(Util.rooti, Util.roott );
    Assert.assertNotNull(subj);
    s.start(subj);
    // Fetch the groups
    // g0
    GrouperGroup    g0  = GrouperGroup.load(s, Util.stem0, Util.extn0);
    Assert.assertNotNull(g0);
    Assert.assertTrue( Util.klassGG.equals( g0.getClass().getName() ) );
    Assert.assertNotNull( g0.type() );
    Assert.assertNotNull( g0.attribute("stem") );
    Assert.assertTrue( g0.attribute("stem").value().equals(Util.stem0) );
    Assert.assertNotNull( g0.attribute("extension") );
    Assert.assertTrue( g0.attribute("extension").value().equals(Util.extn0) );
    // g1
    GrouperGroup    g1  = GrouperGroup.load(s, Util.stem1, Util.extn1);
    Assert.assertNotNull(g1);
    Assert.assertTrue( Util.klassGG.equals( g1.getClass().getName() ) );
    Assert.assertNotNull( g1.type() );
    Assert.assertNotNull( g1.attribute("stem") );
    Assert.assertTrue( g1.attribute("stem").value().equals(Util.stem1) );
    Assert.assertNotNull( g1.attribute("extension") );
    Assert.assertTrue( g1.attribute("extension").value().equals(Util.extn1) );
    // g2
    GrouperGroup    g2  = GrouperGroup.load(s, Util.stem2, Util.extn2);
    Assert.assertNotNull(g2);
    Assert.assertTrue( Util.klassGG.equals( g2.getClass().getName() ) );
    Assert.assertNotNull( g2.type() );
    Assert.assertNotNull( g2.attribute("stem") );
    Assert.assertTrue( g2.attribute("stem").value().equals(Util.stem2) );
    Assert.assertNotNull( g2.attribute("extension") );
    Assert.assertTrue( g2.attribute("extension").value().equals(Util.extn2) );
    // Fetch the members
    // Fetch Member 0
    GrouperMember   m0      = GrouperMember.lookup(Util.m0i, Util.m0t);
    Assert.assertNotNull(m0);
    // Fetch Member 1
    GrouperMember   m1      = GrouperMember.lookup(Util.m1i, Util.m1t);
    Assert.assertNotNull(m1);
    // We're done
    s.stop();
  }

  public void testHasGrantHasRevokeHas() {
    GrouperSession  s     = new GrouperSession();
    Assert.assertNotNull(s);
    Subject         subj  = GrouperSubject.lookup(Util.rooti, Util.roott );
    Assert.assertNotNull(subj);
    s.start(subj);
    // Fetch g0
    GrouperGroup  g0 = GrouperGroup.load(s, Util.stem0, Util.extn0);
    // Fetch Member 0
    GrouperMember m0   = GrouperMember.lookup(Util.m0i, Util.m0t);

    // Assert what privs m0 has on g0
    List privs0 = Grouper.access().has(s, g0, m0);
    Assert.assertNotNull(privs0);
    Assert.assertTrue( privs0.size() == 0 );
    Assert.assertFalse( Grouper.access().has(s, g0, m0, Grouper.PRIV_ADMIN) );
    Assert.assertFalse( Grouper.access().has(s, g0, m0, Grouper.PRIV_OPTIN) );
    Assert.assertFalse( Grouper.access().has(s, g0, m0, Grouper.PRIV_OPTOUT) );
    Assert.assertFalse( Grouper.access().has(s, g0, m0, Grouper.PRIV_READ) );
    Assert.assertFalse( Grouper.access().has(s, g0, m0, Grouper.PRIV_UPDATE) );
    Assert.assertFalse( Grouper.access().has(s, g0, m0, Grouper.PRIV_VIEW) );

    // Take a broader view and see where m0 has each of the privs
    List privs0a  = Grouper.access().has(s, m0, Grouper.PRIV_ADMIN);
    List privs0oi = Grouper.access().has(s, m0, Grouper.PRIV_OPTIN);
    List privs0oo = Grouper.access().has(s, m0, Grouper.PRIV_OPTOUT);
    List privs0r  = Grouper.access().has(s, m0, Grouper.PRIV_READ);
    List privs0u  = Grouper.access().has(s, m0, Grouper.PRIV_UPDATE);
    List privs0v  = Grouper.access().has(s, m0, Grouper.PRIV_VIEW);
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
    List whoHas0a   = Grouper.access().whoHas(s, g0, Grouper.PRIV_ADMIN);
    List whoHas0oi  = Grouper.access().whoHas(s, g0, Grouper.PRIV_OPTIN);
    List whoHas0oo  = Grouper.access().whoHas(s, g0, Grouper.PRIV_OPTOUT);
    List whoHas0r   = Grouper.access().whoHas(s, g0, Grouper.PRIV_READ);
    List whoHas0u   = Grouper.access().whoHas(s, g0, Grouper.PRIV_UPDATE);
    List whoHas0v   = Grouper.access().whoHas(s, g0, Grouper.PRIV_VIEW);
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
    Assert.assertTrue( Grouper.access().grant(s, g0, m0, Grouper.PRIV_ADMIN) );
    Assert.assertTrue( Grouper.access().grant(s, g0, m0, Grouper.PRIV_OPTIN) );
    Assert.assertTrue( Grouper.access().grant(s, g0, m0, Grouper.PRIV_OPTOUT) );
    Assert.assertTrue( Grouper.access().grant(s, g0, m0, Grouper.PRIV_READ) );
    Assert.assertTrue( Grouper.access().grant(s, g0, m0, Grouper.PRIV_UPDATE) );
    Assert.assertTrue( Grouper.access().grant(s, g0, m0, Grouper.PRIV_VIEW) );

    // Assert what privs m0 has on g0
    List privs1 = Grouper.access().has(s, g0, m0);
    Assert.assertNotNull(privs1);
    Assert.assertTrue( privs1.size() == 6 );
    Assert.assertTrue( Grouper.access().has(s, g0, m0, Grouper.PRIV_ADMIN) );
    Assert.assertTrue( Grouper.access().has(s, g0, m0, Grouper.PRIV_OPTIN) );
    Assert.assertTrue( Grouper.access().has(s, g0, m0, Grouper.PRIV_OPTOUT) );
    Assert.assertTrue( Grouper.access().has(s, g0, m0, Grouper.PRIV_READ) );
    Assert.assertTrue( Grouper.access().has(s, g0, m0, Grouper.PRIV_UPDATE) );
    Assert.assertTrue( Grouper.access().has(s, g0, m0, Grouper.PRIV_VIEW) );

    // Take a broader view and see where m0 has each of the privs
    List privs1a  = Grouper.access().has(s, m0, Grouper.PRIV_ADMIN);
    List privs1oi = Grouper.access().has(s, m0, Grouper.PRIV_OPTIN);
    List privs1oo = Grouper.access().has(s, m0, Grouper.PRIV_OPTOUT);
    List privs1r  = Grouper.access().has(s, m0, Grouper.PRIV_READ);
    List privs1u  = Grouper.access().has(s, m0, Grouper.PRIV_UPDATE);
    List privs1v  = Grouper.access().has(s, m0, Grouper.PRIV_VIEW);
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
    List whoHas1a   = Grouper.access().whoHas(s, g0, Grouper.PRIV_ADMIN);
    List whoHas1oi  = Grouper.access().whoHas(s, g0, Grouper.PRIV_OPTIN);
    List whoHas1oo  = Grouper.access().whoHas(s, g0, Grouper.PRIV_OPTOUT);
    List whoHas1r   = Grouper.access().whoHas(s, g0, Grouper.PRIV_READ);
    List whoHas1u   = Grouper.access().whoHas(s, g0, Grouper.PRIV_UPDATE);
    List whoHas1v   = Grouper.access().whoHas(s, g0, Grouper.PRIV_VIEW);
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
    Assert.assertTrue( Grouper.access().revoke(s, g0, m0, Grouper.PRIV_ADMIN) );
    Assert.assertTrue( Grouper.access().revoke(s, g0, m0, Grouper.PRIV_OPTIN) );
    Assert.assertTrue( Grouper.access().revoke(s, g0, m0, Grouper.PRIV_OPTOUT) );
    Assert.assertTrue( Grouper.access().revoke(s, g0, m0, Grouper.PRIV_READ) );
    Assert.assertTrue( Grouper.access().revoke(s, g0, m0, Grouper.PRIV_UPDATE) );
    Assert.assertTrue( Grouper.access().revoke(s, g0, m0, Grouper.PRIV_VIEW) );

    // Assert what privs m0 has on g0
    List privs2 = Grouper.access().has(s, g0, m0);
    Assert.assertNotNull(privs2);
    Assert.assertTrue( privs2.size() == 0 );
    Assert.assertFalse( Grouper.access().has(s, g0, m0, Grouper.PRIV_ADMIN) );
    Assert.assertFalse( Grouper.access().has(s, g0, m0, Grouper.PRIV_OPTIN) );
    Assert.assertFalse( Grouper.access().has(s, g0, m0, Grouper.PRIV_OPTOUT) );
    Assert.assertFalse( Grouper.access().has(s, g0, m0, Grouper.PRIV_READ) );
    Assert.assertFalse( Grouper.access().has(s, g0, m0, Grouper.PRIV_UPDATE) );
    Assert.assertFalse( Grouper.access().has(s, g0, m0, Grouper.PRIV_VIEW) );

    // Take a broader view and see where m0 has each of the privs
    List privs2a  = Grouper.access().has(s, m0, Grouper.PRIV_ADMIN);
    List privs2oi = Grouper.access().has(s, m0, Grouper.PRIV_OPTIN);
    List privs2oo = Grouper.access().has(s, m0, Grouper.PRIV_OPTOUT);
    List privs2r  = Grouper.access().has(s, m0, Grouper.PRIV_READ);
    List privs2u  = Grouper.access().has(s, m0, Grouper.PRIV_UPDATE);
    List privs2v  = Grouper.access().has(s, m0, Grouper.PRIV_VIEW);
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
    List whoHas2a   = Grouper.access().whoHas(s, g0, Grouper.PRIV_ADMIN);
    List whoHas2oi  = Grouper.access().whoHas(s, g0, Grouper.PRIV_OPTIN);
    List whoHas2oo  = Grouper.access().whoHas(s, g0, Grouper.PRIV_OPTOUT);
    List whoHas2r   = Grouper.access().whoHas(s, g0, Grouper.PRIV_READ);
    List whoHas2u   = Grouper.access().whoHas(s, g0, Grouper.PRIV_UPDATE);
    List whoHas2v   = Grouper.access().whoHas(s, g0, Grouper.PRIV_VIEW);
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
    Subject         subj  = GrouperSubject.lookup(Util.rooti, Util.roott );
    Assert.assertNotNull(subj);
    s.start(subj);
    // Fetch the groups
    // g0
    GrouperGroup    g0  = GrouperGroup.load(s, Util.stem0, Util.extn0);
    Assert.assertNotNull(g0);
    // g1
    GrouperGroup    g1  = GrouperGroup.load(s, Util.stem1, Util.extn1);
    Assert.assertNotNull(g1);
    // g2
    GrouperGroup    g2  = GrouperGroup.load(s, Util.stem2, Util.extn2);
    Assert.assertNotNull(g2);
    // Fetch the members
    // Fetch Member 0
    GrouperMember   m0      = GrouperMember.lookup(Util.m0i, Util.m0t);
    Assert.assertNotNull(m0);
    // Fetch Member 1
    GrouperMember   m1      = GrouperMember.lookup(Util.m1i, Util.m1t);
    Assert.assertNotNull(m1);

    // What privs does the current subject have on g0?
    List privs0 = Grouper.access().has(s, g0);
    Assert.assertNotNull(privs0);
    Assert.assertTrue( privs0.size() == 1 );
    Assert.assertTrue( Grouper.access().has(s, g0, Grouper.PRIV_ADMIN) );
    Assert.assertTrue( Grouper.access().has(s, g0, Grouper.PRIV_OPTIN) );
    Assert.assertTrue( Grouper.access().has(s, g0, Grouper.PRIV_OPTOUT) );
    Assert.assertTrue( Grouper.access().has(s, g0, Grouper.PRIV_READ) );
    Assert.assertTrue( Grouper.access().has(s, g0, Grouper.PRIV_UPDATE) );
    Assert.assertTrue( Grouper.access().has(s, g0, Grouper.PRIV_VIEW) );
   
    // What privs does the current subject have on g1?
    List privs1 = Grouper.access().has(s, g1);
    Assert.assertNotNull(privs1);
    Assert.assertTrue( privs1.size() == 1 );
    Assert.assertTrue( Grouper.access().has(s, g1, Grouper.PRIV_ADMIN) );
    Assert.assertTrue( Grouper.access().has(s, g1, Grouper.PRIV_OPTIN) );
    Assert.assertTrue( Grouper.access().has(s, g1, Grouper.PRIV_OPTOUT) );
    Assert.assertTrue( Grouper.access().has(s, g1, Grouper.PRIV_READ) );
    Assert.assertTrue( Grouper.access().has(s, g1, Grouper.PRIV_UPDATE) );
    Assert.assertTrue( Grouper.access().has(s, g1, Grouper.PRIV_VIEW) );
    
    // What privs does the current subject have on g2?
    List privs2 = Grouper.access().has(s, g2);
    Assert.assertNotNull(privs2);
    Assert.assertTrue( privs2.size() == 1 );
    Assert.assertTrue( Grouper.access().has(s, g2, Grouper.PRIV_ADMIN) );
    Assert.assertTrue( Grouper.access().has(s, g2, Grouper.PRIV_OPTIN) );
    Assert.assertTrue( Grouper.access().has(s, g2, Grouper.PRIV_OPTOUT) );
    Assert.assertTrue( Grouper.access().has(s, g2, Grouper.PRIV_READ) );
    Assert.assertTrue( Grouper.access().has(s, g2, Grouper.PRIV_UPDATE) );
    Assert.assertTrue( Grouper.access().has(s, g2, Grouper.PRIV_VIEW) );
   
    // Take a broader view and see where the current subject has each
    // of the privs
    List privs3a  = Grouper.access().has(s, Grouper.PRIV_ADMIN);
    List privs3oi = Grouper.access().has(s, Grouper.PRIV_OPTIN);
    List privs3oo = Grouper.access().has(s, Grouper.PRIV_OPTOUT);
    List privs3r  = Grouper.access().has(s, Grouper.PRIV_READ);
    List privs3u  = Grouper.access().has(s, Grouper.PRIV_UPDATE);
    List privs3v  = Grouper.access().has(s, Grouper.PRIV_VIEW);
    Assert.assertNotNull(privs3a);
    Assert.assertTrue( privs3a.size() == 4 );
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

  // m0 !add m1 as member of g0
  public void testAddVal0() {
    GrouperSession  s = new GrouperSession();
    Assert.assertNotNull(s);
    Subject subj  = GrouperSubject.lookup(Util.m0i, Util.m0t);
    s.start(subj);
    // Fetch g
    GrouperGroup g = GrouperGroup.load(s, Util.stem0, Util.extn0);
    // Fetch m
    GrouperMember m = GrouperMember.lookup(Util.m1i, Util.m1t);
    // Act
    Assert.assertFalse( g.listAddVal(s, m) );
    // We're done
    s.stop();
  }

  // m0 !remove g2 as member of g0
  public void testDelVal0() {
    GrouperSession  s = new GrouperSession();
    Assert.assertNotNull(s);
    Subject subj  = GrouperSubject.lookup(Util.m0i, Util.m0t);
    s.start(subj);
    // Fetch g
    GrouperGroup g = GrouperGroup.load(s, Util.stem0, Util.extn0);
    Assert.assertNotNull(g);
    // Fetch g1 as m1
    GrouperGroup g1 = GrouperGroup.load(s, Util.stem1, Util.extn1);
    Assert.assertNotNull(g1);
    GrouperMember m = GrouperMember.lookup( g1.id(), "group");
    Assert.assertNotNull(m);
    // Act
    Assert.assertFalse( g.listDelVal(s, m) );
    // We're done
    s.stop();
  }

}

