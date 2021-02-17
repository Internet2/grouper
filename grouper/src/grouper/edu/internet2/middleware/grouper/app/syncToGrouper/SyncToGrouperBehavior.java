package edu.internet2.middleware.grouper.app.syncToGrouper;

/**
 * 
 * @author mchyzer
 *
 */
public class SyncToGrouperBehavior {

  /**
   * should we sync stems
   */
  private boolean stemSync;
  
  /**
   * should we sync stems
   * @return
   */
  public boolean isStemSync() {
    return stemSync;
  }

  /**
   * should we sync stems
   * @param stemSync
   */
  public void setStemSync(boolean stemSync) {
    this.stemSync = stemSync;
  }

  /**
   * insert stems
   */
  private boolean stemInsert;

  /**
   * insert stems
   * @return if stem insert
   */
  public boolean isStemInsert() {
    return stemInsert;
  }

  /**
   * insert stems
   * @param stemInsert
   */
  public void setStemInsert(boolean stemInsert) {
    this.stemInsert = stemInsert;
  }

  /**
   * update stems
   * @return
   */
  public boolean isStemUpdate() {
    return stemUpdate;
  }

  /**
   * update stems
   * @param stemUpdate
   */
  public void setStemUpdate(boolean stemUpdate) {
    this.stemUpdate = stemUpdate;
  }

  /**
   * update stems
   */
  private boolean stemUpdate;

  /**
   * sync id index
   */
  private boolean stemSyncFieldIdIndexOnInsert;
  
  /**
   * sync id on insert
   */
  private boolean stemSyncFieldIdOnInsert;
  
  
  /**
   * sync display name
   */
  private boolean stemSyncFieldDisplayName;

  /**
   * sync description
   */
  private boolean stemSyncFieldDescription;

  /**
   * sync stem alternate name
   */
  private boolean stemSyncFieldAlternateName;

  
  
  /**
   * sync id index
   * @return
   */
  public boolean isStemSyncFieldIdIndexOnInsert() {
    return stemSyncFieldIdIndexOnInsert;
  }

  /**
   * sync id index
   * @param stemSyncFieldIdIndex
   */
  public void setStemSyncFieldIdIndexOnInsert(boolean stemSyncFieldIdIndex) {
    this.stemSyncFieldIdIndexOnInsert = stemSyncFieldIdIndex;
  }

  /**
   * sync id on insert
   * @return
   */
  public boolean isStemSyncFieldIdOnInsert() {
    return stemSyncFieldIdOnInsert;
  }

  /**
   * sync id on insert
   * @param stemSyncFieldIdOnInsert
   */
  public void setStemSyncFieldIdOnInsert(boolean stemSyncFieldIdOnInsert) {
    this.stemSyncFieldIdOnInsert = stemSyncFieldIdOnInsert;
  }

  /**
   * sync display name
   * @return
   */
  public boolean isStemSyncFieldDisplayName() {
    return stemSyncFieldDisplayName;
  }

  /**
   * sync display name
   * @param stemSyncFieldDisplayName
   */
  public void setStemSyncFieldDisplayName(boolean stemSyncFieldDisplayName) {
    this.stemSyncFieldDisplayName = stemSyncFieldDisplayName;
  }

  /**
   * sync description
   * @return
   */
  public boolean isStemSyncFieldDescription() {
    return stemSyncFieldDescription;
  }

  /**
   * sync description
   * @param stemSyncFieldDescription
   */
  public void setStemSyncFieldDescription(boolean stemSyncFieldDescription) {
    this.stemSyncFieldDescription = stemSyncFieldDescription;
  }

  /**
   * sync stem alternate name
   * @return
   */
  public boolean isStemSyncFieldAlternateName() {
    return stemSyncFieldAlternateName;
  }

  /**
   * sync stem alternate name
   * @param stemSyncFieldAlternateName
   */
  public void setStemSyncFieldAlternateName(boolean stemSyncFieldAlternateName) {
    this.stemSyncFieldAlternateName = stemSyncFieldAlternateName;
  }

  /**
   * delete extra stems in the ones managed
   */
  private boolean stemDeleteExtra;

  /**
   * should we sync stems
   * @return
   */
  public boolean isStemDeleteExtra() {
    return stemDeleteExtra;
  }

  /**
   * should we sync stems
   * @param stemDeleteExtra
   */
  public void setStemDeleteExtra(boolean stemDeleteExtra) {
    this.stemDeleteExtra = stemDeleteExtra;
  }

  /**
   * load from sql
   */
  private boolean sqlLoad = false;
  
  /**
   * load from sql
   * @param b
   */
  public void setSqlLoad(boolean b) {
    sqlLoad = b;
  }

  /**
   * load from sql
   * @return
   */
  public boolean isSqlLoad() {
    return sqlLoad;
  }

  /**
   * sql load from another grouper
   */
  private boolean sqlLoadFromAnotherGrouper;

  /**
   * delete extra groups in the stems managed
   */
  private boolean groupDeleteExtra;

  /**
   * insert groups
   */
  private boolean groupInsert;

  /**
   * should we sync groups
   */
  private boolean groupSync;

  /**
   * should we sync groups from stems or pass in the groups to sync
   */
  private boolean groupSyncFromStems;

  /**
   * should we sync groups from stems or pass in the groups to sync
   * @return
   */
  public boolean isGroupSyncFromStems() {
    return groupSyncFromStems;
  }

  /**
   * should we sync groups from stems or pass in the groups to sync
   * @param groupSyncfromStems1
   */
  public void setGroupSyncFromStems(boolean groupSyncfromStems1) {
    this.groupSyncFromStems = groupSyncfromStems1;
  }

  /**
   * sync group alternate name
   */
  private boolean groupSyncFieldAlternateName;

  /**
   * sync description
   */
  private boolean groupSyncFieldDescription;

  /**
   * sync display name
   */
  private boolean groupSyncFieldDisplayName;

  /**
   * sync id index
   */
  private boolean groupSyncFieldIdIndexOnInsert;

  /**
   * sync id on insert
   */
  private boolean groupSyncFieldIdOnInsert;

  /**
   * update groups
   */
  private boolean groupUpdate;

  /**
   * delete extra groups in the stems managed
   * @return
   */
  public boolean isGroupDeleteExtra() {
    return groupDeleteExtra;
  }

  /**
   * delete extra groups in the stems managed
   * @param groupDeleteExtra
   */
  public void setGroupDeleteExtra(boolean groupDeleteExtra) {
    this.groupDeleteExtra = groupDeleteExtra;
  }

  /**
   * insert groups
   * @return
   */
  public boolean isGroupInsert() {
    return groupInsert;
  }

  /**
   * insert groups
   * @param groupInsert
   */
  public void setGroupInsert(boolean groupInsert) {
    this.groupInsert = groupInsert;
  }

  /**
   * should we sync groups
   * @return
   */
  public boolean isGroupSync() {
    return groupSync;
  }

  /**
   * should we sync groups
   * @param groupSync
   */
  public void setGroupSync(boolean groupSync) {
    this.groupSync = groupSync;
  }

  /**
   * sync group alternate name
   * @return
   */
  public boolean isGroupSyncFieldAlternateName() {
    return groupSyncFieldAlternateName;
  }

  /**
   * sync group alternate name
   * @param groupSyncFieldAlternateName
   */
  public void setGroupSyncFieldAlternateName(boolean groupSyncFieldAlternateName) {
    this.groupSyncFieldAlternateName = groupSyncFieldAlternateName;
  }

  /**
   * if should sync field disabled timestamp
   */
  private boolean groupSyncFieldDisabledTimestamp;
  
  /**
   * if should sync field disabled timestamp
   * @return
   */
  public boolean isGroupSyncFieldDisabledTimestamp() {
    return groupSyncFieldDisabledTimestamp;
  }

  /**
   * if should sync field disabled timestamp
   * @param groupSyncFieldDisabledTimestamp
   */
  public void setGroupSyncFieldDisabledTimestamp(boolean groupSyncFieldDisabledTimestamp) {
    this.groupSyncFieldDisabledTimestamp = groupSyncFieldDisabledTimestamp;
  }

  /**
   * if should sync field enabled timestamp
   */
  private boolean groupSyncFieldEnabledTimestamp;
  
  /**
   * if should sync field enabled timestamp
   * @return
   */
  public boolean isGroupSyncFieldEnabledTimestamp() {
    return groupSyncFieldEnabledTimestamp;
  }

  /**
   * if should sync field enabled timestamp
   * @param groupSyncFieldEnabledTimestamp
   */
  public void setGroupSyncFieldEnabledTimestamp(boolean groupSyncFieldEnabledTimestamp) {
    this.groupSyncFieldEnabledTimestamp = groupSyncFieldEnabledTimestamp;
  }
  
  /**
   * if should sync field type of group
   */
  private boolean groupSyncFieldTypeOfGroup;
  
  /**
   * if should sync field type of group
   * @return
   */
  public boolean isGroupSyncFieldTypeOfGroup() {
    return groupSyncFieldTypeOfGroup;
  }

  /**
   * if should sync field type of group
   * @param groupSyncFieldTypeOfGroup
   */
  public void setGroupSyncFieldTypeOfGroup(boolean groupSyncFieldTypeOfGroup) {
    this.groupSyncFieldTypeOfGroup = groupSyncFieldTypeOfGroup;
  }

  /**
   * sync description
   * @return
   */
  public boolean isGroupSyncFieldDescription() {
    return groupSyncFieldDescription;
  }

  /**
   * sync description
   * @param groupSyncFieldDescription
   */
  public void setGroupSyncFieldDescription(boolean groupSyncFieldDescription) {
    this.groupSyncFieldDescription = groupSyncFieldDescription;
  }

  /**
   * sync display name
   * @return
   */
  public boolean isGroupSyncFieldDisplayName() {
    return groupSyncFieldDisplayName;
  }

  /**
   * sync display name
   * @param groupSyncFieldDisplayName
   */
  public void setGroupSyncFieldDisplayName(boolean groupSyncFieldDisplayName) {
    this.groupSyncFieldDisplayName = groupSyncFieldDisplayName;
  }

  /**
   * sync id index
   * @return
   */
  public boolean isGroupSyncFieldIdIndexOnInsert() {
    return groupSyncFieldIdIndexOnInsert;
  }

  /**
   * sync id index
   * @param groupSyncFieldIdIndexOnInsert
   */
  public void setGroupSyncFieldIdIndexOnInsert(boolean groupSyncFieldIdIndexOnInsert) {
    this.groupSyncFieldIdIndexOnInsert = groupSyncFieldIdIndexOnInsert;
  }

  /**
   * sync id on insert
   * @return
   */
  public boolean isGroupSyncFieldIdOnInsert() {
    return groupSyncFieldIdOnInsert;
  }

  /**
   * sync id on insert
   * @param groupSyncFieldIdOnInsert
   */
  public void setGroupSyncFieldIdOnInsert(boolean groupSyncFieldIdOnInsert) {
    this.groupSyncFieldIdOnInsert = groupSyncFieldIdOnInsert;
  }

  /**
   * update groups
   * @return
   */
  public boolean isGroupUpdate() {
    return groupUpdate;
  }

  /**
   * update groups
   * @param groupUpdate
   */
  public void setGroupUpdate(boolean groupUpdate) {
    this.groupUpdate = groupUpdate;
  }

  /**
   * sql load from another grouper
   * @return
   */
  public boolean isSqlLoadFromAnotherGrouper() {
    return sqlLoadFromAnotherGrouper;
  }

  /**
   * sql load from another grouper
   * @param sqlLoadFromAnotherGrouper
   */
  public void setSqlLoadFromAnotherGrouper(boolean sqlLoadFromAnotherGrouper) {
    this.sqlLoadFromAnotherGrouper = sqlLoadFromAnotherGrouper;
  }

}
