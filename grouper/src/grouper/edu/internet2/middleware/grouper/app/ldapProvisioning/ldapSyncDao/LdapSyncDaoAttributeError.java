package edu.internet2.middleware.grouper.app.ldapProvisioning.ldapSyncDao;

import edu.internet2.middleware.grouper.app.ldapProvisioning.LdapSyncAttributeMetadata;

/**
 * error on attribute modification
 * @author mchyzer
 *
 */
public class LdapSyncDaoAttributeError {

  /**
   * attribute name with error if applicable
   */
  private LdapSyncAttributeMetadata ldapSyncAttributeMetadata;
  

  /**
   * attribute name with error if applicable 
   * @return metadata
   */
  public LdapSyncAttributeMetadata getLdapSyncAttributeMetadata() {
    return this.ldapSyncAttributeMetadata;
  }

  /**
   * attribute name with error if applicable
   * @param ldapSyncAttributeMetadata1
   */
  public void setLdapSyncAttributeMetadata(
      LdapSyncAttributeMetadata ldapSyncAttributeMetadata1) {
    this.ldapSyncAttributeMetadata = ldapSyncAttributeMetadata1;
  }

  /**
   * attribute value with error if applicable
   * @return attribute value
   */
  public Object getAttributeValue() {
    return this.attributeValue;
  }

  /**
   * attribute value with error if applicable
   * @param attributeValue1
   */
  public void setAttributeValue(Object attributeValue1) {
    this.attributeValue = attributeValue1;
  }
  
  /**
   * type that had issue
   */
  private LdapSyncDaoModificationType ldapSyncDaoModificationType;
  
  
  
  /**
   * type that had issue
   * @return type
   */
  public LdapSyncDaoModificationType getLdapSyncDaoModificationType() {
    return ldapSyncDaoModificationType;
  }

  /**
   * type that had issue
   * @param ldapSyncDaoModificationType1
   */
  public void setLdapSyncDaoModificationType(
      LdapSyncDaoModificationType ldapSyncDaoModificationType1) {
    this.ldapSyncDaoModificationType = ldapSyncDaoModificationType1;
  }

  /**
   * error code if applicable
   * @return error code
   */
  public String getErrorCode() {
    return this.errorCode;
  }

  /**
   * error code if applicable
   * @param errorCode1
   */
  public void setErrorCode(String errorCode1) {
    this.errorCode = errorCode1;
  }

  /**
   * error message including stack if applicable
   * @return error message
   */
  public String getErrorMessage() {
    return this.errorMessage;
  }

  /**
   * error message including stack if applicable
   * @param errorMessage1
   */
  public void setErrorMessage(String errorMessage1) {
    this.errorMessage = errorMessage1;
  }

  /**
   * attribute value with error if applicable
   */
  private Object attributeValue;
  
  /**
   * error code if applicable
   */
  private String errorCode;
  
  /**
   * error message including exception stack if applicable
   */
  private String errorMessage;
  
  
}
