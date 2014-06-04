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
package edu.internet2.middleware.grouperClient.ws.beans;


/**
 * <pre>
 * Class to lookup a stem via web service
 * 
 * </pre>
 * @author mchyzer
 */
public class WsStemLookup {

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
   * uuid of the stem to find
   */
  private String uuid;

  /** name of the stem to find (includes stems, e.g. stem1:stem2:stemName */
  private String stemName;

  /**
   * uuid of the stem to find
   * @return the uuid
   */
  public String getUuid() {
    return this.uuid;
  }

  /**
   * uuid of the stem to find
   * @param uuid1 the uuid to set
   */
  public void setUuid(String uuid1) {
    this.uuid = uuid1;
  }

  /**
   * name of the stem to find (includes stems, e.g. stem1:stem2:stemName
   * @return the theName
   */
  public String getStemName() {
    return this.stemName;
  }

  /**
   * name of the stem to find (includes stems, e.g. stem1:stem2:stemName
   * @param theName the theName to set
   */
  public void setStemName(String theName) {
    this.stemName = theName;
  }

  /**
   * 
   */
  public WsStemLookup() {
    //blank
  }

  /**
   * construct with fields
   * @param theStemName
   * @param stemUuid
   */
  public WsStemLookup(String theStemName, String stemUuid) {
    this.stemName = theStemName;
    this.uuid = stemUuid;
  }

  /**
   * construct with fields
   * @param theIdIndex
   * @param theStemName
   * @param stemUuid
   */
  public WsStemLookup(String theStemName, String stemUuid, String theIdIndex) {
    this.stemName = theStemName;
    this.uuid = stemUuid;
    this.idIndex = theIdIndex;
  }
}
