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
package edu.internet2.middleware.grouper.ws.soap_v2_3;

import java.sql.Timestamp;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignAction;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignActionType;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignType;
import edu.internet2.middleware.grouper.attr.value.AttributeAssignValue;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouper.pit.PITAttributeAssign;
import edu.internet2.middleware.grouper.pit.PITAttributeAssignAction;
import edu.internet2.middleware.grouper.pit.PITAttributeAssignValue;
import edu.internet2.middleware.grouper.pit.PITAttributeDef;
import edu.internet2.middleware.grouper.pit.PITAttributeDefName;
import edu.internet2.middleware.grouper.pit.PITGroup;
import edu.internet2.middleware.grouper.pit.PITMember;
import edu.internet2.middleware.grouper.pit.PITStem;
import edu.internet2.middleware.grouper.pit.finder.PITAttributeAssignValueFinder;
import edu.internet2.middleware.grouper.pit.finder.PITAttributeDefFinder;
import edu.internet2.middleware.grouper.pit.finder.PITAttributeDefNameFinder;
import edu.internet2.middleware.grouper.pit.finder.PITGroupFinder;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ws.util.GrouperServiceUtils;
import edu.internet2.middleware.grouper.ws.util.GrouperWsVersionUtils;


/**
 * result of attribute assign query represents an assignment in the DB
 */
public class WsAttributeAssign implements Comparable<WsAttributeAssign> {

  /** T of F for if this is disallowed.  Defaults to false, only available in 2.0+ */
  private String disallowed;

  
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

  /**
   * compare and sort so results are reproducible for tests
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  public int compareTo(WsAttributeAssign o2) {
    if (this == o2) {
      return 0;
    }
    //lets by null safe here
    if (o2 == null) {
      return 1;
    }
    int compare;
    
    compare = GrouperUtil.compare(this.attributeAssignType, o2.attributeAssignType);
    if (compare != 0) {
      return compare;
    }
    compare = GrouperUtil.compare(this.getAttributeDefName(), o2.getAttributeDefName());
    if (compare != 0) {
      return compare;
    }
    compare = GrouperUtil.compare(this.getAttributeDefNameName(), o2.getAttributeDefNameName());
    if (compare != 0) {
      return compare;
    }
    compare = GrouperUtil.compare(this.attributeAssignActionName, o2.attributeAssignActionName);
    if (compare != 0) {
      return compare;
    }
    compare = GrouperUtil.compare(this.getOwnerGroupName(), o2.getOwnerGroupName());
    if (compare != 0) {
      return compare;
    }
    compare = GrouperUtil.compare(this.getOwnerStemName(), o2.getOwnerStemName());
    if (compare != 0) {
      return compare;
    }
    compare = GrouperUtil.compare(this.getOwnerMemberSourceId(), o2.getOwnerMemberSourceId());
    if (compare != 0) {
      return compare;
    }
    compare = GrouperUtil.compare(this.getOwnerMemberSubjectId(), o2.getOwnerMemberSubjectId());
    if (compare != 0) {
      return compare;
    }
    compare = GrouperUtil.compare(this.getOwnerAttributeDefName(), o2.getOwnerAttributeDefName());
    if (compare != 0) {
      return compare;
    }
    compare = GrouperUtil.compare(this.getOwnerAttributeAssignId(), o2.getOwnerAttributeAssignId());
    if (compare != 0) {
      return compare;
    }
    return GrouperUtil.compare(this.id, o2.id);
  }

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
   * convert attribute assigns
   * @param attributeAssignSet should be the membership, group, and member objects in a row
   * @return the subject results
   */
  public static WsAttributeAssign[] convertAttributeAssigns(Set<AttributeAssign> attributeAssignSet) {
    int attributeAssignSetLength = GrouperUtil.length(attributeAssignSet);
    if (attributeAssignSetLength == 0) {
      return null;
    }
  
    WsAttributeAssign[] wsAttributeAssignResultArray = new WsAttributeAssign[attributeAssignSetLength];
    int index = 0;
    for (AttributeAssign attributeAssign : attributeAssignSet) {
            
      wsAttributeAssignResultArray[index++] = new WsAttributeAssign(attributeAssign);
      
    }
    return wsAttributeAssignResultArray;
  }


  /**
   * 
   */
  public WsAttributeAssign() {
    //default constructor
  }
  
  /**
   * construct with attribute assign to set internal fields
   * 
   * @param attributeAssign
   */
  public WsAttributeAssign(AttributeAssign attributeAssign) {
    
    this.attributeAssignActionId = attributeAssign.getAttributeAssignActionId();
    
    AttributeAssignAction attributeAssignAction = attributeAssign.getAttributeAssignAction();
    
    this.attributeAssignActionName =  attributeAssignAction == null ? null : attributeAssignAction.getName();
    
    this.attributeAssignDelegatable = attributeAssign.getAttributeAssignDelegatableDb();
    
    //right now we only have immediate assignments
    this.attributeAssignActionType = AttributeAssignActionType.immediate.name();
    
    AttributeAssignType theAttributeAssignType = attributeAssign.getAttributeAssignType();
    this.attributeAssignType = theAttributeAssignType == null ? null : theAttributeAssignType.name();
    
    AttributeDefName theAttributeDefName = attributeAssign.getAttributeDefName();
    AttributeDef theAttributeDef = theAttributeDefName == null ? null : theAttributeDefName.getAttributeDef();
    
    this.attributeDefId = theAttributeDefName == null ? null : theAttributeDefName.getAttributeDefId();
    this.attributeDefName = theAttributeDef == null ? null : theAttributeDef.getName();
    this.attributeDefNameId = attributeAssign.getAttributeDefNameId();
    this.attributeDefNameName = theAttributeDefName == null ? null : theAttributeDefName.getName();

    this.createdOn = GrouperServiceUtils.dateToString(attributeAssign.getCreatedOn());
    this.disabledTime = GrouperServiceUtils.dateToString(attributeAssign.getDisabledTime());

    this.enabled = attributeAssign.isEnabled() ? "T" : "F";
    
    this.enabledTime = GrouperServiceUtils.dateToString(attributeAssign.getEnabledTime());

    this.id = attributeAssign.getId();
    this.lastUpdated = GrouperServiceUtils.dateToString(attributeAssign.getLastUpdated());

    this.notes = attributeAssign.getNotes();
    this.ownerAttributeAssignId = attributeAssign.getOwnerAttributeAssignId();
    this.ownerAttributeDefId = attributeAssign.getOwnerAttributeDefId();
    
    AttributeDef ownerAttributeDef = attributeAssign.getOwnerAttributeDef();
    this.ownerAttributeDefName = ownerAttributeDef == null ? null : ownerAttributeDef.getName();
    
    this.ownerGroupId = attributeAssign.getOwnerGroupId();
    
    Group ownerGroup = attributeAssign.getOwnerGroup();
    this.ownerGroupName = ownerGroup == null ? null : ownerGroup.getName();
    
    this.ownerMemberId = attributeAssign.getOwnerMemberId();
    Member ownerMember = attributeAssign.getOwnerMember();
    this.ownerMemberSourceId = ownerMember == null ? null : ownerMember.getSubjectSourceId();
    this.ownerMemberSubjectId = ownerMember == null ? null : ownerMember.getSubjectId();

    this.ownerMembershipId = attributeAssign.getOwnerMembershipId();
    this.ownerStemId = attributeAssign.getOwnerStemId();
    
    Stem ownerStem = attributeAssign.getOwnerStem();
    
    this.ownerStemName = ownerStem == null ? null : ownerStem.getName();
    
    //get the values
    if (theAttributeDef != null && !StringUtils.isBlank(this.id) && theAttributeDef.getValueType() != null
        && theAttributeDef.getValueType().hasValue()) {
      
      Set<AttributeAssignValue> attributeAssignValues = GrouperDAOFactory
        .getFactory().getAttributeAssignValue().findByAttributeAssignId(this.id);
      
      if (GrouperUtil.length(attributeAssignValues) > 0) {
        this.wsAttributeAssignValues = WsAttributeAssignValue.convertAttributeAssigns(attributeAssignValues);
      }
      
    }
    GrouperVersion grouperVersion = GrouperWsVersionUtils.retrieveCurrentClientVersion();
    
    //disallowed is only appplicable post 2.0...
    if (grouperVersion.greaterOrEqualToArg(GrouperVersion.valueOfIgnoreCase("2.0.0"))) {
      this.setDisallowed(attributeAssign.isDisallowed() ? "T" : "F");
    }
    
  }

  /**
   * construct with attribute assign to set internal fields
   * 
   * @param pitAttributeAssign
   * @param pointInTimeFrom 
   * @param pointInTimeTo 
   */
  public WsAttributeAssign(PITAttributeAssign pitAttributeAssign, Timestamp pointInTimeFrom, Timestamp pointInTimeTo) {
    
    this.attributeAssignActionId = pitAttributeAssign.getPITAttributeAssignAction().getSourceId();
    
    PITAttributeAssignAction action = GrouperDAOFactory.getFactory().getPITAttributeAssignAction().findById(pitAttributeAssign.getAttributeAssignActionId(), true);
    
    this.attributeAssignActionName = action.getName();
    
    this.attributeAssignType = pitAttributeAssign.getAttributeAssignTypeDb();
    
    PITAttributeDefName theAttributeDefName = PITAttributeDefNameFinder.findById(pitAttributeAssign.getAttributeDefNameId(), false);
    PITAttributeDef theAttributeDef = PITAttributeDefFinder.findById(theAttributeDefName.getAttributeDefId(), false);
    
    this.attributeDefId = theAttributeDef == null ? null : theAttributeDef.getSourceId();
    this.attributeDefName = theAttributeDef == null ? null : theAttributeDef.getName();
    this.attributeDefNameId = theAttributeDefName == null ? null : theAttributeDefName.getSourceId();
    this.attributeDefNameName = theAttributeDefName == null ? null : theAttributeDefName.getName();

    this.enabled = "T";
    
    this.id = pitAttributeAssign.getSourceId();
    this.ownerAttributeAssignId = pitAttributeAssign.getOwnerAttributeAssignId() == null ? null : pitAttributeAssign.getOwnerPITAttributeAssign().getSourceId();
    this.ownerAttributeDefId = pitAttributeAssign.getOwnerAttributeDefId() == null ? null : pitAttributeAssign.getOwnerPITAttributeDef().getSourceId();
    this.ownerAttributeDefName = pitAttributeAssign.getOwnerAttributeDefId() == null ? null : pitAttributeAssign.getOwnerPITAttributeDef().getName();
    
    if (this.ownerAttributeDefId != null) {
      PITAttributeDef ownerAttributeDef = PITAttributeDefFinder.findById(pitAttributeAssign.getOwnerAttributeDefId(), false);
      this.ownerAttributeDefName = ownerAttributeDef == null ? null : ownerAttributeDef.getName();
    }
    
    this.ownerGroupId = pitAttributeAssign.getOwnerGroupId() == null ? null : pitAttributeAssign.getOwnerPITGroup().getSourceId();
    
    if (this.ownerGroupId != null) {
      PITGroup ownerGroup = PITGroupFinder.findById(pitAttributeAssign.getOwnerGroupId(), false);
      this.ownerGroupName = ownerGroup == null ? null : ownerGroup.getName();
    }
    
    this.ownerMemberId = pitAttributeAssign.getOwnerMemberId() == null ? null : pitAttributeAssign.getOwnerPITMember().getSourceId();
    
    if (this.ownerMemberId != null) {
      PITMember ownerMember = GrouperDAOFactory.getFactory().getPITMember().findById(pitAttributeAssign.getOwnerMemberId(), false);
      this.ownerMemberSourceId = ownerMember == null ? null : ownerMember.getSubjectSourceId();
      this.ownerMemberSubjectId = ownerMember == null ? null : ownerMember.getSubjectId();
    }
    
    this.ownerMembershipId = pitAttributeAssign.getOwnerMembershipId() == null ? null : pitAttributeAssign.getOwnerPITMembership().getSourceId();
    this.ownerStemId = pitAttributeAssign.getOwnerStemId() == null ? null : pitAttributeAssign.getOwnerPITStem().getSourceId();
    
    if (this.ownerStemId != null) {
      PITStem ownerStem = GrouperDAOFactory.getFactory().getPITStem().findById(pitAttributeAssign.getOwnerStemId(), false);
      this.ownerStemName = ownerStem == null ? null : ownerStem.getName();
    }
        
    //get the values
    if (theAttributeDef != null && !StringUtils.isBlank(this.id)) {
      
      Set<PITAttributeAssignValue> values = PITAttributeAssignValueFinder.findByPITAttributeAssign(
          pitAttributeAssign, pointInTimeFrom, pointInTimeTo);
      
      if (GrouperUtil.length(values) > 0) {
        this.wsAttributeAssignValues = WsAttributeAssignValue.convertPITAttributeAssignValues(values);
      }
      
    }
    
    GrouperVersion grouperVersion = GrouperWsVersionUtils.retrieveCurrentClientVersion();
    
    //disallowed is only appplicable post 2.0...
    if (grouperVersion.greaterOrEqualToArg(GrouperVersion.valueOfIgnoreCase("2.0.0"))) {
      this.setDisallowed(pitAttributeAssign.isDisallowed() ? "T" : "F");
    }

  }
}
