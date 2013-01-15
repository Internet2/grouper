/**
 * 
 */
package edu.internet2.middleware.authzStandardApiServer.interfaces.beans.folders;



/**
 * Save a folder result
 * @author mchyzer
 *
 */
public class AsasApiFolderDeleteResult {
  
  /**
   * if this folder was deleted in this request, false for already deleted
   */
  private Boolean deleted;
  
  /**
   * if the parent folder of this object exists at the time of the request
   */
  private Boolean parentFolderExists;

  
  /**
   * if this folder was deleted in this request, false for already deleted
   * @return the deleted
   */
  public Boolean getDeleted() {
    return this.deleted;
  }

  
  /**
   * @param deleted1 the deleted to set
   */
  public void setDeleted(Boolean deleted1) {
    this.deleted = deleted1;
  }
  
  /**
   * if the parent folder of this object exists at the time of the request
   * @return the parentFolderExists
   */
  public Boolean getParentFolderExists() {
    return parentFolderExists;
  }
  
  /**
   * if the parent folder of this object exists at the time of the request
   * @param parentFolderExists1 the parentFolderExists to set
   */
  public void setParentFolderExists(Boolean parentFolderExists1) {
    this.parentFolderExists = parentFolderExists1;
  }
  
}
