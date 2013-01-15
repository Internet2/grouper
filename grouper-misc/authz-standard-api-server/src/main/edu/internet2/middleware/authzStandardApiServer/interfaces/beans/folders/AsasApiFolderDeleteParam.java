/**
 * 
 */
package edu.internet2.middleware.authzStandardApiServer.interfaces.beans.folders;


/**
 * Delete a folder request
 * @author mchyzer
 *
 */
public class AsasApiFolderDeleteParam {

  /**
   * lookup object (generally this is in the url)
   */
  private AsasApiFolderLookup folderLookup;
  
  /** if child objects should be deleted */
  private Boolean recursive;
  
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
   * if child objects should be deleted too
   * @return the recursive
   */
  public Boolean getRecursive() {
    return this.recursive;
  }
  
  /**
   * if child objects should be deleted too
   * @param recursive1 the recursive to set
   */
  public void setRecursive(Boolean recursive1) {
    this.recursive = recursive1;
  }
  
}
