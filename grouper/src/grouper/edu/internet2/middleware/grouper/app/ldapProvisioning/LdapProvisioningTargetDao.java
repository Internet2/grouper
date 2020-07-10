package edu.internet2.middleware.grouper.app.ldapProvisioning;

import java.util.Map;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisionerTargetDaoBase;
import edu.internet2.middleware.grouper.app.provisioning.TargetGroup;


public class LdapProvisioningTargetDao extends GrouperProvisionerTargetDaoBase {

  /**
   * reference back to provisioner, must be set whenn created
   */
  private LdapSync ldapSync;


  /**
   * reference back to provisioner, must be set whenn created
   * @return
   */
  public LdapSync getLdapSync() {
    return ldapSync;
  }


  /**
   * reference back to provisioner, must be set whenn created
   * @param ldapSync
   */
  public void setLdapSync(LdapSync ldapSync) {
    this.ldapSync = ldapSync;
  }

  
  @Override
  public Map<String, TargetGroup> retrieveAllGroups() {
    
    // get configuration
    // get ldap config id
    // construct "all groups" filter
    // take resulting ldapentrys and make ldap target entries
    //TODO
    return null;
  }
  
  
}
