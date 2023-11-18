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
   * messageid of message
   */
  private String messageId;
  
  /**
   * messageid of message
   * @return message id
   */
  public String getMessageId() {
    return this.messageId;
  }

  /**
   * messageid of message
   * @param messageId1
   */
  public void setMessageId(String messageId1) {
    this.messageId = messageId1;
  }

  /**
   * microseconds since 1970 that this event was created.  divide by 1000 for millis
   */
  private Long createdOnMicros = null;
  
  /**
   * microseconds since 1970 that this event was created.  divide by 1000 for millis
   * @return the createdOnMicros
   */
  public Long getCreatedOnMicros() {
    return this.createdOnMicros;
  }
  
  /**
   * microseconds since 1970 that this event was created.  divide by 1000 for millis
   * @param createdOnMicros1 the createdOnMicros to set
   */
  public void setCreatedOnMicros(Long createdOnMicros1) {
    this.createdOnMicros = createdOnMicros1;
  }

  /**
   * type of event
   * @author mchyzer
   *
   */
  public static enum EsbEventType {
    
    /** ATTRIBUTE_ASSIGN_ADD event */
    ATTRIBUTE_ASSIGN_ADD, 
    
    /** ATTRIBUTE_ASSIGN_DELETE event */
    ATTRIBUTE_ASSIGN_DELETE, 
    
    /** ATTRIBUTE_ASSIGN_VALUE_ADD event */
    ATTRIBUTE_ASSIGN_VALUE_ADD, 
    
    /** ATTRIBUTE_ASSIGN_VALUE_DELETE event */
    ATTRIBUTE_ASSIGN_VALUE_DELETE, 
    
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

  /**
   * if doing a sync, this is the provisioner
   */
  private String provisionerName;
  
  /**
   * if doing a sync, this is the provisioner
   * @return provisioner name
   */
  public String getProvisionerName() {
    return this.provisionerName;
  }

  /**
   * if doing a sync, this is the provisioner
   * @param provisionerName1
   */
  public void setProvisionerName(String provisionerName1) {
    this.provisionerName = provisionerName1;
  }

  /**
   * for full sync, or even for group sync potentially, true if the sync should block other sync,
   * false if should not block and should just add more lowel level sync events
   */
  private Boolean provisionerBlocking;

  /**
   * for full sync, or even for group sync potentially, true if the sync should block other sync,
   * false if should not block and should just add more lowel level sync events
   * @return
   */
  public Boolean isProvisionerBlocking() {
    return this.provisionerBlocking;
  }

  
  public void setProvisionerBlocking(Boolean provisionerBlocking1) {
    this.provisionerBlocking = provisionerBlocking1;
  }

  /**
   * if this is a full sync
   */
  private Boolean provisionerFullSync;
  
  /**
   * if this is a full sync
   * @return if full sync
   */
  public Boolean getProvisionerFullSync() {
    return this.provisionerFullSync;
  }

  /**
   * if this is a full sync
   * @param provisionerFullSync1
   */
  public void setProvisionerFullSync(Boolean provisionerFullSync1) {
    this.provisionerFullSync = provisionerFullSync1;
  }

  /**
   * sync type of the sync if full sync.  e.g. fullSyncFull or fullSyncGroup, groupSync, userSync, membershipSync
   */
  private String provisionerSyncType;
  
  /**
   * sync type of the sync.  e.g. fullSyncFull or fullSyncGroup, groupSync, userSync, membershipSync
   * @return sync type
   */
  public String getProvisionerSyncType() {
    return this.provisionerSyncType;
  }

  /**
   * sync type of the sync.  e.g. fullSyncFull or fullSyncGroup, groupSync, userSync, membershipSync
   * @param provisionerSyncType1
   */
  public void setProvisionerSyncType(String provisionerSyncType1) {
    this.provisionerSyncType = provisionerSyncType1;
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
  private String fieldId;

  /** */
  private String groupId;

  /** */
  private String groupName;

  /** */
  private String roleId;

  /** */
  private String roleName;
  
  /** */
  private String memberId;

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
  private String subjectIdentifier0;
  
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
   * @return the subjectIdentifier
   */
  public String getSubjectIdentifier0() {
    return this.subjectIdentifier0;
  }

  
  /**
   * @param subjectIdentifier the subjectIdentifier to set
   */
  public void setSubjectIdentifier0(String subjectIdentifier) {
    this.subjectIdentifier0 = subjectIdentifier;
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
   * field id
   * @return field id
   */
  public String getFieldId() {
    return this.fieldId;
  }

  /**
   * field id
   * @param fieldId1
   */
  public void setFieldId(String fieldId1) {
    this.fieldId = fieldId1;
  }

  
  public Boolean getProvisionerBlocking() {
    return provisionerBlocking;
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
  
  /**
   * @param roleId the roleId to set
   */
  public void setRoleId(String roleId) {
    this.roleId = roleId;
  }

  
  /**
   * @return the roleName
   */
  public String getRoleName() {
    return roleName;
  }

  
  /**
   * @param roleName the roleName to set
   */
  public void setRoleName(String roleName) {
    this.roleName = roleName;
  }

  
  /**
   * @return the memberId
   */
  public String getMemberId() {
    return memberId;
  }

  
  /**
   * @param memberId the memberId to set
   */
  public void setMemberId(String memberId) {
    this.memberId = memberId;
  }

  /**
   * for attribute assignments, if the attributeAssignType is any_mem, then ownerId1 is the groupId and ownerId2 is the memberId
   */
  private String ownerId2;
  
  /**
   * for attribute assignments, if the attributeAssignType is any_mem, then ownerId1 is the groupId and ownerId2 is the memberId
   * @return ownerId2
   */
  public String getOwnerId2() {
    return ownerId2;
  }

  /**
   * for attribute assignments, if the attributeAssignType is any_mem, then ownerId1 is the groupId and ownerId2 is the memberId
   * @param ownerId2
   */
  public void setOwnerId2(String ownerId2) {
    this.ownerId2 = ownerId2;
  }


  /**
   * attribute def name id for attribute assignments
   */
  private String attributeDefNameId;

  
  
  /**
   * attribute def name id for attribute assignments
   * @return attribute def name id
   */
  public String getAttributeDefNameId() {
    return this.attributeDefNameId;
  }

  /**
   * attribute def name id for attribute assignments
   * @param attributeDefNameId1
   */
  public void setAttributeDefNameId(String attributeDefNameId1) {
    this.attributeDefNameId = attributeDefNameId1;
  }

  /**
   * name of attributeDefName
   */
  private String attributeDefNameName;

  /**
   * name of attributeDefName
   * @return name
   */
  public String getAttributeDefNameName() {
    return this.attributeDefNameName;
  }

  /**
   * name of attributeDefName
   * @param attributeDefNameName1 
   */
  public void setAttributeDefNameName(String attributeDefNameName1) {
    this.attributeDefNameName = attributeDefNameName1;
  }

  /**
   * attributeAssignId
   */
  private String attributeAssignId;
  
  
  
  /**
   * attributeAssignId
   * @return id
   */
  public String getAttributeAssignId() {
    return this.attributeAssignId;
  }

  /**
   * attributeAssignId
   * @param attributeAssignId1
   */
  public void setAttributeAssignId(String attributeAssignId1) {
    this.attributeAssignId = attributeAssignId1;
  }

  /**
   * type of value: nullValue, memberId, string, floating, integerValue
   */
  private String valueType;
  
  /**
   * type of value: nullValue, memberId, string, floating, integerValue
   * @return vlaue type
   */
  public String getValueType() {
    return this.valueType;
  }

  /**
   * type of value: nullValue, memberId, string, floating, integerValue
   * @param valueType1
   */
  public void setValueType(String valueType1) {
    this.valueType = valueType1;
  }

  /**
   * attribute assign type: group, group_asgn, member, member_asgn, stem, stem_asgn, any_mem, any_mem_asgn, attr_def, attr_def_asgn, imm_mem, imm_mem_asgn
   */
  private String attributeAssignType;

  
  
  /**
   * attribute assign type: group, group_asgn, member, member_asgn, stem, stem_asgn, any_mem, any_mem_asgn, attr_def, attr_def_asgn, imm_mem, imm_mem_asgn
   * @return type
   */
  public String getAttributeAssignType() {
    return this.attributeAssignType;
  }

  /**
   * attribute assign type: group, group_asgn, member, member_asgn, stem, stem_asgn, any_mem, any_mem_asgn, attr_def, attr_def_asgn, imm_mem, imm_mem_asgn
   * @param attributeAssignType1
   */
  public void setAttributeAssignType(String attributeAssignType1) {
    this.attributeAssignType = attributeAssignType1;
  }

  /**
   * generally: assign
   */
  private String attributeAssignAction;

  
  
  /**
   * generally: assign
   * @return action
   */
  public String getAttributeAssignAction() {
    return this.attributeAssignAction;
  }

  /**
   * generally: assign
   * @param attributeAssignAction1
   */
  public void setAttributeAssignAction(String attributeAssignAction1) {
    this.attributeAssignAction = attributeAssignAction1;
  }

  /**
   * id of attribute assign action
   */
  private String attributeAssignActionId;
  
  /**
   * id of attribute assign action
   * @return id
   */
  public String getAttributeAssignActionId() {
    return this.attributeAssignActionId;
  }

  /**
   * id of attribute assign action
   * @param attributeAssignActionId1
   */
  public void setAttributeAssignActionId(String attributeAssignActionId1) {
    this.attributeAssignActionId = attributeAssignActionId1;
  }

  /**
   * T or F for permissions
   */
  private String disallowed;

  /**
   * T or F for permissions
   * @return T or F
   */
  public String getDisallowed() {
    return this.disallowed;
  }

  /**
   * T or F for permissions
   * @param disallowed1
   */
  public void setDisallowed(String disallowed1) {
    this.disallowed = disallowed1;
  }

  

}
