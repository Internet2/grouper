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

import junit.textui.TestRunner;

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
import edu.internet2.middleware.grouper.misc.GrouperCheckConfig;
import edu.internet2.middleware.grouper.misc.GrouperStartup;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcTableSync;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcTableSyncOutput;
import edu.internet2.middleware.grouperClient.util.GrouperClientConfig;


/**
 *
 */
public class TableSyncTest extends GrouperTest {

  public static void main(String[] args) {
    TestRunner.run(new TableSyncTest("testPersonSyncFull"));
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
      
      GrouperStartup.initLoaderType();
      
      setupTestConfigForIncludeExclude();
      
      GrouperStartup.initIncludeExcludeType();

      GrouperCheckConfig.checkObjects();
      
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
    
    //TODO uncomment: dropTableSyncTables();
    GrouperSession.stopQuietly(this.grouperSession);

  }

  /**
   * @see edu.internet2.middleware.grouper.helper.GrouperTest#setupConfigs()
   */
  @Override
  protected void setupConfigs() {
    super.setupConfigs();
    
    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("grouperClient.jdbc.grouper.driver.elConfig", "${edu.internet2.middleware.grouper.cfg.GrouperHibernateConfig.retrieveConfig().propertyValueString(\"hibernate.connection.driver_class\")}");
    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("grouperClient.jdbc.grouper.url.elConfig", "${edu.internet2.middleware.grouper.cfg.GrouperHibernateConfig.retrieveConfig().propertyValueString(\"hibernate.connection.url\")}");
    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("grouperClient.jdbc.grouper.user.elConfig", "${edu.internet2.middleware.grouper.cfg.GrouperHibernateConfig.retrieveConfig().propertyValueString(\"hibernate.connection.username\")}");
    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("grouperClient.jdbc.grouper.pass.elConfig", "${edu.internet2.middleware.grouper.cfg.GrouperHibernateConfig.retrieveConfig().propertyValueString(\"hibernate.connection.password\")}");
    
    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("grouperClient.syncTable.personSourceTest.databaseFrom", "grouper");
    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("grouperClient.syncTable.personSourceTest.tableFrom", "testgrouper_sync_subject_from");
    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("grouperClient.syncTable.personSourceTest.databaseTo", "grouper");
    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("grouperClient.syncTable.personSourceTest.tableTo", "testgrouper_sync_subject_to");
    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("grouperClient.syncTable.personSourceTest.columns", "*");
    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("grouperClient.syncTable.personSourceTest.primaryKeyColumns", "person_id");

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
   * 
   */
  public void ensureTableSyncTables() {
    //we need to delete the test table if it is there, and create a new one
    //drop field id col, first drop foreign keys
    GrouperDdlUtils.changeDatabase(GrouperTestDdl.V1.getObjectName(), new DdlUtilsChangeDatabase() {
  
      public void changeDatabase(DdlVersionBean ddlVersionBean) {
        
        Database database = ddlVersionBean.getDatabase();
  
        {
          Table loaderTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database,"testgrouper_sync_subject_from");
          
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
              "testgrouper_sync_subject_from", "sample table to SQL sync to another table");
      
        }
        {
          Table loaderTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database,"testgrouper_sync_subject_to");
          
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
              "testgrouper_sync_subject_to", "sample table to SQL sync from another table");
      
        }
      }
      
    });
  }

  /**
   * 
   */
  public void dropTableSyncTables() {
    //we need to delete the test table if it is there, and create a new one
    //drop field id col, first drop foreign keys
    GrouperDdlUtils.changeDatabase(GrouperTestDdl.V1.getObjectName(), new DdlUtilsChangeDatabase() {
  
      public void changeDatabase(DdlVersionBean ddlVersionBean) {
        
        Database database = ddlVersionBean.getDatabase();
  
        {
          Table loaderTable = database.findTable("testgrouper_sync_subject_from");
          
          if (loaderTable != null) {
            database.removeTable(loaderTable);
          }
        }
        
        {
          Table loaderTable = database.findTable("testgrouper_sync_subject_to");
          
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
    gcTableSync.setKey("personSourceTest");
    GcTableSyncOutput gcTableSyncOutput = gcTableSync.fullSync();

    assertEquals(0, gcTableSyncOutput.getDelete());
    assertEquals(0, gcTableSyncOutput.getUpdate());
    assertEquals(recordsSize, gcTableSyncOutput.getTotal());
    assertEquals(recordsSize, gcTableSyncOutput.getInsert());

    countTo = HibernateSession.bySqlStatic().select(int.class, "select count(*) from testgrouper_sync_subject_to");
    
    assertEquals(recordsSize, countTo);

    //do it again should do nothing
    gcTableSync = new GcTableSync();
    gcTableSync.setKey("personSourceTest");
    gcTableSyncOutput = gcTableSync.fullSync();

    assertEquals(0, gcTableSyncOutput.getDelete());
    assertEquals(0, gcTableSyncOutput.getUpdate());
    assertEquals(recordsSize, gcTableSyncOutput.getTotal());
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
    gcTableSync.setKey("personSourceTest");
    gcTableSyncOutput = gcTableSync.fullSync();

    assertEquals(1, gcTableSyncOutput.getDelete());
    assertEquals(1, gcTableSyncOutput.getUpdate());
    assertEquals(recordsSize, gcTableSyncOutput.getTotal());
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
