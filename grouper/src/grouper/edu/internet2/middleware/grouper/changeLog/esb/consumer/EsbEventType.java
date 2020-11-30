package edu.internet2.middleware.grouper.changeLog.esb.consumer;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.changeLog.ChangeLogEntry;
import edu.internet2.middleware.grouper.changeLog.ChangeLogLabel;
import edu.internet2.middleware.grouper.changeLog.ChangeLogLabels;
import edu.internet2.middleware.grouper.privs.PrivilegeType;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * type of event
 * @author mchyzer
 *
 */
public enum EsbEventType {

  /** ATTRIBUTE_ASSIGN_ADD event */
  ATTRIBUTE_ASSIGN_ADD {

    @Override
    public void processChangeLogEntry(EsbEventContainer esbEventContainer,
        ChangeLogEntry changeLogEntry) {

      EsbEvent event = esbEventContainer.getEsbEvent();

      event.setEventType(EsbEventType.ATTRIBUTE_ASSIGN_ADD.name());
      event.setId(retrieveLabelValue(changeLogEntry, ChangeLogLabels.ATTRIBUTE_ASSIGN_ADD.id));
      event.setAttributeDefNameId(retrieveLabelValue(changeLogEntry, ChangeLogLabels.ATTRIBUTE_ASSIGN_ADD.attributeDefNameId));
      event.setAttributeAssignActionId(retrieveLabelValue(changeLogEntry, ChangeLogLabels.ATTRIBUTE_ASSIGN_ADD.attributeAssignActionId));
      event.setAttributeAssignType(retrieveLabelValue(changeLogEntry, ChangeLogLabels.ATTRIBUTE_ASSIGN_ADD.assignType));
      event.setOwnerId(retrieveLabelValue(changeLogEntry, ChangeLogLabels.ATTRIBUTE_ASSIGN_ADD.ownerId1));
      event.setOwnerId2(retrieveLabelValue(changeLogEntry, ChangeLogLabels.ATTRIBUTE_ASSIGN_ADD.ownerId2));
      event.setAttributeDefNameName(retrieveLabelValue(changeLogEntry, ChangeLogLabels.ATTRIBUTE_ASSIGN_ADD.attributeDefNameName));
      event.setAttributeAssignAction(retrieveLabelValue(changeLogEntry, ChangeLogLabels.ATTRIBUTE_ASSIGN_ADD.action));
      event.setDisallowed(retrieveLabelValue(changeLogEntry, ChangeLogLabels.ATTRIBUTE_ASSIGN_ADD.disallowed));
      
    }
  }, 
  
  /** ATTRIBUTE_ASSIGN_DELETE event */
  ATTRIBUTE_ASSIGN_DELETE {

    @Override
    public void processChangeLogEntry(EsbEventContainer esbEventContainer,
        ChangeLogEntry changeLogEntry) {

      EsbEvent event = esbEventContainer.getEsbEvent();

      event.setEventType(EsbEventType.ATTRIBUTE_ASSIGN_DELETE.name());
      event.setId(retrieveLabelValue(changeLogEntry, ChangeLogLabels.ATTRIBUTE_ASSIGN_DELETE.id));
      event.setAttributeDefNameId(retrieveLabelValue(changeLogEntry, ChangeLogLabels.ATTRIBUTE_ASSIGN_DELETE.attributeDefNameId));
      event.setAttributeAssignActionId(retrieveLabelValue(changeLogEntry, ChangeLogLabels.ATTRIBUTE_ASSIGN_DELETE.attributeAssignActionId));
      event.setAttributeAssignType(retrieveLabelValue(changeLogEntry, ChangeLogLabels.ATTRIBUTE_ASSIGN_DELETE.assignType));
      event.setOwnerId(retrieveLabelValue(changeLogEntry, ChangeLogLabels.ATTRIBUTE_ASSIGN_DELETE.ownerId1));
      event.setOwnerId2(retrieveLabelValue(changeLogEntry, ChangeLogLabels.ATTRIBUTE_ASSIGN_DELETE.ownerId2));
      event.setAttributeDefNameName(retrieveLabelValue(changeLogEntry, ChangeLogLabels.ATTRIBUTE_ASSIGN_DELETE.attributeDefNameName));
      event.setAttributeAssignAction(retrieveLabelValue(changeLogEntry, ChangeLogLabels.ATTRIBUTE_ASSIGN_DELETE.action));
      event.setDisallowed(retrieveLabelValue(changeLogEntry, ChangeLogLabels.ATTRIBUTE_ASSIGN_DELETE.disallowed));
      
    }
  }, 
  
  /** ATTRIBUTE_ASSIGN_VALUE_ADD event */
  ATTRIBUTE_ASSIGN_VALUE_ADD {

    @Override
    public void processChangeLogEntry(EsbEventContainer esbEventContainer,
        ChangeLogEntry changeLogEntry) {

      EsbEvent event = esbEventContainer.getEsbEvent();

      event.setEventType(EsbEventType.ATTRIBUTE_ASSIGN_VALUE_ADD.name());
      event.setId(retrieveLabelValue(changeLogEntry, ChangeLogLabels.ATTRIBUTE_ASSIGN_VALUE_ADD.id));
      event.setAttributeAssignId(retrieveLabelValue(changeLogEntry, ChangeLogLabels.ATTRIBUTE_ASSIGN_VALUE_ADD.attributeAssignId));
      event.setAttributeDefNameId(retrieveLabelValue(changeLogEntry, ChangeLogLabels.ATTRIBUTE_ASSIGN_VALUE_ADD.attributeDefNameId));
      event.setAttributeDefNameName(retrieveLabelValue(changeLogEntry, ChangeLogLabels.ATTRIBUTE_ASSIGN_VALUE_ADD.attributeDefNameName));
      event.setPropertyNewValue(retrieveLabelValue(changeLogEntry, ChangeLogLabels.ATTRIBUTE_ASSIGN_VALUE_ADD.value));
      event.setValueType(retrieveLabelValue(changeLogEntry, ChangeLogLabels.ATTRIBUTE_ASSIGN_VALUE_ADD.valueType));
      
    }
  }, 
  
  /** ATTRIBUTE_ASSIGN_VALUE_DELETE event */
  ATTRIBUTE_ASSIGN_VALUE_DELETE {

    @Override
    public void processChangeLogEntry(EsbEventContainer esbEventContainer,
        ChangeLogEntry changeLogEntry) {

      EsbEvent event = esbEventContainer.getEsbEvent();

      event.setEventType(EsbEventType.ATTRIBUTE_ASSIGN_VALUE_DELETE.name());
      event.setId(retrieveLabelValue(changeLogEntry, ChangeLogLabels.ATTRIBUTE_ASSIGN_VALUE_DELETE.id));
      event.setAttributeAssignId(retrieveLabelValue(changeLogEntry, ChangeLogLabels.ATTRIBUTE_ASSIGN_VALUE_DELETE.attributeAssignId));
      event.setAttributeDefNameId(retrieveLabelValue(changeLogEntry, ChangeLogLabels.ATTRIBUTE_ASSIGN_VALUE_DELETE.attributeDefNameId));
      event.setAttributeDefNameName(retrieveLabelValue(changeLogEntry, ChangeLogLabels.ATTRIBUTE_ASSIGN_VALUE_DELETE.attributeDefNameName));
      event.setPropertyNewValue(retrieveLabelValue(changeLogEntry, ChangeLogLabels.ATTRIBUTE_ASSIGN_VALUE_DELETE.value));
      event.setValueType(retrieveLabelValue(changeLogEntry, ChangeLogLabels.ATTRIBUTE_ASSIGN_VALUE_DELETE.valueType));
      
    }
  }, 
  
  /** ENTITY_ADD event */
  ENTITY_ADD {

    @Override
    public void processChangeLogEntry(EsbEventContainer esbEventContainer,
        ChangeLogEntry changeLogEntry) {

      EsbEvent event = esbEventContainer.getEsbEvent();

      event.setEventType(EsbEventType.ENTITY_ADD.name());
      event.setId(retrieveLabelValue(changeLogEntry, ChangeLogLabels.ENTITY_ADD.id));
      event.setName(retrieveLabelValue(changeLogEntry, ChangeLogLabels.ENTITY_ADD.name));
      event.setParentStemId(retrieveLabelValue(changeLogEntry,
          ChangeLogLabels.ENTITY_ADD.parentStemId));
      event.setDisplayName(retrieveLabelValue(changeLogEntry,
          ChangeLogLabels.ENTITY_ADD.displayName));
      event.setDescription(retrieveLabelValue(changeLogEntry,
          ChangeLogLabels.ENTITY_ADD.description));

    }
  }, 
  
  /** ENTITY_DELETE event */
  ENTITY_DELETE {

    @Override
    public void processChangeLogEntry(EsbEventContainer esbEventContainer,
        ChangeLogEntry changeLogEntry) {

      EsbEvent event = esbEventContainer.getEsbEvent();

      event.setEventType(EsbEventType.ENTITY_DELETE.name());
      event
      .setId(retrieveLabelValue(changeLogEntry, ChangeLogLabels.ENTITY_DELETE.id));
      event.setName(retrieveLabelValue(changeLogEntry,
          ChangeLogLabels.ENTITY_DELETE.name));
      event.setParentStemId(retrieveLabelValue(changeLogEntry,
          ChangeLogLabels.ENTITY_DELETE.parentStemId));
      event.setDisplayName(retrieveLabelValue(changeLogEntry,
          ChangeLogLabels.ENTITY_DELETE.displayName));
      event.setDescription(retrieveLabelValue(changeLogEntry,
          ChangeLogLabels.ENTITY_DELETE.description));
      
    }
  },
  
  /** ENTITY_UPDATE event */
  ENTITY_UPDATE {

    @Override
    public void processChangeLogEntry(EsbEventContainer esbEventContainer,
        ChangeLogEntry changeLogEntry) {


      EsbEvent event = esbEventContainer.getEsbEvent();

      event.setEventType(EsbEventType.ENTITY_UPDATE.name());
      event
      .setId(retrieveLabelValue(changeLogEntry, ChangeLogLabels.ENTITY_UPDATE.id));
      event.setName(retrieveLabelValue(changeLogEntry,
          ChangeLogLabels.ENTITY_UPDATE.name));
      event.setParentStemId(retrieveLabelValue(changeLogEntry,
          ChangeLogLabels.ENTITY_UPDATE.parentStemId));
      event.setDisplayName(retrieveLabelValue(changeLogEntry,
          ChangeLogLabels.ENTITY_UPDATE.displayName));
      event.setDisplayExtension(retrieveLabelValue(changeLogEntry,
          ChangeLogLabels.ENTITY_UPDATE.displayExtension));
      event.setDescription(retrieveLabelValue(changeLogEntry,
          ChangeLogLabels.ENTITY_UPDATE.description));
      event.setPropertyChanged(retrieveLabelValue(changeLogEntry,
          ChangeLogLabels.ENTITY_UPDATE.propertyChanged));
      event.setPropertyOldValue(retrieveLabelValue(changeLogEntry,
          ChangeLogLabels.ENTITY_UPDATE.propertyOldValue));
      event.setPropertyNewValue(retrieveLabelValue(changeLogEntry,
          ChangeLogLabels.ENTITY_UPDATE.propertyNewValue));
      
    }
  },

  /** GROUP_ADD event */
  GROUP_ADD {

    @Override
    public void processChangeLogEntry(EsbEventContainer esbEventContainer,
        ChangeLogEntry changeLogEntry) {
      
      EsbEvent event = esbEventContainer.getEsbEvent();
      
      event.setEventType(EsbEventType.GROUP_ADD.name());
      event.setId(retrieveLabelValue(changeLogEntry, ChangeLogLabels.GROUP_ADD.id));
      event.setGroupId(retrieveLabelValue(changeLogEntry, ChangeLogLabels.GROUP_ADD.id));
      event.setName(retrieveLabelValue(changeLogEntry, ChangeLogLabels.GROUP_ADD.name));
      event.setGroupName(retrieveLabelValue(changeLogEntry, ChangeLogLabels.GROUP_ADD.name));
      event.setParentStemId(retrieveLabelValue(changeLogEntry, ChangeLogLabels.GROUP_ADD.parentStemId));
      event.setDisplayName(retrieveLabelValue(changeLogEntry, ChangeLogLabels.GROUP_ADD.displayName));
      event.setDescription(retrieveLabelValue(changeLogEntry, ChangeLogLabels.GROUP_ADD.description));
            
    }
  }, 
  
  /** GROUP_DELETE event */
  GROUP_DELETE {

    @Override
    public void processChangeLogEntry(EsbEventContainer esbEventContainer,
        ChangeLogEntry changeLogEntry) {

      EsbEvent event = esbEventContainer.getEsbEvent();

      event.setEventType(EsbEventType.GROUP_DELETE.name());
      event.setId(retrieveLabelValue(changeLogEntry, ChangeLogLabels.GROUP_DELETE.id));
      event.setGroupId(retrieveLabelValue(changeLogEntry, ChangeLogLabels.GROUP_DELETE.id));
      event.setName(retrieveLabelValue(changeLogEntry,ChangeLogLabels.GROUP_DELETE.name));
      event.setGroupName(retrieveLabelValue(changeLogEntry,ChangeLogLabels.GROUP_DELETE.name));
      event.setParentStemId(retrieveLabelValue(changeLogEntry,
          ChangeLogLabels.GROUP_DELETE.parentStemId));
      event.setDisplayName(retrieveLabelValue(changeLogEntry,
          ChangeLogLabels.GROUP_DELETE.displayName));
      event.setDescription(retrieveLabelValue(changeLogEntry,
          ChangeLogLabels.GROUP_DELETE.description));
      
    }
  },

  /** GROUP_FIELD_ADD event */
  GROUP_FIELD_ADD {

    @Override
    public void processChangeLogEntry(EsbEventContainer esbEventContainer,
        ChangeLogEntry changeLogEntry) {

      EsbEvent event = esbEventContainer.getEsbEvent();

      event.setEventType(EsbEventType.GROUP_FIELD_ADD.name());
      event.setId(retrieveLabelValue(changeLogEntry,
          ChangeLogLabels.GROUP_FIELD_ADD.id));
      event.setName(retrieveLabelValue(changeLogEntry,
          ChangeLogLabels.GROUP_FIELD_ADD.name));
      event.setGroupTypeId(retrieveLabelValue(changeLogEntry,
          ChangeLogLabels.GROUP_FIELD_ADD.groupTypeId));
      event.setGroupTypeName(retrieveLabelValue(changeLogEntry,
          ChangeLogLabels.GROUP_FIELD_ADD.groupTypeName));
      event.setType(retrieveLabelValue(changeLogEntry,
          ChangeLogLabels.GROUP_FIELD_ADD.type));
      
    }
  },
  
  /** GROUP_FIELD_DELETE event */
  GROUP_FIELD_DELETE {

    @Override
    public void processChangeLogEntry(EsbEventContainer esbEventContainer,
        ChangeLogEntry changeLogEntry) {

      EsbEvent event = esbEventContainer.getEsbEvent();

      event.setEventType(EsbEventType.GROUP_FIELD_DELETE.name());
      event.setId(retrieveLabelValue(changeLogEntry,
          ChangeLogLabels.GROUP_FIELD_DELETE.id));
      event.setName(retrieveLabelValue(changeLogEntry,
          ChangeLogLabels.GROUP_FIELD_DELETE.name));
      event.setGroupTypeId(retrieveLabelValue(changeLogEntry,
          ChangeLogLabels.GROUP_FIELD_DELETE.groupTypeId));
      event.setGroupTypeName(retrieveLabelValue(changeLogEntry,
          ChangeLogLabels.GROUP_FIELD_DELETE.groupTypeName));
      event.setType(retrieveLabelValue(changeLogEntry,
          ChangeLogLabels.GROUP_FIELD_DELETE.type));
      
    }
  },
  
  /** GROUP_FIELD_UPDATE event */
  GROUP_FIELD_UPDATE {

    @Override
    public void processChangeLogEntry(EsbEventContainer esbEventContainer,
        ChangeLogEntry changeLogEntry) {


      EsbEvent event = esbEventContainer.getEsbEvent();

      event.setEventType(EsbEventType.GROUP_FIELD_UPDATE.name());
      event.setId(retrieveLabelValue(changeLogEntry,
          ChangeLogLabels.GROUP_FIELD_UPDATE.id));
      event.setName(retrieveLabelValue(changeLogEntry,
          ChangeLogLabels.GROUP_FIELD_UPDATE.name));
      event.setGroupTypeId(retrieveLabelValue(changeLogEntry,
          ChangeLogLabels.GROUP_FIELD_UPDATE.groupTypeId));
      event.setGroupTypeName(retrieveLabelValue(changeLogEntry,
          ChangeLogLabels.GROUP_FIELD_UPDATE.groupTypeName));
      event.setType(retrieveLabelValue(changeLogEntry,
          ChangeLogLabels.GROUP_FIELD_UPDATE.type));
      event.setReadPrivilege(retrieveLabelValue(changeLogEntry,
          ChangeLogLabels.GROUP_FIELD_UPDATE.readPrivilege));
      event.setWritePrivilege(retrieveLabelValue(changeLogEntry,
          ChangeLogLabels.GROUP_FIELD_UPDATE.writePrivilege));
      event.setPropertyChanged(retrieveLabelValue(changeLogEntry,
          ChangeLogLabels.GROUP_FIELD_UPDATE.propertyChanged));
      event.setPropertyOldValue(retrieveLabelValue(changeLogEntry,
          ChangeLogLabels.GROUP_FIELD_UPDATE.propertyOldValue));
      event.setPropertyNewValue(retrieveLabelValue(changeLogEntry,
          ChangeLogLabels.GROUP_FIELD_UPDATE.propertyNewValue));
      
    }
  },

  /** GROUP_TYPE_ADD event */
  GROUP_TYPE_ADD {

    @Override
    public void processChangeLogEntry(EsbEventContainer esbEventContainer,
        ChangeLogEntry changeLogEntry) {
      
    }
  },

  /** GROUP_TYPE_DELETE event */
  GROUP_TYPE_DELETE {

    @Override
    public void processChangeLogEntry(EsbEventContainer esbEventContainer,
        ChangeLogEntry changeLogEntry) {
      
    }
  },

  /** GROUP_TYPE_UPDATE event */
  GROUP_TYPE_UPDATE {

    @Override
    public void processChangeLogEntry(EsbEventContainer esbEventContainer,
        ChangeLogEntry changeLogEntry) {
      
    }
  },

  /** GROUP_UPDATE event */
  GROUP_UPDATE {

    @Override
    public void processChangeLogEntry(EsbEventContainer esbEventContainer,
        ChangeLogEntry changeLogEntry) {


      EsbEvent event = esbEventContainer.getEsbEvent();

      event.setEventType(EsbEventType.GROUP_UPDATE.name());
      event.setId(retrieveLabelValue(changeLogEntry, ChangeLogLabels.GROUP_UPDATE.id));
      event.setGroupId(retrieveLabelValue(changeLogEntry, ChangeLogLabels.GROUP_UPDATE.id));
      event.setName(retrieveLabelValue(changeLogEntry, ChangeLogLabels.GROUP_UPDATE.name));
      event.setGroupName(retrieveLabelValue(changeLogEntry, ChangeLogLabels.GROUP_UPDATE.name));
      event.setParentStemId(retrieveLabelValue(changeLogEntry,
          ChangeLogLabels.GROUP_UPDATE.parentStemId));
      event.setDisplayName(retrieveLabelValue(changeLogEntry,
          ChangeLogLabels.GROUP_UPDATE.displayName));
      event.setDisplayExtension(retrieveLabelValue(changeLogEntry,
          ChangeLogLabels.GROUP_UPDATE.displayExtension));
      event.setDescription(retrieveLabelValue(changeLogEntry,
          ChangeLogLabels.GROUP_UPDATE.description));
      event.setPropertyChanged(retrieveLabelValue(changeLogEntry,
          ChangeLogLabels.GROUP_UPDATE.propertyChanged));
      event.setPropertyOldValue(retrieveLabelValue(changeLogEntry,
          ChangeLogLabels.GROUP_UPDATE.propertyOldValue));
      event.setPropertyNewValue(retrieveLabelValue(changeLogEntry,
          ChangeLogLabels.GROUP_UPDATE.propertyNewValue));
      
      
    }
  },

  /** MEMBERSHIP_ADD event */
  MEMBERSHIP_ADD {

    @Override
    public void processChangeLogEntry(EsbEventContainer esbEventContainer,
        ChangeLogEntry changeLogEntry) {


      EsbEvent event = esbEventContainer.getEsbEvent();

      event.setEventType(EsbEventType.MEMBERSHIP_ADD.name());
      // throws error
      event.setId(retrieveLabelValue(changeLogEntry,
          ChangeLogLabels.MEMBERSHIP_ADD.id));
      event.setFieldName(retrieveLabelValue(changeLogEntry,
          ChangeLogLabels.MEMBERSHIP_ADD.fieldName));
      event.setSubjectId(retrieveLabelValue(changeLogEntry,
          ChangeLogLabels.MEMBERSHIP_ADD.subjectId));
      event.setSourceId(retrieveLabelValue(changeLogEntry,
          ChangeLogLabels.MEMBERSHIP_ADD.sourceId));
      // throws error
      event.setMembershipType(retrieveLabelValue(changeLogEntry,
          ChangeLogLabels.MEMBERSHIP_ADD.membershipType));
      event.setGroupId(retrieveLabelValue(changeLogEntry,
          ChangeLogLabels.MEMBERSHIP_ADD.groupId));
      event.setGroupName(retrieveLabelValue(changeLogEntry,
          ChangeLogLabels.MEMBERSHIP_ADD.groupName));
      event.setMemberId(retrieveLabelValue(changeLogEntry,
          ChangeLogLabels.MEMBERSHIP_ADD.memberId));
      event.setFieldId(retrieveLabelValue(changeLogEntry,
          ChangeLogLabels.MEMBERSHIP_ADD.fieldId));
      
    }
  },

  /** MEMBERSHIP_DELETE event */
  MEMBERSHIP_DELETE {

    @Override
    public void processChangeLogEntry(EsbEventContainer esbEventContainer,
        ChangeLogEntry changeLogEntry) {

      EsbEvent event = esbEventContainer.getEsbEvent();

      event.setEventType(EsbEventType.MEMBERSHIP_DELETE.name());
      event.setId(retrieveLabelValue(changeLogEntry,
          ChangeLogLabels.MEMBERSHIP_DELETE.id));
      event.setFieldName(retrieveLabelValue(changeLogEntry,
          ChangeLogLabels.MEMBERSHIP_DELETE.fieldName));
      event.setSubjectId(retrieveLabelValue(changeLogEntry,
          ChangeLogLabels.MEMBERSHIP_DELETE.subjectId));
      event.setSourceId(retrieveLabelValue(changeLogEntry,
          ChangeLogLabels.MEMBERSHIP_DELETE.sourceId));
      event.setMembershipType(retrieveLabelValue(changeLogEntry,
          ChangeLogLabels.MEMBERSHIP_DELETE.membershipType));
      event.setGroupId(retrieveLabelValue(changeLogEntry,
          ChangeLogLabels.MEMBERSHIP_DELETE.groupId));
      event.setGroupName(retrieveLabelValue(changeLogEntry,
          ChangeLogLabels.MEMBERSHIP_DELETE.groupName));
      event.setMemberId(retrieveLabelValue(changeLogEntry,
          ChangeLogLabels.MEMBERSHIP_DELETE.memberId));
      event.setFieldId(retrieveLabelValue(changeLogEntry,
          ChangeLogLabels.MEMBERSHIP_DELETE.fieldId));

      
    }
  },

  /** MEMBERSHIP_UPDATE event */
  MEMBERSHIP_UPDATE {

    @Override
    public void processChangeLogEntry(EsbEventContainer esbEventContainer,
        ChangeLogEntry changeLogEntry) {


      EsbEvent event = esbEventContainer.getEsbEvent();

      event.setEventType(EsbEventType.MEMBERSHIP_UPDATE.name());
      event.setId(retrieveLabelValue(changeLogEntry,
          ChangeLogLabels.MEMBERSHIP_UPDATE.id));
      event.setFieldName(retrieveLabelValue(changeLogEntry,
          ChangeLogLabels.MEMBERSHIP_UPDATE.fieldName));
      event.setSubjectId(retrieveLabelValue(changeLogEntry,
          ChangeLogLabels.MEMBERSHIP_UPDATE.subjectId));
      event.setSourceId(retrieveLabelValue(changeLogEntry,
          ChangeLogLabels.MEMBERSHIP_UPDATE.sourceId));
      event.setMembershipType(retrieveLabelValue(changeLogEntry,
          ChangeLogLabels.MEMBERSHIP_UPDATE.membershipType));
      event.setGroupId(retrieveLabelValue(changeLogEntry,
          ChangeLogLabels.MEMBERSHIP_UPDATE.groupId));
      event.setGroupName(retrieveLabelValue(changeLogEntry,
          ChangeLogLabels.MEMBERSHIP_UPDATE.groupName));
      event.setPropertyChanged(retrieveLabelValue(changeLogEntry,
          ChangeLogLabels.MEMBERSHIP_UPDATE.propertyChanged));
      event.setPropertyOldValue(retrieveLabelValue(changeLogEntry,
          ChangeLogLabels.MEMBERSHIP_UPDATE.propertyOldValue));
      event.setPropertyNewValue(retrieveLabelValue(changeLogEntry,
          ChangeLogLabels.MEMBERSHIP_UPDATE.propertyNewValue));
      event.setMemberId(retrieveLabelValue(changeLogEntry,
          ChangeLogLabels.MEMBERSHIP_UPDATE.memberId));
      event.setFieldId(retrieveLabelValue(changeLogEntry,
          ChangeLogLabels.MEMBERSHIP_UPDATE.fieldId));

    }
  },

  /** MEMBER_ADD event */
  MEMBER_ADD {

    @Override
    public void processChangeLogEntry(EsbEventContainer esbEventContainer,
        ChangeLogEntry changeLogEntry) {


      EsbEvent event = esbEventContainer.getEsbEvent();

      event.setEventType(EsbEventType.MEMBER_ADD.name());
      // throws error
      event.setId(retrieveLabelValue(changeLogEntry,
          ChangeLogLabels.MEMBER_ADD.id));
      event.setSubjectId(retrieveLabelValue(changeLogEntry,
          ChangeLogLabels.MEMBER_ADD.subjectId));
      event.setSourceId(retrieveLabelValue(changeLogEntry,
          ChangeLogLabels.MEMBER_ADD.subjectSourceId));
      event.setSubjectIdentifier0(retrieveLabelValue(changeLogEntry,
          ChangeLogLabels.MEMBER_ADD.subjectIdentifier0));
      
    }
  },

  /** MEMBER_DELETE event */
  MEMBER_DELETE {

    @Override
    public void processChangeLogEntry(EsbEventContainer esbEventContainer,
        ChangeLogEntry changeLogEntry) {


      EsbEvent event = esbEventContainer.getEsbEvent();

      event.setEventType(EsbEventType.MEMBER_DELETE.name());
      // throws error
      event.setId(retrieveLabelValue(changeLogEntry,
          ChangeLogLabels.MEMBER_ADD.id));
      event.setSubjectId(retrieveLabelValue(changeLogEntry,
          ChangeLogLabels.MEMBER_ADD.subjectId));
      event.setSourceId(retrieveLabelValue(changeLogEntry,
          ChangeLogLabels.MEMBER_ADD.subjectSourceId));
      event.setSubjectIdentifier0(retrieveLabelValue(changeLogEntry,
          ChangeLogLabels.MEMBER_ADD.subjectIdentifier0));
      
    }
  },

  /** MEMBER_UPDATE event */
  MEMBER_UPDATE {

    @Override
    public void processChangeLogEntry(EsbEventContainer esbEventContainer,
        ChangeLogEntry changeLogEntry) {


      EsbEvent event = esbEventContainer.getEsbEvent();

      event.setEventType(EsbEventType.MEMBER_UPDATE.name());
      // throws error
      event.setId(retrieveLabelValue(changeLogEntry,
          ChangeLogLabels.MEMBER_ADD.id));
      event.setSubjectId(retrieveLabelValue(changeLogEntry,
          ChangeLogLabels.MEMBER_ADD.subjectId));
      event.setSourceId(retrieveLabelValue(changeLogEntry,
          ChangeLogLabels.MEMBER_ADD.subjectSourceId));
      event.setSubjectIdentifier0(retrieveLabelValue(changeLogEntry,
          ChangeLogLabels.MEMBER_ADD.subjectIdentifier0));
      
    }
  },

  /** PRIVILEGE_ADD event */
  PRIVILEGE_ADD {

    @Override
    public void processChangeLogEntry(EsbEventContainer esbEventContainer,
        ChangeLogEntry changeLogEntry) {


      EsbEvent event = esbEventContainer.getEsbEvent();

      event.setEventType(EsbEventType.PRIVILEGE_ADD.name());
      // next line throws error, so removed
      //event.setId(retrieveLabelValue(changeLogEntry, ChangeLogLabels.PRIVILEGE_ADD.id));
      event.setPrivilegeName(retrieveLabelValue(changeLogEntry,
          ChangeLogLabels.PRIVILEGE_ADD.privilegeName));
      event.setSubjectId(retrieveLabelValue(changeLogEntry,
          ChangeLogLabels.PRIVILEGE_ADD.subjectId));
      event.setSourceId(retrieveLabelValue(changeLogEntry,
          ChangeLogLabels.PRIVILEGE_ADD.sourceId));
      event.setPrivilegeType(retrieveLabelValue(changeLogEntry,
          ChangeLogLabels.PRIVILEGE_ADD.privilegeType));
      event.setOwnerType(retrieveLabelValue(changeLogEntry,
          ChangeLogLabels.PRIVILEGE_ADD.ownerType));
      event.setOwnerId(retrieveLabelValue(changeLogEntry,
          ChangeLogLabels.PRIVILEGE_ADD.ownerId));
      event.setOwnerName(retrieveLabelValue(changeLogEntry,
          ChangeLogLabels.PRIVILEGE_ADD.ownerName));
      
      if (PrivilegeType.ACCESS.name().toLowerCase().equals(event.getPrivilegeType())) {
        event.setGroupId(event.getOwnerId());
        event.setGroupName(event.getOwnerName());
      }
      
      
    }
  },

  /** PRIVILEGE_DELETE event */
  PRIVILEGE_DELETE {

    @Override
    public void processChangeLogEntry(EsbEventContainer esbEventContainer,
        ChangeLogEntry changeLogEntry) {


      EsbEvent event = esbEventContainer.getEsbEvent();

      event.setEventType(EsbEventType.PRIVILEGE_DELETE.name());
      event.setId(retrieveLabelValue(changeLogEntry,
          ChangeLogLabels.PRIVILEGE_DELETE.id));
      event.setPrivilegeName(retrieveLabelValue(changeLogEntry,
          ChangeLogLabels.PRIVILEGE_DELETE.privilegeName));
      event.setSubjectId(retrieveLabelValue(changeLogEntry,
          ChangeLogLabels.PRIVILEGE_DELETE.subjectId));
      event.setSourceId(retrieveLabelValue(changeLogEntry,
          ChangeLogLabels.PRIVILEGE_DELETE.sourceId));
      event.setPrivilegeType(retrieveLabelValue(changeLogEntry,
          ChangeLogLabels.PRIVILEGE_DELETE.privilegeType));
      event.setOwnerType(retrieveLabelValue(changeLogEntry,
          ChangeLogLabels.PRIVILEGE_DELETE.ownerType));
      event.setOwnerId(retrieveLabelValue(changeLogEntry,
          ChangeLogLabels.PRIVILEGE_DELETE.ownerId));
      event.setOwnerName(retrieveLabelValue(changeLogEntry,
          ChangeLogLabels.PRIVILEGE_DELETE.ownerName));

      if (PrivilegeType.ACCESS.name().toLowerCase().equals(event.getPrivilegeType())) {
        event.setGroupId(event.getOwnerId());
        event.setGroupName(event.getOwnerName());
      }

    }
  },

  /** PRIVILEGE_UPDATE event */
  PRIVILEGE_UPDATE {

    @Override
    public void processChangeLogEntry(EsbEventContainer esbEventContainer,
        ChangeLogEntry changeLogEntry) {


      EsbEvent event = esbEventContainer.getEsbEvent();


      event.setEventType(EsbEventType.PRIVILEGE_UPDATE.name());
      event.setId(retrieveLabelValue(changeLogEntry,
          ChangeLogLabels.PRIVILEGE_UPDATE.id));
      event.setPrivilegeName(retrieveLabelValue(changeLogEntry,
          ChangeLogLabels.PRIVILEGE_UPDATE.privilegeName));
      event.setSubjectId(retrieveLabelValue(changeLogEntry,
          ChangeLogLabels.PRIVILEGE_UPDATE.subjectId));
      event.setSourceId(retrieveLabelValue(changeLogEntry,
          ChangeLogLabels.PRIVILEGE_UPDATE.sourceId));
      event.setPrivilegeType(retrieveLabelValue(changeLogEntry,
          ChangeLogLabels.PRIVILEGE_UPDATE.privilegeType));
      event.setOwnerType(retrieveLabelValue(changeLogEntry,
          ChangeLogLabels.PRIVILEGE_UPDATE.ownerType));
      event.setOwnerId(retrieveLabelValue(changeLogEntry,
          ChangeLogLabels.PRIVILEGE_UPDATE.ownerId));
      event.setOwnerName(retrieveLabelValue(changeLogEntry,
          ChangeLogLabels.PRIVILEGE_UPDATE.ownerName));
      
      if (PrivilegeType.ACCESS.name().toLowerCase().equals(event.getPrivilegeType())) {
        event.setGroupId(event.getOwnerId());
        event.setGroupName(event.getOwnerName());
      }
    }
  },

  /** STEM_ADD event */
  STEM_ADD {

    @Override
    public void processChangeLogEntry(EsbEventContainer esbEventContainer,
        ChangeLogEntry changeLogEntry) {


      EsbEvent event = esbEventContainer.getEsbEvent();

      event.setEventType(EsbEventType.STEM_ADD.name());
      event.setId(retrieveLabelValue(changeLogEntry, ChangeLogLabels.STEM_ADD.id));
      event
      .setName(retrieveLabelValue(changeLogEntry, ChangeLogLabels.STEM_ADD.name));
      event.setParentStemId(retrieveLabelValue(changeLogEntry,
          ChangeLogLabels.STEM_ADD.parentStemId));
      event.setDisplayName(retrieveLabelValue(changeLogEntry,
          ChangeLogLabels.STEM_ADD.displayName));
      event.setDescription(retrieveLabelValue(changeLogEntry,
          ChangeLogLabels.STEM_ADD.description));
      
    }
  },

  /** STEM_DELETE event */
  STEM_DELETE {

    @Override
    public void processChangeLogEntry(EsbEventContainer esbEventContainer,
        ChangeLogEntry changeLogEntry) {


      EsbEvent event = esbEventContainer.getEsbEvent();

      event.setEventType(EsbEventType.STEM_DELETE.name());
      event.setId(retrieveLabelValue(changeLogEntry, ChangeLogLabels.STEM_DELETE.id));
      event.setName(retrieveLabelValue(changeLogEntry,
          ChangeLogLabels.STEM_DELETE.name));
      event.setParentStemId(retrieveLabelValue(changeLogEntry,
          ChangeLogLabels.STEM_DELETE.parentStemId));
      event.setDisplayName(retrieveLabelValue(changeLogEntry,
          ChangeLogLabels.STEM_DELETE.displayName));
      event.setDescription(retrieveLabelValue(changeLogEntry,
          ChangeLogLabels.STEM_DELETE.description));
      
    }
  },

  /** STEM_UPDATE event */
  STEM_UPDATE {

    @Override
    public void processChangeLogEntry(EsbEventContainer esbEventContainer,
        ChangeLogEntry changeLogEntry) {


      EsbEvent event = esbEventContainer.getEsbEvent();

      event.setEventType(EsbEventType.STEM_UPDATE.name());
      event.setId(retrieveLabelValue(changeLogEntry, ChangeLogLabels.STEM_UPDATE.id));
      event.setName(retrieveLabelValue(changeLogEntry,
          ChangeLogLabels.STEM_UPDATE.name));
      event.setParentStemId(retrieveLabelValue(changeLogEntry,
          ChangeLogLabels.STEM_UPDATE.parentStemId));
      event.setDisplayName(retrieveLabelValue(changeLogEntry,
          ChangeLogLabels.STEM_UPDATE.displayName));
      event.setDisplayExtension(retrieveLabelValue(changeLogEntry,
          ChangeLogLabels.STEM_UPDATE.displayExtension));
      event.setDescription(retrieveLabelValue(changeLogEntry,
          ChangeLogLabels.STEM_UPDATE.description));
      event.setPropertyChanged(retrieveLabelValue(changeLogEntry,
          ChangeLogLabels.STEM_UPDATE.propertyChanged));
      event.setPropertyOldValue(retrieveLabelValue(changeLogEntry,
          ChangeLogLabels.STEM_UPDATE.propertyOldValue));
      event.setPropertyNewValue(retrieveLabelValue(changeLogEntry,
          ChangeLogLabels.STEM_UPDATE.propertyNewValue));
      
    }
  },
  
  /** PERMISSION_CHANGE_ON_SUBJECT event */
  PERMISSION_CHANGE_ON_SUBJECT {

    @Override
    public void processChangeLogEntry(EsbEventContainer esbEventContainer,
        ChangeLogEntry changeLogEntry) {


      EsbEvent event = esbEventContainer.getEsbEvent();

      event.setSubjectId(retrieveLabelValue(changeLogEntry, ChangeLogLabels.PERMISSION_CHANGE_ON_SUBJECT.subjectId));
      event.setSourceId(retrieveLabelValue(changeLogEntry, ChangeLogLabels.PERMISSION_CHANGE_ON_SUBJECT.subjectSourceId));
      event.setRoleId(retrieveLabelValue(changeLogEntry, ChangeLogLabels.PERMISSION_CHANGE_ON_SUBJECT.roleId));
      event.setRoleName(retrieveLabelValue(changeLogEntry, ChangeLogLabels.PERMISSION_CHANGE_ON_SUBJECT.roleName));
      event.setMemberId(retrieveLabelValue(changeLogEntry, ChangeLogLabels.PERMISSION_CHANGE_ON_SUBJECT.memberId));

      event.setGroupId(event.getRoleId());
      event.setGroupName(event.getRoleName());
    }
  };
  
  /** */
  private static final Log LOG = GrouperUtil.getLog(EsbEventType.class);


  /**
   * 
   * @param changeLogEntry
   * @param changeLogLabel
   * @return label value
   */
  private static String retrieveLabelValue(ChangeLogEntry changeLogEntry,
      ChangeLogLabel changeLogLabel) {
    try {
      return changeLogEntry.retrieveValueForLabel(changeLogLabel);
    } catch (Exception e) {
      //cannot get value for label
      if (LOG.isDebugEnabled()) {
        LOG.debug("Cannot get value for label: " + changeLogLabel.name());
      }
      return null;
    }
  }


  public abstract void processChangeLogEntry(EsbEventContainer esbEventContainer, ChangeLogEntry changeLogEntry);
  
  /**
   * do a case-insensitive matching
   * 
   * @param string
   * @param exceptionOnNull will not allow null or blank entries
   * @return the enum or null or exception if not found
   */
  public static EsbEventType valueOfIgnoreCase(String string, boolean exceptionOnNull) {
    return GrouperUtil.enumValueOfIgnoreCase(EsbEventType.class, 
        string, exceptionOnNull);

  }
}