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

public class TestGroupsDisplayName extends TestCase {

  private String          attrE = "displayExtension";
  private String          attrN = "displayName";
  private GrouperSession  s;
  private String          valE  = "displayed extension";

  public TestGroupsDisplayName(String name) {
    super(name);
  }

  protected void setUp () {
    DB db = new DB();
    db.emptyTables();
    db.stop();
    s = Constants.createSession();
    Constants.createGroups(s);
  }

  protected void tearDown () {
    s.stop();
  }


  /*
   * TESTS
   */

  public void testDefaultDisplayName() {
    Assert.assertNotNull("session != null", s);
    Assert.assertNotNull(
      attrE + " != null",
      Constants.g0.attribute(attrE)
    );
    Assert.assertTrue(
      attrE + " right value",
      Constants.g0.attribute(attrE).value().equals(Constants.g0e)
    );
    Assert.assertNotNull(
      attrN + " != null",
      Constants.g0.attribute(attrN)
    );
    Assert.assertTrue(
      attrN + " right value",
      Constants.g0.attribute(attrN).value().equals(
        Constants.g0.attribute("name").value()
      )
    );
  }

  public void testSetDisplayExtn() {
    Assert.assertNotNull("session != null", s);
    Constants.g0.attribute(attrE, valE);
    Assert.assertNotNull(
      attrE + " != null",
      Constants.g0.attribute(attrE)
    );
    Assert.assertTrue(
      attrE + " right value",
      Constants.g0.attribute(attrE).value().equals(valE)
    );
    Assert.assertNotNull(
      attrN + " != null",
      Constants.g0.attribute(attrN)
    );
    Assert.assertTrue(
      attrN + " right value",
      Constants.g0.attribute(attrN).value().equals(
        "root:displayed extension"
      )
    );
  }

  public void testUnsetDisplayExtn() {
    Assert.assertNotNull("session != null", s);
    Constants.g0.attribute(attrE, valE);
    Assert.assertNotNull(
      attrE + " != null",
      Constants.g0.attribute(attrE)
    );
    Assert.assertTrue(
      attrE + " right value",
      Constants.g0.attribute(attrE).value().equals(valE)
    );
    Assert.assertNotNull(
      attrN + " != null",
      Constants.g0.attribute(attrN)
    );
    Assert.assertTrue(
      attrN + " right value",
      Constants.g0.attribute(attrN).value().equals(
        "root:displayed extension"
      )
    );
    Constants.g0.attribute(attrE, null);
    Assert.assertNotNull(
      attrE + " != null/reset",
      Constants.g0.attribute(attrE)
    );
    Assert.assertTrue(
      attrE + " right value/reset",
      Constants.g0.attribute(attrE).value().equals(Constants.g0e)
    );
    Assert.assertNotNull(
      attrN + " != null/reset",
      Constants.g0.attribute(attrN)
    );
    Assert.assertTrue(
      attrN + " right value/reset",
      Constants.g0.attribute(attrN).value().equals(
        Constants.g0.attribute("name").value()
      )
    );
  }

  public void testSetDisplayExtnOnParent() {
    Assert.assertNotNull("session != null", s);
    // Parent
    Constants.ns0.attribute(attrE, valE);
    Assert.assertNotNull(
      attrE + " != null (ns0)",
      Constants.ns0.attribute(attrE)
    );
    Assert.assertTrue(
      attrE + " right value (ns0)",
      Constants.ns0.attribute(attrE).value().equals(valE)
    );
    Assert.assertNotNull(
      attrN + " != null (ns0)",
      Constants.ns0.attribute(attrN)
    );
    Assert.assertTrue(
      attrN + " right value (ns0)",
      Constants.ns0.attribute(attrN).value().equals(valE)
    );
    // Child
    Assert.assertNotNull(
      attrE + " != null (g0)",
      Constants.g0.attribute(attrE)
    );
    Assert.assertTrue(
      attrE + " right value (g0)",
      Constants.g0.attribute(attrE).value().equals(
        Constants.g0.attribute("extension").value()
      )
    );
    Assert.assertNotNull(
      attrN + " != null (g0)",
      Constants.g0.attribute(attrN)
    );
    Assert.assertTrue(
      attrN + " right value (g0)",
      Constants.g0.attribute(attrN).value().equals(
        "displayed extension:root group"
      )
    );
  }

  public void testUnsetDisplayExtnOnParent() {
    Assert.assertNotNull("session != null", s);
    // Parent
    Constants.ns0.attribute(attrE, valE);
    Assert.assertNotNull(
      attrE + " != null (ns0)",
      Constants.ns0.attribute(attrE)
    );
    Assert.assertTrue(
      attrE + " right value (ns0)",
      Constants.ns0.attribute(attrE).value().equals(valE)
    );
    Assert.assertNotNull(
      attrN + " != null (ns0)",
      Constants.ns0.attribute(attrN)
    );
    Assert.assertTrue(
      attrN + " right value (ns0)",
      Constants.ns0.attribute(attrN).value().equals(valE)
    );
    // Child
    Assert.assertNotNull(
      attrE + " != null (g0)",
      Constants.g0.attribute(attrE)
    );
    Assert.assertTrue(
      attrE + " right value (g0)",
      Constants.g0.attribute(attrE).value().equals(
        Constants.g0.attribute("extension").value()
      )
    );
    Assert.assertNotNull(
      attrN + " != null (g0)",
      Constants.g0.attribute(attrN)
    );
    Assert.assertTrue(
      attrN + " right value (g0)",
      Constants.g0.attribute(attrN).value().equals(
        "displayed extension:root group"
      )
    );

    // Parent
    Constants.ns0.attribute(attrE, null);
    Assert.assertNotNull(
      attrE + " != null (ns0/reset)",
      Constants.ns0.attribute(attrE)
    );
    Assert.assertTrue(
      attrE + " right value (ns0/reset)",
      Constants.ns0.attribute(attrE).value().equals(Constants.ns0e)
    );
    Assert.assertNotNull(
      attrN + " != null (ns0/reset)",
      Constants.ns0.attribute(attrN)
    );
    Assert.assertTrue(
      attrN + " right value (ns0/reset)",
      Constants.ns0.attribute(attrN).value().equals(
        Constants.ns0.attribute("name").value()
      )
    );
    // Child
    Assert.assertNotNull(
      attrE + " != null (g0/reset)",
      Constants.g0.attribute(attrE)
    );
    Assert.assertTrue(
      attrE + " right value (g0/reset)",
      Constants.g0.attribute(attrE).value().equals(Constants.g0e)
    );
    Assert.assertNotNull(
      attrN + " != null (g0/reset)",
      Constants.g0.attribute(attrN)
    );
    Assert.assertTrue(
      attrN + " right value (g0/reset)",
      Constants.g0.attribute(attrN).value().equals(
        "root:root group"
      )
    );
  }

  public void testSetDisplayExtnOnGrandparent() {
    Assert.assertNotNull("session != null", s);
    // Grandparent
    Constants.ns0.attribute(attrE, valE);
    Assert.assertNotNull(
      attrE + " != null (ns0)",
      Constants.ns0.attribute(attrE)
    );
    Assert.assertTrue(
      attrE + " right value (ns0)",
      Constants.ns0.attribute(attrE).value().equals(valE)
    );
    Assert.assertNotNull(
      attrN + " != null (ns0)",
      Constants.ns0.attribute(attrN)
    );
    Assert.assertTrue(
      attrN + " right value (ns0)",
      Constants.ns0.attribute(attrN).value().equals(valE)
    );
    // Parent
    Assert.assertNotNull(
      attrE + " != null (ns1)",
      Constants.ns1.attribute(attrE)
    );
    Assert.assertTrue(
      attrE + " right value (ns1)",
      Constants.ns1.attribute(attrE).value().equals(
        Constants.ns1.attribute("extension").value()
      )
    );
    Assert.assertNotNull(
      attrN + " != null (ns1)",
      Constants.ns1.attribute(attrN)
    );
    Assert.assertTrue(
      attrN + " right value (ns1)",
      Constants.ns1.attribute(attrN).value().equals(
        "displayed extension:a stem"
      )
    );
    // Child
    Assert.assertNotNull(
      attrE + " != null (g1)",
      Constants.g1.attribute(attrE)
    );
    Assert.assertTrue(
      attrE + " right value (g1)",
      Constants.g1.attribute(attrE).value().equals(
        Constants.g1.attribute("extension").value()
      )
    );
    Assert.assertNotNull(
      attrN + " != null (g1)",
      Constants.g1.attribute(attrN)
    );
    Assert.assertTrue(
      attrN + " right value (g1)",
      Constants.g1.attribute(attrN).value().equals(
        "displayed extension:a stem:a group"
      )
    );
  }

  public void testUnsetDisplayExtnOnGrandparent() {
    Assert.assertNotNull("session != null", s);
    // Grandparent
    Constants.ns0.attribute(attrE, valE);
    Assert.assertNotNull(
      attrE + " != null (ns0)",
      Constants.ns0.attribute(attrE)
    );
    Assert.assertTrue(
      attrE + " right value (ns0)",
      Constants.ns0.attribute(attrE).value().equals(valE)
    );
    Assert.assertNotNull(
      attrN + " != null (ns0)",
      Constants.ns0.attribute(attrN)
    );
    Assert.assertTrue(
      attrN + " right value (ns0)",
      Constants.ns0.attribute(attrN).value().equals(valE)
    );
    // Parent
    Assert.assertNotNull(
      attrE + " != null (ns1)",
      Constants.ns1.attribute(attrE)
    );
    Assert.assertTrue(
      attrE + " right value (ns1)",
      Constants.ns1.attribute(attrE).value().equals(
        Constants.ns1.attribute("extension").value()
      )
    );
    Assert.assertNotNull(
      attrN + " != null (ns1)",
      Constants.ns1.attribute(attrN)
    );
    Assert.assertTrue(
      attrN + " right value (ns1)",
      Constants.ns1.attribute(attrN).value().equals(
        "displayed extension:a stem"
      )
    );
    // Child
    Assert.assertNotNull(
      attrE + " != null (g1)",
      Constants.g1.attribute(attrE)
    );
    Assert.assertTrue(
      attrE + " right value (g1)",
      Constants.g1.attribute(attrE).value().equals(
        Constants.g1.attribute("extension").value()
      )
    );
    Assert.assertNotNull(
      attrN + " != null (g1)",
      Constants.g1.attribute(attrN)
    );
    Assert.assertTrue(
      attrN + " right value (g1)",
      Constants.g1.attribute(attrN).value().equals(
        "displayed extension:a stem:a group"
      )
    );

    // Grandparent
    Constants.ns0.attribute(attrE, null);
    Assert.assertNotNull(
      attrE + " != null (ns0/reset)",
      Constants.ns0.attribute(attrE)
    );
    Assert.assertTrue(
      attrE + " right value (ns0/reset)",
      Constants.ns0.attribute(attrE).value().equals(Constants.ns0e)
    );
    Assert.assertNotNull(
      attrN + " != null (ns0/reset)",
      Constants.ns0.attribute(attrN)
    );
    Assert.assertTrue(
      attrN + " right value (ns0/reset)",
      Constants.ns0.attribute(attrN).value().equals(
        Constants.ns0.attribute("name").value()
      )
    );
    // Parent
    Assert.assertNotNull(
      attrE + " != null (ns1/reset)",
      Constants.ns1.attribute(attrE)
    );
    Assert.assertTrue(
      attrE + " right value (ns1/reset)",
      Constants.ns1.attribute(attrE).value().equals(Constants.ns1e)
    );
    Assert.assertNotNull(
      attrN + " != null (ns1/reset)",
      Constants.ns1.attribute(attrN)
    );
    Assert.assertTrue(
      attrN + " right value (ns1/reset)",
      Constants.ns1.attribute(attrN).value().equals(
        "root:a stem"
      )
    );
    // Child
    Assert.assertNotNull(
      attrE + " != null (g1/reset)",
      Constants.g1.attribute(attrE)
    );
    Assert.assertTrue(
      attrE + " right value (g1/reset)",
      Constants.g1.attribute(attrE).value().equals(Constants.g1e)
    );
    Assert.assertNotNull(
      attrN + " != null (g1/reset)",
      Constants.g1.attribute(attrN)
    );
    Assert.assertTrue(
      attrN + " right value (g1/reset)",
      Constants.g1.attribute(attrN).value().equals(
        "root:a stem:a group"
      )
    );
  }

}

