package edu.internet2.middleware.grouper.app.ldapProvisioning;

import edu.internet2.middleware.grouper.app.provisioning.ProvisioningGroup;
import edu.internet2.middleware.grouper.ldap.LdapEntry;


public class LdapTargetGroup extends ProvisioningGroup {

  private LdapEntry ldapEntry;

  
  public LdapEntry getLdapEntry() {
    return ldapEntry;
  }

  
  public void setLdapEntry(LdapEntry ldapEntry) {
    this.ldapEntry = ldapEntry;
  }
  
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
  public String getId() {
    return this.ldapEntry.getDn();
  }


  @Override
  public void setId(String id1) {
    throw new RuntimeException("Shouldnt set id");
  }
  
}
