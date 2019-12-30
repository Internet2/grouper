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
 * if doing grouping level syncs, this is the last status
 */
@GcPersistableClass(tableName="grouper_sync_grouping", defaultFieldPersist=GcPersist.doPersist)
public class GcGrouperSyncGrouping implements GcSqlAssignPrimaryKey {

  /**
   * 
   */
  private static Log LOG = GrouperClientUtils.retrieveLog(GcGrouperSyncGrouping.class);

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
      LOG.info("GrouperSyncGrouping uuid potential mismatch: " + this.grouperSyncId + ", " + this.groupingId, e);
      // maybe a different uuid is there
      GcGrouperSyncGrouping gcGrouperSyncGrouping = this.grouperSync.retrieveGroupingByGroupingId(this.groupingId);
      if (gcGrouperSyncGrouping != null) {
        this.id = gcGrouperSyncGrouping.getId();
        new GcDbAccess().connectionName(connectionName).storeToDatabase(this);
        LOG.warn("GrouperSyncGrouping uuid mismatch corrected: " + this.grouperSyncId + ", " + this.groupingId);
      }
    }
  }

  /**
   * 
   * @return sync
   */
  public GcGrouperSync retrieveGrouperSync() {
    if (this.grouperSync == null && this.grouperSyncId != null) {
      this.grouperSync = GcGrouperSync.retrieveById(this.connectionName, this.grouperSyncId);
    }
    return this.grouperSync;
  }
  
  /**
   * 
   */
  @GcPersistableField(persist=GcPersist.dontPersist)
  private GcGrouperSync grouperSync;
  
  /**
   * 
   * @return gc grouper sync
   */
  public GcGrouperSync getGrouperSync() {
    return this.grouperSync;
  }
  
  /**
   * 
   * @param gcGrouperSync
   */
  public void setGrouperSync(GcGrouperSync gcGrouperSync) {
    this.grouperSync = gcGrouperSync;
    this.grouperSyncId = gcGrouperSync == null ? null : gcGrouperSync.getId();
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
    
    for (GcGrouperSyncGrouping theGcGrouperSyncGrouping : new GcDbAccess().connectionName("grouper").selectList(GcGrouperSyncGrouping.class)) {
      System.out.println(theGcGrouperSyncGrouping.toString());
    }
    
    // foreign key
    GcGrouperSync gcGrouperSync = new GcGrouperSync();
    gcGrouperSync.setSyncEngine("temp");
    gcGrouperSync.setProvisionerName("myJob");
    gcGrouperSync.store();
    
    GcGrouperSyncGrouping gcGrouperSyncGrouping = new GcGrouperSyncGrouping();
    gcGrouperSyncGrouping.setGrouperSync(gcGrouperSync);
    gcGrouperSyncGrouping.setLastTimeWorkWasDone(new Timestamp(System.currentTimeMillis() + 2000));
    gcGrouperSyncGrouping.groupingFromId2 = "from2";
    gcGrouperSyncGrouping.groupingFromId3 = "from3";
    gcGrouperSyncGrouping.groupingId = "myId";
    gcGrouperSyncGrouping.groupingName = "myName";
    gcGrouperSyncGrouping.groupingToId2 = "toId2";
    gcGrouperSyncGrouping.groupingToId3 = "toId3";
    gcGrouperSyncGrouping.inTargetDb = "T";
    gcGrouperSyncGrouping.inTargetEnd = new Timestamp(123L);
    gcGrouperSyncGrouping.inTargetStart = new Timestamp(234L);
    gcGrouperSyncGrouping.lastTimeWorkWasDone = new Timestamp(345L);
    gcGrouperSyncGrouping.provisionableDb = "T";
    gcGrouperSyncGrouping.provisionableEnd = new Timestamp(456L);
    gcGrouperSyncGrouping.provisionableStart = new Timestamp(567L);
    gcGrouperSyncGrouping.totalCount = 678;
    gcGrouperSyncGrouping.store();
    
    System.out.println("stored");
    
    gcGrouperSyncGrouping = gcGrouperSync.retrieveGroupingByGroupingId("myId");
    System.out.println(gcGrouperSyncGrouping);
    
    gcGrouperSyncGrouping.setGroupingToId2("toId2a");
    gcGrouperSyncGrouping.store();

    System.out.println("updated");

    for (GcGrouperSyncGrouping theGcGrouperSyncStatus : new GcDbAccess().connectionName("grouper").selectList(GcGrouperSyncGrouping.class)) {
      System.out.println(theGcGrouperSyncStatus.toString());
    }

    gcGrouperSyncGrouping.delete();
    gcGrouperSync.delete();
    
    System.out.println("deleted");

    for (GcGrouperSyncGrouping theGcGrouperSyncStatus : new GcDbAccess().connectionName("grouper").selectList(GcGrouperSyncGrouping.class)) {
      System.out.println(theGcGrouperSyncStatus.toString());
    }
  }
  
  
  /**
   * 
   */
  @Override
  public String toString() {
    return new ToStringBuilder(this)
        .append("id", this.id)
        .append("groupingId", this.groupingId)
        .append("grouperSyncId", this.grouperSyncId)
        .append("groupingFromId2", this.groupingFromId2)
        .append("groupingFromId3", this.groupingFromId3)
        .append("groupingName", this.groupingName)
        .append("groupingToId2", this.groupingToId2)
        .append("groupingToId3", this.groupingFromId3)
        .append("inTarget", this.isInTarget())
        .append("inTargetStart", this.getInTargetStart())
        .append("inTargetEnd", this.getInTargetEnd())
        .append("lastTimeWorkWasDone", this.getLastTimeWorkWasDone())
        .append("provisionable", this.isProvisionable())
        .append("provisionableStart", this.getProvisionableStart())
        .append("provisionableEnd", this.getProvisionableEnd())
        .append("totalCount", this.getTotalCount())
        .append("lastUpdated", this.lastUpdated)
        .append("lastTimeWorkWasDone", this.lastTimeWorkWasDone).build();
  }

  /**
   * last time a record was processed
   */
  private Timestamp lastTimeWorkWasDone;
  
  /**
   * last time a record was processe
   * @return last time a record was processed
   */
  public Timestamp getLastTimeWorkWasDone() {
    return this.lastTimeWorkWasDone;
  }

  /**
   * last time a record was processe
   * @param lastTimeWorkWasDone1
   */
  public void setLastTimeWorkWasDone(Timestamp lastTimeWorkWasDone1) {
    this.lastTimeWorkWasDone = lastTimeWorkWasDone1;
  }

  /**
   * 
   */
  public GcGrouperSyncGrouping() {
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
  @GcPersistableField(columnName="in_target")
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
   * if in target
   * @return if in target
   */
  public boolean isInTarget() {
    return GrouperClientUtils.booleanValue(this.inTargetDb, false);
  }

  /**
   * if in target
   * @param in target
   */
  public void setInTarget(boolean inTarget) {
    this.inTargetDb = inTarget ? "T" : "F";
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
   * for groups this is the group idIndex
   */
  private String groupingId;
  
  /**
   * for groups this is the group idIndex
   * @return group id
   */
  public String getGroupingId() {
    return this.groupingId;
  }

  /**
   * for groups this is the group idIndex
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
   * T if provisionable and F is not
   */
  @GcPersistableField(columnName="provisionable")
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
  private Timestamp provisionableStart;
    
  /**
   * millis since 1970 that this grouping started to be provisionable
   * @return millis
   */
  public Timestamp getProvisionableStart() {
    return this.provisionableStart;
  }

  /**
   * millis since 1970 that this grouping started to be provisionable
   * @param provisionableStartMillis1
   */
  public void setProvisionableStart(Timestamp provisionableStartMillis1) {
    this.provisionableStart = provisionableStartMillis1;
  }

  /**
   * when this group was provisioned to target
   */
  private Timestamp inTargetStart;
  
  /**
   * when this group was removed from target
   */
  private Timestamp inTargetEnd;

  
  
  /**
   * when this group was provisioned to target
   * @return when
   */
  public Timestamp getInTargetStart() {
    return this.inTargetStart;
  }

  /**
   * when this group was provisioned to target
   * @param inTargetStart1
   */
  public void setInTargetStart(Timestamp inTargetStart1) {
    this.inTargetStart = inTargetStart1;
  }

  /**
   * when this group was provisioned to target
   * @return when
   */
  public Timestamp getInTargetEnd() {
    return this.inTargetEnd;
  }

  /**
   * when this group was provisioned to target
   * @param inTargetEnd1
   */
  public void setInTargetEnd(Timestamp inTargetEnd1) {
    this.inTargetEnd = inTargetEnd1;
  }

  /**
   * millis since 1970 that this grouping ended being provisionable
   */
  private Timestamp provisionableEnd;

  /**
   * millis since 1970 that this grouping ended being provisionable
   * @return millis
   */
  public Timestamp getProvisionableEnd() {
    return this.provisionableEnd;
  }

  /**
   * millis since 1970 that this grouping ended being provisionable
   * @param provisionableEndMillis1
   */
  public void setProvisionableEnd(Timestamp provisionableEndMillis1) {
    this.provisionableEnd = provisionableEndMillis1;
  }

  /**
   * number of records in this grouping
   */
  private Integer totalCount;

  /**
   * number of records in this grouping
   * @return total count
   */
  public Integer getTotalCount() {
    return this.totalCount;
  }

  /**
   * number of records in this grouping
   * @param totalCount1
   */
  public void setTotalCount(Integer totalCount1) {
    this.totalCount = totalCount1;
  }

  /**
   * for groups this is the group uuid
   */
  private String groupingFromId2;

  /**
   * for groups this is the group uuid
   * @return grouping from id 2
   */
  public String getGroupingFromId2() {
    return this.groupingFromId2;
  }

  /**
   * for groups this is the group uuid
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
   * 
   */
  @Override
  public void gcSqlAssignNewPrimaryKeyForInsert() {
    this.id = GrouperClientUtils.uuid();
  }


}
