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

package edu.internet2.middleware.grouper.group;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.Assert;
import junit.textui.TestRunner;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GroupType;
import edu.internet2.middleware.grouper.GroupTypeFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.Stem.Scope;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.app.attestation.GrouperAttestationJob;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.AttributeDefNameSave;
import edu.internet2.middleware.grouper.attr.AttributeDefSave;
import edu.internet2.middleware.grouper.attr.AttributeDefType;
import edu.internet2.middleware.grouper.attr.AttributeDefValueType;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignSave;
import edu.internet2.middleware.grouper.attr.finder.AttributeAssignValueFinder;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefFinder;
import edu.internet2.middleware.grouper.attr.finder.AttributeAssignValueFinder.AttributeAssignValueFinderResult;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefFinder;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.exception.GroupNotFoundException;
import edu.internet2.middleware.grouper.exception.SchemaException;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.R;
import edu.internet2.middleware.grouper.helper.SessionHelper;
import edu.internet2.middleware.grouper.helper.StemHelper;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.AttributeDefPrivilege;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * @author  blair christensen.
 * @version $Id: TestGroupFinder.java,v 1.1 2009-03-20 19:56:41 mchyzer Exp $
 * @since   1.2.0
 */
public class TestGroupFinder extends GrouperTest {

  private static final Log LOG = GrouperUtil.getLog(TestGroupFinder.class);

  public TestGroupFinder(String name) {
    super(name);
  }

  public TestGroupFinder() {
  }
  
  /**
   * 
   */
  public void testChainingGroupsManageMemberships() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();

    GroupType groupType = GroupType.createType(grouperSession, "testType");
    
    Field field = groupType.addList(grouperSession, "test1", AccessPrivilege.VIEW, AccessPrivilege.ADMIN);
    
    //make some groups
    Group group1a = new GroupSave(grouperSession).assignName("test:test1:testa").assignCreateParentStemsIfNotExist(true).save();
    group1a.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.READ);
    group1a.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.VIEW);
    
    Group group1b = new GroupSave(grouperSession).assignName("test:test1:testb").assignCreateParentStemsIfNotExist(true).save();
    group1b.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.READ);
    group1b.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.VIEW);
    
    Group group2a = new GroupSave(grouperSession).assignName("test:test2:testa").assignCreateParentStemsIfNotExist(true).save();
    group2a.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.READ);
    group2a.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.VIEW);
    
    Group group2b = new GroupSave(grouperSession).assignName("test:test2:testb").assignCreateParentStemsIfNotExist(true).save();
    group2b.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.READ);
    group2b.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.VIEW);

    Group group3a = new GroupSave(grouperSession).assignName("test:test3:testa").assignCreateParentStemsIfNotExist(true).save();
    group3a.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.READ);
    group3a.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.VIEW);

    Group group3b= new GroupSave(grouperSession).assignName("test:test3:testb").assignCreateParentStemsIfNotExist(true).save();
    group3b.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.READ);
    group3b.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.VIEW);

    group1a.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.VIEW);
    group1a.addMember(SubjectTestHelper.SUBJ0, false);

    group1b.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.VIEW);

    group2a.addMember(SubjectTestHelper.SUBJ0, false);
    
    group2b.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.VIEW);
    
    group3a.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.VIEW);
    group3a.grantPriv(SubjectTestHelper.SUBJ2, AccessPrivilege.ADMIN);
    group3a.addMember(SubjectTestHelper.SUBJ0, false);
    
    group3b.addType(groupType);
    group3b.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.READ);
    group3b.addMember(SubjectTestHelper.SUBJ0, field, false);

    //subj0 can view and is in group1a and group3b
    GrouperSession.stopQuietly(grouperSession);
    grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
    Set<Group> groups = new GroupFinder().assignPrivileges(AccessPrivilege.VIEW_PRIVILEGES)
        .assignField(Group.getDefaultList()).assignSubject(SubjectTestHelper.SUBJ0)
        .assignQueryOptions(new QueryOptions().paging(1000, 1, false)).findGroups();
    
    assertEquals(GrouperUtil.toStringForLog(groups), 2, groups.size());
    
    assertTrue(groups.contains(group1a));
    assertTrue(groups.contains(group3a));

    //subj0 cant admin and is in group1a and group3b
    GrouperSession.stopQuietly(grouperSession);
    grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
    groups = new GroupFinder().assignPrivileges(AccessPrivilege.MANAGE_PRIVILEGES)
        .assignField(Group.getDefaultList()).assignSubject(SubjectTestHelper.SUBJ0)
        .assignQueryOptions(new QueryOptions().paging(1000, 1, false)).findGroups();
    
    assertEquals(GrouperUtil.toStringForLog(groups), 0, groups.size());
    
    
    //subj2 can read and subj0 is in group3b
    GrouperSession.stopQuietly(grouperSession);
    grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ2);
    groups = new GroupFinder().assignPrivileges(AccessPrivilege.READ_PRIVILEGES)
        .assignSubject(SubjectTestHelper.SUBJ0).assignField(Group.getDefaultList())
        .assignQueryOptions(new QueryOptions().paging(1000, 1, false)).findGroups();
    
    assertEquals(GrouperUtil.toStringForLog(groups), 1, groups.size());
    
    assertTrue(groups.contains(group3a));

    //subj3 cant read and subj0 is in group3b
    GrouperSession.stopQuietly(grouperSession);
    grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ3);
    groups = new GroupFinder().assignPrivileges(AccessPrivilege.READ_PRIVILEGES)
        .assignSubject(SubjectTestHelper.SUBJ0).assignField(Group.getDefaultList())
        .assignQueryOptions(new QueryOptions().paging(1000, 1, false)).findGroups();
    
    assertEquals(GrouperUtil.toStringForLog(groups), 0, groups.size());
    

    
    
    GrouperSession.stopQuietly(grouperSession);
    
    
  }
  
  /**
   * 
   */
  public void testChainingGroupsManage() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    //make some groups
    Group group1a = new GroupSave(grouperSession).assignName("test:test1:testa").assignCreateParentStemsIfNotExist(true).save();
    group1a.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.READ);
    group1a.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.VIEW);
    
    Group group1b = new GroupSave(grouperSession).assignName("test:test1:testb").assignCreateParentStemsIfNotExist(true).save();
    group1b.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.READ);
    group1b.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.VIEW);
    
    Group group2a = new GroupSave(grouperSession).assignName("test:test2:testa").assignCreateParentStemsIfNotExist(true).save();
    group2a.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.READ);
    group2a.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.VIEW);
    
    Group group2b = new GroupSave(grouperSession).assignName("test:test2:testb").assignCreateParentStemsIfNotExist(true).save();
    group2b.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.READ);
    group2b.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.VIEW);

    group1a.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.ADMIN);
    group1a.grantPriv(SubjectTestHelper.SUBJ1, AccessPrivilege.READ);
    group1a.grantPriv(SubjectTestHelper.SUBJ2, AccessPrivilege.VIEW);
    group1a.grantPriv(SubjectTestHelper.SUBJ3, AccessPrivilege.UPDATE);

    group1b.grantPriv(SubjectTestHelper.SUBJ1, AccessPrivilege.ADMIN);
    group1b.grantPriv(SubjectTestHelper.SUBJ2, AccessPrivilege.READ);
    group1b.grantPriv(SubjectTestHelper.SUBJ3, AccessPrivilege.VIEW);
    group1b.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.UPDATE);

    group2a.grantPriv(SubjectTestHelper.SUBJ2, AccessPrivilege.ADMIN);
    group2a.grantPriv(SubjectTestHelper.SUBJ3, AccessPrivilege.READ);
    group2a.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.VIEW);
    group2a.grantPriv(SubjectTestHelper.SUBJ1, AccessPrivilege.UPDATE);

    group2b.grantPriv(SubjectTestHelper.SUBJ3, AccessPrivilege.ADMIN);
    group2b.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.READ);
    group2b.grantPriv(SubjectTestHelper.SUBJ1, AccessPrivilege.VIEW);
    group2b.grantPriv(SubjectTestHelper.SUBJ2, AccessPrivilege.UPDATE);

    //subj1 can admin group1b, but can update other group2a...
    GrouperSession.stopQuietly(grouperSession);
    grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ1);

    Set<Group> groups = new GroupFinder().assignPrivileges(AccessPrivilege.MANAGE_PRIVILEGES)
        .assignQueryOptions(new QueryOptions().paging(1000, 1, false)).findGroups();
    
    assertEquals(GrouperUtil.toStringForLog(groups), 2, groups.size());
    
    assertTrue(groups.contains(group2a));
    assertTrue(groups.contains(group1b));
    
    //subj0 can read group2b, but can admin other group1a...
    GrouperSession.stopQuietly(grouperSession);
    grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
    groups = new GroupFinder().assignPrivileges(AccessPrivilege.READ_PRIVILEGES)
        .assignQueryOptions(new QueryOptions().paging(1000, 1, false)).findGroups();
    groups = GrouperTest.filterOutBuiltInGroups(groups);
    assertEquals(GrouperUtil.toStringForLog(groups), 2, groups.size());
    
    assertTrue(groups.contains(group2b));
    assertTrue(groups.contains(group1a));
    
    GrouperSession.stopQuietly(grouperSession);
    
    
  }

  public void testChainingGroupFinderExcludingAlternateNames() {
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Group group = new GroupSave(grouperSession).assignName("test-a:group-a").assignCreateParentStemsIfNotExist(true).save();
    group.addAlternateName("test-b:group-b");
    group.store();

    Set<Group> foundGroups = new GroupFinder().assignScope("test-a:group%").findGroups();
    assertEquals("find 1 group by name with scope", foundGroups.size(), 1);
    assertContainsGroup(foundGroups, group, "Found correct group by name with scope");

    foundGroups = new GroupFinder().assignScope("test-b:group%").findGroups();
    assertEquals("find 1 group by alternate name with scope", foundGroups.size(), 1);
    assertContainsGroup(foundGroups, group, "Found correct group by name with scope");

    foundGroups = new GroupFinder().assignScope("bogus:%").findGroups();
    assertEquals("find 0 groups with non-existent name with scope", foundGroups.size(), 0);

    /* When excluding alternate name in finder, should find by name but not alternate name */
    foundGroups = new GroupFinder().assignScope("test-a:group%").assignExcludeAlternateNames(true).findGroups();
    assertEquals("find 1 group by name with scope excluding alternate name", foundGroups.size(), 1);
    assertContainsGroup(foundGroups, group, "Found correct group by name with scope excluding alternate name");

    foundGroups = new GroupFinder().assignScope("test-b:group%").assignExcludeAlternateNames(true).findGroups();
    assertEquals("find 0 groups by alternate name with scope excluding alternate name", foundGroups.size(), 0);
  }

  public void testFailToFindGroupByAttributeNullSession() {
    LOG.info("testFailToFindGroupByAttributeNullSession");
    try {
      R.populateRegistry(0, 0, 0);
      GroupFinder.findByAttribute(null, "description", "i2:a:a", true);
      fail("failed to throw IllegalArgumentException");
    }
    catch (IllegalArgumentException eIA) {
      assertTrue(true);
    }
    catch (Exception e) {
      unexpectedException(e);
    }
  } // public void testFailToFindGroupByAttributeNullSession()

  public void testFailToFindGroupByAttributeNullAttribute() {
    LOG.info("testFailToFindGroupByAttributeNullAttribute");
    try {
      R r = R.populateRegistry(0, 0, 0);
      GroupFinder.findByAttribute(r.rs, null, "i2:a:a", true);
      fail("failed to throw IllegalArgumentException");
    }
    catch (IllegalArgumentException eIA) {
      assertTrue(true);
    }
    catch (Exception e) {
      unexpectedException(e);
    }
  } // public void testFailToFindGroupByAttributeNullAttribute()

  public void testFailToFindGroupByAttributeNullAttributeValue() {
    LOG.info("testFailToFindGroupByAttributeNullAttributeValue");
    try {
      R r = R.populateRegistry(0, 0, 0);
      GroupFinder.findByAttribute(r.rs, "description", null, true);
      fail("failed to throw IllegalArgumentException");
    }
    catch (IllegalArgumentException eIA) {
      assertTrue(true);
    }
    catch (Exception e) {
      unexpectedException(e);
    }
  } // public void testFailToFindGroupByAttributeNullAttributeValue()

  public void testFailToFindGroupByAttribute() {
    LOG.info("testFailToFindGroupByAttribute");
    try {
      R r = R.populateRegistry(0, 0, 0);
      assertDoNotFindGroupByAttribute(r.rs, "description", "i2:a:a");
      r.rs.stop();
    }
    catch (Exception e) {
      unexpectedException(e);
    }
  } // public void testFailToFindGroupByAttribute()

  public void testFindGroupByAttribute() {
    LOG.info("testFindGroupByAttribute");
    try {
      R       r   = R.populateRegistry(1, 1, 0);
      Group   gA  = r.getGroup("a", "a");
      String  val = "a unique value";
      gA.setDescription(val);
      gA.store();
      gA = assertFindGroupByAttribute(r.rs, "description", val);
      assertTrue( gA.getDescription().equals(val) );
      r.rs.stop();
    }
    catch (Exception e) {
      unexpectedException(e);
    }
  } // public void testFindGroupByAttribute()

  public void testFailToFindGroupByCustomTypeNotUnique() {
    LOG.info("testFailToFindGroupByCustomTypeNotUnique");
    try {
      R         r     = R.populateRegistry(1, 2, 0);
      Group     gA    = r.getGroup("a", "a");
      Group     gB    = r.getGroup("a", "b");
      GroupType type  = GroupType.createType(r.rs, "custom group type");
      gA.addType(type);
      gB.addType(type);
      assertDoNotFindGroupByType(r.rs, type);
      r.rs.stop();
    }
    catch (Exception e) {
      unexpectedException(e);
    }
  } // public void testFailToFindGroupByCustomTypeNotUnique()

  public void testFailToFindGroupByType() {
    LOG.info("testFailToFindGroupByType");
    try {
      R r = R.populateRegistry(0, 0, 0);
      GroupType.createType(r.rs, "testType");
      assertDoNotFindGroupByType( r.rs, GroupTypeFinder.find("testType", true) );
      r.rs.stop();
    }
    catch (Exception e) {
      unexpectedException(e);
    }
  } // public void testFailToFindGroupByType()

  public void testFailToFindGroupByTypeInvalidType() {
    LOG.info("testFailToFindGroupByTypeInvalidType");
    try {
      R r = R.populateRegistry(0, 0, 0);
      GroupFinder.findAllByType( r.rs, GroupTypeFinder.find("this is an invalid group type", true) );
      fail("failed to throw IllegalArgumentException");
    }
    catch (SchemaException eS) {
      assertTrue(true);
    }
    catch (Exception e) {
      unexpectedException(e);
    }
  } // public void testFailToFindGroupByTypeInvalidType()

  public void testFailToFindGroupByTypeNotUnique() {
    LOG.info("testFailToFindGroupByTypeNotUnique");
    try {
      R         r     = R.populateRegistry(1, 2, 0);
      GroupType testType = GroupType.createType(r.rs, "testType");
      Set<Group> groups = GrouperTest.filterOutBuiltInGroups(
          GrouperDAOFactory.getFactory().getGroup().getAllGroups());
      assertTrue(groups.size() == 2);
      for (Group group : groups) {
        group.addType(testType);
      }
      
      GroupType type  = GroupTypeFinder.find("testType", true);
      assertDoNotFindGroupByType(r.rs, type);
      r.rs.stop();
    }
    catch (Exception e) {
      unexpectedException(e);
    }
  } // public void testFailToFindGroupByTypeNotUnique()

  public void testFailToFindGroupByTypeNullSession() {
    LOG.info("testFailToFindGroupByTypeNullSession");
    try {
      R r = R.populateRegistry(0, 0, 0);
      GroupType.createType(r.rs, "testType");
      GroupFinder.findAllByType( null, GroupTypeFinder.find("testType", true) );
      fail("failed to throw IllegalArgumentException");
    }
    catch (IllegalArgumentException eIA) {
      assertTrue(true);
    }
    catch (Exception e) {
      unexpectedException(e);
    }
  } // public void testFailToFindGroupByTypeNullSession()

  public void testFailToFindGroupByTypeNullType() {
    LOG.info("testFailToFindGroupByTypeNullType");
    try {
      R r = R.populateRegistry(0, 0, 0);
      GroupFinder.findAllByType(r.rs, null);
      fail("failed to throw IllegalArgumentException");
    }
    catch (IllegalArgumentException eIA) {
      assertTrue(true);
    }
    catch (Exception e) {
      unexpectedException(e);
    }
  } // public void testFailToFindGroupByTypeNullType()

  public void testFindGroupByCustomType() {
    LOG.info("testFindGroupByCustomType");
    try {
      R         r     = R.populateRegistry(1, 1, 0);
      Group     gA    = r.getGroup("a", "a");
      GroupType type  = GroupType.createType(r.rs, "custom group type");
      gA.addType(type);
      assertFindGroupByType(r.rs, type);
      r.rs.stop();
    }
    catch (Exception e) {
      unexpectedException(e);
    }
  } // public void testFindGroupByCustomType()

  public void testFindGroupByType() {
    LOG.info("testFindGroupByType");
    try {
      R         r     = R.populateRegistry(1, 1, 0);
      GroupType testType = GroupType.createType(r.rs, "testType");
      Set<Group> groups = GrouperTest.filterOutBuiltInGroups(
          GrouperDAOFactory.getFactory().getGroup().getAllGroups());
      assertTrue(groups.size() == 1);
      for (Group group : groups) {
        group.addType(testType);
      }
      
      GroupType type  = GroupTypeFinder.find("testType", true);
      assertFindGroupByType(r.rs, type);
      r.rs.stop();
    }
    catch (Exception e) {
      unexpectedException(e);
    }
  } // public void testFindGroupByType()

  // Tests
  
  public void testFindByName() {
    GrouperSession  s     = SessionHelper.getRootSession();
    Stem            root  = StemHelper.findRootStem(s);
    Stem            edu   = StemHelper.addChildStem(root, "edu", "educational");
    Group           i2    = StemHelper.addChildGroup(edu, "i2", "internet2");
    try {
      Group found = GroupFinder.findByName(s, i2.getName(), true);
      Assert.assertTrue("found a group", true);
      Assert.assertNotNull("found group !null", found);
      Assert.assertTrue("found instanceof Group", found instanceof Group);
      Assert.assertTrue("i2 equals found", i2.equals(found));
    }
    catch (GroupNotFoundException e) {
      Assert.fail("failed to find group");
    }
  } // public void testFindByName()

  /**
   * 
   */
  public void testFindByIdIndex() {
    GrouperSession  s     = SessionHelper.getRootSession();
    Stem            root  = StemHelper.findRootStem(s);
    Stem            edu   = StemHelper.addChildStem(root, "edu", "educational");
    Group           i2    = StemHelper.addChildGroup(edu, "i2", "internet2");

    i2.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.VIEW);
    i2.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.VIEW);
    i2.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.READ);
    
    GrouperSession.stopQuietly(s);
    
    s = GrouperSession.start(SubjectTestHelper.SUBJ0);

    Group found = GroupFinder.findByIdIndexSecure(i2.getIdIndex(), true, null);
    
    assertEquals(found.getName(), i2.getName());
    
    GrouperSession.stopQuietly(s);
    
    s = GrouperSession.start(SubjectTestHelper.SUBJ1);
    
    found = GroupFinder.findByIdIndexSecure(i2.getIdIndex(), false, null);
    
    assertNull(found);
    
    try {
      GroupFinder.findByIdIndexSecure(i2.getIdIndex(), true, null);
      fail("shouldnt get here");
    } catch (GroupNotFoundException gnfe) {
      //good
    }
    
    try {
      GroupFinder.findByIdIndexSecure(123456789L, true, null);
      fail("shouldnt get here");
    } catch (GroupNotFoundException gnfe) {
      //good
    }
    
    
  } // public void testFindByIdIndex()

  // Tests
  
  public void testFindByUuid() {
    GrouperSession  s     = SessionHelper.getRootSession();
    Stem            root  = StemHelper.findRootStem(s);
    Stem            edu   = StemHelper.addChildStem(root, "edu", "educational");
    Group           i2    = StemHelper.addChildGroup(edu, "i2", "internet2");
    try {
      Group found = GroupFinder.findByUuid(s, i2.getUuid(), true);
      Assert.assertTrue("found a group", true);
      Assert.assertNotNull("found group !null", found);
      Assert.assertTrue("found instanceof Group", found instanceof Group);
      Assert.assertTrue("i2 equals", i2.equals(found));
    }
    catch (GroupNotFoundException e) {
      Assert.fail("failed to find group");
    }
  } // public void testFindByUuid()
  
  /**
   * 
   */
  public void testFindNonByEmptySetNameOrId() {
    GrouperSession  s     = SessionHelper.getRootSession();
    Stem            root  = StemHelper.findRootStem(s);
    Stem            edu   = StemHelper.addChildStem(root, "edu", "educational");
    @SuppressWarnings("unused")
    Group           i2    = StemHelper.addChildGroup(edu, "i2", "internet2");
    
    Set<Group> groups = new GroupFinder().assignGroupIds(new HashSet<String>()).findGroups();
    
    assertEquals(0, groups.size());
    
    groups = new GroupFinder().assignGroupNames(new HashSet<String>()).findGroups();
    
    assertEquals(0, groups.size());
    
  } // public void testFindByUuid()
  

  /**
   * 
   */
  public void testFindByAttributeDefName() {

    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    AttributeDef attributeDef = new AttributeDefSave(grouperSession).assignAttributeDefNameToEdit("test:attrDef")
        .assignAttributeDefType(AttributeDefType.attr).assignCreateParentStemsIfNotExist(true)
        .assignToGroup(true)
        .assignValueType(AttributeDefValueType.string).save();
    
    AttributeDefName attributeDefName = new AttributeDefNameSave(grouperSession, attributeDef)
      .assignAttributeDefNameNameToEdit("test:attrDefName").assignCreateParentStemsIfNotExist(true).save();

    Group group0 = new GroupSave(grouperSession).assignName("test:group0").assignCreateParentStemsIfNotExist(true).save();
    Group group1 = new GroupSave(grouperSession).assignName("test:group1").assignCreateParentStemsIfNotExist(true).save();
    Group group2 = new GroupSave(grouperSession).assignName("test:group2").assignCreateParentStemsIfNotExist(true).save();
    Group group3 = new GroupSave(grouperSession).assignName("test:group3").assignCreateParentStemsIfNotExist(true).save();
    Group group4 = new GroupSave(grouperSession).assignName("test:group4").assignCreateParentStemsIfNotExist(true).save();

    group0.getAttributeValueDelegate().assignValue(attributeDefName.getName(), "abc");
    group1.getAttributeValueDelegate().assignValue(attributeDefName.getName(), "xyz");
    group3.getAttributeValueDelegate().assignValue(attributeDefName.getName(), "abc");
    group4.getAttributeValueDelegate().assignValue(attributeDefName.getName(), "abc");
    
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_READ, false);
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ1, AttributeDefPrivilege.ATTR_READ, false);
    
    group0.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.GROUP_ATTR_READ);
    group1.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.GROUP_ATTR_READ);
    group2.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.GROUP_ATTR_READ);
    group3.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.GROUP_ATTR_READ);
    
    group0.grantPriv(SubjectTestHelper.SUBJ2, AccessPrivilege.GROUP_ATTR_READ);
    group1.grantPriv(SubjectTestHelper.SUBJ2, AccessPrivilege.GROUP_ATTR_READ);
    group2.grantPriv(SubjectTestHelper.SUBJ2, AccessPrivilege.GROUP_ATTR_READ);
    group3.grantPriv(SubjectTestHelper.SUBJ2, AccessPrivilege.GROUP_ATTR_READ);
    group4.grantPriv(SubjectTestHelper.SUBJ2, AccessPrivilege.GROUP_ATTR_READ);
    
    //subj0 can read most of both
    //subj1 can read the attr
    //subj2 can read the group attrs
    
    GrouperSession.stopQuietly(grouperSession);

    grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
    
    List<Group> groups = new ArrayList<Group>(new GroupFinder().assignNameOfAttributeDefName(attributeDefName.getName())
        .assignPrivileges(AccessPrivilege.ATTRIBUTE_READ_PRIVILEGES).findGroups());
    
    assertEquals(3, GrouperUtil.length(groups));
    assertEquals("test:group0", groups.get(0).getName());
    assertEquals("test:group1", groups.get(1).getName());
    assertEquals("test:group3", groups.get(2).getName());
    
    groups = new ArrayList<Group>(new GroupFinder().assignNameOfAttributeDefName(attributeDefName.getName())
        .assignPrivileges(AccessPrivilege.ATTRIBUTE_READ_PRIVILEGES).assignAttributeValue("abc").findGroups());
    
    assertEquals(2, GrouperUtil.length(groups));
    assertEquals("test:group0", groups.get(0).getName());
    assertEquals("test:group3", groups.get(1).getName());
    
    GrouperSession.stopQuietly(grouperSession);
    

    // #####################
    
    grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ1);

    groups = new ArrayList<Group>(new GroupFinder().assignNameOfAttributeDefName(attributeDefName.getName())
        .assignPrivileges(AccessPrivilege.ATTRIBUTE_READ_PRIVILEGES).findGroups());
    
    assertEquals(0, GrouperUtil.length(groups));
    
    groups = new ArrayList<Group>(new GroupFinder().assignNameOfAttributeDefName(attributeDefName.getName())
        .assignPrivileges(AccessPrivilege.ATTRIBUTE_READ_PRIVILEGES).assignAttributeValue("abc").findGroups());
    
    assertEquals(0, GrouperUtil.length(groups));
    
    GrouperSession.stopQuietly(grouperSession);
    
    // #####################
    
    grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ2);

    groups = new ArrayList<Group>(new GroupFinder().assignNameOfAttributeDefName(attributeDefName.getName())
        .assignPrivileges(AccessPrivilege.ATTRIBUTE_READ_PRIVILEGES).findGroups());
    assertEquals(0, GrouperUtil.length(groups));
    
    groups = new ArrayList<Group>(new GroupFinder().assignIdOfAttributeDefName(attributeDefName.getId())
        .assignPrivileges(AccessPrivilege.ATTRIBUTE_READ_PRIVILEGES).findGroups());
    
    assertEquals(0, GrouperUtil.length(groups));
    
    groups = new ArrayList<Group>(new GroupFinder().assignIdOfAttributeDefName(attributeDefName.getId())
        .assignPrivileges(AccessPrivilege.ATTRIBUTE_READ_PRIVILEGES).assignAttributeValue("abc").findGroups());
    
    assertEquals(0, GrouperUtil.length(groups));

    
    GrouperSession.stopQuietly(grouperSession);
    
    
  }
  
  
  /**
   * 
   */
  public void testFindByAttribute() {
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Group group1 = new GroupSave(grouperSession).assignName("test:group1").assignCreateParentStemsIfNotExist(true).save();
    Group group2 = new GroupSave(grouperSession).assignName("test:group2").assignCreateParentStemsIfNotExist(true).save();
    Group group3 = new GroupSave(grouperSession).assignName("test:group3").assignCreateParentStemsIfNotExist(true).save();

    group1.revokePriv(AccessPrivilege.VIEW);
    group1.revokePriv(AccessPrivilege.READ);
    group2.revokePriv(AccessPrivilege.VIEW);
    group2.revokePriv(AccessPrivilege.READ);
    group3.revokePriv(AccessPrivilege.VIEW);
    group3.revokePriv(AccessPrivilege.READ);
    
    GroupType type1 = GroupType.createType(grouperSession, "type1");
    GroupType type2 = GroupType.createType(grouperSession, "type2");
    
    AttributeDefName type1Attr1 = type1.addAttribute(grouperSession, "type1Attr1");
    AttributeDefName type1Attr2 = type1.addAttribute(grouperSession, "type1Attr2");
    AttributeDefName type2Attr1 = type2.addAttribute(grouperSession, "type2Attr1");
    AttributeDefName type2Attr2 = type2.addAttribute(grouperSession, "type2Attr2");
    
    group1.addType(type1);
    group1.addType(type2);
    group2.addType(type2);
    
    group1.setAttribute("type1Attr1", "test value 1");
    group1.setAttribute("type1Attr2", "test value 2");
    group1.setAttribute("type2Attr2", "test value 3");
    group2.setAttribute("type2Attr2", "test value 4");
    
    Group group = GroupFinder.findByAttribute(grouperSession, "type1Attr1", "test value 1");
    assertEquals(group1.getName(), group.getName());
    
    group = GroupFinder.findByAttribute(grouperSession, "type1Attr2", "test value 2");
    assertEquals(group1.getName(), group.getName());
    
    group = GroupFinder.findByAttribute(grouperSession, "type2Attr2", "test value 3");
    assertEquals(group1.getName(), group.getName());
    
    group = GroupFinder.findByAttribute(grouperSession, "type2Attr2", "test value 4");
    assertEquals(group2.getName(), group.getName());
    
    group = GroupFinder.findByAttribute(grouperSession, "extension", "group1");
    assertEquals(group1.getName(), group.getName());
    
    group = GroupFinder.findByAttribute(grouperSession, "extension", "group2");
    assertEquals(group2.getName(), group.getName());
    
    group = GroupFinder.findByAttribute(grouperSession, "extension", "group3");
    assertEquals(group3.getName(), group.getName());
    
    group1.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.GROUP_ATTR_READ);
    group2.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.VIEW);
    type1.getAttributeDefName().getAttributeDef().getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_READ, false);
    type1.internal_getAttributeDefForAttributes().getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_READ, false);
    
    grouperSession.stop();
    grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);

    // some queries should work
    group = GroupFinder.findByAttribute(grouperSession, "type1Attr1", "test value 1");
    assertEquals(group1.getName(), group.getName());
    
    group = GroupFinder.findByAttribute(grouperSession, "etc:legacy:attribute:legacyAttribute_type1Attr1", "test value 1");
    assertEquals(group1.getName(), group.getName());
    
    group = GroupFinder.findByAttribute(grouperSession, "type1Attr2", "test value 2");
    assertEquals(group1.getName(), group.getName());
    
    group = GroupFinder.findByAttribute(grouperSession, "type2Attr2", "test value 3", false);
    assertNull(group);
    
    group = GroupFinder.findByAttribute(grouperSession, "type2Attr2", "test value 4", false);
    assertNull(group);
    
    group = GroupFinder.findByAttribute(grouperSession, "extension", "group1");
    assertEquals(group1.getName(), group.getName());
    
    group = GroupFinder.findByAttribute(grouperSession, "extension", "group2");
    assertEquals(group2.getName(), group.getName());
    
    group = GroupFinder.findByAttribute(grouperSession, "extension", "group3", false);
    assertNull(group);
  }
  
  /**
   * @see GrouperTest#setupConfigs
   */
  @Override
  protected void setupConfigs() {
    super.setupConfigs();
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.wheel.use", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("stem.validateExtensionByDefault", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("group.validateExtensionByDefault", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("attributeDef.validateExtensionByDefault", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("attributeDefName.validateExtensionByDefault", "false");

  }

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    //TestRunner.run(TestGroupFinder.class);
    TestRunner.run(new TestGroupFinder("testChainingGroupFinderExcludingAlternateNames"));
  }

  /**
   * 
   */
  public void testFlashCachePrivs() {

    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.create.grant.all.read", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.create.grant.all.view", "false");

    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    // this group has attestation
    Group groupA = new GroupSave(grouperSession).assignName("testA:groupA").assignCreateParentStemsIfNotExist(true).save();

    AttributeDef attributeDefA = new AttributeDefSave(grouperSession).assignName("testA:attributeDefA")
        .assignCreateParentStemsIfNotExist(true).assignAttributeDefType(AttributeDefType.attr)
        .assignToGroup(true).assignValueType(AttributeDefValueType.string).save();

    AttributeDefName attributeDefNameA = new AttributeDefNameSave(grouperSession, attributeDefA)
      .assignName("testA:attributeDefA").save();

    Group group = null;
    AttributeDef attributeDef = null;
    AttributeDefName attributeDefName = null;

    group = GroupFinder.findByName(grouperSession, groupA.getName(), false);
    attributeDef = AttributeDefFinder.findByName(attributeDefA.getName(), false);
    attributeDefName = AttributeDefNameFinder.findByName(attributeDefNameA.getName(), false);

    
    GrouperSession.stopQuietly(grouperSession);
    
    grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
    
    int mistakes = 0;
    
    group = GroupFinder.findByName(grouperSession, groupA.getName(), false);
    if (group != null) {
      mistakes++;
    }
    //assertNull(group);
    group = GroupFinder.findByName(grouperSession, groupA.getName(), false);
    if (group != null) {
      mistakes++;
    }
    //assertNull(group);

    group = GroupFinder.findByUuid(grouperSession, groupA.getId(), false);
    if (group != null) {
      mistakes++;
    }
    //assertNull(group);
    group = GroupFinder.findByUuid(grouperSession, groupA.getId(), false);
    if (group != null) {
      mistakes++;
    }
    //assertNull(group);

    group = GroupFinder.findByIdIndexSecure(groupA.getIdIndex(), false, null);
    if (group != null) {
      mistakes++;
    }
    //assertNull(group);
    group = GroupFinder.findByIdIndexSecure(groupA.getIdIndex(), false, null);
    if (group != null) {
      mistakes++;
    }
    //assertNull(group);

    attributeDef = AttributeDefFinder.findByName(attributeDefA.getName(), false);
    if (attributeDef != null) {
      mistakes++;
    }
    //assertNull(attributeDef);
    attributeDef = AttributeDefFinder.findByName(attributeDefA.getName(), false);
    if (attributeDef != null) {
      mistakes++;
    }
    //assertNull(attributeDef);

    attributeDef = AttributeDefFinder.findById(attributeDefA.getId(), false);
    if (attributeDef != null) {
      mistakes++;
    }
    //assertNull(attributeDef);
    attributeDef = AttributeDefFinder.findById(attributeDefA.getId(), false);
    if (attributeDef != null) {
      mistakes++;
    }
    //assertNull(attributeDef);

    attributeDef = AttributeDefFinder.findByIdIndexSecure(attributeDefA.getIdIndex(), false, null);
    if (attributeDef != null) {
      mistakes++;
    }
    //assertNull(attributeDef);
    attributeDef = AttributeDefFinder.findByIdIndexSecure(attributeDefA.getIdIndex(), false, null);
    if (attributeDef != null) {
      mistakes++;
    }
    //assertNull(attributeDef);

    attributeDefName = AttributeDefNameFinder.findByName(attributeDefNameA.getName(), false);
    if (attributeDefName != null) {
      mistakes++;
    }
    //assertNull(attributeDefName);
    attributeDefName = AttributeDefNameFinder.findByName(attributeDefNameA.getName(), false);
    if (attributeDefName != null) {
      mistakes++;
    }
    //assertNull(attributeDefName);
    
    attributeDefName = AttributeDefNameFinder.findById(attributeDefNameA.getId(), false);
    if (attributeDefName != null) {
      mistakes++;
    }
    //assertNull(attributeDefName);
    attributeDefName = AttributeDefNameFinder.findById(attributeDefNameA.getId(), false);
    if (attributeDefName != null) {
      mistakes++;
    }
    //assertNull(attributeDefName);

    attributeDefName = AttributeDefNameFinder.findByIdIndexSecure(attributeDefNameA.getIdIndex(), false, null);
    if (attributeDefName != null) {
      mistakes++;
    }
    //assertNull(attributeDefName);
    attributeDefName = AttributeDefNameFinder.findByIdIndexSecure(attributeDefNameA.getIdIndex(), false, null);
    if (attributeDefName != null) {
      mistakes++;
    }
    //assertNull(attributeDefName);

    assertEquals(0, mistakes);

    
  }
  
  /**
   * 
   */
  public void testFindByAttributeAssignOnAssignValuesAndPrivilege() {
    
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.create.grant.all.read", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.create.grant.all.view", "false");

    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    AttributeDef attributeDef = AttributeDefFinder.findByName("etc:attribute:attestation:attestationValueDef", true);
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_ADMIN, false);
    
    attributeDef = AttributeDefFinder.findByName("etc:attribute:attestation:attestationDef", true);
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_ADMIN, false);
    
    // this group has attestation
    Group groupA = new GroupSave(grouperSession).assignName("testA:groupA").assignCreateParentStemsIfNotExist(true).save();
    
    {
      Group group = groupA;
      
      AttributeAssign attributeAssignBase = new AttributeAssignSave(grouperSession).assignOwnerGroup(group)
          .assignAttributeDefName(GrouperAttestationJob.retrieveAttributeDefNameValueDef()).save();
      
      attributeAssignBase.getAttributeValueDelegate().assignValueString(
          GrouperAttestationJob.retrieveAttributeDefNameCalculatedDaysLeft().getName(), "0");
      attributeAssignBase.getAttributeValueDelegate().assignValueString(
          GrouperAttestationJob.retrieveAttributeDefNameDirectAssignment().getName(), "true");
      attributeAssignBase.getAttributeValueDelegate().assignValueString(
          GrouperAttestationJob.retrieveAttributeDefNameSendEmail().getName(), "false");

      group.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.ADMIN);
      group.grantPriv(SubjectTestHelper.SUBJ1, AccessPrivilege.UPDATE);
      group.grantPriv(SubjectTestHelper.SUBJ1, AccessPrivilege.READ);
      group.grantPriv(SubjectTestHelper.SUBJ2, AccessPrivilege.READ);
      group.grantPriv(SubjectTestHelper.SUBJ3, AccessPrivilege.VIEW);
      
    }
    
    // this group does not have attestation
    Group groupB = new GroupSave(grouperSession).assignName("testB:groupB").assignCreateParentStemsIfNotExist(true).save();

    
    {
      Group group = groupB;

      group.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.ADMIN);
      group.grantPriv(SubjectTestHelper.SUBJ1, AccessPrivilege.UPDATE);
      group.grantPriv(SubjectTestHelper.SUBJ1, AccessPrivilege.READ);
      group.grantPriv(SubjectTestHelper.SUBJ2, AccessPrivilege.READ);
      group.grantPriv(SubjectTestHelper.SUBJ3, AccessPrivilege.VIEW);
      
    }

    // this group has attestation
    Group groupC = new GroupSave(grouperSession).assignName("testC:groupC").assignCreateParentStemsIfNotExist(true).save();

    
    {
      Group group = groupC;
      
      AttributeAssign attributeAssignBase = new AttributeAssignSave(grouperSession).assignOwnerGroup(group)
          .assignAttributeDefName(GrouperAttestationJob.retrieveAttributeDefNameValueDef()).save();
      
      attributeAssignBase.getAttributeValueDelegate().assignValueString(
          GrouperAttestationJob.retrieveAttributeDefNameCalculatedDaysLeft().getName(), "2");
      
      Calendar calendar = new GregorianCalendar();
      calendar.setTime(new Date());
      calendar.add(Calendar.DAY_OF_YEAR, -180 + 2);
      String dateString = new SimpleDateFormat("yyyy/MM/dd").format(calendar.getTime());
      
      attributeAssignBase.getAttributeValueDelegate().assignValueString(
          GrouperAttestationJob.retrieveAttributeDefNameDateCertified().getName(), dateString);
      attributeAssignBase.getAttributeValueDelegate().assignValueString(
          GrouperAttestationJob.retrieveAttributeDefNameDirectAssignment().getName(), "true");
      attributeAssignBase.getAttributeValueDelegate().assignValueString(
          GrouperAttestationJob.retrieveAttributeDefNameSendEmail().getName(), "false");

      group.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.ADMIN);
      group.grantPriv(SubjectTestHelper.SUBJ1, AccessPrivilege.UPDATE);
      group.grantPriv(SubjectTestHelper.SUBJ1, AccessPrivilege.READ);
      group.grantPriv(SubjectTestHelper.SUBJ2, AccessPrivilege.READ);
      group.grantPriv(SubjectTestHelper.SUBJ3, AccessPrivilege.VIEW);
      
    }

    // this group has attestation and no privs
    Group groupD = new GroupSave(grouperSession).assignName("testD:groupD").assignCreateParentStemsIfNotExist(true).save();

    
    {
      Group group = groupD;
      
      AttributeAssign attributeAssignBase = new AttributeAssignSave(grouperSession).assignOwnerGroup(group)
          .assignAttributeDefName(GrouperAttestationJob.retrieveAttributeDefNameValueDef()).save();
      
      attributeAssignBase.getAttributeValueDelegate().assignValueString(
          GrouperAttestationJob.retrieveAttributeDefNameCalculatedDaysLeft().getName(), "5");

      Calendar calendar = new GregorianCalendar();
      calendar.setTime(new Date());
      calendar.add(Calendar.DAY_OF_YEAR, -180+5);
      String dateString = new SimpleDateFormat("yyyy/MM/dd").format(calendar.getTime());
      
      attributeAssignBase.getAttributeValueDelegate().assignValueString(
          GrouperAttestationJob.retrieveAttributeDefNameDateCertified().getName(), dateString);
      attributeAssignBase.getAttributeValueDelegate().assignValueString(
          GrouperAttestationJob.retrieveAttributeDefNameDirectAssignment().getName(), "true");
      attributeAssignBase.getAttributeValueDelegate().assignValueString(
          GrouperAttestationJob.retrieveAttributeDefNameSendEmail().getName(), "false");


      
    }

    // this group has attestation and privs with the wrong value to search for
    Group groupE = new GroupSave(grouperSession).assignName("testE:groupE").assignCreateParentStemsIfNotExist(true).save();

    
    {
      Group group = groupE;
      
      AttributeAssign attributeAssignBase = new AttributeAssignSave(grouperSession).assignOwnerGroup(group)
          .assignAttributeDefName(GrouperAttestationJob.retrieveAttributeDefNameValueDef()).save();
      
      attributeAssignBase.getAttributeValueDelegate().assignValueString(
          GrouperAttestationJob.retrieveAttributeDefNameCalculatedDaysLeft().getName(), "180");

      Calendar calendar = new GregorianCalendar();
      calendar.setTime(new Date());
      calendar.add(Calendar.DAY_OF_YEAR, -180 + 180);
      String dateString = new SimpleDateFormat("yyyy/MM/dd").format(calendar.getTime());
      
      attributeAssignBase.getAttributeValueDelegate().assignValueString(
          GrouperAttestationJob.retrieveAttributeDefNameDateCertified().getName(), dateString);
      attributeAssignBase.getAttributeValueDelegate().assignValueString(
          GrouperAttestationJob.retrieveAttributeDefNameDirectAssignment().getName(), "true");
      attributeAssignBase.getAttributeValueDelegate().assignValueString(
          GrouperAttestationJob.retrieveAttributeDefNameSendEmail().getName(), "false");


      group.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.ADMIN);
      group.grantPriv(SubjectTestHelper.SUBJ1, AccessPrivilege.UPDATE);
      group.grantPriv(SubjectTestHelper.SUBJ1, AccessPrivilege.READ);
      group.grantPriv(SubjectTestHelper.SUBJ2, AccessPrivilege.READ);
      group.grantPriv(SubjectTestHelper.SUBJ3, AccessPrivilege.VIEW);
      
    }

    GrouperSession.stopQuietly(grouperSession);
    grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);

    //make sure attribute def names can be found unsecurely
    AttributeDefName attributeDefName = new AttributeDefNameFinder()
        .addIdOfAttributeDefName(GrouperAttestationJob.retrieveAttributeDefNameCalculatedDaysLeft().getId()).findAttributeName();

    assertNotNull(attributeDefName);
    assertNotNull(attributeDefName.getAttributeDef());

    GrouperSession.stopQuietly(grouperSession);
    grouperSession = GrouperSession.startRootSession();
    
    Set<Group> groups = null;

    //do a search as grouper system
    groups = new GroupFinder().assignPrivileges(AccessPrivilege.UPDATE_PRIVILEGES)
      .assignIdOfAttributeDefName(GrouperAttestationJob.retrieveAttributeDefNameCalculatedDaysLeft().getId())
      .assignAttributeValuesOnAssignment(GrouperAttestationJob.TWO_WEEKS_DAYS_LEFT)
      .findGroups();

    assertContainsGroups(GrouperUtil.toSet(groupA, groupC, groupD), groups, null);

    //try in a folder
    groups = new GroupFinder().assignPrivileges(AccessPrivilege.UPDATE_PRIVILEGES)
      .assignParentStemId(groupA.getParentUuid())
      .assignStemScope(Scope.SUB)
      .assignIdOfAttributeDefName(GrouperAttestationJob.retrieveAttributeDefNameCalculatedDaysLeft().getId())
      .assignAttributeValuesOnAssignment(GrouperAttestationJob.TWO_WEEKS_DAYS_LEFT)
      .findGroups();

    assertContainsGroups(GrouperUtil.toSet(groupA), groups, null);
    
    GrouperSession.stopQuietly(grouperSession);
    grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
    
    //try SUBJ 0 has admin
    groups = new GroupFinder().assignPrivileges(AccessPrivilege.UPDATE_PRIVILEGES)
      .assignIdOfAttributeDefName(GrouperAttestationJob.retrieveAttributeDefNameCalculatedDaysLeft().getId())
      .assignAttributeValuesOnAssignment(GrouperAttestationJob.TWO_WEEKS_DAYS_LEFT)
      .findGroups();

    assertContainsGroups(null, groups, null);

    groups = new GroupFinder().assignPrivileges(AccessPrivilege.UPDATE_PRIVILEGES)
        .assignIdOfAttributeDefName(GrouperAttestationJob.retrieveAttributeDefNameCalculatedDaysLeft().getId())
        .assignAttributeValuesOnAssignment(GrouperAttestationJob.TWO_WEEKS_DAYS_LEFT)
        .assignAttributeCheckReadOnAttributeDef(false)
        .findGroups();

    assertContainsGroups(GrouperUtil.toSet(groupA, groupC), groups, null);

    
    GrouperSession.stopQuietly(grouperSession);
    grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ1);
    
    //try SUBJ 1 has update/read
    groups = new GroupFinder().assignPrivileges(AccessPrivilege.UPDATE_PRIVILEGES)
      .assignIdOfAttributeDefName(GrouperAttestationJob.retrieveAttributeDefNameCalculatedDaysLeft().getId())
      .assignAttributeValuesOnAssignment(GrouperAttestationJob.TWO_WEEKS_DAYS_LEFT)
      .assignAttributeCheckReadOnAttributeDef(false)
      .findGroups();

    assertContainsGroups(GrouperUtil.toSet(groupA, groupC), groups, null);

    GrouperSession.stopQuietly(grouperSession);
    grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ2);
    
    //try SUBJ 2 has read
    groups = new GroupFinder().assignPrivileges(AccessPrivilege.UPDATE_PRIVILEGES)
      .assignIdOfAttributeDefName(GrouperAttestationJob.retrieveAttributeDefNameCalculatedDaysLeft().getId())
      .assignAttributeValuesOnAssignment(GrouperAttestationJob.TWO_WEEKS_DAYS_LEFT)
      .assignAttributeCheckReadOnAttributeDef(false)
      .findGroups();

    assertContainsGroups(null, groups, null);

    groups = new GroupFinder().assignPrivileges(AccessPrivilege.READ_PRIVILEGES)
        .assignIdOfAttributeDefName(GrouperAttestationJob.retrieveAttributeDefNameCalculatedDaysLeft().getId())
        .assignAttributeValuesOnAssignment(GrouperAttestationJob.TWO_WEEKS_DAYS_LEFT)
        .assignAttributeCheckReadOnAttributeDef(false)
        .findGroups();

    assertContainsGroups(GrouperUtil.toSet(groupA, groupC), groups, null);
    
    GrouperSession.stopQuietly(grouperSession);
    grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ3);
    
    //try SUBJ 3 has view
    groups = new GroupFinder().assignPrivileges(AccessPrivilege.UPDATE_PRIVILEGES)
      .assignIdOfAttributeDefName(GrouperAttestationJob.retrieveAttributeDefNameCalculatedDaysLeft().getId())
      .assignAttributeValuesOnAssignment(GrouperAttestationJob.TWO_WEEKS_DAYS_LEFT)
      .assignAttributeCheckReadOnAttributeDef(false)
      .findGroups();

    assertContainsGroups(null, groups, null);

    groups = new GroupFinder().assignPrivileges(AccessPrivilege.READ_PRIVILEGES)
        .assignIdOfAttributeDefName(GrouperAttestationJob.retrieveAttributeDefNameCalculatedDaysLeft().getId())
        .assignAttributeValuesOnAssignment(GrouperAttestationJob.TWO_WEEKS_DAYS_LEFT)
        .assignAttributeCheckReadOnAttributeDef(false)
        .findGroups();

    assertContainsGroups(null, groups, null);

    GrouperSession.stopQuietly(grouperSession);
    grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ4);
    
    //try SUBJ 4 has nothing
    groups = new GroupFinder().assignPrivileges(AccessPrivilege.UPDATE_PRIVILEGES)
      .assignIdOfAttributeDefName(GrouperAttestationJob.retrieveAttributeDefNameCalculatedDaysLeft().getId())
      .assignAttributeValuesOnAssignment(GrouperAttestationJob.TWO_WEEKS_DAYS_LEFT)
      .assignAttributeCheckReadOnAttributeDef(false)
      .findGroups();

    assertContainsGroups(null, groups, null);
    
    groups = new GroupFinder().assignPrivileges(AccessPrivilege.READ_PRIVILEGES)
        .assignIdOfAttributeDefName(GrouperAttestationJob.retrieveAttributeDefNameCalculatedDaysLeft().getId())
        .assignAttributeValuesOnAssignment(GrouperAttestationJob.TWO_WEEKS_DAYS_LEFT)
        .assignAttributeCheckReadOnAttributeDef(false)
        .findGroups();

    assertContainsGroups(null, groups, null);
      
    //test value finder
    //try SUBJ 2 has read
    GrouperSession.stopQuietly(grouperSession);
    grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);

    groups = new GroupFinder().assignPrivileges(AccessPrivilege.UPDATE_PRIVILEGES)
        .assignIdOfAttributeDefName(GrouperAttestationJob.retrieveAttributeDefNameCalculatedDaysLeft().getId())
        .assignAttributeValuesOnAssignment(GrouperAttestationJob.TWO_WEEKS_DAYS_LEFT)
        .assignAttributeCheckReadOnAttributeDef(false)
        .findGroups();

    assertContainsGroups(GrouperUtil.toSet(groupA, groupC), groups, null);

    
    AttributeAssignValueFinderResult attributeAssignValueFinderResult = new AttributeAssignValueFinder().assignOwnerGroupsOfAssignAssign(groups)
        .addAttributeDefNameId(GrouperAttestationJob.retrieveAttributeDefNameValueDef().getId())
        .assignAttributeCheckReadOnAttributeDef(false)
        .findAttributeAssignValuesResult();
      
    //now we have groups, assignments, assignments on assignments, and values
    Map<String, String> attributes = attributeAssignValueFinderResult.retrieveAttributeDefNamesAndValueStrings(groupA.getId());
        
    String calculatedDaysLeft = attributes.get(
      GrouperAttestationJob.retrieveAttributeDefNameCalculatedDaysLeft().getName());

    assertEquals("0", calculatedDaysLeft);
    
    attributes = attributeAssignValueFinderResult.retrieveAttributeDefNamesAndValueStrings(groupC.getId());
    
    calculatedDaysLeft = attributes.get(
      GrouperAttestationJob.retrieveAttributeDefNameCalculatedDaysLeft().getName());

    assertEquals("2", calculatedDaysLeft);

    assertNotNull(attributeAssignValueFinderResult.getMapOwnerIdToAttributeAssign().get(groupA.getId()));
    assertNotNull(attributeAssignValueFinderResult.retrieveAttributeAssignOnAssign(groupA.getId(), GrouperAttestationJob.retrieveAttributeDefNameCalculatedDaysLeft().getName()));
    
    GrouperSession.stopQuietly(grouperSession);
    grouperSession = GrouperSession.startRootSession();
    
    //see if subject0 can see groups in a stem
    Stem stemA = new StemSave(grouperSession).assignName("testA:stemA").assignCreateParentStemsIfNotExist(true).save();
    
    {
      Stem stem = stemA;
      
      AttributeAssign attributeAssignBase = new AttributeAssignSave(grouperSession).assignOwnerStem(stem)
          .assignAttributeDefName(GrouperAttestationJob.retrieveAttributeDefNameValueDef()).save();
      
      attributeAssignBase.getAttributeValueDelegate().assignValueString(
          GrouperAttestationJob.retrieveAttributeDefNameHasAttestation().getName(), "true");

      attributeAssignBase.getAttributeValueDelegate().assignValueString(
          GrouperAttestationJob.retrieveAttributeDefNameSendEmail().getName(), "false");


      stem.grantPriv(SubjectTestHelper.SUBJ0, NamingPrivilege.STEM_ADMIN);

    }

    Set<Stem> stems = new StemFinder().assignPrivileges(NamingPrivilege.ADMIN_PRIVILEGES)
        .assignIdOfAttributeDefName(GrouperAttestationJob.retrieveAttributeDefNameSendEmail().getId())
        .assignAttributeValuesOnAssignment(GrouperUtil.toSetObjectType("true", "false"))
        .assignAttributeCheckReadOnAttributeDef(false).assignQueryOptions(QueryOptions.create(null, null, 1, 150))
        .findStems();
    assertContainsStems(GrouperUtil.toSet(stemA), stems, null);

    GrouperSession.stopQuietly(grouperSession);
    grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);

    stems = new StemFinder().assignPrivileges(NamingPrivilege.ADMIN_PRIVILEGES)
        .assignIdOfAttributeDefName(GrouperAttestationJob.retrieveAttributeDefNameSendEmail().getId())
        .assignAttributeValuesOnAssignment(GrouperUtil.toSetObjectType("true", "false"))
        .assignAttributeCheckReadOnAttributeDef(false).assignQueryOptions(QueryOptions.create(null, null, 1, 150))
        .findStems();
    assertContainsStems(GrouperUtil.toSet(stemA), stems, null);
    
    stems = new StemFinder().assignPrivileges(NamingPrivilege.ADMIN_PRIVILEGES)
        .assignParentStemId(stemA.getParentUuid())
        .assignStemScope(Scope.SUB)
        .assignIdOfAttributeDefName(GrouperAttestationJob.retrieveAttributeDefNameSendEmail().getId())
        .assignAttributeValuesOnAssignment(GrouperUtil.toSetObjectType("true", "false"))
        .assignAttributeCheckReadOnAttributeDef(false).assignQueryOptions(QueryOptions.create(null, null, 1, 150))
        .findStems();
    assertContainsStems(GrouperUtil.toSet(stemA), stems, null);
    
    // this group has attestation and privs with the wrong value to search for
    Group stemAGroupAA = new GroupSave(grouperSession).assignName("testA:stemA:groupAA").assignCreateParentStemsIfNotExist(true).save();
    Group stemAGroupAB = new GroupSave(grouperSession).assignName("testA:stemA:groupAB").assignCreateParentStemsIfNotExist(true).save();
    Group stemAGroupAC = new GroupSave(grouperSession).assignName("testA:stemA:groupAC").assignCreateParentStemsIfNotExist(true).save();

    for (Group group : new Group[]{stemAGroupAA, stemAGroupAB, stemAGroupAC}) {
      group.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.ADMIN, false);
      group.grantPriv(SubjectTestHelper.SUBJ1, AccessPrivilege.UPDATE, false);
      group.grantPriv(SubjectTestHelper.SUBJ1, AccessPrivilege.READ, false);
      group.grantPriv(SubjectTestHelper.SUBJ2, AccessPrivilege.READ, false);
      group.grantPriv(SubjectTestHelper.SUBJ3, AccessPrivilege.VIEW, false);

    }

    grouperSession = GrouperSession.startRootSession();
    GrouperAttestationJob.runDaemonStandalone();
    GrouperSession.stopQuietly(grouperSession);

  }
  
} // public class TestGroupFinder_FindByAttribute

