package edu.internet2.middleware.grouper.app.provisioning.targetDao;


public class TargetDaoRetrieveAllGroupsRequest {

  public TargetDaoRetrieveAllGroupsRequest() {
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

  public TargetDaoRetrieveAllGroupsRequest(boolean includeAllMembershipsIfApplicable) {
    this.includeAllMembershipsIfApplicable = includeAllMembershipsIfApplicable;
  }

}
