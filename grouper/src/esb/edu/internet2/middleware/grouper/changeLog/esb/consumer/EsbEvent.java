/*
 * @author Rob Hebron
 */

package edu.internet2.middleware.grouper.changeLog.esb.consumer;

/**
 * 
 * Simple class to hold changelog event details for dispatch to external system. If an event
 * does not contain a value for a property it will be null
 *
 */
public class EsbEvent {

  public String eventType;

  public String id;

  public String name;

  public String description;

  public String displayExtension;

  public String displayName;

  public String fieldName;

  public String groupId;

  public String groupName;

  public String groupTypeId;

  public String groupTypeName;

  public String membershipType;

  public String ownerId;

  public String ownerName;

  public String ownerType;

  public String parentStemId;

  public String privilegeName;

  public String privilegeType;

  public String propertyChanged;

  public String propertyNewValue;

  public String propertyOldValue;

  public String readPrivilege;

  public String sourceId;

  public String subjectId;

  public String type;

  public String writePrivilege;

  private String[][] subjectAttributes;

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
      newArray[this.subjectAttributes.length + 1][0] = attributeName;
      newArray[this.subjectAttributes.length + 1][1] = attributeValue;
      this.setSubjectAttributes(newArray);
    }
  }
}
