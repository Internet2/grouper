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


public class TestQueries extends TestCase {

  private String extn0    = "extn.0";
  private String extn1    = "extn.1";
  private String extn2    = "extn.2";
  private String klass    = "edu.internet2.middleware.grouper.GrouperQuery";
  private String klassGL  = "edu.internet2.middleware.grouper.GrouperList";
  private String m0id     = "blair";
  private String m0type   = "person";
  private String m1id     = "notblair";
  private String m1type   = "person";
  private String stem0    = "stem.0";
  private String stem1    = "stem.1";
  private String stem2    = "stem.2";

  public TestQueries(String name) {
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
    Assert.assertTrue( g0.exists() );
    // g1
    GrouperGroup    g1  = GrouperGroup.load(s, stem1, extn1);
    Assert.assertNotNull(g1);
    Assert.assertTrue( g1.exists() );
    // g2
    GrouperGroup    g2  = GrouperGroup.load(s, stem2, extn2);
    Assert.assertNotNull(g2);
    Assert.assertTrue( g2.exists() );
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

/*
  public void testAddListData0() {
    GrouperSession  s       = new GrouperSession();
    Subject         subj    = GrouperSubject.lookup( Grouper.config("member.system"), "person" );
    s.start(subj);
    // Fetch g0
    GrouperGroup    g0    = GrouperGroup.load(s, stem0, extn0);
    Assert.assertNotNull(g0);
    Assert.assertTrue(g0.exists());
    // Fetch g2
    GrouperGroup    g2    = GrouperGroup.load(s, stem2, extn2);
    Assert.assertNotNull(g2);
    Assert.assertTrue(g2.exists());
    // Fetch Member 0
    GrouperMember   m0      = GrouperMember.lookup("blair", "person");
    Assert.assertNotNull(m0);
    // Fetch Member 1
    GrouperMember   m1      = GrouperMember.lookup("notblair", "person");
    Assert.assertNotNull(m1);
    // Add m0 to g0 "members"
    Assert.assertTrue( g0.listAddVal(s, m0, "members") );
    // Add m1 to g2 "members"
    Assert.assertTrue( g2.listAddVal(s, m1, "members") );
    // We're done
    s.stop();
  }
*/

/*
  public void testAddListData1() {
    GrouperSession  s       = new GrouperSession();
    Subject         subj    = GrouperSubject.lookup( Grouper.config("member.system"), "person" );
    s.start(subj);
    // Fetch g0
    GrouperGroup    g0    = GrouperGroup.load(s, stem0, extn0);
    Assert.assertNotNull(g0);
    Assert.assertTrue(g0.exists());
    // Fetch g1
    GrouperGroup    g1    = GrouperGroup.load(s, stem1, extn1);
    Assert.assertNotNull(g1);
    Assert.assertTrue(g1.exists());
    // Fetch g1 as m0
    GrouperMember   m0      = GrouperMember.lookup( g1.id(), "group");
    Assert.assertNotNull(m0);
    // Add m0 to g0 "members"
    Assert.assertTrue( g0.listAddVal(s, m0, "members") );
    // We're done
    s.stop();
  } 
*/

  public void testQueryInstantiate() {
    GrouperSession  s     = new GrouperSession();
    Subject         subj  = GrouperSubject.lookup( Grouper.config("member.system"), "person" );
    s.start(subj);
    GrouperQuery    q0    = new GrouperQuery(s);
    Assert.assertNotNull(q0);
    Assert.assertTrue(klass.equals( q0.getClass().getName() ) );
    GrouperQuery    q1    = new GrouperQuery(s);
    Assert.assertTrue(klass.equals( q1.getClass().getName() ) );
    s.stop();
  }

  public void testQuery0() {
    //
    // g0 (g2)  ()
    // g1 ()    ()
    // g2 ()    ()
    //
    GrouperSession  s     = new GrouperSession();
    Subject         subj  = GrouperSubject.lookup( Grouper.config("member.system"), "person" );
    s.start(subj);

    // Create query object
    GrouperQuery    q     = new GrouperQuery(s);

    // We want all members
    try {
      Assert.assertTrue( q.membership(null) );
      List vals = q.query();
      Assert.assertNotNull(vals);
      Assert.assertTrue( vals.size() == 1 );
      Object obj = vals.get(0);
      Assert.assertNotNull(obj);
      Assert.assertTrue( klassGL.equals( obj.getClass().getName() ) );
    } catch (GrouperException e) {
      Assert.fail("Exception: 'q.membership(null)'");
    }

    // We want effective members
    try {
      Assert.assertFalse( q.membership("effective") );
    } catch (GrouperException e) {
      Assert.fail("Exception: 'q.membership(null)'");
    }

    // We want immediate members
    try {
      Assert.assertTrue( q.membership("immediate") );
      List vals = q.query();
      Assert.assertNotNull(vals);
      Assert.assertTrue( vals.size() == 1 );
      Object obj = vals.get(0);
      Assert.assertNotNull(obj);
      Assert.assertTrue( klassGL.equals( obj.getClass().getName() ) );
    } catch (GrouperException e) {
      Assert.fail("Exception: 'q.membership(null)'");
    }

/*
    // Fetch g0 "admins"
    List            admin0  = g0.listVals(s, "admins");
    Assert.assertNotNull(admin0);
    Assert.assertTrue(admin0.size() == 1);
    List            admin0e = g0.listEffVals(s, "admins");
    Assert.assertNotNull(admin0e);
    Assert.assertTrue(admin0e.size() == 0);
    List            admin0i = g0.listImmVals(s, "admins");
    Assert.assertNotNull(admin0i);
    Assert.assertTrue(admin0i.size() == 1);
    // Fetch g1 "admins"
    List            admin1  = g1.listVals(s, "admins");
    Assert.assertNotNull(admin1);
    Assert.assertTrue(admin1.size() == 1);
    List            admin1e = g1.listEffVals(s, "admins");
    Assert.assertNotNull(admin1e);
    Assert.assertTrue(admin1e.size() == 0);
    List            admin1i = g1.listImmVals(s, "admins");
    Assert.assertNotNull(admin1i);
    Assert.assertTrue(admin1i.size() == 1);
    // Fetch g2 "admins"
    List            admin2  = g2.listVals(s, "admins");
    Assert.assertNotNull(admin2);
    Assert.assertTrue(admin2.size() == 1);
    List            admin2e = g2.listEffVals(s, "admins");
    Assert.assertNotNull(admin2e);
    Assert.assertTrue(admin2e.size() == 0);
    List            admin2i = g2.listImmVals(s, "admins");
    Assert.assertNotNull(admin2i);
    Assert.assertTrue(admin2i.size() == 1);
    // Fetch g0 "members"
    List            mem0    = g0.listVals(s, "members");
    Assert.assertNotNull(mem0);
    Assert.assertTrue(mem0.size() == 1);
    List            mem0c   = g0.listVals(s);
    Assert.assertNotNull(mem0c);
    Assert.assertTrue(mem0c.size() == 1);
    List            mem0e   = g0.listEffVals(s, "members"); 
    Assert.assertNotNull(mem0e);
    Assert.assertTrue(mem0e.size() == 0); 
    List            mem0i   = g0.listImmVals(s); 
    Assert.assertNotNull(mem0i);
    Assert.assertTrue(mem0i.size() == 1); 
    // Fetch g1 "members"
    List            mem1    = g1.listVals(s, "members");
    Assert.assertNotNull(mem1);
    Assert.assertTrue(mem1.size() == 0);
    List            mem1c   = g1.listVals(s);
    Assert.assertNotNull(mem1c);
    Assert.assertTrue(mem1c.size() == 0);
    List            mem1e   = g1.listEffVals(s, "members"); 
    Assert.assertNotNull(mem1e);
    Assert.assertTrue(mem1e.size() == 0);
    List            mem1i   = g1.listImmVals(s); 
    Assert.assertNotNull(mem1i);
    Assert.assertTrue(mem1i.size() == 0);
    // Fetch g2 "members"
    List            mem2    = g2.listVals(s, "members");
    Assert.assertNotNull(mem2);
    Assert.assertTrue(mem2.size() == 0);
    List            mem2c   = g2.listVals(s);
    Assert.assertNotNull(mem2c);
    Assert.assertTrue(mem2c.size() == 0);
    List            mem2e   = g2.listEffVals(s, "members"); 
    Assert.assertNotNull(mem2e);
    Assert.assertTrue(mem2e.size() == 0);
    List            mem2i   = g2.listImmVals(s); 
    Assert.assertNotNull(mem2i);
    Assert.assertTrue(mem2i.size() == 0);
*/

    // TODO Assert details about the individual members

    // We're done
    s.stop(); 
  }

}

