package edu.internet2.middleware.grouper.grouperUi.serviceLogic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.externalSubjects.ExternalSubject;
import edu.internet2.middleware.grouper.externalSubjects.ExternalSubjectAttribute;
import edu.internet2.middleware.grouper.externalSubjects.ExternalSubjectStorageController;
import edu.internet2.middleware.grouper.grouperUi.beans.externalSubjectSelfRegister.ExternalRegisterContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.externalSubjectSelfRegister.RegisterField;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiResponseJs;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction;
import edu.internet2.middleware.grouper.hibernate.AuditControl;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.hibernate.HibernateHandler;
import edu.internet2.middleware.grouper.hibernate.HibernateHandlerBean;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.ui.tags.TagUtils;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * logic for external subject sefl register
 * @author mchyzer
 */
public class ExternalSubjectSelfRegister {

  
  /**
   * delete the user's information
   * @param request
   * @param response
   */
  public void delete(HttpServletRequest request, HttpServletResponse response) {

    //setup the container
    final ExternalRegisterContainer externalRegisterContainer = new ExternalRegisterContainer();
    externalRegisterContainer.storeToRequest();

    final String identifier = externalRegisterContainer.getUserLoggedInIdentifier();
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    try {
    
      final ExternalSubject externalSubject = ExternalSubjectStorageController.findByIdentifier(identifier, false, null);
  
      final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      
      String key = null;
      if (externalSubject == null) {
  
        key = "inviteExternalSubjects.deleteNotFound";
        
      } else {

        ExternalSubjectStorageController.delete(externalSubject);
        key = "inviteExternalSubjects.deleteSuccess";

      }

      String message = TagUtils.navResourceString(key);
      
      //note, there is a java way to do this... hmmm
      message = StringUtils.replace(message, "{0}", identifier);
      guiResponseJs.addAction(GuiScreenAction.newAlert(message));
      
      //get a new container
      ExternalRegisterContainer externalRegisterContainer2 = new ExternalRegisterContainer();
      externalRegisterContainer2.storeToRequest();
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#bodyDiv", 
        "/WEB-INF/grouperUi/templates/externalSubjectSelfRegister/externalSubjectSelfRegister.jsp"));

    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }

  }
  
  /**
   * index page of application
   * @param request
   * @param response
   */
  public void index(HttpServletRequest request, HttpServletResponse response) {
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();

    guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#topDiv", 
        "/WEB-INF/grouperUi/templates/common/commonTopExternal.jsp"));

    guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#bodyDiv", 
        "/WEB-INF/grouperUi/templates/externalSubjectSelfRegister/index.jsp"));

  }

  /**
   * index page of application
   * @param request
   * @param response
   */
  public void externalSubjectSelfRegister(HttpServletRequest request, HttpServletResponse response) {
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();

    //setup the container
    final ExternalRegisterContainer externalRegisterContainer = new ExternalRegisterContainer();
    externalRegisterContainer.storeToRequest();

    guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#topDiv", 
        "/WEB-INF/grouperUi/templates/common/commonTopExternal.jsp"));

    guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#bodyDiv", 
        "/WEB-INF/grouperUi/templates/externalSubjectSelfRegister/externalSubjectSelfRegister.jsp"));

  }

  /**
   * submit a form request
   * @param request
   * @param response
   */
  public void submit(HttpServletRequest request, HttpServletResponse response) {
    
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    //setup the container
    final ExternalRegisterContainer externalRegisterContainer = new ExternalRegisterContainer();
    externalRegisterContainer.storeToRequest();

    final String identifier = externalRegisterContainer.getUserLoggedInIdentifier();
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    try {
    
      ExternalSubject externalSubject = ExternalSubjectStorageController.findByIdentifier(identifier, false, null);

      if (externalSubject == null) {
        externalSubject = new ExternalSubject();
        externalSubject.setIdentifier(identifier);
      }


//      ExternalSubjectConfigBean externalSubjectConfigBean = ExternalSubjectConfig.externalSubjectConfigBean();
//      externalSubjectConfigBean.isNameRequired()

      final List<RegisterField> registerFieldsFromScreen = new ArrayList<RegisterField>();
      
      for (RegisterField registerField : externalRegisterContainer.getRegisterFields()) {
        
        String paramName = registerField.getParamName();
        String paramValue = StringUtils.trimToNull(request.getParameter(paramName));
        
        //skip readonly fields
        if  (registerField.isReadonly()) {
          continue;
        }
        
        if (registerField.isRequired() && StringUtils.isBlank(paramValue)) {
          String message = TagUtils.navResourceString("externalSubjectSelfRegister.fieldRequiredError");
          
          //note there is a java way to do this... hmmmm...
          message = StringUtils.replace(message, "{0}", registerField.getLabel());
          guiResponseJs.addAction(GuiScreenAction.newAlert(message));
          return;
        }
        
        registerField.setValue(paramValue);
        registerFieldsFromScreen.add(registerField);
      }

      final ExternalSubject EXTERNAL_SUBJECT = externalSubject;
      
      final String externalSubjectInviteName = request.getParameter("externalSubjectInviteName");
      
      //all validation is done, lets store the info
      HibernateSession.callbackHibernateSession(GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_NOT_AUDIT, new HibernateHandler() {

        /**
         * 
         */
        @Override
        public Object callback(HibernateHandlerBean hibernateHandlerBean)
            throws GrouperDAOException {

          Set<ExternalSubjectAttribute> externalSubjectAttributes = EXTERNAL_SUBJECT.retrieveAttributes();
          
          //make a map for lookups
          Map<String, ExternalSubjectAttribute> externalSubjectAttributeMap = new HashMap<String, ExternalSubjectAttribute>();
          
          for (ExternalSubjectAttribute externalSubjectAttribute : externalSubjectAttributes) {
            externalSubjectAttributeMap.put(externalSubjectAttribute.getAttributeSystemName(), externalSubjectAttribute);
          }
                    
          for (RegisterField registerField : registerFieldsFromScreen) {

            if (registerField.isFieldNotAttribute()) {

              //reflection-like API
              GrouperUtil.assignSetter(EXTERNAL_SUBJECT, registerField.getSystemName(), registerField.getValue(), false);
              
            } else {
              
              //see if attribute is already there
              ExternalSubjectAttribute externalSubjectAttribute = externalSubjectAttributeMap.get(registerField.getSystemName());
              
              if (externalSubjectAttribute == null) {
                externalSubjectAttribute = new ExternalSubjectAttribute();
                externalSubjectAttribute.setAttributeSystemName(registerField.getSystemName());
                externalSubjectAttributes.add(externalSubjectAttribute);
              }

              externalSubjectAttribute.setAttributeValue(registerField.getValue());
            }
          }

          EXTERNAL_SUBJECT.store(externalSubjectAttributes, externalSubjectInviteName);
          
          return null;
        }
      });
      
      String message = TagUtils.navResourceString("externalSubjectSelfRegister.successEdited");
      
      //get a new container
      ExternalRegisterContainer externalRegisterContainer2 = new ExternalRegisterContainer();
      externalRegisterContainer2.storeToRequest();
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#bodyDiv", 
        "/WEB-INF/grouperUi/templates/externalSubjectSelfRegister/externalSubjectSelfRegister.jsp"));

      //note, there is a java way to do this... hmmm
      message = StringUtils.replace(message, "{0}", identifier);
      guiResponseJs.addAction(GuiScreenAction.newAlert(message));
      
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }
  
  /**
   * 
   * @param request
   * @param response
   */
  public void logout(HttpServletRequest request, HttpServletResponse response) {
    new Misc().logout(request, response);
  }

}
