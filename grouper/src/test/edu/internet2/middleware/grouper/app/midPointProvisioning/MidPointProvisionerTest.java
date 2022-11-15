package edu.internet2.middleware.grouper.app.midPointProvisioning;

import java.math.BigDecimal;
import java.sql.Types;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import edu.internet2.middleware.grouper.ddl.DdlUtilsChangeDatabase;
import edu.internet2.middleware.grouper.ddl.DdlVersionBean;
import edu.internet2.middleware.grouper.ddl.GrouperDdlUtils;
import edu.internet2.middleware.grouper.ddl.GrouperMidpointDdl;
import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.model.Database;
import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.model.Table;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.misc.GrouperStartup;
import edu.internet2.middleware.grouper.misc.SaveMode;
import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.util.GrouperClientConfig;

public class MidPointProvisionerTest extends GrouperProvisioningBaseTest {

  @Override
  public String defaultConfigId() {
    return "midPointProvTest";
  }
  
  public static void main(String[] args) {

    GrouperStartup.startup();
    new MidPointProvisionerTest().ensureTableSyncTables();
    //TestRunner.run(new MidPointProvisionerTest("testFullMidPointProvisionerWithLastModifiedAndDeletedColumns"));
  
  }

  public MidPointProvisionerTest() {
    super();
  }
  
  public MidPointProvisionerTest(String name) {
    super(name);
  }

  /**
   * grouper session
   */
  private GrouperSession grouperSession = null;

  /**
   * @see edu.internet2.middleware.grouper.helper.GrouperTest#setUp()
   */
  @Override
  protected void setUp() {
    super.setUp();
    
    try {

      this.grouperSession = GrouperSession.startRootSession();
      
      ensureTableSyncTables();
  
      new GcDbAccess().sql("delete from gr_mp_groups").executeSql();
      new GcDbAccess().sql("delete from gr_mp_group_attributes").executeSql();
      new GcDbAccess().sql("delete from gr_mp_subjects").executeSql();
      new GcDbAccess().sql("delete from gr_mp_subject_attributes").executeSql();
      new GcDbAccess().sql("delete from gr_mp_memberships").executeSql();
      
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    
  }

  /**
   * @see edu.internet2.middleware.grouper.helper.GrouperTest#tearDown()
   */
  @Override
  protected void tearDown() {
    super.tearDown();
    
    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().clear();
    
// TODO   dropTableSyncTables();
    GrouperSession.stopQuietly(this.grouperSession);

  }
  
  /**
   * 
   */
  public void ensureTableSyncTables() {
    
    createTableGroup();
    
    createTableGroupAttr();
    
    createTableEntity();
    
    createTableEntityAttr();
    
    createTableMship0();
    
  }
  
  public void testFullMidPointProvisionerWithLastModifiedAndDeletedColumns() {
    
    MidPointProvisionerTestUtils.configureMidpointProvisioner(new MidPointProvisionerTestConfigInput()
        .addExtraConfig("midPointLastModifiedColumnType", "long")
        .addExtraConfig("midPointLastModifiedColumnName", "last_modified")
        .addExtraConfig("midPointDeletedColumnName", "deleted"));

    Stem stem = new StemSave(this.grouperSession).assignName("test").save();
    Stem stem2 = new StemSave(this.grouperSession).assignName("test2").save();
    
    // mark some folders to provision
    Group testGroup = new GroupSave(this.grouperSession).assignName("test:testGroup").save();
    Group testGroup2 = new GroupSave(this.grouperSession).assignName("test2:testGroup2").save();
    
    testGroup.addMember(SubjectTestHelper.SUBJ0, false);
    testGroup.addMember(SubjectTestHelper.SUBJ1, false);
    
    testGroup2.addMember(SubjectTestHelper.SUBJ2, false);
    testGroup2.addMember(SubjectTestHelper.SUBJ3, false);
    
    final GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision("midPointProvTest");
    attributeValue.setTargetName("midPointProvTest");
    attributeValue.setStemScopeString("sub");
    
    Map<String, Object> metadataNameValues = new HashMap<String, Object>();
    
    Set<String> targetValues = new HashSet<>();
    targetValues.add("a");
    targetValues.add("b");
    metadataNameValues.put("md_grouper_midPointTarget", targetValues);

    attributeValue.setMetadataNameValues(metadataNameValues);

    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);

    //AttributeAssign attributeAssign = stem.getAttributeDelegate().addAttribute(GrouperProvisioningAttributeNames.retrieveAttributeDefNameMarker()).getAttributeAssign();
    //attributeAssign.getAttributeValueDelegate().assignValueString(GrouperProvisioningAttributeNames.retrieveAttributeDefNameDoProvision())
    
    //lets sync these over
    GrouperProvisioningOutput grouperProvisioningOutput = fullProvision();
    GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
    
    List<Object[]> groups = new GcDbAccess().sql("select group_name, id_index, display_name, description, last_modified, deleted from gr_mp_groups").selectList(Object[].class);
    assertEquals(1, groups.size());
    
    assertEquals(testGroup.getName(), groups.get(0)[0]);
    assertEquals(testGroup.getIdIndex().longValue(), ((BigDecimal)groups.get(0)[1]).longValue());
    assertEquals(testGroup.getDisplayName(), groups.get(0)[2]);
    assertNull(groups.get(0)[3]);
    assertNotNull(groups.get(0)[4]);
    assertEquals("F", groups.get(0)[5]);
    
    
    List<Object[]> groupAttributes = new GcDbAccess().sql("select group_id_index, attribute_name, attribute_value, last_modified, deleted from gr_mp_group_attributes").selectList(Object[].class);
    assertEquals(2, groupAttributes.size());
    
    Map<MultiKey, Object[]> attributeNameValueToGroupAttributes = new HashMap<>();
    for (Object[] groupAttribute: groupAttributes) {
      attributeNameValueToGroupAttributes.put(new MultiKey(groupAttribute[1], groupAttribute[2]), groupAttribute);
    }
    
    assertNotNull(attributeNameValueToGroupAttributes.get(new MultiKey("target", "a")));
    assertNotNull(attributeNameValueToGroupAttributes.get(new MultiKey("target", "b")));
    assertNotNull(attributeNameValueToGroupAttributes.get(new MultiKey("target", "a"))[3]);
    assertNotNull(attributeNameValueToGroupAttributes.get(new MultiKey("target", "b"))[3]);
    assertEquals("F", attributeNameValueToGroupAttributes.get(new MultiKey("target", "a"))[4]);
    assertEquals("F", attributeNameValueToGroupAttributes.get(new MultiKey("target", "b"))[4]);
    
    List<Object[]> entities = new GcDbAccess().sql("select subject_id_index, subject_id, last_modified, deleted from gr_mp_subjects").selectList(Object[].class);
    assertEquals(2, entities.size());
    Map<String, Object[]> subjectIdToSubjectAttributes = new HashMap<>();
    for (Object[] entity: entities) {
      subjectIdToSubjectAttributes.put(entity[1].toString(), entity);
    }
    
    assertNotNull(subjectIdToSubjectAttributes.get(SubjectTestHelper.SUBJ0.getId()));
    assertNotNull(subjectIdToSubjectAttributes.get(SubjectTestHelper.SUBJ0.getId())[2]);
    assertEquals("F", subjectIdToSubjectAttributes.get(SubjectTestHelper.SUBJ0.getId())[3]);
    
    assertNotNull(subjectIdToSubjectAttributes.get(SubjectTestHelper.SUBJ1.getId()));
    assertNotNull(subjectIdToSubjectAttributes.get(SubjectTestHelper.SUBJ1.getId())[2]);
    assertEquals("F", subjectIdToSubjectAttributes.get(SubjectTestHelper.SUBJ1.getId())[3]);
    
    
    List<Object[]> memberships = new GcDbAccess().sql("select group_id_index, subject_id_index, last_modified, deleted from gr_mp_memberships").selectList(Object[].class);
    
    Map<MultiKey, Object[]> groupIdSubjectIdToMembershipAttributes = new HashMap<>();
    
    for (Object[] membershipAttributes: memberships) {
      groupIdSubjectIdToMembershipAttributes.put(new MultiKey(membershipAttributes[0], membershipAttributes[1]), membershipAttributes);
    }
    
    assertEquals(2, groupIdSubjectIdToMembershipAttributes.size());
    for (MultiKey groupIdSubjectId : groupIdSubjectIdToMembershipAttributes.keySet()) {
      Object[] membershipAttributes = groupIdSubjectIdToMembershipAttributes.get(groupIdSubjectId);
      assertNotNull(membershipAttributes[2]);
      assertEquals("F", membershipAttributes[3]);
    }
    
    
    // delete a member from testGroup and reprovision
    testGroup.deleteMember(SubjectTestHelper.SUBJ1);
    
    grouperProvisioningOutput = fullProvision();
    grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());

    memberships = new GcDbAccess().sql("select group_id_index, subject_id_index, last_modified, deleted from gr_mp_memberships").selectList(Object[].class);
    
    groupIdSubjectIdToMembershipAttributes = new HashMap<>();
    
    for (Object[] membershipAttributes: memberships) {
      groupIdSubjectIdToMembershipAttributes.put(new MultiKey(membershipAttributes[0], membershipAttributes[1]), membershipAttributes);
    }
    
    assertEquals(2, groupIdSubjectIdToMembershipAttributes.size());
    boolean falseFound = false;
    boolean trueFound = false;
    for (MultiKey groupIdSubjectId : groupIdSubjectIdToMembershipAttributes.keySet()) {
      Object[] membershipAttributes = groupIdSubjectIdToMembershipAttributes.get(groupIdSubjectId);
      assertNotNull(membershipAttributes[2]);
      if (membershipAttributes[3].equals("F")) {
        falseFound = true;
      }
      if (membershipAttributes[3].equals("T")) {
        trueFound = true;
      }
    }
    
    assertTrue(falseFound);
    assertTrue(trueFound);
    
    //update group description and reprovision
    testGroup = new GroupSave(this.grouperSession).assignName(testGroup.getName())
        .assignUuid(testGroup.getUuid()).assignDescription("newDescription")
        .assignSaveMode(SaveMode.UPDATE).save();
    
    grouperProvisioningOutput = fullProvision();
    grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
    
    groups = new GcDbAccess().sql("select group_name, id_index, display_name, description, last_modified, deleted from gr_mp_groups").selectList(Object[].class);
    assertEquals(1, groups.size());
    assertEquals(testGroup.getName(), groups.get(0)[0]);
    assertEquals(testGroup.getIdIndex().longValue(), ((BigDecimal)groups.get(0)[1]).longValue());
    assertEquals(testGroup.getDisplayName(), groups.get(0)[2]);
    assertEquals(testGroup.getDescription(), groups.get(0)[3]);
    assertNotNull(groups.get(0)[4]);
    assertEquals("F", groups.get(0)[5]);
    
    // now delete the group
    testGroup.delete();
    
    grouperProvisioningOutput = fullProvision();
    grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
    
    groups = new GcDbAccess().sql("select group_name, id_index, display_name, description, last_modified, deleted from gr_mp_groups").selectList(Object[].class);
    assertEquals(1, groups.size());
    assertEquals(testGroup.getName(), groups.get(0)[0]);
    assertEquals(testGroup.getIdIndex().longValue(), ((BigDecimal)groups.get(0)[1]).longValue());
    assertEquals(testGroup.getDisplayName(), groups.get(0)[2]);
    assertEquals(testGroup.getDescription(), groups.get(0)[3]);
    assertNotNull(groups.get(0)[4]);
    assertEquals("T", groups.get(0)[5]);
    
    groupAttributes = new GcDbAccess().sql("select group_id_index, attribute_name, attribute_value, last_modified, deleted from gr_mp_group_attributes").selectList(Object[].class);
    assertEquals(2, groupAttributes.size());
    
    attributeNameValueToGroupAttributes = new HashMap<>();
    for (Object[] groupAttribute: groupAttributes) {
      attributeNameValueToGroupAttributes.put(new MultiKey(groupAttribute[1], groupAttribute[2]), groupAttribute);
    }
    
    assertNotNull(attributeNameValueToGroupAttributes.get(new MultiKey("target", "a")));
    assertNotNull(attributeNameValueToGroupAttributes.get(new MultiKey("target", "b")));
    assertNotNull(attributeNameValueToGroupAttributes.get(new MultiKey("target", "a"))[3]);
    assertNotNull(attributeNameValueToGroupAttributes.get(new MultiKey("target", "b"))[3]);
    assertEquals("T", attributeNameValueToGroupAttributes.get(new MultiKey("target", "a"))[4]);
    assertEquals("T", attributeNameValueToGroupAttributes.get(new MultiKey("target", "b"))[4]);
    
    entities = new GcDbAccess().sql("select subject_id_index, subject_id, last_modified, deleted from gr_mp_subjects").selectList(Object[].class);
    assertEquals(2, entities.size());
    subjectIdToSubjectAttributes = new HashMap<>();
    for (Object[] entity: entities) {
      subjectIdToSubjectAttributes.put(entity[1].toString(), entity);
    }
    
    assertNotNull(subjectIdToSubjectAttributes.get(SubjectTestHelper.SUBJ0.getId()));
    assertNotNull(subjectIdToSubjectAttributes.get(SubjectTestHelper.SUBJ0.getId())[2]);
    assertEquals("T", subjectIdToSubjectAttributes.get(SubjectTestHelper.SUBJ0.getId())[3]);
    
    assertNotNull(subjectIdToSubjectAttributes.get(SubjectTestHelper.SUBJ1.getId()));
    assertNotNull(subjectIdToSubjectAttributes.get(SubjectTestHelper.SUBJ1.getId())[2]);
    assertEquals("T", subjectIdToSubjectAttributes.get(SubjectTestHelper.SUBJ1.getId())[3]);
    
    memberships = new GcDbAccess().sql("select group_id_index, subject_id_index, last_modified, deleted from gr_mp_memberships").selectList(Object[].class);
    
    groupIdSubjectIdToMembershipAttributes = new HashMap<>();
    
    for (Object[] membershipAttributes: memberships) {
      groupIdSubjectIdToMembershipAttributes.put(new MultiKey(membershipAttributes[0], membershipAttributes[1]), membershipAttributes);
    }
    
    assertEquals(2, groupIdSubjectIdToMembershipAttributes.size());
    for (MultiKey groupIdSubjectId : groupIdSubjectIdToMembershipAttributes.keySet()) {
      Object[] membershipAttributes = groupIdSubjectIdToMembershipAttributes.get(groupIdSubjectId);
      assertNotNull(membershipAttributes[2]);
      assertEquals("T", membershipAttributes[3]);
    }
    
  }
  
  
  
  public void testFullMidPointProvisionerWithLastModifiedColumnOnly() {
    
    MidPointProvisionerTestUtils.configureMidpointProvisioner(new MidPointProvisionerTestConfigInput()
        .addExtraConfig("midPointLastModifiedColumnType", "long")
        .addExtraConfig("midPointLastModifiedColumnName", "last_modified"));

    Stem stem = new StemSave(this.grouperSession).assignName("test").save();
    Stem stem2 = new StemSave(this.grouperSession).assignName("test2").save();
    
    // mark some folders to provision
    Group testGroup = new GroupSave(this.grouperSession).assignName("test:testGroup").save();
    Group testGroup2 = new GroupSave(this.grouperSession).assignName("test2:testGroup2").save();
    
    testGroup.addMember(SubjectTestHelper.SUBJ0, false);
    testGroup.addMember(SubjectTestHelper.SUBJ1, false);
    
    testGroup2.addMember(SubjectTestHelper.SUBJ2, false);
    testGroup2.addMember(SubjectTestHelper.SUBJ3, false);
    
    final GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision("midPointProvTest");
    attributeValue.setTargetName("midPointProvTest");
    attributeValue.setStemScopeString("sub");
    
    Map<String, Object> metadataNameValues = new HashMap<String, Object>();
    
    Set<String> targetValues = new HashSet<>();
    targetValues.add("a");
    targetValues.add("b");
    metadataNameValues.put("md_grouper_midPointTarget", targetValues);

    attributeValue.setMetadataNameValues(metadataNameValues);

    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);

    //AttributeAssign attributeAssign = stem.getAttributeDelegate().addAttribute(GrouperProvisioningAttributeNames.retrieveAttributeDefNameMarker()).getAttributeAssign();
    //attributeAssign.getAttributeValueDelegate().assignValueString(GrouperProvisioningAttributeNames.retrieveAttributeDefNameDoProvision())
    
    //lets sync these over
    GrouperProvisioningOutput grouperProvisioningOutput = fullProvision();
    GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
    
    List<Object[]> groups = new GcDbAccess().sql("select group_name, id_index, display_name, description, last_modified, deleted from gr_mp_groups").selectList(Object[].class);
    assertEquals(1, groups.size());
    
    assertEquals(testGroup.getName(), groups.get(0)[0]);
    assertEquals(testGroup.getIdIndex().longValue(), ((BigDecimal)groups.get(0)[1]).longValue());
    assertEquals(testGroup.getDisplayName(), groups.get(0)[2]);
    assertNull(groups.get(0)[3]);
    assertNotNull(groups.get(0)[4]);
    assertNull(groups.get(0)[5]);
    
    
    List<Object[]> groupAttributes = new GcDbAccess().sql("select group_id_index, attribute_name, attribute_value, last_modified, deleted from gr_mp_group_attributes").selectList(Object[].class);
    assertEquals(2, groupAttributes.size());
    
    Map<MultiKey, Object[]> attributeNameValueToGroupAttributes = new HashMap<>();
    for (Object[] groupAttribute: groupAttributes) {
      attributeNameValueToGroupAttributes.put(new MultiKey(groupAttribute[1], groupAttribute[2]), groupAttribute);
    }
    
    assertNotNull(attributeNameValueToGroupAttributes.get(new MultiKey("target", "a")));
    assertNotNull(attributeNameValueToGroupAttributes.get(new MultiKey("target", "b")));
    assertNotNull(attributeNameValueToGroupAttributes.get(new MultiKey("target", "a"))[3]);
    assertNotNull(attributeNameValueToGroupAttributes.get(new MultiKey("target", "b"))[3]);
    assertNull(attributeNameValueToGroupAttributes.get(new MultiKey("target", "a"))[4]);
    assertNull(attributeNameValueToGroupAttributes.get(new MultiKey("target", "b"))[4]);
    
    List<Object[]> entities = new GcDbAccess().sql("select subject_id_index, subject_id, last_modified, deleted from gr_mp_subjects").selectList(Object[].class);
    assertEquals(2, entities.size());
    Map<String, Object[]> subjectIdToSubjectAttributes = new HashMap<>();
    for (Object[] entity: entities) {
      subjectIdToSubjectAttributes.put(entity[1].toString(), entity);
    }
    
    assertNotNull(subjectIdToSubjectAttributes.get(SubjectTestHelper.SUBJ0.getId()));
    assertNotNull(subjectIdToSubjectAttributes.get(SubjectTestHelper.SUBJ0.getId())[2]);
    assertNull(subjectIdToSubjectAttributes.get(SubjectTestHelper.SUBJ0.getId())[3]);
    
    assertNotNull(subjectIdToSubjectAttributes.get(SubjectTestHelper.SUBJ1.getId()));
    assertNotNull(subjectIdToSubjectAttributes.get(SubjectTestHelper.SUBJ1.getId())[2]);
    assertNull(subjectIdToSubjectAttributes.get(SubjectTestHelper.SUBJ1.getId())[3]);
    
    
    List<Object[]> memberships = new GcDbAccess().sql("select group_id_index, subject_id_index, last_modified, deleted from gr_mp_memberships").selectList(Object[].class);
    
    Map<MultiKey, Object[]> groupIdSubjectIdToMembershipAttributes = new HashMap<>();
    
    for (Object[] membershipAttributes: memberships) {
      groupIdSubjectIdToMembershipAttributes.put(new MultiKey(membershipAttributes[0], membershipAttributes[1]), membershipAttributes);
    }
    
    assertEquals(2, groupIdSubjectIdToMembershipAttributes.size());
    for (MultiKey groupIdSubjectId : groupIdSubjectIdToMembershipAttributes.keySet()) {
      Object[] membershipAttributes = groupIdSubjectIdToMembershipAttributes.get(groupIdSubjectId);
      assertNotNull(membershipAttributes[2]);
      assertNull(membershipAttributes[3]);
    }
    
    
    // delete a member from testGroup and reprovision
    testGroup.deleteMember(SubjectTestHelper.SUBJ1);
    
    grouperProvisioningOutput = fullProvision();
    grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());

    memberships = new GcDbAccess().sql("select group_id_index, subject_id_index, last_modified, deleted from gr_mp_memberships").selectList(Object[].class);
    
    groupIdSubjectIdToMembershipAttributes = new HashMap<>();
    
    for (Object[] membershipAttributes: memberships) {
      groupIdSubjectIdToMembershipAttributes.put(new MultiKey(membershipAttributes[0], membershipAttributes[1]), membershipAttributes);
    }
    
    assertEquals(1, groupIdSubjectIdToMembershipAttributes.size());
    
    //update group description and reprovision
    testGroup = new GroupSave(this.grouperSession).assignName(testGroup.getName())
        .assignUuid(testGroup.getUuid()).assignDescription("newDescription")
        .assignSaveMode(SaveMode.UPDATE).save();
    
    grouperProvisioningOutput = fullProvision();
    grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
    
    groups = new GcDbAccess().sql("select group_name, id_index, display_name, description, last_modified, deleted from gr_mp_groups").selectList(Object[].class);
    assertEquals(1, groups.size());
    assertEquals(testGroup.getName(), groups.get(0)[0]);
    assertEquals(testGroup.getIdIndex().longValue(), ((BigDecimal)groups.get(0)[1]).longValue());
    assertEquals(testGroup.getDisplayName(), groups.get(0)[2]);
    assertEquals(testGroup.getDescription(), groups.get(0)[3]);
    assertNotNull(groups.get(0)[4]);
    assertNull(groups.get(0)[5]);
    
    // now delete the group
    testGroup.delete();
    
    grouperProvisioningOutput = fullProvision();
    grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
    
    groups = new GcDbAccess().sql("select group_name, id_index, display_name, description, last_modified, deleted from gr_mp_groups").selectList(Object[].class);
    assertEquals(0, groups.size());
    
    groupAttributes = new GcDbAccess().sql("select group_id_index, attribute_name, attribute_value, last_modified, deleted from gr_mp_group_attributes").selectList(Object[].class);
    assertEquals(0, groupAttributes.size());
    
    entities = new GcDbAccess().sql("select subject_id_index, subject_id, last_modified, deleted from gr_mp_subjects").selectList(Object[].class);
    assertEquals(0, entities.size());
    
    memberships = new GcDbAccess().sql("select group_id_index, subject_id_index, last_modified, deleted from gr_mp_memberships").selectList(Object[].class);
    assertEquals(0, memberships.size());
    
  }
  
  public void testFullMidPointProvisionerWithLastModifiedTimestampColumnOnly() {
    
    MidPointProvisionerTestUtils.configureMidpointProvisioner(new MidPointProvisionerTestConfigInput()
        .addExtraConfig("midPointLastModifiedColumnType", "timestamp")
        .addExtraConfig("midPointLastModifiedColumnName", "last_modified"));

    Stem stem = new StemSave(this.grouperSession).assignName("test").save();
    Stem stem2 = new StemSave(this.grouperSession).assignName("test2").save();
    
    // mark some folders to provision
    Group testGroup = new GroupSave(this.grouperSession).assignName("test:testGroup").save();
    Group testGroup2 = new GroupSave(this.grouperSession).assignName("test2:testGroup2").save();
    
    testGroup.addMember(SubjectTestHelper.SUBJ0, false);
    testGroup.addMember(SubjectTestHelper.SUBJ1, false);
    
    testGroup2.addMember(SubjectTestHelper.SUBJ2, false);
    testGroup2.addMember(SubjectTestHelper.SUBJ3, false);
    
    final GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision("midPointProvTest");
    attributeValue.setTargetName("midPointProvTest");
    attributeValue.setStemScopeString("sub");
    
    Map<String, Object> metadataNameValues = new HashMap<String, Object>();
    
    Set<String> targetValues = new HashSet<>();
    targetValues.add("a");
    targetValues.add("b");
    metadataNameValues.put("md_grouper_midPointTarget", targetValues);

    attributeValue.setMetadataNameValues(metadataNameValues);

    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);

    //AttributeAssign attributeAssign = stem.getAttributeDelegate().addAttribute(GrouperProvisioningAttributeNames.retrieveAttributeDefNameMarker()).getAttributeAssign();
    //attributeAssign.getAttributeValueDelegate().assignValueString(GrouperProvisioningAttributeNames.retrieveAttributeDefNameDoProvision())
    
    //lets sync these over
    GrouperProvisioningOutput grouperProvisioningOutput = fullProvision();
    GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
    
    List<Object[]> groups = new GcDbAccess().sql("select group_name, id_index, display_name, description, last_modified, deleted from gr_mp_groups").selectList(Object[].class);
    assertEquals(1, groups.size());
    
    assertEquals(testGroup.getName(), groups.get(0)[0]);
    assertEquals(testGroup.getIdIndex().longValue(), ((BigDecimal)groups.get(0)[1]).longValue());
    assertEquals(testGroup.getDisplayName(), groups.get(0)[2]);
    assertNull(groups.get(0)[3]);
    assertNotNull(groups.get(0)[4]);
    assertNull(groups.get(0)[5]);
    
    
    List<Object[]> groupAttributes = new GcDbAccess().sql("select group_id_index, attribute_name, attribute_value, last_modified, deleted from gr_mp_group_attributes").selectList(Object[].class);
    assertEquals(2, groupAttributes.size());
    
    Map<MultiKey, Object[]> attributeNameValueToGroupAttributes = new HashMap<>();
    for (Object[] groupAttribute: groupAttributes) {
      attributeNameValueToGroupAttributes.put(new MultiKey(groupAttribute[1], groupAttribute[2]), groupAttribute);
    }
    
    assertNotNull(attributeNameValueToGroupAttributes.get(new MultiKey("target", "a")));
    assertNotNull(attributeNameValueToGroupAttributes.get(new MultiKey("target", "b")));
    assertNotNull(attributeNameValueToGroupAttributes.get(new MultiKey("target", "a"))[3]);
    assertNotNull(attributeNameValueToGroupAttributes.get(new MultiKey("target", "b"))[3]);
    assertNull(attributeNameValueToGroupAttributes.get(new MultiKey("target", "a"))[4]);
    assertNull(attributeNameValueToGroupAttributes.get(new MultiKey("target", "b"))[4]);
    
    List<Object[]> entities = new GcDbAccess().sql("select subject_id_index, subject_id, last_modified, deleted from gr_mp_subjects").selectList(Object[].class);
    assertEquals(2, entities.size());
    Map<String, Object[]> subjectIdToSubjectAttributes = new HashMap<>();
    for (Object[] entity: entities) {
      subjectIdToSubjectAttributes.put(entity[1].toString(), entity);
    }
    
    assertNotNull(subjectIdToSubjectAttributes.get(SubjectTestHelper.SUBJ0.getId()));
    assertNotNull(subjectIdToSubjectAttributes.get(SubjectTestHelper.SUBJ0.getId())[2]);
    assertNull(subjectIdToSubjectAttributes.get(SubjectTestHelper.SUBJ0.getId())[3]);
    
    assertNotNull(subjectIdToSubjectAttributes.get(SubjectTestHelper.SUBJ1.getId()));
    assertNotNull(subjectIdToSubjectAttributes.get(SubjectTestHelper.SUBJ1.getId())[2]);
    assertNull(subjectIdToSubjectAttributes.get(SubjectTestHelper.SUBJ1.getId())[3]);
    
    
    List<Object[]> memberships = new GcDbAccess().sql("select group_id_index, subject_id_index, last_modified, deleted from gr_mp_memberships").selectList(Object[].class);
    
    Map<MultiKey, Object[]> groupIdSubjectIdToMembershipAttributes = new HashMap<>();
    
    for (Object[] membershipAttributes: memberships) {
      groupIdSubjectIdToMembershipAttributes.put(new MultiKey(membershipAttributes[0], membershipAttributes[1]), membershipAttributes);
    }
    
    assertEquals(2, groupIdSubjectIdToMembershipAttributes.size());
    for (MultiKey groupIdSubjectId : groupIdSubjectIdToMembershipAttributes.keySet()) {
      Object[] membershipAttributes = groupIdSubjectIdToMembershipAttributes.get(groupIdSubjectId);
      assertNotNull(membershipAttributes[2]);
      assertNull(membershipAttributes[3]);
    }
    
    
    // delete a member from testGroup and reprovision
    testGroup.deleteMember(SubjectTestHelper.SUBJ1);
    
    grouperProvisioningOutput = fullProvision();
    grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());

    memberships = new GcDbAccess().sql("select group_id_index, subject_id_index, last_modified, deleted from gr_mp_memberships").selectList(Object[].class);
    
    groupIdSubjectIdToMembershipAttributes = new HashMap<>();
    
    for (Object[] membershipAttributes: memberships) {
      groupIdSubjectIdToMembershipAttributes.put(new MultiKey(membershipAttributes[0], membershipAttributes[1]), membershipAttributes);
    }
    
    assertEquals(1, groupIdSubjectIdToMembershipAttributes.size());
    
    //update group description and reprovision
    testGroup = new GroupSave(this.grouperSession).assignName(testGroup.getName())
        .assignUuid(testGroup.getUuid()).assignDescription("newDescription")
        .assignSaveMode(SaveMode.UPDATE).save();
    
    grouperProvisioningOutput = fullProvision();
    grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
    
    groups = new GcDbAccess().sql("select group_name, id_index, display_name, description, last_modified, deleted from gr_mp_groups").selectList(Object[].class);
    assertEquals(1, groups.size());
    assertEquals(testGroup.getName(), groups.get(0)[0]);
    assertEquals(testGroup.getIdIndex().longValue(), ((BigDecimal)groups.get(0)[1]).longValue());
    assertEquals(testGroup.getDisplayName(), groups.get(0)[2]);
    assertEquals(testGroup.getDescription(), groups.get(0)[3]);
    assertNotNull(groups.get(0)[4]);
    assertNull(groups.get(0)[5]);
    
    // now delete the group
    testGroup.delete();
    
    grouperProvisioningOutput = fullProvision();
    grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
    
    groups = new GcDbAccess().sql("select group_name, id_index, display_name, description, last_modified, deleted from gr_mp_groups").selectList(Object[].class);
    assertEquals(0, groups.size());
    
    groupAttributes = new GcDbAccess().sql("select group_id_index, attribute_name, attribute_value, last_modified, deleted from gr_mp_group_attributes").selectList(Object[].class);
    assertEquals(0, groupAttributes.size());
    
    entities = new GcDbAccess().sql("select subject_id_index, subject_id, last_modified, deleted from gr_mp_subjects").selectList(Object[].class);
    assertEquals(0, entities.size());
    
    memberships = new GcDbAccess().sql("select group_id_index, subject_id_index, last_modified, deleted from gr_mp_memberships").selectList(Object[].class);
    assertEquals(0, memberships.size());
    
  }
  
  public void testFullMidPointProvisionerWithDeletedColumnOnly() {
    
    MidPointProvisionerTestUtils.configureMidpointProvisioner(new MidPointProvisionerTestConfigInput()
        .addExtraConfig("midPointDeletedColumnName", "deleted"));

    Stem stem = new StemSave(this.grouperSession).assignName("test").save();
    Stem stem2 = new StemSave(this.grouperSession).assignName("test2").save();
    
    // mark some folders to provision
    Group testGroup = new GroupSave(this.grouperSession).assignName("test:testGroup").save();
    Group testGroup2 = new GroupSave(this.grouperSession).assignName("test2:testGroup2").save();
    
    testGroup.addMember(SubjectTestHelper.SUBJ0, false);
    testGroup.addMember(SubjectTestHelper.SUBJ1, false);
    
    testGroup2.addMember(SubjectTestHelper.SUBJ2, false);
    testGroup2.addMember(SubjectTestHelper.SUBJ3, false);
    
    final GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision("midPointProvTest");
    attributeValue.setTargetName("midPointProvTest");
    attributeValue.setStemScopeString("sub");
    
    Map<String, Object> metadataNameValues = new HashMap<String, Object>();
    
    Set<String> targetValues = new HashSet<>();
    targetValues.add("a");
    targetValues.add("b");
    metadataNameValues.put("md_grouper_midPointTarget", targetValues);

    attributeValue.setMetadataNameValues(metadataNameValues);

    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);

    //AttributeAssign attributeAssign = stem.getAttributeDelegate().addAttribute(GrouperProvisioningAttributeNames.retrieveAttributeDefNameMarker()).getAttributeAssign();
    //attributeAssign.getAttributeValueDelegate().assignValueString(GrouperProvisioningAttributeNames.retrieveAttributeDefNameDoProvision())
    
    //lets sync these over
    GrouperProvisioningOutput grouperProvisioningOutput = fullProvision();
    GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
    
    List<Object[]> groups = new GcDbAccess().sql("select group_name, id_index, display_name, description, last_modified, deleted from gr_mp_groups").selectList(Object[].class);
    assertEquals(1, groups.size());
    
    assertEquals(testGroup.getName(), groups.get(0)[0]);
    assertEquals(testGroup.getIdIndex().longValue(), ((BigDecimal)groups.get(0)[1]).longValue());
    assertEquals(testGroup.getDisplayName(), groups.get(0)[2]);
    assertNull(groups.get(0)[3]);
    assertNull(groups.get(0)[4]);
    assertEquals("F", groups.get(0)[5]);
    
    
    List<Object[]> groupAttributes = new GcDbAccess().sql("select group_id_index, attribute_name, attribute_value, last_modified, deleted from gr_mp_group_attributes").selectList(Object[].class);
    assertEquals(2, groupAttributes.size());
    
    Map<MultiKey, Object[]> attributeNameValueToGroupAttributes = new HashMap<>();
    for (Object[] groupAttribute: groupAttributes) {
      attributeNameValueToGroupAttributes.put(new MultiKey(groupAttribute[1], groupAttribute[2]), groupAttribute);
    }
    
    assertNotNull(attributeNameValueToGroupAttributes.get(new MultiKey("target", "a")));
    assertNotNull(attributeNameValueToGroupAttributes.get(new MultiKey("target", "b")));
    assertNull(attributeNameValueToGroupAttributes.get(new MultiKey("target", "a"))[3]);
    assertNull(attributeNameValueToGroupAttributes.get(new MultiKey("target", "b"))[3]);
    assertEquals("F", attributeNameValueToGroupAttributes.get(new MultiKey("target", "a"))[4]);
    assertEquals("F", attributeNameValueToGroupAttributes.get(new MultiKey("target", "b"))[4]);
    
    List<Object[]> entities = new GcDbAccess().sql("select subject_id_index, subject_id, last_modified, deleted from gr_mp_subjects").selectList(Object[].class);
    assertEquals(2, entities.size());
    Map<String, Object[]> subjectIdToSubjectAttributes = new HashMap<>();
    for (Object[] entity: entities) {
      subjectIdToSubjectAttributes.put(entity[1].toString(), entity);
    }
    
    assertNotNull(subjectIdToSubjectAttributes.get(SubjectTestHelper.SUBJ0.getId()));
    assertNull(subjectIdToSubjectAttributes.get(SubjectTestHelper.SUBJ0.getId())[2]);
    assertEquals("F", subjectIdToSubjectAttributes.get(SubjectTestHelper.SUBJ0.getId())[3]);
    
    assertNotNull(subjectIdToSubjectAttributes.get(SubjectTestHelper.SUBJ1.getId()));
    assertNull(subjectIdToSubjectAttributes.get(SubjectTestHelper.SUBJ1.getId())[2]);
    assertEquals("F", subjectIdToSubjectAttributes.get(SubjectTestHelper.SUBJ1.getId())[3]);
    
    
    List<Object[]> memberships = new GcDbAccess().sql("select group_id_index, subject_id_index, last_modified, deleted from gr_mp_memberships").selectList(Object[].class);
    
    Map<MultiKey, Object[]> groupIdSubjectIdToMembershipAttributes = new HashMap<>();
    
    for (Object[] membershipAttributes: memberships) {
      groupIdSubjectIdToMembershipAttributes.put(new MultiKey(membershipAttributes[0], membershipAttributes[1]), membershipAttributes);
    }
    
    assertEquals(2, groupIdSubjectIdToMembershipAttributes.size());
    for (MultiKey groupIdSubjectId : groupIdSubjectIdToMembershipAttributes.keySet()) {
      Object[] membershipAttributes = groupIdSubjectIdToMembershipAttributes.get(groupIdSubjectId);
      assertNull(membershipAttributes[2]);
      assertEquals("F", membershipAttributes[3]);
    }
    
    
    // delete a member from testGroup and reprovision
    testGroup.deleteMember(SubjectTestHelper.SUBJ1);
    
    grouperProvisioningOutput = fullProvision();
    grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());

    memberships = new GcDbAccess().sql("select group_id_index, subject_id_index, last_modified, deleted from gr_mp_memberships").selectList(Object[].class);
    
    groupIdSubjectIdToMembershipAttributes = new HashMap<>();
    
    for (Object[] membershipAttributes: memberships) {
      groupIdSubjectIdToMembershipAttributes.put(new MultiKey(membershipAttributes[0], membershipAttributes[1]), membershipAttributes);
    }
    
    assertEquals(2, groupIdSubjectIdToMembershipAttributes.size());
    boolean falseFound = false;
    boolean trueFound = false;
    for (MultiKey groupIdSubjectId : groupIdSubjectIdToMembershipAttributes.keySet()) {
      Object[] membershipAttributes = groupIdSubjectIdToMembershipAttributes.get(groupIdSubjectId);
      assertNull(membershipAttributes[2]);
      if (membershipAttributes[3].equals("F")) {
        falseFound = true;
      }
      if (membershipAttributes[3].equals("T")) {
        trueFound = true;
      }
    }
    
    assertTrue(falseFound);
    assertTrue(trueFound);
    
    //update group description and reprovision
    testGroup = new GroupSave(this.grouperSession).assignName(testGroup.getName())
        .assignUuid(testGroup.getUuid()).assignDescription("newDescription")
        .assignSaveMode(SaveMode.UPDATE).save();
    
    grouperProvisioningOutput = fullProvision();
    grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
    
    groups = new GcDbAccess().sql("select group_name, id_index, display_name, description, last_modified, deleted from gr_mp_groups").selectList(Object[].class);
    assertEquals(1, groups.size());
    assertEquals(testGroup.getName(), groups.get(0)[0]);
    assertEquals(testGroup.getIdIndex().longValue(), ((BigDecimal)groups.get(0)[1]).longValue());
    assertEquals(testGroup.getDisplayName(), groups.get(0)[2]);
    assertEquals(testGroup.getDescription(), groups.get(0)[3]);
    assertNull(groups.get(0)[4]);
    assertEquals("F", groups.get(0)[5]);
    
    // now delete the group
    testGroup.delete();
    
    grouperProvisioningOutput = fullProvision();
    grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
    
    groups = new GcDbAccess().sql("select group_name, id_index, display_name, description, last_modified, deleted from gr_mp_groups").selectList(Object[].class);
    assertEquals(1, groups.size());
    assertEquals(testGroup.getName(), groups.get(0)[0]);
    assertEquals(testGroup.getIdIndex().longValue(), ((BigDecimal)groups.get(0)[1]).longValue());
    assertEquals(testGroup.getDisplayName(), groups.get(0)[2]);
    assertEquals(testGroup.getDescription(), groups.get(0)[3]);
    assertNull(groups.get(0)[4]);
    assertEquals("T", groups.get(0)[5]);
    
    groupAttributes = new GcDbAccess().sql("select group_id_index, attribute_name, attribute_value, last_modified, deleted from gr_mp_group_attributes").selectList(Object[].class);
    assertEquals(2, groupAttributes.size());
    
    attributeNameValueToGroupAttributes = new HashMap<>();
    for (Object[] groupAttribute: groupAttributes) {
      attributeNameValueToGroupAttributes.put(new MultiKey(groupAttribute[1], groupAttribute[2]), groupAttribute);
    }
    
    assertNotNull(attributeNameValueToGroupAttributes.get(new MultiKey("target", "a")));
    assertNotNull(attributeNameValueToGroupAttributes.get(new MultiKey("target", "b")));
    assertNull(attributeNameValueToGroupAttributes.get(new MultiKey("target", "a"))[3]);
    assertNull(attributeNameValueToGroupAttributes.get(new MultiKey("target", "b"))[3]);
    assertEquals("T", attributeNameValueToGroupAttributes.get(new MultiKey("target", "a"))[4]);
    assertEquals("T", attributeNameValueToGroupAttributes.get(new MultiKey("target", "b"))[4]);
    
    entities = new GcDbAccess().sql("select subject_id_index, subject_id, last_modified, deleted from gr_mp_subjects").selectList(Object[].class);
    assertEquals(2, entities.size());
    subjectIdToSubjectAttributes = new HashMap<>();
    for (Object[] entity: entities) {
      subjectIdToSubjectAttributes.put(entity[1].toString(), entity);
    }
    
    assertNotNull(subjectIdToSubjectAttributes.get(SubjectTestHelper.SUBJ0.getId()));
    assertNull(subjectIdToSubjectAttributes.get(SubjectTestHelper.SUBJ0.getId())[2]);
    assertEquals("T", subjectIdToSubjectAttributes.get(SubjectTestHelper.SUBJ0.getId())[3]);
    
    assertNotNull(subjectIdToSubjectAttributes.get(SubjectTestHelper.SUBJ1.getId()));
    assertNull(subjectIdToSubjectAttributes.get(SubjectTestHelper.SUBJ1.getId())[2]);
    assertEquals("T", subjectIdToSubjectAttributes.get(SubjectTestHelper.SUBJ1.getId())[3]);
    
    memberships = new GcDbAccess().sql("select group_id_index, subject_id_index, last_modified, deleted from gr_mp_memberships").selectList(Object[].class);
    
    groupIdSubjectIdToMembershipAttributes = new HashMap<>();
    
    for (Object[] membershipAttributes: memberships) {
      groupIdSubjectIdToMembershipAttributes.put(new MultiKey(membershipAttributes[0], membershipAttributes[1]), membershipAttributes);
    }
    
    assertEquals(2, groupIdSubjectIdToMembershipAttributes.size());
    for (MultiKey groupIdSubjectId : groupIdSubjectIdToMembershipAttributes.keySet()) {
      Object[] membershipAttributes = groupIdSubjectIdToMembershipAttributes.get(groupIdSubjectId);
      assertNull(membershipAttributes[2]);
      assertEquals("T", membershipAttributes[3]);
    }
    
  }
  
  


  
  /**
   * @param ddlVersionBean
   * @param database
   */
  public void createTableGroup() {
  
    final String tableName = "gr_mp_groups";
  
    try {
      new GcDbAccess().sql("select count(*) from " + tableName).select(int.class);
    } catch (Exception e) {
      //we need to delete the test table if it is there, and create a new one
      //drop field id col, first drop foreign keys
      GrouperDdlUtils.changeDatabase(GrouperMidpointDdl.V1.getObjectName(), new DdlUtilsChangeDatabase() {
    
        public void changeDatabase(DdlVersionBean ddlVersionBean) {
          
          Database database = ddlVersionBean.getDatabase();
    
          
          Table loaderTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, tableName);
          
          GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "group_name", Types.VARCHAR, "1024", false, false);
          GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "id_index", Types.BIGINT, "10", true, true);
          GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "display_name", Types.VARCHAR, "1024", false, false);
          GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "description", Types.VARCHAR, "1024", false, false);
          GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "last_modified", Types.BIGINT, "200", false, true);
          GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "deleted", Types.VARCHAR, "1", false, true);
          
          GrouperDdlUtils.ddlutilsTableComment(ddlVersionBean, tableName, "This table holds groups");
          GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "id_index", "This is the integer identifier for a group and foreign key to group attributes and memberships");

          GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "group_name", "Name of group mapped in some way");
          GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "display_name", "Display name of group mapped in some way");
          GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "description", "Description of group mapped in some way");

          GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "last_modified", "Millis since 1970, will be sequential and unique");
          GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "deleted", "T or F.  Deleted rows will be removed after they have had time to be processed");

          GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, tableName, tableName + "_ldx", true, "last_modified");
          GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, tableName, tableName + "_idx", true, "id_index");
          
          String scriptOverride = ddlVersionBean.isSmallIndexes() ? "\nCREATE INDEX gr_mp_groups_ddx ON gr_mp_groups (display_name(255));\n" : null;
          GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, ddlVersionBean, tableName, tableName + "_ddx", scriptOverride, false, "display_name");

          scriptOverride = ddlVersionBean.isSmallIndexes() ? "\nCREATE INDEX gr_mp_groups_gdx ON gr_mp_groups (group_name(255));\n" : null;
          GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, ddlVersionBean, tableName, tableName + "_gdx", scriptOverride, false, "group_name");

        }
        
      });
            
    }
  }
  
  /**
   * @param ddlVersionBean
   * @param database
   */
  public void createTableGroupAttr() {
  
    String tableName = "gr_mp_group_attributes";
    
    try {
      new GcDbAccess().sql("select count(*) from " + tableName).select(int.class);
    } catch (Exception e) {
      //we need to delete the test table if it is there, and create a new one
      //drop field id col, first drop foreign keys
      GrouperDdlUtils.changeDatabase(GrouperMidpointDdl.V1.getObjectName(), new DdlUtilsChangeDatabase() {
    
        public void changeDatabase(DdlVersionBean ddlVersionBean) {
          
          Database database = ddlVersionBean.getDatabase();

          Table loaderTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, tableName);

          // no primary key
          
          GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "group_id_index", Types.BIGINT, "10", false, true);
          GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "attribute_name", Types.VARCHAR, "1000", false, true);
          GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "attribute_value", Types.VARCHAR, "4000", false, false);
          GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "last_modified", Types.BIGINT, "200", false, true);
          GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "deleted", Types.VARCHAR, "1", false, true);

          GrouperDdlUtils.ddlutilsTableComment(ddlVersionBean, tableName, "This table holds group attributes which are one to one or one to many to the groups table");
          GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "group_id_index", "This is the integer identifier for a group and foreign key to groups and memberships");
          GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "attribute_name", "Attribute name for attributes not in the main group table");
          GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "attribute_value", "Attribute value could be null");
          GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "last_modified", "Millis since 1970, will be sequential and unique");
          GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "deleted", "T or F.  Deleted rows will be removed after they have had time to be processed");

          GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, tableName, tableName + "_ldx", true, "last_modified");
          
          String scriptOverride = ddlVersionBean.isSmallIndexes() ? "\nCREATE UNIQUE INDEX gr_mp_group_attributes_idx ON gr_mp_group_attributes (group_id_index, attribute_name(100), attribute_value(155));\n" : null;
          GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, ddlVersionBean, tableName, tableName + "_idx", scriptOverride, true, "group_id_index", "attribute_name", "attribute_value");

          GrouperDdlUtils.ddlutilsFindOrCreateForeignKey(database, tableName, tableName + "_fk", 
              "gr_mp_groups", "group_id_index", "id_index");

        }
        
      });
    }
  }
  
  /**
   * @param ddlVersionBean
   * @param database
   */
  public void createTableEntity() {
  
    final String tableName = "gr_mp_subjects";
  
    try {
      new GcDbAccess().sql("select count(*) from " + tableName).select(int.class);
    } catch (Exception e) {
      //we need to delete the test table if it is there, and create a new one
      //drop field id col, first drop foreign keys
      GrouperDdlUtils.changeDatabase(GrouperMidpointDdl.V1.getObjectName(), new DdlUtilsChangeDatabase() {
    
        public void changeDatabase(DdlVersionBean ddlVersionBean) {
          
          Database database = ddlVersionBean.getDatabase();
          
          Table loaderTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, tableName);
          
          GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "subject_id_index", Types.BIGINT, "20", true, true);
          GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "subject_id", Types.VARCHAR, "1024", false, false);
          GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "last_modified", Types.BIGINT, "20", false, true);
          GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "deleted", Types.VARCHAR, "1", false, true);
          
          GrouperDdlUtils.ddlutilsTableComment(ddlVersionBean, tableName, "This table holds subjects");
          GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "subject_id_index", "This is the integer identifier for a subject and foreign key to subject attributes and memberships");
          GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "subject_id", "Subject ID mapped in some way");
          GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "last_modified", "Millis since 1970, will be sequential and unique");
          GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "deleted", "T or F.  Deleted rows will be removed after they have had time to be processed");

          GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, tableName, tableName + "_ldx", true, "last_modified");
          GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, tableName, tableName + "_idx", true, "subject_id_index");
          
          String scriptOverride = ddlVersionBean.isSmallIndexes() ? "\nCREATE INDEX gr_mp_subjects_sdx ON gr_mp_subjects (subject_id(255));\n" : null;
          GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, ddlVersionBean, tableName, tableName + "_sdx", scriptOverride, false, "subject_id");

        }
        
      });
    }
    
  }
  
  /**
   * @param ddlVersionBean
   * @param database
   */
  public void createTableEntityAttr() {
  
    String tableName = "gr_mp_subject_attributes";
    
    try {
      new GcDbAccess().sql("select count(*) from " + tableName).select(int.class);
    } catch (Exception e) {
      //we need to delete the test table if it is there, and create a new one
      //drop field id col, first drop foreign keys
      GrouperDdlUtils.changeDatabase(GrouperMidpointDdl.V1.getObjectName(), new DdlUtilsChangeDatabase() {
    
        public void changeDatabase(DdlVersionBean ddlVersionBean) {
          
          Database database = ddlVersionBean.getDatabase();

          Table loaderTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, tableName);
          
          GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "subject_id_index", Types.BIGINT, "20", false, true);
          GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "attribute_name", Types.VARCHAR, "1000", false, true);
          GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "attribute_value", Types.VARCHAR, "4000", false, false);
          GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "last_modified", Types.BIGINT, "200", false, true);
          GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "deleted", Types.VARCHAR, "1", false, true);

          GrouperDdlUtils.ddlutilsTableComment(ddlVersionBean, tableName, "This table holds subject attributes which are one to one or one to many to the subjects table");
          GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "subject_id_index", "This is the integer identifier and foreign key to subjects");
          GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "attribute_name", "Attribute name for attributes not in the main subject table");
          GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "attribute_value", "Attribute value could be null");
          GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "last_modified", "Millis since 1970, will be sequential and unique");
          GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "deleted", "T or F.  Deleted rows will be removed after they have had time to be processed");

          GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, tableName, tableName + "_ldx", true, "last_modified");
          
          String scriptOverride = ddlVersionBean.isSmallIndexes() ? "\nCREATE UNIQUE INDEX gr_mp_subject_attributes_idx ON gr_mp_subject_attributes (subject_id_index, attribute_name(100), attribute_value(155));\n" : null;
          GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, ddlVersionBean, tableName, tableName + "_idx", scriptOverride, true, "subject_id_index", "attribute_name", "attribute_value");

          GrouperDdlUtils.ddlutilsFindOrCreateForeignKey(database, tableName, tableName + "_fk", 
              "gr_mp_subjects", "subject_id_index", "subject_id_index");
        }
        
      });
      
    }

  }
  
  /**
   * @param ddlVersionBean
   * @param database
   */
  public void createTableMship0() {
  
    String tableName = "gr_mp_memberships";
    
    try {
      new GcDbAccess().sql("select count(*) from " + tableName).select(int.class);
    } catch (Exception e) {
      //we need to delete the test table if it is there, and create a new one
      //drop field id col, first drop foreign keys
      GrouperDdlUtils.changeDatabase(GrouperMidpointDdl.V1.getObjectName(), new DdlUtilsChangeDatabase() {
    
        public void changeDatabase(DdlVersionBean ddlVersionBean) {
          
          Database database = ddlVersionBean.getDatabase();

          Table loaderTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, tableName);
          
          GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "group_id_index", Types.BIGINT, "10", false, true);
          GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "subject_id_index", Types.BIGINT, "20", false, true);
          GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "last_modified", Types.BIGINT, "200", false, true);
          GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "deleted", Types.VARCHAR, "1", false, true);

          GrouperDdlUtils.ddlutilsTableComment(ddlVersionBean, tableName, "This table holds memberships.  The primary key is group_id_index and subject_id_index");
          GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "group_id_index", "This is the foreign key to groups");
          GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "subject_id_index", "This is the foreign key to subjects");
          GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "last_modified", "Millis since 1970, will be sequential and unique");
          GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "deleted", "T or F.  Deleted rows will be removed after they have had time to be processed");

          GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, tableName, tableName + "_ldx", true, "last_modified");
          GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, tableName, tableName + "_idx", true, 
              "group_id_index", "subject_id_index");

          GrouperDdlUtils.ddlutilsFindOrCreateForeignKey(database, tableName, tableName + "_sfk", 
              "gr_mp_subjects", "subject_id_index", "subject_id_index");
          GrouperDdlUtils.ddlutilsFindOrCreateForeignKey(database, tableName, tableName + "_gfk", 
              "gr_mp_groups", "group_id_index", "id_index");

          
        }
        
      });
    }
  }

}
