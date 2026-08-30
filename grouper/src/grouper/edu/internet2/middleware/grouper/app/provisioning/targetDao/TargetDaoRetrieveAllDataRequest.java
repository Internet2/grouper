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
   * whether the DAO should retrieve entities (users) from the target in the "retrieve all" pass.
   * Always true on this branch: v6 has no path that asks a DAO to skip part of the target read.
   * Present so a target DAO written on a newer branch, where that path exists, compiles and behaves
   * correctly here without editing its guards. Intentionally read-only -- there is no setter,
   * so code that tries to turn a retrieval off will fail to compile rather than silently do nothing.
   * @return true, always
   */
  public boolean isRetrieveEntities() {
    return true;
  }

  /**
   * whether the DAO should retrieve memberships from the target in the "retrieve all" pass.
   * Always true on this branch -- see {@link #isRetrieveEntities()}.
   * @return true, always
   */
  public boolean isRetrieveMemberships() {
    return true;
  }

  /**
   * whether the DAO should retrieve groups from the target in the "retrieve all" pass.
   * Always true on this branch -- see {@link #isRetrieveEntities()}.
   * @return true, always
   */
  public boolean isRetrieveGroups() {
    return true;
  }

}
