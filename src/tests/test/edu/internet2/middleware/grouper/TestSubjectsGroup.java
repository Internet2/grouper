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
      gA.attribute("description", "this is group a");
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

  // By _stem_
  // TODO Multiple returns
  public void testSubjectInterfaceSearchByStem() {
    Set vals = SubjectFactory.search("root");
    Assert.assertTrue("vals.size()==1", vals.size() == 1);
    Iterator iter = vals.iterator();
    while (iter.hasNext()) {
      Subject subj = (Subject) iter.next();
      Assert.assertTrue("id", gAid.equals(subj.getId()));
      Assert.assertTrue("type", subj.getType().getName().equals("group"));
      Assert.assertTrue("name", subj.getName().equals("root:group a"));
    }
  }

  // By _stem_, partial
  // TODO Multiple returns
  public void testSubjectInterfaceSearchByStemPartial() {
    Set vals = SubjectFactory.search("oo");
    Assert.assertTrue("vals.size()==1", vals.size() == 1);
    Iterator iter = vals.iterator();
    while (iter.hasNext()) {
      Subject subj = (Subject) iter.next();
      Assert.assertTrue("id", gAid.equals(subj.getId()));
      Assert.assertTrue("type", subj.getType().getName().equals("group"));
      Assert.assertTrue("name", subj.getName().equals("root:group a"));
    }
  }

  // By _stem_, no results
  public void testSubjectInterfaceSearchByStemNil() {
    Set vals = SubjectFactory.search("rOOt");
    Assert.assertTrue("vals.size()==0", vals.size() == 0);
  }

  // By _extension_
  // TODO Multiple returns
  public void testSubjectInterfaceSearchByExtn() {
    Set vals = SubjectFactory.search("group a");
    Assert.assertTrue("vals.size()==1", vals.size() == 1);
    Iterator iter = vals.iterator();
    while (iter.hasNext()) {
      Subject subj = (Subject) iter.next();
      Assert.assertTrue("id", gAid.equals(subj.getId()));
      Assert.assertTrue("type", subj.getType().getName().equals("group"));
      Assert.assertTrue("name", subj.getName().equals("root:group a"));
    }
  }

  // By _extension, partial
  // TODO Multiple returns
  public void testSubjectInterfaceSearchByExtnPartial() {
    Set vals = SubjectFactory.search("a");
    Assert.assertTrue("vals.size()==1", vals.size() == 1);
    Iterator iter = vals.iterator();
    while (iter.hasNext()) {
      Subject subj = (Subject) iter.next();
      Assert.assertTrue("id", gAid.equals(subj.getId()));
      Assert.assertTrue("type", subj.getType().getName().equals("group"));
      Assert.assertTrue("name", subj.getName().equals("root:group a"));
    }
  }

  // By _extension, no results
  public void testSubjectInterfaceSearchByExtnNil() {
    Set vals = SubjectFactory.search("group A");
    Assert.assertTrue("vals.size()==0", vals.size() == 0);
  }

/*
  // TODO By _displayExtension_
  // TODO Multiple returns
  public void testSubjectInterfaceSearchByDisplayExtn() {
    Set vals = SubjectFactory.search("root");
    Assert.assertTrue("vals.size()==1", vals.size() == 1);
    Iterator iter = vals.iterator();
    while (iter.hasNext()) {
      Subject subj = (Subject) iter.next();
      Assert.assertTrue("id", gAid.equals(subj.getId()));
      Assert.assertTrue("type", subj.getType().getName().equals("group"));
      Assert.assertTrue("name", subj.getName().equals("root:group a"));
    }
  }

  // TODO By _displayExtension_, partial
  // TODO Multiple returns
  public void testSubjectInterfaceSearchByDisplayExtnPartial() {
    Set vals = SubjectFactory.search("oo");
    Assert.assertTrue("vals.size()==1", vals.size() == 1);
    Iterator iter = vals.iterator();
    while (iter.hasNext()) {
      Subject subj = (Subject) iter.next();
      Assert.assertTrue("id", gAid.equals(subj.getId()));
      Assert.assertTrue("type", subj.getType().getName().equals("group"));
      Assert.assertTrue("name", subj.getName().equals("root:group a"));
    }
  }

  // TODO By _displayExtension_, no results
  public void testSubjectInterfaceSearchByDisplayExtnNil() {
    Set vals = SubjectFactory.search("root root");
    Assert.assertTrue("vals.size()==0", vals.size() == 0);
  }
*/

  // By _name_
  // TODO Multiple returns
  public void testSubjectInterfaceSearchByName() {
    Set vals = SubjectFactory.search("root:group a");
    Assert.assertTrue("vals.size()==1", vals.size() == 1);
    Iterator iter = vals.iterator();
    while (iter.hasNext()) {
      Subject subj = (Subject) iter.next();
      Assert.assertTrue("id", gAid.equals(subj.getId()));
      Assert.assertTrue("type", subj.getType().getName().equals("group"));
      Assert.assertTrue("name", subj.getName().equals("root:group a"));
    }
  }

  // By _name_, partial
  // TODO Multiple returns
  public void testSubjectInterfaceSearchByNamePartial() {
    Set vals = SubjectFactory.search("root:group");
    Assert.assertTrue("vals.size()==1", vals.size() == 1);
    Iterator iter = vals.iterator();
    while (iter.hasNext()) {
      Subject subj = (Subject) iter.next();
      Assert.assertTrue("id", gAid.equals(subj.getId()));
      Assert.assertTrue("type", subj.getType().getName().equals("group"));
      Assert.assertTrue("name", subj.getName().equals("root:group a"));
    }
  }

  // By _name_, no results
  public void testSubjectInterfaceSearchByNameNil() {
    Set vals = SubjectFactory.search("r00t:group A");
    Assert.assertTrue("vals.size()==0", vals.size() == 0);
  }

  public void testGetAttributes() {
    try {
      Subject subj = SubjectFactory.getSubject(gAid, "group");
      Assert.assertTrue("loaded subject", true);
      Map attrs = subj.getAttributes();
      Assert.assertTrue("attrs=3", attrs.size() == 4);
      List keys = new ArrayList( attrs.keySet() );
      Collections.sort(keys);
      Assert.assertTrue("[0]=description", keys.get(0).equals("description"));
      Set vals = new HashSet();
      vals.add("this is group a");
      Assert.assertTrue(
        "[0] vals", attrs.get( keys.get(0)).equals(vals)
      );
      Assert.assertTrue("[1]=extension", keys.get(1).equals("extension"));
      vals = new HashSet();
      vals.add("group a");
      Assert.assertTrue(
        "[1] vals", attrs.get( keys.get(1)).equals(vals)
      );
      Assert.assertTrue("[2]=name", keys.get(2).equals("name"));
      vals = new HashSet();
      vals.add("root:group a");
      Assert.assertTrue(
        "[2] vals", attrs.get( keys.get(2)).equals(vals)
      );
      Assert.assertTrue("[3]=stem", keys.get(3).equals("stem"));
      vals = new HashSet();
      vals.add("root");
      Assert.assertTrue(
        "[3] vals", attrs.get( keys.get(3)).equals(vals)
      );
    } catch (SubjectNotFoundException e) {
      Assert.fail("unable to load subject");
    }
  }

  public void testGetAttributeValue() {
    try {
      Subject subj = SubjectFactory.getSubject(gAid, "group");
      Assert.assertTrue("loaded subject", true);
      String attr = "description";
      String val  = "this is group a";
      Assert.assertNotNull(attr, subj.getAttributeValue(attr));
      Assert.assertTrue(val, subj.getAttributeValue(attr).equals(val));
      attr  = "extension";
      val   = "group a";
      Assert.assertNotNull(attr, subj.getAttributeValue(attr));
      Assert.assertTrue(val, subj.getAttributeValue(attr).equals(val));
      attr  = "name";
      val   = "root:group a";
      Assert.assertNotNull(attr, subj.getAttributeValue(attr));
      Assert.assertTrue(val, subj.getAttributeValue(attr).equals(val));
      attr  = "stem";
      val   = "root";
      Assert.assertNotNull(attr, subj.getAttributeValue(attr));
      Assert.assertTrue(val, subj.getAttributeValue(attr).equals(val));
      attr  = "invalid";
      val   = new String();
      Assert.assertNotNull(attr, subj.getAttributeValue(attr));
      Assert.assertTrue(val, subj.getAttributeValue(attr).equals(val));
    } catch (SubjectNotFoundException e) {
      Assert.fail("unable to load subject");
    }
  }

  public void testGetAttributeValues() {
    try {
      Subject subj = SubjectFactory.getSubject(gAid, "group");
      Assert.assertTrue("loaded subject", true);
      String  attr = "description";
      Set     vals  = new HashSet();
      vals.add("this is group a");
      Assert.assertNotNull(attr, subj.getAttributeValues(attr));
      Assert.assertTrue(vals.toString(), subj.getAttributeValues(attr).equals(vals));
      attr  = "extension";
      vals  = new HashSet();
      vals.add("group a");
      Assert.assertNotNull(attr, subj.getAttributeValues(attr));
      Assert.assertTrue(vals.toString(), subj.getAttributeValues(attr).equals(vals));
      attr  = "name";
      vals  = new HashSet();
      vals.add("root:group a");
      Assert.assertNotNull(attr, subj.getAttributeValues(attr));
      Assert.assertTrue(vals.toString(), subj.getAttributeValues(attr).equals(vals));
      attr  = "stem";
      vals  = new HashSet();
      vals.add("root");
      Assert.assertNotNull(attr, subj.getAttributeValues(attr));
      Assert.assertTrue(vals.toString(), subj.getAttributeValues(attr).equals(vals));
      attr  = "invalid";
      vals  = new HashSet();
      Assert.assertNotNull(attr, subj.getAttributeValues(attr));
      Assert.assertTrue(vals.toString(), subj.getAttributeValues(attr).equals(vals));
    } catch (SubjectNotFoundException e) {
      Assert.fail("unable to load subject");
    }
  }

  public void testGetDescription() {
    try {
      Subject subj = SubjectFactory.getSubject(gAid, "group");
      Assert.assertTrue("loaded subject", true);
      Assert.assertNotNull("description", subj.getDescription());
      Assert.assertTrue(
        "description value", subj.getDescription().equals("this is group a")
      );
    } catch (SubjectNotFoundException e) {
      Assert.fail("unable to load subject");
    }
  }

  // TODO Test with null description
}

