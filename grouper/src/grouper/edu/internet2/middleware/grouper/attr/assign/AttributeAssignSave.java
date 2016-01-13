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
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefFinder;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.attr.value.AttributeAssignValue;
import edu.internet2.middleware.grouper.exception.AttributeDefNameNotFoundException;
import edu.internet2.middleware.grouper.exception.GroupAddException;
import edu.internet2.middleware.grouper.exception.GroupModifyAlreadyExistsException;
import edu.internet2.middleware.grouper.exception.GroupModifyException;
import edu.internet2.middleware.grouper.exception.GroupNotFoundException;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.exception.StemAddException;
import edu.internet2.middleware.grouper.exception.StemNotFoundException;
import edu.internet2.middleware.grouper.group.TypeOfGroup;
import edu.internet2.middleware.grouper.hibernate.GrouperTransaction;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionHandler;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.misc.SaveMode;
import edu.internet2.middleware.grouper.misc.SaveResultType;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
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
   * if doing an import, these are id's which should not be used.  The current assignment will be added to this list
   */
  private Set<String> attributeAssignIdsToNotUse = null;
  
  /**
   * 
   */
  private AttributeAssign attributeAssignmentSimilarToThisRequest = null;
  
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
   * add a value to assign to this assignment
   * @param attributeAssignValue
   * @return this for chaining
   */
  public AttributeAssignSave addAttributeAssignValue(AttributeAssignValue attributeAssignValue) {
    if (this.attributeAssignValues == null) {
      this.attributeAssignValues = new HashSet<AttributeAssignValue>();
    }
    this.attributeAssignValues.add(attributeAssignValue);
    return this;
  }
  
  /**
   * if including assignments on this assignment, put them here
   */
  private Set<AttributeAssignSave> attributeAssignsOnThisAssignment = null;
  
  /**
   * if including assignments on this assignment, put them here
   * @param theAttributeAssignSave
   * @return this for chaining
   */
  public AttributeAssignSave addAttributeAssignOnThisAssignment(AttributeAssignSave theAttributeAssignSave) {
    if (this.attributeAssignsOnThisAssignment == null) {
      this.attributeAssignsOnThisAssignment = new LinkedHashSet<AttributeAssignSave>();
    }
    this.attributeAssignsOnThisAssignment.add(theAttributeAssignSave);
    return this;
  }
  
  /**
   * <pre>
   * create or update a group.  Note this will not rename a group at this time (might in future)
   * 
   * This is a static method since setters to Group objects persist to the DB
   * 
   * Steps:
   * 
   * 1. Find the group by groupNameToEdit
   * 2. Internally set all the fields of the stem (no need to reset if already the same)
   * 3. Store the group (insert or update) if needed
   * 4. Return the group object
   * 
   * This runs in a tx so that if part of it fails the whole thing fails, and potentially the outer
   * transaction too
   * </pre>
   * @return the group
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

    try {
      //do this in a transaction
      AttributeAssign attributeAssign = (AttributeAssign)GrouperTransaction.callbackGrouperTransaction(new GrouperTransactionHandler() {

        @SuppressWarnings("cast")
        @Override
        public Object callback(GrouperTransaction grouperTransaction)
            throws GrouperDAOException {
          
          grouperTransaction.setCachingEnabled(false);
          
          return (AttributeAssign)GrouperSession.callbackGrouperSession(AttributeAssignSave.this.grouperSession, new GrouperSessionHandler() {

              @Override
              public Object callback(GrouperSession theGrouperSession)
                  throws GrouperSessionException {
                                
                AttributeAssign theAttributeAssign = null;

                //see if update
                boolean isUpdate = AttributeAssignSave.this.saveMode == SaveMode.UPDATE;

//                try {
//                  theGroup = GroupFinder.findByName(theGrouperSession, AttributeAssignSave.this.groupNameToEdit, true);
//                  
//                  //while we are here, make sure uuid's match if passed in
//                  if (!StringUtils.isBlank(AttributeAssignSave.this.uuid) && !StringUtils.equals(AttributeAssignSave.this.uuid, theGroup.getUuid())) {
//                    throw new RuntimeException("UUID group changes are not supported: new: " + AttributeAssignSave.this.uuid + ", old: " 
//                        + theGroup.getUuid() + ", " + groupNameForError);
//                  }
//                  
//                } catch (GroupNotFoundException gnfe) {
//                  if (SAVE_MODE.equals(SaveMode.INSERT_OR_UPDATE) || SAVE_MODE.equals(SaveMode.INSERT)) {
//                    isUpdate = false;
//                  } else {
//                      throw new GrouperSessionException(gnfe);
//                  }
//                }
//                //default
//                AttributeAssignSave.this.saveResultType = SaveResultType.NO_CHANGE;
//                boolean needsSave = false;
//                //if inserting
//                if (!isUpdate) {
//                  AttributeAssignSave.this.saveResultType = SaveResultType.INSERT;
//                  if (AttributeAssignSave.this.typeOfGroup == null || AttributeAssignSave.this.typeOfGroup == TypeOfGroup.group) {
//                    theGroup = parentStem.internal_addChildGroup(extensionNew, theDisplayExtension, AttributeAssignSave.this.uuid);
//                  } else if (AttributeAssignSave.this.typeOfGroup == TypeOfGroup.role) {
//                    theGroup = (Group)parentStem.internal_addChildRole(extensionNew, theDisplayExtension, AttributeAssignSave.this.uuid);
//                  } else if (AttributeAssignSave.this.typeOfGroup == TypeOfGroup.entity) {
//                    theGroup = (Group)parentStem.internal_addChildEntity(extensionNew, theDisplayExtension, AttributeAssignSave.this.uuid);
//                  } else {
//                    throw new RuntimeException("Not expecting type of group: " + AttributeAssignSave.this.typeOfGroup);
//                  }
//                  
//                } else {
//                  //check if different so it doesnt make unneeded queries
//                  if (!StringUtils.equals(theGroup.getExtension(), extensionNew)) {
//                      
//                      //lets just confirm that one doesnt exist
//                      String newName = GrouperUtil.parentStemNameFromName(theGroup.getName()) + ":" + extensionNew;
//                      
//                      Group existingGroup = GroupFinder.findByName(theGrouperSession.internal_getRootSession(), newName, false);
//                      
//                      if (existingGroup != null && !StringUtils.equals(theGroup.getUuid(), existingGroup.getUuid())) {
//                        throw new GroupModifyAlreadyExistsException("Group already exists: " + newName);
//                      }
//                      
//                      theGroup.setExtension(extensionNew);
//                    AttributeAssignSave.this.saveResultType = SaveResultType.UPDATE;
//                    needsSave = true;
//                  }
//                  if (!StringUtils.equals(theGroup.getDisplayExtension(), theDisplayExtension)) {
//                    AttributeAssignSave.this.saveResultType = SaveResultType.UPDATE;
//                    theGroup.setDisplayExtension(theDisplayExtension);
//                    needsSave = true;
//                  }
//                }
//
//                if (AttributeAssignSave.this.idIndex != null) {
//                  if (AttributeAssignSave.this.saveResultType == SaveResultType.INSERT) {
//                    
//                    if (theGroup.assignIdIndex(AttributeAssignSave.this.idIndex)) {
//                      needsSave = true;
//                    }
//                    
//                  } else {
//                    //maybe they are equal...
//                    throw new RuntimeException("Cannot update idIndex for an already created group: " + AttributeAssignSave.this.idIndex + ", " + theGroup.getName());
//                  }
//                }
//
//                //now compare and put all attributes (then store if needed)
//                //null throws exception? hmmm.  remove attribute if blank
//                if (!StringUtils.equals(StringUtils.defaultString(theGroup.getDescription()), 
//                    StringUtils.defaultString(StringUtils.trim(AttributeAssignSave.this.description)))) {
//                  needsSave = true;
//                  if (AttributeAssignSave.this.saveResultType == SaveResultType.NO_CHANGE) {
//                    AttributeAssignSave.this.saveResultType = SaveResultType.UPDATE;
//                  }
//                  theGroup.setDescription(AttributeAssignSave.this.description);
//                }
//
//                //compare type of group
//                if (AttributeAssignSave.this.typeOfGroup != null && AttributeAssignSave.this.typeOfGroup != theGroup.getTypeOfGroup()) {
//                  needsSave = true;
//                  if (AttributeAssignSave.this.saveResultType == SaveResultType.NO_CHANGE) {
//                    AttributeAssignSave.this.saveResultType = SaveResultType.UPDATE;
//                  }
//                  theGroup.setTypeOfGroup(AttributeAssignSave.this.typeOfGroup);
//                }
//
//                //only store once
//                if (needsSave) {
//                  theGroup.store();
//                }
//
//                boolean changedPrivs = false;
//                                                
//                boolean readDefaultChecked = theGroup.hasRead(SubjectFinder.findAllSubject());
//      
//                boolean viewDefaultChecked = theGroup.hasView(SubjectFinder.findAllSubject());
//      
//                boolean optinDefaultChecked = theGroup.hasOptin(SubjectFinder.findAllSubject());
//      
//                boolean optoutDefaultChecked = theGroup.hasOptout(SubjectFinder.findAllSubject());
//      
//                boolean attrReadDefaultChecked = theGroup.hasGroupAttrRead(SubjectFinder.findAllSubject());
//                            
//                if (AttributeAssignSave.this.privAllView != null && AttributeAssignSave.this.privAllView != viewDefaultChecked) {
//                  if (AttributeAssignSave.this.privAllView) {
//                    changedPrivs = changedPrivs | theGroup.grantPriv(SubjectFinder.findAllSubject(), AccessPrivilege.VIEW, false);
//                  } else {
//                    changedPrivs = changedPrivs | theGroup.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.VIEW, false);
//                  }
//                }
//      
//                if (AttributeAssignSave.this.privAllRead != null && AttributeAssignSave.this.privAllRead != readDefaultChecked) {
//                  if (AttributeAssignSave.this.privAllRead) {
//                    changedPrivs = changedPrivs | theGroup.grantPriv(SubjectFinder.findAllSubject(), AccessPrivilege.READ, false);
//                  } else {
//                    changedPrivs = changedPrivs | theGroup.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.READ, false);
//                  }
//                }
//                if (AttributeAssignSave.this.privAllOptin != null && AttributeAssignSave.this.privAllOptin != optinDefaultChecked) {
//                  if (AttributeAssignSave.this.privAllOptin) {
//                    changedPrivs = changedPrivs | theGroup.grantPriv(SubjectFinder.findAllSubject(), AccessPrivilege.OPTIN, false);
//                  } else {
//                    changedPrivs = changedPrivs | theGroup.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.OPTIN, false);
//                  }
//                }
//                if (AttributeAssignSave.this.privAllOptout != null && AttributeAssignSave.this.privAllOptout != optoutDefaultChecked) {
//                  if (AttributeAssignSave.this.privAllOptout) {
//                    changedPrivs = changedPrivs | theGroup.grantPriv(SubjectFinder.findAllSubject(), AccessPrivilege.OPTOUT, false);
//                  } else {
//                    changedPrivs = changedPrivs | theGroup.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.OPTOUT, false);
//                  }
//                }
//                if (AttributeAssignSave.this.privAllAttrRead != null && AttributeAssignSave.this.privAllAttrRead != attrReadDefaultChecked) {
//                  if (AttributeAssignSave.this.privAllAttrRead) {
//                    changedPrivs = changedPrivs | theGroup.grantPriv(SubjectFinder.findAllSubject(), AccessPrivilege.GROUP_ATTR_READ, false);
//                  } else {
//                    changedPrivs = changedPrivs | theGroup.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.GROUP_ATTR_READ, false);
//                  }
//                }
//                
//                if (changedPrivs) {
//                  if (AttributeAssignSave.this.saveResultType == SaveResultType.NO_CHANGE) {
//                    AttributeAssignSave.this.saveResultType = SaveResultType.UPDATE;
//                  }
//                }
//                
//			          return theGroup;
                return null;
              }
          });
        }
      });
      //return ownerGroup;
      return null;
    } catch (RuntimeException re) {
      
      GrouperUtil.injectInException(re, "Problem saving group: " + /* this.name + */ ", thread: " + Integer.toHexString(Thread.currentThread().hashCode()));
      
      Throwable throwable = re.getCause();
      if (throwable instanceof StemNotFoundException) {
        throw (StemNotFoundException)throwable;
      }
      if (throwable instanceof InsufficientPrivilegeException) {
        throw (InsufficientPrivilegeException)throwable;
      }
      if (throwable instanceof StemAddException) {
        throw (StemAddException)throwable;
      }
      if (throwable instanceof GroupModifyException) {
        throw (GroupModifyException)throwable;
      }
      if (throwable instanceof GroupNotFoundException) {
        throw (GroupNotFoundException)throwable;
      }
      if (throwable instanceof GroupAddException) {
        throw (GroupAddException)throwable;
      }
      //must just be runtime
      throw re;
    }

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

    if (!StringUtils.isBlank(this.ownerMemberId)) {
      this.ownerMember = MemberFinder.findByUuid(this.grouperSession, this.ownerMemberId, true);
    }
    if (!StringUtils.isBlank(this.ownerMemberSourceId) && this.ownerMember != null) {
      if (!StringUtils.equals(ownerMemberSubject.getSourceId(), this.ownerMemberSourceId)) {
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
      ownerMemberSubject = this.ownerMember.getSubject();
    }
    
    if (this.ownerAttributeAssignId != null) {
      ownerAttributeAssign = GrouperDAOFactory.getFactory().getAttributeAssign().findById(this.ownerAttributeAssignId, true);
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

    // stem
    if (ownerStem != null) {
      if (this.attributeAssignType == null) {
        this.attributeAssignType = AttributeAssignType.stem;
      }
      if (this.attributeAssignType != AttributeAssignType.stem) {
        throw new RuntimeException("attributeAssignType needs to be stem if passing in a stem: " + this.attributeAssignType);
      }
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
      if (this.ownerAttributeAssign != null || this.ownerAttributeDef != null || this.ownerMember != null
          || this.ownerMembership != null || this.ownerStem != null) {
        throw new RuntimeException("Passing in too many types of owners: attributeAssign: "
            + this.ownerAttributeAssign + ", attributeDef: " + this.ownerAttributeDef + ", ownerGroup: " + this.ownerGroup
            + ", ownerMember: " + this.ownerMember + ", ownerMembership: " + this.ownerMembership + ", ownerStem: " + this.ownerStem);
      }
    }

    // eff membership
    if (ownerMember != null && ownerGroup != null) {
      if (this.attributeAssignType == null) {
        this.attributeAssignType = AttributeAssignType.any_mem;
      }
      if (this.attributeAssignType != AttributeAssignType.any_mem) {
        throw new RuntimeException("attributeAssignType needs to be any_mem if passing in a member and group: " + this.attributeAssignType);
      }
      if (this.ownerAttributeAssign != null || this.ownerAttributeDef != null
          || this.ownerMembership != null || this.ownerStem != null) {
        throw new RuntimeException("Passing in too many types of owners: attributeAssign: "
            + this.ownerAttributeAssign + ", attributeDef: " + this.ownerAttributeDef + ", ownerGroup: " + this.ownerGroup
            + ", ownerMember: " + this.ownerMember + ", ownerMembership: " + this.ownerMembership + ", ownerStem: " + this.ownerStem);
      }
    }

    //imm membership
    if (ownerMembership != null) {
      if (this.attributeAssignType == null) {
        this.attributeAssignType = AttributeAssignType.imm_mem;
      }
      if (this.attributeAssignType != AttributeAssignType.imm_mem) {
        throw new RuntimeException("attributeAssignType needs to be imm_mem if passing in a membership: " + this.attributeAssignType);
      }
      if (this.ownerAttributeAssign != null || this.ownerAttributeDef != null || this.ownerGroup != null || this.ownerMember != null
          || this.ownerStem != null) {
        throw new RuntimeException("Passing in too many types of owners: attributeAssign: "
            + this.ownerAttributeAssign + ", attributeDef: " + this.ownerAttributeDef + ", ownerGroup: " + this.ownerGroup
            + ", ownerMember: " + this.ownerMember + ", ownerMembership: " + this.ownerMembership + ", ownerStem: " + this.ownerStem);
      }
    }
    
    // attributeAssign
    if (ownerAttributeAssign != null) {
      
      if (this.attributeAssignType == null) {
        this.attributeAssignType = ownerAttributeAssign.getAttributeAssignType().getAssignmentOnAssignmentType();
      }
      if (this.attributeAssignType != ownerAttributeAssign.getAttributeAssignType().getAssignmentOnAssignmentType()) {
        throw new RuntimeException("attributeAssignType needs to be " + ownerAttributeAssign.getAttributeAssignType().getAssignmentOnAssignmentType() 
            + " if passing in an assignment of: " + ownerAttributeAssign.getAttributeAssignType().name() + ", " + this.attributeAssignType);
      }
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
    Set<AttributeAssign> attributeAssigns = null;
    
    if (this.attributeAssignType == null) {
      throw new RuntimeException("This shouldnt happen, attributeAssignType is null");
    }
    
    //if this is an assignment on assignment, then it is just the owner (if exists... I guess it should)
    if (this.attributeAssignType.isAssignmentOnAssignment()) {
      this.attributeAssignmentSimilarToThisRequest = this.ownerAttributeAssign;
      return;
    }
    
    switch(this.attributeAssignType) {
      case group:
        attributeAssigns = GrouperDAOFactory.getFactory().getAttributeAssign().findGroupAttributeAssignments(null, null, GrouperUtil.toSet(this.attributeDefName.getId()), 
            GrouperUtil.toSet(this.ownerGroup.getId()), GrouperUtil.toSet(this.action), null, true);
        break;
      case stem:
        attributeAssigns = GrouperDAOFactory.getFactory().getAttributeAssign().findStemAttributeAssignments(null, null, GrouperUtil.toSet(this.attributeDefName.getId()), 
            GrouperUtil.toSet(this.ownerStem.getId()), GrouperUtil.toSet(this.action), null, true);
        break;
      case attr_def:
        attributeAssigns = GrouperDAOFactory.getFactory().getAttributeAssign().findAttributeDefAttributeAssignments(null, null, GrouperUtil.toSet(this.attributeDefName.getId()), 
            GrouperUtil.toSet(this.ownerAttributeDef.getId()), GrouperUtil.toSet(this.action), null, true);
        break;
      case member:
        attributeAssigns = GrouperDAOFactory.getFactory().getAttributeAssign().findMemberAttributeAssignments(null, null, GrouperUtil.toSet(this.attributeDefName.getId()), 
            GrouperUtil.toSet(this.ownerStem.getId()), GrouperUtil.toSet(this.action), null, true);
        break;
      case imm_mem:
        attributeAssigns = GrouperDAOFactory.getFactory().getAttributeAssign().findMembershipAttributeAssignments(null, null, GrouperUtil.toSet(this.attributeDefName.getId()), 
            GrouperUtil.toSet(this.ownerMembership.getImmediateMembershipId()), GrouperUtil.toSet(this.action), null, true);
        break;
      case any_mem:
        attributeAssigns = GrouperDAOFactory.getFactory().getAttributeAssign().findAnyMembershipAttributeAssignments(null, null, GrouperUtil.toSet(this.attributeDefName.getId()), 
            GrouperUtil.toSet(new MultiKey(this.ownerGroup.getId(), this.ownerMember.getId())), GrouperUtil.toSet(this.action), null, true);
        break;
    }
    
    if (GrouperUtil.length(attributeAssigns) == 0) {
      return;
    }
    
    //remove any already existing
    if (GrouperUtil.length(this.attributeAssignIdsToNotUse) > 0) {
      attributeAssigns = new HashSet<AttributeAssign>(attributeAssigns);
      Iterator<AttributeAssign> iterator = attributeAssigns.iterator();
      while (iterator.hasNext()) {
        AttributeAssign attributeAssignCurrent = iterator.next();
        if (this.attributeAssignIdsToNotUse.contains(attributeAssignCurrent.getId()) 
            || this.attributeAssignIdsToNotUse.contains(attributeAssignCurrent.getOwnerAttributeAssignId())) {
          iterator.remove();
        }
      }
    }

    if (GrouperUtil.length(attributeAssigns) == 0) {
      return;
    }

    //pick the best one.  is there only one?
    Set<AttributeAssign> immediateAssignments = new HashSet<AttributeAssign>();
    
    for (AttributeAssign attributeAssignCurrent : attributeAssigns) {

      //if this is one of the ones to not use
      if (this.attributeAssignIdsToNotUse != null && this.attributeAssignIdsToNotUse.contains(attributeAssignCurrent.getId())) {
        continue;
      }
      
      if (!attributeAssignCurrent.getAttributeAssignType().isAssignmentOnAssignment()) {
        immediateAssignments.add(attributeAssignCurrent);
      }
      
    }

    if (GrouperUtil.length(immediateAssignments) == 0) {
      return;
    }

    if (GrouperUtil.length(immediateAssignments) == 1) {
      this.attributeAssignmentSimilarToThisRequest = immediateAssignments.iterator().next();
      return;
    }

    //convert to attribute assign ids
    Set<String> attributeAssignIds = new HashSet<String>();
    for (AttributeAssign current : immediateAssignments) {
      attributeAssignIds.add(current.getId());
    }
    
    //look at attribute assignments and values
    Set<AttributeAssignValue> attributeAssignValuesOnAssigns = GrouperDAOFactory.getFactory()
        .getAttributeAssignValue().findValuesOnAssignments(attributeAssignIds, null, null, null);

    Set<AttributeAssignValue> attributeAssignValues = GrouperDAOFactory.getFactory()
        .getAttributeAssignValue().findByAttributeAssignIds(attributeAssignIds);

    Set<AttributeAssignValue> allValues = new HashSet<AttributeAssignValue>();
    allValues.addAll(attributeAssignValuesOnAssigns);
    allValues.addAll(attributeAssignValues);
    
    Map<String, List<Object>> attributeAssignIdToValueSet = new HashMap<String, List<Object>>();
        
    for (AttributeAssignValue currentValue : allValues) {

      //lazy load the values set
      List<Object> values = attributeAssignIdToValueSet.get(currentValue.getAttributeAssignId());
      if (values == null) {
        values = new ArrayList<Object>();
        attributeAssignIdToValueSet.put(currentValue.getAttributeAssignId(), values);
      }

      //get the value
      Object value = currentValue.getValue();

      //add it to the set
      values.add(value);
    }

    //keep track of assignments on each assignment
    Map<String, Set<AttributeAssign>> attributeAssignIdToAssignmentsOfAssignments = new HashMap<String, Set<AttributeAssign>>();
    
    for (AttributeAssign attributeAssignOnAssign : GrouperUtil.nonNull(attributeAssigns)) {
      String ownerAttributeAssignId = attributeAssignOnAssign.getOwnerAttributeAssignId();
      
      if (!StringUtils.isBlank(ownerAttributeAssignId)) {
        Set<AttributeAssign> assignsOnAssign = attributeAssignIdToAssignmentsOfAssignments.get(ownerAttributeAssignId);
        if (assignsOnAssign == null) {
          assignsOnAssign = new HashSet<AttributeAssign>();
          attributeAssignIdToAssignmentsOfAssignments.put(ownerAttributeAssignId, assignsOnAssign);
        }
        
        assignsOnAssign.add(attributeAssignOnAssign);
      }
    }
    
    int bestScore = 0;
    
    for (AttributeAssign currentImmediateAssignment : immediateAssignments) {
      
      //lets look at values
      List<Object> valuesOnImmediateAssignment = attributeAssignIdToValueSet.get(currentImmediateAssignment.getId());
      
      if (GrouperUtil.length(this.attributeAssignValues) == 0 && GrouperUtil.length(valuesOnImmediateAssignment) == 0) {

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
   * @return the score
   */
  static int computerScoreForExistingAssignment(Set<AttributeAssignValue> expectedValues, 
      List<Object> existingValues, Set<AttributeAssignSave> expectedAssignmentsOnAssignment, 
      Set<AttributeAssign> existingAssignmentsOnAssignment, Map<String, List<Object>> valuesOnAttributeAssignments) {

    int score = 0;

    int existingValueCountThatMatches = 0;

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

    //copy the set so we can remove things
    existingAssignmentsOnAssignment = existingAssignmentsOnAssignment == null ? 
        new HashSet<AttributeAssign>() : new HashSet<AttributeAssign>(existingAssignmentsOnAssignment);

    //now lets look at assignments on assignments
    for (AttributeAssignSave expectedAssignmentOnAssignment : expectedAssignmentsOnAssignment) {
      
      //find in existing values
      Iterator<AttributeAssign> existingAttributeAssignIterator = existingAssignmentsOnAssignment.iterator();

//      while (.hasNext()) {
//        Object existingValue = existingValuesIterator.next();
//        if (GrouperUtil.equals(expectedValueObject, existingValue)) {
//          existingValuesIterator.remove();
//          score++;
//          continue;
//        }
//      }
      
      
    }
    
    return score;
  }
  
}
