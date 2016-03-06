/**
 * 
 */
package edu.internet2.middleware.tierApiAuthzClient.corebeans;

/**
 * Save a folder request
 * @author mchyzer
 *
 */
public class AsacFolderSaveRequest {

  /**
   * the saved folder
   */
  private AsacFolder folder = null;
  
  /**
   * @return the folder
   */
  public AsacFolder getFolder() {
    return this.folder;
  }
  
  /**
   * @param folder1 is the folder
   */
  public void setFolder(AsacFolder folder1) {
    this.folder = folder1;
  }
  
  /**
   * lookup object (generally this is in the url)
   */
  private AsacFolderLookup folderLookup;
  
  /** true or false (null if false) */
  private Boolean createParentFoldersIfNotExist;
  
  /** if the save should be constrained to INSERT, UPDATE, or INSERT_OR_UPDATE (default) */
  private String saveMode;
  
  /**
   * lookup object (generally this is in the url)
   * @return the asasFolderLookup
   */
  public AsacFolderLookup getFolderLookup() {
    return this.folderLookup;
  }
  
  /**
   * lookup object (generally this is in the url)
   * @param asasFolderLookup1 the asasFolderLookup to set
   */
  public void setFolderLookup(AsacFolderLookup asasFolderLookup1) {
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
  public String getSaveMode() {
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
  public void setSaveMode(String saveMode1) {
    this.saveMode = saveMode1;
  }
  
}
