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
import  junit.framework.*;

public class TestBug352Access extends TestCase {

  public TestBug352Access(String name) {
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
  

  /*
   * ADMIN required for !root subjects to adjust attributes on groups
   */
  public void testBug352Access() {
    Subject subj0 = SubjectFactory.load(Constants.rootI, Constants.rootT);
    GrouperSession s0 = GrouperSession.start(subj0);
    Subject subj1 = SubjectFactory.load(Constants.mem0I, Constants.mem0T);
    GrouperSession s1 = GrouperSession.start(subj1);

    // Create ns0
    GrouperStem ns0 = GrouperStem.create(
                         s0, Constants.ns0s, Constants.ns0e
                       );
    // Create gA
    GrouperGroup gA  = GrouperGroup.create(
                         s0, Constants.gAs, Constants.gAe
                       );
    // Load gA as subj1
    GrouperGroup gAA = GrouperGroup.load(
                         s1, Constants.gAs, Constants.gAe
                       );

    // Set description as root
    String text0 = "test description";
    try {
      gA.attribute("description", text0);
      Assert.assertTrue("add description to gA", true);
    } catch (RuntimeException e) {
      Assert.fail("add description to gA failed");
    }
    GrouperAttribute desc0 = gA.attribute("description");
    Assert.assertNotNull("gA description !null", desc0);
    Assert.assertTrue(
      "gA description value", desc0.value().equals(text0)
    );

    // Fail to set description as !root
    String text1 = "a longer test description";
    try {
      gAA.attribute("description", text1);
      Assert.fail("add description to gAA should have failed");
    } catch (RuntimeException e) {
      Assert.assertTrue("add description to gAA should fail", true);
    }
    GrouperAttribute desc1 = gAA.attribute("description");
    Assert.assertNotNull("gAA description !null", desc1);
    Assert.assertTrue(
      "gAA description value", desc1.value().equals("")
    );

    // Grant ADMIN to !root
    GrouperMember m = GrouperMember.load(
                        s0, Constants.mem0I, Constants.mem0T
                      );
    Assert.assertTrue(
      "grant ADMIN to m on gAA",
      s0.access().grant(s0, gAA, m, Grouper.PRIV_ADMIN)
    );

    // Set description as !root
    try {
      gAA.attribute("description", text1);
      Assert.assertTrue("add description to gAA", true);
    } catch (RuntimeException e) {
      Assert.fail("add description to gAA should not have failed");
    }
    GrouperAttribute desc2 = gAA.attribute("description");
    Assert.assertNotNull("gAA description !null", desc2);
    Assert.assertTrue(
      "gAA description value", desc2.value().equals(text1)
    );

    s0.stop();
    s1.stop();
  }

}

