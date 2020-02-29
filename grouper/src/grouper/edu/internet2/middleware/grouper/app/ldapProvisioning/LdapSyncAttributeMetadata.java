package edu.internet2.middleware.grouper.app.ldapProvisioning;


/**
 * information about an attribute in ldap
 * @author mchyzer
 *
 */
public class LdapSyncAttributeMetadata {

  /**
   * 
   */
  @Override
  public String toString() {
    return this.attributeName;
  }

  /**
   * metadata column index from zero
   */
  private int columnIndexFromZero;
  
  /**
   * metadata column index from zero
   * @return column index
   */
  public int getColumnIndexFromZero() {
    return columnIndexFromZero;
  }

  /**
   * metadata column index from zero
   * @param columnIndexFromZero1
   */
  public void setColumnIndexFromZero(int columnIndexFromZero1) {
    this.columnIndexFromZero = columnIndexFromZero1;
  }

  /**
   * name of attribute in ldap
   */
  private String attributeName;
  
  /**
   * name of attribute in ldap
   * @return attribute name
   */
  public String getAttributeName() {
    return this.attributeName;
  }

  /**
   * name of attribute in ldap
   * @param attributeName1
   */
  public void setAttributeName(String attributeName1) {
    this.attributeName = attributeName1;
  }

  /**
   * attribute type
   */
  private LdapSyncAttributeType ldapSyncAttributeType;

  /**
   * attribute type
   * @return attribute type
   */
  public LdapSyncAttributeType getLdapSyncAttributeType() {
    return this.ldapSyncAttributeType;
  }

  /**
   * attribute type
   * @param ldapSyncAttributeType1
   */
  public void setLdapSyncAttributeType(LdapSyncAttributeType ldapSyncAttributeType1) {
    this.ldapSyncAttributeType = ldapSyncAttributeType1;
  }
  
  
  
}
