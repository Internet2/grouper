/*
 * @author mchyzer
 * $Id: ChangeLogTypeBuiltin.java,v 1.7 2009-06-11 05:47:02 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.changeLog;



/**
 *
 */
public enum ChangeLogTypeBuiltin implements ChangeLogTypeIdentifier {

  /**
   * add group type
   */
  GROUP_TYPE_ADD(new ChangeLogType("groupType", "addGroupType", ChangeLogLabels.GROUP_TYPE_ADD.id, 
      ChangeLogLabels.GROUP_TYPE_ADD.name)),
  
  /**
   * update group type
   */
  GROUP_TYPE_UPDATE(new ChangeLogType("groupType", "updateGroupType", ChangeLogLabels.GROUP_TYPE_UPDATE.id, 
      ChangeLogLabels.GROUP_TYPE_UPDATE.name, ChangeLogLabels.GROUP_TYPE_UPDATE.propertyChanged, 
      ChangeLogLabels.GROUP_TYPE_UPDATE.propertyOldValue, ChangeLogLabels.GROUP_TYPE_UPDATE.propertyNewValue)),
  
  /**
   * delete group type
   */
  GROUP_TYPE_DELETE(new ChangeLogType("groupType", "deleteGroupType", 
      ChangeLogLabels.GROUP_TYPE_DELETE.id, ChangeLogLabels.GROUP_TYPE_DELETE.name)),
  
  /**
   * add group field
   */
  GROUP_FIELD_ADD(new ChangeLogType("groupField", "addGroupField", ChangeLogLabels.GROUP_FIELD_ADD.id, 
      ChangeLogLabels.GROUP_FIELD_ADD.name, ChangeLogLabels.GROUP_FIELD_ADD.groupTypeId, 
      ChangeLogLabels.GROUP_FIELD_ADD.groupTypeName, ChangeLogLabels.GROUP_FIELD_ADD.type)),
  
  /**
   * update group field
   */
  GROUP_FIELD_UPDATE(new ChangeLogType("groupField", "updateGroupField", ChangeLogLabels.GROUP_FIELD_UPDATE.id, 
      ChangeLogLabels.GROUP_FIELD_UPDATE.name, ChangeLogLabels.GROUP_FIELD_UPDATE.groupTypeId, 
      ChangeLogLabels.GROUP_FIELD_UPDATE.groupTypeName, ChangeLogLabels.GROUP_FIELD_UPDATE.type,
      ChangeLogLabels.GROUP_FIELD_UPDATE.propertyChanged, 
      ChangeLogLabels.GROUP_FIELD_UPDATE.propertyOldValue, ChangeLogLabels.GROUP_FIELD_UPDATE.propertyNewValue)),
  
  /**
   * delete group field
   */
  GROUP_FIELD_DELETE(new ChangeLogType("groupField", "deleteGroupField", ChangeLogLabels.GROUP_FIELD_DELETE.id, 
      ChangeLogLabels.GROUP_FIELD_DELETE.name, ChangeLogLabels.GROUP_FIELD_DELETE.groupTypeId, 
      ChangeLogLabels.GROUP_FIELD_DELETE.groupTypeName, ChangeLogLabels.GROUP_FIELD_DELETE.type)),
  
  /**
   * add group attribute
   */
  GROUP_ATTRIBUTE_ADD(new ChangeLogType("groupAttribute", "addGroupAttribute", "id", "name", "groupId", "groupName", "fieldId", "fieldName", "value")),
  
  /**
   * update group attribute
   */
  GROUP_ATTRIBUTE_UPDATE(new ChangeLogType("groupAttribute", "updateGroupAttribute", "id", "name", "groupId", "groupName", "fieldId", "fieldName", "value", "oldValue")),

  /**
   * delete group attribute
   */
  GROUP_ATTRIBUTE_DELETE(new ChangeLogType("groupAttribute", "deleteGroupAttribute", "id", "name", "groupId", "groupName", "fieldId", "fieldName", "value")),

  /**
   * add group composite
   */
  GROUP_COMPOSITE_ADD(new ChangeLogType("groupComposite", "addGroupComposite", "id", "ownerId", "ownerName", "leftFactorId", "leftFactorName", "rightFactorId", "rightFactorName", "type")),
  
  /**
   * update group composite
   */
  GROUP_COMPOSITE_UPDATE(new ChangeLogType("groupComposite", "updateGroupComposite", "id", "ownerId", "ownerName", "leftFactorId", "leftFactorName", "rightFactorId", "rightFactorName", "type")),
  
  /**
   * delete group composite
   */
  GROUP_COMPOSITE_DELETE(new ChangeLogType("groupComposite", "deleteGroupComposite", "id", "ownerId", "ownerName", "leftFactorId", "leftFactorName", "rightFactorId", "rightFactorName", "type")),
  
  /**
   * assign group type
   */
  GROUP_TYPE_ASSIGN(new ChangeLogType("groupTypeAssignment", "assignGroupType", "id", "groupId", "groupName", "typeId", "typeName")),
  
  /**
   * unassign group type
   */
  GROUP_TYPE_UNASSIGN(new ChangeLogType("groupTypeAssignment", "unassignGroupType", "id", "groupId", "groupName", "typeId", "typeName")),

  /**
   * add membership
   */
  MEMBERSHIP_ADD(new ChangeLogType("membership", "addMembership", 
      ChangeLogLabels.MEMBERSHIP_ADD.id, ChangeLogLabels.MEMBERSHIP_ADD.fieldName,  
      ChangeLogLabels.MEMBERSHIP_ADD.subjectId, 
      ChangeLogLabels.MEMBERSHIP_ADD.sourceId, ChangeLogLabels.MEMBERSHIP_ADD.membershipType, 
      ChangeLogLabels.MEMBERSHIP_ADD.groupId, ChangeLogLabels.MEMBERSHIP_ADD.groupName)),
  
  /**
   * update membership
   */
  MEMBERSHIP_UPDATE(new ChangeLogType("membership", "updateMembership", 
      ChangeLogLabels.MEMBERSHIP_UPDATE.id, ChangeLogLabels.MEMBERSHIP_UPDATE.fieldName,  
      ChangeLogLabels.MEMBERSHIP_UPDATE.subjectId, 
      ChangeLogLabels.MEMBERSHIP_UPDATE.sourceId, ChangeLogLabels.MEMBERSHIP_UPDATE.membershipType, 
      ChangeLogLabels.MEMBERSHIP_UPDATE.groupId, ChangeLogLabels.MEMBERSHIP_UPDATE.groupName,
      ChangeLogLabels.MEMBERSHIP_UPDATE.propertyChanged, ChangeLogLabels.MEMBERSHIP_UPDATE.propertyOldValue, 
      ChangeLogLabels.MEMBERSHIP_UPDATE.propertyNewValue)),

  /**
   * delete membership
   */
  MEMBERSHIP_DELETE(new ChangeLogType("membership", "deleteMembership", 
      ChangeLogLabels.MEMBERSHIP_DELETE.id, ChangeLogLabels.MEMBERSHIP_DELETE.fieldName,  
      ChangeLogLabels.MEMBERSHIP_DELETE.subjectId, 
      ChangeLogLabels.MEMBERSHIP_DELETE.sourceId, ChangeLogLabels.MEMBERSHIP_DELETE.membershipType, 
      ChangeLogLabels.MEMBERSHIP_DELETE.groupId, ChangeLogLabels.MEMBERSHIP_DELETE.groupName)),

  /**
   * add privilege
   */
  PRIVILEGE_ADD(new ChangeLogType("privilege", "addPrivilege", "privilegeName", "memberId", "privilegeType", "ownerType", "ownerId", "ownerName")),
  
  /**
   * update privilege
   */
  PRIVILEGE_UPDATE(new ChangeLogType("privilege", "updatePrivilege", null,"privilegeName", "memberId", "privilegeType", "ownerType", "ownerId", "ownerName")),

  /**
   * delete privilege
   */
  PRIVILEGE_DELETE(new ChangeLogType("privilege", "deletePrivilege", "privilegeName", "memberId", "privilegeType", "ownerType", "ownerId", "ownerName")),

  /**
   * add group
   */
  GROUP_ADD(new ChangeLogType("group", "addGroup", 
      ChangeLogLabels.GROUP_ADD.id, ChangeLogLabels.GROUP_ADD.name, ChangeLogLabels.GROUP_ADD.parentStemId,
      ChangeLogLabels.GROUP_ADD.displayName, ChangeLogLabels.GROUP_ADD.description)),
  
  /**
   * update group
   */
  GROUP_UPDATE(new ChangeLogType("group", "updateGroup", 
      ChangeLogLabels.GROUP_UPDATE.id, ChangeLogLabels.GROUP_UPDATE.name, ChangeLogLabels.GROUP_UPDATE.parentStemId,
      ChangeLogLabels.GROUP_UPDATE.displayName, ChangeLogLabels.GROUP_UPDATE.description, 
      ChangeLogLabels.GROUP_UPDATE.propertyChanged, ChangeLogLabels.GROUP_UPDATE.propertyOldValue, 
      ChangeLogLabels.GROUP_UPDATE.propertyNewValue)),
  
  /**
   * delete group
   */
  GROUP_DELETE(new ChangeLogType("group", "deleteGroup", 
      ChangeLogLabels.GROUP_DELETE.id, ChangeLogLabels.GROUP_DELETE.name, ChangeLogLabels.GROUP_DELETE.parentStemId,
      ChangeLogLabels.GROUP_DELETE.displayName, ChangeLogLabels.GROUP_DELETE.description)),
  
  /**
   * stem add
   */
  STEM_ADD(new ChangeLogType("stem", "addStem", 
      ChangeLogLabels.STEM_ADD.id, ChangeLogLabels.STEM_ADD.name, ChangeLogLabels.STEM_ADD.parentStemId,
      ChangeLogLabels.STEM_ADD.displayName, ChangeLogLabels.STEM_ADD.description)),

  /**
   * stem update
   */
  STEM_UPDATE(new ChangeLogType("stem", "updateStem", 
      ChangeLogLabels.STEM_UPDATE.id, ChangeLogLabels.STEM_UPDATE.name, ChangeLogLabels.STEM_UPDATE.parentStemId,
      ChangeLogLabels.STEM_UPDATE.displayName, ChangeLogLabels.STEM_UPDATE.description, 
      ChangeLogLabels.STEM_UPDATE.propertyChanged, ChangeLogLabels.STEM_UPDATE.propertyOldValue, 
      ChangeLogLabels.STEM_UPDATE.propertyNewValue)),
  
  /**
   * stem delete
   */
  STEM_DELETE(new ChangeLogType("stem", "deleteStem", 
      ChangeLogLabels.STEM_DELETE.id, ChangeLogLabels.STEM_DELETE.name, ChangeLogLabels.STEM_DELETE.parentStemId,
      ChangeLogLabels.STEM_DELETE.displayName, ChangeLogLabels.STEM_DELETE.description)),
  
  /**
   * member change subject
   */
  MEMBER_CHANGE_SUBJECT(new ChangeLogType("member", "changeSubject", "oldMemberId", "oldSubjectId", "oldSourceId", "newMemberId", "newSubjectId", "newSourceId", "deleteOldMember", "memberIdChanged")),
  
  /**
   * copy a group to another stem
   */
  GROUP_COPY(new ChangeLogType("group", "copy", "attributes", "oldGroupUuid", "oldGroupName", "newGroupUuid", "newGroupName", 
      "privilegesOfGroup", "groupAsPrivilege", "listMembersOfGroup", "listGroupAsMember")),
  
  /**
   * move a group to another stem
   */
  GROUP_MOVE(new ChangeLogType("group", "move", "groupUuid", "oldGroupName", "newGroupName", "newStemUuid", 
      "assignAlternateName")),
  
  /**
   * copy a stem to another stem
   */
  STEM_COPY(new ChangeLogType("stem", "copy", "attributes", "oldStemUuid", "oldStemName", "newStemName", "newStemUuid", 
      "privilegesOfStem", "privilegesOfGroup", "listMembersOfGroup", "listGroupAsMember")),
  
  /**
   * move a stem to another stem
   */
  STEM_MOVE(new ChangeLogType("stem", "move", "stemUuid", "oldStemName", "newStemName", "newParentStemUuid", 
      "assignAlternateName"));
  
  /**
   * defaults for audit type, though doesnt hold the id
   */
  private ChangeLogType internalChangeLogTypeDefault;
  
  /**
   * construct
   * @param theInternalChangeLogTypeDefault 
   */
  private ChangeLogTypeBuiltin(ChangeLogType theInternalChangeLogTypeDefault) {
    this.internalChangeLogTypeDefault = theInternalChangeLogTypeDefault;
  }
  
  /**
   * get the audit type from the enum
   * @return the audit type
   */
  public ChangeLogType getChangeLogType() {
    return ChangeLogTypeFinder.find(this.internalChangeLogTypeDefault.getChangeLogCategory(), 
        this.internalChangeLogTypeDefault.getActionName(), true);
  }

  /**
   * get the defaults, but not the id
   * @return the defaults
   */
  public ChangeLogType internal_changeLogTypeDefault() {
    return this.internalChangeLogTypeDefault;
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.changeLog.ChangeLogTypeIdentifier#getChangeLogCategory()
   */
  public String getChangeLogCategory() {
    return this.getChangeLogType().getChangeLogCategory();
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.changeLog.ChangeLogTypeIdentifier#getActionName()
   */
  public String getActionName() {
    return this.getChangeLogType().getActionName();
  }

  /**
   * 
   */
  public static void internal_clearCache() {
    
    //set this to -1 so it will be an insert next time
    for (ChangeLogTypeBuiltin changeLogTypeBuiltin : ChangeLogTypeBuiltin.values()) {
      changeLogTypeBuiltin.internalChangeLogTypeDefault.setHibernateVersionNumber(-1l);
    }
  }
}
