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
