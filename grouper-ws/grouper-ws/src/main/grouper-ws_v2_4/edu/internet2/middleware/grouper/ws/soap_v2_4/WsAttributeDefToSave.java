/*******************************************************************************
 * Copyright 2016 Internet2
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under
 * the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 ******************************************************************************/
/**
 * 
 */
package edu.internet2.middleware.grouper.ws.soap_v2_4;

import com.thoughtworks.xstream.annotations.XStreamOmitField;

import edu.internet2.middleware.grouper.misc.SaveResultType;

/**
 * <pre>
 * Class to save an attribute def via web service
 * 
 * </pre>
 * 
 * @author vsachdeva
 */
public class WsAttributeDefToSave {

  /** attribute def lookup (blank if insert) */
  private WsAttributeDefLookup wsAttributeDefLookup;

  /** attribute def to save */
  private WsAttributeDef wsAttributeDef;

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
   * what ended up happening
   */
  @XStreamOmitField
  private SaveResultType saveResultType;

  /**
   * 
   */
  public WsAttributeDefToSave() {
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
   * @return the wsAttributeDefLookup
   */
  public WsAttributeDefLookup getWsAttributeDefLookup() {
    return this.wsAttributeDefLookup;
  }

  /**
   * @param wsAttributeDefLookup1 the wsAttributeDefLookup to set
   */
  public void setWsAttributeDefLookup(WsAttributeDefLookup wsAttributeDefLookup1) {
    this.wsAttributeDefLookup = wsAttributeDefLookup1;
  }

  /**
   * @return the wsAttributeDef
   */
  public WsAttributeDef getWsAttributeDef() {
    return this.wsAttributeDef;
  }

  /**
   * @param wsAttributeDef1 the wsAttributeDef to set
   */
  public void setWsAttributeDef(WsAttributeDef wsAttributeDef1) {
    this.wsAttributeDef = wsAttributeDef1;
  }
}
