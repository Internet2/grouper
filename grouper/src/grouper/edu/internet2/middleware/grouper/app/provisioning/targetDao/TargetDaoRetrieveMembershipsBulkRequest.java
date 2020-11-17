package edu.internet2.middleware.grouper.app.provisioning.targetDao;

import java.util.List;

import edu.internet2.middleware.grouper.app.provisioning.ProvisioningEntity;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningGroup;
import edu.internet2.middleware.grouperClient.collections.MultiKey;

public class TargetDaoRetrieveMembershipsBulkRequest {

  public TargetDaoRetrieveMembershipsBulkRequest() {
  }

  public TargetDaoRetrieveMembershipsBulkRequest(List<ProvisioningGroup> targetGroups,
      List<ProvisioningEntity> targetEntities,
      List<MultiKey> targetGroupsEntitiesMemberships) {
    super();
    this.targetGroups = targetGroups;
    this.targetEntities = targetEntities;
    this.targetGroupsEntitiesMemberships = targetGroupsEntitiesMemberships;
  }

  private List<ProvisioningGroup> targetGroups;
  
  private List<ProvisioningEntity> targetEntities;
  
  private List<MultiKey> targetGroupsEntitiesMemberships;

  
  public List<ProvisioningGroup> getTargetGroups() {
    return targetGroups;
  }

  
  public void setTargetGroups(List<ProvisioningGroup> targetGroups) {
    this.targetGroups = targetGroups;
  }

  
  public List<ProvisioningEntity> getTargetEntities() {
    return targetEntities;
  }

  
  public void setTargetEntities(List<ProvisioningEntity> targetEntities) {
    this.targetEntities = targetEntities;
  }

  
  public List<MultiKey> getTargetGroupsEntitiesMemberships() {
    return targetGroupsEntitiesMemberships;
  }

  
  public void setTargetGroupsEntitiesMemberships(
      List<MultiKey> targetGroupsEntitiesMemberships) {
    this.targetGroupsEntitiesMemberships = targetGroupsEntitiesMemberships;
  }


}
