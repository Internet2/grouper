package edu.internet2.middleware.grouper.dataField;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.cfg.dbConfig.GrouperDbConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouperClient.config.ConfigPropertiesCascadeBase;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import junit.textui.TestRunner;

public class GrouperDataEngineTest extends GrouperTest {
  
  public GrouperDataEngineTest(String name) {
    super(name);
  }

  public static void main(String[] args) {
    TestRunner.run(new GrouperDataEngineTest("testCalculateHighestLevelAccess"));
  }

  public void testCalculateHighestLevelAccess() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Group viewersGroup = new GroupSave(grouperSession).assignName("test:viewersGroup").assignCreateParentStemsIfNotExist(true).save();
    Group updatersGroup = new GroupSave(grouperSession).assignName("test:updatersGroup").assignCreateParentStemsIfNotExist(true).save();
    Group readersGroup = new GroupSave(grouperSession).assignName("test:readersGroup").assignCreateParentStemsIfNotExist(true).save();
    
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperPrivacyRealm.configId.privacyRealmName").value("realmName").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperPrivacyRealm.configId.privacyRealmPublic").value("false").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperPrivacyRealm.configId.privacyRealmAuthenticated").value("false").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperPrivacyRealm.configId.privacyRealmSysadminsCanView").value("false").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperPrivacyRealm.configId.privacyRealmViewersGroupName").value("test:viewersGroup").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperPrivacyRealm.configId.privacyRealmUpdatersGroupName").value("test:updatersGroup").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperPrivacyRealm.configId.privacyRealmReadersGroupName").value("test:readersGroup").store();
    
    GrouperPrivacyRealmConfig privacyRealmConfig = new GrouperPrivacyRealmConfig();
    privacyRealmConfig.readFromConfig("configId");
    
    String highestLevelAccess = GrouperDataEngine.calculateHighestLevelAccess(privacyRealmConfig, SubjectTestHelper.SUBJ0);
    
    assertEquals("", highestLevelAccess);

    GrouperDataEngine.clearHighestLevelCache();
    //now let's add test.subject.0 to viewersGroup
    viewersGroup.addMember(SubjectTestHelper.SUBJ0);
    highestLevelAccess = GrouperDataEngine.calculateHighestLevelAccess(privacyRealmConfig, SubjectTestHelper.SUBJ0);
    assertEquals("view", highestLevelAccess);
    
    GrouperDataEngine.clearHighestLevelCache();
    //now let's add test.subject.0 to readersGroup
    readersGroup.addMember(SubjectTestHelper.SUBJ0);
    highestLevelAccess = GrouperDataEngine.calculateHighestLevelAccess(privacyRealmConfig, SubjectTestHelper.SUBJ0);
    assertEquals("read", highestLevelAccess);
    
    GrouperDataEngine.clearHighestLevelCache();
    //now let's add test.subject.0 to updatersGroup
    updatersGroup.addMember(SubjectTestHelper.SUBJ0);
    highestLevelAccess = GrouperDataEngine.calculateHighestLevelAccess(privacyRealmConfig, SubjectTestHelper.SUBJ0);
    assertEquals("update", highestLevelAccess);
    
    //now let's say sysadmin access to true
    GrouperDataEngine.clearHighestLevelCache();
    Group sysadminViewersGroup = new GroupSave(grouperSession).assignName("test:sysadminViewersGroup").assignCreateParentStemsIfNotExist(true).save();
    
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.wheel.viewonly.use", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.wheel.viewonly.group", sysadminViewersGroup.getName());
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperPrivacyRealm.configId.privacyRealmSysadminsCanView").value("true").store();
    sysadminViewersGroup.addMember(SubjectTestHelper.SUBJ0);
    privacyRealmConfig.readFromConfig("configId");
    
    highestLevelAccess = GrouperDataEngine.calculateHighestLevelAccess(privacyRealmConfig, SubjectTestHelper.SUBJ0);
    assertEquals("update", highestLevelAccess); // highest level access should still be update because 
    
    GrouperDataEngine.clearHighestLevelCache();
    Group sysadminReadersGroup = new GroupSave(grouperSession).assignName("test:sysadminReadersGroup").assignCreateParentStemsIfNotExist(true).save();
    
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperPrivacyRealm.configId.privacyRealmSysadminsCanView").value("true").store();
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.wheel.readonly.use", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.wheel.readonly.group", sysadminReadersGroup.getName());
    
    sysadminReadersGroup.addMember(SubjectTestHelper.SUBJ0);
    
    highestLevelAccess = GrouperDataEngine.calculateHighestLevelAccess(privacyRealmConfig, SubjectTestHelper.SUBJ0);
    assertEquals("update", highestLevelAccess); // highest level access should still be update
    
    GrouperDataEngine.clearHighestLevelCache();
    Group sysadminGroup = new GroupSave(grouperSession).assignName("test:sysadminGroup").assignCreateParentStemsIfNotExist(true).save();
    
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperPrivacyRealm.configId.privacyRealmSysadminsCanView").value("true").store();
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.wheel.use", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.wheel.group", sysadminGroup.getName());
    sysadminGroup.addMember(SubjectTestHelper.SUBJ0);
    
    highestLevelAccess = GrouperDataEngine.calculateHighestLevelAccess(privacyRealmConfig, SubjectTestHelper.SUBJ0);
    assertEquals("update", highestLevelAccess); // highest level access should still be update
    
    //let's start with a new user subj1 and add them to only sysadmin viewers group
    GrouperDataEngine.clearHighestLevelCache();
    sysadminViewersGroup.addMember(SubjectTestHelper.SUBJ1);
    highestLevelAccess = GrouperDataEngine.calculateHighestLevelAccess(privacyRealmConfig, SubjectTestHelper.SUBJ1);
    assertEquals("view", highestLevelAccess);
    
    GrouperDataEngine.clearHighestLevelCache();
    sysadminReadersGroup.addMember(SubjectTestHelper.SUBJ1);
    highestLevelAccess = GrouperDataEngine.calculateHighestLevelAccess(privacyRealmConfig, SubjectTestHelper.SUBJ1);
    assertEquals("read", highestLevelAccess);
    
    GrouperDataEngine.clearHighestLevelCache();
    sysadminGroup.addMember(SubjectTestHelper.SUBJ1);
    highestLevelAccess = GrouperDataEngine.calculateHighestLevelAccess(privacyRealmConfig, SubjectTestHelper.SUBJ1);
    assertEquals("update", highestLevelAccess);
    
  }

}
