package edu.internet2.middleware.grouper.userLifecycle;

import java.util.List;
import java.util.Set;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.app.loader.OtherJobBase.OtherJobInput;
import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;
import edu.internet2.middleware.grouper.cfg.dbConfig.GrouperDbConfig;
import edu.internet2.middleware.grouper.changeLog.ChangeLogTempToEntity;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.sqlCache.SqlCacheHistoryFullSyncDaemon;
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
    TestRunner.run(new GroupPolicyUserLifecycleFullDaemonTest("testMultipleActionsAndEvents"));
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
  
  public void testRemoveUserFromGroup() {
    
    Group itDeptGroup = new GroupSave(GrouperSession.staticGrouperSession()).assignName("test:it-dept").assignCreateParentStemsIfNotExist(true).save();
    
    
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperUserLifecycleEvent.testLifecycleEventConfigId.name").value("group user add event").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperUserLifecycleEvent.testLifecycleEventConfigId.description").value("group user add description").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperUserLifecycleEvent.testLifecycleEventConfigId.groupUserAddGroup").value(itDeptGroup.getUuid()).store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperUserLifecycleEvent.testLifecycleEventConfigId.naturalLanguageDescriptionJexlPrivileged").value("${true}").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperUserLifecycleEvent.testLifecycleEventConfigId.naturalLanguageDescriptionJexlPrivilegedGroupIdOrName").value(itDeptGroup.getUuid()).store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperUserLifecycleEvent.testLifecycleEventConfigId.naturalLanguageDescriptionJexlUnprivileged").value("${true}").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperUserLifecycleEvent.testLifecycleEventConfigId.trigger").value("groupUserAdd").store();

    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperUserLifecycleAction.action_config_id.name").value("remove user from group").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperUserLifecycleAction.action_config_id.actionType").value("removeUserFromGroup").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperUserLifecycleAction.action_config_id.description").value("remove user from group description").store();
    
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
    
    Set<Member> immediateMembers = policyGroup.getImmediateMembers();
    assertEquals(0, immediateMembers.size());
  }
  
  
 public void testMultipleActionsAndEvents() {
    
    Group itDeptGroup = new GroupSave(GrouperSession.staticGrouperSession()).assignName("test:departments:IT-department").assignCreateParentStemsIfNotExist(true).save();
    Group institutionGroup = new GroupSave(GrouperSession.staticGrouperSession()).assignName("test:community:institutionMember").assignCreateParentStemsIfNotExist(true).save();
    Group allowedToSeeHRdata = new GroupSave(GrouperSession.staticGrouperSession()).assignName("test:community:allowedToSeeHRData").assignCreateParentStemsIfNotExist(true).save();
    
    itDeptGroup.addMember(SubjectFinder.findById("test.subject.0", true)); //test.subject.0 is going to leave the department, hence they will have in flight attributes on the membership in the adobeUsers group
    institutionGroup.addMember(SubjectFinder.findById("test.subject.1", true)); // test.subject.1 is going to leave the institution, hence they will be removed from the adobeUsers group
    
    Group adobeUsers = new GroupSave(GrouperSession.staticGrouperSession()).assignName("test:adobeUsers").assignCreateParentStemsIfNotExist(true).save(); //this is the policy group
    adobeUsers.addMember(SubjectFinder.findById("test.subject.0", true));
    adobeUsers.addMember(SubjectFinder.findById("test.subject.1", true));
    
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperUserLifecycleEvent.leaveDepartment.name").value("leave department").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperUserLifecycleEvent.leaveDepartment.description").value("Leaves a department").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperUserLifecycleEvent.leaveDepartment.groupUserRemoveFolder").value("test:departments").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperUserLifecycleEvent.leaveDepartment.naturalLanguageDescriptionJexlPrivileged").value("${ 'User left department: ' + groupDisplayExtension}").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperUserLifecycleEvent.leaveDepartment.naturalLanguageDescriptionJexlPrivilegedGroupIdOrName").value(allowedToSeeHRdata.getUuid()).store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperUserLifecycleEvent.leaveDepartment.naturalLanguageDescriptionJexlUnprivileged").value("${'User left a department'}").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperUserLifecycleEvent.leaveDepartment.trigger").value("groupUserRemoveFromFolder").store();
    
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperUserLifecycleEvent.leaveInstitution.name").value("leave institution").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperUserLifecycleEvent.leaveInstitution.description").value("Leaves the institution").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperUserLifecycleEvent.leaveInstitution.groupUserRemoveGroup").value(institutionGroup.getUuid()).store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperUserLifecycleEvent.leaveInstitution.naturalLanguageDescriptionJexlPrivileged").value("${ 'User left institution: ' + groupDisplayExtension}").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperUserLifecycleEvent.leaveInstitution.naturalLanguageDescriptionJexlPrivilegedGroupIdOrName").value(allowedToSeeHRdata.getUuid()).store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperUserLifecycleEvent.leaveInstitution.naturalLanguageDescriptionJexlUnprivileged").value("${'User left the institution'}").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperUserLifecycleEvent.leaveInstitution.trigger").value("groupUserRemove").store();

    
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperUserLifecycleAction.endDateOneWeek.name").value("one week grace period").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperUserLifecycleAction.endDateOneWeek.actionType").value("addEndDateOnMembership").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperUserLifecycleAction.endDateOneWeek.description").value("If this action happens, the membership will be removed one week in the future if the admin does not approve it").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperUserLifecycleAction.endDateOneWeek.numberOfDaysInTheFuture").value("7").store();
    
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperUserLifecycleAction.removeUser.name").value("remove user").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperUserLifecycleAction.removeUser.actionType").value("removeUserFromGroup").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperUserLifecycleAction.removeUser.description").value("If this action happens, the membership will be removed immediately.").store();
    
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperUserLifecycleAction.emailServiceManager.name").value("email service manager").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperUserLifecycleAction.emailServiceManager.actionType").value("emailGroupAdmin").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperUserLifecycleAction.emailServiceManager.description").value("Let the service manager know that if they do not approve the membership, it will be removed after the grace period.").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperUserLifecycleAction.emailServiceManager.emailSubjectLine").value("Adobe users grace period approval").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperUserLifecycleAction.emailServiceManager.emailBody").value("""
$$ for (var recordMap : listOfRecordMaps) {
Adobe user with grace period: ${recordMap.get('safeSubjectLifecycleUser').getName()}
$$ }
        """).store();
    
    
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperUserLifecyclePolicy.high_security.name").value("high security").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperUserLifecyclePolicy.high_security.description").value("The high security policy removes users who leave the institution and assigns them one week grace period for users who leave the department.").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperUserLifecyclePolicy.high_security.isPublic").value("true").store();

    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperUserLifecyclePolicyPart.removeUserWhoLeavesInstitution.policy").value("high_security").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperUserLifecyclePolicyPart.removeUserWhoLeavesInstitution.numberOfLifecycleEvents").value("1").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperUserLifecyclePolicyPart.removeUserWhoLeavesInstitution.numberOfLifecycleActions").value("1").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperUserLifecyclePolicyPart.removeUserWhoLeavesInstitution.lifeCycleEvents.0.lifeCycleEventConfig").value("leaveInstitution").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperUserLifecyclePolicyPart.removeUserWhoLeavesInstitution.lifeCycleActions.0.lifeCycleActionConfig").value("removeUser").store();
    
    
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperUserLifecyclePolicyPart.gracePeriodWhenLeavesDepartment.policy").value("high_security").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperUserLifecyclePolicyPart.gracePeriodWhenLeavesDepartment.numberOfLifecycleEvents").value("1").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperUserLifecyclePolicyPart.gracePeriodWhenLeavesDepartment.numberOfLifecycleActions").value("2").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperUserLifecyclePolicyPart.gracePeriodWhenLeavesDepartment.lifeCycleEvents.0.lifeCycleEventConfig").value("leaveDepartment").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperUserLifecyclePolicyPart.gracePeriodWhenLeavesDepartment.lifeCycleActions.0.lifeCycleActionConfig").value("endDateOneWeek").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperUserLifecyclePolicyPart.gracePeriodWhenLeavesDepartment.lifeCycleActions.1.lifeCycleActionConfig").value("emailServiceManager").store();
    
    itDeptGroup.deleteMember(SubjectFinder.findById("test.subject.0", true)); // subject leaving the department. Add in flight attributes on adobeUsers + test.subject.0 membership
    institutionGroup.deleteMember(SubjectFinder.findById("test.subject.1", true)); // subject leaving the institution. Remove this member from adobeUsers as well
    
    
    //This will insert entries in the cache tables
    ChangeLogTempToEntity.convertRecords();
    
    UserLifecycleEngine.syncUserLifecycleEventConfigs(null); // this will make sure lifecycle_event_config table is populated 
    
    GrouperLifecycleEventConfig lifecycleEventConfig = UserLifecycleEventConfigDao.selectByText("leaveDepartment");
    UserLifecycleEventConfiguration.prepareAndStoreSqlCacheDependencies(lifecycleEventConfig); // this will insert entries in the dependency table
    
    GrouperLifecycleEventConfig lifecycleEventConfig1 = UserLifecycleEventConfigDao.selectByText("leaveInstitution");
    UserLifecycleEventConfiguration.prepareAndStoreSqlCacheDependencies(lifecycleEventConfig1); // this will insert entries in the dependency table
    
    // now run the OTHER_JOB_sqlCacheHistoryFullSync to insert entries into the grouper_sql_cache_mship_hst table
    OtherJobInput otherJobInput = new OtherJobInput();
    Hib3GrouperLoaderLog hib3GrouperLoaderLog = new Hib3GrouperLoaderLog();       
    otherJobInput.setJobName("OTHER_JOB_sqlCacheHistoryFullSync");
    otherJobInput.setHib3GrouperLoaderLog(hib3GrouperLoaderLog);
    new SqlCacheHistoryFullSyncDaemon().run(otherJobInput);
    
    GrouperUtil.sleep(2000L);
    
    UserLifecycleService.savePolicyConfigOnGroup(adobeUsers, "high_security", GrouperSession.staticGrouperSession().internal_getRootSession().getSubject()); 
    
    GrouperUtil.sleep(2000L);
    
    // now run the UserLifecycleFullDaemon to add an entry in the grouper_lifecycle_event table
    OtherJobInput otherJobInput1 = new OtherJobInput();
    Hib3GrouperLoaderLog hib3GrouperLoaderLog1 = new Hib3GrouperLoaderLog();
    otherJobInput1.setJobName("OTHER_JOB_userLifecycleFullDaemon");
    otherJobInput1.setHib3GrouperLoaderLog(hib3GrouperLoaderLog1);
    new UserLifecycleFullDaemon().run(otherJobInput1);
    
    GrouperUtil.sleep(2000L);
    
    List<Long> lifecycleInternalIds = new GcDbAccess().sql("select internal_id from grouper_lifecycle_event").selectList(Long.class);    
    assertEquals(2, lifecycleInternalIds.size());
    
    OtherJobInput otherJobInput2 = new OtherJobInput();
    Hib3GrouperLoaderLog hib3GrouperLoaderLog2 = new Hib3GrouperLoaderLog();
    otherJobInput2.setJobName("OTHER_JOB_groupPolicyUserLifecycleFullDaemon");
    otherJobInput2.setHib3GrouperLoaderLog(hib3GrouperLoaderLog2);
    new GroupPolicyUserLifecycleFullDaemon().run(otherJobInput2);
    
    GcDbAccess gcDbAccess = new GcDbAccess();
    
    StringBuilder sqlBuilder = new StringBuilder("select count(gaaamv.attribute_def_name_name2) from grouper_aval_asn_asn_mship_v gaaamv where gaaamv.attribute_def_name_name1 = ? "
        + " and group_name = ? and subject_id = ? ");
    gcDbAccess.addBindVar(UserLifecycleAttributeNames.userLifecycleStemName() +":"+ UserLifecycleAttributeNames.USER_LIFECYCLE_MSHIP_IN_FLIGHT_MARKER);
    gcDbAccess.addBindVar(adobeUsers.getName());
    gcDbAccess.addBindVar("test.subject.0");
    
    Long countOfRow = gcDbAccess.sql(sqlBuilder.toString()).select(Long.class);
    assertEquals(3, countOfRow.intValue());
    
    Set<Member> immediateMembers = adobeUsers.getImmediateMembers();
    assertEquals(1, immediateMembers.size());
    assertEquals("my name is test.subject.0", immediateMembers.iterator().next().getName());
    
  }

}
