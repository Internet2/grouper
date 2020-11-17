package edu.internet2.middleware.grouper.app.provisioning.targetDao;


public class TargetDaoRetrieveAllEntitiesRequest {

  public TargetDaoRetrieveAllEntitiesRequest() {
  }

  /**
   * if memberships are part of entity (e.g. in attribute), then if true, get all memberships too, otherwise just get entity part
   */
  private boolean includeAllMembershipsIfApplicable;

  /**
   * if memberships are part of entity (e.g. in attribute), then if true, get all memberships too, otherwise just get entity part
   * @return
   */
  public boolean isIncludeAllMembershipsIfApplicable() {
    return includeAllMembershipsIfApplicable;
  }

  /**
   * if memberships are part of entity (e.g. in attribute), then if true, get all memberships too, otherwise just get entity part
   * @param includeAllMembershipsIfApplicable
   */
  public void setIncludeAllMembershipsIfApplicable(
      boolean includeAllMembershipsIfApplicable) {
    this.includeAllMembershipsIfApplicable = includeAllMembershipsIfApplicable;
  }


  public TargetDaoRetrieveAllEntitiesRequest(boolean includeAllMembershipsIfApplicable) {
    this.includeAllMembershipsIfApplicable = includeAllMembershipsIfApplicable;
  }
  
}
