package edu.internet2.middleware.grouper.app.sqlProvisioning;

import java.sql.Types;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.model.Table;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
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
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.util.GrouperClientConfig;
import junit.textui.TestRunner;

/**
 * 
 * @author mchyzer
 *
 */
public class SqlMembershipProvisionerTest extends GrouperTest {

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

    TestRunner.run(new SqlMembershipProvisionerTest("testSimpleGroupLdapInsertUpdateDeleteRealTimeChangeLogSize"));
    
  }
  
  public SqlMembershipProvisionerTest() {
    super();
    // TODO Auto-generated constructor stub
  }

  /**
   * 
   * @param name
   */
  public SqlMembershipProvisionerTest(String name) {
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
    
//    dropTableSyncTables();
    GrouperSession.stopQuietly(this.grouperSession);

  }

  /**
   * 
   */
  public void dropTableSyncTables() {
    
    dropTableSyncTable("testgrouper_prov_ldap_group_attr");
    dropTableSyncTable("testgrouper_prov_ldap_group");
    dropTableSyncTable("testgrouper_prov_group");
    dropTableSyncTable("testgrouper_prov_mship0");
    dropTableSyncTable("testgrouper_prov_mship1");
    
  }
  
  
  /**
   * just do a simple full sync of groups and memberships
   */
  public void testSimpleGroupMembershipProvisioningFull_1() {
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.class", SqlMembershipProvisioner.class.getName());
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
        "${grouperTargetMembership.assignAttribute('group_name', grouperProvisioningMembership.getProvisioningGroup().getName())}");
    // # could be group, membership, or entity
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.grouperToTargetTranslation.0.for", "membership");
    //#translate from group auto translated to the common format
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.grouperToTargetTranslation.1.script", 
        "${grouperTargetMembership.assignAttribute('subject_id', grouperProvisioningMembership.getProvisioningEntity().retrieveAttributeValueString('subjectId'))}");
    //# could be group, membership, or entity
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.grouperToTargetTranslation.1.for", "membership");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetMembershipIdExpression", 
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
      
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.class", SqlMembershipProvisioner.class.getName());
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.dbExternalSystemConfigId", "grouper");
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.subjectSourcesToProvision", "jdbc");
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.logAllObjectsVerbose", "true");
  
      
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.grouperToTargetTranslation.0.script", 
          "${grouperTargetGroup.assignAttribute('groupName', grouperProvisioningGroup.getName())}");
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

      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetGroupIdExpression", 
          "${targetGroup.retrieveAttributeValueString('groupName')}");

//      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetMembershipIdExpression", 
//          "${new MultiKey(targetMembership.retrieveAttributeValueString('groupName'), targetMembership.retrieveAttributeValueString('subjectId'))}");
//
//      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetEntityIdExpression", 
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
//          "${commonProvisionToTargetGroup.assignAttribute('groupName', commonGroup.getId());}");
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
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.groupAttributeTableAttributeNameIsGroupTargetId", "groupName");
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
        
        GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.class", SqlMembershipProvisioner.class.getName());
        GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.dbExternalSystemConfigId", "grouper");
        GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.subjectSourcesToProvision", "jdbc");
        GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.logAllObjectsVerbose", "true");
    
        
        GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.grouperToTargetTranslation.0.script", 
            "${grouperTargetGroup.assignAttribute('groupName', grouperProvisioningGroup.getName())}");
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
        
        GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetGroupIdExpression", 
            "${targetGroup.retrieveAttributeValueString('groupName')}");
  
        
        GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.groupTableName", "testgrouper_prov_ldap_group");
        GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.groupAttributeNames", "uuid");
        GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.groupAttributeTableName", "testgrouper_prov_ldap_group_attr");
        GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.groupTableIdColumn", "uuid");
        GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.groupAttributeTableAttributeNameIsGroupTargetId", "groupName");
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
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.class", SqlMembershipProvisioner.class.getName());
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.dbExternalSystemConfigId", "grouper");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.subjectSourcesToProvision", "jdbc");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.logAllObjectsVerbose", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.debugLog", "true");
  
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.grouperToTargetTranslation.0.script", 
        "${grouperTargetGroup.assignAttribute('groupName', grouperProvisioningGroup.getName())}");
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
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetGroupIdExpression", 
        "${targetGroup.retrieveAttributeValueString('groupName')}");
  
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.groupTableName", "testgrouper_prov_ldap_group");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.groupAttributeNames", "uuid");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.groupAttributeTableName", "testgrouper_prov_ldap_group_attr");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.groupTableIdColumn", "uuid");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.groupAttributeTableAttributeNameIsGroupTargetId", "groupName");
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
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.class", SqlMembershipProvisioner.class.getName());
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.dbExternalSystemConfigId", "grouper");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.subjectSourcesToProvision", "jdbc");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.logAllObjectsVerbose", "true");
  
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.grouperToTargetTranslation.0.script", 
        "${grouperTargetGroup.assignAttribute('groupName', grouperProvisioningGroup.getName())}");
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
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetGroupIdExpression", 
        "${targetGroup.retrieveAttributeValueString('groupName')}");
  
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.groupTableName", "testgrouper_prov_ldap_group");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.groupAttributeNames", "uuid");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.groupAttributeTableName", "testgrouper_prov_ldap_group_attr");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.groupTableIdColumn", "uuid");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.groupAttributeTableAttributeNameIsGroupTargetId", "groupName");
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
      
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.class", SqlMembershipProvisioner.class.getName());
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.dbExternalSystemConfigId", "grouper");
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.subjectSourcesToProvision", "jdbc");
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.logAllObjectsVerbose", "true");
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.debugLog", "true");
    
      
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.grouperToTargetTranslation.0.script", 
          "${grouperTargetGroup.assignAttribute('groupName', grouperProvisioningGroup.getName())}");
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
      
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetGroupIdExpression", 
          "${targetGroup.retrieveAttributeValueString('groupName')}");
    
      
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.groupTableName", "testgrouper_prov_ldap_group");
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.groupAttributeNames", "uuid");
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.groupAttributeTableName", "testgrouper_prov_ldap_group_attr");
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.groupTableIdColumn", "uuid");
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.groupAttributeTableAttributeNameIsGroupTargetId", "groupName");
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

}
