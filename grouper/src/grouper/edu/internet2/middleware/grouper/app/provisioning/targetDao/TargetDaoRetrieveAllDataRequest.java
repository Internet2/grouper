package edu.internet2.middleware.grouper.app.provisioning.targetDao;


public class TargetDaoRetrieveAllDataRequest {

  private boolean includeNativeEntity;

  public boolean isIncludeNativeEntity() {
    return includeNativeEntity;
  }

  public void setIncludeNativeEntity(boolean includeNativeEntity) {
    this.includeNativeEntity = includeNativeEntity;
  }

  /**
   * GRP-7048: whether the DAO should retrieve entities (users) from the target as part of the
   * "retrieve all" pass. Defaults to true. When {@code fullSyncUsersFromSyncBack} is enabled the
   * framework sets this to false so a DAO that supports it skips its (potentially very large) user
   * pull -- and any per-membership missing-user lookup -- and the framework instead seeds the
   * target users from the sync-back cache. Groups and memberships are still retrieved normally.
   */
  private boolean retrieveEntities = true;

  public boolean isRetrieveEntities() {
    return retrieveEntities;
  }

  public void setRetrieveEntities(boolean retrieveEntities) {
    this.retrieveEntities = retrieveEntities;
  }

}
