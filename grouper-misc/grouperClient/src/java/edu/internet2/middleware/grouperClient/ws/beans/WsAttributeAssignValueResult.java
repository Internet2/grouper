/**
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
 */
/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperClient.ws.beans;


/**
 * holds an attribute assign result.  Also holds value results (if value operations were performed).
 * note if attribute assignments have values and the attribute is removed, the values will not be in 
 * this result
 */
public class WsAttributeAssignValueResult {

  /** if this assignment was changed, T|F */
  private String changed;

  /** if this assignment was deleted, T|F */
  private String deleted;

  /**
   * if this assignment was deleted, T|F
   * @return if this assignment was deleted, T|F
   */
  public String getDeleted() {
    return this.deleted;
  }

  /**
   * if this assignment was deleted, T|F
   * @param deleted1
   */
  public void setDeleted(String deleted1) {
    this.deleted = deleted1;
  }

  /**
   * if this assignment was changed, T|F
   * @return if changed
   */
  public String getChanged() {
    return this.changed;
  }

  /**
   * if this assignment was changed, T|F
   * @param changed1
   */
  public void setChanged(String changed1) {
    this.changed = changed1;
  }

  /** value of this result */
  private WsAttributeAssignValue wsAttributeAssignValue;

  /**
   * value of this result
   * @return value of this result
   */
  public WsAttributeAssignValue getWsAttributeAssignValue() {
    return this.wsAttributeAssignValue;
  }

  /**
   * value of this result
   * @param wsAttributeAssignValue1
   */
  public void setWsAttributeAssignValue(WsAttributeAssignValue wsAttributeAssignValue1) {
    this.wsAttributeAssignValue = wsAttributeAssignValue1;
  }  
}
