package edu.internet2.middleware.grouper.userLifecycle;

import java.util.List;
import java.util.Set;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.app.loader.OtherJobBase.OtherJobInput;
import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;
import edu.internet2.middleware.grouper.cfg.dbConfig.GrouperDbConfig;
import edu.internet2.middleware.grouper.changeLog.ChangeLogTempToEntity;
import edu.internet2.middleware.grouper.dataField.GrouperDataField;
import edu.internet2.middleware.grouper.dataField.GrouperDataFieldAssignHst;
import edu.internet2.middleware.grouper.dataField.GrouperDataFieldAssignHstDao;
import edu.internet2.middleware.grouper.dataField.GrouperDataFieldDao;
import edu.internet2.middleware.grouper.dataField.GrouperDataRow;
import edu.internet2.middleware.grouper.dataField.GrouperDataRowAssignHst;
import edu.internet2.middleware.grouper.dataField.GrouperDataRowAssignHstDao;
import edu.internet2.middleware.grouper.dataField.GrouperDataRowDao;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.sqlCache.SqlCacheHistoryFullSyncDaemon;
import edu.internet2.middleware.grouper.tableIndex.TableIndex;
import edu.internet2.middleware.grouper.tableIndex.TableIndexType;
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

  /**
   * End-to-end test of the dataFieldRemove trigger: configures an integer data field,
   * inserts a history row whose end_time is more than a year ago (matching the daemon's
   * window), runs UserLifecycleFullDaemon, then verifies that a grouper_lifecycle_event
   * row exists whose privileged dictionary entry holds the rendered (interpolated)
   * template text — not the raw template string.
   */
  public void testDataFieldRemove() {

    // 1. Configure the data field (integer type, attribute structure).
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperPrivacyRealm.public.privacyRealmName").value("public").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperPrivacyRealm.public.privacyRealmPublic").value("true").store();

    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.position.fieldAliases").value("position").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.position.fieldDataType").value("integer").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.position.fieldDataStorePit").value("true").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.position.fieldPrivacyRealm").value("public").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.position.descriptionHtml").value("position field").store();

    // 2. Materialize a row in grouper_data_field for that config.
    GrouperDataFieldDao.insertMissingConfigIds(GrouperUtil.toSet("position"));
    GrouperDataField dataField = GrouperDataFieldDao.selectByText("position");
    assertNotNull(dataField);

    // 3. Resolve a member to attach the history row to.
    Member member = MemberFinder.findBySubject(GrouperSession.staticGrouperSession(),
        SubjectFinder.findById("test.subject.0", true), true);

    // 4. Insert a single hst row whose end_time matches the daemon's "older than a year" window.
    long endTimeMicros = (System.currentTimeMillis() - 400L * 24 * 60 * 60 * 1000) * 1000L;
    GrouperDataFieldAssignHst hst = new GrouperDataFieldAssignHst();
    hst.setInternalId(TableIndex.reserveId(TableIndexType.dataFieldAssignHst));
    hst.setDataFieldInternalId(dataField.getInternalId());
    hst.setMemberInternalId(member.getInternalId());
    hst.setValueInteger(42L);
    hst.setStartTime(endTimeMicros - 1000L);
    hst.setEndTime(endTimeMicros);
    GrouperDataFieldAssignHstDao.store(hst);

    // 5. Configure the lifecycle event with templates that interpolate ${configId} and ${value}.
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperUserLifecycleEvent.removePosition.name").value("remove position").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperUserLifecycleEvent.removePosition.description").value("position removed").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperUserLifecycleEvent.removePosition.trigger").value("dataFieldRemove").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperUserLifecycleEvent.removePosition.groupUserRemoveDataFieldConfigId").value("position").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperUserLifecycleEvent.removePosition.naturalLanguageDescriptionJexlPrivileged").value("Lost ${configId} (value: ${value})").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperUserLifecycleEvent.removePosition.naturalLanguageDescriptionJexlUnprivileged").value("Lost a data field").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperUserLifecycleEvent.removePosition.naturalLanguageDescriptionJexlPrivilegedGroupIdOrName").value("etc:sysadmingroup").store();

    UserLifecycleEngine.syncUserLifecycleEventConfigs(null);

    // 6. Run the daemon.
    OtherJobInput otherJobInput = new OtherJobInput();
    Hib3GrouperLoaderLog log = new Hib3GrouperLoaderLog();
    otherJobInput.setJobName("OTHER_JOB_userLifecycleFullDaemon");
    otherJobInput.setHib3GrouperLoaderLog(log);
    new UserLifecycleFullDaemon().run(otherJobInput);

    // 7. Verify the lifecycle event exists and points at a dictionary entry holding the rendered text.
    String privilegedText = new GcDbAccess().sql(
        "select gd.the_text from grouper_lifecycle_event gle join grouper_dictionary gd "
            + "on gd.internal_id = gle.ntrl_lng_priv_dic_intrnl_id "
            + "where gle.member_internal_id = ?").addBindVar(member.getInternalId()).select(String.class);
    assertEquals("Lost position (value: 42)", privilegedText);

    String unprivilegedText = new GcDbAccess().sql(
        "select gd.the_text from grouper_lifecycle_event gle join grouper_dictionary gd "
            + "on gd.internal_id = gle.ntrl_lng_unpriv_dic_intrnl_id "
            + "where gle.member_internal_id = ?").addBindVar(member.getInternalId()).select(String.class);
    assertEquals("Lost a data field", unprivilegedText);
  }

  /**
   * End-to-end test of the dataRowRemove trigger. Same shape as testDataFieldRemove but
   * exercises grouper_data_row_assign_hst and the dataRow trigger branch of the daemon.
   */
  public void testDataRowRemove() {

    // 1. Configure the data row.
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperPrivacyRealm.public.privacyRealmName").value("public").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperPrivacyRealm.public.privacyRealmPublic").value("true").store();

    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataRow.pursual.rowAliases").value("pursual").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataRow.pursual.rowPrivacyRealm").value("public").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataRow.pursual.rowNumberOfDataFields").value("0").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataRow.pursual.rowDataStorePit").value("true").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataRow.pursual.descriptionHtml").value("pursual row").store();

    // 2. Materialize a row in grouper_data_row for that config.
    GrouperDataRowDao.insertMissingConfigIds(GrouperUtil.toSet("pursual"));
    GrouperDataRow dataRow = GrouperDataRowDao.selectByConfigId("pursual");
    assertNotNull(dataRow);

    // 3. Resolve a member.
    Member member = MemberFinder.findBySubject(GrouperSession.staticGrouperSession(),
        SubjectFinder.findById("test.subject.0", true), true);

    // 4. Insert a single row-assign hst record older than the daemon's window.
    long endTimeMicros = (System.currentTimeMillis() - 400L * 24 * 60 * 60 * 1000) * 1000L;
    GrouperDataRowAssignHst hst = new GrouperDataRowAssignHst();
    hst.setInternalId(TableIndex.reserveId(TableIndexType.dataRowAssignHst));
    hst.setDataRowInternalId(dataRow.getInternalId());
    hst.setDataRowAssignInternalId(TableIndex.reserveId(TableIndexType.dataRowAssign));
    hst.setMemberInternalId(member.getInternalId());
    hst.setStartTime(endTimeMicros - 1000L);
    hst.setEndTime(endTimeMicros);
    GrouperDataRowAssignHstDao.store(hst);

    // 5. Configure the lifecycle event with a template that interpolates ${configId}.
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperUserLifecycleEvent.removePursual.name").value("remove pursual").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperUserLifecycleEvent.removePursual.description").value("pursual removed").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperUserLifecycleEvent.removePursual.trigger").value("dataRowRemove").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperUserLifecycleEvent.removePursual.groupUserRemoveDataRowConfigId").value("pursual").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperUserLifecycleEvent.removePursual.naturalLanguageDescriptionJexlPrivileged").value("Lost ${configId} row").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperUserLifecycleEvent.removePursual.naturalLanguageDescriptionJexlUnprivileged").value("Lost a data row").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperUserLifecycleEvent.removePursual.naturalLanguageDescriptionJexlPrivilegedGroupIdOrName").value("etc:sysadmingroup").store();

    UserLifecycleEngine.syncUserLifecycleEventConfigs(null);

    // 6. Run the daemon.
    OtherJobInput otherJobInput = new OtherJobInput();
    Hib3GrouperLoaderLog log = new Hib3GrouperLoaderLog();
    otherJobInput.setJobName("OTHER_JOB_userLifecycleFullDaemon");
    otherJobInput.setHib3GrouperLoaderLog(log);
    new UserLifecycleFullDaemon().run(otherJobInput);

    // 7. Verify the rendered text reached the dictionary.
    String privilegedText = new GcDbAccess().sql(
        "select gd.the_text from grouper_lifecycle_event gle join grouper_dictionary gd "
            + "on gd.internal_id = gle.ntrl_lng_priv_dic_intrnl_id "
            + "where gle.member_internal_id = ?").addBindVar(member.getInternalId()).select(String.class);
    assertEquals("Lost pursual row", privilegedText);

    String unprivilegedText = new GcDbAccess().sql(
        "select gd.the_text from grouper_lifecycle_event gle join grouper_dictionary gd "
            + "on gd.internal_id = gle.ntrl_lng_unpriv_dic_intrnl_id "
            + "where gle.member_internal_id = ?").addBindVar(member.getInternalId()).select(String.class);
    assertEquals("Lost a data row", unprivilegedText);
  }

}
