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


public class TestGroupAttrs extends TestCase {

  /// Naming Groups
  private String  ns_stem0  = Grouper.NS_ROOT;
  private String  ns_extn0  = "stem.0";
  private String  ns_stem00 = "stem.0";
  private String  ns_extn00 = "stem.0.0";
  private String  ns_stem1  = Grouper.NS_ROOT;
  private String  ns_extn1  = "stem.1";
  private String  ns_stem2  = Grouper.NS_ROOT;
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
    Subject         subj  = GrouperSubject.lookup( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    Assert.assertNotNull(subj);
    s.start(subj);
    // Fetch namespaces
    // ns0
    GrouperGroup ns0 = GrouperGroup.load(s, ns_stem0, ns_extn0, Grouper.NS_TYPE);
    Assert.assertNotNull(ns0);
    // ns00
    GrouperGroup ns00 = GrouperGroup.load(s, ns_stem00, ns_extn00, Grouper.NS_TYPE);
    Assert.assertNotNull(ns00);
    // ns1
    GrouperGroup ns1 = GrouperGroup.load(s, ns_stem1, ns_extn1, Grouper.NS_TYPE);
    Assert.assertNotNull(ns1);
    // ns2
    GrouperGroup ns2 = GrouperGroup.load(s, ns_stem2, ns_extn2, Grouper.NS_TYPE);
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
    GrouperSession  s     = new GrouperSession();
    Assert.assertNotNull(s);
    Subject         subj  = GrouperSubject.lookup( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    Assert.assertNotNull(subj);
    s.start(subj);
    // ns0
    String type = Grouper.NS_TYPE;
    String stem = ns_stem0;
    String extn = ns_extn0; 
    String name = GrouperBackend.groupName(stem, extn);
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
  }

  public void testFetchAttrs1() {
    GrouperSession  s     = new GrouperSession();
    Assert.assertNotNull(s);
    Subject         subj  = GrouperSubject.lookup( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    Assert.assertNotNull(subj);
    s.start(subj);
    // ns00
    String type = Grouper.NS_TYPE;
    String stem = ns_stem00;
    String extn = ns_extn00; 
    String name = GrouperBackend.groupName(stem, extn);
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
  }

  public void testFetchAttrs2() {
    GrouperSession  s     = new GrouperSession();
    Assert.assertNotNull(s);
    Subject         subj  = GrouperSubject.lookup( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    Assert.assertNotNull(subj);
    s.start(subj);
    // ns1
    String type = Grouper.NS_TYPE;
    String stem = ns_stem1;
    String extn = ns_extn1; 
    String name = GrouperBackend.groupName(stem, extn);
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
  }

  public void testFetchAttrs3() {
    GrouperSession  s     = new GrouperSession();
    Assert.assertNotNull(s);
    Subject         subj  = GrouperSubject.lookup( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    Assert.assertNotNull(subj);
    s.start(subj);
    // ns2
    String type = Grouper.NS_TYPE;
    String stem = ns_stem2;
    String extn = ns_extn2; 
    String name = GrouperBackend.groupName(stem, extn);
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
  }

  public void testFetchAttrs4() {
    GrouperSession  s     = new GrouperSession();
    Assert.assertNotNull(s);
    Subject         subj  = GrouperSubject.lookup( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    Assert.assertNotNull(subj);
    s.start(subj);
    // g0
    String type = Grouper.DEF_GROUP_TYPE;
    String stem = stem0;
    String extn = extn0; 
    String name = GrouperBackend.groupName(stem, extn);
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
  }

  public void testFetchAttrs5() {
    GrouperSession  s     = new GrouperSession();
    Assert.assertNotNull(s);
    Subject         subj  = GrouperSubject.lookup( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    Assert.assertNotNull(subj);
    s.start(subj);
    // g1
    String type = Grouper.DEF_GROUP_TYPE;
    String stem = stem1;
    String extn = extn1; 
    String name = GrouperBackend.groupName(stem, extn);
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
  }

  public void testFetchAttrs6() {
    GrouperSession  s     = new GrouperSession();
    Assert.assertNotNull(s);
    Subject         subj  = GrouperSubject.lookup( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    Assert.assertNotNull(subj);
    s.start(subj);
    // g2 
    String type = Grouper.DEF_GROUP_TYPE;
    String stem = stem2;
    String extn = extn2; 
    String name = GrouperBackend.groupName(stem, extn);
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
  }

  public void testFetchAttrs7() {
    GrouperSession  s     = new GrouperSession();
    Assert.assertNotNull(s);
    Subject         subj  = GrouperSubject.lookup( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    Assert.assertNotNull(subj);
    s.start(subj);
    // g3
    String type = Grouper.DEF_GROUP_TYPE;
    String stem = stem3;
    String extn = extn3; 
    String name = GrouperBackend.groupName(stem, extn);
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
  }


  // 
  // Give every group a description
  //

  public void testAddAttr0() {
    GrouperSession  s     = new GrouperSession();
    Assert.assertNotNull(s);
    Subject         subj  = GrouperSubject.lookup( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    Assert.assertNotNull(subj);
    s.start(subj);
    // ns0
    String type = Grouper.NS_TYPE;
    String stem = ns_stem0;
    String extn = ns_extn0; 
    String name = GrouperBackend.groupName(stem, extn);
    String desc = "My Description: " + name;
    GrouperGroup g = GrouperGroup.load(s, stem, extn, type);
    Assert.assertNotNull(g);
    Assert.assertTrue( g.attribute("description", desc) );
    s.stop();
  }

  public void testAddAttr1() {
    GrouperSession  s     = new GrouperSession();
    Assert.assertNotNull(s);
    Subject         subj  = GrouperSubject.lookup( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    Assert.assertNotNull(subj);
    s.start(subj);
    // ns00
    String type = Grouper.NS_TYPE;
    String stem = ns_stem00;
    String extn = ns_extn00; 
    String name = GrouperBackend.groupName(stem, extn);
    String desc = "My Description: " + name;
    GrouperGroup g = GrouperGroup.load(s, stem, extn, type);
    Assert.assertNotNull(g);
    Assert.assertTrue( g.attribute("description", desc) );
    s.stop();
  }

  public void testAddAttr2() {
    GrouperSession  s     = new GrouperSession();
    Assert.assertNotNull(s);
    Subject         subj  = GrouperSubject.lookup( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    Assert.assertNotNull(subj);
    s.start(subj);
    // ns1
    String type = Grouper.NS_TYPE;
    String stem = ns_stem1;
    String extn = ns_extn1; 
    String name = GrouperBackend.groupName(stem, extn);
    String desc = "My Description: " + name;
    GrouperGroup g = GrouperGroup.load(s, stem, extn, type);
    Assert.assertNotNull(g);
    Assert.assertTrue( g.attribute("description", desc) );
    s.stop();
  }

  public void testAddAttr3() {
    GrouperSession  s     = new GrouperSession();
    Assert.assertNotNull(s);
    Subject         subj  = GrouperSubject.lookup( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    Assert.assertNotNull(subj);
    s.start(subj);
    // ns2
    String type = Grouper.NS_TYPE;
    String stem = ns_stem2;
    String extn = ns_extn2; 
    String name = GrouperBackend.groupName(stem, extn);
    String desc = "My Description: " + name;
    GrouperGroup g = GrouperGroup.load(s, stem, extn, type);
    Assert.assertNotNull(g);
    Assert.assertTrue( g.attribute("description", desc) );
    s.stop();
  }

  public void testAddAttr4() {
    GrouperSession  s     = new GrouperSession();
    Assert.assertNotNull(s);
    Subject         subj  = GrouperSubject.lookup( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    Assert.assertNotNull(subj);
    s.start(subj);
    // g0
    String type = Grouper.DEF_GROUP_TYPE;
    String stem = stem0;
    String extn = extn0; 
    String name = GrouperBackend.groupName(stem, extn);
    String desc = "My Description: " + name;
    GrouperGroup g = GrouperGroup.load(s, stem, extn, type);
    Assert.assertNotNull(g);
    Assert.assertTrue( g.attribute("description", desc) );
    s.stop();
  }

  public void testAddAttr5() {
    GrouperSession  s     = new GrouperSession();
    Assert.assertNotNull(s);
    Subject         subj  = GrouperSubject.lookup( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    Assert.assertNotNull(subj);
    s.start(subj);
    // g1
    String type = Grouper.DEF_GROUP_TYPE;
    String stem = stem1;
    String extn = extn1; 
    String name = GrouperBackend.groupName(stem, extn);
    String desc = "My Description: " + name;
    GrouperGroup g = GrouperGroup.load(s, stem, extn, type);
    Assert.assertNotNull(g);
    Assert.assertTrue( g.attribute("description", desc) );
    s.stop();
  }

  public void testAddAttr6() {
    GrouperSession  s     = new GrouperSession();
    Assert.assertNotNull(s);
    Subject         subj  = GrouperSubject.lookup( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    Assert.assertNotNull(subj);
    s.start(subj);
    // g2
    String type = Grouper.DEF_GROUP_TYPE;
    String stem = stem2;
    String extn = extn2; 
    String name = GrouperBackend.groupName(stem, extn);
    String desc = "My Description: " + name;
    GrouperGroup g = GrouperGroup.load(s, stem, extn, type);
    Assert.assertNotNull(g);
    Assert.assertTrue( g.attribute("description", desc) );
    s.stop();
  }

  public void testAddAttr7() {
    GrouperSession  s     = new GrouperSession();
    Assert.assertNotNull(s);
    Subject         subj  = GrouperSubject.lookup( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    Assert.assertNotNull(subj);
    s.start(subj);
    // g3
    String type = Grouper.DEF_GROUP_TYPE;
    String stem = stem3;
    String extn = extn3; 
    String name = GrouperBackend.groupName(stem, extn);
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
    GrouperSession  s     = new GrouperSession();
    Assert.assertNotNull(s);
    Subject         subj  = GrouperSubject.lookup( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    Assert.assertNotNull(subj);
    s.start(subj);
    // ns0
    String type = Grouper.NS_TYPE;
    String stem = ns_stem0;
    String extn = ns_extn0; 
    String name = GrouperBackend.groupName(stem, extn);
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
  }

  public void testFetchAttrs9() {
    GrouperSession  s     = new GrouperSession();
    Assert.assertNotNull(s);
    Subject         subj  = GrouperSubject.lookup( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    Assert.assertNotNull(subj);
    s.start(subj);
    // ns00
    String type = Grouper.NS_TYPE;
    String stem = ns_stem00;
    String extn = ns_extn00; 
    String name = GrouperBackend.groupName(stem, extn);
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
  }

  public void testFetchAttrs10() {
    GrouperSession  s     = new GrouperSession();
    Assert.assertNotNull(s);
    Subject         subj  = GrouperSubject.lookup( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    Assert.assertNotNull(subj);
    s.start(subj);
    // ns1
    String type = Grouper.NS_TYPE;
    String stem = ns_stem1;
    String extn = ns_extn1; 
    String name = GrouperBackend.groupName(stem, extn);
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
  }

  public void testFetchAttrs11() {
    GrouperSession  s     = new GrouperSession();
    Assert.assertNotNull(s);
    Subject         subj  = GrouperSubject.lookup( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    Assert.assertNotNull(subj);
    s.start(subj);
    // ns2
    String type = Grouper.NS_TYPE;
    String stem = ns_stem2;
    String extn = ns_extn2; 
    String name = GrouperBackend.groupName(stem, extn);
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
  }

  public void testFetchAttrs12() {
    GrouperSession  s     = new GrouperSession();
    Assert.assertNotNull(s);
    Subject         subj  = GrouperSubject.lookup( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    Assert.assertNotNull(subj);
    s.start(subj);
    // g0
    String type = Grouper.DEF_GROUP_TYPE;
    String stem = stem0;
    String extn = extn0; 
    String name = GrouperBackend.groupName(stem, extn);
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
  }

  public void testFetchAttrs13() {
    GrouperSession  s     = new GrouperSession();
    Assert.assertNotNull(s);
    Subject         subj  = GrouperSubject.lookup( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    Assert.assertNotNull(subj);
    s.start(subj);
    // g1
    String type = Grouper.DEF_GROUP_TYPE;
    String stem = stem1;
    String extn = extn1; 
    String name = GrouperBackend.groupName(stem, extn);
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
  }

  public void testFetchAttrs14() {
    GrouperSession  s     = new GrouperSession();
    Assert.assertNotNull(s);
    Subject         subj  = GrouperSubject.lookup( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    Assert.assertNotNull(subj);
    s.start(subj);
    // g2 
    String type = Grouper.DEF_GROUP_TYPE;
    String stem = stem2;
    String extn = extn2; 
    String name = GrouperBackend.groupName(stem, extn);
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
  }

  public void testFetchAttrs15() {
    GrouperSession  s     = new GrouperSession();
    Assert.assertNotNull(s);
    Subject         subj  = GrouperSubject.lookup( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    Assert.assertNotNull(subj);
    s.start(subj);
    // g3
    String type = Grouper.DEF_GROUP_TYPE;
    String stem = stem3;
    String extn = extn3; 
    String name = GrouperBackend.groupName(stem, extn);
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
  }

  // 
  // Update the description of every group
  //

  public void testAddAttr8() {
    GrouperSession  s     = new GrouperSession();
    Assert.assertNotNull(s);
    Subject         subj  = GrouperSubject.lookup( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    Assert.assertNotNull(subj);
    s.start(subj);
    // ns0
    String type = Grouper.NS_TYPE;
    String stem = ns_stem0;
    String extn = ns_extn0; 
    String name = GrouperBackend.groupName(stem, extn);
    String desc = "My New Description: " + name;
    GrouperGroup g = GrouperGroup.load(s, stem, extn, type);
    Assert.assertNotNull(g);
    Assert.assertTrue( g.attribute("description", desc) );
    s.stop();
  }

  public void testAddAttr9() {
    GrouperSession  s     = new GrouperSession();
    Assert.assertNotNull(s);
    Subject         subj  = GrouperSubject.lookup( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    Assert.assertNotNull(subj);
    s.start(subj);
    // ns00
    String type = Grouper.NS_TYPE;
    String stem = ns_stem00;
    String extn = ns_extn00; 
    String name = GrouperBackend.groupName(stem, extn);
    String desc = "My New Description: " + name;
    GrouperGroup g = GrouperGroup.load(s, stem, extn, type);
    Assert.assertNotNull(g);
    Assert.assertTrue( g.attribute("description", desc) );
    s.stop();
  }

  public void testAddAttr10() {
    GrouperSession  s     = new GrouperSession();
    Assert.assertNotNull(s);
    Subject         subj  = GrouperSubject.lookup( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    Assert.assertNotNull(subj);
    s.start(subj);
    // ns1
    String type = Grouper.NS_TYPE;
    String stem = ns_stem1;
    String extn = ns_extn1; 
    String name = GrouperBackend.groupName(stem, extn);
    String desc = "My New Description: " + name;
    GrouperGroup g = GrouperGroup.load(s, stem, extn, type);
    Assert.assertNotNull(g);
    Assert.assertTrue( g.attribute("description", desc) );
    s.stop();
  }

  public void testAddAttr11() {
    GrouperSession  s     = new GrouperSession();
    Assert.assertNotNull(s);
    Subject         subj  = GrouperSubject.lookup( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    Assert.assertNotNull(subj);
    s.start(subj);
    // ns2
    String type = Grouper.NS_TYPE;
    String stem = ns_stem2;
    String extn = ns_extn2; 
    String name = GrouperBackend.groupName(stem, extn);
    String desc = "My New Description: " + name;
    GrouperGroup g = GrouperGroup.load(s, stem, extn, type);
    Assert.assertNotNull(g);
    Assert.assertTrue( g.attribute("description", desc) );
    s.stop();
  }

  public void testAddAttr12() {
    GrouperSession  s     = new GrouperSession();
    Assert.assertNotNull(s);
    Subject         subj  = GrouperSubject.lookup( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    Assert.assertNotNull(subj);
    s.start(subj);
    // g0
    String type = Grouper.DEF_GROUP_TYPE;
    String stem = stem0;
    String extn = extn0; 
    String name = GrouperBackend.groupName(stem, extn);
    String desc = "My New Description: " + name;
    GrouperGroup g = GrouperGroup.load(s, stem, extn, type);
    Assert.assertNotNull(g);
    Assert.assertTrue( g.attribute("description", desc) );
    s.stop();
  }

  public void testAddAttr13() {
    GrouperSession  s     = new GrouperSession();
    Assert.assertNotNull(s);
    Subject         subj  = GrouperSubject.lookup( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    Assert.assertNotNull(subj);
    s.start(subj);
    // g1
    String type = Grouper.DEF_GROUP_TYPE;
    String stem = stem1;
    String extn = extn1; 
    String name = GrouperBackend.groupName(stem, extn);
    String desc = "My New Description: " + name;
    GrouperGroup g = GrouperGroup.load(s, stem, extn, type);
    Assert.assertNotNull(g);
    Assert.assertTrue( g.attribute("description", desc) );
    s.stop();
  }

  public void testAddAttr14() {
    GrouperSession  s     = new GrouperSession();
    Assert.assertNotNull(s);
    Subject         subj  = GrouperSubject.lookup( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    Assert.assertNotNull(subj);
    s.start(subj);
    // g2
    String type = Grouper.DEF_GROUP_TYPE;
    String stem = stem2;
    String extn = extn2; 
    String name = GrouperBackend.groupName(stem, extn);
    String desc = "My New Description: " + name;
    GrouperGroup g = GrouperGroup.load(s, stem, extn, type);
    Assert.assertNotNull(g);
    Assert.assertTrue( g.attribute("description", desc) );
    s.stop();
  }

  public void testAddAttr15() {
    GrouperSession  s     = new GrouperSession();
    Assert.assertNotNull(s);
    Subject         subj  = GrouperSubject.lookup( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    Assert.assertNotNull(subj);
    s.start(subj);
    // g3
    String type = Grouper.DEF_GROUP_TYPE;
    String stem = stem3;
    String extn = extn3; 
    String name = GrouperBackend.groupName(stem, extn);
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
    GrouperSession  s     = new GrouperSession();
    Assert.assertNotNull(s);
    Subject         subj  = GrouperSubject.lookup( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    Assert.assertNotNull(subj);
    s.start(subj);
    // ns0
    String type = Grouper.NS_TYPE;
    String stem = ns_stem0;
    String extn = ns_extn0; 
    String name = GrouperBackend.groupName(stem, extn);
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
  }

  public void testFetchAttrs17() {
    GrouperSession  s     = new GrouperSession();
    Assert.assertNotNull(s);
    Subject         subj  = GrouperSubject.lookup( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    Assert.assertNotNull(subj);
    s.start(subj);
    // ns00
    String type = Grouper.NS_TYPE;
    String stem = ns_stem00;
    String extn = ns_extn00; 
    String name = GrouperBackend.groupName(stem, extn);
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
  }

  public void testFetchAttrs18() {
    GrouperSession  s     = new GrouperSession();
    Assert.assertNotNull(s);
    Subject         subj  = GrouperSubject.lookup( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    Assert.assertNotNull(subj);
    s.start(subj);
    // ns1
    String type = Grouper.NS_TYPE;
    String stem = ns_stem1;
    String extn = ns_extn1; 
    String name = GrouperBackend.groupName(stem, extn);
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
  }

  public void testFetchAttrs19() {
    GrouperSession  s     = new GrouperSession();
    Assert.assertNotNull(s);
    Subject         subj  = GrouperSubject.lookup( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    Assert.assertNotNull(subj);
    s.start(subj);
    // ns2
    String type = Grouper.NS_TYPE;
    String stem = ns_stem2;
    String extn = ns_extn2; 
    String name = GrouperBackend.groupName(stem, extn);
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
  }

  public void testFetchAttrs20() {
    GrouperSession  s     = new GrouperSession();
    Assert.assertNotNull(s);
    Subject         subj  = GrouperSubject.lookup( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    Assert.assertNotNull(subj);
    s.start(subj);
    // g0
    String type = Grouper.DEF_GROUP_TYPE;
    String stem = stem0;
    String extn = extn0; 
    String name = GrouperBackend.groupName(stem, extn);
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
  }

  public void testFetchAttrs21() {
    GrouperSession  s     = new GrouperSession();
    Assert.assertNotNull(s);
    Subject         subj  = GrouperSubject.lookup( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    Assert.assertNotNull(subj);
    s.start(subj);
    // g1
    String type = Grouper.DEF_GROUP_TYPE;
    String stem = stem1;
    String extn = extn1; 
    String name = GrouperBackend.groupName(stem, extn);
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
  }

  public void testFetchAttrs22() {
    GrouperSession  s     = new GrouperSession();
    Assert.assertNotNull(s);
    Subject         subj  = GrouperSubject.lookup( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    Assert.assertNotNull(subj);
    s.start(subj);
    // g2 
    String type = Grouper.DEF_GROUP_TYPE;
    String stem = stem2;
    String extn = extn2; 
    String name = GrouperBackend.groupName(stem, extn);
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
  }

  public void testFetchAttrs23() {
    GrouperSession  s     = new GrouperSession();
    Assert.assertNotNull(s);
    Subject         subj  = GrouperSubject.lookup( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    Assert.assertNotNull(subj);
    s.start(subj);
    // g3
    String type = Grouper.DEF_GROUP_TYPE;
    String stem = stem3;
    String extn = extn3; 
    String name = GrouperBackend.groupName(stem, extn);
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
  }

  // 
  // Delete the description of every group
  //

  public void testDelAttr0() {
    GrouperSession  s     = new GrouperSession();
    Assert.assertNotNull(s);
    Subject         subj  = GrouperSubject.lookup( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    Assert.assertNotNull(subj);
    s.start(subj);
    // ns0
    String type = Grouper.NS_TYPE;
    String stem = ns_stem0;
    String extn = ns_extn0; 
    String name = GrouperBackend.groupName(stem, extn);
    GrouperGroup g = GrouperGroup.load(s, stem, extn, type);
    Assert.assertNotNull(g);
    Assert.assertTrue( g.attribute("description", null) );
    s.stop();
  }

  public void testDelAttr1() {
    GrouperSession  s     = new GrouperSession();
    Assert.assertNotNull(s);
    Subject         subj  = GrouperSubject.lookup( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    Assert.assertNotNull(subj);
    s.start(subj);
    // ns00
    String type = Grouper.NS_TYPE;
    String stem = ns_stem00;
    String extn = ns_extn00; 
    String name = GrouperBackend.groupName(stem, extn);
    GrouperGroup g = GrouperGroup.load(s, stem, extn, type);
    Assert.assertNotNull(g);
    Assert.assertTrue( g.attribute("description", null) );
    s.stop();
  }

  public void testDelAttr2() {
    GrouperSession  s     = new GrouperSession();
    Assert.assertNotNull(s);
    Subject         subj  = GrouperSubject.lookup( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    Assert.assertNotNull(subj);
    s.start(subj);
    // ns1
    String type = Grouper.NS_TYPE;
    String stem = ns_stem1;
    String extn = ns_extn1; 
    String name = GrouperBackend.groupName(stem, extn);
    GrouperGroup g = GrouperGroup.load(s, stem, extn, type);
    Assert.assertNotNull(g);
    Assert.assertTrue( g.attribute("description", null) );
    s.stop();
  }

  public void testDelAttr3() {
    GrouperSession  s     = new GrouperSession();
    Assert.assertNotNull(s);
    Subject         subj  = GrouperSubject.lookup( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    Assert.assertNotNull(subj);
    s.start(subj);
    // ns2
    String type = Grouper.NS_TYPE;
    String stem = ns_stem2;
    String extn = ns_extn2; 
    String name = GrouperBackend.groupName(stem, extn);
    GrouperGroup g = GrouperGroup.load(s, stem, extn, type);
    Assert.assertNotNull(g);
    Assert.assertTrue( g.attribute("description", null) );
    s.stop();
  }

  public void testDelAttr4() {
    GrouperSession  s     = new GrouperSession();
    Assert.assertNotNull(s);
    Subject         subj  = GrouperSubject.lookup( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    Assert.assertNotNull(subj);
    s.start(subj);
    // g0
    String type = Grouper.DEF_GROUP_TYPE;
    String stem = stem0;
    String extn = extn0; 
    String name = GrouperBackend.groupName(stem, extn);
    GrouperGroup g = GrouperGroup.load(s, stem, extn, type);
    Assert.assertNotNull(g);
    Assert.assertTrue( g.attribute("description", null) );
    s.stop();
  }

  public void testDelAttr5() {
    GrouperSession  s     = new GrouperSession();
    Assert.assertNotNull(s);
    Subject         subj  = GrouperSubject.lookup( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    Assert.assertNotNull(subj);
    s.start(subj);
    // g1
    String type = Grouper.DEF_GROUP_TYPE;
    String stem = stem1;
    String extn = extn1; 
    String name = GrouperBackend.groupName(stem, extn);
    GrouperGroup g = GrouperGroup.load(s, stem, extn, type);
    Assert.assertNotNull(g);
    Assert.assertTrue( g.attribute("description", null) );
    s.stop();
  }

  public void testDelAttr6() {
    GrouperSession  s     = new GrouperSession();
    Assert.assertNotNull(s);
    Subject         subj  = GrouperSubject.lookup( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    Assert.assertNotNull(subj);
    s.start(subj);
    // g2
    String type = Grouper.DEF_GROUP_TYPE;
    String stem = stem2;
    String extn = extn2; 
    String name = GrouperBackend.groupName(stem, extn);
    GrouperGroup g = GrouperGroup.load(s, stem, extn, type);
    Assert.assertNotNull(g);
    Assert.assertTrue( g.attribute("description", null) );
    s.stop();
  }

  public void testDelAttr7() {
    GrouperSession  s     = new GrouperSession();
    Assert.assertNotNull(s);
    Subject         subj  = GrouperSubject.lookup( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    Assert.assertNotNull(subj);
    s.start(subj);
    // g3
    String type = Grouper.DEF_GROUP_TYPE;
    String stem = stem3;
    String extn = extn3; 
    String name = GrouperBackend.groupName(stem, extn);
    GrouperGroup g = GrouperGroup.load(s, stem, extn, type);
    Assert.assertNotNull(g);
    Assert.assertTrue( g.attribute("description", null) );
    s.stop();
  }


  // 
  // Test deletion of `description' attribute
  //

  public void testFetchAttrs24() {
    GrouperSession  s     = new GrouperSession();
    Assert.assertNotNull(s);
    Subject         subj  = GrouperSubject.lookup( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    Assert.assertNotNull(subj);
    s.start(subj);
    // ns0
    String type = Grouper.NS_TYPE;
    String stem = ns_stem0;
    String extn = ns_extn0; 
    String name = GrouperBackend.groupName(stem, extn);
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
  }

  public void testFetchAttrs25() {
    GrouperSession  s     = new GrouperSession();
    Assert.assertNotNull(s);
    Subject         subj  = GrouperSubject.lookup( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    Assert.assertNotNull(subj);
    s.start(subj);
    // ns00
    String type = Grouper.NS_TYPE;
    String stem = ns_stem00;
    String extn = ns_extn00; 
    String name = GrouperBackend.groupName(stem, extn);
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
  }

  public void testFetchAttrs26() {
    GrouperSession  s     = new GrouperSession();
    Assert.assertNotNull(s);
    Subject         subj  = GrouperSubject.lookup( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    Assert.assertNotNull(subj);
    s.start(subj);
    // ns1
    String type = Grouper.NS_TYPE;
    String stem = ns_stem1;
    String extn = ns_extn1; 
    String name = GrouperBackend.groupName(stem, extn);
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
  }

  public void testFetchAttrs27() {
    GrouperSession  s     = new GrouperSession();
    Assert.assertNotNull(s);
    Subject         subj  = GrouperSubject.lookup( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    Assert.assertNotNull(subj);
    s.start(subj);
    // ns2
    String type = Grouper.NS_TYPE;
    String stem = ns_stem2;
    String extn = ns_extn2; 
    String name = GrouperBackend.groupName(stem, extn);
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
  }

  public void testFetchAttrs28() {
    GrouperSession  s     = new GrouperSession();
    Assert.assertNotNull(s);
    Subject         subj  = GrouperSubject.lookup( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    Assert.assertNotNull(subj);
    s.start(subj);
    // g0
    String type = Grouper.DEF_GROUP_TYPE;
    String stem = stem0;
    String extn = extn0; 
    String name = GrouperBackend.groupName(stem, extn);
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
  }

  public void testFetchAttrs29() {
    GrouperSession  s     = new GrouperSession();
    Assert.assertNotNull(s);
    Subject         subj  = GrouperSubject.lookup( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    Assert.assertNotNull(subj);
    s.start(subj);
    // g1
    String type = Grouper.DEF_GROUP_TYPE;
    String stem = stem1;
    String extn = extn1; 
    String name = GrouperBackend.groupName(stem, extn);
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
  }

  public void testFetchAttrs30() {
    GrouperSession  s     = new GrouperSession();
    Assert.assertNotNull(s);
    Subject         subj  = GrouperSubject.lookup( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    Assert.assertNotNull(subj);
    s.start(subj);
    // g2 
    String type = Grouper.DEF_GROUP_TYPE;
    String stem = stem2;
    String extn = extn2; 
    String name = GrouperBackend.groupName(stem, extn);
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
  }

  public void testFetchAttrs31() {
    GrouperSession  s     = new GrouperSession();
    Assert.assertNotNull(s);
    Subject         subj  = GrouperSubject.lookup( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    Assert.assertNotNull(subj);
    s.start(subj);
    // g3
    String type = Grouper.DEF_GROUP_TYPE;
    String stem = stem3;
    String extn = extn3; 
    String name = GrouperBackend.groupName(stem, extn);
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
  }

  //
  // Test changing the stem and extension attributes
  //

  public void testAddAttr16() {
    GrouperSession  s     = new GrouperSession();
    Assert.assertNotNull(s);
    Subject         subj  = GrouperSubject.lookup( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    Assert.assertNotNull(subj);
    s.start(subj);
    // g0
    String type = Grouper.DEF_GROUP_TYPE;
    String stem = stem0;
    String extn = extn0; 
    String name = GrouperBackend.groupName(stem, extn);
    GrouperGroup g = GrouperGroup.load(s, stem, extn, type);
    Assert.assertNotNull(g);
    Assert.assertFalse( g.attribute("stem", null) );
    s.stop();
  }

  public void testAddAttr17() {
    GrouperSession  s     = new GrouperSession();
    Assert.assertNotNull(s);
    Subject         subj  = GrouperSubject.lookup( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    Assert.assertNotNull(subj);
    s.start(subj);
    // g0
    String type = Grouper.DEF_GROUP_TYPE;
    String stem = stem0;
    String extn = extn0; 
    String name = GrouperBackend.groupName(stem, extn);
    GrouperGroup g = GrouperGroup.load(s, stem, extn, type);
    Assert.assertNotNull(g);
    Assert.assertFalse( g.attribute("stem", extn) );
    s.stop();
  }

  public void testAddAttr18() {
    GrouperSession  s     = new GrouperSession();
    Assert.assertNotNull(s);
    Subject         subj  = GrouperSubject.lookup( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    Assert.assertNotNull(subj);
    s.start(subj);
    // g0
    String type = Grouper.DEF_GROUP_TYPE;
    String stem = stem0;
    String extn = extn0; 
    String name = GrouperBackend.groupName(stem, extn);
    GrouperGroup g = GrouperGroup.load(s, stem, extn, type);
    Assert.assertNotNull(g);
    Assert.assertFalse( g.attribute("extension", null) );
    s.stop();
  }

  public void testAddAttr19() {
    GrouperSession  s     = new GrouperSession();
    Assert.assertNotNull(s);
    Subject         subj  = GrouperSubject.lookup( Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE );
    Assert.assertNotNull(subj);
    s.start(subj);
    // g0
    String type = Grouper.DEF_GROUP_TYPE;
    String stem = stem0;
    String extn = extn0; 
    String name = GrouperBackend.groupName(stem, extn);
    GrouperGroup g = GrouperGroup.load(s, stem, extn, type);
    Assert.assertNotNull(g);
    Assert.assertFalse( g.attribute("extension", stem) );
    s.stop();
  }

}

