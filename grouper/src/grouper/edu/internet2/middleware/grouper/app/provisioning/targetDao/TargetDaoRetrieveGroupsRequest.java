package edu.internet2.middleware.grouper.app.provisioning.targetDao;

import java.util.List;

import edu.internet2.middleware.grouper.app.provisioning.ProvisioningGroup;

public class TargetDaoRetrieveGroupsRequest {

  public TargetDaoRetrieveGroupsRequest() {
  }

  private List<ProvisioningGroup> targetGroups;

  
  public List<ProvisioningGroup> getTargetGroups() {
    return targetGroups;
  }

  
  public void setTargetGroups(List<ProvisioningGroup> targetGroups) {
    this.targetGroups = targetGroups;
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


  public TargetDaoRetrieveGroupsRequest(List<ProvisioningGroup> targetGroups,
      boolean includeAllMembershipsIfApplicable) {
    this.targetGroups = targetGroups;
    this.includeAllMembershipsIfApplicable = includeAllMembershipsIfApplicable;
  }

}
