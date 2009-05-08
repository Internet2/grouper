/*
 * @author mchyzer
 * $Id: ChangeLogTypeBuiltin.java,v 1.1 2009-05-08 05:28:10 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.changeLog;



/**
 *
 */
public enum ChangeLogTypeBuiltin implements ChangeLogTypeIdentifier {

  /**
   * add group type
   */
  GROUP_TYPE_ADD(new ChangeLogType("groupType", "addGroupType", null, "id", "name")),
  
  /**
   * update group type
   */
  GROUP_TYPE_UPDATE(new ChangeLogType("groupType", "updateGroupType", null, "id", "name")),
  
  /**
   * delete group type
   */
  GROUP_TYPE_DELETE(new ChangeLogType("groupType", "deleteGroupType", null, "id", "name")),
  
  /**
   * add group field
   */
  GROUP_FIELD_ADD(new ChangeLogType("groupField", "addGroupField", null, "id", "name", 
      "groupTypeId", "groupTypeName", "type")),
  
  /**
   * update group field
   */
  GROUP_FIELD_UPDATE(new ChangeLogType("groupField", "updateGroupField", null, "id", "name", 
      "groupTypeId", "groupTypeName", "type")),
  
  /**
   * delete group field
   */
  GROUP_FIELD_DELETE(new ChangeLogType("groupField", "deleteGroupField", null, "id", "name", "groupTypeId", "groupTypeName", "type")),
  
  /**
   * add group attribute
   */
  GROUP_ATTRIBUTE_ADD(new ChangeLogType("groupAttribute", "addGroupAttribute", null, "id", "name", "groupId", "groupName", "fieldId", "fieldName", "value")),
  
  /**
   * update group attribute
   */
  GROUP_ATTRIBUTE_UPDATE(new ChangeLogType("groupAttribute", "updateGroupAttribute", null, "id", "name", "groupId", "groupName", "fieldId", "fieldName", "value", "oldValue")),

  /**
   * delete group attribute
   */
  GROUP_ATTRIBUTE_DELETE(new ChangeLogType("groupAttribute", "deleteGroupAttribute", null, "id", "name", "groupId", "groupName", "fieldId", "fieldName", "value")),

  /**
   * add group composite
   */
  GROUP_COMPOSITE_ADD(new ChangeLogType("groupComposite", "addGroupComposite", null, "id", "ownerId", "ownerName", "leftFactorId", "leftFactorName", "rightFactorId", "rightFactorName", "type")),
  
  /**
   * update group composite
   */
  GROUP_COMPOSITE_UPDATE(new ChangeLogType("groupComposite", "updateGroupComposite", null, "id", "ownerId", "ownerName", "leftFactorId", "leftFactorName", "rightFactorId", "rightFactorName", "type")),
  
  /**
   * delete group composite
   */
  GROUP_COMPOSITE_DELETE(new ChangeLogType("groupComposite", "deleteGroupComposite", null, "id", "ownerId", "ownerName", "leftFactorId", "leftFactorName", "rightFactorId", "rightFactorName", "type")),
  
  /**
   * assign group type
   */
  GROUP_TYPE_ASSIGN(new ChangeLogType("groupTypeAssignment", "assignGroupType", null, "id", "groupId", "groupName", "typeId", "typeName")),
  
  /**
   * unassign group type
   */
  GROUP_TYPE_UNASSIGN(new ChangeLogType("groupTypeAssignment", "unassignGroupType", null, "id", "groupId", "groupName", "typeId", "typeName")),

  /**
   * add membership
   */
  MEMBERSHIP_ADD(new ChangeLogType("membership", "addMembership", null, "id", "fieldId", "fieldName", "memberId", "membershipType", "ownerType", "ownerId", "ownerName")),
  
  /**
   * update membership
   */
  MEMBERSHIP_UPDATE(new ChangeLogType("membership", "updateMembership", null, "id", "fieldId", "fieldName", "memberId", "membershipType", "ownerType", "ownerId", "ownerName")),

  /**
   * delete membership
   */
  MEMBERSHIP_DELETE(new ChangeLogType("membership", "deleteMembership", null, "id", "fieldId", "fieldName", "memberId", "membershipType", "ownerType", "ownerId", "ownerName")),

  /**
   * add privilege
   */
  PRIVILEGE_ADD(new ChangeLogType("privilege", "addPrivilege", null, "privilegeName", "memberId", "privilegeType", "ownerType", "ownerId", "ownerName")),
  
  /**
   * update privilege
   */
  PRIVILEGE_UPDATE(new ChangeLogType("privilege", "updatePrivilege", null,"privilegeName", "memberId", "privilegeType", "ownerType", "ownerId", "ownerName")),

  /**
   * delete privilege
   */
  PRIVILEGE_DELETE(new ChangeLogType("privilege", "deletePrivilege", null, "privilegeName", "memberId", "privilegeType", "ownerType", "ownerId", "ownerName")),

  /**
   * add group
   */
  GROUP_ADD(new ChangeLogType("group", "addGroup", null, "id", "name", "parentStemId", "displayName", "description")),
  
  /**
   * update group
   */
  GROUP_UPDATE(new ChangeLogType("group", "updateGroup", null, "id", "name", "parentStemId", "displayName", "description")),
  
  /**
   * delete group
   */
  GROUP_DELETE(new ChangeLogType("group", "deleteGroup", null, "id", "name", "parentStemId", "displayName", "description")),
  
  /**
   * stem add
   */
  STEM_ADD(new ChangeLogType("stem", "addStem", null, "id", "name", "parentStemId", "displayName", "description")),

  /**
   * stem update
   */
  STEM_UPDATE(new ChangeLogType("stem", "updateStem", null, "id", "name", "parentStemId", "displayName", "description")),
  
  /**
   * stem delete
   */
  STEM_DELETE(new ChangeLogType("stem", "deleteStem", null, "id", "name", "parentStemId", "displayName", "description")),
  
  /**
   * member change subject
   */
  MEMBER_CHANGE_SUBJECT(new ChangeLogType("member", "changeSubject", null, "oldMemberId", "oldSubjectId", "oldSourceId", "newMemberId", "newSubjectId", "newSourceId", "deleteOldMember", "memberIdChanged")),
  
  /**
   * copy a group to another stem
   */
  GROUP_COPY(new ChangeLogType("group", "copy", "attributes", "oldGroupUuid", "oldGroupName", "newGroupUuid", "newGroupName", 
      "privilegesOfGroup", "groupAsPrivilege", "listMembersOfGroup", "listGroupAsMember")),
  
  /**
   * move a group to another stem
   */
  GROUP_MOVE(new ChangeLogType("group", "move", null, "groupUuid", "oldGroupName", "newGroupName", "newStemUuid", 
      "assignAlternateName")),
  
  /**
   * copy a stem to another stem
   */
  STEM_COPY(new ChangeLogType("stem", "copy", "attributes", "oldStemUuid", "oldStemName", "newStemName", "newStemUuid", 
      "privilegesOfStem", "privilegesOfGroup", "listMembersOfGroup", "listGroupAsMember")),
  
  /**
   * move a stem to another stem
   */
  STEM_MOVE(new ChangeLogType("stem", "move", null, "stemUuid", "oldStemName", "newStemName", "newParentStemUuid", 
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
