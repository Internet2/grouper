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


public class TestQueries extends TestCase {

  private String extn0    = "extn.0";
  private String extn1    = "extn.1";
  private String extn2    = "extn.2";
  private String id0      = Constants.mem0I;
  private String id1      = Constants.mem1I;
  private String type0    = Constants.mem0T;
  private String type1    = Constants.mem1T;
  private String stem0    = "stem.0";
  private String stem1    = "stem.1";
  private String stem2    = "stem.2";

  public TestQueries(String name) {
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


  // Test requirements for other *real* tests
  public void testRequirements() {
    Subject subj = GrouperSubject.load(Constants.rootI, Constants.rootT);
    Assert.assertNotNull(subj);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // Fetch the groups
    // g0
    GrouperGroup    g0  = GrouperGroup.load(s, stem0, extn0);
    Assert.assertNotNull(g0);
    // g1
    GrouperGroup    g1  = GrouperGroup.load(s, stem1, extn1);
    Assert.assertNotNull(g1);
    // g2
    GrouperGroup    g2  = GrouperGroup.load(s, stem2, extn2);
    Assert.assertNotNull(g2);
    // Fetch the members
    // Fetch Member 0
    GrouperMember   m0      = GrouperMember.load(s, id0, type0);
    Assert.assertNotNull(m0);
    // Fetch Member 1
    GrouperMember   m1      = GrouperMember.load(s, id1, type1);
    Assert.assertNotNull(m1);
    // We're done
    s.stop();
  }

  public void testQueryInstantiate() {
    Subject subj = GrouperSubject.load(Constants.rootI, Constants.rootT);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    GrouperQuery    q0    = new GrouperQuery(s);
    Assert.assertNotNull(q0);
    Assert.assertTrue(Constants.KLASS_GQ.equals( q0.getClass().getName() ) );
    s.stop();
  }

  public void testQuery0() {
    //
    // g0 (g2)  ()
    // g1 ()    ()
    // g2 ()    ()
    //
    Subject subj = GrouperSubject.load(Constants.rootI, Constants.rootT);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);


    // Create query object
    GrouperQuery q = new GrouperQuery(s);

    // We want MEM_ALL
    try {
      Assert.assertTrue( q.membershipType(Grouper.MEM_ALL) );
      List vals = q.query();
      Assert.assertNotNull(vals);
      Assert.assertTrue( vals.size() == 1 );
      Object obj = vals.get(0);
      Assert.assertNotNull(obj);
      Assert.assertTrue( Constants.KLASS_GL.equals( obj.getClass().getName() ) );
    } catch (GrouperException e) {
      Assert.fail("Exception: MEM_ALL");
    }

    // We want MEM_EFF
    try {
      Assert.assertFalse( q.membershipType(Grouper.MEM_EFF) );
    } catch (GrouperException e) {
      Assert.fail("Exception: MEM_EFF");
    }

    // We want MEM_IMM
    try {
      Assert.assertTrue( q.membershipType(Grouper.MEM_IMM) );
      List vals = q.query();
      Assert.assertNotNull(vals);
      Assert.assertTrue( vals.size() == 1 );
      Object obj = vals.get(0);
      Assert.assertNotNull(obj);
      Assert.assertTrue( Constants.KLASS_GL.equals( obj.getClass().getName() ) );
    } catch (GrouperException e) {
      Assert.fail("Exception: MEM_IMM");
    }

    // We're done
    s.stop(); 
  }

  public void testQuery1() {
    //
    // g0 (g2)  ()
    // g1 ()    ()
    // g2 ()    ()
    //
    Subject subj = GrouperSubject.load(Constants.rootI, Constants.rootT);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);


    // Create query object
    GrouperQuery q = new GrouperQuery(s);

    // We want DEF_GROUP_TYPE + MEM_ALL
    try {
      // First DEF_GROUP_TYPE
      Assert.assertTrue( q.groupType(Grouper.DEF_GROUP_TYPE) );
      List vals = q.query();
      Assert.assertNotNull(vals);
      Assert.assertTrue( vals.size() == 1 );
      Object obj = vals.get(0);
      Assert.assertNotNull(obj);
      Assert.assertTrue( Constants.KLASS_GL.equals( obj.getClass().getName() ) );
      // Now with MEM_ALL
      Assert.assertTrue( q.membershipType(Grouper.MEM_ALL) );
      vals = q.query();
      Assert.assertNotNull(vals);
      Assert.assertTrue( vals.size() == 1 );
      obj = vals.get(0);
      Assert.assertNotNull(obj);
      Assert.assertTrue( Constants.KLASS_GL.equals( obj.getClass().getName() ) );
    } catch (GrouperException e) {
      Assert.fail("Exception: DEF_GROUP_TYPE,MEM_ALL");
    }

    // We want DEF_GROUP_TYPE + MEM_EFF
    try {
      // Clear membership query filter
      q.clear("membershipType");
      // First DEF_GROUP_TYPE
      Assert.assertTrue( q.groupType(Grouper.DEF_GROUP_TYPE) );
      List vals = q.query();
      Assert.assertNotNull(vals);
      Assert.assertTrue( vals.size() == 1 );
      Object obj = vals.get(0);
      Assert.assertNotNull(obj);
      Assert.assertTrue( Constants.KLASS_GL.equals( obj.getClass().getName() ) );
      // Now with MEM_EFF
      Assert.assertFalse( q.membershipType(Grouper.MEM_EFF) );
      vals = q.query();
      Assert.assertNotNull(vals);
      Assert.assertTrue( vals.size() == 0 );
    } catch (GrouperException e) {
      Assert.fail("Exception: DEF_GROUP_TYPE,MEM_EFF");
    }

    // We want DEF_GROUP_TYPE + MEM_IMM
    try {
      // Clear membership query filter
      q.clear("membershipType");
      // First DEF_GROUP_TYPE
      Assert.assertTrue( q.groupType(Grouper.DEF_GROUP_TYPE) );
      List vals = q.query();
      Assert.assertNotNull(vals);
      Assert.assertTrue( vals.size() == 1 );
      Object obj = vals.get(0);
      Assert.assertNotNull(obj);
      Assert.assertTrue( Constants.KLASS_GL.equals( obj.getClass().getName() ) );
      // Now with MEM_IMM
      Assert.assertTrue( q.membershipType(Grouper.MEM_IMM) );
      vals = q.query();
      Assert.assertNotNull(vals);
      Assert.assertTrue( vals.size() == 1 );
      obj = vals.get(0);
      Assert.assertNotNull(obj);
      Assert.assertTrue( Constants.KLASS_GL.equals( obj.getClass().getName() ) );
    } catch (GrouperException e) {
      Assert.fail("Exception: DEF_GROUP_TYPE,MEM_IMM");
    }

    // We're done
    s.stop(); 
  }

  public void testQuery2() {
    //
    // g0 (g2)  ()
    // g1 ()    ()
    // g2 ()    ()
    //
    Subject subj = GrouperSubject.load(Constants.rootI, Constants.rootT);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);


    // Create query object
    GrouperQuery q = new GrouperQuery(s);

    // We want NS_TYPE + MEM_ALL
    try {
      // First NS_TYPE
      Assert.assertFalse( q.groupType(Grouper.NS_TYPE) );
      // Now MEM_ALL
      Assert.assertTrue( q.membershipType(Grouper.MEM_ALL) );
      List vals = q.query();
      Assert.assertNotNull(vals);
      Assert.assertTrue( vals.size() == 0 );
    } catch (GrouperException e) {
      Assert.fail("Exception: DEF_GROUP_TYPE,MEM_ALL");
    }

    // We want DEF_GROUP_TYPE + MEM_EFF
    try {
      // Clear all query filters
      q.clear();
      // First NS_TYPE
      Assert.assertFalse( q.groupType(Grouper.NS_TYPE) );
      // Now MEM_EFF
      Assert.assertFalse( q.membershipType(Grouper.MEM_EFF) );
    } catch (GrouperException e) {
      Assert.fail("Exception: DEF_GROUP_TYPE,MEM_EFF");
    }

    // We want DEF_GROUP_TYPE + MEM_IMM
    try {
      // Clear all query filters
      q.clear();
      // First NS_TYPE
      Assert.assertFalse( q.groupType(Grouper.NS_TYPE) );
      // Now MEM_IMM
      Assert.assertTrue( q.membershipType(Grouper.MEM_IMM) );
      List vals = q.query();
      Assert.assertNotNull(vals);
      Assert.assertTrue( vals.size() == 0 );
    } catch (GrouperException e) {
      Assert.fail("Exception: DEF_GROUP_TYPE,MEM_IMM");
    }

    // We're done
    s.stop(); 
  }

  public void testQuery3() {
    //
    // g0 (g2)  ()
    // g1 ()    ()
    // g2 ()    ()
    //
    Subject subj = GrouperSubject.load(Constants.rootI, Constants.rootT);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);
    // Create query object
    GrouperQuery q = new GrouperQuery(s);
    // We want created before now
    try {
      Assert.assertTrue( q.createdBefore( new java.util.Date() ) );
      List vals = q.query();
      Assert.assertNotNull(vals);
      Assert.assertTrue( vals.size() == 1 );
    } catch (GrouperException e) {
      Assert.fail("Exception: createdBefore:NOW");
    }
    s.stop();
  }

  public void testQuery4() {
    //
    // g0 (g2)  ()
    // g1 ()    ()
    // g2 ()    ()
    //
    Subject subj = GrouperSubject.load(Constants.rootI, Constants.rootT);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);
    // Create query object
    GrouperQuery q = new GrouperQuery(s);
    // We want created after now
    try {
      Assert.assertFalse( q.createdAfter( new java.util.Date() ) );
      List vals = q.query();
      Assert.assertNotNull(vals);
      Assert.assertTrue( vals.size() == 0 );
    } catch (GrouperException e) {
      Assert.fail("Exception: createdAfter:NOW");
    }
    s.stop();
  }

  public void testQuery5() {
    //
    // g0 (g2)  ()
    // g1 ()    ()
    // g2 ()    ()
    //
    Subject subj = GrouperSubject.load(Constants.rootI, Constants.rootT);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);
    // Create query object
    GrouperQuery q = new GrouperQuery(s);
    // We want modified before now
    try {
      Assert.assertTrue( q.modifiedBefore( new java.util.Date() ) );
      List vals = q.query();
      Assert.assertNotNull(vals);
      Assert.assertTrue( vals.size() == 1 );
    } catch (GrouperException e) {
      Assert.fail("Exception: modifiedBefore:NOW");
    }
    s.stop();
  }

  public void testQuery6() {
    //
    // g0 (g2)  ()
    // g1 ()    ()
    // g2 ()    ()
    //
    Subject subj = GrouperSubject.load(Constants.rootI, Constants.rootT);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);
    // Create query object
    GrouperQuery q = new GrouperQuery(s);
    // We want modified after now
    try {
      Assert.assertFalse( q.modifiedAfter( new java.util.Date() ) );
      List vals = q.query();
      Assert.assertNotNull(vals);
      Assert.assertTrue( vals.size() == 0 );
    } catch (GrouperException e) {
      Assert.fail("Exception: modifiedAfter:NOW");
    }
    s.stop();
  }

  public void testQuery7() {
    //
    // g0 (g2)  ()
    // g1 ()    ()
    // g2 ()    ()
    //
    Subject subj = GrouperSubject.load(Constants.rootI, Constants.rootT);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);
    // Create query object
    GrouperQuery q = new GrouperQuery(s);
    // We want created before now
    try {
      Assert.assertTrue( q.createdBefore( new java.util.Date() ) );
      Assert.assertTrue( q.groupType(Grouper.DEF_GROUP_TYPE) );
      List vals = q.query();
      Assert.assertNotNull(vals);
      Assert.assertTrue( vals.size() == 1 );
    } catch (GrouperException e) {
      Assert.fail("Exception: createdBefore:NOW");
    }
    s.stop();
  }

  public void testQuery8() {
    //
    // g0 (g2)  ()
    // g1 ()    ()
    // g2 ()    ()
    //
    Subject subj = GrouperSubject.load(Constants.rootI, Constants.rootT);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);
    // Create query object
    GrouperQuery q = new GrouperQuery(s);
    // We want created after now
    try {
      Assert.assertFalse( q.createdAfter( new java.util.Date() ) );
      Assert.assertTrue( q.groupType(Grouper.DEF_GROUP_TYPE) );
      List vals = q.query();
      Assert.assertNotNull(vals);
      Assert.assertTrue( vals.size() == 0 );
    } catch (GrouperException e) {
      Assert.fail("Exception: createdAfter:NOW");
    }
    s.stop();
  }

  public void testQuery9() {
    //
    // g0 (g2)  ()
    // g1 ()    ()
    // g2 ()    ()
    //
    Subject subj = GrouperSubject.load(Constants.rootI, Constants.rootT);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);
    // Create query object
    GrouperQuery q = new GrouperQuery(s);
    // We want modified before now
    try {
      Assert.assertTrue( q.modifiedBefore( new java.util.Date() ) );
      Assert.assertTrue( q.groupType(Grouper.DEF_GROUP_TYPE) );
      List vals = q.query();
      Assert.assertNotNull(vals);
      Assert.assertTrue( vals.size() == 1 );
    } catch (GrouperException e) {
      Assert.fail("Exception: modifiedBefore:NOW");
    }
    s.stop();
  }

  public void testQuery10() {
    //
    // g0 (g2)  ()
    // g1 ()    ()
    // g2 ()    ()
    //
    Subject subj = GrouperSubject.load(Constants.rootI, Constants.rootT);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);
    // Create query object
    GrouperQuery q = new GrouperQuery(s);
    // We want modified after now
    try {
      Assert.assertFalse( q.modifiedAfter( new java.util.Date() ) );
      Assert.assertTrue( q.groupType(Grouper.DEF_GROUP_TYPE) );
      List vals = q.query();
      Assert.assertNotNull(vals);
      Assert.assertTrue( vals.size() == 0 );
    } catch (GrouperException e) {
      Assert.fail("Exception: modifiedAfter:NOW");
    }
    s.stop();
  }

}

