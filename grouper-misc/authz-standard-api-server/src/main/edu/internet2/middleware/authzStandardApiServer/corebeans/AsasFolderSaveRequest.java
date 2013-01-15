/**
 * 
 */
package edu.internet2.middleware.authzStandardApiServer.corebeans;

/**
 * Save a folder request
 * @author mchyzer
 *
 */
public class AsasFolderSaveRequest extends AsasResponseBeanBase {

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
  
  /**
   * lookup object (generally this is in the url)
   */
  private AsasFolderLookup folderLookup;
  
  /** true or false (null if false) */
  private Boolean createParentFoldersIfNotExist;
  
  /** if the save should be constrained to INSERT, UPDATE, or INSERT_OR_UPDATE (default) */
  private String saveMode;
  
  /**
   * lookup object (generally this is in the url)
   * @return the asasFolderLookup
   */
  public AsasFolderLookup getFolderLookup() {
    return this.folderLookup;
  }
  
  /**
   * lookup object (generally this is in the url)
   * @param asasFolderLookup1 the asasFolderLookup to set
   */
  public void setFolderLookup(AsasFolderLookup asasFolderLookup1) {
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
