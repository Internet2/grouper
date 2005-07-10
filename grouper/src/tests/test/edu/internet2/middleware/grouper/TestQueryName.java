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


public class TestQueryName extends TestCase {

  private GrouperSession  s;
  private GrouperQuery    q;

  public TestQueryName(String name) {
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

  public void testQueryStemInvalidName() {
    Assert.assertNotNull(q);
    try {
      Assert.assertFalse(
        "name: nothing", q.namespace("invalid name")
      );
      Assert.assertTrue(
        "name: 0 members", q.query().size() == 0
      );
      Assert.assertFalse(
        "displayName: nothing", 
        q.namespace("invalid displayName")
      );
      Assert.assertTrue(
        "displayName: 0 members", q.query().size() == 0
      );
      Assert.assertFalse(
        "displayExtension: nothing", 
        q.namespace("invalid displayExtension")
      );
      Assert.assertTrue(
        "displayExtension: 0 members", q.query().size() == 0
      );
    } catch (GrouperException e) {
      Assert.fail(e.getMessage());
    }
  }
  public void testQueryGroupInvalidName() {
    Assert.assertNotNull(q);
    try {
      Assert.assertFalse(
        "name: nothing", q.group("invalid name")
      );
      Assert.assertTrue(
        "name: 0 members", q.query().size() == 0
      );
      Assert.assertFalse(
        "displayName: nothing", 
        q.group("invalid displayName")
      );
      Assert.assertTrue(
        "displayName: 0 members", q.query().size() == 0
      );
      Assert.assertFalse(
        "displayExtension: nothing", 
        q.group("invalid displayExtension")
      );
      Assert.assertTrue(
        "displayExtension: 0 members", q.query().size() == 0
      );
    } catch (GrouperException e) {
      Assert.fail(e.getMessage());
    }
  }

  public void testQueryStemInvalidNameButMembersExists() {
    Constants.createMembers(s);
    Assert.assertNotNull(q);
    try {
      Assert.assertFalse(
        "name: nothing", q.namespace("invalid name")
      );
      Assert.assertTrue(
        "name: 0 members", q.query().size() == 0
      );
      Assert.assertFalse(
        "displayName: nothing", 
        q.namespace("invalid displayName")
      );
      Assert.assertTrue(
        "displayName: 0 members", q.query().size() == 0
      );
      Assert.assertFalse(
        "displayExtension: nothing", 
        q.namespace("invalid displayExtension")
      );
      Assert.assertTrue(
        "displayExtension: 0 members", q.query().size() == 0
      );
    } catch (GrouperException e) {
      Assert.fail(e.getMessage());
    }
  }
  public void testQueryGroupInvalidNameButMembersExists() {
    Constants.createMembers(s);
    Assert.assertNotNull(q);
    try {
      Assert.assertFalse(
        "name: nothing", q.group("invalid name")
      );
      Assert.assertTrue(
        "name: 0 members", q.query().size() == 0
      );
      Assert.assertFalse(
        "displayName: nothing", 
        q.group("invalid displayName")
      );
      Assert.assertTrue(
        "displayName: 0 members", q.query().size() == 0
      );
      Assert.assertFalse(
        "displayExtension: nothing", 
        q.group("invalid displayExtension")
      );
      Assert.assertTrue(
        "displayExtension: 0 members", q.query().size() == 0
      );
    } catch (GrouperException e) {
      Assert.fail(e.getMessage());
    }
  }

  public void testQueryStemValidNameNoMembers() {
    Assert.assertNotNull(q);
    try {
      Assert.assertFalse(
        "name: nothing", q.namespace("root:root group")
      );
      Assert.assertTrue(
        "name: 0 members", q.query().size() == 0
      );
      Assert.assertFalse(
        "displayName: nothing", q.namespace("root:root group")
      );
      Assert.assertTrue(
        "displayName: 0 members", q.query().size() == 0
      );
      Assert.assertFalse(
        "displayExtension: nothing", q.namespace("root group")
      );
      Assert.assertTrue(
        "displayExtension: 0 members", q.query().size() == 0
      );
    } catch (GrouperException e) {
      Assert.fail(e.getMessage());
    }
  }

  public void testQueryGroupValidNameNoMembers() {
    Assert.assertNotNull(q);
    try {
      Assert.assertFalse(
        "name: nothing", q.group("root:root group")
      );
      Assert.assertTrue(
        "name: 0 members", q.query().size() == 0
      );
      Assert.assertFalse(
        "displayName: nothing", q.group("root:root group")
      );
      Assert.assertTrue(
        "displayName: 0 members", q.query().size() == 0
      );
      Assert.assertFalse(
        "displayExtension: nothing", q.group("root group")
      );
      Assert.assertTrue(
        "displayExtension: 0 members", q.query().size() == 0
      );
    } catch (GrouperException e) {
      Assert.fail(e.getMessage());
    }
  }

  public void testQueryStemValidNameAndMemberExists() {
    Constants.createMembers(s);
    Assert.assertNotNull(q);
    try {
      Assert.assertFalse(
        "name: nothing", q.namespace("root:root group")
      );
      Assert.assertTrue(
        "name: 0 members", q.query().size() == 0
      );
      Assert.assertFalse(
        "displayName: nothing", q.namespace("root:root group")
      );
      Assert.assertTrue(
        "displayName: 0 members", q.query().size() == 0
      );
      Assert.assertFalse(
        "displayExtension: nothing", q.namespace("root group")
      );
      Assert.assertTrue(
        "displayExtension: 0 members", q.query().size() == 0
      );
    } catch (GrouperException e) {
      Assert.fail(e.getMessage());
    }
  }

  public void testQueryGroupValidNameAndMemberExists() {
    Constants.createMembers(s);
    Assert.assertNotNull(q);
    try {
      Assert.assertTrue(
        "name: something", q.group("root:root group")
      );
      Assert.assertTrue(
        "name: 1 member", q.query().size() == 1
      );
      Assert.assertTrue(
        "displayName: something", q.group("root:root group")
      );
      Assert.assertTrue(
        "displayName: 1 member", q.query().size() == 1
      );
      Assert.assertTrue(
        "displayExtension: something", q.group("root group")
      );
      Assert.assertTrue(
        "displayExtension: 1 member", q.query().size() == 1
      );
    } catch (GrouperException e) {
      Assert.fail(e.getMessage());
    }
  }

  public void testQueryStemValidNamesAndMembersExists() {
    Constants.createMembers(s);
    Assert.assertNotNull(q);
    try {
      Assert.assertFalse(
        "name: nothing", q.namespace("group")
      );
      Assert.assertTrue(
        "name: 0 members", q.query().size() == 0
      );
      Assert.assertFalse(
        "displayName: nothing", q.namespace("group")
      );
      Assert.assertTrue(
        "displayName: 0 members", q.query().size() == 0
      );
      Assert.assertFalse(
        "displayExtension: nothing", q.namespace("group")
      );
      Assert.assertTrue(
        "displayExtension: 0 members", q.query().size() == 0
      );
    } catch (GrouperException e) {
      Assert.fail(e.getMessage());
    }
  }

  public void testQueryGroupValidNamesAndMembersExists() {
    Constants.createMembers(s);
    Assert.assertNotNull(q);
    try {
      Assert.assertTrue(
        "name: something", q.group("group")
      );
      Assert.assertTrue(
        "name: 7 members", q.query().size() == 7
      );
      Assert.assertTrue(
        "displayName: something", q.group("group")
      );
      Assert.assertTrue(
        "displayName: 7 members", q.query().size() == 7
      );
      Assert.assertTrue(
        "displayExtension: something", q.group("group")
      );
      Assert.assertTrue(
        "displayExtension: 7 members", q.query().size() == 7
      );
    } catch (GrouperException e) {
      Assert.fail(e.getMessage());
    }
  }

  public void testQueryStemValidNamesAndMembersExistsButOnlyOne() {
    Constants.createMembers(s);
    Assert.assertNotNull(q);
    try {
      Assert.assertFalse(
        "name: nothing", q.namespace("another")
      );
      Assert.assertTrue(
        "name: 0 members", q.query().size() == 0
      );
      Assert.assertFalse(
        "displayName: nothing", q.namespace("another")
      );
      Assert.assertTrue(
        "displayName: 0 members", q.query().size() == 0
      );
      Assert.assertFalse(
        "displayExtension: nothing", q.namespace("another")
      );
      Assert.assertTrue(
        "displayExtension: 0 members", q.query().size() == 0
      );
    } catch (GrouperException e) {
      Assert.fail(e.getMessage());
    }
  }

  public void testQueryGroupValidNamesAndMembersExistsButOnlyOne() {
    Constants.createMembers(s);
    Assert.assertNotNull(q);
    try {
      Assert.assertTrue(
        "name: something", q.group("another")
      );
      Assert.assertTrue(
        "name: 1 member", q.query().size() == 1
      );
      Assert.assertTrue(
        "displayName: something", q.group("another")
      );
      Assert.assertTrue(
        "displayName: 1 member", q.query().size() == 1
      );
      Assert.assertTrue(
        "displayExtension: something", q.group("another")
      );
      Assert.assertTrue(
        "displayExtension: 1 member", q.query().size() == 1
      );
    } catch (GrouperException e) {
      Assert.fail(e.getMessage());
    }
  }

}

