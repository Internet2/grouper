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

import edu.internet2.middleware.grouper.exception.AttributeDefNameNotFoundException;
import edu.internet2.middleware.grouper.exception.GroupNotFoundException;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.exception.StemNotFoundException;
import edu.internet2.middleware.grouper.hibernate.GrouperTransaction;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionHandler;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.misc.CompositeType;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.misc.SaveMode;
import edu.internet2.middleware.grouper.misc.SaveResultType;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * <p>Use this class to insert or update or delete a composite</p>
 * <p>Sample call (type is complement or intersection)
 * 
 * <blockquote>
 * <pre>
 * Composite composite = new CompositeSave().assignOwnerName(group1.getName()).assignLeftFactorName(group2.getName()).assignRightFactorName(group3.getName())
 *   .assignType("complement").save();
 * </pre>
 * </blockquote>
 * 
 * </p>
 * 
 * <p> Sample call to delete a composite
 * <blockquote>
 * <pre>
 * new CompositeSave().assignOwnerName(group1.getName()).assignLeftFactorName(group2.getName()).assignRightFactorName(group3.getName())
 *  .assignSaveMode(SaveMode.DELETE).save();
 * </pre>
 * </blockquote>
 * </p>
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


  /**
   * create a new membership save
   * @param theGrouperSession
   */
  public CompositeSave() {
  }

  /**
   * type is intersection or complement
   */
  private String type;

  /**
   * uuid of composite on insert
   */
  private String id;

  /**
   * id of composite on insert
   * @param theId
   * @return this for chaining
   */
  public CompositeSave assignId(String theId) {
    this.id = theId;
    return this;
  }
  
  /**
   * 
   * @param theType
   * @return this for chaining
   */
  public CompositeSave assignType(String theType) {
    this.type = theType;
    return this;
  }
  
  /** name to change to */
  private String ownerName;

  /**
   * 
   * @param theLeftFactor
   * @return this for chaining
   */
  public CompositeSave assignLeftFactorName(String theLeftFactor) {
    this.leftFactorName = theLeftFactor;
    return this;
  }
  
  /** leftFactor name to change to */
  private String leftFactorName;

  /**
   * rightFactorName
   */
  private String rightFactorName;
  
  /**
   * 
   * @param theRightFactor
   * @return this for chaining
   */
  public CompositeSave assignRightFactorName(String theRightFactor) {
    this.rightFactorName = theRightFactor;
    return this;
  }
  
  /**
   * name
   * @param name1
   * @return name
   */
  public CompositeSave assignOwnerName(String name1) {
    this.ownerName = name1;
    return this;
  }
  
  /** save mode */
  private SaveMode saveMode;

  /**
   * asssign save mode
   * @param theSaveMode
   * @return this for chaining
   */
  public CompositeSave assignSaveMode(SaveMode theSaveMode) {
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
   * left factor group name, this is the foreign key mutually exclusive with leftFactorGroupId
   * @param theLeftFactorGroupName
   * @return this for chaining
   */
  public CompositeSave assignLeftFactorGroupName(String theLeftFactorGroupName) {
    this.leftFactorName = theLeftFactorGroupName;
    return this;
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
   * rightFactor group of composite
   */
  private Group rightFactorGroup = null;

  /** 
   * right factor group id, this is the foreign key mutually exclusive with fightFactorGroupName
   */
  private String rightFactorGroupId;

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
    if (!StringUtils.isBlank(this.ownerName)) {
      if (this.ownerGroup != null && !StringUtils.equals(this.ownerGroup.getName(), this.ownerName)
          && !StringUtils.equals(ownerGroup.getAlternateName(), this.ownerName)) {
        throw new RuntimeException("Passing in owner group id and owner name but dont match '" + this.ownerGroupId
            + "', '" + this.ownerName + "', '" + this.ownerGroup.getName() + "'");
      }
      if (this.ownerGroup == null) {
        this.ownerGroup = GroupFinder.findByName(this.grouperSession, this.ownerName, true);
      }
    }

    if (this.ownerGroup != null) {
      this.ownerGroupId = this.ownerGroup.getId();
      this.ownerName = this.ownerGroup.getName();
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
    if (!StringUtils.isBlank(this.leftFactorName)) {
      if (this.leftFactorGroup != null && !StringUtils.equals(this.leftFactorGroup.getName(), this.leftFactorName)
          && !StringUtils.equals(leftFactorGroup.getAlternateName(), this.leftFactorName)) {
        throw new RuntimeException("Passing in leftFactor group id and leftFactor name but dont match '" + this.leftFactorGroupId
            + "', '" + this.leftFactorName + "', '" + this.leftFactorGroup.getName() + "'");
      }
      if (this.leftFactorGroup == null) {
        this.leftFactorGroup = GroupFinder.findByName(this.grouperSession, this.leftFactorName, true);
      }
    }

    if (this.leftFactorGroup != null) {
      this.leftFactorGroupId = this.leftFactorGroup.getId();
      this.leftFactorName = this.leftFactorGroup.getName();
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
    if (!StringUtils.isBlank(this.rightFactorName)) {
      if (this.rightFactorGroup != null && !StringUtils.equals(this.rightFactorGroup.getName(), this.rightFactorName)
          && !StringUtils.equals(rightFactorGroup.getAlternateName(), this.rightFactorName)) {
        throw new RuntimeException("Passing in rightFactor group id and rightFactor name but dont match '" + this.rightFactorGroupId
            + "', '" + this.rightFactorName + "', '" + this.rightFactorGroup.getName() + "'");
      }
      if (this.rightFactorGroup == null) {
        this.rightFactorGroup = GroupFinder.findByName(this.grouperSession, this.rightFactorName, true);
      }
    }

    if (this.rightFactorGroup != null) {
      this.rightFactorGroupId = this.rightFactorGroup.getId();
      this.rightFactorName = this.rightFactorGroup.getName();
    } else {
      throw new RuntimeException("rightFactorGroup is required");
    }

    if (this.compositeType == null) {
      this.compositeType = CompositeType.valueOfIgnoreCase(this.type);
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
   * owner group foreign key
   * @param theOwnerGroup
   * @return this for chaining
   */
  public CompositeSave assignOwnerGroup(Group theOwnerGroup) {
    this.ownerGroup = theOwnerGroup;
    this.ownerGroupId = theOwnerGroup == null ? null : theOwnerGroup.getId();
    this.ownerName = theOwnerGroup == null ? null : theOwnerGroup.getName();
    return this;
  }

  /**
   * if this is a group attribute, this is the foreign key
   * @param theLeftFactorGroup
   * @return this for chaining
   */
  public CompositeSave assignLeftFactorGroup(Group theLeftFactorGroup) {
    this.leftFactorGroup = theLeftFactorGroup;
    this.leftFactorGroupId = theLeftFactorGroup == null ? null : theLeftFactorGroup.getId();
    this.leftFactorName = theLeftFactorGroup == null ? null : theLeftFactorGroup.getName();
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
    this.rightFactorName = theRightFactorGroupName;
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
    this.rightFactorName = theRightFactorGroup == null ? null : theRightFactorGroup.getName();
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
    this.ownerName = theOwnerGroupName;
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
   * @return the composite that was updated or created or deleted
   */
  public Composite save() throws AttributeDefNameNotFoundException, InsufficientPrivilegeException, StemNotFoundException, GroupNotFoundException {

    //default to insert or update
    saveMode = (SaveMode)ObjectUtils.defaultIfNull(saveMode, SaveMode.INSERT_OR_UPDATE);

    if (saveMode != SaveMode.DELETE) {
      GrouperUtil.assertion(!StringUtils.isBlank(this.type), "type is required");
    }
    
    if (this.grouperSession == null) {
      this.grouperSession = GrouperSession.staticGrouperSession();
    }
    
    
    return (Composite)GrouperTransaction.callbackGrouperTransaction(new GrouperTransactionHandler() {
    
        public Object callback(GrouperTransaction grouperTransaction)
            throws GrouperDAOException {
          
          grouperTransaction.setCachingEnabled(false);
    
          return (Composite)GrouperSession.callbackGrouperSession(CompositeSave.this.grouperSession, new GrouperSessionHandler() {
            
            @Override
            public Object callback(GrouperSession grouperSession) throws GrouperSessionException {

              try {
                // figure out fields, validate them
                massageAndValidateFields();

                GrouperUtil.assertion(CompositeSave.this.ownerGroup != null, "owner is required");
                GrouperUtil.assertion(CompositeSave.this.leftFactorGroup != null, "leftFactor is required");
                GrouperUtil.assertion(CompositeSave.this.rightFactorGroup != null, "rightFactor is required");

                boolean hasComposite = ownerGroup.isHasComposite();
  
                Composite composite = hasComposite ? ownerGroup.getComposite(true) : null;
                
                if (saveMode == SaveMode.INSERT && hasComposite) {
                  throw new RuntimeException("Inserting composite but it already exists!");
                }
                if (saveMode == SaveMode.UPDATE && !hasComposite) {
                  throw new RuntimeException("Updating composite but it doesnt exist!");
                }
  
                // delete
                if (saveMode == SaveMode.DELETE) {
                  if (!hasComposite) {
                    CompositeSave.this.saveResultType = SaveResultType.NO_CHANGE;
                    return null;
                  }
                  GrouperDAOFactory.getFactory().getComposite().delete(composite);
                  CompositeSave.this.saveResultType = SaveResultType.DELETE;
                  return composite;
                }
  
                // insert
                if (!hasComposite) {
                  composite = ownerGroup.internal_addCompositeMember(GrouperSession.staticGrouperSession(), compositeType, leftFactorGroup, rightFactorGroup, CompositeSave.this.id);
                  CompositeSave.this.saveResultType = SaveResultType.INSERT;
                  return composite;
                }
                
                // it has a composite
                if (StringUtils.equals(composite.getLeftFactorUuid(), leftFactorGroup.getId())
                    && StringUtils.equals(composite.getRightFactorUuid(), rightFactorGroup.getId())
                    && composite.getType() == compositeType) {
                  CompositeSave.this.saveResultType = SaveResultType.NO_CHANGE;
                  return composite;
                }
                
                //its wrong
                ownerGroup.deleteCompositeMember();
                composite = ownerGroup.addCompositeMember(compositeType, leftFactorGroup, rightFactorGroup);
                CompositeSave.this.saveResultType = SaveResultType.UPDATE;
                return composite;
              } catch (RuntimeException re) {
                GrouperUtil.injectInException(re, "Problem saving composite: " + CompositeSave.this.ownerGroup
                    + ", left: " + CompositeSave.this.leftFactorGroup + ", " + CompositeSave.this.compositeType + ", right: " + CompositeSave.this.rightFactorGroup
                    + ", thread: " + Integer.toHexString(Thread.currentThread().hashCode()));
                
                Throwable throwable = re.getCause();
                if (throwable instanceof InsufficientPrivilegeException) {
                  throw (InsufficientPrivilegeException)throwable;
                }
                //must just be runtime
                throw re;

              }
            }
          });
          
        }
      });
  }
}
