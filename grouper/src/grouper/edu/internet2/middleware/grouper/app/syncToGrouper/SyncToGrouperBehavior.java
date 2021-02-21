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
   * should we sync stems from top level stems or pass in the stems to sync
   */
  private boolean stemSyncFromStems;

  /**
   * should we sync stems from top level stems or pass in the stems to sync
   * @return
   */
  public boolean isStemSyncFromStems() {
    return stemSyncFromStems;
  }

  /**
   * should we sync stems from top level stems or pass in the stems to sync
   * @param stemSyncFromStems
   */
  public void setStemSyncFromStems(boolean stemSyncFromStems) {
    this.stemSyncFromStems = stemSyncFromStems;
  }

  /**
   * should we sync composites from stems or pass in the composites to sync
   */
  private boolean compositeSyncFromStems;

  /**
   * should we sync composites from stems or pass in the composites to sync
   * @return
   */
  public boolean isCompositeSyncFromStems() {
    return compositeSyncFromStems;
  }

  /**
   * should we sync composites from stems or pass in the composites to sync
   * @param compositeSyncFromStems
   */
  public void setCompositeSyncFromStems(boolean compositeSyncFromStems) {
    this.compositeSyncFromStems = compositeSyncFromStems;
  }

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
   * delete extra composites in the ones managed
   */
  private boolean compositeDeleteExtra;

  /**
   * insert composites
   */
  private boolean compositeInsert;

  /**
   * should we sync composites
   */
  private boolean compositeSync;

  /**
   * update composites
   */
  private boolean compositeUpdate;

  /**
   * sync id on insert
   */
  private boolean compositeSyncFieldIdOnInsert;

  /**
   * delete extra memberships in the ones managed
   */
  private boolean membershipDeleteExtra;

  /**
   * insert memberships
   */
  private boolean membershipInsert;

  /**
   * should we sync memberships
   */
  private boolean membershipSync;

  /**
   * sync id on insert
   */
  private boolean membershipSyncFieldIdOnInsert;

  /**
   * sync enabled and disabled times (millis since 1970)
   */
  private boolean membershipSyncFieldsEnabledDisabled;

  /**
   * should we sync memberships from stems or pass in the composites to sync
   */
  private boolean membershipSyncFromStems;

  /**
   * update memberships
   */
  private boolean membershipUpdate;
  
  /**
   * delete extra memberships in the ones managed
   * @return
   */
  public boolean isMembershipDeleteExtra() {
    return membershipDeleteExtra;
  }

  /**
   * delete extra memberships in the ones managed
   * @param membershipDeleteExtra
   */
  public void setMembershipDeleteExtra(boolean membershipDeleteExtra) {
    this.membershipDeleteExtra = membershipDeleteExtra;
  }

  /**
   * insert memberships
   * @return
   */
  public boolean isMembershipInsert() {
    return membershipInsert;
  }

  /**
   * insert memberships
   * @param membershipInsert
   */
  public void setMembershipInsert(boolean membershipInsert) {
    this.membershipInsert = membershipInsert;
  }

  /**
   * should we sync memberships
   * @return
   */
  public boolean isMembershipSync() {
    return membershipSync;
  }

  /**
   * should we sync memberships
   * @param membershipSync
   */
  public void setMembershipSync(boolean membershipSync) {
    this.membershipSync = membershipSync;
  }

  /**
   * sync id on insert
   * @return
   */
  public boolean isMembershipSyncFieldIdOnInsert() {
    return membershipSyncFieldIdOnInsert;
  }

  /**
   * sync id on insert
   * @param membershipSyncFieldIdOnInsert
   */
  public void setMembershipSyncFieldIdOnInsert(boolean membershipSyncFieldIdOnInsert) {
    this.membershipSyncFieldIdOnInsert = membershipSyncFieldIdOnInsert;
  }

  /**
   * sync enabled and disabled times (millis since 1970)
   * @return
   */
  public boolean isMembershipSyncFieldsEnabledDisabled() {
    return membershipSyncFieldsEnabledDisabled;
  }

  /**
   * sync enabled and disabled times (millis since 1970)
   * @param membershipSyncFieldsEnabledDisabled
   */
  public void setMembershipSyncFieldsEnabledDisabled(
      boolean membershipSyncFieldsEnabledDisabled) {
    this.membershipSyncFieldsEnabledDisabled = membershipSyncFieldsEnabledDisabled;
  }

  /**
   * should we sync memberships from stems or pass in the composites to sync
   * @return
   */
  public boolean isMembershipSyncFromStems() {
    return membershipSyncFromStems;
  }

  /**
   * should we sync memberships from stems or pass in the composites to sync
   * @param membershipSyncFromStems
   */
  public void setMembershipSyncFromStems(boolean membershipSyncFromStems) {
    this.membershipSyncFromStems = membershipSyncFromStems;
  }

  /**
   * update memberships
   * @return
   */
  public boolean isMembershipUpdate() {
    return membershipUpdate;
  }

  /**
   * update memberships
   * @param membershipUpdate
   */
  public void setMembershipUpdate(boolean membershipUpdate) {
    this.membershipUpdate = membershipUpdate;
  }

  /**
   * sync id on insert
   * @return
   */
  public boolean isCompositeSyncFieldIdOnInsert() {
    return compositeSyncFieldIdOnInsert;
  }

  /**
   * sync id on insert
   * @param compositeSyncFieldIdOnInsert
   */
  public void setCompositeSyncFieldIdOnInsert(boolean compositeSyncFieldIdOnInsert) {
    this.compositeSyncFieldIdOnInsert = compositeSyncFieldIdOnInsert;
  }

  /**
   * delete extra composites in the ones managed
   * @return
   */
  public boolean isCompositeDeleteExtra() {
    return compositeDeleteExtra;
  }

  /**
   * delete extra composites in the ones managed
   * @param compositeDeleteExtra
   */
  public void setCompositeDeleteExtra(boolean compositeDeleteExtra) {
    this.compositeDeleteExtra = compositeDeleteExtra;
  }

  /**
   * insert composites
   * @return
   */
  public boolean isCompositeInsert() {
    return compositeInsert;
  }

  /**
   * insert composites
   * @param compositeInsert
   */
  public void setCompositeInsert(boolean compositeInsert) {
    this.compositeInsert = compositeInsert;
  }

  /**
   * should we sync composites
   * @return
   */
  public boolean isCompositeSync() {
    return compositeSync;
  }

  /**
   * should we sync composites
   * @param compositeSync
   */
  public void setCompositeSync(boolean compositeSync) {
    this.compositeSync = compositeSync;
  }

  /**
   * update composites
   * @return
   */
  public boolean isCompositeUpdate() {
    return compositeUpdate;
  }

  /**
   * update composites
   * @param compositeUpdate
   */
  public void setCompositeUpdate(boolean compositeUpdate) {
    this.compositeUpdate = compositeUpdate;
  }

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
