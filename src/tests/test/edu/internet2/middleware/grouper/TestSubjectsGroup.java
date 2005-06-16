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


public class TestSubjectsGroup extends TestCase {

  private static String gAid = null;

  public TestSubjectsGroup(String name) {
    super(name);
  }

  protected void setUp () {
    DB db = new DB();
    db.emptyTables();
    db.stop();
    createGroups();
  }

  protected void tearDown () {
    // Nothing -- Yet
  }

  private static void createGroups() {
    try {
      Subject subj = SubjectFactory.getSubject(
        Constants.rootI, Constants.rootT
      );
      GrouperSession s = GrouperSession.start(subj);
      GrouperStem ns = GrouperStem.create(
        s, Constants.ns0s, Constants.ns0e
      );
      GrouperGroup gA = GrouperGroup.create(
        s, Constants.gAs, Constants.gAe
      );
      gAid = gA.id();
      s.stop();
    } catch (SubjectNotFoundException e) {
      Assert.fail("unable to load subject");
    }
  }

  /*
   * TESTS
   */
  

  public void testSubjectInterfaceLookupFailureInvalidID() {
    String id   = "invalid id";
    String type = "group";
    try {
      Subject subj = SubjectFactory.getSubject(id, type);
      Assert.fail("invalid subject retrieved");
    } catch (SubjectNotFoundException e) {
      Assert.assertTrue("could not load invalid subject", true);
    }
  }

  public void testSubjectInterfaceLookupFailureInvalidType() {
    try {
      Subject subj = SubjectFactory.getSubject("root:group a", "bad type");
      Assert.fail("invalid subject type retrieved");
    } catch (SubjectNotFoundException e) {
      Assert.assertTrue("could not load invalid subject type", true);
    }
  }

  public void testSubjectInterfaceLookupGroupAByID() {
    try {
      Subject subj = SubjectFactory.getSubject(gAid, "group");
      Assert.assertTrue("loaded subject", true);
      Assert.assertNotNull(subj);
      Assert.assertTrue("id", gAid.equals(subj.getId()));
      Assert.assertTrue("type", subj.getType().getName().equals("group"));
      Assert.assertTrue("name", subj.getName().equals("root:group a"));
      // TODO Assert.assertTrue("description", subj.getDescription().equals(""));
    } catch (SubjectNotFoundException e) {
      Assert.fail("unable to load subject");
    }
  }

  public void testSubjectInterfaceLookupGroupAByIDNoType() {
    try {
      Subject subj = SubjectFactory.getSubject(gAid);
      Assert.fail("loaded invalid subject");
    } catch (SubjectNotFoundException e) {
      Assert.assertTrue("failed to load invalid subject", true);
    }
  }

  public void testSubjectInterfaceLookupGroupAByName() {
    try {
      Subject subj = SubjectFactory.getSubject("root:group a", "group");
      Assert.assertTrue("loaded subject", true);
      Assert.assertNotNull(subj);
      Assert.assertTrue("id", gAid.equals(subj.getId()));
      Assert.assertTrue("type", subj.getType().getName().equals("group"));
      Assert.assertTrue("name", subj.getName().equals("root:group a"));
      // TODO Assert.assertTrue("description", subj.getDescription().equals(""));
    } catch (SubjectNotFoundException e) {
      Assert.fail("unable to load subject");
    }
  }

  public void testSubjectInterfaceLookupGroupAByNameNoType() {
    try {
      Subject subj = SubjectFactory.getSubject("root:group a");
      Assert.fail("loaded invalid subject");
    } catch (SubjectNotFoundException e) {
      Assert.assertTrue("failed to load invalid subject", true);
    }
  }

  // By _name_
  // TODO Multipe returns
  public void testSubjectInterfaceSearchByIDByName() {
    Set vals = SubjectFactory.searchByIdentifier("root:group a", "group");
    Assert.assertTrue("vals.size()==1", vals.size() == 1);
    Iterator iter = vals.iterator();
    while (iter.hasNext()) {
      Subject subj = (Subject) iter.next();
      Assert.assertTrue("id", gAid.equals(subj.getId()));
      Assert.assertTrue("type", subj.getType().getName().equals("group"));
      Assert.assertTrue("name", subj.getName().equals("root:group a"));
    }
  }

  // By _name_, no type
  public void testSubjectInterfaceSearchByIDByNameNoType() {
    Set vals = SubjectFactory.searchByIdentifier("root:group a");
    Assert.assertTrue("vals.size()==0", vals.size() == 0);
  }

  // TODO By _displayName_
  // TODO No type
  // TODO Multipe returns
/*
  public void testSubjectInterfaceSearchByIDByDisplayName() {
    Set vals = SubjectFactory.searchByIdentifier("root:group a", "group");
    Assert.assertTrue("vals.size()==1", vals.size() == 1);
    Iterator iter = vals.iterator();
    while (iter.hasNext()) {
      Subject subj = (Subject) iter.next();
      Assert.assertTrue("id", gAid.equals(subj.getId()));
      Assert.assertTrue("type", subj.getType().getName().equals("group"));
      Assert.assertTrue("name", subj.getName().equals("root:group a"));
    }
  }
*/

  // By _guid_
  public void testSubjectInterfaceSearchByIDByID() {
    Set vals = SubjectFactory.searchByIdentifier(gAid, "group");
    Assert.assertTrue("vals.size()==1", vals.size() == 1);
    Iterator iter = vals.iterator();
    while (iter.hasNext()) {
      Subject subj = (Subject) iter.next();
      Assert.assertTrue("id", gAid.equals(subj.getId()));
      Assert.assertTrue("type", subj.getType().getName().equals("group"));
      Assert.assertTrue("name", subj.getName().equals("root:group a"));
    }
  }

  // By _guid_, no type
  public void testSubjectInterfaceSearchByIDByIDNoType() {
    Set vals = SubjectFactory.searchByIdentifier(gAid);
    Assert.assertTrue("vals.size()==0", vals.size() == 0);
  }

  // TODO Test attributes, etc.
}

