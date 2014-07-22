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
 * 
 */
package edu.internet2.middleware.grouperClient.ws.beans;


/**
 * Result of one attribute def name being retrieved.  The number of
 * attribute def names will equal the number of attribute def names related to the result
 * 
 * @author mchyzer
 */
public class WsAttributeDefName {

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

  /** extension of attributeDefName, the part to the right of last colon in name */
  private String extension;

  /** display extension, the part to the right of the last colon in display name */
  private String displayExtension;

  /**
   * friendly description of this attributeDefName
   */
  private String description;

  /**
   * friendly extensions of attributeDefName and parent stems
   */
  private String displayName;

  /**
   * Full name of the attributeDefName (all extensions of parent stems, separated by colons,  and the extention of this attributeDefName
   */
  private String name;

  /**
   * universally unique identifier of this attributeDefName
   */
  private String uuid;

  /** id of the attribute definition */
  private String attributeDefId;

  /** name of the attribute definition */
  private String attributeDefName;

  
  /**
   * name of the attribute definition
   * @return name of attribute def
   */
  public String getAttributeDefName() {
    return this.attributeDefName;
  }

  /**
   * name of the attribute definition
   * @param attributeDefName1
   */
  public void setAttributeDefName(String attributeDefName1) {
    this.attributeDefName = attributeDefName1;
  }

  /**
   * id of the attribute definition
   * @return id of the attribute definition
   */
  public String getAttributeDefId() {
    return this.attributeDefId;
  }

  /**
   * id of the attribute definition
   * @param attributeDefId1
   */
  public void setAttributeDefId(String attributeDefId1) {
    this.attributeDefId = attributeDefId1;
  }

  /**
   * friendly description of this attributeDefName
   * @return the description
   */
  public String getDescription() {
    return this.description;
  }

  /**
   * friendly extensions of attributeDefName and parent stems
   * @return the displayName
   */
  public String getDisplayName() {
    return this.displayName;
  }

  /**
   * Full name of the attributeDefName (all extensions of parent stems, separated by colons, 
   * and the extention of this attributeDefName
   * @return the name
   */
  public String getName() {
    return this.name;
  }

  /**
   * universally unique identifier of this attributeDefName
   * @return the uuid
   */
  public String getUuid() {
    return this.uuid;
  }

  /**
   * friendly description of this attributeDefName
   * @param description1 the description to set
   */
  public void setDescription(String description1) {
    this.description = description1;
  }

  /**
   * friendly extensions of attributeDefName and parent stems
   * @param displayName1 the displayName to set
   */
  public void setDisplayName(String displayName1) {
    this.displayName = displayName1;
  }

  /**
   * Full name of the attributeDefName (all extensions of parent stems, separated by colons, 
   * and the extention of this attributeDefName
   * @param name1 the name to set
   */
  public void setName(String name1) {
    this.name = name1;
  }

  /**
   * universally unique identifier of this attributeDefName
   * @param uuid1 the uuid to set
   */
  public void setUuid(String uuid1) {
    this.uuid = uuid1;
  }

  /**
   * extension of attributeDefName, the part to the right of last colon in name
   * @return the extension
   */
  public String getExtension() {
    return this.extension;
  }

  /**
   * extension of attributeDefName, the part to the right of last colon in name
   * @param extension1 the extension to set
   */
  public void setExtension(String extension1) {
    this.extension = extension1;
  }

  /**
   * display extension, the part to the right of the last colon in display name
   * @return the displayExtension
   */
  public String getDisplayExtension() {
    return this.displayExtension;
  }

  /**
   * display extension, the part to the right of the last colon in display name
   * @param displayExtension1 the displayExtension to set
   */
  public void setDisplayExtension(String displayExtension1) {
    this.displayExtension = displayExtension1;
  }
}
