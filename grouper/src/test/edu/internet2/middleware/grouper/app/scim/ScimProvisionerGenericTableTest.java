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
import edu.internet2.middleware.grouper.app.scim2Provisioning.AwsScim2MockServiceHandler;
import edu.internet2.middleware.grouper.app.scim2Provisioning.GrouperScim2Group;
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
    AwsScim2MockServiceHandler.ensureScimMockTables();
    TestRunner.run(new ScimProvisionerGenericTableTest("testFullProvisionPopulatesGenericTables"));
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
    AwsScim2MockServiceHandler.ensureScimMockTables();
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
    // clear the mock-membership-strategy override so it doesn't leak into subsequent tests
    // in the same JVM that don't want it.
    new GrouperDbConfig().configFileName("grouper.properties")
        .propertyName("grouperTest.scim2.mock.membershipStrategy.mode")
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

  /**
   * Incremental coverage for the SCIM sync-back wiring. Seeds via two full provisions
   * (which populates reporting tables and the SCIM target), then drives an incremental by
   * adding a third member to testGroup. After the incremental, verify:
   *
   * <ul>
   *   <li>Per-provisioner catalog ids for already-known attribute names are reused — this is
   *       the unique-key regression on {@code (sync, attribute_name)} we fixed on the LDAP
   *       side; SCIM exercises the same code path in {@code GrouperProvisioningLogic}.</li>
   *   <li>Catalog rows remain deduped per {@code (sync, attribute_name)}.</li>
   *   <li>No orphan FKs from attr_value rows to catalog.</li>
   *   <li>The {@code prov_user} / {@code prov_group} rows for our test entities are still
   *       present (incremental shouldn't accidentally delete unrelated rows).</li>
   * </ul>
   *
   * <p>This test does not assert that SUBJ2's membership is reflected in {@code prov_mship}
   * after the incremental — same 1-cycle-lag semantic as LDAP. SCIM's incremental performs
   * a read-before-write to compute the diff; that read captures pre-write state. SUBJ2 will
   * surface on the next read pass.
   */
  public void testIncrementalProvisionPopulatesGenericTables() {

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

    // seed: pass 1 inserts SCIM resources; pass 2 reads them back and populates reporting
    assertEquals(0, fullProvision().getRecordsWithErrors());
    assertEquals(0, fullProvision().getRecordsWithErrors());

    GcGrouperSync gcGrouperSync = GcGrouperSyncDao.retrieveByProvisionerName(null, configId);
    assertNotNull("grouper_sync row should exist for " + configId, gcGrouperSync);
    long syncInternalId = gcGrouperSync.getInternalId();

    // snapshot catalogs to verify ids are reused after the incremental
    Map<String, Long> groupCatalogBefore = readCatalogIds("grouper_prov_group_attr", syncInternalId);
    Map<String, Long> userCatalogBefore = readCatalogIds("grouper_prov_user_attr", syncInternalId);
    assertTrue("expected >=1 group catalog entry after seeding, got " + groupCatalogBefore.size(),
        groupCatalogBefore.size() >= 1);
    assertTrue("expected >=1 user catalog entry after seeding, got " + userCatalogBefore.size(),
        userCatalogBefore.size() >= 1);

    int provGroupRowsBefore = countByProvisioner(configId, "grouper_prov_group");
    int provUserRowsBefore = countByProvisioner(configId, "grouper_prov_user");

    // drive an incremental: add a third subject. SCIM's incremental will retrieveScimGroup
    // and retrieveScimUser to compute the diff, exercising the by-id capture path.
    testGroup.addMember(SubjectTestHelper.SUBJ2, false);
    incrementalProvision();

    // testGroup's prov_group row should still exist (no spurious delete from the scoped sync)
    int provGroupRowsAfter = countByProvisioner(configId, "grouper_prov_group");
    assertTrue("incremental should not shrink prov_group; before=" + provGroupRowsBefore
        + " after=" + provGroupRowsAfter, provGroupRowsAfter >= provGroupRowsBefore);

    int provUserRowsAfter = countByProvisioner(configId, "grouper_prov_user");
    assertTrue("incremental should not shrink prov_user; before=" + provUserRowsBefore
        + " after=" + provUserRowsAfter, provUserRowsAfter >= provUserRowsBefore);

    // every previously-known catalog name keeps its internal_id (regression: incremental
    // used to reserve fresh ids and trip the (sync, attribute_name) unique key)
    Map<String, Long> groupCatalogAfter = readCatalogIds("grouper_prov_group_attr", syncInternalId);
    for (Map.Entry<String, Long> beforeEntry : groupCatalogBefore.entrySet()) {
      Long afterId = groupCatalogAfter.get(beforeEntry.getKey());
      assertNotNull("group catalog row for '" + beforeEntry.getKey()
          + "' should still exist after incremental", afterId);
      assertEquals("group catalog internal_id for '" + beforeEntry.getKey()
          + "' should be reused after incremental",
          beforeEntry.getValue(), afterId);
    }
    Map<String, Long> userCatalogAfter = readCatalogIds("grouper_prov_user_attr", syncInternalId);
    for (Map.Entry<String, Long> beforeEntry : userCatalogBefore.entrySet()) {
      Long afterId = userCatalogAfter.get(beforeEntry.getKey());
      assertNotNull("user catalog row for '" + beforeEntry.getKey()
          + "' should still exist after incremental", afterId);
      assertEquals("user catalog internal_id for '" + beforeEntry.getKey()
          + "' should be reused after incremental",
          beforeEntry.getValue(), afterId);
    }

    // catalogs remain deduped per (sync, attribute_name) after the incremental
    int dupGroupAttr = new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from (select grouper_sync_internal_id, attribute_name, count(*) c "
            + "from grouper_prov_group_attr where grouper_sync_internal_id = ? "
            + "group by grouper_sync_internal_id, attribute_name having count(*) > 1) t")
        .addBindVar(syncInternalId).select(int.class);
    assertEquals("group attr catalog should still be deduped per (sync,name) after incremental",
        0, dupGroupAttr);

    int dupUserAttr = new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from (select grouper_sync_internal_id, attribute_name, count(*) c "
            + "from grouper_prov_user_attr where grouper_sync_internal_id = ? "
            + "group by grouper_sync_internal_id, attribute_name having count(*) > 1) t")
        .addBindVar(syncInternalId).select(int.class);
    assertEquals("user attr catalog should still be deduped per (sync,name) after incremental",
        0, dupUserAttr);

    // no orphan FKs after the incremental
    int orphanGroupValues = new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from grouper_prov_group_attr_value gpv "
            + "left join grouper_prov_group_attr pa on gpv.prov_group_attr_internal_id = pa.internal_id "
            + "join grouper_prov_group pg on gpv.prov_group_internal_id = pg.internal_id "
            + "where pg.grouper_sync_internal_id = ? and pa.internal_id is null")
        .addBindVar(syncInternalId).select(int.class);
    assertEquals("no group_attr_value rows should orphan their catalog FK after incremental",
        0, orphanGroupValues);

    int orphanUserValues = new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from grouper_prov_user_attr_value puv "
            + "left join grouper_prov_user_attr pa on puv.prov_user_attr_internal_id = pa.internal_id "
            + "join grouper_prov_user pu on puv.prov_user_internal_id = pu.internal_id "
            + "where pu.grouper_sync_internal_id = ? and pa.internal_id is null")
        .addBindVar(syncInternalId).select(int.class);
    assertEquals("no user_attr_value rows should orphan their catalog FK after incremental",
        0, orphanUserValues);
  }

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
    // createGroup handler does (see AwsScim2MockServiceHandler.postGroup)
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
