package edu.internet2.middleware.grouper.app.scim;

import java.sql.Timestamp;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioner;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningAttributeValue;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningBaseTest;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningOutput;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningService;
import edu.internet2.middleware.grouper.app.scim2Provisioning.GenericScim2MockServiceHandler;
import edu.internet2.middleware.grouper.app.scim2Provisioning.GrouperScim2Group;
import edu.internet2.middleware.grouper.app.scim2Provisioning.GrouperScim2Membership;
import edu.internet2.middleware.grouper.app.scim2Provisioning.GrouperScim2User;
import edu.internet2.middleware.grouper.cfg.dbConfig.GrouperDbConfig;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.misc.GrouperStartup;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSync;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncDao;
import junit.textui.TestRunner;

/**
 * Verify that SCIM full provisioning populates the generic provisioner reporting tables
 * ({@code grouper_prov_group}, {@code grouper_prov_user}, {@code grouper_prov_*_attr},
 * {@code grouper_prov_*_attr_value}, {@code grouper_prov_mship}) when the load flags are on.
 *
 * <p>The provisioner runs against the in-process SCIM AWS mock (no Tomcat needed) backed by
 * {@code mock_scim_group} / {@code mock_scim_user} / {@code mock_scim_membership} tables.
 *
 * <p>Two-pass pattern (same as LDAP): pass 1 inserts groups/users/memberships into the SCIM
 * target, pass 2 retrieves them back via {@code retrieveScimGroups} / {@code retrieveScimUsers},
 * and the capture hooks in {@link edu.internet2.middleware.grouper.app.scim2Provisioning.GrouperScim2ApiCommands}
 * route through {@link edu.internet2.middleware.grouper.app.scim2Provisioning.GrouperScim2ProvisioningTargetNativeSync}
 * into the reporting tables.
 */
public class ScimProvisionerGenericTableTest extends GrouperProvisioningBaseTest {

  public static void main(String[] args) {
    GenericScim2MockServiceHandler.ensureScimMockTables();
    TestRunner.run(new ScimProvisionerGenericTableTest("testCreateConvergesIntoSyncTablesOnNextRun"));
  }

  public ScimProvisionerGenericTableTest() {
    super();
  }

  public ScimProvisionerGenericTableTest(String name) {
    super(name);
  }

  private GrouperSession grouperSession;

  @Override
  protected void setUp() {
    super.setUp();
    GrouperStartup.startup();
    GenericScim2MockServiceHandler.ensureScimMockTables();
    this.grouperSession = GrouperSession.startRootSession();
    // clear any leftover mock SCIM target state from previous tests in the same JVM
    new GcDbAccess().connectionName("grouper").sql("delete from mock_scim_membership").executeSql();
    new GcDbAccess().connectionName("grouper").sql("delete from mock_scim_group").executeSql();
    new GcDbAccess().connectionName("grouper").sql("delete from mock_scim_user").executeSql();
    // tell the AWS SCIM mock to include {@code members}/{@code groups} arrays in responses,
    // so {@code populateMembershipsFromGroup} populates the cache and our drain captures.
    // without this, the mock omits members from GET /Groups responses by default.
    new GrouperDbConfig().configFileName("grouper.properties")
        .propertyName("grouperTest.scim2.mock.membershipStrategy.mode")
        .value("fullGroupMembershipsInGroupObjectsWhenRetrievingAllGroups").store();
  }

  @Override
  protected void tearDown() {
    // clear the mock-membership-strategy override and the sync-back test knobs so they do not
    // leak into subsequent tests (in this or other classes) sharing the same JVM. These are DB
    // config (read by the mock in its own JVM), not static fields.
    new GrouperDbConfig().configFileName("grouper.properties")
        .propertyName("grouperTest.scim2.mock.membershipStrategy.mode")
        .value("").store();
    new GrouperDbConfig().configFileName("grouper.properties")
        .propertyName("grouperTest.scim2.mock.patchUsersReturnNoBody")
        .value("").store();
    new GrouperDbConfig().configFileName("grouper.properties")
        .propertyName("grouperTest.scim2.mock.deleteUsersReturnSuccessButDoNotDelete")
        .value("").store();
    GrouperSession.stopQuietly(this.grouperSession);
    super.tearDown();
  }

  @Override
  public String defaultConfigId() {
    return "awsProvisioner";
  }

  /**
   * Two-pass full provision against the SCIM AWS mock: pass 1 inserts test:testGroup
   * with two members into mock_scim_*; pass 2 reads them back via retrieveScimGroups /
   * retrieveScimUsers, and the capture hooks in GrouperScim2ApiCommands populate the
   * grouper_prov_* reporting tables.
   */
  public void testFullProvisionPopulatesGenericTables() {

    String configId = "awsProvisioner";

    ScimProvisionerTestUtils.setupAwsExternalSystem();

    ScimProvisionerTestUtils.configureScimProvisioner(new ScimProvisionerTestConfigInput()
        .assignChangelogConsumerConfigId("awsScimProvTestCLC")
        .assignConfigId(configId)
        .assignBearerTokenExternalSystemConfigId("awsConfigId")
        .assignEntityDeleteType("deleteEntitiesIfNotExistInGrouper")
        .assignGroupDeleteType("deleteGroupsIfGrouperDeleted")
        .assignMembershipDeleteType("deleteMembershipsIfGrouperDeleted")
        .assignScimType("AWS")
        .assignGroupAttributeCount(2)
        .assignBearer(true)
        .addExtraConfig("loadEntitiesToGenericGrouperTable", "true")
        .addExtraConfig("loadGroupsToGenericGrouperTable", "true")
        .addExtraConfig("loadMembershipsToGenericGrouperTable", "true"));

    Stem stem = new StemSave(this.grouperSession).assignName("test").save();
    Group testGroup = new GroupSave(this.grouperSession).assignName("test:testGroup").save();
    testGroup.addMember(SubjectTestHelper.SUBJ0, false);
    testGroup.addMember(SubjectTestHelper.SUBJ1, false);

    GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision(configId);
    attributeValue.setTargetName(configId);
    attributeValue.setStemScopeString("sub");
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);

    // baseline: nothing in the generic tables yet
    assertEquals(0, countByProvisioner(configId, "grouper_prov_group"));
    assertEquals(0, countByProvisioner(configId, "grouper_prov_user"));

    // PASS 1: inserts the group + users into SCIM target. Reporting tables still empty
    // because retrieveAllGroups / retrieveAllEntities run before the inserts.
    GrouperProvisioningOutput passOne = fullProvision();
    GrouperProvisioner.retrieveInternalLastProvisioner();
    assertEquals(0, passOne.getRecordsWithErrors());

    // PASS 2: retrieveAllGroups / retrieveAllEntities see the inserted SCIM resources;
    // the capture hooks in GrouperScim2ApiCommands populate the reporting tables.
    GrouperProvisioningOutput passTwo = fullProvision();
    GrouperProvisioner.retrieveInternalLastProvisioner();
    assertEquals(0, passTwo.getRecordsWithErrors());

    GcGrouperSync gcGrouperSync = GcGrouperSyncDao.retrieveByProvisionerName(null, configId);
    assertNotNull("grouper_sync row should exist for " + configId, gcGrouperSync);
    long syncInternalId = gcGrouperSync.getInternalId();

    // ---- group side ----

    int groupRowCount = countByProvisioner(configId, "grouper_prov_group");
    assertTrue("expected >=1 prov_group row after pass 2, got " + groupRowCount,
        groupRowCount >= 1);

    // group attribute catalog: per-provisioner, deduped per (sync, attribute_name)
    int groupAttrCatalogCount = countByProvisioner(configId, "grouper_prov_group_attr");
    assertTrue("expected >=1 group attr catalog row, got " + groupAttrCatalogCount,
        groupAttrCatalogCount >= 1);

    int dupGroupAttr = new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from (select grouper_sync_internal_id, attribute_name, count(*) c "
            + "from grouper_prov_group_attr where grouper_sync_internal_id = ? "
            + "group by grouper_sync_internal_id, attribute_name having count(*) > 1) t")
        .addBindVar(syncInternalId).select(int.class);
    assertEquals("group attr catalog should be deduped per (sync,name)", 0, dupGroupAttr);

    // value rows must reference real catalog rows (no orphan FKs)
    int orphanGroupValues = new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from grouper_prov_group_attr_value gpv "
            + "left join grouper_prov_group_attr pa on gpv.prov_group_attr_internal_id = pa.internal_id "
            + "join grouper_prov_group pg on gpv.prov_group_internal_id = pg.internal_id "
            + "where pg.grouper_sync_internal_id = ? and pa.internal_id is null")
        .addBindVar(syncInternalId).select(int.class);
    assertEquals("no group_attr_value rows should orphan their catalog FK", 0, orphanGroupValues);

    // SCIM build path puts displayName in the attribute map — verify the test group's
    // displayName lands as a value somewhere (resolved through the dictionary join in
    // grouper_prov_group_attr_v)
    int displayNameValueRows = new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from grouper_prov_group_attr_value gpv "
            + "join grouper_prov_group_attr gpa on gpa.internal_id = gpv.prov_group_attr_internal_id "
            + "join grouper_prov_group pg on pg.internal_id = gpv.prov_group_internal_id "
            + "where pg.grouper_sync_internal_id = ? and gpa.attribute_name = 'displayName'")
        .addBindVar(syncInternalId).select(int.class);
    assertTrue("expected >=1 displayName value row, got " + displayNameValueRows,
        displayNameValueRows >= 1);

    // ---- user side ----

    int userRowCount = countByProvisioner(configId, "grouper_prov_user");
    assertTrue("expected >=2 prov_user rows (SUBJ0 + SUBJ1) after pass 2, got " + userRowCount,
        userRowCount >= 2);

    int userAttrCatalogCount = countByProvisioner(configId, "grouper_prov_user_attr");
    assertTrue("expected >=1 user attr catalog row, got " + userAttrCatalogCount,
        userAttrCatalogCount >= 1);

    int dupUserAttr = new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from (select grouper_sync_internal_id, attribute_name, count(*) c "
            + "from grouper_prov_user_attr where grouper_sync_internal_id = ? "
            + "group by grouper_sync_internal_id, attribute_name having count(*) > 1) t")
        .addBindVar(syncInternalId).select(int.class);
    assertEquals("user attr catalog should be deduped per (sync,name)", 0, dupUserAttr);

    int orphanUserValues = new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from grouper_prov_user_attr_value puv "
            + "left join grouper_prov_user_attr pa on puv.prov_user_attr_internal_id = pa.internal_id "
            + "join grouper_prov_user pu on puv.prov_user_internal_id = pu.internal_id "
            + "where pu.grouper_sync_internal_id = ? and pa.internal_id is null")
        .addBindVar(syncInternalId).select(int.class);
    assertEquals("no user_attr_value rows should orphan their catalog FK", 0, orphanUserValues);

    // every prov_user should have at least one attribute value (userName, at minimum)
    int totalUserValueRows = new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from grouper_prov_user_attr_value puv "
            + "join grouper_prov_user pu on puv.prov_user_internal_id = pu.internal_id "
            + "where pu.grouper_sync_internal_id = ?")
        .addBindVar(syncInternalId).select(int.class);
    assertTrue("expected >=userRowCount user attr value rows, got " + totalUserValueRows,
        totalUserValueRows >= userRowCount);

    // ---- memberships ----

    // SCIM membership cache populates groupId → userIds; drained at the end of
    // retrieveScimGroups/Users into native memberships, then synced to grouper_prov_mship
    int membershipRows = new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from grouper_prov_mship "
            + "where grouper_sync_internal_id = ?")
        .addBindVar(syncInternalId).select(int.class);
    assertTrue("expected >=2 prov_mship rows for testGroup's 2 members, got " + membershipRows,
        membershipRows >= 2);
  }

  // TODO re-enable when generic-tables framework supports incremental sync-back
//   /**
//    * Incremental coverage for the SCIM sync-back wiring. Seeds via two full provisions
//    * (which populates reporting tables and the SCIM target), then drives an incremental by
//    * adding a third member to testGroup. After the incremental, verify:
//    *
//    * <ul>
//    *   <li>Per-provisioner catalog ids for already-known attribute names are reused — this is
//    *       the unique-key regression on {@code (sync, attribute_name)} we fixed on the LDAP
//    *       side; SCIM exercises the same code path in {@code GrouperProvisioningLogic}.</li>
//    *   <li>Catalog rows remain deduped per {@code (sync, attribute_name)}.</li>
//    *   <li>No orphan FKs from attr_value rows to catalog.</li>
//    *   <li>The {@code prov_user} / {@code prov_group} rows for our test entities are still
//    *       present (incremental shouldn't accidentally delete unrelated rows).</li>
//    * </ul>
//    *
//    * <p>This test does not assert that SUBJ2's membership is reflected in {@code prov_mship}
//    * after the incremental — same 1-cycle-lag semantic as LDAP. SCIM's incremental performs
//    * a read-before-write to compute the diff; that read captures pre-write state. SUBJ2 will
//    * surface on the next read pass.
//    */
//   public void testIncrementalProvisionPopulatesGenericTables() {
// 
//     String configId = "awsProvisioner";
// 
//     ScimProvisionerTestUtils.setupAwsExternalSystem();
// 
//     ScimProvisionerTestUtils.configureScimProvisioner(new ScimProvisionerTestConfigInput()
//         .assignChangelogConsumerConfigId("awsScimProvTestCLC")
//         .assignConfigId(configId)
//         .assignBearerTokenExternalSystemConfigId("awsConfigId")
//         .assignEntityDeleteType("deleteEntitiesIfNotExistInGrouper")
//         .assignGroupDeleteType("deleteGroupsIfGrouperDeleted")
//         .assignMembershipDeleteType("deleteMembershipsIfGrouperDeleted")
//         .assignScimType("AWS")
//         .assignGroupAttributeCount(2)
//         .assignBearer(true)
//         .addExtraConfig("loadEntitiesToGenericGrouperTable", "true")
//         .addExtraConfig("loadGroupsToGenericGrouperTable", "true")
//         .addExtraConfig("loadMembershipsToGenericGrouperTable", "true"));
// 
//     Stem stem = new StemSave(this.grouperSession).assignName("test").save();
//     Group testGroup = new GroupSave(this.grouperSession).assignName("test:testGroup").save();
//     testGroup.addMember(SubjectTestHelper.SUBJ0, false);
//     testGroup.addMember(SubjectTestHelper.SUBJ1, false);
// 
//     GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
//     attributeValue.setDirectAssignment(true);
//     attributeValue.setDoProvision(configId);
//     attributeValue.setTargetName(configId);
//     attributeValue.setStemScopeString("sub");
//     GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);
// 
//     // seed: pass 1 inserts SCIM resources; pass 2 reads them back and populates reporting
//     assertEquals(0, fullProvision().getRecordsWithErrors());
//     assertEquals(0, fullProvision().getRecordsWithErrors());
// 
//     GcGrouperSync gcGrouperSync = GcGrouperSyncDao.retrieveByProvisionerName(null, configId);
//     assertNotNull("grouper_sync row should exist for " + configId, gcGrouperSync);
//     long syncInternalId = gcGrouperSync.getInternalId();
// 
//     // snapshot catalogs to verify ids are reused after the incremental
//     Map<String, Long> groupCatalogBefore = readCatalogIds("grouper_prov_group_attr", syncInternalId);
//     Map<String, Long> userCatalogBefore = readCatalogIds("grouper_prov_user_attr", syncInternalId);
//     assertTrue("expected >=1 group catalog entry after seeding, got " + groupCatalogBefore.size(),
//         groupCatalogBefore.size() >= 1);
//     assertTrue("expected >=1 user catalog entry after seeding, got " + userCatalogBefore.size(),
//         userCatalogBefore.size() >= 1);
// 
//     int provGroupRowsBefore = countByProvisioner(configId, "grouper_prov_group");
//     int provUserRowsBefore = countByProvisioner(configId, "grouper_prov_user");
// 
//     // drive an incremental: add a third subject. SCIM's incremental will retrieveScimGroup
//     // and retrieveScimUser to compute the diff, exercising the by-id capture path.
//     testGroup.addMember(SubjectTestHelper.SUBJ2, false);
//     incrementalProvision();
// 
//     // testGroup's prov_group row should still exist (no spurious delete from the scoped sync)
//     int provGroupRowsAfter = countByProvisioner(configId, "grouper_prov_group");
//     assertTrue("incremental should not shrink prov_group; before=" + provGroupRowsBefore
//         + " after=" + provGroupRowsAfter, provGroupRowsAfter >= provGroupRowsBefore);
// 
//     int provUserRowsAfter = countByProvisioner(configId, "grouper_prov_user");
//     assertTrue("incremental should not shrink prov_user; before=" + provUserRowsBefore
//         + " after=" + provUserRowsAfter, provUserRowsAfter >= provUserRowsBefore);
// 
//     // every previously-known catalog name keeps its internal_id (regression: incremental
//     // used to reserve fresh ids and trip the (sync, attribute_name) unique key)
//     Map<String, Long> groupCatalogAfter = readCatalogIds("grouper_prov_group_attr", syncInternalId);
//     for (Map.Entry<String, Long> beforeEntry : groupCatalogBefore.entrySet()) {
//       Long afterId = groupCatalogAfter.get(beforeEntry.getKey());
//       assertNotNull("group catalog row for '" + beforeEntry.getKey()
//           + "' should still exist after incremental", afterId);
//       assertEquals("group catalog internal_id for '" + beforeEntry.getKey()
//           + "' should be reused after incremental",
//           beforeEntry.getValue(), afterId);
//     }
//     Map<String, Long> userCatalogAfter = readCatalogIds("grouper_prov_user_attr", syncInternalId);
//     for (Map.Entry<String, Long> beforeEntry : userCatalogBefore.entrySet()) {
//       Long afterId = userCatalogAfter.get(beforeEntry.getKey());
//       assertNotNull("user catalog row for '" + beforeEntry.getKey()
//           + "' should still exist after incremental", afterId);
//       assertEquals("user catalog internal_id for '" + beforeEntry.getKey()
//           + "' should be reused after incremental",
//           beforeEntry.getValue(), afterId);
//     }
// 
//     // catalogs remain deduped per (sync, attribute_name) after the incremental
//     int dupGroupAttr = new GcDbAccess().connectionName("grouper")
//         .sql("select count(*) from (select grouper_sync_internal_id, attribute_name, count(*) c "
//             + "from grouper_prov_group_attr where grouper_sync_internal_id = ? "
//             + "group by grouper_sync_internal_id, attribute_name having count(*) > 1) t")
//         .addBindVar(syncInternalId).select(int.class);
//     assertEquals("group attr catalog should still be deduped per (sync,name) after incremental",
//         0, dupGroupAttr);
// 
//     int dupUserAttr = new GcDbAccess().connectionName("grouper")
//         .sql("select count(*) from (select grouper_sync_internal_id, attribute_name, count(*) c "
//             + "from grouper_prov_user_attr where grouper_sync_internal_id = ? "
//             + "group by grouper_sync_internal_id, attribute_name having count(*) > 1) t")
//         .addBindVar(syncInternalId).select(int.class);
//     assertEquals("user attr catalog should still be deduped per (sync,name) after incremental",
//         0, dupUserAttr);
// 
//     // no orphan FKs after the incremental
//     int orphanGroupValues = new GcDbAccess().connectionName("grouper")
//         .sql("select count(*) from grouper_prov_group_attr_value gpv "
//             + "left join grouper_prov_group_attr pa on gpv.prov_group_attr_internal_id = pa.internal_id "
//             + "join grouper_prov_group pg on gpv.prov_group_internal_id = pg.internal_id "
//             + "where pg.grouper_sync_internal_id = ? and pa.internal_id is null")
//         .addBindVar(syncInternalId).select(int.class);
//     assertEquals("no group_attr_value rows should orphan their catalog FK after incremental",
//         0, orphanGroupValues);
// 
//     int orphanUserValues = new GcDbAccess().connectionName("grouper")
//         .sql("select count(*) from grouper_prov_user_attr_value puv "
//             + "left join grouper_prov_user_attr pa on puv.prov_user_attr_internal_id = pa.internal_id "
//             + "join grouper_prov_user pu on puv.prov_user_internal_id = pu.internal_id "
//             + "where pu.grouper_sync_internal_id = ? and pa.internal_id is null")
//         .addBindVar(syncInternalId).select(int.class);
//     assertEquals("no user_attr_value rows should orphan their catalog FK after incremental",
//         0, orphanUserValues);
//   }

  /**
   * Verify strict-native semantics: when the SCIM target contains entities/groups that
   * Grouper does NOT provision, the full-sync still captures them into the reporting
   * tables. The captured rows have NULL Grouper-side internal IDs (member_internal_id /
   * group_internal_id) since they aren't linked to any Grouper member/group.
   *
   * <p>Setup:
   * <ul>
   *   <li>Grouper provisions {@code test:testGroup} with SUBJ0 + SUBJ1 (normal path).</li>
   *   <li>Mock SCIM target is pre-populated with an orphan group + orphan user before the
   *       provisioner ever runs — these are NOT visible to Grouper.</li>
   *   <li>Provisioner is configured without delete operations enabled, so the orphans
   *       persist across the run instead of being deleted by the daemon.</li>
   * </ul>
   *
   * <p>After {@code fullProvision}, the reporting tables should contain rows for ALL the
   * SCIM target's contents — Grouper's testGroup + SUBJ0 + SUBJ1 + the orphan group + the
   * orphan user — with the orphans clearly marked by NULL Grouper-side ids.
   */
  public void testFullProvisionCapturesOrphanTargetEntities() {

    String configId = "awsProvisioner";

    ScimProvisionerTestUtils.setupAwsExternalSystem();

    // configure provisioner WITHOUT delete-types — daemon will leave orphans alone instead
    // of trying to clean them out
    ScimProvisionerTestUtils.configureScimProvisioner(new ScimProvisionerTestConfigInput()
        .assignChangelogConsumerConfigId("awsScimProvTestCLC")
        .assignConfigId(configId)
        .assignBearerTokenExternalSystemConfigId("awsConfigId")
        .assignScimType("AWS")
        .assignGroupAttributeCount(2)
        .assignBearer(true)
        .addExtraConfig("loadEntitiesToGenericGrouperTable", "true")
        .addExtraConfig("loadGroupsToGenericGrouperTable", "true")
        .addExtraConfig("loadMembershipsToGenericGrouperTable", "true"));

    // pre-populate orphans directly into mock_scim_user / mock_scim_group. these resources
    // exist in the target but are unknown to Grouper, simulating real-world cases where the
    // target system contains data provisioned outside Grouper (or before Grouper took over).
    //
    // important: deliberately set attributes that are NOT in the provisioner's
    // targetGroupAttribute / targetEntityAttribute mapping config (externalId and schemas
    // here — the AWS test config only maps id and displayName for groups). this lets us
    // verify that the sync-back capture is independent of the provisioner's mapping config:
    // we record what the target returned, not just what the provisioner is configured to
    // operate on.
    Timestamp now = new Timestamp(System.currentTimeMillis());

    GrouperScim2Group orphanGroup = new GrouperScim2Group();
    orphanGroup.setId("orphan-group-id-1234");
    orphanGroup.setDisplayName("orphanGroupNotInGrouper");
    orphanGroup.setExternalId("orphan-group-ext");
    orphanGroup.setSchemas("urn:ietf:params:scim:schemas:core:2.0:Group");
    // mock_scim_group has NOT NULL on created / last_modified — mirror what the mock's
    // createGroup handler does (see GenericScim2MockServiceHandler.postGroup)
    orphanGroup.setCreated(now);
    orphanGroup.setLastModified(now);
    HibernateSession.byObjectStatic().save(orphanGroup);

    GrouperScim2User orphanUser = new GrouperScim2User();
    orphanUser.setId("orphan-user-id-5678");
    orphanUser.setUserName("orphan.user.not.in.grouper");
    orphanUser.setDisplayName("Orphan User Not In Grouper");
    orphanUser.setExternalId("orphan-user-ext");
    orphanUser.setEmailValue("orphan@example.edu");
    orphanUser.setSchemas("urn:ietf:params:scim:schemas:core:2.0:User");
    HibernateSession.byObjectStatic().save(orphanUser);

    Stem stem = new StemSave(this.grouperSession).assignName("test").save();
    Group testGroup = new GroupSave(this.grouperSession).assignName("test:testGroup").save();
    testGroup.addMember(SubjectTestHelper.SUBJ0, false);
    testGroup.addMember(SubjectTestHelper.SUBJ1, false);

    GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision(configId);
    attributeValue.setTargetName(configId);
    attributeValue.setStemScopeString("sub");
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);

    // PASS 1: daemon inserts test:testGroup + SUBJ0 + SUBJ1 into SCIM mock; orphans remain
    // untouched (delete-types are disabled). retrieveAll on pass 1 sees orphans + nothing
    // for Grouper's testGroup yet (since insert happens after the read in this run).
    GrouperProvisioningOutput passOne = fullProvision();
    GrouperProvisioner.retrieveInternalLastProvisioner();
    assertEquals(0, passOne.getRecordsWithErrors());

    // PASS 2: retrieveAll sees the orphans AND Grouper's stuff; capture hooks record all
    GrouperProvisioningOutput passTwo = fullProvision();
    GrouperProvisioner.retrieveInternalLastProvisioner();
    assertEquals(0, passTwo.getRecordsWithErrors());

    GcGrouperSync gcGrouperSync = GcGrouperSyncDao.retrieveByProvisionerName(null, configId);
    assertNotNull("grouper_sync row should exist for " + configId, gcGrouperSync);
    long syncInternalId = gcGrouperSync.getInternalId();

    // ---- orphan group landed in prov_group with NULL group_internal_id ----

    int orphanGroupRowsTotal = new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from grouper_prov_group "
            + "where grouper_sync_internal_id = ? and target_group_id = ?")
        .addBindVar(syncInternalId).addBindVar(orphanGroup.getId()).select(int.class);
    assertEquals("expected exactly 1 prov_group row for the orphan group, target_id="
        + orphanGroup.getId(), 1, orphanGroupRowsTotal);

    int orphanGroupRowsUnlinked = new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from grouper_prov_group "
            + "where grouper_sync_internal_id = ? and target_group_id = ? "
            + "and group_internal_id is null")
        .addBindVar(syncInternalId).addBindVar(orphanGroup.getId()).select(int.class);
    assertEquals("orphan group's prov_group row must have group_internal_id IS NULL"
        + " (not linked to any Grouper group)", 1, orphanGroupRowsUnlinked);

    // ---- orphan user landed in prov_user with NULL member_internal_id ----

    int orphanUserRowsTotal = new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from grouper_prov_user "
            + "where grouper_sync_internal_id = ? and target_user_id = ?")
        .addBindVar(syncInternalId).addBindVar(orphanUser.getId()).select(int.class);
    assertEquals("expected exactly 1 prov_user row for the orphan user, target_id="
        + orphanUser.getId(), 1, orphanUserRowsTotal);

    int orphanUserRowsUnlinked = new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from grouper_prov_user "
            + "where grouper_sync_internal_id = ? and target_user_id = ? "
            + "and member_internal_id is null")
        .addBindVar(syncInternalId).addBindVar(orphanUser.getId()).select(int.class);
    assertEquals("orphan user's prov_user row must have member_internal_id IS NULL"
        + " (not linked to any Grouper member)", 1, orphanUserRowsUnlinked);

    // ---- Grouper's own testGroup + 2 members land alongside the orphans, AND have
    //      their Grouper-side linkage columns (group_internal_id / member_internal_id)
    //      populated. The linkage is resolved from the in-memory wrappers at end-of-run.

    int testGroupRows = new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from grouper_prov_group "
            + "where grouper_sync_internal_id = ? and target_group_id != ?")
        .addBindVar(syncInternalId).addBindVar(orphanGroup.getId()).select(int.class);
    assertEquals("expected exactly 1 prov_group row for Grouper's testGroup",
        1, testGroupRows);

    int testGroupRowsLinked = new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from grouper_prov_group "
            + "where grouper_sync_internal_id = ? and target_group_id != ? "
            + "and group_internal_id is not null")
        .addBindVar(syncInternalId).addBindVar(orphanGroup.getId()).select(int.class);
    assertEquals("Grouper's testGroup prov_group row must have group_internal_id linked",
        1, testGroupRowsLinked);

    int nonOrphanUserRows = new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from grouper_prov_user "
            + "where grouper_sync_internal_id = ? and target_user_id != ?")
        .addBindVar(syncInternalId).addBindVar(orphanUser.getId()).select(int.class);
    assertEquals("expected exactly 2 prov_user rows for SUBJ0 + SUBJ1 (Grouper-provisioned)",
        2, nonOrphanUserRows);

    int nonOrphanUserRowsLinked = new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from grouper_prov_user "
            + "where grouper_sync_internal_id = ? and target_user_id != ? "
            + "and member_internal_id is not null")
        .addBindVar(syncInternalId).addBindVar(orphanUser.getId()).select(int.class);
    assertEquals("Grouper-provisioned prov_user rows (SUBJ0 + SUBJ1) must have"
        + " member_internal_id linked", 2, nonOrphanUserRowsLinked);

    // and prove the linkage points at real grouper_members rows
    int linkedToRealMembers = new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from grouper_prov_user pu "
            + "join grouper_members gm on gm.internal_id = pu.member_internal_id "
            + "where pu.grouper_sync_internal_id = ? and pu.target_user_id != ?")
        .addBindVar(syncInternalId).addBindVar(orphanUser.getId()).select(int.class);
    assertEquals("Grouper-provisioned prov_user rows' member_internal_id must resolve to"
        + " grouper_members rows", 2, linkedToRealMembers);

    // ---- orphan's attribute values must reference real catalog rows ----

    int orphanGroupValueRows = new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from grouper_prov_group_attr_value gpv "
            + "join grouper_prov_group pg on pg.internal_id = gpv.prov_group_internal_id "
            + "where pg.grouper_sync_internal_id = ? and pg.target_group_id = ?")
        .addBindVar(syncInternalId).addBindVar(orphanGroup.getId()).select(int.class);
    assertTrue("expected >=1 attribute value row for the orphan group, got "
        + orphanGroupValueRows, orphanGroupValueRows >= 1);

    int orphanUserValueRows = new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from grouper_prov_user_attr_value puv "
            + "join grouper_prov_user pu on pu.internal_id = puv.prov_user_internal_id "
            + "where pu.grouper_sync_internal_id = ? and pu.target_user_id = ?")
        .addBindVar(syncInternalId).addBindVar(orphanUser.getId()).select(int.class);
    assertTrue("expected >=1 attribute value row for the orphan user, got "
        + orphanUserValueRows, orphanUserValueRows >= 1);

    // ---- SCIM default attributes (set on the sync subclass) drive capture. With no
    //      operator-configured nativeAttributesGroups / nativeAttributesEntities, the
    //      defaults from GrouperScim2ProvisioningTargetNativeSync apply:
    //      - groups: displayName, externalId
    //      - users:  userName, displayName, active, externalId, emailValue
    //      The orphans were deliberately given these fields; verify they all land in the
    //      per-provisioner catalog and that the orphans' value rows match.

    for (String defaultAttr : new String[] {"displayName", "externalId"}) {
      int catalogRows = new GcDbAccess().connectionName("grouper")
          .sql("select count(*) from grouper_prov_group_attr "
              + "where grouper_sync_internal_id = ? and attribute_name = ?")
          .addBindVar(syncInternalId).addBindVar(defaultAttr).select(int.class);
      assertEquals("default group attribute '" + defaultAttr
          + "' should be captured in the per-provisioner catalog", 1, catalogRows);

      int valueRows = new GcDbAccess().connectionName("grouper")
          .sql("select count(*) from grouper_prov_group_attr_value gpv "
              + "join grouper_prov_group_attr gpa on gpa.internal_id = gpv.prov_group_attr_internal_id "
              + "join grouper_prov_group pg on pg.internal_id = gpv.prov_group_internal_id "
              + "where pg.grouper_sync_internal_id = ? and pg.target_group_id = ? and gpa.attribute_name = ?")
          .addBindVar(syncInternalId).addBindVar(orphanGroup.getId()).addBindVar(defaultAttr).select(int.class);
      assertEquals("orphan group should have a value row for default attribute '" + defaultAttr + "'",
          1, valueRows);
    }

    for (String defaultAttr : new String[] {"userName", "displayName", "active", "externalId", "emailValue"}) {
      int catalogRows = new GcDbAccess().connectionName("grouper")
          .sql("select count(*) from grouper_prov_user_attr "
              + "where grouper_sync_internal_id = ? and attribute_name = ?")
          .addBindVar(syncInternalId).addBindVar(defaultAttr).select(int.class);
      assertEquals("default user attribute '" + defaultAttr
          + "' should be captured in the per-provisioner catalog", 1, catalogRows);

      int valueRows = new GcDbAccess().connectionName("grouper")
          .sql("select count(*) from grouper_prov_user_attr_value puv "
              + "join grouper_prov_user_attr pua on pua.internal_id = puv.prov_user_attr_internal_id "
              + "join grouper_prov_user pu on pu.internal_id = puv.prov_user_internal_id "
              + "where pu.grouper_sync_internal_id = ? and pu.target_user_id = ? and pua.attribute_name = ?")
          .addBindVar(syncInternalId).addBindVar(orphanUser.getId()).addBindVar(defaultAttr).select(int.class);
      assertEquals("orphan user should have a value row for default attribute '" + defaultAttr + "'",
          1, valueRows);
    }

    // sanity: 'id' must NOT be captured as an attribute — it's already the target_*_id column
    int idAsGroupAttrRows = new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from grouper_prov_group_attr "
            + "where grouper_sync_internal_id = ? and attribute_name = 'id'")
        .addBindVar(syncInternalId).select(int.class);
    assertEquals("'id' must not appear in grouper_prov_group_attr (already target_group_id column)",
        0, idAsGroupAttrRows);

    int idAsUserAttrRows = new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from grouper_prov_user_attr "
            + "where grouper_sync_internal_id = ? and attribute_name = 'id'")
        .addBindVar(syncInternalId).select(int.class);
    assertEquals("'id' must not appear in grouper_prov_user_attr (already target_user_id column)",
        0, idAsUserAttrRows);
  }

  /**
   * Multi-sync coverage with data evolution between rounds. Verifies that the reporting
   * tables track target-side state across cycles, not just on first sync:
   *
   * <ul>
   *   <li><b>Round 1 — initial state</b>: Grouper has {@code test:testGroup} with SUBJ0
   *       only. Two {@code fullProvision} passes seed the SCIM target and populate the
   *       reporting tables. Snapshot the catalog ids for later comparison.</li>
   *   <li><b>Round 2 — data changes</b>:
   *     <ul>
   *       <li>Grouper-side: add SUBJ1 to testGroup (drives a daemon-issued PATCH that
   *           expands the target group's membership and inserts a new SCIM user).</li>
   *       <li>Target-side drift: directly insert a fresh orphan group + orphan user into
   *           {@code mock_scim_*} that Grouper has no knowledge of.</li>
   *     </ul>
   *   </li>
   *   <li><b>Round 3 — second full sync + assertions</b>:
   *     <ul>
   *       <li>Per-provisioner catalog ids are reused (same values as Round 1's snapshot)
   *           — no unique-key collisions from rerunning.</li>
   *       <li>{@code grouper_prov_user} now has 3 rows (SUBJ0, SUBJ1, orphan_user) — both
   *           the Grouper-driven add and the target-drift add land.</li>
   *       <li>{@code grouper_prov_group} now has 2 rows (testGroup, orphan_group).</li>
   *       <li>{@code grouper_prov_mship} now has 2 rows (SUBJ0 + SUBJ1 in testGroup).</li>
   *       <li>The orphan user's {@code userName} value row matches what we inserted, proving
   *           the target-drift entity was captured with its actual attributes.</li>
   *     </ul>
   *   </li>
   * </ul>
   */
  public void testFullProvisionReflectsDataChangesAcrossSyncs() {

    String configId = "awsProvisioner";

    ScimProvisionerTestUtils.setupAwsExternalSystem();

    // delete-types disabled so the mid-test orphans we insert in Round 2 persist
    ScimProvisionerTestUtils.configureScimProvisioner(new ScimProvisionerTestConfigInput()
        .assignChangelogConsumerConfigId("awsScimProvTestCLC")
        .assignConfigId(configId)
        .assignBearerTokenExternalSystemConfigId("awsConfigId")
        .assignScimType("AWS")
        .assignGroupAttributeCount(2)
        .assignBearer(true)
        .addExtraConfig("loadEntitiesToGenericGrouperTable", "true")
        .addExtraConfig("loadGroupsToGenericGrouperTable", "true")
        .addExtraConfig("loadMembershipsToGenericGrouperTable", "true"));

    // ===================== ROUND 1: initial state =====================

    Stem stem = new StemSave(this.grouperSession).assignName("test").save();
    Group testGroup = new GroupSave(this.grouperSession).assignName("test:testGroup").save();
    testGroup.addMember(SubjectTestHelper.SUBJ0, false);

    GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision(configId);
    attributeValue.setTargetName(configId);
    attributeValue.setStemScopeString("sub");
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);

    // pass 1 inserts SUBJ0 + testGroup into SCIM; pass 2 reads them back and populates reporting
    assertEquals(0, fullProvision().getRecordsWithErrors());
    assertEquals(0, fullProvision().getRecordsWithErrors());

    GcGrouperSync gcGrouperSync = GcGrouperSyncDao.retrieveByProvisionerName(null, configId);
    assertNotNull("grouper_sync row should exist for " + configId, gcGrouperSync);
    long syncInternalId = gcGrouperSync.getInternalId();

    // round 1 snapshot: catalog ids that should remain stable across the next syncs
    Map<String, Long> groupCatalogRound1 = readCatalogIds("grouper_prov_group_attr", syncInternalId);
    Map<String, Long> userCatalogRound1 = readCatalogIds("grouper_prov_user_attr", syncInternalId);
    assertTrue("expected the SCIM group defaults in catalog after round 1, got "
        + groupCatalogRound1, groupCatalogRound1.containsKey("displayName"));
    assertTrue("expected the SCIM user defaults in catalog after round 1, got "
        + userCatalogRound1, userCatalogRound1.containsKey("userName"));

    int round1UserRows = countByProvisioner(configId, "grouper_prov_user");
    int round1GroupRows = countByProvisioner(configId, "grouper_prov_group");
    int round1MshipRows = new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from grouper_prov_mship where grouper_sync_internal_id = ?")
        .addBindVar(syncInternalId).select(int.class);
    assertEquals("round 1: 1 prov_user row for SUBJ0", 1, round1UserRows);
    assertEquals("round 1: 1 prov_group row for testGroup", 1, round1GroupRows);
    assertEquals("round 1: 1 prov_mship row for SUBJ0 in testGroup", 1, round1MshipRows);

    // ===================== ROUND 2: data changes =====================

    // Grouper-side: add SUBJ1 to testGroup. next fullProvision will issue a PATCH to insert
    // SUBJ1 into the SCIM target and add the membership.
    testGroup.addMember(SubjectTestHelper.SUBJ1, false);

    // Target-side drift: introduce orphans that the daemon didn't create. simulates real
    // environments where the target has data provisioned outside Grouper. delete-types are
    // disabled in this test config, so these will persist across sync #2.
    Timestamp now = new Timestamp(System.currentTimeMillis());

    GrouperScim2Group orphanGroup = new GrouperScim2Group();
    orphanGroup.setId("orphan-group-evolve-1");
    orphanGroup.setDisplayName("orphanGroupAddedMidTest");
    orphanGroup.setExternalId("orphan-group-evolve-ext");
    orphanGroup.setSchemas("urn:ietf:params:scim:schemas:core:2.0:Group");
    orphanGroup.setCreated(now);
    orphanGroup.setLastModified(now);
    HibernateSession.byObjectStatic().save(orphanGroup);

    GrouperScim2User orphanUser = new GrouperScim2User();
    orphanUser.setId("orphan-user-evolve-1");
    orphanUser.setUserName("orphan.user.added.mid.test");
    orphanUser.setDisplayName("Orphan User Added Mid Test");
    orphanUser.setExternalId("orphan-user-evolve-ext");
    orphanUser.setEmailValue("orphan-evolve@example.edu");
    orphanUser.setSchemas("urn:ietf:params:scim:schemas:core:2.0:User");
    HibernateSession.byObjectStatic().save(orphanUser);

    // ===================== ROUND 3: second full sync + assertions =====================

    // pass 1 issues the Grouper-driven PATCH (SUBJ1 add + membership); pass 2 reads
    // everything back from the SCIM target (Grouper's + orphans) and refreshes reporting.
    assertEquals(0, fullProvision().getRecordsWithErrors());
    assertEquals(0, fullProvision().getRecordsWithErrors());

    // ---- catalog stability ----

    Map<String, Long> groupCatalogRound3 = readCatalogIds("grouper_prov_group_attr", syncInternalId);
    Map<String, Long> userCatalogRound3 = readCatalogIds("grouper_prov_user_attr", syncInternalId);
    for (Map.Entry<String, Long> beforeEntry : groupCatalogRound1.entrySet()) {
      Long afterId = groupCatalogRound3.get(beforeEntry.getKey());
      assertNotNull("group catalog row for '" + beforeEntry.getKey()
          + "' should still exist after round 3", afterId);
      assertEquals("group catalog internal_id for '" + beforeEntry.getKey()
          + "' should be reused across syncs", beforeEntry.getValue(), afterId);
    }
    for (Map.Entry<String, Long> beforeEntry : userCatalogRound1.entrySet()) {
      Long afterId = userCatalogRound3.get(beforeEntry.getKey());
      assertNotNull("user catalog row for '" + beforeEntry.getKey()
          + "' should still exist after round 3", afterId);
      assertEquals("user catalog internal_id for '" + beforeEntry.getKey()
          + "' should be reused across syncs", beforeEntry.getValue(), afterId);
    }

    // ---- row counts reflect the new state ----

    int round3UserRows = countByProvisioner(configId, "grouper_prov_user");
    assertEquals("round 3: 3 prov_user rows expected (SUBJ0, SUBJ1, orphan_user)",
        3, round3UserRows);

    int round3GroupRows = countByProvisioner(configId, "grouper_prov_group");
    assertEquals("round 3: 2 prov_group rows expected (testGroup, orphan_group)",
        2, round3GroupRows);

    int round3MshipRows = new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from grouper_prov_mship where grouper_sync_internal_id = ?")
        .addBindVar(syncInternalId).select(int.class);
    assertEquals("round 3: 2 prov_mship rows expected (SUBJ0 + SUBJ1 in testGroup)",
        2, round3MshipRows);

    // ---- the orphan group's row is present and unlinked ----

    int orphanGroupRow = new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from grouper_prov_group "
            + "where grouper_sync_internal_id = ? and target_group_id = ? "
            + "and group_internal_id is null")
        .addBindVar(syncInternalId).addBindVar(orphanGroup.getId()).select(int.class);
    assertEquals("orphan group should land in prov_group with group_internal_id IS NULL",
        1, orphanGroupRow);

    // ---- the orphan user's userName value row matches what we inserted ----

    String orphanUserNameInReporting = new GcDbAccess().connectionName("grouper")
        .sql("select value_string from grouper_prov_user_attr_v "
            + "where grouper_sync_id = (select id from grouper_sync where internal_id = ?) "
            + "and target_user_id = ? and attribute_name = 'userName'")
        .addBindVar(syncInternalId).addBindVar(orphanUser.getId()).select(String.class);
    assertEquals("orphan user's userName should round-trip through reporting",
        "orphan.user.added.mid.test", orphanUserNameInReporting);

    // ---- catalog still deduped, no orphan FKs ----

    int dupGroupAttr = new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from (select grouper_sync_internal_id, attribute_name, count(*) c "
            + "from grouper_prov_group_attr where grouper_sync_internal_id = ? "
            + "group by grouper_sync_internal_id, attribute_name having count(*) > 1) t")
        .addBindVar(syncInternalId).select(int.class);
    assertEquals("group attr catalog should still be deduped after multi-sync evolution",
        0, dupGroupAttr);

    int dupUserAttr = new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from (select grouper_sync_internal_id, attribute_name, count(*) c "
            + "from grouper_prov_user_attr where grouper_sync_internal_id = ? "
            + "group by grouper_sync_internal_id, attribute_name having count(*) > 1) t")
        .addBindVar(syncInternalId).select(int.class);
    assertEquals("user attr catalog should still be deduped after multi-sync evolution",
        0, dupUserAttr);
  }

  /**
   * Verify the {@code loadGroupsToGenericGrouperTable} flag is honored in isolation: when
   * only groups capture is on, only {@code grouper_prov_group*} rows are written. The user
   * and membership tables stay empty even though the daemon still retrieves users (for
   * provisioning) and memberships (for diffing).
   */
  public void testLoadGroupsFlagInIsolation() {

    String configId = "awsProvisioner";

    ScimProvisionerTestUtils.setupAwsExternalSystem();

    ScimProvisionerTestUtils.configureScimProvisioner(new ScimProvisionerTestConfigInput()
        .assignChangelogConsumerConfigId("awsScimProvTestCLC")
        .assignConfigId(configId)
        .assignBearerTokenExternalSystemConfigId("awsConfigId")
        .assignEntityDeleteType("deleteEntitiesIfNotExistInGrouper")
        .assignGroupDeleteType("deleteGroupsIfGrouperDeleted")
        .assignMembershipDeleteType("deleteMembershipsIfGrouperDeleted")
        .assignScimType("AWS")
        .assignGroupAttributeCount(2)
        .assignBearer(true)
        .addExtraConfig("loadGroupsToGenericGrouperTable", "true")
        .addExtraConfig("loadEntitiesToGenericGrouperTable", "false")
        .addExtraConfig("loadMembershipsToGenericGrouperTable", "false"));

    Stem stem = new StemSave(this.grouperSession).assignName("test").save();
    Group testGroup = new GroupSave(this.grouperSession).assignName("test:testGroup").save();
    testGroup.addMember(SubjectTestHelper.SUBJ0, false);
    testGroup.addMember(SubjectTestHelper.SUBJ1, false);

    GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision(configId);
    attributeValue.setTargetName(configId);
    attributeValue.setStemScopeString("sub");
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);

    assertEquals(0, fullProvision().getRecordsWithErrors());
    assertEquals(0, fullProvision().getRecordsWithErrors());

    GcGrouperSync gcGrouperSync = GcGrouperSyncDao.retrieveByProvisionerName(null, configId);
    long syncInternalId = gcGrouperSync.getInternalId();

    assertTrue("expected >=1 prov_group row when groups capture is on",
        countByProvisioner(configId, "grouper_prov_group") >= 1);
    assertEquals("expected 0 prov_user rows when entities capture is off",
        0, countByProvisioner(configId, "grouper_prov_user"));
    int mshipRows = new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from grouper_prov_mship where grouper_sync_internal_id = ?")
        .addBindVar(syncInternalId).select(int.class);
    assertEquals("expected 0 prov_mship rows when memberships capture is off", 0, mshipRows);
  }

  /**
   * Mirror of {@link #testLoadGroupsFlagInIsolation}: only the entities flag is on. Only
   * {@code grouper_prov_user*} rows are written.
   */
  public void testLoadEntitiesFlagInIsolation() {

    String configId = "awsProvisioner";

    ScimProvisionerTestUtils.setupAwsExternalSystem();

    ScimProvisionerTestUtils.configureScimProvisioner(new ScimProvisionerTestConfigInput()
        .assignChangelogConsumerConfigId("awsScimProvTestCLC")
        .assignConfigId(configId)
        .assignBearerTokenExternalSystemConfigId("awsConfigId")
        .assignEntityDeleteType("deleteEntitiesIfNotExistInGrouper")
        .assignGroupDeleteType("deleteGroupsIfGrouperDeleted")
        .assignMembershipDeleteType("deleteMembershipsIfGrouperDeleted")
        .assignScimType("AWS")
        .assignGroupAttributeCount(2)
        .assignBearer(true)
        .addExtraConfig("loadGroupsToGenericGrouperTable", "false")
        .addExtraConfig("loadEntitiesToGenericGrouperTable", "true")
        .addExtraConfig("loadMembershipsToGenericGrouperTable", "false"));

    Stem stem = new StemSave(this.grouperSession).assignName("test").save();
    Group testGroup = new GroupSave(this.grouperSession).assignName("test:testGroup").save();
    testGroup.addMember(SubjectTestHelper.SUBJ0, false);
    testGroup.addMember(SubjectTestHelper.SUBJ1, false);

    GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision(configId);
    attributeValue.setTargetName(configId);
    attributeValue.setStemScopeString("sub");
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);

    assertEquals(0, fullProvision().getRecordsWithErrors());
    assertEquals(0, fullProvision().getRecordsWithErrors());

    GcGrouperSync gcGrouperSync = GcGrouperSyncDao.retrieveByProvisionerName(null, configId);
    long syncInternalId = gcGrouperSync.getInternalId();

    assertEquals("expected 0 prov_group rows when groups capture is off",
        0, countByProvisioner(configId, "grouper_prov_group"));
    assertTrue("expected >=2 prov_user rows (SUBJ0 + SUBJ1) when entities capture is on",
        countByProvisioner(configId, "grouper_prov_user") >= 2);
    int mshipRows = new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from grouper_prov_mship where grouper_sync_internal_id = ?")
        .addBindVar(syncInternalId).select(int.class);
    assertEquals("expected 0 prov_mship rows when memberships capture is off", 0, mshipRows);
  }

  /**
   * With both object loads on but memberships off, the prov_* object tables populate but
   * {@code grouper_prov_mship} stays empty. Proves the membership gate is independent of
   * the object gates.
   */
  public void testLoadMembershipsFlagOff() {

    String configId = "awsProvisioner";

    ScimProvisionerTestUtils.setupAwsExternalSystem();

    ScimProvisionerTestUtils.configureScimProvisioner(new ScimProvisionerTestConfigInput()
        .assignChangelogConsumerConfigId("awsScimProvTestCLC")
        .assignConfigId(configId)
        .assignBearerTokenExternalSystemConfigId("awsConfigId")
        .assignEntityDeleteType("deleteEntitiesIfNotExistInGrouper")
        .assignGroupDeleteType("deleteGroupsIfGrouperDeleted")
        .assignMembershipDeleteType("deleteMembershipsIfGrouperDeleted")
        .assignScimType("AWS")
        .assignGroupAttributeCount(2)
        .assignBearer(true)
        .addExtraConfig("loadGroupsToGenericGrouperTable", "true")
        .addExtraConfig("loadEntitiesToGenericGrouperTable", "true")
        .addExtraConfig("loadMembershipsToGenericGrouperTable", "false"));

    Stem stem = new StemSave(this.grouperSession).assignName("test").save();
    Group testGroup = new GroupSave(this.grouperSession).assignName("test:testGroup").save();
    testGroup.addMember(SubjectTestHelper.SUBJ0, false);
    testGroup.addMember(SubjectTestHelper.SUBJ1, false);

    GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision(configId);
    attributeValue.setTargetName(configId);
    attributeValue.setStemScopeString("sub");
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);

    assertEquals(0, fullProvision().getRecordsWithErrors());
    assertEquals(0, fullProvision().getRecordsWithErrors());

    GcGrouperSync gcGrouperSync = GcGrouperSyncDao.retrieveByProvisionerName(null, configId);
    long syncInternalId = gcGrouperSync.getInternalId();

    assertTrue("expected >=1 prov_group row",
        countByProvisioner(configId, "grouper_prov_group") >= 1);
    assertTrue("expected >=2 prov_user rows (SUBJ0 + SUBJ1)",
        countByProvisioner(configId, "grouper_prov_user") >= 2);
    int mshipRows = new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from grouper_prov_mship where grouper_sync_internal_id = ?")
        .addBindVar(syncInternalId).select(int.class);
    assertEquals("expected 0 prov_mship rows when memberships capture is off", 0, mshipRows);
  }

  /**
   * With {@code selectAllGroups=false} and {@code selectAllEntities=false}, the daemon
   * fetches only the resources mapped to Grouper-provisioned objects (by id) instead of
   * doing a server-wide listing. An orphan group/user that the target has but Grouper
   * doesn't should NOT land in the reporting tables under this mode.
   *
   * <p>Contrast with {@link #testFullProvisionCapturesOrphanTargetEntities}, which is the
   * selectAll=true case where the orphan IS captured.
   */
  public void testSelectAllFalseExcludesOrphans() {

    String configId = "awsProvisioner";

    ScimProvisionerTestUtils.setupAwsExternalSystem();

    ScimProvisionerTestUtils.configureScimProvisioner(new ScimProvisionerTestConfigInput()
        .assignChangelogConsumerConfigId("awsScimProvTestCLC")
        .assignConfigId(configId)
        .assignBearerTokenExternalSystemConfigId("awsConfigId")
        .assignScimType("AWS")
        .assignGroupAttributeCount(2)
        .assignBearer(true)
        .addExtraConfig("loadEntitiesToGenericGrouperTable", "true")
        .addExtraConfig("loadGroupsToGenericGrouperTable", "true")
        .addExtraConfig("loadMembershipsToGenericGrouperTable", "true")
        .addExtraConfig("selectAllGroups", "false")
        .addExtraConfig("selectAllEntities", "false"));

    // pre-populate an orphan group + orphan user — must NOT appear in reporting because
    // selectAll=false makes the daemon fetch only by id (Grouper-known resources only).
    Timestamp now = new Timestamp(System.currentTimeMillis());

    GrouperScim2Group orphanGroup = new GrouperScim2Group();
    orphanGroup.setId("orphan-group-selnone-1");
    orphanGroup.setDisplayName("orphanGroupSelectAllFalse");
    orphanGroup.setExternalId("orphan-group-selnone-ext");
    orphanGroup.setSchemas("urn:ietf:params:scim:schemas:core:2.0:Group");
    orphanGroup.setCreated(now);
    orphanGroup.setLastModified(now);
    HibernateSession.byObjectStatic().save(orphanGroup);

    GrouperScim2User orphanUser = new GrouperScim2User();
    orphanUser.setId("orphan-user-selnone-1");
    orphanUser.setUserName("orphan.user.selectAllFalse");
    orphanUser.setDisplayName("Orphan User SelectAll False");
    orphanUser.setExternalId("orphan-user-selnone-ext");
    orphanUser.setEmailValue("orphan-selnone@example.edu");
    orphanUser.setSchemas("urn:ietf:params:scim:schemas:core:2.0:User");
    HibernateSession.byObjectStatic().save(orphanUser);

    Stem stem = new StemSave(this.grouperSession).assignName("test").save();
    Group testGroup = new GroupSave(this.grouperSession).assignName("test:testGroup").save();
    testGroup.addMember(SubjectTestHelper.SUBJ0, false);
    testGroup.addMember(SubjectTestHelper.SUBJ1, false);

    GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision(configId);
    attributeValue.setTargetName(configId);
    attributeValue.setStemScopeString("sub");
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);

    assertEquals(0, fullProvision().getRecordsWithErrors());
    assertEquals(0, fullProvision().getRecordsWithErrors());

    GcGrouperSync gcGrouperSync = GcGrouperSyncDao.retrieveByProvisionerName(null, configId);
    long syncInternalId = gcGrouperSync.getInternalId();

    // Grouper-known resources still captured
    assertTrue("Grouper-provisioned testGroup should still be in prov_group",
        countByProvisioner(configId, "grouper_prov_group") >= 1);
    assertTrue("Grouper-provisioned SUBJ0/SUBJ1 should still be in prov_user",
        countByProvisioner(configId, "grouper_prov_user") >= 2);

    // orphans must NOT be in reporting (selectAll=false → no server-wide listing → no capture)
    int orphanGroupRows = new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from grouper_prov_group "
            + "where grouper_sync_internal_id = ? and target_group_id = ?")
        .addBindVar(syncInternalId).addBindVar(orphanGroup.getId()).select(int.class);
    assertEquals("orphan group must NOT be captured when selectAllGroups=false",
        0, orphanGroupRows);

    int orphanUserRows = new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from grouper_prov_user "
            + "where grouper_sync_internal_id = ? and target_user_id = ?")
        .addBindVar(syncInternalId).addBindVar(orphanUser.getId()).select(int.class);
    assertEquals("orphan user must NOT be captured when selectAllEntities=false",
        0, orphanUserRows);
  }

  /**
   * Sync-back smoke test for the scoped-retrieve path: same flow as
   * {@link #testFullProvisionPopulatesGenericTables}, but with
   * {@code selectAllGroups=false} and {@code selectAllEntities=false} so the daemon
   * uses scoped {@code retrieveScimGroup} / {@code retrieveScimUser} (per-id lookups)
   * instead of {@code retrieveScimGroups} / {@code retrieveScimUsers}. Confirms the
   * capture hooks on the scoped retrieve-by-id paths in
   * {@link edu.internet2.middleware.grouper.app.scim2Provisioning.GrouperScim2ApiCommands}
   * fire and populate the generic reporting tables.
   *
   * <p>Mirrors {@code testAzureFullSyncSelectByIdsPopulatesGenericTables} in the Azure
   * provisioner tests.
   */
  public void testScimFullSyncSelectByIdsPopulatesGenericTables() {

    String configId = "awsProvisioner";

    // override the setUp() mode: selectByIds hits GET /Groups/{id} (single group),
    // and the AWS mock only embeds the members array on that endpoint when the
    // strategy is "fullGroupMembershipsInGroupObjectsWhenRetrievingIndividualGroups".
    // Without this, populateMembershipsFromGroup sees no members → cache stays
    // empty → drain captures nothing → prov_mship=0.
    new GrouperDbConfig().configFileName("grouper.properties")
        .propertyName("grouperTest.scim2.mock.membershipStrategy.mode")
        .value("fullGroupMembershipsInGroupObjectsWhenRetrievingIndividualGroups").store();

    ScimProvisionerTestUtils.setupAwsExternalSystem();

    ScimProvisionerTestUtils.configureScimProvisioner(new ScimProvisionerTestConfigInput()
        .assignChangelogConsumerConfigId("awsScimProvTestCLC")
        .assignConfigId(configId)
        .assignBearerTokenExternalSystemConfigId("awsConfigId")
        .assignEntityDeleteType("deleteEntitiesIfNotExistInGrouper")
        .assignGroupDeleteType("deleteGroupsIfGrouperDeleted")
        .assignMembershipDeleteType("deleteMembershipsIfGrouperDeleted")
        .assignScimType("AWS")
        .assignGroupAttributeCount(2)
        .assignBearer(true)
        .addExtraConfig("loadEntitiesToGenericGrouperTable", "true")
        .addExtraConfig("loadGroupsToGenericGrouperTable", "true")
        .addExtraConfig("loadMembershipsToGenericGrouperTable", "true")
        .addExtraConfig("selectAllGroups", "false")
        .addExtraConfig("selectAllEntities", "false"));

    Stem stem = new StemSave(this.grouperSession).assignName("test").save();
    Group testGroup = new GroupSave(this.grouperSession).assignName("test:testGroup").save();
    testGroup.addMember(SubjectTestHelper.SUBJ0, false);
    testGroup.addMember(SubjectTestHelper.SUBJ1, false);

    GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision(configId);
    attributeValue.setTargetName(configId);
    attributeValue.setStemScopeString("sub");
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);

    // baseline: nothing in the generic tables yet
    assertEquals(0, countByProvisioner(configId, "grouper_prov_group"));
    assertEquals(0, countByProvisioner(configId, "grouper_prov_user"));

    // pass 1 inserts target objects; pass 2 reads them back via scoped retrieve-by-id
    // and the capture hooks populate the reporting tables.
    GrouperProvisioningOutput passOne = fullProvision();
    GrouperProvisioner.retrieveInternalLastProvisioner();
    assertEquals(0, passOne.getRecordsWithErrors());

    GrouperProvisioningOutput passTwo = fullProvision();
    GrouperProvisioner.retrieveInternalLastProvisioner();
    assertEquals(0, passTwo.getRecordsWithErrors());

    int groupRowCount = countByProvisioner(configId, "grouper_prov_group");
    assertTrue("expected >=1 prov_group row via scoped retrieve, got " + groupRowCount,
        groupRowCount >= 1);

    int userRowCount = countByProvisioner(configId, "grouper_prov_user");
    assertTrue("expected >=2 prov_user rows via scoped retrieve, got " + userRowCount,
        userRowCount >= 2);

    int membershipRows = countByProvisioner(configId, "grouper_prov_mship");
    assertTrue("expected >=2 prov_mship rows via scoped retrieve, got " + membershipRows,
        membershipRows >= 2);
  }

  /**
   * Strict-native completeness on the membership axis: when a group in the target has
   * members but Grouper doesn't provision the group, those memberships should still be
   * captured in {@code grouper_prov_mship} (with NULL Grouper-side linkage on the orphan
   * side). This is the membership analogue of {@link #testFullProvisionCapturesOrphanTargetEntities}
   * which only covered orphan objects, not orphan memberships.
   */
  public void testFullProvisionCapturesMembershipsFromOrphanGroup() {

    String configId = "awsProvisioner";

    ScimProvisionerTestUtils.setupAwsExternalSystem();

    // delete-types disabled so the orphan group + its membership row persist
    ScimProvisionerTestUtils.configureScimProvisioner(new ScimProvisionerTestConfigInput()
        .assignChangelogConsumerConfigId("awsScimProvTestCLC")
        .assignConfigId(configId)
        .assignBearerTokenExternalSystemConfigId("awsConfigId")
        .assignScimType("AWS")
        .assignGroupAttributeCount(2)
        .assignBearer(true)
        .addExtraConfig("loadEntitiesToGenericGrouperTable", "true")
        .addExtraConfig("loadGroupsToGenericGrouperTable", "true")
        .addExtraConfig("loadMembershipsToGenericGrouperTable", "true"));

    Timestamp now = new Timestamp(System.currentTimeMillis());

    // orphan group + orphan user — neither known to Grouper
    GrouperScim2Group orphanGroup = new GrouperScim2Group();
    orphanGroup.setId("orphan-mship-group-1");
    orphanGroup.setDisplayName("orphanGroupWithMembers");
    orphanGroup.setExternalId("orphan-mship-group-ext");
    orphanGroup.setSchemas("urn:ietf:params:scim:schemas:core:2.0:Group");
    orphanGroup.setCreated(now);
    orphanGroup.setLastModified(now);
    HibernateSession.byObjectStatic().save(orphanGroup);

    GrouperScim2User orphanUser = new GrouperScim2User();
    orphanUser.setId("orphan-mship-user-1");
    orphanUser.setUserName("orphan.mship.user");
    orphanUser.setDisplayName("Orphan Mship User");
    orphanUser.setExternalId("orphan-mship-user-ext");
    orphanUser.setEmailValue("orphan-mship@example.edu");
    orphanUser.setSchemas("urn:ietf:params:scim:schemas:core:2.0:User");
    HibernateSession.byObjectStatic().save(orphanUser);

    // wire them as a membership in the SCIM mock — this is what the daemon will see when
    // it lists groups and the mock includes the members array
    GrouperScim2Membership orphanMembership = new GrouperScim2Membership();
    orphanMembership.setId("orphan-mship-row-1");
    orphanMembership.setGroupId(orphanGroup.getId());
    orphanMembership.setUserId(orphanUser.getId());
    HibernateSession.byObjectStatic().save(orphanMembership);

    Stem stem = new StemSave(this.grouperSession).assignName("test").save();
    Group testGroup = new GroupSave(this.grouperSession).assignName("test:testGroup").save();
    testGroup.addMember(SubjectTestHelper.SUBJ0, false);
    testGroup.addMember(SubjectTestHelper.SUBJ1, false);

    GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision(configId);
    attributeValue.setTargetName(configId);
    attributeValue.setStemScopeString("sub");
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);

    assertEquals(0, fullProvision().getRecordsWithErrors());
    assertEquals(0, fullProvision().getRecordsWithErrors());

    GcGrouperSync gcGrouperSync = GcGrouperSyncDao.retrieveByProvisionerName(null, configId);
    long syncInternalId = gcGrouperSync.getInternalId();

    // orphan group's membership lands in prov_mship — proves strict-native capture on the
    // membership axis is independent of whether the group/user is Grouper-known.
    // prov_mship has FK columns (prov_user_internal_id, prov_group_internal_id) — not
    // target_*_id strings — so join through prov_user/prov_group to identify by target id.
    int orphanMshipRows = new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from grouper_prov_mship pm "
            + "join grouper_prov_group pg on pg.internal_id = pm.prov_group_internal_id "
            + "join grouper_prov_user pu on pu.internal_id = pm.prov_user_internal_id "
            + "where pm.grouper_sync_internal_id = ? "
            + "and pg.target_group_id = ? and pu.target_user_id = ?")
        .addBindVar(syncInternalId).addBindVar(orphanGroup.getId()).addBindVar(orphanUser.getId())
        .select(int.class);
    assertEquals("expected 1 prov_mship row for orphan group → orphan user",
        1, orphanMshipRows);

    // and Grouper's own memberships still land alongside (3 total: SUBJ0+SUBJ1 in testGroup,
    // plus the orphan)
    int totalMshipRows = new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from grouper_prov_mship where grouper_sync_internal_id = ?")
        .addBindVar(syncInternalId).select(int.class);
    assertEquals("expected 3 prov_mship rows total (2 from testGroup + 1 orphan)",
        3, totalMshipRows);
  }

  /**
   * Convergence of newly created SCIM resources into the sync-back tables. Groups and
   * entities now converge SAME-run: the create response is shadowed into the canonical map by
   * the insert hooks ({@code GrouperScim2ApiCommands.createScimGroup / createScimUser}) and
   * the end-of-run drain runs after the write phase, so grouper_prov_group / _user are
   * populated after run 1. Run 2 re-reads and re-asserts (idempotent). Memberships are a
   * separate axis not asserted here.
   */
  public void testCreateConvergesIntoSyncTablesOnNextRun() {
    String configId = "awsProvisioner";

    ScimProvisionerTestUtils.setupAwsExternalSystem();

    ScimProvisionerTestUtils.configureScimProvisioner(new ScimProvisionerTestConfigInput()
        .assignChangelogConsumerConfigId("awsScimProvTestCLC")
        .assignConfigId(configId)
        .assignBearerTokenExternalSystemConfigId("awsConfigId")
        .assignEntityDeleteType("deleteEntitiesIfNotExistInGrouper")
        .assignGroupDeleteType("deleteGroupsIfGrouperDeleted")
        .assignMembershipDeleteType("deleteMembershipsIfGrouperDeleted")
        .assignScimType("AWS")
        .assignGroupAttributeCount(2)
        .assignBearer(true)
        .addExtraConfig("loadEntitiesToGenericGrouperTable", "true")
        .addExtraConfig("loadGroupsToGenericGrouperTable", "true")
        .addExtraConfig("loadMembershipsToGenericGrouperTable", "true"));

    Stem stem = new StemSave(this.grouperSession).assignName("test").save();
    Group testGroup = new GroupSave(this.grouperSession).assignName("test:testGroup").save();
    testGroup.addMember(SubjectTestHelper.SUBJ0, false);
    testGroup.addMember(SubjectTestHelper.SUBJ1, false);

    GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision(configId);
    attributeValue.setTargetName(configId);
    attributeValue.setStemScopeString("sub");
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);

    // baseline
    assertEquals(0, countByProvisioner(configId, "grouper_prov_group"));
    assertEquals(0, countByProvisioner(configId, "grouper_prov_user"));

    // ---- run 1: creates the SCIM target resources. Groups converge SAME-run (insert hook
    // shadows the create response + the end-of-run drain), so prov_group is populated after
    // run 1. Entities/memberships still converge on run 2 until their insert hooks land.
    GrouperProvisioningOutput out1 = fullProvision();
    GrouperProvisioner.retrieveInternalLastProvisioner();
    assertEquals(0, out1.getRecordsWithErrors());

    GcGrouperSync gcGrouperSync = GcGrouperSyncDao.retrieveByProvisionerName(null, configId);
    assertNotNull("grouper_sync row should exist for " + configId, gcGrouperSync);
    long syncInternalId = gcGrouperSync.getInternalId();

    assertEquals("group insert converges into prov_group on the same run (run 1)",
        1, countByProvisioner(configId, "grouper_prov_group"));
    assertEquals("entity inserts converge into prov_user on the same run (run 1)",
        2, countByProvisioner(configId, "grouper_prov_user"));

    // ---- run 2: reads the newly created resources, captures them via the SCIM retrieve
    // hooks, and the run-2 flush writes them to grouper_prov_*.
    GrouperProvisioningOutput out2 = fullProvision();
    assertEquals(0, out2.getRecordsWithErrors());

    // ---- prov_group: testGroup row exists, linked to Grouper's group ----

    int groupRows = countByProvisioner(configId, "grouper_prov_group");
    assertEquals("expected exactly 1 prov_group row for testGroup after run 2", 1, groupRows);

    int groupRowsLinked = new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from grouper_prov_group "
            + "where grouper_sync_internal_id = ? and group_internal_id is not null")
        .addBindVar(syncInternalId).select(int.class);
    assertEquals("captured testGroup prov_group row should be linked to its Grouper group",
        1, groupRowsLinked);

    // ---- prov_user: SUBJ0 + SUBJ1 rows exist, linked to Grouper members ----

    int userRows = countByProvisioner(configId, "grouper_prov_user");
    assertEquals("expected exactly 2 prov_user rows (SUBJ0 + SUBJ1) after run 2", 2, userRows);

    int userRowsLinked = new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from grouper_prov_user "
            + "where grouper_sync_internal_id = ? and member_internal_id is not null")
        .addBindVar(syncInternalId).select(int.class);
    assertEquals("captured prov_user rows should be linked to their Grouper members",
        2, userRowsLinked);

    // ---- attribute values: SCIM defaults should be present from the run-2 read response

    int displayNameValueRows = new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from grouper_prov_group_attr_value gpv "
            + "join grouper_prov_group_attr gpa on gpa.internal_id = gpv.prov_group_attr_internal_id "
            + "join grouper_prov_group pg on pg.internal_id = gpv.prov_group_internal_id "
            + "where pg.grouper_sync_internal_id = ? and gpa.attribute_name = 'displayName'")
        .addBindVar(syncInternalId).select(int.class);
    assertTrue("displayName should be captured from the run-2 read response, got "
        + displayNameValueRows, displayNameValueRows >= 1);

    int userNameValueRows = new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from grouper_prov_user_attr_value puv "
            + "join grouper_prov_user_attr pua on pua.internal_id = puv.prov_user_attr_internal_id "
            + "join grouper_prov_user pu on pu.internal_id = puv.prov_user_internal_id "
            + "where pu.grouper_sync_internal_id = ? and pua.attribute_name = 'userName'")
        .addBindVar(syncInternalId).select(int.class);
    assertEquals("userName should be captured from the run-2 read response for both users",
        2, userNameValueRows);
  }

  /**
   * Group-insert sync-back convergence: a newly created group lands in
   * grouper_prov_group / _attr / _attr_value on the SAME run that creates it -- no second
   * run needed. SCIM is a capture-on-write target: the POST response carries the created
   * resource, so the commands class registers it into the read map like a read (step 4 of
   * the insert hook), and the end-of-run flush writes it. This is the run-1 counterpart of
   * {@link #testCreateConvergesIntoSyncTablesOnNextRun()} once same-run convergence works.
   */
  public void testGroupInsertConvergesSameRun() {
    String configId = "awsProvisioner";

    ScimProvisionerTestUtils.setupAwsExternalSystem();

    ScimProvisionerTestUtils.configureScimProvisioner(new ScimProvisionerTestConfigInput()
        .assignChangelogConsumerConfigId("awsScimProvTestCLC")
        .assignConfigId(configId)
        .assignBearerTokenExternalSystemConfigId("awsConfigId")
        .assignEntityDeleteType("deleteEntitiesIfNotExistInGrouper")
        .assignGroupDeleteType("deleteGroupsIfGrouperDeleted")
        .assignMembershipDeleteType("deleteMembershipsIfGrouperDeleted")
        .assignScimType("AWS")
        .assignGroupAttributeCount(2)
        .assignBearer(true)
        .addExtraConfig("loadEntitiesToGenericGrouperTable", "true")
        .addExtraConfig("loadGroupsToGenericGrouperTable", "true")
        .addExtraConfig("loadMembershipsToGenericGrouperTable", "true"));

    Stem stem = new StemSave(this.grouperSession).assignName("test").save();
    Group testGroup = new GroupSave(this.grouperSession).assignName("test:testGroup").save();
    testGroup.addMember(SubjectTestHelper.SUBJ0, false);
    testGroup.addMember(SubjectTestHelper.SUBJ1, false);

    GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision(configId);
    attributeValue.setTargetName(configId);
    attributeValue.setStemScopeString("sub");
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);

    // baseline: nothing in the mirror yet
    assertEquals(0, countByProvisioner(configId, "grouper_prov_group"));

    // single run: creates the SCIM group and should converge it into the mirror THIS run
    GrouperProvisioningOutput out = fullProvision();
    GrouperProvisioner.retrieveInternalLastProvisioner();
    assertEquals(0, out.getRecordsWithErrors());

    GcGrouperSync gcGrouperSync = GcGrouperSyncDao.retrieveByProvisionerName(null, configId);
    assertNotNull("grouper_sync row should exist for " + configId, gcGrouperSync);
    long syncInternalId = gcGrouperSync.getInternalId();

    // the inserted group converged on the SAME run
    assertEquals("group insert should converge into prov_group on the same run",
        1, countByProvisioner(configId, "grouper_prov_group"));

    // registered "like a read", so it is linked back to its Grouper group
    int groupRowsLinked = new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from grouper_prov_group "
            + "where grouper_sync_internal_id = ? and group_internal_id is not null")
        .addBindVar(syncInternalId).select(int.class);
    assertEquals("converged prov_group row should be linked to its Grouper group",
        1, groupRowsLinked);

    // displayName captured from the insert response, not from a later read
    int displayNameValueRows = new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from grouper_prov_group_attr_value gpv "
            + "join grouper_prov_group_attr gpa on gpa.internal_id = gpv.prov_group_attr_internal_id "
            + "join grouper_prov_group pg on pg.internal_id = gpv.prov_group_internal_id "
            + "where pg.grouper_sync_internal_id = ? and gpa.attribute_name = 'displayName'")
        .addBindVar(syncInternalId).select(int.class);
    assertTrue("displayName should be captured from the insert response, got "
        + displayNameValueRows, displayNameValueRows >= 1);
  }

  /**
   * Shared body for the two update sync-back tests. Drives a same-run attribute update (the
   * user's {@code active}, computed from membership in test2:testGroup2) and asserts the mirror
   * converges to active=true on the SAME run that issues the PATCH. {@code patchReturnsNoBody}
   * selects the production path: false -> the PATCH returns 200 + the resource, so the write
   * hook registers it and the drain skips it (capture-on-write); true -> the PATCH returns 204,
   * so the write hook only marks the id and the end-of-run drain re-reads it. {@code active} is
   * captured by default (DEFAULT_ENTITY_ATTRS) and stored as value_integer (1=true, 0=false).
   */
  private void runUserUpdateConvergesSameRun(boolean patchReturnsNoBody) {
    String configId = "awsProvisioner";

    ScimProvisionerTestUtils.setupAwsExternalSystem();

    ScimProvisionerTestUtils.configureScimProvisioner(new ScimProvisionerTestConfigInput()
        .assignChangelogConsumerConfigId("awsScimProvTestCLC")
        .assignConfigId(configId)
        .assignBearerTokenExternalSystemConfigId("awsConfigId")
        .assignScimType("AWS")
        .assignGroupAttributeCount(2)
        .assignBearer(true)
        .assignUseActiveOnUser(true)
        .addExtraConfig("loadEntitiesToGenericGrouperTable", "true")
        .addExtraConfig("loadGroupsToGenericGrouperTable", "true")
        .addExtraConfig("loadMembershipsToGenericGrouperTable", "true"));

    Stem stem = new StemSave(this.grouperSession).assignName("test").save();
    Group testGroup = new GroupSave(this.grouperSession).assignName("test:testGroup").save();
    testGroup.addMember(SubjectTestHelper.SUBJ0, false);

    // active = isInGroup('test2:testGroup2'); create that group (it is NOT itself provisioned).
    // its parent stem must exist first.
    new StemSave(this.grouperSession).assignName("test2").save();
    Group testGroup2 = new GroupSave(this.grouperSession).assignName("test2:testGroup2").save();

    GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision(configId);
    attributeValue.setTargetName(configId);
    attributeValue.setStemScopeString("sub");
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);

    // select the PATCH response shape (200 + body vs 204 no body) up front: the mock reads this
    // from the shared DB config in its own JVM, so it must be set before the run that PATCHes.
    new GrouperDbConfig().configFileName("grouper.properties")
        .propertyName("grouperTest.scim2.mock.patchUsersReturnNoBody")
        .value(String.valueOf(patchReturnsNoBody)).store();

    // run 1: create SUBJ0 in SCIM. not in test2:testGroup2, so active=false. converges same-run.
    assertEquals(0, fullProvision().getRecordsWithErrors());

    GcGrouperSync gcGrouperSync = GcGrouperSyncDao.retrieveByProvisionerName(null, configId);
    assertNotNull("grouper_sync row should exist for " + configId, gcGrouperSync);
    long syncInternalId = gcGrouperSync.getInternalId();

    // baseline: active is not yet true (0 or absent)
    Long activeBefore = activeValueIntegerInMirror(syncInternalId);
    assertTrue("active should not be true before the update, got " + activeBefore,
        activeBefore == null || activeBefore.longValue() == 0L);

    // change: add SUBJ0 to test2:testGroup2 -> next sync recomputes active=true -> patchScimUser
    testGroup2.addMember(SubjectTestHelper.SUBJ0, false);

    // update run: PATCH active=true. Body returned -> register (drain skips); 204 -> mark +
    // drain re-read. Either way the mirror reflects active=true on THIS run.
    assertEquals(0, fullProvision().getRecordsWithErrors());

    assertEquals("after update, mirror active should converge to true (1) on the same run",
        Long.valueOf(1L), activeValueIntegerInMirror(syncInternalId));
  }

  /**
   * Update sync-back, representation-returned path: the mock PATCH returns 200 + the resource,
   * so the write hook registers it like a read and the drain skips the re-read (capture-on-write).
   */
  public void testUserUpdateConvergesSameRunBodyReturned() {
    runUserUpdateConvergesSameRun(false);
  }

  /**
   * Update sync-back, no-body path: the mock PATCH returns 204, so the write hook only marks the
   * id and the end-of-run drain re-reads the user -- the branch that does not fire when the write
   * returns the representation.
   */
  public void testUserUpdateConvergesSameRunNoBody() {
    runUserUpdateConvergesSameRun(true);
  }

  /**
   * Delete sync-back: when a group (and its now-orphaned entity) is deleted from the target,
   * the mirror drops their rows on the SAME run. A delete is a write with no representation, so
   * the hook marks the id and the end-of-run drain re-reads it -- the target 404 confirms it is
   * gone (we verify, not assume), so it stays out of the read map and the flush deletes its
   * prov_group / prov_user / prov_mship rows. Memberships drop automatically: the flush skips any
   * membership whose group or user record is missing.
   */
  public void testGroupDeleteConvergesSameRun() {
    String configId = "awsProvisioner";

    ScimProvisionerTestUtils.setupAwsExternalSystem();

    ScimProvisionerTestUtils.configureScimProvisioner(new ScimProvisionerTestConfigInput()
        .assignChangelogConsumerConfigId("awsScimProvTestCLC")
        .assignConfigId(configId)
        .assignBearerTokenExternalSystemConfigId("awsConfigId")
        .assignEntityDeleteType("deleteEntitiesIfNotExistInGrouper")
        .assignGroupDeleteType("deleteGroupsIfGrouperDeleted")
        .assignMembershipDeleteType("deleteMembershipsIfGrouperDeleted")
        .assignScimType("AWS")
        .assignGroupAttributeCount(2)
        .assignBearer(true)
        .addExtraConfig("loadEntitiesToGenericGrouperTable", "true")
        .addExtraConfig("loadGroupsToGenericGrouperTable", "true")
        .addExtraConfig("loadMembershipsToGenericGrouperTable", "true"));

    Stem stem = new StemSave(this.grouperSession).assignName("test").save();
    Group testGroup = new GroupSave(this.grouperSession).assignName("test:testGroup").save();
    testGroup.addMember(SubjectTestHelper.SUBJ0, false);

    GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision(configId);
    attributeValue.setTargetName(configId);
    attributeValue.setStemScopeString("sub");
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);

    // run 1: create the group + user in SCIM and converge them into the mirror
    assertEquals(0, fullProvision().getRecordsWithErrors());

    GcGrouperSync gcGrouperSync = GcGrouperSyncDao.retrieveByProvisionerName(null, configId);
    assertNotNull("grouper_sync row should exist for " + configId, gcGrouperSync);
    long syncInternalId = gcGrouperSync.getInternalId();

    assertEquals("group should be in the mirror after create", 1,
        countByProvisioner(configId, "grouper_prov_group"));
    assertEquals("user should be in the mirror after create", 1,
        countByProvisioner(configId, "grouper_prov_user"));

    // delete the group in Grouper; the next sync deletes it (and the now-orphaned user) from SCIM
    testGroup.delete();

    // delete run: SCIM DELETE has no body, so the hook marks and the drain re-reads -> 404 ->
    // the objects stay out of the read map and the flush drops their mirror rows on THIS run.
    assertEquals(0, fullProvision().getRecordsWithErrors());

    assertEquals("group row should be gone from the mirror on the delete run", 0,
        countByProvisioner(configId, "grouper_prov_group"));
    assertEquals("orphaned user row should be gone from the mirror on the delete run", 0,
        countByProvisioner(configId, "grouper_prov_user"));
    int deleteRunMshipRows = new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from grouper_prov_mship where grouper_sync_internal_id = ?")
        .addBindVar(syncInternalId).select(int.class);
    assertEquals("membership rows should be gone from the mirror on the delete run", 0,
        deleteRunMshipRows);
  }

  /**
   * Broken-target delete sync-back: a SCIM target that acks a DELETE (204) but doesn't actually
   * remove the record. The user is still there (unchanged) on re-read, so the mirror must KEEP
   * it -- the "verify, don't assume" path: we don't trust the delete's success, the drain
   * re-reads and finds it still present. The group, which IS truly deleted, still drops.
   */
  public void testUserDeleteBrokenTargetStaysInMirror() {
    String configId = "awsProvisioner";

    ScimProvisionerTestUtils.setupAwsExternalSystem();

    ScimProvisionerTestUtils.configureScimProvisioner(new ScimProvisionerTestConfigInput()
        .assignChangelogConsumerConfigId("awsScimProvTestCLC")
        .assignConfigId(configId)
        .assignBearerTokenExternalSystemConfigId("awsConfigId")
        .assignEntityDeleteType("deleteEntitiesIfNotExistInGrouper")
        .assignGroupDeleteType("deleteGroupsIfGrouperDeleted")
        .assignMembershipDeleteType("deleteMembershipsIfGrouperDeleted")
        .assignScimType("AWS")
        .assignGroupAttributeCount(2)
        .assignBearer(true)
        .addExtraConfig("loadEntitiesToGenericGrouperTable", "true")
        .addExtraConfig("loadGroupsToGenericGrouperTable", "true")
        .addExtraConfig("loadMembershipsToGenericGrouperTable", "true"));

    Stem stem = new StemSave(this.grouperSession).assignName("test").save();
    Group testGroup = new GroupSave(this.grouperSession).assignName("test:testGroup").save();
    testGroup.addMember(SubjectTestHelper.SUBJ0, false);

    GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision(configId);
    attributeValue.setTargetName(configId);
    attributeValue.setStemScopeString("sub");
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);

    // the target acks user DELETEs but doesn't actually remove them (broken SCIM). The mock reads
    // this from the shared DB config in its own JVM, so set it before the run that DELETEs.
    new GrouperDbConfig().configFileName("grouper.properties")
        .propertyName("grouperTest.scim2.mock.deleteUsersReturnSuccessButDoNotDelete")
        .value("true").store();

    // run 1: create the group + user in SCIM and converge them into the mirror
    assertEquals(0, fullProvision().getRecordsWithErrors());

    GcGrouperSync gcGrouperSync = GcGrouperSyncDao.retrieveByProvisionerName(null, configId);
    assertNotNull("grouper_sync row should exist for " + configId, gcGrouperSync);

    assertEquals("user should be in the mirror after create", 1,
        countByProvisioner(configId, "grouper_prov_user"));

    // remove SUBJ0 from the group (keep the group) -> next sync "deletes" the now-orphaned user,
    // which the broken target acks but keeps. (Group stays; this isolates the user delete.)
    testGroup.deleteMember(SubjectTestHelper.SUBJ0);

    assertEquals(0, fullProvision().getRecordsWithErrors());

    // the group was NOT deleted -> still in the mirror
    assertEquals("group row should stay (group was not deleted)", 1,
        countByProvisioner(configId, "grouper_prov_group"));

    // check the target first: the broken mock should have 204-acked the DELETE but kept the row,
    // so the user still exists in the target (verified separately from the mirror's re-read below).
    int mockUserRows = new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from mock_scim_user").select(int.class);
    assertEquals("the user row should remain in the SCIM target (delete was not performed)", 1,
        mockUserRows);

    // the user's delete was acked but not performed -> still in the target, so the re-read keeps it
    assertEquals("user should STAY in the mirror (its delete was acked but not performed)", 1,
        countByProvisioner(configId, "grouper_prov_user"));

    // NOTE: this test deliberately does NOT assert the membership axis. SUBJ0 is testGroup's only
    // member and becomes an orphaned entity, so the framework removes it by deleting the entity
    // (DELETE /Users) -- which the broken target acks but ignores -- rather than issuing a
    // standalone PATCH-remove of the membership. There is therefore no successful membership write
    // for write-tracking to act on, and SUBJ0 is in fact still a member of testGroup in the target;
    // the mirror correctly continues to reflect that (prov_mship stays 1). Membership
    // write-tracking (a real PATCH add/remove updating the mirror with no re-read) is covered by
    // testMembershipRemoveConvergesSameRun, where the member survives in another group so a
    // standalone PATCH-remove is actually issued.
  }

  /**
   * Membership add is write-tracked with no re-read: a member added to an ALREADY-provisioned
   * group is captured into the mirror on the same run, purely from the PATCH op=add write. The
   * pre-write retrieve sees the group without the new member and nothing re-reads the group's
   * members afterward, so only the add hook can make the mirror correct on this run.
   *
   * <p>testGroup is created with SUBJ0 (run 1); SUBJ1 is then added to it (run 2). The add hook
   * must land (testGroup,SUBJ1) so the mirror shows both members after the run that added SUBJ1.
   * This isolates the add hook on a later-run add, distinct from the add-during-create captured by
   * run 1 of {@link #testMembershipRemoveConvergesSameRun()}.
   */
  public void testMembershipAddConvergesSameRun() {
    String configId = "awsProvisioner";

    ScimProvisionerTestUtils.setupAwsExternalSystem();

    ScimProvisionerTestUtils.configureScimProvisioner(new ScimProvisionerTestConfigInput()
        .assignChangelogConsumerConfigId("awsScimProvTestCLC")
        .assignConfigId(configId)
        .assignBearerTokenExternalSystemConfigId("awsConfigId")
        .assignEntityDeleteType("deleteEntitiesIfNotExistInGrouper")
        .assignGroupDeleteType("deleteGroupsIfGrouperDeleted")
        .assignMembershipDeleteType("deleteMembershipsIfGrouperDeleted")
        .assignScimType("AWS")
        .assignGroupAttributeCount(2)
        .assignBearer(true)
        .addExtraConfig("loadEntitiesToGenericGrouperTable", "true")
        .addExtraConfig("loadGroupsToGenericGrouperTable", "true")
        .addExtraConfig("loadMembershipsToGenericGrouperTable", "true"));

    Stem stem = new StemSave(this.grouperSession).assignName("test").save();
    Group testGroup = new GroupSave(this.grouperSession).assignName("test:testGroup").save();
    testGroup.addMember(SubjectTestHelper.SUBJ0, false);

    GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision(configId);
    attributeValue.setTargetName(configId);
    attributeValue.setStemScopeString("sub");
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);

    // run 1: create testGroup + SUBJ0 + the one membership, converge.
    assertEquals(0, fullProvision().getRecordsWithErrors());

    GcGrouperSync gcGrouperSync = GcGrouperSyncDao.retrieveByProvisionerName(null, configId);
    assertNotNull("grouper_sync row should exist for " + configId, gcGrouperSync);

    assertEquals("group should be in the mirror after create", 1,
        countByProvisioner(configId, "grouper_prov_group"));
    assertEquals("SUBJ0 should be in the mirror after create", 1,
        countByProvisioner(configId, "grouper_prov_user"));
    assertEquals("the single membership should be in the mirror after create", 1,
        countByProvisioner(configId, "grouper_prov_mship"));

    // add SUBJ1 to the already-provisioned testGroup
    testGroup.addMember(SubjectTestHelper.SUBJ1, false);

    // run 2: the PATCH op=add fires; the add hook lands (testGroup,SUBJ1) in the native map with
    // no re-read of the group's members.
    assertEquals(0, fullProvision().getRecordsWithErrors());

    assertEquals("group should still be in the mirror", 1,
        countByProvisioner(configId, "grouper_prov_group"));
    assertEquals("both users should be in the mirror after the add", 2,
        countByProvisioner(configId, "grouper_prov_user"));
    // both memberships present: SUBJ0 (from create) + SUBJ1 (write-tracked add this run).
    assertEquals("the added membership should be in the mirror (write-tracked, not re-read)", 2,
        countByProvisioner(configId, "grouper_prov_mship"));
  }

  /**
   * Membership sync-back is write-tracked, not re-read: removing a member from a group that still
   * exists -- whose member also still exists (in another group) -- must drop that one membership
   * from the mirror on the SAME run, driven purely by the PATCH-remove write hook. The pre-write
   * retrieve still sees the member in the group and nothing re-reads the group's members after the
   * write, so only the hook can make the mirror correct on this run.
   *
   * <p>Two groups both hold SUBJ0; SUBJ0 is removed from testGroup only. Both groups survive and
   * SUBJ0 survives (still in otherGroup), so neither endpoint is missing for the surviving
   * membership -- the missing-endpoint cascade cannot be what drops testGroup's membership; only
   * the write hook can. Also asserts the add side: after the create run both memberships are
   * already in the mirror, captured from the PATCH-add writes rather than a later retrieve.
   */
  public void testMembershipRemoveConvergesSameRun() {
    String configId = "awsProvisioner";

    ScimProvisionerTestUtils.setupAwsExternalSystem();

    ScimProvisionerTestUtils.configureScimProvisioner(new ScimProvisionerTestConfigInput()
        .assignChangelogConsumerConfigId("awsScimProvTestCLC")
        .assignConfigId(configId)
        .assignBearerTokenExternalSystemConfigId("awsConfigId")
        .assignEntityDeleteType("deleteEntitiesIfNotExistInGrouper")
        .assignGroupDeleteType("deleteGroupsIfGrouperDeleted")
        .assignMembershipDeleteType("deleteMembershipsIfGrouperDeleted")
        .assignScimType("AWS")
        .assignGroupAttributeCount(2)
        .assignBearer(true)
        .addExtraConfig("loadEntitiesToGenericGrouperTable", "true")
        .addExtraConfig("loadGroupsToGenericGrouperTable", "true")
        .addExtraConfig("loadMembershipsToGenericGrouperTable", "true"));

    Stem stem = new StemSave(this.grouperSession).assignName("test").save();
    Group testGroup = new GroupSave(this.grouperSession).assignName("test:testGroup").save();
    Group otherGroup = new GroupSave(this.grouperSession).assignName("test:otherGroup").save();
    // SUBJ0 is in BOTH groups so that removing it from testGroup leaves it provisioned (still in
    // otherGroup) -- its entity is NOT deleted, so both endpoints of the surviving membership live.
    testGroup.addMember(SubjectTestHelper.SUBJ0, false);
    otherGroup.addMember(SubjectTestHelper.SUBJ0, false);

    GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision(configId);
    attributeValue.setTargetName(configId);
    attributeValue.setStemScopeString("sub");
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);

    // run 1: create both groups + SUBJ0 + both memberships in SCIM and converge them.
    assertEquals(0, fullProvision().getRecordsWithErrors());

    GcGrouperSync gcGrouperSync = GcGrouperSyncDao.retrieveByProvisionerName(null, configId);
    assertNotNull("grouper_sync row should exist for " + configId, gcGrouperSync);

    assertEquals("both groups should be in the mirror after create", 2,
        countByProvisioner(configId, "grouper_prov_group"));
    assertEquals("SUBJ0 should be in the mirror after create", 1,
        countByProvisioner(configId, "grouper_prov_user"));
    // both memberships captured from the PATCH-add writes on the SAME create run (not a later
    // retrieve): proves the membership-add write hook.
    assertEquals("both memberships should be in the mirror after create", 2,
        countByProvisioner(configId, "grouper_prov_mship"));

    // remove SUBJ0 from testGroup only (SUBJ0 stays in otherGroup)
    testGroup.deleteMember(SubjectTestHelper.SUBJ0);

    // run 2: the PATCH-remove fires and the write hook drops (testGroup,SUBJ0) from the native map.
    assertEquals(0, fullProvision().getRecordsWithErrors());

    // both groups survive and SUBJ0 survives -> the missing-endpoint cascade cannot be responsible
    // for the drop; the membership going away is purely the write hook's doing.
    assertEquals("both groups should still be in the mirror", 2,
        countByProvisioner(configId, "grouper_prov_group"));
    assertEquals("SUBJ0 should still be in the mirror (still in otherGroup)", 1,
        countByProvisioner(configId, "grouper_prov_user"));
    // only otherGroup's membership remains; testGroup's was write-tracked out.
    assertEquals("testGroup's membership should be gone, otherGroup's should remain", 1,
        countByProvisioner(configId, "grouper_prov_mship"));
  }

  /**
   * Membership replace (full-members write) is write-tracked the same way, with no re-read: in
   * {@code replaceMemberships=true} mode a membership change re-sends the group's ENTIRE member set
   * (SCIM PATCH op=replace), and the replace hook resets exactly that group's keys in the native
   * map to what was sent. Removing one member from a group therefore drops exactly that one
   * membership from the mirror on the same run, leaving the group's other members and every other
   * group untouched.
   *
   * <p>Two groups each hold SUBJ0 + SUBJ1; SUBJ1 is removed from testGroup only (it survives in
   * otherGroup, so its entity lives). testGroup's full member set is re-sent as {SUBJ0}; the
   * replace hook must reset testGroup's mirror memberships to just SUBJ0, leaving otherGroup's two
   * intact (3 total).
   */
  public void testMembershipReplaceConvergesSameRun() {
    String configId = "awsProvisioner";

    ScimProvisionerTestUtils.setupAwsExternalSystem();

    ScimProvisionerTestUtils.configureScimProvisioner(new ScimProvisionerTestConfigInput()
        .assignChangelogConsumerConfigId("awsScimProvTestCLC")
        .assignConfigId(configId)
        .assignBearerTokenExternalSystemConfigId("awsConfigId")
        .assignEntityDeleteType("deleteEntitiesIfNotExistInGrouper")
        .assignGroupDeleteType("deleteGroupsIfGrouperDeleted")
        .assignMembershipDeleteType("deleteMembershipsIfGrouperDeleted")
        .assignScimType("AWS")
        .assignGroupAttributeCount(2)
        .assignBearer(true)
        // replace mode: a membership change re-sends the group's full member set (PATCH op=replace)
        .addExtraConfig("replaceMemberships", "true")
        .addExtraConfig("loadEntitiesToGenericGrouperTable", "true")
        .addExtraConfig("loadGroupsToGenericGrouperTable", "true")
        .addExtraConfig("loadMembershipsToGenericGrouperTable", "true"));

    Stem stem = new StemSave(this.grouperSession).assignName("test").save();
    Group testGroup = new GroupSave(this.grouperSession).assignName("test:testGroup").save();
    Group otherGroup = new GroupSave(this.grouperSession).assignName("test:otherGroup").save();
    // both groups hold both members so removing SUBJ1 from testGroup leaves SUBJ1 provisioned
    // (still in otherGroup) -- its entity is NOT deleted, isolating the membership replace.
    testGroup.addMember(SubjectTestHelper.SUBJ0, false);
    testGroup.addMember(SubjectTestHelper.SUBJ1, false);
    otherGroup.addMember(SubjectTestHelper.SUBJ0, false);
    otherGroup.addMember(SubjectTestHelper.SUBJ1, false);

    GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision(configId);
    attributeValue.setTargetName(configId);
    attributeValue.setStemScopeString("sub");
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);

    // run 1: create both groups + both users + all four memberships, converged via the
    // full-members replace write.
    assertEquals(0, fullProvision().getRecordsWithErrors());

    GcGrouperSync gcGrouperSync = GcGrouperSyncDao.retrieveByProvisionerName(null, configId);
    assertNotNull("grouper_sync row should exist for " + configId, gcGrouperSync);

    assertEquals("both groups should be in the mirror after create", 2,
        countByProvisioner(configId, "grouper_prov_group"));
    assertEquals("both users should be in the mirror after create", 2,
        countByProvisioner(configId, "grouper_prov_user"));
    // 4 memberships: SUBJ0 + SUBJ1 in each of the 2 groups.
    assertEquals("all four memberships should be in the mirror after create", 4,
        countByProvisioner(configId, "grouper_prov_mship"));

    // remove SUBJ1 from testGroup only (SUBJ1 stays in otherGroup)
    testGroup.deleteMember(SubjectTestHelper.SUBJ1);

    // run 2: testGroup's full member set is re-sent as {SUBJ0}; the replace hook resets testGroup's
    // mirror memberships to exactly that, dropping (testGroup,SUBJ1) with no re-read.
    assertEquals(0, fullProvision().getRecordsWithErrors());

    assertEquals("both groups should still be in the mirror", 2,
        countByProvisioner(configId, "grouper_prov_group"));
    assertEquals("both users should still be in the mirror (SUBJ1 still in otherGroup)", 2,
        countByProvisioner(configId, "grouper_prov_user"));
    // testGroup now has only SUBJ0; otherGroup still has both -> 3 total.
    assertEquals("testGroup's SUBJ1 membership should be replaced out; the other three remain", 3,
        countByProvisioner(configId, "grouper_prov_mship"));
  }

  /** value_integer of the {@code active} attr for the single provisioned user of a sync, or null */
  private Long activeValueIntegerInMirror(long syncInternalId) {
    List<Long> values = new GcDbAccess().connectionName("grouper")
        .sql("select puv.value_integer from grouper_prov_user_attr_value puv "
            + "join grouper_prov_user_attr pua on pua.internal_id = puv.prov_user_attr_internal_id "
            + "join grouper_prov_user pu on pu.internal_id = puv.prov_user_internal_id "
            + "where pu.grouper_sync_internal_id = ? and pua.attribute_name = 'active'")
        .addBindVar(syncInternalId).selectList(Long.class);
    return values.isEmpty() ? null : values.get(0);
  }

  /** read (attribute_name -> internal_id) for an attr catalog of a single sync */
  private Map<String, Long> readCatalogIds(String catalogTable, long syncInternalId) {
    List<Object[]> rows = new GcDbAccess().connectionName("grouper")
        .sql("select attribute_name, internal_id from " + catalogTable
            + " where grouper_sync_internal_id = ?")
        .addBindVar(syncInternalId).selectList(Object[].class);
    Map<String, Long> result = new LinkedHashMap<String, Long>();
    for (Object[] row : rows) {
      if (row == null || row.length < 2 || row[0] == null || row[1] == null) continue;
      result.put(row[0].toString(), ((Number) row[1]).longValue());
    }
    return result;
  }

  private int countByProvisioner(String configId, String tableName) {
    return new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from " + tableName
            + " where grouper_sync_internal_id in (select internal_id from grouper_sync where provisioner_name = ?)")
        .addBindVar(configId).select(int.class);
  }
}
