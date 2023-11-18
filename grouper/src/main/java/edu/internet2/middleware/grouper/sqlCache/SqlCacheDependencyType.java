package edu.internet2.middleware.grouper.sqlCache;

import java.sql.Timestamp;

import org.apache.commons.lang3.builder.EqualsBuilder;

import edu.internet2.middleware.grouper.tableIndex.TableIndex;
import edu.internet2.middleware.grouper.tableIndex.TableIndexType;
import edu.internet2.middleware.grouperClient.jdbc.GcDbVersionable;
import edu.internet2.middleware.grouperClient.jdbc.GcPersist;
import edu.internet2.middleware.grouperClient.jdbc.GcPersistableClass;
import edu.internet2.middleware.grouperClient.jdbc.GcPersistableField;
import edu.internet2.middleware.grouperClient.jdbc.GcSqlAssignPrimaryKey;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;

@GcPersistableClass(tableName="grouper_sql_cache_depend_type", defaultFieldPersist=GcPersist.doPersist)
public class SqlCacheDependencyType implements GcSqlAssignPrimaryKey, GcDbVersionable {

  public SqlCacheDependencyType() {
  }
  
  /**
   * alphaNumeric name of this dependency type
   */
  private String name;

  /**
   * alphaNumeric name of this dependency type
   * @return
   */
  public String getName() {
    return name;
  }

  /**
   * alphaNumeric name of this dependency type
   * @param name
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * describe the dependency and columns
   */
  private String description;
  
  /**
   * describe the dependency and columns
   * @return
   */
  public String getDescription() {
    return description;
  }

  /**
   * describe the dependency and columns
   * @param description
   */
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * when this row was created
   */
  private Timestamp createdOn;
  /**
   * version from db
   */
  @GcPersistableField(persist = GcPersist.dontPersist)
  private SqlCacheDependencyType dbVersion;
  /**
   * internal integer id
   */
  @GcPersistableField(primaryKey=true, primaryKeyManuallyAssigned=true)
  private long internalId = -1;
  
  /**
   * owner_type  varchar 
   * G means group
   * D means data field
   * @return
   */
  public SqlCacheDependencyTypeType getNameEnum() {
    return SqlCacheDependencyTypeType.valueOfIgnoreCase(name, false);
  }

  /**
   * owner_type  varchar 
   * G means group
   * D means data field
   * @param ownerTypeEnum
   */
  public void setNameEnum(SqlCacheDependencyTypeType ownerTypeEnum) {
    this.name = ownerTypeEnum == null ? null : ownerTypeEnum.name();
  }

  /**
   * deep clone the fields in this object
   */
  @Override
  public SqlCacheDependencyType clone() {
  
    SqlCacheDependencyType sqlCacheDependencyType = new SqlCacheDependencyType();
  
    //dbVersion  DONT CLONE
    sqlCacheDependencyType.createdOn = this.createdOn;
    sqlCacheDependencyType.description = sqlCacheDependencyType.description;
    sqlCacheDependencyType.internalId = this.internalId;
    sqlCacheDependencyType.name = this.name;
  
    return sqlCacheDependencyType;
  }

  /**
   * db version
   */
  @Override
  public void dbVersionDelete() {
    this.dbVersion = null;
  }
  /**
   * if we need to update this object
   * @return if needs to update this object
   */
  @Override
  public boolean dbVersionDifferent() {
    return !this.equalsDeep(this.dbVersion);
  }
  /**
   * take a snapshot of the data since this is what is in the db
   */
  @Override
  public void dbVersionReset() {
    //lets get the state from the db so we know what has changed
    this.dbVersion = this.clone();
  }
  
  /**
   * store the internal id to use when the db access stores the object
   */
  @GcPersistableField(persist = GcPersist.dontPersist)
  private Long tempInternalIdOnDeck = null;

  /**
   * store the internal id to use when the db access stores the object
   * @return
   */
  public Long getTempInternalIdOnDeck() {
    return tempInternalIdOnDeck;
  }

  /**
   * store the internal id to use when the db access stores the object
   * @param tempInternalIdOnDeck
   */
  public void setTempInternalIdOnDeck(Long tempInternalIdOnDeck) {
    this.tempInternalIdOnDeck = tempInternalIdOnDeck;
  }

  /**
   *
   */
  public boolean equalsDeep(Object obj) {
    if (this==obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof SqlCacheDependencyType)) {
      return false;
    }
    SqlCacheDependencyType other = (SqlCacheDependencyType) obj;
  
    return new EqualsBuilder()
  
  
      //dbVersion  DONT EQUALS
      .append(this.createdOn, other.createdOn)
      .append(this.description, other.description)
      .append(this.internalId, other.internalId)
      .append(this.name, other.name)
        .isEquals();
  
  }
  /**
   * 
   */
  @Override
  public boolean gcSqlAssignNewPrimaryKeyForInsert() {
    if (this.internalId != -1) {
      return false;
    }
    if (this.tempInternalIdOnDeck != null) {
      this.internalId = this.tempInternalIdOnDeck;
    } else {
      this.internalId = TableIndex.reserveId(TableIndexType.sqlCacheDependencyType);
    }
    return true;
  }

  public void storePrepare() {
    if (this.createdOn == null) {
      this.createdOn = new Timestamp(System.currentTimeMillis());
    }
  }

  /**
   * 
   */
  @Override
  public String toString() {
    return GrouperClientUtils.toStringReflection(this, null);
  }
}