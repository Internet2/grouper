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
 * $Id: CompositeSave.java,v 1.10 2009-11-17 02:52:29 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.group;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.Composite;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.exception.AttributeDefNameNotFoundException;
import edu.internet2.middleware.grouper.exception.GroupNotFoundException;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.exception.StemNotFoundException;
import edu.internet2.middleware.grouper.hibernate.GrouperTransaction;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionHandler;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.misc.CompositeType;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.misc.SaveMode;
import edu.internet2.middleware.grouper.misc.SaveResultType;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * Use this class to insert or update a composite
 * e.g.
 * composite = new CompositeSave(grouperSession).assignOwnerGroupName("test:testGroup").assignLeftFactorGroupName("a:b"c).assignRightFactorGroupName("a:b:d").assignCompsiteType(CompositeType.COMPLEMENT).save();
 */
public class CompositeSave {

  /**
   * create a new composite save
   * @param theGrouperSession
   */
  public CompositeSave(GrouperSession theGrouperSession) {
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
  public CompositeSave assignSaveMode(SaveMode theSaveMode) {
    this.saveMode = theSaveMode;
    return this;
  }
  
  /** save type after the save */
  private SaveResultType saveResultType = null;

  /** id of this composite */
  private String id;

  /**
   * id of this composite
   * @param theId
   * @return this for chaining
   */
  public CompositeSave assignId(String theId) {
    this.id = theId;
    return this;
  }
  
  /** 
   * owner group id, this is the foreign key mutually exclusive with ownerGroupName
   */
  private String ownerGroupId;

  /**
   * this is the foreign key mutually exclusive with ownerGroupName
   * @param theOwnerGroupId
   * @return this for chaining
   */
  public CompositeSave assignOwnerGroupId(String theOwnerGroupId) {
    this.ownerGroupId = theOwnerGroupId;
    return this;
  }

  /**
   * owner group name, this is the foreign key mutually exclusive with ownerGroupId
   */
  private String ownerGroupName;

  /**
   * left factor group name, this is the foreign key mutually exclusive with leftFactorGroupId
   * @param theLeftFactorGroupName
   * @return this for chaining
   */
  public CompositeSave assignLeftFactorGroupName(String theLeftFactorGroupName) {
    this.leftFactorGroupName = theLeftFactorGroupName;
    return this;
  }
  
  /**
   * owner group foreign key
   * @param theOwnerGroup
   * @return this for chaining
   */
  public CompositeSave assignOwnerGroup(Group theOwnerGroup) {
    this.ownerGroup = theOwnerGroup;
    this.ownerGroupId = theOwnerGroup == null ? null : theOwnerGroup.getId();
    this.ownerGroupName = theOwnerGroup == null ? null : theOwnerGroup.getName();
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
   * owner group of composite
   */
  private Group ownerGroup = null;

  /**
   * leftFactor group of composite
   */
  private Group leftFactorGroup = null;

  /** 
   * leftFactorGroupId, this is the foreign key mutually exclusive with leftFactorGroupName
   */
  private String leftFactorGroupId;

  /**
   * left factor group name, this is the foreign key mutually exclusive with leftFactorGroupId
   */
  private String leftFactorGroupName;

  /**
   * rightFactor group of composite
   */
  private Group rightFactorGroup = null;

  /** 
   * right factor group id, this is the foreign key mutually exclusive with fightFactorGroupName
   */
  private String rightFactorGroupId;

  /**
   * right factor group name, this is the foreign key mutually exclusive with rightFactorGroupId
   */
  private String rightFactorGroupName;

  /**
   * type of composite
   */
  private CompositeType compositeType;
  
  /**
   * assign the composite type
   * @param theCompositeType
   * @return this for chaining
   */
  public CompositeSave assignCompositeType(CompositeType theCompositeType) {
    this.compositeType = theCompositeType;
    return this;
  }
  
  /**
   * massage and validate fields
   */
  private void massageAndValidateFields() {

    if (!StringUtils.isBlank(this.ownerGroupId)) {
      if (this.ownerGroup != null) {
        if (!StringUtils.equals(this.ownerGroupId, this.ownerGroup.getId())) {
          throw new RuntimeException("Passing in owner group id and owner group but dont match '" + this.ownerGroupId
              + "', '" + this.ownerGroup.getId() + "', '" + this.ownerGroup.getName() + "'");
        }
      } else {
        this.ownerGroup = GroupFinder.findByUuid(this.grouperSession, this.ownerGroupId, true);
      }
    }
    if (!StringUtils.isBlank(this.ownerGroupName)) {
      if (this.ownerGroup != null && !StringUtils.equals(this.ownerGroup.getName(), this.ownerGroupName)
          && !StringUtils.equals(ownerGroup.getAlternateName(), this.ownerGroupName)) {
        throw new RuntimeException("Passing in owner group id and owner name but dont match '" + this.ownerGroupId
            + "', '" + this.ownerGroupName + "', '" + this.ownerGroup.getName() + "'");
      }
      if (this.ownerGroup == null) {
        this.ownerGroup = GroupFinder.findByName(this.grouperSession, this.ownerGroupName, true);
      }
    }

    if (this.ownerGroup != null) {
      this.ownerGroupId = this.ownerGroup.getId();
      this.ownerGroupName = this.ownerGroup.getName();
    } else {
      throw new RuntimeException("ownerGroup is required");
    }
    
    if (!StringUtils.isBlank(this.leftFactorGroupId)) {
      if (this.leftFactorGroup != null) {
        if (!StringUtils.equals(this.leftFactorGroupId, this.leftFactorGroup.getId())) {
          throw new RuntimeException("Passing in leftFactor group id and leftFactor group but dont match '" + this.leftFactorGroupId
              + "', '" + this.leftFactorGroup.getId() + "', '" + this.leftFactorGroup.getName() + "'");
        }
      } else {
        this.leftFactorGroup = GroupFinder.findByUuid(this.grouperSession, this.leftFactorGroupId, true);
      }
    }
    if (!StringUtils.isBlank(this.leftFactorGroupName)) {
      if (this.leftFactorGroup != null && !StringUtils.equals(this.leftFactorGroup.getName(), this.leftFactorGroupName)
          && !StringUtils.equals(leftFactorGroup.getAlternateName(), this.leftFactorGroupName)) {
        throw new RuntimeException("Passing in leftFactor group id and leftFactor name but dont match '" + this.leftFactorGroupId
            + "', '" + this.leftFactorGroupName + "', '" + this.leftFactorGroup.getName() + "'");
      }
      if (this.leftFactorGroup == null) {
        this.leftFactorGroup = GroupFinder.findByName(this.grouperSession, this.leftFactorGroupName, true);
      }
    }

    if (this.leftFactorGroup != null) {
      this.leftFactorGroupId = this.leftFactorGroup.getId();
      this.leftFactorGroupName = this.leftFactorGroup.getName();
    } else {
      throw new RuntimeException("leftFactorGroup is required");
    }

    if (!StringUtils.isBlank(this.rightFactorGroupId)) {
      if (this.rightFactorGroup != null) {
        if (!StringUtils.equals(this.rightFactorGroupId, this.rightFactorGroup.getId())) {
          throw new RuntimeException("Passing in rightFactor group id and rightFactor group but dont match '" + this.rightFactorGroupId
              + "', '" + this.rightFactorGroup.getId() + "', '" + this.rightFactorGroup.getName() + "'");
        }
      } else {
        this.rightFactorGroup = GroupFinder.findByUuid(this.grouperSession, this.rightFactorGroupId, true);
      }
    }
    if (!StringUtils.isBlank(this.rightFactorGroupName)) {
      if (this.rightFactorGroup != null && !StringUtils.equals(this.rightFactorGroup.getName(), this.rightFactorGroupName)
          && !StringUtils.equals(rightFactorGroup.getAlternateName(), this.rightFactorGroupName)) {
        throw new RuntimeException("Passing in rightFactor group id and rightFactor name but dont match '" + this.rightFactorGroupId
            + "', '" + this.rightFactorGroupName + "', '" + this.rightFactorGroup.getName() + "'");
      }
      if (this.rightFactorGroup == null) {
        this.rightFactorGroup = GroupFinder.findByName(this.grouperSession, this.rightFactorGroupName, true);
      }
    }

    if (this.rightFactorGroup != null) {
      this.rightFactorGroupId = this.rightFactorGroup.getId();
      this.rightFactorGroupName = this.rightFactorGroup.getName();
    } else {
      throw new RuntimeException("rightFactorGroup is required");
    }

    if (this.compositeType == null) {
      throw new RuntimeException("compositeType is required");
    }
    
    //default to insert or update
    this.saveMode = (SaveMode)ObjectUtils.defaultIfNull(this.saveMode, SaveMode.INSERT_OR_UPDATE);

  }

  /**
   * the composite
   */
  private Composite composite;

  /**
   * 
   * @return the composite
   */
  public Composite getComposite() {
    return this.composite;
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
  public Composite save() 
        throws AttributeDefNameNotFoundException, InsufficientPrivilegeException, StemNotFoundException, 
        GroupNotFoundException {

    // figure out fields, validate them
    massageAndValidateFields();

    final SaveMode SAVE_MODE = CompositeSave.this.saveMode;

    final CompositeSave THIS = this;
    
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

                THIS.composite = THIS.ownerGroup.getComposite(false);
                
                //while we are here, make sure uuid's match if passed in
                //not sure this can happen, if you pass in a uuid it will be used
                if (!StringUtils.isBlank(THIS.id) && THIS.composite != null && !StringUtils.equals(THIS.composite.getUuid(), THIS.id)) {
                  throw new RuntimeException("UUID composite changes are not supported: new: " 
                      + THIS.composite.getUuid() + ", old: " 
                      + THIS.id);
                }

                if (THIS.composite == null) {
                  if (SAVE_MODE.equals(SaveMode.INSERT_OR_UPDATE) || SAVE_MODE.equals(SaveMode.INSERT)) {
                    isUpdate = false;
                  } else {
                    throw new RuntimeException("There is no existing composite but the SaveMode is " + SAVE_MODE);
                  }
                } else {
                  
                  if (SAVE_MODE.equals(SaveMode.INSERT_OR_UPDATE)) {
                    isUpdate = true;
                  }
                }
                
                //default
                THIS.saveResultType = SaveResultType.NO_CHANGE;
                //if inserting
                boolean needsSave = false;
                if (!isUpdate) {
                  THIS.saveResultType = SaveResultType.INSERT;
                  
                } else {

                  if (!StringUtils.equals(THIS.leftFactorGroup.getId(), THIS.composite.getLeftFactorUuid())) {
                    needsSave = true;
                  }
                  
                  if (!StringUtils.equals(THIS.rightFactorGroup.getId(), THIS.composite.getRightFactorUuid())) {
                    needsSave = true;
                  }

                  if (THIS.compositeType != THIS.composite.getType()) {
                    needsSave = true;
                  }
                  
                }
                
                //this is an update if it is not an insert and needs an update
                if (needsSave && THIS.saveResultType == SaveResultType.NO_CHANGE) {
                  THIS.saveResultType = SaveResultType.UPDATE;
                }

                //only store once
                if (needsSave) {

                  THIS.ownerGroup.deleteCompositeMember();

                }
                
                if (needsSave || THIS.saveResultType == SaveResultType.INSERT) {
                  THIS.composite = THIS.ownerGroup.internal_addCompositeMember(THIS.grouperSession, THIS.compositeType, THIS.leftFactorGroup, THIS.rightFactorGroup, THIS.id);
                }
                                
                return null;
              }

          });
        }
      });
      return this.composite;
    } catch (RuntimeException re) {

      GrouperUtil.injectInException(re, "Problem saving composite: " + this.ownerGroup
          + ", left: " + this.leftFactorGroup + ", " + this.compositeType + ", right: " + this.rightFactorGroup
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
   * if this is a group attribute, this is the foreign key
   * @param theLeftFactorGroup
   * @return this for chaining
   */
  public CompositeSave assignLeftFactorGroup(Group theLeftFactorGroup) {
    this.leftFactorGroup = theLeftFactorGroup;
    this.leftFactorGroupId = theLeftFactorGroup == null ? null : theLeftFactorGroup.getId();
    this.leftFactorGroupName = theLeftFactorGroup == null ? null : theLeftFactorGroup.getName();
    return this;
  }

  /**
   * if this is a group attribute, this is the foreign key mutually exclusive with ownerGroupName
   * @param theLeftFactorGroupId
   * @return this for chaining
   */
  public CompositeSave assignLeftFactorGroupId(String theLeftFactorGroupId) {
    this.leftFactorGroupId = theLeftFactorGroupId;
    return this;
  }

  /**
   * right factor group name, this is the foreign key mutually exclusive with rightFactorGroupId
   * @param theRightFactorGroupName
   * @return this for chaining
   */
  public CompositeSave assignRightFactorGroupName(String theRightFactorGroupName) {
    this.rightFactorGroupName = theRightFactorGroupName;
    return this;
  }

  /**
   * right factor group this is the foreign key
   * @param theRightFactorGroup
   * @return this for chaining
   */
  public CompositeSave assignRightFactorGroup(Group theRightFactorGroup) {
    this.rightFactorGroup = theRightFactorGroup;
    this.rightFactorGroupId = theRightFactorGroup == null ? null : theRightFactorGroup.getId();
    this.rightFactorGroupName = theRightFactorGroup == null ? null : theRightFactorGroup.getName();
    return this;
  }

  /**
   * right factor group id, this is the foreign key mutually exclusive with rightFactorGroupName
   * @param theRightFactorGroupId
   * @return this for chaining
   */
  public CompositeSave assignRightFactorGroupId(String theRightFactorGroupId) {
    this.rightFactorGroupId = theRightFactorGroupId;
    return this;
  }

  /**
   * owner group name, this is the foreign key mutually exclusive with ownerGroupId
   * @param theOwnerGroupName
   * @return this for chaining
   */
  public CompositeSave assignOwnerGroupName(String theOwnerGroupName) {
    this.ownerGroupName = theOwnerGroupName;
    return this;
  }
  
}
