package edu.internet2.middleware.grouperClient.jdbc;

import java.sql.Types;

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
 * Tests the GcPersistableField(optimisticLockVersion=true) optimistic locking in GcDbAccess
 * (insert sets version 0, update increments and checks it, stale updates/deletes throw
 * GcStaleObjectException, classes without the annotation are unchanged).
 * Uses its own test table and test entity classes, no real Grouper classes are versioned.
 */
public class GcDbAccessOptimisticLockTest extends GrouperTest {

  /**
   * test table, has an id, a value, and a hibernate style version column
   */
  private static final String TABLE_NAME = "testgrouper_opt_lock";

  /**
   * versioned entity with a manually assigned primary key (GcDbAccess checks the db to decide insert vs update)
   */
  @GcPersistableClass(tableName=TABLE_NAME, defaultFieldPersist=GcPersist.doPersist)
  public static class VersionedRow {

    /** primary key */
    @GcPersistableField(primaryKey=true, primaryKeyManuallyAssigned=true)
    private String id;

    /** some data */
    private String theValue;

    /** version, starts at -1 (unsaved) like GrouperAPI.hibernateVersionNumber */
    @GcPersistableField(optimisticLockVersion=true)
    private Long hibernateVersionNumber = -1L;

    /** */
    public VersionedRow() {
    }

    /**
     * @param id1
     * @param theValue1
     */
    public VersionedRow(String id1, String theValue1) {
      this.id = id1;
      this.theValue = theValue1;
    }
  }

  /**
   * versioned entity with defaultUpdate=true, the version decides insert vs update
   */
  @GcPersistableClass(tableName=TABLE_NAME, defaultFieldPersist=GcPersist.doPersist, defaultUpdate=true)
  public static class VersionedDefaultUpdateRow {

    /** primary key */
    @GcPersistableField(primaryKey=true, primaryKeyManuallyAssigned=true)
    private String id;

    /** some data */
    private String theValue;

    /** version, primitive long starting at -1 (unsaved) */
    @GcPersistableField(optimisticLockVersion=true)
    private long hibernateVersionNumber = -1L;

    /** */
    public VersionedDefaultUpdateRow() {
    }

    /**
     * @param id1
     * @param theValue1
     */
    public VersionedDefaultUpdateRow(String id1, String theValue1) {
      this.id = id1;
      this.theValue = theValue1;
    }
  }

  /**
   * same table without the version annotation (version column not mapped), must behave like before:
   * last writer wins, version column untouched
   */
  @GcPersistableClass(tableName=TABLE_NAME, defaultFieldPersist=GcPersist.doPersist)
  public static class UnversionedRow {

    /** primary key */
    @GcPersistableField(primaryKey=true, primaryKeyManuallyAssigned=true)
    private String id;

    /** some data */
    private String theValue;

    /** */
    public UnversionedRow() {
    }
  }

  /**
   * @param name
   */
  public GcDbAccessOptimisticLockTest(String name) {
    super(name);
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new GcDbAccessOptimisticLockTest("testStaleUpdateThrows"));
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
          GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "hibernate_version_number", Types.BIGINT, "12", false, false);
        }
      });
    }
    new GcDbAccess().sql("delete from " + TABLE_NAME).executeSql();
  }

  /**
   * make sure the static switch is back on for other tests
   */
  @Override
  protected void tearDown() {
    GcDbAccess.setOptimisticLocking(true);
    super.tearDown();
  }

  /**
   * @param id
   * @return the version in the db
   */
  private static Long dbVersion(String id) {
    return new GcDbAccess().sql("select hibernate_version_number from " + TABLE_NAME + " where id = ?").addBindVar(id).select(Long.class);
  }

  /**
   * @param id
   * @return the value in the db
   */
  private static String dbValue(String id) {
    return new GcDbAccess().sql("select the_value from " + TABLE_NAME + " where id = ?").addBindVar(id).select(String.class);
  }

  /**
   * @return number of rows in the table
   */
  private static int dbCount() {
    return new GcDbAccess().sql("select count(*) from " + TABLE_NAME).select(int.class);
  }

  /**
   * @param id
   * @return a fresh copy from the db
   */
  private static VersionedRow load(String id) {
    return new GcDbAccess().sql("select * from " + TABLE_NAME + " where id = ?").addBindVar(id).select(VersionedRow.class);
  }

  /**
   * insert sets version 0 on the row and the object
   */
  public void testInsertSetsVersionZero() {
    VersionedRow row = new VersionedRow("a", "value1");
    new GcDbAccess().storeToDatabase(row);

    assertEquals(0L, row.hibernateVersionNumber.longValue());
    assertEquals(0L, dbVersion("a").longValue());
    assertEquals("value1", dbValue("a"));
  }

  /**
   * each update increments the version on the row and the object, the caller does not touch it
   */
  public void testUpdateIncrementsVersion() {
    VersionedRow row = new VersionedRow("a", "value1");
    new GcDbAccess().storeToDatabase(row);

    row.theValue = "value2";
    new GcDbAccess().storeToDatabase(row);
    assertEquals(1L, row.hibernateVersionNumber.longValue());
    assertEquals(1L, dbVersion("a").longValue());
    assertEquals("value2", dbValue("a"));

    // a copy loaded from the db has the db version and can be updated
    VersionedRow loaded = load("a");
    assertEquals(1L, loaded.hibernateVersionNumber.longValue());
    loaded.theValue = "value3";
    new GcDbAccess().storeToDatabase(loaded);
    assertEquals(2L, loaded.hibernateVersionNumber.longValue());
    assertEquals(2L, dbVersion("a").longValue());
    assertEquals("value3", dbValue("a"));
  }

  /**
   * two copies loaded, first one saved, second save throws and does not change or insert anything
   */
  public void testStaleUpdateThrows() {
    new GcDbAccess().storeToDatabase(new VersionedRow("a", "value1"));

    VersionedRow copy1 = load("a");
    VersionedRow copy2 = load("a");

    copy1.theValue = "fromCopy1";
    new GcDbAccess().storeToDatabase(copy1);

    copy2.theValue = "fromCopy2";
    try {
      new GcDbAccess().storeToDatabase(copy2);
      fail("expected GcStaleObjectException");
    } catch (GcStaleObjectException gsoe) {
      // good
    }

    // stale copy keeps its old version, db has copy1's change, no duplicate
    assertEquals(0L, copy2.hibernateVersionNumber.longValue());
    assertEquals("fromCopy1", dbValue("a"));
    assertEquals(1L, dbVersion("a").longValue());
    assertEquals(1, dbCount());

    // reload and try again works
    VersionedRow copy3 = load("a");
    copy3.theValue = "fromCopy3";
    new GcDbAccess().storeToDatabase(copy3);
    assertEquals("fromCopy3", dbValue("a"));
    assertEquals(2L, dbVersion("a").longValue());
  }

  /**
   * manually assigned primary key: the version (not a select) decides insert vs update, so a new object
   * with the id of an existing row tries an insert and fails on the primary key (like hibernate), it does
   * not overwrite the row and does not insert a duplicate
   */
  public void testManualPrimaryKeyNewObjectDoesNotOverwrite() {
    new GcDbAccess().storeToDatabase(new VersionedRow("a", "value1"));

    VersionedRow imposter = new VersionedRow("a", "imposter");
    try {
      new GcDbAccess().storeToDatabase(imposter);
      fail("expected duplicate key exception");
    } catch (RuntimeException re) {
      // good, duplicate primary key
    }
    // failed insert leaves the object unsaved
    assertEquals(-1L, imposter.hibernateVersionNumber.longValue());
    assertEquals("value1", dbValue("a"));
    assertEquals(0L, dbVersion("a").longValue());
    assertEquals(1, dbCount());
  }

  /**
   * defaultUpdate class: unsaved version inserts, saved version updates, stale throws instead of falling back to insert
   */
  public void testDefaultUpdateVersioned() {
    VersionedDefaultUpdateRow row = new VersionedDefaultUpdateRow("a", "value1");
    new GcDbAccess().storeToDatabase(row);
    assertEquals(0L, row.hibernateVersionNumber);
    assertEquals(0L, dbVersion("a").longValue());

    row.theValue = "value2";
    new GcDbAccess().storeToDatabase(row);
    assertEquals(1L, row.hibernateVersionNumber);
    assertEquals("value2", dbValue("a"));

    // stale copy: version 0 but db is 1
    VersionedDefaultUpdateRow stale = new VersionedDefaultUpdateRow("a", "staleValue");
    stale.hibernateVersionNumber = 0L;
    try {
      new GcDbAccess().storeToDatabase(stale);
      fail("expected GcStaleObjectException");
    } catch (GcStaleObjectException gsoe) {
      // good
    }
    assertEquals("value2", dbValue("a"));
    assertEquals(1, dbCount());
  }

  /**
   * delete checks the version: a stale copy cannot delete, a current one can
   */
  public void testStaleDeleteThrows() {
    new GcDbAccess().storeToDatabase(new VersionedRow("a", "value1"));

    VersionedRow copy1 = load("a");
    VersionedRow copy2 = load("a");

    copy1.theValue = "value2";
    new GcDbAccess().storeToDatabase(copy1);

    try {
      new GcDbAccess().deleteFromDatabase(copy2);
      fail("expected GcStaleObjectException");
    } catch (GcStaleObjectException gsoe) {
      // good
    }
    assertEquals(1, dbCount());

    new GcDbAccess().deleteFromDatabase(copy1);
    assertEquals(0, dbCount());
  }

  /**
   * the multiple (batch) delete does not check the version, it deletes by primary key even if stale
   */
  public void testDeleteMultipleIgnoresVersion() {
    new GcDbAccess().storeToDatabase(new VersionedRow("a", "value1"));
    new GcDbAccess().storeToDatabase(new VersionedRow("b", "value1"));

    VersionedRow staleA = load("a");
    VersionedRow currentA = load("a");
    currentA.theValue = "value2";
    new GcDbAccess().storeToDatabase(currentA);

    new GcDbAccess().deleteFromDatabaseMultiple(GrouperUtil.toList(staleA, load("b")));
    assertEquals(0, dbCount());
  }

  /**
   * with dao.optimisticLocking false the version is still incremented but not checked, last writer wins
   */
  public void testOptimisticLockingOff() {
    new GcDbAccess().storeToDatabase(new VersionedRow("a", "value1"));

    VersionedRow copy1 = load("a");
    VersionedRow copy2 = load("a");

    GcDbAccess.setOptimisticLocking(false);
    try {
      copy1.theValue = "fromCopy1";
      new GcDbAccess().storeToDatabase(copy1);
      copy2.theValue = "fromCopy2";
      new GcDbAccess().storeToDatabase(copy2);
    } finally {
      GcDbAccess.setOptimisticLocking(true);
    }
    assertEquals("fromCopy2", dbValue("a"));
    assertEquals(1L, dbVersion("a").longValue());
    assertEquals(1L, copy2.hibernateVersionNumber.longValue());
  }

  /**
   * class without the annotation: no version check, version column untouched (same as before this feature)
   */
  public void testUnversionedUnchanged() {
    new GcDbAccess().sql("insert into " + TABLE_NAME + " (id, the_value, hibernate_version_number) values ('a', 'value1', 5)").executeSql();

    UnversionedRow copy1 = new GcDbAccess().sql("select * from " + TABLE_NAME + " where id = 'a'").select(UnversionedRow.class);
    UnversionedRow copy2 = new GcDbAccess().sql("select * from " + TABLE_NAME + " where id = 'a'").select(UnversionedRow.class);

    copy1.theValue = "fromCopy1";
    new GcDbAccess().storeToDatabase(copy1);
    copy2.theValue = "fromCopy2";
    new GcDbAccess().storeToDatabase(copy2);

    assertEquals("fromCopy2", dbValue("a"));
    assertEquals(5L, dbVersion("a").longValue());

    // batch store still works for unversioned classes
    copy1.theValue = "fromBatch";
    new GcDbAccess().storeListToDatabase(GrouperUtil.toList(copy1));
    assertEquals("fromBatch", dbValue("a"));
  }

  /**
   * batch store refuses versioned classes since it cannot check the version per row
   */
  public void testBatchStoreRefusesVersioned() {
    try {
      new GcDbAccess().storeListToDatabase(GrouperUtil.toList(new VersionedRow("a", "value1")));
      fail("expected exception");
    } catch (RuntimeException re) {
      assertTrue(re.getMessage(), re.getMessage().contains("optimisticLockVersion"));
    }
    assertEquals(0, dbCount());
  }
}
