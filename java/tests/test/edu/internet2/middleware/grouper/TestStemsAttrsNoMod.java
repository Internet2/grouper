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

public class TestStemsAttrsNoMod extends TestCase {

  public TestStemsAttrsNoMod(String name) {
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
  

  // Try to add uneditable attributes
  public void testAddAttrs() {
    Subject subj = GrouperSubject.load(Constants.rootI, Constants.rootT);
    GrouperSession s = GrouperSession.start(subj);

    // Create ns0
    GrouperStem ns = GrouperStem.create(
                       s, Constants.ns0s, Constants.ns0e
                     );

    try {
      ns.attribute("displayname", "new displayname");
      Assert.fail("add displayname");
    } catch (RuntimeException e) {
      Assert.assertTrue("add displayname", true);
    }
    try {
      ns.attribute("extension", "new extension");
      Assert.fail("add extension");
    } catch (RuntimeException e) {
      Assert.assertTrue("add extension", true);
    }
    try {
      ns.attribute("name", "new name");
      Assert.fail("add name");
    } catch (RuntimeException e) {
      Assert.assertTrue("add name", true);
    }
    try {
      ns.attribute("stem", "new stem");
      Assert.fail("add stem");
    } catch (RuntimeException e) {
      Assert.assertTrue("add stem", true);
    }

    s.stop();
  }

  // Try to delete uneditable attributes
  public void testDelAttrs() {
    Subject subj = GrouperSubject.load(Constants.rootI, Constants.rootT);
    GrouperSession s = GrouperSession.start(subj);

    // Create ns0
    GrouperStem ns = GrouperStem.create(
                       s, Constants.ns0s, Constants.ns0e
                     );

    try {
      ns.attribute("displayname", null);
      Assert.fail("delete displayname");
    } catch (RuntimeException e) {
      Assert.assertTrue("delete displayname", true);
    }
    try {
      ns.attribute("extension", null);
      Assert.fail("delete extension");
    } catch (RuntimeException e) {
      Assert.assertTrue("delete extension", true);
    }
    try {
      ns.attribute("name", null);
      Assert.fail("delete name");
    } catch (RuntimeException e) {
      Assert.assertTrue("delete name", true);
    }
    try {
      ns.attribute("stem", null);
      Assert.fail("delete stem");
    } catch (RuntimeException e) {
      Assert.assertTrue("delete stem", true);
    }

    s.stop();
  }

}

