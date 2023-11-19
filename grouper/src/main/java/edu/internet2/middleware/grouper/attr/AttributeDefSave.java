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
 * $Id: AttributeDefSave.java,v 1.10 2009-11-17 02:52:29 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.attr;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefFinder;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.exception.StemAddException;
import edu.internet2.middleware.grouper.exception.StemNotFoundException;
import edu.internet2.middleware.grouper.hibernate.GrouperTransaction;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionHandler;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.misc.SaveMode;
import edu.internet2.middleware.grouper.misc.SaveResultType;
import edu.internet2.middleware.grouper.privs.AttributeDefPrivilege;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * <p>Use this class to insert or update an attribute definition</p>
 * <p>Sample call
 * 
 * <blockquote>
 * <pre>
 * AttributeDefSave attributeDefSave = new AttributeDefSave(grouperSession)
    .assignName("top:b").assignDescription("whatever").assignValueType(AttributeDefValueType.string).assignMultiValued(true);
 * AttributeDef attributeDef = attributeDefSave.save();
 * System.out.println(attributeDefSave.getSaveResultType()); // INSERT, NO_CHANGE, or UPDATE
 * </pre>
 * </blockquote>
 * 
 * </p>
 * 
 * <p> Sample call to update only one attribute
 * <blockquote>
 * <pre>
 * new AttributeDefSave(grouperSession)
 *      .assignName("top:b").assignMultiValued(true).assignReplaceAllSettings(false).save();
 * </pre>
 * </blockquote>
 * </p>
 *
 */
public class AttributeDefSave {
  
  /** id index */
  private Long idIndex;
  
  /**
   * assign id_index
   * @param theIdIndex
   * @return this for chaining
   */
  public AttributeDefSave assignIdIndex(Long theIdIndex) {
    this.idIndex = theIdIndex;
    return this;
  }


  /**
   * create a new attribute def save
   * @param theGrouperSession
   */
  public AttributeDefSave(GrouperSession theGrouperSession) {
    this.grouperSession = theGrouperSession;
  }
  
  /**
   * create a new attribute def save
   * @param theGrouperSession
   */
  public AttributeDefSave() {
    this.grouperSession = GrouperSession.staticGrouperSession();
  }
  
  /** grouper session is required */
  private GrouperSession grouperSession;

  /** if updating an attribute def, this is the attribute def name */
  private String attributeDefNameToEdit;
  
  /**
   * attributeDef name to edit
   * @param theAttributeDefNameToEdit
   * @return the attributeDef name to edit
   */
  public AttributeDefSave assignAttributeDefNameToEdit(String theAttributeDefNameToEdit) {
    this.attributeDefNameToEdit = theAttributeDefNameToEdit;
    return this;
  }
  
  /** id */
  private String id;
  
  /** 
   * uuid
   * @param theUuid
   * @return uuid
   */
  public AttributeDefSave assignId(String theId) {
    this.id = theId;
    return this;
  }
  
  /** name to change to */
  private String name;
  
  /**
   * name
   * @param name1
   * @return name
   */
  public AttributeDefSave assignName(String name1) {
    this.name = name1;
    return this;
  }
  
  /** description */
  private String description;

  private boolean descriptionAssigned;
  
  /**
   * assign description
   * @param theDescription
   * @return this for chaining
   */
  public AttributeDefSave assignDescription(String theDescription) {
    this.description = theDescription;
    this.descriptionAssigned = true;
    return this;
  }

  /** save mode */
  private SaveMode saveMode;

  /**
   * assign save mode
   * @param theSaveMode
   * @return this for chaining
   */
  public AttributeDefSave assignSaveMode(SaveMode theSaveMode) {
    this.saveMode = theSaveMode;
    return this;
  }

  /** if create parent stems if not exist */
  private boolean createParentStemsIfNotExist;

  /**
   * assign create parents if not exist
   * @param theCreateParentStemsIfNotExist
   * @return this for chaining
   */
  public AttributeDefSave assignCreateParentStemsIfNotExist(boolean theCreateParentStemsIfNotExist) {
    this.createParentStemsIfNotExist = theCreateParentStemsIfNotExist;
    return this;
  }

  /** save type after the save */
  private SaveResultType saveResultType = null;

  /** if can assign to attribute def */
  private boolean assignToAttributeDef;

  private boolean assignToAttributeDefAssigned;

  /**
   * if assign
   * @param theBoolean
   * @return this for chaining
   */
  public AttributeDefSave assignToAttributeDef(boolean theBoolean) {
    this.assignToAttributeDef = theBoolean;
    assignToAttributeDefAssigned = true;
    return this;
  }
  
  /** if can assign to assignment of attribute def */
  private boolean assignToAttributeDefAssn;

  private boolean assignToAttributeDefAssnAssigned;

  /**
   * if assign
   * @param theBoolean
   * @return this for chaining
   */
  public AttributeDefSave assignToAttributeDefAssn(boolean theBoolean) {
    this.assignToAttributeDefAssn = theBoolean;
    this.assignToAttributeDefAssnAssigned = true;
    return this;
  }

  /** if can assign to effective membership */
  private boolean assignToEffMembership;

  private boolean assignToEffMembershipAssigned;

  /**
   * if assign
   * @param theBoolean
   * @return this for chaining
   */
  public AttributeDefSave assignToEffMembership(boolean theBoolean) {
    this.assignToEffMembership = theBoolean;
    this.assignToEffMembershipAssigned = true;
    return this;
  }

  /** if can assign to assignment of effective membership */
  private boolean assignToEffMembershipAssn;

  private boolean assignToEffMembershipAssnAssigned;

  /**
   * if assign
   * @param theBoolean
   * @return this for chaining
   */
  public AttributeDefSave assignToEffMembershipAssn(boolean theBoolean) {
    this.assignToEffMembershipAssn = theBoolean;
    this.assignToEffMembershipAssnAssigned = true;
    return this;
  }

  /** if can assign to group/role */
  private boolean assignToGroup;

  private boolean assignToGroupAssigned;

  /**
   * if assign
   * @param theBoolean
   * @return this for chaining
   */
  public AttributeDefSave assignToGroup(boolean theBoolean) {
    this.assignToGroup = theBoolean;
    this.assignToGroupAssigned = true;
    return this;
  }

  /** if can assign to assignment of group/role */
  private boolean assignToGroupAssn;

  private boolean assignToGroupAssnAssigned;

  /**
   * if assign
   * @param theBoolean
   * @return this for chaining
   */
  public AttributeDefSave assignToGroupAssn(boolean theBoolean) {
    this.assignToGroupAssn = theBoolean;
    this.assignToGroupAssnAssigned = true;
    return this;
  }

  /** if can assign to immediate membership */
  private boolean assignToImmMembership;

  private boolean assignToImmMembershipAssigned;

  /**
   * if assign
   * @param theBoolean
   * @return this for chaining
   */
  public AttributeDefSave assignToImmMembership(boolean theBoolean) {
    this.assignToImmMembership = theBoolean;
    this.assignToImmMembershipAssigned = true;
    return this;
  }

  /** if can assign to assignment of immediate membership */
  private boolean assignToImmMembershipAssn;

  private boolean assignToImmMembershipAssnAssigned;

  /**
   * if assign
   * @param theBoolean
   * @return this for chaining
   */
  public AttributeDefSave assignToImmMembershipAssn(boolean theBoolean) {
    this.assignToImmMembershipAssn = theBoolean;
    this.assignToImmMembershipAssnAssigned = true;
    return this;
  }

  /** if can assign to member */
  private boolean assignToMember;

  private boolean assignToMemberAssigned;

  /**
   * if assign
   * @param theBoolean
   * @return this for chaining
   */
  public AttributeDefSave assignToMember(boolean theBoolean) {
    this.assignToMember = theBoolean;
    this.assignToMemberAssigned = true;
    return this;
  }

  /** if can assign to assignment of member */
  private boolean assignToMemberAssn;

  private boolean assignToMemberAssnAssigned;

  /**
   * if assign
   * @param theBoolean
   * @return this for chaining
   */
  public AttributeDefSave assignToMemberAssn(boolean theBoolean) {
    this.assignToMemberAssn = theBoolean;
    this.assignToMemberAssnAssigned = true;
    return this;
  }

  /** if can assign to stem */
  private boolean assignToStem;

  private boolean assignToStemAssigned;

  /**
   * if assign
   * @param theBoolean
   * @return this for chaining
   */
  public AttributeDefSave assignToStem(boolean theBoolean) {
    this.assignToStem = theBoolean;
    this.assignToStemAssigned = true;
    return this;
  }

  /** if can assign to assignment of stem */
  private boolean assignToStemAssn;

  private boolean assignToStemAssnAssigned;

  /**
   * if assign
   * @param theBoolean
   * @return this for chaining
   */
  public AttributeDefSave assignToStemAssn(boolean theBoolean) {
    this.assignToStemAssn = theBoolean;
    this.assignToStemAssnAssigned = true;
    return this;
  }

  /**
   * if the attribute def is public, otherwise you just see it in this stem and substem
   */
  private boolean attributeDefPublic = false;

  private boolean attributeDefPublicAssigned;

  /**
   * if assign
   * @param theBoolean
   * @return this for chaining
   */
  public AttributeDefSave assignAttributeDefPublic(boolean theBoolean) {
    this.attributeDefPublic = theBoolean;
    this.attributeDefPublicAssigned = true;
    return this;
  }

  /**
   * type of this attribute (e.g. attribute or privilege)
   */
  private AttributeDefType attributeDefType;

  /**
   * if assign
   * @param theBoolean
   * @return this for chaining
   */
  public AttributeDefSave assignAttributeDefType(AttributeDefType theAttributeDefType) {
    this.attributeDefType = theAttributeDefType;
    return this;
  }

  /**
   * if this attribute can be assigned to the same action to the same object more than once
   */
  private boolean multiAssignable;

  private boolean multiAssignableAssigned;

  /**
   * if assign
   * @param theBoolean
   * @return this for chaining
   */
  public AttributeDefSave assignMultiAssignable(boolean theBoolean) {
    this.multiAssignable = theBoolean;
    this.multiAssignableAssigned = true;
    return this;
  }

  /**
   * if more than one value (same type) can be assigned to the attribute assignment
   */
  private boolean multiValued;

  private boolean multiValuedAssigned;

  /**
   * if assign
   * @param theBoolean
   * @return self for chaining
   */
  public AttributeDefSave assignMultiValued(boolean theBoolean) {
    this.multiValued = theBoolean;
    this.multiValuedAssigned = true;
    return this;
  }

  /**
   * type of the value,  int, double, string, marker
   */
  private AttributeDefValueType valueType = AttributeDefValueType.marker;

  private boolean valueTypeAssigned;

  /**
   * if the priv admin should be different from the defaults
   */
  private Boolean privAllAdmin;

  /**
   * if the priv attr update should be different from the defaults
   */
  private Boolean privAllAttrRead;

  /**
   * if the priv attr update should be different from the defaults
   */
  private Boolean privAllAttrUpdate;

  /**
   * if the priv optin should be different from the defaults
   */
  private Boolean privAllOptin;

  /**
   * if the priv optout should be different from the defaults
   */
  private Boolean privAllOptout;

  /**
   * if the priv read should be different from the defaults
   */
  private Boolean privAllRead;

  /**
   * if the priv update should be different from the defaults
   */
  private Boolean privAllUpdate;

  /**
   * if the priv view should be different from the defaults
   */
  private Boolean privAllView;
  
  /**
   * replace all existing settings. defaults to true.
   */
  private boolean replaceAllSettings = true;
  
  /**
   * replace all existing settings. defaults to true.
   * @return this for chaining
   */
  public AttributeDefSave assignReplaceAllSettings(boolean theReplaceAllSettings) {
    this.replaceAllSettings = theReplaceAllSettings;
    return this;
  }
  
  /**
   * if assign
   * @param theBoolean
   * @return this for chaining
   */
  public AttributeDefSave assignValueType(AttributeDefValueType attributeDefValueType) {
    this.valueType = attributeDefValueType;
    this.valueTypeAssigned = true;
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
   * <pre>
   * create or update an attribute def.  Note this will not rename an attribute def at this time (might in future)
   * 
   * Steps:
   * 
   * 1. Find the attributeDef by attributeDefNameToEdit
   * 2. Internally set all the fields of the attributeDef (no need to reset if already the same)
   * 3. Store the attributeDef (insert or update) if needed
   * 4. Return the attributeDef object
   * 
   * This runs in a tx so that if part of it fails the whole thing fails, and potentially the outer
   * transaction too
   * </pre>
   * @return the attributeDef
   * @throws StemNotFoundException
   * @throws InsufficientPrivilegeException
   * @throws StemAddException
   */
  public AttributeDef save() 
        throws StemNotFoundException, InsufficientPrivilegeException, StemAddException {
    
    //get from uuid since could be a rename
    if (StringUtils.isBlank(this.attributeDefNameToEdit) && !StringUtils.isBlank(this.id)) {
      AttributeDef attributeDef = AttributeDefFinder.findById(this.id, false, new QueryOptions().secondLevelCache(false));
      if (attributeDef != null) {
        this.attributeDefNameToEdit = attributeDef.getName();
      }
    }
    
    //help with incomplete entries
    if (StringUtils.isBlank(this.name)) {
      this.name = this.attributeDefNameToEdit;
    }
    
    if (StringUtils.isBlank(this.attributeDefNameToEdit)) {
      this.attributeDefNameToEdit = this.name;
    }
    
    
    //validate
    //get the attribute def name
    if (!StringUtils.contains(this.name, ":")) {
      throw new RuntimeException("AttributeDef name must exist and must contain at least one stem name (separated by colons): '" + name + "'" );
    }

    //default to insert or update
    saveMode = (SaveMode)ObjectUtils.defaultIfNull(saveMode, SaveMode.INSERT_OR_UPDATE);
    final SaveMode SAVE_MODE = saveMode;

    try {
      //do this in a transaction
      AttributeDef attributeDef = (AttributeDef)GrouperTransaction.callbackGrouperTransaction(new GrouperTransactionHandler() {
  
        public Object callback(GrouperTransaction grouperTransaction)
            throws GrouperDAOException {
          
          grouperTransaction.setCachingEnabled(false);
          
          return (AttributeDef)GrouperSession.callbackGrouperSession(AttributeDefSave.this.grouperSession, new GrouperSessionHandler() {

              public Object callback(GrouperSession grouperSession)
                  throws GrouperSessionException {
              
                String attributeDefNameForError = GrouperUtil.defaultIfBlank(AttributeDefSave.this.attributeDefNameToEdit, AttributeDefSave.this.name);
                
                int lastColonIndex = AttributeDefSave.this.name.lastIndexOf(':');
                boolean topLevelAttributeDef = lastColonIndex < 0;
        
                //empty is root stem
                String parentStemNameNew = GrouperUtil.parentStemNameFromName(AttributeDefSave.this.name);
                
                //note, this might be blank
                String extensionNew = GrouperUtil.extensionFromName(AttributeDefSave.this.name);
                                
                //lets find the stem
                Stem parentStem = null;
                
                try {
                  parentStem = topLevelAttributeDef ? StemFinder.findRootStem(grouperSession) 
                      : StemFinder.findByName(grouperSession, parentStemNameNew, true);
                } catch (StemNotFoundException snfe) {
                  
                  //see if we should fix this problem
                  if (AttributeDefSave.this.createParentStemsIfNotExist) {
                    
                    //at this point the stem should be there (and is equal to currentStem), 
                    //just to be sure, query again
                    parentStem = Stem._createStemAndParentStemsIfNotExist(grouperSession, parentStemNameNew, parentStemNameNew);
                  } else {
                    throw new GrouperSessionException(new StemNotFoundException("Cant find stem: '" + parentStemNameNew 
                        + "' (from update on stem name: '" + attributeDefNameForError + "')"));
                  }
                }
                              
                AttributeDef theAttributeDef = null;
                //see if update
                boolean isUpdate = SAVE_MODE.isUpdate(AttributeDefSave.this.attributeDefNameToEdit, AttributeDefSave.this.name);
        
                if (isUpdate) {
                  String parentStemNameLookup = GrouperUtil.parentStemNameFromName(AttributeDefSave.this.attributeDefNameToEdit);
                  if (!StringUtils.equals(parentStemNameLookup, parentStemNameNew)) {
                    throw new GrouperSessionException(new RuntimeException("Can't move an attributeDef.  Existing parentStem: '"
                        + parentStemNameLookup + "', new stem: '" + parentStemNameNew + "'"));
                  }
                }    
                theAttributeDef = GrouperDAOFactory.getFactory().getAttributeDef().findByNameSecure(
                    AttributeDefSave.this.attributeDefNameToEdit, false, new QueryOptions().secondLevelCache(false));
                
                if (theAttributeDef != null) {
                  //while we are here, make sure id's match if passed in
                  if (!StringUtils.isBlank(AttributeDefSave.this.id) && !StringUtils.equals(AttributeDefSave.this.id, theAttributeDef.getUuid())) {
                    throw new RuntimeException("UUID attributeDef changes are not supported: new: " + AttributeDefSave.this.id + ", old: " 
                        + theAttributeDef.getId() + ", " + attributeDefNameForError);
                  }
                  
                } else {
                  if (SAVE_MODE.equals(SaveMode.INSERT_OR_UPDATE) || SAVE_MODE.equals(SaveMode.INSERT)) {
                    isUpdate = false;
                  } else {
                    throw new RuntimeException("Cant find attributeDef: " + attributeDefNameForError);
                  }
                  
                }
                //default
                AttributeDefSave.this.saveResultType = SaveResultType.NO_CHANGE;
                boolean needsSave = false;
                //if inserting
                if (!isUpdate) {
                  
                  if (!replaceAllSettings) {
                    throw new RuntimeException("cannot insert attribute def with replaceAllSettings being false");
                  }
                  
                  saveResultType = SaveResultType.INSERT;
                  AttributeDefType theAttributeDefType = GrouperUtil.defaultIfNull(AttributeDefSave.this.attributeDefType, AttributeDefType.attr);
                  theAttributeDef = parentStem.internal_addChildAttributeDef(AttributeDefSave.this.grouperSession, 
                      extensionNew, AttributeDefSave.this.id, theAttributeDefType, AttributeDefSave.this.description);
                } else {
                  
                  if (AttributeDefSave.this.attributeDefType != null && AttributeDefSave.this.attributeDefType != theAttributeDef.getAttributeDefType()) {
                    throw new RuntimeException("attributeDefType cannot be updated.");
                  }
                  
                  //check if different so it doesnt make unneeded queries
                  if (!StringUtils.equals(theAttributeDef.getExtension(), extensionNew)) {
                      
                    //lets just confirm that one doesnt exist
                    final String newName = GrouperUtil.parentStemNameFromName(theAttributeDef.getName()) + ":" + extensionNew;
                    
                    AttributeDef existingAttributeDef = (AttributeDef)GrouperSession.callbackGrouperSession(grouperSession.internal_getRootSession(), new GrouperSessionHandler() {
                      
                      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
                        return AttributeDefFinder.findByName(newName, false);
                      }
                    });
                    
                    if (existingAttributeDef != null && !StringUtils.equals(theAttributeDef.getId(), existingAttributeDef.getId())) {
                      throw new RuntimeException("AttributeDef already exists: " + newName);
                    }
                      
                    theAttributeDef.setExtension(extensionNew);
                    AttributeDefSave.this.saveResultType = SaveResultType.UPDATE;
                    needsSave = true;
                  }
                }
                
                if (AttributeDefSave.this.idIndex != null) {
                  if (AttributeDefSave.this.saveResultType == SaveResultType.INSERT) {

                    if (theAttributeDef.assignIdIndex(AttributeDefSave.this.idIndex)) {
                      needsSave = true;
                    }

                  } else {
                    //maybe they are equal...
                    throw new RuntimeException("Cannot update idIndex for an already created AttributeDef: " + idIndex + ", " + theAttributeDef.getName());
                  }
                }


                //now compare and put all attributes (then store if needed)
                //null throws exception? hmmm.  remove attribute if blank
                if (!StringUtils.equals(StringUtils.defaultString(StringUtils.trim(theAttributeDef.getDescription())), 
                    StringUtils.defaultString(StringUtils.trim(AttributeDefSave.this.description)))) {
                  
                  if (replaceAllSettings || descriptionAssigned) { 
                    
                    needsSave = true;
                    if (AttributeDefSave.this.saveResultType == SaveResultType.NO_CHANGE) {
                      AttributeDefSave.this.saveResultType = SaveResultType.UPDATE;
                    }
                    theAttributeDef.setDescription(AttributeDefSave.this.description);
                  }
                 
                }
                
                
                if (AttributeDefSave.this.assignToAttributeDef != theAttributeDef.isAssignToAttributeDef()) {
                  
                  if (replaceAllSettings || assignToAttributeDefAssigned) {
                    needsSave = true;
                    if (AttributeDefSave.this.saveResultType == SaveResultType.NO_CHANGE) {
                      AttributeDefSave.this.saveResultType = SaveResultType.UPDATE;
                    }
                    theAttributeDef.setAssignToAttributeDef(AttributeDefSave.this.assignToAttributeDef);
                  }
                  
                }
                if (AttributeDefSave.this.assignToAttributeDefAssn != theAttributeDef.isAssignToAttributeDefAssn()) {
                  
                  if (replaceAllSettings || assignToAttributeDefAssnAssigned) {
                    needsSave = true;
                    if (AttributeDefSave.this.saveResultType == SaveResultType.NO_CHANGE) {
                      AttributeDefSave.this.saveResultType = SaveResultType.UPDATE;
                    }
                    theAttributeDef.setAssignToAttributeDefAssn(AttributeDefSave.this.assignToAttributeDefAssn);
                  }
                  
                }
                if (AttributeDefSave.this.assignToEffMembership != theAttributeDef.isAssignToEffMembership()) {
                  if (replaceAllSettings || assignToEffMembershipAssigned) {
                    needsSave = true;
                    if (AttributeDefSave.this.saveResultType == SaveResultType.NO_CHANGE) {
                      AttributeDefSave.this.saveResultType = SaveResultType.UPDATE;
                    }
                    theAttributeDef.setAssignToEffMembership(AttributeDefSave.this.assignToEffMembership);
                  }
                }
                if (AttributeDefSave.this.assignToEffMembershipAssn != theAttributeDef.isAssignToEffMembershipAssn()) {
                  
                  if (replaceAllSettings || assignToEffMembershipAssnAssigned) {
                    needsSave = true;
                    if (AttributeDefSave.this.saveResultType == SaveResultType.NO_CHANGE) {
                      AttributeDefSave.this.saveResultType = SaveResultType.UPDATE;
                    }
                    theAttributeDef.setAssignToEffMembershipAssn(AttributeDefSave.this.assignToEffMembershipAssn);
                  }
                  
                }
                if (AttributeDefSave.this.assignToGroup != theAttributeDef.isAssignToGroup()) {
                  
                  if (replaceAllSettings || assignToGroupAssigned) {
                    needsSave = true;
                    if (AttributeDefSave.this.saveResultType == SaveResultType.NO_CHANGE) {
                      AttributeDefSave.this.saveResultType = SaveResultType.UPDATE;
                    }
                    theAttributeDef.setAssignToGroup(AttributeDefSave.this.assignToGroup);
                  }
                  
                }
                if (AttributeDefSave.this.assignToGroupAssn != theAttributeDef.isAssignToGroupAssn()) {
                  
                  if (replaceAllSettings || assignToGroupAssnAssigned) {
                    needsSave = true;
                    if (AttributeDefSave.this.saveResultType == SaveResultType.NO_CHANGE) {
                      AttributeDefSave.this.saveResultType = SaveResultType.UPDATE;
                    }
                    theAttributeDef.setAssignToGroupAssn(AttributeDefSave.this.assignToGroupAssn);
                  }
                  
                }
                if (AttributeDefSave.this.assignToImmMembership != theAttributeDef.isAssignToImmMembership()) {
                  
                  if (replaceAllSettings || assignToImmMembershipAssigned) {
                    needsSave = true;
                    if (AttributeDefSave.this.saveResultType == SaveResultType.NO_CHANGE) {
                      AttributeDefSave.this.saveResultType = SaveResultType.UPDATE;
                    }
                    theAttributeDef.setAssignToImmMembership(AttributeDefSave.this.assignToImmMembership);
                  }
                  
                }
                if (AttributeDefSave.this.assignToImmMembershipAssn != theAttributeDef.isAssignToImmMembershipAssn()) {
                  
                  if (replaceAllSettings || assignToImmMembershipAssnAssigned) {
                    needsSave = true;
                    if (AttributeDefSave.this.saveResultType == SaveResultType.NO_CHANGE) {
                      AttributeDefSave.this.saveResultType = SaveResultType.UPDATE;
                    }
                    theAttributeDef.setAssignToImmMembershipAssn(AttributeDefSave.this.assignToImmMembershipAssn);
                  }
                  
                }
                if (AttributeDefSave.this.assignToMember != theAttributeDef.isAssignToMember()) {
                  
                  if (replaceAllSettings || assignToMemberAssigned) {
                    needsSave = true;
                    if (AttributeDefSave.this.saveResultType == SaveResultType.NO_CHANGE) {
                      AttributeDefSave.this.saveResultType = SaveResultType.UPDATE;
                    }
                    theAttributeDef.setAssignToMember(AttributeDefSave.this.assignToMember);
                  }
                  
                }
                if (AttributeDefSave.this.assignToMemberAssn != theAttributeDef.isAssignToMemberAssn()) {
                  
                  if (replaceAllSettings || assignToMemberAssnAssigned) {
                    needsSave = true;
                    if (AttributeDefSave.this.saveResultType == SaveResultType.NO_CHANGE) {
                      AttributeDefSave.this.saveResultType = SaveResultType.UPDATE;
                    }
                    theAttributeDef.setAssignToMemberAssn(AttributeDefSave.this.assignToMemberAssn);
                  }
                  
                }
                if (AttributeDefSave.this.assignToStem != theAttributeDef.isAssignToStem()) {
                  
                  if (replaceAllSettings || assignToStemAssigned) {
                    needsSave = true;
                    if (AttributeDefSave.this.saveResultType == SaveResultType.NO_CHANGE) {
                      AttributeDefSave.this.saveResultType = SaveResultType.UPDATE;
                    }
                    theAttributeDef.setAssignToStem(AttributeDefSave.this.assignToStem);
                  }
                  
                }
                if (AttributeDefSave.this.assignToStemAssn != theAttributeDef.isAssignToStemAssn()) {
                  
                  if (replaceAllSettings || assignToStemAssnAssigned) {
                    needsSave = true;
                    if (AttributeDefSave.this.saveResultType == SaveResultType.NO_CHANGE) {
                      AttributeDefSave.this.saveResultType = SaveResultType.UPDATE;
                    }
                    theAttributeDef.setAssignToStemAssn(AttributeDefSave.this.assignToStemAssn);
                  }
                  
                }
                if (AttributeDefSave.this.attributeDefPublic != theAttributeDef.isAttributeDefPublic()) {
                  
                  if (replaceAllSettings || attributeDefPublicAssigned) {
                    needsSave = true;
                    if (AttributeDefSave.this.saveResultType == SaveResultType.NO_CHANGE) {
                      AttributeDefSave.this.saveResultType = SaveResultType.UPDATE;
                    }
                    theAttributeDef.setAttributeDefPublic(AttributeDefSave.this.attributeDefPublic);
                  }
                  
                }
                if (AttributeDefSave.this.multiAssignable != theAttributeDef.isMultiAssignable()) {
                  
                  if (replaceAllSettings || multiAssignableAssigned) {
                    needsSave = true;
                    if (AttributeDefSave.this.saveResultType == SaveResultType.NO_CHANGE) {
                      AttributeDefSave.this.saveResultType = SaveResultType.UPDATE;
                    }
                    theAttributeDef.setMultiAssignable(AttributeDefSave.this.multiAssignable);
                  }
                  
                }
                if (AttributeDefSave.this.multiValued != theAttributeDef.isMultiValued()) {
                  
                  if (replaceAllSettings || multiValuedAssigned) {
                    needsSave = true;
                    if (AttributeDefSave.this.saveResultType == SaveResultType.NO_CHANGE) {
                      AttributeDefSave.this.saveResultType = SaveResultType.UPDATE;
                    }
                    theAttributeDef.setMultiValued(AttributeDefSave.this.multiValued);
                  }
                  
                }
                if (AttributeDefSave.this.valueType != theAttributeDef.getValueType()) {
                  
                  if (replaceAllSettings || valueTypeAssigned) {
                    needsSave = true;
                    if (AttributeDefSave.this.saveResultType == SaveResultType.NO_CHANGE) {
                      AttributeDefSave.this.saveResultType = SaveResultType.UPDATE;
                    }
                    theAttributeDef.setValueType(AttributeDefSave.this.valueType);
                  }
                  
                }
                
                //only store once
                if (needsSave) {
                  theAttributeDef.store();
                }

                boolean changedPrivs = false;
                
                boolean adminDefaultChecked = theAttributeDef.getPrivilegeDelegate().hasAttrAdmin(SubjectFinder.findAllSubject());
                
                boolean updateDefaultChecked = theAttributeDef.getPrivilegeDelegate().hasAttrUpdate(SubjectFinder.findAllSubject());
                
                boolean readDefaultChecked = theAttributeDef.getPrivilegeDelegate().hasAttrRead(SubjectFinder.findAllSubject());
      
                boolean viewDefaultChecked = theAttributeDef.getPrivilegeDelegate().hasAttrView(SubjectFinder.findAllSubject());
      
                boolean optinDefaultChecked = theAttributeDef.getPrivilegeDelegate().hasAttrOptin(SubjectFinder.findAllSubject());
      
                boolean optoutDefaultChecked = theAttributeDef.getPrivilegeDelegate().hasAttrOptout(SubjectFinder.findAllSubject());
      
                boolean attrReadDefaultChecked = theAttributeDef.getPrivilegeDelegate().hasAttrDefAttrRead(SubjectFinder.findAllSubject());
      
                boolean attrUpdateDefaultChecked = theAttributeDef.getPrivilegeDelegate().hasAttrDefAttrUpdate(SubjectFinder.findAllSubject());
      
                if (AttributeDefSave.this.privAllAdmin != null && AttributeDefSave.this.privAllAdmin != adminDefaultChecked) {
                  
                  if (replaceAllSettings || privAllAdminAssigned) {
                    if (AttributeDefSave.this.privAllAdmin) {
                      changedPrivs = changedPrivs | theAttributeDef.getPrivilegeDelegate().grantPriv(SubjectFinder.findAllSubject(), AttributeDefPrivilege.ATTR_ADMIN, false);
                    } else {
                      changedPrivs = changedPrivs | theAttributeDef.getPrivilegeDelegate().revokePriv(SubjectFinder.findAllSubject(), AttributeDefPrivilege.ATTR_ADMIN, false);
                    }
                  }
                  
                }

                if (AttributeDefSave.this.privAllView != null && AttributeDefSave.this.privAllView != viewDefaultChecked) {
                  
                  if (replaceAllSettings || privAllViewAssigned) {
                    if (AttributeDefSave.this.privAllView) {
                      changedPrivs = changedPrivs | theAttributeDef.getPrivilegeDelegate().grantPriv(SubjectFinder.findAllSubject(), AttributeDefPrivilege.ATTR_VIEW, false);
                    } else {
                      changedPrivs = changedPrivs | theAttributeDef.getPrivilegeDelegate().revokePriv(SubjectFinder.findAllSubject(), AttributeDefPrivilege.ATTR_VIEW, false);
                    }
                  }
                  
                }
      
                if (AttributeDefSave.this.privAllRead != null && AttributeDefSave.this.privAllRead != readDefaultChecked) {
                  
                  if (replaceAllSettings || privAllReadAssigned) {
                    if (AttributeDefSave.this.privAllRead) {
                      changedPrivs = changedPrivs | theAttributeDef.getPrivilegeDelegate().grantPriv(SubjectFinder.findAllSubject(), AttributeDefPrivilege.ATTR_READ, false);
                    } else {
                      changedPrivs = changedPrivs | theAttributeDef.getPrivilegeDelegate().revokePriv(SubjectFinder.findAllSubject(), AttributeDefPrivilege.ATTR_READ, false);
                    }
                  }
                  
                }
                if (AttributeDefSave.this.privAllUpdate != null && AttributeDefSave.this.privAllUpdate != updateDefaultChecked) {
                  
                  if (replaceAllSettings || privAllUpdateAssigned) {
                    if (AttributeDefSave.this.privAllUpdate) {
                      changedPrivs = changedPrivs | theAttributeDef.getPrivilegeDelegate().grantPriv(SubjectFinder.findAllSubject(), AttributeDefPrivilege.ATTR_UPDATE, false);
                    } else {
                      changedPrivs = changedPrivs | theAttributeDef.getPrivilegeDelegate().revokePriv(SubjectFinder.findAllSubject(), AttributeDefPrivilege.ATTR_UPDATE, false);
                    }
                  }
                  
                }
                if (AttributeDefSave.this.privAllOptin != null && AttributeDefSave.this.privAllOptin != optinDefaultChecked) {
                  
                  if (replaceAllSettings || privAllOptinAssigned) {
                    if (AttributeDefSave.this.privAllOptin) {
                      changedPrivs = changedPrivs | theAttributeDef.getPrivilegeDelegate().grantPriv(SubjectFinder.findAllSubject(), AttributeDefPrivilege.ATTR_OPTIN, false);
                    } else {
                      changedPrivs = changedPrivs | theAttributeDef.getPrivilegeDelegate().revokePriv(SubjectFinder.findAllSubject(), AttributeDefPrivilege.ATTR_OPTIN, false);
                    }
                  }
                  
                }
                if (AttributeDefSave.this.privAllOptout != null && AttributeDefSave.this.privAllOptout != optoutDefaultChecked) {
                  
                  if (replaceAllSettings || privAllOptoutAssigned) {
                    if (AttributeDefSave.this.privAllOptout) {
                      changedPrivs = changedPrivs | theAttributeDef.getPrivilegeDelegate().grantPriv(SubjectFinder.findAllSubject(), AttributeDefPrivilege.ATTR_OPTOUT, false);
                    } else {
                      changedPrivs = changedPrivs | theAttributeDef.getPrivilegeDelegate().revokePriv(SubjectFinder.findAllSubject(), AttributeDefPrivilege.ATTR_OPTOUT, false);
                    }
                  }
                  
                }
                if (AttributeDefSave.this.privAllAttrRead != null && AttributeDefSave.this.privAllAttrRead != attrReadDefaultChecked) {
                  
                  if (replaceAllSettings || privAllAttrReadAssigned) {
                    if (AttributeDefSave.this.privAllAttrRead) {
                      changedPrivs = changedPrivs | theAttributeDef.getPrivilegeDelegate().grantPriv(SubjectFinder.findAllSubject(), AttributeDefPrivilege.ATTR_DEF_ATTR_READ, false);
                    } else {
                      changedPrivs = changedPrivs | theAttributeDef.getPrivilegeDelegate().revokePriv(SubjectFinder.findAllSubject(), AttributeDefPrivilege.ATTR_DEF_ATTR_READ, false);
                    }
                  }
                  
                }
                if (AttributeDefSave.this.privAllAttrUpdate != null && AttributeDefSave.this.privAllAttrUpdate != attrUpdateDefaultChecked) {
                  
                  if (replaceAllSettings || privAllAttrUpdateAssigned) {
                    if (AttributeDefSave.this.privAllAttrUpdate) {
                      changedPrivs = changedPrivs | theAttributeDef.getPrivilegeDelegate().grantPriv(SubjectFinder.findAllSubject(), AttributeDefPrivilege.ATTR_DEF_ATTR_UPDATE, false);
                    } else {
                      changedPrivs = changedPrivs | theAttributeDef.getPrivilegeDelegate().revokePriv(SubjectFinder.findAllSubject(), AttributeDefPrivilege.ATTR_DEF_ATTR_UPDATE, false);
                    }
                  }
                  
                }
                
                if (changedPrivs) {
                  if (AttributeDefSave.this.saveResultType == SaveResultType.NO_CHANGE) {
                    AttributeDefSave.this.saveResultType = SaveResultType.UPDATE;
                  }
                }

                
                return theAttributeDef;
              }
              
            });
            
        }
      });
      return attributeDef;
    } catch (RuntimeException re) {
      
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
      //must just be runtime
      throw re;
    }

  }

  private boolean privAllAdminAssigned;

  private boolean privAllAttrReadAssigned;

  private boolean privAllAttrUpdateAssigned;

  private boolean privAllOptinAssigned;

  private boolean privAllOptoutAssigned;

  private boolean privAllReadAssigned;

  private boolean privAllUpdateAssigned;

  private boolean privAllViewAssigned;
  
  /**
   * assign priv admin to be different than the defaults for grouperAll
   * @param thePrivAllAdmin
   * @return this for chaining
   */
  public AttributeDefSave assignPrivAllAdmin(boolean thePrivAllAdmin) {
    this.privAllAdmin = thePrivAllAdmin;
    this.privAllAdminAssigned = true;
    return this;
  }


  /**
   * assign priv attr read to be different than the defaults for grouperAll
   * @param thePrivAllAttrRead
   * @return this for chaining
   */
  public AttributeDefSave assignPrivAllAttrRead(boolean thePrivAllAttrRead) {
    this.privAllAttrRead = thePrivAllAttrRead;
    this.privAllAttrReadAssigned = true;
    return this;
  }


  /**
   * assign priv attr update to be different than the defaults for grouperAll
   * @param thePrivAllAttrUpdate
   * @return this for chaining
   */
  public AttributeDefSave assignPrivAllAttrUpdate(boolean thePrivAllAttrUpdate) {
    this.privAllAttrUpdate = thePrivAllAttrUpdate;
    this.privAllAttrUpdateAssigned = true;
    return this;
  }


  /**
   * assign priv optin to be different than the defaults for grouperAll
   * @param thePrivAllOptin
   * @return this for chaining
   */
  public AttributeDefSave assignPrivAllOptin(boolean thePrivAllOptin) {
    this.privAllOptin = thePrivAllOptin;
    this.privAllOptinAssigned = true;
    return this;
  }


  /**
   * assign priv optout to be different than the defaults for grouperAll
   * @param thePrivAllOptout
   * @return this for chaining
   */
  public AttributeDefSave assignPrivAllOptout(boolean thePrivAllOptout) {
    this.privAllOptout = thePrivAllOptout;
    this.privAllOptoutAssigned = true;
    return this;
  }


  /**
   * assign priv read to be different than the defaults for grouperAll
   * @param thePrivAllRead
   * @return this for chaining
   */
  public AttributeDefSave assignPrivAllRead(boolean thePrivAllRead) {
    this.privAllRead = thePrivAllRead;
    this.privAllReadAssigned = true;
    return this;
  }


  /**
   * assign priv update to be different than the defaults for grouperAll
   * @param thePrivAllUpdate
   * @return this for chaining
   */
  public AttributeDefSave assignPrivAllUpdate(boolean thePrivAllUpdate) {
    this.privAllUpdate = thePrivAllUpdate;
    this.privAllUpdateAssigned = true;
    return this;
  }


  /**
   * assign priv view to be different than the defaults for grouperAll
   * @param thePrivAllView
   * @return this for chaining
   */
  public AttributeDefSave assignPrivAllView(boolean thePrivAllView) {
    this.privAllView = thePrivAllView;
    this.privAllViewAssigned = true;
    return this;
  }
}
