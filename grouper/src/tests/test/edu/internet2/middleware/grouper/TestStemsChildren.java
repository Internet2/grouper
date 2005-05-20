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

public class TestStemsChildren extends TestCase {

  public TestStemsChildren(String name) {
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
  

  // NS at root-level
  public void testCreateNS0() {
    Subject subj = SubjectFactory.getSubject(Constants.rootI, Constants.rootT);
    GrouperSession s = GrouperSession.start(subj);

    // Create ns0
    GrouperStem ns0 = GrouperStem.create(
                        s, Constants.ns0s, Constants.ns0e
                      );
    Assert.assertNotNull("ns0 !null", ns0);

    // Check child stems
    Assert.assertTrue("ns0 child stems == 0", ns0.stems().size() == 0);

    s.stop();
  }

  // NS at root-level
  public void testFetchNS0() {
    Subject subj = SubjectFactory.getSubject(Constants.rootI, Constants.rootT);
    GrouperSession s = GrouperSession.start(subj);

    // Create ns0
    GrouperStem ns0 = GrouperStem.create(
                         s, Constants.ns0s, Constants.ns0e
                       );

    // Fetch ns0
    GrouperStem ns = GrouperStem.load(
                        s, Constants.ns0s, Constants.ns0e
                      );
    Assert.assertNotNull("ns0 !null", ns);

    // Check child stems
    Assert.assertTrue("ns0 child stems == 0", ns.stems().size() == 0);
    // Check child groups
    Assert.assertTrue("ns0 child groups == 0", ns.groups().size() == 0);

    s.stop();
  }

  // NS one level deep
  public void testCreateNS1() {
    Subject subj = SubjectFactory.getSubject(Constants.rootI, Constants.rootT);
    GrouperSession s = GrouperSession.start(subj);

    // Create ns0
    GrouperStem ns0 = GrouperStem.create(
                         s, Constants.ns0s, Constants.ns0e
                       );

    // Create ns1 as child of ns0
    GrouperStem ns1 = GrouperStem.create(
                         s, Constants.ns1s, Constants.ns1e
                       );
    Assert.assertNotNull("ns1 !null", ns1);

    // Check child stems
    Assert.assertTrue("ns0 child stems == 1", ns0.stems().size() == 1);
    Assert.assertTrue("ns1 child stems == 0", ns1.stems().size() == 0);
    // Check child groups
    Assert.assertTrue("ns0 child groups == 0", ns0.groups().size() == 0);
    Assert.assertTrue("ns1 child groups == 0", ns1.groups().size() == 0);

    s.stop();
  }

  // NS one level deep
  public void testFetchNS1() {
    Subject subj = SubjectFactory.getSubject(Constants.rootI, Constants.rootT);
    GrouperSession s = GrouperSession.start(subj);

    // Create ns0
    GrouperStem ns0 = GrouperStem.create(
                         s, Constants.ns0s, Constants.ns0e
                       );
    // Create ns1
    GrouperStem ns1 = GrouperStem.create(
                         s, Constants.ns1s, Constants.ns1e
                       );
    // Fetch ns1
    GrouperStem ns = GrouperStem.load(
                        s, Constants.ns1s, Constants.ns1e
                      );
    Assert.assertNotNull("ns1 !null", ns);

    // Check child stems
    Assert.assertTrue("ns0 child stems == 1", ns0.stems().size() == 1);
    Assert.assertTrue("ns1 child stems == 0", ns1.stems().size() == 0);
    Assert.assertTrue("ns  child stems == 0", ns.stems().size() == 0);
    // Check child groups
    Assert.assertTrue("ns0 child groups == 0", ns0.groups().size() == 0);
    Assert.assertTrue("ns1 child groups == 0", ns1.groups().size() == 0);
    Assert.assertTrue("ns  child groups == 0", ns.groups().size() == 0);

    s.stop();
  }

  // NS two levels deep
  public void testCreateNS2() {
    Subject subj = SubjectFactory.getSubject(Constants.rootI, Constants.rootT);
    GrouperSession s = GrouperSession.start(subj);

    // Create ns0
    GrouperStem ns0 = GrouperStem.create(
                         s, Constants.ns0s, Constants.ns0e
                       );
    // Create ns1
    GrouperStem ns1 = GrouperStem.create(
                         s, Constants.ns1s, Constants.ns1e
                       );
    // Create ns2 as child of ns1
    GrouperStem ns2 = GrouperStem.create(
                         s, Constants.ns2s, Constants.ns2e
                       );
    Assert.assertNotNull("ns2 !null", ns2);

    // Check child stems
    Assert.assertTrue("ns0 child stems == 1", ns0.stems().size() == 1);
    Assert.assertTrue("ns1 child stems == 1", ns1.stems().size() == 1);
    Assert.assertTrue("ns2 child stems == 0", ns2.stems().size() == 0);
    // Check child groups
    Assert.assertTrue("ns0 child groups == 0", ns0.groups().size() == 0);
    Assert.assertTrue("ns1 child groups == 0", ns1.groups().size() == 0);
    Assert.assertTrue("ns2 child groups == 0", ns2.groups().size() == 0);

    s.stop();
  }

  // NS two levels deep
  public void testFetchNS2() {
    Subject subj = SubjectFactory.getSubject(Constants.rootI, Constants.rootT);
    GrouperSession s = GrouperSession.start(subj);

    // Create ns0
    GrouperStem ns0 = GrouperStem.create(
                         s, Constants.ns0s, Constants.ns0e
                       );
    // Create ns1
    GrouperStem ns1 = GrouperStem.create(
                         s, Constants.ns1s, Constants.ns1e
                       );
    // Create ns2 as child of ns1
    GrouperStem ns2 = GrouperStem.create(
                         s, Constants.ns2s, Constants.ns2e
                       );
    // Fetch ns2
    GrouperStem ns = GrouperStem.load(
                        s, Constants.ns2s, Constants.ns2e
                      );
    Assert.assertNotNull("ns2 !null", ns);

    // Check child stems
    Assert.assertTrue("ns0 child stems == 1", ns0.stems().size() == 1);
    Assert.assertTrue("ns1 child stems == 1", ns1.stems().size() == 1);
    Assert.assertTrue("ns2 child stems == 0", ns2.stems().size() == 0);
    Assert.assertTrue("ns  child stems == 0", ns.stems().size()  == 0);
    // Check child groups
    Assert.assertTrue("ns0 child groups == 0", ns0.groups().size() == 0);
    Assert.assertTrue("ns1 child groups == 0", ns1.groups().size() == 0);
    Assert.assertTrue("ns2 child groups == 0", ns2.groups().size() == 0);
    Assert.assertTrue("ns  child groups == 0", ns.groups().size()  == 0);

    s.stop();
  }

  // NS two levels deep with groups all around
  public void testCreateNS3AndGroups() {
    Subject subj = SubjectFactory.getSubject(Constants.rootI, Constants.rootT);
    GrouperSession s = GrouperSession.start(subj);

    // Create ns0
    GrouperStem ns0 = GrouperStem.create(
                         s, Constants.ns0s, Constants.ns0e
                       );
    // Create ns1
    GrouperStem ns1 = GrouperStem.create(
                         s, Constants.ns1s, Constants.ns1e
                       );
    // Create ns2 as child of ns1
    GrouperStem ns2 = GrouperStem.create(
                         s, Constants.ns2s, Constants.ns2e
                       );
    // Create groups
    GrouperGroup g0 = GrouperGroup.create(
                        s, Constants.g0s, Constants.g0e
                      );
    GrouperGroup g1 = GrouperGroup.create(
                        s, Constants.g1s, Constants.g1e
                      );
    GrouperGroup g2 = GrouperGroup.create(
                        s, Constants.g2s, Constants.g2e
                      );
    GrouperGroup gA = GrouperGroup.create(
                        s, Constants.gAs, Constants.gAe
                      );
    GrouperGroup gB = GrouperGroup.create(
                        s, Constants.gBs, Constants.gBe
                      );
    GrouperGroup gC = GrouperGroup.create(
                        s, Constants.gCs, Constants.gCe
                      );
    GrouperGroup gD = GrouperGroup.create(
                        s, Constants.gDs, Constants.gDe
                      );

    // Check child stems
    Assert.assertTrue("ns0 child stems == 1", ns0.stems().size() == 1);
    Assert.assertTrue("ns1 child stems == 1", ns1.stems().size() == 1);
    Assert.assertTrue("ns2 child stems == 0", ns2.stems().size() == 0);
    // Check child groups
    Assert.assertTrue("ns0 child groups == 5", ns0.groups().size() == 5);
    Assert.assertTrue("ns1 child groups == 1", ns1.groups().size() == 1);
    Assert.assertTrue("ns2 child groups == 1", ns2.groups().size() == 1);

    s.stop();
  }

}

