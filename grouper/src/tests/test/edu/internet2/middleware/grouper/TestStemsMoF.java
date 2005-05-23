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

public class TestStemsMoF extends TestCase {

  public TestStemsMoF(String name) {
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
  

  // NS at root-level - all list values
  public void testMoFNS0() {
    Subject subj = null;
    try {
      subj = SubjectFactory.getSubject(Constants.rootI, Constants.rootT);
    } catch (SubjectNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    GrouperSession s = GrouperSession.start(subj);

    // Create ns0
    GrouperStem ns0 = GrouperStem.create(
                         s, Constants.ns0s, Constants.ns0e
                       );

    // Inspect lists
    List admins   = ns0.listVals("admins");
    Assert.assertNotNull("admins !null", admins);
    Assert.assertTrue("admins == 0", admins.size() == 0);
    List creators = ns0.listVals("creators");
    Assert.assertNotNull("creators !null", creators);
    Assert.assertTrue("creators == 0", creators.size() == 0);
    List members  = ns0.listVals("members");
    Assert.assertNotNull("members !null", members);
    Assert.assertTrue("members == 0", members.size() == 0);
    List optins   = ns0.listVals("optins");
    Assert.assertNotNull("optins !null", optins);
    Assert.assertTrue("optins == 0", optins.size() == 0);
    List optouts  = ns0.listVals("optouts");
    Assert.assertNotNull("optouts !null", optouts);
    Assert.assertTrue("optouts == 0", optouts.size() == 0);
    List readers  = ns0.listVals("readers");
    Assert.assertNotNull("readers !null", readers);
    Assert.assertTrue("readers == 0", readers.size() == 0);
    List stemmers = ns0.listVals("stemmers");
    Assert.assertNotNull("stemmers !null", stemmers);
    Assert.assertTrue("stemmers == 1", stemmers.size() == 1);
    List updaters = ns0.listVals("updaters");
    Assert.assertNotNull("updaters !null", updaters);
    Assert.assertTrue("updaters == 0", updaters.size() == 0);
    List viewers = ns0.listVals("viewers");
    Assert.assertNotNull("viewers !null", viewers);
    Assert.assertTrue("viewers == 0", viewers.size() == 0);
    
    s.stop();
  }
  // NS at root-level - immediate list vals
  public void testMoFNS0Imm() {
    Subject subj = null;
    try {
      subj = SubjectFactory.getSubject(Constants.rootI, Constants.rootT);
    } catch (SubjectNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    GrouperSession s = GrouperSession.start(subj);

    // Create ns0
    GrouperStem ns0 = GrouperStem.create(
                         s, Constants.ns0s, Constants.ns0e
                       );

    // Inspect lists
    List admins   = ns0.listImmVals("admins");
    Assert.assertNotNull("admins !null", admins);
    Assert.assertTrue("admins == 0", admins.size() == 0);
    List creators = ns0.listImmVals("creators");
    Assert.assertNotNull("creators !null", creators);
    Assert.assertTrue("creators == 0", creators.size() == 0);
    List members  = ns0.listImmVals("members");
    Assert.assertNotNull("members !null", members);
    Assert.assertTrue("members == 0", members.size() == 0);
    List optins   = ns0.listImmVals("optins");
    Assert.assertNotNull("optins !null", optins);
    Assert.assertTrue("optins == 0", optins.size() == 0);
    List optouts  = ns0.listImmVals("optouts");
    Assert.assertNotNull("optouts !null", optouts);
    Assert.assertTrue("optouts == 0", optouts.size() == 0);
    List readers  = ns0.listImmVals("readers");
    Assert.assertNotNull("readers !null", readers);
    Assert.assertTrue("readers == 0", readers.size() == 0);
    List stemmers = ns0.listImmVals("stemmers");
    Assert.assertNotNull("stemmers !null", stemmers);
    Assert.assertTrue("stemmers == 1", stemmers.size() == 1);
    List updaters = ns0.listImmVals("updaters");
    Assert.assertNotNull("updaters !null", updaters);
    Assert.assertTrue("updaters == 0", updaters.size() == 0);
    List viewers = ns0.listImmVals("viewers");
    Assert.assertNotNull("viewers !null", viewers);
    Assert.assertTrue("viewers == 0", viewers.size() == 0);
    
    s.stop();
  }

  // NS at root-level - effective list vals
  public void testMoFNS0Eff() {
    Subject subj = null;
    try {
      subj = SubjectFactory.getSubject(Constants.rootI, Constants.rootT);
    } catch (SubjectNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    GrouperSession s = GrouperSession.start(subj);

    // Create ns0
    GrouperStem ns0 = GrouperStem.create(
                         s, Constants.ns0s, Constants.ns0e
                       );

    // Inspect lists
    List admins   = ns0.listEffVals("admins");
    Assert.assertNotNull("admins !null", admins);
    Assert.assertTrue("admins == 0", admins.size() == 0);
    List creators = ns0.listEffVals("creators");
    Assert.assertNotNull("creators !null", creators);
    Assert.assertTrue("creators == 0", creators.size() == 0);
    List members  = ns0.listEffVals("members");
    Assert.assertNotNull("members !null", members);
    Assert.assertTrue("members == 0", members.size() == 0);
    List optins   = ns0.listEffVals("optins");
    Assert.assertNotNull("optins !null", optins);
    Assert.assertTrue("optins == 0", optins.size() == 0);
    List optouts  = ns0.listEffVals("optouts");
    Assert.assertNotNull("optouts !null", optouts);
    Assert.assertTrue("optouts == 0", optouts.size() == 0);
    List readers  = ns0.listEffVals("readers");
    Assert.assertNotNull("readers !null", readers);
    Assert.assertTrue("readers == 0", readers.size() == 0);
    List stemmers = ns0.listEffVals("stemmers");
    Assert.assertNotNull("stemmers !null", stemmers);
    Assert.assertTrue("stemmers == 0", stemmers.size() == 0);
    List updaters = ns0.listEffVals("updaters");
    Assert.assertNotNull("updaters !null", updaters);
    Assert.assertTrue("updaters == 0", updaters.size() == 0);
    List viewers = ns0.listEffVals("viewers");
    Assert.assertNotNull("viewers !null", viewers);
    Assert.assertTrue("viewers == 0", viewers.size() == 0);
    
    s.stop();
  }

}

