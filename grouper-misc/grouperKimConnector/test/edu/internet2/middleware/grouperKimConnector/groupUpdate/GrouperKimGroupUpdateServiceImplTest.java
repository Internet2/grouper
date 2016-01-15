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
/**
 * @author mchyzer
 * $Id: GrouperKimGroupUpdateServiceImplTest.java,v 1.1 2009-12-21 06:15:06 mchyzer Exp $
 */
package edu.internet2.middleware.grouperKimConnector.groupUpdate;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GroupType;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.ws.util.RestClientSettings;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.grouperKimConnector.util.GrouperKimUtils;
import junit.textui.TestRunner;
import org.kuali.rice.kim.bo.group.dto.GroupInfo;
import org.kuali.rice.kim.bo.types.dto.AttributeSet;


/**
 *
 */
public class GrouperKimGroupUpdateServiceImplTest extends GrouperTest {

  /** root session */
  private GrouperSession grouperSession;
  /** group type */
  private GroupType groupType = null;

  /**
   * 
   */
  public GrouperKimGroupUpdateServiceImplTest() {
    super();
    
  }

  /**
   * @param name
   */
  public GrouperKimGroupUpdateServiceImplTest(String name) {
    super(name);
    
  }

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    
    //TestRunner.run(GrouperKimGroupUpdateServiceImplTest.class);
    TestRunner.run(new GrouperKimGroupUpdateServiceImplTest("testRemoveAllMembersFromGroup"));
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.helper.GrouperTest#setUp()
   */
  @Override
  protected void setUp() {
    // dont do this, it deletes types
    // super.setUp();
    
    this.grouperSession = GrouperSession.startRootSession();
  
    GrouperClientUtils.grouperClientOverrideMap().put("kim.stem", "test:kim");
    GrouperClientUtils.grouperClientOverrideMap().put("grouper.types.of.kim.groups", "someType");
    GrouperClientUtils.grouperClientOverrideMap().put("grouper.kim.plugin.subjectSourceId", "jdbc");
    GrouperClientUtils.grouperClientOverrideMap().put("grouper.kim.plugin.subjectSourceIds", "jdbc");
  
    this.groupType = GroupType.createType(this.grouperSession, "someType", false);
    
    this.groupType.addAttribute(this.grouperSession, "anAttribute");
  
    String wsUserLabel = GrouperClientUtils.propertiesValue(
        "grouperClient.webService.user.label", true);
    String wsUserString = GrouperClientUtils.propertiesValue(
        "grouperClient.webService." + wsUserLabel, true);
  
    RestClientSettings.resetData(wsUserString, false);
  
    //create the kim stem
    new StemSave(this.grouperSession).assignStemNameToEdit(GrouperKimUtils.kimStem())
      .assignName(GrouperKimUtils.kimStem()).assignCreateParentStemsIfNotExist(true).save();
  
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.helper.GrouperTest#tearDown()
   */
  @Override
  protected void tearDown() {
    GrouperSession.stopQuietly(this.grouperSession);
    super.tearDown();
  }

  /**
   * 
   */
  public void testAddGroupToGroup() {
    
    GroupInfo groupInfo = new GroupInfo();
    groupInfo.setActive(true);
    groupInfo.setGroupDescription("description");
    groupInfo.setNamespaceCode("test");
    groupInfo.setGroupName("testGroup");
    
    groupInfo = new GrouperKimGroupUpdateServiceImpl().createGroup(groupInfo);
    
    GroupInfo groupInfo2 = new GroupInfo();
    groupInfo2.setActive(true);
    groupInfo2.setGroupDescription("description2");
    groupInfo2.setNamespaceCode("test");
    groupInfo2.setGroupName("testGroup2");
    
    groupInfo2 = new GrouperKimGroupUpdateServiceImpl().createGroup(groupInfo2);
    
    boolean result = new GrouperKimGroupUpdateServiceImpl().addGroupToGroup(groupInfo2.getGroupId(), groupInfo.getGroupId());
    
    assertTrue("added", result);
  
    //lets see if member
    Group group = GroupFinder.findByName(this.grouperSession, GrouperKimUtils.kimStem() + ":test:testGroup", true);
    
    Group group2 = GroupFinder.findByName(this.grouperSession, GrouperKimUtils.kimStem() + ":test:testGroup2", true);
    
    assertTrue(group.hasMember(group2.toSubject()));
    
    result = new GrouperKimGroupUpdateServiceImpl().addGroupToGroup(groupInfo2.getGroupId(), groupInfo.getGroupId());
    
    assertFalse("already exists", result);
    
  }

  /**
   * 
   */
  public void testAddPrincipalToGroup() {
    
    GroupInfo groupInfo = new GroupInfo();
    groupInfo.setActive(true);
    groupInfo.setGroupDescription("description");
    groupInfo.setNamespaceCode("test");
    groupInfo.setGroupName("testGroup");
    
    groupInfo = new GrouperKimGroupUpdateServiceImpl().createGroup(groupInfo);
    
    Group group = GroupFinder.findByName(this.grouperSession, GrouperKimUtils.kimStem() + ":test:testGroup", true);
    
    boolean result = new GrouperKimGroupUpdateServiceImpl().addPrincipalToGroup("test.subject.0", groupInfo.getGroupId());
    
    assertTrue("added", result);
    
    assertTrue(group.hasMember(SubjectFinder.findById("test.subject.0", true)));
    
    result = new GrouperKimGroupUpdateServiceImpl().addPrincipalToGroup("test.subject.0", groupInfo.getGroupId());
    
    assertFalse("already added", result);
  }

  /**
   * 
   */
  public void testCreateGroup() {
    
    //try without type
    GrouperClientUtils.grouperClientOverrideMap().remove("grouper.types.of.kim.groups");
  
    GroupInfo groupInfo = new GroupInfo();
    groupInfo.setActive(true);
    groupInfo.setGroupDescription("description");
    groupInfo.setNamespaceCode("test");
    groupInfo.setGroupName("testGroup");
    
    new GrouperKimGroupUpdateServiceImpl().createGroup(groupInfo);
    
    Group group = GroupFinder.findByName(this.grouperSession, GrouperKimUtils.kimStem() + ":test:testGroup", true);
    
    assertEquals("description", group.getDescription());
    assertEquals(GrouperKimUtils.kimStem() + ":test:testGroup", group.getName());
    assertEquals(GrouperKimUtils.kimStem() + ":test:testGroup", group.getDisplayName());
    assertFalse(group.hasType(this.groupType));
  
  }

  /**
   * 
   */
  public void testCreateGroupWithAttribute() {
    
    GroupInfo groupInfo = new GroupInfo();
    groupInfo.setActive(true);
    groupInfo.setGroupDescription("description");
    groupInfo.setNamespaceCode("test");
    groupInfo.setGroupName("testGroup");
    AttributeSet attributeSet = new AttributeSet();
    attributeSet.put("anAttribute", "testingAnAttribute");
    groupInfo.setAttributes(attributeSet);
    
    new GrouperKimGroupUpdateServiceImpl().createGroup(groupInfo);
    
    Group group = GroupFinder.findByName(this.grouperSession, GrouperKimUtils.kimStem() + ":test:testGroup", true);
    
    assertEquals("description", group.getDescription());
    assertEquals(GrouperKimUtils.kimStem() + ":test:testGroup", group.getName());
    assertEquals(GrouperKimUtils.kimStem() + ":test:testGroup", group.getDisplayName());
    assertTrue(group.hasType(this.groupType));
    assertEquals("testingAnAttribute", group.getAttributeValue("anAttribute", false, true));
  }

  /**
   * 
   */
  public void testRemoveAllMembersFromGroup() {
    
    GroupInfo groupInfo = new GroupInfo();
    groupInfo.setActive(true);
    groupInfo.setGroupDescription("description");
    groupInfo.setNamespaceCode("test");
    groupInfo.setGroupName("testGroup");
    
    groupInfo = new GrouperKimGroupUpdateServiceImpl().createGroup(groupInfo);
    
    Group group = GroupFinder.findByName(this.grouperSession, GrouperKimUtils.kimStem() + ":test:testGroup", true);
    
    boolean result = new GrouperKimGroupUpdateServiceImpl().addPrincipalToGroup("test.subject.0", groupInfo.getGroupId());
    
    assertTrue("added", result);
    
    assertTrue(group.hasMember(SubjectFinder.findById("test.subject.0", true)));
  
    //add group
    GroupInfo groupInfo2 = new GroupInfo();
    groupInfo2.setActive(true);
    groupInfo2.setGroupDescription("description2");
    groupInfo2.setNamespaceCode("test");
    groupInfo2.setGroupName("testGroup2");
    
    groupInfo2 = new GrouperKimGroupUpdateServiceImpl().createGroup(groupInfo2);
    
    result = new GrouperKimGroupUpdateServiceImpl().addGroupToGroup(groupInfo2.getGroupId(), groupInfo.getGroupId());
    
    assertTrue("added", result);
    
    new GrouperKimGroupUpdateServiceImpl().removeAllGroupMembers(groupInfo.getGroupId());
    
    assertFalse(group.hasMember(SubjectFinder.findById("test.subject.0", true)));
    
    Group group2 = GroupFinder.findByName(this.grouperSession, GrouperKimUtils.kimStem() + ":test:testGroup2", true);
    
    assertFalse(group.hasMember(group2.toSubject()));
    
  }

  /**
   * 
   */
  public void testRemoveGroupToGroup() {
    
    GroupInfo groupInfo = new GroupInfo();
    groupInfo.setActive(true);
    groupInfo.setGroupDescription("description");
    groupInfo.setNamespaceCode("test");
    groupInfo.setGroupName("testGroup");
    
    groupInfo = new GrouperKimGroupUpdateServiceImpl().createGroup(groupInfo);
    
    GroupInfo groupInfo2 = new GroupInfo();
    groupInfo2.setActive(true);
    groupInfo2.setGroupDescription("description2");
    groupInfo2.setNamespaceCode("test");
    groupInfo2.setGroupName("testGroup2");
    
    groupInfo2 = new GrouperKimGroupUpdateServiceImpl().createGroup(groupInfo2);
    
    boolean result = new GrouperKimGroupUpdateServiceImpl().addGroupToGroup(groupInfo2.getGroupId(), groupInfo.getGroupId());
    
    assertTrue("added", result);
  
    //lets see if member
    Group group = GroupFinder.findByName(this.grouperSession, GrouperKimUtils.kimStem() + ":test:testGroup", true);
    
    Group group2 = GroupFinder.findByName(this.grouperSession, GrouperKimUtils.kimStem() + ":test:testGroup2", true);
    
    assertTrue(group.hasMember(group2.toSubject()));
    
    result = new GrouperKimGroupUpdateServiceImpl().removeGroupFromGroup(groupInfo2.getGroupId(), groupInfo.getGroupId());
    
    assertTrue("removed", result);
  
    assertFalse(group.hasMember(group2.toSubject()));
    
    result = new GrouperKimGroupUpdateServiceImpl().removeGroupFromGroup(groupInfo2.getGroupId(), groupInfo.getGroupId());
    
    assertFalse("already removed", result);
    
    
  }

  /**
   * 
   */
  public void testRemovePrincipalFromGroup() {
    
    GroupInfo groupInfo = new GroupInfo();
    groupInfo.setActive(true);
    groupInfo.setGroupDescription("description");
    groupInfo.setNamespaceCode("test");
    groupInfo.setGroupName("testGroup");
    
    groupInfo = new GrouperKimGroupUpdateServiceImpl().createGroup(groupInfo);
    
    Group group = GroupFinder.findByName(this.grouperSession, GrouperKimUtils.kimStem() + ":test:testGroup", true);
    
    boolean result = new GrouperKimGroupUpdateServiceImpl().addPrincipalToGroup("test.subject.0", groupInfo.getGroupId());
    
    assertTrue("added", result);
    
    assertTrue(group.hasMember(SubjectFinder.findById("test.subject.0", true)));
    
    result = new GrouperKimGroupUpdateServiceImpl().removePrincipalFromGroup("test.subject.0", groupInfo.getGroupId());
    
    assertTrue("removed", result);
  
    assertFalse(group.hasMember(SubjectFinder.findById("test.subject.0", true)));
    
    result = new GrouperKimGroupUpdateServiceImpl().removePrincipalFromGroup("test.subject.0", groupInfo.getGroupId());
    
    assertFalse("already removed", result);
    
    
  }

  /**
   * 
   */
  public void testUpdateGroup() {
    
    //create a group
    
    //try without type
    GrouperClientUtils.grouperClientOverrideMap().remove("grouper.types.of.kim.groups");
  
    GroupInfo groupInfo = new GroupInfo();
    groupInfo.setActive(true);
    groupInfo.setGroupDescription("description");
    groupInfo.setNamespaceCode("test");
    groupInfo.setGroupName("testGroup");
    
    new GrouperKimGroupUpdateServiceImpl().createGroup(groupInfo);
    
    Group group = GroupFinder.findByName(this.grouperSession, GrouperKimUtils.kimStem() + ":test:testGroup", true);
    
    assertEquals("description", group.getDescription());
    assertEquals(GrouperKimUtils.kimStem() + ":test:testGroup", group.getName());
    assertEquals(GrouperKimUtils.kimStem() + ":test:testGroup", group.getDisplayName());
    assertFalse(group.hasType(this.groupType));
    
    //update it
    groupInfo.setGroupDescription("newDescription");
    
    new GrouperKimGroupUpdateServiceImpl().updateGroup(group.getUuid(), groupInfo);
    
    group = GroupFinder.findByName(this.grouperSession, GrouperKimUtils.kimStem() + ":test:testGroup", true);
    
    assertEquals("description", group.getDescription());
    assertEquals(GrouperKimUtils.kimStem() + ":test:testGroup", group.getName());
    assertEquals(GrouperKimUtils.kimStem() + ":test:testGroup", group.getDisplayName());
    assertFalse(group.hasType(this.groupType));
    
  }

}
