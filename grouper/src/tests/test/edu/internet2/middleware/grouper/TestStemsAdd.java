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

public class TestStemsAdd extends TestCase {

  public TestStemsAdd(String name) {
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
  

  // Confirm non-existence of stem
  public void testNS0DoesNotExist() {
    Subject subj = GrouperSubject.load(Constants.rootI, Constants.rootT);
    Assert.assertNotNull("subj !null", subj);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull("session !null", s);

    // Confirm that NS0 doesn't exist
    GrouperStem ns0 = GrouperStem.load(
                        s, Constants.ns0s, Constants.ns0e
                      );
    Assert.assertNull("ns0 null", ns0);

    // We're done
    s.stop();
  }

  // NS at root-level
  public void testCreateNS0() {
    Subject subj = GrouperSubject.load(Constants.rootI, Constants.rootT);
    GrouperSession s = GrouperSession.start(subj);

    // Create ns0
    GrouperStem ns0 = GrouperStem.create(
                        s, Constants.ns0s, Constants.ns0e
                      );
    Assert.assertNotNull("ns0 !null", ns0);
    Assert.assertTrue(
                      "ns0 right class", 
                      Constants.KLASS_GST.equals( ns0.getClass().getName() )
                     );
    String type = ns0.type();
    Assert.assertNotNull("ns0 type !null", type);
    Assert.assertTrue("ns0 type vale", type.equals(Grouper.NS_TYPE));
    String stem = ns0.attribute("stem").value();
    Assert.assertNotNull("ns0 stem !null", stem);
    Assert.assertTrue("ns0 stem val", stem.equals(Constants.ns0s));
    String extn = ns0.attribute("extension").value();
    Assert.assertNotNull("ns0 extn !null", extn);
    Assert.assertTrue("ns0 extn val", extn.equals(Constants.ns0e));

    s.stop();
  }

  // NS at root-level
  public void testFetchNS0() {
    Subject subj = GrouperSubject.load(Constants.rootI, Constants.rootT);
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
    Assert.assertTrue(
                      "ns0 right class", 
                      Constants.KLASS_GST.equals( ns.getClass().getName() )
                     );
    String type = ns.type();
    Assert.assertNotNull("ns type !null", type);
    Assert.assertTrue("ns type vale", type.equals(Grouper.NS_TYPE));
    String stem = ns.attribute("stem").value();
    Assert.assertNotNull("ns stem !null", stem);
    Assert.assertTrue("ns stem val", stem.equals(Constants.ns0s));
    String extn = ns.attribute("extension").value();
    Assert.assertNotNull("ns0 extn !null", extn);
    Assert.assertTrue("ns0 extn val", extn.equals(Constants.ns0e));

    s.stop();
  }

  // Confirm non-existence of stem
  public void testNS1DoesNotExist() {
    Subject subj = GrouperSubject.load(Constants.rootI, Constants.rootT);
    Assert.assertNotNull("subj !null", subj);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull("session !null", s);

    // Confirm that NS1 doesn't exist
    GrouperStem ns1 = GrouperStem.load(
                         s, Constants.ns1s, Constants.ns1e
                       );
    Assert.assertNull("ns1 null", ns1);

    // We're done
    s.stop();
  }

  // NS one level deep
  public void testCreateNS1() {
    Subject subj = GrouperSubject.load(Constants.rootI, Constants.rootT);
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
    Assert.assertTrue(
                      "ns1 right class", 
                      Constants.KLASS_GST.equals( ns1.getClass().getName() )
                     );
    String type = ns1.type();
    Assert.assertNotNull("ns1 type !null", type);
    Assert.assertTrue("ns1 type vale", type.equals(Grouper.NS_TYPE));
    String stem = ns1.attribute("stem").value();
    Assert.assertNotNull("ns1 stem !null", stem);
    Assert.assertTrue("ns1 stem val", stem.equals(Constants.ns1s));
    String extn = ns1.attribute("extension").value();
    Assert.assertNotNull("ns1 extn !null", extn);
    Assert.assertTrue("ns1 extn val", extn.equals(Constants.ns1e));

    s.stop();
  }

  // NS one level deep
  public void testFetchNS1() {
    Subject subj = GrouperSubject.load(Constants.rootI, Constants.rootT);
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
    Assert.assertTrue(
                      "ns1 right class", 
                      Constants.KLASS_GST.equals( ns.getClass().getName() )
                     );
    String type = ns.type();
    Assert.assertNotNull("ns1 type !null", type);
    Assert.assertTrue("ns1 type vale", type.equals(Grouper.NS_TYPE));
    String stem = ns.attribute("stem").value();
    Assert.assertNotNull("ns1 stem !null", stem);
    Assert.assertTrue("ns1 stem val", stem.equals(Constants.ns1s));
    String extn = ns.attribute("extension").value();
    Assert.assertNotNull("ns1 extn !null", extn);
    Assert.assertTrue("ns1 extn val", extn.equals(Constants.ns1e));

    s.stop();
  }

  // Confirm non-existence of stem
  public void testNS2DoesNotExist() {
    Subject subj = GrouperSubject.load(Constants.rootI, Constants.rootT);
    Assert.assertNotNull("subj !null", subj);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull("session !null", s);

    // Confirm that NS2 doesn't exist
    GrouperStem ns2 = GrouperStem.load(
                         s, Constants.ns2s, Constants.ns2e
                       );
    Assert.assertNull("ns2 null", ns2);

    // We're done
    s.stop();
  }

  // NS two levels deep with missing parent stem
  public void testCreateNS2WithMissingParentStem() {
    Subject subj = GrouperSubject.load(Constants.rootI, Constants.rootT);
    GrouperSession s = GrouperSession.start(subj);

    // Create ns0
    GrouperStem ns0 = GrouperStem.create(
                         s, Constants.ns0s, Constants.ns0e
                       );

    // Create ns2 as child of ns1
    try {
      GrouperStem ns2 = GrouperStem.create(
                           s, Constants.ns2s, Constants.ns2e
                         );
      Assert.fail("ns2 null");
    } catch (RuntimeException e) {
      Assert.assertTrue("ns2 null", true);
    }

    s.stop();
  }

  // NS two levels deep
  public void testCreateNS2() {
    Subject subj = GrouperSubject.load(Constants.rootI, Constants.rootT);
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
    Assert.assertTrue(
                      "ns2 right class", 
                      Constants.KLASS_GST.equals( ns2.getClass().getName() )
                     );
    String type = ns2.type();
    Assert.assertNotNull("ns2 type !null", type);
    Assert.assertTrue("ns2 type vale", type.equals(Grouper.NS_TYPE));
    String stem = ns2.attribute("stem").value();
    Assert.assertNotNull("ns2 stem !null", stem);
    Assert.assertTrue("ns2 stem val", stem.equals(Constants.ns2s));
    String extn = ns2.attribute("extension").value();
    Assert.assertNotNull("ns2 extn !null", extn);
    Assert.assertTrue("ns2 extn val", extn.equals(Constants.ns2e));

    s.stop();
  }

  // NS two levels deep
  public void testFetchNS2() {
    Subject subj = GrouperSubject.load(Constants.rootI, Constants.rootT);
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
    Assert.assertTrue(
                      "ns2 right class", 
                      Constants.KLASS_GST.equals( ns.getClass().getName() )
                     );
    String type = ns.type();
    Assert.assertNotNull("ns2 type !null", type);
    Assert.assertTrue("ns2 type vale", type.equals(Grouper.NS_TYPE));
    String stem = ns.attribute("stem").value();
    Assert.assertNotNull("ns2 stem !null", stem);
    Assert.assertTrue("ns2 stem val", stem.equals(Constants.ns2s));
    String extn = ns.attribute("extension").value();
    Assert.assertNotNull("ns2 extn !null", extn);
    Assert.assertTrue("ns2 extn val", extn.equals(Constants.ns2e));

    s.stop();
  }

  // null or blank ns
  public void testCreateNullOrBlankNS() {
    Subject subj = GrouperSubject.load(Constants.rootI, Constants.rootT);
    GrouperSession s = GrouperSession.start(subj);

    // Create ns0
    try {
      GrouperStem ns0 = GrouperStem.create(
                          s, null, null
                        );
      Assert.fail("stem == null, extn == null");
    } catch (RuntimeException e) {
      Assert.assertTrue("caught double null", true);
    }
    try {
      GrouperStem ns0 = GrouperStem.create(
                          s, null, Constants.ns0e
                        );
      Assert.fail("stem == null");
    } catch (RuntimeException e) {
      Assert.assertTrue("caught null stem", true);
    }
    try {
      GrouperStem ns0 = GrouperStem.create(
                          s, Constants.ns0s, null
                        );
      Assert.fail("extn == null");
    } catch (RuntimeException e) {
      Assert.assertTrue("caught null extn", true);
    }
    try {
      GrouperStem ns0 = GrouperStem.create(
                          s, "", ""
                        );
      Assert.fail("stem == blank, extn == blank");
    } catch (RuntimeException e) {
      Assert.assertTrue("caught double blank", true);
    }
    try {
      GrouperStem ns0 = GrouperStem.create(
                          s, "", Constants.ns0e
                        );
      Assert.fail("stem == blank");
    } catch (RuntimeException e) {
      Assert.assertTrue("caught blank stem", true);
    }
    try {
      GrouperStem ns0 = GrouperStem.create(
                          s, Constants.ns0s, ""
                        );
      Assert.fail("extn == blank");
    } catch (RuntimeException e) {
      Assert.assertTrue("caught blank extn", true);
    }

    s.stop();
  }

}

