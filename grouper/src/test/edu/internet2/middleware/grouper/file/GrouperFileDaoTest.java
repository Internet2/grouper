package edu.internet2.middleware.grouper.file;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Random;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.internal.util.GrouperUuid;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.jdbc.GcStaleObjectException;
import junit.textui.TestRunner;

/**
 * GRP-7440: grouper_file persisted with GcDbAccess (GrouperFileDao) instead of hibernate.  Inserts and updates,
 * optimistic locking on hibernate_version_number, context id, timestamps, varchar vs clob contents, the
 * finders that do not load the contents, and deletes.
 */
public class GrouperFileDaoTest extends GrouperTest {

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new GrouperFileDaoTest("testStaleUpdateThrows"));
  }

  /**
   * @param name
   */
  public GrouperFileDaoTest(String name) {
    super(name);
  }

  /**
   * new unsaved file
   * @param path file path (unique)
   * @param contents
   * @return the file, not saved
   */
  private static GrouperFile newFile(String path, String contents) {
    GrouperFile grouperFile = new GrouperFile();
    grouperFile.setId(GrouperUuid.getUuid());
    grouperFile.setSystemName("grp7440test");
    grouperFile.setFilePath(path);
    grouperFile.setFileName(StringUtils.substringAfterLast(path, "/"));
    grouperFile.setValueToSave(contents);
    return grouperFile;
  }

  /**
   * version straight from the database
   * @param id
   * @return the hibernate_version_number
   */
  private static Long dbVersion(String id) {
    return new GcDbAccess().sql("select hibernate_version_number from grouper_file where id = ?")
        .addBindVar(id).select(Long.class);
  }

  /**
   * insert then update: version 0 then 1, context id and timestamps set, contents round trip
   */
  public void testInsertAndUpdate() {

    // given
    GrouperFile grouperFile = newFile("/grp7440/a.txt", "small contents");
    assertEquals(Long.valueOf(-1), grouperFile.getHibernateVersionNumber());

    // when: insert
    GrouperFileDao.store(grouperFile);

    // then
    assertEquals(Long.valueOf(0), grouperFile.getHibernateVersionNumber());
    assertEquals(Long.valueOf(0), dbVersion(grouperFile.getId()));
    assertNotNull(grouperFile.getContextId());
    assertNotNull(grouperFile.getCreatedOnMicros());
    assertEquals(grouperFile.getCreatedOnMicros(), grouperFile.getUpdatedOnMicros());

    GrouperFile loaded = GrouperFileDao.findById(grouperFile.getId(), true);
    assertEquals("grp7440test", loaded.getSystemName());
    assertEquals("/grp7440/a.txt", loaded.getFilePath());
    assertEquals("a.txt", loaded.getFileName());
    assertEquals("small contents", loaded.retrieveValue());
    assertEquals("small contents", loaded.getFileContentsVarcharDb());
    assertNull(loaded.getFileContentsClobDb());
    assertEquals(Long.valueOf(14), loaded.getFileContentsBytes());
    assertEquals(grouperFile.getContextId(), loaded.getContextId());
    assertEquals(grouperFile.getCreatedOnMicros(), loaded.getCreatedOnMicros());
    assertEquals(Long.valueOf(0), loaded.getHibernateVersionNumber());

    // when: update the loaded copy with large contents (goes to the clob)
    String bigContents = StringUtils.repeat("abcdefghij", 1000);
    loaded.setValueToSave(bigContents);
    GrouperFileDao.store(loaded);

    // then: same row, version 1, created kept, updated advanced
    assertEquals(Long.valueOf(1), loaded.getHibernateVersionNumber());
    assertEquals(Long.valueOf(1), dbVersion(grouperFile.getId()));
    GrouperFile reloaded = GrouperFileDao.findById(grouperFile.getId(), true);
    assertEquals(bigContents, reloaded.retrieveValue());
    assertNull(reloaded.getFileContentsVarcharDb());
    assertEquals(bigContents, reloaded.getFileContentsClobDb());
    assertEquals(Long.valueOf(10000), reloaded.getFileContentsBytes());
    assertEquals(grouperFile.getCreatedOnMicros(), reloaded.getCreatedOnMicros());
    assertTrue(reloaded.getUpdatedOnMicros() > grouperFile.getUpdatedOnMicros());
    assertEquals(1, new GcDbAccess().sql("select count(*) from grouper_file where system_name = 'grp7440test'").select(int.class).intValue());
  }

  /**
   * the context id and timestamps come from GrouperFile.dbPreStore (GcDbAccessLifecycle), so they are set
   * even when the bean is stored with GcDbAccess directly instead of through GrouperFileDao
   */
  public void testLifecycleSetsFieldsOnDirectStore() {

    // given
    GrouperFile grouperFile = newFile("/grp7440/d.txt", "contents");
    assertNull(grouperFile.getContextId());
    assertNull(grouperFile.getCreatedOnMicros());

    // when
    new GcDbAccess().storeToDatabase(grouperFile);

    // then
    assertNotNull(grouperFile.getContextId());
    assertNotNull(grouperFile.getCreatedOnMicros());
    assertEquals(grouperFile.getCreatedOnMicros(), grouperFile.getUpdatedOnMicros());
    GrouperFile loaded = GrouperFileDao.findById(grouperFile.getId(), true);
    assertEquals(grouperFile.getContextId(), loaded.getContextId());
    assertEquals(grouperFile.getCreatedOnMicros(), loaded.getCreatedOnMicros());
    assertEquals(grouperFile.getUpdatedOnMicros(), loaded.getUpdatedOnMicros());
  }

  /**
   * two copies of the same row: saving the second after the first throws, and does not change the row
   */
  public void testStaleUpdateThrows() {

    // given
    GrouperFile grouperFile = newFile("/grp7440/b.txt", "v1");
    GrouperFileDao.store(grouperFile);
    GrouperFile copy1 = GrouperFileDao.findById(grouperFile.getId(), true);
    GrouperFile copy2 = GrouperFileDao.findById(grouperFile.getId(), true);

    // when: first copy saved
    copy1.setValueToSave("v2");
    GrouperFileDao.store(copy1);

    // then: second copy is stale
    copy2.setValueToSave("v3");
    try {
      GrouperFileDao.store(copy2);
      fail("expected GcStaleObjectException");
    } catch (GcStaleObjectException gsoe) {
      // expected
    }
    assertEquals("v2", GrouperFileDao.findById(grouperFile.getId(), true).retrieveValue());
    assertEquals(Long.valueOf(1), dbVersion(grouperFile.getId()));

    // a row deleted out from under a copy is also stale (not re-inserted)
    GrouperFileDao.deleteById(grouperFile.getId());
    copy1.setValueToSave("v4");
    try {
      GrouperFileDao.store(copy1);
      fail("expected GcStaleObjectException");
    } catch (GcStaleObjectException gsoe) {
      // expected
    }
    assertNull(GrouperFileDao.findById(grouperFile.getId(), false));
  }

  /**
   * finders that do not load the contents, find by path, and deletes
   */
  public void testFindersAndDelete() {

    // given
    GrouperFile grouperFile = newFile("/grp7440/c.csv", "a,b\n1,2\n");
    GrouperFileDao.store(grouperFile);

    // then
    assertEquals("c.csv", GrouperFileDao.findFileNameById(grouperFile.getId()));
    assertNull(GrouperFileDao.findFileNameById("notAnId"));
    assertTrue(GrouperFileDao.existsById(grouperFile.getId()));
    assertFalse(GrouperFileDao.existsById("notAnId"));
    assertEquals(grouperFile.getId(), GrouperFileDao.findIdBySystemNameAndFilePath("grp7440test", "/grp7440/c.csv"));
    assertNull(GrouperFileDao.findIdBySystemNameAndFilePath("otherSystem", "/grp7440/c.csv"));
    assertEquals("a,b\n1,2\n", GrouperFileDao.findByFilePath("/grp7440/c.csv", true).retrieveValue());
    assertNull(GrouperFileDao.findByFilePath("/grp7440/missing.csv", false));
    assertNull(GrouperFileDao.findById("notAnId", false));
    try {
      GrouperFileDao.findById("notAnId", true);
      fail("expected exception");
    } catch (RuntimeException re) {
      // expected
    }

    // when / then: delete by id
    assertEquals(1, GrouperFileDao.deleteById(grouperFile.getId()));
    assertEquals(0, GrouperFileDao.deleteById(grouperFile.getId()));
    assertFalse(GrouperFileDao.existsById(grouperFile.getId()));
  }

  /**
   * grouperFile.maxSizeBytes: at the limit stores, over the limit throws on insert and update and stores nothing,
   * negative means no limit
   */
  public void testMaxSize() {

    // default is 50MB
    assertEquals(50 * 1024 * 1024, GrouperFileDao.DEFAULT_MAX_SIZE_BYTES);

    try {
      GrouperConfig.retrieveConfig().propertiesOverrideMap().put(GrouperFileDao.CONFIG_MAX_SIZE_BYTES, "10");
      assertEquals(10, GrouperFileDao.maxSizeBytes());

      // at the limit is ok
      GrouperFile grouperFile = newFile("/grp7444/a.txt", "0123456789");
      GrouperFileDao.store(grouperFile);
      assertEquals("0123456789", GrouperFileDao.findById(grouperFile.getId(), true).retrieveValue());

      // update over the limit throws and leaves the row alone
      grouperFile.setValueToSave("0123456789a");
      try {
        GrouperFileDao.store(grouperFile);
        fail("expected exception");
      } catch (RuntimeException re) {
        assertTrue(re.getMessage(), re.getMessage().contains(GrouperFileDao.CONFIG_MAX_SIZE_BYTES));
      }
      assertEquals("0123456789", GrouperFileDao.findById(grouperFile.getId(), true).retrieveValue());

      // insert over the limit throws and inserts nothing
      GrouperFile bigFile = newFile("/grp7444/b.txt", "0123456789a");
      try {
        GrouperFileDao.store(bigFile);
        fail("expected exception");
      } catch (RuntimeException re) {
        // expected
      }
      assertFalse(GrouperFileDao.existsById(bigFile.getId()));

      // -1 is no limit
      GrouperConfig.retrieveConfig().propertiesOverrideMap().put(GrouperFileDao.CONFIG_MAX_SIZE_BYTES, "-1");
      GrouperFileDao.store(bigFile);
      assertEquals("0123456789a", GrouperFileDao.findById(bigFile.getId(), true).retrieveValue());

      GrouperFileDao.deleteById(grouperFile.getId());
      GrouperFileDao.deleteById(bigFile.getId());
    } finally {
      GrouperConfig.retrieveConfig().propertiesOverrideMap().remove(GrouperFileDao.CONFIG_MAX_SIZE_BYTES);
    }
  }

  /**
   * GRP-7446: binary contents in file_contents_blob.  A ~5MB random byte array round trips (by id and by path),
   * the text columns are null for a binary row, switching text to binary and back clears the other columns,
   * retrieveValue() throws on a binary row, retrieveBytes() works for both, and grouperFile.maxSizeBytes
   * applies to the byte length
   */
  public void testBinaryContents() {

    // given: random bytes (not valid utf-8, so any text conversion would corrupt them)
    byte[] bigBytes = new byte[5 * 1024 * 1024 + 7];
    new Random(7446).nextBytes(bigBytes);
    GrouperFile grouperFile = newFile("/grp7446/a.zip", "placeholder");
    grouperFile.setBytesToSave(bigBytes);
    assertTrue(grouperFile.isBinary());
    assertNull(grouperFile.getFileContentsVarcharDb());
    assertNull(grouperFile.getFileContentsClobDb());

    // when
    GrouperFileDao.store(grouperFile);

    // then: binary round trip by id and by path
    GrouperFile loaded = GrouperFileDao.findById(grouperFile.getId(), true);
    assertTrue(loaded.isBinary());
    assertTrue(Arrays.equals(bigBytes, loaded.getFileContentsBlobDb()));
    assertTrue(Arrays.equals(bigBytes, loaded.retrieveBytes()));
    assertNull(loaded.getFileContentsVarcharDb());
    assertNull(loaded.getFileContentsClobDb());
    assertEquals(Long.valueOf(bigBytes.length), loaded.getFileContentsBytes());
    assertTrue(Arrays.equals(bigBytes, GrouperFileDao.findByFilePath("/grp7446/a.zip", true).retrieveBytes()));
    try {
      loaded.retrieveValue();
      fail("binary file has no text value");
    } catch (RuntimeException re) {
      assertTrue(re.getMessage(), re.getMessage().contains("retrieveBytes"));
    }

    // when: same row switched to text
    loaded.setValueToSave("now text");
    GrouperFileDao.store(loaded);

    // then: blob cleared
    GrouperFile textFile = GrouperFileDao.findById(grouperFile.getId(), true);
    assertFalse(textFile.isBinary());
    assertNull(textFile.getFileContentsBlobDb());
    assertEquals("now text", textFile.retrieveValue());
    assertTrue(Arrays.equals("now text".getBytes(StandardCharsets.UTF_8), textFile.retrieveBytes()));

    // when: back to binary (small)
    byte[] smallBytes = new byte[] {0, 1, 2, (byte)0xff, (byte)0xfe};
    textFile.setBytesToSave(smallBytes);
    GrouperFileDao.store(textFile);

    // then: text cleared
    GrouperFile binaryAgain = GrouperFileDao.findById(grouperFile.getId(), true);
    assertTrue(Arrays.equals(smallBytes, binaryAgain.retrieveBytes()));
    assertNull(binaryAgain.getFileContentsVarcharDb());
    assertNull(binaryAgain.getFileContentsClobDb());
    assertEquals(Long.valueOf(5), binaryAgain.getFileContentsBytes());

    // the max size applies to binary files too
    try {
      GrouperConfig.retrieveConfig().propertiesOverrideMap().put(GrouperFileDao.CONFIG_MAX_SIZE_BYTES, "4");
      binaryAgain.setBytesToSave(new byte[] {1, 2, 3, 4, 5});
      try {
        GrouperFileDao.store(binaryAgain);
        fail("expected exception");
      } catch (RuntimeException re) {
        assertTrue(re.getMessage(), re.getMessage().contains(GrouperFileDao.CONFIG_MAX_SIZE_BYTES));
      }
    } finally {
      GrouperConfig.retrieveConfig().propertiesOverrideMap().remove(GrouperFileDao.CONFIG_MAX_SIZE_BYTES);
    }

    GrouperFileDao.deleteById(grouperFile.getId());
  }

}
