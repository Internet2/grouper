/**
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
 */
/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.client;

import java.util.HashSet;
import java.util.Set;

import junit.textui.TestRunner;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.app.loader.GrouperLoader;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderType;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.externalSubjects.ExternalSubject;
import edu.internet2.middleware.grouper.externalSubjects.ExternalSubjectConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.api.GcAddMember;
import edu.internet2.middleware.grouperClient.api.GcDeleteMember;
import edu.internet2.middleware.grouperClient.api.GcGetMembers;
import edu.internet2.middleware.grouperClient.api.GcGroupSave;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetMembersResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetMembersResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroup;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroupLookup;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroupToSave;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubject;
import edu.internet2.middleware.subject.Subject;


/**
 * <pre>
 * note: this will add junk external users to the database...  testuser1@internet2.edu
 * </pre>
 */
public class GroupSyncDaemonTest extends GrouperTest {

  /** location in remote that can be used for testing */
  private String remoteTestFolder = null;
  
  /**
   * @see edu.internet2.middleware.grouper.helper.GrouperTest#setUp()
   */
  @Override
  protected void setUp() {
    super.setUp();

    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperClient.remoteGrouperTest.id", "remoteGrouperTest");
    
    {
      String url = GrouperConfig.getProperty("junit.test.groupSync.url");
      assertTrue(StringUtils.isNotBlank(url));
      
      GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperClient.remoteGrouperTest.properties.grouperClient.webService.url", url);
    }
    
    {
      String user = GrouperConfig.getProperty("junit.test.groupSync.user");
      assertTrue(StringUtils.isNotBlank(user));
      
      GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperClient.remoteGrouperTest.properties.grouperClient.webService.login", user);
    }
    
    {
      String pass = GrouperConfig.getProperty("junit.test.groupSync.password");
      assertTrue(StringUtils.isNotBlank(pass));
      
      GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperClient.remoteGrouperTest.properties.grouperClient.webService.password", pass);
    }

    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperClient.remoteGrouperTest.source.externalSubjects.id", "externalSubjects");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperClient.remoteGrouperTest.source.externalSubjects.local.sourceId", "grouperExternal");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperClient.remoteGrouperTest.source.externalSubjects.local.read.subjectId", "identifier");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperClient.remoteGrouperTest.source.externalSubjects.local.write.subjectId", "identifier");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperClient.remoteGrouperTest.source.externalSubjects.remote.sourceId", 
        GrouperConfig.getProperty("junit.test.groupSync.remoteSourceId"));
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperClient.remoteGrouperTest.source.externalSubjects.remote.read.subjectId", 
        GrouperConfig.getProperty("junit.test.groupSync.remoteReadSubjectId"));
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperClient.remoteGrouperTest.source.externalSubjects.remote.write.subjectId", 
        GrouperConfig.getProperty("junit.test.groupSync.remoteWriteSubjectId"));

    //########################## PUSH
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("syncAnotherGrouper.unitTest.connectionName", "remoteGrouperTest");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("syncAnotherGrouper.unitTest.syncType", "push");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("syncAnotherGrouper.unitTest.cron", "0 0 5 * * ?");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("syncAnotherGrouper.unitTest.local.groupName", "localGroupSyncTest:localTestPush");
    
    this.remoteTestFolder = GrouperConfig.getProperty("junit.test.groupSync.folder");
    assertTrue(StringUtils.isNotBlank(this.remoteTestFolder));
    
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("syncAnotherGrouper.unitTest.remote.groupName", this.remoteTestFolder + ":" + "remoteTestPush");
    
    String pushAddExternalSubjectIfNotExist = StringUtils.defaultString(GrouperConfig.getProperty("junit.test.groupSync.pushAddExternalSubjectIfNotExist"), "true");

    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("syncAnotherGrouper.unitTest.addExternalSubjectIfNotFound", pushAddExternalSubjectIfNotExist);
    
    //##########################  PULL
    
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("syncAnotherGrouper.unitTestPull.connectionName", "remoteGrouperTest");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("syncAnotherGrouper.unitTestPull.syncType", "pull");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("syncAnotherGrouper.unitTestPull.cron", "0 0 5 * * ?");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("syncAnotherGrouper.unitTestPull.local.groupName", "localGroupSyncTest:localTestPull");
    
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("syncAnotherGrouper.unitTestPull.remote.groupName", this.remoteTestFolder + ":" + "remoteTestPull");
    
    //note, always true to create subjects
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("syncAnotherGrouper.unitTestPull.addExternalSubjectIfNotFound", "true");    

    //########################## PUSH INCREMENTAL
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("syncAnotherGrouper.unitTestPushIncremental.connectionName", "remoteGrouperTest");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("syncAnotherGrouper.unitTestPushIncremental.syncType", "incremental_push");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("syncAnotherGrouper.unitTestPushIncremental.cron", "0 0 5 * * ?");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("syncAnotherGrouper.unitTestPushIncremental.local.groupName", "localGroupSyncTest:localTestPushIncremental");
    
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("syncAnotherGrouper.unitTestPushIncremental.remote.groupName", this.remoteTestFolder + ":" + "remoteTestPushIncremental");
    
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("syncAnotherGrouper.unitTestPushIncremental.addExternalSubjectIfNotFound", pushAddExternalSubjectIfNotExist);
    
  }

  /**
   * setup a client connection
   */
  private static void setUpClient() {
    //get the connection, and set it up
    
    ClientCustomizerContext clientCustomizerContext = new ClientCustomizerContext();
    clientCustomizerContext.setConnectionName("remoteGrouperTest");
    clientCustomizer = new ClientCustomizer();
    clientCustomizer.init(clientCustomizerContext);
    clientCustomizer.setupConnection();

  }
  
  /**
   * @see edu.internet2.middleware.grouper.helper.GrouperTest#tearDown()
   */
  @Override
  protected void tearDown() {
    super.tearDown();
  }
  
  /**
   * teardown client
   */
  private static void tearDownClient() {
    if (clientCustomizer != null) {
      clientCustomizer.teardownConnection();
    }
  }
  
  /** client customizer */
  private static ClientCustomizer clientCustomizer = null;
  
  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new GroupSyncDaemonTest("testSyncGroupPushIncremental"));
  }

  /**
   * 
   */
  public GroupSyncDaemonTest() {
    super();
    
  }

  /**
   * @param name
   */
  public GroupSyncDaemonTest(String name) {
    super(name);
    
  }

  /**
   * note, this isnt a real test since the demo server needs to be setup correctly...
   * test a sync group push
   */
  public void testSyncGroupPush() {

    //create a local group
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    GrouperLoader.runOnceByJobName(grouperSession, GrouperLoaderType.GROUPER_CHANGE_LOG_TEMP_TO_CHANGE_LOG);
    GrouperLoader.runOnceByJobName(grouperSession, GrouperLoaderType.GROUPER_CHANGE_LOG_CONSUMER_PREFIX + "syncGroups");

    boolean createFolderIfNotExist = 
        GrouperConfig.getPropertyBoolean("junit.test.groupSync.createRemoteFolderIfNotExist", true);

    GroupSave groupSave = new GroupSave(grouperSession).assignName("localGroupSyncTest:localTestPush");
    
    if (createFolderIfNotExist) {
      groupSave.assignCreateParentStemsIfNotExist(true);
    }
    
    Group localTestPush = groupSave.save();
    
    createRemoteGroup(this.remoteTestFolder, "remoteTestPush");
    
    //sync the group
    assertEquals(0, GroupSyncDaemon.syncGroup(localTestPush.getName()));
    
    //lets see the remote group has no members
    Set<String> remoteIdentifiers = remoteMembers(this.remoteTestFolder + ":" + "remoteTestPush");
    
    assertEquals(0, remoteIdentifiers.size());
    
    //############ ADD A MEMBER
    //add an external member
    Subject testUser1 = createExternalPerson("testuser1");
    Subject testUser2 = createExternalPerson("testuser2");
    
    localTestPush.addMember(testUser1);
    localTestPush.addMember(testUser2);
    
    addRemoteMember(this.remoteTestFolder + ":" + "remoteTestPush", "testuser7@internet2.edu");
    
    GrouperLoader.runOnceByJobName(grouperSession, GrouperLoaderType.GROUPER_CHANGE_LOG_TEMP_TO_CHANGE_LOG);
    GrouperLoader.runOnceByJobName(grouperSession, GrouperLoaderType.GROUPER_CHANGE_LOG_CONSUMER_PREFIX + "syncGroups");

    //make sure nothing happened since it is not incremental
    remoteIdentifiers = remoteMembers(this.remoteTestFolder + ":" + "remoteTestPush");
    
    assertEquals(1, remoteIdentifiers.size());
    
    assertTrue(remoteIdentifiers.contains("testuser7@internet2.edu"));
    
    
    //sync the group
    assertEquals(2, GroupSyncDaemon.syncGroup(localTestPush.getName()));
    
    //lets see the remote group has no members
    remoteIdentifiers = remoteMembers(this.remoteTestFolder + ":" + "remoteTestPush");
    
    assertEquals(2, remoteIdentifiers.size());
    
    assertTrue(remoteIdentifiers.contains("testuser1@internet2.edu"));
    assertTrue(remoteIdentifiers.contains("testuser2@internet2.edu"));

    
    //############ CHANGE THE MEMBERS
    localTestPush.deleteMember(testUser1);
    
    Subject testUser3 = createExternalPerson("testuser3");
    Subject testUser4 = createExternalPerson("testuser4");
    localTestPush.addMember(testUser3);
    localTestPush.addMember(testUser4);
    
    //sync the group
    assertEquals(3, GroupSyncDaemon.syncGroup(localTestPush.getName()));
    
    //lets see the remote group has no members
    remoteIdentifiers = remoteMembers(this.remoteTestFolder + ":" + "remoteTestPush");
    
    assertEquals(3, remoteIdentifiers.size());
    
    assertTrue(remoteIdentifiers.contains("testuser2@internet2.edu"));
    assertTrue(remoteIdentifiers.contains("testuser3@internet2.edu"));
    assertTrue(remoteIdentifiers.contains("testuser4@internet2.edu"));
    
  }
  
  /**
   * note, this isnt a real test since the demo server needs to be setup correctly...
   * test a sync group push
   */
  public void testSyncGroupPull() {

    //create a local group
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    
    Group localTestPull = new GroupSave(grouperSession).assignName("localGroupSyncTest:localTestPull")
      .assignCreateParentStemsIfNotExist(true).save();
    
    createRemoteGroup(this.remoteTestFolder, "remoteTestPull");
    
    //sync the group
    assertEquals(0, GroupSyncDaemon.syncGroup(localTestPull.getName()));
    
    //lets see the remote group has no members
    Set<String> localIdentifiers = localMembers(localTestPull.getName());
    
    assertEquals(0, localIdentifiers.size());
    
    //############ ADD A MEMBER
    //add an external member
    Subject testUser7 = createExternalPerson("testuser7");
    
    addRemoteMember(this.remoteTestFolder + ":remoteTestPull", "testuser1@internet2.edu");
    addRemoteMember(this.remoteTestFolder + ":remoteTestPull", "testuser2@internet2.edu");
    
    localTestPull.addMember(testUser7);
    
    //sync the group
    assertEquals(3, GroupSyncDaemon.syncGroup(localTestPull.getName()));
    
    //lets see the remote group has no members
    localIdentifiers = localMembers(localTestPull.getName());
    
    assertEquals(2, localIdentifiers.size());
    
    assertTrue(localIdentifiers.contains("testuser1@internet2.edu"));
    assertTrue(localIdentifiers.contains("testuser2@internet2.edu"));

    
    //############ CHANGE THE MEMBERS
    removeRemoteMember(this.remoteTestFolder + ":remoteTestPull", "testuser1@internet2.edu");
    addRemoteMember(this.remoteTestFolder + ":remoteTestPull", "testuser3@internet2.edu");
    addRemoteMember(this.remoteTestFolder + ":remoteTestPull", "testuser4@internet2.edu");
    
    //sync the group
    assertEquals(3, GroupSyncDaemon.syncGroup(localTestPull.getName()));
    
    //lets see the remote group has no members
    localIdentifiers = localMembers(localTestPull.getName());
    
    assertEquals(3, localIdentifiers.size());
    
    assertTrue(localIdentifiers.contains("testuser2@internet2.edu"));
    assertTrue(localIdentifiers.contains("testuser3@internet2.edu"));
    assertTrue(localIdentifiers.contains("testuser4@internet2.edu"));
    
  }
  
  /**
   * note, this isnt a real test since the demo server needs to be setup correctly...
   * test a sync group push
   */
  public void testSyncGroupPushIncremental() {
  
    //create a local group
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    GrouperLoader.runOnceByJobName(grouperSession, GrouperLoaderType.GROUPER_CHANGE_LOG_TEMP_TO_CHANGE_LOG);
    GrouperLoader.runOnceByJobName(grouperSession, GrouperLoaderType.GROUPER_CHANGE_LOG_CONSUMER_PREFIX + "syncGroups");

    boolean createFolderIfNotExist = 
        GrouperConfig.getPropertyBoolean("junit.test.groupSync.createRemoteFolderIfNotExist", true);
  
    GroupSave groupSave = new GroupSave(grouperSession).assignName("localGroupSyncTest:localTestPushIncremental");
    
    if (createFolderIfNotExist) {
      groupSave.assignCreateParentStemsIfNotExist(true);
    }
    
    Group localTestPush = groupSave.save();
    
    createRemoteGroup(this.remoteTestFolder, "remoteTestPushIncremental");
    
    //sync the group
    assertEquals(0, GroupSyncDaemon.syncGroup(localTestPush.getName()));
    
    //lets see the remote group has no members
    Set<String> remoteIdentifiers = remoteMembers(this.remoteTestFolder + ":" + "remoteTestPushIncremental");
    
    assertEquals(0, remoteIdentifiers.size());
    
    //############ ADD A MEMBER
    //add an external member
    Subject testUser1 = createExternalPerson("testuser1");
    Subject testUser2 = createExternalPerson("testuser2");
    
    localTestPush.addMember(testUser1);
    localTestPush.addMember(testUser2);
    
    addRemoteMember(this.remoteTestFolder + ":" + "remoteTestPushIncremental", "testuser7@internet2.edu");
    
    GrouperLoader.runOnceByJobName(grouperSession, GrouperLoaderType.GROUPER_CHANGE_LOG_TEMP_TO_CHANGE_LOG);
    GrouperLoader.runOnceByJobName(grouperSession, GrouperLoaderType.GROUPER_CHANGE_LOG_CONSUMER_PREFIX + "syncGroups");

    //make sure nothing happened since it is not incremental
    remoteIdentifiers = remoteMembers(this.remoteTestFolder + ":" + "remoteTestPushIncremental");
    
    assertEquals(3, remoteIdentifiers.size());

    assertTrue(remoteIdentifiers.contains("testuser1@internet2.edu"));
    assertTrue(remoteIdentifiers.contains("testuser2@internet2.edu"));
    assertTrue(remoteIdentifiers.contains("testuser7@internet2.edu"));

    
    //sync the group
    assertEquals(2, GroupSyncDaemon.syncGroup(localTestPush.getName()));
    
    //lets see the remote group has no members
    remoteIdentifiers = remoteMembers(this.remoteTestFolder + ":" + "remoteTestPushIncremental");
    
    assertEquals(2, remoteIdentifiers.size());
    
    assertTrue(remoteIdentifiers.contains("testuser1@internet2.edu"));
    assertTrue(remoteIdentifiers.contains("testuser2@internet2.edu"));
  
    
    //############ CHANGE THE MEMBERS
    localTestPush.deleteMember(testUser1);
    
    Subject testUser3 = createExternalPerson("testuser3");
    Subject testUser4 = createExternalPerson("testuser4");
    localTestPush.addMember(testUser3);
    localTestPush.addMember(testUser4);

    GrouperLoader.runOnceByJobName(grouperSession, GrouperLoaderType.GROUPER_CHANGE_LOG_TEMP_TO_CHANGE_LOG);
    GrouperLoader.runOnceByJobName(grouperSession, GrouperLoaderType.GROUPER_CHANGE_LOG_CONSUMER_PREFIX + "syncGroups");

    //make sure nothing happened since it is not incremental
    remoteIdentifiers = remoteMembers(this.remoteTestFolder + ":" + "remoteTestPushIncremental");
    
    assertEquals(3, remoteIdentifiers.size());
    
    assertTrue(remoteIdentifiers.contains("testuser2@internet2.edu"));
    assertTrue(remoteIdentifiers.contains("testuser3@internet2.edu"));
    assertTrue(remoteIdentifiers.contains("testuser4@internet2.edu"));

    
    //sync the group
    assertEquals(3, GroupSyncDaemon.syncGroup(localTestPush.getName()));
    
    //lets see the remote group has no members
    remoteIdentifiers = remoteMembers(this.remoteTestFolder + ":" + "remoteTestPushIncremental");
    
    assertEquals(3, remoteIdentifiers.size());
    
    assertTrue(remoteIdentifiers.contains("testuser2@internet2.edu"));
    assertTrue(remoteIdentifiers.contains("testuser3@internet2.edu"));
    assertTrue(remoteIdentifiers.contains("testuser4@internet2.edu"));
    
  }

  /**
   * create an external person
   * @param name
   * @return the person created as subject
   */
  public static Subject createExternalPerson(String name) {
    ExternalSubjectConfig.clearCache();
    ExternalSubject externalSubjectTestUser1 = new ExternalSubject();
    externalSubjectTestUser1.setName(name);
    externalSubjectTestUser1.setIdentifier(name + "@internet2.edu");
    externalSubjectTestUser1.setEmail(name + "@internet2.edu");
    externalSubjectTestUser1.setInstitution("Internet2");
    externalSubjectTestUser1.store();
    return SubjectFinder.findByIdentifier(name + "@internet2.edu", true);
  }
  
  /**
   * @param stemName 
   * @param extension 
   * 
   */
  public static void createRemoteGroup(String stemName, String extension) {
    
    //lets make a remote group
    WsGroup wsGroup = new WsGroup();
    wsGroup.setExtension(extension);
    wsGroup.setDisplayExtension(extension);
    wsGroup.setName(stemName + ":" + extension);
    wsGroup.setDisplayName(stemName + ":" + extension);
    
    WsGroupToSave wsGroupToSave = new WsGroupToSave();
    wsGroupToSave.setWsGroup(wsGroup);
    wsGroupToSave.setWsGroupLookup(new WsGroupLookup(stemName + ":" + extension, null));
    wsGroupToSave.setCreateParentStemsIfNotExist("T");
    setUpClient();
    new GcGroupSave().addGroupToSave(wsGroupToSave).execute();
    tearDownClient();
    
    //lets init all the members
    Set<String> identifiers = remoteMembers(stemName + ":" + extension);
    for (String identifier : identifiers) {
      removeRemoteMember(stemName + ":" + extension, identifier);
    }
    
    
  }
  
  /**
   * @param groupName 
   * @return the group name
   */
  public static Set<String> localMembers(String groupName) {
    
    Set<String> identifiers = new HashSet<String>();
    Group group = GroupFinder.findByName(GrouperSession.staticGrouperSession(), groupName, true);
    for (Member member : GrouperUtil.nonNull(group.getMembers())) {
      Subject subject = member.getSubject();
      String identifier = subject.getAttributeValue("identifier");
      if (!StringUtils.isBlank(identifier)) {
        identifiers.add(identifier);
      }
    }
    return identifiers;
  }

  /**
   * @param groupName 
   * @return the group name
   */
  public static Set<String> remoteMembers(String groupName) {
    setUpClient();

    String remoteIdentifierName = GrouperConfig.getProperty(
        "grouperClient.remoteGrouperTest.source.externalSubjects.remote.read.subjectId");
    WsGetMembersResults wsGetMembersResults = new GcGetMembers().addGroupName(groupName)
      .addSubjectAttributeName(remoteIdentifierName).execute();
    
    tearDownClient();
    
    WsGetMembersResult wsGetMembersResult = wsGetMembersResults.getResults()[0];

    Set<String> identifiers = new HashSet<String>();
    if (wsGetMembersResult != null && wsGetMembersResult.getWsSubjects() != null) {
      for (WsSubject wsSubject : wsGetMembersResult.getWsSubjects()) {
  
        String identifier = GrouperClientUtils.subjectAttributeValue(
            wsSubject, wsGetMembersResults.getSubjectAttributeNames(), remoteIdentifierName);
        if (!StringUtils.isBlank(identifier)) {
          identifiers.add(identifier);
        }
  
      }
    }
    return identifiers;
  }
  
  /**
   * @param groupName 
   * @param identifier
   */
  public static void addRemoteMember(String groupName, String identifier) {
    
    setUpClient();

    GcAddMember gcAddMember = new GcAddMember().assignGroupName(groupName).addSubjectIdentifier(identifier);

    if (GrouperConfig.getPropertyBoolean("junit.test.groupSync.pushAddExternalSubjectIfNotExist", false)) {
      gcAddMember.assignAddExternalSubjectIfNotFound(true);
    }
  
    gcAddMember.execute();
    
    tearDownClient();
    
  }

  /**
   * @param groupName 
   * @param identifier
   */
  public static void removeRemoteMember(String groupName, String identifier) {
    
    setUpClient();

    new GcDeleteMember().assignGroupName(groupName).addSubjectIdentifier(identifier).execute();
    
    tearDownClient();
    
  }

}
