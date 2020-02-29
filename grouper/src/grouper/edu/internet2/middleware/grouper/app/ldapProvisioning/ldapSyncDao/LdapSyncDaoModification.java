package edu.internet2.middleware.grouper.app.ldapProvisioning.ldapSyncDao;

import java.util.List;

import edu.internet2.middleware.grouper.app.ldapProvisioning.LdapSyncAttributeMetadata;

/**
 * add/remove/replace an attribute
 * @author mchyzer
 */
public class LdapSyncDaoModification {

  /**
   * attribute name
   */
  private LdapSyncAttributeMetadata ldapSyncAttributeMetadata;
  
  
  
  /**
   * attribute name
   * @return metadata
   */
  public LdapSyncAttributeMetadata getLdapSyncAttributeMetadata() {
    return this.ldapSyncAttributeMetadata;
  }

  /**
   * attribute name
   * @param ldapSyncAttributeMetadata1
   */
  public void setLdapSyncAttributeMetadata(
      LdapSyncAttributeMetadata ldapSyncAttributeMetadata1) {
    this.ldapSyncAttributeMetadata = ldapSyncAttributeMetadata1;
  }

  /**
   * 
   * @return attribute values
   */
  public List<Object> getAttributeValues() {
    return this.attributeValues;
  }

  /**
   * 
   * @param attributeValues
   */
  public void setAttributeValues(List<Object> attributeValues) {
    this.attributeValues = attributeValues;
  }

  
  public LdapSyncDaoModificationType getLdapSyncDaoModificationType() {
    return ldapSyncDaoModificationType;
  }

  
  public void setLdapSyncDaoModificationType(
      LdapSyncDaoModificationType ldapSyncDaoModificationType) {
    this.ldapSyncDaoModificationType = ldapSyncDaoModificationType;
  }
  
  /**
   * attribute values to add/remove/replace
   */
  private List<Object> attributeValues;
  
  /**
   * modification type
   */
  private LdapSyncDaoModificationType ldapSyncDaoModificationType;
}
