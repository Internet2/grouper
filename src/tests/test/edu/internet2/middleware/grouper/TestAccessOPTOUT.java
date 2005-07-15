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


public class TestAccessOPTOUT extends TestCase {

  private GrouperSession  s, nrs0, nrs1;
  private GrouperQuery    q;

  public TestAccessOPTOUT(String name) {
    super(name);
  }

  protected void setUp () {
    DB db = new DB();
    db.emptyTables();
    db.stop();
    s = Constants.createSession();
    Assert.assertNotNull("s", s);
    nrs0 = Constants.createSession(Constants.mem0I, Constants.mem0T);
    Assert.assertNotNull("nrs0", nrs0);
    nrs1 = Constants.createSession(Constants.mem1I, Constants.mem1T);
    Assert.assertNotNull("nrs1", nrs1);
    Constants.createGroups(s);
    q = new GrouperQuery(s);
    Assert.assertNotNull("q", q);
  }

  protected void tearDown () {
    s.stop();
    nrs0.stop();
    nrs1.stop();
  }


  /*
   * TESTS
   */

  public void testOptoutAsRootWithoutOPTOUT() {
    try {
      Constants.g0.listAddVal(Constants.mr);
      Assert.assertTrue("listAddVal", true);
    } catch (RuntimeException e) {
      Assert.fail("!listAddVal");
    }
    try {
      Constants.g0.listDelVal(Constants.mr);
      Assert.assertTrue("optout", true);
    } catch (RuntimeException e) {
      Assert.fail("!optout");
    }
    Assert.assertTrue("g0", Constants.g0.listVals().size() == 0);
    Assert.assertFalse("m", Constants.g0.hasMember(Constants.mr));
  }

  public void testOptoutAsRootWithOPTOUT() {
    try {
      Constants.g0.listAddVal(Constants.mr);
      Assert.assertTrue("listAddVal", true);
    } catch (RuntimeException e) {
      Assert.fail("!listAddVal");
    }
    Constants.grantPriv(
      s, Constants.g0, Constants.g0.toMember(), Grouper.PRIV_OPTOUT
    );
    try {
      Constants.g0.listDelVal(Constants.mr);
      Assert.assertTrue("optout", true);
    } catch (RuntimeException e) {
      Assert.fail("!optout");
    }
    Assert.assertTrue("g0", Constants.g0.listVals().size() == 0);
    Assert.assertFalse("m", Constants.g0.hasMember(Constants.mr));
  }

  public void testOptoutAsNonRootWithoutOPTOUT() {
    Constants.createMembers(s);

    GrouperGroup g = Constants.loadGroup(nrs0, Constants.g0s, Constants.g0e);
    // Should not be able to remove other members
    try {
      g.listDelVal(Constants.mr);
      Assert.fail("optout != update");
    } catch (RuntimeException e) {
      Assert.assertTrue("optout != update", true);
    }
    // Should not be able to remove ourself 
    try {
      g.listDelVal(Constants.m0);
      Assert.fail("!optout");
    } catch (RuntimeException e) {
      Assert.assertTrue("!optout", true);
    }
    Assert.assertTrue("g0", Constants.g0.listVals().size() > 0);
    Assert.assertFalse("mr", Constants.g0.hasMember(Constants.mr));
    Assert.assertTrue("m0", Constants.g0.hasMember(Constants.m0));
  }

  public void testOptoutAsNonRootWithOPTOUT() {
    Constants.createMembers(s);
    Constants.grantPriv(
      s, Constants.g0, Constants.m0, Grouper.PRIV_OPTOUT
    );

    GrouperGroup g = Constants.loadGroup(nrs0, Constants.g0s, Constants.g0e);
    // Should not be able to remove other members
    try {
      g.listDelVal(Constants.mr);
      Assert.fail("optout != update");
    } catch (RuntimeException e) {
      Assert.assertTrue("optout != update", true);
    }
    // Should be able to remove ourself 
    try {
      g.listDelVal(Constants.m0);
      Assert.assertTrue("optout", true);
    } catch (RuntimeException e) {
      Assert.fail("optout");
    }
    Assert.assertTrue("g0", Constants.g0.listVals().size() == 0);
    Assert.assertFalse("mr", Constants.g0.hasMember(Constants.mr));
    Assert.assertFalse("m0", Constants.g0.hasMember(Constants.m0));
  }

}

