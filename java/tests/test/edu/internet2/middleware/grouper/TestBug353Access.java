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

public class TestBug353Access extends TestCase {

  public TestBug353Access(String name) {
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
   * Test that !root subjects can create groups provided they have
   * CREATE on the parent stem
   */
  public void testBug353Access() {
    // Create ns0
    Subject subj0 = SubjectFactory.getSubject(Constants.rootI, Constants.rootT);
    GrouperSession s0 = GrouperSession.start(subj0);
    GrouperStem ns0  = GrouperStem.create(
                         s0, Constants.ns0s, Constants.ns0e
                       );
    GrouperMember m0 = GrouperMember.load(
                         s0, Constants.mem0I, Constants.mem0T
                       );
    s0.naming().grant(s0, ns0, m0, Grouper.PRIV_CREATE);
    s0.stop();

    
    //Create g0
    Subject subj1 = SubjectFactory.getSubject(Constants.mem0I, Constants.mem0T);
    GrouperSession s1 = GrouperSession.start(subj1);
    GrouperGroup g0 = GrouperGroup.create(
                        s1, Constants.g0s, Constants.g0e
                      );

    // the basics  
    Assert.assertNotNull("g0 !null", g0);
    Assert.assertTrue(
                      "g0 right class", 
                      Constants.KLASS_GG.equals( g0.getClass().getName() )
                     );
    String type = g0.type();

    // attrs and schema
    Assert.assertNotNull("g0 type !null", type);
    Assert.assertTrue("g0 type val", type.equals(Grouper.DEF_GROUP_TYPE));
    String stem = g0.attribute("stem").value();
    Assert.assertNotNull("g0 stem !null", stem);
    Assert.assertTrue("g0 stem val", stem.equals(Constants.g0s));
    String extn = g0.attribute("extension").value();
    Assert.assertNotNull("g0 extn !null", extn);
    Assert.assertTrue("g0 extn val", extn.equals(Constants.g0e));

    // privs
    Assert.assertTrue(
      "m0 has ADMIN on g0",
      s1.access().has(s1, g0, Grouper.PRIV_ADMIN)
    );

    s1.stop();
  }

}

