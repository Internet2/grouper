/**
 * Copyright 2014 Internet2
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
 * represents a limit on a permission.  Note this is an attribute assignment
 * you can link to the attribute assignment by the attribute assign id
 */
public class WsPermissionLimit {

  /** value(s) in this assignment if any */
  private WsAttributeAssignValue[] wsAttributeAssignValues;
  
  /** if this is an attribute assign attribute, this is the foreign key */
  private String attributeAssignId;
  
  /** attribute name id in this assignment */
  private String attributeDefNameId;
  
  /** attribute name in this assignment */
  private String attributeDefNameName;

  /**
   * value(s) in this assignment if any
   * @return values
   */
  public WsAttributeAssignValue[] getWsAttributeAssignValues() {
    return this.wsAttributeAssignValues;
  }

  /**
   * value(s) in this assignment if any
   * @param wsAttributeAssignValues1
   */
  public void setWsAttributeAssignValues(WsAttributeAssignValue[] wsAttributeAssignValues1) {
    this.wsAttributeAssignValues = wsAttributeAssignValues1;
  }

  /**
   * if this is an attribute assign attribute, this is the foreign key
   * @return attribute assign id
   */
  public String getAttributeAssignId() {
    return this.attributeAssignId;
  }

  /**
   * attribute name id in this assignment
   * @return attribute name id in this assignment
   */
  public String getAttributeDefNameId() {
    return this.attributeDefNameId;
  }

  /**
   * attribute name in this assignment
   * @return attribute name in this assignment
   */
  public String getAttributeDefNameName() {
    return this.attributeDefNameName;
  }

  /**
   * if this is an attribute assign attribute, this is the foreign key
   * @param ownerAttributeAssignId1
   */
  public void setAttributeAssignId(String ownerAttributeAssignId1) {
    this.attributeAssignId = ownerAttributeAssignId1;
  }

  /**
   * attribute name id in this assignment
   * @param attributeDefNameId1
   */
  public void setAttributeDefNameId(String attributeDefNameId1) {
    this.attributeDefNameId = attributeDefNameId1;
  }

  /**
   * attribute name in this assignment
   * @param attributeDefNameName1
   */
  public void setAttributeDefNameName(String attributeDefNameName1) {
    this.attributeDefNameName = attributeDefNameName1;
  }
}
