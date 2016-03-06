/**
 * 
 */
package edu.internet2.middleware.tierApiAuthzServer.corebeans;

/**
 * Save a folder request
 * @author mchyzer
 *
 */
public class AsasFolderDeleteRequest {

  /**
   * lookup object (generally this is in the url)
   */
  private AsasFolderLookup folderLookup;
  
  /** true or false (null if false) if delete subobjects too */
  private Boolean recursive;
  
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
   * true or false (null if false) if delete subobjects too
   * @return the recursive
   */
  public Boolean getRecursive() {
    return this.recursive;
  }

  
  /**
   * true or false (null if false) if delete subobjects too
   * @param recursive1 the recursive to set
   */
  public void setRecursive(Boolean recursive1) {
    this.recursive = recursive1;
  }

  
}
