/*******************************************************************************
 * Copyright 2012 Internet2
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
/*
  Copyright (C) 2004-2007 University Corporation for Advanced Internet Development, Inc.
  Copyright (C) 2004-2007 The University Of Chicago

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
import java.util.Set;

import junit.framework.Assert;
import junit.textui.TestRunner;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.exception.AttributeNotFoundException;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.exception.SchemaException;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.R;
import edu.internet2.middleware.grouper.helper.SessionHelper;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.helper.T;
import edu.internet2.middleware.grouper.misc.E;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
import edu.internet2.middleware.grouper.privs.Privilege;
import edu.internet2.middleware.grouper.registry.RegistryReset;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * @author  blair christensen.
 * @version $Id: TestGroupType.java,v 1.3 2009-03-24 17:12:09 mchyzer Exp $
 */
public class TestGroupType extends GrouperTest {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new TestGroupType("testXmlInsert"));
  }
  
  // Private Static Class Constants
  private static final Log LOG = GrouperUtil.getLog(TestGroupType.class);

  public TestGroupType(String name) {
    super(name);
  }

  public void testFindAllTypes() {
    LOG.info("testFindAllTypes");
    Set types = GroupTypeFinder.findAll();    
    T.amount("public group types", 1, types.size());
  } // public void testFindAllTypes()

  public void testAddAndDeleteCustomTypeAsNonRoot() {
    LOG.info("testAddAndDeleteCustomTypeAsNonRoot");
    try {
      R r = R.populateRegistry(1, 1, 1);
  
      String    tName = "test type";
      // Create the custom type as root
      GroupType type  = GroupType.createType(r.rs, tName);
  
      // Now allow a non-root subj to admin this group
      Group   g     = r.getGroup("a", "a");
      Subject subj  = r.getSubject("a");
      g.grantPriv(subj, AccessPrivilege.ADMIN);
  
      // Now start non-root session and add+delete group type as non-root
      GrouperSession nrs = GrouperSession.start(subj);
      Assert.assertFalse( "no custom type"      , g.hasType(type) );
      g.addType(type);
      Assert.assertTrue(  "now has custom type" , g.hasType(type) );
      g.deleteType(type);
      Assert.assertFalse( "custom type removed" , g.hasType(type) );
      nrs.stop();
  
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  }

  public void testDeleteType2() {
    LOG.info("testDeleteType");
    try {
      R               r       = R.populateRegistry(0, 0, 0);
      GroupType       custom  = GroupType.createType(r.rs, "custom");
      Field           customA = custom.addAttribute(
        r.rs, "custom a", AccessPrivilege.ADMIN, AccessPrivilege.ADMIN, false
      );
      Field           customL = custom.addList(
        r.rs, "custom l", AccessPrivilege.ADMIN, AccessPrivilege.ADMIN
      );
      custom.delete(r.rs);
      try {
        GroupTypeFinder.find(custom.getName(), true);
        Assert.fail("FAIL: found deleted type");
      }
      catch (SchemaException eS) {
        Assert.assertTrue("OK: deleted type not found", true);
      }
      try {
        FieldFinder.find(customA.getName(), true);
        Assert.fail("FAIL: found deleted attribute");
      }
      catch (SchemaException eS) {
        Assert.assertTrue("OK: deleted attribute not found", true);
      }
      try {
        FieldFinder.find(customL.getName(), true);
        Assert.fail("FAIL: found deleted list");
      }
      catch (SchemaException eS) {
        Assert.assertTrue("OK: deleted list not found", true);
      }
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testDeleteType()

  public void testDeleteType() {
    LOG.info("testDeleteType");
    try {
      R               r       = R.populateRegistry(0, 0, 0);
      GroupType       custom  = GroupType.createType(r.rs, "custom");
      custom.delete(r.rs);
      try {
        GroupTypeFinder.find(custom.getName(), true);
        Assert.fail("FAIL: found deleted type");
      }
      catch (SchemaException eS) {
        Assert.assertTrue("OK: deleted type not found", true);
      }
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testDeleteType()

  public void testFailToAddFieldToBaseAsNonRoot() {
    LOG.info("testFailToAddFieldToBaseAsNonRoot");
    try {
      R               r     = R.populateRegistry(1, 1, 1);
      GroupType       base  = GroupTypeFinder.find("base", true);
      Subject         subj  = r.getSubject("a");
      GrouperSession  s     = GrouperSession.start(subj);
      try {
        base.addList(s, "test", AccessPrivilege.VIEW, AccessPrivilege.UPDATE);
        Assert.fail("added field to base as non-root");
      }
      catch (InsufficientPrivilegeException eIP) {
        Assert.assertTrue("OK: not privileged to add field", true);
      }
      finally {
        s.stop();
        r.rs.stop();
      }
    }
    catch (Exception e) {
      Assert.fail("unexpected exception: " + e.getMessage());
    }
  } // public void testFailToAddFieldToBaseAsNonRoot()

  public void testFailToDeleteWhenInUse() {
    LOG.info("testFailToDeleteWhenInUse");
    try {
      R               r       = R.populateRegistry(1, 1, 0);
      GroupType       custom  = GroupType.createType(r.rs, "custom");
      Group           gA      = r.getGroup("a", "a");
      gA.addType(custom);
      try {
        custom.delete(r.rs);
        Assert.fail("deleted in use type");
      }
      catch (SchemaException eS) {
        assertContains(
          "OK: failed to delete in use type",
          eS.getMessage(), 
          E.GROUPTYPE_DELINUSE
        );
      } 
      finally {
        r.rs.stop();
      }
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testFailToDeleteWhenInUse()

  public void testFailToDeleteWhenNotRoot() {
    LOG.info("testFailToDeleteWhenNotRoot");
    try {
      R               r       = R.populateRegistry(0, 0, 1);
      GroupType       custom  = GroupType.createType(r.rs, "custom");
      GrouperSession  s       = GrouperSession.start( r.getSubject("a"));
      try {
        custom.delete(s);
        Assert.fail("deleted group type as !root");
      }
      catch (InsufficientPrivilegeException eIP) {
        assertTrue(GrouperUtil.getFullStackTrace(eIP), eIP.getMessage().contains(E.GROUPTYPE_NODEL));
      } 
      finally {
        s.stop();
        r.rs.stop();
      }
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testFailToDeleteWhenNotRoot()

  public void testFailToDeleteWhenSystemType() {
    LOG.info("testFailToDeleteWhenSystemType");
    try {
      R               r       = R.populateRegistry(0, 0, 0);
      GroupType       base    = GroupTypeFinder.find("base", true);
      try {
        base.delete(r.rs);
        Assert.fail("deleted system type");
      }
      catch (SchemaException eS) {
        assertTrue(GrouperUtil.getFullStackTrace(eS), eS.getMessage().contains(E.GROUPTYPE_NODELSYS));
      } 
      finally {
        r.rs.stop();
      }
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testFailToDeleteWhenSystemType() 

  public void testFindAllAssignableTypes() {
    LOG.info("testFindAllAssignableTypes");
    Set types = GroupTypeFinder.findAllAssignable();    
    T.amount("public group types", 0, types.size());
  } // public void testFindAllAssignableTypes()

  public void testFindAllAssignableTypesAfterAddition() {
    LOG.info("testFindAllAssignableTypesAfterAddition");
    Set types = GroupTypeFinder.findAllAssignable();    
    T.amount("assignable group types before addition", 0, types.size());
    GrouperSession s = null;
    try {
      String    name  = "test";
      s               = SessionHelper.getRootSession();
      GroupType type  = GroupType.createType(s, name);
      Assert.assertTrue("added type: " + type, true);
      types = GroupTypeFinder.findAllAssignable();
      T.amount("assignable group types after addition", 1, types.size());
    }
    catch (Exception e) {
      Assert.fail(e.getMessage());
    }
    finally {
      SessionHelper.stop(s);
    }
  } // public void testFindAllAssignableTypesAfterAddition()

  /**
   * 
   */
  public void testFindAllTypesAfterAddition() {
    LOG.info("testFindAllTypesAfterAddition");
    Set types = GroupTypeFinder.findAll();    
    T.amount("public group types before addition", 1, types.size());
    GrouperSession s = null;
    try {
      String    name  = "test";
      s               = SessionHelper.getRootSession();
      GroupType type  = GroupType.createType(s, name);
      Assert.assertTrue("added type: " + type, true);
      types = GroupTypeFinder.findAll();
      T.amount("public group types after addition", 2, types.size());
    }
    catch (Exception e) {
      Assert.fail(e.getMessage());
    }
    finally {
      SessionHelper.stop(s);
    }
  }

  public void testUseCustomAttributeAsNonRoot() {
    LOG.info("testUseCustomAttributeAsNonRoot");
    try {
      R         r       = R.populateRegistry(1, 1, 1);
      Group     gA      = r.getGroup("a", "a");
      String    name    = gA.getName();
      Subject   subjA   = r.getSubject("a");
      GroupType custom  = GroupType.createType(r.rs, "custom");
      Field     attr    = custom.addAttribute(
        r.rs, "custom a", AccessPrivilege.ADMIN, AccessPrivilege.ADMIN, false
      );
      gA.addType(custom);
      gA.grantPriv(subjA, AccessPrivilege.ADMIN);
      r.rs.stop();
  
      // Now test-and-set attribute as !root
      GrouperSession  s = GrouperSession.start(subjA);
      Group           g = GroupFinder.findByName(s, name, true);
      Assert.assertTrue(
        "group has custom type", g.hasType(custom)
      );         
      Assert.assertTrue(
        "group does not have attribute set - yet",
        g.getAttributeValue(attr.getName(), false, false).equals(GrouperConfig.EMPTY_STRING)
      );
      try {
        g.setAttribute(attr.getName(), name);

        Assert.assertTrue("set attribute", true);
      }
      catch (Exception e) {
        Assert.fail("exception while setting custom attribute! - " + e.getMessage());
      }
      T.string("now group has attribute set", name, g.getAttributeValue(attr.getName(), false, true));
      s.stop();
  
      // Now make sure it was properly persisted
      GrouperSession  S = GrouperSession.start(subjA);
      Group           G = GroupFinder.findByName(S, name, true);
      T.string("attribute was persisted", name, G.getAttributeValue(attr.getName(), false, true));
      S.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testUseCustomAttributeAsNonRoot()

  public void testAddBaseType() {
    GrouperSession  s = null;
    try {
      s = SessionHelper.getRootSession();
      GroupType type = GroupTypeFinder.find("base", true);
      Stem  root  = StemFinder.findRootStem(s);
      Stem  edu   = root.addChildStem("edu", "edu");
      Group g     = edu.addChildGroup("g", "g");
      try {
        g.addType(type);
        Assert.fail("added base type");
      }
      catch (Exception e) {
        Assert.assertTrue("cannot add base type", true);
      }
    }
    catch (Exception e) {
      Assert.fail("unexpected exception: " + e.getMessage());
    }
    finally {
      SessionHelper.stop(s);
    }
  } // public void testAddBaseType()

  public void testAddExistingField() {
    GrouperSession  s     = null;
    String          type  = "base";
    String          name  = "members";
    Privilege       read  = AccessPrivilege.VIEW;
    Privilege       write = AccessPrivilege.UPDATE;
    try {
      GroupType base = GroupTypeFinder.find(type, true);
      s = SessionHelper.getRootSession();
      base.addList(s, name, read, write);
      Assert.fail("added field to base type"); 
    }
    catch (InsufficientPrivilegeException eIP) {
      Assert.assertTrue("cannot modify fields on system types", true);
    }
    catch (Exception e) {
      T.e(e);
    }
    finally {
      SessionHelper.stop(s);
    }
  } // public void testAddExistingField()

  /**
   * 
   */
  public void testAddFieldAttribute() {
    GrouperSession  s     = null;
    String          type  = "customType.0";
    String          name  = "customField";
    Privilege       read  = AccessPrivilege.VIEW;
    Privilege       write = AccessPrivilege.UPDATE;
    boolean         req   = false;
    s = SessionHelper.getRootSession();
    GroupType custom = GroupType.createType(s, type);
    custom.addAttribute(s, name, read, write, req);
    Assert.assertTrue("added ATTRIBUTE field", true);

    //try to add an attribute with a field name... it should not be allowed
    try {
      custom.addAttribute(s, Group.FIELD_DESCRIPTION, read, write, req);
      fail("Shouldnt be able to create a built in field attribute");
    } catch (Exception e) {
      //good
    }
    
    try {
      custom.addAttribute(s, Group.FIELD_EXTENSION, read, write, req);
      fail("Shouldnt be able to create a built in field attribute");
    } catch (Exception e) {
      //good
    }
    
    try {
      custom.addAttribute(s, Group.FIELD_DISPLAY_EXTENSION, read, write, req);
      fail("Shouldnt be able to create a built in field attribute");
    } catch (Exception e) {
      //good
    }
    
    try {
      custom.addAttribute(s, Group.FIELD_NAME, read, write, req);
      fail("Shouldnt be able to create a built in field attribute");
    } catch (Exception e) {
      //good
    }
    
    try {
      custom.addAttribute(s, Group.FIELD_DISPLAY_NAME, read, write, req);
      fail("Shouldnt be able to create a built in field attribute");
    } catch (Exception e) {
      //good
    }
    
    
    SessionHelper.stop(s);
  } // public void testAddFieldAttribute()

  public void testAddFieldDuplicateName() {
    GrouperSession  s     = null;
    String          type  = "customType.2";
    String          name  = "customField";
    Privilege       read  = AccessPrivilege.VIEW;
    Privilege       write = AccessPrivilege.UPDATE;
    boolean         req   = false;
    try {
      s = SessionHelper.getRootSession();
      GroupType custom = GroupType.createType(s, type);
      custom.addList(s, name, read, write);
      Assert.assertTrue("added LIST field", true);
      try {
        custom.addAttribute(s, name, read, write, req);
        Assert.fail("added duplicate field name");
      }
      catch (SchemaException eS) {
        Assert.assertTrue("cannot add duplicate field name", true);
      }
    }
    catch (InsufficientPrivilegeException eIP) {
      Assert.fail("unexpected exception: " + eIP.getMessage());
    }
    catch (SchemaException eS) {
      Assert.fail("unexpected exception: " + eS.getMessage());
    }
    finally {
      SessionHelper.stop(s);
    }
  } // public void testAddFieldDuplicateName()

  public void testAddFieldList() {
    GrouperSession  s     = null;
    String          type  = "customType.1";
    String          name  = "customField";
    Privilege       read  = AccessPrivilege.VIEW;
    Privilege       write = AccessPrivilege.UPDATE;
    try {
      s = SessionHelper.getRootSession();
      GroupType custom = GroupType.createType(s, type);
      custom.addList(s, name, read, write);
      Assert.assertTrue("added LIST field", true);
    }
    catch (InsufficientPrivilegeException eIP) {
      Assert.fail("unexpected exception: " + eIP.getMessage());
    }
    catch (SchemaException eS) {
      Assert.fail("unexpected exception: " + eS.getMessage());
    }
    finally {
      SessionHelper.stop(s);
    }
  } // public void testAddFieldList()

  public void testAddFieldReadNotAccess() {
    GrouperSession  s     = null;
    String          type  = "customType";
    String          name  = "customField";
    Privilege       read  = NamingPrivilege.CREATE;
    Privilege       write = AccessPrivilege.UPDATE;
    try {
      s = SessionHelper.getRootSession();
      GroupType custom = GroupType.createType(s, type);
      custom.addList(s, name, read, write);
      Assert.fail("added field with !ACCESS read priv");
    }
    catch (InsufficientPrivilegeException eIP) {
      Assert.fail("unexpected exception: " + eIP.getMessage());
    }
    catch (SchemaException eS) {
      Assert.assertTrue("read priv must be ACCESS", true);
    }
    finally {
      SessionHelper.stop(s);
    }
  } // public void testAddFieldReadNotAccess()

  public void testAddFieldToBase() {
    GrouperSession  s     = null;
    String          type  = "base";
    String          name  = "customField";
    Privilege       read  = AccessPrivilege.VIEW;
    Privilege       write = AccessPrivilege.UPDATE;
    try {
      GroupType base = GroupTypeFinder.find(type, true);
      s = SessionHelper.getRootSession();
      base.addList(s, name, read, write);
      Assert.fail("added field to base type"); 
    }
    catch (InsufficientPrivilegeException eIP) {
      Assert.assertTrue("cannot modify fields on system types", true);
    }
    catch (Exception e) {
      T.e(e);
    }
    finally {
      SessionHelper.stop(s);
    }
  } // public void testAddFieldToBase()

  public void testAddFieldToNaming() {
    GrouperSession  s     = null;
    String          type  = "naming";
    String          name  = "customField";
    Privilege       read  = AccessPrivilege.VIEW;
    Privilege       write = AccessPrivilege.UPDATE;
    try {
      GroupType base = GroupTypeFinder.find(type, true);
      s = SessionHelper.getRootSession();
      base.addList(s, name, read, write);
      Assert.fail("added field to naming type"); 
    }
    catch (InsufficientPrivilegeException eIP) {
      Assert.assertTrue("cannot modify fields on system types", true);
    }
    catch (Exception e) {
      T.e(e);
    }
    finally {
      SessionHelper.stop(s);
    }
  } // public void testAddFieldToNaming()

  public void testAddFieldWriteNotAccess() {
    GrouperSession  s     = null;
    String          type  = "customType";
    String          name  = "customField";
    Privilege       read  = AccessPrivilege.VIEW;
    Privilege       write = NamingPrivilege.STEM;
    try {
      s = SessionHelper.getRootSession();
      GroupType custom = GroupType.createType(s, type);
      custom.addList(s, name, read, write);
      Assert.fail("added field with !ACCESS write priv");
    }
    catch (InsufficientPrivilegeException eIP) {
      Assert.fail("unexpected exception: " + eIP.getMessage());
    }
    catch (SchemaException eS) {
      Assert.assertTrue("write priv must be ACCESS", true);
    }
    finally {
      SessionHelper.stop(s);
    }
  } // public void testAddFieldWriteNotAccess()

  public void testAddNamingType() {
    GrouperSession  s = null;
    try {
      s = SessionHelper.getRootSession();
      GroupType type = GroupTypeFinder.find("naming", true);
      Stem  root  = StemFinder.findRootStem(s);
      Stem  edu   = root.addChildStem("edu", "edu");
      Group g     = edu.addChildGroup("g", "g");
      try {
        g.addType(type);
        Assert.fail("added naming type");
      }
      catch (Exception e) {
        Assert.assertTrue("cannot add naming type", true);
      }
    }
    catch (Exception e) {
      Assert.fail("unexpected exception: " + e.getMessage());
    }
    finally {
      SessionHelper.stop(s);
    }
  } // public void testAddNamingType()

  /**
   * 
   */
  public void testAddRemoveType() {
    GrouperSession  s     = null;
    String          type  = "customType.1a";
    String          name  = "customField1a";
    Privilege       read  = AccessPrivilege.VIEW;
    Privilege       write = AccessPrivilege.UPDATE;
    try {
      s = SessionHelper.getRootSession();
      GroupType custom = GroupType.createType(s, type);
      custom.addAttribute(s, name, read, write, false);
      Group group = Group.saveGroup(s, null, null, "test:test1", "test1", "test1", null, true);
      group.addType(custom);
      group.setAttribute(name, "theTest");
      group.deleteType(custom);
      group.addType(custom);
      String value = group.getAttributeValue(name, false, false);
      assertTrue(value, StringUtils.isBlank(value));
    } catch (Exception e) {
      throw new RuntimeException(e);
    } finally {
      SessionHelper.stop(s);
    }
  
  }

  // Tests
  
  public void testCreateExistingType() {
    GrouperSession  s     = null;
    String          name  = "base";
    try {
      s               = SessionHelper.getRootSession();
      GroupType.createType(s, name);
      Assert.fail("created existing type: " + name);
    }
    catch (InsufficientPrivilegeException eIP) {
      Assert.fail(eIP.getMessage());
    }
    catch (SchemaException eS) {
      Assert.assertTrue("did not create existing type: " + name, true);
    }
    finally {
      SessionHelper.stop(s);
    }
  } // public void testCreateExistingType()

  public void testCreateNewTypeAsNonRoot() {
    GrouperSession  s     = null;
    String          name  = "customType";
    try {
      s               = SessionHelper.getSession(SubjectTestHelper.SUBJ0_ID);
      GroupType.createType(s, name);
      Assert.fail("created custom type as non-root: " + name);
    }
    catch (InsufficientPrivilegeException eIP) {
      Assert.assertTrue("did not create custom type as non-root: " + name, true);
    }
    catch (SchemaException eS) {
      Assert.fail(
        "did not create custom type as non-root: " + name + ": " + eS.getMessage()
      );
    }
    finally {
      SessionHelper.stop(s);      
    }
  } // public void testCreateNewTypeAsNonRoot()

  public void testCreateNewTypeAsRoot() {
    GrouperSession  s     = null;
    String          name  = "customType";
    try {
      s               = SessionHelper.getRootSession();
      GroupType.createType(s, name);
      Assert.assertTrue("created custom type: " + name, true);
    }
    catch (InsufficientPrivilegeException eIP) {
      Assert.fail(eIP.getMessage());
    }
    catch (SchemaException eS) {
      Assert.fail("failed to create custom type: " + name + ": " + eS.getMessage());
    }
    finally {
      SessionHelper.stop(s);      
    }
  } // public void testCreateNewTypeAsRoot() 

  /**
   * 
   */
  public void testDeleteAsNonRoot() {
    GrouperSession  s     = null;
    String          type  = "customType.TDANR";
    String          name  = "customField.TDANR";
    Privilege       read  = AccessPrivilege.VIEW;
    Privilege       write = AccessPrivilege.UPDATE;
    boolean         req   = false;
    try {
      s = SessionHelper.getRootSession();
      GroupType custom = GroupType.createType(s, type);
      custom.addAttribute(s, name, read, write, req);
      Assert.assertTrue("added ATTRIBUTE field", true);
      GrouperSession nrs = SessionHelper.getSession(SubjectTestHelper.SUBJ0_ID);
      try {
        custom.deleteField(nrs, name);
        Assert.fail("deleted as !root");
      }
      catch (Exception e) {
        Assert.assertTrue("could not delete as !root", true);
      }
      finally {
        nrs.stop();
      }
    }
    catch (Exception e) {
      Assert.fail("unexpected exception: " + e.getMessage());
    }
    finally {
      SessionHelper.stop(s);
    }
  }

  /**
   * 
   */
  public void testDeleteFromBase() {
    GrouperSession  s     = null;
    String          type  = "base";
    String          name  = "description";
    try {
      s = SessionHelper.getRootSession();
      GroupType base = GroupTypeFinder.find(type, true);
      try {
        base.deleteField(s, name);  
        Assert.fail("deleted field from BASE");
      }
      catch (Exception e) {
        Assert.assertTrue("could not delete attr", true);
      }
    }
    catch (Exception e) {
      Assert.fail("unexpected exception: " + e.getMessage());
    }
    finally {
      SessionHelper.stop(s);
    }
  }

  /**
   * 
   */
  public void testDeleteFromNaming() {
    GrouperSession  s     = null;
    String          type  = "naming";
    String          name  = Field.FIELD_NAME_CREATORS;
    try {
      s = SessionHelper.getRootSession();
      GroupType naming = GroupTypeFinder.find(type, true);
      try {
        naming.deleteField(s, name);  
        Assert.fail("deleted field from NAMING");
      }
      catch (Exception e) {
        Assert.assertTrue("could not delete attr", true);
      }
    }
    catch (Exception e) {
      Assert.fail("unexpected exception: " + e.getMessage());
    }
    finally {
      SessionHelper.stop(s);
    }
  }

  /**
   * 
   */
  public void testDeleteUnusedCustomAttribute() {
    GrouperSession  s     = null;
    String          type  = "customType.TDUCA";
    String          name  = "customField.TDUCA";
    Privilege       read  = AccessPrivilege.VIEW;
    Privilege       write = AccessPrivilege.UPDATE;
    boolean         req   = false;
    try {
      s = SessionHelper.getRootSession();
      GroupType custom = GroupType.createType(s, type);
      Field f = custom.addAttribute(s, name, read, write, req);
      Assert.assertTrue("added ATTRIBUTE field", true);
      f = FieldFinder.find(name, true);
      try {
        custom.deleteField(s, name);  
        Assert.assertTrue("deleted unused ATTRIBUTE", true);
        Set fields = custom.getFields();
        if (fields.contains(f)) {
          Assert.fail("deleted ATTRIBUTE still exists");
        } 
      }
      catch (Exception e) {
        Assert.fail("could not delete ATTRIBUTE: " + e.getMessage());
      }
    }
    catch (Exception e) {
      Assert.fail("unexpected exception: " + e.getMessage());
    }
    finally {
      SessionHelper.stop(s);
    }
  }

  /**
   * 
   */
  public void testDeleteUnusedCustomList() {
    GrouperSession  s     = null;
    String          type  = "customType.TDUCL";
    String          name  = "customField.TDUCL";
    Privilege       read  = AccessPrivilege.VIEW;
    Privilege       write = AccessPrivilege.UPDATE;
    try {
      s = SessionHelper.getRootSession();
      GroupType custom = GroupType.createType(s, type);
      Field f = custom.addList(s, name, read, write);
      Assert.assertTrue("added LIST field", true);
      f = FieldFinder.find(name, true);
      try {
        custom.deleteField(s, name);  
        Assert.assertTrue("deleted unused LIST", true);
        Set fields = custom.getFields();
        if (fields.contains(f)) {
          Assert.fail("deleted LIST still exists");
        } 
      }
      catch (Exception e) {
        String error = "could not delete LIST: " + e.getMessage();
        LOG.error(error, e);
        Assert.fail(error + ", " + ExceptionUtils.getFullStackTrace(e));
      }
    }
    catch (Exception e) {
      Assert.fail("unexpected exception: " + e.getMessage());
    }
    finally {
      SessionHelper.stop(s);
    }
  } // public void testDeleteUnusedCustomList() 

  public void testFailToDeleteUsedCustomAttribute() {
    GrouperSession  s     = null;
    String          type  = "customType.FTTDUCA";
    String          name  = "customField.FTTDUCA";
    Privilege       read  = AccessPrivilege.VIEW;
    Privilege       write = AccessPrivilege.UPDATE;
    boolean         req   = false;
    try {
      s = SessionHelper.getRootSession();
      GroupType custom = GroupType.createType(s, type);
      custom.addAttribute(s, name, read, write, req);
      Assert.assertTrue("added ATTRIBUTE field", true);
      Stem  root  = StemFinder.findRootStem(s);
      Stem  ns    = root.addChildStem("ns", "ns");
      Group g     = ns.addChildGroup("g", "g");
      g.addType(custom);
      g.setAttribute(name, name);

      try {
        custom.deleteField(s, name);  
        Assert.fail("deleted in-use ATTRIBUTE");
      }
      catch (Exception e) {
        Assert.assertTrue("could not delete in-use ATTRIBUTE", true);
      }
    }
    catch (Exception e) {
      Assert.fail("unexpected exception: " + e.getMessage());
    }
    finally {
      SessionHelper.stop(s);
    }
  } // public void testFailToDeleteUsedCustomAttribute()

  public void testFailToDeleteUsedCustomList() {
    GrouperSession  s     = null;
    String          type  = "customType.FTDUCL";
    String          name  = "customField.FTTDUCL";
    Privilege       read  = AccessPrivilege.VIEW;
    Privilege       write = AccessPrivilege.UPDATE;
    try {
      s = SessionHelper.getRootSession();
      GroupType custom = GroupType.createType(s, type);
      Field f = custom.addList(s, name, read, write);
      Assert.assertTrue("added LIST field", true);
      Stem  root  = StemFinder.findRootStem(s);
      Stem  ns    = root.addChildStem("ns", "ns");
      Group g     = ns.addChildGroup("g", "g");
      g.addType(custom);
      f = FieldFinder.find(name, true);
      g.addMember(SubjectTestHelper.SUBJ0, f);
      try {
        custom.deleteField(s, name);  
        Assert.fail("deleted in-use LIST");
      }
      catch (Exception e) {
        Assert.assertTrue("could not delete in-use LIST", true);
      }
    }
    catch (Exception e) {
      Assert.fail("unexpected exception: " + e.getMessage());
    }
    finally {
      SessionHelper.stop(s);
    }
  } // public void testFailToDeleteUsedCustomList()

  public void testFailToFindCustomField() {
    GrouperSession  s     = null;
    String          name  = "customField";
    try {
      s = SessionHelper.getRootSession();
      FieldFinder.find(name, true);
      Assert.fail("found custom field");
    }
    catch (SchemaException eS) {
      Assert.assertTrue("did not find custom field", true);
    }
    finally {
      SessionHelper.stop(s);
    }
  } // public void testFailToFindCustomField()

  public void testFailToFindCustomType() {
    GrouperSession  s     = null;
    String          name  = "customType";
    try {
      s               = SessionHelper.getRootSession();
      GroupTypeFinder.find(name, true);
      Assert.fail("somehow found custom type: " + name);
    }
    catch (SchemaException eS) {
      Assert.assertTrue("did not find custom type: " + name, true);
    }
    finally {
      SessionHelper.stop(s);      
    }
  } // public void testFailToFindCustomType()

  public void testFindCustomField() {
    GrouperSession  s     = null;
    String          type  = "customType.3";
    String          name  = "customField.3";
    Privilege       read  = AccessPrivilege.VIEW;
    Privilege       write = AccessPrivilege.UPDATE;
    try {
      s = SessionHelper.getRootSession();
      GroupType custom = GroupType.createType(s, type);
      custom.addList(s, name, read, write);
      Assert.assertTrue("added LIST field", true);
      try {
        FieldFinder.find(name, true);
        Assert.assertTrue("found custom field", true);
      }
      catch (SchemaException eS) {
        Assert.fail("did not find custom field: " + eS.getMessage());
      }
    }
    catch (InsufficientPrivilegeException eIP) {
      Assert.fail("unexpected exception: " + eIP.getMessage());
    }
    catch (SchemaException eS) {
      Assert.fail("unexpected exception: " + eS.getMessage());
    }
    finally {
      SessionHelper.stop(s);
    }
  } // public void testFindCustomField()

  public void testFindCustomType() {
    GrouperSession  s     = null;
    String          name  = "customType";
    try {
      s               = SessionHelper.getRootSession();
      GroupType.createType(s, name);
      try {
        GroupTypeFinder.find(name, true);
        Assert.assertTrue("found custom type: " + name, true);
      }
      catch  (SchemaException eS) {
        Assert.fail("failed to find custom type: " + name + ":" + eS.getMessage());
      }
    }
    catch (InsufficientPrivilegeException eIP) {
      Assert.fail(eIP.getMessage());
    }
    catch (SchemaException eS) {
      Assert.fail("failed to create custom type: " + name + ": " + eS.getMessage());
    }
    finally {
      SessionHelper.stop(s);      
    }
  } // public void testFindCustomType()

  /**
   * 
   */
  public void testGetAttributeReturnTypes() {
    GrouperSession s = null;
    try {
      s = SessionHelper.getRootSession();
      String    type    = "customType.TGART";
      GroupType custom  = GroupType.createType(s, type);
      String    name    = "customField.TGART";
      Privilege read    = AccessPrivilege.VIEW;
      Privilege write   = AccessPrivilege.UPDATE;
      boolean   req     = false;
      Field     f       = custom.addAttribute(s, name, read, write, req);
  
      Stem      root    = StemFinder.findRootStem(s);
      Stem      edu     = root.addChildStem("edu", "edu");
      Group     g       = edu.addChildGroup("g", "g");
  
      try {
        Assert.assertTrue(
          "name", g.getName().equals("edu:g")
        );
      }
      catch (Exception e) {
        Assert.fail("unexpected exception (name): " + e.getMessage());
      }
  
      try {
        Assert.assertTrue(
          "description", g.getDescription().equals( "" )
        );
      }
      catch (Exception e) {
        Assert.fail("unexpected exception (desc): " + e.getMessage());
      }
  
      try {
        g.getAttributeValue(f.getName(), false, true);
        Assert.fail("retrieved schema-violating attribute");
      }
      catch (Exception e) {
        Assert.assertTrue("failed to retrieve schema-violating attribute", true);
      }
  
    }
    catch (Exception e) {
      Assert.fail(e.getMessage());
    }
    finally {
      SessionHelper.stop(s);
    }
  }

  public void testUseCustomAttribute() {
    GrouperSession  s     = null;
    String          type  = "customType.TUCA";
    String          name  = "customField.TUCA";
    Privilege       read  = AccessPrivilege.VIEW;
    Privilege       write = AccessPrivilege.UPDATE;
    boolean         req   = false;
    try {
      s = SessionHelper.getRootSession();
      GroupType custom = GroupType.createType(s, type);
      custom.addAttribute(s, name, read, write, req);
      Assert.assertTrue("added ATTRIBUTE field", true);
  
      Stem  root  = StemFinder.findRootStem(s);
      Stem  edu   = root.addChildStem("edu", "edu");
      Group g     = edu.addChildGroup("g", "g");
  
      Assert.assertTrue("no custom type", !g.hasType(custom));
  
      g.addType(custom);
      Assert.assertTrue("custom type", g.hasType(custom));
  
      g.setAttribute(name, name);

      Assert.assertTrue( "has attribute", g.getAttributeValue(name, false, true).equals(name) );
  
      g.deleteAttribute(name);
  
      g.deleteType(custom);
      Assert.assertTrue("custom type removed", !g.hasType(custom));
  
      try {
        g.setAttribute(name, name);

        fail("added field without type");
      }
      catch (AttributeNotFoundException eExpected) { 
        assertTrue("could not add field without type", true);
      }
    }
    catch (Exception e) {
      T.e(e);
    }
    finally {
      SessionHelper.stop(s);
    }
  } // public void testUseCustomAttribute()

  public void testUseCustomAttributeRequired() {
    GrouperSession  s     = null;
    String          type  = "customType.TUCAR";
    String          name  = "customField.TUCAR";
    Privilege       read  = AccessPrivilege.VIEW;
    Privilege       write = AccessPrivilege.UPDATE;
    boolean         req   = true;
    try {
      s = SessionHelper.getRootSession();
      GroupType custom = GroupType.createType(s, type);
      custom.addAttribute(s, name, read, write, req);
      Assert.assertTrue("added ATTRIBUTE field", true);
      try {
        Stem  root  = StemFinder.findRootStem(s);
        Stem  edu   = root.addChildStem("edu", "edu");
        Group g     = edu.addChildGroup("g", "g");
  
        Assert.assertTrue("no custom type", !g.hasType(custom));
  
        g.addType(custom);
        Assert.assertTrue("custom type", g.hasType(custom));
  
        g.setAttribute(name, name);

        Assert.assertTrue(
          "has attribute", 
          g.getAttributeValue(name, false, true).equals(name)
        );
  
        try {
          g.deleteAttribute(name);
          Assert.fail("deleted required attribute");
        }
        catch (Exception e) {
          Assert.assertTrue("cannot delete required attribute", true);
        }
  
      }
      catch (Exception e) {
        Assert.fail(e.getMessage());
      }
    }
    catch (InsufficientPrivilegeException eIP) {
      Assert.fail("unexpected exception: " + eIP.getMessage());
    }
    catch (SchemaException eS) {
      Assert.fail("unexpected exception: " + eS.getMessage());
    }
    finally {
      SessionHelper.stop(s);
    }
  }

  public void testUseCustomList() {
    GrouperSession  s     = null;
    String          type  = "customType.TUCL";
    String          name  = "customField.TUCL";
    Privilege       read  = AccessPrivilege.VIEW;
    Privilege       write = AccessPrivilege.UPDATE;
    try {
      s = SessionHelper.getRootSession();
      GroupType custom = GroupType.createType(s, type);
      Field f = custom.addList(s, name, read, write);
      Assert.assertTrue("added LIST field", true);
  
      Stem  root  = StemFinder.findRootStem(s);
      Stem  edu   = root.addChildStem("edu", "edu");
      Group g     = edu.addChildGroup("g", "g");
  
      Assert.assertTrue("no custom type", !g.hasType(custom));
  
      g.addType(custom);
      Assert.assertTrue("custom type", g.hasType(custom));
  
      g.addMember(SubjectTestHelper.SUBJ0, f);
      Assert.assertTrue("has member", g.hasMember(SubjectTestHelper.SUBJ0, f));
  
      g.deleteMember(SubjectTestHelper.SUBJ0, f);
  
      g.deleteType(custom);
      Assert.assertTrue("custom type removed", !g.hasType(custom));
  
      try {
        g.addMember(SubjectTestHelper.SUBJ0, f);
        Assert.fail("added field without type");
      }
      catch (Exception e) {
        Assert.assertTrue("could not add field without type", true);
      }
    }
    catch (Exception e) {
      T.e(e);
    }
    finally {
      SessionHelper.stop(s);
    }
  } // public void testUseCustomList()

  // TESTS //  
  
  public void testDelete_FieldsDeletedWhenGroupTypeIsDeleted() {
    try {
      LOG.info("testDelete_FieldsDeletedWhenGroupTypeIsDeleted");
      R               r     = new R();
      GrouperSession  s     = r.getSession();
      GroupType       type  = GroupType.createType(s, "custom type");
      type.addAttribute(s, "custom attribute", AccessPrivilege.ADMIN, AccessPrivilege.ADMIN, false);
      type.addList(s, "custom list", AccessPrivilege.ADMIN, AccessPrivilege.ADMIN);
  
      assertEquals(
        "grouptype has fields before deletion",
        2, GrouperDAOFactory.getFactory().getField().findAllFieldsByGroupType( type.getUuid() ).size()
      );
      type.delete(s); // fields show be automatically deleted when the parent type is deleted
      assertEquals(
        "grouptype does not have fields after deletion",
        0, GrouperDAOFactory.getFactory().getField().findAllFieldsByGroupType( type.getUuid() ).size()
      );
    }
    catch (Exception e) {
      unexpectedException(e);
    }
  } // public void testDelete_FieldsDeletedWhenGroupTypeIsDeleted()

  public void testDelete_FieldsDeletedWhenRegistryIsReset() {
    try {
      LOG.info("testDelete_FieldsDeletedWhenRegistryIsReset");
      R               r     = new R();
      GrouperSession  s     = r.getSession();
      GroupType       type  = GroupType.createType(s, "custom type");
      type.addAttribute(s, "custom attribute", AccessPrivilege.ADMIN, AccessPrivilege.ADMIN, false);
      type.addList(s, "custom list", AccessPrivilege.ADMIN, AccessPrivilege.ADMIN);
  
      assertEquals(
        "grouptype has fields before reset",
        2, GrouperDAOFactory.getFactory().getField().findAllFieldsByGroupType( type.getUuid() ).size()
      );
      RegistryReset.reset();  // fields should be deleted when registry is reset
      assertEquals(
        "grouptype does not have fields after reset",
        0, GrouperDAOFactory.getFactory().getField().findAllFieldsByGroupType( type.getUuid() ).size()
      );
    }
    catch (Exception e) {
      unexpectedException(e);
    }
  } // public void testDelete_FieldsDeletedWhenRegistryIsReset()

  /**
   * make an example group type for testing
   * @return an example groupType
   */
  public static GroupType exampleGroupType() {
    GroupType groupType = new GroupType();
    groupType.setIsAssignable(true);
    groupType.setContextId("contextId");
    groupType.setCreateTime(3L);
    groupType.setCreatorUuid("creatorId");
    groupType.setHibernateVersionNumber(3L);
    groupType.setIsInternal(true);
    groupType.setName("name");
    groupType.setUuid("uuid");
    
    return groupType;
  }
  
  /**
   * make an example group type for testing
   * @return an example group type
   */
  public static GroupType exampleGroupTypeDb() {
    GroupType groupType = GroupTypeFinder.find("example", false);
    if (groupType == null) {
      groupType = GroupType.createType(GrouperSession.staticGrouperSession(), "example");
    }
    
    return groupType;
  }

  
  /**
   * make an example grouptype for testing
   * @return an example grouptype
   */
  public static GroupType exampleRetrieveGroupTypeDb() {
    GroupType groupType = GroupTypeFinder.find("example", true);
    return groupType;
  }

  
  /**
   * make sure update properties are detected correctly
   */
  public void testXmlInsert() {
    
    GrouperSession.startRootSession();
    
    GroupType groupTypeOriginal = GroupType.createType(GrouperSession.staticGrouperSession(), "exampleInsert");
    groupTypeOriginal = GroupTypeFinder.find("exampleInsert", true);
    //do this because last membership update isnt there, only in db
    GroupType groupTypeCopy = GroupTypeFinder.find("exampleInsert", true);
    GroupType groupTypeCopy2 = GroupTypeFinder.find("exampleInsert", true);
    groupTypeCopy.delete(GrouperSession.staticGrouperSession());
    
    //lets insert the original
    groupTypeCopy2.xmlSaveBusinessProperties(null);
    groupTypeCopy2.xmlSaveUpdateProperties();

    //refresh from DB
    groupTypeCopy = GroupTypeFinder.findByUuid(groupTypeOriginal.getUuid(), true);
    
    assertFalse(groupTypeCopy == groupTypeOriginal);
    assertFalse(groupTypeCopy.xmlDifferentBusinessProperties(groupTypeOriginal));
    assertFalse(groupTypeCopy.xmlDifferentUpdateProperties(groupTypeOriginal));
    
  }
  
  /**
   * make sure update properties are detected correctly
   */
  public void testXmlDifferentUpdateProperties() {
    
    @SuppressWarnings("unused")
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    GroupType groupType = null;
    GroupType exampleGroupType = null;

    
    //TEST UPDATE PROPERTIES
    {
      groupType = exampleGroupTypeDb();
      exampleGroupType = groupType.clone();
      
      groupType.setContextId("abc");
      
      assertFalse(groupType.xmlDifferentBusinessProperties(exampleGroupType));
      assertTrue(groupType.xmlDifferentUpdateProperties(exampleGroupType));

      groupType.setContextId(exampleGroupType.getContextId());
      groupType.xmlSaveUpdateProperties();

      groupType = exampleRetrieveGroupTypeDb();
      
      assertFalse(groupType.xmlDifferentBusinessProperties(exampleGroupType));
      assertFalse(groupType.xmlDifferentUpdateProperties(exampleGroupType));
      
    }
    
    {
      groupType = exampleGroupTypeDb();
      exampleGroupType = groupType.clone();

      groupType.setCreateTime(99);
      
      assertFalse(groupType.xmlDifferentBusinessProperties(exampleGroupType));
      assertTrue(groupType.xmlDifferentUpdateProperties(exampleGroupType));

      groupType.setCreateTime(exampleGroupType.getCreateTime());
      groupType.xmlSaveUpdateProperties();
      
      groupType = exampleRetrieveGroupTypeDb();
      
      assertFalse(groupType.xmlDifferentBusinessProperties(exampleGroupType));
      assertFalse(groupType.xmlDifferentUpdateProperties(exampleGroupType));
    }
    
    {
      groupType = exampleGroupTypeDb();
      exampleGroupType = groupType.clone();

      groupType.setCreatorUuid("abc");
      
      assertFalse(groupType.xmlDifferentBusinessProperties(exampleGroupType));
      assertTrue(groupType.xmlDifferentUpdateProperties(exampleGroupType));

      groupType.setCreatorUuid(exampleGroupType.getCreatorUuid());
      groupType.xmlSaveUpdateProperties();
      
      groupType = exampleRetrieveGroupTypeDb();
      
      assertFalse(groupType.xmlDifferentBusinessProperties(exampleGroupType));
      assertFalse(groupType.xmlDifferentUpdateProperties(exampleGroupType));
    }
    
    {
      groupType = exampleGroupTypeDb();
      exampleGroupType = groupType.clone();

      groupType.setHibernateVersionNumber(99L);
      
      assertFalse(groupType.xmlDifferentBusinessProperties(exampleGroupType));
      assertTrue(groupType.xmlDifferentUpdateProperties(exampleGroupType));

      groupType.setHibernateVersionNumber(exampleGroupType.getHibernateVersionNumber());
      groupType.xmlSaveUpdateProperties();
      
      groupType = exampleRetrieveGroupTypeDb();
      
      assertFalse(groupType.xmlDifferentBusinessProperties(exampleGroupType));
      assertFalse(groupType.xmlDifferentUpdateProperties(exampleGroupType));
    }
    //TEST BUSINESS PROPERTIES
    
    {
      groupType = exampleGroupTypeDb();
      exampleGroupType = groupType.clone();

      groupType.setName("abc");
      
      assertTrue(groupType.xmlDifferentBusinessProperties(exampleGroupType));
      assertFalse(groupType.xmlDifferentUpdateProperties(exampleGroupType));

      groupType.setName(exampleGroupType.getName());
      groupType.xmlSaveBusinessProperties(exampleGroupType.clone());
      groupType.xmlSaveUpdateProperties();
      
      groupType = exampleRetrieveGroupTypeDb();
      
      assertFalse(groupType.xmlDifferentBusinessProperties(exampleGroupType));
      assertFalse(groupType.xmlDifferentUpdateProperties(exampleGroupType));
    
    }
    
    {
      groupType = exampleGroupTypeDb();
      exampleGroupType = groupType.clone();

      groupType.setIsAssignable(false);
      
      assertTrue(groupType.xmlDifferentBusinessProperties(exampleGroupType));
      assertFalse(groupType.xmlDifferentUpdateProperties(exampleGroupType));

      groupType.setIsAssignable(exampleGroupType.getIsAssignable());
      groupType.xmlSaveBusinessProperties(exampleGroupType.clone());
      groupType.xmlSaveUpdateProperties();
      
      groupType = exampleRetrieveGroupTypeDb();
      
      assertFalse(groupType.xmlDifferentBusinessProperties(exampleGroupType));
      assertFalse(groupType.xmlDifferentUpdateProperties(exampleGroupType));
    
    }
    
    {
      groupType = exampleGroupTypeDb();
      exampleGroupType = groupType.clone();

      groupType.setIsInternal(true);
      
      assertTrue(groupType.xmlDifferentBusinessProperties(exampleGroupType));
      assertFalse(groupType.xmlDifferentUpdateProperties(exampleGroupType));

      groupType.setIsInternal(exampleGroupType.getIsInternal());
      groupType.xmlSaveBusinessProperties(exampleGroupType.clone());
      groupType.xmlSaveUpdateProperties();
      
      groupType = exampleRetrieveGroupTypeDb();
      
      assertFalse(groupType.xmlDifferentBusinessProperties(exampleGroupType));
      assertFalse(groupType.xmlDifferentUpdateProperties(exampleGroupType));
    
    }
    
    {
      groupType = exampleGroupTypeDb();
      exampleGroupType = groupType.clone();

      groupType.setUuid("abc");
      
      assertTrue(groupType.xmlDifferentBusinessProperties(exampleGroupType));
      assertFalse(groupType.xmlDifferentUpdateProperties(exampleGroupType));

      groupType.setUuid(exampleGroupType.getUuid());
      groupType.xmlSaveBusinessProperties(exampleGroupType.clone());
      groupType.xmlSaveUpdateProperties();
      
      groupType = exampleRetrieveGroupTypeDb();
      
      assertFalse(groupType.xmlDifferentBusinessProperties(exampleGroupType));
      assertFalse(groupType.xmlDifferentUpdateProperties(exampleGroupType));
    
    }
  }

  
}

