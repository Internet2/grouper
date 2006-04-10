/*
  Copyright 2004-2006 University Corporation for Advanced Internet Development, Inc.
  Copyright 2004-2006 The University Of Chicago

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

package edu.internet2.middleware.grouper;


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
 * @version $Id: TestGroup.java,v 1.1.2.1 2006-04-10 19:07:20 blair Exp $
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

  public void testGetAndHasPrivs() {
    LOG.info("testGetAndHasPrivs");
    try {
      Subject         subj  = SubjectTestHelper.SUBJ0;
      Subject         subj1 = SubjectTestHelper.SUBJ1;
      Subject         all   = SubjectTestHelper.SUBJA;
      GrouperSession  s     = SessionHelper.getRootSession();
      Group           uofc  = edu.addChildGroup("uofc", "uofc");
      GroupHelper.addMember(uofc, subj, "members");
      GroupHelper.addMember(i2, uofc.toSubject(), "members");
      Member          m     = MemberFinder.findBySubject(s, subj);
      PrivHelper.grantPriv(s, i2,   all,  AccessPrivilege.OPTIN);
      PrivHelper.grantPriv(s, uofc, subj, AccessPrivilege.UPDATE);

      // Get access privs
      Assert.assertTrue("admins/i2      == 1",  i2.getAdmins().size()   == 1);
      Assert.assertTrue("optins/i2      == 1",  i2.getOptins().size()   == 1);
      Assert.assertTrue("optouts/i2     == 0",  i2.getOptouts().size()  == 0);
      Assert.assertTrue("readers/i2     == 1",  i2.getReaders().size()  == 1);
      Assert.assertTrue("updaters/i2    == 0",  i2.getUpdaters().size() == 0);
      Assert.assertTrue("viewers/i2     == 1",  i2.getViewers().size()  == 1);

      Assert.assertTrue("admins/uofc    == 1",  uofc.getAdmins().size()   == 1);
      Assert.assertTrue("optins/uofc    == 0",  uofc.getOptins().size()   == 0);
      Assert.assertTrue("optouts/uofc   == 0",  uofc.getOptouts().size()  == 0);
      Assert.assertTrue("readers/uofc   == 1",  uofc.getReaders().size()  == 1);
      Assert.assertTrue("updaters/uofc  == 1",  uofc.getUpdaters().size() == 1);
      Assert.assertTrue("viewers/uofc   == 1",  uofc.getViewers().size()  == 1);

      // Has access privs
      Assert.assertTrue("admin/i2/subj0",     !i2.hasAdmin(subj)      );
      Assert.assertTrue("admin/i2/subj1",     !i2.hasAdmin(subj1)     );
      Assert.assertTrue("admin/i2/subjA",     !i2.hasAdmin(all)       );

      Assert.assertTrue("optin/i2/subj0",     i2.hasOptin(subj)       );
      Assert.assertTrue("optin/i2/subj1",     i2.hasOptin(subj1)      );
      Assert.assertTrue("optin/i2/subjA",     i2.hasOptin(all)        );

      Assert.assertTrue("optout/i2/subj0",    !i2.hasOptout(subj)     );
      Assert.assertTrue("optout/i2/subj1",    !i2.hasOptout(subj1)    );
      Assert.assertTrue("optout/i2/subjA",    !i2.hasOptout(all)      );

      Assert.assertTrue("read/i2/subj0",      i2.hasRead(subj)        );
      Assert.assertTrue("read/i2/subj1",      i2.hasRead(subj1)       );
      Assert.assertTrue("read/i2/subjA",      i2.hasRead(all)         );

      Assert.assertTrue("update/i2/subj0",    !i2.hasUpdate(subj)     );
      Assert.assertTrue("update/i2/subj1",    !i2.hasUpdate(subj1)    );
      Assert.assertTrue("update/i2/subjA",    !i2.hasUpdate(all)      );

      Assert.assertTrue("view/i2/subj0",      i2.hasView(subj)        );
      Assert.assertTrue("view/i2/subj1",      i2.hasView(subj1)       );
      Assert.assertTrue("view/i2/subjA",      i2.hasView(all)         );

      Assert.assertTrue("admin/uofc/subj0",   !uofc.hasAdmin(subj)    );
      Assert.assertTrue("admin/uofc/subj1",   !uofc.hasAdmin(subj1)   );
      Assert.assertTrue("admin/uofc/subjA",   !uofc.hasAdmin(all)     );

      Assert.assertTrue("optin/uofc/subj0",   !uofc.hasOptin(subj)    );
      Assert.assertTrue("optin/uofc/subj1",   !uofc.hasOptin(subj1)   );
      Assert.assertTrue("optin/uofc/subjA",   !uofc.hasOptin(all)     );

      Assert.assertTrue("optout/uofc/subj0",  !uofc.hasOptout(subj)   );
      Assert.assertTrue("optout/uofc/subj1",  !uofc.hasOptout(subj1)  );
      Assert.assertTrue("optout/uofc/subjA",  !uofc.hasOptout(all)    );

      Assert.assertTrue("read/uofc/subj0",    uofc.hasRead(subj)      );
      Assert.assertTrue("read/uofc/subj1",    uofc.hasRead(subj1)     );
      Assert.assertTrue("read/uofc/subjA",    uofc.hasRead(all)       );

      Assert.assertTrue("update/uofc/subj0",  uofc.hasUpdate(subj)    );
      Assert.assertTrue("update/uofc/subj1",  !uofc.hasUpdate(subj1)  );
      Assert.assertTrue("update/uofc/subjA",  !uofc.hasUpdate(all)    );

      Assert.assertTrue("view/uofc/subj0",    uofc.hasView(subj)      );
      Assert.assertTrue("view/uofc/subj1",    uofc.hasView(subj1)     );
      Assert.assertTrue("view/uofc/subjA",    uofc.hasView(all)       );

      s.stop();
    }
    catch (Exception e) {
      Assert.fail(e.getMessage());
    }
  } // public void testGetAndHasPrivs()

  public void testSetDescription() {
    LOG.info("testSetDescription");
    try {
      GrouperSession  s     = SessionHelper.getRootSession();
      String          orig  = i2.getDescription(); 
      String          set   = "this is a group"; 
      i2.setDescription(set);
      Assert.assertTrue("!orig",  !i2.getDescription().equals(orig));
      Assert.assertTrue("set",    i2.getDescription().equals(set));
      s.stop();
    }
    catch (Exception e) {
      Assert.fail(e.getMessage());
    }
  } // public void testSetDescription()

}

