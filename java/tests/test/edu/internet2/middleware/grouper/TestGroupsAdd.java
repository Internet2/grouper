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

  private String  ns_stem0   = Grouper.NS_ROOT;
  private String  ns_extn0   = "stem.0";
  private String  ns_stem00  = "stem.0";
  private String  ns_extn00  = "stem.0.0";

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
  private String  stem5   = "stem.0";
  private String  extn5   = "ext:n.5";
  

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
    Subject         subj  = GrouperSubject.load( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);


    // Confirm that groups don't exist
    GrouperGroup    g0  = GrouperGroup.load(s, stem0, extn0);
    Assert.assertNull(g0);
    GrouperGroup    g1  = GrouperGroup.load(s, stem1, extn1);
    Assert.assertNull(g1);
    GrouperGroup    g2  = GrouperGroup.load(s, stem2, extn2);
    Assert.assertNull(g2);
    GrouperGroup    g3  = GrouperGroup.load(s, stem3, extn3);
    Assert.assertNull(g3);
    GrouperGroup    g4  = GrouperGroup.load(s, stem4, extn4);
    Assert.assertNull(g4);

    // We're done
    s.stop();
  }

  public void testCreateG0() {
    Subject         subj  = GrouperSubject.load( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // Create g0
    String stem = stem0;
    String extn = extn0;
    GrouperGroup ns = GrouperGroup.create(s, stem, extn);
    Assert.assertNotNull(ns);
    Assert.assertTrue( klass.equals( ns.getClass().getName() ) );
    Assert.assertNotNull( ns.type() );
    Assert.assertTrue( ns.type().equals(Grouper.DEF_GROUP_TYPE) ); 
    Assert.assertNotNull( ns.attribute(s, "stem") );
    Assert.assertTrue( ns.attribute(s, "stem").value().equals(stem) );
    Assert.assertNotNull( ns.attribute(s, "extension") );
    Assert.assertTrue( ns.attribute(s, "extension").value().equals(extn) );
    s.stop();
  }

  public void testCreateG1() {
    Subject         subj  = GrouperSubject.load( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // Create g1
    String stem = stem1;
    String extn = extn1;
    GrouperGroup ns = GrouperGroup.create(s, stem, extn);
    Assert.assertNotNull(ns);
    Assert.assertTrue( klass.equals( ns.getClass().getName() ) );
    Assert.assertNotNull( ns.type() );
    Assert.assertTrue( ns.type().equals(Grouper.DEF_GROUP_TYPE) ); 
    Assert.assertNotNull( ns.attribute(s, "stem") );
    Assert.assertTrue( ns.attribute(s, "stem").value().equals(stem) );
    Assert.assertNotNull( ns.attribute(s, "extension") );
    Assert.assertTrue( ns.attribute(s, "extension").value().equals(extn) );
    s.stop();
  }

  public void testCreateG2() {
    Subject         subj  = GrouperSubject.load( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // Create g2
    String stem = stem2;
    String extn = extn2;
    GrouperGroup ns = GrouperGroup.create(s, stem, extn);
    Assert.assertNotNull(ns);
    Assert.assertTrue( klass.equals( ns.getClass().getName() ) );
    Assert.assertNotNull( ns.type() );
    Assert.assertTrue( ns.type().equals(Grouper.DEF_GROUP_TYPE) ); 
    Assert.assertNotNull( ns.attribute(s, "stem") );
    Assert.assertTrue( ns.attribute(s, "stem").value().equals(stem) );
    Assert.assertNotNull( ns.attribute(s, "extension") );
    Assert.assertTrue( ns.attribute(s, "extension").value().equals(extn) );
    s.stop();
  }

  public void testCreateG3() {
    Subject         subj  = GrouperSubject.load( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // Create g3
    String stem = stem3;
    String extn = extn3;
    GrouperGroup ns = GrouperGroup.create(s, stem, extn);
    Assert.assertNotNull(ns);
    Assert.assertTrue( klass.equals( ns.getClass().getName() ) );
    Assert.assertNotNull( ns.type() );
    Assert.assertTrue( ns.type().equals(Grouper.DEF_GROUP_TYPE) ); 
    Assert.assertNotNull( ns.attribute(s, "stem") );
    Assert.assertTrue( ns.attribute(s, "stem").value().equals(stem) );
    Assert.assertNotNull( ns.attribute(s, "extension") );
    Assert.assertTrue( ns.attribute(s, "extension").value().equals(extn) );
    s.stop();
  }

  public void testCreateG4() {
    Subject         subj  = GrouperSubject.load( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // Create g4
    String stem = stem4;
    String extn = extn4;
    GrouperGroup ns = GrouperGroup.create(s, stem, extn);
    Assert.assertNotNull(ns);
    Assert.assertTrue( klass.equals( ns.getClass().getName() ) );
    Assert.assertNotNull( ns.type() );
    Assert.assertTrue( ns.type().equals(Grouper.DEF_GROUP_TYPE) ); 
    Assert.assertNotNull( ns.attribute(s, "stem") );
    Assert.assertTrue( ns.attribute(s, "stem").value().equals(stem) );
    Assert.assertNotNull( ns.attribute(s, "extension") );
    Assert.assertTrue( ns.attribute(s, "extension").value().equals(extn) );
    s.stop();
  }

  public void testFetchG0() {
    Subject         subj  = GrouperSubject.load( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // Fetch g0
    GrouperGroup g = GrouperGroup.load(s, stem0, extn0);
    Assert.assertNotNull(g);
    Assert.assertTrue( klass.equals( g.getClass().getName() ) );
    Assert.assertNotNull( g.id() );
    Assert.assertNotNull( g.type() );
    Assert.assertNotNull( g.attribute(s, "stem") );
    Assert.assertTrue( g.attribute(s, "stem").value().equals(stem0) );
    Assert.assertNotNull( g.attribute(s, "extension") );
    Assert.assertTrue( g.attribute(s, "extension").value().equals(extn0) );
    Assert.assertNull( g.createSource() );
    Assert.assertNotNull( g.createSubject().getId() );
    Assert.assertNotNull( g.createTime() );
    Assert.assertNull( g.modifySource() );
    Assert.assertNull( g.modifySubject().getId() );
    Assert.assertNull( g.modifyTime() );
    s.stop();
  }

  public void testFetchG1() {
    Subject         subj  = GrouperSubject.load( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // Fetch g1
    GrouperGroup g = GrouperGroup.load(s, stem1, extn1);
    Assert.assertNotNull(g);
    Assert.assertTrue( klass.equals( g.getClass().getName() ) );
    Assert.assertNotNull( g.id() );
    Assert.assertNotNull( g.type() );
    Assert.assertNotNull( g.attribute(s, "stem") );
    Assert.assertTrue( g.attribute(s, "stem").value().equals(stem1) );
    Assert.assertNotNull( g.attribute(s, "extension") );
    Assert.assertTrue( g.attribute(s, "extension").value().equals(extn1) );
    Assert.assertNull( g.createSource() );
    Assert.assertNotNull( g.createSubject().getId() );
    Assert.assertNotNull( g.createTime() );
    Assert.assertNull( g.modifySource() );
    Assert.assertNull( g.modifySubject().getId() );
    Assert.assertNull( g.modifyTime() );
    s.stop();
  }

  public void testFetchG2() {
    Subject         subj  = GrouperSubject.load( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // Fetch g2
    GrouperGroup g = GrouperGroup.load(s, stem2, extn2);
    Assert.assertNotNull(g);
    Assert.assertTrue( klass.equals( g.getClass().getName() ) );
    Assert.assertNotNull( g.id() );
    Assert.assertNotNull( g.type() );
    Assert.assertNotNull( g.attribute(s, "stem") );
    Assert.assertTrue( g.attribute(s, "stem").value().equals(stem2) );
    Assert.assertNotNull( g.attribute(s, "extension") );
    Assert.assertTrue( g.attribute(s, "extension").value().equals(extn2) );
    Assert.assertNull( g.createSource() );
    Assert.assertNotNull( g.createSubject().getId() );
    Assert.assertNotNull( g.createTime() );
    Assert.assertNull( g.modifySource() );
    Assert.assertNull( g.modifySubject().getId() );
    Assert.assertNull( g.modifyTime() );
    s.stop();
  }

  public void testFetchG3() {
    Subject         subj  = GrouperSubject.load( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // Fetch g3
    GrouperGroup g = GrouperGroup.load(s, stem3, extn3);
    Assert.assertNotNull(g);
    Assert.assertTrue( klass.equals( g.getClass().getName() ) );
    Assert.assertNotNull( g.id() );
    Assert.assertNotNull( g.type() );
    Assert.assertNotNull( g.attribute(s, "stem") );
    Assert.assertTrue( g.attribute(s, "stem").value().equals(stem3) );
    Assert.assertNotNull( g.attribute(s, "extension") );
    Assert.assertTrue( g.attribute(s, "extension").value().equals(extn3) );
    Assert.assertNull( g.createSource() );
    Assert.assertNotNull( g.createSubject().getId() );
    Assert.assertNotNull( g.createTime() );
    Assert.assertNull( g.modifySource() );
    Assert.assertNull( g.modifySubject().getId() );
    Assert.assertNull( g.modifyTime() );
    s.stop();
  }

  public void testFetchG4() {
    Subject         subj  = GrouperSubject.load( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // Fetch g4
    GrouperGroup g = GrouperGroup.load(s, stem4, extn4);
    Assert.assertNotNull(g);
    Assert.assertTrue( klass.equals( g.getClass().getName() ) );
    Assert.assertNotNull( g.id() );
    Assert.assertNotNull( g.type() );
    Assert.assertNotNull( g.attribute(s, "stem") );
    Assert.assertTrue( g.attribute(s, "stem").value().equals(stem4) );
    Assert.assertNotNull( g.attribute(s, "extension") );
    Assert.assertTrue( g.attribute(s, "extension").value().equals(extn4) );
    Assert.assertNull( g.createSource() );
    Assert.assertNotNull( g.createSubject().getId() );
    Assert.assertNotNull( g.createTime() );
    Assert.assertNull( g.modifySource() );
    Assert.assertNull( g.modifySubject().getId() );
    Assert.assertNull( g.modifyTime() );
    s.stop();
  }

  // Delete a group
  public void testDeleteGroups0() {
    Subject subj  = GrouperSubject.load(
                      Grouper.config("member.system"), 
                      Grouper.DEF_SUBJ_TYPE
                    );
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull("session !null", s);
    // Delete g4
    GrouperGroup g4 = GrouperGroup.load(s, stem4, extn4);
    Assert.assertNotNull("g4 loaded, !null", g4);
    Assert.assertTrue("g4 deleted",  GrouperGroup.delete(s, g4) );
    // We're done
    s.stop();
  }

  // Fetch valid groups
  public void testFetchGroups1() {
    Subject         subj  = GrouperSubject.load( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);


    // Fetch the groups
    // g0
    GrouperGroup    g0  = GrouperGroup.load(s, stem0, extn0);
    Assert.assertNotNull(g0);
    Assert.assertTrue( klass.equals( g0.getClass().getName() ) );
    Assert.assertNotNull( g0.id() );
    Assert.assertNotNull( g0.type() );
    Assert.assertNotNull( g0.attribute(s, "stem") );
    Assert.assertTrue( g0.attribute(s, "stem").value().equals(stem0) );
    Assert.assertNotNull( g0.attribute(s, "extension") );
    Assert.assertTrue( g0.attribute(s, "extension").value().equals(extn0) );
    // g1
    GrouperGroup    g1  = GrouperGroup.load(s, stem1, extn1);
    Assert.assertNotNull(g1);
    Assert.assertTrue( klass.equals( g1.getClass().getName() ) );
    Assert.assertNotNull( g1.id() );
    Assert.assertNotNull( g1.type() );
    Assert.assertNotNull( g1.attribute(s, "stem") );
    Assert.assertTrue( g1.attribute(s, "stem").value().equals(stem1) );
    Assert.assertNotNull( g1.attribute(s, "extension") );
    Assert.assertTrue( g1.attribute(s, "extension").value().equals(extn1) );
    // g2
    GrouperGroup    g2  = GrouperGroup.load(s, stem2, extn2);
    Assert.assertNotNull(g2);
    Assert.assertTrue( klass.equals( g2.getClass().getName() ) );
    Assert.assertNotNull( g2.id() );
    Assert.assertNotNull( g2.type() );
    Assert.assertNotNull( g2.attribute(s, "stem") );
    Assert.assertTrue( g2.attribute(s, "stem").value().equals(stem2) );
    Assert.assertNotNull( g2.attribute(s, "extension") );
    Assert.assertTrue( g2.attribute(s, "extension").value().equals(extn2) );
    // g3
    GrouperGroup    g3  = GrouperGroup.load(s, stem3, extn3);
    Assert.assertNotNull(g3);
    Assert.assertTrue( klass.equals( g3.getClass().getName() ) );
    Assert.assertNotNull( g3.id() );
    Assert.assertNotNull( g3.type() );
    Assert.assertNotNull( g3.attribute(s, "stem") );
    Assert.assertTrue( g3.attribute(s, "stem").value().equals(stem3) );
    Assert.assertNotNull( g3.attribute(s, "extension") );
    Assert.assertTrue( g3.attribute(s, "extension").value().equals(extn3) );
    // g4
    GrouperGroup    g4  = GrouperGroup.load(s, stem4, extn4);
    Assert.assertNull(g4);

    // We're done
    s.stop();
  }

  public void testCreateG5() {
    Subject         subj  = GrouperSubject.load( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // Attempt to recreate g0
    String stem = stem0;
    String extn = extn0;
    GrouperGroup g = GrouperGroup.create(s, stem, extn);
    Assert.assertNull(g);
    s.stop();
  }

  public void testFetchG5() {
    Subject         subj  = GrouperSubject.load( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // Attempt to fetch a DEF_GROUP_TYPE group as a NS_TYPE group
    // g0
    GrouperGroup g  = GrouperGroup.load(s, stem0, extn0, Grouper.NS_TYPE);
    Assert.assertNull(g);
    s.stop();
  }

  public void testFetchG6() {
    Subject         subj  = GrouperSubject.load( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // Attempt to fetch a NS_TYPE type group (explicitly) as a DEF_GROUP_TYPE group
    // ns0
    GrouperGroup g  = GrouperGroup.load(s, ns_stem0, ns_extn0, Grouper.DEF_GROUP_TYPE);
    Assert.assertNull(g);
    s.stop();
  }

  public void testFetchG7() {
    Subject         subj  = GrouperSubject.load( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // Attempt to fetch a NS_TYPE type group (implicitly) as a DEF_GROUP_TYPE group
    // ns0
    GrouperGroup g  = GrouperGroup.load(s, ns_stem0, ns_extn0);
    Assert.assertNull(g);
    s.stop();
  }

  public void testFetchG8() {
    Subject         subj  = GrouperSubject.load( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // Attempt to fetch a NS_TYPE type group (explicitly) as a DEF_GROUP_TYPE group
    // ns00
    GrouperGroup g  = GrouperGroup.load(s, ns_stem00, ns_extn00, Grouper.DEF_GROUP_TYPE);
    Assert.assertNull(g);
    s.stop();
  }

  public void testFetchG9() {
    Subject         subj  = GrouperSubject.load( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // Attempt to fetch a NS_TYPE type group (implicitly) as a DEF_GROUP_TYPE group
    // ns00
    GrouperGroup g  = GrouperGroup.load(s, ns_stem00, ns_extn00);
    Assert.assertNull(g);
    s.stop();
  }

  public void testCreateG6() {
    Subject         subj  = GrouperSubject.load( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // Attempt to create a group with bad chars in extn
    String stem = stem5;
    String extn = extn5;
    GrouperGroup g = GrouperGroup.create(s, stem, extn);
    Assert.assertNull(g);
    s.stop();
  }

  public void testCreateG7() {
    //Subject         subj  = GrouperSubject.load( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    Subject subj = GrouperSubject.load(Util.m0i, Util.m0t);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // Attempt to create a group in a ns where the subject does not
    // have the STEM priv
    String stem = Util.stem6;
    String extn = Util.extn6;
    GrouperGroup g = GrouperGroup.create(s, stem, extn);
    Assert.assertNull(g);
    s.stop();
  }

  public void testFetchG10() {
    Subject subj  = GrouperSubject.load(Util.rooti, Util.roott);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);
    // g0
    GrouperGroup g  = GrouperGroup.load(s, Util.stem0, Util.extn0);
    Assert.assertNotNull(g);
    Assert.assertNotNull(g.name());
    Assert.assertNotNull(g.id());
    Assert.assertNotNull(g.type());
    // Refetch by id
    GrouperGroup g1 = GrouperGroup.loadByID(s, g.id());
    Assert.assertNotNull(g1);
    Assert.assertNotNull(g1.name());
    Assert.assertNotNull(g1.id());
    Assert.assertNotNull(g1.type());
    Assert.assertTrue(g.name().equals(g1.name()));
    Assert.assertTrue(g.id().equals(g1.id()));
    Assert.assertTrue(g.type().equals(g1.type()));
    // Refetch by id with type
    GrouperGroup g2 = GrouperGroup.loadByID(s, g.id(), Grouper.DEF_GROUP_TYPE);
    Assert.assertNotNull(g2);
    Assert.assertNotNull(g2.name());
    Assert.assertNotNull(g2.id());
    Assert.assertNotNull(g2.type());
    Assert.assertTrue(g.name().equals(g2.name()));
    Assert.assertTrue(g.id().equals(g2.id()));
    Assert.assertTrue(g.type().equals(g2.type()));
    // Refetch by name
    GrouperGroup g3 = GrouperGroup.loadByName(s, g.name());
    Assert.assertNotNull(g3);
    Assert.assertNotNull(g3.name());
    Assert.assertNotNull(g3.id());
    Assert.assertNotNull(g3.type());
    Assert.assertTrue(g.name().equals(g3.name()));
    Assert.assertTrue(g.id().equals(g3.id()));
    Assert.assertTrue(g.type().equals(g3.type()));
    // Refetch by name with type
    GrouperGroup g4 = GrouperGroup.loadByName(s, g.name(), Grouper.DEF_GROUP_TYPE);
    Assert.assertNotNull(g4);
    Assert.assertNotNull(g4.name());
    Assert.assertNotNull(g4.id());
    Assert.assertNotNull(g4.type());
    Assert.assertTrue(g.name().equals(g4.name()));
    Assert.assertTrue(g.id().equals(g4.id()));
    Assert.assertTrue(g.type().equals(g4.type()));
    s.stop();
  }

}

