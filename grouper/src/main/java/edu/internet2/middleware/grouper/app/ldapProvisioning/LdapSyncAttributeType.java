package edu.internet2.middleware.grouper.app.ldapProvisioning;


public enum LdapSyncAttributeType {

  /**
   * object is a Boolean
   */
  BOOLEAN,

  /**
   * object is a string
   */
  STRING,
  
  /**
   * object is numeric, i.e. BigDecimal
   */
  BIG_DECIMAL,
  
  /**
   * object is an array of unordered strings
   */
  STRING_ARRAY,
  
  /**
   * object is a timestamp
   */
  TIMESTAMP;
  
}
