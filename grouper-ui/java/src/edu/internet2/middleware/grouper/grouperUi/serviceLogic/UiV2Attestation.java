package edu.internet2.middleware.grouper.grouperUi.serviceLogic;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.Stem.Scope;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.app.attestation.GrouperAttestationJob;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignable;
import edu.internet2.middleware.grouper.attr.finder.AttributeAssignValueFinder;
import edu.internet2.middleware.grouper.attr.finder.AttributeAssignValueFinder.AttributeAssignValueFinderResult;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.audit.AuditEntry;
import edu.internet2.middleware.grouper.audit.AuditFieldType;
import edu.internet2.middleware.grouper.audit.AuditTypeBuiltin;
import edu.internet2.middleware.grouper.audit.UserAuditQuery;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiPaging;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiResponseJs;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction.GuiMessageType;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiSorting;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.AttestationContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GroupContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GrouperRequestContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GuiAttestation;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GuiAuditEntry;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.TextContainer;
import edu.internet2.middleware.grouper.hibernate.AuditControl;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.hibernate.HibernateHandler;
import edu.internet2.middleware.grouper.hibernate.HibernateHandlerBean;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.ui.tags.GrouperPagingTag2;
import edu.internet2.middleware.grouper.ui.util.GrouperUiUtils;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

public class UiV2Attestation {
  
  /** logger */
  private static final Log LOG = GrouperUtil.getLog(UiV2Attestation.class);
  
  /**
   * group attestation https://spaces.internet2.edu/display/Grouper/Grouper+attestation
   * @param request
   * @param response
   */
  public void groupAttestation(HttpServletRequest request, HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    Group group = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      group = UiV2Group.retrieveGroupHelper(request, AccessPrivilege.READ).getGroup();
      
      if (group == null) {
        return;
      }
      
      final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      final Group GROUP = group;
      
      //switch over to admin so attributes work
      GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        @Override
        public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
          
          if (!checkAttributeDef(guiResponseJs)) {
            return null;
          }

          setupAttestation(GROUP);
          
          guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
              "/WEB-INF/grouperUi2/group/groupAttestation.jsp"));
          
          return null;
        }
      });
      

      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
  public static GuiAttestation retrieveGuiAttestation(final AttributeAssignable attributeAssignable) {
    
    final AttributeAssign attributeAssign = attributeAssignable.getAttributeDelegate().retrieveAssignment(null, 
        GrouperAttestationJob.retrieveAttributeDefNameValueDef(), false, false);
    
    if (attributeAssign == null) {
      return null;
    }
    
    //switch over to admin so attributes work
    Object guiAttestation = GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      
      @Override
      public GuiAttestation callback(GrouperSession grouperSession) throws GrouperSessionException {
        
        String attestationDirectAssignment = attributeAssign.getAttributeValueDelegate().retrieveValueString(
            GrouperAttestationJob.retrieveAttributeDefNameDirectAssignment().getName());
        String attestationSendEmail = attributeAssign.getAttributeValueDelegate().retrieveValueString(GrouperAttestationJob.retrieveAttributeDefNameSendEmail().getName());
        String attestationEmailAddresses = attributeAssign.getAttributeValueDelegate().retrieveValueString(GrouperAttestationJob.retrieveAttributeDefNameEmailAddresses().getName());
        String attestationDaysUntilRecertify = attributeAssign.getAttributeValueDelegate().retrieveValueString(GrouperAttestationJob.retrieveAttributeDefNameDaysUntilRecertify().getName());
        String attestationLastEmailedDate = attributeAssign.getAttributeValueDelegate().retrieveValueString(
            GrouperAttestationJob.retrieveAttributeDefNameEmailedDate().getName());
        String attestationDaysBeforeToRemind = attributeAssign.getAttributeValueDelegate().retrieveValueString(
            GrouperAttestationJob.retrieveAttributeDefNameDaysBeforeToRemind().getName());
        String attestationStemScope = attributeAssign.getAttributeValueDelegate().retrieveValueString(
            GrouperAttestationJob.retrieveAttributeDefNameStemScope().getName());
        String attestationDateCertified = attributeAssign.getAttributeValueDelegate().retrieveValueString(
            GrouperAttestationJob.retrieveAttributeDefNameDateCertified().getName());
        
        if (attributeAssignable instanceof Group) {

          String daysLeftBeforeAttestation = attributeAssign.getAttributeValueDelegate().retrieveValueString(
              GrouperAttestationJob.retrieveAttributeDefNameCalculatedDaysLeft().getName());
          if (StringUtils.isBlank(daysLeftBeforeAttestation)) {

            GrouperAttestationJob.updateCalculatedDaysUntilRecertify((Group)attributeAssignable, attributeAssign);

          }
          daysLeftBeforeAttestation = attributeAssign.getAttributeValueDelegate().retrieveValueString(
              GrouperAttestationJob.retrieveAttributeDefNameCalculatedDaysLeft().getName());
          int daysLeft = GrouperUtil.intValue(daysLeftBeforeAttestation);
          return new GuiAttestation(attributeAssignable, GrouperUtil.booleanObjectValue(attestationSendEmail), attestationEmailAddresses, attestationDaysUntilRecertify,
              attestationLastEmailedDate, attestationDaysBeforeToRemind, attestationStemScope, attestationDateCertified, GrouperUtil.booleanValue(attestationDirectAssignment, false), daysLeft);
        } else if (attributeAssignable instanceof Stem) {
          return new GuiAttestation(attributeAssignable, GrouperUtil.booleanObjectValue(attestationSendEmail), attestationEmailAddresses, attestationDaysUntilRecertify,
              attestationLastEmailedDate, attestationDaysBeforeToRemind, attestationStemScope, attestationDateCertified, GrouperUtil.booleanValue(attestationDirectAssignment, false), null);
        }
        
        return null;
      }
    });
    
    return (GuiAttestation)guiAttestation;
  }
  
//  /**
//   * @param request
//   * @param response
//   * @param group
//   */
//  private void groupAttestationHelper(HttpServletRequest request, HttpServletResponse response, Group group) {
//    
//    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
//    GrouperRequestContainer grouperRequestContainer = GrouperRequestContainer.retrieveFromRequestOrCreate();
//    
//    if (!checkAttributeDef(guiResponseJs)) {
//      return;
//    }
//
//    // group has direct attestation
//    if (group.getAttributeDelegate().hasAttribute(GrouperAttestationJob.retrieveAttributeDefNameValueDef())) {
//      
//      AttributeAssignable attributeAssignable = group.getAttributeDelegate().getAttributeOrAncestorAttribute(
//          GrouperAttestationJob.retrieveAttributeDefNameValueDef().getName(), false);
//      
//      GuiAttestation guiAttestation = retrieveGroupAttestation(attributeAssignable);
//    
//      if (guiAttestation != null) {
//        // if attribute directAttestation is set to false, we need to look at the closest parent stem with attestation attributes and populate the guiAttestation with that.
//        if (guiAttestation.getGrouperAttestationDirectAssignment() == null || !guiAttestation.getGrouperAttestationDirectAssignment()) {
//          
//          Stem parentStem = group.getParentStem();
//          if (parentStem.getAttributeDelegate().hasAttributeOrAncestorHasAttribute(GrouperAttestationJob.retrieveAttributeDefNameValueDef().getName(), false)) {
//            AttributeAssignable parentAttributeAssignable = parentStem.getAttributeDelegate().getAttributeOrAncestorAttribute(GrouperAttestationJob.retrieveAttributeDefNameValueDef().getName(), false);
//            // overwrite the blank/null values of group attributes with stem attributes;
//            
//            GuiAttestation parentGuiAttestation = retrieveGroupAttestation(parentAttributeAssignable);
//            
//            // parent has attestation attributes??
//            if (parentGuiAttestation != null) {
//              // overwrite the attribute values which are not present in the group.
//              
//              if (guiAttestation.getGrouperAttestationSendEmail() == null) {
//                guiAttestation.setGrouperAttestationSendEmail(parentGuiAttestation.getGrouperAttestationSendEmail());
//              }
//              
//              if (guiAttestation.getGrouperAttestationEmailAddresses() == null) {
//                guiAttestation.setGrouperAttestationEmailAddresses(parentGuiAttestation.getGrouperAttestationEmailAddresses());
//              }
//              
//              if (guiAttestation.getGrouperAttestationDaysUntilRecertify() == null) {
//                guiAttestation.setGrouperAttestationDaysUntilRecertify(parentGuiAttestation.getGrouperAttestationDaysUntilRecertify());
//              }
//              
//              if (guiAttestation.getGrouperAttestationDaysBeforeToRemind() == null) {
//                guiAttestation.setGrouperAttestationDaysBeforeToRemind(parentGuiAttestation.getGrouperAttestationDaysBeforeToRemind());
//              }
//              
//            }
//            
//          }
//          
//        }
//        grouperRequestContainer.getAttestationContainer().setGuiAttestation(guiAttestation);
//        
//      } else {
//        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
//            TextContainer.retrieveFromRequest().getText().get("attestationAttributeAssignedError")));
//        return;
//      }
//      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#groupAttestation",
//          "/WEB-INF/grouperUi2/group/groupAttestationView.jsp"));
//      
//    } else if (group.getAttributeDelegate().hasAttributeOrAncestorHasAttribute(GrouperAttestationJob.retrieveAttributeDefNameValueDef().getName(), false)) {
//      
//      AttributeAssignable attributeAssignable = group.getAttributeDelegate().getAttributeOrAncestorAttribute(GrouperAttestationJob.retrieveAttributeDefNameValueDef().getName(), false);
//      
//      GuiAttestation guiAttestation = retrieveGroupAttestation(attributeAssignable);
//    
//      if (guiAttestation != null) {
//        grouperRequestContainer.getAttestationContainer().setGuiAttestation(guiAttestation);
//      } else {
//        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
//            TextContainer.retrieveFromRequest().getText().get("attestationAttributeAssignedError")));
//        return;
//      }
//      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#groupAttestation",
//          "/WEB-INF/grouperUi2/group/groupAttestationView.jsp"));
//      
//    }
//    
//  }
  
  
  
  /**
   * @param request
   * @param response
   */
  public void attestGroupFromOutside(HttpServletRequest request, HttpServletResponse response) {
    
    attestGroupHelper(request, false);
    
    new UiV2Group().viewGroup(request, response);
  }
  
  /**
   * @param request
   * @param response
   */
  public void attestGroup(HttpServletRequest request, HttpServletResponse response) {
    
    attestGroupHelper(request, true);
    
  }

  /**
   * 
   * @param request
   */
  public void attestGroupHelper(HttpServletRequest request, boolean drawAttestationScreen) {
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    Group group = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
        
      group = UiV2Group.retrieveGroupHelper(request, AccessPrivilege.UPDATE).getGroup();
      
      if (group == null) {
        return;
      }
      
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();

      final Group GROUP = group;
      
      AttributeAssign attributeAssign = group.getAttributeDelegate().retrieveAssignment(null, GrouperAttestationJob.retrieveAttributeDefNameValueDef(), false, false);
      if (attributeAssign == null) {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("attestationAttributeNotFoundError")));
        return;
      }
      
      //switch over to admin so attributes work
      GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        @Override
        public Object callback(GrouperSession grouperSession) throws GrouperSessionException {

          updateAttestationLastCertifiedDate(GROUP);
          
          AuditEntry auditEntry = new AuditEntry(AuditTypeBuiltin.GROUP_ATTESTATION_UPDATE_LAST_CERTIFIED_DATE, 
              "groupId", GROUP.getId(), "groupName", GROUP.getName());
          auditEntry.setDescription("Update last certified date attribute of group: " + GROUP.getName());
          attestationSaveAudit(auditEntry);

          setupAttestation(GROUP);            
          
          return null;
        }
      });

      if (drawAttestationScreen) {
        guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#groupAttestation",
            "/WEB-INF/grouperUi2/group/groupAttestationView.jsp"));
      }
      
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
          TextContainer.retrieveFromRequest().getText().get("attestationLastCertifiedUpdateSuccess")));

    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
  /**
   * @param group
   */
  private void updateAttestationLastCertifiedDate(Group group) {
    if (!group.getAttributeDelegate().hasAttributeByName(GrouperAttestationJob.retrieveAttributeDefNameValueDef().getName())) {
      group.getAttributeDelegate().assignAttribute(GrouperAttestationJob.retrieveAttributeDefNameValueDef());
    } 
    AttributeAssign attributeAssign = group.getAttributeDelegate().retrieveAssignment(null, GrouperAttestationJob.retrieveAttributeDefNameValueDef(), false, false);
    
    String date = new SimpleDateFormat("yyyy/MM/dd").format(new Date());
    attributeAssign.getAttributeValueDelegate().assignValue(GrouperAttestationJob.retrieveAttributeDefNameDateCertified().getName(), date);
    
    // add/update the directAssignment attribute
    String attestationDirectAssignment = attributeAssign.getAttributeValueDelegate().retrieveValueString(GrouperAttestationJob.retrieveAttributeDefNameDirectAssignment().getName());
    if (attestationDirectAssignment == null) {
      attributeAssign.getAttributeValueDelegate().assignValue(GrouperAttestationJob.retrieveAttributeDefNameDirectAssignment().getName(), "false");
    }
    //attributeAssign.saveOrUpdate(false);
    
    //reset number of days
    GrouperAttestationJob.updateCalculatedDaysUntilRecertify(group, attributeAssign);

  }
  
  /**
   * 
   * @param request
   * @param response
   */
  private boolean editAttestationSaveHelper(HttpServletRequest request, HttpServletResponse response, boolean isForGroup, List<GuiScreenAction> guiScreenActions) {
    
    GrouperRequestContainer grouperRequestContainer = GrouperRequestContainer.retrieveFromRequestOrCreate();

    AttestationContainer attestationContainer = grouperRequestContainer.getAttestationContainer();
    
    boolean hasError = false;

    boolean validationDone = false;
    if (!hasError) {
      String rawHasAttestation = request.getParameter("grouperAttestationHasAttestationName");
      if (!GrouperUtil.booleanValue(rawHasAttestation)) {
        validationDone = true;
      }
    }
    
    if (!validationDone && !hasError) {
      String rawRecertifyDays = request.getParameter("grouperAttestationCustomRecertifyDaysName");
      if (rawRecertifyDays != null) {
        try {
          int recertifyDays = GrouperUtil.intValue(rawRecertifyDays);
          if (recertifyDays < 1) {
            guiScreenActions.add(GuiScreenAction.newValidationMessage(GuiMessageType.error, "#grouperAttestationCustomRecertifyDaysId",
                TextContainer.retrieveFromRequest().getText().get("attestationCustomRecertifyDaysGreaterThanZero")));
            hasError = true;
          }
        } catch (Exception e) {
          guiScreenActions.add(GuiScreenAction.newValidationMessage(GuiMessageType.error, "#grouperAttestationCustomRecertifyDaysId",
              TextContainer.retrieveFromRequest().getText().get("attestationCustomRecertifyDaysNumeric")));
          hasError = true;
        }
      } else  {
        String rawUseDefault = request.getParameter("grouperAttestationDefaultCertifyName");
        if (!GrouperUtil.booleanValue(rawUseDefault)) {
          guiScreenActions.add(GuiScreenAction.newValidationMessage(GuiMessageType.error, "#grouperAttestationCustomRecertifyDaysId",
              TextContainer.retrieveFromRequest().getText().get("attestationCustomRecertifyDaysRequired")));
          hasError = true;
          
        }
      }
      
    }

    if (isForGroup) {
      
      //copy back to screen even if there is an error
      editGroupAttestationHelper(request, attestationContainer);
    } else {
      editStemAttestationHelper(request, attestationContainer);
    }
    
    if (!hasError && attestationContainer.isEditAttestationSendEmail() 
        && !attestationContainer.isEditAttestationEmailGroupManagers()
        && StringUtils.isBlank(attestationContainer.getEditAttestationEmailAddresses())) {
      
      guiScreenActions.add(GuiScreenAction.newValidationMessage(GuiMessageType.error, "#grouperAttestationEmailAddressesId", 
          TextContainer.retrieveFromRequest().getText().get("grouperAttestationEmailAddressesRequired")));
      hasError = true;
    }
    return hasError;

  }
  
  /**
   * 
   * @param request
   * @param response
   */
  public void editGroupAttestationSave(final HttpServletRequest request, final HttpServletResponse response) {

    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    try {

      grouperSession = GrouperSession.start(loggedInSubject);
        
      final Group group = UiV2Group.retrieveGroupHelper(request, AccessPrivilege.UPDATE).getGroup();
      
      if (group == null) {
        return;
      }
      
      final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      
      //switch over to admin so attributes work
      GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        @Override
        public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
          
          guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
              "/WEB-INF/grouperUi2/group/groupAttestation.jsp"));
          
          if (!checkAttributeDef(guiResponseJs)) {
            return null;
          }

          GrouperRequestContainer grouperRequestContainer = GrouperRequestContainer.retrieveFromRequestOrCreate();

          AttestationContainer attestationContainer = grouperRequestContainer.getAttestationContainer();

          List<GuiScreenAction> guiScreenActions = new ArrayList<GuiScreenAction>();

          boolean hasError = editAttestationSaveHelper(request, response, true, guiScreenActions);
          
          if (!hasError) {

            AuditEntry auditEntry = null;

            //if it was removed
            if (!attestationContainer.isEditAttestationIsAssigned()) {
              if (attestationContainer.isDirectGroupAttestationAssignment()) {

                // remove most of the attributes
                GrouperAttestationJob.removeDirectGroupAttestation(group, true);
      
                guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
                    TextContainer.retrieveFromRequest().getText().get("grouperAttestationEditRemoved")));
      
                auditEntry = new AuditEntry(AuditTypeBuiltin.GROUP_ATTESTATION_DELETE, "groupId", group.getId(), "groupName", group.getName());
                auditEntry.setDescription("Update group attestation: "+group.getName());

                attestationSaveAudit(auditEntry);

              }
            } else {
              if (!attestationContainer.isDirectGroupAttestationAssignment()) {
                group.getAttributeDelegate().assignAttribute(GrouperAttestationJob.retrieveAttributeDefNameValueDef()); // we are adding attribute here
                auditEntry = new AuditEntry(AuditTypeBuiltin.GROUP_ATTESTATION_ADD, "groupId", group.getId(), "groupName", group.getName());
                auditEntry.setDescription("Add group attestation: "+group.getName());
                
              } else {
                auditEntry = new AuditEntry(AuditTypeBuiltin.GROUP_ATTESTATION_UPDATE, "groupId", group.getId(), "groupName", group.getName());
                auditEntry.setDescription("Update group attestation: "+group.getName());
              }
              
              updateGroupAttestationAttributes(group, true, attestationContainer.isEditAttestationSendEmail(), 
                  attestationContainer.getEditAttestationEmailAddresses(), GrouperUtil.stringValue(attestationContainer.getEditAttestationCustomRecertifyDays()), 
                  null, attestationContainer.isEditAttestationResetCertifiedToToday());
              guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2Attestation.groupAttestation&groupId=" + group.getId() + "')"));
                
              attestationSaveAudit(auditEntry);
            }
          }
          
          if (!hasError) {

            guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2Attestation.groupAttestation&groupId=" + group.getId() + "')"));

            guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
                TextContainer.retrieveFromRequest().getText().get("grouperAttestationEditSaveSuccess")));

          } else {
            
            guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#groupAttestation", 
                "/WEB-INF/grouperUi2/group/groupAttestationEdit.jsp"));
            
            //add these after screen drawn
            for (GuiScreenAction guiScreenAction : guiScreenActions) {
              guiResponseJs.addAction(guiScreenAction);
            }
            
          }
          return null;
        }
      });
        

      
    } catch (RuntimeException re) {
      if (GrouperUiUtils.vetoHandle(GuiResponseJs.retrieveGuiResponseJs(), re)) {
        return;
      }
      throw re;
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }

  }
  
  /**
   * @param request
   * @param response
   */
  public void editGroupAttestation(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    Group group = null;

    try {

      grouperSession = GrouperSession.start(loggedInSubject);
        
      group = UiV2Group.retrieveGroupHelper(request, AccessPrivilege.UPDATE).getGroup();
      
      if (group == null) {
        return;
      }

      final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      
      //switch over to admin so attributes work
      GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        @Override
        public Object callback(GrouperSession grouperSession) throws GrouperSessionException {

          guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
              "/WEB-INF/grouperUi2/group/groupAttestation.jsp"));
          
          if (!checkAttributeDef(guiResponseJs)) {
            return null;
          }

          GrouperRequestContainer grouperRequestContainer = GrouperRequestContainer.retrieveFromRequestOrCreate();

          AttestationContainer attestationContainer = grouperRequestContainer.getAttestationContainer();

          attestationContainer.setEditAttestationIsAssigned(attestationContainer.isDirectGroupAttestationAssignment());
          attestationContainer.setEditAttestationSendEmail(attestationContainer.isSendEmail());
          attestationContainer.setEditAttestationEmailGroupManagers(attestationContainer.isEmailGroupManagers());
          attestationContainer.setEditAttestationEmailAddresses(attestationContainer.getEmailAddresses());
          Integer recertifyDays = attestationContainer.getRecertifyDays();
          attestationContainer.setEditAttestationDefaultCertify(recertifyDays == null);
          if (recertifyDays != null) {
            attestationContainer.setEditAttestationCustomRecertifyDays(recertifyDays);
          }
          editGroupAttestationHelper(request, attestationContainer);

          return null;
        }
      });

      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#groupAttestation", 
          "/WEB-INF/grouperUi2/group/groupAttestationEdit.jsp"));

    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }

  /**
   * 
   * @param attestationContainer
   */
  private void editAttestationHelper(HttpServletRequest request, AttestationContainer attestationContainer, boolean defaultMarkAsReviewed) {
    
    {
      Boolean isAttestationFromFormBoolean = GrouperUtil.booleanObjectValue(request.getParameter("grouperAttestationHasAttestationName"));

      if (isAttestationFromFormBoolean != null) {
        attestationContainer.setEditAttestationIsAssigned(isAttestationFromFormBoolean);
      }
    }
    
    if (attestationContainer.isEditAttestationIsAssigned()) {
      attestationContainer.setEditAttestationShowSendEmail(true);
    }

    {
      Boolean isAttestationStemScopeSubBoolean = GrouperUtil.booleanObjectValue(request.getParameter("grouperAttestationStemScopeName"));

      if (isAttestationStemScopeSubBoolean != null) {
        attestationContainer.setEditAttestationStemScopeSub(isAttestationStemScopeSubBoolean);
      } else {
        // default to true
        attestationContainer.setEditAttestationStemScopeSub(true);
      }
    }


    {
      Boolean isAttestationSendEmailBoolean = GrouperUtil.booleanObjectValue(request.getParameter("grouperAttestationSendEmailName"));

      if (isAttestationSendEmailBoolean != null) {
        attestationContainer.setEditAttestationSendEmail(isAttestationSendEmailBoolean);
      }
    }

    if (attestationContainer.isEditAttestationShowSendEmail() && attestationContainer.isEditAttestationSendEmail()) {
      attestationContainer.setEditAttestationShowEmailSettings(true);
    }
    
    {
      Boolean isAttestationEmailManagersBoolean = GrouperUtil.booleanObjectValue(request.getParameter("grouperAttestationEmailGroupManagersName"));

      if (isAttestationEmailManagersBoolean != null) {
        attestationContainer.setEditAttestationEmailGroupManagers(isAttestationEmailManagersBoolean);
      }
    }

    {
      Boolean isAttestationMarkAsReviewedBoolean = GrouperUtil.booleanObjectValue(request.getParameter("grouperAttestationMarkAsReviewedName"));

      if (isAttestationMarkAsReviewedBoolean != null) {
        attestationContainer.setEditAttestationResetCertifiedToToday(isAttestationMarkAsReviewedBoolean);
      } else {
        //default to true
        attestationContainer.setEditAttestationResetCertifiedToToday(defaultMarkAsReviewed);
      }
    }

    
    
    if (attestationContainer.isEditAttestationShowEmailSettings() && !attestationContainer.isEditAttestationEmailGroupManagers()) {
      attestationContainer.setEditAttestationShowEmailAddresses(true);
    }

    {
      String emailAddresses = request.getParameter("grouperAttestationEmailAddressesName");

      if (emailAddresses != null) {
        attestationContainer.setEditAttestationEmailAddresses(emailAddresses);
      }
    }

    {
      String defaultRecertify = request.getParameter("grouperAttestationDefaultCertifyName");

      if (defaultRecertify != null) {
        
        boolean defaultCertify = GrouperUtil.booleanValue(defaultRecertify);
        attestationContainer.setEditAttestationDefaultCertify(defaultCertify);
        
        if (!defaultCertify) {
          String customRecertifyDays = request.getParameter("grouperAttestationCustomRecertifyDaysName");
          if (customRecertifyDays != null) {
            try {
              attestationContainer.setEditAttestationCustomRecertifyDays(GrouperUtil.intValue(customRecertifyDays));
            } catch (Exception e) {
              //swallow
            }
          }
        }
      }
    }
    

  }
  
  /**
   * @param request
   * @param grouperLoaderContainer
   */
  private void editGroupAttestationHelper(HttpServletRequest request,
      AttestationContainer attestationContainer) {


    editAttestationHelper(request, attestationContainer, true);
    

  }

  /**
   * make sure attribute def is there
   * @param guiResponseJs
   * @return true if k
   */
  private boolean checkAttributeDef(GuiResponseJs guiResponseJs) {
    AttestationContainer attestationContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getAttestationContainer();
    
    AttributeDef attributeDef = null;
    try {
      
      attributeDef = GrouperAttestationJob.retrieveAttributeDef();

      //init all the attestation stuff
      attestationContainer.getGuiAttestation();
      return true;
    } catch (RuntimeException e) {
      if (attributeDef == null) {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("attestationAttributeNotFoundError")));
        return false;
      }
      throw e;
    }
  }
  
//  /**
//   * @param request
//   * @param response
//   */
//  public void editGroupAttestation2(HttpServletRequest request, HttpServletResponse response) {
//    
//    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
//    GrouperSession grouperSession = null;
//    Group group = null;
//    try {
//      grouperSession = GrouperSession.start(loggedInSubject);
//      group = UiV2Group.retrieveGroupHelper(request, AccessPrivilege.READ).getGroup();
//      if (group == null) {
//        return;
//      }
//      
//      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
//      
//      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
//          "/WEB-INF/grouperUi2/group/groupAttestation.jsp"));
//      
//      GrouperRequestContainer grouperRequestContainer = GrouperRequestContainer.retrieveFromRequestOrCreate();
//      GuiAttestation attestation = retrieveGroupAttestation(group);
//      if (attestation != null) {
//        grouperRequestContainer.getAttestationContainer().setGuiAttestation(attestation);
//        guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#groupAttestation", 
//            "/WEB-INF/grouperUi2/group/groupEditAttestation.jsp"));
//      }
//    } finally {
//      GrouperSession.stopQuietly(grouperSession);
//    }
//  }
  
//  /**
//   * edit attestation attributes
//   * @param request
//   * @param response
//   */
//  public void editGroupAttestationSubmit(HttpServletRequest request, HttpServletResponse response) {
//    
//    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
//    
//    GrouperSession grouperSession = null;
//  
//    GrouperRequestContainer grouperRequestContainer = GrouperRequestContainer.retrieveFromRequestOrCreate();
//
//    Group group = null;
//    
//    boolean error = false;
//  
//    try {
//  
//      grouperSession = GrouperSession.start(loggedInSubject);
//        
//      group = UiV2Group.retrieveGroupHelper(request, AccessPrivilege.READ).getGroup();
//      
//      if (group == null) {
//        return;
//      }
//      
//      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
//      
//      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
//          "/WEB-INF/grouperUi2/group/groupAttestation.jsp"));
//      
//      boolean sendEmail = GrouperUtil.booleanValue(request.getParameter("grouperAttestationSendEmail[]"), false);
//      //boolean updateLastCertifiedDate = GrouperUtil.booleanValue(request.getParameter("attestationUpdateLastCertified[]"), false);
//      String emailAddresses = request.getParameter("grouperAttestationEmailAddresses");
//      String daysUntilRectify = request.getParameter("grouperAttestationDaysUntilRecertify");
//      String daysBeforeReminder = request.getParameter("grouperAttestationDaysBeforeToRemind");
//
//      GuiScreenAction guiScreenActionError = null;
//
//      String daysUntilRectifyOrDefault = daysUntilRectify;
//
//      if (StringUtils.isBlank(daysUntilRectifyOrDefault)) {
//        daysUntilRectifyOrDefault = "" + GrouperConfig.retrieveConfig().propertyValueInt("attestation.default.daysUntilRecertify", 180);
//      }
//      
//      if (!error && !NumberUtils.isNumber(daysUntilRectifyOrDefault)) {
//        guiScreenActionError = GuiScreenAction.newValidationMessage(GuiMessageType.error, 
//            "#grouperAttestationDaysUntilRecertify",
//            TextContainer.retrieveFromRequest().getText().get("attestationDaysUntilRectifyValidationError"));
//        error = true;
//      }
//      
//      String daysBeforeReminderOrDefault = daysBeforeReminder;
//
//      if (StringUtils.isBlank(daysBeforeReminderOrDefault)) {
//        daysBeforeReminderOrDefault = "0";
//      }
//      
//      if (!error && !NumberUtils.isNumber(daysBeforeReminderOrDefault)) {
//        guiScreenActionError = GuiScreenAction.newValidationMessage(GuiMessageType.error, 
//            "#grouperAttestationDaysBeforeToRemind",
//            TextContainer.retrieveFromRequest().getText().get("attestationDaysBeforeReminderValidationError"));
//        error = true;
//      }
//
//      if (!checkAttributeDef(guiResponseJs)) {
//        error = true;
//      }
//      
//      if (error) {
//        
//        GuiAttestation guiAttestation = new GuiAttestation(group, GuiAttestation.Type.DIRECT);
//        
//        guiAttestation.setGrouperAttestationDaysBeforeToRemind(daysBeforeReminder);
//        guiAttestation.setGrouperAttestationDaysUntilRecertify(daysUntilRectify);
//        guiAttestation.setGrouperAttestationEmailAddresses(emailAddresses);
//        guiAttestation.setGrouperAttestationSendEmail(sendEmail);
//        
//        grouperRequestContainer.getAttestationContainer().setGuiAttestation(guiAttestation);
//        
//        guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#groupAttestation",
//            "/WEB-INF/grouperUi2/group/groupAttestationEdit.jsp"));
//
//        if (guiScreenActionError != null) {
//          guiResponseJs.addAction(guiScreenActionError);
//        }
//      }
//      
//    } finally {
//      GrouperSession.stopQuietly(grouperSession);
//    }
//    
//  }
  
  /**
   * @param group
   * @param attributeDefName
   * @param sendEmail
   * @param emailAddresses
   * @param daysUntilRectify
   * @param daysBeforeReminder
   * @param updateLastCertifiedDate
   */
  private void updateGroupAttestationAttributes(Group group, boolean directAssignment, boolean sendEmail, 
      String emailAddresses, String daysUntilRectify, String daysBeforeReminder, boolean updateLastCertifiedDate) {
    
    AttributeAssign attributeAssign = group.getAttributeDelegate().retrieveAssignment(null, GrouperAttestationJob.retrieveAttributeDefNameValueDef(), false, false);
    attributeAssign.getAttributeValueDelegate().assignValue(GrouperAttestationJob.retrieveAttributeDefNameDirectAssignment().getName(), directAssignment ? "true": "false");
    attributeAssign.getAttributeValueDelegate().assignValue(GrouperAttestationJob.retrieveAttributeDefNameSendEmail().getName(), sendEmail ? "true": "false");
    attributeAssign.getAttributeValueDelegate().assignValue(GrouperAttestationJob.retrieveAttributeDefNameEmailAddresses().getName(), emailAddresses);
    attributeAssign.getAttributeValueDelegate().assignValue(GrouperAttestationJob.retrieveAttributeDefNameDaysUntilRecertify().getName(), daysUntilRectify);
    attributeAssign.getAttributeValueDelegate().assignValue(GrouperAttestationJob.retrieveAttributeDefNameDaysBeforeToRemind().getName(), daysBeforeReminder);
    if (updateLastCertifiedDate) {
      String date = new SimpleDateFormat("yyyy/MM/dd").format(new Date());
      attributeAssign.getAttributeValueDelegate().assignValue(GrouperAttestationJob.retrieveAttributeDefNameDateCertified().getName(), date);
    }
    attributeAssign.saveOrUpdate(false);
  }

  
  /**
   * stem attestation
   * @param request
   * @param response
   */
  public void stemAttestation(HttpServletRequest request, HttpServletResponse response) {
        
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    Stem stem = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      stem = UiV2Stem.retrieveStemHelper(request, true).getStem();
      
      if (stem == null) {
        return;
      }
      
      final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      
      final Stem STEM = stem;
      
      //switch over to admin so attributes work
      GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        @Override
        public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
          
          if (!checkAttributeDef(guiResponseJs)) {
            return null;
          }

          setupAttestation(STEM);
          
          guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
              "/WEB-INF/grouperUi2/stem/stemAttestation.jsp"));
          
          return null;
        }
      });
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
  private GuiAttestation retrieveStemAttestation(AttributeAssignable attributeAssignable) {
    GuiAttestation result = null;
    AttributeAssign attributeAssign = attributeAssignable.getAttributeDelegate().retrieveAssignment(null, GrouperAttestationJob.retrieveAttributeDefNameValueDef(), false, false);
    String attestationSendEmail = attributeAssign.getAttributeValueDelegate().retrieveValueString(GrouperAttestationJob.retrieveAttributeDefNameSendEmail().getName());
    String attestationEmailAddresses = attributeAssign.getAttributeValueDelegate().retrieveValueString(GrouperAttestationJob.retrieveAttributeDefNameEmailAddresses().getName());
    String attestationDaysUntilRecertify = attributeAssign.getAttributeValueDelegate().retrieveValueString(GrouperAttestationJob.retrieveAttributeDefNameDaysUntilRecertify().getName());
    String attestationDaysBeforeToRemind = attributeAssign.getAttributeValueDelegate().retrieveValueString(GrouperAttestationJob.retrieveAttributeDefNameDaysBeforeToRemind().getName());
    String attestationStemScope = attributeAssign.getAttributeValueDelegate().retrieveValueString(GrouperAttestationJob.retrieveAttributeDefNameStemScope().getName());
    
    if (attributeAssignable instanceof Stem) {
      result = new GuiAttestation(attributeAssignable, GrouperUtil.booleanObjectValue(attestationSendEmail), 
          attestationEmailAddresses, attestationDaysUntilRecertify,
          null, attestationDaysBeforeToRemind, attestationStemScope, null, false, null);
    }
    return result;
  }
  
  private void stemAttestationHelper(HttpServletRequest request, HttpServletResponse response, Stem stem) {
    
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    GrouperRequestContainer grouperRequestContainer = GrouperRequestContainer.retrieveFromRequestOrCreate();
    
    if (!checkAttributeDef(guiResponseJs)) {
      return;
    }
    
    // stem has direct attestation
    if (stem.getAttributeDelegate().hasAttribute(GrouperAttestationJob.retrieveAttributeDefNameValueDef())) {
      
      AttributeAssignable attributeAssignable = stem.getAttributeDelegate().getAttributeOrAncestorAttribute(GrouperAttestationJob.retrieveAttributeDefNameValueDef().getName(), false);
      
      GuiAttestation guiAttestation = retrieveStemAttestation(attributeAssignable);
    
      if (guiAttestation != null) {
        grouperRequestContainer.getStemContainer().setGuiAttestation(guiAttestation);
      } else {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("attestationAttributeAssignedError")));
        return;
      }
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#stemAttestation",
          "/WEB-INF/grouperUi2/stem/stemViewAttestation.jsp"));
      
    } else if (stem.getAttributeDelegate().hasAttributeOrAncestorHasAttribute(GrouperAttestationJob.retrieveAttributeDefNameValueDef().getName(), false)) {
      
      AttributeAssignable attributeAssignable = stem.getAttributeDelegate().getAttributeOrAncestorAttribute(GrouperAttestationJob.retrieveAttributeDefNameValueDef().getName(), false);
      
      GuiAttestation guiAttestation = retrieveStemAttestation(attributeAssignable);
    
      if (guiAttestation == null) {
        grouperRequestContainer.getStemContainer().setGuiAttestation(guiAttestation);
      } else {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("attestationAttributeAssignedError")));
        return;
      }
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#stemAttestation",
          "/WEB-INF/grouperUi2/stem/stemViewAttestation.jsp"));
      
    } else {
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#stemAttestation",
          "/WEB-INF/grouperUi2/stem/stemNoAttestation.jsp"));
    }
    
  }
  
//  /**
//   * @param request
//   * @param response
//   */
//  public void addStemAttestation(HttpServletRequest request, HttpServletResponse response) {
//    
//    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
//    
//    GrouperSession grouperSession = null;
//  
//    Stem stem = null;
//  
//    try {
//  
//      grouperSession = GrouperSession.start(loggedInSubject);
//        
//      stem = UiV2Stem.retrieveStemHelper(request, true).getStem();
//      
//      if (stem == null) {
//        return;
//      }
//      
//      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
//      
//      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
//          "/WEB-INF/grouperUi2/stem/stemAttestation.jsp"));
//      
//      if (!checkAttributeDef(guiResponseJs)) {
//        return;
//      }
//      GrouperRequestContainer grouperRequestContainer = GrouperRequestContainer.retrieveFromRequestOrCreate();
//      
//      GuiAttestation guiAttestation = new GuiAttestation(stem, GuiAttestation.Type.DIRECT);
//      grouperRequestContainer.getStemContainer().setGuiAttestation(guiAttestation);
//      
//      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#stemAttestation",
//          "/WEB-INF/grouperUi2/stem/stemAttestationEdit.jsp"));
//      
//    } finally {
//      GrouperSession.stopQuietly(grouperSession);
//    }
//    
//  }
  
  /**
   * 
   * @param request
   * @param response
   */
  public void updateUncertifiedGroupAttestationLastCertifiedDate(HttpServletRequest request, HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    Stem stem = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
        
      stem = UiV2Stem.retrieveStemHelper(request, true).getStem();
      
      if (stem == null) {
        return;
      }
      
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
          TextContainer.retrieveFromRequest().getText().get("attestationLastCertifiedUpdateSuccess")));
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId",
          "/WEB-INF/grouperUi2/stem/stemAttestation.jsp"));
      
      updateAttestationLastCertifiedDate(stem, true);
      stemAttestationHelper(request, response, stem);
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }
  
  /**
   * update the last certified date for all the groups under this folder. 
   * @param request
   * @param response
   */
  public void updateAllGroupAttestationLastCertifiedDate(HttpServletRequest request, HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    Stem stem = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
        
      stem = UiV2Stem.retrieveStemHelper(request, true).getStem();
      
      if (stem == null) {
        return;
      }
      
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
          TextContainer.retrieveFromRequest().getText().get("attestationLastCertifiedUpdateSuccess")));
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId",
          "/WEB-INF/grouperUi2/stem/stemAttestation.jsp"));
      
      updateAttestationLastCertifiedDate(stem, false);
      stemAttestationHelper(request, response, stem);
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
  /**
   * @param stem
   */
  private void updateAttestationLastCertifiedDate(Stem stem, boolean onlyIfNeverCertified) {
    
    AttributeDefName attributeDefName = AttributeDefNameFinder.findByName(GrouperAttestationJob.retrieveAttributeDefNameValueDef().getName(), false);
    
    String date = new SimpleDateFormat("yyyy/MM/dd").format(new Date());
    
    Set<Group> childGroups = stem.getChildGroups(Scope.SUB);
    
    // go through all the child groups and certify if they have attestation attributes
    for (Group group: childGroups) {
      AttributeAssign attributeAssign = group.getAttributeDelegate().retrieveAssignment(null, attributeDefName, false, false);
      if (attributeAssign != null) {
        if (onlyIfNeverCertified && attributeAssign.getAttributeValueDelegate().retrieveValueString(GrouperAttestationJob.retrieveAttributeDefNameDateCertified().getName()) == null) {
          attributeAssign.getAttributeValueDelegate().assignValue(GrouperAttestationJob.retrieveAttributeDefNameDateCertified().getName(), date);
          attributeAssign.saveOrUpdate(false);
        } else if (!onlyIfNeverCertified) {
          attributeAssign.getAttributeValueDelegate().assignValue(GrouperAttestationJob.retrieveAttributeDefNameDateCertified().getName(), date);
          attributeAssign.saveOrUpdate(false);
        }
      }
    }
        
  }
  
//  /**
//   * @param request
//   * @param response
//   */
//  public void editStemAttestation2(HttpServletRequest request, HttpServletResponse response) {
//    
//    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
//    GrouperSession grouperSession = null;
//    Stem stem = null;
//    try {
//      grouperSession = GrouperSession.start(loggedInSubject);
//      stem = UiV2Stem.retrieveStemHelper(request, true).getStem();
//      if (stem == null) {
//        return;
//      }
//      
//      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
//      
//      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
//          "/WEB-INF/grouperUi2/stem/stemAttestation.jsp"));
//      
//      GrouperRequestContainer grouperRequestContainer = GrouperRequestContainer.retrieveFromRequestOrCreate();
//      GuiAttestation attestation = retrieveStemAttestation(stem, GuiAttestation.Type.DIRECT);
//      
//      if (attestation != null) {
//        grouperRequestContainer.getStemContainer().setGuiAttestation(attestation);
//        guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#stemAttestation", 
//            "/WEB-INF/grouperUi2/stem/stemAttestationEdit.jsp"));
//      }
//      
//    } finally {
//      GrouperSession.stopQuietly(grouperSession);
//    }
//  }
  
  /**
   * 
   * @param request
   * @param response
   */
  public void editStemAttestationSubmit(HttpServletRequest request, HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    GrouperRequestContainer grouperRequestContainer = GrouperRequestContainer.retrieveFromRequestOrCreate();
    
    Stem stem = null;
  
    boolean error = false;
    
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
        
      stem = UiV2Stem.retrieveStemHelper(request, true).getStem();
      
      if (stem == null) {
        return;
      }
      
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/stem/stemAttestation.jsp"));
      
      boolean sendEmail = GrouperUtil.booleanValue(request.getParameter("grouperAttestationSendEmail[]"), false);
      boolean updateLastCertifiedDate = GrouperUtil.booleanValue(request.getParameter("attestationUpdateLastCertified[]"), false);
      String emailAddresses = request.getParameter("grouperAttestationEmailAddresses");
      String daysUntilRectify = request.getParameter("grouperAttestationDaysUntilRecertify");
      String daysBeforeReminder = request.getParameter("grouperAttestationDaysBeforeToRemind");
      String stemScope = request.getParameter("levelsName");

      GuiScreenAction guiScreenActionError = null;

      String daysUntilRectifyOrDefault = daysUntilRectify;
      
      if (StringUtils.isBlank(daysUntilRectifyOrDefault)) {
        daysUntilRectifyOrDefault = "" + GrouperConfig.retrieveConfig().propertyValueInt("attestation.default.daysUntilRecertify", 180);
      }

      if (!error && !NumberUtils.isNumber(daysUntilRectifyOrDefault)) {
        guiScreenActionError = GuiScreenAction.newValidationMessage(GuiMessageType.error, 
            "#grouperAttestationDaysUntilRecertify",
            TextContainer.retrieveFromRequest().getText().get("attestationDaysUntilRectifyValidationError"));
        error = true;
      }

      String daysBeforeReminderOrDefault = daysBeforeReminder;

      if (StringUtils.isBlank(daysBeforeReminderOrDefault)) {
        daysBeforeReminderOrDefault = "0";
      }

      if (!error && !NumberUtils.isNumber(daysBeforeReminderOrDefault)) {
        guiScreenActionError = GuiScreenAction.newValidationMessage(GuiMessageType.error, 
            "#grouperAttestationDaysBeforeToRemind",
            TextContainer.retrieveFromRequest().getText().get("attestationDaysBeforeReminderValidationError"));
        error = true;
      }
      
      AttributeDefName attributeDefName = null;
      Scope scope = null;
      
      if (!error) {
        scope = Scope.valueOfIgnoreCase(stemScope, false);
        
        attributeDefName = AttributeDefNameFinder.findByName(GrouperAttestationJob.retrieveAttributeDefNameValueDef().getName(), false);
        if (attributeDefName == null) {
          guiScreenActionError = GuiScreenAction.newMessage(GuiMessageType.error, 
              TextContainer.retrieveFromRequest().getText().get("attestationAttributeNotFoundError"));
          error = true;
        }
      }
      
      if (!error) {
        final AuditEntry auditEntry;
        if (!stem.getAttributeDelegate().hasAttributeByName(GrouperAttestationJob.retrieveAttributeDefNameValueDef().getName())) {
          stem.getAttributeDelegate().assignAttribute(attributeDefName); // we are adding attribute here
          auditEntry = new AuditEntry(AuditTypeBuiltin.STEM_ATTESTATION_ADD, "stemId", stem.getId(), "stemName", stem.getDisplayName());
          auditEntry.setDescription("Add stem attestation: "+stem.getName());
        } else {
          auditEntry = new AuditEntry(AuditTypeBuiltin.STEM_ATTESTATION_UPDATE, "stemId", stem.getId(), "stemName", stem.getDisplayName());
          auditEntry.setDescription("Update stem attestation: "+stem.getName());
        }
        updateStemAttestationAttributes(stem, attributeDefName, sendEmail, emailAddresses, daysUntilRectify, daysBeforeReminder, updateLastCertifiedDate, scope);
        guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2Attestation.stemAttestation&stemId=" + stem.getId() + "')"));
        attestationSaveAudit(auditEntry);
      }

      if (error) {

        GuiAttestation guiAttestation = new GuiAttestation(stem);

        guiAttestation.setGrouperAttestationDaysBeforeToRemind(daysBeforeReminder);
        guiAttestation.setGrouperAttestationDaysUntilRecertify(daysUntilRectify);
        guiAttestation.setGrouperAttestationEmailAddresses(emailAddresses);
        guiAttestation.setGrouperAttestationSendEmail(sendEmail);
        
        grouperRequestContainer.getStemContainer().setGuiAttestation(guiAttestation);
        
        guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#stemAttestation",
            "/WEB-INF/grouperUi2/stem/stemAttestationEdit.jsp"));

        if (guiScreenActionError != null) {
          guiResponseJs.addAction(guiScreenActionError);
        }
      }
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }

  /**
   * 
   * @param auditEntry
   */
  private static void attestationSaveAudit(final AuditEntry auditEntry) {
    HibernateSession.callbackHibernateSession(
        GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_AUDIT,
          new HibernateHandler() {
              public Object callback(HibernateHandlerBean hibernateHandlerBean)
                  throws GrouperDAOException {
                
                auditEntry.saveOrUpdate(true);
                return null;
              }
        });

  }
  
  
  private void updateStemAttestationAttributes(Stem stem, AttributeDefName attributeDefName, boolean sendEmail, 
      String emailAddresses, String daysUntilRectify, String daysBeforeReminder, boolean updateLastCertifiedDate, Scope scope) {
    
    AttributeAssign attributeAssign = stem.getAttributeDelegate().retrieveAssignment(null, attributeDefName, false, false);
    attributeAssign.getAttributeValueDelegate().assignValue(GrouperAttestationJob.retrieveAttributeDefNameSendEmail().getName(), sendEmail ? "true": "false");
    attributeAssign.getAttributeValueDelegate().assignValue(GrouperAttestationJob.retrieveAttributeDefNameEmailAddresses().getName(), emailAddresses);
    attributeAssign.getAttributeValueDelegate().assignValue(GrouperAttestationJob.retrieveAttributeDefNameDaysUntilRecertify().getName(), daysUntilRectify);
    attributeAssign.getAttributeValueDelegate().assignValue(GrouperAttestationJob.retrieveAttributeDefNameDaysBeforeToRemind().getName(), daysBeforeReminder);
    attributeAssign.getAttributeValueDelegate().assignValue(GrouperAttestationJob.retrieveAttributeDefNameStemScope().getName(), scope.name().toLowerCase());
    if (updateLastCertifiedDate) {
      updateAttestationLastCertifiedDate(stem, false);
    }
    attributeAssign.saveOrUpdate(false);
  }

  /**
   * @param request
   * @param response
   */
  public void editStemAttestation(HttpServletRequest request, HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    Stem stem = null;

    try {

      grouperSession = GrouperSession.start(loggedInSubject);
        
      stem = UiV2Stem.retrieveStemHelper(request, true).getStem();
      if (stem == null) {
        return;
      }
      
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/stem/stemAttestation.jsp"));
      
      if (!checkAttributeDef(guiResponseJs)) {
        return;
      }

      GrouperRequestContainer grouperRequestContainer = GrouperRequestContainer.retrieveFromRequestOrCreate();

      AttestationContainer attestationContainer = grouperRequestContainer.getAttestationContainer();

      attestationContainer.setEditAttestationIsAssigned(attestationContainer.isDirectStemAttestationAssignment());
      attestationContainer.setEditAttestationSendEmail(attestationContainer.isSendEmail());
      attestationContainer.setEditAttestationEmailGroupManagers(attestationContainer.isEmailGroupManagers());
      attestationContainer.setEditAttestationEmailAddresses(attestationContainer.getEmailAddresses());
      Integer recertifyDays = attestationContainer.getRecertifyDays();
      attestationContainer.setEditAttestationDefaultCertify(recertifyDays == null);
      if (recertifyDays != null) {
        attestationContainer.setEditAttestationCustomRecertifyDays(recertifyDays);
      }
      editStemAttestationHelper(request, attestationContainer);

      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#stemAttestation", 
          "/WEB-INF/grouperUi2/stem/stemAttestationEdit.jsp"));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * @param request
   * @param grouperLoaderContainer
   */
  private void editStemAttestationHelper(HttpServletRequest request,
      AttestationContainer attestationContainer) {
    editAttestationHelper(request, attestationContainer, false);
  }

  /**
   * 
   * @param request
   * @param response
   */
  public void editStemAttestationSave(HttpServletRequest request, HttpServletResponse response) {
  
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    Stem stem = null;

    try {

      grouperSession = GrouperSession.start(loggedInSubject);
        
      stem = UiV2Stem.retrieveStemHelper(request, true).getStem();
      if (stem == null) {
        return;
      }
      
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/stem/stemAttestation.jsp"));
      
      if (!checkAttributeDef(guiResponseJs)) {
        return;
      }
  
      GrouperRequestContainer grouperRequestContainer = GrouperRequestContainer.retrieveFromRequestOrCreate();
  
      AttestationContainer attestationContainer = grouperRequestContainer.getAttestationContainer();
  
      List<GuiScreenAction> guiScreenActions = new ArrayList<GuiScreenAction>();

      boolean hasError = editAttestationSaveHelper(request, response, true, guiScreenActions);
      
      if (!hasError) {
        //if it was removed
        AuditEntry auditEntry = null;
        if (!attestationContainer.isEditAttestationIsAssigned()) {
          if (attestationContainer.isDirectStemAttestationAssignment()) {
  
            stem.getAttributeDelegate().removeAttribute(GrouperAttestationJob.retrieveAttributeDefNameValueDef());
  
            guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
                TextContainer.retrieveFromRequest().getText().get("grouperAttestationEditRemoved")));

            auditEntry = new AuditEntry(AuditTypeBuiltin.STEM_ATTESTATION_DELETE, "stemId", stem.getId(), "stemName", stem.getName());
            auditEntry.setDescription("Update stem attestation: "+stem.getName());

            attestationSaveAudit(auditEntry);

          }
        } else {
          if (!attestationContainer.isDirectGroupAttestationAssignment()) {
            stem.getAttributeDelegate().assignAttribute(GrouperAttestationJob.retrieveAttributeDefNameValueDef()); // we are adding attribute here
            auditEntry = new AuditEntry(AuditTypeBuiltin.STEM_ATTESTATION_ADD, "stemId", stem.getId(), "stemName", stem.getName());
            auditEntry.setDescription("Add stem attestation: "+stem.getName());

          } else {
            auditEntry = new AuditEntry(AuditTypeBuiltin.STEM_ATTESTATION_UPDATE, "stemId", stem.getId(), "stemName", stem.getName());
            auditEntry.setDescription("Update stem attestation: "+stem.getName());
          }

          updateStemAttestationAttributes(stem, GrouperAttestationJob.retrieveAttributeDefNameValueDef(), attestationContainer.isEditAttestationSendEmail(), 
              attestationContainer.getEditAttestationEmailAddresses(), GrouperUtil.stringValue(attestationContainer.getEditAttestationCustomRecertifyDays()), 
              null, attestationContainer.isEditAttestationResetCertifiedToToday(), attestationContainer.isEditAttestationStemScopeSub() ? Stem.Scope.SUB : Stem.Scope.ONE);
          guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2Attestation.stemAttestation&stemId=" + stem.getId() + "')"));

          attestationSaveAudit(auditEntry);
        }
      }
      
      if (!hasError) {
  
        guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2Attestation.stemAttestation&stemId=" + stem.getId() + "')"));
  
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
            TextContainer.retrieveFromRequest().getText().get("grouperAttestationEditSaveSuccess")));
  
      } else {
        
        guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#groupAttestation", 
            "/WEB-INF/grouperUi2/group/groupAttestationEdit.jsp"));
        
        //add these after screen drawn
        for (GuiScreenAction guiScreenAction : guiScreenActions) {
          guiResponseJs.addAction(guiScreenAction);
        }
        
      }
    } catch (RuntimeException re) {
      if (GrouperUiUtils.vetoHandle(GuiResponseJs.retrieveGuiResponseJs(), re)) {
        return;
      }
      throw re;
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  
  }

  /**
   * @param request
   * @param response
   */
  public void clearGroupAttestation(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    Group group = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
        
      group = UiV2Group.retrieveGroupHelper(request, AccessPrivilege.UPDATE).getGroup();
      
      if (group == null) {
        return;
      }
  
      final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      
      final Group GROUP = group;
      
      //switch over to admin so attributes work
      Object error = GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        @Override
        public Boolean callback(GrouperSession grouperSession) throws GrouperSessionException {
  
          AttributeAssign attributeAssign = GROUP.getAttributeDelegate().retrieveAssignment(null, GrouperAttestationJob.retrieveAttributeDefNameValueDef(), false, false);
          if (attributeAssign == null) {
            guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
                TextContainer.retrieveFromRequest().getText().get("attestationAttributeNotFoundError")));
            return true;
          }
          attributeAssign.getAttributeValueDelegate().assignValue(GrouperAttestationJob.retrieveAttributeDefNameDateCertified().getName(), null);

          GrouperAttestationJob.updateCalculatedDaysUntilRecertify(GROUP, attributeAssign);
  
          AuditEntry auditEntry = new AuditEntry(AuditTypeBuiltin.GROUP_ATTESTATION_CLEAR_LAST_CERTIFIED_DATE, 
              "groupId", GROUP.getId(), "groupName", GROUP.getName());
          auditEntry.setDescription("Clear last certified date attribute of group: " + GROUP.getName());
          attestationSaveAudit(auditEntry);

          setupAttestation(GROUP);            
          
          return false;
        }
      });

      if (!(Boolean) error) {
        guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#groupAttestation",
            "/WEB-INF/grouperUi2/group/groupAttestationView.jsp"));

        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
            TextContainer.retrieveFromRequest().getText().get("groupAttestationSuccessClearedAttestationDate")));
      }

    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }

  /**
   * @param request
   * @param response
   */
  public void runDaemon(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
        
      final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();

      AttestationContainer attestationContainer = GrouperRequestContainer.retrieveFromRequestOrCreate()
          .getAttestationContainer();

      if (!attestationContainer.isCanRunDaemon()) {
        throw new RuntimeException("Not allowed!!!!!");
      }
      
      final boolean[] DONE = new boolean[]{false};
      
      Thread thread = new Thread(new Runnable() {

        @Override
        public void run() {
          GrouperSession grouperSession = GrouperSession.startRootSession();
          try {
            GrouperAttestationJob.runDaemonStandalone();
            DONE[0] = true;
          } catch (RuntimeException re) {
            LOG.error("Error in running daemon", re);
          } finally {
            GrouperSession.stopQuietly(grouperSession);
          }
          
        }
        
      });

      thread.start();
      
      try {
        thread.join(45000);
      } catch (Exception e) {
        throw new RuntimeException("Exception in thread");
      }

      if (DONE[0]) {

        //if we are on a group screen
        Group group = UiV2Group.retrieveGroupHelper(request, AccessPrivilege.READ, false).getGroup();

        if (group != null) {
          setupAttestation(group);            
            
          guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#groupAttestation",
              "/WEB-INF/grouperUi2/group/groupAttestationView.jsp"));
        }    
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
                TextContainer.retrieveFromRequest().getText().get("groupAttestationSuccessDaemonRan")));
        
      } else {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.info, 
            TextContainer.retrieveFromRequest().getText().get("groupAttestationInfoDaemonInRunning")));

      }
      
  
  
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }

  /**
   * view audits for group attestation
   * @param request
   * @param response
   */
  public void viewGroupAudits(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    Group group = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      group = UiV2Group.retrieveGroupHelper(request, AccessPrivilege.READ).getGroup();
      
      if (group == null) {
        return;
      }
  
      final Group GROUP = group;
      
      //switch over to admin so attributes work
      GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        @Override
        public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
          
          final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();

          if (!checkAttributeDef(guiResponseJs)) {
            return null;
          }
          guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
              "/WEB-INF/grouperUi2/group/groupAttestation.jsp"));

          guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#groupAttestation",
              "/WEB-INF/grouperUi2/group/groupAttestationViewAudits.jsp"));

          viewGroupAuditsHelper(request, response, GROUP);
      
          return null;
        }
      });
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * filter audits for group attestation
   * @param request
   * @param response
   */
  public void viewGroupAuditsFilter(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    Group group = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      group = UiV2Group.retrieveGroupHelper(request, AccessPrivilege.READ).getGroup();
      
      if (group == null) {
        return;
      }
  
      final Group GROUP = group;
      
      //switch over to admin so attributes work
      GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {

        @Override
        public Object callback(GrouperSession grouperSession) throws GrouperSessionException {

          final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();

          if (!checkAttributeDef(guiResponseJs)) {
            return null;
          }

          viewGroupAuditsHelper(request, response, GROUP);

          return null;
        }
      });
      

    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * the audit filter button was pressed, or paging or sorting, or view audits or something
   * @param request
   * @param response
   * @param group 
   */
  private void viewGroupAuditsHelper(HttpServletRequest request, HttpServletResponse response, Group group) {
    
    GrouperRequestContainer grouperRequestContainer = GrouperRequestContainer.retrieveFromRequestOrCreate();
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
    //all, on, before, between, or since
    String filterTypeString = request.getParameter("filterType");
  
    if (StringUtils.isBlank(filterTypeString)) {
      filterTypeString = "all";
    }
    
    String filterFromDateString = request.getParameter("filterFromDate");
    String filterToDateString = request.getParameter("filterToDate");
  
    //massage dates
    if (StringUtils.equals(filterTypeString, "all")) {
      guiResponseJs.addAction(GuiScreenAction.newFormFieldValue("filterFromDate", ""));
      filterFromDateString = null;
      guiResponseJs.addAction(GuiScreenAction.newFormFieldValue("filterToDate", ""));
      filterToDateString = null;
    } else if (StringUtils.equals(filterTypeString, "on")) {
  
      guiResponseJs.addAction(GuiScreenAction.newFormFieldValue("filterToDate", ""));
      filterToDateString = null;
    } else if (StringUtils.equals(filterTypeString, "before")) {
      guiResponseJs.addAction(GuiScreenAction.newFormFieldValue("filterToDate", ""));
      filterToDateString = null;
    } else if (StringUtils.equals(filterTypeString, "between")) {
    } else if (StringUtils.equals(filterTypeString, "since")) {
      guiResponseJs.addAction(GuiScreenAction.newFormFieldValue("filterToDate", ""));
      filterToDateString = null;
    } else {
      //should never happen
      throw new RuntimeException("Not expecting filterType string: " + filterTypeString);
    }
  
    Date filterFromDate = null;
    Date filterToDate = null;
  
    if (StringUtils.equals(filterTypeString, "on") || StringUtils.equals(filterTypeString, "before")
        || StringUtils.equals(filterTypeString, "between") || StringUtils.equals(filterTypeString, "since")) {
      if (StringUtils.isBlank(filterFromDateString)) {
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error,
            "#from-date",
            TextContainer.retrieveFromRequest().getText().get("groupAuditLogFilterFromDateRequired")));
        return;
      }
      try {
        filterFromDate = GrouperUtil.stringToTimestamp(filterFromDateString);
      } catch (Exception e) {
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error,
            "#from-date",
            TextContainer.retrieveFromRequest().getText().get("groupAuditLogFilterFromDateInvalid")));
        return;
      }
    }
    if (StringUtils.equals(filterTypeString, "between")) {
      if (StringUtils.isBlank(filterToDateString)) {
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error,
            "#to-date",
            TextContainer.retrieveFromRequest().getText().get("groupAuditLogFilterToDateRequired")));
        return;
      }
      try {
        filterToDate = GrouperUtil.stringToTimestamp(filterToDateString);
      } catch (Exception e) {
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error,
            "#to-date",
            TextContainer.retrieveFromRequest().getText().get("groupAuditLogFilterToDateInvalid")));
        return;
      }
    }
    
    boolean extendedResults = false;
  
    {
      String showExtendedResultsString = request.getParameter("showExtendedResults[]");
      if (!StringUtils.isBlank(showExtendedResultsString)) {
        extendedResults = GrouperUtil.booleanValue(showExtendedResultsString);
      }
    }
    
    GroupContainer groupContainer = grouperRequestContainer.getGroupContainer();
    
    GuiPaging guiPaging = groupContainer.getGuiPaging();
    QueryOptions queryOptions = new QueryOptions();
  
    GrouperPagingTag2.processRequest(request, guiPaging, queryOptions);
  
    UserAuditQuery query = new UserAuditQuery();
  
    //process dates
    if (StringUtils.equals(filterTypeString, "on")) {
  
      query.setOnDate(filterFromDate);
    } else  if (StringUtils.equals(filterTypeString, "between")) {
      query.setFromDate(filterFromDate);
      query.setToDate(filterToDate);
    } else  if (StringUtils.equals(filterTypeString, "since")) {
      query.setFromDate(filterFromDate);
    } else  if (StringUtils.equals(filterTypeString, "before")) {
      query.setToDate(filterToDate);
    }
    
    query.setQueryOptions(queryOptions);
  
    queryOptions.sortDesc("lastUpdatedDb");
    
    GuiSorting guiSorting = new GuiSorting(queryOptions.getQuerySort());
    groupContainer.setGuiSorting(guiSorting);
  
    guiSorting.processRequest(request);
    
    query.addAuditTypeFieldValue(AuditFieldType.AUDIT_TYPE_ATTESTATION_GROUP_ID, group.getId());
  
    List<AuditEntry> auditEntries = query.execute();
  
    groupContainer.setGuiAuditEntries(GuiAuditEntry.convertFromAuditEntries(auditEntries));
  
    guiPaging.setTotalRecordCount(queryOptions.getQueryPaging().getTotalRecordCount());
  
    if (GrouperUtil.length(auditEntries) == 0) {
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.info,
          TextContainer.retrieveFromRequest().getText().get("groupAuditLogNoEntriesFound")));
    }
    
    groupContainer.setAuditExtendedResults(extendedResults);
    guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#groupAuditFilterResultsId", 
        "/WEB-INF/grouperUi2/group/groupAttestationViewAuditsContents.jsp"));
  
  }

  /**
   * the attestation overall button was pressed from misc page
   * @param request
   * @param response
   */
  public void allSettings(HttpServletRequest request, HttpServletResponse response) {
  
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
  
    GrouperSession grouperSession = null;
  
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
    try {
      grouperSession = GrouperSession.start(loggedInSubject);
      
      List<GuiAttestation> guiAttestations = new ArrayList<GuiAttestation>();
      GrouperRequestContainer.retrieveFromRequestOrCreate().getAttestationContainer().setGuiAttestations(guiAttestations);

      {
        //get stems with settings
        Set<Stem> stems = new StemFinder().assignPrivileges(NamingPrivilege.ADMIN_PRIVILEGES)
            .assignIdOfAttributeDefName(GrouperAttestationJob.retrieveAttributeDefNameSendEmail().getId())
            .assignAttributeValuesOnAssignment(GrouperUtil.toSetObjectType("true", "false"))
            .assignAttributeCheckReadOnAttributeDef(false).assignQueryOptions(QueryOptions.create(null, null, 1, 150))
            .findStems();
  
        if (GrouperUtil.length(stems) > 0) {
          AttributeAssignValueFinderResult attributeAssignValueFinderResult = new AttributeAssignValueFinder().assignOwnerStemsOfAssignAssign(stems)
            .addAttributeDefNameIdsOfBaseAssignment(GrouperAttestationJob.retrieveAttributeDefNameValueDef().getId())
            .assignAttributeCheckReadOnAttributeDef(false)
            .findAttributeAssignValuesResult();
    
          guiAttestations.addAll(GuiAttestation.convertStemIntoGuiAttestation(stems,
              attributeAssignValueFinderResult));
        }
      }
      
      {
        //get groups with settings
        Set<Group> groups = new GroupFinder().assignPrivileges(AccessPrivilege.READ_PRIVILEGES)
            .assignIdOfAttributeDefName(GrouperAttestationJob.retrieveAttributeDefNameSendEmail().getId())
            .assignAttributeValuesOnAssignment(GrouperUtil.toSetObjectType("true", "false"))
            .assignAttributeCheckReadOnAttributeDef(false).assignQueryOptions(QueryOptions.create(null, null, 1, 150))
            .findGroups();
        
        if (GrouperUtil.length(groups) > 0) {
          AttributeAssignValueFinderResult attributeAssignValueFinderResult = new AttributeAssignValueFinder().assignOwnerGroupsOfAssignAssign(groups)
              .addAttributeDefNameIdsOfBaseAssignment(GrouperAttestationJob.retrieveAttributeDefNameValueDef().getId())
              .assignAttributeCheckReadOnAttributeDef(false)
              .findAttributeAssignValuesResult();
          guiAttestations.addAll(GuiAttestation.convertGroupIntoGuiAttestation(groups,
              attributeAssignValueFinderResult));
          
        }
  
      }
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/group/groupAttestationOverallSettings.jsp"));

    } catch (RuntimeException re) {
      if (GrouperUiUtils.vetoHandle(GuiResponseJs.retrieveGuiResponseJs(), re)) {
        return;
      }
      throw re;
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }


  
  /**
   * the attestation overall button was pressed from misc page
   * @param request
   * @param response
   */
  public void attestationOverall(HttpServletRequest request, HttpServletResponse response) {
  
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
  
    GrouperSession grouperSession = null;
  
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
    try {
      grouperSession = GrouperSession.start(loggedInSubject);
      
      List<GuiAttestation> guiAttestations = new ArrayList<GuiAttestation>();
      GrouperRequestContainer.retrieveFromRequestOrCreate().getAttestationContainer().setGuiAttestations(guiAttestations);

      Set<Group> groups = new GroupFinder().assignPrivileges(AccessPrivilege.READ_PRIVILEGES)
          .assignIdOfAttributeDefName(GrouperAttestationJob.retrieveAttributeDefNameCalculatedDaysLeft().getId())
          .assignAttributeValuesOnAssignment(GrouperAttestationJob.TWO_WEEKS_DAYS_LEFT)
          .assignAttributeCheckReadOnAttributeDef(false).assignQueryOptions(QueryOptions.create(null, null, 1, 150))
          .findGroups();
      
      if (GrouperUtil.length(groups) > 0) {
        AttributeAssignValueFinderResult attributeAssignValueFinderResult = new AttributeAssignValueFinder().assignOwnerGroupsOfAssignAssign(groups)
          .addAttributeDefNameIdsOfBaseAssignment(GrouperAttestationJob.retrieveAttributeDefNameValueDef().getId())
          .assignAttributeCheckReadOnAttributeDef(false)
          .findAttributeAssignValuesResult();
  
        guiAttestations.addAll(GuiAttestation.convertGroupIntoGuiAttestation(groups,
            attributeAssignValueFinderResult));
      }
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/group/groupAttestationOverall.jsp"));

    } catch (RuntimeException re) {
      if (GrouperUiUtils.vetoHandle(GuiResponseJs.retrieveGuiResponseJs(), re)) {
        return;
      }
      throw re;
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
     * the attestation groups in a stem was pressed
     * @param request
     * @param response
     */
    public void viewGroupsInStem(HttpServletRequest request, HttpServletResponse response) {
    
      final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
      GrouperSession grouperSession = null;
    
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
      try {
        grouperSession = GrouperSession.start(loggedInSubject);

        Stem stem = UiV2Stem.retrieveStemHelper(request, false, false, true).getStem();
        
        if (stem == null) {
          return;
        }
        
        List<GuiAttestation> guiAttestations = new ArrayList<GuiAttestation>();
        GrouperRequestContainer.retrieveFromRequestOrCreate().getAttestationContainer().setGuiAttestations(guiAttestations);
  
        Set<Group> groups = new GroupFinder().assignPrivileges(AccessPrivilege.READ_PRIVILEGES)
          .assignIdOfAttributeDefName(GrouperAttestationJob.retrieveAttributeDefNameCalculatedDaysLeft().getId())
          .assignParentStemId(stem.getId())
          .assignStemScope(Scope.SUB)
          .assignAttributeValuesOnAssignment(GrouperAttestationJob.TWO_WEEKS_DAYS_LEFT)
          .assignAttributeCheckReadOnAttributeDef(false).assignQueryOptions(QueryOptions.create(null, null, 1, 150))
          .findGroups();

        if (GrouperUtil.length(groups) > 0) {
          AttributeAssignValueFinderResult attributeAssignValueFinderResult = new AttributeAssignValueFinder().assignOwnerGroupsOfAssignAssign(groups)
            .addAttributeDefNameIdsOfBaseAssignment(GrouperAttestationJob.retrieveAttributeDefNameValueDef().getId())
            .assignAttributeCheckReadOnAttributeDef(false)
            .findAttributeAssignValuesResult();
          
          guiAttestations.addAll(GuiAttestation.convertGroupIntoGuiAttestation(groups,
              attributeAssignValueFinderResult));
        }        
        guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
            "/WEB-INF/grouperUi2/stem/stemAttestationGroups.jsp"));
    
      } catch (RuntimeException re) {
        if (GrouperUiUtils.vetoHandle(GuiResponseJs.retrieveGuiResponseJs(), re)) {
          return;
        }
        throw re;
      } finally {
        GrouperSession.stopQuietly(grouperSession);
      }
    }

  /**
   * attestation settings in a stem
   * @param request
   * @param response
   */
  public void viewSettingsInStem(HttpServletRequest request, HttpServletResponse response) {
  
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
  
    GrouperSession grouperSession = null;
  
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
    try {
      grouperSession = GrouperSession.start(loggedInSubject);
      
      Stem stem = UiV2Stem.retrieveStemHelper(request, false, false, true).getStem();
      
      if (stem == null) {
        return;
      }

      List<GuiAttestation> guiAttestations = new ArrayList<GuiAttestation>();
      GrouperRequestContainer.retrieveFromRequestOrCreate().getAttestationContainer().setGuiAttestations(guiAttestations);
  
      {
        //does this stem have settings?
        GuiAttestation guiAttestation = retrieveGuiAttestation(stem);
        if (guiAttestation != null) {
          guiAttestations.add(guiAttestation);
        }
      }
      
      {
        //get stems with settings
        Set<Stem> stems = new StemFinder().assignPrivileges(NamingPrivilege.ADMIN_PRIVILEGES)
            .assignIdOfAttributeDefName(GrouperAttestationJob.retrieveAttributeDefNameSendEmail().getId())
            .assignParentStemId(stem.getId())
            .assignStemScope(Scope.SUB)
            .assignAttributeValuesOnAssignment(GrouperUtil.toSetObjectType("true", "false"))
            .assignAttributeCheckReadOnAttributeDef(false).assignQueryOptions(QueryOptions.create(null, null, 1, 150))
            .findStems();
  
        if (GrouperUtil.length(stems) > 0) {
          AttributeAssignValueFinderResult attributeAssignValueFinderResult = new AttributeAssignValueFinder().assignOwnerStemsOfAssignAssign(stems)
            .addAttributeDefNameIdsOfBaseAssignment(GrouperAttestationJob.retrieveAttributeDefNameValueDef().getId())
            .assignAttributeCheckReadOnAttributeDef(false)
            .findAttributeAssignValuesResult();
    
          guiAttestations.addAll(GuiAttestation.convertStemIntoGuiAttestation(stems,
              attributeAssignValueFinderResult));
        }
      }
      
      {
        //get groups with settings
        Set<Group> groups = new GroupFinder().assignPrivileges(AccessPrivilege.READ_PRIVILEGES)
            .assignIdOfAttributeDefName(GrouperAttestationJob.retrieveAttributeDefNameSendEmail().getId())
            .assignParentStemId(stem.getId())
            .assignStemScope(Scope.SUB)
            .assignAttributeValuesOnAssignment(GrouperUtil.toSetObjectType("true", "false"))
            .assignAttributeCheckReadOnAttributeDef(false).assignQueryOptions(QueryOptions.create(null, null, 1, 150))
            .findGroups();
  
        if (GrouperUtil.length(groups) > 0) {
          AttributeAssignValueFinderResult attributeAssignValueFinderResult = new AttributeAssignValueFinder().assignOwnerGroupsOfAssignAssign(groups)
            .addAttributeDefNameIdsOfBaseAssignment(GrouperAttestationJob.retrieveAttributeDefNameValueDef().getId())
            .assignAttributeCheckReadOnAttributeDef(false)
            .findAttributeAssignValuesResult();
    
          guiAttestations.addAll(GuiAttestation.convertGroupIntoGuiAttestation(groups,
              attributeAssignValueFinderResult));
        }
      }
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/stem/stemAttestationSettings.jsp"));
  
    } catch (RuntimeException re) {
      if (GrouperUiUtils.vetoHandle(GuiResponseJs.retrieveGuiResponseJs(), re)) {
        return;
      }
      throw re;
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * setup attestation stuff
   * @param attributeAssignable
   */
  public static void setupAttestation(final AttributeAssignable attributeAssignable) {
    GrouperRequestContainer grouperRequestContainer = GrouperRequestContainer.retrieveFromRequestOrCreate();
    AttestationContainer attestationContainer = grouperRequestContainer
        .getAttestationContainer();

    GuiAttestation attestation = retrieveGuiAttestation(attributeAssignable);
    if (attestation != null) {
      attestationContainer.setGuiAttestation(attestation);
    }
  }
  

}
