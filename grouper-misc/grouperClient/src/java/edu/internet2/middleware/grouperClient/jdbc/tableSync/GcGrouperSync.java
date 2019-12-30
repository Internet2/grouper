/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperClient.jdbc.tableSync;

import java.sql.Timestamp;

import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.jdbc.GcPersist;
import edu.internet2.middleware.grouperClient.jdbc.GcPersistableClass;
import edu.internet2.middleware.grouperClient.jdbc.GcPersistableField;
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
  public static GcGrouperSync retrieveByProvisionerName(String theConnectionName, String provisionerName) {
    theConnectionName = GcGrouperSync.defaultConnectionName(theConnectionName);
    GcGrouperSync gcGrouperSync = new GcDbAccess().connectionName(theConnectionName)
        .sql("select * from grouper_sync where provisioner_name = ?").addBindVar(provisionerName).select(GcGrouperSync.class);
    gcGrouperSync.connectionName = theConnectionName;
    return gcGrouperSync;
  }
  
  /**
   * select grouper sync by id
   * @param theConnectionName
   * @param provisionerName
   * @return the sync
   */
  public static GcGrouperSync retrieveById(String theConnectionName, String id) {
    theConnectionName = GcGrouperSync.defaultConnectionName(theConnectionName);
    GcGrouperSync gcGrouperSync = new GcDbAccess().connectionName(theConnectionName)
        .sql("select * from grouper_sync where id = ?").addBindVar(id).select(GcGrouperSync.class);
    gcGrouperSync.connectionName = theConnectionName;
    return gcGrouperSync;
  }
  
  /**
   * select grouper sync job by sync type
   * @param connectionName
   * @param syncType
   * @return the job
   */
  public GcGrouperSyncJob retrieveJobBySyncType(String syncType) {
    return new GcDbAccess().connectionName(this.connectionName)
        .sql("select * from grouper_sync_job where grouper_sync_id = ? and sync_type = ?")
          .addBindVar(this.id).addBindVar(syncType).select(GcGrouperSyncJob.class);
  }
  
  /**
   * select grouper sync grouping by grouping id
   * @param connectionName
   * @param groupingId
   * @return the grouping
   */
  public GcGrouperSyncGrouping retrieveGroupingByGroupingId(String groupingId) {
    return new GcDbAccess().connectionName(this.connectionName)
        .sql("select * from grouper_sync_grouping where grouper_sync_id = ? and grouping_id = ?")
          .addBindVar(this.id).addBindVar(groupingId).select(GcGrouperSyncGrouping.class);
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
    } catch (RuntimeException e) {
      LOG.info("GrouperSync uuid potential mismatch: " + this.provisionerName, e);
      // maybe a different uuid is there
      GcGrouperSync gcGrouperSync = retrieveByProvisionerName(this.connectionName, this.getProvisionerName());
      if (gcGrouperSync != null) {
        this.id = gcGrouperSync.getId();
        new GcDbAccess().connectionName(this.connectionName).storeToDatabase(this);
        LOG.warn("GrouperSync uuid mismatch corrected: " + this.provisionerName);
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
    gcGrouperSync.setGroupingCount(5);
    gcGrouperSync.setUserCount(12);
    gcGrouperSync.setConnectionName("grouper");
    gcGrouperSync.store();
    
    System.out.println("stored");
    
    GcGrouperSync theGcGrouperSync = retrieveByProvisionerName(null, "myJob");
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
        .append("groupingCount", this.groupingCount)
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
   * name of provisioner must be unique in combination with sync_engine.  this is the config key generally
   */
  private String provisionerName;


  /**
   * name of provisioner must be unique in combination with sync_engine.  this is the config key generally
   * @return provisioner name
   */
  public String getProvisionerName() {
    return this.provisionerName;
  }

  /**
   * name of provisioner must be unique in combination with sync_engine.  this is the config key generally
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
   * if grouping this is the number of groups
   */
  private Integer groupingCount;


  /**
   * if grouping this is the number of groups
   * @return grouping count
   */
  public Integer getGroupingCount() {
    return this.groupingCount;
  }

  /**
   * if grouping this is the number of groups
   * @param groupingCount1
   */
  public void setGroupingCount(Integer groupingCount1) {
    this.groupingCount = groupingCount1;
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
