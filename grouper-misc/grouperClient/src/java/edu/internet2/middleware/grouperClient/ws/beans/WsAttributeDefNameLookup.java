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
 * 
 */
package edu.internet2.middleware.grouperClient.ws.beans;


/**
 * <pre>
 * Class to lookup an attribute def name via web service
 * 
 * </pre>
 * @author mchyzer
 */
public class WsAttributeDefNameLookup {

  /**
   * integer ID for object
   */
  private String idIndex;
  
  /**
   * integer ID for object
   * @return the id
   */
  public String getIdIndex() {
    return this.idIndex;
  }

  /**
   * integer ID for object
   * @param idIndex1
   */
  public void setIdIndex(String idIndex1) {
    this.idIndex = idIndex1;
  }

  /**
   * 
   */
  public WsAttributeDefNameLookup() {
    super();
  }

  /**
   * @param uuid1
   * @param name1
   */
  public WsAttributeDefNameLookup(String name1, String uuid1) {
    super();
    this.uuid = uuid1;
    this.name = name1;
  }

  /**
   * @param name1
   * @param uuid1
   * @param idIndex1
   */
  public WsAttributeDefNameLookup(String name1, String uuid1, String idIndex1) {
    super();
    this.idIndex = idIndex1;
    this.uuid = uuid1;
    this.name = name1;
  }

  /**
   * uuid of the attributeDefName to find
   */
  private String uuid;

  /** name of the attributeDefName to find (includes stems, e.g. stem1:stem2:attributeDefNameName */
  private String name;

  /**
   * uuid of the attributeDefName to find
   * @return the uuid
   */
  public String getUuid() {
    return this.uuid;
  }

  /**
   * uuid of the attributeDefName to find
   * @param uuid1 the uuid to set
   */
  public void setUuid(String uuid1) {
    this.uuid = uuid1;
  }

  /**
   * name of the attributeDefName to find (includes stems, e.g. stem1:stem2:attributeDefNameName
   * @return the theName
   */
  public String getName() {
    return this.name;
  }

  /**
   * name of the attributeDefName to find (includes stems, e.g. stem1:stem2:attributeDefNameName
   * @param theName the theName to set
   */
  public void setName(String theName) {
    this.name = theName;
  }

}
