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
 * Save a folder
 * @author mchyzer
 *
 */
public class AsasFolderSaveResponse extends AsasResponseBeanBase {
  
  /**
   * if this folder was created
   */
  private Boolean created;
  
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
  
  
  
}
