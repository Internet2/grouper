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

package edu.internet2.middleware.grouper.group;
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
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
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
    
    assertEquals(GrouperUtil.toStringForLog(groups), 2, groups.size());
    
    assertTrue(groups.contains(group2b));
    assertTrue(groups.contains(group1a));
    
    GrouperSession.stopQuietly(grouperSession);
    
    
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
      Set<Group> groups = GrouperDAOFactory.getFactory().getGroup().getAllGroups();
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
      Set<Group> groups = GrouperDAOFactory.getFactory().getGroup().getAllGroups();
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
  
  }

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    //TestRunner.run(TestGroupFinder.class);
    TestRunner.run(new TestGroupFinder("testFindByAttribute"));
  }

} // public class TestGroupFinder_FindByAttribute

