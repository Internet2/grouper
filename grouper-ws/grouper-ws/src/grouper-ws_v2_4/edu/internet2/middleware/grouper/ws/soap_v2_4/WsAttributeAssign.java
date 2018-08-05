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
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.ws.soap_v2_4;




/**
 * result of attribute assign query represents an assignment in the DB
 */
public class WsAttributeAssign {

  /** type of assignment from enum AttributeAssignActionType e.g. effective, immediate */
  private String attributeAssignActionType;
  
  /** AttributeAssignDelegatable enum (generally only for permissions): TRUE, FALSE, GRANT */
  private String attributeAssignDelegatable;
  
  /**
   * AttributeAssignDelegatable enum (generally only for permissions): TRUE, FALSE, GRANT
   * @return delegatable
   */
  public String getAttributeAssignDelegatable() {
    return this.attributeAssignDelegatable;
  }


  /**
   * AttributeAssignDelegatable enum (generally only for permissions): TRUE, FALSE, GRANT
   * @param attributeAssignDelegatable1
   */
  public void setAttributeAssignDelegatable(String attributeAssignDelegatable1) {
    this.attributeAssignDelegatable = attributeAssignDelegatable1;
  }


  /**
   * type of assignment from enum AttributeAssignActionType e.g. effective, immediate
   * @return type of assignment from enum AttributeAssignActionType e.g. effective, immediate
   */
  public String getAttributeAssignActionType() {
    return this.attributeAssignActionType;
  }


  /**
   * type of assignment from enum AttributeAssignActionType e.g. effective, immediate
   * @param attributeAssignActionType1 type of assignment from enum AttributeAssignActionType e.g. effective, immediate
   */
  public void setAttributeAssignActionType(String attributeAssignActionType1) {
    this.attributeAssignActionType = attributeAssignActionType1;
  }


  /**
   * id of action for this assignment (e.g. assign).  Generally this will be AttributeDef.ACTION_DEFAULT
   */
  private String attributeAssignActionId;

  /**
   * name of action for this assignment (e.g. assign).  Generally this will be AttributeDef.ACTION_DEFAULT
   */
  private String attributeAssignActionName;

  /** 
   * Type of owner, from enum AttributeAssignType, e.g.
   * group, member, stem, any_mem, imm_mem, attr_def, group_asgn, mem_asgn, 
   * stem_asgn, any_mem_asgn, imm_mem_asgn, attr_def_asgn  
   */
  private String attributeAssignType;

  /** attribute name id in this assignment */
  private String attributeDefNameId;
  
  /** attribute name in this assignment */
  private String attributeDefNameName;

  /** id of attribute def in this assignment */
  private String attributeDefId;
  
  /** name of attribute def in this assignment */
  private String attributeDefName;

  /** value(s) in this assignment if any */
  private WsAttributeAssignValue[] wsAttributeAssignValues;

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
   * id of attribute def in this assignment
   * @return id of attribute def in this assignment
   */
  public String getAttributeDefId() {
    return this.attributeDefId;
  }

  /**
   * id of attribute def in this assignment
   * @param attributeDefId1
   */
  public void setAttributeDefId(String attributeDefId1) {
    this.attributeDefId = attributeDefId1;
  }

  /**
   * name of attribute def in this assignment
   * @return name of attribute def in this assignment
   */
  public String getAttributeDefName() {
    return this.attributeDefName;
  }


  /**
   * name of attribute def in this assignment
   * @param attributeDefName1
   */
  public void setAttributeDefName(String attributeDefName1) {
    this.attributeDefName = attributeDefName1;
  }


  /**
   * when created: yyyy/MM/dd HH:mm:ss.SSS Note that HH is 0-23
   */
  private String createdOn;
  
  /**
   * if there is a date here, and it is in the past, this assignment is disabled
   * yyyy/MM/dd HH:mm:ss.SSS Note that HH is 0-23
   */
  private String disabledTime;

  /**
   * T or F for if this assignment is enabled (e.g. might have expired) 
   */
  private String enabled;

  /**
   * if there is a date here, and it is in the future, this assignment is disabled
   * until that time: yyyy/MM/dd HH:mm:ss.SSS Note that HH is 0-23
   */
  private String enabledTime;

  /** id of this attribute assignment */
  private String id;
  
  /**
   * time when this attribute was last modified
   * yyyy/MM/dd HH:mm:ss.SSS Note that HH is 0-23
   */
  private String lastUpdated;

  /**
   * notes about this assignment, free-form text
   */
  private String notes;
  
  /** if this is an attribute assign attribute, this is the foreign key */
  private String ownerAttributeAssignId;
  
  /** if this is an attribute def attribute, this is the foreign key */
  private String ownerAttributeDefId;
  
  /** if this is an attribute def attribute, this is the name of foreign key */
  private String ownerAttributeDefName;
  
  /** if this is a group attribute, this is the foreign key */
  private String ownerGroupId;
  
  /** if this is a group attribute, this is the name of the foreign key */
  private String ownerGroupName;
  
  /** if this is a member attribute, this is the foreign key */
  private String ownerMemberId;
  
  /** if this is a member attribute, this is the subject of the foreign key */
  private String ownerMemberSubjectId;
  
  /** if this is a member attribute, this is the source of the foreign key */
  private String ownerMemberSourceId;
  
  /** if this is a membership attribute, this is the foreign key */
  private String ownerMembershipId;

  /** if this is a stem attribute, this is the foreign key */
  private String ownerStemId;

  /** if this is a stem attribute, this is the stem of the foreign key */
  private String ownerStemName;

  /** T of F for if this is disallowed.  Defaults to false, only available in 2.0+ */
  private String disallowed;

  /**
   * id of action for this assignment (e.g. assign).  Generally this will be AttributeDef.ACTION_DEFAULT
   * @return id of action for this assignment (e.g. assign).  Generally this will be AttributeDef.ACTION_DEFAULT
   */
  public String getAttributeAssignActionId() {
    return this.attributeAssignActionId;
  }

  /**
   * id of action for this assignment (e.g. assign).  Generally this will be AttributeDef.ACTION_DEFAULT
   * @param attributeAssignActionId1
   */
  public void setAttributeAssignActionId(String attributeAssignActionId1) {
    this.attributeAssignActionId = attributeAssignActionId1;
  }

  /**
   *  name of action for this assignment (e.g. assign).  Generally this will be AttributeDef.ACTION_DEFAULT
   * @return  name of action for this assignment (e.g. assign).  Generally this will be AttributeDef.ACTION_DEFAULT
   */
  public String getAttributeAssignActionName() {
    return this.attributeAssignActionName;
  }

  /**
   *  name of action for this assignment (e.g. assign).  Generally this will be AttributeDef.ACTION_DEFAULT
   * @param attributeAssignActionName1
   */
  public void setAttributeAssignActionName(String attributeAssignActionName1) {
    this.attributeAssignActionName = attributeAssignActionName1;
  }

  /**
   * Type of owner, from enum AttributeAssignType, e.g.
   * group, member, stem, any_mem, imm_mem, attr_def, group_asgn, mem_asgn, 
   * stem_asgn, any_mem_asgn, imm_mem_asgn, attr_def_asgn  
   * @return type
   */
  public String getAttributeAssignType() {
    return this.attributeAssignType;
  }

  /**
   * Type of owner, from enum AttributeAssignType, e.g.
   * group, member, stem, any_mem, imm_mem, attr_def, group_asgn, mem_asgn, 
   * stem_asgn, any_mem_asgn, imm_mem_asgn, attr_def_asgn  
   * @param attributeAssignType1
   */
  public void setAttributeAssignType(String attributeAssignType1) {
    this.attributeAssignType = attributeAssignType1;
  }

  /**
   * attribute name id in this assignment
   * @return attribute name id in this assignment
   */
  public String getAttributeDefNameId() {
    return this.attributeDefNameId;
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
   * @return attribute name in this assignment
   */
  public String getAttributeDefNameName() {
    return this.attributeDefNameName;
  }

  /**
   * attribute name in this assignment
   * @param attributeDefNameName1
   */
  public void setAttributeDefNameName(String attributeDefNameName1) {
    this.attributeDefNameName = attributeDefNameName1;
  }

  /**
   * when created: yyyy/MM/dd HH:mm:ss.SSS Note that HH is 0-23
   * @return when created
   */
  public String getCreatedOn() {
    return this.createdOn;
  }

  /**
   * when created: yyyy/MM/dd HH:mm:ss.SSS Note that HH is 0-23
   * @param createdOn1
   */
  public void setCreatedOn(String createdOn1) {
    this.createdOn = createdOn1;
  }

  /**
   * if there is a date here, and it is in the past, this assignment is disabled
   * yyyy/MM/dd HH:mm:ss.SSS Note that HH is 0-23
   * @return the disabled time
   */
  public String getDisabledTime() {
    return this.disabledTime;
  }

  /**
   * if there is a date here, and it is in the past, this assignment is disabled
   * yyyy/MM/dd HH:mm:ss.SSS Note that HH is 0-23
   * @param disabledTime1
   */
  public void setDisabledTime(String disabledTime1) {
    this.disabledTime = disabledTime1;
  }

  /**
   * T or F for if this assignment is enabled (e.g. might have expired) 
   * @return T or F
   */
  public String getEnabled() {
    return this.enabled;
  }

  /**
   * T or F for if this assignment is enabled (e.g. might have expired) 
   * @param enabled1
   */
  public void setEnabled(String enabled1) {
    this.enabled = enabled1;
  }

  /**
   * if there is a date here, and it is in the future, this assignment is disabled
   * until that time: yyyy/MM/dd HH:mm:ss.SSS Note that HH is 0-23
   * @return enabled time
   */
  public String getEnabledTime() {
    return this.enabledTime;
  }

  /**
   * if there is a date here, and it is in the future, this assignment is disabled
   * until that time: yyyy/MM/dd HH:mm:ss.SSS Note that HH is 0-23
   * @param enabledTime1
   */
  public void setEnabledTime(String enabledTime1) {
    this.enabledTime = enabledTime1;
  }

  /**
   * id of this attribute assignment
   * @return id
   */
  public String getId() {
    return this.id;
  }

  /**
   * id of this attribute assignment
   * @param id1
   */
  public void setId(String id1) {
    this.id = id1;
  }

  /**
   * time when this attribute was last modified
   * yyyy/MM/dd HH:mm:ss.SSS Note that HH is 0-23
   * @return last updated
   */
  public String getLastUpdated() {
    return this.lastUpdated;
  }

  /**
   * time when this attribute was last modified
   * yyyy/MM/dd HH:mm:ss.SSS Note that HH is 0-23
   * @param lastUpdated1
   */
  public void setLastUpdated(String lastUpdated1) {
    this.lastUpdated = lastUpdated1;
  }

  /**
   * notes about this assignment, free-form text
   * @return notes
   */
  public String getNotes() {
    return this.notes;
  }

  /**
   * notes about this assignment, free-form text
   * @param notes1
   */
  public void setNotes(String notes1) {
    this.notes = notes1;
  }

  /**
   * if this is an attribute assign attribute, this is the foreign key
   * @return attribute assign id
   */
  public String getOwnerAttributeAssignId() {
    return this.ownerAttributeAssignId;
  }

  /**
   * if this is an attribute assign attribute, this is the foreign key
   * @param ownerAttributeAssignId1
   */
  public void setOwnerAttributeAssignId(String ownerAttributeAssignId1) {
    this.ownerAttributeAssignId = ownerAttributeAssignId1;
  }

  /**
   * if this is an attribute def attribute, this is the foreign key
   * @return owner attribute def id
   */
  public String getOwnerAttributeDefId() {
    return this.ownerAttributeDefId;
  }

  /**
   * if this is an attribute def attribute, this is the foreign key
   * @param ownerAttributeDefId1
   */
  public void setOwnerAttributeDefId(String ownerAttributeDefId1) {
    this.ownerAttributeDefId = ownerAttributeDefId1;
  }

  /**
   * if this is an attribute def attribute, this is the name of foreign key
   * @return owner attribute def name
   */
  public String getOwnerAttributeDefName() {
    return this.ownerAttributeDefName;
  }

  /**
   * if this is an attribute def attribute, this is the name of foreign key
   * @param ownerAttributeDefName1
   */
  public void setOwnerAttributeDefName(String ownerAttributeDefName1) {
    this.ownerAttributeDefName = ownerAttributeDefName1;
  }

  /**
   * if this is a group attribute, this is the foreign key
   * @return the owner group id
   */
  public String getOwnerGroupId() {
    return this.ownerGroupId;
  }

  /**
   * if this is a group attribute, this is the foreign key
   * @param ownerGroupId1
   */
  public void setOwnerGroupId(String ownerGroupId1) {
    this.ownerGroupId = ownerGroupId1;
  }

  /**
   * if this is a group attribute, this is the name of the foreign key
   * @return owner group name
   */
  public String getOwnerGroupName() {
    return this.ownerGroupName;
  }

  /**
   * if this is a group attribute, this is the name of the foreign key
   * @param ownerGroupName1
   */
  public void setOwnerGroupName(String ownerGroupName1) {
    this.ownerGroupName = ownerGroupName1;
  }

  /**
   * if this is a member attribute, this is the foreign key
   * @return member id
   */
  public String getOwnerMemberId() {
    return this.ownerMemberId;
  }

  /**
   * if this is a member attribute, this is the foreign key
   * @param ownerMemberId1
   */
  public void setOwnerMemberId(String ownerMemberId1) {
    this.ownerMemberId = ownerMemberId1;
  }

  /**
   * if this is a member attribute, this is the subject of the foreign key
   * @return owner subject id
   */
  public String getOwnerMemberSubjectId() {
    return this.ownerMemberSubjectId;
  }

  /**
   * if this is a member attribute, this is the subject of the foreign key
   * @param ownerMemberSubjectId1
   */
  public void setOwnerMemberSubjectId(String ownerMemberSubjectId1) {
    this.ownerMemberSubjectId = ownerMemberSubjectId1;
  }

  /**
   * if this is a member attribute, this is the source of the foreign key
   * @return owner member source id
   */
  public String getOwnerMemberSourceId() {
    return this.ownerMemberSourceId;
  }

  /**
   * if this is a member attribute, this is the source of the foreign key
   * @param ownerMemberSourceId1
   */
  public void setOwnerMemberSourceId(String ownerMemberSourceId1) {
    this.ownerMemberSourceId = ownerMemberSourceId1;
  }

  /**
   * if this is a membership attribute, this is the foreign key
   * @return membership attribute
   */
  public String getOwnerMembershipId() {
    return this.ownerMembershipId;
  }

  /**
   * if this is a membership attribute, this is the foreign key
   * @param ownerMembershipId1
   */
  public void setOwnerMembershipId(String ownerMembershipId1) {
    this.ownerMembershipId = ownerMembershipId1;
  }

  /**
   * if this is a stem attribute, this is the foreign key
   * @return owner stem id
   */
  public String getOwnerStemId() {
    return this.ownerStemId;
  }

  /**
   * if this is a stem attribute, this is the foreign key
   * @param ownerStemId1
   */
  public void setOwnerStemId(String ownerStemId1) {
    this.ownerStemId = ownerStemId1;
  }

  /**
   * if this is a stem attribute, this is the stem of the foreign key
   * @return stem name
   */
  public String getOwnerStemName() {
    return this.ownerStemName;
  }

  /**
   * if this is a stem attribute, this is the stem of the foreign key
   * @param ownerStemName1
   */
  public void setOwnerStemName(String ownerStemName1) {
    this.ownerStemName = ownerStemName1;
  }

  /**
   * @return the disallowed
   */
  public String getDisallowed() {
    return this.disallowed;
  }


  /**
   * @param disallowed1 the disallowed to set
   */
  public void setDisallowed(String disallowed1) {
    this.disallowed = disallowed1;
  }


  /**
   * 
   */
  public WsAttributeAssign() {
    //default constructor
  }

}
