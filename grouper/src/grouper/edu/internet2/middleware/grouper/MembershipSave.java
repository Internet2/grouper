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

import java.sql.Timestamp;

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
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectUtils;


/**
 * Use this class to insert or update a membership
 */
public class MembershipSave {
  
  
  
  /**
   * create a new membership save
   * @param theGrouperSession
   */
  public MembershipSave() {
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
  public MembershipSave assignGroupId(String theGroupId) {
    this.groupId = theGroupId;
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
  public MembershipSave assignGroupName(String theGroupName) {
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
  public MembershipSave assignMemberId(String theMemberId) {
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
  public MembershipSave assignSubjectId(String theSubjectId) {
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
  public MembershipSave assignSubjectSourceId(String theSubjectSourceId) {
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
  public MembershipSave assignSubjectIdentifier(String theSubjectIdentifier) {
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
  public MembershipSave assignImmediateMembershipId(String theImmediateMembershipId) {
    this.immediateMembershipId = theImmediateMembershipId;
    return this;
  }

  /**
   * millis since 1970 that immediate mship disabled
   */
  private Long immediateMshipDisabledTime;

  /**
   * millis since 1970 that immediate mship disabled
   * @param theImmediateMshipDisabledTime
   * @return this for chaining
   */
  public MembershipSave assignImmediateMshipDisabledTime(Long theImmediateMshipDisabledTime) {
    this.immediateMshipDisabledTime = theImmediateMshipDisabledTime;
    return this;
  }

  /**
   * millis since 1970 that immediate mship disabled
   */
  private Long immediateMshipEnabledTime;

  /**
   * 
   * @param theImmediateMshipEnabledTime
   * @return this for chaining
   */
  public MembershipSave assignImmediateMshipEnabledTime(Long theImmediateMshipEnabledTime) {
    this.immediateMshipEnabledTime = theImmediateMshipEnabledTime;
    return this;
  }

  
  /** save mode */
  private SaveMode saveMode;

  /**
   * asssign save mode
   * @param theSaveMode
   * @return this for chaining
   */
  public MembershipSave assignSaveMode(SaveMode theSaveMode) {
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
          Group group = null;
          Member member = null;
          Subject subject = null;
          
          if (!StringUtils.isBlank(MembershipSave.this.immediateMembershipId)) {
            membership = MembershipFinder.findByUuid(GrouperSession.staticGrouperSession(), MembershipSave.this.immediateMembershipId, false, false);
          } else {
            if (!StringUtils.isBlank(MembershipSave.this.groupId)) {
              group = GroupFinder.findByUuid(GrouperSession.staticGrouperSession(), MembershipSave.this.groupId, false, new QueryOptions().secondLevelCache(false));
            } else if (!StringUtils.isBlank(MembershipSave.this.groupName)) {
              group = GroupFinder.findByName(GrouperSession.staticGrouperSession(), MembershipSave.this.groupName, false, new QueryOptions().secondLevelCache(false));
            } else {
              throw new RuntimeException("Group id or name is required");
            }
            
            if (!StringUtils.isBlank(MembershipSave.this.memberId)) {
              member = MemberFinder.findByUuid(GrouperSession.staticGrouperSession(), MembershipSave.this.groupId, false);
            } else {
              GrouperUtil.assertion(StringUtils.isNotBlank(MembershipSave.this.subjectSourceId), "subjectSourceId is required");

              if (!StringUtils.isBlank(MembershipSave.this.subjectId)) {
                subject = SubjectFinder.findByIdAndSource(MembershipSave.this.subjectId, MembershipSave.this.subjectSourceId, false);
              } else if (!StringUtils.isBlank(MembershipSave.this.subjectIdentifier)) {
                subject = SubjectFinder.findByIdentifierAndSource(MembershipSave.this.subjectIdentifier, MembershipSave.this.subjectSourceId, false);
              } else {
                throw new RuntimeException("Subject id or identifier is required");
              }
              
              if (subject != null) {
                member = MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), subject, false);
              }
              if (subject == null && member != null) {
                subject = member.getSubject();
              }
            }
            
            if (group != null && member != null) {
              membership = GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType( 
                  group.getId(), member.getId(), Group.getDefaultList(), MembershipType.IMMEDIATE.getTypeString(), true, true);
            }
          }
          
          // check security
          if (membership != null && group == null) {
            group = GroupFinder.findByUuid(GrouperSession.staticGrouperSession(), membership.getOwnerGroupId(), true, new QueryOptions().secondLevelCache(false));
          }

          GrouperUtil.assertion(group!=null, 
              "Group not found");

          GrouperUtil.assertion(group.canHavePrivilege(GrouperSession.staticGrouperSession().getSubject(), AccessPrivilege.UPDATE.getName(), false), 
              "Subject '" + SubjectUtils.subjectToString(GrouperSession.staticGrouperSession().getSubject()) + "' cannot UPDATE group '" + group.getName() + "'");
          

          // handle deletes
          if (saveMode == SaveMode.DELETE) {
            if (membership == null) {
              MembershipSave.this.saveResultType = SaveResultType.NO_CHANGE;
              return null;
            }
            membership.delete();
            MembershipSave.this.saveResultType = SaveResultType.DELETE;
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
            group.internal_addMember(subject, Group.getDefaultList(), false, MembershipSave.this.immediateMembershipId, new Timestamp(MembershipSave.this.immediateMshipEnabledTime), 
                new Timestamp(MembershipSave.this.immediateMshipDisabledTime), false);
            MembershipSave.this.saveResultType = SaveResultType.INSERT;
          } else if (GrouperUtil.equals(MembershipSave.this.immediateMshipDisabledTime, GrouperUtil.longObjectValue(membership.getDisabledTime(), true))
              && GrouperUtil.equals(MembershipSave.this.immediateMshipEnabledTime, GrouperUtil.longObjectValue(membership.getEnabledTime(), true))) {
            MembershipSave.this.saveResultType = SaveResultType.NO_CHANGE;
            return membership;
          } else {
            membership.setEnabledTime(MembershipSave.this.immediateMshipEnabledTime == null ? null : new Timestamp(MembershipSave.this.immediateMshipEnabledTime));
            membership.setDisabledTime(MembershipSave.this.immediateMshipDisabledTime == null ? null : new Timestamp(MembershipSave.this.immediateMshipDisabledTime));
            GrouperDAOFactory.getFactory().getMembership().update(membership);
            MembershipSave.this.saveResultType = SaveResultType.UPDATE;
          }
          if (membership == null) {
            
          }
          return membership;
        }
      });
  }
}
