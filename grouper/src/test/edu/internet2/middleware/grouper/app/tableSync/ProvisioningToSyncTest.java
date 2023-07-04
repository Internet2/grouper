/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.app.tableSync;

import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
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
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderStatus;
import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningAttributeNames;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningAttributeValue;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningJob;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningService;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.AttributeDefSave;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
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
import edu.internet2.middleware.grouper.misc.GrouperStartup;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSync;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncDao;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncGroup;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncJob;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncLog;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncMember;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncMembership;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcTableSync;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcTableSyncOutput;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcTableSyncSubtype;
import edu.internet2.middleware.grouperClient.util.GrouperClientConfig;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.lang3.RandomStringUtils;
import junit.textui.TestRunner;


/**
 *
 */
public class ProvisioningToSyncTest extends GrouperTest {

  public static void main(String[] args) {
    GrouperStartup.startup();
    //TestRunner.run(new ProvisioningToSyncTest("testGcGrouperSyncLogStoreAndDelete"));
    //TestRunner.run(new ProvisioningToSyncTest("testGcGrouperSyncJobStoreAndDelete"));
    //TestRunner.run(new ProvisioningToSyncTest("testGcGrouperSyncGroupStoreAndDelete"));
    //TestRunner.run(new ProvisioningToSyncTest("testGcGrouperSyncMembershipStoreAndDelete"));
    //TestRunner.run(new ProvisioningToSyncTest("testProvisioningAttributesToGroupSyncFull"));
    TestRunner.run(new ProvisioningToSyncTest("testGcGrouperSyncLogStoreAndDelete"));
    
  }
  
  /**
   * 
   */
  public ProvisioningToSyncTest() {
    super();
    
  }

  /**
   * grouper session
   */
  private GrouperSession grouperSession = null;
  /**
   * 
   */
  private final String JOB_NAME = "TEST_TARGET";

  /**
   * @param name
   */
  public ProvisioningToSyncTest(String name) {
    super(name);
  }

  /**
   * @see edu.internet2.middleware.grouper.helper.GrouperTest#setUp()
   */
  @Override
  protected void setUp() {

    super.setUp();
    
    try {

      this.grouperSession = GrouperSession.startRootSession();
      
      ensureTableSyncTables();
  
      HibernateSession.byHqlStatic().createQuery("delete from TestgrouperSyncSubjectTo").executeUpdate();
      
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    
  }

  @Override
  protected void setupConfigs() {
    super.setupConfigs();
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("provisioningInUi.enable", "true");

    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("provisioning.target.testTarget.key", "testTarget");


  }

  /**
   * @see edu.internet2.middleware.grouper.helper.GrouperTest#tearDown()
   */
  @Override
  protected void tearDown() {
    super.tearDown();
    
    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().clear();
    
// TODO    dropTableSyncTables();
    GrouperSession.stopQuietly(this.grouperSession);

  }

  /**
   * @param ddlVersionBean
   * @param database
   * @param tableName
   */
  public void createTable(DdlVersionBean ddlVersionBean, Database database, String tableName) {

    try {
      new GcDbAccess().sql("select count(1) from " + tableName).select(int.class);
      return;
    } catch (Exception e) {
      //create the object
    }
    
    Table loaderTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, tableName);
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "member_id", 
        Types.VARCHAR, "40", true, true);
 
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "group_id", 
        Types.VARCHAR, "40", true, true);
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "field_id", 
        Types.VARCHAR, "40", true, true);
 
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "list_name", 
        Types.VARCHAR, "40", true, true);
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "group_name", 
        Types.VARCHAR, "400", false, false);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "subject_id", 
        Types.VARCHAR, "400", false, false);
 
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "subject_source", 
        Types.VARCHAR, "40", false, false);
   
    GrouperDdlUtils.ddlutilsTableComment(ddlVersionBean, 
        tableName, tableName);
    
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "member_id", "member_id");
    
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
  
        createView(ddlVersionBean, database, "testgrouper_mship_from_v");

        createTable(ddlVersionBean, database, "testgrouper_sync_subject_to");

      }
      
    });
  }

  /**
   * 
   */
  public void dropTableSyncTables() {
    
    dropTableSyncTable("testgrouper_mship_from_v");
    dropTableSyncTable("testgrouper_sync_subject_to");
    
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
    if (tableName.toLowerCase().endsWith("_v")) {
      try {
        HibernateSession.bySqlStatic().executeSql("drop table " + tableName);
      } catch (Exception e) {
        return;
      }
      
    } else {
      try {
        HibernateSession.bySqlStatic().executeSql("drop table " + tableName);
      } catch (Exception e) {
        return;
      }
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

  public void testGcGrouperSyncGroupStoreAndDelete() {

    //try to store an insert
    GcGrouperSync gcGrouperSync = GcGrouperSyncDao.retrieveOrCreateByProvisionerName(null, "temp123");
    GcGrouperSyncGroup gcGrouperSyncGroup = gcGrouperSync.getGcGrouperSyncGroupDao().groupRetrieveOrCreateByGroupId("abc");
    gcGrouperSync.getGcGrouperSyncDao().storeAllObjects();
    
    assertNotNull(gcGrouperSyncGroup.getId());
    
    gcGrouperSyncGroup = gcGrouperSync.getGcGrouperSyncGroupDao().internal_groupRetrieveFromDbById(gcGrouperSyncGroup.getId());

    assertEquals("abc", gcGrouperSyncGroup.getGroupId());

    //try to store an update
    gcGrouperSyncGroup.setGroupAttributeValueCache0("def");
    gcGrouperSync.getGcGrouperSyncGroupDao().internal_groupStore(gcGrouperSyncGroup);

    gcGrouperSyncGroup = gcGrouperSync.getGcGrouperSyncGroupDao().internal_groupRetrieveFromDbById(gcGrouperSyncGroup.getId());
    assertEquals("def", gcGrouperSyncGroup.getGroupAttributeValueCache0());

    //try to store a delete
    gcGrouperSync.getGcGrouperSyncGroupDao().groupDelete(gcGrouperSyncGroup, false, false);
    
    gcGrouperSyncGroup = gcGrouperSync.getGcGrouperSyncGroupDao().internal_groupRetrieveFromDbById(gcGrouperSyncGroup.getId());

    assertNull(gcGrouperSyncGroup);

    
    //try to store some inserts
    GcGrouperSyncGroup gcGrouperSyncGroup1 = new GcGrouperSyncGroup();
    gcGrouperSyncGroup1.setGrouperSync(gcGrouperSync);
    gcGrouperSyncGroup1.setGroupId("abc");
    GcGrouperSyncGroup gcGrouperSyncGroup2 = new GcGrouperSyncGroup();
    gcGrouperSyncGroup2.setGrouperSync(gcGrouperSync);
    gcGrouperSyncGroup2.setGroupId("def");

    List<GcGrouperSyncGroup> gcGrouperSyncGroups = new ArrayList<GcGrouperSyncGroup>();
    gcGrouperSyncGroups.add(gcGrouperSyncGroup1);
    gcGrouperSyncGroups.add(gcGrouperSyncGroup2);
    
    gcGrouperSync.getGcGrouperSyncGroupDao().internal_groupStore(gcGrouperSyncGroups);
    
    assertNotNull(gcGrouperSyncGroup1.getId());
    assertNotNull(gcGrouperSyncGroup2.getId());

    gcGrouperSyncGroup1 = gcGrouperSync.getGcGrouperSyncGroupDao().internal_groupRetrieveFromDbById(gcGrouperSyncGroup1.getId());
    gcGrouperSyncGroup2 = gcGrouperSync.getGcGrouperSyncGroupDao().internal_groupRetrieveFromDbById(gcGrouperSyncGroup2.getId());

    assertEquals("abc", gcGrouperSyncGroup1.getGroupId());
    assertEquals("def", gcGrouperSyncGroup2.getGroupId());

    //try to store an update
    gcGrouperSyncGroup1.setGroupAttributeValueCache0("mno");
    gcGrouperSyncGroup2.setGroupAttributeValueCache0("pqr");

    gcGrouperSyncGroups = new ArrayList<GcGrouperSyncGroup>();
    gcGrouperSyncGroups.add(gcGrouperSyncGroup1);
    gcGrouperSyncGroups.add(gcGrouperSyncGroup2);
    
    gcGrouperSync.getGcGrouperSyncGroupDao().internal_groupStore(gcGrouperSyncGroups);
    
    gcGrouperSyncGroup1 = gcGrouperSync.getGcGrouperSyncGroupDao().internal_groupRetrieveFromDbById(gcGrouperSyncGroup1.getId());
    gcGrouperSyncGroup2 = gcGrouperSync.getGcGrouperSyncGroupDao().internal_groupRetrieveFromDbById(gcGrouperSyncGroup2.getId());

    assertEquals("mno", gcGrouperSyncGroup1.getGroupAttributeValueCache0());
    assertEquals("pqr", gcGrouperSyncGroup2.getGroupAttributeValueCache0());

    gcGrouperSyncGroups = new ArrayList<GcGrouperSyncGroup>();
    gcGrouperSyncGroups.add(gcGrouperSyncGroup1);
    gcGrouperSyncGroups.add(gcGrouperSyncGroup2);
    
    //try to store a delete
    gcGrouperSync.getGcGrouperSyncGroupDao().groupDelete(gcGrouperSyncGroups, true, true);
    
    gcGrouperSyncGroup1 = gcGrouperSync.getGcGrouperSyncGroupDao().internal_groupRetrieveFromDbById(gcGrouperSyncGroup1.getId());
    assertNull(gcGrouperSyncGroup1);
    gcGrouperSyncGroup2 = gcGrouperSync.getGcGrouperSyncGroupDao().internal_groupRetrieveFromDbById(gcGrouperSyncGroup2.getId());
    assertNull(gcGrouperSyncGroup2);
    
    gcGrouperSync.getGcGrouperSyncDao().delete();

    gcGrouperSync = GcGrouperSyncDao.retrieveOrCreateByProvisionerName(null, "temp124");
    
    //try some inserts, some updates, and some no changes
    Map<String, GcGrouperSyncGroup> groupIdToGcGrouperSyncGroup = gcGrouperSync.getGcGrouperSyncGroupDao().groupRetrieveOrCreateByGroupIds(GrouperUtil.toList("abc", "def"));
    gcGrouperSyncGroup1 = groupIdToGcGrouperSyncGroup.get("abc");
    gcGrouperSyncGroup2 = groupIdToGcGrouperSyncGroup.get("def");
    GcGrouperSyncGroup gcGrouperSyncGroup3 = gcGrouperSync.getGcGrouperSyncGroupDao().groupRetrieveOrCreateByGroupId("mno");
    GcGrouperSyncGroup gcGrouperSyncGroup4 = gcGrouperSync.getGcGrouperSyncGroupDao().groupRetrieveOrCreateByGroupId("pqr");

    int changes = gcGrouperSync.getGcGrouperSyncDao().storeAllObjects() + gcGrouperSync.getInternalObjectsCreatedCount();

    assertEquals(4, changes);
    
    GcGrouperSyncGroup gcGrouperSyncGroup5 = gcGrouperSync.getGcGrouperSyncGroupDao().groupRetrieveOrCreateByGroupId("stu");
    GcGrouperSyncGroup gcGrouperSyncGroup6 = gcGrouperSync.getGcGrouperSyncGroupDao().groupRetrieveOrCreateByGroupId("vwx");

    //try to store an update
    gcGrouperSyncGroup3.setGroupAttributeValueCache0("mno");
    gcGrouperSyncGroup4.setGroupAttributeValueCache0("pqr");

    changes = gcGrouperSync.getGcGrouperSyncDao().storeAllObjects() + gcGrouperSync.getInternalObjectsCreatedCount();

    assertEquals(8, changes);
    
    gcGrouperSyncGroup3 = gcGrouperSync.getGcGrouperSyncGroupDao().internal_groupRetrieveFromDbById(gcGrouperSyncGroup3.getId());
    gcGrouperSyncGroup4 = gcGrouperSync.getGcGrouperSyncGroupDao().internal_groupRetrieveFromDbById(gcGrouperSyncGroup4.getId());

    assertEquals("mno", gcGrouperSyncGroup3.getGroupAttributeValueCache0());
    assertEquals("pqr", gcGrouperSyncGroup4.getGroupAttributeValueCache0());

    gcGrouperSyncGroup3 = gcGrouperSync.getGcGrouperSyncGroupDao().internal_groupRetrieveFromDbByGroupId(gcGrouperSyncGroup3.getGroupId());
    assertEquals("mno", gcGrouperSyncGroup3.getGroupAttributeValueCache0());
    
    gcGrouperSyncGroups = gcGrouperSync.getGcGrouperSyncGroupDao().internal_groupRetrieveFromDbAll();
    assertEquals(6, gcGrouperSyncGroups.size());
    
    
    GcGrouperSyncGroup gcGrouperSyncGroup7 = gcGrouperSync.getGcGrouperSyncGroupDao().groupCreateByGroupId("ghi");
    assertNotNull(gcGrouperSync.getGcGrouperSyncGroupDao().internal_groupRetrieveFromDbById(gcGrouperSyncGroup7.getId()));
    assertEquals(0, gcGrouperSync.getGcGrouperSyncDao().storeAllObjects());
    assertNotNull(gcGrouperSync.getGcGrouperSyncGroupDao().internal_groupRetrieveFromDbById(gcGrouperSyncGroup7.getId()));
    
    gcGrouperSync.getGcGrouperSyncGroupDao().groupDelete(gcGrouperSyncGroup7, true, true);
    assertNull(gcGrouperSync.getGcGrouperSyncGroupDao().internal_groupRetrieveFromDbById(gcGrouperSyncGroup7.getId()));
    assertNull(gcGrouperSync.getGcGrouperSyncGroupDao().groupRetrieveById(gcGrouperSyncGroup7.getId()));

    assertNotNull(gcGrouperSync.getGcGrouperSyncGroupDao().groupRetrieveByGroupId(gcGrouperSyncGroup6.getGroupId()));
    
    groupIdToGcGrouperSyncGroup = gcGrouperSync.getGcGrouperSyncGroupDao().groupRetrieveByGroupIds(GrouperUtil.toSet(gcGrouperSyncGroup1.getGroupId(), gcGrouperSyncGroup2.getGroupId()));

    assertEquals(2, groupIdToGcGrouperSyncGroup.size());
    assertTrue(groupIdToGcGrouperSyncGroup.containsKey(gcGrouperSyncGroup1.getGroupId()));
    assertTrue(groupIdToGcGrouperSyncGroup.containsKey(gcGrouperSyncGroup2.getGroupId()));
    
    groupIdToGcGrouperSyncGroup = gcGrouperSync.getGcGrouperSyncGroupDao().internal_groupRetrieveFromDbByIds(GrouperUtil.toSet(gcGrouperSyncGroup1.getId(), gcGrouperSyncGroup2.getId()));

    assertEquals(2, groupIdToGcGrouperSyncGroup.size());
    assertTrue(groupIdToGcGrouperSyncGroup.containsKey(gcGrouperSyncGroup1.getId()));
    assertTrue(groupIdToGcGrouperSyncGroup.containsKey(gcGrouperSyncGroup2.getId()));

    GcDbAccess.threadLocalQueryCountReset();

    gcGrouperSyncGroup1 = gcGrouperSync.getGcGrouperSyncGroupDao().groupRetrieveByGroupId(gcGrouperSyncGroup1.getGroupId());
    assertEquals("abc", gcGrouperSyncGroup1.getGroupId());
    gcGrouperSyncGroup1 = gcGrouperSync.getGcGrouperSyncGroupDao().groupRetrieveById(gcGrouperSyncGroup1.getId());
    assertEquals("abc", gcGrouperSyncGroup1.getGroupId());

    assertEquals(0, GcDbAccess.threadLocalQueryCountRetrieve());

    gcGrouperSync.getGcGrouperSyncGroupDao().internal_groupCacheDelete(gcGrouperSyncGroup1);
    GcDbAccess.threadLocalQueryCountReset();

    gcGrouperSyncGroup1 = gcGrouperSync.getGcGrouperSyncGroupDao().groupRetrieveByGroupId(gcGrouperSyncGroup1.getGroupId());
    assertEquals("abc", gcGrouperSyncGroup1.getGroupId());

    assertEquals(1, GcDbAccess.threadLocalQueryCountRetrieve());

    gcGrouperSyncGroup1 = gcGrouperSync.getGcGrouperSyncGroupDao().groupRetrieveById(gcGrouperSyncGroup1.getId());
    assertEquals("abc", gcGrouperSyncGroup1.getGroupId());

    assertEquals(1, GcDbAccess.threadLocalQueryCountRetrieve());

    gcGrouperSync.getGcGrouperSyncGroupDao().internal_groupCacheDelete(gcGrouperSyncGroup1);
    GcDbAccess.threadLocalQueryCountReset();

    gcGrouperSyncGroup1 = gcGrouperSync.getGcGrouperSyncGroupDao().groupRetrieveById(gcGrouperSyncGroup1.getId());
    assertEquals("abc", gcGrouperSyncGroup1.getGroupId());

    assertEquals(1, GcDbAccess.threadLocalQueryCountRetrieve());

    gcGrouperSync.getGcGrouperSyncDao().storeAllObjects();

    int existingLogs = gcGrouperSync.getGcGrouperSyncLogDao().internal_logRetrieveFromDbByOwnerId(gcGrouperSyncGroup1.getId()).size();

    assertEquals(0, existingLogs);
    
    GcGrouperSyncLog gcGrouperSyncLog = gcGrouperSync.getGcGrouperSyncGroupDao().groupCreateLog(gcGrouperSyncGroup1);
    gcGrouperSyncLog.setDescriptionToSave("hey");
    assertEquals(1, gcGrouperSync.getGcGrouperSyncDao().storeAllObjects());
    
    List<GcGrouperSyncLog> logs = gcGrouperSync.getGcGrouperSyncLogDao().internal_logRetrieveFromDbByOwnerId(gcGrouperSyncGroup1.getId());
    assertEquals(1, logs.size());
    assertEquals("hey", logs.get(0).getDescriptionOrDescriptionClob());
    
//    (Collection<String>)
    
    //  System.out.println("none");
    //  
    //  for (GcGrouperSyncGroup theGcGrouperSyncGroup : new GcDbAccess().connectionName("grouper").selectList(GcGrouperSyncGroup.class)) {
    //    System.out.println(theGcGrouperSyncGroup.toString());
    //  }
    //  
    //  // foreign key
    //  GcGrouperSync gcGrouperSync = new GcGrouperSync();
    //  gcGrouperSync.setSyncEngine("temp");
    //  gcGrouperSync.setProvisionerName("myJob");
    //  gcGrouperSync.store();
    //  
    //  GcGrouperSyncGroup gcGrouperSyncGroup = new GcGrouperSyncGroup();
    //  gcGrouperSyncGroup.setGrouperSync(gcGrouperSync);
    //  gcGrouperSyncGroup.setLastTimeWorkWasDone(new Timestamp(System.currentTimeMillis() + 2000));
    //  gcGrouperSyncGroup.groupAttributeValueCache0 = "from2";
    //  gcGrouperSyncGroup.groupAttributeValueCache1 = "from3";
    //  gcGrouperSyncGroup.groupId = "myId";
    //  gcGrouperSyncGroup.groupIdIndex = 123L;
    //  gcGrouperSyncGroup.groupName = "myName";
    //  gcGrouperSyncGroup.groupAttributeValueCache2 = "toId2";
    //  gcGrouperSyncGroup.groupAttributeValueCache3 = "toId3";
    //  gcGrouperSyncGroup.inTargetDb = "T";
    //  gcGrouperSyncGroup.inTargetInsertOrExistsDb = "T";
    //  gcGrouperSyncGroup.inTargetEnd = new Timestamp(123L);
    //  gcGrouperSyncGroup.inTargetStart = new Timestamp(234L);
    //  gcGrouperSyncGroup.lastTimeWorkWasDone = new Timestamp(345L);
    //  gcGrouperSyncGroup.provisionableDb = "T";
    //  gcGrouperSyncGroup.provisionableEnd = new Timestamp(456L);
    //  gcGrouperSyncGroup.provisionableStart = new Timestamp(567L);
    //  gcGrouperSync.internal_groupStore(gcGrouperSyncGroup);
    //  
    //  System.out.println("stored");
    //  
    //  gcGrouperSyncGroup = gcGrouperSync.groupRetrieveByGroupId("myId");
    //  System.out.println(gcGrouperSyncGroup);
    //  
    //  gcGrouperSyncGroup.setGroupAttributeValueCache2("toId2a");
    //  gcGrouperSync.internal_groupStore(gcGrouperSyncGroup);
    //
    //  System.out.println("updated");
    //
    //  for (GcGrouperSyncGroup theGcGrouperSyncStatus : new GcDbAccess().connectionName("grouper").selectList(GcGrouperSyncGroup.class)) {
    //    System.out.println(theGcGrouperSyncStatus.toString());
    //  }
    //
    //  gcGrouperSyncGroup.delete();
    //  gcGrouperSync.delete();
    //  
    //  System.out.println("deleted");
    //
    //  for (GcGrouperSyncGroup theGcGrouperSyncStatus : new GcDbAccess().connectionName("grouper").selectList(GcGrouperSyncGroup.class)) {
    //    System.out.println(theGcGrouperSyncStatus.toString());
    //  }

  }
  
//  /**
//   * add provisioning attributes and see them transition to group sync attribute
//   */
//  public void testProvisioningAttributesToGroupSyncFull() {
//    
//    // this wont work without provisioner
//    GrouperSession grouperSession = GrouperSession.startRootSession();
//        
//    Stem testStem = new StemSave(grouperSession).assignName("test").save();
//    
//    Group testGroup1 = new GroupSave(grouperSession).assignName("test:testGroup1").save();
//    
//    testGroup1.addMember(SubjectTestHelper.SUBJ0);
//    testGroup1.addMember(SubjectTestHelper.SUBJ1);
//    
//    Group testGroup2 = new GroupSave(grouperSession).assignName("test:testGroup2").save();
//
//    testGroup2.addMember(SubjectTestHelper.SUBJ2);
//
//    Group testGroup3 = new GroupSave(grouperSession).assignName("test:testGroup3").save();
//
//    testGroup3.addMember(SubjectTestHelper.SUBJ3);
//
//    Group testGroup4 = new GroupSave(grouperSession).assignName("test:test2:testGroup4").assignCreateParentStemsIfNotExist(true).save();
//
//    testGroup4.addMember(SubjectTestHelper.SUBJ4);
//
//    // marker
//    AttributeDefName provisioningMarkerAttributeDefName = GrouperProvisioningAttributeNames.retrieveAttributeDefNameMarker();
//
//    // target name
//    AttributeDefName provisioningTargetAttributeDefName = GrouperProvisioningAttributeNames.retrieveAttributeDefNameTarget();
//
//    // direct name
//    AttributeDefName provisioningDirectAttributeDefName = GrouperProvisioningAttributeNames.retrieveAttributeDefNameDirectAssignment();
//
//    // direct name
//    AttributeDefName provisioningStemScopeAttributeDefName = GrouperProvisioningAttributeNames.retrieveAttributeDefNameStemScope();
//
//    // do provision
//    AttributeDefName provisioningDoProvisionAttributeDefName = GrouperProvisioningAttributeNames.retrieveAttributeDefNameDoProvision();
//    
//    Set<Group> groups = GrouperProvisioningService.findAllGroupsForTarget("testTarget");
//    assertEquals(0, GrouperUtil.length(groups));
//    
//    AttributeAssign testStemAttributeAssign = testStem.getAttributeDelegate().addAttribute(provisioningMarkerAttributeDefName).getAttributeAssign();
//    testStemAttributeAssign.getAttributeValueDelegate().assignValue(provisioningDirectAttributeDefName.getName(), "true");
//    testStemAttributeAssign.getAttributeValueDelegate().assignValue(provisioningStemScopeAttributeDefName.getName(), "sub");
//    testStemAttributeAssign.getAttributeValueDelegate().assignValue(provisioningTargetAttributeDefName.getName(), "testTarget");
//    testStemAttributeAssign.getAttributeValueDelegate().assignValue(provisioningDoProvisionAttributeDefName.getName(), "true");
//
//    AttributeAssign testGroup2attributeAssign = testGroup2.getAttributeDelegate().addAttribute(provisioningMarkerAttributeDefName).getAttributeAssign();
//    testGroup2attributeAssign.getAttributeValueDelegate().assignValue(provisioningDirectAttributeDefName.getName(), "true");
//    testGroup2attributeAssign.getAttributeValueDelegate().assignValue(provisioningTargetAttributeDefName.getName(), "testTarget");
//    testGroup2attributeAssign.getAttributeValueDelegate().assignValue(provisioningDoProvisionAttributeDefName.getName(), "false");
//    
//    GrouperProvisioningJob.runDaemonStandalone();
//    
//    //Then - Most the children of stem0 should have metadata coming from parent
//    GrouperProvisioningAttributeValue grouperProvisioningAttributeValue = GrouperProvisioningService.getProvisioningAttributeValue(testGroup1, "testTarget");
//    assertEquals(testStem.getUuid(), grouperProvisioningAttributeValue.getOwnerStemId());
//    assertFalse(grouperProvisioningAttributeValue.isDirectAssignment());
//    assertEquals("testTarget", grouperProvisioningAttributeValue.getDoProvision());
//    
//    grouperProvisioningAttributeValue = GrouperProvisioningService.getProvisioningAttributeValue(testGroup2, "testTarget");
//    assertNull(grouperProvisioningAttributeValue.getOwnerStemId());
//    assertTrue(grouperProvisioningAttributeValue.isDirectAssignment());
//    assertNull(grouperProvisioningAttributeValue.getDoProvision());
//    
//    grouperProvisioningAttributeValue = GrouperProvisioningService.getProvisioningAttributeValue(testGroup3, "testTarget");
//    assertEquals(testStem.getUuid(), grouperProvisioningAttributeValue.getOwnerStemId());
//    assertFalse(grouperProvisioningAttributeValue.isDirectAssignment());
//    assertEquals("testTarget", grouperProvisioningAttributeValue.getDoProvision());
//    
//    grouperProvisioningAttributeValue = GrouperProvisioningService.getProvisioningAttributeValue(testGroup4, "testTarget");
//    assertEquals(testStem.getUuid(), grouperProvisioningAttributeValue.getOwnerStemId());
//    assertFalse(grouperProvisioningAttributeValue.isDirectAssignment());
//    assertEquals("testTarget", grouperProvisioningAttributeValue.getDoProvision());
//    
//    groups = GrouperProvisioningService.findAllGroupsForTarget("testTarget");
//    assertEquals(3, GrouperUtil.length(groups));
//
//    assertTrue(groups.contains(testGroup1));
//    assertTrue(groups.contains(testGroup3));
//    assertTrue(groups.contains(testGroup4));
//    
//    //TODO move to new method
//    ProvisioningSyncResult provisioningSyncResult = new ProvisioningSyncResult(); //new ProvisioningSyncIntegration().assignTarget("testTarget").fullSync();
//    
//    Map<String, Group> groupIdToGroup = new HashMap<String, Group>(); // provisioningSyncResult.getMapGroupIdToGroup();
//
//    assertEquals(3, groupIdToGroup.size());
//    assertTrue(groupIdToGroup.containsKey(testGroup1.getId()));
//    assertTrue(groupIdToGroup.containsKey(testGroup3.getId()));
//    assertTrue(groupIdToGroup.containsKey(testGroup4.getId()));
//
//    //TODO
//    Map<String, Group> groupIdToGcGrouperSyncGroup = new HashMap<String, Group>();//provisioningSyncResult.getMapGroupIdToGroup();
//
//    assertEquals(3, groupIdToGcGrouperSyncGroup.size());
//    assertTrue(groupIdToGcGrouperSyncGroup.containsKey(testGroup1.getId()));
//    assertTrue(groupIdToGcGrouperSyncGroup.containsKey(testGroup3.getId()));
//    assertTrue(groupIdToGcGrouperSyncGroup.containsKey(testGroup4.getId()));
//
//    GcGrouperSync gcGrouperSync = GcGrouperSyncDao.retrieveOrCreateByProvisionerName("grouper", "testTarget");
//    List<GcGrouperSyncGroup> gcGrouperSyncGroups = gcGrouperSync.getGcGrouperSyncGroupDao().groupRetrieveAll();
//    
//    Set<String> uuidsToProvision = new HashSet<String>();
//    
//    for (GcGrouperSyncGroup gcGrouperSyncGroup : gcGrouperSyncGroups) {
//      if (gcGrouperSyncGroup.isProvisionable()) {
//        uuidsToProvision.add(gcGrouperSyncGroup.getGroupId());
//      }
//    }
//    
//    assertEquals(3, uuidsToProvision.size());
//    assertTrue(uuidsToProvision.contains(testGroup1.getId()));
//    assertTrue(uuidsToProvision.contains(testGroup3.getId()));
//    assertTrue(uuidsToProvision.contains(testGroup4.getId()));
//
//    // ##########  Remove, then add
//    testStemAttributeAssign.getAttributeValueDelegate().assignValue(provisioningDoProvisionAttributeDefName.getName(), "false");
//
//    GrouperProvisioningJob.runDaemonStandalone();
//    
//    //Then - Most the children of stem0 should have metadata coming from parent
//    grouperProvisioningAttributeValue = GrouperProvisioningService.getProvisioningAttributeValue(testGroup1, "testTarget");
//    assertEquals(testStem.getUuid(), grouperProvisioningAttributeValue.getOwnerStemId());
//    assertFalse(grouperProvisioningAttributeValue.isDirectAssignment());
//    assertNull(grouperProvisioningAttributeValue.getDoProvision());
//    
//    grouperProvisioningAttributeValue = GrouperProvisioningService.getProvisioningAttributeValue(testGroup2, "testTarget");
//    assertNull(grouperProvisioningAttributeValue.getOwnerStemId());
//    assertTrue(grouperProvisioningAttributeValue.isDirectAssignment());
//    assertNull(grouperProvisioningAttributeValue.getDoProvision());
//    
//    grouperProvisioningAttributeValue = GrouperProvisioningService.getProvisioningAttributeValue(testGroup3, "testTarget");
//    assertEquals(testStem.getUuid(), grouperProvisioningAttributeValue.getOwnerStemId());
//    assertFalse(grouperProvisioningAttributeValue.isDirectAssignment());
//    assertNull(grouperProvisioningAttributeValue.getDoProvision());
//    
//    grouperProvisioningAttributeValue = GrouperProvisioningService.getProvisioningAttributeValue(testGroup4, "testTarget");
//    assertEquals(testStem.getUuid(), grouperProvisioningAttributeValue.getOwnerStemId());
//    assertFalse(grouperProvisioningAttributeValue.isDirectAssignment());
//    assertNull(grouperProvisioningAttributeValue.getDoProvision());
//    
//    groups = GrouperProvisioningService.findAllGroupsForTarget("testTarget");
//    assertEquals(0, GrouperUtil.length(groups));
//
//    // TODO
//    //provisioningSyncResult = null; //new ProvisioningSyncIntegration().assignTarget("testTarget").fullSync();
//    
//    //groupIdToGroup = provisioningSyncResult.getMapGroupIdToGroup();
//
//    assertEquals(0, groupIdToGroup.size());
//
//    //TODO 
//    //groupIdToGcGrouperSyncGroup = provisioningSyncResult.getMapGroupIdToGroup();
//
//    assertEquals(0, groupIdToGcGrouperSyncGroup.size());
//
//    // ##########  Put back
//    testStemAttributeAssign.getAttributeValueDelegate().assignValue(provisioningDoProvisionAttributeDefName.getName(), "true");
//
//    GrouperProvisioningJob.runDaemonStandalone();
//    
//  }
  
  /**
   * esb consumer
   */
  private EsbConsumer esbConsumer;
  
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
   * @param ddlVersionBean
   * @param database
   * @param viewName
   */
  public void createView(DdlVersionBean ddlVersionBean, Database database, String viewName) {
  
    try {
      new GcDbAccess().sql("select count(1) from " + viewName).select(int.class);
      return;
    } catch (Exception e) {
      //create the object
    }

    GrouperDdlUtils.ddlutilsCreateOrReplaceView(ddlVersionBean, viewName, 
        "test membership view",
        GrouperUtil.toSet("MEMBER_ID", "GROUP_ID", "FIELD_ID", "LIST_NAME", "SUBJECT_ID", "SUBJECT_SOURCE", "GROUP_NAME"),
        GrouperUtil.toSet("MEMBER_ID: uuid of the member",
            "GROUP_ID: uuid of the group",
            "FIELD_ID: uuid of the field",
            "LIST_NAME: name of the list, e.g. members",
            "SUBJECT_ID: of the member of the group", 
            "SUBJECT_SOURCE: of the member of the group", 
            "GROUP_NAME: system name of the group"
            ),
            "select ms.member_id, gg.id as group_id, gf.id, gf.name as list_name, gg.name as group_name, gm.subject_id, gm.subject_source " + 
            "from grouper_memberships ms, grouper_group_set gs, grouper_groups gg, grouper_sync gsync, grouper_sync_group gsg, grouper_members gm, grouper_fields gf " + 
            "where ms.owner_id = gs.member_id and ms.field_id= gs.member_field_id and gs.owner_group_id = gg.id and gm.id = ms.member_id and gf.id = gs.field_id " + 
            "and gsync.id = gsg.grouper_sync_id and gsg.group_id = gg.id and gm.subject_source = 'jdbc' and ms.enabled = 'T' and gsg.provisionable = 'T'" + 
            "");
    
  }

//  /**
//     * add provisioning attributes and see them transition to group sync attribute
//     */
//    public void testEsbConsumer() {
//      
//      GrouperSession grouperSession = GrouperSession.startRootSession();
//      
//      Stem testStem = new StemSave(grouperSession).assignName("test").save();
//      
//      Group testGroup1 = new GroupSave(grouperSession).assignName("test:testGroup1").save();
//      
//      testGroup1.addMember(SubjectTestHelper.SUBJ0);
//      testGroup1.addMember(SubjectTestHelper.SUBJ1);
//      
//      Group testGroup2 = new GroupSave(grouperSession).assignName("test:testGroup2").save();
//  
//      testGroup2.addMember(SubjectTestHelper.SUBJ2);
//  
//      Group testGroup3 = new GroupSave(grouperSession).assignName("test:testGroup3").save();
//  
//      testGroup3.addMember(SubjectTestHelper.SUBJ3);
//  
//      Group testGroup4 = new GroupSave(grouperSession).assignName("test:test2:testGroup4").assignCreateParentStemsIfNotExist(true).save();
//  
//      testGroup4.addMember(SubjectTestHelper.SUBJ4);
//  
//      // marker
//      AttributeDefName provisioningMarkerAttributeDefName = GrouperProvisioningAttributeNames.retrieveAttributeDefNameMarker();
//  
//      // target name
//      AttributeDefName provisioningTargetAttributeDefName = GrouperProvisioningAttributeNames.retrieveAttributeDefNameTarget();
//  
//      // direct name
//      AttributeDefName provisioningDirectAttributeDefName = GrouperProvisioningAttributeNames.retrieveAttributeDefNameDirectAssignment();
//  
//      // direct name
//      AttributeDefName provisioningStemScopeAttributeDefName = GrouperProvisioningAttributeNames.retrieveAttributeDefNameStemScope();
//  
//      // do provision
//      AttributeDefName provisioningDoProvisionAttributeDefName = GrouperProvisioningAttributeNames.retrieveAttributeDefNameDoProvision();
//      
//      Set<Group> groups = GrouperProvisioningService.findAllGroupsForTarget("testTarget");
//      assertEquals(0, GrouperUtil.length(groups));
//      
//      AttributeAssign testStemAttributeAssign = testStem.getAttributeDelegate().addAttribute(provisioningMarkerAttributeDefName).getAttributeAssign();
//      testStemAttributeAssign.getAttributeValueDelegate().assignValue(provisioningDirectAttributeDefName.getName(), "true");
//      testStemAttributeAssign.getAttributeValueDelegate().assignValue(provisioningStemScopeAttributeDefName.getName(), "sub");
//      testStemAttributeAssign.getAttributeValueDelegate().assignValue(provisioningTargetAttributeDefName.getName(), "testTarget");
//      testStemAttributeAssign.getAttributeValueDelegate().assignValue(provisioningDoProvisionAttributeDefName.getName(), "true");
//  
//      AttributeAssign testGroup2attributeAssign = testGroup2.getAttributeDelegate().addAttribute(provisioningMarkerAttributeDefName).getAttributeAssign();
//      testGroup2attributeAssign.getAttributeValueDelegate().assignValue(provisioningDirectAttributeDefName.getName(), "true");
//      testGroup2attributeAssign.getAttributeValueDelegate().assignValue(provisioningTargetAttributeDefName.getName(), "testTarget");
//      testGroup2attributeAssign.getAttributeValueDelegate().assignValue(provisioningDoProvisionAttributeDefName.getName(), "false");
//      
//      // propagate the attributes to children
//      GrouperProvisioningJob.runDaemonStandalone();
//      
//      // create the sync stuff
//      //TODO
//      //new ProvisioningSyncIntegration().assignTarget("testTarget").fullSync();
//            
//      
//      GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("grouperClient.syncTable.testTarget.databaseFrom", "grouper");
//      GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("grouperClient.syncTable.testTarget.tableFrom", "testgrouper_mship_from_v");
//      GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("grouperClient.syncTable.testTarget.databaseTo", "grouper");
//      GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("grouperClient.syncTable.testTarget.tableTo", "testgrouper_sync_subject_to");
//      GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("grouperClient.syncTable.testTarget.columns", "*");
//      GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("grouperClient.syncTable.testTarget.primaryKeyColumns", "group_id, member_id, field_id");
//      
//      
//      GcTableSync gcTableSync = new GcTableSync();
//      GcTableSyncOutput gcTableSyncOutput = gcTableSync.sync("testTarget", GcTableSyncSubtype.fullSyncFull); 
//
//      GcGrouperSync gcGrouperSync = GcGrouperSyncDao.retrieveByProvisionerName(null, "testTarget");
//      GcGrouperSyncJob gcGrouperSyncJob = gcGrouperSync.getGcGrouperSyncJobDao().jobRetrieveBySyncType(GcTableSyncSubtype.fullSyncFull.name());
//      
//      Timestamp lastSyncTimestamp = gcGrouperSyncJob.getLastSyncTimestamp();
//      
//      assertNotNull(lastSyncTimestamp);
//      
//      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer." + JOB_NAME + ".class", 
//          EsbConsumer.class.getName());
//      
//      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer." + JOB_NAME + ".publisher.class", 
//          TableSyncProvisioningConsumer.class.getName());
//      
//      //something that will never fire
//      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer." + JOB_NAME + ".quartzCron", 
//          "9 59 23 31 12 ? 2099");
//      
//      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer." + JOB_NAME + ".elfilter", 
//          "(event.eventType == 'MEMBERSHIP_DELETE' || event.eventType == 'MEMBERSHIP_ADD' || event.eventType == 'MEMBERSHIP_UPDATE')  && event.sourceId == 'jdbc' ");
//
//      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer." + JOB_NAME + ".provisionerConfigId", "testTarget");
//
//      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer." + JOB_NAME + ".provisionerJobSyncType", 
//          GcTableSyncSubtype.incrementalFromIdentifiedPrimaryKeys.name());
//
//      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer." + JOB_NAME + ".publisher.debug", "true");
//
//      //make sure unexpecte events are handled
//      new AttributeDefSave(grouperSession).assignName("test:whateverDef").save();
//      
//      // run the loader, initial run does nothing
//      Hib3GrouperLoaderLog hib3GrouperLoaderLog = runJobs(true, true);
//  
//      assertEquals("SUCCESS", hib3GrouperLoaderLog.getStatus());
//  
//      assertEquals(0, this.esbConsumer.internal_esbConsumerTestingData.changeLogEntryListSize);
//      
//      testGroup4.addMember(SubjectTestHelper.SUBJ5);
//
//      hib3GrouperLoaderLog = runJobs(true, true);
//      
//      assertEquals("SUCCESS", hib3GrouperLoaderLog.getStatus());
//  
//      // theres an add member and an add membership
//      assertEquals(2, this.esbConsumer.internal_esbConsumerTestingData.changeLogEntryListSize);
//      assertEquals(2, this.esbConsumer.internal_esbConsumerTestingData.convertAllChangeLogEventsToEsbEventsSize);
//      assertEquals(0, this.esbConsumer.internal_esbConsumerTestingData.filterInvalidEventTypesSize);
//
//      assertEquals("testTarget", this.esbConsumer.internal_esbConsumerTestingData.provisionerConfigId);
//      assertEquals("incrementalFromIdentifiedPrimaryKeys", this.esbConsumer.internal_esbConsumerTestingData.provisionerJobSyncType);
//      
//      assertEquals(0, this.esbConsumer.internal_esbConsumerTestingData.skippedEventsDueToFullSync);
//      assertEquals(0, this.esbConsumer.internal_esbConsumerTestingData.groupIdCountToAddToTarget);
//      assertEquals(0, this.esbConsumer.internal_esbConsumerTestingData.groupIdCountToRemoveFromTarget);
//      assertEquals(0, this.esbConsumer.internal_esbConsumerTestingData.gcGrouperSyncGroupsCountInitial);
//      assertEquals(0, this.esbConsumer.internal_esbConsumerTestingData.eventsFilteredByGroupEvents);
//      assertEquals(0, this.esbConsumer.internal_esbConsumerTestingData.eventsWithAddedSubjectAttributes);
//      assertEquals(1, this.esbConsumer.internal_esbConsumerTestingData.skippedEventsDueToExpressionLanguageCount);
//      assertEquals("MEMBER_ADD", this.esbConsumer.internal_esbConsumerTestingData.skippedEventsDueToExpressionLanguage.get(0).getEsbEvent().getEventType());
//      assertEquals(1, this.esbConsumer.internal_esbConsumerTestingData.gcGrouperSyncGroupGroupIdsToRetrieveCount);
//      assertEquals(1, this.esbConsumer.internal_esbConsumerTestingData.gcGrouperSyncGroupsRetrievedByEventsSize);
//      assertEquals(1, this.esbConsumer.internal_esbConsumerTestingData.filterByNotProvisionablePreSize);
//      assertEquals(0, this.esbConsumer.internal_esbConsumerTestingData.eventsFilteredByNotProvisionable);
//      assertEquals(0, this.esbConsumer.internal_esbConsumerTestingData.eventsFilteredNotTrackedAtAll);
//      assertEquals(0, this.esbConsumer.internal_esbConsumerTestingData.eventsFilteredNotTrackedOrProvisionable);
//      assertEquals(1, this.esbConsumer.internal_esbConsumerTestingData.filterByNotProvisionablePostSize);
//      
//      // ####### That should add the member to the target
//      
//      
//      // #######  Full sync should remove events
//      testGroup4.deleteMember(SubjectTestHelper.SUBJ5);
//
//      GrouperUtil.sleep(100);
//      
//      gcTableSync = new GcTableSync();
//      gcTableSyncOutput = gcTableSync.sync("testTarget", GcTableSyncSubtype.fullSyncFull); 
//      
//      gcGrouperSync = GcGrouperSyncDao.retrieveByProvisionerName(null, "testTarget");
//      gcGrouperSyncJob = gcGrouperSync.getGcGrouperSyncJobDao().jobRetrieveBySyncType(GcTableSyncSubtype.fullSyncFull.name());
//      
//      assertTrue(gcGrouperSyncJob.getLastSyncTimestamp().getTime() > lastSyncTimestamp.getTime());
//      assertTrue(gcGrouperSync.getLastFullSyncRun().getTime() > lastSyncTimestamp.getTime());
//            
//      assertEquals(1, gcTableSyncOutput.getDelete());
//      assertEquals(0, gcTableSyncOutput.getUpdate());
//      assertEquals(0, gcTableSyncOutput.getInsert());
//
//      hib3GrouperLoaderLog = runJobs(true, true);
//      
//      assertEquals("SUCCESS", hib3GrouperLoaderLog.getStatus());
//  
//      assertEquals(1, this.esbConsumer.internal_esbConsumerTestingData.changeLogEntryListSize);
//      assertEquals(1, this.esbConsumer.internal_esbConsumerTestingData.convertAllChangeLogEventsToEsbEventsSize);
//      assertEquals(0, this.esbConsumer.internal_esbConsumerTestingData.filterInvalidEventTypesSize);
//      assertEquals(1, this.esbConsumer.internal_esbConsumerTestingData.skippedEventsDueToFullSync);
//      
//      // ######### Add a group, should see those events
//      
//      // TODO sdf
//  
//    }

  public void testGcGrouperSyncLogStoreAndDelete() {
  
      //try to store an insert
      GcGrouperSync gcGrouperSync = GcGrouperSyncDao.retrieveOrCreateByProvisionerName(null, "temp123");
      GcGrouperSyncLog gcGrouperSyncLog = gcGrouperSync.getGcGrouperSyncLogDao().logCreateByOwnerId("abc");
      gcGrouperSync.getGcGrouperSyncDao().storeAllObjects();
      
      assertNotNull(gcGrouperSyncLog.getId());
      
      gcGrouperSyncLog = gcGrouperSync.getGcGrouperSyncLogDao().internal_logRetrieveFromDbById(gcGrouperSyncLog.getId());
  
      assertEquals("abc", gcGrouperSyncLog.getGrouperSyncOwnerId());
  
      //try to store an update
      gcGrouperSyncLog.setDescriptionToSave("def");
      gcGrouperSync.getGcGrouperSyncLogDao().internal_logStore(gcGrouperSyncLog);
  
      gcGrouperSyncLog = gcGrouperSync.getGcGrouperSyncLogDao().internal_logRetrieveFromDbById(gcGrouperSyncLog.getId());
      assertEquals("def", gcGrouperSyncLog.getDescriptionOrDescriptionClob());
  
      //try to store a delete
      gcGrouperSync.getGcGrouperSyncLogDao().logDelete(gcGrouperSyncLog);
      
      gcGrouperSyncLog = gcGrouperSync.getGcGrouperSyncLogDao().internal_logRetrieveFromDbById(gcGrouperSyncLog.getId());
  
      assertNull(gcGrouperSyncLog);
  
      
      //try to store some inserts
      GcGrouperSyncLog gcGrouperSyncLog1 = new GcGrouperSyncLog();
      gcGrouperSyncLog1.setGrouperSync(gcGrouperSync);
      gcGrouperSyncLog1.setGrouperSyncOwnerId("abc");
      GcGrouperSyncLog gcGrouperSyncLog2 = new GcGrouperSyncLog();
      gcGrouperSyncLog2.setGrouperSync(gcGrouperSync);
      gcGrouperSyncLog2.setGrouperSyncOwnerId("def");
  
      List<GcGrouperSyncLog> gcGrouperSyncLogs = new ArrayList<GcGrouperSyncLog>();
      gcGrouperSyncLogs.add(gcGrouperSyncLog1);
      gcGrouperSyncLogs.add(gcGrouperSyncLog2);
      
      gcGrouperSync.getGcGrouperSyncLogDao().internal_logStore(gcGrouperSyncLogs);
      
      assertNotNull(gcGrouperSyncLog1.getId());
      assertNotNull(gcGrouperSyncLog2.getId());
  
      gcGrouperSyncLog1 = gcGrouperSync.getGcGrouperSyncLogDao().internal_logRetrieveFromDbById(gcGrouperSyncLog1.getId());
      gcGrouperSyncLog2 = gcGrouperSync.getGcGrouperSyncLogDao().internal_logRetrieveFromDbById(gcGrouperSyncLog2.getId());
  
      assertEquals("abc", gcGrouperSyncLog1.getGrouperSyncOwnerId());
      assertEquals("def", gcGrouperSyncLog2.getGrouperSyncOwnerId());
  
      //try to store an update
      gcGrouperSyncLog1.setDescriptionToSave("mno");
      gcGrouperSyncLog2.setDescriptionToSave("pqr");
  
      gcGrouperSyncLogs = new ArrayList<GcGrouperSyncLog>();
      gcGrouperSyncLogs.add(gcGrouperSyncLog1);
      gcGrouperSyncLogs.add(gcGrouperSyncLog2);
      
      gcGrouperSync.getGcGrouperSyncLogDao().internal_logStore(gcGrouperSyncLogs);
      
      gcGrouperSyncLog1 = gcGrouperSync.getGcGrouperSyncLogDao().internal_logRetrieveFromDbById(gcGrouperSyncLog1.getId());
      gcGrouperSyncLog2 = gcGrouperSync.getGcGrouperSyncLogDao().internal_logRetrieveFromDbById(gcGrouperSyncLog2.getId());
  
      assertEquals("mno", gcGrouperSyncLog1.getDescriptionOrDescriptionClob());
      assertEquals("pqr", gcGrouperSyncLog2.getDescriptionOrDescriptionClob());
  
      gcGrouperSyncLogs = new ArrayList<GcGrouperSyncLog>();
      gcGrouperSyncLogs.add(gcGrouperSyncLog1);
      gcGrouperSyncLogs.add(gcGrouperSyncLog2);
      
      //try to store a delete
      gcGrouperSync.getGcGrouperSyncLogDao().logDelete(gcGrouperSyncLogs);
      
      gcGrouperSyncLog1 = gcGrouperSync.getGcGrouperSyncLogDao().internal_logRetrieveFromDbById(gcGrouperSyncLog1.getId());
      assertNull(gcGrouperSyncLog1);
      gcGrouperSyncLog2 = gcGrouperSync.getGcGrouperSyncLogDao().internal_logRetrieveFromDbById(gcGrouperSyncLog2.getId());
      assertNull(gcGrouperSyncLog2);
      
      gcGrouperSync.getGcGrouperSyncDao().delete();
  
      gcGrouperSync = GcGrouperSyncDao.retrieveOrCreateByProvisionerName(null, "temp124");
      
      //try some inserts, some updates, and some no changes
      gcGrouperSyncLog1 = gcGrouperSync.getGcGrouperSyncLogDao().logCreateByOwnerId("abc");
      gcGrouperSyncLog2 = gcGrouperSync.getGcGrouperSyncLogDao().logCreateByOwnerId("def");
      GcGrouperSyncLog gcGrouperSyncLog3 = gcGrouperSync.getGcGrouperSyncLogDao().logCreateByOwnerId("mno");
      GcGrouperSyncLog gcGrouperSyncLog4 = gcGrouperSync.getGcGrouperSyncLogDao().logCreateByOwnerId("pqr");
  
      int changes = gcGrouperSync.getGcGrouperSyncDao().storeAllObjects() + gcGrouperSync.getInternalObjectsCreatedCount();
  
      assertEquals(4, changes);
      
      GcGrouperSyncLog gcGrouperSyncGroup5 = gcGrouperSync.getGcGrouperSyncLogDao().logCreateByOwnerId("stu");
      GcGrouperSyncLog gcGrouperSyncGroup6 = gcGrouperSync.getGcGrouperSyncLogDao().logCreateByOwnerId("vwx");
  
      //try to store an update
      gcGrouperSyncLog3.setDescriptionToSave("mno");
      
      String randomLongDescription = RandomStringUtils.random(4500, true, true);
      
      gcGrouperSyncLog4.setDescriptionToSave(randomLongDescription);
  
      changes = gcGrouperSync.getGcGrouperSyncDao().storeAllObjects() + gcGrouperSync.getInternalObjectsCreatedCount();
  
      assertEquals(4, changes);
      
      gcGrouperSyncLog3 = gcGrouperSync.getGcGrouperSyncLogDao().internal_logRetrieveFromDbById(gcGrouperSyncLog3.getId());
      gcGrouperSyncLog4 = gcGrouperSync.getGcGrouperSyncLogDao().internal_logRetrieveFromDbById(gcGrouperSyncLog4.getId());
  
      assertEquals("mno", gcGrouperSyncLog3.getDescriptionOrDescriptionClob());
      assertEquals(randomLongDescription, gcGrouperSyncLog4.getDescriptionOrDescriptionClob());
  
      gcGrouperSyncLog3 = gcGrouperSync.getGcGrouperSyncLogDao().internal_logRetrieveFromDbByOwnerId(gcGrouperSyncLog3.getGrouperSyncOwnerId()).get(0);
      assertEquals("mno", gcGrouperSyncLog3.getDescriptionOrDescriptionClob());
      
      gcGrouperSyncLogs = gcGrouperSync.getGcGrouperSyncLogDao().internal_logRetrieveFromDbAll();
      assertEquals(6, gcGrouperSyncLogs.size());
      
      
      GcGrouperSyncLog gcGrouperSyncGroup7 = gcGrouperSync.getGcGrouperSyncLogDao().logCreateByOwnerId("ghi");
      assertNull(gcGrouperSync.getGcGrouperSyncLogDao().internal_logRetrieveFromDbById(gcGrouperSyncGroup7.getId()));
      assertEquals(1, gcGrouperSync.getGcGrouperSyncDao().storeAllObjects());
      assertNotNull(gcGrouperSync.getGcGrouperSyncLogDao().internal_logRetrieveFromDbById(gcGrouperSyncGroup7.getId()));
      
      gcGrouperSync.getGcGrouperSyncLogDao().logDelete(gcGrouperSyncGroup7);
      assertNull(gcGrouperSync.getGcGrouperSyncLogDao().internal_logRetrieveFromDbById(gcGrouperSyncGroup7.getId()));
      assertNull(gcGrouperSync.getGcGrouperSyncLogDao().logRetrieveById(gcGrouperSyncGroup7.getId()));
  
      
  
    }

  public void testGcGrouperSyncJobStoreAndDelete() {
  
      //try to store an insert
      GcGrouperSync gcGrouperSync = GcGrouperSyncDao.retrieveOrCreateByProvisionerName(null, "temp123");
      GcGrouperSyncJob gcGrouperSyncJob = gcGrouperSync.getGcGrouperSyncJobDao().jobRetrieveOrCreateBySyncType("abc");
      gcGrouperSync.getGcGrouperSyncDao().storeAllObjects();
      
      assertNotNull(gcGrouperSyncJob.getId());
      
      gcGrouperSyncJob = gcGrouperSync.getGcGrouperSyncJobDao().internal_jobRetrieveFromDbById(gcGrouperSyncJob.getId());
  
      assertEquals("abc", gcGrouperSyncJob.getSyncType());
  
      //try to store an update
      gcGrouperSyncJob.setErrorMessage("def");
      gcGrouperSync.getGcGrouperSyncJobDao().internal_jobStore(gcGrouperSyncJob);
  
      gcGrouperSyncJob = gcGrouperSync.getGcGrouperSyncJobDao().internal_jobRetrieveFromDbById(gcGrouperSyncJob.getId());
      assertEquals("def", gcGrouperSyncJob.getErrorMessage());
  
      //try to store a delete
      gcGrouperSync.getGcGrouperSyncJobDao().jobDelete(gcGrouperSyncJob, false);
      
      gcGrouperSyncJob = gcGrouperSync.getGcGrouperSyncJobDao().internal_jobRetrieveFromDbById(gcGrouperSyncJob.getId());
  
      assertNull(gcGrouperSyncJob);
  
      
      //try to store some inserts
      GcGrouperSyncJob gcGrouperSyncJob1 = new GcGrouperSyncJob();
      gcGrouperSyncJob1.setGrouperSync(gcGrouperSync);
      gcGrouperSyncJob1.setSyncType("abc");
      GcGrouperSyncJob gcGrouperSyncJob2 = new GcGrouperSyncJob();
      gcGrouperSyncJob2.setGrouperSync(gcGrouperSync);
      gcGrouperSyncJob2.setSyncType("def");
  
      List<GcGrouperSyncJob> gcGrouperSyncJobs = new ArrayList<GcGrouperSyncJob>();
      gcGrouperSyncJobs.add(gcGrouperSyncJob1);
      gcGrouperSyncJobs.add(gcGrouperSyncJob2);
      
      gcGrouperSync.getGcGrouperSyncJobDao().internal_jobStore(gcGrouperSyncJobs);
      
      assertNotNull(gcGrouperSyncJob1.getId());
      assertNotNull(gcGrouperSyncJob2.getId());
  
      gcGrouperSyncJob1 = gcGrouperSync.getGcGrouperSyncJobDao().internal_jobRetrieveFromDbById(gcGrouperSyncJob1.getId());
      gcGrouperSyncJob2 = gcGrouperSync.getGcGrouperSyncJobDao().internal_jobRetrieveFromDbById(gcGrouperSyncJob2.getId());
  
      assertEquals("abc", gcGrouperSyncJob1.getSyncType());
      assertEquals("def", gcGrouperSyncJob2.getSyncType());
  
      //try to store an update
      gcGrouperSyncJob1.setErrorMessage("mno");
      gcGrouperSyncJob2.setErrorMessage("pqr");
  
      gcGrouperSyncJobs = new ArrayList<GcGrouperSyncJob>();
      gcGrouperSyncJobs.add(gcGrouperSyncJob1);
      gcGrouperSyncJobs.add(gcGrouperSyncJob2);
      
      gcGrouperSync.getGcGrouperSyncJobDao().internal_jobStore(gcGrouperSyncJobs);
      
      gcGrouperSyncJob1 = gcGrouperSync.getGcGrouperSyncJobDao().internal_jobRetrieveFromDbById(gcGrouperSyncJob1.getId());
      gcGrouperSyncJob2 = gcGrouperSync.getGcGrouperSyncJobDao().internal_jobRetrieveFromDbById(gcGrouperSyncJob2.getId());
  
      assertEquals("mno", gcGrouperSyncJob1.getErrorMessage());
      assertEquals("pqr", gcGrouperSyncJob2.getErrorMessage());
  
      gcGrouperSyncJobs = new ArrayList<GcGrouperSyncJob>();
      gcGrouperSyncJobs.add(gcGrouperSyncJob1);
      gcGrouperSyncJobs.add(gcGrouperSyncJob2);
      
      //try to store a delete
      gcGrouperSync.getGcGrouperSyncJobDao().jobDelete(gcGrouperSyncJobs, true);
      
      gcGrouperSyncJob1 = gcGrouperSync.getGcGrouperSyncJobDao().internal_jobRetrieveFromDbById(gcGrouperSyncJob1.getId());
      assertNull(gcGrouperSyncJob1);
      gcGrouperSyncJob2 = gcGrouperSync.getGcGrouperSyncJobDao().internal_jobRetrieveFromDbById(gcGrouperSyncJob2.getId());
      assertNull(gcGrouperSyncJob2);
      
      gcGrouperSync.getGcGrouperSyncDao().delete();
  
      gcGrouperSync = GcGrouperSyncDao.retrieveOrCreateByProvisionerName(null, "temp124");
      
      //try some inserts, some updates, and some no changes
      gcGrouperSyncJob1 = gcGrouperSync.getGcGrouperSyncJobDao().jobRetrieveOrCreateBySyncType("abc");
      gcGrouperSyncJob2 = gcGrouperSync.getGcGrouperSyncJobDao().jobRetrieveOrCreateBySyncType("def");
      GcGrouperSyncJob gcGrouperSyncJob3 = gcGrouperSync.getGcGrouperSyncJobDao().jobRetrieveOrCreateBySyncType("mno");
      GcGrouperSyncJob gcGrouperSyncJob4 = gcGrouperSync.getGcGrouperSyncJobDao().jobRetrieveOrCreateBySyncType("pqr");
  
      int changes = gcGrouperSync.getGcGrouperSyncDao().storeAllObjects() + gcGrouperSync.getInternalObjectsCreatedCount();
  
      assertEquals(4, changes);
      
      GcGrouperSyncJob gcGrouperSyncJob5 = gcGrouperSync.getGcGrouperSyncJobDao().jobRetrieveOrCreateBySyncType("stu");
      GcGrouperSyncJob gcGrouperSyncJob6 = gcGrouperSync.getGcGrouperSyncJobDao().jobRetrieveOrCreateBySyncType("vwx");
  
      //try to store an update
      gcGrouperSyncJob3.setErrorMessage("mno");
      gcGrouperSyncJob4.setErrorMessage("pqr");
  
      changes = gcGrouperSync.getGcGrouperSyncDao().storeAllObjects() + gcGrouperSync.getInternalObjectsCreatedCount();
  
      assertEquals(8, changes);
      
      gcGrouperSyncJob3 = gcGrouperSync.getGcGrouperSyncJobDao().internal_jobRetrieveFromDbById(gcGrouperSyncJob3.getId());
      gcGrouperSyncJob4 = gcGrouperSync.getGcGrouperSyncJobDao().internal_jobRetrieveFromDbById(gcGrouperSyncJob4.getId());
  
      assertEquals("mno", gcGrouperSyncJob3.getErrorMessage());
      assertEquals("pqr", gcGrouperSyncJob4.getErrorMessage());
  
      gcGrouperSyncJob3 = gcGrouperSync.getGcGrouperSyncJobDao().internal_jobRetrieveFromDbBySyncType(gcGrouperSyncJob3.getSyncType());
      assertEquals("mno", gcGrouperSyncJob3.getErrorMessage());
      
      gcGrouperSyncJobs = gcGrouperSync.getGcGrouperSyncJobDao().internal_jobRetrieveFromDbAll();
      assertEquals(6, gcGrouperSyncJobs.size());
      
      
      GcGrouperSyncJob gcGrouperSyncJob7 = gcGrouperSync.getGcGrouperSyncJobDao().jobCreateBySyncType("ghi");
      assertNotNull(gcGrouperSync.getGcGrouperSyncJobDao().internal_jobRetrieveFromDbById(gcGrouperSyncJob7.getId()));
      assertEquals(0, gcGrouperSync.getGcGrouperSyncDao().storeAllObjects());
      assertNotNull(gcGrouperSync.getGcGrouperSyncJobDao().internal_jobRetrieveFromDbById(gcGrouperSyncJob7.getId()));
      
      gcGrouperSync.getGcGrouperSyncJobDao().jobDelete(gcGrouperSyncJob7, true);
      assertNull(gcGrouperSync.getGcGrouperSyncJobDao().internal_jobRetrieveFromDbById(gcGrouperSyncJob7.getId()));
      assertNull(gcGrouperSync.getGcGrouperSyncJobDao().jobRetrieveById(gcGrouperSyncJob7.getId()));
  
      assertNotNull(gcGrouperSync.getGcGrouperSyncJobDao().jobRetrieveBySyncType(gcGrouperSyncJob6.getSyncType()));
      
  //TODO    groupRetrieveOrCreateLog(GcGrouperSyncJob)
  
      GcDbAccess.threadLocalQueryCountReset();
  
      gcGrouperSyncJob1 = gcGrouperSync.getGcGrouperSyncJobDao().jobRetrieveBySyncType(gcGrouperSyncJob1.getSyncType());
      assertEquals("abc", gcGrouperSyncJob1.getSyncType());
      gcGrouperSyncJob1 = gcGrouperSync.getGcGrouperSyncJobDao().jobRetrieveById(gcGrouperSyncJob1.getId());
      assertEquals("abc", gcGrouperSyncJob1.getSyncType());
  
      assertEquals(0, GcDbAccess.threadLocalQueryCountRetrieve());
  
      gcGrouperSync.getGcGrouperSyncJobDao().internal_jobCacheDelete(gcGrouperSyncJob1);
      GcDbAccess.threadLocalQueryCountReset();
  
      gcGrouperSyncJob1 = gcGrouperSync.getGcGrouperSyncJobDao().jobRetrieveBySyncType(gcGrouperSyncJob1.getSyncType());
      assertEquals("abc", gcGrouperSyncJob1.getSyncType());
  
      assertEquals(1, GcDbAccess.threadLocalQueryCountRetrieve());
  
      gcGrouperSyncJob1 = gcGrouperSync.getGcGrouperSyncJobDao().jobRetrieveById(gcGrouperSyncJob1.getId());
      assertEquals("abc", gcGrouperSyncJob1.getSyncType());
  
      assertEquals(1, GcDbAccess.threadLocalQueryCountRetrieve());
  
      gcGrouperSync.getGcGrouperSyncJobDao().internal_jobCacheDelete(gcGrouperSyncJob1);
      GcDbAccess.threadLocalQueryCountReset();
  
      gcGrouperSyncJob1 = gcGrouperSync.getGcGrouperSyncJobDao().jobRetrieveById(gcGrouperSyncJob1.getId());
      assertEquals("abc", gcGrouperSyncJob1.getSyncType());
  
      assertEquals(1, GcDbAccess.threadLocalQueryCountRetrieve());
  
      gcGrouperSync.getGcGrouperSyncDao().storeAllObjects();

      GcGrouperSyncLog gcGrouperSyncLog = gcGrouperSync.getGcGrouperSyncLogDao().logCreateByOwnerId(gcGrouperSyncJob1.getId());
      gcGrouperSyncLog.setDescriptionToSave("hey");
      assertEquals(1, gcGrouperSync.getGcGrouperSyncDao().storeAllObjects());
      
      List<GcGrouperSyncLog> logs = gcGrouperSync.getGcGrouperSyncLogDao().internal_logRetrieveFromDbByOwnerId(gcGrouperSyncJob1.getId());
      assertEquals(1, logs.size());
      assertEquals("hey", logs.get(0).getDescriptionOrDescriptionClob());

    }

  public void testGcGrouperSyncMemberStoreAndDelete() {
  
      //try to store an insert
      GcGrouperSync gcGrouperSync = GcGrouperSyncDao.retrieveOrCreateByProvisionerName(null, "temp123");
      GcGrouperSyncMember gcGrouperSyncMember = gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveOrCreateByMemberId("abc");
      gcGrouperSync.getGcGrouperSyncDao().storeAllObjects();
      
      assertNotNull(gcGrouperSyncMember.getId());
      
      gcGrouperSyncMember = gcGrouperSync.getGcGrouperSyncMemberDao().internal_memberRetrieveFromDbById(gcGrouperSyncMember.getId());
  
      assertEquals("abc", gcGrouperSyncMember.getMemberId());
  
      //try to store an update
      gcGrouperSyncMember.setErrorMessage("def");
      gcGrouperSync.getGcGrouperSyncMemberDao().internal_memberStore(gcGrouperSyncMember);
  
      gcGrouperSyncMember = gcGrouperSync.getGcGrouperSyncMemberDao().internal_memberRetrieveFromDbById(gcGrouperSyncMember.getId());
      assertEquals("def", gcGrouperSyncMember.getErrorMessage());
  
      //try to store a delete
      gcGrouperSync.getGcGrouperSyncMemberDao().memberDelete(gcGrouperSyncMember, false, false);
      
      gcGrouperSyncMember = gcGrouperSync.getGcGrouperSyncMemberDao().internal_memberRetrieveFromDbById(gcGrouperSyncMember.getId());
  
      assertNull(gcGrouperSyncMember);
  
      
      //try to store some inserts
      GcGrouperSyncMember gcGrouperSyncMember1 = new GcGrouperSyncMember();
      gcGrouperSyncMember1.setGrouperSync(gcGrouperSync);
      gcGrouperSyncMember1.setMemberId("abc");
      GcGrouperSyncMember gcGrouperSyncMember2 = new GcGrouperSyncMember();
      gcGrouperSyncMember2.setGrouperSync(gcGrouperSync);
      gcGrouperSyncMember2.setMemberId("def");
  
      List<GcGrouperSyncMember> gcGrouperSyncMembers = new ArrayList<GcGrouperSyncMember>();
      gcGrouperSyncMembers.add(gcGrouperSyncMember1);
      gcGrouperSyncMembers.add(gcGrouperSyncMember2);
      
      gcGrouperSync.getGcGrouperSyncMemberDao().internal_memberStore(gcGrouperSyncMembers);
      
      assertNotNull(gcGrouperSyncMember1.getId());
      assertNotNull(gcGrouperSyncMember2.getId());
  
      gcGrouperSyncMember1 = gcGrouperSync.getGcGrouperSyncMemberDao().internal_memberRetrieveFromDbById(gcGrouperSyncMember1.getId());
      gcGrouperSyncMember2 = gcGrouperSync.getGcGrouperSyncMemberDao().internal_memberRetrieveFromDbById(gcGrouperSyncMember2.getId());
  
      assertEquals("abc", gcGrouperSyncMember1.getMemberId());
      assertEquals("def", gcGrouperSyncMember2.getMemberId());
  
      //try to store an update
      gcGrouperSyncMember1.setErrorMessage("mno");
      gcGrouperSyncMember2.setErrorMessage("pqr");
  
      gcGrouperSyncMembers = new ArrayList<GcGrouperSyncMember>();
      gcGrouperSyncMembers.add(gcGrouperSyncMember1);
      gcGrouperSyncMembers.add(gcGrouperSyncMember2);
      
      gcGrouperSync.getGcGrouperSyncMemberDao().internal_memberStore(gcGrouperSyncMembers);
      
      gcGrouperSyncMember1 = gcGrouperSync.getGcGrouperSyncMemberDao().internal_memberRetrieveFromDbById(gcGrouperSyncMember1.getId());
      gcGrouperSyncMember2 = gcGrouperSync.getGcGrouperSyncMemberDao().internal_memberRetrieveFromDbById(gcGrouperSyncMember2.getId());
  
      assertEquals("mno", gcGrouperSyncMember1.getErrorMessage());
      assertEquals("pqr", gcGrouperSyncMember2.getErrorMessage());
  
      gcGrouperSyncMembers = new ArrayList<GcGrouperSyncMember>();
      gcGrouperSyncMembers.add(gcGrouperSyncMember1);
      gcGrouperSyncMembers.add(gcGrouperSyncMember2);
      
      //try to store a delete
      gcGrouperSync.getGcGrouperSyncMemberDao().memberDelete(gcGrouperSyncMembers, true, true);
      
      gcGrouperSyncMember1 = gcGrouperSync.getGcGrouperSyncMemberDao().internal_memberRetrieveFromDbById(gcGrouperSyncMember1.getId());
      assertNull(gcGrouperSyncMember1);
      gcGrouperSyncMember2 = gcGrouperSync.getGcGrouperSyncMemberDao().internal_memberRetrieveFromDbById(gcGrouperSyncMember2.getId());
      assertNull(gcGrouperSyncMember2);
      
      gcGrouperSync.getGcGrouperSyncDao().delete();
  
      gcGrouperSync = GcGrouperSyncDao.retrieveOrCreateByProvisionerName(null, "temp124");
      
      //try some inserts, some updates, and some no changes
      Map<String, GcGrouperSyncMember> groupIdToGcGrouperSyncMember = gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveOrCreateByMemberIds(GrouperUtil.toList("abc", "def"));
      gcGrouperSyncMember1 = groupIdToGcGrouperSyncMember.get("abc");
      gcGrouperSyncMember2 = groupIdToGcGrouperSyncMember.get("def");
      GcGrouperSyncMember gcGrouperSyncMember3 = gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveOrCreateByMemberId("mno");
      GcGrouperSyncMember gcGrouperSyncMember4 = gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveOrCreateByMemberId("pqr");
  
      int changes = gcGrouperSync.getGcGrouperSyncDao().storeAllObjects() + gcGrouperSync.getInternalObjectsCreatedCount();
  
      assertEquals(4, changes);
      
      GcGrouperSyncMember gcGrouperSyncMember5 = gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveOrCreateByMemberId("stu");
      GcGrouperSyncMember gcGrouperSyncMember6 = gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveOrCreateByMemberId("vwx");
  
      //try to store an update
      gcGrouperSyncMember3.setErrorMessage("mno");
      gcGrouperSyncMember4.setErrorMessage("pqr");
  
      changes = gcGrouperSync.getGcGrouperSyncDao().storeAllObjects() + gcGrouperSync.getInternalObjectsCreatedCount();
  
      assertEquals(8, changes);
      
      gcGrouperSyncMember3 = gcGrouperSync.getGcGrouperSyncMemberDao().internal_memberRetrieveFromDbById(gcGrouperSyncMember3.getId());
      gcGrouperSyncMember4 = gcGrouperSync.getGcGrouperSyncMemberDao().internal_memberRetrieveFromDbById(gcGrouperSyncMember4.getId());
  
      assertEquals("mno", gcGrouperSyncMember3.getErrorMessage());
      assertEquals("pqr", gcGrouperSyncMember4.getErrorMessage());
  
      gcGrouperSyncMember3 = gcGrouperSync.getGcGrouperSyncMemberDao().internal_memberRetrieveFromDbByMemberId(gcGrouperSyncMember3.getMemberId());
      assertEquals("mno", gcGrouperSyncMember3.getErrorMessage());
      
      gcGrouperSyncMembers = gcGrouperSync.getGcGrouperSyncMemberDao().internal_memberRetrieveFromDbAll();
      assertEquals(6, gcGrouperSyncMembers.size());
      
      
      GcGrouperSyncMember gcGrouperSyncMember7 = gcGrouperSync.getGcGrouperSyncMemberDao().memberCreateByMemberId("ghi");
      assertNotNull(gcGrouperSync.getGcGrouperSyncMemberDao().internal_memberRetrieveFromDbById(gcGrouperSyncMember7.getId()));
      assertEquals(0, gcGrouperSync.getGcGrouperSyncDao().storeAllObjects());
      assertNotNull(gcGrouperSync.getGcGrouperSyncMemberDao().internal_memberRetrieveFromDbById(gcGrouperSyncMember7.getId()));
      
      gcGrouperSync.getGcGrouperSyncMemberDao().memberDelete(gcGrouperSyncMember7, true, true);
      assertNull(gcGrouperSync.getGcGrouperSyncMemberDao().internal_memberRetrieveFromDbById(gcGrouperSyncMember7.getId()));
      assertNull(gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveById(gcGrouperSyncMember7.getId()));
  
      assertNotNull(gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberId(gcGrouperSyncMember6.getMemberId()));
      
      groupIdToGcGrouperSyncMember = gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberIds(GrouperUtil.toSet(gcGrouperSyncMember1.getMemberId(), gcGrouperSyncMember2.getMemberId()));
  
      assertEquals(2, groupIdToGcGrouperSyncMember.size());
      assertTrue(groupIdToGcGrouperSyncMember.containsKey(gcGrouperSyncMember1.getMemberId()));
      assertTrue(groupIdToGcGrouperSyncMember.containsKey(gcGrouperSyncMember2.getMemberId()));
      
      groupIdToGcGrouperSyncMember = gcGrouperSync.getGcGrouperSyncMemberDao().internal_memberRetrieveFromDbByIds(GrouperUtil.toSet(gcGrouperSyncMember1.getId(), gcGrouperSyncMember2.getId()));
  
      assertEquals(2, groupIdToGcGrouperSyncMember.size());
      assertTrue(groupIdToGcGrouperSyncMember.containsKey(gcGrouperSyncMember1.getId()));
      assertTrue(groupIdToGcGrouperSyncMember.containsKey(gcGrouperSyncMember2.getId()));
  
      GcDbAccess.threadLocalQueryCountReset();
  
      gcGrouperSyncMember1 = gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberId(gcGrouperSyncMember1.getMemberId());
      assertEquals("abc", gcGrouperSyncMember1.getMemberId());
      gcGrouperSyncMember1 = gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveById(gcGrouperSyncMember1.getId());
      assertEquals("abc", gcGrouperSyncMember1.getMemberId());
  
      assertEquals(0, GcDbAccess.threadLocalQueryCountRetrieve());
  
      gcGrouperSync.getGcGrouperSyncMemberDao().internal_memberCacheDelete(gcGrouperSyncMember1);
      GcDbAccess.threadLocalQueryCountReset();
  
      gcGrouperSyncMember1 = gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberId(gcGrouperSyncMember1.getMemberId());
      assertEquals("abc", gcGrouperSyncMember1.getMemberId());
  
      assertEquals(1, GcDbAccess.threadLocalQueryCountRetrieve());
  
      gcGrouperSyncMember1 = gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveById(gcGrouperSyncMember1.getId());
      assertEquals("abc", gcGrouperSyncMember1.getMemberId());
  
      assertEquals(1, GcDbAccess.threadLocalQueryCountRetrieve());
  
      gcGrouperSync.getGcGrouperSyncMemberDao().internal_memberCacheDelete(gcGrouperSyncMember1);
      GcDbAccess.threadLocalQueryCountReset();
  
      gcGrouperSyncMember1 = gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveById(gcGrouperSyncMember1.getId());
      assertEquals("abc", gcGrouperSyncMember1.getMemberId());
  
      assertEquals(1, GcDbAccess.threadLocalQueryCountRetrieve());
  
      gcGrouperSync.getGcGrouperSyncDao().storeAllObjects();
  
      int existingLogs = gcGrouperSync.getGcGrouperSyncLogDao().internal_logRetrieveFromDbByOwnerId(gcGrouperSyncMember1.getId()).size();
  
      assertEquals(0, existingLogs);
      
      GcGrouperSyncLog gcGrouperSyncLog = gcGrouperSync.getGcGrouperSyncMemberDao().memberCreateLog(gcGrouperSyncMember1);
      gcGrouperSyncLog.setDescriptionToSave("hey");
      assertEquals(1, gcGrouperSync.getGcGrouperSyncDao().storeAllObjects());
      
      List<GcGrouperSyncLog> logs = gcGrouperSync.getGcGrouperSyncLogDao().internal_logRetrieveFromDbByOwnerId(gcGrouperSyncMember1.getId());
      assertEquals(1, logs.size());
      assertEquals("hey", logs.get(0).getDescriptionOrDescriptionClob());
      
  
    }

  public void testGcGrouperSyncMembershipStoreAndDelete() {
  
    //try to store an insert
    GcGrouperSync gcGrouperSync = GcGrouperSyncDao.retrieveOrCreateByProvisionerName(null, "temp123");
    
    GcGrouperSyncGroup gcGrouperSyncGroup = gcGrouperSync.getGcGrouperSyncGroupDao().groupCreateByGroupId("group");
    GcGrouperSyncGroup gcGrouperSyncGroup1 = gcGrouperSync.getGcGrouperSyncGroupDao().groupCreateByGroupId("group1");
    GcGrouperSyncGroup gcGrouperSyncGroup2 = gcGrouperSync.getGcGrouperSyncGroupDao().groupCreateByGroupId("group2");
    GcGrouperSyncMember gcGrouperSyncMember = gcGrouperSync.getGcGrouperSyncMemberDao().memberCreateByMemberId("member");
    GcGrouperSyncMember gcGrouperSyncMember1 = gcGrouperSync.getGcGrouperSyncMemberDao().memberCreateByMemberId("member1");
    GcGrouperSyncMember gcGrouperSyncMember2 = gcGrouperSync.getGcGrouperSyncMemberDao().memberCreateByMemberId("member2");
    
    gcGrouperSync = GcGrouperSyncDao.retrieveOrCreateByProvisionerName(null, "temp123");
    
    GcGrouperSyncMembership gcGrouperSyncMembership = gcGrouperSync.getGcGrouperSyncMembershipDao().membershipRetrieveOrCreateByGroupIdAndMemberId("group", "member");
    gcGrouperSyncMembership.setMembershipId("abc");
    gcGrouperSync.getGcGrouperSyncDao().storeAllObjects();
    
    assertNotNull(gcGrouperSyncMembership.getId());
    
    gcGrouperSyncMembership = gcGrouperSync.getGcGrouperSyncMembershipDao().internal_membershipRetrieveFromDbById(gcGrouperSyncMembership.getId());
  
    assertEquals("abc", gcGrouperSyncMembership.getMembershipId());
  
    //try to store an update
    gcGrouperSyncMembership.setErrorMessage("def");
    gcGrouperSync.getGcGrouperSyncMembershipDao().internal_membershipStore(gcGrouperSyncMembership);
  
    gcGrouperSyncMembership = gcGrouperSync.getGcGrouperSyncMembershipDao().internal_membershipRetrieveFromDbById(gcGrouperSyncMembership.getId());
    assertEquals("def", gcGrouperSyncMembership.getErrorMessage());
  
    //try to store a delete
    gcGrouperSync.getGcGrouperSyncMembershipDao().membershipDelete(gcGrouperSyncMembership, false);
    
    gcGrouperSyncMembership = gcGrouperSync.getGcGrouperSyncMembershipDao().internal_membershipRetrieveFromDbById(gcGrouperSyncMembership.getId());
  
    assertNull(gcGrouperSyncMembership);
  
    
    //try to store some inserts
    GcGrouperSyncMembership gcGrouperSyncMembership1 = new GcGrouperSyncMembership();
    gcGrouperSyncMembership1.setGrouperSync(gcGrouperSync);
    gcGrouperSyncMembership1.setGrouperSyncGroupId(gcGrouperSyncGroup1.getId());
    gcGrouperSyncMembership1.setGrouperSyncMemberId(gcGrouperSyncMember1.getId());
    gcGrouperSyncMembership1.setMembershipId("abc");
    GcGrouperSyncMembership gcGrouperSyncMembership2 = new GcGrouperSyncMembership();
    gcGrouperSyncMembership2.setGrouperSync(gcGrouperSync);
    gcGrouperSyncMembership2.setGrouperSyncGroupId(gcGrouperSyncGroup2.getId());
    gcGrouperSyncMembership2.setGrouperSyncMemberId(gcGrouperSyncMember2.getId());
    gcGrouperSyncMembership2.setMembershipId("def");
  
    List<GcGrouperSyncMembership> gcGrouperSyncMemberships = new ArrayList<GcGrouperSyncMembership>();
    gcGrouperSyncMemberships.add(gcGrouperSyncMembership1);
    gcGrouperSyncMemberships.add(gcGrouperSyncMembership2);
    
    gcGrouperSync.getGcGrouperSyncMembershipDao().internal_membershipStore(gcGrouperSyncMemberships);
    
    assertNotNull(gcGrouperSyncMembership1.getId());
    assertNotNull(gcGrouperSyncMembership2.getId());
  
    gcGrouperSyncMembership1 = gcGrouperSync.getGcGrouperSyncMembershipDao().internal_membershipRetrieveFromDbById(gcGrouperSyncMembership1.getId());
    gcGrouperSyncMembership2 = gcGrouperSync.getGcGrouperSyncMembershipDao().internal_membershipRetrieveFromDbById(gcGrouperSyncMembership2.getId());
  
    assertEquals("abc", gcGrouperSyncMembership1.getMembershipId());
    assertEquals("def", gcGrouperSyncMembership2.getMembershipId());
  
    //try to store an update
    gcGrouperSyncMembership1.setErrorMessage("mno");
    gcGrouperSyncMembership2.setErrorMessage("pqr");
  
    gcGrouperSyncMemberships = new ArrayList<GcGrouperSyncMembership>();
    gcGrouperSyncMemberships.add(gcGrouperSyncMembership1);
    gcGrouperSyncMemberships.add(gcGrouperSyncMembership2);
    
    gcGrouperSync.getGcGrouperSyncMembershipDao().internal_membershipStore(gcGrouperSyncMemberships);
    
    gcGrouperSyncMembership1 = gcGrouperSync.getGcGrouperSyncMembershipDao().internal_membershipRetrieveFromDbById(gcGrouperSyncMembership1.getId());
    gcGrouperSyncMembership2 = gcGrouperSync.getGcGrouperSyncMembershipDao().internal_membershipRetrieveFromDbById(gcGrouperSyncMembership2.getId());
  
    assertEquals("mno", gcGrouperSyncMembership1.getErrorMessage());
    assertEquals("pqr", gcGrouperSyncMembership2.getErrorMessage());
  
    gcGrouperSyncMemberships = new ArrayList<GcGrouperSyncMembership>();
    gcGrouperSyncMemberships.add(gcGrouperSyncMembership1);
    gcGrouperSyncMemberships.add(gcGrouperSyncMembership2);
    
    //try to store a delete
    gcGrouperSync.getGcGrouperSyncMembershipDao().membershipDelete(gcGrouperSyncMemberships, true);
    
    gcGrouperSyncMembership1 = gcGrouperSync.getGcGrouperSyncMembershipDao().internal_membershipRetrieveFromDbById(gcGrouperSyncMembership1.getId());
    assertNull(gcGrouperSyncMembership1);
    gcGrouperSyncMembership2 = gcGrouperSync.getGcGrouperSyncMembershipDao().internal_membershipRetrieveFromDbById(gcGrouperSyncMembership2.getId());
    assertNull(gcGrouperSyncMembership2);
    
    gcGrouperSync.getGcGrouperSyncGroupDao().groupDelete(gcGrouperSyncGroup, false, false);
    gcGrouperSync.getGcGrouperSyncGroupDao().groupDelete(gcGrouperSyncGroup1, true, true);
    gcGrouperSync.getGcGrouperSyncGroupDao().groupDelete(gcGrouperSyncGroup2, false, false);
    gcGrouperSync.getGcGrouperSyncMemberDao().memberDelete(gcGrouperSyncMember, false, false);
    gcGrouperSync.getGcGrouperSyncMemberDao().memberDelete(gcGrouperSyncMember1, false, false);
    gcGrouperSync.getGcGrouperSyncMemberDao().memberDelete(gcGrouperSyncMember2, false, false);
    
    gcGrouperSync.getGcGrouperSyncDao().delete();
  
    gcGrouperSync = GcGrouperSyncDao.retrieveOrCreateByProvisionerName(null, "temp124");
  
    gcGrouperSyncGroup = gcGrouperSync.getGcGrouperSyncGroupDao().groupCreateByGroupId("group");
    gcGrouperSyncGroup1 = gcGrouperSync.getGcGrouperSyncGroupDao().groupCreateByGroupId("group1");
    gcGrouperSyncGroup2 = gcGrouperSync.getGcGrouperSyncGroupDao().groupCreateByGroupId("group2");
    gcGrouperSyncMember = gcGrouperSync.getGcGrouperSyncMemberDao().memberCreateByMemberId("member");
    gcGrouperSyncMember1 = gcGrouperSync.getGcGrouperSyncMemberDao().memberCreateByMemberId("member1");
    gcGrouperSyncMember2 = gcGrouperSync.getGcGrouperSyncMemberDao().memberCreateByMemberId("member2");

    GcGrouperSyncGroup gcGrouperSyncGroup3 = gcGrouperSync.getGcGrouperSyncGroupDao().groupCreateByGroupId("group3");
    GcGrouperSyncGroup gcGrouperSyncGroup4 = gcGrouperSync.getGcGrouperSyncGroupDao().groupCreateByGroupId("group4");
    GcGrouperSyncGroup gcGrouperSyncGroup5 = gcGrouperSync.getGcGrouperSyncGroupDao().groupCreateByGroupId("group5");
    GcGrouperSyncGroup gcGrouperSyncGroup6 = gcGrouperSync.getGcGrouperSyncGroupDao().groupCreateByGroupId("group6");
    GcGrouperSyncGroup gcGrouperSyncGroup7 = gcGrouperSync.getGcGrouperSyncGroupDao().groupCreateByGroupId("group7");
    GcGrouperSyncGroup gcGrouperSyncGroup8 = gcGrouperSync.getGcGrouperSyncGroupDao().groupCreateByGroupId("group8");
    GcGrouperSyncMember gcGrouperSyncMember3 = gcGrouperSync.getGcGrouperSyncMemberDao().memberCreateByMemberId("member3");
    GcGrouperSyncMember gcGrouperSyncMember4 = gcGrouperSync.getGcGrouperSyncMemberDao().memberCreateByMemberId("member4");
    GcGrouperSyncMember gcGrouperSyncMember5 = gcGrouperSync.getGcGrouperSyncMemberDao().memberCreateByMemberId("member5");
    GcGrouperSyncMember gcGrouperSyncMember6 = gcGrouperSync.getGcGrouperSyncMemberDao().memberCreateByMemberId("member6");
    GcGrouperSyncMember gcGrouperSyncMember7 = gcGrouperSync.getGcGrouperSyncMemberDao().memberCreateByMemberId("member7");
    GcGrouperSyncMember gcGrouperSyncMember8 = gcGrouperSync.getGcGrouperSyncMemberDao().memberCreateByMemberId("member8");
    
    gcGrouperSync.setInternalObjectsCreatedCount(0);
    
    //try some inserts, some updates, and some no changes
    MultiKey memberhip1multiKey = new MultiKey("group1", "member1");
    MultiKey memberhip2multiKey = new MultiKey("group2", "member2");
    Map<MultiKey, GcGrouperSyncMembership> groupIdToGcGrouperSyncMembership = gcGrouperSync.getGcGrouperSyncMembershipDao().membershipRetrieveOrCreateByGroupIdsAndMemberIds(
        gcGrouperSync.getId(), GrouperUtil.toList(memberhip1multiKey, memberhip2multiKey));
    gcGrouperSyncMembership1 = groupIdToGcGrouperSyncMembership.get(memberhip1multiKey);
    gcGrouperSyncMembership2 = groupIdToGcGrouperSyncMembership.get(memberhip2multiKey);
    GcGrouperSyncMembership gcGrouperSyncMembership3 = gcGrouperSync.getGcGrouperSyncMembershipDao().membershipRetrieveOrCreateByGroupIdAndMemberId("group3", "member3");
    GcGrouperSyncMembership gcGrouperSyncMembership4 = gcGrouperSync.getGcGrouperSyncMembershipDao().membershipRetrieveOrCreateByGroupIdAndMemberId("group4", "member4");
  
    int changes = gcGrouperSync.getGcGrouperSyncDao().storeAllObjects() + gcGrouperSync.getInternalObjectsCreatedCount();
  
    assertEquals(4, changes);

    gcGrouperSyncMembership1.setMembershipId("abc");
    gcGrouperSyncMembership2.setMembershipId("bcd");
    gcGrouperSync.getGcGrouperSyncDao().storeAllObjects();
    
    GcGrouperSyncMembership gcGrouperSyncMembership5 = gcGrouperSync.getGcGrouperSyncMembershipDao().membershipRetrieveOrCreateByGroupIdAndMemberId("group5", "member5");
    GcGrouperSyncMembership gcGrouperSyncMembership6 = gcGrouperSync.getGcGrouperSyncMembershipDao().membershipRetrieveOrCreateByGroupIdAndMemberId("group6", "member6");
  
    //try to store an update
    gcGrouperSyncMembership3.setErrorMessage("mno");
    gcGrouperSyncMembership4.setErrorMessage("pqr");
  
    changes = gcGrouperSync.getGcGrouperSyncDao().storeAllObjects() + gcGrouperSync.getInternalObjectsCreatedCount();
  
    assertEquals(8, changes);
    
    gcGrouperSyncMembership3 = gcGrouperSync.getGcGrouperSyncMembershipDao().internal_membershipRetrieveFromDbById(gcGrouperSyncMembership3.getId());
    gcGrouperSyncMembership4 = gcGrouperSync.getGcGrouperSyncMembershipDao().internal_membershipRetrieveFromDbById(gcGrouperSyncMembership4.getId());
  
    assertEquals("mno", gcGrouperSyncMembership3.getErrorMessage());
    assertEquals("pqr", gcGrouperSyncMembership4.getErrorMessage());
  
    gcGrouperSyncMembership3 = gcGrouperSync.getGcGrouperSyncMembershipDao().membershipRetrieveByGroupIdAndMemberId("group3", "member3");
    assertEquals("mno", gcGrouperSyncMembership3.getErrorMessage());
    
    gcGrouperSyncMemberships = gcGrouperSync.getGcGrouperSyncMembershipDao().internal_membershipRetrieveFromDbAll();
    assertEquals(6, gcGrouperSyncMemberships.size());
    
    GcGrouperSyncMembership gcGrouperSyncMembership7 = gcGrouperSync.getGcGrouperSyncMembershipDao().membershipCreateByGroupAndMember(gcGrouperSyncGroup7, gcGrouperSyncMember7);
    assertNotNull(gcGrouperSync.getGcGrouperSyncMembershipDao().internal_membershipRetrieveFromDbById(gcGrouperSyncMembership7.getId()));
    assertEquals(0, gcGrouperSync.getGcGrouperSyncDao().storeAllObjects());
    assertNotNull(gcGrouperSync.getGcGrouperSyncMembershipDao().internal_membershipRetrieveFromDbById(gcGrouperSyncMembership7.getId()));
    
    gcGrouperSync.getGcGrouperSyncMembershipDao().membershipDelete(gcGrouperSyncMembership7, true);
    assertNull(gcGrouperSync.getGcGrouperSyncMembershipDao().internal_membershipRetrieveFromDbById(gcGrouperSyncMembership7.getId()));
    assertNull(gcGrouperSync.getGcGrouperSyncMembershipDao().membershipRetrieveById(gcGrouperSyncMembership7.getId()));
  
    assertNotNull(gcGrouperSync.getGcGrouperSyncMembershipDao().membershipRetrieveById(gcGrouperSyncMembership6.getId()));
    
    groupIdToGcGrouperSyncMembership = gcGrouperSync.getGcGrouperSyncMembershipDao().membershipRetrieveByGroupIdsAndMemberIds(gcGrouperSync.getId(), GrouperUtil.toSet(
        new MultiKey(gcGrouperSyncGroup1.getGroupId(), gcGrouperSyncMember1.getMemberId()), 
        new MultiKey(gcGrouperSyncGroup2.getGroupId(), gcGrouperSyncMember2.getMemberId())));
  
    assertEquals(2, groupIdToGcGrouperSyncMembership.size());
    assertTrue(groupIdToGcGrouperSyncMembership.containsKey(new MultiKey(gcGrouperSyncGroup1.getGroupId(), gcGrouperSyncMember1.getMemberId())));
    assertTrue(groupIdToGcGrouperSyncMembership.containsKey(new MultiKey(gcGrouperSyncGroup2.getGroupId(), gcGrouperSyncMember2.getMemberId())));
    
    groupIdToGcGrouperSyncMembership = gcGrouperSync.getGcGrouperSyncMembershipDao().internal_membershipRetrieveFromDbBySyncGroupIdsAndSyncMemberIds(
        gcGrouperSync.getId(),
        GrouperUtil.toSet(new MultiKey(gcGrouperSyncMembership1.getGrouperSyncGroupId(), gcGrouperSyncMembership1.getGrouperSyncMemberId()),
            new MultiKey(gcGrouperSyncMembership2.getGrouperSyncGroupId(), gcGrouperSyncMembership2.getGrouperSyncMemberId())));
  
    assertEquals(2, groupIdToGcGrouperSyncMembership.size());
    assertTrue(groupIdToGcGrouperSyncMembership.containsKey(new MultiKey(gcGrouperSyncMembership1.getGrouperSyncGroupId(), gcGrouperSyncMembership1.getGrouperSyncMemberId())));
    assertTrue(groupIdToGcGrouperSyncMembership.containsKey(new MultiKey(gcGrouperSyncMembership2.getGrouperSyncGroupId(), gcGrouperSyncMembership2.getGrouperSyncMemberId())));
  
    GcDbAccess.threadLocalQueryCountReset();
  
    gcGrouperSyncMembership1 = gcGrouperSync.getGcGrouperSyncMembershipDao().membershipRetrieveByGroupIdAndMemberId(gcGrouperSyncGroup1.getGroupId(), gcGrouperSyncMember1.getMemberId());
    assertEquals("abc", gcGrouperSyncMembership1.getMembershipId());
    gcGrouperSyncMembership1 = gcGrouperSync.getGcGrouperSyncMembershipDao().membershipRetrieveById(gcGrouperSyncMembership1.getId());
    assertEquals("abc", gcGrouperSyncMembership1.getMembershipId());
  
    assertEquals(0, GcDbAccess.threadLocalQueryCountRetrieve());
  
    gcGrouperSync.getGcGrouperSyncMembershipDao().internal_membershipCacheDelete(gcGrouperSyncMembership1);
    GcDbAccess.threadLocalQueryCountReset();
  
    gcGrouperSyncMembership1 = gcGrouperSync.getGcGrouperSyncMembershipDao().membershipRetrieveByGroupIdAndMemberId(gcGrouperSyncGroup1.getGroupId(), gcGrouperSyncMember1.getMemberId());
    assertEquals("abc", gcGrouperSyncMembership1.getMembershipId());
  
    assertEquals(1, GcDbAccess.threadLocalQueryCountRetrieve());
  
    gcGrouperSyncMembership1 = gcGrouperSync.getGcGrouperSyncMembershipDao().membershipRetrieveById(gcGrouperSyncMembership1.getId());
    assertEquals("abc", gcGrouperSyncMembership1.getMembershipId());
  
    assertEquals(1, GcDbAccess.threadLocalQueryCountRetrieve());
  
    gcGrouperSync.getGcGrouperSyncMembershipDao().internal_membershipCacheDelete(gcGrouperSyncMembership1);
    GcDbAccess.threadLocalQueryCountReset();
  
    gcGrouperSyncMembership1 = gcGrouperSync.getGcGrouperSyncMembershipDao().membershipRetrieveById(gcGrouperSyncMembership1.getId());
    assertEquals("abc", gcGrouperSyncMembership1.getMembershipId());
  
    assertEquals(1, GcDbAccess.threadLocalQueryCountRetrieve());
  
    gcGrouperSync.getGcGrouperSyncDao().storeAllObjects();
  
    int existingLogs = gcGrouperSync.getGcGrouperSyncLogDao().internal_logRetrieveFromDbByOwnerId(gcGrouperSyncMembership1.getId()).size();
  
    assertEquals(0, existingLogs);
    
    GcGrouperSyncLog gcGrouperSyncLog = gcGrouperSync.getGcGrouperSyncMembershipDao().membershipCreateLog(gcGrouperSyncMembership1);
    gcGrouperSyncLog.setDescriptionToSave("hey");
    assertEquals(1, gcGrouperSync.getGcGrouperSyncDao().storeAllObjects());
    
    List<GcGrouperSyncLog> logs = gcGrouperSync.getGcGrouperSyncLogDao().internal_logRetrieveFromDbByOwnerId(gcGrouperSyncMembership1.getId());
    assertEquals(1, logs.size());
    assertEquals("hey", logs.get(0).getDescriptionOrDescriptionClob());
    
  
  }

//  /**
//   * add provisioning attributes and see them transition to group sync attribute
//   */
//  public void testEsbConsumerInvalidEvent() {
//    
//    GrouperSession grouperSession = GrouperSession.startRootSession();
//    
//    Stem testStem = new StemSave(grouperSession).assignName("test").save();
//    
//    Group testGroup1 = new GroupSave(grouperSession).assignName("test:testGroup1").save();
//    
//    testGroup1.addMember(SubjectTestHelper.SUBJ0);
//    testGroup1.addMember(SubjectTestHelper.SUBJ1);
//    
//    Group testGroup2 = new GroupSave(grouperSession).assignName("test:testGroup2").save();
//  
//    testGroup2.addMember(SubjectTestHelper.SUBJ2);
//  
//    Group testGroup3 = new GroupSave(grouperSession).assignName("test:testGroup3").save();
//  
//    testGroup3.addMember(SubjectTestHelper.SUBJ3);
//  
//    Group testGroup4 = new GroupSave(grouperSession).assignName("test:test2:testGroup4").assignCreateParentStemsIfNotExist(true).save();
//  
//    testGroup4.addMember(SubjectTestHelper.SUBJ4);
//  
//    // marker
//    AttributeDefName provisioningMarkerAttributeDefName = GrouperProvisioningAttributeNames.retrieveAttributeDefNameMarker();
//  
//    // target name
//    AttributeDefName provisioningTargetAttributeDefName = GrouperProvisioningAttributeNames.retrieveAttributeDefNameTarget();
//  
//    // direct name
//    AttributeDefName provisioningDirectAttributeDefName = GrouperProvisioningAttributeNames.retrieveAttributeDefNameDirectAssignment();
//  
//    // direct name
//    AttributeDefName provisioningStemScopeAttributeDefName = GrouperProvisioningAttributeNames.retrieveAttributeDefNameStemScope();
//  
//    // do provision
//    AttributeDefName provisioningDoProvisionAttributeDefName = GrouperProvisioningAttributeNames.retrieveAttributeDefNameDoProvision();
//    
//    Set<Group> groups = GrouperProvisioningService.findAllGroupsForTarget("testTarget");
//    assertEquals(0, GrouperUtil.length(groups));
//    
//    AttributeAssign testStemAttributeAssign = testStem.getAttributeDelegate().addAttribute(provisioningMarkerAttributeDefName).getAttributeAssign();
//    testStemAttributeAssign.getAttributeValueDelegate().assignValue(provisioningDirectAttributeDefName.getName(), "true");
//    testStemAttributeAssign.getAttributeValueDelegate().assignValue(provisioningStemScopeAttributeDefName.getName(), "sub");
//    testStemAttributeAssign.getAttributeValueDelegate().assignValue(provisioningTargetAttributeDefName.getName(), "testTarget");
//    testStemAttributeAssign.getAttributeValueDelegate().assignValue(provisioningDoProvisionAttributeDefName.getName(), "true");
//  
//    AttributeAssign testGroup2attributeAssign = testGroup2.getAttributeDelegate().addAttribute(provisioningMarkerAttributeDefName).getAttributeAssign();
//    testGroup2attributeAssign.getAttributeValueDelegate().assignValue(provisioningDirectAttributeDefName.getName(), "true");
//    testGroup2attributeAssign.getAttributeValueDelegate().assignValue(provisioningTargetAttributeDefName.getName(), "testTarget");
//    testGroup2attributeAssign.getAttributeValueDelegate().assignValue(provisioningDoProvisionAttributeDefName.getName(), "false");
//    
//    // propagate the attributes to children
//    GrouperProvisioningJob.runDaemonStandalone();
//    
//    // create the sync stuff
//    // TODO
//    //new ProvisioningSyncIntegration().assignTarget("testTarget").fullSync();
//          
//    
//    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("grouperClient.syncTable.testTarget.databaseFrom", "grouper");
//    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("grouperClient.syncTable.testTarget.tableFrom", "testgrouper_mship_from_v");
//    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("grouperClient.syncTable.testTarget.databaseTo", "grouper");
//    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("grouperClient.syncTable.testTarget.tableTo", "testgrouper_sync_subject_to");
//    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("grouperClient.syncTable.testTarget.columns", "*");
//    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("grouperClient.syncTable.testTarget.primaryKeyColumns", "group_id, member_id, field_id");
//    
//    
//    GcTableSync gcTableSync = new GcTableSync();
//    GcTableSyncOutput gcTableSyncOutput = gcTableSync.sync("testTarget", GcTableSyncSubtype.fullSyncFull); 
//  
//    GcGrouperSync gcGrouperSync = GcGrouperSyncDao.retrieveByProvisionerName(null, "testTarget");
//    GcGrouperSyncJob gcGrouperSyncJob = gcGrouperSync.getGcGrouperSyncJobDao().jobRetrieveBySyncType(GcTableSyncSubtype.fullSyncFull.name());
//    
//    Timestamp lastSyncTimestamp = gcGrouperSyncJob.getLastSyncTimestamp();
//    
//    assertNotNull(lastSyncTimestamp);
//    
//    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer." + JOB_NAME + ".class", 
//        EsbConsumer.class.getName());
//    
//    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer." + JOB_NAME + ".publisher.class", 
//        TableSyncProvisioningConsumer.class.getName());
//    
//    //something that will never fire
//    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer." + JOB_NAME + ".quartzCron", 
//        "9 59 23 31 12 ? 2099");
//    
//    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer." + JOB_NAME + ".provisionerConfigId", "testTarget");
//  
//    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer." + JOB_NAME + ".provisionerJobSyncType", 
//        GcTableSyncSubtype.incrementalFromIdentifiedPrimaryKeys.name());
//  
//    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer." + JOB_NAME + ".publisher.debug", "true");
//  
//    // run the loader, initial run tries that event
//    Hib3GrouperLoaderLog hib3GrouperLoaderLog = runJobs(true, true);
//
//    //make sure unexpecte events are handled
//    new AttributeDefSave(grouperSession).assignName("test:whateverDef").save();
//    
//    hib3GrouperLoaderLog = runJobs(true, true);
//    assertEquals("SUCCESS", hib3GrouperLoaderLog.getStatus());
//  }

  /**
   * add provisioning attributes and see them transition to group sync attribute
   */
  public void testEsbConsumerPrint() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
              
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer." + JOB_NAME + ".class", 
        EsbConsumer.class.getName());
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer." + JOB_NAME + ".publisher.class", 
        ProvisioningSampleListener.class.getName());
    
    //something that will never fire
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer." + JOB_NAME + ".quartzCron", 
        "9 59 23 31 12 ? 2099");
    
  
    // run the loader, initial run tries that event
    Hib3GrouperLoaderLog hib3GrouperLoaderLog = runJobs(true, true);
  
    int messageCount = ProvisioningSampleListener.messageCount;
    
    Stem testStem = new StemSave(grouperSession).assignName("test").save();
    
    Group testGroup1 = new GroupSave(grouperSession).assignName("test:testGroup1").save();
    
    testGroup1.addMember(SubjectTestHelper.SUBJ0);
    testGroup1.addMember(SubjectTestHelper.SUBJ1);
    
    Group testGroup2 = new GroupSave(grouperSession).assignName("test:testGroup2").save();
  
    testGroup2.addMember(SubjectTestHelper.SUBJ2);
  
    Group testGroup3 = new GroupSave(grouperSession).assignName("test:testGroup3").save();
  
    testGroup3.addMember(SubjectTestHelper.SUBJ3);
  
    Group testGroup4 = new GroupSave(grouperSession).assignName("test:test2:testGroup4").assignCreateParentStemsIfNotExist(true).save();
  
    testGroup4.addMember(SubjectTestHelper.SUBJ4);
  
    // marker
    AttributeDefName provisioningMarkerAttributeDefName = GrouperProvisioningAttributeNames.retrieveAttributeDefNameMarker();
  
    // target name
    AttributeDefName provisioningTargetAttributeDefName = GrouperProvisioningAttributeNames.retrieveAttributeDefNameTarget();
  
    // direct name
    AttributeDefName provisioningDirectAttributeDefName = GrouperProvisioningAttributeNames.retrieveAttributeDefNameDirectAssignment();
  
    // direct name
    AttributeDefName provisioningStemScopeAttributeDefName = GrouperProvisioningAttributeNames.retrieveAttributeDefNameStemScope();
  
    // do provision
    AttributeDefName provisioningDoProvisionAttributeDefName = GrouperProvisioningAttributeNames.retrieveAttributeDefNameDoProvision();
    
    Set<Group> groups = GrouperProvisioningService.findAllGroupsForTarget("testTarget");
    assertEquals(0, GrouperUtil.length(groups));
    
    AttributeAssign testStemAttributeAssign = testStem.getAttributeDelegate().addAttribute(provisioningMarkerAttributeDefName).getAttributeAssign();
    testStemAttributeAssign.getAttributeValueDelegate().assignValue(provisioningDirectAttributeDefName.getName(), "true");
    testStemAttributeAssign.getAttributeValueDelegate().assignValue(provisioningStemScopeAttributeDefName.getName(), "sub");
    testStemAttributeAssign.getAttributeValueDelegate().assignValue(provisioningTargetAttributeDefName.getName(), "testTarget");
    testStemAttributeAssign.getAttributeValueDelegate().assignValue(provisioningDoProvisionAttributeDefName.getName(), "true");
  
    AttributeAssign testGroup2attributeAssign = testGroup2.getAttributeDelegate().addAttribute(provisioningMarkerAttributeDefName).getAttributeAssign();
    testGroup2attributeAssign.getAttributeValueDelegate().assignValue(provisioningDirectAttributeDefName.getName(), "true");
    testGroup2attributeAssign.getAttributeValueDelegate().assignValue(provisioningTargetAttributeDefName.getName(), "testTarget");
    testGroup2attributeAssign.getAttributeValueDelegate().assignValue(provisioningDoProvisionAttributeDefName.getName(), "false");

    //make sure unexpecte events are handled
    new AttributeDefSave(grouperSession).assignName("test:whateverDef").save();
    
    hib3GrouperLoaderLog = runJobs(true, true);
    assertEquals("SUCCESS", hib3GrouperLoaderLog.getStatus());
    
    assertTrue(ProvisioningSampleListener.messageCount + "", ProvisioningSampleListener.messageCount > messageCount + 10);
    
    long maxSequenceNumber = new GcDbAccess().sql("select max(sequence_number) from grouper_change_log_entry").select(long.class);

    long lastProcessedSequenceNumber = new GcDbAccess().sql("select last_sequence_processed from grouper_change_log_consumer where name = 'TEST_TARGET'").select(long.class);

    assertEquals(maxSequenceNumber, lastProcessedSequenceNumber);
    
  }

}
