/*******************************************************************************
 * Copyright 2016 Internet2
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
/**
 * 
 */
package edu.internet2.middleware.tierApiAuthzServer.interfaces.beans.folders;


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
