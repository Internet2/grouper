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


public class TestGroupAttrs extends TestCase {

  /// Naming Groups
  private String  ns_extn0  = "stem.0";
  private String  ns_stem00 = "stem.0";
  private String  ns_extn00 = "stem.0.0";
  private String  ns_extn1  = "stem.1";
  private String  ns_extn2  = "stem.2";

  // Groups
  private String  stem0 = "stem.0";
  private String  extn0 = "extn.0";
  private String  stem1 = "stem.1";
  private String  extn1 = "extn.1";
  private String  stem2 = "stem.2";
  private String  extn2 = "extn.2";
  private String  stem3 = "stem.0";
  private String  extn3 = "extn.3";


  public TestGroupAttrs(String name) {
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
    Subject         subj  = GrouperSubject.load( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    Assert.assertNotNull(subj);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // Fetch namespaces
    // ns0
    GrouperGroup ns0 = GrouperGroup.load(s, Grouper.NS_ROOT, ns_extn0, Grouper.NS_TYPE);
    Assert.assertNotNull(ns0);
    // ns00
    GrouperGroup ns00 = GrouperGroup.load(s, ns_stem00, ns_extn00, Grouper.NS_TYPE);
    Assert.assertNotNull(ns00);
    // ns1
    GrouperGroup ns1 = GrouperGroup.load(s, Grouper.NS_ROOT, ns_extn1, Grouper.NS_TYPE);
    Assert.assertNotNull(ns1);
    // ns2
    GrouperGroup ns2 = GrouperGroup.load(s, Grouper.NS_ROOT, ns_extn2, Grouper.NS_TYPE);
    Assert.assertNotNull(ns2);
    // Fetch groups
    // g0
    GrouperGroup g0 = GrouperGroup.load(s, stem0, extn0);
    Assert.assertNotNull(g0);
    // g1
    GrouperGroup g1 = GrouperGroup.load(s, stem1, extn1);
    Assert.assertNotNull(g1);
    // g2
    GrouperGroup g2 = GrouperGroup.load(s, stem2, extn2);
    Assert.assertNotNull(g2);
    // g3
    GrouperGroup g3 = GrouperGroup.load(s, stem3, extn3);
    Assert.assertNotNull(g3);

    // We're done
    s.stop();
  }


  //
  // Test Initial Attribute Values
  //

  public void testFetchAttrs0() {
    Subject         subj  = GrouperSubject.load( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    Assert.assertNotNull(subj);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // ns0
    String type = Grouper.NS_TYPE;
    String extn = ns_extn0; 
    String name = GrouperGroup.groupName(Grouper.NS_ROOT, extn);
    GrouperGroup g = GrouperGroup.load(s, Grouper.NS_ROOT, extn, type);
    Assert.assertNotNull("g !null", g);
    Assert.assertNotNull("g id !null", g.id() );
    Assert.assertNotNull("g type !null", g.type() );
    Assert.assertNotNull("g stem !null", g.attribute("stem") );
    Assert.assertTrue("g stem value", g.attribute("stem").value().equals(Grouper.NS_ROOT) );
    Assert.assertNotNull("g extn !null", g.attribute("extension") );
    Assert.assertTrue("g extn value", g.attribute("extension").value().equals(extn) );
    Assert.assertNotNull("g name !null", name);
    Assert.assertTrue("g name value", g.attribute("name").value().equals(name) );
    Assert.assertNull("g desc null", g.attribute("description") );
    Assert.assertNull("g createSource null", g.createSource() );
    Assert.assertNotNull("g createSubject !null", g.createSubject().getId() );
    Assert.assertNotNull("g createTime !null", g.createTime() );
    Assert.assertNull("g modifySource null", g.modifySource() );
    Assert.assertNull("g modifySubject !null", g.modifySubject().getId() );
    Assert.assertNull("g modifyTime !null", g.modifyTime() );
    s.stop();
  }

  public void testFetchAttrs1() {
    Subject         subj  = GrouperSubject.load( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    Assert.assertNotNull(subj);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // ns00
    String type = Grouper.NS_TYPE;
    String stem = ns_stem00;
    String extn = ns_extn00; 
    String name = GrouperGroup.groupName(stem, extn);
    GrouperGroup g = GrouperGroup.load(s, stem, extn, type);
    Assert.assertNotNull(g);
    Assert.assertNotNull( g.id() );
    Assert.assertNotNull( g.type() );
    Assert.assertNotNull( g.attribute("stem") );
    Assert.assertTrue( g.attribute("stem").value().equals(stem) );
    Assert.assertNotNull( g.attribute("extension") );
    Assert.assertTrue( g.attribute("extension").value().equals(extn) );
    Assert.assertNotNull(name);
    Assert.assertTrue( g.attribute("name").value().equals(name) );
    Assert.assertNull( g.attribute("description") );
    Assert.assertNull( g.createSource() );
    Assert.assertNotNull( g.createSubject().getId() );
    Assert.assertNotNull( g.createTime() );
    Assert.assertNull( g.modifySource() );
    Assert.assertNull( g.modifySubject().getId() );
    Assert.assertNull( g.modifyTime() );
    s.stop();
  }

  public void testFetchAttrs2() {
    Subject         subj  = GrouperSubject.load( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    Assert.assertNotNull(subj);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // ns1
    String type = Grouper.NS_TYPE;
    String extn = ns_extn1; 
    String name = GrouperGroup.groupName(Grouper.NS_ROOT, extn);
    GrouperGroup g = GrouperGroup.load(s, Grouper.NS_ROOT, extn, type);
    Assert.assertNotNull(g);
    Assert.assertNotNull( g.id() );
    Assert.assertNotNull( g.type() );
    Assert.assertNotNull( g.attribute("stem") );
    Assert.assertTrue( g.attribute("stem").value().equals(Grouper.NS_ROOT) );
    Assert.assertNotNull( g.attribute("extension") );
    Assert.assertTrue( g.attribute("extension").value().equals(extn) );
    Assert.assertNotNull(name);
    Assert.assertTrue( g.attribute("name").value().equals(name) );
    Assert.assertNull( g.attribute("description") );
    Assert.assertNull( g.createSource() );
    Assert.assertNotNull( g.createSubject().getId() );
    Assert.assertNotNull( g.createTime() );
    Assert.assertNull( g.modifySource() );
    Assert.assertNull( g.modifySubject().getId() );
    Assert.assertNull( g.modifyTime() );
    s.stop();
  }

  public void testFetchAttrs3() {
    Subject         subj  = GrouperSubject.load( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    Assert.assertNotNull(subj);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // ns2
    String type = Grouper.NS_TYPE;
    String extn = ns_extn2; 
    String name = GrouperGroup.groupName(Grouper.NS_ROOT, extn);
    GrouperGroup g = GrouperGroup.load(s, Grouper.NS_ROOT, extn, type);
    Assert.assertNotNull(g);
    Assert.assertNotNull( g.id() );
    Assert.assertNotNull( g.type() );
    Assert.assertNotNull( g.attribute("stem") );
    Assert.assertTrue( g.attribute("stem").value().equals(Grouper.NS_ROOT) );
    Assert.assertNotNull( g.attribute("extension") );
    Assert.assertTrue( g.attribute("extension").value().equals(extn) );
    Assert.assertNotNull(name);
    Assert.assertTrue( g.attribute("name").value().equals(name) );
    Assert.assertNull( g.attribute("description") );
    Assert.assertNull( g.createSource() );
    Assert.assertNotNull( g.createSubject().getId() );
    Assert.assertNotNull( g.createTime() );
    Assert.assertNull( g.modifySource() );
    Assert.assertNull( g.modifySubject().getId() );
    Assert.assertNull( g.modifyTime() );
    s.stop();
  }

  public void testFetchAttrs4() {
    Subject         subj  = GrouperSubject.load( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    Assert.assertNotNull(subj);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // g0
    String type = Grouper.DEF_GROUP_TYPE;
    String stem = stem0;
    String extn = extn0; 
    String name = GrouperGroup.groupName(stem, extn);
    GrouperGroup g = GrouperGroup.load(s, stem, extn, type);
    Assert.assertNotNull(g);
    Assert.assertNotNull( g.id() );
    Assert.assertNotNull( g.type() );
    Assert.assertNotNull( g.attribute("stem") );
    Assert.assertTrue( g.attribute("stem").value().equals(stem) );
    Assert.assertNotNull( g.attribute("extension") );
    Assert.assertTrue( g.attribute("extension").value().equals(extn) );
    Assert.assertNotNull(name);
    Assert.assertTrue( g.attribute("name").value().equals(name) );
    Assert.assertNull( g.attribute("description") );
    Assert.assertNull( g.createSource() );
    Assert.assertNotNull( g.createSubject().getId() );
    Assert.assertNotNull( g.createTime() );
    Assert.assertNull( g.modifySource() );
    Assert.assertNull( g.modifySubject().getId() );
    Assert.assertNull( g.modifyTime() );
    s.stop();
  }

  public void testFetchAttrs5() {
    Subject         subj  = GrouperSubject.load( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    Assert.assertNotNull(subj);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // g1
    String type = Grouper.DEF_GROUP_TYPE;
    String stem = stem1;
    String extn = extn1; 
    String name = GrouperGroup.groupName(stem, extn);
    GrouperGroup g = GrouperGroup.load(s, stem, extn, type);
    Assert.assertNotNull(g);
    Assert.assertNotNull( g.id() );
    Assert.assertNotNull( g.type() );
    Assert.assertNotNull( g.attribute("stem") );
    Assert.assertTrue( g.attribute("stem").value().equals(stem) );
    Assert.assertNotNull( g.attribute("extension") );
    Assert.assertTrue( g.attribute("extension").value().equals(extn) );
    Assert.assertNotNull(name);
    Assert.assertTrue( g.attribute("name").value().equals(name) );
    Assert.assertNull( g.attribute("description") );
    Assert.assertNull( g.createSource() );
    Assert.assertNotNull( g.createSubject().getId() );
    Assert.assertNotNull( g.createTime() );
    Assert.assertNull( g.modifySource() );
    Assert.assertNull( g.modifySubject().getId() );
    Assert.assertNull( g.modifyTime() );
    s.stop();
  }

  public void testFetchAttrs6() {
    Subject         subj  = GrouperSubject.load( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    Assert.assertNotNull(subj);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // g2 
    String type = Grouper.DEF_GROUP_TYPE;
    String stem = stem2;
    String extn = extn2; 
    String name = GrouperGroup.groupName(stem, extn);
    GrouperGroup g = GrouperGroup.load(s, stem, extn, type);
    Assert.assertNotNull(g);
    Assert.assertNotNull( g.id() );
    Assert.assertNotNull( g.type() );
    Assert.assertNotNull( g.attribute("stem") );
    Assert.assertTrue( g.attribute("stem").value().equals(stem) );
    Assert.assertNotNull( g.attribute("extension") );
    Assert.assertTrue( g.attribute("extension").value().equals(extn) );
    Assert.assertNotNull(name);
    Assert.assertTrue( g.attribute("name").value().equals(name) );
    Assert.assertNull( g.attribute("description") );
    Assert.assertNull( g.createSource() );
    Assert.assertNotNull( g.createSubject().getId() );
    Assert.assertNotNull( g.createTime() );
    Assert.assertNull( g.modifySource() );
    Assert.assertNull( g.modifySubject().getId() );
    Assert.assertNull( g.modifyTime() );
    s.stop();
  }

  public void testFetchAttrs7() {
    Subject         subj  = GrouperSubject.load( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    Assert.assertNotNull(subj);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // g3
    String type = Grouper.DEF_GROUP_TYPE;
    String stem = stem3;
    String extn = extn3; 
    String name = GrouperGroup.groupName(stem, extn);
    GrouperGroup g = GrouperGroup.load(s, stem, extn, type);
    Assert.assertNotNull(g);
    Assert.assertNotNull( g.id() );
    Assert.assertNotNull( g.type() );
    Assert.assertNotNull( g.attribute("stem") );
    Assert.assertTrue( g.attribute("stem").value().equals(stem) );
    Assert.assertNotNull( g.attribute("extension") );
    Assert.assertTrue( g.attribute("extension").value().equals(extn) );
    Assert.assertNotNull(name);
    Assert.assertTrue( g.attribute("name").value().equals(name) );
    Assert.assertNull( g.attribute("description") );
    Assert.assertNull( g.createSource() );
    Assert.assertNotNull( g.createSubject().getId() );
    Assert.assertNotNull( g.createTime() );
    Assert.assertNull( g.modifySource() );
    Assert.assertNull( g.modifySubject().getId() );
    Assert.assertNull( g.modifyTime() );
    s.stop();
  }


  // 
  // Give every group a description
  //

  public void testAddAttr0() {
    Subject         subj  = GrouperSubject.load( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    Assert.assertNotNull(subj);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // ns0
    String type = Grouper.NS_TYPE;
    String extn = ns_extn0; 
    String name = GrouperGroup.groupName(Grouper.NS_ROOT, extn);
    String desc = "My Description: " + name;
    GrouperGroup g = GrouperGroup.load(s, Grouper.NS_ROOT, extn, type);
    Assert.assertNotNull(g);
    Assert.assertTrue( g.attribute("description", desc) );
    s.stop();
  }

  public void testAddAttr1() {
    Subject         subj  = GrouperSubject.load( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    Assert.assertNotNull(subj);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // ns00
    String type = Grouper.NS_TYPE;
    String stem = ns_stem00;
    String extn = ns_extn00; 
    String name = GrouperGroup.groupName(stem, extn);
    String desc = "My Description: " + name;
    GrouperGroup g = GrouperGroup.load(s, stem, extn, type);
    Assert.assertNotNull(g);
    Assert.assertTrue( g.attribute("description", desc) );
    s.stop();
  }

  public void testAddAttr2() {
    Subject         subj  = GrouperSubject.load( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    Assert.assertNotNull(subj);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // ns1
    String type = Grouper.NS_TYPE;
    String extn = ns_extn1; 
    String name = GrouperGroup.groupName(Grouper.NS_ROOT, extn);
    String desc = "My Description: " + name;
    GrouperGroup g = GrouperGroup.load(s, Grouper.NS_ROOT, extn, type);
    Assert.assertNotNull(g);
    Assert.assertTrue( g.attribute("description", desc) );
    s.stop();
  }

  public void testAddAttr3() {
    Subject         subj  = GrouperSubject.load( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    Assert.assertNotNull(subj);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // ns2
    String type = Grouper.NS_TYPE;
    String extn = ns_extn2; 
    String name = GrouperGroup.groupName(Grouper.NS_ROOT, extn);
    String desc = "My Description: " + name;
    GrouperGroup g = GrouperGroup.load(s, Grouper.NS_ROOT, extn, type);
    Assert.assertNotNull(g);
    Assert.assertTrue( g.attribute("description", desc) );
    s.stop();
  }

  public void testAddAttr4() {
    Subject         subj  = GrouperSubject.load( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    Assert.assertNotNull(subj);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // g0
    String type = Grouper.DEF_GROUP_TYPE;
    String stem = stem0;
    String extn = extn0; 
    String name = GrouperGroup.groupName(stem, extn);
    String desc = "My Description: " + name;
    GrouperGroup g = GrouperGroup.load(s, stem, extn, type);
    Assert.assertNotNull(g);
    Assert.assertTrue( g.attribute("description", desc) );
    s.stop();
  }

  public void testAddAttr5() {
    Subject         subj  = GrouperSubject.load( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    Assert.assertNotNull(subj);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // g1
    String type = Grouper.DEF_GROUP_TYPE;
    String stem = stem1;
    String extn = extn1; 
    String name = GrouperGroup.groupName(stem, extn);
    String desc = "My Description: " + name;
    GrouperGroup g = GrouperGroup.load(s, stem, extn, type);
    Assert.assertNotNull(g);
    Assert.assertTrue( g.attribute("description", desc) );
    s.stop();
  }

  public void testAddAttr6() {
    Subject         subj  = GrouperSubject.load( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    Assert.assertNotNull(subj);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // g2
    String type = Grouper.DEF_GROUP_TYPE;
    String stem = stem2;
    String extn = extn2; 
    String name = GrouperGroup.groupName(stem, extn);
    String desc = "My Description: " + name;
    GrouperGroup g = GrouperGroup.load(s, stem, extn, type);
    Assert.assertNotNull(g);
    Assert.assertTrue( g.attribute("description", desc) );
    s.stop();
  }

  public void testAddAttr7() {
    Subject         subj  = GrouperSubject.load( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    Assert.assertNotNull(subj);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // g3
    String type = Grouper.DEF_GROUP_TYPE;
    String stem = stem3;
    String extn = extn3; 
    String name = GrouperGroup.groupName(stem, extn);
    String desc = "My Description: " + name;
    GrouperGroup g = GrouperGroup.load(s, stem, extn, type);
    Assert.assertNotNull(g);
    Assert.assertTrue( g.attribute("description", desc) );
    s.stop();
  }


  // 
  // Test addition of `description' attribute
  //


  public void testFetchAttrs8() {
    Subject         subj  = GrouperSubject.load( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    Assert.assertNotNull(subj);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // ns0
    String type = Grouper.NS_TYPE;
    String extn = ns_extn0; 
    String name = GrouperGroup.groupName(Grouper.NS_ROOT, extn);
    String desc = "My Description: " + name;
    GrouperGroup g = GrouperGroup.load(s, Grouper.NS_ROOT, extn, type);
    Assert.assertNotNull(g);
    Assert.assertNotNull( g.id() );
    Assert.assertNotNull( g.type() );
    Assert.assertNotNull( g.attribute("stem") );
    Assert.assertTrue( g.attribute("stem").value().equals(Grouper.NS_ROOT) );
    Assert.assertNotNull( g.attribute("extension") );
    Assert.assertTrue( g.attribute("extension").value().equals(extn) );
    Assert.assertNotNull(name);
    Assert.assertTrue( g.attribute("name").value().equals(name) );
    Assert.assertNotNull( g.attribute("description") );
    Assert.assertTrue( g.attribute("description").value().equals(desc) );
    Assert.assertNull( g.createSource() );
    Assert.assertNotNull( g.createSubject().getId() );
    Assert.assertNotNull( g.createTime() );
    Assert.assertNull( g.modifySource() );
    Assert.assertNotNull( g.modifySubject().getId() );
    Assert.assertNotNull( g.modifyTime() );
    s.stop();
  }

  public void testFetchAttrs9() {
    Subject         subj  = GrouperSubject.load( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    Assert.assertNotNull(subj);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // ns00
    String type = Grouper.NS_TYPE;
    String stem = ns_stem00;
    String extn = ns_extn00; 
    String name = GrouperGroup.groupName(stem, extn);
    String desc = "My Description: " + name;
    GrouperGroup g = GrouperGroup.load(s, stem, extn, type);
    Assert.assertNotNull(g);
    Assert.assertNotNull( g.id() );
    Assert.assertNotNull( g.type() );
    Assert.assertNotNull( g.attribute("stem") );
    Assert.assertTrue( g.attribute("stem").value().equals(stem) );
    Assert.assertNotNull( g.attribute("extension") );
    Assert.assertTrue( g.attribute("extension").value().equals(extn) );
    Assert.assertNotNull(name);
    Assert.assertTrue( g.attribute("name").value().equals(name) );
    Assert.assertNotNull( g.attribute("description") );
    Assert.assertTrue( g.attribute("description").value().equals(desc) );
    Assert.assertNull( g.createSource() );
    Assert.assertNotNull( g.createSubject().getId() );
    Assert.assertNotNull( g.createTime() );
    Assert.assertNull( g.modifySource() );
    Assert.assertNotNull( g.modifySubject().getId() );
    Assert.assertNotNull( g.modifyTime() );
    s.stop();
  }

  public void testFetchAttrs10() {
    Subject         subj  = GrouperSubject.load( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    Assert.assertNotNull(subj);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // ns1
    String type = Grouper.NS_TYPE;
    String extn = ns_extn1; 
    String name = GrouperGroup.groupName(Grouper.NS_ROOT, extn);
    String desc = "My Description: " + name;
    GrouperGroup g = GrouperGroup.load(s, Grouper.NS_ROOT, extn, type);
    Assert.assertNotNull(g);
    Assert.assertNotNull( g.id() );
    Assert.assertNotNull( g.type() );
    Assert.assertNotNull( g.attribute("stem") );
    Assert.assertTrue( g.attribute("stem").value().equals(Grouper.NS_ROOT) );
    Assert.assertNotNull( g.attribute("extension") );
    Assert.assertTrue( g.attribute("extension").value().equals(extn) );
    Assert.assertNotNull(name);
    Assert.assertTrue( g.attribute("name").value().equals(name) );
    Assert.assertNotNull( g.attribute("description") );
    Assert.assertTrue( g.attribute("description").value().equals(desc) );
    Assert.assertNull( g.createSource() );
    Assert.assertNotNull( g.createSubject().getId() );
    Assert.assertNotNull( g.createTime() );
    Assert.assertNull( g.modifySource() );
    Assert.assertNotNull( g.modifySubject().getId() );
    Assert.assertNotNull( g.modifyTime() );
    s.stop();
  }

  public void testFetchAttrs11() {
    Subject         subj  = GrouperSubject.load( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    Assert.assertNotNull(subj);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // ns2
    String type = Grouper.NS_TYPE;
    String extn = ns_extn2; 
    String name = GrouperGroup.groupName(Grouper.NS_ROOT, extn);
    String desc = "My Description: " + name;
    GrouperGroup g = GrouperGroup.load(s, Grouper.NS_ROOT, extn, type);
    Assert.assertNotNull(g);
    Assert.assertNotNull( g.id() );
    Assert.assertNotNull( g.type() );
    Assert.assertNotNull( g.attribute("stem") );
    Assert.assertTrue( g.attribute("stem").value().equals(Grouper.NS_ROOT) );
    Assert.assertNotNull( g.attribute("extension") );
    Assert.assertTrue( g.attribute("extension").value().equals(extn) );
    Assert.assertNotNull(name);
    Assert.assertTrue( g.attribute("name").value().equals(name) );
    Assert.assertNotNull( g.attribute("description") );
    Assert.assertTrue( g.attribute("description").value().equals(desc) );
    Assert.assertNull( g.createSource() );
    Assert.assertNotNull( g.createSubject().getId() );
    Assert.assertNotNull( g.createTime() );
    Assert.assertNull( g.modifySource() );
    Assert.assertNotNull( g.modifySubject().getId() );
    Assert.assertNotNull( g.modifyTime() );
    s.stop();
  }

  public void testFetchAttrs12() {
    Subject         subj  = GrouperSubject.load( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    Assert.assertNotNull(subj);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // g0
    String type = Grouper.DEF_GROUP_TYPE;
    String stem = stem0;
    String extn = extn0; 
    String name = GrouperGroup.groupName(stem, extn);
    String desc = "My Description: " + name;
    GrouperGroup g = GrouperGroup.load(s, stem, extn, type);
    Assert.assertNotNull(g);
    Assert.assertNotNull( g.id() );
    Assert.assertNotNull( g.type() );
    Assert.assertNotNull( g.attribute("stem") );
    Assert.assertTrue( g.attribute("stem").value().equals(stem) );
    Assert.assertNotNull( g.attribute("extension") );
    Assert.assertTrue( g.attribute("extension").value().equals(extn) );
    Assert.assertNotNull(name);
    Assert.assertTrue( g.attribute("name").value().equals(name) );
    Assert.assertNotNull( g.attribute("description") );
    Assert.assertTrue( g.attribute("description").value().equals(desc) );
    Assert.assertNull( g.createSource() );
    Assert.assertNotNull( g.createSubject().getId() );
    Assert.assertNotNull( g.createTime() );
    Assert.assertNull( g.modifySource() );
    Assert.assertNotNull( g.modifySubject().getId() );
    Assert.assertNotNull( g.modifyTime() );
    s.stop();
  }

  public void testFetchAttrs13() {
    Subject         subj  = GrouperSubject.load( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    Assert.assertNotNull(subj);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // g1
    String type = Grouper.DEF_GROUP_TYPE;
    String stem = stem1;
    String extn = extn1; 
    String name = GrouperGroup.groupName(stem, extn);
    String desc = "My Description: " + name;
    GrouperGroup g = GrouperGroup.load(s, stem, extn, type);
    Assert.assertNotNull(g);
    Assert.assertNotNull( g.id() );
    Assert.assertNotNull( g.type() );
    Assert.assertNotNull( g.attribute("stem") );
    Assert.assertTrue( g.attribute("stem").value().equals(stem) );
    Assert.assertNotNull( g.attribute("extension") );
    Assert.assertTrue( g.attribute("extension").value().equals(extn) );
    Assert.assertNotNull(name);
    Assert.assertTrue( g.attribute("name").value().equals(name) );
    Assert.assertNotNull( g.attribute("description") );
    Assert.assertTrue( g.attribute("description").value().equals(desc) );
    Assert.assertNull( g.createSource() );
    Assert.assertNotNull( g.createSubject().getId() );
    Assert.assertNotNull( g.createTime() );
    Assert.assertNull( g.modifySource() );
    Assert.assertNotNull( g.modifySubject().getId() );
    Assert.assertNotNull( g.modifyTime() );
    s.stop();
  }

  public void testFetchAttrs14() {
    Subject         subj  = GrouperSubject.load( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    Assert.assertNotNull(subj);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // g2 
    String type = Grouper.DEF_GROUP_TYPE;
    String stem = stem2;
    String extn = extn2; 
    String name = GrouperGroup.groupName(stem, extn);
    String desc = "My Description: " + name;
    GrouperGroup g = GrouperGroup.load(s, stem, extn, type);
    Assert.assertNotNull(g);
    Assert.assertNotNull( g.id() );
    Assert.assertNotNull( g.type() );
    Assert.assertNotNull( g.attribute("stem") );
    Assert.assertTrue( g.attribute("stem").value().equals(stem) );
    Assert.assertNotNull( g.attribute("extension") );
    Assert.assertTrue( g.attribute("extension").value().equals(extn) );
    Assert.assertNotNull(name);
    Assert.assertTrue( g.attribute("name").value().equals(name) );
    Assert.assertNotNull( g.attribute("description") );
    Assert.assertTrue( g.attribute("description").value().equals(desc) );
    Assert.assertNull( g.createSource() );
    Assert.assertNotNull( g.createSubject().getId() );
    Assert.assertNotNull( g.createTime() );
    Assert.assertNull( g.modifySource() );
    Assert.assertNotNull( g.modifySubject().getId() );
    Assert.assertNotNull( g.modifyTime() );
    s.stop();
  }

  public void testFetchAttrs15() {
    Subject         subj  = GrouperSubject.load( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    Assert.assertNotNull(subj);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // g3
    String type = Grouper.DEF_GROUP_TYPE;
    String stem = stem3;
    String extn = extn3; 
    String name = GrouperGroup.groupName(stem, extn);
    String desc = "My Description: " + name;
    GrouperGroup g = GrouperGroup.load(s, stem, extn, type);
    Assert.assertNotNull(g);
    Assert.assertNotNull( g.id() );
    Assert.assertNotNull( g.type() );
    Assert.assertNotNull( g.attribute("stem") );
    Assert.assertTrue( g.attribute("stem").value().equals(stem) );
    Assert.assertNotNull( g.attribute("extension") );
    Assert.assertTrue( g.attribute("extension").value().equals(extn) );
    Assert.assertNotNull(name);
    Assert.assertTrue( g.attribute("name").value().equals(name) );
    Assert.assertNotNull( g.attribute("description") );
    Assert.assertTrue( g.attribute("description").value().equals(desc) );
    Assert.assertNull( g.createSource() );
    Assert.assertNotNull( g.createSubject().getId() );
    Assert.assertNotNull( g.createTime() );
    Assert.assertNull( g.modifySource() );
    Assert.assertNotNull( g.modifySubject().getId() );
    Assert.assertNotNull( g.modifyTime() );
    s.stop();
  }

  // 
  // Update the description of every group
  //

  public void testAddAttr8() {
    Subject         subj  = GrouperSubject.load( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    Assert.assertNotNull(subj);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // ns0
    String type = Grouper.NS_TYPE;
    String extn = ns_extn0; 
    String name = GrouperGroup.groupName(Grouper.NS_ROOT, extn);
    String desc = "My New Description: " + name;
    GrouperGroup g = GrouperGroup.load(s, Grouper.NS_ROOT, extn, type);
    Assert.assertNotNull(g);
    Assert.assertTrue( g.attribute("description", desc) );
    s.stop();
  }

  public void testAddAttr9() {
    Subject         subj  = GrouperSubject.load( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    Assert.assertNotNull(subj);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // ns00
    String type = Grouper.NS_TYPE;
    String stem = ns_stem00;
    String extn = ns_extn00; 
    String name = GrouperGroup.groupName(stem, extn);
    String desc = "My New Description: " + name;
    GrouperGroup g = GrouperGroup.load(s, stem, extn, type);
    Assert.assertNotNull(g);
    Assert.assertTrue( g.attribute("description", desc) );
    s.stop();
  }

  public void testAddAttr10() {
    Subject         subj  = GrouperSubject.load( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    Assert.assertNotNull(subj);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // ns1
    String type = Grouper.NS_TYPE;
    String extn = ns_extn1; 
    String name = GrouperGroup.groupName(Grouper.NS_ROOT, extn);
    String desc = "My New Description: " + name;
    GrouperGroup g = GrouperGroup.load(s, Grouper.NS_ROOT, extn, type);
    Assert.assertNotNull(g);
    Assert.assertTrue( g.attribute("description", desc) );
    s.stop();
  }

  public void testAddAttr11() {
    Subject         subj  = GrouperSubject.load( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    Assert.assertNotNull(subj);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // ns2
    String type = Grouper.NS_TYPE;
    String extn = ns_extn2; 
    String name = GrouperGroup.groupName(Grouper.NS_ROOT, extn);
    String desc = "My New Description: " + name;
    GrouperGroup g = GrouperGroup.load(s, Grouper.NS_ROOT, extn, type);
    Assert.assertNotNull(g);
    Assert.assertTrue( g.attribute("description", desc) );
    s.stop();
  }

  public void testAddAttr12() {
    Subject         subj  = GrouperSubject.load( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    Assert.assertNotNull(subj);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // g0
    String type = Grouper.DEF_GROUP_TYPE;
    String stem = stem0;
    String extn = extn0; 
    String name = GrouperGroup.groupName(stem, extn);
    String desc = "My New Description: " + name;
    GrouperGroup g = GrouperGroup.load(s, stem, extn, type);
    Assert.assertNotNull(g);
    Assert.assertTrue( g.attribute("description", desc) );
    s.stop();
  }

  public void testAddAttr13() {
    Subject         subj  = GrouperSubject.load( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    Assert.assertNotNull(subj);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // g1
    String type = Grouper.DEF_GROUP_TYPE;
    String stem = stem1;
    String extn = extn1; 
    String name = GrouperGroup.groupName(stem, extn);
    String desc = "My New Description: " + name;
    GrouperGroup g = GrouperGroup.load(s, stem, extn, type);
    Assert.assertNotNull(g);
    Assert.assertTrue( g.attribute("description", desc) );
    s.stop();
  }

  public void testAddAttr14() {
    Subject         subj  = GrouperSubject.load( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    Assert.assertNotNull(subj);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // g2
    String type = Grouper.DEF_GROUP_TYPE;
    String stem = stem2;
    String extn = extn2; 
    String name = GrouperGroup.groupName(stem, extn);
    String desc = "My New Description: " + name;
    GrouperGroup g = GrouperGroup.load(s, stem, extn, type);
    Assert.assertNotNull(g);
    Assert.assertTrue( g.attribute("description", desc) );
    s.stop();
  }

  public void testAddAttr15() {
    Subject         subj  = GrouperSubject.load( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    Assert.assertNotNull(subj);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // g3
    String type = Grouper.DEF_GROUP_TYPE;
    String stem = stem3;
    String extn = extn3; 
    String name = GrouperGroup.groupName(stem, extn);
    String desc = "My New Description: " + name;
    GrouperGroup g = GrouperGroup.load(s, stem, extn, type);
    Assert.assertNotNull(g);
    Assert.assertTrue( g.attribute("description", desc) );
    s.stop();
  }


  // 
  // Test updating of `description' attribute
  //


  public void testFetchAttrs16() {
    Subject         subj  = GrouperSubject.load( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    Assert.assertNotNull(subj);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // ns0
    String type = Grouper.NS_TYPE;
    String extn = ns_extn0; 
    String name = GrouperGroup.groupName(Grouper.NS_ROOT, extn);
    String desc = "My New Description: " + name;
    GrouperGroup g = GrouperGroup.load(s, Grouper.NS_ROOT, extn, type);
    Assert.assertNotNull(g);
    Assert.assertNotNull( g.id() );
    Assert.assertNotNull( g.type() );
    Assert.assertNotNull( g.attribute("stem") );
    Assert.assertTrue( g.attribute("stem").value().equals(Grouper.NS_ROOT) );
    Assert.assertNotNull( g.attribute("extension") );
    Assert.assertTrue( g.attribute("extension").value().equals(extn) );
    Assert.assertNotNull(name);
    Assert.assertTrue( g.attribute("name").value().equals(name) );
    Assert.assertNotNull( g.attribute("description") );
    Assert.assertTrue( g.attribute("description").value().equals(desc) );
    Assert.assertNull( g.createSource() );
    Assert.assertNotNull( g.createSubject().getId() );
    Assert.assertNotNull( g.createTime() );
    Assert.assertNull( g.modifySource() );
    Assert.assertNotNull( g.modifySubject().getId() );
    Assert.assertNotNull( g.modifyTime() );
    s.stop();
  }

  public void testFetchAttrs17() {
    Subject         subj  = GrouperSubject.load( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    Assert.assertNotNull(subj);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // ns00
    String type = Grouper.NS_TYPE;
    String stem = ns_stem00;
    String extn = ns_extn00; 
    String name = GrouperGroup.groupName(stem, extn);
    String desc = "My New Description: " + name;
    GrouperGroup g = GrouperGroup.load(s, stem, extn, type);
    Assert.assertNotNull(g);
    Assert.assertNotNull( g.id() );
    Assert.assertNotNull( g.type() );
    Assert.assertNotNull( g.attribute("stem") );
    Assert.assertTrue( g.attribute("stem").value().equals(stem) );
    Assert.assertNotNull( g.attribute("extension") );
    Assert.assertTrue( g.attribute("extension").value().equals(extn) );
    Assert.assertNotNull(name);
    Assert.assertTrue( g.attribute("name").value().equals(name) );
    Assert.assertNotNull( g.attribute("description") );
    Assert.assertTrue( g.attribute("description").value().equals(desc) );
    Assert.assertNull( g.createSource() );
    Assert.assertNotNull( g.createSubject().getId() );
    Assert.assertNotNull( g.createTime() );
    Assert.assertNull( g.modifySource() );
    Assert.assertNotNull( g.modifySubject().getId() );
    Assert.assertNotNull( g.modifyTime() );
    s.stop();
  }

  public void testFetchAttrs18() {
    Subject         subj  = GrouperSubject.load( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    Assert.assertNotNull(subj);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // ns1
    String type = Grouper.NS_TYPE;
    String extn = ns_extn1; 
    String name = GrouperGroup.groupName(Grouper.NS_ROOT, extn);
    String desc = "My New Description: " + name;
    GrouperGroup g = GrouperGroup.load(s, Grouper.NS_ROOT, extn, type);
    Assert.assertNotNull(g);
    Assert.assertNotNull( g.id() );
    Assert.assertNotNull( g.type() );
    Assert.assertNotNull( g.attribute("stem") );
    Assert.assertTrue( g.attribute("stem").value().equals(Grouper.NS_ROOT) );
    Assert.assertNotNull( g.attribute("extension") );
    Assert.assertTrue( g.attribute("extension").value().equals(extn) );
    Assert.assertNotNull(name);
    Assert.assertTrue( g.attribute("name").value().equals(name) );
    Assert.assertNotNull( g.attribute("description") );
    Assert.assertTrue( g.attribute("description").value().equals(desc) );
    Assert.assertNull( g.createSource() );
    Assert.assertNotNull( g.createSubject().getId() );
    Assert.assertNotNull( g.createTime() );
    Assert.assertNull( g.modifySource() );
    Assert.assertNotNull( g.modifySubject().getId() );
    Assert.assertNotNull( g.modifyTime() );
    s.stop();
  }

  public void testFetchAttrs19() {
    Subject         subj  = GrouperSubject.load( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    Assert.assertNotNull(subj);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // ns2
    String type = Grouper.NS_TYPE;
    String extn = ns_extn2; 
    String name = GrouperGroup.groupName(Grouper.NS_ROOT, extn);
    String desc = "My New Description: " + name;
    GrouperGroup g = GrouperGroup.load(s, Grouper.NS_ROOT, extn, type);
    Assert.assertNotNull(g);
    Assert.assertNotNull( g.id() );
    Assert.assertNotNull( g.type() );
    Assert.assertNotNull( g.attribute("stem") );
    Assert.assertTrue( g.attribute("stem").value().equals(Grouper.NS_ROOT) );
    Assert.assertNotNull( g.attribute("extension") );
    Assert.assertTrue( g.attribute("extension").value().equals(extn) );
    Assert.assertNotNull(name);
    Assert.assertTrue( g.attribute("name").value().equals(name) );
    Assert.assertNotNull( g.attribute("description") );
    Assert.assertTrue( g.attribute("description").value().equals(desc) );
    Assert.assertNull( g.createSource() );
    Assert.assertNotNull( g.createSubject().getId() );
    Assert.assertNotNull( g.createTime() );
    Assert.assertNull( g.modifySource() );
    Assert.assertNotNull( g.modifySubject().getId() );
    Assert.assertNotNull( g.modifyTime() );
    s.stop();
  }

  public void testFetchAttrs20() {
    Subject         subj  = GrouperSubject.load( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    Assert.assertNotNull(subj);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // g0
    String type = Grouper.DEF_GROUP_TYPE;
    String stem = stem0;
    String extn = extn0; 
    String name = GrouperGroup.groupName(stem, extn);
    String desc = "My New Description: " + name;
    GrouperGroup g = GrouperGroup.load(s, stem, extn, type);
    Assert.assertNotNull(g);
    Assert.assertNotNull( g.id() );
    Assert.assertNotNull( g.type() );
    Assert.assertNotNull( g.attribute("stem") );
    Assert.assertTrue( g.attribute("stem").value().equals(stem) );
    Assert.assertNotNull( g.attribute("extension") );
    Assert.assertTrue( g.attribute("extension").value().equals(extn) );
    Assert.assertNotNull(name);
    Assert.assertTrue( g.attribute("name").value().equals(name) );
    Assert.assertNotNull( g.attribute("description") );
    Assert.assertTrue( g.attribute("description").value().equals(desc) );
    Assert.assertNull( g.createSource() );
    Assert.assertNotNull( g.createSubject().getId() );
    Assert.assertNotNull( g.createTime() );
    Assert.assertNull( g.modifySource() );
    Assert.assertNotNull( g.modifySubject().getId() );
    Assert.assertNotNull( g.modifyTime() );
    s.stop();
  }

  public void testFetchAttrs21() {
    Subject         subj  = GrouperSubject.load( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    Assert.assertNotNull(subj);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // g1
    String type = Grouper.DEF_GROUP_TYPE;
    String stem = stem1;
    String extn = extn1; 
    String name = GrouperGroup.groupName(stem, extn);
    String desc = "My New Description: " + name;
    GrouperGroup g = GrouperGroup.load(s, stem, extn, type);
    Assert.assertNotNull(g);
    Assert.assertNotNull( g.id() );
    Assert.assertNotNull( g.type() );
    Assert.assertNotNull( g.attribute("stem") );
    Assert.assertTrue( g.attribute("stem").value().equals(stem) );
    Assert.assertNotNull( g.attribute("extension") );
    Assert.assertTrue( g.attribute("extension").value().equals(extn) );
    Assert.assertNotNull(name);
    Assert.assertTrue( g.attribute("name").value().equals(name) );
    Assert.assertNotNull( g.attribute("description") );
    Assert.assertTrue( g.attribute("description").value().equals(desc) );
    Assert.assertNull( g.createSource() );
    Assert.assertNotNull( g.createSubject().getId() );
    Assert.assertNotNull( g.createTime() );
    Assert.assertNull( g.modifySource() );
    Assert.assertNotNull( g.modifySubject().getId() );
    Assert.assertNotNull( g.modifyTime() );
    s.stop();
  }

  public void testFetchAttrs22() {
    Subject         subj  = GrouperSubject.load( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    Assert.assertNotNull(subj);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // g2 
    String type = Grouper.DEF_GROUP_TYPE;
    String stem = stem2;
    String extn = extn2; 
    String name = GrouperGroup.groupName(stem, extn);
    String desc = "My New Description: " + name;
    GrouperGroup g = GrouperGroup.load(s, stem, extn, type);
    Assert.assertNotNull(g);
    Assert.assertNotNull( g.id() );
    Assert.assertNotNull( g.type() );
    Assert.assertNotNull( g.attribute("stem") );
    Assert.assertTrue( g.attribute("stem").value().equals(stem) );
    Assert.assertNotNull( g.attribute("extension") );
    Assert.assertTrue( g.attribute("extension").value().equals(extn) );
    Assert.assertNotNull(name);
    Assert.assertTrue( g.attribute("name").value().equals(name) );
    Assert.assertNotNull( g.attribute("description") );
    Assert.assertTrue( g.attribute("description").value().equals(desc) );
    Assert.assertNull( g.createSource() );
    Assert.assertNotNull( g.createSubject().getId() );
    Assert.assertNotNull( g.createTime() );
    Assert.assertNull( g.modifySource() );
    Assert.assertNotNull( g.modifySubject().getId() );
    Assert.assertNotNull( g.modifyTime() );
    s.stop();
  }

  public void testFetchAttrs23() {
    Subject         subj  = GrouperSubject.load( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    Assert.assertNotNull(subj);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // g3
    String type = Grouper.DEF_GROUP_TYPE;
    String stem = stem3;
    String extn = extn3; 
    String name = GrouperGroup.groupName(stem, extn);
    String desc = "My New Description: " + name;
    GrouperGroup g = GrouperGroup.load(s, stem, extn, type);
    Assert.assertNotNull(g);
    Assert.assertNotNull( g.id() );
    Assert.assertNotNull( g.type() );
    Assert.assertNotNull( g.attribute("stem") );
    Assert.assertTrue( g.attribute("stem").value().equals(stem) );
    Assert.assertNotNull( g.attribute("extension") );
    Assert.assertTrue( g.attribute("extension").value().equals(extn) );
    Assert.assertNotNull(name);
    Assert.assertTrue( g.attribute("name").value().equals(name) );
    Assert.assertNotNull( g.attribute("description") );
    Assert.assertTrue( g.attribute("description").value().equals(desc) );
    Assert.assertNull( g.createSource() );
    Assert.assertNotNull( g.createSubject().getId() );
    Assert.assertNotNull( g.createTime() );
    Assert.assertNull( g.modifySource() );
    Assert.assertNotNull( g.modifySubject().getId() );
    Assert.assertNotNull( g.modifyTime() );
    s.stop();
  }

  // 
  // Delete the description of every group
  //

  public void testDelAttr0() {
    Subject         subj  = GrouperSubject.load( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    Assert.assertNotNull(subj);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // ns0
    String type = Grouper.NS_TYPE;
    String extn = ns_extn0; 
    String name = GrouperGroup.groupName(Grouper.NS_ROOT, extn);
    GrouperGroup g = GrouperGroup.load(s, Grouper.NS_ROOT, extn, type);
    Assert.assertNotNull(g);
    Assert.assertTrue( g.attribute("description", null) );
    s.stop();
  }

  public void testDelAttr1() {
    Subject         subj  = GrouperSubject.load( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    Assert.assertNotNull(subj);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // ns00
    String type = Grouper.NS_TYPE;
    String stem = ns_stem00;
    String extn = ns_extn00; 
    String name = GrouperGroup.groupName(stem, extn);
    GrouperGroup g = GrouperGroup.load(s, stem, extn, type);
    Assert.assertNotNull(g);
    Assert.assertTrue( g.attribute("description", null) );
    s.stop();
  }

  public void testDelAttr2() {
    Subject         subj  = GrouperSubject.load( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    Assert.assertNotNull(subj);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // ns1
    String type = Grouper.NS_TYPE;
    String extn = ns_extn1; 
    String name = GrouperGroup.groupName(Grouper.NS_ROOT, extn);
    GrouperGroup g = GrouperGroup.load(s, Grouper.NS_ROOT, extn, type);
    Assert.assertNotNull(g);
    Assert.assertTrue( g.attribute("description", null) );
    s.stop();
  }

  public void testDelAttr3() {
    Subject         subj  = GrouperSubject.load( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    Assert.assertNotNull(subj);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // ns2
    String type = Grouper.NS_TYPE;
    String extn = ns_extn2; 
    String name = GrouperGroup.groupName(Grouper.NS_ROOT, extn);
    GrouperGroup g = GrouperGroup.load(s, Grouper.NS_ROOT, extn, type);
    Assert.assertNotNull(g);
    Assert.assertTrue( g.attribute("description", null) );
    s.stop();
  }

  public void testDelAttr4() {
    Subject         subj  = GrouperSubject.load( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    Assert.assertNotNull(subj);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // g0
    String type = Grouper.DEF_GROUP_TYPE;
    String stem = stem0;
    String extn = extn0; 
    String name = GrouperGroup.groupName(stem, extn);
    GrouperGroup g = GrouperGroup.load(s, stem, extn, type);
    Assert.assertNotNull(g);
    Assert.assertTrue( g.attribute("description", null) );
    s.stop();
  }

  public void testDelAttr5() {
    Subject         subj  = GrouperSubject.load( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    Assert.assertNotNull(subj);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // g1
    String type = Grouper.DEF_GROUP_TYPE;
    String stem = stem1;
    String extn = extn1; 
    String name = GrouperGroup.groupName(stem, extn);
    GrouperGroup g = GrouperGroup.load(s, stem, extn, type);
    Assert.assertNotNull(g);
    Assert.assertTrue( g.attribute("description", null) );
    s.stop();
  }

  public void testDelAttr6() {
    Subject         subj  = GrouperSubject.load( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    Assert.assertNotNull(subj);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // g2
    String type = Grouper.DEF_GROUP_TYPE;
    String stem = stem2;
    String extn = extn2; 
    String name = GrouperGroup.groupName(stem, extn);
    GrouperGroup g = GrouperGroup.load(s, stem, extn, type);
    Assert.assertNotNull(g);
    Assert.assertTrue( g.attribute("description", null) );
    s.stop();
  }

  public void testDelAttr7() {
    Subject         subj  = GrouperSubject.load( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    Assert.assertNotNull(subj);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // g3
    String type = Grouper.DEF_GROUP_TYPE;
    String stem = stem3;
    String extn = extn3; 
    String name = GrouperGroup.groupName(stem, extn);
    GrouperGroup g = GrouperGroup.load(s, stem, extn, type);
    Assert.assertNotNull(g);
    Assert.assertTrue( g.attribute("description", null) );
    s.stop();
  }


  // 
  // Test deletion of `description' attribute
  //

  public void testFetchAttrs24() {
    Subject         subj  = GrouperSubject.load( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    Assert.assertNotNull(subj);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // ns0
    String type = Grouper.NS_TYPE;
    String extn = ns_extn0; 
    String name = GrouperGroup.groupName(Grouper.NS_ROOT, extn);
    GrouperGroup g = GrouperGroup.load(s, Grouper.NS_ROOT, extn, type);
    Assert.assertNotNull(g);
    Assert.assertNotNull( g.id() );
    Assert.assertNotNull( g.type() );
    Assert.assertNotNull( g.attribute("stem") );
    Assert.assertTrue( g.attribute("stem").value().equals(Grouper.NS_ROOT) );
    Assert.assertNotNull( g.attribute("extension") );
    Assert.assertTrue( g.attribute("extension").value().equals(extn) );
    Assert.assertNotNull(name);
    Assert.assertTrue( g.attribute("name").value().equals(name) );
    Assert.assertNull( g.attribute("description") );
    Assert.assertNull( g.createSource() );
    Assert.assertNotNull( g.createSubject().getId() );
    Assert.assertNotNull( g.createTime() );
    Assert.assertNull( g.modifySource() );
    Assert.assertNotNull( g.modifySubject().getId() );
    Assert.assertNotNull( g.modifyTime() );
    s.stop();
  }

  public void testFetchAttrs25() {
    Subject         subj  = GrouperSubject.load( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    Assert.assertNotNull(subj);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // ns00
    String type = Grouper.NS_TYPE;
    String stem = ns_stem00;
    String extn = ns_extn00; 
    String name = GrouperGroup.groupName(stem, extn);
    GrouperGroup g = GrouperGroup.load(s, stem, extn, type);
    Assert.assertNotNull(g);
    Assert.assertNotNull( g.id() );
    Assert.assertNotNull( g.type() );
    Assert.assertNotNull( g.attribute("stem") );
    Assert.assertTrue( g.attribute("stem").value().equals(stem) );
    Assert.assertNotNull( g.attribute("extension") );
    Assert.assertTrue( g.attribute("extension").value().equals(extn) );
    Assert.assertNotNull(name);
    Assert.assertTrue( g.attribute("name").value().equals(name) );
    Assert.assertNull( g.attribute("description") );
    Assert.assertNull( g.createSource() );
    Assert.assertNotNull( g.createSubject().getId() );
    Assert.assertNotNull( g.createTime() );
    Assert.assertNull( g.modifySource() );
    Assert.assertNotNull( g.modifySubject().getId() );
    Assert.assertNotNull( g.modifyTime() );
    s.stop();
  }

  public void testFetchAttrs26() {
    Subject         subj  = GrouperSubject.load( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    Assert.assertNotNull(subj);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // ns1
    String type = Grouper.NS_TYPE;
    String extn = ns_extn1; 
    String name = GrouperGroup.groupName(Grouper.NS_ROOT, extn);
    GrouperGroup g = GrouperGroup.load(s, Grouper.NS_ROOT, extn, type);
    Assert.assertNotNull(g);
    Assert.assertNotNull( g.id() );
    Assert.assertNotNull( g.type() );
    Assert.assertNotNull( g.attribute("stem") );
    Assert.assertTrue( g.attribute("stem").value().equals(Grouper.NS_ROOT) );
    Assert.assertNotNull( g.attribute("extension") );
    Assert.assertTrue( g.attribute("extension").value().equals(extn) );
    Assert.assertNotNull(name);
    Assert.assertTrue( g.attribute("name").value().equals(name) );
    Assert.assertNull( g.attribute("description") );
    Assert.assertNull( g.createSource() );
    Assert.assertNotNull( g.createSubject().getId() );
    Assert.assertNotNull( g.createTime() );
    Assert.assertNull( g.modifySource() );
    Assert.assertNotNull( g.modifySubject().getId() );
    Assert.assertNotNull( g.modifyTime() );
    s.stop();
  }

  public void testFetchAttrs27() {
    Subject         subj  = GrouperSubject.load( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    Assert.assertNotNull(subj);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // ns2
    String type = Grouper.NS_TYPE;
    String extn = ns_extn2; 
    String name = GrouperGroup.groupName(Grouper.NS_ROOT, extn);
    GrouperGroup g = GrouperGroup.load(s, Grouper.NS_ROOT, extn, type);
    Assert.assertNotNull(g);
    Assert.assertNotNull( g.id() );
    Assert.assertNotNull( g.type() );
    Assert.assertNotNull( g.attribute("stem") );
    Assert.assertTrue( g.attribute("stem").value().equals(Grouper.NS_ROOT) );
    Assert.assertNotNull( g.attribute("extension") );
    Assert.assertTrue( g.attribute("extension").value().equals(extn) );
    Assert.assertNotNull(name);
    Assert.assertTrue( g.attribute("name").value().equals(name) );
    Assert.assertNull( g.attribute("description") );
    Assert.assertNull( g.createSource() );
    Assert.assertNotNull( g.createSubject().getId() );
    Assert.assertNotNull( g.createTime() );
    Assert.assertNull( g.modifySource() );
    Assert.assertNotNull( g.modifySubject().getId() );
    Assert.assertNotNull( g.modifyTime() );
    s.stop();
  }

  public void testFetchAttrs28() {
    Subject         subj  = GrouperSubject.load( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    Assert.assertNotNull(subj);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // g0
    String type = Grouper.DEF_GROUP_TYPE;
    String stem = stem0;
    String extn = extn0; 
    String name = GrouperGroup.groupName(stem, extn);
    GrouperGroup g = GrouperGroup.load(s, stem, extn, type);
    Assert.assertNotNull(g);
    Assert.assertNotNull( g.id() );
    Assert.assertNotNull( g.type() );
    Assert.assertNotNull( g.attribute("stem") );
    Assert.assertTrue( g.attribute("stem").value().equals(stem) );
    Assert.assertNotNull( g.attribute("extension") );
    Assert.assertTrue( g.attribute("extension").value().equals(extn) );
    Assert.assertNotNull(name);
    Assert.assertTrue( g.attribute("name").value().equals(name) );
    Assert.assertNull( g.attribute("description") );
    Assert.assertNull( g.createSource() );
    Assert.assertNotNull( g.createSubject().getId() );
    Assert.assertNotNull( g.createTime() );
    Assert.assertNull( g.modifySource() );
    Assert.assertNotNull( g.modifySubject().getId() );
    Assert.assertNotNull( g.modifyTime() );
    s.stop();
  }

  public void testFetchAttrs29() {
    Subject         subj  = GrouperSubject.load( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    Assert.assertNotNull(subj);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // g1
    String type = Grouper.DEF_GROUP_TYPE;
    String stem = stem1;
    String extn = extn1; 
    String name = GrouperGroup.groupName(stem, extn);
    GrouperGroup g = GrouperGroup.load(s, stem, extn, type);
    Assert.assertNotNull(g);
    Assert.assertNotNull( g.id() );
    Assert.assertNotNull( g.type() );
    Assert.assertNotNull( g.attribute("stem") );
    Assert.assertTrue( g.attribute("stem").value().equals(stem) );
    Assert.assertNotNull( g.attribute("extension") );
    Assert.assertTrue( g.attribute("extension").value().equals(extn) );
    Assert.assertNotNull(name);
    Assert.assertTrue( g.attribute("name").value().equals(name) );
    Assert.assertNull( g.attribute("description") );
    Assert.assertNull( g.createSource() );
    Assert.assertNotNull( g.createSubject().getId() );
    Assert.assertNotNull( g.createTime() );
    Assert.assertNull( g.modifySource() );
    Assert.assertNotNull( g.modifySubject().getId() );
    Assert.assertNotNull( g.modifyTime() );
    s.stop();
  }

  public void testFetchAttrs30() {
    Subject         subj  = GrouperSubject.load( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    Assert.assertNotNull(subj);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // g2 
    String type = Grouper.DEF_GROUP_TYPE;
    String stem = stem2;
    String extn = extn2; 
    String name = GrouperGroup.groupName(stem, extn);
    GrouperGroup g = GrouperGroup.load(s, stem, extn, type);
    Assert.assertNotNull(g);
    Assert.assertNotNull( g.id() );
    Assert.assertNotNull( g.type() );
    Assert.assertNotNull( g.attribute("stem") );
    Assert.assertTrue( g.attribute("stem").value().equals(stem) );
    Assert.assertNotNull( g.attribute("extension") );
    Assert.assertTrue( g.attribute("extension").value().equals(extn) );
    Assert.assertNotNull(name);
    Assert.assertTrue( g.attribute("name").value().equals(name) );
    Assert.assertNull( g.attribute("description") );
    Assert.assertNull( g.createSource() );
    Assert.assertNotNull( g.createSubject().getId() );
    Assert.assertNotNull( g.createTime() );
    Assert.assertNull( g.modifySource() );
    Assert.assertNotNull( g.modifySubject().getId() );
    Assert.assertNotNull( g.modifyTime() );
    s.stop();
  }

  public void testFetchAttrs31() {
    Subject         subj  = GrouperSubject.load( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    Assert.assertNotNull(subj);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // g3
    String type = Grouper.DEF_GROUP_TYPE;
    String stem = stem3;
    String extn = extn3; 
    String name = GrouperGroup.groupName(stem, extn);
    GrouperGroup g = GrouperGroup.load(s, stem, extn, type);
    Assert.assertNotNull(g);
    Assert.assertNotNull( g.id() );
    Assert.assertNotNull( g.type() );
    Assert.assertNotNull( g.attribute("stem") );
    Assert.assertTrue( g.attribute("stem").value().equals(stem) );
    Assert.assertNotNull( g.attribute("extension") );
    Assert.assertTrue( g.attribute("extension").value().equals(extn) );
    Assert.assertNotNull(name);
    Assert.assertTrue( g.attribute("name").value().equals(name) );
    Assert.assertNull( g.attribute("description") );
    Assert.assertNull( g.createSource() );
    Assert.assertNotNull( g.createSubject().getId() );
    Assert.assertNotNull( g.createTime() );
    Assert.assertNull( g.modifySource() );
    Assert.assertNotNull( g.modifySubject().getId() );
    Assert.assertNotNull( g.modifyTime() );
    s.stop();
  }

  //
  // Test changing the stem and extension attributes
  //

  public void testAddAttr16() {
    Subject         subj  = GrouperSubject.load( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    Assert.assertNotNull(subj);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // g0
    String type = Grouper.DEF_GROUP_TYPE;
    String stem = stem0;
    String extn = extn0; 
    String name = GrouperGroup.groupName(stem, extn);
    GrouperGroup g = GrouperGroup.load(s, stem, extn, type);
    Assert.assertNotNull(g);
    Assert.assertFalse( g.attribute("stem", null) );
    s.stop();
  }

  public void testAddAttr17() {
    Subject         subj  = GrouperSubject.load( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    Assert.assertNotNull(subj);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // g0
    String type = Grouper.DEF_GROUP_TYPE;
    String stem = stem0;
    String extn = extn0; 
    String name = GrouperGroup.groupName(stem, extn);
    GrouperGroup g = GrouperGroup.load(s, stem, extn, type);
    Assert.assertNotNull(g);
    Assert.assertFalse( g.attribute("stem", extn) );
    s.stop();
  }

  public void testAddAttr18() {
    Subject         subj  = GrouperSubject.load( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    Assert.assertNotNull(subj);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // g0
    String type = Grouper.DEF_GROUP_TYPE;
    String stem = stem0;
    String extn = extn0; 
    String name = GrouperGroup.groupName(stem, extn);
    GrouperGroup g = GrouperGroup.load(s, stem, extn, type);
    Assert.assertNotNull(g);
    Assert.assertFalse( g.attribute("extension", null) );
    s.stop();
  }

  public void testAddAttr19() {
    Subject         subj  = GrouperSubject.load( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    Assert.assertNotNull(subj);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull(s);

    // g0
    String type = Grouper.DEF_GROUP_TYPE;
    String stem = stem0;
    String extn = extn0; 
    String name = GrouperGroup.groupName(stem, extn);
    GrouperGroup g = GrouperGroup.load(s, stem, extn, type);
    Assert.assertNotNull(g);
    Assert.assertFalse( g.attribute("extension", stem) );
    s.stop();
  }

}

