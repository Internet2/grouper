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
 * Result of one member being retrieved from a group.
 * 
 * @author mchyzer
 */
public class WsMembership {

  /** id of the membership */
  private String membershipId = null;

  /** list name of the membership */
  private String listName = null;

  /** list type of the membership */
  private String listType = null;

  /** membership type of the membership */
  private String membershipType = null;

  /** if the membership is enabled, T or F */
  private String enabled = null;

  /** timestamp this membership is enabled: yyyy/MM/dd HH:mm:ss.SSS */
  private String enabledTime = null;

  /** timestamp this membership is disabled: yyyy/MM/dd HH:mm:ss.SSS */
  private String disabledTime = null;

  /** member id of the member */
  private String memberId = null;
  
  /** group uuid of the group */
  private String groupId = null;
  
  /** uuid of the stem for stem privileges */
  private String ownerStemId = null;
  
  /** name of the stem for stem privileges */
  private String ownerStemName = null;
  
  /**
   * id of the attribute def for attribute def privileges
   */
  private String ownerIdOfAttributeDef = null;

  /**
   * name of the attribute def for attribute def privileges
   */
  private String ownerNameOfAttributeDef = null;

  
  
  /**
   * uuid of the stem for stem privileges
   * @return uuid
   */
  public String getOwnerStemId() {
    return this.ownerStemId;
  }

  /**
   * uuid of the stem for stem privileges
   * @param stemId1
   */
  public void setOwnerStemId(String stemId1) {
    this.ownerStemId = stemId1;
  }

  /**
   * name of the stem for stem privileges
   * @return the name
   */
  public String getOwnerStemName() {
    return this.ownerStemName;
  }

  /**
   * name of the stem for stem privileges
   * @param stemName1
   */
  public void setOwnerStemName(String stemName1) {
    this.ownerStemName = stemName1;
  }

  /**
   * id of the attr def for attr def privileges
   * @return id
   */
  public String getOwnerIdOfAttributeDef() {
    return this.ownerIdOfAttributeDef;
  }

  /**
   * id of the attr def for attr def privileges
   * @param idOfAttributeDef1
   */
  public void setOwnerIdOfAttributeDef(String idOfAttributeDef1) {
    this.ownerIdOfAttributeDef = idOfAttributeDef1;
  }

  /**
   * name of the attr def for attr def privileges
   * @return name
   */
  public String getOwnerNameOfAttributeDef() {
    return this.ownerNameOfAttributeDef;
  }

  /**
   * name of the attr def for attr def privileges
   * @param nameOfAttributeDef1
   */
  public void setOwnerNameOfAttributeDef(String nameOfAttributeDef1) {
    this.ownerNameOfAttributeDef = nameOfAttributeDef1;
  }

  /** subject id of the subject involved */
  private String subjectId = null;
  
  /** sourceId of the subject involved */
  private String subjectSourceId = null;
  
  /** groupName of the group involved */
  private String groupName = null;
  
  /**
   * member id of the subject involved
   * @return member id
   */
  public String getMemberId() {
    return this.memberId;
  }

  /**
   * member id of the subject involved
   * @param memberId1
   */
  public void setMemberId(String memberId1) {
    this.memberId = memberId1;
  }

  /**
   * group id of the group involved
   * @return the group id
   */
  public String getGroupId() {
    return this.groupId;
  }

  /**
   * group id of the group involved
   * @param groupUuid
   */
  public void setGroupId(String groupUuid) {
    this.groupId = groupUuid;
  }

  /**
   * subject id of the subject involved
   * @return the subject id
   */
  public String getSubjectId() {
    return this.subjectId;
  }

  /**
   * subject id of the subject involved
   * @param subjectId1
   */
  public void setSubjectId(String subjectId1) {
    this.subjectId = subjectId1;
  }

  /**
   * source if of the subject involved
   * @return the source id
   */
  public String getSubjectSourceId() {
    return this.subjectSourceId;
  }

  /**
   * source id of the subject involved
   * @param subjectSourceId1
   */
  public void setSubjectSourceId(String subjectSourceId1) {
    this.subjectSourceId = subjectSourceId1;
  }

  /**
   * name of the group involved
   * @return the group name
   */
  public String getGroupName() {
    return this.groupName;
  }

  /**
   * name of the group involved
   * @param groupName1
   */
  public void setGroupName(String groupName1) {
    this.groupName = groupName1;
  }

  /**
   * enabled time: yyyy/MM/dd HH:mm:ss.SSS
   * @return enabled time
   */
  public String getEnabledTime() {
    return this.enabledTime;
  }

  /**
   * enabled time: yyyy/MM/dd HH:mm:ss.SSS
   * @param enabledTime1
   */
  public void setEnabledTime(String enabledTime1) {
    this.enabledTime = enabledTime1;
  }

  /**
   * disabled time: yyyy/MM/dd HH:mm:ss.SSS
   * @return disabled time
   */
  public String getDisabledTime() {
    return this.disabledTime;
  }

  /**
   * abled time: yyyy/MM/dd HH:mm:ss.SSS
   * @param disabledTime1
   */
  public void setDisabledTime(String disabledTime1) {
    this.disabledTime = disabledTime1;
  }

  /**
   * enabled
   * @return if enabled T or F
   */
  public String getEnabled() {
    return this.enabled;
  }

  /**
   * if enabled T or F
   * @param enabled1
   */
  public void setEnabled(String enabled1) {
    this.enabled = enabled1;
  }

  /**
   * id of the membership
   * 
   * @return the membershipId
   */
  public String getMembershipId() {
    return this.membershipId;
  }

  /**
   * id of the membership
   * 
   * @param membershipId1
   *            the membershipId to set
   */
  public void setMembershipId(String membershipId1) {
    this.membershipId = membershipId1;
  }

  /**
   * no-arg constructor
   */
  public WsMembership() {
    // nothing
  }

  /** timestamp it was created: yyyy/MM/dd HH:mm:ss.SSS */
  private String createTime;

  /** immediate id of the membership */
  private String immediateMembershipId = null;

  /**
   * list name of the membership
   * 
   * @return the listName
   */
  public String getListName() {
    return this.listName;
  }

  /**
   * list name of the membership
   * 
   * @param listName1
   *            the listName to set
   */
  public void setListName(String listName1) {
    this.listName = listName1;
  }

  /**
   * list type of the membership
   * 
   * @return the listType
   */
  public String getListType() {
    return this.listType;
  }

  /**
   * list type of the membership
   * 
   * @param listType1
   *            the listType to set
   */
  public void setListType(String listType1) {
    this.listType = listType1;
  }

  /**
   * @return the membershipType
   */
  public String getMembershipType() {
    return this.membershipType;
  }

  /**
   * @param membershipType1
   *            the membershipType to set
   */
  public void setMembershipType(String membershipType1) {
    this.membershipType = membershipType1;
  }

  /**
   * timestamp it was created: yyyy/MM/dd HH:mm:ss.SSS
   * 
   * @return the createTime
   */
  public String getCreateTime() {
    return this.createTime;
  }

  /**
   * timestamp it was created: yyyy/MM/dd HH:mm:ss.SSS
   * 
   * @param createTime1
   *            the createTime to set
   */
  public void setCreateTime(String createTime1) {
    this.createTime = createTime1;
  }

  /**
   * immediate id of the membership
   * @return immediate id
   */
  public String getImmediateMembershipId() {
    return this.immediateMembershipId;
  }

  /**
   * immediate id of the membership
   * @param immediateMembershipId1
   */
  public void setImmediateMembershipId(String immediateMembershipId1) {
    this.immediateMembershipId = immediateMembershipId1;
  }

}
