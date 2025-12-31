package edu.internet2.middleware.grouper.userLifecycle;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.loader.OtherJobBase.OtherJobInput;
import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;
import edu.internet2.middleware.grouper.app.usdu.SubjectChangeDaemon;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.cfg.dbConfig.GrouperDbConfig;
import edu.internet2.middleware.grouper.changeLog.ChangeLogTempToEntity;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.sqlCache.SqlCacheFullSyncDaemon;
import edu.internet2.middleware.grouper.sqlCache.SqlCacheGroup;
import edu.internet2.middleware.grouper.sqlCache.SqlCacheGroupDao;
import edu.internet2.middleware.grouper.util.GrouperEmail;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import junit.textui.TestRunner;

public class GroupPolicyUserLifecycleFullDaemonTest extends GrouperTest {
  
  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new GroupPolicyUserLifecycleFullDaemonTest("testSendEmail"));
  }
  
  /**
   * 
   */
  public GroupPolicyUserLifecycleFullDaemonTest() {
    super();
  }

  /**
   * 
   * @param name
   */
  public GroupPolicyUserLifecycleFullDaemonTest(String name) {
    super(name);
  }
  
  
  public void testSendEmail() {
    
    Group itDeptGroup = new GroupSave(GrouperSession.staticGrouperSession()).assignName("test:it-dept").assignCreateParentStemsIfNotExist(true).save();
    
    
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperUserLifecycleEvent.testLifecycleEventConfigId.name").value("group user add event").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperUserLifecycleEvent.testLifecycleEventConfigId.description").value("group user add description").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperUserLifecycleEvent.testLifecycleEventConfigId.groupUserAddGroup").value(itDeptGroup.getUuid()).store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperUserLifecycleEvent.testLifecycleEventConfigId.naturalLanguageDescriptionJexlPrivileged").value("${true}").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperUserLifecycleEvent.testLifecycleEventConfigId.naturalLanguageDescriptionJexlPrivilegedGroupIdOrName").value(itDeptGroup.getUuid()).store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperUserLifecycleEvent.testLifecycleEventConfigId.naturalLanguageDescriptionJexlUnprivileged").value("${true}").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperUserLifecycleEvent.testLifecycleEventConfigId.trigger").value("groupUserAdd").store();

    
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperUserLifecycleAction.action_config_id.name").value("email user action").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperUserLifecycleAction.action_config_id.actionType").value("emailUser").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperUserLifecycleAction.action_config_id.description").value("email user action description").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperUserLifecycleAction.action_config_id.emailBody").value("email body").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperUserLifecycleAction.action_config_id.emailSubjectLine").value("email subject").store();
    
    
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperUserLifecyclePolicy.test_policy.name").value("policy name").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperUserLifecyclePolicy.test_policy.description").value("policy description").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperUserLifecyclePolicy.test_policy.isPublic").value("true").store();

    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperUserLifecyclePolicyPart.policy_part_config.policy").value("test_policy").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperUserLifecyclePolicyPart.policy_part_config.numberOfLifecycleEvents").value("1").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperUserLifecyclePolicyPart.policy_part_config.numberOfLifecycleActions").value("1").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperUserLifecyclePolicyPart.policy_part_config.lifeCycleEvents.0.lifeCycleEventConfig").value("testLifecycleEventConfigId").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperUserLifecyclePolicyPart.policy_part_config.lifeCycleActions.0.lifeCycleActionConfig").value("action_config_id").store();
    
    
    Group policyGroup = new GroupSave(GrouperSession.staticGrouperSession()).assignName("test:policy-group").assignCreateParentStemsIfNotExist(true).save();
    itDeptGroup.addMember(SubjectFinder.findById("test.subject.0", true));
    policyGroup.addMember(SubjectFinder.findById("test.subject.0", true));
    
    
//    SqlCacheGroup sqlCacheGroup = new SqlCacheGroup();
//    sqlCacheGroup.setFieldInternalId(fieldInternalId);
//    
//    SqlCacheGroupDao.store(sqlCacheGroup);

    //This will insert entries in the cache tables
    ChangeLogTempToEntity.convertRecords();
    
    
//    OtherJobInput otherJobInput = new OtherJobInput();
//    Hib3GrouperLoaderLog hib3GrouperLoaderLog = new Hib3GrouperLoaderLog();
//    otherJobInput.setJobName("OTHER_JOB_userLifecycleFullDaemon");
//    otherJobInput.setHib3GrouperLoaderLog(hib3GrouperLoaderLog);
//    new SqlCacheFullSyncDaemon().run(otherJobInput);
    
    GrouperUtil.sleep(5000L);
    
    UserLifecycleService.savePolicyConfigOnGroup(policyGroup, "test_policy", GrouperSession.staticGrouperSession().internal_getRootSession().getSubject());
    
    UserLifecycleEngine.syncUserLifecycleEventConfigs(null);
    
    GrouperUtil.sleep(2000L);
    
    // now run the UserLifecycleFullDaemon to add an entry in the grouper_lifecycle_event table
    OtherJobInput otherJobInput1 = new OtherJobInput();
    Hib3GrouperLoaderLog hib3GrouperLoaderLog1 = new Hib3GrouperLoaderLog();
    otherJobInput1.setJobName("OTHER_JOB_userLifecycleFullDaemon");
    otherJobInput1.setHib3GrouperLoaderLog(hib3GrouperLoaderLog1);
    new UserLifecycleFullDaemon().run(otherJobInput1);
    
    GrouperUtil.sleep(5000L);
    
    Long lifecycleInternalId = new GcDbAccess().sql("select internal_id from grouper_lifecycle_event").select(Long.class);    
    assertNotNull(lifecycleInternalId);
    
    OtherJobInput otherJobInput2 = new OtherJobInput();
    Hib3GrouperLoaderLog hib3GrouperLoaderLog2 = new Hib3GrouperLoaderLog();
    otherJobInput2.setJobName("OTHER_JOB_groupPolicyUserLifecycleFullDaemon");
    otherJobInput2.setHib3GrouperLoaderLog(hib3GrouperLoaderLog2);
    new GroupPolicyUserLifecycleFullDaemon().run(otherJobInput2);
    
    long emailSent = GrouperEmail.testingEmailCount;
//    GrouperEmail.testingEmails().get(0).getTo()
    assertEquals(1, emailSent);
    
  }

}
