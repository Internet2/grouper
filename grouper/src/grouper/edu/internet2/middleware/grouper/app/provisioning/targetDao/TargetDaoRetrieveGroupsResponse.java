package edu.internet2.middleware.grouper.app.provisioning.targetDao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.internet2.middleware.grouper.app.provisioning.ProvisioningGroup;

public class TargetDaoRetrieveGroupsResponse {

  public TargetDaoRetrieveGroupsResponse() {
  }

  /**
   * map of retrieved group to target native group, optional, only if the target native group is needed later on
   */
  private Map<ProvisioningGroup, Object> targetGroupToTargetNativeGroup = new HashMap<ProvisioningGroup, Object>();
  
  /**
   * map of retrieved group to target native group, optional, only if the target native group is needed later on
   * @return
   */
  public Map<ProvisioningGroup, Object> getTargetGroupToTargetNativeGroup() {
    return targetGroupToTargetNativeGroup;
  }

  /**
   * map of retrieved group to target native group, optional, only if the target native group is needed later on
   * @param targetGroupToTargetNativeGroup
   */
  public void setTargetGroupToTargetNativeGroup(Map<ProvisioningGroup, Object> targetGroupToTargetNativeGroup) {
    this.targetGroupToTargetNativeGroup = targetGroupToTargetNativeGroup;
  }

  private List<ProvisioningGroup> targetGroups;

  
  public List<ProvisioningGroup> getTargetGroups() {
    return targetGroups;
  }

  
  public void setTargetGroups(List<ProvisioningGroup> targetGroups) {
    this.targetGroups = targetGroups;
  }


  public TargetDaoRetrieveGroupsResponse(List<ProvisioningGroup> targetGroups) {
    super();
    this.targetGroups = targetGroups;
  }
  
  
}
