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
 * $Id: GrouperKimGroupServiceImplTest.java,v 1.2 2009-12-21 06:15:06 mchyzer Exp $
 */
package edu.internet2.middleware.grouperKimConnector.group;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.textui.TestRunner;

import org.kuali.rice.kim.bo.group.dto.GroupInfo;
import org.kuali.rice.kim.bo.group.dto.GroupMembershipInfo;
import org.kuali.rice.kim.bo.types.dto.AttributeSet;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GroupType;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.MembershipFinder;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.Stem.Scope;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.ws.util.RestClientSettings;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.grouperKimConnector.groupUpdate.GrouperKimGroupUpdateServiceImpl;
import edu.internet2.middleware.grouperKimConnector.util.GrouperKimUtils;


/**
 *
 */
public class GrouperKimGroupServiceImplTest extends GrouperTest {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    
    //TestRunner.run(GrouperKimGroupServiceImplTest.class);
    TestRunner.run(new GrouperKimGroupServiceImplTest("testGetGroupIdsForPrincipal"));
  }
  
  /** root session */
  private GrouperSession grouperSession;
  
  /**
   * 
   * @see edu.internet2.middleware.grouper.helper.GrouperTest#tearDown()
   */
  @Override
  protected void tearDown() {
    GrouperSession.stopQuietly(this.grouperSession);
    super.tearDown();
  }

  /** group type */
  private GroupType groupType = null;
  
  /** */
  private Group nonKimTestGroup = null;

  /** */
  private Group nonKimTestGroup2 = null;

  /** */
  private Stem kimStem = null;
  
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
    GrouperClientUtils.grouperClientOverrideMap().put("kim.override.groupTypeId", "1");

    this.groupType = GroupType.createType(this.grouperSession, "someType", false);
    
    this.groupType.addAttribute(this.grouperSession, "anAttribute");

    String wsUserLabel = GrouperClientUtils.propertiesValue(
        "grouperClient.webService.user.label", true);
    String wsUserString = GrouperClientUtils.propertiesValue(
        "grouperClient.webService." + wsUserLabel, true);

    RestClientSettings.resetData(wsUserString, false);

    //create the kim stem
    this.kimStem = new StemSave(this.grouperSession).assignStemNameToEdit(GrouperKimUtils.kimStem())
      .assignName(GrouperKimUtils.kimStem()).assignCreateParentStemsIfNotExist(true).save();
    
    //create a non kim stem to make sure we arent getting groups we dont care about
    new StemSave(this.grouperSession).assignStemNameToEdit("nonKim")
      .assignName("nonKim").assignCreateParentStemsIfNotExist(true).save();
    
    this.nonKimTestGroup = new GroupSave(this.grouperSession).assignGroupNameToEdit("nonKim:testGroup")
      .assignName("nonKim:testGroup").save();
    
    this.nonKimTestGroup.addMember(SubjectFinder.findById("test.subject.0", true));
    this.nonKimTestGroup.addMember(SubjectFinder.findById("test.subject.1", true));

    this.nonKimTestGroup2 = new GroupSave(this.grouperSession).assignGroupNameToEdit("nonKim:testGroup2")
      .assignName("nonKim:testGroup2").save();
    
    this.nonKimTestGroup2.addMember(SubjectFinder.findById("test.subject.0", true));
    this.nonKimTestGroup2.addMember(SubjectFinder.findById("test.subject.1", true));
    
    this.nonKimTestGroup.addMember(this.nonKimTestGroup2.toSubject());
  }

  /**
   * 
   */
  public GrouperKimGroupServiceImplTest() {
    super();
    
  }

  /**
   * @param name
   */
  public GrouperKimGroupServiceImplTest(String name) {
    super(name);
    
  }
  
  /**
   * 
   */
  public void testGetDirectGroupIdsForPrincipal() {
    
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

    Group group = GroupFinder.findByName(this.grouperSession, GrouperKimUtils.kimStem() + ":test:testGroup", true);
    Group group2 = GroupFinder.findByName(this.grouperSession, GrouperKimUtils.kimStem() + ":test:testGroup2", true);
    
    result = new GrouperKimGroupUpdateServiceImpl().addPrincipalToGroup("test.subject.0", groupInfo2.getGroupId());
    
    assertTrue("added", result);
    
    assertTrue(group2.hasMember(SubjectFinder.findById("test.subject.0", true)));
    assertTrue(group.hasMember(SubjectFinder.findById("test.subject.0", true)));
    
    List<String> groupIds = new GrouperKimGroupServiceImpl().getDirectGroupIdsForPrincipal("test.subject.0");
    
    assertEquals(1, groupIds.size());
    assertEquals(group2.getUuid(), groupIds.get(0));
  }
  
  /**
   * 
   */
  public void testGetDirectMemberGroupIds() {
    
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

    Group group = GroupFinder.findByName(this.grouperSession, GrouperKimUtils.kimStem() + ":test:testGroup", true);
    Group group2 = GroupFinder.findByName(this.grouperSession, GrouperKimUtils.kimStem() + ":test:testGroup2", true);
    
    result = new GrouperKimGroupUpdateServiceImpl().addPrincipalToGroup("test.subject.0", groupInfo2.getGroupId());
    
    assertTrue("added", result);
    
    assertTrue(group2.hasMember(SubjectFinder.findById("test.subject.0", true)));
    assertTrue(group.hasMember(SubjectFinder.findById("test.subject.0", true)));
    
    group.addMember(this.nonKimTestGroup.toSubject());
    
    List<String> groupIds = new GrouperKimGroupServiceImpl().getDirectMemberGroupIds(group.getUuid());
    assertEquals(1, groupIds.size());
    assertEquals(group2.getUuid(), groupIds.get(0));
    
    groupIds = new GrouperKimGroupServiceImpl().getDirectMemberGroupIds(group2.getUuid());
    assertEquals(0, GrouperClientUtils.length(groupIds));
  }
  
  /**
   * 
   */
  @SuppressWarnings("unused")
  public void testGetDirectMemberPrincipalIds() {
    
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

    Group group = GroupFinder.findByName(this.grouperSession, GrouperKimUtils.kimStem() + ":test:testGroup", true);

    Group group2 = GroupFinder.findByName(this.grouperSession, GrouperKimUtils.kimStem() + ":test:testGroup2", true);
    
    group.addMember(this.nonKimTestGroup.toSubject());

    result = new GrouperKimGroupUpdateServiceImpl().addPrincipalToGroup("test.subject.0", groupInfo2.getGroupId());
    assertTrue("added", result);
    result = new GrouperKimGroupUpdateServiceImpl().addPrincipalToGroup("test.subject.1", groupInfo.getGroupId());
    assertTrue("added", result);

    List<String> principalIds = new GrouperKimGroupServiceImpl().getDirectMemberPrincipalIds(group.getUuid());
    
    assertEquals(1, principalIds.size());
    assertEquals("test.subject.1", principalIds.get(0));
    
  }
  
  /**
   * 
   */
  public void testGetDirectParentGroupIds() {
    
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
    
    assertTrue(result);
    
    GroupInfo groupInfo3 = new GroupInfo();
    groupInfo3.setActive(true);
    groupInfo3.setGroupDescription("description3");
    groupInfo3.setNamespaceCode("test");
    groupInfo3.setGroupName("testGroup3");
    
    groupInfo3 = new GrouperKimGroupUpdateServiceImpl().createGroup(groupInfo3);

    Group group2 = GroupFinder.findByName(this.grouperSession, GrouperKimUtils.kimStem() + ":test:testGroup2", true);
    Group group3 = GroupFinder.findByName(this.grouperSession, GrouperKimUtils.kimStem() + ":test:testGroup3", true);

    result = new GrouperKimGroupUpdateServiceImpl().addGroupToGroup(groupInfo3.getGroupId(), groupInfo2.getGroupId());
    
    assertTrue(result);
    
    this.nonKimTestGroup.addMember(group3.toSubject());
    
    List<String> groupIds = new GrouperKimGroupServiceImpl().getDirectParentGroupIds(group3.getUuid());
    assertEquals(1, groupIds.size());
    assertEquals(group2.getUuid(), groupIds.get(0));
  }
  
  /**
   * 
   */
  public void testGetGroupAttributes() {
    GroupInfo groupInfo = new GroupInfo();
    groupInfo.setActive(true);
    groupInfo.setGroupDescription("description");
    groupInfo.setNamespaceCode("test");
    groupInfo.setGroupName("testGroup");
    AttributeSet attributeSet = new AttributeSet();
    attributeSet.put("anAttribute", "testingAnAttribute");
    groupInfo.setAttributes(attributeSet);
    
    groupInfo = new GrouperKimGroupUpdateServiceImpl().createGroup(groupInfo);

    Group group = GroupFinder.findByName(this.grouperSession, GrouperKimUtils.kimStem() + ":test:testGroup", true);
    
    assertEquals("testingAnAttribute", group.getAttributeValue("anAttribute", false, true));
    
    Map<String, String> attributes = new GrouperKimGroupServiceImpl().getGroupAttributes(groupInfo.getGroupId());
    
    assertEquals(1, attributes.size());
    assertEquals("testingAnAttribute", attributes.get("anAttribute"));
    
    
  }
  
  /**
   * 
   */
  public void testGetGroupIdsForPrincipal() {
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

    Group group = GroupFinder.findByName(this.grouperSession, GrouperKimUtils.kimStem() + ":test:testGroup", true);

    Group group2 = GroupFinder.findByName(this.grouperSession, GrouperKimUtils.kimStem() + ":test:testGroup2", true);
    
    group.addMember(this.nonKimTestGroup.toSubject());

    result = new GrouperKimGroupUpdateServiceImpl().addPrincipalToGroup("test.subject.0", groupInfo2.getGroupId());
    assertTrue("added", result);
    result = new GrouperKimGroupUpdateServiceImpl().addPrincipalToGroup("test.subject.1", groupInfo.getGroupId());
    assertTrue("added", result);

    List<String> groupIds = new GrouperKimGroupServiceImpl().getGroupIdsForPrincipal("test.subject.0");
    
    assertEquals(2, groupIds.size());
    
    assertTrue(groupIds.contains(group.getUuid()));
    assertTrue(groupIds.contains(group2.getUuid()));
    
    
  }
  
  /**
   * 
   */
  public void testGetGroupIdsForPrincipalByNamespace() {
    
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

    Group group = GroupFinder.findByName(this.grouperSession, GrouperKimUtils.kimStem() + ":test:testGroup", true);

    Group group2 = GroupFinder.findByName(this.grouperSession, GrouperKimUtils.kimStem() + ":test:testGroup2", true);
    
    group.addMember(this.nonKimTestGroup.toSubject());

    result = new GrouperKimGroupUpdateServiceImpl().addPrincipalToGroup("test.subject.0", groupInfo2.getGroupId());
    assertTrue("added", result);
    result = new GrouperKimGroupUpdateServiceImpl().addPrincipalToGroup("test.subject.1", groupInfo.getGroupId());
    assertTrue("added", result);

    List<String> groupIds = new GrouperKimGroupServiceImpl().getGroupIdsForPrincipalByNamespace("test.subject.0", "test2");

    assertEquals(0, GrouperClientUtils.length(groupIds));
    
    groupIds = new GrouperKimGroupServiceImpl().getGroupIdsForPrincipalByNamespace("test.subject.0", "test");
    
    assertEquals(2, groupIds.size());
    
    assertTrue(groupIds.contains(group.getUuid()));
    assertTrue(groupIds.contains(group2.getUuid()));
    
  }
  
  /**
   * 
   */
  public void testGetGroupInfo() {
    GroupInfo groupInfo = new GroupInfo();
    groupInfo.setActive(true);
    groupInfo.setGroupDescription("description");
    groupInfo.setNamespaceCode("test");
    groupInfo.setGroupName("testGroup");
    AttributeSet attributeSet = new AttributeSet();
    attributeSet.put("anAttribute", "testingAnAttribute");
    groupInfo.setAttributes(attributeSet);
    
    groupInfo = new GrouperKimGroupUpdateServiceImpl().createGroup(groupInfo);
    
    String groupId = groupInfo.getGroupId();
    
    groupInfo = new GrouperKimGroupServiceImpl().getGroupInfo(groupInfo.getGroupId());
    
    assertEquals(true, groupInfo.isActive());
    assertEquals("description", groupInfo.getGroupDescription());
    assertEquals("test", groupInfo.getNamespaceCode());
    assertEquals("testGroup", groupInfo.getGroupName());
    assertEquals("testingAnAttribute", groupInfo.getAttributes().get("anAttribute"));
    assertEquals(groupId, groupInfo.getGroupId());
  }
  
  /**
   * 
   */
  public void testGetGroupInfoByName() {
    GroupInfo groupInfo = new GroupInfo();
    groupInfo.setActive(true);
    groupInfo.setGroupDescription("description");
    groupInfo.setNamespaceCode("test");
    groupInfo.setGroupName("testGroup");
    AttributeSet attributeSet = new AttributeSet();
    attributeSet.put("anAttribute", "testingAnAttribute");
    groupInfo.setAttributes(attributeSet);
    
    groupInfo = new GrouperKimGroupUpdateServiceImpl().createGroup(groupInfo);
    
    String groupId = groupInfo.getGroupId();
    
    groupInfo = new GrouperKimGroupServiceImpl().getGroupInfoByName("test", "testGroup");
    
    assertEquals(true, groupInfo.isActive());
    assertEquals("description", groupInfo.getGroupDescription());
    assertEquals("test", groupInfo.getNamespaceCode());
    assertEquals("testGroup", groupInfo.getGroupName());
    assertEquals("testingAnAttribute", groupInfo.getAttributes().get("anAttribute"));
    assertEquals(groupId, groupInfo.getGroupId());
    
  }
  
  /**
   * 
   */
  public void testGetGroupInfos() {
    GroupInfo groupInfo = new GroupInfo();
    groupInfo.setActive(true);
    groupInfo.setGroupDescription("description");
    groupInfo.setNamespaceCode("test");
    groupInfo.setGroupName("testGroup");
    AttributeSet attributeSet = new AttributeSet();
    attributeSet.put("anAttribute", "testingAnAttribute");
    groupInfo.setAttributes(attributeSet);
    
    groupInfo = new GrouperKimGroupUpdateServiceImpl().createGroup(groupInfo);
    
    String groupId = groupInfo.getGroupId();
    
    GroupInfo groupInfo2 = new GroupInfo();
    groupInfo2.setActive(true);
    groupInfo2.setGroupDescription("description2");
    groupInfo2.setNamespaceCode("test");
    groupInfo2.setGroupName("testGroup2");
    AttributeSet attributeSet2 = new AttributeSet();
    attributeSet2.put("anAttribute", "testingAnAttribute2");
    groupInfo2.setAttributes(attributeSet2);
    
    groupInfo2 = new GrouperKimGroupUpdateServiceImpl().createGroup(groupInfo2);
    
    String groupId2 = groupInfo2.getGroupId();
    
    Map<String, GroupInfo> groupInfoMap = new GrouperKimGroupServiceImpl().getGroupInfos(
        GrouperClientUtils.toSet(groupId, groupId2));
    
    groupInfo = groupInfoMap.get(groupId);
    
    assertEquals(true, groupInfo.isActive());
    assertEquals("description", groupInfo.getGroupDescription());
    assertEquals("test", groupInfo.getNamespaceCode());
    assertEquals("testGroup", groupInfo.getGroupName());
    assertEquals("testingAnAttribute", groupInfo.getAttributes().get("anAttribute"));
    assertEquals(groupId, groupInfo.getGroupId());
    
    groupInfo2 = groupInfoMap.get(groupId2);
    assertEquals(true, groupInfo2.isActive());
    assertEquals("description2", groupInfo2.getGroupDescription());
    assertEquals("test", groupInfo2.getNamespaceCode());
    assertEquals("testGroup2", groupInfo2.getGroupName());
    assertEquals("testingAnAttribute2", groupInfo2.getAttributes().get("anAttribute"));
    assertEquals(groupId2, groupInfo2.getGroupId());
  }
  
  /**
   * 
   */
  @SuppressWarnings("unused")
  public void testGetGroupMembers() {
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

    Group group = GroupFinder.findByName(this.grouperSession, GrouperKimUtils.kimStem() + ":test:testGroup", true);

    Group group2 = GroupFinder.findByName(this.grouperSession, GrouperKimUtils.kimStem() + ":test:testGroup2", true);
    
    group.addMember(this.nonKimTestGroup.toSubject());
    
    //add one from a source which should be filtered out
    group.addMember(SubjectFinder.findById("GrouperSystem", true));

    result = new GrouperKimGroupUpdateServiceImpl().addPrincipalToGroup("test.subject.0", groupInfo2.getGroupId());
    assertTrue("added", result);
    result = new GrouperKimGroupUpdateServiceImpl().addPrincipalToGroup("test.subject.1", groupInfo.getGroupId());
    assertTrue("added", result);
    
    //get the memberships
    Set<Object[]> membershipObjects = MembershipFinder.findMemberships(GrouperClientUtils.toSet(group.getUuid()), null, null, null, null, null, null, this.kimStem, Scope.SUB, true);
    Object[][] membershipObjectArray = GrouperClientUtils.toArray(membershipObjects, Object[].class);

    //int index = 0;
    //for (Object[] current : membershipObjectArray) {
    //  Membership membership = (Membership)current[0];
    //  Group currentGroup = (Group)current[1];
    //  Member member = (Member)current[2];
    //  
    //  System.out.println(index + ": " + currentGroup.getName() + ", gid:" + currentGroup.getUuid() + ", " + member.getSubjectSourceId() 
    //      + ": sid: " + member.getSubjectId() + ", " + membership.getListName() + ", " + membership.getType() + ", msid: " + membership.getUuid());
    //  
    //  if (GrouperClientUtils.equals("g:gsa", member.getSubjectSourceId())) {
    //    
    //    Group memberGroup = GroupFinder.findByUuid(this.grouperSession, member.getSubjectId(), true);
    //    System.out.println("    - groupMemberName: " + memberGroup.getName());
    //    
    //  }
    //  
    //  index++;
    //}
    
    //Note: I dont think these are ordered
    //0: test:kim:test:testGroup, gid:37579dee0beb4e1e8ecebe2ea7419a7b, g:isa: sid: GrouperSystem, members, immediate, msid: 6a6e126c9ce244b8a3d734c844136d83:6447b7b49eed4bd9ab6483789f4c04a2
    //1: test:kim:test:testGroup, gid:37579dee0beb4e1e8ecebe2ea7419a7b, g:gsa: sid: e6517b2a6fb5433a8a4f8af093552d54, members, immediate, msid: e957c435e6c94f9686cac884018e335f:6447b7b49eed4bd9ab6483789f4c04a2
    //    - groupMemberName: nonKim:testGroup
    //2: test:kim:test:testGroup, gid:37579dee0beb4e1e8ecebe2ea7419a7b, g:gsa: sid: bcc47e07ec044cd489541249fa1c02d3, members, immediate, msid: f981ab97a93440a3bfde30fbecf2d30a:6447b7b49eed4bd9ab6483789f4c04a2
    //    - groupMemberName: test:kim:test:testGroup2
    //3: test:kim:test:testGroup, gid:37579dee0beb4e1e8ecebe2ea7419a7b, jdbc: sid: test.subject.1, members, immediate, msid: c15bb9607a544908a82ee9621dec2140:6447b7b49eed4bd9ab6483789f4c04a2
    //4: test:kim:test:testGroup, gid:37579dee0beb4e1e8ecebe2ea7419a7b, jdbc: sid: test.subject.0, members, effective, msid: 9c0ffa7a06ae49ef8b5f3879efb90522:8e758bdccfb6422b9190dae4a3911653
    //5: test:kim:test:testGroup, gid:37579dee0beb4e1e8ecebe2ea7419a7b, jdbc: sid: test.subject.1, members, effective, msid: 8d81c76752d6476e8badc8e835b21fc6:8e758bdccfb6422b9190dae4a3911653
    //6: test:kim:test:testGroup, gid:37579dee0beb4e1e8ecebe2ea7419a7b, jdbc: sid: test.subject.0, members, effective, msid: a41015ff22894b2ea622ae7f186bc0fe:d7e3be2c04e746d58e6eb5aa8cbf995b
    //7: test:kim:test:testGroup, gid:37579dee0beb4e1e8ecebe2ea7419a7b, jdbc: sid: test.subject.0, members, effective, msid: b1c570b1414e4307b481bf9519103219:df29918dfa1343d7ab306a2f8ea3fa3e
    //8: test:kim:test:testGroup, gid:37579dee0beb4e1e8ecebe2ea7419a7b, g:gsa: sid: 451baba76fed44339379ef032a68d216, members, effective, msid: d062c5938aae40a4a39bce0cc77d0d10:df29918dfa1343d7ab306a2f8ea3fa3e
    //    - groupMemberName: nonKim:testGroup2
    //9: test:kim:test:testGroup, gid:37579dee0beb4e1e8ecebe2ea7419a7b, jdbc: sid: test.subject.1, members, effective, msid: 9feb0f342fcc43eaa709fdeb6343ae93:df29918dfa1343d7ab306a2f8ea3fa3e

    
    //Object[] membershipObject0 = (Object[])GrouperClientUtils.get(membershipObjects, 0);
    //Member member0 = (Member)membershipObject0[2];
    //Membership membership0 = (Membership)membershipObject0[0];
    
    Collection<GroupMembershipInfo> groupMembershipInfos = new GrouperKimGroupServiceImpl().getGroupMembers(GrouperClientUtils.toList(group.getUuid()));
    
    //index = 0;
    //for (GroupMembershipInfo groupMembershipInfo : groupMembershipInfos) {
    //  System.out.println(index + ": gid: " + groupMembershipInfo.getGroupId() + ", " + groupMembershipInfo.getMemberId() 
    //      + ", msid: " + groupMembershipInfo.getGroupMemberId());
    //  index++;
    //}

    //0: gid: 37579dee0beb4e1e8ecebe2ea7419a7b, bcc47e07ec044cd489541249fa1c02d3, msid: f981ab97a93440a3bfde30fbecf2d30a:6447b7b49eed4bd9ab6483789f4c04a2
    //1: gid: 37579dee0beb4e1e8ecebe2ea7419a7b, test.subject.0, msid: 9c0ffa7a06ae49ef8b5f3879efb90522:8e758bdccfb6422b9190dae4a3911653
    //2: gid: 37579dee0beb4e1e8ecebe2ea7419a7b, test.subject.0, msid: a41015ff22894b2ea622ae7f186bc0fe:d7e3be2c04e746d58e6eb5aa8cbf995b
    //3: gid: 37579dee0beb4e1e8ecebe2ea7419a7b, test.subject.0, msid: b1c570b1414e4307b481bf9519103219:df29918dfa1343d7ab306a2f8ea3fa3e
    //4: gid: 37579dee0beb4e1e8ecebe2ea7419a7b, test.subject.1, msid: 8d81c76752d6476e8badc8e835b21fc6:8e758bdccfb6422b9190dae4a3911653
    //5: gid: 37579dee0beb4e1e8ecebe2ea7419a7b, test.subject.1, msid: 9feb0f342fcc43eaa709fdeb6343ae93:df29918dfa1343d7ab306a2f8ea3fa3e
    //6: gid: 37579dee0beb4e1e8ecebe2ea7419a7b, test.subject.1, msid: c15bb9607a544908a82ee9621dec2140:6447b7b49eed4bd9ab6483789f4c04a2

    
    assertEquals(7, groupMembershipInfos.size());
    
    GroupMembershipInfo groupMembershipInfo0 = (GroupMembershipInfo)GrouperClientUtils.get(groupMembershipInfos, 0);
    GroupMembershipInfo groupMembershipInfo1 = (GroupMembershipInfo)GrouperClientUtils.get(groupMembershipInfos, 1);
    GroupMembershipInfo groupMembershipInfo2 = (GroupMembershipInfo)GrouperClientUtils.get(groupMembershipInfos, 2);
    GroupMembershipInfo groupMembershipInfo3 = (GroupMembershipInfo)GrouperClientUtils.get(groupMembershipInfos, 3);
    GroupMembershipInfo groupMembershipInfo4 = (GroupMembershipInfo)GrouperClientUtils.get(groupMembershipInfos, 4);
    GroupMembershipInfo groupMembershipInfo5 = (GroupMembershipInfo)GrouperClientUtils.get(groupMembershipInfos, 5);
    GroupMembershipInfo groupMembershipInfo6 = (GroupMembershipInfo)GrouperClientUtils.get(groupMembershipInfos, 6);

    assertEquals(group2.getUuid(), groupMembershipInfo0.getMemberId());
    assertEquals("test.subject.0", groupMembershipInfo1.getMemberId());
    assertEquals("test.subject.0", groupMembershipInfo2.getMemberId());
    assertEquals("test.subject.0", groupMembershipInfo3.getMemberId());
    assertEquals("test.subject.1", groupMembershipInfo4.getMemberId());
    assertEquals("test.subject.1", groupMembershipInfo5.getMemberId());
    assertEquals("test.subject.1", groupMembershipInfo6.getMemberId());
    
    assertEquals(group.getUuid(), groupMembershipInfo0.getGroupId());
    assertEquals(group.getUuid(), groupMembershipInfo1.getGroupId());
    assertEquals(group.getUuid(), groupMembershipInfo2.getGroupId());
    assertEquals(group.getUuid(), groupMembershipInfo3.getGroupId());
    assertEquals(group.getUuid(), groupMembershipInfo4.getGroupId());
    assertEquals(group.getUuid(), groupMembershipInfo5.getGroupId());
    assertEquals(group.getUuid(), groupMembershipInfo6.getGroupId());
    
    
  }
  
  /**
   * 
   */
  @SuppressWarnings("unused")
  public void testGetGroupMembersOfGroup() {
    
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

    Group group = GroupFinder.findByName(this.grouperSession, GrouperKimUtils.kimStem() + ":test:testGroup", true);

    Group group2 = GroupFinder.findByName(this.grouperSession, GrouperKimUtils.kimStem() + ":test:testGroup2", true);
    
    group.addMember(this.nonKimTestGroup.toSubject());
    
    //add one from a source which should be filtered out
    group.addMember(SubjectFinder.findById("GrouperSystem", true));

    result = new GrouperKimGroupUpdateServiceImpl().addPrincipalToGroup("test.subject.0", groupInfo2.getGroupId());
    assertTrue("added", result);
    result = new GrouperKimGroupUpdateServiceImpl().addPrincipalToGroup("test.subject.1", groupInfo.getGroupId());
    assertTrue("added", result);
    
    //get the memberships
    Set<Object[]> membershipObjects = MembershipFinder.findMemberships(GrouperClientUtils.toSet(group.getUuid()), null, null, null, null, null, null, this.kimStem, Scope.SUB, true);
    Object[][] membershipObjectArray = GrouperClientUtils.toArray(membershipObjects, Object[].class);

    //int index = 0;
    //for (Object[] current : membershipObjectArray) {
    //  Membership membership = (Membership)current[0];
    //  Group currentGroup = (Group)current[1];
    //  Member member = (Member)current[2];
    //  
    //  System.out.println(index + ": " + currentGroup.getName() + ", gid:" + currentGroup.getUuid() + ", " + member.getSubjectSourceId() 
    //      + ": sid: " + member.getSubjectId() + ", " + membership.getListName() + ", " + membership.getType() + ", msid: " + membership.getUuid());
    //  
    //  if (GrouperClientUtils.equals("g:gsa", member.getSubjectSourceId())) {
    //    
    //    Group memberGroup = GroupFinder.findByUuid(this.grouperSession, member.getSubjectId(), true);
    //    System.out.println("    - groupMemberName: " + memberGroup.getName());
    //    
    //  }
    //  
    //  index++;
    //}
    
    //Note: I dont think these are ordered
    //0: test:kim:test:testGroup, gid:37579dee0beb4e1e8ecebe2ea7419a7b, g:isa: sid: GrouperSystem, members, immediate, msid: 6a6e126c9ce244b8a3d734c844136d83:6447b7b49eed4bd9ab6483789f4c04a2
    //1: test:kim:test:testGroup, gid:37579dee0beb4e1e8ecebe2ea7419a7b, g:gsa: sid: e6517b2a6fb5433a8a4f8af093552d54, members, immediate, msid: e957c435e6c94f9686cac884018e335f:6447b7b49eed4bd9ab6483789f4c04a2
    //    - groupMemberName: nonKim:testGroup
    //2: test:kim:test:testGroup, gid:37579dee0beb4e1e8ecebe2ea7419a7b, g:gsa: sid: bcc47e07ec044cd489541249fa1c02d3, members, immediate, msid: f981ab97a93440a3bfde30fbecf2d30a:6447b7b49eed4bd9ab6483789f4c04a2
    //    - groupMemberName: test:kim:test:testGroup2
    //3: test:kim:test:testGroup, gid:37579dee0beb4e1e8ecebe2ea7419a7b, jdbc: sid: test.subject.1, members, immediate, msid: c15bb9607a544908a82ee9621dec2140:6447b7b49eed4bd9ab6483789f4c04a2
    //4: test:kim:test:testGroup, gid:37579dee0beb4e1e8ecebe2ea7419a7b, jdbc: sid: test.subject.0, members, effective, msid: 9c0ffa7a06ae49ef8b5f3879efb90522:8e758bdccfb6422b9190dae4a3911653
    //5: test:kim:test:testGroup, gid:37579dee0beb4e1e8ecebe2ea7419a7b, jdbc: sid: test.subject.1, members, effective, msid: 8d81c76752d6476e8badc8e835b21fc6:8e758bdccfb6422b9190dae4a3911653
    //6: test:kim:test:testGroup, gid:37579dee0beb4e1e8ecebe2ea7419a7b, jdbc: sid: test.subject.0, members, effective, msid: a41015ff22894b2ea622ae7f186bc0fe:d7e3be2c04e746d58e6eb5aa8cbf995b
    //7: test:kim:test:testGroup, gid:37579dee0beb4e1e8ecebe2ea7419a7b, jdbc: sid: test.subject.0, members, effective, msid: b1c570b1414e4307b481bf9519103219:df29918dfa1343d7ab306a2f8ea3fa3e
    //8: test:kim:test:testGroup, gid:37579dee0beb4e1e8ecebe2ea7419a7b, g:gsa: sid: 451baba76fed44339379ef032a68d216, members, effective, msid: d062c5938aae40a4a39bce0cc77d0d10:df29918dfa1343d7ab306a2f8ea3fa3e
    //    - groupMemberName: nonKim:testGroup2
    //9: test:kim:test:testGroup, gid:37579dee0beb4e1e8ecebe2ea7419a7b, jdbc: sid: test.subject.1, members, effective, msid: 9feb0f342fcc43eaa709fdeb6343ae93:df29918dfa1343d7ab306a2f8ea3fa3e

    
    //Object[] membershipObject0 = (Object[])GrouperClientUtils.get(membershipObjects, 0);
    //Member member0 = (Member)membershipObject0[2];
    //Membership membership0 = (Membership)membershipObject0[0];
    
    Collection<GroupMembershipInfo> groupMembershipInfos = new GrouperKimGroupServiceImpl().getGroupMembersOfGroup(group.getUuid());
    
    //index = 0;
    //for (GroupMembershipInfo groupMembershipInfo : groupMembershipInfos) {
    //  System.out.println(index + ": gid: " + groupMembershipInfo.getGroupId() + ", " + groupMembershipInfo.getMemberId() 
    //      + ", msid: " + groupMembershipInfo.getGroupMemberId());
    //  index++;
    //}

    //0: gid: 37579dee0beb4e1e8ecebe2ea7419a7b, bcc47e07ec044cd489541249fa1c02d3, msid: f981ab97a93440a3bfde30fbecf2d30a:6447b7b49eed4bd9ab6483789f4c04a2
    //1: gid: 37579dee0beb4e1e8ecebe2ea7419a7b, test.subject.0, msid: 9c0ffa7a06ae49ef8b5f3879efb90522:8e758bdccfb6422b9190dae4a3911653
    //2: gid: 37579dee0beb4e1e8ecebe2ea7419a7b, test.subject.0, msid: a41015ff22894b2ea622ae7f186bc0fe:d7e3be2c04e746d58e6eb5aa8cbf995b
    //3: gid: 37579dee0beb4e1e8ecebe2ea7419a7b, test.subject.0, msid: b1c570b1414e4307b481bf9519103219:df29918dfa1343d7ab306a2f8ea3fa3e
    //4: gid: 37579dee0beb4e1e8ecebe2ea7419a7b, test.subject.1, msid: 8d81c76752d6476e8badc8e835b21fc6:8e758bdccfb6422b9190dae4a3911653
    //5: gid: 37579dee0beb4e1e8ecebe2ea7419a7b, test.subject.1, msid: 9feb0f342fcc43eaa709fdeb6343ae93:df29918dfa1343d7ab306a2f8ea3fa3e
    //6: gid: 37579dee0beb4e1e8ecebe2ea7419a7b, test.subject.1, msid: c15bb9607a544908a82ee9621dec2140:6447b7b49eed4bd9ab6483789f4c04a2

    
    assertEquals(7, groupMembershipInfos.size());
    
    GroupMembershipInfo groupMembershipInfo0 = (GroupMembershipInfo)GrouperClientUtils.get(groupMembershipInfos, 0);
    GroupMembershipInfo groupMembershipInfo1 = (GroupMembershipInfo)GrouperClientUtils.get(groupMembershipInfos, 1);
    GroupMembershipInfo groupMembershipInfo2 = (GroupMembershipInfo)GrouperClientUtils.get(groupMembershipInfos, 2);
    GroupMembershipInfo groupMembershipInfo3 = (GroupMembershipInfo)GrouperClientUtils.get(groupMembershipInfos, 3);
    GroupMembershipInfo groupMembershipInfo4 = (GroupMembershipInfo)GrouperClientUtils.get(groupMembershipInfos, 4);
    GroupMembershipInfo groupMembershipInfo5 = (GroupMembershipInfo)GrouperClientUtils.get(groupMembershipInfos, 5);
    GroupMembershipInfo groupMembershipInfo6 = (GroupMembershipInfo)GrouperClientUtils.get(groupMembershipInfos, 6);

    assertEquals(group2.getUuid(), groupMembershipInfo0.getMemberId());
    assertEquals("test.subject.0", groupMembershipInfo1.getMemberId());
    assertEquals("test.subject.0", groupMembershipInfo2.getMemberId());
    assertEquals("test.subject.0", groupMembershipInfo3.getMemberId());
    assertEquals("test.subject.1", groupMembershipInfo4.getMemberId());
    assertEquals("test.subject.1", groupMembershipInfo5.getMemberId());
    assertEquals("test.subject.1", groupMembershipInfo6.getMemberId());
    
    assertEquals(group.getUuid(), groupMembershipInfo0.getGroupId());
    assertEquals(group.getUuid(), groupMembershipInfo1.getGroupId());
    assertEquals(group.getUuid(), groupMembershipInfo2.getGroupId());
    assertEquals(group.getUuid(), groupMembershipInfo3.getGroupId());
    assertEquals(group.getUuid(), groupMembershipInfo4.getGroupId());
    assertEquals(group.getUuid(), groupMembershipInfo5.getGroupId());
    assertEquals(group.getUuid(), groupMembershipInfo6.getGroupId());
    
    
    
  }
  
  /**
   * 
   */
  public void testGetGroupsForPrincipal() {
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

    Group group = GroupFinder.findByName(this.grouperSession, GrouperKimUtils.kimStem() + ":test:testGroup", true);
    Group group2 = GroupFinder.findByName(this.grouperSession, GrouperKimUtils.kimStem() + ":test:testGroup2", true);
    
    result = new GrouperKimGroupUpdateServiceImpl().addPrincipalToGroup("test.subject.0", groupInfo2.getGroupId());
    
    assertTrue("added", result);
    
    assertTrue(group2.hasMember(SubjectFinder.findById("test.subject.0", true)));
    assertTrue(group.hasMember(SubjectFinder.findById("test.subject.0", true)));
    
    List<GroupInfo> groupInfos = new GrouperKimGroupServiceImpl().getGroupsForPrincipal("test.subject.0");
    
    assertEquals(2, groupInfos.size());
    assertTrue(GrouperClientUtils.equals(group.getUuid(), groupInfos.get(0).getGroupId()) || 
        GrouperClientUtils.equals(group.getUuid(), groupInfos.get(1).getGroupId()));
    
    assertTrue(GrouperClientUtils.equals(group2.getUuid(), groupInfos.get(0).getGroupId()) || 
        GrouperClientUtils.equals(group2.getUuid(), groupInfos.get(1).getGroupId()));

  }
  
  /**
   * 
   */
  public void testGetGroupsForPrincipalByNamespace() {
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

    Group group = GroupFinder.findByName(this.grouperSession, GrouperKimUtils.kimStem() + ":test:testGroup", true);
    Group group2 = GroupFinder.findByName(this.grouperSession, GrouperKimUtils.kimStem() + ":test:testGroup2", true);
    
    result = new GrouperKimGroupUpdateServiceImpl().addPrincipalToGroup("test.subject.0", groupInfo2.getGroupId());
    
    assertTrue("added", result);
    
    assertTrue(group2.hasMember(SubjectFinder.findById("test.subject.0", true)));
    assertTrue(group.hasMember(SubjectFinder.findById("test.subject.0", true)));
    
    List<GroupInfo> groupInfos = new GrouperKimGroupServiceImpl().getGroupsForPrincipalByNamespace("test.subject.0", "test");
    
    assertEquals(2, groupInfos.size());
    assertTrue(GrouperClientUtils.equals(group.getUuid(), groupInfos.get(0).getGroupId()) || 
        GrouperClientUtils.equals(group.getUuid(), groupInfos.get(1).getGroupId()));
    
    assertTrue(GrouperClientUtils.equals(group2.getUuid(), groupInfos.get(0).getGroupId()) || 
        GrouperClientUtils.equals(group2.getUuid(), groupInfos.get(1).getGroupId()));
    
    groupInfos = new GrouperKimGroupServiceImpl().getGroupsForPrincipalByNamespace("test.subject.0", "whatever");
    
    assertEquals(0, groupInfos.size());
  }
  
  /**
   * 
   */
  public void testGetMemberGroupIds() {
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
    
    assertTrue(result);
    
    GroupInfo groupInfo3 = new GroupInfo();
    groupInfo3.setActive(true);
    groupInfo3.setGroupDescription("description3");
    groupInfo3.setNamespaceCode("test");
    groupInfo3.setGroupName("testGroup3");
    
    groupInfo3 = new GrouperKimGroupUpdateServiceImpl().createGroup(groupInfo3);
    
    Group group = GroupFinder.findByName(this.grouperSession, GrouperKimUtils.kimStem() + ":test:testGroup", true);
    Group group2 = GroupFinder.findByName(this.grouperSession, GrouperKimUtils.kimStem() + ":test:testGroup2", true);
    Group group3 = GroupFinder.findByName(this.grouperSession, GrouperKimUtils.kimStem() + ":test:testGroup3", true);

    group.addMember(this.nonKimTestGroup.toSubject());
    
    result = new GrouperKimGroupUpdateServiceImpl().addGroupToGroup(groupInfo3.getGroupId(), groupInfo2.getGroupId());
    
    assertTrue(result);
    
    List<String> groupIds = new GrouperKimGroupServiceImpl().getMemberGroupIds(groupInfo.getGroupId());
    
    assertEquals(2, groupIds.size());

    assertTrue(groupIds.contains(group2.getUuid()));
    assertTrue(groupIds.contains(group3.getUuid()));
    
  }
  
  /**
   * 
   */
  @SuppressWarnings("unused")
  public void testGetMemberPrincipalIds() {
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

    Group group = GroupFinder.findByName(this.grouperSession, GrouperKimUtils.kimStem() + ":test:testGroup", true);

    Group group2 = GroupFinder.findByName(this.grouperSession, GrouperKimUtils.kimStem() + ":test:testGroup2", true);
    
    group.addMember(this.nonKimTestGroup.toSubject());

    result = new GrouperKimGroupUpdateServiceImpl().addPrincipalToGroup("test.subject.0", groupInfo2.getGroupId());
    assertTrue("added", result);
    result = new GrouperKimGroupUpdateServiceImpl().addPrincipalToGroup("test.subject.1", groupInfo.getGroupId());
    assertTrue("added", result);

    List<String> principalIds = new GrouperKimGroupServiceImpl().getMemberPrincipalIds(group.getUuid());
    
    assertEquals(2, principalIds.size());
    assertTrue(principalIds.contains("test.subject.0"));
    assertTrue(principalIds.contains("test.subject.1"));

  }
  
  /**
   * 
   */
  public void testGetParentGroupIds() {
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
    
    assertTrue(result);
    
    GroupInfo groupInfo3 = new GroupInfo();
    groupInfo3.setActive(true);
    groupInfo3.setGroupDescription("description3");
    groupInfo3.setNamespaceCode("test");
    groupInfo3.setGroupName("testGroup3");
    
    groupInfo3 = new GrouperKimGroupUpdateServiceImpl().createGroup(groupInfo3);
    
    Group group = GroupFinder.findByName(this.grouperSession, GrouperKimUtils.kimStem() + ":test:testGroup", true);
    Group group2 = GroupFinder.findByName(this.grouperSession, GrouperKimUtils.kimStem() + ":test:testGroup2", true);
    Group group3 = GroupFinder.findByName(this.grouperSession, GrouperKimUtils.kimStem() + ":test:testGroup3", true);

    group.addMember(this.nonKimTestGroup.toSubject());
    this.nonKimTestGroup.addMember(group3.toSubject());
    
    
    result = new GrouperKimGroupUpdateServiceImpl().addGroupToGroup(groupInfo3.getGroupId(), groupInfo2.getGroupId());
    
    assertTrue(result);
    
    List<String> groupIds = new GrouperKimGroupServiceImpl().getParentGroupIds(groupInfo3.getGroupId());
    
    assertEquals(2, groupIds.size());

    assertTrue(groupIds.contains(group.getUuid()));
    assertTrue(groupIds.contains(group2.getUuid()));

  }
  
  /**
   * 
   */
  public void testIsDirectMemberOfGroup() {
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

    Group group = GroupFinder.findByName(this.grouperSession, GrouperKimUtils.kimStem() + ":test:testGroup", true);
    Group group2 = GroupFinder.findByName(this.grouperSession, GrouperKimUtils.kimStem() + ":test:testGroup2", true);
    
    result = new GrouperKimGroupUpdateServiceImpl().addPrincipalToGroup("test.subject.0", groupInfo2.getGroupId());
    result = new GrouperKimGroupUpdateServiceImpl().addPrincipalToGroup("test.subject.1", groupInfo.getGroupId());
    
    assertTrue("added", result);
    
    assertTrue(group2.hasMember(SubjectFinder.findById("test.subject.0", true)));
    assertTrue(group.hasMember(SubjectFinder.findById("test.subject.0", true)));
    
    group.addMember(this.nonKimTestGroup.toSubject());
    
    assertFalse(new GrouperKimGroupServiceImpl().isDirectMemberOfGroup("test.subject.0", group.getUuid()));
    assertTrue(new GrouperKimGroupServiceImpl().isDirectMemberOfGroup("test.subject.1", group.getUuid()));
    
  }
  
  /**
   * 
   */
  public void testIsGroupActive() {
    GroupInfo groupInfo = new GroupInfo();
    groupInfo.setActive(true);
    groupInfo.setGroupDescription("description");
    groupInfo.setNamespaceCode("test");
    groupInfo.setGroupName("testGroup");
    
    groupInfo = new GrouperKimGroupUpdateServiceImpl().createGroup(groupInfo);

    assertTrue(new GrouperKimGroupServiceImpl().isGroupActive(groupInfo.getGroupId()));
    assertFalse(new GrouperKimGroupServiceImpl().isGroupActive("abc"));
    
  }
  
  /**
   * 
   */
  @SuppressWarnings("unused")
  public void testIsGroupMemberOfGroup() {
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
    
    assertTrue(result);
    
    GroupInfo groupInfo3 = new GroupInfo();
    groupInfo3.setActive(true);
    groupInfo3.setGroupDescription("description3");
    groupInfo3.setNamespaceCode("test");
    groupInfo3.setGroupName("testGroup3");
    
    groupInfo3 = new GrouperKimGroupUpdateServiceImpl().createGroup(groupInfo3);
    
    Group group = GroupFinder.findByName(this.grouperSession, GrouperKimUtils.kimStem() + ":test:testGroup", true);
    Group group2 = GroupFinder.findByName(this.grouperSession, GrouperKimUtils.kimStem() + ":test:testGroup2", true);
    Group group3 = GroupFinder.findByName(this.grouperSession, GrouperKimUtils.kimStem() + ":test:testGroup3", true);

    group.addMember(this.nonKimTestGroup.toSubject());
    this.nonKimTestGroup.addMember(group3.toSubject());
    
    
    result = new GrouperKimGroupUpdateServiceImpl().addGroupToGroup(groupInfo3.getGroupId(), groupInfo2.getGroupId());
    
    assertTrue(result);
    
    assertTrue(new GrouperKimGroupServiceImpl().isGroupMemberOfGroup(groupInfo2.getGroupId(), groupInfo.getGroupId()));
    assertTrue(new GrouperKimGroupServiceImpl().isGroupMemberOfGroup(groupInfo3.getGroupId(), groupInfo2.getGroupId()));
    assertTrue(new GrouperKimGroupServiceImpl().isGroupMemberOfGroup(groupInfo3.getGroupId(), groupInfo.getGroupId()));
    assertFalse(new GrouperKimGroupServiceImpl().isGroupMemberOfGroup(groupInfo.getGroupId(), groupInfo3.getGroupId()));
  }
  
  /**
   * 
   */
  public void testIsMemberOfGroup() {
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

    Group group = GroupFinder.findByName(this.grouperSession, GrouperKimUtils.kimStem() + ":test:testGroup", true);
    Group group2 = GroupFinder.findByName(this.grouperSession, GrouperKimUtils.kimStem() + ":test:testGroup2", true);
    
    result = new GrouperKimGroupUpdateServiceImpl().addPrincipalToGroup("test.subject.0", groupInfo2.getGroupId());
    result = new GrouperKimGroupUpdateServiceImpl().addPrincipalToGroup("test.subject.1", groupInfo.getGroupId());
    
    assertTrue("added", result);
    
    assertTrue(group2.hasMember(SubjectFinder.findById("test.subject.0", true)));
    assertTrue(group.hasMember(SubjectFinder.findById("test.subject.0", true)));
    
    group.addMember(this.nonKimTestGroup.toSubject());
    
    assertTrue(new GrouperKimGroupServiceImpl().isMemberOfGroup("test.subject.0", group.getUuid()));
    assertTrue(new GrouperKimGroupServiceImpl().isMemberOfGroup("test.subject.1", group.getUuid()));
    assertFalse(new GrouperKimGroupServiceImpl().isMemberOfGroup("test.subject.2", group.getUuid()));
    

  }
  
  /**
   * 
   */
  public void testLookupGroupIds() {
    try {
      new GrouperKimGroupServiceImpl().lookupGroupIds(null);
      fail("Not implemented");
    } catch (Exception e) {
      //good
    }
  }
  
  /**
   * 
   */
  public void testLookupGroups() {
    try {
      new GrouperKimGroupServiceImpl().lookupGroups(null);
      fail("Not implemented");
    } catch (Exception e) {
      //good
    }
    
  }
}
