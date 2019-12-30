/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperClient.jdbc.tableSync;

import java.sql.Timestamp;

import edu.internet2.middleware.grouperClient.jdbc.GcPersist;
import edu.internet2.middleware.grouperClient.jdbc.GcPersistableClass;
import edu.internet2.middleware.grouperClient.jdbc.GcPersistableField;
import edu.internet2.middleware.grouperClient.jdbc.GcSqlAssignPrimaryKey;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;


/**
 * if doing user level syncs, this is the metadata
 */
@GcPersistableClass(tableName="grouper_sync_user", defaultFieldPersist=GcPersist.doPersist)
public class GcGrouperSyncUser implements GcSqlAssignPrimaryKey {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    
  }
  
  /**
   * 
   */
  public GcGrouperSyncUser() {
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
   * if this grouping exists in the target/destination
   */
  private String inTargetDb;
  
  /**
   * if this grouping exists in the target/destination
   * @return if in target
   */
  public String getInTargetDb() {
    return this.inTargetDb;
  }

  /**
   * if this grouping exists in the target/destination
   * @param inTargetDb1
   */
  public void setInTargetDb(String inTargetDb1) {
    this.inTargetDb = inTargetDb1;
  }

  /**
   * if this grouping exists in the target/destination
   * @return if is target
   */
  public Boolean getInTarget() {
    return GrouperClientUtils.booleanObjectValue(this.inTargetDb);
  }
  
  /**
   * if this grouping exists in the target/destination
   * @param inTarget
   */
  public void setInTarget(Boolean inTarget) {
    this.inTargetDb = inTarget ? "T" : "F";
  }
  
  /**
   * uuid of the job in grouper_sync
   */
  private String grouperSyncId;
  
  /**
   * uuid of the job in grouper_sync
   * @return uuid of the job in grouper_sync
   */ 
  public String getGrouperSyncId() {
    return this.grouperSyncId;
  }

  /**
   * uuid of the job in grouper_sync
   * @param grouperSyncId1
   */
  public void setGrouperSyncId(String grouperSyncId1) {
    this.grouperSyncId = grouperSyncId1;
  }

  /**
   * the last time the grouping job was run this is the server it was run on
   */
  private String lastGroupingServer;

  /**
   * the last time the grouping job was run this is the server it was run on
   * @return last time grouping job run
   */
  public String getLastGroupingServer() {
    return this.lastGroupingServer;
  }

  /**
   * the last time the grouping job was run this is the server it was run on
   * @param lastGroupingServer1
   */
  public void setLastGroupingServer(String lastGroupingServer1) {
    this.lastGroupingServer = lastGroupingServer1;
  }

  
  /**
   * the last time the incremental job was run this is the server it was run on
   */
  private String lastIncrementalServer;

  /**
   * the last time the incremental job was run this is the server it was run on
   * @return last incremental
   */
  public String getLastIncrementalServer() {
    return this.lastIncrementalServer;
  }

  /**
   * the last time the incremental job was run this is the server it was run on
   * @param lastIncrementalServer1
   */
  public void setLastIncrementalServer(String lastIncrementalServer1) {
    this.lastIncrementalServer = lastIncrementalServer1;
  }

  /**
   * records changed during last run
   */
  private Long lastGroupingRecords;
  
  
  /**
   * records changed during last run
   * @return the lastRecordsChangeCount
   */
  public Long getLastGroupingRecords() {
    return this.lastGroupingRecords;
  }

  
  /**
   * records changed during last run
   * @param lastRecordsChangeCount1 the lastRecordsChangeCount to set
   */
  public void setLastGroupingRecords(Long lastRecordsChangeCount1) {
    this.lastGroupingRecords = lastRecordsChangeCount1;
  }

  /**
   * description of last work done
   */
  private String lastGroupingDescription;
  
  /**
   * description of last grouping work done
   * @return the lastDescription
   */
  public String getLastGroupingDescription() {
    return this.lastGroupingDescription;
  }

  
  /**
   * description of last grouping work done
   * @param lastDescription1 the lastDescription to set
   */
  public void setLastGroupingDescription(String lastDescription1) {
    this.lastGroupingDescription = lastDescription1;
  }

  /**
   * when this record was last updated
   */
  private Timestamp lastUpdated;
  
  /**
   * when this record was last updated
   * @return the lastUpdated
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
   * for groups this is the group uuid
   */
  private String groupingId;
  
  /**
   * for groups this is the group uuid
   * @return group id
   */
  public String getGroupingId() {
    return this.groupingId;
  }

  /**
   * for groups this is the group uuid
   * @param groupingId1
   */
  public void setGroupingId(String groupingId1) {
    this.groupingId = groupingId1;
  }

  /**
   * for groups this is the group system name
   */
  private String groupingName;
  
  /**
   * for groups this is the group system name
   * @return grouping name
   */
  public String getGroupingName() {
    return this.groupingName;
  }

  /**
   * for groups this is the group system name
   * @param groupingName1
   */
  public void setGroupingName(String groupingName1) {
    this.groupingName = groupingName1;
  }

  /**
   * SUCCESS, ERROR, WARNING, CONFIG_ERROR
   */
  private String lastGroupingStatus;

  /**
   * SUCCESS, ERROR, WARNING, CONFIG_ERROR
   * @return status
   */
  public String getLastGroupingStatus() {
    return this.lastGroupingStatus;
  }

  /**
   * SUCCESS, ERROR, WARNING, CONFIG_ERROR
   * @param status1
   */
  public void setLastGroupingStatus(String status1) {
    this.lastGroupingStatus = status1;
  }

  /**
   * SUCCESS, ERROR, WARNING, CONFIG_ERROR
   */
  private String lastIncrementalStatus;
  
  /**
   * SUCCESS, ERROR, WARNING, CONFIG_ERROR
   * @return last incremental status
   */
  public String getLastIncrementalStatus() {
    return lastIncrementalStatus;
  }

  /**
   * SUCCESS, ERROR, WARNING, CONFIG_ERROR
   * @param lastIncrementalStatus1
   */
  public void setLastIncrementalStatus(String lastIncrementalStatus1) {
    this.lastIncrementalStatus = lastIncrementalStatus1;
  }

  /**
   * how long the grouping sync took
   */
  private Long lastGroupingTookMillis;

  /**
   * how long the grouping sync took
   * @return last grouping
   */
  public Long getLastGroupingTookMillis() {
    return this.lastGroupingTookMillis;
  }

  /**
   * how long the grouping sync took
   * @param lastGroupingTookMillis1
   */
  public void setLastGroupingTookMillis(Long lastGroupingTookMillis1) {
    this.lastGroupingTookMillis = lastGroupingTookMillis1;
  }

  /**
   * T if provisionable and F is not
   */
  private String provisionableDb;
  
  /**
   * T if provisionable and F is not
   * @return if provisionable
   */
  public String getProvisionableDb() {
    return this.provisionableDb;
  }

  /**
   * T if provisionable and F is not
   * @param provisionableDb1
   */
  public void setProvisionableDb(String provisionableDb1) {
    this.provisionableDb = provisionableDb1;
  }

  /**
   * if provisionable
   * @return if provisionable
   */
  public boolean isProvisionable() {
    return GrouperClientUtils.booleanValue(this.provisionableDb, false);
  }

  /**
   * if provisionable
   * @param provisionable
   */
  public void setProvisionable(boolean provisionable) {
    this.provisionableDb = provisionable ? "T" : "F";
  }
  
  /**
   * millis since 1970 that this grouping started to be provisionable
   */
  private Long provisionableStartMillis;
    
  /**
   * millis since 1970 that this grouping started to be provisionable
   * @return millis
   */
  public Long getProvisionableStartMillis() {
    return this.provisionableStartMillis;
  }

  /**
   * millis since 1970 that this grouping started to be provisionable
   * @param provisionableStartMillis1
   */
  public void setProvisionableStartMillis(Long provisionableStartMillis1) {
    this.provisionableStartMillis = provisionableStartMillis1;
  }

  /**
   * millis since 1970 that this grouping ended being provisionable
   */
  private Long provisionableEndMillis;

  /**
   * millis since 1970 that this grouping ended being provisionable
   * @return millis
   */
  public Long getProvisionableEndMillis() {
    return this.provisionableEndMillis;
  }

  /**
   * millis since 1970 that this grouping ended being provisionable
   * @param provisionableEndMillis1
   */
  public void setProvisionableEndMillis(Long provisionableEndMillis1) {
    this.provisionableEndMillis = provisionableEndMillis1;
  }

  /**
   * number of records in this grouping
   */
  private Long totalCount;

  /**
   * number of records in this grouping
   * @return total count
   */
  public Long getTotalCount() {
    return this.totalCount;
  }

  /**
   * number of records in this grouping
   * @param totalCount1
   */
  public void setTotalCount(Long totalCount1) {
    this.totalCount = totalCount1;
  }

  /**
   * for groups this is the group idIndex
   */
  private String groupingFromId2;

  /**
   * for groups this is the group idIndex
   * @return grouping from id 2
   */
  public String getGroupingFromId2() {
    return this.groupingFromId2;
  }

  /**
   * for groups this is the group idIndex
   * @param groupingFromId2_1
   */
  public void setGroupingFromId2(String groupingFromId2_1) {
    this.groupingFromId2 = groupingFromId2_1;
  }

  /**
   * other metadata on groups
   */
  private String groupingFromId3;

  /**
   * other metadata on groups
   * @return id3
   */
  public String getGroupingFromId3() {
    return this.groupingFromId3;
  }

  /**
   * other metadata on groups
   * @param groupingFromId3_1
   */
  public void setGroupingFromId3(String groupingFromId3_1) {
    this.groupingFromId3 = groupingFromId3_1;
  }

  /**
   * other metadata on groups
   */
  private String groupingToId2;
  
  /**
   * other metadata on groups
   * @return metadata
   */
  public String getGroupingToId2() {
    return this.groupingToId2;
  }

  /**
   * other metadata on groups
   * @param groupingToId2_1
   */
  public void setGroupingToId2(String groupingToId2_1) {
    this.groupingToId2 = groupingToId2_1;
  }

  /**
   * other metadata on groups
   */
  private String groupingToId3;

  /**
   * other metadata on groups
   * @return grouping id
   */
  public String getGroupingToId3() {
    return this.groupingToId3;
  }

  /**
   * other metadata on groups
   * @param groupingToId3_1
   */
  public void setGroupingToId3(String groupingToId3_1) {
    this.groupingToId3 = groupingToId3_1;
  }

  /**
   * when this grouping was last synced
   */
  private Timestamp lastGroupingSync;
  
  /**
   * when this grouping was last synced
   * @return last sync
   */
  public Timestamp getLastGroupingSync() {
    return this.lastGroupingSync;
  }

  /**
   * when this grouping was last synced
   * @param lastGroupingSync1
   */
  public void setLastGroupingSync(Timestamp lastGroupingSync1) {
    this.lastGroupingSync = lastGroupingSync1;
  }

  /**
   * when this grouping was last incremental synced
   */
  private Timestamp lastIncrementalSync;

  /**
   * when this grouping was last incremental synced
   * @return last sync
   */
  public Timestamp getLastIncrementalSync() {
    return this.lastIncrementalSync;
  }

  /**
   * when this grouping was last incremental synced
   * @param lastIncrementalSync1
   */
  public void setLastIncrementalSync(Timestamp lastIncrementalSync1) {
    this.lastIncrementalSync = lastIncrementalSync1;
  }

  /**
   * description of last work done
   */
  private String lastIncrementalDescription;

  /**
   * when this grouping was last incremental synced
   * @return last incremental desc
   */
  public String getLastIncrementalDescription() {
    return this.lastIncrementalDescription;
  }

  /**
   * when this grouping was last incremental synced
   * @param lastIncrementalDescription1
   */
  public void setLastIncrementalDescription(String lastIncrementalDescription1) {
    this.lastIncrementalDescription = lastIncrementalDescription1;
  }

  /**
   * records changed during last run
   */
  private Long lastIncrementalRecords;

  
  /**
   * records changed during last run
   * @return records changed
   */
  public Long getLastIncrementalRecords() {
    return this.lastIncrementalRecords;
  }

  /**
   * records changed during last run
   * @param lastIncrementalRecords1
   */
  public void setLastIncrementalRecords(Long lastIncrementalRecords1) {
    this.lastIncrementalRecords = lastIncrementalRecords1;
  }

  /**
   * how long the grouping sync took
   */
  private Long lastIncrementalTookMillis;


  /**
   * how long the grouping sync took
   * @return sync took
   */
  public Long getLastIncrementalTookMillis() {
    return this.lastIncrementalTookMillis;
  }

  /**
   * how long the grouping sync took
   * @param lastIncrementalTookMillis1
   */
  public void setLastIncrementalTookMillis(Long lastIncrementalTookMillis1) {
    this.lastIncrementalTookMillis = lastIncrementalTookMillis1;
  }

  /**
   * 
   */
  @Override
  public void gcSqlAssignNewPrimaryKeyForInsert() {
    this.id = GrouperClientUtils.uuid();
  }

}
