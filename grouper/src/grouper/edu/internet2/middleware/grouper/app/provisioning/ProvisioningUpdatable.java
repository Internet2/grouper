package edu.internet2.middleware.grouper.app.provisioning;

import java.util.Map;

import edu.internet2.middleware.grouperClient.collections.MultiKey;

public interface ProvisioningUpdatable {

  public Map<MultiKey, Object> getInternal_fieldsToUpdate();
  
  public void setInternal_fieldsToUpdate(Map<MultiKey, Object> internal_fieldsToUpdate);
  
}
