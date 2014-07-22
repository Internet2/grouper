/**
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
 */
/*
 * @author mchyzer
 * $Id: GroupSave.java,v 1.10 2009-11-17 02:52:29 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.entity;

import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.exception.GroupAddException;
import edu.internet2.middleware.grouper.exception.GroupModifyException;
import edu.internet2.middleware.grouper.exception.GroupNotFoundException;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.exception.StemAddException;
import edu.internet2.middleware.grouper.exception.StemNotFoundException;
import edu.internet2.middleware.grouper.group.TypeOfGroup;
import edu.internet2.middleware.grouper.misc.SaveMode;
import edu.internet2.middleware.grouper.misc.SaveResultType;


/**
 * Use this class to insert or update an entity
 */
public class EntitySave {
  
  /** group save wrapped around */
  private GroupSave groupSave = null;
  
  
  
  /**
   * create a new entity save
   * @param theGrouperSession
   */
  public EntitySave(GrouperSession theGrouperSession) {
    this.groupSave = new GroupSave(theGrouperSession);
    this.groupSave.assignTypeOfGroup(TypeOfGroup.entity);
  }
  
  /**
   * group name to edit
   * @param theGroupNameToEdit
   * @return the group name to edit
   */
  public EntitySave assignEntityNameToEdit(String theGroupNameToEdit) {
    this.groupSave.assignGroupNameToEdit(theGroupNameToEdit);
    return this;
  }
  
  /** 
   * uuid
   * @param theUuid
   * @return uuid
   */
  public EntitySave assignUuid(String theUuid) {
    this.groupSave.assignUuid(theUuid);
    return this;
  }
  
  /**
   * 
   * @param theDisplayName
   * @return this for chaining
   */
  public EntitySave assignDisplayName(String theDisplayName) {
    this.groupSave.assignDisplayName(theDisplayName);
    return this;
  }
  
  /**
   * name
   * @param name1
   * @return name
   */
  public EntitySave assignName(String name1) {
    this.groupSave.assignName(name1);
    return this;
  }
  
  /**
   * display extension
   * @param theDisplayExtension
   * @return this for chaining
   */
  public EntitySave assignDisplayExtension(String theDisplayExtension) {
    this.groupSave.assignDisplayExtension(theDisplayExtension);
    return this;
  }

  /**
   * assign description
   * @param theDescription
   * @return this for chaining
   */
  public EntitySave assignDescription(String theDescription) {
    this.groupSave.assignDescription(theDescription);
    return this;
  }

  /**
   * assign save mode
   * @param theSaveMode
   * @return this for chaining
   */
  public EntitySave assignSaveMode(SaveMode theSaveMode) {
    this.groupSave.assignSaveMode(theSaveMode);
    return this;
  }

  /**
   * assign create parents if not exist
   * @param theCreateParentStemsIfNotExist
   * @return this for chaining
   */
  public EntitySave assignCreateParentStemsIfNotExist(boolean theCreateParentStemsIfNotExist) {
    this.groupSave.assignCreateParentStemsIfNotExist(theCreateParentStemsIfNotExist);
    return this;
  }

  /**
   * get the save type
   * @return save type
   */
  public SaveResultType getSaveResultType() {
    return this.groupSave.getSaveResultType();
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
   * @throws StemAddException
   * @throws GroupModifyException
   * @throws GroupNotFoundException
   * @throws GroupAddException
   */
  public Entity save() {

    return this.groupSave.save();
    
  }
}
