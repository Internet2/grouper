/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperBox;

import java.util.Map;
import java.util.Random;

import com.box.sdk.BoxUser.Status;

import junit.framework.TestCase;
import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.app.loader.GrouperLoader;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouperClient.util.ExpirableCache;
import edu.internet2.middleware.grouperClient.util.GrouperClientConfig;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.subject.Subject;


/**
 *
 */
public class GrouperBoxMessageConsumerTest extends TestCase {
  
  /** if using messaging */
  private static final boolean useMessaging = false;
  
  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    
    TestRunner.run(new GrouperBoxMessageConsumerTest("testAddGroupDeleteGroupWithMembershipsNotValidUser"));
//    TestRunner.run(new GrouperBoxMessageConsumerTest("testAddGroupDeleteGroupWithMembershipsNotValidUser"));
  }

  /**
   * @see junit.framework.TestCase#setUp()
   */
  @Override
  protected void setUp() throws Exception {
    super.setUp();

    new GrouperBoxFullRefreshTest("").setUp();

    GrouperSession grouperSession = GrouperSession.startRootSession();

    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_consumer_boxEsb");
    
    if (useMessaging) {
      HibernateSession.bySqlStatic().executeSql("delete from grouper_message where queue_name=?", 
          GrouperClientUtils.toList((Object)
              GrouperClientConfig.retrieveConfig().propertyValueStringRequired("grouperBox.messaging.queueName")));
    }
    
    //sync everything up by full sync
    GrouperBoxFullRefresh.fullRefreshLogic();
    
    GrouperSession.stopQuietly(grouperSession);
    
  }

  /**
   * @see junit.framework.TestCase#tearDown()
   */
  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
  }

  /**
   * @param name
   */
  public GrouperBoxMessageConsumerTest(String name) {
    super(name);
  }

  
  /**
   * 
   */
  public void testAddGroupDeleteGroupIncrementalNoDelete() {

    String groupName = GrouperClientConfig.retrieveConfig()
        .propertyValueStringRequired("grouperBox.folder.name.withBoxGroups") 
        + ":" + "randgroup" + Math.abs(new Random().nextInt());
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Group group = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true)
      .assignName(groupName).save();
    
    
    assertFalse(GrouperBoxCommands.retrieveBoxGroups().containsKey(group.getExtension()));
    
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_consumer_boxEsb");
    if (useMessaging) {
      GrouperBoxMessageConsumer.incrementalSync();
    }
    
    assertTrue(GrouperBoxCommands.retrieveBoxGroups().containsKey(group.getExtension()));
    
    group.delete();

    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("grouperBox.deleteGroupsInBoxWhichArentInGrouper", "false");

    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_consumer_boxEsb");
    if (useMessaging) {
      GrouperBoxMessageConsumer.incrementalSync();
    }

    assertTrue(GrouperBoxCommands.retrieveBoxGroups().containsKey(group.getExtension()));
    
    GrouperSession.stopQuietly(grouperSession);

    
  }
  /**
   * 
   */
  public void testAddGroupDeleteGroupIncrementalDelete() {

    String groupName = GrouperClientConfig.retrieveConfig()
        .propertyValueStringRequired("grouperBox.folder.name.withBoxGroups") 
        + ":" + "randgroup" + Math.abs(new Random().nextInt());
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Group group = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true)
      .assignName(groupName).save();
    
    
    assertFalse(GrouperBoxCommands.retrieveBoxGroups().containsKey(group.getExtension()));
    
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_consumer_boxEsb");
    if (useMessaging) {
      GrouperBoxMessageConsumer.incrementalSync();
    }
    
    assertTrue(GrouperBoxCommands.retrieveBoxGroups().containsKey(group.getExtension()));
    
    group.delete();

    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("grouperBox.deleteGroupsInBoxWhichArentInGrouper", "true");

    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_consumer_boxEsb");
    if (useMessaging) {
      GrouperBoxMessageConsumer.incrementalSync();
    }

    assertFalse(GrouperBoxCommands.retrieveBoxGroups().containsKey(group.getExtension()));
    
    GrouperSession.stopQuietly(grouperSession);

    
  }
  
  /**
   * 
   */
  public void testAddGroupDeleteGroupWithMembershipsDontDeleteGroup() {

    String groupName = GrouperClientConfig.retrieveConfig()
        .propertyValueStringRequired("grouperBox.folder.name.withBoxGroups") 
        + ":" + "randgroup" + Math.abs(new Random().nextInt());
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Group group = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true)
      .assignName(groupName).save();
    
    Subject subject1 = SubjectFinder.findById(GrouperBoxFullRefreshTest.subject1id, true);
    group.addMember(subject1);
    Subject subject2 = SubjectFinder.findById(GrouperBoxFullRefreshTest.subject2id, true);
    Subject subject3 = SubjectFinder.findById(GrouperBoxFullRefreshTest.subject3id, true);
    group.addMember(subject2);
    
    assertFalse(GrouperBoxCommands.retrieveBoxGroups().containsKey(group.getExtension()));
    
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_consumer_boxEsb");
    if (useMessaging) {
      GrouperBoxMessageConsumer.incrementalSync();
    }

    Map<String, GrouperBoxGroup> boxGroupMap = GrouperBoxCommands.retrieveBoxGroups();
    assertTrue(boxGroupMap.containsKey(group.getExtension()));
    GrouperBoxGroup grouperBoxGroup = boxGroupMap.get(group.getExtension());
    assertTrue(grouperBoxGroup.getMemberUsers().containsKey(subject1.getAttributeValue("email")));
    assertTrue(grouperBoxGroup.getMemberUsers().containsKey(subject2.getAttributeValue("email")));
    assertFalse(grouperBoxGroup.getMemberUsers().containsKey(subject3.getAttributeValue("email")));
    
    //change a membership
    group.deleteMember(subject2);
    group.addMember(subject3);

    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_consumer_boxEsb");
    if (useMessaging) {
      GrouperBoxMessageConsumer.incrementalSync();
    }

    boxGroupMap = GrouperBoxCommands.retrieveBoxGroups();
    assertTrue(boxGroupMap.containsKey(group.getExtension()));
    grouperBoxGroup = boxGroupMap.get(group.getExtension());
    assertTrue(grouperBoxGroup.getMemberUsers().containsKey(subject1.getAttributeValue("email")));
    assertFalse(grouperBoxGroup.getMemberUsers().containsKey(subject2.getAttributeValue("email")));
    assertTrue(grouperBoxGroup.getMemberUsers().containsKey(subject3.getAttributeValue("email")));

    group.delete();

    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("grouperBox.deleteGroupsInBoxWhichArentInGrouper", "false");

    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_consumer_boxEsb");
    if (useMessaging) {
      GrouperBoxMessageConsumer.incrementalSync();
    }

    boxGroupMap = GrouperBoxCommands.retrieveBoxGroups();
    assertTrue(boxGroupMap.containsKey(group.getExtension()));
    grouperBoxGroup = boxGroupMap.get(group.getExtension());
    assertFalse(grouperBoxGroup.getMemberUsers().containsKey(subject1.getAttributeValue("email")));
    assertFalse(grouperBoxGroup.getMemberUsers().containsKey(subject2.getAttributeValue("email")));
    assertFalse(grouperBoxGroup.getMemberUsers().containsKey(subject3.getAttributeValue("email")));
    
    
    GrouperSession.stopQuietly(grouperSession);

  }

  /**
   * 
   */
  public void testAddGroupDeleteGroupWithMembershipsDeleteGroup() {

    String groupName = GrouperClientConfig.retrieveConfig()
        .propertyValueStringRequired("grouperBox.folder.name.withBoxGroups") 
        + ":" + "randgroup" + Math.abs(new Random().nextInt());
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Group group = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true)
      .assignName(groupName).save();
    
    Subject subject1 = SubjectFinder.findById(GrouperBoxFullRefreshTest.subject1id, true);
    group.addMember(subject1);
    Subject subject2 = SubjectFinder.findById(GrouperBoxFullRefreshTest.subject2id, true);
    Subject subject3 = SubjectFinder.findById(GrouperBoxFullRefreshTest.subject3id, true);
    group.addMember(subject2);
    
    assertFalse(GrouperBoxCommands.retrieveBoxGroups().containsKey(group.getExtension()));
    
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_consumer_boxEsb");
    if (useMessaging) {
      GrouperBoxMessageConsumer.incrementalSync();
    }

    Map<String, GrouperBoxGroup> boxGroupMap = GrouperBoxCommands.retrieveBoxGroups();
    assertTrue(boxGroupMap.containsKey(group.getExtension()));
    GrouperBoxGroup grouperBoxGroup = boxGroupMap.get(group.getExtension());
    assertTrue(grouperBoxGroup.getMemberUsers().containsKey(subject1.getAttributeValue("email")));
    assertTrue(grouperBoxGroup.getMemberUsers().containsKey(subject2.getAttributeValue("email")));
    assertFalse(grouperBoxGroup.getMemberUsers().containsKey(subject3.getAttributeValue("email")));
    
    //change a membership
    group.deleteMember(subject2);
    group.addMember(subject3);

    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_consumer_boxEsb");
    if (useMessaging) {
      GrouperBoxMessageConsumer.incrementalSync();
    }

    boxGroupMap = GrouperBoxCommands.retrieveBoxGroups();
    assertTrue(boxGroupMap.containsKey(group.getExtension()));
    grouperBoxGroup = boxGroupMap.get(group.getExtension());
    assertTrue(grouperBoxGroup.getMemberUsers().containsKey(subject1.getAttributeValue("email")));
    assertFalse(grouperBoxGroup.getMemberUsers().containsKey(subject2.getAttributeValue("email")));
    assertTrue(grouperBoxGroup.getMemberUsers().containsKey(subject3.getAttributeValue("email")));

    group.delete();

    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("grouperBox.deleteGroupsInBoxWhichArentInGrouper", "true");

    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_consumer_boxEsb");
    if (useMessaging) {
      GrouperBoxMessageConsumer.incrementalSync();
    }

    boxGroupMap = GrouperBoxCommands.retrieveBoxGroups();
    assertFalse(boxGroupMap.containsKey(group.getExtension()));
    
    GrouperSession.stopQuietly(grouperSession);

  }

  /**
   * 
   */
  public void testAddGroupDeleteGroupWithMembershipsNotValidUser() {

    String groupName = GrouperClientConfig.retrieveConfig()
        .propertyValueStringRequired("grouperBox.folder.name.withBoxGroups") 
        + ":" + "randgroup" + Math.abs(new Random().nextInt());
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Group group = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true)
      .assignName(groupName).save();
    
    Subject subject1 = SubjectFinder.findById(GrouperBoxFullRefreshTest.subject1id, true);
    group.addMember(subject1);
    Subject subject2 = SubjectFinder.findById(GrouperBoxFullRefreshTest.subject2id, true);
    Subject subject3 = SubjectFinder.findById(GrouperBoxFullRefreshTest.subject3id, true);
    group.addMember(subject2);
    group.addMember(SubjectTestHelper.SUBJ0);

    String groupName2 = GrouperClientConfig.retrieveConfig()
        .propertyValueStringRequired("grouperBox.folder.name.withBoxGroups") 
        + ":" + "randgroup" + Math.abs(new Random().nextInt());
    Group group2 = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true)
      .assignName(groupName2).save();

    //should be ignored
    group.addMember(group2.toSubject());
    
    //subject 2 is not a valid member anymore, subject 0 is
    String requireGroupName = GrouperClientConfig.retrieveConfig().propertyValueStringRequired("grouperBox.requireGroup");
    Group requireGroup = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true)
      .assignName(requireGroupName).save();
    requireGroup.deleteMember(subject2, false);
    requireGroup.addMember(SubjectTestHelper.SUBJ0, false);
    
    assertFalse(GrouperBoxCommands.retrieveBoxGroups().containsKey(group.getExtension()));
    
    ExpirableCache.clearAll();

    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_consumer_boxEsb");
    if (useMessaging) {
      GrouperBoxMessageConsumer.incrementalSync();
    }

    Map<String, GrouperBoxGroup> boxGroupMap = GrouperBoxCommands.retrieveBoxGroups();
    assertTrue(boxGroupMap.containsKey(group.getExtension()));
    GrouperBoxGroup grouperBoxGroup = boxGroupMap.get(group.getExtension());
    assertTrue(grouperBoxGroup.getMemberUsers().containsKey(subject1.getAttributeValue("email")));
    assertFalse(grouperBoxGroup.getMemberUsers().containsKey(subject2.getAttributeValue("email")));
    assertFalse(grouperBoxGroup.getMemberUsers().containsKey(subject3.getAttributeValue("email")));

    //subject2 should now be inactive
    assertEquals(Status.INACTIVE, GrouperBoxUser.retrieveUsers().get(subject2.getAttributeValue("email")).getBoxUserInfo().getStatus());
    
    assertEquals(1, grouperBoxGroup.getMemberUsers().size());
    
    //make subject 1 invalid too
    requireGroup.deleteMember(subject1, false);
    group.addMember(subject3, false);

    ExpirableCache.clearAll();

    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_consumer_boxEsb");
    if (useMessaging) {
      GrouperBoxMessageConsumer.incrementalSync();
    }

    boxGroupMap = GrouperBoxCommands.retrieveBoxGroups();
    assertTrue(boxGroupMap.containsKey(group.getExtension()));
    grouperBoxGroup = boxGroupMap.get(group.getExtension());
    assertFalse(grouperBoxGroup.getMemberUsers().containsKey(subject1.getAttributeValue("email")));
    assertFalse(grouperBoxGroup.getMemberUsers().containsKey(subject2.getAttributeValue("email")));
    assertTrue(grouperBoxGroup.getMemberUsers().containsKey(subject3.getAttributeValue("email")));
    assertEquals(1, grouperBoxGroup.getMemberUsers().size());

    assertEquals(Status.INACTIVE, GrouperBoxUser.retrieveUsers().get(subject1.getAttributeValue("email")).getBoxUserInfo().getStatus());

    group2.delete();
    group.delete();

    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("grouperBox.deleteGroupsInBoxWhichArentInGrouper", "true");

    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_consumer_boxEsb");
    if (useMessaging) {
      GrouperBoxMessageConsumer.incrementalSync();
    }

    boxGroupMap = GrouperBoxCommands.retrieveBoxGroups();
    assertFalse(boxGroupMap.containsKey(group.getExtension()));
    
    GrouperSession.stopQuietly(grouperSession);

  }

  
}
