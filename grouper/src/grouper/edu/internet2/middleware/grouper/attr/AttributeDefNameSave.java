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
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
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
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * Use this class to insert or update a attribute def name
 */
public class AttributeDefNameSave {
  
  /** attribute def this name is associated with */
  private AttributeDef attributeDef;
  
  /**
   * create a new attribute def name save
   * @param theGrouperSession
   * @param theAttributeDef
   */
  public AttributeDefNameSave(GrouperSession theGrouperSession, AttributeDef theAttributeDef) {
    this.grouperSession = theGrouperSession;
    this.attributeDef = theAttributeDef;
  }
  
  /** grouper session is required */
  private GrouperSession grouperSession;

  /** if updating an attribute def name, this is the attribute def name */
  private String attributeDefNameNameToEdit;
  
  /**
   * attributeDefName name to edit
   * @param theAttributeDefNameNameToEdit
   * @return the attributeDefName name to edit
   */
  public AttributeDefNameSave assignAttributeDefNameNameToEdit(String theAttributeDefNameNameToEdit) {
    this.attributeDefNameNameToEdit = theAttributeDefNameNameToEdit;
    return this;
  }
  
  /** id */
  private String id;
  
  /** 
   * id
   * @param theId
   * @return uuid
   */
  public AttributeDefNameSave assignUuid(String theId) {
    this.id = theId;
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
  public AttributeDefNameSave assignDisplayName(String theDisplayName) {
    this.displayName = theDisplayName;
    return this;
  }
  
  /**
   * name
   * @param name1
   * @return name
   */
  public AttributeDefNameSave assignName(String name1) {
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
  public AttributeDefNameSave assignDisplayExtension(String theDisplayExtension) {
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
  public AttributeDefNameSave assignDescription(String theDescription) {
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
  public AttributeDefNameSave assignSaveMode(SaveMode theSaveMode) {
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
  public AttributeDefNameSave assignCreateParentStemsIfNotExist(boolean theCreateParentStemsIfNotExist) {
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
   * create or update a attributeDefName.  Note this will not rename an attributeDefName at this time (might in future)
   * 
   * This is a static method since setters to AttributeDefName objects persist to the DB
   * 
   * Steps:
   * 
   * 1. Find the attributeDefName by attributeDefNameNameToEdit
   * 2. Internally set all the fields of the attributeDefName (no need to reset if already the same)
   * 3. Store the attributeDefName (insert or update) if needed
   * 4. Return the attributeDefName object
   * 
   * This runs in a tx so that if part of it fails the whole thing fails, and potentially the outer
   * transaction too
   * </pre>
   * @return the attributeDefName
   * @throws StemNotFoundException
   * @throws InsufficientPrivilegeException
   * @throws StemAddException
   */
  public AttributeDefName save() 
        throws StemNotFoundException, InsufficientPrivilegeException, StemAddException {
  
    //help with incomplete entries
    if (StringUtils.isBlank(this.name)) {
      this.name = this.attributeDefNameNameToEdit;
    }
    
    if (StringUtils.isBlank(this.attributeDefNameNameToEdit)) {
      this.attributeDefNameNameToEdit = this.name;
    }
    
    //validate
    //get the attributeDefName name
    if (!StringUtils.contains(this.name, ":")) {
      throw new RuntimeException("AttributeDefName name must exist and must contain at least one stem name (separated by colons): '" + name + "'" );
    }

    //default to insert or update
    saveMode = (SaveMode)ObjectUtils.defaultIfNull(saveMode, SaveMode.INSERT_OR_UPDATE);
    final SaveMode SAVE_MODE = saveMode;

    try {
      //do this in a transaction
      AttributeDefName attributeDefName = (AttributeDefName)GrouperTransaction.callbackGrouperTransaction(new GrouperTransactionHandler() {
  
        public Object callback(GrouperTransaction grouperTransaction)
            throws GrouperDAOException {
          
          grouperTransaction.setCachingEnabled(false);
          
          return (AttributeDefName)GrouperSession.callbackGrouperSession(AttributeDefNameSave.this.grouperSession, new GrouperSessionHandler() {

              public Object callback(GrouperSession grouperSession)
                  throws GrouperSessionException {
                
                String attributeDefNameNameForError = GrouperUtil.defaultIfBlank(AttributeDefNameSave.this.attributeDefNameNameToEdit, AttributeDefNameSave.this.name);
                
                int lastColonIndex = AttributeDefNameSave.this.name.lastIndexOf(':');
                boolean topLevelAttributeDefName = lastColonIndex < 0;
        
                //empty is root stem
                String parentStemNameNew = GrouperUtil.parentStemNameFromName(AttributeDefNameSave.this.name);
                
                //note, this might be blank
                String parentStemDisplayNameNew = GrouperUtil.parentStemNameFromName(displayName);
                String extensionNew = GrouperUtil.extensionFromName(AttributeDefNameSave.this.name);
                
                String displayExtensionFromDisplayNameNew = GrouperUtil.extensionFromName(AttributeDefNameSave.this.displayName);

                //figure out the display extension from the extension or the name (or both!)
                String theDisplayExtension = null;
                
                //if blank, and display name blank, then, use extension
                if (StringUtils.isBlank(AttributeDefNameSave.this.displayExtension) && StringUtils.isBlank(AttributeDefNameSave.this.displayName)) {
                  theDisplayExtension = extensionNew;
                } else if (!StringUtils.isBlank(AttributeDefNameSave.this.displayExtension) && !StringUtils.isBlank(AttributeDefNameSave.this.displayName)) {
                  //if neither blank
                  if (!StringUtils.equals(displayExtensionFromDisplayNameNew, AttributeDefNameSave.this.displayExtension)) {
                    throw new RuntimeException("The display extension '" + AttributeDefNameSave.this.displayExtension 
                        + "' is not consistent with the last part of the attributeDefName name '" 
                        + displayExtensionFromDisplayNameNew + "', display name: " + AttributeDefNameSave.this.displayName);
                  }
                  theDisplayExtension = displayExtensionFromDisplayNameNew;
                } else if (!StringUtils.isBlank(AttributeDefNameSave.this.displayExtension)) {
                  theDisplayExtension = AttributeDefNameSave.this.displayExtension;
                } else if (!StringUtils.isBlank(AttributeDefNameSave.this.displayName)) {
                  theDisplayExtension = displayExtensionFromDisplayNameNew;
                } else {
                  throw new RuntimeException("Shouldnt get here");
                }

                
                //lets find the stem
                Stem parentStem = null;
                
                try {
                  parentStem = topLevelAttributeDefName ? StemFinder.findRootStem(grouperSession) 
                      : StemFinder.findByName(grouperSession, parentStemNameNew, true);
                } catch (StemNotFoundException snfe) {
                  
                  //see if we should fix this problem
                  if (AttributeDefNameSave.this.createParentStemsIfNotExist) {
                    
                    //at this point the stem should be there (and is equal to currentStem), 
                    //just to be sure, query again
                    parentStem = Stem._createStemAndParentStemsIfNotExist(grouperSession, parentStemNameNew, parentStemDisplayNameNew);
                  } else {
                    throw new GrouperSessionException(new StemNotFoundException("Cant find stem: '" + parentStemNameNew 
                        + "' (from update on stem name: '" + attributeDefNameNameForError + "')"));
                  }
                }
                
                AttributeDefName theAttributeDefName = null;
                //see if update
                boolean isUpdate = SAVE_MODE.isUpdate(AttributeDefNameSave.this.attributeDefNameNameToEdit, AttributeDefNameSave.this.name);
        
                if (isUpdate) {
                  String parentStemNameLookup = GrouperUtil.parentStemNameFromName(AttributeDefNameSave.this.attributeDefNameNameToEdit);
                  if (!StringUtils.equals(parentStemNameLookup, parentStemNameNew)) {
                    throw new GrouperSessionException(new RuntimeException("Can't move an attributeDefName.  Existing parentStem: '"
                        + parentStemNameLookup + "', new stem: '" + parentStemNameNew + "'"));
                  }
                }    
                theAttributeDefName = GrouperDAOFactory.getFactory().getAttributeDefName().findByNameSecure(
                    AttributeDefNameSave.this.attributeDefNameNameToEdit, false, new QueryOptions().secondLevelCache(false));
                
                if (theAttributeDefName != null) {

                  //while we are here, make sure id's match if passed in
                  if (!StringUtils.isBlank(AttributeDefNameSave.this.id) && !StringUtils.equals(AttributeDefNameSave.this.id, theAttributeDefName.getId())) {
                    throw new RuntimeException("ID attributeDefName changes are not supported: new: " + AttributeDefNameSave.this.id + ", old: " 
                        + theAttributeDefName.getId() + ", " + attributeDefNameNameForError);
                  }
                  
                } else {
                  if (SAVE_MODE.equals(SaveMode.INSERT_OR_UPDATE) || SAVE_MODE.equals(SaveMode.INSERT)) {
                    isUpdate = false;
                  } else {
                    throw new RuntimeException("Cant find attributeDefName: " + attributeDefNameNameForError);
                  }
                  
                }
                //default
                AttributeDefNameSave.this.saveResultType = SaveResultType.NO_CHANGE;
                boolean needsSave = false;
                //if inserting
                if (!isUpdate) {
                  saveResultType = SaveResultType.INSERT;
                  theAttributeDefName = parentStem.internal_addChildAttributeDefName(grouperSession, 
                      AttributeDefNameSave.this.attributeDef, extensionNew, theDisplayExtension, 
                      AttributeDefNameSave.this.id, AttributeDefNameSave.this.description);
                } else {

                  //check if different so it doesnt make unneeded queries
                  if (!StringUtils.equals(theAttributeDefName.getExtension(), extensionNew)) {
                      
                    //lets just confirm that one doesnt exist
                    final String newName = GrouperUtil.parentStemNameFromName(theAttributeDefName.getName()) + ":" + extensionNew;
                    
                    AttributeDefName existingAttributeDefName = (AttributeDefName)GrouperSession.callbackGrouperSession(
                        grouperSession.internal_getRootSession(), new GrouperSessionHandler() {

                      public Object callback(GrouperSession grouperSession)
                          throws GrouperSessionException {
                        return AttributeDefNameFinder.findByName(newName, false);
                      }
                      
                    });
                    
                    if (existingAttributeDefName != null && !StringUtils.equals(theAttributeDefName.getId(), existingAttributeDefName.getId())) {
                      throw new RuntimeException("AttributeDefNameName already exists: " + newName);
                    }
                    
                    theAttributeDefName.setExtension(extensionNew);

                    AttributeDefNameSave.this.saveResultType = SaveResultType.UPDATE;
                    needsSave = true;
                  }
                  if (!StringUtils.equals(theAttributeDefName.getDisplayExtension(), theDisplayExtension)) {
                    AttributeDefNameSave.this.saveResultType = SaveResultType.UPDATE;
                    theAttributeDefName.setDisplayExtensionDb(theDisplayExtension);
                    theAttributeDefName.setDisplayNameDb(GrouperUtil.parentStemNameFromName(theAttributeDefName.getDisplayName()) + ":" + theDisplayExtension);
                    needsSave = true;
                  }
                }
                
                //now compare and put all attributes (then store if needed)
                //null throws exception? hmmm.  remove attribute if blank
                if (!StringUtils.equals(StringUtils.defaultString(theAttributeDefName.getDescription()), 
                    StringUtils.defaultString(StringUtils.trim(AttributeDefNameSave.this.description)))) {
                  needsSave = true;
                  if (AttributeDefNameSave.this.saveResultType == SaveResultType.NO_CHANGE) {
                    AttributeDefNameSave.this.saveResultType = SaveResultType.UPDATE;
                  }
                  theAttributeDefName.setDescription(AttributeDefNameSave.this.description);
                }
                if (!StringUtils.equals(StringUtils.defaultString(theAttributeDefName.getAttributeDefId()), 
                    StringUtils.defaultString(StringUtils.trim(AttributeDefNameSave.this.attributeDef.getId())))) {
                  throw new RuntimeException("Cannot change attributeDefId of an attributeDefName! " 
                      + AttributeDefNameSave.this.attributeDef.getId() + ", " + theAttributeDefName.getAttributeDefId());
                }


                //only store once
                if (needsSave) {
                  theAttributeDefName.store();
                }
                
                return theAttributeDefName;
              }
              
            });
            
        }
      });
      return attributeDefName;
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
