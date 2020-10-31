package edu.internet2.middleware.grouper.app.sqlProvisioning;

import java.sql.Types;
import java.util.ArrayList;
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
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
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
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSync;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncDao;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncGroup;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncJob;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncJobState;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncMember;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncMembership;
import edu.internet2.middleware.grouperClient.util.GrouperClientConfig;
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

    TestRunner.run(new SqlProvisionerTest("testSimpleGroupLdapPa"));
    
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
  
      new GcDbAccess().sql("delete from testgrouper_prov_ldap_group_attr").executeSql();
      new GcDbAccess().sql("delete from testgrouper_prov_ldap_group").executeSql();
      new GcDbAccess().sql("delete from testgrouper_prov_ldap_entity_attr").executeSql();
      new GcDbAccess().sql("delete from testgrouper_prov_ldap_entity").executeSql();
      new GcDbAccess().sql("delete from testgrouper_prov_group").executeSql();
      new GcDbAccess().sql("delete from testgrouper_prov_mship0").executeSql();
      new GcDbAccess().sql("delete from testgrouper_prov_mship1").executeSql();
      
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
    
    dropTableSyncTable("testgrouper_prov_ldap_group_attr");
    dropTableSyncTable("testgrouper_prov_ldap_group");
    dropTableSyncTable("testgrouper_prov_ldap_entity_attr");
    dropTableSyncTable("testgrouper_prov_ldap_entity");
    dropTableSyncTable("testgrouper_prov_group");
    dropTableSyncTable("testgrouper_prov_mship0");
    dropTableSyncTable("testgrouper_prov_mship1");
    
  }
  
  
  /**
   * just do a simple full sync of groups and memberships
   */
  public void testSimpleGroupMembershipProvisioningFull_1() {
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.class", SqlProvisioner.class.getName());
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.dbExternalSystemConfigId", "grouper");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.subjectSourcesToProvision", "jdbc");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.logAllObjectsVerbose", "true");

//    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.membershipUserColumn", "subject_id");
//    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.membershipUserValueFormat", "${targetEntity.id}");
//    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.membershipGroupColumn", "group_name");
//    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.membershipGroupValueFormat", "${targetGroup.id}");
//    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.syncMemberToId3AttributeValueFormat", "${targetEntity.id}");
//    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.syncGroupToId3AttributeValueFormat", "${targetGroup.name}");
//    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.membershipCreationNumberOfAttributes", "2");
//    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.membershipCreationColumnTemplate_attr_0", "group_name");
//    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.membershipCreationColumnTemplate_val_0", "${targetGroup.name}");
//    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.membershipCreationColumnTemplate_attr_1", "subject_id");
//    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.membershipCreationColumnTemplate_val_1", "${targetEntity.id}");
    
    
    //#translate from group auto translated to the common format
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.grouperToTargetTranslation.0.script", 
        "${grouperTargetMembership.assignAttributeValue('group_name', grouperProvisioningMembership.getProvisioningGroup().getName())}");
    // # could be group, membership, or entity
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.grouperToTargetTranslation.0.for", "membership");
    //#translate from group auto translated to the common format
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.grouperToTargetTranslation.1.script", 
        "${grouperTargetMembership.assignAttributeValue('subject_id', grouperProvisioningMembership.getProvisioningEntity().retrieveAttributeValueString('subjectId'))}");
    //# could be group, membership, or entity
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.grouperToTargetTranslation.1.for", "membership");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.membershipMatchingIdExpression", 
        "new('edu.internet2.middleware.grouperClient.collections.MultiKey', targetMembership.retrieveAttributeValueString('group_name'), "
        + "targetMembership.retrieveAttributeValueString('subject_id'))");

    
    
//    
//    
//    
//    //#translate from group auto translated to the common format
//    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.commonToTargetTranslation.2.script", "${targetCommonGroup.setId(targetProvisioningGroup.getId())}");
//    //# could be group, membership, or entity
//    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.commonToTargetTranslation.2.for", "group");
//    //#translate from group auto translated to the common format
//    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.commonToTargetTranslation.3.script", "${targetCommonEntity.setId(targetProvisioningEntity.getId())}");
//    //# could be group, membership, or entity
//    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.commonToTargetTranslation.3.for", "entity");
//    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.membershipTableName", "testgrouper_prov_mship0");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.membershipAttributeNames", "group_name, subject_id");
    
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
    attributeValue.setDoProvision(true);
    attributeValue.setTargetName("sqlProvTest");
    attributeValue.setStemScopeString("sub");

    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);

    //AttributeAssign attributeAssign = stem.getAttributeDelegate().addAttribute(GrouperProvisioningAttributeNames.retrieveAttributeDefNameMarker()).getAttributeAssign();
    //attributeAssign.getAttributeValueDelegate().assignValueString(GrouperProvisioningAttributeNames.retrieveAttributeDefNameDoProvision())
    
    //lets sync these over
    GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveProvisioner("sqlProvTest");
    
    GrouperProvisioningOutput grouperProvisioningOutput = grouperProvisioner.provision(GrouperProvisioningType.fullProvisionFull); 
  
    List<Object[]> dataInTable = new GcDbAccess().sql("select group_name, subject_id from testgrouper_prov_mship0").selectList(Object[].class);
    
    Set<MultiKey> groupIdSubjectIdsInTable = new HashSet<MultiKey>();
    
    for (Object[] groupIdSubjectId: dataInTable) {
      groupIdSubjectIdsInTable.add(new MultiKey(groupIdSubjectId[0], groupIdSubjectId[1]));
    }
    
    assertEquals(2, groupIdSubjectIdsInTable.size());
    assertTrue(groupIdSubjectIdsInTable.contains(new MultiKey("test:testGroup", "test.subject.0")));
    assertTrue(groupIdSubjectIdsInTable.contains(new MultiKey("test:testGroup", "test.subject.1")));
  }

  /**
   * @param tableName
   */
  public void dropTableSyncTable(final String tableName) {
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
    //we need to delete the test table if it is there, and create a new one
    //drop field id col, first drop foreign keys
    GrouperDdlUtils.changeDatabase(GrouperTestDdl.V1.getObjectName(), new DdlUtilsChangeDatabase() {
  
      public void changeDatabase(DdlVersionBean ddlVersionBean) {
        
        Database database = ddlVersionBean.getDatabase();
  
        createTableLdapGroup(ddlVersionBean, database);
        createTableLdapGroupAttr(ddlVersionBean, database);
        createTableLdapEntity(ddlVersionBean, database);
        createTableLdapEntityAttr(ddlVersionBean, database);

        createTableGroup(ddlVersionBean, database);
        
        createTableMship0(ddlVersionBean, database);
        
        createTableMship1(ddlVersionBean, database);
      }
      
    });
  }

  /**
   * @param ddlVersionBean
   * @param database
   */
  public void createTableGroup(DdlVersionBean ddlVersionBean, Database database) {
  
    String tableName = "testgrouper_prov_group";
    
    Table loaderTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, tableName);
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "uuid", Types.VARCHAR, "40", true, true);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "posix_id", Types.BIGINT, "10", false, true);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "name", Types.VARCHAR, "1024", false, true);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "display_name", Types.VARCHAR, "1024", false, false);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "description", Types.VARCHAR, "1024", false, false);
    
  }
  
  /**
   * @param ddlVersionBean
   * @param database
   */
  public void createTableLdapGroup(DdlVersionBean ddlVersionBean, Database database) {
  
    String tableName = "testgrouper_prov_ldap_group";
    
    Table loaderTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, tableName);
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "uuid", Types.VARCHAR, "40", true, true);
    
  }
  
  /**
   * @param ddlVersionBean
   * @param database
   */
  public void createTableLdapEntity(DdlVersionBean ddlVersionBean, Database database) {
  
    String tableName = "testgrouper_prov_ldap_entity";
    
    Table loaderTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, tableName);
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "entity_uuid", Types.VARCHAR, "40", true, true);
    
  }
  
  /**
   * @param ddlVersionBean
   * @param database
   */
  public void createTableLdapGroupAttr(DdlVersionBean ddlVersionBean, Database database) {
  
    String tableName = "testgrouper_prov_ldap_group_attr";
    
    Table loaderTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, tableName);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "group_uuid", Types.VARCHAR, "40", true, true);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "attribute_name", Types.VARCHAR, "200", true, true);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "attribute_value", Types.VARCHAR, "200", true, false);

  }

  /**
   * @param ddlVersionBean
   * @param database
   */
  public void createTableMship1(DdlVersionBean ddlVersionBean, Database database) {
  
    String tableName = "testgrouper_prov_mship1";
    
    Table loaderTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, tableName);
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "uuid", Types.VARCHAR, "40", true, true);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "group_uuid", Types.VARCHAR, "40", false, true);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "subject_id", Types.VARCHAR, "1024", false, true);
    
  }

  /**
   * @param ddlVersionBean
   * @param database
   */
  public void createTableMship0(DdlVersionBean ddlVersionBean, Database database) {
  
    String tableName = "testgrouper_prov_mship0";
    
    Table loaderTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, tableName);
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "group_name", Types.VARCHAR, "180", true, true);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "subject_id", Types.VARCHAR, "70", true, true);
    
  }

  /**
     * just do a simple full sync of groups and memberships
     */
    public void testSimpleGroupLdap() {
      
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.class", SqlProvisioner.class.getName());
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.dbExternalSystemConfigId", "grouper");
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.subjectSourcesToProvision", "jdbc");
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.logAllObjectsVerbose", "true");
  
      
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.grouperToTargetTranslation.0.script", 
          "${grouperTargetGroup.assignAttributeValue(('groupName', grouperProvisioningGroup.getName())}");
      // # could be group, membership, or entity
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.grouperToTargetTranslation.0.for", "group");
      //#translate from group auto translated to the common format
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.grouperToTargetTranslation.1.script", 
          "${grouperTargetGroup.addAttributeValueForMembership('subjectId', "
          + "grouperProvisioningEntity.retrieveAttributeValueString('subjectId'))}");
      // # could be group, membership, or entity
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.grouperToTargetTranslation.1.for", "membership");

      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.grouperToTargetTranslation.2.script", 
          "${grouperTargetMembership.setRemoveFromList(true)}");
      // # could be group, membership, or entity
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.grouperToTargetTranslation.2.for", "membership");

      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.groupMatchingIdExpression", 
          "${targetGroup.retrieveAttributeValueString('groupName')}");

//      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.membershipMatchingIdExpression", 
//          "${new MultiKey(targetMembership.retrieveAttributeValueString('groupName'), targetMembership.retrieveAttributeValueString('subjectId'))}");
//
//      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.entityMatchingIdExpression", 
//          "${targetEntity.retrieveAttributeValueString('subjectId')}");
      
      //      //#translate from group auto translated to the common format
//      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.grouperToTargetTranslation.1.script", 
//          "${grouperTargetEntity.setId(grouperProvisioningEntity.retrieveAttributeValueString('subjectId'))}");
//      //# could be group, membership, or entity
//      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.grouperToTargetTranslation.1.for", "entity");
//      //#translate from group auto translated to the common format
//      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetToCommonTranslation.0.script", 
//          "${grouperTargetGroup.setGroupId(targetProvisioningGroup.retrieveAttributeValueString('groupName')); "
//          + "grouperTargetGroup.addMembershipsWithEntityIds(targetProvisioningGroup.retrieveAttributeValue('members'));}");
//      //# could be group, membership, or entity
//      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetToCommonTranslation.0.for", "group");
//      //#translate from group auto translated to the common format
//      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.commonToTargetTranslation.0.script", 
//          "${commonProvisionToTargetGroup.assignAttributeValue('groupName', commonGroup.getId());}");
//      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.commonToTargetTranslation.0.for", "group");
//      
//      //#translate from group auto translated to the common format
//      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.commonToTargetTranslation.1.script", 
//          "${commonMembership..manageAttributeValue(action, 'subjectId', commonMembership.getProvisioningEntityId());"
//          + "commonProvisionToTargetMembership.setRemoveFromList(true);}");

      //# could be group, membership, or entity
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.commonToTargetTranslation.1.for", "membership");
      
      
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.groupTableName", "testgrouper_prov_ldap_group");
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.groupAttributeNames", "uuid");
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.groupAttributeTableName", "testgrouper_prov_ldap_group_attr");
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.groupTableIdColumn", "uuid");
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.groupAttributeTableAttributeNameIsGroupMatchingId", "groupName");
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.groupAttributeTableForeignKeyToGroup", "group_uuid");
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.groupAttributeTableAttributeNameColumn", "attribute_name");
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.groupAttributeTableAttributeValueColumn", "attribute_value");
      
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
      attributeValue.setDoProvision(true);
      attributeValue.setTargetName("sqlProvTest");
      attributeValue.setStemScopeString("sub");
  
      GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);
  
      //AttributeAssign attributeAssign = stem.getAttributeDelegate().addAttribute(GrouperProvisioningAttributeNames.retrieveAttributeDefNameMarker()).getAttributeAssign();
      //attributeAssign.getAttributeValueDelegate().assignValueString(GrouperProvisioningAttributeNames.retrieveAttributeDefNameDoProvision())
      
      //lets sync these over
      GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveProvisioner("sqlProvTest");
      
      GrouperProvisioningOutput grouperProvisioningOutput = grouperProvisioner.provision(GrouperProvisioningType.fullProvisionFull); 

      String sql = "select uuid from testgrouper_prov_ldap_group";
      
      List<Object[]> dataInTable = new GcDbAccess().sql(sql).selectList(Object[].class);
      
      Set<MultiKey> groupNamesInTable = new HashSet<MultiKey>();
      
      for (Object[] row: dataInTable) {
        groupNamesInTable.add(new MultiKey(row));
      }
      
      assertEquals(1, groupNamesInTable.size());
      assertTrue(groupNamesInTable.contains(new MultiKey(new Object[]{"test:testGroup"})));

      sql = "select group_uuid, attribute_name, attribute_value from testgrouper_prov_ldap_group_attr";
      
      dataInTable = new GcDbAccess().sql(sql).selectList(Object[].class);
      
      Set<MultiKey> attributesInTable = new HashSet<MultiKey>();
      
      for (Object[] row: dataInTable) {
        attributesInTable.add(new MultiKey(row));
      }
      
      assertEquals(3, attributesInTable.size());
      assertTrue(attributesInTable.contains(new MultiKey("test:testGroup", "groupName", "test:testGroup")));
      assertTrue(attributesInTable.contains(new MultiKey("test:testGroup", "subjectId", "test.subject.0")));
      assertTrue(attributesInTable.contains(new MultiKey("test:testGroup", "subjectId", "test.subject.1")));
    }

  /**
       * just do a simple full sync of groups and memberships
       */
      public void testSimpleGroupLdapInsertUpdateDeleteFullSync() {
        
        GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.class", SqlProvisioner.class.getName());
        GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.dbExternalSystemConfigId", "grouper");
        GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.subjectSourcesToProvision", "jdbc");
        GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.logAllObjectsVerbose", "true");
    
        
        GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.grouperToTargetTranslation.0.script", 
            "${grouperTargetGroup.assignAttributeValue('groupName', grouperProvisioningGroup.getName())}");
        // # could be group, membership, or entity
        GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.grouperToTargetTranslation.0.for", "group");
        //#translate from group auto translated to the common format
        GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.grouperToTargetTranslation.1.script", 
            "${grouperTargetGroup.addAttributeValueForMembership('subjectId', "
            + "grouperProvisioningEntity.retrieveAttributeValueString('subjectId'))}");
        // # could be group, membership, or entity
        GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.grouperToTargetTranslation.1.for", "membership");
  
        GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.grouperToTargetTranslation.2.script", 
            "${grouperTargetMembership.setRemoveFromList(true)}");
        // # could be group, membership, or entity
        GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.grouperToTargetTranslation.2.for", "membership");
  
        //# could be group, membership, or entity
        GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.commonToTargetTranslation.1.for", "membership");
        
        GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.groupMatchingIdExpression", 
            "${targetGroup.retrieveAttributeValueString('groupName')}");
  
        
        GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.groupTableName", "testgrouper_prov_ldap_group");
        GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.groupAttributeNames", "uuid");
        GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.groupAttributeTableName", "testgrouper_prov_ldap_group_attr");
        GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.groupTableIdColumn", "uuid");
        GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.groupAttributeTableAttributeNameIsGroupMatchingId", "groupName");
        GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.groupAttributeTableForeignKeyToGroup", "group_uuid");
        GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.groupAttributeTableAttributeNameColumn", "attribute_name");
        GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.groupAttributeTableAttributeValueColumn", "attribute_value");
        
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
        attributeValue.setDoProvision(true);
        attributeValue.setTargetName("sqlProvTest");
        attributeValue.setStemScopeString("sub");
    
        GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);
        GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem3);
    
        //lets sync these over
        GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveProvisioner("sqlProvTest");
        
        GrouperProvisioningOutput grouperProvisioningOutput = grouperProvisioner.provision(GrouperProvisioningType.fullProvisionFull); 
  
        String sql = "select uuid from testgrouper_prov_ldap_group";
        
        List<Object[]> dataInTable = new GcDbAccess().sql(sql).selectList(Object[].class);
        
        Set<MultiKey> groupNamesInTable = new HashSet<MultiKey>();
        
        for (Object[] row: dataInTable) {
          groupNamesInTable.add(new MultiKey(row));
        }
        
        assertEquals(2, groupNamesInTable.size());
        assertTrue(groupNamesInTable.contains(new MultiKey(new Object[]{"test:testGroup"})));
        assertTrue(groupNamesInTable.contains(new MultiKey(new Object[]{"test3:testGroup3"})));
  
        sql = "select group_uuid, attribute_name, attribute_value from testgrouper_prov_ldap_group_attr";
        
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
  
        sql = "select uuid from testgrouper_prov_ldap_group";
        
        dataInTable = new GcDbAccess().sql(sql).selectList(Object[].class);
        
        groupNamesInTable = new HashSet<MultiKey>();
        
        for (Object[] row: dataInTable) {
          groupNamesInTable.add(new MultiKey(row));
        }
        
        assertEquals(2, groupNamesInTable.size());
        assertTrue(groupNamesInTable.contains(new MultiKey(new Object[]{"test:testGroup"})));
        assertTrue(groupNamesInTable.contains(new MultiKey(new Object[]{"test4:testGroup4"})));
  
        sql = "select group_uuid, attribute_name, attribute_value from testgrouper_prov_ldap_group_attr";
        
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
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.class", SqlProvisioner.class.getName());
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.dbExternalSystemConfigId", "grouper");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.subjectSourcesToProvision", "jdbc");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.logAllObjectsVerbose", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.debugLog", "true");
  
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.grouperToTargetTranslation.0.script", 
        "${grouperTargetGroup.assignAttributeValue('groupName', grouperProvisioningGroup.getName())}");
    // # could be group, membership, or entity
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.grouperToTargetTranslation.0.for", "group");
    
    //#translate from group auto translated to the common format
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.grouperToTargetTranslation.1.script", 
        "${grouperTargetGroup.addAttributeValueForMembership('subjectId', "
        + "grouperProvisioningEntity.retrieveAttributeValueString('subjectId'))}");
    // # could be group, membership, or entity
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.grouperToTargetTranslation.1.for", "membership");
  
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.grouperToTargetTranslation.2.script", 
        "${grouperTargetMembership.setRemoveFromList(true)}");
    // # could be group, membership, or entity
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.grouperToTargetTranslation.2.for", "membership");
  
    //# could be group, membership, or entity
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.commonToTargetTranslation.1.for", "membership");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.groupMatchingIdExpression", 
        "${targetGroup.retrieveAttributeValueString('groupName')}");
  
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.groupTableName", "testgrouper_prov_ldap_group");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.groupAttributeNames", "uuid");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.groupAttributeTableName", "testgrouper_prov_ldap_group_attr");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.groupTableIdColumn", "uuid");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.groupAttributeTableAttributeNameIsGroupMatchingId", "groupName");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.groupAttributeTableForeignKeyToGroup", "group_uuid");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.groupAttributeTableAttributeNameColumn", "attribute_name");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.groupAttributeTableAttributeValueColumn", "attribute_value");
    
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
    attributeValue.setDoProvision(true);
    attributeValue.setTargetName("sqlProvTest");
    attributeValue.setStemScopeString("sub");
  
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem3);
  
    //lets sync these over
    GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveProvisioner("sqlProvTest");
    
    GrouperProvisioningOutput grouperProvisioningOutput = grouperProvisioner.provision(GrouperProvisioningType.fullProvisionFull); 
  
    String sql = "select uuid from testgrouper_prov_ldap_group";
    
    List<Object[]> dataInTable = new GcDbAccess().sql(sql).selectList(Object[].class);
    
    Set<MultiKey> groupNamesInTable = new HashSet<MultiKey>();
    
    for (Object[] row: dataInTable) {
      groupNamesInTable.add(new MultiKey(row));
    }
    
    assertEquals(2, groupNamesInTable.size());
    assertTrue(groupNamesInTable.contains(new MultiKey(new Object[]{"test:testGroup"})));
    assertTrue(groupNamesInTable.contains(new MultiKey(new Object[]{"test3:testGroup3"})));
  
    sql = "select group_uuid, attribute_name, attribute_value from testgrouper_prov_ldap_group_attr";
    
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

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer." + JOB_NAME + ".provisionerTarget", "sqlProvTest");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer." + JOB_NAME + ".provisionerJobSyncType", 
        GrouperProvisioningType.incrementalProvisionChangeLog.name());

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer." + JOB_NAME + ".publisher.debug", "true");

    //clear out changelog
    // run the provisioner, it will init
    hib3GrouperLoaderLog = runJobs(true, true);
    
    // add 4
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
    // this includes fields and attributes etc
    assertEquals(5, GrouperUtil.intValue(hib3GrouperLoaderLog.getDeleteCount(), -1));
//TODO    assertEquals(5, GrouperUtil.intValue(hib3GrouperLoaderLog.getInsertCount(), -1));
//TODO    assertEquals(1, GrouperUtil.intValue(hib3GrouperLoaderLog.getUpdateCount(), -1));

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
  
    sql = "select group_uuid, attribute_name, attribute_value from testgrouper_prov_ldap_group_attr";
    
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
  
    sql = "select group_uuid, attribute_name, attribute_value from testgrouper_prov_ldap_group_attr";
    
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

    
    sql = "select uuid from testgrouper_prov_ldap_group";
    
    dataInTable = new GcDbAccess().sql(sql).selectList(Object[].class);
    
    groupNamesInTable = new HashSet<MultiKey>();
    
    for (Object[] row: dataInTable) {
      groupNamesInTable.add(new MultiKey(row));
    }
    
    assertEquals(2, groupNamesInTable.size());
    assertTrue(groupNamesInTable.contains(new MultiKey(new Object[]{"test:testGroup"})));
    assertTrue(groupNamesInTable.contains(new MultiKey(new Object[]{"test4:testGroup4"})));
  
    sql = "select group_uuid, attribute_name, attribute_value from testgrouper_prov_ldap_group_attr";
    
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
   * esb consumer
   */
  private EsbConsumer esbConsumer;

  /**
   * 
   */
  private final String JOB_NAME = "TEST_SQL_LDAP";

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
      hib3GrouploaderLog.setJobName("CHANGE_LOG_consumer_" + JOB_NAME);
      hib3GrouploaderLog.setStatus(GrouperLoaderStatus.RUNNING.name());
      this.esbConsumer = new EsbConsumer();
      ChangeLogHelper.processRecords(JOB_NAME, hib3GrouploaderLog, this.esbConsumer);
  
      return hib3GrouploaderLog;
    }
    
    return null;
  }

  /**
   * pa use case with group members ldap and user link
   */
  public void testSimpleGroupLdapInsertUpdateDeleteFullSync2() {
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.class", SqlProvisioner.class.getName());
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.dbExternalSystemConfigId", "grouper");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.subjectSourcesToProvision", "jdbc");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.logAllObjectsVerbose", "true");
  
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.grouperToTargetTranslation.0.script", 
        "${grouperTargetGroup.assignAttributeValue('groupName', grouperProvisioningGroup.getName())}");
    // # could be group, membership, or entity
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.grouperToTargetTranslation.0.for", "group");
    //#translate from group auto translated to the common format
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.grouperToTargetTranslation.1.script", 
        "${grouperTargetGroup.addAttributeValueForMembership('subjectId', "
        + "grouperProvisioningEntity.retrieveAttributeValueString('subjectId'))}");
    // # could be group, membership, or entity
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.grouperToTargetTranslation.1.for", "membership");
  
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.grouperToTargetTranslation.2.script", 
        "${grouperTargetMembership.setRemoveFromList(true)}");
    // # could be group, membership, or entity
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.grouperToTargetTranslation.2.for", "membership");
  
    //# could be group, membership, or entity
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.commonToTargetTranslation.1.for", "membership");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.groupMatchingIdExpression", 
        "${targetGroup.retrieveAttributeValueString('groupName')}");
  
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.groupTableName", "testgrouper_prov_ldap_group");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.groupAttributeNames", "uuid");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.groupAttributeTableName", "testgrouper_prov_ldap_group_attr");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.groupTableIdColumn", "uuid");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.groupAttributeTableAttributeNameIsGroupMatchingId", "groupName");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.groupAttributeTableForeignKeyToGroup", "group_uuid");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.groupAttributeTableAttributeNameColumn", "attribute_name");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.groupAttributeTableAttributeValueColumn", "attribute_value");
    
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
    attributeValue.setDoProvision(true);
    attributeValue.setTargetName("sqlProvTest");
    attributeValue.setStemScopeString("sub");
  
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem3);
  
    //lets sync these over
    GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveProvisioner("sqlProvTest");
    
    GrouperProvisioningOutput grouperProvisioningOutput = grouperProvisioner.provision(GrouperProvisioningType.fullProvisionFull); 
  
    String sql = "select uuid from testgrouper_prov_ldap_group";
    
    List<Object[]> dataInTable = new GcDbAccess().sql(sql).selectList(Object[].class);
    
    Set<MultiKey> groupNamesInTable = new HashSet<MultiKey>();
    
    for (Object[] row: dataInTable) {
      groupNamesInTable.add(new MultiKey(row));
    }
    
    assertEquals(2, groupNamesInTable.size());
    assertTrue(groupNamesInTable.contains(new MultiKey(new Object[]{"test:testGroup"})));
    assertTrue(groupNamesInTable.contains(new MultiKey(new Object[]{"test3:testGroup3"})));
  
    sql = "select group_uuid, attribute_name, attribute_value from testgrouper_prov_ldap_group_attr";
    
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
  
    sql = "select uuid from testgrouper_prov_ldap_group";
    
    dataInTable = new GcDbAccess().sql(sql).selectList(Object[].class);
    
    groupNamesInTable = new HashSet<MultiKey>();
    
    for (Object[] row: dataInTable) {
      groupNamesInTable.add(new MultiKey(row));
    }
    
    assertEquals(2, groupNamesInTable.size());
    assertTrue(groupNamesInTable.contains(new MultiKey(new Object[]{"test:testGroup"})));
    assertTrue(groupNamesInTable.contains(new MultiKey(new Object[]{"test4:testGroup4"})));
  
    sql = "select group_uuid, attribute_name, attribute_value from testgrouper_prov_ldap_group_attr";
    
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
      
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.class", SqlProvisioner.class.getName());
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.dbExternalSystemConfigId", "grouper");
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.subjectSourcesToProvision", "jdbc");
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.logAllObjectsVerbose", "true");
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.debugLog", "true");
    
      
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.grouperToTargetTranslation.0.script", 
          "${grouperTargetGroup.assignAttributeValue('groupName', grouperProvisioningGroup.getName())}");
      // # could be group, membership, or entity
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.grouperToTargetTranslation.0.for", "group");
      
      //#translate from group auto translated to the common format
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.grouperToTargetTranslation.1.script", 
          "${grouperTargetGroup.addAttributeValueForMembership('subjectId', "
          + "grouperProvisioningEntity.retrieveAttributeValueString('subjectId'))}");
      // # could be group, membership, or entity
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.grouperToTargetTranslation.1.for", "membership");
    
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.grouperToTargetTranslation.2.script", 
          "${grouperTargetMembership.setRemoveFromList(true)}");
      // # could be group, membership, or entity
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.grouperToTargetTranslation.2.for", "membership");
    
      //# could be group, membership, or entity
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.commonToTargetTranslation.1.for", "membership");
      
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.groupMatchingIdExpression", 
          "${targetGroup.retrieveAttributeValueString('groupName')}");
    
      
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.groupTableName", "testgrouper_prov_ldap_group");
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.groupAttributeNames", "uuid");
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.groupAttributeTableName", "testgrouper_prov_ldap_group_attr");
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.groupTableIdColumn", "uuid");
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.groupAttributeTableAttributeNameIsGroupMatchingId", "groupName");
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.groupAttributeTableForeignKeyToGroup", "group_uuid");
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.groupAttributeTableAttributeNameColumn", "attribute_name");
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.groupAttributeTableAttributeValueColumn", "attribute_value");
      
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
      attributeValue.setDoProvision(true);
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
  
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer." + JOB_NAME + ".provisionerTarget", "sqlProvTest");
  
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
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.sqlProvisioningType", "sqlLikeLdapGroupMemberships");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.subjectSourcesToProvision", "jdbc");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.logAllObjectsVerbose", "true");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.grouperToTargetTranslation.0.script", 
        "${grouperTargetGroup.assignAttributeValue('groupName', grouperProvisioningGroup.getName())}");
    // # could be group, membership, or entity
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.grouperToTargetTranslation.0.for", "group");
    //#translate from group auto translated to the common format
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.grouperToTargetTranslation.1.script", 
        "${grouperTargetGroup.addAttributeValueForMembership('subjectId', "
        + "grouperProvisioningEntity.retrieveAttributeValueString('subjectId'))}");
    // # could be group, membership, or entity
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.grouperToTargetTranslation.1.for", "membership");
  
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.grouperToTargetTranslation.2.script", 
        "${grouperTargetMembership.setRemoveFromList(true)}");
    // # could be group, membership, or entity
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.grouperToTargetTranslation.2.for", "membership");
  
    //# could be group, membership, or entity
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.commonToTargetTranslation.1.for", "membership");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.groupMatchingIdExpression", 
        "${targetGroup.retrieveAttributeValueString('groupName')}");
  
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.groupAttributeNameForMemberships", "subjectId");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.groupTableName", "testgrouper_prov_ldap_group");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.groupAttributeNames", "uuid");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.groupAttributeTableName", "testgrouper_prov_ldap_group_attr");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.groupTableIdColumn", "uuid");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.groupAttributeTableAttributeNameIsGroupMatchingId", "groupName");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.groupAttributeTableForeignKeyToGroup", "group_uuid");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.groupAttributeTableAttributeNameColumn", "attribute_name");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.groupAttributeTableAttributeValueColumn", "attribute_value");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.entityTableName", "testgrouper_prov_ldap_entity");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.entityAttributeNames", "entity_uuid");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.entityAttributeTableName", "testgrouper_prov_ldap_entity_attr");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.entityTableIdColumn", "entity_uuid");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.entityAttributeTableAttributeNameIsEntityMatchingId", "entityName");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.entityAttributeTableForeignKeyToEntity", "entity_uuid");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.entityAttributeTableAttributeNameColumn", "entity_attribute_name");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.entityAttributeTableAttributeValueColumn", "entity_attribute_value");

    // # if provisioning in ui should be enabled
    //# {valueType: "boolean", required: true}
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("provisioningInUi.enable", "true");

    int countFromGroupTable = -1;
    int countFromGroupAttributeTable = -1;
    countFromGroupTable = new GcDbAccess().sql("select count(*) from testgrouper_prov_ldap_group").select(int.class);
    assertEquals(0, countFromGroupTable);
    countFromGroupAttributeTable = new GcDbAccess().sql("select count(*) from testgrouper_prov_ldap_group_attr").select(int.class);
    assertEquals(0, countFromGroupAttributeTable);

    GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveProvisioner("sqlProvTest");
    grouperProvisioner.retrieveGrouperProvisioningBehavior().setGrouperProvisioningType(GrouperProvisioningType.fullProvisionFull);
    grouperProvisioner.retrieveGrouperProvisioningConfiguration().configureProvisioner();

    ProvisioningGroup provisioningGroup1 = new ProvisioningGroup();
    provisioningGroup1.setId("abc123");
    provisioningGroup1.assignAttributeValue("groupName", "a:b:c");
    provisioningGroup1.assignAttributeValue("subjectId", "subjectId0");
    provisioningGroup1.assignAttributeValue("subjectId", "subjectId1");

    provisioningGroup1.addInternal_objectChange(new ProvisioningObjectChange(ProvisioningObjectChangeDataType.field, "id", null, ProvisioningObjectChangeAction.insert, null, "abc123"));
    provisioningGroup1.addInternal_objectChange(new ProvisioningObjectChange(ProvisioningObjectChangeDataType.attribute, null, "groupName", ProvisioningObjectChangeAction.insert, null, "a:b:c"));
    provisioningGroup1.addInternal_objectChange(new ProvisioningObjectChange(ProvisioningObjectChangeDataType.attribute, null, "subjectId", ProvisioningObjectChangeAction.insert, null, "subjectId0"));
    provisioningGroup1.addInternal_objectChange(new ProvisioningObjectChange(ProvisioningObjectChangeDataType.attribute, null, "subjectId", ProvisioningObjectChangeAction.insert, null, "subjectId1"));

    ProvisioningGroup provisioningGroup2 = new ProvisioningGroup();
    provisioningGroup2.setId("def456");
    provisioningGroup2.assignAttributeValue("groupName", "d:e:f");
    provisioningGroup2.assignAttributeValue("subjectId", "subjectId2");
    provisioningGroup2.assignAttributeValue("subjectId", "subjectId3");

    provisioningGroup2.addInternal_objectChange(new ProvisioningObjectChange(ProvisioningObjectChangeDataType.field, "id", null, ProvisioningObjectChangeAction.insert, null, "def456"));
    provisioningGroup2.addInternal_objectChange(new ProvisioningObjectChange(ProvisioningObjectChangeDataType.attribute, null, "groupName", ProvisioningObjectChangeAction.insert, null, "d:e:f"));
    provisioningGroup2.addInternal_objectChange(new ProvisioningObjectChange(ProvisioningObjectChangeDataType.attribute, null, "subjectId", ProvisioningObjectChangeAction.insert, null, "subjectId2"));
    provisioningGroup2.addInternal_objectChange(new ProvisioningObjectChange(ProvisioningObjectChangeDataType.attribute, null, "subjectId", ProvisioningObjectChangeAction.insert, null, "subjectId3"));
    
    ProvisioningGroup provisioningGroup3 = new ProvisioningGroup();
    provisioningGroup3.setId("ghi789");
    provisioningGroup3.assignAttributeValue("groupName", "g:h:i");
    provisioningGroup3.assignAttributeValue("subjectId", "subjectId4");
    provisioningGroup3.assignAttributeValue("subjectId", "subjectId5");

    provisioningGroup3.addInternal_objectChange(new ProvisioningObjectChange(ProvisioningObjectChangeDataType.field, "id", null, ProvisioningObjectChangeAction.insert, null, "ghi789"));
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
      idToGroup.put(provisioningGroup.getId(), provisioningGroup);
    }
    assertEquals(3, idToGroup.size());
    ProvisioningGroup provisioningGroup1retrieved = idToGroup.get("abc123");
    assertEquals("abc123", provisioningGroup1retrieved.getId());
    assertEquals("a:b:c", provisioningGroup1retrieved.retrieveAttributeValueString("groupName"));
    assertTrue(provisioningGroup1retrieved.retrieveAttributeValueSet("subjectId").contains("subjectId0"));
    assertTrue(provisioningGroup1retrieved.retrieveAttributeValueSet("subjectId").contains("subjectId1"));
    ProvisioningGroup provisioningGroup2retrieved = idToGroup.get("def456");
    assertEquals("def456", provisioningGroup2retrieved.getId());
    assertEquals("d:e:f", provisioningGroup2retrieved.retrieveAttributeValueString("groupName"));
    assertTrue(provisioningGroup2retrieved.retrieveAttributeValueSet("subjectId").contains("subjectId2"));
    assertTrue(provisioningGroup2retrieved.retrieveAttributeValueSet("subjectId").contains("subjectId3"));
    ProvisioningGroup provisioningGroup3retrieved = idToGroup.get("ghi789");
    assertEquals("ghi789", provisioningGroup3retrieved.getId());
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
      idToGroup.put(provisioningGroup.getId(), provisioningGroup);
    }
    assertEquals(2, idToGroup.size());
    provisioningGroup1retrieved = idToGroup.get("abc123");
    assertEquals("abc123", provisioningGroup1retrieved.getId());
    assertEquals("a:b:c", provisioningGroup1retrieved.retrieveAttributeValueString("groupName"));
    assertTrue(provisioningGroup1retrieved.retrieveAttributeValueSet("subjectId").contains("subjectId0"));
    assertTrue(provisioningGroup1retrieved.retrieveAttributeValueSet("subjectId").contains("subjectId1"));
    provisioningGroup2retrieved = idToGroup.get("def456");
    assertEquals("def456", provisioningGroup2retrieved.getId());
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
      idToGroup.put(provisioningGroup.getId(), provisioningGroup);
    }
    assertEquals(2, idToGroup.size());
    provisioningGroup1retrieved = idToGroup.get("abc123");
    assertEquals("abc123", provisioningGroup1retrieved.getId());
    assertEquals("a:b:c", provisioningGroup1retrieved.retrieveAttributeValueString("groupName"));
    assertFalse(provisioningGroup1retrieved.getAttributes().containsKey("subjectId"));
    provisioningGroup2retrieved = idToGroup.get("def456");
    assertEquals("def456", provisioningGroup2retrieved.getId());
    assertEquals("d:e:f", provisioningGroup2retrieved.retrieveAttributeValueString("groupName"));
    assertFalse(provisioningGroup2retrieved.getAttributes().containsKey("subjectId"));

    // update groups
    ProvisioningGroup provisioningGroup1update = new ProvisioningGroup();
    provisioningGroup1update.setId("abc123");
    provisioningGroup1update.assignAttributeValue("groupName", "a:b:c");
    provisioningGroup1update.assignAttributeValue("subjectId", "subjectId0");
    provisioningGroup1update.assignAttributeValue("subjectId", "subjectId1");

    provisioningGroup1update.addInternal_objectChange(new ProvisioningObjectChange(ProvisioningObjectChangeDataType.attribute, null, "groupName2", ProvisioningObjectChangeAction.insert, null, "a:b:c2"));
    provisioningGroup1update.addInternal_objectChange(new ProvisioningObjectChange(ProvisioningObjectChangeDataType.attribute, null, "groupName", ProvisioningObjectChangeAction.update, "a:b:c", "a:b:cu"));
    provisioningGroup1update.addInternal_objectChange(new ProvisioningObjectChange(ProvisioningObjectChangeDataType.attribute, null, "subjectId", ProvisioningObjectChangeAction.insert, null, "subjectId0i"));
    provisioningGroup1update.addInternal_objectChange(new ProvisioningObjectChange(ProvisioningObjectChangeDataType.attribute, null, "subjectId", ProvisioningObjectChangeAction.delete, "subjectId1", null));
    provisioningGroup1update.addInternal_objectChange(new ProvisioningObjectChange(ProvisioningObjectChangeDataType.attribute, null, "subjectId", ProvisioningObjectChangeAction.update, "subjectId0", "subjectId0u"));

    ProvisioningGroup provisioningGroup2update = new ProvisioningGroup();
    provisioningGroup2update.setId("def456");
    provisioningGroup2update.assignAttributeValue("groupName", "d:e:f");
    provisioningGroup2update.assignAttributeValue("subjectId", "subjectId2");
    provisioningGroup2update.assignAttributeValue("subjectId", "subjectId3");

    provisioningGroup2update.addInternal_objectChange(new ProvisioningObjectChange(ProvisioningObjectChangeDataType.attribute, null, "groupName2", ProvisioningObjectChangeAction.insert, null, "d:e:f2"));
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
    
    assertEquals(11, attributesInTable.size());
    assertTrue(attributesInTable.contains(new MultiKey("abc123", "groupName2", "a:b:c2")));
    assertTrue(attributesInTable.contains(new MultiKey("abc123", "groupName", "a:b:cu")));
    assertTrue(attributesInTable.contains(new MultiKey("abc123", "subjectId", "subjectId0i")));
    assertTrue(attributesInTable.contains(new MultiKey("abc123", "subjectId", "subjectId0u")));
    assertTrue(attributesInTable.contains(new MultiKey("def456", "groupName2", "d:e:f2")));
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
    new GcDbAccess().sql("insert into testgrouper_prov_ldap_entity_attr (entity_uuid, entity_attribute_name, entity_attribute_value) values (?,?,?)")
      .addBindVar("subject0uuid").addBindVar("dn").addBindVar("subject0").executeSql();
    new GcDbAccess().sql("insert into testgrouper_prov_ldap_entity_attr (entity_uuid, entity_attribute_name, entity_attribute_value) values (?,?,?)")
      .addBindVar("subject0uuid").addBindVar("employeeId").addBindVar("10021368").executeSql();
    
    new GcDbAccess().sql("insert into testgrouper_prov_ldap_entity (entity_uuid) values (?)").addBindVar("subject1uuid").executeSql();
    new GcDbAccess().sql("insert into testgrouper_prov_ldap_entity_attr (entity_uuid, entity_attribute_name, entity_attribute_value) values (?,?,?)")
      .addBindVar("subject1uuid").addBindVar("dn").addBindVar("subject1").executeSql();
    new GcDbAccess().sql("insert into testgrouper_prov_ldap_entity_attr (entity_uuid, entity_attribute_name, entity_attribute_value) values (?,?,?)")
      .addBindVar("subject1uuid").addBindVar("employeeId").addBindVar("12345678").executeSql();

    new GcDbAccess().sql("insert into testgrouper_prov_ldap_entity (entity_uuid) values (?)").addBindVar("subject2uuid").executeSql();
    new GcDbAccess().sql("insert into testgrouper_prov_ldap_entity_attr (entity_uuid, entity_attribute_name, entity_attribute_value) values (?,?,?)")
      .addBindVar("subject2uuid").addBindVar("dn").addBindVar("subject2").executeSql();
    new GcDbAccess().sql("insert into testgrouper_prov_ldap_entity_attr (entity_uuid, entity_attribute_name, entity_attribute_value) values (?,?,?)")
      .addBindVar("subject2uuid").addBindVar("employeeId").addBindVar("34567890").executeSql();

    // retrieve all entities
    TargetDaoRetrieveAllEntitiesResponse targetDaoRetrieveAllEntitiesResponse = grouperProvisioner.retrieveGrouperTargetDaoAdapter().retrieveAllEntities(new TargetDaoRetrieveAllEntitiesRequest());
    Map<String, ProvisioningEntity> idToEntity = new HashMap<String, ProvisioningEntity>();
    for (ProvisioningEntity provisioningEntity : targetDaoRetrieveAllEntitiesResponse.getTargetEntities()) {
      idToEntity.put(provisioningEntity.getId(), provisioningEntity);
    }
    assertEquals(3, idToEntity.size());
    ProvisioningEntity provisioningEntity1retrieved = idToEntity.get("subject0uuid");
    assertEquals(provisioningEntity1retrieved.toString(), "subject0uuid", provisioningEntity1retrieved.getId());
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
    String sql = "select group_uuid, attribute_name, attribute_value from testgrouper_prov_ldap_group_attr";
    
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
  public void createTableLdapEntityAttr(DdlVersionBean ddlVersionBean, Database database) {
  
    String tableName = "testgrouper_prov_ldap_entity_attr";
    
    Table loaderTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, tableName);
  
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "entity_uuid", Types.VARCHAR, "40", true, true);
  
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "entity_attribute_name", Types.VARCHAR, "200", true, true);
  
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "entity_attribute_value", Types.VARCHAR, "200", true, false);
  
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
    attributeValue.setDoProvision(true);
    attributeValue.setTargetName("pspng_oneprod");
    attributeValue.setStemScopeString("sub");

    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);

    // add some entities
    for (int i=0;i<10;i++) {
      String uuid = GrouperUuid.getUuid();
      String dn = "dn_test.subject." + i;
      new GcDbAccess().sql("insert into testgrouper_prov_ldap_entity (entity_uuid) values (?)").addBindVar(uuid).executeSql();
      new GcDbAccess().sql("insert into testgrouper_prov_ldap_entity_attr (entity_uuid, entity_attribute_name, entity_attribute_value) values (?,?,?)")
        .addBindVar(uuid).addBindVar("dn").addBindVar(dn).executeSql();
      new GcDbAccess().sql("insert into testgrouper_prov_ldap_entity_attr (entity_uuid, entity_attribute_name, entity_attribute_value) values (?,?,?)")
        .addBindVar(uuid).addBindVar("employeeID").addBindVar("test.subject." + i).executeSql();
    }
    
    
    //AttributeAssign attributeAssign = stem.getAttributeDelegate().addAttribute(GrouperProvisioningAttributeNames.retrieveAttributeDefNameMarker()).getAttributeAssign();
    //attributeAssign.getAttributeValueDelegate().assignValueString(GrouperProvisioningAttributeNames.retrieveAttributeDefNameDoProvision())
    
    //lets sync these over
    GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveProvisioner("pspng_oneprod");
    
    GrouperProvisioningOutput grouperProvisioningOutput = grouperProvisioner.provision(GrouperProvisioningType.fullProvisionFull); 

    String sql = "select uuid from testgrouper_prov_ldap_group";
    
    List<Object[]> dataInTable = new GcDbAccess().sql(sql).selectList(Object[].class);
    
    Set<MultiKey> groupNamesInTable = new HashSet<MultiKey>();
    
    for (Object[] row: dataInTable) {
      groupNamesInTable.add(new MultiKey(row));
    }
    
    assertEquals(1, groupNamesInTable.size());
    assertTrue(groupNamesInTable.contains(new MultiKey(new Object[]{"test:testGroup"})));

    sql = "select group_uuid, attribute_name, attribute_value from testgrouper_prov_ldap_group_attr";
    
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
    assertTrue(System.currentTimeMillis() >  gcGrouperSync.getLastFullSyncRun().getTime());
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
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.pspng_oneprod.class", SqlProvisioner.class.getName());
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.pspng_oneprod.dbExternalSystemConfigId", "grouper");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.pspng_oneprod.subjectSourcesToProvision", "jdbc");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.pspng_oneprod.logAllObjectsVerbose", "true");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.pspng_oneprod.provisionerName", "One prod LDAP flat");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.pspng_oneprod.sqlProvisioningType", "sqlLikeLdapGroupMemberships");
//    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.pspng_oneprod.groupSearchBaseDn", "OU=Grouper,OU=365Groups,DC=one,DC=upenn,DC=edu");
//    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.pspng_oneprod.userSearchBaseDn", "DC=one,DC=upenn,DC=edu");

    //TODO make an attribute config for this
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.pspng_oneprod.common.entityLink.memberToId2", "${targetEntity.retrieveAttributeValue('dn')}");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.pspng_oneprod.common.groupLink.groupToId2", "${targetGroup.retrieveAttributeValue('dn')}");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.pspng_oneprod.grouperToTargetTranslationMembership.scriptCount", "1");
    
    //TODO make an attribute config for this
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.pspng_oneprod.grouperToTargetTranslationMembership.0.script", 
        "${if (!grouperUtil.isBlank(gcGrouperSyncMember.getMemberToId2())) { "
        + "grouperTargetGroup.addAttributeValueForMembership('member', gcGrouperSyncMember.getMemberToId2());"
        + "}"
        + "}");
    
//    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.pspng_oneprod.grouperToTargetTranslationEntity.scriptCount",  "2");
//    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.pspng_oneprod.grouperToTargetTranslationEntity.0.script",  
//        "${grouperTargetEntity.assignAttributeValue('employeeID',  grouperProvisioningEntity.getSubjectId())}");
//    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.pspng_oneprod.grouperToTargetTranslationEntity.1.script",  
//        "${grouperTargetEntity.assignAttributeValue('dn', gcGrouperSyncMember.getMemberToId2() )}");
          
//    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.pspng_oneprod.grouperToTargetTranslationGroup.scriptCount", "2");
//    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.pspng_oneprod.grouperToTargetTranslationGroup.0.script", 
//        "${grouperTargetGroup.assignAttributeValue('gidNumber', grouperProvisioningGroup.getIdIndex()); }");
//    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.pspng_oneprod.grouperToTargetTranslationGroup.1.script", 
//        "${grouperTargetGroup.assignAttributeValue('dn', gcGrouperSyncGroup.getGroupToId2()); }");

    // TODO matching id
//    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.pspng_oneprod.groupMatchingIdAttribute", "gidNumber");
//    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.pspng_oneprod.entityMatchingIdAttribute", "employeeID");

//    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.pspng_oneprod.grouperToTargetTranslationGroupCreateOnly.scriptCount", "4");
//    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.pspng_oneprod.grouperToTargetTranslationGroupCreateOnly.0.script", 
//        "${grouperTargetGroup.assignAttributeValue('dn', 'cn=' + grouperProvisioningGroup.getName() + ',OU=Grouper,OU=365Groups,DC=one,DC=upenn,DC=edu'); }");
//    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.pspng_oneprod.grouperToTargetTranslationGroupCreateOnly.1.script", 
//        "${grouperTargetGroup.assignAttributeValue('cn', grouperProvisioningGroup.getName()); }");
//    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.pspng_oneprod.grouperToTargetTranslationGroupCreateOnly.2.script", 
//        "${grouperTargetGroup.assignAttributeValue('objectClass', grouperUtil.toSet('group')); }");
////    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.pspng_oneprod.grouperToTargetTranslationGroupCreateOnly.3.script", 
////        "${grouperTargetGroup.setId(edu.internet2.middleware.grouper.internal.util.GrouperUuid.getUuid()); }");
//    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.pspng_oneprod.grouperToTargetTranslationGroupCreateOnly.3.script", 
//        "${grouperTargetGroup.setId(grouperProvisioningGroup.getName()); }");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.pspng_oneprod.targetGroupAttributeCount", "6");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.pspng_oneprod.targetGroupAttribute.0.name", "cn");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.pspng_oneprod.targetGroupAttribute.0.isFieldElseAttribute", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.pspng_oneprod.targetGroupAttribute.0.valueType", "string");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.pspng_oneprod.targetGroupAttribute.0.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.pspng_oneprod.targetGroupAttribute.0.multiValued", "false");
    //TODO do simple scripts without scripts perhaps (strip whitespace, check to see if it matches, and replace with enum)
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.pspng_oneprod.targetGroupAttribute.0.translateExpressionCreateOnly", "${grouperProvisioningGroup.getName()}");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.pspng_oneprod.targetGroupAttribute.1.name", "dn");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.pspng_oneprod.targetGroupAttribute.1.isFieldElseAttribute", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.pspng_oneprod.targetGroupAttribute.1.valueType", "string");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.pspng_oneprod.targetGroupAttribute.1.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.pspng_oneprod.targetGroupAttribute.1.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.pspng_oneprod.targetGroupAttribute.1.multiValued", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.pspng_oneprod.targetGroupAttribute.1.translateExpression", "${gcGrouperSyncGroup.getGroupToId2()}");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.pspng_oneprod.targetGroupAttribute.1.translateExpressionCreateOnly", "${'cn=' + grouperProvisioningGroup.getName() + ',OU=Grouper,OU=365Groups,DC=one,DC=upenn,DC=edu'}");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.pspng_oneprod.targetGroupAttribute.2.name", "gidNumber");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.pspng_oneprod.targetGroupAttribute.2.isFieldElseAttribute", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.pspng_oneprod.targetGroupAttribute.2.valueType", "long");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.pspng_oneprod.targetGroupAttribute.2.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.pspng_oneprod.targetGroupAttribute.2.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.pspng_oneprod.targetGroupAttribute.2.matchingId", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.pspng_oneprod.targetGroupAttribute.2.multiValued", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.pspng_oneprod.targetGroupAttribute.2.translateExpression", "${grouperProvisioningGroup.getIdIndex()}");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.pspng_oneprod.targetGroupAttribute.3.name", "objectClass");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.pspng_oneprod.targetGroupAttribute.3.isFieldElseAttribute", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.pspng_oneprod.targetGroupAttribute.3.valueType", "string");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.pspng_oneprod.targetGroupAttribute.3.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.pspng_oneprod.targetGroupAttribute.3.multiValued", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.pspng_oneprod.targetGroupAttribute.3.translateExpressionCreateOnly", "${grouperUtil.toSet('group')}");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.pspng_oneprod.targetGroupAttribute.4.name", "member");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.pspng_oneprod.targetGroupAttribute.4.valueType", "string");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.pspng_oneprod.targetGroupAttribute.4.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.pspng_oneprod.targetGroupAttribute.4.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.pspng_oneprod.targetGroupAttribute.4.update", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.pspng_oneprod.targetGroupAttribute.4.delete", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.pspng_oneprod.targetGroupAttribute.4.membershipAttribute", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.pspng_oneprod.targetGroupAttribute.4.multiValued", "true");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.pspng_oneprod.targetGroupAttribute.5.name", "id");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.pspng_oneprod.targetGroupAttribute.5.isFieldElseAttribute", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.pspng_oneprod.targetGroupAttribute.5.valueType", "string");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.pspng_oneprod.targetGroupAttribute.5.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.pspng_oneprod.targetGroupAttribute.5.update", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.pspng_oneprod.targetGroupAttribute.5.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.pspng_oneprod.targetGroupAttribute.5.translateExpression", "${grouperProvisioningGroup.getName()}");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.pspng_oneprod.targetEntityAttributeCount", "2");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.pspng_oneprod.targetEntityAttribute.0.name", "dn");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.pspng_oneprod.targetEntityAttribute.0.valueType", "string");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.pspng_oneprod.targetEntityAttribute.0.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.pspng_oneprod.targetEntityAttribute.0.translateExpression", "${gcGrouperSyncMember.getMemberToId2()}");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.pspng_oneprod.targetEntityAttribute.1.name", "employeeID");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.pspng_oneprod.targetEntityAttribute.1.valueType", "string");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.pspng_oneprod.targetEntityAttribute.1.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.pspng_oneprod.targetEntityAttribute.1.matchingId", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.pspng_oneprod.targetEntityAttribute.1.translateExpression", "${grouperProvisioningEntity.getSubjectId()}");
    
//    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.pspng_oneprod.groupSearchAllFilter", "objectclass=group");
//    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.pspng_oneprod.userSearchAllFilter", "employeeID=*");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.pspng_oneprod.userSearchFilter", "employeeID=${targetEntity.retrieveAttributeValue('employeeID')}");
//    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.pspng_oneprod.groupSearchFilter", "(&(objectclass=group) (gidNumber=${grouperProvisioningGroup.retrieveAttributeValue('gidNumber')}))");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.pspng_oneprod.groupSearchFilter", "gidNumber=${targetGroup.retrieveAttributeValue('gidNumber')}");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.pspng_oneprod.userSearchAttributes", "dn,employeeID");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.pspng_oneprod.groupSearchAttributes", "dn,gidNumber");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.pspng_oneprod.createEntities", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.pspng_oneprod.deleteEntities", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.pspng_oneprod.createGroups", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.pspng_oneprod.deleteGroupsNotInGrouper", "true");
//    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.pspng_oneprod.deleteGroupsDeletedGrouper", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.pspng_oneprod.targetGroupAttributeNameForMemberships", "member");
    
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.pspng_oneprod.groupTableName", "testgrouper_prov_ldap_group");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.pspng_oneprod.groupAttributeNames", "uuid");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.pspng_oneprod.groupAttributeTableName", "testgrouper_prov_ldap_group_attr");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.pspng_oneprod.groupTableIdColumn", "uuid");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.pspng_oneprod.groupAttributeTableAttributeNameIsGroupMatchingId", "gidNumber");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.pspng_oneprod.groupAttributeTableForeignKeyToGroup", "group_uuid");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.pspng_oneprod.groupAttributeTableAttributeNameColumn", "attribute_name");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.pspng_oneprod.groupAttributeTableAttributeValueColumn", "attribute_value");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.pspng_oneprod.entityTableName", "testgrouper_prov_ldap_entity");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.pspng_oneprod.entityAttributeNames", "entity_uuid");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.pspng_oneprod.entityAttributeTableName", "testgrouper_prov_ldap_entity_attr");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.pspng_oneprod.entityTableIdColumn", "entity_uuid");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.pspng_oneprod.entityAttributeTableAttributeNameIsEntityMatchingId", "employeeID");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.pspng_oneprod.entityAttributeTableForeignKeyToEntity", "entity_uuid");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.pspng_oneprod.entityAttributeTableAttributeNameColumn", "entity_attribute_name");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.pspng_oneprod.entityAttributeTableAttributeValueColumn", "entity_attribute_value");
  }

  /**
   * just do a simple full sync of groups and memberships
   */
  public void testSimpleGroupLdapPaRealTime() {
    
    long started = System.currentTimeMillis();

    configureLdapPaTestCase();

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
    attributeValue.setDoProvision(true);
    attributeValue.setTargetName("pspng_oneprod");
    attributeValue.setStemScopeString("sub");

    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);

    // add some entities
    for (int i=0;i<10;i++) {
      String uuid = GrouperUuid.getUuid();
      String dn = "dn_test.subject." + i;
      new GcDbAccess().sql("insert into testgrouper_prov_ldap_entity (entity_uuid) values (?)").addBindVar(uuid).executeSql();
      new GcDbAccess().sql("insert into testgrouper_prov_ldap_entity_attr (entity_uuid, entity_attribute_name, entity_attribute_value) values (?,?,?)")
        .addBindVar(uuid).addBindVar("dn").addBindVar(dn).executeSql();
      new GcDbAccess().sql("insert into testgrouper_prov_ldap_entity_attr (entity_uuid, entity_attribute_name, entity_attribute_value) values (?,?,?)")
        .addBindVar(uuid).addBindVar("employeeID").addBindVar("test.subject." + i).executeSql();
    }
    
    
    //AttributeAssign attributeAssign = stem.getAttributeDelegate().addAttribute(GrouperProvisioningAttributeNames.retrieveAttributeDefNameMarker()).getAttributeAssign();
    //attributeAssign.getAttributeValueDelegate().assignValueString(GrouperProvisioningAttributeNames.retrieveAttributeDefNameDoProvision())
    
    //lets sync these over
    GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveProvisioner("pspng_oneprod");
    
    GrouperProvisioningOutput grouperProvisioningOutput = grouperProvisioner.provision(GrouperProvisioningType.fullProvisionFull); 

    String sql = "select uuid from testgrouper_prov_ldap_group";
    
    List<Object[]> dataInTable = new GcDbAccess().sql(sql).selectList(Object[].class);
    
    Set<MultiKey> groupNamesInTable = new HashSet<MultiKey>();
    
    for (Object[] row: dataInTable) {
      groupNamesInTable.add(new MultiKey(row));
    }
    
    assertEquals(1, groupNamesInTable.size());
    assertTrue(groupNamesInTable.contains(new MultiKey(new Object[]{"test:testGroup"})));

    sql = "select group_uuid, attribute_name, attribute_value from testgrouper_prov_ldap_group_attr";
    
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

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer." + JOB_NAME + ".provisionerTarget", "pspng_oneprod");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer." + JOB_NAME + ".provisionerJobSyncType", 
        GrouperProvisioningType.incrementalProvisionChangeLog.name());

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer." + JOB_NAME + ".publisher.debug", "true");

    //clear out changelog
    // run the provisioner, it will init
    hib3GrouperLoaderLog = runJobs(true, true);
    
    // add 4
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
    // this includes fields and attributes etc
    assertEquals(5, GrouperUtil.intValue(hib3GrouperLoaderLog.getDeleteCount(), -1));


  }

}
