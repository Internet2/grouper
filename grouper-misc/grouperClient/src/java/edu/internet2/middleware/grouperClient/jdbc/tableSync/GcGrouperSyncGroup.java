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
import edu.internet2.middleware.grouperClient.jdbc.GcPersistableHelper;
import edu.internet2.middleware.grouperClient.jdbc.GcSqlAssignPrimaryKey;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.lang3.builder.ToStringBuilder;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.logging.Log;


/**
 * data about groups being synced
 */
@GcPersistableClass(tableName="grouper_sync_group", defaultFieldPersist=GcPersist.doPersist)
public class GcGrouperSyncGroup implements GcSqlAssignPrimaryKey {

  /**
   * delete all data if table is here
   */
  public static void reset() {
    
    try {
      // if its not there forget about it... TODO remove this in 2.5+
      new GcDbAccess().connectionName("grouper").sql("select * from " + GcPersistableHelper.tableName(GcGrouperSyncGroup.class) + " where 1 != 1").select(Integer.class);
    } catch (Exception e) {
      return;
    }

    new GcDbAccess().connectionName("grouper").sql("delete from " + GcPersistableHelper.tableName(GcGrouperSyncGroup.class)).executeSql();
  }

  /**
   * 
   */
  private static Log LOG = GrouperClientUtils.retrieveLog(GcGrouperSyncGroup.class);

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
      LOG.info("GrouperSyncGroup uuid potential mismatch: " + this.grouperSyncId + ", " + this.groupId, re);
      // maybe a different uuid is there
      GcGrouperSyncGroup gcGrouperSyncGroup = this.grouperSync.retrieveGroupByGroupId(this.groupId);
      if (gcGrouperSyncGroup != null) {
        this.id = gcGrouperSyncGroup.getId();
        new GcDbAccess().connectionName(connectionName).storeToDatabase(this);
        LOG.warn("GrouperSyncGroup uuid mismatch corrected: " + this.grouperSyncId + ", " + this.groupId);
      } else {
        throw re;
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
    
    for (GcGrouperSyncGroup theGcGrouperSyncGroup : new GcDbAccess().connectionName("grouper").selectList(GcGrouperSyncGroup.class)) {
      System.out.println(theGcGrouperSyncGroup.toString());
    }
    
    // foreign key
    GcGrouperSync gcGrouperSync = new GcGrouperSync();
    gcGrouperSync.setSyncEngine("temp");
    gcGrouperSync.setProvisionerName("myJob");
    gcGrouperSync.store();
    
    GcGrouperSyncGroup gcGrouperSyncGroup = new GcGrouperSyncGroup();
    gcGrouperSyncGroup.setGrouperSync(gcGrouperSync);
    gcGrouperSyncGroup.setLastTimeWorkWasDone(new Timestamp(System.currentTimeMillis() + 2000));
    gcGrouperSyncGroup.groupFromId2 = "from2";
    gcGrouperSyncGroup.groupFromId3 = "from3";
    gcGrouperSyncGroup.groupId = "myId";
    gcGrouperSyncGroup.groupName = "myName";
    gcGrouperSyncGroup.groupToId2 = "toId2";
    gcGrouperSyncGroup.groupToId3 = "toId3";
    gcGrouperSyncGroup.inTargetDb = "T";
    gcGrouperSyncGroup.inTargetInsertOrExistsDb = "T";
    gcGrouperSyncGroup.inTargetEnd = new Timestamp(123L);
    gcGrouperSyncGroup.inTargetStart = new Timestamp(234L);
    gcGrouperSyncGroup.lastTimeWorkWasDone = new Timestamp(345L);
    gcGrouperSyncGroup.provisionableDb = "T";
    gcGrouperSyncGroup.provisionableEnd = new Timestamp(456L);
    gcGrouperSyncGroup.provisionableStart = new Timestamp(567L);
    gcGrouperSyncGroup.store();
    
    System.out.println("stored");
    
    gcGrouperSyncGroup = gcGrouperSync.retrieveGroupByGroupId("myId");
    System.out.println(gcGrouperSyncGroup);
    
    gcGrouperSyncGroup.setGroupToId2("toId2a");
    gcGrouperSyncGroup.store();

    System.out.println("updated");

    for (GcGrouperSyncGroup theGcGrouperSyncStatus : new GcDbAccess().connectionName("grouper").selectList(GcGrouperSyncGroup.class)) {
      System.out.println(theGcGrouperSyncStatus.toString());
    }

    gcGrouperSyncGroup.delete();
    gcGrouperSync.delete();
    
    System.out.println("deleted");

    for (GcGrouperSyncGroup theGcGrouperSyncStatus : new GcDbAccess().connectionName("grouper").selectList(GcGrouperSyncGroup.class)) {
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
        .append("groupId", this.groupId)
        .append("grouperSyncId", this.grouperSyncId)
        .append("groupFromId2", this.groupFromId2)
        .append("groupFromId3", this.groupFromId3)
        .append("groupName", this.groupName)
        .append("groupToId2", this.groupToId2)
        .append("groupToId3", this.groupFromId3)
        .append("inTarget", this.isInTarget())
        .append("inTargetInsertOrExists", this.isInTargetInsertOrExists())
        .append("inTargetStart", this.getInTargetStart())
        .append("inTargetEnd", this.getInTargetEnd())
        .append("provisionable", this.isProvisionable())
        .append("provisionableStart", this.getProvisionableStart())
        .append("provisionableEnd", this.getProvisionableEnd())
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
  public GcGrouperSyncGroup() {
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
   * if this group exists in the target/destination
   */
  @GcPersistableField(columnName="in_target")
  private String inTargetDb;
  
  /**
   * if this group exists in the target/destination
   * @return if in target
   */
  public String getInTargetDb() {
    return this.inTargetDb;
  }

  /**
   * if this group exists in the target/destination
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
   * if this group exists in the target/destination
   * @return if is target
   */
  public Boolean getInTarget() {
    return GrouperClientUtils.booleanObjectValue(this.inTargetDb);
  }
  
  /**
   * T if inserted on the in_target_start date, or F if it existed then and not sure when inserted
   */
  @GcPersistableField(columnName="in_target_insert_or_exists")
  private String inTargetInsertOrExistsDb;

  /**
   * T if inserted on the in_target_start date, or F if it existed then and not sure when inserted
   * @return true or false
   */
  public String getInTargetInsertOrExistsDb() {
    return this.inTargetInsertOrExistsDb;
  }

  /**
   * T if inserted on the in_target_start date, or F if it existed then and not sure when inserted
   * @param inTargetInsertOrExistsDb1
   */
  public void setInTargetInsertOrExistsDb(String inTargetInsertOrExistsDb1) {
    this.inTargetInsertOrExistsDb = inTargetInsertOrExistsDb1;
  }

  /**
   * T if inserted on the in_target_start date, or F if it existed then and not sure when inserted
   * @return true or false
   */
  public boolean isInTargetInsertOrExists() {
    return GrouperClientUtils.booleanValue(this.inTargetInsertOrExistsDb, false);
  }
  
  /**
   * T if inserted on the in_target_start date, or F if it existed then and not sure when inserted
   * @param inTargetInsertOrExists
   */
  public void setInTargetInsertOrExists(boolean inTargetInsertOrExists) {
    this.inTargetInsertOrExistsDb = inTargetInsertOrExists ? "T" : "F";
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
    if (this.grouperSync == null || !GrouperClientUtils.equals(this.grouperSync.getId(), grouperSyncId1)) {
      this.grouperSync = null;
    }
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
   * for groups this is the group uuid, though not a real foreign key
   */
  private String groupId;
  
  /**
   * for groups this is the group uuid, though not a real foreign key
   * @return group id
   */
  public String getGroupId() {
    return this.groupId;
  }

  /**
   * for groups this is the group uuid, though not a real foreign key
   * @param groupId1
   */
  public void setGroupId(String groupId1) {
    this.groupId = groupId1;
  }

  /**
   * for groups this is the group system name
   */
  private String groupName;
  
  /**
   * for groups this is the group system name
   * @return group name
   */
  public String getGroupName() {
    return this.groupName;
  }

  /**
   * for groups this is the group system name
   * @param groupName1
   */
  public void setGroupName(String groupName1) {
    this.groupName = groupName1;
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
   * millis since 1970 that this group started to be provisionable
   */
  private Timestamp provisionableStart;
    
  /**
   * millis since 1970 that this group started to be provisionable
   * @return millis
   */
  public Timestamp getProvisionableStart() {
    return this.provisionableStart;
  }

  /**
   * millis since 1970 that this group started to be provisionable
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
   * millis since 1970 that this group ended being provisionable
   */
  private Timestamp provisionableEnd;

  /**
   * millis since 1970 that this group ended being provisionable
   * @return millis
   */
  public Timestamp getProvisionableEnd() {
    return this.provisionableEnd;
  }

  /**
   * millis since 1970 that this group ended being provisionable
   * @param provisionableEndMillis1
   */
  public void setProvisionableEnd(Timestamp provisionableEndMillis1) {
    this.provisionableEnd = provisionableEndMillis1;
  }

  /**
   * for groups this is the group idIndex
   */
  private String groupFromId2;

  /**
   * for groups this is the group idIndex
   * @return group from id 2
   */
  public String getGroupFromId2() {
    return this.groupFromId2;
  }

  /**
   * for groups this is the group idIndex
   * @param groupFromId2_1
   */
  public void setGroupFromId2(String groupFromId2_1) {
    this.groupFromId2 = groupFromId2_1;
  }

  /**
   * other metadata on groups
   */
  private String groupFromId3;

  /**
   * other metadata on groups
   * @return id3
   */
  public String getGroupFromId3() {
    return this.groupFromId3;
  }

  /**
   * other metadata on groups
   * @param groupFromId3_1
   */
  public void setGroupFromId3(String groupFromId3_1) {
    this.groupFromId3 = groupFromId3_1;
  }

  /**
   * other metadata on groups
   */
  private String groupToId2;
  
  /**
   * other metadata on groups
   * @return metadata
   */
  public String getGroupToId2() {
    return this.groupToId2;
  }

  /**
   * other metadata on groups
   * @param groupToId2_1
   */
  public void setGroupToId2(String groupToId2_1) {
    this.groupToId2 = groupToId2_1;
  }

  /**
   * other metadata on groups
   */
  private String groupToId3;

  /**
   * other metadata on groups
   * @return group id
   */
  public String getGroupToId3() {
    return this.groupToId3;
  }

  /**
   * other metadata on groups
   * @param groupToId3_1
   */
  public void setGroupToId3(String groupToId3_1) {
    this.groupToId3 = groupToId3_1;
  }

  /**
   * 
   */
  @Override
  public void gcSqlAssignNewPrimaryKeyForInsert() {
    this.id = GrouperClientUtils.uuid();
  }


  /**
   * select grouper sync group by id
   * @param theConnectionName
   * @param provisionerName
   * @return the sync
   */
  public static GcGrouperSyncGroup retrieveById(String theConnectionName, String id) {
    theConnectionName = GcGrouperSync.defaultConnectionName(theConnectionName);
    GcGrouperSyncGroup gcGrouperSyncGroup = new GcDbAccess().connectionName(theConnectionName)
        .sql("select * from grouper_sync_group where id = ?").addBindVar(id).select(GcGrouperSyncGroup.class);
    if (gcGrouperSyncGroup != null) {
      gcGrouperSyncGroup.connectionName = theConnectionName;
    }
    return gcGrouperSyncGroup;
  }


  /**
   * select grouper sync group by sync id and group id
   * @param grouperSyncId
   * @param grouperGroupId
   * @param provisionerName
   * @return the sync
   */
  public static GcGrouperSyncGroup retrieveBySyncIdAndGroupId(String theConnectionName, String grouperSyncId, String grouperGroupId) {
    theConnectionName = GcGrouperSync.defaultConnectionName(theConnectionName);
    GcGrouperSyncGroup gcGrouperSyncGroup = new GcDbAccess().connectionName(theConnectionName)
        .sql("select * from grouper_sync_group where grouper_sync_id = ? and group_id = ?").addBindVar(grouperSyncId).addBindVar(grouperGroupId).select(GcGrouperSyncGroup.class);
    if (gcGrouperSyncGroup != null) {
      gcGrouperSyncGroup.connectionName = theConnectionName;
    }
    return gcGrouperSyncGroup;
  }


}
