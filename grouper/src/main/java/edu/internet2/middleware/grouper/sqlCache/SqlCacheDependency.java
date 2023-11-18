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

@GcPersistableClass(tableName="grouper_sql_cache_dependency", defaultFieldPersist=GcPersist.doPersist)
public class SqlCacheDependency implements GcSqlAssignPrimaryKey, GcDbVersionable {

  public SqlCacheDependency() {
  }
  
  /**
   * the thing that something is dependent on
   */
  private long ownerInternalId = -1;
  
  public long getOwnerInternalId() {
    return ownerInternalId;
  }
  
  public void setOwnerInternalId(long ownerInternalId) {
    this.ownerInternalId = ownerInternalId;
  }

  /**
   * the thing that depends on something else
   */
  private long dependentInternalId = -1;
  
  
  public long getDependentInternalId() {
    return dependentInternalId;
  }

  
  /**
   * SqlCacheDependencyType or SqlCacheDependencyTypeType
   */
  @GcPersistableField(columnName = "dep_type_internal_id")
  private long dependencyTypeInternalId = -1;
  
  
  public long getDependencyTypeInternalId() {
    return dependencyTypeInternalId;
  }

  
  public void setDependencyTypeInternalId(long dependencyTypeInternalId) {
    this.dependencyTypeInternalId = dependencyTypeInternalId;
  }

  /**
   * when this row was created
   */
  private Timestamp createdOn;
  /**
   * version from db
   */
  @GcPersistableField(persist = GcPersist.dontPersist)
  private SqlCacheDependency dbVersion;
  /**
   * internal integer id
   */
  @GcPersistableField(primaryKey=true, primaryKeyManuallyAssigned=true)
  private long internalId = -1;
  
  /**
   * deep clone the fields in this object
   */
  @Override
  public SqlCacheDependency clone() {
  
    SqlCacheDependency sqlCacheDependency = new SqlCacheDependency();
  
    //dbVersion  DONT CLONE
    sqlCacheDependency.createdOn = this.createdOn;
    sqlCacheDependency.dependencyTypeInternalId = sqlCacheDependency.dependencyTypeInternalId;
    sqlCacheDependency.dependentInternalId = sqlCacheDependency.dependentInternalId;
    sqlCacheDependency.internalId = this.internalId;
    sqlCacheDependency.ownerInternalId = this.ownerInternalId;
  
    return sqlCacheDependency;
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
    if (!(obj instanceof SqlCacheDependency)) {
      return false;
    }
    SqlCacheDependency other = (SqlCacheDependency) obj;
  
    return new EqualsBuilder()
  
  
      //dbVersion  DONT EQUALS
      .append(this.createdOn, other.createdOn)
      .append(this.dependencyTypeInternalId, other.dependencyTypeInternalId)
      .append(this.dependentInternalId, other.dependentInternalId)
      .append(this.internalId, other.internalId)
      .append(this.ownerInternalId, other.ownerInternalId)
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
      this.internalId = TableIndex.reserveId(TableIndexType.sqlCacheDependency);
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