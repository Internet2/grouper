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
 * @author mchyzer
 * $Id: GrouperLoaderSecurityTest.java,v 1.5 2009-08-11 20:18:08 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.app.loader;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GroupType;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.hooks.examples.GroupTypeSecurityHook;
import edu.internet2.middleware.grouper.hooks.logic.HookVeto;
import edu.internet2.middleware.grouper.misc.GrouperCheckConfig;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.subject.Subject;


/**
 * 
 */
public class GrouperLoaderSecurityTest extends GrouperTest {

  /**
   * @param name
   */
  public GrouperLoaderSecurityTest(String name) {
    super(name);
  }

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new GrouperLoaderSecurityTest("testSecurityUserEditTypeNonGroupFail"));
  }
  
  /**
   * 
   * @see edu.internet2.middleware.grouper.helper.GrouperTest#setUp()
   */
  @Override
  protected void setUp() {
    super.setUp();
    try {
      GroupTypeSecurityHook.resetCacheSettings();

      this.grouperSession = GrouperSession.startRootSession();

      //make sure a user can change a type
      this.groupType = GroupType.createType(grouperSession, "groupType", false);

      this.attr = this.groupType.addAttribute(grouperSession,"attribute", 
          false);
    
      String groupName = "aStem:testUserEditType";
      this.group = Group.saveGroup(this.grouperSession, groupName, 
          null, groupName, null, null, null, true);

      String groupNameTyped = "aStem:testUserEditTyped";

      this.groupWithType = Group.saveGroup(this.grouperSession, groupNameTyped, 
          null, groupNameTyped, null, null, null, true);
      
      this.groupWithType.addType(this.groupType);
      
      this.groupWithType.setAttribute(this.attr.getLegacyAttributeName(true), "fieldValue");
  
      String groupToBeInToEditName = "aStem:groupToBeInToEdit";
      
      this.groupToBeInToEdit = Group.saveGroup(this.grouperSession, groupToBeInToEditName, 
          null, groupToBeInToEditName, null, null, null, true);
      
      String wheelGroupName = "etc:wheelGroup";

      GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.wheel.use", "true");
      GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.wheel.group", wheelGroupName);

      this.wheelGroup = Group.saveGroup(this.grouperSession, wheelGroupName, 
          null, wheelGroupName, null, null, null, true);

    } catch (Exception e) {
      throw new RuntimeException(e);
    }

  }
  
  /** group to add a type to */
  private Group group;

  /** group to add a type to */
  private Group groupToBeInToEdit;

  /** group to add a type to */
  private Group wheelGroup;

  /** group to add a type to */
  private Group groupWithType;
  
  /**
   * group type
   */
  private GroupType groupType;

  /**
   * attr
   */
  private AttributeDefName attr;
  
  /**
   * 
   */
  private GrouperSession grouperSession;
  
  /**
   * 
   * @see edu.internet2.middleware.grouper.helper.GrouperTest#tearDown()
   */
  @Override
  protected void tearDown() {
    GrouperSession.stopQuietly(this.grouperSession);
    super.tearDown();
    GroupTypeSecurityHook.resetCacheSettings();
  }

  /**
   * make sure configured groups get autocreated
   * @throws Exception 
   */
  public void testGroupAutoCreate() throws Exception {
    
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("configuration.autocreate.system.groups", "true");
    String groupName = "etc:someGroup";
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("security.types.groupType.allowOnlyGroup", groupName);
    GroupTypeSecurityHook.registerHookIfNecessary(true);
    GroupTypeSecurityHook.resetCacheSettings();
    
    Group group = GroupFinder.findByName(this.grouperSession, groupName, false);
    
    assertNull("Shouldnt find group yet", group);
    
    GrouperCheckConfig.checkGroups();
    
    group = GroupFinder.findByName(this.grouperSession, groupName, false);
    
    assertNotNull("Should find group now", group);
  }
  
  /**
   * edit a type and attribute of a group as a user with admin privileges
   * @throws Exception 
   * 
   */
  public void testSecurityUserEditType() throws Exception {
    
    userEditTypeHelper(SubjectTestHelper.SUBJ0);
    
  }

  /**
   * edit a type and attribute of a group as a user with admin privileges
   * @throws Exception 
   * 
   */
  public void testSecurityUserEditTypeNonGroupFail() throws Exception {

    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("security.types.groupType.allowOnlyGroup", this.groupToBeInToEdit.getName());
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperIncludeExclude.requireGroups.use", "true");
    GroupTypeSecurityHook.registerHookIfNecessary(true);
    GroupTypeSecurityHook.resetCacheSettings();

    assertFalse(this.group.hasType(this.groupType));
    assertFalse(this.group.getAttributesMap(true).containsKey(this.attr.getLegacyAttributeName(true)));

    try {
      userEditTypeHelper(SubjectTestHelper.SUBJ0);
      fail("should be vetoed");
    } catch (HookVeto hv) {
      //good
    }
    //refresh it
    this.group = GroupFinder.findByName(this.grouperSession, this.group.getName(), true);
    
    //type should not be applied
    assertFalse(this.group.hasType(this.groupType));
    assertFalse(this.group.getAttributesMap(true).containsKey(this.attr.getLegacyAttributeName(true)));

    assertTrue(this.groupWithType.hasType(this.groupType));
    assertEquals("fieldValue", this.groupWithType.getAttributeValue(this.attr.getLegacyAttributeName(true), false, true));

    try {
      userUpdateTypeHelper(SubjectTestHelper.SUBJ0, true);
      fail("should veto");
    } catch (HookVeto hv) {
      //good
    }

    this.groupWithType = GroupFinder.findByName(this.grouperSession, this.groupWithType.getName(), true);

    assertTrue(this.groupWithType.hasType(this.groupType));
    assertEquals("fieldValue", this.groupWithType.getAttributeValue(this.attr.getLegacyAttributeName(true), false, true));

  }

  /**
   * edit a type and attribute of a group as a user with admin privileges
   * @throws Exception 
   * 
   */
  public void testSecurityUserEditTypeGroupSucceed() throws Exception {

    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("security.types.groupType.allowOnlyGroup", this.groupToBeInToEdit.getName());
    GroupTypeSecurityHook.registerHookIfNecessary(true);
    GroupTypeSecurityHook.resetCacheSettings();

    this.groupToBeInToEdit.addMember(SubjectTestHelper.SUBJ0);
    
    userEditTypeHelper(SubjectTestHelper.SUBJ0);
    
    userUpdateTypeHelper(SubjectTestHelper.SUBJ0, true);
  }

  /**
   * @param subject 
   * @throws Exception
   */
  private void userEditTypeHelper(Subject subject) throws Exception {
    
    group.grantPriv(subject, AccessPrivilege.ADMIN, false);

    GrouperSession.stopQuietly(this.grouperSession);
    
    try {
      this.grouperSession = GrouperSession.start(subject);
      
      //do some inserts and deletes here
      
      group.addType(this.groupType);
      assertTrue(this.group.hasType(this.groupType));
      group.setAttribute(this.attr.getLegacyAttributeName(true), "test");

      assertEquals("test", this.group.getAttributeValue(this.attr.getLegacyAttributeName(true), false, true));
      
      group.setAttribute(this.attr.getLegacyAttributeName(true), "test2");

      assertEquals("test2", this.group.getAttributeValue(this.attr.getLegacyAttributeName(true), false, true));
      
      group.deleteAttribute(this.attr.getLegacyAttributeName(true));

      assertFalse(this.group.getAttributesMap(true).containsKey(this.attr.getLegacyAttributeName(true)));
      
      group.deleteType(this.groupType);
      assertFalse(this.group.hasType(this.groupType));
    
    } finally {
      GrouperSession.stopQuietly(this.grouperSession);
      //go back to normal
      this.grouperSession = GrouperSession.startRootSession();
    }
  }
  
  /**
   * @param subject 
   * @param doAttributeFirst if we should edit attribute first
   * @throws Exception
   */
  private void userUpdateTypeHelper(Subject subject, boolean doAttributeFirst) throws Exception {
    
    groupWithType.grantPriv(subject, AccessPrivilege.ADMIN, false);

    GrouperSession.stopQuietly(this.grouperSession);
    
    try {
      this.grouperSession = GrouperSession.start(subject);

      if (doAttributeFirst) {
        //do some updates and deletes here
        groupWithType.setAttribute(this.attr.getLegacyAttributeName(true), "test2");
  
        assertEquals("test2", this.groupWithType.getAttributeValue(this.attr.getLegacyAttributeName(true), false, true));
      }
      
      groupWithType.deleteType(this.groupType);

      assertFalse(this.groupWithType.hasType(this.groupType));
      
    } finally {
      GrouperSession.stopQuietly(this.grouperSession);
      //go back to normal
      this.grouperSession = GrouperSession.startRootSession();
    }
  }

  /**
   * edit a type and attribute of a group as a user with admin privileges
   * @throws Exception 
   * 
   */
  public void testSecurityUserEditTypeWheelFail() throws Exception {

    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("security.types.groupType.wheelOnly", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperIncludeExclude.requireGroups.use", "true");
    GroupTypeSecurityHook.registerHookIfNecessary(true);
    GroupTypeSecurityHook.resetCacheSettings();

    GrouperSession testSession = GrouperSession.start(SubjectTestHelper.SUBJ0, false);
    assertFalse(PrivilegeHelper.isRoot(testSession));
    assertFalse(PrivilegeHelper.isWheel(testSession));
    
    try {
      userEditTypeHelper(SubjectTestHelper.SUBJ0);
      fail("should be vetoed");
    } catch (HookVeto hv) {
      //good
    }
    
    this.group = GroupFinder.findByName(this.grouperSession, this.group.getName(), true);
    
    //type should not be applied
    assertFalse(this.group.hasType(this.groupType));
    assertFalse(this.group.getAttributesMap(true).containsKey(this.attr.getLegacyAttributeName(true)));
  
    try {
      userUpdateTypeHelper(SubjectTestHelper.SUBJ0, true);
      fail("should veto");
    } catch (HookVeto hv) {
      //good
    }

    this.groupWithType = GroupFinder.findByName(this.grouperSession, this.groupWithType.getName(), true);

    assertTrue(this.groupWithType.hasType(this.groupType));
    assertEquals("fieldValue", this.groupWithType.getAttributeValue(this.attr.getLegacyAttributeName(true), false, true));
  
  }

  /**
   * edit a type and attribute of a group as a user with admin privileges
   * @throws Exception 
   * 
   */
  public void testSecurityUserEditTypeWheelSucceed() throws Exception {
  
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("security.types.groupType.wheelOnly", "true");
    GroupTypeSecurityHook.registerHookIfNecessary(true);
    GroupTypeSecurityHook.resetCacheSettings();

    this.wheelGroup.addMember(SubjectTestHelper.SUBJ0);
    
    userEditTypeHelper(SubjectTestHelper.SUBJ0);
    
    userUpdateTypeHelper(SubjectTestHelper.SUBJ0, true);
  }
  
}
