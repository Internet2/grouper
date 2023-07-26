package edu.internet2.middleware.grouper.sqlCache;

import java.sql.Timestamp;

import edu.internet2.middleware.grouperClient.jdbc.GcPersist;
import edu.internet2.middleware.grouperClient.jdbc.GcPersistableClass;
import edu.internet2.middleware.grouperClient.jdbc.GcPersistableField;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;

@GcPersistableClass(tableName="grouper_sql_cache_depend_type", defaultFieldPersist=GcPersist.doPersist)
public class SqlCacheDependencyType {

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
   */
  private SqlCacheDependencyTypeType ownerTypeEnum;

  /**
   * owner_type  varchar 
   * G means group
   * D means data field
   * @return
   */
  public SqlCacheDependencyTypeType getOwnerTypeEnum() {
    return ownerTypeEnum;
  }

  /**
   * owner_type  varchar 
   * G means group
   * D means data field
   * @param ownerTypeEnum
   */
  public void setOwnerTypeEnum(SqlCacheDependencyTypeType ownerTypeEnum) {
    this.ownerTypeEnum = ownerTypeEnum;
  }

//  /**
//   * deep clone the fields in this object
//   */
//  @Override
//  public SqlCacheGroup clone() {
//  
//    SqlCacheGroup sqlCacheGroup = new SqlCacheGroup();
//  
//    //dbVersion  DONT CLONE
//  
//    sqlCacheGroup.createdOn = this.createdOn;
//    sqlCacheGroup.disabledOn = this.disabledOn;
//    sqlCacheGroup.fieldInternalId = this.fieldInternalId;
//    sqlCacheGroup.groupInternalId = this.groupInternalId;
//    sqlCacheGroup.internalId = this.internalId;
//    sqlCacheGroup.membershipSize = this.membershipSize;
//    sqlCacheGroup.membershipSizeHst = this.membershipSizeHst;
//  
//    return sqlCacheGroup;
//  }
//  /**
//   * db version
//   */
//  @Override
//  public void dbVersionDelete() {
//    this.dbVersion = null;
//  }
//  /**
//   * if we need to update this object
//   * @return if needs to update this object
//   */
//  @Override
//  public boolean dbVersionDifferent() {
//    return !this.equalsDeep(this.dbVersion);
//  }
//  /**
//   * take a snapshot of the data since this is what is in the db
//   */
//  @Override
//  public void dbVersionReset() {
//    //lets get the state from the db so we know what has changed
//    this.dbVersion = this.clone();
//  }
//  /**
//   *
//   */
//  public boolean equalsDeep(Object obj) {
//    if (this==obj) {
//      return true;
//    }
//    if (obj == null) {
//      return false;
//    }
//    if (!(obj instanceof GrouperDictionary)) {
//      return false;
//    }
//    SqlCacheGroup other = (SqlCacheGroup) obj;
//  
//    return new EqualsBuilder()
//  
//  
//      //dbVersion  DONT EQUALS
//      .append(this.createdOn, other.createdOn)
//      .append(this.disabledOn, other.disabledOn)
//      .append(this.enabledOn, other.enabledOn)
//      .append(this.fieldInternalId, other.fieldInternalId)
//      .append(this.groupInternalId, other.groupInternalId)
//      .append(this.internalId, other.internalId)
//      .append(this.membershipSize, other.membershipSize)
//      .append(this.membershipSizeHst, other.membershipSizeHst)
//        .isEquals();
//  
//  }
//  /**
//   * 
//   */
//  @Override
//  public boolean gcSqlAssignNewPrimaryKeyForInsert() {
//    if (this.internalId != -1) {
//      return false;
//    }
//    if (this.tempInternalIdOnDeck != null) {
//      this.internalId = this.tempInternalIdOnDeck;
//    } else {
//      this.internalId = TableIndex.reserveId(TableIndexType.sqlGroupCache);
//    }
//    return true;
//  }
//  /**
//   * when this row was created
//   * @return
//   */
//  public Timestamp getCreatedOn() {
//    return createdOn;
//  }
//  public SqlCacheGroup getDbVersion() {
//    return this.dbVersion;
//  }
//  /**
//   * internal integer id
//   * @return
//   */
//  public long getInternalId() {
//    return internalId;
//  }
//  /**
//   * when this row was created
//   * @param createdOn
//   */
//  public void setCreatedOn(Timestamp createdOn) {
//    this.createdOn = createdOn;
//  }
//  /**
//   * internal integer id
//   * @param internalId
//   */
//  public void setInternalId(long internalId) {
//    this.internalId = internalId;
//  }
//  /**
//   * store the internal id to use when the db access stores the object
//   * @param tempInternalIdOnDeck
//   */
//  public void setTempInternalIdOnDeck(Long tempInternalIdOnDeck) {
//    this.tempInternalIdOnDeck = tempInternalIdOnDeck;
//  }
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