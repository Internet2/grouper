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


public class TestGroupLists extends TestCase {

  private String stem0  = "stem.0";
  private String stem1  = "stem.1";
  private String stem2  = "stem.2";
  private String extn0  = "extn.0";
  private String extn1  = "extn.1";
  private String extn2  = "extn.2";


  public TestGroupLists(String name) {
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

  public void testAddListData0() {
    Subject         subj    = GrouperSubject.load( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // Fetch Group 0
    GrouperGroup    g0    = GrouperGroup.load(s, stem0, extn0);
    Assert.assertNotNull(g0);
    // Fetch Group 2
    GrouperGroup    g2    = GrouperGroup.load(s, stem2, extn2);
    Assert.assertNotNull(g2);
    // Fetch Member 0
    GrouperMember   m0      = GrouperMember.load("blair", Grouper.DEF_SUBJ_TYPE);
    Assert.assertNotNull(m0);
    // Fetch Member 1
    GrouperMember   m1      = GrouperMember.load("notblair", Grouper.DEF_SUBJ_TYPE);
    Assert.assertNotNull(m1);
    // Add m0 to g0 Grouper.DEF_LIST_TYPE
    Assert.assertTrue( g0.listAddVal(s, m0, Grouper.DEF_LIST_TYPE) );
    // Do not silently fail when adding a list value that already exists
    Assert.assertFalse( g0.listAddVal(s, m0, Grouper.DEF_LIST_TYPE) );
    // Add m1 to g2 Grouper.DEF_LIST_TYPE
    Assert.assertTrue( g2.listAddVal(s, m1, Grouper.DEF_LIST_TYPE) );
    // We're done
    s.stop();
  }

  public void testAddListData0_1() {
    Subject subj = GrouperSubject.load( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);
    // Fetch Group 0
    GrouperGroup g0 = GrouperGroup.load(s, stem0, extn0);
    Assert.assertNotNull(g0);
    // Fetch Member 0
    GrouperMember m0 = GrouperMember.load("blair", Grouper.DEF_SUBJ_TYPE);
    Assert.assertNotNull(m0);
    // Do not silently fail when adding a list value that already exists
    Assert.assertFalse( g0.listAddVal(s, m0) );
    // We're done
    s.stop();
  }

  public void testFetchListData0() {
    //
    //  g0: m0  / 
    //  g1:     /
    //  g2: m1  /
    //
    Subject         subj    = GrouperSubject.load( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // Fetch Group 0
    GrouperGroup    g0    = GrouperGroup.load(s, stem0, extn0);
    Assert.assertNotNull(g0);
    // Fetch Group 1
    GrouperGroup    g1    = GrouperGroup.load(s, stem1, extn1);
    Assert.assertNotNull(g1);
    // Fetch Group 2
    GrouperGroup    g2    = GrouperGroup.load(s, stem2, extn2);
    Assert.assertNotNull(g2);
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
    // Fetch g0 Grouper.DEF_LIST_TYPE
    List            mem0    = g0.listVals(s, Grouper.DEF_LIST_TYPE);
    Assert.assertNotNull(mem0);
    Assert.assertTrue(mem0.size() == 1);
    List            mem0c   = g0.listVals(s);
    Assert.assertNotNull(mem0c);
    Assert.assertTrue(mem0c.size() == 1);
    List            mem0e   = g0.listEffVals(s, Grouper.DEF_LIST_TYPE); 
    Assert.assertNotNull(mem0e);
    Assert.assertTrue(mem0e.size() == 0);
    List            mem0i   = g0.listImmVals(s); 
    Assert.assertNotNull(mem0i);
    Assert.assertTrue(mem0i.size() == 1);
    // Fetch g1 Grouper.DEF_LIST_TYPE
    List            mem1    = g1.listVals(s, Grouper.DEF_LIST_TYPE);
    Assert.assertNotNull(mem1);
    Assert.assertTrue(mem1.size() == 0);
    List            mem1c   = g1.listVals(s);
    Assert.assertNotNull(mem1c);
    Assert.assertTrue(mem1c.size() == 0);
    List            mem1e   = g1.listEffVals(s, Grouper.DEF_LIST_TYPE); 
    Assert.assertNotNull(mem1e);
    Assert.assertTrue(mem1e.size() == 0);
    List            mem1i   = g1.listImmVals(s); 
    Assert.assertNotNull(mem1i);
    Assert.assertTrue(mem1i.size() == 0);
    // Fetch g2 Grouper.DEF_LIST_TYPE
    List            mem2    = g2.listVals(s, Grouper.DEF_LIST_TYPE);
    Assert.assertNotNull(mem2);
    Assert.assertTrue(mem2.size() == 1);
    List            mem2c   = g2.listVals(s);
    Assert.assertNotNull(mem2c);
    Assert.assertTrue(mem2c.size() == 1);
    List            mem2e   = g2.listEffVals(s, Grouper.DEF_LIST_TYPE); 
    Assert.assertNotNull(mem2e);
    Assert.assertTrue(mem2e.size() == 0);
    List            mem2i   = g2.listImmVals(s); 
    Assert.assertNotNull(mem2i);
    Assert.assertTrue(mem2i.size() == 1);

    // TODO Assert details about the individual members

    // We're done
    s.stop(); 
  }

  public void testFetchInvalidListData0() {
    Subject         subj    = GrouperSubject.load( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // Fetch Group 0
    GrouperGroup    g0    = GrouperGroup.load(s, stem0, extn0);
    Assert.assertNotNull(g0);
    // Fetch Group 1
    GrouperGroup    g1    = GrouperGroup.load(s, stem1, extn1);
    Assert.assertNotNull(g1);
    // Fetch Group 2
    GrouperGroup    g2    = GrouperGroup.load(s, stem2, extn2);
    Assert.assertNotNull(g2);
    // Fetch g0 "invalid admins"
    List            admin0  = g0.listVals(s, "invalid admins");
    Assert.assertNotNull(admin0);
    Assert.assertTrue(admin0.size() == 0);
    // Fetch g1 "invalid admins"
    List            admin1  = g1.listVals(s, "invalid admins");
    Assert.assertNotNull(admin1);
    Assert.assertTrue(admin1.size() == 0);
    // Fetch g2 "invalid admins"
    List            admin2  = g2.listVals(s, "invalid admins");
    Assert.assertNotNull(admin2);
    Assert.assertTrue(admin2.size() == 0);
    // Fetch g0 "invalid members"
    List            mem0    = g0.listVals(s, "invalid members");
    Assert.assertNotNull(mem0);
    Assert.assertTrue(mem0.size() == 0);
    // Fetch g1 "invalid members"
    List            mem1    = g1.listVals(s, "invalid members");
    Assert.assertNotNull(mem1);
    Assert.assertTrue(mem1.size() == 0);
    // Fetch g2 "invalid members"
    List            mem2    = g2.listVals(s, "invalid members");
    Assert.assertNotNull(mem2);
    Assert.assertTrue(mem2.size() == 0);

    // We're done
    s.stop(); 
  }

  public void testAddInvalidListData0() {
    Subject         subj    = GrouperSubject.load( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // Fetch Group 0
    GrouperGroup    g0    = GrouperGroup.load(s, stem0, extn0);
    Assert.assertNotNull(g0);
    // Fetch Group 2
    GrouperGroup    g2    = GrouperGroup.load(s, stem2, extn2);
    Assert.assertNotNull(g2);
    // Fetch Member 0
    GrouperMember   m0      = GrouperMember.load("blair", Grouper.DEF_SUBJ_TYPE);
    Assert.assertNotNull(m0);
    // Fetch Member 1
    GrouperMember   m1      = GrouperMember.load("notblair", Grouper.DEF_SUBJ_TYPE);
    Assert.assertNotNull(m1);
    // Add m0 to g0 Grouper.DEF_LIST_TYPE
   // FIXME  (#269) Assert.assertFalse( g0.listAddVal(s, m0, "invalid members") );
    // Add m1 to g2 Grouper.DEF_LIST_TYPE
    // FIXME (#269) Assert.assertFalse( g2.listAddVal(s, m1, "invalid members") );
    // We're done
    s.stop();
  }

  public void testAddListData1() {
    Subject         subj    = GrouperSubject.load( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // Fetch Group 0
    GrouperGroup    g0    = GrouperGroup.load(s, stem0, extn0);
    Assert.assertNotNull(g0);
    // Fetch Group 1
    GrouperGroup    g1    = GrouperGroup.load(s, stem1, extn1);
    Assert.assertNotNull(g1);
    // Fetch g1 as m0
    GrouperMember   m0      = GrouperMember.load( g1.id(), "group");
    Assert.assertNotNull(m0);
    // Add m0 to g0 Grouper.DEF_LIST_TYPE
    Assert.assertTrue( g0.listAddVal(s, m0) );
    // We're done
    s.stop();
  } 

  public void testFetchListData1() {
    //
    //  g0: m0, g1  / 
    //  g1:         /
    //  g2: m1      /
    //
    Subject         subj    = GrouperSubject.load( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // Fetch Group 0
    GrouperGroup    g0    = GrouperGroup.load(s, stem0, extn0);
    Assert.assertNotNull(g0);
    // Fetch Group 1
    GrouperGroup    g1    = GrouperGroup.load(s, stem1, extn1);
    Assert.assertNotNull(g1);
    // Fetch Group 2
    GrouperGroup    g2    = GrouperGroup.load(s, stem2, extn2);
    Assert.assertNotNull(g2);
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
    // Fetch g0 Grouper.DEF_LIST_TYPE
    List            mem0    = g0.listVals(s, Grouper.DEF_LIST_TYPE);
    Assert.assertNotNull(mem0);
    Assert.assertTrue(mem0.size() == 2);
    List            mem0c   = g0.listVals(s);
    Assert.assertNotNull(mem0c);
    Assert.assertTrue(mem0c.size() == 2);
    List            mem0e   = g0.listEffVals(s, Grouper.DEF_LIST_TYPE); 
    Assert.assertNotNull(mem0e);
    Assert.assertTrue(mem0e.size() == 0);
    List            mem0i   = g0.listImmVals(s); 
    Assert.assertNotNull(mem0i);
    Assert.assertTrue(mem0i.size() == 2);
    // Fetch g1 Grouper.DEF_LIST_TYPE
    List            mem1    = g1.listVals(s, Grouper.DEF_LIST_TYPE);
    Assert.assertNotNull(mem1);
    Assert.assertTrue(mem1.size() == 0);
    List            mem1c   = g1.listVals(s);
    Assert.assertNotNull(mem1c);
    Assert.assertTrue(mem1c.size() == 0);
    List            mem1e   = g1.listEffVals(s, Grouper.DEF_LIST_TYPE); 
    Assert.assertNotNull(mem1e);
    Assert.assertTrue(mem1e.size() == 0);
    List            mem1i   = g1.listImmVals(s); 
    Assert.assertNotNull(mem1i);
    Assert.assertTrue(mem1i.size() == 0);
    // Fetch g2 Grouper.DEF_LIST_TYPE
    List            mem2    = g2.listVals(s, Grouper.DEF_LIST_TYPE);
    Assert.assertNotNull(mem2);
    Assert.assertTrue(mem2.size() == 1);
    List            mem2c   = g2.listVals(s);
    Assert.assertNotNull(mem2c);
    Assert.assertTrue(mem2c.size() == 1);
    List            mem2e   = g2.listEffVals(s, Grouper.DEF_LIST_TYPE); 
    Assert.assertNotNull(mem2e);
    Assert.assertTrue(mem2e.size() == 0);
    List            mem2i   = g2.listImmVals(s); 
    Assert.assertNotNull(mem2i);
    Assert.assertTrue(mem2i.size() == 1);

    // TODO Assert details about the individual members

    // We're done
    s.stop(); 
  }

  public void testAddListData2() {
    Subject         subj    = GrouperSubject.load( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // Fetch Group 0
    GrouperGroup    g0    = GrouperGroup.load(s, stem0, extn0);
    Assert.assertNotNull(g0);
    // Fetch Group 2
    GrouperGroup    g2    = GrouperGroup.load(s, stem2, extn2);
    Assert.assertNotNull(g2);
    // Fetch g2 as m0
    GrouperMember   m0      = GrouperMember.load( g2.id(), "group");
    Assert.assertNotNull(m0);
    // Add m0 to g0 Grouper.DEF_LIST_TYPE
    Assert.assertTrue( g0.listAddVal(s, m0) );
    // We're done
    s.stop();
  }

  public void testFetchListData2() {
    //
    //  g0: m0, g1, g2  / m1^g2
    //  g1:             /
    //  g2: m1          /
    //
    Subject         subj    = GrouperSubject.load( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    GrouperSession s = GrouperSession.start(subj);
    GrouperGroup    g0    = GrouperGroup.load(s, stem0, extn0);
    GrouperGroup    g1    = GrouperGroup.load(s, stem1, extn1);
    GrouperGroup    g2    = GrouperGroup.load(s, stem2, extn2);
    // Fetch g0 "admins"
    List            admin0  = g0.listVals(s, "admins");
    Assert.assertNotNull("g0 admin !null", admin0);
    Assert.assertTrue("g0 admin == 1", admin0.size() == 1);
    List            admin0e = g0.listEffVals(s, "admins");
    Assert.assertNotNull("g0 eff admin != null", admin0e);
    Assert.assertTrue("g0 eff admin == 0", admin0e.size() == 0);
    List            admin0i = g0.listImmVals(s, "admins");
    Assert.assertNotNull("g0 imm admin != null", admin0i);
    Assert.assertTrue("g0 imm admin = 1", admin0i.size() == 1);
    // Fetch g1 "admins"
    List            admin1  = g1.listVals(s, "admins");
    Assert.assertNotNull("g1 admin !null", admin1);
    Assert.assertTrue("g1 admin == 1", admin1.size() == 1);
    List            admin1e = g1.listEffVals(s, "admins");
    Assert.assertNotNull("g1 eff admin !null", admin1e);
    Assert.assertTrue("g1 eff admin == 0", admin1e.size() == 0);
    List            admin1i = g1.listImmVals(s, "admins");
    Assert.assertNotNull("g1 imm admin !null", admin1i);
    Assert.assertTrue("g1 imm admin == 1", admin1i.size() == 1);
    // Fetch g2 "admins"
    List            admin2  = g2.listVals(s, "admins");
    Assert.assertNotNull("g2 admin !null", admin2);
    Assert.assertTrue("g2 admin == 1", admin2.size() == 1);
    List            admin2e = g2.listEffVals(s, "admins");
    Assert.assertNotNull("g2 eff admin !null", admin2e);
    Assert.assertTrue("g2 eff admin == 0", admin2e.size() == 0);
    List            admin2i = g2.listImmVals(s, "admins");
    Assert.assertNotNull("g2 imm admin !null", admin2i);
    Assert.assertTrue("g2 imm admin == 1", admin2i.size() == 1);
    // Fetch g0 Grouper.DEF_LIST_TYPE
    List            mem0    = g0.listVals(s, Grouper.DEF_LIST_TYPE);
    Assert.assertNotNull("g0 mship !null", mem0);
    Assert.assertTrue("g0 mship == 4", mem0.size() == 4);
    List            mem0c   = g0.listVals(s);
    Assert.assertNotNull("g0 mship !null no type", mem0c);
    Assert.assertTrue("g0 mship == 4 no type", mem0c.size() == 4);
    List            mem0e   = g0.listEffVals(s, Grouper.DEF_LIST_TYPE); 
    Assert.assertNotNull("g0 eff mship !null", mem0e);
    Assert.assertTrue("g0 eff mship == 1", mem0e.size() == 1); 
    List            mem0i   = g0.listImmVals(s); 
    Assert.assertNotNull("g0 imm mship !null no type", mem0i);
    Assert.assertTrue("g0 imm mship == 3 no type", mem0i.size() == 3); 
    // Fetch g1 Grouper.DEF_LIST_TYPE
    List            mem1    = g1.listVals(s, Grouper.DEF_LIST_TYPE);
    Assert.assertNotNull("g1 mship !null", mem1);
    Assert.assertTrue("g1 mship == 0", mem1.size() == 0);
    List            mem1c   = g1.listVals(s);
    Assert.assertNotNull("g1 mship !null no type", mem1c);
    Assert.assertTrue("g1 mship == 0 no type", mem1c.size() == 0);
    List            mem1e   = g1.listEffVals(s, Grouper.DEF_LIST_TYPE); 
    Assert.assertNotNull("g1 eff mship !null", mem1e);
    Assert.assertTrue("g1 eff mship == 0", mem1e.size() == 0);
    List            mem1i   = g1.listImmVals(s); 
    Assert.assertNotNull("g1 imm mship !null no type", mem1i);
    Assert.assertTrue("g1 imm mship == 0 no type", mem1i.size() == 0);
    // Fetch g2 Grouper.DEF_LIST_TYPE
    List            mem2    = g2.listVals(s, Grouper.DEF_LIST_TYPE);
    Assert.assertNotNull("g2 mship !null", mem2);
    Assert.assertTrue("g2 mship == 1", mem2.size() == 1);
    List            mem2c   = g2.listVals(s);
    Assert.assertNotNull("g2 mship !null no type", mem2c);
    Assert.assertTrue("g2 mship == 1 no type", mem2c.size() == 1);
    List            mem2e   = g2.listEffVals(s, Grouper.DEF_LIST_TYPE); 
    Assert.assertNotNull("g2 eff mship !null", mem2e);
    Assert.assertTrue("g2 eff mship == 0", mem2e.size() == 0);
    List            mem2i   = g2.listImmVals(s); 
    Assert.assertNotNull("g2 imm mship !null no type", mem2i);
    Assert.assertTrue("g2 imm mship == 1 no type", mem2i.size() == 1);
    // We're done
    s.stop(); 
  }

  public void testRemoveListData0() {
    Subject         subj    = GrouperSubject.load( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // Fetch Group 0
    GrouperGroup    g0    = GrouperGroup.load(s, stem0, extn0);
    Assert.assertNotNull(g0);
    // Fetch Group 2
    GrouperGroup    g2    = GrouperGroup.load(s, stem2, extn2);
    Assert.assertNotNull(g2);
    // Fetch Member 0
    GrouperMember   m0      = GrouperMember.load("blair", Grouper.DEF_SUBJ_TYPE);
    Assert.assertNotNull(m0);
    // Fetch Member 1
    GrouperMember   m1      = GrouperMember.load("notblair", Grouper.DEF_SUBJ_TYPE);
    Assert.assertNotNull(m1);
    // Remove m0 from g0 Grouper.DEF_LIST_TYPE
    Assert.assertTrue( g0.listDelVal(s, m0) );
    // We're done
    s.stop();
  }

  public void testFetchListData3() {
    //
    //  g0: g1, g2  / m1^g2
    //  g1:         /
    //  g2: m1      /
    //
    Subject         subj    = GrouperSubject.load( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // Fetch Group 0
    GrouperGroup    g0    = GrouperGroup.load(s, stem0, extn0);
    Assert.assertNotNull(g0);
    // Fetch Group 1
    GrouperGroup    g1    = GrouperGroup.load(s, stem1, extn1);
    Assert.assertNotNull(g1);
    // Fetch Group 2
    GrouperGroup    g2    = GrouperGroup.load(s, stem2, extn2);
    Assert.assertNotNull(g2);
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
    // Fetch g0 Grouper.DEF_LIST_TYPE
    List            mem0    = g0.listVals(s, Grouper.DEF_LIST_TYPE);
    Assert.assertNotNull(mem0);
    Assert.assertTrue(mem0.size() == 3);
    List            mem0c   = g0.listVals(s);
    Assert.assertNotNull(mem0c);
    Assert.assertTrue(mem0c.size() == 3);
    List            mem0e   = g0.listEffVals(s, Grouper.DEF_LIST_TYPE); 
    Assert.assertNotNull(mem0e);
    Assert.assertTrue(mem0e.size() == 1); 
    List            mem0i   = g0.listImmVals(s); 
    Assert.assertNotNull(mem0i);
    Assert.assertTrue(mem0i.size() == 2); 
    // Fetch g1 Grouper.DEF_LIST_TYPE
    List            mem1    = g1.listVals(s, Grouper.DEF_LIST_TYPE);
    Assert.assertNotNull(mem1);
    Assert.assertTrue(mem1.size() == 0);
    List            mem1c   = g1.listVals(s);
    Assert.assertNotNull(mem1c);
    Assert.assertTrue(mem1c.size() == 0);
    List            mem1e   = g1.listEffVals(s, Grouper.DEF_LIST_TYPE); 
    Assert.assertNotNull(mem1e);
    Assert.assertTrue(mem1e.size() == 0);
    List            mem1i   = g1.listImmVals(s); 
    Assert.assertNotNull(mem1i);
    Assert.assertTrue(mem1i.size() == 0);
    // Fetch g2 Grouper.DEF_LIST_TYPE
    List            mem2    = g2.listVals(s, Grouper.DEF_LIST_TYPE);
    Assert.assertNotNull(mem2);
    Assert.assertTrue(mem2.size() == 1);
    List            mem2c   = g2.listVals(s);
    Assert.assertNotNull(mem2c);
    Assert.assertTrue(mem2c.size() == 1);
    List            mem2e   = g2.listEffVals(s, Grouper.DEF_LIST_TYPE); 
    Assert.assertNotNull(mem2e);
    Assert.assertTrue(mem2e.size() == 0);
    List            mem2i   = g2.listImmVals(s); 
    Assert.assertNotNull(mem2i);
    Assert.assertTrue(mem2i.size() == 1);

    // TODO Assert details about the individual members

    // We're done
    s.stop(); 
  }

  public void testRemoveListData2() {
    Subject         subj    = GrouperSubject.load( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // Fetch Group 0
    GrouperGroup    g0    = GrouperGroup.load(s, stem0, extn0);
    Assert.assertNotNull(g0);
    // Fetch Group 1
    GrouperGroup    g1    = GrouperGroup.load(s, stem1, extn1);
    Assert.assertNotNull(g1);
    // Fetch g1 as m1
    GrouperMember   m1      = GrouperMember.load( g1.id(), "group");
    Assert.assertNotNull(m1);
    // Remove m1 (g1) from g0 Grouper.DEF_LIST_TYPE
    Assert.assertTrue( g0.listDelVal(s, m1) );
    // We're done
    s.stop();
  }

  public void testFetchListData4() {
    //
    //  g0: g2  / m1^g2
    //  g1:     /
    //  g2: m1  /
    //
    Subject         subj    = GrouperSubject.load( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // Fetch Group 0
    GrouperGroup    g0    = GrouperGroup.load(s, stem0, extn0);
    Assert.assertNotNull(g0);
    // Fetch Group 1
    GrouperGroup    g1    = GrouperGroup.load(s, stem1, extn1);
    Assert.assertNotNull(g1);
    // Fetch Group 2
    GrouperGroup    g2    = GrouperGroup.load(s, stem2, extn2);
    Assert.assertNotNull(g2);
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
    // Fetch g0 Grouper.DEF_LIST_TYPE
    List            mem0    = g0.listVals(s, Grouper.DEF_LIST_TYPE);
    Assert.assertNotNull(mem0);
    Assert.assertTrue(mem0.size() == 2);
    List            mem0c   = g0.listVals(s);
    Assert.assertNotNull(mem0c);
    Assert.assertTrue(mem0c.size() == 2);
    List            mem0e   = g0.listEffVals(s, Grouper.DEF_LIST_TYPE); 
    Assert.assertNotNull(mem0e);
    Assert.assertTrue(mem0e.size() == 1); 
    List            mem0i   = g0.listImmVals(s); 
    Assert.assertNotNull(mem0i);
    Assert.assertTrue(mem0i.size() == 1); 
    // Fetch g1 Grouper.DEF_LIST_TYPE
    List            mem1    = g1.listVals(s, Grouper.DEF_LIST_TYPE);
    Assert.assertNotNull(mem1);
    Assert.assertTrue(mem1.size() == 0);
    List            mem1c   = g1.listVals(s);
    Assert.assertNotNull(mem1c);
    Assert.assertTrue(mem1c.size() == 0);
    List            mem1e   = g1.listEffVals(s, Grouper.DEF_LIST_TYPE); 
    Assert.assertNotNull(mem1e);
    Assert.assertTrue(mem1e.size() == 0);
    List            mem1i   = g1.listImmVals(s); 
    Assert.assertNotNull(mem1i);
    Assert.assertTrue(mem1i.size() == 0);
    // Fetch g2 Grouper.DEF_LIST_TYPE
    List            mem2    = g2.listVals(s, Grouper.DEF_LIST_TYPE);
    Assert.assertNotNull(mem2);
    Assert.assertTrue(mem2.size() == 1);
    List            mem2c   = g2.listVals(s);
    Assert.assertNotNull(mem2c);
    Assert.assertTrue(mem2c.size() == 1);
    List            mem2e   = g2.listEffVals(s, Grouper.DEF_LIST_TYPE); 
    Assert.assertNotNull(mem2e);
    Assert.assertTrue(mem2e.size() == 0);
    List            mem2i   = g2.listImmVals(s); 
    Assert.assertNotNull(mem2i);
    Assert.assertTrue(mem2i.size() == 1);

    // TODO Assert details about the individual members

    // We're done
    s.stop(); 
  }

  public void testRemoveListData3() {
    Subject         subj    = GrouperSubject.load( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // Fetch Group 0
    GrouperGroup    g0    = GrouperGroup.load(s, stem0, extn0);
    Assert.assertNotNull(g0);
    // Fetch Group 1
    GrouperGroup    g2    = GrouperGroup.load(s, stem2, extn2);
    Assert.assertNotNull(g2);
    // Fetch g2 as m2
    GrouperMember   m2      = GrouperMember.load( g2.id(), "group");
    Assert.assertNotNull(m2);
    // Remove m2 (g2) from g0 Grouper.DEF_LIST_TYPE
    Assert.assertTrue( g0.listDelVal(s, m2) );
    // We're done
    s.stop();
  }

  public void testFetchListData5() {
    //
    //  g0:     /
    //  g1:     /
    //  g2: m1  /
    //
    Subject         subj    = GrouperSubject.load( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // Fetch Group 0
    GrouperGroup    g0    = GrouperGroup.load(s, stem0, extn0);
    Assert.assertNotNull(g0);
    // Fetch Group 1
    GrouperGroup    g1    = GrouperGroup.load(s, stem1, extn1);
    Assert.assertNotNull(g1);
    // Fetch Group 2
    GrouperGroup    g2    = GrouperGroup.load(s, stem2, extn2);
    Assert.assertNotNull(g2);
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
    // Fetch g0 Grouper.DEF_LIST_TYPE
    List            mem0    = g0.listVals(s, Grouper.DEF_LIST_TYPE);
    Assert.assertNotNull(mem0);
    Assert.assertTrue(mem0.size() == 0);
    List            mem0c   = g0.listVals(s);
    Assert.assertNotNull(mem0c);
    Assert.assertTrue(mem0c.size() == 0);
    List            mem0e   = g0.listEffVals(s, Grouper.DEF_LIST_TYPE); 
    Assert.assertNotNull(mem0e);
    Assert.assertTrue(mem0e.size() == 0); 
    List            mem0i   = g0.listImmVals(s); 
    Assert.assertNotNull(mem0i);
    Assert.assertTrue(mem0i.size() == 0); 
    // Fetch g1 Grouper.DEF_LIST_TYPE
    List            mem1    = g1.listVals(s, Grouper.DEF_LIST_TYPE);
    Assert.assertNotNull(mem1);
    Assert.assertTrue(mem1.size() == 0);
    List            mem1c   = g1.listVals(s);
    Assert.assertNotNull(mem1c);
    Assert.assertTrue(mem1c.size() == 0);
    List            mem1e   = g1.listEffVals(s, Grouper.DEF_LIST_TYPE); 
    Assert.assertNotNull(mem1e);
    Assert.assertTrue(mem1e.size() == 0);
    List            mem1i   = g1.listImmVals(s); 
    Assert.assertNotNull(mem1i);
    Assert.assertTrue(mem1i.size() == 0);
    // Fetch g2 Grouper.DEF_LIST_TYPE
    List            mem2    = g2.listVals(s, Grouper.DEF_LIST_TYPE);
    Assert.assertNotNull(mem2);
    Assert.assertTrue(mem2.size() == 1);
    List            mem2c   = g2.listVals(s);
    Assert.assertNotNull(mem2c);
    Assert.assertTrue(mem2c.size() == 1);
    List            mem2e   = g2.listEffVals(s, Grouper.DEF_LIST_TYPE); 
    Assert.assertNotNull(mem2e);
    Assert.assertTrue(mem2e.size() == 0);
    List            mem2i   = g2.listImmVals(s); 
    Assert.assertNotNull(mem2i);
    Assert.assertTrue(mem2i.size() == 1);

    // TODO Assert details about the individual members

    // We're done
    s.stop(); 
  }

  public void testAddListData3() {
    Subject         subj    = GrouperSubject.load( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // Fetch g0
    GrouperGroup    g0    = GrouperGroup.load(s, stem0, extn0);
    Assert.assertNotNull(g0);
    // Fetch g2
    GrouperGroup    g2    = GrouperGroup.load(s, stem2, extn2);
    Assert.assertNotNull(g2);
    // Fetch g2 as m2
    GrouperMember   m2      = GrouperMember.load( g2.id(), "group");
    Assert.assertNotNull(m2);
    // Add m2/g2 to g0
    Assert.assertTrue( g0.listAddVal(s, m2) );
    // We're done
    s.stop();
  }

  public void testFetchListData6() {
    //
    //  g0: g2  / m1^g2
    //  g1:     /
    //  g2: m1  /
    //
    Subject         subj    = GrouperSubject.load( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // Fetch Group 0
    GrouperGroup    g0    = GrouperGroup.load(s, stem0, extn0);
    Assert.assertNotNull(g0);
    // Fetch Group 1
    GrouperGroup    g1    = GrouperGroup.load(s, stem1, extn1);
    Assert.assertNotNull(g1);
    // Fetch Group 2
    GrouperGroup    g2    = GrouperGroup.load(s, stem2, extn2);
    Assert.assertNotNull(g2);
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
    // Fetch g0 Grouper.DEF_LIST_TYPE
    List            mem0    = g0.listVals(s, Grouper.DEF_LIST_TYPE);
    Assert.assertNotNull(mem0);
    Assert.assertTrue(mem0.size() == 2);
    List            mem0c   = g0.listVals(s);
    Assert.assertNotNull(mem0c);
    Assert.assertTrue(mem0c.size() == 2);
    List            mem0e   = g0.listEffVals(s, Grouper.DEF_LIST_TYPE); 
    Assert.assertNotNull(mem0e);
    Assert.assertTrue(mem0e.size() == 1); 
    List            mem0i   = g0.listImmVals(s); 
    Assert.assertNotNull(mem0i);
    Assert.assertTrue(mem0i.size() == 1); 
    // Fetch g1 Grouper.DEF_LIST_TYPE
    List            mem1    = g1.listVals(s, Grouper.DEF_LIST_TYPE);
    Assert.assertNotNull(mem1);
    Assert.assertTrue(mem1.size() == 0);
    List            mem1c   = g1.listVals(s);
    Assert.assertNotNull(mem1c);
    Assert.assertTrue(mem1c.size() == 0);
    List            mem1e   = g1.listEffVals(s, Grouper.DEF_LIST_TYPE); 
    Assert.assertNotNull(mem1e);
    Assert.assertTrue(mem1e.size() == 0);
    List            mem1i   = g1.listImmVals(s); 
    Assert.assertNotNull(mem1i);
    Assert.assertTrue(mem1i.size() == 0);
    // Fetch g2 Grouper.DEF_LIST_TYPE
    List            mem2    = g2.listVals(s, Grouper.DEF_LIST_TYPE);
    Assert.assertNotNull(mem2);
    Assert.assertTrue(mem2.size() == 1);
    List            mem2c   = g2.listVals(s);
    Assert.assertNotNull(mem2c);
    Assert.assertTrue(mem2c.size() == 1);
    List            mem2e   = g2.listEffVals(s, Grouper.DEF_LIST_TYPE); 
    Assert.assertNotNull(mem2e);
    Assert.assertTrue(mem2e.size() == 0);
    List            mem2i   = g2.listImmVals(s); 
    Assert.assertNotNull(mem2i);
    Assert.assertTrue(mem2i.size() == 1);

    // TODO Assert details about the individual members

    // We're done
    s.stop(); 
  }

  public void testRemoveListData4() {
    Subject         subj    = GrouperSubject.load( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // Fetch g2
    GrouperGroup    g2    = GrouperGroup.load(s, stem2, extn2);
    Assert.assertNotNull(g2);
    // Fetch m1
    GrouperMember   m1      = GrouperMember.load("notblair", Grouper.DEF_SUBJ_TYPE);
    Assert.assertNotNull(m1);
    // Fetch g2 as m2
    GrouperMember   m2      = GrouperMember.load( g2.id(), "group");
    Assert.assertNotNull(m2);
    // Remove m1 from g2 Grouper.DEF_LIST_TYPE
    Assert.assertTrue( g2.listDelVal(s, m1) );
    // We're done
    s.stop();
  }

  public void testFetchListData7() {
    //
    //  g0: g2  / 
    //  g1:     /
    //  g2:     /
    //
    Subject         subj    = GrouperSubject.load( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    GrouperSession s = GrouperSession.start(subj);
    // Fetch groups
    GrouperGroup    g0    = GrouperGroup.load(s, stem0, extn0);
    GrouperGroup    g1    = GrouperGroup.load(s, stem1, extn1);
    GrouperGroup    g2    = GrouperGroup.load(s, stem2, extn2);
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
    // Fetch g0 Grouper.DEF_LIST_TYPE
    List            mem0    = g0.listVals(s, Grouper.DEF_LIST_TYPE);
    Assert.assertNotNull("g0 mship !null", mem0);
    Assert.assertTrue("g0 mship == 1", mem0.size() == 1);
    List            mem0e   = g0.listEffVals(s, Grouper.DEF_LIST_TYPE); 
    Assert.assertNotNull("g0 eff mship !null", mem0e);
    Assert.assertTrue("g0 eff mship == 0", mem0e.size() == 0); 
    List            mem0i   = g0.listImmVals(s); 
    Assert.assertNotNull("g0 imm mship !null", mem0i);
    Assert.assertTrue("g0 imm mship !null", mem0i.size() == 1); 
    // Fetch g1 Grouper.DEF_LIST_TYPE
    List            mem1    = g1.listVals(s, Grouper.DEF_LIST_TYPE);
    Assert.assertNotNull("g1 mship !null", mem1);
    Assert.assertTrue("g1 mship == 0", mem1.size() == 0);
    List            mem1e   = g1.listEffVals(s, Grouper.DEF_LIST_TYPE); 
    Assert.assertNotNull("g1 eff mship !null", mem1e);
    Assert.assertTrue("g1 eff mship == 0", mem1e.size() == 0);
    List            mem1i   = g1.listImmVals(s); 
    Assert.assertNotNull("g1 imm mship !null", mem1i);
    Assert.assertTrue("g1 imm mship == 0", mem1i.size() == 0);
    // Fetch g2 Grouper.DEF_LIST_TYPE
    List            mem2    = g2.listVals(s, Grouper.DEF_LIST_TYPE);
    Assert.assertNotNull("g2 mship !null", mem2);
    Assert.assertTrue("g2 mship == 0", mem2.size() == 0);
    List            mem2e   = g2.listEffVals(s, Grouper.DEF_LIST_TYPE); 
    Assert.assertNotNull("g2 eff mship !null", mem2e);
    Assert.assertTrue("g2 eff mship == 0", mem2e.size() == 0);
    List            mem2i   = g2.listImmVals(s); 
    Assert.assertNotNull("g2 imm mship !null", mem2i);
    Assert.assertTrue("g2 imm mship == 0", mem2i.size() == 0);

    // TODO Assert details about the individual members

    // We're done
    s.stop(); 
  }

  public void testPrep0() {
    Subject         subj    = GrouperSubject.load( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // Fetch g0
    GrouperGroup g0 = GrouperGroup.load(s, stem0, extn0);
    Assert.assertNotNull(g0);
    // Fetch g1
    GrouperGroup g1 = GrouperGroup.load(s, stem1, extn1);
    Assert.assertNotNull(g1);
    // Fetch m1
    GrouperMember m0 = GrouperMember.load("blair", Grouper.DEF_SUBJ_TYPE);
    Assert.assertNotNull(m0);
    // Fetch g1 as m1
    GrouperMember m1 = GrouperMember.load( g1.id(), "group");
    Assert.assertNotNull(m1);
    // Add m0 to g1
    Assert.assertTrue( g1.listAddVal(s, m0) );
    // Add g1 to g0
    Assert.assertTrue( g0.listAddVal(s, m1) );
    // We're done
    s.stop();
  }

  public void testFetchLV0() {
    //
    // g0 (g1, g2)  (m0)
    // g1 (m0)      ()
    // g2 ()        ()
    //
    Subject         subj    = GrouperSubject.load( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // Fetch Group 0
    GrouperGroup    g0    = GrouperGroup.load(s, stem0, extn0);
    Assert.assertNotNull(g0);
    // Fetch Group 1
    GrouperGroup    g1    = GrouperGroup.load(s, stem1, extn1);
    Assert.assertNotNull(g1);
    // Fetch Group 2
    GrouperGroup    g2    = GrouperGroup.load(s, stem2, extn2);
    Assert.assertNotNull(g2);
    // Fetch g0 Grouper.DEF_LIST_TYPE
    List            mem0    = g0.listVals(s, Grouper.DEF_LIST_TYPE);
    Assert.assertNotNull(mem0);
    Assert.assertTrue(mem0.size() == 3);
    List            mem0c   = g0.listVals(s);
    Assert.assertNotNull(mem0c);
    Assert.assertTrue(mem0c.size() == 3);
    List            mem0e   = g0.listEffVals(s, Grouper.DEF_LIST_TYPE); 
    Assert.assertNotNull(mem0e);
    Assert.assertTrue(mem0e.size() == 1); 
    List            mem0i   = g0.listImmVals(s); 
    Assert.assertNotNull(mem0i);
    Assert.assertTrue(mem0i.size() == 2); 
    // Fetch g1 Grouper.DEF_LIST_TYPE
    List            mem1    = g1.listVals(s, Grouper.DEF_LIST_TYPE);
    Assert.assertNotNull(mem1);
    Assert.assertTrue(mem1.size() == 1);
    List            mem1c   = g1.listVals(s);
    Assert.assertNotNull(mem1c);
    Assert.assertTrue(mem1c.size() == 1);
    List            mem1e   = g1.listEffVals(s, Grouper.DEF_LIST_TYPE); 
    Assert.assertNotNull(mem1e);
    Assert.assertTrue(mem1e.size() == 0);
    List            mem1i   = g1.listImmVals(s); 
    Assert.assertNotNull(mem1i);
    Assert.assertTrue(mem1i.size() == 1);
    // Fetch g2 Grouper.DEF_LIST_TYPE
    List            mem2    = g2.listVals(s, Grouper.DEF_LIST_TYPE);
    Assert.assertNotNull(mem2);
    Assert.assertTrue(mem2.size() == 0);
    List            mem2c   = g2.listVals(s);
    Assert.assertNotNull(mem2c);
    Assert.assertTrue(mem2c.size() == 0);
    List            mem2e   = g2.listEffVals(s, Grouper.DEF_LIST_TYPE); 
    Assert.assertNotNull(mem2e);
    Assert.assertTrue(mem2e.size() == 0);
    List            mem2i   = g2.listImmVals(s); 
    Assert.assertNotNull(mem2i);
    Assert.assertTrue(mem2i.size() == 0);

    // We're done
    s.stop(); 
  }

  public void testPrep1() {
    Subject         subj    = GrouperSubject.load( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // Fetch g1
    GrouperGroup g1 = GrouperGroup.load(s, stem1, extn1);
    Assert.assertNotNull(g1);
    // Fail to delete g1 become it has a member and is a member
    Assert.assertFalse(GrouperGroup.delete(s, g1) );
    // We're done
    s.stop();
  }

  public void testPrep2() {
    Subject         subj    = GrouperSubject.load( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // Fetch g0
    GrouperGroup g0 = GrouperGroup.load(s, stem0, extn0);
    Assert.assertNotNull(g0);
    // Fetch g1
    GrouperGroup g1 = GrouperGroup.load(s, stem1, extn1);
    Assert.assertNotNull(g1);
    // Fetch g1 as m1
    GrouperMember m1 = GrouperMember.load( g1.id(), "group");
    Assert.assertNotNull(m1);
    // Remove g1 from g0
    Assert.assertTrue( g0.listDelVal(s, m1) );
    // We're done
    s.stop();
  }

  public void testGroupDel0() {
    Subject subj = GrouperSubject.load(
                     Grouper.config("member.system"), 
                     Grouper.DEF_SUBJ_TYPE
                   );
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull("sess !null", s);
    // Fetch g1
    GrouperGroup g1 = GrouperGroup.load(s, stem1, extn1);
    Assert.assertNotNull("loaded g1 !null", g1);
    // Fail to delete g1 as it still has a member 
    Assert.assertFalse("fail to delete g1", GrouperGroup.delete(s, g1) );
    // We're done
    s.stop();
  }

  public void testPrep3() {
    Subject         subj    = GrouperSubject.load( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // Fetch g1
    GrouperGroup g1 = GrouperGroup.load(s, stem1, extn1);
    Assert.assertNotNull(g1);
    // Fetch m0
    GrouperMember m0 = GrouperMember.load("blair", Grouper.DEF_SUBJ_TYPE);
    Assert.assertNotNull(m0);
    // Remove m0 from g1
    Assert.assertTrue( g1.listDelVal(s, m0) );
    // We're done
    s.stop();
  }

  public void testGroupDel1() {
    Subject         subj    = GrouperSubject.load( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // Fetch g1
    GrouperGroup g1 = GrouperGroup.load(s, stem1, extn1);
    Assert.assertNotNull(g1);
    // Delete g1 
    Assert.assertTrue(GrouperGroup.delete(s, g1) );
    // We're done
    s.stop();
  }

  public void testFetchLV1() {
    //
    // g0 (g2)  ()
    // g2 ()    ()
    //
    Subject         subj    = GrouperSubject.load( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // Fetch Group 0
    GrouperGroup    g0    = GrouperGroup.load(s, stem0, extn0);
    Assert.assertNotNull(g0);
    // Fetch Group 1
    GrouperGroup    g1    = GrouperGroup.load(s, stem1, extn1);
    Assert.assertNull(g1);
    // Fetch Group 2
    GrouperGroup    g2    = GrouperGroup.load(s, stem2, extn2);
    Assert.assertNotNull(g2);
    // Fetch g0 Grouper.DEF_LIST_TYPE
    List            mem0    = g0.listVals(s, Grouper.DEF_LIST_TYPE);
    Assert.assertNotNull(mem0);
    Assert.assertTrue(mem0.size() == 1);
    List            mem0c   = g0.listVals(s);
    Assert.assertNotNull(mem0c);
    Assert.assertTrue(mem0c.size() == 1);
    List            mem0e   = g0.listEffVals(s, Grouper.DEF_LIST_TYPE); 
    Assert.assertNotNull(mem0e);
    Assert.assertTrue(mem0e.size() == 0); 
    List            mem0i   = g0.listImmVals(s); 
    Assert.assertNotNull(mem0i);
    Assert.assertTrue(mem0i.size() == 1); 
    // Fetch g2 Grouper.DEF_LIST_TYPE
    List            mem2    = g2.listVals(s, Grouper.DEF_LIST_TYPE);
    Assert.assertNotNull(mem2);
    Assert.assertTrue(mem2.size() == 0);
    List            mem2c   = g2.listVals(s);
    Assert.assertNotNull(mem2c);
    Assert.assertTrue(mem2c.size() == 0);
    List            mem2e   = g2.listEffVals(s, Grouper.DEF_LIST_TYPE); 
    Assert.assertNotNull(mem2e);
    Assert.assertTrue(mem2e.size() == 0);
    List            mem2i   = g2.listImmVals(s); 
    Assert.assertNotNull(mem2i);
    Assert.assertTrue(mem2i.size() == 0);
    // We're done
    s.stop(); 
  }

  public void testCreateG0() {
    Subject         subj  = GrouperSubject.load( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // Create g1
    String stem = "stem.1";
    String extn = "extn.1";
    GrouperGroup g = GrouperGroup.create(s, stem, extn);
    Assert.assertNotNull(g);
    s.stop();
  }

  public void testFetchLV2() {
    //
    // g0 (g2)  ()
    // g1 ()    ()
    // g2 ()    ()
    //
    Subject         subj    = GrouperSubject.load( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // Fetch Group 0
    GrouperGroup    g0    = GrouperGroup.load(s, stem0, extn0);
    Assert.assertNotNull(g0);
    // Fetch Group 1
    GrouperGroup    g1    = GrouperGroup.load(s, stem1, extn1);
    Assert.assertNotNull(g1);
    // Fetch Group 2
    GrouperGroup    g2    = GrouperGroup.load(s, stem2, extn2);
    Assert.assertNotNull(g2);
    // Fetch g0 Grouper.DEF_LIST_TYPE
    List            mem0    = g0.listVals(s, Grouper.DEF_LIST_TYPE);
    Assert.assertNotNull(mem0);
    Assert.assertTrue(mem0.size() == 1);
    List            mem0c   = g0.listVals(s);
    Assert.assertNotNull(mem0c);
    Assert.assertTrue(mem0c.size() == 1);
    List            mem0e   = g0.listEffVals(s, Grouper.DEF_LIST_TYPE); 
    Assert.assertNotNull(mem0e);
    Assert.assertTrue(mem0e.size() == 0); 
    List            mem0i   = g0.listImmVals(s); 
    Assert.assertNotNull(mem0i);
    Assert.assertTrue(mem0i.size() == 1); 
    // Fetch g1 Grouper.DEF_LIST_TYPE
    List            mem1    = g1.listVals(s, Grouper.DEF_LIST_TYPE);
    Assert.assertNotNull(mem1);
    Assert.assertTrue(mem1.size() == 0);
    List            mem1c   = g1.listVals(s);
    Assert.assertNotNull(mem1c);
    Assert.assertTrue(mem1c.size() == 0);
    List            mem1e   = g1.listEffVals(s, Grouper.DEF_LIST_TYPE); 
    Assert.assertNotNull(mem1e);
    Assert.assertTrue(mem1e.size() == 0);
    List            mem1i   = g1.listImmVals(s); 
    Assert.assertNotNull(mem1i);
    Assert.assertTrue(mem1i.size() == 0);
    // Fetch g2 Grouper.DEF_LIST_TYPE
    List            mem2    = g2.listVals(s, Grouper.DEF_LIST_TYPE);
    Assert.assertNotNull(mem2);
    Assert.assertTrue(mem2.size() == 0);
    List            mem2c   = g2.listVals(s);
    Assert.assertNotNull(mem2c);
    Assert.assertTrue(mem2c.size() == 0);
    List            mem2e   = g2.listEffVals(s, Grouper.DEF_LIST_TYPE); 
    Assert.assertNotNull(mem2e);
    Assert.assertTrue(mem2e.size() == 0);
    List            mem2i   = g2.listImmVals(s); 
    Assert.assertNotNull(mem2i);
    Assert.assertTrue(mem2i.size() == 0);
    // We're done
    s.stop(); 
  }

  public void testPrep4() {
    Subject subj  = GrouperSubject.load(Util.rooti, Util.roott);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);
    GrouperGroup g7 = GrouperGroup.create(s, Util.stem7, Util.extn7);
    GrouperGroup g8 = GrouperGroup.create(s, Util.stem8, Util.extn8);
    GrouperGroup g9 = GrouperGroup.create(s, Util.stem9, Util.extn9);
    Assert.assertNotNull(g7);
    Assert.assertNotNull(g8);
    Assert.assertNotNull(g9);
    GrouperMember m0 = GrouperMember.load(Util.m0i, Util.m0t);
    GrouperMember m1 = GrouperMember.load(Util.m1i, Util.m1t);
    GrouperMember m2 = GrouperMember.load(Util.rooti, Util.roott);
    Assert.assertNotNull(m0);
    Assert.assertNotNull(m1);
    Assert.assertNotNull(m2);
    Assert.assertTrue( g7.listAddVal(s, m0) );
    Assert.assertTrue( g8.listAddVal(s, m1) );
    Assert.assertTrue( g9.listAddVal(s, m2) );
    s.stop();
  }

  public void testFetchLV4_0() {
    //
    // g7 (m0)  ()
    // g8 (m1)    ()
    // g9 (m2)    ()
    //
    Subject subj    = GrouperSubject.load(Util.rooti, Util.roott);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);
    GrouperGroup g7 = GrouperGroup.load(s, Util.stem7, Util.extn7);
    GrouperGroup g8 = GrouperGroup.load(s, Util.stem8, Util.extn8);
    GrouperGroup g9 = GrouperGroup.load(s, Util.stem9, Util.extn9);
    Assert.assertNotNull(g7);
    Assert.assertNotNull(g8);
    Assert.assertNotNull(g9);
    List mem7   = g7.listVals(s);
    List mem7e  = g7.listEffVals(s);
    List mem7i  = g7.listImmVals(s);
    List mem8   = g8.listVals(s);
    List mem8e  = g8.listEffVals(s);
    List mem8i  = g8.listImmVals(s);
    List mem9   = g9.listVals(s);
    List mem9e  = g9.listEffVals(s);
    List mem9i  = g9.listImmVals(s);
    Assert.assertTrue(mem7.size()   == 1);
    Assert.assertTrue(mem7e.size()  == 0);
    Assert.assertTrue(mem7i.size()  == 1);
    Assert.assertTrue(mem8.size()   == 1);
    Assert.assertTrue(mem8e.size()  == 0);
    Assert.assertTrue(mem8i.size()  == 1);
    Assert.assertTrue(mem9.size()   == 1);
    Assert.assertTrue(mem9e.size()  == 0);
    Assert.assertTrue(mem9i.size()  == 1);
    // We're done
    s.stop(); 
  }

  public void testAdd4_0() {
    Subject subj  = GrouperSubject.load(Util.rooti, Util.roott);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);
    GrouperGroup g7 = GrouperGroup.load(s, Util.stem7, Util.extn7);
    GrouperGroup g8 = GrouperGroup.load(s, Util.stem8, Util.extn8);
    GrouperGroup g9 = GrouperGroup.load(s, Util.stem9, Util.extn9);
    GrouperMember g7m = GrouperMember.load(g7.id(), "group");
    GrouperMember g8m = GrouperMember.load(g8.id(), "group");
    GrouperMember g9m = GrouperMember.load(g9.id(), "group");
    Assert.assertNotNull(g7m);
    Assert.assertNotNull(g8m);
    Assert.assertNotNull(g9m);
    Assert.assertTrue( g8.listAddVal(s, g7m) );
    s.stop();
  }

  public void testFetchLV4_1() {
    //
    // g7 (m0)      ()
    // g8 (m1, g7)  (m0)
    // g9 (m2)      ()
    //
    Subject subj    = GrouperSubject.load(Util.rooti, Util.roott);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);
    GrouperGroup g7 = GrouperGroup.load(s, Util.stem7, Util.extn7);
    GrouperGroup g8 = GrouperGroup.load(s, Util.stem8, Util.extn8);
    GrouperGroup g9 = GrouperGroup.load(s, Util.stem9, Util.extn9);
    Assert.assertNotNull(g7);
    Assert.assertNotNull(g8);
    Assert.assertNotNull(g9);
    List mem7   = g7.listVals(s);
    List mem7e  = g7.listEffVals(s);
    List mem7i  = g7.listImmVals(s);
    List mem8   = g8.listVals(s);
    List mem8e  = g8.listEffVals(s);
    List mem8i  = g8.listImmVals(s);
    List mem9   = g9.listVals(s);
    List mem9e  = g9.listEffVals(s);
    List mem9i  = g9.listImmVals(s);
    Assert.assertTrue(mem7.size()   == 1);
    Assert.assertTrue(mem7e.size()  == 0);
    Assert.assertTrue(mem7i.size()  == 1);
    Assert.assertTrue(mem8.size()   == 3); 
    Assert.assertTrue(mem8e.size()  == 1); 
    Assert.assertTrue(mem8i.size()  == 2);
    Assert.assertTrue(mem9.size()   == 1);
    Assert.assertTrue(mem9e.size()  == 0);
    Assert.assertTrue(mem9i.size()  == 1);
    // We're done
    s.stop(); 
  }

  public void testAdd4_1() {
    Subject subj  = GrouperSubject.load(Util.rooti, Util.roott);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);
    GrouperGroup g7 = GrouperGroup.load(s, Util.stem7, Util.extn7);
    GrouperGroup g8 = GrouperGroup.load(s, Util.stem8, Util.extn8);
    GrouperGroup g9 = GrouperGroup.load(s, Util.stem9, Util.extn9);
    GrouperMember g7m = GrouperMember.load(g7.id(), "group");
    GrouperMember g8m = GrouperMember.load(g8.id(), "group");
    GrouperMember g9m = GrouperMember.load(g9.id(), "group");
    Assert.assertNotNull(g7m);
    Assert.assertNotNull(g8m);
    Assert.assertNotNull(g9m);
    Assert.assertTrue( g9.listAddVal(s, g8m) );
    s.stop();
  }

  public void testFetchLV4_2() {
    //
    // g7 (m0)      ()
    // g8 (m1, g7)  (m0)
    // g9 (m2, g8)  (m1, g7, m0)
    //
    Subject subj    = GrouperSubject.load(Util.rooti, Util.roott);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);
    GrouperGroup g7 = GrouperGroup.load(s, Util.stem7, Util.extn7);
    GrouperGroup g8 = GrouperGroup.load(s, Util.stem8, Util.extn8);
    GrouperGroup g9 = GrouperGroup.load(s, Util.stem9, Util.extn9);
    Assert.assertNotNull(g7);
    Assert.assertNotNull(g8);
    Assert.assertNotNull(g9);
    List mem7   = g7.listVals(s);
    List mem7e  = g7.listEffVals(s);
    List mem7i  = g7.listImmVals(s);
    List mem8   = g8.listVals(s);
    List mem8e  = g8.listEffVals(s);
    List mem8i  = g8.listImmVals(s);
    List mem9   = g9.listVals(s);
    List mem9e  = g9.listEffVals(s);
    List mem9i  = g9.listImmVals(s);
    Assert.assertTrue(mem7.size()   == 1);
    Assert.assertTrue(mem7e.size()  == 0);
    Assert.assertTrue(mem7i.size()  == 1);
    Assert.assertTrue(mem8.size()   == 3);
    Assert.assertTrue(mem8e.size()  == 1);
    Assert.assertTrue(mem8i.size()  == 2);
    Assert.assertTrue(mem9.size()   == 5); // 5
    Assert.assertTrue(mem9e.size()  == 3); // 3
    Assert.assertTrue(mem9i.size()  == 2);
    // We're done
    s.stop(); 
  }

  public void testDel4_0() {
    Subject subj  = GrouperSubject.load(Util.rooti, Util.roott);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);
    GrouperGroup g7 = GrouperGroup.load(s, Util.stem7, Util.extn7);
    GrouperGroup g8 = GrouperGroup.load(s, Util.stem8, Util.extn8);
    GrouperGroup g9 = GrouperGroup.load(s, Util.stem9, Util.extn9);
    GrouperMember g7m = GrouperMember.load(g7.id(), "group");
    GrouperMember g8m = GrouperMember.load(g8.id(), "group");
    GrouperMember g9m = GrouperMember.load(g9.id(), "group");
    Assert.assertNotNull(g7m);
    Assert.assertNotNull(g8m);
    Assert.assertNotNull(g9m);
    Assert.assertTrue( g8.listDelVal(s, g7m) );
    s.stop();
  }

  public void testFetchLV4_3() {
    //
    // g7 (m0)      ()
    // g8 (m1)      ()
    // g9 (m2, g8)  (m1)
    //
    Subject subj    = GrouperSubject.load(Util.rooti, Util.roott);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);
    GrouperGroup g7 = GrouperGroup.load(s, Util.stem7, Util.extn7);
    GrouperGroup g8 = GrouperGroup.load(s, Util.stem8, Util.extn8);
    GrouperGroup g9 = GrouperGroup.load(s, Util.stem9, Util.extn9);
    Assert.assertNotNull(g7);
    Assert.assertNotNull(g8);
    Assert.assertNotNull(g9);
    List mem7   = g7.listVals(s);
    List mem7e  = g7.listEffVals(s);
    List mem7i  = g7.listImmVals(s);
    List mem8   = g8.listVals(s);
    List mem8e  = g8.listEffVals(s);
    List mem8i  = g8.listImmVals(s);
    List mem9   = g9.listVals(s);
    List mem9e  = g9.listEffVals(s);
    List mem9i  = g9.listImmVals(s);
    Assert.assertTrue(mem7.size()   == 1);
    Assert.assertTrue(mem7e.size()  == 0);
    Assert.assertTrue(mem7i.size()  == 1);
    Assert.assertTrue(mem8.size()   == 1); 
    Assert.assertTrue(mem8e.size()  == 0); 
    Assert.assertTrue(mem8i.size()  == 1);
    Assert.assertTrue(mem9.size()   == 3); 
    Assert.assertTrue(mem9e.size()  == 1); 
    Assert.assertTrue(mem9i.size()  == 2);
    // We're done
    s.stop(); 
  }

  public void testDel4_1() {
    Subject subj  = GrouperSubject.load(Util.rooti, Util.roott);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);
    GrouperGroup g7 = GrouperGroup.load(s, Util.stem7, Util.extn7);
    GrouperGroup g8 = GrouperGroup.load(s, Util.stem8, Util.extn8);
    GrouperGroup g9 = GrouperGroup.load(s, Util.stem9, Util.extn9);
    GrouperMember g7m = GrouperMember.load(g7.id(), "group");
    GrouperMember g8m = GrouperMember.load(g8.id(), "group");
    GrouperMember g9m = GrouperMember.load(g9.id(), "group");
    Assert.assertNotNull(g7m);
    Assert.assertNotNull(g8m);
    Assert.assertNotNull(g9m);
    GrouperMember m1 = GrouperMember.load(Util.m1i, Util.m1t);
    Assert.assertNotNull(m1);
    Assert.assertTrue( g8.listDelVal(s, m1) );
    s.stop();
  }

  public void testFetchLV4_4() {
    //
    // g7 (m0)      ()
    // g8 ()        ()
    // g9 (m2, g8)  ()
    //
    Subject subj    = GrouperSubject.load(Util.rooti, Util.roott);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);
    GrouperGroup g7 = GrouperGroup.load(s, Util.stem7, Util.extn7);
    GrouperGroup g8 = GrouperGroup.load(s, Util.stem8, Util.extn8);
    GrouperGroup g9 = GrouperGroup.load(s, Util.stem9, Util.extn9);
    Assert.assertNotNull(g7);
    Assert.assertNotNull(g8);
    Assert.assertNotNull(g9);
    List mem7   = g7.listVals(s);
    List mem7e  = g7.listEffVals(s);
    List mem7i  = g7.listImmVals(s);
    List mem8   = g8.listVals(s);
    List mem8e  = g8.listEffVals(s);
    List mem8i  = g8.listImmVals(s);
    List mem9   = g9.listVals(s);
    List mem9e  = g9.listEffVals(s);
    List mem9i  = g9.listImmVals(s);
    Assert.assertTrue(mem7.size()   == 1);
    Assert.assertTrue(mem7e.size()  == 0);
    Assert.assertTrue(mem7i.size()  == 1);
    Assert.assertTrue(mem8.size()   == 0); 
    Assert.assertTrue(mem8e.size()  == 0); 
    Assert.assertTrue(mem8i.size()  == 0);
    Assert.assertTrue(mem9.size()   == 2); 
    Assert.assertTrue(mem9e.size()  == 0); 
    Assert.assertTrue(mem9i.size()  == 2);
    // We're done
    s.stop(); 
  }

  public void testDel4_2() {
    Subject subj  = GrouperSubject.load(Util.rooti, Util.roott);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);
    GrouperGroup g7 = GrouperGroup.load(s, Util.stem7, Util.extn7);
    GrouperGroup g8 = GrouperGroup.load(s, Util.stem8, Util.extn8);
    GrouperGroup g9 = GrouperGroup.load(s, Util.stem9, Util.extn9);
    GrouperMember g7m = GrouperMember.load(g7.id(), "group");
    GrouperMember g8m = GrouperMember.load(g8.id(), "group");
    GrouperMember g9m = GrouperMember.load(g9.id(), "group");
    Assert.assertNotNull(g7m);
    Assert.assertNotNull(g8m);
    Assert.assertNotNull(g9m);
    GrouperMember m0 = GrouperMember.load(Util.m0i, Util.m0t);
    Assert.assertNotNull(m0);
    GrouperMember m2 = GrouperMember.load(Util.rooti, Util.roott);
    Assert.assertNotNull(m2);
    Assert.assertTrue( g7.listDelVal(s, m0) );
    Assert.assertTrue( g9.listDelVal(s, m2) );
    Assert.assertTrue( g9.listDelVal(s, g8m) );
    Assert.assertTrue( GrouperGroup.delete(s, g7) );
    Assert.assertTrue( GrouperGroup.delete(s, g8) );
    Assert.assertTrue( GrouperGroup.delete(s, g9) );
    s.stop();
  }

  public void testFetchLV4_5() {
    //
    // g7 X
    // g8 X
    // g9 X
    //
    Subject subj    = GrouperSubject.load(Util.rooti, Util.roott);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);
    GrouperGroup g7 = GrouperGroup.load(s, Util.stem7, Util.extn7);
    GrouperGroup g8 = GrouperGroup.load(s, Util.stem8, Util.extn8);
    GrouperGroup g9 = GrouperGroup.load(s, Util.stem9, Util.extn9);
    Assert.assertNull(g7);
    Assert.assertNull(g8);
    Assert.assertNull(g9);
    // We're done
    s.stop(); 
  }

  public void testPrep5() {
    Subject subj  = GrouperSubject.load(Util.rooti, Util.roott);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);
    GrouperGroup g7 = GrouperGroup.create(s, Util.stem7, Util.extn7);
    GrouperGroup g8 = GrouperGroup.create(s, Util.stem8, Util.extn8);
    GrouperGroup g9 = GrouperGroup.create(s, Util.stem9, Util.extn9);
    GrouperGroup g10 = GrouperGroup.create(s, Util.stem10, Util.extn10);
    Assert.assertNotNull(g7);
    Assert.assertNotNull(g8);
    Assert.assertNotNull(g9);
    Assert.assertNotNull(g10);
    GrouperMember m0 = GrouperMember.load(Util.m0i, Util.m0t);
    GrouperMember m1 = GrouperMember.load(Util.m1i, Util.m1t);
    GrouperMember m2 = GrouperMember.load(Util.rooti, Util.roott);
    Assert.assertNotNull(m0);
    Assert.assertNotNull(m1);
    Assert.assertNotNull(m2);
    Assert.assertTrue( g7.listAddVal(s, m0) );
    Assert.assertTrue( g8.listAddVal(s, m1) );
    Assert.assertTrue( g9.listAddVal(s, m2) );
    Assert.assertTrue( g10.listAddVal(s, m0) );
    s.stop();
  }

  public void testFetchLV5_0() {
    //
    //  g7:   m0  /
    //  g8:   m1  /
    //  g9:   m2  /
    //  g10:  m0  /
    //
    Subject subj    = GrouperSubject.load(Util.rooti, Util.roott);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);
    GrouperGroup g7 = GrouperGroup.load(s, Util.stem7, Util.extn7);
    GrouperGroup g8 = GrouperGroup.load(s, Util.stem8, Util.extn8);
    GrouperGroup g9 = GrouperGroup.load(s, Util.stem9, Util.extn9);
    GrouperGroup g10 = GrouperGroup.load(s, Util.stem10, Util.extn10);
    Assert.assertNotNull(g7);
    Assert.assertNotNull(g8);
    Assert.assertNotNull(g9);
    Assert.assertNotNull(g10);
    List mem7   = g7.listVals(s);
    List mem7e  = g7.listEffVals(s);
    List mem7i  = g7.listImmVals(s);
    List mem8   = g8.listVals(s);
    List mem8e  = g8.listEffVals(s);
    List mem8i  = g8.listImmVals(s);
    List mem9   = g9.listVals(s);
    List mem9e  = g9.listEffVals(s);
    List mem9i  = g9.listImmVals(s);
    List mem10   = g10.listVals(s);
    List mem10e  = g10.listEffVals(s);
    List mem10i  = g10.listImmVals(s);
    Assert.assertTrue(mem7.size()   == 1);
    Assert.assertTrue(mem7e.size()  == 0);
    Assert.assertTrue(mem7i.size()  == 1);
    Assert.assertTrue(mem8.size()   == 1);
    Assert.assertTrue(mem8e.size()  == 0);
    Assert.assertTrue(mem8i.size()  == 1);
    Assert.assertTrue(mem9.size()   == 1);
    Assert.assertTrue(mem9e.size()  == 0);
    Assert.assertTrue(mem9i.size()  == 1);
    Assert.assertTrue(mem10.size()   == 1);
    Assert.assertTrue(mem10e.size()  == 0);
    Assert.assertTrue(mem10i.size()  == 1);
    // We're done
    s.stop(); 
  }

  public void testAdd5_0() {
    Subject subj  = GrouperSubject.load(Util.rooti, Util.roott);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);
    GrouperGroup g7 = GrouperGroup.load(s, Util.stem7, Util.extn7);
    GrouperGroup g8 = GrouperGroup.load(s, Util.stem8, Util.extn8);
    GrouperGroup g9 = GrouperGroup.load(s, Util.stem9, Util.extn9);
    GrouperGroup g10 = GrouperGroup.load(s, Util.stem10, Util.extn10);
    GrouperMember g7m = GrouperMember.load(g7.id(), "group");
    GrouperMember g8m = GrouperMember.load(g8.id(), "group");
    GrouperMember g9m = GrouperMember.load(g9.id(), "group");
    GrouperMember g10m = GrouperMember.load(g10.id(), "group");
    Assert.assertNotNull(g7m);
    Assert.assertNotNull(g8m);
    Assert.assertNotNull(g9m);
    Assert.assertNotNull(g10m);
    Assert.assertTrue( g8.listAddVal(s, g7m) );
    s.stop();
  }

  public void testFetchLV5_1() {
    //
    //  g7:   m0      /
    //  g8:   m1, g7  / m0^g7
    //  g9:   m2      /
    //  g10:  m0      /
    //
    Subject subj    = GrouperSubject.load(Util.rooti, Util.roott);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);
    GrouperGroup g7 = GrouperGroup.load(s, Util.stem7, Util.extn7);
    GrouperGroup g8 = GrouperGroup.load(s, Util.stem8, Util.extn8);
    GrouperGroup g9 = GrouperGroup.load(s, Util.stem9, Util.extn9);
    GrouperGroup g10 = GrouperGroup.load(s, Util.stem10, Util.extn10);
    Assert.assertNotNull(g7);
    Assert.assertNotNull(g8);
    Assert.assertNotNull(g9);
    Assert.assertNotNull(g10);
    List mem7   = g7.listVals(s);
    List mem7e  = g7.listEffVals(s);
    List mem7i  = g7.listImmVals(s);
    List mem8   = g8.listVals(s);
    List mem8e  = g8.listEffVals(s);
    List mem8i  = g8.listImmVals(s);
    List mem9   = g9.listVals(s);
    List mem9e  = g9.listEffVals(s);
    List mem9i  = g9.listImmVals(s);
    List mem10   = g10.listVals(s);
    List mem10e  = g10.listEffVals(s);
    List mem10i  = g10.listImmVals(s);
    Assert.assertTrue(mem7.size()   == 1);
    Assert.assertTrue(mem7e.size()  == 0);
    Assert.assertTrue(mem7i.size()  == 1);
    Assert.assertTrue(mem8.size()   == 3); 
    Assert.assertTrue(mem8e.size()  == 1); 
    Assert.assertTrue(mem8i.size()  == 2);
    Assert.assertTrue(mem9.size()   == 1);
    Assert.assertTrue(mem9e.size()  == 0);
    Assert.assertTrue(mem9i.size()  == 1);
    Assert.assertTrue(mem10.size()   == 1);
    Assert.assertTrue(mem10e.size()  == 0);
    Assert.assertTrue(mem10i.size()  == 1);
    // We're done
    s.stop(); 
  }

  public void testAdd5_1() {
    Subject subj  = GrouperSubject.load(Util.rooti, Util.roott);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);
    GrouperGroup g7 = GrouperGroup.load(s, Util.stem7, Util.extn7);
    GrouperGroup g8 = GrouperGroup.load(s, Util.stem8, Util.extn8);
    GrouperGroup g9 = GrouperGroup.load(s, Util.stem9, Util.extn9);
    GrouperGroup g10 = GrouperGroup.load(s, Util.stem10, Util.extn10);
    GrouperMember g7m = GrouperMember.load(g7.id(), "group");
    GrouperMember g8m = GrouperMember.load(g8.id(), "group");
    GrouperMember g9m = GrouperMember.load(g9.id(), "group");
    GrouperMember g10m = GrouperMember.load(g10.id(), "group");
    Assert.assertNotNull(g7m);
    Assert.assertNotNull(g8m);
    Assert.assertNotNull(g9m);
    Assert.assertNotNull(g10m);
    Assert.assertTrue( g9.listAddVal(s, g8m) );
    s.stop();
  }

  public void testFetchLV5_2() {
    //
    //  g7:   m0      /
    //  g8:   m1, g7  / m0^g7
    //  g9:   m2, g8  / m1^g8, g7^g8, m0^g8
    //  g10:  m0      /
    //
    Subject subj    = GrouperSubject.load(Util.rooti, Util.roott);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);
    GrouperGroup g7 = GrouperGroup.load(s, Util.stem7, Util.extn7);
    GrouperGroup g8 = GrouperGroup.load(s, Util.stem8, Util.extn8);
    GrouperGroup g9 = GrouperGroup.load(s, Util.stem9, Util.extn9);
    GrouperGroup g10 = GrouperGroup.load(s, Util.stem10, Util.extn10);
    Assert.assertNotNull(g7);
    Assert.assertNotNull(g8);
    Assert.assertNotNull(g9);
    Assert.assertNotNull(g10);
    List mem7   = g7.listVals(s);
    List mem7e  = g7.listEffVals(s);
    List mem7i  = g7.listImmVals(s);
    List mem8   = g8.listVals(s);
    List mem8e  = g8.listEffVals(s);
    List mem8i  = g8.listImmVals(s);
    List mem9   = g9.listVals(s);
    List mem9e  = g9.listEffVals(s);
    List mem9i  = g9.listImmVals(s);
    List mem10   = g10.listVals(s);
    List mem10e  = g10.listEffVals(s);
    List mem10i  = g10.listImmVals(s);
    Assert.assertTrue(mem7.size()   == 1);
    Assert.assertTrue(mem7e.size()  == 0);
    Assert.assertTrue(mem7i.size()  == 1);
    Assert.assertTrue(mem8.size()   == 3);
    Assert.assertTrue(mem8e.size()  == 1);
    Assert.assertTrue(mem8i.size()  == 2);
    Assert.assertTrue(mem9.size()   == 5); 
    Assert.assertTrue(mem9e.size()  == 3);
    Assert.assertTrue(mem9i.size()  == 2);
    Assert.assertTrue(mem10.size()  == 1); 
    Assert.assertTrue(mem10e.size() == 0); 
    Assert.assertTrue(mem10i.size() == 1);
    // We're done
    s.stop(); 
  }

  public void testAdd5_2() {
    Subject subj  = GrouperSubject.load(Util.rooti, Util.roott);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);
    GrouperGroup g7 = GrouperGroup.load(s, Util.stem7, Util.extn7);
    GrouperGroup g8 = GrouperGroup.load(s, Util.stem8, Util.extn8);
    GrouperGroup g9 = GrouperGroup.load(s, Util.stem9, Util.extn9);
    GrouperGroup g10 = GrouperGroup.load(s, Util.stem10, Util.extn10);
    GrouperMember g7m = GrouperMember.load(g7.id(), "group");
    GrouperMember g8m = GrouperMember.load(g8.id(), "group");
    GrouperMember g9m = GrouperMember.load(g9.id(), "group");
    GrouperMember g10m = GrouperMember.load(g10.id(), "group");
    Assert.assertNotNull(g7m);
    Assert.assertNotNull(g8m);
    Assert.assertNotNull(g9m);
    Assert.assertNotNull(g10m);
    Assert.assertTrue( g7.listAddVal(s, g10m) );
    s.stop();
  }

  public void testFetchLV5_3() {
    //
    //  g7:   m0, g10 / m0^g10
    //  g8:   m1, g7  / m0^g7, g10^g7 XXX m0^g7
    //  g9:   m2, g8  / m1^g8, g7^g8, m0^g8, g10^g8 XXX m0^g8
    //  g10:  m0      /
    //
    Subject subj    = GrouperSubject.load(Util.rooti, Util.roott);
    GrouperSession s = GrouperSession.start(subj);
    GrouperGroup g7 = GrouperGroup.load(s, Util.stem7, Util.extn7);
    GrouperGroup g8 = GrouperGroup.load(s, Util.stem8, Util.extn8);
    GrouperGroup g9 = GrouperGroup.load(s, Util.stem9, Util.extn9);
    GrouperGroup g10 = GrouperGroup.load(s, Util.stem10, Util.extn10);
    Assert.assertTrue("g7  mship",      g7.listVals(s).size()     == 3); 
    Assert.assertTrue("g7  eff mship",  g7.listEffVals(s).size()  == 1);
    Assert.assertTrue("g7  imm mship",  g7.listImmVals(s).size()  == 2);
    Assert.assertTrue("g8  mship",      g8.listVals(s).size()     == 4); // 5
    Assert.assertTrue("g8  eff mship",  g8.listEffVals(s).size()  == 2); // 3
    Assert.assertTrue("g8  imm mship",  g8.listImmVals(s).size()  == 2);
    Assert.assertTrue("g9  mship",      g9.listVals(s).size()     == 6); // 7
    Assert.assertTrue("g9  eff mship",  g9.listEffVals(s).size()  == 4); // 5
    Assert.assertTrue("g9  imm mship",  g9.listImmVals(s).size()  == 2);
    Assert.assertTrue("g10 mship",      g10.listVals(s).size()    == 1);
    Assert.assertTrue("g10 eff mship",  g10.listEffVals(s).size() == 0);
    Assert.assertTrue("g10 imm mship",  g10.listImmVals(s).size() == 1);
    // We're done
    s.stop(); 
  }

  public void testDel5_0() {
    Subject subj  = GrouperSubject.load(Util.rooti, Util.roott);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);
    GrouperGroup g7 = GrouperGroup.load(s, Util.stem7, Util.extn7);
    GrouperGroup g8 = GrouperGroup.load(s, Util.stem8, Util.extn8);
    GrouperGroup g9 = GrouperGroup.load(s, Util.stem9, Util.extn9);
    GrouperGroup g10 = GrouperGroup.load(s, Util.stem10, Util.extn10);
    GrouperMember g7m = GrouperMember.load(g7.id(), "group");
    GrouperMember g8m = GrouperMember.load(g8.id(), "group");
    GrouperMember g9m = GrouperMember.load(g9.id(), "group");
    GrouperMember g10m = GrouperMember.load(g10.id(), "group");
    Assert.assertNotNull(g7m);
    Assert.assertNotNull(g8m);
    Assert.assertNotNull(g9m);
    Assert.assertNotNull(g10m);
    Assert.assertTrue( g9.listDelVal(s, g8m) );
    s.stop();
  }

  public void testFetchLV5_4() {
    //
    //  g7:   m0, g10 / m0^g10
    //  g8:   m1, g7  / m0^g7, g10^g7 XXX m0^g7
    //  g9:   m2      / 
    //  g10:  m0      /
    //
    Subject subj      = GrouperSubject.load(Util.rooti, Util.roott);
    GrouperSession s  = GrouperSession.start(subj);
    GrouperGroup g7   = GrouperGroup.load(s, Util.stem7, Util.extn7);
    GrouperGroup g8   = GrouperGroup.load(s, Util.stem8, Util.extn8);
    GrouperGroup g9   = GrouperGroup.load(s, Util.stem9, Util.extn9);
    GrouperGroup g10  = GrouperGroup.load(s, Util.stem10, Util.extn10);
    Assert.assertTrue("g7  mship",      g7.listVals(s).size()     == 3); 
    Assert.assertTrue("g7  eff mship",  g7.listEffVals(s).size()  == 1);
    Assert.assertTrue("g7  imm mship",  g7.listImmVals(s).size()  == 2);
    Assert.assertTrue("g8  mship",      g8.listVals(s).size()     == 4); // 5
    Assert.assertTrue("g8  eff mship",  g8.listEffVals(s).size()  == 2); // 3
    Assert.assertTrue("g8  imm mship",  g8.listImmVals(s).size()  == 2);
    Assert.assertTrue("g9  mship",      g9.listVals(s).size()     == 1); 
    Assert.assertTrue("g9  eff mship",  g9.listEffVals(s).size()  == 0); 
    Assert.assertTrue("g9  imm mship",  g9.listImmVals(s).size()  == 1);
    Assert.assertTrue("g10 mship",      g10.listVals(s).size()    == 1);
    Assert.assertTrue("g10 eff mship",  g10.listEffVals(s).size() == 0);
    Assert.assertTrue("g10 imm mship",  g10.listImmVals(s).size() == 1);
    // We're done
    s.stop(); 
  }

  public void testDel5_1() {
    Subject subj  = GrouperSubject.load(Util.rooti, Util.roott);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);
    GrouperGroup g7 = GrouperGroup.load(s, Util.stem7, Util.extn7);
    GrouperGroup g8 = GrouperGroup.load(s, Util.stem8, Util.extn8);
    GrouperGroup g9 = GrouperGroup.load(s, Util.stem9, Util.extn9);
    GrouperGroup g10 = GrouperGroup.load(s, Util.stem10, Util.extn10);
    GrouperMember g7m = GrouperMember.load(g7.id(), "group");
    GrouperMember g8m = GrouperMember.load(g8.id(), "group");
    GrouperMember g9m = GrouperMember.load(g9.id(), "group");
    GrouperMember g10m = GrouperMember.load(g10.id(), "group");
    Assert.assertNotNull(g7m);
    Assert.assertNotNull(g8m);
    Assert.assertNotNull(g9m);
    Assert.assertNotNull(g10m);
    Assert.assertTrue( g7.listDelVal(s, g10m) );
    s.stop();
  }

  public void testFetchLV5_5() {
    //
    //  g7:   m0      / 
    //  g8:   m1, g7  / XXX m0^g7
    //  g9:   m2      / 
    //  g10:  m0      /
    //
    Subject subj    =  GrouperSubject.load(Util.rooti, Util.roott);
    GrouperSession s  = GrouperSession.start(subj);
    GrouperGroup g7   = GrouperGroup.load(s, Util.stem7, Util.extn7);
    GrouperGroup g8   = GrouperGroup.load(s, Util.stem8, Util.extn8);
    GrouperGroup g9   = GrouperGroup.load(s, Util.stem9, Util.extn9);
    GrouperGroup g10  = GrouperGroup.load(s, Util.stem10, Util.extn10);
    Assert.assertTrue("g7  mship",      g7.listVals(s).size()     == 1); 
    Assert.assertTrue("g7  eff mship",  g7.listEffVals(s).size()  == 0); 
    Assert.assertTrue("g7  imm mship",  g7.listImmVals(s).size()  == 1); 
    Assert.assertTrue("g8  mship",      g8.listVals(s).size()     == 2); // 3
    Assert.assertTrue("g8  eff mship",  g8.listEffVals(s).size()  == 0); // 1
    Assert.assertTrue("g8  imm mship",  g8.listImmVals(s).size()  == 2);
    Assert.assertTrue("g9  mship",      g9.listVals(s).size()     == 1); 
    Assert.assertTrue("g9  eff mship",  g9.listEffVals(s).size()  == 0); 
    Assert.assertTrue("g9  imm mship",  g9.listImmVals(s).size()  == 1);
    Assert.assertTrue("g10 mship",      g10.listVals(s).size()    == 1);
    Assert.assertTrue("g10 eff mship",  g10.listEffVals(s).size() == 0);
    Assert.assertTrue("g10 imm mship",  g10.listImmVals(s).size() == 1);
    // We're done
    s.stop(); 
  }

  public void testDel5_2() {
    Subject subj  = GrouperSubject.load(Util.rooti, Util.roott);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);
    GrouperGroup g7 = GrouperGroup.load(s, Util.stem7, Util.extn7);
    GrouperGroup g8 = GrouperGroup.load(s, Util.stem8, Util.extn8);
    GrouperGroup g9 = GrouperGroup.load(s, Util.stem9, Util.extn9);
    GrouperGroup g10 = GrouperGroup.load(s, Util.stem10, Util.extn10);
    GrouperMember g7m = GrouperMember.load(g7.id(), "group");
    GrouperMember g8m = GrouperMember.load(g8.id(), "group");
    GrouperMember g9m = GrouperMember.load(g9.id(), "group");
    GrouperMember g10m = GrouperMember.load(g10.id(), "group");
    GrouperMember m0 = GrouperMember.load(Util.m0i, Util.m0t);
    Assert.assertNotNull(g7m);
    Assert.assertNotNull(g8m);
    Assert.assertNotNull(g9m);
    Assert.assertNotNull(g10m);
    Assert.assertNotNull(m0);
    Assert.assertTrue( g7.listDelVal(s, m0) );
    s.stop();
  }

  public void testFetchLV5_6() {
    //
    //  g7:           / 
    //  g8:   m1, g7  / 
    //  g9:   m2      / 
    //  g10:  m0      /
    //
    Subject subj    = GrouperSubject.load(Util.rooti, Util.roott);
    GrouperSession s = GrouperSession.start(subj);
    GrouperGroup g7 = GrouperGroup.load(s, Util.stem7, Util.extn7);
    GrouperGroup g8 = GrouperGroup.load(s, Util.stem8, Util.extn8);
    GrouperGroup g9 = GrouperGroup.load(s, Util.stem9, Util.extn9);
    GrouperGroup g10 = GrouperGroup.load(s, Util.stem10, Util.extn10);
    Assert.assertTrue("g7  mship",      g7.listVals(s).size()     == 0); 
    Assert.assertTrue("g7  eff mship",  g7.listEffVals(s).size()  == 0); 
    Assert.assertTrue("g7  imm mship",  g7.listImmVals(s).size()  == 0); 
    Assert.assertTrue("g8  mship",      g8.listVals(s).size()     == 2); 
    Assert.assertTrue("g8  eff mship",  g8.listEffVals(s).size()  == 0); 
    Assert.assertTrue("g8  imm mship",  g8.listImmVals(s).size()  == 2);
    Assert.assertTrue("g9  mship",      g9.listVals(s).size()     == 1); 
    Assert.assertTrue("g9  eff mship",  g9.listEffVals(s).size()  == 0); 
    Assert.assertTrue("g9  imm mship",  g9.listImmVals(s).size()  == 1);
    Assert.assertTrue("g10 mship",      g10.listVals(s).size()    == 1);
    Assert.assertTrue("g10 eff mship",  g10.listEffVals(s).size() == 0);
    Assert.assertTrue("g10 imm mship",  g10.listImmVals(s).size() == 1);
    // We're done
    s.stop(); 
  }

  public void testDel5_3() {
    Subject subj  = GrouperSubject.load(Util.rooti, Util.roott);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);
    GrouperGroup g7 = GrouperGroup.load(s, Util.stem7, Util.extn7);
    GrouperGroup g8 = GrouperGroup.load(s, Util.stem8, Util.extn8);
    GrouperGroup g9 = GrouperGroup.load(s, Util.stem9, Util.extn9);
    GrouperGroup g10 = GrouperGroup.load(s, Util.stem10, Util.extn10);
    GrouperMember g7m = GrouperMember.load(g7.id(), "group");
    GrouperMember g8m = GrouperMember.load(g8.id(), "group");
    GrouperMember g9m = GrouperMember.load(g9.id(), "group");
    GrouperMember g10m = GrouperMember.load(g10.id(), "group");
    GrouperMember m0 = GrouperMember.load(Util.m0i, Util.m0t);
    GrouperMember m1 = GrouperMember.load(Util.m1i, Util.m1t);
    GrouperMember m2 = GrouperMember.load(Util.rooti, Util.roott);
    Assert.assertNotNull(g7m);
    Assert.assertNotNull(g8m);
    Assert.assertNotNull(g9m);
    Assert.assertNotNull(g10m);
    Assert.assertNotNull(m0);
    Assert.assertNotNull(m1);
    Assert.assertNotNull(m2);
    Assert.assertTrue( g8.listDelVal(s, m1) );
    Assert.assertTrue( g8.listDelVal(s, g7m) );
    Assert.assertTrue( g9.listDelVal(s, m2) );
    Assert.assertTrue( g10.listDelVal(s, m0) );
    Assert.assertTrue( GrouperGroup.delete(s, g7) );
    Assert.assertTrue( GrouperGroup.delete(s, g8) );
    Assert.assertTrue( GrouperGroup.delete(s, g9) );
    Assert.assertTrue( GrouperGroup.delete(s, g10) );
    s.stop();
  }

  public void testFetchLV5_7() {
    //
    //  g7:   X
    //  g8:   X
    //  g9:   X
    //  g10:  X
    //
    Subject subj      = GrouperSubject.load(Util.rooti, Util.roott);
    GrouperSession s  = GrouperSession.start(subj);
    GrouperGroup g7   = GrouperGroup.load(s, Util.stem7, Util.extn7);
    GrouperGroup g8   = GrouperGroup.load(s, Util.stem8, Util.extn8);
    GrouperGroup g9   = GrouperGroup.load(s, Util.stem9, Util.extn9);
    GrouperGroup g10  = GrouperGroup.load(s, Util.stem10, Util.extn10);
    Assert.assertNull(g7);
    Assert.assertNull(g8);
    Assert.assertNull(g9);
    Assert.assertNull(g10);
    // We're done
    s.stop(); 
  }

  // grouperzilla#286
  public void testLoop0Setup() {
    //
    //  g11: m0
    //  g12: m1
    //
    Subject subj  = GrouperSubject.load(Util.rooti, Util.roott);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);
    GrouperGroup g11 = GrouperGroup.create(s, Util.stem11, Util.extn11);
    GrouperGroup g12 = GrouperGroup.create(s, Util.stem12, Util.extn12);
    Assert.assertNotNull(g11);
    Assert.assertNotNull(g12);
    GrouperMember m0 = GrouperMember.load(Util.m0i, Util.m0t);
    GrouperMember m1 = GrouperMember.load(Util.m1i, Util.m1t);
    Assert.assertNotNull(m0);
    Assert.assertNotNull(m1);
    Assert.assertTrue( g11.listAddVal(s, m0) );
    Assert.assertTrue( g12.listAddVal(s, m1) );
    Assert.assertTrue( g11.listVals(s).size()     == 1 );
    Assert.assertTrue( g11.listImmVals(s).size()  == 1 );
    Assert.assertTrue( g11.listEffVals(s).size()  == 0 );
    Assert.assertTrue( g12.listVals(s).size()     == 1 );
    Assert.assertTrue( g12.listImmVals(s).size()  == 1 );
    Assert.assertTrue( g12.listEffVals(s).size()  == 0 );
    s.stop();
  }

  // Make g11 a member of g12
  public void testLoop0t0() {
    //
    //  g11: m0
    //  g12: m1, g11, m0^g11
    //
    Subject subj  = GrouperSubject.load(Util.rooti, Util.roott);
    GrouperSession s = GrouperSession.start(subj);
    GrouperGroup  g11 = GrouperGroup.load(s, Util.stem11, Util.extn11);
    GrouperGroup  g12 = GrouperGroup.load(s, Util.stem12, Util.extn12);
    GrouperMember m11 = GrouperMember.load( g11.id(), "group" );
    Assert.assertNotNull(m11);
    Assert.assertTrue( "Add g11 to g12" , g12.listAddVal(s, m11) );
    Assert.assertTrue( "g11 mships"     , g11.listVals(s).size()     == 1 );
    Assert.assertTrue( "g11 imm mships" , g11.listImmVals(s).size()  == 1 );
    Assert.assertTrue( "g11 eff mships" , g11.listEffVals(s).size()  == 0 );
    Assert.assertTrue( "g12 mships"     , g12.listVals(s).size()     == 3 );
    Assert.assertTrue( "g12 imm mships" , g12.listImmVals(s).size()  == 2 );
    Assert.assertTrue( "g12 eff mships" , g12.listEffVals(s).size()  == 1 );
    s.stop();
  }

  // Make g12 a member of g11
  public void testLoop0t1() {
    //
    //  g11: m0, g12, m1^g12, g11^g12, m0^g12
    //  g12: m1, g11, m0^g11, g12^g11, m1^g11, g11^g11 
    //
    Subject subj  = GrouperSubject.load(Util.rooti, Util.roott);
    GrouperSession s = GrouperSession.start(subj);
    GrouperGroup  g11 = GrouperGroup.load(s, Util.stem11, Util.extn11);
    GrouperGroup  g12 = GrouperGroup.load(s, Util.stem12, Util.extn12);
    GrouperMember m12 = GrouperMember.load( g12.id(), "group" );
    Assert.assertNotNull(m12);
    Assert.assertTrue( "Add g12 to g11" , g11.listAddVal(s, m12) );
    Assert.assertTrue( "g11 mships"     , g11.listVals(s).size()     == 5 ); // 4
    Assert.assertTrue( "g11 imm mships" , g11.listImmVals(s).size()  == 2 );
    Assert.assertTrue( "g11 eff mships" , g11.listEffVals(s).size()  == 3 ); // 2
    Assert.assertTrue( "g12 mships"     , g12.listVals(s).size()     == 6 ); // 4
    Assert.assertTrue( "g12 imm mships" , g12.listImmVals(s).size()  == 2 );
    Assert.assertTrue( "g12 eff mships" , g12.listEffVals(s).size()  == 4 ); // 2
    s.stop();
  }

  // Make m2 a member of g11
  public void testLoop0t2() {
    //
    //  g11: m0, g12, m1^g12, g11^g12, m0^g12, m2, m2^g12
    //  g12: m1, g11, m0^g11, g12^g11, m1^g11, g11^g11, m2^g11
    //
    Subject subj      = GrouperSubject.load(Util.rooti, Util.roott);
    GrouperSession s  = GrouperSession.start(subj);
    GrouperGroup  g11 = GrouperGroup.load(s, Util.stem11, Util.extn11);
    GrouperGroup  g12 = GrouperGroup.load(s, Util.stem12, Util.extn12);
    GrouperMember m2  = GrouperMember.load(Util.rooti, Util.roott);
    Assert.assertNotNull(m2);
    Assert.assertTrue( "Add m2 to g11"  , g11.listAddVal(s, m2) );
    Assert.assertTrue( "g11 mships"     , g11.listVals(s).size()     == 7 ); // 5
    Assert.assertTrue( "g11 imm mships" , g11.listImmVals(s).size()  == 3 );
    Assert.assertTrue( "g11 eff mships" , g11.listEffVals(s).size()  == 4 ); // 2
    Assert.assertTrue( "g12 mships"     , g12.listVals(s).size()     == 7 ); // 5
    Assert.assertTrue( "g12 imm mships" , g12.listImmVals(s).size()  == 2 );
    Assert.assertTrue( "g12 eff mships" , g12.listEffVals(s).size()  == 5 ); // 3
    s.stop();
  }

  // Make m2 a member of g12
  public void testLoop0t3() {
    //
    //  g11: m0, g12, m1^g12, g11^g12, m0^g12, m2, m2^g12
    //  g12: m1, g11, m0^g11, g12^g11, m1^g11, g11^g11, m2^g11, m2
    //
    Subject subj      = GrouperSubject.load(Util.rooti, Util.roott);
    GrouperSession s  = GrouperSession.start(subj);
    GrouperGroup  g11 = GrouperGroup.load(s, Util.stem11, Util.extn11);
    GrouperGroup  g12 = GrouperGroup.load(s, Util.stem12, Util.extn12);
    GrouperMember m2  = GrouperMember.load(Util.rooti, Util.roott);
    Assert.assertNotNull(m2);
    Assert.assertTrue( "Add m2 to g12"  , g12.listAddVal(s, m2) );
    Assert.assertTrue( "g11 mships"     , g11.listVals(s).size()     == 7 ); // 6
    Assert.assertTrue( "g11 imm mships" , g11.listImmVals(s).size()  == 3 );
    Assert.assertTrue( "g11 eff mships" , g11.listEffVals(s).size()  == 4 ); // 3
    Assert.assertTrue( "g12 mships"     , g12.listVals(s).size()     == 8 ); // 6
    Assert.assertTrue( "g12 imm mships" , g12.listImmVals(s).size()  == 3 );
    Assert.assertTrue( "g12 eff mships" , g12.listEffVals(s).size()  == 5 ); // 3
    s.stop();
  }

  public void testLoop0TearDown() {
    Subject subj  = GrouperSubject.load(Util.rooti, Util.roott);
    GrouperSession s = GrouperSession.start(subj);
    GrouperGroup  g11 = GrouperGroup.load(s, Util.stem11, Util.extn11);
    GrouperGroup  g12 = GrouperGroup.load(s, Util.stem12, Util.extn12);
    GrouperMember m0  = GrouperMember.load(Util.m0i, Util.m0t);
    GrouperMember m1  = GrouperMember.load(Util.m1i, Util.m1t);
    GrouperMember m2  = GrouperMember.load(Util.rooti, Util.roott);
    GrouperMember m11 = GrouperMember.load( g11.id(), "group" );
    GrouperMember m12 = GrouperMember.load( g12.id(), "group" );
    Assert.assertTrue( g11.listDelVal(s, m0) );
    Assert.assertTrue( g11.listDelVal(s, m2) );
    Assert.assertTrue( g11.listDelVal(s, m12) );
    Assert.assertTrue( g12.listDelVal(s, m1) );
    Assert.assertTrue( g12.listDelVal(s, m2) );
    Assert.assertTrue( g12.listDelVal(s, m11) );
    Assert.assertTrue( GrouperGroup.delete(s, g11) );
    Assert.assertTrue( GrouperGroup.delete(s, g12) );
  }

  // grouperzilla#286
  public void testLoop1Setup() {
    //
    //  g11: 
    //  g12: 
    //  g13: 
    //
    Subject subj  = GrouperSubject.load(Util.rooti, Util.roott);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);
    GrouperGroup g11 = GrouperGroup.create(s, Util.stem11, Util.extn11);
    GrouperGroup g12 = GrouperGroup.create(s, Util.stem12, Util.extn12);
    GrouperGroup g13 = GrouperGroup.create(s, Util.stem13, Util.extn13);
    Assert.assertNotNull(g11);
    Assert.assertNotNull(g12);
    Assert.assertNotNull(g13);
    Assert.assertTrue( g11.listVals(s).size()     == 0 );
    Assert.assertTrue( g11.listImmVals(s).size()  == 0 );
    Assert.assertTrue( g11.listEffVals(s).size()  == 0 );
    Assert.assertTrue( g12.listVals(s).size()     == 0 );
    Assert.assertTrue( g12.listImmVals(s).size()  == 0 );
    Assert.assertTrue( g12.listEffVals(s).size()  == 0 );
    Assert.assertTrue( g13.listVals(s).size()     == 0 );
    Assert.assertTrue( g13.listImmVals(s).size()  == 0 );
    Assert.assertTrue( g13.listEffVals(s).size()  == 0 );
    s.stop();
  }

  // Add g11 to g12
  public void testLoop1t0() {
    //
    //  g11: 
    //  g12: g11
    //  g13: 
    //
    Subject subj      = GrouperSubject.load(Util.rooti, Util.roott);
    GrouperSession s  = GrouperSession.start(subj);
    GrouperGroup g11  = GrouperGroup.load(s, Util.stem11, Util.extn11);
    GrouperGroup g12  = GrouperGroup.load(s, Util.stem12, Util.extn12);
    GrouperGroup g13  = GrouperGroup.load(s, Util.stem13, Util.extn13);
    GrouperMember m11 = GrouperMember.load( g11.id(), "group" );
    Assert.assertNotNull(m11);
    Assert.assertTrue( "Add g11 to g12" , g12.listAddVal(s, m11) );
    Assert.assertTrue( "g11 mships"     , g11.listVals(s).size()     == 0 );
    Assert.assertTrue( "g11 imm mships" , g11.listImmVals(s).size()  == 0 );
    Assert.assertTrue( "g11 eff mships" , g11.listEffVals(s).size()  == 0 );
    Assert.assertTrue( "g12 mships"     , g12.listVals(s).size()     == 1 );
    Assert.assertTrue( "g12 imm mships" , g12.listImmVals(s).size()  == 1 );
    Assert.assertTrue( "g12 eff mships" , g12.listEffVals(s).size()  == 0 );
    Assert.assertTrue( "g13 mships"     , g13.listVals(s).size()     == 0 );
    Assert.assertTrue( "g13 imm mships" , g13.listImmVals(s).size()  == 0 );
    Assert.assertTrue( "g13 eff mships" , g13.listEffVals(s).size()  == 0 );
    s.stop();
  }

  // Add g11 to g13
  public void testLoop1t1() {
    //
    //  g11: 
    //  g12: g11
    //  g13: g11
    //
    Subject subj      = GrouperSubject.load(Util.rooti, Util.roott);
    GrouperSession s  = GrouperSession.start(subj);
    GrouperGroup g11  = GrouperGroup.load(s, Util.stem11, Util.extn11);
    GrouperGroup g12  = GrouperGroup.load(s, Util.stem12, Util.extn12);
    GrouperGroup g13  = GrouperGroup.load(s, Util.stem13, Util.extn13);
    GrouperMember m11 = GrouperMember.load( g11.id(), "group" );
    Assert.assertTrue( "Add g11 to g13" , g13.listAddVal(s, m11) );
    Assert.assertTrue( "g11 mships"     , g11.listVals(s).size()     == 0 );
    Assert.assertTrue( "g11 imm mships" , g11.listImmVals(s).size()  == 0 );
    Assert.assertTrue( "g11 eff mships" , g11.listEffVals(s).size()  == 0 );
    Assert.assertTrue( "g12 mships"     , g12.listVals(s).size()     == 1 );
    Assert.assertTrue( "g12 imm mships" , g12.listImmVals(s).size()  == 1 );
    Assert.assertTrue( "g12 eff mships" , g12.listEffVals(s).size()  == 0 );
    Assert.assertTrue( "g13 mships"     , g13.listVals(s).size()     == 1 );
    Assert.assertTrue( "g13 imm mships" , g13.listImmVals(s).size()  == 1 );
    Assert.assertTrue( "g13 eff mships" , g13.listEffVals(s).size()  == 0 );
    s.stop();
  }

  // Add g12 to g11
  public void testLoop1t2() {
    //
    //  g11: g12, g11^g12
    //  g12: g11, g12^g11, g11^g11
    //  g13: g11, g12^g11, g11^g11
    //
    Subject subj      = GrouperSubject.load(Util.rooti, Util.roott);
    GrouperSession s  = GrouperSession.start(subj);
    GrouperGroup g11  = GrouperGroup.load(s, Util.stem11, Util.extn11);
    GrouperGroup g12  = GrouperGroup.load(s, Util.stem12, Util.extn12);
    GrouperGroup g13  = GrouperGroup.load(s, Util.stem13, Util.extn13);
    GrouperMember m12 = GrouperMember.load( g12.id(), "group" );
    Assert.assertTrue( "Add g12 to g11" , g11.listAddVal(s, m12) );
    Assert.assertTrue( "g11 mships"     , g11.listVals(s).size()     == 2 );
    Assert.assertTrue( "g11 imm mships" , g11.listImmVals(s).size()  == 1 );
    Assert.assertTrue( "g11 eff mships" , g11.listEffVals(s).size()  == 1 );
    Assert.assertTrue( "g12 mships"     , g12.listVals(s).size()     == 3 ); 
    Assert.assertTrue( "g12 imm mships" , g12.listImmVals(s).size()  == 1 );
    Assert.assertTrue( "g12 eff mships" , g12.listEffVals(s).size()  == 2 ); 
    Assert.assertTrue( "g13 mships"     , g13.listVals(s).size()     == 3 );
    Assert.assertTrue( "g13 imm mships" , g13.listImmVals(s).size()  == 1 );
    Assert.assertTrue( "g13 eff mships" , g13.listEffVals(s).size()  == 2 );
    s.stop();
  }

  // Add g13 to g11
  public void testLoop1t3() {
    Subject subj      = GrouperSubject.load(Util.rooti, Util.roott);
    GrouperSession s  = GrouperSession.start(subj);
    GrouperGroup g11  = GrouperGroup.load(s, Util.stem11, Util.extn11);
    GrouperGroup g12  = GrouperGroup.load(s, Util.stem12, Util.extn12);
    GrouperGroup g13  = GrouperGroup.load(s, Util.stem13, Util.extn13);
    GrouperMember m13 = GrouperMember.load( g13.id(), "group" );
    Assert.assertTrue( "Add g13 to g11" , g11.listAddVal(s, m13) );
    Assert.assertTrue( "g11 mships"     , g11.listVals(s).size()     == 7 ); 
    Assert.assertTrue( "g11 imm mships" , g11.listImmVals(s).size()  == 2 );
    Assert.assertTrue( "g11 eff mships" , g11.listEffVals(s).size()  == 5 ); 
    Assert.assertTrue( "g12 mships"     , g12.listVals(s).size()     == 4 ); 
    Assert.assertTrue( "g12 imm mships" , g12.listImmVals(s).size()  == 1 );
    Assert.assertTrue( "g12 eff mships" , g12.listEffVals(s).size()  == 3 ); 
    Assert.assertTrue( "g13 mships"     , g13.listVals(s).size()     == 4 ); 
    Assert.assertTrue( "g13 imm mships" , g13.listImmVals(s).size()  == 1 );
    Assert.assertTrue( "g13 eff mships" , g13.listEffVals(s).size()  == 3 ); 
    s.stop();
  }

  public void testLoop1TearDown() {
    Subject subj  = GrouperSubject.load(Util.rooti, Util.roott);
    GrouperSession s = GrouperSession.start(subj);
    GrouperGroup  g11 = GrouperGroup.load(s, Util.stem11, Util.extn11);
    GrouperGroup  g12 = GrouperGroup.load(s, Util.stem12, Util.extn12);
    GrouperGroup  g13 = GrouperGroup.load(s, Util.stem13, Util.extn13);
    GrouperMember m11 = GrouperMember.load( g11.id(), "group" );
    GrouperMember m12 = GrouperMember.load( g12.id(), "group" );
    GrouperMember m13 = GrouperMember.load( g13.id(), "group" );
    Assert.assertTrue( g11.listDelVal(s, m12) );
    Assert.assertTrue( g11.listDelVal(s, m13) );
    Assert.assertTrue( g12.listDelVal(s, m11) );
    Assert.assertTrue( g13.listDelVal(s, m11) );
    Assert.assertTrue( GrouperGroup.delete(s, g11) );
    Assert.assertTrue( GrouperGroup.delete(s, g12) );
    Assert.assertTrue( GrouperGroup.delete(s, g13) );
  }

}

