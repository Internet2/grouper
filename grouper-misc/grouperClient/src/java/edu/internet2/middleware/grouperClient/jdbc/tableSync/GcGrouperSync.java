/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperClient.jdbc.tableSync;

import java.sql.Timestamp;
import java.util.List;
import java.util.Random;

import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.jdbc.GcPersist;
import edu.internet2.middleware.grouperClient.jdbc.GcPersistableClass;
import edu.internet2.middleware.grouperClient.jdbc.GcPersistableField;
import edu.internet2.middleware.grouperClient.jdbc.GcPersistableHelper;
import edu.internet2.middleware.grouperClient.jdbc.GcSqlAssignPrimaryKey;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.lang3.builder.ToStringBuilder;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.logging.Log;


/**
 * one record for each provisioner.  even if full and incremental, only one record
 */
@GcPersistableClass(tableName="grouper_sync", defaultFieldPersist=GcPersist.doPersist)
public class GcGrouperSync implements GcSqlAssignPrimaryKey {

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
   * select grouper sync by provisioner name
   * @param theConnectionName
   * @param provisionerName
   * @return the sync
   */
  public static GcGrouperSync retrieveByProvisionerName(String theConnectionName, String syncEngine, String provisionerName) {
    theConnectionName = GcGrouperSync.defaultConnectionName(theConnectionName);
    GcGrouperSync gcGrouperSync = new GcDbAccess().connectionName(theConnectionName)
        .sql("select * from grouper_sync where sync_engine = ? and provisioner_name = ?")
         .addBindVar(syncEngine).addBindVar(provisionerName).select(GcGrouperSync.class);
    if (gcGrouperSync != null) {
      gcGrouperSync.connectionName = theConnectionName;
    }
    return gcGrouperSync;
  }
  
  /**
   * wait for related jobs to finish running, then run
   * @param provisionerName
   * @param goToPendingFirstAkaLargeJob is if this is a  big job and needs to register as pending first so 
   * it knows it should run now.  falso if quick job and doesnt matter
   */
  public GcGrouperSyncJob waitForRelatedJobsToFinishThenRun(String syncType, boolean goToPendingFirstAkaLargeJob) {
    
    List<GcGrouperSyncJob> allGcGrouperSyncJobs = this.retrieveAllJobs();

    GcGrouperSyncJob gcGrouperSyncJob = GcGrouperSyncJob.retrieveJobBySyncType(allGcGrouperSyncJobs, syncType);
    
    // if doesnt exist, 
    if (gcGrouperSyncJob == null) {
      gcGrouperSyncJob = new GcGrouperSyncJob();
      gcGrouperSyncJob.setGrouperSync(this);
      gcGrouperSyncJob.setSyncType(syncType);
      allGcGrouperSyncJobs.add(gcGrouperSyncJob);
    }
    
    //go to pending first?
    if (goToPendingFirstAkaLargeJob) {
      gcGrouperSyncJob.setJobState(GcGrouperSyncJobState.pending);
      gcGrouperSyncJob.setHeartbeat(new Timestamp(System.currentTimeMillis()));
      gcGrouperSyncJob.store();
      
      // sleep between half and full second
      GrouperClientUtils.sleep(500 + new Random().nextInt(500));
      
      allGcGrouperSyncJobs = this.retrieveAllJobs();
      gcGrouperSyncJob = GcGrouperSyncJob.retrieveJobBySyncType(allGcGrouperSyncJobs, syncType);
      
    }
  
    // back this off so we arent overchecking
    int sleepSeconds = 15;
    long started = System.currentTimeMillis();
    
    while(true) {

      // see how many other jobs running
      int runningOrPendingCount = 0;
      for (GcGrouperSyncJob currentGrouperSyncJob : GrouperClientUtils.nonNull(allGcGrouperSyncJobs)) {
        if (GrouperClientUtils.equals(currentGrouperSyncJob.getSyncType(), syncType)) {
          continue;
        }
        
        //if the heartbeat is bad dontw worry about it
        if (currentGrouperSyncJob.getHeartbeat() == null || System.currentTimeMillis() - currentGrouperSyncJob.getHeartbeat().getTime() > 90000) {
          // dont worry about it
          continue;
        }
        
        if (GcGrouperSyncJobState.running == currentGrouperSyncJob.getJobState()) {
          runningOrPendingCount++;
          continue;
        }
        
        // dont run if we are a small job
        if (GcGrouperSyncJobState.pending == currentGrouperSyncJob.getJobState() && !goToPendingFirstAkaLargeJob) {
          runningOrPendingCount++;
          continue;
        }
      }
      
      //if nothing there or only one there, we done
      if (runningOrPendingCount == 0) {
        gcGrouperSyncJob.setJobState(GcGrouperSyncJobState.running);
        gcGrouperSyncJob.setHeartbeat(new Timestamp(System.currentTimeMillis()));
        gcGrouperSyncJob.store();
        return gcGrouperSyncJob;
      }

      // lets go to pending and set the heartbeat
      if (sleepSeconds > 60) {
        gcGrouperSyncJob.setJobState(GcGrouperSyncJobState.notRunning);
      }
      gcGrouperSyncJob.setHeartbeat(new Timestamp(System.currentTimeMillis()));
      gcGrouperSyncJob.store();
      
      // sleep a little at first then ramp it up
      GrouperClientUtils.sleep((sleepSeconds*1000) + new Random().nextInt(5000));

      if (sleepSeconds > 60 || goToPendingFirstAkaLargeJob) {
        gcGrouperSyncJob.setJobState(GcGrouperSyncJobState.pending);
        
      }
      gcGrouperSyncJob.setHeartbeat(new Timestamp(System.currentTimeMillis()));
      gcGrouperSyncJob.store();

      if (goToPendingFirstAkaLargeJob) {
        
        // sleep between half and full second
        GrouperClientUtils.sleep(500 + new Random().nextInt(500));
        
      }
      if (sleepSeconds < 120) {
        sleepSeconds *= 2;
      }
      if (sleepSeconds > 120) {
        sleepSeconds = 120;
      }
      
      allGcGrouperSyncJobs = this.retrieveAllJobs();
      gcGrouperSyncJob = GcGrouperSyncJob.retrieveJobBySyncType(allGcGrouperSyncJobs, syncType);

      if (System.currentTimeMillis() - started > 1000 * 60 * 60 * 24) {
        throw new RuntimeException("Job cannot start for a day! " + this.connectionName + ", " + this.syncEngine + ", " + this.provisionerName + ", " + syncType);
      }
    }    
  }
  
  /**
   * wait for related jobs to finish running, then run
   */
  public List<GcGrouperSyncJob> retrieveAllJobs() {
    
    List<GcGrouperSyncJob> gcGrouperSyncJobs = new GcDbAccess().connectionName(this.connectionName)
        .sql("select * from grouper_sync_job where grouper_sync_id = ?").addBindVar(this.id).selectList(GcGrouperSyncJob.class);
    for (GcGrouperSyncJob gcGrouperSyncJob : GrouperClientUtils.nonNull(gcGrouperSyncJobs)) {
      gcGrouperSyncJob.setConnectionName(this.connectionName);
    }
    return gcGrouperSyncJobs;
  }
  
  
  /**
   * retrieve a sync provisioner or create
   * @param theConnectionName
   * @param syncEngine
   * @param provisionerName
   * @return the sync
   */
  public static GcGrouperSync retrieveOrCreateByProvisionerName(String theConnectionName, String syncEngine, String provisionerName) {
    GcGrouperSync gcGrouperSync = retrieveByProvisionerName(theConnectionName, syncEngine, provisionerName);
    if (gcGrouperSync == null) {
      try {
        gcGrouperSync = new GcGrouperSync();
        gcGrouperSync.setConnectionName(theConnectionName);
        gcGrouperSync.setProvisionerName(provisionerName);
        gcGrouperSync.setSyncEngine(syncEngine);
        gcGrouperSync.store();
      } catch (RuntimeException re) {
        // maybe someone else just created it...
        GrouperClientUtils.sleep(2000);
        gcGrouperSync = retrieveByProvisionerName(theConnectionName, syncEngine, provisionerName);
        if (gcGrouperSync == null) {
          throw re;
        }
      }
    }
    return gcGrouperSync;
  }
  
  /**
   * register this job as pending
   * @param provisionerName
   * @param syncType
   */
  public void listProvisionerJobAsPending(String syncType) {
    // get or create provisioner
    
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
   * when last full sync ran
   */
  private Timestamp lastFullSyncRun;
  
  
  
  /**
   * when last full sync ran
   * @return when
   */
  public Timestamp getLastFullSyncRun() {
    return this.lastFullSyncRun;
  }

  /**
   * when last full sync ran
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
   * select grouper sync by id
   * @param theConnectionName
   * @param id
   * @return the sync
   */
  public static GcGrouperSync retrieveById(String theConnectionName, String id) {
    theConnectionName = GcGrouperSync.defaultConnectionName(theConnectionName);
    GcGrouperSync gcGrouperSync = new GcDbAccess().connectionName(theConnectionName)
        .sql("select * from grouper_sync where id = ?").addBindVar(id).select(GcGrouperSync.class);
    if (gcGrouperSync != null) {
      gcGrouperSync.connectionName = theConnectionName;
    }
    return gcGrouperSync;
  }
  
  /**
   * select grouper sync job by sync type
   * @param connectionName
   * @param syncType
   * @return the job
   */
  public GcGrouperSyncJob retrieveJobBySyncType(String syncType) {
    GcGrouperSyncJob gcGrouperSyncJob = new GcDbAccess().connectionName(this.connectionName)
        .sql("select * from grouper_sync_job where grouper_sync_id = ? and sync_type = ?")
          .addBindVar(this.id).addBindVar(syncType).select(GcGrouperSyncJob.class);
    if (gcGrouperSyncJob != null) {
      gcGrouperSyncJob.setGrouperSync(this);
    }
    return gcGrouperSyncJob;
  }
  
  /**
   * retrieve a sync provisioner or create
   * @param theConnectionName
   * @param grouperSyncId
   * @param syncType
   * @return the sync job
   */
  public GcGrouperSyncJob retrieveJobOrCreateBySyncType(String syncType) {
    GcGrouperSyncJob gcGrouperSyncJob = retrieveJobBySyncType(syncType);
    if (gcGrouperSyncJob == null) {
      try {
        gcGrouperSyncJob = new GcGrouperSyncJob();
        gcGrouperSyncJob.setGrouperSync(this);
        gcGrouperSyncJob.setSyncType(syncType);
        gcGrouperSyncJob.store();
      } catch (RuntimeException re) {
        // maybe someone else just created it...
        GrouperClientUtils.sleep(2000);
        gcGrouperSyncJob = retrieveJobBySyncType(syncType);
        if (gcGrouperSyncJob == null) {
          throw re;
        }
      }
    }
    return gcGrouperSyncJob;
  }


  /**
   * select grouper sync group by group id
   * @param connectionName
   * @param groupId
   * @return the group
   */
  public GcGrouperSyncGroup retrieveGroupByGroupId(String groupId) {
    GcGrouperSyncGroup gcGrouperSyncGroup = new GcDbAccess().connectionName(this.connectionName)
        .sql("select * from grouper_sync_group where grouper_sync_id = ? and group_id = ?")
          .addBindVar(this.id).addBindVar(groupId).select(GcGrouperSyncGroup.class);
    if (gcGrouperSyncGroup != null) {
      gcGrouperSyncGroup.setGrouperSync(this);
      gcGrouperSyncGroup.setConnectionName(this.connectionName);
    }
    return gcGrouperSyncGroup;
  }
  
  /**
   * select grouper sync user by user id
   * @param connectionName
   * @param mcemberId
   * @return the user
   */
  public GcGrouperSyncMember retrieveMemberByMemberId(String memberId) {
    GcGrouperSyncMember gcGrouperSyncUser = new GcDbAccess().connectionName(this.connectionName)
        .sql("select * from grouper_sync_member where grouper_sync_id = ? and member_id = ?")
          .addBindVar(this.id).addBindVar(memberId).select(GcGrouperSyncMember.class);
    if (gcGrouperSyncUser != null) {
      gcGrouperSyncUser.setGrouperSync(this);
      gcGrouperSyncUser.setConnectionName(this.connectionName);
    }
    return gcGrouperSyncUser;
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

  /**
   * 
   * @param connectionName
   */
  public void store() {
    try {
      this.lastUpdated = new Timestamp(System.currentTimeMillis());
      this.connectionName = GcGrouperSync.defaultConnectionName(this.connectionName);
      new GcDbAccess().connectionName(this.connectionName).storeToDatabase(this);
    } catch (RuntimeException re) {
      LOG.info("GrouperSync uuid potential mismatch: " + this.provisionerName, re);
      // maybe a different uuid is there
      GcGrouperSync gcGrouperSync = retrieveByProvisionerName(this.connectionName, this.syncEngine, this.getProvisionerName());
      if (gcGrouperSync != null) {
        this.id = gcGrouperSync.getId();
        new GcDbAccess().connectionName(this.connectionName).storeToDatabase(this);
        LOG.warn("GrouperSync uuid mismatch corrected: " + this.provisionerName);
      } else {
        throw re;
      }
    }
  }
  
  /**
   * 
   * @param connectionName
   */
  public void delete() {
    this.connectionName = GcGrouperSync.defaultConnectionName(this.connectionName);
    new GcDbAccess().connectionName(this.connectionName).deleteFromDatabase(this);
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
    gcGrouperSync.setId(GrouperClientUtils.uuid());
    gcGrouperSync.setSyncEngine("temp");
    gcGrouperSync.setProvisionerName("myJob");
    gcGrouperSync.setLastUpdated(new Timestamp(System.currentTimeMillis()));
    gcGrouperSync.setRecordsCount(10);
    gcGrouperSync.setGroupCount(5);
    gcGrouperSync.setUserCount(12);
    gcGrouperSync.setConnectionName("grouper");
    gcGrouperSync.store();
    
    System.out.println("stored");
    
    GcGrouperSync theGcGrouperSync = retrieveByProvisionerName(null, "temp", "myJob");
    System.out.println(theGcGrouperSync.toString());

    gcGrouperSync.setRecordsCount(12);
    gcGrouperSync.store();

    System.out.println("updated");

    for (GcGrouperSync theGcGrouperSync2 : new GcDbAccess().connectionName("grouper").selectList(GcGrouperSync.class)) {
      System.out.println(theGcGrouperSync2.toString());
    }

    gcGrouperSync.delete();

    System.out.println("deleted");

    for (GcGrouperSync theGcGrouperSync2 : new GcDbAccess().connectionName("grouper").selectList(GcGrouperSync.class)) {
      System.out.println(theGcGrouperSync2.toString());
    }
    
    System.out.println("retrieveOrCreate");
    gcGrouperSync = retrieveOrCreateByProvisionerName(null, "temp", "myJob");
    System.out.println(gcGrouperSync.toString());

    System.out.println("retrieve");
    theGcGrouperSync = retrieveByProvisionerName(null, "temp", "myJob");
    System.out.println(gcGrouperSync.toString());

    System.out.println("retrieveOrCreate");
    gcGrouperSync = retrieveOrCreateByProvisionerName(null, "temp", "myJob");
    System.out.println(gcGrouperSync.toString());
    
    System.out.println("deleted");
    gcGrouperSync.delete();
    for (GcGrouperSync theGcGrouperSync2 : new GcDbAccess().connectionName("grouper").selectList(GcGrouperSync.class)) {
      System.out.println(theGcGrouperSync2.toString());
    }
  }

  /**
   * 
   */
  @Override
  public String toString() {
    return new ToStringBuilder(this)
        .append("id", this.id)
        .append("syncEngine", this.syncEngine)
        .append("provisionerName", this.provisionerName)
        .append("lastUpdated", this.lastUpdated)
        .append("recordsCount", this.recordsCount)
        .append("groupCount", this.groupCount)
        .append("incrementalIndexOrMillis", this.incrementalIndex)
        .append("incrementalTimestamp", this.incrementalTimestamp)
        .append("userCount", this.userCount).build();
  }



  /**
   * 
   */
  public GcGrouperSync() {
  }

  /**
   * uuid of this record in this table
   */
  @GcPersistableField(primaryKey=true, primaryKeyManuallyAssigned=true)
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
  public void gcSqlAssignNewPrimaryKeyForInsert() {
    this.id = GrouperClientUtils.uuid();
  }
  
}
