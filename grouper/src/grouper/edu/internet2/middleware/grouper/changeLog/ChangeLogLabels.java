/*
 * @author mchyzer
 * $Id: ChangeLogLabels.java,v 1.5 2009-10-31 17:46:47 shilen Exp $
 */
package edu.internet2.middleware.grouper.changeLog;


/**
 *
 */
public class ChangeLogLabels {

  /**
   * label for group type add
   */
  public static enum GROUP_TYPE_ADD implements ChangeLogLabel {

    /** id of the group type */
    id, 
    
    /** name */
    name;
  };
  
  /** labels for a group type update */
  public static enum GROUP_TYPE_UPDATE implements ChangeLogLabel {
    
    /** id */
    id, 
    
    /** name */
    name, 
    
    /** property which changed */
    propertyChanged, 
    
    /** old value of the property */
    propertyOldValue,

    /** new value of the property */
    propertyNewValue;
  };
      
  /**
   * labels for group type delete
   */
  public static enum GROUP_TYPE_DELETE implements ChangeLogLabel {
    
    /** id of the group type */
    id,
    
    /** name */
    name;
  };

  /**
   * label for group add
   */
  public static enum GROUP_ADD implements ChangeLogLabel {

    /** id of the group */
    id, 
    
    /** name */
    name,
    
    /** parent stem id */
    parentStemId,
    
    /** display name */
    displayName,
    
    /** description */
    description;
  };
  
  /** labels for a group update */
  public static enum GROUP_UPDATE implements ChangeLogLabel {
    
    /** id */
    id, 
    
    /** name */
    name, 
    
    /** parent stem id */
    parentStemId,
    
    /** display name */
    displayName,
    
    /** display extension (not stored in col, but yes stored in diffs on update */
    displayExtension,
    
    /** description */
    description,
    
    /** property which changed */
    propertyChanged, 
    
    /** old value of the property */
    propertyOldValue,
    
    /** new value of the property */
    propertyNewValue;
  };
      
  /**
   * labels for group delete
   */
  public static enum GROUP_DELETE implements ChangeLogLabel {
    
    /** id of the group */
    id,
    
    /** name */
    name,
    
    /** parent stem id */
    parentStemId, 

    /** display name */
    displayName, 
    
    /** description */
    description;
  };
  
  /**
   * label for group field add
   */
  public static enum GROUP_FIELD_ADD implements ChangeLogLabel {

    /** id of the group type */
    id, 
    
    /** name */
    name,
    
    /** group type id */
    groupTypeId,
    
    /** group type name */
    groupTypeName, 
    
    /** type */
    type;
  };
  
  /** labels for a group field update */
  public static enum GROUP_FIELD_UPDATE implements ChangeLogLabel {
    
    /** id */
    id, 
    
    /** name */
    name, 
    
    /** groupTypeId */
    groupTypeId,
    
    /** groupTypeName */
    groupTypeName, 
    
    /** type */
    type,
    
    /** readPrivilege */
    readPrivilege,
    
    /** writePrivilege */
    writePrivilege,
    
    /** isNullable */
    isNullable,
    
    /** property which changed */
    propertyChanged, 
    
    /** old value of the property */
    propertyOldValue,

    /** new value of the property */
    propertyNewValue;
  };
  
  /**
   * labels for group field delete
   */
  public static enum GROUP_FIELD_DELETE implements ChangeLogLabel {
    
    /** id of the group type */
    id,
    
    /** name */
    name,
    
    /** groupTypeId */
    groupTypeId,
    
    /** groupTypeName */
    groupTypeName,
    
    /** type */
    type;
  
  };

  /**
   * label for attribute def add
   */
  public static enum ATTRIBUTE_DEF_ADD implements ChangeLogLabel {
  
    /** id of the attribute def */
    id, 
    
    /** name */
    name,
    
    /** stem id */
    stemId,
    
    /** description */
    description;
  }

  /** labels for a attribute def update */
  public static enum ATTRIBUTE_DEF_UPDATE implements ChangeLogLabel {
    
    /** id */
    id, 
    
    /** name */
    name, 
    
    /** stem id */
    stemId,
    
    /** description */
    description,
    
    /** property which changed */
    propertyChanged, 
    
    /** old value of the property */
    propertyOldValue,
    
    /** new value of the property */
    propertyNewValue;
  };

  /**
   * labels for attribute def delete
   */
  public static enum ATTRIBUTE_DEF_DELETE implements ChangeLogLabel {
    
    /** id of the attribute def */
    id,
    
    /** name */
    name,
    
    /** stem id */
    stemId,
    
    /** description */
    description;
  };
  
  /**
   * label for stem add
   */
  public static enum STEM_ADD implements ChangeLogLabel {
  
    /** id of the stem */
    id, 
    
    /** name */
    name,
    
    /** parent stem id */
    parentStemId,
    
    /** display name */
    displayName,
    
    /** description */
    description;
  }

  /** labels for a stem update */
  public static enum STEM_UPDATE implements ChangeLogLabel {
    
    /** id */
    id, 
    
    /** name */
    name, 
    
    /** parent stem id */
    parentStemId,
    
    /** display name */
    displayName,
    
    /** display extension (not stored in col, but yes stored in diffs on update */
    displayExtension,
    
    /** description */
    description,
    
    /** property which changed */
    propertyChanged, 
    
    /** old value of the property */
    propertyOldValue,
    
    /** new value of the property */
    propertyNewValue;
  };

  /**
   * labels for stem delete
   */
  public static enum STEM_DELETE implements ChangeLogLabel {
    
    /** id of the stem */
    id,
    
    /** name */
    name,
    
    /** parent stem id */
    parentStemId, 
  
    /** display name */
    displayName, 
    
    /** description */
    description;
  };

  /**
   * 
   * label for membership add
   */
  public static enum MEMBERSHIP_ADD implements ChangeLogLabel {
  
    /** id of the stem */
    id, 
    
    /** fieldName */
    fieldName,
    
    /** subjectId */
    subjectId,
    
    /** sourceId */
    sourceId,
    
    /** membershipType */
    membershipType,

    /** groupId */
    groupId,
    
    /** groupName */
    groupName,
    
    /** member id */
    memberId,
    
    /** field id */
    fieldId;
  };

  /** labels for a membership update */
  public static enum MEMBERSHIP_UPDATE implements ChangeLogLabel {
    
    /** id of the stem */
    id, 
    
    /** fieldName */
    fieldName,
    
    /** subjectId */
    subjectId,
    
    /** sourceId */
    sourceId,
    
    /** membershipType */
    membershipType,

    /** groupId */
    groupId,
    
    /** groupName */
    groupName,
    
    /** property which changed */
    propertyChanged, 
    
    /** old value of the property */
    propertyOldValue,
    
    /** new value of the property */
    propertyNewValue;
  };

  /**
   * labels for membership delete
   */
  public static enum MEMBERSHIP_DELETE implements ChangeLogLabel {
    
    /** id of the stem */
    id, 
    
    /** fieldName */
    fieldName,
    
    /** subjectId */
    subjectId,
    
    /** sourceId */
    sourceId,
    
    /** membershipType */
    membershipType,

    /** groupId */
    groupId,
    
    /** groupName */
    groupName,
    
    /** member id */
    memberId,
    
    /** field id */
    fieldId;
  };
  
  /**
   * labels for privilege add
   */
  public static enum PRIVILEGE_ADD implements ChangeLogLabel {
    
    /** id of membership */
    id, 
    
    /** privilegeName */
    privilegeName,
    
    /** subjectId */
    subjectId,
    
    /** sourceId */
    sourceId,
    
    /** privilegeType */
    privilegeType,
    
    /** ownerType */
    ownerType,

    /** ownerId */
    ownerId,
    
    /** ownerName */
    ownerName,
    
    /** member id */
    memberId,
    
    /** field id */
    fieldId,
    
    /** membershipType */
    membershipType;
  };
  
  /**
   * labels for privilege update
   */
  public static enum PRIVILEGE_UPDATE implements ChangeLogLabel {
    
    /** id of membership */
    id, 
    
    /** privilegeName */
    privilegeName,
    
    /** subjectId */
    subjectId,
    
    /** sourceId */
    sourceId,
    
    /** privilegeType */
    privilegeType,
    
    /** ownerType */
    ownerType,

    /** ownerId */
    ownerId,
    
    /** ownerName */
    ownerName,
    
    /** membershipType */
    membershipType;
  };
  
  /**
   * labels for privilege delete
   */
  public static enum PRIVILEGE_DELETE implements ChangeLogLabel {
    
    /** id of membership */
    id, 
    
    /** privilegeName */
    privilegeName,
    
    /** subjectId */
    subjectId,
    
    /** sourceId */
    sourceId,
    
    /** privilegeType */
    privilegeType,
    
    /** ownerType */
    ownerType,

    /** ownerId */
    ownerId,
    
    /** ownerName */
    ownerName,
    
    /** member id */
    memberId,
    
    /** field id */
    fieldId,
    
    /** membershipType */
    membershipType;
  };
  
  /**
   * labels for member add
   */
  public static enum MEMBER_ADD implements ChangeLogLabel {
    
    /** id */
    id, 
    
    /** subjectId */
    subjectId, 
    
    /** subjectSourceId */
    subjectSourceId,
    
    /** subjectTypeId */
    subjectTypeId;
  };
  
  /**
   * labels for member delete
   */
  public static enum MEMBER_DELETE implements ChangeLogLabel {
    
    /** id */
    id, 
    
    /** subjectId */
    subjectId, 
    
    /** subjectSourceId */
    subjectSourceId,
    
    /** subjectTypeId */
    subjectTypeId;
  };
  
  /**
   * labels for member update
   */
  public static enum MEMBER_UPDATE implements ChangeLogLabel {
    
    /** id */
    id, 
    
    /** subjectId */
    subjectId, 
    
    /** subjectSourceId */
    subjectSourceId,
    
    /** subjectTypeId */
    subjectTypeId,
    
    /** property which changed */
    propertyChanged, 
    
    /** old value of the property */
    propertyOldValue,
    
    /** new value of the property */
    propertyNewValue;
  };
  
  /**
   * labels for group type assign
   */
  public static enum GROUP_TYPE_ASSIGN implements ChangeLogLabel {
    
    /** id */
    id, 
    
    /** groupId */
    groupId,
    
    /** groupName */
    groupName,
    
    /** typeId */
    typeId,
    
    /** typeName */
    typeName;
  };
  
  /**
   * labels for group type unassign
   */
  public static enum GROUP_TYPE_UNASSIGN implements ChangeLogLabel {
    
    /** id */
    id, 
    
    /** groupId */
    groupId,
    
    /** groupName */
    groupName,
    
    /** typeId */
    typeId,
    
    /** typeName */
    typeName;
  };
}
