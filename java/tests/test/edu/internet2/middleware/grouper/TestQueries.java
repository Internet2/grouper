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


  public void testQueryInstantiate() {
    Subject subj = null;
    try {
      subj = SubjectFactory.getSubject(Constants.rootI, Constants.rootT);
    } catch (SubjectNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    GrouperSession s = GrouperSession.start(subj);
    GrouperQuery q = new GrouperQuery(s);
    Assert.assertNotNull(q);
    Assert.assertTrue(Constants.KLASS_GQ.equals( q.getClass().getName() ) );
    s.stop();
  }

  //
  // gC -> gA 
  //
  public void testQueryMemAllEffAndImm() {
    Subject subj = null;
    try {
      subj = SubjectFactory.getSubject(Constants.rootI, Constants.rootT);
    } catch (SubjectNotFoundException e1) {
      // TODO Auto-generated catch block
      e1.printStackTrace();
    }
    GrouperSession s = GrouperSession.start(subj);

    // Create ns0
    GrouperStem ns0 = GrouperStem.create(
                         s, Constants.ns0s, Constants.ns0e
                       );
    // Create gA
    GrouperGroup gA = GrouperGroup.create(
                         s, Constants.gBs, Constants.gAe
                       );
    // Create gB
    GrouperGroup gB = GrouperGroup.create(
                         s, Constants.gBs, Constants.gBe
                       );
    // Create gC
    GrouperGroup gC = GrouperGroup.create(
                         s, Constants.gCs, Constants.gCe
                       );
    // Add gC to gA
    try {
      gA.listAddVal(gC.toMember());
    } catch (RuntimeException e) {
      Assert.fail("add gC to gA");
    }

    // Create query object
    GrouperQuery q = new GrouperQuery(s);

    // We want MEM_ALL
    try {
      Assert.assertTrue("MEM_ALL true", q.membershipType(Grouper.MEM_ALL) );
      List vals = q.query();
      Assert.assertTrue("vals == 1", vals.size() == 1);
      Assert.assertTrue(
        "right class", 
        Constants.KLASS_GL.equals(vals.get(0).getClass().getName())
      );
    } catch (GrouperException e) {
      Assert.fail("Exception: MEM_ALL");
    }

    // We want MEM_IMM
    try {
      Assert.assertTrue("MEM_IMM true",  q.membershipType(Grouper.MEM_IMM) );
      List vals = q.query();
      Assert.assertTrue("imm vals == 1", vals.size() == 1 );
      Assert.assertTrue(
        "right class", 
        Constants.KLASS_GL.equals(vals.get(0).getClass().getName())
      );
    } catch (GrouperException e) {
      Assert.fail("Exception: MEM_IMM");
    }

    // We want MEM_EFF
    try {
      Assert.assertFalse("MEM_EFF false", q.membershipType(Grouper.MEM_EFF) );
    } catch (GrouperException e) {
      Assert.fail("Exception: MEM_EFF");
    }

    // We're done
    s.stop(); 
  }

  public void testQueryDefGroupTypeAndMemAll() {
    Subject subj = null;
    try {
      subj = SubjectFactory.getSubject(Constants.rootI, Constants.rootT);
    } catch (SubjectNotFoundException e1) {
      // TODO Auto-generated catch block
      e1.printStackTrace();
    }
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // Create ns0
    GrouperStem ns0 = GrouperStem.create(
                         s, Constants.ns0s, Constants.ns0e
                       );
    // Create gA
    GrouperGroup gA = GrouperGroup.create(
                         s, Constants.gBs, Constants.gAe
                       );
    // Create gB
    GrouperGroup gB = GrouperGroup.create(
                         s, Constants.gBs, Constants.gBe
                       );
    // Create gC
    GrouperGroup gC = GrouperGroup.create(
                         s, Constants.gCs, Constants.gCe
                       );
    try {
      gA.listAddVal(gC.toMember());
    } catch (RuntimeException e) {
      Assert.fail("failed to add gC -> gA: " + e);
    }

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

  public void testQueryTypesAndMems() {
    Subject subj = null;
    try {
      subj = SubjectFactory.getSubject(Constants.rootI, Constants.rootT);
    } catch (SubjectNotFoundException e1) {
      // TODO Auto-generated catch block
      e1.printStackTrace();
    }
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // Create ns0
    GrouperStem ns0 = GrouperStem.create(
                         s, Constants.ns0s, Constants.ns0e
                       );
    // Create gA
    GrouperGroup gA = GrouperGroup.create(
                         s, Constants.gBs, Constants.gAe
                       );
    // Create gB
    GrouperGroup gB = GrouperGroup.create(
                         s, Constants.gBs, Constants.gBe
                       );
    // Create gC
    GrouperGroup gC = GrouperGroup.create(
                         s, Constants.gCs, Constants.gCe
                       );
    try {
      gA.listAddVal(gC.toMember());
    } catch (RuntimeException e) {
      Assert.fail("failed to add gC -> gA: " + e);
    }

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

  public void testQueryCreatedBeforeNow() {
    Subject subj = null;
    try {
      subj = SubjectFactory.getSubject(Constants.rootI, Constants.rootT);
    } catch (SubjectNotFoundException e1) {
      // TODO Auto-generated catch block
      e1.printStackTrace();
    }
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);
    // Create ns0
    GrouperStem ns0 = GrouperStem.create(
                         s, Constants.ns0s, Constants.ns0e
                       );
    // Create gA
    GrouperGroup gA = GrouperGroup.create(
                         s, Constants.gBs, Constants.gAe
                       );
    // Create gB
    GrouperGroup gB = GrouperGroup.create(
                         s, Constants.gBs, Constants.gBe
                       );
    // Create gC
    GrouperGroup gC = GrouperGroup.create(
                         s, Constants.gCs, Constants.gCe
                       );
    try {
      gA.listAddVal(gC.toMember());
    } catch (RuntimeException e) {
      Assert.fail("failed to add gC -> gA: " + e);
    }

    // Create query object
    GrouperQuery q = new GrouperQuery(s);
    // We want created before now
    try {
      Thread.sleep(500);
      Assert.assertTrue( q.createdBefore( new java.util.Date() ) );
      List vals = q.query();
      Assert.assertNotNull(vals);
      Assert.assertTrue( vals.size() == 1 );
    } catch (GrouperException e) {
      Assert.fail("Exception: createdBefore:NOW");
    } catch (InterruptedException e) {
      Assert.fail("Failed to sleep");
    }
    s.stop();
  }

  public void testQueryCreatedAfterNow() {
    Subject subj = null;
    try {
      subj = SubjectFactory.getSubject(Constants.rootI, Constants.rootT);
    } catch (SubjectNotFoundException e1) {
      // TODO Auto-generated catch block
      e1.printStackTrace();
    }
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);
    // Create ns0
    GrouperStem ns0 = GrouperStem.create(
                         s, Constants.ns0s, Constants.ns0e
                       );
    // Create gA
    GrouperGroup gA = GrouperGroup.create(
                         s, Constants.gBs, Constants.gAe
                       );
    // Create gB
    GrouperGroup gB = GrouperGroup.create(
                         s, Constants.gBs, Constants.gBe
                       );
    // Create gC
    GrouperGroup gC = GrouperGroup.create(
                         s, Constants.gCs, Constants.gCe
                       );
    try {
      gA.listAddVal(gC.toMember());
    } catch (RuntimeException e) {
      Assert.fail("failed to add gC -> gA: " + e);
    }

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

  public void testQueryModifedBeforeNow() {
    Subject subj = null;
    try {
      subj = SubjectFactory.getSubject(Constants.rootI, Constants.rootT);
    } catch (SubjectNotFoundException e1) {
      // TODO Auto-generated catch block
      e1.printStackTrace();
    }
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);
    // Create ns0
    GrouperStem ns0 = GrouperStem.create(
                         s, Constants.ns0s, Constants.ns0e
                       );
    // Create gA
    GrouperGroup gA = GrouperGroup.create(
                         s, Constants.gBs, Constants.gAe
                       );
    // Create gB
    GrouperGroup gB = GrouperGroup.create(
                         s, Constants.gBs, Constants.gBe
                       );
    // Create gC
    GrouperGroup gC = GrouperGroup.create(
                         s, Constants.gCs, Constants.gCe
                       );
    try {
      gA.listAddVal(gC.toMember());
    } catch (RuntimeException e) {
      Assert.fail("failed to add gC -> gA: " + e);
    }

    // Create query object
    GrouperQuery q = new GrouperQuery(s);
    // We want modified before now
    try {
      Thread.sleep(500);
      Assert.assertTrue( q.modifiedBefore( new java.util.Date() ) );
      List vals = q.query();
      Assert.assertNotNull(vals);
      Assert.assertTrue( vals.size() == 1 );
    } catch (InterruptedException e) {
      Assert.fail("Failed to sleep");
    } catch (GrouperException e) {
      Assert.fail("Exception: modifiedBefore:NOW");
    }
    s.stop();
  }

  public void testQueryModifiedAfterNow() {
    Subject subj = null;
    try {
      subj = SubjectFactory.getSubject(Constants.rootI, Constants.rootT);
    } catch (SubjectNotFoundException e1) {
      // TODO Auto-generated catch block
      e1.printStackTrace();
    }
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);
    // Create ns0
    GrouperStem ns0 = GrouperStem.create(
                         s, Constants.ns0s, Constants.ns0e
                       );
    // Create gA
    GrouperGroup gA = GrouperGroup.create(
                         s, Constants.gBs, Constants.gAe
                       );
    // Create gB
    GrouperGroup gB = GrouperGroup.create(
                         s, Constants.gBs, Constants.gBe
                       );
    // Create gC
    GrouperGroup gC = GrouperGroup.create(
                         s, Constants.gCs, Constants.gCe
                       );
    try {
      gA.listAddVal(gC.toMember());
    } catch (RuntimeException e) {
      Assert.fail("failed to add gC -> gA: " + e);
    }

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

}

