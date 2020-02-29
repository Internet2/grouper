package edu.internet2.middleware.grouper.app.ldapProvisioning;

/**
 * one ldap group or user
 * @author mchyzer
 *
 */
public class LdapSyncObject {

  
  /**
   * link back up to container
   */
  private LdapSyncObjectContainer ldapSyncObjectContainer;
  
  /**
   * link back up to container
   * @return
   */
  public LdapSyncObjectContainer getLdapSyncObjectContainer() {
    return ldapSyncObjectContainer;
  }

  /**
   * link back up to container
   * @param ldapSyncObjectContainer1
   */
  public void setLdapSyncObjectContainer(LdapSyncObjectContainer ldapSyncObjectContainer1) {
    this.ldapSyncObjectContainer = ldapSyncObjectContainer1;
  }


  /**
   * dn of object
   */
  private String dn;
  
  /**
   * dn of object
   * @return
   */
  public String getDn() {
    return this.dn;
  }

  /**
   * dn of object
   * @param dn1
   */
  public void setDn(String dn1) {
    this.dn = dn1;
  }
  

  /**
   * attribute values.  these are in the same order as the schema dictate, and are the same type as the schema dictates
   * note: value could be: LdapSyncAttributeCondition.ATTRIBUTE_NOT_EXIST, or LdapSyncAttributeCondition.ATTRIBUTE_EXIST_WITH_NO_VALUE
   */
  private Object[] attributeValues;
  

  /**
   * non membership attributes.  these are in the same order as the schema dictate, and are the same type as the schema dictates
   * note: value could be: LdapSyncAttributeCondition.ATTRIBUTE_NOT_EXIST, or LdapSyncAttributeCondition.ATTRIBUTE_EXIST_WITH_NO_VALUE
   * @return attributes
   */
  public Object[] getAttributes() {
    return this.attributeValues;
  }

  /**
   * non membership attributes.  these are in the same order as the schema dictate, and are the same type as the schema dictates
   * note: value could be: LdapSyncAttributeCondition.ATTRIBUTE_NOT_EXIST, or LdapSyncAttributeCondition.ATTRIBUTE_EXIST_WITH_NO_VALUE
   * @param attributes1
   */
  public void setAttributes(Object[] attributes1) {
    this.attributeValues = attributes1;
  }
  
  
}
