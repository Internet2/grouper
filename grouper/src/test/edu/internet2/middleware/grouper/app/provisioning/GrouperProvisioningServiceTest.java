package edu.internet2.middleware.grouper.app.provisioning;

import static edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningAttributeNames.PROVISIONING_DIRECT_ASSIGNMENT;
import static edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningAttributeNames.PROVISIONING_STEM_SCOPE;
import static edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningAttributeNames.PROVISIONING_TARGET;
import static edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningAttributeNames.retrieveAttributeDefNameBase;
import static edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningSettings.provisioningConfigStemName;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.misc.GrouperCheckConfig;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.session.GrouperSessionResult;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSync;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncGroup;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncJob;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncJobState;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncLog;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncMember;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncMembership;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.lang3.BooleanUtils;

public class GrouperProvisioningServiceTest extends GrouperTest {
  
  @Override
  protected void setUp() {
    super.setUp();
    GrouperCheckConfig.checkGroups();
    GrouperCheckConfig.waitUntilDoneWithExtraConfig();
    
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("provisioningInUi.enable", "true");
    
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldap.class", "LdapProvisioner");
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
    
    GrouperProvisioningAttributeValue attributeValue = GrouperProvisioningService.getProvisioningAttributeValue(stem0, "ldap-1");
    
    assertNotNull(attributeValue);
    
    //When
    // we expect ldap-1 to be deleted because only ldap and box are valid ones as per the setUp method above
    GrouperProvisioningService.deleteInvalidConfigs();
    
    //Then
    attributeValue = GrouperProvisioningService.getProvisioningAttributeValue(stem0, "ldap-1");
    
    assertNull(attributeValue);
    
    
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
  
  public void testSaveOrUpdateProvisioningAttributes() {
    
    //Given
    GrouperSessionResult grouperSessionResult = GrouperSession.startRootSessionIfNotStarted();
    GrouperSession grouperSession = grouperSessionResult.getGrouperSession();
    
    Stem stem0 = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test").save();
    Stem stemTest1 = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:test1").save();
    
    GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setTargetName("ldap");
    
    //When
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem0);
    
    //Then
    GrouperProvisioningAttributeValue attributeValue1 = GrouperProvisioningService.getProvisioningAttributeValue(stemTest1, "ldap");
    assertFalse(attributeValue1.isDirectAssignment());
    assertEquals("ldap", attributeValue1.getTargetName());
  }
  
  public void testCopyConfigFromParent() {
    
    //Given
    GrouperSessionResult grouperSessionResult = GrouperSession.startRootSessionIfNotStarted();
    GrouperSession grouperSession = grouperSessionResult.getGrouperSession();
    
    Stem stem0 = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test").save();
    Stem stemTest1 = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:test1").save();
    
    saveProvisioningAttributeMetadata(stem0, true, "ldap");
    
    //When
    GrouperProvisioningService.copyConfigFromParent(stemTest1);
    
    //Then
    GrouperProvisioningAttributeValue attributeValue1 = GrouperProvisioningService.getProvisioningAttributeValue(stemTest1, "ldap");
    assertFalse(attributeValue1.isDirectAssignment());
    assertEquals("ldap", attributeValue1.getTargetName());
    
  }
  
  public void testTargetNotEditableWhenReadOnlyIsTrue() {
    
    //Given
    GrouperSessionResult grouperSessionResult = GrouperSession.startRootSessionIfNotStarted();
    GrouperSession grouperSession = grouperSessionResult.getGrouperSession();
    
    Stem stem0 = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test").save();
    
    GrouperProvisioningTarget target1 = new GrouperProvisioningTarget("ldapReadOnlyKey", "ldapReadOnly");
    target1.setReadOnly(true);
    GrouperProvisioningSettings.getTargets(false).put("ldapReadOnly", target1);
    
    saveProvisioningAttributeMetadata(stem0, true, "ldapReadOnly");
    
    Stem etc = new StemSave(grouperSession).assignStemNameToEdit("etc").assignName("etc").save();
    Group wheel = etc.addChildGroup("wheel","wheel");
    wheel.addMember(SubjectTestHelper.SUBJ0);
    
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.wheel.use", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.wheel.group", wheel.getName());
    
    //When
    boolean isEditable = GrouperProvisioningService.isTargetEditable(target1, SubjectTestHelper.SUBJ0, stem0);
    
    // Then
    assertFalse(isEditable);
    
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
