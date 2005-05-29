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

public class TestBug352Naming extends TestCase {

  public TestBug352Naming(String name) {
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
   * STEM required for !root subjects to adjust attributes on stems
   */
  public void testBug352Naming() {
    Subject subj0 = null;
    try {
      subj0 = SubjectFactory.getSubject(Constants.rootI, Constants.rootT);
    } catch (SubjectNotFoundException e1) {
      // TODO Auto-generated catch block
      e1.printStackTrace();
    }
    GrouperSession s0 = GrouperSession.start(subj0);
    Subject subj1 = null;
    try {
      subj1 = SubjectFactory.getSubject(Constants.mem0I, Constants.mem0T);
    } catch (SubjectNotFoundException e2) {
      // TODO Auto-generated catch block
      e2.printStackTrace();
    }
    GrouperSession s1 = GrouperSession.start(subj1);

    // Create ns0
    GrouperStem ns0 = GrouperStem.create(
                         s0, Constants.ns0s, Constants.ns0e
                       );
    // Create ns1
    GrouperStem ns1 = GrouperStem.create(
                         s0, Constants.ns1s, Constants.ns1e
                       );
    // Load ns1 as subj1
    GrouperStem ns11 = GrouperStem.load(
                         s1, Constants.ns1s, Constants.ns1e
                       );

    // Set description as root
    String text0 = "test description";
    try {
      ns1.attribute("description", text0);
      Assert.assertTrue("add description to ns1", true);
    } catch (RuntimeException e) {
      Assert.fail("add description to ns1 failed");
    }
    GrouperAttribute desc0 = ns1.attribute("description");
    Assert.assertNotNull("ns1 description !null", desc0);
    Assert.assertTrue(
      "ns1 description value", desc0.value().equals(text0)
    );

    // Fail to set description as !root
    String text1 = "a longer test description";
    try {
      ns11.attribute("description", text1);
      Assert.fail("add description to ns11 should have failed");
    } catch (RuntimeException e) {
      Assert.assertTrue("add description to ns11 should fail", true);
    }
    GrouperAttribute desc1 = ns11.attribute("description");
    Assert.assertNotNull("ns11 description !null", desc1);
    Assert.assertTrue(
      "ns11 description value", desc1.value().equals("")
    );

    // Grant STEM to !root
    GrouperMember m = Common.loadMember(
                        s0, Constants.mem0I, Constants.mem0T
                      );
    Assert.assertTrue(
      "grant ADMIN to m on ns11",
      s0.naming().grant(s0, ns11, m, Grouper.PRIV_STEM)
    );

    // Set description as !root
    try {
      ns11.attribute("description", text1);
      Assert.assertTrue("add description to ns11", true);
    } catch (RuntimeException e) {
      Assert.fail("add description to ns11 should not have failed");
    }
    GrouperAttribute desc2 = ns11.attribute("description");
    Assert.assertNotNull("ns11 description !null", desc2);
    Assert.assertTrue(
      "ns11 description value", desc2.value().equals(text1)
    );

    s0.stop();
    s1.stop();
  }

}

