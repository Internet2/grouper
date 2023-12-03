package edu.internet2.middleware.grouper.dataField;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.cfg.dbConfig.GrouperDbConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
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
    
  }

}
