package edu.internet2.middleware.grouper.userLifecycle;

import java.time.Instant;
import java.util.List;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.app.loader.OtherJobBase.OtherJobInput;
import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;
import edu.internet2.middleware.grouper.cfg.dbConfig.GrouperDbConfig;
import edu.internet2.middleware.grouper.changeLog.ChangeLogTempToEntity;
import edu.internet2.middleware.grouper.helper.GrouperTest;
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
    TestRunner.run(new GroupPolicyUserLifecycleFullDaemonTest("testAddEndDateToMembership"));
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
    
    
    //This will insert entries in the cache tables
    ChangeLogTempToEntity.convertRecords();
    
    GrouperUtil.sleep(2000L);
    
    UserLifecycleService.savePolicyConfigOnGroup(policyGroup, "test_policy", GrouperSession.staticGrouperSession().internal_getRootSession().getSubject());
    
    UserLifecycleEngine.syncUserLifecycleEventConfigs(null);
    
    GrouperUtil.sleep(2000L);
    
    // now run the UserLifecycleFullDaemon to add an entry in the grouper_lifecycle_event table
    OtherJobInput otherJobInput1 = new OtherJobInput();
    Hib3GrouperLoaderLog hib3GrouperLoaderLog1 = new Hib3GrouperLoaderLog();
    otherJobInput1.setJobName("OTHER_JOB_userLifecycleFullDaemon");
    otherJobInput1.setHib3GrouperLoaderLog(hib3GrouperLoaderLog1);
    new UserLifecycleFullDaemon().run(otherJobInput1);
    
    GrouperUtil.sleep(2000L);
    
    Long lifecycleInternalId = new GcDbAccess().sql("select internal_id from grouper_lifecycle_event").select(Long.class);    
    assertNotNull(lifecycleInternalId);
    
    OtherJobInput otherJobInput2 = new OtherJobInput();
    Hib3GrouperLoaderLog hib3GrouperLoaderLog2 = new Hib3GrouperLoaderLog();
    otherJobInput2.setJobName("OTHER_JOB_groupPolicyUserLifecycleFullDaemon");
    otherJobInput2.setHib3GrouperLoaderLog(hib3GrouperLoaderLog2);
    new GroupPolicyUserLifecycleFullDaemon().run(otherJobInput2);
    
    long emailSent = GrouperEmail.testingEmailCount;
    assertEquals(1, emailSent);
    assertEquals("email body", GrouperEmail.testingEmails().get(0).getBody());
    
    
  }
  
  public void testAddEndDateToMembership() {
    
    Group itDeptGroup = new GroupSave(GrouperSession.staticGrouperSession()).assignName("test:it-dept").assignCreateParentStemsIfNotExist(true).save();
    
    
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperUserLifecycleEvent.testLifecycleEventConfigId.name").value("group user add event").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperUserLifecycleEvent.testLifecycleEventConfigId.description").value("group user add description").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperUserLifecycleEvent.testLifecycleEventConfigId.groupUserAddGroup").value(itDeptGroup.getUuid()).store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperUserLifecycleEvent.testLifecycleEventConfigId.naturalLanguageDescriptionJexlPrivileged").value("${true}").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperUserLifecycleEvent.testLifecycleEventConfigId.naturalLanguageDescriptionJexlPrivilegedGroupIdOrName").value(itDeptGroup.getUuid()).store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperUserLifecycleEvent.testLifecycleEventConfigId.naturalLanguageDescriptionJexlUnprivileged").value("${true}").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperUserLifecycleEvent.testLifecycleEventConfigId.trigger").value("groupUserAdd").store();

    
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperUserLifecycleAction.action_config_id.name").value("add end date on membership").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperUserLifecycleAction.action_config_id.actionType").value("addEndDateOnMembership").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperUserLifecycleAction.action_config_id.description").value("add end date on membership description").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperUserLifecycleAction.action_config_id.numberOfDaysInTheFuture").value("7").store();
    
    
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
    
    
    //This will insert entries in the cache tables
    ChangeLogTempToEntity.convertRecords();
    
    GrouperUtil.sleep(2000L);
    
    UserLifecycleService.savePolicyConfigOnGroup(policyGroup, "test_policy", GrouperSession.staticGrouperSession().internal_getRootSession().getSubject());
    
    UserLifecycleEngine.syncUserLifecycleEventConfigs(null);
    
    GrouperUtil.sleep(2000L);
    
    // now run the UserLifecycleFullDaemon to add an entry in the grouper_lifecycle_event table
    OtherJobInput otherJobInput1 = new OtherJobInput();
    Hib3GrouperLoaderLog hib3GrouperLoaderLog1 = new Hib3GrouperLoaderLog();
    otherJobInput1.setJobName("OTHER_JOB_userLifecycleFullDaemon");
    otherJobInput1.setHib3GrouperLoaderLog(hib3GrouperLoaderLog1);
    new UserLifecycleFullDaemon().run(otherJobInput1);
    
    GrouperUtil.sleep(2000L);
    
    Long lifecycleInternalId = new GcDbAccess().sql("select internal_id from grouper_lifecycle_event").select(Long.class);    
    assertNotNull(lifecycleInternalId);
    
    OtherJobInput otherJobInput2 = new OtherJobInput();
    Hib3GrouperLoaderLog hib3GrouperLoaderLog2 = new Hib3GrouperLoaderLog();
    otherJobInput2.setJobName("OTHER_JOB_groupPolicyUserLifecycleFullDaemon");
    otherJobInput2.setHib3GrouperLoaderLog(hib3GrouperLoaderLog2);
    new GroupPolicyUserLifecycleFullDaemon().run(otherJobInput2);
    
    GcDbAccess gcDbAccess = new GcDbAccess();
    
    StringBuilder sqlBuilder = new StringBuilder("select count(gaaamv.attribute_def_name_name2) from grouper_aval_asn_asn_mship_v gaaamv where gaaamv.attribute_def_name_name1 = ? "
        + " and group_name = ? and subject_id = ? ");
    gcDbAccess.addBindVar(UserLifecycleAttributeNames.userLifecycleStemName() +":"+ UserLifecycleAttributeNames.USER_LIFECYCLE_MSHIP_IN_FLIGHT_MARKER);
    gcDbAccess.addBindVar(policyGroup.getName());
    gcDbAccess.addBindVar("test.subject.0");
    
    Long countOfRow = gcDbAccess.sql(sqlBuilder.toString()).select(Long.class);
    assertEquals(3, countOfRow.intValue());
  }

}
