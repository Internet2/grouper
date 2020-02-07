/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperBox;

import java.util.Map;
import java.util.Random;
import java.util.Set;

import junit.framework.TestCase;
import junit.textui.TestRunner;

import com.box.sdk.BoxUser.Status;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.Stem.Scope;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouperClient.util.ExpirableCache;
import edu.internet2.middleware.grouperClient.util.GrouperClientConfig;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.subject.Subject;


/**
 *
 */
public class GrouperBoxFullRefreshTest extends TestCase {

  
  
  /**
   * @param name
   */
  public GrouperBoxFullRefreshTest(String name) {
    super(name);
  }

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    //TestRunner.run(GrouperBoxFullRefreshTest.class);
    TestRunner.run(new GrouperBoxFullRefreshTest("testAddGroupDeleteGroupWithMembershipsNotValidUser"));
    
  }


  
  /**
   * @see junit.framework.TestCase#setUp()
   */
  @Override
  public void setUp() throws Exception {
    super.setUp();
    
    String requireGroupName = GrouperClientConfig.retrieveConfig().propertyValueStringRequired("grouperBox.requireGroup");

    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    //get rid of old groups from tests not deleted
    Stem parentFolder = StemFinder.findByName(grouperSession, 
        GrouperClientConfig.retrieveConfig().propertyValueString("grouperBox.folder.name.withBoxGroups"), true);
    Set<Group> groups = new GroupFinder().assignParentStemId(parentFolder.getId()).assignStemScope(Scope.ONE).findGroups();
    
    for (Group group : GrouperClientUtils.nonNull(groups)) {
      
      if (group.getExtension().startsWith("randgroup")) {
        group.delete();
      }
    }
    
    
    Group group = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true)
      .assignName(requireGroupName).save();

    //make sure all three are box users
    Subject subject1 = SubjectFinder.findById(GrouperBoxFullRefreshTest.subject1id, true);
    group.addMember(subject1, false);
    Subject subject2 = SubjectFinder.findById(GrouperBoxFullRefreshTest.subject2id, true);
    group.addMember(subject2, false);
    Subject subject3 = SubjectFinder.findById(subject3id, true);
    group.addMember(subject3, false);
    group.deleteMember(SubjectTestHelper.SUBJ0, false);

    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("grouperBox.statusDeprovisionedUsers", "inactive");
    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("grouperBox.statusUndeprovisionedUsers", "active");
    
    ExpirableCache.clearAll();
    
    //make sure box users are active
    Map<String, GrouperBoxUser> boxUsers = GrouperBoxCommands.retrieveBoxUsers();
    for (Subject subject : new Subject[]{subject1, subject2, subject3}) {
      String loginid = subject.getAttributeValue("email");
      GrouperBoxUser grouperBoxUser = boxUsers.get(loginid);
      if (grouperBoxUser.getBoxUserInfo().getStatus() != Status.ACTIVE) {
        grouperBoxUser.getBoxUserInfo().setStatus(Status.ACTIVE);
        GrouperBoxCommands.updateBoxUser(grouperBoxUser, false);
      }
    }
    
    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("grouperBox.deleteGroupsInBoxWhichArentInGrouper", "true");

    GrouperBoxFullRefresh.fullRefreshLogic();

    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("grouperBox.deleteGroupsInBoxWhichArentInGrouper", "false");

    GrouperSession.stopQuietly(grouperSession);

  }

  /**
   * @see junit.framework.TestCase#tearDown()
   */
  @Override
  public void tearDown() throws Exception {
    super.tearDown();
    
    //leave it like we found it
    this.setUp();
  }

  /** test subject */
  public static String subject1id = "johnjohnsonsmithYahoo";

  /** test subject */
  public static String subject2id = "mchyzerGoogle";
  
  /** test suject */
  public static String subject3id = "kfb1";

  /**
   * 
   */
  public void testAddGroupDeleteGroup() {

    String groupName = GrouperClientConfig.retrieveConfig()
        .propertyValueStringRequired("grouperBox.folder.name.withBoxGroups") 
        + ":" + "randgroup" + Math.abs(new Random().nextInt());
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Group group = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true)
      .assignName(groupName).save();
    
    assertFalse(GrouperBoxCommands.retrieveBoxGroups().containsKey(group.getExtension()));
    
    GrouperBoxFullRefresh.fullRefreshLogic();
    
    assertTrue(GrouperBoxCommands.retrieveBoxGroups().containsKey(group.getExtension()));
    
    group.delete();

    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("grouperBox.deleteGroupsInBoxWhichArentInGrouper", "false");

    GrouperBoxFullRefresh.fullRefreshLogic();

    assertTrue(GrouperBoxCommands.retrieveBoxGroups().containsKey(group.getExtension()));
    
    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("grouperBox.deleteGroupsInBoxWhichArentInGrouper", "true");

    GrouperBoxFullRefresh.fullRefreshLogic();

    assertFalse(GrouperBoxCommands.retrieveBoxGroups().containsKey(group.getExtension()));
    
    GrouperSession.stopQuietly(grouperSession);

    
  }

  /**
   * 
   */
  public void testAddGroupDeleteGroupWithMembershipsDontDeleteFullGroup() {

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
    
    GrouperBoxFullRefresh.fullRefreshLogic();
    Map<String, GrouperBoxGroup> boxGroupMap = GrouperBoxCommands.retrieveBoxGroups();
    assertTrue(boxGroupMap.containsKey(group.getExtension()));
    GrouperBoxGroup grouperBoxGroup = boxGroupMap.get(group.getExtension());
    assertTrue(grouperBoxGroup.getMemberUsers().containsKey(subject1.getAttributeValue("email")));
    assertTrue(grouperBoxGroup.getMemberUsers().containsKey(subject2.getAttributeValue("email")));
    assertFalse(grouperBoxGroup.getMemberUsers().containsKey(subject3.getAttributeValue("email")));
    
    //change a membership
    group.deleteMember(subject2);
    group.addMember(subject3);

    GrouperBoxFullRefresh.fullRefreshLogic();
    boxGroupMap = GrouperBoxCommands.retrieveBoxGroups();
    assertTrue(boxGroupMap.containsKey(group.getExtension()));
    grouperBoxGroup = boxGroupMap.get(group.getExtension());
    assertTrue(grouperBoxGroup.getMemberUsers().containsKey(subject1.getAttributeValue("email")));
    assertFalse(grouperBoxGroup.getMemberUsers().containsKey(subject2.getAttributeValue("email")));
    assertTrue(grouperBoxGroup.getMemberUsers().containsKey(subject3.getAttributeValue("email")));

    group.delete();

    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("grouperBox.deleteGroupsInBoxWhichArentInGrouper", "false");

    GrouperBoxFullRefresh.fullRefreshLogic();

    boxGroupMap = GrouperBoxCommands.retrieveBoxGroups();
    assertTrue(boxGroupMap.containsKey(group.getExtension()));
    grouperBoxGroup = boxGroupMap.get(group.getExtension());
    assertFalse(grouperBoxGroup.getMemberUsers().containsKey(subject1.getAttributeValue("email")));
    assertFalse(grouperBoxGroup.getMemberUsers().containsKey(subject2.getAttributeValue("email")));
    assertFalse(grouperBoxGroup.getMemberUsers().containsKey(subject3.getAttributeValue("email")));
    
    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("grouperBox.deleteGroupsInBoxWhichArentInGrouper", "true");

    GrouperBoxFullRefresh.fullRefreshLogic();

    assertFalse(GrouperBoxCommands.retrieveBoxGroups().containsKey(group.getExtension()));
    
    GrouperSession.stopQuietly(grouperSession);

  }

  /**
   * 
   */
  public void testAddGroupDeleteGroupWithMembershipsDeleteFullGroup() {

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
    
    GrouperBoxFullRefresh.fullRefreshLogic();
    Map<String, GrouperBoxGroup> boxGroupMap = GrouperBoxCommands.retrieveBoxGroups();
    assertTrue(boxGroupMap.containsKey(group.getExtension()));
    GrouperBoxGroup grouperBoxGroup = boxGroupMap.get(group.getExtension());
    assertTrue(grouperBoxGroup.getMemberUsers().containsKey(subject1.getAttributeValue("email")));
    assertTrue(grouperBoxGroup.getMemberUsers().containsKey(subject2.getAttributeValue("email")));
    assertFalse(grouperBoxGroup.getMemberUsers().containsKey(subject3.getAttributeValue("email")));
    
    //change a membership
    group.deleteMember(subject2);
    group.addMember(subject3);

    GrouperBoxFullRefresh.fullRefreshLogic();
    boxGroupMap = GrouperBoxCommands.retrieveBoxGroups();
    assertTrue(boxGroupMap.containsKey(group.getExtension()));
    grouperBoxGroup = boxGroupMap.get(group.getExtension());
    assertTrue(grouperBoxGroup.getMemberUsers().containsKey(subject1.getAttributeValue("email")));
    assertFalse(grouperBoxGroup.getMemberUsers().containsKey(subject2.getAttributeValue("email")));
    assertTrue(grouperBoxGroup.getMemberUsers().containsKey(subject3.getAttributeValue("email")));

    group.delete();

    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("grouperBox.deleteGroupsInBoxWhichArentInGrouper", "true");

    GrouperBoxFullRefresh.fullRefreshLogic();

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

    GrouperBoxFullRefresh.fullRefreshLogic();

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

    GrouperBoxFullRefresh.fullRefreshLogic();
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

    GrouperBoxFullRefresh.fullRefreshLogic();

    boxGroupMap = GrouperBoxCommands.retrieveBoxGroups();
    assertFalse(boxGroupMap.containsKey(group.getExtension()));
    
    GrouperSession.stopQuietly(grouperSession);

  }

}
