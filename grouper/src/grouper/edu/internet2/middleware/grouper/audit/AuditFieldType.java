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
 * $Id: AuditFieldType.java,v 1.4 2009-08-10 14:01:17 isgwb Exp $
 */
package edu.internet2.middleware.grouper.audit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.internet2.middleware.grouperClient.collections.MultiKey;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import edu.internet2.middleware.grouper.hibernate.HibUtils;


/**
 * group together audit columns into certain types, for easy querying
 */
public class AuditFieldType {

  /** audit type for group id */
  public static final String AUDIT_TYPE_GROUP_ID = "groupId";
  
  /** audit type for group id */
  public static final String AUDIT_TYPE_ATTESTATION_GROUP_ID = "attestationGroupId";
  
  /** audit type for group id */
  public static final String AUDIT_TYPE_STEM_ID = "stemId";
  
  /** audit type for member id */
  public static final String AUDIT_TYPE_MEMBER_ID = "memberId";
  
  /** audit type for group type id */
  public static final String AUDIT_TYPE_GROUPTYPE_ID = "groupTypeId";
  
  /** map of string (field type, e.g. groupId), to a list of multikey: AuditTypeIdentifier and field label) */
  private static Map<String, Set<MultiKey>> auditFieldTypes = new HashMap<String, Set<MultiKey>>();

  /** init audit field types */
  private static boolean initAuditFieldTypesDone = false;
  
  /**
   * init audit types
   */
  private static void initAuditFieldTypes() {
    if (initAuditFieldTypesDone) {
      return;
    }

    initAuditFieldTypesDone = true;
    
    addAuditFieldType(AUDIT_TYPE_STEM_ID, AuditTypeBuiltin.STEM_DEPROVISIONING_UPDATE_LAST_CERTIFIED_DATE, "stemId");
    addAuditFieldType(AUDIT_TYPE_STEM_ID, AuditTypeBuiltin.STEM_DEPROVISIONING_CLEAR_LAST_CERTIFIED_DATE, "stemId");
    addAuditFieldType(AUDIT_TYPE_STEM_ID, AuditTypeBuiltin.STEM_ATTESTATION_ADD, "stemId");
    addAuditFieldType(AUDIT_TYPE_STEM_ID, AuditTypeBuiltin.STEM_ATTESTATION_DELETE, "stemId");
    addAuditFieldType(AUDIT_TYPE_STEM_ID, AuditTypeBuiltin.STEM_ATTESTATION_UPDATE, "stemId");
    addAuditFieldType(AUDIT_TYPE_STEM_ID, AuditTypeBuiltin.ATTRIBUTE_ASSIGN_STEM_ADD, "ownerStemId");
    addAuditFieldType(AUDIT_TYPE_STEM_ID, AuditTypeBuiltin.ATTRIBUTE_ASSIGN_STEM_DELETE, "ownerStemId");
    addAuditFieldType(AUDIT_TYPE_STEM_ID, AuditTypeBuiltin.ATTRIBUTE_ASSIGN_STEM_UPDATE, "ownerStemId");
    addAuditFieldType(AUDIT_TYPE_STEM_ID, AuditTypeBuiltin.PRIVILEGE_STEM_DELETE, "stemId");
    addAuditFieldType(AUDIT_TYPE_STEM_ID, AuditTypeBuiltin.ATTRIBUTE_DEF_ADD, "parentStemId");
    addAuditFieldType(AUDIT_TYPE_STEM_ID, AuditTypeBuiltin.ATTRIBUTE_DEF_DELETE, "parentStemId");
    addAuditFieldType(AUDIT_TYPE_STEM_ID, AuditTypeBuiltin.ATTRIBUTE_DEF_UPDATE, "parentStemId");
    addAuditFieldType(AUDIT_TYPE_STEM_ID, AuditTypeBuiltin.ATTRIBUTE_DEF_NAME_ADD, "parentStemId");
    addAuditFieldType(AUDIT_TYPE_STEM_ID, AuditTypeBuiltin.ATTRIBUTE_DEF_NAME_DELETE, "parentStemId");
    addAuditFieldType(AUDIT_TYPE_STEM_ID, AuditTypeBuiltin.ATTRIBUTE_DEF_NAME_UPDATE, "parentStemId");
    addAuditFieldType(AUDIT_TYPE_STEM_ID, AuditTypeBuiltin.GROUP_DELETE, "parentStemId");
    addAuditFieldType(AUDIT_TYPE_STEM_ID, AuditTypeBuiltin.GROUP_DELETE_ALL_MEMBERSHIPS, "parentStemId");
    addAuditFieldType(AUDIT_TYPE_STEM_ID, AuditTypeBuiltin.GROUP_UPDATE, "parentStemId");
    addAuditFieldType(AUDIT_TYPE_STEM_ID, AuditTypeBuiltin.GROUP_ADD, "parentStemId");
    addAuditFieldType(AUDIT_TYPE_STEM_ID, AuditTypeBuiltin.STEM_ADD, "id");
    addAuditFieldType(AUDIT_TYPE_STEM_ID, AuditTypeBuiltin.STEM_ADD, "parentStemId");
    addAuditFieldType(AUDIT_TYPE_STEM_ID, AuditTypeBuiltin.STEM_DELETE, "id");
    addAuditFieldType(AUDIT_TYPE_STEM_ID, AuditTypeBuiltin.STEM_DELETE, "parentStemId");
    addAuditFieldType(AUDIT_TYPE_STEM_ID, AuditTypeBuiltin.STEM_UPDATE, "id");
    addAuditFieldType(AUDIT_TYPE_STEM_ID, AuditTypeBuiltin.STEM_UPDATE, "parentStemId");
    addAuditFieldType(AUDIT_TYPE_STEM_ID, AuditTypeBuiltin.STEM_COPY, "oldStemId");
    addAuditFieldType(AUDIT_TYPE_STEM_ID, AuditTypeBuiltin.STEM_COPY, "newStemId");
    addAuditFieldType(AUDIT_TYPE_STEM_ID, AuditTypeBuiltin.STEM_MOVE, "stemId");
    addAuditFieldType(AUDIT_TYPE_STEM_ID, AuditTypeBuiltin.STEM_MOVE, "newParentStemId");
    addAuditFieldType(AUDIT_TYPE_STEM_ID, AuditTypeBuiltin.GROUP_MOVE, "newStemId");
    addAuditFieldType(AUDIT_TYPE_STEM_ID, AuditTypeBuiltin.STEM_ATTESTATION_UPDATE_LAST_CERTIFIED_DATE, "stemId");

    addAuditFieldType(AUDIT_TYPE_ATTESTATION_GROUP_ID, AuditTypeBuiltin.GROUP_ATTESTATION_ADD, "groupId");
    addAuditFieldType(AUDIT_TYPE_ATTESTATION_GROUP_ID, AuditTypeBuiltin.GROUP_ATTESTATION_DELETE, "groupId");
    addAuditFieldType(AUDIT_TYPE_ATTESTATION_GROUP_ID, AuditTypeBuiltin.GROUP_ATTESTATION_UPDATE, "groupId");
    addAuditFieldType(AUDIT_TYPE_ATTESTATION_GROUP_ID, AuditTypeBuiltin.GROUP_ATTESTATION_UPDATE_LAST_CERTIFIED_DATE, "groupId");
    addAuditFieldType(AUDIT_TYPE_ATTESTATION_GROUP_ID, AuditTypeBuiltin.GROUP_ATTESTATION_CLEAR_LAST_CERTIFIED_DATE, "groupId");

    addAuditFieldType(AUDIT_TYPE_GROUP_ID, AuditTypeBuiltin.GROUP_DEPROVISIONING_UPDATE_LAST_CERTIFIED_DATE, "groupId");
    addAuditFieldType(AUDIT_TYPE_GROUP_ID, AuditTypeBuiltin.GROUP_DEPROVISIONING_CLEAR_LAST_CERTIFIED_DATE, "groupId");
    addAuditFieldType(AUDIT_TYPE_GROUP_ID, AuditTypeBuiltin.GROUP_ATTESTATION_ADD, "groupId");
    addAuditFieldType(AUDIT_TYPE_GROUP_ID, AuditTypeBuiltin.GROUP_ATTESTATION_DELETE, "groupId");
    addAuditFieldType(AUDIT_TYPE_GROUP_ID, AuditTypeBuiltin.GROUP_ATTESTATION_UPDATE, "groupId");
    addAuditFieldType(AUDIT_TYPE_GROUP_ID, AuditTypeBuiltin.GROUP_ATTESTATION_UPDATE_LAST_CERTIFIED_DATE, "groupId");
    addAuditFieldType(AUDIT_TYPE_GROUP_ID, AuditTypeBuiltin.GROUP_ATTESTATION_CLEAR_LAST_CERTIFIED_DATE, "groupId");
    addAuditFieldType(AUDIT_TYPE_GROUP_ID, AuditTypeBuiltin.GROUP_ADD, "id");
    addAuditFieldType(AUDIT_TYPE_GROUP_ID, AuditTypeBuiltin.GROUP_UPDATE, "id");
    addAuditFieldType(AUDIT_TYPE_GROUP_ID, AuditTypeBuiltin.GROUP_DELETE, "id");
    addAuditFieldType(AUDIT_TYPE_GROUP_ID, AuditTypeBuiltin.GROUP_DELETE_ALL_MEMBERSHIPS, "id");
    addAuditFieldType(AUDIT_TYPE_GROUP_ID, AuditTypeBuiltin.ATTRIBUTE_ASSIGN_GROUP_ADD, "ownerGroupId");
    addAuditFieldType(AUDIT_TYPE_GROUP_ID, AuditTypeBuiltin.ATTRIBUTE_ASSIGN_GROUP_DELETE, "ownerGroupId");
    addAuditFieldType(AUDIT_TYPE_GROUP_ID, AuditTypeBuiltin.ATTRIBUTE_ASSIGN_GROUP_UPDATE, "ownerGroupId");
    addAuditFieldType(AUDIT_TYPE_GROUP_ID, AuditTypeBuiltin.ATTRIBUTE_ASSIGN_ANYMSHIP_ADD, "ownerGroupId");
    addAuditFieldType(AUDIT_TYPE_GROUP_ID, AuditTypeBuiltin.ATTRIBUTE_ASSIGN_ANYMSHIP_DELETE, "ownerGroupId");
    addAuditFieldType(AUDIT_TYPE_GROUP_ID, AuditTypeBuiltin.ATTRIBUTE_ASSIGN_ANYMSHIP_UPDATE, "ownerGroupId");
    addAuditFieldType(AUDIT_TYPE_GROUP_ID, AuditTypeBuiltin.ATTRIBUTE_ASSIGN_IMMMSHIP_ADD, "ownerOwnerId");
    addAuditFieldType(AUDIT_TYPE_GROUP_ID, AuditTypeBuiltin.ATTRIBUTE_ASSIGN_IMMMSHIP_DELETE, "ownerOwnerId");
    addAuditFieldType(AUDIT_TYPE_GROUP_ID, AuditTypeBuiltin.ATTRIBUTE_ASSIGN_IMMMSHIP_UPDATE, "ownerOwnerId");
    addAuditFieldType(AUDIT_TYPE_GROUP_ID, AuditTypeBuiltin.GROUP_ATTRIBUTE_ADD, "groupId");
    addAuditFieldType(AUDIT_TYPE_GROUP_ID, AuditTypeBuiltin.GROUP_ATTRIBUTE_DELETE, "groupId");
    addAuditFieldType(AUDIT_TYPE_GROUP_ID, AuditTypeBuiltin.GROUP_ATTRIBUTE_UPDATE, "groupId");
    addAuditFieldType(AUDIT_TYPE_GROUP_ID, AuditTypeBuiltin.GROUP_COMPOSITE_ADD, "ownerId");
    addAuditFieldType(AUDIT_TYPE_GROUP_ID, AuditTypeBuiltin.GROUP_COMPOSITE_ADD, "leftFactorId");
    addAuditFieldType(AUDIT_TYPE_GROUP_ID, AuditTypeBuiltin.GROUP_COMPOSITE_ADD, "rightFactorId");
    addAuditFieldType(AUDIT_TYPE_GROUP_ID, AuditTypeBuiltin.GROUP_COMPOSITE_UPDATE, "ownerId");
    addAuditFieldType(AUDIT_TYPE_GROUP_ID, AuditTypeBuiltin.GROUP_COMPOSITE_UPDATE, "leftFactorId");
    addAuditFieldType(AUDIT_TYPE_GROUP_ID, AuditTypeBuiltin.GROUP_COMPOSITE_UPDATE, "rightFactorId");
    addAuditFieldType(AUDIT_TYPE_GROUP_ID, AuditTypeBuiltin.GROUP_COMPOSITE_DELETE, "ownerId");
    addAuditFieldType(AUDIT_TYPE_GROUP_ID, AuditTypeBuiltin.GROUP_COMPOSITE_DELETE, "leftFactorId");
    addAuditFieldType(AUDIT_TYPE_GROUP_ID, AuditTypeBuiltin.GROUP_COMPOSITE_DELETE, "rightFactorId");
    addAuditFieldType(AUDIT_TYPE_GROUP_ID, AuditTypeBuiltin.GROUP_COPY, "oldGroupId");
    addAuditFieldType(AUDIT_TYPE_GROUP_ID, AuditTypeBuiltin.GROUP_COPY, "newGroupId");
    addAuditFieldType(AUDIT_TYPE_GROUP_ID, AuditTypeBuiltin.GROUP_MOVE, "groupId");
    addAuditFieldType(AUDIT_TYPE_GROUP_ID, AuditTypeBuiltin.GROUP_TYPE_ASSIGN, "groupId");
    addAuditFieldType(AUDIT_TYPE_GROUP_ID, AuditTypeBuiltin.GROUP_TYPE_UNASSIGN, "groupId");
    addAuditFieldType(AUDIT_TYPE_GROUP_ID, AuditTypeBuiltin.MEMBERSHIP_GROUP_ADD, "groupId");
    addAuditFieldType(AUDIT_TYPE_GROUP_ID, AuditTypeBuiltin.MEMBERSHIP_GROUP_ADD, "memberId");
    addAuditFieldType(AUDIT_TYPE_GROUP_ID, AuditTypeBuiltin.MEMBERSHIP_GROUP_UPDATE, "groupId");
    addAuditFieldType(AUDIT_TYPE_GROUP_ID, AuditTypeBuiltin.MEMBERSHIP_GROUP_UPDATE, "memberId");
    addAuditFieldType(AUDIT_TYPE_GROUP_ID, AuditTypeBuiltin.MEMBERSHIP_GROUP_DELETE, "groupId");
    addAuditFieldType(AUDIT_TYPE_GROUP_ID, AuditTypeBuiltin.MEMBERSHIP_GROUP_DELETE, "memberId");
    addAuditFieldType(AUDIT_TYPE_GROUP_ID, AuditTypeBuiltin.MEMBERSHIP_GROUP_EXPORT, "groupId");
    addAuditFieldType(AUDIT_TYPE_GROUP_ID, AuditTypeBuiltin.MEMBERSHIP_GROUP_IMPORT, "groupId");
    addAuditFieldType(AUDIT_TYPE_GROUP_ID, AuditTypeBuiltin.PRIVILEGE_GROUP_ADD, "groupId");
    addAuditFieldType(AUDIT_TYPE_GROUP_ID, AuditTypeBuiltin.PRIVILEGE_GROUP_ADD, "memberId");
    addAuditFieldType(AUDIT_TYPE_GROUP_ID, AuditTypeBuiltin.PRIVILEGE_GROUP_UPDATE, "groupId");
    addAuditFieldType(AUDIT_TYPE_GROUP_ID, AuditTypeBuiltin.PRIVILEGE_GROUP_UPDATE, "memberId");
    addAuditFieldType(AUDIT_TYPE_GROUP_ID, AuditTypeBuiltin.PRIVILEGE_GROUP_DELETE, "groupId");
    addAuditFieldType(AUDIT_TYPE_GROUP_ID, AuditTypeBuiltin.PROVISIONER_SYNC_RUN_GROUP, "groupId");
    addAuditFieldType(AUDIT_TYPE_GROUP_ID, AuditTypeBuiltin.PRIVILEGE_GROUP_DELETE, "memberId");

    addAuditFieldType(AUDIT_TYPE_MEMBER_ID, AuditTypeBuiltin.MEMBER_DEPROVISIONING, "memberId");
    addAuditFieldType(AUDIT_TYPE_MEMBER_ID, AuditTypeBuiltin.MEMBER_CHANGE_SUBJECT, "oldMemberId");
    addAuditFieldType(AUDIT_TYPE_MEMBER_ID, AuditTypeBuiltin.MEMBER_CHANGE_SUBJECT, "newMemberId");
    addAuditFieldType(AUDIT_TYPE_MEMBER_ID, AuditTypeBuiltin.MEMBERSHIP_GROUP_ADD, "memberId");
    addAuditFieldType(AUDIT_TYPE_MEMBER_ID, AuditTypeBuiltin.MEMBERSHIP_GROUP_UPDATE, "memberId");
    addAuditFieldType(AUDIT_TYPE_MEMBER_ID, AuditTypeBuiltin.MEMBERSHIP_GROUP_DELETE, "memberId");
    addAuditFieldType(AUDIT_TYPE_MEMBER_ID, AuditTypeBuiltin.PRIVILEGE_GROUP_ADD, "memberId");
    addAuditFieldType(AUDIT_TYPE_MEMBER_ID, AuditTypeBuiltin.PRIVILEGE_GROUP_UPDATE, "memberId");
    addAuditFieldType(AUDIT_TYPE_MEMBER_ID, AuditTypeBuiltin.PRIVILEGE_GROUP_DELETE, "memberId");
    addAuditFieldType(AUDIT_TYPE_MEMBER_ID, AuditTypeBuiltin.PRIVILEGE_STEM_ADD, "memberId");
    addAuditFieldType(AUDIT_TYPE_MEMBER_ID, AuditTypeBuiltin.PRIVILEGE_STEM_UPDATE, "memberId");
    addAuditFieldType(AUDIT_TYPE_MEMBER_ID, AuditTypeBuiltin.PRIVILEGE_STEM_DELETE, "memberId");
    addAuditFieldType(AUDIT_TYPE_MEMBER_ID, AuditTypeBuiltin.PRIVILEGE_ATTRIBUTE_DEF_ADD, "memberId");
    addAuditFieldType(AUDIT_TYPE_MEMBER_ID, AuditTypeBuiltin.PRIVILEGE_ATTRIBUTE_DEF_UPDATE, "memberId");
    addAuditFieldType(AUDIT_TYPE_MEMBER_ID, AuditTypeBuiltin.PRIVILEGE_ATTRIBUTE_DEF_DELETE, "memberId");
    
    addAuditFieldType(AUDIT_TYPE_GROUPTYPE_ID, AuditTypeBuiltin.GROUP_TYPE_ADD, "id");
    addAuditFieldType(AUDIT_TYPE_GROUPTYPE_ID, AuditTypeBuiltin.GROUP_TYPE_DELETE, "id");
    addAuditFieldType(AUDIT_TYPE_GROUPTYPE_ID, AuditTypeBuiltin.GROUP_TYPE_UPDATE, "id");
    addAuditFieldType(AUDIT_TYPE_GROUPTYPE_ID, AuditTypeBuiltin.GROUP_FIELD_ADD, "groupTypeId");
    addAuditFieldType(AUDIT_TYPE_GROUPTYPE_ID, AuditTypeBuiltin.GROUP_FIELD_UPDATE, "groupTypeId");
    addAuditFieldType(AUDIT_TYPE_GROUPTYPE_ID, AuditTypeBuiltin.GROUP_FIELD_DELETE, "groupTypeId");

  }
  
  /**
   * add an audit field type for field type searches
   * @param fieldType 
   * @param auditTypeIdentifier
   * @param fieldLabel
   */
  public static void addAuditFieldType(String fieldType, AuditTypeIdentifier auditTypeIdentifier, String fieldLabel) {
    Set<MultiKey> fieldTypeList = fieldTypeSet(fieldType);
    fieldTypeList.add(new MultiKey(auditTypeIdentifier, fieldLabel));
  }

  /**
   * return the list of multikeys of 
   * @param fieldType
   * @return the set of multikeys which is the AuditTypeIdentifier and field label, will not be null
   */
  public static Set<MultiKey> fieldTypeSet(String fieldType) {
    initAuditFieldTypes();
    Set<MultiKey> fieldTypeSet = auditFieldTypes.get(fieldType);
    if (fieldTypeSet == null) {
      fieldTypeSet = new HashSet<MultiKey>();
      auditFieldTypes.put(fieldType, fieldTypeSet);
    }
    return fieldTypeSet;
  }
  
  /**
   * return the criterion for a hibernate criteria query
   * @param fieldType
   * @param fieldValue 
   * @return the criterion or null if not exist
   */
  public static Criterion criterion(String fieldType, Object fieldValue) {
    Set<MultiKey> fieldTypeSet = fieldTypeSet(fieldType);
    if (fieldTypeSet.size() == 0) {
      return null;
    }
    List<Criterion> criterionList = new ArrayList<Criterion>();
    for (MultiKey multiKey : fieldTypeSet) {
      AuditTypeIdentifier auditTypeIdentifier = (AuditTypeIdentifier) multiKey.getKey(0);
      String fieldLabel = (String) multiKey.getKey(1);
      AuditType auditType = AuditTypeFinder.find(auditTypeIdentifier.getAuditCategory(), 
          auditTypeIdentifier.getActionName(), false);
      if (auditType == null) {
        continue;
      }
      Criterion auditTypeCriterion = Restrictions.eq(AuditEntry.FIELD_AUDIT_TYPE_ID, auditType.getId());
      String auditEntryField = auditType.retrieveAuditEntryFieldForLabel(fieldLabel);
      Criterion auditEntryFieldCriterion = Restrictions.eq(auditEntryField, fieldValue);
      Criterion andCriterion = HibUtils.listCrit(auditTypeCriterion, auditEntryFieldCriterion);
      criterionList.add(andCriterion);
    }
    
    //lets convert the list to a bunch of ors
    if (criterionList.size() == 0) {
      return null;
    }
    if (criterionList.size() == 1) {
      return criterionList.get(0);
    }
    
    Criterion result = HibUtils.listCritOr(criterionList);
    
    return result;
  }
  
}
