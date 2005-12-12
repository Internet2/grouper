/*
  Copyright 2004-2005 University Corporation for Advanced Internet Development, Inc.
  Copyright 2004-2005 The University Of Chicago

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
*/

package test.edu.internet2.middleware.grouper;


import  edu.internet2.middleware.grouper.*;
import  edu.internet2.middleware.subject.*;
import  edu.internet2.middleware.subject.provider.*;
import  java.util.*;
import  junit.framework.*;
import  org.apache.commons.logging.*;


/**
 * Test {@link Group}.
 * <p />
 * @author  blair christensen.
 * @version $Id: TestGroup.java,v 1.4 2005-12-12 19:54:04 blair Exp $
 */
public class TestGroup extends TestCase {

  // Private Class Constants
  private static final Log LOG = LogFactory.getLog(TestGroup.class);


  // Private Class Variables
  private static Stem           edu;
  private static Group          i2;
  private static Stem           root;
  private static GrouperSession s; 

  
  public TestGroup(String name) {
    super(name);
  }

  protected void setUp () {
    LOG.debug("setUp");
    RegistryReset.resetRegistryAndAddTestSubjects();
    s     = SessionHelper.getRootSession();
    root  = StemHelper.findRootStem(s);
    edu   = StemHelper.addChildStem(root, "edu", "education");
    i2    = StemHelper.addChildGroup(edu, "i2", "internet2");
  }

  protected void tearDown () {
    LOG.debug("tearDown");
  }

  // Tests

  public void testGetParentStem() {
    LOG.info("testGetParentStem");
    Stem parent = i2.getParentStem();
    Assert.assertNotNull("group has parent", parent);
    Assert.assertTrue("parent == edu", parent.equals(edu));
    Assert.assertTrue(
      "root has STEM on parent", parent.hasStem(s.getSubject())
    );
  } // public void testGetParentStem()

  public void testGetTypes() {
    LOG.info("testGetTypes");
    Set types = i2.getTypes();
    Assert.assertTrue("has 1 type/" + types.size(), types.size() == 1);
    Iterator iter = types.iterator();
    while (iter.hasNext()) {
      GroupType type = (GroupType) iter.next();
      Assert.assertNotNull("type !null", type);
      Assert.assertTrue("type instanceof GroupType", type instanceof GroupType);
      Assert.assertTrue("type name == base", type.getName().equals("base"));
      Set fields = type.getFields();
      Assert.assertTrue(
        "type has 12 fields/" + fields.size(), fields.size() == 12
      );
      Iterator  fIter = fields.iterator();
      FieldHelper.testField( 
        (Field) fIter.next()   , 
        "admins"              , FieldType.ACCESS,
        AccessPrivilege.ADMIN , AccessPrivilege.ADMIN
      );
      FieldHelper.testField( 
        (Field) fIter.next()   , 
        "description"         , FieldType.ATTRIBUTE,
        AccessPrivilege.READ  , AccessPrivilege.ADMIN
      );
      FieldHelper.testField( 
        (Field) fIter.next()   , 
        "displayExtension"    , FieldType.ATTRIBUTE,
        AccessPrivilege.VIEW  , AccessPrivilege.ADMIN
      );
      FieldHelper.testField( 
        (Field) fIter.next()   , 
        "displayName"         , FieldType.ATTRIBUTE,
        AccessPrivilege.VIEW  , AccessPrivilege.SYSTEM
      );
      FieldHelper.testField( 
        (Field) fIter.next()   , 
        "extension"           , FieldType.ATTRIBUTE,
        AccessPrivilege.VIEW, AccessPrivilege.ADMIN
      );
      FieldHelper.testField( 
        (Field) fIter.next()   , 
        "members"             , FieldType.LIST,
        AccessPrivilege.READ  , AccessPrivilege.UPDATE
      );
      FieldHelper.testField( 
        (Field) fIter.next()   , 
        "name"                , FieldType.ATTRIBUTE,
        AccessPrivilege.VIEW  , AccessPrivilege.SYSTEM
      );
      FieldHelper.testField( 
        (Field) fIter.next()   , 
        "optins"              , FieldType.ACCESS,
        AccessPrivilege.UPDATE, AccessPrivilege.UPDATE
      );
      FieldHelper.testField( 
        (Field) fIter.next()   , 
        "optouts"             , FieldType.ACCESS,
        AccessPrivilege.UPDATE, AccessPrivilege.UPDATE
      );
      FieldHelper.testField( 
        (Field) fIter.next()   , 
        "readers"             , FieldType.ACCESS,
        AccessPrivilege.ADMIN , AccessPrivilege.ADMIN
      );
      FieldHelper.testField( 
        (Field) fIter.next()   , 
        "updaters"            , FieldType.ACCESS,
        AccessPrivilege.ADMIN , AccessPrivilege.ADMIN
      );
      FieldHelper.testField( 
        (Field) fIter.next()   , 
        "viewers"             , FieldType.ACCESS,
        AccessPrivilege.ADMIN , AccessPrivilege.ADMIN
      );
    }
  } // public void testGetTypes()

  public void testAddChildGroupWithBadExtnOrDisplayExtn() {
    LOG.info("testAddChildGroupWithBadExtnOrDisplayExtn");
    try {
      try {
        Group badE = edu.addChildGroup(null, "test");
        Assert.fail("added group with null extn");
      }
      catch (GroupAddException eSA) {
        Assert.assertTrue("null extn", true);
      }
      try {
        Group badE = edu.addChildGroup("", "test");
        Assert.fail("added group with empty extn");
      }
      catch (GroupAddException eSA) {
        Assert.assertTrue("empty extn", true);
      }
      try {
        Group badE = edu.addChildGroup("a:test", "test");
        Assert.fail("added group with colon-containing extn");
      }
      catch (GroupAddException eSA) {
        Assert.assertTrue("colon-containing extn", true);
      }
      try {
        Group badE = edu.addChildGroup("test", null);
        Assert.fail("added group with null displayExtn");
      }
      catch (GroupAddException eSA) {
        Assert.assertTrue("null displayExtn", true);
      }
      try {
        Group badE = edu.addChildGroup("test", "");
        Assert.fail("added group with empty displayextn");
      }
      catch (GroupAddException eSA) {
        Assert.assertTrue("empty displayExtn", true);
      }
      try {
        Group badE = edu.addChildGroup("test", "a:test");
        Assert.fail("added group with colon-containing displayExtn");
      }
      catch (GroupAddException eSA) {
        Assert.assertTrue("colon-containing displayExtn", true);
      }
    }
    catch (Exception e) {
      Assert.fail(e.getMessage());
    }
  } // public void testAddChildGroupWithBadExtnOrDisplayExtn()

  public void testSetBadGroupExtension() {
    LOG.info("testSetBadGroupExtension");
    try {
      try {
        i2.setExtension(null);
        Assert.fail("set null extn");
      }
      catch (GroupModifyException eSA) {
        Assert.assertTrue("null extn", true);
      }
      try {
        i2.setExtension("");
        Assert.fail("set empty extn");
      }
      catch (GroupModifyException eSA) {
        Assert.assertTrue("empty extn", true);
      }
      try {
        i2.setExtension("a:test");
        Assert.fail("set colon-containing extn");
      }
      catch (GroupModifyException eSA) {
        Assert.assertTrue("colon-containing extn", true);
      }
    }
    catch (Exception e) {
      Assert.fail(e.getMessage());
    }
  } // public void testSetBadGroupExtension()

  public void testSetBadGroupDisplayExtension() {
    LOG.info("testSetBadGroupDisplayExtension");
    try {
      try {
        i2.setDisplayExtension(null);
        Assert.fail("set null displayExtn");
      }
      catch (GroupModifyException eSA) {
        Assert.assertTrue("null displayExtn", true);
      }
      try {
        i2.setDisplayExtension("");
        Assert.fail("set empty displayExtn");
      }
      catch (GroupModifyException eSA) {
        Assert.assertTrue("empty displayExtn", true);
      }
      try {
        i2.setDisplayExtension("a:test");
        Assert.fail("set colon-containing displayExtn");
      }
      catch (GroupModifyException eSA) {
        Assert.assertTrue("colon-containing displayExtn", true);
      }
    }
    catch (Exception e) {
      Assert.fail(e.getMessage());
    }
  } // public void testSetBadGroupDisplayExtension()

}

