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

import edu.internet2.middleware.grouper.Composite;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.exception.AttributeDefNameNotFoundException;
import edu.internet2.middleware.grouper.exception.GroupNotFoundException;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.exception.StemNotFoundException;
import edu.internet2.middleware.grouper.misc.CompositeType;
import edu.internet2.middleware.grouper.misc.SaveMode;


/**
 * @deprecated use edu.internet2.middleware.grouper.CompositeSave
 */
@Deprecated
public class CompositeSave {

  private edu.internet2.middleware.grouper.CompositeSave compositeSaveDelegate;
  
  /**
   * create a new composite save
   * @param theGrouperSession
   */
  public CompositeSave(GrouperSession theGrouperSession) {
    this.compositeSaveDelegate = new edu.internet2.middleware.grouper.CompositeSave(theGrouperSession);
  }
  
  /**
   * create a new composite save
   * @param theGrouperSession
   */
  public CompositeSave() {
    this.compositeSaveDelegate = new edu.internet2.middleware.grouper.CompositeSave();
  }

  /**
   * assign save mode
   * @param theSaveMode
   * @return this for chaining
   */
  public CompositeSave assignSaveMode(SaveMode theSaveMode) {
    this.compositeSaveDelegate.assignSaveMode(theSaveMode);
    return this;
  }
  
  /**
   * id of this composite
   * @param theId
   * @return this for chaining
   */
  public CompositeSave assignId(String theId) {
    this.compositeSaveDelegate.assignId(theId);
    return this;
  }
  
  /**
   * this is the foreign key mutually exclusive with ownerGroupName
   * @param theOwnerGroupId
   * @return this for chaining
   */
  public CompositeSave assignOwnerGroupId(String theOwnerGroupId) {
    this.compositeSaveDelegate.assignOwnerGroupId(theOwnerGroupId);
    return this;
  }

  /**
   * left factor group name, this is the foreign key mutually exclusive with leftFactorGroupId
   * @param theLeftFactorGroupName
   * @return this for chaining
   */
  public CompositeSave assignLeftFactorGroupName(String theLeftFactorGroupName) {
    this.compositeSaveDelegate.assignLeftFactorGroupName(theLeftFactorGroupName);
    return this;
  }
  
  /**
   * owner group foreign key
   * @param theOwnerGroup
   * @return this for chaining
   */
  public CompositeSave assignOwnerGroup(Group theOwnerGroup) {
    this.compositeSaveDelegate.assignOwnerGroup(theOwnerGroup);
    return this;
  }
  
  /**
   * assign the composite type
   * @param theCompositeType
   * @return this for chaining
   */
  public CompositeSave assignCompositeType(CompositeType theCompositeType) {
    this.compositeSaveDelegate.assignCompositeType(theCompositeType);
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
  public Composite save() 
        throws AttributeDefNameNotFoundException, InsufficientPrivilegeException, StemNotFoundException, 
        GroupNotFoundException {
    return this.compositeSaveDelegate.save();

  }

  /**
   * if this is a group attribute, this is the foreign key
   * @param theLeftFactorGroup
   * @return this for chaining
   */
  public CompositeSave assignLeftFactorGroup(Group theLeftFactorGroup) {
    this.compositeSaveDelegate.assignLeftFactorGroup(theLeftFactorGroup);
    return this;
  }

  /**
   * if this is a group attribute, this is the foreign key mutually exclusive with ownerGroupName
   * @param theLeftFactorGroupId
   * @return this for chaining
   */
  public CompositeSave assignLeftFactorGroupId(String theLeftFactorGroupId) {
    this.compositeSaveDelegate.assignLeftFactorGroupId(theLeftFactorGroupId);
    return this;
  }

  /**
   * right factor group name, this is the foreign key mutually exclusive with rightFactorGroupId
   * @param theRightFactorGroupName
   * @return this for chaining
   */
  public CompositeSave assignRightFactorGroupName(String theRightFactorGroupName) {
    this.compositeSaveDelegate.assignRightFactorGroupName(theRightFactorGroupName);
    return this;
  }

  /**
   * right factor group this is the foreign key
   * @param theRightFactorGroup
   * @return this for chaining
   */
  public CompositeSave assignRightFactorGroup(Group theRightFactorGroup) {
    this.compositeSaveDelegate.assignRightFactorGroup(theRightFactorGroup);
    return this;
  }

  /**
   * right factor group id, this is the foreign key mutually exclusive with rightFactorGroupName
   * @param theRightFactorGroupId
   * @return this for chaining
   */
  public CompositeSave assignRightFactorGroupId(String theRightFactorGroupId) {
    this.compositeSaveDelegate.assignRightFactorGroupId(theRightFactorGroupId);
    return this;
  }

  /**
   * owner group name, this is the foreign key mutually exclusive with ownerGroupId
   * @param theOwnerGroupName
   * @return this for chaining
   */
  public CompositeSave assignOwnerGroupName(String theOwnerGroupName) {
    this.compositeSaveDelegate.assignOwnerGroupName(theOwnerGroupName);
    return this;
  }

  /**
   * 
   * @param theLeftFactor
   * @return this for chaining
   */
  public CompositeSave assignLeftFactorName(String theLeftFactor) {
    this.compositeSaveDelegate.assignLeftFactorGroupName(theLeftFactor);
    return this;
  }

  /**
   * name
   * @param name1
   * @return name
   */
  public CompositeSave assignOwnerName(String name1) {
    this.compositeSaveDelegate.assignOwnerGroupName(name1);
    return this;
  }

  /**
   * 
   * @param theRightFactor
   * @return this for chaining
   */
  public CompositeSave assignRightFactorName(String theRightFactor) {
    this.compositeSaveDelegate.assignRightFactorName(theRightFactor);
    return this;
  }

  /**
   * 
   * @param theType
   * @return this for chaining
   */
  public CompositeSave assignType(String theType) {
    this.compositeSaveDelegate.assignType(theType);
    return this;
  }

  /**
   * 
   * @return the composite
   */
  public Composite getComposite() {
    return this.compositeSaveDelegate.getComposite();
  }
  
}
