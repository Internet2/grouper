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
 * $Id: GroupSave.java,v 1.10 2009-11-17 02:52:29 mchyzer Exp $
 */
package edu.internet2.middleware.grouper;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.app.loader.GrouperLoader;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
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
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.misc.SaveMode;
import edu.internet2.middleware.grouper.misc.SaveResultType;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * Use this class to insert or update a group
 * e.g.
 * group = new GroupSave(grouperSession).assignName("test:testGroup").assignCreateParentStemsIfNotExist(true).save();
 */
public class GroupSave {
  
  /**
   * if the priv admin should be different from the defaults
   */
  private Boolean privAllAdmin;
  
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
   * if the priv optin should be different from the defaults
   */
  private Boolean privAllOptin;
  
  /**
   * if the priv optout should be different from the defaults
   */
  private Boolean privAllOptout;
  
  /**
   * if the priv attr update should be different from the defaults
   */
  private Boolean privAllAttrRead;
  
  /**
   * if the priv attr update should be different from the defaults
   */
  private Boolean privAllAttrUpdate;
  
  /**
   * assign priv admin to be different than the defaults for grouperAll
   * @param thePrivAllAdmin
   * @return this for chaining
   */
  public GroupSave assignPrivAllAdmin(boolean thePrivAllAdmin) {
    this.privAllAdmin = thePrivAllAdmin;
    return this;
  }

  /**
   * assign priv view to be different than the defaults for grouperAll
   * @param thePrivAllView
   * @return this for chaining
   */
  public GroupSave assignPrivAllView(boolean thePrivAllView) {
    this.privAllView = thePrivAllView;
    return this;
  }

  /**
   * assign priv read to be different than the defaults for grouperAll
   * @param thePrivAllRead
   * @return this for chaining
   */
  public GroupSave assignPrivAllRead(boolean thePrivAllRead) {
    this.privAllRead = thePrivAllRead;
    return this;
  }

  /**
   * assign priv update to be different than the defaults for grouperAll
   * @param thePrivAllUpdate
   * @return this for chaining
   */
  public GroupSave assignPrivAllUpdate(boolean thePrivAllUpdate) {
    this.privAllUpdate = thePrivAllUpdate;
    return this;
  }

  /**
   * assign priv optin to be different than the defaults for grouperAll
   * @param thePrivAllOptin
   * @return this for chaining
   */
  public GroupSave assignPrivAllOptin(boolean thePrivAllOptin) {
    this.privAllOptin = thePrivAllOptin;
    return this;
  }

  /**
   * assign priv optout to be different than the defaults for grouperAll
   * @param thePrivAllOptout
   * @return this for chaining
   */
  public GroupSave assignPrivAllOptout(boolean thePrivAllOptout) {
    this.privAllOptout = thePrivAllOptout;
    return this;
  }

  /**
   * assign priv attr read to be different than the defaults for grouperAll
   * @param thePrivAllAttrRead
   * @return this for chaining
   */
  public GroupSave assignPrivAllAttrRead(boolean thePrivAllAttrRead) {
    this.privAllAttrRead = thePrivAllAttrRead;
    return this;
  }

  /**
   * assign priv attr update to be different than the defaults for grouperAll
   * @param thePrivAllAttrUpdate
   * @return this for chaining
   */
  public GroupSave assignPrivAllAttrUpdate(boolean thePrivAllAttrUpdate) {
    this.privAllAttrUpdate = thePrivAllAttrUpdate;
    return this;
  }


  /**
   * create a new group save
   * @param theGrouperSession
   */
  public GroupSave(GrouperSession theGrouperSession) {
    this.grouperSession = theGrouperSession;
  }
  
  /** grouper session is required */
  private GrouperSession grouperSession;

  /** if updating a group, this is the group name */
  private String groupNameToEdit;
  
  /**
   * group name to edit
   * @param theGroupNameToEdit
   * @return the group name to edit
   */
  public GroupSave assignGroupNameToEdit(String theGroupNameToEdit) {
    this.groupNameToEdit = theGroupNameToEdit;
    return this;
  }
  
  /** uuid */
  private String uuid;
  
  /** 
   * uuid
   * @param theUuid
   * @return uuid
   */
  public GroupSave assignUuid(String theUuid) {
    this.uuid = theUuid;
    return this;
  }
  
  /** name to change to */
  private String name;

  /** display name, really only necessary if creating parent stems */
  private String displayName;

  /**
   * 
   * @param theDisplayName
   * @return this for chaining
   */
  public GroupSave assignDisplayName(String theDisplayName) {
    this.displayName = theDisplayName;
    return this;
  }
  
  /**
   * name
   * @param name1
   * @return name
   */
  public GroupSave assignName(String name1) {
    this.name = name1;
    return this;
  }
  
  /** display extension */
  private String displayExtension;

  /**
   * display extension
   * @param theDisplayExtension
   * @return this for chaining
   */
  public GroupSave assignDisplayExtension(String theDisplayExtension) {
    this.displayExtension = theDisplayExtension;
    return this;
  }

  /** description */
  private String description;
  
  /**
   * assign description
   * @param theDescription
   * @return this for chaining
   */
  public GroupSave assignDescription(String theDescription) {
    this.description = theDescription;
    return this;
  }

  /** id index */
  private Long idIndex;
  
  /**
   * assign id_index
   * @param theIdIndex
   * @return this for chaining
   */
  public GroupSave assignIdIndex(Long theIdIndex) {
    this.idIndex = theIdIndex;
    return this;
  }

  /** save mode */
  private SaveMode saveMode;

  /**
   * assign save mode
   * @param theSaveMode
   * @return this for chaining
   */
  public GroupSave assignSaveMode(SaveMode theSaveMode) {
    this.saveMode = theSaveMode;
    return this;
  }

  /**
   * assign save mode
   * @param theTypeOfGroup
   * @return this for chaining
   */
  public GroupSave assignTypeOfGroup(TypeOfGroup theTypeOfGroup) {
    this.typeOfGroup = theTypeOfGroup;
    return this;
  }

  /** if create parent stems if not exist */
  private boolean createParentStemsIfNotExist;

  /**
   * assign create parents if not exist
   * @param theCreateParentStemsIfNotExist
   * @return this for chaining
   */
  public GroupSave assignCreateParentStemsIfNotExist(boolean theCreateParentStemsIfNotExist) {
    this.createParentStemsIfNotExist = theCreateParentStemsIfNotExist;
    return this;
  }

  /** save type after the save */
  private SaveResultType saveResultType = null;
  
  /** typeOfGroup */
  private TypeOfGroup typeOfGroup = null;
  
  /**
   * get the save type
   * @return save type
   */
  public SaveResultType getSaveResultType() {
    return this.saveResultType;
  }
  
  /**
   * <pre>
   * create or update a group.  Do not throw checked exceptions, wrap in unchecked
   * 
   * Note this will not rename a group at this time (might in future)
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
   * @deprecated use save() instead
   */
  @Deprecated
  public Group saveUnchecked() {
    return this.save();
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
  public Group save() 
        throws StemNotFoundException, InsufficientPrivilegeException, StemAddException, 
        GroupModifyException, GroupNotFoundException, GroupAddException {

    //help with incomplete entries
    if (StringUtils.isBlank(this.name)) {
      this.name = this.groupNameToEdit;
    }

    //get from uuid since could be a rename
    if (StringUtils.isBlank(this.groupNameToEdit) && !StringUtils.isBlank(this.uuid)) {
      Group group = GroupFinder.findByUuid(GrouperSession.staticGrouperSession(), this.uuid, true);
      this.groupNameToEdit = group.getName();
    }
    
    if (StringUtils.isBlank(this.groupNameToEdit)) {
      this.groupNameToEdit = this.name;
    }
    
    //validate
    //get the stem name
    if (!StringUtils.contains(GroupSave.this.name, ":")) {
      throw new RuntimeException("Group name must exist and must contain at least one stem name (separated by colons): '" + GroupSave.this.name + "'" );
    }

    //default to insert or update
    GroupSave.this.saveMode = (SaveMode)ObjectUtils.defaultIfNull(GroupSave.this.saveMode, SaveMode.INSERT_OR_UPDATE);
    final SaveMode SAVE_MODE = GroupSave.this.saveMode;

    try {
      //do this in a transaction
      Group group = (Group)GrouperTransaction.callbackGrouperTransaction(new GrouperTransactionHandler() {
  
        @SuppressWarnings("cast")
        @Override
        public Object callback(GrouperTransaction grouperTransaction)
            throws GrouperDAOException {
          
          grouperTransaction.setCachingEnabled(false);
          
          return (Group)GrouperSession.callbackGrouperSession(GroupSave.this.grouperSession, new GrouperSessionHandler() {

              @Override
              public Object callback(GrouperSession theGrouperSession)
                  throws GrouperSessionException {
                
                String groupNameForError = GrouperUtil.defaultIfBlank(GroupSave.this.groupNameToEdit, GroupSave.this.name);
                
                int lastColonIndex = GroupSave.this.name.lastIndexOf(':');
                boolean topLevelGroup = lastColonIndex < 0;
        
                //empty is root stem
                String parentStemNameNew = GrouperUtil.parentStemNameFromName(GroupSave.this.name);
                
                //note, this might be blank
                String parentStemDisplayNameNew = GrouperUtil.parentStemNameFromName(GroupSave.this.displayName);
                String extensionNew = GrouperUtil.extensionFromName(GroupSave.this.name);
                
                String displayExtensionFromDisplayNameNew = GrouperUtil.extensionFromName(GroupSave.this.displayName);

                //figure out the display extension from the extension or the name (or both!)
                String theDisplayExtension = null;
                
                //if blank, and display name blank, then, use extension
                if (StringUtils.isBlank(GroupSave.this.displayExtension) && StringUtils.isBlank(GroupSave.this.displayName)) {
                  theDisplayExtension = extensionNew;
                } else if (!StringUtils.isBlank(GroupSave.this.displayExtension) && !StringUtils.isBlank(GroupSave.this.displayName)) {
                  //if neither blank
                  if (!StringUtils.equals(displayExtensionFromDisplayNameNew, GroupSave.this.displayExtension)) {
                    throw new RuntimeException("The display extension '" + GroupSave.this.displayExtension 
                        + "' is not consistent with the last part of the group name '" 
                        + displayExtensionFromDisplayNameNew + "', display name: " + GroupSave.this.displayName);
                  }
                  theDisplayExtension = displayExtensionFromDisplayNameNew;
                } else if (!StringUtils.isBlank(GroupSave.this.displayExtension)) {
                  theDisplayExtension = GroupSave.this.displayExtension;
                } else if (!StringUtils.isBlank(GroupSave.this.displayName)) {
                  theDisplayExtension = displayExtensionFromDisplayNameNew;
                } else {
                  throw new RuntimeException("Shouldnt get here");
                }

                
                //lets find the stem
                Stem parentStem = null;
                
                try {
                  parentStem = topLevelGroup ? StemFinder.findRootStem(theGrouperSession) 
                      : StemFinder.findByName(theGrouperSession, parentStemNameNew, true);
                } catch (StemNotFoundException snfe) {
                  
                  //see if we should fix this problem
                  if (GroupSave.this.createParentStemsIfNotExist) {
                    
                    //at this point the stem should be there (and is equal to currentStem), 
                    //just to be sure, query again
                    if (GrouperLoader.isDryRun()) {
                      parentStem = StemFinder.findByName(theGrouperSession, parentStemNameNew, false);
                      if (parentStem == null) {
                        parentStem = new Stem();
                        parentStem.setNameDb(parentStemNameNew);
                        parentStem.setExtensionDb(GrouperUtil.extensionFromName(parentStemNameNew));
                      }
                    } else {
                      parentStem = Stem._createStemAndParentStemsIfNotExist(theGrouperSession, parentStemNameNew, parentStemDisplayNameNew);
                    }
                  } else {
                    throw new GrouperSessionException(new StemNotFoundException("Cant find stem: '" + parentStemNameNew 
                        + "' (from update on stem name: '" + groupNameForError + "')"));
                  }
                }
                
                Group theGroup = null;
                //see if update
                boolean isUpdate = SAVE_MODE.isUpdate(GroupSave.this.groupNameToEdit, GroupSave.this.name);
        
                if (isUpdate) {
                  String parentStemNameLookup = GrouperUtil.parentStemNameFromName(GroupSave.this.groupNameToEdit);
                  if (!StringUtils.equals(parentStemNameLookup, parentStemNameNew)) {
                    throw new GrouperSessionException(new GroupModifyException("Can't move a group.  Existing parentStem: '"
                        + parentStemNameLookup + "', new stem: '" + parentStemNameNew + "'"));
                }    
                try {
                    theGroup = GroupFinder.findByName(theGrouperSession, GroupSave.this.groupNameToEdit, true);
                    
                    //while we are here, make sure uuid's match if passed in
                    if (!StringUtils.isBlank(GroupSave.this.uuid) && !StringUtils.equals(GroupSave.this.uuid, theGroup.getUuid())) {
                      throw new RuntimeException("UUID group changes are not supported: new: " + GroupSave.this.uuid + ", old: " 
                          + theGroup.getUuid() + ", " + groupNameForError);
                    }
                    
                  } catch (GroupNotFoundException gnfe) {
                    if (SAVE_MODE.equals(SaveMode.INSERT_OR_UPDATE) || SAVE_MODE.equals(SaveMode.INSERT)) {
                      isUpdate = false;
                    } else {
                        throw new GrouperSessionException(gnfe);
                    }
                  }
                }
                //default
                GroupSave.this.saveResultType = SaveResultType.NO_CHANGE;
                boolean needsSave = false;
                //if inserting
                if (!isUpdate) {
                  GroupSave.this.saveResultType = SaveResultType.INSERT;
                  if (GroupSave.this.typeOfGroup == null || GroupSave.this.typeOfGroup == TypeOfGroup.group) {
                    theGroup = parentStem.internal_addChildGroup(extensionNew, theDisplayExtension, GroupSave.this.uuid);
                  } else if (GroupSave.this.typeOfGroup == TypeOfGroup.role) {
                    theGroup = (Group)parentStem.internal_addChildRole(extensionNew, theDisplayExtension, GroupSave.this.uuid);
                  } else if (GroupSave.this.typeOfGroup == TypeOfGroup.entity) {
                    theGroup = (Group)parentStem.internal_addChildEntity(extensionNew, theDisplayExtension, GroupSave.this.uuid);
                  } else {
                    throw new RuntimeException("Not expecting type of group: " + GroupSave.this.typeOfGroup);
                  }
                  
                } else {
                  //check if different so it doesnt make unneeded queries
                  if (!StringUtils.equals(theGroup.getExtension(), extensionNew)) {
                      
                      //lets just confirm that one doesnt exist
                      String newName = GrouperUtil.parentStemNameFromName(theGroup.getName()) + ":" + extensionNew;
                      
                      Group existingGroup = GroupFinder.findByName(theGrouperSession.internal_getRootSession(), newName, false);
                      
                      if (existingGroup != null && !StringUtils.equals(theGroup.getUuid(), existingGroup.getUuid())) {
                        throw new GroupModifyAlreadyExistsException("Group already exists: " + newName);
                      }
                      
                      theGroup.setExtension(extensionNew);
                    GroupSave.this.saveResultType = SaveResultType.UPDATE;
                    needsSave = true;
                  }
                  if (!StringUtils.equals(theGroup.getDisplayExtension(), theDisplayExtension)) {
                    GroupSave.this.saveResultType = SaveResultType.UPDATE;
                    theGroup.setDisplayExtension(theDisplayExtension);
                    needsSave = true;
                  }
                }

                if (GroupSave.this.idIndex != null) {
                  if (GroupSave.this.saveResultType == SaveResultType.INSERT) {
                    
                    if (theGroup.assignIdIndex(GroupSave.this.idIndex)) {
                      needsSave = true;
                    }
                    
                  } else {
                    //maybe they are equal...
                    throw new RuntimeException("Cannot update idIndex for an already created group: " + GroupSave.this.idIndex + ", " + theGroup.getName());
                  }
                }

                //now compare and put all attributes (then store if needed)
                //null throws exception? hmmm.  remove attribute if blank
                if (!StringUtils.equals(StringUtils.defaultString(theGroup.getDescription()), 
                    StringUtils.defaultString(StringUtils.trim(GroupSave.this.description)))) {
                  needsSave = true;
                  if (GroupSave.this.saveResultType == SaveResultType.NO_CHANGE) {
                    GroupSave.this.saveResultType = SaveResultType.UPDATE;
                  }
                  theGroup.setDescription(GroupSave.this.description);
                }

                //compare type of group
                if (GroupSave.this.typeOfGroup != null && GroupSave.this.typeOfGroup != theGroup.getTypeOfGroup()) {
                  needsSave = true;
                  if (GroupSave.this.saveResultType == SaveResultType.NO_CHANGE) {
                    GroupSave.this.saveResultType = SaveResultType.UPDATE;
                  }
                  theGroup.setTypeOfGroup(GroupSave.this.typeOfGroup);
                }

                //only store once
                if (needsSave) {
                  theGroup.store();
                }

                boolean changedPrivs = false;
                
                boolean adminDefaultChecked = theGroup.hasAdmin(SubjectFinder.findAllSubject());
                
                boolean updateDefaultChecked = theGroup.hasUpdate(SubjectFinder.findAllSubject());
                
                boolean readDefaultChecked = theGroup.hasRead(SubjectFinder.findAllSubject());
      
                boolean viewDefaultChecked = theGroup.hasView(SubjectFinder.findAllSubject());
      
                boolean optinDefaultChecked = theGroup.hasOptin(SubjectFinder.findAllSubject());
      
                boolean optoutDefaultChecked = theGroup.hasOptout(SubjectFinder.findAllSubject());
      
                boolean attrReadDefaultChecked = theGroup.hasGroupAttrRead(SubjectFinder.findAllSubject());
      
                boolean attrUpdateDefaultChecked = theGroup.hasGroupAttrUpdate(SubjectFinder.findAllSubject());
      
                if (GroupSave.this.privAllAdmin != null && GroupSave.this.privAllAdmin != adminDefaultChecked) {
                  if (GroupSave.this.privAllAdmin) {
                    changedPrivs = changedPrivs | theGroup.grantPriv(SubjectFinder.findAllSubject(), AccessPrivilege.ADMIN, false);
                  } else {
                    changedPrivs = changedPrivs | theGroup.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.ADMIN, false);
                  }
                }
                            
                if (GroupSave.this.privAllView != null && GroupSave.this.privAllView != viewDefaultChecked) {
                  if (GroupSave.this.privAllView) {
                    changedPrivs = changedPrivs | theGroup.grantPriv(SubjectFinder.findAllSubject(), AccessPrivilege.VIEW, false);
                  } else {
                    changedPrivs = changedPrivs | theGroup.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.VIEW, false);
                  }
                }
      
                if (GroupSave.this.privAllRead != null && GroupSave.this.privAllRead != readDefaultChecked) {
                  if (GroupSave.this.privAllRead) {
                    changedPrivs = changedPrivs | theGroup.grantPriv(SubjectFinder.findAllSubject(), AccessPrivilege.READ, false);
                  } else {
                    changedPrivs = changedPrivs | theGroup.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.READ, false);
                  }
                }
                if (GroupSave.this.privAllUpdate != null && GroupSave.this.privAllUpdate != updateDefaultChecked) {
                  if (GroupSave.this.privAllUpdate) {
                    changedPrivs = changedPrivs | theGroup.grantPriv(SubjectFinder.findAllSubject(), AccessPrivilege.UPDATE, false);
                  } else {
                    changedPrivs = changedPrivs | theGroup.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.UPDATE, false);
                  }
                }
                if (GroupSave.this.privAllOptin != null && GroupSave.this.privAllOptin != optinDefaultChecked) {
                  if (GroupSave.this.privAllOptin) {
                    changedPrivs = changedPrivs | theGroup.grantPriv(SubjectFinder.findAllSubject(), AccessPrivilege.OPTIN, false);
                  } else {
                    changedPrivs = changedPrivs | theGroup.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.OPTIN, false);
                  }
                }
                if (GroupSave.this.privAllOptout != null && GroupSave.this.privAllOptout != optoutDefaultChecked) {
                  if (GroupSave.this.privAllOptout) {
                    changedPrivs = changedPrivs | theGroup.grantPriv(SubjectFinder.findAllSubject(), AccessPrivilege.OPTOUT, false);
                  } else {
                    changedPrivs = changedPrivs | theGroup.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.OPTOUT, false);
                  }
                }
                if (GroupSave.this.privAllAttrRead != null && GroupSave.this.privAllAttrRead != attrReadDefaultChecked) {
                  if (GroupSave.this.privAllAttrRead) {
                    changedPrivs = changedPrivs | theGroup.grantPriv(SubjectFinder.findAllSubject(), AccessPrivilege.GROUP_ATTR_READ, false);
                  } else {
                    changedPrivs = changedPrivs | theGroup.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.GROUP_ATTR_READ, false);
                  }
                }
                if (GroupSave.this.privAllAttrUpdate != null && GroupSave.this.privAllAttrUpdate != attrUpdateDefaultChecked) {
                  if (GroupSave.this.privAllAttrUpdate) {
                    changedPrivs = changedPrivs | theGroup.grantPriv(SubjectFinder.findAllSubject(), AccessPrivilege.GROUP_ATTR_UPDATE, false);
                  } else {
                    changedPrivs = changedPrivs | theGroup.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.GROUP_ATTR_UPDATE, false);
                  }
                }
                
                if (changedPrivs) {
                  if (GroupSave.this.saveResultType == SaveResultType.NO_CHANGE) {
                    GroupSave.this.saveResultType = SaveResultType.UPDATE;
                  }
                }
                
			          return theGroup;
              }
          });
        }
      });
      return group;
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
}
