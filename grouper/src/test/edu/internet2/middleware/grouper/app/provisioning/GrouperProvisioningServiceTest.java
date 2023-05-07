package edu.internet2.middleware.grouper.app.provisioning;

import static edu.internet2.middleware.grouper.app.grouperTypes.GrouperObjectTypesAttributeNames.GROUPER_OBJECT_TYPE_DIRECT_ASSIGNMENT;
import static edu.internet2.middleware.grouper.app.grouperTypes.GrouperObjectTypesAttributeNames.GROUPER_OBJECT_TYPE_NAME;
import static edu.internet2.middleware.grouper.app.grouperTypes.GrouperObjectTypesSettings.objectTypesStemName;
import static edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningAttributeNames.PROVISIONING_DIRECT_ASSIGNMENT;
import static edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningAttributeNames.PROVISIONING_STEM_SCOPE;
import static edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningAttributeNames.PROVISIONING_TARGET;
import static edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningAttributeNames.retrieveAttributeDefNameBase;
import static edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningSettings.provisioningConfigStemName;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.app.grouperTypes.GrouperObjectTypesAttributeNames;
import edu.internet2.middleware.grouper.app.ldapProvisioning.LdapSync;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.changeLog.esb.consumer.EsbConsumer;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.misc.GrouperCheckConfig;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.session.GrouperSessionResult;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSync;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncDao;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncGroup;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncJob;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncJobState;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncLog;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncMember;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncMembership;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.lang3.BooleanUtils;
import junit.textui.TestRunner;

/**
 * TODO refactor tests for 2.6.9 configs
 * @author mchyzer
 *
 */
public class GrouperProvisioningServiceTest extends GrouperProvisioningBaseTest {
  
  public GrouperProvisioningServiceTest(String name) {
    super(name);
  }
  
  @Override
  public String defaultConfigId() {
    return "junitProvisioningAttributePropagationTest";
  }

  public static void main(String[] args) {
    TestRunner.run(new GrouperProvisioningServiceTest("testSaveOrUpdateProvisioningAttributes"));
  }
  
  @Override
  protected void setUp() {
    super.setUp();
    GrouperCheckConfig.checkGroups();
    GrouperCheckConfig.waitUntilDoneWithExtraConfig();
    
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("provisioningInUi.enable", "true");
    
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldap.class", LdapSync.class.getName());
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.box.class", "BoxProvisioner");
    
  }
  
  public void testRetrieveGcGrouperGroup() {
    
    //Given
    GcGrouperSync gcGrouperSync = new GcGrouperSync();
    gcGrouperSync.setSyncEngine("temp");
    gcGrouperSync.setProvisionerName("myJob");
    gcGrouperSync.setLastUpdated(new Timestamp(System.currentTimeMillis()));
    gcGrouperSync.setLastFullSyncRun(new Timestamp(System.currentTimeMillis() - 100000));
    gcGrouperSync.setLastIncrementalSyncRun(new Timestamp(System.currentTimeMillis() - 700000));
    gcGrouperSync.getGcGrouperSyncDao().store();
    
    GcGrouperSyncGroup gcGrouperSyncGroup = gcGrouperSync.getGcGrouperSyncGroupDao().groupRetrieveOrCreateByGroupId("myId");
    gcGrouperSyncGroup.setLastTimeWorkWasDone(new Timestamp(System.currentTimeMillis() + 2000));
    gcGrouperSync.getGcGrouperSyncGroupDao().internal_groupStore(gcGrouperSyncGroup);
    
    //When
    GcGrouperSyncGroup grouperSyncGroup = GrouperProvisioningService.retrieveGcGrouperGroup("myId", "myJob");
    
    //Then
    assertNotNull(grouperSyncGroup);
    
  }
  
  public void testRetrieveGcGrouperSyncLogs() {
    
    //Given
    GrouperSession.startRootSessionIfNotStarted();
    
    GcGrouperSync gcGrouperSync = new GcGrouperSync();
    gcGrouperSync.setSyncEngine("temp");
    gcGrouperSync.setProvisionerName("myJob");
    gcGrouperSync.setUserCount(10);
    gcGrouperSync.setGroupCount(20);
    gcGrouperSync.setRecordsCount(30);
    gcGrouperSync.setLastUpdated(new Timestamp(System.currentTimeMillis()));
    gcGrouperSync.setLastFullSyncRun(new Timestamp(System.currentTimeMillis() - 100000));
    gcGrouperSync.setLastIncrementalSyncRun(new Timestamp(System.currentTimeMillis() - 700000));
    gcGrouperSync.getGcGrouperSyncDao().store();

    for (int i=0; i<=200; i++) {

      GcGrouperSyncJob gcGrouperSyncJob = new GcGrouperSyncJob();
      gcGrouperSyncJob.setGrouperSync(gcGrouperSync);
      gcGrouperSyncJob.setJobState(GcGrouperSyncJobState.notRunning);
      gcGrouperSyncJob.setLastSyncIndex(135L);
      gcGrouperSyncJob.setLastTimeWorkWasDone(new Timestamp(System.currentTimeMillis() + 2000));
      gcGrouperSyncJob.setSyncType("testSyncType"+String.valueOf(i));
      gcGrouperSync.getGcGrouperSyncJobDao().internal_jobStore(gcGrouperSyncJob);

      GcGrouperSyncGroup gcGrouperSyncGroup = gcGrouperSync.getGcGrouperSyncGroupDao().groupRetrieveOrCreateByGroupId("myId");
      gcGrouperSyncGroup.setLastTimeWorkWasDone(new Timestamp(System.currentTimeMillis() + 2000));
      gcGrouperSync.getGcGrouperSyncGroupDao().internal_groupStore(gcGrouperSyncGroup);

      GcGrouperSyncMember gcGrouperSyncMember = new GcGrouperSyncMember();
      gcGrouperSyncMember.setGrouperSync(gcGrouperSync);
      gcGrouperSyncMember.setLastTimeWorkWasDone(new Timestamp(System.currentTimeMillis() + 2000));
      gcGrouperSyncMember.setMemberId("memId"+String.valueOf(i));
      gcGrouperSyncMember.setSourceId("sourceId");
      gcGrouperSyncMember.setSubjectId("subjectId");
      gcGrouperSyncMember.setSubjectIdentifier("subjectIdentifier");
      gcGrouperSyncMember.setInTargetDb("T");
      gcGrouperSyncMember.setProvisionableDb("T");
      gcGrouperSyncMember.setProvisionableEnd(new Timestamp(456L));
      gcGrouperSyncMember.setProvisionableStart(new Timestamp(567L));
      gcGrouperSync.getGcGrouperSyncMemberDao().internal_memberStore(gcGrouperSyncMember);


      GcGrouperSyncLog gcGrouperSyncLog = new GcGrouperSyncLog();
      gcGrouperSyncLog.setDescriptionToSave("desc");
      gcGrouperSyncLog.setGrouperSync(gcGrouperSync);

      gcGrouperSyncLog.setJobTookMillis(1223 +  i);
      gcGrouperSyncLog.setRecordsChanged(12 + i);
      gcGrouperSyncLog.setRecordsProcessed(23 + i);

      if (i % 3 == 0) {
        gcGrouperSyncLog.setGrouperSyncOwnerId(gcGrouperSyncMember.getId());
      } else if (i % 2 == 0) {
        gcGrouperSyncLog.setGrouperSyncOwnerId(gcGrouperSyncGroup.getId());
      } else if (i % 2 == 1) {
        gcGrouperSyncLog.setGrouperSyncOwnerId(gcGrouperSyncJob.getId());
      } else {
        System.out.println("Should never go here. i = "+i);
      }

      gcGrouperSync.getGcGrouperSyncLogDao().internal_logStore(gcGrouperSyncLog);
    }

    //When
    List<GrouperSyncLogWithOwner> gcGrouperSyncLogs = GrouperProvisioningService.retrieveGcGrouperSyncLogs("myJob", new QueryOptions());

    //Then
    assertEquals(100, gcGrouperSyncLogs.size());
    
    Set<String> logTypes = new HashSet<String>();
    logTypes.add("Job");
    logTypes.add("Group");
    logTypes.add("Member");
    
    for (GrouperSyncLogWithOwner grouperSyncLogWithOwner: gcGrouperSyncLogs) {
      String logType = grouperSyncLogWithOwner.getLogType();
      assertTrue(logTypes.contains(logType));
      
      if (logType.equals("Job")) {
        assertNotNull(grouperSyncLogWithOwner.getGcGrouperSyncJob());
      }
      
      if (logType.equals("Group")) {
        assertNotNull(grouperSyncLogWithOwner.getGcGrouperSyncGroup());
      }
      
      if (logType.equals("Member")) {
        assertNotNull(grouperSyncLogWithOwner.getGcGrouperSyncMember());
      }
      
    }
    
  }
  
  public void testRetrieveNumberOfGroupsInTargetInStem() {
    
    //Given
    GrouperSessionResult grouperSessionResult = GrouperSession.startRootSessionIfNotStarted();
    GrouperSession grouperSession = grouperSessionResult.getGrouperSession();
    
    Stem stem0 = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test").save();
    Group group0 = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:test1").save();
    
    GcGrouperSync gcGrouperSync = new GcGrouperSync();
    gcGrouperSync.setSyncEngine("temp");
    gcGrouperSync.setProvisionerName("myJob");
    gcGrouperSync.setUserCount(10);
    gcGrouperSync.setGroupCount(20);
    gcGrouperSync.setRecordsCount(30);
    gcGrouperSync.setLastUpdated(new Timestamp(System.currentTimeMillis()));
    gcGrouperSync.setLastFullSyncRun(new Timestamp(System.currentTimeMillis() - 100000));
    gcGrouperSync.setLastIncrementalSyncRun(new Timestamp(System.currentTimeMillis() - 700000));
    gcGrouperSync.getGcGrouperSyncDao().store();
    
    GcGrouperSyncJob gcGrouperSyncJob = new GcGrouperSyncJob();
    gcGrouperSyncJob.setGrouperSync(gcGrouperSync);
    gcGrouperSyncJob.setJobState(GcGrouperSyncJobState.notRunning);
    gcGrouperSyncJob.setLastSyncIndex(135L);
    gcGrouperSyncJob.setLastTimeWorkWasDone(new Timestamp(System.currentTimeMillis() + 2000));
    gcGrouperSyncJob.setSyncType("testSyncType");
    gcGrouperSync.getGcGrouperSyncJobDao().internal_jobStore(gcGrouperSyncJob);
    
    GcGrouperSyncGroup gcGrouperSyncGroup = gcGrouperSync.getGcGrouperSyncGroupDao().groupRetrieveOrCreateByGroupId(group0.getId());
    gcGrouperSyncGroup.setLastTimeWorkWasDone(new Timestamp(System.currentTimeMillis() + 2000));
    gcGrouperSyncGroup.setProvisionable(true);
    gcGrouperSync.getGcGrouperSyncGroupDao().internal_groupStore(gcGrouperSyncGroup);
    
    //When
    long groupsInTargetInStem = GrouperProvisioningService.retrieveNumberOfGroupsInTargetInStem(stem0.getId(), "myJob");

    //Then
    assertEquals(1, groupsInTargetInStem);
    
  }
  
  
  //Chris, please review this extra carefully :)
  public void testRetrieveNumberOfGroupsInTargetInMember() {
    
    //Given
    GrouperSessionResult grouperSessionResult = GrouperSession.startRootSessionIfNotStarted();
    GrouperSession grouperSession = grouperSessionResult.getGrouperSession();
    
    Stem root = StemFinder.findRootStem(grouperSession);
    Stem top = root.addChildStem("top", "top");
    Group one = top.addChildGroup("one", "one");
    Group two = top.addChildGroup("two", "two");
    
    // now add two -> one as a disabled membership
    Membership ms = new Membership();
    ms.setCreatorUuid(grouperSession.getMemberUuid());
    ms.setFieldId(Group.getDefaultList().getUuid());
    ms.setMemberUuid(two.toMember().getUuid());
    ms.setOwnerGroupId(one.getUuid());
    ms.setMember(two.toMember());
    ms.setEnabled(true);
    ms.setEnabledTime(new Timestamp(System.currentTimeMillis() - 2000));
    GrouperDAOFactory.getFactory().getMembership().save(ms);
    
    // foreign key
    GcGrouperSync gcGrouperSync = new GcGrouperSync();
    gcGrouperSync.setSyncEngine("temp");
    gcGrouperSync.setProvisionerName("myJob-3");
    gcGrouperSync.getGcGrouperSyncDao().store();
    
    GcGrouperSyncGroup gcGrouperSyncGroup = gcGrouperSync.getGcGrouperSyncGroupDao().groupRetrieveOrCreateByGroupId("myId");
    gcGrouperSyncGroup.setLastTimeWorkWasDone(new Timestamp(System.currentTimeMillis() + 2000));
    gcGrouperSync.getGcGrouperSyncGroupDao().internal_groupStore(gcGrouperSyncGroup);

    GcGrouperSyncMember gcGrouperSyncMember = new GcGrouperSyncMember();
    gcGrouperSyncMember.setGrouperSync(gcGrouperSync);
    gcGrouperSyncMember.setProvisionable(true);
    gcGrouperSyncMember.setLastTimeWorkWasDone(new Timestamp(System.currentTimeMillis() + 2000));
    gcGrouperSyncMember.setMemberId(two.toMember().getUuid());
    gcGrouperSync.getGcGrouperSyncMemberDao().internal_memberStore(gcGrouperSyncMember);

    GcGrouperSyncMembership gcGrouperSyncMembership = new GcGrouperSyncMembership();
    gcGrouperSyncMembership.setGrouperSyncGroup(gcGrouperSyncGroup);
    gcGrouperSyncMembership.setGrouperSyncMember(gcGrouperSyncMember);
    gcGrouperSyncMembership.setGrouperSync(gcGrouperSync);
    gcGrouperSyncMembership.setMembershipId("memId");
    gcGrouperSync.getGcGrouperSyncMembershipDao().internal_membershipStore(gcGrouperSyncMembership);
    
    //When
    long groupsInTargetInMember = GrouperProvisioningService.retrieveNumberOfGroupsInTargetInMember(two.toMember().getUuid(), "myJob-3");
    
    //Then
    assertEquals(1, groupsInTargetInMember);
  }
  
  //Chris, please review this extra carefully :)
  public void testRetrieveNumberOfUsersInTargetInGroup() {
    
    //Given
    GrouperSessionResult grouperSessionResult = GrouperSession.startRootSessionIfNotStarted();
    GrouperSession grouperSession = grouperSessionResult.getGrouperSession();
    
    Group group0 = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:test1").save();
    
    Stem root = StemFinder.findRootStem(grouperSession);
    Stem top = root.addChildStem("top", "top");
    Group one = top.addChildGroup("one", "one");
    Group two = top.addChildGroup("two", "two");
    
    // now add two -> one as a disabled membership
    Membership ms = new Membership();
    ms.setCreatorUuid(grouperSession.getMemberUuid());
    ms.setFieldId(Group.getDefaultList().getUuid());
    ms.setMemberUuid(two.toMember().getUuid());
    ms.setOwnerGroupId(one.getUuid());
    ms.setMember(two.toMember());
    ms.setEnabled(true);
    ms.setEnabledTime(new Timestamp(System.currentTimeMillis() - 2000));
    GrouperDAOFactory.getFactory().getMembership().save(ms);
    
    // foreign key
    GcGrouperSync gcGrouperSync = new GcGrouperSync();
    gcGrouperSync.setSyncEngine("temp");
    gcGrouperSync.setProvisionerName("myJob-3");
    gcGrouperSync.getGcGrouperSyncDao().store();
    
    GcGrouperSyncGroup gcGrouperSyncGroup = gcGrouperSync.getGcGrouperSyncGroupDao().groupRetrieveOrCreateByGroupId(group0.getId());
    gcGrouperSyncGroup.setLastTimeWorkWasDone(new Timestamp(System.currentTimeMillis() + 2000));
    gcGrouperSyncGroup.setProvisionable(true);
    gcGrouperSync.getGcGrouperSyncGroupDao().internal_groupStore(gcGrouperSyncGroup);

    GcGrouperSyncMember gcGrouperSyncMember = new GcGrouperSyncMember();
    gcGrouperSyncMember.setGrouperSync(gcGrouperSync);
    gcGrouperSyncMember.setProvisionable(true);
    gcGrouperSyncMember.setLastTimeWorkWasDone(new Timestamp(System.currentTimeMillis() + 2000));
    gcGrouperSyncMember.setMemberId(two.toMember().getUuid());
    gcGrouperSync.getGcGrouperSyncMemberDao().internal_memberStore(gcGrouperSyncMember);

    GcGrouperSyncMembership gcGrouperSyncMembership = new GcGrouperSyncMembership();
    gcGrouperSyncMembership.setGrouperSyncGroup(gcGrouperSyncGroup);
    gcGrouperSyncMembership.setGrouperSyncMember(gcGrouperSyncMember);
    gcGrouperSyncMembership.setGrouperSync(gcGrouperSync);
    gcGrouperSyncMembership.setMembershipId("memId");
    gcGrouperSync.getGcGrouperSyncMembershipDao().internal_membershipStore(gcGrouperSyncMembership);
  
    //When
    long usersInTargetInGroup = GrouperProvisioningService.retrieveNumberOfUsersInTargetInGroup(group0.getId(), "myJob-3");
    
    //Then
    assertEquals(1, usersInTargetInGroup);
  }
  
  public void testRetrieveNumberOfMembershipsInTargetInStem() {
    
    //Given
    GrouperSessionResult grouperSessionResult = GrouperSession.startRootSessionIfNotStarted();
    GrouperSession grouperSession = grouperSessionResult.getGrouperSession();
    
    Stem stem0 = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test").save();
    Group group0 = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:test1").save();
    
    Stem root = StemFinder.findRootStem(grouperSession);
    Stem top = root.addChildStem("top", "top");
    Group one = top.addChildGroup("one", "one");
    Group two = top.addChildGroup("two", "two");
    
    // now add two -> one as a disabled membership
    Membership ms = new Membership();
    ms.setCreatorUuid(grouperSession.getMemberUuid());
    ms.setFieldId(Group.getDefaultList().getUuid());
    ms.setMemberUuid(two.toMember().getUuid());
    ms.setOwnerGroupId(one.getUuid());
    ms.setMember(two.toMember());
    ms.setEnabled(true);
    ms.setEnabledTime(new Timestamp(System.currentTimeMillis() - 2000));
    GrouperDAOFactory.getFactory().getMembership().save(ms);
    
    // foreign key
    GcGrouperSync gcGrouperSync = new GcGrouperSync();
    gcGrouperSync.setSyncEngine("temp");
    gcGrouperSync.setProvisionerName("myJob-3");
    gcGrouperSync.getGcGrouperSyncDao().store();
    
    GcGrouperSyncGroup gcGrouperSyncGroup = gcGrouperSync.getGcGrouperSyncGroupDao().groupRetrieveOrCreateByGroupId(group0.getId());
    gcGrouperSyncGroup.setLastTimeWorkWasDone(new Timestamp(System.currentTimeMillis() + 2000));
    gcGrouperSyncGroup.setProvisionable(true);
    gcGrouperSync.getGcGrouperSyncGroupDao().internal_groupStore(gcGrouperSyncGroup);

    GcGrouperSyncMember gcGrouperSyncMember = new GcGrouperSyncMember();
    gcGrouperSyncMember.setGrouperSync(gcGrouperSync);
    gcGrouperSyncMember.setProvisionable(true);
    gcGrouperSyncMember.setLastTimeWorkWasDone(new Timestamp(System.currentTimeMillis() + 2000));
    gcGrouperSyncMember.setMemberId(two.toMember().getUuid());
    gcGrouperSync.getGcGrouperSyncMemberDao().internal_memberStore(gcGrouperSyncMember);

    GcGrouperSyncMembership gcGrouperSyncMembership = new GcGrouperSyncMembership();
    gcGrouperSyncMembership.setGrouperSyncGroup(gcGrouperSyncGroup);
    gcGrouperSyncMembership.setGrouperSyncMember(gcGrouperSyncMember);
    gcGrouperSyncMembership.setGrouperSync(gcGrouperSync);
    gcGrouperSyncMembership.setMembershipId("memId");
    gcGrouperSync.getGcGrouperSyncMembershipDao().internal_membershipStore(gcGrouperSyncMembership);
    
    //When
    long membershipsInTargetInStem = GrouperProvisioningService.retrieveNumberOfMembershipsInTargetInStem(stem0.getId(), "myJob-3");
    
    //Then
    assertEquals(1, membershipsInTargetInStem);
  }
  
  public void testRetrieveNumberOfUsersInTargetInStem() {
    //Given
    GrouperSessionResult grouperSessionResult = GrouperSession.startRootSessionIfNotStarted();
    GrouperSession grouperSession = grouperSessionResult.getGrouperSession();
    
    Stem stem0 = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test").save();
    Group group0 = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:test1").save();
    
    Stem root = StemFinder.findRootStem(grouperSession);
    Stem top = root.addChildStem("top", "top");
    Group one = top.addChildGroup("one", "one");
    Group two = top.addChildGroup("two", "two");
    
    // now add two -> one as a disabled membership
    Membership ms = new Membership();
    ms.setCreatorUuid(grouperSession.getMemberUuid());
    ms.setFieldId(Group.getDefaultList().getUuid());
    ms.setMemberUuid(two.toMember().getUuid());
    ms.setOwnerGroupId(one.getUuid());
    ms.setMember(two.toMember());
    ms.setEnabled(true);
    ms.setEnabledTime(new Timestamp(System.currentTimeMillis() - 2000));
    GrouperDAOFactory.getFactory().getMembership().save(ms);
    
    // foreign key
    GcGrouperSync gcGrouperSync = new GcGrouperSync();
    gcGrouperSync.setSyncEngine("temp");
    gcGrouperSync.setProvisionerName("myJob-3");
    gcGrouperSync.getGcGrouperSyncDao().store();
    
    GcGrouperSyncGroup gcGrouperSyncGroup = gcGrouperSync.getGcGrouperSyncGroupDao().groupRetrieveOrCreateByGroupId(group0.getId());
    gcGrouperSyncGroup.setLastTimeWorkWasDone(new Timestamp(System.currentTimeMillis() + 2000));
    gcGrouperSyncGroup.setProvisionable(true);
    gcGrouperSync.getGcGrouperSyncGroupDao().internal_groupStore(gcGrouperSyncGroup);

    GcGrouperSyncMember gcGrouperSyncMember = new GcGrouperSyncMember();
    gcGrouperSyncMember.setGrouperSync(gcGrouperSync);
    gcGrouperSyncMember.setProvisionable(true);
    gcGrouperSyncMember.setLastTimeWorkWasDone(new Timestamp(System.currentTimeMillis() + 2000));
    gcGrouperSyncMember.setMemberId(two.toMember().getUuid());
    gcGrouperSync.getGcGrouperSyncMemberDao().internal_memberStore(gcGrouperSyncMember);

    GcGrouperSyncMembership gcGrouperSyncMembership = new GcGrouperSyncMembership();
    gcGrouperSyncMembership.setGrouperSyncGroup(gcGrouperSyncGroup);
    gcGrouperSyncMembership.setGrouperSyncMember(gcGrouperSyncMember);
    gcGrouperSyncMembership.setGrouperSync(gcGrouperSync);
    gcGrouperSyncMembership.setMembershipId("memId");
    gcGrouperSync.getGcGrouperSyncMembershipDao().internal_membershipStore(gcGrouperSyncMembership);
    
    //When
    long usersInTargetInStem = GrouperProvisioningService.retrieveNumberOfUsersInTargetInStem(stem0.getId(), "myJob-3");
    
    //Then
    assertEquals(1, usersInTargetInStem);
    
  }
  
  //Chris, please review this extra carefully :)
  public void testRetrieveGcGrouperSyncMembers() {
    
    //Given
    GrouperSessionResult grouperSessionResult = GrouperSession.startRootSessionIfNotStarted();
    GrouperSession grouperSession = grouperSessionResult.getGrouperSession();
    
    Group group0 = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:test1").save();
    
    Stem root = StemFinder.findRootStem(grouperSession);
    Stem top = root.addChildStem("top", "top");
    Group two = top.addChildGroup("two", "two");
    
    // foreign key
    GcGrouperSync gcGrouperSync = new GcGrouperSync();
    gcGrouperSync.setSyncEngine("temp");
    gcGrouperSync.setProvisionerName("myJob-3");
    gcGrouperSync.getGcGrouperSyncDao().store();
    
    GcGrouperSyncGroup gcGrouperSyncGroup = gcGrouperSync.getGcGrouperSyncGroupDao().groupRetrieveOrCreateByGroupId(group0.getId());
    gcGrouperSyncGroup.setLastTimeWorkWasDone(new Timestamp(System.currentTimeMillis() + 2000));
    gcGrouperSyncGroup.setProvisionable(true);
    gcGrouperSync.getGcGrouperSyncGroupDao().internal_groupStore(gcGrouperSyncGroup);

    GcGrouperSyncMember gcGrouperSyncMember = new GcGrouperSyncMember();
    gcGrouperSyncMember.setGrouperSync(gcGrouperSync);
    gcGrouperSyncMember.setProvisionable(true);
    gcGrouperSyncMember.setLastTimeWorkWasDone(new Timestamp(System.currentTimeMillis() + 2000));
    gcGrouperSyncMember.setMemberId(two.toMember().getUuid());
    gcGrouperSync.getGcGrouperSyncMemberDao().internal_memberStore(gcGrouperSyncMember);
    
    //When
    List<GcGrouperSyncMember> grouperSyncMembers = GrouperProvisioningService.retrieveGcGrouperSyncMembers(two.toMember().getUuid());
    
    //Then
    assertEquals(1, grouperSyncMembers.size());
    assertEquals(gcGrouperSyncMember.getId(), grouperSyncMembers.get(0).getId());
    assertEquals(gcGrouperSync.getId(), grouperSyncMembers.get(0).getGrouperSync().getId());
    
  }
  
  //Chris, please review this extra carefully :)
  public void testRetrieveGcGrouperSyncMemberships() {
    
    //Given
    GrouperSessionResult grouperSessionResult = GrouperSession.startRootSessionIfNotStarted();
    GrouperSession grouperSession = grouperSessionResult.getGrouperSession();
    
    Group group0 = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:test1").save();
    
    Stem root = StemFinder.findRootStem(grouperSession);
    Stem top = root.addChildStem("top", "top");
    Group two = top.addChildGroup("two", "two");
    
    // foreign key
    GcGrouperSync gcGrouperSync = new GcGrouperSync();
    gcGrouperSync.setSyncEngine("temp");
    gcGrouperSync.setProvisionerName("myJob-3");
    gcGrouperSync.getGcGrouperSyncDao().store();
    
    GcGrouperSyncGroup gcGrouperSyncGroup = gcGrouperSync.getGcGrouperSyncGroupDao().groupRetrieveOrCreateByGroupId(group0.getId());
    gcGrouperSyncGroup.setLastTimeWorkWasDone(new Timestamp(System.currentTimeMillis() + 2000));
    gcGrouperSyncGroup.setProvisionable(true);
    gcGrouperSync.getGcGrouperSyncGroupDao().internal_groupStore(gcGrouperSyncGroup);

    GcGrouperSyncMember gcGrouperSyncMember = new GcGrouperSyncMember();
    gcGrouperSyncMember.setGrouperSync(gcGrouperSync);
    gcGrouperSyncMember.setProvisionable(true);
    gcGrouperSyncMember.setLastTimeWorkWasDone(new Timestamp(System.currentTimeMillis() + 2000));
    gcGrouperSyncMember.setMemberId(two.toMember().getUuid());
    gcGrouperSync.getGcGrouperSyncMemberDao().internal_memberStore(gcGrouperSyncMember);
    
    GcGrouperSyncMembership gcGrouperSyncMembership = new GcGrouperSyncMembership();
    gcGrouperSyncMembership.setGrouperSyncGroup(gcGrouperSyncGroup);
    gcGrouperSyncMembership.setGrouperSyncMember(gcGrouperSyncMember);
    gcGrouperSyncMembership.setGrouperSync(gcGrouperSync);
    gcGrouperSyncMembership.setMembershipId("memId");
    gcGrouperSync.getGcGrouperSyncMembershipDao().internal_membershipStore(gcGrouperSyncMembership);
    
    //When
    List<GcGrouperSyncMembership> grouperSyncMemberships = GrouperProvisioningService.retrieveGcGrouperSyncMemberships(two.toMember().getUuid(), group0.getId());
    
    //Then
    assertEquals(1, grouperSyncMemberships.size());
    assertEquals(gcGrouperSyncMembership.getId(), grouperSyncMemberships.get(0).getId());
    assertEquals(gcGrouperSync.getId(), grouperSyncMemberships.get(0).getGrouperSync().getId());
  }
  
  public void testDeleteInvalidConfigs() {
    
    //Given
    GrouperSessionResult grouperSessionResult = GrouperSession.startRootSessionIfNotStarted();
    GrouperSession grouperSession = grouperSessionResult.getGrouperSession();
    
    Stem stem0 = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test").save();
    
    saveProvisioningAttributeMetadata(stem0, true, "ldap-1");
    saveProvisioningAttributeMetadata(stem0, true, "ldap");
    
    GrouperProvisioningAttributeValue attributeValue = GrouperProvisioningService.getProvisioningAttributeValue(stem0, "ldap-1");
    
    assertNotNull(attributeValue);
    
    //When
    // we expect ldap-1 to be deleted because only ldap and box are valid ones as per the setUp method above
    GrouperProvisioningService.deleteInvalidConfigs();
    
    //Then
    attributeValue = GrouperProvisioningService.getProvisioningAttributeValue(stem0, "ldap-1");
    assertNull(attributeValue);
    
    attributeValue = GrouperProvisioningService.getProvisioningAttributeValue(stem0, "ldap");
    assertNotNull(attributeValue);
  }
  
  public void testGetProvisioningAttributeValue() {
    
    //Given
    GrouperSessionResult grouperSessionResult = GrouperSession.startRootSessionIfNotStarted();
    GrouperSession grouperSession = grouperSessionResult.getGrouperSession();
    
    Stem stem0 = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test").save();
    
    saveProvisioningAttributeMetadata(stem0, true, "ldap");
    
    //When
    GrouperProvisioningAttributeValue attributeValue1 = GrouperProvisioningService.getProvisioningAttributeValue(stem0, "ldap");
    
    //Then
    assertTrue(attributeValue1.isDirectAssignment());
    assertEquals("ldap", attributeValue1.getTargetName());
    
  }
  
  public void testGetProvisioningAttributeValueWithIndirectAndPolicyRestriction() {
    
    GrouperSessionResult grouperSessionResult = GrouperSession.startRootSessionIfNotStarted();
    GrouperSession grouperSession = grouperSessionResult.getGrouperSession();
    
    Stem stem0 = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test").save();
    Group group0 = stem0.addChildGroup("group0", "group0");
    
    GrouperProvisioningAttributeValue grouperProvisioningAttributeValue = new GrouperProvisioningAttributeValue();
    grouperProvisioningAttributeValue.setTargetName("ldap");
    grouperProvisioningAttributeValue.setDirectAssignment(true);
    grouperProvisioningAttributeValue.setDoProvision("ldap");
    grouperProvisioningAttributeValue.setStemScopeString(Stem.Scope.SUB.name().toLowerCase());
    
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(grouperProvisioningAttributeValue, stem0);
    
    GrouperProvisioningAttributeValue attributeValue1 = GrouperProvisioningService.getProvisioningAttributeValue(group0, "ldap");
    
    assertFalse(attributeValue1.isDirectAssignment());
    assertEquals("ldap", attributeValue1.getTargetName());
    assertEquals("ldap", attributeValue1.getDoProvision());
    assertEquals(0, attributeValue1.getMetadataNameValues().size());

    // add policy group restriction to config
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldap.onlyProvisionPolicyGroups", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldap.allowPolicyGroupOverride", "true");
    
    attributeValue1 = GrouperProvisioningService.getProvisioningAttributeValue(group0, "ldap");
    
    assertNull(attributeValue1);
    
    // make group0 into a policy group
    addPolicyType(group0);
    
    attributeValue1 = GrouperProvisioningService.getProvisioningAttributeValue(group0, "ldap");
    
    assertFalse(attributeValue1.isDirectAssignment());
    assertEquals("ldap", attributeValue1.getTargetName());
    assertEquals("ldap", attributeValue1.getDoProvision());
    assertEquals(0, attributeValue1.getMetadataNameValues().size());
    
    // delete policy designation
    group0.getAttributeDelegate().removeAttribute(GrouperObjectTypesAttributeNames.retrieveAttributeDefNameBase());

    attributeValue1 = GrouperProvisioningService.getProvisioningAttributeValue(group0, "ldap");
    
    assertNull(attributeValue1);
    
    // add override to folder
    grouperProvisioningAttributeValue.getMetadataNameValues().put("md_grouper_allowPolicyGroupOverride", false);
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(grouperProvisioningAttributeValue, stem0);
    
    attributeValue1 = GrouperProvisioningService.getProvisioningAttributeValue(group0, "ldap");
    
    assertFalse(attributeValue1.isDirectAssignment());
    assertEquals("ldap", attributeValue1.getTargetName());
    assertEquals("ldap", attributeValue1.getDoProvision());
    assertEquals(1, attributeValue1.getMetadataNameValues().size());
  }
  
  public void testGetProvisioningAttributeValueWithIndirectAndRegexRestriction() {
    
    GrouperSessionResult grouperSessionResult = GrouperSession.startRootSessionIfNotStarted();
    GrouperSession grouperSession = grouperSessionResult.getGrouperSession();
    
    Stem stem0 = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test").save();
    Group group0 = stem0.addChildGroup("group0_includes", "group0_includes");
    
    GrouperProvisioningAttributeValue grouperProvisioningAttributeValue = new GrouperProvisioningAttributeValue();
    grouperProvisioningAttributeValue.setTargetName("ldap");
    grouperProvisioningAttributeValue.setDirectAssignment(true);
    grouperProvisioningAttributeValue.setDoProvision("ldap");
    grouperProvisioningAttributeValue.setStemScopeString(Stem.Scope.SUB.name().toLowerCase());
    
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(grouperProvisioningAttributeValue, stem0);
    
    GrouperProvisioningAttributeValue attributeValue1 = GrouperProvisioningService.getProvisioningAttributeValue(group0, "ldap");
    
    assertFalse(attributeValue1.isDirectAssignment());
    assertEquals("ldap", attributeValue1.getTargetName());
    assertEquals("ldap", attributeValue1.getDoProvision());
    assertEquals(0, attributeValue1.getMetadataNameValues().size());

    // add policy group restriction to config
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldap.provisionableRegex", "groupExtension not matches ^.*_includes$|^.*_excludes$");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldap.allowProvisionableRegexOverride", "true");
    
    attributeValue1 = GrouperProvisioningService.getProvisioningAttributeValue(group0, "ldap");
    
    assertNull(attributeValue1);
    
    // rename group
    group0.setExtension("group0");
    group0.store();
    
    attributeValue1 = GrouperProvisioningService.getProvisioningAttributeValue(group0, "ldap");
    
    assertFalse(attributeValue1.isDirectAssignment());
    assertEquals("ldap", attributeValue1.getTargetName());
    assertEquals("ldap", attributeValue1.getDoProvision());
    assertEquals(0, attributeValue1.getMetadataNameValues().size());
    
    // rename back
    group0.setExtension("group0_includes");
    group0.store();

    attributeValue1 = GrouperProvisioningService.getProvisioningAttributeValue(group0, "ldap");
    
    assertNull(attributeValue1);
    
    // add override to folder
    grouperProvisioningAttributeValue.getMetadataNameValues().put("md_grouper_allowProvisionableRegexOverride", "groupExtension not matches ^.*_includesxx$|^.*_excludesxx$");
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(grouperProvisioningAttributeValue, stem0);
    
    attributeValue1 = GrouperProvisioningService.getProvisioningAttributeValue(group0, "ldap");
    
    assertFalse(attributeValue1.isDirectAssignment());
    assertEquals("ldap", attributeValue1.getTargetName());
    assertEquals("ldap", attributeValue1.getDoProvision());
    assertEquals(1, attributeValue1.getMetadataNameValues().size());
  }
  
  private static void addPolicyType(Group group) {

    AttributeAssign attributeAssign = group.getAttributeDelegate().addAttribute(GrouperObjectTypesAttributeNames.retrieveAttributeDefNameBase()).getAttributeAssign();

    AttributeDefName attributeDefName = AttributeDefNameFinder.findByName(objectTypesStemName()+":"+GROUPER_OBJECT_TYPE_DIRECT_ASSIGNMENT, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), "true");

    attributeDefName = AttributeDefNameFinder.findByName(objectTypesStemName()+":"+GROUPER_OBJECT_TYPE_NAME, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), "policy");

    attributeAssign.saveOrUpdate();
  }
  
  public void testGetProvisioningAttributeValues() {
    
    //Given
    GrouperSessionResult grouperSessionResult = GrouperSession.startRootSessionIfNotStarted();
    GrouperSession grouperSession = grouperSessionResult.getGrouperSession();
    
    Stem stem0 = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test").save();
    
    saveProvisioningAttributeMetadata(stem0, true, "ldap");
    saveProvisioningAttributeMetadata(stem0, true, "box");
    
    //When
    List<GrouperProvisioningAttributeValue> attributeValues = GrouperProvisioningService.getProvisioningAttributeValues(stem0);
    
    //Then
    assertEquals(attributeValues.size(), 2);
    String targetName1 = attributeValues.get(0).getTargetName();
    String targetName2 = attributeValues.get(1).getTargetName();
    
    List<String> validNames = Arrays.asList("ldap", "box");
    
    assertTrue(validNames.contains(targetName1));
    assertTrue(validNames.contains(targetName2));
    
  }
  
  @SuppressWarnings("unchecked")
  public void testSaveOrUpdateProvisioningAttributes() {
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("ldap.personLdap.url", "ldap://localhost:389");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("ldap.personLdap.user", "cn=admin,dc=example,dc=edu");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("ldap.personLdap.pass", "does not matter");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.numberOfGroupAttributes", "6");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.0.name", "name");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.0.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.0.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.0.translateExpressionType", "grouperProvisioningGroupField");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.0.translateFromGrouperProvisioningGroupField", "name");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.groupAttributeValueCacheHas", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.groupAttributeValueCache2has", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.groupAttributeValueCache2source", "target");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.groupAttributeValueCache2type", "groupAttribute");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.groupAttributeValueCache2groupAttribute", "name");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.1.name", "businessCategory");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.1.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.1.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.1.translateExpressionType", "grouperProvisioningGroupField");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.1.translateFromGrouperProvisioningGroupField", "idIndex");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.groupMatchingAttributeCount", "1");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.groupMatchingAttribute0name", "businessCategory");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.2.name", "cn");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.2.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.2.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.2.translateExpressionType", "grouperProvisioningGroupField");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.2.translateFromGrouperProvisioningGroupField", "name");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.3.name", "objectClass");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.3.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.3.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.3.multiValued", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.3.translateExpressionType", "translationScript");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.3.translateExpression", "${grouperUtil.toSet('top', 'groupOfNames')}");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.4.name", "member");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.4.defaultValue", "cn=admin,dc=example,dc=edu");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.groupMembershipAttributeName", "member");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.groupMembershipAttributeValue", "entityAttributeValueCache2");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.5.name", "description");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.5.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.5.update", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.5.delete", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.5.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.5.translateExpressionType", "grouperProvisioningGroupField");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetGroupAttribute.5.translateFromGrouperProvisioningGroupField", "description");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.numberOfEntityAttributes", "2");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetEntityAttribute.0.name", "name");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetEntityAttribute.0.select", "true");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.entityAttributeValueCacheHas", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.entityAttributeValueCache2has", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.entityAttributeValueCache2source", "target");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.entityAttributeValueCache2type", "entityAttribute");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.entityAttributeValueCache2entityAttribute", "name");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetEntityAttribute.1.name", "uid");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetEntityAttribute.1.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetEntityAttribute.1.translateExpressionType", "grouperProvisioningEntityField");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.targetEntityAttribute.1.translateFromGrouperProvisioningEntityField", "subjectId");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.entityMatchingAttributeCount", "1");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.entityMatchingAttribute0name", "uid");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.class", LdapSync.class.getName());
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.ldapExternalSystemConfigId", "personLdap");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.subjectSourcesToProvision", "jdbc");

    // ldap specific properties
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.provisioningType", "groupAttributes");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.groupSearchBaseDn", "ou=Groups,dc=example,dc=edu");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.groupSearchAllFilter", "(objectClass=groupOfNames)");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.groupSearchFilter", "(&(objectClass=groupOfNames)(businessCategory=${targetGroup.retrieveAttributeValue('businessCategory')}))");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.userSearchBaseDn", "ou=People,dc=example,dc=edu");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.userSearchAllFilter", "(&(objectClass=person)(uid=*))");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.userSearchFilter", "(&(objectClass=person)(uid=${targetEntity.retrieveAttributeValue('uid')}))");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.groupDnType", "flat");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.operateOnGrouperGroups", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.hasTargetGroupLink", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.selectGroups", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.insertGroups", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.deleteGroups", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.deleteGroupsIfNotExistInGrouper", "true");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.operateOnGrouperEntities", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.hasTargetEntityLink", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.selectEntities", "true");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.operateOnGrouperMemberships", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.provisioningType", "groupAttributes");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.selectMemberships", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.deleteMemberships", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.deleteMembershipsIfNotExistInGrouper", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.insertMemberships", "true");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.junitProvisioningAttributePropagationTest.logAllObjectsVerbose", "true");

    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("provisioningInUi.enable", "true");
    
    // edu.internet2.middleware.grouper.changeLog.esb.consumer
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer.provisioner_incremental_junitProvisioningAttributePropagationTestCLC.class", EsbConsumer.class.getName());
    // edu.internet2.middleware.grouper.app.provisioning
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer.provisioner_incremental_junitProvisioningAttributePropagationTestCLC.publisher.class", ProvisioningConsumer.class.getName());
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer.provisioner_incremental_junitProvisioningAttributePropagationTestCLC.quartzCron",  "9 59 23 31 12 ? 2099");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer.provisioner_incremental_junitProvisioningAttributePropagationTestCLC.provisionerConfigId", "junitProvisioningAttributePropagationTest");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer.provisioner_incremental_junitProvisioningAttributePropagationTestCLC.provisionerJobSyncType", GrouperProvisioningType.incrementalProvisionChangeLog.name());
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer.provisioner_incremental_junitProvisioningAttributePropagationTestCLC.publisher.debug", "true");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.provisioner_full_junitProvisioningAttributePropagationTest.class", GrouperProvisioningFullSyncJob.class.getName());
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.provisioner_full_junitProvisioningAttributePropagationTest.quartzCron", "9 59 23 31 12 ? 2099");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.provisioner_full_junitProvisioningAttributePropagationTest.provisionerConfigId", "junitProvisioningAttributePropagationTest");

    //Given
    GrouperSessionResult grouperSessionResult = GrouperSession.startRootSessionIfNotStarted();
    GrouperSession grouperSession = grouperSessionResult.getGrouperSession();
    
    Stem stem0 = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test").save();
    Group group0 = stem0.addChildGroup("group0", "group0");
    
    GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setTargetName("junitProvisioningAttributePropagationTest");
    attributeValue.setDoProvision("junitProvisioningAttributePropagationTest");
    Map<String, Object> metadataNameValues = new HashMap<String, Object>();
    metadataNameValues.put("string", "string");
    metadataNameValues.put("int", 2);
    metadataNameValues.put("float", 3.14);
    metadataNameValues.put("boolean", true);
    metadataNameValues.put("timestamp", new Timestamp(new Date().getTime()));
    attributeValue.setMetadataNameValues(metadataNameValues);
    
    //When
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem0);
    
    GrouperProvisioningOutput grouperProvisioningOutput = fullProvision();
    
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
    
    //Then
    assertEquals(1, GcGrouperSyncDao.retrieveOrCreateByProvisionerName("grouper", "junitProvisioningAttributePropagationTest").getGcGrouperSyncGroupDao().groupRetrieveAll().size());
    GcGrouperSyncGroup group0SyncGroup = GcGrouperSyncDao.retrieveOrCreateByProvisionerName("grouper", "junitProvisioningAttributePropagationTest").getGcGrouperSyncGroupDao().groupRetrieveByGroupId(group0.getId());
    
    assertEquals("T", group0SyncGroup.getProvisionableDb());
    assertNotNull(group0SyncGroup.getMetadataJson());
    
    Map<String, Object> metadata;
    try {
      metadata = GrouperProvisioningSettings.objectMapper.readValue(group0SyncGroup.getMetadataJson(), Map.class);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    assertEquals("string", metadata.get("string"));
    assertEquals(2, metadata.get("int"));
    assertEquals(3.14, metadata.get("float"));
    assertEquals(true, metadata.get("boolean"));
    assertTrue(metadata.containsKey("timestamp"));
  }
  
  public void testSaveOrUpdateProvisioningAttributesForMember() {
    
    //Given
    GrouperSessionResult grouperSessionResult = GrouperSession.startRootSessionIfNotStarted();
    GrouperSession grouperSession = grouperSessionResult.getGrouperSession();
    
    Member member = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ0, true);
    
    GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setTargetName("ldap");
    Map<String, Object> metadataNameValues = new HashMap<String, Object>();
    metadataNameValues.put("string", "string");
    metadataNameValues.put("int", 2);
    metadataNameValues.put("float", 3.14);
    metadataNameValues.put("boolean", true);
    metadataNameValues.put("timestamp", new Timestamp(new Date().getTime()));
    attributeValue.setMetadataNameValues(metadataNameValues);
    
    //When
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, member);
    
    //Then
    GrouperProvisioningAttributeValue attributeValue1 = GrouperProvisioningService.getProvisioningAttributeValue(member, "ldap");
    assertEquals("ldap", attributeValue1.getTargetName());
    
    Map<String, Object> metadata = attributeValue1.getMetadataNameValues();
    assertEquals("string", metadata.get("string"));
    assertEquals(2, metadata.get("int"));
    assertEquals(3.14, metadata.get("float"));
    assertEquals(true, metadata.get("boolean"));
    assertTrue(metadata.containsKey("timestamp"));
  }
  
  public void testSaveOrUpdateProvisioningAttributesForMembership() {
    
    //Given
    GrouperSessionResult grouperSessionResult = GrouperSession.startRootSessionIfNotStarted();
    GrouperSession grouperSession = grouperSessionResult.getGrouperSession();
    
    Group someGroup = new GroupSave(grouperSession).assignName("a:b:c")
        .assignCreateParentStemsIfNotExist(true).save();
    
    someGroup.addMember(SubjectTestHelper.SUBJ0);
    
    Member member = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ0, true);
    
    GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setTargetName("ldap");
    Map<String, Object> metadataNameValues = new HashMap<String, Object>();
    metadataNameValues.put("string", "string");
    metadataNameValues.put("int", 2);
    metadataNameValues.put("float", 3.14);
    metadataNameValues.put("boolean", true);
    metadataNameValues.put("timestamp", new Timestamp(new Date().getTime()));
    attributeValue.setMetadataNameValues(metadataNameValues);
    
    //When
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, someGroup, member);
    
    //Then
    GrouperProvisioningAttributeValue attributeValue1 = GrouperProvisioningService.getProvisioningAttributeValue(someGroup, member, "ldap");
    assertEquals("ldap", attributeValue1.getTargetName());
    
    Map<String, Object> metadata = attributeValue1.getMetadataNameValues();
    assertEquals("string", metadata.get("string"));
    assertEquals(2, metadata.get("int"));
    assertEquals(3.14, metadata.get("float"));
    assertEquals(true, metadata.get("boolean"));
    assertTrue(metadata.containsKey("timestamp"));
  }
  
  public void testTargetEditableWhenReadOnlyIsFalse() {
    
    //Given
    GrouperSessionResult grouperSessionResult = GrouperSession.startRootSessionIfNotStarted();
    GrouperSession grouperSession = grouperSessionResult.getGrouperSession();
    
    Stem stem0 = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test").save();
    
    GrouperProvisioningTarget target1 = new GrouperProvisioningTarget("ldapEditableKey", "ldapEditable");
    target1.setReadOnly(false);
    GrouperProvisioningSettings.getTargets(false).put("ldapEditable", target1);
    
    saveProvisioningAttributeMetadata(stem0, true, "ldapEditable");
    
    Stem etc = new StemSave(grouperSession).assignStemNameToEdit("etc").assignName("etc").save();
    Group wheel = etc.addChildGroup("wheel","wheel");
    wheel.addMember(SubjectTestHelper.SUBJ0);
    
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.wheel.use", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.wheel.group", wheel.getName());
    
    //When
    boolean isEditable = GrouperProvisioningService.isTargetEditable(target1, SubjectTestHelper.SUBJ0, stem0);
    
    // Then
    assertTrue(isEditable);
    
  }
  
  public void testTargetNotEditableWhenSubjectNotMemberOfGroup() {
    
    //Given
    GrouperSessionResult grouperSessionResult = GrouperSession.startRootSessionIfNotStarted();
    GrouperSession grouperSession = grouperSessionResult.getGrouperSession();
    
    Stem stem0 = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test").save();
    
    Group group0 = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:test1").save();
    
    
    GrouperProvisioningTarget target1 = new GrouperProvisioningTarget("ldapTargetKey", "ldapTarget");
    target1.setGroupAllowedToAssign(group0.getName());
    GrouperProvisioningSettings.getTargets(false).put("ldapTarget", target1);
    
    saveProvisioningAttributeMetadata(stem0, true, "ldapTarget");
    
    //When
    boolean isEditable = GrouperProvisioningService.isTargetEditable(target1, SubjectTestHelper.SUBJ0, stem0);
    
    // Then
    assertFalse(isEditable);
    
  }
  
  
  private static void saveProvisioningAttributeMetadata(Stem stem, boolean isDirect, String targetName) {
    
    AttributeAssign attributeAssign = stem.getAttributeDelegate().addAttribute(retrieveAttributeDefNameBase()).getAttributeAssign();
    
    AttributeDefName attributeDefName = AttributeDefNameFinder.findByName(provisioningConfigStemName()+":"+PROVISIONING_DIRECT_ASSIGNMENT, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), BooleanUtils.toStringTrueFalse(isDirect));
    
    attributeDefName = AttributeDefNameFinder.findByName(provisioningConfigStemName()+":"+PROVISIONING_TARGET, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), targetName);
    
    attributeDefName = AttributeDefNameFinder.findByName(provisioningConfigStemName()+":"+PROVISIONING_STEM_SCOPE, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), Stem.Scope.SUB.name());
    
    attributeAssign.saveOrUpdate();
    
  }

}
