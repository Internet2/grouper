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

public class TestGroupsAttrsAdd extends TestCase {

  public TestGroupsAttrsAdd(String name) {
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
  public void testCreateAttrsG0() {
    Subject subj = GrouperSubject.load(Constants.rootI, Constants.rootT);
    GrouperSession s = GrouperSession.start(subj);

    // Create ns0
    GrouperStem ns0 = GrouperStem.create(
                         s, Constants.ns0s, Constants.ns0e
                       );
    // Create g0
    GrouperGroup g   = GrouperGroup.create(
                         s, Constants.g0s, Constants.g0e
                       );
    
    GrouperAttribute stem = g.attribute("stem");
    Assert.assertNotNull("stem !null", stem);
    Assert.assertTrue(
                      "stem class", 
                      Constants.KLASS_GA.equals(stem.getClass().getName()) 
                     );
    Assert.assertTrue("stem value", stem.value().equals(Constants.g0s));
    GrouperAttribute extn = g.attribute("extension");
    Assert.assertNotNull("extn !null", extn);
    Assert.assertTrue(
                      "extn class", 
                      Constants.KLASS_GA.equals(extn.getClass().getName()) 
                     );
    Assert.assertTrue("extn value", extn.value().equals(Constants.g0e));
    GrouperAttribute name = g.attribute("name");
    Assert.assertNotNull("name !null", name);
    Assert.assertTrue(
                      "name class", 
                      Constants.KLASS_GA.equals(name.getClass().getName()) 
                     );
    Assert.assertTrue(
                      "name value", 
                      name.value().equals( 
                        GrouperGroup.groupName(
                          Constants.g0s, Constants.g0e
                        )
                      )
                     );
    GrouperAttribute desc = g.attribute("description");
    Assert.assertNull("description null", desc);
    Assert.assertNull("createSource null", g.createSource());
    Assert.assertNotNull("createSubject null", g.createSubject());
    Assert.assertNotNull("createTime null", g.createTime());
    Assert.assertNull("modifySource null", g.modifySource());
    Assert.assertNull("modifySubject null", g.modifySubject());
    Assert.assertNull("modifyTime null", g.modifyTime());
    Assert.assertNotNull("name() !null", g.name());
    Assert.assertTrue(
      "name() value", 
      g.name().equals(GrouperGroup.groupName(Constants.g0s, Constants.g0e))
    );

    s.stop();
  }

  // Group at root-level
  public void testAddAttrsG0() {
    Subject subj = GrouperSubject.load(Constants.rootI, Constants.rootT);
    GrouperSession s = GrouperSession.start(subj);

    // Create ns0
    GrouperStem ns0 = GrouperStem.create(
                         s, Constants.ns0s, Constants.ns0e
                       );
    // Create g0
    GrouperGroup g   = GrouperGroup.create(
                         s, Constants.g0s, Constants.g0e
                       );
    
    GrouperAttribute stem = g.attribute("stem");
    Assert.assertNotNull("stem !null", stem);
    Assert.assertTrue(
                      "stem class", 
                      Constants.KLASS_GA.equals(stem.getClass().getName()) 
                     );
    Assert.assertTrue("stem value", stem.value().equals(Constants.g0s));
    GrouperAttribute extn = g.attribute("extension");
    Assert.assertNotNull("extn !null", extn);
    Assert.assertTrue(
                      "extn class", 
                      Constants.KLASS_GA.equals(extn.getClass().getName()) 
                     );
    Assert.assertTrue("extn value", extn.value().equals(Constants.g0e));
    GrouperAttribute name = g.attribute("name");
    Assert.assertNotNull("name !null", name);
    Assert.assertTrue(
                      "name class", 
                      Constants.KLASS_GA.equals(name.getClass().getName()) 
                     );
    Assert.assertTrue(
                      "name value", 
                      name.value().equals( 
                        GrouperGroup.groupName(
                          Constants.g0s, Constants.g0e
                        )
                      )
                     );
    GrouperAttribute desc = g.attribute("description");
    Assert.assertNull("description null", desc);
    Assert.assertNull("createSource null", g.createSource());
    Assert.assertNotNull("createSubject null", g.createSubject());
    Assert.assertNotNull("createTime null", g.createTime());
    Assert.assertNull("modifySource null", g.modifySource());
    Assert.assertNull("modifySubject null", g.modifySubject());
    Assert.assertNull("modifyTime null", g.modifyTime());
    Assert.assertNotNull("name() !null", g.name());
    Assert.assertTrue(
      "name() value", 
      g.name().equals(GrouperGroup.groupName(Constants.g0s, Constants.g0e))
    );

    // Set description
    String text = "test description";
    try {
      g.attribute("description", text);
      Assert.assertTrue("add description to g0", true);
    } catch (RuntimeException e) {
      Assert.fail("add description to g0");
    }

    // Check values
    GrouperAttribute setDesc = g.attribute("description");
    Assert.assertNotNull("set description !null", setDesc);
    Assert.assertTrue(
      "set description class", 
      Constants.KLASS_GA.equals(setDesc.getClass().getName()) 
    );
    Assert.assertTrue(
      "set description value", setDesc.value().equals(text)
    );
    Assert.assertNull("modifySource null", g.modifySource());
    Assert.assertNotNull("modifySubject !null", g.modifySubject());
    Assert.assertNotNull("modifyTime !null", g.modifyTime());

    s.stop();
  }

}

