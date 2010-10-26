/*
 * @author mchyzer
 * $Id: SimpleMembershipUpdateContainer.java,v 1.4 2009-11-02 08:50:40 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.grouperUi.beans.externalSubjectSelfRegister;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.ui.exceptions.NoSessionException;
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

  /**
   * 
   */
  private void initFields() {
    
    
    this.registerFields = new ArrayList<RegisterField>();
    RegisterField registerField = null;
    
    {
      registerField = new RegisterField();
      registerField.setSystemName("identifier");
      
      String identifierLabel = GrouperUiUtils.message("externalSubjectSelfRegister.register.field.identifier.label");
      registerField.setLabel(identifierLabel);
      
      registerField.setFieldNotAttribute(true);
      registerField.setReadonly(true);
      registerField.setRequired(false);
      
      String identifierTooltip = GrouperUiUtils.message("externalSubjectSelfRegister.register.field.identifier.tooltip", true);
      
      registerField.setTooltip(identifierTooltip);
      
      String value = GrouperUiFilter.remoteUser(GrouperUiFilter.retrieveHttpServletRequest());
      
      registerField.setValue(value);
      this.registerFields.add(registerField);
      
    }
    
    {
      registerField = new RegisterField();
      registerField.setSystemName("name");
      registerField.setLabel("Name");
      registerField.setTooltip("Something");
      registerField.setRequired(true);
      this.registerFields.add(registerField);
      
    }
    
    {

      registerField = new RegisterField();
      registerField.setSystemName("institution");
      registerField.setRequired(true);
      registerField.setLabel("Institution");
      registerField.setTooltip("Something");
      this.registerFields.add(registerField);
    }

    {

      registerField = new RegisterField();
      registerField.setSystemName("departmentAndTitle");
      registerField.setRequired(true);
      registerField.setLabel("Department and title");
      registerField.setTooltip("Something");
      this.registerFields.add(registerField);
    }

    {

      registerField = new RegisterField();
      registerField.setSystemName("email");
      registerField.setRequired(true);
      registerField.setLabel("Email");
      registerField.setTooltip("Something");
      this.registerFields.add(registerField);
    }

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
