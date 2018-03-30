/**
 * Copyright 2014 Internet2
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
 */
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

import edu.internet2.middleware.grouper.attr.AttributeDefName;
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
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.AttributeDefPrivilege;
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
    TestRunner.run(new TestGroupType("testDelete_FieldsDeletedWhenGroupTypeIsDeleted"));
  }
  
  // Private Static Class Constants
  private static final Log LOG = GrouperUtil.getLog(TestGroupType.class);

  public TestGroupType(String name) {
    super(name);
  }

  public void testFindAllTypes() {
    LOG.info("testFindAllTypes");
    Set types = GroupTypeFinder.findAll();    
    T.amount("public group types", 0, types.size());
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
      type.getAttributeDefName().getAttributeDef().getPrivilegeDelegate().grantPriv(subj, AttributeDefPrivilege.ATTR_READ, false);
      type.getAttributeDefName().getAttributeDef().getPrivilegeDelegate().grantPriv(subj, AttributeDefPrivilege.ATTR_UPDATE, false);
  
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
      AttributeDefName customA = custom.addAttribute(
        r.rs, "custom a", false
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

  public void testFailToAddFieldAsNonRoot() {
    LOG.info("testFailToAddFieldAsNonRoot");
    try {
      R               r     = R.populateRegistry(1, 1, 1);
      GroupType       test  = GroupType.createType(r.rs, "testType");
      Subject         subj  = r.getSubject("a");
      test.getAttributeDefName().getAttributeDef().getPrivilegeDelegate().grantPriv(subj, AttributeDefPrivilege.ATTR_ADMIN, false);
      GrouperSession  s     = GrouperSession.start(subj);
      
      try {
        test.addList(s, "test", AccessPrivilege.VIEW, AccessPrivilege.UPDATE);
        Assert.fail("added field to custom type as non-root");
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
      throw new RuntimeException(e);
    }
  } // public void testFailToAddFieldAsNonRoot()

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
      custom.getAttributeDefName().getAttributeDef().getPrivilegeDelegate().grantPriv(r.getSubject("a"), AttributeDefPrivilege.ATTR_ADMIN, false);
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
    T.amount("public group types before addition", 0, types.size());
    GrouperSession s = null;
    try {
      String    name  = "test";
      s               = SessionHelper.getRootSession();
      GroupType type  = GroupType.createType(s, name);
      Assert.assertTrue("added type: " + type, true);
      types = GroupTypeFinder.findAll();
      T.amount("public group types after addition", 1, types.size());
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
      AttributeDefName     attr    = custom.addAttribute(
        r.rs, "custom a", false
      );
      gA.addType(custom);
      gA.grantPriv(subjA, AccessPrivilege.ADMIN);
      custom.getAttributeDefName().getAttributeDef().getPrivilegeDelegate().grantPriv(subjA, AttributeDefPrivilege.ATTR_READ, false);
      custom.getAttributeDefName().getAttributeDef().getPrivilegeDelegate().grantPriv(subjA, AttributeDefPrivilege.ATTR_UPDATE, false);
      custom.internal_getAttributeDefForAttributes().getPrivilegeDelegate().grantPriv(subjA, AttributeDefPrivilege.ATTR_READ, false);
      custom.internal_getAttributeDefForAttributes().getPrivilegeDelegate().grantPriv(subjA, AttributeDefPrivilege.ATTR_UPDATE, false);
      r.rs.stop();
  
      // Now test-and-set attribute as !root
      GrouperSession  s = GrouperSession.start(subjA);
      Group           g = GroupFinder.findByName(s, name, true);
      Assert.assertTrue(
        "group has custom type", g.hasType(custom)
      );         
      Assert.assertTrue(
        "group does not have attribute set - yet",
        g.getAttributeValue(attr.getLegacyAttributeName(true), false, false).equals(GrouperConfig.EMPTY_STRING)
      );
      try {
        g.setAttribute(attr.getLegacyAttributeName(true), name);

        Assert.assertTrue("set attribute", true);
      }
      catch (Exception e) {
        Assert.fail("exception while setting custom attribute! - " + e.getMessage());
      }
      T.string("now group has attribute set", name, g.getAttributeValue(attr.getLegacyAttributeName(true), false, true));
      s.stop();
  
      // Now make sure it was properly persisted
      GrouperSession  S = GrouperSession.start(subjA);
      Group           G = GroupFinder.findByName(S, name, true);
      T.string("attribute was persisted", name, G.getAttributeValue(attr.getLegacyAttributeName(true), false, true));
      S.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testUseCustomAttributeAsNonRoot()



  /**
   * 
   */
  public void testAddFieldAttribute() {
    GrouperSession  s     = null;
    String          type  = "customType.0";
    String          name  = "customField";
    boolean         req   = false;
    s = SessionHelper.getRootSession();
    GroupType custom = GroupType.createType(s, type);
    custom.addAttribute(s, name, req);
    Assert.assertTrue("added ATTRIBUTE field", true);

    //try to add an attribute with a field name... it should not be allowed
    try {
      custom.addAttribute(s, Group.FIELD_DESCRIPTION, req);
      fail("Shouldnt be able to create a built in field attribute");
    } catch (Exception e) {
      //good
    }
    
    try {
      custom.addAttribute(s, Group.FIELD_EXTENSION, req);
      fail("Shouldnt be able to create a built in field attribute");
    } catch (Exception e) {
      //good
    }
    
    try {
      custom.addAttribute(s, Group.FIELD_DISPLAY_EXTENSION, req);
      fail("Shouldnt be able to create a built in field attribute");
    } catch (Exception e) {
      //good
    }
    
    try {
      custom.addAttribute(s, Group.FIELD_NAME, req);
      fail("Shouldnt be able to create a built in field attribute");
    } catch (Exception e) {
      //good
    }
    
    try {
      custom.addAttribute(s, Group.FIELD_DISPLAY_NAME, req);
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
    try {
      s = SessionHelper.getRootSession();
      GroupType custom = GroupType.createType(s, type);
      custom.addList(s, name, read, write);
      Assert.assertTrue("added LIST field", true);
      try {
        custom.addList(s, name, read, write);
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

  public void testAddAttributeDuplicateName() {
    GrouperSession  s     = null;
    String          type  = "customType.2";
    String          name  = "customAttribute";
    try {
      s = SessionHelper.getRootSession();
      GroupType custom = GroupType.createType(s, type);
      custom.addAttribute(s, name);
      try {
        custom.addAttribute(s, name);
        Assert.fail("added duplicate attribute");
      }
      catch (SchemaException eS) {
        Assert.assertTrue("cannot add duplicate attribute", true);
      }
    } finally {
      SessionHelper.stop(s);
    }
  }
  
  public void testAddFieldThenAttributeSameName() {
    GrouperSession  s     = null;
    String          type  = "customType.2";
    String          list  = "customList";
    String          attr  = "customAttribute";
    Privilege       read  = AccessPrivilege.VIEW;
    Privilege       write = AccessPrivilege.UPDATE;

    try {
      s = SessionHelper.getRootSession();
      Group group = Group.saveGroup(s, null, null, "test:test1", "test1", "test1", null, true);
      GroupType custom = GroupType.createType(s, type);
      Field field = custom.addList(s, list, read, write);
      custom.addAttribute(s, attr);
      group.addType(custom);
      
      group.setAttribute(attr, "testing");
      group.addMember(SubjectTestHelper.SUBJ0, field);
      group.getAttributes();
      group.getMembers(field);
    } finally {
      SessionHelper.stop(s);
    }
  }
  
  public void testAddAttributeThenFieldSameName() {
    GrouperSession  s     = null;
    String          type  = "customType.2";
    String          list  = "customList";
    String          attr  = "customAttribute";
    Privilege       read  = AccessPrivilege.VIEW;
    Privilege       write = AccessPrivilege.UPDATE;

    try {
      s = SessionHelper.getRootSession();
      Group group = Group.saveGroup(s, null, null, "test:test1", "test1", "test1", null, true);
      GroupType custom = GroupType.createType(s, type);
      custom.addAttribute(s, attr);
      Field field = custom.addList(s, list, read, write);
      group.addType(custom);
      
      group.setAttribute(attr, "testing");
      group.addMember(SubjectTestHelper.SUBJ0, field);
      group.getAttributes();
      group.getMembers(field);
    } finally {
      SessionHelper.stop(s);
    }
  }
  
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

  /**
   * 
   */
  public void testAddRemoveType() {
    GrouperSession  s     = null;
    String          type  = "customType.1a";
    String          name  = "customField1a";
    try {
      s = SessionHelper.getRootSession();
      GroupType custom = GroupType.createType(s, type);
      custom.addAttribute(s, name, false);
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
    String          name  = "testType";
    try {
      s               = SessionHelper.getRootSession();
      try {
        GroupType.createType(s, name);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
      
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
    boolean         req   = false;
    try {
      s = SessionHelper.getRootSession();
      GroupType custom = GroupType.createType(s, type);
      custom.addAttribute(s, name, req);
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
      boolean   req     = false;
      AttributeDefName attr = custom.addAttribute(s, name, req);
  
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
        g.getAttributeValue(attr.getLegacyAttributeName(true), false, true);
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
      custom.addAttribute(s, name, req);
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
      type.addList(s, "custom list", AccessPrivilege.ADMIN, AccessPrivilege.ADMIN);
  
      assertEquals(
        "grouptype has fields before deletion",
        1, FieldFinder.findAllByGroupType(type).size()
      );
      type.delete(s); // fields show be automatically deleted when the parent type is deleted
      assertEquals(
        "grouptype does not have fields after deletion",
        0, FieldFinder.findAllByGroupType(type).size()
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
      type.addList(s, "custom list", AccessPrivilege.ADMIN, AccessPrivilege.ADMIN);
  
      assertEquals(
        "grouptype has fields before reset",
        1, FieldFinder.findAllByGroupType(type).size()
      );
      RegistryReset.reset();  // fields should be deleted when registry is reset
      assertEquals(
        "grouptype does not have fields after reset",
        0, FieldFinder.findAllByGroupType(type).size()
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
    groupType.setContextId("contextId");
    groupType.setCreateTime(3L);
    groupType.setHibernateVersionNumber(3L);
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

  public void testDeleteGroupWithAttrsAndList() {
    GrouperSession  s     = null;
    String          type  = "customType.TUCL";
    String          name  = "customField.TUCL";
    Privilege       read  = AccessPrivilege.VIEW;
    Privilege       write = AccessPrivilege.UPDATE;
    try {
      s = SessionHelper.getRootSession();
      GroupType custom = GroupType.createType(s, type);
      Field f = custom.addList(s, name, read, write);
      AttributeDefName     attr    = custom.addAttribute(s, "custom a", true);
      Assert.assertTrue("added LIST field", true);
  
      Stem  root  = StemFinder.findRootStem(s);
      Stem  edu   = root.addChildStem("edu", "edu");
      Group g     = edu.addChildGroup("g", "g");
  
      Assert.assertTrue("no custom type", !g.hasType(custom));
  
      g.addType(custom);
      Assert.assertTrue("custom type", g.hasType(custom));
  
      g.addMember(SubjectTestHelper.SUBJ0, f);
      Assert.assertTrue("has member", g.hasMember(SubjectTestHelper.SUBJ0, f));
    
      g.setAttribute(attr.getLegacyAttributeName(true), "blah");
      g.store();
      
      g.delete();
    }
    catch (Exception e) {
      T.e(e);
    }
    finally {
      SessionHelper.stop(s);
    }
  }
}

