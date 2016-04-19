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
 * Save a folder result
 * @author mchyzer
 *
 */
public class AsasApiFolderDeleteResult {
  
  /**
   * if this folder was deleted in this request, false for already deleted
   */
  private Boolean deleted;
  
  /**
   * if the parent folder of this object exists at the time of the request
   */
  private Boolean parentFolderExists;

  
  /**
   * if this folder was deleted in this request, false for already deleted
   * @return the deleted
   */
  public Boolean getDeleted() {
    return this.deleted;
  }

  
  /**
   * @param deleted1 the deleted to set
   */
  public void setDeleted(Boolean deleted1) {
    this.deleted = deleted1;
  }
  
  /**
   * if the parent folder of this object exists at the time of the request
   * @return the parentFolderExists
   */
  public Boolean getParentFolderExists() {
    return parentFolderExists;
  }
  
  /**
   * if the parent folder of this object exists at the time of the request
   * @param parentFolderExists1 the parentFolderExists to set
   */
  public void setParentFolderExists(Boolean parentFolderExists1) {
    this.parentFolderExists = parentFolderExists1;
  }
  
}
