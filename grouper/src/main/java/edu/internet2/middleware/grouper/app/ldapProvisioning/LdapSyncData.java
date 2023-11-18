package edu.internet2.middleware.grouper.app.ldapProvisioning;

/**
 * data retrieved from ldap
 * @author mchyzer
 *
 */
public class LdapSyncData {

  /**
   * reference back up to ldap sync
   */
  private LdapSync ldapSync;
  
  
  
  /**
   * reference back up to ldap sync
   * @return ldap sync
   */
  public LdapSync getLdapSync() {
    return this.ldapSync;
  }

  /**
   * reference back up to ldap sync
   * @param ldapSync1
   */
  public void setLdapSync(LdapSync ldapSync1) {
    this.ldapSync = ldapSync1;
  }

  /**
   * groups from ldap
   */
  private LdapSyncObjectContainer groups;


  
  /**
   * groups from ldap
   * @return
   */
  public LdapSyncObjectContainer getGroups() {
    return this.groups;
  }

  /**
   * groups from ldap
   * @param groups1
   */
  public void setGroups(LdapSyncObjectContainer groups1) {
    this.groups = groups1;
  }

  /**
   * users from ldap
   */
  private LdapSyncObjectContainer users;

  /**
   * users from ldap
   * @return users
   */
  public LdapSyncObjectContainer getUsers() {
    return this.users;
  }

  /**
   * users from ldap
   * @param users1
   */
  public void setUsers(LdapSyncObjectContainer users1) {
    this.users = users1;
  }
  
}
