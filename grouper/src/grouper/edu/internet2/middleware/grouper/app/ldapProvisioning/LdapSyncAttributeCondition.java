package edu.internet2.middleware.grouper.app.ldapProvisioning;

/**
 * special conditions of attributes
 * @author mchyzer
 */
public enum LdapSyncAttributeCondition {

  /**
   * attribute doesnt exist
   */
  ATTRIBUTE_NOT_EXIST,
  
  /**
   * attribute exists with no value
   */
  ATTRIBUTE_EXIST_WITH_NO_VALUE;
  
}
