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


public class TestQueryGroup extends TestCase {

  private GrouperSession  s;
  private GrouperQuery    q;

  public TestQueryGroup(String name) {
    super(name);
  }

  protected void setUp () {
    DB db = new DB();
    db.emptyTables();
    db.stop();
    s = Constants.createSession();
    Constants.createGroups(s);
    q = new GrouperQuery(s);
  }

  protected void tearDown () {
    s.stop();
  }


  /*
   * TESTS
   */

  public void testQueryInvalidGroupNoMembers() {
    Assert.assertNotNull(q);
    Assert.assertFalse(
      "group: nothing", q.group("this:base:does:not:exist")
    );
    Assert.assertTrue(
      "group: stems=0", q.getStems().size() == 0
    );
    Assert.assertTrue(
      "group: groups=0", q.getGroups().size() == 0
    );
    Assert.assertTrue(
      "group: listValues=0", q.getListValues().size() == 0
    );
    Assert.assertTrue(
      "group: members=0", q.getMembers().size() == 0
    );
  }

  public void testQueryValidGroupNoMembers() {
    Assert.assertNotNull(q);
    Assert.assertTrue(
      "group: something", q.group("root")
    );
    Assert.assertTrue(
      "group: stems=0", q.getStems().size() == 0
    );
    Assert.assertTrue(
      "group: groups=7", q.getGroups().size() == 7
    );
    // Admins && Stemmers
    Assert.assertTrue(
      "group: listValues=7", q.getListValues().size() == 7
    );
    Assert.assertTrue(
      "group: members=0", q.getMembers().size() == 0
    );
  }

  public void testQueryInvalidGroupMembers() {
    Constants.createMembers(s);
    Assert.assertNotNull(q);
    Assert.assertFalse(
      "group: nothing", q.group("this:base:does:not:exist")
    );
    Assert.assertTrue(
      "group: stems=0", q.getStems().size() == 0
    );
    Assert.assertTrue(
      "group: groups=0", q.getGroups().size() == 0
    );
    Assert.assertTrue(
      "group: listValues=0", q.getListValues().size() == 0
    );
    Assert.assertTrue(
      "group: members=0", q.getMembers().size() == 0
    );
  }

  public void testQueryValidGroupMembers() {
    Constants.createMembers(s);
    Assert.assertNotNull(q);
    Assert.assertTrue(
      "group: something", q.group("root")
    );
    Assert.assertTrue(
      "group: stems=0", q.getStems().size() == 0
    );
    Assert.assertTrue(
      "group: groups=7", q.getGroups().size() == 7
    );
    // Admins && Stemmers && Members
    Assert.assertTrue(
      "group: listValues=14", q.getListValues().size() == 14
    );
    Assert.assertTrue(
      "group: members=2", q.getMembers().size() == 2
    );
  }

  public void testQueryValidGroupMembersFuzzy() {
    Constants.createMembers(s);
    Assert.assertNotNull(q);
    Assert.assertTrue(
      "group: something", q.group("roo")
    );
    Assert.assertTrue(
      "group: stems=0", q.getStems().size() == 0
    );
    Assert.assertTrue(
      "group: groups=7", q.getGroups().size() == 7
    );
    // Admins && Stemmers && Members
    Assert.assertTrue(
      "group: listValues=14", q.getListValues().size() == 14
    );
    Assert.assertTrue(
      "group: members=2", q.getMembers().size() == 2
    );
  }

  public void testQueryValidGroupMembersDeeper() {
    Constants.createMembers(s);
    Assert.assertNotNull(q);
    Assert.assertTrue(
      "group: something", q.group("root:a stem")
    );
    Assert.assertTrue(
      "group: stems=0", q.getStems().size() == 0
    );
    Assert.assertTrue(
      "group: groups=2", q.getGroups().size() == 2
    );
    // Admins && Stemmers && Members
    Assert.assertTrue(
      "group: listValues=4", q.getListValues().size() == 4
    );
    Assert.assertTrue(
      "group: members=2", q.getMembers().size() == 2
    );
  }

  public void testQueryValidGroupMembersEvenDeeper() {
    Constants.createMembers(s);
    Assert.assertNotNull(q);
    Assert.assertTrue(
      "group: something", q.group("root:a stem:another stem")
    );
    Assert.assertTrue(
      "group: stems=0", q.getStems().size() == 0
    );
    Assert.assertTrue(
      "group: groups=1", q.getGroups().size() == 1
    );
    // Admins && Stemmers && Members
    Assert.assertTrue(
      "group: listValues=2", q.getListValues().size() == 2
    );
    Assert.assertTrue(
      "group: members=1", q.getMembers().size() == 1
    );
  }

}

