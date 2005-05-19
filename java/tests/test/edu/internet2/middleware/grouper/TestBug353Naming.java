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

public class TestBug353Naming extends TestCase {

  public TestBug353Naming(String name) {
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
   * Test that !root subjects can create stems provided they have STEM
   * on the parent stem
   */
  public void testBug353Naming() {
    // Create ns0
    Subject subj0 = GrouperSubject.load(Constants.rootI, Constants.rootT);
    GrouperSession s0 = GrouperSession.start(subj0);
    GrouperStem ns0  = GrouperStem.create(
                         s0, Constants.ns0s, Constants.ns0e
                       );
    GrouperMember m0 = GrouperMember.load(
                         s0, Constants.mem0I, Constants.mem0T
                       );
    s0.naming().grant(s0, ns0, m0, Grouper.PRIV_STEM);
    s0.stop();

    
    // Create ns1 as child of ns0
    Subject subj1 = GrouperSubject.load(Constants.mem0I, Constants.mem0T);
    GrouperSession s1 = GrouperSession.start(subj1);
    GrouperStem ns1 = GrouperStem.create(
                         s1, Constants.ns1s, Constants.ns1e
                       );

    // the basics
    Assert.assertNotNull("ns1 !null", ns1);
    Assert.assertTrue(
                      "ns1 right class", 
                      Constants.KLASS_GST.equals( ns1.getClass().getName() )
                     );

    // attr and schema
    String type = ns1.type();
    Assert.assertNotNull("ns1 type !null", type);
    Assert.assertTrue("ns1 type vale", type.equals(Grouper.NS_TYPE));
    String stem = ns1.attribute("stem").value();
    Assert.assertNotNull("ns1 stem !null", stem);
    Assert.assertTrue("ns1 stem val", stem.equals(Constants.ns1s));
    String extn = ns1.attribute("extension").value();
    Assert.assertNotNull("ns1 extn !null", extn);
    Assert.assertTrue("ns1 extn val", extn.equals(Constants.ns1e));
    // privs
    Assert.assertTrue(
      "m0 has STEM on ns1",
      s1.naming().has(s1, ns1, Grouper.PRIV_STEM)
    );

    s1.stop();
  }

}

