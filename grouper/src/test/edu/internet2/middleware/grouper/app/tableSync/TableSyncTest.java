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
import java.util.List;

import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.model.Table;
import org.hibernate.type.IntegerType;
import org.hibernate.type.StringType;

import edu.internet2.middleware.grouper.GrouperAPI;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.app.loader.GrouperLoader;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.ddl.DdlUtilsChangeDatabase;
import edu.internet2.middleware.grouper.ddl.DdlVersionBean;
import edu.internet2.middleware.grouper.ddl.GrouperDdlUtils;
import edu.internet2.middleware.grouper.ddl.GrouperTestDdl;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.hibernate.HibUtils;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSync;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncGroup;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncJob;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncLog;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncMember;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncMembership;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcTableSync;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcTableSyncColumnMetadata;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcTableSyncColumnMetadata.ColumnType;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcTableSyncOutput;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcTableSyncSubtype;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcTableSyncTableBean;
import edu.internet2.middleware.grouperClient.util.GrouperClientConfig;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import junit.textui.TestRunner;


/**
 *
 */
public class TableSyncTest extends GrouperTest {

  public static void main(String[] args) {
//    GrouperStartup.startup();
//    
//    GrouperSession grouperSession = GrouperSession.startRootSession();
//    
//    GrouperLoader.runOnceByJobName(grouperSession, "OTHER_JOB_person_source_test_full");
    
    
//    TestRunner.run(new TableSyncTest("testTableSyncMetadata"));
    TestRunner.run(new TableSyncTest("testPersonSyncIncrementalPrimaryKey"));
    
//    BigDecimal a = new BigDecimal(1);
//    BigDecimal b = new BigDecimal(1.000);
//    
//    System.out.println(a.equals(b));
//    
//    System.out.println(a.hashCode() + ", " + b.hashCode());
//
//    a = new BigDecimal(1.5);
//    b = new BigDecimal((double)(3D/2));
//    
//    System.out.println(a.equals(b));
//    System.out.println(a.hashCode() + ", " + b.hashCode());
    
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
  public TableSyncTest() {
    super();
    
  }

  /**
   * grouper session
   */
  private GrouperSession grouperSession = null;

  /**
   * 
   */
  public void testTableSyncMetadata() {
    
    boolean foundName = false;
    
    GcTableSyncTableBean gcTableSyncTableBean = new GcTableSyncTableBean();
    gcTableSyncTableBean.configureMetadata("grouper", "grouper_groups");
    
    for (GcTableSyncColumnMetadata gcTableSyncColumnMetadata : gcTableSyncTableBean.getTableMetadata().getColumnMetadata()) {
      if (GrouperClientUtils.equalsIgnoreCase(gcTableSyncColumnMetadata.getColumnName(), "name")) {
        assertEquals(ColumnType.STRING, gcTableSyncColumnMetadata.getColumnType());
        //System.out.println(gcTableSyncColumnMetadata.getColumnName());
        foundName = true;
      }
    }
    assertTrue(foundName);
  }
  
  /**
   * @param name
   */
  public TableSyncTest(String name) {
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
  
      HibernateSession.byHqlStatic().createQuery("delete from TestgrouperSyncSubjectFrom").executeUpdate();
      HibernateSession.byHqlStatic().createQuery("delete from TestgrouperSyncSubjectTo").executeUpdate();
      HibernateSession.byHqlStatic().createQuery("delete from TestgrouperSyncChangeLog").executeUpdate();
      
      GcGrouperSyncLog.reset();
      GcGrouperSyncMembership.reset();
      GcGrouperSyncGroup.reset();
      GcGrouperSyncMember.reset();
      GcGrouperSyncJob.reset();
      GcGrouperSync.reset();
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
    
    dropTableSyncTables();
    GrouperSession.stopQuietly(this.grouperSession);

  }

  /**
   * @param ddlVersionBean
   * @param database
   * @param tableName
   */
  public void createTable(DdlVersionBean ddlVersionBean, Database database, String tableName, boolean useDecimal) {

    Table loaderTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, tableName);
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "person_id", 
        useDecimal ? Types.NUMERIC : Types.BIGINT, null, true, true);
 
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "hibernate_version_number", 
        Types.INTEGER, "10", false, true);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "net_id", 
        Types.VARCHAR, "30", false, true);
 
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "some_int", 
        Types.INTEGER, "10", false, false);
   
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "some_float", 
        Types.DOUBLE, null, false, false);
   
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "some_date", 
        Types.DATE, null, false, false);
   
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "some_timestamp", 
        Types.TIMESTAMP, null, false, false);
   
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "change_flag", 
        Types.INTEGER, null, false, false);
   
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "the_group", 
        Types.VARCHAR, "20", false, false);
   
    GrouperDdlUtils.ddlutilsTableComment(ddlVersionBean, 
        tableName, tableName);
    
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "person_id", "person_id");
    
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
  
        createTable(ddlVersionBean, database, "testgrouper_sync_subject_from", true);

        createTable(ddlVersionBean, database, "testgrouper_sync_subject_to", false);

        createTableChangeLog(ddlVersionBean, database);
      }
      
    });
  }

  /**
   * 
   */
  public void dropTableSyncTables() {
    
    dropTableSyncTable("testgrouper_sync_subject_from");
    dropTableSyncTable("testgrouper_sync_subject_to");
    dropTableSyncTable("testgrouper_sync_change_log");
    
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
  public void testPersonSyncFullChangeFlag() {
    
    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("grouperClient.syncTable.personSourceTest.databaseFrom", "grouper");
    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("grouperClient.syncTable.personSourceTest.tableFrom", "testgrouper_sync_subject_from");
    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("grouperClient.syncTable.personSourceTest.databaseTo", "grouper");
    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("grouperClient.syncTable.personSourceTest.tableTo", "testgrouper_sync_subject_to");
    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("grouperClient.syncTable.personSourceTest.columns", "*");
    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("grouperClient.syncTable.personSourceTest.changeFlagColumn", "change_flag");
    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("grouperClient.syncTable.personSourceTest.primaryKeyColumns", "person_id");
    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("grouperClient.syncTable.personSourceTest.batchSize", "5");
    
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
    
    int recordsSize = 250;
    
    for (int i=0;i<recordsSize;i++) {
      TestgrouperSyncSubjectFrom testgrouperSyncSubjectFrom = new TestgrouperSyncSubjectFrom();
      testgrouperSyncSubjectFrom.setPersonId(i);
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
    GcTableSyncOutput gcTableSyncOutput = gcTableSync.sync("personSourceTest", GcTableSyncSubtype.fullSyncChangeFlag); 

    assertEquals(0, gcTableSyncOutput.getDelete());
    assertEquals(0, gcTableSyncOutput.getUpdate());
    assertEquals(recordsSize*2, gcTableSyncOutput.getRowsSelectedFrom());
    assertEquals(recordsSize, gcTableSyncOutput.getInsert());
    assertEquals(recordsSize, gcTableSync.getGcGrouperSync().getRecordsCount().intValue());

    countTo = HibernateSession.bySqlStatic().select(int.class, "select count(*) from testgrouper_sync_subject_to");

    assertEquals(recordsSize, countTo);

    //do it again should do nothing
    gcTableSync = new GcTableSync();
    gcTableSyncOutput = gcTableSync.sync("personSourceTest", GcTableSyncSubtype.fullSyncChangeFlag); 

    assertEquals(0, gcTableSyncOutput.getDelete());
    assertEquals(0, gcTableSyncOutput.getUpdate());
    assertEquals(recordsSize, gcTableSyncOutput.getRowsSelectedFrom());
    assertEquals(0, gcTableSyncOutput.getInsert());
    
    TestgrouperSyncSubjectTo testgrouperSyncSubjectTo = HibernateSession.byObjectStatic().load(TestgrouperSyncSubjectTo.class, 0L);
    assertEquals(new Long(0), testgrouperSyncSubjectTo.getPersonId());
    assertEquals("netId_0", testgrouperSyncSubjectTo.getNetId());
    assertEquals(new Date(date.getTimeInMillis()), testgrouperSyncSubjectTo.getSomeDate());
    assertEquals(1.1d, testgrouperSyncSubjectTo.getSomeFloat());
    assertEquals(new Integer(1), testgrouperSyncSubjectTo.getSomeInt());
    assertEquals(new Timestamp(timestamp.getTimeInMillis()), testgrouperSyncSubjectTo.getSomeTimestamp());
    
    // this will be a delete
    TestgrouperSyncSubjectFrom testgrouperSyncSubjectFrom = HibernateSession.byObjectStatic().load(TestgrouperSyncSubjectFrom.class, 0);
    HibernateSession.byObjectStatic().delete(testgrouperSyncSubjectFrom);

    // this will be an insert
    testgrouperSyncSubjectFrom.setPersonId(-1);
    testgrouperSyncSubjectFrom.setHibernateVersionNumber(GrouperAPI.INITIAL_VERSION_NUMBER);
    HibernateSession.byObjectStatic().saveOrUpdate(testgrouperSyncSubjectFrom);
  
    // this will be an update
    testgrouperSyncSubjectFrom = HibernateSession.byObjectStatic().load(TestgrouperSyncSubjectFrom.class, 1);
    testgrouperSyncSubjectFrom.setNetId("55");
    HibernateSession.byObjectStatic().saveOrUpdate(testgrouperSyncSubjectFrom);

    gcTableSync = new GcTableSync();
    gcTableSyncOutput = gcTableSync.sync("personSourceTest", GcTableSyncSubtype.fullSyncChangeFlag); 
        
    assertEquals(1, gcTableSyncOutput.getDelete());
    assertEquals(1, gcTableSyncOutput.getUpdate());
    assertEquals(recordsSize+2, gcTableSyncOutput.getRowsSelectedFrom());
    assertEquals(1, gcTableSyncOutput.getInsert());
    
    int rows = HibernateSession.bySqlStatic().select(int.class, "select count(*) from testgrouper_sync_subject_to where person_id = ?", 
        HibUtils.listObject(0), HibUtils.listType(StringType.INSTANCE));
    assertEquals(0, rows);

    testgrouperSyncSubjectTo = HibernateSession.byObjectStatic().load(TestgrouperSyncSubjectTo.class, -1L);
    assertNotNull(testgrouperSyncSubjectTo);
    
    assertEquals(new Long(-1), testgrouperSyncSubjectTo.getPersonId());
    assertEquals("netId_0", testgrouperSyncSubjectTo.getNetId());
    assertEquals(new Date(date.getTimeInMillis()), testgrouperSyncSubjectTo.getSomeDate());
    assertEquals(1.1d, testgrouperSyncSubjectTo.getSomeFloat());
    assertEquals(new Integer(1), testgrouperSyncSubjectTo.getSomeInt());
    assertEquals(new Timestamp(timestamp.getTimeInMillis()), testgrouperSyncSubjectTo.getSomeTimestamp());
  
    testgrouperSyncSubjectTo = HibernateSession.byObjectStatic().load(TestgrouperSyncSubjectTo.class, 1L);
    assertNotNull(testgrouperSyncSubjectTo);
    assertEquals("55", testgrouperSyncSubjectTo.getNetId());
    
  }

  /**
     * 
     */
    public void testPersonSyncIncrementalAllColumns() {
      
      GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("grouperClient.syncTable.personSourceTest.databaseFrom", "grouper");
      GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("grouperClient.syncTable.personSourceTest.tableFrom", "testgrouper_sync_subject_from");
      GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("grouperClient.syncTable.personSourceTest.databaseTo", "grouper");
      GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("grouperClient.syncTable.personSourceTest.tableTo", "testgrouper_sync_subject_to");
      GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("grouperClient.syncTable.personSourceTest.columns", "*");
      
      // https://stackoverflow.com/questions/40841078/how-to-get-primary-keys-for-all-tables-in-jdbc
      GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("grouperClient.syncTable.personSourceTest.primaryKeyColumns", "person_id");
      GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("grouperClient.syncTable.personSourceTest.incrementalAllColumnsColumn", "some_timestamp");
      
      int countFrom = HibernateSession.bySqlStatic().select(int.class, "select count(*) from testgrouper_sync_subject_from");
      
      assertEquals(0, countFrom);
  
      int countTo = HibernateSession.bySqlStatic().select(int.class, "select count(*) from testgrouper_sync_subject_to");
      
      assertEquals(0, countTo);
  
      GrouperUtil.sleep(100);
      
      List<TestgrouperSyncSubjectFrom> testgrouperSyncSubjectFroms = new ArrayList<TestgrouperSyncSubjectFrom>();
      
      long now = System.currentTimeMillis();
      Calendar date = new GregorianCalendar();
      date.setTimeInMillis(now);
      date.set(Calendar.YEAR, 2019);
      date.set(Calendar.HOUR_OF_DAY, 0);
      date.set(Calendar.MINUTE, 0);
      date.set(Calendar.MILLISECOND, 0);
      date.set(Calendar.SECOND, 0);
  
      Calendar timestamp = new GregorianCalendar();
      timestamp.setTimeInMillis(now);
      timestamp.set(Calendar.MILLISECOND, 0);
      timestamp.add(Calendar.HOUR_OF_DAY, 0);
      timestamp.add(Calendar.MINUTE, 0);
      timestamp.add(Calendar.SECOND, 0);
      
      int recordsSize = 25000;
      
      for (int i=0;i<recordsSize;i++) {
        TestgrouperSyncSubjectFrom testgrouperSyncSubjectFrom = new TestgrouperSyncSubjectFrom();
        testgrouperSyncSubjectFrom.setPersonId(i);
        testgrouperSyncSubjectFrom.setNetId("netId_" + i);
        testgrouperSyncSubjectFrom.setSomeInt(1+i);
        
        Calendar calendar = new GregorianCalendar();
        calendar.setTimeInMillis(date.getTimeInMillis());
        calendar.add(Calendar.MILLISECOND, i);
  
        testgrouperSyncSubjectFrom.setSomeDate(calendar.getTime()); // yyyy/mm/dd
        testgrouperSyncSubjectFrom.setSomeFloat(1.1d + i);
        
        testgrouperSyncSubjectFrom.setSomeTimestamp(new Timestamp(System.currentTimeMillis()));
        testgrouperSyncSubjectFroms.add(testgrouperSyncSubjectFrom);
        
        if (testgrouperSyncSubjectFroms.size() == 1000) {
          HibernateSession.byObjectStatic().saveBatch(testgrouperSyncSubjectFroms);
          testgrouperSyncSubjectFroms.clear();
        }
        
      }
      if (testgrouperSyncSubjectFroms.size() > 0) {
        HibernateSession.byObjectStatic().saveBatch(testgrouperSyncSubjectFroms);
      }
      GrouperUtil.sleep(100);
  
      // ######################
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
      
      GrouperUtil.sleep(100);

      // do some inserts and some updates and some deletes
      int numberOfInserts = 4;
      int numberOfDeletes = 2;
      int numberOfUpdates = 3;
      for (int i=recordsSize;i<recordsSize + numberOfInserts;i++) {
        TestgrouperSyncSubjectFrom testgrouperSyncSubjectFrom = new TestgrouperSyncSubjectFrom();
        testgrouperSyncSubjectFrom.setPersonId(i);
        testgrouperSyncSubjectFrom.setNetId("newnetId_" + i);
        testgrouperSyncSubjectFrom.setSomeInt(1+i);
        
        Calendar calendar = new GregorianCalendar();
        calendar.setTimeInMillis(date.getTimeInMillis());
        calendar.add(Calendar.DAY_OF_YEAR, i);
  
        testgrouperSyncSubjectFrom.setSomeDate(calendar.getTime()); // yyyy/mm/dd
        testgrouperSyncSubjectFrom.setSomeFloat(1.1d + i);
        
        calendar = new GregorianCalendar();
        calendar.setTimeInMillis(timestamp.getTimeInMillis());
        calendar.add(Calendar.DAY_OF_YEAR, i);
        
        testgrouperSyncSubjectFrom.setSomeTimestamp(new Timestamp(System.currentTimeMillis()));
        HibernateSession.byObjectStatic().save(testgrouperSyncSubjectFrom);
      }
      for (int i=0;i<numberOfUpdates;i++) {
        TestgrouperSyncSubjectFrom testgrouperSyncSubjectFrom = HibernateSession.byObjectStatic().load(TestgrouperSyncSubjectFrom.class, i);
        testgrouperSyncSubjectFrom.setNetId("newnetId_" + i);
        testgrouperSyncSubjectFrom.setSomeTimestamp(new Timestamp(System.currentTimeMillis()));
        HibernateSession.byObjectStatic().update(testgrouperSyncSubjectFrom);
      }
      for (int i=numberOfUpdates;i<numberOfUpdates + numberOfDeletes;i++) {
        TestgrouperSyncSubjectFrom testgrouperSyncSubjectFrom = HibernateSession.byObjectStatic().load(TestgrouperSyncSubjectFrom.class, i);
        HibernateSession.byObjectStatic().delete(testgrouperSyncSubjectFrom);
      }

      GrouperUtil.sleep(100);

      // ######################
      //do it again should do some things
      gcTableSync = new GcTableSync();
      gcTableSyncOutput = gcTableSync.sync("personSourceTest", GcTableSyncSubtype.incrementalAllColumns); 

      // these dont happen on incremental all columns
      assertEquals(0, gcTableSyncOutput.getDelete());
      assertEquals(numberOfUpdates, gcTableSyncOutput.getUpdate());
      assertEquals(numberOfUpdates + numberOfInserts, gcTableSyncOutput.getRowsSelectedFrom());
      assertEquals(numberOfInserts, gcTableSyncOutput.getInsert());
      
      for (int i=0;i<numberOfUpdates;i++) {
        TestgrouperSyncSubjectTo testgrouperSyncSubjectTo = HibernateSession.byObjectStatic().load(TestgrouperSyncSubjectTo.class, new Long(i));
        assertEquals("newnetId_" + i, testgrouperSyncSubjectTo.getNetId());
      }
      for (int i=recordsSize;i<recordsSize + numberOfInserts;i++) {
        TestgrouperSyncSubjectTo testgrouperSyncSubjectTo = HibernateSession.byObjectStatic().load(TestgrouperSyncSubjectTo.class, new Long(i));
        assertEquals("newnetId_" + i, testgrouperSyncSubjectTo.getNetId());
      }
      // these are still there until full sync
      for (int i=numberOfUpdates;i<numberOfUpdates + numberOfDeletes;i++) {
        TestgrouperSyncSubjectTo testgrouperSyncSubjectTo = HibernateSession.byObjectStatic().load(TestgrouperSyncSubjectTo.class, new Long(i));
        assertEquals("netId_" + i, testgrouperSyncSubjectTo.getNetId());
        
      }
      
      GrouperUtil.sleep(100);

      
      // ######################
      // do a full and see deletes work
      gcTableSync = new GcTableSync();      
      gcTableSyncOutput = gcTableSync.sync("personSourceTest", GcTableSyncSubtype.fullSyncFull); 
          
      assertEquals(numberOfDeletes, gcTableSyncOutput.getDelete());
      assertEquals(0, gcTableSyncOutput.getUpdate());
      assertEquals(0, gcTableSyncOutput.getInsert());
      
      for (int i=numberOfUpdates;i<numberOfUpdates + numberOfDeletes;i++) {
        int rows = HibernateSession.bySqlStatic().select(int.class, "select count(*) from testgrouper_sync_subject_to where person_id = ?", 
            HibUtils.listObject(i), HibUtils.listType(StringType.INSTANCE));
        assertEquals(0, rows);
      }
     
      GrouperUtil.sleep(100);

      // ######################
      // incremental should do nothing
      gcTableSync = new GcTableSync();
      gcTableSyncOutput = gcTableSync.sync("personSourceTest", GcTableSyncSubtype.incrementalAllColumns); 

      // these dont happen on incremental all columns
      assertEquals(0, gcTableSyncOutput.getDelete());
      assertEquals(0, gcTableSyncOutput.getUpdate());
      assertEquals(0, gcTableSyncOutput.getRowsSelectedFrom());
      assertEquals(0, gcTableSyncOutput.getInsert());

      GrouperUtil.sleep(100);

      // ######################
      // full should do nothing
      gcTableSync = new GcTableSync();
      gcTableSyncOutput = gcTableSync.sync("personSourceTest", GcTableSyncSubtype.fullSyncFull); 

      // these dont happen on incremental all columns
      assertEquals(0, gcTableSyncOutput.getDelete());
      assertEquals(0, gcTableSyncOutput.getUpdate());
      assertEquals(recordsSize + numberOfInserts - numberOfDeletes, gcTableSyncOutput.getRowsSelectedFrom());
      assertEquals(0, gcTableSyncOutput.getInsert());

      GrouperUtil.sleep(100);

      // ######################
      // make changes, do a full and then an incremental, shouldnt do anything
      GrouperUtil.sleep(100);
      
      int numberOfNewInserts = 5;
      int numberOfNewDeletes = 6;
      int numberOfNewUpdates = 7;
      for (int i=recordsSize + numberOfInserts;i<recordsSize + numberOfInserts + numberOfNewInserts;i++) {
        TestgrouperSyncSubjectFrom testgrouperSyncSubjectFrom = new TestgrouperSyncSubjectFrom();
        testgrouperSyncSubjectFrom.setPersonId(i);
        testgrouperSyncSubjectFrom.setNetId("newnetId_" + i);
        testgrouperSyncSubjectFrom.setSomeInt(1+i);
        
        Calendar calendar = new GregorianCalendar();
        calendar.setTimeInMillis(date.getTimeInMillis());
        calendar.add(Calendar.DAY_OF_YEAR, i);
  
        testgrouperSyncSubjectFrom.setSomeDate(calendar.getTime()); // yyyy/mm/dd
        testgrouperSyncSubjectFrom.setSomeFloat(1.1d + i);
        
        calendar = new GregorianCalendar();
        calendar.setTimeInMillis(timestamp.getTimeInMillis());
        calendar.add(Calendar.DAY_OF_YEAR, i);
        
        testgrouperSyncSubjectFrom.setSomeTimestamp(new Timestamp(System.currentTimeMillis()));
        HibernateSession.byObjectStatic().save(testgrouperSyncSubjectFrom);
      }
      for (int i=numberOfUpdates + numberOfDeletes;i<numberOfUpdates + numberOfDeletes+ numberOfNewUpdates;i++) {
        TestgrouperSyncSubjectFrom testgrouperSyncSubjectFrom = HibernateSession.byObjectStatic().load(TestgrouperSyncSubjectFrom.class, i);
        testgrouperSyncSubjectFrom.setNetId("newnetId_" + i);
        testgrouperSyncSubjectFrom.setSomeTimestamp(new Timestamp(System.currentTimeMillis()));
        HibernateSession.byObjectStatic().update(testgrouperSyncSubjectFrom);
      }
      for (int i=numberOfUpdates + numberOfDeletes + numberOfNewUpdates;i<numberOfUpdates + numberOfDeletes + numberOfNewUpdates + numberOfNewDeletes;i++) {
        TestgrouperSyncSubjectFrom testgrouperSyncSubjectFrom = HibernateSession.byObjectStatic().load(TestgrouperSyncSubjectFrom.class, i);
        HibernateSession.byObjectStatic().delete(testgrouperSyncSubjectFrom);
      }
      GrouperUtil.sleep(100);

      gcTableSync = new GcTableSync();
      gcTableSyncOutput = gcTableSync.sync("personSourceTest", GcTableSyncSubtype.fullSyncFull); 

      // these dont happen on incremental all columns
      assertEquals(numberOfNewDeletes, gcTableSyncOutput.getDelete());
      assertEquals(numberOfNewUpdates, gcTableSyncOutput.getUpdate());
      assertEquals(recordsSize + numberOfInserts + numberOfNewInserts - (numberOfDeletes + numberOfNewDeletes), gcTableSyncOutput.getRowsSelectedFrom());
      assertEquals(numberOfNewInserts, gcTableSyncOutput.getInsert());
      
      GrouperUtil.sleep(100);

      {
        int i=numberOfUpdates + numberOfDeletes + numberOfNewUpdates + numberOfNewDeletes;
        TestgrouperSyncSubjectFrom testgrouperSyncSubjectFrom = HibernateSession.byObjectStatic().load(TestgrouperSyncSubjectFrom.class, i);
        testgrouperSyncSubjectFrom.setNetId("newnetId_" + i);
        testgrouperSyncSubjectFrom.setSomeTimestamp(new Timestamp(System.currentTimeMillis()));
        HibernateSession.byObjectStatic().update(testgrouperSyncSubjectFrom);
      }

      GrouperUtil.sleep(100);

      // ######################
      // incremental should fix this one record since full just fixed everything
      gcTableSync = new GcTableSync();
      gcTableSyncOutput = gcTableSync.sync("personSourceTest", GcTableSyncSubtype.incrementalAllColumns); 

      // these dont happen on incremental all columns
      assertEquals(0, gcTableSyncOutput.getDelete());
      assertEquals(1, gcTableSyncOutput.getUpdate());
      assertEquals(1, gcTableSyncOutput.getRowsSelectedFrom());
      assertEquals(0, gcTableSyncOutput.getInsert());


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
      
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.person_source_test_full.class", "edu.internet2.middleware.grouper.app.tableSync.TableSyncOtherJob");
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.person_source_test_full.quartzCron", "0 0 2 * * ?");
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.person_source_test_full.grouperClientTableSyncConfigKey", "personSourceTest");
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.person_source_test_full.syncType", "fullSyncFull");
      
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
        testgrouperSyncSubjectFrom.setPersonId(i);
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
      
      TestgrouperSyncSubjectTo testgrouperSyncSubjectTo = HibernateSession.byObjectStatic().load(TestgrouperSyncSubjectTo.class, 0L);
      assertEquals(new Long(0), testgrouperSyncSubjectTo.getPersonId());
      assertEquals("netId_0", testgrouperSyncSubjectTo.getNetId());
      assertEquals(new Date(date.getTimeInMillis()), testgrouperSyncSubjectTo.getSomeDate());
      assertEquals(1.1d, testgrouperSyncSubjectTo.getSomeFloat());
      assertEquals(new Integer(1), testgrouperSyncSubjectTo.getSomeInt());
      assertEquals(new Timestamp(timestamp.getTimeInMillis()), testgrouperSyncSubjectTo.getSomeTimestamp());
      
      // this will be a delete
      TestgrouperSyncSubjectFrom testgrouperSyncSubjectFrom = HibernateSession.byObjectStatic().load(TestgrouperSyncSubjectFrom.class, 0);
      HibernateSession.byObjectStatic().delete(testgrouperSyncSubjectFrom);
  
      // this will be an insert
      testgrouperSyncSubjectFrom.setPersonId(-1);
      testgrouperSyncSubjectFrom.setHibernateVersionNumber(GrouperAPI.INITIAL_VERSION_NUMBER);
      HibernateSession.byObjectStatic().saveOrUpdate(testgrouperSyncSubjectFrom);
    
      // this will be an update
      testgrouperSyncSubjectFrom = HibernateSession.byObjectStatic().load(TestgrouperSyncSubjectFrom.class, 1);
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
      
      testgrouperSyncSubjectTo = HibernateSession.byObjectStatic().load(TestgrouperSyncSubjectTo.class, -1l);
      assertNotNull(testgrouperSyncSubjectTo);
      
      assertEquals(new Long(-1), testgrouperSyncSubjectTo.getPersonId());
      assertEquals("netId_0", testgrouperSyncSubjectTo.getNetId());
      assertEquals(new Date(date.getTimeInMillis()), testgrouperSyncSubjectTo.getSomeDate());
      assertEquals(1.1d, testgrouperSyncSubjectTo.getSomeFloat());
      assertEquals(new Integer(1), testgrouperSyncSubjectTo.getSomeInt());
      assertEquals(new Timestamp(timestamp.getTimeInMillis()), testgrouperSyncSubjectTo.getSomeTimestamp());
    
      testgrouperSyncSubjectTo = HibernateSession.byObjectStatic().load(TestgrouperSyncSubjectTo.class, 1l);
      assertNotNull(testgrouperSyncSubjectTo);
      assertEquals("55", testgrouperSyncSubjectTo.getNetId());
      
      GrouperLoader.runOnceByJobName(this.grouperSession, "OTHER_JOB_person_source_test_full");


    }

  /**
     * 
     */
    public void testPersonSyncFullGroupings() {
      
      GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("grouperClient.syncTable.personSourceTest.databaseFrom", "grouper");
      GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("grouperClient.syncTable.personSourceTest.tableFrom", "testgrouper_sync_subject_from");
      GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("grouperClient.syncTable.personSourceTest.databaseTo", "grouper");
      GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("grouperClient.syncTable.personSourceTest.tableTo", "testgrouper_sync_subject_to");
      GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("grouperClient.syncTable.personSourceTest.columns", "*");
      GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("grouperClient.syncTable.personSourceTest.changeFlagColumn", "change_flag");
      GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("grouperClient.syncTable.personSourceTest.primaryKeyColumns", "person_id");
      GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("grouperClient.syncTable.personSourceTest.groupingColumn", "the_group");
      GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("grouperClient.syncTable.personSourceTest.groupingSize", "5");
      
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
        testgrouperSyncSubjectFrom.setPersonId(i);
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
        
        testgrouperSyncSubjectFrom.setTheGroup("group_" + ((int)((i+1)/10)));
        
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
      GcTableSyncOutput gcTableSyncOutput = gcTableSync.sync("personSourceTest", GcTableSyncSubtype.fullSyncGroups); 
  
      assertEquals(0, gcTableSyncOutput.getDelete());
      assertEquals(0, gcTableSyncOutput.getUpdate());
      assertTrue(gcTableSyncOutput.getRowsSelectedFrom() > recordsSize);
      assertEquals(recordsSize, gcTableSyncOutput.getInsert());
      assertEquals(recordsSize, gcTableSync.getGcGrouperSync().getRecordsCount().intValue());
  
      countTo = HibernateSession.bySqlStatic().select(int.class, "select count(*) from testgrouper_sync_subject_to");
  
      assertEquals(recordsSize, countTo);
  
      //do it again should do nothing
      gcTableSync = new GcTableSync();      
      gcTableSyncOutput = gcTableSync.sync("personSourceTest", GcTableSyncSubtype.fullSyncGroups); 
  
      assertEquals(0, gcTableSyncOutput.getDelete());
      assertEquals(0, gcTableSyncOutput.getUpdate());
      assertTrue(gcTableSyncOutput.getRowsSelectedFrom() > recordsSize);
      assertEquals(0, gcTableSyncOutput.getInsert());
      
      TestgrouperSyncSubjectTo testgrouperSyncSubjectTo = HibernateSession.byObjectStatic().load(TestgrouperSyncSubjectTo.class, 0l);
      assertEquals(new Long(0), testgrouperSyncSubjectTo.getPersonId());
      assertEquals("netId_0", testgrouperSyncSubjectTo.getNetId());
      assertEquals(new Date(date.getTimeInMillis()), testgrouperSyncSubjectTo.getSomeDate());
      assertEquals(1.1d, testgrouperSyncSubjectTo.getSomeFloat());
      assertEquals(new Integer(1), testgrouperSyncSubjectTo.getSomeInt());
      assertEquals(new Timestamp(timestamp.getTimeInMillis()), testgrouperSyncSubjectTo.getSomeTimestamp());
      
      // this will be a delete
      TestgrouperSyncSubjectFrom testgrouperSyncSubjectFrom = HibernateSession.byObjectStatic().load(TestgrouperSyncSubjectFrom.class, 0);
      HibernateSession.byObjectStatic().delete(testgrouperSyncSubjectFrom);
  
      // this will be an insert
      testgrouperSyncSubjectFrom.setPersonId(-1);
      testgrouperSyncSubjectFrom.setHibernateVersionNumber(GrouperAPI.INITIAL_VERSION_NUMBER);
      HibernateSession.byObjectStatic().saveOrUpdate(testgrouperSyncSubjectFrom);
    
      // this will be an update
      testgrouperSyncSubjectFrom = HibernateSession.byObjectStatic().load(TestgrouperSyncSubjectFrom.class, 1);
      testgrouperSyncSubjectFrom.setNetId("55");
      HibernateSession.byObjectStatic().saveOrUpdate(testgrouperSyncSubjectFrom);
  
      gcTableSync = new GcTableSync();     
      gcTableSyncOutput = gcTableSync.sync("personSourceTest", GcTableSyncSubtype.fullSyncGroups); 
          
      assertEquals(1, gcTableSyncOutput.getDelete());
      assertEquals(1, gcTableSyncOutput.getUpdate());
      assertTrue(gcTableSyncOutput.getRowsSelectedFrom() > recordsSize);
      assertEquals(1, gcTableSyncOutput.getInsert());
      
      int rows = HibernateSession.bySqlStatic().select(int.class, "select count(*) from testgrouper_sync_subject_to where person_id = ?", 
          HibUtils.listObject(0), HibUtils.listType(StringType.INSTANCE));
      assertEquals(0, rows);
  
      testgrouperSyncSubjectTo = HibernateSession.byObjectStatic().load(TestgrouperSyncSubjectTo.class, -1L);
      assertNotNull(testgrouperSyncSubjectTo);
      
      assertEquals(new Long(-1), testgrouperSyncSubjectTo.getPersonId());
      assertEquals("netId_0", testgrouperSyncSubjectTo.getNetId());
      assertEquals(new Date(date.getTimeInMillis()), testgrouperSyncSubjectTo.getSomeDate());
      assertEquals(1.1d, testgrouperSyncSubjectTo.getSomeFloat());
      assertEquals(new Integer(1), testgrouperSyncSubjectTo.getSomeInt());
      assertEquals(new Timestamp(timestamp.getTimeInMillis()), testgrouperSyncSubjectTo.getSomeTimestamp());
    
      testgrouperSyncSubjectTo = HibernateSession.byObjectStatic().load(TestgrouperSyncSubjectTo.class, 1L);
      assertNotNull(testgrouperSyncSubjectTo);
      assertEquals("55", testgrouperSyncSubjectTo.getNetId());
      
    }

  /**
   * 
   */
  public void testPersonSyncIncrementalPrimaryKey() {
    
    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("grouperClient.syncTable.personSourceTest.databaseFrom", "grouper");
    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("grouperClient.syncTable.personSourceTest.tableFrom", "testgrouper_sync_subject_from");
    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("grouperClient.syncTable.personSourceTest.databaseTo", "grouper");
    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("grouperClient.syncTable.personSourceTest.tableTo", "testgrouper_sync_subject_to");
    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("grouperClient.syncTable.personSourceTest.columns", "*");
    
    // https://stackoverflow.com/questions/40841078/how-to-get-primary-keys-for-all-tables-in-jdbc
    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("grouperClient.syncTable.personSourceTest.primaryKeyColumns", "person_id");
    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("grouperClient.syncTable.personSourceTest.incrementalProgressColumn", "last_updated");

    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("grouperClient.syncTable.personSourceTest.incrementalPrimaryKeyTable", "testgrouper_sync_change_log");
    
    int countFrom = HibernateSession.bySqlStatic().select(int.class, "select count(*) from testgrouper_sync_subject_from");
    
    assertEquals(0, countFrom);
  
    int countTo = HibernateSession.bySqlStatic().select(int.class, "select count(*) from testgrouper_sync_subject_to");
    
    assertEquals(0, countTo);
  
    List<TestgrouperSyncSubjectFrom> testgrouperSyncSubjectFroms = new ArrayList<TestgrouperSyncSubjectFrom>();
    
    long now = System.currentTimeMillis();
    Calendar date = new GregorianCalendar();
    date.setTimeInMillis(now);
    date.set(Calendar.YEAR, 2019);
    date.set(Calendar.HOUR_OF_DAY, 0);
    date.set(Calendar.MINUTE, 0);
    date.set(Calendar.MILLISECOND, 0);
    date.set(Calendar.SECOND, 0);
  
    Calendar timestamp = new GregorianCalendar();
    timestamp.setTimeInMillis(now);
    timestamp.set(Calendar.MILLISECOND, 0);
    timestamp.add(Calendar.HOUR_OF_DAY, 0);
    timestamp.add(Calendar.MINUTE, 0);
    timestamp.add(Calendar.SECOND, 0);
    
    //int recordsSize = 25000; 
    int recordsSize = 25000;
    
    for (int i=0;i<recordsSize;i++) {
      TestgrouperSyncSubjectFrom testgrouperSyncSubjectFrom = new TestgrouperSyncSubjectFrom();
      testgrouperSyncSubjectFrom.setPersonId(i);
      testgrouperSyncSubjectFrom.setNetId("netId_" + i);
      testgrouperSyncSubjectFrom.setSomeInt(1+i);
      
      Calendar calendar = new GregorianCalendar();
      calendar.setTimeInMillis(date.getTimeInMillis());
      calendar.add(Calendar.MILLISECOND, i);
  
      testgrouperSyncSubjectFrom.setSomeDate(calendar.getTime()); // yyyy/mm/dd
      testgrouperSyncSubjectFrom.setSomeFloat(1.1d + i);
      
      calendar = new GregorianCalendar();
      calendar.setTimeInMillis(timestamp.getTimeInMillis());
      calendar.add(Calendar.MILLISECOND, i);
      
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
  
    // ######################
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

    GrouperClientUtils.sleep(100);
    // do some inserts and some updates and some deletes
    int numberOfInserts = 4;
    int numberOfDeletes = 2;
    int numberOfUpdates = 3;
    for (int i=recordsSize;i<recordsSize + numberOfInserts;i++) {
      TestgrouperSyncSubjectFrom testgrouperSyncSubjectFrom = new TestgrouperSyncSubjectFrom();
      testgrouperSyncSubjectFrom.setPersonId(i);
      testgrouperSyncSubjectFrom.setNetId("newnetId_" + i);
      testgrouperSyncSubjectFrom.setSomeInt(1+i);
      
      Calendar calendar = new GregorianCalendar();
      calendar.setTimeInMillis(date.getTimeInMillis());
      calendar.add(Calendar.DAY_OF_YEAR, i);
  
      testgrouperSyncSubjectFrom.setSomeDate(calendar.getTime()); // yyyy/mm/dd
      testgrouperSyncSubjectFrom.setSomeFloat(1.1d + i);
      
      calendar = new GregorianCalendar();
      calendar.setTimeInMillis(timestamp.getTimeInMillis());
      calendar.add(Calendar.DAY_OF_YEAR, i);
      
      testgrouperSyncSubjectFrom.setSomeTimestamp(new Timestamp(System.currentTimeMillis()));
      HibernateSession.byObjectStatic().save(testgrouperSyncSubjectFrom);
    }
    for (int i=0;i<numberOfUpdates;i++) {
      TestgrouperSyncSubjectFrom testgrouperSyncSubjectFrom = HibernateSession.byObjectStatic().load(TestgrouperSyncSubjectFrom.class, i);
      testgrouperSyncSubjectFrom.setNetId("newnetId_" + i);
      testgrouperSyncSubjectFrom.setSomeTimestamp(new Timestamp(System.currentTimeMillis()));
      HibernateSession.byObjectStatic().update(testgrouperSyncSubjectFrom);
    }
    for (int i=numberOfUpdates;i<numberOfUpdates + numberOfDeletes;i++) {
      TestgrouperSyncSubjectFrom testgrouperSyncSubjectFrom = HibernateSession.byObjectStatic().load(TestgrouperSyncSubjectFrom.class, i);
      HibernateSession.byObjectStatic().delete(testgrouperSyncSubjectFrom);
    }
    GrouperClientUtils.sleep(100);

    // ######################
    //do it again update some
    gcTableSync = new GcTableSync();  
    gcTableSyncOutput = gcTableSync.sync("personSourceTest", GcTableSyncSubtype.incrementalPrimaryKey); 
  
    // these dont happen on incremental all columns
    assertEquals(numberOfDeletes, gcTableSyncOutput.getDelete());
    assertEquals(numberOfUpdates, gcTableSyncOutput.getUpdate());
    assertEquals(numberOfUpdates + numberOfInserts, gcTableSyncOutput.getRowsSelectedFrom());
    assertEquals(numberOfInserts, gcTableSyncOutput.getInsert());
    
    for (int i=0;i<numberOfUpdates;i++) {
      TestgrouperSyncSubjectTo testgrouperSyncSubjectTo = HibernateSession.byObjectStatic().load(TestgrouperSyncSubjectTo.class, new Long(i));
      assertEquals("newnetId_" + i, testgrouperSyncSubjectTo.getNetId());
    }
    for (int i=recordsSize;i<recordsSize + numberOfInserts;i++) {
      TestgrouperSyncSubjectTo testgrouperSyncSubjectTo = HibernateSession.byObjectStatic().load(TestgrouperSyncSubjectTo.class, new Long(i));
      assertEquals("newnetId_" + i, testgrouperSyncSubjectTo.getNetId());
    }
    for (int i=numberOfUpdates;i<numberOfUpdates + numberOfDeletes;i++) {
      int rows = HibernateSession.bySqlStatic().select(int.class, "select count(*) from testgrouper_sync_subject_to where person_id = ?", 
          HibUtils.listObject(i), HibUtils.listType(IntegerType.INSTANCE));
      assertEquals(0, rows);
    }
    
    // ######################
    // do a full and nothing
    gcTableSync = new GcTableSync();    
    gcTableSyncOutput = gcTableSync.sync("personSourceTest", GcTableSyncSubtype.fullSyncFull); 
        
    assertEquals(0, gcTableSyncOutput.getDelete());
    assertEquals(0, gcTableSyncOutput.getUpdate());
    assertEquals(recordsSize + numberOfInserts - numberOfDeletes, gcTableSyncOutput.getRowsSelectedFrom());
    assertEquals(0, gcTableSyncOutput.getInsert());
    assertEquals(0, gcTableSyncOutput.getDelete());
    
  
    // ######################
    // incremental should do nothing
    gcTableSync = new GcTableSync();  
    gcTableSyncOutput = gcTableSync.sync("personSourceTest", GcTableSyncSubtype.incrementalPrimaryKey); 
  
    // these dont happen on incremental all columns
    assertEquals(0, gcTableSyncOutput.getDelete());
    assertEquals(0, gcTableSyncOutput.getUpdate());
    assertEquals(0, gcTableSyncOutput.getRowsSelectedFrom());
    assertEquals(0, gcTableSyncOutput.getInsert());
  
    // ######################
    // make changes, do a full and then an incremental, shouldnt do anything
    GrouperUtil.sleep(100);
    
    int numberOfNewInserts = 5;
    int numberOfNewDeletes = 6;
    int numberOfNewUpdates = 7;
    for (int i=recordsSize + numberOfInserts;i<recordsSize + numberOfInserts + numberOfNewInserts;i++) {
      TestgrouperSyncSubjectFrom testgrouperSyncSubjectFrom = new TestgrouperSyncSubjectFrom();
      testgrouperSyncSubjectFrom.setPersonId(i);
      testgrouperSyncSubjectFrom.setNetId("newnetId_" + i);
      testgrouperSyncSubjectFrom.setSomeInt(1+i);
      
      Calendar calendar = new GregorianCalendar();
      calendar.setTimeInMillis(date.getTimeInMillis());
      calendar.add(Calendar.DAY_OF_YEAR, i);
  
      testgrouperSyncSubjectFrom.setSomeDate(calendar.getTime()); // yyyy/mm/dd
      testgrouperSyncSubjectFrom.setSomeFloat(1.1d + i);
      
      calendar = new GregorianCalendar();
      calendar.setTimeInMillis(timestamp.getTimeInMillis());
      calendar.add(Calendar.DAY_OF_YEAR, i);
      
      testgrouperSyncSubjectFrom.setSomeTimestamp(new Timestamp(System.currentTimeMillis()));
      HibernateSession.byObjectStatic().save(testgrouperSyncSubjectFrom);
    }
    for (int i=numberOfUpdates + numberOfDeletes;i<numberOfUpdates + numberOfDeletes+ numberOfNewUpdates;i++) {
      TestgrouperSyncSubjectFrom testgrouperSyncSubjectFrom = HibernateSession.byObjectStatic().load(TestgrouperSyncSubjectFrom.class, i);
      testgrouperSyncSubjectFrom.setNetId("newnetId_" + i);
      testgrouperSyncSubjectFrom.setSomeTimestamp(new Timestamp(System.currentTimeMillis()));
      HibernateSession.byObjectStatic().update(testgrouperSyncSubjectFrom);
    }
    for (int i=numberOfUpdates + numberOfDeletes + numberOfNewUpdates;i<numberOfUpdates + numberOfDeletes + numberOfNewUpdates + numberOfNewDeletes;i++) {
      TestgrouperSyncSubjectFrom testgrouperSyncSubjectFrom = HibernateSession.byObjectStatic().load(TestgrouperSyncSubjectFrom.class, i);
      HibernateSession.byObjectStatic().delete(testgrouperSyncSubjectFrom);
    }
    GrouperUtil.sleep(100);
  
    gcTableSync = new GcTableSync();  
    gcTableSyncOutput = gcTableSync.sync("personSourceTest", GcTableSyncSubtype.fullSyncFull); 
  
    // these dont happen on incremental all columns
    assertEquals(numberOfNewDeletes, gcTableSyncOutput.getDelete());
    assertEquals(numberOfNewUpdates, gcTableSyncOutput.getUpdate());
    assertEquals(recordsSize + numberOfInserts + numberOfNewInserts - (numberOfDeletes + numberOfNewDeletes), gcTableSyncOutput.getRowsSelectedFrom());
    assertEquals(numberOfNewInserts, gcTableSyncOutput.getInsert());
    
    GrouperUtil.sleep(100);
  
    {
      int i=numberOfUpdates + numberOfDeletes + numberOfNewUpdates + numberOfNewDeletes;
      TestgrouperSyncSubjectFrom testgrouperSyncSubjectFrom = HibernateSession.byObjectStatic().load(TestgrouperSyncSubjectFrom.class, i);
      testgrouperSyncSubjectFrom.setNetId("newnetId_" + i);
      testgrouperSyncSubjectFrom.setSomeTimestamp(new Timestamp(System.currentTimeMillis()));
      HibernateSession.byObjectStatic().update(testgrouperSyncSubjectFrom);
    }
  
    GrouperUtil.sleep(100);
  
    // ######################
    // incremental should fix this one record since full just fixed everything
    gcTableSync = new GcTableSync();  
    gcTableSyncOutput = gcTableSync.sync("personSourceTest", GcTableSyncSubtype.incrementalPrimaryKey); 
  
    // these dont happen on incremental all columns
    assertEquals(0, gcTableSyncOutput.getDelete());
    assertEquals(1, gcTableSyncOutput.getUpdate());
    assertEquals(1, gcTableSyncOutput.getRowsSelectedFrom());
    assertEquals(0, gcTableSyncOutput.getInsert());
  
  
  }

  /**
   * @param ddlVersionBean
   * @param database
   * @param tableName
   */
  public void createTableChangeLog(DdlVersionBean ddlVersionBean, Database database) {

    Table loaderTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, "testgrouper_sync_change_log");

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "uuid", 
        Types.VARCHAR, "40", true, true);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "person_id", 
        Types.VARCHAR, "8", false, true);
  
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "hibernate_version_number", 
        Types.INTEGER, "10", false, true);
  
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "last_updated", 
        Types.TIMESTAMP, null, false, true);
  
    GrouperDdlUtils.ddlutilsTableComment(ddlVersionBean, 
        "testgrouper_sync_change_log", "testgrouper_sync_change_log");
    
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, "testgrouper_sync_change_log", "person_id", "person_id");
    
  }

  /**
   * 
   */
  public void testPersonSyncIncrementalSwitchToFull() {
    
    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("grouperClient.syncTable.personSourceTest.databaseFrom", "grouper");
    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("grouperClient.syncTable.personSourceTest.tableFrom", "testgrouper_sync_subject_from");
    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("grouperClient.syncTable.personSourceTest.databaseTo", "grouper");
    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("grouperClient.syncTable.personSourceTest.tableTo", "testgrouper_sync_subject_to");
    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("grouperClient.syncTable.personSourceTest.columns", "*");
    
    // https://stackoverflow.com/questions/40841078/how-to-get-primary-keys-for-all-tables-in-jdbc
    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("grouperClient.syncTable.personSourceTest.primaryKeyColumns", "person_id");
    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("grouperClient.syncTable.personSourceTest.incrementalAllColumnsColumn", "some_timestamp");
    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("grouperClient.syncTable.personSourceTest.switchFromIncrementalToFullIfOverRecords", "100");
    
    int countFrom = HibernateSession.bySqlStatic().select(int.class, "select count(*) from testgrouper_sync_subject_from");
    
    assertEquals(0, countFrom);
  
    int countTo = HibernateSession.bySqlStatic().select(int.class, "select count(*) from testgrouper_sync_subject_to");
    
    assertEquals(0, countTo);
  
    GrouperUtil.sleep(100);
    
    List<TestgrouperSyncSubjectFrom> testgrouperSyncSubjectFroms = new ArrayList<TestgrouperSyncSubjectFrom>();
    
    long now = System.currentTimeMillis();
    Calendar date = new GregorianCalendar();
    date.setTimeInMillis(now);
    date.set(Calendar.YEAR, 2019);
    date.set(Calendar.HOUR_OF_DAY, 0);
    date.set(Calendar.MINUTE, 0);
    date.set(Calendar.MILLISECOND, 0);
    date.set(Calendar.SECOND, 0);
  
    Calendar timestamp = new GregorianCalendar();
    timestamp.setTimeInMillis(now);
    timestamp.set(Calendar.MILLISECOND, 0);
    timestamp.add(Calendar.HOUR_OF_DAY, 0);
    timestamp.add(Calendar.MINUTE, 0);
    timestamp.add(Calendar.SECOND, 0);
    
    int recordsSize = 25000;
    
    for (int i=0;i<recordsSize;i++) {
      TestgrouperSyncSubjectFrom testgrouperSyncSubjectFrom = new TestgrouperSyncSubjectFrom();
      testgrouperSyncSubjectFrom.setPersonId(i);
      testgrouperSyncSubjectFrom.setNetId("netId_" + i);
      testgrouperSyncSubjectFrom.setSomeInt(1+i);
      
      Calendar calendar = new GregorianCalendar();
      calendar.setTimeInMillis(date.getTimeInMillis());
      calendar.add(Calendar.MILLISECOND, i);
  
      testgrouperSyncSubjectFrom.setSomeDate(calendar.getTime()); // yyyy/mm/dd
      testgrouperSyncSubjectFrom.setSomeFloat(1.1d + i);
      
      testgrouperSyncSubjectFrom.setSomeTimestamp(new Timestamp(System.currentTimeMillis()));
      testgrouperSyncSubjectFroms.add(testgrouperSyncSubjectFrom);
      
      if (testgrouperSyncSubjectFroms.size() == 1000) {
        HibernateSession.byObjectStatic().saveBatch(testgrouperSyncSubjectFroms);
        testgrouperSyncSubjectFroms.clear();
      }
      
    }
    if (testgrouperSyncSubjectFroms.size() > 0) {
      HibernateSession.byObjectStatic().saveBatch(testgrouperSyncSubjectFroms);
    }
    GrouperUtil.sleep(100);
  
    // ######################
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
    
    GrouperUtil.sleep(100);
  
    // do some inserts and some updates and some deletes
    int numberOfInserts = 4;
    int numberOfDeletes = 2;
    int numberOfUpdates = 3;
    for (int i=recordsSize;i<recordsSize + numberOfInserts;i++) {
      TestgrouperSyncSubjectFrom testgrouperSyncSubjectFrom = new TestgrouperSyncSubjectFrom();
      testgrouperSyncSubjectFrom.setPersonId(i);
      testgrouperSyncSubjectFrom.setNetId("newnetId_" + i);
      testgrouperSyncSubjectFrom.setSomeInt(1+i);
      
      Calendar calendar = new GregorianCalendar();
      calendar.setTimeInMillis(date.getTimeInMillis());
      calendar.add(Calendar.DAY_OF_YEAR, i);
  
      testgrouperSyncSubjectFrom.setSomeDate(calendar.getTime()); // yyyy/mm/dd
      testgrouperSyncSubjectFrom.setSomeFloat(1.1d + i);
      
      calendar = new GregorianCalendar();
      calendar.setTimeInMillis(timestamp.getTimeInMillis());
      calendar.add(Calendar.DAY_OF_YEAR, i);
      
      testgrouperSyncSubjectFrom.setSomeTimestamp(new Timestamp(System.currentTimeMillis()));
      HibernateSession.byObjectStatic().save(testgrouperSyncSubjectFrom);
    }
    for (int i=0;i<numberOfUpdates;i++) {
      TestgrouperSyncSubjectFrom testgrouperSyncSubjectFrom = HibernateSession.byObjectStatic().load(TestgrouperSyncSubjectFrom.class, i);
      testgrouperSyncSubjectFrom.setNetId("newnetId_" + i);
      testgrouperSyncSubjectFrom.setSomeTimestamp(new Timestamp(System.currentTimeMillis()));
      HibernateSession.byObjectStatic().update(testgrouperSyncSubjectFrom);
    }
    for (int i=numberOfUpdates;i<numberOfUpdates + numberOfDeletes;i++) {
      TestgrouperSyncSubjectFrom testgrouperSyncSubjectFrom = HibernateSession.byObjectStatic().load(TestgrouperSyncSubjectFrom.class, i);
      HibernateSession.byObjectStatic().delete(testgrouperSyncSubjectFrom);
    }
  
    GrouperUtil.sleep(100);
  
    // ######################
    //do it again should do some things
    gcTableSync = new GcTableSync();  
    gcTableSyncOutput = gcTableSync.sync("personSourceTest", GcTableSyncSubtype.incrementalAllColumns); 
  
    // these dont happen on incremental all columns
    assertEquals(0, gcTableSyncOutput.getDelete());
    assertEquals(numberOfUpdates, gcTableSyncOutput.getUpdate());
    assertEquals(numberOfUpdates + numberOfInserts, gcTableSyncOutput.getRowsSelectedFrom());
    assertEquals(numberOfInserts, gcTableSyncOutput.getInsert());
    
    for (int i=0;i<numberOfUpdates;i++) {
      TestgrouperSyncSubjectTo testgrouperSyncSubjectTo = HibernateSession.byObjectStatic().load(TestgrouperSyncSubjectTo.class, new Long(i));
      assertEquals("newnetId_" + i, testgrouperSyncSubjectTo.getNetId());
    }
    for (int i=recordsSize;i<recordsSize + numberOfInserts;i++) {
      TestgrouperSyncSubjectTo testgrouperSyncSubjectTo = HibernateSession.byObjectStatic().load(TestgrouperSyncSubjectTo.class, new Long(i));
      assertEquals("newnetId_" + i, testgrouperSyncSubjectTo.getNetId());
    }
    // these are still there until full sync
    for (int i=numberOfUpdates;i<numberOfUpdates + numberOfDeletes;i++) {
      TestgrouperSyncSubjectTo testgrouperSyncSubjectTo = HibernateSession.byObjectStatic().load(TestgrouperSyncSubjectTo.class, new Long(i));
      assertEquals("netId_" + i, testgrouperSyncSubjectTo.getNetId());
      
    }
    
    assertFalse(gcTableSyncOutput.isSwitchedToFull());
    
    GrouperUtil.sleep(100);
  
    
    // ######################
    // make changes, do a fan incremental, should fix these with full sync
    GrouperUtil.sleep(100);
    
    int numberOfNewInserts = 105;
    int numberOfNewDeletes = 6;
    int numberOfNewUpdates = 7;
    for (int i=recordsSize + numberOfInserts;i<recordsSize + numberOfInserts + numberOfNewInserts;i++) {
      TestgrouperSyncSubjectFrom testgrouperSyncSubjectFrom = new TestgrouperSyncSubjectFrom();
      testgrouperSyncSubjectFrom.setPersonId(i);
      testgrouperSyncSubjectFrom.setNetId("newnetId_" + i);
      testgrouperSyncSubjectFrom.setSomeInt(1+i);
      
      Calendar calendar = new GregorianCalendar();
      calendar.setTimeInMillis(date.getTimeInMillis());
      calendar.add(Calendar.DAY_OF_YEAR, i);
  
      testgrouperSyncSubjectFrom.setSomeDate(calendar.getTime()); // yyyy/mm/dd
      testgrouperSyncSubjectFrom.setSomeFloat(1.1d + i);
      
      calendar = new GregorianCalendar();
      calendar.setTimeInMillis(timestamp.getTimeInMillis());
      calendar.add(Calendar.DAY_OF_YEAR, i);
      
      testgrouperSyncSubjectFrom.setSomeTimestamp(new Timestamp(System.currentTimeMillis()));
      HibernateSession.byObjectStatic().save(testgrouperSyncSubjectFrom);
    }
    for (int i=numberOfUpdates + numberOfDeletes;i<numberOfUpdates + numberOfDeletes+ numberOfNewUpdates;i++) {
      TestgrouperSyncSubjectFrom testgrouperSyncSubjectFrom = HibernateSession.byObjectStatic().load(TestgrouperSyncSubjectFrom.class, i);
      testgrouperSyncSubjectFrom.setNetId("newnetId_" + i);
      testgrouperSyncSubjectFrom.setSomeTimestamp(new Timestamp(System.currentTimeMillis()));
      HibernateSession.byObjectStatic().update(testgrouperSyncSubjectFrom);
    }
    for (int i=numberOfUpdates + numberOfDeletes + numberOfNewUpdates;i<numberOfUpdates + numberOfDeletes + numberOfNewUpdates + numberOfNewDeletes;i++) {
      TestgrouperSyncSubjectFrom testgrouperSyncSubjectFrom = HibernateSession.byObjectStatic().load(TestgrouperSyncSubjectFrom.class, i);
      HibernateSession.byObjectStatic().delete(testgrouperSyncSubjectFrom);
    }
    GrouperUtil.sleep(100);

    //do another incremental, nothing to do
    gcTableSync = new GcTableSync();  
    gcTableSyncOutput = gcTableSync.sync("personSourceTest", GcTableSyncSubtype.incrementalAllColumns); 
  
    // these dont happen on incremental all columns
    assertEquals(0, gcTableSyncOutput.getDelete());
    assertEquals(0, gcTableSyncOutput.getUpdate());
    assertEquals(numberOfNewInserts + numberOfNewUpdates, gcTableSyncOutput.getRowsSelectedFrom());
    assertEquals(0, gcTableSyncOutput.getInsert());
    assertTrue(gcTableSyncOutput.isSwitchedToFull());

    
    GrouperUtil.sleep(100);
  
    // ######################
    // incremental should select nothing
    gcTableSync = new GcTableSync();  
    gcTableSyncOutput = gcTableSync.sync("personSourceTest", GcTableSyncSubtype.incrementalAllColumns); 
  
    // these dont happen on incremental all columns
    assertEquals(0, gcTableSyncOutput.getDelete());
    assertEquals(0, gcTableSyncOutput.getUpdate());
    assertEquals(0, gcTableSyncOutput.getRowsSelectedFrom());
    assertEquals(0, gcTableSyncOutput.getInsert());
  
  
  }

  /**
   * 
   */
  public void testPersonSyncIncrementalSwitchToGroup() {
    
    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("grouperClient.syncTable.personSourceTest.databaseFrom", "grouper");
    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("grouperClient.syncTable.personSourceTest.tableFrom", "testgrouper_sync_subject_from");
    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("grouperClient.syncTable.personSourceTest.databaseTo", "grouper");
    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("grouperClient.syncTable.personSourceTest.tableTo", "testgrouper_sync_subject_to");
    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("grouperClient.syncTable.personSourceTest.columns", "*");
    
    // https://stackoverflow.com/questions/40841078/how-to-get-primary-keys-for-all-tables-in-jdbc
    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("grouperClient.syncTable.personSourceTest.primaryKeyColumns", "person_id");
    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("grouperClient.syncTable.personSourceTest.groupingColumn", "the_group");
    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("grouperClient.syncTable.personSourceTest.groupingSize", "5");
    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("grouperClient.syncTable.personSourceTest.incrementalAllColumnsColumn", "some_timestamp");
    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("grouperClient.syncTable.personSourceTest.switchFromIncrementalToFullIfOverRecords", "100");
    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("grouperClient.syncTable.personSourceTest.switchFromIncrementalToGroupIfOverRecordsInGroup", "5");
    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("grouperClient.syncTable.personSourceTest.switchFromIncrementalToFullIfOverGroupCount", "100");

    int countFrom = HibernateSession.bySqlStatic().select(int.class, "select count(*) from testgrouper_sync_subject_from");
    
    assertEquals(0, countFrom);
  
    int countTo = HibernateSession.bySqlStatic().select(int.class, "select count(*) from testgrouper_sync_subject_to");
    
    assertEquals(0, countTo);
  
    GrouperUtil.sleep(100);
    
    List<TestgrouperSyncSubjectFrom> testgrouperSyncSubjectFroms = new ArrayList<TestgrouperSyncSubjectFrom>();
    
    long now = System.currentTimeMillis();
    Calendar date = new GregorianCalendar();
    date.setTimeInMillis(now);
    date.set(Calendar.YEAR, 2019);
    date.set(Calendar.HOUR_OF_DAY, 0);
    date.set(Calendar.MINUTE, 0);
    date.set(Calendar.MILLISECOND, 0);
    date.set(Calendar.SECOND, 0);
  
    Calendar timestamp = new GregorianCalendar();
    timestamp.setTimeInMillis(now);
    timestamp.set(Calendar.MILLISECOND, 0);
    timestamp.add(Calendar.HOUR_OF_DAY, 0);
    timestamp.add(Calendar.MINUTE, 0);
    timestamp.add(Calendar.SECOND, 0);
    
    int recordsSize = 25000;
    
    for (int i=0;i<recordsSize;i++) {
      TestgrouperSyncSubjectFrom testgrouperSyncSubjectFrom = new TestgrouperSyncSubjectFrom();
      testgrouperSyncSubjectFrom.setPersonId(i);
      testgrouperSyncSubjectFrom.setNetId("netId_" + i);
      testgrouperSyncSubjectFrom.setSomeInt(1+i);
      
      Calendar calendar = new GregorianCalendar();
      calendar.setTimeInMillis(date.getTimeInMillis());
      calendar.add(Calendar.MILLISECOND, i);
  
      testgrouperSyncSubjectFrom.setSomeDate(calendar.getTime()); // yyyy/mm/dd
      testgrouperSyncSubjectFrom.setSomeFloat(1.1d + i);
      
      testgrouperSyncSubjectFrom.setSomeTimestamp(new Timestamp(System.currentTimeMillis()));
      
      testgrouperSyncSubjectFrom.setTheGroup("group_" + ((int)((i+1)/10)));

      testgrouperSyncSubjectFroms.add(testgrouperSyncSubjectFrom);
      
      if (testgrouperSyncSubjectFroms.size() == 1000) {
        HibernateSession.byObjectStatic().saveBatch(testgrouperSyncSubjectFroms);
        testgrouperSyncSubjectFroms.clear();
      }
      
    }
    if (testgrouperSyncSubjectFroms.size() > 0) {
      HibernateSession.byObjectStatic().saveBatch(testgrouperSyncSubjectFroms);
    }
    GrouperUtil.sleep(100);
  
    // ######################
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

    GrouperUtil.sleep(100);

    // do some inserts and some updates and some deletes
    int numberOfInserts = 4;
    int numberOfDeletes = 2;
    int numberOfUpdates = 3;
    for (int i=recordsSize;i<recordsSize + numberOfInserts;i++) {
      TestgrouperSyncSubjectFrom testgrouperSyncSubjectFrom = new TestgrouperSyncSubjectFrom();
      testgrouperSyncSubjectFrom.setPersonId(i);
      testgrouperSyncSubjectFrom.setNetId("newnetId_" + i);
      testgrouperSyncSubjectFrom.setSomeInt(1+i);
      
      Calendar calendar = new GregorianCalendar();
      calendar.setTimeInMillis(date.getTimeInMillis());
      calendar.add(Calendar.DAY_OF_YEAR, i);
  
      testgrouperSyncSubjectFrom.setSomeDate(calendar.getTime()); // yyyy/mm/dd
      testgrouperSyncSubjectFrom.setSomeFloat(1.1d + i);
      
      calendar = new GregorianCalendar();
      calendar.setTimeInMillis(timestamp.getTimeInMillis());
      calendar.add(Calendar.DAY_OF_YEAR, i);
      
      testgrouperSyncSubjectFrom.setSomeTimestamp(new Timestamp(System.currentTimeMillis()));
      
      testgrouperSyncSubjectFrom.setTheGroup("group_" + ((int)((i+1)/10)));

      HibernateSession.byObjectStatic().save(testgrouperSyncSubjectFrom);
    }
    for (int i=0;i<numberOfUpdates;i++) {
      TestgrouperSyncSubjectFrom testgrouperSyncSubjectFrom = HibernateSession.byObjectStatic().load(TestgrouperSyncSubjectFrom.class, i);
      testgrouperSyncSubjectFrom.setNetId("newnetId_" + i);
      testgrouperSyncSubjectFrom.setSomeTimestamp(new Timestamp(System.currentTimeMillis()));
      HibernateSession.byObjectStatic().update(testgrouperSyncSubjectFrom);
    }
    for (int i=numberOfUpdates;i<numberOfUpdates + numberOfDeletes;i++) {
      TestgrouperSyncSubjectFrom testgrouperSyncSubjectFrom = HibernateSession.byObjectStatic().load(TestgrouperSyncSubjectFrom.class, i);
      HibernateSession.byObjectStatic().delete(testgrouperSyncSubjectFrom);
    }
  
    GrouperUtil.sleep(100);
  
    // ######################
    //do it again should do some things
    gcTableSync = new GcTableSync();  
    gcTableSyncOutput = gcTableSync.sync("personSourceTest", GcTableSyncSubtype.incrementalAllColumns); 
  
    // these dont happen on incremental all columns
    assertEquals(0, gcTableSyncOutput.getDelete());
    assertEquals(numberOfUpdates, gcTableSyncOutput.getUpdate());
    assertEquals(numberOfUpdates + numberOfInserts, gcTableSyncOutput.getRowsSelectedFrom());
    assertEquals(numberOfInserts, gcTableSyncOutput.getInsert());
    
    for (int i=0;i<numberOfUpdates;i++) {
      TestgrouperSyncSubjectTo testgrouperSyncSubjectTo = HibernateSession.byObjectStatic().load(TestgrouperSyncSubjectTo.class, new Long(i));
      assertEquals("newnetId_" + i, testgrouperSyncSubjectTo.getNetId());
    }
    for (int i=recordsSize;i<recordsSize + numberOfInserts;i++) {
      TestgrouperSyncSubjectTo testgrouperSyncSubjectTo = HibernateSession.byObjectStatic().load(TestgrouperSyncSubjectTo.class, new Long(i));
      assertEquals("newnetId_" + i, testgrouperSyncSubjectTo.getNetId());
    }
    // these are still there until full sync
    for (int i=numberOfUpdates;i<numberOfUpdates + numberOfDeletes;i++) {
      TestgrouperSyncSubjectTo testgrouperSyncSubjectTo = HibernateSession.byObjectStatic().load(TestgrouperSyncSubjectTo.class, new Long(i));
      assertEquals("netId_" + i, testgrouperSyncSubjectTo.getNetId());
      
    }
    
    assertFalse(gcTableSyncOutput.isSwitchedToFull());
    
    GrouperUtil.sleep(100);
  
    
    // ######################
    // make changes, do a fan incremental, should fix these with full sync
    GrouperUtil.sleep(100);
    
    int numberOfNewInserts = 105;
    int numberOfNewDeletes = 6;
    int numberOfNewUpdates = 7;
    for (int i=recordsSize + numberOfInserts;i<recordsSize + numberOfInserts + numberOfNewInserts;i++) {
      TestgrouperSyncSubjectFrom testgrouperSyncSubjectFrom = new TestgrouperSyncSubjectFrom();
      testgrouperSyncSubjectFrom.setPersonId(i);
      testgrouperSyncSubjectFrom.setNetId("newnetId_" + i);
      testgrouperSyncSubjectFrom.setSomeInt(1+i);
      
      Calendar calendar = new GregorianCalendar();
      calendar.setTimeInMillis(date.getTimeInMillis());
      calendar.add(Calendar.DAY_OF_YEAR, i);
  
      testgrouperSyncSubjectFrom.setSomeDate(calendar.getTime()); // yyyy/mm/dd
      testgrouperSyncSubjectFrom.setSomeFloat(1.1d + i);
      
      calendar = new GregorianCalendar();
      calendar.setTimeInMillis(timestamp.getTimeInMillis());
      calendar.add(Calendar.DAY_OF_YEAR, i);
      
      testgrouperSyncSubjectFrom.setSomeTimestamp(new Timestamp(System.currentTimeMillis()));
      
      testgrouperSyncSubjectFrom.setTheGroup("group_" + ((int)((i+1)/10)));

      HibernateSession.byObjectStatic().save(testgrouperSyncSubjectFrom);
    }
    for (int i=numberOfUpdates + numberOfDeletes;i<numberOfUpdates + numberOfDeletes+ numberOfNewUpdates;i++) {
      TestgrouperSyncSubjectFrom testgrouperSyncSubjectFrom = HibernateSession.byObjectStatic().load(TestgrouperSyncSubjectFrom.class, i);
      testgrouperSyncSubjectFrom.setNetId("newnetId_" + i);
      testgrouperSyncSubjectFrom.setSomeTimestamp(new Timestamp(System.currentTimeMillis()));
      HibernateSession.byObjectStatic().update(testgrouperSyncSubjectFrom);
    }
    for (int i=numberOfUpdates + numberOfDeletes + numberOfNewUpdates;i<numberOfUpdates + numberOfDeletes + numberOfNewUpdates + numberOfNewDeletes;i++) {
      TestgrouperSyncSubjectFrom testgrouperSyncSubjectFrom = HibernateSession.byObjectStatic().load(TestgrouperSyncSubjectFrom.class, i);
      HibernateSession.byObjectStatic().delete(testgrouperSyncSubjectFrom);
    }
    GrouperUtil.sleep(100);
  
    //do another incremental, nothing to do
    gcTableSync = new GcTableSync();  
    gcTableSyncOutput = gcTableSync.sync("personSourceTest", GcTableSyncSubtype.incrementalAllColumns); 
  
    // these dont happen on incremental all columns
    assertEquals(0, gcTableSyncOutput.getDelete());
    assertEquals(7, gcTableSyncOutput.getUpdate());
    assertTrue(numberOfNewInserts + numberOfNewUpdates < gcTableSyncOutput.getRowsSelectedFrom());
    assertEquals(numberOfNewInserts, gcTableSyncOutput.getInsert());
    assertFalse(gcTableSyncOutput.isSwitchedToFull());
    assertEquals(10, GrouperUtil.length(gcTableSyncOutput.getSwitchedToGroups()));
  
    
    GrouperUtil.sleep(100);
  
    // ######################
    // incremental should select nothing
    gcTableSync = new GcTableSync();  
    gcTableSyncOutput = gcTableSync.sync("personSourceTest", GcTableSyncSubtype.incrementalAllColumns); 
  
    // these dont happen on incremental all columns
    assertEquals(0, gcTableSyncOutput.getDelete());
    assertEquals(0, gcTableSyncOutput.getUpdate());
    assertEquals(0, gcTableSyncOutput.getRowsSelectedFrom());
    assertEquals(0, gcTableSyncOutput.getInsert());
  
  
  }

  /**
   * 
   */
  public void testPersonSyncFullMetadata() {
    
    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("grouperClient.syncTable.personSourceTest.databaseFrom", "grouper");
    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("grouperClient.syncTable.personSourceTest.tableFrom", "testgrouper_sync_subject_from");
    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("grouperClient.syncTable.personSourceTest.databaseTo", "grouper");
    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("grouperClient.syncTable.personSourceTest.tableTo", "testgrouper_sync_subject_to");
    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("grouperClient.syncTable.personSourceTest.columns", "*");
    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("grouperClient.syncTable.personSourceTest.changeFlagColumn", "change_flag");
    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("grouperClient.syncTable.personSourceTest.primaryKeyColumns", "person_id");
    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("grouperClient.syncTable.personSourceTest.groupingColumn", "the_group");
    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("grouperClient.syncTable.personSourceTest.groupingSize", "5");
    
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
      testgrouperSyncSubjectFrom.setPersonId(i);
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
      
      testgrouperSyncSubjectFrom.setTheGroup("group_" + ((int)((i+1)/10)));
      
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
    GcTableSyncOutput gcTableSyncOutput = gcTableSync.sync("personSourceTest", GcTableSyncSubtype.fullSyncMetadata); 
  
    assertEquals(0, gcTableSyncOutput.getDelete());
    assertEquals(0, gcTableSyncOutput.getUpdate());
    assertTrue(gcTableSyncOutput.getRowsSelectedFrom() > recordsSize);
    assertEquals(recordsSize, gcTableSyncOutput.getInsert());
  
    countTo = HibernateSession.bySqlStatic().select(int.class, "select count(*) from testgrouper_sync_subject_to");
  
    assertEquals(recordsSize, countTo);
  
    //do it again should do nothing
    gcTableSync = new GcTableSync();    
    gcTableSyncOutput = gcTableSync.sync("personSourceTest", GcTableSyncSubtype.fullSyncMetadata); 
  
    assertEquals(0, gcTableSyncOutput.getDelete());
    assertEquals(0, gcTableSyncOutput.getUpdate());
    assertEquals(0, gcTableSyncOutput.getInsert());
    
    TestgrouperSyncSubjectTo testgrouperSyncSubjectTo = HibernateSession.byObjectStatic().load(TestgrouperSyncSubjectTo.class, 0L);
    assertEquals(new Long(0L), testgrouperSyncSubjectTo.getPersonId());
    assertEquals("netId_0", testgrouperSyncSubjectTo.getNetId());
    assertEquals(new Date(date.getTimeInMillis()), testgrouperSyncSubjectTo.getSomeDate());
    assertEquals(1.1d, testgrouperSyncSubjectTo.getSomeFloat());
    assertEquals(new Integer(1), testgrouperSyncSubjectTo.getSomeInt());
    assertEquals(new Timestamp(timestamp.getTimeInMillis()), testgrouperSyncSubjectTo.getSomeTimestamp());
    
    // this will be a delete
    TestgrouperSyncSubjectFrom testgrouperSyncSubjectFrom = HibernateSession.byObjectStatic().load(TestgrouperSyncSubjectFrom.class, 0);
    // sdf
    int deletedRows = HibernateSession.bySqlStatic().executeSql("delete from testgrouper_sync_subject_from where the_group = ?", 
        HibUtils.listObject(testgrouperSyncSubjectFrom.getTheGroup()), HibUtils.listType(StringType.INSTANCE)); 

    // this will be an insert
    testgrouperSyncSubjectFrom = new TestgrouperSyncSubjectFrom();
    testgrouperSyncSubjectFrom.setPersonId(-1);
    testgrouperSyncSubjectFrom.setNetId("myNetIdWhatever");
    testgrouperSyncSubjectFrom.setHibernateVersionNumber(GrouperAPI.INITIAL_VERSION_NUMBER);
    testgrouperSyncSubjectFrom.setTheGroup("group_99a");
    HibernateSession.byObjectStatic().saveOrUpdate(testgrouperSyncSubjectFrom);
  
    // this will be nothing since exisitng groups dont get changed
    testgrouperSyncSubjectFrom = HibernateSession.byObjectStatic().load(TestgrouperSyncSubjectFrom.class, 10);
    testgrouperSyncSubjectFrom.setNetId("55");
    HibernateSession.byObjectStatic().saveOrUpdate(testgrouperSyncSubjectFrom);
  
    gcTableSync = new GcTableSync();
    gcTableSyncOutput = gcTableSync.sync("personSourceTest", GcTableSyncSubtype.fullSyncMetadata); 
        
    assertEquals(deletedRows, gcTableSyncOutput.getDelete());
    assertEquals(0, gcTableSyncOutput.getUpdate());
    assertEquals(1, gcTableSyncOutput.getInsert());
    
    gcTableSync = new GcTableSync();
    gcTableSyncOutput = gcTableSync.sync("personSourceTest", GcTableSyncSubtype.fullSyncFull); 
    
    assertEquals(0, gcTableSyncOutput.getDelete());
    assertEquals(1, gcTableSyncOutput.getUpdate());
    assertEquals(0, gcTableSyncOutput.getInsert());
    
  }

  /**
   * 
   */
  public void testPersonSyncFullGroupingsNumber() {
    
    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("grouperClient.syncTable.personSourceTest.databaseFrom", "grouper");
    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("grouperClient.syncTable.personSourceTest.tableFrom", "testgrouper_sync_subject_from");
    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("grouperClient.syncTable.personSourceTest.databaseTo", "grouper");
    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("grouperClient.syncTable.personSourceTest.tableTo", "testgrouper_sync_subject_to");
    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("grouperClient.syncTable.personSourceTest.columns", "*");
    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("grouperClient.syncTable.personSourceTest.changeFlagColumn", "change_flag");
    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("grouperClient.syncTable.personSourceTest.primaryKeyColumns", "person_id");
    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("grouperClient.syncTable.personSourceTest.groupingColumn", "person_id");
    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("grouperClient.syncTable.personSourceTest.groupingSize", "5");
    
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
      testgrouperSyncSubjectFrom.setPersonId(i);
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
      
      testgrouperSyncSubjectFrom.setTheGroup("group_" + ((int)((i+1)/10)));
      
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
    GcTableSyncOutput gcTableSyncOutput = gcTableSync.sync("personSourceTest", GcTableSyncSubtype.fullSyncGroups); 
  
    assertEquals(0, gcTableSyncOutput.getDelete());
    assertEquals(0, gcTableSyncOutput.getUpdate());
    assertTrue(gcTableSyncOutput.getRowsSelectedFrom() > recordsSize);
    assertEquals(recordsSize, gcTableSyncOutput.getInsert());
    assertEquals(recordsSize, gcTableSync.getGcGrouperSync().getRecordsCount().intValue());
  
    countTo = HibernateSession.bySqlStatic().select(int.class, "select count(*) from testgrouper_sync_subject_to");
  
    assertEquals(recordsSize, countTo);
  
    //do it again should do nothing
    gcTableSync = new GcTableSync();      
    gcTableSyncOutput = gcTableSync.sync("personSourceTest", GcTableSyncSubtype.fullSyncGroups); 
  
    assertEquals(0, gcTableSyncOutput.getDelete());
    assertEquals(0, gcTableSyncOutput.getUpdate());
    assertTrue(gcTableSyncOutput.getRowsSelectedFrom() > recordsSize);
    assertEquals(0, gcTableSyncOutput.getInsert());
    
    TestgrouperSyncSubjectTo testgrouperSyncSubjectTo = HibernateSession.byObjectStatic().load(TestgrouperSyncSubjectTo.class, 0l);
    assertEquals(new Long(0), testgrouperSyncSubjectTo.getPersonId());
    assertEquals("netId_0", testgrouperSyncSubjectTo.getNetId());
    assertEquals(new Date(date.getTimeInMillis()), testgrouperSyncSubjectTo.getSomeDate());
    assertEquals(1.1d, testgrouperSyncSubjectTo.getSomeFloat());
    assertEquals(new Integer(1), testgrouperSyncSubjectTo.getSomeInt());
    assertEquals(new Timestamp(timestamp.getTimeInMillis()), testgrouperSyncSubjectTo.getSomeTimestamp());
    
    // this will be a delete
    TestgrouperSyncSubjectFrom testgrouperSyncSubjectFrom = HibernateSession.byObjectStatic().load(TestgrouperSyncSubjectFrom.class, 0);
    HibernateSession.byObjectStatic().delete(testgrouperSyncSubjectFrom);
  
    // this will be an insert
    testgrouperSyncSubjectFrom.setPersonId(-1);
    testgrouperSyncSubjectFrom.setHibernateVersionNumber(GrouperAPI.INITIAL_VERSION_NUMBER);
    HibernateSession.byObjectStatic().saveOrUpdate(testgrouperSyncSubjectFrom);
  
    // this will be an update
    testgrouperSyncSubjectFrom = HibernateSession.byObjectStatic().load(TestgrouperSyncSubjectFrom.class, 1);
    testgrouperSyncSubjectFrom.setNetId("55");
    HibernateSession.byObjectStatic().saveOrUpdate(testgrouperSyncSubjectFrom);
  
    gcTableSync = new GcTableSync();     
    gcTableSyncOutput = gcTableSync.sync("personSourceTest", GcTableSyncSubtype.fullSyncGroups); 
        
    assertEquals(1, gcTableSyncOutput.getDelete());
    assertEquals(1, gcTableSyncOutput.getUpdate());
    assertTrue(gcTableSyncOutput.getRowsSelectedFrom() > recordsSize);
    assertEquals(1, gcTableSyncOutput.getInsert());
    
    int rows = HibernateSession.bySqlStatic().select(int.class, "select count(*) from testgrouper_sync_subject_to where person_id = ?", 
        HibUtils.listObject(0), HibUtils.listType(StringType.INSTANCE));
    assertEquals(0, rows);
  
    testgrouperSyncSubjectTo = HibernateSession.byObjectStatic().load(TestgrouperSyncSubjectTo.class, -1L);
    assertNotNull(testgrouperSyncSubjectTo);
    
    assertEquals(new Long(-1), testgrouperSyncSubjectTo.getPersonId());
    assertEquals("netId_0", testgrouperSyncSubjectTo.getNetId());
    assertEquals(new Date(date.getTimeInMillis()), testgrouperSyncSubjectTo.getSomeDate());
    assertEquals(1.1d, testgrouperSyncSubjectTo.getSomeFloat());
    assertEquals(new Integer(1), testgrouperSyncSubjectTo.getSomeInt());
    assertEquals(new Timestamp(timestamp.getTimeInMillis()), testgrouperSyncSubjectTo.getSomeTimestamp());
  
    testgrouperSyncSubjectTo = HibernateSession.byObjectStatic().load(TestgrouperSyncSubjectTo.class, 1L);
    assertNotNull(testgrouperSyncSubjectTo);
    assertEquals("55", testgrouperSyncSubjectTo.getNetId());
    
  }

  /**
   * 
   */
  public void testPersonSyncIncrementalSwitchToGroupInteger() {
    
    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("grouperClient.syncTable.personSourceTest.databaseFrom", "grouper");
    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("grouperClient.syncTable.personSourceTest.tableFrom", "testgrouper_sync_subject_from");
    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("grouperClient.syncTable.personSourceTest.databaseTo", "grouper");
    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("grouperClient.syncTable.personSourceTest.tableTo", "testgrouper_sync_subject_to");
    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("grouperClient.syncTable.personSourceTest.columns", "*");
    
    // https://stackoverflow.com/questions/40841078/how-to-get-primary-keys-for-all-tables-in-jdbc
    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("grouperClient.syncTable.personSourceTest.primaryKeyColumns", "person_id");
    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("grouperClient.syncTable.personSourceTest.groupingColumn", "person_id");
    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("grouperClient.syncTable.personSourceTest.groupingSize", "5");
    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("grouperClient.syncTable.personSourceTest.incrementalAllColumnsColumn", "some_timestamp");
    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("grouperClient.syncTable.personSourceTest.switchFromIncrementalToFullIfOverRecords", "100");
    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("grouperClient.syncTable.personSourceTest.switchFromIncrementalToGroupIfOverRecordsInGroup", "5");
    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("grouperClient.syncTable.personSourceTest.switchFromIncrementalToFullIfOverGroupCount", "100");
  
    int countFrom = HibernateSession.bySqlStatic().select(int.class, "select count(*) from testgrouper_sync_subject_from");
    
    assertEquals(0, countFrom);
  
    int countTo = HibernateSession.bySqlStatic().select(int.class, "select count(*) from testgrouper_sync_subject_to");
    
    assertEquals(0, countTo);
  
    GrouperUtil.sleep(100);
    
    List<TestgrouperSyncSubjectFrom> testgrouperSyncSubjectFroms = new ArrayList<TestgrouperSyncSubjectFrom>();
    
    long now = System.currentTimeMillis();
    Calendar date = new GregorianCalendar();
    date.setTimeInMillis(now);
    date.set(Calendar.YEAR, 2019);
    date.set(Calendar.HOUR_OF_DAY, 0);
    date.set(Calendar.MINUTE, 0);
    date.set(Calendar.MILLISECOND, 0);
    date.set(Calendar.SECOND, 0);
  
    Calendar timestamp = new GregorianCalendar();
    timestamp.setTimeInMillis(now);
    timestamp.set(Calendar.MILLISECOND, 0);
    timestamp.add(Calendar.HOUR_OF_DAY, 0);
    timestamp.add(Calendar.MINUTE, 0);
    timestamp.add(Calendar.SECOND, 0);
    
    int recordsSize = 25000;
    
    for (int i=0;i<recordsSize;i++) {
      TestgrouperSyncSubjectFrom testgrouperSyncSubjectFrom = new TestgrouperSyncSubjectFrom();
      testgrouperSyncSubjectFrom.setPersonId(i);
      testgrouperSyncSubjectFrom.setNetId("netId_" + i);
      testgrouperSyncSubjectFrom.setSomeInt(1+i);
      
      Calendar calendar = new GregorianCalendar();
      calendar.setTimeInMillis(date.getTimeInMillis());
      calendar.add(Calendar.MILLISECOND, i);
  
      testgrouperSyncSubjectFrom.setSomeDate(calendar.getTime()); // yyyy/mm/dd
      testgrouperSyncSubjectFrom.setSomeFloat(1.1d + i);
      
      testgrouperSyncSubjectFrom.setSomeTimestamp(new Timestamp(System.currentTimeMillis()));
      
      testgrouperSyncSubjectFrom.setTheGroup("group_" + ((int)((i+1)/10)));
  
      testgrouperSyncSubjectFroms.add(testgrouperSyncSubjectFrom);
      
      if (testgrouperSyncSubjectFroms.size() == 1000) {
        HibernateSession.byObjectStatic().saveBatch(testgrouperSyncSubjectFroms);
        testgrouperSyncSubjectFroms.clear();
      }
      
    }
    if (testgrouperSyncSubjectFroms.size() > 0) {
      HibernateSession.byObjectStatic().saveBatch(testgrouperSyncSubjectFroms);
    }
    GrouperUtil.sleep(100);
  
    // ######################
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
  
    GrouperUtil.sleep(100);
  
    // do some inserts and some updates and some deletes
    int numberOfInserts = 4;
    int numberOfDeletes = 2;
    int numberOfUpdates = 3;
    for (int i=recordsSize;i<recordsSize + numberOfInserts;i++) {
      TestgrouperSyncSubjectFrom testgrouperSyncSubjectFrom = new TestgrouperSyncSubjectFrom();
      testgrouperSyncSubjectFrom.setPersonId(i);
      testgrouperSyncSubjectFrom.setNetId("newnetId_" + i);
      testgrouperSyncSubjectFrom.setSomeInt(1+i);
      
      Calendar calendar = new GregorianCalendar();
      calendar.setTimeInMillis(date.getTimeInMillis());
      calendar.add(Calendar.DAY_OF_YEAR, i);
  
      testgrouperSyncSubjectFrom.setSomeDate(calendar.getTime()); // yyyy/mm/dd
      testgrouperSyncSubjectFrom.setSomeFloat(1.1d + i);
      
      calendar = new GregorianCalendar();
      calendar.setTimeInMillis(timestamp.getTimeInMillis());
      calendar.add(Calendar.DAY_OF_YEAR, i);
      
      testgrouperSyncSubjectFrom.setSomeTimestamp(new Timestamp(System.currentTimeMillis()));
      
      testgrouperSyncSubjectFrom.setTheGroup("group_" + ((int)((i+1)/10)));
  
      HibernateSession.byObjectStatic().save(testgrouperSyncSubjectFrom);
    }
    for (int i=0;i<numberOfUpdates;i++) {
      TestgrouperSyncSubjectFrom testgrouperSyncSubjectFrom = HibernateSession.byObjectStatic().load(TestgrouperSyncSubjectFrom.class, i);
      testgrouperSyncSubjectFrom.setNetId("newnetId_" + i);
      testgrouperSyncSubjectFrom.setSomeTimestamp(new Timestamp(System.currentTimeMillis()));
      HibernateSession.byObjectStatic().update(testgrouperSyncSubjectFrom);
    }
    for (int i=numberOfUpdates;i<numberOfUpdates + numberOfDeletes;i++) {
      TestgrouperSyncSubjectFrom testgrouperSyncSubjectFrom = HibernateSession.byObjectStatic().load(TestgrouperSyncSubjectFrom.class, i);
      HibernateSession.byObjectStatic().delete(testgrouperSyncSubjectFrom);
    }
  
    GrouperUtil.sleep(100);
  
    // ######################
    //do it again should do some things
    gcTableSync = new GcTableSync();  
    gcTableSyncOutput = gcTableSync.sync("personSourceTest", GcTableSyncSubtype.incrementalAllColumns); 
  
    // these dont happen on incremental all columns
    assertEquals(0, gcTableSyncOutput.getDelete());
    assertEquals(numberOfUpdates, gcTableSyncOutput.getUpdate());
    assertEquals(numberOfUpdates + numberOfInserts, gcTableSyncOutput.getRowsSelectedFrom());
    assertEquals(numberOfInserts, gcTableSyncOutput.getInsert());
    
    for (int i=0;i<numberOfUpdates;i++) {
      TestgrouperSyncSubjectTo testgrouperSyncSubjectTo = HibernateSession.byObjectStatic().load(TestgrouperSyncSubjectTo.class, new Long(i));
      assertEquals("newnetId_" + i, testgrouperSyncSubjectTo.getNetId());
    }
    for (int i=recordsSize;i<recordsSize + numberOfInserts;i++) {
      TestgrouperSyncSubjectTo testgrouperSyncSubjectTo = HibernateSession.byObjectStatic().load(TestgrouperSyncSubjectTo.class, new Long(i));
      assertEquals("newnetId_" + i, testgrouperSyncSubjectTo.getNetId());
    }
    // these are still there until full sync
    for (int i=numberOfUpdates;i<numberOfUpdates + numberOfDeletes;i++) {
      TestgrouperSyncSubjectTo testgrouperSyncSubjectTo = HibernateSession.byObjectStatic().load(TestgrouperSyncSubjectTo.class, new Long(i));
      assertEquals("netId_" + i, testgrouperSyncSubjectTo.getNetId());
      
    }
    
    assertFalse(gcTableSyncOutput.isSwitchedToFull());
    
    GrouperUtil.sleep(100);
  
    
    // ######################
    // make changes, do a fan incremental, should fix these with full sync
    GrouperUtil.sleep(100);
    
    int numberOfNewInserts = 105;
    int numberOfNewDeletes = 6;
    int numberOfNewUpdates = 7;
    for (int i=recordsSize + numberOfInserts;i<recordsSize + numberOfInserts + numberOfNewInserts;i++) {
      TestgrouperSyncSubjectFrom testgrouperSyncSubjectFrom = new TestgrouperSyncSubjectFrom();
      testgrouperSyncSubjectFrom.setPersonId(i);
      testgrouperSyncSubjectFrom.setNetId("newnetId_" + i);
      testgrouperSyncSubjectFrom.setSomeInt(1+i);
      
      Calendar calendar = new GregorianCalendar();
      calendar.setTimeInMillis(date.getTimeInMillis());
      calendar.add(Calendar.DAY_OF_YEAR, i);
  
      testgrouperSyncSubjectFrom.setSomeDate(calendar.getTime()); // yyyy/mm/dd
      testgrouperSyncSubjectFrom.setSomeFloat(1.1d + i);
      
      calendar = new GregorianCalendar();
      calendar.setTimeInMillis(timestamp.getTimeInMillis());
      calendar.add(Calendar.DAY_OF_YEAR, i);
      
      testgrouperSyncSubjectFrom.setSomeTimestamp(new Timestamp(System.currentTimeMillis()));
      
      testgrouperSyncSubjectFrom.setTheGroup("group_" + ((int)((i+1)/10)));
  
      HibernateSession.byObjectStatic().save(testgrouperSyncSubjectFrom);
    }
    for (int i=numberOfUpdates + numberOfDeletes;i<numberOfUpdates + numberOfDeletes+ numberOfNewUpdates;i++) {
      TestgrouperSyncSubjectFrom testgrouperSyncSubjectFrom = HibernateSession.byObjectStatic().load(TestgrouperSyncSubjectFrom.class, i);
      testgrouperSyncSubjectFrom.setNetId("newnetId_" + i);
      testgrouperSyncSubjectFrom.setSomeTimestamp(new Timestamp(System.currentTimeMillis()));
      HibernateSession.byObjectStatic().update(testgrouperSyncSubjectFrom);
    }
    for (int i=numberOfUpdates + numberOfDeletes + numberOfNewUpdates;i<numberOfUpdates + numberOfDeletes + numberOfNewUpdates + numberOfNewDeletes;i++) {
      TestgrouperSyncSubjectFrom testgrouperSyncSubjectFrom = HibernateSession.byObjectStatic().load(TestgrouperSyncSubjectFrom.class, i);
      HibernateSession.byObjectStatic().delete(testgrouperSyncSubjectFrom);
    }
    GrouperUtil.sleep(100);
  
    // ######################
    // incremental should select nothing
    gcTableSync = new GcTableSync();  
    gcTableSyncOutput = gcTableSync.sync("personSourceTest", GcTableSyncSubtype.incrementalAllColumns); 
  
    // these dont happen on incremental all columns
    assertEquals(0, gcTableSyncOutput.getDelete());
    assertEquals(0, gcTableSyncOutput.getUpdate());
    assertEquals(0, gcTableSyncOutput.getInsert());
  
  
  }

  /**
   * 
   */
  public void testPersonSyncIncremental() {
    
    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("grouperClient.syncTable.personSourceTest.databaseFrom", "grouper");
    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("grouperClient.syncTable.personSourceTest.tableFrom", "testgrouper_sync_subject_from");
    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("grouperClient.syncTable.personSourceTest.databaseTo", "grouper");
    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("grouperClient.syncTable.personSourceTest.tableTo", "testgrouper_sync_subject_to");
    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("grouperClient.syncTable.personSourceTest.columns", "*");
    
    // https://stackoverflow.com/questions/40841078/how-to-get-primary-keys-for-all-tables-in-jdbc
    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("grouperClient.syncTable.personSourceTest.primaryKeyColumns", "person_id");
    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("grouperClient.syncTable.personSourceTest.incrementalProgressColumn", "last_updated");
  
    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("grouperClient.syncTable.personSourceTest.incrementalPrimaryKeyTable", "testgrouper_sync_change_log");
    
    int countFrom = HibernateSession.bySqlStatic().select(int.class, "select count(*) from testgrouper_sync_subject_from");
    
    assertEquals(0, countFrom);
  
    int countTo = HibernateSession.bySqlStatic().select(int.class, "select count(*) from testgrouper_sync_subject_to");
    
    assertEquals(0, countTo);
  
    List<TestgrouperSyncSubjectFrom> testgrouperSyncSubjectFroms = new ArrayList<TestgrouperSyncSubjectFrom>();
    
    long now = System.currentTimeMillis();
    Calendar date = new GregorianCalendar();
    date.setTimeInMillis(now);
    date.set(Calendar.YEAR, 2019);
    date.set(Calendar.HOUR_OF_DAY, 0);
    date.set(Calendar.MINUTE, 0);
    date.set(Calendar.MILLISECOND, 0);
    date.set(Calendar.SECOND, 0);
  
    Calendar timestamp = new GregorianCalendar();
    timestamp.setTimeInMillis(now);
    timestamp.set(Calendar.MILLISECOND, 0);
    timestamp.add(Calendar.HOUR_OF_DAY, 0);
    timestamp.add(Calendar.MINUTE, 0);
    timestamp.add(Calendar.SECOND, 0);
    
    //int recordsSize = 25000; 
    int recordsSize = 25000;
    
    for (int i=0;i<recordsSize;i++) {
      TestgrouperSyncSubjectFrom testgrouperSyncSubjectFrom = new TestgrouperSyncSubjectFrom();
      testgrouperSyncSubjectFrom.setPersonId(i);
      testgrouperSyncSubjectFrom.setNetId("netId_" + i);
      testgrouperSyncSubjectFrom.setSomeInt(1+i);
      
      Calendar calendar = new GregorianCalendar();
      calendar.setTimeInMillis(date.getTimeInMillis());
      calendar.add(Calendar.MILLISECOND, i);
  
      testgrouperSyncSubjectFrom.setSomeDate(calendar.getTime()); // yyyy/mm/dd
      testgrouperSyncSubjectFrom.setSomeFloat(1.1d + i);
      
      calendar = new GregorianCalendar();
      calendar.setTimeInMillis(timestamp.getTimeInMillis());
      calendar.add(Calendar.MILLISECOND, i);
      
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
  
    // ######################
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
  
    GrouperClientUtils.sleep(100);
    // do some inserts and some updates and some deletes
    int numberOfInserts = 4;
    int numberOfDeletes = 2;
    int numberOfUpdates = 3;
    for (int i=recordsSize;i<recordsSize + numberOfInserts;i++) {
      TestgrouperSyncSubjectFrom testgrouperSyncSubjectFrom = new TestgrouperSyncSubjectFrom();
      testgrouperSyncSubjectFrom.setPersonId(i);
      testgrouperSyncSubjectFrom.setNetId("newnetId_" + i);
      testgrouperSyncSubjectFrom.setSomeInt(1+i);
      
      Calendar calendar = new GregorianCalendar();
      calendar.setTimeInMillis(date.getTimeInMillis());
      calendar.add(Calendar.DAY_OF_YEAR, i);
  
      testgrouperSyncSubjectFrom.setSomeDate(calendar.getTime()); // yyyy/mm/dd
      testgrouperSyncSubjectFrom.setSomeFloat(1.1d + i);
      
      calendar = new GregorianCalendar();
      calendar.setTimeInMillis(timestamp.getTimeInMillis());
      calendar.add(Calendar.DAY_OF_YEAR, i);
      
      testgrouperSyncSubjectFrom.setSomeTimestamp(new Timestamp(System.currentTimeMillis()));
      HibernateSession.byObjectStatic().save(testgrouperSyncSubjectFrom);
    }
    for (int i=0;i<numberOfUpdates;i++) {
      TestgrouperSyncSubjectFrom testgrouperSyncSubjectFrom = HibernateSession.byObjectStatic().load(TestgrouperSyncSubjectFrom.class, i);
      testgrouperSyncSubjectFrom.setNetId("newnetId_" + i);
      testgrouperSyncSubjectFrom.setSomeTimestamp(new Timestamp(System.currentTimeMillis()));
      HibernateSession.byObjectStatic().update(testgrouperSyncSubjectFrom);
    }
    for (int i=numberOfUpdates;i<numberOfUpdates + numberOfDeletes;i++) {
      TestgrouperSyncSubjectFrom testgrouperSyncSubjectFrom = HibernateSession.byObjectStatic().load(TestgrouperSyncSubjectFrom.class, i);
      HibernateSession.byObjectStatic().delete(testgrouperSyncSubjectFrom);
    }
    GrouperClientUtils.sleep(100);
  
    // ######################
    //do it again update some
    gcTableSync = new GcTableSync();  
    gcTableSyncOutput = gcTableSync.sync("personSourceTest", GcTableSyncSubtype.incrementalPrimaryKey); 
  
    // these dont happen on incremental all columns
    assertEquals(numberOfDeletes, gcTableSyncOutput.getDelete());
    assertEquals(numberOfUpdates, gcTableSyncOutput.getUpdate());
    assertEquals(numberOfUpdates + numberOfInserts, gcTableSyncOutput.getRowsSelectedFrom());
    assertEquals(numberOfInserts, gcTableSyncOutput.getInsert());
    
    for (int i=0;i<numberOfUpdates;i++) {
      TestgrouperSyncSubjectTo testgrouperSyncSubjectTo = HibernateSession.byObjectStatic().load(TestgrouperSyncSubjectTo.class, new Long(i));
      assertEquals("newnetId_" + i, testgrouperSyncSubjectTo.getNetId());
    }
    for (int i=recordsSize;i<recordsSize + numberOfInserts;i++) {
      TestgrouperSyncSubjectTo testgrouperSyncSubjectTo = HibernateSession.byObjectStatic().load(TestgrouperSyncSubjectTo.class, new Long(i));
      assertEquals("newnetId_" + i, testgrouperSyncSubjectTo.getNetId());
    }
    for (int i=numberOfUpdates;i<numberOfUpdates + numberOfDeletes;i++) {
      int rows = HibernateSession.bySqlStatic().select(int.class, "select count(*) from testgrouper_sync_subject_to where person_id = ?", 
          HibUtils.listObject(i), HibUtils.listType(IntegerType.INSTANCE));
      assertEquals(0, rows);
    }
    
    // ######################
    // do a full and nothing
    gcTableSync = new GcTableSync();    
    gcTableSyncOutput = gcTableSync.sync("personSourceTest", GcTableSyncSubtype.fullSyncFull); 
        
    assertEquals(0, gcTableSyncOutput.getDelete());
    assertEquals(0, gcTableSyncOutput.getUpdate());
    assertEquals(recordsSize + numberOfInserts - numberOfDeletes, gcTableSyncOutput.getRowsSelectedFrom());
    assertEquals(0, gcTableSyncOutput.getInsert());
    assertEquals(0, gcTableSyncOutput.getDelete());
    
  
    // ######################
    // incremental should do nothing
    gcTableSync = new GcTableSync();  
    gcTableSyncOutput = gcTableSync.sync("personSourceTest", GcTableSyncSubtype.incrementalPrimaryKey); 
  
    // these dont happen on incremental all columns
    assertEquals(0, gcTableSyncOutput.getDelete());
    assertEquals(0, gcTableSyncOutput.getUpdate());
    assertEquals(0, gcTableSyncOutput.getRowsSelectedFrom());
    assertEquals(0, gcTableSyncOutput.getInsert());
  
    // ######################
    // make changes, do a full and then an incremental, shouldnt do anything
    GrouperUtil.sleep(100);
    
    int numberOfNewInserts = 5;
    int numberOfNewDeletes = 6;
    int numberOfNewUpdates = 7;
    for (int i=recordsSize + numberOfInserts;i<recordsSize + numberOfInserts + numberOfNewInserts;i++) {
      TestgrouperSyncSubjectFrom testgrouperSyncSubjectFrom = new TestgrouperSyncSubjectFrom();
      testgrouperSyncSubjectFrom.setPersonId(i);
      testgrouperSyncSubjectFrom.setNetId("newnetId_" + i);
      testgrouperSyncSubjectFrom.setSomeInt(1+i);
      
      Calendar calendar = new GregorianCalendar();
      calendar.setTimeInMillis(date.getTimeInMillis());
      calendar.add(Calendar.DAY_OF_YEAR, i);
  
      testgrouperSyncSubjectFrom.setSomeDate(calendar.getTime()); // yyyy/mm/dd
      testgrouperSyncSubjectFrom.setSomeFloat(1.1d + i);
      
      calendar = new GregorianCalendar();
      calendar.setTimeInMillis(timestamp.getTimeInMillis());
      calendar.add(Calendar.DAY_OF_YEAR, i);
      
      testgrouperSyncSubjectFrom.setSomeTimestamp(new Timestamp(System.currentTimeMillis()));
      HibernateSession.byObjectStatic().save(testgrouperSyncSubjectFrom);
    }
    for (int i=numberOfUpdates + numberOfDeletes;i<numberOfUpdates + numberOfDeletes+ numberOfNewUpdates;i++) {
      TestgrouperSyncSubjectFrom testgrouperSyncSubjectFrom = HibernateSession.byObjectStatic().load(TestgrouperSyncSubjectFrom.class, i);
      testgrouperSyncSubjectFrom.setNetId("newnetId_" + i);
      testgrouperSyncSubjectFrom.setSomeTimestamp(new Timestamp(System.currentTimeMillis()));
      HibernateSession.byObjectStatic().update(testgrouperSyncSubjectFrom);
    }
    for (int i=numberOfUpdates + numberOfDeletes + numberOfNewUpdates;i<numberOfUpdates + numberOfDeletes + numberOfNewUpdates + numberOfNewDeletes;i++) {
      TestgrouperSyncSubjectFrom testgrouperSyncSubjectFrom = HibernateSession.byObjectStatic().load(TestgrouperSyncSubjectFrom.class, i);
      HibernateSession.byObjectStatic().delete(testgrouperSyncSubjectFrom);
    }
    GrouperUtil.sleep(100);
  
    gcTableSync = new GcTableSync();  
    gcTableSyncOutput = gcTableSync.sync("personSourceTest", GcTableSyncSubtype.fullSyncFull); 
  
    // these dont happen on incremental all columns
    assertEquals(numberOfNewDeletes, gcTableSyncOutput.getDelete());
    assertEquals(numberOfNewUpdates, gcTableSyncOutput.getUpdate());
    assertEquals(recordsSize + numberOfInserts + numberOfNewInserts - (numberOfDeletes + numberOfNewDeletes), gcTableSyncOutput.getRowsSelectedFrom());
    assertEquals(numberOfNewInserts, gcTableSyncOutput.getInsert());
    
    GrouperUtil.sleep(100);
  
    {
      int i=numberOfUpdates + numberOfDeletes + numberOfNewUpdates + numberOfNewDeletes;
      TestgrouperSyncSubjectFrom testgrouperSyncSubjectFrom = HibernateSession.byObjectStatic().load(TestgrouperSyncSubjectFrom.class, i);
      testgrouperSyncSubjectFrom.setNetId("newnetId_" + i);
      testgrouperSyncSubjectFrom.setSomeTimestamp(new Timestamp(System.currentTimeMillis()));
      HibernateSession.byObjectStatic().update(testgrouperSyncSubjectFrom);
    }
  
    GrouperUtil.sleep(100);
  
    // ######################
    // incremental should fix this one record since full just fixed everything
    gcTableSync = new GcTableSync();  
    gcTableSyncOutput = gcTableSync.sync("personSourceTest", GcTableSyncSubtype.incrementalPrimaryKey); 
  
    // these dont happen on incremental all columns
    assertEquals(0, gcTableSyncOutput.getDelete());
    assertEquals(1, gcTableSyncOutput.getUpdate());
    assertEquals(1, gcTableSyncOutput.getRowsSelectedFrom());
    assertEquals(0, gcTableSyncOutput.getInsert());
  
  
  }

}
