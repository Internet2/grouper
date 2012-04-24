/*******************************************************************************
 * Copyright 2012 Internet2
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
 ******************************************************************************/
/**
 * 
 */
package edu.internet2.middleware.grouper.ws.soap_v2_1;



/**
 * <pre>
 * Class to save a group via web service
 * 
 * </pre>
 * 
 * @author mchyzer
 */
public class WsGroupToSave {

  /** stem lookup (blank if insert) */
  private WsGroupLookup wsGroupLookup;

  /** group to save */
  private WsGroup wsGroup;

  /** T or F (null if F) */
  private String createParentStemsIfNotExist;
  
  /**
   * if should create parent stems if not exist
   * @return T or F or null (F)
   */
  public String getCreateParentStemsIfNotExist() {
    return this.createParentStemsIfNotExist;
  }

  /**
   * if should create parent stems if not exist
   * @param createParentStemsIfNotExist1 T or F or null (F)
   */
  public void setCreateParentStemsIfNotExist(String createParentStemsIfNotExist1) {
    this.createParentStemsIfNotExist = createParentStemsIfNotExist1;
  }

  /** if the save should be constrained to INSERT, UPDATE, or INSERT_OR_UPDATE (default) */
  private String saveMode;

  /**
   * 
   */
  public WsGroupToSave() {
    // empty constructor
  }

  /**
   * if the save should be constrained to INSERT, UPDATE, or INSERT_OR_UPDATE (default)
   * @return the saveMode
   */
  public String getSaveMode() {
    return this.saveMode;
  }

  /**
   * if the save should be constrained to INSERT, UPDATE, or INSERT_OR_UPDATE (default)
   * @param saveMode1 the saveMode to set
   */
  public void setSaveMode(String saveMode1) {
    this.saveMode = saveMode1;
  }

  /**
   * @return the wsGroupLookup
   */
  public WsGroupLookup getWsGroupLookup() {
    return this.wsGroupLookup;
  }

  /**
   * @param wsGroupLookup1 the wsGroupLookup to set
   */
  public void setWsGroupLookup(WsGroupLookup wsGroupLookup1) {
    this.wsGroupLookup = wsGroupLookup1;
  }

  /**
   * @return the wsGroup
   */
  public WsGroup getWsGroup() {
    return this.wsGroup;
  }

  /**
   * @param wsGroup1 the wsGroup to set
   */
  public void setWsGroup(WsGroup wsGroup1) {
    this.wsGroup = wsGroup1;
  }
}
