/**
 * 
 */
package edu.internet2.middleware.authzStandardApiServer.interfaces.beans.folders;

import edu.internet2.middleware.authzStandardApiServer.interfaces.beans.AsasSaveMode;

/**
 * Save a folder request
 * @author mchyzer
 *
 */
public class AsasApiFolderSaveParam {

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
  
  /**
   * lookup object (generally this is in the url)
   */
  private AsasApiFolderLookup folderLookup;
  
  /** true or false (null if false) */
  private Boolean createParentFoldersIfNotExist;
  
  /** if the save should be constrained to INSERT, UPDATE, or INSERT_OR_UPDATE (default) */
  private AsasSaveMode saveMode;
  
  /**
   * lookup object (generally this is in the url)
   * @return the asasFolderLookup
   */
  public AsasApiFolderLookup getFolderLookup() {
    return this.folderLookup;
  }
  
  /**
   * lookup object (generally this is in the url)
   * @param asasFolderLookup1 the asasFolderLookup to set
   */
  public void setFolderLookup(AsasApiFolderLookup asasFolderLookup1) {
    this.folderLookup = asasFolderLookup1;
  }

  /**
   * if should create parent stems if not exist
   * @return true or false or null (false)
   */
  public Boolean getCreateParentFoldersIfNotExist() {
    return this.createParentFoldersIfNotExist;
  }

  /**
   * if the save should be constrained to INSERT, UPDATE, or INSERT_OR_UPDATE (default)
   * @return the saveMode
   */
  public AsasSaveMode getSaveMode() {
    return this.saveMode;
  }

  /**
   * if should create parent stems if not exist
   * @param createParentStemsIfNotExist1 true or false or null (false)
   */
  public void setCreateParentFoldersIfNotExist(Boolean createParentStemsIfNotExist1) {
    this.createParentFoldersIfNotExist = createParentStemsIfNotExist1;
  }

  /**
   * if the save should be constrained to INSERT, UPDATE, or INSERT_OR_UPDATE (default)
   * @param saveMode1 the saveMode to set
   */
  public void setSaveMode(AsasSaveMode saveMode1) {
    this.saveMode = saveMode1;
  }
  
}
