package edu.internet2.middleware.grouper.app.ldapProvisioning;

import java.util.LinkedHashMap;
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
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.ldap.LdapAttribute;
import edu.internet2.middleware.grouper.ldap.LdapEntry;
import edu.internet2.middleware.grouper.ldap.LdapSessionUtils;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSync;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncDao;
import edu.internet2.middleware.subject.provider.SourceManager;
import junit.textui.TestRunner;

/**
 * Verify that LDAP full provisioning populates the generic provisioner reporting
 * tables when the load flags are on.
 *
 * <p>The simple posix config used here is group-target style: groups get created in LDAP
 * with members stored in the {@code description} attribute. Under strict-native semantics:
 * <ul>
 *   <li>grouper_prov_group is populated from {@code retrieveAllGroups} on the LDAP target
 *       — but only on a run where the group is actually present in LDAP. The first run
 *       inserts the group; the second run reads it back. We do two passes here.</li>
 *   <li>Memberships in this style live as {@code description} values on each group. They
 *       appear as native memberships when the search returns the description attribute
 *       (via {@code includeAllMembershipsIfApplicable}).</li>
 *   <li>Entity-side reporting is not exercised by this config (no LDAP query for users).
 *       A separate test using an entity-attribute provisioning config would cover it.</li>
 * </ul>
 */
public class LdapProvisionerGenericTableTest extends GrouperProvisioningBaseTest {

  public static void main(String[] args) {
    TestRunner.run(new LdapProvisionerGenericTableTest("testFullProvisionPopulatesGenericTablesGroupSide"));
  }

  public LdapProvisionerGenericTableTest() {
    super();
  }

  public LdapProvisionerGenericTableTest(String name) {
    super(name);
  }

  private GrouperSession grouperSession = null;

  @Override
  protected void setUp() {
    super.setUp();

    try {
      this.grouperSession = GrouperSession.startRootSession();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    LdapProvisionerTestUtils.stopAndRemoveLdapContainer();
    LdapProvisionerTestUtils.startLdapContainer();
    LdapProvisionerTestUtils.setupSubjectSource();
  }

  @Override
  protected void tearDown() {
    // matches SimpleLdapProvisionerTest convention: leave the container running so the
    // next test's GrouperStartup constructor (which checks personLdapSource via LDAP)
    // doesn't crash. setUp() removes and re-creates the container at the start of each run.
    SourceManager.getInstance().internal_removeSource("personLdapSource");
    GrouperSession.stopQuietly(this.grouperSession);
    super.tearDown();
  }

  @Override
  public String defaultConfigId() {
    return "ldapProvTest";
  }

  /**
   * Two-pass full provision: pass 1 inserts groups into LDAP, pass 2 reads them back via
   * {@code retrieveAllGroups} and the new strict-native loader populates grouper_prov_group
   * + the per-provisioner attribute catalog + value rows.
   */
  public void testFullProvisionPopulatesGenericTablesGroupSide() {

    String configId = "ldapProvTest";

    LdapProvisionerTestUtils.configureLdapProvisioner(
        new LdapProvisionerTestConfigInput()
            .assignPosixGroup(true)
            .assignMembershipAttribute("description")
            .assignEntityAttributeCount(1)
            .assignSubjectSourcesToProvision("jdbc")
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
    assertEquals(0, countByProvisioner(configId, "grouper_prov_group_attr"));

    // PASS 1: provisioner queries empty LDAP, inserts testGroup. prov_group still 0
    // because retrieveAllGroups happened before the insert.
    GrouperProvisioningOutput passOne = fullProvision();
    GrouperProvisioner.retrieveInternalLastProvisioner();
    assertEquals(0, passOne.getRecordsWithErrors());

    // PASS 2: provisioner queries LDAP, sees testGroup, populates the reporting tables.
    GrouperProvisioningOutput passTwo = fullProvision();
    GrouperProvisioner.retrieveInternalLastProvisioner();
    assertEquals(0, passTwo.getRecordsWithErrors());

    GcGrouperSync gcGrouperSync = GcGrouperSyncDao.retrieveByProvisionerName(null, configId);
    assertNotNull("grouper_sync row should exist for " + configId, gcGrouperSync);
    long syncInternalId = gcGrouperSync.getInternalId();

    // exactly one prov_group row for testGroup (DN starts with cn=test:testGroup,)
    int testGroupRows = new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from grouper_prov_group "
            + "where grouper_sync_internal_id = ? and target_group_id like 'cn=test:testGroup,%'")
        .addBindVar(syncInternalId).select(int.class);
    assertEquals("expected exactly 1 prov_group row for testGroup", 1, testGroupRows);

    // group attribute catalog: per-provisioner names, deduped (no per-group duplication)
    int groupAttrCatalogCount = countByProvisioner(configId, "grouper_prov_group_attr");
    if (groupAttrCatalogCount < 1) {
      // diagnostic dump to figure out why the catalog is empty
      java.util.List<String> dns = new GcDbAccess().connectionName("grouper")
          .sql("select target_group_id from grouper_prov_group where grouper_sync_internal_id = ?")
          .addBindVar(syncInternalId).selectList(String.class);
      int attrValueCount = new GcDbAccess().connectionName("grouper")
          .sql("select count(*) from grouper_prov_group_attr_value gpv join grouper_prov_group pg on gpv.prov_group_internal_id = pg.internal_id where pg.grouper_sync_internal_id = ?")
          .addBindVar(syncInternalId).select(int.class);
      Map<String, Object> debugMap = GrouperProvisioner.retrieveInternalLastProvisioner().getDebugMap();
      java.util.Map<String, Object> filteredDebug = new java.util.LinkedHashMap<String, Object>();
      for (java.util.Map.Entry<String, Object> e : debugMap.entrySet()) {
        if (e.getKey().toLowerCase().contains("generic") || e.getKey().toLowerCase().contains("native")) {
          filteredDebug.put(e.getKey(), e.getValue());
        }
      }
      fail("expected at least 1 group attr catalog row, got " + groupAttrCatalogCount
          + "; prov_group DNs=" + dns
          + "; prov_group_attr_value rows=" + attrValueCount
          + "; debug entries=" + filteredDebug);
    }

    // catalog dedup invariant: no two catalog rows for the same (sync, name)
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

    // testGroup should have at least one attribute value row (cn, gidNumber, etc.)
    int testGroupValueRows = new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from grouper_prov_group_attr_value gpv "
            + "join grouper_prov_group pg on gpv.prov_group_internal_id = pg.internal_id "
            + "where pg.grouper_sync_internal_id = ? and pg.target_group_id like 'cn=test:testGroup,%'")
        .addBindVar(syncInternalId).select(int.class);
    assertTrue("expected >=1 attr value row for testGroup, got " + testGroupValueRows,
        testGroupValueRows >= 1);
  }

  /**
   * Entity-side test using the {@code harvardGroupOfNames} strategy. That strategy
   * configures the full entity-side LDAP query path (entityMatchingAttribute0name=uid,
   * selectAllEntities=true, targetEntityAttribute.0.name=ldap_dn, etc.), which our
   * strict-native loader needs in order to populate grouper_prov_user from a real
   * {@code retrieveAllEntities} call against the LDAP container.
   */
  public void testFullProvisionPopulatesGenericTablesEntitySide() {

    String configId = "ldapProvTest";

    LdapProvisionerTestUtils.configureLdapProvisioner(
        new LdapProvisionerTestConfigInput()
            .assignProvisioningStrategy("harvardGroupOfNames")
            .assignSubjectSourcesToProvision("jdbc")
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
    assertEquals(0, countByProvisioner(configId, "grouper_prov_user"));
    assertEquals(0, countByProvisioner(configId, "grouper_prov_user_attr"));

    // PASS 1: provisioner creates LDAP entries for testGroup. Strict-native means
    // prov_user / prov_group still 0 since retrieveAll* runs before the inserts.
    GrouperProvisioningOutput passOne = fullProvision();
    GrouperProvisioner.retrieveInternalLastProvisioner();
    assertEquals(0, passOne.getRecordsWithErrors());

    // PASS 2: provisioner now reads the LDAP target back; the new strict-native loader
    // populates prov_user from the entity native list.
    GrouperProvisioningOutput passTwo = fullProvision();
    GrouperProvisioner.retrieveInternalLastProvisioner();
    assertEquals(0, passTwo.getRecordsWithErrors());

    GcGrouperSync gcGrouperSync = GcGrouperSyncDao.retrieveByProvisionerName(null, configId);
    assertNotNull("grouper_sync row should exist for " + configId, gcGrouperSync);
    long syncInternalId = gcGrouperSync.getInternalId();

    // The harvardGroupOfNames strategy doesn't insert entities (insertEntities=false,
    // customizeEntityCrud=false), so our test subjects never land in LDAP. But the LDAP
    // container has ~2000 pre-populated users, and retrieveAllEntities returns them all.
    // Strict-native reporting picks them up.
    int userRowCount = countByProvisioner(configId, "grouper_prov_user");
    assertTrue("expected many prov_user rows from native LDAP fetch (~2000), got " + userRowCount,
        userRowCount >= 100);

    // every prov_user row should have a DN-style target_user_id
    int badDnRows = new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from grouper_prov_user "
            + "where grouper_sync_internal_id = ? and target_user_id not like 'uid=%,ou=People,%'")
        .addBindVar(syncInternalId).select(int.class);
    assertEquals("all prov_user rows should be DNs under ou=People", 0, badDnRows);

    // user attribute catalog should have entries (per-provisioner-per-name)
    int userAttrCatalogCount = countByProvisioner(configId, "grouper_prov_user_attr");
    assertTrue("expected user attr catalog entries from native fetch, got " + userAttrCatalogCount,
        userAttrCatalogCount >= 1);

    // catalog dedup invariant
    int dupUserAttr = new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from (select grouper_sync_internal_id, attribute_name, count(*) c "
            + "from grouper_prov_user_attr where grouper_sync_internal_id = ? "
            + "group by grouper_sync_internal_id, attribute_name having count(*) > 1) t")
        .addBindVar(syncInternalId).select(int.class);
    assertEquals("user attr catalog should be deduped per (sync,name)", 0, dupUserAttr);

    // value rows must reference real catalog rows
    int orphanUserValues = new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from grouper_prov_user_attr_value puv "
            + "left join grouper_prov_user_attr pa on puv.prov_user_attr_internal_id = pa.internal_id "
            + "join grouper_prov_user pu on puv.prov_user_internal_id = pu.internal_id "
            + "where pu.grouper_sync_internal_id = ? and pa.internal_id is null")
        .addBindVar(syncInternalId).select(int.class);
    assertEquals("no user_attr_value rows should orphan their catalog FK", 0, orphanUserValues);

    // every prov_user should have at least one attribute value (uid, at minimum)
    int totalUserValueRows = new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from grouper_prov_user_attr_value puv "
            + "join grouper_prov_user pu on puv.prov_user_internal_id = pu.internal_id "
            + "where pu.grouper_sync_internal_id = ?")
        .addBindVar(syncInternalId).select(int.class);
    assertTrue("expected many attr value rows from native fetch, got " + totalUserValueRows,
        totalUserValueRows >= userRowCount);
  }

  /**
   * Exercise the {@code nativeAttributesGroups} config in its CSV form. After provisioning,
   * confirm that every requested attribute lands in the catalog and that multi-valued LDAP
   * attributes (objectClass, description) produce multiple value rows.
   */
  public void testNativeAttributesGroupsCsvForm() {
    String configId = "ldapProvTest";

    LdapProvisionerTestUtils.configureLdapProvisioner(
        new LdapProvisionerTestConfigInput()
            .assignPosixGroup(true)
            .assignMembershipAttribute("description")
            .assignEntityAttributeCount(1)
            .assignSubjectSourcesToProvision("jdbc")
            .addExtraConfig("loadEntitiesToGenericGrouperTable", "true")
            .addExtraConfig("loadGroupsToGenericGrouperTable", "true")
            .addExtraConfig("loadMembershipsToGenericGrouperTable", "true")
            .addExtraConfig("nativeAttributesGroups", "objectClass, description, cn"));

    runNativeAttributesGroupsAssertions(configId);
  }

  /**
   * Exercise the {@code nativeAttributesGroups} config in its JSON form (equivalent attribute
   * set to the CSV test). Same multi-value expectations.
   */
  public void testNativeAttributesGroupsJsonForm() {
    String configId = "ldapProvTest";

    String json = "[{\"name\":\"objectClass\"},"
        + "{\"name\":\"description\"},"
        + "{\"name\":\"cn\",\"type\":\"string\"}]";

    LdapProvisionerTestUtils.configureLdapProvisioner(
        new LdapProvisionerTestConfigInput()
            .assignPosixGroup(true)
            .assignMembershipAttribute("description")
            .assignEntityAttributeCount(1)
            .assignSubjectSourcesToProvision("jdbc")
            .addExtraConfig("loadEntitiesToGenericGrouperTable", "true")
            .addExtraConfig("loadGroupsToGenericGrouperTable", "true")
            .addExtraConfig("loadMembershipsToGenericGrouperTable", "true")
            .addExtraConfig("nativeAttributesGroups", json));

    runNativeAttributesGroupsAssertions(configId);
  }

  /**
   * Shared two-pass scaffold + assertion block reused by the CSV and JSON tests above.
   * After pass 2, the configured group should appear in {@code grouper_prov_group} and the
   * group attribute catalog should contain each native attribute we requested. Multi-valued
   * LDAP attributes (objectClass, description) must produce multiple value rows.
   */
  private void runNativeAttributesGroupsAssertions(String configId) {

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

    // PASS 1: insert group into LDAP (description gets test.subject.0 + test.subject.1, so
    // it ends up multi-valued); strict-native reporting tables still empty after pass 1.
    GrouperProvisioningOutput passOne = fullProvision();
    GrouperProvisioner.retrieveInternalLastProvisioner();
    assertEquals(0, passOne.getRecordsWithErrors());

    // PASS 2: retrieveAllGroups returns the LDAP group with all its attributes; native
    // reporting populates prov_group + per-name catalog + per-value rows.
    GrouperProvisioningOutput passTwo = fullProvision();
    GrouperProvisioner.retrieveInternalLastProvisioner();
    assertEquals(0, passTwo.getRecordsWithErrors());

    GcGrouperSync gcGrouperSync = GcGrouperSyncDao.retrieveByProvisionerName(null, configId);
    assertNotNull("grouper_sync row should exist for " + configId, gcGrouperSync);
    long syncInternalId = gcGrouperSync.getInternalId();

    assertEquals("expected 1 prov_group row for testGroup", 1,
        (int) new GcDbAccess().connectionName("grouper")
            .sql("select count(*) from grouper_prov_group "
                + "where grouper_sync_internal_id = ? and target_group_id like 'cn=test:testGroup,%'")
            .addBindVar(syncInternalId).select(int.class));

    // each of the three requested native attributes should show up in the catalog
    for (String attributeName : new String[] {"objectClass", "description", "cn"}) {
      int catalogRowCount = new GcDbAccess().connectionName("grouper")
          .sql("select count(*) from grouper_prov_group_attr "
              + "where grouper_sync_internal_id = ? and attribute_name = ?")
          .addBindVar(syncInternalId).addBindVar(attributeName).select(int.class);
      assertEquals("expected exactly 1 catalog row for attribute '" + attributeName + "'",
          1, catalogRowCount);
    }

    // objectClass is multi-valued in LDAP (top + posixGroup) → 2 value rows
    int objectClassValueCount = new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from grouper_prov_group_attr_value gpv "
            + "join grouper_prov_group_attr gpa on gpa.internal_id = gpv.prov_group_attr_internal_id "
            + "join grouper_prov_group pg on pg.internal_id = gpv.prov_group_internal_id "
            + "where pg.grouper_sync_internal_id = ? and gpa.attribute_name = 'objectClass'")
        .addBindVar(syncInternalId).select(int.class);
    assertEquals("expected 2 value rows for multi-valued objectClass (top, posixGroup)",
        2, objectClassValueCount);

    // description was used as the membership attribute, so pass 1 populated it with both
    // subject ids; pass 2 reads them back → 2 value rows
    int descriptionValueCount = new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from grouper_prov_group_attr_value gpv "
            + "join grouper_prov_group_attr gpa on gpa.internal_id = gpv.prov_group_attr_internal_id "
            + "join grouper_prov_group pg on pg.internal_id = gpv.prov_group_internal_id "
            + "where pg.grouper_sync_internal_id = ? and gpa.attribute_name = 'description'")
        .addBindVar(syncInternalId).select(int.class);
    assertEquals("expected 2 value rows for multi-valued description (subject.0, subject.1)",
        2, descriptionValueCount);

    // cn is single-valued → exactly 1 value row
    int cnValueCount = new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from grouper_prov_group_attr_value gpv "
            + "join grouper_prov_group_attr gpa on gpa.internal_id = gpv.prov_group_attr_internal_id "
            + "join grouper_prov_group pg on pg.internal_id = gpv.prov_group_internal_id "
            + "where pg.grouper_sync_internal_id = ? and gpa.attribute_name = 'cn'")
        .addBindVar(syncInternalId).select(int.class);
    assertEquals("expected 1 value row for single-valued cn", 1, cnValueCount);

    // the cn value should be 'test:testGroup' (resolved via the grouper_dictionary join in
    // grouper_prov_group_attr_v); sanity-check via the view
    String cnString = new GcDbAccess().connectionName("grouper")
        .sql("select value_string from grouper_prov_group_attr_v "
            + "where grouper_sync_id = (select id from grouper_sync where internal_id = ?) "
            + "and attribute_name = 'cn'")
        .addBindVar(syncInternalId).select(String.class);
    assertEquals("test:testGroup", cnString);
  }

  // TODO re-enable when generic-tables framework supports incremental sync-back
//   /**
//    * Group-side incremental coverage: after seeding via full provision, drive an incremental
//    * pass and assert the per-provisioner catalog ids stay stable (no new rows for already-known
//    * attribute names; this is the unique-constraint regression we fixed), no duplicates, and
//    * the updated membership reaches the reporting tables.
//    */
//   public void testIncrementalProvisionPopulatesGenericTablesGroupSide() {
// 
//     String configId = "ldapProvTest";
// 
//     LdapProvisionerTestUtils.configureLdapProvisioner(
//         new LdapProvisionerTestConfigInput()
//             .assignPosixGroup(true)
//             .assignMembershipAttribute("description")
//             .assignEntityAttributeCount(1)
//             .assignSubjectSourcesToProvision("jdbc")
//             .addExtraConfig("loadEntitiesToGenericGrouperTable", "true")
//             .addExtraConfig("loadGroupsToGenericGrouperTable", "true")
//             .addExtraConfig("loadMembershipsToGenericGrouperTable", "true"));
// 
//     Stem stem = new StemSave(this.grouperSession).assignName("test").save();
//     Group testGroup = new GroupSave(this.grouperSession).assignName("test:testGroup").save();
//     testGroup.addMember(SubjectTestHelper.SUBJ0, false);
// 
//     GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
//     attributeValue.setDirectAssignment(true);
//     attributeValue.setDoProvision(configId);
//     attributeValue.setTargetName(configId);
//     attributeValue.setStemScopeString("sub");
//     GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);
// 
//     // seed: pass 1 inserts the group; pass 2 reads it back and populates the reporting tables
//     assertEquals(0, fullProvision().getRecordsWithErrors());
//     assertEquals(0, fullProvision().getRecordsWithErrors());
// 
//     GcGrouperSync gcGrouperSync = GcGrouperSyncDao.retrieveByProvisionerName(null, configId);
//     assertNotNull("grouper_sync row should exist for " + configId, gcGrouperSync);
//     long syncInternalId = gcGrouperSync.getInternalId();
// 
//     // snapshot catalog (attribute_name -> internal_id) so we can verify reuse after incremental
//     Map<String, Long> catalogBefore = readGroupAttrCatalogIds(syncInternalId);
//     assertTrue("expected at least 1 group catalog entry after seeding, got " + catalogBefore.size(),
//         catalogBefore.size() >= 1);
// 
//     int testGroupRowsBefore = new GcDbAccess().connectionName("grouper")
//         .sql("select count(*) from grouper_prov_group "
//             + "where grouper_sync_internal_id = ? and target_group_id like 'cn=test:testGroup,%'")
//         .addBindVar(syncInternalId).select(int.class);
//     assertEquals("seeded testGroup row count", 1, testGroupRowsBefore);
// 
//     // change Grouper state to drive an incremental: add a second member so description
//     // becomes multi-valued. retrieveGroups (by key) path runs during the incremental.
//     testGroup.addMember(SubjectTestHelper.SUBJ1, false);
//     incrementalProvision();
// 
//     // post-incremental: same provisioner sync_internal_id; exactly 1 prov_group row still
//     int testGroupRowsAfter = new GcDbAccess().connectionName("grouper")
//         .sql("select count(*) from grouper_prov_group "
//             + "where grouper_sync_internal_id = ? and target_group_id like 'cn=test:testGroup,%'")
//         .addBindVar(syncInternalId).select(int.class);
//     assertEquals("incremental should not create a duplicate prov_group row", 1, testGroupRowsAfter);
// 
//     // every previously-known catalog name keeps its internal_id (regression: incremental used
//     // to reserve a fresh id and trip grouper_prov_groupat_idx1 on the (sync,name) unique key)
//     Map<String, Long> catalogAfter = readGroupAttrCatalogIds(syncInternalId);
//     for (Map.Entry<String, Long> beforeEntry : catalogBefore.entrySet()) {
//       Long afterId = catalogAfter.get(beforeEntry.getKey());
//       assertNotNull("catalog row for '" + beforeEntry.getKey() + "' should still exist after incremental",
//           afterId);
//       assertEquals("catalog internal_id for '" + beforeEntry.getKey() + "' should be reused after incremental",
//           beforeEntry.getValue(), afterId);
//     }
// 
//     // catalog must remain deduped per (sync, name)
//     int dupGroupAttr = new GcDbAccess().connectionName("grouper")
//         .sql("select count(*) from (select grouper_sync_internal_id, attribute_name, count(*) c "
//             + "from grouper_prov_group_attr where grouper_sync_internal_id = ? "
//             + "group by grouper_sync_internal_id, attribute_name having count(*) > 1) t")
//         .addBindVar(syncInternalId).select(int.class);
//     assertEquals("group attr catalog should still be deduped per (sync,name) after incremental",
//         0, dupGroupAttr);
// 
//     // no orphan FKs across the value rows after incremental
//     int orphanGroupValues = new GcDbAccess().connectionName("grouper")
//         .sql("select count(*) from grouper_prov_group_attr_value gpv "
//             + "left join grouper_prov_group_attr pa on gpv.prov_group_attr_internal_id = pa.internal_id "
//             + "join grouper_prov_group pg on gpv.prov_group_internal_id = pg.internal_id "
//             + "where pg.grouper_sync_internal_id = ? and pa.internal_id is null")
//         .addBindVar(syncInternalId).select(int.class);
//     assertEquals("no group_attr_value rows should orphan their catalog FK after incremental",
//         0, orphanGroupValues);
//   }

  // TODO re-enable when generic-tables framework supports incremental sync-back
//   /**
//    * Entity-side incremental coverage using harvardGroupOfNames. The LDAP container is
//    * pre-populated with ~2000 users; the full-pass seeds prov_user from retrieveAllEntities.
//    * After an incremental triggered by a group membership change, we verify that the per-user
//    * attribute catalog ids are still stable and no duplicate rows surface.
//    */
//   public void testIncrementalProvisionPopulatesGenericTablesEntitySide() {
// 
//     String configId = "ldapProvTest";
// 
//     LdapProvisionerTestUtils.configureLdapProvisioner(
//         new LdapProvisionerTestConfigInput()
//             .assignProvisioningStrategy("harvardGroupOfNames")
//             .assignSubjectSourcesToProvision("jdbc")
//             .addExtraConfig("loadEntitiesToGenericGrouperTable", "true")
//             .addExtraConfig("loadGroupsToGenericGrouperTable", "true")
//             .addExtraConfig("loadMembershipsToGenericGrouperTable", "true"));
// 
//     Stem stem = new StemSave(this.grouperSession).assignName("test").save();
//     Group testGroup = new GroupSave(this.grouperSession).assignName("test:testGroup").save();
//     testGroup.addMember(SubjectTestHelper.SUBJ0, false);
// 
//     GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
//     attributeValue.setDirectAssignment(true);
//     attributeValue.setDoProvision(configId);
//     attributeValue.setTargetName(configId);
//     attributeValue.setStemScopeString("sub");
//     GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);
// 
//     // seed: two full passes, second one populates the reporting tables
//     assertEquals(0, fullProvision().getRecordsWithErrors());
//     assertEquals(0, fullProvision().getRecordsWithErrors());
// 
//     GcGrouperSync gcGrouperSync = GcGrouperSyncDao.retrieveByProvisionerName(null, configId);
//     assertNotNull("grouper_sync row should exist for " + configId, gcGrouperSync);
//     long syncInternalId = gcGrouperSync.getInternalId();
// 
//     // snapshot user-side catalog before incremental
//     Map<String, Long> userCatalogBefore = readUserAttrCatalogIds(syncInternalId);
//     assertTrue("expected at least 1 user catalog entry after seeding, got " + userCatalogBefore.size(),
//         userCatalogBefore.size() >= 1);
//     int userRowsBefore = countByProvisioner(configId, "grouper_prov_user");
//     assertTrue("expected many prov_user rows after seeding, got " + userRowsBefore,
//         userRowsBefore >= 100);
// 
//     // drive an incremental
//     testGroup.addMember(SubjectTestHelper.SUBJ1, false);
//     incrementalProvision();
// 
//     // user-side catalog ids must be reused (the per-provisioner attribute names don't
//     // depend on which entry triggered the incremental — they live at the sync layer)
//     Map<String, Long> userCatalogAfter = readUserAttrCatalogIds(syncInternalId);
//     for (Map.Entry<String, Long> beforeEntry : userCatalogBefore.entrySet()) {
//       Long afterId = userCatalogAfter.get(beforeEntry.getKey());
//       assertNotNull("user catalog row for '" + beforeEntry.getKey() + "' should still exist after incremental",
//           afterId);
//       assertEquals("user catalog internal_id for '" + beforeEntry.getKey() + "' should be reused",
//           beforeEntry.getValue(), afterId);
//     }
// 
//     // no duplicate catalog rows
//     int dupUserAttr = new GcDbAccess().connectionName("grouper")
//         .sql("select count(*) from (select grouper_sync_internal_id, attribute_name, count(*) c "
//             + "from grouper_prov_user_attr where grouper_sync_internal_id = ? "
//             + "group by grouper_sync_internal_id, attribute_name having count(*) > 1) t")
//         .addBindVar(syncInternalId).select(int.class);
//     assertEquals("user attr catalog should still be deduped per (sync,name) after incremental",
//         0, dupUserAttr);
// 
//     // no orphan FKs on the value rows
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
   * Native-attributes (CSV form) under incremental: seed via full provision, then trigger an
   * incremental and verify the configured native attributes still resolve and multi-valued
   * LDAP attributes still produce multiple value rows.
   */
  public void testNativeAttributesGroupsCsvFormIncremental() {
    String configId = "ldapProvTest";

    LdapProvisionerTestUtils.configureLdapProvisioner(
        new LdapProvisionerTestConfigInput()
            .assignPosixGroup(true)
            .assignMembershipAttribute("description")
            .assignEntityAttributeCount(1)
            .assignSubjectSourcesToProvision("jdbc")
            .addExtraConfig("loadEntitiesToGenericGrouperTable", "true")
            .addExtraConfig("loadGroupsToGenericGrouperTable", "true")
            .addExtraConfig("loadMembershipsToGenericGrouperTable", "true")
            .addExtraConfig("nativeAttributesGroups", "objectClass, description, cn"));

    runNativeAttributesGroupsIncrementalAssertions(configId);
  }

  /**
   * Native-attributes (JSON form) under incremental — same expectations as the CSV variant.
   */
  public void testNativeAttributesGroupsJsonFormIncremental() {
    String configId = "ldapProvTest";

    String json = "[{\"name\":\"objectClass\"},"
        + "{\"name\":\"description\"},"
        + "{\"name\":\"cn\",\"type\":\"string\"}]";

    LdapProvisionerTestUtils.configureLdapProvisioner(
        new LdapProvisionerTestConfigInput()
            .assignPosixGroup(true)
            .assignMembershipAttribute("description")
            .assignEntityAttributeCount(1)
            .assignSubjectSourcesToProvision("jdbc")
            .addExtraConfig("loadEntitiesToGenericGrouperTable", "true")
            .addExtraConfig("loadGroupsToGenericGrouperTable", "true")
            .addExtraConfig("loadMembershipsToGenericGrouperTable", "true")
            .addExtraConfig("nativeAttributesGroups", json));

    runNativeAttributesGroupsIncrementalAssertions(configId);
  }

  /**
   * Shared scaffold for the CSV/JSON incremental variants. Seeds Grouper with BOTH SUBJ0 and
   * SUBJ1 so the LDAP description attribute lands multi-valued during the seed pass and the
   * reporting tables already show 2 description value rows. Then drives an incremental by
   * adding SUBJ2, which triggers {@code retrieveGroupByDn} during the incremental's
   * read-before-write step. That read captures the pre-write LDAP state — description = [
   * SUBJ0, SUBJ1] — so the reporting tables still show 2 description rows afterwards.
   *
   * <p>This explicitly documents the "1-cycle lag" semantic of read-only capture: reporting
   * tables reflect what the daemon last observed at the target, not the post-write state.
   * The SUBJ2 add will appear in reporting on the NEXT daemon cycle. Write-side capture is a
   * future opt-in via a {@code readAfterWrite} flag.
   */
  private void runNativeAttributesGroupsIncrementalAssertions(String configId) {

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

    // seed via two full passes (pass 1 inserts into LDAP, pass 2 reads back + reports).
    // LDAP description ends up with both SUBJ0 and SUBJ1 (multi-valued), and reporting
    // tables show 2 description value rows after pass 2.
    assertEquals(0, fullProvision().getRecordsWithErrors());
    assertEquals(0, fullProvision().getRecordsWithErrors());

    GcGrouperSync gcGrouperSync = GcGrouperSyncDao.retrieveByProvisionerName(null, configId);
    assertNotNull("grouper_sync row should exist for " + configId, gcGrouperSync);
    long syncInternalId = gcGrouperSync.getInternalId();

    Map<String, Long> catalogBefore = readGroupAttrCatalogIds(syncInternalId);
    for (String attributeName : new String[] {"objectClass", "description", "cn"}) {
      assertNotNull("expected '" + attributeName + "' in catalog after seeding",
          catalogBefore.get(attributeName));
    }

    // drive an incremental by adding a third subject. the daemon will run retrieveGroupByDn
    // against testGroup to compute the diff before writing — that read is what exercises the
    // by-DN native capture path.
    testGroup.addMember(SubjectTestHelper.SUBJ2, false);
    incrementalProvision();

    // catalog ids for objectClass, description, cn must be reused (no unique-key collision)
    Map<String, Long> catalogAfter = readGroupAttrCatalogIds(syncInternalId);
    for (String attributeName : new String[] {"objectClass", "description", "cn"}) {
      assertEquals("catalog id for '" + attributeName + "' should be reused after incremental",
          catalogBefore.get(attributeName), catalogAfter.get(attributeName));
    }

    // objectClass is still multi-valued (top + posixGroup) -> 2 value rows
    int objectClassValueCount = new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from grouper_prov_group_attr_value gpv "
            + "join grouper_prov_group_attr gpa on gpa.internal_id = gpv.prov_group_attr_internal_id "
            + "join grouper_prov_group pg on pg.internal_id = gpv.prov_group_internal_id "
            + "where pg.grouper_sync_internal_id = ? and gpa.attribute_name = 'objectClass'")
        .addBindVar(syncInternalId).select(int.class);
    assertEquals("expected 2 value rows for multi-valued objectClass after incremental",
        2, objectClassValueCount);

    // description was captured by retrieveGroupByDn from the pre-write LDAP state which had
    // both SUBJ0 and SUBJ1 (multi-valued). The newly-added SUBJ2 was not yet in LDAP at
    // read-time, so it does not appear in this cycle's reporting tables — it will surface
    // on the next read pass. This proves the by-DN read captured the multi-valued attr.
    int descriptionValueCount = new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from grouper_prov_group_attr_value gpv "
            + "join grouper_prov_group_attr gpa on gpa.internal_id = gpv.prov_group_attr_internal_id "
            + "join grouper_prov_group pg on pg.internal_id = gpv.prov_group_internal_id "
            + "where pg.grouper_sync_internal_id = ? and gpa.attribute_name = 'description'")
        .addBindVar(syncInternalId).select(int.class);
    assertEquals("expected 2 value rows captured for multi-valued description by retrieveGroupByDn",
        2, descriptionValueCount);

    // cn is single-valued -> still exactly 1 value row
    int cnValueCount = new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from grouper_prov_group_attr_value gpv "
            + "join grouper_prov_group_attr gpa on gpa.internal_id = gpv.prov_group_attr_internal_id "
            + "join grouper_prov_group pg on pg.internal_id = gpv.prov_group_internal_id "
            + "where pg.grouper_sync_internal_id = ? and gpa.attribute_name = 'cn'")
        .addBindVar(syncInternalId).select(int.class);
    assertEquals("expected 1 value row for single-valued cn after incremental", 1, cnValueCount);
  }

  /** read (attribute_name -> internal_id) for the group attr catalog of a single sync */
  private Map<String, Long> readGroupAttrCatalogIds(long syncInternalId) {
    java.util.List<Object[]> rows = new GcDbAccess().connectionName("grouper")
        .sql("select attribute_name, internal_id from grouper_prov_group_attr "
            + "where grouper_sync_internal_id = ?")
        .addBindVar(syncInternalId).selectList(Object[].class);
    Map<String, Long> result = new LinkedHashMap<String, Long>();
    for (Object[] row : rows) {
      if (row == null || row.length < 2 || row[0] == null || row[1] == null) continue;
      result.put(row[0].toString(), ((Number) row[1]).longValue());
    }
    return result;
  }

  /** read (attribute_name -> internal_id) for the user attr catalog of a single sync */
  private Map<String, Long> readUserAttrCatalogIds(long syncInternalId) {
    java.util.List<Object[]> rows = new GcDbAccess().connectionName("grouper")
        .sql("select attribute_name, internal_id from grouper_prov_user_attr "
            + "where grouper_sync_internal_id = ?")
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

  /** alias for {@link #countByProvisioner(String, String)} matching the cross-protocol naming. */
  private int countSyncBack(String configId, String tableName) {
    return countByProvisioner(configId, tableName);
  }

  /**
   * Three-axis sync-back smoke test for LDAP using {@code harvardGroupOfNames} (which
   * provisions both group-side and entity-side payloads). Mirrors the Azure
   * {@code testAzureFullSyncPopulatesGenericTables} structure: with all three
   * {@code load*ToGenericGrouperTable=true} flags on and the default
   * {@code selectAll*}=true, two {@code fullProvision()} passes should populate
   * grouper_prov_group / _user / _mship from the LDAP read path through the
   * {@code retrieveAllGroups} / {@code retrieveAllEntities} capture hooks.
   */
  public void testLdapFullSyncPopulatesGenericTables() {

    String configId = "ldapProvTest";

    LdapProvisionerTestUtils.configureLdapProvisioner(
        new LdapProvisionerTestConfigInput()
            .assignProvisioningStrategy("harvardGroupOfNames")
            .assignSubjectSourcesToProvision("jdbc")
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

    // harvardGroupOfNames has insertEntities=false, so the provisioner won't create
    // the user records on its own. Seed the LDAP user OU directly so the read-back
    // pass has something to capture as native users.
    seedLdapTestSubject("test.subject.0");
    seedLdapTestSubject("test.subject.1");

    // baseline: nothing in the generic tables yet
    assertEquals(0, countSyncBack(configId, "grouper_prov_group"));
    assertEquals(0, countSyncBack(configId, "grouper_prov_user"));
    assertEquals(0, countSyncBack(configId, "grouper_prov_mship"));

    // pass 1 writes the LDAP target; pass 2 reads it back and the capture hooks
    // populate the sync-back tables (read-state convergence contract).
    GrouperProvisioningOutput passOne = fullProvision();
    assertEquals(0, passOne.getRecordsWithErrors());

    GrouperProvisioningOutput passTwo = fullProvision();
    assertEquals(0, passTwo.getRecordsWithErrors());

    assertTrue("expected at least 1 prov_group row after sync-back",
        countSyncBack(configId, "grouper_prov_group") >= 1);
    assertTrue("expected at least 2 prov_user rows (SUBJ0 + SUBJ1)",
        countSyncBack(configId, "grouper_prov_user") >= 2);
    assertTrue("expected at least 2 prov_mship rows",
        countSyncBack(configId, "grouper_prov_mship") >= 2);
  }

  /**
   * Sync-back smoke test for the scoped-retrieve path: same as
   * {@link #testLdapFullSyncPopulatesGenericTables} but with
   * {@code selectAllGroups=false} and {@code selectAllEntities=false} so the DAO
   * goes through {@code retrieveGroups} / {@code retrieveEntities} (per-id lookups)
   * instead of {@code retrieveAllGroups} / {@code retrieveAllEntities}. Confirms the
   * capture hooks on the scoped retrieve methods fire under LDAP.
   */
  public void testLdapFullSyncSelectByIdsPopulatesGenericTables() {

    String configId = "ldapProvTest";

    LdapProvisionerTestUtils.configureLdapProvisioner(
        new LdapProvisionerTestConfigInput()
            .assignProvisioningStrategy("harvardGroupOfNames")
            .assignSubjectSourcesToProvision("jdbc")
            .addExtraConfig("selectAllGroups", "false")
            .addExtraConfig("selectAllEntities", "false")
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

    // harvardGroupOfNames has insertEntities=false, so we must pre-seed the user
    // entries in ou=People for the scoped retrieveEntities pass to find anything
    // to capture.
    seedLdapTestSubject("test.subject.0");
    seedLdapTestSubject("test.subject.1");

    assertEquals(0, countSyncBack(configId, "grouper_prov_group"));
    assertEquals(0, countSyncBack(configId, "grouper_prov_user"));
    assertEquals(0, countSyncBack(configId, "grouper_prov_mship"));

    // pass 1 inserts into LDAP; pass 2 reads it back via the scoped retrieve paths
    // (retrieveGroups + retrieveEntities) so capture hooks on those methods fire.
    GrouperProvisioningOutput passOne = fullProvision();
    assertEquals(0, passOne.getRecordsWithErrors());

    GrouperProvisioningOutput passTwo = fullProvision();
    assertEquals(0, passTwo.getRecordsWithErrors());

    assertTrue("expected at least 1 prov_group row via scoped retrieve",
        countSyncBack(configId, "grouper_prov_group") >= 1);
    assertTrue("expected at least 2 prov_user rows via scoped retrieve",
        countSyncBack(configId, "grouper_prov_user") >= 2);
    assertTrue("expected at least 2 prov_mship rows via scoped retrieve",
        countSyncBack(configId, "grouper_prov_mship") >= 2);
  }

  /**
   * Create a minimal inetOrgPerson entry under ou=People for the given uid so the
   * sync-back capture has a real LdapEntry to read on the pass-2 retrieveEntities.
   * Used by the harvardGroupOfNames sync-back tests, where insertEntities=false
   * means the provisioner does not create user records itself.
   */
  private static void seedLdapTestSubject(String uid) {
    LdapEntry ldapEntry = new LdapEntry("uid=" + uid + ",ou=People,dc=example,dc=edu");
    LdapAttribute objectClass = new LdapAttribute("objectClass");
    objectClass.addStringValue("top");
    objectClass.addStringValue("person");
    objectClass.addStringValue("organizationalPerson");
    objectClass.addStringValue("inetOrgPerson");
    ldapEntry.addAttribute(objectClass);
    ldapEntry.addAttribute(new LdapAttribute("uid", uid));
    ldapEntry.addAttribute(new LdapAttribute("cn", uid));
    ldapEntry.addAttribute(new LdapAttribute("sn", uid));
    LdapSessionUtils.ldapSession().create("personLdap", ldapEntry);
  }

}
