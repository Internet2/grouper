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
    Subject         subj    = GrouperSubject.lookup( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // Fetch Group 0
    GrouperGroup    g0    = GrouperGroup.load(s, stem0, extn0);
    Assert.assertNotNull(g0);
    // Fetch Group 2
    GrouperGroup    g2    = GrouperGroup.load(s, stem2, extn2);
    Assert.assertNotNull(g2);
    // Fetch Member 0
    GrouperMember   m0      = GrouperMember.lookup("blair", Grouper.DEF_SUBJ_TYPE);
    Assert.assertNotNull(m0);
    // Fetch Member 1
    GrouperMember   m1      = GrouperMember.lookup("notblair", Grouper.DEF_SUBJ_TYPE);
    Assert.assertNotNull(m1);
    // Add m0 to g0 Grouper.DEF_LIST_TYPE
    Assert.assertTrue( g0.listAddVal(s, m0) );
    // Add m1 to g2 Grouper.DEF_LIST_TYPE
    Assert.assertTrue( g2.listAddVal(s, m1) );
    // We're done
    s.stop();
  }

  public void testFetchListData0() {
    // g0 (m0)  ()
    // g1 ()    ()
    // g2 (m1)  ()
    Subject         subj    = GrouperSubject.lookup( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
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
    Subject         subj    = GrouperSubject.lookup( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
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
    Subject         subj    = GrouperSubject.lookup( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // Fetch Group 0
    GrouperGroup    g0    = GrouperGroup.load(s, stem0, extn0);
    Assert.assertNotNull(g0);
    // Fetch Group 2
    GrouperGroup    g2    = GrouperGroup.load(s, stem2, extn2);
    Assert.assertNotNull(g2);
    // Fetch Member 0
    GrouperMember   m0      = GrouperMember.lookup("blair", Grouper.DEF_SUBJ_TYPE);
    Assert.assertNotNull(m0);
    // Fetch Member 1
    GrouperMember   m1      = GrouperMember.lookup("notblair", Grouper.DEF_SUBJ_TYPE);
    Assert.assertNotNull(m1);
    // Add m0 to g0 Grouper.DEF_LIST_TYPE
   // FIXME  (#269) Assert.assertFalse( g0.listAddVal(s, m0, "invalid members") );
    // Add m1 to g2 Grouper.DEF_LIST_TYPE
    // FIXME (#269) Assert.assertFalse( g2.listAddVal(s, m1, "invalid members") );
    // We're done
    s.stop();
  }

  public void testAddListData1() {
    Subject         subj    = GrouperSubject.lookup( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // Fetch Group 0
    GrouperGroup    g0    = GrouperGroup.load(s, stem0, extn0);
    Assert.assertNotNull(g0);
    // Fetch Group 1
    GrouperGroup    g1    = GrouperGroup.load(s, stem1, extn1);
    Assert.assertNotNull(g1);
    // Fetch g1 as m0
    GrouperMember   m0      = GrouperMember.lookup( g1.id(), "group");
    Assert.assertNotNull(m0);
    // Add m0 to g0 Grouper.DEF_LIST_TYPE
    Assert.assertTrue( g0.listAddVal(s, m0) );
    // We're done
    s.stop();
  } 

  public void testFetchListData1() {
    // g0 (m0, g1)  ()
    // g1 ()        ()
    // g2 (m1)      ()
    Subject         subj    = GrouperSubject.lookup( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
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
    Subject         subj    = GrouperSubject.lookup( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // Fetch Group 0
    GrouperGroup    g0    = GrouperGroup.load(s, stem0, extn0);
    Assert.assertNotNull(g0);
    // Fetch Group 2
    GrouperGroup    g2    = GrouperGroup.load(s, stem2, extn2);
    Assert.assertNotNull(g2);
    // Fetch g2 as m0
    GrouperMember   m0      = GrouperMember.lookup( g2.id(), "group");
    Assert.assertNotNull(m0);
    // Add m0 to g0 Grouper.DEF_LIST_TYPE
    Assert.assertTrue( g0.listAddVal(s, m0) );
    // We're done
    s.stop();
  }

  public void testFetchListData2() {
    //
    // g0 (m0, g1, g2)  (m1)
    // g1 ()            ()
    // g2 (m1)          ()
    //
    Subject         subj    = GrouperSubject.lookup( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
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
    Assert.assertTrue(mem0.size() == 4);
    List            mem0c   = g0.listVals(s);
    Assert.assertNotNull(mem0c);
    Assert.assertTrue(mem0c.size() == 4);
    List            mem0e   = g0.listEffVals(s, Grouper.DEF_LIST_TYPE); 
    Assert.assertNotNull(mem0e);
    Assert.assertTrue(mem0e.size() == 1); 
    List            mem0i   = g0.listImmVals(s); 
    Assert.assertNotNull(mem0i);
    Assert.assertTrue(mem0i.size() == 3); 
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

  public void testRemoveListData0() {
    Subject         subj    = GrouperSubject.lookup( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // Fetch Group 0
    GrouperGroup    g0    = GrouperGroup.load(s, stem0, extn0);
    Assert.assertNotNull(g0);
    // Fetch Group 2
    GrouperGroup    g2    = GrouperGroup.load(s, stem2, extn2);
    Assert.assertNotNull(g2);
    // Fetch Member 0
    GrouperMember   m0      = GrouperMember.lookup("blair", Grouper.DEF_SUBJ_TYPE);
    Assert.assertNotNull(m0);
    // Fetch Member 1
    GrouperMember   m1      = GrouperMember.lookup("notblair", Grouper.DEF_SUBJ_TYPE);
    Assert.assertNotNull(m1);
    // Remove m0 from g0 Grouper.DEF_LIST_TYPE
    Assert.assertTrue( g0.listDelVal(s, m0) );
    // We're done
    s.stop();
  }

  public void testFetchListData3() {
    //
    // g0 (g1, g2)  (m1)
    // g1 ()        ()
    // g2 (m1)      ()
    //
    Subject         subj    = GrouperSubject.lookup( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
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
    Subject         subj    = GrouperSubject.lookup( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // Fetch Group 0
    GrouperGroup    g0    = GrouperGroup.load(s, stem0, extn0);
    Assert.assertNotNull(g0);
    // Fetch Group 1
    GrouperGroup    g1    = GrouperGroup.load(s, stem1, extn1);
    Assert.assertNotNull(g1);
    // Fetch g1 as m1
    GrouperMember   m1      = GrouperMember.lookup( g1.id(), "group");
    Assert.assertNotNull(m1);
    // Remove m1 (g1) from g0 Grouper.DEF_LIST_TYPE
    Assert.assertTrue( g0.listDelVal(s, m1) );
    // We're done
    s.stop();
  }

  public void testFetchListData4() {
    //
    // g0 (g2)  (m1)
    // g1 ()    ()
    // g2 (m1)  ()
    //
    Subject         subj    = GrouperSubject.lookup( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
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
    Subject         subj    = GrouperSubject.lookup( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // Fetch Group 0
    GrouperGroup    g0    = GrouperGroup.load(s, stem0, extn0);
    Assert.assertNotNull(g0);
    // Fetch Group 1
    GrouperGroup    g2    = GrouperGroup.load(s, stem2, extn2);
    Assert.assertNotNull(g2);
    // Fetch g2 as m2
    GrouperMember   m2      = GrouperMember.lookup( g2.id(), "group");
    Assert.assertNotNull(m2);
    // Remove m2 (g2) from g0 Grouper.DEF_LIST_TYPE
    Assert.assertTrue( g0.listDelVal(s, m2) );
    // We're done
    s.stop();
  }

  public void testFetchListData5() {
    //
    // g0 ()    ()
    // g1 ()    ()
    // g2 (m1)  ()
    //
    Subject         subj    = GrouperSubject.lookup( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
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
    Subject         subj    = GrouperSubject.lookup( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // Fetch g0
    GrouperGroup    g0    = GrouperGroup.load(s, stem0, extn0);
    Assert.assertNotNull(g0);
    // Fetch g2
    GrouperGroup    g2    = GrouperGroup.load(s, stem2, extn2);
    Assert.assertNotNull(g2);
    // Fetch g2 as m2
    GrouperMember   m2      = GrouperMember.lookup( g2.id(), "group");
    Assert.assertNotNull(m2);
    // Add m2/g2 to g0
    Assert.assertTrue( g0.listAddVal(s, m2) );
    // We're done
    s.stop();
  }

  public void testFetchListData6() {
    //
    // g0 (g2)  (m1)
    // g1 ()    ()
    // g2 (m1)  ()
    //
    Subject         subj    = GrouperSubject.lookup( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
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
    Subject         subj    = GrouperSubject.lookup( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // Fetch g2
    GrouperGroup    g2    = GrouperGroup.load(s, stem2, extn2);
    Assert.assertNotNull(g2);
    // Fetch m1
    GrouperMember   m1      = GrouperMember.lookup("notblair", Grouper.DEF_SUBJ_TYPE);
    Assert.assertNotNull(m1);
    // Fetch g2 as m2
    GrouperMember   m2      = GrouperMember.lookup( g2.id(), "group");
    Assert.assertNotNull(m2);
    // Remove m1 from g2 Grouper.DEF_LIST_TYPE
    Assert.assertTrue( g2.listDelVal(s, m1) );
    // We're done
    s.stop();
  }

  public void testFetchListData7() {
    //
    // g0 (g2)  ()
    // g1 ()    ()
    // g2 ()    ()
    //
    Subject         subj    = GrouperSubject.lookup( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
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

    // TODO Assert details about the individual members

    // We're done
    s.stop(); 
  }

  public void testPrep0() {
    Subject         subj    = GrouperSubject.lookup( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // Fetch g0
    GrouperGroup g0 = GrouperGroup.load(s, stem0, extn0);
    Assert.assertNotNull(g0);
    // Fetch g1
    GrouperGroup g1 = GrouperGroup.load(s, stem1, extn1);
    Assert.assertNotNull(g1);
    // Fetch m1
    GrouperMember m0 = GrouperMember.lookup("blair", Grouper.DEF_SUBJ_TYPE);
    Assert.assertNotNull(m0);
    // Fetch g1 as m1
    GrouperMember m1 = GrouperMember.lookup( g1.id(), "group");
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
    Subject         subj    = GrouperSubject.lookup( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
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
    Subject         subj    = GrouperSubject.lookup( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
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
    Subject         subj    = GrouperSubject.lookup( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // Fetch g0
    GrouperGroup g0 = GrouperGroup.load(s, stem0, extn0);
    Assert.assertNotNull(g0);
    // Fetch g1
    GrouperGroup g1 = GrouperGroup.load(s, stem1, extn1);
    Assert.assertNotNull(g1);
    // Fetch g1 as m1
    GrouperMember m1 = GrouperMember.lookup( g1.id(), "group");
    Assert.assertNotNull(m1);
    // Remove g1 from g0
    Assert.assertTrue( g0.listDelVal(s, m1) );
    // We're done
    s.stop();
  }

  public void testGroupDel0() {
    Subject         subj    = GrouperSubject.lookup( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // Fetch g1
    GrouperGroup g1 = GrouperGroup.load(s, stem1, extn1);
    Assert.assertNotNull(g1);
    // Fail to delete g1 as it still has a member 
    Assert.assertFalse(GrouperGroup.delete(s, g1) );
    // We're done
    s.stop();
  }

  public void testPrep3() {
    Subject         subj    = GrouperSubject.lookup( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // Fetch g1
    GrouperGroup g1 = GrouperGroup.load(s, stem1, extn1);
    Assert.assertNotNull(g1);
    // Fetch m0
    GrouperMember m0 = GrouperMember.lookup("blair", Grouper.DEF_SUBJ_TYPE);
    Assert.assertNotNull(m0);
    // Remove m0 from g1
    Assert.assertTrue( g1.listDelVal(s, m0) );
    // We're done
    s.stop();
  }

  public void testGroupDel1() {
    Subject         subj    = GrouperSubject.lookup( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
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
    Subject         subj    = GrouperSubject.lookup( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
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
    Subject         subj  = GrouperSubject.lookup( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
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
    Subject         subj    = GrouperSubject.lookup( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
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

}

