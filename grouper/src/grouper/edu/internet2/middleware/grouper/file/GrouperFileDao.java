package edu.internet2.middleware.grouper.file;

import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;

/**
 * dao for grouper_file, with GcDbAccess (GRP-7440, was Hib3GrouperFileDAO).
 *
 * <p>Methods that only need the id or file name select just that column, so the (possibly large) contents
 * are not loaded.  Saves are optimistic locked on hibernate_version_number (see GrouperFile).  Note: like
 * before, GcDbAccess uses its own connection, it does not join a hibernate transaction.</p>
 */
public class GrouperFileDao {

  /**
   * no instances, all static
   */
  private GrouperFileDao() {
  }

  /**
   * delete all rows, for unit tests
   */
  public static void reset() {
    new GcDbAccess().sql("delete from " + GrouperFile.TABLE_GROUPER_FILE).executeSql();
  }

  /**
   * find by id, including the contents
   * @param id
   * @param exceptionIfNotFound
   * @return the file or null if not found
   */
  public static GrouperFile findById(String id, boolean exceptionIfNotFound) {
    GrouperFile grouperFile = new GcDbAccess()
        .sql("select * from " + GrouperFile.TABLE_GROUPER_FILE + " where " + GrouperFile.COLUMN_ID + " = ?")
        .addBindVar(id).select(GrouperFile.class);

    if (grouperFile == null && exceptionIfNotFound) {
      throw new RuntimeException("Cant find file by id: " + id);
    }

    return grouperFile;
  }

  /**
   * find by file path (file_path is unique), including the contents
   * @param filePath
   * @param exceptionIfNotFound
   * @return the file or null if not found
   */
  public static GrouperFile findByFilePath(String filePath, boolean exceptionIfNotFound) {
    GrouperFile grouperFile = new GcDbAccess()
        .sql("select * from " + GrouperFile.TABLE_GROUPER_FILE + " where " + GrouperFile.COLUMN_FILE_PATH + " = ?")
        .addBindVar(filePath).select(GrouperFile.class);

    if (grouperFile == null && exceptionIfNotFound) {
      throw new RuntimeException("Cant find file by path: " + filePath);
    }

    return grouperFile;
  }

  /**
   * find the id by system name and file path without loading the contents
   * @param systemName
   * @param filePath
   * @return the id or null if not found
   */
  public static String findIdBySystemNameAndFilePath(String systemName, String filePath) {
    return new GcDbAccess()
        .sql("select " + GrouperFile.COLUMN_ID + " from " + GrouperFile.TABLE_GROUPER_FILE
            + " where " + GrouperFile.COLUMN_SYSTEM_NAME + " = ? and " + GrouperFile.COLUMN_FILE_PATH + " = ?")
        .addBindVar(systemName).addBindVar(filePath).select(String.class);
  }

  /**
   * find the file name by id without loading the contents
   * @param id
   * @return the file name or null if not found
   */
  public static String findFileNameById(String id) {
    return new GcDbAccess()
        .sql("select " + GrouperFile.COLUMN_FILE_NAME + " from " + GrouperFile.TABLE_GROUPER_FILE
            + " where " + GrouperFile.COLUMN_ID + " = ?")
        .addBindVar(id).select(String.class);
  }

  /**
   * see if a row exists without loading the contents
   * @param id
   * @return true if there is a grouper_file row with this id
   */
  public static boolean existsById(String id) {
    return findFileNameById(id) != null;
  }

  /**
   * insert or update.  Inserts if the version is -1 (never saved), else updates and throws
   * GcStaleObjectException if the row was changed or deleted since it was loaded.  The context id and
   * created / updated timestamps are set by GrouperFile.dbPreStore (GcDbAccessLifecycle) during the store.
   * @param grouperFile
   */
  public static void store(GrouperFile grouperFile) {
    GrouperUtil.assertion(grouperFile != null, "grouperFile is null");
    GrouperUtil.assertion(grouperFile.getId() != null, "grouperFile id is null");

    new GcDbAccess().storeToDatabase(grouperFile);
  }

  /**
   * delete by id without loading the contents (no version check)
   * @param id
   * @return number of rows deleted
   */
  public static int deleteById(String id) {
    return new GcDbAccess()
        .sql("delete from " + GrouperFile.TABLE_GROUPER_FILE + " where " + GrouperFile.COLUMN_ID + " = ?")
        .addBindVar(id).executeSql();
  }

}
