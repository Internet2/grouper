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

import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.exception.StemAddException;
import edu.internet2.middleware.grouper.exception.StemModifyException;
import edu.internet2.middleware.grouper.exception.StemNotFoundException;
import edu.internet2.middleware.grouper.hibernate.GrouperTransaction;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionHandler;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.misc.SaveMode;
import edu.internet2.middleware.grouper.misc.SaveResultType;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * 
 * <p> Use this class to insert or update a stem </p>
 * 
 * <p>
 * Sample call
 * <blockquote>
 * <pre>
 * StemSave stemSave = new StemSave(grouperSession).assignName("test")
 *  .assignCreateParentStemsIfNotExist(true).assignDisplayName("test")
 *  .assignDescription("testDescription");
 * Stem stem = stemSave.save();
 * System.out.println(stemSave.getSaveResultType()); // DELETE, INSERT, NO_CHANGE, or UPDATE
 * </pre>
 * </blockquote>
 * </p>
 * 
 * <p>
 * Sample to delete
 * 
 * <blockquote>
 * <pre>
 * new StemSave(grouperSession).assignUuid(stem.getId()).assignSaveMode(SaveMode.DELETE).save();
 * </pre>
 * </blockquote>
 * </p>
 * <p>
 * To edit just one field (the description) for existing stem
 * <blockquote>
 * <pre>
 * new StemSave(grouperSession).assignUuid(stem.getId())
 *  .assignDisplayExtension("test1")
 *  .assignAlternateName("newAlternateName")
 *  .assignReplaceAllSettings(false).save();
 * </blockquote>
 * </pre>
 * </p>
 */
public class StemSave {
  
  
  /**
   * replace all existing settings. defaults to true.
   */
  private boolean replaceAllSettings = true;
  
  /**
   * replace all existing settings. defaults to true.
   * @return this for chaining
   */
  public StemSave assignReplaceAllSettings(boolean theReplaceAllSettings) {
    this.replaceAllSettings = theReplaceAllSettings;
    return this;
  }
  
  /**
   * set this to true to run as a root session
   */
  private boolean runAsRoot;
  
  /**
   * set this to true to run as a root session
   * @param runAsRoot
   * @return
   */
  public StemSave assignRunAsRoot(boolean runAsRoot) {
    this.runAsRoot = runAsRoot;
    return this;
  }
  
  /** id index */
  private Long idIndex;
  
  /**
   * assign id_index
   * @param theIdIndex
   * @return this for chaining
   */
  public StemSave assignIdIndex(Long theIdIndex) {
    this.idIndex = theIdIndex;
    return this;
  }

  /**
   * create a new stem save
   * @param theGrouperSession
   */
  public StemSave() {
    this.grouperSession = GrouperSession.staticGrouperSession();
    GrouperUtil.assertion(this.grouperSession != null || this.runAsRoot, "grouperSession cant be null or runAsRoot must be true");
  }

  /**
   * create a new stem save
   * @param theGrouperSession
   */
  public StemSave(GrouperSession theGrouperSession) {
    this.grouperSession = theGrouperSession;
    GrouperUtil.assertion(this.grouperSession != null || this.runAsRoot, "grouperSession cant be null or runAsRoot must be true");
  }
  
  /** grouper session is required */
  private GrouperSession grouperSession;

  /** if updating a stem, this is the stem name */
  private String stemNameToEdit;
  
  /**
   * stem name to edit
   * @param theStemNameToEdit
   * @return the stem name to edit
   */
  public StemSave assignStemNameToEdit(String theStemNameToEdit) {
    this.stemNameToEdit = theStemNameToEdit;
    return this;
  }
  
  /** uuid */
  private String uuid;
  
  /** 
   * uuid
   * @param theUuid
   * @return uuid
   */
  public StemSave assignUuid(String theUuid) {
    this.uuid = theUuid;
    return this;
  }
  
  /** name to change to */
  private String name;

  /**
   * name
   * @param name1
   * @return name
   */
  public StemSave assignName(String name1) {
    this.name = name1;
    return this;
  }
  
  /** display extension */
  private String displayExtension;

  private boolean displayExtensionAssigned;

  /**
   * display extension
   * @param theDisplayExtension
   * @return this for chaining
   */
  public StemSave assignDisplayExtension(String theDisplayExtension) {
    this.displayExtension = theDisplayExtension;
    this.displayExtensionAssigned = true;
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
  public StemSave assignDescription(String theDescription) {
    this.description = theDescription;
    this.descriptionAssigned = true;
    return this;
  }
  
  /** alternateName */
  private String alternateName;

  private boolean alternateNameAssigned;
  
  /**
   * assign alternateName
   * @param theAlternateName
   * @return this for chaining
   */
  public StemSave assignAlternateName(String theAlternateName) {
    this.alternateName = theAlternateName;
    this.alternateNameAssigned = true;
    return this;
  }
  
  private boolean setAlternateNameIfRename = true;
  
  /**
   * whether an alternate name should automatically be assigned if doing a rename
   * @param theSetAlternateNameIfRename
   * @return this for chaining
   */
  public StemSave assignSetAlternateNameIfRename(boolean theSetAlternateNameIfRename) {
    this.setAlternateNameIfRename = theSetAlternateNameIfRename;
    return this;
  }

  /** save mode */
  private SaveMode saveMode;

  /**
   * asssign save mode
   * @param theSaveMode
   * @return this for chaining
   */
  public StemSave assignSaveMode(SaveMode theSaveMode) {
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
  public StemSave assignCreateParentStemsIfNotExist(boolean theCreateParentStemsIfNotExist) {
    this.createParentStemsIfNotExist = theCreateParentStemsIfNotExist;
    return this;
  }

  /** save type after the save */
  private SaveResultType saveResultType = null;

  /**
   * display name, really only necessary if creating parent stems 
   */
  private String displayName;

  private boolean displayNameAssigned;
  
  /**
   * get the save type
   * @return save type
   */
  public SaveResultType getSaveResultType() {
    return this.saveResultType;
  }
  
  /**
   * <pre>
   * create or update a stem.  Note this will not move a stem at this time (might in future)
   * 
   * This is a static method since setters to Stem objects persist to the DB
   * 
   * Steps:
   * 
   * 1. Find the stem by stemNameToEdit (if not there then its an insert)
   * 2. Internally set all the fields of the stem (no need to reset if already the same)
   * 3. Store the stem (insert or update) if needed
   * 4. Return the stem object
   * 
   * This occurs in a transaction, so if a part of it fails, it rolls back, and potentially
   * rolls back outer transactions too
   * </pre>
   * @return the stem saved
   * @deprecated
   */
  @Deprecated
  public Stem saveUnchecked() {
    return this.save();
  }

  
  /**
   * <pre>
   * create or update a stem.  Note this will not move a stem at this time (might in future)
   * 
   * This is a static method since setters to Stem objects persist to the DB
   * 
   * Steps:
   * 
   * 1. Find the stem by stemNameToEdit (if not there then its an insert)
   * 2. Internally set all the fields of the stem (no need to reset if already the same)
   * 3. Store the stem (insert or update) if needed
   * 4. Return the stem object
   * 
   * This occurs in a transaction, so if a part of it fails, it rolls back, and potentially
   * rolls back outer transactions too
   * </pre>
   * @return the stem that was updated or created
   * @throws StemNotFoundException 
   * @throws InsufficientPrivilegeException 
   * @throws StemAddException 
   * @throws StemModifyException 
   */
  public Stem save() throws StemNotFoundException,  InsufficientPrivilegeException,
      StemAddException, StemModifyException {

    //get from uuid since could be a rename
    if (StringUtils.isBlank(this.stemNameToEdit) && !StringUtils.isBlank(this.uuid)) {
      Stem stem = StemFinder.findByUuid(GrouperSession.staticGrouperSession(), this.uuid, false, new QueryOptions().secondLevelCache(false));
      if (stem != null) {
        this.stemNameToEdit = stem.getName();
      }
    }
    
    //help with incomplete entries
    if (StringUtils.isBlank(this.name)) {
      this.name = this.stemNameToEdit;
    }

    if (StringUtils.isBlank(this.stemNameToEdit)) {
      this.stemNameToEdit = this.name;
    }

    //default to insert or update
    saveMode = (SaveMode)ObjectUtils.defaultIfNull(saveMode, SaveMode.INSERT_OR_UPDATE);
    final SaveMode SAVE_MODE = saveMode;
    try {
      //do this in a transaction
      Stem stem = (Stem)GrouperTransaction.callbackGrouperTransaction(new GrouperTransactionHandler() {
    
        public Object callback(GrouperTransaction grouperTransaction)
            throws GrouperDAOException {
          
          grouperTransaction.setCachingEnabled(false);
          
          GrouperSessionHandler grouperSessionHandler = new GrouperSessionHandler() {
    
            public Object callback(GrouperSession grouperSession)
                throws GrouperSessionException {
              try {
                String stemNameForError = GrouperUtil.defaultIfBlank(stemNameToEdit, name);
                
                // delete
                if (saveMode == SaveMode.DELETE) {
                  Stem stem = null;
                  if (!StringUtils.isBlank(uuid)) {
                    stem = StemFinder.findByUuid(grouperSession, uuid, false, new QueryOptions().secondLevelCache(false));
                  } else if (!StringUtils.isBlank(stemNameToEdit)) {
                    stem = StemFinder.findByName(grouperSession, stemNameToEdit, false, new QueryOptions().secondLevelCache(false));
                  } else {
                    throw new RuntimeException("Need uuid or name to delete stem!");
                  }
                  if (stem == null) {
                    StemSave.this.saveResultType = SaveResultType.NO_CHANGE;
                    return null;
                  }
                  stem.obliterate(false, false);
                  StemSave.this.saveResultType = SaveResultType.DELETE;
                  return stem;
                }

                int lastColonIndex = name.lastIndexOf(':');
                boolean topLevelStem = lastColonIndex < 0;
        
                //empty is root stem
                String parentStemNameNew = GrouperUtil.parentStemNameFromName(name);
                String parentStemDisplayNameNew = GrouperUtil.parentStemNameFromName(displayName);
                String extensionNew = GrouperUtil.extensionFromName(name);
                String displayExtensionFromDisplayNameNew = GrouperUtil.extensionFromName(StemSave.this.displayName);
                
                //figure out the display extension from the extension or the name (or both!)
                String theDisplayExtension = null;
                
                //if blank, and display name blank, then, use extension
                if (StringUtils.isBlank(StemSave.this.displayExtension) && StringUtils.isBlank(StemSave.this.displayName)) {
                  theDisplayExtension = extensionNew;
                } else if (!StringUtils.isBlank(StemSave.this.displayExtension) && !StringUtils.isBlank(StemSave.this.displayName)) {
                  //if neither blank
                  if (!StringUtils.equals(displayExtensionFromDisplayNameNew, StemSave.this.displayExtension)) {
                    throw new RuntimeException("The display extension '" + StemSave.this.displayExtension 
                        + "' is not consistent with the last part of the stem name '" 
                        + displayExtensionFromDisplayNameNew + "', display name: " + StemSave.this.displayName);
                  }
                  theDisplayExtension = displayExtensionFromDisplayNameNew;
                } else if (!StringUtils.isBlank(StemSave.this.displayExtension)) {
                  theDisplayExtension = StemSave.this.displayExtension;
                } else if (!StringUtils.isBlank(StemSave.this.displayName)) {
                  theDisplayExtension = displayExtensionFromDisplayNameNew;
                } else {
                  throw new RuntimeException("Shouldnt get here");
                }
                
                //lets find the stem
                Stem parentStem = null;
                
                try {
                  parentStem = topLevelStem ? StemFinder.findRootStem(grouperSession) 
                      : StemFinder.findByName(grouperSession, parentStemNameNew, true);
                } catch (StemNotFoundException snfe) {
                  
                  //see if we should fix this problem
                  if (createParentStemsIfNotExist) {
                    
                    //at this point the stem should be there (and is equal to currentStem), 
                    //just to be sure, query again
                    parentStem = Stem._createStemAndParentStemsIfNotExist(grouperSession, 
                        parentStemNameNew, parentStemDisplayNameNew);
                  } else {
                    throw new StemNotFoundException("Cant find stem: '" + parentStemNameNew 
                        + "' (from update on stem name: '" + stemNameForError + "')");
                  }
                }
                
                Stem theStem = null;
                //see if update
                boolean isUpdate = SAVE_MODE.isUpdate(stemNameToEdit, name);

                if (isUpdate) {
                  String parentStemNameLookup = GrouperUtil.parentStemNameFromName(stemNameToEdit);
                  if (!StringUtils.equals(parentStemNameLookup, parentStemNameNew)) {
                    throw new StemModifyException("Can't move a stem.  Existing parentStem: '"
                        + parentStemNameLookup + "', new stem: '" + parentStemNameNew + "'");
                }    
                try {
                    theStem = StemFinder.findByName(grouperSession, stemNameToEdit, true);
                    
                    //while we are here, make sure uuid's match if passed in
                    if (!StringUtils.isBlank(uuid) && !StringUtils.equals(uuid, theStem.getUuid())) {
                      throw new RuntimeException("UUID stem changes are not supported: new: " + uuid + ", old: " 
                          + theStem.getUuid() + ", " + stemNameForError);
                    }
                    
                  } catch (StemNotFoundException snfe) {
                    //if update we have a problem
                    if (SAVE_MODE.equals(SaveMode.INSERT_OR_UPDATE) || SAVE_MODE.equals(SaveMode.INSERT)) {
                      isUpdate = false;
                    } else {
                        throw snfe;
                    }
                  }
                }
                //default
                StemSave.this.saveResultType = SaveResultType.NO_CHANGE;
                boolean needsSave = false;

                boolean isRename = false;

                //if inserting
                if (!isUpdate) {
                  
                  if (!replaceAllSettings) {
                    throw new RuntimeException("cannot insert attribute def name with replaceAllSettings being false");
                  }
                  
                  StemSave.this.saveResultType = SaveResultType.INSERT;
                  
                  boolean failOnExists = SAVE_MODE.equals(SaveMode.INSERT);
                  
                  if (StringUtils.isBlank(StemSave.this.uuid)) {
                    //if no uuid
                    theStem = parentStem.addChildStem(extensionNew, theDisplayExtension, null, failOnExists);
                  } else {
                    //if uuid
                    theStem = parentStem.addChildStem(extensionNew, theDisplayExtension, StemSave.this.uuid, failOnExists);
                  }
                } else {
                  //check if different so it doesnt make unneeded queries
                  if (!StringUtils.equals(theStem.getExtension(), extensionNew)) {
                    needsSave = true;
                    isRename = true;
                    StemSave.this.saveResultType = SaveResultType.UPDATE;
                    theStem.setExtension(extensionNew, StemSave.this.setAlternateNameIfRename);
                  }
                  if (!StringUtils.equals(theStem.getDisplayExtension(), theDisplayExtension)) {
                    
                    if (replaceAllSettings || displayExtensionAssigned || displayNameAssigned) {
                      needsSave = true;
                      StemSave.this.saveResultType = SaveResultType.UPDATE;
                      theStem.setDisplayExtension(theDisplayExtension);
                    }
                    
                  }
                }

                if (StemSave.this.idIndex != null) {
                  if (StemSave.this.saveResultType == SaveResultType.INSERT) {

                    if (theStem.assignIdIndex(StemSave.this.idIndex)) {
                      needsSave = true;
                    }
                    
                  } else {
                    //maybe they are equal...
                    throw new RuntimeException("Cannot update idIndex for an already created stem: " + StemSave.this.idIndex + ", " + theStem.getName());
                  }
                }


                
                //now compare and put all attributes (then store if needed)
                if (!StringUtils.equals(StringUtils.defaultString(StringUtils.trim(theStem.getDescription())), 
                    StringUtils.defaultString(StringUtils.trim(StemSave.this.description)))) {
                  
                  if (replaceAllSettings || descriptionAssigned) { 
                    needsSave = true;
                    if (StemSave.this.saveResultType == SaveResultType.NO_CHANGE) {
                      StemSave.this.saveResultType = SaveResultType.UPDATE;
                    }
                    theStem.setDescription(StemSave.this.description);
                  }
                  
                }
                
                if (!isRename) {
                  if (!StringUtils.equals(StringUtils.defaultString(StringUtils.trim(theStem.getAlternateName())), 
                      StringUtils.defaultString(StringUtils.trim(StemSave.this.alternateName)))) {
                    
                    if (replaceAllSettings || alternateNameAssigned) {
                      needsSave = true;
                      if (StemSave.this.saveResultType == SaveResultType.NO_CHANGE) {
                        StemSave.this.saveResultType = SaveResultType.UPDATE;
                      }
                      if (StringUtils.isBlank(StemSave.this.alternateName)) {
                        theStem.deleteAlternateName(theStem.getAlternateName());
                      } else {
                        theStem.addAlternateName(StringUtils.trim(StemSave.this.alternateName));
                      }
                    }
                    
                  }
                }


                //only store once
                if (needsSave) {
                  theStem.store();
                }
    
                return theStem;
                //wrap checked exceptions inside unchecked, and rethrow outside
              } catch (StemNotFoundException snfe) {
                throw new GrouperSessionException(snfe);
              } catch (InsufficientPrivilegeException ipe) {
                throw new GrouperSessionException(ipe);
              } catch (StemAddException sae) {
                throw new GrouperSessionException(sae);
              } catch (StemModifyException sme) {
                throw new GrouperSessionException(sme);
              }
            }
          
          };
          
          if (runAsRoot) {
            return (Stem)GrouperSession.internal_callbackRootGrouperSession(grouperSessionHandler);
          }
          
          return (Stem)GrouperSession.callbackGrouperSession(grouperSession, grouperSessionHandler);
        }
      });
      return stem;
    } catch (GrouperSessionException gse) {
      
      Throwable throwable = gse.getCause();
      if (throwable instanceof StemNotFoundException) {
        throw (StemNotFoundException)throwable;
      }
      if (throwable instanceof InsufficientPrivilegeException) {
        throw (InsufficientPrivilegeException)throwable;
      }
      if (throwable instanceof StemAddException) {
        throw (StemAddException)throwable;
      }
      if (throwable instanceof StemModifyException) {
        throw (StemModifyException)throwable;
      }
      //must just be runtime
      throw gse;
    }
  }

  /**
   * 
   * @param theDisplayName
   * @return this for chaining
   */
  public StemSave assignDisplayName(String theDisplayName) {
    this.displayName = theDisplayName;
    this.displayNameAssigned = true;
    return this;
  }
}
