package edu.internet2.middleware.grouper.app.syncToGrouper;

import java.sql.Types;

import org.apache.commons.lang.StringUtils;
import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.model.Table;

import edu.internet2.middleware.grouper.CompositeSave;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.app.sqlProvisioning.SqlProvisionerTest;
import edu.internet2.middleware.grouper.ddl.DdlUtilsChangeDatabase;
import edu.internet2.middleware.grouper.ddl.DdlVersionBean;
import edu.internet2.middleware.grouper.ddl.GrouperDdlUtils;
import edu.internet2.middleware.grouper.ddl.GrouperTestDdl;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.internal.util.GrouperUuid;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import junit.textui.TestRunner;


public class SyncToGrouperTest extends GrouperTest {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    
    TestRunner.run(new SyncToGrouperTest("testSyncCompositeDb"));
    
  }
  
  public SyncToGrouperTest() {
    super();
  }

  /**
   * 
   * @param name
   */
  public SyncToGrouperTest(String name) {
    super(name);
  }

  public void testSyncStemNamesAndIds() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    SyncToGrouper syncToGrouper = new SyncToGrouper();
    SyncToGrouperReport syncToGrouperReport = syncToGrouper.syncLogic();
    
    assertTrue(syncToGrouperReport.getOutputLines().contains(SyncStemToGrouperLogic.STEM_SYNC_FALSE));
    assertEquals(0, syncToGrouperReport.getStemInserts());

    // ##############
    syncToGrouper = new SyncToGrouper();
    syncToGrouper.getSyncToGrouperBehavior().setStemSync(true);
    syncToGrouperReport = syncToGrouper.syncLogic();

    assertFalse(syncToGrouperReport.getOutputLines().contains(SyncStemToGrouperLogic.STEM_SYNC_FALSE));
    assertTrue(syncToGrouperReport.getOutputLines().contains(SyncStemToGrouperLogic.NO_FOLDERS_TO_SYNC));
    assertEquals(0, syncToGrouperReport.getStemInserts());

    // ##############
    syncToGrouper = new SyncToGrouper();
    syncToGrouper.getSyncToGrouperBehavior().setStemSync(true);
    syncToGrouper.setSyncStemToGrouperBeans(GrouperUtil.toList(new SyncStemToGrouperBean("test"), new SyncStemToGrouperBean("test:testStem")));
    syncToGrouperReport = syncToGrouper.syncLogic();
    
    assertEquals(1, GrouperUtil.length(syncToGrouper.getSyncStemToGrouperLogic().getTopLevelStemNamesToSync().size()));
    assertTrue(syncToGrouper.getSyncStemToGrouperLogic().getTopLevelStemNamesToSync().contains("test"));

    assertEquals(0, GrouperUtil.length(syncToGrouper.getSyncStemToGrouperLogic().getGrouperStemNameToStem()));

    assertEquals(0, syncToGrouperReport.getStemInserts());

    // ##############
    syncToGrouper = new SyncToGrouper();
    syncToGrouper.getSyncToGrouperBehavior().setStemSync(true);
    syncToGrouper.getSyncToGrouperBehavior().setStemInsert(true);
    syncToGrouper.setSyncStemToGrouperBeans(GrouperUtil.toList(new SyncStemToGrouperBean("test"), new SyncStemToGrouperBean("test:testStem")));
    syncToGrouperReport = syncToGrouper.syncLogic();
    
    assertEquals(1, GrouperUtil.length(syncToGrouper.getSyncStemToGrouperLogic().getTopLevelStemNamesToSync().size()));
    assertTrue(syncToGrouper.getSyncStemToGrouperLogic().getTopLevelStemNamesToSync().contains("test"));

    assertEquals(0, GrouperUtil.length(syncToGrouper.getSyncStemToGrouperLogic().getGrouperStemNameToStem()));

    assertEquals(2, syncToGrouperReport.getStemInserts());
    assertEquals(2, syncToGrouperReport.getDifferenceCountOverall());
    assertEquals(2, GrouperUtil.length(syncToGrouperReport.getStemInsertsNames()));
    assertTrue(syncToGrouperReport.getStemInsertsNames().contains("test"));
    assertTrue(syncToGrouperReport.getStemInsertsNames().contains("test:testStem"));
    
    assertNull(StemFinder.findByName(grouperSession, "test", false));
    assertNull(StemFinder.findByName(grouperSession, "test:testStem", false));
    
    assertTrue(syncToGrouper.isSuccess());
    
    // ##############
    syncToGrouper = new SyncToGrouper();
    syncToGrouper.setReadWrite(true);
    syncToGrouper.getSyncToGrouperBehavior().setStemSync(true);
    syncToGrouper.getSyncToGrouperBehavior().setStemInsert(true);
    syncToGrouper.setSyncStemToGrouperBeans(GrouperUtil.toList(new SyncStemToGrouperBean("test"), new SyncStemToGrouperBean("test:testStem")));
    syncToGrouperReport = syncToGrouper.syncLogic();
    
    assertEquals(1, GrouperUtil.length(syncToGrouper.getSyncStemToGrouperLogic().getTopLevelStemNamesToSync().size()));
    assertTrue(syncToGrouper.getSyncStemToGrouperLogic().getTopLevelStemNamesToSync().contains("test"));

    assertEquals(0, GrouperUtil.length(syncToGrouper.getSyncStemToGrouperLogic().getGrouperStemNameToStem()));

    assertEquals(2, syncToGrouperReport.getDifferenceCountOverall());
    assertEquals(2, syncToGrouperReport.getChangeCountOverall());
    assertEquals(0, syncToGrouperReport.getErrorLines().size());
    assertEquals(2, syncToGrouperReport.getStemInserts());
    assertEquals(2, GrouperUtil.length(syncToGrouperReport.getStemInsertsNames()));
    assertTrue(syncToGrouperReport.getStemInsertsNames().contains("test"));
    assertTrue(syncToGrouperReport.getStemInsertsNames().contains("test:testStem"));
    
    Stem stemTest = StemFinder.findByName(grouperSession, "test", true);
    Stem stemTestStem = StemFinder.findByName(grouperSession, "test:testStem", true);

    assertTrue(syncToGrouper.isSuccess());

    // ##############
    syncToGrouper = new SyncToGrouper();
    syncToGrouper.setReadWrite(true);
    syncToGrouper.getSyncToGrouperBehavior().setStemSync(true);
    syncToGrouper.getSyncToGrouperBehavior().setStemInsert(true);
    syncToGrouper.setSyncStemToGrouperBeans(GrouperUtil.toList(new SyncStemToGrouperBean("test"), new SyncStemToGrouperBean("test:testStem")));
    syncToGrouperReport = syncToGrouper.syncLogic();
    
    assertEquals(1, GrouperUtil.length(syncToGrouper.getSyncStemToGrouperLogic().getTopLevelStemNamesToSync().size()));
    assertTrue(syncToGrouper.getSyncStemToGrouperLogic().getTopLevelStemNamesToSync().contains("test"));

    assertEquals(2, GrouperUtil.length(syncToGrouper.getSyncStemToGrouperLogic().getGrouperStemNameToStem()));

    assertEquals(0, syncToGrouperReport.getDifferenceCountOverall());
    assertEquals(0, syncToGrouperReport.getChangeCountOverall());
    assertEquals(0, syncToGrouperReport.getErrorLines().size());
    assertEquals(0, syncToGrouperReport.getStemInserts());
    assertEquals(0, GrouperUtil.length(syncToGrouperReport.getStemInsertsNames()));
    
    stemTest = StemFinder.findByName(grouperSession, "test", true);
    stemTestStem = StemFinder.findByName(grouperSession, "test:testStem", true);

    assertTrue(syncToGrouper.isSuccess());

    // ##############
    
    stemTest.obliterate(false, false, false);
    
    syncToGrouper = new SyncToGrouper();
    syncToGrouper.setReadWrite(true);
    syncToGrouper.getSyncToGrouperBehavior().setStemSync(true);
    syncToGrouper.getSyncToGrouperBehavior().setStemInsert(true);
    syncToGrouper.getSyncToGrouperBehavior().setStemSyncFieldIdOnInsert(true);
    String testUuid = GrouperUuid.getUuid();
    String testStemUuid = GrouperUuid.getUuid();
    
    syncToGrouper.setSyncStemToGrouperBeans(
        GrouperUtil.toList(new SyncStemToGrouperBean("test").assignId(testUuid), new SyncStemToGrouperBean("test:testStem").assignId(testStemUuid)));
    syncToGrouperReport = syncToGrouper.syncLogic();
    
    assertEquals(1, GrouperUtil.length(syncToGrouper.getSyncStemToGrouperLogic().getTopLevelStemNamesToSync().size()));
    assertTrue(syncToGrouper.getSyncStemToGrouperLogic().getTopLevelStemNamesToSync().contains("test"));

    assertEquals(0, GrouperUtil.length(syncToGrouper.getSyncStemToGrouperLogic().getGrouperStemNameToStem()));

    assertEquals(2, syncToGrouperReport.getDifferenceCountOverall());
    assertEquals(2, syncToGrouperReport.getChangeCountOverall());
    assertEquals(0, syncToGrouperReport.getErrorLines().size());
    assertEquals(2, syncToGrouperReport.getStemInserts());
    assertEquals(2, GrouperUtil.length(syncToGrouperReport.getStemInsertsNames()));
    assertTrue(syncToGrouperReport.getStemInsertsNames().contains("test"));
    assertTrue(syncToGrouperReport.getStemInsertsNames().contains("test:testStem"));
    
    stemTest = StemFinder.findByName(grouperSession, "test", true);
    assertEquals(testUuid, stemTest.getId());
    stemTestStem = StemFinder.findByName(grouperSession, "test:testStem", true);
    assertEquals(testStemUuid, stemTestStem.getId());

    assertTrue(syncToGrouper.isSuccess());

  }

  public void testSyncStemDescription() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    SyncToGrouper syncToGrouper = new SyncToGrouper();
    syncToGrouper.setReadWrite(true);
    syncToGrouper.getSyncToGrouperBehavior().setStemSync(true);
    syncToGrouper.getSyncToGrouperBehavior().setStemInsert(true);
    syncToGrouper.getSyncToGrouperBehavior().setStemUpdate(true);
    syncToGrouper.getSyncToGrouperBehavior().setStemSyncFieldIdOnInsert(true);
    syncToGrouper.getSyncToGrouperBehavior().setStemSyncFieldDescription(true);
    
    String testUuid = GrouperUuid.getUuid();
    String testStemUuid = GrouperUuid.getUuid();
    
    syncToGrouper.setSyncStemToGrouperBeans(
        GrouperUtil.toList(new SyncStemToGrouperBean("test").assignId(testUuid).assignDescription("testDesc1"), 
                           new SyncStemToGrouperBean("test:testStem").assignId(testStemUuid).assignDescription("testStemDesc1")));
    SyncToGrouperReport syncToGrouperReport = syncToGrouper.syncLogic();
    
    assertEquals(1, GrouperUtil.length(syncToGrouper.getSyncStemToGrouperLogic().getTopLevelStemNamesToSync().size()));
    assertTrue(syncToGrouper.getSyncStemToGrouperLogic().getTopLevelStemNamesToSync().contains("test"));

    assertEquals(0, GrouperUtil.length(syncToGrouper.getSyncStemToGrouperLogic().getGrouperStemNameToStem()));

    assertEquals(2, syncToGrouperReport.getDifferenceCountOverall());
    assertEquals(2, syncToGrouperReport.getChangeCountOverall());
    assertEquals(0, syncToGrouperReport.getErrorLines().size());
    assertEquals(2, syncToGrouperReport.getStemInserts());
    assertEquals(2, GrouperUtil.length(syncToGrouperReport.getStemInsertsNames()));
    assertTrue(syncToGrouperReport.getStemInsertsNames().contains("test"));
    assertTrue(syncToGrouperReport.getStemInsertsNames().contains("test:testStem"));
    
    Stem stemTest = StemFinder.findByName(grouperSession, "test", true);
    assertEquals(testUuid, stemTest.getId());
    assertEquals("testDesc1", stemTest.getDescription());
    Stem stemTestStem = StemFinder.findByName(grouperSession, "test:testStem", true);
    assertEquals(testStemUuid, stemTestStem.getId());
    assertEquals("testStemDesc1", stemTestStem.getDescription());

    assertTrue(syncToGrouper.isSuccess());

    // #############################
    
    syncToGrouper = new SyncToGrouper();
    syncToGrouper.setReadWrite(true);
    syncToGrouper.getSyncToGrouperBehavior().setStemSync(true);
    syncToGrouper.getSyncToGrouperBehavior().setStemInsert(true);
    syncToGrouper.getSyncToGrouperBehavior().setStemUpdate(true);
    syncToGrouper.getSyncToGrouperBehavior().setStemSyncFieldIdOnInsert(true);
    syncToGrouper.getSyncToGrouperBehavior().setStemSyncFieldDescription(true);
    
    syncToGrouper.setSyncStemToGrouperBeans(
        GrouperUtil.toList(new SyncStemToGrouperBean("test").assignId(testUuid).assignDescription("testDesc2"), 
                           new SyncStemToGrouperBean("test:testStem").assignId(testStemUuid).assignDescription("testStemDesc1")));
    syncToGrouperReport = syncToGrouper.syncLogic();
    
    assertEquals(1, GrouperUtil.length(syncToGrouper.getSyncStemToGrouperLogic().getTopLevelStemNamesToSync().size()));
    assertTrue(syncToGrouper.getSyncStemToGrouperLogic().getTopLevelStemNamesToSync().contains("test"));

    assertEquals(2, GrouperUtil.length(syncToGrouper.getSyncStemToGrouperLogic().getGrouperStemNameToStem()));

    assertEquals(1, syncToGrouperReport.getDifferenceCountOverall());
    assertEquals(1, syncToGrouperReport.getChangeCountOverall());
    assertEquals(0, syncToGrouperReport.getErrorLines().size());
    assertEquals(0, syncToGrouperReport.getStemInserts());
    assertEquals(1, syncToGrouperReport.getStemUpdates());
    assertEquals(1, GrouperUtil.length(syncToGrouperReport.getStemUpdatesNames()));
    assertTrue(syncToGrouperReport.getStemUpdatesNames().contains("test"));
    
    stemTest = StemFinder.findByName(grouperSession, "test", true);
    assertEquals(testUuid, stemTest.getId());
    assertEquals("testDesc2", stemTest.getDescription());
    stemTestStem = StemFinder.findByName(grouperSession, "test:testStem", true);
    assertEquals(testStemUuid, stemTestStem.getId());
    assertEquals("testStemDesc1", stemTestStem.getDescription());

    assertTrue(syncToGrouper.isSuccess());

    
    
    
  }


  public void dropTables() {
    SqlProvisionerTest.dropTableSyncTable("testgrouper_syncgr_stem");
    SqlProvisionerTest.dropTableSyncTable("testgrouper_syncgr_group");
    SqlProvisionerTest.dropTableSyncTable("testgrouper_syncgr_composite");
  }

  public void createTables() {
    createTableStem();
    createTableGroup();
    createTableComposite();
  }

  /**
   * @param ddlVersionBean
   * @param database
   */
  public void createTableStem() {
  
    final String tableName = "testgrouper_syncgr_stem";
  
    try {
      new GcDbAccess().sql("select count(*) from " + tableName).select(int.class);
    } catch (Exception e) {
      //we need to delete the test table if it is there, and create a new one
      //drop field id col, first drop foreign keys
      GrouperDdlUtils.changeDatabase(GrouperTestDdl.V1.getObjectName(), new DdlUtilsChangeDatabase() {
    
        public void changeDatabase(DdlVersionBean ddlVersionBean) {
          
          Database database = ddlVersionBean.getDatabase();
    
          
          Table loaderTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, tableName);
          
          GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "name", Types.VARCHAR, "1024", true, true);

          GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "id_index", Types.BIGINT, "10", false, false);

          GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "id", Types.VARCHAR, "40", false, false);

          GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "display_name", Types.VARCHAR, "1024", false, false);

          GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "description", Types.VARCHAR, "1024", false, false);

          GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "alternate_name", Types.VARCHAR, "256", false, false);
        }
        
      });
    }
  }
  
  /**
   * @param ddlVersionBean
   * @param database
   */
  public void createTableGroup() {
  
    final String tableName = "testgrouper_syncgr_group";
  
    try {
      new GcDbAccess().sql("select count(*) from " + tableName).select(int.class);
    } catch (Exception e) {
      //we need to delete the test table if it is there, and create a new one
      //drop field id col, first drop foreign keys
      GrouperDdlUtils.changeDatabase(GrouperTestDdl.V1.getObjectName(), new DdlUtilsChangeDatabase() {
    
        public void changeDatabase(DdlVersionBean ddlVersionBean) {
          
          Database database = ddlVersionBean.getDatabase();
          
          Table loaderTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, tableName);
          
          GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "name", Types.VARCHAR, "1024", true, true);

          GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "alternate_name", Types.VARCHAR, "256", false, false);

          GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "description", Types.VARCHAR, "1024", false, false);

          GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "disabled_timestamp", Types.BIGINT, "10", false, false);

          GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "display_name", Types.VARCHAR, "1024", false, false);

          GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "enabled_timestamp", Types.BIGINT, "10", false, false);

          GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "id", Types.VARCHAR, "40", false, false);

          GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "id_index", Types.BIGINT, "10", false, false);

          GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "type_of_group", Types.VARCHAR, "10", false, false);
        }
        
      });
    }
  }
  
  public void testSyncStemNameIdDb() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    dropTables();
    createTables();
    
    SyncToGrouper syncToGrouper = new SyncToGrouper();
    SyncToGrouperReport syncToGrouperReport = syncToGrouper.syncLogic();
    
    // ##############
    syncToGrouper = new SyncToGrouper();
    syncToGrouper.setReadWrite(true);
    syncToGrouper.getSyncToGrouperBehavior().setStemSync(true);
    syncToGrouper.getSyncToGrouperBehavior().setStemInsert(true);
    syncToGrouper.getSyncToGrouperBehavior().setStemSyncFieldIdOnInsert(true);
    syncToGrouper.getSyncToGrouperBehavior().setStemSyncFieldIdIndexOnInsert(true);
    syncToGrouper.getSyncToGrouperBehavior().setSqlLoad(true);
    syncToGrouper.getSyncToGrouperFromSql().setDatabaseConfigId("grouper");
    syncToGrouper.getSyncToGrouperFromSql().setStemSql("select * from testgrouper_syncgr_stem");
    
    String testUuid = GrouperUuid.getUuid();
    SyncStemToGrouperBean syncStemToGrouperBeanTest = new SyncStemToGrouperBean("test").assignId(testUuid).assignIdIndex(800L);
    syncStemToGrouperBeanTest.store();

    String testStemUuid = GrouperUuid.getUuid();
    SyncStemToGrouperBean syncStemToGrouperBeanTestStem = new SyncStemToGrouperBean("test:testStem").assignId(testStemUuid);
    syncStemToGrouperBeanTestStem.store();
    
    syncToGrouperReport = syncToGrouper.syncLogic();
    
    assertEquals(1, GrouperUtil.length(syncToGrouper.getSyncStemToGrouperLogic().getTopLevelStemNamesToSync().size()));
    assertTrue(syncToGrouper.getSyncStemToGrouperLogic().getTopLevelStemNamesToSync().contains("test"));
  
    assertEquals(0, GrouperUtil.length(syncToGrouper.getSyncStemToGrouperLogic().getGrouperStemNameToStem()));
  
    assertEquals(2, syncToGrouperReport.getDifferenceCountOverall());
    assertEquals(2, syncToGrouperReport.getChangeCountOverall());
    assertEquals(0, syncToGrouperReport.getErrorLines().size());
    assertEquals(2, syncToGrouperReport.getStemInserts());
    assertEquals(2, GrouperUtil.length(syncToGrouperReport.getStemInsertsNames()));
    assertTrue(syncToGrouperReport.getStemInsertsNames().contains("test"));
    assertTrue(syncToGrouperReport.getStemInsertsNames().contains("test:testStem"));
    
    Stem stemTest = StemFinder.findByName(grouperSession, "test", true);
    assertEquals(testUuid, stemTest.getId());
    assertEquals(new Long(800L), stemTest.getIdIndex());

    Stem stemTestStem = StemFinder.findByName(grouperSession, "test:testStem", true);
    assertEquals(testStemUuid, stemTestStem.getId());
  
    assertTrue(syncToGrouper.isSuccess());
  
    // #############################
    
    syncToGrouper = new SyncToGrouper();
    syncToGrouper.setReadWrite(true);
    syncToGrouper.getSyncToGrouperBehavior().setStemSync(true);
    syncToGrouper.getSyncToGrouperBehavior().setStemInsert(true);
    syncToGrouper.getSyncToGrouperBehavior().setStemUpdate(true);
    syncToGrouper.getSyncToGrouperBehavior().setStemSyncFieldIdOnInsert(true);
    syncToGrouper.getSyncToGrouperBehavior().setStemSyncFieldDescription(true);
    syncToGrouper.getSyncToGrouperBehavior().setSqlLoad(true);
    syncToGrouper.getSyncToGrouperFromSql().setDatabaseConfigId("grouper");
    syncToGrouper.getSyncToGrouperFromSql().setStemSql("select * from testgrouper_syncgr_stem");
    
    syncStemToGrouperBeanTest.assignDescription("testDesc1").store();
    
    syncToGrouperReport = syncToGrouper.syncLogic();
    
    assertEquals(1, GrouperUtil.length(syncToGrouper.getSyncStemToGrouperLogic().getTopLevelStemNamesToSync().size()));
    assertTrue(syncToGrouper.getSyncStemToGrouperLogic().getTopLevelStemNamesToSync().contains("test"));

    assertEquals(2, GrouperUtil.length(syncToGrouper.getSyncStemToGrouperLogic().getGrouperStemNameToStem()));

    assertEquals(1, syncToGrouperReport.getDifferenceCountOverall());
    assertEquals(1, syncToGrouperReport.getChangeCountOverall());
    assertEquals(0, syncToGrouperReport.getErrorLines().size());
    assertEquals(0, syncToGrouperReport.getStemInserts());
    assertEquals(1, syncToGrouperReport.getStemUpdates());
    assertEquals(1, GrouperUtil.length(syncToGrouperReport.getStemUpdatesNames()));
    assertTrue(syncToGrouperReport.getStemUpdatesNames().contains("test"));
    
    stemTest = StemFinder.findByName(grouperSession, "test", true);
    assertEquals(testUuid, stemTest.getId());
    assertEquals("testDesc1", stemTest.getDescription());
    stemTestStem = StemFinder.findByName(grouperSession, "test:testStem", true);
    assertEquals(testStemUuid, stemTestStem.getId());
    assertTrue(StringUtils.isBlank(stemTestStem.getDescription()));

    assertTrue(syncToGrouper.isSuccess());

    // #############################

    syncToGrouper = new SyncToGrouper();
    syncToGrouper.setReadWrite(true);
    syncToGrouper.getSyncToGrouperBehavior().setStemSync(true);
    syncToGrouper.getSyncToGrouperBehavior().setStemInsert(true);
    syncToGrouper.getSyncToGrouperBehavior().setStemUpdate(true);
    syncToGrouper.getSyncToGrouperBehavior().setStemSyncFieldIdOnInsert(true);
    syncToGrouper.getSyncToGrouperBehavior().setStemSyncFieldDisplayName(true);
    syncToGrouper.getSyncToGrouperBehavior().setStemSyncFieldDescription(true);
    syncToGrouper.getSyncToGrouperBehavior().setSqlLoad(true);
    syncToGrouper.getSyncToGrouperFromSql().setDatabaseConfigId("grouper");
    syncToGrouper.getSyncToGrouperFromSql().setStemSql("select * from testgrouper_syncgr_stem");

    syncStemToGrouperBeanTest.assignDisplayName("Test").store();
    
    syncToGrouperReport = syncToGrouper.syncLogic();
    
    assertEquals(1, GrouperUtil.length(syncToGrouper.getSyncStemToGrouperLogic().getTopLevelStemNamesToSync().size()));
    assertTrue(syncToGrouper.getSyncStemToGrouperLogic().getTopLevelStemNamesToSync().contains("test"));
  
    assertEquals(2, GrouperUtil.length(syncToGrouper.getSyncStemToGrouperLogic().getGrouperStemNameToStem()));
  
    assertEquals(1, syncToGrouperReport.getDifferenceCountOverall());
    assertEquals(1, syncToGrouperReport.getChangeCountOverall());
    assertEquals(0, syncToGrouperReport.getErrorLines().size());
    assertEquals(0, syncToGrouperReport.getStemInserts());
    assertEquals(1, syncToGrouperReport.getStemUpdates());
    assertEquals(1, GrouperUtil.length(syncToGrouperReport.getStemUpdatesNames()));
    assertTrue(syncToGrouperReport.getStemUpdatesNames().contains("test"));
    
    stemTest = StemFinder.findByName(grouperSession, "test", true);
    assertEquals(testUuid, stemTest.getId());
    assertEquals("Test", stemTest.getDisplayName());
    assertEquals("testDesc1", stemTest.getDescription());
    stemTestStem = StemFinder.findByName(grouperSession, "test:testStem", true);
    assertEquals(testStemUuid, stemTestStem.getId());
    assertTrue(StringUtils.isBlank(stemTestStem.getDescription()));
    assertEquals("Test:testStem", stemTestStem.getDisplayName());
  
    assertTrue(syncToGrouper.isSuccess());

    // #############################

    syncToGrouper = new SyncToGrouper();
    syncToGrouper.setReadWrite(true);
    syncToGrouper.getSyncToGrouperBehavior().setStemSync(true);
    syncToGrouper.getSyncToGrouperBehavior().setStemInsert(true);
    syncToGrouper.getSyncToGrouperBehavior().setStemUpdate(true);
    syncToGrouper.getSyncToGrouperBehavior().setStemSyncFieldIdOnInsert(true);
    syncToGrouper.getSyncToGrouperBehavior().setStemSyncFieldDisplayName(true);
    syncToGrouper.getSyncToGrouperBehavior().setStemSyncFieldDescription(true);
    syncToGrouper.getSyncToGrouperBehavior().setStemSyncFieldAlternateName(true);
    syncToGrouper.getSyncToGrouperBehavior().setSqlLoad(true);
    syncToGrouper.getSyncToGrouperFromSql().setDatabaseConfigId("grouper");
    syncToGrouper.getSyncToGrouperFromSql().setStemSql("select * from testgrouper_syncgr_stem");

    syncStemToGrouperBeanTest.assignDisplayName("Test").assignAlternateName("myTest").store();
    
    syncToGrouperReport = syncToGrouper.syncLogic();
    
    assertEquals(1, GrouperUtil.length(syncToGrouper.getSyncStemToGrouperLogic().getTopLevelStemNamesToSync().size()));
    assertTrue(syncToGrouper.getSyncStemToGrouperLogic().getTopLevelStemNamesToSync().contains("test"));
  
    assertEquals(2, GrouperUtil.length(syncToGrouper.getSyncStemToGrouperLogic().getGrouperStemNameToStem()));
  
    assertEquals(1, syncToGrouperReport.getDifferenceCountOverall());
    assertEquals(1, syncToGrouperReport.getChangeCountOverall());
    assertEquals(0, syncToGrouperReport.getErrorLines().size());
    assertEquals(0, syncToGrouperReport.getStemInserts());
    assertEquals(1, syncToGrouperReport.getStemUpdates());
    assertEquals(1, GrouperUtil.length(syncToGrouperReport.getStemUpdatesNames()));
    assertTrue(syncToGrouperReport.getStemUpdatesNames().contains("test"));
    
    stemTest = StemFinder.findByName(grouperSession, "test", true);
    assertEquals(testUuid, stemTest.getId());
    assertEquals("Test", stemTest.getDisplayName());
    assertEquals("testDesc1", stemTest.getDescription());
    assertEquals("myTest", stemTest.getAlternateName());

    stemTestStem = StemFinder.findByName(grouperSession, "test:testStem", true);
    assertEquals(testStemUuid, stemTestStem.getId());
    assertTrue(StringUtils.isBlank(stemTestStem.getDescription()));
    assertEquals("Test:testStem", stemTestStem.getDisplayName());
    assertTrue(StringUtils.isBlank(stemTestStem.getAlternateName()));
  
    assertTrue(syncToGrouper.isSuccess());



  }
  
  public void testSyncStemNamesDb() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    dropTables();
    createTables();
    
    SyncToGrouper syncToGrouper = new SyncToGrouper();
    SyncToGrouperReport syncToGrouperReport = syncToGrouper.syncLogic();
    
    // ##############
    syncToGrouper = new SyncToGrouper();
    syncToGrouper.setReadWrite(true);
    syncToGrouper.getSyncToGrouperBehavior().setStemSync(true);
    syncToGrouper.getSyncToGrouperBehavior().setStemInsert(true);
    syncToGrouper.getSyncToGrouperBehavior().setSqlLoad(true);
    syncToGrouper.getSyncToGrouperFromSql().setDatabaseConfigId("grouper");
    syncToGrouper.getSyncToGrouperFromSql().setStemSql("select name from testgrouper_syncgr_stem");
    
    SyncStemToGrouperBean syncStemToGrouperBeanTest = new SyncStemToGrouperBean("test");
    syncStemToGrouperBeanTest.store();
    SyncStemToGrouperBean syncStemToGrouperBeanTestStem = new SyncStemToGrouperBean("test:testStem");
    syncStemToGrouperBeanTestStem.store();
    
    
    syncToGrouperReport = syncToGrouper.syncLogic();
    
    assertEquals(1, GrouperUtil.length(syncToGrouper.getSyncStemToGrouperLogic().getTopLevelStemNamesToSync().size()));
    assertTrue(syncToGrouper.getSyncStemToGrouperLogic().getTopLevelStemNamesToSync().contains("test"));
  
    assertEquals(0, GrouperUtil.length(syncToGrouper.getSyncStemToGrouperLogic().getGrouperStemNameToStem()));
  
    assertEquals(2, syncToGrouperReport.getDifferenceCountOverall());
    assertEquals(2, syncToGrouperReport.getChangeCountOverall());
    assertEquals(0, syncToGrouperReport.getErrorLines().size());
    assertEquals(2, syncToGrouperReport.getStemInserts());
    assertEquals(2, GrouperUtil.length(syncToGrouperReport.getStemInsertsNames()));
    assertTrue(syncToGrouperReport.getStemInsertsNames().contains("test"));
    assertTrue(syncToGrouperReport.getStemInsertsNames().contains("test:testStem"));
    
    Stem stemTest = StemFinder.findByName(grouperSession, "test", true);
    Stem stemTestStem = StemFinder.findByName(grouperSession, "test:testStem", true);
  
    assertTrue(syncToGrouper.isSuccess());
  
    dropTables();
  }

  public void testSyncStemDisplayName() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    SyncToGrouper syncToGrouper = new SyncToGrouper();
    syncToGrouper.setReadWrite(true);
    syncToGrouper.getSyncToGrouperBehavior().setStemSync(true);
    syncToGrouper.getSyncToGrouperBehavior().setStemInsert(true);
    syncToGrouper.getSyncToGrouperBehavior().setStemUpdate(true);
    syncToGrouper.getSyncToGrouperBehavior().setStemSyncFieldIdOnInsert(true);
    syncToGrouper.getSyncToGrouperBehavior().setStemSyncFieldDisplayName(true);
    syncToGrouper.getSyncToGrouperBehavior().setStemSyncFieldDescription(true);
    
    String testUuid = GrouperUuid.getUuid();
    String testStemUuid = GrouperUuid.getUuid();
    
    syncToGrouper.setSyncStemToGrouperBeans(
        GrouperUtil.toList(new SyncStemToGrouperBean("test").assignId(testUuid).assignDescription("testDesc1").assignDisplayName("Test"), 
                           new SyncStemToGrouperBean("test:testStem").assignId(testStemUuid).assignDescription("testStemDesc1").assignDisplayName("Test:Test Stem")));
    
    SyncToGrouperReport syncToGrouperReport = syncToGrouper.syncLogic();
    
    assertEquals(1, GrouperUtil.length(syncToGrouper.getSyncStemToGrouperLogic().getTopLevelStemNamesToSync().size()));
    assertTrue(syncToGrouper.getSyncStemToGrouperLogic().getTopLevelStemNamesToSync().contains("test"));
  
    assertEquals(0, GrouperUtil.length(syncToGrouper.getSyncStemToGrouperLogic().getGrouperStemNameToStem()));
  
    assertEquals(2, syncToGrouperReport.getDifferenceCountOverall());
    assertEquals(2, syncToGrouperReport.getChangeCountOverall());
    assertEquals(0, syncToGrouperReport.getErrorLines().size());
    assertEquals(2, syncToGrouperReport.getStemInserts());
    assertEquals(2, GrouperUtil.length(syncToGrouperReport.getStemInsertsNames()));
    assertTrue(syncToGrouperReport.getStemInsertsNames().contains("test"));
    assertTrue(syncToGrouperReport.getStemInsertsNames().contains("test:testStem"));
    
    Stem stemTest = StemFinder.findByName(grouperSession, "test", true);
    assertEquals(testUuid, stemTest.getId());
    assertEquals("Test", stemTest.getDisplayName());
    assertEquals("testDesc1", stemTest.getDescription());
    Stem stemTestStem = StemFinder.findByName(grouperSession, "test:testStem", true);
    assertEquals(testStemUuid, stemTestStem.getId());
    assertEquals("testStemDesc1", stemTestStem.getDescription());
    assertEquals("Test:Test Stem", stemTestStem.getDisplayName());
  
    assertTrue(syncToGrouper.isSuccess());
  
    // #############################
    
    syncToGrouper = new SyncToGrouper();
    syncToGrouper.setReadWrite(true);
    syncToGrouper.getSyncToGrouperBehavior().setStemSync(true);
    syncToGrouper.getSyncToGrouperBehavior().setStemInsert(true);
    syncToGrouper.getSyncToGrouperBehavior().setStemUpdate(true);
    syncToGrouper.getSyncToGrouperBehavior().setStemSyncFieldIdOnInsert(true);
    syncToGrouper.getSyncToGrouperBehavior().setStemSyncFieldDescription(true);
    syncToGrouper.getSyncToGrouperBehavior().setStemSyncFieldDisplayName(true);
    
    syncToGrouper.setSyncStemToGrouperBeans(
        GrouperUtil.toList(new SyncStemToGrouperBean("test").assignId(testUuid).assignDescription("testDesc2").assignDisplayName("Test2"), 
                           new SyncStemToGrouperBean("test:testStem").assignId(testStemUuid).assignDescription("testStemDesc1").assignDisplayName("Test2:Test Stem")));
    syncToGrouperReport = syncToGrouper.syncLogic();
    
    assertEquals(1, GrouperUtil.length(syncToGrouper.getSyncStemToGrouperLogic().getTopLevelStemNamesToSync().size()));
    assertTrue(syncToGrouper.getSyncStemToGrouperLogic().getTopLevelStemNamesToSync().contains("test"));
  
    assertEquals(2, GrouperUtil.length(syncToGrouper.getSyncStemToGrouperLogic().getGrouperStemNameToStem()));
  
    assertEquals(1, syncToGrouperReport.getDifferenceCountOverall());
    assertEquals(1, syncToGrouperReport.getChangeCountOverall());
    assertEquals(0, syncToGrouperReport.getErrorLines().size());
    assertEquals(0, syncToGrouperReport.getStemInserts());
    assertEquals(1, syncToGrouperReport.getStemUpdates());
    assertEquals(1, GrouperUtil.length(syncToGrouperReport.getStemUpdatesNames()));
    assertTrue(syncToGrouperReport.getStemUpdatesNames().contains("test"));
    
    stemTest = StemFinder.findByName(grouperSession, "test", true);
    assertEquals(testUuid, stemTest.getId());
    assertEquals("testDesc2", stemTest.getDescription());
    assertEquals("Test2", stemTest.getDisplayName());
    stemTestStem = StemFinder.findByName(grouperSession, "test:testStem", true);
    assertEquals(testStemUuid, stemTestStem.getId());
    assertEquals("testStemDesc1", stemTestStem.getDescription());
    assertEquals("Test2:Test Stem", stemTestStem.getDisplayName());
  
    assertTrue(syncToGrouper.isSuccess());
      
  }

  public void testSyncStemAlternateName() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    SyncToGrouper syncToGrouper = new SyncToGrouper();
    syncToGrouper.setReadWrite(true);
    syncToGrouper.getSyncToGrouperBehavior().setStemSync(true);
    syncToGrouper.getSyncToGrouperBehavior().setStemInsert(true);
    syncToGrouper.getSyncToGrouperBehavior().setStemUpdate(true);
    syncToGrouper.getSyncToGrouperBehavior().setStemSyncFieldIdOnInsert(true);
    syncToGrouper.getSyncToGrouperBehavior().setStemSyncFieldDisplayName(true);
    syncToGrouper.getSyncToGrouperBehavior().setStemSyncFieldDescription(true);
    syncToGrouper.getSyncToGrouperBehavior().setStemSyncFieldAlternateName(true);
    syncToGrouper.getSyncToGrouperBehavior().setStemSyncFieldIdIndexOnInsert(true);
    
    String testUuid = GrouperUuid.getUuid();
    String testStemUuid = GrouperUuid.getUuid();
    
    syncToGrouper.setSyncStemToGrouperBeans(
        GrouperUtil.toList(new SyncStemToGrouperBean("test").assignId(testUuid).assignDescription("testDesc1")
                              .assignDisplayName("Test").assignAlternateName("myTest").assignIdIndex(800L), 
                           new SyncStemToGrouperBean("test:testStem").assignId(testStemUuid).assignDescription("testStemDesc1")
                               .assignDisplayName("Test:Test Stem")));
    
    SyncToGrouperReport syncToGrouperReport = syncToGrouper.syncLogic();
    
    assertEquals(1, GrouperUtil.length(syncToGrouper.getSyncStemToGrouperLogic().getTopLevelStemNamesToSync().size()));
    assertTrue(syncToGrouper.getSyncStemToGrouperLogic().getTopLevelStemNamesToSync().contains("test"));
  
    assertEquals(0, GrouperUtil.length(syncToGrouper.getSyncStemToGrouperLogic().getGrouperStemNameToStem()));
  
    assertEquals(2, syncToGrouperReport.getDifferenceCountOverall());
    assertEquals(2, syncToGrouperReport.getChangeCountOverall());
    assertEquals(0, syncToGrouperReport.getErrorLines().size());
    assertEquals(2, syncToGrouperReport.getStemInserts());
    assertEquals(2, GrouperUtil.length(syncToGrouperReport.getStemInsertsNames()));
    assertTrue(syncToGrouperReport.getStemInsertsNames().contains("test"));
    assertTrue(syncToGrouperReport.getStemInsertsNames().contains("test:testStem"));
    
    Stem stemTest = StemFinder.findByName(grouperSession, "test", true);
    assertEquals(testUuid, stemTest.getId());
    assertEquals("Test", stemTest.getDisplayName());
    assertEquals("testDesc1", stemTest.getDescription());
    assertEquals(new Long(800L), stemTest.getIdIndex());
    assertEquals("myTest", stemTest.getAlternateName());
    
    Stem stemTestStem = StemFinder.findByName(grouperSession, "test:testStem", true);
    assertEquals(testStemUuid, stemTestStem.getId());
    assertEquals("testStemDesc1", stemTestStem.getDescription());
    assertEquals("Test:Test Stem", stemTestStem.getDisplayName());
    assertTrue(StringUtils.isBlank(stemTestStem.getAlternateName()));
  
    assertTrue(syncToGrouper.isSuccess());
  
    // #############################
    
    syncToGrouper = new SyncToGrouper();
    syncToGrouper.setReadWrite(true);
    syncToGrouper.getSyncToGrouperBehavior().setStemSync(true);
    syncToGrouper.getSyncToGrouperBehavior().setStemInsert(true);
    syncToGrouper.getSyncToGrouperBehavior().setStemUpdate(true);
    syncToGrouper.getSyncToGrouperBehavior().setStemSyncFieldIdOnInsert(true);
    syncToGrouper.getSyncToGrouperBehavior().setStemSyncFieldDescription(true);
    syncToGrouper.getSyncToGrouperBehavior().setStemSyncFieldDisplayName(true);
    syncToGrouper.getSyncToGrouperBehavior().setStemSyncFieldAlternateName(true);
    syncToGrouper.getSyncToGrouperBehavior().setStemSyncFieldIdIndexOnInsert(true);
    
    syncToGrouper.setSyncStemToGrouperBeans(
        GrouperUtil.toList(new SyncStemToGrouperBean("test").assignId(testUuid).assignDescription("testDesc2")
                              .assignDisplayName("Test2").assignAlternateName("myTest2"), 
                           new SyncStemToGrouperBean("test:testStem").assignId(testStemUuid)
                               .assignDescription("testStemDesc1").assignDisplayName("Test2:Test Stem")));
    syncToGrouperReport = syncToGrouper.syncLogic();
    
    assertEquals(1, GrouperUtil.length(syncToGrouper.getSyncStemToGrouperLogic().getTopLevelStemNamesToSync().size()));
    assertTrue(syncToGrouper.getSyncStemToGrouperLogic().getTopLevelStemNamesToSync().contains("test"));
  
    assertEquals(2, GrouperUtil.length(syncToGrouper.getSyncStemToGrouperLogic().getGrouperStemNameToStem()));
  
    assertEquals(1, syncToGrouperReport.getDifferenceCountOverall());
    assertEquals(1, syncToGrouperReport.getChangeCountOverall());
    assertEquals(0, syncToGrouperReport.getErrorLines().size());
    assertEquals(0, syncToGrouperReport.getStemInserts());
    assertEquals(1, syncToGrouperReport.getStemUpdates());
    assertEquals(1, GrouperUtil.length(syncToGrouperReport.getStemUpdatesNames()));
    assertTrue(syncToGrouperReport.getStemUpdatesNames().contains("test"));
    
    stemTest = StemFinder.findByName(grouperSession, "test", true);
    assertEquals(testUuid, stemTest.getId());
    assertEquals("testDesc2", stemTest.getDescription());
    assertEquals("Test2", stemTest.getDisplayName());
    assertEquals(new Long(800L), stemTest.getIdIndex());
    assertEquals("myTest2", stemTest.getAlternateName());

    stemTestStem = StemFinder.findByName(grouperSession, "test:testStem", true);
    assertEquals(testStemUuid, stemTestStem.getId());
    assertEquals("testStemDesc1", stemTestStem.getDescription());
    assertEquals("Test2:Test Stem", stemTestStem.getDisplayName());
    assertTrue(StringUtils.isBlank(stemTestStem.getAlternateName()));

    assertTrue(syncToGrouper.isSuccess());
      
  }

  public void testSyncFromGrouper() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    SyncToGrouper syncToGrouper = new SyncToGrouper();
    SyncToGrouperReport syncToGrouperReport = syncToGrouper.syncLogic();
    
    // ##############
    syncToGrouper = new SyncToGrouper();
    syncToGrouper.setReadWrite(true);
    syncToGrouper.getSyncToGrouperBehavior().setStemSync(true);
    syncToGrouper.getSyncToGrouperBehavior().setStemInsert(true);
    syncToGrouper.getSyncToGrouperBehavior().setStemSyncFieldIdOnInsert(true);
    syncToGrouper.getSyncToGrouperBehavior().setStemSyncFieldIdIndexOnInsert(true);
    syncToGrouper.getSyncToGrouperBehavior().setSqlLoad(true);
    syncToGrouper.getSyncToGrouperBehavior().setSqlLoadFromAnotherGrouper(true);
    syncToGrouper.getSyncToGrouperFromSql().setDatabaseConfigId("grouper");
    syncToGrouper.getSyncToGrouperFromSql().setDatabaseSyncFromAnotherGrouperTopLevelStems(GrouperUtil.toList("test"));
    
    syncToGrouper.getSyncToGrouperBehavior().setGroupSync(true);
    syncToGrouper.getSyncToGrouperBehavior().setGroupInsert(true);
    syncToGrouper.getSyncToGrouperBehavior().setGroupUpdate(true);
    syncToGrouper.getSyncToGrouperBehavior().setGroupSyncFieldIdOnInsert(true);
    syncToGrouper.getSyncToGrouperBehavior().setGroupSyncFieldDisplayName(true);
    syncToGrouper.getSyncToGrouperBehavior().setGroupSyncFieldDescription(true);
    syncToGrouper.getSyncToGrouperBehavior().setGroupSyncFieldAlternateName(true);
    syncToGrouper.getSyncToGrouperBehavior().setGroupSyncFieldIdIndexOnInsert(true);


    new StemSave(grouperSession).assignName("test").save();
    new StemSave(grouperSession).assignName("test:testStem").save();
        
    new GroupSave(grouperSession).assignName("test:testGroup1").save();
    new GroupSave(grouperSession).assignName("test:testGroup2").save();
    
    syncToGrouperReport = syncToGrouper.syncLogic();
    
    assertEquals(1, GrouperUtil.length(syncToGrouper.getSyncStemToGrouperLogic().getTopLevelStemNamesToSync().size()));
    assertTrue(syncToGrouper.getSyncStemToGrouperLogic().getTopLevelStemNamesToSync().contains("test"));
  
    assertEquals(2, GrouperUtil.length(syncToGrouper.getSyncStemToGrouperLogic().getGrouperStemNameToStem()));
    assertEquals(2, GrouperUtil.length(syncToGrouper.getSyncGroupToGrouperLogic().getGrouperGroupNameToGroup()));

    assertEquals(0, syncToGrouperReport.getDifferenceCountOverall());
    assertEquals(0, syncToGrouperReport.getChangeCountOverall());
    assertEquals(0, syncToGrouperReport.getErrorLines().size());
    assertEquals(0, syncToGrouperReport.getStemInserts());
    assertEquals(0, syncToGrouperReport.getGroupInserts());
    assertEquals(0, GrouperUtil.length(syncToGrouperReport.getStemInsertsNames()));
    assertEquals(0, GrouperUtil.length(syncToGrouperReport.getGroupInsertsNames()));
    
    Stem stemTest = StemFinder.findByName(grouperSession, "test", true);
  
    Stem stemTestStem = StemFinder.findByName(grouperSession, "test:testStem", true);
  
    Group groupTestGroup1 = GroupFinder.findByName(grouperSession, "test:testGroup1", true);
    Group groupTestGroup2 = GroupFinder.findByName(grouperSession, "test:testGroup2", true);

    assertTrue(syncToGrouper.isSuccess());
  
  }

  public void testSyncStemDelete() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    SyncToGrouper syncToGrouper = new SyncToGrouper();
    syncToGrouper.setReadWrite(true);
    syncToGrouper.getSyncToGrouperBehavior().setStemSync(true);
    syncToGrouper.getSyncToGrouperBehavior().setStemInsert(true);
    syncToGrouper.getSyncToGrouperBehavior().setStemUpdate(true);
    
    syncToGrouper.setSyncStemToGrouperBeans(
        GrouperUtil.toList(new SyncStemToGrouperBean("test"), 
                           new SyncStemToGrouperBean("test:testStem")));
    SyncToGrouperReport syncToGrouperReport = syncToGrouper.syncLogic();
    
    assertEquals(1, GrouperUtil.length(syncToGrouper.getSyncStemToGrouperLogic().getTopLevelStemNamesToSync().size()));
    assertTrue(syncToGrouper.getSyncStemToGrouperLogic().getTopLevelStemNamesToSync().contains("test"));
  
    assertEquals(0, GrouperUtil.length(syncToGrouper.getSyncStemToGrouperLogic().getGrouperStemNameToStem()));
  
    assertEquals(2, syncToGrouperReport.getDifferenceCountOverall());
    assertEquals(2, syncToGrouperReport.getChangeCountOverall());
    assertEquals(0, syncToGrouperReport.getErrorLines().size());
    assertEquals(2, syncToGrouperReport.getStemInserts());
    assertEquals(2, GrouperUtil.length(syncToGrouperReport.getStemInsertsNames()));
    assertTrue(syncToGrouperReport.getStemInsertsNames().contains("test"));
    assertTrue(syncToGrouperReport.getStemInsertsNames().contains("test:testStem"));
    
    Stem stemTest = StemFinder.findByName(grouperSession, "test", true);
    Stem stemTestStem = StemFinder.findByName(grouperSession, "test:testStem", true);
  
    assertTrue(syncToGrouper.isSuccess());
  
    // #############################
    
    syncToGrouper = new SyncToGrouper();
    syncToGrouper.setReadWrite(true);
    syncToGrouper.getSyncToGrouperBehavior().setStemSync(true);
    syncToGrouper.getSyncToGrouperBehavior().setStemInsert(true);
    syncToGrouper.getSyncToGrouperBehavior().setStemUpdate(true);
    syncToGrouper.getSyncToGrouperBehavior().setStemSyncFieldIdOnInsert(true);
    syncToGrouper.getSyncToGrouperBehavior().setStemSyncFieldDescription(true);

    syncToGrouper.setSyncStemToGrouperBeans(
        GrouperUtil.toList(new SyncStemToGrouperBean("test"), 
                           new SyncStemToGrouperBean("test:testStem")));
    
    new StemSave(grouperSession).assignName("test:testStem2").save();
    
    syncToGrouperReport = syncToGrouper.syncLogic();

    assertEquals(1, GrouperUtil.length(syncToGrouper.getSyncStemToGrouperLogic().getTopLevelStemNamesToSync().size()));
    assertTrue(syncToGrouper.getSyncStemToGrouperLogic().getTopLevelStemNamesToSync().contains("test"));

    assertEquals(3, GrouperUtil.length(syncToGrouper.getSyncStemToGrouperLogic().getGrouperStemNameToStem()));

    assertEquals(0, syncToGrouperReport.getDifferenceCountOverall());
    assertEquals(0, syncToGrouperReport.getChangeCountOverall());
    assertEquals(0, syncToGrouperReport.getErrorLines().size());
    assertEquals(0, syncToGrouperReport.getStemInserts());
    assertEquals(0, syncToGrouperReport.getStemUpdates());
    assertEquals(0, GrouperUtil.length(syncToGrouperReport.getStemUpdatesNames()));
    
    stemTest = StemFinder.findByName(grouperSession, "test", true);
    stemTestStem = StemFinder.findByName(grouperSession, "test:testStem", true);
    Stem stemTestStem2 = StemFinder.findByName(grouperSession, "test:testStem2", true);
  
    assertTrue(syncToGrouper.isSuccess());
    
    // #############################
    
    syncToGrouper = new SyncToGrouper();
    syncToGrouper.setReadWrite(true);
    syncToGrouper.getSyncToGrouperBehavior().setStemSync(true);
    syncToGrouper.getSyncToGrouperBehavior().setStemInsert(true);
    syncToGrouper.getSyncToGrouperBehavior().setStemUpdate(true);
    syncToGrouper.getSyncToGrouperBehavior().setStemDeleteExtra(true);
    syncToGrouper.getSyncToGrouperBehavior().setStemSyncFieldIdOnInsert(true);
    syncToGrouper.getSyncToGrouperBehavior().setStemSyncFieldDescription(true);

    syncToGrouper.setSyncStemToGrouperBeans(
        GrouperUtil.toList(new SyncStemToGrouperBean("test"), 
                           new SyncStemToGrouperBean("test:testStem")));
    
    new StemSave(grouperSession).assignName("test:testStem2").save();
    
    syncToGrouperReport = syncToGrouper.syncLogic();

    assertEquals(1, GrouperUtil.length(syncToGrouper.getSyncStemToGrouperLogic().getTopLevelStemNamesToSync().size()));
    assertTrue(syncToGrouper.getSyncStemToGrouperLogic().getTopLevelStemNamesToSync().contains("test"));

    assertEquals(3, GrouperUtil.length(syncToGrouper.getSyncStemToGrouperLogic().getGrouperStemNameToStem()));

    assertEquals(1, syncToGrouperReport.getDifferenceCountOverall());
    assertEquals(1, syncToGrouperReport.getChangeCountOverall());
    assertEquals(0, syncToGrouperReport.getErrorLines().size());
    assertEquals(0, syncToGrouperReport.getStemInserts());
    assertEquals(1, syncToGrouperReport.getStemDeletes());
    assertEquals(0, syncToGrouperReport.getStemUpdates());
    assertEquals(0, GrouperUtil.length(syncToGrouperReport.getStemUpdatesNames()));
    assertTrue(syncToGrouperReport.getStemDeletesNames().contains("test:testStem2"));
    
    stemTest = StemFinder.findByName(grouperSession, "test", true);
    stemTestStem = StemFinder.findByName(grouperSession, "test:testStem", true);
    stemTestStem2 = StemFinder.findByName(grouperSession, "test:testStem2", false);
    assertNull(stemTestStem2);
    assertTrue(syncToGrouper.isSuccess());
    
  }

  public void testSyncGroupAlternateName() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    SyncToGrouper syncToGrouper = new SyncToGrouper();
    syncToGrouper.setReadWrite(true);
    syncToGrouper.getSyncToGrouperBehavior().setStemSync(true);
    syncToGrouper.getSyncToGrouperBehavior().setGroupSync(true);
    syncToGrouper.getSyncToGrouperBehavior().setGroupInsert(true);
    syncToGrouper.getSyncToGrouperBehavior().setGroupUpdate(true);
    syncToGrouper.getSyncToGrouperBehavior().setGroupSyncFieldIdOnInsert(true);
    syncToGrouper.getSyncToGrouperBehavior().setGroupSyncFieldDisplayName(true);
    syncToGrouper.getSyncToGrouperBehavior().setGroupSyncFieldDescription(true);
    syncToGrouper.getSyncToGrouperBehavior().setGroupSyncFieldAlternateName(true);
    syncToGrouper.getSyncToGrouperBehavior().setGroupSyncFieldIdIndexOnInsert(true);
    
    String testGroup1uuid = GrouperUuid.getUuid();
    String testGroup2uuid = GrouperUuid.getUuid();
    
    new StemSave(grouperSession).assignName("test").save();
    
    syncToGrouper.setSyncStemToGrouperBeans(
        GrouperUtil.toList(new SyncStemToGrouperBean("test")));
    syncToGrouper.setSyncGroupToGrouperBeans(
        GrouperUtil.toList(
            new SyncGroupToGrouperBean("test:testGroup1").assignId(testGroup1uuid)
              .assignDescription("testGroup1desc").assignDisplayName("test:Test Group 1")
              .assignAlternateName("test:theTestGroup1").assignIdIndex(800L),
            new SyncGroupToGrouperBean("test:testGroup2").assignId(testGroup2uuid)
              .assignDescription("testGroup2desc")));

    SyncToGrouperReport syncToGrouperReport = syncToGrouper.syncLogic();

    assertEquals(0, GrouperUtil.length(syncToGrouper.getSyncGroupToGrouperLogic().getGrouperGroupNameToGroup()));
  
    assertEquals(2, syncToGrouperReport.getDifferenceCountOverall());
    assertEquals(2, syncToGrouperReport.getChangeCountOverall());
    assertEquals(0, syncToGrouperReport.getErrorLines().size());
    assertEquals(2, syncToGrouperReport.getGroupInserts());
    assertEquals(2, GrouperUtil.length(syncToGrouperReport.getGroupInsertsNames()));
    assertTrue(syncToGrouperReport.getGroupInsertsNames().contains("test:testGroup1"));
    assertTrue(syncToGrouperReport.getGroupInsertsNames().contains("test:testGroup2"));
    
    Group groupTestGroup1 = GroupFinder.findByName(grouperSession, "test:testGroup1", true);
    assertEquals(testGroup1uuid, groupTestGroup1.getId());
    assertEquals("testGroup1desc", groupTestGroup1.getDescription());
    assertEquals("test:Test Group 1", groupTestGroup1.getDisplayName());
    assertEquals(new Long(800L), groupTestGroup1.getIdIndex());
    assertEquals("test:theTestGroup1", groupTestGroup1.getAlternateName());
    
    Group groupTestGroup2 = GroupFinder.findByName(grouperSession, "test:testGroup2", true);
    assertEquals(testGroup2uuid, groupTestGroup2.getId());
    assertEquals("testGroup2desc", groupTestGroup2.getDescription());
    assertEquals("test:testGroup2", groupTestGroup2.getDisplayName());
    assertTrue(StringUtils.isBlank(groupTestGroup2.getAlternateName()));
  
    assertTrue(syncToGrouper.isSuccess());
  
    // #############################
    
    syncToGrouper = new SyncToGrouper();
    syncToGrouper.setReadWrite(true);
    syncToGrouper.getSyncToGrouperBehavior().setStemSync(true);
    syncToGrouper.getSyncToGrouperBehavior().setGroupSync(true);
    syncToGrouper.getSyncToGrouperBehavior().setGroupInsert(true);
    syncToGrouper.getSyncToGrouperBehavior().setGroupUpdate(true);
    syncToGrouper.getSyncToGrouperBehavior().setGroupSyncFieldIdOnInsert(true);
    syncToGrouper.getSyncToGrouperBehavior().setGroupSyncFieldDisplayName(true);
    syncToGrouper.getSyncToGrouperBehavior().setGroupSyncFieldDescription(true);
    syncToGrouper.getSyncToGrouperBehavior().setGroupSyncFieldAlternateName(true);
    syncToGrouper.getSyncToGrouperBehavior().setGroupSyncFieldIdIndexOnInsert(true);

    syncToGrouper.setSyncStemToGrouperBeans(
        GrouperUtil.toList(new SyncStemToGrouperBean("test")));
    syncToGrouper.setSyncGroupToGrouperBeans(
        GrouperUtil.toList(
            new SyncGroupToGrouperBean("test:testGroup1").assignId(testGroup1uuid)
              .assignDescription("testGroup1descA").assignDisplayName("test:Test Group 1A")
              .assignAlternateName("test:theTestGroup1A").assignIdIndex(800L),
            new SyncGroupToGrouperBean("test:testGroup2").assignId(testGroup2uuid)
              .assignDescription("testGroup2desc")));

    syncToGrouperReport = syncToGrouper.syncLogic();
    
    assertEquals(1, GrouperUtil.length(syncToGrouper.getSyncStemToGrouperLogic().getTopLevelStemNamesToSync().size()));
    assertTrue(syncToGrouper.getSyncStemToGrouperLogic().getTopLevelStemNamesToSync().contains("test"));
  
    assertEquals(2, GrouperUtil.length(syncToGrouper.getSyncGroupToGrouperLogic().getGrouperGroupNameToGroup()));
  
    assertEquals(1, syncToGrouperReport.getDifferenceCountOverall());
    assertEquals(1, syncToGrouperReport.getChangeCountOverall());
    assertEquals(0, syncToGrouperReport.getErrorLines().size());
    assertEquals(0, syncToGrouperReport.getGroupInserts());
    assertEquals(1, syncToGrouperReport.getGroupUpdates());
    assertEquals(1, GrouperUtil.length(syncToGrouperReport.getGroupUpdatesNames()));
    assertTrue(syncToGrouperReport.getGroupUpdatesNames().contains("test:testGroup1"));
    
    groupTestGroup1 = GroupFinder.findByName(grouperSession, "test:testGroup1", true);
    assertEquals(testGroup1uuid, groupTestGroup1.getId());
    assertEquals("testGroup1descA", groupTestGroup1.getDescription());
    assertEquals("test:Test Group 1A", groupTestGroup1.getDisplayName());
    assertEquals(new Long(800L), groupTestGroup1.getIdIndex());
    assertEquals("test:theTestGroup1A", groupTestGroup1.getAlternateName());
  
    groupTestGroup2 = GroupFinder.findByName(grouperSession, "test:testGroup2", true);
    assertEquals(testGroup2uuid, groupTestGroup2.getId());
    assertEquals("testGroup2desc", groupTestGroup2.getDescription());
    assertEquals("test:testGroup2", groupTestGroup2.getDisplayName());
    assertTrue(StringUtils.isBlank(groupTestGroup2.getAlternateName()));
  
    assertTrue(syncToGrouper.isSuccess());
      
  }

  public void testSyncGroupDelete() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    SyncToGrouper syncToGrouper = new SyncToGrouper();
    syncToGrouper.setReadWrite(true);
    syncToGrouper.getSyncToGrouperBehavior().setStemSync(true);
    syncToGrouper.getSyncToGrouperBehavior().setGroupSync(true);
    syncToGrouper.getSyncToGrouperBehavior().setGroupInsert(true);
    syncToGrouper.getSyncToGrouperBehavior().setGroupUpdate(true);
    syncToGrouper.getSyncToGrouperBehavior().setGroupSyncFromStems(true);
    
    new StemSave(grouperSession).assignName("test").save();
    
    syncToGrouper.setSyncStemToGrouperBeans(
        GrouperUtil.toList(new SyncStemToGrouperBean("test")));

    syncToGrouper.setSyncGroupToGrouperBeans(
        GrouperUtil.toList(
            new SyncGroupToGrouperBean("test:testGroup1"),
            new SyncGroupToGrouperBean("test:testGroup2")));

    syncToGrouper.setSyncStemToGrouperBeans(
        GrouperUtil.toList(new SyncStemToGrouperBean("test")));
    SyncToGrouperReport syncToGrouperReport = syncToGrouper.syncLogic();
    
    assertEquals(0, GrouperUtil.length(syncToGrouper.getSyncGroupToGrouperLogic().getGrouperGroupNameToGroup()));
  
    assertEquals(2, syncToGrouperReport.getDifferenceCountOverall());
    assertEquals(2, syncToGrouperReport.getChangeCountOverall());
    assertEquals(0, syncToGrouperReport.getErrorLines().size());
    assertEquals(2, syncToGrouperReport.getGroupInserts());
    assertEquals(2, GrouperUtil.length(syncToGrouperReport.getGroupInsertsNames()));
    assertTrue(syncToGrouperReport.getGroupInsertsNames().contains("test:testGroup1"));
    assertTrue(syncToGrouperReport.getGroupInsertsNames().contains("test:testGroup2"));
    
    Group groupTestGroup1 = GroupFinder.findByName(grouperSession, "test:testGroup1", true);
    Group groupTestGroup2 = GroupFinder.findByName(grouperSession, "test:testGroup2", true);

    assertTrue(syncToGrouper.isSuccess());
  
    // #############################
    
    syncToGrouper = new SyncToGrouper();
    syncToGrouper.setReadWrite(true);
    syncToGrouper.getSyncToGrouperBehavior().setStemSync(true);
    syncToGrouper.getSyncToGrouperBehavior().setGroupSync(true);
    syncToGrouper.getSyncToGrouperBehavior().setGroupSyncFromStems(true);
    syncToGrouper.getSyncToGrouperBehavior().setGroupInsert(true);
    syncToGrouper.getSyncToGrouperBehavior().setGroupUpdate(true);
    syncToGrouper.getSyncToGrouperBehavior().setGroupSyncFieldIdOnInsert(true);
    syncToGrouper.getSyncToGrouperBehavior().setGroupSyncFieldDescription(true);

    syncToGrouper.setSyncStemToGrouperBeans(
        GrouperUtil.toList(new SyncStemToGrouperBean("test")));

    syncToGrouper.setSyncGroupToGrouperBeans(
        GrouperUtil.toList(
            new SyncGroupToGrouperBean("test:testGroup1"),
            new SyncGroupToGrouperBean("test:testGroup2")));

    new GroupSave(grouperSession).assignName("test:testGroup3").save();

    syncToGrouperReport = syncToGrouper.syncLogic();
  
    assertEquals(3, GrouperUtil.length(syncToGrouper.getSyncGroupToGrouperLogic().getGrouperGroupNameToGroup()));
  
    assertEquals(0, syncToGrouperReport.getDifferenceCountOverall());
    assertEquals(0, syncToGrouperReport.getChangeCountOverall());
    assertEquals(0, syncToGrouperReport.getErrorLines().size());
    assertEquals(0, syncToGrouperReport.getGroupInserts());
    assertEquals(0, syncToGrouperReport.getGroupUpdates());
    assertEquals(0, GrouperUtil.length(syncToGrouperReport.getGroupUpdatesNames()));
    
    groupTestGroup1 = GroupFinder.findByName(grouperSession, "test:testGroup1", true);
    groupTestGroup2 = GroupFinder.findByName(grouperSession, "test:testGroup2", true);
    Group groupTestGroup3 = GroupFinder.findByName(grouperSession, "test:testGroup3", true);
  
    assertTrue(syncToGrouper.isSuccess());
    
    // #############################
    
    syncToGrouper = new SyncToGrouper();
    syncToGrouper.setReadWrite(true);
    syncToGrouper.getSyncToGrouperBehavior().setStemSync(true);
    syncToGrouper.getSyncToGrouperBehavior().setGroupSync(true);
    syncToGrouper.getSyncToGrouperBehavior().setGroupInsert(true);
    syncToGrouper.getSyncToGrouperBehavior().setGroupUpdate(true);
    syncToGrouper.getSyncToGrouperBehavior().setGroupDeleteExtra(true);
    syncToGrouper.getSyncToGrouperBehavior().setGroupSyncFieldIdOnInsert(true);
    syncToGrouper.getSyncToGrouperBehavior().setGroupSyncFieldDescription(true);
    syncToGrouper.getSyncToGrouperBehavior().setGroupSyncFromStems(true);

    syncToGrouper.setSyncStemToGrouperBeans(
        GrouperUtil.toList(new SyncStemToGrouperBean("test")));

    syncToGrouper.setSyncGroupToGrouperBeans(
        GrouperUtil.toList(
            new SyncGroupToGrouperBean("test:testGroup1"),
            new SyncGroupToGrouperBean("test:testGroup2")));
    
    syncToGrouperReport = syncToGrouper.syncLogic();
  
    assertEquals(3, GrouperUtil.length(syncToGrouper.getSyncGroupToGrouperLogic().getGrouperGroupNameToGroup()));
  
    assertEquals(1, syncToGrouperReport.getDifferenceCountOverall());
    assertEquals(1, syncToGrouperReport.getChangeCountOverall());
    assertEquals(0, syncToGrouperReport.getErrorLines().size());
    assertEquals(0, syncToGrouperReport.getGroupInserts());
    assertEquals(1, syncToGrouperReport.getGroupDeletes());
    assertEquals(0, syncToGrouperReport.getGroupUpdates());
    assertEquals(0, GrouperUtil.length(syncToGrouperReport.getGroupUpdatesNames()));
    assertTrue(syncToGrouperReport.getGroupDeletesNames().contains("test:testGroup3"));
    
    groupTestGroup1 = GroupFinder.findByName(grouperSession, "test:testGroup1", true);
    groupTestGroup2 = GroupFinder.findByName(grouperSession, "test:testGroup2", true);
    groupTestGroup3 = GroupFinder.findByName(grouperSession, "test:testGroup3", false);
    assertNull(groupTestGroup3);
    assertTrue(syncToGrouper.isSuccess());
    
  }

  public void testSyncGroupNameIdDb() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    dropTables();
    createTables();
    
    SyncToGrouper syncToGrouper = new SyncToGrouper();
    SyncToGrouperReport syncToGrouperReport = syncToGrouper.syncLogic();
    
    // ##############
    syncToGrouper = new SyncToGrouper();
    syncToGrouper.setReadWrite(true);
    syncToGrouper.getSyncToGrouperBehavior().setStemSync(true);
    syncToGrouper.getSyncToGrouperBehavior().setSqlLoad(true);
    syncToGrouper.getSyncToGrouperFromSql().setDatabaseConfigId("grouper");
    syncToGrouper.getSyncToGrouperFromSql().setStemSql("select * from testgrouper_syncgr_stem");
    syncToGrouper.getSyncToGrouperFromSql().setGroupSql("select * from testgrouper_syncgr_group");
    syncToGrouper.getSyncToGrouperBehavior().setGroupSync(true);
    syncToGrouper.getSyncToGrouperBehavior().setGroupInsert(true);
    syncToGrouper.getSyncToGrouperBehavior().setGroupUpdate(true);
    syncToGrouper.getSyncToGrouperBehavior().setGroupSyncFieldIdOnInsert(true);
    syncToGrouper.getSyncToGrouperBehavior().setGroupSyncFieldDisplayName(true);
    syncToGrouper.getSyncToGrouperBehavior().setGroupSyncFieldDescription(true);
    syncToGrouper.getSyncToGrouperBehavior().setGroupSyncFieldAlternateName(true);
    syncToGrouper.getSyncToGrouperBehavior().setGroupSyncFieldIdIndexOnInsert(true);

    SyncStemToGrouperBean syncStemToGrouperBeanTest = new SyncStemToGrouperBean("test");
    syncStemToGrouperBeanTest.store();
    
    new StemSave(grouperSession).assignName("test").save();

    String testGroup1uuid = GrouperUuid.getUuid();
    String testGroup2uuid = GrouperUuid.getUuid();

    SyncGroupToGrouperBean syncGroupToGrouperBeanTestGroup1 = new SyncGroupToGrouperBean("test:testGroup1").assignId(testGroup1uuid);
    syncGroupToGrouperBeanTestGroup1.store();
    SyncGroupToGrouperBean syncGroupToGrouperBeanTestGroup2 = new SyncGroupToGrouperBean("test:testGroup2").assignId(testGroup2uuid);
    syncGroupToGrouperBeanTestGroup2.store();

    syncToGrouperReport = syncToGrouper.syncLogic();
    
    assertEquals(0, GrouperUtil.length(syncToGrouper.getSyncGroupToGrouperLogic().getGrouperGroupNameToGroup()));
    
    assertEquals(2, syncToGrouperReport.getDifferenceCountOverall());
    assertEquals(2, syncToGrouperReport.getChangeCountOverall());
    assertEquals(0, syncToGrouperReport.getErrorLines().size());
    assertEquals(2, syncToGrouperReport.getGroupInserts());
    assertEquals(2, GrouperUtil.length(syncToGrouperReport.getGroupInsertsNames()));
    assertTrue(syncToGrouperReport.getGroupInsertsNames().contains("test:testGroup1"));
    assertTrue(syncToGrouperReport.getGroupInsertsNames().contains("test:testGroup2"));
    
    Group groupTestGroup1 = GroupFinder.findByName(grouperSession, "test:testGroup1", true);
    assertEquals(testGroup1uuid, groupTestGroup1.getId());
    
    Group groupTestGroup2 = GroupFinder.findByName(grouperSession, "test:testGroup2", true);
    assertEquals(testGroup2uuid, groupTestGroup2.getId());
  
    assertTrue(syncToGrouper.isSuccess());
  
    // #############################
    
    syncToGrouper = new SyncToGrouper();
    syncToGrouper.setReadWrite(true);
    syncToGrouper.getSyncToGrouperBehavior().setStemSync(true);
    syncToGrouper.getSyncToGrouperBehavior().setSqlLoad(true);
    syncToGrouper.getSyncToGrouperFromSql().setDatabaseConfigId("grouper");
    syncToGrouper.getSyncToGrouperFromSql().setStemSql("select * from testgrouper_syncgr_stem");
    syncToGrouper.getSyncToGrouperFromSql().setGroupSql("select * from testgrouper_syncgr_group");
    syncToGrouper.getSyncToGrouperBehavior().setGroupSync(true);
    syncToGrouper.getSyncToGrouperBehavior().setGroupInsert(true);
    syncToGrouper.getSyncToGrouperBehavior().setGroupUpdate(true);
    syncToGrouper.getSyncToGrouperBehavior().setGroupSyncFieldIdOnInsert(true);
    syncToGrouper.getSyncToGrouperBehavior().setGroupSyncFieldDisplayName(true);
    syncToGrouper.getSyncToGrouperBehavior().setGroupSyncFieldDescription(true);
    syncToGrouper.getSyncToGrouperBehavior().setGroupSyncFieldAlternateName(true);
    syncToGrouper.getSyncToGrouperBehavior().setGroupSyncFieldIdIndexOnInsert(true);
    
    syncGroupToGrouperBeanTestGroup1.assignDescription("testGroup1desc").store();
    
    syncToGrouperReport = syncToGrouper.syncLogic();
    
    assertEquals(2, GrouperUtil.length(syncToGrouper.getSyncGroupToGrouperLogic().getGrouperGroupNameToGroup()));
  
    assertEquals(1, syncToGrouperReport.getDifferenceCountOverall());
    assertEquals(1, syncToGrouperReport.getChangeCountOverall());
    assertEquals(0, syncToGrouperReport.getErrorLines().size());
    assertEquals(0, syncToGrouperReport.getGroupInserts());
    assertEquals(1, syncToGrouperReport.getGroupUpdates());
    assertEquals(1, GrouperUtil.length(syncToGrouperReport.getGroupUpdatesNames()));
    assertTrue(syncToGrouperReport.getGroupUpdatesNames().contains("test:testGroup1"));
    
    groupTestGroup1 = GroupFinder.findByName(grouperSession, "test:testGroup1", true);
    assertEquals(testGroup1uuid, groupTestGroup1.getId());
    assertEquals("testGroup1desc", groupTestGroup1.getDescription());
    
    groupTestGroup2 = GroupFinder.findByName(grouperSession, "test:testGroup2", true);
    
    assertTrue(syncToGrouper.isSuccess());
  
  
  }

  public void testSyncComposites() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    SyncToGrouper syncToGrouper = new SyncToGrouper();
    syncToGrouper.setReadWrite(true);
    syncToGrouper.getSyncToGrouperBehavior().setStemSync(true);
    syncToGrouper.getSyncToGrouperBehavior().setCompositeSync(true);
    syncToGrouper.getSyncToGrouperBehavior().setCompositeInsert(true);
    syncToGrouper.getSyncToGrouperBehavior().setCompositeSyncFromStems(true);
    syncToGrouper.getSyncToGrouperBehavior().setCompositeUpdate(true);
    syncToGrouper.getSyncToGrouperBehavior().setCompositeSyncFieldIdOnInsert(true);
    
    String testComposite1uuid = GrouperUuid.getUuid();
    String testComposite2uuid = GrouperUuid.getUuid();
    
    new StemSave(grouperSession).assignName("test").save();
    Group test_composite_owner1 = new GroupSave(grouperSession).assignName("test:composite_owner1").save();
    Group test_composite_owner2 = new GroupSave(grouperSession).assignName("test:composite_owner2").save();
    Group test_composite_owner3 = new GroupSave(grouperSession).assignName("test:composite_owner3").save();
    Group test_composite_left1 = new GroupSave(grouperSession).assignName("test:composite_left1").save();
    Group test_composite_left2 = new GroupSave(grouperSession).assignName("test:composite_left2").save();
    Group test_composite_left3 = new GroupSave(grouperSession).assignName("test:composite_left3").save();
    Group test_composite_right1 = new GroupSave(grouperSession).assignName("test:composite_right1").save();
    Group test_composite_right2 = new GroupSave(grouperSession).assignName("test:composite_right2").save();
    Group test_composite_right3 = new GroupSave(grouperSession).assignName("test:composite_right3").save();
    
    new CompositeSave().assignOwnerName(test_composite_owner3.getName())
      .assignLeftFactorName(test_composite_left3.getName()).assignRightFactorName(test_composite_right3.getName()).assignType("complement").save();
    
    syncToGrouper.setSyncStemToGrouperBeans(
        GrouperUtil.toList(new SyncStemToGrouperBean("test")));
    syncToGrouper.setSyncCompositeToGrouperBeans(
        GrouperUtil.toList(
            new SyncCompositeToGrouperBean().assignId(testComposite1uuid)
              .assignOwnerName("test:composite_owner1")
              .assignLeftFactorName("test:composite_left1").assignRightFactorName("test:composite_right1")
              .assignType("complement"),
            new SyncCompositeToGrouperBean().assignId(testComposite2uuid)
            .assignOwnerName("test:composite_owner2")
              .assignLeftFactorName("test:composite_left2").assignRightFactorName("test:composite_right2")
              .assignType("intersection")));

    SyncToGrouperReport syncToGrouperReport = syncToGrouper.syncLogic();

    assertEquals(1, GrouperUtil.length(syncToGrouper.getSyncCompositeToGrouperLogic().getGrouperCompositeOwnerLeftRightTypeToComposite()));

    assertEquals(2, syncToGrouperReport.getDifferenceCountOverall());
    assertEquals(2, syncToGrouperReport.getCompositeInserts());
    assertEquals(2, GrouperUtil.length(syncToGrouperReport.getCompositeInsertsNames()));
    assertTrue(syncToGrouperReport.getCompositeInsertsNames().contains("test:composite_owner1"));
    assertTrue(syncToGrouperReport.getCompositeInsertsNames().contains("test:composite_owner2"));
    assertEquals(2, syncToGrouperReport.getChangeCountOverall());
    assertEquals(0, syncToGrouperReport.getErrorLines().size());
    
    assertTrue(test_composite_owner1.hasComposite());
    assertEquals("test:composite_left1", test_composite_owner1.getComposite(true).getLeftGroup().getName());
    assertEquals("test:composite_right1", test_composite_owner1.getComposite(true).getRightGroup().getName());
    assertEquals("complement", test_composite_owner1.getComposite(true).getTypeDb());
    assertEquals(testComposite1uuid, test_composite_owner1.getComposite(true).getUuid());
    assertTrue(test_composite_owner2.hasComposite());
    assertEquals("test:composite_left2", test_composite_owner2.getComposite(true).getLeftGroup().getName());
    assertEquals("test:composite_right2", test_composite_owner2.getComposite(true).getRightGroup().getName());
    assertEquals("intersection", test_composite_owner2.getComposite(true).getTypeDb());
    assertEquals(testComposite2uuid, test_composite_owner2.getComposite(true).getUuid());
    
    assertTrue(syncToGrouper.isSuccess());
  
    // #############################
    
    syncToGrouper = new SyncToGrouper();
    syncToGrouper.setReadWrite(true);
    syncToGrouper.getSyncToGrouperBehavior().setStemSync(true);
    syncToGrouper.getSyncToGrouperBehavior().setCompositeSync(true);
    syncToGrouper.getSyncToGrouperBehavior().setCompositeInsert(true);
    syncToGrouper.getSyncToGrouperBehavior().setCompositeSyncFromStems(true);
    syncToGrouper.getSyncToGrouperBehavior().setCompositeUpdate(true);
    syncToGrouper.getSyncToGrouperBehavior().setCompositeDeleteExtra(true);
    
    syncToGrouper.setSyncStemToGrouperBeans(
        GrouperUtil.toList(new SyncStemToGrouperBean("test")));
    syncToGrouper.setSyncCompositeToGrouperBeans(
        GrouperUtil.toList(
            new SyncCompositeToGrouperBean().assignId(testComposite1uuid)
              .assignOwnerName("test:composite_owner1")
              .assignLeftFactorName("test:composite_left1").assignRightFactorName("test:composite_right1")
              .assignType("intersection"),
            new SyncCompositeToGrouperBean().assignId(testComposite1uuid)
            .assignOwnerName("test:composite_owner2")
              .assignLeftFactorName("test:composite_left2").assignRightFactorName("test:composite_right2")
              .assignType("intersection")));

    syncToGrouperReport = syncToGrouper.syncLogic();

    assertEquals(3, GrouperUtil.length(syncToGrouper.getSyncCompositeToGrouperLogic().getGrouperCompositeOwnerLeftRightTypeToComposite()));

    assertEquals(0, syncToGrouperReport.getErrorLines().size());
    assertEquals(1, syncToGrouperReport.getCompositeUpdates());
    assertEquals(1, GrouperUtil.length(syncToGrouperReport.getCompositeUpdatesNames()));
    assertTrue(syncToGrouperReport.getCompositeUpdatesNames().contains("test:composite_owner1"));
    assertEquals(1, syncToGrouperReport.getCompositeDeletes());
    assertEquals(1, GrouperUtil.length(syncToGrouperReport.getCompositeDeletesNames()));
    assertTrue(syncToGrouperReport.getCompositeDeletesNames().contains("test:composite_owner3"));
    assertEquals(2, syncToGrouperReport.getChangeCountOverall());
    assertEquals(2, syncToGrouperReport.getDifferenceCountOverall());
    
    assertTrue(test_composite_owner1.hasComposite());
    assertEquals("test:composite_left1", test_composite_owner1.getComposite(true).getLeftGroup().getName());
    assertEquals("test:composite_right1", test_composite_owner1.getComposite(true).getRightGroup().getName());
    assertEquals("intersection", test_composite_owner1.getComposite(true).getTypeDb());
    assertFalse(StringUtils.equals(testComposite1uuid, test_composite_owner1.getComposite(true).getUuid()));
    assertTrue(test_composite_owner2.hasComposite());
    assertEquals("test:composite_left2", test_composite_owner2.getComposite(true).getLeftGroup().getName());
    assertEquals("test:composite_right2", test_composite_owner2.getComposite(true).getRightGroup().getName());
    assertEquals("intersection", test_composite_owner2.getComposite(true).getTypeDb());
    assertEquals(testComposite2uuid, test_composite_owner2.getComposite(true).getUuid());
    assertFalse(test_composite_owner3.hasComposite());
    
    assertTrue(syncToGrouper.isSuccess());
  
  }

  public void testSyncCompositeDb() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    dropTables();
    createTables();
    
    SyncToGrouper syncToGrouper = new SyncToGrouper();
    
    // ##############
    syncToGrouper = new SyncToGrouper();
    syncToGrouper.setReadWrite(true);
    syncToGrouper.getSyncToGrouperBehavior().setStemSync(true);
    syncToGrouper.getSyncToGrouperBehavior().setSqlLoad(true);
    syncToGrouper.getSyncToGrouperFromSql().setDatabaseConfigId("grouper");
    syncToGrouper.getSyncToGrouperFromSql().setStemSql("select * from testgrouper_syncgr_stem");
    syncToGrouper.getSyncToGrouperFromSql().setGroupSql("select * from testgrouper_syncgr_group");
    syncToGrouper.getSyncToGrouperFromSql().setCompositeSql("select * from testgrouper_syncgr_composite");
    syncToGrouper.getSyncToGrouperBehavior().setStemSync(true);
    syncToGrouper.getSyncToGrouperBehavior().setCompositeSync(true);
    syncToGrouper.getSyncToGrouperBehavior().setCompositeInsert(true);
    syncToGrouper.getSyncToGrouperBehavior().setCompositeSyncFromStems(true);
    syncToGrouper.getSyncToGrouperBehavior().setCompositeUpdate(true);
    syncToGrouper.getSyncToGrouperBehavior().setCompositeSyncFieldIdOnInsert(true);

    SyncStemToGrouperBean syncStemToGrouperBeanTest = new SyncStemToGrouperBean("test");
    syncStemToGrouperBeanTest.store();
    
    new StemSave(grouperSession).assignName("test").save();
  
    String testComposite1uuid = GrouperUuid.getUuid();
    String testComposite2uuid = GrouperUuid.getUuid();
    
    new StemSave(grouperSession).assignName("test").save();
    Group test_composite_owner1 = new GroupSave(grouperSession).assignName("test:composite_owner1").save();
    Group test_composite_owner2 = new GroupSave(grouperSession).assignName("test:composite_owner2").save();
    Group test_composite_owner3 = new GroupSave(grouperSession).assignName("test:composite_owner3").save();
    Group test_composite_left1 = new GroupSave(grouperSession).assignName("test:composite_left1").save();
    Group test_composite_left2 = new GroupSave(grouperSession).assignName("test:composite_left2").save();
    Group test_composite_left3 = new GroupSave(grouperSession).assignName("test:composite_left3").save();
    Group test_composite_right1 = new GroupSave(grouperSession).assignName("test:composite_right1").save();
    Group test_composite_right2 = new GroupSave(grouperSession).assignName("test:composite_right2").save();
    Group test_composite_right3 = new GroupSave(grouperSession).assignName("test:composite_right3").save();
  
    new CompositeSave().assignOwnerName(test_composite_owner3.getName())
      .assignLeftFactorName(test_composite_left3.getName()).assignRightFactorName(test_composite_right3.getName()).assignType("complement").save();

    SyncCompositeToGrouperBean syncCompositeToGrouperBean1 = new SyncCompositeToGrouperBean().assignIdForInsert(testComposite1uuid)
      .assignOwnerName("test:composite_owner1")
      .assignLeftFactorName("test:composite_left1").assignRightFactorName("test:composite_right1")
      .assignType("complement");
    syncCompositeToGrouperBean1.store();    

    SyncCompositeToGrouperBean syncCompositeToGrouperBean2 = new SyncCompositeToGrouperBean().assignIdForInsert(testComposite2uuid)
      .assignOwnerName("test:composite_owner2")
      .assignLeftFactorName("test:composite_left2").assignRightFactorName("test:composite_right2")
      .assignType("intersection");
    syncCompositeToGrouperBean2.store();    

    SyncToGrouperReport syncToGrouperReport = syncToGrouper.syncLogic();

    assertEquals(1, GrouperUtil.length(syncToGrouper.getSyncCompositeToGrouperLogic().getGrouperCompositeOwnerLeftRightTypeToComposite()));

    assertEquals(2, syncToGrouperReport.getDifferenceCountOverall());
    assertEquals(2, syncToGrouperReport.getCompositeInserts());
    assertEquals(2, GrouperUtil.length(syncToGrouperReport.getCompositeInsertsNames()));
    assertTrue(syncToGrouperReport.getCompositeInsertsNames().contains("test:composite_owner1"));
    assertTrue(syncToGrouperReport.getCompositeInsertsNames().contains("test:composite_owner2"));
    assertEquals(2, syncToGrouperReport.getChangeCountOverall());
    assertEquals(0, syncToGrouperReport.getErrorLines().size());
    
    assertTrue(test_composite_owner1.hasComposite());
    assertEquals("test:composite_left1", test_composite_owner1.getComposite(true).getLeftGroup().getName());
    assertEquals("test:composite_right1", test_composite_owner1.getComposite(true).getRightGroup().getName());
    assertEquals("complement", test_composite_owner1.getComposite(true).getTypeDb());
    assertEquals(testComposite1uuid, test_composite_owner1.getComposite(true).getUuid());
    assertTrue(test_composite_owner2.hasComposite());
    assertEquals("test:composite_left2", test_composite_owner2.getComposite(true).getLeftGroup().getName());
    assertEquals("test:composite_right2", test_composite_owner2.getComposite(true).getRightGroup().getName());
    assertEquals("intersection", test_composite_owner2.getComposite(true).getTypeDb());
    assertEquals(testComposite2uuid, test_composite_owner2.getComposite(true).getUuid());
    
    assertTrue(syncToGrouper.isSuccess());
  
    // #############################
    
    syncToGrouper = new SyncToGrouper();
    syncToGrouper.setReadWrite(true);
    syncToGrouper.getSyncToGrouperBehavior().setStemSync(true);
    syncToGrouper.getSyncToGrouperBehavior().setSqlLoad(true);
    syncToGrouper.getSyncToGrouperFromSql().setDatabaseConfigId("grouper");
    syncToGrouper.getSyncToGrouperFromSql().setStemSql("select * from testgrouper_syncgr_stem");
    syncToGrouper.getSyncToGrouperFromSql().setGroupSql("select * from testgrouper_syncgr_group");
    syncToGrouper.getSyncToGrouperFromSql().setCompositeSql("select * from testgrouper_syncgr_composite");
    syncToGrouper.getSyncToGrouperBehavior().setStemSync(true);
    syncToGrouper.getSyncToGrouperBehavior().setCompositeSync(true);
    syncToGrouper.getSyncToGrouperBehavior().setCompositeInsert(true);
    syncToGrouper.getSyncToGrouperBehavior().setCompositeSyncFromStems(true);
    syncToGrouper.getSyncToGrouperBehavior().setCompositeUpdate(true);
    syncToGrouper.getSyncToGrouperBehavior().setCompositeSyncFieldIdOnInsert(true);
    syncToGrouper.getSyncToGrouperBehavior().setCompositeDeleteExtra(true);

    
    syncCompositeToGrouperBean1.assignType("intersection");
    syncCompositeToGrouperBean1.store();
    
    syncToGrouperReport = syncToGrouper.syncLogic();

    assertEquals(3, GrouperUtil.length(syncToGrouper.getSyncCompositeToGrouperLogic().getGrouperCompositeOwnerLeftRightTypeToComposite()));

    assertEquals(0, syncToGrouperReport.getErrorLines().size());
    assertEquals(1, syncToGrouperReport.getCompositeUpdates());
    assertEquals(1, GrouperUtil.length(syncToGrouperReport.getCompositeUpdatesNames()));
    assertTrue(syncToGrouperReport.getCompositeUpdatesNames().contains("test:composite_owner1"));
    assertEquals(1, syncToGrouperReport.getCompositeDeletes());
    assertEquals(1, GrouperUtil.length(syncToGrouperReport.getCompositeDeletesNames()));
    assertTrue(syncToGrouperReport.getCompositeDeletesNames().contains("test:composite_owner3"));
    assertEquals(2, syncToGrouperReport.getChangeCountOverall());
    assertEquals(2, syncToGrouperReport.getDifferenceCountOverall());
    
    assertTrue(test_composite_owner1.hasComposite());
    assertEquals("test:composite_left1", test_composite_owner1.getComposite(true).getLeftGroup().getName());
    assertEquals("test:composite_right1", test_composite_owner1.getComposite(true).getRightGroup().getName());
    assertEquals("intersection", test_composite_owner1.getComposite(true).getTypeDb());
    assertFalse(StringUtils.equals(testComposite1uuid, test_composite_owner1.getComposite(true).getUuid()));
    assertTrue(test_composite_owner2.hasComposite());
    assertEquals("test:composite_left2", test_composite_owner2.getComposite(true).getLeftGroup().getName());
    assertEquals("test:composite_right2", test_composite_owner2.getComposite(true).getRightGroup().getName());
    assertEquals("intersection", test_composite_owner2.getComposite(true).getTypeDb());
    assertEquals(testComposite2uuid, test_composite_owner2.getComposite(true).getUuid());
    assertFalse(test_composite_owner3.hasComposite());
    
    assertTrue(syncToGrouper.isSuccess());
  
  
  }

  /**
   * @param ddlVersionBean
   * @param database
   */
  public void createTableComposite() {
  
    final String tableName = "testgrouper_syncgr_composite";
  
    try {
      new GcDbAccess().sql("select count(*) from " + tableName).select(int.class);
    } catch (Exception e) {
      //we need to delete the test table if it is there, and create a new one
      //drop field id col, first drop foreign keys
      GrouperDdlUtils.changeDatabase(GrouperTestDdl.V1.getObjectName(), new DdlUtilsChangeDatabase() {
    
        public void changeDatabase(DdlVersionBean ddlVersionBean) {
          
          Database database = ddlVersionBean.getDatabase();
    
          
          Table loaderTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, tableName);
          
          GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "id", Types.VARCHAR, "40", true, true);
  
          GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "owner_name", Types.VARCHAR, "1024", false, true);
          
          GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "left_factor_name", Types.VARCHAR, "1024", false, true);
          
          GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "right_factor_name", Types.VARCHAR, "1024", false, true);
          
          GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "type", Types.VARCHAR, "20", false, true);
  
        }
        
      });
    }
  }


}
