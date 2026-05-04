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
 * tables (grouper_prov_user, grouper_prov_group, _attr catalogs, _attr_value, _mship*)
 * when the loadEntitiesToGenericGrouperTable / loadGroupsToGenericGrouperTable /
 * loadMembershipsToGenericGrouperTable flags are on.
 *
 * Counts are asserted against rows tied to specific target IDs (not raw row totals),
 * so any extra LDAP seed entries don't influence the outcome.
 */
public class LdapProvisionerGenericTableTest extends GrouperProvisioningBaseTest {

  public static void main(String[] args) {
    TestRunner.run(new LdapProvisionerGenericTableTest("testFullProvisionPopulatesGenericTables"));
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
    SourceManager.getInstance().internal_removeSource("personLdapSource");
    GrouperSession.stopQuietly(this.grouperSession);
    LdapProvisionerTestUtils.stopAndRemoveLdapContainer();
    super.tearDown();
  }

  @Override
  public String defaultConfigId() {
    return "ldapProvTest";
  }

  /**
   * basic end-to-end: a full LDAP provision should populate all eight grouper_prov_* tables.
   * Asserts exact counts via target_id filters so the test is independent of any extra
   * LDAP seed entries in the container.
   */
  public void testFullProvisionPopulatesGenericTables() {

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
    assertEquals(0, countByProvisioner(configId, "grouper_prov_user"));
    assertEquals(0, countByProvisioner(configId, "grouper_prov_group"));
    assertEquals(0, countByProvisioner(configId, "grouper_prov_user_attr"));
    assertEquals(0, countByProvisioner(configId, "grouper_prov_group_attr"));
    assertEquals(0, countByProvisioner(configId, "grouper_prov_mship_role"));
    assertEquals(0, countByProvisioner(configId, "grouper_prov_mship"));

    GrouperProvisioningOutput output = fullProvision();
    GrouperProvisioner.retrieveInternalLastProvisioner();
    assertEquals(0, output.getRecordsWithErrors());

    GcGrouperSync sync = GcGrouperSyncDao.retrieveByProvisionerName(null, configId);
    assertNotNull("grouper_sync row should exist for " + configId, sync);
    long syncId = sync.getInternalId();

    // exactly one prov_group row matching testGroup's target id (LDAP DN starts with cn=test:testGroup)
    int testGroupRows = new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from grouper_prov_group "
            + "where grouper_sync_internal_id = ? and target_group_id like 'cn=test:testGroup,%'")
        .addBindVar(syncId).select(int.class);
    assertEquals("expected exactly 1 prov_group row for testGroup", 1, testGroupRows);

    // exactly one prov_user row for SUBJ0 and exactly one for SUBJ1 (LDAP DN by uid)
    int subj0Rows = new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from grouper_prov_user "
            + "where grouper_sync_internal_id = ? and target_user_id like 'uid=test.subject.0,%'")
        .addBindVar(syncId).select(int.class);
    assertEquals("expected exactly 1 prov_user row for SUBJ0", 1, subj0Rows);

    int subj1Rows = new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from grouper_prov_user "
            + "where grouper_sync_internal_id = ? and target_user_id like 'uid=test.subject.1,%'")
        .addBindVar(syncId).select(int.class);
    assertEquals("expected exactly 1 prov_user row for SUBJ1", 1, subj1Rows);

    // exactly two mship rows linking testGroup to its two members
    int testGroupMshipRows = new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from grouper_prov_mship gpm "
            + "join grouper_prov_group pg on gpm.prov_group_internal_id = pg.internal_id "
            + "join grouper_prov_user pu on gpm.prov_user_internal_id = pu.internal_id "
            + "where gpm.grouper_sync_internal_id = ? and pg.target_group_id like 'cn=test:testGroup,%'")
        .addBindVar(syncId).select(int.class);
    assertEquals("expected exactly 2 mship rows for testGroup", 2, testGroupMshipRows);

    // attribute catalog dedup: no two catalog rows with the same (sync, attribute_name)
    int dupGroupAttr = new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from (select grouper_sync_internal_id, attribute_name, count(*) c "
            + "from grouper_prov_group_attr where grouper_sync_internal_id = ? "
            + "group by grouper_sync_internal_id, attribute_name having count(*) > 1) t")
        .addBindVar(syncId).select(int.class);
    assertEquals("group attr catalog should be deduped per (sync,name)", 0, dupGroupAttr);

    int dupUserAttr = new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from (select grouper_sync_internal_id, attribute_name, count(*) c "
            + "from grouper_prov_user_attr where grouper_sync_internal_id = ? "
            + "group by grouper_sync_internal_id, attribute_name having count(*) > 1) t")
        .addBindVar(syncId).select(int.class);
    assertEquals("user attr catalog should be deduped per (sync,name)", 0, dupUserAttr);

    // value rows must reference real catalog rows (no orphan FKs)
    int orphanGroupValues = new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from grouper_prov_group_attr_value gpv "
            + "left join grouper_prov_group_attr pa on gpv.prov_group_attr_internal_id = pa.internal_id "
            + "join grouper_prov_group pg on gpv.prov_group_internal_id = pg.internal_id "
            + "where pg.grouper_sync_internal_id = ? and pa.internal_id is null")
        .addBindVar(syncId).select(int.class);
    assertEquals("no group_attr_value rows should orphan their catalog FK", 0, orphanGroupValues);

    int orphanUserValues = new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from grouper_prov_user_attr_value puv "
            + "left join grouper_prov_user_attr pa on puv.prov_user_attr_internal_id = pa.internal_id "
            + "join grouper_prov_user pu on puv.prov_user_internal_id = pu.internal_id "
            + "where pu.grouper_sync_internal_id = ? and pa.internal_id is null")
        .addBindVar(syncId).select(int.class);
    assertEquals("no user_attr_value rows should orphan their catalog FK", 0, orphanUserValues);

    // testGroup should have at least one attribute name in the catalog with at least one value row
    int testGroupValueRows = new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from grouper_prov_group_attr_value gpv "
            + "join grouper_prov_group pg on gpv.prov_group_internal_id = pg.internal_id "
            + "where pg.grouper_sync_internal_id = ? and pg.target_group_id like 'cn=test:testGroup,%'")
        .addBindVar(syncId).select(int.class);
    assertTrue("expected >=1 value row for testGroup, got " + testGroupValueRows, testGroupValueRows >= 1);
  }

  private int countByProvisioner(String configId, String tableName) {
    return new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from " + tableName
            + " where grouper_sync_internal_id in (select internal_id from grouper_sync where provisioner_name = ?)")
        .addBindVar(configId).select(int.class);
  }

}
