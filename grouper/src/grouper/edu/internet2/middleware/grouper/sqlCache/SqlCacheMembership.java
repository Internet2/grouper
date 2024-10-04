package edu.internet2.middleware.grouper.sqlCache;

import java.sql.Timestamp;

import edu.internet2.middleware.grouper.dictionary.GrouperDictionary;
import edu.internet2.middleware.grouperClient.jdbc.GcDbVersionable;
import edu.internet2.middleware.grouperClient.jdbc.GcPersist;
import edu.internet2.middleware.grouperClient.jdbc.GcPersistableClass;
import edu.internet2.middleware.grouperClient.jdbc.GcPersistableField;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;

@GcPersistableClass(tableName="grouper_sql_cache_mship", defaultFieldPersist=GcPersist.doPersist)
public class SqlCacheMembership implements GcDbVersionable {

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

  }

  /**
   * deep clone the fields in this object
   */
  @Override
  public SqlCacheMembership clone() {

    SqlCacheMembership sqlCacheGroup = new SqlCacheMembership();
  
    //dbVersion  DONT CLONE
  
    sqlCacheGroup.flattenedAddTimestamp = this.flattenedAddTimestamp;
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
      .append(this.flattenedAddTimestamp, other.flattenedAddTimestamp)
      .append(this.memberInternalId, other.memberInternalId)
      .append(this.sqlCacheGroupInternalId, other.sqlCacheGroupInternalId)
        .isEquals();

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
  @GcPersistableField(compoundPrimaryKey=true, primaryKeyManuallyAssigned=true)
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
  @GcPersistableField(compoundPrimaryKey=true, primaryKeyManuallyAssigned=true)
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
  

  public SqlCacheMembership getDbVersion() {
    return this.dbVersion;
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

  /** when this member was last added to this group after they werent a member before */
  public static final String COLUMN_FLATTENED_ADD_TIMESTAMP = "flattened_add_timestamp";

  /** internal id of the member of this group/list */
  public static final String COLUMN_MEMBER_INTERNAL_ID = "member_internal_id";

  /** refers to which group and list this membership refers to */
  public static final String COLUMN_SQL_CACHE_GROUP_INTERNAL_ID = "sql_cache_group_internal_id";


}
