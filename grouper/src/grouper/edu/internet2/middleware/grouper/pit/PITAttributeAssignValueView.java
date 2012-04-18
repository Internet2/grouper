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
package edu.internet2.middleware.grouper.pit;

import java.util.Set;

import edu.internet2.middleware.grouper.GrouperAPI;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * @author shilen
 * $Id$
 */
@SuppressWarnings("serial")
public class PITAttributeAssignValueView extends GrouperPIT {
  
  /** constant for field name for: attributeAssignValueId */
  public static final String FIELD_ATTRIBUTE_ASSIGN_VALUE_ID = "attributeAssignValueId";

  /** constant for field name for: attributeAssignId */
  public static final String FIELD_ATTRIBUTE_ASSIGN_ID = "attributeAssignId";
  
  /** constant for field name for: ownerAttributeAssignId */
  public static final String FIELD_OWNER_ATTRIBUTE_ASSIGN_ID = "ownerAttributeAssignId";

  /** constant for field name for: ownerAttributeDefId */
  public static final String FIELD_OWNER_ATTRIBUTE_DEF_ID = "ownerAttributeDefId";

  /** constant for field name for: ownerGroupId */
  public static final String FIELD_OWNER_GROUP_ID = "ownerGroupId";

  /** constant for field name for: ownerMemberId */
  public static final String FIELD_OWNER_MEMBER_ID = "ownerMemberId";

  /** constant for field name for: ownerMembershipId */
  public static final String FIELD_OWNER_MEMBERSHIP_ID = "ownerMembershipId";

  /** constant for field name for: ownerStemId */
  public static final String FIELD_OWNER_STEM_ID = "ownerStemId";
  
  /** constant for field name for: attributeAssignActionId */
  public static final String FIELD_ATTRIBUTE_ASSIGN_ACTION_ID = "attributeAssignActionId";

  /** constant for field name for: attributeAssignType */
  public static final String FIELD_ATTRIBUTE_ASSIGN_TYPE = "attributeAssignType";

  /** constant for field name for: attributeDefNameId */
  public static final String FIELD_ATTRIBUTE_DEF_NAME_ID = "attributeDefNameId";
  
  /** constant for field name for: valueFloating */
  public static final String FIELD_VALUE_FLOATING = "valueFloating";

  /** constant for field name for: valueInteger */
  public static final String FIELD_VALUE_INTEGER = "valueInteger";

  /** constant for field name for: valueMemberId */
  public static final String FIELD_VALUE_MEMBER_ID = "valueMemberId";

  /** constant for field name for: valueString */
  public static final String FIELD_VALUE_STRING = "valueString";
  
  /**
   * fields which are included in clone method
   */
  private static final Set<String> CLONE_FIELDS = GrouperUtil.toSet(
      FIELD_ACTIVE_DB, FIELD_ATTRIBUTE_ASSIGN_ID, FIELD_END_TIME_DB, 
      FIELD_ATTRIBUTE_ASSIGN_VALUE_ID, FIELD_START_TIME_DB, FIELD_VALUE_FLOATING, 
      FIELD_VALUE_INTEGER, FIELD_VALUE_MEMBER_ID, FIELD_VALUE_STRING,
      FIELD_ATTRIBUTE_ASSIGN_ACTION_ID, FIELD_ATTRIBUTE_ASSIGN_TYPE, FIELD_ATTRIBUTE_DEF_NAME_ID,
      FIELD_OWNER_ATTRIBUTE_ASSIGN_ID, FIELD_OWNER_ATTRIBUTE_DEF_ID, FIELD_OWNER_GROUP_ID,
      FIELD_OWNER_MEMBER_ID, FIELD_OWNER_MEMBERSHIP_ID, FIELD_OWNER_STEM_ID);
  
  
  /** attributeAssignValueId */
  private String attributeAssignValueId;
  
  /** attributeAssignId */
  private String attributeAssignId;
  
  /** if this is an attribute assign attribute, this is the foreign key */
  private String ownerAttributeAssignId;
  
  /** if this is an attribute def attribute, this is the foreign key */
  private String ownerAttributeDefId;
  
  /** if this is a group attribute, this is the foreign key */
  private String ownerGroupId;
  
  /** if this is a member attribute, this is the foreign key */
  private String ownerMemberId;
  
  /** if this is a membership attribute, this is the foreign key */
  private String ownerMembershipId;
  
  /** if this is a stem attribute, this is the foreign key */
  private String ownerStemId;
  
  /** attributeAssignActionId */
  private String attributeAssignActionId;
  
  /** attributeAssignType */
  private String attributeAssignType;

  /** attributeDefNameId */
  private String attributeDefNameId;
  
  /** string value */
  private String valueString;

  /** floating point value */
  private Double valueFloating;

  /** integer value */
  private Long valueInteger;
  
  /** member id value */
  private String valueMemberId;

  /** pitAttributeAssign */
  private PITAttributeAssign pitAttributeAssign = null;
  
  /** pitAttributeAssignValue */
  private PITAttributeAssignValue pitAttributeAssignValue = null;
  
  /** pitAttributeDefName */
  private PITAttributeDefName pitAttributeDefName = null;  
  
  /** pitAttributeAssignAction */
  private PITAttributeAssignAction pitAttributeAssignAction = null;
  
  /** pitOwnerAttributeAssign */
  private PITAttributeAssign pitOwnerAttributeAssign = null;
  
  /** pitOwnerAttributeDef */
  private PITAttributeDef pitOwnerAttributeDef = null;
  
  /** pitOwnerGroup */
  private PITGroup pitOwnerGroup = null;
  
  /** pitOwnerMember */
  private PITMember pitOwnerMember = null;
  
  /** pitOwnerMembership */
  private PITMembership pitOwnerMembership = null;
  
  /** pitOwnerStem */
  private PITStem pitOwnerStem = null;
  
  /**
   * @see edu.internet2.middleware.grouper.GrouperAPI#clone()
   */
  @Override
  public GrouperAPI clone() {
    return GrouperUtil.clone(this, CLONE_FIELDS);
  }
  
  /**
   * @return ownerAttributeAssignId
   */
  public String getOwnerAttributeAssignId() {
    return ownerAttributeAssignId;
  }

  /**
   * @param ownerAttributeAssignId
   */
  public void setOwnerAttributeAssignId(String ownerAttributeAssignId) {
    this.ownerAttributeAssignId = ownerAttributeAssignId;
  }

  /**
   * @return ownerAttributeDefId
   */
  public String getOwnerAttributeDefId() {
    return ownerAttributeDefId;
  }

  /**
   * @param ownerAttributeDefId
   */
  public void setOwnerAttributeDefId(String ownerAttributeDefId) {
    this.ownerAttributeDefId = ownerAttributeDefId;
  }

  /**
   * @return ownerGroupId
   */
  public String getOwnerGroupId() {
    return ownerGroupId;
  }

  /**
   * @param ownerGroupId
   */
  public void setOwnerGroupId(String ownerGroupId) {
    this.ownerGroupId = ownerGroupId;
  }

  /**
   * @return ownerMemberId
   */
  public String getOwnerMemberId() {
    return ownerMemberId;
  }

  /**
   * @param ownerMemberId
   */
  public void setOwnerMemberId(String ownerMemberId) {
    this.ownerMemberId = ownerMemberId;
  }

  /**
   * @return ownerMembershipId
   */
  public String getOwnerMembershipId() {
    return ownerMembershipId;
  }

  /**
   * @param ownerMembershipId
   */
  public void setOwnerMembershipId(String ownerMembershipId) {
    this.ownerMembershipId = ownerMembershipId;
  }

  /**
   * @return ownerStemId
   */
  public String getOwnerStemId() {
    return ownerStemId;
  }

  /**
   * @param ownerStemId
   */
  public void setOwnerStemId(String ownerStemId) {
    this.ownerStemId = ownerStemId;
  }

  /**
   * @return attributeAssignActionId
   */
  public String getAttributeAssignActionId() {
    return attributeAssignActionId;
  }

  /**
   * @param attributeAssignActionId
   */
  public void setAttributeAssignActionId(String attributeAssignActionId) {
    this.attributeAssignActionId = attributeAssignActionId;
  }

  /**
   * @return attributeAssignType
   */
  public String getAttributeAssignTypeDb() {
    return attributeAssignType;
  }

  /**
   * @param attributeAssignType
   */
  public void setAttributeAssignTypeDb(String attributeAssignType) {
    this.attributeAssignType = attributeAssignType;
  }

  /**
   * @return attributeDefNameId
   */
  public String getAttributeDefNameId() {
    return attributeDefNameId;
  }

  /**
   * @param attributeDefNameId
   */
  public void setAttributeDefNameId(String attributeDefNameId) {
    this.attributeDefNameId = attributeDefNameId;
  }
  

  /**
   * @return the valueString
   */
  public String getValueString() {
    return valueString;
  }

  
  /**
   * @param valueString the valueString to set
   */
  public void setValueString(String valueString) {
    this.valueString = valueString;
  }

  
  /**
   * @return the valueFloating
   */
  public Double getValueFloating() {
    return valueFloating;
  }

  
  /**
   * @param valueFloating the valueFloating to set
   */
  public void setValueFloating(Double valueFloating) {
    this.valueFloating = valueFloating;
  }

  
  /**
   * @return the valueInteger
   */
  public Long getValueInteger() {
    return valueInteger;
  }

  
  /**
   * @param valueInteger the valueInteger to set
   */
  public void setValueInteger(Long valueInteger) {
    this.valueInteger = valueInteger;
  }

  
  /**
   * @return the valueMemberId
   */
  public String getValueMemberId() {
    return valueMemberId;
  }

  
  /**
   * @param valueMemberId the valueMemberId to set
   */
  public void setValueMemberId(String valueMemberId) {
    this.valueMemberId = valueMemberId;
  }

  
  /**
   * @return the attributeAssignValueId
   */
  public String getAttributeAssignValueId() {
    return attributeAssignValueId;
  }

  
  /**
   * @param attributeAssignValueId the attributeAssignValueId to set
   */
  public void setAttributeAssignValueId(String attributeAssignValueId) {
    this.attributeAssignValueId = attributeAssignValueId;
  }

  
  /**
   * @return the attributeAssignId
   */
  public String getAttributeAssignId() {
    return attributeAssignId;
  }

  
  /**
   * @param attributeAssignId the attributeAssignId to set
   */
  public void setAttributeAssignId(String attributeAssignId) {
    this.attributeAssignId = attributeAssignId;
  }
  
  /**
   * @return pitOwnerStem
   */
  public PITStem getOwnerPITStem() {
    if (pitOwnerStem == null && ownerStemId != null) {
      pitOwnerStem = GrouperDAOFactory.getFactory().getPITStem().findById(ownerStemId, true);
    }
    
    return pitOwnerStem;
  }
  
  /**
   * @return pitOwnerGroup
   */
  public PITGroup getOwnerPITGroup() {
    if (pitOwnerGroup == null && ownerGroupId != null) {
      pitOwnerGroup = GrouperDAOFactory.getFactory().getPITGroup().findById(ownerGroupId, true);
    }
    
    return pitOwnerGroup;
  }
  
  /**
   * @return pitOwnerAttributeDef
   */
  public PITAttributeDef getOwnerPITAttributeDef() {
    if (pitOwnerAttributeDef == null && ownerAttributeDefId != null) {
      pitOwnerAttributeDef = GrouperDAOFactory.getFactory().getPITAttributeDef().findById(ownerAttributeDefId, true);
    }
    
    return pitOwnerAttributeDef;
  }
  
  /**
   * @return pitOwnerMember
   */
  public PITMember getOwnerPITMember() {
    if (pitOwnerMember == null && ownerMemberId != null) {
      pitOwnerMember = GrouperDAOFactory.getFactory().getPITMember().findById(ownerMemberId, true);
    }
    
    return pitOwnerMember;
  }
  
  /**
   * @return pitOwnerAttributeAssign
   */
  public PITAttributeAssign getOwnerPITAttributeAssign() {
    if (pitOwnerAttributeAssign == null && ownerAttributeAssignId != null) {
      pitOwnerAttributeAssign = GrouperDAOFactory.getFactory().getPITAttributeAssign().findById(ownerAttributeAssignId, true);
    }
    
    return pitOwnerAttributeAssign;
  }
  
  /**
   * @return pitOwnerMembership
   */
  public PITMembership getOwnerPITMembership() {
    if (pitOwnerMembership == null && ownerMembershipId != null) {
      pitOwnerMembership = GrouperDAOFactory.getFactory().getPITMembership().findById(ownerMembershipId, true);
    }
    
    return pitOwnerMembership;
  }
  
  /**
   * @return pitAttributeAssign
   */
  public PITAttributeAssign getPITAttributeAssign() {
    if (pitAttributeAssign == null && attributeAssignId != null) {
      pitAttributeAssign = GrouperDAOFactory.getFactory().getPITAttributeAssign().findById(attributeAssignId, true);
    }
    
    return pitAttributeAssign;
  }
  
  /**
   * @return pitAttributeAssignValue
   */
  public PITAttributeAssignValue getPITAttributeAssignValue() {
    if (pitAttributeAssignValue == null && attributeAssignValueId != null) {
      pitAttributeAssignValue = GrouperDAOFactory.getFactory().getPITAttributeAssignValue().findById(attributeAssignValueId, true);
    }
    
    return pitAttributeAssignValue;
  }
  
  /**
   * @return pitAttributeAssignAction
   */
  public PITAttributeAssignAction getPITAttributeAssignAction() {
    if (pitAttributeAssignAction == null && attributeAssignActionId != null) {
      pitAttributeAssignAction = GrouperDAOFactory.getFactory().getPITAttributeAssignAction().findById(attributeAssignActionId, true);
    }
    
    return pitAttributeAssignAction;
  }
  
  /**
   * @return pitAttributeDefName
   */
  public PITAttributeDefName getPITAttributeDefName() {
    if (pitAttributeDefName == null && attributeDefNameId != null) {
      pitAttributeDefName = GrouperDAOFactory.getFactory().getPITAttributeDefName().findById(attributeDefNameId, true);
    }
    
    return pitAttributeDefName;
  }
}
