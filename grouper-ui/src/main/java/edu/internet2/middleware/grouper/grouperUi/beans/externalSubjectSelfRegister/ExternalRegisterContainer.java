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
 * $Id: SimpleMembershipUpdateContainer.java,v 1.4 2009-11-02 08:50:40 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.grouperUi.beans.externalSubjectSelfRegister;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.externalSubjects.ExternalSubject;
import edu.internet2.middleware.grouper.externalSubjects.ExternalSubjectAttribute;
import edu.internet2.middleware.grouper.externalSubjects.ExternalSubjectConfig;
import edu.internet2.middleware.grouper.externalSubjects.ExternalSubjectStorageController;
import edu.internet2.middleware.grouper.externalSubjects.ExternalSubjectConfig.ExternalSubjectAttributeConfigBean;
import edu.internet2.middleware.grouper.externalSubjects.ExternalSubjectConfig.ExternalSubjectConfigBean;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.ui.exceptions.NoSessionException;
import edu.internet2.middleware.grouper.ui.tags.TagUtils;
import edu.internet2.middleware.grouper.ui.util.GrouperUiConfig;
import edu.internet2.middleware.grouper.ui.util.GrouperUiUtils;



/**
 * bean for simple membership update.  holds all state for this module
 */
@SuppressWarnings("serial")
public class ExternalRegisterContainer implements Serializable {

  /**
   * 
   */
  public ExternalRegisterContainer() {
    
    this.initFields();
    
  }

  /** cache if this is an insert or not */
  private Boolean insert = null;
  
  /**
   * if should show delete button
   * @return if should show
   */
  public boolean isShowDeleteButton() {
    return !this.isInsert() && GrouperUiConfig.retrieveConfig().propertyValueBoolean("externalMembers.allowSelfDelete", false);
  }
  
  /**
   * if this record exists in the DB then it is an update.  Else it is an insert
   * @return if this is an insert or update
   */
  public boolean isInsert() {
    
    if (this.insert == null) {
    
      final String identifier = this.getUserLoggedInIdentifier();
      
      GrouperSession grouperSession = GrouperSession.startRootSession(false);
      ExternalSubject externalSubject = null;
      try {
        externalSubject = (ExternalSubject)GrouperSession.callbackGrouperSession(grouperSession, new GrouperSessionHandler() {
          
          @Override
          public Object callback(GrouperSession theGrouperSession) throws GrouperSessionException {
            return ExternalSubjectStorageController.findByIdentifier(identifier, false, null);
          }
        });
            
            //if its null then it is an insert
        this.insert = externalSubject == null;

      } finally {
          GrouperSession.stopQuietly(grouperSession);
        }
      }
    return this.insert;
  }
  
  /**
   * 
   */
  private void initFields() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession(false);
    try { 
      GrouperSession.callbackGrouperSession(grouperSession, new GrouperSessionHandler() {
  
        /**
         * 
         */
        @Override
        public Object callback(GrouperSession theGrouperSession) throws GrouperSessionException {
          
          ExternalRegisterContainer.this.registerFields = new ArrayList<RegisterField>();
          RegisterField registerField = null;
  
          ExternalSubject externalSubject = null;
          
          {
            registerField = new RegisterField();
            registerField.setSystemName("identifier");
            registerField.setParamName("param_identifier");
            String identifierLabel = GrouperUiUtils.message("externalSubjectSelfRegister.register.field.identifier.label");
            registerField.setLabel(identifierLabel);
            
            registerField.setFieldNotAttribute(true);
            registerField.setReadonly(true);
            registerField.setRequired(false);
            
            String identifierTooltip = GrouperUiUtils.message("externalSubjectSelfRegister.register.field.identifier.tooltip", true);
            
            registerField.setTooltip(identifierTooltip);
            
            String identifier = getUserLoggedInIdentifier();
            
            registerField.setValue(identifier);
            ExternalRegisterContainer.this.registerFields.add(registerField);
  
            //get the current subject so we can prepopulate data, if it exists
            externalSubject = ExternalSubjectStorageController.findByIdentifier(identifier, false, new QueryOptions().secondLevelCache(false));
            
          }
          
          ExternalSubjectConfigBean externalSubjectConfigBean = ExternalSubjectConfig.externalSubjectConfigBean();
          
          
          {
            registerField = new RegisterField();
            registerField.setSystemName("name");
            
            if (externalSubjectConfigBean.isNameRequired()) {
              registerField.setRequired(true);
            }
            registerField.setFieldNotAttribute(true);
            registerField.setParamName("param_name");
  
            String label = GrouperUiUtils.message("externalSubjectSelfRegister.register.field.name.label", true);
            String tooltip = GrouperUiUtils.message("externalSubjectSelfRegister.register.field.name.tooltip", true);
            
            registerField.setLabel(label);
            registerField.setTooltip(tooltip);
            
            if (externalSubject != null) {
              registerField.setValue(externalSubject.getName());
            }
            
            ExternalRegisterContainer.this.registerFields.add(registerField);
            
          }
          
          {
  
            if (externalSubjectConfigBean.isInstitutionEnabled()) {
              registerField = new RegisterField();
              registerField.setSystemName("institution");
              if (externalSubjectConfigBean.isInstitutionRequired()) {
                registerField.setRequired(true);
              }
              registerField.setFieldNotAttribute(true);
  
              String label = GrouperUiUtils.message("externalSubjectSelfRegister.register.field.institution.label", true);
              String tooltip = GrouperUiUtils.message("externalSubjectSelfRegister.register.field.institution.tooltip", true);
              
              registerField.setLabel(label);
              registerField.setTooltip(tooltip);
  
              registerField.setParamName("param_institution");
  
              if (externalSubject != null) {
                registerField.setValue(externalSubject.getInstitution());
              }
  
              ExternalRegisterContainer.this.registerFields.add(registerField);
            }
          }
  
  
          {
            if (externalSubjectConfigBean.isEmailEnabled()) {
              registerField = new RegisterField();
              registerField.setSystemName("email");
  
              if (externalSubjectConfigBean.isEmailRequired()) {
                registerField.setRequired(true);
              }
  
              registerField.setFieldNotAttribute(true);
  
              String label = GrouperUiUtils.message("externalSubjectSelfRegister.register.field.email.label", true);
              String tooltip = GrouperUiUtils.message("externalSubjectSelfRegister.register.field.email.tooltip", true);
              
              registerField.setLabel(label);
              registerField.setTooltip(tooltip);
              registerField.setParamName("param_email");
  
              if (externalSubject != null) {
                registerField.setValue(externalSubject.getEmail());
              }
  
              ExternalRegisterContainer.this.registerFields.add(registerField);
            }
          }
  
          for (ExternalSubjectAttributeConfigBean externalSubjectAttributeConfigBean 
              : externalSubjectConfigBean.getExternalSubjectAttributeConfigBeans()) {
  
            registerField = new RegisterField();
            registerField.setSystemName(externalSubjectAttributeConfigBean.getSystemName());
            registerField.setRequired(externalSubjectAttributeConfigBean.isRequired());
            registerField.setFieldNotAttribute(false);
  
            String label = GrouperUiUtils.message("externalSubjectSelfRegister.register.field." + registerField.getSystemName() + ".label", true);
            String tooltip = GrouperUiUtils.message("externalSubjectSelfRegister.register.field." + registerField.getSystemName() + ".tooltip", true);
  
            registerField.setLabel(label);
            registerField.setTooltip(tooltip);
  
            if (externalSubject != null) {
              ExternalSubjectAttribute externalSubjectAttribute = externalSubject.retrieveAttribute(externalSubjectAttributeConfigBean.getSystemName(), false);
              if (externalSubjectAttribute != null) {
                registerField.setValue(externalSubjectAttribute.getAttributeValue());
              }
            }
  
            registerField.setParamName("param_" + registerField.getSystemName());
            ExternalRegisterContainer.this.registerFields.add(registerField);
  
          }
  
          return null;
        }
      });
    } finally {      
        GrouperSession.stopQuietly(grouperSession);
      }
    }

  /**
   * get the identifier of the user logged in
   * @return the identifier
   */
  public String getUserLoggedInIdentifier() {
    return GrouperUiFilter.remoteUser(GrouperUiFilter.retrieveHttpServletRequest());
  }
  
  /**
   * list of fields to show on screen
   */
  private List<RegisterField> registerFields;
  
  /**
   * list of fields to show on screen
   * @return regsiter fields
   */
  public List<RegisterField> getRegisterFields() {
    return this.registerFields;
  }
  
  /**
   * store to session scope
   */
  public void storeToRequest() {
    HttpServletRequest httpServletRequest = GrouperUiFilter.retrieveHttpServletRequest();
    httpServletRequest.setAttribute("externalRegisterContainer", this);
  }

  /**
   * retrieveFromSession, cannot be null
   * @return the app state in request scope
   */
  public static ExternalRegisterContainer retrieveFromRequest() {
    HttpServletRequest httpServletRequest = GrouperUiFilter.retrieveHttpServletRequest();

    ExternalRegisterContainer externalRegisterContainer = (ExternalRegisterContainer)httpServletRequest
      .getAttribute("externalRegisterContainer");
    if (externalRegisterContainer == null) {
      throw new NoSessionException(GrouperUiUtils.message("externalSubjectSelfRegister.noContainer"));
    }
    return externalRegisterContainer;
  }
}
