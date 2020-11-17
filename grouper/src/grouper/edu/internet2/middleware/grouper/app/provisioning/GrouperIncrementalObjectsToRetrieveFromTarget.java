package edu.internet2.middleware.grouper.app.provisioning;

import java.util.List;

import edu.internet2.middleware.grouperClient.collections.MultiKey;

/**
 * groupertargetObjects to retrieve from target for incremental provisioning
 * @author mchyzer-local
 *
 */
public class GrouperIncrementalObjectsToRetrieveFromTarget {

  private GrouperProvisioner grouperProvisioner;

  
  public GrouperProvisioner getGrouperProvisioner() {
    return grouperProvisioner;
  }

  
  public void setGrouperProvisioner(GrouperProvisioner grouperProvisioner) {
    this.grouperProvisioner = grouperProvisioner;
  }
  
}
