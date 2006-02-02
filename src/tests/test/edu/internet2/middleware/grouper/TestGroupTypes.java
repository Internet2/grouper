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
 * Test Group Types.
 * <p />
 * @author  blair christensen.
 * @version $Id: TestGroupTypes.java,v 1.4 2006-02-02 16:56:46 blair Exp $
 */
public class TestGroupTypes extends TestCase {

  // Private Class Constants
  private static final Log LOG = LogFactory.getLog(TestGroupTypes.class);


  public TestGroupTypes(String name) {
    super(name);
  }

  protected void setUp () {
    LOG.debug("setUp");
    RegistryReset.resetRegistryAndAddTestSubjects();
  }

  protected void tearDown () {
    LOG.debug("tearDown");
  }

  // Tests

  public void testCreateExistingType() {
    GrouperSession  s     = null;
    String          name  = "base";
    try {
      s               = SessionHelper.getRootSession();
      GroupType type  = GroupType.createType(s, name);
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

  public void testCreateNewTypeAsRoot() {
    GrouperSession  s     = null;
    String          name  = "customType";
    try {
      s               = SessionHelper.getRootSession();
      GroupType type  = GroupType.createType(s, name);
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

  public void testCreateNewTypeAsNonRoot() {
    GrouperSession  s     = null;
    String          name  = "customType";
    try {
      s               = SessionHelper.getSession(SubjectHelper.SUBJ0_ID);
      GroupType type  = GroupType.createType(s, name);
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

  public void testFailToFindCustomType() {
    GrouperSession  s     = null;
    String          name  = "customType";
    try {
      s               = SessionHelper.getRootSession();
      GroupType type  = GroupTypeFinder.find(name);
      Assert.fail("somehow found custom type: " + name);
    }
    catch (SchemaException eS) {
      Assert.assertTrue("did not find custom type: " + name, true);
    }
    finally {
      SessionHelper.stop(s);      
    }
  } // public void testFailToFindCustomType()

  public void testFindCustomType() {
    GrouperSession  s     = null;
    String          name  = "customType";
    try {
      s               = SessionHelper.getRootSession();
      GroupType type  = GroupType.createType(s, name);
      try {
        GroupType found = GroupTypeFinder.find(name);
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

  public void testFailToFindCustomField() {
    GrouperSession  s     = null;
    String          type  = "customType";
    String          name  = "customField";
    FieldType       ft    = FieldType.LIST; 
    Privilege       read  = AccessPrivilege.VIEW;
    Privilege       write = AccessPrivilege.UPDATE;
    boolean         req   = false;
    try {
      s = SessionHelper.getRootSession();
      Field found = FieldFinder.find(name);
      Assert.fail("found custom field");
    }
    catch (SchemaException eS) {
      Assert.assertTrue("did not find custom field", true);
    }
    finally {
      SessionHelper.stop(s);
    }
  } // public void testFailToFindCustomField()

  public void testAddFieldAsNonRoot() {
    GrouperSession  s     = null;
    String          type  = "base";
    String          name  = "customField";
    FieldType       ft    = FieldType.LIST;
    Privilege       read  = AccessPrivilege.VIEW;
    Privilege       write = AccessPrivilege.UPDATE;
    boolean         req   = false;
    try {
      GroupType base = GroupTypeFinder.find(type);
      s = SessionHelper.getSession(SubjectHelper.SUBJ0_ID);
      base.addField(s, name, ft, read, write, req);
      Assert.fail("added field to base type"); 
    }
    catch (InsufficientPrivilegeException eIP) {
      Assert.assertTrue("not privileged to add field", true); 
    }
    catch (SchemaException eS) {
      Assert.fail("unexpected exception: " + eS.getMessage());
    }
    finally {
      SessionHelper.stop(s);
    }
  } // public void testAddFieldAsNonRoot()

  public void testAddExistingField() {
    GrouperSession  s     = null;
    String          type  = "base";
    String          name  = "members";
    FieldType       ft    = FieldType.LIST;
    Privilege       read  = AccessPrivilege.VIEW;
    Privilege       write = AccessPrivilege.UPDATE;
    boolean         req   = false;
    try {
      GroupType base = GroupTypeFinder.find(type);
      s = SessionHelper.getRootSession();
      base.addField(s, name, ft, read, write, req);
      Assert.fail("added field to base type"); 
    }
    catch (InsufficientPrivilegeException eIP) {
      Assert.fail("unexpected exception: " + eIP.getMessage());
    }
    catch (SchemaException eS) {
      Assert.assertTrue("field already exists", true);
    }
    finally {
      SessionHelper.stop(s);
    }
  } // public void testAddExistingField()

  public void testAddFieldToBase() {
    GrouperSession  s     = null;
    String          type  = "base";
    String          name  = "customField";
    FieldType       ft    = FieldType.LIST;
    Privilege       read  = AccessPrivilege.VIEW;
    Privilege       write = AccessPrivilege.UPDATE;
    boolean         req   = false;
    try {
      GroupType base = GroupTypeFinder.find(type);
      s = SessionHelper.getRootSession();
      base.addField(s, name, ft, read, write, req);
      Assert.fail("added field to base type"); 
    }
    catch (InsufficientPrivilegeException eIP) {
      Assert.fail("unexpected exception: " + eIP.getMessage());
    }
    catch (SchemaException eS) {
      Assert.assertTrue("cannot add to base", true);
    }
    finally {
      SessionHelper.stop(s);
    }
  } // public void testAddFieldToBase()

  public void testAddFieldToNaming() {
    GrouperSession  s     = null;
    String          type  = "naming";
    String          name  = "customField";
    FieldType       ft    = FieldType.LIST;
    Privilege       read  = AccessPrivilege.VIEW;
    Privilege       write = AccessPrivilege.UPDATE;
    boolean         req   = false;
    try {
      GroupType base = GroupTypeFinder.find(type);
      s = SessionHelper.getRootSession();
      base.addField(s, name, ft, read, write, req);
      Assert.fail("added field to naming type"); 
    }
    catch (InsufficientPrivilegeException eIP) {
      Assert.fail("unexpected exception: " + eIP.getMessage());
    }
    catch (SchemaException eS) {
      Assert.assertTrue("cannot add to naming", true);
    }
    finally {
      SessionHelper.stop(s);
    }
  } // public void testAddFieldToNaming()

  public void testAddFieldTypeAccess() {
    GrouperSession  s     = null;
    String          type  = "customType";
    String          name  = "customField";
    FieldType       ft    = FieldType.ACCESS;
    Privilege       read  = AccessPrivilege.VIEW;
    Privilege       write = AccessPrivilege.UPDATE;
    boolean         req   = false;
    try {
      s = SessionHelper.getRootSession();
      GroupType custom = GroupType.createType(s, type);
      custom.addField(s, name, ft, read, write, req);
      Assert.fail("added ACCESS field");
    }
    catch (InsufficientPrivilegeException eIP) {
      Assert.fail("unexpected exception: " + eIP.getMessage());
    }
    catch (SchemaException eS) {
      Assert.assertTrue("cannot add ACCESS field", true);
    }
    finally {
      SessionHelper.stop(s);
    }
  } // public void testAddFieldAccess()

  public void testAddFieldTypeNaming() {
    GrouperSession  s     = null;
    String          type  = "customType";
    String          name  = "customField";
    FieldType       ft    = FieldType.NAMING;
    Privilege       read  = AccessPrivilege.VIEW;
    Privilege       write = AccessPrivilege.UPDATE;
    boolean         req   = false;
    try {
      s = SessionHelper.getRootSession();
      GroupType custom = GroupType.createType(s, type);
      custom.addField(s, name, ft, read, write, req);
      Assert.fail("added NAMING field");
    }
    catch (InsufficientPrivilegeException eIP) {
      Assert.fail("unexpected exception: " + eIP.getMessage());
    }
    catch (SchemaException eS) {
      Assert.assertTrue("cannot add NAMING field", true);
    }
    finally {
      SessionHelper.stop(s);
    }
  } // public void testAddFieldNaming()

  public void testAddFieldReadNotAccess() {
    GrouperSession  s     = null;
    String          type  = "customType";
    String          name  = "customField";
    FieldType       ft    = FieldType.LIST; 
    Privilege       read  = NamingPrivilege.CREATE;
    Privilege       write = AccessPrivilege.UPDATE;
    boolean         req   = false;
    try {
      s = SessionHelper.getRootSession();
      GroupType custom = GroupType.createType(s, type);
      custom.addField(s, name, ft, read, write, req);
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

  public void testAddFieldWriteNotAccess() {
    GrouperSession  s     = null;
    String          type  = "customType";
    String          name  = "customField";
    FieldType       ft    = FieldType.LIST; 
    Privilege       read  = AccessPrivilege.VIEW;
    Privilege       write = NamingPrivilege.STEM;
    boolean         req   = false;
    try {
      s = SessionHelper.getRootSession();
      GroupType custom = GroupType.createType(s, type);
      custom.addField(s, name, ft, read, write, req);
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

  public void testAddFieldAttribute() {
    GrouperSession  s     = null;
    String          type  = "customType.0";
    String          name  = "customField";
    FieldType       ft    = FieldType.ATTRIBUTE; 
    Privilege       read  = AccessPrivilege.VIEW;
    Privilege       write = AccessPrivilege.UPDATE;
    boolean         req   = false;
    try {
      s = SessionHelper.getRootSession();
      GroupType custom = GroupType.createType(s, type);
      custom.addField(s, name, ft, read, write, req);
      Assert.assertTrue("added ATTRIBUTE field", true);
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
  } // public void testAddFieldAttribute()

  public void testAddFieldList() {
    GrouperSession  s     = null;
    String          type  = "customType.1";
    String          name  = "customField";
    FieldType       ft    = FieldType.LIST; 
    Privilege       read  = AccessPrivilege.VIEW;
    Privilege       write = AccessPrivilege.UPDATE;
    boolean         req   = false;
    try {
      s = SessionHelper.getRootSession();
      GroupType custom = GroupType.createType(s, type);
      custom.addField(s, name, ft, read, write, req);
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

  public void testAddFieldDuplicateName() {
    GrouperSession  s     = null;
    String          type  = "customType.2";
    String          name  = "customField";
    FieldType       ft    = FieldType.LIST; 
    Privilege       read  = AccessPrivilege.VIEW;
    Privilege       write = AccessPrivilege.UPDATE;
    boolean         req   = false;
    try {
      s = SessionHelper.getRootSession();
      GroupType custom = GroupType.createType(s, type);
      custom.addField(s, name, ft, read, write, req);
      Assert.assertTrue("added LIST field", true);
      try {
        custom.addField(s, name, FieldType.ATTRIBUTE, read, write, req);
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

  public void testFindCustomField() {
    GrouperSession  s     = null;
    String          type  = "customType.3";
    String          name  = "customField.3";
    FieldType       ft    = FieldType.LIST; 
    Privilege       read  = AccessPrivilege.VIEW;
    Privilege       write = AccessPrivilege.UPDATE;
    boolean         req   = false;
    try {
      s = SessionHelper.getRootSession();
      GroupType custom = GroupType.createType(s, type);
      custom.addField(s, name, ft, read, write, req);
      Assert.assertTrue("added LIST field", true);
      try {
        Field found = FieldFinder.find(name);
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

  public void testUseCustomList() {
    GrouperSession  s     = null;
    String          type  = "customType.TUCL";
    String          name  = "customField.TUCL";
    FieldType       ft    = FieldType.LIST; 
    Privilege       read  = AccessPrivilege.VIEW;
    Privilege       write = AccessPrivilege.UPDATE;
    boolean         req   = false;
    try {
      s = SessionHelper.getRootSession();
      GroupType custom = GroupType.createType(s, type);
      custom.addField(s, name, ft, read, write, req);
      Assert.assertTrue("added LIST field", true);
      try {
        Field f     = FieldFinder.find(name);
        Stem  root  = StemFinder.findRootStem(s);
        Stem  edu   = root.addChildStem("edu", "edu");
        Group g     = edu.addChildGroup("g", "g");

        Assert.assertTrue("no custom type", !g.hasType(custom));

        g.addType(custom);
        Assert.assertTrue("custom type", g.hasType(custom));

        g.addMember(SubjectHelper.SUBJ0, f);
        Assert.assertTrue("has member", g.hasMember(SubjectHelper.SUBJ0, f));

        g.deleteMember(SubjectHelper.SUBJ0, f);

        g.deleteType(custom);
        Assert.assertTrue("custom type removed", !g.hasType(custom));

        try {
          g.addMember(SubjectHelper.SUBJ0, f);
          Assert.fail("added field without type");
        }
        catch (Exception e) {
          Assert.assertTrue("could not add field without type", true);
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
  } // public void testUseCustomList()

  public void testUseCustomAttribute() {
    GrouperSession  s     = null;
    String          type  = "customType.TUCA";
    String          name  = "customField.TUCA";
    FieldType       ft    = FieldType.ATTRIBUTE; 
    Privilege       read  = AccessPrivilege.VIEW;
    Privilege       write = AccessPrivilege.UPDATE;
    boolean         req   = false;
    try {
      s = SessionHelper.getRootSession();
      GroupType custom = GroupType.createType(s, type);
      custom.addField(s, name, ft, read, write, req);
      Assert.assertTrue("added ATTRIBUTE field", true);
      try {
        Field f     = FieldFinder.find(name);
        Stem  root  = StemFinder.findRootStem(s);
        Stem  edu   = root.addChildStem("edu", "edu");
        Group g     = edu.addChildGroup("g", "g");

        Assert.assertTrue("no custom type", !g.hasType(custom));

        g.addType(custom);
        Assert.assertTrue("custom type", g.hasType(custom));

        g.setAttribute(name, name);
        Assert.assertTrue(
          "has attribute", 
          g.getAttribute(name).equals(name)
        );

        g.deleteAttribute(name);

        g.deleteType(custom);
        Assert.assertTrue("custom type removed", !g.hasType(custom));

        try {
          g.setAttribute(name, name);
          Assert.fail("added field without type");
        }
        catch (Exception e) {
          Assert.assertTrue("could not add field without type", true);
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
  } // public void testUseCustomAttribute()

  public void testUseCustomAttributeRequired() {
    GrouperSession  s     = null;
    String          type  = "customType.TUCAR";
    String          name  = "customField.TUCAR";
    FieldType       ft    = FieldType.ATTRIBUTE; 
    Privilege       read  = AccessPrivilege.VIEW;
    Privilege       write = AccessPrivilege.UPDATE;
    boolean         req   = true;
    try {
      s = SessionHelper.getRootSession();
      GroupType custom = GroupType.createType(s, type);
      custom.addField(s, name, ft, read, write, req);
      Assert.assertTrue("added ATTRIBUTE field", true);
      try {
        Field f     = FieldFinder.find(name);
        Stem  root  = StemFinder.findRootStem(s);
        Stem  edu   = root.addChildStem("edu", "edu");
        Group g     = edu.addChildGroup("g", "g");

        Assert.assertTrue("no custom type", !g.hasType(custom));

        g.addType(custom);
        Assert.assertTrue("custom type", g.hasType(custom));

        g.setAttribute(name, name);
        Assert.assertTrue(
          "has attribute", 
          g.getAttribute(name).equals(name)
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
  } // public void testUseCustomAttributeRequired()

  public void testGetAttributeReturnTypes() {
    GrouperSession s = null;
    try {
      s = SessionHelper.getRootSession();
      String    type    = "customType.TGART";
      GroupType custom  = GroupType.createType(s, type);
      String    name    = "customField.TGART";
      FieldType ft      = FieldType.ATTRIBUTE; 
      Privilege read    = AccessPrivilege.VIEW;
      Privilege write   = AccessPrivilege.UPDATE;
      boolean   req     = false;
      custom.addField(s, name, ft, read, write, req);
      Field     f       = FieldFinder.find(name);

      Stem      root    = StemFinder.findRootStem(s);
      Stem      edu     = root.addChildStem("edu", "edu");
      Group     g       = edu.addChildGroup("g", "g");

      try {
        Assert.assertTrue(
          "name", g.getAttribute("name").equals("edu:g")
        );
      }
      catch (Exception e) {
        Assert.fail("unexpected exception (name): " + e.getMessage());
      }

      try {
        Assert.assertTrue(
          "description", g.getAttribute("description").equals( new String() )
        );
      }
      catch (Exception e) {
        Assert.fail("unexpected exception (desc): " + e.getMessage());
      }

      try {
        String fail = g.getAttribute(f.getName());
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
  } // public void testGetAttributeReturnTypes() 

  public void testDeleteFromBase() {
    GrouperSession  s     = null;
    String          type  = "base";
    String          name  = "description";
    try {
      s = SessionHelper.getRootSession();
      GroupType base = GroupTypeFinder.find(type);
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
  } // public void testDeleteFromBase() 

  public void testDeleteFromNaming() {
    GrouperSession  s     = null;
    String          type  = "naming";
    String          name  = "creators";
    try {
      s = SessionHelper.getRootSession();
      GroupType naming = GroupTypeFinder.find(type);
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
  } // public void testDeleteFromNaming() 

  public void testDeleteAsNonRoot() {
    GrouperSession  s     = null;
    String          type  = "customType.TDANR";
    String          name  = "customField.TDANR";
    FieldType       ft    = FieldType.ATTRIBUTE; 
    Privilege       read  = AccessPrivilege.VIEW;
    Privilege       write = AccessPrivilege.UPDATE;
    boolean         req   = false;
    try {
      s = SessionHelper.getRootSession();
      GroupType custom = GroupType.createType(s, type);
      custom.addField(s, name, ft, read, write, req);
      Assert.assertTrue("added ATTRIBUTE field", true);
      GrouperSession nrs = SessionHelper.getSession(SubjectHelper.SUBJ0_ID);
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
  } // public void testDeleteAsNonRoot()

  public void testDeleteUnusedCustomAttribute() {
    GrouperSession  s     = null;
    String          type  = "customType.TDUCA";
    String          name  = "customField.TDUCA";
    FieldType       ft    = FieldType.ATTRIBUTE; 
    Privilege       read  = AccessPrivilege.VIEW;
    Privilege       write = AccessPrivilege.UPDATE;
    boolean         req   = false;
    try {
      s = SessionHelper.getRootSession();
      GroupType custom = GroupType.createType(s, type);
      custom.addField(s, name, ft, read, write, req);
      Assert.assertTrue("added ATTRIBUTE field", true);
      Field f = FieldFinder.find(name);
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
  } // public void testDeleteUnusedCustomAttribute() 

  public void testDeleteUnusedCustomList() {
    GrouperSession  s     = null;
    String          type  = "customType.TDUCL";
    String          name  = "customField.TDUCL";
    FieldType       ft    = FieldType.LIST; 
    Privilege       read  = AccessPrivilege.VIEW;
    Privilege       write = AccessPrivilege.UPDATE;
    boolean         req   = false;
    try {
      s = SessionHelper.getRootSession();
      GroupType custom = GroupType.createType(s, type);
      custom.addField(s, name, ft, read, write, req);
      Assert.assertTrue("added LIST field", true);
      Field f = FieldFinder.find(name);
      try {
        custom.deleteField(s, name);  
        Assert.assertTrue("deleted unused LIST", true);
        Set fields = custom.getFields();
        if (fields.contains(f)) {
          Assert.fail("deleted LIST still exists");
        } 
      }
      catch (Exception e) {
        Assert.fail("could not delete LIST: " + e.getMessage());
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
    FieldType       ft    = FieldType.ATTRIBUTE; 
    Privilege       read  = AccessPrivilege.VIEW;
    Privilege       write = AccessPrivilege.UPDATE;
    boolean         req   = false;
    try {
      s = SessionHelper.getRootSession();
      GroupType custom = GroupType.createType(s, type);
      custom.addField(s, name, ft, read, write, req);
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
    FieldType       ft    = FieldType.LIST; 
    Privilege       read  = AccessPrivilege.VIEW;
    Privilege       write = AccessPrivilege.UPDATE;
    boolean         req   = false;
    try {
      s = SessionHelper.getRootSession();
      GroupType custom = GroupType.createType(s, type);
      custom.addField(s, name, ft, read, write, req);
      Assert.assertTrue("added LIST field", true);
      Stem  root  = StemFinder.findRootStem(s);
      Stem  ns    = root.addChildStem("ns", "ns");
      Group g     = ns.addChildGroup("g", "g");
      g.addType(custom);
      Field f     = FieldFinder.find(name);
      g.addMember(SubjectHelper.SUBJ0, f);
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

}

