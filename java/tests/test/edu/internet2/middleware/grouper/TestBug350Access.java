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

public class TestBug350Access extends TestCase {

  public TestBug350Access(String name) {
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
  

  //
  // Grant m0 ADMIN on g0
  // Grant g0 ADMIN on g1
  // Make  m1 a member of g0
  //
  public void testBug350Access() {
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
    // Create g0
    GrouperGroup g0  = GrouperGroup.create(
                         s, Constants.g0s, Constants.g0e
                       );
    // Create g1
    GrouperGroup g1  = GrouperGroup.create(
                         s, Constants.g1s, Constants.g1e
                       );
    // Load m0
    GrouperMember m0 = GrouperMember.load(
                         s, Constants.mem0I, Constants.mem0T
                       );
    // Load m1
    GrouperMember m1 = GrouperMember.load(
                         s, Constants.mem1I, Constants.mem1T
                       );

    // Grant m0 ADMIN on g0
    Assert.assertTrue(
      "grant m0 ADMIN on g0", 
      s.access().grant(s, g0, m0, Grouper.PRIV_ADMIN)
    );
    // Grant g0 ADMIN on g1
    Assert.assertTrue(
      "grant g0 ADMIN on g1", 
      s.access().grant(s, g1, g0.toMember(), Grouper.PRIV_ADMIN)
    );
    // Make m1 a member of g0
    try {
      g0.listAddVal(m1);
      Assert.assertTrue("add m1 to g0", true);
    } catch (RuntimeException e) {
      Assert.fail("add m1 to g0");
    };

    // Check with group methods to avoid the privilege interface

    // g0
    Assert.assertTrue(
      "g0 members == 0", g0.listVals("members").size() == 1
    ); 
    Assert.assertTrue(
      "g0 imm members == 0", g0.listImmVals("members").size() == 1
    ); 
    Assert.assertTrue(
      "g0 eff members == 0", g0.listEffVals("members").size() == 0
    ); 
    Assert.assertTrue(
      "g0 admins == 2", g0.listVals("admins").size() == 2
    ); 
    Assert.assertTrue(
      "g0 imm admins == 2", g0.listImmVals("admins").size() == 2
    ); 
    Assert.assertTrue(
      "g0 eff admins == 0", g0.listEffVals("admins").size() == 0
    ); 
    Iterator iter0AI = g0.listImmVals("admins").iterator();
    while (iter0AI.hasNext()) {
      GrouperList lv = (GrouperList) iter0AI.next();
      if (lv.member().typeID().equals("person")) {
        if        (lv.member().subjectID().equals(Constants.rootI)) {
          Assert.assertTrue("g0 imm admin == rootI", true);
        } else if (lv.member().subjectID().equals(Constants.mem0I)) {
          Assert.assertTrue("g0 imm admin == mem0I", true);
        } else {
          Assert.fail(
            "invalid g0 imm admin == " + lv.member()
          );
        }
      } else {
        Assert.fail(
          "g0 admin invalid member type: " + 
          lv.member().typeID()
        ); 
      }
    }

    // g1
    Assert.assertTrue(
      "g1 members == 0", g1.listVals("members").size() == 0
    ); 
    Assert.assertTrue(
      "g1 imm members == 0", g1.listImmVals("members").size() == 0
    ); 
    Assert.assertTrue(
      "g1 eff members == 0", g1.listEffVals("members").size() == 0
    ); 
    Assert.assertTrue(
      "g1 admins == 3", g1.listVals("admins").size() == 3
    ); 
    Assert.assertTrue(
      "g1 imm admins == 2", g1.listImmVals("admins").size() == 2
    ); 
    Assert.assertTrue(
      "g1 eff admins == 1", g1.listEffVals("admins").size() == 1
    ); 
    Iterator iter1AI = g1.listImmVals("admins").iterator();
    while (iter1AI.hasNext()) {
      GrouperList lv = (GrouperList) iter1AI.next();
      if        (lv.member().typeID().equals("group")) {
        Assert.assertTrue(
          "g0 imm admin == g0", lv.member().toGroup().equals(g0)
        );
      } else if (lv.member().typeID().equals("person")) {
        if (lv.member().subjectID().equals(Constants.rootI)) {
          Assert.assertTrue("g0 imm admin == rootI", true);
        } else {
          Assert.fail(
            "invalid g0 imm admin == " + lv.member()
          );
        }
      }
    }
    Iterator iter1AE = g1.listEffVals("admins").iterator();
    while (iter1AE.hasNext()) {
      GrouperList lv = (GrouperList) iter1AE.next();
      if (lv.member().typeID().equals("person")) {
        if (lv.member().subjectID().equals(Constants.mem1I)) {
          Assert.assertTrue("g0 imm admin == mem1I", true);
          List chain = lv.chain();
          Assert.assertTrue(
            "chain size == 1", chain.size() == 1
          );
          MemberVia mv0 = (MemberVia) chain.get(0);
          Assert.assertNotNull("mv0 !null", mv0);
          GrouperList c0 = mv0.toList(s);
          Assert.assertNotNull("c0 !null", c0);
          Assert.assertTrue(
            "c0 g == g1", c0.group().equals(g1)
          );
          Assert.assertTrue(
            "c0 m == g0", c0.member().toGroup().equals(g0)
          );
          Assert.assertTrue(
            "c0 l == members", c0.groupField().equals("members")
          );
        } else {
          Assert.fail(
            "invalid g0 imm admin == " + lv.member()
          );
        }
      } else {
        Assert.fail(
          "g0 admin invalid member type: " + lv.member().typeID()
        ); 
      }
    }

    s.stop();
  }

}

