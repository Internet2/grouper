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


public class TestAccessVIEW extends TestCase {

  private GrouperSession  s, nrs0, nrs1;
  private GrouperQuery    q;

  public TestAccessVIEW(String name) {
    super(name);
  }

  protected void setUp () {
    DB db = new DB();
    db.emptyTables();
    db.stop();
    s = Constants.createSession();
    Assert.assertNotNull("s !null", s);
    nrs0 = Constants.createSession(Constants.mem0I, Constants.mem0T);
    Assert.assertNotNull("nrs0 !null", nrs0);
    nrs1 = Constants.createSession(Constants.mem1I, Constants.mem1T);
    Assert.assertNotNull("nrs1 !null", nrs1);
    Constants.createGroups(s);
    Constants.createMembers(s);
    q = new GrouperQuery(s);
    Assert.assertNotNull("q !null", q);
  }

  protected void tearDown () {
    s.stop();
    nrs0.stop();
    nrs1.stop();
  }


  /*
   * TESTS
   */

  public void testViewAsRoot() {
    Assert.assertNotNull("g0 !null", Constants.g0);
    Assert.assertNotNull("g1 !null", Constants.g1);
    Assert.assertNotNull("g2 !null", Constants.g2);
    Assert.assertNotNull("gA !null", Constants.gA);
    Assert.assertNotNull("gB !null", Constants.gB);
    Assert.assertNotNull("gC !null", Constants.gC);
    Assert.assertNotNull("gD !null", Constants.gD);
  }

  public void testViewAsNonRootWithNoExplicitViewers() {
    GrouperGroup g = Constants.loadGroup(nrs0, Constants.g0s, Constants.g0e);
    Assert.assertNotNull("g0 !null", g);

    g = Constants.loadGroup(nrs0, Constants.g1s, Constants.g1e);
    Assert.assertNotNull("g1 !null", g);

    g = Constants.loadGroup(nrs0, Constants.g2s, Constants.g2e);
    Assert.assertNotNull("g2 !null", g);

    g = Constants.loadGroup(nrs0, Constants.gAs, Constants.gAe);
    Assert.assertNotNull("gA !null", g);

    g = Constants.loadGroup(nrs0, Constants.gBs, Constants.gBe);
    Assert.assertNotNull("gB !null", g);

    g = Constants.loadGroup(nrs0, Constants.gCs, Constants.gCe);
    Assert.assertNotNull("gC !null", g);

    g = Constants.loadGroup(nrs0, Constants.gDs, Constants.gDe);
    Assert.assertNotNull("gD !null", g);
  }

  public void testViewAsNonRootWithExplicitViewersAndNoGrantedPriv() {
    Constants.grantPriv(Constants.g0s, Constants.g0e, Constants.m0, Grouper.PRIV_VIEW);
    GrouperGroup g = Constants.loadGroup(nrs1, Constants.g0s, Constants.g0e);
    Assert.assertNull("g0 null", g);

    Constants.grantPriv(Constants.g1s, Constants.g1e, Constants.m0, Grouper.PRIV_VIEW);
    g = Constants.loadGroup(nrs1, Constants.g1s, Constants.g1e);
    Assert.assertNull("g1 null", g);

    Constants.grantPriv(Constants.g2s, Constants.g2e, Constants.m0, Grouper.PRIV_VIEW);
    g = Constants.loadGroup(nrs1, Constants.g2s, Constants.g2e);
    Assert.assertNull("g2 null", g);

    Constants.grantPriv(Constants.gAs, Constants.gAe, Constants.m0, Grouper.PRIV_VIEW);
    g = Constants.loadGroup(nrs1, Constants.gAs, Constants.gAe);
    Assert.assertNull("gA null", g);

    Constants.grantPriv(Constants.gBs, Constants.gBe, Constants.m0, Grouper.PRIV_VIEW);
    g = Constants.loadGroup(nrs1, Constants.gBs, Constants.gBe);
    Assert.assertNull("gB null", g);

    Constants.grantPriv(Constants.gCs, Constants.gCe, Constants.m0, Grouper.PRIV_VIEW);
    g = Constants.loadGroup(nrs1, Constants.gCs, Constants.gCe);
    Assert.assertNull("gC null", g);

    Constants.grantPriv(Constants.gDs, Constants.gDe, Constants.m0, Grouper.PRIV_VIEW);
    g = Constants.loadGroup(nrs1, Constants.gDs, Constants.gDe);
    Assert.assertNull("gD null", g);
  }

  public void testViewAsNonRootWithExplicitViewersAndNoGrantedPrivButRoot() {
    Constants.grantPriv(Constants.g0s, Constants.g0e, Constants.m0, Grouper.PRIV_VIEW);
    GrouperGroup g = Constants.loadGroup(s, Constants.g0s, Constants.g0e);
    Assert.assertNotNull("g0 !null", g);

    Constants.grantPriv(Constants.g1s, Constants.g1e, Constants.m0, Grouper.PRIV_VIEW);
    g = Constants.loadGroup(s, Constants.g1s, Constants.g1e);
    Assert.assertNotNull("g1 !null", g);

    Constants.grantPriv(Constants.g2s, Constants.g2e, Constants.m0, Grouper.PRIV_VIEW);
    g = Constants.loadGroup(s, Constants.g2s, Constants.g2e);
    Assert.assertNotNull("g2 !null", g);

    Constants.grantPriv(Constants.gAs, Constants.gAe, Constants.m0, Grouper.PRIV_VIEW);
    g = Constants.loadGroup(s, Constants.gAs, Constants.gAe);
    Assert.assertNotNull("gA !null", g);

    Constants.grantPriv(Constants.gBs, Constants.gBe, Constants.m0, Grouper.PRIV_VIEW);
    g = Constants.loadGroup(s, Constants.gBs, Constants.gBe);
    Assert.assertNotNull("gB !null", g);

    Constants.grantPriv(Constants.gCs, Constants.gCe, Constants.m0, Grouper.PRIV_VIEW);
    g = Constants.loadGroup(s, Constants.gCs, Constants.gCe);
    Assert.assertNotNull("gC !null", g);

    Constants.grantPriv(Constants.gDs, Constants.gDe, Constants.m0, Grouper.PRIV_VIEW);
    g = Constants.loadGroup(s, Constants.gDs, Constants.gDe);
    Assert.assertNotNull("gD !null", g);
  }

  public void testViewAsNonRootWithExplicitViewersAndGrantedPriv() {
    Constants.grantPriv(Constants.g0s, Constants.g0e, Constants.m0, Grouper.PRIV_VIEW);
    GrouperGroup g = Constants.loadGroup(nrs0, Constants.g0s, Constants.g0e);
    Assert.assertNotNull("g0 !null", g);

    Constants.grantPriv(Constants.g1s, Constants.g1e, Constants.m0, Grouper.PRIV_VIEW);
    g = Constants.loadGroup(nrs0, Constants.g1s, Constants.g1e);
    Assert.assertNotNull("g1 !null", g);

    Constants.grantPriv(Constants.g2s, Constants.g2e, Constants.m0, Grouper.PRIV_VIEW);
    g = Constants.loadGroup(nrs0, Constants.g2s, Constants.g2e);
    Assert.assertNotNull("g2 !null", g);

    Constants.grantPriv(Constants.gAs, Constants.gAe, Constants.m0, Grouper.PRIV_VIEW);
    g = Constants.loadGroup(nrs0, Constants.gAs, Constants.gAe);
    Assert.assertNotNull("gA !null", g);

    Constants.grantPriv(Constants.gBs, Constants.gBe, Constants.m0, Grouper.PRIV_VIEW);
    g = Constants.loadGroup(nrs0, Constants.gBs, Constants.gBe);
    Assert.assertNotNull("gB !null", g);

    Constants.grantPriv(Constants.gCs, Constants.gCe, Constants.m0, Grouper.PRIV_VIEW);
    g = Constants.loadGroup(nrs0, Constants.gCs, Constants.gCe);
    Assert.assertNotNull("gC !null", g);

    Constants.grantPriv(Constants.gDs, Constants.gDe, Constants.m0, Grouper.PRIV_VIEW);
    g = Constants.loadGroup(nrs0, Constants.gDs, Constants.gDe);
    Assert.assertNotNull("gD !null", g);
  }

}
