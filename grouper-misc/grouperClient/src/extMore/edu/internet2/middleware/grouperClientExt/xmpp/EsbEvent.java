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
/*
 * @author Rob Hebron
 */

package edu.internet2.middleware.grouperClientExt.xmpp;

import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;

/**
 * 
 * Simple class to hold changelog event details for dispatch to external system. If an event
 * does not contain a value for a property it will be null
 *
 */
public class EsbEvent {


  /**
   * type of event
   * @author mchyzer
   *
   */
  public static enum EsbEventType {
    
    /** ENTITY_ADD event */
    ENTITY_ADD, 
    
    /** ENTITY_DELETE event */
    ENTITY_DELETE,
    
    /** ENTITY_UPDATE event */
    ENTITY_UPDATE,

    /** GROUP_ADD event */
    GROUP_ADD, 
    
    /** GROUP_DELETE event */
    GROUP_DELETE,
    
    /** GROUP_FIELD_ADD event */
    GROUP_FIELD_ADD,
    
    /** GROUP_FIELD_DELETE event */
    GROUP_FIELD_DELETE,
    
    /** GROUP_FIELD_UPDATE event */
    GROUP_FIELD_UPDATE,
  
    /** GROUP_TYPE_ADD event */
    GROUP_TYPE_ADD,
  
    /** GROUP_TYPE_DELETE event */
    GROUP_TYPE_DELETE,
  
    /** GROUP_TYPE_UPDATE event */
    GROUP_TYPE_UPDATE,
  
    /** GROUP_UPDATE event */
    GROUP_UPDATE,
  
    /** MEMBERSHIP_ADD event */
    MEMBERSHIP_ADD,
  
    /** MEMBERSHIP_DELETE event */
    MEMBERSHIP_DELETE,
  
    /** MEMBERSHIP_UPDATE event */
    MEMBERSHIP_UPDATE,
  
    /** PRIVILEGE_ADD event */
    PRIVILEGE_ADD,
  
    /** PRIVILEGE_DELETE event */
    PRIVILEGE_DELETE,
  
    /** PRIVILEGE_UPDATE event */
    PRIVILEGE_UPDATE,
  
    /** STEM_ADD event */
    STEM_ADD,
  
    /** STEM_DELETE event */
    STEM_DELETE,
  
    /** STEM_UPDATE event */
    STEM_UPDATE;
    
  }

  /**
   * get a subject attribute by name
   * @param attributeName
   * @return the name
   */
  public String subjectAttribute(String attributeName) {
    
    for (String[] row : GrouperClientUtils.nonNull(this.subjectAttributes, String[].class)) {
      if (GrouperClientUtils.equals(attributeName, row[0])) {
        return row[1];
      }
    }
    return null;
  }
  
  /**
   * see if has non blank attribute
   * @param attributeName
   * @return true if the subject has attribute
   */
  public boolean subjectHasAttribute(String attributeName) {
    return !GrouperClientUtils.isBlank(subjectAttribute(attributeName));
  }
  
  /**
   * if a change occurred but no sensitive data is being sent
   */
  private boolean changeOccurred;
  
  /**
   * if a change occurred but no sensitive data is being sent
   * @return the changeOccurred
   */
  public boolean isChangeOccurred() {
    return this.changeOccurred;
  }
  
  /**
   * if a change occurred but no sensitive data is being sent
   * @param changeOccurred the changeOccurred to set
   */
  public void setChangeOccurred(boolean changeOccurred) {
    this.changeOccurred = changeOccurred;
  }

  /** sequence number of event for logging or whatnot */
  private String sequenceNumber;
  
  /** */
  private String eventType;

  /** */
  private String id;

  /** */
  private String name;

  /** */
  private String description;

  /** */
  private String displayExtension;

  /** */
  private String displayName;

  /** */
  private String fieldName;

  /** */
  private String groupId;

  /** */
  private String groupName;

  /** */
  private String groupTypeId;

  /** */
  private String groupTypeName;

  /** */
  private String membershipType;

  /** */
  private String ownerId;

  /** */
  private String ownerName;

  /** */
  private String ownerType;

  /** */
  private String parentStemId;

  /** */
  private String privilegeName;

  /** */
  private String privilegeType;

  /** */
  private String propertyChanged;

  /** */
  private String propertyNewValue;

  /** */
  private String propertyOldValue;

  /** */
  private String readPrivilege;

  /** */
  private String sourceId;

  /** */
  private String subjectId;

  /** */
  private String type;

  /** */
  private String writePrivilege;

  /** */
  private String[][] subjectAttributes;
  
  /**
   * sequence number of event for logging or whatnot
   * @return sequence number
   */
  public String getSequenceNumber() {
    return sequenceNumber;
  }

  /**
   * sequence number of event for logging or whatnot
   * @param sequenceNumber1
   */
  public void setSequenceNumber(String sequenceNumber1) {
    this.sequenceNumber = sequenceNumber1;
  }

  /**
   * 
   * @return eventType
   */
  public String getEventType() {
    return eventType;
  }

  /**
   * 
   * @param eventType
   */
  public void setEventType(String eventType) {
    this.eventType = eventType;
  }

  /**
   * 
   * @return id
   */
  public String getId() {
    return id;
  }

  /**
   * 
   * @param id
   */
  public void setId(String id) {
    this.id = id;
  }

  /**
   * 
   * @return name
   */
  public String getName() {
    return name;
  }

  /**
   * 
   * @param name
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * 
   * @return description
   */
  public String getDescription() {
    return description;
  }

  /**
   * 
   * @param description
   */
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * 
   * @return displayExtension
   */
  public String getDisplayExtension() {
    return displayExtension;
  }

  /**
   * 
   * @param displayExtension
   */
  public void setDisplayExtension(String displayExtension) {
    this.displayExtension = displayExtension;
  }

  /**
   * 
   * @return displayName
   */
  public String getDisplayName() {
    return displayName;
  }

  /**
   * 
   * @param displayName
   */
  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  /**
   * 
   * @return fieldName
   */
  public String getFieldName() {
    return fieldName;
  }

  /**
   * 
   * @param fieldName
   */
  public void setFieldName(String fieldName) {
    this.fieldName = fieldName;
  }

  /**
   * 
   * @return groupId
   */
  public String getGroupId() {
    return groupId;
  }

  /**
   * 
   * @param groupId
   */
  public void setGroupId(String groupId) {
    this.groupId = groupId;
  }

  /**
   * 
   * @return groupName
   */
  public String getGroupName() {
    return groupName;
  }

  /**
   * 
   * @param groupName
   */
  public void setGroupName(String groupName) {
    this.groupName = groupName;
  }

  /**
   * 
   * @return groupTypeId
   */
  public String getGroupTypeId() {
    return groupTypeId;
  }

  /**
   * 
   * @param groupTypeId
   */
  public void setGroupTypeId(String groupTypeId) {
    this.groupTypeId = groupTypeId;
  }

  /**
   * 
   * @return groupTypeName
   */
  public String getGroupTypeName() {
    return groupTypeName;
  }

  /**
   * 
   * @param groupTypeName
   */
  public void setGroupTypeName(String groupTypeName) {
    this.groupTypeName = groupTypeName;
  }

  /**
   * 
   * @return membershipType
   */
  public String getMembershipType() {
    return membershipType;
  }

  /**
   * 
   * @param membershipType
   */
  public void setMembershipType(String membershipType) {
    this.membershipType = membershipType;
  }

  /**
   * 
   * @return ownerId
   */
  public String getOwnerId() {
    return ownerId;
  }

  /**
   * 
   * @param ownerId
   */
  public void setOwnerId(String ownerId) {
    this.ownerId = ownerId;
  }

  /**
   * 
   * @return ownerName
   */
  public String getOwnerName() {
    return ownerName;
  }

  /**
   * 
   * @param ownerName
   */
  public void setOwnerName(String ownerName) {
    this.ownerName = ownerName;
  }

  /**
   * 
   * @return ownerType
   */
  public String getOwnerType() {
    return ownerType;
  }

  /**
   * 
   * @param ownerType
   */
  public void setOwnerType(String ownerType) {
    this.ownerType = ownerType;
  }

  /**
   * 
   * @return parentStemId
   */
  public String getParentStemId() {
    return parentStemId;
  }

  /**
   * 
   * @param parentStemId
   */
  public void setParentStemId(String parentStemId) {
    this.parentStemId = parentStemId;
  }

  /**
   * 
   * @return privilegeName
   */
  public String getPrivilegeName() {
    return privilegeName;
  }

  /**
   * 
   * @param privilegeName
   */
  public void setPrivilegeName(String privilegeName) {
    this.privilegeName = privilegeName;
  }

  /**
   * 
   * @return privilegeType
   */
  public String getPrivilegeType() {
    return privilegeType;
  }

  /**
   * 
   * @param privilegeType
   */
  public void setPrivilegeType(String privilegeType) {
    this.privilegeType = privilegeType;
  }

  /**
   * 
   * @return propertyChanged
   */
  public String getPropertyChanged() {
    return propertyChanged;
  }

  /**
   * 
   * @param propertyChanged
   */
  public void setPropertyChanged(String propertyChanged) {
    this.propertyChanged = propertyChanged;
  }

  /**
   * 
   * @return propertyNewValue
   */
  public String getPropertyNewValue() {
    return propertyNewValue;
  }

  /**
   * 
   * @param propertyNewValue
   */
  public void setPropertyNewValue(String propertyNewValue) {
    this.propertyNewValue = propertyNewValue;
  }

  /**
   * 
   * @return propertyOldValue
   */
  public String getPropertyOldValue() {
    return propertyOldValue;
  }

  /**
   * 
   * @param propertyOldValue
   */
  public void setPropertyOldValue(String propertyOldValue) {
    this.propertyOldValue = propertyOldValue;
  }

  /**
   * 
   * @return readPrivilege
   */
  public String getReadPrivilege() {
    return readPrivilege;
  }

  /**
   * 
   * @param readPrivilege
   */
  public void setReadPrivilege(String readPrivilege) {
    this.readPrivilege = readPrivilege;
  }

  /**
   * 
   * @return sourceId
   */
  public String getSourceId() {
    return sourceId;
  }

  /**
   * 
   * @param sourceId
   */
  public void setSourceId(String sourceId) {
    this.sourceId = sourceId;
  }

  /**
   * 
   * @return subjectId
   */
  public String getSubjectId() {
    return subjectId;
  }

  /**
   * 
   * @param subjectId
   */
  public void setSubjectId(String subjectId) {
    this.subjectId = subjectId;
  }

  /**
   * 
   * @return type
   */
  public String getType() {
    return type;
  }

  /**
   * 
   * @param type
   */
  public void setType(String type) {
    this.type = type;
  }

  /**
   * 
   * @return writePrivilege
   */
  public String getWritePrivilege() {
    return writePrivilege;
  }

  /**
   * 
   * @param writePrivilege
   */
  public void setWritePrivilege(String writePrivilege) {
    this.writePrivilege = writePrivilege;
  }

  /**
   * 
   * @return subjectAttributes
   */
  public String[][] getSubjectAttributes() {
    return subjectAttributes;
  }

  /**
   * 
   * @param subjectAttributes
   */
  public void setSubjectAttributes(String[][] subjectAttributes) {
    this.subjectAttributes = subjectAttributes;
  }

  /**
   * Method to add attribute name/value pair to subjectAttribute array
   * @param attributeName
   * @param attributeValue
   */
  public void addSubjectAttribute(String attributeName, String attributeValue) {
    if (this.subjectAttributes == null) {
      this.subjectAttributes = new String[1][2];
      this.subjectAttributes[0][0] = attributeName;
      this.subjectAttributes[0][1] = attributeValue;
    } else {
      String[][] newArray = new String[this.subjectAttributes.length + 1][2];
      System.arraycopy(this.subjectAttributes, 0, newArray, 0,
          this.subjectAttributes.length);
      newArray[this.subjectAttributes.length][0] = attributeName;
      newArray[this.subjectAttributes.length][1] = attributeValue;
      this.setSubjectAttributes(newArray);
    }
  }
}
