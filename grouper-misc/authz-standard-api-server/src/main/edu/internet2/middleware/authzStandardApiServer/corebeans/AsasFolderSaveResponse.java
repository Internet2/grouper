/**
 * 
 */
package edu.internet2.middleware.authzStandardApiServer.corebeans;



/**
 * Save a folder
 * @author mchyzer
 *
 */
public class AsasFolderSaveResponse extends AsasResponseBeanBase {
  
  /**
   * if this folder was created
   */
  private Boolean created;
  
  /**
   * if this folder was updated
   */
  private Boolean updated;
  
  /**
   * if this folder was created
   * @return the created
   */
  public Boolean getCreated() {
    return this.created;
  }
  
  /**
   * if this folder was created
   * @param created1 the created to set
   */
  public void setCreated(Boolean created1) {
    this.created = created1;
  }
  
  /**
   * if this folder was updated
   * @return the updated
   */
  public Boolean getUpdated() {
    return this.updated;
  }
  
  /**
   * if this folder was updated
   * @param updated the updated to set
   */
  public void setUpdated(Boolean updated) {
    this.updated = updated;
  }

  /**
   * the saved folder
   */
  private AsasFolder folder = null;
  
  /**
   * @return the folder
   */
  public AsasFolder getFolder() {
    return this.folder;
  }

  /**
   * @param folder1 is the folder
   */
  public void setFolder(AsasFolder folder1) {
    this.folder = folder1;
  }
  
  
  
}
