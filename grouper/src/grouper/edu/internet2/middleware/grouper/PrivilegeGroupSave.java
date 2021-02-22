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
 * $Id: StemSave.java,v 1.5 2009-11-17 02:52:29 mchyzer Exp $
 */
package edu.internet2.middleware.grouper;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.exception.GroupNotFoundException;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.hibernate.GrouperTransaction;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionHandler;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.membership.MembershipType;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.SaveMode;
import edu.internet2.middleware.grouper.misc.SaveResultType;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.Privilege;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectUtils;


/**
 * Use this class to insert or update a group privilege
 */
public class PrivilegeGroupSave {
  
  
  
  /**
   * create a new privilege save
   * @param theGrouperSession
   */
  public PrivilegeGroupSave() {
  }
  
  /**
   * group id to add to, mutually exclusive with group name
   */
  private String groupId;

  /**
   * group id to add to, mutually exclusive with group name
   * @param theGroupId
   * @return this for chaining
   */
  public PrivilegeGroupSave assignGroupId(String theGroupId) {
    this.groupId = theGroupId;
    return this;
  }
  
  /**
   * group
   */
  private Group group;
  
  /**
   * assign a group
   * @param theGroup
   * @return this for chaining
   */
  public PrivilegeGroupSave assignGroup(Group theGroup) {
    this.group = theGroup;
    return this;
  }

  /**
   * subject to add
   */
  private Subject subject = null;
  
  /**
   * subject to add
   * @param theSubject
   * @return this for chaining
   */
  public PrivilegeGroupSave assignSubject(Subject theSubject) {
    this.subject = theSubject;
    return this;
  }
  
  /**
   * member to add
   */
  private Member member = null;
  
  /**
   * member to add
   * @param member
   * @return this for chaining
   */
  public PrivilegeGroupSave assignMember(Member theMember) {
    this.member = theMember;
    return this;
  }
  
  /**
   * group name to add to, mutually exclusive with group id
   */
  private String groupName;

  /**
   * group name to add to, mutually exclusive with group id
   * @param theGroupName
   * @return this for chaining
   */
  public PrivilegeGroupSave assignGroupName(String theGroupName) {
    this.groupName = theGroupName;
    return this;
  }

  
  /**
   * member id to add
   */
  private String memberId;

  /**
   * member id to add
   * @param theMemberId
   * @return this for chaining
   */
  public PrivilegeGroupSave assignMemberId(String theMemberId) {
    this.memberId = theMemberId;
    return this;
  }

  /**
   * subject id to add, mutually exclusive and preferable to subject identifier
   */
  private String subjectId;

  /**
   * subject id to add, mutually exclusive and preferable to subject identifier
   * @param theSubjectId
   * @return this for chaining
   */
  public PrivilegeGroupSave assignSubjectId(String theSubjectId) {
    this.subjectId = theSubjectId;
    return this;
  }
  
  /**
   * subject source id to add
   */
  private String subjectSourceId;

  /**
   * subject source id to add
   * @param theSubjectSourceId
   * @return this for chaining
   */
  public PrivilegeGroupSave assignSubjectSourceId(String theSubjectSourceId) {
    this.subjectSourceId = theSubjectSourceId;
    return this;
  }
  
  /**
   * subject identifier to add, mutually exclusive and not preferable to subject id
   */
  private String subjectIdentifier;

  /**
   * subject identifier to add, mutually exclusive and not preferable to subject id
   * @param thesubjectIdentifier
   * @return this for chaining
   */
  public PrivilegeGroupSave assignSubjectIdentifier(String theSubjectIdentifier) {
    this.subjectIdentifier = theSubjectIdentifier;
    return this;
  }

  /**
   * uuid of this membership for inserts (optional)
   */
  private String immediateMembershipId;

  /**
   * uuid of this membership for inserts (optional)
   * @param theImmediateMembershipId
   * @return this for chaining
   */
  public PrivilegeGroupSave assignImmediateMembershipId(String theImmediateMembershipId) {
    this.immediateMembershipId = theImmediateMembershipId;
    return this;
  }

  /** save mode */
  private SaveMode saveMode;

  /**
   * asssign save mode
   * @param theSaveMode
   * @return this for chaining
   */
  public PrivilegeGroupSave assignSaveMode(SaveMode theSaveMode) {
    this.saveMode = theSaveMode;
    return this;
  }

  /** save type after the save */
  private SaveResultType saveResultType = null;

  /**
   * get the save type
   * @return save type
   */
  public SaveResultType getSaveResultType() {
    return this.saveResultType;
  }
  
  /**
   * field of privilege
   */
  private String fieldId;

  /**
   * field of privilege
   * @param theFieldId
   * @return this for chaining
   */
  public PrivilegeGroupSave assignFieldId(String theFieldId) {
    this.fieldId = theFieldId;
    return this;
  }

  /**
   * field of field name
   */
  private String fieldName;

  /**
   * field of privilege (could be privilege name too)
   * @param theFieldName
   * @return this for chaining
   */
  public PrivilegeGroupSave assignFieldName(String theFieldName) {
    this.fieldName = theFieldName;
    return this;
  }
  
  /**
   * field of privilege
   */
  private Field field;

  /**
   * field of privilege
   * @param theFieldId
   * @return this for chaining
   */
  public PrivilegeGroupSave assignField(Field theField) {
    this.field = theField;
    return this;
  }
  
  /**
   * <pre>
   * create or update or delete a composite
   * </pre>
   * @return the composite that was updated or created or deleted
   */
  public Membership save() throws InsufficientPrivilegeException, GroupNotFoundException {

    boolean canIdentifyMembership = StringUtils.isNotBlank(this.immediateMembershipId);

    if (!canIdentifyMembership) {
      GrouperUtil.assertion(StringUtils.isNotBlank(this.groupId) || StringUtils.isNotBlank(this.groupName), "immediateMembershipId or groupId or groupName is required");
      
      boolean canIdentifyMember = StringUtils.isNotBlank(this.memberId);
      if (!canIdentifyMember) {
        GrouperUtil.assertion(StringUtils.isNotBlank(this.subjectSourceId), "immediateMembershipId or memberId or subjectSourceId is required");
        GrouperUtil.assertion(StringUtils.isNotBlank(this.subjectId) || StringUtils.isNotBlank(this.subjectIdentifier), "immediateMembershipId or memberId or subjectId or subjectIdentifier is required");
      }
      
    }
    //default to insert or update
    saveMode = (SaveMode)ObjectUtils.defaultIfNull(saveMode, SaveMode.INSERT_OR_UPDATE);

    return (Membership)GrouperTransaction.callbackGrouperTransaction(new GrouperTransactionHandler() {
    
        public Object callback(GrouperTransaction grouperTransaction)
            throws GrouperDAOException {
          
          grouperTransaction.setCachingEnabled(false);
          
          // start by finding existing membership
          Membership membership = null;

          if (membership == null && !StringUtils.isBlank(PrivilegeGroupSave.this.immediateMembershipId)) {
            membership = MembershipFinder.findByUuid(GrouperSession.staticGrouperSession(), PrivilegeGroupSave.this.immediateMembershipId, false, false);
          }

          if (group == null && !StringUtils.isBlank(PrivilegeGroupSave.this.groupId)) {
            group = GroupFinder.findByUuid(GrouperSession.staticGrouperSession(), PrivilegeGroupSave.this.groupId, false, new QueryOptions().secondLevelCache(false));
          } 
          if (group == null && !StringUtils.isBlank(PrivilegeGroupSave.this.groupName)) {
            group = GroupFinder.findByName(GrouperSession.staticGrouperSession(), PrivilegeGroupSave.this.groupName, false, new QueryOptions().secondLevelCache(false));
          }
          if (membership != null && group == null) {
            group = GroupFinder.findByUuid(GrouperSession.staticGrouperSession(), membership.getOwnerGroupId(), true, new QueryOptions().secondLevelCache(false));
          }
          GrouperUtil.assertion(group!=null,  "Group not found");

          if (member == null && !StringUtils.isBlank(PrivilegeGroupSave.this.memberId)) {
            member = MemberFinder.findByUuid(GrouperSession.staticGrouperSession(), PrivilegeGroupSave.this.memberId, false);
          }

          if (subject == null && !StringUtils.isBlank(subjectId) && !StringUtils.isBlank(subjectSourceId)) {
            subject = SubjectFinder.findByIdAndSource(PrivilegeGroupSave.this.subjectId, PrivilegeGroupSave.this.subjectSourceId, false);
          }            
          if (subject == null && !StringUtils.isBlank(subjectIdentifier) && !StringUtils.isBlank(subjectSourceId)) {
            subject = SubjectFinder.findByIdentifierAndSource(PrivilegeGroupSave.this.subjectIdentifier, PrivilegeGroupSave.this.subjectSourceId, false);
          }
          if (subject == null && member != null) {
            subject = member.getSubject();
          }
          if (member == null && subject != null) {
            member = MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), subject, saveMode!=SaveMode.DELETE);
          }
          GrouperUtil.assertion(member!=null,  "Member not found");
          GrouperUtil.assertion(subject!=null,  "Subject not found");
          
          if (field == null && !StringUtils.isBlank(fieldId)) {
            field = FieldFinder.findById(fieldId, true);
          }
          if (field == null && !StringUtils.isBlank(fieldName)) {
            field = FieldFinder.find(fieldName, true, true);
          }
          
          GrouperUtil.assertion(field!=null,  "Field not found");
          GrouperUtil.assertion(field.isGroupAccessField(), "Must be group access field '" + field.getName() + "'");
          
          if (membership == null) {
            membership = GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType( 
                group.getId(), member.getId(), field, MembershipType.IMMEDIATE.getTypeString(), false, true);
          }

          if (!group.canHavePrivilege(GrouperSession.staticGrouperSession().getSubject(), AccessPrivilege.ADMIN.getName(), false)) {
            throw new RuntimeException("Subject '" + SubjectUtils.subjectToString(GrouperSession.staticGrouperSession().getSubject()) 
              + "' cannot ADMIN group '" + group.getName() + "'");
          }

          // handle deletes
          if (saveMode == SaveMode.DELETE) {
            if (membership == null) {
              PrivilegeGroupSave.this.saveResultType = SaveResultType.NO_CHANGE;
              return null;
            }
            group.revokePriv(subject, Privilege.listToPriv(field.getName(), true), false);
            PrivilegeGroupSave.this.saveResultType = SaveResultType.DELETE;
            return membership;
          }
          
          if (saveMode == SaveMode.INSERT && membership != null) {
            throw new RuntimeException("Inserting membership but it already exists!");
          }
          if (saveMode == SaveMode.UPDATE && membership == null) {
            throw new RuntimeException("Updating membership but it doesnt exist!");
          }

          // insert
          if (membership == null) {
            group.internal_grantPriv(subject, AccessPrivilege.listToPriv(field.getName()), false, PrivilegeGroupSave.this.immediateMembershipId);
            PrivilegeGroupSave.this.saveResultType = SaveResultType.INSERT;
          } else {
            PrivilegeGroupSave.this.saveResultType = SaveResultType.NO_CHANGE;
            return membership;
          }
          return membership;
        }
      });
  }
}
