package edu.internet2.middleware.grouper.file;

import java.nio.charset.StandardCharsets;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.hibernate.GrouperContext;
import edu.internet2.middleware.grouper.internal.util.GrouperUuid;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccessLifecycle;
import edu.internet2.middleware.grouperClient.jdbc.GcPersist;
import edu.internet2.middleware.grouperClient.jdbc.GcPersistableClass;
import edu.internet2.middleware.grouperClient.jdbc.GcPersistableField;

/**
 * a row in grouper_file: a file stored in the database for reports, workflow, gsh template downloads, etc.
 *
 * <p>Persisted with GcDbAccess (GRP-7440, was hibernate), see {@link GrouperFileDao}.  Field names map to
 * column names (fileContentsVarchar to file_contents_varchar, etc).  hibernate_version_number is the
 * optimistic locking version: -1 means not saved yet (insert), 0 or more means saved (update, checked
 * against the database version).  The context id and created / updated timestamps are set in
 * {@link #dbPreStore(boolean)}, like the hibernate GrouperAPI.onPreSave did.</p>
 */
@GcPersistableClass(tableName=GrouperFile.TABLE_GROUPER_FILE, defaultFieldPersist=GcPersist.doPersist)
public class GrouperFile implements GcDbAccessLifecycle {

  /** db id for this row */
  public static final String COLUMN_ID = "id";

  /** System name this file belongs to eg: workflow */
  public static final String COLUMN_SYSTEM_NAME = "system_name";

  /** Name of the file */
  public static final String COLUMN_FILE_NAME = "file_name";

  /** Unique path of the file */
  public static final String COLUMN_FILE_PATH = "file_path";

  /** Context id links together multiple operations into one high level action */
  public static final String COLUMN_CONTEXT_ID = "context_id";

  /** contents of the file if can fit into 4000 bytes */
  public static final String COLUMN_FILE_CONTENTS_VARCHAR = "file_contents_varchar";

  /** large contents of the file */
  public static final String COLUMN_FILE_CONTENTS_CLOB = "file_contents_clob";

  /** size of file contents in bytes */
  public static final String COLUMN_FILE_CONTENTS_BYTES = "file_contents_bytes";

  /** micros since 1970 when this row was inserted, never changed after that */
  public static final String COLUMN_CREATED_ON_MICROS = "created_on_micros";

  /** micros since 1970 when this row was last saved (insert or update) */
  public static final String COLUMN_UPDATED_ON_MICROS = "updated_on_micros";

  /** binary contents of the file (zip, xlsx, etc), null if the contents are text (GRP-7446) */
  public static final String COLUMN_FILE_CONTENTS_BLOB = "file_contents_blob";

  /** optimistic locking version of the row */
  public static final String COLUMN_HIBERNATE_VERSION_NUMBER = "hibernate_version_number";

  /**
   * name of the table in the database.
   */
  public static final String TABLE_GROUPER_FILE = "grouper_file";

  /**
   * columns other than the contents, select these when the (possibly large) contents are not needed
   */
  public static final String METADATA_COLUMNS = COLUMN_ID + ", " + COLUMN_SYSTEM_NAME + ", " + COLUMN_FILE_NAME
      + ", " + COLUMN_FILE_PATH + ", " + COLUMN_CONTEXT_ID + ", " + COLUMN_FILE_CONTENTS_BYTES
      + ", " + COLUMN_CREATED_ON_MICROS + ", " + COLUMN_UPDATED_ON_MICROS + ", " + COLUMN_HIBERNATE_VERSION_NUMBER;

  /** uuid of this row, assigned by the caller before the first save */
  @GcPersistableField(primaryKey=true, primaryKeyManuallyAssigned=true)
  private String id;

  /**
   * @param id1 the id to set
   */
  public void setId(String id1) {
    this.id = id1;
  }

  /**
   * @return id
   */
  public String getId() {
    return this.id;
  }

  /** context id ties multiple db changes, set in dbPreStore on save */
  private String contextId;

  /**
   * @return context id
   */
  public String getContextId() {
    return this.contextId;
  }

  /**
   * set context id
   * @param contextId1
   */
  public void setContextId(String contextId1) {
    this.contextId = contextId1;
  }

  /**
   * system name, e.g. workflow, report, gshTemplateDownload
   */
  private String systemName;

  /**
   * @return system name
   */
  public String getSystemName() {
    return this.systemName;
  }

  /**
   * @param systemName1
   */
  public void setSystemName(String systemName1) {
    this.systemName = systemName1;
  }

  /** name of the file e.g. for the download */
  private String fileName;

  /**
   * @return file name
   */
  public String getFileName() {
    return this.fileName;
  }

  /**
   * @param fileName1
   */
  public void setFileName(String fileName1) {
    this.fileName = fileName1;
  }

  /** unique path of the file */
  private String filePath;

  /**
   * @return file path
   */
  public String getFilePath() {
    return this.filePath;
  }

  /**
   * @param filePath1
   */
  public void setFilePath(String filePath1) {
    this.filePath = filePath1;
  }

  /**
   * contents if they fit in the varchar column, null if they are in the clob (or not selected)
   */
  private String fileContentsVarchar;

  /**
   * contents if they fit in the varchar column
   * @return the contents or null
   */
  public String getFileContentsVarcharDb() {
    return this.fileContentsVarchar;
  }

  /**
   * @param fileContentsVarchar1
   */
  public void setFileContentsVarcharDb(String fileContentsVarchar1) {
    this.fileContentsVarchar = fileContentsVarchar1;
  }

  /**
   * contents if they are too big for the varchar column, null otherwise (or not selected)
   */
  private String fileContentsClob;

  /**
   * contents if they are too big for the varchar column
   * @return the contents or null
   */
  public String getFileContentsClobDb() {
    return this.fileContentsClob;
  }

  /**
   * @param fileContentsClob1
   */
  public void setFileContentsClobDb(String fileContentsClob1) {
    this.fileContentsClob = fileContentsClob1;
  }

  /**
   * retrieve value. based on the size, it will be retrieved from file_contents_varchar or file_contents_clob.
   * Throws if the file is binary, use retrieveBytes() for that
   * @return the contents
   */
  public String retrieveValue() {

    // a binary file has no text, dont quietly hand back null
    if (this.fileContentsBlob != null) {
      throw new RuntimeException("File '" + this.filePath + "' is binary, use retrieveBytes()");
    }

    if (StringUtils.isNotBlank(this.fileContentsVarchar)) {
      return this.fileContentsVarchar;
    }

    return this.fileContentsClob;

  }

  /**
   * size of file contents in bytes
   */
  private Long fileContentsBytes;

  /**
   * size of file contents in bytes
   * @return the fileContentsBytes
   */
  public Long getFileContentsBytes() {
    return this.fileContentsBytes;
  }

  /**
   * size of file contents in bytes
   * @param fileContentsBytes1
   */
  public void setFileContentsBytes(Long fileContentsBytes1) {
    this.fileContentsBytes = fileContentsBytes1;
  }

  /**
   * set contents to save. based on the size, it will be saved in file_contents_varchar or file_contents_clob
   * @param value
   */
  public void setValueToSave(String value) {
    int lengthAscii = GrouperUtil.lengthAscii(value);
    if (lengthAscii <= 3000) {
      this.fileContentsVarchar = value;
      this.fileContentsClob = null;
    } else {
      this.fileContentsClob = value;
      this.fileContentsVarchar = null;
    }
    this.fileContentsBytes = Long.valueOf(lengthAscii);
    // a row is either text or binary
    this.fileContentsBlob = null;
  }

  /**
   * binary contents (zip, xlsx, etc), null if the contents are text.  Note: oracle stores an empty byte[] as
   * null, so an empty binary file reads back as an empty text file (retrieveBytes() is still an empty array)
   */
  private byte[] fileContentsBlob;

  /**
   * binary contents, null if the contents are text
   * @return the bytes or null
   */
  public byte[] getFileContentsBlobDb() {
    return this.fileContentsBlob;
  }

  /**
   * @param fileContentsBlob1
   */
  public void setFileContentsBlobDb(byte[] fileContentsBlob1) {
    this.fileContentsBlob = fileContentsBlob1;
  }

  /**
   * set binary contents to save (GRP-7446), e.g. a zip.  Clears the text columns, a row is either text or binary.
   * file_contents_bytes is the byte length, so grouperFile.maxSizeBytes applies
   * @param bytes
   */
  public void setBytesToSave(byte[] bytes) {
    if (bytes == null) {
      throw new RuntimeException("Bytes cannot be null for file: " + this.filePath);
    }
    this.fileContentsBlob = bytes;
    this.fileContentsVarchar = null;
    this.fileContentsClob = null;
    this.fileContentsBytes = Long.valueOf(bytes.length);
  }

  /**
   * @return true if the contents are binary (file_contents_blob), false if text
   */
  public boolean isBinary() {
    return this.fileContentsBlob != null;
  }

  /**
   * retrieve the contents as bytes whether the file is binary or text (text as UTF-8), e.g. for a download.
   * Never null, an empty file is an empty array
   * @return the bytes
   */
  public byte[] retrieveBytes() {
    if (this.fileContentsBlob != null) {
      return this.fileContentsBlob;
    }
    String text = StringUtils.isNotBlank(this.fileContentsVarchar) ? this.fileContentsVarchar : this.fileContentsClob;
    return StringUtils.defaultString(text).getBytes(StandardCharsets.UTF_8);
  }

  /**
   * micros since 1970 when this row was inserted.  Set in dbPreStore on the first save, never changed after that.
   * Null only on a new object that has not been saved yet (the column is NOT NULL).
   */
  private Long createdOnMicros;

  /**
   * micros since 1970 when this row was inserted
   * @return the createdOnMicros
   */
  public Long getCreatedOnMicros() {
    return this.createdOnMicros;
  }

  /**
   * micros since 1970 when this row was inserted
   * @param createdOnMicros1
   */
  public void setCreatedOnMicros(Long createdOnMicros1) {
    this.createdOnMicros = createdOnMicros1;
  }

  /**
   * micros since 1970 when this row was last saved.  Set in dbPreStore on every save (insert and update),
   * so time-based cleanup can go by when the contents were last written.
   */
  private Long updatedOnMicros;

  /**
   * micros since 1970 when this row was last saved
   * @return the updatedOnMicros
   */
  public Long getUpdatedOnMicros() {
    return this.updatedOnMicros;
  }

  /**
   * micros since 1970 when this row was last saved
   * @param updatedOnMicros1
   */
  public void setUpdatedOnMicros(Long updatedOnMicros1) {
    this.updatedOnMicros = updatedOnMicros1;
  }

  /**
   * optimistic locking version (the column name is from when this table was hibernate mapped).
   * -1 means not saved yet so the next store is an insert, GcDbAccess sets 0 on insert and increments it
   * on each update, and an update with a version that does not match the database throws
   * GcStaleObjectException
   */
  @GcPersistableField(optimisticLockVersion=true)
  private Long hibernateVersionNumber = -1L;

  /**
   * @return the optimistic locking version, -1 if not saved yet
   */
  public Long getHibernateVersionNumber() {
    return this.hibernateVersionNumber;
  }

  /**
   * @param hibernateVersionNumber1
   */
  public void setHibernateVersionNumber(Long hibernateVersionNumber1) {
    this.hibernateVersionNumber = hibernateVersionNumber1;
  }

  /**
   * called by GcDbAccess before insert or update (GcDbAccessLifecycle, like hibernate onPreSave / onPreUpdate).
   * Sets the context id, created_on_micros once, and updated_on_micros on every save.  Safe to call more
   * than once for the same store (GcDbAccess may), it only sets fields.
   * @param isInsert true if insert, false if update
   */
  @Override
  public void dbPreStore(boolean isInsert) {

    // hibernate created a temporary inner context (new uuid) around the save if there was none, GcDbAccess has
    // no hibernate session, so use the current context if there is one (e.g. inside a UI or WS action), else a
    // new uuid like hibernate did.  false so it never trips audit.requireAuditsForAllActions
    String currentContextId = GrouperContext.retrieveContextId(false);
    this.contextId = currentContextId != null ? currentContextId : GrouperUuid.getUuid();

    long nowMicros = System.currentTimeMillis() * 1000L;

    // make sure updated always moves forward, even if two saves happen in the same millisecond
    if (this.updatedOnMicros != null && nowMicros <= this.updatedOnMicros) {
      nowMicros = this.updatedOnMicros + 1;
    }

    // created is set once on insert, never changed
    if (this.createdOnMicros == null) {
      this.createdOnMicros = nowMicros;
    }
    this.updatedOnMicros = nowMicros;
  }

}
