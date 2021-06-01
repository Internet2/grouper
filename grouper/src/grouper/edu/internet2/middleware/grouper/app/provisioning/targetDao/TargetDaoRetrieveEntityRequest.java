package edu.internet2.middleware.grouper.app.provisioning.targetDao;

import edu.internet2.middleware.grouper.app.provisioning.ProvisioningEntity;

public class TargetDaoRetrieveEntityRequest {

  public TargetDaoRetrieveEntityRequest() {
  }

  private ProvisioningEntity targetEntity;

  public ProvisioningEntity getTargetEntity() {
    return targetEntity;
  }

  
  public void setTargetEntity(ProvisioningEntity targetEntity) {
    this.targetEntity = targetEntity;
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


  public TargetDaoRetrieveEntityRequest(ProvisioningEntity targetEntity,
      boolean includeAllMembershipsIfApplicable) {
    super();
    this.targetEntity = targetEntity;
    this.includeAllMembershipsIfApplicable = includeAllMembershipsIfApplicable;
  }

  
}
