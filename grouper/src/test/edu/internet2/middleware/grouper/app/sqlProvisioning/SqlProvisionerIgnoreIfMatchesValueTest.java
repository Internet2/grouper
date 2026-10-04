package edu.internet2.middleware.grouper.app.sqlProvisioning;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioner;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningAttributeValue;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningBaseTest;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningFullSyncJob;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningOutput;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningService;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningConsumer;
import edu.internet2.middleware.grouper.cfg.dbConfig.GrouperDbConfig;
import edu.internet2.middleware.grouper.changeLog.esb.consumer.EsbConsumer;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.grouperClient.config.ConfigPropertiesCascadeBase;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import junit.textui.TestRunner;

/**
 * GRP-7436: provisioner ignoreIfMatchesValue on group, entity and membership attributes, for each
 * membership style (membershipObjects, groupAttributes, entityAttributes), full and incremental.
 *
 * <p>An ignored object (Grouper side or target side, checked after matching) must never be inserted,
 * updated or deleted, and neither may the memberships of an ignored group or entity.  Each test seeds
 * target rows that a normal sync would delete, and checks which ones are deleted and which are kept.</p>
 *
 * <p>Runs against the database only (the SQL provisioner test tables), no Tomcat.</p>
 */
public class SqlProvisionerIgnoreIfMatchesValueTest extends GrouperProvisioningBaseTest {

  public static void main(String[] args) {
    TestRunner.run(new SqlProvisionerIgnoreIfMatchesValueTest("testMembershipObjectsIgnoreGroupAndEntityFull"));
  }

  public SqlProvisionerIgnoreIfMatchesValueTest(String name) {
    super(name);
  }

  @Override
  public String defaultConfigId() {
    return "sqlProvTest";
  }

  private GrouperSession grouperSession = null;

  @Override
  protected void setUp() {
    super.setUp();
    this.grouperSession = GrouperSession.startRootSession();

    // the SQL provisioner test tables
    new SqlProvisionerTest().ensureTableSyncTables();
    for (String table : new String[] {"testgrouper_prov_mship0", "testgrouper_prov_mship2", "testgrouper_prov_group",
        "testgrouper_prov_entity", "testgrouper_pro_ldap_group_attr", "testgrouper_prov_ldap_group",
        "testgrouper_pro_dap_entity_attr", "testgrouper_prov_ldap_entity"}) {
      new GcDbAccess().sql("delete from " + table).executeSql();
    }
  }

  @Override
  protected void tearDown() {
    GrouperSession.stopQuietly(this.grouperSession);
    super.tearDown();
  }

  // =============================================
  // helpers
  // =============================================

  /**
   * mark the "test" folder provisionable to sqlProvTest
   */
  private Stem provisionTestFolder() {
    Stem stem = new StemSave(this.grouperSession).assignName("test").save();
    GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision("sqlProvTest");
    attributeValue.setTargetName("sqlProvTest");
    attributeValue.setStemScopeString("sub");
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);
    return stem;
  }

  /**
   * @return rows of a two column query as MultiKeys
   */
  private static Set<MultiKey> rows(String sql) {
    Set<MultiKey> result = new HashSet<MultiKey>();
    for (Object[] row : new GcDbAccess().sql(sql).selectList(Object[].class)) {
      result.add(new MultiKey(row));
    }
    return result;
  }

  /**
   * @param debugKey e.g. ignoredMemberships
   * @return the count the last provisioner run put in its debug map
   */
  private static int lastDebugCount(String debugKey) {
    Object value = GrouperProvisioner.retrieveInternalLastProvisioner().getDebugMap().get(debugKey);
    return value == null ? 0 : GrouperUtil.intValue(value);
  }

  /**
   * store one provisioner property
   */
  private static void storeProvisionerConfig(String suffix, String value) {
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.sqlProvTest." + suffix).value(value).store();
  }

  /**
   * full sync and incremental job entries for sqlProvTest (for a config not built by SqlProvisionerTestUtils)
   */
  private static void storeJobConfig() {
    for (String[] keyValue : new String[][] {
        {"otherJob.provisioner_full_sqlProvTest.class", GrouperProvisioningFullSyncJob.class.getName()},
        {"otherJob.provisioner_full_sqlProvTest.quartzCron", "9 59 23 31 12 ? 2099"},
        {"otherJob.provisioner_full_sqlProvTest.provisionerConfigId", "sqlProvTest"},
        {"changeLog.consumer.provisioner_incremental_sqlProvTest.class", EsbConsumer.class.getName()},
        {"changeLog.consumer.provisioner_incremental_sqlProvTest.quartzCron", "9 59 23 31 12 ? 2099"},
        {"changeLog.consumer.provisioner_incremental_sqlProvTest.provisionerConfigId", "sqlProvTest"},
        {"changeLog.consumer.provisioner_incremental_sqlProvTest.publisher.class", ProvisioningConsumer.class.getName()},
        {"changeLog.consumer.provisioner_incremental_sqlProvTest.publisher.debug", "true"}}) {
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName(keyValue[0]).value(keyValue[1]).store();
    }
  }

  // =============================================
  // membershipObjects: ignore on a membership attribute
  // =============================================

  /**
   * membershipObjects, memberships only (group_name, subject_id), ignore subject_id test.subject.1 and
   * test.subject.4
   * @param ignoreValue the configured ignoreIfMatchesValue
   * @param caseSensitiveCompare null to leave the default
   */
  private void configureMembershipOnly(String ignoreValue, String caseSensitiveCompare) {
    SqlProvisionerTestConfigInput input = new SqlProvisionerTestConfigInput()
        .assignMembershipDeleteType("deleteMembershipsIfNotExistInGrouper")
        .assignMembershipTableName("testgrouper_prov_mship0")
        .assignMembershipAttributeCount(2)
        .addExtraConfig("targetMembershipAttribute.1.showAttributeValueSettings", "true")
        .addExtraConfig("targetMembershipAttribute.1.ignoreIfMatchesValue", ignoreValue);
    if (caseSensitiveCompare != null) {
      input.addExtraConfig("targetMembershipAttribute.1.caseSensitiveCompare", caseSensitiveCompare);
    }
    SqlProvisionerTestUtils.configureSqlProvisioner(input);
  }

  /**
   * Target has (testGroup, s1) and (testGroup, s3) out of band; Grouper has s0 and s4.  s1 and s4 are
   * ignored.  Full sync: s0 inserted, s3 deleted, s1 kept (not deleted), s4 not inserted.
   */
  public void testMembershipObjectsIgnoreMembershipFull() {
    configureMembershipOnly("test.subject.1, test.subject.4", null);
    assertMembershipOnlyIgnored();
  }

  /**
   * Same as full, with an upper case ignore value and caseSensitiveCompare false.
   */
  public void testMembershipObjectsIgnoreMembershipCaseInsensitive() {
    configureMembershipOnly("TEST.SUBJECT.1, TEST.SUBJECT.4", "false");
    assertMembershipOnlyIgnored();
  }

  /**
   * With caseSensitiveCompare left at its default (true) an upper case ignore value does not match:
   * s1 is deleted and s4 is inserted like any other membership.
   */
  public void testMembershipObjectsIgnoreMembershipCaseSensitiveNoMatch() {
    configureMembershipOnly("TEST.SUBJECT.1, TEST.SUBJECT.4", null);
    provisionTestFolder();
    Group testGroup = new GroupSave(this.grouperSession).assignName("test:testGroup").save();
    testGroup.addMember(SubjectTestHelper.SUBJ0, false);
    testGroup.addMember(SubjectTestHelper.SUBJ4, false);
    new GcDbAccess().sql("insert into testgrouper_prov_mship0 (group_name, subject_id) values ('test:testGroup', 'test.subject.1')").executeSql();

    GrouperProvisioningOutput output = fullProvision();
    assertEquals(0, output.getRecordsWithErrors());

    Set<MultiKey> memberships = rows("select group_name, subject_id from testgrouper_prov_mship0");
    assertEquals(memberships.toString(), 2, memberships.size());
    assertTrue(memberships.contains(new MultiKey("test:testGroup", "test.subject.0")));
    assertTrue(memberships.contains(new MultiKey("test:testGroup", "test.subject.4")));
    assertEquals(0, lastDebugCount("ignoredMemberships"));
  }

  private void assertMembershipOnlyIgnored() {
    provisionTestFolder();
    Group testGroup = new GroupSave(this.grouperSession).assignName("test:testGroup").save();
    testGroup.addMember(SubjectTestHelper.SUBJ0, false);
    testGroup.addMember(SubjectTestHelper.SUBJ4, false);

    new GcDbAccess().sql("insert into testgrouper_prov_mship0 (group_name, subject_id) values ('test:testGroup', 'test.subject.1')").executeSql();
    new GcDbAccess().sql("insert into testgrouper_prov_mship0 (group_name, subject_id) values ('test:testGroup', 'test.subject.3')").executeSql();

    GrouperProvisioningOutput output = fullProvision();
    assertEquals(0, output.getRecordsWithErrors());

    Set<MultiKey> memberships = rows("select group_name, subject_id from testgrouper_prov_mship0");
    assertEquals(memberships.toString(), 2, memberships.size());
    assertTrue("inserted", memberships.contains(new MultiKey("test:testGroup", "test.subject.0")));
    assertTrue("ignored target-only membership kept", memberships.contains(new MultiKey("test:testGroup", "test.subject.1")));
    assertFalse("not ignored, not in Grouper: deleted", memberships.contains(new MultiKey("test:testGroup", "test.subject.3")));
    assertFalse("ignored Grouper membership not inserted", memberships.contains(new MultiKey("test:testGroup", "test.subject.4")));
    assertEquals(2, lastDebugCount("ignoredMemberships"));
  }

  /**
   * Incremental: after a full sync, adding an ignored member (s4) inserts nothing, adding s5 inserts it,
   * and removing s0 deletes it, while the out-of-band ignored (testGroup, s1) is never touched.
   */
  public void testMembershipObjectsIgnoreMembershipIncremental() {
    configureMembershipOnly("test.subject.1, test.subject.4", null);
    provisionTestFolder();
    Group testGroup = new GroupSave(this.grouperSession).assignName("test:testGroup").save();
    testGroup.addMember(SubjectTestHelper.SUBJ0, false);
    new GcDbAccess().sql("insert into testgrouper_prov_mship0 (group_name, subject_id) values ('test:testGroup', 'test.subject.1')").executeSql();

    fullProvision();
    incrementalProvision();

    testGroup.addMember(SubjectTestHelper.SUBJ4, false);
    testGroup.addMember(SubjectTestHelper.SUBJ5, false);
    testGroup.deleteMember(SubjectTestHelper.SUBJ0);
    incrementalProvision();

    Set<MultiKey> memberships = rows("select group_name, subject_id from testgrouper_prov_mship0");
    assertEquals(memberships.toString(), 2, memberships.size());
    assertTrue("ignored membership kept", memberships.contains(new MultiKey("test:testGroup", "test.subject.1")));
    assertTrue("normal add inserted", memberships.contains(new MultiKey("test:testGroup", "test.subject.5")));
    assertFalse("ignored add not inserted", memberships.contains(new MultiKey("test:testGroup", "test.subject.4")));
    assertFalse("normal remove deleted", memberships.contains(new MultiKey("test:testGroup", "test.subject.0")));
  }

  // =============================================
  // membershipObjects: ignore on group and entity attributes
  // =============================================

  /**
   * groups (testgrouper_prov_group), entities (testgrouper_prov_entity) and memberships
   * (testgrouper_prov_mship2), deleting groups / entities / memberships not in Grouper.  Ignore entities
   * whose subject_id_or_identifier is test.subject.1 and groups whose description is doNotProvision.
   */
  private void configureGroupEntityMembership() {
    SqlProvisionerTestUtils.configureSqlProvisioner(new SqlProvisionerTestConfigInput()
        .assignEntityDeleteType("deleteEntitiesIfNotExistInGrouper")
        .assignGroupDeleteType("deleteGroupsIfNotExistInGrouper")
        .assignMembershipDeleteType("deleteMembershipsIfNotExistInGrouper")
        .assignGroupTableName("testgrouper_prov_group")
        .assignGroupTableIdColumn("uuid")
        .assignEntityTableName("testgrouper_prov_entity")
        .assignEntityTableIdColumn("uuid")
        .assignMembershipTableName("testgrouper_prov_mship2")
        .assignMembershipTableIdColumn("uuid")
        .assignMembershipGroupForeignKeyColumn("group_uuid")
        .assignMembershipEntityForeignKeyColumn("entity_uuid")
        .assignHasTargetEntityLink(true)
        .assignEntityAttributeCount(5)
        .assignGroupAttributeCount(4)
        .assignMembershipAttributeCount(3)
        .addExtraConfig("targetEntityAttribute.2.showAttributeValueSettings", "true")
        .addExtraConfig("targetEntityAttribute.2.ignoreIfMatchesValue", "test.subject.1")
        .addExtraConfig("targetGroupAttribute.1.showAttributeValueSettings", "true")
        .addExtraConfig("targetGroupAttribute.1.ignoreIfMatchesValue", "doNotProvision"));
  }

  /**
   * <ol>
   *   <li>Grouper: testGroup (s0, s1), ignoredGroup (description doNotProvision, s0).  Full sync inserts
   *       testGroup, entity s0 and (testGroup, s0) only: s1 is an ignored entity and ignoredGroup an
   *       ignored group, so neither they nor their memberships are inserted.</li>
   *   <li>Out of band, the target gets an ignored entity (test.subject.1) in testGroup, an ignored group
   *       with s0 in it, and a stray entity in testGroup.  Full sync deletes the stray entity and its
   *       membership, and keeps the ignored entity, the ignored group and both of their memberships.</li>
   *   <li>The target copy of testGroup gets description doNotProvision (only the target side matches).
   *       The matched pair is now ignored: its description is not put back, and removing s0 from
   *       testGroup in Grouper does not delete (testGroup, s0).</li>
   * </ol>
   */
  public void testMembershipObjectsIgnoreGroupAndEntityFull() {
    configureGroupEntityMembership();
    provisionTestFolder();

    Group testGroup = new GroupSave(this.grouperSession).assignName("test:testGroup").assignDescription("normal").save();
    testGroup.addMember(SubjectTestHelper.SUBJ0, false);
    testGroup.addMember(SubjectTestHelper.SUBJ1, false);
    Group ignoredGroup = new GroupSave(this.grouperSession).assignName("test:ignoredGroup").assignDescription("doNotProvision").save();
    ignoredGroup.addMember(SubjectTestHelper.SUBJ0, false);

    // 1. Grouper-side ignores
    GrouperProvisioningOutput output = fullProvision();
    assertEquals(0, output.getRecordsWithErrors());

    List<Object[]> groups = new GcDbAccess().sql("select uuid, name from testgrouper_prov_group").selectList(Object[].class);
    assertEquals("only testGroup inserted", 1, groups.size());
    assertEquals("test:testGroup", groups.get(0)[1]);
    String testGroupUuid = (String)groups.get(0)[0];

    List<Object[]> entities = new GcDbAccess().sql("select uuid, subject_id_or_identifier from testgrouper_prov_entity").selectList(Object[].class);
    assertEquals("only s0 inserted", 1, entities.size());
    assertEquals("test.subject.0", entities.get(0)[1]);
    String subj0Uuid = (String)entities.get(0)[0];

    Set<MultiKey> memberships = rows("select group_uuid, entity_uuid from testgrouper_prov_mship2");
    assertEquals(memberships.toString(), 1, memberships.size());
    assertTrue(memberships.contains(new MultiKey(testGroupUuid, subj0Uuid)));
    assertEquals(1, lastDebugCount("ignoredGroups"));
    assertEquals(1, lastDebugCount("ignoredEntities"));

    // 2. target-only ignored objects, and a stray that is not ignored
    new GcDbAccess().sql("insert into testgrouper_prov_entity (uuid, name, subject_id_or_identifier) values ('ignEnt', 'lab account', 'test.subject.1')").executeSql();
    new GcDbAccess().sql("insert into testgrouper_prov_entity (uuid, name, subject_id_or_identifier) values ('stray', 'stray account', 'test.subject.9')").executeSql();
    new GcDbAccess().sql("insert into testgrouper_prov_group (uuid, posix_id, name, description) values ('ignGrp', 999999, 'lab:group', 'doNotProvision')").executeSql();
    new GcDbAccess().sql("insert into testgrouper_prov_mship2 (uuid, group_uuid, entity_uuid) values ('m1', ?, 'ignEnt')").addBindVar(testGroupUuid).executeSql();
    new GcDbAccess().sql("insert into testgrouper_prov_mship2 (uuid, group_uuid, entity_uuid) values ('m2', 'ignGrp', ?)").addBindVar(subj0Uuid).executeSql();
    new GcDbAccess().sql("insert into testgrouper_prov_mship2 (uuid, group_uuid, entity_uuid) values ('m3', ?, 'stray')").addBindVar(testGroupUuid).executeSql();

    output = fullProvision();
    assertEquals(0, output.getRecordsWithErrors());

    Set<MultiKey> entityIds = rows("select uuid, subject_id_or_identifier from testgrouper_prov_entity");
    assertTrue("ignored target entity kept", entityIds.contains(new MultiKey("ignEnt", "test.subject.1")));
    assertFalse("stray entity deleted", entityIds.contains(new MultiKey("stray", "test.subject.9")));
    assertEquals(new Integer(1), new GcDbAccess().sql("select count(1) from testgrouper_prov_group where uuid = 'ignGrp'").select(int.class));

    memberships = rows("select group_uuid, entity_uuid from testgrouper_prov_mship2");
    assertTrue("normal membership kept", memberships.contains(new MultiKey(testGroupUuid, subj0Uuid)));
    assertTrue("membership of ignored entity kept", memberships.contains(new MultiKey(testGroupUuid, "ignEnt")));
    assertTrue("membership in ignored group kept", memberships.contains(new MultiKey("ignGrp", subj0Uuid)));
    assertFalse("membership of stray entity deleted", memberships.contains(new MultiKey(testGroupUuid, "stray")));
    assertEquals(memberships.toString(), 3, memberships.size());

    // 3. only the target side of a matched pair matches
    new GcDbAccess().sql("update testgrouper_prov_group set description = 'doNotProvision' where uuid = ?").addBindVar(testGroupUuid).executeSql();
    testGroup.deleteMember(SubjectTestHelper.SUBJ0);

    output = fullProvision();
    assertEquals(0, output.getRecordsWithErrors());

    assertEquals("ignored pair not updated", "doNotProvision", new GcDbAccess()
        .sql("select description from testgrouper_prov_group where uuid = ?").addBindVar(testGroupUuid).select(String.class));
    memberships = rows("select group_uuid, entity_uuid from testgrouper_prov_mship2");
    assertTrue("membership of the ignored group not deleted", memberships.contains(new MultiKey(testGroupUuid, subj0Uuid)));
  }

  /**
   * Incremental: adding a member to an ignored group, and adding an ignored subject to a normal group,
   * insert nothing; a normal add still works.
   */
  public void testMembershipObjectsIgnoreGroupAndEntityIncremental() {
    configureGroupEntityMembership();
    provisionTestFolder();

    Group testGroup = new GroupSave(this.grouperSession).assignName("test:testGroup").assignDescription("normal").save();
    testGroup.addMember(SubjectTestHelper.SUBJ0, false);
    Group ignoredGroup = new GroupSave(this.grouperSession).assignName("test:ignoredGroup").assignDescription("doNotProvision").save();

    fullProvision();
    incrementalProvision();

    ignoredGroup.addMember(SubjectTestHelper.SUBJ2, false);
    testGroup.addMember(SubjectTestHelper.SUBJ1, false);
    testGroup.addMember(SubjectTestHelper.SUBJ3, false);
    incrementalProvision();

    assertEquals(new Integer(1), new GcDbAccess().sql("select count(1) from testgrouper_prov_group").select(int.class));
    Set<MultiKey> entities = rows("select subject_id_or_identifier, subject_id_or_identifier from testgrouper_prov_entity");
    assertFalse("ignored entity not inserted", entities.contains(new MultiKey("test.subject.1", "test.subject.1")));
    assertTrue("normal entity inserted", entities.contains(new MultiKey("test.subject.3", "test.subject.3")));
    assertEquals("s0 and s3 in testGroup only", new Integer(2),
        new GcDbAccess().sql("select count(1) from testgrouper_prov_mship2").select(int.class));
  }

  // =============================================
  // groupAttributes: ignore on an entity attribute
  // =============================================

  /**
   * groupAttributes: memberships are the subjectId values of a group (testgrouper_pro_ldap_group_attr).
   * s1 is an ignored entity (targetEntityAttribute.0 subjectId).  Full sync adds s0 but not s1.  Then,
   * out of band, the target group gets subjectId values s1 and s3: the next full sync removes s3 (not in
   * Grouper) and keeps s1 (ignored).
   */
  public void testGroupAttributesIgnoreEntityFull() {
    SqlProvisionerTestUtils.configureSqlProvisioner(new SqlProvisionerTestConfigInput()
        .assignGroupDeleteType("deleteGroupsIfNotExistInGrouper")
        .assignMembershipDeleteType("deleteMembershipsIfNotExistInGrouper")
        .assignGroupAttributesTable(true)
        .assignGroupTableName("testgrouper_prov_ldap_group")
        .assignGroupTableIdColumn("uuid")
        .assignHasTargetGroupLink(true)
        .assignGroupAttributeCount(4)
        .assignPosixId(false)
        .assignProvisioningType("groupAttributes")
        .addExtraConfig("targetEntityAttribute.0.showAttributeValueSettings", "true")
        .addExtraConfig("targetEntityAttribute.0.ignoreIfMatchesValue", "test.subject.1"));

    provisionTestFolder();
    Group testGroup = new GroupSave(this.grouperSession).assignName("test:testGroup").save();
    testGroup.addMember(SubjectTestHelper.SUBJ0, false);
    testGroup.addMember(SubjectTestHelper.SUBJ1, false);

    GrouperProvisioningOutput output = fullProvision();
    assertEquals(0, output.getRecordsWithErrors());

    String uuid = new GcDbAccess().sql("select uuid from testgrouper_prov_ldap_group").select(String.class);
    Set<MultiKey> attributes = rows("select attribute_name, attribute_value from testgrouper_pro_ldap_group_attr");
    assertTrue(attributes.contains(new MultiKey("subjectId", "test.subject.0")));
    assertFalse("ignored entity not added", attributes.contains(new MultiKey("subjectId", "test.subject.1")));

    new GcDbAccess().sql("insert into testgrouper_pro_ldap_group_attr (group_uuid, attribute_name, attribute_value) values (?, 'subjectId', 'test.subject.1')").addBindVar(uuid).executeSql();
    new GcDbAccess().sql("insert into testgrouper_pro_ldap_group_attr (group_uuid, attribute_name, attribute_value) values (?, 'subjectId', 'test.subject.3')").addBindVar(uuid).executeSql();

    output = fullProvision();
    assertEquals(0, output.getRecordsWithErrors());

    attributes = rows("select attribute_name, attribute_value from testgrouper_pro_ldap_group_attr");
    assertTrue(attributes.contains(new MultiKey("subjectId", "test.subject.0")));
    assertTrue("ignored entity's value kept", attributes.contains(new MultiKey("subjectId", "test.subject.1")));
    assertFalse("value not in Grouper removed", attributes.contains(new MultiKey("subjectId", "test.subject.3")));
    assertTrue(lastDebugCount("ignoredEntities") >= 1);
  }

  /**
   * groupAttributes, ignore on the group's membership attribute itself (targetGroupAttribute.3 subjectId):
   * only those values (memberships) are ignored, never the group.  lab.account is not a subject in
   * Grouper at all, so this is the only way to keep it.  Full sync: the group is provisioned with s0, the
   * out-of-band lab.account value is kept, a stray s3 value is removed, and a Grouper member whose value
   * is ignored (s1) is not added.  Then an incremental removal of s1 does not remove anything either.
   */
  public void testGroupAttributesIgnoreMembershipValueFull() {
    SqlProvisionerTestUtils.configureSqlProvisioner(new SqlProvisionerTestConfigInput()
        .assignGroupDeleteType("deleteGroupsIfNotExistInGrouper")
        .assignMembershipDeleteType("deleteMembershipsIfNotExistInGrouper")
        .assignGroupAttributesTable(true)
        .assignGroupTableName("testgrouper_prov_ldap_group")
        .assignGroupTableIdColumn("uuid")
        .assignHasTargetGroupLink(true)
        .assignGroupAttributeCount(4)
        .assignPosixId(false)
        .assignProvisioningType("groupAttributes")
        .addExtraConfig("targetGroupAttribute.3.showAttributeValueSettings", "true")
        .addExtraConfig("targetGroupAttribute.3.ignoreIfMatchesValue", "lab.account, test.subject.1"));

    provisionTestFolder();
    Group testGroup = new GroupSave(this.grouperSession).assignName("test:testGroup").save();
    testGroup.addMember(SubjectTestHelper.SUBJ0, false);

    GrouperProvisioningOutput output = fullProvision();
    assertEquals(0, output.getRecordsWithErrors());
    assertEquals("group is not ignored", 0, lastDebugCount("ignoredGroups"));

    String uuid = new GcDbAccess().sql("select uuid from testgrouper_prov_ldap_group").select(String.class);
    assertNotNull("group provisioned", uuid);

    new GcDbAccess().sql("insert into testgrouper_pro_ldap_group_attr (group_uuid, attribute_name, attribute_value) values (?, 'subjectId', 'lab.account')").addBindVar(uuid).executeSql();
    new GcDbAccess().sql("insert into testgrouper_pro_ldap_group_attr (group_uuid, attribute_name, attribute_value) values (?, 'subjectId', 'test.subject.3')").addBindVar(uuid).executeSql();
    testGroup.addMember(SubjectTestHelper.SUBJ1, false);

    output = fullProvision();
    assertEquals(0, output.getRecordsWithErrors());

    Set<MultiKey> attributes = rows("select attribute_name, attribute_value from testgrouper_pro_ldap_group_attr where attribute_name = 'subjectId'");
    assertTrue(attributes.contains(new MultiKey("subjectId", "test.subject.0")));
    assertTrue("ignored value of a non-Grouper account kept", attributes.contains(new MultiKey("subjectId", "lab.account")));
    assertFalse("value not in Grouper removed", attributes.contains(new MultiKey("subjectId", "test.subject.3")));
    assertFalse("ignored Grouper member not added", attributes.contains(new MultiKey("subjectId", "test.subject.1")));
    assertEquals(attributes.toString(), 2, attributes.size());

    // incremental: s1 is ignored, so neither adding it out of band nor removing it in Grouper changes it
    new GcDbAccess().sql("insert into testgrouper_pro_ldap_group_attr (group_uuid, attribute_name, attribute_value) values (?, 'subjectId', 'test.subject.1')").addBindVar(uuid).executeSql();
    incrementalProvision();
    testGroup.deleteMember(SubjectTestHelper.SUBJ1);
    incrementalProvision();

    attributes = rows("select attribute_name, attribute_value from testgrouper_pro_ldap_group_attr where attribute_name = 'subjectId'");
    assertTrue("ignored value not removed by incremental", attributes.contains(new MultiKey("subjectId", "test.subject.1")));
    assertTrue(attributes.contains(new MultiKey("subjectId", "lab.account")));
  }

  // =============================================
  // entityAttributes: ignore on a group attribute
  // =============================================

  /**
   * entityAttributes over SQL: entities in testgrouper_prov_ldap_entity, their memberOf values (group
   * names) in testgrouper_pro_dap_entity_attr.  Groups are not stored, only used for the values.
   */
  private void configureEntityAttributes() {
    String[][] config = {
        {"class", SqlProvisioner.class.getName()},
        {"dbExternalSystemConfigId", "grouper"},
        {"debugLog", "true"},
        {"logAllObjectsVerbose", "true"},
        {"showAdvanced", "true"},
        {"provisioningType", "entityAttributes"},
        {"subjectSourcesToProvision", "jdbc"},
        {"operateOnGrouperEntities", "true"},
        {"operateOnGrouperMemberships", "true"},
        {"operateOnGrouperGroups", "true"},

        {"userTableName", "testgrouper_prov_ldap_entity"},
        {"userPrimaryKey", "entity_uuid"},
        {"useSeparateTableForEntityAttributes", "true"},
        {"entityAttributesAttributeNameColumn", "entity_attribute_name"},
        {"entityAttributesAttributeValueColumn", "entity_attribute_value"},
        {"entityAttributesEntityForeignKeyColumn", "entity_uuid"},
        {"entityAttributesTableName", "testgrouper_pro_dap_entity_attr"},
        {"entity2advanced", "true"},

        {"customizeEntityCrud", "true"},
        {"makeChangesToEntities", "true"},
        {"selectAllEntities", "true"},
        {"insertEntities", "true"},
        {"updateEntities", "true"},
        {"deleteEntities", "false"},
        {"numberOfEntityAttributes", "2"},
        {"targetEntityAttribute.0.name", "entity_uuid"},
        {"targetEntityAttribute.0.storageType", "entityTableColumn"},
        {"targetEntityAttribute.0.translateExpressionType", "grouperProvisioningEntityField"},
        {"targetEntityAttribute.0.translateFromGrouperProvisioningEntityField", "subjectId"},
        {"targetEntityAttribute.1.name", "memberOf"},
        {"targetEntityAttribute.1.storageType", "separateAttributesTable"},
        {"targetEntityAttribute.1.multiValued", "true"},
        {"entityMatchingAttributeCount", "1"},
        {"entityMatchingAttribute0name", "entity_uuid"},
        {"entityMembershipAttributeName", "memberOf"},
        {"entityMembershipAttributeValue", "groupAttributeValueCache0"},

        {"customizeMembershipCrud", "true"},
        {"deleteMembershipsIfNotExistInGrouper", "true"},

        {"customizeGroupCrud", "true"},
        {"selectGroups", "false"},
        {"insertGroups", "false"},
        {"updateGroups", "false"},
        {"deleteGroups", "false"},
        {"numberOfGroupAttributes", "1"},
        {"targetGroupAttribute.0.name", "groupName"},
        {"targetGroupAttribute.0.translateExpressionType", "grouperProvisioningGroupField"},
        {"targetGroupAttribute.0.translateFromGrouperProvisioningGroupField", "name"},
        {"targetGroupAttribute.0.showAttributeValueSettings", "true"},
        {"targetGroupAttribute.0.ignoreIfMatchesValue", "test:ignoredGroup"},
        {"groupAttributeValueCacheHas", "true"},
        {"groupAttributeValueCache0has", "true"},
        {"groupAttributeValueCache0source", "grouper"},
        {"groupAttributeValueCache0type", "groupAttribute"},
        {"groupAttributeValueCache0groupAttribute", "groupName"},
    };
    for (String[] keyValue : config) {
      storeProvisionerConfig(keyValue[0], keyValue[1]);
    }
    storeJobConfig();
    ConfigPropertiesCascadeBase.clearCache();
  }

  /**
   * entityAttributes: s0 is in testGroup and in ignoredGroup (an ignored group).  Full sync gives s0
   * memberOf testGroup only.  Then, out of band, s0 gets memberOf test:ignoredGroup and test:stale: the
   * next full sync removes test:stale (s0 is not in it in Grouper) and keeps test:ignoredGroup (ignored group).
   */
  public void testEntityAttributesIgnoreGroupFull() {
    configureEntityAttributes();
    provisionTestFolder();

    Group testGroup = new GroupSave(this.grouperSession).assignName("test:testGroup").save();
    testGroup.addMember(SubjectTestHelper.SUBJ0, false);
    // a provisionable group s0 is not in: its value is tracked (deleteMembershipsOnlyInTrackedGroups
    // defaults to true, so a value that is not a Grouper group would be left alone), so it must be removed
    Group staleGroup = new GroupSave(this.grouperSession).assignName("test:stale").save();
    staleGroup.addMember(SubjectTestHelper.SUBJ1, false);
    Group ignoredGroup = new GroupSave(this.grouperSession).assignName("test:ignoredGroup").save();
    ignoredGroup.addMember(SubjectTestHelper.SUBJ0, false);

    GrouperProvisioningOutput output = fullProvision();
    assertEquals(0, output.getRecordsWithErrors());

    Set<MultiKey> attributes = rows("select entity_attribute_name, entity_attribute_value from testgrouper_pro_dap_entity_attr where entity_uuid = 'test.subject.0'");
    assertTrue(attributes.toString(), attributes.contains(new MultiKey("memberOf", "test:testGroup")));
    assertFalse("ignored group not added", attributes.contains(new MultiKey("memberOf", "test:ignoredGroup")));
    assertEquals(1, lastDebugCount("ignoredGroups"));

    new GcDbAccess().sql("insert into testgrouper_pro_dap_entity_attr (entity_uuid, entity_attribute_name, entity_attribute_value) values ('test.subject.0', 'memberOf', 'test:ignoredGroup')").executeSql();
    new GcDbAccess().sql("insert into testgrouper_pro_dap_entity_attr (entity_uuid, entity_attribute_name, entity_attribute_value) values ('test.subject.0', 'memberOf', 'test:stale')").executeSql();

    output = fullProvision();
    assertEquals(0, output.getRecordsWithErrors());

    attributes = rows("select entity_attribute_name, entity_attribute_value from testgrouper_pro_dap_entity_attr where entity_uuid = 'test.subject.0'");
    assertTrue(attributes.contains(new MultiKey("memberOf", "test:testGroup")));
    assertTrue("ignored group's value kept", attributes.contains(new MultiKey("memberOf", "test:ignoredGroup")));
    assertFalse("s0 not in test:stale in Grouper: removed", attributes.contains(new MultiKey("memberOf", "test:stale")));
  }

  /**
   * entityAttributes, ignore on the entity's membership attribute itself (memberOf): the value
   * lab:externalGroup is not a Grouper group, but it is still ignored (kept), and the entity is not.
   */
  public void testEntityAttributesIgnoreMembershipValueFull() {
    configureEntityAttributes();
    // no group ignores here, only the memberOf value
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.sqlProvTest.targetGroupAttribute.0.ignoreIfMatchesValue").delete();
    storeProvisionerConfig("targetEntityAttribute.1.showAttributeValueSettings", "true");
    storeProvisionerConfig("targetEntityAttribute.1.ignoreIfMatchesValue", "lab:externalGroup");
    ConfigPropertiesCascadeBase.clearCache();
    provisionTestFolder();

    Group testGroup = new GroupSave(this.grouperSession).assignName("test:testGroup").save();
    testGroup.addMember(SubjectTestHelper.SUBJ0, false);
    // a provisionable group s0 is not in: its value is tracked (deleteMembershipsOnlyInTrackedGroups
    // defaults to true, so a value that is not a Grouper group would be left alone), so it must be removed
    Group staleGroup = new GroupSave(this.grouperSession).assignName("test:stale").save();
    staleGroup.addMember(SubjectTestHelper.SUBJ1, false);

    GrouperProvisioningOutput output = fullProvision();
    assertEquals(0, output.getRecordsWithErrors());
    assertEquals("entity is not ignored", 0, lastDebugCount("ignoredEntities"));

    new GcDbAccess().sql("insert into testgrouper_pro_dap_entity_attr (entity_uuid, entity_attribute_name, entity_attribute_value) values ('test.subject.0', 'memberOf', 'lab:externalGroup')").executeSql();
    new GcDbAccess().sql("insert into testgrouper_pro_dap_entity_attr (entity_uuid, entity_attribute_name, entity_attribute_value) values ('test.subject.0', 'memberOf', 'test:stale')").executeSql();

    output = fullProvision();
    assertEquals(0, output.getRecordsWithErrors());

    Set<MultiKey> attributes = rows("select entity_attribute_name, entity_attribute_value from testgrouper_pro_dap_entity_attr where entity_uuid = 'test.subject.0'");
    assertTrue(attributes.contains(new MultiKey("memberOf", "test:testGroup")));
    assertTrue("ignored value kept", attributes.contains(new MultiKey("memberOf", "lab:externalGroup")));
    assertFalse("s0 not in test:stale in Grouper: removed", attributes.contains(new MultiKey("memberOf", "test:stale")));
  }

  // =============================================
  // nothing configured
  // =============================================

  /**
   * With no ignoreIfMatchesValue the run is unchanged: out-of-band memberships are deleted and the
   * ignore counts are zero.
   */
  public void testNothingConfiguredUnchanged() {
    SqlProvisionerTestUtils.configureSqlProvisioner(new SqlProvisionerTestConfigInput()
        .assignMembershipDeleteType("deleteMembershipsIfNotExistInGrouper")
        .assignMembershipTableName("testgrouper_prov_mship0")
        .assignMembershipAttributeCount(2));
    provisionTestFolder();
    Group testGroup = new GroupSave(this.grouperSession).assignName("test:testGroup").save();
    testGroup.addMember(SubjectTestHelper.SUBJ0, false);
    new GcDbAccess().sql("insert into testgrouper_prov_mship0 (group_name, subject_id) values ('test:testGroup', 'test.subject.1')").executeSql();

    GrouperProvisioningOutput output = fullProvision();
    assertEquals(0, output.getRecordsWithErrors());

    Set<MultiKey> memberships = rows("select group_name, subject_id from testgrouper_prov_mship0");
    assertEquals(memberships.toString(), 1, memberships.size());
    assertTrue(memberships.contains(new MultiKey("test:testGroup", "test.subject.0")));
    assertEquals(0, lastDebugCount("ignoredGroups"));
    assertEquals(0, lastDebugCount("ignoredEntities"));
    assertEquals(0, lastDebugCount("ignoredMemberships"));
  }

}
