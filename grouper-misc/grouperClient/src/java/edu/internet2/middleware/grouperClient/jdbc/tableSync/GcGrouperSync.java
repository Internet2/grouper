/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperClient.jdbc.tableSync;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.jdbc.GcDbVersionable;
import edu.internet2.middleware.grouperClient.jdbc.GcPersist;
import edu.internet2.middleware.grouperClient.jdbc.GcPersistableClass;
import edu.internet2.middleware.grouperClient.jdbc.GcPersistableField;
import edu.internet2.middleware.grouperClient.jdbc.GcPersistableHelper;
import edu.internet2.middleware.grouperClient.jdbc.GcSqlAssignPrimaryKey;
import edu.internet2.middleware.grouperClient.util.GrouperClientConfig;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.lang3.builder.EqualsBuilder;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.logging.Log;


/**
 * one record for each provisioner.  even if full and incremental, only one record.
 * retrieve all other sync objects from this object, and pass it around, and store all at once at end
 */
@GcPersistableClass(tableName="grouper_sync", defaultFieldPersist=GcPersist.doPersist)
public class GcGrouperSync implements GcSqlAssignPrimaryKey, GcDbVersionable {

  /**
   * keep count of objects created
   */
  @GcPersistableField(persist = GcPersist.dontPersist)
  private int internalObjectsCreatedCount = 0;
  
  /**
   * keep count of objects created
   * @return count
   */
  public int getInternalObjectsCreatedCount() {
    return this.internalObjectsCreatedCount;
  }
  
  /**
   * keep count of objects created
   * @param internalObjectsCreatedCount1
   */
  public void setInternalObjectsCreatedCount(int internalObjectsCreatedCount1) {
    this.internalObjectsCreatedCount = internalObjectsCreatedCount1;
  }

  /**
   * add object count created (query count)
   * @param amountToAdd
   */
  public void addObjectCreatedCount(int amountToAdd) {
    this.internalObjectsCreatedCount += amountToAdd;
  }
  
  /**
   * keep an internal cache of jobs by sync type
   */
  @GcPersistableField(persist = GcPersist.dontPersist)
  private Map<String, GcGrouperSyncJob> internalCacheSyncJobs = new HashMap<String, GcGrouperSyncJob>();
  
  /**
   * keep an internal cache of jobs by uuid
   */
  @GcPersistableField(persist = GcPersist.dontPersist)
  private Map<String, GcGrouperSyncJob> internalCacheSyncJobsById = new HashMap<String, GcGrouperSyncJob>();

  /**
   * batch size for this provisioner
   */
  @GcPersistableField(persist = GcPersist.dontPersist)
  private Integer batchSize;
      
  /**
   * 
   * @return batch size if configured or 1000 be default
   */
  public int batchSize() {
    if (this.batchSize == null) {
      // batch these up
      int defaultBatchSize = GrouperClientConfig.retrieveConfig().propertyValueInt("grouperClient.syncTableDefault.batchSize", 1000);
      this.batchSize = GrouperClientConfig.retrieveConfig().propertyValueInt("grouperClient.syncTable." + this.provisionerName + ".batchSize", defaultBatchSize);
    }
    return batchSize;
  }
  
  /**
   * max bind vars
   */
  @GcPersistableField(persist = GcPersist.dontPersist)
  private Integer maxBindVarsInSelect;

  /**
   * 
   * @return batch size if configured or 1000 be default
   */
  public int maxBindVarsInSelect() {
    if (this.maxBindVarsInSelect == null) {
      // batch these up
      int defaultMaxBindVarsInSelect = GrouperClientConfig.retrieveConfig().propertyValueInt("grouperClient.syncTableDefault.maxBindVarsInSelect", 900);
      this.maxBindVarsInSelect = GrouperClientConfig.retrieveConfig().propertyValueInt("grouperClient.syncTable." + this.provisionerName + ".maxBindVarsInSelect", defaultMaxBindVarsInSelect);
    }
    return maxBindVarsInSelect;
  }

  /**
   * dao for group operations
   */
  @GcPersistableField(persist = GcPersist.dontPersist)
  private GcGrouperSyncGroupDao gcGrouperSyncGroupDao = new GcGrouperSyncGroupDao();

  /**
   * dao for sync
   */
  @GcPersistableField(persist = GcPersist.dontPersist)
  private GcGrouperSyncDao gcGrouperSyncDao = new GcGrouperSyncDao();

  /**
   * dao for groups
   * @return
   */
  public GcGrouperSyncGroupDao getGcGrouperSyncGroupDao() {
    return this.gcGrouperSyncGroupDao;
  }

  /**
   * keep an internal cache of members by member id
   */
  @GcPersistableField(persist = GcPersist.dontPersist)
  private Map<String, GcGrouperSyncMember> internalCacheSyncMembers = new HashMap<String, GcGrouperSyncMember>();
  
  /**
   * keep an internal cache of members by uuid
   */
  @GcPersistableField(persist = GcPersist.dontPersist)
  private Map<String, GcGrouperSyncMember> internalCacheSyncMembersById = new HashMap<String, GcGrouperSyncMember>();
  
  /**
   * keep an internal cache of memberships by sync_group_id and sync_member_id
   */
  @GcPersistableField(persist = GcPersist.dontPersist)
  private Map<MultiKey, GcGrouperSyncMembership> internalCacheSyncMemberships = new HashMap<MultiKey, GcGrouperSyncMembership>();
  
  /**
   * keep an internal cache of memberships by uuid
   */
  @GcPersistableField(persist = GcPersist.dontPersist)
  private Map<MultiKey, GcGrouperSyncMembership> internalCacheSyncMembershipsById = new HashMap<MultiKey, GcGrouperSyncMembership>();
  
  /**
   * keep an internal cache of logs by owner id
   */
  @GcPersistableField(persist = GcPersist.dontPersist)
  private Map<String, GcGrouperSyncLog> internalCacheSyncLogs = new HashMap<String, GcGrouperSyncLog>();
  
  
  
  //########## START GENERATED BY GcDbVersionableGenerate.java ###########
  /** save the state when retrieving from DB */
  @GcPersistableField(persist = GcPersist.dontPersist)
  private GcGrouperSync dbVersion = null;

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

  /**
   * deep clone the fields in this object
   */
  @Override
  public GcGrouperSync clone() {

    GcGrouperSync gcGrouperSync = new GcGrouperSync();
  //connectionName  DONT CLONE

  //dbVersion  DONT CLONE

  gcGrouperSync.groupCount = this.groupCount;
  gcGrouperSync.id = this.id;
  gcGrouperSync.incrementalIndex = this.incrementalIndex;
  gcGrouperSync.incrementalTimestamp = this.incrementalTimestamp;
  gcGrouperSync.lastFullMetadataSyncRun = this.lastFullMetadataSyncRun;
  gcGrouperSync.lastFullMetadataSyncStart = this.lastFullMetadataSyncStart;
  gcGrouperSync.lastFullSyncRun = this.lastFullSyncRun;
  gcGrouperSync.lastFullSyncStart = this.lastFullSyncStart;
  gcGrouperSync.lastIncrementalSyncRun = this.lastIncrementalSyncRun;
  //lastUpdated  DONT CLONE

  gcGrouperSync.provisionerName = this.provisionerName;
  gcGrouperSync.recordsCount = this.recordsCount;
  gcGrouperSync.syncEngine = this.syncEngine;
  gcGrouperSync.userCount = this.userCount;

    return gcGrouperSync;
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
    if (!(obj instanceof GcGrouperSync)) {
      return false;
    }
    GcGrouperSync other = (GcGrouperSync) obj;

    return new EqualsBuilder()


      //connectionName  DONT EQUALS

      //dbVersion  DONT EQUALS

      .append(this.groupCount, other.groupCount)
      .append(this.id, other.id)
      .append(this.incrementalIndex, other.incrementalIndex)
      .append(this.incrementalTimestamp, other.incrementalTimestamp)
      .append(this.lastFullMetadataSyncRun, other.lastFullMetadataSyncRun)
      .append(this.lastFullMetadataSyncStart, other.lastFullMetadataSyncStart)
      .append(this.lastFullSyncRun, other.lastFullSyncRun)
      .append(this.lastFullSyncStart, other.lastFullSyncStart)
      .append(this.lastIncrementalSyncRun, other.lastIncrementalSyncRun)
      //lastUpdated  DONT EQUALS

      .append(this.provisionerName, other.provisionerName)
      .append(this.recordsCount, other.recordsCount)
      .append(this.syncEngine, other.syncEngine)
      .append(this.userCount, other.userCount)
        .isEquals();

  }
  //########## END GENERATED BY GcDbVersionableGenerate.java ###########

  /**
   * delete all data if table is here
   */
  public static void reset() {
    
    try {
      // if its not there forget about it... TODO remove this in 2.5+
      new GcDbAccess().connectionName("grouper").sql("select * from " + GcPersistableHelper.tableName(GcGrouperSync.class) + " where 1 != 1").select(Integer.class);
    } catch (Exception e) {
      return;
    }

    new GcDbAccess().connectionName("grouper").sql("delete from " + GcPersistableHelper.tableName(GcGrouperSync.class)).executeSql();
  }

  /**
   * use this for sql engine sync
   */
  public static final String SQL_SYNC_ENGINE = "sqlTableSync";

  /**
   * use this for provisioning
   */
  public static final String PROVISIONING = "provisioning";

  /**
   * use this for deprovisioning
   */
  public static final String DEPROVISIONING = "deprovisioning";
  
  /**
   * use this to propagate object types from folders to sub folders and groups
   */
  public static final String OBJECT_TYPE_PROPAGATION = "objectType";

  /**
   * use this to propagate attestation from folders to groups
   */
  public static final String ATTESTATION_PROPAGATION = "attestation";

  /**
   * 
   */
  private static Log LOG = GrouperClientUtils.retrieveLog(GcGrouperSync.class);

  /**
   * use 'grouper' if not specified
   * @param connectionName
   * @return the connection name
   */
  public static String defaultConnectionName(String connectionName) {
    return GrouperClientUtils.defaultIfBlank(connectionName, "grouper");
  }
  
  /**
   * when incremental sync ran
   */
  private Timestamp lastIncrementalSyncRun;
  
  
  
  /**
   * when incremental sync ran
   * @return
   */
  public Timestamp getLastIncrementalSyncRun() {
    return this.lastIncrementalSyncRun;
  }

  /**
   * when incremental sync ran
   * @param lastIncrementalSyncRun1
   */
  public void setLastIncrementalSyncRun(Timestamp lastIncrementalSyncRun1) {
    this.lastIncrementalSyncRun = lastIncrementalSyncRun1;
  }

  /**
   * when last full sync started
   */
  private Timestamp lastFullSyncStart;

  /**
   * when last full sync started
   * @return
   */
  public Timestamp getLastFullSyncStart() {
    return lastFullSyncStart;
  }

  /**
   * when last full sync started
   * @param lastFullSyncStart
   */
  public void setLastFullSyncStart(Timestamp lastFullSyncStart) {
    this.lastFullSyncStart = lastFullSyncStart;
  }

  /**
   * when last full sync ran (end)
   */
  private Timestamp lastFullSyncRun;
  
  
  
  /**
   * when last full sync ran (end)
   * @return when
   */
  public Timestamp getLastFullSyncRun() {
    return this.lastFullSyncRun;
  }

  /**
   * when last full sync ran (end)
   * @param lastFullSyncRun1
   */
  public void setLastFullSyncRun(Timestamp lastFullSyncRun1) {
    this.lastFullSyncRun = lastFullSyncRun1;
  }

  /**
   * when last full metadata sync ran.  this needs to run when groups get renamed
   */
  private Timestamp lastFullMetadataSyncRun;
  

  /**
   * when last full metadata sync started.  this needs to run when groups get renamed
   */
  private Timestamp lastFullMetadataSyncStart;
  
  /**
   * when last full metadata sync started.  this needs to run when groups get renamed
   * @return
   */
  public Timestamp getLastFullMetadataSyncStart() {
    return lastFullMetadataSyncStart;
  }

  /**
   * when last full metadata sync started.  this needs to run when groups get renamed
   * @param lastFullMetadataSyncStart
   */
  public void setLastFullMetadataSyncStart(Timestamp lastFullMetadataSyncStart) {
    this.lastFullMetadataSyncStart = lastFullMetadataSyncStart;
  }

  /**
   * when last full metadata sync ran.  this needs to run when groups get renamed
   * @return when
   */
  public Timestamp getLastFullMetadataSyncRun() {
    return this.lastFullMetadataSyncRun;
  }

  /**
   * when last full metadata sync ran.  this needs to run when groups get renamed
   * @param lastFullMetadataSyncRun1
   */
  public void setLastFullMetadataSyncRun(Timestamp lastFullMetadataSyncRun1) {
    this.lastFullMetadataSyncRun = lastFullMetadataSyncRun1;
  }

  /**
   * int of last record processed
   */
  private Long incrementalIndex;
  
  /**
   * when last record processed if timestamp and not integer
   */
  private Timestamp incrementalTimestamp;

  
  
  /**
   * int of last record processed
   * @return number
   */
  public Long getIncrementalIndex() {
    return this.incrementalIndex;
  }

  /**
   * int of last record processed
   * @param incrementalIndexOrMillis1
   */
  public void setIncrementalIndex(Long incrementalIndexOrMillis1) {
    this.incrementalIndex = incrementalIndexOrMillis1;
  }

  /**
   * when last record processed if timestamp and not integer
   * @return timestamp
   */
  public Timestamp getIncrementalTimestamp() {
    return this.incrementalTimestamp;
  }

  /**
   * when last record processed if timestamp and not integer
   * @param incrementalTimestamp1
   */
  public void setIncrementalTimestamp(Timestamp incrementalTimestamp1) {
    this.incrementalTimestamp = incrementalTimestamp1;
  }

  /**
   * connection name or null for default
   */
  @GcPersistableField(persist=GcPersist.dontPersist)
  private String connectionName;

  /**
   * connection name or null for default
   * @return connection name
   */
  public String getConnectionName() {
    return this.connectionName;
  }

  /**
   * connection name or null for default
   * @param connectionName1
   */
  public void setConnectionName(String connectionName1) {
    this.connectionName = connectionName1;
  }


  public void storePrepare() {
    this.lastUpdated = new Timestamp(System.currentTimeMillis());
    this.connectionName = GcGrouperSync.defaultConnectionName(this.connectionName);
  }
  
  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    
    System.out.println("none");
    
    // should get none
    for (GcGrouperSync theGcGrouperSync : new GcDbAccess().connectionName("grouper").selectList(GcGrouperSync.class)) {
      System.out.println(theGcGrouperSync.toString());
    }

    GcGrouperSync gcGrouperSync = new GcGrouperSync();
    gcGrouperSync.setSyncEngine("temp");
    gcGrouperSync.setProvisionerName("myJob");
    gcGrouperSync.setLastUpdated(new Timestamp(System.currentTimeMillis()));
    gcGrouperSync.setRecordsCount(10);
    gcGrouperSync.setGroupCount(5);
    gcGrouperSync.setUserCount(12);
    gcGrouperSync.setConnectionName("grouper");
    gcGrouperSync.getGcGrouperSyncDao().store();
    
    System.out.println("stored");
    
    GcGrouperSync theGcGrouperSync = GcGrouperSyncDao.retrieveByProvisionerName(null, "myJob");
    System.out.println(theGcGrouperSync.toString());

    gcGrouperSync.setRecordsCount(12);
    gcGrouperSync.getGcGrouperSyncDao().store();

    System.out.println("updated");

    for (GcGrouperSync theGcGrouperSync2 : new GcDbAccess().connectionName("grouper").selectList(GcGrouperSync.class)) {
      System.out.println(theGcGrouperSync2.toString());
    }

    gcGrouperSync.getGcGrouperSyncDao().delete();

    System.out.println("deleted");

    for (GcGrouperSync theGcGrouperSync2 : new GcDbAccess().connectionName("grouper").selectList(GcGrouperSync.class)) {
      System.out.println(theGcGrouperSync2.toString());
    }
    
    System.out.println("retrieveOrCreate");
    gcGrouperSync = GcGrouperSyncDao.retrieveOrCreateByProvisionerName(null, "myJob");
    System.out.println(gcGrouperSync.toString());

    System.out.println("retrieve");
    theGcGrouperSync = GcGrouperSyncDao.retrieveByProvisionerName(null, "myJob");
    System.out.println(gcGrouperSync.toString());

    System.out.println("retrieveOrCreate");
    gcGrouperSync = GcGrouperSyncDao.retrieveOrCreateByProvisionerName(null, "myJob");
    System.out.println(gcGrouperSync.toString());
    
    System.out.println("deleted");
    gcGrouperSync.getGcGrouperSyncDao().delete();
    for (GcGrouperSync theGcGrouperSync2 : new GcDbAccess().connectionName("grouper").selectList(GcGrouperSync.class)) {
      System.out.println(theGcGrouperSync2.toString());
    }
    
    int queryCount = GcDbAccess.threadLocalQueryCountRetrieve();
    System.out.println("Query count orig: " + queryCount);
    
    gcGrouperSync = GcGrouperSyncDao.retrieveOrCreateByProvisionerName(null, "myJob");
    System.out.println(gcGrouperSync.toString());
    
    queryCount = GcDbAccess.threadLocalQueryCountRetrieve();
    System.out.println("Query count after insert: " + queryCount);
    
    gcGrouperSync.setLastIncrementalSyncRun(new Timestamp(System.currentTimeMillis()));
    gcGrouperSync.getGcGrouperSyncDao().store();

    queryCount = GcDbAccess.threadLocalQueryCountRetrieve();
    System.out.println("Query count after update with changes: " + queryCount);

    gcGrouperSync = GcGrouperSyncDao.retrieveOrCreateByProvisionerName(null, "myJob");
    System.out.println(gcGrouperSync.toString());
    
    queryCount = GcDbAccess.threadLocalQueryCountRetrieve();
    System.out.println("Query count before update without changes: " + queryCount);

    gcGrouperSync.getGcGrouperSyncDao().store();

    queryCount = GcDbAccess.threadLocalQueryCountRetrieve();
    System.out.println("Query count after update with no changes: " + queryCount);

  }

  /**
   * 
   */
  @Override
  public String toString() {
    return GrouperClientUtils.toStringReflection(this, toStringFieldNamesToIgnore);
  }

  private static Set<String> toStringFieldNamesToIgnore = GrouperClientUtils.toSet("batchSize", "connectionName", "dbVersion", "gcGrouperSyncDao",
      "gcGrouperSyncGroupDao", "gcGrouperSyncJobDao", "gcGrouperSyncLogDao", "gcGrouperSyncMemberDao", "gcGrouperSyncMembershipDao",
      "groupCount", "internalCacheSyncJobs", "internalCacheSyncJobsById", "internalCacheSyncLogs", "internalCacheSyncMembers",
      "internalCacheSyncMembersById",  "internalCacheSyncMemberships",  "internalCacheSyncMembershipsById",  "internalObjectsCreatedCount",  
      "lastFullMetadataSyncRun",  "lastFullSyncRun",  "lastUpdated",  "maxBindVarsInSelect");
  
  /**
   * 
   */
  public GcGrouperSync() {
    this.gcGrouperSyncDao.setGcGrouperSync(this);
    this.gcGrouperSyncGroupDao.setGcGrouperSync(this);
    this.gcGrouperSyncLogDao.setGcGrouperSync(this);
    this.gcGrouperSyncJobDao.setGcGrouperSync(this);
    this.gcGrouperSyncMemberDao.setGcGrouperSync(this);
    this.gcGrouperSyncMembershipDao.setGcGrouperSync(this);
  }

  /**
   * uuid of this record in this table
   */
  @GcPersistableField(primaryKey=true, primaryKeyManuallyAssigned=false)
  private String id;

  
  /**
   * uuid of this record in this table
   * @return the id
   */
  public String getId() {
    return this.id;
  }

  
  /**
   * uuid of this record in this table
   * @param id1 the id to set
   */
  public void setId(String id1) {
    this.id = id1;
  }

  /**
   * e.g. for syncing sql, it sqlTableSync
   */
  private String syncEngine;

  /**
   * e.g. for syncing sql, it sqlTableSync
   * @return sync engine
   */
  public String getSyncEngine() {
    return this.syncEngine;
  }

  /**
   * e.g. for syncing sql, it sqlTableSync
   * @param syncEngine1
   */
  public void setSyncEngine(String syncEngine1) {
    this.syncEngine = syncEngine1;
  }

  /**
   * name of provisioner must be unique.  this is the config key generally
   */
  private String provisionerName;


  /**
   * name of provisioner must be unique.  this is the config key generally
   * @return provisioner name
   */
  public String getProvisionerName() {
    return this.provisionerName;
  }

  /**
   * name of provisioner must be unique.  this is the config key generally
   * @param provisionerName1
   */
  public void setProvisionerName(String provisionerName1) {
    this.provisionerName = provisionerName1;
  }

  /**
   * when this record was last updated
   */
  private Timestamp lastUpdated;


  /**
   * when this record was last updated
   * @return when last updated
   */
  public Timestamp getLastUpdated() {
    return this.lastUpdated;
  }

  /**
   * when this record was last updated
   * @param lastUpdated1
   */
  public void setLastUpdated(Timestamp lastUpdated1) {
    this.lastUpdated = lastUpdated1;
  }

  /**
   * if group this is the number of groups
   */
  private Integer groupCount;


  /**
   * if group this is the number of groups
   * @return group count
   */
  public Integer getGroupCount() {
    return this.groupCount;
  }

  /**
   * if group this is the number of groups
   * @param groupCount1
   */
  public void setGroupCount(Integer groupCount1) {
    this.groupCount = groupCount1;
  }

  /**
   * if has users, this is the number of users
   */
  private Integer userCount;


  /**
   * if has users, this is the number of users
   * @return user count
   */
  public Integer getUserCount() {
    return this.userCount;
  }

  /**
   * if has users, this is the number of users
   * @param userCount1
   */
  public void setUserCount(Integer userCount1) {
    this.userCount = userCount1;
  }
  
  /**
   * number of records including users, groups, etc
   */
  private Integer recordsCount;

  /**
   * dao for log operations
   */
  @GcPersistableField(persist = GcPersist.dontPersist)
  private GcGrouperSyncLogDao gcGrouperSyncLogDao = new GcGrouperSyncLogDao();

  /**
   * dao for job operations
   */
  @GcPersistableField(persist = GcPersist.dontPersist)
  private GcGrouperSyncJobDao gcGrouperSyncJobDao = new GcGrouperSyncJobDao();

  /**
   * dao for member operations
   */
  @GcPersistableField(persist = GcPersist.dontPersist)
  private GcGrouperSyncMemberDao gcGrouperSyncMemberDao = new GcGrouperSyncMemberDao();

  /**
   * dao for membership operations
   */
  @GcPersistableField(persist = GcPersist.dontPersist)
  private GcGrouperSyncMembershipDao gcGrouperSyncMembershipDao = new GcGrouperSyncMembershipDao();


  /**
   * dao for log operations
   * @return
   */
  public GcGrouperSyncLogDao getGcGrouperSyncLogDao() {
    return this.gcGrouperSyncLogDao;
  }

  /**
   * number of records including users, groups, etc
   * @return number of records
   */
  public Integer getRecordsCount() {
    return this.recordsCount;
  }

  /**
   * number of records including users, groups, etc
   * @param recordsCount1
   */
  public void setRecordsCount(Integer recordsCount1) {
    this.recordsCount = recordsCount1;
  }

  /**
   * 
   */
  @Override
  public boolean gcSqlAssignNewPrimaryKeyForInsert() {
    if (this.id != null) {
      return false;
    }
    this.id = GrouperClientUtils.uuid();
    return true;
  }


  /**
   * dao for jobs
   * @return
   */
  public GcGrouperSyncJobDao getGcGrouperSyncJobDao() {
    return this.gcGrouperSyncJobDao;
  }
  
  /**
   * dao for members
   * @return
   */
  public GcGrouperSyncMemberDao getGcGrouperSyncMemberDao() {
    return this.gcGrouperSyncMemberDao;
  }
  
  /**
   * dao for memberships
   * @return
   */
  public GcGrouperSyncMembershipDao getGcGrouperSyncMembershipDao() {
    return this.gcGrouperSyncMembershipDao;
  }


  /**
   * dao for syncs
   * @return
   */
  public GcGrouperSyncDao getGcGrouperSyncDao() {
    return this.gcGrouperSyncDao;
  }
  
}
