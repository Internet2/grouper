package edu.internet2.middleware.grouper.app.provisioning;

/**
 * state about the increment or provisioning in general for this item
 * @author mchyzer
 *
 */
public class ProvisioningStateBase {
  
  private boolean loggable;
  
  public boolean isLoggableHelper() {
    return loggable;
  }

  
  public void setLoggable(boolean loggable) {
    this.loggable = loggable;
  }


  /**
   * insert, update, or delete
   */
  private GrouperIncrementalDataAction grouperIncrementalDataAction;

  /**
   * if we are recalcing this object (not including group memberships or entity memberships)
   * this means compare the grouper and target side. for groups and entities, if recalcObject is true, we always fetch from target then
   * for memberships, even if recalcObject is true, we might not fetch individual memberships from target. 
   * for e.g. we're recalcing group memberships, we don't need to fetch individual memberships for that group
   */
  private boolean recalcObject;
  
  /**
   * when this action happened millis since 1970
   */
  private Long millisSince1970;

  
  /**
   * insert, update, or delete
   * @return
   */
  public GrouperIncrementalDataAction getGrouperIncrementalDataAction() {
    return grouperIncrementalDataAction;
  }

  /**
   * insert, update, or delete
   * @param grouperIncrementalDataAction
   */
  public void setGrouperIncrementalDataAction(
      GrouperIncrementalDataAction grouperIncrementalDataAction) {
    this.grouperIncrementalDataAction = grouperIncrementalDataAction;
  }

  /**
   * if we are recalcing this object (not including memberships)
   * @return
   */
  public boolean isRecalcObject() {
    return recalcObject;
  }

  /**
   * if we are recalcing this object (not including memberships)
   * @param recalc
   */
  public void setRecalcObject(boolean recalc) {
    this.recalcObject = recalc;
  }

  /**
   * when this action happened millis since 1970
   * @return
   */
  public Long getMillisSince1970() {
    return millisSince1970;
  }

  /**
   * when this action happened millis since 1970
   * @param millisSince1970
   */
  public void setMillisSince1970(Long millisSince1970) {
    this.millisSince1970 = millisSince1970;
  }
  
  /**
   * should we select this from target individually... i.e. for memberships, if 
   * selecting all memberships for group, then no need to select individual memberships
   */
  private boolean select;
  
  /**
   * should we select this from target individually... i.e. for memberships, if 
   * selecting all memberships for group, then no need to select individual memberships
   * @return
   */
  public boolean isSelect() {
    return select;
  }

  /**
   * should we select this from target individually... i.e. for memberships, if 
   * selecting all memberships for group, then no need to select individual memberships
   * @param select
   */
  public void setSelect(boolean select) {
    this.select = select;
  }

  /**
   * if this item was attempted to be selected from target.  Note, for memberships, if
   * selecting all memberships for a group, then the memberships retrieved will be selected result processed true
   */
  private boolean selectResultProcessed;

  
  
  /**
   * if this item was attempted to be selected from target.  Note, for memberships, if
   * selecting all memberships for a group, then the memberships retrieved will be selected result processed true
   * @return
   */
  public boolean isSelectResultProcessed() {
    return selectResultProcessed;
  }


  /**
   * if this item was attempted to be selected from target.  Note, for memberships, if
   * selecting all memberships for a group, then the memberships retrieved will be selected result processed true
   * @param selectResultProcessed
   */
  public void setSelectResultProcessed(boolean selectResultProcessed) {
    this.selectResultProcessed = selectResultProcessed;
  }

  /**
   * if this insert was sent to target
   */
  private boolean insertResultProcessed;
  
  /**
   * if this update was sent to target
   */
  private boolean updateResultProcessed;
  
  /**
   * if this delete was sent to target
   */
  private boolean deleteResultProcessed;

  /**
   * if this is for a create in target
   */
  private boolean create;

  /**
   * if the grouperProvisioningGroup side is for a delete.  includes things that are known 
   * to be needed to be deleted.  This is used to retrieve the correct
   * incremental state from the target
   */
  private boolean delete;

  public boolean isRecalcObjectMemberships() {
    if (this instanceof ProvisioningStateGroup) {
      return ((ProvisioningStateGroup)this).isRecalcGroupMemberships();
    }
    if (this instanceof ProvisioningStateEntity) {
      return ((ProvisioningStateEntity)this).isRecalcEntityMemberships();
    }
    throw new RuntimeException("Not expecting type: " + this.getClass().getName());
  }

  /**
   * if the grouperProvisioningGroup side is for an update.  includes things that are known 
   * to be needed to be updated.  This is used to retrieve the correct
   * incremental state from the target
   */
  private boolean update;
  
  /**
   * if this insert was sent to target
   * @return
   */
  public boolean isInsertResultProcessed() {
    return insertResultProcessed;
  }

  /**
   * if this insert was sent to target
   * @param insertResultProcessed
   */
  public void setInsertResultProcessed(boolean insertResultProcessed) {
    this.insertResultProcessed = insertResultProcessed;
  }

  /**
   * if this update was sent to target
   */
  public boolean isUpdateResultProcessed() {
    return updateResultProcessed;
  }

  /**
   * if this update was sent to target
   * @param updateResultProcessed
   */
  public void setUpdateResultProcessed(boolean updateResultProcessed) {
    this.updateResultProcessed = updateResultProcessed;
  }

  /**
   * if this delete was sent to target
   * @return
   */
  public boolean isDeleteResultProcessed() {
    return deleteResultProcessed;
  }

  /**
   * if this delete was sent to target
   * @param deleteResultProcessed
   */
  public void setDeleteResultProcessed(boolean deleteResultProcessed) {
    this.deleteResultProcessed = deleteResultProcessed;
  }

  /**
   * if this is for a create in target
   * @return
   */
  public boolean isCreate() {
    return create;
  }

  /**
   * if the grouperProvisioningGroup side is for a delete.  includes things that are known 
   * to be needed to be deleted.  This is used to retrieve the correct
   * incremental state from the target
   * @return
   */
  public boolean isDelete() {
    return delete;
  }

  /**
   * if the grouperProvisioningGroup side is for an update.  includes things that are known 
   * to be needed to be updated.  This is used to retrieve the correct
   * incremental state from the target
   * @return
   */
  public boolean isUpdate() {
    return this.update;
  }

  /**
   * if this is for a create in target
   * @param create
   */
  public void setCreate(boolean create) {
    this.create = create;
  }

  /**
   * if the grouperProvisioningGroup side is for a delete.  includes things that are known 
   * to be needed to be deleted.  This is used to retrieve the correct
   * incremental state from the target
   * @param delete
   */
  public void setDelete(boolean delete) {
    this.delete = delete;
  }

  /**
   * if the grouperProvisioningGroup side is for an update.  includes things that are known 
   * to be needed to be updated.  This is used to retrieve the correct
   * incremental state from the target
   * @param update
   */
  public void setUpdate(boolean update) {
    this.update = update;
  }
  
  
}
