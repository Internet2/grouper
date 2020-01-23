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
import org.hibernate.ObjectNotFoundException;

import edu.internet2.middleware.grouper.GrouperAPI;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.ddl.DdlUtilsChangeDatabase;
import edu.internet2.middleware.grouper.ddl.DdlVersionBean;
import edu.internet2.middleware.grouper.ddl.GrouperDdlUtils;
import edu.internet2.middleware.grouper.ddl.GrouperTestDdl;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcTableSync;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcTableSyncOutput;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcTableSyncSubtype;
import edu.internet2.middleware.grouperClient.util.GrouperClientConfig;
import junit.textui.TestRunner;


/**
 *
 */
public class TableSyncTest extends GrouperTest {

  public static void main(String[] args) {
    TestRunner.run(new TableSyncTest("testPersonSyncFull"));
    
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
    // TODO
//    for (GcTableSyncColumnMetadata gcTableSyncColumnMetadata : GcTableSync.processDatabaseColumnMetadata("grouper", "grouper_groups")) {
//      if (StringUtils.equalsIgnoreCase(gcTableSyncColumnMetadata.getColumnName(), "name")) {
//        assertEquals(ColumnType.STRING, gcTableSyncColumnMetadata.getColumnType());
//        //System.out.println(gcTableSyncColumnMetadata.getColumnName());
//        foundName = true;
//      }
//    }
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
    //super.setUp();
    
    try {

      this.grouperSession = GrouperSession.startRootSession();
      
      //dropTableSyncTables();

      //ensureTableSyncTables();
  
      HibernateSession.byHqlStatic().createQuery("delete from TestgrouperSyncSubjectFrom").executeUpdate();
      HibernateSession.byHqlStatic().createQuery("delete from TestgrouperSyncSubjectTo").executeUpdate();
      
//      GrouperStartup.initLoaderType();
//      
//      setupTestConfigForIncludeExclude();
//      
//      GrouperStartup.initIncludeExcludeType();
//
//      GrouperCheckConfig.checkObjects();
      
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
    
    //dropTableSyncTables();
    GrouperSession.stopQuietly(this.grouperSession);

  }

  /**
   * @see edu.internet2.middleware.grouper.helper.GrouperTest#setupConfigs()
   */
  @Override
  protected void setupConfigs() {
    super.setupConfigs();
    
//    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("grouperClient.jdbc.grouper.driver.elConfig", "${edu.internet2.middleware.grouper.cfg.GrouperHibernateConfig.retrieveConfig().propertyValueString(\"hibernate.connection.driver_class\")}");
//    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("grouperClient.jdbc.grouper.url.elConfig", "${edu.internet2.middleware.grouper.cfg.GrouperHibernateConfig.retrieveConfig().propertyValueString(\"hibernate.connection.url\")}");
//    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("grouperClient.jdbc.grouper.user.elConfig", "${edu.internet2.middleware.grouper.cfg.GrouperHibernateConfig.retrieveConfig().propertyValueString(\"hibernate.connection.username\")}");
//    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("grouperClient.jdbc.grouper.pass.elConfig", "${edu.internet2.middleware.grouper.cfg.GrouperHibernateConfig.retrieveConfig().propertyValueString(\"hibernate.connection.password\")}");
    

    //  GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("", "");

    //  # column of date or timestamp which is last updated of row
    //  # {valueType: "string"}
    //  grouperClient.syncTable.personSourceTest.realTimeLastUpdatedCol = last_updated
    //
    //  # schema of real time table if different than connecting schema
    //  # {valueType: "string"}
    //  grouperClient.syncTable.personSourceTest.realTimeSchema = pcdadmin
    //
    //  # table of real time change log table
    //  # {valueType: "string"}
    //  grouperClient.syncTable.personSourceTest.realTimeTable = computed_person_chglog
    //
    //  # database which holds table to keep status of real time and full sync
    //  # {valueType: "string"}
    //  grouperClient.syncTable.personSourceTest.statusDatabase = awsDev
    //
    //  # schema of status table if different than connecting schema
    //  # {valueType: "string"}
    //  grouperClient.syncTable.personSourceTest.statusSchema = 
    //
    //  # table of status where real time and full sync status is
    //  # {valueType: "string"}
    //  grouperClient.syncTable.personSourceTest.statusTable = grouper_chance_log_consumer
    //
    //  # if doing real time and nightly full sync, this is the hour where full sync should start
    //  # {valueType: "integer"}
    //  grouperClient.syncTable.personSourceTest.fullSyncHourStart = 3
    //
    //  # if doing real time and nightly full sync, this is the hour where full sync should have already started
    //  # {valueType: "integer"}
    //  grouperClient.syncTable.personSourceTest.fullSyncHourEnd = 4
  
  }

  /**
   * @param ddlVersionBean
   * @param database
   * @param tableName
   */
  public void createTable(DdlVersionBean ddlVersionBean, Database database, String tableName) {

    Table loaderTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, tableName);
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "person_id", 
        Types.VARCHAR, "8", true, true);
 
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "hibernate_version_number", 
        Types.INTEGER, "10", true, true);

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
  
        createTable(ddlVersionBean, database, "testgrouper_sync_subject_from");

        createTable(ddlVersionBean, database, "testgrouper_sync_subject_to");

      }
      
    });
  }

  /**
   * 
   */
  public void dropTableSyncTables() {
    
    dropTableSyncTable("testgrouper_sync_subject_from");
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
  public void testPersonSyncFull() {
    
    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("grouperClient.syncTable.personSourceTest.databaseFrom", "grouper");
    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("grouperClient.syncTable.personSourceTest.tableFrom", "testgrouper_sync_subject_from");
    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("grouperClient.syncTable.personSourceTest.databaseTo", "grouper");
    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("grouperClient.syncTable.personSourceTest.tableTo", "testgrouper_sync_subject_to");
    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("grouperClient.syncTable.personSourceTest.columns", "*");
    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("grouperClient.syncTable.personSourceTest.primaryKeyColumns", "person_id");

// TODO    
//    # grouper client or loader database key (readonly) if large queries should be performed against a different database
//    # {valueType: "string"}
//    #grouperClient.syncTable.personSource.databaseToReadonly = 

 // TODO    
//    # if doing fullSyncChangeFlag (look for a col that says if the rows are equal, e.g. a timestamp or a checksum)
//    # {valueType: "string"}
//    # grouperClient.syncTable.personSource.fullSyncChangeFlagColumn = check_sum

// TODO    
//    # the grouping column is what is uniquely selected, and then batched through to get data.  Optional.
//    # for groups this should be the group uuid
//    # {valueType: "string"}
//    # grouperClient.syncTable.personSource.groupingColumn = penn_id

// TODO    
//    # the grouping column is what is uniquely selected, and then batched through to get data, defaults to 10000
//    # {valueType: "integer"}
//    # grouperClient.syncTable.personSource.groupingSize = 10000

// TODO    
//    # size of jdbc batches
//    # {valueType: "integer"}
//    # grouperClient.syncTable.personSource.batchSize = 800

// TODO    
//    # if querying a real time table, this is the table, needs to have primary key columns.
//    # each record will check the source and destination and see what to do
//    # {valueType: "string"}
//    # grouperClient.syncTable.personSource.incrementalPrimaryKeyTable = real_time_table

// TODO    
//    # name of a column that has a sequence or last updated date
//    # {valueType: "string", multiple: true}
//    # grouperClient.syncTable.personSource.incrementalAllColumnsColumn = lastUpdated

// TODO    
//    # database where status table is.  defaults to "grouper"
//    # {valueType: "string"}
//    # grouperClient.syncTable.personSource.statusDatabase = grouper

// TODO    
//    # dont run if has run in last X minutes (e.g. for full syncs maybe set to 6 hours = 360 minutes)
//    # {valueType: "string"}
//    # grouperClient.syncTable.personSource.dontRunIfHasRunInLastMinutes = 
    
    
    
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
    
    //int recordsSize = 25000; TODO
    int recordsSize = 250;
    
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
    gcTableSync.configure("personSourceTest", GcTableSyncSubtype.fullSyncFull);

    GcTableSyncOutput gcTableSyncOutput = new GcTableSyncOutput();
    gcTableSync.sync(gcTableSyncOutput); 

    assertEquals(0, gcTableSyncOutput.getDelete());
    assertEquals(0, gcTableSyncOutput.getUpdate());
    assertEquals(recordsSize, gcTableSyncOutput.getRowsSelectedFrom());
    assertEquals(recordsSize, gcTableSyncOutput.getInsert());

    countTo = HibernateSession.bySqlStatic().select(int.class, "select count(*) from testgrouper_sync_subject_to");

    assertEquals(recordsSize, countTo);

    //do it again should do nothing
    gcTableSync = new GcTableSync();
    gcTableSync.configure("personSourceTest", GcTableSyncSubtype.fullSyncFull);
    
    gcTableSyncOutput = new GcTableSyncOutput();
    gcTableSync.sync(gcTableSyncOutput); 

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
    gcTableSync.configure("personSourceTest", GcTableSyncSubtype.fullSyncFull);
    
    gcTableSyncOutput = new GcTableSyncOutput();
    gcTableSync.sync(gcTableSyncOutput); 
        
    assertEquals(1, gcTableSyncOutput.getDelete());
    assertEquals(1, gcTableSyncOutput.getUpdate());
    assertEquals(recordsSize, gcTableSyncOutput.getRowsSelectedFrom());
    assertEquals(1, gcTableSyncOutput.getInsert());
    
    try {
      HibernateSession.byObjectStatic().load(TestgrouperSyncSubjectTo.class, "0");
      throw new RuntimeException("Shouldnt be found");
    } catch (GrouperDAOException onfe) {
      // good
    } catch (ObjectNotFoundException onfe) {
      // good
    }
    
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

}
