/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.app.tableSync;

import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.model.Table;
import org.hibernate.type.StringType;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperAPI;
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
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.changeLog.ChangeLogHelper;
import edu.internet2.middleware.grouper.changeLog.ChangeLogTempToEntity;
import edu.internet2.middleware.grouper.changeLog.consumer.PrintChangeLogConsumer;
import edu.internet2.middleware.grouper.ddl.DdlUtilsChangeDatabase;
import edu.internet2.middleware.grouper.ddl.DdlVersionBean;
import edu.internet2.middleware.grouper.ddl.GrouperDdlUtils;
import edu.internet2.middleware.grouper.ddl.GrouperTestDdl;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.hibernate.HibUtils;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.misc.GrouperStartup;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSync;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncGroup;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcTableSync;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcTableSyncOutput;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcTableSyncSubtype;
import edu.internet2.middleware.grouperClient.util.GrouperClientConfig;
import junit.textui.TestRunner;


/**
 *
 */
public class ProvisioningToSyncTest extends GrouperTest {

  public static void main(String[] args) {
    GrouperStartup.startup();
//    TestRunner.run(new TableSyncTest("testTableSyncMetadata"));
//    TestRunner.run(new TableSyncTest("testPersonSyncFull"));
    TestRunner.run(new ProvisioningToSyncTest("testProvisioningAttributesToGroupSyncFull"));
//    TestRunner.run(new TableSyncTest("testPersonSyncFullChangeFlag"));
//    TestRunner.run(new TableSyncTest("testPersonSyncIncrementalPrimaryKey"));
    
//    List<Object[]> results = new GcDbAccess().connectionName("grouper")
//        .sql("select PERSON_ID, HIBERNATE_VERSION_NUMBER, NET_ID, SOME_INT, SOME_FLOAT, SOME_DATE, SOME_TIMESTAMP from testgrouper_sync_subject_from")
//        .selectList(Object[].class);
//    for (Object[] result : results) {
//      System.out.println(GrouperClientUtils.toStringForLog(result));
//    }
    
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

    Table loaderTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, tableName);
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "member_id", 
        Types.VARCHAR, "40", true, true);
 
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "group_id", 
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

  /**
   * add provisioning attributes and see them transition to group sync attribute
   */
  public void testProvisioningAttributesToGroupSyncFull() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
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
    
    GrouperProvisioningJob.runDaemonStandalone();
    
    //Then - Most the children of stem0 should have metadata coming from parent
    GrouperProvisioningAttributeValue grouperProvisioningAttributeValue = GrouperProvisioningService.getProvisioningAttributeValue(testGroup1, "testTarget");
    assertEquals(testStem.getUuid(), grouperProvisioningAttributeValue.getOwnerStemId());
    assertFalse(grouperProvisioningAttributeValue.isDirectAssignment());
    assertEquals(true, grouperProvisioningAttributeValue.isDoProvision());
    
    grouperProvisioningAttributeValue = GrouperProvisioningService.getProvisioningAttributeValue(testGroup2, "testTarget");
    assertNull(grouperProvisioningAttributeValue.getOwnerStemId());
    assertTrue(grouperProvisioningAttributeValue.isDirectAssignment());
    assertEquals(false, grouperProvisioningAttributeValue.isDoProvision());
    
    grouperProvisioningAttributeValue = GrouperProvisioningService.getProvisioningAttributeValue(testGroup3, "testTarget");
    assertEquals(testStem.getUuid(), grouperProvisioningAttributeValue.getOwnerStemId());
    assertFalse(grouperProvisioningAttributeValue.isDirectAssignment());
    assertEquals(true, grouperProvisioningAttributeValue.isDoProvision());
    
    grouperProvisioningAttributeValue = GrouperProvisioningService.getProvisioningAttributeValue(testGroup4, "testTarget");
    assertEquals(testStem.getUuid(), grouperProvisioningAttributeValue.getOwnerStemId());
    assertFalse(grouperProvisioningAttributeValue.isDirectAssignment());
    assertEquals(true, grouperProvisioningAttributeValue.isDoProvision());
    
    groups = GrouperProvisioningService.findAllGroupsForTarget("testTarget");
    assertEquals(3, GrouperUtil.length(groups));

    assertTrue(groups.contains(testGroup1));
    assertTrue(groups.contains(testGroup3));
    assertTrue(groups.contains(testGroup4));
    
    ProvisioningSyncResult provisioningSyncResult = new ProvisioningSyncIntegration().assignTarget("testTarget").fullSync();
    
    Map<String, Group> groupIdToGroup = provisioningSyncResult.getMapGroupIdToGroup();

    assertEquals(3, groupIdToGroup.size());
    assertTrue(groupIdToGroup.containsKey(testGroup1.getId()));
    assertTrue(groupIdToGroup.containsKey(testGroup3.getId()));
    assertTrue(groupIdToGroup.containsKey(testGroup4.getId()));

    Map<String, Group> groupIdToGcGrouperSyncGroup = provisioningSyncResult.getMapGroupIdToGroup();

    assertEquals(3, groupIdToGcGrouperSyncGroup.size());
    assertTrue(groupIdToGcGrouperSyncGroup.containsKey(testGroup1.getId()));
    assertTrue(groupIdToGcGrouperSyncGroup.containsKey(testGroup3.getId()));
    assertTrue(groupIdToGcGrouperSyncGroup.containsKey(testGroup4.getId()));

    GcGrouperSync gcGrouperSync = GcGrouperSync.retrieveOrCreateByProvisionerName("grouper", "testTarget");
    List<GcGrouperSyncGroup> gcGrouperSyncGroups = gcGrouperSync.retrieveAllGroups();
    
    Set<String> uuidsToProvision = new HashSet<String>();
    
    for (GcGrouperSyncGroup gcGrouperSyncGroup : gcGrouperSyncGroups) {
      if (gcGrouperSyncGroup.isProvisionable()) {
        uuidsToProvision.add(gcGrouperSyncGroup.getGroupId());
      }
    }
    
    assertEquals(3, uuidsToProvision.size());
    assertTrue(uuidsToProvision.contains(testGroup1.getId()));
    assertTrue(uuidsToProvision.contains(testGroup3.getId()));
    assertTrue(uuidsToProvision.contains(testGroup4.getId()));

    // ##########  Remove, then add
    testStemAttributeAssign.getAttributeValueDelegate().assignValue(provisioningDoProvisionAttributeDefName.getName(), "false");

    GrouperProvisioningJob.runDaemonStandalone();
    
    //Then - Most the children of stem0 should have metadata coming from parent
    grouperProvisioningAttributeValue = GrouperProvisioningService.getProvisioningAttributeValue(testGroup1, "testTarget");
    assertEquals(testStem.getUuid(), grouperProvisioningAttributeValue.getOwnerStemId());
    assertFalse(grouperProvisioningAttributeValue.isDirectAssignment());
    assertEquals(false, grouperProvisioningAttributeValue.isDoProvision());
    
    grouperProvisioningAttributeValue = GrouperProvisioningService.getProvisioningAttributeValue(testGroup2, "testTarget");
    assertNull(grouperProvisioningAttributeValue.getOwnerStemId());
    assertTrue(grouperProvisioningAttributeValue.isDirectAssignment());
    assertEquals(false, grouperProvisioningAttributeValue.isDoProvision());
    
    grouperProvisioningAttributeValue = GrouperProvisioningService.getProvisioningAttributeValue(testGroup3, "testTarget");
    assertEquals(testStem.getUuid(), grouperProvisioningAttributeValue.getOwnerStemId());
    assertFalse(grouperProvisioningAttributeValue.isDirectAssignment());
    assertEquals(false, grouperProvisioningAttributeValue.isDoProvision());
    
    grouperProvisioningAttributeValue = GrouperProvisioningService.getProvisioningAttributeValue(testGroup4, "testTarget");
    assertEquals(testStem.getUuid(), grouperProvisioningAttributeValue.getOwnerStemId());
    assertFalse(grouperProvisioningAttributeValue.isDirectAssignment());
    assertEquals(false, grouperProvisioningAttributeValue.isDoProvision());
    
    groups = GrouperProvisioningService.findAllGroupsForTarget("testTarget");
    assertEquals(0, GrouperUtil.length(groups));

    provisioningSyncResult = new ProvisioningSyncIntegration().assignTarget("testTarget").fullSync();
    
    groupIdToGroup = provisioningSyncResult.getMapGroupIdToGroup();

    assertEquals(0, groupIdToGroup.size());

    groupIdToGcGrouperSyncGroup = provisioningSyncResult.getMapGroupIdToGroup();

    assertEquals(0, groupIdToGcGrouperSyncGroup.size());

    // ##########  Put back
    testStemAttributeAssign.getAttributeValueDelegate().assignValue(provisioningDoProvisionAttributeDefName.getName(), "true");

    GrouperProvisioningJob.runDaemonStandalone();
    

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer." + JOB_NAME + ".class", 
        "edu.internet2.middleware.grouper.changeLog.consumer.PrintChangeLogConsumer");
    
    //something that will never fire
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer." + JOB_NAME + ".quartzCron", 
        "0 0 5 * * 2000");

//    assertEquals("SUCCESS", hib3GrouploaderLog.getStatus());
//
//    assertEquals(2, PrintChangeLogConsumer.eventsProcessed.size());
//    assertTrue(GrouperUtil.toStringForLog(PrintChangeLogConsumer.eventsProcessed), 
//        PrintChangeLogConsumer.eventsProcessed.contains(JOB_NAME + " add group " + group1.getName() + " and memberships"));
//    assertTrue(GrouperUtil.toStringForLog(PrintChangeLogConsumer.eventsProcessed), 
//        PrintChangeLogConsumer.eventsProcessed.contains(JOB_NAME + " add group " + group2.getName() + " and memberships"));

  }
  
  /**
   * 
   */
  private Hib3GrouperLoaderLog runJobs() {
    
    ChangeLogTempToEntity.convertRecords();
    
    Hib3GrouperLoaderLog hib3GrouploaderLog = new Hib3GrouperLoaderLog();
    hib3GrouploaderLog.setHost(GrouperUtil.hostname());
    hib3GrouploaderLog.setJobName("CHANGE_LOG_consumer_" + JOB_NAME);
    hib3GrouploaderLog.setStatus(GrouperLoaderStatus.RUNNING.name());
    ChangeLogHelper.processRecords(JOB_NAME, hib3GrouploaderLog, new PrintChangeLogConsumer());

    return hib3GrouploaderLog;
  }

  /**
     * 
     */
    public void testPersonSyncFull() {

      GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("grouperClient.syncTable.personSourceTest.databaseFrom", "grouper");
      GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("grouperClient.syncTable.personSourceTest.tableFrom", "testgrouper_sync_subject_from");
      GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("grouperClient.syncTable.personSourceTest.databaseTo", "grouper");
      GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("grouperClient.syncTable.personSourceTest.tableTo", "testgrouper_sync_subject_to");
      GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("grouperClient.syncTable.personSourceTest.columns", "*");
  
      GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("grouperClient.syncTable.personSourceTest.primaryKeyColumns", "person_id");
      
      int countFrom = HibernateSession.bySqlStatic().select(int.class, "select count(*) from testgrouper_sync_subject_from");
      
      assertEquals(0, countFrom);
  
      int countTo = HibernateSession.bySqlStatic().select(int.class, "select count(*) from testgrouper_sync_subject_to");
      
      assertEquals(0, countTo);
  
      List<TestgrouperSyncSubjectFrom> testgrouperSyncSubjectFroms = new ArrayList<TestgrouperSyncSubjectFrom>();
      
      long now = System.currentTimeMillis();
      Calendar date = new GregorianCalendar();
      date.setTimeInMillis(now);
      date.set(Calendar.HOUR_OF_DAY, 0);
      date.set(Calendar.MINUTE, 0);
      date.set(Calendar.MILLISECOND, 0);
      date.set(Calendar.SECOND, 0);
  
      Calendar timestamp = new GregorianCalendar();
      timestamp.setTimeInMillis(now);
      timestamp.set(Calendar.MILLISECOND, 0);
      timestamp.add(Calendar.HOUR_OF_DAY, 1);
      timestamp.add(Calendar.MINUTE, 1);
      
      int recordsSize = 25000;
      
      for (int i=0;i<recordsSize;i++) {
        TestgrouperSyncSubjectFrom testgrouperSyncSubjectFrom = new TestgrouperSyncSubjectFrom();
        testgrouperSyncSubjectFrom.setPersonId(i+"");
        testgrouperSyncSubjectFrom.setNetId("netId_" + i);
        testgrouperSyncSubjectFrom.setSomeInt(1+i);
        
        Calendar calendar = new GregorianCalendar();
        calendar.setTimeInMillis(date.getTimeInMillis());
        calendar.add(Calendar.DAY_OF_YEAR, i);
  
        testgrouperSyncSubjectFrom.setSomeDate(calendar.getTime()); // yyyy/mm/dd
        testgrouperSyncSubjectFrom.setSomeFloat(1.1d + i);
        
        calendar = new GregorianCalendar();
        calendar.setTimeInMillis(timestamp.getTimeInMillis());
        calendar.add(Calendar.DAY_OF_YEAR, i);
        
        testgrouperSyncSubjectFrom.setSomeTimestamp(new Timestamp(calendar.getTimeInMillis()));
        testgrouperSyncSubjectFroms.add(testgrouperSyncSubjectFrom);
        
        if (testgrouperSyncSubjectFroms.size() == 1000) {
          HibernateSession.byObjectStatic().saveBatch(testgrouperSyncSubjectFroms);
          testgrouperSyncSubjectFroms.clear();
        }
        
      }
      if (testgrouperSyncSubjectFroms.size() > 0) {
        HibernateSession.byObjectStatic().saveBatch(testgrouperSyncSubjectFroms);
      }
  
      //lets sync these over
      
      GcTableSync gcTableSync = new GcTableSync();
  
      GcTableSyncOutput gcTableSyncOutput = gcTableSync.sync("personSourceTest", GcTableSyncSubtype.fullSyncFull); 
  
      assertEquals(0, gcTableSyncOutput.getDelete());
      assertEquals(0, gcTableSyncOutput.getUpdate());
      assertEquals(recordsSize, gcTableSyncOutput.getRowsSelectedFrom());
      assertEquals(recordsSize, gcTableSyncOutput.getInsert());
      assertEquals(recordsSize, gcTableSync.getGcGrouperSync().getRecordsCount().intValue());
  
      countTo = HibernateSession.bySqlStatic().select(int.class, "select count(*) from testgrouper_sync_subject_to");
  
      assertEquals(recordsSize, countTo);
  
      //do it again should do nothing
      gcTableSync = new GcTableSync();
      
      gcTableSyncOutput = gcTableSync.sync("personSourceTest", GcTableSyncSubtype.fullSyncFull); 
  
      assertEquals(0, gcTableSyncOutput.getDelete());
      assertEquals(0, gcTableSyncOutput.getUpdate());
      assertEquals(recordsSize, gcTableSyncOutput.getRowsSelectedFrom());
      assertEquals(0, gcTableSyncOutput.getInsert());
      
      TestgrouperSyncSubjectTo testgrouperSyncSubjectTo = HibernateSession.byObjectStatic().load(TestgrouperSyncSubjectTo.class, "0");
      assertEquals("0", testgrouperSyncSubjectTo.getPersonId());
      assertEquals("netId_0", testgrouperSyncSubjectTo.getNetId());
      assertEquals(new Date(date.getTimeInMillis()), testgrouperSyncSubjectTo.getSomeDate());
      assertEquals(1.1d, testgrouperSyncSubjectTo.getSomeFloat());
      assertEquals(new Integer(1), testgrouperSyncSubjectTo.getSomeInt());
      assertEquals(new Timestamp(timestamp.getTimeInMillis()), testgrouperSyncSubjectTo.getSomeTimestamp());
      
      // this will be a delete
      TestgrouperSyncSubjectFrom testgrouperSyncSubjectFrom = HibernateSession.byObjectStatic().load(TestgrouperSyncSubjectFrom.class, "0");
      HibernateSession.byObjectStatic().delete(testgrouperSyncSubjectFrom);
  
      // this will be an insert
      testgrouperSyncSubjectFrom.setPersonId("-1");
      testgrouperSyncSubjectFrom.setHibernateVersionNumber(GrouperAPI.INITIAL_VERSION_NUMBER);
      HibernateSession.byObjectStatic().saveOrUpdate(testgrouperSyncSubjectFrom);
    
      // this will be an update
      testgrouperSyncSubjectFrom = HibernateSession.byObjectStatic().load(TestgrouperSyncSubjectFrom.class, "1");
      testgrouperSyncSubjectFrom.setNetId("55");
      HibernateSession.byObjectStatic().saveOrUpdate(testgrouperSyncSubjectFrom);
  
      gcTableSync = new GcTableSync();
      
      gcTableSyncOutput = gcTableSync.sync("personSourceTest", GcTableSyncSubtype.fullSyncFull); 
          
      assertEquals(1, gcTableSyncOutput.getDelete());
      assertEquals(1, gcTableSyncOutput.getUpdate());
      assertEquals(recordsSize, gcTableSyncOutput.getRowsSelectedFrom());
      assertEquals(1, gcTableSyncOutput.getInsert());
      
      int rows = HibernateSession.bySqlStatic().select(int.class, "select count(*) from testgrouper_sync_subject_to where person_id = ?", 
          HibUtils.listObject(0), HibUtils.listType(StringType.INSTANCE));
      assertEquals(0, rows);
      
      testgrouperSyncSubjectTo = HibernateSession.byObjectStatic().load(TestgrouperSyncSubjectTo.class, "-1");
      assertNotNull(testgrouperSyncSubjectTo);
      
      assertEquals("-1", testgrouperSyncSubjectTo.getPersonId());
      assertEquals("netId_0", testgrouperSyncSubjectTo.getNetId());
      assertEquals(new Date(date.getTimeInMillis()), testgrouperSyncSubjectTo.getSomeDate());
      assertEquals(1.1d, testgrouperSyncSubjectTo.getSomeFloat());
      assertEquals(new Integer(1), testgrouperSyncSubjectTo.getSomeInt());
      assertEquals(new Timestamp(timestamp.getTimeInMillis()), testgrouperSyncSubjectTo.getSomeTimestamp());
    
      testgrouperSyncSubjectTo = HibernateSession.byObjectStatic().load(TestgrouperSyncSubjectTo.class, "1");
      assertNotNull(testgrouperSyncSubjectTo);
      assertEquals("55", testgrouperSyncSubjectTo.getNetId());
      
    }

  /**
   * @param ddlVersionBean
   * @param database
   * @param viewName
   */
  public void createView(DdlVersionBean ddlVersionBean, Database database, String viewName) {
  
    GrouperDdlUtils.ddlutilsCreateOrReplaceView(ddlVersionBean, viewName, 
        "test membership view",
        GrouperUtil.toSet("MEMBER_ID", "GROUP_ID", "LIST_NAME", "SUBJECT_ID", "SUBJECT_SOURCE", "GROUP_NAME"),
        GrouperUtil.toSet("MEMBER_ID: uuid of the member",
            "GROUP_ID: uuid of the group",
            "LIST_NAME: name of the list, e.g. members",
            "SUBJECT_ID: of the member of the group", 
            "SUBJECT_SOURCE: of the member of the group", 
            "GROUP_NAME: system name of the group"
            ),
            "select ms.member_id, gg.id as group_id, gf.name as list_name, gg.name as group_name, gm.subject_id, gm.subject_source " + 
            "from grouper_memberships ms, grouper_group_set gs, grouper_groups gg, grouper_sync gsync, grouper_sync_group gsg, grouper_members gm, grouper_fields gf " + 
            "where ms.owner_id = gs.member_id and ms.field_id= gs.member_field_id and gs.owner_group_id = gg.id and gm.id = ms.member_id and gf.id = gs.field_id " + 
            "and gsync.id = gsg.grouper_sync_id and gsg.group_id = gg.id and gm.subject_source = 'jdbc' and ms.enabled = 'T' and gsg.provisionable = 'T'" + 
            "");
    
  }

}
