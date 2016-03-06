/**
 * 
 */
package edu.internet2.middleware.tierApiAuthzServer.interfaces.beans.folders;



/**
 * Save a folder result
 * @author mchyzer
 *
 */
public class AsasApiFolderSaveResult {

  /**
   * set true if on insert, the object already exists
   */
  private Boolean insertAlreadyExists;
  
  /**
   * set true if on update, the object already exists
   */
  private Boolean updateDoesntExist;
  
  /**
   * set true if on insert, the object already exists
   * @return the insertAlreadyExists
   */
  public Boolean getInsertAlreadyExists() {
    return insertAlreadyExists;
  }
  
  /**
   * set true if on insert, the object already exists
   * @param insertAlreadyExists the insertAlreadyExists to set
   */
  public void setInsertAlreadyExists(Boolean insertAlreadyExists) {
    this.insertAlreadyExists = insertAlreadyExists;
  }
  
  /**
   * set true if on update, the object already exists
   * @return the updateDoesntExist
   */
  public Boolean getUpdateDoesntExist() {
    return updateDoesntExist;
  }
  
  /**
   * set true if on update, the object already exists
   * @param updateDoesntExist1 the updateDoesntExist to set
   */
  public void setUpdateDoesntExist(Boolean updateDoesntExist1) {
    this.updateDoesntExist = updateDoesntExist1;
  }

  /**
   * if this folder was created
   */
  private Boolean created;
  
  /**
   * if there was a problem and its because the parent folder didnt exist and createParentFoldersIfNotExist was not true
   */
  private Boolean parentFolderDoesntExist;
  
  
  /**
   * if there was a problem and its because the parent folder didnt exist and createParentFoldersIfNotExist was not true
   * @return the parentFolderDoesntExist
   */
  public Boolean getParentFolderDoesntExist() {
    return this.parentFolderDoesntExist;
  }

  
  /**
   * if there was a problem and its because the parent folder didnt exist and createParentFoldersIfNotExist was not true
   * @param parentFolderDoesntExist1 the parentFolderDoesntExist to set
   */
  public void setParentFolderDoesntExist(Boolean parentFolderDoesntExist1) {
    this.parentFolderDoesntExist = parentFolderDoesntExist1;
  }

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
  private AsasApiFolder folder = null;
  
  /**
   * @return the folder
   */
  public AsasApiFolder getFolder() {
    return this.folder;
  }

  /**
   * @param folder1 is the folder
   */
  public void setFolder(AsasApiFolder folder1) {
    this.folder = folder1;
  }
  
  
  
}
