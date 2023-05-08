package edu.internet2.middleware.grouper.sqlCache;

import java.sql.Timestamp;

import edu.internet2.middleware.grouper.dictionary.GrouperDictionary;
import edu.internet2.middleware.grouper.tableIndex.TableIndex;
import edu.internet2.middleware.grouper.tableIndex.TableIndexType;
import edu.internet2.middleware.grouperClient.jdbc.GcDbVersionable;
import edu.internet2.middleware.grouperClient.jdbc.GcPersist;
import edu.internet2.middleware.grouperClient.jdbc.GcPersistableClass;
import edu.internet2.middleware.grouperClient.jdbc.GcPersistableField;
import edu.internet2.middleware.grouperClient.jdbc.GcSqlAssignPrimaryKey;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.lang3.builder.EqualsBuilder;

@GcPersistableClass(tableName="grouper_sql_cache_mship", defaultFieldPersist=GcPersist.doPersist)
public class SqlCacheMembership implements GcSqlAssignPrimaryKey, GcDbVersionable {

  public SqlCacheMembership() {
    
  }

  /**
   * version from db
   */
  @GcPersistableField(persist = GcPersist.dontPersist)
  private SqlCacheMembership dbVersion;
  
  /**
   * take a snapshot of the data since this is what is in the db
   */
  @Override
  public void dbVersionReset() {
    //lets get the state from the db so we know what has changed
    this.dbVersion = this.clone();
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
   * db version
   */
  @Override
  public void dbVersionDelete() {
    this.dbVersion = null;
  }

  public void storePrepare() {
    if (this.createdOn == null) {
      this.createdOn = new Timestamp(System.currentTimeMillis());
    }
    
  }

  /**
   * deep clone the fields in this object
   */
  @Override
  public SqlCacheMembership clone() {

    SqlCacheMembership sqlCacheGroup = new SqlCacheMembership();
  
    //dbVersion  DONT CLONE
  
    sqlCacheGroup.createdOn = this.createdOn;
    sqlCacheGroup.flattenedAddTimestamp = this.flattenedAddTimestamp;
    sqlCacheGroup.internalId = this.internalId;
    sqlCacheGroup.memberInternalId = this.memberInternalId;
    sqlCacheGroup.sqlCacheGroupInternalId = this.sqlCacheGroupInternalId;
  
    return sqlCacheGroup;
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
    if (!(obj instanceof GrouperDictionary)) {
      return false;
    }
    SqlCacheMembership other = (SqlCacheMembership) obj;

    return new EqualsBuilder()

      //dbVersion  DONT EQUALS
      .append(this.createdOn, other.createdOn)
      .append(this.flattenedAddTimestamp, other.flattenedAddTimestamp)
      .append(this.internalId, other.internalId)
      .append(this.memberInternalId, other.memberInternalId)
      .append(this.sqlCacheGroupInternalId, other.sqlCacheGroupInternalId)
        .isEquals();

  }

  /**
   * internal integer id
   */
  @GcPersistableField(primaryKey=true, primaryKeyManuallyAssigned=true)
  private long internalId = -1;
  
  /**
   * internal integer id
   * @return
   */
  public long getInternalId() {
    return internalId;
  }

  /**
   * internal integer id
   * @param internalId
   */
  public void setInternalId(long internalId) {
    this.internalId = internalId;
  }

  /**
   * 
   */
  @Override
  public boolean gcSqlAssignNewPrimaryKeyForInsert() {
    if (this.internalId != -1) {
      return false;
    }
    this.internalId = TableIndex.reserveId(TableIndexType.sqlGroupCache);
    return true;
  }

  /**
   * when this member was last added to this group after they werent a member before
   */
  private Timestamp flattenedAddTimestamp;
  
  /**
   * when this member was last added to this group after they werent a member before
   * @return
   */
  public Timestamp getFlattenedAddTimestamp() {
    return flattenedAddTimestamp;
  }

  /**
   * when this member was last added to this group after they werent a member before
   * @param flattenedAddTimestamp
   */
  public void setFlattenedAddTimestamp(Timestamp flattenedAddTimestamp) {
    this.flattenedAddTimestamp = flattenedAddTimestamp;
  }

  /**
   * internal id of the member of this group/list
   */
  private Long memberInternalId;
  
  /**
   * internal id of the member of this group/list
   * @return
   */
  public Long getMemberInternalId() {
    return memberInternalId;
  }

  /**
   * internal id of the member of this group/list
   * @param memberInternalId
   */
  public void setMemberInternalId(Long memberInternalId) {
    this.memberInternalId = memberInternalId;
  }

  /**
   * refers to which group and list this membership refers to
   */
  private Long sqlCacheGroupInternalId;
  
  /**
   * refers to which group and list this membership refers to
   * @return
   */
  public Long getSqlCacheGroupInternalId() {
    return sqlCacheGroupInternalId;
  }

  /**
   * refers to which group and list this membership refers to
   * @param sqlCacheGroupInternalId
   */
  public void setSqlCacheGroupInternalId(Long sqlCacheGroupInternalId) {
    this.sqlCacheGroupInternalId = sqlCacheGroupInternalId;
  }

  /**
   * when this row was created
   */
  private Timestamp createdOn;
  
  /**
   * when this row was created
   * @return
   */
  public Timestamp getCreatedOn() {
    return createdOn;
  }

  /**
   * when this row was created
   * @param createdOn
   */
  public void setCreatedOn(Timestamp createdOn) {
    this.createdOn = createdOn;
  }

  
  /**
   * 
   */
  @Override
  public String toString() {
    return GrouperClientUtils.toStringReflection(this, null);
  }

  /** table name for sql cache */
  public static final String TABLE_GROUPER_SQL_CACHE_MEMBERSHIP = "grouper_sql_cache_mship";
  
  /** created on col in db */
  public static final String COLUMN_CREATED_ON = "created_on";

  /** when this member was last added to this group after they werent a member before */
  public static final String COLUMN_FLATTENED_ADD_TIMESTAMP = "flattened_add_timestamp";

  /** internal id on col in db */
  public static final String COLUMN_INTERNAL_ID = "internal_id";

  /** internal id of the member of this group/list */
  public static final String COLUMN_MEMBER_INTERNAL_ID = "member_internal_id";

  /** refers to which group and list this membership refers to */
  public static final String COLUMN_SQL_CACHE_GROUP_INTERNAL_ID = "sql_cache_group_internal_id";


}
