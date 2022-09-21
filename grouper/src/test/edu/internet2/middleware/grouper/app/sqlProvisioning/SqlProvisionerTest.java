package edu.internet2.middleware.grouper.app.sqlProvisioning;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.model.Database;
import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.model.Table;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.app.ldapProvisioning.LdapProvisionerTestUtils;
import edu.internet2.middleware.grouper.app.ldapProvisioning.ldapSyncDao.LdapSyncDaoForLdap;
import edu.internet2.middleware.grouper.app.loader.GrouperLoader;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderStatus;
import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioner;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningAttributeNames;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningAttributeValue;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningBaseTest;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningJob;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningOutput;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningService;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningType;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningConsumer;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningEntity;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningGroup;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningGroupWrapper;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningObjectChange;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningObjectChangeAction;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningUpdatable;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoDeleteGroupsRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoInsertGroupsRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveAllEntitiesRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveAllEntitiesResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveAllGroupsRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveAllGroupsResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveGroupsRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveGroupsResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoUpdateGroupsRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoUpdateGroupsResponse;
import edu.internet2.middleware.grouper.attr.AttributeDefSave;
import edu.internet2.middleware.grouper.cache.GrouperCacheUtils;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.cfg.dbConfig.GrouperDbConfig;
import edu.internet2.middleware.grouper.changeLog.ChangeLogHelper;
import edu.internet2.middleware.grouper.changeLog.ChangeLogTempToEntity;
import edu.internet2.middleware.grouper.changeLog.esb.consumer.EsbConsumer;
import edu.internet2.middleware.grouper.ddl.DdlUtilsChangeDatabase;
import edu.internet2.middleware.grouper.ddl.DdlVersionBean;
import edu.internet2.middleware.grouper.ddl.GrouperDdlUtils;
import edu.internet2.middleware.grouper.ddl.GrouperTestDdl;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.hib3.Hib3MemberDAO;
import edu.internet2.middleware.grouper.internal.util.GrouperUuid;
import edu.internet2.middleware.grouper.ldap.LdapAttribute;
import edu.internet2.middleware.grouper.ldap.LdapModificationItem;
import edu.internet2.middleware.grouper.ldap.LdapModificationResult;
import edu.internet2.middleware.grouper.ldap.LdapModificationType;
import edu.internet2.middleware.grouper.misc.GrouperFailsafe;
import edu.internet2.middleware.grouper.misc.GrouperStartup;
import edu.internet2.middleware.grouper.misc.M;
import edu.internet2.middleware.grouper.misc.SaveMode;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSync;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncDao;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncErrorCode;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncGroup;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncJob;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncJobState;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncMember;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncMembership;
import edu.internet2.middleware.grouperClient.util.GrouperClientConfig;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.lang3.StringUtils;
import edu.internet2.middleware.subject.Subject;
import junit.textui.TestRunner;

/**
 * 
 * @author mchyzer
 *
 */
public class SqlProvisionerTest extends GrouperProvisioningBaseTest {

  /**
   * 
   */
  @Override
  public String defaultConfigId() {
    return "sqlProvTest";
  }

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    
//    GrouperStartup.startup();
//    
//    GrouperSession grouperSession = GrouperSession.startRootSession();
//
//    //RegistryReset rr = new RegistryReset();
//
//   // rr._addSubjects();
//
//    
//
//    
//    SqlMembershipProvisionerTest sqlMembershipProvisionerTest = new SqlMembershipProvisionerTest();
//    sqlMembershipProvisionerTest.ensureTableSyncTables();
//
//    new GcDbAccess().sql("delete from testgrouper_prov_group");
//    new GcDbAccess().sql("delete from testgrouper_prov_mship0");
//    new GcDbAccess().sql("delete from testgrouper_prov_mship1");
//
//    sqlMembershipProvisionerTest.grouperSession = grouperSession;
//    sqlMembershipProvisionerTest.testSimpleGroupMembershipProvisioningFull_1();

    GrouperStartup.startup();
    // testSimpleGroupLdapPa
    TestRunner.run(new SqlProvisionerTest("testGroupEntityMembershipRenameEntityIncrementalMatchOnOld"));
    
  }
  
  public SqlProvisionerTest() {
    super();
    // TODO Auto-generated constructor stub
  }

  /**
   * 
   * @param name
   */
  public SqlProvisionerTest(String name) {
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
  
      new GcDbAccess().sql("delete from testgrouper_pro_ldap_group_attr").executeSql();
      new GcDbAccess().sql("delete from testgrouper_prov_ldap_group").executeSql();
      new GcDbAccess().sql("delete from testgrouper_pro_dap_entity_attr").executeSql();
      new GcDbAccess().sql("delete from testgrouper_prov_ldap_entity").executeSql();
      new GcDbAccess().sql("delete from testgrouper_prov_group").executeSql();
      new GcDbAccess().sql("delete from testgrouper_prov_entity").executeSql();
      new GcDbAccess().sql("delete from testgrouper_prov_mship0").executeSql();
      new GcDbAccess().sql("delete from testgrouper_prov_mship1").executeSql();
      new GcDbAccess().sql("delete from testgrouper_prov_mship2").executeSql();
      
      failsafeGroups.clear();
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
  public void dropTableSyncTables() {
    
    dropTableSyncTable("testgrouper_pro_ldap_group_attr");
    dropTableSyncTable("testgrouper_prov_ldap_group");
    dropTableSyncTable("testgrouper_pro_dap_entity_attr");
    dropTableSyncTable("testgrouper_prov_ldap_entity");
    dropTableSyncTable("testgrouper_prov_group");
    dropTableSyncTable("testgrouper_prov_mship0");
    dropTableSyncTable("testgrouper_prov_mship1");
    
  }
  
  public void testSimpleMembershipGroupNameSubjectId() {
    
    SqlProvisionerTestUtils.configureSqlProvisioner(new SqlProvisionerTestConfigInput()
        .assignMembershipDeleteType("deleteMembershipsIfNotExistInGrouper")
        .assignMembershipTableName("testgrouper_prov_mship0")
//        .assignMembershipTableIdColumn("group_name, subject_id")
//        .assignMembershipGroupForeignKeyColumn("group_name")
//        .assignMembershipEntityForeignKeyColumn("subject_id")
        .assignMembershipAttributeCount(2)
    );

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
    attributeValue.setDoProvision("sqlProvTest");
    attributeValue.setTargetName("sqlProvTest");
    attributeValue.setStemScopeString("sub");

    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);

    //AttributeAssign attributeAssign = stem.getAttributeDelegate().addAttribute(GrouperProvisioningAttributeNames.retrieveAttributeDefNameMarker()).getAttributeAssign();
    //attributeAssign.getAttributeValueDelegate().assignValueString(GrouperProvisioningAttributeNames.retrieveAttributeDefNameDoProvision())
    
    //lets sync these over
    GrouperProvisioningOutput grouperProvisioningOutput = fullProvision();
    GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
    
    String sql = "select group_name, subject_id from testgrouper_prov_mship0";
    
    List<Object[]> dataInTable = new GcDbAccess().sql(sql).selectList(Object[].class);
    
    Set<MultiKey> membershipsInTable = new HashSet<MultiKey>();
    
    for (Object[] row: dataInTable) {
      membershipsInTable.add(new MultiKey(row));
    }
    
    assertEquals(2, membershipsInTable.size());
    assertTrue(membershipsInTable.contains(new MultiKey("test:testGroup", "test.subject.0")));
    assertTrue(membershipsInTable.contains(new MultiKey("test:testGroup", "test.subject.1")));

  }
  
  public void testIncrementalSyncSqlProvisionerFailsafe() {
    
    
    setupFailsafeJob();

    GrouperStartup.startup();
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.sqlProvTest.failsafeMinGroupSize").value("8").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.sqlProvTest.failsafeMaxOverallPercentMembershipsRemove").value("20").store();
        
    assertEquals(new Integer(0), new GcDbAccess().connectionName("grouper").sql("select count(1) from testgrouper_prov_group").select(int.class));
    
    GrouperProvisioningOutput grouperProvisioningOutput = fullProvision();
    GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    
    incrementalProvision();
    
    GrouperSession grouperSession = GrouperSession.startRootSession();

    
    //lets sync these over
    String jobName = "OTHER_JOB_provisioner_full_sqlProvTest";
    fullProvision();
    
    assertFalse(GrouperFailsafe.isFailsafeIssue(jobName));
    
    List<Object[]> groups = new GcDbAccess().sql("select uuid, posix_id, name from testgrouper_prov_group order by name").selectList(Object[].class);
    assertEquals(10, groups.size());
    
    List<Object[]> entities = new GcDbAccess().sql("select uuid, name, subject_id_or_identifier, description from testgrouper_prov_entity order by name").selectList(Object[].class);
    assertEquals(10, entities.size());
    
    List<Object[]> memberships = new GcDbAccess().sql("select group_uuid, entity_uuid from testgrouper_prov_mship2").selectList(Object[].class);
    assertEquals(100, memberships.size());
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.sqlProvTest.failsafeMaxOverallPercentMembershipsRemove").value("25").store();
    
    for (int i=0;i<5;i++) {
      failsafeGroups.get(i).delete();
    }
    try {
      GrouperLoader.runOnceByJobName(this.grouperSession, jobName);
      fail();
    } catch(Exception e) {
    }
    
    assertTrue(GrouperFailsafe.isFailsafeIssue(jobName));

    Hib3GrouperLoaderLog hib3GrouperLoaderLog = Hib3GrouperLoaderLog.retrieveMostRecentLog(jobName);
    assertEquals("ERROR_FAILSAFE", hib3GrouperLoaderLog.getStatus());

    this.failsafeGroups.get(6).deleteMember(SubjectTestHelper.SUBJ3);
    try {
      // incremental will fail after the full fails with failsafe
      incrementalProvision();
      fail();
    } catch (Exception e) {
    }
        
    groups = new GcDbAccess().sql("select uuid, posix_id, name from testgrouper_prov_group order by name").selectList(Object[].class);
    assertEquals(10, groups.size());
    
    entities = new GcDbAccess().sql("select uuid, name, subject_id_or_identifier, description from testgrouper_prov_entity order by name").selectList(Object[].class);
    assertEquals(10, entities.size());
    
    memberships = new GcDbAccess().sql("select group_uuid, entity_uuid from testgrouper_prov_mship2").selectList(Object[].class);
    assertEquals(100, memberships.size());
    
    GrouperUtil.sleep(1000);
    
    assertFalse(GrouperFailsafe.isApproved(jobName));
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.sqlProvTest.failsafeMaxOverallPercentMembershipsRemove").value("60").store();
  
    GrouperLoader.runOnceByJobName(this.grouperSession, jobName);
  
    hib3GrouperLoaderLog = Hib3GrouperLoaderLog.retrieveMostRecentLog(jobName);
    assertEquals("SUCCESS", hib3GrouperLoaderLog.getStatus());
    assertFalse(GrouperFailsafe.isFailsafeIssue(jobName));
  
    memberships = new GcDbAccess().sql("select group_uuid, entity_uuid from testgrouper_prov_mship2").selectList(Object[].class);
    
    assertEquals(49, memberships.size());
    
    groups = new GcDbAccess().sql("select uuid, posix_id, name from testgrouper_prov_group order by name").selectList(Object[].class);
    assertEquals(5, groups.size());
    
    entities = new GcDbAccess().sql("select uuid, name, subject_id_or_identifier, description from testgrouper_prov_entity order by name").selectList(Object[].class);
    assertEquals(10, entities.size());
    
    memberships = new GcDbAccess().sql("select group_uuid, entity_uuid from testgrouper_prov_mship2").selectList(Object[].class);
    assertEquals(49, memberships.size());
    
  }
  
  public void testIncrementalSyncSqlProvisioner() {
    
    GrouperStartup.startup();
    

    try {
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
          .assignHasTargetGroupLink(true)
          .assignEntityAttributeCount(5)
          .assignGroupAttributeCount(4)
          .assignMembershipAttributeCount(3)
          );

      
      
      assertEquals(new Integer(0), new GcDbAccess().connectionName("grouper").sql("select count(1) from testgrouper_prov_group").select(int.class));
      
      GrouperProvisioningOutput grouperProvisioningOutput = fullProvision();
      GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();

      incrementalProvision();
      
      GrouperSession grouperSession = GrouperSession.startRootSession();
      
      Stem stem = new StemSave(grouperSession).assignName("test").save();
      Stem stem2 = new StemSave(grouperSession).assignName("test2").save();
      
      // mark some folders to provision
      Group testGroup = new GroupSave(grouperSession).assignName("test:testGroup").assignDescription("old description").save();
      Group testGroup2 = new GroupSave(grouperSession).assignName("test2:testGroup2").save();
      
      testGroup.addMember(SubjectTestHelper.SUBJ0, false);
      testGroup.addMember(SubjectTestHelper.SUBJ1, false);
      
      testGroup2.addMember(SubjectTestHelper.SUBJ2, false);
      testGroup2.addMember(SubjectTestHelper.SUBJ3, false);
      
      final GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
      attributeValue.setDirectAssignment(true);
      attributeValue.setDoProvision("sqlProvTest");
      attributeValue.setTargetName("sqlProvTest");
      attributeValue.setStemScopeString("sub");
  
      GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);
  
      assertEquals(new Integer(0), new GcDbAccess().connectionName("grouper").sql("select count(1) from testgrouper_prov_group").select(int.class));
      // assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperAzureGroup").list(GrouperAzureGroup.class).size());
      
      incrementalProvision();
      
      assertEquals(new Integer(1), new GcDbAccess().connectionName("grouper").sql("select count(1) from testgrouper_prov_group").select(int.class));
      assertEquals(new Integer(2), new GcDbAccess().connectionName("grouper").sql("select count(1) from testgrouper_prov_entity").select(int.class));
      assertEquals(new Integer(2), new GcDbAccess().connectionName("grouper").sql("select count(1) from testgrouper_prov_mship2").select(int.class));
      
      //now remove one of the subjects from the testGroup
      testGroup.deleteMember(SubjectTestHelper.SUBJ1);
      incrementalProvision();
      
      assertEquals(new Integer(1), new GcDbAccess().connectionName("grouper").sql("select count(1) from testgrouper_prov_group").select(int.class));
      assertEquals(new Integer(1), new GcDbAccess().connectionName("grouper").sql("select count(1) from testgrouper_prov_entity").select(int.class));
      assertEquals(new Integer(1), new GcDbAccess().connectionName("grouper").sql("select count(1) from testgrouper_prov_mship2").select(int.class));
      
      testGroup.addMember(SubjectTestHelper.SUBJ3);
      incrementalProvision();
      
      assertEquals(new Integer(1), new GcDbAccess().connectionName("grouper").sql("select count(1) from testgrouper_prov_group").select(int.class));
      assertEquals(new Integer(2), new GcDbAccess().connectionName("grouper").sql("select count(1) from testgrouper_prov_entity").select(int.class));
      assertEquals(new Integer(2), new GcDbAccess().connectionName("grouper").sql("select count(1) from testgrouper_prov_mship2").select(int.class));
      
      // change the description
      assertEquals("old description", new GcDbAccess().sql("select description from testgrouper_prov_group where name = 'test:testGroup'").select(String.class));
      testGroup = new GroupSave().assignName(testGroup.getName()).assignDescription("new description").assignReplaceAllSettings(false).save();
      incrementalProvision();

      assertEquals("new description", new GcDbAccess().sql("select description from testgrouper_prov_group where name = 'test:testGroup'").select(String.class));
      
      
      
      //now delete the group and sync again
      testGroup.delete();
      incrementalProvision();
      
      assertEquals(new Integer(0), new GcDbAccess().connectionName("grouper").sql("select count(1) from testgrouper_prov_group").select(int.class));
      assertEquals(new Integer(0), new GcDbAccess().connectionName("grouper").sql("select count(1) from testgrouper_prov_entity").select(int.class));
      assertEquals(new Integer(0), new GcDbAccess().connectionName("grouper").sql("select count(1) from testgrouper_prov_mship2").select(int.class));
      
      
    } finally {
    }
    
  }
  
  /**
   * just do a simple full sync of groups and memberships
   */
  public void testSimpleGroupMembershipProvisioningFull_1_GlobalResolvers() {
    if (!GrouperConfig.retrieveConfig().propertyValueBoolean("junit.test.ldap.dinkel", false)) {
      System.out.println("Not running testSimpleGroupMembershipProvisioningFull_1_GlobalResolvers since grouper.properties junit.test.ldap.dinkel = false");
      return;
    }
    stopAndStartLdapContainer();
    
    SqlProvisionerTestUtils.configureSqlProvisioner(new SqlProvisionerTestConfigInput()
        .assignEntityDeleteType("deleteEntitiesIfNotExistInGrouper")
        .assignGroupDeleteType("deleteGroupsIfNotExistInGrouper")
        .assignMembershipDeleteType("deleteMembershipsIfNotExistInGrouper")
        .assignEntityResolverGlobal(true)
        .assignGroupTableName("testgrouper_prov_group")
        .assignGroupTableIdColumn("uuid")
        .assignEntityTableName("testgrouper_prov_entity")
        .assignEntityTableIdColumn("uuid")
        .assignMembershipTableName("testgrouper_prov_mship2")
        .assignMembershipTableIdColumn("uuid")
        .assignMembershipGroupForeignKeyColumn("group_uuid")
        .assignMembershipEntityForeignKeyColumn("entity_uuid")
        .assignHasTargetEntityLink(true)
        .assignEntityAttributeCount(6)
        .assignGroupAttributeCount(4)
        .assignMembershipAttributeCount(3)
        );
    
    new GcDbAccess().sql("delete from testgrouper_prov_entity1").executeSql();
    
    new GcDbAccess().sql("insert into testgrouper_prov_entity1 values (?, ?, ?)").addBindVar("test.subject.0")
    .addBindVar("school0")
    .addBindVar("jdbc")
    .executeSql();
    
    new GcDbAccess().sql("insert into testgrouper_prov_entity1 values (?, ?, ?)").addBindVar("test.subject.1")
    .addBindVar("school1")
    .addBindVar("jdbc")
    .executeSql();
    
    new GcDbAccess().sql("insert into testgrouper_prov_entity1 values (?, ?, ?)").addBindVar("test.subject.2")
    .addBindVar("school2")
    .addBindVar("invalidSource")
    .executeSql();
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("ldap.personLdap.url").value("ldap://localhost:389").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("ldap.personLdap.user").value("cn=admin,dc=example,dc=edu").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("ldap.personLdap.pass").value("secret").store();
    
    LdapAttribute replaceAttribute = new LdapAttribute("mail");
    replaceAttribute.addStringValues(Arrays.asList("test.subject.1"));
    
    LdapModificationItem item = new LdapModificationItem(LdapModificationType.REPLACE_ATTRIBUTE, replaceAttribute);
   
    List<LdapModificationItem> ldapModificationItems = new ArrayList<LdapModificationItem>();
    ldapModificationItems.add(item);
   
    LdapSyncDaoForLdap ldapSyncDaoForLdap = new LdapSyncDaoForLdap();
    LdapModificationResult result = ldapSyncDaoForLdap.modify("personLdap", "uid=aanderson,ou=People,dc=example,dc=edu", ldapModificationItems);
    
    replaceAttribute = new LdapAttribute("mail");
    replaceAttribute.addStringValues(Arrays.asList("test.subject.0"));
    
    item = new LdapModificationItem(LdapModificationType.REPLACE_ATTRIBUTE, replaceAttribute);
   
    ldapModificationItems = new ArrayList<LdapModificationItem>();
    ldapModificationItems.add(item);
   
    ldapSyncDaoForLdap = new LdapSyncDaoForLdap();
    result = ldapSyncDaoForLdap.modify("personLdap", "uid=aanderson727,ou=People,dc=example,dc=edu", ldapModificationItems);
    
    
    Stem stem = new StemSave(this.grouperSession).assignName("test").save();
    Stem stem2 = new StemSave(this.grouperSession).assignName("test2").save();
    
    // mark some folders to provision
    Group testGroup = new GroupSave(this.grouperSession).assignName("test:testGroup").assignDescription("oldDescription").save();
    Group testGroup2 = new GroupSave(this.grouperSession).assignName("test2:testGroup2").save();
    
    testGroup.addMember(SubjectTestHelper.SUBJ0, false);
    testGroup.addMember(SubjectTestHelper.SUBJ1, false);
    testGroup.addMember(SubjectTestHelper.SUBJ2, false);
    
    testGroup2.addMember(SubjectTestHelper.SUBJ2, false);
    testGroup2.addMember(SubjectTestHelper.SUBJ3, false);
    
    final GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision("sqlProvTest");
    attributeValue.setTargetName("sqlProvTest");
    attributeValue.setStemScopeString("sub");

    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);

    //AttributeAssign attributeAssign = stem.getAttributeDelegate().addAttribute(GrouperProvisioningAttributeNames.retrieveAttributeDefNameMarker()).getAttributeAssign();
    //attributeAssign.getAttributeValueDelegate().assignValueString(GrouperProvisioningAttributeNames.retrieveAttributeDefNameDoProvision())
    
    //lets sync these over
    GrouperProvisioningOutput grouperProvisioningOutput = fullProvision();
    GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
    
    List<Object[]> groups = new GcDbAccess().sql("select uuid, posix_id, name, description from testgrouper_prov_group").selectList(Object[].class);
    assertEquals(1, groups.size());
    
    String testGroupUuidInTarget = groups.get(0)[0].toString();
    assertNotNull(testGroupUuidInTarget);
    
    assertEquals(testGroup.getIdIndex().longValue(), ((BigDecimal)groups.get(0)[1]).longValue());
    assertEquals(testGroup.getName(), groups.get(0)[2]);
    assertEquals(testGroup.getDescription(), groups.get(0)[3]);
    
    List<Object[]> entities = new GcDbAccess().sql("select uuid, name, subject_id_or_identifier, description, school from testgrouper_prov_entity").selectList(Object[].class);
    assertEquals(3, entities.size());
    
    String subject0EntityUUID = null;
    String subject1EntityUUID = null;
    String subject2EntityUUID = null;
    
    Map<String, Object[]> entityNameToAllAttributes = new HashMap<String, Object[]>();
    for (Object[] entityAttributes: entities) {
      entityNameToAllAttributes.put(entityAttributes[1].toString(), entityAttributes);
      if (StringUtils.equals(entityAttributes[1].toString(), SubjectTestHelper.SUBJ0.getName())) {
        subject0EntityUUID = entityAttributes[0].toString();
      }
      if (StringUtils.equals(entityAttributes[1].toString(), SubjectTestHelper.SUBJ1.getName())) {
        subject1EntityUUID = entityAttributes[0].toString();
      }
      if (StringUtils.equals(entityAttributes[1].toString(), SubjectTestHelper.SUBJ2.getName())) {
        subject2EntityUUID = entityAttributes[0].toString();
      }
    }
    
    assertTrue(entityNameToAllAttributes.containsKey(SubjectTestHelper.SUBJ0.getName()));
    assertTrue(entityNameToAllAttributes.containsKey(SubjectTestHelper.SUBJ1.getName()));
    assertTrue(entityNameToAllAttributes.containsKey(SubjectTestHelper.SUBJ2.getName()));
    
    String schoolForSubject0 = new GcDbAccess().sql("select school from testgrouper_prov_entity where uuid = '"+subject0EntityUUID+"'").select(String.class);
    assertNotNull(schoolForSubject0);
    
    String schoolForSubject1 = new GcDbAccess().sql("select school from testgrouper_prov_entity where uuid = '"+subject1EntityUUID+"'").select(String.class);
    assertNotNull(schoolForSubject1);
    
    // for subject2EntityUUID, school is going to be null because subject source for that entity in the entity attributes table is not jdbc
    String schoolForSubject2 = new GcDbAccess().sql("select school from testgrouper_prov_entity where uuid = '"+subject2EntityUUID+"'").select(String.class);
    assertNull(schoolForSubject2);
    
    List<Object[]> memberships = new GcDbAccess().sql("select group_uuid, entity_uuid from testgrouper_prov_mship2").selectList(Object[].class);
    
    Set<MultiKey> groupIdSubjectIdsInTable = new HashSet<MultiKey>();
    
    for (Object[] membershipAttributes: memberships) {
      groupIdSubjectIdsInTable.add(new MultiKey(membershipAttributes[0], membershipAttributes[1]));
    }
    
    assertEquals(3, groupIdSubjectIdsInTable.size());
    
    assertTrue(groupIdSubjectIdsInTable.contains(new MultiKey(testGroupUuidInTarget, subject0EntityUUID)));
    assertTrue(groupIdSubjectIdsInTable.contains(new MultiKey(testGroupUuidInTarget, subject1EntityUUID)));
    assertTrue(groupIdSubjectIdsInTable.contains(new MultiKey(testGroupUuidInTarget, subject2EntityUUID)));
    
    // delete two members from testGroup and reprovision
    testGroup.deleteMember(SubjectTestHelper.SUBJ1);
    testGroup.deleteMember(SubjectTestHelper.SUBJ2);
    
    grouperProvisioningOutput = fullProvision();
    grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
    
    entities = new GcDbAccess().sql("select uuid, name, subject_id_or_identifier, description from testgrouper_prov_entity").selectList(Object[].class);
    assertEquals(1, entities.size());
    
    assertEquals(subject0EntityUUID, entities.get(0)[0].toString());
//    subject0EntityUUID = entities.get(0)[0].toString();
    
    assertEquals(SubjectTestHelper.SUBJ0.getName(), entities.get(0)[1].toString());

    memberships = new GcDbAccess().sql("select group_uuid, entity_uuid from testgrouper_prov_mship2").selectList(Object[].class);
    
    groupIdSubjectIdsInTable = new HashSet<MultiKey>();
    
    for (Object[] groupIdSubjectId: memberships) {
      groupIdSubjectIdsInTable.add(new MultiKey(groupIdSubjectId[0], groupIdSubjectId[1]));
    }
    
    assertEquals(1, groupIdSubjectIdsInTable.size());
    assertTrue(groupIdSubjectIdsInTable.contains(new MultiKey(testGroupUuidInTarget, subject0EntityUUID)));
    
    //update group and reprovision
    testGroup = new GroupSave(this.grouperSession).assignName(testGroup.getName())
        .assignUuid(testGroup.getUuid()).assignDescription("newDescription")
        .assignSaveMode(SaveMode.UPDATE).save();
    
    grouperProvisioningOutput = fullProvision();
    grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
    
    groups = new GcDbAccess().sql("select uuid, posix_id, name, description from testgrouper_prov_group").selectList(Object[].class);
    assertEquals(1, groups.size());
    
    assertEquals(testGroupUuidInTarget, groups.get(0)[0].toString());
    
    assertEquals(testGroup.getIdIndex().longValue(), ((BigDecimal)groups.get(0)[1]).longValue());
    assertEquals(testGroup.getName(), groups.get(0)[2]);
    assertEquals(testGroup.getDescription(), groups.get(0)[3]);
    
    // now delete the group
    testGroup.delete();
    
    grouperProvisioningOutput = fullProvision();
    grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
    
    groups = new GcDbAccess().sql("select uuid, posix_id, name, description from testgrouper_prov_group").selectList(Object[].class);
    assertEquals(0, groups.size());
    
    entities = new GcDbAccess().sql("select uuid, name, subject_id_or_identifier from testgrouper_prov_entity").selectList(Object[].class);
    assertEquals(0, entities.size());
    
    memberships = new GcDbAccess().sql("select group_uuid, entity_uuid from testgrouper_prov_mship2").selectList(Object[].class);
    assertEquals(0, memberships.size());
  }
  
  /**
   * just do a simple full sync of groups and memberships
   */
  public void testSimpleGroupMembershipProvisioningFull_1_LocalResolvers() {
    
    stopAndStartLdapContainer();
    
    SqlProvisionerTestUtils.configureSqlProvisioner(new SqlProvisionerTestConfigInput()
        .assignEntityDeleteType("deleteEntitiesIfNotExistInGrouper")
        .assignGroupDeleteType("deleteGroupsIfNotExistInGrouper")
        .assignMembershipDeleteType("deleteMembershipsIfNotExistInGrouper")
        .assignEntityResolverLocal(true)
        .assignGroupTableName("testgrouper_prov_group")
        .assignGroupTableIdColumn("uuid")
        .assignEntityTableName("testgrouper_prov_entity")
        .assignEntityTableIdColumn("uuid")
        .assignMembershipTableName("testgrouper_prov_mship2")
        .assignMembershipTableIdColumn("uuid")
        .assignMembershipGroupForeignKeyColumn("group_uuid")
        .assignMembershipEntityForeignKeyColumn("entity_uuid")
        .assignHasTargetEntityLink(true)
        .assignEntityAttributeCount(6)
        .assignGroupAttributeCount(4)
        .assignMembershipAttributeCount(3)
        );
    
    LdapAttribute replaceAttribute = new LdapAttribute("mail");
    replaceAttribute.addStringValues(Arrays.asList("test.subject.1"));
    
    LdapModificationItem item = new LdapModificationItem(LdapModificationType.REPLACE_ATTRIBUTE, replaceAttribute);
   
    List<LdapModificationItem> ldapModificationItems = new ArrayList<LdapModificationItem>();
    ldapModificationItems.add(item);
   
    LdapSyncDaoForLdap ldapSyncDaoForLdap = new LdapSyncDaoForLdap();
    LdapModificationResult result = ldapSyncDaoForLdap.modify("personLdap", "uid=aanderson,ou=People,dc=example,dc=edu", ldapModificationItems);
    
    replaceAttribute = new LdapAttribute("mail");
    replaceAttribute.addStringValues(Arrays.asList("test.subject.0"));
    
    item = new LdapModificationItem(LdapModificationType.REPLACE_ATTRIBUTE, replaceAttribute);
   
    ldapModificationItems = new ArrayList<LdapModificationItem>();
    ldapModificationItems.add(item);
   
    ldapSyncDaoForLdap = new LdapSyncDaoForLdap();
    result = ldapSyncDaoForLdap.modify("personLdap", "uid=aanderson727,ou=People,dc=example,dc=edu", ldapModificationItems);
    
        
    Stem stem = new StemSave(this.grouperSession).assignName("test").save();
    Stem stem2 = new StemSave(this.grouperSession).assignName("test2").save();
    
    // mark some folders to provision
    Group testGroup = new GroupSave(this.grouperSession).assignName("test:testGroup").assignDescription("oldDescription").save();
    Group testGroup2 = new GroupSave(this.grouperSession).assignName("test2:testGroup2").save();
    
    testGroup.addMember(SubjectTestHelper.SUBJ0, false);
    testGroup.addMember(SubjectTestHelper.SUBJ1, false);
    
    testGroup2.addMember(SubjectTestHelper.SUBJ2, false);
    testGroup2.addMember(SubjectTestHelper.SUBJ3, false);
    
    final GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision("sqlProvTest");
    attributeValue.setTargetName("sqlProvTest");
    attributeValue.setStemScopeString("sub");

    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);

    //AttributeAssign attributeAssign = stem.getAttributeDelegate().addAttribute(GrouperProvisioningAttributeNames.retrieveAttributeDefNameMarker()).getAttributeAssign();
    //attributeAssign.getAttributeValueDelegate().assignValueString(GrouperProvisioningAttributeNames.retrieveAttributeDefNameDoProvision())
    
    //lets sync these over
    GrouperProvisioningOutput grouperProvisioningOutput = fullProvision();
    GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
    
    List<Object[]> groups = new GcDbAccess().sql("select uuid, posix_id, name, description from testgrouper_prov_group").selectList(Object[].class);
    assertEquals(1, groups.size());
    
    String testGroupUuidInTarget = groups.get(0)[0].toString();
    assertNotNull(testGroupUuidInTarget);
    
    assertEquals(testGroup.getIdIndex().longValue(), ((BigDecimal)groups.get(0)[1]).longValue());
    assertEquals(testGroup.getName(), groups.get(0)[2]);
    assertEquals(testGroup.getDescription(), groups.get(0)[3]);
    
    List<Object[]> entities = new GcDbAccess().sql("select uuid, name, subject_id_or_identifier, description from testgrouper_prov_entity").selectList(Object[].class);
    assertEquals(2, entities.size());
    
    String subject0EntityUUID = null;
    String subject1EntityUUID2 = null;
    
    Map<String, Object[]> entityNameToAllAttributes = new HashMap<String, Object[]>();
    for (Object[] entityAttributes: entities) {
      entityNameToAllAttributes.put(entityAttributes[1].toString(), entityAttributes);
      if (StringUtils.equals(entityAttributes[1].toString(), SubjectTestHelper.SUBJ0.getName())) {
        subject0EntityUUID = entityAttributes[0].toString();
      }
      if (StringUtils.equals(entityAttributes[1].toString(), SubjectTestHelper.SUBJ1.getName())) {
        subject1EntityUUID2 = entityAttributes[0].toString();
      }
    }
    
    assertTrue(entityNameToAllAttributes.containsKey(SubjectTestHelper.SUBJ0.getName()));
    assertTrue(entityNameToAllAttributes.containsKey(SubjectTestHelper.SUBJ1.getName()));
    
    List<Object[]> memberships = new GcDbAccess().sql("select group_uuid, entity_uuid from testgrouper_prov_mship2").selectList(Object[].class);
    
    Set<MultiKey> groupIdSubjectIdsInTable = new HashSet<MultiKey>();
    
    for (Object[] membershipAttributes: memberships) {
      groupIdSubjectIdsInTable.add(new MultiKey(membershipAttributes[0], membershipAttributes[1]));
    }
    
    assertEquals(2, groupIdSubjectIdsInTable.size());
    
    assertTrue(groupIdSubjectIdsInTable.contains(new MultiKey(testGroupUuidInTarget, subject0EntityUUID)));
    assertTrue(groupIdSubjectIdsInTable.contains(new MultiKey(testGroupUuidInTarget, subject1EntityUUID2)));
    
    // delete a member from testGroup and reprovision
    testGroup.deleteMember(SubjectTestHelper.SUBJ1);
    
    grouperProvisioningOutput = fullProvision();
    grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
    
    entities = new GcDbAccess().sql("select uuid, name, subject_id_or_identifier, description from testgrouper_prov_entity").selectList(Object[].class);
    assertEquals(1, entities.size());
    
    assertEquals(subject0EntityUUID, entities.get(0)[0].toString());
//    subject0EntityUUID = entities.get(0)[0].toString();
    
    assertEquals(SubjectTestHelper.SUBJ0.getName(), entities.get(0)[1].toString());

    memberships = new GcDbAccess().sql("select group_uuid, entity_uuid from testgrouper_prov_mship2").selectList(Object[].class);
    
    groupIdSubjectIdsInTable = new HashSet<MultiKey>();
    
    for (Object[] groupIdSubjectId: memberships) {
      groupIdSubjectIdsInTable.add(new MultiKey(groupIdSubjectId[0], groupIdSubjectId[1]));
    }
    
    assertEquals(1, groupIdSubjectIdsInTable.size());
    assertTrue(groupIdSubjectIdsInTable.contains(new MultiKey(testGroupUuidInTarget, subject0EntityUUID)));
    
    //update group and reprovision
    testGroup = new GroupSave(this.grouperSession).assignName(testGroup.getName())
        .assignUuid(testGroup.getUuid()).assignDescription("newDescription")
        .assignSaveMode(SaveMode.UPDATE).save();
    
    grouperProvisioningOutput = fullProvision();
    grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
    
    groups = new GcDbAccess().sql("select uuid, posix_id, name, description from testgrouper_prov_group").selectList(Object[].class);
    assertEquals(1, groups.size());
    
    assertEquals(testGroupUuidInTarget, groups.get(0)[0].toString());
    
    assertEquals(testGroup.getIdIndex().longValue(), ((BigDecimal)groups.get(0)[1]).longValue());
    assertEquals(testGroup.getName(), groups.get(0)[2]);
    assertEquals(testGroup.getDescription(), groups.get(0)[3]);
    
    // now delete the group
    testGroup.delete();
    
    grouperProvisioningOutput = fullProvision();
    grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
    
    groups = new GcDbAccess().sql("select uuid, posix_id, name, description from testgrouper_prov_group").selectList(Object[].class);
    assertEquals(0, groups.size());
    
    entities = new GcDbAccess().sql("select uuid, name, subject_id_or_identifier from testgrouper_prov_entity").selectList(Object[].class);
    assertEquals(0, entities.size());
    
    memberships = new GcDbAccess().sql("select group_uuid, entity_uuid from testgrouper_prov_mship2").selectList(Object[].class);
    assertEquals(0, memberships.size());
  }
  
  public void testSimpleGroupMembershipProvisioningFullWithAttributesTable() {
        
    SqlProvisionerTestUtils.configureSqlProvisioner(new SqlProvisionerTestConfigInput()
        .assignEntityDeleteType("deleteEntitiesIfNotExistInGrouper")
        .assignGroupDeleteType("deleteGroupsIfNotExistInGrouper")
        .assignMembershipDeleteType("deleteMembershipsIfNotExistInGrouper")
        .assignEntityAttributesTable(true)
        .assignGroupAttributesTable(true)
        .assignGroupTableName("testgrouper_prov_group")
        .assignGroupTableIdColumn("uuid")
        .assignEntityTableName("testgrouper_prov_entity")
        .assignEntityTableIdColumn("uuid")
        .assignMembershipTableName("testgrouper_prov_mship2")
        .assignMembershipTableIdColumn("uuid")
        .assignMembershipGroupForeignKeyColumn("group_uuid")
        .assignMembershipEntityForeignKeyColumn("entity_uuid")
        .assignHasTargetEntityLink(true)
        .assignHasTargetGroupLink(true)
        .assignEntityAttributeCount(5)
        .assignGroupAttributeCount(4)
        .assignMembershipAttributeCount(3)
        );

    Stem stem = new StemSave(this.grouperSession).assignName("test").save();
    Stem stem2 = new StemSave(this.grouperSession).assignName("test2").save();
    
    // mark some folders to provision
    Group testGroup = new GroupSave(this.grouperSession).assignName("test:testGroup").assignDescription("testDescription1").save();
    Group testGroup2 = new GroupSave(this.grouperSession).assignName("test2:testGroup2").assignDescription("testDescription2").save();
    
    testGroup.addMember(SubjectTestHelper.SUBJ0, false);
    testGroup.addMember(SubjectTestHelper.SUBJ1, false);
    
    testGroup2.addMember(SubjectTestHelper.SUBJ2, false);
    testGroup2.addMember(SubjectTestHelper.SUBJ3, false);
    
    final GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision("sqlProvTest");
    attributeValue.setTargetName("sqlProvTest");
    attributeValue.setStemScopeString("sub");

    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);

    //AttributeAssign attributeAssign = stem.getAttributeDelegate().addAttribute(GrouperProvisioningAttributeNames.retrieveAttributeDefNameMarker()).getAttributeAssign();
    //attributeAssign.getAttributeValueDelegate().assignValueString(GrouperProvisioningAttributeNames.retrieveAttributeDefNameDoProvision())
    
    //lets sync these over
    GrouperProvisioningOutput grouperProvisioningOutput = fullProvision();
    GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
    
    List<Object[]> groups = new GcDbAccess().sql("select uuid, posix_id, name from testgrouper_prov_group").selectList(Object[].class);
    assertEquals(1, groups.size());
    
    String testGroupUuidInTarget = groups.get(0)[0].toString();
    assertNotNull(testGroupUuidInTarget);
    
    assertEquals(testGroup.getIdIndex().longValue(), ((BigDecimal)groups.get(0)[1]).longValue());
    assertEquals(testGroup.getName(), groups.get(0)[2]);
    
    List<Object[]> groupAttributes = new GcDbAccess().sql("select attribute_name, attribute_value from testgrouper_pro_ldap_group_attr where group_uuid = '"+testGroupUuidInTarget+"'").selectList(Object[].class);
    assertEquals(1, groupAttributes.size());
    
    assertEquals("description", groupAttributes.get(0)[0]);
    assertEquals(testGroup.getDescription(), groupAttributes.get(0)[1]);
    
    List<Object[]> entities = new GcDbAccess().sql("select uuid, name, subject_id_or_identifier, description from testgrouper_prov_entity").selectList(Object[].class);
    assertEquals(2, entities.size());
    
    String subject0EntityUUID = null;
    String subject1EntityUUID2 = null;
    
    Map<String, Object[]> entityNameToAllAttributes = new HashMap<String, Object[]>();
    for (Object[] entityAttributes: entities) {
      entityNameToAllAttributes.put(entityAttributes[1].toString(), entityAttributes);
      if (StringUtils.equals(entityAttributes[1].toString(), SubjectTestHelper.SUBJ0.getName())) {
        subject0EntityUUID = entityAttributes[0].toString();
      }
      if (StringUtils.equals(entityAttributes[1].toString(), SubjectTestHelper.SUBJ1.getName())) {
        subject1EntityUUID2 = entityAttributes[0].toString();
      }
    }
    
    assertTrue(entityNameToAllAttributes.containsKey(SubjectTestHelper.SUBJ0.getName()));
    assertTrue(entityNameToAllAttributes.containsKey(SubjectTestHelper.SUBJ1.getName()));
    
    List<Object[]> memberships = new GcDbAccess().sql("select group_uuid, entity_uuid from testgrouper_prov_mship2").selectList(Object[].class);
    
    Set<MultiKey> groupIdSubjectIdsInTable = new HashSet<MultiKey>();
    
    for (Object[] membershipAttributes: memberships) {
      groupIdSubjectIdsInTable.add(new MultiKey(membershipAttributes[0], membershipAttributes[1]));
    }
    
    assertEquals(2, groupIdSubjectIdsInTable.size());
    
    assertTrue(groupIdSubjectIdsInTable.contains(new MultiKey(testGroupUuidInTarget, subject0EntityUUID)));
    assertTrue(groupIdSubjectIdsInTable.contains(new MultiKey(testGroupUuidInTarget, subject1EntityUUID2)));
    
    // delete a member from testGroup and reprovision
    testGroup.deleteMember(SubjectTestHelper.SUBJ1);
    
    grouperProvisioningOutput = fullProvision();
    grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());

    memberships = new GcDbAccess().sql("select group_uuid, entity_uuid from testgrouper_prov_mship2").selectList(Object[].class);
    
    groupIdSubjectIdsInTable = new HashSet<MultiKey>();
    
    for (Object[] groupIdSubjectId: memberships) {
      groupIdSubjectIdsInTable.add(new MultiKey(groupIdSubjectId[0], groupIdSubjectId[1]));
    }
    
    assertEquals(1, groupIdSubjectIdsInTable.size());
    
    //update group description and reprovision
    testGroup = new GroupSave(this.grouperSession).assignName(testGroup.getName())
        .assignUuid(testGroup.getUuid()).assignDescription("newDescription")
        .assignSaveMode(SaveMode.UPDATE).save();
    
    grouperProvisioningOutput = fullProvision();
    grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
    
    groups = new GcDbAccess().sql("select uuid, posix_id, name from testgrouper_prov_group").selectList(Object[].class);
    assertEquals(1, groups.size());
    
    assertEquals(testGroupUuidInTarget, groups.get(0)[0].toString());
    
    assertEquals(testGroup.getIdIndex().longValue(), ((BigDecimal)groups.get(0)[1]).longValue());
    assertEquals(testGroup.getName(), groups.get(0)[2]);
    
    groupAttributes = new GcDbAccess().sql("select attribute_name, attribute_value from testgrouper_pro_ldap_group_attr where group_uuid = '"+testGroupUuidInTarget+"'").selectList(Object[].class);
    assertEquals(1, groupAttributes.size());
    
    assertEquals("description", groupAttributes.get(0)[0]);
    assertEquals(testGroup.getDescription(), groupAttributes.get(0)[1]);
    
    
    //update entity description and reprovision
    new GcDbAccess().sql("update subjectattribute set value = 'newTestDescription' where subjectid = 'test.subject.0' and name = 'description' ").executeSql();
    GrouperCacheUtils.clearAllCaches();
    SubjectFinder.findById("test.subject.0", true);
    grouperProvisioningOutput = fullProvision();
    grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
    
    String testSubject0Id = new GcDbAccess().sql("select uuid from testgrouper_prov_entity where subject_id_or_identifier = 'test.subject.0' ").select(String.class);

    List<Object[]> entityAttributes = new GcDbAccess().sql("select entity_attribute_name, entity_attribute_value from testgrouper_pro_dap_entity_attr where entity_uuid = '"+testSubject0Id+"'").selectList(Object[].class);
    assertEquals(1, entityAttributes.size());
    
    assertEquals("description", entityAttributes.get(0)[0]);
    assertEquals("newTestDescription", entityAttributes.get(0)[1]);
    
    // now delete the group
    testGroup.delete();
    
    grouperProvisioningOutput = fullProvision();
    grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
    
    groups = new GcDbAccess().sql("select uuid, posix_id, name, description from testgrouper_prov_group").selectList(Object[].class);
    assertEquals(0, groups.size());
    
    groupAttributes = new GcDbAccess().sql("select attribute_name, attribute_value from testgrouper_pro_ldap_group_attr where group_uuid = '"+testGroupUuidInTarget+"'").selectList(Object[].class);
    assertEquals(0, groupAttributes.size());
    
    entities = new GcDbAccess().sql("select uuid, name, subject_id_or_identifier from testgrouper_prov_entity").selectList(Object[].class);
    assertEquals(0, entities.size());
    
    memberships = new GcDbAccess().sql("select group_uuid, entity_uuid from testgrouper_prov_mship2").selectList(Object[].class);
    assertEquals(0, memberships.size());
    
  }

  public void testGroupEntityMembershipRenameGroupFullMatchOnOld() {
    
    SqlProvisionerTestUtils.configureSqlProvisioner(new SqlProvisionerTestConfigInput()
        .assignEntityDeleteType("deleteEntitiesIfNotExistInGrouper")
        .assignGroupDeleteType("deleteGroupsIfNotExistInGrouper")
        .assignMembershipDeleteType("deleteMembershipsIfNotExistInGrouper")
        .assignEntityAttributesTable(true)
        .assignGroupAttributesTable(true)
        .assignGroupTableName("testgrouper_prov_group")
        .assignGroupTableIdColumn("uuid")
        .assignEntityTableName("testgrouper_prov_entity")
        .assignEntityTableIdColumn("uuid")
        .assignMembershipTableName("testgrouper_prov_mship2")
        .assignMembershipTableIdColumn("uuid")
        .assignMembershipGroupForeignKeyColumn("group_uuid")
        .assignMembershipEntityForeignKeyColumn("entity_uuid")
        .assignHasTargetEntityLink(true)
        .assignHasTargetGroupLink(true)
  // TODO      .assignCacheObjects(true)
        .assignEntityAttributeCount(5)
        .assignGroupAttributeCount(4)
        .assignMembershipAttributeCount(3)
        .addExtraConfig("targetGroupAttribute.2.translateFromGrouperProvisioningGroupField", "displayName")
        .addExtraConfig("groupMatchingAttributeSameAsSearchAttribute", "false")
        .addExtraConfig("groupMatchingAttribute0name", "name")
        );

    Stem stem = new StemSave(this.grouperSession).assignName("test").save();
    Stem stem2 = new StemSave(this.grouperSession).assignName("test2").save();
    
    // mark some folders to provision
    Group testGroup = new GroupSave(this.grouperSession).assignName("test:testGroup").assignDescription("testDescription1").save();
    Group testGroup2 = new GroupSave(this.grouperSession).assignName("test2:testGroup2").assignDescription("testDescription2").save();
    
    testGroup.addMember(SubjectTestHelper.SUBJ0, false);
    testGroup.addMember(SubjectTestHelper.SUBJ1, false);
    
    testGroup2.addMember(SubjectTestHelper.SUBJ2, false);
    testGroup2.addMember(SubjectTestHelper.SUBJ3, false);
    
    final GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision("sqlProvTest");
    attributeValue.setTargetName("sqlProvTest");
    attributeValue.setStemScopeString("sub");

    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);

    //AttributeAssign attributeAssign = stem.getAttributeDelegate().addAttribute(GrouperProvisioningAttributeNames.retrieveAttributeDefNameMarker()).getAttributeAssign();
    //attributeAssign.getAttributeValueDelegate().assignValueString(GrouperProvisioningAttributeNames.retrieveAttributeDefNameDoProvision())
    
    //lets sync these over
    GrouperProvisioningOutput grouperProvisioningOutput = fullProvision();
    GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
    
    List<Object[]> groups = new GcDbAccess().sql("select uuid, posix_id, name from testgrouper_prov_group").selectList(Object[].class);
    assertEquals(1, groups.size());
    
    String testGroupUuidInTarget = groups.get(0)[0].toString();
    assertNotNull(testGroupUuidInTarget);
    
    assertEquals(testGroup.getIdIndex().longValue(), ((BigDecimal)groups.get(0)[1]).longValue());
    assertEquals(testGroup.getName(), groups.get(0)[2]);
    
    List<Object[]> groupAttributes = new GcDbAccess().sql("select attribute_name, attribute_value from testgrouper_pro_ldap_group_attr where group_uuid = '"+testGroupUuidInTarget+"'").selectList(Object[].class);
    assertEquals(1, groupAttributes.size());
    
    assertEquals("description", groupAttributes.get(0)[0]);
    assertEquals(testGroup.getDescription(), groupAttributes.get(0)[1]);
    
    List<Object[]> entities = new GcDbAccess().sql("select uuid, name, subject_id_or_identifier, description from testgrouper_prov_entity").selectList(Object[].class);
    assertEquals(2, entities.size());
    
    String subject0EntityUUID = null;
    String subject1EntityUUID2 = null;
    
    Map<String, Object[]> entityNameToAllAttributes = new HashMap<String, Object[]>();
    for (Object[] entityAttributes: entities) {
      entityNameToAllAttributes.put(entityAttributes[1].toString(), entityAttributes);
      if (StringUtils.equals(entityAttributes[1].toString(), SubjectTestHelper.SUBJ0.getName())) {
        subject0EntityUUID = entityAttributes[0].toString();
      }
      if (StringUtils.equals(entityAttributes[1].toString(), SubjectTestHelper.SUBJ1.getName())) {
        subject1EntityUUID2 = entityAttributes[0].toString();
      }
    }
    
    assertTrue(entityNameToAllAttributes.containsKey(SubjectTestHelper.SUBJ0.getName()));
    assertTrue(entityNameToAllAttributes.containsKey(SubjectTestHelper.SUBJ1.getName()));
    
    List<Object[]> memberships = new GcDbAccess().sql("select group_uuid, entity_uuid from testgrouper_prov_mship2").selectList(Object[].class);
    
    Set<MultiKey> groupIdSubjectIdsInTable = new HashSet<MultiKey>();
    
    for (Object[] membershipAttributes: memberships) {
      groupIdSubjectIdsInTable.add(new MultiKey(membershipAttributes[0], membershipAttributes[1]));
    }
    
    assertEquals(2, groupIdSubjectIdsInTable.size());
    
    assertTrue(groupIdSubjectIdsInTable.contains(new MultiKey(testGroupUuidInTarget, subject0EntityUUID)));
    assertTrue(groupIdSubjectIdsInTable.contains(new MultiKey(testGroupUuidInTarget, subject1EntityUUID2)));
    
    // rename the group in grouper
    testGroup = new GroupSave().assignUuid(testGroup.getUuid()).assignDisplayName(testGroup.getDisplayName() + "New").assignReplaceAllSettings(false).save();
    
    grouperProvisioningOutput = fullProvision();
    grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());

    groups = new GcDbAccess().sql("select uuid, posix_id, name from testgrouper_prov_group").selectList(Object[].class);
    assertEquals(1, groups.size());
    
    testGroupUuidInTarget = groups.get(0)[0].toString();
    assertNotNull(testGroupUuidInTarget);
    
    assertEquals(testGroup.getIdIndex().longValue(), ((BigDecimal)groups.get(0)[1]).longValue());
    assertEquals(testGroup.getDisplayName(), groups.get(0)[2]);
    
  }

  public void testGroupEntityMembershipRenameGroupIncrementalMatchOnOld() {
    
    SqlProvisionerTestUtils.configureSqlProvisioner(new SqlProvisionerTestConfigInput()
        .assignEntityDeleteType("deleteEntitiesIfNotExistInGrouper")
        .assignGroupDeleteType("deleteGroupsIfNotExistInGrouper")
        .assignMembershipDeleteType("deleteMembershipsIfNotExistInGrouper")
        .assignEntityAttributesTable(true)
        .assignGroupAttributesTable(true)
        .assignGroupTableName("testgrouper_prov_group")
        .assignGroupTableIdColumn("uuid")
        .assignEntityTableName("testgrouper_prov_entity")
        .assignEntityTableIdColumn("uuid")
        .assignMembershipTableName("testgrouper_prov_mship2")
        .assignMembershipTableIdColumn("uuid")
        .assignMembershipGroupForeignKeyColumn("group_uuid")
        .assignMembershipEntityForeignKeyColumn("entity_uuid")
        .assignHasTargetEntityLink(true)
        .assignHasTargetGroupLink(true)
        .assignEntityAttributeCount(5)
        .assignGroupAttributeCount(4)
        .assignMembershipAttributeCount(3)
        .addExtraConfig("targetGroupAttribute.2.translateFromGrouperProvisioningGroupField", "displayName")
        .addExtraConfig("groupMatchingAttributeSameAsSearchAttribute", "false")
        .addExtraConfig("groupMatchingAttribute0name", "name")
        .addExtraConfig("recalculateAllOperations", "true")
        );

    Stem stem = new StemSave(this.grouperSession).assignName("test").save();
    Stem stem2 = new StemSave(this.grouperSession).assignName("test2").save();
    
    // mark some folders to provision
    Group testGroup = new GroupSave(this.grouperSession).assignName("test:testGroup").assignDescription("testDescription1").save();
    Group testGroup2 = new GroupSave(this.grouperSession).assignName("test2:testGroup2").assignDescription("testDescription2").save();
    
    testGroup.addMember(SubjectTestHelper.SUBJ0, false);
    testGroup.addMember(SubjectTestHelper.SUBJ1, false);
    
    testGroup2.addMember(SubjectTestHelper.SUBJ2, false);
    testGroup2.addMember(SubjectTestHelper.SUBJ3, false);
    
    final GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision("sqlProvTest");
    attributeValue.setTargetName("sqlProvTest");
    attributeValue.setStemScopeString("sub");

    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);

    //AttributeAssign attributeAssign = stem.getAttributeDelegate().addAttribute(GrouperProvisioningAttributeNames.retrieveAttributeDefNameMarker()).getAttributeAssign();
    //attributeAssign.getAttributeValueDelegate().assignValueString(GrouperProvisioningAttributeNames.retrieveAttributeDefNameDoProvision())
    
    //lets sync these over
    GrouperProvisioningOutput grouperProvisioningOutput = fullProvision();
    GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
    
    incrementalProvision();

    List<Object[]> groups = new GcDbAccess().sql("select uuid, posix_id, name from testgrouper_prov_group").selectList(Object[].class);
    assertEquals(1, groups.size());
    
    String testGroupUuidInTarget = groups.get(0)[0].toString();
    assertNotNull(testGroupUuidInTarget);
    
    assertEquals(testGroup.getIdIndex().longValue(), ((BigDecimal)groups.get(0)[1]).longValue());
    assertEquals(testGroup.getName(), groups.get(0)[2]);
    
    List<Object[]> groupAttributes = new GcDbAccess().sql("select attribute_name, attribute_value from testgrouper_pro_ldap_group_attr where group_uuid = '"+testGroupUuidInTarget+"'").selectList(Object[].class);
    assertEquals(1, groupAttributes.size());
    
    assertEquals("description", groupAttributes.get(0)[0]);
    assertEquals(testGroup.getDescription(), groupAttributes.get(0)[1]);
    
    List<Object[]> entities = new GcDbAccess().sql("select uuid, name, subject_id_or_identifier, description from testgrouper_prov_entity").selectList(Object[].class);
    assertEquals(2, entities.size());
    
    String subject0EntityUUID = null;
    String subject1EntityUUID2 = null;
    
    Map<String, Object[]> entityNameToAllAttributes = new HashMap<String, Object[]>();
    for (Object[] entityAttributes: entities) {
      entityNameToAllAttributes.put(entityAttributes[1].toString(), entityAttributes);
      if (StringUtils.equals(entityAttributes[1].toString(), SubjectTestHelper.SUBJ0.getName())) {
        subject0EntityUUID = entityAttributes[0].toString();
      }
      if (StringUtils.equals(entityAttributes[1].toString(), SubjectTestHelper.SUBJ1.getName())) {
        subject1EntityUUID2 = entityAttributes[0].toString();
      }
    }
    
    assertTrue(entityNameToAllAttributes.containsKey(SubjectTestHelper.SUBJ0.getName()));
    assertTrue(entityNameToAllAttributes.containsKey(SubjectTestHelper.SUBJ1.getName()));
    
    List<Object[]> memberships = new GcDbAccess().sql("select group_uuid, entity_uuid from testgrouper_prov_mship2").selectList(Object[].class);
    
    Set<MultiKey> groupIdSubjectIdsInTable = new HashSet<MultiKey>();
    
    for (Object[] membershipAttributes: memberships) {
      groupIdSubjectIdsInTable.add(new MultiKey(membershipAttributes[0], membershipAttributes[1]));
    }
    
    assertEquals(2, groupIdSubjectIdsInTable.size());
    
    assertTrue(groupIdSubjectIdsInTable.contains(new MultiKey(testGroupUuidInTarget, subject0EntityUUID)));
    assertTrue(groupIdSubjectIdsInTable.contains(new MultiKey(testGroupUuidInTarget, subject1EntityUUID2)));
    
    // rename the group in grouper
    testGroup = new GroupSave().assignUuid(testGroup.getUuid()).assignDisplayName(testGroup.getDisplayName() + "New").assignReplaceAllSettings(false).save();
    
    incrementalProvision();
    grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
    
    assertEquals(1, GrouperUtil.intValue(grouperProvisioner.getDebugMap().get("retrieveGroupsFromCache"), -1));
    
    groups = new GcDbAccess().sql("select uuid, posix_id, name from testgrouper_prov_group").selectList(Object[].class);
    assertEquals(1, groups.size());
    
    testGroupUuidInTarget = groups.get(0)[0].toString();
    assertNotNull(testGroupUuidInTarget);
    
    assertEquals(testGroup.getIdIndex().longValue(), ((BigDecimal)groups.get(0)[1]).longValue());
    assertEquals(testGroup.getDisplayName(), groups.get(0)[2]);
    
  }

  /**
   * @param tableName
   */
  public static void dropTableSyncTable(final String tableName) {
    try {
      // if you cant connrc to it, its not there
      HibernateSession.bySqlStatic().select(Integer.class, "select count(1) from " + tableName);
    } catch (Exception e) {
      return;
    }
    try {
      HibernateSession.bySqlStatic().executeSql("drop table " + tableName);
    } catch (Exception e) {
      return;
    }
    try {
      // if you cant connrc to it, its not there
      HibernateSession.bySqlStatic().select(Integer.class, "select count(1) from " + tableName);
    } catch (Exception e) {
      return;
    }
    //we need to delete the test table if it is there, and create a new one
    //drop field id col, first drop foreign keys
    GrouperDdlUtils.changeDatabase(GrouperTestDdl.V1.getObjectName(), new DdlUtilsChangeDatabase() {
  
      public void changeDatabase(DdlVersionBean ddlVersionBean) {
        
        Database database = ddlVersionBean.getDatabase();
  
        {
          Table loaderTable = database.findTable(tableName);
          
          if (loaderTable != null) {
            database.removeTable(loaderTable);
          }
        }
                
      }
      
    });
  }

  /**
   * 
   */
  public void ensureTableSyncTables() {
    
    createTableLdapGroup();
    createTableLdapGroupAttr();
    createTableLdapEntity();
    createTableLdapEntityAttr();

    createTableGroup();
    
    createTableEntity();
    
    createTableMship0();
    
    createTableMship1();

    createTableMship2();
  }

  /**
   * @param ddlVersionBean
   * @param database
   */
  public void createTableGroup() {
  
    final String tableName = "testgrouper_prov_group";
  
    try {
      new GcDbAccess().sql("select count(*) from " + tableName).select(int.class);
    } catch (Exception e) {
      //we need to delete the test table if it is there, and create a new one
      //drop field id col, first drop foreign keys
      GrouperDdlUtils.changeDatabase(GrouperTestDdl.V1.getObjectName(), new DdlUtilsChangeDatabase() {
    
        public void changeDatabase(DdlVersionBean ddlVersionBean) {
          
          Database database = ddlVersionBean.getDatabase();
    
          
          Table loaderTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, tableName);
          
          GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "uuid", Types.VARCHAR, "1024", true, true);

          GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "posix_id", Types.BIGINT, "10", false, false);

          GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "name", Types.VARCHAR, "1024", false, false);

          GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "display_name", Types.VARCHAR, "1024", false, false);

          GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "description", Types.VARCHAR, "1024", false, false);
        }
        
      });
    }
  }
  
  public void stopAndStartLdapContainer() {
    
    LdapProvisionerTestUtils.stopAndRemoveLdapContainer();
    LdapProvisionerTestUtils.startLdapContainer();
    LdapProvisionerTestUtils.setupLdapExternalSystem();
    
  }
  
  /**
   * @param ddlVersionBean
   * @param database
   */
  public void createTableEntity() {
  
    final String tableName = "testgrouper_prov_entity";
  
    try {
      new GcDbAccess().sql("select count(*) from " + tableName).select(int.class);
    } catch (Exception e) {
      //we need to delete the test table if it is there, and create a new one
      //drop field id col, first drop foreign keys
      GrouperDdlUtils.changeDatabase(GrouperTestDdl.V1.getObjectName(), new DdlUtilsChangeDatabase() {
    
        public void changeDatabase(DdlVersionBean ddlVersionBean) {
          
          Database database = ddlVersionBean.getDatabase();
          
          Table loaderTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, tableName);
          
          GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "uuid", Types.VARCHAR, "40", true, true);

          GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "name", Types.VARCHAR, "1024", false, false);

          GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "subject_id_or_identifier", Types.VARCHAR, "1024", false, false);

          GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "email", Types.VARCHAR, "1024", false, false);

          GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "description", Types.VARCHAR, "1024", false, false);

          GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "school", Types.VARCHAR, "1024", false, false);

        }
        
      });
    }
    
    
    final String tableName1 = "testgrouper_prov_entity1";
    
    try {
      new GcDbAccess().sql("select count(*) from " + tableName1).select(int.class);
    } catch (Exception e) {
      //we need to delete the test table if it is there, and create a new one
      //drop field id col, first drop foreign keys
      GrouperDdlUtils.changeDatabase(GrouperTestDdl.V1.getObjectName(), new DdlUtilsChangeDatabase() {
    
        public void changeDatabase(DdlVersionBean ddlVersionBean) {
          
          Database database = ddlVersionBean.getDatabase();
          
          Table loaderTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, tableName1);
          
          GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "subject_id_or_identifier", Types.VARCHAR, "1024", false, false);

          GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "school", Types.VARCHAR, "1024", false, false);
          GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "subject_source_id", Types.VARCHAR, "1024", false, false);
          
        }
        
      });
    }
  }
  
  /**
   * @param ddlVersionBean
   * @param database
   */
  public void createTableLdapGroup() {

    final String tableName = "testgrouper_prov_ldap_group";

    try {
      new GcDbAccess().sql("select count(*) from " + tableName).select(int.class);
    } catch (Exception e) {
      //we need to delete the test table if it is there, and create a new one
      //drop field id col, first drop foreign keys
      GrouperDdlUtils.changeDatabase(GrouperTestDdl.V1.getObjectName(), new DdlUtilsChangeDatabase() {
    
        public void changeDatabase(DdlVersionBean ddlVersionBean) {
          
          Database database = ddlVersionBean.getDatabase();
    
          
          Table loaderTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, tableName);
          
          GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "uuid", Types.VARCHAR, "40", true, true);
        }
        
      });
    }
    
  }
  
  /**
   * @param ddlVersionBean
   * @param database
   */
  public void createTableLdapEntity() {
  
    String tableName = "testgrouper_prov_ldap_entity";
    
    try {
      new GcDbAccess().sql("select count(*) from " + tableName).select(int.class);
    } catch (Exception e) {
      //we need to delete the test table if it is there, and create a new one
      //drop field id col, first drop foreign keys
      GrouperDdlUtils.changeDatabase(GrouperTestDdl.V1.getObjectName(), new DdlUtilsChangeDatabase() {
    
        public void changeDatabase(DdlVersionBean ddlVersionBean) {
          
          Database database = ddlVersionBean.getDatabase();
    
          Table loaderTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, tableName);
          
          GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "entity_uuid", Types.VARCHAR, "40", true, true);
        }
        
      });
    }
    
  }
  
  /**
   * @param ddlVersionBean
   * @param database
   */
  public void createTableLdapGroupAttr() {
  
    String tableName = "testgrouper_pro_ldap_group_attr";
    
    try {
      new GcDbAccess().sql("select count(*) from " + tableName).select(int.class);
    } catch (Exception e) {
      //we need to delete the test table if it is there, and create a new one
      //drop field id col, first drop foreign keys
      GrouperDdlUtils.changeDatabase(GrouperTestDdl.V1.getObjectName(), new DdlUtilsChangeDatabase() {
    
        public void changeDatabase(DdlVersionBean ddlVersionBean) {
          
          Database database = ddlVersionBean.getDatabase();

          Table loaderTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, tableName);

          // no primary key
          
          GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "group_uuid", Types.VARCHAR, "40", false, false);

          GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "attribute_name", Types.VARCHAR, "200", false, false);

          GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "attribute_value", Types.VARCHAR, "200", false, false);

          GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "last_modified", Types.TIMESTAMP, "200", false, false);

        }
        
      });
      GrouperDdlUtils.changeDatabase(GrouperTestDdl.V1.getObjectName(), new DdlUtilsChangeDatabase() {
        
        public void changeDatabase(DdlVersionBean ddlVersionBean) {
          
          Database database = ddlVersionBean.getDatabase();

          GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, ddlVersionBean, tableName, "testgrouper_pro_ldap_gr_idx0", null, false, "group_uuid", "attribute_name");
          
          GrouperDdlUtils.ddlutilsFindOrCreateForeignKey(database, tableName, "testgrouper_pro_ldap_gr_fk", "testgrouper_prov_ldap_group", "group_uuid", "uuid");
        }
        
      });
    }
  }

  /**
   * @param ddlVersionBean
   * @param database
   */
  public void createTableMship1() {
  
    String tableName = "testgrouper_prov_mship1";
    
    try {
      new GcDbAccess().sql("select count(*) from " + tableName).select(int.class);
    } catch (Exception e) {
      //we need to delete the test table if it is there, and create a new one
      //drop field id col, first drop foreign keys
      GrouperDdlUtils.changeDatabase(GrouperTestDdl.V1.getObjectName(), new DdlUtilsChangeDatabase() {
    
        public void changeDatabase(DdlVersionBean ddlVersionBean) {
          
          Database database = ddlVersionBean.getDatabase();

          Table loaderTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, tableName);
          
          GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "uuid", Types.VARCHAR, "40", true, true);

          GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "group_uuid", Types.VARCHAR, "40", false, true);

          GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "subject_id", Types.VARCHAR, "1024", false, true);
          
        }
        
      });
    }
  }
  
  /**
   * @param ddlVersionBean
   * @param database
   */
  public void createTableMship2() {
  
    String tableName = "testgrouper_prov_mship2";
    
    try {
      new GcDbAccess().sql("select count(*) from " + tableName).select(int.class);
    } catch (Exception e) {
      //we need to delete the test table if it is there, and create a new one
      //drop field id col, first drop foreign keys
      GrouperDdlUtils.changeDatabase(GrouperTestDdl.V1.getObjectName(), new DdlUtilsChangeDatabase() {
    
        public void changeDatabase(DdlVersionBean ddlVersionBean) {
          
          Database database = ddlVersionBean.getDatabase();

          Table loaderTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, tableName);
          
          GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "uuid", Types.VARCHAR, "40", true, true);

          GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "group_uuid", Types.VARCHAR, "40", false, true);

          GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "entity_uuid", Types.VARCHAR, "1024", false, true);
          
        }
        
      });
    }
  }

  /**
   * @param ddlVersionBean
   * @param database
   */
  public void createTableMship0() {
  
    String tableName = "testgrouper_prov_mship0";
    
    try {
      new GcDbAccess().sql("select count(*) from " + tableName).select(int.class);
    } catch (Exception e) {
      //we need to delete the test table if it is there, and create a new one
      //drop field id col, first drop foreign keys
      GrouperDdlUtils.changeDatabase(GrouperTestDdl.V1.getObjectName(), new DdlUtilsChangeDatabase() {
    
        public void changeDatabase(DdlVersionBean ddlVersionBean) {
          
          Database database = ddlVersionBean.getDatabase();

          Table loaderTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, tableName);
          
          GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "group_name", Types.VARCHAR, "180", true, true);

          GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "subject_id", Types.VARCHAR, "70", true, true);
          
        }
        
      });
    }
  }

  /**
     * just do a simple full sync of groups and memberships
     */
    public void testSimpleGroupLdap() {
      
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
      );

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
      attributeValue.setDoProvision("sqlProvTest");
      attributeValue.setTargetName("sqlProvTest");
      attributeValue.setStemScopeString("sub");
  
      GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);
  
      //AttributeAssign attributeAssign = stem.getAttributeDelegate().addAttribute(GrouperProvisioningAttributeNames.retrieveAttributeDefNameMarker()).getAttributeAssign();
      //attributeAssign.getAttributeValueDelegate().assignValueString(GrouperProvisioningAttributeNames.retrieveAttributeDefNameDoProvision())
      
      //lets sync these over
      GrouperProvisioningOutput grouperProvisioningOutput = fullProvision();
      GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
      assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
      
      String sql = "select uuid from testgrouper_prov_ldap_group";
      
      List<String> stringsInTable = new GcDbAccess().sql(sql).selectList(String.class);
            
      assertEquals(1, stringsInTable.size());
      String uuid = stringsInTable.get(0);
      sql = "select group_uuid, attribute_name, attribute_value from testgrouper_pro_ldap_group_attr";
      
      List<Object[]> dataInTable = new GcDbAccess().sql(sql).selectList(Object[].class);
      
      Set<MultiKey> attributesInTable = new HashSet<MultiKey>();
      
      for (Object[] row: dataInTable) {
        attributesInTable.add(new MultiKey(row));
      }
      
      assertEquals(3, attributesInTable.size());
      assertTrue(attributesInTable.contains(new MultiKey(uuid, "name", "test:testGroup")));
      assertTrue(attributesInTable.contains(new MultiKey(uuid, "subjectId", "test.subject.0")));
      assertTrue(attributesInTable.contains(new MultiKey(uuid, "subjectId", "test.subject.1")));
    }

  /**
       * just do a simple full sync of groups and memberships
       */
      public void testSimpleGroupLdapInsertUpdateDeleteFullSync() {

        simpleGroupLdapConfigure();    
            
        Stem stem = new StemSave(this.grouperSession).assignName("test").save();
        Stem stem2 = new StemSave(this.grouperSession).assignName("test2").save();
        Stem stem3 = new StemSave(this.grouperSession).assignName("test3").save();
        Stem stem4 = new StemSave(this.grouperSession).assignName("test4").save();
        
        // mark some folders to provision
        Group testGroup = new GroupSave(this.grouperSession).assignName("test:testGroup").save();
        Group testGroup2 = new GroupSave(this.grouperSession).assignName("test2:testGroup2").save();
        Group testGroup3 = new GroupSave(this.grouperSession).assignName("test3:testGroup3").save();
        Group testGroup4 = new GroupSave(this.grouperSession).assignName("test4:testGroup4").save();
        
        testGroup.addMember(SubjectTestHelper.SUBJ0, false);
        testGroup.addMember(SubjectTestHelper.SUBJ1, false);

        testGroup2.addMember(SubjectTestHelper.SUBJ2, false);
        testGroup2.addMember(SubjectTestHelper.SUBJ3, false);

        testGroup3.addMember(SubjectTestHelper.SUBJ4, false);
        testGroup3.addMember(SubjectTestHelper.SUBJ5, false);

        testGroup4.addMember(SubjectTestHelper.SUBJ6, false);
        testGroup4.addMember(SubjectTestHelper.SUBJ7, false);
        
        final GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
        attributeValue.setDirectAssignment(true);
        attributeValue.setDoProvision("sqlProvTest");
        attributeValue.setTargetName("sqlProvTest");
        attributeValue.setStemScopeString("sub");
    
        GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);
        GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem3);
    
        //lets sync these over
        GrouperProvisioningOutput grouperProvisioningOutput = fullProvision();
        GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
        assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());

        String sql = "select uuid from testgrouper_prov_ldap_group";
        
        List<Object[]> dataInTable = new GcDbAccess().sql(sql).selectList(Object[].class);
        
        Set<MultiKey> groupNamesInTable = new HashSet<MultiKey>();
        
        for (Object[] row: dataInTable) {
          groupNamesInTable.add(new MultiKey(row));
        }
        
        assertEquals(2, groupNamesInTable.size());
        assertTrue(groupNamesInTable.contains(new MultiKey(new Object[]{"test:testGroup"})));
        assertTrue(groupNamesInTable.contains(new MultiKey(new Object[]{"test3:testGroup3"})));
  
        sql = "select group_uuid, attribute_name, attribute_value from testgrouper_pro_ldap_group_attr";
        
        dataInTable = new GcDbAccess().sql(sql).selectList(Object[].class);
        
        Set<MultiKey> attributesInTable = new HashSet<MultiKey>();
        
        for (Object[] row: dataInTable) {
          attributesInTable.add(new MultiKey(row));
        }
        
        assertEquals(6, attributesInTable.size());
        assertTrue(attributesInTable.contains(new MultiKey("test:testGroup", "groupName", "test:testGroup")));
        assertTrue(attributesInTable.contains(new MultiKey("test:testGroup", "subjectId", "test.subject.0")));
        assertTrue(attributesInTable.contains(new MultiKey("test:testGroup", "subjectId", "test.subject.1")));
        assertTrue(attributesInTable.contains(new MultiKey("test3:testGroup3", "groupName", "test3:testGroup3")));
        assertTrue(attributesInTable.contains(new MultiKey("test3:testGroup3", "subjectId", "test.subject.4")));
        assertTrue(attributesInTable.contains(new MultiKey("test3:testGroup3", "subjectId", "test.subject.5")));

        // add 4
        GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem4);

        // remove 3
        stem3.getAttributeDelegate().removeAttribute(GrouperProvisioningAttributeNames.retrieveAttributeDefNameMarker());
        GrouperProvisioningJob.runDaemonStandalone();
        // add member, remove member
        testGroup.addMember(SubjectTestHelper.SUBJ8, false);
        testGroup.deleteMember(SubjectTestHelper.SUBJ1, false);

        grouperProvisioningOutput = fullProvision();
        grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
        assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());

        sql = "select uuid from testgrouper_prov_ldap_group";
        
        dataInTable = new GcDbAccess().sql(sql).selectList(Object[].class);
        
        groupNamesInTable = new HashSet<MultiKey>();
        
        for (Object[] row: dataInTable) {
          groupNamesInTable.add(new MultiKey(row));
        }
        
        assertEquals(2, groupNamesInTable.size());
        assertTrue(groupNamesInTable.contains(new MultiKey(new Object[]{"test:testGroup"})));
        assertTrue(groupNamesInTable.contains(new MultiKey(new Object[]{"test4:testGroup4"})));
  
        sql = "select group_uuid, attribute_name, attribute_value from testgrouper_pro_ldap_group_attr";
        
        dataInTable = new GcDbAccess().sql(sql).selectList(Object[].class);
        
        attributesInTable = new HashSet<MultiKey>();
        
        for (Object[] row: dataInTable) {
          attributesInTable.add(new MultiKey(row));
        }
        
        assertEquals(6, attributesInTable.size());
        assertTrue(attributesInTable.contains(new MultiKey("test:testGroup", "groupName", "test:testGroup")));
        assertTrue(attributesInTable.contains(new MultiKey("test:testGroup", "subjectId", "test.subject.0")));
        assertTrue(attributesInTable.contains(new MultiKey("test:testGroup", "subjectId", "test.subject.8")));
        assertTrue(attributesInTable.contains(new MultiKey("test4:testGroup4", "groupName", "test4:testGroup4")));
        assertTrue(attributesInTable.contains(new MultiKey("test4:testGroup4", "subjectId", "test.subject.6")));
        assertTrue(attributesInTable.contains(new MultiKey("test4:testGroup4", "subjectId", "test.subject.7")));
        
      }

  /**
   * just do a simple full sync of groups and memberships
   */
  public void testSimpleGroupLdapInsertUpdateDeleteRealTime() {
    
    simpleGroupLdapConfigure();

    
    
    Stem stem = new StemSave(this.grouperSession).assignName("test").save();
    Stem stem2 = new StemSave(this.grouperSession).assignName("test2").save();
    Stem stem3 = new StemSave(this.grouperSession).assignName("test3").save();
    Stem stem4 = new StemSave(this.grouperSession).assignName("test4").save();
    
    // mark some folders to provision
    Group testGroup = new GroupSave(this.grouperSession).assignName("test:testGroup").save();
    Group testGroup2 = new GroupSave(this.grouperSession).assignName("test2:testGroup2").save();
    Group testGroup3 = new GroupSave(this.grouperSession).assignName("test3:testGroup3").save();
    Group testGroup4 = new GroupSave(this.grouperSession).assignName("test4:testGroup4").save();
    
    testGroup.addMember(SubjectTestHelper.SUBJ0, false);
    testGroup.addMember(SubjectTestHelper.SUBJ1, false);
  
    testGroup2.addMember(SubjectTestHelper.SUBJ2, false);
    testGroup2.addMember(SubjectTestHelper.SUBJ3, false);
  
    testGroup3.addMember(SubjectTestHelper.SUBJ4, false);
    testGroup3.addMember(SubjectTestHelper.SUBJ5, false);
  
    testGroup4.addMember(SubjectTestHelper.SUBJ6, false);
    testGroup4.addMember(SubjectTestHelper.SUBJ7, false);
    
    final GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision("sqlProvTest");
    attributeValue.setTargetName("sqlProvTest");
    attributeValue.setStemScopeString("sub");
  
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem3);
  
    //lets sync these over
    GrouperProvisioningOutput grouperProvisioningOutput = fullProvision();
    GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());

    Hib3GrouperLoaderLog hib3GrouperLoaderLog = grouperProvisioner.retrieveGrouperProvisioningOutput().getHib3GrouperLoaderLog();
    // we have 4 memberships and 2 groups.  the users arent in the target
    assertEquals(6, GrouperUtil.intValue(hib3GrouperLoaderLog.getTotalCount(), -1));
    assertEquals(0, GrouperUtil.intValue(hib3GrouperLoaderLog.getDeleteCount(), -1));
    // insert those
    assertEquals(6, GrouperUtil.intValue(hib3GrouperLoaderLog.getInsertCount(), -1));
    // had to update the two groups
    assertEquals(0, GrouperUtil.intValue(hib3GrouperLoaderLog.getUpdateCount(), -1));


    String sql = "select uuid from testgrouper_prov_ldap_group";
    
    List<Object[]> dataInTable = new GcDbAccess().sql(sql).selectList(Object[].class);
    
    Set<MultiKey> groupNamesInTable = new HashSet<MultiKey>();
    
    for (Object[] row: dataInTable) {
      groupNamesInTable.add(new MultiKey(row));
    }
    
    assertEquals(2, groupNamesInTable.size());
    assertTrue(groupNamesInTable.contains(new MultiKey(new Object[]{"test:testGroup"})));
    assertTrue(groupNamesInTable.contains(new MultiKey(new Object[]{"test3:testGroup3"})));
  
    sql = "select group_uuid, attribute_name, attribute_value from testgrouper_pro_ldap_group_attr";
    
    dataInTable = new GcDbAccess().sql(sql).selectList(Object[].class);
    
    Set<MultiKey> attributesInTable = new HashSet<MultiKey>();
    
    for (Object[] row: dataInTable) {
      attributesInTable.add(new MultiKey(row));
    }
    
    assertEquals(6, attributesInTable.size());
    assertTrue(attributesInTable.contains(new MultiKey("test:testGroup", "groupName", "test:testGroup")));
    assertTrue(attributesInTable.contains(new MultiKey("test:testGroup", "subjectId", "test.subject.0")));
    assertTrue(attributesInTable.contains(new MultiKey("test:testGroup", "subjectId", "test.subject.1")));
    assertTrue(attributesInTable.contains(new MultiKey("test3:testGroup3", "groupName", "test3:testGroup3")));
    assertTrue(attributesInTable.contains(new MultiKey("test3:testGroup3", "subjectId", "test.subject.4")));
    assertTrue(attributesInTable.contains(new MultiKey("test3:testGroup3", "subjectId", "test.subject.5")));
  
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer." + JOB_NAME + ".class", 
        EsbConsumer.class.getName());
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer." + JOB_NAME + ".publisher.class", 
        ProvisioningConsumer.class.getName());
    
    //something that will never fire
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer." + JOB_NAME + ".quartzCron", 
        "0 0 0 1 1 ? 2200");

    // we dont need an EL filter
//    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer." + JOB_NAME + ".elfilter", 
//        "(event.eventType == 'MEMBERSHIP_DELETE' || event.eventType == 'MEMBERSHIP_ADD' || event.eventType == 'MEMBERSHIP_UPDATE')  && event.sourceId == 'jdbc' ");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer." + JOB_NAME + ".provisionerConfigId", "sqlProvTest");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer." + JOB_NAME + ".provisionerJobSyncType", 
        GrouperProvisioningType.incrementalProvisionChangeLog.name());

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer." + JOB_NAME + ".publisher.debug", "true");

    //clear out changelog
    // run the provisioner, it will init
    hib3GrouperLoaderLog = incrementalProvision();
    assertEquals(0, GrouperUtil.intValue(hib3GrouperLoaderLog.getTotalCount(), -1));
    assertEquals(0, GrouperUtil.intValue(hib3GrouperLoaderLog.getInsertCount(), -1));
    assertEquals(0, GrouperUtil.intValue(hib3GrouperLoaderLog.getUpdateCount(), -1));
    assertEquals(0, GrouperUtil.intValue(hib3GrouperLoaderLog.getDeleteCount(), -1));

    // add 3
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem4);
  
    // remove 3
    stem3.getAttributeDelegate().removeAttribute(GrouperProvisioningAttributeNames.retrieveAttributeDefNameMarker());
    GrouperProvisioningJob.runDaemonStandalone();
    // add member, remove member
    testGroup.addMember(SubjectTestHelper.SUBJ8, false);
    testGroup.deleteMember(SubjectTestHelper.SUBJ1, false);

    //make sure unexpected events are handled
    new AttributeDefSave(grouperSession).assignName("test:whateverDef").assignCreateParentStemsIfNotExist(true).save();
    
    // run the provisioner
    hib3GrouperLoaderLog = incrementalProvision();

    assertEquals("SUCCESS", hib3GrouperLoaderLog.getStatus());

    assertTrue(10 < GrouperUtil.intValue(hib3GrouperLoaderLog.getTotalCount(), -1));
    assertEquals(4, GrouperUtil.intValue(hib3GrouperLoaderLog.getDeleteCount(), -1));
    assertEquals(4, GrouperUtil.intValue(hib3GrouperLoaderLog.getInsertCount(), -1));
    assertEquals(1, GrouperUtil.intValue(hib3GrouperLoaderLog.getUpdateCount(), -1));

//TODO    assertEquals(0, this.esbConsumer.internal_esbConsumerTestingData.changeLogEntryListSize);
    
    sql = "select uuid from testgrouper_prov_ldap_group";
    
    dataInTable = new GcDbAccess().sql(sql).selectList(Object[].class);
    
    groupNamesInTable = new HashSet<MultiKey>();
    
    for (Object[] row: dataInTable) {
      groupNamesInTable.add(new MultiKey(row));
    }
    
    assertEquals(2, groupNamesInTable.size());
    assertTrue(groupNamesInTable.contains(new MultiKey(new Object[]{"test:testGroup"})));
    assertTrue(groupNamesInTable.contains(new MultiKey(new Object[]{"test4:testGroup4"})));
  
    sql = "select group_uuid, attribute_name, attribute_value from testgrouper_pro_ldap_group_attr";
    
    dataInTable = new GcDbAccess().sql(sql).selectList(Object[].class);
    
    attributesInTable = new HashSet<MultiKey>();
    
    for (Object[] row: dataInTable) {
      attributesInTable.add(new MultiKey(row));
    }
    
    assertEquals(6, attributesInTable.size());
    assertTrue(attributesInTable.contains(new MultiKey("test:testGroup", "groupName", "test:testGroup")));
    assertTrue(attributesInTable.contains(new MultiKey("test:testGroup", "subjectId", "test.subject.0")));
    assertTrue(attributesInTable.contains(new MultiKey("test:testGroup", "subjectId", "test.subject.8")));
    assertTrue(attributesInTable.contains(new MultiKey("test4:testGroup4", "groupName", "test4:testGroup4")));
    assertTrue(attributesInTable.contains(new MultiKey("test4:testGroup4", "subjectId", "test.subject.6")));
    assertTrue(attributesInTable.contains(new MultiKey("test4:testGroup4", "subjectId", "test.subject.7")));

    // do an incremental should do nothing
    hib3GrouperLoaderLog = incrementalProvision();
    assertEquals(0, GrouperUtil.intValue(hib3GrouperLoaderLog.getDeleteCount(), -1));
    assertEquals(0, GrouperUtil.intValue(hib3GrouperLoaderLog.getInsertCount(), -1));
    assertEquals(0, GrouperUtil.intValue(hib3GrouperLoaderLog.getUpdateCount(), -1));

    
    sql = "select uuid from testgrouper_prov_ldap_group";
    
    dataInTable = new GcDbAccess().sql(sql).selectList(Object[].class);
    
    groupNamesInTable = new HashSet<MultiKey>();
    
    for (Object[] row: dataInTable) {
      groupNamesInTable.add(new MultiKey(row));
    }
    
    assertEquals(2, groupNamesInTable.size());
    assertTrue(groupNamesInTable.contains(new MultiKey(new Object[]{"test:testGroup"})));
    assertTrue(groupNamesInTable.contains(new MultiKey(new Object[]{"test4:testGroup4"})));
  
    sql = "select group_uuid, attribute_name, attribute_value from testgrouper_pro_ldap_group_attr";
    
    dataInTable = new GcDbAccess().sql(sql).selectList(Object[].class);
    
    attributesInTable = new HashSet<MultiKey>();
    
    for (Object[] row: dataInTable) {
      attributesInTable.add(new MultiKey(row));
    }
    
    assertEquals(6, attributesInTable.size());
    assertTrue(attributesInTable.contains(new MultiKey("test:testGroup", "groupName", "test:testGroup")));
    assertTrue(attributesInTable.contains(new MultiKey("test:testGroup", "subjectId", "test.subject.0")));
    assertTrue(attributesInTable.contains(new MultiKey("test:testGroup", "subjectId", "test.subject.8")));
    assertTrue(attributesInTable.contains(new MultiKey("test4:testGroup4", "groupName", "test4:testGroup4")));
    assertTrue(attributesInTable.contains(new MultiKey("test4:testGroup4", "subjectId", "test.subject.6")));
    assertTrue(attributesInTable.contains(new MultiKey("test4:testGroup4", "subjectId", "test.subject.7")));

    // do a full should do nothing
    grouperProvisioningOutput = fullProvision();
    grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
    
    sql = "select uuid from testgrouper_prov_ldap_group";
    
    dataInTable = new GcDbAccess().sql(sql).selectList(Object[].class);
    
    groupNamesInTable = new HashSet<MultiKey>();
    
    for (Object[] row: dataInTable) {
      groupNamesInTable.add(new MultiKey(row));
    }
    
    assertEquals(2, groupNamesInTable.size());
    assertTrue(groupNamesInTable.contains(new MultiKey(new Object[]{"test:testGroup"})));
    assertTrue(groupNamesInTable.contains(new MultiKey(new Object[]{"test4:testGroup4"})));
  
    sql = "select group_uuid, attribute_name, attribute_value from testgrouper_pro_ldap_group_attr";
    
    dataInTable = new GcDbAccess().sql(sql).selectList(Object[].class);
    
    attributesInTable = new HashSet<MultiKey>();
    
    for (Object[] row: dataInTable) {
      attributesInTable.add(new MultiKey(row));
    }
    
    assertEquals(6, attributesInTable.size());
    assertTrue(attributesInTable.contains(new MultiKey("test:testGroup", "groupName", "test:testGroup")));
    assertTrue(attributesInTable.contains(new MultiKey("test:testGroup", "subjectId", "test.subject.0")));
    assertTrue(attributesInTable.contains(new MultiKey("test:testGroup", "subjectId", "test.subject.8")));
    assertTrue(attributesInTable.contains(new MultiKey("test4:testGroup4", "groupName", "test4:testGroup4")));
    assertTrue(attributesInTable.contains(new MultiKey("test4:testGroup4", "subjectId", "test.subject.6")));
    assertTrue(attributesInTable.contains(new MultiKey("test4:testGroup4", "subjectId", "test.subject.7")));


  }

  private void simpleGroupLdapConfigure() {
    SqlProvisionerTestUtils.configureSqlProvisioner(new SqlProvisionerTestConfigInput()
        .assignGroupDeleteType("deleteGroupsIfNotExistInGrouper")
        .assignMembershipDeleteType("deleteMembershipsIfNotExistInGrouper")
        .assignGroupAttributesTable(true)
        .assignGroupTableName("testgrouper_prov_ldap_group")
        .assignGroupTableIdColumn("uuid")
        .assignGroupAttributeCount(3)
        .assignProvisioningType("groupAttributes")
      );

  }

  /**
   * esb consumer
   */
  private EsbConsumer esbConsumer;

  /**
   * 
   */
  private final String JOB_NAME = "sqlProvisionerIncremental";

  /**
   * pa use case with group members ldap and user link
   */
  public void testSimpleGroupLdapInsertUpdateDeleteFullSync2() {
    
    simpleGroupLdapConfigure();
        
    Stem stem = new StemSave(this.grouperSession).assignName("test").save();
    Stem stem2 = new StemSave(this.grouperSession).assignName("test2").save();
    Stem stem3 = new StemSave(this.grouperSession).assignName("test3").save();
    Stem stem4 = new StemSave(this.grouperSession).assignName("test4").save();
    
    // mark some folders to provision
    Group testGroup = new GroupSave(this.grouperSession).assignName("test:testGroup").save();
    Group testGroup2 = new GroupSave(this.grouperSession).assignName("test2:testGroup2").save();
    Group testGroup3 = new GroupSave(this.grouperSession).assignName("test3:testGroup3").save();
    Group testGroup4 = new GroupSave(this.grouperSession).assignName("test4:testGroup4").save();
    
    testGroup.addMember(SubjectTestHelper.SUBJ0, false);
    testGroup.addMember(SubjectTestHelper.SUBJ1, false);
  
    testGroup2.addMember(SubjectTestHelper.SUBJ2, false);
    testGroup2.addMember(SubjectTestHelper.SUBJ3, false);
  
    testGroup3.addMember(SubjectTestHelper.SUBJ4, false);
    testGroup3.addMember(SubjectTestHelper.SUBJ5, false);
  
    testGroup4.addMember(SubjectTestHelper.SUBJ6, false);
    testGroup4.addMember(SubjectTestHelper.SUBJ7, false);
    
    final GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision("sqlProvTest");
    attributeValue.setTargetName("sqlProvTest");
    attributeValue.setStemScopeString("sub");
  
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem3);
  
    //lets sync these over
    GrouperProvisioningOutput grouperProvisioningOutput = fullProvision();
    GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());

    String sql = "select uuid from testgrouper_prov_ldap_group";
    
    List<Object[]> dataInTable = new GcDbAccess().sql(sql).selectList(Object[].class);
    
    Set<MultiKey> groupNamesInTable = new HashSet<MultiKey>();
    
    for (Object[] row: dataInTable) {
      groupNamesInTable.add(new MultiKey(row));
    }
    
    assertEquals(2, groupNamesInTable.size());
    assertTrue(groupNamesInTable.contains(new MultiKey(new Object[]{"test:testGroup"})));
    assertTrue(groupNamesInTable.contains(new MultiKey(new Object[]{"test3:testGroup3"})));
  
    sql = "select group_uuid, attribute_name, attribute_value from testgrouper_pro_ldap_group_attr";
    
    dataInTable = new GcDbAccess().sql(sql).selectList(Object[].class);
    
    Set<MultiKey> attributesInTable = new HashSet<MultiKey>();
    
    for (Object[] row: dataInTable) {
      attributesInTable.add(new MultiKey(row));
    }
    
    assertEquals(6, attributesInTable.size());
    assertTrue(attributesInTable.contains(new MultiKey("test:testGroup", "groupName", "test:testGroup")));
    assertTrue(attributesInTable.contains(new MultiKey("test:testGroup", "subjectId", "test.subject.0")));
    assertTrue(attributesInTable.contains(new MultiKey("test:testGroup", "subjectId", "test.subject.1")));
    assertTrue(attributesInTable.contains(new MultiKey("test3:testGroup3", "groupName", "test3:testGroup3")));
    assertTrue(attributesInTable.contains(new MultiKey("test3:testGroup3", "subjectId", "test.subject.4")));
    assertTrue(attributesInTable.contains(new MultiKey("test3:testGroup3", "subjectId", "test.subject.5")));
  
    // add 4
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem4);
  
    // remove 3
    stem3.getAttributeDelegate().removeAttribute(GrouperProvisioningAttributeNames.retrieveAttributeDefNameMarker());
    GrouperProvisioningJob.runDaemonStandalone();
    // add member, remove member
    testGroup.addMember(SubjectTestHelper.SUBJ8, false);
    testGroup.deleteMember(SubjectTestHelper.SUBJ1, false);
  
    grouperProvisioningOutput = fullProvision();
    grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());

    sql = "select uuid from testgrouper_prov_ldap_group";
    
    dataInTable = new GcDbAccess().sql(sql).selectList(Object[].class);
    
    groupNamesInTable = new HashSet<MultiKey>();
    
    for (Object[] row: dataInTable) {
      groupNamesInTable.add(new MultiKey(row));
    }
    
    assertEquals(2, groupNamesInTable.size());
    assertTrue(groupNamesInTable.contains(new MultiKey(new Object[]{"test:testGroup"})));
    assertTrue(groupNamesInTable.contains(new MultiKey(new Object[]{"test4:testGroup4"})));
  
    sql = "select group_uuid, attribute_name, attribute_value from testgrouper_pro_ldap_group_attr";
    
    dataInTable = new GcDbAccess().sql(sql).selectList(Object[].class);
    
    attributesInTable = new HashSet<MultiKey>();
    
    for (Object[] row: dataInTable) {
      attributesInTable.add(new MultiKey(row));
    }
    
    assertEquals(6, attributesInTable.size());
    assertTrue(attributesInTable.contains(new MultiKey("test:testGroup", "groupName", "test:testGroup")));
    assertTrue(attributesInTable.contains(new MultiKey("test:testGroup", "subjectId", "test.subject.0")));
    assertTrue(attributesInTable.contains(new MultiKey("test:testGroup", "subjectId", "test.subject.8")));
    assertTrue(attributesInTable.contains(new MultiKey("test4:testGroup4", "groupName", "test4:testGroup4")));
    assertTrue(attributesInTable.contains(new MultiKey("test4:testGroup4", "subjectId", "test.subject.6")));
    assertTrue(attributesInTable.contains(new MultiKey("test4:testGroup4", "subjectId", "test.subject.7")));
    
  }

  /**
     * just do a simple full sync of groups and memberships
     */
    public void testSimpleGroupLdapInsertUpdateDeleteRealTimeChangeLogSize() {
      
      simpleGroupLdapConfigure();
          
      Stem stem = new StemSave(this.grouperSession).assignName("test").save();
      Stem stem2 = new StemSave(this.grouperSession).assignName("test2").save();
      Stem stem3 = new StemSave(this.grouperSession).assignName("test3").save();
      Stem stem4 = new StemSave(this.grouperSession).assignName("test4").save();
      
      // mark some folders to provision
      Group testGroup = new GroupSave(this.grouperSession).assignName("test:testGroup").save();
      Group testGroup2 = new GroupSave(this.grouperSession).assignName("test2:testGroup2").save();
      Group testGroup3 = new GroupSave(this.grouperSession).assignName("test3:testGroup3").save();
      Group testGroup4 = new GroupSave(this.grouperSession).assignName("test4:testGroup4").save();
      
      testGroup.addMember(SubjectTestHelper.SUBJ0, false);
      testGroup.addMember(SubjectTestHelper.SUBJ1, false);
    
      testGroup2.addMember(SubjectTestHelper.SUBJ2, false);
      testGroup2.addMember(SubjectTestHelper.SUBJ3, false);
    
      testGroup3.addMember(SubjectTestHelper.SUBJ4, false);
      testGroup3.addMember(SubjectTestHelper.SUBJ5, false);
    
      testGroup4.addMember(SubjectTestHelper.SUBJ6, false);
      testGroup4.addMember(SubjectTestHelper.SUBJ7, false);
      
      final GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
      attributeValue.setDirectAssignment(true);
      attributeValue.setDoProvision("sqlProvTest");
      attributeValue.setTargetName("sqlProvTest");
      attributeValue.setStemScopeString("sub");
    
      GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);
      GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem3);
    
    
      Hib3GrouperLoaderLog hib3GrouperLoaderLog = null;
      
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer." + JOB_NAME + ".class", 
          EsbConsumer.class.getName());
      
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer." + JOB_NAME + ".publisher.class", 
          ProvisioningConsumer.class.getName());
      
      //something that will never fire
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer." + JOB_NAME + ".quartzCron", 
          "0 0 0 1 1 ? 2200");
  
      // we dont need an EL filter
  //    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer." + JOB_NAME + ".elfilter", 
  //        "(event.eventType == 'MEMBERSHIP_DELETE' || event.eventType == 'MEMBERSHIP_ADD' || event.eventType == 'MEMBERSHIP_UPDATE')  && event.sourceId == 'jdbc' ");
  
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer." + JOB_NAME + ".provisionerConfigId", "sqlProvTest");
  
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer." + JOB_NAME + ".provisionerJobSyncType", 
          GrouperProvisioningType.incrementalProvisionChangeLog.name());
  
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer." + JOB_NAME + ".publisher.debug", "true");
  
      //clear out changelog
      // run the provisioner, it will init
      hib3GrouperLoaderLog = incrementalProvision();
      
      int changeLogTempSize = new GcDbAccess().sql("select count(1) from grouper_change_log_entry_temp").select(int.class);
      assertEquals(0, changeLogTempSize);
      
      // add group 4
      GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem4);
    
      // remove group 3
      stem3.getAttributeDelegate().removeAttribute(GrouperProvisioningAttributeNames.retrieveAttributeDefNameMarker());
      GrouperProvisioningJob.runDaemonStandalone();

      // add member, remove member
      testGroup.addMember(SubjectTestHelper.SUBJ8, false);
      testGroup.deleteMember(SubjectTestHelper.SUBJ1, false);
  
      int newChangeLogTempSize = new GcDbAccess().sql("select count(1) from grouper_change_log_entry_temp").select(int.class);
      
      assertTrue(newChangeLogTempSize + "", newChangeLogTempSize - changeLogTempSize < 50);
      
    }

  /**
   * just do a simple full sync of groups and memberships
   */
  public void testSimpleGroupLdapDao() {
    
    SqlProvisionerTestUtils.configureSqlProvisioner(new SqlProvisionerTestConfigInput()
        .assignEntityDeleteType("deleteEntitiesIfNotExistInGrouper")
        .assignGroupDeleteType("deleteGroupsIfNotExistInGrouper")
        .assignMembershipDeleteType("deleteMembershipsIfNotExistInGrouper")
        .assignGroupAttributesTable(true)
        .assignGroupTableName("testgrouper_prov_ldap_group")
        .assignGroupTableIdColumn("uuid")
        .assignEntityAttributesTable(true)
        .assignEntityTableName("testgrouper_prov_ldap_entity")
        .assignEntityTableIdColumn("entity_uuid")
        .assignEntityAttributeCount(3)
        .assignGroupAttributeCount(3)
        .assignProvisioningType("groupAttributes")
        );
    
    int countFromGroupTable = -1;
    int countFromGroupAttributeTable = -1;
    countFromGroupTable = new GcDbAccess().sql("select count(*) from testgrouper_prov_ldap_group").select(int.class);
    assertEquals(0, countFromGroupTable);
    countFromGroupAttributeTable = new GcDbAccess().sql("select count(*) from testgrouper_pro_ldap_group_attr").select(int.class);
    assertEquals(0, countFromGroupAttributeTable);

    GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveProvisioner("sqlProvTest");
    grouperProvisioner.retrieveGrouperProvisioningBehavior().setGrouperProvisioningType(GrouperProvisioningType.fullProvisionFull);
    grouperProvisioner.retrieveGrouperProvisioningConfiguration().configureProvisioner();
    // let the target dao tell the framework what it can do
    grouperProvisioner.retrieveGrouperProvisioningTargetDaoAdapter().getWrappedDao().registerGrouperProvisionerDaoCapabilities(
        grouperProvisioner.retrieveGrouperProvisioningTargetDaoAdapter().getWrappedDao().getGrouperProvisionerDaoCapabilities()
        );

    // let the provisioner tell the framework how the provisioner should behave with respect to the target
    grouperProvisioner.registerProvisioningBehaviors(grouperProvisioner.retrieveGrouperProvisioningBehavior());

    ProvisioningGroup provisioningGroup1 = new ProvisioningGroup();
    ProvisioningGroupWrapper provisioningGroupWrapper = new ProvisioningGroupWrapper();
    provisioningGroupWrapper.setGrouperProvisioner(grouperProvisioner);
    provisioningGroup1.setProvisioningGroupWrapper(provisioningGroupWrapper);
    provisioningGroup1.assignAttributeValue("uuid", "abc123");
    provisioningGroup1.assignAttributeValue("groupName", "a:b:c");
    provisioningGroup1.assignAttributeValue("subjectId", "subjectId0");
    provisioningGroup1.assignAttributeValue("subjectId", "subjectId1");

    provisioningGroup1.addInternal_objectChange(new ProvisioningObjectChange("uuid", ProvisioningObjectChangeAction.insert, null, "abc123"));
    provisioningGroup1.addInternal_objectChange(new ProvisioningObjectChange("groupName", ProvisioningObjectChangeAction.insert, null, "a:b:c"));
    provisioningGroup1.addInternal_objectChange(new ProvisioningObjectChange("subjectId", ProvisioningObjectChangeAction.insert, null, "subjectId0"));
    provisioningGroup1.addInternal_objectChange(new ProvisioningObjectChange("subjectId", ProvisioningObjectChangeAction.insert, null, "subjectId1"));

    ProvisioningGroup provisioningGroup2 = new ProvisioningGroup();
    provisioningGroupWrapper = new ProvisioningGroupWrapper();
    provisioningGroupWrapper.setGrouperProvisioner(grouperProvisioner);
    provisioningGroup2.setProvisioningGroupWrapper(provisioningGroupWrapper);
    provisioningGroup2.assignAttributeValue("uuid", "def456");
    provisioningGroup2.assignAttributeValue("groupName", "d:e:f");
    provisioningGroup2.assignAttributeValue("subjectId", "subjectId2");
    provisioningGroup2.assignAttributeValue("subjectId", "subjectId3");

    provisioningGroup2.addInternal_objectChange(new ProvisioningObjectChange("uuid", ProvisioningObjectChangeAction.insert, null, "def456"));
    provisioningGroup2.addInternal_objectChange(new ProvisioningObjectChange("groupName", ProvisioningObjectChangeAction.insert, null, "d:e:f"));
    provisioningGroup2.addInternal_objectChange(new ProvisioningObjectChange("subjectId", ProvisioningObjectChangeAction.insert, null, "subjectId2"));
    provisioningGroup2.addInternal_objectChange(new ProvisioningObjectChange("subjectId", ProvisioningObjectChangeAction.insert, null, "subjectId3"));
    
    ProvisioningGroup provisioningGroup3 = new ProvisioningGroup();
    provisioningGroupWrapper = new ProvisioningGroupWrapper();
    provisioningGroupWrapper.setGrouperProvisioner(grouperProvisioner);
    provisioningGroup3.setProvisioningGroupWrapper(provisioningGroupWrapper);
    provisioningGroup3.assignAttributeValue("uuid", "ghi789");
    provisioningGroup3.assignAttributeValue("groupName", "g:h:i");
    provisioningGroup3.assignAttributeValue("subjectId", "subjectId4");
    provisioningGroup3.assignAttributeValue("subjectId", "subjectId5");

    provisioningGroup3.addInternal_objectChange(new ProvisioningObjectChange("uuid", ProvisioningObjectChangeAction.insert, null, "ghi789"));
    provisioningGroup3.addInternal_objectChange(new ProvisioningObjectChange("groupName", ProvisioningObjectChangeAction.insert, null, "g:h:i"));
    provisioningGroup3.addInternal_objectChange(new ProvisioningObjectChange("subjectId", ProvisioningObjectChangeAction.insert, null, "subjectId4"));
    provisioningGroup3.addInternal_objectChange(new ProvisioningObjectChange("subjectId", ProvisioningObjectChangeAction.insert, null, "subjectId5"));
    
    grouperProvisioner.retrieveGrouperProvisioningTargetDaoAdapter().insertGroups(new TargetDaoInsertGroupsRequest(GrouperUtil.toList(provisioningGroup1, provisioningGroup2, provisioningGroup3)));

    Set<MultiKey> groupNamesInTable = selectGroupLikeLdapRecords();
    
    assertEquals(3, groupNamesInTable.size());
    assertTrue(groupNamesInTable.contains(new MultiKey(new Object[]{"abc123"})));
    assertTrue(groupNamesInTable.contains(new MultiKey(new Object[]{"def456"})));
    assertTrue(groupNamesInTable.contains(new MultiKey(new Object[]{"ghi789"})));
  
    Set<MultiKey> attributesInTable = selectGroupLikeLdapAttributeRecords();
    
    assertEquals(9, attributesInTable.size());
    assertTrue(attributesInTable.contains(new MultiKey("abc123", "groupName", "a:b:c")));
    assertTrue(attributesInTable.contains(new MultiKey("abc123", "subjectId", "subjectId0")));
    assertTrue(attributesInTable.contains(new MultiKey("abc123", "subjectId", "subjectId1")));
    assertTrue(attributesInTable.contains(new MultiKey("def456", "groupName", "d:e:f")));
    assertTrue(attributesInTable.contains(new MultiKey("def456", "subjectId", "subjectId2")));
    assertTrue(attributesInTable.contains(new MultiKey("def456", "subjectId", "subjectId3")));
    assertTrue(attributesInTable.contains(new MultiKey("ghi789", "groupName", "g:h:i")));
    assertTrue(attributesInTable.contains(new MultiKey("ghi789", "subjectId", "subjectId4")));
    assertTrue(attributesInTable.contains(new MultiKey("ghi789", "subjectId", "subjectId5")));

    // retrieve all groups with memberships
    TargetDaoRetrieveAllGroupsResponse targetDaoRetrieveAllGroupsResponse = grouperProvisioner.retrieveGrouperProvisioningTargetDaoAdapter().retrieveAllGroups(new TargetDaoRetrieveAllGroupsRequest(true));
    Map<String, ProvisioningGroup> idToGroup = new HashMap<String, ProvisioningGroup>();
    for (ProvisioningGroup provisioningGroup : targetDaoRetrieveAllGroupsResponse.getTargetGroups()) {
      idToGroup.put(provisioningGroup.retrieveAttributeValueString("uuid"), provisioningGroup);
    }
    assertEquals(3, idToGroup.size());
    ProvisioningGroup provisioningGroup1retrieved = idToGroup.get("abc123");
    assertEquals("abc123", provisioningGroup1retrieved.retrieveAttributeValueString("uuid"));
    assertEquals("a:b:c", provisioningGroup1retrieved.retrieveAttributeValueString("groupName"));
    assertTrue(provisioningGroup1retrieved.retrieveAttributeValueSet("subjectId").contains("subjectId0"));
    assertTrue(provisioningGroup1retrieved.retrieveAttributeValueSet("subjectId").contains("subjectId1"));
    ProvisioningGroup provisioningGroup2retrieved = idToGroup.get("def456");
    assertEquals("def456", provisioningGroup2retrieved.retrieveAttributeValueString("uuid"));
    assertEquals("d:e:f", provisioningGroup2retrieved.retrieveAttributeValueString("groupName"));
    assertTrue(provisioningGroup2retrieved.retrieveAttributeValueSet("subjectId").contains("subjectId2"));
    assertTrue(provisioningGroup2retrieved.retrieveAttributeValueSet("subjectId").contains("subjectId3"));
    ProvisioningGroup provisioningGroup3retrieved = idToGroup.get("ghi789");
    assertEquals("ghi789", provisioningGroup3retrieved.retrieveAttributeValueString("uuid"));
    assertEquals("g:h:i", provisioningGroup3retrieved.retrieveAttributeValueString("groupName"));
    assertTrue(provisioningGroup3retrieved.retrieveAttributeValueSet("subjectId").contains("subjectId4"));
    assertTrue(provisioningGroup3retrieved.retrieveAttributeValueSet("subjectId").contains("subjectId5"));
    
    // retrieve some groups with memberships
    List<ProvisioningGroup> grouperTargetGroups = GrouperUtil.toList(provisioningGroup1, provisioningGroup2);
    grouperProvisioner.retrieveGrouperProvisioningTranslator().idTargetGroups(
        grouperTargetGroups);

    TargetDaoRetrieveGroupsRequest targetDaoRetrieveGroupsRequest = new TargetDaoRetrieveGroupsRequest(
        grouperTargetGroups, true);
    TargetDaoRetrieveGroupsResponse targetDaoRetrieveGroupsResponse = 
        grouperProvisioner.retrieveGrouperProvisioningTargetDaoAdapter().retrieveGroups(targetDaoRetrieveGroupsRequest);
    idToGroup = new HashMap<String, ProvisioningGroup>();
    for (ProvisioningGroup provisioningGroup : targetDaoRetrieveGroupsResponse.getTargetGroups()) {
      idToGroup.put(provisioningGroup.retrieveAttributeValueString("uuid"), provisioningGroup);
    }
    assertEquals(2, idToGroup.size());
    provisioningGroup1retrieved = idToGroup.get("abc123");
    assertEquals("abc123", provisioningGroup1retrieved.retrieveAttributeValueString("uuid"));
    assertEquals("a:b:c", provisioningGroup1retrieved.retrieveAttributeValueString("groupName"));
    assertTrue(provisioningGroup1retrieved.retrieveAttributeValueSet("subjectId").contains("subjectId0"));
    assertTrue(provisioningGroup1retrieved.retrieveAttributeValueSet("subjectId").contains("subjectId1"));
    provisioningGroup2retrieved = idToGroup.get("def456");
    assertEquals("def456", provisioningGroup2retrieved.retrieveAttributeValueString("uuid"));
    assertEquals("d:e:f", provisioningGroup2retrieved.retrieveAttributeValueString("groupName"));
    assertTrue(provisioningGroup2retrieved.retrieveAttributeValueSet("subjectId").contains("subjectId2"));
    assertTrue(provisioningGroup2retrieved.retrieveAttributeValueSet("subjectId").contains("subjectId3"));
    
    // retrieve some groups without memberships
    targetDaoRetrieveGroupsRequest = new TargetDaoRetrieveGroupsRequest(
        grouperTargetGroups, false);
    targetDaoRetrieveGroupsResponse = 
        grouperProvisioner.retrieveGrouperProvisioningTargetDaoAdapter().retrieveGroups(targetDaoRetrieveGroupsRequest);
    idToGroup = new HashMap<String, ProvisioningGroup>();
    for (ProvisioningGroup provisioningGroup : targetDaoRetrieveGroupsResponse.getTargetGroups()) {
      idToGroup.put(provisioningGroup.retrieveAttributeValueString("uuid"), provisioningGroup);
    }
    assertEquals(2, idToGroup.size());
    provisioningGroup1retrieved = idToGroup.get("abc123");
    assertEquals("abc123", provisioningGroup1retrieved.retrieveAttributeValueString("uuid"));
    assertEquals("a:b:c", provisioningGroup1retrieved.retrieveAttributeValueString("groupName"));
    assertFalse(provisioningGroup1retrieved.getAttributes().containsKey("subjectId"));
    provisioningGroup2retrieved = idToGroup.get("def456");
    assertEquals("def456", provisioningGroup2retrieved.retrieveAttributeValueString("uuid"));
    assertEquals("d:e:f", provisioningGroup2retrieved.retrieveAttributeValueString("groupName"));
    assertFalse(provisioningGroup2retrieved.getAttributes().containsKey("subjectId"));

    // update groups
    ProvisioningGroup provisioningGroup1update = new ProvisioningGroup();
    provisioningGroup1update.assignAttributeValue("uuid", "abc123");
    provisioningGroup1update.assignAttributeValue("groupName", "a:b:c");
    provisioningGroup1update.assignAttributeValue("subjectId", "subjectId0");
    provisioningGroup1update.assignAttributeValue("subjectId", "subjectId1");

    provisioningGroup1update.addInternal_objectChange(new ProvisioningObjectChange("groupName", ProvisioningObjectChangeAction.update, "a:b:c", "a:b:cu"));
    provisioningGroup1update.addInternal_objectChange(new ProvisioningObjectChange("subjectId", ProvisioningObjectChangeAction.insert, null, "subjectId0i"));
    provisioningGroup1update.addInternal_objectChange(new ProvisioningObjectChange("subjectId", ProvisioningObjectChangeAction.delete, "subjectId1", null));
    provisioningGroup1update.addInternal_objectChange(new ProvisioningObjectChange("subjectId", ProvisioningObjectChangeAction.update, "subjectId0", "subjectId0u"));

    ProvisioningGroup provisioningGroup2update = new ProvisioningGroup();
    provisioningGroup2update.assignAttributeValue("uuid", "def456");
    provisioningGroup2update.assignAttributeValue("groupName", "d:e:f");
    provisioningGroup2update.assignAttributeValue("subjectId", "subjectId2");
    provisioningGroup2update.assignAttributeValue("subjectId", "subjectId3");

    provisioningGroup2update.addInternal_objectChange(new ProvisioningObjectChange("groupName", ProvisioningObjectChangeAction.update, "d:e:f", "d:e:fu"));
    provisioningGroup2update.addInternal_objectChange(new ProvisioningObjectChange("subjectId", ProvisioningObjectChangeAction.insert, null, "subjectId2i"));
    provisioningGroup2update.addInternal_objectChange(new ProvisioningObjectChange("subjectId", ProvisioningObjectChangeAction.delete, "subjectId3", null));
    provisioningGroup2update.addInternal_objectChange(new ProvisioningObjectChange("subjectId", ProvisioningObjectChangeAction.update, "subjectId2", "subjectId2u"));

    TargetDaoUpdateGroupsRequest targetDaoUpdateGroupsRequest = new TargetDaoUpdateGroupsRequest(
        GrouperUtil.toList(provisioningGroup1update, provisioningGroup2update));

    TargetDaoUpdateGroupsResponse targetDaoUpdateGroupsResponse = 
        grouperProvisioner.retrieveGrouperProvisioningTargetDaoAdapter().updateGroups(targetDaoUpdateGroupsRequest);
    
    groupNamesInTable = selectGroupLikeLdapRecords();
    
    assertEquals(3, groupNamesInTable.size());
    assertTrue(groupNamesInTable.contains(new MultiKey(new Object[]{"abc123"})));
    assertTrue(groupNamesInTable.contains(new MultiKey(new Object[]{"def456"})));
    assertTrue(groupNamesInTable.contains(new MultiKey(new Object[]{"ghi789"})));
  
    attributesInTable = selectGroupLikeLdapAttributeRecords();
    
    assertEquals(9, attributesInTable.size());
    assertTrue(attributesInTable.contains(new MultiKey("abc123", "groupName", "a:b:cu")));
    assertTrue(attributesInTable.contains(new MultiKey("abc123", "subjectId", "subjectId0i")));
    assertTrue(attributesInTable.contains(new MultiKey("abc123", "subjectId", "subjectId0u")));
    assertTrue(attributesInTable.contains(new MultiKey("def456", "groupName", "d:e:fu")));
    assertTrue(attributesInTable.contains(new MultiKey("def456", "subjectId", "subjectId2i")));
    assertTrue(attributesInTable.contains(new MultiKey("def456", "subjectId", "subjectId2u")));
    assertTrue(attributesInTable.contains(new MultiKey("ghi789", "groupName", "g:h:i")));
    assertTrue(attributesInTable.contains(new MultiKey("ghi789", "subjectId", "subjectId4")));
    assertTrue(attributesInTable.contains(new MultiKey("ghi789", "subjectId", "subjectId5")));

    
    // delete groups
    grouperProvisioner.retrieveGrouperProvisioningTargetDaoAdapter().deleteGroups(new TargetDaoDeleteGroupsRequest(
        grouperTargetGroups));

    groupNamesInTable = selectGroupLikeLdapRecords();
    
    assertEquals(1, groupNamesInTable.size());
    assertTrue(groupNamesInTable.contains(new MultiKey(new Object[]{"ghi789"})));
  
    attributesInTable = selectGroupLikeLdapAttributeRecords();
    
    assertEquals(3, attributesInTable.size());
    assertTrue(attributesInTable.contains(new MultiKey("ghi789", "groupName", "g:h:i")));
    assertTrue(attributesInTable.contains(new MultiKey("ghi789", "subjectId", "subjectId4")));
    assertTrue(attributesInTable.contains(new MultiKey("ghi789", "subjectId", "subjectId5")));

    // insert some entities
    new GcDbAccess().sql("insert into testgrouper_prov_ldap_entity (entity_uuid) values (?)").addBindVar("subject0uuid").executeSql();
    new GcDbAccess().sql("insert into testgrouper_pro_dap_entity_attr (entity_uuid, entity_attribute_name, entity_attribute_value) values (?,?,?)")
      .addBindVar("subject0uuid").addBindVar("dn").addBindVar("subject0").executeSql();
    new GcDbAccess().sql("insert into testgrouper_pro_dap_entity_attr (entity_uuid, entity_attribute_name, entity_attribute_value) values (?,?,?)")
      .addBindVar("subject0uuid").addBindVar("employeeId").addBindVar("10021368").executeSql();
    
    new GcDbAccess().sql("insert into testgrouper_prov_ldap_entity (entity_uuid) values (?)").addBindVar("subject1uuid").executeSql();
    new GcDbAccess().sql("insert into testgrouper_pro_dap_entity_attr (entity_uuid, entity_attribute_name, entity_attribute_value) values (?,?,?)")
      .addBindVar("subject1uuid").addBindVar("dn").addBindVar("subject1").executeSql();
    new GcDbAccess().sql("insert into testgrouper_pro_dap_entity_attr (entity_uuid, entity_attribute_name, entity_attribute_value) values (?,?,?)")
      .addBindVar("subject1uuid").addBindVar("employeeId").addBindVar("12345678").executeSql();

    new GcDbAccess().sql("insert into testgrouper_prov_ldap_entity (entity_uuid) values (?)").addBindVar("subject2uuid").executeSql();
    new GcDbAccess().sql("insert into testgrouper_pro_dap_entity_attr (entity_uuid, entity_attribute_name, entity_attribute_value) values (?,?,?)")
      .addBindVar("subject2uuid").addBindVar("dn").addBindVar("subject2").executeSql();
    new GcDbAccess().sql("insert into testgrouper_pro_dap_entity_attr (entity_uuid, entity_attribute_name, entity_attribute_value) values (?,?,?)")
      .addBindVar("subject2uuid").addBindVar("employeeId").addBindVar("34567890").executeSql();

    // retrieve all entities
    TargetDaoRetrieveAllEntitiesResponse targetDaoRetrieveAllEntitiesResponse = grouperProvisioner.retrieveGrouperProvisioningTargetDaoAdapter().retrieveAllEntities(new TargetDaoRetrieveAllEntitiesRequest());
    Map<String, ProvisioningEntity> idToEntity = new HashMap<String, ProvisioningEntity>();
    for (ProvisioningEntity provisioningEntity : targetDaoRetrieveAllEntitiesResponse.getTargetEntities()) {
      idToEntity.put(provisioningEntity.retrieveAttributeValueString("entity_uuid"), provisioningEntity);
    }
    assertEquals(3, idToEntity.size());
    ProvisioningEntity provisioningEntity1retrieved = idToEntity.get("subject0uuid");
    assertEquals(provisioningEntity1retrieved.toString(), "subject0uuid", provisioningEntity1retrieved.retrieveAttributeValueString("entity_uuid"));
    assertEquals("subject0", provisioningEntity1retrieved.retrieveAttributeValueString("dn"));
    assertEquals("10021368", provisioningEntity1retrieved.retrieveAttributeValueString("employeeId"));
    ProvisioningEntity provisioningEntity2retrieved = idToEntity.get("subject1uuid");
    assertEquals("subject1", provisioningEntity2retrieved.retrieveAttributeValueString("dn"));
    assertEquals("12345678", provisioningEntity2retrieved.retrieveAttributeValueString("employeeId"));
    ProvisioningEntity provisioningEntity3retrieved = idToEntity.get("subject2uuid");
    assertEquals("subject2", provisioningEntity3retrieved.retrieveAttributeValueString("dn"));
    assertEquals("34567890", provisioningEntity3retrieved.retrieveAttributeValueString("employeeId"));
    
//    // retrieve some groups with memberships
//    TargetDaoRetrieveEntitiesRequest targetDaoRetrieveEntitiesRequest = new TargetDaoRetrieveEntitiesRequest(
//        GrouperUtil.toList(provisioningEntity1, provisioningEntity2), true);
//    TargetDaoRetrieveEntitiesResponse targetDaoRetrieveEntitiesResponse = 
//        grouperProvisioner.retrieveTargetDao().retrieveEntities(targetDaoRetrieveEntitiesRequest);
//    idToEntity = new HashMap<String, ProvisioningEntity>();
//    for (ProvisioningGroup provisioningGroup : targetDaoRetrieveEntitiesResponse.getTargetGroups()) {
//      idToEntity.put(provisioningGroup.getId(), provisioningGroup);
//    }
//    assertEquals(2, idToEntity.size());
//    provisioningEntity1retrieved = idToEntity.get("abc123");
//    assertEquals("abc123", provisioningEntity1retrieved.getId());
//    assertEquals("a:b:c", provisioningEntity1retrieved.retrieveAttributeValueString("groupName"));
//    assertTrue(provisioningEntity1retrieved.retrieveAttributeValueSet("subjectId").contains("subjectId0"));
//    assertTrue(provisioningEntity1retrieved.retrieveAttributeValueSet("subjectId").contains("subjectId1"));
//    provisioningEntity2retrieved = idToEntity.get("def456");
//    assertEquals("def456", provisioningEntity2retrieved.getId());
//    assertEquals("d:e:f", provisioningEntity2retrieved.retrieveAttributeValueString("groupName"));
//    assertTrue(provisioningEntity2retrieved.retrieveAttributeValueSet("subjectId").contains("subjectId2"));
//    assertTrue(provisioningEntity2retrieved.retrieveAttributeValueSet("subjectId").contains("subjectId3"));

    
  }

  public Set<MultiKey> selectGroupLikeLdapAttributeRecords() {
    String sql = "select group_uuid, attribute_name, attribute_value from testgrouper_pro_ldap_group_attr";
    
    List<Object[]> dataInTable = new GcDbAccess().sql(sql).selectList(Object[].class);
    
    Set<MultiKey> attributesInTable = new HashSet<MultiKey>();
    
    for (Object[] row: dataInTable) {
      attributesInTable.add(new MultiKey(row));
    }
    return attributesInTable;
  }

  public Set<MultiKey> selectGroupLikeLdapRecords() {
    String sql = "select uuid from testgrouper_prov_ldap_group";
    
    List<Object[]> dataInTable = new GcDbAccess().sql(sql).selectList(Object[].class);
    
    Set<MultiKey> groupNamesInTable = new HashSet<MultiKey>();
    
    for (Object[] row: dataInTable) {
      groupNamesInTable.add(new MultiKey(row));
    }
    return groupNamesInTable;
  }

  /**
   * @param ddlVersionBean
   * @param database
   */
  public void createTableLdapEntityAttr() {
  
    String tableName = "testgrouper_pro_dap_entity_attr";
    
    try {
      new GcDbAccess().sql("select count(*) from " + tableName).select(int.class);
    } catch (Exception e) {
      //we need to delete the test table if it is there, and create a new one
      //drop field id col, first drop foreign keys
      GrouperDdlUtils.changeDatabase(GrouperTestDdl.V1.getObjectName(), new DdlUtilsChangeDatabase() {
    
        public void changeDatabase(DdlVersionBean ddlVersionBean) {
          
          Database database = ddlVersionBean.getDatabase();

          Table loaderTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, tableName);
          
          GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "entity_uuid", Types.VARCHAR, "40", false, true);
        
          GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "entity_attribute_name", Types.VARCHAR, "200", false, true);
        
          GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "entity_attribute_value", Types.VARCHAR, "200", false, false);

          GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "last_modified", Types.TIMESTAMP, "200", false, false);
        

        }
        
      });
      
      GrouperDdlUtils.changeDatabase(GrouperTestDdl.V1.getObjectName(), new DdlUtilsChangeDatabase() {
        
        public void changeDatabase(DdlVersionBean ddlVersionBean) {
          
          Database database = ddlVersionBean.getDatabase();

          GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, ddlVersionBean, tableName, "testgrouper_pro_ldap_en_idx0", null, false, "entity_uuid", "entity_attribute_name");

          GrouperDdlUtils.ddlutilsFindOrCreateForeignKey(database, tableName, "testgrouper_pro_ldap_en_fk", "testgrouper_prov_ldap_entity", "entity_uuid", "entity_uuid");

        }
        
      });
    }

  }

  /**
   * just do a simple full sync of groups and memberships
   */
  public void testSimpleGroupLdapPa() {
    
    long started = System.currentTimeMillis();
    
    configureLdapPaTestCase();

        
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
    attributeValue.setDoProvision("sqlProvTest");
    attributeValue.setTargetName("sqlProvTest");
    attributeValue.setStemScopeString("sub");

    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);

    // add some entities
    for (int i=0;i<10;i++) {
      String uuid = GrouperUuid.getUuid();
      String dn = "dn_test.subject." + i;
      new GcDbAccess().sql("insert into testgrouper_prov_ldap_entity (entity_uuid) values (?)").addBindVar(uuid).executeSql();
      new GcDbAccess().sql("insert into testgrouper_pro_dap_entity_attr (entity_uuid, entity_attribute_name, entity_attribute_value) values (?,?,?)")
        .addBindVar(uuid).addBindVar("dn").addBindVar(dn).executeSql();
      new GcDbAccess().sql("insert into testgrouper_pro_dap_entity_attr (entity_uuid, entity_attribute_name, entity_attribute_value) values (?,?,?)")
        .addBindVar(uuid).addBindVar("employeeId").addBindVar("test.subject." + i).executeSql();
    }
    
    
    //AttributeAssign attributeAssign = stem.getAttributeDelegate().addAttribute(GrouperProvisioningAttributeNames.retrieveAttributeDefNameMarker()).getAttributeAssign();
    //attributeAssign.getAttributeValueDelegate().assignValueString(GrouperProvisioningAttributeNames.retrieveAttributeDefNameDoProvision())
    
    //lets sync these over
    GrouperProvisioningOutput grouperProvisioningOutput = fullProvision();
    GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    
    // make sure some time has passed
    GrouperUtil.sleep(1000);
    
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());

    String sql = "select uuid from testgrouper_prov_ldap_group";
    
    List<Object[]> dataInTable = new GcDbAccess().sql(sql).selectList(Object[].class);
    
    Set<MultiKey> groupNamesInTable = new HashSet<MultiKey>();
    
    for (Object[] row: dataInTable) {
      groupNamesInTable.add(new MultiKey(row));
    }
    
    assertEquals(1, groupNamesInTable.size());
    assertTrue(groupNamesInTable.contains(new MultiKey(new Object[]{"test:testGroup"})));

    sql = "select group_uuid, attribute_name, attribute_value from testgrouper_pro_ldap_group_attr";
    
    dataInTable = new GcDbAccess().sql(sql).selectList(Object[].class);
    
    Set<MultiKey> attributesInTable = new HashSet<MultiKey>();
    
    for (Object[] row: dataInTable) {
      attributesInTable.add(new MultiKey(row));
    }
    
    assertEquals(6, attributesInTable.size());
    
    assertTrue(attributesInTable.contains(new MultiKey("test:testGroup", "cn", "test:testGroup")));
    assertTrue(attributesInTable.contains(new MultiKey("test:testGroup", "dn", "cn=test:testGroup,OU=Grouper,OU=365Groups,DC=one,DC=upenn,DC=edu")));
    assertTrue(attributesInTable.contains(new MultiKey("test:testGroup", "gidNumber", "" + testGroup.getIdIndex())));
    assertTrue(attributesInTable.contains(new MultiKey("test:testGroup", "objectClass", "group")));
    assertTrue(attributesInTable.contains(new MultiKey("test:testGroup", "member", "dn_test.subject.0")));
    assertTrue(attributesInTable.contains(new MultiKey("test:testGroup", "member", "dn_test.subject.1")));
    //object changes
    assertEquals(0, GrouperUtil.length(grouperProvisioner.retrieveGrouperProvisioningDataChanges().getTargetObjectInserts().getProvisioningGroups()));
    assertEquals(0, GrouperUtil.length(grouperProvisioner.retrieveGrouperProvisioningDataChanges().getTargetObjectInserts().getProvisioningEntities()));
    assertEquals(0, GrouperUtil.length(grouperProvisioner.retrieveGrouperProvisioningDataChanges().getTargetObjectInserts().getProvisioningMemberships()));
    assertEquals(1, GrouperUtil.length(grouperProvisioner.retrieveGrouperProvisioningDataChanges().getTargetObjectUpdates().getProvisioningGroups()));
    assertEquals(0, GrouperUtil.length(grouperProvisioner.retrieveGrouperProvisioningDataChanges().getTargetObjectUpdates().getProvisioningEntities()));
    assertEquals(0, GrouperUtil.length(grouperProvisioner.retrieveGrouperProvisioningDataChanges().getTargetObjectUpdates().getProvisioningMemberships()));
    assertEquals(0, GrouperUtil.length(grouperProvisioner.retrieveGrouperProvisioningDataChanges().getTargetObjectDeletes().getProvisioningGroups()));
    assertEquals(0, GrouperUtil.length(grouperProvisioner.retrieveGrouperProvisioningDataChanges().getTargetObjectDeletes().getProvisioningEntities()));
    assertEquals(0, GrouperUtil.length(grouperProvisioner.retrieveGrouperProvisioningDataChanges().getTargetObjectDeletes().getProvisioningMemberships()));
    
    // field changes
    assertEquals(2, GrouperUtil.length(grouperProvisioner.retrieveGrouperProvisioningDataChanges().getTargetObjectUpdates().getProvisioningGroups().iterator().next().getInternal_objectChanges()));
    List<ProvisioningObjectChange> provisioningObjectChanges = new ArrayList<ProvisioningObjectChange>(
        grouperProvisioner.retrieveGrouperProvisioningDataChanges().getTargetObjectUpdates().getProvisioningGroups().iterator().next().getInternal_objectChanges());
    assertEquals(ProvisioningObjectChangeAction.insert, provisioningObjectChanges.get(0).getProvisioningObjectChangeAction());
    assertEquals(ProvisioningObjectChangeAction.insert, provisioningObjectChanges.get(1).getProvisioningObjectChangeAction());
    assertEquals("member", provisioningObjectChanges.get(0).getAttributeName());
    assertEquals("member", provisioningObjectChanges.get(1).getAttributeName());

    //get the grouper_sync and check cols
    GcGrouperSync gcGrouperSync = GcGrouperSyncDao.retrieveByProvisionerName(null, "sqlProvTest");
    assertEquals(1, gcGrouperSync.getGroupCount().intValue());
    assertEquals(2, gcGrouperSync.getUserCount().intValue());
    assertEquals(2, gcGrouperSync.getRecordsCount().intValue());
    assertTrue(started <  gcGrouperSync.getLastFullSyncRun().getTime());
    assertTrue(new Timestamp(System.currentTimeMillis()) + " > " + new Timestamp(gcGrouperSync.getLastFullSyncRun().getTime()), System.currentTimeMillis() >  gcGrouperSync.getLastFullSyncRun().getTime());
    assertTrue(started < gcGrouperSync.getLastUpdated().getTime());
    assertTrue(System.currentTimeMillis() > gcGrouperSync.getLastUpdated().getTime());
    
    GcGrouperSyncJob gcGrouperSyncJob = gcGrouperSync.getGcGrouperSyncJobDao().jobRetrieveBySyncType("fullProvisionFull");
    assertEquals(100, gcGrouperSyncJob.getPercentComplete().intValue());
    assertEquals(GcGrouperSyncJobState.notRunning, gcGrouperSyncJob.getJobState());
    assertTrue(started < gcGrouperSyncJob.getLastSyncTimestamp().getTime());
    assertTrue(System.currentTimeMillis() > gcGrouperSyncJob.getLastSyncTimestamp().getTime());
    assertTrue(started < gcGrouperSyncJob.getLastTimeWorkWasDone().getTime());
    assertTrue(System.currentTimeMillis() > gcGrouperSyncJob.getLastTimeWorkWasDone().getTime());
    assertTrue(started < gcGrouperSyncJob.getHeartbeat().getTime());
    assertTrue(System.currentTimeMillis() > gcGrouperSyncJob.getHeartbeat().getTime());
    assertTrue(started < gcGrouperSyncJob.getLastUpdated().getTime());
    assertTrue(System.currentTimeMillis() > gcGrouperSyncJob.getLastUpdated().getTime());
    assertNull(gcGrouperSyncJob.getErrorMessage());
    assertNull(gcGrouperSyncJob.getErrorTimestamp());
    
    GcGrouperSyncGroup gcGrouperSyncGroup = gcGrouperSync.getGcGrouperSyncGroupDao().groupRetrieveByGroupId(testGroup.getId());
    assertEquals(testGroup.getId(), gcGrouperSyncGroup.getGroupId());
    assertEquals(testGroup.getName(), gcGrouperSyncGroup.getGroupName());
    assertEquals(testGroup.getIdIndex(), gcGrouperSyncGroup.getGroupIdIndex());
    assertEquals("T", gcGrouperSyncGroup.getProvisionableDb());
    assertEquals("T", gcGrouperSyncGroup.getInTargetDb());
    assertEquals("T", gcGrouperSyncGroup.getInTargetInsertOrExistsDb());
    assertTrue(started < gcGrouperSyncGroup.getInTargetStart().getTime());
    assertTrue(System.currentTimeMillis() > gcGrouperSyncGroup.getInTargetStart().getTime());
    assertNull(gcGrouperSyncGroup.getInTargetEnd());
    assertTrue(started < gcGrouperSyncGroup.getProvisionableStart().getTime());
    assertTrue(System.currentTimeMillis() > gcGrouperSyncGroup.getProvisionableStart().getTime());
    assertNull(gcGrouperSyncGroup.getProvisionableEnd());
    assertTrue(started < gcGrouperSyncGroup.getLastUpdated().getTime());
    assertTrue(System.currentTimeMillis() > gcGrouperSyncGroup.getLastUpdated().getTime());
    assertEquals("cn=test:testGroup,OU=Grouper,OU=365Groups,DC=one,DC=upenn,DC=edu", gcGrouperSyncGroup.getGroupAttributeValueCache2());
    assertNull(gcGrouperSyncGroup.getGroupAttributeValueCache0());
    assertNull(gcGrouperSyncGroup.getGroupAttributeValueCache1());
    assertNull(gcGrouperSyncGroup.getGroupAttributeValueCache3());
    assertNull(gcGrouperSyncGroup.getLastGroupMetadataSync());
    assertNull(gcGrouperSyncGroup.getErrorMessage());
    assertNull(gcGrouperSyncGroup.getErrorTimestamp());
    assertNull(gcGrouperSyncGroup.getLastGroupSync());

    Member testSubject0member = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ0, true);
    
    GcGrouperSyncMember gcGrouperSyncMember = gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberId(testSubject0member.getId());
    assertEquals(testSubject0member.getId(), gcGrouperSyncMember.getMemberId());
    assertEquals(testSubject0member.getSubjectId(), gcGrouperSyncMember.getSubjectId());
    assertEquals(testSubject0member.getSubjectSourceId(), gcGrouperSyncMember.getSourceId());
    assertEquals(testSubject0member.getSubjectIdentifier0(), gcGrouperSyncMember.getSubjectIdentifier());
    assertEquals("T", gcGrouperSyncMember.getProvisionableDb());
    assertEquals("T", gcGrouperSyncMember.getInTargetDb());
    assertEquals("F", gcGrouperSyncMember.getInTargetInsertOrExistsDb());
    assertTrue(started < gcGrouperSyncMember.getInTargetStart().getTime());
    assertTrue(System.currentTimeMillis() > gcGrouperSyncMember.getInTargetStart().getTime());
    assertNull(gcGrouperSyncMember.getInTargetEnd());
    assertTrue(started < gcGrouperSyncMember.getProvisionableStart().getTime());
    assertTrue(System.currentTimeMillis() > gcGrouperSyncMember.getProvisionableStart().getTime());
    assertNull(gcGrouperSyncMember.getProvisionableEnd());
    assertTrue(started < gcGrouperSyncMember.getLastUpdated().getTime());
    assertTrue(System.currentTimeMillis() > gcGrouperSyncMember.getLastUpdated().getTime());
    assertNull(gcGrouperSyncMember.getEntityAttributeValueCache0());
    assertNull(gcGrouperSyncMember.getEntityAttributeValueCache1());
    assertEquals("dn_test.subject.0", gcGrouperSyncMember.getEntityAttributeValueCache2());
    assertNull(gcGrouperSyncMember.getEntityAttributeValueCache3());
    assertNull(gcGrouperSyncMember.getLastUserMetadataSync());
    assertNull(gcGrouperSyncMember.getErrorMessage());
    assertNull(gcGrouperSyncMember.getErrorTimestamp());
    assertNull(gcGrouperSyncMember.getLastUserSync());

    GcGrouperSyncMembership gcGrouperSyncMembership = gcGrouperSync.getGcGrouperSyncMembershipDao().membershipRetrieveByGroupIdAndMemberId(testGroup.getId(), testSubject0member.getId());
    assertEquals("T", gcGrouperSyncMembership.getInTargetDb());
    assertEquals("T", gcGrouperSyncMembership.getInTargetInsertOrExistsDb());
    assertTrue(started < gcGrouperSyncMembership.getInTargetStart().getTime());
    assertTrue(System.currentTimeMillis() > gcGrouperSyncMembership.getInTargetStart().getTime());
    assertNull(gcGrouperSyncMembership.getInTargetEnd());
    assertTrue(started < gcGrouperSyncMembership.getLastUpdated().getTime());
    assertTrue(System.currentTimeMillis() > gcGrouperSyncMembership.getLastUpdated().getTime());
    assertNull(gcGrouperSyncMembership.getMembershipId());
    assertNull(gcGrouperSyncMembership.getMembershipId2());
    assertNull(gcGrouperSyncMembership.getErrorMessage());
    assertNull(gcGrouperSyncMembership.getErrorTimestamp());


  }


  /**
   * just do a simple full sync of groups and memberships, rename the group, find it with matching old values
   */
  public void testSimpleGroupLdapPaFullRenameMatchingOld() {
    
    long started = System.currentTimeMillis();
    
    SqlProvisionerTestUtils.configureSqlProvisioner(new SqlProvisionerTestConfigInput()
        .assignGroupDeleteType("deleteGroupsIfGrouperDeleted")
        .assignMembershipDeleteType("deleteMembershipsIfNotExistInGrouper")
        .assignEntityAttributesTable(true)
        .assignGroupAttributesTable(true)
        .assignGroupTableName("testgrouper_prov_ldap_group")
        .assignGroupTableIdColumn("uuid")
        .assignEntityTableName("testgrouper_prov_ldap_entity")
        .assignEntityTableIdColumn("entity_uuid")
        .assignHasTargetEntityLink(true)
        .assignEntityAttributeCount(3)
        .assignGroupAttributeCount(6)
        .assignProvisioningType("groupAttributes")
        .addExtraConfig("logCommandsAlways", "true")
        );
    
        
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
    attributeValue.setDoProvision("sqlProvTest");
    attributeValue.setTargetName("sqlProvTest");
    attributeValue.setStemScopeString("sub");

    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);

    // add some entities
    for (int i=0;i<10;i++) {
      String uuid = GrouperUuid.getUuid();
      String dn = "dn_test.subject." + i;
      new GcDbAccess().sql("insert into testgrouper_prov_ldap_entity (entity_uuid) values (?)").addBindVar(uuid).executeSql();
      new GcDbAccess().sql("insert into testgrouper_pro_dap_entity_attr (entity_uuid, entity_attribute_name, entity_attribute_value) values (?,?,?)")
        .addBindVar(uuid).addBindVar("dn").addBindVar(dn).executeSql();
      new GcDbAccess().sql("insert into testgrouper_pro_dap_entity_attr (entity_uuid, entity_attribute_name, entity_attribute_value) values (?,?,?)")
        .addBindVar(uuid).addBindVar("employeeId").addBindVar("test.subject." + i).executeSql();
    }
    
    
    //lets sync these over
    GrouperProvisioningOutput grouperProvisioningOutput = fullProvision();
    GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    
    // make sure some time has passed
    GrouperUtil.sleep(1000);
    
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());

    String sql = "select uuid from testgrouper_prov_ldap_group";
    
    List<Object[]> dataInTable = new GcDbAccess().sql(sql).selectList(Object[].class);
    
    Set<MultiKey> groupNamesInTable = new HashSet<MultiKey>();
    
    for (Object[] row: dataInTable) {
      groupNamesInTable.add(new MultiKey(row));
    }
    
    assertEquals(1, groupNamesInTable.size());
    assertTrue(groupNamesInTable.contains(new MultiKey(new Object[]{"test:testGroup"})));

    sql = "select group_uuid, attribute_name, attribute_value from testgrouper_pro_ldap_group_attr";
    
    dataInTable = new GcDbAccess().sql(sql).selectList(Object[].class);
    
    Set<MultiKey> attributesInTable = new HashSet<MultiKey>();
    
    for (Object[] row: dataInTable) {
      attributesInTable.add(new MultiKey(row));
    }
    
    assertEquals(6, attributesInTable.size());
    
    assertTrue(attributesInTable.contains(new MultiKey("test:testGroup", "cn", "test:testGroup")));
    assertTrue(attributesInTable.contains(new MultiKey("test:testGroup", "dn", "cn=test:testGroup,OU=Grouper,OU=365Groups,DC=one,DC=upenn,DC=edu")));
    assertTrue(attributesInTable.contains(new MultiKey("test:testGroup", "gidNumber", "" + testGroup.getIdIndex())));
    assertTrue(attributesInTable.contains(new MultiKey("test:testGroup", "objectClass", "group")));
    assertTrue(attributesInTable.contains(new MultiKey("test:testGroup", "member", "dn_test.subject.0")));
    assertTrue(attributesInTable.contains(new MultiKey("test:testGroup", "member", "dn_test.subject.1")));
    //object changes
    assertEquals(0, GrouperUtil.length(grouperProvisioner.retrieveGrouperProvisioningDataChanges().getTargetObjectInserts().getProvisioningGroups()));
    assertEquals(0, GrouperUtil.length(grouperProvisioner.retrieveGrouperProvisioningDataChanges().getTargetObjectInserts().getProvisioningEntities()));
    assertEquals(0, GrouperUtil.length(grouperProvisioner.retrieveGrouperProvisioningDataChanges().getTargetObjectInserts().getProvisioningMemberships()));
    assertEquals(1, GrouperUtil.length(grouperProvisioner.retrieveGrouperProvisioningDataChanges().getTargetObjectUpdates().getProvisioningGroups()));
    assertEquals(0, GrouperUtil.length(grouperProvisioner.retrieveGrouperProvisioningDataChanges().getTargetObjectUpdates().getProvisioningEntities()));
    assertEquals(0, GrouperUtil.length(grouperProvisioner.retrieveGrouperProvisioningDataChanges().getTargetObjectUpdates().getProvisioningMemberships()));
    assertEquals(0, GrouperUtil.length(grouperProvisioner.retrieveGrouperProvisioningDataChanges().getTargetObjectDeletes().getProvisioningGroups()));
    assertEquals(0, GrouperUtil.length(grouperProvisioner.retrieveGrouperProvisioningDataChanges().getTargetObjectDeletes().getProvisioningEntities()));
    assertEquals(0, GrouperUtil.length(grouperProvisioner.retrieveGrouperProvisioningDataChanges().getTargetObjectDeletes().getProvisioningMemberships()));
    
    // field changes
    assertEquals(2, GrouperUtil.length(grouperProvisioner.retrieveGrouperProvisioningDataChanges().getTargetObjectUpdates().getProvisioningGroups().iterator().next().getInternal_objectChanges()));
    List<ProvisioningObjectChange> provisioningObjectChanges = new ArrayList<ProvisioningObjectChange>(
        grouperProvisioner.retrieveGrouperProvisioningDataChanges().getTargetObjectUpdates().getProvisioningGroups().iterator().next().getInternal_objectChanges());
    assertEquals(ProvisioningObjectChangeAction.insert, provisioningObjectChanges.get(0).getProvisioningObjectChangeAction());
    assertEquals(ProvisioningObjectChangeAction.insert, provisioningObjectChanges.get(1).getProvisioningObjectChangeAction());
    assertEquals("member", provisioningObjectChanges.get(0).getAttributeName());
    assertEquals("member", provisioningObjectChanges.get(1).getAttributeName());

    //get the grouper_sync and check cols
    GcGrouperSync gcGrouperSync = GcGrouperSyncDao.retrieveByProvisionerName(null, "sqlProvTest");
    
    GcGrouperSyncGroup gcGrouperSyncGroup = gcGrouperSync.getGcGrouperSyncGroupDao().groupRetrieveByGroupId(testGroup.getId());
    assertEquals("cn=test:testGroup,OU=Grouper,OU=365Groups,DC=one,DC=upenn,DC=edu", gcGrouperSyncGroup.getGroupAttributeValueCache2());

    Member testSubject0member = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ0, true);
    
    GcGrouperSyncMember gcGrouperSyncMember = gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberId(testSubject0member.getId());
    assertEquals("dn_test.subject.0", gcGrouperSyncMember.getEntityAttributeValueCache2());

    new GroupSave().assignGroupNameToEdit(testGroup.getName()).assignName(testGroup.getName() + "New").save();

    //lets sync these over
    grouperProvisioningOutput = fullProvision();
    grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    
    // make sure some time has passed
    GrouperUtil.sleep(1000);
    
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());

    sql = "select uuid from testgrouper_prov_ldap_group";
    
    dataInTable = new GcDbAccess().sql(sql).selectList(Object[].class);
    
    groupNamesInTable = new HashSet<MultiKey>();
    
    for (Object[] row: dataInTable) {
      groupNamesInTable.add(new MultiKey(row));
    }
    
    assertEquals(1, groupNamesInTable.size());
    assertTrue(groupNamesInTable.contains(new MultiKey(new Object[]{"test:testGroupNew"})));

    sql = "select group_uuid, attribute_name, attribute_value from testgrouper_pro_ldap_group_attr";
    
    dataInTable = new GcDbAccess().sql(sql).selectList(Object[].class);
    
    attributesInTable = new HashSet<MultiKey>();
    
    for (Object[] row: dataInTable) {
      attributesInTable.add(new MultiKey(row));
    }
    
    assertEquals(6, attributesInTable.size());
    
    assertTrue(attributesInTable.contains(new MultiKey("test:testGroupNew", "cn", "test:testGroupNew")));
    assertTrue(attributesInTable.contains(new MultiKey("test:testGroupNew", "dn", "cn=test:testGroupNew,OU=Grouper,OU=365Groups,DC=one,DC=upenn,DC=edu")));
    assertTrue(attributesInTable.contains(new MultiKey("test:testGroupNew", "gidNumber", "" + testGroup.getIdIndex())));
    assertTrue(attributesInTable.contains(new MultiKey("test:testGroupNew", "objectClass", "group")));
    assertTrue(attributesInTable.contains(new MultiKey("test:testGroupNew", "member", "dn_test.subject.0")));
    assertTrue(attributesInTable.contains(new MultiKey("test:testGroupNew", "member", "dn_test.subject.1")));
    //object changes
    assertEquals(0, GrouperUtil.length(grouperProvisioner.retrieveGrouperProvisioningDataChanges().getTargetObjectInserts().getProvisioningGroups()));
    assertEquals(0, GrouperUtil.length(grouperProvisioner.retrieveGrouperProvisioningDataChanges().getTargetObjectInserts().getProvisioningEntities()));
    assertEquals(0, GrouperUtil.length(grouperProvisioner.retrieveGrouperProvisioningDataChanges().getTargetObjectInserts().getProvisioningMemberships()));
    assertEquals(1, GrouperUtil.length(grouperProvisioner.retrieveGrouperProvisioningDataChanges().getTargetObjectUpdates().getProvisioningGroups()));
    assertEquals(0, GrouperUtil.length(grouperProvisioner.retrieveGrouperProvisioningDataChanges().getTargetObjectUpdates().getProvisioningEntities()));
    assertEquals(0, GrouperUtil.length(grouperProvisioner.retrieveGrouperProvisioningDataChanges().getTargetObjectUpdates().getProvisioningMemberships()));
    assertEquals(0, GrouperUtil.length(grouperProvisioner.retrieveGrouperProvisioningDataChanges().getTargetObjectDeletes().getProvisioningGroups()));
    assertEquals(0, GrouperUtil.length(grouperProvisioner.retrieveGrouperProvisioningDataChanges().getTargetObjectDeletes().getProvisioningEntities()));
    assertEquals(0, GrouperUtil.length(grouperProvisioner.retrieveGrouperProvisioningDataChanges().getTargetObjectDeletes().getProvisioningMemberships()));
    
    // field changes
    assertEquals(3, GrouperUtil.length(grouperProvisioner.retrieveGrouperProvisioningDataChanges().getTargetObjectUpdates().getProvisioningGroups().iterator().next().getInternal_objectChanges()));
    provisioningObjectChanges = new ArrayList<ProvisioningObjectChange>(
        grouperProvisioner.retrieveGrouperProvisioningDataChanges().getTargetObjectUpdates().getProvisioningGroups().iterator().next().getInternal_objectChanges());
    assertEquals(ProvisioningObjectChangeAction.update, provisioningObjectChanges.get(0).getProvisioningObjectChangeAction());
    assertEquals(ProvisioningObjectChangeAction.update, provisioningObjectChanges.get(1).getProvisioningObjectChangeAction());
    assertEquals(ProvisioningObjectChangeAction.update, provisioningObjectChanges.get(2).getProvisioningObjectChangeAction());

    //get the grouper_sync and check cols
    gcGrouperSync = GcGrouperSyncDao.retrieveByProvisionerName(null, "sqlProvTest");
    
    gcGrouperSyncGroup = gcGrouperSync.getGcGrouperSyncGroupDao().groupRetrieveByGroupId(testGroup.getId());
    assertEquals("cn=test:testGroupNew,OU=Grouper,OU=365Groups,DC=one,DC=upenn,DC=edu", gcGrouperSyncGroup.getGroupAttributeValueCache2());

    testSubject0member = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ0, true);
    
    gcGrouperSyncMember = gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberId(testSubject0member.getId());
    assertEquals("dn_test.subject.0", gcGrouperSyncMember.getEntityAttributeValueCache2());

  }


  public void configureLdapPaTestCase() {
    SqlProvisionerTestUtils.configureSqlProvisioner(new SqlProvisionerTestConfigInput()
        .assignGroupDeleteType("deleteGroupsIfGrouperDeleted")
        .assignMembershipDeleteType("deleteMembershipsIfNotExistInGrouper")
        .assignEntityAttributesTable(true)
        .assignGroupAttributesTable(true)
        .assignGroupTableName("testgrouper_prov_ldap_group")
        .assignGroupTableIdColumn("uuid")
        .assignEntityTableName("testgrouper_prov_ldap_entity")
        .assignEntityTableIdColumn("entity_uuid")
        .assignHasTargetEntityLink(true)
        .assignEntityAttributeCount(3)
        .assignGroupAttributeCount(6)
        .assignProvisioningType("groupAttributes")
        );

  }
  

  /**
   * just do a simple full sync of groups and memberships
   */
  public void testSimpleGroupLdapPaRealTime() {
    
    long started = System.currentTimeMillis();

    configureLdapPaTestCase();

    Stem stem = new StemSave(this.grouperSession).assignName("test").save();
    Stem stem2 = new StemSave(this.grouperSession).assignName("test2").save();
    Stem stem3 = new StemSave(this.grouperSession).assignName("test3").save();
    Stem stem4 = new StemSave(this.grouperSession).assignName("test4").save();
    
    // mark some folders to provision
    Group testGroup = new GroupSave(this.grouperSession).assignName("test:testGroup").save();
    Group testGroup2 = new GroupSave(this.grouperSession).assignName("test2:testGroup2").save();
    Group testGroup3 = new GroupSave(this.grouperSession).assignName("test3:testGroup3").save();
    Group testGroup4 = new GroupSave(this.grouperSession).assignName("test4:testGroup4").save();
    
    testGroup.addMember(SubjectTestHelper.SUBJ0, false);
    testGroup.addMember(SubjectTestHelper.SUBJ1, false);
  
    testGroup2.addMember(SubjectTestHelper.SUBJ2, false);
    testGroup2.addMember(SubjectTestHelper.SUBJ3, false);
  
    testGroup3.addMember(SubjectTestHelper.SUBJ4, false);
    testGroup3.addMember(SubjectTestHelper.SUBJ5, false);
  
    testGroup4.addMember(SubjectTestHelper.SUBJ6, false);
    testGroup4.addMember(SubjectTestHelper.SUBJ7, false);

    GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision("sqlProvTest");
    attributeValue.setTargetName("sqlProvTest");
    attributeValue.setStemScopeString("sub");

    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);

    // add some entities
    for (int i=0;i<10;i++) {
      String uuid = GrouperUuid.getUuid();
      String dn = "dn_test.subject." + i;
      new GcDbAccess().sql("insert into testgrouper_prov_ldap_entity (entity_uuid) values (?)").addBindVar(uuid).executeSql();
      new GcDbAccess().sql("insert into testgrouper_pro_dap_entity_attr (entity_uuid, entity_attribute_name, entity_attribute_value) values (?,?,?)")
        .addBindVar(uuid).addBindVar("dn").addBindVar(dn).executeSql();
      new GcDbAccess().sql("insert into testgrouper_pro_dap_entity_attr (entity_uuid, entity_attribute_name, entity_attribute_value) values (?,?,?)")
        .addBindVar(uuid).addBindVar("employeeId").addBindVar("test.subject." + i).executeSql();
    }
    
    
    //AttributeAssign attributeAssign = stem.getAttributeDelegate().addAttribute(GrouperProvisioningAttributeNames.retrieveAttributeDefNameMarker()).getAttributeAssign();
    //attributeAssign.getAttributeValueDelegate().assignValueString(GrouperProvisioningAttributeNames.retrieveAttributeDefNameDoProvision())
    
    //lets sync these over
    GrouperProvisioningOutput grouperProvisioningOutput = fullProvision();
    GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());

    String sql = "select uuid from testgrouper_prov_ldap_group";
    
    List<Object[]> dataInTable = new GcDbAccess().sql(sql).selectList(Object[].class);
    
    Set<MultiKey> groupNamesInTable = new HashSet<MultiKey>();
    
    for (Object[] row: dataInTable) {
      groupNamesInTable.add(new MultiKey(row));
    }
    
    assertEquals(1, groupNamesInTable.size());
    assertTrue(groupNamesInTable.contains(new MultiKey(new Object[]{"test:testGroup"})));

    sql = "select group_uuid, attribute_name, attribute_value from testgrouper_pro_ldap_group_attr";
    
    dataInTable = new GcDbAccess().sql(sql).selectList(Object[].class);
    
    Set<MultiKey> attributesInTable = new HashSet<MultiKey>();
    
    for (Object[] row: dataInTable) {
      attributesInTable.add(new MultiKey(row));
    }
    
    assertEquals(6, attributesInTable.size());
    
    assertTrue(attributesInTable.contains(new MultiKey("test:testGroup", "cn", "test:testGroup")));
    assertTrue(attributesInTable.contains(new MultiKey("test:testGroup", "dn", "cn=test:testGroup,OU=Grouper,OU=365Groups,DC=one,DC=upenn,DC=edu")));
    assertTrue(attributesInTable.contains(new MultiKey("test:testGroup", "gidNumber", "" + testGroup.getIdIndex())));
    assertTrue(attributesInTable.contains(new MultiKey("test:testGroup", "objectClass", "group")));
    assertTrue(attributesInTable.contains(new MultiKey("test:testGroup", "member", "dn_test.subject.0")));
    assertTrue(attributesInTable.contains(new MultiKey("test:testGroup", "member", "dn_test.subject.1")));
    
    Hib3GrouperLoaderLog hib3GrouperLoaderLog = null;
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("changeLog.consumer." + JOB_NAME + ".class").value(EsbConsumer.class.getName()).store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("changeLog.consumer." + JOB_NAME + ".publisher.class").value(ProvisioningConsumer.class.getName()).store();
    
    //something that will never fire
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("changeLog.consumer." + JOB_NAME + ".quartzCron").value("0 0 0 1 1 ? 2200").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("changeLog.consumer." + JOB_NAME + ".provisionerConfigId").value("sqlProvTest").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("changeLog.consumer." + JOB_NAME + ".provisionerJobSyncType").value(GrouperProvisioningType.incrementalProvisionChangeLog.name()).store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("changeLog.consumer." + JOB_NAME + ".publisher.debug").value("true").store();
    
    //clear out changelog
    // run the provisioner, it will init
    hib3GrouperLoaderLog = incrementalProvision();
    
    // add 4
    attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision("sqlProvTest");
    attributeValue.setTargetName("sqlProvTest");

    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem4);
  
    // remove 3:  note, its not on 3 so im not sure what this is doing
    stem3.getAttributeDelegate().removeAttribute(GrouperProvisioningAttributeNames.retrieveAttributeDefNameMarker());
 
    GrouperProvisioningJob.runDaemonStandalone();
    // add member, remove member
    testGroup.addMember(SubjectTestHelper.SUBJ8, false);
    testGroup.deleteMember(SubjectTestHelper.SUBJ1, false);

    //make sure unexpected events are handled
    new AttributeDefSave(grouperSession).assignName("test:whateverDef").assignCreateParentStemsIfNotExist(true).save();
    
    // run the provisioner
    hib3GrouperLoaderLog = incrementalProvision();

    assertEquals("SUCCESS", hib3GrouperLoaderLog.getStatus());
    
    //TODO add in all counts and look in target and make sure right
    // this includes fields and attributes etc
    assertEquals(1, GrouperUtil.intValue(hib3GrouperLoaderLog.getDeleteCount(), -1));


  }

  /**
   * just do a simple full sync of groups and memberships
   */
  public void testSimpleGroupLdapPaRealTimeAddMember() {
    
    long started = System.currentTimeMillis();

    configureLdapPaTestCase();

    Stem stem = new StemSave(this.grouperSession).assignName("test").save();
    Stem stem2 = new StemSave(this.grouperSession).assignName("test2").save();
    Stem stem3 = new StemSave(this.grouperSession).assignName("test3").save();
    Stem stem4 = new StemSave(this.grouperSession).assignName("test4").save();
    
    // mark some folders to provision
    Group testGroup = new GroupSave(this.grouperSession).assignName("test:testGroup").save();
    Group testGroup2 = new GroupSave(this.grouperSession).assignName("test2:testGroup2").save();
    Group testGroup3 = new GroupSave(this.grouperSession).assignName("test3:testGroup3").save();
    Group testGroup4 = new GroupSave(this.grouperSession).assignName("test4:testGroup4").save();
    
    testGroup.addMember(SubjectTestHelper.SUBJ0, false);
    testGroup.addMember(SubjectTestHelper.SUBJ1, false);
  
    testGroup2.addMember(SubjectTestHelper.SUBJ2, false);
    testGroup2.addMember(SubjectTestHelper.SUBJ3, false);
  
    testGroup3.addMember(SubjectTestHelper.SUBJ4, false);
    testGroup3.addMember(SubjectTestHelper.SUBJ5, false);
  
    testGroup4.addMember(SubjectTestHelper.SUBJ6, false);
    testGroup4.addMember(SubjectTestHelper.SUBJ7, false);

    final GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision("sqlProvTest");
    attributeValue.setTargetName("sqlProvTest");
    attributeValue.setStemScopeString("sub");

    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);

    // add some entities
    for (int i=0;i<10;i++) {
      String uuid = GrouperUuid.getUuid();
      String dn = "dn_test.subject." + i;
      new GcDbAccess().sql("insert into testgrouper_prov_ldap_entity (entity_uuid) values (?)").addBindVar(uuid).executeSql();
      new GcDbAccess().sql("insert into testgrouper_pro_dap_entity_attr (entity_uuid, entity_attribute_name, entity_attribute_value) values (?,?,?)")
        .addBindVar(uuid).addBindVar("dn").addBindVar(dn).executeSql();
      new GcDbAccess().sql("insert into testgrouper_pro_dap_entity_attr (entity_uuid, entity_attribute_name, entity_attribute_value) values (?,?,?)")
        .addBindVar(uuid).addBindVar("employeeId").addBindVar("test.subject." + i).executeSql();
    }
    
    
    //AttributeAssign attributeAssign = stem.getAttributeDelegate().addAttribute(GrouperProvisioningAttributeNames.retrieveAttributeDefNameMarker()).getAttributeAssign();
    //attributeAssign.getAttributeValueDelegate().assignValueString(GrouperProvisioningAttributeNames.retrieveAttributeDefNameDoProvision())
    
    //lets sync these over
    GrouperProvisioningOutput grouperProvisioningOutput = fullProvision();
    GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    GrouperUtil.sleep(500);
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());

    assertLdapGroupNamesInTable(GrouperUtil.toSet("test:testGroup"));

    assertLdapAttributesInTable(GrouperUtil.toSet(
        new MultiKey("test:testGroup", "cn", "test:testGroup"),
        new MultiKey("test:testGroup", "dn", "cn=test:testGroup,OU=Grouper,OU=365Groups,DC=one,DC=upenn,DC=edu"),
        new MultiKey("test:testGroup", "gidNumber", "" + testGroup.getIdIndex()),
        new MultiKey("test:testGroup", "objectClass", "group"),
        new MultiKey("test:testGroup", "member", "dn_test.subject.0"),
        new MultiKey("test:testGroup", "member", "dn_test.subject.1")));
    
    GcGrouperSync gcGrouperSync = GcGrouperSyncDao.retrieveByProvisionerName(null, "sqlProvTest");

    GcGrouperSyncGroup gcGrouperSyncGroup = gcGrouperSync.getGcGrouperSyncGroupDao().groupRetrieveByGroupId(testGroup.getId());
    assertEquals(testGroup.getId(), gcGrouperSyncGroup.getGroupId());
    assertEquals(testGroup.getName(), gcGrouperSyncGroup.getGroupName());
    assertEquals(testGroup.getIdIndex(), gcGrouperSyncGroup.getGroupIdIndex());
    assertEquals("T", gcGrouperSyncGroup.getProvisionableDb());
    assertEquals("T", gcGrouperSyncGroup.getInTargetDb());
    assertEquals("T", gcGrouperSyncGroup.getInTargetInsertOrExistsDb());
    assertTrue(started < gcGrouperSyncGroup.getInTargetStart().getTime());
    assertTrue(System.currentTimeMillis() > gcGrouperSyncGroup.getInTargetStart().getTime());
    assertNull(gcGrouperSyncGroup.getInTargetEnd());
    assertTrue(started < gcGrouperSyncGroup.getProvisionableStart().getTime());
    assertEquals("cn=test:testGroup,OU=Grouper,OU=365Groups,DC=one,DC=upenn,DC=edu", gcGrouperSyncGroup.getGroupAttributeValueCache2());
    
    Hib3GrouperLoaderLog hib3GrouperLoaderLog = null;
    
    //clear out changelog
    // run the provisioner, it will init
    hib3GrouperLoaderLog = incrementalProvision();
    
    assertLdapGroupNamesInTable(GrouperUtil.toSet("test:testGroup"));

    assertLdapAttributesInTable(GrouperUtil.toSet(
        new MultiKey("test:testGroup", "cn", "test:testGroup"),
        new MultiKey("test:testGroup", "dn", "cn=test:testGroup,OU=Grouper,OU=365Groups,DC=one,DC=upenn,DC=edu"),
        new MultiKey("test:testGroup", "gidNumber", "" + testGroup.getIdIndex()),
        new MultiKey("test:testGroup", "objectClass", "group"),
        new MultiKey("test:testGroup", "member", "dn_test.subject.0"),
        new MultiKey("test:testGroup", "member", "dn_test.subject.1")));
    

//    ProvisioningConsumer provisioningConsumer = (ProvisioningConsumer)this.esbConsumer.getEsbPublisherBase();
//
//    grouperProvisioner = provisioningConsumer.getGrouperProvisioner();
    
    grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    GrouperUtil.sleep(500);
    
    assertEquals(0, GrouperUtil.intValue(grouperProvisioner.getDebugMap().get("messageCountForProvisioner"), 0));
    
    assertFalse((Boolean)grouperProvisioner.getDebugMap().get("hasIncrementalDataToProcess"));
    
    // add member
    testGroup.addMember(SubjectTestHelper.SUBJ8, false);

    // run the provisioner
    hib3GrouperLoaderLog = incrementalProvision();

//    provisioningConsumer = (ProvisioningConsumer)this.esbConsumer.getEsbPublisherBase();
//
//    grouperProvisioner = provisioningConsumer.getGrouperProvisioner();
    grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    GrouperUtil.sleep(500);
    
    assertEquals(0, GrouperUtil.intValue(grouperProvisioner.getDebugMap().get("missingGroupsForCreate"), 0));

    assertEquals("SUCCESS", hib3GrouperLoaderLog.getStatus());

    assertLdapGroupNamesInTable(GrouperUtil.toSet("test:testGroup"));

    assertLdapAttributesInTable(GrouperUtil.toSet(
        new MultiKey("test:testGroup", "cn", "test:testGroup"),
        new MultiKey("test:testGroup", "dn", "cn=test:testGroup,OU=Grouper,OU=365Groups,DC=one,DC=upenn,DC=edu"),
        new MultiKey("test:testGroup", "gidNumber", "" + testGroup.getIdIndex()),
        new MultiKey("test:testGroup", "objectClass", "group"),
        new MultiKey("test:testGroup", "member", "dn_test.subject.0"),
        new MultiKey("test:testGroup", "member", "dn_test.subject.1"),
        new MultiKey("test:testGroup", "member", "dn_test.subject.8"))        
        );

    // this includes fields and attributes etc
    assertEquals(1, GrouperUtil.intValue(hib3GrouperLoaderLog.getInsertCount(), -1));

    // remove member
    testGroup.deleteMember(SubjectTestHelper.SUBJ1, false);

    // run the provisioner
    hib3GrouperLoaderLog = incrementalProvision();

//    provisioningConsumer = (ProvisioningConsumer)this.esbConsumer.getEsbPublisherBase();
//
//    grouperProvisioner = provisioningConsumer.getGrouperProvisioner();
    grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    GrouperUtil.sleep(500);

    assertEquals("SUCCESS", hib3GrouperLoaderLog.getStatus());

    assertLdapGroupNamesInTable(GrouperUtil.toSet("test:testGroup"));

    assertLdapAttributesInTable(GrouperUtil.toSet(
        new MultiKey("test:testGroup", "cn", "test:testGroup"),
        new MultiKey("test:testGroup", "dn", "cn=test:testGroup,OU=Grouper,OU=365Groups,DC=one,DC=upenn,DC=edu"),
        new MultiKey("test:testGroup", "gidNumber", "" + testGroup.getIdIndex()),
        new MultiKey("test:testGroup", "objectClass", "group"),
        new MultiKey("test:testGroup", "member", "dn_test.subject.0"),
        new MultiKey("test:testGroup", "member", "dn_test.subject.8"))        
        );

    // this includes fields and attributes etc
    assertEquals(1, GrouperUtil.intValue(hib3GrouperLoaderLog.getDeleteCount(), -1));
  }

  private void assertLdapAttributesInTable(Set<MultiKey> set) {
    String sql = "select group_uuid, attribute_name, attribute_value from testgrouper_pro_ldap_group_attr";
    
    List<Object[]> dataInTable = new GcDbAccess().sql(sql).selectList(Object[].class);
    
    Set<MultiKey> attributesInTable = new HashSet<MultiKey>();
    
    for (Object[] row: dataInTable) {
      attributesInTable.add(new MultiKey(row));
    }
    
    String errorMessage = "Expected: " + GrouperUtil.setToString(set) + "      ,     Actual: " + GrouperUtil.setToString(attributesInTable);

    assertEquals(errorMessage,set.size(), attributesInTable.size());
    for (MultiKey name: set) {
      if (!attributesInTable.contains(name)) {
        assertTrue(errorMessage, false);
      }
    }

  }
  
  private void assertLdapGroupNamesInTable(Set<String> set) {
    String sql = "select uuid from testgrouper_prov_ldap_group";

    List<String> dataInTable = new GcDbAccess().sql(sql).selectList(String.class);
    
    Set<String> groupNamesInTable = new HashSet<String>();
    
    for (String row: dataInTable) {
      groupNamesInTable.add(row);
    }
    
    String errorMessage = "Expected: " + GrouperUtil.setToString(set) + "      ,     Actual: " + GrouperUtil.setToString(groupNamesInTable);
    assertEquals(errorMessage,set.size(), groupNamesInTable.size());
    for (String name: set) {
      if (!groupNamesInTable.contains(name)) {
        assertTrue(errorMessage, false);
      }
    }
    
  }

  private void setupFailsafeJob() {

    SqlProvisionerTestUtils.configureSqlProvisioner(new SqlProvisionerTestConfigInput()
        .assignEntityDeleteType("deleteEntitiesIfNotExistInGrouper")
        .assignGroupDeleteType("deleteGroupsIfNotExistInGrouper")
        .assignMembershipDeleteType("deleteMembershipsIfNotExistInGrouper")
        .assignEntityAttributesTable(true)
        .assignGroupAttributesTable(true)
        .assignGroupTableName("testgrouper_prov_group")
        .assignGroupTableIdColumn("uuid")
        .assignEntityTableName("testgrouper_prov_entity")
        .assignEntityTableIdColumn("uuid")
        .assignMembershipTableName("testgrouper_prov_mship2")
        .assignMembershipTableIdColumn("uuid")
        .assignMembershipGroupForeignKeyColumn("group_uuid")
        .assignMembershipEntityForeignKeyColumn("entity_uuid")
        .assignHasTargetEntityLink(true)
        .assignHasTargetGroupLink(true)
        .assignEntityAttributeCount(5)
        .assignGroupAttributeCount(4)
        .assignMembershipAttributeCount(3)
        .assignFailsafeDefaults(true)
        );
    
    // this closes the grouper session
    GrouperLoader.scheduleJobs();     
    this.grouperSession = GrouperSession.startRootSession();

    Stem stem = new StemSave(this.grouperSession).assignName("test").save();

    // mark some folders to provision
    for (int i=0;i<10;i++) {
      failsafeGroups.add(new GroupSave(this.grouperSession).assignName("test:testGroup" + i).assignDescription("testDescription" + i).save());
      
      failsafeGroups.get(i).addMember(SubjectTestHelper.SUBJ0, false);
      failsafeGroups.get(i).addMember(SubjectTestHelper.SUBJ1, false);
      failsafeGroups.get(i).addMember(SubjectTestHelper.SUBJ2, false);
      failsafeGroups.get(i).addMember(SubjectTestHelper.SUBJ3, false);
      failsafeGroups.get(i).addMember(SubjectTestHelper.SUBJ4, false);
      failsafeGroups.get(i).addMember(SubjectTestHelper.SUBJ5, false);
      failsafeGroups.get(i).addMember(SubjectTestHelper.SUBJ6, false);
      failsafeGroups.get(i).addMember(SubjectTestHelper.SUBJ7, false);
      failsafeGroups.get(i).addMember(SubjectTestHelper.SUBJ8, false);
      failsafeGroups.get(i).addMember(SubjectTestHelper.SUBJ9, false);

    }
    
    final GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision("sqlProvTest");
    attributeValue.setTargetName("sqlProvTest");
    attributeValue.setStemScopeString("sub");
  
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);

  }
  
  private List<Group> failsafeGroups = new ArrayList<Group>();
  
  public void testSimpleGroupMembershipProvisioningFullWithAttributesTableFailsafe() {
    
    setupFailsafeJob();
    
    //AttributeAssign attributeAssign = stem.getAttributeDelegate().addAttribute(GrouperProvisioningAttributeNames.retrieveAttributeDefNameMarker()).getAttributeAssign();
    //attributeAssign.getAttributeValueDelegate().assignValueString(GrouperProvisioningAttributeNames.retrieveAttributeDefNameDoProvision())
    
    //lets sync these over
    String jobName = "OTHER_JOB_provisioner_full_sqlProvTest";
    
    fullProvision();
    
    assertFalse(GrouperFailsafe.isFailsafeIssue(jobName));
    
    List<Object[]> groups = new GcDbAccess().sql("select uuid, posix_id, name from testgrouper_prov_group order by name").selectList(Object[].class);
    assertEquals(10, groups.size());
    
    List<Object[]> entities = new GcDbAccess().sql("select uuid, name, subject_id_or_identifier, description from testgrouper_prov_entity order by name").selectList(Object[].class);
    assertEquals(10, entities.size());
    
    List<Object[]> memberships = new GcDbAccess().sql("select group_uuid, entity_uuid from testgrouper_prov_mship2").selectList(Object[].class);
    assertEquals(100, memberships.size());
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.sqlProvTest.failsafeMinOverallNumberOfMembers").value("85").store();
    
    for (int i=0;i<5;i++) {
      failsafeGroups.get(i).delete();
    }
    try {
      GrouperLoader.runOnceByJobName(this.grouperSession, jobName);
      fail();
    } catch(Exception e) {
    }
    
    assertTrue(GrouperFailsafe.isFailsafeIssue(jobName));

    Hib3GrouperLoaderLog hib3GrouperLoaderLog = Hib3GrouperLoaderLog.retrieveMostRecentLog(jobName);
    assertEquals("ERROR_FAILSAFE", hib3GrouperLoaderLog.getStatus());
    
    groups = new GcDbAccess().sql("select uuid, posix_id, name from testgrouper_prov_group order by name").selectList(Object[].class);
    assertEquals(10, groups.size());
    
    entities = new GcDbAccess().sql("select uuid, name, subject_id_or_identifier, description from testgrouper_prov_entity order by name").selectList(Object[].class);
    assertEquals(10, entities.size());
    
    memberships = new GcDbAccess().sql("select group_uuid, entity_uuid from testgrouper_prov_mship2").selectList(Object[].class);
    assertEquals(100, memberships.size());
    
    GrouperUtil.sleep(1000);
    
    assertFalse(GrouperFailsafe.isApproved(jobName));
    GrouperFailsafe.assignApproveNextRun(jobName);
    assertTrue(GrouperFailsafe.isApproved(jobName));

    GrouperLoader.runOnceByJobName(this.grouperSession, jobName);

    hib3GrouperLoaderLog = Hib3GrouperLoaderLog.retrieveMostRecentLog(jobName);
    assertEquals("SUCCESS", hib3GrouperLoaderLog.getStatus());
    assertFalse(GrouperFailsafe.isFailsafeIssue(jobName));

    memberships = new GcDbAccess().sql("select group_uuid, entity_uuid from testgrouper_prov_mship2").selectList(Object[].class);
    
    assertEquals(50, memberships.size());
    
    groups = new GcDbAccess().sql("select uuid, posix_id, name from testgrouper_prov_group order by name").selectList(Object[].class);
    assertEquals(5, groups.size());
    
    entities = new GcDbAccess().sql("select uuid, name, subject_id_or_identifier, description from testgrouper_prov_entity order by name").selectList(Object[].class);
    assertEquals(10, entities.size());
    
    memberships = new GcDbAccess().sql("select group_uuid, entity_uuid from testgrouper_prov_mship2").selectList(Object[].class);
    assertEquals(50, memberships.size());

  }

  public void testSimpleGroupMembershipProvisioningFullWithAttributesTableRequiredMembers() {
    
    SqlProvisionerTestUtils.configureSqlProvisioner(new SqlProvisionerTestConfigInput()
        .assignGroupDeleteType("deleteGroupsIfNotExistInGrouper")
        .assignGroupTableName("testgrouper_prov_group")
        .assignGroupTableIdColumn("uuid")
        .assignGroupAttributeCount(1)
        .addExtraConfig("group2advanced", "true")
        .addExtraConfig("groupsRequireMembers", "true")
        .assignOperateOnGrouperMemberships(false)
        .assignProvisioningType(null)
        );

    // this closes the grouper session
    GrouperLoader.scheduleJobs();     
    this.grouperSession = GrouperSession.startRootSession();

    Stem stem = new StemSave(this.grouperSession).assignName("test").save();

    // mark some folders to provision
    for (int i=0;i<10;i++) {
      failsafeGroups.add(new GroupSave(this.grouperSession).assignName("test:testGroup" + i).assignDescription("testDescription" + i).save());
      
      failsafeGroups.get(i).addMember(SubjectTestHelper.SUBJ0, false);

    }

    // group with no members
    failsafeGroups.add(new GroupSave(this.grouperSession).assignName("test:testGroup" + 10).assignDescription("testDescription" + 10).save());
    
    final GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision("sqlProvTest");
    attributeValue.setTargetName("sqlProvTest");
    attributeValue.setStemScopeString("sub");
  
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);
    
    //lets sync these over
    String jobName = "OTHER_JOB_provisioner_full_sqlProvTest";
    
    fullProvision();
    
    List<Object[]> groups = new GcDbAccess().sql("select uuid from testgrouper_prov_group order by name").selectList(Object[].class);
    assertEquals(10, groups.size());
    
    for (int i=0;i<5;i++) {
      failsafeGroups.get(i).deleteMember(SubjectTestHelper.SUBJ0, true);
    }
    GrouperLoader.runOnceByJobName(this.grouperSession, jobName);
    
    Hib3GrouperLoaderLog hib3GrouperLoaderLog = Hib3GrouperLoaderLog.retrieveMostRecentLog(jobName);
    assertEquals("SUCCESS", hib3GrouperLoaderLog.getStatus());
    
    groups = new GcDbAccess().sql("select uuid from testgrouper_prov_group order by name").selectList(Object[].class);
    assertEquals(5, groups.size());
    
  }

  public void testSimpleGroupMembershipProvisioningFullWithAttributesTableFailsafeMinGroupSize() {
    
    setupFailsafeJob();
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.sqlProvTest.failsafeMinGroupSize").value("8").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.sqlProvTest.failsafeMaxPercentRemove").value("20").store();
    
    //lets sync these over
    String jobName = "OTHER_JOB_provisioner_full_sqlProvTest";
    
    fullProvision();
    
    assertFalse(GrouperFailsafe.isFailsafeIssue(jobName));
    
    List<Object[]> groups = new GcDbAccess().sql("select uuid, posix_id, name from testgrouper_prov_group order by name").selectList(Object[].class);
    assertEquals(10, groups.size());
    
    List<Object[]> entities = new GcDbAccess().sql("select uuid, name, subject_id_or_identifier, description from testgrouper_prov_entity order by name").selectList(Object[].class);
    assertEquals(10, entities.size());
    
    List<Object[]> memberships = new GcDbAccess().sql("select group_uuid, entity_uuid from testgrouper_prov_mship2").selectList(Object[].class);
    assertEquals(100, memberships.size());

    failsafeGroups.get(0).deleteMember(SubjectTestHelper.SUBJ0, false);
    failsafeGroups.get(0).deleteMember(SubjectTestHelper.SUBJ1, false);
    failsafeGroups.get(0).deleteMember(SubjectTestHelper.SUBJ2, false);
    failsafeGroups.get(0).deleteMember(SubjectTestHelper.SUBJ3, false);
    failsafeGroups.get(0).deleteMember(SubjectTestHelper.SUBJ4, false);

    try {
      GrouperLoader.runOnceByJobName(this.grouperSession, jobName);
      fail();
    } catch(Exception e) {
    }
    
    assertTrue(GrouperFailsafe.isFailsafeIssue(jobName));
  
    Hib3GrouperLoaderLog hib3GrouperLoaderLog = Hib3GrouperLoaderLog.retrieveMostRecentLog(jobName);
    assertEquals("ERROR_FAILSAFE", hib3GrouperLoaderLog.getStatus());

    groups = new GcDbAccess().sql("select uuid, posix_id, name from testgrouper_prov_group order by name").selectList(Object[].class);
    assertEquals(10, groups.size());
    
    entities = new GcDbAccess().sql("select uuid, name, subject_id_or_identifier, description from testgrouper_prov_entity order by name").selectList(Object[].class);
    assertEquals(10, entities.size());
    
    memberships = new GcDbAccess().sql("select group_uuid, entity_uuid from testgrouper_prov_mship2").selectList(Object[].class);
    assertEquals(100, memberships.size());
    
    GrouperUtil.sleep(1000);
    
    assertFalse(GrouperFailsafe.isApproved(jobName));
    GrouperFailsafe.removeFailure(jobName);

    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.sqlProvTest.failsafeMinGroupSize").value("15").store();

    GrouperLoader.runOnceByJobName(this.grouperSession, jobName);

    hib3GrouperLoaderLog = Hib3GrouperLoaderLog.retrieveMostRecentLog(jobName);
    assertEquals("SUCCESS", hib3GrouperLoaderLog.getStatus());
    assertFalse(GrouperFailsafe.isFailsafeIssue(jobName));
  
    groups = new GcDbAccess().sql("select uuid, posix_id, name from testgrouper_prov_group order by name").selectList(Object[].class);
    assertEquals(10, groups.size());
    
    entities = new GcDbAccess().sql("select uuid, name, subject_id_or_identifier, description from testgrouper_prov_entity order by name").selectList(Object[].class);
    assertEquals(10, entities.size());
    
    memberships = new GcDbAccess().sql("select group_uuid, entity_uuid from testgrouper_prov_mship2").selectList(Object[].class);
    assertEquals(95, memberships.size());
  
  }

  public void testSimpleGroupMembershipProvisioningFullWithAttributesTableFailsafe2() {
    
    setupFailsafeJob();
    
    //AttributeAssign attributeAssign = stem.getAttributeDelegate().addAttribute(GrouperProvisioningAttributeNames.retrieveAttributeDefNameMarker()).getAttributeAssign();
    //attributeAssign.getAttributeValueDelegate().assignValueString(GrouperProvisioningAttributeNames.retrieveAttributeDefNameDoProvision())
    
    //lets sync these over
    String jobName = "OTHER_JOB_provisioner_full_sqlProvTest";
    fullProvision();
    
    assertFalse(GrouperFailsafe.isFailsafeIssue(jobName));
    
    List<Object[]> groups = new GcDbAccess().sql("select uuid, posix_id, name from testgrouper_prov_group order by name").selectList(Object[].class);
    assertEquals(10, groups.size());
    
    List<Object[]> entities = new GcDbAccess().sql("select uuid, name, subject_id_or_identifier, description from testgrouper_prov_entity order by name").selectList(Object[].class);
    assertEquals(10, entities.size());
    
    List<Object[]> memberships = new GcDbAccess().sql("select group_uuid, entity_uuid from testgrouper_prov_mship2").selectList(Object[].class);
    assertEquals(100, memberships.size());
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.sqlProvTest.failsafeMinOverallNumberOfMembers").value("85").store();
    
    for (int i=0;i<5;i++) {
      failsafeGroups.get(i).delete();
    }
    try {
      GrouperLoader.runOnceByJobName(this.grouperSession, jobName);
      fail();
    } catch(Exception e) {
    }
    
    assertTrue(GrouperFailsafe.isFailsafeIssue(jobName));
  
    Hib3GrouperLoaderLog hib3GrouperLoaderLog = Hib3GrouperLoaderLog.retrieveMostRecentLog(jobName);
    assertEquals("ERROR_FAILSAFE", hib3GrouperLoaderLog.getStatus());
    
    groups = new GcDbAccess().sql("select uuid, posix_id, name from testgrouper_prov_group order by name").selectList(Object[].class);
    assertEquals(10, groups.size());
    
    entities = new GcDbAccess().sql("select uuid, name, subject_id_or_identifier, description from testgrouper_prov_entity order by name").selectList(Object[].class);
    assertEquals(10, entities.size());
    
    memberships = new GcDbAccess().sql("select group_uuid, entity_uuid from testgrouper_prov_mship2").selectList(Object[].class);
    assertEquals(100, memberships.size());
    
    GrouperUtil.sleep(1000);
    
    assertFalse(GrouperFailsafe.isApproved(jobName));
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.sqlProvTest.failsafeMinOverallNumberOfMembers").value("45").store();
  
    GrouperLoader.runOnceByJobName(this.grouperSession, jobName);
  
    hib3GrouperLoaderLog = Hib3GrouperLoaderLog.retrieveMostRecentLog(jobName);
    assertEquals("SUCCESS", hib3GrouperLoaderLog.getStatus());
    assertFalse(GrouperFailsafe.isFailsafeIssue(jobName));
  
    memberships = new GcDbAccess().sql("select group_uuid, entity_uuid from testgrouper_prov_mship2").selectList(Object[].class);
    
    assertEquals(50, memberships.size());
    
    groups = new GcDbAccess().sql("select uuid, posix_id, name from testgrouper_prov_group order by name").selectList(Object[].class);
    assertEquals(5, groups.size());
    
    entities = new GcDbAccess().sql("select uuid, name, subject_id_or_identifier, description from testgrouper_prov_entity order by name").selectList(Object[].class);
    assertEquals(10, entities.size());
    
    memberships = new GcDbAccess().sql("select group_uuid, entity_uuid from testgrouper_prov_mship2").selectList(Object[].class);
    assertEquals(50, memberships.size());
  
  }

  public void testSimpleGroupMembershipProvisioningFullWithAttributesTableFailsafeMinManagedGroups() {
    
    setupFailsafeJob();
    
    //lets sync these over
    String jobName = "OTHER_JOB_provisioner_full_sqlProvTest";
    fullProvision();
    
    assertFalse(GrouperFailsafe.isFailsafeIssue(jobName));
    
    List<Object[]> groups = new GcDbAccess().sql("select uuid, posix_id, name from testgrouper_prov_group order by name").selectList(Object[].class);
    assertEquals(10, groups.size());
    
    List<Object[]> entities = new GcDbAccess().sql("select uuid, name, subject_id_or_identifier, description from testgrouper_prov_entity order by name").selectList(Object[].class);
    assertEquals(10, entities.size());
    
    List<Object[]> memberships = new GcDbAccess().sql("select group_uuid, entity_uuid from testgrouper_prov_mship2").selectList(Object[].class);
    assertEquals(100, memberships.size());
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.sqlProvTest.failsafeMaxOverallPercentGroupsRemove").value("25").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.sqlProvTest.failsafeMinManagedGroups").value("8").store();
    
    for (int i=0;i<5;i++) {
      failsafeGroups.get(i).delete();
    }
    try {
      GrouperLoader.runOnceByJobName(this.grouperSession, jobName);
      fail();
    } catch(Exception e) {
    }
    
    assertTrue(GrouperFailsafe.isFailsafeIssue(jobName));
  
    Hib3GrouperLoaderLog hib3GrouperLoaderLog = Hib3GrouperLoaderLog.retrieveMostRecentLog(jobName);
    assertEquals("ERROR_FAILSAFE", hib3GrouperLoaderLog.getStatus());
    
    groups = new GcDbAccess().sql("select uuid, posix_id, name from testgrouper_prov_group order by name").selectList(Object[].class);
    assertEquals(10, groups.size());
    
    entities = new GcDbAccess().sql("select uuid, name, subject_id_or_identifier, description from testgrouper_prov_entity order by name").selectList(Object[].class);
    assertEquals(10, entities.size());
    
    memberships = new GcDbAccess().sql("select group_uuid, entity_uuid from testgrouper_prov_mship2").selectList(Object[].class);
    assertEquals(100, memberships.size());
    
    GrouperUtil.sleep(1000);
    
    assertFalse(GrouperFailsafe.isApproved(jobName));
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.sqlProvTest.failsafeMinManagedGroups").value("12").store();
  
    GrouperLoader.runOnceByJobName(this.grouperSession, jobName);
  
    hib3GrouperLoaderLog = Hib3GrouperLoaderLog.retrieveMostRecentLog(jobName);
    assertEquals("SUCCESS", hib3GrouperLoaderLog.getStatus());
    assertFalse(GrouperFailsafe.isFailsafeIssue(jobName));
  
    memberships = new GcDbAccess().sql("select group_uuid, entity_uuid from testgrouper_prov_mship2").selectList(Object[].class);
    
    assertEquals(50, memberships.size());
    
    groups = new GcDbAccess().sql("select uuid, posix_id, name from testgrouper_prov_group order by name").selectList(Object[].class);
    assertEquals(5, groups.size());
    
    entities = new GcDbAccess().sql("select uuid, name, subject_id_or_identifier, description from testgrouper_prov_entity order by name").selectList(Object[].class);
    assertEquals(10, entities.size());
    
    memberships = new GcDbAccess().sql("select group_uuid, entity_uuid from testgrouper_prov_mship2").selectList(Object[].class);
    assertEquals(50, memberships.size());
  
  }

  public void testSimpleGroupMembershipProvisioningFullWithAttributesTableFailsafeOverallPercentRemoved() {
    
    setupFailsafeJob();
    
    //lets sync these over
    String jobName = "OTHER_JOB_provisioner_full_sqlProvTest";
    
    fullProvision();
    
    assertFalse(GrouperFailsafe.isFailsafeIssue(jobName));
    
    List<Object[]> groups = new GcDbAccess().sql("select uuid, posix_id, name from testgrouper_prov_group order by name").selectList(Object[].class);
    assertEquals(10, groups.size());
    
    List<Object[]> entities = new GcDbAccess().sql("select uuid, name, subject_id_or_identifier, description from testgrouper_prov_entity order by name").selectList(Object[].class);
    assertEquals(10, entities.size());
    
    List<Object[]> memberships = new GcDbAccess().sql("select group_uuid, entity_uuid from testgrouper_prov_mship2").selectList(Object[].class);
    assertEquals(100, memberships.size());
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.sqlProvTest.failsafeMaxOverallPercentMembershipsRemove").value("25").store();
    
    for (int i=0;i<5;i++) {
      failsafeGroups.get(i).delete();
    }
    try {
      GrouperLoader.runOnceByJobName(this.grouperSession, jobName);
      fail();
    } catch(Exception e) {
    }
    
    assertTrue(GrouperFailsafe.isFailsafeIssue(jobName));
  
    Hib3GrouperLoaderLog hib3GrouperLoaderLog = Hib3GrouperLoaderLog.retrieveMostRecentLog(jobName);
    assertEquals("ERROR_FAILSAFE", hib3GrouperLoaderLog.getStatus());
    
    groups = new GcDbAccess().sql("select uuid, posix_id, name from testgrouper_prov_group order by name").selectList(Object[].class);
    assertEquals(10, groups.size());
    
    entities = new GcDbAccess().sql("select uuid, name, subject_id_or_identifier, description from testgrouper_prov_entity order by name").selectList(Object[].class);
    assertEquals(10, entities.size());
    
    memberships = new GcDbAccess().sql("select group_uuid, entity_uuid from testgrouper_prov_mship2").selectList(Object[].class);
    assertEquals(100, memberships.size());
    
    GrouperUtil.sleep(1000);
    
    assertFalse(GrouperFailsafe.isApproved(jobName));
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.sqlProvTest.failsafeMaxOverallPercentMembershipsRemove").value("60").store();
  
    GrouperLoader.runOnceByJobName(this.grouperSession, jobName);
  
    hib3GrouperLoaderLog = Hib3GrouperLoaderLog.retrieveMostRecentLog(jobName);
    assertEquals("SUCCESS", hib3GrouperLoaderLog.getStatus());
    assertFalse(GrouperFailsafe.isFailsafeIssue(jobName));
  
    memberships = new GcDbAccess().sql("select group_uuid, entity_uuid from testgrouper_prov_mship2").selectList(Object[].class);
    
    assertEquals(50, memberships.size());
    
    groups = new GcDbAccess().sql("select uuid, posix_id, name from testgrouper_prov_group order by name").selectList(Object[].class);
    assertEquals(5, groups.size());
    
    entities = new GcDbAccess().sql("select uuid, name, subject_id_or_identifier, description from testgrouper_prov_entity order by name").selectList(Object[].class);
    assertEquals(10, entities.size());
    
    memberships = new GcDbAccess().sql("select group_uuid, entity_uuid from testgrouper_prov_mship2").selectList(Object[].class);
    assertEquals(50, memberships.size());
  
  }

  /**
   * just do a simple full sync of groups and memberships
   */
  public void testSimpleGroupLdapPaMatchingIdMissingValidation() {
    
    long started = System.currentTimeMillis();
    
    configureLdapPaTestCase();

    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.sqlProvTest.targetGroupAttribute.2.translateExpressionType").value("translationScript").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.sqlProvTest.targetGroupAttribute.2.translateExpression")
      .value("${grouperProvisioningGroup.name == 'test:testGroup' ? null : grouperProvisioningGroup.idIndex}").store();

    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.sqlProvTest.targetEntityAttribute.1.translateExpressionType").value("translationScript").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.sqlProvTest.targetEntityAttribute.1.translateExpression")
    .value("${grouperProvisioningEntity.subjectId == 'test.subject.4' ? null : grouperProvisioningEntity.subjectId}").store();

    //lets sync these over
    GrouperProvisioningOutput grouperProvisioningOutput = fullProvision();
    GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();

    incrementalProvision();

    
    Stem stem = new StemSave(this.grouperSession).assignName("test").save();
    
    // mark some folders to provision
    Group testGroup = new GroupSave(this.grouperSession).assignName("test:testGroup").save();
    Group testGroup2 = new GroupSave(this.grouperSession).assignName("test:testGroup2").save();
    
    testGroup.addMember(SubjectTestHelper.SUBJ0, false);
    testGroup.addMember(SubjectTestHelper.SUBJ1, false);
    
    testGroup2.addMember(SubjectTestHelper.SUBJ2, false);
    testGroup2.addMember(SubjectTestHelper.SUBJ3, false);
    testGroup2.addMember(SubjectTestHelper.SUBJ4, false);
    
    Member member0 = MemberFinder.findBySubject(this.grouperSession, SubjectTestHelper.SUBJ0, true);
    Member member1 = MemberFinder.findBySubject(this.grouperSession, SubjectTestHelper.SUBJ1, true);
    Member member2 = MemberFinder.findBySubject(this.grouperSession, SubjectTestHelper.SUBJ2, true);
    Member member3 = MemberFinder.findBySubject(this.grouperSession, SubjectTestHelper.SUBJ3, true);
    Member member4 = MemberFinder.findBySubject(this.grouperSession, SubjectTestHelper.SUBJ4, true);
    
    final GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision("sqlProvTest");
    attributeValue.setTargetName("sqlProvTest");
    attributeValue.setStemScopeString("sub");
  
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);
  
    // add some entities
    for (int i=0;i<10;i++) {
      String uuid = GrouperUuid.getUuid();
      String dn = "dn_test.subject." + i;
      new GcDbAccess().sql("insert into testgrouper_prov_ldap_entity (entity_uuid) values (?)").addBindVar(uuid).executeSql();
      new GcDbAccess().sql("insert into testgrouper_pro_dap_entity_attr (entity_uuid, entity_attribute_name, entity_attribute_value) values (?,?,?)")
        .addBindVar(uuid).addBindVar("dn").addBindVar(dn).executeSql();
      new GcDbAccess().sql("insert into testgrouper_pro_dap_entity_attr (entity_uuid, entity_attribute_name, entity_attribute_value) values (?,?,?)")
        .addBindVar(uuid).addBindVar("employeeId").addBindVar("test.subject." + i).executeSql();
    }
    
    
    //AttributeAssign attributeAssign = stem.getAttributeDelegate().addAttribute(GrouperProvisioningAttributeNames.retrieveAttributeDefNameMarker()).getAttributeAssign();
    //attributeAssign.getAttributeValueDelegate().assignValueString(GrouperProvisioningAttributeNames.retrieveAttributeDefNameDoProvision())
    
    //lets sync these over
    try {
      grouperProvisioningOutput = fullProvision("sqlProvTest", true);
      fail();
    } catch (Exception e) {
      
    }
    grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    
    // make sure some time has passed
    GrouperUtil.sleep(1000);
    
    Hib3GrouperLoaderLog hib3GrouperLoaderLog = Hib3GrouperLoaderLog.retrieveMostRecentLog("OTHER_JOB_provisioner_full_sqlProvTest");
    assertEquals(GrouperLoaderStatus.ERROR.name(), hib3GrouperLoaderLog.getStatus());
    
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
  
    String sql = "select uuid from testgrouper_prov_ldap_group";
    
    List<Object[]> dataInTable = new GcDbAccess().sql(sql).selectList(Object[].class);
    
    Set<MultiKey> groupNamesInTable = new HashSet<MultiKey>();
    
    for (Object[] row: dataInTable) {
      groupNamesInTable.add(new MultiKey(row));
    }
    
    assertEquals(1, groupNamesInTable.size());
    assertTrue(groupNamesInTable.contains(new MultiKey(new Object[]{"test:testGroup2"})));
  
    sql = "select group_uuid, attribute_name, attribute_value from testgrouper_pro_ldap_group_attr";
    
    dataInTable = new GcDbAccess().sql(sql).selectList(Object[].class);
    
    Set<MultiKey> attributesInTable = new HashSet<MultiKey>();
    
    for (Object[] row: dataInTable) {
      attributesInTable.add(new MultiKey(row));
    }
    
    assertEquals(6, attributesInTable.size());
    
    assertTrue(attributesInTable.contains(new MultiKey("test:testGroup2", "cn", "test:testGroup2")));
    assertTrue(attributesInTable.contains(new MultiKey("test:testGroup2", "dn", "cn=test:testGroup2,OU=Grouper,OU=365Groups,DC=one,DC=upenn,DC=edu")));
    assertTrue(attributesInTable.contains(new MultiKey("test:testGroup2", "gidNumber", "" + testGroup2.getIdIndex())));
    assertTrue(attributesInTable.contains(new MultiKey("test:testGroup2", "objectClass", "group")));
    assertTrue(attributesInTable.contains(new MultiKey("test:testGroup2", "member", "dn_test.subject.2")));
    assertTrue(attributesInTable.contains(new MultiKey("test:testGroup2", "member", "dn_test.subject.3")));
    //object changes
    assertEquals(0, GrouperUtil.length(grouperProvisioner.retrieveGrouperProvisioningDataChanges().getTargetObjectInserts().getProvisioningGroups()));
    assertEquals(0, GrouperUtil.length(grouperProvisioner.retrieveGrouperProvisioningDataChanges().getTargetObjectInserts().getProvisioningEntities()));
    assertEquals(0, GrouperUtil.length(grouperProvisioner.retrieveGrouperProvisioningDataChanges().getTargetObjectInserts().getProvisioningMemberships()));
    assertEquals(1, GrouperUtil.length(grouperProvisioner.retrieveGrouperProvisioningDataChanges().getTargetObjectUpdates().getProvisioningGroups()));
    assertEquals(0, GrouperUtil.length(grouperProvisioner.retrieveGrouperProvisioningDataChanges().getTargetObjectUpdates().getProvisioningEntities()));
    assertEquals(0, GrouperUtil.length(grouperProvisioner.retrieveGrouperProvisioningDataChanges().getTargetObjectUpdates().getProvisioningMemberships()));
    assertEquals(0, GrouperUtil.length(grouperProvisioner.retrieveGrouperProvisioningDataChanges().getTargetObjectDeletes().getProvisioningGroups()));
    assertEquals(0, GrouperUtil.length(grouperProvisioner.retrieveGrouperProvisioningDataChanges().getTargetObjectDeletes().getProvisioningEntities()));
    assertEquals(0, GrouperUtil.length(grouperProvisioner.retrieveGrouperProvisioningDataChanges().getTargetObjectDeletes().getProvisioningMemberships()));
    
    // field changes
    assertEquals(2, GrouperUtil.length(grouperProvisioner.retrieveGrouperProvisioningDataChanges().getTargetObjectUpdates().getProvisioningGroups().iterator().next().getInternal_objectChanges()));
    List<ProvisioningObjectChange> provisioningObjectChanges = new ArrayList<ProvisioningObjectChange>(
        grouperProvisioner.retrieveGrouperProvisioningDataChanges().getTargetObjectUpdates().getProvisioningGroups().iterator().next().getInternal_objectChanges());
    assertEquals(ProvisioningObjectChangeAction.insert, provisioningObjectChanges.get(0).getProvisioningObjectChangeAction());
    assertEquals(ProvisioningObjectChangeAction.insert, provisioningObjectChanges.get(1).getProvisioningObjectChangeAction());
    assertEquals("member", provisioningObjectChanges.get(0).getAttributeName());
    assertEquals("member", provisioningObjectChanges.get(1).getAttributeName());
  
    //get the grouper_sync and check cols
    GcGrouperSync gcGrouperSync = GcGrouperSyncDao.retrieveByProvisionerName(null, "sqlProvTest");
    assertEquals(2, gcGrouperSync.getGroupCount().intValue());
    
    // 2 valid, 1 invalid, 2 in invalid group
    assertEquals(5, gcGrouperSync.getUserCount().intValue());
    assertEquals(5, gcGrouperSync.getRecordsCount().intValue());
    assertTrue(started <  gcGrouperSync.getLastFullSyncRun().getTime());
    assertTrue(new Timestamp(System.currentTimeMillis()) + " > " + new Timestamp(gcGrouperSync.getLastFullSyncRun().getTime()), System.currentTimeMillis() >  gcGrouperSync.getLastFullSyncRun().getTime());
    assertTrue(started < gcGrouperSync.getLastUpdated().getTime());
    assertTrue(System.currentTimeMillis() > gcGrouperSync.getLastUpdated().getTime());
    
    GcGrouperSyncJob gcGrouperSyncJob = gcGrouperSync.getGcGrouperSyncJobDao().jobRetrieveBySyncType("fullProvisionFull");
    assertEquals(100, gcGrouperSyncJob.getPercentComplete().intValue());
    assertEquals(GcGrouperSyncJobState.notRunning, gcGrouperSyncJob.getJobState());
    assertTrue(started < gcGrouperSyncJob.getLastSyncTimestamp().getTime());
    assertTrue(System.currentTimeMillis() > gcGrouperSyncJob.getLastSyncTimestamp().getTime());
    assertTrue(started < gcGrouperSyncJob.getLastTimeWorkWasDone().getTime());
    assertTrue(System.currentTimeMillis() > gcGrouperSyncJob.getLastTimeWorkWasDone().getTime());
    assertTrue(started < gcGrouperSyncJob.getHeartbeat().getTime());
    assertTrue(System.currentTimeMillis() > gcGrouperSyncJob.getHeartbeat().getTime());
    assertTrue(started < gcGrouperSyncJob.getLastUpdated().getTime());
    assertTrue(System.currentTimeMillis() > gcGrouperSyncJob.getLastUpdated().getTime());
    assertNull(gcGrouperSyncJob.getErrorMessage());
    assertNull(gcGrouperSyncJob.getErrorTimestamp());
    
    {
      GcGrouperSyncGroup gcGrouperSyncGroup = gcGrouperSync.getGcGrouperSyncGroupDao().groupRetrieveByGroupId(testGroup.getId());
      assertEquals(testGroup.getId(), gcGrouperSyncGroup.getGroupId());
      assertEquals(testGroup.getName(), gcGrouperSyncGroup.getGroupName());
      assertEquals(testGroup.getIdIndex(), gcGrouperSyncGroup.getGroupIdIndex());
      assertEquals("T", gcGrouperSyncGroup.getProvisionableDb());
      assertFalse("T".equals(gcGrouperSyncGroup.getInTargetDb()));
      assertFalse("T".equals(gcGrouperSyncGroup.getInTargetInsertOrExistsDb()));
      assertEquals(GcGrouperSyncErrorCode.MAT, gcGrouperSyncGroup.getErrorCode());
    }
    
    {
      GcGrouperSyncGroup gcGrouperSyncGroup2 = gcGrouperSync.getGcGrouperSyncGroupDao().groupRetrieveByGroupId(testGroup2.getId());
      assertEquals(testGroup2.getId(), gcGrouperSyncGroup2.getGroupId());
      assertEquals(testGroup2.getName(), gcGrouperSyncGroup2.getGroupName());
      assertEquals(testGroup2.getIdIndex(), gcGrouperSyncGroup2.getGroupIdIndex());
      assertEquals("T", gcGrouperSyncGroup2.getProvisionableDb());
      assertTrue("T".equals(gcGrouperSyncGroup2.getInTargetDb()));
      assertTrue("T".equals(gcGrouperSyncGroup2.getInTargetInsertOrExistsDb()));
      assertNull(gcGrouperSyncGroup2.getErrorCode());
    }
    
    {
      GcGrouperSyncMember gcGrouperSyncMember0 = gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberId(member0.getId());
      assertEquals(member0.getSubjectId(), gcGrouperSyncMember0.getSubjectId());
      assertEquals(member0.getSubjectSourceId(), gcGrouperSyncMember0.getSourceId());
      assertEquals("T", gcGrouperSyncMember0.getProvisionableDb());
      assertTrue("T".equals(gcGrouperSyncMember0.getInTargetDb()));
      assertTrue("F".equals(gcGrouperSyncMember0.getInTargetInsertOrExistsDb()));
      assertNull(gcGrouperSyncMember0.getErrorCode());
    }  
    
    {
      GcGrouperSyncMember gcGrouperSyncMember1 = gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberId(member1.getId());
      assertEquals(member1.getSubjectId(), gcGrouperSyncMember1.getSubjectId());
      assertEquals(member1.getSubjectSourceId(), gcGrouperSyncMember1.getSourceId());
      assertEquals("T", gcGrouperSyncMember1.getProvisionableDb());
      assertTrue("T".equals(gcGrouperSyncMember1.getInTargetDb()));
      assertTrue("F".equals(gcGrouperSyncMember1.getInTargetInsertOrExistsDb()));
      assertNull(gcGrouperSyncMember1.getErrorCode());
    }  
    
    {
      GcGrouperSyncMember gcGrouperSyncMember2 = gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberId(member2.getId());
      assertEquals(member2.getSubjectId(), gcGrouperSyncMember2.getSubjectId());
      assertEquals(member2.getSubjectSourceId(), gcGrouperSyncMember2.getSourceId());
      assertEquals("T", gcGrouperSyncMember2.getProvisionableDb());
      assertTrue("T".equals(gcGrouperSyncMember2.getInTargetDb()));
      assertTrue("F".equals(gcGrouperSyncMember2.getInTargetInsertOrExistsDb()));
      assertNull(gcGrouperSyncMember2.getErrorCode());
    }  
    
    {
      GcGrouperSyncMember gcGrouperSyncMember3 = gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberId(member3.getId());
      assertEquals(member3.getSubjectId(), gcGrouperSyncMember3.getSubjectId());
      assertEquals(member3.getSubjectSourceId(), gcGrouperSyncMember3.getSourceId());
      assertEquals("T", gcGrouperSyncMember3.getProvisionableDb());
      assertTrue("T".equals(gcGrouperSyncMember3.getInTargetDb()));
      assertTrue("F".equals(gcGrouperSyncMember3.getInTargetInsertOrExistsDb()));
      assertNull(gcGrouperSyncMember3.getErrorCode());
    }  
    
    {
      GcGrouperSyncMember gcGrouperSyncMember4 = gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberId(member4.getId());
      assertEquals(member4.getSubjectId(), gcGrouperSyncMember4.getSubjectId());
      assertEquals(member4.getSubjectSourceId(), gcGrouperSyncMember4.getSourceId());
      assertEquals("T", gcGrouperSyncMember4.getProvisionableDb());
      assertFalse("T".equals(gcGrouperSyncMember4.getInTargetDb()));
      assertFalse("F".equals(gcGrouperSyncMember4.getInTargetInsertOrExistsDb()));
      assertEquals(GcGrouperSyncErrorCode.MAT, gcGrouperSyncMember4.getErrorCode());
    }  
    
    {
      GcGrouperSyncMembership gcGrouperSyncMembership0 = gcGrouperSync.getGcGrouperSyncMembershipDao().membershipRetrieveByGroupIdAndMemberId(testGroup.getId(), member0.getId());
      assertFalse("T".equals(gcGrouperSyncMembership0.getInTargetDb()));
      assertFalse("T".equals(gcGrouperSyncMembership0.getInTargetInsertOrExistsDb()));
      assertEquals(GcGrouperSyncErrorCode.MAT, gcGrouperSyncMembership0.getErrorCode());
    }  
    {
      GcGrouperSyncMembership gcGrouperSyncMembership1 = gcGrouperSync.getGcGrouperSyncMembershipDao().membershipRetrieveByGroupIdAndMemberId(testGroup.getId(), member1.getId());
      assertFalse("T".equals(gcGrouperSyncMembership1.getInTargetDb()));
      assertFalse("T".equals(gcGrouperSyncMembership1.getInTargetInsertOrExistsDb()));
      assertEquals(GcGrouperSyncErrorCode.MAT, gcGrouperSyncMembership1.getErrorCode());
    }  
    
    {
      GcGrouperSyncMembership gcGrouperSyncMembership2 = gcGrouperSync.getGcGrouperSyncMembershipDao().membershipRetrieveByGroupIdAndMemberId(testGroup2.getId(), member2.getId());
      assertTrue("T".equals(gcGrouperSyncMembership2.getInTargetDb()));
      assertTrue("T".equals(gcGrouperSyncMembership2.getInTargetInsertOrExistsDb()));
      assertNull(gcGrouperSyncMembership2.getErrorCode());
    }  
    
    {
      GcGrouperSyncMembership gcGrouperSyncMembership3 = gcGrouperSync.getGcGrouperSyncMembershipDao().membershipRetrieveByGroupIdAndMemberId(testGroup2.getId(), member3.getId());
      assertTrue("T".equals(gcGrouperSyncMembership3.getInTargetDb()));
      assertTrue("T".equals(gcGrouperSyncMembership3.getInTargetInsertOrExistsDb()));
      assertNull(gcGrouperSyncMembership3.getErrorCode());
    }  
    
    {
      GcGrouperSyncMembership gcGrouperSyncMembership4 = gcGrouperSync.getGcGrouperSyncMembershipDao().membershipRetrieveByGroupIdAndMemberId(testGroup2.getId(), member4.getId());
      assertFalse("T".equals(gcGrouperSyncMembership4.getInTargetDb()));
      assertFalse("T".equals(gcGrouperSyncMembership4.getInTargetInsertOrExistsDb()));
      assertEquals(GcGrouperSyncErrorCode.MAT, gcGrouperSyncMembership4.getErrorCode());
    }  

    // this should not retry
    try {
      hib3GrouperLoaderLog = incrementalProvision(defaultConfigId(), true, true, true);
      fail();
    } catch (Exception e) {
      
    }
    assertEquals("ERROR", hib3GrouperLoaderLog.getStatus());
    
    grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    assertEquals(0, GrouperUtil.intValue(grouperProvisioner.getDebugMap().get("addErrorsToQueue"), 0));
    assertTrue(GrouperUtil.intValue(grouperProvisioner.getDebugMap().get("skippedEventsDueToFullSync"), 0) > 0);
    assertEquals(0, GrouperUtil.intValue(grouperProvisioner.getDebugMap().get("messageCountForProvisioner"), 0));
    
    
    hib3GrouperLoaderLog = incrementalProvision();
    assertEquals("SUCCESS", hib3GrouperLoaderLog.getStatus());
    
    grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    assertEquals(0, GrouperUtil.intValue(grouperProvisioner.getDebugMap().get("addErrorsToQueue"), 0));
    assertEquals(0, GrouperUtil.intValue(grouperProvisioner.getDebugMap().get("skippedEventsDueToFullSync"), 0));
    assertEquals(0, GrouperUtil.intValue(grouperProvisioner.getDebugMap().get("messageCountForProvisioner"), 0));
    
  
  }

  public void testSimpleMembershipGroupNameSubjectIdDeleteLogDaemon() {
    GrouperLoader.runOnceByJobName(grouperSession, "OTHER_JOB_deleteOldSyncLogs");
    Hib3GrouperLoaderLog hib3GrouperLoaderLog = Hib3GrouperLoaderLog.retrieveMostRecentLog("OTHER_JOB_deleteOldSyncLogs");

    assertEquals(0, GrouperUtil.intValue(hib3GrouperLoaderLog.getDeleteCount(), 0));
    
    SqlProvisionerTestUtils.configureSqlProvisioner(new SqlProvisionerTestConfigInput()
        .assignMembershipDeleteType("deleteMembershipsIfNotExistInGrouper")
        .assignMembershipTableName("testgrouper_prov_mship0")
  //        .assignMembershipTableIdColumn("group_name, subject_id")
  //        .assignMembershipGroupForeignKeyColumn("group_name")
  //        .assignMembershipEntityForeignKeyColumn("subject_id")
        .assignMembershipAttributeCount(2)
    );

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
    attributeValue.setDoProvision("sqlProvTest");
    attributeValue.setTargetName("sqlProvTest");
    attributeValue.setStemScopeString("sub");

    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);

    //AttributeAssign attributeAssign = stem.getAttributeDelegate().addAttribute(GrouperProvisioningAttributeNames.retrieveAttributeDefNameMarker()).getAttributeAssign();
    //attributeAssign.getAttributeValueDelegate().assignValueString(GrouperProvisioningAttributeNames.retrieveAttributeDefNameDoProvision())
    
    //lets sync these over
    GrouperProvisioningOutput grouperProvisioningOutput = fullProvision();
    GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
    
    String sql = "select group_name, subject_id from testgrouper_prov_mship0";
    
    List<Object[]> dataInTable = new GcDbAccess().sql(sql).selectList(Object[].class);
    
    Set<MultiKey> membershipsInTable = new HashSet<MultiKey>();
    
    for (Object[] row: dataInTable) {
      membershipsInTable.add(new MultiKey(row));
    }
    
    assertEquals(2, membershipsInTable.size());
    assertTrue(membershipsInTable.contains(new MultiKey("test:testGroup", "test.subject.0")));
    assertTrue(membershipsInTable.contains(new MultiKey("test:testGroup", "test.subject.1")));

    // sleep so we can find most recent
    GrouperUtil.sleep(5000);
    GrouperLoader.runOnceByJobName(grouperSession, "OTHER_JOB_deleteOldSyncLogs");
    hib3GrouperLoaderLog = Hib3GrouperLoaderLog.retrieveMostRecentLog("OTHER_JOB_deleteOldSyncLogs");

    assertEquals(0, GrouperUtil.intValue(hib3GrouperLoaderLog.getDeleteCount(), 0));

    int count = new GcDbAccess().sql("select count(1) from grouper_sync_log").select(int.class);
    assertTrue(count + "", count > 0);
    
    List<String> ids = new GcDbAccess().sql("select id from grouper_sync_log").selectList(String.class);
    String id = GrouperUtil.listPopOne(ids);
    
    new GcDbAccess().sql("update grouper_sync_log set last_updated = ? where id = ?").addBindVar(new Timestamp(System.currentTimeMillis() - (1000*60*60*24*8))).addBindVar(id).executeSql();
    
    // sleep so we can find most recent
    GrouperUtil.sleep(5000);
    GrouperLoader.runOnceByJobName(grouperSession, "OTHER_JOB_deleteOldSyncLogs");
    hib3GrouperLoaderLog = Hib3GrouperLoaderLog.retrieveMostRecentLog("OTHER_JOB_deleteOldSyncLogs");

    assertEquals(1, GrouperUtil.intValue(hib3GrouperLoaderLog.getDeleteCount(), 0));
    assertEquals(count-1, new GcDbAccess().sql("select count(1) from grouper_sync_log").select(int.class).intValue());
    
  }

  public void testGroupEntityMembershipRenameEntityFullMatchOnOld() {
    
    SqlProvisionerTestUtils.configureSqlProvisioner(new SqlProvisionerTestConfigInput()
        .assignEntityDeleteType("deleteEntitiesIfNotExistInGrouper")
        .assignGroupDeleteType("deleteGroupsIfNotExistInGrouper")
        .assignMembershipDeleteType("deleteMembershipsIfNotExistInGrouper")
        .assignEntityAttributesTable(true)
        .assignGroupAttributesTable(true)
        .assignGroupTableName("testgrouper_prov_group")
        .assignGroupTableIdColumn("uuid")
        .assignEntityTableName("testgrouper_prov_entity")
        .assignEntityTableIdColumn("uuid")
        .assignMembershipTableName("testgrouper_prov_mship2")
        .assignMembershipTableIdColumn("uuid")
        .assignMembershipGroupForeignKeyColumn("group_uuid")
        .assignMembershipEntityForeignKeyColumn("entity_uuid")
        .assignHasTargetEntityLink(true)
        .assignHasTargetGroupLink(true)
        .assignEntityAttributeCount(5)
        .assignGroupAttributeCount(4)
        .assignMembershipAttributeCount(3)
        .addExtraConfig("targetGroupAttribute.2.translateFromGrouperProvisioningGroupField", "displayName")
        .addExtraConfig("groupMatchingAttributeSameAsSearchAttribute", "false")
        .addExtraConfig("groupMatchingAttribute0name", "name")
        );
  
    Stem stem = new StemSave(this.grouperSession).assignName("test").save();
    Stem stem2 = new StemSave(this.grouperSession).assignName("test2").save();
    
    // mark some folders to provision
    Group testGroup = new GroupSave(this.grouperSession).assignName("test:testGroup").assignDescription("testDescription1").save();
    Group testGroup2 = new GroupSave(this.grouperSession).assignName("test2:testGroup2").assignDescription("testDescription2").save();
    
    testGroup.addMember(SubjectTestHelper.SUBJ0, false);
    testGroup.addMember(SubjectTestHelper.SUBJ1, false);
    
    testGroup2.addMember(SubjectTestHelper.SUBJ2, false);
    testGroup2.addMember(SubjectTestHelper.SUBJ3, false);
    
    final GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision("sqlProvTest");
    attributeValue.setTargetName("sqlProvTest");
    attributeValue.setStemScopeString("sub");
  
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);
  
    //AttributeAssign attributeAssign = stem.getAttributeDelegate().addAttribute(GrouperProvisioningAttributeNames.retrieveAttributeDefNameMarker()).getAttributeAssign();
    //attributeAssign.getAttributeValueDelegate().assignValueString(GrouperProvisioningAttributeNames.retrieveAttributeDefNameDoProvision())
    
    //lets sync these over
    GrouperProvisioningOutput grouperProvisioningOutput = fullProvision();
    GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
    
    //get the grouper_sync and check cols
    GcGrouperSync gcGrouperSync = GcGrouperSyncDao.retrieveByProvisionerName(null, "sqlProvTest");
    
    Member testSubject0member = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ0, true);
    
    GcGrouperSyncMember gcGrouperSyncMember = gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberId(testSubject0member.getId());
    assertEquals(SubjectTestHelper.SUBJ0.getName(), gcGrouperSyncMember.getEntityAttributeValueCache1());

    
    List<Object[]> groups = new GcDbAccess().sql("select uuid, posix_id, name from testgrouper_prov_group").selectList(Object[].class);
    assertEquals(1, groups.size());
    
    String testGroupUuidInTarget = groups.get(0)[0].toString();
    assertNotNull(testGroupUuidInTarget);
    
    assertEquals(testGroup.getIdIndex().longValue(), ((BigDecimal)groups.get(0)[1]).longValue());
    assertEquals(testGroup.getName(), groups.get(0)[2]);
    
    List<Object[]> groupAttributes = new GcDbAccess().sql("select attribute_name, attribute_value from testgrouper_pro_ldap_group_attr where group_uuid = '"+testGroupUuidInTarget+"'").selectList(Object[].class);
    assertEquals(1, groupAttributes.size());
    
    assertEquals("description", groupAttributes.get(0)[0]);
    assertEquals(testGroup.getDescription(), groupAttributes.get(0)[1]);
    
    List<Object[]> entities = new GcDbAccess().sql("select uuid, name, subject_id_or_identifier, description from testgrouper_prov_entity").selectList(Object[].class);
    assertEquals(2, entities.size());
    
    String subject0EntityUUID = null;
    String subject1EntityUUID2 = null;
    
    Map<String, Object[]> entityNameToAllAttributes = new HashMap<String, Object[]>();
    for (Object[] entityAttributes: entities) {
      entityNameToAllAttributes.put(entityAttributes[1].toString(), entityAttributes);
      if (StringUtils.equals(entityAttributes[1].toString(), SubjectTestHelper.SUBJ0.getName())) {
        subject0EntityUUID = entityAttributes[0].toString();
      }
      if (StringUtils.equals(entityAttributes[1].toString(), SubjectTestHelper.SUBJ1.getName())) {
        subject1EntityUUID2 = entityAttributes[0].toString();
      }
    }

    assertEquals(subject0EntityUUID, gcGrouperSyncMember.getEntityAttributeValueCache0());

    assertTrue(entityNameToAllAttributes.containsKey(SubjectTestHelper.SUBJ0.getName()));
    assertTrue(entityNameToAllAttributes.containsKey(SubjectTestHelper.SUBJ1.getName()));
    
    List<Object[]> memberships = new GcDbAccess().sql("select group_uuid, entity_uuid from testgrouper_prov_mship2").selectList(Object[].class);
    
    Set<MultiKey> groupIdSubjectIdsInTable = new HashSet<MultiKey>();
    
    for (Object[] membershipAttributes: memberships) {
      groupIdSubjectIdsInTable.add(new MultiKey(membershipAttributes[0], membershipAttributes[1]));
    }
    
    assertEquals(2, groupIdSubjectIdsInTable.size());
    
    assertTrue(groupIdSubjectIdsInTable.contains(new MultiKey(testGroupUuidInTarget, subject0EntityUUID)));
    assertTrue(groupIdSubjectIdsInTable.contains(new MultiKey(testGroupUuidInTarget, subject1EntityUUID2)));
    
    // rename the entity in grouper
    
    HibernateSession.bySqlStatic().executeSql("update subject set name='my name is test.subject.0_new' where subjectid='test.subject.0'", null, null);
    HibernateSession.bySqlStatic().executeSql("update subjectattribute set value='my name is test.subject.0_new' where subjectid='test.subject.0' and name='name'", null, null);
    Hib3MemberDAO.membersCacheClear();
    SubjectFinder.flushCache();
    Subject subject = SubjectFinder.findById("test.subject.0", true);
    Member member = MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), subject);
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(true), "OTHER_JOB_usduDaemon");

    grouperProvisioningOutput = fullProvision(); 
    grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
  
    //get the grouper_sync and check cols
    gcGrouperSync = GcGrouperSyncDao.retrieveByProvisionerName(null, "sqlProvTest");
    
    testSubject0member = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ0, true);
    
    gcGrouperSyncMember = gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberId(testSubject0member.getId());
    assertEquals("my name is test.subject.0_new", gcGrouperSyncMember.getEntityAttributeValueCache1());

    entities = new GcDbAccess().sql("select uuid, name, subject_id_or_identifier, description from testgrouper_prov_entity").selectList(Object[].class);
    assertEquals(2, entities.size());
    
    subject0EntityUUID = null;
    subject1EntityUUID2 = null;
    
    entityNameToAllAttributes = new HashMap<String, Object[]>();
    for (Object[] entityAttributes: entities) {
      entityNameToAllAttributes.put(entityAttributes[1].toString(), entityAttributes);
      if (StringUtils.equals(entityAttributes[1].toString(), SubjectTestHelper.SUBJ0.getName()+"_new")) {
        subject0EntityUUID = entityAttributes[0].toString();
      }
      if (StringUtils.equals(entityAttributes[1].toString(), SubjectTestHelper.SUBJ1.getName())) {
        subject1EntityUUID2 = entityAttributes[0].toString();
      }
    }
    
    assertEquals(subject0EntityUUID, gcGrouperSyncMember.getEntityAttributeValueCache0());

    assertTrue(entityNameToAllAttributes.containsKey(SubjectTestHelper.SUBJ0.getName()+"_new"));
    assertTrue(entityNameToAllAttributes.containsKey(SubjectTestHelper.SUBJ1.getName()));

    
  }

  public void testGroupEntityMembershipRenameEntityIncrementalMatchOnOld() {
    
    SqlProvisionerTestUtils.configureSqlProvisioner(new SqlProvisionerTestConfigInput()
        .assignEntityDeleteType("deleteEntitiesIfNotExistInGrouper")
        .assignGroupDeleteType("deleteGroupsIfNotExistInGrouper")
        .assignMembershipDeleteType("deleteMembershipsIfNotExistInGrouper")
        .assignEntityAttributesTable(true)
        .assignGroupAttributesTable(true)
        .assignGroupTableName("testgrouper_prov_group")
        .assignGroupTableIdColumn("uuid")
        .assignEntityTableName("testgrouper_prov_entity")
        .assignEntityTableIdColumn("uuid")
        .assignMembershipTableName("testgrouper_prov_mship2")
        .assignMembershipTableIdColumn("uuid")
        .assignMembershipGroupForeignKeyColumn("group_uuid")
        .assignMembershipEntityForeignKeyColumn("entity_uuid")
        .assignHasTargetEntityLink(true)
        .assignHasTargetGroupLink(true)
        .assignEntityAttributeCount(5)
        .assignGroupAttributeCount(4)
        .assignMembershipAttributeCount(3)
        .addExtraConfig("targetGroupAttribute.2.translateFromGrouperProvisioningGroupField", "displayName")
        .addExtraConfig("groupMatchingAttributeSameAsSearchAttribute", "false")
        .addExtraConfig("groupMatchingAttribute0name", "name")
        );
  
    fullProvision();
    incrementalProvision();
    
    String subject0name = SubjectTestHelper.SUBJ0.getName()+"_new";

    Stem stem = new StemSave(this.grouperSession).assignName("test").save();
    Stem stem2 = new StemSave(this.grouperSession).assignName("test2").save();
    
    // mark some folders to provision
    Group testGroup = new GroupSave(this.grouperSession).assignName("test:testGroup").assignDescription("testDescription1").save();
    Group testGroup2 = new GroupSave(this.grouperSession).assignName("test2:testGroup2").assignDescription("testDescription2").save();
    
    testGroup.addMember(SubjectTestHelper.SUBJ0, false);
    testGroup.addMember(SubjectTestHelper.SUBJ1, false);
    
    testGroup2.addMember(SubjectTestHelper.SUBJ2, false);
    testGroup2.addMember(SubjectTestHelper.SUBJ3, false);
    
    final GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision("sqlProvTest");
    attributeValue.setTargetName("sqlProvTest");
    attributeValue.setStemScopeString("sub");
  
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);
  
    //AttributeAssign attributeAssign = stem.getAttributeDelegate().addAttribute(GrouperProvisioningAttributeNames.retrieveAttributeDefNameMarker()).getAttributeAssign();
    //attributeAssign.getAttributeValueDelegate().assignValueString(GrouperProvisioningAttributeNames.retrieveAttributeDefNameDoProvision())
    
    //lets sync these over
    incrementalProvision();
    GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    GrouperProvisioningOutput grouperProvisioningOutput = grouperProvisioner.retrieveGrouperProvisioningOutput();
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
    
    //get the grouper_sync and check cols
    GcGrouperSync gcGrouperSync = GcGrouperSyncDao.retrieveByProvisionerName(null, "sqlProvTest");
    
    Member testSubject0member = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ0, true);
    
    GcGrouperSyncMember gcGrouperSyncMember = gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberId(testSubject0member.getId());
    assertEquals(SubjectTestHelper.SUBJ0.getName(), gcGrouperSyncMember.getEntityAttributeValueCache1());
  
    
    List<Object[]> groups = new GcDbAccess().sql("select uuid, posix_id, name from testgrouper_prov_group").selectList(Object[].class);
    assertEquals(1, groups.size());
    
    String testGroupUuidInTarget = groups.get(0)[0].toString();
    assertNotNull(testGroupUuidInTarget);
    
    assertEquals(testGroup.getIdIndex().longValue(), ((BigDecimal)groups.get(0)[1]).longValue());
    assertEquals(testGroup.getName(), groups.get(0)[2]);
    
    List<Object[]> groupAttributes = new GcDbAccess().sql("select attribute_name, attribute_value from testgrouper_pro_ldap_group_attr where group_uuid = '"+testGroupUuidInTarget+"'").selectList(Object[].class);
    assertEquals(1, groupAttributes.size());
    
    assertEquals("description", groupAttributes.get(0)[0]);
    assertEquals(testGroup.getDescription(), groupAttributes.get(0)[1]);
    
    List<Object[]> entities = new GcDbAccess().sql("select uuid, name, subject_id_or_identifier, description from testgrouper_prov_entity").selectList(Object[].class);
    assertEquals(2, entities.size());
    
    String subject0EntityUUID = null;
    String subject1EntityUUID2 = null;
    
    Map<String, Object[]> entityNameToAllAttributes = new HashMap<String, Object[]>();
    for (Object[] entityAttributes: entities) {
      entityNameToAllAttributes.put(entityAttributes[1].toString(), entityAttributes);
      if (StringUtils.equals(entityAttributes[1].toString(), SubjectTestHelper.SUBJ0.getName())) {
        subject0EntityUUID = entityAttributes[0].toString();
      }
      if (StringUtils.equals(entityAttributes[1].toString(), SubjectTestHelper.SUBJ1.getName())) {
        subject1EntityUUID2 = entityAttributes[0].toString();
      }
    }
  
    assertEquals(subject0EntityUUID, gcGrouperSyncMember.getEntityAttributeValueCache0());
  
    assertTrue(entityNameToAllAttributes.containsKey(SubjectTestHelper.SUBJ0.getName()));
    assertTrue(entityNameToAllAttributes.containsKey(SubjectTestHelper.SUBJ1.getName()));
    
    List<Object[]> memberships = new GcDbAccess().sql("select group_uuid, entity_uuid from testgrouper_prov_mship2").selectList(Object[].class);
    
    Set<MultiKey> groupIdSubjectIdsInTable = new HashSet<MultiKey>();
    
    for (Object[] membershipAttributes: memberships) {
      groupIdSubjectIdsInTable.add(new MultiKey(membershipAttributes[0], membershipAttributes[1]));
    }
    
    assertEquals(2, groupIdSubjectIdsInTable.size());
    
    assertTrue(groupIdSubjectIdsInTable.contains(new MultiKey(testGroupUuidInTarget, subject0EntityUUID)));
    assertTrue(groupIdSubjectIdsInTable.contains(new MultiKey(testGroupUuidInTarget, subject1EntityUUID2)));
    
    // rename the entity in grouper
    
    HibernateSession.bySqlStatic().executeSql("update subject set name='my name is test.subject.0_new' where subjectid='test.subject.0'", null, null);
    HibernateSession.bySqlStatic().executeSql("update subjectattribute set value='my name is test.subject.0_new' where subjectid='test.subject.0' and name='name'", null, null);
    Hib3MemberDAO.membersCacheClear();
    SubjectFinder.flushCache();
    Subject subject = SubjectFinder.findById("test.subject.0", true);
    Member member = MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), subject);
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(true), "OTHER_JOB_usduDaemon");
  
    incrementalProvision();
    grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    grouperProvisioningOutput = grouperProvisioner.retrieveGrouperProvisioningOutput();
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
  
    //get the grouper_sync and check cols
    gcGrouperSync = GcGrouperSyncDao.retrieveByProvisionerName(null, "sqlProvTest");
    
    testSubject0member = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ0, true);
    
    gcGrouperSyncMember = gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberId(testSubject0member.getId());
    assertEquals("my name is test.subject.0_new", gcGrouperSyncMember.getEntityAttributeValueCache1());
  
    entities = new GcDbAccess().sql("select uuid, name, subject_id_or_identifier, description from testgrouper_prov_entity").selectList(Object[].class);
    assertEquals(2, entities.size());
    
    subject0EntityUUID = null;
    subject1EntityUUID2 = null;
    
    entityNameToAllAttributes = new HashMap<String, Object[]>();
    for (Object[] entityAttributes: entities) {
      entityNameToAllAttributes.put(entityAttributes[1].toString(), entityAttributes);
      if (StringUtils.equals(entityAttributes[1].toString(), subject0name)) {
        subject0EntityUUID = entityAttributes[0].toString();
      }
      if (StringUtils.equals(entityAttributes[1].toString(), SubjectTestHelper.SUBJ1.getName())) {
        subject1EntityUUID2 = entityAttributes[0].toString();
      }
    }
    
    // this wont work until entity recalc from message works
    assertEquals(subject0EntityUUID, gcGrouperSyncMember.getEntityAttributeValueCache0());
  
    assertTrue(entityNameToAllAttributes.containsKey(subject0name));
    assertTrue(entityNameToAllAttributes.containsKey(SubjectTestHelper.SUBJ1.getName()));
  
    
  }

  /**
   * just do a simple full sync of groups and memberships
   */
  public void testSimpleGroupLdapPaObjectCache() {
    
    long started = System.currentTimeMillis();
    
    SqlProvisionerTestUtils.configureSqlProvisioner(new SqlProvisionerTestConfigInput()
        .assignCacheObjects(true)
        .assignGroupDeleteType("deleteGroupsIfGrouperDeleted")
        .assignMembershipDeleteType("deleteMembershipsIfNotExistInGrouper")
        .assignEntityAttributesTable(true)
        .assignGroupAttributesTable(true)
        .assignGroupTableName("testgrouper_prov_ldap_group")
        .assignGroupTableIdColumn("uuid")
        .assignEntityTableName("testgrouper_prov_ldap_entity")
        .assignEntityTableIdColumn("entity_uuid")
        .assignHasTargetEntityLink(true)
        .assignEntityAttributeCount(3)
        .assignGroupAttributeCount(6)
        .assignProvisioningType("groupAttributes")
        );

  
        
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
    attributeValue.setDoProvision("sqlProvTest");
    attributeValue.setTargetName("sqlProvTest");
    attributeValue.setStemScopeString("sub");
  
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);
  
    // add some entities
    for (int i=0;i<10;i++) {
      String uuid = GrouperUuid.getUuid();
      String dn = "dn_test.subject." + i;
      new GcDbAccess().sql("insert into testgrouper_prov_ldap_entity (entity_uuid) values (?)").addBindVar(uuid).executeSql();
      new GcDbAccess().sql("insert into testgrouper_pro_dap_entity_attr (entity_uuid, entity_attribute_name, entity_attribute_value) values (?,?,?)")
        .addBindVar(uuid).addBindVar("dn").addBindVar(dn).executeSql();
      new GcDbAccess().sql("insert into testgrouper_pro_dap_entity_attr (entity_uuid, entity_attribute_name, entity_attribute_value) values (?,?,?)")
        .addBindVar(uuid).addBindVar("employeeId").addBindVar("test.subject." + i).executeSql();
    }
    
    
    //AttributeAssign attributeAssign = stem.getAttributeDelegate().addAttribute(GrouperProvisioningAttributeNames.retrieveAttributeDefNameMarker()).getAttributeAssign();
    //attributeAssign.getAttributeValueDelegate().assignValueString(GrouperProvisioningAttributeNames.retrieveAttributeDefNameDoProvision())
    
    //lets sync these over
    GrouperProvisioningOutput grouperProvisioningOutput = fullProvision();
    GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    
    // make sure some time has passed
    GrouperUtil.sleep(1000);
    
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
  
    String sql = "select uuid from testgrouper_prov_ldap_group";
    
    List<Object[]> dataInTable = new GcDbAccess().sql(sql).selectList(Object[].class);
    
    Set<MultiKey> groupNamesInTable = new HashSet<MultiKey>();
    
    for (Object[] row: dataInTable) {
      groupNamesInTable.add(new MultiKey(row));
    }
    
    assertEquals(1, groupNamesInTable.size());
    assertTrue(groupNamesInTable.contains(new MultiKey(new Object[]{"test:testGroup"})));
  
    sql = "select group_uuid, attribute_name, attribute_value from testgrouper_pro_ldap_group_attr";
    
    dataInTable = new GcDbAccess().sql(sql).selectList(Object[].class);
    
    Set<MultiKey> attributesInTable = new HashSet<MultiKey>();
    
    for (Object[] row: dataInTable) {
      attributesInTable.add(new MultiKey(row));
    }
    
    assertEquals(6, attributesInTable.size());
    
    assertTrue(attributesInTable.contains(new MultiKey("test:testGroup", "cn", "test:testGroup")));
    assertTrue(attributesInTable.contains(new MultiKey("test:testGroup", "dn", "cn=test:testGroup,OU=Grouper,OU=365Groups,DC=one,DC=upenn,DC=edu")));
    assertTrue(attributesInTable.contains(new MultiKey("test:testGroup", "gidNumber", "" + testGroup.getIdIndex())));
    assertTrue(attributesInTable.contains(new MultiKey("test:testGroup", "objectClass", "group")));
    assertTrue(attributesInTable.contains(new MultiKey("test:testGroup", "member", "dn_test.subject.0")));
    assertTrue(attributesInTable.contains(new MultiKey("test:testGroup", "member", "dn_test.subject.1")));
    //object changes
    assertEquals(0, GrouperUtil.length(grouperProvisioner.retrieveGrouperProvisioningDataChanges().getTargetObjectInserts().getProvisioningGroups()));
    assertEquals(0, GrouperUtil.length(grouperProvisioner.retrieveGrouperProvisioningDataChanges().getTargetObjectInserts().getProvisioningEntities()));
    assertEquals(0, GrouperUtil.length(grouperProvisioner.retrieveGrouperProvisioningDataChanges().getTargetObjectInserts().getProvisioningMemberships()));
    assertEquals(1, GrouperUtil.length(grouperProvisioner.retrieveGrouperProvisioningDataChanges().getTargetObjectUpdates().getProvisioningGroups()));
    assertEquals(0, GrouperUtil.length(grouperProvisioner.retrieveGrouperProvisioningDataChanges().getTargetObjectUpdates().getProvisioningEntities()));
    assertEquals(0, GrouperUtil.length(grouperProvisioner.retrieveGrouperProvisioningDataChanges().getTargetObjectUpdates().getProvisioningMemberships()));
    assertEquals(0, GrouperUtil.length(grouperProvisioner.retrieveGrouperProvisioningDataChanges().getTargetObjectDeletes().getProvisioningGroups()));
    assertEquals(0, GrouperUtil.length(grouperProvisioner.retrieveGrouperProvisioningDataChanges().getTargetObjectDeletes().getProvisioningEntities()));
    assertEquals(0, GrouperUtil.length(grouperProvisioner.retrieveGrouperProvisioningDataChanges().getTargetObjectDeletes().getProvisioningMemberships()));
    
    // field changes
    assertEquals(2, GrouperUtil.length(grouperProvisioner.retrieveGrouperProvisioningDataChanges().getTargetObjectUpdates().getProvisioningGroups().iterator().next().getInternal_objectChanges()));
    List<ProvisioningObjectChange> provisioningObjectChanges = new ArrayList<ProvisioningObjectChange>(
        grouperProvisioner.retrieveGrouperProvisioningDataChanges().getTargetObjectUpdates().getProvisioningGroups().iterator().next().getInternal_objectChanges());
    assertEquals(ProvisioningObjectChangeAction.insert, provisioningObjectChanges.get(0).getProvisioningObjectChangeAction());
    assertEquals(ProvisioningObjectChangeAction.insert, provisioningObjectChanges.get(1).getProvisioningObjectChangeAction());
    assertEquals("member", provisioningObjectChanges.get(0).getAttributeName());
    assertEquals("member", provisioningObjectChanges.get(1).getAttributeName());
  
    //get the grouper_sync and check cols
    GcGrouperSync gcGrouperSync = GcGrouperSyncDao.retrieveByProvisionerName(null, "sqlProvTest");
    assertEquals(1, gcGrouperSync.getGroupCount().intValue());
    assertEquals(2, gcGrouperSync.getUserCount().intValue());
    assertEquals(2, gcGrouperSync.getRecordsCount().intValue());
    assertTrue(started <  gcGrouperSync.getLastFullSyncRun().getTime());
    assertTrue(new Timestamp(System.currentTimeMillis()) + " > " + new Timestamp(gcGrouperSync.getLastFullSyncRun().getTime()), System.currentTimeMillis() >  gcGrouperSync.getLastFullSyncRun().getTime());
    assertTrue(started < gcGrouperSync.getLastUpdated().getTime());
    assertTrue(System.currentTimeMillis() > gcGrouperSync.getLastUpdated().getTime());
    
    GcGrouperSyncJob gcGrouperSyncJob = gcGrouperSync.getGcGrouperSyncJobDao().jobRetrieveBySyncType("fullProvisionFull");
    assertEquals(100, gcGrouperSyncJob.getPercentComplete().intValue());
    assertEquals(GcGrouperSyncJobState.notRunning, gcGrouperSyncJob.getJobState());
    assertTrue(started < gcGrouperSyncJob.getLastSyncTimestamp().getTime());
    assertTrue(System.currentTimeMillis() > gcGrouperSyncJob.getLastSyncTimestamp().getTime());
    assertTrue(started < gcGrouperSyncJob.getLastTimeWorkWasDone().getTime());
    assertTrue(System.currentTimeMillis() > gcGrouperSyncJob.getLastTimeWorkWasDone().getTime());
    assertTrue(started < gcGrouperSyncJob.getHeartbeat().getTime());
    assertTrue(System.currentTimeMillis() > gcGrouperSyncJob.getHeartbeat().getTime());
    assertTrue(started < gcGrouperSyncJob.getLastUpdated().getTime());
    assertTrue(System.currentTimeMillis() > gcGrouperSyncJob.getLastUpdated().getTime());
    assertNull(gcGrouperSyncJob.getErrorMessage());
    assertNull(gcGrouperSyncJob.getErrorTimestamp());
    
    GcGrouperSyncGroup gcGrouperSyncGroup = gcGrouperSync.getGcGrouperSyncGroupDao().groupRetrieveByGroupId(testGroup.getId());
    assertEquals(testGroup.getId(), gcGrouperSyncGroup.getGroupId());
    assertEquals(testGroup.getName(), gcGrouperSyncGroup.getGroupName());
    assertEquals(testGroup.getIdIndex(), gcGrouperSyncGroup.getGroupIdIndex());
    assertEquals("T", gcGrouperSyncGroup.getProvisionableDb());
    assertEquals("T", gcGrouperSyncGroup.getInTargetDb());
    assertEquals("T", gcGrouperSyncGroup.getInTargetInsertOrExistsDb());
    assertTrue(started < gcGrouperSyncGroup.getInTargetStart().getTime());
    assertTrue(System.currentTimeMillis() > gcGrouperSyncGroup.getInTargetStart().getTime());
    assertNull(gcGrouperSyncGroup.getInTargetEnd());
    assertTrue(started < gcGrouperSyncGroup.getProvisionableStart().getTime());
    assertTrue(System.currentTimeMillis() > gcGrouperSyncGroup.getProvisionableStart().getTime());
    assertNull(gcGrouperSyncGroup.getProvisionableEnd());
    assertTrue(started < gcGrouperSyncGroup.getLastUpdated().getTime());
    assertTrue(System.currentTimeMillis() > gcGrouperSyncGroup.getLastUpdated().getTime());
    assertEquals("cn=test:testGroup,OU=Grouper,OU=365Groups,DC=one,DC=upenn,DC=edu", gcGrouperSyncGroup.getGroupAttributeValueCache2());
    assertNull(gcGrouperSyncGroup.getGroupAttributeValueCache0());
    assertNull(gcGrouperSyncGroup.getGroupAttributeValueCache1());
    assertNull(gcGrouperSyncGroup.getGroupAttributeValueCache3());
    assertNull(gcGrouperSyncGroup.getLastGroupMetadataSync());
    assertNull(gcGrouperSyncGroup.getErrorMessage());
    assertNull(gcGrouperSyncGroup.getErrorTimestamp());
    assertNull(gcGrouperSyncGroup.getLastGroupSync());
  
    Member testSubject0member = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ0, true);
    
    GcGrouperSyncMember gcGrouperSyncMember = gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberId(testSubject0member.getId());
    assertEquals(testSubject0member.getId(), gcGrouperSyncMember.getMemberId());
    assertEquals(testSubject0member.getSubjectId(), gcGrouperSyncMember.getSubjectId());
    assertEquals(testSubject0member.getSubjectSourceId(), gcGrouperSyncMember.getSourceId());
    assertEquals(testSubject0member.getSubjectIdentifier0(), gcGrouperSyncMember.getSubjectIdentifier());
    assertEquals("T", gcGrouperSyncMember.getProvisionableDb());
    assertEquals("T", gcGrouperSyncMember.getInTargetDb());
    assertEquals("F", gcGrouperSyncMember.getInTargetInsertOrExistsDb());
    assertTrue(started < gcGrouperSyncMember.getInTargetStart().getTime());
    assertTrue(System.currentTimeMillis() > gcGrouperSyncMember.getInTargetStart().getTime());
    assertNull(gcGrouperSyncMember.getInTargetEnd());
    assertTrue(started < gcGrouperSyncMember.getProvisionableStart().getTime());
    assertTrue(System.currentTimeMillis() > gcGrouperSyncMember.getProvisionableStart().getTime());
    assertNull(gcGrouperSyncMember.getProvisionableEnd());
    assertTrue(started < gcGrouperSyncMember.getLastUpdated().getTime());
    assertTrue(System.currentTimeMillis() > gcGrouperSyncMember.getLastUpdated().getTime());
    assertNull(gcGrouperSyncMember.getEntityAttributeValueCache0());
    assertNull(gcGrouperSyncMember.getEntityAttributeValueCache1());
    String cacheJson = gcGrouperSyncMember.getEntityAttributeValueCache2();
    ProvisioningEntity provisioningEntity = new ProvisioningEntity();
    provisioningEntity.fromJsonForCache(cacheJson);
    assertEquals("dn_test.subject.0", provisioningEntity.retrieveAttributeValue("dn"));
    assertNull(gcGrouperSyncMember.getEntityAttributeValueCache3());
    assertNull(gcGrouperSyncMember.getLastUserMetadataSync());
    assertNull(gcGrouperSyncMember.getErrorMessage());
    assertNull(gcGrouperSyncMember.getErrorTimestamp());
    assertNull(gcGrouperSyncMember.getLastUserSync());
  
    GcGrouperSyncMembership gcGrouperSyncMembership = gcGrouperSync.getGcGrouperSyncMembershipDao().membershipRetrieveByGroupIdAndMemberId(testGroup.getId(), testSubject0member.getId());
    assertEquals("T", gcGrouperSyncMembership.getInTargetDb());
    assertEquals("T", gcGrouperSyncMembership.getInTargetInsertOrExistsDb());
    assertTrue(started < gcGrouperSyncMembership.getInTargetStart().getTime());
    assertTrue(System.currentTimeMillis() > gcGrouperSyncMembership.getInTargetStart().getTime());
    assertNull(gcGrouperSyncMembership.getInTargetEnd());
    assertTrue(started < gcGrouperSyncMembership.getLastUpdated().getTime());
    assertTrue(System.currentTimeMillis() > gcGrouperSyncMembership.getLastUpdated().getTime());
    assertNull(gcGrouperSyncMembership.getMembershipId());
    assertNull(gcGrouperSyncMembership.getMembershipId2());
    assertNull(gcGrouperSyncMembership.getErrorMessage());
    assertNull(gcGrouperSyncMembership.getErrorTimestamp());
  
  
  }
}
