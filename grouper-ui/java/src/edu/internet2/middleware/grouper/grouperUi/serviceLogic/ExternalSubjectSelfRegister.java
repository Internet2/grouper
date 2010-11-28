package edu.internet2.middleware.grouper.grouperUi.serviceLogic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.externalSubjects.ExternalSubject;
import edu.internet2.middleware.grouper.externalSubjects.ExternalSubjectAttribute;
import edu.internet2.middleware.grouper.externalSubjects.ExternalSubjectInviteBean;
import edu.internet2.middleware.grouper.externalSubjects.ExternalSubjectStorageController;
import edu.internet2.middleware.grouper.grouperUi.beans.externalSubjectSelfRegister.ExternalRegisterContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.externalSubjectSelfRegister.RegisterField;
import edu.internet2.middleware.grouper.grouperUi.beans.json.AppState;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiResponseJs;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction;
import edu.internet2.middleware.grouper.hibernate.AuditControl;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.hibernate.HibernateHandler;
import edu.internet2.middleware.grouper.hibernate.HibernateHandlerBean;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.ui.tags.TagUtils;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * logic for external subject sefl register
 * @author mchyzer
 */
public class ExternalSubjectSelfRegister {

  /** logger */
  protected static final Log LOG = LogFactory.getLog(ExternalSubjectSelfRegister.class);

  /**
   * delete the user's information
   * @param request
   * @param response
   */
  public void delete(HttpServletRequest request, HttpServletResponse response) {

    //setup the container
    final ExternalRegisterContainer externalRegisterContainer = new ExternalRegisterContainer();
    externalRegisterContainer.storeToRequest();

    if (!this.allowedToRegister(false)) {
      return;
    }

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

    if (!this.allowedToRegister(true)) {
      return;
    }
    
    guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#bodyDiv", 
        "/WEB-INF/grouperUi/templates/externalSubjectSelfRegister/externalSubjectSelfRegister.jsp"));

  }

  /**
   * see if the user is allowed to register
   * @param displayErrorIfNotProblem if we should display an error (i.e. the first screen, not the second if editing)
   * @return true if ok, false if not, also might have a side effect of an error message
   */
  private boolean allowedToRegister(boolean displayErrorIfNotProblem) {
    
    final ExternalRegisterContainer externalRegisterContainer = ExternalRegisterContainer.retrieveFromRequest();

    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    boolean allGoodWithInvite = false;

    AppState appState = AppState.retrieveFromRequest();

    //lets see if there is an invite id
    String id = appState.getUrlArgObjectOrParam("externalSubjectInviteId");
    
    boolean hasInvite = !StringUtils.isBlank(id);
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    ExternalSubject externalSubject = null;
    ExternalSubjectInviteBean externalSubjectInviteBean = null;
    try {

      if (hasInvite) {
        
        try {
          //see if this is a valid invite
          externalSubjectInviteBean = ExternalSubjectInviteBean.findByUuid(id);
        } catch (Exception e) {
          LOG.debug("Problem finding invite: " + id, e);
        }
        if (externalSubjectInviteBean != null && !externalSubjectInviteBean.isExpired()) {
          allGoodWithInvite = true;
        }
      }
      
      //see if add or edit, if found external subject then edit, else add
      String identifier = externalRegisterContainer.getUserLoggedInIdentifier();


      externalSubject = ExternalSubjectStorageController.findByIdentifier(identifier, false, null);
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }

    //if no invite or problem with invite
    if (!allGoodWithInvite) {
      
      if (GrouperConfig.getPropertyBoolean("externalSubjects.registerRequiresInvite", false)) {
        
        String navKey = null;
        
        //if one was passed, then that is a problem
        if (hasInvite) {
          //if they already exist, they can edit, but 
          navKey = "externalSubjectSelfRegister.cantFindInviteAndCannotRegister";
        } else {
          //if its not null then it must be an edit which is allowed
          if (externalSubject == null) {
            //=Error: you cannot register without an invitation.
            navKey = "externalSubjectSelfRegister.cannotRegisterWithoutInvite";
          }
        }
        if (!StringUtils.isBlank(navKey)) {
         
          //if it is an edit, then they are allowed through, tell them they have a problem with the invite
          if (externalSubject != null) {

            //=Error: your invitation cannot be found or is expired.  You are not allowed to register without 
            //a valid invitation.  Contact the person who invited you to issue another invitation.
            String message = TagUtils.navResourceString("externalSubjectSelfRegister.cantFindInviteButCanRegister");
            if (displayErrorIfNotProblem) {
              guiResponseJs.addAction(GuiScreenAction.newAlert(message));
            }        
            
          } else {
            //if an insert, and required to have a token, then do not continue
            String message = TagUtils.navResourceString(navKey);
            
            guiResponseJs.addAction(GuiScreenAction.newAlert(message));
            return false;
          }
        }
      } else {

        if (hasInvite) {
          //if invite is not required, and there was an invite, and there was a problem, tell the user
          //=Error: your invitation cannot be found or is expired.  You can still register, though you may 
          //not have the correct role memberships.  Register and contact the person who invited you to 
          //grant role memberships if applicable.
          String message = TagUtils.navResourceString("externalSubjectSelfRegister.cantFindInviteButCanRegister");
          
          if (displayErrorIfNotProblem) {
            guiResponseJs.addAction(GuiScreenAction.newAlert(message));
          }
          
          //note, we arent required to have an id, so dont sweat it...
        }
      }
      
    }

    return true;
    
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

    if (!this.allowedToRegister(false)) {
      return;
    }
    
    final String identifier = externalRegisterContainer.getUserLoggedInIdentifier();
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    try {
    
      ExternalSubject externalSubject = ExternalSubjectStorageController.findByIdentifier(identifier, false, null);

      if (externalSubject == null) {
        externalSubject = new ExternalSubject();
        externalSubject.setIdentifier(identifier);
      }

      boolean invalidIdentifier = false;
      
      Subject subjectWhoRegistered = null;
      String message = null;
      
      try {
        externalSubject.validateIdentifier();
      } catch (Exception e) {
        
        LOG.warn("Invalid identifier: " + identifier, e);
        
        message = TagUtils.navResourceString("externalSubjectSelfRegister.invalidIdentifier");
        
        //note there is a java way to do this... hmmmm...
        message = StringUtils.replace(message, "{0}", identifier);
        guiResponseJs.addAction(GuiScreenAction.newAlert(message));
        message = null;
        invalidIdentifier = true;
        //lets see if that subject exists not as an external subject...
        try {
          subjectWhoRegistered = SubjectFinder.findByIdOrIdentifier(identifier, false);
        } catch (Exception e2) {
          LOG.warn("Problem looking for subject: " + identifier, e2);
        }
        if (subjectWhoRegistered == null) {
          return;
        }
      }

      AppState appState = AppState.retrieveFromRequest();

      if (!invalidIdentifier) {
        //ExternalSubjectConfigBean externalSubjectConfigBean = ExternalSubjectConfig.externalSubjectConfigBean();
        //externalSubjectConfigBean.isNameRequired()
  
        final List<RegisterField> registerFieldsFromScreen = new ArrayList<RegisterField>();
        
        for (RegisterField registerField : externalRegisterContainer.getRegisterFields()) {
          
          String paramName = registerField.getParamName();
          String paramValue = StringUtils.trimToNull(request.getParameter(paramName));
          
          //skip readonly fields
          if  (registerField.isReadonly()) {
            continue;
          }
          
          if (registerField.isRequired() && StringUtils.isBlank(paramValue)) {
            String theMessage = TagUtils.navResourceString("externalSubjectSelfRegister.fieldRequiredError");
            
            //note there is a java way to do this... hmmmm...
            theMessage = StringUtils.replace(theMessage, "{0}", registerField.getLabel());
            guiResponseJs.addAction(GuiScreenAction.newAlert(theMessage));
            return;
          }
          
          registerField.setValue(paramValue);
          registerFieldsFromScreen.add(registerField);
        }
        
        final ExternalSubject EXTERNAL_SUBJECT = externalSubject;
  
        final String externalSubjectInviteName = appState.getUrlArgObjectOrParam("externalSubjectInviteName");
        
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
  
        //get the subject
        subjectWhoRegistered = SubjectFinder.findByIdAndSource(externalSubject.getUuid(), ExternalSubject.sourceId(), true);
        
        message = TagUtils.navResourceString("externalSubjectSelfRegister.successEdited");
  
        //note, there is a java way to do this... hmmm
        message = StringUtils.replace(message, "{0}", identifier);
      }
      
      //lets process any invites
      //lets see if there is an invite id
      String id = appState.getUrlArgObjectOrParam("externalSubjectInviteId");
      
      boolean hasInvite = !StringUtils.isBlank(id);

      if (hasInvite) {

        ExternalSubjectInviteBean externalSubjectInviteBean = ExternalSubjectInviteBean.findByUuid(id);
        //see if this is a valid invite
        
        if (externalSubjectInviteBean == null) {
          
          message = TagUtils.navResourceString("externalSubjectSelfRegister.cantFindInvite");
          
        } else {
          
          if (!invalidIdentifier) {
            //add vetted email address
            externalSubject.addVettedEmailAddress(externalSubjectInviteBean.getEmailAddress());
          }
           
          //we found the invite
          List<ExternalSubjectInviteBean> externalSubjectInviteBeans = ExternalSubjectInviteBean.findByEmailAddress(externalSubjectInviteBean.getEmailAddress());
          
          //see if any are invalid
          final StringBuilder messageBuilder = new StringBuilder();
          Set<String> emailAddressesToNotify = new HashSet<String>();
          
          for (final ExternalSubjectInviteBean thisExternalSubjectInviteBean : externalSubjectInviteBeans) {
            
            String memberIdWhoInvited = null;
            Member memberWhoInvited = null;
            String nameWhoInvited = null;
            try {
              //lets process it
              memberIdWhoInvited = thisExternalSubjectInviteBean.getMemberId();
              memberWhoInvited = MemberFinder.findByUuid(grouperSession, memberIdWhoInvited, true);
              nameWhoInvited = memberWhoInvited.getSubject().getName();

              if (GrouperUtil.length(thisExternalSubjectInviteBean.getEmailsWhenRegistered()) > 0) {
                emailAddressesToNotify.addAll(thisExternalSubjectInviteBean.getEmailsWhenRegistered());
              }

              if (GrouperUtil.length(thisExternalSubjectInviteBean.getGroupIds()) > 0) {
                if (thisExternalSubjectInviteBean.isExpired()) {
                  //if there are groups, that is bad, if not, then dont worry...
                  String currentMessage = TagUtils.navResourceString("externalSubjectSelfRegister.invalidInvite");
                  currentMessage = StringUtils.replace(currentMessage, "{0}", nameWhoInvited);
                  messageBuilder.append(currentMessage + "<br /><br />");
                  continue;
                }
                
                //lets assign to the groups...
                String errorMessage = null;
                final Subject SUBJECT_WHO_REGISTERED = subjectWhoRegistered;
                
                GrouperSession grouperSessionInviter = GrouperSession.start(memberWhoInvited.getSubject(), false);
                try {
                  errorMessage = (String)GrouperSession.callbackGrouperSession(grouperSessionInviter, new GrouperSessionHandler() {
                    
                    @Override
                    public Object callback(GrouperSession grouperSessionInviter) throws GrouperSessionException {

                      StringBuilder errorMessageBuilder = new StringBuilder();
                      
                      StringBuilder allRoles = new StringBuilder();
                      for (String groupId : thisExternalSubjectInviteBean.getGroupIds()) {

                        //dont crash on each one, do as many as we can...
                        Group group = null;
                        String groupNameForLog = groupId;
                        try {
                          group = GroupFinder.findByUuid(grouperSessionInviter, groupId, true);
                          groupNameForLog = group.getName();
                          group.addMember(SUBJECT_WHO_REGISTERED, false);
                          if (allRoles.length() > 0) {
                            allRoles.append(", ");
                          }
                          allRoles.append(group.getDisplayExtension());
                        } catch (Exception e) {
                          errorMessageBuilder.append("Cant add member to group: " + groupNameForLog + ", " + e.getMessage());
                          LOG.error("Cant add member to group: " + groupNameForLog, e);
                        }
                      }
                      
                      String message = TagUtils.navResourceString("externalSubjectSelfRegister.inviteSuccess");
                      //note, there is a java way to do this... hmmm
                      message = StringUtils.replace(message, "{0}", identifier);
                      message = StringUtils.replace(message, "{1}", allRoles.toString());
                      messageBuilder.append(message).append("<br /><br />");

                      return errorMessageBuilder.toString();
                    }
                  });
                } finally {
                  GrouperSession.stopQuietly(grouperSessionInviter);
                }
                if (!StringUtils.isBlank(errorMessage)) {
                  throw new RuntimeException(errorMessage);
                }
                
              }
            } catch (Exception e) {
              LOG.error("Problem with external subject invite for: " + identifier + ", " + thisExternalSubjectInviteBean, e);
              String currentMessage = TagUtils.navResourceString("externalSubjectSelfRegister.invalidInvite");
              currentMessage = StringUtils.replace(currentMessage, "{0}", nameWhoInvited);
              messageBuilder.append(currentMessage + "<br /><br />");
            }
            try {
              thisExternalSubjectInviteBean.deleteFromDb();
            } catch (Exception e) {
              LOG.error("Problem with deleting invitation: " + identifier + ", " + thisExternalSubjectInviteBean, e);
            }
          }
          
          if (GrouperUtil.length(emailAddressesToNotify) > 0) {
            for (String emailAddressToNotify : emailAddressesToNotify) {
              String emailAddressOfInvitee = externalSubjectInviteBean.getEmailAddress();
              ExternalSubject.notifyWatcherAboutRegistration(identifier, emailAddressToNotify,
                  emailAddressOfInvitee);
            }
          }
          
          if (messageBuilder.length() > 0) {
            message = messageBuilder.toString();
          } else {
            message = TagUtils.navResourceString("externalSubjectSelfRegister.inviteSuccess");
            //note, there is a java way to do this... hmmm
            message = StringUtils.replace(message, "{0}", identifier);
          }
        }
      }

      //get a new container so the new data shows on screen...
      ExternalRegisterContainer externalRegisterContainer2 = new ExternalRegisterContainer();
      externalRegisterContainer2.storeToRequest();
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#bodyDiv", 
        "/WEB-INF/grouperUi/templates/externalSubjectSelfRegister/externalSubjectSelfRegister.jsp"));

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
