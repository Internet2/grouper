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
import org.hibernate.type.StringType;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperAPI;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningAttributeNames;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningSettings;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.AttributeDefType;
import edu.internet2.middleware.grouper.ddl.DdlUtilsChangeDatabase;
import edu.internet2.middleware.grouper.ddl.DdlVersionBean;
import edu.internet2.middleware.grouper.ddl.GrouperDdlUtils;
import edu.internet2.middleware.grouper.ddl.GrouperTestDdl;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.hibernate.HibUtils;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.GrouperStartup;
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
    TestRunner.run(new ProvisioningToSyncTest("testPersonSyncIncrementalSwitchToGroup"));
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
  
      HibernateSession.byHqlStatic().createQuery("delete from TestgrouperSyncSubjectFrom").executeUpdate();
      HibernateSession.byHqlStatic().createQuery("delete from TestgrouperSyncSubjectTo").executeUpdate();
      HibernateSession.byHqlStatic().createQuery("delete from TestgrouperSyncChangeLog").executeUpdate();
      
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
  public void createTable(DdlVersionBean ddlVersionBean, Database database, String tableName) {

    Table loaderTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, tableName);
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "person_id", 
        Types.VARCHAR, "8", true, true);
 
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
  
        createTable(ddlVersionBean, database, "testgrouper_sync_subject_from");

        createTable(ddlVersionBean, database, "testgrouper_sync_subject_to");

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
   * add provisioning attributes and see them transition to group sync attribute
   */
  public void testProvisioningAttributesToGroupSyncFull() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    Stem testStem = new StemSave(grouperSession).assignName("test").save();
    
    Group testGroup1 = new GroupSave(grouperSession).assignName("test:testGroup1").save();
    
    Group testGroup2 = new GroupSave(grouperSession).assignName("test:testGroup2").save();
    
    Group testGroup3 = new GroupSave(grouperSession).assignName("test:testGroup3").save();

    Group testGroup4 = new GroupSave(grouperSession).assignName("test:test2:testGroup4").assignCreateParentStemsIfNotExist(true).save();

    
//    String provGrouperProvisioningSettings.provisioningConfigStemName();
//    
//    Stem grouperProvisioningStemName = StemFinder.findByName(grouperSession, grouperProvisioningUiRootStemName, false);
//    if (grouperProvisioningStemName == null) {
//      grouperProvisioningStemName = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true)
//        .assignDescription("folder to store attribute defs and names for provisioning in ui").assignName(grouperProvisioningUiRootStemName)
//        .save();
//    }
//
//    //see if attributeDef is there
//    String provisioningDefName = grouperProvisioningUiRootStemName + ":" + GrouperProvisioningAttributeNames.PROVISIONING_DEF;
//    AttributeDef provisioningDef = GrouperDAOFactory.getFactory().getAttributeDef().findByNameSecure(
//        provisioningDefName, false, new QueryOptions().secondLevelCache(false));
//    if (provisioningDef == null) {
//      provisioningDef = grouperProvisioningStemName.addChildAttributeDef(GrouperProvisioningAttributeNames.PROVISIONING_DEF, AttributeDefType.type);
//      //assign once for each target
//      provisioningDef.setMultiAssignable(true);
//      provisioningDef.setAssignToGroup(true);
//      provisioningDef.setAssignToStem(true);
//      provisioningDef.store();
//    }
//    
//    //add a name
//    AttributeDefName attribute = checkAttribute(grouperProvisioningStemName, provisioningDef, GrouperProvisioningAttributeNames.PROVISIONING_ATTRIBUTE_NAME, "has provisioning attributes", wasInCheckConfig);
//    
//    //lets add some rule attributes
//    String provisioningValueAttrDefName = grouperProvisioningUiRootStemName + ":" + GrouperProvisioningAttributeNames.PROVISIONING_VALUE_DEF
    
    
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

}
