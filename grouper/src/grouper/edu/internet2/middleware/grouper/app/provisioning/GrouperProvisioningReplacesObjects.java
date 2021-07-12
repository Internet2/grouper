package edu.internet2.middleware.grouper.app.provisioning;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.internet2.middleware.grouper.util.GrouperUtil;

public class GrouperProvisioningReplacesObjects {
  
  private Map<ProvisioningGroup, List<ProvisioningMembership>> provisioningMemberships = new HashMap<ProvisioningGroup, List<ProvisioningMembership>>();
  
  public Map<ProvisioningGroup, List<ProvisioningMembership>> getProvisioningMemberships() {
    return provisioningMemberships;
  }
  
  public void setProvisioningMemberships(
      Map<ProvisioningGroup, List<ProvisioningMembership>> provisioningMemberships) {
    this.provisioningMemberships = provisioningMemberships;
  }
  
  public boolean wasWorkDone() {

    for (List<ProvisioningMembership> provisioningMemberships : GrouperUtil.nonNull(this.provisioningMemberships.values())) {
      
      for (ProvisioningMembership provisioningMembership : GrouperUtil.nonNull(provisioningMemberships)) {
        if (GrouperUtil.booleanValue(provisioningMembership.getProvisioned(), false) && provisioningMembership.getException() == null) {
          return true;
        }
      }
      
    }
    
    return false;
  }
  

}
