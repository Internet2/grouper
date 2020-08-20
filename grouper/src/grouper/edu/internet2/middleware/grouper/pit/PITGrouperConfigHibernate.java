package edu.internet2.middleware.grouper.pit;

import java.sql.Timestamp;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.GrouperAPI;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileHierarchy;
import edu.internet2.middleware.grouper.internal.dao.hib3.Hib3GrouperVersioned;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.util.GrouperUtil;

@SuppressWarnings("serial")
public class PITGrouperConfigHibernate extends GrouperPIT implements Hib3GrouperVersioned {
  
  /** db id for this row */
  public static final String COLUMN_ID = "id";
  
  /** column */
  public static final String COLUMN_SOURCE_ID = "source_id";

  /** Context id links together multiple operations into one high level action */
  public static final String COLUMN_CONTEXT_ID = "context_id";

  /** hibernate version */
  public static final String COLUMN_HIBERNATE_VERSION_NUMBER = "hibernate_version_number";

  /** key of the property */
  public static final String COLUMN_CONFIG_KEY = "config_key";

  /** value of the property */
  public static final String COLUMN_CONFIG_VALUE = "config_value";
  
  /** value when it's too big for config_value */
  public static final String COLUMN_CONFIG_VALUE_CLOB = "config_value_clob";
  
  /** size of the config value */
  public static final String COLUMN_CONFIG_VALUE_BYTES = "config_value_bytes";
  
  /** comment of the property */
  public static final String COLUMN_CONFIG_COMMENT = "config_comment";
  
  /** if there is more data than fits in the column this is the 0 indexed order */
  public static final String COLUMN_CONFIG_SEQUENCE = "config_sequence";
  
  /** config file, e.g. grouper.properties */
  public static final String COLUMN_CONFIG_FILE_NAME = "config_file_name";
  
  /** config file hierarchy, e.g. BASE, INSTITUTION, ENVIRONMENT, GROUPER_ENGINE */
  public static final String COLUMN_CONFIG_FILE_HIERARCHY = "config_file_hierarchy";
    
  /** millis since 1970 this row was last updated */
  public static final String COLUMN_LAST_UPDATED = "last_updated";
    
  /**
   * for built in configs, this is the index that will identify if the database configs should be replaced from the java code
   */
  public static final String COLUMN_CONFIG_VERSION_INDEX = "config_version_index";
  
  /**
   * if this is a password or for whatever other reason is encrypted
   */
  public static final String COLUMN_CONFIG_ENCRYPTED = "config_encrypted";

  /** constant for field name for: id */
  public static final String FIELD_ID = "id";
  
  /** constant for field name for: sourceId */
  public static final String FIELD_SOURCE_ID = "sourceId";
  
  /** constant for field name for: contextId */
  public static final String FIELD_CONTEXT_ID = "contextId";
  
  /** constant for field name for: configComment */
  public static final String FIELD_CONFIG_COMMENT = "configComment";

  /** constant for field name for: configEncrypted */
  public static final String FIELD_CONFIG_ENCRYPTED = "configEncrypted";

  /** constant for field name for: configFileHierarchy */
  public static final String FIELD_CONFIG_FILE_HIERARCHY = "configFileHierarchy";

  /** constant for field name for: configFileName */
  public static final String FIELD_CONFIG_FILE_NAME = "configFileName";

  /** constant for field name for: configKey */
  public static final String FIELD_CONFIG_KEY = "configKey";

  /** constant for field name for: configSequence */
  public static final String FIELD_CONFIG_SEQUENCE = "configSequence";

  /** constant for field name for: configValue */
  public static final String FIELD_CONFIG_VALUE = "configValue";

  /** constant for field name for: configValueClob */
  public static final String FIELD_CONFIG_VALUE_CLOB = "configValueClob";

  /** constant for field name for: configValueBytes */
  public static final String FIELD_CONFIG_VALUE_BYTES = "configValueBytes";

  /** constant for field name for: configVersionIndex */
  public static final String FIELD_CONFIG_VERSION_INDEX = "configVersionIndex";

  /** constant for field name for: lastUpdatedDb */
  public static final String FIELD_LAST_UPDATED_DB = "lastUpdatedDb";
  
  /**
   * name of the table in the database.
   */
  public static final String TABLE_GROUPER_PIT_CONFIG = "grouper_pit_config";
  
  /** id of this type */
  private String id;

  /** context id ties multiple db changes */
  private String contextId;

  /** sourceId */
  private String sourceId;
  
  /**
   * millis since 1970 that this record was last updated
   */
  private Long lastUpdatedDb;
  
  /**
   * when last updated
   * @return timestamp
   */
  public Timestamp getLastUpdated() {
    return this.lastUpdatedDb == null ? null : new Timestamp(this.lastUpdatedDb);
  }

  /**
   * when last updated
   * @return timestamp
   */
  public Long getLastUpdatedDb() {
    return this.lastUpdatedDb;
  }
  
  /**
   * when last updated
   * @param lastUpdated1
   */
  public void setLastUpdated(Timestamp lastUpdated1) {
    this.lastUpdatedDb = lastUpdated1 == null ? null : lastUpdated1.getTime();
  }

  /**
   * when last updated
   * @param lastUpdated1
   */
  public void setLastUpdatedDb(Long lastUpdated1) {
    this.lastUpdatedDb = lastUpdated1;
  }
  

  /**
   * for built in configs, this is the index that will identify if the database configs should be replaced from the java code
   */
  private int configVersionIndex = 0;
  
  
  /**
   * for built in configs, this is the index that will identify if the database configs should be replaced from the java code
   * @return the configVersionIndex
   */
  public int getConfigVersionIndex() {
    return this.configVersionIndex;
  }

  
  /**
   * for built in configs, this is the index that will identify if the database configs should be replaced from the java code
   * @param configVersionIndex1 the configVersionIndex to set
   */
  public void setConfigVersionIndex(int configVersionIndex1) {
    this.configVersionIndex = configVersionIndex1;
  }

  /**
   * value of the property
   */
  private String configValue;
  
  /**
   * value of the property
   * @return the configValue
   */
  public String getConfigValueDb() {
    return this.configValue;
  }
  
  
  private String previousConfigValue;
  
  public String getPreviousConfigValueDb() {
    return this.previousConfigValue;
  }
  
  public void setPreviousConfigValueDb(String previousConfigValue) {
    this.previousConfigValue = previousConfigValue;
  }
  
  /**
   * get previous value for the config. Try config value first and if null return config value clob
   * @return
   */
  public String getPreviousValue() {
    if (StringUtils.isNotBlank(previousConfigValue)) {
      return previousConfigValue;
    }
    
    return previousConfigValueClob;
  }
  
  /**
   * get current value for the config. Try config value first and if null return config value clob
   * @return
   */
  public String getValue() {
    
    if (StringUtils.isNotBlank(configValue)) {
      return configValue;
    }
    
    return configValueClob;
  }
  
  /**
   * set current value to save in db. Based on the length we either save in config value or config value clob 
   * @param value
   */
  public void setValueToSave(String value) {
    int lengthAscii = GrouperUtil.lengthAscii(value);
    if (GrouperUtil.lengthAscii(value) <= 3000) {
      this.configValue = value;
      this.configValueClob = null;
    } else {
      this.configValueClob = value;
      this.configValue = null;
    }
    this.configValueBytes = new Long(lengthAscii);
  }
  
  
  /**
   * value of the property
   * @param configValue1 the configValue to set
   */
  public void setConfigValueDb(String configValue1) {
    this.configValue = configValue1;
  }
  
  /**
   * config value clob. it's used when configValue can't hold the data
   */
  private String configValueClob;
  
  /**
   * clob value of the property
   * @return the configValueClob
   */
  public String getConfigValueClobDb() {
    return this.configValueClob;
  }
  
  /**
   * clob value of the property
   * @return the configValueClob
   */
  public String getConfigValueClob() {
    String theConfigValueUnencrypted = this.configValueClob;
    
    if (this.isConfigEncrypted()) {
      throw new RuntimeException("Implement this");
    }
    
    return theConfigValueUnencrypted;
  }
  
  /**
   * value of the property
   * @param configValueClob1 the configValueClob to set
   */
  public void setConfigValueClobDb(String configValueClob1) {
    this.configValueClob = configValueClob1;
  }
  
  /** previous version of config value clob */
  private String previousConfigValueClob;
  
  /**
   * @return previous version of config value clob
   */
  public String getPreviousConfigValueClobDb() {
    return previousConfigValueClob;
  }

  /**
   * previous version of config value clob
   * @param previousConfigValueClob
   */
  public void setPreviousConfigValueClobDb(String previousConfigValueClob) {
    this.previousConfigValueClob = previousConfigValueClob;
  }


  /**
   * size of config value in bytes
   */
  private Long configValueBytes;
  
  /**
   * size of config value in bytes
   * @return the configValueBytes
   */
  public Long getConfigValueBytes() {
    return configValueBytes;
  }

  /**
   * size of config value in bytes
   * @param the configValueBytes
   */
  public void setConfigValueBytes(Long configValueBytes) {
    this.configValueBytes = configValueBytes;
  }
  
  /**
   * if there is more data than fits in the column this is the 0 indexed order
   */
  private int configSequence = 0;
  
  /**
   * if there is more data than fits in the column this is the 0 indexed order
   * @return the configSequence
   */
  public int getConfigSequence() {
    return this.configSequence;
  }
  
  /**
   * if there is more data than fits in the column this is the 0 indexed order
   * @param configSequence1 the configSequence to set
   */
  public void setConfigSequence(int configSequence1) {
    this.configSequence = configSequence1;
  }

  /**
   * key of the property 
   */
  private String configKey;
  
  
  /**
   * key of the property 
   * @return the configKey
   */
  public String getConfigKey() {
    return this.configKey;
  }

  /**
   * key of the property 
   * @param configKey1 the configKey to set
   */
  public void setConfigKey(String configKey1) {
    this.configKey = configKey1;
  }

  /**
   * config file, e.g. grouper.properties
   */
  private String configFileName;
  
  /**
   * config file, e.g. grouper.properties
   * @return the configFileName
   */
  public String getConfigFileNameDb() {
    return this.configFileName;
  }

  /**
   * config file, e.g. grouper.properties
   * @param configFileName the configFileName to set
   */
  public void setConfigFileNameDb(String configFileName) {
    this.configFileName = configFileName;
  }

  /**
   * @return the configFileHierarchy
   */
  public String getConfigFileHierarchyDb() {
    return this.configFileHierarchy;
  }

  
  /**
   * @param configFileHierarchy the configFileHierarchy to set
   */
  public void setConfigFileHierarchyDb(String configFileHierarchy) {
    this.configFileHierarchy = configFileHierarchy;
  }

  /**
   * @return the configFileHierarchy
   */
  public ConfigFileHierarchy getConfigFileHierarchy() {
    return ConfigFileHierarchy.valueOfIgnoreCase(this.configFileHierarchy, false);
  }

  
  /**
   * @param configFileHierarchyEnum the configFileHierarchy to set
   */
  public void setConfigFileHierarchy(ConfigFileHierarchy configFileHierarchyEnum) {
    this.configFileHierarchy = configFileHierarchyEnum == null ? null : configFileHierarchyEnum.name();
  }

  /**
   * config file hierarchy, e.g. base, institution, or env
   */
  private String configFileHierarchy;
  
  /**
   * comment of the property
   */
  private String configComment;
  
  /**
   * @return the configComment
   */
  public String getConfigComment() {
    return this.configComment;
  }

  /**
   * @param configComment1 the configComment to set
   */
  public void setConfigComment(String configComment1) {
    this.configComment = configComment1;
  }

  /**
   * if this is a password or for whatever other reason is encrypted
   */
  private boolean configEncrypted;

  /**
   * if this is a password or for whatever other reason is encrypted, T|F
   * @return if config encrypted
   */
  public String getConfigEncryptedDb() {
    return this.configEncrypted ? "T" : "F";
  }

  /**
   * if this is a password or for whatever other reason is encrypted
   * @param theConfigEncrypted
   */
  public void setConfigEncryptedDb(String theConfigEncrypted) {
    this.configEncrypted = GrouperUtil.booleanValue(theConfigEncrypted, false);
  }
  
  /**
   * if this is a password or for whatever other reason is encrypted
   * @return the configEncrypted
   */
  public boolean isConfigEncrypted() {
    return this.configEncrypted;
  }
  
  /**
   * if this is a password or for whatever other reason is encrypted
   * @param configEncrypted1 the configEncrypted to set
   */
  public void setConfigEncrypted(boolean configEncrypted1) {
    this.configEncrypted = configEncrypted1;
  }

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
   * clob value of the property
   * @param configValueClob1 the configValueClob to set
   */
  public void setConfigValueClob(String configValueClob1) {
    this.configValueClob = configValueClob1;
  }
  
  
  public String getSourceId() {
    return sourceId;
  }

  
  public void setSourceId(String sourceId) {
    this.sourceId = sourceId;
  }

  
  public String getConfigFileName() {
    return configFileName;
  }

  
  public void setConfigFileName(String configFileName) {
    this.configFileName = configFileName;
  }

  
  public void setConfigFileHierarchy(String configFileHierarchy) {
    this.configFileHierarchy = configFileHierarchy;
  }
  
  /**
   * fields which are included in clone method
   */
  private static final Set<String> CLONE_FIELDS = GrouperUtil.toSet(
      FIELD_CONFIG_COMMENT, FIELD_CONFIG_ENCRYPTED, FIELD_CONFIG_FILE_HIERARCHY, FIELD_CONFIG_FILE_NAME, 
      FIELD_CONFIG_KEY, FIELD_CONFIG_SEQUENCE, FIELD_CONFIG_VALUE, FIELD_CONFIG_VALUE_CLOB, FIELD_CONFIG_VALUE_BYTES, 
      FIELD_CONFIG_VERSION_INDEX, FIELD_CONTEXT_ID, FIELD_DB_VERSION, FIELD_HIBERNATE_VERSION_NUMBER, FIELD_ID, 
      FIELD_LAST_UPDATED_DB, FIELD_ACTIVE_DB, FIELD_START_TIME_DB, FIELD_END_TIME_DB, FIELD_SOURCE_ID);
  
  
  @Override
  public GrouperAPI clone() {
    return GrouperUtil.clone(this, CLONE_FIELDS);
  }
  
  
  /**
   * save or update this object
   */
  public void saveOrUpdate() {
    GrouperDAOFactory.getFactory().getPITConfig().saveOrUpdate(this);
  }
  
  /**
   * delete this object
   */
  public void delete() {
    GrouperDAOFactory.getFactory().getPITConfig().delete(this);
  }
  
  
}
