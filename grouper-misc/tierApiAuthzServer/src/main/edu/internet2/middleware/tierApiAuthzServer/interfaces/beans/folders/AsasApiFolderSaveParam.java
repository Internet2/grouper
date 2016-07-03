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

import edu.internet2.middleware.tierApiAuthzServer.interfaces.beans.AsasSaveMode;

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
