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


public class TestGroups extends TestCase {

  private String  klass   = "edu.internet2.middleware.grouper.GrouperGroup";

  private String  stem0   = "stem.0";
  private String  extn0   = "extn.0";
  private String  stem1   = "stem.1";
  private String  extn1   = "extn.1";
  private String  stem2   = "stem.2";
  private String  extn2   = "extn.2";
  private String  stem3   = "stem.0";
  private String  extn3   = "extn.3";
  private String  stem4   = "stem.0";
  private String  extn4   = "extn.4";
  

  public TestGroups(String name) {
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
  

  // Fetch non-existent groups
  public void testGroupsExistFalse() {
    GrouperSession  s     = new GrouperSession();
    Subject         subj  = GrouperSubject.lookup( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    s.start(subj);

    // Confirm that groups don't exist
    GrouperGroup    g0  = GrouperGroup.lookup(s, stem0, extn0);
    Assert.assertNull(g0);
    GrouperGroup    g1  = GrouperGroup.lookup(s, stem1, extn1);
    Assert.assertNull(g1);
    GrouperGroup    g2  = GrouperGroup.lookup(s, stem2, extn2);
    Assert.assertNull(g2);
    GrouperGroup    g3  = GrouperGroup.lookup(s, stem3, extn3);
    Assert.assertNull(g3);
    GrouperGroup    g4  = GrouperGroup.lookup(s, stem4, extn4);
    Assert.assertNull(g4);

    // We're done
    s.stop();
  }

  public void testCreateG0() {
    GrouperSession  s     = new GrouperSession();
    Subject         subj  = GrouperSubject.lookup( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    s.start(subj);
    // Create g0
    String stem = stem0;
    String extn = extn0;
    GrouperGroup ns = GrouperGroup.create(s, stem, extn);
    Assert.assertNotNull(ns);
    Assert.assertTrue( klass.equals( ns.getClass().getName() ) );
    Assert.assertNotNull( ns.type() );
    Assert.assertTrue( ns.type().equals(Grouper.DEF_GROUP_TYPE) ); 
    Assert.assertNotNull( ns.attribute("stem") );
    Assert.assertTrue( ns.attribute("stem").value().equals(stem) );
    Assert.assertNotNull( ns.attribute("extension") );
    Assert.assertTrue( ns.attribute("extension").value().equals(extn) );
    s.stop();
  }

  public void testCreateG1() {
    GrouperSession  s     = new GrouperSession();
    Subject         subj  = GrouperSubject.lookup( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    s.start(subj);
    // Create g1
    String stem = stem1;
    String extn = extn1;
    GrouperGroup ns = GrouperGroup.create(s, stem, extn);
    Assert.assertNotNull(ns);
    Assert.assertTrue( klass.equals( ns.getClass().getName() ) );
    Assert.assertNotNull( ns.type() );
    Assert.assertTrue( ns.type().equals(Grouper.DEF_GROUP_TYPE) ); 
    Assert.assertNotNull( ns.attribute("stem") );
    Assert.assertTrue( ns.attribute("stem").value().equals(stem) );
    Assert.assertNotNull( ns.attribute("extension") );
    Assert.assertTrue( ns.attribute("extension").value().equals(extn) );
    s.stop();
  }

  public void testCreateG2() {
    GrouperSession  s     = new GrouperSession();
    Subject         subj  = GrouperSubject.lookup( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    s.start(subj);
    // Create g2
    String stem = stem2;
    String extn = extn2;
    GrouperGroup ns = GrouperGroup.create(s, stem, extn);
    Assert.assertNotNull(ns);
    Assert.assertTrue( klass.equals( ns.getClass().getName() ) );
    Assert.assertNotNull( ns.type() );
    Assert.assertTrue( ns.type().equals(Grouper.DEF_GROUP_TYPE) ); 
    Assert.assertNotNull( ns.attribute("stem") );
    Assert.assertTrue( ns.attribute("stem").value().equals(stem) );
    Assert.assertNotNull( ns.attribute("extension") );
    Assert.assertTrue( ns.attribute("extension").value().equals(extn) );
    s.stop();
  }

  public void testCreateG3() {
    GrouperSession  s     = new GrouperSession();
    Subject         subj  = GrouperSubject.lookup( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    s.start(subj);
    // Create g3
    String stem = stem3;
    String extn = extn3;
    GrouperGroup ns = GrouperGroup.create(s, stem, extn);
    Assert.assertNotNull(ns);
    Assert.assertTrue( klass.equals( ns.getClass().getName() ) );
    Assert.assertNotNull( ns.type() );
    Assert.assertTrue( ns.type().equals(Grouper.DEF_GROUP_TYPE) ); 
    Assert.assertNotNull( ns.attribute("stem") );
    Assert.assertTrue( ns.attribute("stem").value().equals(stem) );
    Assert.assertNotNull( ns.attribute("extension") );
    Assert.assertTrue( ns.attribute("extension").value().equals(extn) );
    s.stop();
  }

  public void testCreateG4() {
    GrouperSession  s     = new GrouperSession();
    Subject         subj  = GrouperSubject.lookup( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    s.start(subj);
    // Create g4
    String stem = stem4;
    String extn = extn4;
    GrouperGroup ns = GrouperGroup.create(s, stem, extn);
    Assert.assertNotNull(ns);
    Assert.assertTrue( klass.equals( ns.getClass().getName() ) );
    Assert.assertNotNull( ns.type() );
    Assert.assertTrue( ns.type().equals(Grouper.DEF_GROUP_TYPE) ); 
    Assert.assertNotNull( ns.attribute("stem") );
    Assert.assertTrue( ns.attribute("stem").value().equals(stem) );
    Assert.assertNotNull( ns.attribute("extension") );
    Assert.assertTrue( ns.attribute("extension").value().equals(extn) );
    s.stop();
  }

  public void testFetchG0() {
    GrouperSession  s     = new GrouperSession();
    Subject         subj  = GrouperSubject.lookup( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    s.start(subj);
    // Fetch g0
    GrouperGroup g = GrouperGroup.lookup(s, stem0, extn0);
    Assert.assertNotNull(g);
    Assert.assertTrue( klass.equals( g.getClass().getName() ) );
    Assert.assertNotNull( g.id() );
    Assert.assertNotNull( g.type() );
    Assert.assertNotNull( g.attribute("stem") );
    Assert.assertTrue( g.attribute("stem").value().equals(stem0) );
    Assert.assertNotNull( g.attribute("extension") );
    Assert.assertTrue( g.attribute("extension").value().equals(extn0) );
    Assert.assertNull( g.createSource() );
    Assert.assertNotNull( g.createSubject() );
    Assert.assertNotNull( g.createTime() );
    Assert.assertNull( g.modifySource() );
    Assert.assertNull( g.modifySubject() );
    Assert.assertNull( g.modifyTime() );
    s.stop();
  }

  public void testFetchG1() {
    GrouperSession  s     = new GrouperSession();
    Subject         subj  = GrouperSubject.lookup( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    s.start(subj);
    // Fetch g1
    GrouperGroup g = GrouperGroup.lookup(s, stem1, extn1);
    Assert.assertNotNull(g);
    Assert.assertTrue( klass.equals( g.getClass().getName() ) );
    Assert.assertNotNull( g.id() );
    Assert.assertNotNull( g.type() );
    Assert.assertNotNull( g.attribute("stem") );
    Assert.assertTrue( g.attribute("stem").value().equals(stem1) );
    Assert.assertNotNull( g.attribute("extension") );
    Assert.assertTrue( g.attribute("extension").value().equals(extn1) );
    Assert.assertNull( g.createSource() );
    Assert.assertNotNull( g.createSubject() );
    Assert.assertNotNull( g.createTime() );
    Assert.assertNull( g.modifySource() );
    Assert.assertNull( g.modifySubject() );
    Assert.assertNull( g.modifyTime() );
    s.stop();
  }

  public void testFetchG2() {
    GrouperSession  s     = new GrouperSession();
    Subject         subj  = GrouperSubject.lookup( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    s.start(subj);
    // Fetch g2
    GrouperGroup g = GrouperGroup.lookup(s, stem2, extn2);
    Assert.assertNotNull(g);
    Assert.assertTrue( klass.equals( g.getClass().getName() ) );
    Assert.assertNotNull( g.id() );
    Assert.assertNotNull( g.type() );
    Assert.assertNotNull( g.attribute("stem") );
    Assert.assertTrue( g.attribute("stem").value().equals(stem2) );
    Assert.assertNotNull( g.attribute("extension") );
    Assert.assertTrue( g.attribute("extension").value().equals(extn2) );
    Assert.assertNull( g.createSource() );
    Assert.assertNotNull( g.createSubject() );
    Assert.assertNotNull( g.createTime() );
    Assert.assertNull( g.modifySource() );
    Assert.assertNull( g.modifySubject() );
    Assert.assertNull( g.modifyTime() );
    s.stop();
  }

  public void testFetchG3() {
    GrouperSession  s     = new GrouperSession();
    Subject         subj  = GrouperSubject.lookup( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    s.start(subj);
    // Fetch g3
    GrouperGroup g = GrouperGroup.lookup(s, stem3, extn3);
    Assert.assertNotNull(g);
    Assert.assertTrue( klass.equals( g.getClass().getName() ) );
    Assert.assertNotNull( g.id() );
    Assert.assertNotNull( g.type() );
    Assert.assertNotNull( g.attribute("stem") );
    Assert.assertTrue( g.attribute("stem").value().equals(stem3) );
    Assert.assertNotNull( g.attribute("extension") );
    Assert.assertTrue( g.attribute("extension").value().equals(extn3) );
    Assert.assertNull( g.createSource() );
    Assert.assertNotNull( g.createSubject() );
    Assert.assertNotNull( g.createTime() );
    Assert.assertNull( g.modifySource() );
    Assert.assertNull( g.modifySubject() );
    Assert.assertNull( g.modifyTime() );
    s.stop();
  }

  public void testFetchG4() {
    GrouperSession  s     = new GrouperSession();
    Subject         subj  = GrouperSubject.lookup( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    s.start(subj);
    // Fetch g4
    GrouperGroup g = GrouperGroup.lookup(s, stem4, extn4);
    Assert.assertNotNull(g);
    Assert.assertTrue( klass.equals( g.getClass().getName() ) );
    Assert.assertNotNull( g.id() );
    Assert.assertNotNull( g.type() );
    Assert.assertNotNull( g.attribute("stem") );
    Assert.assertTrue( g.attribute("stem").value().equals(stem4) );
    Assert.assertNotNull( g.attribute("extension") );
    Assert.assertTrue( g.attribute("extension").value().equals(extn4) );
    Assert.assertNull( g.createSource() );
    Assert.assertNotNull( g.createSubject() );
    Assert.assertNotNull( g.createTime() );
    Assert.assertNull( g.modifySource() );
    Assert.assertNull( g.modifySubject() );
    Assert.assertNull( g.modifyTime() );
    s.stop();
  }

  // Delete a group
  public void testDeleteGroups0() {
    GrouperSession  s     = new GrouperSession();
    Subject         subj  = GrouperSubject.lookup( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    s.start(subj);

    // Delete g4
    GrouperGroup  g4 = GrouperGroup.lookup(s, stem4, extn4);
    Assert.assertNotNull(g4);
    Assert.assertTrue( GrouperGroup.delete(s, g4) );

    // We're done
    s.stop();
  }

  // Fetch valid groups
  public void testFetchGroups1() {
    GrouperSession  s     = new GrouperSession();
    Subject         subj  = GrouperSubject.lookup( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    s.start(subj);

    // Fetch the groups
    // g0
    GrouperGroup    g0  = GrouperGroup.lookup(s, stem0, extn0);
    Assert.assertNotNull(g0);
    Assert.assertTrue( klass.equals( g0.getClass().getName() ) );
    Assert.assertNotNull( g0.id() );
    Assert.assertNotNull( g0.type() );
    Assert.assertNotNull( g0.attribute("stem") );
    Assert.assertTrue( g0.attribute("stem").value().equals(stem0) );
    Assert.assertNotNull( g0.attribute("extension") );
    Assert.assertTrue( g0.attribute("extension").value().equals(extn0) );
    // g1
    GrouperGroup    g1  = GrouperGroup.lookup(s, stem1, extn1);
    Assert.assertNotNull(g1);
    Assert.assertTrue( klass.equals( g1.getClass().getName() ) );
    Assert.assertNotNull( g1.id() );
    Assert.assertNotNull( g1.type() );
    Assert.assertNotNull( g1.attribute("stem") );
    Assert.assertTrue( g1.attribute("stem").value().equals(stem1) );
    Assert.assertNotNull( g1.attribute("extension") );
    Assert.assertTrue( g1.attribute("extension").value().equals(extn1) );
    // g2
    GrouperGroup    g2  = GrouperGroup.lookup(s, stem2, extn2);
    Assert.assertNotNull(g2);
    Assert.assertTrue( klass.equals( g2.getClass().getName() ) );
    Assert.assertNotNull( g2.id() );
    Assert.assertNotNull( g2.type() );
    Assert.assertNotNull( g2.attribute("stem") );
    Assert.assertTrue( g2.attribute("stem").value().equals(stem2) );
    Assert.assertNotNull( g2.attribute("extension") );
    Assert.assertTrue( g2.attribute("extension").value().equals(extn2) );
    // g3
    GrouperGroup    g3  = GrouperGroup.lookup(s, stem3, extn3);
    Assert.assertNotNull(g3);
    Assert.assertTrue( klass.equals( g3.getClass().getName() ) );
    Assert.assertNotNull( g3.id() );
    Assert.assertNotNull( g3.type() );
    Assert.assertNotNull( g3.attribute("stem") );
    Assert.assertTrue( g3.attribute("stem").value().equals(stem3) );
    Assert.assertNotNull( g3.attribute("extension") );
    Assert.assertTrue( g3.attribute("extension").value().equals(extn3) );
    // g4
    GrouperGroup    g4  = GrouperGroup.lookup(s, stem4, extn4);
    Assert.assertNull(g4);

    // We're done
    s.stop();
  }

  public void testCreateG5() {
    GrouperSession  s     = new GrouperSession();
    Subject         subj  = GrouperSubject.lookup( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    s.start(subj);
    // Attempt to recreate g0
    String stem = stem0;
    String extn = extn0;
    GrouperGroup g = GrouperGroup.create(s, stem, extn);
    Assert.assertNull(g);
    s.stop();
  }

}

