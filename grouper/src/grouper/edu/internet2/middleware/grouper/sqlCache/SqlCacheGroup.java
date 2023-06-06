package edu.internet2.middleware.grouper.sqlCache;

import java.sql.Timestamp;

import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.dictionary.GrouperDictionary;
import edu.internet2.middleware.grouper.tableIndex.TableIndex;
import edu.internet2.middleware.grouper.tableIndex.TableIndexType;
import edu.internet2.middleware.grouperClient.jdbc.GcDbVersionable;
import edu.internet2.middleware.grouperClient.jdbc.GcPersist;
import edu.internet2.middleware.grouperClient.jdbc.GcPersistableClass;
import edu.internet2.middleware.grouperClient.jdbc.GcPersistableField;
import edu.internet2.middleware.grouperClient.jdbc.GcSqlAssignPrimaryKey;
import edu.internet2.middleware.grouperClient.util.ExpirableCache;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.lang3.builder.EqualsBuilder;

@GcPersistableClass(tableName="grouper_sql_cache_group", defaultFieldPersist=GcPersist.doPersist)
public class SqlCacheGroup implements GcSqlAssignPrimaryKey, GcDbVersionable {

  public SqlCacheGroup() {
    
  }

  /**
   * extension of folder
   */
  public static String attributeDefFolderExtension = "sqlCacheable";
 
  private static String attributeDefFolderName = null;
  
  public static String attributeDefFolderName() {
    
    if (attributeDefFolderName == null) {
      attributeDefFolderName = GrouperConfig.retrieveConfig().propertyValueString("grouper.rootStemForBuiltinObjects") + ":" + SqlCacheGroup.attributeDefFolderExtension;
    }
    
    return attributeDefFolderName;
  }
  
  
  
  /**
   * extension of folder
   */
  public static String attributeDefNameExtensionListName = "sqlCacheableListName";

  private static String attributeDefNameNameListName = null;

  public static String attributeDefNameNameListName() {
    
    if (attributeDefNameNameListName == null) {
      attributeDefNameNameListName = attributeDefFolderName() + ":" + SqlCacheGroup.attributeDefNameExtensionListName;
    }
    
    return attributeDefNameNameListName;
  }

  /**
   * marker extension of attribute def
   */
  public static String attributeDefMarkerExtension = "sqlCacheableGroupMarkerDef";

  private static String attributeDefMarkerName = null;

  public static String attributeDefMarkerName() {
    
    if (attributeDefMarkerName == null) {
      attributeDefMarkerName = attributeDefFolderName() + ":" + SqlCacheGroup.attributeDefMarkerExtension;
    }
    
    return attributeDefMarkerName;
  }

  /**
   * extension of attribute def
   */
  public static String attributeDefExtension = "sqlCacheableGroupDef";

  private static String attributeDefName = null;

  public static String attributeDefName() {
    
    if (attributeDefName == null) {
      attributeDefName = attributeDefFolderName() + ":" + SqlCacheGroup.attributeDefExtension;
    }
    
    return attributeDefName;
  }

  /**
   * extension of marker attribute
   */
  public static String attributeDefNameMarkerExtension = "sqlCacheableGroup";

  private static String attributeDefNameMarkerName = null;

  public static String attributeDefNameMarkerName() {
    
    if (attributeDefNameMarkerName == null) {
      attributeDefNameMarkerName = attributeDefFolderName() + ":" + SqlCacheGroup.attributeDefNameMarkerExtension;
    }
    
    return attributeDefNameMarkerName;
  }

  /**
   * version from db
   */
  @GcPersistableField(persist = GcPersist.dontPersist)
  private SqlCacheGroup dbVersion;
  
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
  public SqlCacheGroup clone() {

    SqlCacheGroup sqlCacheGroup = new SqlCacheGroup();
  
    //dbVersion  DONT CLONE
  
    sqlCacheGroup.createdOn = this.createdOn;
    sqlCacheGroup.disabledOn = this.disabledOn;
    sqlCacheGroup.fieldInternalId = this.fieldInternalId;
    sqlCacheGroup.groupInternalId = this.groupInternalId;
    sqlCacheGroup.internalId = this.internalId;
    sqlCacheGroup.membershipSize = this.membershipSize;
    sqlCacheGroup.membershipSizeHst = this.membershipSizeHst;
  
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
    SqlCacheGroup other = (SqlCacheGroup) obj;

    return new EqualsBuilder()


      //dbVersion  DONT EQUALS
      .append(this.createdOn, other.createdOn)
      .append(this.disabledOn, other.disabledOn)
      .append(this.enabledOn, other.enabledOn)
      .append(this.fieldInternalId, other.fieldInternalId)
      .append(this.groupInternalId, other.groupInternalId)
      .append(this.internalId, other.internalId)
      .append(this.membershipSize, other.membershipSize)
      .append(this.membershipSizeHst, other.membershipSizeHst)
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
   * refers to the group being cached
   */
  private long groupInternalId = -1;


  /**
   * refers to the group being cached
   * @return
   */
  public long getGroupInternalId() {
    return groupInternalId;
  }

  /**
   * refers to the group being cached
   * @param groupInternalId
   */
  public void setGroupInternalId(long groupInternalId) {
    this.groupInternalId = groupInternalId;
  }

  /**
   * refers to the field of the group being cached
   */
  private long fieldInternalId = -1;

  /**
   * refers to the field of the group being cached
   * @return
   */
  public long getFieldInternalId() {
    return fieldInternalId;
  }

  /**
   * refers to the field of the group being cached
   * @param fieldInternalId
   */
  public void setFieldInternalId(long fieldInternalId) {
    this.fieldInternalId = fieldInternalId;
  }

  /**
   * number of members approximately for this list.  Note:
   * two incrementals at the same time could skew it
   */
  private long membershipSize = -1;

  /**
   * number of members approximately for this list.  Note:
   * two incrementals at the same time could skew it
   * @return
   */
  public long getMembershipSize() {
    return membershipSize;
  }

  /**
   * number of members approximately for this list.  Note:
   * two incrementals at the same time could skew it
   * @param membershipSize
   */
  public void setMembershipSize(long membershipSize) {
    this.membershipSize = membershipSize;
  }

  /**
   * number of records approximately for this group in the grouper hst memberships table
   * Note: two increments at the same time could skew it
   */
  private long membershipSizeHst = -1;


  /**
   * number of records approximately for this group in the grouper hst memberships table
   * Note: two increments at the same time could skew it
   * @return
   */
  public long getMembershipSizeHst() {
    return membershipSizeHst;
  }

  /**
   * number of records approximately for this group in the grouper hst memberships table
   * Note: two increments at the same time could skew it
   * @param membershipSizeHst
   */
  public void setMembershipSizeHst(long membershipSizeHst) {
    this.membershipSizeHst = membershipSizeHst;
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
   * when this row is ready to use by consumers (once the memberships are loaded)
   */
  private Timestamp enabledOn;

  /**
   * when this row is ready to use by consumers (once the memberships are loaded)
   * @return
   */
  public Timestamp getEnabledOn() {
    return enabledOn;
  }

  /**
   * when this row is ready to use by consumers (once the memberships are loaded)
   * @param enabledOn
   */
  public void setEnabledOn(Timestamp enabledOn) {
    this.enabledOn = enabledOn;
  }


  /**
   * when this shouldnt be used any more by consumers (before deletion)
   */
  private Timestamp disabledOn;
  
  /**
   * when this shouldnt be used any more by consumers (before deletion)
   * @return
   */
  public Timestamp getDisabledOn() {
    return disabledOn;
  }

  /**
   * when this shouldnt be used any more by consumers (before deletion)
   * @param disabledOn
   */
  public void setDisabledOn(Timestamp disabledOn) {
    this.disabledOn = disabledOn;
  }

  /**
   * 
   */
  @Override
  public String toString() {
    return GrouperClientUtils.toStringReflection(this, null);
  }

  /** table name for sql cache */
  public static final String TABLE_GROUPER_SQL_CACHE_GROUP = "grouper_sql_cache_group";
  
  /** created on col in db */
  public static final String COLUMN_CREATED_ON = "created_on";

  /** disabled on col in db */
  public static final String COLUMN_DISABLED_ON = "disabled_on";

  /** enabled on col in db */
  public static final String COLUMN_ENABLED_ON = "enabled_on";

  /** field internal id on col in db */
  public static final String COLUMN_FIELD_INTERNAL_ID = "field_internal_id";

  /** group internal id on col in db */
  public static final String COLUMN_GROUP_INTERNAL_ID = "group_internal_id";

  /** internal id on col in db */
  public static final String COLUMN_INTERNAL_ID = "internal_id";

  /** membership size on col in db */
  public static final String COLUMN_MEMBERSHIP_SIZE = "membership_size";

  /** membership size on col in db */
  public static final String COLUMN_MEMBERSHIP_SIZE_HST = "membership_size_hst";

  
}
