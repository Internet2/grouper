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

import edu.internet2.middleware.grouper.cfg.ApiConfig;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.exception.GroupNotFoundException;
import edu.internet2.middleware.grouper.exception.SchemaException;
import edu.internet2.middleware.grouper.hibernate.GrouperRollbackType;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.hibernate.HibernateHandler;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.misc.CompositeType;
import edu.internet2.middleware.grouper.misc.GrouperStartup;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * @author  mchyzer
 * @version $Id: TestGroupTypeIncludeExclude.java,v 1.1 2008-11-04 07:17:56 mchyzer Exp $
 */
public class TestGroupTypeIncludeExclude extends GrouperTest {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new TestGroupTypeIncludeExclude("testRequireGroup"));
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
      ApiConfig.testConfig.put("grouperIncludeExclude.use", "false");
      ApiConfig.testConfig.put("grouperIncludeExclude.requireGroups.use", "false");
      super.setUp();
      
      this.grouperSession = GrouperSession.startRootSession();

      ApiConfig.testConfig.put("grouperIncludeExclude.use", "true");
      ApiConfig.testConfig.put("grouperIncludeExclude.requireGroups.use", "true");

      ApiConfig.testConfig.put("grouperIncludeExclude.type.name", "grouperIncludeExclude");
      ApiConfig.testConfig.put("grouperIncludeExclude.requireGroups.type.name", "requireInGroups");

      ApiConfig.testConfig.put("grouperIncludeExclude.tooltip", "Select this type to auto-create other groups which facilitate having include and exclude list, and setting up group math so that other groups can be required (e.g. activeEmployee)");

      ApiConfig.testConfig.put("grouperIncludeExclude.requireGroups.attributeName", "requireAlsoInGroups");
      ApiConfig.testConfig.put("grouperIncludeExclude.requireGroups.tooltip", "Enter in comma separated group path(s).  An entity must be in these groups for it to be in the overall group.  e.g. stem1:stem2:group1, stem1:stem3:group2");

      ApiConfig.testConfig.put("grouperIncludeExclude.systemOfRecord.extension.suffix", "_systemOfRecord");
      ApiConfig.testConfig.put("grouperIncludeExclude.include.extension.suffix", "_includes");
      ApiConfig.testConfig.put("grouperIncludeExclude.exclude.extension.suffix", "_excludes");
      ApiConfig.testConfig.put("grouperIncludeExclude.systemOfRecordAndIncludes.extension.suffix", "_systemOfRecordAndIncludes");
      ApiConfig.testConfig.put("grouperIncludeExclude.includesMinusExcludes.extension.suffix", "_includesMinusExcludes");

      ApiConfig.testConfig.put("grouperIncludeExclude.requireGroups.extension.suffix", "_requireGroups${i}");

      ApiConfig.testConfig.put("grouperIncludeExclude.systemOfRecord.displayExtension.suffix", "${space}system of record");
      ApiConfig.testConfig.put("grouperIncludeExclude.include.displayExtension.suffix", "${space}includes");
      ApiConfig.testConfig.put("grouperIncludeExclude.exclude.displayExtension.suffix", "${space}excludes");
      ApiConfig.testConfig.put("grouperIncludeExclude.systemOfRecordAndIncludes.displayExtension.suffix", "${space}system of record and includes");
      ApiConfig.testConfig.put("grouperIncludeExclude.includesMinusExcludes.displayExtension.suffix", "${space}includes minus excludes");

      ApiConfig.testConfig.put("grouperIncludeExclude.requireGroups.displayExtension.suffix", "${space}includes minus exludes minus andGroup${i}");

      ApiConfig.testConfig.put("grouperIncludeExclude.overall.description", "Group containing list of ${displayExtension} after adding the includes and subtracting the excludes");
      ApiConfig.testConfig.put("grouperIncludeExclude.systemOfRecord.description", "Group containing list of ${displayExtension} (generally straight from the system of record) without yet considering manual include or exclude lists");
      ApiConfig.testConfig.put("grouperIncludeExclude.include.description", "Group containing manual list of includes for group ${displayExtension} which will be added to the system of record list (unless the subject is also in the excludes group)");
      ApiConfig.testConfig.put("grouperIncludeExclude.exclude.description", "Group containing manual list of excludes for group ${displayExtension} which will not be in the overall group");
      ApiConfig.testConfig.put("grouperIncludeExclude.systemOfRecordAndIncludes.description", "Internal utility group for group ${displayExtension} which facilitates the group math for the include and exclude lists");
      ApiConfig.testConfig.put("grouperIncludeExclude.includesMinusExclude.description", "Internal utility group for group ${displayExtension} which facilitates includes, excludes, and required groups (e.g. activeEmployee)");

      ApiConfig.testConfig.put("grouperIncludeExclude.requireGroups.description", "Internal utility group for group ${displayExtension} which facilitates includes, excludes, and required groups (e.g. activeEmployee)");

      ApiConfig.testConfig.put("grouperIncludeExclude.requireGroup.name.0", "requireActiveEmployee");
      ApiConfig.testConfig.put("grouperIncludeExclude.requireGroup.attributeOrType.0", "attribute");
      ApiConfig.testConfig.put("grouperIncludeExclude.requireGroup.group.0", "aStem:activeEmployee");
      ApiConfig.testConfig.put("grouperIncludeExclude.requireGroup.description.0", "If value is true, members of the overall group must be an active employee (in the aStem:activeEmployee group).  Otherwise, leave this value not filled in.");

      ApiConfig.testConfig.put("grouperIncludeExclude.requireGroup.name.1", "requireActiveStudent");
      ApiConfig.testConfig.put("grouperIncludeExclude.requireGroup.attributeOrType.1", "type");
      ApiConfig.testConfig.put("grouperIncludeExclude.requireGroup.group.1", "aStem:activeStudent");
      ApiConfig.testConfig.put("grouperIncludeExclude.requireGroup.description.1", "If value is true, members of the overall group must be an active student (in the aStem:activeStudent group).  Otherwise, leave this value not filled in.");

      String groupTypeName = GrouperConfig.getProperty("grouperIncludeExclude.type.name");

      String requireInGroupsTypeName = GrouperConfig.getProperty("grouperIncludeExclude.requireGroups.type.name");

      //shouldnt exist
      try {
        GroupTypeFinder.find(groupTypeName);
        fail("Shouldnt exist now");
      } catch (SchemaException se) {
        //good
      }

      try {
        GroupTypeFinder.find(requireInGroupsTypeName);
        fail("Shouldnt exist now");
      } catch (SchemaException se) {
        //good
      }

      try {
        GroupTypeFinder.find("requireInActiveStudent");
        fail("Shouldnt exist now");
      } catch (SchemaException se) {
        //good
      }

      GrouperStartup.initIncludeExcludeType();
      
      this.includeExcludeType = GroupTypeFinder.find(groupTypeName);
      
      assertNotNull("Should exist now", this.includeExcludeType);
      
      this.requireGroupsType = GroupTypeFinder.find(requireInGroupsTypeName);
      
      assertNotNull("Should exist now", this.requireGroupsType);

      this.requireActiveStudentType = GroupTypeFinder.find("requireActiveStudent");
      
      assertNotNull("Should exist now", this.requireGroupsType);
      
      this.requireActiveEmployee = FieldFinder.find("requireActiveEmployee");
      
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
  private Field requireActiveEmployee;
  
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

    HibernateSession.callbackHibernateSession(GrouperTransactionType.READ_WRITE_NEW, new HibernateHandler() {

      public Object callback(HibernateSession hibernateSession)
          throws GrouperDAOException {
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
    HibernateSession.callbackHibernateSession(GrouperTransactionType.READ_WRITE_NEW, new HibernateHandler() {

      public Object callback(HibernateSession hibernateSession)
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
      systemOfRecordGroup = GroupFinder.findByName(this.grouperSession, startWithOverallGroup ? systemOfRecordIdPath : overallName);
      fail("Should not find this group yet");
    } catch (GroupNotFoundException gnfe) {
      //good
    }
  
    aGroup.addType(includeExcludeType);
    
    //refresh this
    Group overallGroup = GroupFinder.findByName(this.grouperSession, overallName);
    
    //check the system of record group
    systemOfRecordGroup = GroupFinder.findByName(this.grouperSession, systemOfRecordIdPath);
    
    assertNotNull("Should exist now", systemOfRecordGroup);
    
    //make sure name and description are all good for system of record
    if (startWithOverallGroup) {
      assertEquals(overallGroup.getDisplayName() + " system of record", systemOfRecordGroup.getDisplayName());
      assertTrue("Should contain", systemOfRecordGroup.getDescription().toLowerCase().contains("system of record"));
    }
    
    //check the includes group
    Group includesGroup = GroupFinder.findByName(this.grouperSession, overallName + "_includes");
    {
      
      assertNotNull("Should exist now", includesGroup);
      
      //make sure name and description are all good for system of record
      assertEquals(overallGroup.getDisplayName() + " includes", includesGroup.getDisplayName());
      assertTrue("Should contain", includesGroup.getDescription().toLowerCase().contains("list of includes"));
    }
    
    //check the excludes group
    Group excludesGroup = GroupFinder.findByName(this.grouperSession, overallName + "_excludes");
    {
      
      assertNotNull("Should exist now", excludesGroup);
      
      //make sure name and description are all good for system of record
      assertEquals(overallGroup.getDisplayName() + " excludes", excludesGroup.getDisplayName());
      assertTrue("Should contain", excludesGroup.getDescription().toLowerCase().contains("list of excludes"));
    }      
  
    //check the excludes group
    Group systemOfRecordAndIncludesGroup = GroupFinder.findByName(this.grouperSession, overallName + "_systemOfRecordAndIncludes");
    {
      
      assertNotNull("Should exist now", systemOfRecordAndIncludesGroup);
      
      //make sure name and description are all good for system of record
      assertEquals(overallGroup.getDisplayName() + " system of record and includes", systemOfRecordAndIncludesGroup.getDisplayName());
      assertTrue("Should contain", systemOfRecordAndIncludesGroup.getDescription().toLowerCase().contains("group math for the include and exclude lists"));
      
      assertTrue("should have system of record as member", systemOfRecordAndIncludesGroup.hasImmediateMember(SubjectFinder.findById(systemOfRecordGroup.getUuid())));
      assertTrue("should have includes as member", systemOfRecordAndIncludesGroup.hasImmediateMember(SubjectFinder.findById(includesGroup.getUuid())));
    }      
  
    //check the overall group
    assertTrue(overallGroup.getComposite().getType().equals(CompositeType.COMPLEMENT));
    assertEquals(overallGroup.getComposite().getLeftGroup(), systemOfRecordAndIncludesGroup);
    assertEquals(overallGroup.getComposite().getRightGroup(), excludesGroup);
    
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

  }

  /**
   * @throws Exception 
   */
  public void testRequireGroup1() throws Exception {
    //make a stem and a group
    String overallName = "aStem:aGroup";

    Group aGroup = Group.saveGroup(this.grouperSession, null, null, overallName, null, null, null, true);

    Group activeStudentGroup = Group.saveGroup(this.grouperSession, null, null, "aStem:activeStudent", null, null, null, true);

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
    
    Group systemOfRecordGroup = null;

    String systemOfRecordIdPath = overallName + "_systemOfRecord";
    
    systemOfRecordGroup = GroupFinder.findByName(this.grouperSession, systemOfRecordIdPath, false);
    assertNull("Should not find this group yet", systemOfRecordGroup);

    //call the method
    aGroup.addType(this.requireActiveStudentType);

    //add an attributes, first one, then two others
    //aGroup.setAttribute("requireActiveEmployee", "true");
    //aGroup.setAttribute("requireAlsoInGroups", "aStem:anotherGroup,aStem:yetAnotherGroup");
    //aGroup.store();
    
    //refresh this
    Group overallGroup = GroupFinder.findByName(this.grouperSession, overallName);
    //get the system of record
    systemOfRecordGroup = GroupFinder.findByName(this.grouperSession, systemOfRecordIdPath);
    
    Group requireGroups1 = GroupFinder.findByName(this.grouperSession, overallName + "_requireGroups1", false);
    Group requireGroups2 = GroupFinder.findByName(this.grouperSession, overallName + "_requireGroups2", false);
    Group requireGroups3 = GroupFinder.findByName(this.grouperSession, overallName + "_requireGroups3", false);
    Group requireGroups4 = GroupFinder.findByName(this.grouperSession, overallName + "_requireGroups4", false);
    Group requireGroups5 = GroupFinder.findByName(this.grouperSession, overallName + "_requireGroups5", false);

    assertNull(requireGroups1);
    assertNull(requireGroups2);
    assertNull(requireGroups3);
    assertNull(requireGroups4);
    assertNull(requireGroups5);
    
    assertTrue(overallGroup.getComposite().getType().equals(CompositeType.INTERSECTION));
    assertEquals(overallGroup.getComposite().getLeftGroup(), systemOfRecordGroup);
    assertEquals(overallGroup.getComposite().getRightGroup(), activeStudentGroup);

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


    
  }
  
  /**
   * @throws Exception 
   */
  public void testRequireGroup() throws Exception {
    
    //make a stem and a group
    String overallName = "aStem:aGroup";

    Group aGroup = Group.saveGroup(this.grouperSession, null, null, overallName, null, null, null, true);

    Group activeEmployeeGroup = Group.saveGroup(this.grouperSession, null, null, "aStem:activeEmployee", null, null, null, true);

    Group anotherGroup = Group.saveGroup(this.grouperSession, null, null, "aStem:anotherGroup", null, null, null, true);

    Group yetAnotherGroup = Group.saveGroup(this.grouperSession, null, null, "aStem:yetAnotherGroup", null, null, null, true);

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
    aGroup.store();
    
    //refresh this
    Group overallGroup = GroupFinder.findByName(this.grouperSession, overallName);
    
    //check the system of record group
    systemOfRecordGroup = GroupFinder.findByName(this.grouperSession, systemOfRecordIdPath);
    
    //check the includes group
    Group includesGroup = GroupFinder.findByName(this.grouperSession, overallName + "_includes");

    //check the excludes group
    Group excludesGroup = GroupFinder.findByName(this.grouperSession, overallName + "_excludes");

    //check the excludes group
    Group systemOfRecordAndIncludesGroup = GroupFinder.findByName(this.grouperSession, overallName + "_systemOfRecordAndIncludes");

    //includes minus excludes
    Group includesMinusExcludes = GroupFinder.findByName(this.grouperSession, overallName + "_includesMinusExcludes");
    
    //first helper composite
    Group requireGroups1 = GroupFinder.findByName(this.grouperSession, overallName + "_requireGroups1");
    Group requireGroups2 = GroupFinder.findByName(this.grouperSession, overallName + "_requireGroups2");
    Group requireGroups3 = GroupFinder.findByName(this.grouperSession, overallName + "_requireGroups3", false);

    assertNull("Shouldnt need 3 helpers, only 2", requireGroups3);

    //structure will be
    //includesMinusExcludes is composite complement: systemOfRecordAndIncludesGroup minus excludesGroup
    //requireGroups2 is composite complement: includesMinusExcludes minus aStem:activeEmployee
    //requireGroups1 is composite complement: requireGroups2 minus aStem:anotherGroup
    //overallGroup is composite complement: requireGroups1 minus aStem:yetAnotherGroup
    
    //check the includesMinusExcludes group
    assertTrue(includesMinusExcludes.getComposite().getType().equals(CompositeType.COMPLEMENT));
    assertEquals(includesMinusExcludes.getComposite().getLeftGroup(), systemOfRecordAndIncludesGroup);
    assertEquals(includesMinusExcludes.getComposite().getRightGroup(), excludesGroup);

    //check the requireGroups2 group
    assertTrue(requireGroups2.getComposite().getType().equals(CompositeType.INTERSECTION));
    assertEquals(requireGroups2.getComposite().getLeftGroup(), includesMinusExcludes);
    assertEquals(requireGroups2.getComposite().getRightGroup(), activeEmployeeGroup);

    //check the requireGroups1 group
    assertTrue(requireGroups1.getComposite().getType().equals(CompositeType.INTERSECTION));
    assertEquals(requireGroups1.getComposite().getLeftGroup(), requireGroups2);
    assertEquals(requireGroups1.getComposite().getRightGroup(), anotherGroup);

    //check the overall group
    assertTrue(overallGroup.getComposite().getType().equals(CompositeType.INTERSECTION));
    assertEquals(overallGroup.getComposite().getLeftGroup(), requireGroups1);
    assertEquals(overallGroup.getComposite().getRightGroup(), yetAnotherGroup);
    
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
    aGroup.store();
    
    //refresh this
    overallGroup = GroupFinder.findByName(this.grouperSession, overallName);
    
    //check the system of record group
    systemOfRecordGroup = GroupFinder.findByName(this.grouperSession, systemOfRecordIdPath);
    
    //check the excludes group
    systemOfRecordAndIncludesGroup = GroupFinder.findByName(this.grouperSession, overallName + "_systemOfRecordAndIncludes");

    //includes minus excludes
    includesMinusExcludes = GroupFinder.findByName(this.grouperSession, overallName + "_includesMinusExcludes");
    
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
    assertTrue(includesMinusExcludes.getComposite().getType().equals(CompositeType.COMPLEMENT));
    assertEquals(includesMinusExcludes.getComposite().getLeftGroup(), systemOfRecordAndIncludesGroup);
    assertEquals(includesMinusExcludes.getComposite().getRightGroup(), excludesGroup);

    //check the overall group
    assertTrue(overallGroup.getComposite().getType().equals(CompositeType.INTERSECTION));
    assertEquals(overallGroup.getComposite().getLeftGroup(), includesMinusExcludes);
    assertEquals(overallGroup.getComposite().getRightGroup(), activeEmployeeGroup);
    
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
    aGroup.store();
    
    //and lets test the overall membership
    assertTrue(overallGroup.hasMember(SubjectTestHelper.SUBJ0));
    assertTrue(overallGroup.hasMember(SubjectTestHelper.SUBJ1));
    assertTrue(overallGroup.hasMember(SubjectTestHelper.SUBJ2));
    assertFalse(overallGroup.hasMember(SubjectTestHelper.SUBJ3));
    assertTrue(overallGroup.hasMember(SubjectTestHelper.SUBJ4));
    assertFalse(overallGroup.hasMember(SubjectTestHelper.SUBJ5));
    assertTrue(overallGroup.hasMember(SubjectTestHelper.SUBJ6));
    assertTrue(overallGroup.hasMember(SubjectTestHelper.SUBJ7));
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.GrouperTest#tearDown()
   */
  @Override
  protected void tearDown() {
    super.tearDown();
    GrouperSession.stopQuietly(this.grouperSession);
  }
  
}

