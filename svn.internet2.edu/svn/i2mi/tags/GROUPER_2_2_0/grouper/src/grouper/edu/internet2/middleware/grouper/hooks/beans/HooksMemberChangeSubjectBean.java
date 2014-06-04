/*******************************************************************************
 * Copyright 2012 Internet2
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
 ******************************************************************************/
/*
 * @author mchyzer
 * $Id: HooksMemberChangeSubjectBean.java,v 1.1 2008-10-17 12:06:37 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks.beans;

import java.util.Set;

import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.annotations.GrouperIgnoreDbVersion;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;


/**
 * bean to hold objects for member change subject operation
 */
@GrouperIgnoreDbVersion
public class HooksMemberChangeSubjectBean extends HooksBean {
  
  //*****  START GENERATED WITH GenerateFieldConstants.java *****//

  /** constant for field name for: deletingOldMember */
  public static final String FIELD_DELETING_OLD_MEMBER = "deletingOldMember";

  /** constant for field name for: member */
  public static final String FIELD_MEMBER = "member";

  /** constant for field name for: newMemberDidntExist */
  public static final String FIELD_NEW_MEMBER_DIDNT_EXIST = "newMemberDidntExist";

  /** constant for field name for: newSubject */
  public static final String FIELD_NEW_SUBJECT = "newSubject";

  /** constant for field name for: oldSubjectId */
  public static final String FIELD_OLD_SUBJECT_ID = "oldSubjectId";

  /** constant for field name for: oldSubjectSourceId */
  public static final String FIELD_OLD_SUBJECT_SOURCE_ID = "oldSubjectSourceId";

  /**
   * fields which are included in clone method
   */
  private static final Set<String> CLONE_FIELDS = GrouperUtil.toSet(
      FIELD_DELETING_OLD_MEMBER, FIELD_MEMBER, FIELD_NEW_MEMBER_DIDNT_EXIST, FIELD_NEW_SUBJECT, 
      FIELD_OLD_SUBJECT_ID, FIELD_OLD_SUBJECT_SOURCE_ID);

  //*****  END GENERATED WITH GenerateFieldConstants.java *****//
  
  /** object being affected */
  private Member member = null;
  
  /** new subject */
  private Subject newSubject = null;
  
  /** old subject source id */
  private String oldSubjectId = null;

  /** old subject source id */
  private String oldSubjectSourceId = null;
  
  /** if we are deleting the old member object (only applicable if new member exists) */
  private boolean deletingOldMember = false;
  
  /** if the new member didnt exist, then just put the new subject in the old member */
  private boolean newMemberDidntExist = false; 

  /**
   * 
   */
  public HooksMemberChangeSubjectBean() {
    super();
  }

  
  /**
   * construct
   * @param theMember
   * @param theNewSubject
   * @param theOldSubjectId 
   * @param theOldSubjectSourceId 
   * @param theDeletingOldMember
   * @param theNewMemberDidntExist
   */
  public HooksMemberChangeSubjectBean(Member theMember, Subject theNewSubject,
      String theOldSubjectId, String theOldSubjectSourceId, boolean theDeletingOldMember, 
      boolean theNewMemberDidntExist) {
    this.member = theMember;
    this.newSubject = theNewSubject;
    this.oldSubjectId = theOldSubjectId;
    this.oldSubjectSourceId = theOldSubjectSourceId;
    this.deletingOldMember = theDeletingOldMember;
    this.newMemberDidntExist = theNewMemberDidntExist;
  }


  /**
   * object being inserted
   * @return the Member
   */
  public Member getMember() {
    return this.member;
  }

  /**
   * subject that is being changed to
   * @return the new subject
   */
  public Subject getNewSubject() {
    return this.newSubject;
  }
  
  /**
   * deep clone the fields in this object
   */
  @Override
  public HooksMemberChangeSubjectBean clone() {
    return GrouperUtil.clone(this, CLONE_FIELDS);
  }

  /**
   * old subject info
   * @return old subject
   */
  public String getOldSubjectId() {
    return this.oldSubjectId;
  }

  /**
   * old subject info
   * @return old subject
   */
  public String getOldSubjectSourceId() {
    return this.oldSubjectSourceId;
  }

  /**
   * if we are deleting the old member object (only applicable if new member exists)
   * @return true/false
   */
  public boolean isDeletingOldMember() {
    return this.deletingOldMember;
  }

  /**
   * if the new member didnt exist, then just put the new subject in the old member
   * @return true/false
   */
  public boolean isNewMemberDidntExist() {
    return this.newMemberDidntExist;
  }
}
