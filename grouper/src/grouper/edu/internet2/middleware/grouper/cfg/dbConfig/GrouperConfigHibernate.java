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

import java.sql.Timestamp;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import edu.internet2.middleware.grouper.GrouperAPI;
import edu.internet2.middleware.grouper.exception.GroupDeleteException;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.hib3.Hib3GrouperVersioned;
import edu.internet2.middleware.grouper.internal.util.GrouperUuid;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * database configuration
 * @author mchyzer
 */
@SuppressWarnings("serial")
public class GrouperConfigHibernate extends GrouperAPI implements Hib3GrouperVersioned, Comparable<GrouperConfigHibernate> {

  /** db uuid for this row */
  public static final String COLUMN_ID = "id";

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
      FIELD_CONFIG_KEY, FIELD_CONFIG_SEQUENCE, FIELD_CONFIG_VALUE, FIELD_CONFIG_VERSION_INDEX, 
      FIELD_CONTEXT_ID, FIELD_ID, FIELD_LAST_UPDATED_DB);

  /**
   * fields which are included in clone method
   */
  private static final Set<String> CLONE_FIELDS = GrouperUtil.toSet(
      FIELD_CONFIG_COMMENT, FIELD_CONFIG_ENCRYPTED, FIELD_CONFIG_FILE_HIERARCHY, FIELD_CONFIG_FILE_NAME, 
      FIELD_CONFIG_KEY, FIELD_CONFIG_SEQUENCE, FIELD_CONFIG_VALUE, FIELD_CONFIG_VERSION_INDEX, 
      FIELD_CONTEXT_ID, FIELD_DB_VERSION, FIELD_HIBERNATE_VERSION_NUMBER, FIELD_ID, 
      FIELD_LAST_UPDATED_DB);

  //*****  END GENERATED WITH GenerateFieldConstants.java *****//
  
  /** hibernate version */
  public static final String COLUMN_HIBERNATE_VERSION_NUMBER = "hibernate_version_number";

  /** key of the property */
  public static final String COLUMN_CONFIG_KEY = "config_key";

  /** value of the property */
  public static final String COLUMN_CONFIG_VALUE = "config_value";
  
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
   * value of the property
   * @return the configValue
   */
  public String getConfigValue() {
    String theConfigValueUnencrypted = this.configValue;
    
    if (this.isConfigEncrypted()) {
      throw new RuntimeException("Implement this");
    }
    
    return theConfigValueUnencrypted;
  }
  
  /**
   * value of the property
   * @param configValue1 the configValue to set
   */
  public void setConfigValueDb(String configValue1) {
    this.configValue = configValue1;
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
   * save or update this object
   */
  public void saveOrUpdate() {
    
    GrouperDAOFactory.getFactory().getConfig().saveOrUpdate(this);
  }

  /**
   * delete this object
   */
  public void delete() {
    GrouperDAOFactory.getFactory().getConfig().delete(this);
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
  public void setConfigValue(String configValue1) {
    this.configValue = configValue1;
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

}
