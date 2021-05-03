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
 * @author mchyzer
 * $Id: AuditTypeBuiltin.java,v 1.10 2009-07-17 03:00:29 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.audit;

import java.util.HashMap;
import java.util.Map;

import edu.internet2.middleware.grouperClient.collections.MultiKey;



/**
 *
 */
public enum AuditTypeBuiltin implements AuditTypeIdentifier {

  /**
   * add group type
   */
  GROUP_TYPE_ADD(new AuditType("groupType", "addGroupType", null, "id", "name")),
  
  /**
   * update group type
   */
  GROUP_TYPE_UPDATE(new AuditType("groupType", "updateGroupType", null, "id", "name")),
  
  /**
   * delete group type
   */
  GROUP_TYPE_DELETE(new AuditType("groupType", "deleteGroupType", null, "id", "name")),
  
  /**
   * add group field
   */
  GROUP_FIELD_ADD(new AuditType("groupField", "addGroupField", null, "id", "name", 
      "groupTypeId", "groupTypeName", "type")),
  
  /**
   * update group field
   */
  GROUP_FIELD_UPDATE(new AuditType("groupField", "updateGroupField", null, "id", "name", 
      "groupTypeId", "groupTypeName", "type")),
  
  /**
   * delete group field
   */
  GROUP_FIELD_DELETE(new AuditType("groupField", "deleteGroupField", null, "id", "name", 
      "groupTypeId", "groupTypeName", "type")),
  
  /**
   * add group attribute
   */
  GROUP_ATTRIBUTE_ADD(new AuditType("groupAttribute", "addGroupAttribute", null, "id", 
      "groupId", "groupName", "fieldId", "fieldName", "value")),
  
  /**
   * update group attribute
   */
  GROUP_ATTRIBUTE_UPDATE(new AuditType("groupAttribute", "updateGroupAttribute", null, "id", 
      "groupId", "groupName", "fieldId", "fieldName", "value", "oldValue")),

  /**
   * delete group attribute
   */
  GROUP_ATTRIBUTE_DELETE(new AuditType("groupAttribute", "deleteGroupAttribute", null, "id", 
      "groupId", "groupName", "fieldId", "fieldName", "value")),

  /**
   * add group composite
   */
  GROUP_COMPOSITE_ADD(new AuditType("groupComposite", "addGroupComposite", null, "id", "ownerId", 
      "ownerName", "leftFactorId", "leftFactorName", "rightFactorId", "rightFactorName", "type")),

  /**
   * update group composite
   */
  GROUP_COMPOSITE_UPDATE(new AuditType("groupComposite", "updateGroupComposite", null, "id", "ownerId", 
      "ownerName", "leftFactorId", "leftFactorName", "rightFactorId", "rightFactorName", "type")),
  
  /**
   * delete group composite
   */
  GROUP_COMPOSITE_DELETE(new AuditType("groupComposite", "deleteGroupComposite", null, "id", "ownerId", 
      "ownerName", "leftFactorId", "leftFactorName", "rightFactorId", "rightFactorName", "type")),
  
  /**
   * assign group type
   */
  GROUP_TYPE_ASSIGN(new AuditType("groupTypeAssignment", "assignGroupType", null, "id", "groupId", 
      "groupName", "typeId", "typeName")),
  
  /**
   * unassign group type
   */
  GROUP_TYPE_UNASSIGN(new AuditType("groupTypeAssignment", "unassignGroupType", null, "id", "groupId", 
      "groupName", "typeId", "typeName")),

  /**
   * add group membership
   */
  MEMBERSHIP_GROUP_ADD(new AuditType("membership", "addGroupMembership", null, "id", 
      "fieldId", "fieldName", "memberId", "membershipType", "groupId", "groupName")),
  
  /**
   * update group membership
   */
  MEMBERSHIP_GROUP_UPDATE(new AuditType("membership", "updateGroupMembership", null, "id", 
      "fieldId", "fieldName", "memberId", "membershipType", "groupId", "groupName")),

  /**
   * delete group membership
   */
  MEMBERSHIP_GROUP_DELETE(new AuditType("membership", "deleteGroupMembership", null, "id", 
      "fieldId", "fieldName", "memberId", "membershipType", "groupId", "groupName")),
      
 /**
  * export group membership     
  */
  MEMBERSHIP_GROUP_EXPORT(new AuditType("membership", "exportMembership", null, "groupId", "groupName", 
      "exportSize", "file")),
      
  /**
   * import group membership     
   */
  MEMBERSHIP_GROUP_IMPORT(new AuditType("membership", "importMembership", null, "groupId", "groupName", 
      "file", "totalAdded", "totalDeleted")),
     

  /**
   * add group privilege
   */
  PRIVILEGE_GROUP_ADD(new AuditType("privilege", "addGroupPrivilege", null, "privilegeName", "memberId", 
      "privilegeType", "groupId", "groupName")),
  
  /**
   * update group privilege
   */
  PRIVILEGE_GROUP_UPDATE(new AuditType("privilege", "updateGroupPrivilege", null,"privilegeName", "memberId", 
      "privilegeType", "groupId", "groupName")),

  /**
   * delete group privilege
   */
  PRIVILEGE_GROUP_DELETE(new AuditType("privilege", "deleteGroupPrivilege", null, "privilegeName", "memberId", 
      "privilegeType", "groupId", "groupName")),

  /**
   * add stem privilege
   */
  PRIVILEGE_STEM_ADD(new AuditType("privilege", "addStemPrivilege", null, "privilegeName", "memberId", 
      "privilegeType", "stemId", "stemName")),
  
  /**
   * update stem privilege
   */
  PRIVILEGE_STEM_UPDATE(new AuditType("privilege", "updateStemPrivilege", null,"privilegeName", "memberId", 
      "privilegeType", "stemId", "stemName")),

  /**
   * delete stem privilege
   */
  PRIVILEGE_STEM_DELETE(new AuditType("privilege", "deleteStemPrivilege", null, "privilegeName", "memberId", 
      "privilegeType", "stemId", "stemName")),

  /**
   * add attrDef privilege
   */
  PRIVILEGE_ATTRIBUTE_DEF_ADD(new AuditType("privilege", "addAttributeDefPrivilege", null, "privilegeName", "memberId", 
      "privilegeType", "attributeDefId", "attributeDefName")),
  
  /**
   * update attrDef privilege
   */
  PRIVILEGE_ATTRIBUTE_DEF_UPDATE(new AuditType("privilege", "updateAttributeDefPrivilege", null,"privilegeName", "memberId", 
      "privilegeType", "attributeDefId", "attributeDefName")),

  /**
   * delete attrDef privilege
   */
  PRIVILEGE_ATTRIBUTE_DEF_DELETE(new AuditType("privilege", "deleteAttributeDefPrivilege", null, "privilegeName", "memberId", 
      "privilegeType", "attributeDefId", "attributeDefName")),

  /**
   * add attribute def
   */
  ATTRIBUTE_DEF_ADD(new AuditType("attributeDef", "addAttributeDef", null, 
      "id", "name", "description", "parentStemId")),
  
  /**
   * update attribute def
   */
  ATTRIBUTE_DEF_UPDATE(new AuditType("attributeDef", "updateAttributeDef", null, "id", "name", 
      "description", "parentStemId")),

  /**
   * delete attribute def
   */
  ATTRIBUTE_DEF_DELETE(new AuditType("attributeDef", "deleteAttributeDef", null, "id", "name", 
      "description", "parentStemId")),

  /**
   * 
   */
  ATTRIBUTE_DEF_NAME_ADD(new AuditType("attributeDefName", "addAttributeDefName", null, 
      "id", "name", "displayName", "description", "parentStemId", "parentAttributeDefId", "parentAttributeDefName")),
  
  /**
   * update attribute def
   */
  ATTRIBUTE_DEF_NAME_UPDATE(new AuditType("attributeDefName", "updateAttributeDefName", null, "id", "name", "displayName", 
      "description", "parentStemId", "parentAttributeDefId", "parentAttributeDefName")),

  /**
   * delete attribute def
   */
  ATTRIBUTE_DEF_NAME_DELETE(new AuditType("attributeDefName", "deleteAttributeDefName", null, "id", "name", "displayName", 
      "description", "parentStemId", "parentAttributeDefId", "parentAttributeDefName")),

  /**
   * 
   */
  ATTRIBUTE_ASSIGN_VALUE_ADD(new AuditType("attributeAssignValue", "addAttributeAssignValue", null, 
      "id", "attributeAssignId", "attributeDefNameId", "value", "attributeDefNameName")),
  
  /**
   * update attribute def
   */
  ATTRIBUTE_ASSIGN_VALUE_UPDATE(new AuditType("attributeAssignValue", "updateAttributeAssignValue", null, 
      "id", "attributeAssignId", "attributeDefNameId", "value", "attributeDefNameName")),

  /**
   * delete attribute def
   */
  ATTRIBUTE_ASSIGN_VALUE_DELETE(new AuditType("attributeAssignValue", "deleteAttributeAssignValue", null, 
      "id", "attributeAssignId", "attributeDefNameId", "value", "attributeDefNameName")),


      
  /**
   * 
   */
  ATTRIBUTE_ASSIGN_GROUP_ADD(new AuditType("attributeAssignGroup", "addAttributeAssignGroup", null, 
      "id", "ownerGroupName", "ownerGroupId", "attributeDefNameName", "attributeDefNameId", "action", "attributeDefId")),
  
  /**
   * update group
   */
  ATTRIBUTE_ASSIGN_GROUP_UPDATE(new AuditType("attributeAssignGroup", "updateAttributeAssignGroup", null, 
      "id", "ownerGroupName", "ownerGroupId", "attributeDefNameName", "attributeDefNameId", "action", "attributeDefId")),

  /**
   * delete group
   */
  ATTRIBUTE_ASSIGN_GROUP_DELETE(new AuditType("attributeAssignGroup", "deleteAttributeAssignGroup", null, 
      "id", "ownerGroupName", "ownerGroupId", "attributeDefNameName", "attributeDefNameId", "action", "attributeDefId")),

  /**
   * 
   */
  ATTRIBUTE_ASSIGN_STEM_ADD(new AuditType("attributeAssignStem", "addAttributeAssignStem", null, 
      "id", "ownerStemName", "ownerStemId", "attributeDefNameName", "attributeDefNameId", "action", "attributeDefId")),
  
  /**
   * update stem
   */
  ATTRIBUTE_ASSIGN_STEM_UPDATE(new AuditType("attributeAssignStem", "updateAttributeAssignStem", null, 
      "id", "ownerStemName", "ownerStemId", "attributeDefNameName", "attributeDefNameId", "action", "attributeDefId")),

  /**
   * delete stem
   */
  ATTRIBUTE_ASSIGN_STEM_DELETE(new AuditType("attributeAssignStem", "deleteAttributeAssignStem", null, 
      "id", "ownerStemName", "ownerStemId", "attributeDefNameName", "attributeDefNameId", "action", "attributeDefId")),

  /**
   * 
   */
  ATTRIBUTE_ASSIGN_MEMBER_ADD(new AuditType("attributeAssignMember", "addAttributeAssignMember", null, 
      "id", "ownerSourceId", "ownerSubjectId", "ownerMemberId", "attributeDefNameName", "attributeDefNameId", "action", "attributeDefId")),
  
  /**
   * update member
   */
  ATTRIBUTE_ASSIGN_MEMBER_UPDATE(new AuditType("attributeAssignMember", "updateAttributeAssignMember", null, 
      "id", "ownerSourceId", "ownerSubjectId", "ownerMemberId", "attributeDefNameName", "attributeDefNameId", "action", "attributeDefId")),

  /**
   * delete member
   */
  ATTRIBUTE_ASSIGN_MEMBER_DELETE(new AuditType("attributeAssignMember", "deleteAttributeAssignMember", null, 
      "id", "ownerSourceId", "ownerSubjectId", "ownerMemberId", "attributeDefNameName", "attributeDefNameId", "action", "attributeDefId")),

  /**
   * 
   */
  ATTRIBUTE_ASSIGN_IMMMSHIP_ADD(new AuditType("attributeAssignImmMship", "addAttributeAssignImmMship", null, 
      "id", "ownerMembershipId", "ownerOwnerId", "ownerMemberId", "attributeDefNameName", "attributeDefNameId", "action", "attributeDefId")),
  
  /**
   * update imm mship
   */
  ATTRIBUTE_ASSIGN_IMMMSHIP_UPDATE(new AuditType("attributeAssignImmMship", "updateAttributeAssignImmMship", null, 
      "id", "ownerMembershipId", "ownerOwnerId", "ownerMemberId", "attributeDefNameName", "attributeDefNameId", "action", "attributeDefId")),

  /**
   * delete imm mship
   */
  ATTRIBUTE_ASSIGN_IMMMSHIP_DELETE(new AuditType("attributeAssignImmMship", "deleteAttributeAssignImmMship", null, 
      "id", "ownerMembershipId", "ownerOwnerId", "ownerMemberId", "attributeDefNameName", "attributeDefNameId", "action", "attributeDefId")),

  /**
   * 
   */
  ATTRIBUTE_ASSIGN_ANYMSHIP_ADD(new AuditType("attributeAssignAnyMship", "addAttributeAssignAnyMship", null, 
      "id", "ownerGroupId", "ownerGroupName", "ownerMemberId", "attributeDefNameName", "attributeDefNameId", "action", "attributeDefId")),
  
  /**
   * update any mship
   */
  ATTRIBUTE_ASSIGN_ANYMSHIP_UPDATE(new AuditType("attributeAssignAnyMship", "updateAttributeAssignAnyMship", null, 
      "id", "ownerGroupId", "ownerGroupName", "ownerMemberId", "attributeDefNameName", "attributeDefNameId", "action", "attributeDefId")),

  /**
   * delete any mship
   */
  ATTRIBUTE_ASSIGN_ANYMSHIP_DELETE(new AuditType("attributeAssignAnyMship", "deleteAttributeAssignAnyMship", null, 
      "id", "ownerGroupId", "ownerGroupName", "ownerMemberId", "attributeDefNameName", "attributeDefNameId", "action", "attributeDefId")),

  /**
   * insert attribute def
   */
  ATTRIBUTE_ASSIGN_ATTRDEF_ADD(new AuditType("attributeAssignAttrDef", "addAttributeAssignAttrDef", null, 
      "id", "ownerAttributeDefId", "ownerAttributeDefName", "attributeDefNameName", "attributeDefNameId", "action", "attributeDefId")),
  
  /**
   * update attribute def
   */
  ATTRIBUTE_ASSIGN_ATTRDEF_UPDATE(new AuditType("attributeAssignAttrDef", "updateAttributeAssignAttrDef", null, 
      "id", "ownerAttributeDefId", "ownerAttributeDefName", "attributeDefNameName", "attributeDefNameId", "action", "attributeDefId")),

  /**
   * delete attribute def
   */
  ATTRIBUTE_ASSIGN_ATTRDEF_DELETE(new AuditType("attributeAssignAttrDef", "deleteAttributeAssignAttrDef", null, 
      "id", "ownerAttributeDefId", "ownerAttributeDefName", "attributeDefNameName", "attributeDefNameId", "action", "attributeDefId")),

  /**
   * insert attribute assign
   */
  ATTRIBUTE_ASSIGN_ASSIGN_ADD(new AuditType("attributeAssignAssign", "addAttributeAssignAssign", null, 
      "id", "ownerAttributeAssignId", "attributeDefNameName", "attributeDefNameId", "action", "attributeDefId")),
  
  /**
   * update attribute assign
   */
  ATTRIBUTE_ASSIGN_ASSIGN_UPDATE(new AuditType("attributeAssignAssign", "updateAttributeAssignAssign", null, 
      "id", "ownerAttributeAssignId", "attributeDefNameName", "attributeDefNameId", "action", "attributeDefId")),

  /**
   * delete attribute assign
   */
  ATTRIBUTE_ASSIGN_ASSIGN_DELETE(new AuditType("attributeAssignAssign", "deleteAttributeAssignAssign", null, 
      "id", "ownerAttributeAssignId", "attributeDefNameName", "attributeDefNameId", "action", "attributeDefId")),

  /**
   * externalSubject add
   */
  EXTERNAL_SUBJECT_ADD(new AuditType("externalSubject", "addExternalSubject", null, "id", "name", "identifier")),
  
  /**
   * externalSubject update
   */
  EXTERNAL_SUBJECT_UPDATE(new AuditType("externalSubject", "updateExternalSubject", null, "id", "name", "identifier")),
  
  /**
   * externalSubject delete
   */
  EXTERNAL_SUBJECT_DELETE(new AuditType("externalSubject", "deleteExternalSubject", null, "id", "name", "identifier")),
  
      
  /**
   * externalSubjectAttribute add
   */
  EXTERNAL_SUBJ_ATTR_ADD(new AuditType("externalSubjectAttribute", "addExternalSubjAttr", null, "id", "identifier", "name", "value")),
  
  /**
   * externalSubjectAttribute update
   */
  EXTERNAL_SUBJ_ATTR_UPDATE(new AuditType("externalSubjectAttribute", "updateExternalSubjAttr", null, "id", "identifier", "name", "value")),
  
  /**
   * externalSubjectAttribute delete
   */
  EXTERNAL_SUBJ_ATTR_DELETE(new AuditType("externalSubjectAttribute", "deleteExternalSubjAttr", null, "id", "identifier", "name", "value")),
  
      
      
  /**
   * add group
   */
  GROUP_ADD(new AuditType("group", "addGroup", null, "id", "name", "parentStemId", 
      "displayName", "description")),
  
  /**
   * update group
   */
  GROUP_UPDATE(new AuditType("group", "updateGroup", null, "id", "name", "parentStemId", 
      "displayName", "description")),
  
  /**
   * delete group
   */
  GROUP_DELETE(new AuditType("group", "deleteGroup", null, "id", "name", "parentStemId", 
      "displayName", "description")),
  
  /**
   * delete group
   */
  GROUP_DELETE_ALL_MEMBERSHIPS(new AuditType("group", "deleteAllGroupMemberships", null, "id", "name", "parentStemId", 
      "displayName", "description")),
  
  /**
   * enable group
   */
  GROUP_ENABLE(new AuditType("group", "enableGroup", null, "id", "name", "parentStemId", 
      "displayName", "description")),
  
  /**
   * disable group
   */
  GROUP_DISABLE(new AuditType("group", "disableGroup", null, "id", "name", "parentStemId", 
      "displayName", "description")),
      
  /**
   * add entity
   */
  ENTITY_ADD(new AuditType("entity", "addEntity", null, "id", "name", "parentStemId", 
      "displayName", "description")),
  
  /**
   * update entity
   */
  ENTITY_UPDATE(new AuditType("entity", "updateEntity", null, "id", "name", "parentStemId", 
      "displayName", "description")),
  
  /**
   * delete entity
   */
  ENTITY_DELETE(new AuditType("entity", "deleteEntity", null, "id", "name", "parentStemId", 
      "displayName", "description")),
  
  /**
   * stem add
   */
  STEM_ADD(new AuditType("stem", "addStem", null, "id", "name", "parentStemId", 
      "displayName", "description")),
  
  /**
   * stem update
   */
  STEM_UPDATE(new AuditType("stem", "updateStem", null, "id", "name", "parentStemId", 
      "displayName", "description")),
  
  /**
   * stem delete
   */
  STEM_DELETE(new AuditType("stem", "deleteStem", null, "id", "name", "parentStemId", 
      "displayName", "description")),
  
  /**
   * member change subject
   */
  MEMBER_CHANGE_SUBJECT(new AuditType("member", "changeSubject", null, "oldMemberId", 
      "oldSubjectId", "oldSourceId", "newMemberId", "newSubjectId", "newSourceId", "deleteOldMember", "memberIdChanged")),
  
  /**
   * import from xml
   */
  XML_IMPORT(new AuditType("importExport", "import", null, "fileName", "subjectId")),
  
  
  /**
   * copy a group to another stem
   */
  GROUP_COPY(new AuditType("group", "copy", "attributes", "oldGroupId", "oldGroupName", "newGroupId", "newGroupName", 
      "privilegesOfGroup", "groupAsPrivilege", "listMembersOfGroup", "listGroupAsMember")),
  
  /**
   * move a group to another stem
   */
  GROUP_MOVE(new AuditType("group", "move", null, "groupId", "oldGroupName", "newGroupName", "newStemId", 
      "assignAlternateName")),
  
  /**
   * copy a stem to another stem
   */
  STEM_COPY(new AuditType("stem", "copy", "attributes", "oldStemId", "oldStemName", "newStemName", "newStemId", 
      "privilegesOfStem", "privilegesOfGroup", "listMembersOfGroup", "listGroupAsMember")),
  
  /**
   * move a stem to another stem
   */
  STEM_MOVE(new AuditType("stem", "move", null, "stemId", "oldStemName", "newStemName", "newParentStemId", 
      "assignAlternateName")),
  
  /**
   * register or edit an external subject
   */
  EXTERNAL_SUBJECT_REGISTER_ADD(new AuditType("externalSubjectRegister", "addExternalSubject", "numberOfInvites", "identifier", "sourceId", 
      "subjectId", "inviteEmailSentTo", "groupIdsAssigned", "groupNamesAssigned")),
  
  /**
   * delete an external subject
   */
  EXTERNAL_SUBJECT_REGISTER_DELETE(new AuditType("externalSubjectRegister", "deleteExternalSubject", null, "identifier", 
      "subjectId")),
  
  /**
   * register or edit an external subject
   */
  EXTERNAL_SUBJECT_REGISTER_UPDATE(new AuditType("externalSubjectRegister", "updateExternalSubject", "numberOfInvites", "identifier", "sourceId", 
      "subjectId", "inviteEmailSentTo", "groupIdsAssigned", "groupNamesAssigned")),
  
  /**
   * register or edit an external subject
   */
  EXTERNAL_SUBJECT_INVITE_EMAIL(new AuditType("externalSubjectInvite", "createInviteByEmail", null, "emailsSentTo", "inviterMemberId", 
      "groupIdsAssigned", "groupNamesAssigned")),
  
  /**
   * register or edit an external subject
   */
  EXTERNAL_SUBJECT_INVITE_IDENTIFIER(new AuditType("externalSubjectInvite", "createInviteByIdentifier", null, "identifiers", "inviterMemberId", 
      "groupIdsAssigned", "groupNamesAssigned")),
  
  /**
   * add attestation for group
   */
  GROUP_ATTESTATION_ADD(new AuditType("groupAttestationAdd", "addGroupAttestation", null, "groupId", "groupName")),
  
  /**
   * delete attestation for group
   */
  GROUP_ATTESTATION_DELETE(new AuditType("groupAttestationDelete", "deleteGroupAttestation", null, "groupId", "groupName")),

  /** 
   * update attestation for group
   */
  GROUP_ATTESTATION_UPDATE(new AuditType("groupAttestation", "updateGroupAttestation", null, "groupId", "groupName")),
  
  /**
   * update last certified date for group
   */
  GROUP_ATTESTATION_UPDATE_LAST_CERTIFIED_DATE(new AuditType("groupAttestation", "updateGroupLastCertifiedDate", null, "groupId", "groupName")),

  /**
   * clear last certified date for group
   */
  GROUP_ATTESTATION_CLEAR_LAST_CERTIFIED_DATE(new AuditType("groupAttestation", "clearGroupLastCertifiedDate", null, "groupId", "groupName")),

  /**
   * update last certified date for attr def
   */
  ATTR_DEPROVISIONING_UPDATE_LAST_CERTIFIED_DATE(new AuditType("attrDeprovisioning", "updateAttrDepCertifiedDate", null, "attributeDefId", "attributeDefName")),

  /**
   * clear last certified date for attr def
   */
  ATTR_DEPROVISIONING_CLEAR_LAST_CERTIFIED_DATE(new AuditType("attrDeprovisioning", "clearAttrDepCertifiedDate", null, "attributeDefId", "attributeDefName")),

  /**
   * update last certified date for group
   */
  GROUP_DEPROVISIONING_UPDATE_LAST_CERTIFIED_DATE(new AuditType("groupDeprovisioning", "updateGroupDepCertifiedDate", null, "groupId", "groupName")),

  /**
   * update last certified date for group
   */
  MEMBER_DEPROVISIONING(new AuditType("memberDeprovisioning", "deprovision", null, "memberId", "affiliation")),

  /**
   * clear last certified date for group
   */
  GROUP_DEPROVISIONING_CLEAR_LAST_CERTIFIED_DATE(new AuditType("groupDeprovisioning", "clearGroupDepCertifiedDate", null, "groupId", "groupName")),

  /**
   * update last certified date for stem
   */
  STEM_DEPROVISIONING_UPDATE_LAST_CERTIFIED_DATE(new AuditType("stemDeprovisioning", "updateStemDepCertifiedDate", null, "stemId", "stemName")),

  /**
   * clear last certified date for stem
   */
  STEM_DEPROVISIONING_CLEAR_LAST_CERTIFIED_DATE(new AuditType("stemDeprovisioning", "clearStemDepCertifiedDate", null, "stemId", "stemName")),

  /**
   * add attestation for stem
   */
  STEM_ATTESTATION_ADD(new AuditType("stemAttestation", "addStemAttestation", null, "stemId", "stemName")),

  /**
   * add attestation for stem
   */
  STEM_ATTESTATION_DELETE(new AuditType("stemAttestation", "deleteStemAttestation", null, "stemId", "stemName")),

  /** 
   * update attestation for stem
   */
  STEM_ATTESTATION_UPDATE(new AuditType("stemAttestation", "updateStemAttestation", null, "stemId", "stemName")),

  /**
   * update last certified date for stem
   */
  STEM_ATTESTATION_UPDATE_LAST_CERTIFIED_DATE(new AuditType("stemAttestation", "updateStemLastCertifiedDate", null, "stemId", "stemName")),
  
  /** 
   * USDU member delete
   */
  USDU_MEMBER_DELETE(new AuditType("usdu", "usduMemberDelete", null, "memberId", "sourceId", "subjectId")),
  
  /**
   * add report config for stem
   */
  STEM_REPORT_CONFIG_ADD(new AuditType("stemReportConfig", "addStemReportConfig", null, "stemId", "stemName", "reportConfigId")),
  
  /**
   * update report config for stem
   */
  STEM_REPORT_CONFIG_UPDATE(new AuditType("stemReportConfig", "updateStemReportConfig", null, "stemId", "stemName", "reportConfigId")),
  
  /**
   * delete report config for stem
   */
  STEM_REPORT_CONFIG_DELETE(new AuditType("stemReportConfig", "deleteStemReportConfig", null, "stemId", "stemName", "reportConfigId")),
  
  /**
   * download report for stem
   */
  STEM_REPORT_DOWNLONAD(new AuditType("stemReportConfig", "downloadStemReport", null, "stemId", "stemName", "reportInstanceId")),
  
  /**
   * add report config for group
   */
  GROUP_REPORT_CONFIG_ADD(new AuditType("groupReportConfig", "addGroupReportConfig", null, "groupId", "groupName", "reportConfigId")),
  
  /**
   * update report config for group
   */
  GROUP_REPORT_CONFIG_UPDATE(new AuditType("groupReportConfig", "updateGroupReportConfig", null, "groupId", "groupName", "reportConfigId")),
  
  /**
   * delete report config for group
   */
  GROUP_REPORT_CONFIG_DELETE(new AuditType("groupReportConfig", "deleteGroupReportConfig", null, "groupId", "groupName", "reportConfigId")),
  
  /**
   * download report for group
   */
  GROUP_REPORT_DOWNLONAD(new AuditType("groupReportConfig", "downloadGroupReport", null, "groupId", "groupName", "reportInstanceId")),
  
  /**
   * add configuration
   */
  CONFIGURATION_ADD(new AuditType("configurationFile", "addConfigEntry", null, "id", "configFile", 
      "key", "value", "configHierarchy")),

  /**
   * update configuration
   */
  CONFIGURATION_UPDATE(new AuditType("configurationFile", "updateConfigEntry", null, "id", "configFile", 
      "key", "value", "configHierarchy", "previousValue")),

  /**
   * delete configuration
   */
  CONFIGURATION_DELETE(new AuditType("configurationFile", "deleteConfigEntry", null, "id", "configFile", 
      "key", "value", "configHierarchy", "previousValue")),
  
  /**
   * provisioner sync run for a provisioner for a single group 
   */
  PROVISIONER_SYNC_RUN_GROUP(new AuditType("provisionerSync", "provisionerGroupSync", null, "groupId", "provisionerName")),

  /**
   * provisioner sync run for a provisioner for all configured groups and stems
   */
  PROVISIONER_SYNC_RUN(new AuditType("provisionerSync", "provisionerSync", null, "provisionerName")),
  
  /**
   * gsh template execute
   */
  GSH_TEMPLATE_EXEC(new AuditType("gshTemplate", "gshTemplateExec", null, "gshTemplateConfigId", "status")),
  
  /**
   * add gsh template
   */
  GSH_TEMPLATE_ADD(new AuditType("gshTemplate", "gshTemplateAdd", null, "gshTemplateConfigId")),
  
  /**
   * update gsh template
   */
  GSH_TEMPLATE_UPDATE(new AuditType("gshTemplate", "gshTemplateUpdate", null, "gshTemplateConfigId")),
  
  /**
   * delete gsh template
   */
  GSH_TEMPLATE_DELETE(new AuditType("gshTemplate", "gshTemplateDelete", null, "gshTemplateConfigId"))
  ;

  /**
   * defaults for audit type, though doesnt hold the id
   */
  private AuditType internalAuditTypeDefault;
  
  /**
   * construct
   * @param theInternalAuditTypeDefault 
   */
  private AuditTypeBuiltin(AuditType theInternalAuditTypeDefault) {
    this.internalAuditTypeDefault = theInternalAuditTypeDefault;
  }
  
  /**
   * get the audit type from the enum
   * @return the audit type
   */
  public AuditType getAuditType() {
    return AuditTypeFinder.find(this.internalAuditTypeDefault.getAuditCategory(), 
        this.internalAuditTypeDefault.getActionName(), true);
  }

  /**
   * get the defaults, but not the id
   * @return the defaults
   */
  public AuditType internal_auditTypeDefault() {
    return this.internalAuditTypeDefault;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.audit.AuditTypeIdentifier#getAuditCategory()
   */
  public String getAuditCategory() {
    return this.getAuditType().getAuditCategory();
  }
  
  /**
   * @see edu.internet2.middleware.grouper.audit.AuditTypeIdentifier#getActionName()
   */
  public String getActionName() {
    return this.getAuditType().getActionName();
  }
  
  /**
   * 
   */
  public static void internal_clearCache() {
    
    //set this to -1 so it will be an insert next time
    for (AuditTypeBuiltin auditTypeBuiltin : AuditTypeBuiltin.values()) {
      auditTypeBuiltin.internalAuditTypeDefault.setHibernateVersionNumber(-1l);
    }
  }
  
  /**
   * cache of category and action to enum
   */
  private static Map<MultiKey, AuditTypeBuiltin> categoryAndActionToBuiltin = null;
  
  /**
   * cache of category and action to enum
   * @return the map
   */
  private static Map<MultiKey, AuditTypeBuiltin> categoryAndActionToBuiltin() {
    if (categoryAndActionToBuiltin == null) {
      synchronized(AuditTypeBuiltin.class) {
        
        if (categoryAndActionToBuiltin == null) {
          Map<MultiKey, AuditTypeBuiltin> tempMap = new HashMap<MultiKey, AuditTypeBuiltin>();
          for (AuditTypeBuiltin auditTypeBuiltin : values()) {
            tempMap.put(new MultiKey(auditTypeBuiltin.getAuditCategory(), auditTypeBuiltin.getActionName()), auditTypeBuiltin);
          }
          categoryAndActionToBuiltin = tempMap;
        }
      }
    }
    return categoryAndActionToBuiltin;
  }
  
  
  /**
   * get the enum based on category and action
   * @param category
   * @param action
   * @param exceptionIfNotFound
   * @return the enum
   */
  public static AuditTypeBuiltin valueOfIgnoreCase(String category, String action, boolean exceptionIfNotFound) {
    
    MultiKey multiKey = new MultiKey(category, action);
    AuditTypeBuiltin auditTypeBuiltin = categoryAndActionToBuiltin().get(multiKey);
    if (auditTypeBuiltin == null && exceptionIfNotFound) {
      throw new RuntimeException("Cant find AuditTypeBuilting for " + category + " and " + action);
    }
    return auditTypeBuiltin;
  }
  
}
