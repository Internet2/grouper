/**
 * 
 */
package edu.internet2.middleware.authzStandardApiClient.corebeans;

/**
 * Delete a folder
 * @author mchyzer
 *
 */
public class AsacFolderDeleteResponse extends AsacResponseBeanBase {

  /**
   * true or false if the parent folder exists
   */
  private Boolean parentFolderExists;
  
  
  
  
  /**
   * true or false if the parent folder exists
   * @return the parentFolderExists
   */
  public Boolean getParentFolderExists() {
    return this.parentFolderExists;
  }

  
  /**
   * true or false if the parent folder exists
   * @param parentFolderExists1 the parentFolderExists to set
   */
  public void setParentFolderExists(Boolean parentFolderExists1) {
    this.parentFolderExists = parentFolderExists1;
  }

  /**
   * if this folder was deleted
   */
  private Boolean deleted;

  /**
   * if this folder was deleted
   * @return the deleted
   */
  public Boolean getDeleted() {
    return this.deleted;
  }

  /**
   * if this folder was deleted
   * @param deleted1 the deleted to set
   */
  public void setDeleted(Boolean deleted1) {
    this.deleted = deleted1;
  }

}
