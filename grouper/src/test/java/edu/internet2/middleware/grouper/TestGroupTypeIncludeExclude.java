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
import junit.textui.TestRunner;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.exception.GroupNotFoundException;
import edu.internet2.middleware.grouper.exception.SchemaException;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.hibernate.AuditControl;
import edu.internet2.middleware.grouper.hibernate.GrouperRollbackType;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.hibernate.HibernateHandler;
import edu.internet2.middleware.grouper.hibernate.HibernateHandlerBean;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.misc.CompositeType;
import edu.internet2.middleware.grouper.misc.GrouperStartup;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * @author  mchyzer
 * @version $Id: TestGroupTypeIncludeExclude.java,v 1.9 2009-03-24 17:12:09 mchyzer Exp $
 */
public class TestGroupTypeIncludeExclude extends GrouperTest {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new TestGroupTypeIncludeExclude("testRequireGroup"));
    //TestRunner.run(TestGroupTypeIncludeExclude.class);
  }
  
  /**
   * 
   */
  @SuppressWarnings("unused")
  private static final Log LOG = GrouperUtil.getLog(TestGroupTypeIncludeExclude.class);

  /**
   * 
   * @param name
   */
  public TestGroupTypeIncludeExclude(String name) {
    super(name);
  }

  /**
   * 
   * @see junit.framework.TestCase#setUp()
   */
  protected void setUp () {
    try {
      //dont precreate this type
      GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperIncludeExclude.use", "false");
      GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperIncludeExclude.requireGroups.use", "false");
      super.setUp();
      
      this.grouperSession = GrouperSession.startRootSession();

      setupTestConfigForIncludeExclude();

      String groupTypeName = GrouperConfig.getProperty("grouperIncludeExclude.type.name");

      String requireInGroupsTypeName = GrouperConfig.getProperty("grouperIncludeExclude.requireGroups.type.name");

      //shouldnt exist
      try {
        GroupTypeFinder.find(groupTypeName, true);
        fail("Shouldnt exist now");
      } catch (SchemaException se) {
        //good
      }

      try {
        GroupTypeFinder.find(requireInGroupsTypeName, true);
        fail("Shouldnt exist now");
      } catch (SchemaException se) {
        //good
      }

      try {
        GroupTypeFinder.find("requireInActiveStudent", true);
        fail("Shouldnt exist now");
      } catch (SchemaException se) {
        //good
      }

      GrouperStartup.initIncludeExcludeType();
      
      this.includeExcludeType = GroupTypeFinder.find(groupTypeName, true);
      
      assertNotNull("Should exist now", this.includeExcludeType);
      
      this.requireGroupsType = GroupTypeFinder.find(requireInGroupsTypeName, true);
      
      assertNotNull("Should exist now", this.requireGroupsType);

      this.requireActiveStudentType = GroupTypeFinder.find("requireActiveStudent", true);
      
      assertNotNull("Should exist now", this.requireGroupsType);
      
      this.requireActiveEmployee = AttributeDefNameFinder.findByName("etc:legacy:attribute:legacyAttribute_requireActiveEmployee", true);
      
      assertNotNull("Should exist now", this.requireActiveEmployee);

    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /** include exclude type */
  private GroupType includeExcludeType;
  
  /** require groups type */
  private GroupType requireGroupsType;
  
  /** require active student type */
  private GroupType requireActiveStudentType;
  
  /** require active employee attribute */
  private AttributeDefName requireActiveEmployee;
  
  /**
   * 
   */
  private GrouperSession grouperSession = null;
  
  /**
   * @throws Exception 
   */
  public void testIncludeExcludeOverall() throws Exception {
    includeExcludeHelper(true);
  }
  
  /**
   * @throws Exception 
   */
  public void testIncludeExcludeSystemOfRecord() throws Exception {
    includeExcludeHelper(false);
  }
  
  /**
   * test transaction
   * @throws Exception 
   */
  public void testTransaction() throws Exception {
    
    //make a stem and a group
    String overallName = "aStem:aGroup";

    Group aGroup = Group.saveGroup(this.grouperSession, null, null, overallName, null, null, null, true);

    //lets start by adding first five to the main group
    aGroup.addMember(SubjectTestHelper.SUBJ0);
    aGroup.addMember(SubjectTestHelper.SUBJ1);
    aGroup.addMember(SubjectTestHelper.SUBJ2);
    aGroup.addMember(SubjectTestHelper.SUBJ3);
    aGroup.addMember(SubjectTestHelper.SUBJ4);


    Group systemOfRecordGroup = null;

    String systemOfRecordIdPath = overallName + "_systemOfRecord";
    
    systemOfRecordGroup = GroupFinder.findByName(this.grouperSession, systemOfRecordIdPath, false);
    assertNull("Should not find this group yet", systemOfRecordGroup);
    
    final Group GROUP1 = aGroup;

    HibernateSession.callbackHibernateSession(
        GrouperTransactionType.READ_WRITE_NEW, AuditControl.WILL_NOT_AUDIT, new HibernateHandler() {

      public Object callback(HibernateHandlerBean hibernateHandlerBean)
          throws GrouperDAOException {
        HibernateSession hibernateSession = hibernateHandlerBean.getHibernateSession();
        try {
          GROUP1.addType(TestGroupTypeIncludeExclude.this.includeExcludeType);
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
        hibernateSession.rollback(GrouperRollbackType.ROLLBACK_NOW);
        return null;
        
      }
      
    });
    systemOfRecordGroup = GroupFinder.findByName(this.grouperSession, systemOfRecordIdPath, false);
    assertNull("Should not find this group yet", systemOfRecordGroup);
    
    //refresh this group object since it thinks it has the type already
    aGroup = GroupFinder.findByName(this.grouperSession, overallName, false);
    
    final Group GROUP2 = aGroup;
    HibernateSession.callbackHibernateSession(
        GrouperTransactionType.READ_WRITE_NEW, AuditControl.WILL_NOT_AUDIT, new HibernateHandler() {

      public Object callback(HibernateHandlerBean hibernateHandlerBean)
          throws GrouperDAOException {
        try {
          GROUP2.addType(includeExcludeType);
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
        //this time let it commit
        return null;
        
      }
      
    });
    systemOfRecordGroup = GroupFinder.findByName(this.grouperSession, systemOfRecordIdPath, false);
    assertNotNull("Should find this group now", systemOfRecordGroup);

    
  }
  
  /**
   * @param startWithOverallGroup true for overall and false for system of record
   * @throws Exception 
   */
  private void includeExcludeHelper(boolean startWithOverallGroup) throws Exception {
    
    //make a stem and a group
    String overallName = "aStem:aGroup";
    String aGroupName = startWithOverallGroup ? overallName : (overallName + "_systemOfRecord");
  
    Group aGroup = Group.saveGroup(this.grouperSession, null, null, aGroupName, null, null, null, true);
  
    //lets start by adding first five to the main group
    aGroup.addMember(SubjectTestHelper.SUBJ0);
    aGroup.addMember(SubjectTestHelper.SUBJ1);
    aGroup.addMember(SubjectTestHelper.SUBJ2);
    aGroup.addMember(SubjectTestHelper.SUBJ3);
    aGroup.addMember(SubjectTestHelper.SUBJ4);
  
  
    Group systemOfRecordGroup = null;
  
    String systemOfRecordIdPath = overallName + "_systemOfRecord";
    
    try {
      systemOfRecordGroup = GroupFinder.findByName(this.grouperSession, startWithOverallGroup ? systemOfRecordIdPath : overallName, true);
      fail("Should not find this group yet");
    } catch (GroupNotFoundException gnfe) {
      //good
    }
  
    aGroup.addType(includeExcludeType);
    
    //refresh this
    Group overallGroup = GroupFinder.findByName(this.grouperSession, overallName, true);
    
    //check the system of record group
    systemOfRecordGroup = GroupFinder.findByName(this.grouperSession, systemOfRecordIdPath, true);
    
    assertNotNull("Should exist now", systemOfRecordGroup);
    
    //make sure name and description are all good for system of record
    if (startWithOverallGroup) {
      assertEquals(overallGroup.getDisplayName() + " system of record", systemOfRecordGroup.getDisplayName());
      assertTrue("Should contain", systemOfRecordGroup.getDescription().toLowerCase().contains("system of record"));
    }
    
    //check the includes group
    Group includesGroup = GroupFinder.findByName(this.grouperSession, overallName + "_includes", true);
    {
      
      assertNotNull("Should exist now", includesGroup);
      
      //make sure name and description are all good for system of record
      assertEquals(overallGroup.getDisplayName() + " includes", includesGroup.getDisplayName());
      assertTrue("Should contain", includesGroup.getDescription().toLowerCase().contains("list of includes"));
    }
    
    //check the excludes group
    Group excludesGroup = GroupFinder.findByName(this.grouperSession, overallName + "_excludes", true);
    {
      
      assertNotNull("Should exist now", excludesGroup);
      
      //make sure name and description are all good for system of record
      assertEquals(overallGroup.getDisplayName() + " excludes", excludesGroup.getDisplayName());
      assertTrue("Should contain", excludesGroup.getDescription().toLowerCase().contains("list of excludes"));
    }      
  
    //check the excludes group
    Group systemOfRecordAndIncludesGroup = GroupFinder.findByName(this.grouperSession, overallName + "_systemOfRecordAndIncludes", true);
    {
      
      assertNotNull("Should exist now", systemOfRecordAndIncludesGroup);
      
      //make sure name and description are all good for system of record
      assertEquals(overallGroup.getDisplayName() + " system of record and includes", systemOfRecordAndIncludesGroup.getDisplayName());
      assertTrue("Should contain", systemOfRecordAndIncludesGroup.getDescription().toLowerCase().contains("group math for the include and exclude lists"));
      
      assertTrue("should have system of record as member", systemOfRecordAndIncludesGroup.hasImmediateMember(SubjectFinder.findById(systemOfRecordGroup.getUuid(), true)));
      assertTrue("should have includes as member", systemOfRecordAndIncludesGroup.hasImmediateMember(SubjectFinder.findById(includesGroup.getUuid(), true)));
    }      
  
    //check the overall group
    assertTrue(overallGroup.getComposite(true).getType().equals(CompositeType.COMPLEMENT));
    assertEquals(overallGroup.getComposite(true).getLeftGroup(), systemOfRecordAndIncludesGroup);
    assertEquals(overallGroup.getComposite(true).getRightGroup(), excludesGroup);
    
    //now lets add some to the include
    includesGroup.addMember(SubjectTestHelper.SUBJ4);
    includesGroup.addMember(SubjectTestHelper.SUBJ5);
    includesGroup.addMember(SubjectTestHelper.SUBJ6);
  
    //lets remove some from the exclude
    excludesGroup.addMember(SubjectTestHelper.SUBJ3);
    excludesGroup.addMember(SubjectTestHelper.SUBJ5);
  
    //and add one to the system of record
    systemOfRecordGroup.addMember(SubjectTestHelper.SUBJ7);
    
    //and lets test the overall membership
    assertTrue(overallGroup.hasMember(SubjectTestHelper.SUBJ0));
    assertTrue(overallGroup.hasMember(SubjectTestHelper.SUBJ1));
    assertTrue(overallGroup.hasMember(SubjectTestHelper.SUBJ2));
    assertFalse(overallGroup.hasMember(SubjectTestHelper.SUBJ3));
    assertTrue(overallGroup.hasMember(SubjectTestHelper.SUBJ4));
    assertFalse(overallGroup.hasMember(SubjectTestHelper.SUBJ5));
    assertTrue(overallGroup.hasMember(SubjectTestHelper.SUBJ6));
    assertTrue(overallGroup.hasMember(SubjectTestHelper.SUBJ7));

    aGroup = GroupFinder.findByName(grouperSession, aGroup.getName(), true);

    aGroup.delete();

  }

  /**
   * @throws Exception 
   */
  public void testRequireGroup1() throws Exception {
    //make a stem and a group
    String overallName = "aStem:aGroup";
  
    Group aGroup = Group.saveGroup(this.grouperSession, null, null, overallName, null, null, null, true);
  
    Group activeStudentGroup = Group.saveGroup(this.grouperSession, "aStem:activeStudent", null, "aStem:activeStudent", null, null, null, true);
  
    Group activeEmployeeGroup = Group.saveGroup(this.grouperSession, "aStem:activeEmployee", null, "aStem:activeEmployee", null, null, null, true);
  
    Group anotherGroup = Group.saveGroup(this.grouperSession, "aStem:anotherGroup", null, "aStem:anotherGroup", null, null, null, true);
  
    Group yetAnotherGroup = Group.saveGroup(this.grouperSession, "aStem:yetAnotherGroup", null, "aStem:yetAnotherGroup", null, null, null, true);
  
    //lets start by adding first five to the main group
    aGroup.addMember(SubjectTestHelper.SUBJ0);
    aGroup.addMember(SubjectTestHelper.SUBJ1);
    aGroup.addMember(SubjectTestHelper.SUBJ2);
    aGroup.addMember(SubjectTestHelper.SUBJ3);
    aGroup.addMember(SubjectTestHelper.SUBJ4);
  
    activeStudentGroup.addMember(SubjectTestHelper.SUBJ0);
    activeStudentGroup.addMember(SubjectTestHelper.SUBJ1);
    activeStudentGroup.addMember(SubjectTestHelper.SUBJ2);
    activeStudentGroup.addMember(SubjectTestHelper.SUBJ3);
    
    activeEmployeeGroup.addMember(SubjectTestHelper.SUBJ0);
    activeEmployeeGroup.addMember(SubjectTestHelper.SUBJ1);
    activeEmployeeGroup.addMember(SubjectTestHelper.SUBJ2);
    activeEmployeeGroup.addMember(SubjectTestHelper.SUBJ4);
    
    anotherGroup.addMember(SubjectTestHelper.SUBJ0);
    anotherGroup.addMember(SubjectTestHelper.SUBJ1);
    anotherGroup.addMember(SubjectTestHelper.SUBJ3);
    anotherGroup.addMember(SubjectTestHelper.SUBJ4);
    
    yetAnotherGroup.addMember(SubjectTestHelper.SUBJ0);
    yetAnotherGroup.addMember(SubjectTestHelper.SUBJ2);
    yetAnotherGroup.addMember(SubjectTestHelper.SUBJ3);
    yetAnotherGroup.addMember(SubjectTestHelper.SUBJ4);
    
    Group systemOfRecordGroup = null;
  
    String systemOfRecordIdPath = overallName + "_systemOfRecord";
    
    systemOfRecordGroup = GroupFinder.findByName(this.grouperSession, systemOfRecordIdPath, false);
    assertNull("Should not find this group yet", systemOfRecordGroup);
  
    //call the method
    aGroup.addType(this.requireActiveStudentType);
  
    //refresh this
    Group overallGroup = GroupFinder.findByName(this.grouperSession, overallName, true);
    //get the system of record
    systemOfRecordGroup = GroupFinder.findByName(this.grouperSession, systemOfRecordIdPath, true);
    
    Group requireGroups1 = GroupFinder.findByName(this.grouperSession, overallName + "_requireGroups1", false);
    Group requireGroups2 = GroupFinder.findByName(this.grouperSession, overallName + "_requireGroups2", false);
    Group requireGroups3 = GroupFinder.findByName(this.grouperSession, overallName + "_requireGroups3", false);
    Group requireGroups4 = GroupFinder.findByName(this.grouperSession, overallName + "_requireGroups4", false);
    Group requireGroups5 = GroupFinder.findByName(this.grouperSession, overallName + "_requireGroups5", false);
    Group includesGroup = GroupFinder.findByName(this.grouperSession, overallName + "_includes", false);
    Group excludesGroup = GroupFinder.findByName(this.grouperSession, overallName + "_excludes", false);
    Group systemOfRecordAndIncludesGroup = GroupFinder.findByName(this.grouperSession, overallName + "_systemOfRecordAndIncludes", false);
    Group includesMinusExcludes = GroupFinder.findByName(this.grouperSession, overallName + "_includesMinusExcludes", false);
  
    assertNull(requireGroups1);
    assertNull(requireGroups2);
    assertNull(requireGroups3);
    assertNull(requireGroups4);
    assertNull(requireGroups5);
    assertNull(includesGroup);
    assertNull(excludesGroup);
    assertNull(systemOfRecordAndIncludesGroup);
    assertNull(includesMinusExcludes);
    
    assertTrue(overallGroup.getComposite(true).getType().equals(CompositeType.INTERSECTION));
    assertEquals(overallGroup.getComposite(true).getLeftGroup(), systemOfRecordGroup);
    assertEquals(overallGroup.getComposite(true).getRightGroup(), activeStudentGroup);
  
    //and lets test the overall membership
    assertTrue(overallGroup.hasMember(SubjectTestHelper.SUBJ0));
    assertTrue(overallGroup.hasMember(SubjectTestHelper.SUBJ1));
    assertTrue(overallGroup.hasMember(SubjectTestHelper.SUBJ2));
    assertTrue(overallGroup.hasMember(SubjectTestHelper.SUBJ3));
    assertFalse(overallGroup.hasMember(SubjectTestHelper.SUBJ4));
  
    assertTrue(systemOfRecordGroup.hasMember(SubjectTestHelper.SUBJ0));
    assertTrue(systemOfRecordGroup.hasMember(SubjectTestHelper.SUBJ1));
    assertTrue(systemOfRecordGroup.hasMember(SubjectTestHelper.SUBJ2));
    assertTrue(systemOfRecordGroup.hasMember(SubjectTestHelper.SUBJ3));
    assertTrue(systemOfRecordGroup.hasMember(SubjectTestHelper.SUBJ4));
    
    assertEquals(5, systemOfRecordGroup.getMembers().size());
  
    //add an attributes, first one, then two others
    aGroup.addType(this.requireGroupsType);
    aGroup.setAttribute("requireActiveEmployee", "true");
    aGroup.setAttribute("requireAlsoInGroups", "aStem:anotherGroup,aStem:yetAnotherGroup");
  
    //requireGroups3 is composite complement: systemOfRecord complement aStem:activeEmployee
    //requireGroups2 is composite complement: requireGroups3 complement aStem:activeStudent
    //requireGroups1 is composite complement: requireGroups2 complement aStem:anotherGroup
    //overallGroup is composite complement: requireGroups1 complement aStem:yetAnotherGroup
    requireGroups1 = GroupFinder.findByName(this.grouperSession, overallName + "_requireGroups1", false);
    requireGroups2 = GroupFinder.findByName(this.grouperSession, overallName + "_requireGroups2", false);
    requireGroups3 = GroupFinder.findByName(this.grouperSession, overallName + "_requireGroups3", false);
    requireGroups4 = GroupFinder.findByName(this.grouperSession, overallName + "_requireGroups4", false);
    includesGroup = GroupFinder.findByName(this.grouperSession, overallName + "_includes", false);
    excludesGroup = GroupFinder.findByName(this.grouperSession, overallName + "_excludes", false);
    systemOfRecordAndIncludesGroup = GroupFinder.findByName(this.grouperSession, overallName + "_systemOfRecordAndIncludes", false);
    includesMinusExcludes = GroupFinder.findByName(this.grouperSession, overallName + "_includesMinusExcludes", false);
  
    assertNotNull(requireGroups1);
    assertNotNull(requireGroups2);
    assertNotNull(requireGroups3);
    assertNull(requireGroups4);
    assertNull(includesGroup);
    assertNull(excludesGroup);
    assertNull(systemOfRecordAndIncludesGroup);
    assertNull(includesMinusExcludes);
  
    //check the requireGroups3 group
    assertTrue(requireGroups3.getComposite(true).getType().equals(CompositeType.INTERSECTION));
    assertEquals(requireGroups3.getComposite(true).getLeftGroup(), systemOfRecordGroup);
    assertEquals(requireGroups3.getComposite(true).getRightGroup(), activeEmployeeGroup);
  
    assertTrue(requireGroups3.hasMember(SubjectTestHelper.SUBJ0));
    assertTrue(requireGroups3.hasMember(SubjectTestHelper.SUBJ1));
    assertTrue(requireGroups3.hasMember(SubjectTestHelper.SUBJ2));
    assertFalse(requireGroups3.hasMember(SubjectTestHelper.SUBJ3));
    assertTrue(requireGroups3.hasMember(SubjectTestHelper.SUBJ4));
  
    //check the requireGroups2 group
    assertTrue(requireGroups2.getComposite(true).getType().equals(CompositeType.INTERSECTION));
    assertEquals(requireGroups2.getComposite(true).getLeftGroup(), requireGroups3);
    assertEquals(requireGroups2.getComposite(true).getRightGroup(), activeStudentGroup);
  
    assertTrue(requireGroups2.hasMember(SubjectTestHelper.SUBJ0));
    assertTrue(requireGroups2.hasMember(SubjectTestHelper.SUBJ1));
    assertTrue(requireGroups2.hasMember(SubjectTestHelper.SUBJ2));
    assertFalse(requireGroups2.hasMember(SubjectTestHelper.SUBJ3));
    assertFalse(requireGroups2.hasMember(SubjectTestHelper.SUBJ4));
  
    //check the requireGroups1 group
    assertTrue(requireGroups1.getComposite(true).getType().equals(CompositeType.INTERSECTION));
    assertEquals(requireGroups1.getComposite(true).getLeftGroup(), requireGroups2);
    assertEquals(requireGroups1.getComposite(true).getRightGroup(), anotherGroup);
  
    assertTrue(requireGroups1.hasMember(SubjectTestHelper.SUBJ0));
    assertTrue(requireGroups1.hasMember(SubjectTestHelper.SUBJ1));
    assertFalse(requireGroups1.hasMember(SubjectTestHelper.SUBJ2));
    assertFalse(requireGroups1.hasMember(SubjectTestHelper.SUBJ3));
    assertFalse(requireGroups1.hasMember(SubjectTestHelper.SUBJ4));
  
    //check the overall group
    assertTrue(overallGroup.getComposite(true).getType().equals(CompositeType.INTERSECTION));
    assertEquals(overallGroup.getComposite(true).getLeftGroup(), requireGroups1);
    assertEquals(overallGroup.getComposite(true).getRightGroup(), yetAnotherGroup);
  
    assertTrue(overallGroup.hasMember(SubjectTestHelper.SUBJ0));
    assertFalse(overallGroup.hasMember(SubjectTestHelper.SUBJ1));
    assertFalse(overallGroup.hasMember(SubjectTestHelper.SUBJ2));
    assertFalse(overallGroup.hasMember(SubjectTestHelper.SUBJ3));
    assertFalse(overallGroup.hasMember(SubjectTestHelper.SUBJ4));
  
    //###################################
    //## remove active student
    //call the method
    aGroup.deleteType(this.requireActiveStudentType);
  
    //requireGroups2 is composite complement: systemOfRecord complement aStem:activeStudent
    //requireGroups1 is composite complement: requireGroups2 complement aStem:anotherGroup
    //overallGroup is composite complement: requireGroups1 complement aStem:yetAnotherGroup
    requireGroups1 = GroupFinder.findByName(this.grouperSession, overallName + "_requireGroups1", false);
    requireGroups2 = GroupFinder.findByName(this.grouperSession, overallName + "_requireGroups2", false);
    requireGroups3 = GroupFinder.findByName(this.grouperSession, overallName + "_requireGroups3", false);
    requireGroups4 = GroupFinder.findByName(this.grouperSession, overallName + "_requireGroups4", false);
    includesGroup = GroupFinder.findByName(this.grouperSession, overallName + "_includes", false);
    excludesGroup = GroupFinder.findByName(this.grouperSession, overallName + "_excludes", false);
    systemOfRecordAndIncludesGroup = GroupFinder.findByName(this.grouperSession, overallName + "_systemOfRecordAndIncludes", false);
    includesMinusExcludes = GroupFinder.findByName(this.grouperSession, overallName + "_includesMinusExcludes", false);
  
    assertNotNull(requireGroups1);
    assertNotNull(requireGroups2);
    assertNull(requireGroups3);
    assertNull(requireGroups4);
    assertNull(includesGroup);
    assertNull(excludesGroup);
    assertNull(systemOfRecordAndIncludesGroup);
    assertNull(includesMinusExcludes);
  
    //check the requireGroups2 group
    assertTrue(requireGroups2.getComposite(true).getType().equals(CompositeType.INTERSECTION));
    assertEquals(requireGroups2.getComposite(true).getLeftGroup(), systemOfRecordGroup);
    assertEquals(requireGroups2.getComposite(true).getRightGroup(), activeEmployeeGroup);
  
    
    anotherGroup = GroupFinder.findByName(this.grouperSession, "aStem:anotherGroup", true);
    assertTrue(anotherGroup.hasMember(SubjectTestHelper.SUBJ0));
    assertTrue(anotherGroup.hasMember(SubjectTestHelper.SUBJ1));
    assertTrue(anotherGroup.hasMember(SubjectTestHelper.SUBJ3));
    assertTrue(anotherGroup.hasMember(SubjectTestHelper.SUBJ4));

    requireGroups2 = GroupFinder.findByName(this.grouperSession, overallName + "_requireGroups2", false);
    assertTrue(requireGroups2.hasMember(SubjectTestHelper.SUBJ0));
    assertTrue(requireGroups2.hasMember(SubjectTestHelper.SUBJ1));
    assertTrue(requireGroups2.hasMember(SubjectTestHelper.SUBJ2));
    assertFalse(requireGroups2.hasMember(SubjectTestHelper.SUBJ3));
    assertTrue(requireGroups2.hasMember(SubjectTestHelper.SUBJ4));
  
    //check the requireGroups1 group
    assertTrue(requireGroups1.getComposite(true).getType().equals(CompositeType.INTERSECTION));
    assertEquals(requireGroups1.getComposite(true).getLeftGroup(), requireGroups2);
    assertEquals(requireGroups1.getComposite(true).getRightGroup(), anotherGroup);
  
    assertTrue(requireGroups1.hasMember(SubjectTestHelper.SUBJ0));
    assertTrue(requireGroups1.hasMember(SubjectTestHelper.SUBJ1));
    assertFalse(requireGroups1.hasMember(SubjectTestHelper.SUBJ2));
    assertFalse(requireGroups1.hasMember(SubjectTestHelper.SUBJ3));
    assertTrue(requireGroups1.hasMember(SubjectTestHelper.SUBJ4));
  
    //check the overall group
    assertTrue(overallGroup.getComposite(true).getType().equals(CompositeType.INTERSECTION));
    assertEquals(overallGroup.getComposite(true).getLeftGroup(), requireGroups1);
    assertEquals(overallGroup.getComposite(true).getRightGroup(), yetAnotherGroup);
  
    assertTrue(overallGroup.hasMember(SubjectTestHelper.SUBJ0));
    assertFalse(overallGroup.hasMember(SubjectTestHelper.SUBJ1));
    assertFalse(overallGroup.hasMember(SubjectTestHelper.SUBJ2));
    assertFalse(overallGroup.hasMember(SubjectTestHelper.SUBJ3));
    assertTrue(overallGroup.hasMember(SubjectTestHelper.SUBJ4));
  
    //#################################
    //## delete other type
    //call the method
    aGroup.deleteType(this.requireGroupsType);
  
    //overallGroup has one member: systemOfRecord
    requireGroups1 = GroupFinder.findByName(this.grouperSession, overallName + "_requireGroups1", false);
    requireGroups2 = GroupFinder.findByName(this.grouperSession, overallName + "_requireGroups2", false);
    requireGroups3 = GroupFinder.findByName(this.grouperSession, overallName + "_requireGroups3", false);
    requireGroups4 = GroupFinder.findByName(this.grouperSession, overallName + "_requireGroups4", false);
    includesGroup = GroupFinder.findByName(this.grouperSession, overallName + "_includes", false);
    excludesGroup = GroupFinder.findByName(this.grouperSession, overallName + "_excludes", false);
    systemOfRecordAndIncludesGroup = GroupFinder.findByName(this.grouperSession, overallName + "_systemOfRecordAndIncludes", false);
    includesMinusExcludes = GroupFinder.findByName(this.grouperSession, overallName + "_includesMinusExcludes", false);
  
    assertNull(requireGroups1);
    assertNull(requireGroups2);
    assertNull(requireGroups3);
    assertNull(requireGroups4);
    assertNull(includesGroup);
    assertNull(excludesGroup);
    assertNull(systemOfRecordAndIncludesGroup);
    assertNull(includesMinusExcludes);
  
    //check the overall group
    assertEquals(overallGroup.getImmediateMembers().size(), 1);
    assertTrue(overallGroup.hasImmediateMember(SubjectFinder.findById(systemOfRecordGroup.getUuid(), true)));
  
    assertTrue(overallGroup.hasMember(SubjectTestHelper.SUBJ0));
    assertTrue(overallGroup.hasMember(SubjectTestHelper.SUBJ1));
    assertTrue(overallGroup.hasMember(SubjectTestHelper.SUBJ2));
    assertTrue(overallGroup.hasMember(SubjectTestHelper.SUBJ3));
    assertTrue(overallGroup.hasMember(SubjectTestHelper.SUBJ4));
    
  }

  /**
   * @throws Exception 
   */
  public void testRequireGroup2() throws Exception {
    //make a stem and a group
    String overallName = "aStem:aGroup";

    Group aGroup = Group.saveGroup(this.grouperSession, null, null, overallName, null, null, null, true);

    Group activeStudentGroup = Group.saveGroup(this.grouperSession, "aStem:activeStudent", null, "aStem:activeStudent", null, null, null, true);

    Group activeEmployeeGroup = Group.saveGroup(this.grouperSession, "aStem:activeEmployee", null, "aStem:activeEmployee", null, null, null, true);

    Group anotherGroup = Group.saveGroup(this.grouperSession, "aStem:anotherGroup", null, "aStem:anotherGroup", null, null, null, true);

    Group yetAnotherGroup = Group.saveGroup(this.grouperSession, "aStem:yetAnotherGroup", null, "aStem:yetAnotherGroup", null, null, null, true);

    //lets start by adding first five to the main group
    aGroup.addMember(SubjectTestHelper.SUBJ0);
    aGroup.addMember(SubjectTestHelper.SUBJ1);
    aGroup.addMember(SubjectTestHelper.SUBJ2);
    aGroup.addMember(SubjectTestHelper.SUBJ3);
    aGroup.addMember(SubjectTestHelper.SUBJ4);

    activeStudentGroup.addMember(SubjectTestHelper.SUBJ0);
    activeStudentGroup.addMember(SubjectTestHelper.SUBJ1);
    activeStudentGroup.addMember(SubjectTestHelper.SUBJ2);
    activeStudentGroup.addMember(SubjectTestHelper.SUBJ3);
    activeStudentGroup.addMember(SubjectTestHelper.SUBJ5);
    
    activeEmployeeGroup.addMember(SubjectTestHelper.SUBJ0);
    activeEmployeeGroup.addMember(SubjectTestHelper.SUBJ1);
    activeEmployeeGroup.addMember(SubjectTestHelper.SUBJ2);
    activeEmployeeGroup.addMember(SubjectTestHelper.SUBJ4);
    activeEmployeeGroup.addMember(SubjectTestHelper.SUBJ5);
    
    anotherGroup.addMember(SubjectTestHelper.SUBJ0);
    anotherGroup.addMember(SubjectTestHelper.SUBJ1);
    anotherGroup.addMember(SubjectTestHelper.SUBJ3);
    anotherGroup.addMember(SubjectTestHelper.SUBJ4);
    anotherGroup.addMember(SubjectTestHelper.SUBJ5);
    
    yetAnotherGroup.addMember(SubjectTestHelper.SUBJ0);
    yetAnotherGroup.addMember(SubjectTestHelper.SUBJ2);
    yetAnotherGroup.addMember(SubjectTestHelper.SUBJ3);
    yetAnotherGroup.addMember(SubjectTestHelper.SUBJ4);
    yetAnotherGroup.addMember(SubjectTestHelper.SUBJ5);
    
    Group systemOfRecordGroup = null;

    String systemOfRecordIdPath = overallName + "_systemOfRecord";
    
    systemOfRecordGroup = GroupFinder.findByName(this.grouperSession, systemOfRecordIdPath, false);
    assertNull("Should not find this group yet", systemOfRecordGroup);

    //call the method
    aGroup.addType(this.requireActiveStudentType);

    //refresh this
    Group overallGroup = GroupFinder.findByName(this.grouperSession, overallName, true);
    //get the system of record
    systemOfRecordGroup = GroupFinder.findByName(this.grouperSession, systemOfRecordIdPath, true);
    
    Group requireGroups1 = GroupFinder.findByName(this.grouperSession, overallName + "_requireGroups1", false);
    Group requireGroups2 = GroupFinder.findByName(this.grouperSession, overallName + "_requireGroups2", false);
    Group requireGroups3 = GroupFinder.findByName(this.grouperSession, overallName + "_requireGroups3", false);
    Group requireGroups4 = GroupFinder.findByName(this.grouperSession, overallName + "_requireGroups4", false);
    Group requireGroups5 = GroupFinder.findByName(this.grouperSession, overallName + "_requireGroups5", false);
    Group includesGroup = GroupFinder.findByName(this.grouperSession, overallName + "_includes", false);
    Group excludesGroup = GroupFinder.findByName(this.grouperSession, overallName + "_excludes", false);
    Group systemOfRecordAndIncludesGroup = GroupFinder.findByName(this.grouperSession, overallName + "_systemOfRecordAndIncludes", false);
    Group includesMinusExcludes = GroupFinder.findByName(this.grouperSession, overallName + "_includesMinusExcludes", false);

    assertNull(requireGroups1);
    assertNull(requireGroups2);
    assertNull(requireGroups3);
    assertNull(requireGroups4);
    assertNull(requireGroups5);
    assertNull(includesGroup);
    assertNull(excludesGroup);
    assertNull(systemOfRecordAndIncludesGroup);
    assertNull(includesMinusExcludes);
    
    assertTrue(overallGroup.getComposite(true).getType().equals(CompositeType.INTERSECTION));
    assertEquals(overallGroup.getComposite(true).getLeftGroup(), systemOfRecordGroup);
    assertEquals(overallGroup.getComposite(true).getRightGroup(), activeStudentGroup);

    //and lets test the overall membership
    assertTrue(overallGroup.hasMember(SubjectTestHelper.SUBJ0));
    assertTrue(overallGroup.hasMember(SubjectTestHelper.SUBJ1));
    assertTrue(overallGroup.hasMember(SubjectTestHelper.SUBJ2));
    assertTrue(overallGroup.hasMember(SubjectTestHelper.SUBJ3));
    assertFalse(overallGroup.hasMember(SubjectTestHelper.SUBJ4));
    assertFalse(overallGroup.hasMember(SubjectTestHelper.SUBJ5));
    assertFalse(overallGroup.hasMember(SubjectTestHelper.SUBJ6));

    assertTrue(systemOfRecordGroup.hasMember(SubjectTestHelper.SUBJ0));
    assertTrue(systemOfRecordGroup.hasMember(SubjectTestHelper.SUBJ1));
    assertTrue(systemOfRecordGroup.hasMember(SubjectTestHelper.SUBJ2));
    assertTrue(systemOfRecordGroup.hasMember(SubjectTestHelper.SUBJ3));
    assertTrue(systemOfRecordGroup.hasMember(SubjectTestHelper.SUBJ4));
    assertFalse(systemOfRecordGroup.hasMember(SubjectTestHelper.SUBJ5));
    assertFalse(systemOfRecordGroup.hasMember(SubjectTestHelper.SUBJ6));
    
    assertEquals(5, systemOfRecordGroup.getMembers().size());

    //add an attributes, first one, then two others
    aGroup.addType(this.requireGroupsType);
    aGroup.setAttribute("requireActiveEmployee", "true");
    aGroup.setAttribute("requireAlsoInGroups", "aStem:anotherGroup,aStem:yetAnotherGroup");


    //requireGroups3 is composite complement: systemOfRecord complement aStem:activeEmployee
    //requireGroups2 is composite complement: requireGroups3 complement aStem:activeStudent
    //requireGroups1 is composite complement: requireGroups2 complement aStem:anotherGroup
    //overallGroup is composite complement: requireGroups1 complement aStem:yetAnotherGroup
    overallGroup = GroupFinder.findByName(this.grouperSession, overallName, true);
    systemOfRecordGroup = GroupFinder.findByName(this.grouperSession, systemOfRecordIdPath, true);
    requireGroups1 = GroupFinder.findByName(this.grouperSession, overallName + "_requireGroups1", false);
    requireGroups2 = GroupFinder.findByName(this.grouperSession, overallName + "_requireGroups2", false);
    requireGroups3 = GroupFinder.findByName(this.grouperSession, overallName + "_requireGroups3", false);
    requireGroups4 = GroupFinder.findByName(this.grouperSession, overallName + "_requireGroups4", false);
    includesGroup = GroupFinder.findByName(this.grouperSession, overallName + "_includes", false);
    excludesGroup = GroupFinder.findByName(this.grouperSession, overallName + "_excludes", false);
    systemOfRecordAndIncludesGroup = GroupFinder.findByName(this.grouperSession, overallName + "_systemOfRecordAndIncludes", false);
    includesMinusExcludes = GroupFinder.findByName(this.grouperSession, overallName + "_includesMinusExcludes", false);
    
    assertNotNull(requireGroups1);
    assertNotNull(requireGroups2);
    assertNotNull(requireGroups3);
    assertNull(requireGroups4);
    assertNull(includesGroup);
    assertNull(excludesGroup);
    assertNull(systemOfRecordAndIncludesGroup);
    assertNull(includesMinusExcludes);

    //check the requireGroups3 group
    assertTrue(requireGroups3.getComposite(true).getType().equals(CompositeType.INTERSECTION));
    assertEquals(requireGroups3.getComposite(true).getLeftGroup(), systemOfRecordGroup);
    assertEquals(requireGroups3.getComposite(true).getRightGroup(), activeEmployeeGroup);

    assertTrue(requireGroups3.hasMember(SubjectTestHelper.SUBJ0));
    assertTrue(requireGroups3.hasMember(SubjectTestHelper.SUBJ1));
    assertTrue(requireGroups3.hasMember(SubjectTestHelper.SUBJ2));
    assertFalse(requireGroups3.hasMember(SubjectTestHelper.SUBJ3));
    assertTrue(requireGroups3.hasMember(SubjectTestHelper.SUBJ4));
    assertFalse(requireGroups3.hasMember(SubjectTestHelper.SUBJ5));
    assertFalse(requireGroups3.hasMember(SubjectTestHelper.SUBJ6));

    //check the requireGroups2 group
    assertTrue(requireGroups2.getComposite(true).getType().equals(CompositeType.INTERSECTION));
    assertEquals(requireGroups2.getComposite(true).getLeftGroup(), requireGroups3);
    assertEquals(requireGroups2.getComposite(true).getRightGroup(), activeStudentGroup);

    assertTrue(requireGroups2.hasMember(SubjectTestHelper.SUBJ0));
    assertTrue(requireGroups2.hasMember(SubjectTestHelper.SUBJ1));
    assertTrue(requireGroups2.hasMember(SubjectTestHelper.SUBJ2));
    assertFalse(requireGroups2.hasMember(SubjectTestHelper.SUBJ3));
    assertFalse(requireGroups2.hasMember(SubjectTestHelper.SUBJ4));
    assertFalse(requireGroups2.hasMember(SubjectTestHelper.SUBJ5));
    assertFalse(requireGroups2.hasMember(SubjectTestHelper.SUBJ6));

    //check the requireGroups1 group
    assertTrue(requireGroups1.getComposite(true).getType().equals(CompositeType.INTERSECTION));
    assertEquals(requireGroups1.getComposite(true).getLeftGroup(), requireGroups2);
    assertEquals(requireGroups1.getComposite(true).getRightGroup(), anotherGroup);

    assertTrue(requireGroups1.hasMember(SubjectTestHelper.SUBJ0));
    assertTrue(requireGroups1.hasMember(SubjectTestHelper.SUBJ1));
    assertFalse(requireGroups1.hasMember(SubjectTestHelper.SUBJ2));
    assertFalse(requireGroups1.hasMember(SubjectTestHelper.SUBJ3));
    assertFalse(requireGroups1.hasMember(SubjectTestHelper.SUBJ4));
    assertFalse(requireGroups1.hasMember(SubjectTestHelper.SUBJ5));
    assertFalse(requireGroups1.hasMember(SubjectTestHelper.SUBJ6));

    //check the overall group
    assertTrue(overallGroup.getComposite(true).getType().equals(CompositeType.INTERSECTION));
    assertEquals(overallGroup.getComposite(true).getLeftGroup(), requireGroups1);
    assertEquals(overallGroup.getComposite(true).getRightGroup(), yetAnotherGroup);

    assertTrue(overallGroup.hasMember(SubjectTestHelper.SUBJ0));
    assertFalse(overallGroup.hasMember(SubjectTestHelper.SUBJ1));
    assertFalse(overallGroup.hasMember(SubjectTestHelper.SUBJ2));
    assertFalse(overallGroup.hasMember(SubjectTestHelper.SUBJ3));
    assertFalse(overallGroup.hasMember(SubjectTestHelper.SUBJ4));
    assertFalse(overallGroup.hasMember(SubjectTestHelper.SUBJ5));
    assertFalse(overallGroup.hasMember(SubjectTestHelper.SUBJ6));

    //###################################
    //## remove active student
    //call the method
    aGroup.deleteType(this.requireActiveStudentType);

    //requireGroups2 is composite complement: systemOfRecord complement aStem:activeStudent
    //requireGroups1 is composite complement: requireGroups2 complement aStem:anotherGroup
    //overallGroup is composite complement: requireGroups1 complement aStem:yetAnotherGroup
    overallGroup = GroupFinder.findByName(this.grouperSession, overallName, true);
    systemOfRecordGroup = GroupFinder.findByName(this.grouperSession, systemOfRecordIdPath, true);
    requireGroups1 = GroupFinder.findByName(this.grouperSession, overallName + "_requireGroups1", false);
    requireGroups2 = GroupFinder.findByName(this.grouperSession, overallName + "_requireGroups2", false);
    requireGroups3 = GroupFinder.findByName(this.grouperSession, overallName + "_requireGroups3", false);
    requireGroups4 = GroupFinder.findByName(this.grouperSession, overallName + "_requireGroups4", false);
    includesGroup = GroupFinder.findByName(this.grouperSession, overallName + "_includes", false);
    excludesGroup = GroupFinder.findByName(this.grouperSession, overallName + "_excludes", false);
    systemOfRecordAndIncludesGroup = GroupFinder.findByName(this.grouperSession, overallName + "_systemOfRecordAndIncludes", false);
    includesMinusExcludes = GroupFinder.findByName(this.grouperSession, overallName + "_includesMinusExcludes", false);

    assertNotNull(requireGroups1);
    assertNotNull(requireGroups2);
    assertNull(requireGroups3);
    assertNull(requireGroups4);
    assertNull(includesGroup);
    assertNull(excludesGroup);
    assertNull(systemOfRecordAndIncludesGroup);
    assertNull(includesMinusExcludes);

    //check the requireGroups2 group
    assertTrue(requireGroups2.getComposite(true).getType().equals(CompositeType.INTERSECTION));
    assertEquals(requireGroups2.getComposite(true).getLeftGroup(), systemOfRecordGroup);
    assertEquals(requireGroups2.getComposite(true).getRightGroup(), activeEmployeeGroup);

    assertTrue(requireGroups2.hasMember(SubjectTestHelper.SUBJ0));
    assertTrue(requireGroups2.hasMember(SubjectTestHelper.SUBJ1));
    assertTrue(requireGroups2.hasMember(SubjectTestHelper.SUBJ2));
    assertFalse(requireGroups2.hasMember(SubjectTestHelper.SUBJ3));
    assertTrue(requireGroups2.hasMember(SubjectTestHelper.SUBJ4));
    assertFalse(requireGroups2.hasMember(SubjectTestHelper.SUBJ5));

    //check the requireGroups1 group
    assertTrue(requireGroups1.getComposite(true).getType().equals(CompositeType.INTERSECTION));
    assertEquals(requireGroups1.getComposite(true).getLeftGroup(), requireGroups2);
    assertEquals(requireGroups1.getComposite(true).getRightGroup(), anotherGroup);

    assertTrue(requireGroups1.hasMember(SubjectTestHelper.SUBJ0));
    assertTrue(requireGroups1.hasMember(SubjectTestHelper.SUBJ1));
    assertFalse(requireGroups1.hasMember(SubjectTestHelper.SUBJ2));
    assertFalse(requireGroups1.hasMember(SubjectTestHelper.SUBJ3));
    assertTrue(requireGroups1.hasMember(SubjectTestHelper.SUBJ4));
    assertFalse(requireGroups1.hasMember(SubjectTestHelper.SUBJ5));

    //check the overall group
    assertTrue(overallGroup.getComposite(true).getType().equals(CompositeType.INTERSECTION));
    assertEquals(overallGroup.getComposite(true).getLeftGroup(), requireGroups1);
    assertEquals(overallGroup.getComposite(true).getRightGroup(), yetAnotherGroup);

    assertTrue(overallGroup.hasMember(SubjectTestHelper.SUBJ0));
    assertFalse(overallGroup.hasMember(SubjectTestHelper.SUBJ1));
    assertFalse(overallGroup.hasMember(SubjectTestHelper.SUBJ2));
    assertFalse(overallGroup.hasMember(SubjectTestHelper.SUBJ3));
    assertTrue(overallGroup.hasMember(SubjectTestHelper.SUBJ4));
    assertFalse(overallGroup.hasMember(SubjectTestHelper.SUBJ5));

    //#################################
    //## add in includeExclude
    aGroup.addType(this.includeExcludeType);
    
    //systemOfRecordAndIncludes has two members, system or record and includes
    //includesMinusExcludes is composite minus: systemOfRecordAndIncludes minus excludes
    //requireGroups2 is composite complement: systemOfRecordAndIncludes complement aStem:activeStudent
    //requireGroups1 is composite complement: requireGroups2 complement aStem:anotherGroup
    //overallGroup is composite complement: requireGroups1 complement aStem:yetAnotherGroup
    overallGroup = GroupFinder.findByName(this.grouperSession, overallName, true);
    systemOfRecordGroup = GroupFinder.findByName(this.grouperSession, systemOfRecordIdPath, true);
    requireGroups1 = GroupFinder.findByName(this.grouperSession, overallName + "_requireGroups1", false);
    requireGroups2 = GroupFinder.findByName(this.grouperSession, overallName + "_requireGroups2", false);
    requireGroups3 = GroupFinder.findByName(this.grouperSession, overallName + "_requireGroups3", false);
    requireGroups4 = GroupFinder.findByName(this.grouperSession, overallName + "_requireGroups4", false);
    includesGroup = GroupFinder.findByName(this.grouperSession, overallName + "_includes", false);
    excludesGroup = GroupFinder.findByName(this.grouperSession, overallName + "_excludes", false);
    systemOfRecordAndIncludesGroup = GroupFinder.findByName(this.grouperSession, overallName + "_systemOfRecordAndIncludes", false);
    includesMinusExcludes = GroupFinder.findByName(this.grouperSession, overallName + "_includesMinusExcludes", false);

    assertNotNull(requireGroups1);
    assertNotNull(requireGroups2);
    assertNull(requireGroups3);
    assertNull(requireGroups4);
    assertNotNull(includesGroup);
    assertNotNull(excludesGroup);
    assertNotNull(systemOfRecordAndIncludesGroup);
    assertNotNull(includesMinusExcludes);
    
    includesGroup.addMember(SubjectTestHelper.SUBJ5);
    includesGroup.addMember(SubjectTestHelper.SUBJ6);
    excludesGroup.addMember(SubjectTestHelper.SUBJ0);

    //check the system of record group
    assertTrue(systemOfRecordGroup.hasMember(SubjectTestHelper.SUBJ0));
    assertTrue(systemOfRecordGroup.hasMember(SubjectTestHelper.SUBJ1));
    assertTrue(systemOfRecordGroup.hasMember(SubjectTestHelper.SUBJ2));
    assertTrue(systemOfRecordGroup.hasMember(SubjectTestHelper.SUBJ3));
    assertTrue(systemOfRecordGroup.hasMember(SubjectTestHelper.SUBJ4));
    assertFalse(systemOfRecordGroup.hasMember(SubjectTestHelper.SUBJ5));
    assertFalse(systemOfRecordGroup.hasMember(SubjectTestHelper.SUBJ6));
    
    
    //check the systemOfRecordAndIncludes group
    assertFalse(systemOfRecordAndIncludesGroup.hasComposite());
    assertEquals(2, systemOfRecordAndIncludesGroup.getImmediateMembers().size());
    assertTrue(systemOfRecordAndIncludesGroup.hasImmediateMember(SubjectFinder.findById(systemOfRecordGroup.getUuid(), true)));
    assertTrue(systemOfRecordAndIncludesGroup.hasImmediateMember(SubjectFinder.findById(includesGroup.getUuid(), true)));

    assertTrue(systemOfRecordAndIncludesGroup.hasMember(SubjectTestHelper.SUBJ0));
    assertTrue(systemOfRecordAndIncludesGroup.hasMember(SubjectTestHelper.SUBJ1));
    assertTrue(systemOfRecordAndIncludesGroup.hasMember(SubjectTestHelper.SUBJ2));
    assertTrue(systemOfRecordAndIncludesGroup.hasMember(SubjectTestHelper.SUBJ3));
    assertTrue(systemOfRecordAndIncludesGroup.hasMember(SubjectTestHelper.SUBJ4));
    assertTrue(systemOfRecordAndIncludesGroup.hasMember(SubjectTestHelper.SUBJ5));
    assertTrue(systemOfRecordAndIncludesGroup.hasMember(SubjectTestHelper.SUBJ6));
    
    //check the includesMinusExcludes group
    assertTrue(includesMinusExcludes.getComposite(true).getType().equals(CompositeType.COMPLEMENT));
    assertEquals(includesMinusExcludes.getComposite(true).getLeftGroup(), systemOfRecordAndIncludesGroup);
    assertEquals(includesMinusExcludes.getComposite(true).getRightGroup(), excludesGroup);

    assertFalse(includesMinusExcludes.hasMember(SubjectTestHelper.SUBJ0));
    assertTrue(includesMinusExcludes.hasMember(SubjectTestHelper.SUBJ1));
    assertTrue(includesMinusExcludes.hasMember(SubjectTestHelper.SUBJ2));
    assertTrue(includesMinusExcludes.hasMember(SubjectTestHelper.SUBJ3));
    assertTrue(includesMinusExcludes.hasMember(SubjectTestHelper.SUBJ4));
    assertTrue(includesMinusExcludes.hasMember(SubjectTestHelper.SUBJ5));
    assertTrue(includesMinusExcludes.hasMember(SubjectTestHelper.SUBJ6));
    
    //check the requireGroups2 group
    assertTrue(requireGroups2.getComposite(true).getType().equals(CompositeType.INTERSECTION));
    assertEquals(requireGroups2.getComposite(true).getLeftGroup(), includesMinusExcludes);
    assertEquals(requireGroups2.getComposite(true).getRightGroup(), activeEmployeeGroup);

    assertFalse(requireGroups2.hasMember(SubjectTestHelper.SUBJ0));
    assertTrue(requireGroups2.hasMember(SubjectTestHelper.SUBJ1));
    assertTrue(requireGroups2.hasMember(SubjectTestHelper.SUBJ2));
    assertFalse(requireGroups2.hasMember(SubjectTestHelper.SUBJ3));
    assertTrue(requireGroups2.hasMember(SubjectTestHelper.SUBJ4));
    assertTrue(requireGroups2.hasMember(SubjectTestHelper.SUBJ5));
    assertFalse(requireGroups2.hasMember(SubjectTestHelper.SUBJ6));

    //check the requireGroups1 group
    assertTrue(requireGroups1.getComposite(true).getType().equals(CompositeType.INTERSECTION));
    assertEquals(requireGroups1.getComposite(true).getLeftGroup(), requireGroups2);
    assertEquals(requireGroups1.getComposite(true).getRightGroup(), anotherGroup);

    assertFalse(requireGroups1.hasMember(SubjectTestHelper.SUBJ0));
    assertTrue(requireGroups1.hasMember(SubjectTestHelper.SUBJ1));
    assertFalse(requireGroups1.hasMember(SubjectTestHelper.SUBJ2));
    assertFalse(requireGroups1.hasMember(SubjectTestHelper.SUBJ3));
    assertTrue(requireGroups1.hasMember(SubjectTestHelper.SUBJ4));
    assertTrue(requireGroups1.hasMember(SubjectTestHelper.SUBJ5));
    assertFalse(requireGroups1.hasMember(SubjectTestHelper.SUBJ6));

    //check the overall group
    assertTrue(overallGroup.getComposite(true).getType().equals(CompositeType.INTERSECTION));
    assertEquals(overallGroup.getComposite(true).getLeftGroup(), requireGroups1);
    assertEquals(overallGroup.getComposite(true).getRightGroup(), yetAnotherGroup);

    assertFalse(overallGroup.hasMember(SubjectTestHelper.SUBJ0));
    assertFalse(overallGroup.hasMember(SubjectTestHelper.SUBJ1));
    assertFalse(overallGroup.hasMember(SubjectTestHelper.SUBJ2));
    assertFalse(overallGroup.hasMember(SubjectTestHelper.SUBJ3));
    assertTrue(overallGroup.hasMember(SubjectTestHelper.SUBJ4));
    assertTrue(overallGroup.hasMember(SubjectTestHelper.SUBJ5));
    assertFalse(overallGroup.hasMember(SubjectTestHelper.SUBJ6));

    //#################################
    //## take off include/exclude, and nothing should change
    
    aGroup.deleteType(this.includeExcludeType);
    
    //systemOfRecordAndIncludes has two members, system or record and includes
    //includesMinusExcludes is composite minus: systemOfRecordAndIncludes minus excludes
    //requireGroups2 is composite complement: systemOfRecordAndIncludes complement aStem:activeStudent
    //requireGroups1 is composite complement: requireGroups2 complement aStem:anotherGroup
    //overallGroup is composite complement: requireGroups1 complement aStem:yetAnotherGroup
    overallGroup = GroupFinder.findByName(this.grouperSession, overallName, true);
    systemOfRecordGroup = GroupFinder.findByName(this.grouperSession, systemOfRecordIdPath, true);
    requireGroups1 = GroupFinder.findByName(this.grouperSession, overallName + "_requireGroups1", false);
    requireGroups2 = GroupFinder.findByName(this.grouperSession, overallName + "_requireGroups2", false);
    requireGroups3 = GroupFinder.findByName(this.grouperSession, overallName + "_requireGroups3", false);
    requireGroups4 = GroupFinder.findByName(this.grouperSession, overallName + "_requireGroups4", false);
    includesGroup = GroupFinder.findByName(this.grouperSession, overallName + "_includes", false);
    excludesGroup = GroupFinder.findByName(this.grouperSession, overallName + "_excludes", false);
    systemOfRecordAndIncludesGroup = GroupFinder.findByName(this.grouperSession, overallName + "_systemOfRecordAndIncludes", false);
    includesMinusExcludes = GroupFinder.findByName(this.grouperSession, overallName + "_includesMinusExcludes", false);

    assertNotNull(requireGroups1);
    assertNotNull(requireGroups2);
    assertNull(requireGroups3);
    assertNull(requireGroups4);
    assertNotNull(includesGroup);
    assertNotNull(excludesGroup);
    assertNotNull(systemOfRecordAndIncludesGroup);
    assertNotNull(includesMinusExcludes);
    
    //check the system of record group
    assertTrue(systemOfRecordGroup.hasMember(SubjectTestHelper.SUBJ0));
    assertTrue(systemOfRecordGroup.hasMember(SubjectTestHelper.SUBJ1));
    assertTrue(systemOfRecordGroup.hasMember(SubjectTestHelper.SUBJ2));
    assertTrue(systemOfRecordGroup.hasMember(SubjectTestHelper.SUBJ3));
    assertTrue(systemOfRecordGroup.hasMember(SubjectTestHelper.SUBJ4));
    assertFalse(systemOfRecordGroup.hasMember(SubjectTestHelper.SUBJ5));
    assertFalse(systemOfRecordGroup.hasMember(SubjectTestHelper.SUBJ6));
    
    
    //check the systemOfRecordAndIncludes group
    assertFalse(systemOfRecordAndIncludesGroup.hasComposite());
    assertEquals(2, systemOfRecordAndIncludesGroup.getImmediateMembers().size());
    assertTrue(systemOfRecordAndIncludesGroup.hasImmediateMember(SubjectFinder.findById(systemOfRecordGroup.getUuid(), true)));
    assertTrue(systemOfRecordAndIncludesGroup.hasImmediateMember(SubjectFinder.findById(includesGroup.getUuid(), true)));

    assertTrue(systemOfRecordAndIncludesGroup.hasMember(SubjectTestHelper.SUBJ0));
    assertTrue(systemOfRecordAndIncludesGroup.hasMember(SubjectTestHelper.SUBJ1));
    assertTrue(systemOfRecordAndIncludesGroup.hasMember(SubjectTestHelper.SUBJ2));
    assertTrue(systemOfRecordAndIncludesGroup.hasMember(SubjectTestHelper.SUBJ3));
    assertTrue(systemOfRecordAndIncludesGroup.hasMember(SubjectTestHelper.SUBJ4));
    assertTrue(systemOfRecordAndIncludesGroup.hasMember(SubjectTestHelper.SUBJ5));
    assertTrue(systemOfRecordAndIncludesGroup.hasMember(SubjectTestHelper.SUBJ6));
    
    //check the includesMinusExcludes group
    assertTrue(includesMinusExcludes.getComposite(true).getType().equals(CompositeType.COMPLEMENT));
    assertEquals(includesMinusExcludes.getComposite(true).getLeftGroup(), systemOfRecordAndIncludesGroup);
    assertEquals(includesMinusExcludes.getComposite(true).getRightGroup(), excludesGroup);

    assertFalse(includesMinusExcludes.hasMember(SubjectTestHelper.SUBJ0));
    assertTrue(includesMinusExcludes.hasMember(SubjectTestHelper.SUBJ1));
    assertTrue(includesMinusExcludes.hasMember(SubjectTestHelper.SUBJ2));
    assertTrue(includesMinusExcludes.hasMember(SubjectTestHelper.SUBJ3));
    assertTrue(includesMinusExcludes.hasMember(SubjectTestHelper.SUBJ4));
    assertTrue(includesMinusExcludes.hasMember(SubjectTestHelper.SUBJ5));
    assertTrue(includesMinusExcludes.hasMember(SubjectTestHelper.SUBJ6));
    
    //check the requireGroups2 group
    assertTrue(requireGroups2.getComposite(true).getType().equals(CompositeType.INTERSECTION));
    assertEquals(requireGroups2.getComposite(true).getLeftGroup(), includesMinusExcludes);
    assertEquals(requireGroups2.getComposite(true).getRightGroup(), activeEmployeeGroup);

    assertFalse(requireGroups2.hasMember(SubjectTestHelper.SUBJ0));
    assertTrue(requireGroups2.hasMember(SubjectTestHelper.SUBJ1));
    assertTrue(requireGroups2.hasMember(SubjectTestHelper.SUBJ2));
    assertFalse(requireGroups2.hasMember(SubjectTestHelper.SUBJ3));
    assertTrue(requireGroups2.hasMember(SubjectTestHelper.SUBJ4));
    assertTrue(requireGroups2.hasMember(SubjectTestHelper.SUBJ5));
    assertFalse(requireGroups2.hasMember(SubjectTestHelper.SUBJ6));

    //check the requireGroups1 group
    assertTrue(requireGroups1.getComposite(true).getType().equals(CompositeType.INTERSECTION));
    assertEquals(requireGroups1.getComposite(true).getLeftGroup(), requireGroups2);
    assertEquals(requireGroups1.getComposite(true).getRightGroup(), anotherGroup);

    assertFalse(requireGroups1.hasMember(SubjectTestHelper.SUBJ0));
    assertTrue(requireGroups1.hasMember(SubjectTestHelper.SUBJ1));
    assertFalse(requireGroups1.hasMember(SubjectTestHelper.SUBJ2));
    assertFalse(requireGroups1.hasMember(SubjectTestHelper.SUBJ3));
    assertTrue(requireGroups1.hasMember(SubjectTestHelper.SUBJ4));
    assertTrue(requireGroups1.hasMember(SubjectTestHelper.SUBJ5));
    assertFalse(requireGroups1.hasMember(SubjectTestHelper.SUBJ6));

    //check the overall group
    assertTrue(overallGroup.getComposite(true).getType().equals(CompositeType.INTERSECTION));
    assertEquals(overallGroup.getComposite(true).getLeftGroup(), requireGroups1);
    assertEquals(overallGroup.getComposite(true).getRightGroup(), yetAnotherGroup);

    assertFalse(overallGroup.hasMember(SubjectTestHelper.SUBJ0));
    assertFalse(overallGroup.hasMember(SubjectTestHelper.SUBJ1));
    assertFalse(overallGroup.hasMember(SubjectTestHelper.SUBJ2));
    assertFalse(overallGroup.hasMember(SubjectTestHelper.SUBJ3));
    assertTrue(overallGroup.hasMember(SubjectTestHelper.SUBJ4));
    assertTrue(overallGroup.hasMember(SubjectTestHelper.SUBJ5));
    assertFalse(overallGroup.hasMember(SubjectTestHelper.SUBJ6));
    
    
    
    //#################################
    //## delete other type
    //call the method
    aGroup.deleteType(this.requireGroupsType);

    //overallGroup has one member: systemOfRecord
    overallGroup = GroupFinder.findByName(this.grouperSession, overallName, false);
    requireGroups1 = GroupFinder.findByName(this.grouperSession, overallName + "_requireGroups1", false);
    requireGroups2 = GroupFinder.findByName(this.grouperSession, overallName + "_requireGroups2", false);
    requireGroups3 = GroupFinder.findByName(this.grouperSession, overallName + "_requireGroups3", false);
    requireGroups4 = GroupFinder.findByName(this.grouperSession, overallName + "_requireGroups4", false);
    includesGroup = GroupFinder.findByName(this.grouperSession, overallName + "_includes", false);
    excludesGroup = GroupFinder.findByName(this.grouperSession, overallName + "_excludes", false);
    systemOfRecordAndIncludesGroup = GroupFinder.findByName(this.grouperSession, overallName + "_systemOfRecordAndIncludes", false);
    includesMinusExcludes = GroupFinder.findByName(this.grouperSession, overallName + "_includesMinusExcludes", false);

    assertNotNull(overallGroup);
    assertNull(requireGroups1);
    assertNull(requireGroups2);
    assertNull(requireGroups3);
    assertNull(requireGroups4);
    assertNotNull(includesGroup);
    assertNotNull(excludesGroup);
    assertNotNull(systemOfRecordAndIncludesGroup);
    assertNull(includesMinusExcludes);

    //check the systemOfRecordAndIncludes group
    assertFalse(systemOfRecordAndIncludesGroup.hasComposite());
    assertEquals(2, systemOfRecordAndIncludesGroup.getImmediateMembers().size());
    assertTrue(systemOfRecordAndIncludesGroup.hasImmediateMember(SubjectFinder.findById(systemOfRecordGroup.getUuid(), true)));
    assertTrue(systemOfRecordAndIncludesGroup.hasImmediateMember(SubjectFinder.findById(includesGroup.getUuid(), true)));

    assertTrue(systemOfRecordAndIncludesGroup.hasMember(SubjectTestHelper.SUBJ0));
    assertTrue(systemOfRecordAndIncludesGroup.hasMember(SubjectTestHelper.SUBJ1));
    assertTrue(systemOfRecordAndIncludesGroup.hasMember(SubjectTestHelper.SUBJ2));
    assertTrue(systemOfRecordAndIncludesGroup.hasMember(SubjectTestHelper.SUBJ3));
    assertTrue(systemOfRecordAndIncludesGroup.hasMember(SubjectTestHelper.SUBJ4));
    assertTrue(systemOfRecordAndIncludesGroup.hasMember(SubjectTestHelper.SUBJ5));
    assertTrue(systemOfRecordAndIncludesGroup.hasMember(SubjectTestHelper.SUBJ6));

    //check the overall group
    assertTrue(overallGroup.getComposite(true).getType().equals(CompositeType.COMPLEMENT));
    assertEquals(overallGroup.getComposite(true).getLeftGroup(), systemOfRecordAndIncludesGroup);
    assertEquals(overallGroup.getComposite(true).getRightGroup(), excludesGroup);

    assertFalse(overallGroup.hasMember(SubjectTestHelper.SUBJ0));
    assertTrue(overallGroup.hasMember(SubjectTestHelper.SUBJ1));
    assertTrue(overallGroup.hasMember(SubjectTestHelper.SUBJ2));
    assertTrue(overallGroup.hasMember(SubjectTestHelper.SUBJ3));
    assertTrue(overallGroup.hasMember(SubjectTestHelper.SUBJ4));
    assertTrue(overallGroup.hasMember(SubjectTestHelper.SUBJ5));
    assertTrue(overallGroup.hasMember(SubjectTestHelper.SUBJ6));
    
    //############################################
    //delete the two include and exclude groups
    overallGroup.deleteCompositeMember();
    includesGroup.delete();
    excludesGroup.delete();
    
    overallGroup.manageIncludesExcludesRequiredGroups(grouperSession, false);
    
    //overallGroup has one member: systemOfRecord
    overallGroup = GroupFinder.findByName(this.grouperSession, overallName, false);
    systemOfRecordGroup = GroupFinder.findByName(this.grouperSession, overallName + "_systemOfRecord", false);
    requireGroups1 = GroupFinder.findByName(this.grouperSession, overallName + "_requireGroups1", false);
    requireGroups2 = GroupFinder.findByName(this.grouperSession, overallName + "_requireGroups2", false);
    requireGroups3 = GroupFinder.findByName(this.grouperSession, overallName + "_requireGroups3", false);
    requireGroups4 = GroupFinder.findByName(this.grouperSession, overallName + "_requireGroups4", false);
    includesGroup = GroupFinder.findByName(this.grouperSession, overallName + "_includes", false);
    excludesGroup = GroupFinder.findByName(this.grouperSession, overallName + "_excludes", false);
    systemOfRecordAndIncludesGroup = GroupFinder.findByName(this.grouperSession, overallName + "_systemOfRecordAndIncludes", false);
    includesMinusExcludes = GroupFinder.findByName(this.grouperSession, overallName + "_includesMinusExcludes", false);
  
    assertNotNull(overallGroup);
    assertNotNull(overallGroup);
    assertNull(requireGroups1);
    assertNull(requireGroups2);
    assertNull(requireGroups3);
    assertNull(requireGroups4);
    assertNull(includesGroup);
    assertNull(excludesGroup);
    assertNull(systemOfRecordAndIncludesGroup);
    assertNull(includesMinusExcludes);
  
    //check the overall group
    assertEquals(overallGroup.getImmediateMembers().size(), 1);
    assertTrue(overallGroup.hasImmediateMember(SubjectFinder.findById(systemOfRecordGroup.getUuid(), true)));
  
    assertTrue(overallGroup.hasMember(SubjectTestHelper.SUBJ0));
    assertTrue(overallGroup.hasMember(SubjectTestHelper.SUBJ1));
    assertTrue(overallGroup.hasMember(SubjectTestHelper.SUBJ2));
    assertTrue(overallGroup.hasMember(SubjectTestHelper.SUBJ3));
    assertTrue(overallGroup.hasMember(SubjectTestHelper.SUBJ4));

    
    
  }
  
  /**
   * @throws Exception 
   */
  public void testRequireGroup() throws Exception {
    
    //make a stem and a group
    String overallName = "aStem:aGroup";

    Group aGroup = Group.saveGroup(this.grouperSession, null, null, overallName, null, null, null, true);

    Group activeEmployeeGroup = Group.saveGroup(this.grouperSession, "aStem:activeEmployee", null, "aStem:activeEmployee", null, null, null, true);

    Group anotherGroup = Group.saveGroup(this.grouperSession, "aStem:anotherGroup", null, "aStem:anotherGroup", null, null, null, true);

    Group yetAnotherGroup = Group.saveGroup(this.grouperSession, "aStem:yetAnotherGroup", null, "aStem:yetAnotherGroup", null, null, null, true);

    //lets start by adding first five to the main group
    aGroup.addMember(SubjectTestHelper.SUBJ0);
    aGroup.addMember(SubjectTestHelper.SUBJ1);
    aGroup.addMember(SubjectTestHelper.SUBJ2);
    aGroup.addMember(SubjectTestHelper.SUBJ3);
    aGroup.addMember(SubjectTestHelper.SUBJ4);


    Group systemOfRecordGroup = null;

    String systemOfRecordIdPath = overallName + "_systemOfRecord";
    
    systemOfRecordGroup = GroupFinder.findByName(this.grouperSession, systemOfRecordIdPath, false);
    assertNull("Should not find this group yet", systemOfRecordGroup);

    //call the method
    aGroup.addType(this.includeExcludeType);
    aGroup.addType(this.requireGroupsType);

    //add an attributes, first one, then two others
    aGroup.setAttribute("requireActiveEmployee", "true");
    aGroup.setAttribute("requireAlsoInGroups", "aStem:anotherGroup,aStem:yetAnotherGroup");
    
    //refresh this
    Group overallGroup = GroupFinder.findByName(this.grouperSession, overallName, true);
    
    //check the system of record group
    systemOfRecordGroup = GroupFinder.findByName(this.grouperSession, systemOfRecordIdPath, true);
    
    //check the includes group
    Group includesGroup = GroupFinder.findByName(this.grouperSession, overallName + "_includes", true);

    //check the excludes group
    Group excludesGroup = GroupFinder.findByName(this.grouperSession, overallName + "_excludes", true);

    //check the sor and includes group
    Group systemOfRecordAndIncludesGroup = GroupFinder.findByName(this.grouperSession, overallName + "_systemOfRecordAndIncludes", true);

    //includes minus excludes
    Group includesMinusExcludes = GroupFinder.findByName(this.grouperSession, overallName + "_includesMinusExcludes", true);
    
    //first helper composite
    Group requireGroups1 = GroupFinder.findByName(this.grouperSession, overallName + "_requireGroups1", true);
    Group requireGroups2 = GroupFinder.findByName(this.grouperSession, overallName + "_requireGroups2", true);
    Group requireGroups3 = GroupFinder.findByName(this.grouperSession, overallName + "_requireGroups3", false);

    assertNull("Shouldnt need 3 helpers, only 2", requireGroups3);

    //structure will be
    //includesMinusExcludes is composite complement: systemOfRecordAndIncludesGroup minus excludesGroup
    //requireGroups2 is composite complement: includesMinusExcludes minus aStem:activeEmployee
    //requireGroups1 is composite complement: requireGroups2 minus aStem:anotherGroup
    //overallGroup is composite complement: requireGroups1 minus aStem:yetAnotherGroup
    
    //check the includesMinusExcludes group
    assertTrue(includesMinusExcludes.getComposite(true).getType().equals(CompositeType.COMPLEMENT));
    assertEquals(includesMinusExcludes.getComposite(true).getLeftGroup(), systemOfRecordAndIncludesGroup);
    assertEquals(includesMinusExcludes.getComposite(true).getRightGroup(), excludesGroup);

    //check the requireGroups2 group
    assertTrue(requireGroups2.getComposite(true).getType().equals(CompositeType.INTERSECTION));
    assertEquals(requireGroups2.getComposite(true).getLeftGroup(), includesMinusExcludes);
    assertEquals(requireGroups2.getComposite(true).getRightGroup(), activeEmployeeGroup);

    //check the requireGroups1 group
    assertTrue(requireGroups1.getComposite(true).getType().equals(CompositeType.INTERSECTION));
    assertEquals(requireGroups1.getComposite(true).getLeftGroup(), requireGroups2);
    assertEquals(requireGroups1.getComposite(true).getRightGroup(), anotherGroup);

    //check the overall group
    assertTrue(overallGroup.getComposite(true).getType().equals(CompositeType.INTERSECTION));
    assertEquals(overallGroup.getComposite(true).getLeftGroup(), requireGroups1);
    assertEquals(overallGroup.getComposite(true).getRightGroup(), yetAnotherGroup);
    
    //now lets add some to the include
    includesGroup.addMember(SubjectTestHelper.SUBJ4);
    includesGroup.addMember(SubjectTestHelper.SUBJ5);
    includesGroup.addMember(SubjectTestHelper.SUBJ6);

    //lets remove some from the exclude
    excludesGroup.addMember(SubjectTestHelper.SUBJ3);
    excludesGroup.addMember(SubjectTestHelper.SUBJ5);

    //and add one to the system of record
    systemOfRecordGroup.addMember(SubjectTestHelper.SUBJ7);

    //add some to active employees
    activeEmployeeGroup.addMember(SubjectTestHelper.SUBJ0);
    activeEmployeeGroup.addMember(SubjectTestHelper.SUBJ1);
    activeEmployeeGroup.addMember(SubjectTestHelper.SUBJ2);
    activeEmployeeGroup.addMember(SubjectTestHelper.SUBJ3);
    activeEmployeeGroup.addMember(SubjectTestHelper.SUBJ4);
    activeEmployeeGroup.addMember(SubjectTestHelper.SUBJ5);
    activeEmployeeGroup.addMember(SubjectTestHelper.SUBJ6);
    
    //add some to another group
    anotherGroup.addMember(SubjectTestHelper.SUBJ0);
    anotherGroup.addMember(SubjectTestHelper.SUBJ1);
    anotherGroup.addMember(SubjectTestHelper.SUBJ2);
    anotherGroup.addMember(SubjectTestHelper.SUBJ3);
    anotherGroup.addMember(SubjectTestHelper.SUBJ4);
    anotherGroup.addMember(SubjectTestHelper.SUBJ5);
    anotherGroup.addMember(SubjectTestHelper.SUBJ7);
    
    //add some to yet another group
    yetAnotherGroup.addMember(SubjectTestHelper.SUBJ0);
    yetAnotherGroup.addMember(SubjectTestHelper.SUBJ1);
    yetAnotherGroup.addMember(SubjectTestHelper.SUBJ2);
    yetAnotherGroup.addMember(SubjectTestHelper.SUBJ3);
    yetAnotherGroup.addMember(SubjectTestHelper.SUBJ5);
    yetAnotherGroup.addMember(SubjectTestHelper.SUBJ6);
    yetAnotherGroup.addMember(SubjectTestHelper.SUBJ7);
    
    //and lets test the overall membership
    assertTrue(overallGroup.hasMember(SubjectTestHelper.SUBJ0));
    assertTrue(overallGroup.hasMember(SubjectTestHelper.SUBJ1));
    assertTrue(overallGroup.hasMember(SubjectTestHelper.SUBJ2));
    assertFalse(overallGroup.hasMember(SubjectTestHelper.SUBJ3));
    assertFalse(overallGroup.hasMember(SubjectTestHelper.SUBJ4));
    assertFalse(overallGroup.hasMember(SubjectTestHelper.SUBJ5));
    assertFalse(overallGroup.hasMember(SubjectTestHelper.SUBJ6));
    assertFalse(overallGroup.hasMember(SubjectTestHelper.SUBJ7));
    
    //#######################################################################
    //now remove an attribute
    aGroup.deleteAttribute("requireAlsoInGroups");
    
    //refresh this
    overallGroup = GroupFinder.findByName(this.grouperSession, overallName, true);
    
    //check the system of record group
    systemOfRecordGroup = GroupFinder.findByName(this.grouperSession, systemOfRecordIdPath, true);
    
    //check the excludes group
    systemOfRecordAndIncludesGroup = GroupFinder.findByName(this.grouperSession, overallName + "_systemOfRecordAndIncludes", true);

    //includes minus excludes
    includesMinusExcludes = GroupFinder.findByName(this.grouperSession, overallName + "_includesMinusExcludes", true);
    
    //first helper composite
    requireGroups1 = GroupFinder.findByName(this.grouperSession, overallName + "_requireGroups1", false);
    requireGroups2 = GroupFinder.findByName(this.grouperSession, overallName + "_requireGroups2", false);
    requireGroups3 = GroupFinder.findByName(this.grouperSession, overallName + "_requireGroups3", false);

    assertNull("Shouldnt need 3 helpers, only 0", requireGroups1);
    assertNull("Shouldnt need 3 helpers, only 0", requireGroups2);
    assertNull("Shouldnt need 3 helpers, only 0", requireGroups3);

    //structure will be
    //includesMinusExcludes is composite complement: systemOfRecordAndIncludesGroup minus excludesGroup
    //overallGroup is composite complement: includesMinusExcludes minus aStem:activeEmployee
    
    //check the includesMinusExcludes group
    assertTrue(includesMinusExcludes.getComposite(true).getType().equals(CompositeType.COMPLEMENT));
    assertEquals(includesMinusExcludes.getComposite(true).getLeftGroup(), systemOfRecordAndIncludesGroup);
    assertEquals(includesMinusExcludes.getComposite(true).getRightGroup(), excludesGroup);

    //check the overall group
    assertTrue(overallGroup.getComposite(true).getType().equals(CompositeType.INTERSECTION));
    assertEquals(overallGroup.getComposite(true).getLeftGroup(), includesMinusExcludes);
    assertEquals(overallGroup.getComposite(true).getRightGroup(), activeEmployeeGroup);
    
    //and lets test the overall membership
    assertTrue(overallGroup.hasMember(SubjectTestHelper.SUBJ0));
    assertTrue(overallGroup.hasMember(SubjectTestHelper.SUBJ1));
    assertTrue(overallGroup.hasMember(SubjectTestHelper.SUBJ2));
    assertFalse(overallGroup.hasMember(SubjectTestHelper.SUBJ3));
    assertTrue(overallGroup.hasMember(SubjectTestHelper.SUBJ4));
    assertFalse(overallGroup.hasMember(SubjectTestHelper.SUBJ5));
    assertTrue(overallGroup.hasMember(SubjectTestHelper.SUBJ6));
    assertFalse(overallGroup.hasMember(SubjectTestHelper.SUBJ7));
    
    //lets try to remove that and group too
    aGroup.deleteAttribute("requireActiveEmployee");
    
    overallGroup = GroupFinder.findByName(this.grouperSession, overallName, true);

    //and lets test the overall membership
    assertTrue(overallGroup.hasMember(SubjectTestHelper.SUBJ0));
    assertTrue(overallGroup.hasMember(SubjectTestHelper.SUBJ1));
    assertTrue(overallGroup.hasMember(SubjectTestHelper.SUBJ2));
    assertFalse(overallGroup.hasMember(SubjectTestHelper.SUBJ3));
    assertTrue(overallGroup.hasMember(SubjectTestHelper.SUBJ4));
    assertFalse(overallGroup.hasMember(SubjectTestHelper.SUBJ5));
    assertTrue(overallGroup.hasMember(SubjectTestHelper.SUBJ6));
    assertTrue(overallGroup.hasMember(SubjectTestHelper.SUBJ7));
    
    aGroup = GroupFinder.findByName(grouperSession, aGroup.getName(), true);

    aGroup.delete();
    
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.helper.GrouperTest#tearDown()
   */
  @Override
  protected void tearDown() {
    super.tearDown();
    GrouperSession.stopQuietly(this.grouperSession);
  }
  
}

