/*******************************************************************************
 * Copyright 2016 Internet2
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
 * $Id: AttributeAssignSave.java,v 1.10 2009-11-17 02:52:29 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.attr.assign;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.keyvalue.MultiKey;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.MembershipFinder;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.finder.AttributeAssignFinder;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefFinder;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.attr.value.AttributeAssignValue;
import edu.internet2.middleware.grouper.exception.AttributeDefNameNotFoundException;
import edu.internet2.middleware.grouper.exception.GroupNotFoundException;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.exception.StemNotFoundException;
import edu.internet2.middleware.grouper.group.GroupMember;
import edu.internet2.middleware.grouper.hibernate.GrouperTransaction;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionHandler;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.misc.SaveMode;
import edu.internet2.middleware.grouper.misc.SaveResultType;
import edu.internet2.middleware.grouper.permissions.PermissionAllowed;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;


/**
 * Use this class to insert or update an attribute assign
 * e.g.
 * attributeAssign = new AttributeAssignSave(grouperSession).assignOwnerStemName("test:testFolder").assignNameOfAttributeName("a:b"c).save();
 */
public class AttributeAssignSave {

  /**
   * create a new attribute assign save
   * @param theGrouperSession
   */
  public AttributeAssignSave(GrouperSession theGrouperSession) {
    this.grouperSession = theGrouperSession;
  }
  
  /** grouper session is required */
  private GrouperSession grouperSession;

  /** save mode */
  private SaveMode saveMode;

  /**
   * assign save mode
   * @param theSaveMode
   * @return this for chaining
   */
  public AttributeAssignSave assignSaveMode(SaveMode theSaveMode) {
    this.saveMode = theSaveMode;
    return this;
  }
  
  /** save type after the save */
  private SaveResultType saveResultType = null;

  /**
   * id of action for this assignment (e.g. assign).  Generally this will be AttributeDef.ACTION_DEFAULT
   */
  private String attributeAssignActionId;
  
  /** if the subjects assigned to the attribute can delegate to someone else, or delegate as delegatable */
  private AttributeAssignDelegatable attributeAssignDelegatable;
  
  /**
   * if the subjects assigned to the attribute can delegate to someone else, or delegate as delegatable
   * @param theAttributeAssignDelegatable
   * @return this for chaining
   */
  public AttributeAssignSave assignAttributeAssignDelegatable(AttributeAssignDelegatable theAttributeAssignDelegatable) {
    this.attributeAssignDelegatable = theAttributeAssignDelegatable;
    return this;
  }
  
  /**
   * attribute assignable
   */
  private AttributeAssignable attributeAssignable;
  
  /** type of assignment */
  private AttributeAssignType attributeAssignType;

  /**
   * type of assignment
   * @param theAttributeAssignType
   * @return this for chaining
   */
  public AttributeAssignSave assignAttributeAssignType(AttributeAssignType theAttributeAssignType) {
    this.attributeAssignType = theAttributeAssignType;
    return this;
  }
  
  /** attribute name in this assignment as opposed to nameOfAttributeDefName */
  private String attributeDefNameId;

  /**
   * attribute name in this assignment as opposed to nameOfAttributeDefName
   * @param theAttributeDefNameId
   * @return this for chaining
   */
  public AttributeAssignSave assignAttributeDefNameId(String theAttributeDefNameId) {
    this.attributeDefNameId = theAttributeDefNameId;
    return this;
  }
  
  /**
   * attribute name in this assignment as opposed to nameOfAttributeDefName
   * @param theAttributeDefName
   * @return this for chaining
   */
  public AttributeAssignSave assignAttributeDefName(AttributeDefName theAttributeDefName) {
    this.attributeDefName = theAttributeDefName;
    this.attributeDefNameId = theAttributeDefName == null ? null : theAttributeDefName.getId();
    this.nameOfAttributeDefName = theAttributeDefName == null ? null : theAttributeDefName.getName();
    return this;
  }
  
  /**
   * attribute name in this assignment as opposed to attributeDefNameId
   */
  private String nameOfAttributeDefName;

  /**
   * attribute name in this assignment as opposed to attributeDefNameId
   * @param theNameOfAttributeDefName
   * @return this for chaining
   */
  public AttributeAssignSave assignNameOfAttributeDefName(String theNameOfAttributeDefName) {
    this.nameOfAttributeDefName = theNameOfAttributeDefName;
    return this;
  }
  
  /**
   * if there is a date here, and it is in the past, this assignment is disabled
   */
  private Long disabledTimeDb;

  /**
   * if there is a date here, and it is in the past, this assignment is disabled
   * @param theDisabledTime
   * @return this for chaining
   */
  public AttributeAssignSave assignDisabledTime(Long theDisabledTime) {
    this.disabledTimeDb = theDisabledTime;
    return this;
  }
  
  /**
   * if there is a date here, and it is in the past, this assignment is disabled
   * @param theDisabledTimestamp
   * @return this for chaining
   */
  public AttributeAssignSave assignDisabledTime(Timestamp theDisabledTimestamp) {
    this.disabledTimeDb = theDisabledTimestamp == null ? null : theDisabledTimestamp.getTime();
    return this;
  }
  
  /**
   * if this is a permission, then if this permission assignment is allowed or not 
   */
  private boolean disallowed = false;

  /**
   * if this is a permission, then if this permission assignment is allowed or not 
   * @param theDisallowed
   * @return this for chaining
   */
  public AttributeAssignSave assignDisallowed(boolean theDisallowed) {
    this.disallowed = theDisallowed;
    return this;
  }
  
  /**
   * if there is a date here, and it is in the future, this assignment is disabled
   * until that time
   */
  private Long enabledTimeDb;

  /**
   * if there is a date here, and it is in the future, this assignment is disabled
   * until that time
   * @param theEnabledTimeDb
   * @return this for chaining
   */
  public AttributeAssignSave assignEnabledTime(Long theEnabledTimeDb) {
    this.enabledTimeDb = theEnabledTimeDb;
    return this;
  }

  /**
   * if there is a date here, and it is in the future, this assignment is disabled
   * until that time
   * @param theEnabledTimestamp
   * @return this for chaining
   */
  public AttributeAssignSave assignEnabledTimestamp(Timestamp theEnabledTimestamp) {
    this.enabledTimeDb = theEnabledTimestamp == null ? null : theEnabledTimestamp.getTime();
    return this;
  }
  
  /** id of this attribute assign */
  private String id;

  /**
   * id of this attribute assign
   * @param theId
   * @return this for chaining
   */
  public AttributeAssignSave assignId(String theId) {
    this.id = theId;
    return this;
  }
  
  /**
   * notes about this assignment, free-form text
   */
  private String notes;

  /**
   * notes about this assignment, free-form text
   * @param theNotes
   * @return this for chaining
   */
  public AttributeAssignSave assignNotes(String theNotes) {
    this.notes = theNotes;
    return this;
  }
  
  /** 
   * if this is an attribute assign attribute, this is the foreign key 
   */
  private String ownerAttributeAssignId;

  /**
   * if this is an attribute assign attribute, this is the foreign key 
   * @param theOwnerAttributeAssignId
   * @return this for chaining
   */
  public AttributeAssignSave assignOwnerAttributeAssignId(String theOwnerAttributeAssignId) {
    this.ownerAttributeAssignId = theOwnerAttributeAssignId;
    return this;
  }
  
  /**
   * if this is an attribute assign attribute, this is the foreign key 
   * @param theOwnerAttributeAssign
   * @return this for chaining
   */
  public AttributeAssignSave assignOwnerAttributeAssign(AttributeAssign theOwnerAttributeAssign) {
    this.ownerAttributeAssignId = theOwnerAttributeAssign == null ? null : theOwnerAttributeAssign.getId();
    this.ownerAttributeAssign = theOwnerAttributeAssign;
    return this;
  }
  
  /** 
   * if this is an attribute def attribute, this is the foreign key, mutually exclusive with ownerNameOfAttributeDef
   */
  private String ownerAttributeDefId;

  /**
   * if this is an attribute def attribute, this is the foreign key, mutually exclusive with ownerNameOfAttributeDef
   * @param theOwnerAttributeDefId
   * @return this for chaining
   */
  public AttributeAssignSave assignOwnerAttributeDefId(String theOwnerAttributeDefId) {
    this.ownerAttributeDefId = theOwnerAttributeDefId;
    return this;
  }
  
  /**
   * if this is an attribute def attribute, this is the foreign key
   * @param theOwnerAttributeDef
   * @return this for chaining
   */
  public AttributeAssignSave assignOwnerAttributeDef(AttributeDef theOwnerAttributeDef) {
    this.ownerAttributeDef = theOwnerAttributeDef;
    this.ownerAttributeDefId = theOwnerAttributeDef == null ? null : theOwnerAttributeDef.getId();
    this.nameOfAttributeDefName = theOwnerAttributeDef == null ? null : theOwnerAttributeDef.getName();
    return this;
  }
  
  /**
   * if this is an attribute def attribute, this is the foreign key mutually exclusive with ownerAttributeDefId
   */
  private String ownerNameOfAttributeDef;

  /**
   * if this is an attribute def attribute, this is the foreign key mutually exclusive with ownerAttributeDefId
   * @param theOwnerNameOfAttributeDef
   * @return this for chaining
   */
  public AttributeAssignSave assignOwnerNameOfAttributeDef(String theOwnerNameOfAttributeDef) {
    this.ownerNameOfAttributeDef = theOwnerNameOfAttributeDef;
    return this;
  }
  
  /** 
   * if this is a group attribute, this is the foreign key mutually exclusive with ownerGroupName
   */
  private String ownerGroupId;

  /**
   * if this is a group attribute, this is the foreign key mutually exclusive with ownerGroupName
   * @param theOwnerGroupId
   * @return this for chaining
   */
  public AttributeAssignSave assignOwnerGroupId(String theOwnerGroupId) {
    this.ownerGroupId = theOwnerGroupId;
    return this;
  }

  /**
   * if this is a group attribute, this is the foreign key mutually exclusive with ownerGroupId
   */
  private String ownerGroupName;

  /**
   * if this is a group attribute, this is the foreign key mutually exclusive with ownerGroupId
   * @param theOwnerGroupName
   * @return this for chaining
   */
  public AttributeAssignSave assignOwnerGroupName(String theOwnerGroupName) {
    this.ownerGroupName = theOwnerGroupName;
    return this;
  }
  
  /**
   * if this is a group attribute, this is the foreign key
   * @param theOwnerGroup
   * @return this for chaining
   */
  public AttributeAssignSave assignOwnerGroup(Group theOwnerGroup) {
    this.ownerGroup = theOwnerGroup;
    this.ownerGroupId = theOwnerGroup == null ? null : theOwnerGroup.getId();
    this.ownerGroupName = theOwnerGroup == null ? null : theOwnerGroup.getName();
    return this;
  }
  
  /** 
   * if this is a member attribute, this is the foreign key, mutually exclusive with ownerMemberSubjectId(entifier) and ownerMemberSourceId
   */
  private String ownerMemberId;

  /**
   * if this is a member attribute, this is the foreign key, mutually exclusive with ownerMemberSubjectId(entifier) and ownerMemberSourceId
   * @param theOwnerMemberId
   * @return this for chaining
   */
  public AttributeAssignSave assignOwnerMemberId(String theOwnerMemberId) {
    this.ownerMemberId = theOwnerMemberId;
    return this;
  }

  /**
   * if this is a member attribute, this is the foreign key
   * @param theOwnerMember
   * @return this for chaining
   */
  public AttributeAssignSave assignOwnerMember(Member theOwnerMember) {
    this.ownerMember = theOwnerMember;
    this.ownerMemberId = theOwnerMember == null ? null : theOwnerMember.getId();
    this.ownerMemberSourceId = theOwnerMember == null ? null : theOwnerMember.getSubjectSourceId();
    this.ownerMemberSubjectId = theOwnerMember == null ? null : theOwnerMember.getSubjectId();
    //hmm, leave subject identifier alone
    return this;
  }

  /**
   * if this is a member attribute, this is the foreign key, mutually exclusive with ownerMemberId, also need to pass in ownerMemberSourceId
   */
  private String ownerMemberSubjectId;
  
  /**
   * if this is a member attribute, this is the foreign key, mutually exclusive with ownerMemberId, also need to pass in ownerMemberSourceId
   * @param theOwnerMemberSubjectId
   * @return this for chaining
   */
  public AttributeAssignSave assignOwnerMemberSubjectId(String theOwnerMemberSubjectId) {
    this.ownerMemberSubjectId = theOwnerMemberSubjectId;
    return this;
  }
  
  /**
   * if this is a member attribute, this is the foreign key, mutually exclusive with ownerMemberId, also need to pass in ownerMemberSourceId
   */
  private String ownerMemberSubjectIdentifier;
  
  /**
   * if this is a member attribute, this is the foreign key, mutually exclusive with ownerMemberId, also need to pass in ownerMemberSourceId
   * @param theOwnerMemberSubjectIdentifier
   * @return this for chaining
   */
  public AttributeAssignSave assignOwnerMemberSubjectIdentifier(String theOwnerMemberSubjectIdentifier) {
    this.ownerMemberSubjectIdentifier = theOwnerMemberSubjectIdentifier;
    return this;
  }

  /**
   * if this is a member attribute, this is the foreign key, mutually exclusive with ownerMemberId, also need to pass in ownerMemberSubjectId or ownerMemberSubjectIdentifier
   */
  private String ownerMemberSourceId;

  /**
   * if this is a member attribute, this is the foreign key, mutually exclusive with ownerMemberId, also need to pass in ownerMemberSubjectId or ownerMemberSubjectIdentifier
   * @param theOwnerMemberSourceId
   * @return this for chaining
   */
  public AttributeAssignSave assignOwnerMemberSourceId(String theOwnerMemberSourceId) {
    this.ownerMemberSourceId = theOwnerMemberSourceId;
    return this;
  }
  
  /** 
   * if this is a membership attribute, this is the foreign key.  mutually exclusive with group and member foreign keys
   */
  private String ownerMembershipId;

  /**
   * if this is a membership attribute, this is the foreign key.  mutually exclusive with group and member foreign keys
   * @param theOwnerMembershipId
   * @return this for chaining
   */
  public AttributeAssignSave assignOwnerMembershipId(String theOwnerMembershipId) {
    this.ownerMembershipId = theOwnerMembershipId;
    return this;
  }
  
  /**
   * if this is a membership attribute, this is the foreign key.  mutually exclusive with group and member foreign keys
   * @param theOwnerMembership
   * @return this for chaining
   */
  public AttributeAssignSave assignOwnerMembership(Membership theOwnerMembership) {
    this.ownerMembershipId = theOwnerMembership == null ? null : theOwnerMembership.getUuid();
    this.ownerMembership = theOwnerMembership;
    return this;
  }
  
  /**
   * if this is a stem attribute, this is the foreign key, mutually exclusive with ownerStemName
   */
  private String ownerStemId;

  /**
   * if this is a stem attribute, this is the foreign key, mutually exclusive with ownerStemName
   * @param theOwnerStemId
   * @return this for chaining
   */
  public AttributeAssignSave assignOwnerStemId(String theOwnerStemId) {
    this.ownerStemId = theOwnerStemId;
    return this;
  }
  
  /**
   * if this is a stem attribute, this is the foreign key, mutually exclusive with ownerStemName
   * @param theOwnerStem
   * @return this for chaining
   */
  public AttributeAssignSave assignOwnerStem(Stem theOwnerStem) {
    this.ownerStem = theOwnerStem;
    this.ownerStemId = theOwnerStem == null ? null : theOwnerStem.getId();
    this.ownerStemName = theOwnerStem == null ? null : theOwnerStem.getName();
    return this;
  }
  
  /**
   * if this is a stem attribute, this is the foreign key, mutually exclusive with ownerStemId
   */
  private String ownerStemName;
  
  /**
   * if this is a stem attribute, this is the foreign key, mutually exclusive with ownerStemId
   * @param theOwnerStemName
   * @return this for chaining
   */
  public AttributeAssignSave assignOwnerStemName(String theOwnerStemName) {
    this.ownerStemName = theOwnerStemName;
    return this;
  }
  
  /**
   * get the save type
   * @return save type
   */
  public SaveResultType getSaveResultType() {
    return this.saveResultType;
  }
  
  /**
   * assign the action id for the action on this assignment
   * @param theAttributeAssignActionId
   * @return the id of the action on this assignment
   */
  public AttributeAssignSave assignAttributeAssignActionId(String theAttributeAssignActionId) {
    this.attributeAssignActionId = theAttributeAssignActionId;
    return this;
  }
  
  /**
   * action for the assignment (or send in attributeAssignActionId)
   */
  private String action;
  
  /**
   * assign the action
   * @param theAction
   * @return this for chaining
   */
  public AttributeAssignSave assignAction(String theAction) {
    this.action = theAction;
    return this;
  }

  /**
   * owner group of attribute assign
   */
  private Group ownerGroup = null;

  /**
   * owner stem of attribute assign
   */
  private Stem ownerStem = null;

  /**
   * owner attributeDef of attribute assign
   */
  private AttributeDef ownerAttributeDef = null;

  /**
   * owner membership of attribute assign
   */
  private Membership ownerMembership = null;

  /**
   * owner member of attribute assign
   */
  private Member ownerMember = null;

  /**
   * owner memberSubject of attribute assign
   */
  private Subject ownerMemberSubject = null;

  /**
   * owner attributeAssign of attribute assign
   */
  private AttributeAssign ownerAttributeAssign = null;

  /**
   * attribute def name
   */
  private AttributeDefName attributeDefName;
  
  /**
   * if the attribute assign ids should be added to the set to not use
   */
  private boolean putAttributeAssignIdsToNotUseSet = false;
  
  /**
   * if the attribute assign ids should be added to the set to not use
   * @param thePutAttributeAssignIdsToNotUseSet
   * @return this for chaining
   */
  public AttributeAssignSave assignPutAttributeAssignIdsToNotUseSet(boolean thePutAttributeAssignIdsToNotUseSet) {
    this.putAttributeAssignIdsToNotUseSet = thePutAttributeAssignIdsToNotUseSet;
    return this;
  }
  
  /**
   * if doing an import, these are id's which should not be used.  The current assignment will be added to this list
   */
  private Set<String> attributeAssignIdsToNotUse = null;
  
  /**
   * this is the existing attribute assign or the new one if creating
   */
  private AttributeAssign attributeAssign = null;
  
  /**
   * if doing an import, these are id's which should not be used.  The current assignment will be added to this list
   * @param theAttributeAssignIdsToNotUse
   * @return this for chaining
   */
  public AttributeAssignSave assignAttributeAssignIdsToNotUse(Set<String> theAttributeAssignIdsToNotUse) {
    this.attributeAssignIdsToNotUse = theAttributeAssignIdsToNotUse;
    return this;
  }
  
  /**
   * if also assigning values, pass them in here
   */
  private Set<AttributeAssignValue> attributeAssignValues = null;
  
  /**
   * add a value to assign to this assignment.  add null if remove all
   * @param attributeAssignValue
   * @return this for chaining
   */
  public AttributeAssignSave addAttributeAssignValue(AttributeAssignValue attributeAssignValue) {
    if (this.attributeAssignValues == null) {
      this.attributeAssignValues = new HashSet<AttributeAssignValue>();
    }
    if (attributeAssignValue != null) {
      this.attributeAssignValues.add(attributeAssignValue);
    }
    return this;
  }
  
  /**
   * if including assignments on this assignment, put them here
   */
  private Set<AttributeAssignSave> attributeAssignsOnThisAssignment = null;
  
  /**
   * if including assignments on this assignment, put them here, put in null to remove assignments
   * @param theAttributeAssignSave
   * @return this for chaining
   */
  public AttributeAssignSave addAttributeAssignOnThisAssignment(AttributeAssignSave theAttributeAssignSave) {
    if (this.attributeAssignsOnThisAssignment == null) {
      this.attributeAssignsOnThisAssignment = new LinkedHashSet<AttributeAssignSave>();
    }
    if (theAttributeAssignSave != null) {
      this.attributeAssignsOnThisAssignment.add(theAttributeAssignSave);
    }
    return this;
  }
  
  /**
   * <pre>
   * create or update an attribute assignment
   * 
   * Steps:
   * 
   * 1. Find an existing attribute assignment
   * 2. Internally set all the fields of the assignment
   * 3. Store the assignment (insert or update) if needed
   * 4. Manage assignments on assignments and values
   * 5. Return the assignment object
   * 
   * This runs in a tx so that if part of it fails the whole thing fails, and potentially the outer
   * transaction too
   * 
   * </pre>
   * @return the assignment
   * @throws StemNotFoundException
   * @throws InsufficientPrivilegeException
   * @throws GroupNotFoundException
   * @throws AttributeDefNameNotFoundException
   */
  public AttributeAssign save() 
        throws AttributeDefNameNotFoundException, InsufficientPrivilegeException, StemNotFoundException, 
        GroupNotFoundException {

    // figure out fields, validate them
    massageAndValidateFields();

    final SaveMode SAVE_MODE = AttributeAssignSave.this.saveMode;

    final AttributeAssignSave THIS = this;
    
    try {
      GrouperTransaction.callbackGrouperTransaction(new GrouperTransactionHandler() {

        @SuppressWarnings("cast")
        @Override
        public Object callback(GrouperTransaction grouperTransaction)
            throws GrouperDAOException {
          
          grouperTransaction.setCachingEnabled(false);
          
          return (Void)GrouperSession.callbackGrouperSession(THIS.grouperSession, new GrouperSessionHandler() {

              @Override
              public Object callback(GrouperSession theGrouperSession)
                  throws GrouperSessionException {
                                
                //see if update
                boolean isUpdate = THIS.saveMode == SaveMode.UPDATE;

                THIS.findExistingAttributeAssignment();

                //while we are here, make sure uuid's match if passed in
                //not sure this can happen, if you pass in a uuid it will be used
                if (!StringUtils.isBlank(THIS.id) && THIS.attributeAssign != null && !StringUtils.equals(THIS.attributeAssign.getId(), THIS.id)) {
                  throw new RuntimeException("UUID attribute assign changes are not supported: new: " 
                      + THIS.attributeAssign.getId() + ", old: " 
                      + THIS.id);
                }

                if (THIS.attributeAssign == null) {
                  if (SAVE_MODE.equals(SaveMode.INSERT_OR_UPDATE) || SAVE_MODE.equals(SaveMode.INSERT)) {
                    isUpdate = false;
                  } else {
                    throw new RuntimeException("There is no existing attributeAssignment but the SaveMode is " + SAVE_MODE);
                  }
                } else {
                  
                  if (SAVE_MODE.equals(SaveMode.INSERT_OR_UPDATE)) {
                    isUpdate = true;
                  }
                }
                
                //default
                THIS.saveResultType = SaveResultType.NO_CHANGE;
                boolean needsSave = false;
                //if inserting
                if (!isUpdate) {
                  THIS.saveResultType = SaveResultType.INSERT;

                  if (THIS.attributeDefName.getAttributeDef().isMultiAssignable()) {
                    THIS.attributeAssign = THIS.attributeAssignable.getAttributeDelegate()
                        .addAttribute(THIS.action, THIS.attributeDefName).getAttributeAssign();
                    THIS.changesCount++;
                  } else {
                    THIS.attributeAssign = THIS.attributeAssignable.getAttributeDelegate().assignAttribute(
                        THIS.action, THIS.attributeDefName, THIS.disallowed ? PermissionAllowed.DISALLOWED : null).getAttributeAssign();
                    THIS.changesCount++;
                  }
                }

                if (GrouperUtil.length(THIS.attributeAssignIdsToNotUse) > 0 
                    && THIS.attributeAssignIdsToNotUse.contains(THIS.attributeAssign.getId())) {
                  throw new RuntimeException("AttributeAssign ID should not be used (in list to not use): " + THIS.attributeAssign.getId());
                }

                if (THIS.putAttributeAssignIdsToNotUseSet && THIS.attributeAssignIdsToNotUse != null ) {
                  THIS.attributeAssignIdsToNotUse.add(THIS.attributeAssign.getId());
                }

                //now compare and put all attributes (then store if needed)
                if (!GrouperUtil.equals(THIS.attributeAssignDelegatable, THIS.attributeAssign.getAttributeAssignDelegatable())) {
                  needsSave = true;
                  THIS.attributeAssign.setAttributeAssignDelegatable(THIS.attributeAssignDelegatable);
                }

                if (!GrouperUtil.equals(THIS.disabledTimeDb, THIS.attributeAssign.getDisabledTimeDb())) {
                  needsSave = true;
                  THIS.attributeAssign.setDisabledTimeDb(THIS.disabledTimeDb);
                }

                if (THIS.disallowed != THIS.attributeAssign.isDisallowed()) {
                  needsSave = true;
                  THIS.attributeAssign.setDisallowed(THIS.disallowed);
                }

                if (!GrouperUtil.equals(THIS.enabledTimeDb, THIS.attributeAssign.getEnabledTimeDb())) {
                  needsSave = true;
                  THIS.attributeAssign.setEnabledTimeDb(THIS.enabledTimeDb);
                }

                if (!StringUtils.equals(GrouperUtil.defaultIfEmpty(THIS.notes, ""), GrouperUtil.defaultIfEmpty(THIS.attributeAssign.getNotes(), ""))) {
                  needsSave = true;
                  THIS.attributeAssign.setNotes(THIS.notes);
                }
                
                //this is an update if it is not an insert and needs an update
                if (needsSave && THIS.saveResultType == SaveResultType.NO_CHANGE) {
                  THIS.saveResultType = SaveResultType.UPDATE;
                }

                //only store once
                if (needsSave) {
                  THIS.changesCount++;
                  THIS.attributeAssign.saveOrUpdate();
                }
                
                if (THIS.attributeAssignValues != null) {
                  Set<AttributeAssignValue> expectedAttributeAssignValues = new HashSet<AttributeAssignValue>(
                      GrouperUtil.nonNull(THIS.attributeAssignValues));
                  
                  //update values
                  THIS.changesCount += THIS.attributeAssign.getValueDelegate().replaceValues(expectedAttributeAssignValues);
                }
                
                if (THIS.attributeAssignsOnThisAssignment != null) {
                  THIS.replaceAttributeAssignmentsOnAssignments();
                }
                
                return null;
              }

          });
        }
      });
      return this.attributeAssign;
    } catch (RuntimeException re) {
      
      GrouperUtil.injectInException(re, "Problem saving attributeAssign: " + GrouperUtil.toStringForLog(this.attributeAssign, 500) 
          + ", thread: " + Integer.toHexString(Thread.currentThread().hashCode()));
      
      Throwable throwable = re.getCause();
      if (throwable instanceof InsufficientPrivilegeException) {
        throw (InsufficientPrivilegeException)throwable;
      }
      //must just be runtime
      throw re;
    }

  }
  
  /**
   * replace attributes, update if possible... works for single or multi-assign
   */
  private void replaceAttributeAssignmentsOnAssignments() {

    if (this.attributeAssignsOnThisAssignment == null) {
      return;
    }
    
    Set<String> attributeAssignAssignIdsAlreadyUsed = new HashSet<String>();

    //loop through the assignments to ensure
    for(AttributeAssignSave attributeAssignAssignSave : this.attributeAssignsOnThisAssignment) {
      
      //the owner is the attributeAssign object from the parent
      attributeAssignAssignSave.assignOwnerAttributeAssign(this.attributeAssign);
      
      attributeAssignAssignSave.assignAttributeAssignIdsToNotUse(attributeAssignAssignIdsAlreadyUsed);
      attributeAssignAssignSave.assignPutAttributeAssignIdsToNotUseSet(this.putAttributeAssignIdsToNotUseSet);
      
      //find existing (not already used)
      AttributeAssign attributeAssignAssign = attributeAssignAssignSave.save();
      
      this.changesCount += attributeAssignAssignSave.getChangesCount();
      
      //keep track of ones we have already user
      attributeAssignAssignIdsAlreadyUsed.add(attributeAssignAssign.getId());
      
    }
    
    //remove attributes not defined
    //get existing attributes
    Set<AttributeAssign> existingAttributeAssigns = this.attributeAssign.getAttributeDelegate().retrieveAssignments();

    for (AttributeAssign existingAttributeAssign : GrouperUtil.nonNull(existingAttributeAssigns)) {
      if (!attributeAssignAssignIdsAlreadyUsed.contains(existingAttributeAssign.getId())) {
        existingAttributeAssign.delete();
        this.changesCount++;
      }
    }
    
  }


  /**
   * count how many things were changed (attributes values etc)
   */
  private int changesCount = 0;

  /**
   * count how many things were changed (attributes values etc)
   * @return change count
   */
  public int getChangesCount() {
    return this.changesCount;
  }
  
  /**
   * massage and validate fields
   */
  private void massageAndValidateFields() {
    //help with incomplete entries
    if (StringUtils.isBlank(this.action)) {
      if (StringUtils.isBlank(this.attributeAssignActionId)) {
        this.action = "assign";
      }
    }
    
    if (this.attributeAssignDelegatable == null) {
      //default to false
      this.attributeAssignDelegatable = AttributeAssignDelegatable.FALSE;
    }
    
    if (!StringUtils.isBlank(this.attributeAssignActionId)) {
      AttributeAssignAction attributeAssignAction = GrouperDAOFactory.getFactory().getAttributeAssignAction().findById(this.attributeAssignActionId, true);
      if (!StringUtils.isBlank(this.action)) {
        if (!StringUtils.equals(this.action, attributeAssignAction.getName())) {
          throw new RuntimeException("Not expecting action '" + this.action + "' and attributeAssignAction.id '" + this.attributeAssignActionId + "' which is '" + attributeAssignAction.getId() + "'");
        }
      }
      this.action = attributeAssignAction.getName();
    }

    if (!StringUtils.isBlank(this.ownerGroupId)) {
      this.ownerGroup = GroupFinder.findByUuid(this.grouperSession, this.ownerGroupId, true);
    }
    if (!StringUtils.isBlank(this.ownerGroupName)) {
      if (this.ownerGroup != null && !StringUtils.equals(this.ownerGroup.getName(), this.ownerGroupName)
          && !StringUtils.equals(ownerGroup.getAlternateName(), this.ownerGroupName)) {
        throw new RuntimeException("Passing in group id and name but dont match '" + this.ownerGroupId
            + "', '" + this.ownerGroupName + "', '" + this.ownerGroup.getName() + "'");
      }
      if (this.ownerGroup == null) {
        this.ownerGroup = GroupFinder.findByName(this.grouperSession, this.ownerGroupName, true);
      }
    }

    if (this.ownerGroup != null) {
      this.ownerGroupId = this.ownerGroup.getId();
      this.ownerGroupName = this.ownerGroup.getName();
    }
    
    if (!StringUtils.isBlank(this.ownerStemId)) {
      this.ownerStem = StemFinder.findByUuid(this.grouperSession, this.ownerStemId, true);
    }
    if (!StringUtils.isBlank(this.ownerStemName)) {
      if (this.ownerStem != null && !StringUtils.equals(this.ownerStem.getName(), this.ownerStemName)
          && !StringUtils.equals(this.ownerStem.getAlternateName(), this.ownerStemName)) {
        throw new RuntimeException("Passing in stem id and name but dont match '" + this.ownerStemId
            + "', '" + this.ownerStemName + "', '" + this.ownerStem.getName() + "'");
      }
      if (this.ownerStem == null) {
        this.ownerStem = StemFinder.findByName(this.grouperSession, this.ownerStemName, true);
      }
    }

    if (this.ownerStem != null) {
      this.ownerStemId = this.ownerStem.getId();
      this.ownerStemName = this.ownerStem.getName();
    }
    
    if (!StringUtils.isBlank(this.ownerAttributeDefId)) {
      this.ownerAttributeDef = AttributeDefFinder.findById(this.ownerAttributeDefId, true);
    }
    if (!StringUtils.isBlank(this.ownerNameOfAttributeDef)) {
      if (this.ownerAttributeDef != null && !StringUtils.equals(this.ownerAttributeDef.getName(), this.ownerNameOfAttributeDef)) {
        throw new RuntimeException("Passing in owner attribute def id and name but dont match '" + this.ownerAttributeDefId
            + "', '" + this.ownerNameOfAttributeDef + "', '" + this.ownerAttributeDef.getName() + "'");
      }
      if (this.ownerAttributeDef == null) {
        this.ownerAttributeDef = AttributeDefFinder.findByName(this.ownerNameOfAttributeDef, true);
      }
    }

    if (this.ownerAttributeDef != null) {
      this.ownerAttributeDefId = this.ownerAttributeDef.getId();
      this.ownerNameOfAttributeDef = this.ownerAttributeDef.getName();
    }
    
    if (!StringUtils.isBlank(this.ownerMemberId)) {
      this.ownerMember = MemberFinder.findByUuid(this.grouperSession, this.ownerMemberId, true);
    }
    if (!StringUtils.isBlank(this.ownerMemberSourceId) && this.ownerMember != null) {
      if (!StringUtils.equals(this.ownerMember.getSubjectSourceId(), this.ownerMemberSourceId)) {
        throw new RuntimeException("Passing in owner member id and owner member source id but dont match '" + this.ownerMemberId
            + "', '" + this.ownerMemberSourceId + "', '" + ownerMemberSubject.getSourceId() + "'");
      }
    }
    if (!StringUtils.isBlank(this.ownerMemberSubjectId) || !StringUtils.isBlank(this.ownerMemberSubjectIdentifier)) {
      Member ownerMemberTemp1 = null;
      Member ownerMemberTemp2 = null;
      if (!StringUtils.isBlank(this.ownerMemberSubjectId)) {
        if (!StringUtils.isBlank(this.ownerMemberSourceId)) {
          Subject subject = SubjectFinder.findByIdAndSource(this.ownerMemberSubjectId, this.ownerMemberSourceId, true);
          ownerMemberTemp1 = MemberFinder.findBySubject(this.grouperSession, subject, true);
        } else {
          Subject subject = SubjectFinder.findById(this.ownerMemberSubjectId, true);
          ownerMemberTemp1 = MemberFinder.findBySubject(this.grouperSession, subject, true);
        }
      } else if (!StringUtils.isBlank(this.ownerMemberSubjectIdentifier)) {
        if (!StringUtils.isBlank(this.ownerMemberSourceId)) {
          Subject subject = SubjectFinder.findByIdentifierAndSource(this.ownerMemberSubjectIdentifier, this.ownerMemberSourceId, true);
          ownerMemberTemp2 = MemberFinder.findBySubject(this.grouperSession, subject, true);
        } else {
          Subject subject = SubjectFinder.findById(this.ownerMemberSubjectIdentifier, true);
          ownerMemberTemp2 = MemberFinder.findBySubject(this.grouperSession, subject, true);
        }
      }
      if (ownerMemberTemp1 != null && ownerMemberTemp2 != null) {
        if (!StringUtils.equals(ownerMemberTemp1.getUuid(), ownerMemberTemp2.getUuid())) {
          throw new RuntimeException("Passing in owner member subject id and owner member subject identifier but dont match '" + this.ownerMemberSubjectId
              + "', '" + this.ownerMemberSubjectIdentifier + "', '" + ownerMemberTemp1.getSubjectId() + "'"
              + "', '" + ownerMemberTemp2.getSubjectId() + ", " + ownerMemberTemp1.getUuid() + "', '"
              + ownerMemberTemp2.getUuid() + "'");
        }
      }
      //if identifier and no id, move it over
      if (ownerMemberTemp1 == null) {
        ownerMemberTemp1 = ownerMemberTemp2;
      }
      //if id or identifier and uuid, make sure they match
      if (ownerMemberTemp1 != null && this.ownerMember != null) {
        if (!StringUtils.equals(ownerMemberTemp1.getUuid(), this.ownerMember.getUuid())) {
          throw new RuntimeException("Passing in owner member subject id(entifier) and owner member id but dont match '" 
              + this.ownerMemberSubjectId
              + "', '" + this.ownerMemberSubjectIdentifier + "', '" + ownerMemberId + "', '" 
              + ownerMemberTemp1.getSubjectId() + "'"
              + "', '" + this.ownerMember.getSubjectId() + ", " + ownerMemberTemp1.getUuid() + "', '"
              + this.ownerMember.getUuid() + "'");
        }
        //if id or identifier and no uuid, move it over
        if (this.ownerMember == null) {
          this.ownerMember = ownerMemberTemp1;
        }
      }
    }
    if (this.ownerMember != null) {
      this.ownerMemberSubject = this.ownerMember.getSubject();
      this.ownerMemberId = this.ownerMember.getId();
      this.ownerMemberSubjectId = this.ownerMember.getSubjectId();
      this.ownerMemberSourceId = this.ownerMember.getSubjectSourceId();
    }
    
    if (this.ownerAttributeAssignId != null) {
      ownerAttributeAssign = GrouperDAOFactory.getFactory().getAttributeAssign().findById(this.ownerAttributeAssignId, true);
    }

    if (this.ownerMembershipId != null) {
      ownerMembership = MembershipFinder.findByUuid(this.grouperSession, this.ownerMembershipId, true, false); 
    }

    this.attributeDefName = null;
    
    if (!StringUtils.isBlank(this.attributeDefNameId)) {
      this.attributeDefName = AttributeDefNameFinder.findById(this.attributeDefNameId, true);
      if (!StringUtils.isBlank(this.nameOfAttributeDefName)) {
        if (!StringUtils.equals(this.attributeDefName.getName(), this.nameOfAttributeDefName)) {
          throw new RuntimeException("Passing in attributeDefNameId and nameOfAttributeDefName but dont match '" + this.attributeDefNameId
              + "', '" + this.nameOfAttributeDefName + "', '" + this.attributeDefName.getName() + "'");
        }
        
      }
    }
    if (this.attributeDefName == null && !StringUtils.isBlank(this.nameOfAttributeDefName)) {
      this.attributeDefName = AttributeDefNameFinder.findByName(this.nameOfAttributeDefName, true);
    }
    if (this.attributeDefName == null) {
      throw new RuntimeException("attributeDefNameId or nameOfAttributeDefName are required");
    }
    this.attributeDefNameId = this.attributeDefName.getId();
    this.nameOfAttributeDefName = this.attributeDefName.getName();

    // stem
    if (this.ownerStem != null) {
      if (this.attributeAssignType == null) {
        this.attributeAssignType = AttributeAssignType.stem;
      }
      if (this.attributeAssignType != AttributeAssignType.stem) {
        throw new RuntimeException("attributeAssignType needs to be stem if passing in a stem: " + this.attributeAssignType);
      }
      this.attributeAssignable = this.ownerStem;
      if (this.ownerAttributeAssign != null || this.ownerAttributeDef != null || this.ownerGroup != null
          || this.ownerMember != null || this.ownerMembership != null) {
        throw new RuntimeException("Passing in too many types of owners: attributeAssign: "
            + this.ownerAttributeAssign + ", attributeDef: " + this.ownerAttributeDef + ", ownerGroup: " + this.ownerGroup
            + ", ownerMember: " + this.ownerMember + ", ownerMembership: " + this.ownerMembership + ", ownerStem: " + this.ownerStem);
      }
    }

    // attributeDef
    if (this.ownerAttributeDef != null) {
      if (this.attributeAssignType == null) {
        this.attributeAssignType = AttributeAssignType.attr_def;
      }
      if (this.attributeAssignType != AttributeAssignType.attr_def) {
        throw new RuntimeException("attributeAssignType needs to be attr_def if passing in an attr_def: " + this.attributeAssignType);
      }
      this.attributeAssignable = this.ownerAttributeDef;
      if (this.ownerAttributeAssign != null || this.ownerStem != null || this.ownerGroup != null
          || this.ownerMember != null || this.ownerMembership != null) {
        throw new RuntimeException("Passing in too many types of owners: attributeAssign: "
            + this.ownerAttributeAssign + ", attributeDef: " + this.ownerAttributeDef + ", ownerGroup: " + this.ownerGroup
            + ", ownerMember: " + this.ownerMember + ", ownerMembership: " + this.ownerMembership + ", ownerStem: " + this.ownerStem);
      }
    }

    //member
    if (ownerMember != null && ownerGroup == null) {
      if (this.attributeAssignType == null) {
        this.attributeAssignType = AttributeAssignType.member;
      }
      if (this.attributeAssignType != AttributeAssignType.member) {
        throw new RuntimeException("attributeAssignType needs to be member if passing in a member: " + this.attributeAssignType);
      }
      this.attributeAssignable = this.ownerMember;
      if (this.ownerAttributeAssign != null || this.ownerAttributeDef != null || this.ownerGroup != null
          || this.ownerMembership != null || this.ownerStem != null) {
        throw new RuntimeException("Passing in too many types of owners: attributeAssign: "
            + this.ownerAttributeAssign + ", attributeDef: " + this.ownerAttributeDef + ", ownerGroup: " + this.ownerGroup
            + ", ownerMember: " + this.ownerMember + ", ownerMembership: " + this.ownerMembership + ", ownerStem: " + this.ownerStem);
      }
    }

    // group
    if (this.ownerMember == null && this.ownerGroup != null) {
      if (this.attributeAssignType == null) {
        this.attributeAssignType = AttributeAssignType.group;
      }
      if (this.attributeAssignType != AttributeAssignType.group) {
        throw new RuntimeException("attributeAssignType needs to be group if passing in a group: " + this.attributeAssignType);
      }
      this.attributeAssignable = this.ownerGroup;
      if (this.ownerAttributeAssign != null || this.ownerAttributeDef != null || this.ownerMember != null
          || this.ownerMembership != null || this.ownerStem != null) {
        throw new RuntimeException("Passing in too many types of owners: attributeAssign: "
            + this.ownerAttributeAssign + ", attributeDef: " + this.ownerAttributeDef + ", ownerGroup: " + this.ownerGroup
            + ", ownerMember: " + this.ownerMember + ", ownerMembership: " + this.ownerMembership + ", ownerStem: " + this.ownerStem);
      }
    }

    // eff membership
    if (this.ownerMember != null && this.ownerGroup != null) {
      if (this.attributeAssignType == null) {
        this.attributeAssignType = AttributeAssignType.any_mem;
      }
      if (this.attributeAssignType == AttributeAssignType.imm_mem) {
        Membership membership = new MembershipFinder().addGroup(this.ownerGroup).addMemberId(this.ownerMember.getId())
            .assignField(Group.getDefaultList()).findMembership(false);
        if (membership == null) {
          throw new RuntimeException("Cant find immediate membership on group: " + this.ownerGroupName + ", and subject: " + GrouperUtil.subjectToString(this.ownerMember.getSubject()));
        }
        this.assignOwnerMembership(membership);
        this.assignOwnerGroup(null);
        this.assignOwnerMember(null);
      } else {
        if (this.attributeAssignType != AttributeAssignType.any_mem) {
          throw new RuntimeException("attributeAssignType needs to be any_mem if passing in a member and group: " + this.attributeAssignType);
        }
        this.attributeAssignable = new GroupMember(this.ownerGroup, this.ownerMember);
        if (this.ownerAttributeAssign != null || this.ownerAttributeDef != null
            || this.ownerMembership != null || this.ownerStem != null) {
          throw new RuntimeException("Passing in too many types of owners: attributeAssign: "
              + this.ownerAttributeAssign + ", attributeDef: " + this.ownerAttributeDef + ", ownerGroup: " + this.ownerGroup
              + ", ownerMember: " + this.ownerMember + ", ownerMembership: " + this.ownerMembership + ", ownerStem: " + this.ownerStem);
        }
      }
    }

    //imm membership
    if (this.ownerMembership != null) {
      if (this.attributeAssignType == null) {
        this.attributeAssignType = AttributeAssignType.imm_mem;
      }
      if (this.attributeAssignType != AttributeAssignType.imm_mem) {
        throw new RuntimeException("attributeAssignType needs to be imm_mem if passing in a membership: " + this.attributeAssignType);
      }
      this.attributeAssignable = this.ownerMembership;
      if (this.ownerAttributeAssign != null || this.ownerAttributeDef != null || this.ownerGroup != null || this.ownerMember != null
          || this.ownerStem != null) {
        throw new RuntimeException("Passing in too many types of owners: attributeAssign: "
            + this.ownerAttributeAssign + ", attributeDef: " + this.ownerAttributeDef + ", ownerGroup: " + this.ownerGroup
            + ", ownerMember: " + this.ownerMember + ", ownerMembership: " + this.ownerMembership + ", ownerStem: " + this.ownerStem);
      }
    }
    
    // attributeAssign
    if (this.ownerAttributeAssign != null) {
      
      if (this.attributeAssignType == null) {
        this.attributeAssignType = this.ownerAttributeAssign.getAttributeAssignType().getAssignmentOnAssignmentType();
      }
      if (this.attributeAssignType != this.ownerAttributeAssign.getAttributeAssignType().getAssignmentOnAssignmentType()) {
        throw new RuntimeException("attributeAssignType needs to be " + ownerAttributeAssign.getAttributeAssignType().getAssignmentOnAssignmentType() 
            + " if passing in an assignment of: " + ownerAttributeAssign.getAttributeAssignType().name() + ", " + this.attributeAssignType);
      }
      this.attributeAssignable = this.ownerAttributeAssign;
      if (this.ownerAttributeDef != null || this.ownerStem != null || this.ownerGroup != null || this.ownerMember != null || this.ownerMembership != null) {
        throw new RuntimeException("Passing in too many types of owners: attributeAssign: "
            + this.ownerAttributeAssign + ", attributeDef: " + this.ownerAttributeDef + ", ownerGroup: " + this.ownerGroup
            + ", ownerMember: " + this.ownerMember + ", ownerMembership: " + this.ownerMembership + ", ownerStem: " + this.ownerStem);
      }
    }
    
    if (this.attributeAssignType == null) {
      throw new RuntimeException("Why is attributeAssignType still null???");
    }
    
    //default to insert or update
    this.saveMode = (SaveMode)ObjectUtils.defaultIfNull(this.saveMode, SaveMode.INSERT_OR_UPDATE);

  }
  
  /**
   * find an existing assignment that is similar this request
   */
  private void findExistingAttributeAssignment() {
    //query to find all the assignments of this attribute on this owner, page this
    Set<AttributeAssign> existingAttributeAssigns = null;
    
    if (this.attributeAssignType == null) {
      throw new RuntimeException("This shouldnt happen, attributeAssignType is null");
    }

    //if we have an id, use it
    if (!StringUtils.isBlank(this.id)) {
      this.attributeAssign = AttributeAssignFinder.findById(this.id, true);
      return;
    }
    
    ////if this is an assignment on assignment, then it is just the owner (if exists... I guess it should)
    //if (this.attributeAssignType.isAssignmentOnAssignment()) {
    //  this.attributeAssign = this.ownerAttributeAssign;
    //  return;
    //}
    
    switch(this.attributeAssignType) {
      case group:
        existingAttributeAssigns = GrouperDAOFactory.getFactory().getAttributeAssign().findGroupAttributeAssignments(null, null, GrouperUtil.toSet(this.attributeDefName.getId()), 
            GrouperUtil.toSet(this.ownerGroup.getId()), GrouperUtil.toSet(this.action), null, true);
        break;
      case stem:
        existingAttributeAssigns = GrouperDAOFactory.getFactory().getAttributeAssign().findStemAttributeAssignments(null, null, GrouperUtil.toSet(this.attributeDefName.getId()), 
            GrouperUtil.toSet(this.ownerStem.getId()), GrouperUtil.toSet(this.action), null, true);
        break;
      case attr_def:
        existingAttributeAssigns = GrouperDAOFactory.getFactory().getAttributeAssign().findAttributeDefAttributeAssignments(null, null, GrouperUtil.toSet(this.attributeDefName.getId()), 
            GrouperUtil.toSet(this.ownerAttributeDef.getId()), GrouperUtil.toSet(this.action), null, true);
        break;
      case member:
        existingAttributeAssigns = GrouperDAOFactory.getFactory().getAttributeAssign().findMemberAttributeAssignments(null, null, GrouperUtil.toSet(this.attributeDefName.getId()), 
            GrouperUtil.toSet(this.ownerStem.getId()), GrouperUtil.toSet(this.action), null, true);
        break;
      case imm_mem:
        existingAttributeAssigns = GrouperDAOFactory.getFactory().getAttributeAssign().findMembershipAttributeAssignments(null, null, GrouperUtil.toSet(this.attributeDefName.getId()), 
            GrouperUtil.toSet(this.ownerMembership.getImmediateMembershipId()), GrouperUtil.toSet(this.action), null, true);
        break;
      case any_mem:
        existingAttributeAssigns = GrouperDAOFactory.getFactory().getAttributeAssign().findAnyMembershipAttributeAssignments(null, null, GrouperUtil.toSet(this.attributeDefName.getId()), 
            GrouperUtil.toSet(new MultiKey(this.ownerGroup.getId(), this.ownerMember.getId())), GrouperUtil.toSet(this.action), null, true);
        break;
      case group_asgn:
        existingAttributeAssigns = GrouperDAOFactory.getFactory().getAttributeAssign().findGroupAttributeAssignmentsOnAssignments(
            null, null, GrouperUtil.toSet(this.attributeDefName.getId()), 
            null, GrouperUtil.toSet(this.action), null, null, null, null, false, GrouperUtil.toSet(this.ownerAttributeAssign.getId()), null, null, null, false);
        break;
      case stem_asgn:
        existingAttributeAssigns = GrouperDAOFactory.getFactory().getAttributeAssign().findStemAttributeAssignmentsOnAssignments(
            null, null, GrouperUtil.toSet(this.attributeDefName.getId()), 
            null, GrouperUtil.toSet(this.action), null, null, null, null, false, GrouperUtil.toSet(this.ownerAttributeAssign.getId()), null, null, null, false);
        break;
      case attr_def_asgn:
        existingAttributeAssigns = GrouperDAOFactory.getFactory().getAttributeAssign().findAttributeDefAttributeAssignmentsOnAssignments(
            null, null, GrouperUtil.toSet(this.attributeDefName.getId()), 
            null, GrouperUtil.toSet(this.action), null, null, null, null, false, GrouperUtil.toSet(this.ownerAttributeAssign.getId()), null, null, null, false);
        break;
      case mem_asgn:
        existingAttributeAssigns = GrouperDAOFactory.getFactory().getAttributeAssign().findMemberAttributeAssignmentsOnAssignments(
            null, null, GrouperUtil.toSet(this.attributeDefName.getId()), 
            null, GrouperUtil.toSet(this.action), null, null, null, null, false, GrouperUtil.toSet(this.ownerAttributeAssign.getId()), null, null, null, false);
        break;
      case imm_mem_asgn:
        existingAttributeAssigns = GrouperDAOFactory.getFactory().getAttributeAssign().findMembershipAttributeAssignmentsOnAssignments(
            null, null, GrouperUtil.toSet(this.attributeDefName.getId()), 
            null, GrouperUtil.toSet(this.action), null, null, null, null, false, 
            GrouperUtil.toSet(this.ownerAttributeAssign.getId()), null, null, null, false);
        break;
      case any_mem_asgn:
        existingAttributeAssigns = GrouperDAOFactory.getFactory().getAttributeAssign().findAnyMembershipAttributeAssignmentsOnAssignments(
            null, null, GrouperUtil.toSet(this.attributeDefName.getId()), 
            null, GrouperUtil.toSet(this.action), null, null, null, null, false, 
            GrouperUtil.toSet(this.ownerAttributeAssign.getId()), null, null, null, false);
        break;
    }
    
    if (GrouperUtil.length(existingAttributeAssigns) == 0) {
      return;
    }
    
    //remove any already existing
    if (GrouperUtil.length(this.attributeAssignIdsToNotUse) > 0) {
      existingAttributeAssigns = new HashSet<AttributeAssign>(existingAttributeAssigns);
      Iterator<AttributeAssign> iterator = existingAttributeAssigns.iterator();
      while (iterator.hasNext()) {
        AttributeAssign attributeAssignCurrent = iterator.next();
        if (this.attributeAssignIdsToNotUse.contains(attributeAssignCurrent.getId()) 
            || this.attributeAssignIdsToNotUse.contains(attributeAssignCurrent.getOwnerAttributeAssignId())) {
          iterator.remove();
        }
      }
    }

    if (GrouperUtil.length(existingAttributeAssigns) == 0) {
      return;
    }
    
    //if this is an assignment on an object, not an assignment on an assignment
    if (!this.attributeAssignType.isAssignmentOnAssignment()) {
      
      //pick the best one.  is there only one?
      Set<AttributeAssign> existingImmediateAssignments = new HashSet<AttributeAssign>();
      
      for (AttributeAssign attributeAssignCurrent : existingAttributeAssigns) {
  
        //if this is one of the ones to not use
        if (this.attributeAssignIdsToNotUse != null && this.attributeAssignIdsToNotUse.contains(attributeAssignCurrent.getId())) {
          continue;
        }
        
        if (!attributeAssignCurrent.getAttributeAssignType().isAssignmentOnAssignment()) {
          existingImmediateAssignments.add(attributeAssignCurrent);
        }
        
      }
  
      if (GrouperUtil.length(existingImmediateAssignments) == 0) {
        return;
      }
  
      if (GrouperUtil.length(existingImmediateAssignments) == 1) {
        this.attributeAssign = existingImmediateAssignments.iterator().next();
        return;
      }
  
      //convert to attribute assign ids
      Set<String> existingImmediateAttributeAssignIds = new HashSet<String>();
      for (AttributeAssign current : existingImmediateAssignments) {
        existingImmediateAttributeAssignIds.add(current.getId());
      }
      
      //look at attribute assignments and values
      Set<AttributeAssignValue> existingAttributeAssignValuesOnAssigns = GrouperDAOFactory.getFactory()
          .getAttributeAssignValue().findValuesOnAssignments(existingImmediateAttributeAssignIds, null, null, null);
  
      Set<AttributeAssignValue> existingAttributeAssignValues = GrouperDAOFactory.getFactory()
          .getAttributeAssignValue().findByAttributeAssignIds(existingImmediateAttributeAssignIds);
  
      Set<AttributeAssignValue> allExistingValues = new HashSet<AttributeAssignValue>();
      allExistingValues.addAll(existingAttributeAssignValuesOnAssigns);
      allExistingValues.addAll(existingAttributeAssignValues);
      
      Map<String, List<Object>> existingAttributeAssignIdToValueSet = new HashMap<String, List<Object>>();
          
      for (AttributeAssignValue currentExistingValue : allExistingValues) {
  
        //lazy load the values set
        List<Object> existingValues = existingAttributeAssignIdToValueSet.get(currentExistingValue.getAttributeAssignId());
        if (existingValues == null) {
          existingValues = new ArrayList<Object>();
          existingAttributeAssignIdToValueSet.put(currentExistingValue.getAttributeAssignId(), existingValues);
        }
  
        //get the value
        Object value = currentExistingValue.getValue();
  
        //add it to the set
        existingValues.add(value);
      }
  
      //keep track of assignments on each assignment
      Map<String, Set<AttributeAssign>> existingAttributeAssignIdToAssignmentsOfAssignments = new HashMap<String, Set<AttributeAssign>>();
      
      for (AttributeAssign existingAttributeAssignOnAssign : GrouperUtil.nonNull(existingAttributeAssigns)) {
        String ownerAttributeAssignId = existingAttributeAssignOnAssign.getOwnerAttributeAssignId();
        
        if (!StringUtils.isBlank(ownerAttributeAssignId)) {
          Set<AttributeAssign> existingAssignsOnAssign = existingAttributeAssignIdToAssignmentsOfAssignments.get(ownerAttributeAssignId);
          if (existingAssignsOnAssign == null) {
            existingAssignsOnAssign = new HashSet<AttributeAssign>();
            existingAttributeAssignIdToAssignmentsOfAssignments.put(ownerAttributeAssignId, existingAssignsOnAssign);
          }
          
          existingAssignsOnAssign.add(existingAttributeAssignOnAssign);
        }
      }
      
      int bestScore = -99999999;
      //loop through existing assignments
      for (AttributeAssign currentImmediateAssignment : existingImmediateAssignments) {
        
        //lets look at values
        List<Object> existingCurrentValuesOnImmediateAssignment = existingAttributeAssignIdToValueSet.get(currentImmediateAssignment.getId());
        Set<AttributeAssign> existingAttributeAssignmentsOnAssignment = existingAttributeAssignIdToAssignmentsOfAssignments.get(currentImmediateAssignment.getId());
        int currentScore = computeScoreForExistingAssignment(this.attributeAssignValues, 
            existingCurrentValuesOnImmediateAssignment, this.attributeAssignsOnThisAssignment, 
            existingAttributeAssignmentsOnAssignment, existingAttributeAssignIdToValueSet);
        
        if (currentScore > bestScore) {
          bestScore = currentScore;
          this.attributeAssign = currentImmediateAssignment;
        }
        
      }
    } else {
      //the attribute assign type is an assignment on an assignment
      //pick the best one.  is there only one?
      
      for (AttributeAssign attributeAssignCurrent : existingAttributeAssigns) {
  
        //if this is one of the ones to not use
        if (this.attributeAssignIdsToNotUse != null && this.attributeAssignIdsToNotUse.contains(attributeAssignCurrent.getId())) {
          continue;
        }
        
      }
  
      if (GrouperUtil.length(existingAttributeAssigns) == 1) {
        this.attributeAssign = existingAttributeAssigns.iterator().next();
        return;
      }
  
      //convert to attribute assign ids
      Set<String> existingAttributeAssignIds = new HashSet<String>();
      for (AttributeAssign current : existingAttributeAssigns) {
        existingAttributeAssignIds.add(current.getId());
      }
      
      //look at values
      Set<AttributeAssignValue> existingAttributeAssignValues = GrouperDAOFactory.getFactory()
          .getAttributeAssignValue().findByAttributeAssignIds(existingAttributeAssignIds);
  
      Map<String, List<Object>> existingAttributeAssignIdToValueSet = new HashMap<String, List<Object>>();
          
      for (AttributeAssignValue currentExistingValue : existingAttributeAssignValues) {
  
        //lazy load the values set
        List<Object> existingValues = existingAttributeAssignIdToValueSet.get(currentExistingValue.getAttributeAssignId());
        if (existingValues == null) {
          existingValues = new ArrayList<Object>();
          existingAttributeAssignIdToValueSet.put(currentExistingValue.getAttributeAssignId(), existingValues);
        }
  
        //get the value
        Object value = currentExistingValue.getValue();
  
        //add it to the set
        existingValues.add(value);
      }
  
      int bestScore = -99999999;
      //loop through existing assignments
      for (AttributeAssign currentImmediateAssignment : existingAttributeAssigns) {
        
        //lets look at values
        List<Object> existingCurrentValuesOnImmediateAssignment = existingAttributeAssignIdToValueSet.get(currentImmediateAssignment.getId());
        int currentScore = computeScoreForExistingAssignment(this.attributeAssignValues, 
            existingCurrentValuesOnImmediateAssignment, null, 
            null, existingAttributeAssignIdToValueSet);
        
        if (currentScore > bestScore) {
          bestScore = currentScore;
          this.attributeAssign = currentImmediateAssignment;
        }
        
      }
    }
  }
  

  /**
   * score this by value is a point, attribute assign on assign is a point, and attribute value on assign is 2 points
   * if the attribute assign is wrong on either end, subtract a point
   * if the attribute assign value is wrong on either end, subtract a point
   * @param expectedValues
   * @param existingValues
   * @param expectedAssignmentsOnAssignment
   * @param existingAssignmentsOnAssignment
   * @param valuesOnAttributeAssignments 
   * @return the score
   */
  static int computeScoreForExistingAssignment(Set<AttributeAssignValue> expectedValues, 
      List<Object> existingValues, Set<AttributeAssignSave> expectedAssignmentsOnAssignment, 
      Set<AttributeAssign> existingAssignmentsOnAssignment, Map<String, List<Object>> valuesOnAttributeAssignments) {

    int score = 0;

    score += computeValueScore(expectedValues, existingValues);

    //copy the set so we can remove things
    existingAssignmentsOnAssignment = existingAssignmentsOnAssignment == null ? 
        new HashSet<AttributeAssign>() : new HashSet<AttributeAssign>(existingAssignmentsOnAssignment);

    expectedAssignmentsOnAssignment = expectedAssignmentsOnAssignment == null ? 
        new HashSet<AttributeAssignSave>() : new HashSet<AttributeAssignSave>(expectedAssignmentsOnAssignment);

    Iterator<AttributeAssignSave> expectedAttributeAssignSaveIterator = expectedAssignmentsOnAssignment.iterator();    
        
    //now lets look at assignments on assignments
    OUTER: while (expectedAttributeAssignSaveIterator.hasNext()) {
      
      AttributeAssignSave expectedAssignmentOnAssignment = expectedAttributeAssignSaveIterator.next();
      
      //find in existing values
      Iterator<AttributeAssign> existingAttributeAssignIterator = existingAssignmentsOnAssignment.iterator();

      while (existingAttributeAssignIterator.hasNext()) {
        AttributeAssign existingAssign = existingAttributeAssignIterator.next();
//        AttributeDefName existingAttributeDefName = existingAssign.getAttributeDefName();
//        String nameOfAttributeDefName = existingAttributeDefName.getName();
        
        //hmmm, do we need to find the best match or the first match?   currently the first match
        if (!StringUtils.isBlank(expectedAssignmentOnAssignment.attributeDefNameId) && StringUtils.equals(expectedAssignmentOnAssignment.attributeDefNameId, existingAssign.getAttributeDefNameId())) {
          existingAttributeAssignIterator.remove();
          expectedAttributeAssignSaveIterator.remove();
          score++;
          
          //do the values
          List<Object> existingAttributeOnAttributeValues = valuesOnAttributeAssignments.get(existingAssign.getId());
          Set<AttributeAssignValue> expectedAttributeOnAttributeValues = expectedAssignmentOnAssignment.attributeAssignValues;
          score += computeValueScore(expectedAttributeOnAttributeValues, existingAttributeOnAttributeValues);
          
          continue OUTER;
        }        
      }

      
    }
    score -= existingAssignmentsOnAssignment.size();
    
    score -= expectedAssignmentsOnAssignment.size();
    
    return score;
  }

  /**
   * @param expectedValues
   * @param existingValues
   * @return value score
   */
  private static int computeValueScore(Set<AttributeAssignValue> expectedValues,
      List<Object> existingValues) {
    
    int score = 0;
    
    //turn this into a linked list so we can remove things quickly
    existingValues = existingValues == null ? new LinkedList<Object>() : new LinkedList<Object>(existingValues);

    //do values first
    for (AttributeAssignValue expectedValue : GrouperUtil.nonNull(expectedValues)) {

      Object expectedValueObject = expectedValue.getValue();

      //find in existing values
      Iterator<Object> existingValuesIterator = existingValues.iterator();

      while (existingValuesIterator.hasNext()) {
        Object existingValue = existingValuesIterator.next();
        if (GrouperUtil.equals(expectedValueObject, existingValue)) {
          existingValuesIterator.remove();
          score++;
          continue;
        }
      }

      //couldnt find the value, thats ok

    }

    //see how many existing values didnt match
    score -= existingValues.size();
    return score;
  }
  
}
