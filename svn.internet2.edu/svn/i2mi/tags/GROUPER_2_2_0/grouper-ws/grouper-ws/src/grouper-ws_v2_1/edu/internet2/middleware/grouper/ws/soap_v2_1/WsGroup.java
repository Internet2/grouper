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
 * Result of one group being retrieved since a user is a member of it.  The number of
 * groups will equal the number of groups the user is a member of (provided the filter matches)
 * 
 * @author mchyzer
 */
public class WsGroup {

  /** type of group can be an enum of TypeOfGroup, e.g. group, role, entity */
  private String typeOfGroup;
  
  /**
   * type of group can be an enum of TypeOfGroup, e.g. group, role, entity
   * @return type of group
   */
  public String getTypeOfGroup() {
    return this.typeOfGroup;
  }

  /**
   * type of group can be an enum of TypeOfGroup, e.g. group, role, entity
   * @param typeOfGroup1
   */
  public void setTypeOfGroup(String typeOfGroup1) {
    this.typeOfGroup = typeOfGroup1;
  }

  /** extension of group, the part to the right of last colon in name */
  private String extension;

  /** display extension, the part to the right of the last colon in display name */
  private String displayExtension;

  /**
   * friendly description of this group
   */
  private String description;

  /**
   * friendly extensions of group and parent stems
   */
  private String displayName;

  /**
   * Full name of the group (all extensions of parent stems, separated by colons,  and the extention of this group
   */
  private String name;

  /**
   * universally unique identifier of this group
   */
  private String uuid;

  /**
   * if requested, return the detail properties of the group
   */
  private WsGroupDetail detail;

  /**
   * no arg constructor
   */
  public WsGroup() {
    //blank

  }

  /**
   * friendly description of this group
   * @return the description
   */
  public String getDescription() {
    return this.description;
  }

  /**
   * friendly extensions of group and parent stems
   * @return the displayName
   */
  public String getDisplayName() {
    return this.displayName;
  }

  /**
   * Full name of the group (all extensions of parent stems, separated by colons, 
   * and the extention of this group
   * @return the name
   */
  public String getName() {
    return this.name;
  }

  /**
   * universally unique identifier of this group
   * @return the uuid
   */
  public String getUuid() {
    return this.uuid;
  }

  /**
   * friendly description of this group
   * @param description1 the description to set
   */
  public void setDescription(String description1) {
    this.description = description1;
  }

  /**
   * friendly extensions of group and parent stems
   * @param displayName1 the displayName to set
   */
  public void setDisplayName(String displayName1) {
    this.displayName = displayName1;
  }

  /**
   * Full name of the group (all extensions of parent stems, separated by colons, 
   * and the extention of this group
   * @param name1 the name to set
   */
  public void setName(String name1) {
    this.name = name1;
  }

  /**
   * universally unique identifier of this group
   * @param uuid1 the uuid to set
   */
  public void setUuid(String uuid1) {
    this.uuid = uuid1;
  }

  /**
   * if requested, these are the detail results of the group
   * @return the detail
   */
  public WsGroupDetail getDetail() {
    return this.detail;
  }

  /**
   * if requested, these are the detail results of the group
   * @param detail1 the detail to set
   */
  public void setDetail(WsGroupDetail detail1) {
    this.detail = detail1;
  }

  /**
   * extension of group, the part to the right of last colon in name
   * @return the extension
   */
  public String getExtension() {
    return this.extension;
  }

  /**
   * extension of group, the part to the right of last colon in name
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
