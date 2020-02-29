package edu.internet2.middleware.grouper.app.ldapProvisioning.ldapSyncDao;

import java.util.List;

/**
 *  
 * @author mchyzer
 *
 */
public class LdapSyncDaoResult {

  /**
   * true if full success
   */
  private boolean success;
  
  /**
   * true if full success
   * @return if success
   */
  public boolean isSuccess() {
    return this.success;
  }

  /**
   * true if full success
   * @param success1
   */
  public void setSuccess(boolean success1) {
    this.success = success1;
  }

  /**
   * attribute errors
   */
  private List<LdapSyncDaoAttributeError> attributeErrors;


  /**
   * attribute errors
   * @return list of attribute errors
   */
  public List<LdapSyncDaoAttributeError> getAttributeErrors() {
    return this.attributeErrors;
  }

  /**
   * attribute errors
   * @param attributeErrors1
   */
  public void setAttributeErrors(List<LdapSyncDaoAttributeError> attributeErrors1) {
    this.attributeErrors = attributeErrors1;
  }
  
  
  
}
