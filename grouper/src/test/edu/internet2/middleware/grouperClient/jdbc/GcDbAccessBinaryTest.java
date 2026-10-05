package edu.internet2.middleware.grouperClient.jdbc;

import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;

import edu.internet2.middleware.grouper.ddl.DdlUtilsChangeDatabase;
import edu.internet2.middleware.grouper.ddl.DdlVersionBean;
import edu.internet2.middleware.grouper.ddl.GrouperDdlUtils;
import edu.internet2.middleware.grouper.ddl.GrouperTestDdl;
import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.model.Database;
import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.model.Table;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import junit.textui.TestRunner;

/**
 * Tests binary (byte[]) column support in GcDbAccess: reading binary columns with select(byte[].class),
 * selectList, Map / Object[] rows, and GcPersistableClass beans with a byte[] field, plus storing those
 * beans with a null byte[] (bound as GcDbTypedNull.BINARY).
 * Uses its own test table with a nullable binary column: Types.BLOB which ddlutils makes postgres BYTEA,
 * oracle BLOB, mysql LONGBLOB.
 */
public class GcDbAccessBinaryTest extends GrouperTest {

  /**
   * test table, has an id, a name, and a nullable binary column
   */
  private static final String TABLE_NAME = "testgrouper_binary";

  /**
   * entity with a byte[] field and a manually assigned primary key
   */
  @GcPersistableClass(tableName=TABLE_NAME, defaultFieldPersist=GcPersist.doPersist)
  public static class BinaryRow {

    /** primary key */
    @GcPersistableField(primaryKey=true, primaryKeyManuallyAssigned=true)
    private String id;

    /** some text */
    private String theName;

    /** binary data, nullable */
    private byte[] theBytes;

    /** */
    public BinaryRow() {
    }

    /**
     * @param id1
     * @param theName1
     * @param theBytes1
     */
    public BinaryRow(String id1, String theName1, byte[] theBytes1) {
      this.id = id1;
      this.theName = theName1;
      this.theBytes = theBytes1;
    }
  }

  /**
   * @param name
   */
  public GcDbAccessBinaryTest(String name) {
    super(name);
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new GcDbAccessBinaryTest("testBeanNullBytes"));
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
          GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "the_name", Types.VARCHAR, "100", false, false);
          GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "the_bytes", Types.BLOB, null, false, false);
        }
      });
    }
    new GcDbAccess().sql("delete from " + TABLE_NAME).executeSql();
  }

  /**
   * @param size
   * @return random bytes, all 256 values show up so encoding problems would be caught
   */
  private static byte[] randomBytes(int size) {
    byte[] bytes = new byte[size];
    new Random(size).nextBytes(bytes);
    return bytes;
  }

  /**
   * @param id
   * @return the bytes in the db
   */
  private static byte[] dbBytes(String id) {
    return new GcDbAccess().sql("select the_bytes from " + TABLE_NAME + " where id = ?").addBindVar(id).select(byte[].class);
  }

  /**
   * @param id
   * @return a fresh copy from the db
   */
  private static BinaryRow load(String id) {
    return new GcDbAccess().sql("select * from " + TABLE_NAME + " where id = ?").addBindVar(id).select(BinaryRow.class);
  }

  /**
   * @param id
   * @param bytes
   */
  private static void insertWithSql(String id, byte[] bytes) {
    new GcDbAccess().sql("insert into " + TABLE_NAME + " (id, the_name, the_bytes) values (?, ?, ?)")
      .addBindVar(id).addBindVar("name_" + id).addBindVar(bytes).executeSql();
  }

  /**
   * oracle stores an empty byte[] as null, everyone else keeps it empty
   * @param actual
   */
  private static void assertEmptyBytes(byte[] actual) {
    if (GrouperDdlUtils.isOracle()) {
      assertTrue(actual == null || actual.length == 0);
    } else {
      assertNotNull(actual);
      assertEquals(0, actual.length);
    }
  }

  /**
   * sql + addBindVar(byte[]) and select(byte[].class): empty, small, ~5MB
   */
  public void testSqlRoundTrip() {
    byte[] small = randomBytes(1000);
    byte[] large = randomBytes(5 * 1024 * 1024 + 17);

    insertWithSql("empty", new byte[0]);
    insertWithSql("small", small);
    insertWithSql("large", large);

    assertEmptyBytes(dbBytes("empty"));
    assertTrue(Arrays.equals(small, dbBytes("small")));
    assertTrue(Arrays.equals(large, dbBytes("large")));
  }

  /**
   * plain addBindVar(null) still works like it always did, and GcDbTypedNull.BINARY binds a binary null
   */
  public void testSqlNullBinds() {
    insertWithSql("plainNull", null);
    assertNull(dbBytes("plainNull"));

    new GcDbAccess().sql("insert into " + TABLE_NAME + " (id, the_name, the_bytes) values (?, ?, ?)")
      .addBindVar("typedNull").addBindVar("name").addBindVar(GcDbTypedNull.BINARY).executeSql();
    assertNull(dbBytes("typedNull"));
    assertEquals("name", new GcDbAccess().sql("select the_name from " + TABLE_NAME + " where id = ?").addBindVar("typedNull").select(String.class));

    // set a value then back to null with the typed null
    byte[] small = randomBytes(10);
    new GcDbAccess().sql("update " + TABLE_NAME + " set the_bytes = ? where id = ?").addBindVar(small).addBindVar("typedNull").executeSql();
    assertTrue(Arrays.equals(small, dbBytes("typedNull")));
    new GcDbAccess().sql("update " + TABLE_NAME + " set the_bytes = ? where id = ?").addBindVar(GcDbTypedNull.BINARY).addBindVar("typedNull").executeSql();
    assertNull(dbBytes("typedNull"));
  }

  /**
   * selectList(byte[].class), and Map / Object[] rows with byte[] columns
   */
  public void testSelectList() {
    byte[] bytesA = randomBytes(100);
    byte[] bytesB = randomBytes(200);
    insertWithSql("a", bytesA);
    insertWithSql("b", bytesB);
    insertWithSql("c", null);

    List<byte[]> bytesList = new GcDbAccess().sql("select the_bytes from " + TABLE_NAME + " order by id").selectList(byte[].class);
    assertEquals(3, bytesList.size());
    assertTrue(Arrays.equals(bytesA, bytesList.get(0)));
    assertTrue(Arrays.equals(bytesB, bytesList.get(1)));
    assertNull(bytesList.get(2));

    @SuppressWarnings("rawtypes")
    List<Map> maps = new GcDbAccess().sql("select id, the_bytes from " + TABLE_NAME + " order by id").selectList(Map.class);
    assertEquals(3, maps.size());
    assertTrue(Arrays.equals(bytesA, (byte[])maps.get(0).get("the_bytes")));
    assertNull(maps.get(2).get("the_bytes"));

    List<Object[]> rows = new GcDbAccess().sql("select id, the_bytes from " + TABLE_NAME + " order by id").selectList(Object[].class);
    assertEquals("b", rows.get(1)[0]);
    assertTrue(Arrays.equals(bytesB, (byte[])rows.get(1)[1]));

    List<BinaryRow> beans = new GcDbAccess().sql("select * from " + TABLE_NAME + " order by id").selectList(BinaryRow.class);
    assertEquals(3, beans.size());
    assertEquals("name_a", beans.get(0).theName);
    assertTrue(Arrays.equals(bytesA, beans.get(0).theBytes));
    assertNull(beans.get(2).theBytes);
  }

  /**
   * bean with a byte[] field: storeToDatabase insert and update, load with select(BinaryRow.class)
   */
  public void testBeanInsertUpdate() {
    byte[] small = randomBytes(1000);
    BinaryRow row = new BinaryRow("a", "name1", small);
    new GcDbAccess().storeToDatabase(row);

    BinaryRow loaded = load("a");
    assertEquals("name1", loaded.theName);
    assertTrue(Arrays.equals(small, loaded.theBytes));

    // update to a big value
    byte[] large = randomBytes(5 * 1024 * 1024 + 3);
    loaded.theBytes = large;
    loaded.theName = "name2";
    new GcDbAccess().storeToDatabase(loaded);

    BinaryRow loaded2 = load("a");
    assertEquals("name2", loaded2.theName);
    assertTrue(Arrays.equals(large, loaded2.theBytes));

    // empty array
    loaded2.theBytes = new byte[0];
    new GcDbAccess().storeToDatabase(loaded2);
    assertEmptyBytes(load("a").theBytes);
  }

  /**
   * bean with a null byte[] field inserts and updates (the typed null bind), and a value then back to null
   */
  public void testBeanNullBytes() {
    BinaryRow row = new BinaryRow("a", "name1", null);
    new GcDbAccess().storeToDatabase(row);

    BinaryRow loaded = load("a");
    assertEquals("name1", loaded.theName);
    assertNull(loaded.theBytes);

    // update other field, bytes still null
    loaded.theName = "name2";
    new GcDbAccess().storeToDatabase(loaded);
    loaded = load("a");
    assertEquals("name2", loaded.theName);
    assertNull(loaded.theBytes);

    // set a value
    byte[] small = randomBytes(50);
    loaded.theBytes = small;
    new GcDbAccess().storeToDatabase(loaded);
    loaded = load("a");
    assertTrue(Arrays.equals(small, loaded.theBytes));

    // back to null
    loaded.theBytes = null;
    new GcDbAccess().storeToDatabase(loaded);
    loaded = load("a");
    assertEquals("name2", loaded.theName);
    assertNull(loaded.theBytes);
    assertNull(dbBytes("a"));
  }

  /**
   * storeBatchToDatabase with null and non-null byte[] fields, insert then update
   */
  public void testBeanBatch() {
    List<BinaryRow> rows = new ArrayList<BinaryRow>();
    for (int i=0;i<5;i++) {
      rows.add(new BinaryRow("id" + i, "name" + i, i % 2 == 0 ? null : randomBytes(100 + i)));
    }
    new GcDbAccess().storeBatchToDatabase(rows, 200);

    for (int i=0;i<5;i++) {
      BinaryRow loaded = load("id" + i);
      assertEquals("name" + i, loaded.theName);
      if (i % 2 == 0) {
        assertNull(loaded.theBytes);
      } else {
        assertTrue(Arrays.equals(randomBytes(100 + i), loaded.theBytes));
      }
    }

    // flip which ones are null
    for (int i=0;i<5;i++) {
      rows.get(i).theBytes = i % 2 == 0 ? randomBytes(300 + i) : null;
    }
    new GcDbAccess().storeBatchToDatabase(rows, 200);

    for (int i=0;i<5;i++) {
      BinaryRow loaded = load("id" + i);
      if (i % 2 == 0) {
        assertTrue(Arrays.equals(randomBytes(300 + i), loaded.theBytes));
      } else {
        assertNull(loaded.theBytes);
      }
    }
  }

  /**
   * the typed null marker only applies to null byte[] fields, everything else passes through
   */
  public void testConvertNullForBind() {
    assertSame(GcDbTypedNull.BINARY, GcDbTypedNull.convertNullForBind(byte[].class, null));
    byte[] bytes = new byte[] {1, 2};
    assertSame(bytes, GcDbTypedNull.convertNullForBind(byte[].class, bytes));
    assertNull(GcDbTypedNull.convertNullForBind(String.class, null));
    assertEquals("x", GcDbTypedNull.convertNullForBind(String.class, "x"));
    assertEquals("null", GcDbTypedNull.BINARY.toString());
  }
}
