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

/*
 * $Id: TestAccessPrivs.java,v 1.34 2005-03-19 23:57:52 blair Exp $
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
    List privs = s.access().has(s, ns0);
    Assert.assertTrue("privs == 0", privs.size() == 0);
    // Because we are connected as root, everything will return true
    Assert.assertTrue(
      "has ADMIN",  s.access().has(s, ns0, Grouper.PRIV_ADMIN)
    );
    Assert.assertTrue(
      "has OPTIN",  s.access().has(s, ns0, Grouper.PRIV_OPTIN)
    );
    Assert.assertTrue(
      "has OPTOUT",  s.access().has(s, ns0, Grouper.PRIV_OPTOUT)
    );
    Assert.assertTrue(
      "has READ",  s.access().has(s, ns0, Grouper.PRIV_READ)
    );
    Assert.assertTrue(
      "has UPDATE", s.access().has(s, ns0, Grouper.PRIV_UPDATE)
    );
    Assert.assertTrue(
      "has VIEW",  s.access().has(s, ns0, Grouper.PRIV_VIEW)
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
    // Create g
    GrouperGroup g0  = GrouperGroup.create(
                         s, Constants.g0s, Constants.g0e
                       );

    // Assert current privs
    List privs = s.access().has(s, g0);
    Assert.assertTrue("privs == 1", privs.size() == 1);
    // Because we are connected as root, everything will return true
    Assert.assertTrue(
      "has ADMIN",  s.access().has(s, g0, Grouper.PRIV_ADMIN)
    );
    Assert.assertTrue(
      "has OPTIN",  s.access().has(s, g0, Grouper.PRIV_OPTIN)
    );
    Assert.assertTrue(
      "has OPTOUT",  s.access().has(s, g0, Grouper.PRIV_OPTOUT)
    );
    Assert.assertTrue(
      "has READ",  s.access().has(s, g0, Grouper.PRIV_READ)
    );
    Assert.assertTrue(
      "has UPDATE", s.access().has(s, g0, Grouper.PRIV_UPDATE)
    );
    Assert.assertTrue(
      "has VIEW",  s.access().has(s, g0, Grouper.PRIV_VIEW)
    );

    // We're done
    s.stop();
  }

/*
  // m0 !add g0 as member of g4
  public void testAddVal0() {
    Subject subj  = GrouperSubject.load(Constants.mem0I, Constants.mem0T);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // Fetch g
    GrouperGroup g = GrouperGroup.load(s, Constants.stem4, Constants.extn4);
    Assert.assertNotNull(g);
    GrouperGroup g1 = GrouperGroup.load(s, Constants.stem0, Constants.extn0);
    Assert.assertNotNull(g1);
    // Fetch m
    GrouperMember m = GrouperMember.load(s, g1.id(), "group");
    Assert.assertNotNull(m);
    // Act
    Assert.assertFalse( g.listAddVal(m) );
    // We're done
    s.stop();
  }

  // m0 !add attribute to g4
  public void testAddVal1() {
    Subject subj  = GrouperSubject.load(Constants.mem0I, Constants.mem0T);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // Fetch g
    GrouperGroup g = GrouperGroup.load(s, Constants.stem4, Constants.extn4);
    Assert.assertNotNull(g);
    // Act
    Assert.assertFalse( g.attribute("description", "new desc") );
    // We're done
    s.stop();
  }

  // m0 !remove attribute to g4
  public void testDelVal0() {
    Subject subj  = GrouperSubject.load(Constants.mem0I, Constants.mem0T);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // Fetch g
    GrouperGroup g = GrouperGroup.load(s, Constants.stem4, Constants.extn4);
    Assert.assertNotNull(g);
    // Act
    Assert.assertFalse( g.attribute("description", null) );
    // We're done
    s.stop();
  }

  // m0 !remove m1 as member of g4
  public void testDelVal1() {
    Subject subj  = GrouperSubject.load(Constants.mem0I, Constants.mem0T);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // Fetch g
    GrouperGroup g = GrouperGroup.load(s, Constants.stem0, Constants.extn0);
    Assert.assertNotNull(g);
    // Fetch g1 as m1
    GrouperGroup g1 = GrouperGroup.load(s, Constants.stem1, Constants.extn1);
    Assert.assertNotNull(g1);
    GrouperMember m = GrouperMember.load(s, g1.id(), "group");
    Assert.assertNotNull(m);
    // Act
    Assert.assertFalse( g.listDelVal(m) );
    // We're done
    s.stop();
  }

  // m0 !delete g4
  public void testDel0() {
    Subject subj  = GrouperSubject.load(Constants.mem0I, Constants.mem0T);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // Fetch g
    GrouperGroup g = GrouperGroup.load(s, Constants.stem4, Constants.extn4);
    Assert.assertNotNull(g);
    // Act
    Assert.assertFalse( GrouperGroup.delete(s, g) );
    // We're done
    s.stop();
  }

  // grant UPDATE to m0
  public void testPrep1() {
    Subject subj = GrouperSubject.load(Constants.rootI, Constants.rootT);
    Assert.assertNotNull(subj);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    GrouperGroup g = GrouperGroup.load(s, Constants.stem4, Constants.extn4);
    Assert.assertNotNull(g);
    GrouperMember m = GrouperMember.load(s, Constants.mem0I, Constants.mem0T);
    Assert.assertNotNull(m);
    Assert.assertTrue( s.access().grant(s, g, m, Grouper.PRIV_UPDATE) );
    s.stop();
  }

  // m0 add g0 as member of g4
  public void testAddVal2() {
    Subject subj  = GrouperSubject.load(Constants.mem0I, Constants.mem0T);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // Fetch g
    GrouperGroup g = GrouperGroup.load(s, Constants.stem4, Constants.extn4);
    Assert.assertNotNull(g);
    GrouperGroup g1 = GrouperGroup.load(s, Constants.stem0, Constants.extn0);
    Assert.assertNotNull(g1);
    // Fetch m
    GrouperMember m = GrouperMember.load(s, g1.id(), "group");
    Assert.assertNotNull(m);
    // Act
    Assert.assertTrue( g.listAddVal(m) );
    // We're done
    s.stop();
  }

  // m0 add attribute to g4
  public void testAddVal3() {
    Subject subj  = GrouperSubject.load(Constants.mem0I, Constants.mem0T);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // Fetch g
    GrouperGroup g = GrouperGroup.load(s, Constants.stem4, Constants.extn4);
    Assert.assertNotNull(g);
    // Act
    Assert.assertFalse( g.attribute("description", "new desc") );
    // We're done
    s.stop();
  }

  // grant ADMIN to m0
  public void testPrep2() {
    Subject subj = GrouperSubject.load(Constants.rootI, Constants.rootT);
    Assert.assertNotNull(subj);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);
    GrouperGroup g = GrouperGroup.load(s, Constants.stem4, Constants.extn4);
    Assert.assertNotNull(g);
    GrouperMember m = GrouperMember.load(s, Constants.mem0I, Constants.mem0T);
    Assert.assertNotNull(m);
    Assert.assertTrue( s.access().grant(s, g, m, Grouper.PRIV_ADMIN) );
    Assert.assertTrue( s.access().has(s, g, m, Grouper.PRIV_ADMIN) );
    s.stop();
  }

  // create a "description" for m0 to then delete
  public void testPrep2_0() {
    Subject subj  = GrouperSubject.load(Constants.rootI, Constants.rootT);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);
    // Fetch g
    GrouperGroup g = GrouperGroup.load(s, Constants.stem4, Constants.extn4);
    Assert.assertNotNull(g);
    // Act
    Assert.assertTrue( g.attribute("description", "new desc") );
    // We're done
    s.stop();
  }

  // m0 remove attribute to g4
  public void testDelVal2() {
    Subject subj  = GrouperSubject.load(Constants.mem0I, Constants.mem0T);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);
    // Fetch g
    GrouperGroup g = GrouperGroup.load(s, Constants.stem4, Constants.extn4);
    Assert.assertNotNull(g);
    // Act
    Assert.assertTrue( g.attribute("description", null) );
    // We're done
    s.stop();
  }

  // m0 remove g0 as member of g4
  public void testDelVal4() {
    Subject subj  = GrouperSubject.load(Constants.mem0I, Constants.mem0T);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // Fetch g
    GrouperGroup g = GrouperGroup.load(s, Constants.stem4, Constants.extn4);
    Assert.assertNotNull(g);
    GrouperGroup g1 = GrouperGroup.load(s, Constants.stem0, Constants.extn0);
    Assert.assertNotNull(g1);
    // Fetch m
    GrouperMember m = GrouperMember.load(s, g1.id(), "group");
    Assert.assertNotNull(m);
    // Act
    Assert.assertTrue( g.listDelVal(m) );
    // We're done
    s.stop();
  }

  // revoke ADMIN from m0
  public void testPrep4() {
    Subject subj = GrouperSubject.load(Constants.rootI, Constants.rootT);
    Assert.assertNotNull(subj);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    GrouperGroup g = GrouperGroup.load(s, Constants.stem4, Constants.extn4);
    Assert.assertNotNull(g);
    GrouperMember m = GrouperMember.load(s, Constants.mem0I, Constants.mem0T);
    Assert.assertNotNull(m);
    Assert.assertTrue( s.access().revoke(s, g, m, Grouper.PRIV_ADMIN) );
    s.stop();
  }

  // revoke UPDATE from m0
  public void testPrep5() {
    Subject subj = GrouperSubject.load(Constants.rootI, Constants.rootT);
    Assert.assertNotNull(subj);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    GrouperGroup g = GrouperGroup.load(s, Constants.stem4, Constants.extn4);
    Assert.assertNotNull(g);
    GrouperMember m = GrouperMember.load(s, Constants.mem0I, Constants.mem0T);
    Assert.assertNotNull(m);
    Assert.assertTrue( s.access().revoke(s, g, m, Grouper.PRIV_UPDATE) );
    s.stop();
  }

  // m0 !remove m1 as member of g4
  public void testDelVal3() {
    Subject subj  = GrouperSubject.load(Constants.mem0I, Constants.mem0T);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // Fetch g
    GrouperGroup g = GrouperGroup.load(s, Constants.stem0, Constants.extn0);
    Assert.assertNotNull(g);
    // Fetch g1 as m1
    GrouperGroup g1 = GrouperGroup.load(s, Constants.stem1, Constants.extn1);
    Assert.assertNotNull(g1);
    GrouperMember m = GrouperMember.load(s, g1.id(), "group");
    Assert.assertNotNull(m);
    // Act
    Assert.assertFalse( g.listDelVal(m) );
    // We're done
    s.stop();
  }

  // delete test group
  public void testPrep6() {
    Subject subj = GrouperSubject.load(Constants.rootI, Constants.rootT);
    Assert.assertNotNull(subj);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    GrouperGroup g = GrouperGroup.load(s, Constants.stem4, Constants.extn4);
    Assert.assertNotNull(g);
    GrouperMember m = GrouperMember.load(s, Constants.mem1I, Constants.mem1T);
    Assert.assertNotNull(m);
    Assert.assertTrue( g.listDelVal(m) );
    Assert.assertTrue( GrouperGroup.delete(s, g) );
    s.stop();
  }
*/

}

