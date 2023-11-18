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
 ******************************************************************************/
/**
 * 
 */
package edu.internet2.middleware.grouper.ws.soap_v2_4;


/**
 * Result of one external subject attribute being retrieved.
 * 
 * @author mchyzer
 */
public class WsExternalSubjectAttribute {

  /**
   * attribute value
   */
  private String attributeValue;
  
  
  /**
   * attribute value
   * @return the attributeValue
   */
  public String getAttributeValue() {
    return this.attributeValue;
  }

  
  /**
   * attribute value
   * @param attributeValue1 the attributeValue to set
   */
  public void setAttributeValue(String attributeValue1) {
    this.attributeValue = attributeValue1;
  }

  /**
   * Full name of the group (all extensions of parent stems, separated by colons,  and the extention of this group
   */
  private String attributeSystemName;

  /**
   * universally unique identifier of this group
   */
  private String uuid;

  /**
   * no arg constructor
   */
  public WsExternalSubjectAttribute() {
    //blank

  }

  /**
   * Full name of the group (all extensions of parent stems, separated by colons, 
   * and the extention of this group
   * @return the name
   */
  public String getAttributeSystemName() {
    return this.attributeSystemName;
  }

  /**
   * universally unique identifier of this group
   * @return the uuid
   */
  public String getUuid() {
    return this.uuid;
  }

  /**
   * Full name of the group (all extensions of parent stems, separated by colons, 
   * and the extention of this group
   * @param name1 the name to set
   */
  public void setAttributeSystemName(String name1) {
    this.attributeSystemName = name1;
  }

  /**
   * universally unique identifier of this group
   * @param uuid1 the uuid to set
   */
  public void setUuid(String uuid1) {
    this.uuid = uuid1;
  }
}
