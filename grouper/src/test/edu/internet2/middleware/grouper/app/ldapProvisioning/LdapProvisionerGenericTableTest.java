package edu.internet2.middleware.grouper.app.ldapProvisioning;

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
    assertTrue("expected at least 1 group attr catalog row, got " + groupAttrCatalogCount,
        groupAttrCatalogCount >= 1);

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

    int userRowCount = countByProvisioner(configId, "grouper_prov_user");
    assertTrue("expected at least 1 prov_user row from native LDAP fetch, got " + userRowCount,
        userRowCount >= 1);

    // SUBJ0 and SUBJ1 should both have prov_user rows (LDAP DN uses uid=test.subject.N)
    int subj0Rows = new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from grouper_prov_user "
            + "where grouper_sync_internal_id = ? and target_user_id like 'uid=test.subject.0,%'")
        .addBindVar(syncInternalId).select(int.class);
    assertEquals("expected exactly 1 prov_user row for SUBJ0", 1, subj0Rows);

    int subj1Rows = new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from grouper_prov_user "
            + "where grouper_sync_internal_id = ? and target_user_id like 'uid=test.subject.1,%'")
        .addBindVar(syncInternalId).select(int.class);
    assertEquals("expected exactly 1 prov_user row for SUBJ1", 1, subj1Rows);

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

    int subj0ValueRows = new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from grouper_prov_user_attr_value puv "
            + "join grouper_prov_user pu on puv.prov_user_internal_id = pu.internal_id "
            + "where pu.grouper_sync_internal_id = ? and pu.target_user_id like 'uid=test.subject.0,%'")
        .addBindVar(syncInternalId).select(int.class);
    assertTrue("expected >=1 attr value row for SUBJ0, got " + subj0ValueRows, subj0ValueRows >= 1);
  }

  private int countByProvisioner(String configId, String tableName) {
    return new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from " + tableName
            + " where grouper_sync_internal_id in (select internal_id from grouper_sync where provisioner_name = ?)")
        .addBindVar(configId).select(int.class);
  }

}
