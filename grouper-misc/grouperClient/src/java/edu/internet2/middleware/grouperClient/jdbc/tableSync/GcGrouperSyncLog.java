package edu.internet2.middleware.grouperClient.jdbc.tableSync;

import java.net.InetAddress;
import java.sql.Timestamp;

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
 * last log for this sync that affected this group/member/membership/job
 * @author mchyzer
 *
 */
@GcPersistableClass(tableName="grouper_sync_log", defaultFieldPersist=GcPersist.doPersist)
public class GcGrouperSyncLog implements GcSqlAssignPrimaryKey {

  /**
   * delete all data if table is here
   */
  public static void reset() {
    
    try {
      // if its not there forget about it... TODO remove this in 2.5+
      new GcDbAccess().connectionName("grouper").sql("select * from " + GcPersistableHelper.tableName(GcGrouperSyncLog.class) + " where 1 != 1").select(Integer.class);
    } catch (Exception e) {
      return;
    }

    new GcDbAccess().connectionName("grouper").sql("delete from " + GcPersistableHelper.tableName(GcGrouperSyncLog.class)).executeSql();
  }

  /**
   * select grouper sync log by id
   * @param theConnectionName
   * @param id
   * @return the log
   */
  public static GcGrouperSyncLog retrieveById(String theConnectionName, String id) {
    theConnectionName = GcGrouperSync.defaultConnectionName(theConnectionName);
    GcGrouperSyncLog gcGrouperSyncLog = new GcDbAccess().connectionName(theConnectionName)
        .sql("select * from grouper_sync_log where id = ?").addBindVar(id).select(GcGrouperSyncLog.class);
    if (gcGrouperSyncLog != null) {
      gcGrouperSyncLog.connectionName = theConnectionName;
    }
    return gcGrouperSyncLog;
  }

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    
    System.out.println("none");
    
    for (GcGrouperSyncLog theGcGrouperSyncLog : new GcDbAccess().connectionName("grouper").selectList(GcGrouperSyncLog.class)) {
      System.out.println(theGcGrouperSyncLog.toString());
    }
    
    // foreign key
    GcGrouperSync gcGrouperSync = new GcGrouperSync();
    gcGrouperSync.setSyncEngine("temp");
    gcGrouperSync.setProvisionerName("myJob");
    gcGrouperSync.store();
    
    GcGrouperSyncJob gcGrouperSyncJob = new GcGrouperSyncJob();
    gcGrouperSyncJob.setGrouperSync(gcGrouperSync);
    gcGrouperSyncJob.setJobState(GcGrouperSyncJobState.notRunning);
    gcGrouperSyncJob.setLastSyncIndex(135L);
    gcGrouperSyncJob.setLastTimeWorkWasDone(new Timestamp(System.currentTimeMillis() + 2000));
    gcGrouperSyncJob.setSyncType("testSyncType");
    gcGrouperSyncJob.store();

    GcGrouperSyncGroup gcGrouperSyncGroup = new GcGrouperSyncGroup();
    gcGrouperSyncGroup.setGrouperSync(gcGrouperSync);
    gcGrouperSyncGroup.setLastTimeWorkWasDone(new Timestamp(System.currentTimeMillis() + 2000));
    gcGrouperSyncGroup.setGroupId("myId");
    gcGrouperSyncGroup.store();

    GcGrouperSyncLog gcGrouperSyncLog = new GcGrouperSyncLog();
    gcGrouperSyncLog.description = "desc";
    gcGrouperSyncLog.setGrouperSyncJob(gcGrouperSyncJob);
    gcGrouperSyncLog.setGrouperSyncGroup(gcGrouperSyncGroup);
    gcGrouperSyncLog.jobTookMillis = 1223;
    gcGrouperSyncLog.recordsChanged = 12;
    gcGrouperSyncLog.recordsProcessed = 23;
    gcGrouperSyncLog.store();

    System.out.println("stored");

    gcGrouperSyncLog = retrieveByJobAndOwner("grouper", gcGrouperSyncJob.getId(), gcGrouperSyncGroup.getId());
    System.out.println(gcGrouperSyncLog);
    
    gcGrouperSyncLog.setDescription("desc2");
    gcGrouperSyncLog.store();

    System.out.println("updated");

    for (GcGrouperSyncLog theGcGrouperSyncLog : new GcDbAccess().connectionName("grouper").selectList(GcGrouperSyncLog.class)) {
      System.out.println(theGcGrouperSyncLog.toString());
    }

    gcGrouperSyncLog.delete();
    gcGrouperSyncGroup.delete();
    gcGrouperSyncJob.delete();
    gcGrouperSync.delete();
    
    System.out.println("deleted");

    for (GcGrouperSyncGroup theGcGrouperSyncStatus : new GcDbAccess().connectionName("grouper").selectList(GcGrouperSyncGroup.class)) {
      System.out.println(theGcGrouperSyncStatus.toString());
    }
  }

  /**
   * 
   */
  public GcGrouperSyncLog() {
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
   * uuid of this record in this table
   */
  @GcPersistableField(primaryKey=true, primaryKeyManuallyAssigned=true)
  private String id;

  
  /**
   * uuid of this record in this table
   * @return id
   */
  public String getId() {
    return this.id;
  }

  /**
   * uuid of this record in this table
   * @param id1
   */
  public void setId(String id1) {
    this.id = id1;
  }
  
  /**
   * foreign key to grouper_sync_group table
   */
  private String grouperSyncGroupId;


  /**
   * foreign key to grouper_sync_group table
   * @return group id
   */
  public String getGrouperSyncGroupId() {
    return this.grouperSyncGroupId;
  }

  /**
   * foreign key to grouper_sync_group table
   * @param grouperSyncGroupId1
   */
  public void setGrouperSyncGroupId(String grouperSyncGroupId1) {
    this.grouperSyncGroupId = grouperSyncGroupId1;
    if (this.grouperSyncGroup == null || !GrouperClientUtils.equals(grouperSyncGroupId1, this.grouperSyncGroup.getId())) {
      this.grouperSyncGroup = null;
    }
    if (this.grouperSyncGroupId != null) {
      this.grouperSyncOwnerId = this.grouperSyncGroupId;
    }
  }
  
  /**
   * foreign key to grouper_sync_member table
   */
  private String grouperSyncMemberId;


  /**
   * foreign key to grouper_sync_member table
   * @return foreign key
   */
  public String getGrouperSyncMemberId() {
    return this.grouperSyncMemberId;
  }

  /**
   * foreign key to grouper_sync_member table
   * @param grouperSyncMemberId1
   */
  public void setGrouperSyncMemberId(String grouperSyncMemberId1) {
    this.grouperSyncMemberId = grouperSyncMemberId1;
    if (this.grouperSyncMember == null || !GrouperClientUtils.equals(grouperSyncMemberId1, this.grouperSyncMember.getId())) {
      this.grouperSyncMember = null;
    }
    if (this.grouperSyncMemberId != null) {
      this.grouperSyncOwnerId = this.grouperSyncMemberId;
    }

  }

  /**
   * either the grouper_sync_member_id or the grouper_sync_group_id or grouper_sync_job_id (if log for job wide)
   */
  private String grouperSyncOwnerId;


  /**
   * either the grouper_sync_member_id or the grouper_sync_group_id or grouper_sync_job_id (if log for job wide)
   * @return owner id
   */
  public String getGrouperSyncOwnerId() {
    return this.grouperSyncOwnerId;
  }

  /**
   * either the grouper_sync_member_id or the grouper_sync_group_id or grouper_sync_job_id (if log for job wide)
   * @param grouperSyncOwnerId1
   */
  public void setGrouperSyncOwnerId(String grouperSyncOwnerId1) {
    this.grouperSyncOwnerId = grouperSyncOwnerId1;
  }
  
  /**
   * foreign key to grouper_sync_job table
   */
  private String grouperSyncJobId;


  /**
   * foreign key to grouper_sync_job table
   * @return foreign key
   */
  public String getGrouperSyncJobId() {
    return this.grouperSyncJobId;
  }

  /**
   * foreign key to grouper_sync_job table
   * @param grouperSyncJobId1
   */
  public void setGrouperSyncJobId(String grouperSyncJobId1) {
    this.grouperSyncJobId = grouperSyncJobId1;
    if (this.grouperSyncJob == null || !GrouperClientUtils.equals(grouperSyncJobId1, this.grouperSyncJob.getId())) {
      this.grouperSyncJob = null;
    }
    if (this.grouperSyncMemberId != null && this.grouperSyncOwnerId == null) {
      this.grouperSyncOwnerId = this.grouperSyncMemberId;
    }

  }

  /**
   * SUCCESS, ERROR, WARNING, CONFIG_ERROR
   */
  @GcPersistableField(columnName="status")
  private String statusDb;

  /**
   * SUCCESS, ERROR, WARNING, CONFIG_ERROR
   * @return status
   */
  public String getStatusDb() {
    return this.statusDb;
  }

  /**
   * SUCCESS, ERROR, WARNING, CONFIG_ERROR
   * @param status1
   */
  public void setStatusDb(String status1) {
    this.statusDb = status1;
  }
  
  /**
   * 
   * @return the state or null if not there
   */
  public GcGrouperSyncLogState getStatus() {
    return GcGrouperSyncLogState.valueOfIgnoreCase(this.statusDb);
  }
  
  /**
   * 
   * @param gcGrouperSyncLogState
   */
  public void setStatus(GcGrouperSyncLogState gcGrouperSyncLogState) {
    this.statusDb = gcGrouperSyncLogState == null ? null : gcGrouperSyncLogState.name();
  }

  /**
   * when the last sync started
   */
  private Timestamp syncTimestamp;


  /**
   * when the last sync started
   * @return timestamp
   */
  public Timestamp getSyncTimestamp() {
    return this.syncTimestamp;
  }

  /**
   * when the last sync started
   * @param syncTimestamp1
   */
  public void setSyncTimestamp(Timestamp syncTimestamp1) {
    this.syncTimestamp = syncTimestamp1;
  }

  /**
   * description of last sync
   */
  private String description;


  /**
   * description of last sync
   * @return description
   */
  public String getDescription() {
    return this.description;
  }

  /**
   * description of last sync
   * @param description1
   */
  public void setDescription(String description1) {
    this.description = description1;
  }
  
  /**
   * how many records were processed the last time this sync ran
   */
  private Integer recordsProcessed;


  /**
   * how many records were processed the last time this sync ran
   * @return records processed
   */
  public Integer getRecordsProcessed() {
    return this.recordsProcessed;
  }

  /**
   * how many records were processed the last time this sync ran
   * @param recordsProcessed1
   */
  public void setRecordsProcessed(Integer recordsProcessed1) {
    this.recordsProcessed = recordsProcessed1;
  }
  
  /**
   * how many records were changed the last time this sync ran
   */
  private Integer recordsChanged;


  /**
   * how many records were changed the last time this sync ran
   * @return records changed
   */
  public Integer getRecordsChanged() {
    return this.recordsChanged;
  }

  /**
   * how many records were changed the last time this sync ran
   * @param recordsChanged1
   */
  public void setRecordsChanged(Integer recordsChanged1) {
    this.recordsChanged = recordsChanged1;
  }
  
  /**
   * how many millis it took to run this job
   */
  private Integer jobTookMillis;


  /**
   * how many millis it took to run this job
   * @return job millis
   */
  public Integer getJobTookMillis() {
    return this.jobTookMillis;
  }

  /**
   * how many millis it took to run this job
   * @param jobTookMillis
   */
  public void setJobTookMillis(Integer jobTookMillis) {
    this.jobTookMillis = jobTookMillis;
  }
  
  /**
   * which server this occurred on
   */
  private String server;
  
  
  
  /**
   * foreign key to grouper_sync_membership table
   */
  private String grouperSyncMembershipId;

  /**
   * foreign key to grouper_sync_membership table
   * @return foreign key
   */
  public String getGrouperSyncMembershipId() {
    return this.grouperSyncMembershipId;
  }

  /**
   * foreign key to grouper_sync_membership table
   * @param grouperSyncMembershipId1
   */
  public void setGrouperSyncMembershipId(String grouperSyncMembershipId1) {
    this.grouperSyncMembershipId = grouperSyncMembershipId1;
    if (this.grouperSyncMembership == null || !GrouperClientUtils.equals(grouperSyncMembershipId1, this.grouperSyncMembership.getId())) {
      this.grouperSyncMembership = null;
    }
    if (this.grouperSyncMembershipId != null) {
      this.grouperSyncOwnerId = this.grouperSyncMembershipId;
    }

  }

  /**
   * which server this occurred on
   * @return server
   */
  public String getServer() {
    return this.server;
  }

  /**
   * which server this occurred on
   * @param server1
   */
  public void setServer(String server1) {
    this.server = server1;
  }

  /**
   * connection name or null for default
   */
  @GcPersistableField(persist=GcPersist.dontPersist)
  private String connectionName;

  /**
   * link back to sync group
   */
  @GcPersistableField(persist=GcPersist.dontPersist)
  private GcGrouperSyncGroup grouperSyncGroup = null;

  /**
   * link back to sync member
   */
  @GcPersistableField(persist=GcPersist.dontPersist)
  private GcGrouperSyncMember grouperSyncMember = null;



  /**
   * link back to sync membership
   */
  @GcPersistableField(persist=GcPersist.dontPersist)
  private GcGrouperSyncMembership grouperSyncMembership = null;



  /**
   * link back to sync job
   */
  @GcPersistableField(persist=GcPersist.dontPersist)
  private GcGrouperSyncJob grouperSyncJob = null;
  
  /**
   * 
   */
  private static Log LOG = GrouperClientUtils.retrieveLog(GcGrouperSyncLog.class);

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
   */
  @Override
  public void gcSqlAssignNewPrimaryKeyForInsert() {
    this.id = GrouperClientUtils.uuid();
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
   * @param connectionName
   */
  public void store() {
    try {
      if (GrouperClientUtils.isBlank(this.server)) {
        try {
          this.server = InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
          //dont worry about it
          LOG.info(e);
        }
      }
      this.lastUpdated = new Timestamp(System.currentTimeMillis());
      this.connectionName = GcGrouperSync.defaultConnectionName(this.connectionName);
      //abbrev this to below 4k in case of special chars
      this.description = GrouperClientUtils.abbreviate(this.description, 3700);
      new GcDbAccess().connectionName(this.connectionName).storeToDatabase(this);
    } catch (RuntimeException re) {

      LOG.info("GcGrouperSyncLog uuid potential mismatch: " + this.grouperSyncJobId + ", " + this.grouperSyncOwnerId, re);

      // maybe a different uuid is there
      GcGrouperSyncLog gcGrouperSyncLog = retrieveByJobAndOwner(this.connectionName, this.grouperSyncJobId, this.grouperSyncOwnerId);
      if (gcGrouperSyncLog != null) {
        this.id = gcGrouperSyncLog.getId();
        new GcDbAccess().connectionName(connectionName).storeToDatabase(this);
        LOG.warn("GcGrouperSyncLog uuid mismatch corrected: " + this.grouperSyncJobId + ", " + this.grouperSyncOwnerId);
      } else {
        throw re;
      }
    }
  }

  /**
   * 
   */
  @Override
  public String toString() {
    return new ToStringBuilder(this)
        .append("id", this.id)
        .append("connectionName", this.connectionName)
        .append("grouperSyncMemberId", this.grouperSyncMemberId)
        .append("grouperSyncGroupId", this.grouperSyncGroupId)
        .append("grouperSyncJobId", this.grouperSyncJobId)
        .append("grouperSyncMembershipId", this.grouperSyncMembershipId)
        .append("grouperSyncOwnerId", this.grouperSyncOwnerId)
        .append("jobTookMillis", this.jobTookMillis)
        .append("recordsChanged", this.recordsChanged)
        .append("recordsProcessed", this.recordsProcessed)
        .append("server", this.server)
        .append("lastUpdated", this.lastUpdated)
        .append("status", this.statusDb)
        .append("description", this.description).build();
  }

  /**
   * link back to sync group
   * @return group
   */
  public GcGrouperSyncGroup getGrouperSyncGroup() {
    return this.grouperSyncGroup;
  }

  /**
   * link back to sync member
   * @return member
   */
  public GcGrouperSyncMember getGrouperSyncMember() {
    return this.grouperSyncMember;
  }

  /**
   * 
   * @return sync
   */
  public GcGrouperSyncGroup retrieveGrouperSyncGroup() {
    if (this.grouperSyncGroup == null && this.grouperSyncGroupId != null) {
      this.grouperSyncGroup = GcGrouperSyncGroup.retrieveById(this.connectionName, this.grouperSyncGroupId);
    }
    return this.grouperSyncGroup;
  }

  /**
   * 
   * @return sync
   */
  public GcGrouperSyncMember retrieveGrouperSyncMember() {
    if (this.grouperSyncMember == null && this.grouperSyncMemberId != null) {
      this.grouperSyncMember = GcGrouperSyncMember.retrieveById(this.connectionName, this.grouperSyncMemberId);
    }
    return this.grouperSyncMember;
  }

  /**
   * link back to sync group
   * @param gcGrouperSyncGroup
   */
  public void setGrouperSyncGroup(GcGrouperSyncGroup gcGrouperSyncGroup) {
    this.grouperSyncGroup = gcGrouperSyncGroup;
    this.setGrouperSyncGroupId(gcGrouperSyncGroup == null ? null : gcGrouperSyncGroup.getId());
  }

  /**
   * link back to sync member
   * @param gcGrouperSyncMember1
   */
  public void setGrouperSyncMember(GcGrouperSyncMember gcGrouperSyncMember1) {
    
    this.grouperSyncMember = gcGrouperSyncMember1;
    this.setGrouperSyncMemberId(gcGrouperSyncMember1 == null ? null : gcGrouperSyncMember1.getId());
  }

  /**
   * link back to sync membership
   * @return membership
   */
  public GcGrouperSyncMembership getGrouperSyncMembership() {
    return this.grouperSyncMembership;
  }

  /**
   * 
   * @return sync
   */
  public GcGrouperSyncMembership retrieveGrouperSyncMembership() {
    if (this.grouperSyncMembership == null && this.grouperSyncMembershipId != null) {
      this.grouperSyncMembership = GcGrouperSyncMembership.retrieveById(this.connectionName, this.grouperSyncMembershipId);
    }
    return this.grouperSyncMembership;
  }

  /**
   * link back to sync membership
   * @param gcGrouperSyncMembership1
   */
  public void setGrouperSyncMembership(GcGrouperSyncMembership gcGrouperSyncMembership1) {
    
    this.grouperSyncMembership = gcGrouperSyncMembership1;
    this.setGrouperSyncMembershipId(gcGrouperSyncMembership1 == null ? null : gcGrouperSyncMembership1.getId());
  }

  /**
   * link back to sync job
   * @return job
   */
  public GcGrouperSyncJob getGrouperSyncJob() {
    return this.grouperSyncJob;
  }

  /**
   * 
   * @return sync
   */
  public GcGrouperSyncJob retrieveGrouperSyncJob() {
    if (this.grouperSyncJob == null && this.grouperSyncJobId != null) {
      this.grouperSyncJob = GcGrouperSyncJob.retrieveById(this.connectionName, this.grouperSyncJobId);
    }
    return this.grouperSyncJob;
  }

  /**
   * link back to sync job
   * @param gcGrouperSyncJob1
   */
  public void setGrouperSyncJob(GcGrouperSyncJob gcGrouperSyncJob1) {
    
    this.grouperSyncJob = gcGrouperSyncJob1;
    this.setGrouperSyncJobId(gcGrouperSyncJob1 == null ? null : gcGrouperSyncJob1.getId());
  }

  /**
   * select log by job and owner
   * @param theConnectionName
   * @param grouperSyncGroupId
   * @param grouperSyncMemberId
   * @return the sync
   */
  public static GcGrouperSyncLog retrieveByJobAndOwner(String theConnectionName, String grouperSyncJobId, String grouperSyncOwnerId) {
    theConnectionName = GcGrouperSync.defaultConnectionName(theConnectionName);
    GcGrouperSyncLog gcGrouperSyncLog = new GcDbAccess().connectionName(theConnectionName)
        .sql("select * from grouper_sync_log where grouper_sync_job_id = ? and grouper_sync_owner_id = ?").addBindVar(grouperSyncJobId)
          .addBindVar(grouperSyncOwnerId).select(GcGrouperSyncLog.class);
    if (gcGrouperSyncLog != null) {
      gcGrouperSyncLog.connectionName = theConnectionName;
    }
    return gcGrouperSyncLog;
  }

}
