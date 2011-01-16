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
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.cfg.ApiConfig;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.externalSubjects.ExternalSubject;
import edu.internet2.middleware.grouper.externalSubjects.ExternalSubjectConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
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

    ApiConfig.testConfig.put("grouperClient.remoteGrouperTest.id", "remoteGrouperTest");
    
    {
      String url = GrouperConfig.getProperty("junit.test.groupSync.url");
      assertTrue(StringUtils.isNotBlank(url));
      
      ApiConfig.testConfig.put("grouperClient.remoteGrouperTest.properties.grouperClient.webService.url", url);
    }
    
    {
      String user = GrouperConfig.getProperty("junit.test.groupSync.user");
      assertTrue(StringUtils.isNotBlank(user));
      
      ApiConfig.testConfig.put("grouperClient.remoteGrouperTest.properties.grouperClient.webService.login", user);
    }
    
    {
      String pass = GrouperConfig.getProperty("junit.test.groupSync.password");
      assertTrue(StringUtils.isNotBlank(pass));
      
      ApiConfig.testConfig.put("grouperClient.remoteGrouperTest.properties.grouperClient.webService.password", pass);
    }

    ApiConfig.testConfig.put("grouperClient.remoteGrouperTest.source.externalSubjects.id", "externalSubjects");
    ApiConfig.testConfig.put("grouperClient.remoteGrouperTest.source.externalSubjects.local.sourceId", "grouperExternal");
    ApiConfig.testConfig.put("grouperClient.remoteGrouperTest.source.externalSubjects.local.read.subjectId", "identifier");
    ApiConfig.testConfig.put("grouperClient.remoteGrouperTest.source.externalSubjects.local.write.subjectId", "identifier");
    ApiConfig.testConfig.put("grouperClient.remoteGrouperTest.source.externalSubjects.remote.sourceId", "grouperExternal");
    ApiConfig.testConfig.put("grouperClient.remoteGrouperTest.source.externalSubjects.remote.read.subjectId", "identifier");
    ApiConfig.testConfig.put("grouperClient.remoteGrouperTest.source.externalSubjects.remote.write.subjectId", "identifier");


    ApiConfig.testConfig.put("syncAnotherGrouper.unitTest.connectionName", "remoteGrouperTest");
    ApiConfig.testConfig.put("syncAnotherGrouper.unitTest.syncType", "push");
    ApiConfig.testConfig.put("syncAnotherGrouper.unitTest.cron", "0 0 5 * * ?");
    ApiConfig.testConfig.put("syncAnotherGrouper.unitTest.local.groupName", "localGroupSyncTest:localTestPush");
    
    this.remoteTestFolder = GrouperConfig.getProperty("junit.test.groupSync.folder");
    assertTrue(StringUtils.isNotBlank(this.remoteTestFolder));
    
    ApiConfig.testConfig.put("syncAnotherGrouper.unitTest.remote.groupName", this.remoteTestFolder + ":" + "remoteTestPush");
    
    String addExternalSubjectIfNotExist = StringUtils.defaultString(GrouperConfig.getProperty("junit.test.groupSync.addExternalSubjectIfNotFound"), "true");

    ApiConfig.testConfig.put("syncAnotherGrouper.unitTest.addExternalSubjectIfNotFound", addExternalSubjectIfNotExist);
    
    //##########################  PULL
    
    ApiConfig.testConfig.put("syncAnotherGrouper.unitTestPull.connectionName", "remoteGrouperPullTest");
    ApiConfig.testConfig.put("syncAnotherGrouper.unitTestPull.syncType", "pull");
    ApiConfig.testConfig.put("syncAnotherGrouper.unitTestPull.cron", "0 0 5 * * ?");
    ApiConfig.testConfig.put("syncAnotherGrouper.unitTestPull.local.groupName", "localGroupSyncTest:localTestPush");
    
    this.remoteTestFolder = GrouperConfig.getProperty("junit.test.groupSync.folder");
    assertTrue(StringUtils.isNotBlank(this.remoteTestFolder));
    
    ApiConfig.testConfig.put("syncAnotherGrouper.unitTestPull.remote.groupName", this.remoteTestFolder + ":" + "remoteTestPush");
    
    ApiConfig.testConfig.put("syncAnotherGrouper.unitTestPull.addExternalSubjectIfNotFound", addExternalSubjectIfNotExist);
    
  }

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new GroupSyncDaemonTest("testSyncGroupPush"));
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
    
    
    Group localTestPush = new GroupSave(grouperSession).assignName("localGroupSyncTest:localTestPush")
      .assignCreateParentStemsIfNotExist(true).save();
    
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
    new GcGroupSave().addGroupToSave(wsGroupToSave).execute();

  }
  
  /**
   * @param groupName 
   * @param stemName 
   * @param extension 
   * @return the group name
   */
  public static Set<String> remoteMembers(String groupName) {
    
    WsGetMembersResults wsGetMembersResults = new GcGetMembers().addGroupName(groupName)
      .addSubjectAttributeName("identifier").execute();
    WsGetMembersResult wsGetMembersResult = wsGetMembersResults.getResults()[0];

    Set<String> identifiers = new HashSet<String>();
    if (wsGetMembersResult != null && wsGetMembersResult.getWsSubjects() != null) {
      for (WsSubject wsSubject : wsGetMembersResult.getWsSubjects()) {
  
        String identifier = GrouperClientUtils.subjectAttributeValue(
            wsSubject, wsGetMembersResults.getSubjectAttributeNames(), "identifier");
        if (!StringUtils.isBlank(identifier)) {
          identifiers.add(identifier);
        }
  
      }
    }
    return identifiers;
  }
}
