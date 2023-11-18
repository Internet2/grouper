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

@GcPersistableClass(tableName="grouper_sql_cache_mship_hst", defaultFieldPersist=GcPersist.doPersist)
public class SqlCacheMembershipHst implements GcSqlAssignPrimaryKey, GcDbVersionable {

  public SqlCacheMembershipHst() {
    
  }

  /**
   * version from db
   */
  @GcPersistableField(persist = GcPersist.dontPersist)
  private SqlCacheMembershipHst dbVersion;
  
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
  }

  /**
   * deep clone the fields in this object
   */
  @Override
  public SqlCacheMembershipHst clone() {

    SqlCacheMembershipHst sqlCacheGroup = new SqlCacheMembershipHst();

    sqlCacheGroup.endTime = this.endTime;
    sqlCacheGroup.startTime = this.startTime;
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
    SqlCacheMembershipHst other = (SqlCacheMembershipHst) obj;

    return new EqualsBuilder()

      //dbVersion  DONT EQUALS
      .append(this.endTime, other.endTime)
      .append(this.startTime, other.startTime)
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
   * when this flattened membership started
   */
  private Timestamp startTime;
  
  /**
   * when this flattened membership started
   * @return
   */
  public Timestamp getStartTime() {
    return startTime;
  }

  /**
   * when this flattened membership started
   * @param startTime
   */
  public void setStartTime(Timestamp startTime) {
    this.startTime = startTime;
  }

  /**
   * when this flattened membership ended
   */
  private Timestamp endTime;

  
  
  /**
   * when this flattened membership ended
   * @return
   */
  public Timestamp getEndTime() {
    return endTime;
  }

  /**
   * when this flattened membership ended
   * @param endTime
   */
  public void setEndTime(Timestamp endTime) {
    this.endTime = endTime;
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
   * 
   */
  @Override
  public String toString() {
    return GrouperClientUtils.toStringReflection(this, null);
  }

  /** table name for sql cache */
  public static final String TABLE_GROUPER_SQL_CACHE_MEMBERSHIP_HST = "grouper_sql_cache_mship_hst";
  
  /** when this membership ended col in db */
  public static final String COLUMN_END_TIME = "end_time";

  /** when this membership started col in db */
  public static final String COLUMN_START_TIME = "start_time";

  /** internal id on col in db */
  public static final String COLUMN_INTERNAL_ID = "internal_id";

  /** internal id of the member of this group/list */
  public static final String COLUMN_MEMBER_INTERNAL_ID = "member_internal_id";

  /** refers to which group and list this membership refers to */
  public static final String COLUMN_SQL_CACHE_GROUP_INTERNAL_ID = "sql_cache_group_internal_id";


}
