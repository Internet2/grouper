package edu.internet2.middleware.grouper.app.scim;

import java.sql.Timestamp;
import java.util.List;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningAttributeValue;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningBaseTest;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningOutput;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningService;
import edu.internet2.middleware.grouper.app.scim2Provisioning.GrouperScim2ApiCommands;
import edu.internet2.middleware.grouper.app.scim2Provisioning.GrouperScim2Group;
import edu.internet2.middleware.grouper.app.scim2Provisioning.GrouperScim2Membership;
import edu.internet2.middleware.grouper.app.scim2Provisioning.GrouperScim2User;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.misc.GrouperStartup;
import edu.internet2.middleware.grouper.util.CommandLineExec;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSync;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncDao;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncGroup;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncJob;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncJobState;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncMember;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncMembership;
import junit.textui.TestRunner;

public class GrouperAwsProvisionerTest extends GrouperProvisioningBaseTest {

  public static void main(String[] args) {
    TestRunner.run(new GrouperAwsProvisionerTest("testAWSFullSyncProvisionGroupAndThenDeleteTheGroup"));

  }
  
  @Override
  public String defaultConfigId() {
    return "awsProvisioner";
  }

  public static boolean startTomcat = false;
  
  public GrouperAwsProvisionerTest(String name) {
    super(name);
  }
  
  public void testAWSFullSyncProvisionGroupAndThenDeleteTheGroup() {
    
    if (!tomcatRunTests()) {
      return;
    }

    ScimProvisionerTestUtils.setupAwsExternalSystem();

    ScimProvisionerTestUtils.configureScimProvisioner(new ScimProvisionerTestConfigInput()
      .assignChangelogConsumerConfigId("awsScimProvTestCLC").assignConfigId("awsProvisioner")
      .assignBearerTokenExternalSystemConfigId("awsConfigId")
      .assignEntityDeleteType("deleteEntitiesIfNotExistInGrouper")
      .assignGroupDeleteType("deleteGroupsIfGrouperDeleted")
      .assignMembershipDeleteType("deleteMembershipsIfGrouperDeleted")
      .assignScimType("AWS")
      .assignGroupAttributeCount(2)
    );



    GrouperStartup.startup();
    
    if (startTomcat) {
      CommandLineExec commandLineExec = tomcatStart();
    }
    
    try {
      // this will create tables
      List<GrouperScim2User> grouperScimUsers = GrouperScim2ApiCommands.retrieveScimUsers("awsConfigId", null);
  
      new GcDbAccess().connectionName("grouper").sql("delete from mock_scim_membership").executeSql();
      new GcDbAccess().connectionName("grouper").sql("delete from mock_scim_group").executeSql();
      new GcDbAccess().connectionName("grouper").sql("delete from mock_scim_user").executeSql();

      GrouperSession grouperSession = GrouperSession.startRootSession();
      
      Stem stem = new StemSave(grouperSession).assignName("test").save();
      Stem stem2 = new StemSave(grouperSession).assignName("test2").save();
      
      // mark some folders to provision
      Group testGroup = new GroupSave(grouperSession).assignName("test:testGroup").save();
      Group testGroup2 = new GroupSave(grouperSession).assignName("test2:testGroup2").save();
      
      testGroup.addMember(SubjectTestHelper.SUBJ0, false);
      testGroup.addMember(SubjectTestHelper.SUBJ1, false);
      
      testGroup2.addMember(SubjectTestHelper.SUBJ1, false);
      testGroup2.addMember(SubjectTestHelper.SUBJ2, false);
      testGroup2.addMember(SubjectTestHelper.SUBJ3, false);
      
      final GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
      attributeValue.setDirectAssignment(true);
      attributeValue.setDoProvision("awsProvisioner");
      attributeValue.setTargetName("awsProvisioner");
      attributeValue.setStemScopeString("sub");
  
      GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);
  
      //lets sync these over      
      assertEquals(new Integer(0), new GcDbAccess().connectionName("grouper").sql("select count(1) from mock_scim_group").select(int.class));
  
      
      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperScim2Group").list(GrouperScim2Group.class).size());
      
      long started = System.currentTimeMillis();
      
      GrouperProvisioningOutput grouperProvisioningOutput = fullProvision();
      GrouperUtil.sleep(2000);
      assertTrue(1 <= grouperProvisioningOutput.getInsert());
      assertEquals(1, HibernateSession.byHqlStatic().createQuery("from GrouperScim2Group").list(GrouperScim2Group.class).size());
      assertEquals(2, HibernateSession.byHqlStatic().createQuery("from GrouperScim2User").list(GrouperScim2User.class).size());
      assertEquals(2, HibernateSession.byHqlStatic().createQuery("from GrouperScim2Membership").list(GrouperScim2Membership.class).size());
      GrouperScim2Group grouperScimGroup = HibernateSession.byHqlStatic().createQuery("from GrouperScim2Group").list(GrouperScim2Group.class).get(0);

      assertEquals("testGroup", grouperScimGroup.getDisplayName());
      
      GcGrouperSync gcGrouperSync = GcGrouperSyncDao.retrieveByProvisionerName(null, "awsProvisioner");
      assertEquals(1, gcGrouperSync.getGroupCount().intValue());
      
      GcGrouperSyncGroup gcGrouperSyncGroup = gcGrouperSync.getGcGrouperSyncGroupDao().groupRetrieveByGroupId(testGroup.getId());
      assertEquals(testGroup.getId(), gcGrouperSyncGroup.getGroupId());
      assertEquals(testGroup.getName(), gcGrouperSyncGroup.getGroupName());
      
      assertEquals(1, gcGrouperSync.getGroupCount().intValue());
      assertEquals(2, gcGrouperSync.getUserCount().intValue());
      assertEquals(2, gcGrouperSync.getRecordsCount().intValue());
      assertTrue(started <=  gcGrouperSync.getLastFullSyncRun().getTime());
      assertTrue(new Timestamp(System.currentTimeMillis()) + ", " + gcGrouperSync.getLastFullSyncRun(), 
      System.currentTimeMillis() >=  gcGrouperSync.getLastFullSyncRun().getTime());
      assertTrue(started <= gcGrouperSync.getLastUpdated().getTime());
      assertTrue(System.currentTimeMillis() >= gcGrouperSync.getLastUpdated().getTime());
      
      GcGrouperSyncJob gcGrouperSyncJob = gcGrouperSync.getGcGrouperSyncJobDao().jobRetrieveBySyncType("fullProvisionFull");
      GrouperUtil.sleep(2000);
      assertEquals(100, gcGrouperSyncJob.getPercentComplete().intValue());
      assertEquals(GcGrouperSyncJobState.notRunning, gcGrouperSyncJob.getJobState());
      assertTrue(started <= gcGrouperSyncJob.getLastSyncTimestamp().getTime());
      assertTrue(System.currentTimeMillis() >= gcGrouperSyncJob.getLastSyncTimestamp().getTime());
      assertTrue(started <= gcGrouperSyncJob.getLastTimeWorkWasDone().getTime());
      assertTrue(System.currentTimeMillis() >= gcGrouperSyncJob.getLastTimeWorkWasDone().getTime());
      assertTrue(started <= gcGrouperSyncJob.getHeartbeat().getTime());
      assertTrue(System.currentTimeMillis() >= gcGrouperSyncJob.getHeartbeat().getTime());
      assertTrue(started <= gcGrouperSyncJob.getLastUpdated().getTime());
      assertTrue(System.currentTimeMillis() >= gcGrouperSyncJob.getLastUpdated().getTime());
      assertNull(gcGrouperSyncJob.getErrorMessage());
      assertNull(gcGrouperSyncJob.getErrorTimestamp());
      
      assertEquals(testGroup.getId(), gcGrouperSyncGroup.getGroupId());
      assertEquals(testGroup.getName(), gcGrouperSyncGroup.getGroupName());
      assertEquals(testGroup.getIdIndex(), gcGrouperSyncGroup.getGroupIdIndex());
      assertEquals("T", gcGrouperSyncGroup.getProvisionableDb());
      assertEquals("T", gcGrouperSyncGroup.getInTargetDb());
      assertEquals("T", gcGrouperSyncGroup.getInTargetInsertOrExistsDb());
      assertTrue(started <= gcGrouperSyncGroup.getInTargetStart().getTime());
      assertTrue(System.currentTimeMillis() >= gcGrouperSyncGroup.getInTargetStart().getTime());
      assertNull(gcGrouperSyncGroup.getInTargetEnd());
      assertTrue(started <= gcGrouperSyncGroup.getProvisionableStart().getTime());
      assertTrue(System.currentTimeMillis() >= gcGrouperSyncGroup.getProvisionableStart().getTime());
      assertNull(gcGrouperSyncGroup.getProvisionableEnd());
      assertTrue(started <= gcGrouperSyncGroup.getLastUpdated().getTime());
      assertTrue(System.currentTimeMillis() >= gcGrouperSyncGroup.getLastUpdated().getTime());
      assertNotNull(gcGrouperSyncGroup.getGroupAttributeValueCache2());
      assertNull(gcGrouperSyncGroup.getGroupAttributeValueCache0());
      assertNull(gcGrouperSyncGroup.getGroupAttributeValueCache1());
      assertNull(gcGrouperSyncGroup.getGroupAttributeValueCache3());
      assertNotNull(gcGrouperSyncGroup.getLastGroupMetadataSync());
      assertNull(gcGrouperSyncGroup.getErrorMessage());
      assertNull(gcGrouperSyncGroup.getErrorTimestamp());
      assertNull(gcGrouperSyncGroup.getLastGroupSync());
      
      Member testSubject0member = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ0, true);
      Member testSubject1member = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ1, true);
      
      GcGrouperSyncMember gcGrouperSyncMember = gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberId(testSubject0member.getId());
      assertEquals(testSubject0member.getId(), gcGrouperSyncMember.getMemberId());
      assertEquals(testSubject0member.getSubjectId(), gcGrouperSyncMember.getSubjectId());
      assertEquals(testSubject0member.getSubjectSourceId(), gcGrouperSyncMember.getSourceId());
      assertEquals(testSubject0member.getSubjectIdentifier0(), gcGrouperSyncMember.getSubjectIdentifier());
      assertEquals("T", gcGrouperSyncMember.getProvisionableDb());
      assertEquals("T", gcGrouperSyncMember.getInTargetDb());
      assertEquals("T", gcGrouperSyncMember.getInTargetInsertOrExistsDb());
      assertTrue(started <= gcGrouperSyncMember.getInTargetStart().getTime());
      assertNull(gcGrouperSyncMember.getInTargetEnd());
      assertTrue(started <= gcGrouperSyncMember.getProvisionableStart().getTime());
      assertTrue(System.currentTimeMillis() >= gcGrouperSyncMember.getProvisionableStart().getTime());
      assertNull(gcGrouperSyncMember.getProvisionableEnd());
      assertTrue(started <= gcGrouperSyncMember.getLastUpdated().getTime());
      assertTrue(System.currentTimeMillis() >= gcGrouperSyncMember.getLastUpdated().getTime());
      assertNull(gcGrouperSyncMember.getEntityAttributeValueCache0());
      assertNull(gcGrouperSyncMember.getEntityAttributeValueCache1());
      assertNotNull(gcGrouperSyncMember.getEntityAttributeValueCache2());
      assertNull(gcGrouperSyncMember.getEntityAttributeValueCache3());
      assertNull(gcGrouperSyncMember.getLastUserMetadataSync());
      assertNull(gcGrouperSyncMember.getErrorMessage());
      assertNull(gcGrouperSyncMember.getErrorTimestamp());
      assertNull(gcGrouperSyncMember.getLastUserSync());

      gcGrouperSyncMember = gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberId(testSubject1member.getId());
      assertEquals(testSubject1member.getId(), gcGrouperSyncMember.getMemberId());
      assertEquals(testSubject1member.getSubjectId(), gcGrouperSyncMember.getSubjectId());
      assertEquals(testSubject1member.getSubjectSourceId(), gcGrouperSyncMember.getSourceId());
      assertEquals(testSubject1member.getSubjectIdentifier0(), gcGrouperSyncMember.getSubjectIdentifier());
      assertEquals("T", gcGrouperSyncMember.getProvisionableDb());
      assertEquals("T", gcGrouperSyncMember.getInTargetDb());
      assertEquals("T", gcGrouperSyncMember.getInTargetInsertOrExistsDb());
      assertTrue(started <= gcGrouperSyncMember.getInTargetStart().getTime());
      assertNull(gcGrouperSyncMember.getInTargetEnd());
      assertTrue(started <= gcGrouperSyncMember.getProvisionableStart().getTime());
      assertTrue(System.currentTimeMillis() >= gcGrouperSyncMember.getProvisionableStart().getTime());
      assertNull(gcGrouperSyncMember.getProvisionableEnd());
      assertTrue(started <= gcGrouperSyncMember.getLastUpdated().getTime());
      assertTrue(System.currentTimeMillis() >= gcGrouperSyncMember.getLastUpdated().getTime());
      assertNull(gcGrouperSyncMember.getEntityAttributeValueCache0());
      assertNull(gcGrouperSyncMember.getEntityAttributeValueCache1());
      assertNotNull(gcGrouperSyncMember.getEntityAttributeValueCache2());
      assertNull(gcGrouperSyncMember.getEntityAttributeValueCache3());
      assertNull(gcGrouperSyncMember.getLastUserMetadataSync());
      assertNull(gcGrouperSyncMember.getErrorMessage());
      assertNull(gcGrouperSyncMember.getErrorTimestamp());
      assertNull(gcGrouperSyncMember.getLastUserSync());
      
      GcGrouperSyncMembership gcGrouperSyncMembership = gcGrouperSync.getGcGrouperSyncMembershipDao().membershipRetrieveByGroupIdAndMemberId(testGroup.getId(), testSubject0member.getId());
      assertEquals("T", gcGrouperSyncMembership.getInTargetDb());
      assertEquals("T", gcGrouperSyncMembership.getInTargetInsertOrExistsDb());
      assertTrue(started < gcGrouperSyncMembership.getInTargetStart().getTime());
      assertTrue(System.currentTimeMillis() >= gcGrouperSyncMembership.getInTargetStart().getTime());
      assertNull(gcGrouperSyncMembership.getInTargetEnd());
      assertTrue(started < gcGrouperSyncMembership.getLastUpdated().getTime());
      assertTrue(System.currentTimeMillis() >= gcGrouperSyncMembership.getLastUpdated().getTime());
      assertNull(gcGrouperSyncMembership.getMembershipId());
      assertNull(gcGrouperSyncMembership.getMembershipId2());
      assertNull(gcGrouperSyncMembership.getErrorMessage());
      assertNull(gcGrouperSyncMembership.getErrorTimestamp());
      
      gcGrouperSyncMembership = gcGrouperSync.getGcGrouperSyncMembershipDao().membershipRetrieveByGroupIdAndMemberId(testGroup.getId(), testSubject1member.getId());
      assertEquals("T", gcGrouperSyncMembership.getInTargetDb());
      assertEquals("T", gcGrouperSyncMembership.getInTargetInsertOrExistsDb());
      assertTrue(started < gcGrouperSyncMembership.getInTargetStart().getTime());
      assertTrue(System.currentTimeMillis() >= gcGrouperSyncMembership.getInTargetStart().getTime());
      assertNull(gcGrouperSyncMembership.getInTargetEnd());
      assertTrue(started < gcGrouperSyncMembership.getLastUpdated().getTime());
      assertTrue(System.currentTimeMillis() >= gcGrouperSyncMembership.getLastUpdated().getTime());
      assertNull(gcGrouperSyncMembership.getMembershipId());
      assertNull(gcGrouperSyncMembership.getMembershipId2());
      assertNull(gcGrouperSyncMembership.getErrorMessage());
      assertNull(gcGrouperSyncMembership.getErrorTimestamp());
      
      
      //now remove one of the subjects from the testGroup
      testGroup.deleteMember(SubjectTestHelper.SUBJ1);
      
      // now run the full sync again and the member should be deleted from mock_scim_membership also
      started = System.currentTimeMillis();
      
      grouperProvisioningOutput = fullProvision();
      GrouperUtil.sleep(2000);
      assertEquals(1, HibernateSession.byHqlStatic().createQuery("from GrouperScim2Group").list(GrouperScim2Group.class).size());
      assertEquals(1, HibernateSession.byHqlStatic().createQuery("from GrouperScim2User").list(GrouperScim2User.class).size());
      assertEquals(1, HibernateSession.byHqlStatic().createQuery("from GrouperScim2Membership").list(GrouperScim2Membership.class).size());
      
      
      gcGrouperSync = GcGrouperSyncDao.retrieveByProvisionerName(null, "awsProvisioner");
      assertEquals(1, gcGrouperSync.getGroupCount().intValue());
      
      gcGrouperSyncGroup = gcGrouperSync.getGcGrouperSyncGroupDao().groupRetrieveByGroupId(testGroup.getId());
      assertEquals(testGroup.getId(), gcGrouperSyncGroup.getGroupId());
      assertEquals(testGroup.getName(), gcGrouperSyncGroup.getGroupName());
      
      assertEquals(1, gcGrouperSync.getGroupCount().intValue());
      assertEquals(1, gcGrouperSync.getUserCount().intValue());
      assertEquals(1, gcGrouperSync.getRecordsCount().intValue());
      assertTrue(new Timestamp(System.currentTimeMillis()) + ", " + gcGrouperSync.getLastFullSyncRun(), 
          System.currentTimeMillis() >=  gcGrouperSync.getLastFullSyncRun().getTime());
      assertTrue(System.currentTimeMillis() >= gcGrouperSync.getLastUpdated().getTime());
      
      gcGrouperSyncJob = gcGrouperSync.getGcGrouperSyncJobDao().jobRetrieveBySyncType("fullProvisionFull");
      GrouperUtil.sleep(2000);
      assertEquals(100, gcGrouperSyncJob.getPercentComplete().intValue());
      assertEquals(GcGrouperSyncJobState.notRunning, gcGrouperSyncJob.getJobState());
      assertTrue(System.currentTimeMillis() >= gcGrouperSyncJob.getLastSyncTimestamp().getTime());
      assertTrue(System.currentTimeMillis() >= gcGrouperSyncJob.getLastTimeWorkWasDone().getTime());
      assertTrue(System.currentTimeMillis() >= gcGrouperSyncJob.getHeartbeat().getTime());
      assertTrue(System.currentTimeMillis() >= gcGrouperSyncJob.getLastUpdated().getTime());
      assertNull(gcGrouperSyncJob.getErrorMessage());
      assertNull(gcGrouperSyncJob.getErrorTimestamp());
      
      assertEquals(testGroup.getId(), gcGrouperSyncGroup.getGroupId());
      assertEquals(testGroup.getName(), gcGrouperSyncGroup.getGroupName());
      assertEquals(testGroup.getIdIndex(), gcGrouperSyncGroup.getGroupIdIndex());
      assertEquals("T", gcGrouperSyncGroup.getProvisionableDb());
      assertEquals("T", gcGrouperSyncGroup.getInTargetDb());
      assertEquals("T", gcGrouperSyncGroup.getInTargetInsertOrExistsDb());
      assertTrue(started > gcGrouperSyncGroup.getInTargetStart().getTime()); // because no change from previous run
      assertTrue(System.currentTimeMillis() >= gcGrouperSyncGroup.getInTargetStart().getTime());
      assertNull(gcGrouperSyncGroup.getInTargetEnd());
      assertTrue(started > gcGrouperSyncGroup.getProvisionableStart().getTime()); // because no change from previous run
      assertTrue(System.currentTimeMillis() >= gcGrouperSyncGroup.getProvisionableStart().getTime());
      assertNull(gcGrouperSyncGroup.getProvisionableEnd());
      assertTrue(started > gcGrouperSyncGroup.getLastUpdated().getTime()); // because no change from previous run
      assertTrue(System.currentTimeMillis() >= gcGrouperSyncGroup.getLastUpdated().getTime());
      assertNotNull(gcGrouperSyncGroup.getGroupAttributeValueCache2());
      assertNull(gcGrouperSyncGroup.getGroupAttributeValueCache0());
      assertNull(gcGrouperSyncGroup.getGroupAttributeValueCache1());
      assertNull(gcGrouperSyncGroup.getGroupAttributeValueCache3());
      assertNotNull(gcGrouperSyncGroup.getLastGroupMetadataSync());
      assertNull(gcGrouperSyncGroup.getErrorMessage());
      assertNull(gcGrouperSyncGroup.getErrorTimestamp());
      assertNull(gcGrouperSyncGroup.getLastGroupSync());
      
      testSubject0member = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ0, true);
      testSubject1member = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ1, true);
      
      gcGrouperSyncMember = gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberId(testSubject0member.getId());
      assertEquals(testSubject0member.getId(), gcGrouperSyncMember.getMemberId());
      assertEquals(testSubject0member.getSubjectId(), gcGrouperSyncMember.getSubjectId());
      assertEquals(testSubject0member.getSubjectSourceId(), gcGrouperSyncMember.getSourceId());
      assertEquals(testSubject0member.getSubjectIdentifier0(), gcGrouperSyncMember.getSubjectIdentifier());
      assertEquals("T", gcGrouperSyncMember.getProvisionableDb());
      assertEquals("T", gcGrouperSyncMember.getInTargetDb());
      assertEquals("T", gcGrouperSyncMember.getInTargetInsertOrExistsDb());
      assertTrue(started > gcGrouperSyncMember.getInTargetStart().getTime()); // because no change from previous run
      assertNull(gcGrouperSyncMember.getInTargetEnd());
      assertTrue(started > gcGrouperSyncMember.getProvisionableStart().getTime()); // because no change from previous run
      assertTrue(System.currentTimeMillis() >= gcGrouperSyncMember.getProvisionableStart().getTime());
      assertNull(gcGrouperSyncMember.getProvisionableEnd());
      assertTrue(started > gcGrouperSyncMember.getLastUpdated().getTime()); // because no change from previous run
      assertTrue(System.currentTimeMillis() >= gcGrouperSyncMember.getLastUpdated().getTime());
      assertNull(gcGrouperSyncMember.getEntityAttributeValueCache0());
      assertNull(gcGrouperSyncMember.getEntityAttributeValueCache1());
      assertNotNull(gcGrouperSyncMember.getEntityAttributeValueCache2());
      assertNull(gcGrouperSyncMember.getEntityAttributeValueCache3());
      assertNull(gcGrouperSyncMember.getLastUserMetadataSync());
      assertNull(gcGrouperSyncMember.getErrorMessage());
      assertNull(gcGrouperSyncMember.getErrorTimestamp());
      assertNull(gcGrouperSyncMember.getLastUserSync());
      
      gcGrouperSyncMember = gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberId(testSubject1member.getId());
      assertEquals(testSubject1member.getId(), gcGrouperSyncMember.getMemberId());
      assertEquals(testSubject1member.getSubjectId(), gcGrouperSyncMember.getSubjectId());
      assertEquals(testSubject1member.getSubjectSourceId(), gcGrouperSyncMember.getSourceId());
//      assertEquals(testSubject1member.getSubjectIdentifier0(), gcGrouperSyncMember.getSubjectIdentifier());
      assertEquals("F", gcGrouperSyncMember.getProvisionableDb());
      assertEquals("F", gcGrouperSyncMember.getInTargetDb());
      assertEquals("T", gcGrouperSyncMember.getInTargetInsertOrExistsDb());
      assertNotNull(gcGrouperSyncMember.getInTargetEnd());
      assertTrue(System.currentTimeMillis() >= gcGrouperSyncMember.getProvisionableStart().getTime());
      assertNotNull(gcGrouperSyncMember.getProvisionableEnd());
      assertTrue(System.currentTimeMillis() >= gcGrouperSyncMember.getLastUpdated().getTime());
      assertNull(gcGrouperSyncMember.getEntityAttributeValueCache0());
      assertNull(gcGrouperSyncMember.getEntityAttributeValueCache1());
      assertNotNull(gcGrouperSyncMember.getEntityAttributeValueCache2());
      assertNull(gcGrouperSyncMember.getEntityAttributeValueCache3());
      assertNull(gcGrouperSyncMember.getLastUserMetadataSync());
      assertNull(gcGrouperSyncMember.getErrorMessage());
      assertNull(gcGrouperSyncMember.getErrorTimestamp());
      assertNull(gcGrouperSyncMember.getLastUserSync());

      gcGrouperSyncMembership = gcGrouperSync.getGcGrouperSyncMembershipDao().membershipRetrieveByGroupIdAndMemberId(testGroup.getId(), testSubject0member.getId());
      assertEquals("T", gcGrouperSyncMembership.getInTargetDb());
      assertEquals("T", gcGrouperSyncMembership.getInTargetInsertOrExistsDb());
      assertTrue(System.currentTimeMillis() >= gcGrouperSyncMembership.getInTargetStart().getTime());
      assertNull(gcGrouperSyncMembership.getInTargetEnd());
      assertTrue(System.currentTimeMillis() >= gcGrouperSyncMembership.getLastUpdated().getTime());
      assertNull(gcGrouperSyncMembership.getMembershipId());
      assertNull(gcGrouperSyncMembership.getMembershipId2());
      assertNull(gcGrouperSyncMembership.getErrorMessage());
      assertNull(gcGrouperSyncMembership.getErrorTimestamp());
      
      gcGrouperSyncMembership = gcGrouperSync.getGcGrouperSyncMembershipDao().membershipRetrieveByGroupIdAndMemberId(testGroup.getId(), testSubject1member.getId());
      assertEquals("F", gcGrouperSyncMembership.getInTargetDb());
      assertEquals("T", gcGrouperSyncMembership.getInTargetInsertOrExistsDb());
      assertTrue(System.currentTimeMillis() >= gcGrouperSyncMembership.getInTargetStart().getTime());
      assertNotNull(gcGrouperSyncMembership.getInTargetEnd());
      assertTrue(System.currentTimeMillis() >= gcGrouperSyncMembership.getLastUpdated().getTime());
      assertNull(gcGrouperSyncMembership.getMembershipId());
      assertNull(gcGrouperSyncMembership.getMembershipId2());
      assertNull(gcGrouperSyncMembership.getErrorMessage());
      assertNull(gcGrouperSyncMembership.getErrorTimestamp());
      
      //now delete the group and sync again
      testGroup.delete();
      
      started = System.currentTimeMillis();
      
      grouperProvisioningOutput = fullProvision();
      GrouperUtil.sleep(2000);
      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperScim2Group").list(GrouperScim2Group.class).size());
      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperScim2User").list(GrouperScim2User.class).size());
      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperScim2Membership").list(GrouperScim2Membership.class).size());
      
      gcGrouperSync = GcGrouperSyncDao.retrieveByProvisionerName(null, "awsProvisioner");
      assertEquals(0, gcGrouperSync.getGroupCount().intValue());
      
      gcGrouperSyncGroup = gcGrouperSync.getGcGrouperSyncGroupDao().groupRetrieveByGroupId(testGroup.getId());
      assertEquals(testGroup.getId(), gcGrouperSyncGroup.getGroupId());
      assertEquals(testGroup.getName(), gcGrouperSyncGroup.getGroupName());
      
      assertEquals(0, gcGrouperSync.getUserCount().intValue());
      assertEquals(0, gcGrouperSync.getRecordsCount().intValue());
      assertTrue(new Timestamp(System.currentTimeMillis()) + ", " + gcGrouperSync.getLastFullSyncRun(), 
          System.currentTimeMillis() >=  gcGrouperSync.getLastFullSyncRun().getTime());
      assertTrue(System.currentTimeMillis() >= gcGrouperSync.getLastUpdated().getTime());
      
      gcGrouperSyncJob = gcGrouperSync.getGcGrouperSyncJobDao().jobRetrieveBySyncType("fullProvisionFull");
      GrouperUtil.sleep(2000);
      assertEquals(100, gcGrouperSyncJob.getPercentComplete().intValue());
      assertEquals(GcGrouperSyncJobState.notRunning, gcGrouperSyncJob.getJobState());
      assertTrue(System.currentTimeMillis() >= gcGrouperSyncJob.getLastSyncTimestamp().getTime());
      assertTrue(System.currentTimeMillis() >= gcGrouperSyncJob.getLastTimeWorkWasDone().getTime());
      assertTrue(System.currentTimeMillis() >= gcGrouperSyncJob.getHeartbeat().getTime());
      assertTrue(System.currentTimeMillis() >= gcGrouperSyncJob.getLastUpdated().getTime());
      assertNull(gcGrouperSyncJob.getErrorMessage());
      assertNull(gcGrouperSyncJob.getErrorTimestamp());
      
      assertEquals(testGroup.getId(), gcGrouperSyncGroup.getGroupId());
      assertEquals(testGroup.getName(), gcGrouperSyncGroup.getGroupName());
      assertEquals(testGroup.getIdIndex(), gcGrouperSyncGroup.getGroupIdIndex());
      assertEquals("F", gcGrouperSyncGroup.getProvisionableDb());
      assertEquals("F", gcGrouperSyncGroup.getInTargetDb());
      assertEquals("T", gcGrouperSyncGroup.getInTargetInsertOrExistsDb());
      assertTrue(System.currentTimeMillis() >= gcGrouperSyncGroup.getInTargetStart().getTime());
      assertNotNull(gcGrouperSyncGroup.getInTargetEnd());
      assertTrue(started >= gcGrouperSyncGroup.getProvisionableStart().getTime());
      assertTrue(System.currentTimeMillis() >= gcGrouperSyncGroup.getProvisionableStart().getTime());
      assertNotNull(gcGrouperSyncGroup.getProvisionableEnd());
      assertTrue(System.currentTimeMillis() >= gcGrouperSyncGroup.getLastUpdated().getTime());
      assertNotNull(gcGrouperSyncGroup.getGroupAttributeValueCache2());
      assertNull(gcGrouperSyncGroup.getGroupAttributeValueCache0());
      assertNull(gcGrouperSyncGroup.getGroupAttributeValueCache1());
      assertNull(gcGrouperSyncGroup.getGroupAttributeValueCache3());
      assertNotNull(gcGrouperSyncGroup.getLastGroupMetadataSync());
      assertNull(gcGrouperSyncGroup.getErrorMessage());
      assertNull(gcGrouperSyncGroup.getErrorTimestamp());
      assertNull(gcGrouperSyncGroup.getLastGroupSync());
      
      testSubject0member = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ0, true);
      testSubject1member = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ1, true);
      
      gcGrouperSyncMember = gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberId(testSubject0member.getId());
      assertEquals(testSubject0member.getId(), gcGrouperSyncMember.getMemberId());
      assertEquals(testSubject0member.getSubjectId(), gcGrouperSyncMember.getSubjectId());
      assertEquals(testSubject0member.getSubjectSourceId(), gcGrouperSyncMember.getSourceId());
      assertEquals("F", gcGrouperSyncMember.getProvisionableDb());
      assertEquals("F", gcGrouperSyncMember.getInTargetDb());
      assertEquals("T", gcGrouperSyncMember.getInTargetInsertOrExistsDb());
      assertTrue(started > gcGrouperSyncMember.getInTargetStart().getTime());
      assertNotNull(gcGrouperSyncMember.getInTargetEnd());
      assertTrue(System.currentTimeMillis() >= gcGrouperSyncMember.getProvisionableStart().getTime());
      assertNotNull(gcGrouperSyncMember.getProvisionableEnd());
      assertTrue(System.currentTimeMillis() >= gcGrouperSyncMember.getLastUpdated().getTime());
      assertNull(gcGrouperSyncMember.getEntityAttributeValueCache0());
      assertNull(gcGrouperSyncMember.getEntityAttributeValueCache1());
      assertNotNull(gcGrouperSyncMember.getEntityAttributeValueCache2());
      assertNull(gcGrouperSyncMember.getEntityAttributeValueCache3());
      assertNull(gcGrouperSyncMember.getLastUserMetadataSync());
      assertNull(gcGrouperSyncMember.getErrorMessage());
      assertNull(gcGrouperSyncMember.getErrorTimestamp());
      assertNull(gcGrouperSyncMember.getLastUserSync());
      
      gcGrouperSyncMember = gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberId(testSubject1member.getId());
      assertEquals(testSubject1member.getId(), gcGrouperSyncMember.getMemberId());
      assertEquals(testSubject1member.getSubjectId(), gcGrouperSyncMember.getSubjectId());
      assertEquals(testSubject1member.getSubjectSourceId(), gcGrouperSyncMember.getSourceId());
      assertEquals("F", gcGrouperSyncMember.getProvisionableDb());
      assertEquals("F", gcGrouperSyncMember.getInTargetDb());
      assertEquals("T", gcGrouperSyncMember.getInTargetInsertOrExistsDb());
      assertNotNull(gcGrouperSyncMember.getInTargetEnd());
      assertTrue(System.currentTimeMillis() >= gcGrouperSyncMember.getProvisionableStart().getTime());
      assertNotNull(gcGrouperSyncMember.getProvisionableEnd());
      assertTrue(System.currentTimeMillis() >= gcGrouperSyncMember.getLastUpdated().getTime());
      assertNull(gcGrouperSyncMember.getEntityAttributeValueCache0());
      assertNull(gcGrouperSyncMember.getEntityAttributeValueCache1());
      assertNotNull(gcGrouperSyncMember.getEntityAttributeValueCache2());
      assertNull(gcGrouperSyncMember.getEntityAttributeValueCache3());
      assertNull(gcGrouperSyncMember.getLastUserMetadataSync());
      assertNull(gcGrouperSyncMember.getErrorMessage());
      assertNull(gcGrouperSyncMember.getErrorTimestamp());
      assertNull(gcGrouperSyncMember.getLastUserSync());

      gcGrouperSyncMembership = gcGrouperSync.getGcGrouperSyncMembershipDao().membershipRetrieveByGroupIdAndMemberId(testGroup.getId(), testSubject0member.getId());
      assertEquals("F", gcGrouperSyncMembership.getInTargetDb());
      assertEquals("T", gcGrouperSyncMembership.getInTargetInsertOrExistsDb());
      assertTrue(System.currentTimeMillis() >= gcGrouperSyncMembership.getInTargetStart().getTime());
      assertNotNull(gcGrouperSyncMembership.getInTargetEnd());
      assertTrue(System.currentTimeMillis() >= gcGrouperSyncMembership.getLastUpdated().getTime());
      assertNull(gcGrouperSyncMembership.getMembershipId());
      assertNull(gcGrouperSyncMembership.getMembershipId2());
      assertNull(gcGrouperSyncMembership.getErrorMessage());
      assertNull(gcGrouperSyncMembership.getErrorTimestamp());
      
      gcGrouperSyncMembership = gcGrouperSync.getGcGrouperSyncMembershipDao().membershipRetrieveByGroupIdAndMemberId(testGroup.getId(), testSubject1member.getId());
      assertEquals("F", gcGrouperSyncMembership.getInTargetDb());
      assertEquals("T", gcGrouperSyncMembership.getInTargetInsertOrExistsDb());
      assertTrue(System.currentTimeMillis() >= gcGrouperSyncMembership.getInTargetStart().getTime());
      assertNotNull(gcGrouperSyncMembership.getInTargetEnd());
      assertTrue(System.currentTimeMillis() >= gcGrouperSyncMembership.getLastUpdated().getTime());
      assertNull(gcGrouperSyncMembership.getMembershipId());
      assertNull(gcGrouperSyncMembership.getMembershipId2());
      assertNull(gcGrouperSyncMembership.getErrorMessage());
      assertNull(gcGrouperSyncMembership.getErrorTimestamp());
      
    } finally {
//      tomcatStop();
//      if (commandLineExec != null) {
//        GrouperUtil.threadJoin(commandLineExec.getThread());
//      }
    }
    
  }
  
  
  public void testAWSIncrementalSyncProvisionGroupAndThenDeleteTheGroup() {
    
    if (!tomcatRunTests()) {
      return;
    }

    ScimProvisionerTestUtils.setupAwsExternalSystem();

    ScimProvisionerTestUtils.configureScimProvisioner(new ScimProvisionerTestConfigInput()
        .assignChangelogConsumerConfigId("awsScimProvTestCLC").assignConfigId("awsProvisioner")
        .assignBearerTokenExternalSystemConfigId("awsConfigId")
        .assignEntityDeleteType("deleteEntitiesIfNotExistInGrouper")
        .assignGroupDeleteType("deleteGroupsIfGrouperDeleted")
        .assignMembershipDeleteType("deleteMembershipsIfGrouperDeleted")
        .assignScimType("AWS")
        .assignGroupAttributeCount(2)
      );

    GrouperStartup.startup();
    
    

    if (startTomcat) {
      CommandLineExec commandLineExec = tomcatStart();
    }
    
    try {
      // this will create tables
      List<GrouperScim2User> grouperScimUsers = GrouperScim2ApiCommands.retrieveScimUsers("awsConfigId", null);
  
      new GcDbAccess().connectionName("grouper").sql("delete from mock_scim_membership").executeSql();
      new GcDbAccess().connectionName("grouper").sql("delete from mock_scim_group").executeSql();
      new GcDbAccess().connectionName("grouper").sql("delete from mock_scim_user").executeSql();
      
      GrouperSession grouperSession = GrouperSession.startRootSession();
      
      assertEquals(new Integer(0), new GcDbAccess().connectionName("grouper").sql("select count(1) from mock_scim_group").select(int.class));
      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperScim2Group").list(GrouperScim2Group.class).size());
      
      GrouperProvisioningOutput grouperProvisioningOutput = fullProvision();
      GrouperUtil.sleep(2000);

      incrementalProvision();
      
      Stem stem = new StemSave(grouperSession).assignName("test").save();
      Stem stem2 = new StemSave(grouperSession).assignName("test2").save();
      
      // mark some folders to provision
      Group testGroup = new GroupSave(grouperSession).assignName("test:testGroup").save();
      Group testGroup2 = new GroupSave(grouperSession).assignName("test2:testGroup2").save();
      
      testGroup.addMember(SubjectTestHelper.SUBJ0, false);
      testGroup.addMember(SubjectTestHelper.SUBJ1, false);
      
      testGroup2.addMember(SubjectTestHelper.SUBJ2, false);
      testGroup2.addMember(SubjectTestHelper.SUBJ3, false);
      
      final GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
      attributeValue.setDirectAssignment(true);
      attributeValue.setDoProvision("awsProvisioner");
      attributeValue.setTargetName("awsProvisioner");
      attributeValue.setStemScopeString("sub");
  
      GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);
  
      assertEquals(new Integer(0), new GcDbAccess().connectionName("grouper").sql("select count(1) from mock_scim_group").select(int.class));
      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperScim2Group").list(GrouperScim2Group.class).size());
      
      long started = System.currentTimeMillis();
      incrementalProvision();
      
      assertEquals(1, HibernateSession.byHqlStatic().createQuery("from GrouperScim2Group").list(GrouperScim2Group.class).size());
      assertEquals(2, HibernateSession.byHqlStatic().createQuery("from GrouperScim2User").list(GrouperScim2User.class).size());
      assertEquals(2, HibernateSession.byHqlStatic().createQuery("from GrouperScim2Membership").list(GrouperScim2Membership.class).size());
      GrouperScim2Group grouperScim2Group = HibernateSession.byHqlStatic().createQuery("from GrouperScim2Group").list(GrouperScim2Group.class).get(0);
      
      assertEquals("testGroup", grouperScim2Group.getDisplayName());
      
      GcGrouperSync gcGrouperSync = GcGrouperSyncDao.retrieveByProvisionerName(null, "awsProvisioner");
      
      GcGrouperSyncGroup gcGrouperSyncGroup = gcGrouperSync.getGcGrouperSyncGroupDao().groupRetrieveByGroupId(testGroup.getId());
      assertEquals(testGroup.getId(), gcGrouperSyncGroup.getGroupId());
      assertEquals(testGroup.getName(), gcGrouperSyncGroup.getGroupName());
      
      assertEquals(1, gcGrouperSync.getGroupCount().intValue());
      assertEquals(2, gcGrouperSync.getUserCount().intValue());
      assertEquals(2, gcGrouperSync.getRecordsCount().intValue());
      assertTrue(started < gcGrouperSync.getLastUpdated().getTime());
      assertTrue(System.currentTimeMillis() >= gcGrouperSync.getLastUpdated().getTime());
      
      GcGrouperSyncJob gcGrouperSyncJob = gcGrouperSync.getGcGrouperSyncJobDao().jobRetrieveBySyncType("incrementalProvisionChangeLog");
      assertEquals(100, gcGrouperSyncJob.getPercentComplete().intValue());
      assertEquals(GcGrouperSyncJobState.notRunning, gcGrouperSyncJob.getJobState());
      assertTrue(started < gcGrouperSyncJob.getLastSyncTimestamp().getTime());
      assertTrue(started < gcGrouperSyncJob.getLastSyncStart().getTime());
      assertTrue(System.currentTimeMillis() >= gcGrouperSyncJob.getLastSyncTimestamp().getTime());
      assertTrue(System.currentTimeMillis() >= gcGrouperSyncJob.getLastSyncStart().getTime());
      assertTrue(started < gcGrouperSyncJob.getLastTimeWorkWasDone().getTime());
      assertTrue(System.currentTimeMillis() >= gcGrouperSyncJob.getLastTimeWorkWasDone().getTime());
      assertTrue(started < gcGrouperSyncJob.getHeartbeat().getTime());
      assertTrue(System.currentTimeMillis() >= gcGrouperSyncJob.getHeartbeat().getTime());
      assertTrue(started < gcGrouperSyncJob.getLastUpdated().getTime());
      assertTrue(System.currentTimeMillis() >= gcGrouperSyncJob.getLastUpdated().getTime());
      assertNull(gcGrouperSyncJob.getErrorMessage());
      assertNull(gcGrouperSyncJob.getErrorTimestamp());
      
      assertEquals(testGroup.getId(), gcGrouperSyncGroup.getGroupId());
      assertEquals(testGroup.getName(), gcGrouperSyncGroup.getGroupName());
      assertEquals(testGroup.getIdIndex(), gcGrouperSyncGroup.getGroupIdIndex());
      assertEquals("T", gcGrouperSyncGroup.getProvisionableDb());
      assertEquals("T", gcGrouperSyncGroup.getInTargetDb());
      assertEquals("T", gcGrouperSyncGroup.getInTargetInsertOrExistsDb());
      assertTrue(started < gcGrouperSyncGroup.getInTargetStart().getTime());
      assertTrue(System.currentTimeMillis() >= gcGrouperSyncGroup.getInTargetStart().getTime());
      assertNull(gcGrouperSyncGroup.getInTargetEnd());
      assertTrue(started < gcGrouperSyncGroup.getProvisionableStart().getTime());
      assertTrue(System.currentTimeMillis() >= gcGrouperSyncGroup.getProvisionableStart().getTime());
      assertNull(gcGrouperSyncGroup.getProvisionableEnd());
      assertTrue(started < gcGrouperSyncGroup.getLastUpdated().getTime());
      assertTrue(System.currentTimeMillis() >= gcGrouperSyncGroup.getLastUpdated().getTime());
      assertNotNull(gcGrouperSyncGroup.getGroupAttributeValueCache2());
      assertNull(gcGrouperSyncGroup.getGroupAttributeValueCache0());
      assertNull(gcGrouperSyncGroup.getGroupAttributeValueCache1());
      assertNull(gcGrouperSyncGroup.getGroupAttributeValueCache3());
      assertNull(gcGrouperSyncGroup.getLastGroupMetadataSync());
      assertNull(gcGrouperSyncGroup.getErrorMessage());
      assertNull(gcGrouperSyncGroup.getErrorTimestamp());
      assertNull(gcGrouperSyncGroup.getLastGroupSync());
      
      Member testSubject0member = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ0, true);
      Member testSubject1member = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ1, true);
      
      GcGrouperSyncMember gcGrouperSyncMember = gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberId(testSubject0member.getId());
      assertEquals(testSubject0member.getId(), gcGrouperSyncMember.getMemberId());
      assertEquals(testSubject0member.getSubjectId(), gcGrouperSyncMember.getSubjectId());
      assertEquals(testSubject0member.getSubjectSourceId(), gcGrouperSyncMember.getSourceId());
      assertEquals(testSubject0member.getSubjectIdentifier0(), gcGrouperSyncMember.getSubjectIdentifier());
      assertEquals("T", gcGrouperSyncMember.getProvisionableDb());
      assertEquals("T", gcGrouperSyncMember.getInTargetDb());
      assertEquals("T", gcGrouperSyncMember.getInTargetInsertOrExistsDb());
      assertTrue(started < gcGrouperSyncMember.getInTargetStart().getTime());
      assertNull(gcGrouperSyncMember.getInTargetEnd());
      assertTrue(started < gcGrouperSyncMember.getProvisionableStart().getTime());
      assertTrue(System.currentTimeMillis() >= gcGrouperSyncMember.getProvisionableStart().getTime());
      assertNull(gcGrouperSyncMember.getProvisionableEnd());
      assertTrue(started < gcGrouperSyncMember.getLastUpdated().getTime());
      assertTrue(System.currentTimeMillis() >= gcGrouperSyncMember.getLastUpdated().getTime());
      assertNull(gcGrouperSyncMember.getEntityAttributeValueCache0());
      assertNull(gcGrouperSyncMember.getEntityAttributeValueCache1());
      assertNotNull(gcGrouperSyncMember.getEntityAttributeValueCache2());
      assertNull(gcGrouperSyncMember.getEntityAttributeValueCache3());
      assertNull(gcGrouperSyncMember.getLastUserMetadataSync());
      assertNull(gcGrouperSyncMember.getErrorMessage());
      assertNull(gcGrouperSyncMember.getErrorTimestamp());
      assertNull(gcGrouperSyncMember.getLastUserSync());

      gcGrouperSyncMember = gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberId(testSubject1member.getId());
      assertEquals(testSubject1member.getId(), gcGrouperSyncMember.getMemberId());
      assertEquals(testSubject1member.getSubjectId(), gcGrouperSyncMember.getSubjectId());
      assertEquals(testSubject1member.getSubjectSourceId(), gcGrouperSyncMember.getSourceId());
      assertEquals(testSubject1member.getSubjectIdentifier0(), gcGrouperSyncMember.getSubjectIdentifier());
      assertEquals("T", gcGrouperSyncMember.getProvisionableDb());
      assertEquals("T", gcGrouperSyncMember.getInTargetDb());
      assertEquals("T", gcGrouperSyncMember.getInTargetInsertOrExistsDb());
      assertTrue(started < gcGrouperSyncMember.getInTargetStart().getTime());
      assertNull(gcGrouperSyncMember.getInTargetEnd());
      assertTrue(started < gcGrouperSyncMember.getProvisionableStart().getTime());
      assertTrue(System.currentTimeMillis() >= gcGrouperSyncMember.getProvisionableStart().getTime());
      assertNull(gcGrouperSyncMember.getProvisionableEnd());
      assertTrue(started < gcGrouperSyncMember.getLastUpdated().getTime());
      assertTrue(System.currentTimeMillis() >= gcGrouperSyncMember.getLastUpdated().getTime());
      assertNull(gcGrouperSyncMember.getEntityAttributeValueCache0());
      assertNull(gcGrouperSyncMember.getEntityAttributeValueCache1());
      assertNotNull(gcGrouperSyncMember.getEntityAttributeValueCache2());
      assertNull(gcGrouperSyncMember.getEntityAttributeValueCache3());
      assertNull(gcGrouperSyncMember.getLastUserMetadataSync());
      assertNull(gcGrouperSyncMember.getErrorMessage());
      assertNull(gcGrouperSyncMember.getErrorTimestamp());
      assertNull(gcGrouperSyncMember.getLastUserSync());
      
      GcGrouperSyncMembership gcGrouperSyncMembership = gcGrouperSync.getGcGrouperSyncMembershipDao().membershipRetrieveByGroupIdAndMemberId(testGroup.getId(), testSubject0member.getId());
      assertEquals("T", gcGrouperSyncMembership.getInTargetDb());
      assertEquals("T", gcGrouperSyncMembership.getInTargetInsertOrExistsDb());
      assertTrue(started < gcGrouperSyncMembership.getInTargetStart().getTime());
      assertTrue(System.currentTimeMillis() >= gcGrouperSyncMembership.getInTargetStart().getTime());
      assertNull(gcGrouperSyncMembership.getInTargetEnd());
      assertTrue(started < gcGrouperSyncMembership.getLastUpdated().getTime());
      assertTrue(System.currentTimeMillis() >= gcGrouperSyncMembership.getLastUpdated().getTime());
      assertNull(gcGrouperSyncMembership.getMembershipId());
      assertNull(gcGrouperSyncMembership.getMembershipId2());
      assertNull(gcGrouperSyncMembership.getErrorMessage());
      assertNull(gcGrouperSyncMembership.getErrorTimestamp());
      
      gcGrouperSyncMembership = gcGrouperSync.getGcGrouperSyncMembershipDao().membershipRetrieveByGroupIdAndMemberId(testGroup.getId(), testSubject1member.getId());
      assertEquals("T", gcGrouperSyncMembership.getInTargetDb());
      assertEquals("T", gcGrouperSyncMembership.getInTargetInsertOrExistsDb());
      assertTrue(started < gcGrouperSyncMembership.getInTargetStart().getTime());
      assertTrue(System.currentTimeMillis() >= gcGrouperSyncMembership.getInTargetStart().getTime());
      assertNull(gcGrouperSyncMembership.getInTargetEnd());
      assertTrue(started < gcGrouperSyncMembership.getLastUpdated().getTime());
      assertTrue(System.currentTimeMillis() >= gcGrouperSyncMembership.getLastUpdated().getTime());
      assertNull(gcGrouperSyncMembership.getMembershipId());
      assertNull(gcGrouperSyncMembership.getMembershipId2());
      assertNull(gcGrouperSyncMembership.getErrorMessage());
      assertNull(gcGrouperSyncMembership.getErrorTimestamp());
      
      //now remove one of the subjects from the testGroup
      testGroup.deleteMember(SubjectTestHelper.SUBJ1);
      
      incrementalProvision();
      
      assertEquals(1, HibernateSession.byHqlStatic().createQuery("from GrouperScim2Group").list(GrouperScim2Group.class).size());
      assertEquals(1, HibernateSession.byHqlStatic().createQuery("from GrouperScim2User").list(GrouperScim2User.class).size());
      assertEquals(1, HibernateSession.byHqlStatic().createQuery("from GrouperScim2Membership").list(GrouperScim2Membership.class).size());
      
      
      //now add a subject to test group
      testGroup.addMember(SubjectTestHelper.SUBJ3, false);
      incrementalProvision();
      assertEquals(1, HibernateSession.byHqlStatic().createQuery("from GrouperScim2Group").list(GrouperScim2Group.class).size());
      assertEquals(2, HibernateSession.byHqlStatic().createQuery("from GrouperScim2User").list(GrouperScim2User.class).size());
      assertEquals(2, HibernateSession.byHqlStatic().createQuery("from GrouperScim2Membership").list(GrouperScim2Membership.class).size());
      
      //now delete the group and sync again
      testGroup.delete();
      
      incrementalProvision();
      
      //assertEquals(1, grouperProvisioningOutput.getDelete());
      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperScim2Group").list(GrouperScim2Group.class).size());
      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperScim2User").list(GrouperScim2User.class).size());
      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperScim2Membership").list(GrouperScim2Membership.class).size());
      
    } finally {
//      tomcatStop();
//      if (commandLineExec != null) {
//        GrouperUtil.threadJoin(commandLineExec.getThread());
//      }
    }
    
  }
  
}
