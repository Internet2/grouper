/*
 * @author mchyzer
 * $Id: GroupSave.java,v 1.3.2.1 2009-01-23 06:32:51 mchyzer Exp $
 */
package edu.internet2.middleware.grouper;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.exception.AttributeNotFoundException;
import edu.internet2.middleware.grouper.exception.GroupAddException;
import edu.internet2.middleware.grouper.exception.GroupModifyException;
import edu.internet2.middleware.grouper.exception.GroupNotFoundException;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.exception.StemAddException;
import edu.internet2.middleware.grouper.exception.StemNotFoundException;
import edu.internet2.middleware.grouper.hibernate.GrouperTransaction;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionHandler;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.misc.SaveMode;
import edu.internet2.middleware.grouper.misc.SaveResultType;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * Use this class to insert or update a group
 */
public class GroupSave {
  
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

  /** save mode */
  private SaveMode saveMode;

  /**
   * asssign save mode
   * @param theSaveMode
   * @return this for chaining
   */
  public GroupSave assignSaveMode(SaveMode theSaveMode) {
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
  public GroupSave assignCreateParentStemsIfNotExist(boolean theCreateParentStemsIfNotExist) {
    this.createParentStemsIfNotExist = theCreateParentStemsIfNotExist;
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
   */
  public Group saveUnchecked() {
    try {
      return this.save();
    } catch (Exception e) {
      if (e instanceof RuntimeException) {
        throw (RuntimeException)e;
      }
      throw new RuntimeException(e.getMessage(), e);
    }
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
  
    //validate
    //get the stem name
    if (!StringUtils.contains(name, ":")) {
      throw new RuntimeException("Group name must exist and must contain at least one stem name (separated by colons): '" + name + "'" );
    }

    //default to insert or update
    saveMode = (SaveMode)ObjectUtils.defaultIfNull(saveMode, SaveMode.INSERT_OR_UPDATE);
    final SaveMode SAVE_MODE = saveMode;

    try {
      //do this in a transaction
      Group group = (Group)GrouperTransaction.callbackGrouperTransaction(new GrouperTransactionHandler() {
  
        public Object callback(GrouperTransaction grouperTransaction)
            throws GrouperDAOException {

          return (Group)GrouperSession.callbackGrouperSession(GroupSave.this.grouperSession, new GrouperSessionHandler() {

              public Object callback(GrouperSession grouperSession)
                  throws GrouperSessionException {
                
                try {
                  String groupNameForError = GrouperUtil.defaultIfBlank(groupNameToEdit, GroupSave.this.name);
                  
                  int lastColonIndex = GroupSave.this.name.lastIndexOf(':');
                  boolean topLevelGroup = lastColonIndex < 0;
          
                  //empty is root stem
                  String parentStemNameNew = GrouperUtil.parentStemNameFromName(GroupSave.this.name);
                  
                  //note, this might be blank
                  String parentStemDisplayNameNew = GrouperUtil.parentStemNameFromName(displayName);
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
                    parentStem = topLevelGroup ? StemFinder.findRootStem(grouperSession) 
                        : StemFinder.findByName(grouperSession, parentStemNameNew);
                  } catch (StemNotFoundException snfe) {
                    
                    //see if we should fix this problem
                    if (GroupSave.this.createParentStemsIfNotExist) {
                      
                      //at this point the stem should be there (and is equal to currentStem), 
                      //just to be sure, query again
                      parentStem = Stem._createStemAndParentStemsIfNotExist(grouperSession, parentStemNameNew, parentStemDisplayNameNew);
                    } else {
                      throw new GrouperSessionException(new StemNotFoundException("Cant find stem: '" + parentStemNameNew 
                          + "' (from update on stem name: '" + groupNameForError + "')"));
                    }
                  }
                  
                  Group theGroup = null;
                  //see if update
                  boolean isUpdate = SAVE_MODE.isUpdate(GroupSave.this.groupNameToEdit);
          
                  if (isUpdate) {
                    String parentStemNameLookup = GrouperUtil.parentStemNameFromName(GroupSave.this.groupNameToEdit);
                    if (!StringUtils.equals(parentStemNameLookup, parentStemNameNew)) {
                      throw new GrouperSessionException(new GroupModifyException("Can't move a group.  Existing parentStem: '"
                          + parentStemNameLookup + "', new stem: '" + parentStemNameNew + "'"));
                  }    
                  try {
                      theGroup = GroupFinder.findByName(grouperSession, GroupSave.this.groupNameToEdit);
                      
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
                    saveResultType = SaveResultType.INSERT;
                    if (StringUtils.isBlank(GroupSave.this.uuid)) {
                      try {
                        //if no uuid
                        theGroup = parentStem.addChildGroup(extensionNew, theDisplayExtension);
                      } catch (GroupAddException gae) {
                        //here for debugging
                        throw new GrouperSessionException(gae);
                      }
                    } else {
                      //if uuid
                      theGroup = parentStem.internal_addChildGroup(extensionNew, theDisplayExtension, GroupSave.this.uuid);
                    }
                  } else {
                    //check if different so it doesnt make unneeded queries
                    if (!StringUtils.equals(theGroup.getExtension(), extensionNew)) {
                      GroupSave.this.saveResultType = SaveResultType.UPDATE;
                      theGroup.setExtension(extensionNew);
                      needsSave = true;
                    }
                    if (!StringUtils.equals(theGroup.getDisplayExtension(), theDisplayExtension)) {
                      GroupSave.this.saveResultType = SaveResultType.UPDATE;
                      theGroup.setDisplayExtension(theDisplayExtension);
                      needsSave = true;
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
                    if (!StringUtils.isBlank(GroupSave.this.description)) {
                      theGroup.setDescription(GroupSave.this.description);
                    } else {
                      try {
                        theGroup.deleteAttribute(GrouperConfig.ATTR_DESCRIPTION);
                      } catch (AttributeNotFoundException anfe) {
                        throw new RuntimeException(anfe);
                      }
                    }
                  }

                  //only store once
                  if (needsSave) {
                    theGroup.store();
                  }
                  
                  return theGroup;
                  //wrap checked exceptions inside unchecked, and rethrow outside
                } catch (StemNotFoundException snfe) {
                  throw new RuntimeException(snfe);
                } catch (InsufficientPrivilegeException ipe) {
                  throw new RuntimeException(ipe);
                } catch (StemAddException sae) {
                  throw new RuntimeException(sae);
                } catch (GroupModifyException gme) {
                  throw new RuntimeException(gme);
                } catch (GroupAddException gae) {
                  throw new RuntimeException(gae);
                }
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
