package edu.internet2.middleware.grouper.file;

import java.util.Set;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.GrouperAPI;
import edu.internet2.middleware.grouper.internal.dao.hib3.Hib3GrouperVersioned;
import edu.internet2.middleware.grouper.misc.GrouperHasContext;
import edu.internet2.middleware.grouper.util.GrouperUtil;

@SuppressWarnings("serial")
public class GrouperFile extends GrouperAPI implements GrouperHasContext, Hib3GrouperVersioned {
  
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
  
  /** constant for field name for: id */
  public static final String FIELD_ID = "id";
  
  /** constant for field name for: systemName */
  public static final String FIELD_SYSTEM_NAME = "systemName";
  
  /** constant for field name for: contextId */
  public static final String FIELD_CONTEXT_ID = "contextId";
  
  /** constant for field name for: fileName */
  public static final String FIELD_FILE_NAME = "fileName";

  /** constant for field name for: filePath */
  public static final String FIELD_FILE_PATH = "filePath";

  /** constant for field name for: fileContentsVarchar */
  public static final String FIELD_FILE_CONTENTS_VARCHAR = "fileContentsVarchar";

  /** constant for field name for: fileContentsBytes */
  public static final String FIELD_FILE_CONTENTS_BYTES = "fileContentsBytes";

  /** constant for field name for: fileContentsClob */
  public static final String FIELD_FILE_CONTENTS_CLOB = "fileContentsClob";

  /**
   * name of the table in the database.
   */
  public static final String TABLE_GROUPER_FILE = "grouper_file";
  
  /**
   * fields which are included in db version
   */
  private static final Set<String> DB_VERSION_FIELDS = GrouperUtil.toSet(
      FIELD_SYSTEM_NAME, FIELD_FILE_NAME, FIELD_FILE_PATH, FIELD_FILE_CONTENTS_VARCHAR, 
      FIELD_FILE_CONTENTS_BYTES, FIELD_FILE_CONTENTS_CLOB, FIELD_CONTEXT_ID, FIELD_ID);

  /**
   * fields which are included in clone method
   */
  private static final Set<String> CLONE_FIELDS = GrouperUtil.toSet(
      FIELD_SYSTEM_NAME, FIELD_FILE_NAME, FIELD_FILE_PATH, FIELD_FILE_CONTENTS_VARCHAR, 
      FIELD_FILE_CONTENTS_BYTES, FIELD_FILE_CONTENTS_CLOB, 
      FIELD_CONTEXT_ID, FIELD_DB_VERSION, FIELD_HIBERNATE_VERSION_NUMBER, FIELD_ID);
  
  
  /** id of this type */
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
    return id;
  }
  
  /** context id ties multiple db changes */
  private String contextId;
  
  
  /**
   * @return context id
   */
  public String getContextId() {
    return contextId;
  }

  /**
   * set context id
   * @param contextId1
   */
  public void setContextId(String contextId1) {
    this.contextId = contextId1;
  }
  
  /**
   * system name
   */
  private String systemName;
  

  
  public String getSystemName() {
    return systemName;
  }

  
  public void setSystemName(String systemName) {
    this.systemName = systemName;
  }
  
  
  private String fileName;
  
  
  public String getFileName() {
    return fileName;
  }

  
  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

  private String filePath;
  
  
  public String getFilePath() {
    return filePath;
  }

  
  public void setFilePath(String filePath) {
    this.filePath = filePath;
  }

  
  
  /**
   * value of the property
   */
  private String fileContentsVarchar;
  
  /**
   * value of the property
   * @return the configValue
   */
  public String getFileContentsVarcharDb() {
    return this.fileContentsVarchar;
  }
  
  public void setFileContentsVarcharDb(String fileContentsVarchar) {
    this.fileContentsVarchar = fileContentsVarchar;
  }
  
  private String fileContentsClob;
  
  public String getFileContentsClobDb() {
    return fileContentsClob;
  }

  
  public void setFileContentsClobDb(String fileContentsClob) {
    this.fileContentsClob = fileContentsClob;
  }

  /**
   * retrieve value. based on the size, it will be retrieved from file_contents_varchar or file_contents_clob
   * @return
   */
  public String retrieveValue() {
    
    if (StringUtils.isNotBlank(fileContentsVarchar)) {
      return fileContentsVarchar;
    }
    
    return fileContentsClob;
    
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
    return fileContentsBytes;
  }

  /**
   * size of file contents in bytes
   * @param the fileContentsBytes
   */
  public void setFileContentsBytes(Long fileContentsBytes) {
    this.fileContentsBytes = fileContentsBytes;
  }
  
  /**
   * set config value to save. based on the size, it will be saved in config_value or config_value_clob
   * @param value
   */
  public void setValueToSave(String value) {
    int lengthAscii = GrouperUtil.lengthAscii(value);
    if (GrouperUtil.lengthAscii(value) <= 3000) {
      this.fileContentsVarchar = value;
      this.fileContentsClob = null;
    } else {
      this.fileContentsClob = value;
      this.fileContentsVarchar = null;
    }
    this.fileContentsBytes = new Long(lengthAscii);
  }
  
  @Override
  public GrouperAPI clone() {
    return GrouperUtil.clone(this, CLONE_FIELDS);
  }
  
  /**
   * take a snapshot of the data since this is what is in the db
   */
  @Override
  public void dbVersionReset() {
    //lets get the state from the db so we know what has changed
    this.dbVersion = GrouperUtil.clone(this, DB_VERSION_FIELDS);
  }

}
