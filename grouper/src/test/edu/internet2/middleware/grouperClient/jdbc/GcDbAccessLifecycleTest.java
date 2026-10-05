package edu.internet2.middleware.grouperClient.jdbc;

import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import edu.internet2.middleware.grouper.ddl.DdlUtilsChangeDatabase;
import edu.internet2.middleware.grouper.ddl.DdlVersionBean;
import edu.internet2.middleware.grouper.ddl.GrouperDdlUtils;
import edu.internet2.middleware.grouper.ddl.GrouperTestDdl;
import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.model.Database;
import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.model.Table;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import junit.textui.TestRunner;

/**
 * Tests the GcDbAccessLifecycle callbacks (dbPreStore / dbPreDelete) in GcDbAccess: called with the right
 * insert/update flag, before the field values are read (so changes are stored), for single and batch
 * store and delete.  Uses its own test table and test entity classes.
 */
public class GcDbAccessLifecycleTest extends GrouperTest {

  /**
   * test table: id, a value, and columns the callbacks set
   */
  private static final String TABLE_NAME = "testgrouper_lifecycle";

  /**
   * bean with callbacks, sets the context id and timestamps itself, and logs the calls
   */
  @GcPersistableClass(tableName=TABLE_NAME, defaultFieldPersist=GcPersist.doPersist)
  public static class LifecycleRow implements GcDbAccessLifecycle {

    /** primary key */
    @GcPersistableField(primaryKey=true, primaryKeyManuallyAssigned=true)
    private String id;

    /** some data */
    protected String theValue;

    /** set by dbPreStore */
    private String contextId;

    /** set by dbPreStore on insert */
    private Long createdOn;

    /** set by dbPreStore on every store */
    private Long lastUpdated;

    /** which callbacks were called, not persisted */
    @GcPersistableField(persist=GcPersist.dontPersist)
    protected List<String> calls = new ArrayList<String>();

    /** */
    public LifecycleRow() {
    }

    /**
     * @param id1
     * @param theValue1
     */
    public LifecycleRow(String id1, String theValue1) {
      this.id = id1;
      this.theValue = theValue1;
    }

    @Override
    public void dbPreStore(boolean isInsert) {
      this.calls.add("store:" + isInsert);
      this.contextId = isInsert ? "ctxInsert" : "ctxUpdate";
      long now = System.currentTimeMillis();
      if (isInsert) {
        this.createdOn = now;
      }
      this.lastUpdated = now;
    }

    @Override
    public void dbPreDelete() {
      this.calls.add("delete");
    }
  }

  /**
   * defaultUpdate bean: tries an update first, falls back to insert, so dbPreStore is called twice
   */
  @GcPersistableClass(tableName=TABLE_NAME, defaultFieldPersist=GcPersist.doPersist, defaultUpdate=true)
  public static class LifecycleDefaultUpdateRow extends LifecycleRow {

    /** */
    public LifecycleDefaultUpdateRow() {
    }

    /**
     * @param id1
     * @param theValue1
     */
    public LifecycleDefaultUpdateRow(String id1, String theValue1) {
      super(id1, theValue1);
    }
  }

  /**
   * bean without callbacks, behaves as before
   */
  @GcPersistableClass(tableName=TABLE_NAME, defaultFieldPersist=GcPersist.doPersist)
  public static class PlainRow {

    /** primary key */
    @GcPersistableField(primaryKey=true, primaryKeyManuallyAssigned=true)
    private String id;

    /** some data */
    private String theValue;

    /** */
    public PlainRow() {
    }
  }

  /**
   * @param name
   */
  public GcDbAccessLifecycleTest(String name) {
    super(name);
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new GcDbAccessLifecycleTest("testInsertAndUpdate"));
  }

  /**
   * create the test table if needed and empty it
   */
  @Override
  public void setUp() {
    super.setUp();
    try {
      new GcDbAccess().sql("select count(*) from " + TABLE_NAME).select(int.class);
    } catch (Exception e) {
      GrouperDdlUtils.changeDatabase(GrouperTestDdl.V1.getObjectName(), new DdlUtilsChangeDatabase() {
        public void changeDatabase(DdlVersionBean ddlVersionBean) {
          Database database = ddlVersionBean.getDatabase();
          Table table = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, TABLE_NAME);
          GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "id", Types.VARCHAR, "40", true, true);
          GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "the_value", Types.VARCHAR, "100", false, false);
          GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "context_id", Types.VARCHAR, "40", false, false);
          GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "created_on", Types.BIGINT, "20", false, false);
          GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "last_updated", Types.BIGINT, "20", false, false);
        }
      });
    }
    new GcDbAccess().sql("delete from " + TABLE_NAME).executeSql();
  }

  /**
   * @param id
   * @return a fresh copy from the db
   */
  private static LifecycleRow load(String id) {
    return new GcDbAccess().sql("select * from " + TABLE_NAME + " where id = ?").addBindVar(id).select(LifecycleRow.class);
  }

  /**
   * @return number of rows in the table
   */
  private static int dbCount() {
    return new GcDbAccess().sql("select count(*) from " + TABLE_NAME).select(int.class);
  }

  /**
   * insert calls dbPreStore(true), update calls dbPreStore(false), and what the callback sets is stored
   */
  public void testInsertAndUpdate() {
    LifecycleRow row = new LifecycleRow("a", "value1");
    new GcDbAccess().storeToDatabase(row);
    assertEquals(GrouperUtil.toList("store:true"), row.calls);

    LifecycleRow dbRow = load("a");
    assertEquals("ctxInsert", dbRow.contextId);
    assertNotNull(dbRow.createdOn);
    assertEquals(dbRow.createdOn, dbRow.lastUpdated);
    Long createdOn = dbRow.createdOn;

    row.theValue = "value2";
    row.lastUpdated = 0L;
    new GcDbAccess().storeToDatabase(row);
    assertEquals(GrouperUtil.toList("store:true", "store:false"), row.calls);

    dbRow = load("a");
    assertEquals("value2", dbRow.theValue);
    assertEquals("ctxUpdate", dbRow.contextId);
    assertEquals(createdOn, dbRow.createdOn);
    assertTrue(dbRow.lastUpdated >= createdOn);
  }

  /**
   * defaultUpdate class: the update finds no row, falls back to insert, dbPreStore is called again with true
   * and the insert has what that call set
   */
  public void testDefaultUpdateFallbackToInsert() {
    LifecycleDefaultUpdateRow row = new LifecycleDefaultUpdateRow("a", "value1");
    new GcDbAccess().storeToDatabase(row);
    assertEquals(GrouperUtil.toList("store:false", "store:true"), row.calls);

    LifecycleRow dbRow = load("a");
    assertEquals("ctxInsert", dbRow.contextId);
    assertNotNull(dbRow.createdOn);

    // existing row is just an update
    row.theValue = "value2";
    new GcDbAccess().storeToDatabase(row);
    assertEquals(GrouperUtil.toList("store:false", "store:true", "store:false"), row.calls);
    dbRow = load("a");
    assertEquals("value2", dbRow.theValue);
    assertEquals("ctxUpdate", dbRow.contextId);
  }

  /**
   * batch store calls dbPreStore for each object with the right flag, and stores what it sets
   */
  public void testBatchStore() {
    LifecycleRow rowA = new LifecycleRow("a", "value1");
    LifecycleRow rowB = new LifecycleRow("b", "value1");
    new GcDbAccess().storeListToDatabase(GrouperUtil.toList(rowA, rowB));
    assertEquals(GrouperUtil.toList("store:true"), rowA.calls);
    assertEquals(GrouperUtil.toList("store:true"), rowB.calls);
    assertEquals("ctxInsert", load("a").contextId);
    assertEquals("ctxInsert", load("b").contextId);

    rowA.theValue = "value2";
    rowB.theValue = "value2";
    new GcDbAccess().storeListToDatabase(GrouperUtil.toList(rowA, rowB));
    assertEquals(GrouperUtil.toList("store:true", "store:false"), rowA.calls);
    assertEquals(GrouperUtil.toList("store:true", "store:false"), rowB.calls);
    assertEquals("ctxUpdate", load("a").contextId);
    assertEquals("value2", load("b").theValue);
  }

  /**
   * single and multiple delete call dbPreDelete
   */
  public void testDelete() {
    new GcDbAccess().storeToDatabase(new LifecycleRow("a", "value1"));
    new GcDbAccess().storeToDatabase(new LifecycleRow("b", "value1"));
    new GcDbAccess().storeToDatabase(new LifecycleRow("c", "value1"));

    LifecycleRow rowA = load("a");
    new GcDbAccess().deleteFromDatabase(rowA);
    assertEquals(GrouperUtil.toList("delete"), rowA.calls);
    assertEquals(2, dbCount());

    LifecycleRow rowB = load("b");
    LifecycleRow rowC = load("c");
    new GcDbAccess().deleteFromDatabaseMultiple(GrouperUtil.toList(rowB, rowC));
    assertEquals(GrouperUtil.toList("delete"), rowB.calls);
    assertEquals(GrouperUtil.toList("delete"), rowC.calls);
    assertEquals(0, dbCount());
  }

  /**
   * class without the interface stores and deletes as before
   */
  public void testPlainClassUnchanged() {
    PlainRow row = new PlainRow();
    row.id = "a";
    row.theValue = "value1";
    new GcDbAccess().storeToDatabase(row);

    row.theValue = "value2";
    new GcDbAccess().storeToDatabase(row);

    LifecycleRow dbRow = load("a");
    assertEquals("value2", dbRow.theValue);
    assertNull(dbRow.contextId);
    assertNull(dbRow.createdOn);

    new GcDbAccess().deleteFromDatabase(row);
    assertEquals(0, dbCount());
  }
}
