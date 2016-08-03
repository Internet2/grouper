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
 * $Id: ExternalSubjectSave.java,v 1.10 2009-11-17 02:52:29 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.externalSubjects;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.hibernate.GrouperTransaction;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionHandler;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.misc.SaveMode;
import edu.internet2.middleware.grouper.misc.SaveResultType;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * Use this class to insert or update a external subject
 */
public class ExternalSubjectSave {
  
  /**
   * 
   */
  private Map<String, String> attributes = new LinkedHashMap<String, String>();
  
  /**
   * assign attributes
   * @param theAttributes
   * @return this for chaining
   */
  public ExternalSubjectSave assignAttributes(Map<String, String> theAttributes) {
    this.attributes = theAttributes;
    return this;
  }

  /**
   * add an attribute assignment
   * @param name
   * @param value
   * @return this for chaining
   */
  public ExternalSubjectSave addAttribute(String name, String value) {
    this.attributes.put(name, value);
    return this;
  }
  
  /**
   * create a new external subject save
   * @param theGrouperSession
   */
  public ExternalSubjectSave(GrouperSession theGrouperSession) {
    this.grouperSession = theGrouperSession;
  }
  
  /** grouper session is required */
  private GrouperSession grouperSession;

  /** if updating an external subject, this is the identifier */
  private String identifierToEdit;
  
  /**
   * identifier to edit
   * @param theIdentifierToEdit
   * @return the identifier to edit
   */
  public ExternalSubjectSave assignIdentifierToEdit(String theIdentifierToEdit) {
    this.identifierToEdit = theIdentifierToEdit;
    return this;
  }
  
  /** uuid */
  private String uuid;
  
  /** 
   * uuid
   * @param theUuid
   * @return uuid
   */
  public ExternalSubjectSave assignUuid(String theUuid) {
    this.uuid = theUuid;
    return this;
  }

  /**
   * 
   * @param theIdentifier
   * @return this for chaining
   */
  public ExternalSubjectSave assignIdentifier(String theIdentifier) {
    this.identifier = theIdentifier;
    return this;
  }
  
  /**
   * 
   * @param theEnabled
   * @return this for chaining
   */
  public ExternalSubjectSave assignEnabled(boolean theEnabled) {
    this.enabled = theEnabled;
    return this;
  }
  
  /**
   * 
   * @param theEmail
   * @return this for chaining
   */
  public ExternalSubjectSave assignEmail(String theEmail) {
    this.email = theEmail;
    return this;
  }
  
  /**
   * name
   * @param name1
   * @return name
   */
  public ExternalSubjectSave assignName(String name1) {
    this.name = name1;
    return this;
  }
  
  /** save mode */
  private SaveMode saveMode;

  /**
   * assign save mode
   * @param theSaveMode
   * @return this for chaining
   */
  public ExternalSubjectSave assignSaveMode(SaveMode theSaveMode) {
    this.saveMode = theSaveMode;
    return this;
  }

  /** save type after the save */
  private SaveResultType saveResultType = null;

  /** email address */
  private String email;

  /** is this is currently enabled */
  private boolean enabled = true;

  /** the thing that the subject uses to login */
  private String identifier;

  /** institution where the user is from */
  private String institution;

  /** name of subject */
  private String name;

  /** comma separated vetted email addresses */
  private String vettedEmailAddresses;
  
  /**
   * 
   * @param theVettedEmailAddresses
   * @return this for chaining
   */
  public ExternalSubjectSave assignVettedEmailAddresses(String theVettedEmailAddresses) {
    this.vettedEmailAddresses = theVettedEmailAddresses;
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
   * create or update a external subject.
   * 
   * Steps:
   * 
   * 1. Find the external subject by identifierToEdit
   * 2. Internally set all the fields of the stem (no need to reset if already the same)
   * 3. Store the external subject (insert or update) if needed
   * 4. Return the external subject object
   * 
   * This runs in a tx so that if part of it fails the whole thing fails, and potentially the outer
   * transaction too
   * </pre>
   * @return the external subject
   * @throws InsufficientPrivilegeException
   */
  public ExternalSubject save() {

    //help with incomplete entries
    if (StringUtils.isBlank(this.identifier)) {
      this.identifier = this.identifierToEdit;
    }

    //get from uuid since could be a rename
    if (StringUtils.isBlank(this.identifierToEdit) && !StringUtils.isBlank(this.uuid)) {
      ExternalSubject externalSubject = HibernateSession.byObjectStatic().load(ExternalSubject.class, this.uuid);
      this.identifierToEdit = externalSubject.getUuid();
    }
    
    if (StringUtils.isBlank(this.identifierToEdit)) {
      this.identifierToEdit = this.identifier;
    }
    
    //default to insert or update
    ExternalSubjectSave.this.saveMode = (SaveMode)ObjectUtils.defaultIfNull(ExternalSubjectSave.this.saveMode, SaveMode.INSERT_OR_UPDATE);
    final SaveMode SAVE_MODE = ExternalSubjectSave.this.saveMode;

    try {
      //do this in a transaction
      ExternalSubject externalSubject = (ExternalSubject)GrouperTransaction.callbackGrouperTransaction(new GrouperTransactionHandler() {
  
        @SuppressWarnings("cast")
        @Override
        public Object callback(GrouperTransaction grouperTransaction)
            throws GrouperDAOException {
          
          grouperTransaction.setCachingEnabled(false);
          
          return (ExternalSubject)GrouperSession.callbackGrouperSession(ExternalSubjectSave.this.grouperSession, new GrouperSessionHandler() {

              @Override
              public Object callback(GrouperSession theGrouperSession)
                  throws GrouperSessionException {
                
                String identifierForError = GrouperUtil.defaultIfBlank(ExternalSubjectSave.this.identifierToEdit, ExternalSubjectSave.this.identifier);
                
                ExternalSubject theExternalSubject = null;

                //see if update
                boolean isUpdate = SAVE_MODE.isUpdate(ExternalSubjectSave.this.identifierToEdit, ExternalSubjectSave.this.identifier);

                theExternalSubject = GrouperDAOFactory.getFactory().getExternalSubject().findByIdentifier(ExternalSubjectSave.this.identifierToEdit, false, null);

                if (theExternalSubject != null) {
                  //while we are here, make sure uuid's match if passed in
                  if (!StringUtils.isBlank(ExternalSubjectSave.this.uuid) && !StringUtils.equals(ExternalSubjectSave.this.uuid, theExternalSubject.getUuid())) {
                    throw new RuntimeException("UUID external subject changes are not supported: new: " + ExternalSubjectSave.this.uuid + ", old: " 
                        + theExternalSubject.getUuid() + ", " + identifierForError);
                  }
                  
                } else {
                  
                  if (SAVE_MODE.equals(SaveMode.INSERT_OR_UPDATE) || SAVE_MODE.equals(SaveMode.INSERT)) {
                    isUpdate = false;
                  } else {
                    throw new RuntimeException("Cant update an external subject which doesnt already exist! " + ExternalSubjectSave.this.identifierToEdit);
                  }
                }

                //default
                ExternalSubjectSave.this.saveResultType = SaveResultType.NO_CHANGE;
                boolean needsSave = false;
                //if inserting
                if (!isUpdate) {

                  ExternalSubjectSave.this.saveResultType = SaveResultType.INSERT;
                  needsSave = true;
                  theExternalSubject = new ExternalSubject();
                  theExternalSubject.setIdentifier(ExternalSubjectSave.this.identifierToEdit);

                }
                
                //now compare and put all attributes (then store if needed)
                if (GrouperUtil.booleanValue(ExternalSubjectSave.this.enabled) != theExternalSubject.isEnabled()) {
                  if (ExternalSubjectSave.this.saveResultType == SaveResultType.NO_CHANGE) {
                    ExternalSubjectSave.this.saveResultType = SaveResultType.UPDATE;
                  }
                  theExternalSubject.setEnabled(ExternalSubjectSave.this.enabled);
                  needsSave = true;
                }

                if (!StringUtils.equals(StringUtils.trimToEmpty(ExternalSubjectSave.this.email), StringUtils.trimToEmpty(theExternalSubject.getEmail()))) {
                  if (ExternalSubjectSave.this.saveResultType == SaveResultType.NO_CHANGE) {
                    ExternalSubjectSave.this.saveResultType = SaveResultType.UPDATE;
                  }
                  theExternalSubject.setEmail(ExternalSubjectSave.this.email);
                  needsSave = true;
                }

                if (!StringUtils.equals(StringUtils.trimToEmpty(ExternalSubjectSave.this.institution), StringUtils.trimToEmpty(theExternalSubject.getInstitution()))) {
                  if (ExternalSubjectSave.this.saveResultType == SaveResultType.NO_CHANGE) {
                    ExternalSubjectSave.this.saveResultType = SaveResultType.UPDATE;
                  }
                  theExternalSubject.setInstitution(ExternalSubjectSave.this.institution);
                  needsSave = true;
                }

                if (!StringUtils.equals(StringUtils.trimToEmpty(ExternalSubjectSave.this.name), StringUtils.trimToEmpty(theExternalSubject.getName()))) {
                  if (ExternalSubjectSave.this.saveResultType == SaveResultType.NO_CHANGE) {
                    ExternalSubjectSave.this.saveResultType = SaveResultType.UPDATE;
                  }
                  theExternalSubject.setName(ExternalSubjectSave.this.name);
                  needsSave = true;
                }
                if (!StringUtils.equals(StringUtils.trimToEmpty(ExternalSubjectSave.this.vettedEmailAddresses), StringUtils.trimToEmpty(theExternalSubject.getVettedEmailAddresses()))) {
                  if (ExternalSubjectSave.this.saveResultType == SaveResultType.NO_CHANGE) {
                    ExternalSubjectSave.this.saveResultType = SaveResultType.UPDATE;
                  }
                  theExternalSubject.setVettedEmailAddresses(ExternalSubjectSave.this.vettedEmailAddresses);
                  needsSave = true;
                }

                Map<String, ExternalSubjectAttribute> mapNewAttributeNameToObject = new HashMap<String, ExternalSubjectAttribute>();
                Map<String, ExternalSubjectAttribute> mapOldAttributeNameToObject = new HashMap<String, ExternalSubjectAttribute>();

                if (!StringUtils.isBlank(theExternalSubject.getUuid())) {
                  Set<ExternalSubjectAttribute> setOldAttributes = new HashSet<ExternalSubjectAttribute>();
                  setOldAttributes.addAll(GrouperUtil.nonNull(theExternalSubject.retrieveAttributes()));
                  for (ExternalSubjectAttribute externalSubjectAttribute : setOldAttributes) {
                    mapOldAttributeNameToObject.put(externalSubjectAttribute.getAttributeSystemName(), externalSubjectAttribute);
                  }
                }
                for (String attributeName : GrouperUtil.nonNull(ExternalSubjectSave.this.attributes).keySet()) {
                  String newValue = ExternalSubjectSave.this.attributes.get(attributeName);
                  {
                    ExternalSubjectAttribute oldExternalSubjectAttribute = mapOldAttributeNameToObject.get(attributeName);
                    if (oldExternalSubjectAttribute != null) {
                      mapOldAttributeNameToObject.remove(attributeName);
                      mapNewAttributeNameToObject.put(attributeName, oldExternalSubjectAttribute);
                      if (StringUtils.equals(newValue, oldExternalSubjectAttribute.getAttributeValue())) {
                        continue;
                      }
                      oldExternalSubjectAttribute.setAttributeValue(newValue);
                      if (ExternalSubjectSave.this.saveResultType == SaveResultType.NO_CHANGE) {
                        ExternalSubjectSave.this.saveResultType = SaveResultType.UPDATE;
                      }
                      needsSave = true;
                      continue;
                    }
                  }
                  ExternalSubjectAttribute externalSubjectAttribute = new ExternalSubjectAttribute();
                  externalSubjectAttribute.setAttributeSystemName(attributeName);
                  externalSubjectAttribute.setAttributeValue(newValue);
                  mapNewAttributeNameToObject.put(attributeName, externalSubjectAttribute);
                  if (ExternalSubjectSave.this.saveResultType == SaveResultType.NO_CHANGE) {
                    ExternalSubjectSave.this.saveResultType = SaveResultType.UPDATE;
                  }
                  needsSave = true;
                }
                
                if (mapOldAttributeNameToObject.size() > 0) {
                  if (ExternalSubjectSave.this.saveResultType == SaveResultType.NO_CHANGE) {
                    ExternalSubjectSave.this.saveResultType = SaveResultType.UPDATE;
                  }
                  needsSave = true;
                  for (ExternalSubjectAttribute externalSubjectAttribute : mapOldAttributeNameToObject.values()) {
                    externalSubjectAttribute.setAttributeValue(null);
                    mapNewAttributeNameToObject.put(externalSubjectAttribute.getAttributeSystemName(), externalSubjectAttribute);
                  }
                }
                
                //only store once, include attributes
                if (needsSave) {
                  theExternalSubject.store(new HashSet<ExternalSubjectAttribute>(mapNewAttributeNameToObject.values()), null, true, true, false);
                }
                
			          return theExternalSubject;
              }
          });
        }
      });
      return externalSubject;
    } catch (RuntimeException re) {
      
      GrouperUtil.injectInException(re, "Problem saving externalSubject: " + this.identifier + ", thread: " + Integer.toHexString(Thread.currentThread().hashCode()));
      
      Throwable throwable = re.getCause();
//      if (throwable instanceof StemNotFoundException) {
//        throw (StemNotFoundException)throwable;
//      }
      if (throwable instanceof InsufficientPrivilegeException) {
        throw (InsufficientPrivilegeException)throwable;
      }
      //must just be runtime
      throw re;
    }

  }
}
