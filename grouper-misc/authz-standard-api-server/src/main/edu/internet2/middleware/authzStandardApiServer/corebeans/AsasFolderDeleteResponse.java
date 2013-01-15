/**
 * 
 */
package edu.internet2.middleware.authzStandardApiServer.corebeans;



/**
 * Delete a folder
 * @author mchyzer
 */
public class AsasFolderDeleteResponse extends AsasResponseBeanBase {
  
  /**
   * if this folder was deleted in this request
   */
  private Boolean deleted;
  
  /**
   * if the parent folder of this folder exists
   */
  private Boolean parentFolderExists;

  
  /**
   * if this folder was deleted in this request
   * @return the deleted
   */
  public Boolean getDeleted() {
    return deleted;
  }

  
  /**
   * if this folder was deleted in this request
   * @param deleted1 the deleted to set
   */
  public void setDeleted(Boolean deleted1) {
    this.deleted = deleted1;
  }

  
  /**
   * if the parent folder of this folder exists
   * @return the parentFolderExists
   */
  public Boolean getParentFolderExists() {
    return parentFolderExists;
  }

  
  /**
   * if the parent folder of this folder exists
   * @param parentFolderExists1 the parentFolderExists to set
   */
  public void setParentFolderExists(Boolean parentFolderExists1) {
    this.parentFolderExists = parentFolderExists1;
  }
  
  
}
