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

import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.model.Table;

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
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningObjectChangeDataType;
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
import edu.internet2.middleware.grouper.cfg.GrouperHibernateConfig;
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
import edu.internet2.middleware.grouper.internal.util.GrouperUuid;
import edu.internet2.middleware.grouper.ldap.LdapAttribute;
import edu.internet2.middleware.grouper.ldap.LdapModificationItem;
import edu.internet2.middleware.grouper.ldap.LdapModificationResult;
import edu.internet2.middleware.grouper.ldap.LdapModificationType;
import edu.internet2.middleware.grouper.misc.GrouperFailsafe;
import edu.internet2.middleware.grouper.misc.GrouperStartup;
import edu.internet2.middleware.grouper.misc.SaveMode;
import edu.internet2.middleware.grouper.util.CommandLineExec;
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
import junit.textui.TestRunner;

/**
 * 
 * @author mchyzer
 *
 */
public class SqlProvisionerTest extends GrouperTest {

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
    TestRunner.run(new SqlProvisionerTest("testSimpleGroupLdapPaMatchingIdMissingValidation"));
    
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
  
  public void testIncrementalSyncSqlProvisionerFailsafe() {
    
    
    setupFailsafeJob();
    
    // # if provisioning in ui should be enabled
    //# {valueType: "boolean", required: true}
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("provisioningInUi.enable", "true");

    GrouperStartup.startup();
    
    // edu.internet2.middleware.grouper.changeLog.esb.consumer
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer.sqlProvTestCLC.class", EsbConsumer.class.getName());
    // edu.internet2.middleware.grouper.app.provisioning
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer.sqlProvTestCLC.publisher.class", ProvisioningConsumer.class.getName());
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer.sqlProvTestCLC.quartzCron",  "0 0 5 * * 2000");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer.sqlProvTestCLC.provisionerConfigId", "mySqlProvisioner1");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer.sqlProvTestCLC.provisionerJobSyncType", GrouperProvisioningType.incrementalProvisionChangeLog.name());
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer.sqlProvTestCLC.publisher.debug", "true");

    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.failsafeMinGroupSize").value("8").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.failsafeMaxOverallPercentMembershipsRemove").value("20").store();
        
    GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveProvisioner("mySqlProvisioner1");
    
    assertEquals(new Integer(0), new GcDbAccess().connectionName("grouper").sql("select count(1) from testgrouper_prov_group").select(int.class));
    
    GrouperProvisioningOutput grouperProvisioningOutput = grouperProvisioner.provision(GrouperProvisioningType.fullProvisionFull);
    
    runJobs(true, true);
    
    GrouperSession grouperSession = GrouperSession.startRootSession();

    
    //lets sync these over
    String jobName = "OTHER_JOB_sqlProvisionerFull";
    GrouperLoader.runOnceByJobName(this.grouperSession, jobName);
    
    assertFalse(GrouperFailsafe.isFailsafeIssue(jobName));
    
    List<Object[]> groups = new GcDbAccess().sql("select uuid, posix_id, name from testgrouper_prov_group order by name").selectList(Object[].class);
    assertEquals(10, groups.size());
    
    List<Object[]> entities = new GcDbAccess().sql("select uuid, name, subject_id_or_identifier, description from testgrouper_prov_entity order by name").selectList(Object[].class);
    assertEquals(10, entities.size());
    
    List<Object[]> memberships = new GcDbAccess().sql("select group_uuid, entity_uuid from testgrouper_prov_mship2").selectList(Object[].class);
    assertEquals(100, memberships.size());
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.failsafeMaxOverallPercentMembershipsRemove").value("25").store();
    
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
    runJobs(true, true);
        
    groups = new GcDbAccess().sql("select uuid, posix_id, name from testgrouper_prov_group order by name").selectList(Object[].class);
    assertEquals(10, groups.size());
    
    entities = new GcDbAccess().sql("select uuid, name, subject_id_or_identifier, description from testgrouper_prov_entity order by name").selectList(Object[].class);
    assertEquals(10, entities.size());
    
    memberships = new GcDbAccess().sql("select group_uuid, entity_uuid from testgrouper_prov_mship2").selectList(Object[].class);
    assertEquals(100, memberships.size());
    
    GrouperUtil.sleep(1000);
    
    assertFalse(GrouperFailsafe.isApproved(jobName));
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.failsafeMaxOverallPercentMembershipsRemove").value("60").store();
  
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
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.class").value("edu.internet2.middleware.grouper.app.sqlProvisioning.SqlProvisioner").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.dbExternalSystemConfigId").value("grouper").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.hasTargetEntityLink").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.hasTargetGroupLink").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.debugLog").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.deleteEntities").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.deleteEntitiesIfNotExistInGrouper").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.deleteGroups").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.deleteGroupsIfNotExistInGrouper").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.deleteMemberships").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.deleteMembershipsIfNotExistInGrouper").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.groupTableIdColumn").value("uuid").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.groupTableName").value("testgrouper_prov_group").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.insertEntities").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.insertGroups").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.insertMemberships").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.logAllObjectsVerbose").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.membershipEntityForeignKeyColumn").value("entity_uuid").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.membershipGroupForeignKeyColumn").value("group_uuid").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.membershipTableName").value("testgrouper_prov_mship2").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.membershipPrimaryKey").value("uuid").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.numberOfEntityAttributes").value("5").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.numberOfGroupAttributes").value("3").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.numberOfMembershipAttributes").value("3").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.operateOnGrouperEntities").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.operateOnGrouperGroups").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.operateOnGrouperMemberships").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.provisioningType").value("membershipObjects").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.selectAllEntities").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.selectEntities").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.selectGroups").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.selectMemberships").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.showAdvanced").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.showProvisioningDiagnostics").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.subjectSourcesToProvision").value("jdbc").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.tableStructures").value("defaultTableStructure").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.0.insert").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.0.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.0.name").value("uuid").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.0.select").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.0.translateToMemberSyncField").value("memberToId2").store();
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.0.translateExpressionCreateOnly").value("${edu.internet2.middleware.grouper.internal.util.GrouperUuid.getUuid()}").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.0.translateExpressionTypeCreateOnly").value("translationScript").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.1.insert").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.1.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.1.matchingId").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.1.name").value("name").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.1.searchAttribute").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.1.select").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.1.translateExpressionType").value("grouperProvisioningEntityField").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.1.translateFromGrouperProvisioningEntityField").value("name").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.1.update").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.2.insert").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.2.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.2.name").value("subject_id_or_identifier").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.2.select").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.2.translateExpressionType").value("grouperProvisioningEntityField").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.2.translateFromGrouperProvisioningEntityField").value("subjectId").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.2.update").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.3.insert").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.3.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.3.name").value("email").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.3.select").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.3.translateExpressionType").value("grouperProvisioningEntityField").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.3.translateFromGrouperProvisioningEntityField").value("email").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.3.update").value("true").store();
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.4.insert").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.4.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.4.name").value("description").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.4.select").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.4.translateExpressionType").value("grouperProvisioningEntityField").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.4.translateFromGrouperProvisioningEntityField").value("attribute__description").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.4.update").value("true").store();
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.0.insert").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.0.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.0.name").value("uuid").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.0.select").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.0.translateToGroupSyncField").value("groupToId2").store();
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.0.translateExpressionCreateOnly").value("${edu.internet2.middleware.grouper.internal.util.GrouperUuid.getUuid()}").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.0.translateExpressionTypeCreateOnly").value("translationScript").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.0.storageType").value("groupTableColumn").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.1.insert").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.1.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.1.matchingId").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.1.name").value("posix_id").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.1.select").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.1.translateExpressionType").value("grouperProvisioningGroupField").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.1.translateFromGrouperProvisioningGroupField").value("idIndex").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.1.update").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.1.valueType").value("long").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.1.storageType").value("groupTableColumn").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.2.insert").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.2.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.2.name").value("name").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.2.searchAttribute").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.2.select").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.2.translateExpressionType").value("grouperProvisioningGroupField").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.2.translateFromGrouperProvisioningGroupField").value("name").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.2.update").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.2.storageType").value("groupTableColumn").store();
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.3.insert").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.3.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.3.name").value("description").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.3.select").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.3.translateExpressionType").value("grouperProvisioningGroupField").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.3.translateFromGrouperProvisioningGroupField").value("attribute__description").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.3.update").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.3.storageType").value("groupTableColumn").store();
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetMembershipAttribute.0.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetMembershipAttribute.0.name").value("uuid").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetMembershipAttribute.0.insert").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetMembershipAttribute.0.select").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetMembershipAttribute.0.translateExpression").value("${edu.internet2.middleware.grouper.internal.util.GrouperUuid.getUuid()}").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetMembershipAttribute.0.translateExpressionType").value("translationScript").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetMembershipAttribute.1.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetMembershipAttribute.1.name").value("group_uuid").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetMembershipAttribute.1.translateExpressionType").value("groupSyncField").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetMembershipAttribute.1.translateFromGroupSyncField").value("groupToId2").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetMembershipAttribute.1.insert").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetMembershipAttribute.1.select").value("true").store();
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetMembershipAttribute.10.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetMembershipAttribute.11.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetMembershipAttribute.12.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetMembershipAttribute.13.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetMembershipAttribute.14.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetMembershipAttribute.15.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetMembershipAttribute.16.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetMembershipAttribute.17.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetMembershipAttribute.18.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetMembershipAttribute.19.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetMembershipAttribute.2.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetMembershipAttribute.2.name").value("entity_uuid").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetMembershipAttribute.2.translateExpressionType").value("memberSyncField").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetMembershipAttribute.2.translateFromMemberSyncField").value("memberToId2").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetMembershipAttribute.2.insert").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetMembershipAttribute.2.select").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetMembershipAttribute.2.update").value("true").store();
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetMembershipAttribute.3.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetMembershipAttribute.4.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetMembershipAttribute.5.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetMembershipAttribute.6.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetMembershipAttribute.7.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetMembershipAttribute.8.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetMembershipAttribute.9.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.updateEntities").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.updateGroups").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.userPrimaryKey").value("uuid").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.userTableName").value("testgrouper_prov_entity").store();
    
    // # if provisioning in ui should be enabled
    //# {valueType: "boolean", required: true}
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("provisioningInUi.enable", "true");

    GrouperStartup.startup();
    
    // edu.internet2.middleware.grouper.changeLog.esb.consumer
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer.sqlProvTestCLC.class", EsbConsumer.class.getName());
    // edu.internet2.middleware.grouper.app.provisioning
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer.sqlProvTestCLC.publisher.class", ProvisioningConsumer.class.getName());
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer.sqlProvTestCLC.quartzCron",  "0 0 5 * * 2000");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer.sqlProvTestCLC.provisionerConfigId", "mySqlProvisioner1");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer.sqlProvTestCLC.provisionerJobSyncType", GrouperProvisioningType.incrementalProvisionChangeLog.name());
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer.sqlProvTestCLC.publisher.debug", "true");

    try {
     
      
      GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveProvisioner("mySqlProvisioner1");
      
      assertEquals(new Integer(0), new GcDbAccess().connectionName("grouper").sql("select count(1) from testgrouper_prov_group").select(int.class));
      
      GrouperProvisioningOutput grouperProvisioningOutput = grouperProvisioner.provision(GrouperProvisioningType.fullProvisionFull);
      
      runJobs(true, true);
      
      GrouperSession grouperSession = GrouperSession.startRootSession();
      
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
      attributeValue.setDoProvision("mySqlProvisioner1");
      attributeValue.setTargetName("mySqlProvisioner1");
      attributeValue.setStemScopeString("sub");
  
      GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);
  
      assertEquals(new Integer(0), new GcDbAccess().connectionName("grouper").sql("select count(1) from testgrouper_prov_group").select(int.class));
      // assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperAzureGroup").list(GrouperAzureGroup.class).size());
      
      runJobs(true, true);
      
      assertEquals(new Integer(1), new GcDbAccess().connectionName("grouper").sql("select count(1) from testgrouper_prov_group").select(int.class));
      assertEquals(new Integer(2), new GcDbAccess().connectionName("grouper").sql("select count(1) from testgrouper_prov_entity").select(int.class));
      assertEquals(new Integer(2), new GcDbAccess().connectionName("grouper").sql("select count(1) from testgrouper_prov_mship2").select(int.class));
      
      //now remove one of the subjects from the testGroup
      testGroup.deleteMember(SubjectTestHelper.SUBJ1);
      runJobs(true, true);
      
      assertEquals(new Integer(1), new GcDbAccess().connectionName("grouper").sql("select count(1) from testgrouper_prov_group").select(int.class));
      assertEquals(new Integer(1), new GcDbAccess().connectionName("grouper").sql("select count(1) from testgrouper_prov_entity").select(int.class));
      assertEquals(new Integer(1), new GcDbAccess().connectionName("grouper").sql("select count(1) from testgrouper_prov_mship2").select(int.class));
      
      testGroup.addMember(SubjectTestHelper.SUBJ3);
      runJobs(true, true);
      
      assertEquals(new Integer(1), new GcDbAccess().connectionName("grouper").sql("select count(1) from testgrouper_prov_group").select(int.class));
      assertEquals(new Integer(2), new GcDbAccess().connectionName("grouper").sql("select count(1) from testgrouper_prov_entity").select(int.class));
      assertEquals(new Integer(2), new GcDbAccess().connectionName("grouper").sql("select count(1) from testgrouper_prov_mship2").select(int.class));
      
      //now delete the group and sync again
      testGroup.delete();
      runJobs(true, true);
      
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
    
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("entityAttributeResolver.globalSqlEntityResolver.grouperAttributeThatMatchesRow").value("subjectId").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("entityAttributeResolver.globalSqlEntityResolver.resolverType").value("sql").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("entityAttributeResolver.globalSqlEntityResolver.sqlConfigId").value("grouper").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("entityAttributeResolver.globalSqlEntityResolver.subjectSourceIdColumn").value("jdbc").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("entityAttributeResolver.globalSqlEntityResolver.subjectSearchMatchingColumn").value("subject_id_or_identifier").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("entityAttributeResolver.globalSqlEntityResolver.tableOrViewName").value("testgrouper_prov_entity1").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("entityAttributeResolver.globalSqlEntityResolver.columnNames").value("school,subject_id_or_identifier").store();
    
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("entityAttributeResolver.globalLdapConfig.baseDn").value("ou=People,dc=example,dc=edu").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("entityAttributeResolver.globalLdapConfig.grouperAttributeThatMatchesRecord").value("subjectId").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("entityAttributeResolver.globalLdapConfig.ldapAttributes").value("givenName,mail,objectClass").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("entityAttributeResolver.globalLdapConfig.ldapConfigId").value("personLdap").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("entityAttributeResolver.globalLdapConfig.resolverType").value("ldap").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("entityAttributeResolver.globalLdapConfig.searchScope").value("SUBTREE_SCOPE").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("entityAttributeResolver.globalLdapConfig.subjectSearchMatchingAttribute").value("mail").store();
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.entityAttributesNotInSubjectSource").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.globalSQLResolver").value("globalSqlEntityResolver").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.globalLDAPResolver").value("globalLdapConfig").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.useGlobalSQLResolver").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.useGlobalLDAPResolver").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.resolveAttributesWithSQL").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.resolveAttributesWithLDAP").value("true").store();
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.class").value("edu.internet2.middleware.grouper.app.sqlProvisioning.SqlProvisioner").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.dbExternalSystemConfigId").value("grouper").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.hasTargetEntityLink").value("true").store();
    //new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.hasTargetGroupLink").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.debugLog").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.deleteEntities").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.deleteEntitiesIfNotExistInGrouper").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.deleteGroups").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.deleteGroupsIfNotExistInGrouper").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.deleteMemberships").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.deleteMembershipsIfNotExistInGrouper").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.groupTableIdColumn").value("uuid").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.groupTableName").value("testgrouper_prov_group").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.insertEntities").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.insertGroups").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.insertMemberships").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.logAllObjectsVerbose").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.membershipEntityForeignKeyColumn").value("entity_uuid").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.membershipGroupForeignKeyColumn").value("group_uuid").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.membershipTableName").value("testgrouper_prov_mship2").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.membershipPrimaryKey").value("uuid").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.numberOfEntityAttributes").value("6").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.numberOfGroupAttributes").value("3").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.numberOfMembershipAttributes").value("3").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.operateOnGrouperEntities").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.operateOnGrouperGroups").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.operateOnGrouperMemberships").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.provisioningType").value("membershipObjects").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.selectAllEntities").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.selectEntities").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.selectGroups").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.selectMemberships").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.showAdvanced").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.showProvisioningDiagnostics").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.subjectSourcesToProvision").value("jdbc").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.tableStructures").value("defaultTableStructure").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.0.insert").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.0.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.0.name").value("uuid").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.0.select").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.0.translateToMemberSyncField").value("memberToId2").store();
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.0.translateExpression").value("${edu.internet2.middleware.grouper.internal.util.GrouperUuid.getUuid()}").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.0.translateExpressionType").value("translationScript").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.1.insert").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.1.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.1.matchingId").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.1.name").value("name").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.1.searchAttribute").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.1.select").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.1.translateExpressionType").value("grouperProvisioningEntityField").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.1.translateFromGrouperProvisioningEntityField").value("name").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.1.update").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.2.insert").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.2.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.2.name").value("subject_id_or_identifier").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.2.select").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.2.translateExpressionType").value("grouperProvisioningEntityField").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.2.translateFromGrouperProvisioningEntityField").value("subjectId").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.2.update").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.3.insert").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.3.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.3.name").value("email").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.3.select").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.3.translateExpressionType").value("grouperProvisioningEntityField").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.3.translateFromGrouperProvisioningEntityField").value("email").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.3.update").value("true").store();
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.4.insert").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.4.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.4.name").value("description").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.4.select").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.4.translateExpressionType").value("grouperProvisioningEntityField").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.4.translateFromGrouperProvisioningEntityField").value("attribute__description").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.4.update").value("true").store();
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.5.insert").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.5.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.5.name").value("school").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.5.select").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.5.translateExpressionType").value("translationScript").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.5.translateExpression").value("${grouperProvisioningEntity.retrieveAttributeValueString('entityAttributeResolverLdap__givenname')}").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.5.update").value("true").store();
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.0.insert").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.0.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.0.name").value("uuid").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.0.select").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.0.translateGrouperToGroupSyncField").value("groupFromId2").store();
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.0.translateExpression").value("${edu.internet2.middleware.grouper.internal.util.GrouperUuid.getUuid()}").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.0.translateExpressionType").value("translationScript").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.0.storageType").value("groupTableColumn").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.1.insert").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.1.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.1.matchingId").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.1.name").value("posix_id").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.1.select").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.1.translateExpressionType").value("grouperProvisioningGroupField").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.1.translateFromGrouperProvisioningGroupField").value("idIndex").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.1.update").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.1.valueType").value("long").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.1.storageType").value("groupTableColumn").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.2.insert").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.2.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.2.name").value("name").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.2.searchAttribute").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.2.select").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.2.translateExpressionType").value("grouperProvisioningGroupField").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.2.translateFromGrouperProvisioningGroupField").value("name").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.2.update").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.2.storageType").value("groupTableColumn").store();
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.3.insert").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.3.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.3.name").value("description").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.3.select").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.3.translateExpressionType").value("grouperProvisioningGroupField").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.3.translateFromGrouperProvisioningGroupField").value("attribute__description").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.3.update").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.3.storageType").value("groupTableColumn").store();
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetMembershipAttribute.0.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetMembershipAttribute.0.name").value("uuid").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetMembershipAttribute.0.insert").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetMembershipAttribute.0.select").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetMembershipAttribute.0.translateExpression").value("${edu.internet2.middleware.grouper.internal.util.GrouperUuid.getUuid()}").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetMembershipAttribute.0.translateExpressionType").value("translationScript").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetMembershipAttribute.1.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetMembershipAttribute.1.name").value("group_uuid").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetMembershipAttribute.1.translateExpressionType").value("groupSyncField").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetMembershipAttribute.1.translateFromGroupSyncField").value("groupFromId2").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetMembershipAttribute.1.insert").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetMembershipAttribute.1.select").value("true").store();
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetMembershipAttribute.10.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetMembershipAttribute.11.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetMembershipAttribute.12.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetMembershipAttribute.13.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetMembershipAttribute.14.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetMembershipAttribute.15.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetMembershipAttribute.16.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetMembershipAttribute.17.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetMembershipAttribute.18.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetMembershipAttribute.19.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetMembershipAttribute.2.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetMembershipAttribute.2.name").value("entity_uuid").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetMembershipAttribute.2.translateExpressionType").value("memberSyncField").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetMembershipAttribute.2.translateFromMemberSyncField").value("memberToId2").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetMembershipAttribute.2.insert").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetMembershipAttribute.2.select").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetMembershipAttribute.2.update").value("true").store();
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetMembershipAttribute.3.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetMembershipAttribute.4.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetMembershipAttribute.5.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetMembershipAttribute.6.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetMembershipAttribute.7.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetMembershipAttribute.8.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetMembershipAttribute.9.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.updateEntities").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.updateGroups").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.userPrimaryKey").value("uuid").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.userTableName").value("testgrouper_prov_entity").store();
    
    // # if provisioning in ui should be enabled
    //# {valueType: "boolean", required: true}
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("provisioningInUi.enable", "true");
        
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
    attributeValue.setDoProvision("mySqlProvisioner1");
    attributeValue.setTargetName("mySqlProvisioner1");
    attributeValue.setStemScopeString("sub");

    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);

    //AttributeAssign attributeAssign = stem.getAttributeDelegate().addAttribute(GrouperProvisioningAttributeNames.retrieveAttributeDefNameMarker()).getAttributeAssign();
    //attributeAssign.getAttributeValueDelegate().assignValueString(GrouperProvisioningAttributeNames.retrieveAttributeDefNameDoProvision())
    
    //lets sync these over
    GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveProvisioner("mySqlProvisioner1");
    
    GrouperProvisioningOutput grouperProvisioningOutput = grouperProvisioner.provision(GrouperProvisioningType.fullProvisionFull); 
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
    
    grouperProvisioner = GrouperProvisioner.retrieveProvisioner("mySqlProvisioner1");
    
    grouperProvisioningOutput = grouperProvisioner.provision(GrouperProvisioningType.fullProvisionFull); 
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
    
    grouperProvisioner = GrouperProvisioner.retrieveProvisioner("mySqlProvisioner1");
    
    grouperProvisioningOutput = grouperProvisioner.provision(GrouperProvisioningType.fullProvisionFull); 
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
    
    groups = new GcDbAccess().sql("select uuid, posix_id, name, description from testgrouper_prov_group").selectList(Object[].class);
    assertEquals(1, groups.size());
    
    assertEquals(testGroupUuidInTarget, groups.get(0)[0].toString());
    
    assertEquals(testGroup.getIdIndex().longValue(), ((BigDecimal)groups.get(0)[1]).longValue());
    assertEquals(testGroup.getName(), groups.get(0)[2]);
    assertEquals(testGroup.getDescription(), groups.get(0)[3]);
    
    // now delete the group
    testGroup.delete();
    
    grouperProvisioner = GrouperProvisioner.retrieveProvisioner("mySqlProvisioner1");
    
    grouperProvisioningOutput = grouperProvisioner.provision(GrouperProvisioningType.fullProvisionFull); 
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
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.columnNames").value("school").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.dbExternalSystemConfigId").value("grouper").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.entityAttributesNotInSubjectSource").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.useGlobalSQLResolver").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.useGlobalLDAPResolver").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.resolveAttributesWithSQL").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.resolveAttributesWithLDAP").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.selectAllSQLOnFull").value("false").store();
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.ldapConfigId").value("personLdap").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.baseDN").value("ou=People,dc=example,dc=edu").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.searchScope").value("SUBTREE_SCOPE").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.attributes").value("givenName,mail,objectClass").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.ldapMatchingSearchAttribute").value("mail").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.ldapMappingType").value("entityAttribute").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.ldapMappingEntityAttribute").value("subjectId").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.filterAllLDAPOnFull").value("false").store();
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.class").value("edu.internet2.middleware.grouper.app.sqlProvisioning.SqlProvisioner").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.dbExternalSystemConfigId").value("grouper").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.hasTargetEntityLink").value("true").store();
    //new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.hasTargetGroupLink").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.debugLog").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.deleteEntities").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.deleteEntitiesIfNotExistInGrouper").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.deleteGroups").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.deleteGroupsIfNotExistInGrouper").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.deleteMemberships").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.deleteMembershipsIfNotExistInGrouper").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.groupTableIdColumn").value("uuid").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.groupTableName").value("testgrouper_prov_group").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.insertEntities").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.insertGroups").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.insertMemberships").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.logAllObjectsVerbose").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.membershipEntityForeignKeyColumn").value("entity_uuid").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.membershipGroupForeignKeyColumn").value("group_uuid").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.membershipTableName").value("testgrouper_prov_mship2").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.membershipPrimaryKey").value("uuid").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.numberOfEntityAttributes").value("6").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.numberOfGroupAttributes").value("3").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.numberOfMembershipAttributes").value("3").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.operateOnGrouperEntities").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.operateOnGrouperGroups").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.operateOnGrouperMemberships").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.provisioningType").value("membershipObjects").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.selectAllEntities").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.selectEntities").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.selectGroups").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.selectMemberships").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.showAdvanced").value("true").store();

    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.sqlConfigId").value("grouper").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.sqlMappingEntityAttribute").value("subjectId").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.sqlMappingType").value("entityAttribute").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.subjectSearchMatchingColumn").value("subject_id_or_identifier").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.tableOrViewName").value("testgrouper_prov_entity1").store();
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.showProvisioningDiagnostics").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.subjectSourcesToProvision").value("jdbc").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.tableStructures").value("defaultTableStructure").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.0.insert").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.0.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.0.name").value("uuid").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.0.select").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.0.translateToMemberSyncField").value("memberToId2").store();
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.0.translateExpression").value("${edu.internet2.middleware.grouper.internal.util.GrouperUuid.getUuid()}").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.0.translateExpressionType").value("translationScript").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.1.insert").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.1.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.1.matchingId").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.1.name").value("name").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.1.searchAttribute").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.1.select").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.1.translateExpressionType").value("grouperProvisioningEntityField").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.1.translateFromGrouperProvisioningEntityField").value("name").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.1.update").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.2.insert").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.2.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.2.name").value("subject_id_or_identifier").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.2.select").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.2.translateExpressionType").value("grouperProvisioningEntityField").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.2.translateFromGrouperProvisioningEntityField").value("subjectId").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.2.update").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.3.insert").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.3.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.3.name").value("email").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.3.select").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.3.translateExpressionType").value("grouperProvisioningEntityField").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.3.translateFromGrouperProvisioningEntityField").value("email").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.3.update").value("true").store();
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.4.insert").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.4.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.4.name").value("description").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.4.select").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.4.translateExpressionType").value("grouperProvisioningEntityField").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.4.translateFromGrouperProvisioningEntityField").value("attribute__description").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.4.update").value("true").store();
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.5.insert").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.5.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.5.name").value("school").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.5.select").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.5.translateExpressionType").value("translationScript").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.5.translateExpression").value("${grouperProvisioningEntity.retrieveAttributeValueString('entityAttributeResolverLdap__givenname')}").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.5.update").value("true").store();
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.0.insert").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.0.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.0.name").value("uuid").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.0.select").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.0.translateGrouperToGroupSyncField").value("groupFromId2").store();
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.0.translateExpression").value("${edu.internet2.middleware.grouper.internal.util.GrouperUuid.getUuid()}").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.0.translateExpressionType").value("translationScript").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.0.storageType").value("groupTableColumn").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.1.insert").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.1.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.1.matchingId").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.1.name").value("posix_id").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.1.select").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.1.translateExpressionType").value("grouperProvisioningGroupField").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.1.translateFromGrouperProvisioningGroupField").value("idIndex").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.1.update").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.1.valueType").value("long").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.1.storageType").value("groupTableColumn").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.2.insert").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.2.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.2.name").value("name").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.2.searchAttribute").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.2.select").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.2.translateExpressionType").value("grouperProvisioningGroupField").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.2.translateFromGrouperProvisioningGroupField").value("name").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.2.update").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.2.storageType").value("groupTableColumn").store();

    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.3.insert").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.3.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.3.name").value("description").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.3.select").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.3.translateExpressionType").value("grouperProvisioningGroupField").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.3.translateFromGrouperProvisioningGroupField").value("attribute__description").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.3.update").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.3.storageType").value("groupTableColumn").store();
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetMembershipAttribute.0.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetMembershipAttribute.0.name").value("uuid").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetMembershipAttribute.0.insert").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetMembershipAttribute.0.select").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetMembershipAttribute.0.translateExpression").value("${edu.internet2.middleware.grouper.internal.util.GrouperUuid.getUuid()}").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetMembershipAttribute.0.translateExpressionType").value("translationScript").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetMembershipAttribute.1.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetMembershipAttribute.1.name").value("group_uuid").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetMembershipAttribute.1.translateExpressionType").value("groupSyncField").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetMembershipAttribute.1.translateFromGroupSyncField").value("groupFromId2").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetMembershipAttribute.1.insert").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetMembershipAttribute.1.select").value("true").store();
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetMembershipAttribute.2.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetMembershipAttribute.2.name").value("entity_uuid").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetMembershipAttribute.2.translateExpressionType").value("memberSyncField").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetMembershipAttribute.2.translateFromMemberSyncField").value("memberToId2").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetMembershipAttribute.2.insert").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetMembershipAttribute.2.select").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetMembershipAttribute.2.update").value("true").store();
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.updateEntities").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.updateGroups").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.userPrimaryKey").value("uuid").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.userTableName").value("testgrouper_prov_entity").store();
    
    // # if provisioning in ui should be enabled
    //# {valueType: "boolean", required: true}
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("provisioningInUi.enable", "true");
        
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
    attributeValue.setDoProvision("mySqlProvisioner1");
    attributeValue.setTargetName("mySqlProvisioner1");
    attributeValue.setStemScopeString("sub");

    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);

    //AttributeAssign attributeAssign = stem.getAttributeDelegate().addAttribute(GrouperProvisioningAttributeNames.retrieveAttributeDefNameMarker()).getAttributeAssign();
    //attributeAssign.getAttributeValueDelegate().assignValueString(GrouperProvisioningAttributeNames.retrieveAttributeDefNameDoProvision())
    
    //lets sync these over
    GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveProvisioner("mySqlProvisioner1");
    
    GrouperProvisioningOutput grouperProvisioningOutput = grouperProvisioner.provision(GrouperProvisioningType.fullProvisionFull); 
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
    
    grouperProvisioner = GrouperProvisioner.retrieveProvisioner("mySqlProvisioner1");
    
    grouperProvisioningOutput = grouperProvisioner.provision(GrouperProvisioningType.fullProvisionFull); 
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
    
    grouperProvisioner = GrouperProvisioner.retrieveProvisioner("mySqlProvisioner1");
    
    grouperProvisioningOutput = grouperProvisioner.provision(GrouperProvisioningType.fullProvisionFull); 
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
    
    groups = new GcDbAccess().sql("select uuid, posix_id, name, description from testgrouper_prov_group").selectList(Object[].class);
    assertEquals(1, groups.size());
    
    assertEquals(testGroupUuidInTarget, groups.get(0)[0].toString());
    
    assertEquals(testGroup.getIdIndex().longValue(), ((BigDecimal)groups.get(0)[1]).longValue());
    assertEquals(testGroup.getName(), groups.get(0)[2]);
    assertEquals(testGroup.getDescription(), groups.get(0)[3]);
    
    // now delete the group
    testGroup.delete();
    
    grouperProvisioner = GrouperProvisioner.retrieveProvisioner("mySqlProvisioner1");
    
    grouperProvisioningOutput = grouperProvisioner.provision(GrouperProvisioningType.fullProvisionFull); 
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
    
    groups = new GcDbAccess().sql("select uuid, posix_id, name, description from testgrouper_prov_group").selectList(Object[].class);
    assertEquals(0, groups.size());
    
    entities = new GcDbAccess().sql("select uuid, name, subject_id_or_identifier from testgrouper_prov_entity").selectList(Object[].class);
    assertEquals(0, entities.size());
    
    memberships = new GcDbAccess().sql("select group_uuid, entity_uuid from testgrouper_prov_mship2").selectList(Object[].class);
    assertEquals(0, memberships.size());
  }
  
  public void testSimpleGroupMembershipProvisioningFullWithAttributesTable() {
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.class").value("edu.internet2.middleware.grouper.app.sqlProvisioning.SqlProvisioner").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.dbExternalSystemConfigId").value("grouper").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.hasTargetEntityLink").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.hasTargetGroupLink").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.debugLog").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.deleteEntities").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.deleteEntitiesIfNotExistInGrouper").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.deleteGroups").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.deleteGroupsIfNotExistInGrouper").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.deleteMemberships").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.deleteMembershipsIfNotExistInGrouper").value("true").store();

    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.entityAttributesAttributeNameColumn").value("entity_attribute_name").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.entityAttributesAttributeValueColumn").value("entity_attribute_value").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.entityAttributesEntityForeignKeyColumn").value("entity_uuid").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.entityAttributesLastModifiedColumn").value("last_modified").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.entityAttributesLastModifiedColumnType").value("timestamp").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.entityAttributesTableName").value("testgrouper_pro_dap_entity_attr").store();
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.groupAttributesAttributeNameColumn").value("attribute_name").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.groupAttributesAttributeValueColumn").value("attribute_value").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.groupAttributesGroupForeignKeyColumn").value("group_uuid").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.groupAttributesLastModifiedColumn").value("last_modified").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.groupAttributesLastModifiedColumnType").value("timestamp").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.groupAttributesTableName").value("testgrouper_pro_ldap_group_attr").store();
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.groupTableIdColumn").value("uuid").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.groupTableName").value("testgrouper_prov_group").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.insertEntities").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.insertGroups").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.insertMemberships").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.logAllObjectsVerbose").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.membershipEntityForeignKeyColumn").value("entity_uuid").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.membershipGroupForeignKeyColumn").value("group_uuid").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.membershipTableName").value("testgrouper_prov_mship2").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.membershipPrimaryKey").value("uuid").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.numberOfEntityAttributes").value("5").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.numberOfGroupAttributes").value("4").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.numberOfMembershipAttributes").value("3").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.operateOnGrouperEntities").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.operateOnGrouperGroups").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.operateOnGrouperMemberships").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.provisioningType").value("membershipObjects").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.selectAllEntities").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.selectEntities").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.selectGroups").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.selectMemberships").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.showAdvanced").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.showProvisioningDiagnostics").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.subjectSourcesToProvision").value("jdbc").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.tableStructures").value("defaultTableStructure").store();
    
    
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.0.insert").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.0.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.0.name").value("uuid").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.0.select").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.0.translateGrouperToMemberSyncField").value("memberFromId2").store();
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.0.storageType").value("entityTableColumn").store();
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.0.translateExpression").value("${edu.internet2.middleware.grouper.internal.util.GrouperUuid.getUuid()}").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.0.translateExpressionType").value("translationScript").store();
    
    
    
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.1.insert").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.1.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.1.matchingId").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.1.name").value("name").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.1.searchAttribute").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.1.select").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.1.storageType").value("entityTableColumn").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.1.translateExpressionType").value("grouperProvisioningEntityField").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.1.translateFromGrouperProvisioningEntityField").value("name").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.1.update").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.2.insert").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.2.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.2.name").value("subject_id_or_identifier").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.2.select").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.2.storageType").value("entityTableColumn").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.2.translateExpressionType").value("grouperProvisioningEntityField").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.2.translateFromGrouperProvisioningEntityField").value("subjectId").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.2.update").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.3.insert").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.3.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.3.name").value("email").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.3.select").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.3.storageType").value("entityTableColumn").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.3.translateExpressionType").value("grouperProvisioningEntityField").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.3.translateFromGrouperProvisioningEntityField").value("email").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.3.update").value("true").store();
    
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.4.insert").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.4.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.4.name").value("description").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.4.select").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.4.storageType").value("separateAttributesTable").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.4.translateExpressionType").value("grouperProvisioningEntityField").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.4.translateFromGrouperProvisioningEntityField").value("attribute__description").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.4.update").value("true").store();
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.0.insert").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.0.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.0.name").value("uuid").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.0.select").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.0.storageType").value("groupTableColumn").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.0.translateGrouperToGroupSyncField").value("groupFromId2").store();
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.0.translateExpression").value("${edu.internet2.middleware.grouper.internal.util.GrouperUuid.getUuid()}").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.0.translateExpressionType").value("translationScript").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.1.insert").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.1.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.1.matchingId").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.1.name").value("posix_id").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.1.select").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.1.storageType").value("groupTableColumn").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.1.translateExpressionType").value("grouperProvisioningGroupField").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.1.translateFromGrouperProvisioningGroupField").value("idIndex").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.1.update").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.1.valueType").value("long").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.2.insert").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.2.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.2.name").value("name").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.2.searchAttribute").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.2.select").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.2.storageType").value("groupTableColumn").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.2.translateExpressionType").value("grouperProvisioningGroupField").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.2.translateFromGrouperProvisioningGroupField").value("name").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.2.update").value("true").store();

    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.3.insert").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.3.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.3.name").value("description").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.3.select").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.3.storageType").value("separateAttributesTable").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.3.translateExpressionType").value("grouperProvisioningGroupField").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.3.translateFromGrouperProvisioningGroupField").value("attribute__description").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.3.update").value("true").store();
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetMembershipAttribute.0.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetMembershipAttribute.0.name").value("uuid").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetMembershipAttribute.0.insert").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetMembershipAttribute.0.select").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetMembershipAttribute.0.translateExpression").value("${edu.internet2.middleware.grouper.internal.util.GrouperUuid.getUuid()}").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetMembershipAttribute.0.translateExpressionType").value("translationScript").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetMembershipAttribute.1.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetMembershipAttribute.1.name").value("group_uuid").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetMembershipAttribute.1.translateExpressionType").value("groupSyncField").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetMembershipAttribute.1.translateFromGroupSyncField").value("groupFromId2").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetMembershipAttribute.1.insert").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetMembershipAttribute.1.select").value("true").store();
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetMembershipAttribute.10.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetMembershipAttribute.11.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetMembershipAttribute.12.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetMembershipAttribute.13.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetMembershipAttribute.14.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetMembershipAttribute.15.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetMembershipAttribute.16.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetMembershipAttribute.17.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetMembershipAttribute.18.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetMembershipAttribute.19.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetMembershipAttribute.2.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetMembershipAttribute.2.name").value("entity_uuid").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetMembershipAttribute.2.translateExpressionType").value("memberSyncField").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetMembershipAttribute.2.translateFromMemberSyncField").value("memberFromId2").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetMembershipAttribute.2.insert").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetMembershipAttribute.2.select").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetMembershipAttribute.2.update").value("true").store();
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetMembershipAttribute.3.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetMembershipAttribute.4.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetMembershipAttribute.5.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetMembershipAttribute.6.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetMembershipAttribute.7.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetMembershipAttribute.8.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetMembershipAttribute.9.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.updateEntities").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.updateGroups").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.userPrimaryKey").value("uuid").store();
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.useSeparateTableForGroupAttributes").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.useSeparateTableForEntityAttributes").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.userTableName").value("testgrouper_prov_entity").store();
    
    // # if provisioning in ui should be enabled
    //# {valueType: "boolean", required: true}
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("provisioningInUi.enable", "true");

        
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
    attributeValue.setDoProvision("mySqlProvisioner1");
    attributeValue.setTargetName("mySqlProvisioner1");
    attributeValue.setStemScopeString("sub");

    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);

    //AttributeAssign attributeAssign = stem.getAttributeDelegate().addAttribute(GrouperProvisioningAttributeNames.retrieveAttributeDefNameMarker()).getAttributeAssign();
    //attributeAssign.getAttributeValueDelegate().assignValueString(GrouperProvisioningAttributeNames.retrieveAttributeDefNameDoProvision())
    
    //lets sync these over
    GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveProvisioner("mySqlProvisioner1");
    
    GrouperProvisioningOutput grouperProvisioningOutput = grouperProvisioner.provision(GrouperProvisioningType.fullProvisionFull);
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
    
    grouperProvisioner = GrouperProvisioner.retrieveProvisioner("mySqlProvisioner1");
    
    grouperProvisioningOutput = grouperProvisioner.provision(GrouperProvisioningType.fullProvisionFull); 
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
    
    grouperProvisioner = GrouperProvisioner.retrieveProvisioner("mySqlProvisioner1");
    
    grouperProvisioningOutput = grouperProvisioner.provision(GrouperProvisioningType.fullProvisionFull); 
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
    grouperProvisioner = GrouperProvisioner.retrieveProvisioner("mySqlProvisioner1");
    
    grouperProvisioningOutput = grouperProvisioner.provision(GrouperProvisioningType.fullProvisionFull); 
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
    
    String testSubject0Id = new GcDbAccess().sql("select uuid from testgrouper_prov_entity where subject_id_or_identifier = 'test.subject.0' ").select(String.class);

    List<Object[]> entityAttributes = new GcDbAccess().sql("select entity_attribute_name, entity_attribute_value from testgrouper_pro_dap_entity_attr where entity_uuid = '"+testSubject0Id+"'").selectList(Object[].class);
    assertEquals(1, entityAttributes.size());
    
    assertEquals("description", entityAttributes.get(0)[0]);
    assertEquals("newTestDescription", entityAttributes.get(0)[1]);
    
    // now delete the group
    testGroup.delete();
    
    grouperProvisioner = GrouperProvisioner.retrieveProvisioner("mySqlProvisioner1");
    
    grouperProvisioningOutput = grouperProvisioner.provision(GrouperProvisioningType.fullProvisionFull); 
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
          
          GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "group_uuid", Types.VARCHAR, "40", true, false);

          GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "attribute_name", Types.VARCHAR, "200", true, false);

          GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "attribute_value", Types.VARCHAR, "200", false, false);

          GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "last_modified", Types.TIMESTAMP, "200", false, false);

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
      
      /*

      provisioner.sqlProvTest.class = edu.internet2.middleware.grouper.app.sqlProvisioning.SqlProvisioner
provisioner.sqlProvTest.dbExternalSystemConfigId = grouper
provisioner.sqlProvTest.deleteGroups = true
provisioner.sqlProvTest.deleteGroupsIfNotExistInGrouper = true
provisioner.sqlProvTest.deleteMemberships = true
provisioner.sqlProvTest.deleteMembershipsIfNotExistInGrouper = true
provisioner.sqlProvTest.groupAttributesAttributeNameColumn = attribute_name
provisioner.sqlProvTest.groupAttributesAttributeValueColumn = attribute_value
provisioner.sqlProvTest.groupAttributesGroupForeignKeyColumn = group_uuid
provisioner.sqlProvTest.groupAttributesTableName = testgrouper_pro_ldap_group_attr
provisioner.sqlProvTest.groupTableIdColumn = uuid
provisioner.sqlProvTest.groupTableName = testgrouper_prov_ldap_group
provisioner.sqlProvTest.logAllObjectsVerbose = true
provisioner.sqlProvTest.debugLog = true
provisioner.sqlProvTest.hasTargetGroupLink = true
provisioner.sqlProvTest.insertGroups = true
provisioner.sqlProvTest.insertMemberships = true
provisioner.sqlProvTest.numberOfGroupAttributes = 4
provisioner.sqlProvTest.operateOnGrouperGroups = true
provisioner.sqlProvTest.operateOnGrouperMemberships = true
provisioner.sqlProvTest.provisioningType = groupAttributes
provisioner.sqlProvTest.selectGroups = true
provisioner.sqlProvTest.selectMemberships = true
provisioner.sqlProvTest.subjectSourcesToProvision = jdbc
provisioner.sqlProvTest.tableStructures = defaultTableStructure
provisioner.sqlProvTest.targetGroupAttribute.0.insert = true
provisioner.sqlProvTest.targetGroupAttribute.0.isFieldElseAttribute = false
provisioner.sqlProvTest.targetGroupAttribute.0.name = uuid
provisioner.sqlProvTest.targetGroupAttribute.0.select = true
provisioner.sqlProvTest.targetGroupAttribute.0.storageType = groupTableColumn
provisioner.sqlProvTest.targetGroupAttribute.0.translateToGroupSyncField = groupToId2
provisioner.mySqlProvisioner1.targetEntityAttribute.0.translateExpressionCreateOnly = ${edu.internet2.middleware.grouper.internal.util.GrouperUuid.getUuid()}
provisioner.mySqlProvisioner1.targetEntityAttribute.0.translateExpressionCreateOnlyType = translationScript
provisioner.sqlProvTest.targetGroupAttribute.1.insert = true
provisioner.sqlProvTest.targetGroupAttribute.1.isFieldElseAttribute = false
provisioner.sqlProvTest.targetGroupAttribute.1.matchingId = true
provisioner.sqlProvTest.targetGroupAttribute.1.name = name
provisioner.sqlProvTest.targetGroupAttribute.1.searchAttribute = true
provisioner.sqlProvTest.targetGroupAttribute.1.select = true
provisioner.sqlProvTest.targetGroupAttribute.1.storageType = separateAttributesTable
provisioner.sqlProvTest.targetGroupAttribute.1.translateExpressionType = grouperProvisioningGroupField
provisioner.sqlProvTest.targetGroupAttribute.1.translateFromGrouperProvisioningGroupField = name
provisioner.sqlProvTest.targetGroupAttribute.2.insert = true
provisioner.sqlProvTest.targetGroupAttribute.2.isFieldElseAttribute = false
provisioner.sqlProvTest.targetGroupAttribute.2.name = description
provisioner.sqlProvTest.targetGroupAttribute.2.select = true
provisioner.sqlProvTest.targetGroupAttribute.2.storageType = separateAttributesTable
provisioner.sqlProvTest.targetGroupAttribute.2.translateExpressionType = grouperProvisioningGroupField
provisioner.sqlProvTest.targetGroupAttribute.2.translateFromGrouperProvisioningGroupField = attribute__description
provisioner.sqlProvTest.targetGroupAttribute.2.update = true
provisioner.sqlProvTest.targetGroupAttribute.3.isFieldElseAttribute = false
provisioner.sqlProvTest.targetGroupAttribute.3.membershipAttribute = true
provisioner.sqlProvTest.targetGroupAttribute.3.multiValued = true
provisioner.sqlProvTest.targetGroupAttribute.3.name = subjectId
provisioner.sqlProvTest.targetGroupAttribute.3.storageType = separateAttributesTable
provisioner.sqlProvTest.targetGroupAttribute.3.translateFromMemberSyncField = subjectId
provisioner.sqlProvTest.updateGroups = true
provisioner.sqlProvTest.useSeparateTableForGroupAttributes = true
 */
      
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.class", "edu.internet2.middleware.grouper.app.sqlProvisioning.SqlProvisioner");
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.dbExternalSystemConfigId", "grouper");
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.deleteGroups", "true");
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.deleteGroupsIfNotExistInGrouper", "true");
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.deleteMemberships", "true");
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.deleteMembershipsIfNotExistInGrouper", "true");
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.groupAttributesAttributeNameColumn", "attribute_name");
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.groupAttributesAttributeValueColumn", "attribute_value");
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.groupAttributesGroupForeignKeyColumn", "group_uuid");
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.groupAttributesTableName", "testgrouper_pro_ldap_group_attr");
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.groupTableIdColumn", "uuid");
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.groupTableName", "testgrouper_prov_ldap_group");
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.hasTargetGroupLink", "true");
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.insertGroups", "true");
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.insertMemberships", "true");
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.logAllObjectsVerbose", "true");
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.debugLog", "true");
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.numberOfGroupAttributes", "4");
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.operateOnGrouperGroups", "true");
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.operateOnGrouperMemberships", "true");
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.provisioningType", "groupAttributes");
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.selectGroups", "true");
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.selectMemberships", "true");
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.subjectSourcesToProvision", "jdbc");
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.tableStructures", "defaultTableStructure");
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetGroupAttribute.0.insert", "true");
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetGroupAttribute.0.isFieldElseAttribute", "false");
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetGroupAttribute.0.name", "uuid");
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetGroupAttribute.0.select", "true");
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetGroupAttribute.0.storageType", "groupTableColumn");
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetGroupAttribute.0.translateToGroupSyncField", "groupToId2");
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetGroupAttribute.0.translateExpressionCreateOnly","${edu.internet2.middleware.grouper.internal.util.GrouperUuid.getUuid()}");
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetGroupAttribute.0.translateExpressionTypeCreateOnly","translationScript");
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetGroupAttribute.1.insert", "true");
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetGroupAttribute.1.isFieldElseAttribute", "false");
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetGroupAttribute.1.matchingId", "true");
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetGroupAttribute.1.name", "name");
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetGroupAttribute.1.searchAttribute", "true");
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetGroupAttribute.1.select", "true");
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetGroupAttribute.1.storageType", "separateAttributesTable");
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetGroupAttribute.1.translateExpressionType", "grouperProvisioningGroupField");
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetGroupAttribute.1.translateFromGrouperProvisioningGroupField", "name");
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetGroupAttribute.2.insert", "true");
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetGroupAttribute.2.isFieldElseAttribute", "false");
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetGroupAttribute.2.name", "description");
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetGroupAttribute.2.select", "true");
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetGroupAttribute.2.storageType", "separateAttributesTable");
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetGroupAttribute.2.translateExpressionType", "grouperProvisioningGroupField");
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetGroupAttribute.2.translateFromGrouperProvisioningGroupField", "attribute__description");
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetGroupAttribute.2.update", "true");
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetGroupAttribute.3.isFieldElseAttribute", "false");
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetGroupAttribute.3.membershipAttribute", "true");
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetGroupAttribute.3.multiValued", "true");
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetGroupAttribute.3.name", "subjectId");
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetGroupAttribute.3.storageType", "separateAttributesTable");
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetGroupAttribute.3.translateFromMemberSyncField", "subjectId");
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.updateGroups", "true");
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.useSeparateTableForGroupAttributes", "true");
  
          
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
      GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveProvisioner("sqlProvTest");
      
      GrouperProvisioningOutput grouperProvisioningOutput = grouperProvisioner.provision(GrouperProvisioningType.fullProvisionFull); 
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
        GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveProvisioner("sqlProvTest");
        
        GrouperProvisioningOutput grouperProvisioningOutput = grouperProvisioner.provision(GrouperProvisioningType.fullProvisionFull); 
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

        grouperProvisioner = GrouperProvisioner.retrieveProvisioner("sqlProvTest");
        
        grouperProvisioningOutput = grouperProvisioner.provision(GrouperProvisioningType.fullProvisionFull); 
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

    
    
    // # if provisioning in ui should be enabled
    //# {valueType: "boolean", required: true}
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("provisioningInUi.enable", "true");
  
        
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
    GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveProvisioner("sqlProvTest");
    
    GrouperProvisioningOutput grouperProvisioningOutput = grouperProvisioner.provision(GrouperProvisioningType.fullProvisionFull); 
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());

    Hib3GrouperLoaderLog hib3GrouperLoaderLog = grouperProvisioner.getGrouperProvisioningOutput().getHib3GrouperLoaderLog();
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
        "0 0 5 * * 2000");

    // we dont need an EL filter
//    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer." + JOB_NAME + ".elfilter", 
//        "(event.eventType == 'MEMBERSHIP_DELETE' || event.eventType == 'MEMBERSHIP_ADD' || event.eventType == 'MEMBERSHIP_UPDATE')  && event.sourceId == 'jdbc' ");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer." + JOB_NAME + ".provisionerConfigId", "sqlProvTest");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer." + JOB_NAME + ".provisionerJobSyncType", 
        GrouperProvisioningType.incrementalProvisionChangeLog.name());

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer." + JOB_NAME + ".publisher.debug", "true");

    //clear out changelog
    // run the provisioner, it will init
    hib3GrouperLoaderLog = runJobs(true, true);
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
    hib3GrouperLoaderLog = runJobs(true, true);

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
    hib3GrouperLoaderLog = runJobs(true, true);
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
    grouperProvisioner = GrouperProvisioner.retrieveProvisioner("sqlProvTest");
    grouperProvisioningOutput = grouperProvisioner.provision(GrouperProvisioningType.fullProvisionFull); 
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
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.class", SqlProvisioner.class.getName());
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.dbExternalSystemConfigId", "grouper");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.subjectSourcesToProvision", "jdbc");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.logAllObjectsVerbose", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.debugLog", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.provisioningType", "groupAttributes");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.tableStructures", "defaultTableStructure");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.groupTableName", "testgrouper_prov_ldap_group");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.groupTableIdColumn", "uuid");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.groupAttributesTableName", "testgrouper_pro_ldap_group_attr");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.groupAttributeTableAttributeNameIsGroupMatchingId", "groupName");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.groupAttributesGroupForeignKeyColumn", "group_uuid");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.groupAttributesAttributeNameColumn", "attribute_name");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.groupAttributesAttributeValueColumn", "attribute_value");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.numberOfGroupAttributes", "3");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetGroupAttribute.0.name", "uuid");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetGroupAttribute.0.isFieldElseAttribute", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetGroupAttribute.0.valueType", "string");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetGroupAttribute.0.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetGroupAttribute.0.update", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetGroupAttribute.0.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetGroupAttribute.0.matchingId", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetGroupAttribute.0.searchAttribute", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetGroupAttribute.0.multiValued", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetGroupAttribute.0.translateExpressionType", "grouperProvisioningGroupField");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetGroupAttribute.0.translateFromGrouperProvisioningGroupField", "name");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetGroupAttribute.0.storageType", "groupTableColumn");

    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetGroupAttribute.1.name", "subjectId");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetGroupAttribute.1.isFieldElseAttribute", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetGroupAttribute.1.valueType", "string");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetGroupAttribute.1.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetGroupAttribute.1.update", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetGroupAttribute.1.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetGroupAttribute.1.matchingId", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetGroupAttribute.1.multiValued", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetGroupAttribute.1.membershipAttribute", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetGroupAttribute.1.translateFromMemberSyncField", "subjectId");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetGroupAttribute.1.storageType", "separateAttributesTable");
  
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetGroupAttribute.2.name", "groupName");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetGroupAttribute.2.isFieldElseAttribute", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetGroupAttribute.2.valueType", "string");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetGroupAttribute.2.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetGroupAttribute.2.update", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetGroupAttribute.2.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetGroupAttribute.2.storageType", "separateAttributesTable");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetGroupAttribute.2.translateExpressionType", "grouperProvisioningGroupField");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetGroupAttribute.2.translateFromGrouperProvisioningGroupField", "name");
  
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.operateOnGrouperGroups", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.operateOnGrouperMemberships", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.selectGroups", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.selectMemberships", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.insertGroups", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.insertMemberships", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.deleteGroups", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.deleteGroupsIfNotExistInGrouper", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.deleteMemberships", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.deleteMembershipsIfNotExistInGrouper", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.updateGroups", "true");
  }

  /**
   * esb consumer
   */
  private EsbConsumer esbConsumer;

  /**
   * 
   */
  private final String JOB_NAME = "sqlProvTestCLC";

  /**
   * 
   */
  private Hib3GrouperLoaderLog runJobs(boolean runChangeLog, boolean runConsumer) {
    
    if (runChangeLog) {
      ChangeLogTempToEntity.convertRecords();
    }
    
    if (runConsumer) {
      Hib3GrouperLoaderLog hib3GrouploaderLog = new Hib3GrouperLoaderLog();
      hib3GrouploaderLog.setHost(GrouperUtil.hostname());
      hib3GrouploaderLog.setJobName("CHANGE_LOG_consumer_sqlProvTestCLC");
      hib3GrouploaderLog.setStatus(GrouperLoaderStatus.RUNNING.name());
      this.esbConsumer = new EsbConsumer();
      ChangeLogHelper.processRecords("sqlProvTestCLC", hib3GrouploaderLog, this.esbConsumer);
  
      return hib3GrouploaderLog;
    }
    
    return null;
  }

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
    GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveProvisioner("sqlProvTest");
    
    GrouperProvisioningOutput grouperProvisioningOutput = grouperProvisioner.provision(GrouperProvisioningType.fullProvisionFull); 
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
  
    grouperProvisioner = GrouperProvisioner.retrieveProvisioner("sqlProvTest");
    
    grouperProvisioningOutput = grouperProvisioner.provision(GrouperProvisioningType.fullProvisionFull); 
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
          "0 0 5 * * 2000");
  
      // we dont need an EL filter
  //    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer." + JOB_NAME + ".elfilter", 
  //        "(event.eventType == 'MEMBERSHIP_DELETE' || event.eventType == 'MEMBERSHIP_ADD' || event.eventType == 'MEMBERSHIP_UPDATE')  && event.sourceId == 'jdbc' ");
  
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer." + JOB_NAME + ".provisionerConfigId", "sqlProvTest");
  
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer." + JOB_NAME + ".provisionerJobSyncType", 
          GrouperProvisioningType.incrementalProvisionChangeLog.name());
  
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer." + JOB_NAME + ".publisher.debug", "true");
  
      //clear out changelog
      // run the provisioner, it will init
      hib3GrouperLoaderLog = runJobs(true, true);
      
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
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.class", SqlProvisioner.class.getName());
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.dbExternalSystemConfigId", "grouper");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.provisioningType", "groupAttributes");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.pspng_oneprod.tableStructures", "defaultTableStructure");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.subjectSourcesToProvision", "jdbc");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.logAllObjectsVerbose", "true");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.numberOfGroupAttributes", "3");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetGroupAttribute.0.name", "uuid");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetGroupAttribute.0.isFieldElseAttribute", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetGroupAttribute.0.valueType", "string");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetGroupAttribute.0.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetGroupAttribute.0.update", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetGroupAttribute.0.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetGroupAttribute.0.matchingId", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetGroupAttribute.0.searchAttribute", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetGroupAttribute.0.multiValued", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetGroupAttribute.0.translateExpressionType", "grouperProvisioningGroupField");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetGroupAttribute.0.translateFromGrouperProvisioningGroupField", "id");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetGroupAttribute.0.storageType", "groupTableColumn");

    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetGroupAttribute.1.name", "subjectId");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetGroupAttribute.1.isFieldElseAttribute", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetGroupAttribute.1.valueType", "string");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetGroupAttribute.1.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetGroupAttribute.1.update", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetGroupAttribute.1.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetGroupAttribute.1.matchingId", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetGroupAttribute.1.multiValued", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetGroupAttribute.1.membershipAttribute", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetGroupAttribute.1.translateFromMemberSyncField", "subjectId");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetGroupAttribute.1.storageType", "separateAttributesTable");
  
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetGroupAttribute.2.name", "groupName");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetGroupAttribute.2.isFieldElseAttribute", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetGroupAttribute.2.valueType", "string");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetGroupAttribute.2.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetGroupAttribute.2.update", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetGroupAttribute.2.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetGroupAttribute.2.storageType", "separateAttributesTable");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetGroupAttribute.2.translateExpressionType", "grouperProvisioningGroupField");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetGroupAttribute.2.translateFromGrouperProvisioningGroupField", "name");
  
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.groupTableName", "testgrouper_prov_ldap_group");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.groupTableIdColumn", "uuid");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.groupAttributesTableName", "testgrouper_pro_ldap_group_attr");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.groupAttributeTableAttributeNameIsGroupMatchingId", "groupName");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.groupAttributesGroupForeignKeyColumn", "group_uuid");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.groupAttributesAttributeNameColumn", "attribute_name");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.groupAttributesAttributeValueColumn", "attribute_value");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.userTableName", "testgrouper_prov_ldap_entity");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.userPrimaryKey", "entity_uuid");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.entityAttributesTableName", "testgrouper_pro_dap_entity_attr");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.entityAttributesTableAttributeNameIsEntityMatchingId", "entityName");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.entityAttributesEntityForeignKeyColumn", "entity_uuid");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.entityAttributesAttributeNameColumn", "entity_attribute_name");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.entityAttributesAttributeValueColumn", "entity_attribute_value");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.operateOnGrouperGroups", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.operateOnGrouperMemberships", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.selectGroups", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.selectMemberships", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.insertGroups", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.insertMemberships", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.deleteGroups", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.deleteGroupsIfNotExistInGrouper", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.deleteMemberships", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.deleteMembershipsIfNotExistInGrouper", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.updateGroups", "true");




    
    
    
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.numberOfEntityAttributes", "3");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetEntityAttribute.0.name", "entity_uuid");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetEntityAttribute.0.isFieldElseAttribute", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetEntityAttribute.0.valueType", "string");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetEntityAttribute.0.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetEntityAttribute.0.update", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetEntityAttribute.0.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetEntityAttribute.0.matchingId", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetEntityAttribute.0.searchAttribute", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetEntityAttribute.0.multiValued", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetEntityAttribute.0.translateExpressionType", "grouperProvisioningEntityField");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetEntityAttribute.0.translateFromGrouperProvisioningEntityField", "id");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetEntityAttribute.0.storageType", "entityTableColumn");

    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetEntityAttribute.1.name", "dn");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetEntityAttribute.1.isFieldElseAttribute", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetEntityAttribute.1.valueType", "string");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetEntityAttribute.1.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetEntityAttribute.1.update", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetEntityAttribute.1.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetEntityAttribute.1.translateExpressionType", "grouperProvisioningEntityField");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetEntityAttribute.1.translateFromGrouperProvisioningEntityField", "subjectId");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetEntityAttribute.1.storageType", "separateAttributesTable");
  
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetEntityAttribute.2.name", "employeeId");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetEntityAttribute.2.isFieldElseAttribute", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetEntityAttribute.2.valueType", "string");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetEntityAttribute.2.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetEntityAttribute.2.update", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetEntityAttribute.2.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetEntityAttribute.2.storageType", "separateAttributesTable");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetEntityAttribute.2.translateExpressionType", "grouperProvisioningEntityField");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetEntityAttribute.2.translateFromGrouperProvisioningEntityField", "name");
  

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.selectEntities", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.insertEntities", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.deleteEntities", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.deleteEntitiesIfNotExistInGrouper", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.updateEntities", "true");

    
    
    
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
    grouperProvisioner.retrieveGrouperTargetDaoAdapter().getWrappedDao().registerGrouperProvisionerDaoCapabilities(
        grouperProvisioner.retrieveGrouperTargetDaoAdapter().getWrappedDao().getGrouperProvisionerDaoCapabilities()
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

    provisioningGroup1.addInternal_objectChange(new ProvisioningObjectChange(ProvisioningObjectChangeDataType.attribute, null, "uuid", ProvisioningObjectChangeAction.insert, null, "abc123"));
    provisioningGroup1.addInternal_objectChange(new ProvisioningObjectChange(ProvisioningObjectChangeDataType.attribute, null, "groupName", ProvisioningObjectChangeAction.insert, null, "a:b:c"));
    provisioningGroup1.addInternal_objectChange(new ProvisioningObjectChange(ProvisioningObjectChangeDataType.attribute, null, "subjectId", ProvisioningObjectChangeAction.insert, null, "subjectId0"));
    provisioningGroup1.addInternal_objectChange(new ProvisioningObjectChange(ProvisioningObjectChangeDataType.attribute, null, "subjectId", ProvisioningObjectChangeAction.insert, null, "subjectId1"));

    ProvisioningGroup provisioningGroup2 = new ProvisioningGroup();
    provisioningGroupWrapper = new ProvisioningGroupWrapper();
    provisioningGroupWrapper.setGrouperProvisioner(grouperProvisioner);
    provisioningGroup2.setProvisioningGroupWrapper(provisioningGroupWrapper);
    provisioningGroup2.assignAttributeValue("uuid", "def456");
    provisioningGroup2.assignAttributeValue("groupName", "d:e:f");
    provisioningGroup2.assignAttributeValue("subjectId", "subjectId2");
    provisioningGroup2.assignAttributeValue("subjectId", "subjectId3");

    provisioningGroup2.addInternal_objectChange(new ProvisioningObjectChange(ProvisioningObjectChangeDataType.attribute, null, "uuid", ProvisioningObjectChangeAction.insert, null, "def456"));
    provisioningGroup2.addInternal_objectChange(new ProvisioningObjectChange(ProvisioningObjectChangeDataType.attribute, null, "groupName", ProvisioningObjectChangeAction.insert, null, "d:e:f"));
    provisioningGroup2.addInternal_objectChange(new ProvisioningObjectChange(ProvisioningObjectChangeDataType.attribute, null, "subjectId", ProvisioningObjectChangeAction.insert, null, "subjectId2"));
    provisioningGroup2.addInternal_objectChange(new ProvisioningObjectChange(ProvisioningObjectChangeDataType.attribute, null, "subjectId", ProvisioningObjectChangeAction.insert, null, "subjectId3"));
    
    ProvisioningGroup provisioningGroup3 = new ProvisioningGroup();
    provisioningGroupWrapper = new ProvisioningGroupWrapper();
    provisioningGroupWrapper.setGrouperProvisioner(grouperProvisioner);
    provisioningGroup3.setProvisioningGroupWrapper(provisioningGroupWrapper);
    provisioningGroup3.assignAttributeValue("uuid", "ghi789");
    provisioningGroup3.assignAttributeValue("groupName", "g:h:i");
    provisioningGroup3.assignAttributeValue("subjectId", "subjectId4");
    provisioningGroup3.assignAttributeValue("subjectId", "subjectId5");

    provisioningGroup3.addInternal_objectChange(new ProvisioningObjectChange(ProvisioningObjectChangeDataType.attribute, null, "uuid", ProvisioningObjectChangeAction.insert, null, "ghi789"));
    provisioningGroup3.addInternal_objectChange(new ProvisioningObjectChange(ProvisioningObjectChangeDataType.attribute, null, "groupName", ProvisioningObjectChangeAction.insert, null, "g:h:i"));
    provisioningGroup3.addInternal_objectChange(new ProvisioningObjectChange(ProvisioningObjectChangeDataType.attribute, null, "subjectId", ProvisioningObjectChangeAction.insert, null, "subjectId4"));
    provisioningGroup3.addInternal_objectChange(new ProvisioningObjectChange(ProvisioningObjectChangeDataType.attribute, null, "subjectId", ProvisioningObjectChangeAction.insert, null, "subjectId5"));
    
    grouperProvisioner.retrieveGrouperTargetDaoAdapter().insertGroups(new TargetDaoInsertGroupsRequest(GrouperUtil.toList(provisioningGroup1, provisioningGroup2, provisioningGroup3)));

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
    TargetDaoRetrieveAllGroupsResponse targetDaoRetrieveAllGroupsResponse = grouperProvisioner.retrieveGrouperTargetDaoAdapter().retrieveAllGroups(new TargetDaoRetrieveAllGroupsRequest(true));
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
    TargetDaoRetrieveGroupsRequest targetDaoRetrieveGroupsRequest = new TargetDaoRetrieveGroupsRequest(
        GrouperUtil.toList(provisioningGroup1, provisioningGroup2), true);
    TargetDaoRetrieveGroupsResponse targetDaoRetrieveGroupsResponse = 
        grouperProvisioner.retrieveGrouperTargetDaoAdapter().retrieveGroups(targetDaoRetrieveGroupsRequest);
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
        GrouperUtil.toList(provisioningGroup1, provisioningGroup2), false);
    targetDaoRetrieveGroupsResponse = 
        grouperProvisioner.retrieveGrouperTargetDaoAdapter().retrieveGroups(targetDaoRetrieveGroupsRequest);
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

    provisioningGroup1update.addInternal_objectChange(new ProvisioningObjectChange(ProvisioningObjectChangeDataType.attribute, null, "groupName", ProvisioningObjectChangeAction.update, "a:b:c", "a:b:cu"));
    provisioningGroup1update.addInternal_objectChange(new ProvisioningObjectChange(ProvisioningObjectChangeDataType.attribute, null, "subjectId", ProvisioningObjectChangeAction.insert, null, "subjectId0i"));
    provisioningGroup1update.addInternal_objectChange(new ProvisioningObjectChange(ProvisioningObjectChangeDataType.attribute, null, "subjectId", ProvisioningObjectChangeAction.delete, "subjectId1", null));
    provisioningGroup1update.addInternal_objectChange(new ProvisioningObjectChange(ProvisioningObjectChangeDataType.attribute, null, "subjectId", ProvisioningObjectChangeAction.update, "subjectId0", "subjectId0u"));

    ProvisioningGroup provisioningGroup2update = new ProvisioningGroup();
    provisioningGroup2update.assignAttributeValue("uuid", "def456");
    provisioningGroup2update.assignAttributeValue("groupName", "d:e:f");
    provisioningGroup2update.assignAttributeValue("subjectId", "subjectId2");
    provisioningGroup2update.assignAttributeValue("subjectId", "subjectId3");

    provisioningGroup2update.addInternal_objectChange(new ProvisioningObjectChange(ProvisioningObjectChangeDataType.attribute, null, "groupName", ProvisioningObjectChangeAction.update, "d:e:f", "d:e:fu"));
    provisioningGroup2update.addInternal_objectChange(new ProvisioningObjectChange(ProvisioningObjectChangeDataType.attribute, null, "subjectId", ProvisioningObjectChangeAction.insert, null, "subjectId2i"));
    provisioningGroup2update.addInternal_objectChange(new ProvisioningObjectChange(ProvisioningObjectChangeDataType.attribute, null, "subjectId", ProvisioningObjectChangeAction.delete, "subjectId3", null));
    provisioningGroup2update.addInternal_objectChange(new ProvisioningObjectChange(ProvisioningObjectChangeDataType.attribute, null, "subjectId", ProvisioningObjectChangeAction.update, "subjectId2", "subjectId2u"));

    TargetDaoUpdateGroupsRequest targetDaoUpdateGroupsRequest = new TargetDaoUpdateGroupsRequest(
        GrouperUtil.toList(provisioningGroup1update, provisioningGroup2update));

    TargetDaoUpdateGroupsResponse targetDaoUpdateGroupsResponse = 
        grouperProvisioner.retrieveGrouperTargetDaoAdapter().updateGroups(targetDaoUpdateGroupsRequest);
    
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
    grouperProvisioner.retrieveGrouperTargetDaoAdapter().deleteGroups(new TargetDaoDeleteGroupsRequest(
        GrouperUtil.toList(provisioningGroup1, provisioningGroup2)));

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
    TargetDaoRetrieveAllEntitiesResponse targetDaoRetrieveAllEntitiesResponse = grouperProvisioner.retrieveGrouperTargetDaoAdapter().retrieveAllEntities(new TargetDaoRetrieveAllEntitiesRequest());
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
        
          GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, ddlVersionBean, tableName, "testgrouper_pro_ldap_en_idx0", null, false, "entity_uuid", "attribute_name");

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

    // # if provisioning in ui should be enabled
    //# {valueType: "boolean", required: true}
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("provisioningInUi.enable", "true");

        
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
    attributeValue.setDoProvision("pspng_oneprod");
    attributeValue.setTargetName("pspng_oneprod");
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
        .addBindVar(uuid).addBindVar("employeeID").addBindVar("test.subject." + i).executeSql();
    }
    
    
    //AttributeAssign attributeAssign = stem.getAttributeDelegate().addAttribute(GrouperProvisioningAttributeNames.retrieveAttributeDefNameMarker()).getAttributeAssign();
    //attributeAssign.getAttributeValueDelegate().assignValueString(GrouperProvisioningAttributeNames.retrieveAttributeDefNameDoProvision())
    
    //lets sync these over
    GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveProvisioner("pspng_oneprod");
    
    GrouperProvisioningOutput grouperProvisioningOutput = grouperProvisioner.provision(GrouperProvisioningType.fullProvisionFull); 
    
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
    GcGrouperSync gcGrouperSync = GcGrouperSyncDao.retrieveByProvisionerName(null, "pspng_oneprod");
    assertEquals(1, gcGrouperSync.getGroupCount().intValue());
    assertEquals(2, gcGrouperSync.getUserCount().intValue());
    assertEquals(1+2+2, gcGrouperSync.getRecordsCount().intValue());
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
    assertEquals("cn=test:testGroup,OU=Grouper,OU=365Groups,DC=one,DC=upenn,DC=edu", gcGrouperSyncGroup.getGroupToId2());
    assertNull(gcGrouperSyncGroup.getGroupFromId2());
    assertNull(gcGrouperSyncGroup.getGroupFromId3());
    assertNull(gcGrouperSyncGroup.getGroupToId3());
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
    assertNull(gcGrouperSyncMember.getMemberFromId2());
    assertNull(gcGrouperSyncMember.getMemberFromId3());
    assertEquals("dn_test.subject.0", gcGrouperSyncMember.getMemberToId2());
    assertNull(gcGrouperSyncMember.getMemberToId3());
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

  public void configureLdapPaTestCase() {
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.class").value(SqlProvisioner.class.getName()).store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.dbExternalSystemConfigId").value("grouper").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.subjectSourcesToProvision").value("jdbc").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.logAllObjectsVerbose").value("true").store();

    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.provisionerName").value("One prod LDAP flat").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.provisioningType").value("groupAttributes").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.tableStructures").value("defaultTableStructure").store();

    //TODO make an attribute config for this
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.hasTargetEntityLink").value("true").store();
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.numberOfGroupAttributes").value("6").store();

    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.targetGroupAttribute.0.name").value("cn").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.targetGroupAttribute.0.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.targetGroupAttribute.0.valueType").value("string").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.targetGroupAttribute.0.insert").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.targetGroupAttribute.0.select").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.targetGroupAttribute.0.multiValued").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.targetGroupAttribute.0.storageType").value("separateAttributesTable").store();
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.targetGroupAttribute.0.translateExpressionType").value("grouperProvisioningGroupField").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.targetGroupAttribute.0.translateFromGrouperProvisioningGroupField").value("name").store();

    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.targetGroupAttribute.1.name").value("dn").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.targetGroupAttribute.1.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.targetGroupAttribute.1.valueType").value("string").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.targetGroupAttribute.1.insert").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.targetGroupAttribute.1.select").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.targetGroupAttribute.1.multiValued").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.targetGroupAttribute.1.translateExpressionType").value("translationScript").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.targetGroupAttribute.1.translateExpression").value("${'cn=' + grouperProvisioningGroup.getName() + ',OU=Grouper,OU=365Groups,DC=one,DC=upenn,DC=edu'}").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.targetGroupAttribute.1.translateGrouperToGroupSyncField").value("groupToId2").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.targetGroupAttribute.1.storageType").value("separateAttributesTable").store();

    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.targetGroupAttribute.2.name").value("gidNumber").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.targetGroupAttribute.2.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.targetGroupAttribute.2.valueType").value("long").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.targetGroupAttribute.2.insert").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.targetGroupAttribute.2.select").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.targetGroupAttribute.2.matchingId").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.targetGroupAttribute.2.searchAttribute").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.targetGroupAttribute.2.multiValued").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.targetGroupAttribute.2.translateExpressionType").value("grouperProvisioningGroupField").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.targetGroupAttribute.2.translateFromGrouperProvisioningGroupField").value("idIndex").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.targetGroupAttribute.2.storageType").value("separateAttributesTable").store();

    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.targetGroupAttribute.3.name").value("objectClass").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.targetGroupAttribute.3.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.targetGroupAttribute.3.valueType").value("string").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.targetGroupAttribute.3.insert").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.targetGroupAttribute.3.select").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.targetGroupAttribute.3.multiValued").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.targetGroupAttribute.3.translateExpressionType").value("translationScript").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.targetGroupAttribute.3.translateExpression").value("${grouperUtil.toSet('group')}").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.targetGroupAttribute.3.storageType").value("separateAttributesTable").store();

    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.targetGroupAttribute.4.name").value("member").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.targetGroupAttribute.4.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.targetGroupAttribute.4.valueType").value("string").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.targetGroupAttribute.4.select").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.targetGroupAttribute.4.insert").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.targetGroupAttribute.4.update").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.targetGroupAttribute.4.delete").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.targetGroupAttribute.4.membershipAttribute").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.targetGroupAttribute.4.multiValued").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.targetGroupAttribute.4.translateFromMemberSyncField").value("memberToId2").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.targetGroupAttribute.4.storageType").value("separateAttributesTable").store();
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.targetGroupAttribute.5.name").value("uuid").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.targetGroupAttribute.5.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.targetGroupAttribute.5.valueType").value("string").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.targetGroupAttribute.5.select").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.targetGroupAttribute.5.insert").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.targetGroupAttribute.5.translateExpressionType").value("grouperProvisioningGroupField").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.targetGroupAttribute.5.translateFromGrouperProvisioningGroupField").value("name").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.targetGroupAttribute.5.storageType").value("groupTableColumn").store();
    
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.targetEntityAttributeCount").value("3").store();

    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.targetEntityAttribute.0.name").value("dn").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.targetEntityAttribute.0.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.targetEntityAttribute.0.valueType").value("string").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.targetEntityAttribute.0.select").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.targetEntityAttribute.0.storageType").value("separateAttributesTable").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.targetEntityAttribute.0.translateToMemberSyncField").value("memberToId2").store();

    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.targetEntityAttribute.1.name").value("employeeID").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.targetEntityAttribute.1.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.targetEntityAttribute.1.valueType").value("string").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.targetEntityAttribute.1.select").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.targetEntityAttribute.1.searchAttribute").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.targetEntityAttribute.1.matchingId").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.targetEntityAttribute.1.translateExpressionType").value("grouperProvisioningEntityField").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.targetEntityAttribute.1.translateFromGrouperProvisioningEntityField").value("subjectId").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.targetEntityAttribute.1.storageType").value("separateAttributesTable").store();

    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.targetEntityAttribute.2.name").value("entity_uuid").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.targetEntityAttribute.2.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.targetEntityAttribute.2.valueType").value("string").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.targetEntityAttribute.2.select").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.targetEntityAttribute.2.storageType").value("entityTableColumn").store();
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.operateOnGrouperGroups").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.operateOnGrouperMemberships").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.operateOnGrouperEntities").value("true").store();
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.insertEntities").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.deleteEntities").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.insertGroups").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.updateGroups").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.deleteGroups").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.deleteGroupsNotInGrouper").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.selectGroups").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.selectEntities").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.selectMemberships").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.insertMemberships").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.deleteMemberships").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.deleteMembershipsIfNotExistInGrouper").value("true").store();
//    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.deleteGroupsDeletedGrouper").value("true").store();
    
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.groupTableName").value("testgrouper_prov_ldap_group").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.groupTableIdColumn").value("uuid").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.groupAttributesTableName").value("testgrouper_pro_ldap_group_attr").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.groupAttributesGroupForeignKeyColumn").value("group_uuid").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.groupAttributesAttributeNameColumn").value("attribute_name").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.groupAttributesAttributeValueColumn").value("attribute_value").store();
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.userTableName").value("testgrouper_prov_ldap_entity").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.userPrimaryKey").value("entity_uuid").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.entityAttributesTableName").value("testgrouper_pro_dap_entity_attr").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.entityAttributesEntityForeignKeyColumn").value("entity_uuid").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.entityAttributesAttributeNameColumn").value("entity_attribute_name").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.entityAttributesAttributeValueColumn").value("entity_attribute_value").store();

    // edu.internet2.middleware.grouper.changeLog.esb.consumer
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("changeLog.consumer.sqlProvTestCLC.class").value(EsbConsumer.class.getName()).store();
    // edu.internet2.middleware.grouper.app.provisioning
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("changeLog.consumer.sqlProvTestCLC.publisher.class").value(ProvisioningConsumer.class.getName()).store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("changeLog.consumer.sqlProvTestCLC.quartzCron").value("0 0 5 * * 2000").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("changeLog.consumer.sqlProvTestCLC.provisionerConfigId").value("pspng_oneprod").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("changeLog.consumer.sqlProvTestCLC.provisionerJobSyncType").value(GrouperProvisioningType.incrementalProvisionChangeLog.name()).store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("changeLog.consumer.sqlProvTestCLC.publisher.debug").value("true").store();

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
    attributeValue.setDoProvision("pspng_oneprod");
    attributeValue.setTargetName("pspng_oneprod");
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
        .addBindVar(uuid).addBindVar("employeeID").addBindVar("test.subject." + i).executeSql();
    }
    
    
    //AttributeAssign attributeAssign = stem.getAttributeDelegate().addAttribute(GrouperProvisioningAttributeNames.retrieveAttributeDefNameMarker()).getAttributeAssign();
    //attributeAssign.getAttributeValueDelegate().assignValueString(GrouperProvisioningAttributeNames.retrieveAttributeDefNameDoProvision())
    
    //lets sync these over
    GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveProvisioner("pspng_oneprod");
    
    GrouperProvisioningOutput grouperProvisioningOutput = grouperProvisioner.provision(GrouperProvisioningType.fullProvisionFull); 
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
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("changeLog.consumer." + JOB_NAME + ".quartzCron").value("0 0 5 * * 2000").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("changeLog.consumer." + JOB_NAME + ".provisionerConfigId").value("pspng_oneprod").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("changeLog.consumer." + JOB_NAME + ".provisionerJobSyncType").value(GrouperProvisioningType.incrementalProvisionChangeLog.name()).store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("changeLog.consumer." + JOB_NAME + ".publisher.debug").value("true").store();
    
    //clear out changelog
    // run the provisioner, it will init
    hib3GrouperLoaderLog = runJobs(true, true);
    
    // add 4
    attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision("pspng_oneprod");
    attributeValue.setTargetName("pspng_oneprod");

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
    hib3GrouperLoaderLog = runJobs(true, true);

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

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer.sqlProvTestCLC.class", "edu.internet2.middleware.grouper.changeLog.esb.consumer.EsbConsumer");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer.sqlProvTestCLC.publisher.class", "edu.internet2.middleware.grouper.app.provisioning.ProvisioningConsumer");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer.sqlProvTestCLC.quartzCron", "0 * * * * 2000");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer.sqlProvTestCLC.provisionerConfigId", "pspng_oneprod");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer.sqlProvTestCLC.provisionerJobSyncType", "incrementalProvisionChangeLog");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer.sqlProvTestCLC.publisher.debug", "true");
    
    // # if provisioning in ui should be enabled
    //# {valueType: "boolean", required: true}
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("provisioningInUi.enable", "true");

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
    attributeValue.setDoProvision("pspng_oneprod");
    attributeValue.setTargetName("pspng_oneprod");
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
        .addBindVar(uuid).addBindVar("employeeID").addBindVar("test.subject." + i).executeSql();
    }
    
    
    //AttributeAssign attributeAssign = stem.getAttributeDelegate().addAttribute(GrouperProvisioningAttributeNames.retrieveAttributeDefNameMarker()).getAttributeAssign();
    //attributeAssign.getAttributeValueDelegate().assignValueString(GrouperProvisioningAttributeNames.retrieveAttributeDefNameDoProvision())
    
    //lets sync these over
    GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveProvisioner("pspng_oneprod");
    
    GrouperProvisioningOutput grouperProvisioningOutput = grouperProvisioner.provision(GrouperProvisioningType.fullProvisionFull); 
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
    
    GcGrouperSync gcGrouperSync = GcGrouperSyncDao.retrieveByProvisionerName(null, "pspng_oneprod");

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
    assertEquals("cn=test:testGroup,OU=Grouper,OU=365Groups,DC=one,DC=upenn,DC=edu", gcGrouperSyncGroup.getGroupToId2());
    
    Hib3GrouperLoaderLog hib3GrouperLoaderLog = null;
    
    //clear out changelog
    // run the provisioner, it will init
    hib3GrouperLoaderLog = runJobs(true, true);
    
    assertLdapGroupNamesInTable(GrouperUtil.toSet("test:testGroup"));

    assertLdapAttributesInTable(GrouperUtil.toSet(
        new MultiKey("test:testGroup", "cn", "test:testGroup"),
        new MultiKey("test:testGroup", "dn", "cn=test:testGroup,OU=Grouper,OU=365Groups,DC=one,DC=upenn,DC=edu"),
        new MultiKey("test:testGroup", "gidNumber", "" + testGroup.getIdIndex()),
        new MultiKey("test:testGroup", "objectClass", "group"),
        new MultiKey("test:testGroup", "member", "dn_test.subject.0"),
        new MultiKey("test:testGroup", "member", "dn_test.subject.1")));
    

    ProvisioningConsumer provisioningConsumer = (ProvisioningConsumer)this.esbConsumer.getEsbPublisherBase();

    grouperProvisioner = provisioningConsumer.getGrouperProvisioner();
    
    assertEquals(0, GrouperUtil.intValue(grouperProvisioner.getDebugMap().get("messageCountForProvisioner"), 0));
    
    assertFalse((Boolean)grouperProvisioner.getDebugMap().get("hasIncrementalDataToProcess"));
    
    // add member
    testGroup.addMember(SubjectTestHelper.SUBJ8, false);

    // run the provisioner
    hib3GrouperLoaderLog = runJobs(true, true);

    provisioningConsumer = (ProvisioningConsumer)this.esbConsumer.getEsbPublisherBase();

    grouperProvisioner = provisioningConsumer.getGrouperProvisioner();
    
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
    hib3GrouperLoaderLog = runJobs(true, true);

    provisioningConsumer = (ProvisioningConsumer)this.esbConsumer.getEsbPublisherBase();

    grouperProvisioner = provisioningConsumer.getGrouperProvisioner();

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
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.class").value("edu.internet2.middleware.grouper.app.sqlProvisioning.SqlProvisioner").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.dbExternalSystemConfigId").value("grouper").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.hasTargetEntityLink").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.hasTargetGroupLink").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.debugLog").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.deleteEntities").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.deleteEntitiesIfNotExistInGrouper").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.deleteGroups").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.deleteGroupsIfNotExistInGrouper").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.deleteMemberships").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.deleteMembershipsIfNotExistInGrouper").value("true").store();
  
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.entityAttributesAttributeNameColumn").value("entity_attribute_name").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.entityAttributesAttributeValueColumn").value("entity_attribute_value").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.entityAttributesEntityForeignKeyColumn").value("entity_uuid").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.entityAttributesLastModifiedColumn").value("last_modified").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.entityAttributesLastModifiedColumnType").value("timestamp").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.entityAttributesTableName").value("testgrouper_pro_dap_entity_attr").store();
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.groupAttributesAttributeNameColumn").value("attribute_name").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.groupAttributesAttributeValueColumn").value("attribute_value").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.groupAttributesGroupForeignKeyColumn").value("group_uuid").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.groupAttributesLastModifiedColumn").value("last_modified").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.groupAttributesLastModifiedColumnType").value("timestamp").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.groupAttributesTableName").value("testgrouper_pro_ldap_group_attr").store();
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.groupTableIdColumn").value("uuid").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.groupTableName").value("testgrouper_prov_group").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.insertEntities").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.insertGroups").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.insertMemberships").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.logAllObjectsVerbose").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.membershipEntityForeignKeyColumn").value("entity_uuid").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.membershipGroupForeignKeyColumn").value("group_uuid").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.membershipTableName").value("testgrouper_prov_mship2").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.membershipPrimaryKey").value("uuid").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.numberOfEntityAttributes").value("5").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.numberOfGroupAttributes").value("4").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.numberOfMembershipAttributes").value("3").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.operateOnGrouperEntities").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.operateOnGrouperGroups").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.operateOnGrouperMemberships").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.provisioningType").value("membershipObjects").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.selectAllEntities").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.selectEntities").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.selectGroups").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.selectMemberships").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.showAdvanced").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.showProvisioningDiagnostics").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.subjectSourcesToProvision").value("jdbc").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.tableStructures").value("defaultTableStructure").store();
    
    
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.0.insert").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.0.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.0.name").value("uuid").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.0.select").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.0.translateGrouperToMemberSyncField").value("memberFromId2").store();
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.0.storageType").value("entityTableColumn").store();
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.0.translateExpression").value("${edu.internet2.middleware.grouper.internal.util.GrouperUuid.getUuid()}").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.0.translateExpressionType").value("translationScript").store();
    
    
    
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.1.insert").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.1.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.1.matchingId").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.1.name").value("name").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.1.searchAttribute").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.1.select").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.1.storageType").value("entityTableColumn").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.1.translateExpressionType").value("grouperProvisioningEntityField").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.1.translateFromGrouperProvisioningEntityField").value("name").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.1.update").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.2.insert").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.2.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.2.name").value("subject_id_or_identifier").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.2.select").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.2.storageType").value("entityTableColumn").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.2.translateExpressionType").value("grouperProvisioningEntityField").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.2.translateFromGrouperProvisioningEntityField").value("subjectId").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.2.update").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.3.insert").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.3.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.3.name").value("email").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.3.select").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.3.storageType").value("entityTableColumn").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.3.translateExpressionType").value("grouperProvisioningEntityField").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.3.translateFromGrouperProvisioningEntityField").value("email").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.3.update").value("true").store();
    
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.4.insert").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.4.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.4.name").value("description").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.4.select").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.4.storageType").value("separateAttributesTable").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.4.translateExpressionType").value("grouperProvisioningEntityField").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.4.translateFromGrouperProvisioningEntityField").value("attribute__description").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetEntityAttribute.4.update").value("true").store();
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.0.insert").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.0.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.0.name").value("uuid").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.0.select").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.0.storageType").value("groupTableColumn").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.0.translateGrouperToGroupSyncField").value("groupFromId2").store();
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.0.translateExpression").value("${edu.internet2.middleware.grouper.internal.util.GrouperUuid.getUuid()}").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.0.translateExpressionType").value("translationScript").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.1.insert").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.1.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.1.matchingId").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.1.name").value("posix_id").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.1.select").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.1.storageType").value("groupTableColumn").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.1.translateExpressionType").value("grouperProvisioningGroupField").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.1.translateFromGrouperProvisioningGroupField").value("idIndex").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.1.update").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.1.valueType").value("long").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.2.insert").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.2.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.2.name").value("name").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.2.searchAttribute").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.2.select").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.2.storageType").value("groupTableColumn").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.2.translateExpressionType").value("grouperProvisioningGroupField").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.2.translateFromGrouperProvisioningGroupField").value("name").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.2.update").value("true").store();
  
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.3.insert").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.3.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.3.name").value("description").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.3.select").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.3.storageType").value("separateAttributesTable").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.3.translateExpressionType").value("grouperProvisioningGroupField").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.3.translateFromGrouperProvisioningGroupField").value("attribute__description").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.3.update").value("true").store();
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetMembershipAttribute.0.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetMembershipAttribute.0.name").value("uuid").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetMembershipAttribute.0.insert").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetMembershipAttribute.0.select").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetMembershipAttribute.0.translateExpression").value("${edu.internet2.middleware.grouper.internal.util.GrouperUuid.getUuid()}").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetMembershipAttribute.0.translateExpressionType").value("translationScript").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetMembershipAttribute.1.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetMembershipAttribute.1.name").value("group_uuid").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetMembershipAttribute.1.translateExpressionType").value("groupSyncField").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetMembershipAttribute.1.translateFromGroupSyncField").value("groupFromId2").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetMembershipAttribute.1.insert").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetMembershipAttribute.1.select").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetMembershipAttribute.1.update").value("true").store();
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetMembershipAttribute.10.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetMembershipAttribute.11.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetMembershipAttribute.12.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetMembershipAttribute.13.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetMembershipAttribute.14.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetMembershipAttribute.15.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetMembershipAttribute.16.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetMembershipAttribute.17.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetMembershipAttribute.18.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetMembershipAttribute.19.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetMembershipAttribute.2.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetMembershipAttribute.2.name").value("entity_uuid").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetMembershipAttribute.2.translateExpressionType").value("memberSyncField").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetMembershipAttribute.2.translateFromMemberSyncField").value("memberFromId2").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetMembershipAttribute.2.insert").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetMembershipAttribute.2.select").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetMembershipAttribute.2.update").value("true").store();
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetMembershipAttribute.3.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetMembershipAttribute.4.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetMembershipAttribute.5.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetMembershipAttribute.6.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetMembershipAttribute.7.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetMembershipAttribute.8.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetMembershipAttribute.9.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.updateEntities").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.updateGroups").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.userPrimaryKey").value("uuid").store();
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.useSeparateTableForGroupAttributes").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.useSeparateTableForEntityAttributes").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.userTableName").value("testgrouper_prov_entity").store();

    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.showFailsafe").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.failsafeUse").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.failsafeSendEmail").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.failsafeMinGroupSize").value("-1").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.failsafeMaxPercentRemove").value("-1").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.failsafeMinManagedGroups").value("-1").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.failsafeMaxOverallPercentGroupsRemove").value("-1").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.failsafeMaxOverallPercentMembershipsRemove").value("-1").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.failsafeMinOverallNumberOfMembers").value("-1").store();

    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("otherJob.sqlProvisionerFull.class").value("edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningFullSyncJob").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("otherJob.sqlProvisionerFull.quartzCron").value("0 0 4 * * ?").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("otherJob.sqlProvisionerFull.provisionerConfigId").value("mySqlProvisioner1").store();
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("changeLog.consumer.sqlProvisionerIncremental.class").value("edu.internet2.middleware.grouper.changeLog.esb.consumer.EsbConsumer").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("changeLog.consumer.sqlProvisionerIncremental.quartzCron").value("0 * * * * ?").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("changeLog.consumer.sqlProvisionerIncremental.provisionerConfigId").value("mySqlProvisioner1").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("changeLog.consumer.sqlProvisionerIncremental.publisher.class").value("edu.internet2.middleware.grouper.app.provisioning.ProvisioningConsumer").store();

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
    attributeValue.setDoProvision("mySqlProvisioner1");
    attributeValue.setTargetName("mySqlProvisioner1");
    attributeValue.setStemScopeString("sub");
  
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);

  }
  
  private List<Group> failsafeGroups = new ArrayList<Group>();
  
  public void testSimpleGroupMembershipProvisioningFullWithAttributesTableFailsafe() {
    
    setupFailsafeJob();
    
    //AttributeAssign attributeAssign = stem.getAttributeDelegate().addAttribute(GrouperProvisioningAttributeNames.retrieveAttributeDefNameMarker()).getAttributeAssign();
    //attributeAssign.getAttributeValueDelegate().assignValueString(GrouperProvisioningAttributeNames.retrieveAttributeDefNameDoProvision())
    
    //lets sync these over
    String jobName = "OTHER_JOB_sqlProvisionerFull";
    GrouperLoader.runOnceByJobName(this.grouperSession, jobName);
    
    assertFalse(GrouperFailsafe.isFailsafeIssue(jobName));
    
    List<Object[]> groups = new GcDbAccess().sql("select uuid, posix_id, name from testgrouper_prov_group order by name").selectList(Object[].class);
    assertEquals(10, groups.size());
    
    List<Object[]> entities = new GcDbAccess().sql("select uuid, name, subject_id_or_identifier, description from testgrouper_prov_entity order by name").selectList(Object[].class);
    assertEquals(10, entities.size());
    
    List<Object[]> memberships = new GcDbAccess().sql("select group_uuid, entity_uuid from testgrouper_prov_mship2").selectList(Object[].class);
    assertEquals(100, memberships.size());
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.failsafeMinOverallNumberOfMembers").value("85").store();
    
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
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.class").value("edu.internet2.middleware.grouper.app.sqlProvisioning.SqlProvisioner").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.dbExternalSystemConfigId").value("grouper").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.debugLog").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.deleteGroups").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.deleteGroupsIfNotExistInGrouper").value("true").store();
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.groupsRequireMembers").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.groupTableIdColumn").value("uuid").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.groupTableName").value("testgrouper_prov_group").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.logAllObjectsVerbose").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.numberOfGroupAttributes").value("1").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.operateOnGrouperGroups").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.selectGroups").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.insertGroups").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.showAdvanced").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.showProvisioningDiagnostics").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.subjectSourcesToProvision").value("jdbc").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.tableStructures").value("defaultTableStructure").store();
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.0.insert").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.0.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.0.name").value("uuid").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.0.select").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.0.storageType").value("groupTableColumn").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.0.translateExpressionType").value("grouperProvisioningGroupField").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.0.translateFromGrouperProvisioningGroupField").value("name").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.0.matchingId").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.0.searchAttribute").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.targetGroupAttribute.0.storageType").value("groupTableColumn").store();

    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.updateGroups").value("true").store();

    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("otherJob.sqlProvisionerFull.class").value("edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningFullSyncJob").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("otherJob.sqlProvisionerFull.quartzCron").value("0 0 4 * * ?").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("otherJob.sqlProvisionerFull.provisionerConfigId").value("mySqlProvisioner1").store();

    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("changeLog.consumer.sqlProvisionerIncremental.class").value("edu.internet2.middleware.grouper.changeLog.esb.consumer.EsbConsumer").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("changeLog.consumer.sqlProvisionerIncremental.quartzCron").value("0 * * * * ?").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("changeLog.consumer.sqlProvisionerIncremental.provisionerConfigId").value("mySqlProvisioner1").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("changeLog.consumer.sqlProvisionerIncremental.publisher.class").value("edu.internet2.middleware.grouper.app.provisioning.ProvisioningConsumer").store();

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
    attributeValue.setDoProvision("mySqlProvisioner1");
    attributeValue.setTargetName("mySqlProvisioner1");
    attributeValue.setStemScopeString("sub");
  
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);
    
    //lets sync these over
    String jobName = "OTHER_JOB_sqlProvisionerFull";
    GrouperLoader.runOnceByJobName(this.grouperSession, jobName);
    
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
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.failsafeMinGroupSize").value("8").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.failsafeMaxPercentRemove").value("20").store();
    
    //lets sync these over
    String jobName = "OTHER_JOB_sqlProvisionerFull";
    GrouperLoader.runOnceByJobName(this.grouperSession, jobName);
    
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

    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.failsafeMinGroupSize").value("15").store();

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
    String jobName = "OTHER_JOB_sqlProvisionerFull";
    GrouperLoader.runOnceByJobName(this.grouperSession, jobName);
    
    assertFalse(GrouperFailsafe.isFailsafeIssue(jobName));
    
    List<Object[]> groups = new GcDbAccess().sql("select uuid, posix_id, name from testgrouper_prov_group order by name").selectList(Object[].class);
    assertEquals(10, groups.size());
    
    List<Object[]> entities = new GcDbAccess().sql("select uuid, name, subject_id_or_identifier, description from testgrouper_prov_entity order by name").selectList(Object[].class);
    assertEquals(10, entities.size());
    
    List<Object[]> memberships = new GcDbAccess().sql("select group_uuid, entity_uuid from testgrouper_prov_mship2").selectList(Object[].class);
    assertEquals(100, memberships.size());
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.failsafeMinOverallNumberOfMembers").value("85").store();
    
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
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.failsafeMinOverallNumberOfMembers").value("45").store();
  
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
    String jobName = "OTHER_JOB_sqlProvisionerFull";
    GrouperLoader.runOnceByJobName(this.grouperSession, jobName);
    
    assertFalse(GrouperFailsafe.isFailsafeIssue(jobName));
    
    List<Object[]> groups = new GcDbAccess().sql("select uuid, posix_id, name from testgrouper_prov_group order by name").selectList(Object[].class);
    assertEquals(10, groups.size());
    
    List<Object[]> entities = new GcDbAccess().sql("select uuid, name, subject_id_or_identifier, description from testgrouper_prov_entity order by name").selectList(Object[].class);
    assertEquals(10, entities.size());
    
    List<Object[]> memberships = new GcDbAccess().sql("select group_uuid, entity_uuid from testgrouper_prov_mship2").selectList(Object[].class);
    assertEquals(100, memberships.size());
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.failsafeMaxOverallPercentGroupsRemove").value("25").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.failsafeMinManagedGroups").value("8").store();
    
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
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.failsafeMinManagedGroups").value("12").store();
  
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
    String jobName = "OTHER_JOB_sqlProvisionerFull";
    GrouperLoader.runOnceByJobName(this.grouperSession, jobName);
    
    assertFalse(GrouperFailsafe.isFailsafeIssue(jobName));
    
    List<Object[]> groups = new GcDbAccess().sql("select uuid, posix_id, name from testgrouper_prov_group order by name").selectList(Object[].class);
    assertEquals(10, groups.size());
    
    List<Object[]> entities = new GcDbAccess().sql("select uuid, name, subject_id_or_identifier, description from testgrouper_prov_entity order by name").selectList(Object[].class);
    assertEquals(10, entities.size());
    
    List<Object[]> memberships = new GcDbAccess().sql("select group_uuid, entity_uuid from testgrouper_prov_mship2").selectList(Object[].class);
    assertEquals(100, memberships.size());
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.failsafeMaxOverallPercentMembershipsRemove").value("25").store();
    
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
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.mySqlProvisioner1.failsafeMaxOverallPercentMembershipsRemove").value("60").store();
  
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

    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.targetGroupAttribute.2.translateExpressionType").value("translationScript").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.targetGroupAttribute.2.translateExpression")
      .value("${grouperProvisioningGroup.name == 'test:testGroup' ? null : grouperProvisioningGroup.idIndex}").store();

    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.targetEntityAttribute.1.translateExpressionType").value("translationScript").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.targetEntityAttribute.1.translateExpression")
    .value("${grouperProvisioningEntity.subjectId == 'test.subject.4' ? null : grouperProvisioningEntity.subjectId}").store();

    //lets sync these over
    GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveProvisioner("pspng_oneprod");
    
    GrouperProvisioningOutput grouperProvisioningOutput = grouperProvisioner.provision(GrouperProvisioningType.fullProvisionFull); 

    runJobs(true, true);

    
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
    attributeValue.setDoProvision("pspng_oneprod");
    attributeValue.setTargetName("pspng_oneprod");
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
        .addBindVar(uuid).addBindVar("employeeID").addBindVar("test.subject." + i).executeSql();
    }
    
    
    //AttributeAssign attributeAssign = stem.getAttributeDelegate().addAttribute(GrouperProvisioningAttributeNames.retrieveAttributeDefNameMarker()).getAttributeAssign();
    //attributeAssign.getAttributeValueDelegate().assignValueString(GrouperProvisioningAttributeNames.retrieveAttributeDefNameDoProvision())
    
    //lets sync these over
    grouperProvisioner = GrouperProvisioner.retrieveProvisioner("pspng_oneprod");
    
    grouperProvisioningOutput = grouperProvisioner.provision(GrouperProvisioningType.fullProvisionFull); 
    
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
    GcGrouperSync gcGrouperSync = GcGrouperSyncDao.retrieveByProvisionerName(null, "pspng_oneprod");
    assertEquals(2, gcGrouperSync.getGroupCount().intValue());
    
    // 2 valid, 1 invalid, 2 in invalid group
    assertEquals(5, gcGrouperSync.getUserCount().intValue());
    assertEquals(2+5+5, gcGrouperSync.getRecordsCount().intValue());
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
      assertEquals(GcGrouperSyncErrorCode.REQ, gcGrouperSyncGroup.getErrorCode());
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
      assertEquals(GcGrouperSyncErrorCode.REQ, gcGrouperSyncMember4.getErrorCode());
    }  
    
    {
      GcGrouperSyncMembership gcGrouperSyncMembership0 = gcGrouperSync.getGcGrouperSyncMembershipDao().membershipRetrieveByGroupIdAndMemberId(testGroup.getId(), member0.getId());
      assertFalse("T".equals(gcGrouperSyncMembership0.getInTargetDb()));
      assertFalse("T".equals(gcGrouperSyncMembership0.getInTargetInsertOrExistsDb()));
      assertEquals(GcGrouperSyncErrorCode.REQ, gcGrouperSyncMembership0.getErrorCode());
    }  
    {
      GcGrouperSyncMembership gcGrouperSyncMembership1 = gcGrouperSync.getGcGrouperSyncMembershipDao().membershipRetrieveByGroupIdAndMemberId(testGroup.getId(), member1.getId());
      assertFalse("T".equals(gcGrouperSyncMembership1.getInTargetDb()));
      assertFalse("T".equals(gcGrouperSyncMembership1.getInTargetInsertOrExistsDb()));
      assertEquals(GcGrouperSyncErrorCode.REQ, gcGrouperSyncMembership1.getErrorCode());
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
      assertEquals(GcGrouperSyncErrorCode.REQ, gcGrouperSyncMembership4.getErrorCode());
    }  

    // this should not retry
    Hib3GrouperLoaderLog hib3GrouperLoaderLog = runJobs(true, true);
    assertEquals("SUCCESS", hib3GrouperLoaderLog.getStatus());
    
    grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    assertEquals(0, GrouperUtil.intValue(grouperProvisioner.getDebugMap().get("addErrorsToQueue"), 0));
    assertTrue(GrouperUtil.intValue(grouperProvisioner.getDebugMap().get("skippedEventsDueToFullSync"), 0) > 0);
    assertEquals(0, GrouperUtil.intValue(grouperProvisioner.getDebugMap().get("messageCountForProvisioner"), 0));
    
    
    hib3GrouperLoaderLog = runJobs(true, true);
    assertEquals("SUCCESS", hib3GrouperLoaderLog.getStatus());
    
    grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    assertEquals(0, GrouperUtil.intValue(grouperProvisioner.getDebugMap().get("addErrorsToQueue"), 0));
    assertEquals(0, GrouperUtil.intValue(grouperProvisioner.getDebugMap().get("skippedEventsDueToFullSync"), 0));
    assertEquals(0, GrouperUtil.intValue(grouperProvisioner.getDebugMap().get("messageCountForProvisioner"), 0));
    
  
  }

}
