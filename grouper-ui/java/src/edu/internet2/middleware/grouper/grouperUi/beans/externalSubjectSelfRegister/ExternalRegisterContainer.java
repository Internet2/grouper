/*
 * @author mchyzer
 * $Id: SimpleMembershipUpdateContainer.java,v 1.4 2009-11-02 08:50:40 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.grouperUi.beans.externalSubjectSelfRegister;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.externalSubjects.ExternalSubject;
import edu.internet2.middleware.grouper.externalSubjects.ExternalSubjectAttribute;
import edu.internet2.middleware.grouper.externalSubjects.ExternalSubjectConfig;
import edu.internet2.middleware.grouper.externalSubjects.ExternalSubjectStorageController;
import edu.internet2.middleware.grouper.externalSubjects.ExternalSubjectConfig.ExternalSubjectAttributeConfigBean;
import edu.internet2.middleware.grouper.externalSubjects.ExternalSubjectConfig.ExternalSubjectConfigBean;
import edu.internet2.middleware.grouper.grouperUi.beans.json.AppState;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiResponseJs;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.ui.exceptions.NoSessionException;
import edu.internet2.middleware.grouper.ui.tags.TagUtils;
import edu.internet2.middleware.grouper.ui.util.GrouperUiUtils;
import edu.internet2.middleware.subject.Subject;



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
   * if this record exists in the DB then it is an update.  Else it is an insert
   * @return if this is an insert or update
   */
  public boolean isInsert() {
    
    if (this.insert == null) {
    
      final String identifier = this.getUserLoggedInIdentifier();
      
      GrouperSession grouperSession = GrouperSession.startRootSession();
      try {
        ExternalSubject externalSubject = ExternalSubjectStorageController.findByIdentifier(identifier, false, null);
        
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

    GrouperSession.callbackGrouperSession(grouperSession, new GrouperSessionHandler() {

      /**
       * 
       */
      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        
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
          externalSubject = ExternalSubjectStorageController.findByIdentifier(identifier, false, null);
          
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
    GrouperSession.stopQuietly(grouperSession);
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

  /** cache the default group once we determine it is ok to use */
  private Group defaultGroup = null;
  
  /**
   * if there is a group passed in the URL, make sure it is ok for security, and return it
   * @return the group
   */
  public Group getDefaultGroup() {
    if (this.defaultGroup == null) {
      AppState appState = AppState.retrieveFromRequest();
  
      //lets see if there is an invite id
      final String groupId = appState.getUrlArgObjectOrParam("groupId");
      final String groupName = appState.getUrlArgObjectOrParam("groupName");
      
      //if nothing was passed in
      if (StringUtils.isBlank(groupId) && StringUtils.isBlank(groupName)) {
        return null;
      }
      
      if (!StringUtils.isBlank(groupId) && !StringUtils.isBlank(groupName)) {
        throw new RuntimeException("Dont pass in groupId and groupName");
      }
      
      final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
      
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
      GrouperSession grouperSession = null;
    
      Group group = null;
      
      try {
        grouperSession = GrouperSession.start(loggedInSubject);
        
        if (!StringUtils.isBlank(groupId)) {
          group = GroupFinder.findByUuid(grouperSession, groupId, false);
        }
        if (!StringUtils.isBlank(groupName)) {
          group = GroupFinder.findByName(grouperSession, groupName, false);
        }
        if (group == null) {
          String errorMessage = TagUtils.navResourceString("inviteExternalSubjects.invalidGroupUuid");
          errorMessage = StringUtils.replace(errorMessage, "{0}", GrouperUiUtils.escapeHtml(StringUtils.defaultString(groupId, groupName), true));
          guiResponseJs.addAction(GuiScreenAction.newAlert(errorMessage));
          return null;
          
        }
  
      } finally {
        GrouperSession.stopQuietly(grouperSession); 
      }
      
      grouperSession = GrouperSession.startRootSession();
      try {
        
        if (!group.hasUpdate(loggedInSubject) && !group.hasAdmin(loggedInSubject)) {
          
          String errorMessage = TagUtils.navResourceString("inviteExternalSubjects.invalidGroupPrivileges");
          errorMessage = StringUtils.replace(errorMessage, "{0}", GrouperUiUtils.escapeHtml(group.getDisplayName(), true));
          guiResponseJs.addAction(GuiScreenAction.newAlert(errorMessage));
          return null;
        }
        this.defaultGroup = group;

        
      } finally {
        GrouperSession.stopQuietly(grouperSession);
      }
      
    }
    return this.defaultGroup;      
    
  }
  
  /**
   * if we should show links to the UI
   * @return
   */
  public boolean isShowLinksToUi() {
    return this.getDefaultGroup() != null;
  }
  
  /**
   * if there is a group passed in via URL, set it on the screen
   * @return the group text
   */
  public String getFirstComboDefaultText() {
    Group theDefaultGroup = this.getDefaultGroup();
    return theDefaultGroup == null ? null : theDefaultGroup.getDisplayName();
  }
  
  /**
   * if there is a group passed in via URL, set it on the screen
   * @return the group value
   */
  public String getFirstComboDefaultValue() {
    Group theDefaultGroup = this.getDefaultGroup();
    return theDefaultGroup == null ? null : theDefaultGroup.getUuid();
  }
}
