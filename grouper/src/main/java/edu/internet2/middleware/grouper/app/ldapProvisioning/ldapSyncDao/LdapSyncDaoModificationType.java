package edu.internet2.middleware.grouper.app.ldapProvisioning.ldapSyncDao;


public enum LdapSyncDaoModificationType {

  /**
   * add an attribute
   */
  ADD,

  /** replace an attribute. */
  REPLACE,

  /** remove an attribute. */
  REMOVE;
}
