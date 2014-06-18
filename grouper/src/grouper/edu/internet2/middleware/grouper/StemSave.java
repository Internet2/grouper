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
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.misc.SaveMode;
import edu.internet2.middleware.grouper.misc.SaveResultType;
import edu.internet2.middleware.grouper.tableIndex.TableIndex;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * Use this class to insert or update a stem
 */
public class StemSave {
  
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
  public StemSave(GrouperSession theGrouperSession) {
    this.grouperSession = theGrouperSession;
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

  /**
   * display extension
   * @param theDisplayExtension
   * @return this for chaining
   */
  public StemSave assignDisplayExtension(String theDisplayExtension) {
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
  public StemSave assignDescription(String theDescription) {
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

    //help with incomplete entries
    if (StringUtils.isBlank(this.name)) {
      this.name = this.stemNameToEdit;
    }

    //get from uuid since could be a rename
    if (StringUtils.isBlank(this.stemNameToEdit) && !StringUtils.isBlank(this.uuid)) {
      Stem stem = StemFinder.findByUuid(GrouperSession.staticGrouperSession(), this.uuid, true);
      this.stemNameToEdit = stem.getName();
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
          return (Stem)GrouperSession.callbackGrouperSession(grouperSession, new GrouperSessionHandler() {
    
            public Object callback(GrouperSession grouperSession)
                throws GrouperSessionException {
              try {
                String stemNameForError = GrouperUtil.defaultIfBlank(stemNameToEdit, name);
                
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

                //if inserting
                if (!isUpdate) {
                  StemSave.this.saveResultType = SaveResultType.INSERT;
                  if (StringUtils.isBlank(uuid)) {
                    //if no uuid
                    theStem = parentStem.addChildStem(extensionNew, theDisplayExtension);
                  } else {
                    //if uuid
                    theStem = parentStem.internal_addChildStem(extensionNew, theDisplayExtension, uuid);
                  }
                } else {
                  //check if different so it doesnt make unneeded queries
                  if (!StringUtils.equals(theStem.getExtension(), extensionNew)) {
                    needsSave = true;
                    StemSave.this.saveResultType = SaveResultType.UPDATE;
                    theStem.setExtension(extensionNew);
                  }
                  if (!StringUtils.equals(theStem.getDisplayExtension(), theDisplayExtension)) {
                    needsSave = true;
                    StemSave.this.saveResultType = SaveResultType.UPDATE;
                    theStem.setDisplayExtension(theDisplayExtension);
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
                if (!StringUtils.equals(StringUtils.defaultString(theStem.getDescription()), 
                    StringUtils.defaultString(StringUtils.trim(StemSave.this.description)))) {
                  needsSave = true;
                  if (StemSave.this.saveResultType == SaveResultType.NO_CHANGE) {
                    StemSave.this.saveResultType = SaveResultType.UPDATE;
                  }
                  theStem.setDescription(StemSave.this.description);
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
          
          });
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
    return this;
  }
}
