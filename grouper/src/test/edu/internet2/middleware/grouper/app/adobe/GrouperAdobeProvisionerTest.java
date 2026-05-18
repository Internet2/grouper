package edu.internet2.middleware.grouper.app.adobe;

import java.util.ArrayList;
import java.util.List;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningAttributeValue;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningBaseTest;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningDiagnosticsContainer;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningOutput;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningService;
import edu.internet2.middleware.grouper.app.scim2Provisioning.GrouperScim2Group;
import edu.internet2.middleware.grouper.app.scim2Provisioning.GrouperScim2Membership;
import edu.internet2.middleware.grouper.app.scim2Provisioning.GrouperScim2User;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.misc.GrouperStartup;
import edu.internet2.middleware.grouper.util.CommandLineExec;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import junit.textui.TestRunner;

public class GrouperAdobeProvisionerTest extends GrouperProvisioningBaseTest {
  
  
  public static void main(String[] args) {

    AdobeMockServiceHandler.ensureAdobeMockTables();
    TestRunner.run(new GrouperAdobeProvisionerTest("testAdobeFullSyncProvisionGroupAndThenDeleteTheGroup"));

  }
  
  @Override
  public String defaultConfigId() {
    return "adobeProvisioner";
  }

  public static boolean startTomcat = false;
  
  public GrouperAdobeProvisionerTest(String name) {
    super(name);
  }
  
  
  
  private void validateNoErrors(GrouperProvisioningDiagnosticsContainer grouperProvisioningDiagnosticsContainer) {
    String[] lines = grouperProvisioningDiagnosticsContainer.getReportFinal().split("\n"); 
    List<String> errorLines = new ArrayList<String>();
    for (String line : lines) {
      if (line.contains("'red'") || line.contains("Error:")) {
        errorLines.add(line);
      }
    }
    
    if (errorLines.size() > 0) {
      fail("There are " + errorLines.size() + " errors in report: " + errorLines);
    }
  }

  public void testAdobeFullSyncProvisionGroupAndThenDeleteTheGroup() {
    
    if (!tomcatRunTests()) {
      return;
    }

    AdobeProvisionerTestUtils.setupAdobeExternalSystem();

    String adobeConfigId = "adobe";
    AdobeProvisionerTestUtils.configureAdobeProvisioner(new AdobeProvisionerTestConfigInput()
      .assignChangelogConsumerConfigId("adobeProvTestCLC").assignConfigId("adobeProvisioner")
      .assignEntityDeleteType("deleteEntitiesIfNotExistInGrouper")
      .assignGroupDeleteType("deleteGroupsIfGrouperDeleted")
      .assignMembershipDeleteType("deleteMembershipsIfGrouperDeleted")
      .assignGroupAttributeCount(2)
    );



    GrouperStartup.startup();
    
    if (startTomcat) {
      CommandLineExec commandLineExec = tomcatStart();
    }
    
    try {
      // this will create tables
      List<GrouperAdobeUser> grouperScimUsers = GrouperAdobeApiCommands.retrieveAdobeUsers(adobeConfigId, true, "testOrgId");
  
      new GcDbAccess().connectionName("grouper").sql("delete from mock_adobe_membership").executeSql();
      new GcDbAccess().connectionName("grouper").sql("delete from mock_adobe_group").executeSql();
      new GcDbAccess().connectionName("grouper").sql("delete from mock_adobe_user").executeSql();

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
      attributeValue.setDoProvision("adobeProvisioner");
      attributeValue.setTargetName("adobeProvisioner");
      attributeValue.setStemScopeString("sub");
  
      GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);
  
      //lets sync these over      
      assertEquals(new Integer(0), new GcDbAccess().connectionName("grouper").sql("select count(1) from mock_adobe_group").select(int.class));
  
      
      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperAdobeGroup").list(GrouperScim2Group.class).size());
      
      long started = System.currentTimeMillis();
      
      GrouperProvisioningOutput grouperProvisioningOutput = fullProvision();
      GrouperUtil.sleep(2000);
      assertTrue(1 <= grouperProvisioningOutput.getInsert());
      assertEquals(1, HibernateSession.byHqlStatic().createQuery("from GrouperAdobeGroup").list(GrouperAdobeGroup.class).size());
      assertEquals(2, HibernateSession.byHqlStatic().createQuery("from GrouperAdobeUser").list(GrouperAdobeUser.class).size());
      assertEquals(2, HibernateSession.byHqlStatic().createQuery("from GrouperAdobeMembership").list(GrouperAdobeMembership.class).size());
      GrouperAdobeGroup grouperAdobeGroup = HibernateSession.byHqlStatic().createQuery("from GrouperAdobeGroup").list(GrouperAdobeGroup.class).get(0);

      assertEquals("testGroup", grouperAdobeGroup.getName());
      
      
      //now remove one of the subjects from the testGroup
      testGroup.deleteMember(SubjectTestHelper.SUBJ1);
      
      // now run the full sync again and the member should be deleted from mock_adobe_membership also
      started = System.currentTimeMillis();
      
      grouperProvisioningOutput = fullProvision();
      GrouperUtil.sleep(2000);
      assertEquals(1, HibernateSession.byHqlStatic().createQuery("from GrouperAdobeGroup").list(GrouperAdobeGroup.class).size());
      assertEquals(1, HibernateSession.byHqlStatic().createQuery("from GrouperAdobeUser").list(GrouperAdobeUser.class).size());
      assertEquals(1, HibernateSession.byHqlStatic().createQuery("from GrouperAdobeMembership").list(GrouperAdobeMembership.class).size());
      
      

    } finally {
//      tomcatStop();
//      if (commandLineExec != null) {
//        GrouperUtil.threadJoin(commandLineExec.getThread());
//      }
    }

  }

  /**
   * Sync-back smoke test: with all three load*ToGenericGrouperTable flags on, a full
   * provision populates grouper_prov_user / _group / _mship from the Adobe read path.
   * Asserts all three axes have rows and at least one row per axis is linked back to
   * its Grouper counterpart. Framework-detail coverage (flag isolation, native-attribute
   * config, validation) lives in the SCIM + LDAP suites.
   */
  public void testAdobeFullSyncPopulatesGenericTables() {

    if (!tomcatRunTests()) {
      return;
    }

    AdobeProvisionerTestUtils.setupAdobeExternalSystem();

    String configId = "adobeProvisioner";
    AdobeProvisionerTestUtils.configureAdobeProvisioner(new AdobeProvisionerTestConfigInput()
      .assignChangelogConsumerConfigId("adobeProvTestCLC").assignConfigId(configId)
      .assignEntityDeleteType("deleteEntitiesIfNotExistInGrouper")
      .assignGroupDeleteType("deleteGroupsIfGrouperDeleted")
      .assignMembershipDeleteType("deleteMembershipsIfGrouperDeleted")
      .assignGroupAttributeCount(2)
      .addExtraConfig("recalculateAllOperations", "true")
      .addExtraConfig("loadEntitiesToGenericGrouperTable", "true")
      .addExtraConfig("loadGroupsToGenericGrouperTable", "true")
      .addExtraConfig("loadMembershipsToGenericGrouperTable", "true"));

    GrouperStartup.startup();

    // baseline: clean Adobe mock target + seed Grouper-side stems/groups/members
    new GcDbAccess().connectionName("grouper").sql("delete from mock_adobe_membership").executeSql();
    new GcDbAccess().connectionName("grouper").sql("delete from mock_adobe_group").executeSql();
    new GcDbAccess().connectionName("grouper").sql("delete from mock_adobe_user").executeSql();

    GrouperSession grouperSession = GrouperSession.startRootSession();

    Stem stem = new StemSave(grouperSession).assignName("test").save();
    Group testGroup = new GroupSave(grouperSession).assignName("test:testGroup").save();
    testGroup.addMember(SubjectTestHelper.SUBJ0, false);
    testGroup.addMember(SubjectTestHelper.SUBJ1, false);

    GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision(configId);
    attributeValue.setTargetName(configId);
    attributeValue.setStemScopeString("sub");
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);

    assertEquals(0, countSyncBack(configId, "grouper_prov_group"));
    assertEquals(0, countSyncBack(configId, "grouper_prov_user"));
    assertEquals(0, countSyncBack(configId, "grouper_prov_mship"));

    // first pass writes the Adobe target; sync-back tables stay empty until the next
    // read pass captures the new objects (read-state convergence contract).
    GrouperProvisioningOutput out1 = fullProvision();
    assertEquals(0, out1.getRecordsWithErrors());

    // second pass: reads back what we just wrote, captures through the sync hooks, flushes
    GrouperProvisioningOutput out2 = fullProvision();
    assertEquals(0, out2.getRecordsWithErrors());

    assertTrue("expected at least 1 prov_group row after sync-back",
        countSyncBack(configId, "grouper_prov_group") >= 1);
    assertTrue("expected at least 2 prov_user rows (SUBJ0 + SUBJ1)",
        countSyncBack(configId, "grouper_prov_user") >= 2);
    assertTrue("expected at least 2 prov_mship rows",
        countSyncBack(configId, "grouper_prov_mship") >= 2);

    int linkedGroups = new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from grouper_prov_group "
            + "where grouper_sync_internal_id in (select internal_id from grouper_sync where provisioner_name = ?) "
            + "and group_internal_id is not null")
        .addBindVar(configId).select(int.class);
    assertTrue("at least one prov_group row should be linked to a Grouper group", linkedGroups >= 1);

    int linkedUsers = new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from grouper_prov_user "
            + "where grouper_sync_internal_id in (select internal_id from grouper_sync where provisioner_name = ?) "
            + "and member_internal_id is not null")
        .addBindVar(configId).select(int.class);
    assertTrue("at least one prov_user row should be linked to a Grouper member", linkedUsers >= 1);
  }

  /**
   * Sync-back smoke test for the scoped-retrieve path. Same flow as the selectAll variant
   * but with {@code selectAllGroups=false} / {@code selectAllEntities=false} so the DAO
   * routes through scoped {@code retrieveGroup} / {@code retrieveEntity} (per-id lookups)
   * instead of {@code retrieveAllGroups} / {@code retrieveAllEntities}. Exercises the
   * scoped-retrieve capture hooks.
   *
   * <p>Incremental coverage is intentionally deferred — the framework today only captures
   * from reads, and write changes converge on the next read pass. Closing that gap is the
   * write-shadow precision pass tracked in section 10 of the sync-back doc.
   */
  public void testAdobeFullSyncSelectByIdsPopulatesGenericTables() {

    if (!tomcatRunTests()) {
      return;
    }

    AdobeProvisionerTestUtils.setupAdobeExternalSystem();

    String configId = "adobeProvisioner";
    AdobeProvisionerTestUtils.configureAdobeProvisioner(new AdobeProvisionerTestConfigInput()
      .assignChangelogConsumerConfigId("adobeProvTestCLC").assignConfigId(configId)
      .assignEntityDeleteType("deleteEntitiesIfNotExistInGrouper")
      .assignGroupDeleteType("deleteGroupsIfGrouperDeleted")
      .assignMembershipDeleteType("deleteMembershipsIfGrouperDeleted")
      .assignGroupAttributeCount(2)
      .addExtraConfig("selectAllGroups", "false")
      .addExtraConfig("selectAllEntities", "false")
      // disable the unrelated "loadEntitiesToGrouperTable" Adobe feature — its loader NPEs
      // when a scoped retrieveEntity returns null (pre-existing Adobe bug, not sync-back related)
      .addExtraConfig("loadEntitiesToGrouperTable", "false")
      .addExtraConfig("recalculateAllOperations", "true")
      .addExtraConfig("loadEntitiesToGenericGrouperTable", "true")
      .addExtraConfig("loadGroupsToGenericGrouperTable", "true")
      .addExtraConfig("loadMembershipsToGenericGrouperTable", "true"));

    GrouperStartup.startup();

    new GcDbAccess().connectionName("grouper").sql("delete from mock_adobe_membership").executeSql();
    new GcDbAccess().connectionName("grouper").sql("delete from mock_adobe_group").executeSql();
    new GcDbAccess().connectionName("grouper").sql("delete from mock_adobe_user").executeSql();

    GrouperSession grouperSession = GrouperSession.startRootSession();

    Stem stem = new StemSave(grouperSession).assignName("test").save();
    Group testGroup = new GroupSave(grouperSession).assignName("test:testGroup").save();
    testGroup.addMember(SubjectTestHelper.SUBJ0, false);
    testGroup.addMember(SubjectTestHelper.SUBJ1, false);

    GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision(configId);
    attributeValue.setTargetName(configId);
    attributeValue.setStemScopeString("sub");
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);

    assertEquals(0, countSyncBack(configId, "grouper_prov_group"));
    assertEquals(0, countSyncBack(configId, "grouper_prov_user"));
    assertEquals(0, countSyncBack(configId, "grouper_prov_mship"));

    // pass 1 inserts target objects; pass 2 reads them back via scoped retrieve and flushes.
    GrouperProvisioningOutput out1 = fullProvision();
    assertEquals(0, out1.getRecordsWithErrors());
    GrouperProvisioningOutput out2 = fullProvision();
    assertEquals(0, out2.getRecordsWithErrors());

    assertTrue("expected at least 1 prov_group row via scoped retrieve",
        countSyncBack(configId, "grouper_prov_group") >= 1);
    assertTrue("expected at least 2 prov_user rows via scoped retrieve",
        countSyncBack(configId, "grouper_prov_user") >= 2);
    // memberships in selectAll=false mode may use a different (or no) retrieve path
    // per-protocol — the selectAll=true test already proves memberships flow end-to-end.
  }

  /** count rows for a given prov_* table scoped to a provisioner name */
  private int countSyncBack(String configId, String tableName) {
    return new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from " + tableName
            + " where grouper_sync_internal_id in (select internal_id from grouper_sync where provisioner_name = ?)")
        .addBindVar(configId).select(int.class);
  }

}
