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

  private String stem0  = "stem.0";
  private String stem1  = "stem.1";
  private String stem2  = "stem.2";
  private String stem3  = "stem.3";
  private String stem4  = "stem.4";
  private String extn0  = "extn.0";
  private String extn1  = "extn.1";
  private String extn2  = "extn.2";
  private String extn3  = "extn.3";
  private String extn4  = "extn.4";
  
  private String klass  = "edu.internet2.middleware.grouper.GrouperGroup";

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
    Subject         subj  = GrouperSubject.lookup( Grouper.config("member.system"), "person" );
    s.start(subj);

    // Confirm that groups don't exist
    GrouperGroup    g0  = GrouperGroup.load(s, stem0, extn0);
    Assert.assertFalse( g0.exists() );
    GrouperGroup    g1  = GrouperGroup.load(s, stem1, extn1);
    Assert.assertFalse( g1.exists() );
    GrouperGroup    g2  = GrouperGroup.load(s, stem2, extn2);
    Assert.assertFalse( g2.exists() );
    GrouperGroup    g3  = GrouperGroup.load(s, stem3, extn3);
    Assert.assertFalse( g3.exists() );
    GrouperGroup    g4  = GrouperGroup.load(s, stem4, extn4);
    Assert.assertFalse( g4.exists() );

    // We're done
    s.stop();
  }

  // Create groups
  public void testCreateGroups() {
    GrouperSession  s     = new GrouperSession();
    Subject         subj  = GrouperSubject.lookup( Grouper.config("member.system"), "person" );
    s.start(subj);

    // Create the groups
    // g0
    GrouperGroup    g0  = GrouperGroup.create(s, stem0, extn0);
    Assert.assertNotNull(g0);
    Assert.assertTrue( klass.equals( g0.getClass().getName() ) );
    Assert.assertTrue( g0.exists() );
    Assert.assertNotNull( g0.type() );
    Assert.assertNotNull( g0.attribute("stem") );
    Assert.assertTrue( g0.attribute("stem").value().equals(stem0) );
    Assert.assertNotNull( g0.attribute("extension") );
    Assert.assertTrue( g0.attribute("extension").value().equals(extn0) );
    // g1
    GrouperGroup    g1  = GrouperGroup.create(s, stem1, extn1);
    Assert.assertNotNull(g1);
    Assert.assertTrue( klass.equals( g1.getClass().getName() ) );
    Assert.assertTrue( g1.exists() );
    Assert.assertNotNull( g1.type() );
    Assert.assertNotNull( g1.attribute("stem") );
    Assert.assertTrue( g1.attribute("stem").value().equals(stem1) );
    Assert.assertNotNull( g1.attribute("extension") );
    Assert.assertTrue( g1.attribute("extension").value().equals(extn1) );
    // g2
    GrouperGroup    g2  = GrouperGroup.create(s, stem2, extn2);
    Assert.assertNotNull(g2);
    Assert.assertTrue( klass.equals( g2.getClass().getName() ) );
    Assert.assertTrue( g2.exists() );
    Assert.assertNotNull( g2.type() );
    Assert.assertNotNull( g2.attribute("stem") );
    Assert.assertTrue( g2.attribute("stem").value().equals(stem2) );
    Assert.assertNotNull( g2.attribute("extension") );
    Assert.assertTrue( g2.attribute("extension").value().equals(extn2) );
    // g3
    GrouperGroup    g3  = GrouperGroup.create(s, stem3, extn3);
    Assert.assertNotNull(g3);
    Assert.assertTrue( klass.equals( g3.getClass().getName() ) );
    Assert.assertTrue( g3.exists() );
    Assert.assertNotNull( g3.type() );
    Assert.assertNotNull( g3.attribute("stem") );
    Assert.assertTrue( g3.attribute("stem").value().equals(stem3) );
    Assert.assertNotNull( g3.attribute("extension") );
    Assert.assertTrue( g3.attribute("extension").value().equals(extn3) );
    // g4
    GrouperGroup    g4  = GrouperGroup.create(s, stem4, extn4);
    Assert.assertNotNull(g4);
    Assert.assertTrue( klass.equals( g4.getClass().getName() ) );
    Assert.assertTrue( g4.exists() );
    Assert.assertNotNull( g4.type() );
    Assert.assertNotNull( g4.attribute("stem") );
    Assert.assertTrue( g4.attribute("stem").value().equals(stem4) );
    Assert.assertNotNull( g4.attribute("extension") );
    Assert.assertTrue( g4.attribute("extension").value().equals(extn4) );

    // We're done
    s.stop();
  }

  // Fetch valid groups
  public void testFetchGroups0() {
    GrouperSession  s     = new GrouperSession();
    Subject         subj  = GrouperSubject.lookup( Grouper.config("member.system"), "person" );
    s.start(subj);

    // Fetch the groups
    // g0
    GrouperGroup    g0  = GrouperGroup.load(s, stem0, extn0);
    Assert.assertNotNull(g0);
    Assert.assertTrue( klass.equals( g0.getClass().getName() ) );
    Assert.assertTrue( g0.exists() );
    Assert.assertNotNull( g0.id() );
    Assert.assertNotNull( g0.type() );
    Assert.assertNotNull( g0.attribute("stem") );
    Assert.assertTrue( g0.attribute("stem").value().equals(stem0) );
    Assert.assertNotNull( g0.attribute("extension") );
    Assert.assertTrue( g0.attribute("extension").value().equals(extn0) );
    // FIXME Either replace or test everywhere
    Assert.assertNotNull( g0.opattr("createSubject") );
    Assert.assertNotNull( g0.opattr("createTime") );
    // g1
    GrouperGroup    g1  = GrouperGroup.load(s, stem1, extn1);
    Assert.assertNotNull(g1);
    Assert.assertTrue( klass.equals( g1.getClass().getName() ) );
    Assert.assertTrue( g1.exists() );
    Assert.assertNotNull( g1.id() );
    Assert.assertNotNull( g1.type() );
    Assert.assertNotNull( g1.attribute("stem") );
    Assert.assertTrue( g1.attribute("stem").value().equals(stem1) );
    Assert.assertNotNull( g1.attribute("extension") );
    Assert.assertTrue( g1.attribute("extension").value().equals(extn1) );
    // g2
    GrouperGroup    g2  = GrouperGroup.load(s, stem2, extn2);
    Assert.assertNotNull(g2);
    Assert.assertTrue( klass.equals( g2.getClass().getName() ) );
    Assert.assertTrue( g2.exists() );
    Assert.assertNotNull( g2.id() );
    Assert.assertNotNull( g2.type() );
    Assert.assertNotNull( g2.attribute("stem") );
    Assert.assertTrue( g2.attribute("stem").value().equals(stem2) );
    Assert.assertNotNull( g2.attribute("extension") );
    Assert.assertTrue( g2.attribute("extension").value().equals(extn2) );
    // g3
    GrouperGroup    g3  = GrouperGroup.load(s, stem3, extn3);
    Assert.assertNotNull(g3);
    Assert.assertTrue( klass.equals( g3.getClass().getName() ) );
    Assert.assertTrue( g3.exists() );
    Assert.assertNotNull( g3.id() );
    Assert.assertNotNull( g3.type() );
    Assert.assertNotNull( g3.attribute("stem") );
    Assert.assertTrue( g3.attribute("stem").value().equals(stem3) );
    Assert.assertNotNull( g3.attribute("extension") );
    Assert.assertTrue( g3.attribute("extension").value().equals(extn3) );
    // g4
    GrouperGroup    g4  = GrouperGroup.load(s, stem4, extn4);
    Assert.assertNotNull(g4);
    Assert.assertTrue( klass.equals( g4.getClass().getName() ) );
    Assert.assertTrue( g4.exists() );
    Assert.assertNotNull( g4.id() );
    Assert.assertNotNull( g4.type() );
    Assert.assertNotNull( g4.attribute("stem") );
    Assert.assertTrue( g4.attribute("stem").value().equals(stem4) );
    Assert.assertNotNull( g4.attribute("extension") );
    Assert.assertTrue( g4.attribute("extension").value().equals(extn4) );

    // We're done
    s.stop();
  }

  // Delete a group
  public void testDeleteGroups0() {
    GrouperSession  s     = new GrouperSession();
    Subject         subj  = GrouperSubject.lookup( Grouper.config("member.system"), "person" );
    s.start(subj);

    // Delete g4
    GrouperGroup  g4 = GrouperGroup.load(s, stem4, extn4);
    Assert.assertNotNull(g4);
    Assert.assertTrue( g4.exists() );
    Assert.assertTrue( GrouperGroup.delete(s, g4) );

    // We're done
    s.stop();
  }

  // Fetch valid groups
  public void testFetchGroups1() {
    GrouperSession  s     = new GrouperSession();
    Subject         subj  = GrouperSubject.lookup( Grouper.config("member.system"), "person" );
    s.start(subj);

    // Fetch the groups
    // g0
    GrouperGroup    g0  = GrouperGroup.load(s, stem0, extn0);
    Assert.assertNotNull(g0);
    Assert.assertTrue( klass.equals( g0.getClass().getName() ) );
    Assert.assertTrue( g0.exists() );
    Assert.assertNotNull( g0.id() );
    Assert.assertNotNull( g0.type() );
    Assert.assertNotNull( g0.attribute("stem") );
    Assert.assertTrue( g0.attribute("stem").value().equals(stem0) );
    Assert.assertNotNull( g0.attribute("extension") );
    Assert.assertTrue( g0.attribute("extension").value().equals(extn0) );
    // g1
    GrouperGroup    g1  = GrouperGroup.load(s, stem1, extn1);
    Assert.assertNotNull(g1);
    Assert.assertTrue( klass.equals( g1.getClass().getName() ) );
    Assert.assertTrue( g1.exists() );
    Assert.assertNotNull( g1.id() );
    Assert.assertNotNull( g1.type() );
    Assert.assertNotNull( g1.attribute("stem") );
    Assert.assertTrue( g1.attribute("stem").value().equals(stem1) );
    Assert.assertNotNull( g1.attribute("extension") );
    Assert.assertTrue( g1.attribute("extension").value().equals(extn1) );
    // g2
    GrouperGroup    g2  = GrouperGroup.load(s, stem2, extn2);
    Assert.assertNotNull(g2);
    Assert.assertTrue( klass.equals( g2.getClass().getName() ) );
    Assert.assertTrue( g2.exists() );
    Assert.assertNotNull( g2.id() );
    Assert.assertNotNull( g2.type() );
    Assert.assertNotNull( g2.attribute("stem") );
    Assert.assertTrue( g2.attribute("stem").value().equals(stem2) );
    Assert.assertNotNull( g2.attribute("extension") );
    Assert.assertTrue( g2.attribute("extension").value().equals(extn2) );
    // g3
    GrouperGroup    g3  = GrouperGroup.load(s, stem3, extn3);
    Assert.assertNotNull(g3);
    Assert.assertTrue( klass.equals( g3.getClass().getName() ) );
    Assert.assertTrue( g3.exists() );
    Assert.assertNotNull( g3.id() );
    Assert.assertNotNull( g3.type() );
    Assert.assertNotNull( g3.attribute("stem") );
    Assert.assertTrue( g3.attribute("stem").value().equals(stem3) );
    Assert.assertNotNull( g3.attribute("extension") );
    Assert.assertTrue( g3.attribute("extension").value().equals(extn3) );
    // g4
    GrouperGroup    g4  = GrouperGroup.load(s, stem4, extn4);
    Assert.assertNotNull(g4);
    Assert.assertFalse( g4.exists() );

    // We're done
    s.stop();
  }

  // TODO Assert ADMIN priv (create + fetch)
  // TODO Delete group

}

