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


public class TestBug405 extends TestCase {

  private GrouperSession  s;
  private GrouperQuery    q;

  public TestBug405(String name) {
    super(name);
  }

  protected void setUp () {
    DB db = new DB();
    db.emptyTables();
    db.stop();
    s = Constants.createSession();
    Constants.createGroups(s);
    Constants.createMembers(s);
  }

  protected void tearDown () {
    s.stop();
  }


  /*
   * TESTS
   */

  public void testBug405MshipsThenPrivs() {
    // gA == "admins"
    // gB == "all"
    // gC == "finance"

    // Assert size of mships and privs
    Assert.assertTrue(
      "[0] gA members=0", Constants.gA.listVals().size() == 0
    );
    Assert.assertTrue(
      "[0] gA ADMINS=1", 
      s.access().whoHas(s, Constants.gA, Grouper.PRIV_ADMIN).size() == 1
    );
    Assert.assertTrue(
      "[0] gA READERS=0", 
      s.access().whoHas(s, Constants.gA, Grouper.PRIV_READ).size() == 0
    );
    Assert.assertTrue(
      "[0] gB members=0", Constants.gB.listVals().size() == 0
    );
    Assert.assertTrue(
      "[0] gB ADMINS=1", 
      s.access().whoHas(s, Constants.gB, Grouper.PRIV_ADMIN).size() == 1
    );
    Assert.assertTrue(
      "[0] gB READERS=0", 
      s.access().whoHas(s, Constants.gB, Grouper.PRIV_READ).size() == 0
    );
    Assert.assertTrue(
      "[0] gC members=0", Constants.gC.listVals().size() == 0
    );
    Assert.assertTrue(
      "[0] gC ADMINS=1", 
      s.access().whoHas(s, Constants.gC, Grouper.PRIV_ADMIN).size() == 1
    );
    Assert.assertTrue(
      "[0] gC READERS=0",
      s.access().whoHas(s, Constants.gC, Grouper.PRIV_READ).size() == 0
    );

    // Create the mships and assert size of mships and privs
    try {
      Constants.gA.listAddVal(Constants.m0);
      Assert.assertTrue("add m0 to gA", true);
    } catch (Exception e) {
      Assert.fail("add m0 to gA");
    }
    try {
      Constants.gB.listAddVal(Constants.m0);
      Assert.assertTrue("add m0 to gB", true);
    } catch (Exception e) {
      Assert.fail("add m0 to gB");
    }
    try {
      Constants.gB.listAddVal(Constants.m1);
      Assert.assertTrue("add m1 to gB", true);
    } catch (Exception e) {
      Assert.fail("add m1 to gB");
    }
    Assert.assertTrue(
      "[1] gA members=1", Constants.gA.listVals().size() == 1
    );
    Assert.assertTrue(
      "[1] gA ADMINS=1", 
      s.access().whoHas(s, Constants.gA, Grouper.PRIV_ADMIN).size() == 1
    );
    Assert.assertTrue(
      "[1] gA READERS=0", 
      s.access().whoHas(s, Constants.gA, Grouper.PRIV_READ).size() == 0
    );
    Assert.assertTrue(
      "[1] gB members=2", Constants.gB.listVals().size() == 2
    );
    Assert.assertTrue(
      "[1] gB ADMINS=1", 
      s.access().whoHas(s, Constants.gB, Grouper.PRIV_ADMIN).size() == 1
    );
    Assert.assertTrue(
      "[1] gB READERS=0", 
      s.access().whoHas(s, Constants.gB, Grouper.PRIV_READ).size() == 0
    );
    Assert.assertTrue(
      "[1] gC members=0", Constants.gC.listVals().size() == 0
    );
    Assert.assertTrue(
      "[1] gC ADMINS=1", 
      s.access().whoHas(s, Constants.gC, Grouper.PRIV_ADMIN).size() == 1
    );
    Assert.assertTrue(
      "[1] gC READERS=0",
      s.access().whoHas(s, Constants.gC, Grouper.PRIV_READ).size() == 0
    );

    // Grant ADMIN and assert size of mships and privs
    Assert.assertTrue(
      "grant ADMIN to gA on gC",
      s.access().grant(
        s, Constants.gC, Constants.gA.toMember(), Grouper.PRIV_ADMIN
      )
    );
    Assert.assertTrue(
      "[2] gA members=1", Constants.gA.listVals().size() == 1
    );
    Assert.assertTrue(
      "[2] gA ADMINS=1", 
      s.access().whoHas(s, Constants.gA, Grouper.PRIV_ADMIN).size() == 1
    );
    Assert.assertTrue(
      "[2] gA READERS=0", 
      s.access().whoHas(s, Constants.gA, Grouper.PRIV_READ).size() == 0
    );
    Assert.assertTrue(
      "[2] gB members=2", Constants.gB.listVals().size() == 2
    );
    Assert.assertTrue(
      "[2] gB ADMINS=1", 
      s.access().whoHas(s, Constants.gB, Grouper.PRIV_ADMIN).size() == 1
    );
    Assert.assertTrue(
      "[2] gB READERS=0", 
      s.access().whoHas(s, Constants.gB, Grouper.PRIV_READ).size() == 0
    );
    Assert.assertTrue(
      "[2] gC members=0", Constants.gC.listVals().size() == 0
    );
    Assert.assertTrue(
      "[2] gC ADMINS=3", 
      s.access().whoHas(s, Constants.gC, Grouper.PRIV_ADMIN).size() == 3
    );
    Assert.assertTrue(
      "[2] gC READERS=0",
      s.access().whoHas(s, Constants.gC, Grouper.PRIV_READ).size() == 0
    );


    // Grant READ and assert size of mships and privs
    Assert.assertTrue(
      "grant READ to gB on gA",
      s.access().grant(
        s, Constants.gA, Constants.gB.toMember(), Grouper.PRIV_READ
      )
    );
    Assert.assertTrue(
      "[3] gA members=1", Constants.gA.listVals().size() == 1
    );
    Assert.assertTrue(
      "[3] gA ADMINS=1", 
      s.access().whoHas(s, Constants.gA, Grouper.PRIV_ADMIN).size() == 1
    );
    Assert.assertTrue(
      "[3] gA READERS=3", 
      s.access().whoHas(s, Constants.gA, Grouper.PRIV_READ).size() == 3
    );
    Assert.assertTrue(
      "[3] gB members=2", Constants.gB.listVals().size() == 2
    );
    Assert.assertTrue(
      "[3] gB ADMINS=1", 
      s.access().whoHas(s, Constants.gB, Grouper.PRIV_ADMIN).size() == 1
    );
    Assert.assertTrue(
      "[3] gB READERS=0", 
      s.access().whoHas(s, Constants.gB, Grouper.PRIV_READ).size() == 0
    );
    Assert.assertTrue(
      "[3] gC members=0", Constants.gC.listVals().size() == 0
    );
    Assert.assertTrue(
      "[3] gC ADMINS=3", 
      s.access().whoHas(s, Constants.gC, Grouper.PRIV_ADMIN).size() == 3
    );
    Assert.assertTrue(
      "[3] gC READERS=0",
      s.access().whoHas(s, Constants.gC, Grouper.PRIV_READ).size() == 0
    );

  }

  public void testBug405PrivsThenMshipPrivs() {
    // gA == "admins"
    // gB == "all"
    // gC == "finance"

    // Assert size of mships and privs
    Assert.assertTrue(
      "[0] gA members=0", Constants.gA.listVals().size() == 0
    );
    Assert.assertTrue(
      "[0] gA ADMINS=1", 
      s.access().whoHas(s, Constants.gA, Grouper.PRIV_ADMIN).size() == 1
    );
    Assert.assertTrue(
      "[0] gA READERS=0", 
      s.access().whoHas(s, Constants.gA, Grouper.PRIV_READ).size() == 0
    );
    Assert.assertTrue(
      "[0] gB members=0", Constants.gB.listVals().size() == 0
    );
    Assert.assertTrue(
      "[0] gB ADMINS=1", 
      s.access().whoHas(s, Constants.gB, Grouper.PRIV_ADMIN).size() == 1
    );
    Assert.assertTrue(
      "[0] gB READERS=0", 
      s.access().whoHas(s, Constants.gB, Grouper.PRIV_READ).size() == 0
    );
    Assert.assertTrue(
      "[0] gC members=0", Constants.gC.listVals().size() == 0
    );
    Assert.assertTrue(
      "[0] gC ADMINS=1", 
      s.access().whoHas(s, Constants.gC, Grouper.PRIV_ADMIN).size() == 1
    );
    Assert.assertTrue(
      "[0] gC READERS=0",
      s.access().whoHas(s, Constants.gC, Grouper.PRIV_READ).size() == 0
    );

    // Grant READ and assert size of mships and privs
    Assert.assertTrue(
      "grant READ to gB on gA",
      s.access().grant(
        s, Constants.gA, Constants.gB.toMember(), Grouper.PRIV_READ
      )
    );
    Assert.assertTrue(
      "[1] gA members=0", Constants.gA.listVals().size() == 0
    );
    Assert.assertTrue(
      "[1] gA ADMINS=1", 
      s.access().whoHas(s, Constants.gA, Grouper.PRIV_ADMIN).size() == 1
    );
    Assert.assertTrue(
      "[1] gA READERS=1", 
      s.access().whoHas(s, Constants.gA, Grouper.PRIV_READ).size() == 1
    );
    Assert.assertTrue(
      "[1] gB members=0", Constants.gB.listVals().size() == 0
    );
    Assert.assertTrue(
      "[1] gB ADMINS=1", 
      s.access().whoHas(s, Constants.gB, Grouper.PRIV_ADMIN).size() == 1
    );
    Assert.assertTrue(
      "[1] gB READERS=0", 
      s.access().whoHas(s, Constants.gB, Grouper.PRIV_READ).size() == 0
    );
    Assert.assertTrue(
      "[1] gC members=0", Constants.gC.listVals().size() == 0
    );
    Assert.assertTrue(
      "[1] gC ADMINS=1", 
      s.access().whoHas(s, Constants.gC, Grouper.PRIV_ADMIN).size() == 1
    );
    Assert.assertTrue(
      "[1] gC READERS=0",
      s.access().whoHas(s, Constants.gC, Grouper.PRIV_READ).size() == 0
    );

    // Grant ADMIN and assert size of mships and privs
    Assert.assertTrue(
      "grant ADMIN to gA on gC",
      s.access().grant(
        s, Constants.gC, Constants.gA.toMember(), Grouper.PRIV_ADMIN
      )
    );
    Assert.assertTrue(
      "[2] gA members=0", Constants.gA.listVals().size() == 0
    );
    Assert.assertTrue(
      "[2] gA ADMINS=1", 
      s.access().whoHas(s, Constants.gA, Grouper.PRIV_ADMIN).size() == 1
    );
    Assert.assertTrue(
      "[2] gA READERS=1", 
      s.access().whoHas(s, Constants.gA, Grouper.PRIV_READ).size() == 1
    );
    Assert.assertTrue(
      "[2] gB members=0", Constants.gB.listVals().size() == 0
    );
    Assert.assertTrue(
      "[2] gB ADMINS=1", 
      s.access().whoHas(s, Constants.gB, Grouper.PRIV_ADMIN).size() == 1
    );
    Assert.assertTrue(
      "[2] gB READERS=0", 
      s.access().whoHas(s, Constants.gB, Grouper.PRIV_READ).size() == 0
    );
    Assert.assertTrue(
      "[2] gC members=0", Constants.gC.listVals().size() == 0
    );
    Assert.assertTrue(
      "[2] gC ADMINS=2", 
      s.access().whoHas(s, Constants.gC, Grouper.PRIV_ADMIN).size() == 2
    );
    Assert.assertTrue(
      "[2] gC READERS=0",
      s.access().whoHas(s, Constants.gC, Grouper.PRIV_READ).size() == 0
    );

    // Create the mships and assert size of mships and privs
    try {
      Constants.gA.listAddVal(Constants.m0);
      Assert.assertTrue("add m0 to gA", true);
    } catch (Exception e) {
      Assert.fail("add m0 to gA");
    }
    try {
      Constants.gB.listAddVal(Constants.m0);
      Assert.assertTrue("add m0 to gB", true);
    } catch (Exception e) {
      Assert.fail("add m0 to gB");
    }
    try {
      Constants.gB.listAddVal(Constants.m1);
      Assert.assertTrue("add m1 to gB", true);
    } catch (Exception e) {
      Assert.fail("add m1 to gB");
    }
    Assert.assertTrue(
      "[3] gA members=1", Constants.gA.listVals().size() == 1
    );
    Assert.assertTrue(
      "[3] gA ADMINS=1", 
      s.access().whoHas(s, Constants.gA, Grouper.PRIV_ADMIN).size() == 1
    );
    Assert.assertTrue(
      "[3] gA READERS=3", 
      s.access().whoHas(s, Constants.gA, Grouper.PRIV_READ).size() == 3
    );
    Assert.assertTrue(
      "[3] gB members=2", Constants.gB.listVals().size() == 2
    );
    Assert.assertTrue(
      "[3] gB ADMINS=1", 
      s.access().whoHas(s, Constants.gB, Grouper.PRIV_ADMIN).size() == 1
    );
    Assert.assertTrue(
      "[3] gB READERS=0", 
      s.access().whoHas(s, Constants.gB, Grouper.PRIV_READ).size() == 0
    );
    Assert.assertTrue(
      "[3] gC members=0", Constants.gC.listVals().size() == 0
    );
    Assert.assertTrue(
      "[3] gC ADMINS=3", 
      s.access().whoHas(s, Constants.gC, Grouper.PRIV_ADMIN).size() == 3
    );
    Assert.assertTrue(
      "[3] gC READERS=0",
      s.access().whoHas(s, Constants.gC, Grouper.PRIV_READ).size() == 0
    );

  }

}

