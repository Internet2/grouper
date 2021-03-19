/**
 * Copyright 2018 Internet2
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.internet2.middleware.grouper.cfg.dbConfig;

import java.io.File;
import java.sql.Timestamp;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.GrouperAPI;
import edu.internet2.middleware.grouper.audit.AuditEntry;
import edu.internet2.middleware.grouper.audit.AuditTypeBuiltin;
import edu.internet2.middleware.grouper.cache.GrouperCacheDatabase;
import edu.internet2.middleware.grouper.cache.GrouperCacheDatabaseClear;
import edu.internet2.middleware.grouper.cache.GrouperCacheDatabaseClearInput;
import edu.internet2.middleware.grouper.hibernate.AuditControl;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.hibernate.HibernateHandler;
import edu.internet2.middleware.grouper.hibernate.HibernateHandlerBean;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.internal.dao.hib3.Hib3GrouperVersioned;
import edu.internet2.middleware.grouper.internal.util.GrouperUuid;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.pit.PITGrouperConfigHibernate;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.config.ConfigPropertiesCascadeBase;
import edu.internet2.middleware.grouperClient.config.db.ConfigDatabaseLogic;
import edu.internet2.middleware.morphString.Morph;
import edu.internet2.middleware.subject.config.SubjectConfig;

/**
 * database configuration
 * @author mchyzer
 */
@SuppressWarnings("serial")
public class GrouperConfigHibernate extends GrouperAPI implements Hib3GrouperVersioned, Comparable<GrouperConfigHibernate>, GrouperCacheDatabaseClear {

  public static final String ESCAPED_PASSWORD = "*******";
  
  /** db uuid for this row */
  public static final String COLUMN_ID = "id";

  private static Pattern subjectPropertiesConfigPattern = Pattern.compile("^subjectApi\\.source\\.([^.]+)\\..*$");

  private static Log LOG = LogFactory.getLog(GrouperConfigHibernate.class);

  /**
   * @see edu.internet2.middleware.grouper.GrouperAPI#onPreSave(edu.internet2.middleware.grouper.hibernate.HibernateSession)
   */
  @Override
  public void onPreSave(HibernateSession hibernateSession) {
    
    if (StringUtils.isBlank(this.id)) {
      this.setId(GrouperUuid.getUuid() );
    }
    
    super.onPreSave(hibernateSession);
    this.truncate();
    this.internal_setModifiedIfNeeded();
  }

  /**
   * 
   */
  private void internal_setModifiedIfNeeded() {
    this.setLastUpdatedDb(System.currentTimeMillis() );
  }

  /**
   * @see edu.internet2.middleware.grouper.GrouperAPI#onPreUpdate(edu.internet2.middleware.grouper.hibernate.HibernateSession)
   */
  @Override
  public void onPreUpdate(HibernateSession hibernateSession) {
    super.onPreUpdate(hibernateSession);
    this.truncate();
    this.internal_setModifiedIfNeeded();

  }

  //*****  START GENERATED WITH GenerateFieldConstants.java *****//

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

  /** constant for field name for: contextId */
  public static final String FIELD_CONTEXT_ID = "contextId";

  /** constant for field name for: id */
  public static final String FIELD_ID = "id";

  /** constant for field name for: lastUpdatedDb */
  public static final String FIELD_LAST_UPDATED_DB = "lastUpdatedDb";

  /**
   * fields which are included in db version
   */
  private static final Set<String> DB_VERSION_FIELDS = GrouperUtil.toSet(
      FIELD_CONFIG_COMMENT, FIELD_CONFIG_ENCRYPTED, FIELD_CONFIG_FILE_HIERARCHY, FIELD_CONFIG_FILE_NAME, 
      FIELD_CONFIG_KEY, FIELD_CONFIG_SEQUENCE, FIELD_CONFIG_VALUE, FIELD_CONFIG_VALUE_CLOB, FIELD_CONFIG_VALUE_BYTES,
      FIELD_CONFIG_VERSION_INDEX, FIELD_CONTEXT_ID, FIELD_ID, FIELD_LAST_UPDATED_DB);

  /**
   * fields which are included in clone method
   */
  private static final Set<String> CLONE_FIELDS = GrouperUtil.toSet(
      FIELD_CONFIG_COMMENT, FIELD_CONFIG_ENCRYPTED, FIELD_CONFIG_FILE_HIERARCHY, FIELD_CONFIG_FILE_NAME, 
      FIELD_CONFIG_KEY, FIELD_CONFIG_SEQUENCE, FIELD_CONFIG_VALUE, FIELD_CONFIG_VALUE_CLOB, FIELD_CONFIG_VALUE_BYTES, 
      FIELD_CONFIG_VERSION_INDEX, FIELD_CONTEXT_ID, FIELD_DB_VERSION, FIELD_HIBERNATE_VERSION_NUMBER, FIELD_ID, 
      FIELD_LAST_UPDATED_DB);

  //*****  END GENERATED WITH GenerateFieldConstants.java *****//
  
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
  
  /**
   * retrieve value. based on the size, it will be retrieved from config_value or config_value_clob
   * @return
   */
  public String retrieveValue() {
    
    if (StringUtils.isNotBlank(configValue)) {
      return configValue;
    }
    
    return configValueClob;
    
  }
  
  /**
   * set config value to save. based on the size, it will be saved in config_value or config_value_clob
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
   * name of the table in the database.
   */
  public static final String TABLE_GROUPER_CONFIG = "grouper_config";

  /** context id ties multiple db changes */
  private String contextId;

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
   * create a new PIT grouper config entry in the database based on the grouper config
   * @param auditEntryId
   * @param config
   */
  public static void createNewPITGrouperConfigHibernate(String auditEntryId, String newActiveStatus,
      GrouperConfigHibernate config, String previousConfigValue, String previousConfigValueClob) {
    PITGrouperConfigHibernate pit = new PITGrouperConfigHibernate();
    pit.setId(GrouperUuid.getUuid());
    pit.setSourceId(config.getId());
    pit.setContextId(auditEntryId);
    pit.setActiveDb(newActiveStatus);
    pit.setStartTimeDb(System.currentTimeMillis() * 1000);
    pit.setConfigComment(config.getConfigComment());
    pit.setConfigEncryptedDb(config.getConfigEncryptedDb());
    pit.setConfigFileHierarchyDb(config.getConfigFileHierarchyDb());
    pit.setConfigFileNameDb(config.getConfigFileNameDb());
    pit.setConfigKey(config.getConfigKey());
    pit.setConfigSequence(config.getConfigSequence());
    
    if ("T".equals(newActiveStatus)) {      
      pit.setValueToSave(config.retrieveValue());
    }
    
    pit.setLastUpdatedDb(config.getLastUpdatedDb());
    pit.setConfigVersionIndex(config.getConfigVersionIndex());
    pit.setPreviousConfigValueDb(previousConfigValue);
    pit.setPreviousConfigValueClobDb(previousConfigValueClob);
    
    pit.saveOrUpdate();
  }
  
  /**
   * save or update this object
   */
  public void saveOrUpdate(boolean addNew) {
    
    HibernateSession.callbackHibernateSession(GrouperTransactionType.READ_WRITE_NEW, 
        AuditControl.WILL_AUDIT, new HibernateHandler() {
          
          @Override
          public Object callback(HibernateHandlerBean hibernateHandlerBean)
              throws GrouperDAOException {
            
            GrouperDAOFactory.getFactory().getConfig().saveOrUpdate(GrouperConfigHibernate.this);
            
            String valueForAudit = GrouperConfigHibernate.this.isConfigEncrypted() ? ESCAPED_PASSWORD : GrouperConfigHibernate.this.retrieveValue();
        
              AuditTypeBuiltin auditTypeBuiltin = addNew ? AuditTypeBuiltin.CONFIGURATION_ADD : AuditTypeBuiltin.CONFIGURATION_UPDATE;
              
              AuditEntry auditEntry = new AuditEntry(auditTypeBuiltin, "id", 
                  GrouperConfigHibernate.this.getId(), "configFile", GrouperConfigHibernate.this.getConfigFileNameDb(), 
                  "key", GrouperConfigHibernate.this.getConfigKey(), "value", 
                  valueForAudit, "configHierarchy", GrouperConfigHibernate.this.getConfigFileHierarchyDb());
              
              auditEntry.setDescription((addNew ? "Add" : "Update") + " config entry: " + GrouperConfigHibernate.this.getConfigFileNameDb() 
                + ", " + GrouperConfigHibernate.this.getConfigKey() + " = " + valueForAudit);
              auditEntry.saveOrUpdate(true);
              
              if (addNew) {
                createNewPITGrouperConfigHibernate(auditEntry.getId(), "T", GrouperConfigHibernate.this, null, null);
              } else {
                PITGrouperConfigHibernate pit = GrouperDAOFactory.getFactory().getPITConfig().findBySourceIdActive(GrouperConfigHibernate.this.id, false);
                if (pit != null) {
                  pit.setActiveDb("F");
                  pit.setEndTimeDb(System.currentTimeMillis() * 1000);
                  pit.saveOrUpdate();
                }
                createNewPITGrouperConfigHibernate(auditEntry.getId(), "T",
                    GrouperConfigHibernate.this, pit.getConfigValueDb(), pit.getConfigValueClobDb());
                
              }
            return null;
          }
        });
    
    updateLastUpdated();
    reloadSubjectSourceIfApplicable();
  }
  
  /**
   * see if password based on various factors
   * @param configFileName if known or null if not
   * @param configItemMetadata if known or null if not
   * @param key or null if not known
   * @param value if there is one at this point or null if not
   * @param hasValue true if there is a value, false if not
   * @param userSelectedPassword true if the user selected that this is a password.   null if NA
   * @return true if password
   */
  public static boolean isPassword(ConfigFileName configFileName, ConfigItemMetadata configItemMetadata, String key, String value, boolean hasValue, Boolean userSelectedPassword) {
    return isPasswordHelper(configFileName, configItemMetadata, key, value, hasValue, userSelectedPassword);
  }
  
  /**
   * see if password based on various factors
   * @param configItemMetadata
   * @return true if password
   */
  public static boolean isPasswordHelper(ConfigItemMetadata configItemMetadata) {
    
    if (configItemMetadata != null) {
      return configItemMetadata.isSensitive() || configItemMetadata.getValueType() == ConfigItemMetadataType.PASSWORD;
    }
    return false;
  }
  
  /**
   * if the value is a file and it exists, then this is not a password to be escaped
   * @param configItemMetadata
   * @param propertyValueString
   * @return true if should be escaped
   */
  public static boolean isPasswordHelper(ConfigItemMetadata configItemMetadata,
      String propertyValueString) {
    if (StringUtils.isBlank(propertyValueString)) {
      return false;
    }
    if (propertyValueString.length() > 5 && new File(propertyValueString).exists()) {
      return false;
    }
    return isPasswordHelper(configItemMetadata);
  }
  
  /**
   * see if password based on various factors
   * @param configFileName
   * @param configItemMetadata
   * @param key
   * @param value
   * @param hasValue
   * @param userSelectedPassword
   * @return true if password
   */
  public static boolean isPasswordHelper(ConfigFileName configFileName, ConfigItemMetadata configItemMetadata, 
      String key, String value, boolean hasValue, Boolean userSelectedPassword) {
  
    if (key != null && key.endsWith(".elConfig")) {
      // this is a script, not a password
      return false;
    }
    
    if (hasValue && !StringUtils.isBlank(value)) {
      try {
        if (StringUtils.isNotBlank(Morph.decrypt(value))) {
          return true;
        }
      } catch (Exception e) {
        // ignore
      }
    }
    
    // if there is a value, and it is a file, then its not a password
    if (hasValue && !StringUtils.isBlank(value)) {
      File theFile = new File(value);
      if (theFile.exists() && theFile.isFile()) {
        return false;
      }
    }
    
    // if the configured metadata is not null then check that
    if (isPasswordHelper(configItemMetadata)) {
      return true;
    }
    
    // look for a key with certain words inside
    if (key != null) {
      String lowerKey = key.toLowerCase();
  
      if (lowerKey.contains("pass") || lowerKey.contains("secret") || lowerKey.contains("private")) {
        return true;
      }
    
      //lets try to find the config item metadata by key to be sure
      if (configItemMetadata == null) {
        configItemMetadata = ConfigFileName.findConfigItemMetdata(key);
      }
      if (isPasswordHelper(configItemMetadata)) {
        return true;
      }
    }
  
    // if the user selected that this is a password, then i guess it is
    if (userSelectedPassword != null && userSelectedPassword) {
      return true;
    }
    
    return false;
  }

  /**
   * delete this object
   */
  public void delete() {
    
    HibernateSession.callbackHibernateSession(GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, 
        AuditControl.WILL_AUDIT, new HibernateHandler() {
          
          @Override
          public Object callback(HibernateHandlerBean hibernateHandlerBean)
              throws GrouperDAOException {
            
            GrouperDAOFactory.getFactory().getConfig().delete(GrouperConfigHibernate.this);
            
            boolean isValueEncrypted = GrouperConfigHibernate.isPassword(GrouperConfigHibernate.this.getConfigFileName(), null, GrouperConfigHibernate.this.getConfigKey(), 
                GrouperConfigHibernate.this.retrieveValue(), true, GrouperConfigHibernate.this.isConfigEncrypted());

            String valueForAudit = isValueEncrypted ? ESCAPED_PASSWORD : GrouperConfigHibernate.this.retrieveValue();

            AuditEntry auditEntry = new AuditEntry(AuditTypeBuiltin.CONFIGURATION_DELETE, "id", 
                GrouperConfigHibernate.this.getId(), "configFile", GrouperConfigHibernate.this.getConfigFileNameDb(), 
                "key", GrouperConfigHibernate.this.getConfigKey(), "previousValue", 
                valueForAudit, 
                    "configHierarchy", GrouperConfigHibernate.this.getConfigFileHierarchyDb());
            auditEntry.setDescription("Delete config entry: " + GrouperConfigHibernate.this.getConfigFileNameDb() 
              + ", " + GrouperConfigHibernate.this.getConfigKey() + " = " + valueForAudit);
            auditEntry.saveOrUpdate(true);
            
            PITGrouperConfigHibernate pit = GrouperDAOFactory.getFactory().getPITConfig().findBySourceIdActive(id, false);
            if (pit == null) {
              return null;
            }
            
            pit.setEndTimeDb(System.currentTimeMillis() * 1000);
            pit.setActiveDb("F");
            pit.saveOrUpdate();
            
            createNewPITGrouperConfigHibernate(auditEntry.getId(), "F",
                GrouperConfigHibernate.this, pit.getConfigValueDb(), pit.getConfigValueClobDb());
            
            return null;
          }
        });
    
    updateLastUpdated();
    reloadSubjectSourceIfApplicable();
  }

  /**
   * 
   */
  private static boolean databaseCacheRegistered = false;
  
  public static void registerDatabaseCache() {

    if (!databaseCacheRegistered) {
      
      GrouperCacheDatabase.customRegisterDatabaseClearable(ConfigDatabaseLogic.DATABASE_CACHE_KEY, new GrouperConfigHibernate());
      
      databaseCacheRegistered = true;
    }
    
  }
  
  /**
   * update last updated if something changed
   */
  public static void updateLastUpdated() {
    GrouperCacheDatabase.customNotifyDatabaseOfChanges(ConfigDatabaseLogic.DATABASE_CACHE_KEY);
    clearConfigsInMemory();
  }
  
  /**
   * make sure this object will fit in the DB
   */
  public void truncate() {
    this.configComment = GrouperUtil.truncateAscii(this.configComment, 4000);
    String originalValue = this.configValue;
    String newValue = GrouperUtil.truncateAscii(this.configValue, 4000);
    if (!StringUtils.equals(originalValue, newValue)) {
      throw new RuntimeException("config value is too long: '" + this.configValue + "'");
    }
  }

  /**
   * value of the property
   * @param configValue1 the configValue to set
   */
//  public void setConfigValue(String configValue1) {
//    this.configValue = configValue1;
//  }
  
  /**
   * clob value of the property
   * @param configValueClob1 the configValueClob to set
   */
  public void setConfigValueClob(String configValueClob1) {
    this.configValueClob = configValueClob1;
  }
  
  /**
   * @param failIfNull 
   * @return the set of different fields
   * @see edu.internet2.middleware.grouper.GrouperAPI#dbVersionDifferentFields()
   */
  public Set<String> dbVersionDifferentFields(boolean failIfNull) {
    if (this.dbVersion == null) {
      if (failIfNull) {
        throw new RuntimeException("State was never stored from db");
      }
      return null;
    }
    //easier to unit test if everything is ordered
    Set<String> result = GrouperUtil.compareObjectFields(this, this.dbVersion,
        DB_VERSION_FIELDS, null);

    return result;
  }

  /**
   * take a snapshot of the data since this is what is in the db
   */
  @Override
  public void dbVersionReset() {
    //lets get the state from the db so we know what has changed
    this.dbVersion = GrouperUtil.clone(this, DB_VERSION_FIELDS);
  }

  /**
   * deep clone the fields in this object
   */
  @Override
  public GrouperConfigHibernate clone() {
    return GrouperUtil.clone(this, CLONE_FIELDS);
  }

  /**
   * 
   * @see java.lang.Object#equals(java.lang.Object)
   */
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof GrouperConfigHibernate)) {
      return false;
    }
    GrouperConfigHibernate otherConfig = (GrouperConfigHibernate) other;

    if (StringUtils.equals(this.getId(), otherConfig.getId())) {
      return true;
    }
    
    return new EqualsBuilder()
      .append( this.getConfigKey(), otherConfig.getConfigKey())
      .append( this.getConfigFileName(), otherConfig.getConfigFileName())
      .append( this.getConfigFileHierarchy(), otherConfig.getConfigFileHierarchy())
      .append( this.getConfigSequence(), otherConfig.getConfigSequence())
      .isEquals();
  } // public boolean equals(other)

  /**
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  public int compareTo(GrouperConfigHibernate otherConfig) {
    if (otherConfig == null) {
      return 1;
    }
    return new CompareToBuilder()
      .append( this.getConfigFileName(), otherConfig.getConfigFileName())
      .append( this.getConfigFileHierarchy(), otherConfig.getConfigFileHierarchy())
      .append( this.getConfigKey(), otherConfig.getConfigKey())
      .append( this.getConfigSequence(), otherConfig.getConfigSequence())
      .toComparison();
  }

  /**
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    return new HashCodeBuilder()
      .append( this.getConfigFileName())
      .append( this.getConfigFileHierarchy())
      .append( this.getConfigKey())
      .append( this.getConfigSequence())
      .toHashCode();
  }

  /**
   * config file, e.g. grouper.properties
   * @param configFileName the configFileName to set
   */
  public void setConfigFileName(ConfigFileName configFileName) {
    this.configFileName = configFileName == null ? null : configFileName.getConfigFileName();
  }

  /**
   * @return the configFileName
   */
  public ConfigFileName getConfigFileName() {
    return ConfigFileName.valueOfIgnoreCase(this.configFileName, false);
  }

  /**
   * clear the cache when the database tells us to
   */
  @Override
  public void clear(GrouperCacheDatabaseClearInput grouperCacheDatabaseClearInput) {
    clearConfigsInMemory();
  }

  /**
   * clear the cache when the database tells us to
   */
  public static void clearConfigsInMemory() {
    ConfigDatabaseLogic.clearCache(false);
    ConfigPropertiesCascadeBase.clearCacheThisOnly();
  }
  
  private void reloadSubjectSourceIfApplicable() {
    if ("subject.properties".equals(this.configFileName) && !StringUtils.isEmpty(this.configKey)) {
      Matcher matcher = subjectPropertiesConfigPattern.matcher(this.configKey);
      if (matcher.matches()) {
        String sourceConfigId = matcher.group(1);
        String sourceId = SubjectConfig.retrieveConfig().propertyValueString("subjectApi.source." + sourceConfigId + ".id");
        
        if (!StringUtils.isEmpty(sourceId)) {
          try {
            // TODO when the new subject source ui is created, this should probably be called after it validates the config

            GrouperCacheDatabase.notifyDatabaseOfCacheUpdate("custom__edu.internet2.middleware.subject.provider.SourceManager.reloadSource____" + sourceId, false);
          } catch (Exception e) {
            LOG.error("Failed to reload subject source " + sourceId, e);
          }
        }
      }
    }
  }
}
