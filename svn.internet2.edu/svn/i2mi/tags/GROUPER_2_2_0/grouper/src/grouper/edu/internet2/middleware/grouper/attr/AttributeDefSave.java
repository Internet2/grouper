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
 * $Id: GroupSave.java,v 1.10 2009-11-17 02:52:29 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.attr;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
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
import edu.internet2.middleware.grouper.tableIndex.TableIndex;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * Use this class to insert or update an attribute definition
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
  
  /**
   * assign description
   * @param theDescription
   * @return this for chaining
   */
  public AttributeDefSave assignDescription(String theDescription) {
    this.description = theDescription;
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

  /**
   * if assign
   * @param theBoolean
   * @return this for chaining
   */
  public AttributeDefSave assignToAttributeDef(boolean theBoolean) {
    this.assignToAttributeDef = theBoolean;
    return this;
  }
  
  /** if can assign to assignment of attribute def */
  private boolean assignToAttributeDefAssn;

  /**
   * if assign
   * @param theBoolean
   * @return this for chaining
   */
  public AttributeDefSave assignToAttributeDefAssn(boolean theBoolean) {
    this.assignToAttributeDefAssn = theBoolean;
    return this;
  }

  /** if can assign to effective membership */
  private boolean assignToEffMembership;

  /**
   * if assign
   * @param theBoolean
   * @return this for chaining
   */
  public AttributeDefSave assignToEffMembership(boolean theBoolean) {
    this.assignToEffMembership = theBoolean;
    return this;
  }

  /** if can assign to assignment of effective membership */
  private boolean assignToEffMembershipAssn;

  /**
   * if assign
   * @param theBoolean
   * @return this for chaining
   */
  public AttributeDefSave assignToEffMembershipAssn(boolean theBoolean) {
    this.assignToEffMembershipAssn = theBoolean;
    return this;
  }

  /** if can assign to group/role */
  private boolean assignToGroup;

  /**
   * if assign
   * @param theBoolean
   * @return this for chaining
   */
  public AttributeDefSave assignToGroup(boolean theBoolean) {
    this.assignToGroup = theBoolean;
    return this;
  }

  /** if can assign to assignment of group/role */
  private boolean assignToGroupAssn;

  /**
   * if assign
   * @param theBoolean
   * @return this for chaining
   */
  public AttributeDefSave assignToGroupAssn(boolean theBoolean) {
    this.assignToGroupAssn = theBoolean;
    return this;
  }

  /** if can assign to immediate membership */
  private boolean assignToImmMembership;

  /**
   * if assign
   * @param theBoolean
   * @return this for chaining
   */
  public AttributeDefSave assignToImmMembership(boolean theBoolean) {
    this.assignToImmMembership = theBoolean;
    return this;
  }

  /** if can assign to assignment of immediate membership */
  private boolean assignToImmMembershipAssn;

  /**
   * if assign
   * @param theBoolean
   * @return this for chaining
   */
  public AttributeDefSave assignToImmMembershipAssn(boolean theBoolean) {
    this.assignToImmMembershipAssn = theBoolean;
    return this;
  }

  /** if can assign to member */
  private boolean assignToMember;

  /**
   * if assign
   * @param theBoolean
   * @return this for chaining
   */
  public AttributeDefSave assignToMember(boolean theBoolean) {
    this.assignToMember = theBoolean;
    return this;
  }

  /** if can assign to assignment of member */
  private boolean assignToMemberAssn;

  /**
   * if assign
   * @param theBoolean
   * @return this for chaining
   */
  public AttributeDefSave assignToMemberAssn(boolean theBoolean) {
    this.assignToMemberAssn = theBoolean;
    return this;
  }

  /** if can assign to stem */
  private boolean assignToStem;

  /**
   * if assign
   * @param theBoolean
   * @return this for chaining
   */
  public AttributeDefSave assignToStem(boolean theBoolean) {
    this.assignToStem = theBoolean;
    return this;
  }

  /** if can assign to assignment of stem */
  private boolean assignToStemAssn;

  /**
   * if assign
   * @param theBoolean
   * @return this for chaining
   */
  public AttributeDefSave assignToStemAssn(boolean theBoolean) {
    this.assignToStemAssn = theBoolean;
    return this;
  }

  /**
   * if the attribute def is public, otherwise you just see it in this stem and substem
   */
  private boolean attributeDefPublic = false;

  /**
   * if assign
   * @param theBoolean
   * @return this for chaining
   */
  public AttributeDefSave assignAttributeDefPublic(boolean theBoolean) {
    this.attributeDefPublic = theBoolean;
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

  /**
   * if assign
   * @param theBoolean
   * @return this for chaining
   */
  public AttributeDefSave assignMultiAssignable(boolean theBoolean) {
    this.multiAssignable = theBoolean;
    return this;
  }

  /**
   * if more than one value (same type) can be assigned to the attribute assignment
   */
  private boolean multiValued;

  /**
   * if assign
   * @param theBoolean
   * @return self for chaining
   */
  public AttributeDefSave assignMultiValued(boolean theBoolean) {
    this.multiValued = theBoolean;
    return this;
  }

  /**
   * type of the value,  int, double, string, marker
   */
  private AttributeDefValueType valueType = AttributeDefValueType.marker;
  
  /**
   * if assign
   * @param theBoolean
   * @return this for chaining
   */
  public AttributeDefSave assignValueType(AttributeDefValueType attributeDefValueType) {
    this.valueType = attributeDefValueType;
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
                  saveResultType = SaveResultType.INSERT;
                  AttributeDefType theAttributeDefType = GrouperUtil.defaultIfNull(AttributeDefSave.this.attributeDefType, AttributeDefType.attr);
                  theAttributeDef = parentStem.internal_addChildAttributeDef(AttributeDefSave.this.grouperSession, 
                      extensionNew, AttributeDefSave.this.id, theAttributeDefType, AttributeDefSave.this.description);
                } else {
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
                if (!StringUtils.equals(StringUtils.defaultString(theAttributeDef.getDescription()), 
                    StringUtils.defaultString(StringUtils.trim(AttributeDefSave.this.description)))) {
                  needsSave = true;
                  if (AttributeDefSave.this.saveResultType == SaveResultType.NO_CHANGE) {
                    AttributeDefSave.this.saveResultType = SaveResultType.UPDATE;
                  }
                  theAttributeDef.setDescription(AttributeDefSave.this.description);
                }

                if (AttributeDefSave.this.assignToAttributeDef != theAttributeDef.isAssignToAttributeDef()) {
                  needsSave = true;
                  if (AttributeDefSave.this.saveResultType == SaveResultType.NO_CHANGE) {
                    AttributeDefSave.this.saveResultType = SaveResultType.UPDATE;
                  }
                  theAttributeDef.setAssignToAttributeDef(AttributeDefSave.this.assignToAttributeDef);
                }
                if (AttributeDefSave.this.assignToAttributeDefAssn != theAttributeDef.isAssignToAttributeDefAssn()) {
                  needsSave = true;
                  if (AttributeDefSave.this.saveResultType == SaveResultType.NO_CHANGE) {
                    AttributeDefSave.this.saveResultType = SaveResultType.UPDATE;
                  }
                  theAttributeDef.setAssignToAttributeDefAssn(AttributeDefSave.this.assignToAttributeDefAssn);
                }
                if (AttributeDefSave.this.assignToEffMembership != theAttributeDef.isAssignToEffMembership()) {
                  needsSave = true;
                  if (AttributeDefSave.this.saveResultType == SaveResultType.NO_CHANGE) {
                    AttributeDefSave.this.saveResultType = SaveResultType.UPDATE;
                  }
                  theAttributeDef.setAssignToEffMembership(AttributeDefSave.this.assignToEffMembership);
                }
                if (AttributeDefSave.this.assignToEffMembershipAssn != theAttributeDef.isAssignToEffMembershipAssn()) {
                  needsSave = true;
                  if (AttributeDefSave.this.saveResultType == SaveResultType.NO_CHANGE) {
                    AttributeDefSave.this.saveResultType = SaveResultType.UPDATE;
                  }
                  theAttributeDef.setAssignToEffMembershipAssn(AttributeDefSave.this.assignToEffMembershipAssn);
                }
                if (AttributeDefSave.this.assignToGroup != theAttributeDef.isAssignToGroup()) {
                  needsSave = true;
                  if (AttributeDefSave.this.saveResultType == SaveResultType.NO_CHANGE) {
                    AttributeDefSave.this.saveResultType = SaveResultType.UPDATE;
                  }
                  theAttributeDef.setAssignToGroup(AttributeDefSave.this.assignToGroup);
                }
                if (AttributeDefSave.this.assignToGroupAssn != theAttributeDef.isAssignToGroupAssn()) {
                  needsSave = true;
                  if (AttributeDefSave.this.saveResultType == SaveResultType.NO_CHANGE) {
                    AttributeDefSave.this.saveResultType = SaveResultType.UPDATE;
                  }
                  theAttributeDef.setAssignToGroupAssn(AttributeDefSave.this.assignToGroupAssn);
                }
                if (AttributeDefSave.this.assignToImmMembership != theAttributeDef.isAssignToImmMembership()) {
                  needsSave = true;
                  if (AttributeDefSave.this.saveResultType == SaveResultType.NO_CHANGE) {
                    AttributeDefSave.this.saveResultType = SaveResultType.UPDATE;
                  }
                  theAttributeDef.setAssignToImmMembership(AttributeDefSave.this.assignToImmMembership);
                }
                if (AttributeDefSave.this.assignToImmMembershipAssn != theAttributeDef.isAssignToImmMembershipAssn()) {
                  needsSave = true;
                  if (AttributeDefSave.this.saveResultType == SaveResultType.NO_CHANGE) {
                    AttributeDefSave.this.saveResultType = SaveResultType.UPDATE;
                  }
                  theAttributeDef.setAssignToImmMembershipAssn(AttributeDefSave.this.assignToImmMembershipAssn);
                }
                if (AttributeDefSave.this.assignToMember != theAttributeDef.isAssignToMember()) {
                  needsSave = true;
                  if (AttributeDefSave.this.saveResultType == SaveResultType.NO_CHANGE) {
                    AttributeDefSave.this.saveResultType = SaveResultType.UPDATE;
                  }
                  theAttributeDef.setAssignToMember(AttributeDefSave.this.assignToMember);
                }
                if (AttributeDefSave.this.assignToMemberAssn != theAttributeDef.isAssignToMemberAssn()) {
                  needsSave = true;
                  if (AttributeDefSave.this.saveResultType == SaveResultType.NO_CHANGE) {
                    AttributeDefSave.this.saveResultType = SaveResultType.UPDATE;
                  }
                  theAttributeDef.setAssignToMemberAssn(AttributeDefSave.this.assignToMemberAssn);
                }
                if (AttributeDefSave.this.assignToStem != theAttributeDef.isAssignToStem()) {
                  needsSave = true;
                  if (AttributeDefSave.this.saveResultType == SaveResultType.NO_CHANGE) {
                    AttributeDefSave.this.saveResultType = SaveResultType.UPDATE;
                  }
                  theAttributeDef.setAssignToStem(AttributeDefSave.this.assignToStem);
                }
                if (AttributeDefSave.this.assignToStemAssn != theAttributeDef.isAssignToStemAssn()) {
                  needsSave = true;
                  if (AttributeDefSave.this.saveResultType == SaveResultType.NO_CHANGE) {
                    AttributeDefSave.this.saveResultType = SaveResultType.UPDATE;
                  }
                  theAttributeDef.setAssignToStemAssn(AttributeDefSave.this.assignToStemAssn);
                }
                if (AttributeDefSave.this.attributeDefPublic != theAttributeDef.isAttributeDefPublic()) {
                  needsSave = true;
                  if (AttributeDefSave.this.saveResultType == SaveResultType.NO_CHANGE) {
                    AttributeDefSave.this.saveResultType = SaveResultType.UPDATE;
                  }
                  theAttributeDef.setAttributeDefPublic(AttributeDefSave.this.attributeDefPublic);
                }
                if (AttributeDefSave.this.multiAssignable != theAttributeDef.isMultiAssignable()) {
                  needsSave = true;
                  if (AttributeDefSave.this.saveResultType == SaveResultType.NO_CHANGE) {
                    AttributeDefSave.this.saveResultType = SaveResultType.UPDATE;
                  }
                  theAttributeDef.setMultiAssignable(AttributeDefSave.this.multiAssignable);
                }
                if (AttributeDefSave.this.multiValued != theAttributeDef.isMultiValued()) {
                  needsSave = true;
                  if (AttributeDefSave.this.saveResultType == SaveResultType.NO_CHANGE) {
                    AttributeDefSave.this.saveResultType = SaveResultType.UPDATE;
                  }
                  theAttributeDef.setMultiValued(AttributeDefSave.this.multiValued);
                }
                if (AttributeDefSave.this.valueType != theAttributeDef.getValueType()) {
                  needsSave = true;
                  if (AttributeDefSave.this.saveResultType == SaveResultType.NO_CHANGE) {
                    AttributeDefSave.this.saveResultType = SaveResultType.UPDATE;
                  }
                  theAttributeDef.setValueType(AttributeDefSave.this.valueType);
                }
                
                //only store once
                if (needsSave) {
                  theAttributeDef.store();
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
}
