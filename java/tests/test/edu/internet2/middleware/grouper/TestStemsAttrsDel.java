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

public class TestStemsAttrsDel extends TestCase {

  public TestStemsAttrsDel(String name) {
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
  

  // Group at root-level
  public void testCreateThenAddAndDeleteDesc() {
    Subject subj = null;
    try {
      subj = SubjectFactory.getSubject(Constants.rootI, Constants.rootT);
    } catch (SubjectNotFoundException e1) {
      // TODO Auto-generated catch block
      e1.printStackTrace();
    }
    GrouperSession s = GrouperSession.start(subj);

    // Create ns0
    GrouperStem ns = GrouperStem.create(
                       s, Constants.ns0s, Constants.ns0e
                     );
    
    GrouperAttribute desc = ns.attribute("description");
    Assert.assertNotNull("desc null", desc);
    Assert.assertTrue(
      "desc value", desc.value().equals("")
    );
    Assert.assertNull("modifySource null", ns.modifySource());
    Assert.assertNull("modifySubject null", ns.modifySubject());
    Assert.assertNull("modifyTime null", ns.modifyTime());
    Assert.assertNotNull("name() !null", ns.name());
    Assert.assertTrue(
      "name() value", 
      ns.name().equals(GrouperStem.groupName(Constants.ns0s, Constants.ns0e))
    );

    // Set description
    String text = "test description";
    try {
      ns.attribute("description", text);
      Assert.assertTrue("add description to ns0", true);
    } catch (RuntimeException e) {
      Assert.fail("add description to ns0");
    }

    // Check values
    GrouperAttribute setDesc = ns.attribute("description");
    Assert.assertNotNull("set description !null", setDesc);
    Assert.assertTrue(
      "set description value", setDesc.value().equals(text)
    );
    Assert.assertNull("modifySource null", ns.modifySource());
    Assert.assertNotNull("modifySubject !null", ns.modifySubject());
    Assert.assertNotNull("modifyTime !null", ns.modifyTime());

    // Delete description
    try {
      ns.attribute("description", null);
      Assert.assertTrue("delete description for ns0", true);
    } catch (RuntimeException e) {
      Assert.fail("delete description for ns0");
    }

    // Check values
    desc = ns.attribute("description");
    Assert.assertNotNull("desc !null", desc);
    Assert.assertTrue("desc field",
      desc.field().equals("description")
    );
    Assert.assertNull("modifySource null", ns.modifySource());
    Assert.assertNotNull("modifySubject !null", ns.modifySubject());
    Assert.assertNotNull("modifyTime !null", ns.modifyTime());

    s.stop();
  }

  // Group at root-level
  public void testDeleteThenFetchDescSameSession() {
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
    // Set description
    String text = "test description";
    try {
      ns0.attribute("description", text);
      Assert.assertTrue("add description to ns0", true);
    } catch (RuntimeException e) {
      Assert.fail("add description to ns0");
    }
    // Delete description
    try {
      ns0.attribute("description", null);
      Assert.assertTrue("delete description from ns0", true);
    } catch (RuntimeException e) {
      Assert.fail("delete description from ns0");
    }

    // Load ns0
    GrouperStem ns = GrouperStem.load(
                        s, Constants.ns0s, Constants.ns0e
                      );
    // Check values
    GrouperAttribute desc = ns0.attribute("description");
    Assert.assertNotNull("desc !null", desc);
    Assert.assertTrue("desc field",
      desc.field().equals("description")
    );
    Assert.assertNull("modifySource null", ns.modifySource());
    Assert.assertNotNull("modifySubject !null", ns.modifySubject());
    Assert.assertNotNull("modifyTime !null", ns.modifyTime());

    s.stop();
  }

  // Group at root-level
  public void testDeleteThenFetchDescNewSession() {
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
    // Set description
    String text = "test description";
    try {
      ns0.attribute("description", text);
      Assert.assertTrue("add description to ns0", true);
    } catch (RuntimeException e) {
      Assert.fail("add description to ns0");
    }
    // Delete description
    try {
      ns0.attribute("description", null);
      Assert.assertTrue("delete description from ns0", true);
    } catch (RuntimeException e) {
      Assert.fail("delete description from ns0");
    }
    s.stop();

    s = GrouperSession.start(subj);
    // Load ns0
    GrouperStem ns = GrouperStem.load(
                       s, Constants.ns0s, Constants.ns0e
                     );
    // Check values
    GrouperAttribute desc = ns.attribute("description");
    Assert.assertNotNull("desc !null", desc);
    Assert.assertTrue("desc field",
      desc.field().equals("description")
    );
    Assert.assertNull("modifySource null", ns.modifySource());
    Assert.assertNotNull("modifySubject !null", ns.modifySubject());
    Assert.assertNotNull("modifyTime !null", ns.modifyTime());

    s.stop();
  }

}

