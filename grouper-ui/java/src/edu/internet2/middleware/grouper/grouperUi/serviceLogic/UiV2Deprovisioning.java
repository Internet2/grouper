package edu.internet2.middleware.grouper.grouperUi.serviceLogic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.FieldType;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.GrouperSourceAdapter;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.MembershipFinder;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.Stem.Scope;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.app.deprovisioning.DeprovisionedSubject;
import edu.internet2.middleware.grouper.app.deprovisioning.GrouperDeprovisioningAffiliation;
import edu.internet2.middleware.grouper.app.deprovisioning.GrouperDeprovisioningAttributeNames;
import edu.internet2.middleware.grouper.app.deprovisioning.GrouperDeprovisioningAttributeValue;
import edu.internet2.middleware.grouper.app.deprovisioning.GrouperDeprovisioningConfiguration;
import edu.internet2.middleware.grouper.app.deprovisioning.GrouperDeprovisioningEmailService;
import edu.internet2.middleware.grouper.app.deprovisioning.GrouperDeprovisioningEmailService.EmailPerPerson;
import edu.internet2.middleware.grouper.app.deprovisioning.GrouperDeprovisioningJob;
import edu.internet2.middleware.grouper.app.deprovisioning.GrouperDeprovisioningLogic;
import edu.internet2.middleware.grouper.app.deprovisioning.GrouperDeprovisioningOverallConfiguration;
import edu.internet2.middleware.grouper.app.deprovisioning.GrouperDeprovisioningSettings;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignable;
import edu.internet2.middleware.grouper.audit.AuditEntry;
import edu.internet2.middleware.grouper.audit.AuditTypeBuiltin;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiDeprovisioningMembershipSubjectContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiGroup;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiMember;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiMembershipSubjectContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiSubject;
import edu.internet2.middleware.grouper.grouperUi.beans.dojo.DojoComboLogic;
import edu.internet2.middleware.grouper.grouperUi.beans.dojo.DojoComboQueryLogicBase;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiResponseJs;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction.GuiMessageType;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.AttestationContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.DeprovisioningContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GroupContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GrouperRequestContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.TextContainer;
import edu.internet2.middleware.grouper.hibernate.AuditControl;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.hibernate.HibernateHandler;
import edu.internet2.middleware.grouper.hibernate.HibernateHandlerBean;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.membership.MembershipResult;
import edu.internet2.middleware.grouper.membership.MembershipSubjectContainer;
import edu.internet2.middleware.grouper.membership.MembershipType;
import edu.internet2.middleware.grouper.misc.GrouperObject;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.AttributeDefPrivilege;
import edu.internet2.middleware.grouper.subj.SubjectHelper;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.ui.util.GrouperUiUtils;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Source;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectNotUniqueException;
import edu.internet2.middleware.subject.provider.SourceManager;

/**
 * 
 */
public class UiV2Deprovisioning {
  
  /** logger */
  private static final Log LOG = GrouperUtil.getLog(UiV2Deprovisioning.class);
  
  /**
   * make sure attribute def is there and enabled etc
   * @return true if k
   */
  private boolean checkDeprovisioning() {
    
    final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();

    AttestationContainer attestationContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getAttestationContainer();

    if (!GrouperDeprovisioningSettings.deprovisioningEnabled()) {
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
          TextContainer.retrieveFromRequest().getText().get("deprovisioningNotEnabledError")));
      return false;
    }

    AttributeDef attributeDefBase = null;
    try {
      
      attributeDefBase = GrouperDeprovisioningAttributeNames.retrieveAttributeDefBaseDef();

      //init all the attestation stuff
      attestationContainer.getGuiAttestation();

    } catch (RuntimeException e) {
      if (attributeDefBase == null) {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("deprovisioningAttributeNotFoundError")));
        return false;
      }
      throw e;
    }
    
    return true;
  }
  /**
   * @param request
   * @param FINISHED
   * @param deprovisioningContainer
   * @param guiScreenActions
   * @param hasError
   * @return
   */
  private boolean deprovisioningOnObjectEditSaveHelper(final HttpServletRequest request,
      final boolean[] FINISHED, final DeprovisioningContainer deprovisioningContainer,
      List<GuiScreenAction> guiScreenActions, boolean hasError, final GrouperObject grouperObject) {
    String affiliation = request.getParameter("grouperDeprovisioningHasAffiliationName");
    deprovisioningContainer.setAffiliation(affiliation);
    if (StringUtils.isBlank(affiliation)) {
      guiScreenActions.add(GuiScreenAction.newValidationMessage(GuiMessageType.error, "#grouperDeprovisioningHasAffiliationId",
          TextContainer.retrieveFromRequest().getText().get("deprovisioningAffiliationRequired")));
      hasError = true;
    }
    
    GrouperDeprovisioningAttributeValue grouperDeprovisioningAttributeValue = deprovisioningContainer.getGrouperDeprovisioningAttributeValueNew();
    
    if (!hasError) {

      GrouperDeprovisioningConfiguration grouperDeprovisioningConfiguration = deprovisioningContainer.getGrouperDeprovisioningOverallConfiguration().getAffiliationToConfiguration().get(affiliation);

      // remove assignment
      if (!grouperDeprovisioningAttributeValue.isDirectAssignment()) {
        grouperDeprovisioningConfiguration.clearOutConfigurationButLeaveMetadata();
      }
      
      grouperDeprovisioningConfiguration.storeConfiguration();

      if (!grouperDeprovisioningAttributeValue.isDirectAssignment()) {
        //update for this object since now potentially inherited
        GrouperDeprovisioningLogic.updateDeprovisioningMetadataForSingleObject(grouperObject);
      }

      if (grouperObject instanceof Stem) {
        final RuntimeException[] RUNTIME_EXCEPTION = new RuntimeException[1];
        Thread thread = new Thread(new Runnable() {
  
          public void run() {
  
            try {
              GrouperSession.startRootSession();
  
              //since this is a folder, we need to update things for this object
              GrouperDeprovisioningLogic.updateDeprovisioningMetadata((Stem)grouperObject);
  
              FINISHED[0] = true;
            } catch (RuntimeException re) {
              //log incase thread didnt finish when screen was drawing
              LOG.error("Error updating deprovisioning stem parts", re);
              RUNTIME_EXCEPTION[0] = re;
            }
            
          }
          
        });
  
        thread.start();
        
        try {
          thread.join(30000);
        } catch (InterruptedException ie) {
          throw new RuntimeException(ie);
        }
  
        if (RUNTIME_EXCEPTION[0] != null) {
          throw RUNTIME_EXCEPTION[0];
        }
      }
    }
    return hasError;
  }

  /**
   * delete
   * @param request
   * @param response
   */
  public void xxx_deprovisioningReportOnFolder(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    Stem stem = null;

    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
      
      stem = UiV2Stem.retrieveStemHelper(request, true).getStem();
      
      if (stem == null) {
        return;
      }
      
      if (!xxx_deprovisioningReportOnObjectHelper(stem)) {
        return;
      }
  
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }

  }

  /**
   * @param request
   * @param response
   */
  public void xxx_deprovisioningReportOnFolderSubmit(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    Stem stem = null;

    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
      
      stem = UiV2Stem.retrieveStemHelper(request, true).getStem();
      
      if (stem == null) {
        return;
      }
      
      final Set<String> memberIds = new HashSet<String>();
      
      for (int i=0;i<10000;i++) {
        String memberId = request.getParameter("memberIds_" + i + "[]");
        if (!StringUtils.isBlank(memberId)) {
          memberIds.add(memberId);
        }
      }
      
      final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      
      if (GrouperUtil.length(memberIds) > 0) {
        
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("deprovisioningNoMembersSelected")));
        
        return;
      }
      
      final Stem STEM = stem;
      
      GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        @Override
        public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
          
          int failures = 0;

          for (String memberId : GrouperUtil.nonNull(memberIds)) {
            try {
              
              Member member = MemberFinder.findByUuid(grouperSession, memberId, true);
              Subject subject = member.getSubject();
              GrouperDeprovisioningLogic.removeAccess(STEM, subject);
              
            } catch (Exception e) {
              LOG.error("Error with removing priv: " + memberId + ", " + STEM.getName(), e);
              failures++;
            }
          }
          
          if (failures == 0) {
            
            guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2Deprovisioning.deprovisioningReportOnFolder&stemId=" + STEM.getId() + "')"));
            
            guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
                TextContainer.retrieveFromRequest().getText().get("")));
          } else {
            guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
                TextContainer.retrieveFromRequest().getText().get("deprovisioningDeprovisionFromReportError")));
          }
          return null;
        }
      });
      
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * @param request
   * @param response
   */
  public void xxx_deprovisioningReportOnAttributeDefSubmit(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    AttributeDef attributeDef = null;
    
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      attributeDef = UiV2AttributeDef.retrieveAttributeDefHelper(request, AttributeDefPrivilege.ATTR_READ, true).getAttributeDef();

      if (attributeDef != null) {
        attributeDef = UiV2AttributeDef.retrieveAttributeDefHelper(request, AttributeDefPrivilege.ATTR_UPDATE, true).getAttributeDef();
      }
      
      if (attributeDef == null) {
        return;
      }
      
      final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      
      final AttributeDef ATTRIBUTE_DEF = attributeDef;
      
      final Set<String> memberIds = new HashSet<String>();
      
      for (int i=0;i<10000;i++) {
        String memberId = request.getParameter("memberIds_" + i + "[]");
        if (!StringUtils.isBlank(memberId)) {
          memberIds.add(memberId);
        }
      }
      
      if (GrouperUtil.length(memberIds) > 0) {
        
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("deprovisioningNoMembersSelected")));
        
        return;
      }
      
      GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        @Override
        public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
          
          int failures = 0;

          for (String memberId : GrouperUtil.nonNull(memberIds)) {
            try {
              
              Member member = MemberFinder.findByUuid(grouperSession, memberId, true);
              Subject subject = member.getSubject();
              GrouperDeprovisioningLogic.removeAccess(ATTRIBUTE_DEF, subject);
              
            } catch (Exception e) {
              LOG.error("Error with removing priv: " + memberId + ", " + ATTRIBUTE_DEF.getName(), e);
              failures++;
            }
          }
          
          if (failures == 0) {
            
            guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2Deprovisioning.deprovisioningReportOnAttributeDef&attributeDefId=" + ATTRIBUTE_DEF.getId() + "')"));
            
            guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
                TextContainer.retrieveFromRequest().getText().get("")));
          } else {
            guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
                TextContainer.retrieveFromRequest().getText().get("deprovisioningDeprovisionFromReportError")));
          }
          return null;
        }
      });
      
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * @param request
   * @param response
   */
  public void deprovisioningOnGroupReportSubmit(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    Group group = null;
    
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      group = UiV2Group.retrieveGroupHelper(request, AccessPrivilege.UPDATE).getGroup();
      
      if (group != null) {
        group = UiV2Group.retrieveGroupHelper(request, AccessPrivilege.READ).getGroup();
      }
      
      if (group == null) {
        return;
      }
      
      final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      
      final Group GROUP = group;
      
      final Set<String> memberIds = new HashSet<String>();
      
      for (int i=0;i<10000;i++) {
        String memberId = request.getParameter("memberIds_" + i + "[]");
        if (!StringUtils.isBlank(memberId)) {
          memberIds.add(memberId);
        }
      }
      
      if (GrouperUtil.length(memberIds) == 0) {
        
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("deprovisioningNoMembersSelected")));
        
        return;
      }
      
      GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        @Override
        public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
          
          int failures = 0;

          for (String memberId : GrouperUtil.nonNull(memberIds)) {
            try {
              
              Member member = MemberFinder.findByUuid(grouperSession, memberId, true);
              Subject subject = member.getSubject();
              GrouperDeprovisioningLogic.removeAccess(GROUP, subject);
              
            } catch (Exception e) {
              LOG.error("Error with removing priv: " + memberId + ", " + GROUP.getName(), e);
              failures++;
            }
          }
          
          if (failures == 0) {
            
            deprovisioningOnGroupReport(request, response);
            
            guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
                TextContainer.retrieveFromRequest().getText().get("deprovisioningDeprovisionFromReportSuccess")));
          } else {
            guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
                TextContainer.retrieveFromRequest().getText().get("deprovisioningDeprovisionFromReportError")));
          }
          return null;
        }
      });
      
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * do a report on an object
   * @param grouperObject 
   * @return true to show report, false to not
   */
  private boolean xxx_deprovisioningReportOnObjectHelper(final GrouperObject grouperObject) {
    
    final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    final DeprovisioningContainer deprovisioningContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getDeprovisioningContainer();
    
    //switch over to admin so attributes work
    boolean shouldContinue = (Boolean)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      
      @Override
      public Object callback(GrouperSession theGrouperSession) throws GrouperSessionException {
        
        if (!checkDeprovisioning()) {
          return false;
        }
        
        if (!deprovisioningContainer.isCanWriteDeprovisioning()) {
          guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
              TextContainer.retrieveFromRequest().getText().get("deprovisioningNotAllowedToWriteDeprovisioningFolder")));
          return false;
        }
  
        //get all deprovisioned users for realms where deprovisioning is on for this object
        //are there any?
        GrouperDeprovisioningOverallConfiguration grouperDeprovisioningOverallConfiguration = GrouperDeprovisioningOverallConfiguration.retrieveConfiguration(grouperObject);
        
        Set<String> affiliationsWithDeprovisioning = new TreeSet<String>();
        
        for (String affiliation : GrouperDeprovisioningAffiliation.retrieveAllAffiliations().keySet()) {
          
          GrouperDeprovisioningConfiguration grouperDeprovisioningConfiguration = grouperDeprovisioningOverallConfiguration.getAffiliationToConfiguration().get(affiliation);
  
          // we good
          GrouperDeprovisioningAttributeValue originalConfig = grouperDeprovisioningConfiguration.getOriginalConfig();
          if (originalConfig != null && originalConfig.isDeprovision()) {
            affiliationsWithDeprovisioning.add(affiliation);
          }
        }
        
        if (GrouperUtil.length(affiliationsWithDeprovisioning) == 0) {
          guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
              TextContainer.retrieveFromRequest().getText().get("deprovisioningNoAffiliationsDeprovisionForThisObject")));
          return false;
        }
        
        return true;
      }
    });
    return shouldContinue;
  }
  
  /**
   * 
   * @param request
   * @param response
   */
  public void deprovisioningOnFolderEditSave(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    Stem stem = null;

    final boolean[] FINISHED = new boolean[]{false};

    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
      
      stem = UiV2Stem.retrieveStemHelper(request, true).getStem();
      
      if (stem == null) {
        return;
      }
      
      final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      final DeprovisioningContainer deprovisioningContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getDeprovisioningContainer();
      
      //switch over to admin so attributes work
      boolean shouldContinue = (Boolean)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        @Override
        public Object callback(GrouperSession theGrouperSession) throws GrouperSessionException {
          
          if (!checkDeprovisioning()) {
            return false;
          }
          
          if (!deprovisioningContainer.isCanWriteDeprovisioning()) {
            guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
                TextContainer.retrieveFromRequest().getText().get("deprovisioningNotAllowedToWriteDeprovisioningFolder")));
            return false;
          }

          return true;
        }
      });
      
      if (!shouldContinue) {
        return;
      }
  
      if (!deprovisionOnObjectEditHelper(request, response, true, stem)) {
        return;
      }
      final Stem STEM = stem;
      //switch over to admin so attributes work
      GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        @Override
        public Object callback(GrouperSession theGrouperSession) throws GrouperSessionException {
          
          List<GuiScreenAction> guiScreenActions = new ArrayList<GuiScreenAction>();
          boolean hasError = false;
          
          hasError = UiV2Deprovisioning.this.deprovisioningOnObjectEditSaveHelper(request, FINISHED, deprovisioningContainer, guiScreenActions, hasError, STEM);
          
          if (!hasError) {
            guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2Deprovisioning.deprovisioningOnFolder&stemId=" + STEM.getId() + "')"));

            if (!FINISHED[0]) {
              guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
                  TextContainer.retrieveFromRequest().getText().get("deprovisioningEditSaveSuccessNotFinished")));

            } else {
              guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
                  TextContainer.retrieveFromRequest().getText().get("deprovisioningEditSaveSuccess")));

            }

          } else {
            
            guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
                "/WEB-INF/grouperUi2/deprovisioning/deprovisioningFolderSettingsEdit.jsp"));
            
            //add these after screen drawn
            for (GuiScreenAction guiScreenAction : guiScreenActions) {
              guiResponseJs.addAction(guiScreenAction);
            }
            
          }

          return null;
        }
      });

    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }


  }



  /**
   * process inputs on a save or on ajax
   * @param request 
   * @param response 
   * @param onSubmit 
   * @param attributeAssignable
   * @return true if proceed to page, false to just return an error message
   */
  private boolean deprovisionOnObjectEditHelper(final HttpServletRequest request, final HttpServletResponse response, final boolean onSubmit, 
      final AttributeAssignable attributeAssignable) {
    
    GrouperSession grouperSession = GrouperSession.staticGrouperSession();
  
    try {
  
      final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      
      final DeprovisioningContainer deprovisioningContainer = GrouperRequestContainer
          .retrieveFromRequestOrCreate().getDeprovisioningContainer();

      //switch over to admin so attributes work
      boolean shouldContinue = (Boolean)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        @Override
        public Object callback(GrouperSession theGrouperSession) throws GrouperSessionException {
          
          setupDeprovisioningConfiguration(attributeAssignable);
          
          String affiliation = request.getParameter("grouperDeprovisioningHasAffiliationName");
          deprovisioningContainer.setAffiliation(affiliation);
          return true;
        }
      });
      
      if (!shouldContinue) {
        return false;
      }

      boolean switchedAffiliation = false;
      {
        String previousAffiliation = request.getParameter("grouperDeprovisioningPreviousAffiliationName");
        String affiliation = request.getParameter("grouperDeprovisioningHasAffiliationName");
        if (!StringUtils.isBlank(previousAffiliation)) {
          switchedAffiliation = !StringUtils.equals(affiliation, previousAffiliation);
        }
      }      
      final boolean SWITCHED_AFFILIATION = switchedAffiliation;

      String groupIdToEmail = null;
      String groupNameToEmail = null;
      Group groupToEmail = null;
      
      {
        String groupIdOrName = SWITCHED_AFFILIATION ? null : request.getParameter("grouperDeprovisioningEmailGroupIdMembersName");
        if (groupIdOrName == null) {
          
          if (!StringUtils.isBlank(deprovisioningContainer.getAffiliation())) {

            groupIdOrName = GrouperRequestContainer.retrieveFromRequestOrCreate().getDeprovisioningContainer()
                .getGrouperDeprovisioningAttributeValueNew().getMailToGroupString();
          }
        }
        if (!StringUtils.isBlank(groupIdOrName)) {
          
          //lets see if user can find the group, and can READ it
          groupToEmail = new GroupFinder().addGroupId(groupIdOrName).assignPrivileges(AccessPrivilege.READ_PRIVILEGES).findGroup();
          
          if (groupToEmail == null) {
            groupToEmail = new GroupFinder().addGroupName(groupIdOrName).assignPrivileges(AccessPrivilege.READ_PRIVILEGES).findGroup();
          }
          if (groupToEmail != null) {
            groupIdToEmail = groupToEmail.getId();
            groupNameToEmail = groupToEmail.getName();
            deprovisioningContainer.setGrouperDeprovisioningEmailGuiGroup(new GuiGroup(groupToEmail));

          }
        }
      }

      final Group GROUP_TO_EMAIL = groupToEmail;
      final String GROUP_ID_TO_EMAIL = groupIdToEmail;
      final String GROUP_NAME_TO_EMAIL = groupNameToEmail;
      
      //switch over to admin so attributes work
      return (Boolean)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        @Override
        public Object callback(GrouperSession theGrouperSession) throws GrouperSessionException {
          
          DeprovisioningContainer deprovisioningContainer = GrouperRequestContainer
              .retrieveFromRequestOrCreate().getDeprovisioningContainer();
          
          if (!StringUtils.isBlank(deprovisioningContainer.getAffiliation())) {
            
            boolean hasExistingConfiguration = deprovisioningContainer.getGrouperDeprovisioningOverallConfiguration().hasConfigurationForAffiliation(deprovisioningContainer.getAffiliation());
            
            GrouperDeprovisioningAttributeValue grouperDeprovisioningAttributeValue = deprovisioningContainer.getGrouperDeprovisioningAttributeValueNew();

            if (!hasExistingConfiguration) {
              grouperDeprovisioningAttributeValue.setDeprovision(true);
            }

            if (!SWITCHED_AFFILIATION) {
              Boolean hasConfiguration = GrouperUtil.booleanObjectValue(request.getParameter("grouperDeprovisioningHasConfigurationName"));
              if (hasConfiguration != null) {
                grouperDeprovisioningAttributeValue.setDirectAssignment(GrouperUtil.booleanValue(hasConfiguration, false));
              }
              
              Boolean deprovision = GrouperUtil.booleanObjectValue(request.getParameter("grouperDeprovisioningDeprovisionName"));
              if (deprovision != null) {
                grouperDeprovisioningAttributeValue.setDeprovision(deprovision);
                
              }
              
              Scope scope = Scope.valueOfIgnoreCase(request.getParameter("grouperDeprovisioningStemScopeName"), false);
              if (scope != null) {
                grouperDeprovisioningAttributeValue.setStemScope(scope);
              }
              
              Boolean sendEmail = GrouperUtil.booleanObjectValue(request.getParameter("grouperDeprovisioningSendEmailName"));
              if (sendEmail != null) {
                grouperDeprovisioningAttributeValue.setSendEmail(GrouperUtil.booleanValue(sendEmail, false));
              }
              
              if (sendEmail != null && sendEmail) {

                if (GrouperUtil.booleanValue(request.getParameter("grouperDeprovisioningHasEmailBodyName"), false)) {
                  String emailBody = request.getParameter("grouperDeprovisioningEmailBodyName");
                  grouperDeprovisioningAttributeValue.setEmailBodyString(StringUtils.trimToNull(emailBody));
                }
                
                Boolean emailManagers = GrouperUtil.booleanObjectValue(request.getParameter("grouperDeprovisioningEmailManagersName"));
                
                if (emailManagers != null && emailManagers) {
                  grouperDeprovisioningAttributeValue.setEmailAddressesString(null);
                  grouperDeprovisioningAttributeValue.setMailToGroupString(null);
                }
                if (emailManagers != null && !emailManagers) {
                  grouperDeprovisioningAttributeValue.setEmailManagers(false);
                }
                
                if (!grouperDeprovisioningAttributeValue.isEmailManagers()) {
                  
                  Boolean emailGroupMembers = GrouperUtil.booleanObjectValue(request.getParameter("grouperDeprovisioningEmailGroupMembersName"));
                  
                  if (emailGroupMembers != null && emailGroupMembers) {
                    grouperDeprovisioningAttributeValue.setEmailAddressesString(null);
                    grouperDeprovisioningAttributeValue.setEmailGroupMembers(true);
                  }
                  if (emailGroupMembers != null && !emailGroupMembers) {
                    grouperDeprovisioningAttributeValue.setEmailGroupMembers(false);
                  }
                  
                  if (!grouperDeprovisioningAttributeValue.isEmailGroupMembers()) {
                    String emailAddresses = request.getParameter("grouperDeprovisioningEmailAddressesName");
                    grouperDeprovisioningAttributeValue.setEmailAddressesString(emailAddresses);
                  } else {
                    String groupIdOrName = request.getParameter("grouperDeprovisioningEmailGroupIdMembersName");
                    if (StringUtils.isBlank(groupIdOrName)) {
                      
                      if (onSubmit) {
                        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, 
                            "#grouperDeprovisioningEmailGroupIdMembersId",
                            TextContainer.retrieveFromRequest().getText().get("deprovisioningGroupIdIsBlank")));
                        
                        return false;
                      }
                    } else {
  
                      if (GROUP_TO_EMAIL == null) {
                        
                        if (onSubmit) {
                          guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error,
                            "#grouperDeprovisioningEmailGroupIdMembersId",
                            TextContainer.retrieveFromRequest().getText().get("deprovisioningGroupIdNotFound")));
                        
                          return false;
                        }
                        
                      } else {
                        grouperDeprovisioningAttributeValue.setMailToGroupString(groupIdOrName);
                      }
                      
                    }
                    
                  }
                  
                }
              } 
              
              Boolean showForRemovalBoolean = GrouperUtil.booleanObjectValue(request.getParameter("grouperDeprovisioningShowForRemovalName"));
              
              if (showForRemovalBoolean != null) {
                grouperDeprovisioningAttributeValue.setShowForRemoval(showForRemovalBoolean);
                
                if (!showForRemovalBoolean) {
                  grouperDeprovisioningAttributeValue.setAutoselectForRemoval(null);
                } else {
                  
                  Boolean autoselectForRemovalBoolean = GrouperUtil.booleanObjectValue(request.getParameter("grouperDeprovisioningAutoselectForRemovalName"));
                  
                  if (autoselectForRemovalBoolean != null) {
                    grouperDeprovisioningAttributeValue.setAutoselectForRemoval(autoselectForRemovalBoolean);
                  }                
                  
                }
                
              }
              
              Boolean allowAddsBoolean = GrouperUtil.booleanObjectValue(request.getParameter("grouperDeprovisioningAllowAddsName"));
              
              if (allowAddsBoolean != null) {
                grouperDeprovisioningAttributeValue.setAllowAddsWhileDeprovisioned(allowAddsBoolean);
              }
              
              Boolean autoChangeLoaderBoolean = GrouperUtil.booleanObjectValue(request.getParameter("grouperDeprovisioningAutochangeLoaderName"));
              
              if (autoChangeLoaderBoolean != null) {
                grouperDeprovisioningAttributeValue.setAutoChangeLoader(autoChangeLoaderBoolean);
              }
              
            }            
          }
          
          return true;
        }
      });
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }


  }
  
  /**
   * 
   * @param request
   * @param response
   */
  public void deprovisioningOnFolderEdit(final HttpServletRequest request, final HttpServletResponse response) {
    
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
      final DeprovisioningContainer deprovisioningContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getDeprovisioningContainer();
      
      //switch over to admin so attributes work
      boolean shouldContinue = (Boolean)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        @Override
        public Object callback(GrouperSession theGrouperSession) throws GrouperSessionException {
          
          if (!checkDeprovisioning()) {
            return false;
          }
          
          if (!deprovisioningContainer.isCanWriteDeprovisioning()) {
            guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
                TextContainer.retrieveFromRequest().getText().get("deprovisioningNotAllowedToWriteDeprovisioningFolder")));
            return false;
          }

          return true;
        }
      });
      
      if (!shouldContinue) {
        return;
      }
      
      if (!deprovisionOnObjectEditHelper(request, response, false, stem)) {
        return;
      }


      //switch over to admin so attributes work
      GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        @Override
        public Object callback(GrouperSession theGrouperSession) throws GrouperSessionException {
          
          
          guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
              "/WEB-INF/grouperUi2/deprovisioning/deprovisioningFolderSettingsEdit.jsp"));
          
          return null;
        }
      });
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }

  
  /**
   * 
   * @param request
   * @param response
   */
  public void deprovisioningOnFolder(final HttpServletRequest request, HttpServletResponse response) {
    
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
      
      deprovisionOnObjectHelper(stem);
  
      //switch over to admin so attributes work
      GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        @Override
        public Object callback(GrouperSession theGrouperSession) throws GrouperSessionException {
          
          if (!checkDeprovisioning()) {
            return null;
          }
            
          guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
              "/WEB-INF/grouperUi2/deprovisioning/deprovisioningFolderSettingsView.jsp"));
          
          return null;
        }
      });
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }


  }
  
  /**
   * 
   * @param attributeAssignable
   */
  private void deprovisionOnObjectHelper(final AttributeAssignable attributeAssignable) {

    final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();

    //switch over to admin so attributes work
    GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      
      @Override
      public Object callback(GrouperSession theGrouperSession) throws GrouperSessionException {
        
        if (!checkDeprovisioning()) {
          return null;
        }
        
        DeprovisioningContainer deprovisioningContainer = GrouperRequestContainer
            .retrieveFromRequestOrCreate().getDeprovisioningContainer();
        
        if (!deprovisioningContainer.isCanReadDeprovisioning()) {
          guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
              TextContainer.retrieveFromRequest().getText().get("deprovisioningNotAllowedToReadDeprovisioningFolder")));
        }
        
        setupDeprovisioningConfiguration(attributeAssignable);
        
        return null;
      }
    });
    
    
    for (String affiliation : GrouperDeprovisioningAffiliation.retrieveAllAffiliations().keySet()) {

      Group groupToEmail = null;
      GrouperRequestContainer.retrieveFromRequestOrCreate().getDeprovisioningContainer().setAffiliation(affiliation);
      String groupIdOrName = GrouperRequestContainer.retrieveFromRequestOrCreate().getDeprovisioningContainer()
          .getGrouperDeprovisioningAttributeValueNew().getMailToGroupString();
      if (!StringUtils.isBlank(groupIdOrName)) {
        
        //lets see if user can find the group, and can READ it
        groupToEmail = new GroupFinder().addGroupId(groupIdOrName).assignPrivileges(AccessPrivilege.READ_PRIVILEGES).findGroup();
        
        if (groupToEmail == null) {
          groupToEmail = new GroupFinder().addGroupName(groupIdOrName).assignPrivileges(AccessPrivilege.READ_PRIVILEGES).findGroup();
        }
        if (groupToEmail != null) {
          DeprovisioningContainer deprovisioningContainer = GrouperRequestContainer
              .retrieveFromRequestOrCreate().getDeprovisioningContainer();
          deprovisioningContainer.setGrouperDeprovisioningEmailGuiGroup(new GuiGroup(groupToEmail));
        }
      }
    }

  }
  
  /**
   * setup deprovisioning view on attribute assignable (folder, group, attribute)
   * @param attributeAssignable
   */
  private static void setupDeprovisioningConfiguration(AttributeAssignable attributeAssignable) {

    GrouperRequestContainer grouperRequestContainer = GrouperRequestContainer.retrieveFromRequestOrCreate();
    DeprovisioningContainer deprovisioningContainer = grouperRequestContainer.getDeprovisioningContainer();

    deprovisioningContainer.getGrouperDeprovisioningOverallConfiguration();
  }
  
  /**
   * main deprovisioning link
   * @param request
   * @param response
   */
  public void deprovisioningMain(HttpServletRequest request, HttpServletResponse response) {

    final GrouperRequestContainer grouperRequestContainer = GrouperRequestContainer.retrieveFromRequestOrCreate();
    final DeprovisioningContainer deprovisioningContainer = grouperRequestContainer.getDeprovisioningContainer();
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      deprovisioningContainer.assertDeprovisioningEnabledAndAllowed();
      
      final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
            
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/deprovisioning/deprovisioningMain.jsp"));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }

  }
  
  
  /**
   * combo filter
   * @param request
   * @param response
   */
  public void addMemberFilter(HttpServletRequest request, HttpServletResponse response) {
  
    //run the combo logic
    DojoComboLogic.logic(request, response, new DojoComboQueryLogicBase<Subject>() {
  
      /**
       */
      @Override
      public Subject lookup(HttpServletRequest localRequest, GrouperSession grouperSession, String query) {
  
        //when we refer to subjects in the dropdown, we will use a sourceId / subject tuple
        
        Subject subject = null;
            
        try {
          GrouperSourceAdapter.searchForGroupsWithReadPrivilege(true);
          if (query != null && query.contains("||")) {
            String sourceId = GrouperUtil.prefixOrSuffix(query, "||", true);
            String subjectId = GrouperUtil.prefixOrSuffix(query, "||", false);
            subject =  SubjectFinder.findByIdOrIdentifierAndSource(subjectId, sourceId, false);
          } else {
            try { 
              subject = SubjectFinder.findByIdOrIdentifier(query, false);
            } catch (SubjectNotUniqueException snue) {
              //ignore this...
              if (LOG.isDebugEnabled()) {
                LOG.debug("Find by id or identifier not unique: '" + query + "'");
              }
            }
          }
        } finally {
          GrouperSourceAdapter.clearSearchForGroupsWithReadPrivilege();
        }

        //dont do groups or internal
        if (subject != null && !SubjectHelper.inSourceList(GrouperDeprovisioningSettings.retrieveSourcesAllowedToDeprovision(), subject.getSource())) {
          subject = null;
        }
        
        return subject;
      }
  
      /**
       * 
       */
      @Override
      public Collection<Subject> search(HttpServletRequest localRequest, GrouperSession grouperSession, String query) {
        
        try {
          GrouperSourceAdapter.searchForGroupsWithReadPrivilege(true);
          Collection<Subject> results = 
              SubjectFinder.findPage(query, GrouperDeprovisioningSettings.retrieveSourcesAllowedToDeprovision()).getResults();
          return results;
        } finally {
          GrouperSourceAdapter.clearSearchForGroupsWithReadPrivilege();
        }
      
      }
  
      /**
       * 
       * @param t
       * @return source with id
       */
      @Override
      public String retrieveId(GrouperSession grouperSession, Subject t) {
        return t.getSourceId() + "||" + t.getId();
      }
      
      /**
       * 
       */
      @Override
      public String retrieveLabel(GrouperSession grouperSession, Subject t) {
        return new GuiSubject(t).getScreenLabelLong();
      }
  
      /**
       * 
       */
      @Override
      public String retrieveHtmlLabel(GrouperSession grouperSession, Subject t) {
        String value = new GuiSubject(t).getScreenLabelLongWithIcon();
        return value;
      }
  
      /**
       * 
       */
      @Override
      public String initialValidationError(HttpServletRequest localRequest, GrouperSession grouperSession) {
  
        //MCH 20140316
        //Group group = retrieveGroupHelper(request, AccessPrivilege.UPDATE).getGroup();
        //
        //if (group == null) {
        //  
        //  return "Not allowed to edit group";
        //}
        //
        return null;
      }
    });
  
              
  }
  /**
   * search for a subject to add to the group
   * @param request
   * @param response
   */
  public void addMemberSearch(HttpServletRequest request, HttpServletResponse response) {
  
    GrouperRequestContainer grouperRequestContainer = GrouperRequestContainer.retrieveFromRequestOrCreate();
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
    GrouperSession grouperSession = null;
    
    try {
      grouperSession = GrouperSession.start(loggedInSubject);
  
  
      GroupContainer groupContainer = grouperRequestContainer.getGroupContainer();
  
      String searchString = request.getParameter("addMemberSubjectSearch");
      
      boolean searchOk = GrouperUiUtils.searchStringValid(searchString);
      if (!searchOk) {
        
        guiResponseJs.addAction(GuiScreenAction.newInnerHtml("#addMemberResults", 
            TextContainer.retrieveFromRequest().getText().get("groupAddMemberNotEnoughChars")));
        return;
      }
  
      String matchExactIdString = request.getParameter("matchExactId[]");
      boolean matchExactId = GrouperUtil.booleanValue(matchExactIdString, false);
  
      String sourceId = request.getParameter("sourceId");
      
      Set<Subject> subjects = null;
      if (matchExactId) {
        if (GrouperConfig.retrieveConfig().propertyValueBoolean("grouperQuerySubjectsMultipleQueriesCommaSeparated", true)) {
          Set<String> searchStrings = GrouperUtil.splitTrimToSet(searchString, ",");
          if (StringUtils.equals("all", sourceId)) {
            subjects = new LinkedHashSet<Subject>(GrouperUtil.nonNull(SubjectFinder.findByIdsOrIdentifiers(searchStrings, GrouperDeprovisioningSettings.retrieveSourcesAllowedToDeprovision())).values());
          } else {
            subjects = new LinkedHashSet<Subject>(GrouperUtil.nonNull(SubjectFinder.findByIdsOrIdentifiers(searchStrings, sourceId)).values());
          }
        } else {
          Subject subject = null;
          if (StringUtils.equals("all", sourceId)) {
            try {
              subject = SubjectFinder.findByIdOrIdentifier(searchString, false);
            } catch (SubjectNotUniqueException snue) {
              //ignore
            }
          } else {
            subject = SubjectFinder.findByIdOrIdentifierAndSource(searchString, sourceId, false);
          }
  
          subjects = new LinkedHashSet<Subject>();
          if (subject != null) {
            subjects.add(subject);
          }
        }
      } else {
        Set<Source> sources = null;
        if (StringUtils.equals("all", sourceId)) {
          sources = GrouperDeprovisioningSettings.retrieveSourcesAllowedToDeprovision();
        } else {
          sources = GrouperUtil.toSet(SourceManager.getInstance().getSource(sourceId));
        }
        subjects = SubjectFinder.findPage(searchString, sources).getResults();
      }
      
      if (GrouperUtil.length(subjects) == 0) {
  
        guiResponseJs.addAction(GuiScreenAction.newInnerHtml("#addMemberResults", 
            TextContainer.retrieveFromRequest().getText().get("groupAddMemberNoSubjectsFound")));
        return;
      }
      
      Set<GuiSubject> guiSubjects = GuiSubject.convertFromSubjects(subjects, "uiV2.subjectSearchResults", 30);
      
      groupContainer.setGuiSubjectsAddMember(guiSubjects);
  
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#addMemberResults", 
          "/WEB-INF/grouperUi2/group/addMemberResults.jsp"));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
  /**
   * show user access to deprovision
   * @param request
   * @param response
   */
  public void deprovisionUserSubmit(HttpServletRequest request, HttpServletResponse response) {
    
    final GrouperRequestContainer grouperRequestContainer = GrouperRequestContainer.retrieveFromRequestOrCreate();
    final DeprovisioningContainer deprovisioningContainer = grouperRequestContainer.getDeprovisioningContainer();
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      deprovisioningContainer.assertDeprovisioningEnabledAndAllowed();
      
      final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      
      String subjectString = request.getParameter("groupAddMemberComboName");
      
      if (StringUtils.isBlank(subjectString)) {
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, 
            "#groupAddMemberComboId",
            TextContainer.retrieveFromRequest().getText().get("deprovisioningNoAffiliationSelected")));
        return;
      }
      
      GrouperDeprovisioningAffiliation  deprovisioningAffiliation = retrieveAffiliation(request, loggedInSubject, true);
      
      if (deprovisioningAffiliation == null) {
        return;
      }
      
      deprovisioningContainer.setAffiliation(deprovisioningAffiliation.getLabel());
      
      Subject subject = null;
      
      if (subjectString != null && subjectString.contains("||")) {
        String sourceId = GrouperUtil.prefixOrSuffix(subjectString, "||", true);
        String subjectId = GrouperUtil.prefixOrSuffix(subjectString, "||", false);
        subject =  SubjectFinder.findByIdOrIdentifierAndSource(subjectId, sourceId, false);

      } else {
        try {
          subject = SubjectFinder.findByIdOrIdentifier(subjectString, false);
        } catch (SubjectNotUniqueException snue) {
          //ignore
        }
          
      }

      // must be in a certain source
      if (subject != null && !SubjectHelper.inSourceList(GrouperDeprovisioningSettings.retrieveSourcesAllowedToDeprovision(), subject.getSource())) {
        subject = null;
      }
      
      if (subject == null) {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("deprovisioningCantFindSubject")));
        return;
      }
      
      final Subject SUBJECT = subject;
      
      GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        @Override
        public Object callback(GrouperSession grouperSession) throws GrouperSessionException {

          Member member = MemberFinder.findBySubject(grouperSession, SUBJECT, true);
          deprovisioningContainer.setDeprovisionedMemberId(member.getId());
          
          Set<MembershipSubjectContainer> membershipSubjectContainers = new HashSet<MembershipSubjectContainer>();
          
          Set<MembershipSubjectContainer> membershipSubjectContainersSet = MembershipFinder.findAllImmediateMemberhipSubjectContainers(grouperSession, SUBJECT);
          
          OUTER: for (MembershipSubjectContainer membershipSubjectContainer : membershipSubjectContainersSet) {
              
            //see if already there, update the membership
            for (MembershipSubjectContainer existingSubjectContainer : membershipSubjectContainersSet) {
              if (shouldMembershipSubjectContainerBeMerged(membershipSubjectContainer, existingSubjectContainer)) {
                existingSubjectContainer.getMembershipContainers().putAll(membershipSubjectContainer.getMembershipContainers());
                membershipSubjectContainers.add(existingSubjectContainer);
                continue OUTER;
              }
            }
            membershipSubjectContainers.add(membershipSubjectContainer);
          }
          
          Set<GuiMembershipSubjectContainer> guiMembershipSubjectContainers = GuiMembershipSubjectContainer.convertFromMembershipSubjectContainers(membershipSubjectContainers);
          
          Set<GuiDeprovisioningMembershipSubjectContainer> guiDeprovisioningContainers = GuiDeprovisioningMembershipSubjectContainer.convertFromGuiMembershipSubjectContainers(guiMembershipSubjectContainers);
          
          deprovisioningContainer.setGuiDeprovisioningMembershipSubjectContainers(guiDeprovisioningContainers);
          
          return null;
        }
        
      });
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#deprovisioningUserResultsDivId",
          "/WEB-INF/grouperUi2/deprovisioning/deprovisioningUserResults.jsp"));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }

  }
  
  /**
   * deprovision a user
   * @param request
   * @param response
   */
  public void deprovisionUserDeprovisionSubmit(HttpServletRequest request, HttpServletResponse response) {

    final GrouperRequestContainer grouperRequestContainer = GrouperRequestContainer.retrieveFromRequestOrCreate();
    final DeprovisioningContainer deprovisioningContainer = grouperRequestContainer.getDeprovisioningContainer();
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    try {
      grouperSession = GrouperSession.start(loggedInSubject);
      
      deprovisioningContainer.assertDeprovisioningEnabledAndAllowed();
      
      GrouperDeprovisioningAffiliation deprovisioningAffiliation = retrieveAffiliation(request, loggedInSubject, true);
      
      if (deprovisioningAffiliation == null) {
        return;
      }
      
      Set<String> membershipsIds = new HashSet<String>();
      
      for (int i=0;i<10000;i++) {
        String membershipId = request.getParameter("membershipRow_" + i + "[]");
        if (!StringUtils.isBlank(membershipId)) {
          membershipsIds.add(membershipId);
        }
      }
      
      //String reason = 

      final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      
      int failures = 0;
      
      Set<Membership> membershipsDeprovisionedSuccessfully = new HashSet<Membership>();
      
      for (String membershipId : GrouperUtil.nonNull(membershipsIds)) {
        try {
          Membership membership = MembershipFinder.findByUuid(GrouperSession.start(loggedInSubject), membershipId, false, true);
          GrouperDeprovisioningLogic.removeAccess(membership);
          membershipsDeprovisionedSuccessfully.add(membership);
          } catch (Exception e) {
          LOG.warn("Error with membership: " + membershipId + ", user: " + loggedInSubject, e);
          failures++;
        }
      }
      
      String memberId = request.getParameter("memberId");
      Member member = MemberFinder.findByUuid(grouperSession, memberId, true);
      Subject subject = member.getSubject();

      if (membershipsDeprovisionedSuccessfully.size() > 0 || GrouperUtil.length(membershipsIds) == 0) {
                
        Group deprovisionGroup = deprovisioningAffiliation.getUsersWhoHaveBeenDeprovisionedGroup();
        // subject is same for all memberships
        boolean added = deprovisionGroup.addMember(subject, false);
        if (added) {
          GrouperDeprovisioningEmailService emailService = new GrouperDeprovisioningEmailService();
          Map<String, EmailPerPerson> emailObjects = emailService.buildEmailObjectForOneDeprovisionedSubject(grouperSession,
              membershipsDeprovisionedSuccessfully, deprovisioningAffiliation, false);
          emailService.sendEmailToUsers(emailObjects);
        }
      }
      
      if (failures == 0) {
        
        AuditEntry auditEntry = new AuditEntry(AuditTypeBuiltin.MEMBER_DEPROVISIONING, 
            "memberId", member.getId(), "affiliation", deprovisioningAffiliation.getLabel());
        auditEntry.setDescription("Deprovisioned user: " + GrouperUtil.subjectToString(subject) + " from affiliation: " + deprovisioningAffiliation.getLabel());
        deprovisioningSaveAudit(auditEntry);
        
        guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2Deprovisioning.viewRecentlyDeprovisionedUsers&affiliation=" + deprovisioningAffiliation.getLabel() + "')"));
        
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
            TextContainer.retrieveFromRequest().getText().get("deprovisioningDeprovisionSuccess")));
      } else {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("deprovisioningDeprovisionError")));
      }
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }
  
  /**
   * @param request
   * @param response
   */
  public void deprovisioningAffiliationSubmit(HttpServletRequest request, HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    try {
      grouperSession = GrouperSession.start(loggedInSubject);
      
      final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      
      GrouperDeprovisioningAffiliation  deprovisioningAffiliation = retrieveAffiliation(request, loggedInSubject, true);
      
      if (deprovisioningAffiliation == null) {
        return;
      }
      
      guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2Deprovisioning.viewRecentlyDeprovisionedUsers&affiliation=" + deprovisioningAffiliation.getLabel() + "')"));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }
  
  /**
   * view recently deprovisioned users
   * @param request
   * @param response
   */
  public void viewRecentlyDeprovisionedUsers(HttpServletRequest request, HttpServletResponse response) {
    
    final GrouperRequestContainer grouperRequestContainer = GrouperRequestContainer.retrieveFromRequestOrCreate();
    final DeprovisioningContainer deprovisioningContainer = grouperRequestContainer.getDeprovisioningContainer();
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    try {
      grouperSession = GrouperSession.start(loggedInSubject);
      
      final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      
      GrouperDeprovisioningAffiliation  deprovisioningAffiliation = retrieveAffiliation(request, loggedInSubject, false);
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId",
          "/WEB-INF/grouperUi2/deprovisioning/deprovisioningViewRecent.jsp"));
      
      if (!GrouperUtil.isBlank(deprovisioningAffiliation)) {
        Set<Member> usersWhoHaveBeenDeprovisioned = deprovisioningAffiliation.getUsersWhoHaveBeenDeprovisioned();
        
        deprovisioningContainer.setDeprovisionedGuiMembers(GuiMember.convertFromMembers(usersWhoHaveBeenDeprovisioned));

        guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#deprovisioningUsers",
            "/WEB-INF/grouperUi2/deprovisioning/deprovisioningMainHelper.jsp"));
        
      }
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }
  
  /**
   * deprovision a user
   * @param request
   * @param response
   */
  public void deprovisionUser(HttpServletRequest request, HttpServletResponse response) {
    
    final GrouperRequestContainer grouperRequestContainer = GrouperRequestContainer.retrieveFromRequestOrCreate();
    final DeprovisioningContainer deprovisioningContainer = grouperRequestContainer.getDeprovisioningContainer();
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
      
      deprovisioningContainer.assertDeprovisioningEnabledAndAllowed();
      
      final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      
      GrouperDeprovisioningAffiliation  deprovisioningAffiliation = retrieveAffiliation(request, loggedInSubject, false);
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/deprovisioning/deprovisioningUser.jsp"));

      if (deprovisioningAffiliation != null) {
        guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#deprovisioningUsers",
            "/WEB-INF/grouperUi2/deprovisioning/deprovisioningUserSearch.jsp"));
      }

            
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }
  
  /**
   * 
   * @param request
   * @param response
   */
  public void deprovisioningOnGroup(final HttpServletRequest request, HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    Group group = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      group = UiV2Group.retrieveGroupHelper(request, AccessPrivilege.UPDATE).getGroup();
      
      if (group != null) {
        group = UiV2Group.retrieveGroupHelper(request, AccessPrivilege.READ).getGroup();
      }
      
      if (group == null) {
        return;
      }
      
      final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      
      final Group GROUP = group;
      
      deprovisionOnObjectHelper(group);
  
      //switch over to admin so attributes work
      GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        @Override
        public Object callback(GrouperSession theGrouperSession) throws GrouperSessionException {
          
          if (!checkDeprovisioning()) {
            return null;
          }
            
          guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
              "/WEB-INF/grouperUi2/deprovisioning/deprovisioningGroupSettingsView.jsp"));
          
          return null;
        }
      });
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  
  
  }

  /**
   * report on deprovisioning on group
   * @param request
   * @param response
   */
  public void deprovisioningOnGroupReport(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    Group group = null;
    
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);

      // needs UPDATE and READ
      group = UiV2Group.retrieveGroupHelper(request, AccessPrivilege.UPDATE).getGroup();
      if (group == null) {
        return;
      }
      group = UiV2Group.retrieveGroupHelper(request, AccessPrivilege.READ).getGroup();
      
      if (group == null) {
        return;
      }

      final Group GROUP = group;

      final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();      

      GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        public Object callback(GrouperSession grouperSession) throws GrouperSessionException {

          final GrouperRequestContainer grouperRequestContainer = GrouperRequestContainer.retrieveFromRequestOrCreate();
          final DeprovisioningContainer deprovisioningContainer = grouperRequestContainer.getDeprovisioningContainer();
          
          if (!GrouperDeprovisioningSettings.deprovisioningEnabled()) {
            throw new RuntimeException("Deprovisioning is disabled");
          }

          //get all the users who are deprovisioned
          Set<DeprovisionedSubject> subjectsWhoAreDeprovisioned = GrouperDeprovisioningLogic.subjectsWhoAreDeprovisionedInRelationToOwnerWithAffiliations(GROUP, false);
          
          Set<MembershipSubjectContainer> membershipSubjectContainers = new HashSet<MembershipSubjectContainer>();
          
          if (GrouperUtil.length(subjectsWhoAreDeprovisioned) > 0) {
          
            Set<Subject> subjects = DeprovisionedSubject.retrieveSubjectsFromDeprovisionedSubject(subjectsWhoAreDeprovisioned);
            
            MembershipResult membershipResult = new MembershipFinder().assignMembershipType(MembershipType.IMMEDIATE).addGroup(GROUP)
                .assignFieldType(FieldType.ACCESS).addSubjects(subjects).findMembershipResult();
            
            membershipSubjectContainers.addAll(GrouperUtil.nonNull(membershipResult.getMembershipSubjectContainers()));

            membershipResult = new MembershipFinder().assignMembershipType(MembershipType.IMMEDIATE).addGroup(GROUP)
                .assignFieldType(FieldType.LIST).addSubjects(subjects).findMembershipResult();
            
            OUTER: for (MembershipSubjectContainer membershipSubjectContainer : GrouperUtil.nonNull(membershipResult.getMembershipSubjectContainers())) {
              
              //see if already there, update the membership
              for (MembershipSubjectContainer existingSubjectContainer : membershipSubjectContainers) {
                if (StringUtils.equals(existingSubjectContainer.getMember().getUuid(), membershipSubjectContainer.getMember().getUuid())) {
                  existingSubjectContainer.getMembershipContainers().putAll(membershipSubjectContainer.getMembershipContainers());
                  continue OUTER;
                }
              }
              membershipSubjectContainers.add(membershipSubjectContainer);
            }
          }
          
          Set<GuiMembershipSubjectContainer> guiMembershipSubjectContainers = GuiMembershipSubjectContainer.convertFromMembershipSubjectContainers(membershipSubjectContainers);
          Set<GuiDeprovisioningMembershipSubjectContainer> guiDeprovisioningContainers = 
              GuiDeprovisioningMembershipSubjectContainer.convertFromGuiMembershipSubjectContainers(guiMembershipSubjectContainers);
          GuiDeprovisioningMembershipSubjectContainer.markAffiliations(guiDeprovisioningContainers, subjectsWhoAreDeprovisioned);
          deprovisioningContainer.setGuiDeprovisioningMembershipSubjectContainers(guiDeprovisioningContainers);
          
          return null;
        }
      });
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/deprovisioning/deprovisioningGroupReport.jsp"));

    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }

    
  }
  
  /**
   * 
   * @param request
   * @param response
   */
  public void deprovisioningOnGroupEdit(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    Group group = null;
    
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      group = UiV2Group.retrieveGroupHelper(request, AccessPrivilege.UPDATE).getGroup();
      
      if (group != null) {
        group = UiV2Group.retrieveGroupHelper(request, AccessPrivilege.READ).getGroup();
      }
      
      if (group == null) {
        return;
      }
      
      final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      
      final Group GROUP = group;

      final DeprovisioningContainer deprovisioningContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getDeprovisioningContainer();
      
      //switch over to admin so attributes work
      boolean shouldContinue = (Boolean)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        @Override
        public Object callback(GrouperSession theGrouperSession) throws GrouperSessionException {
          
          if (!checkDeprovisioning()) {
            return false;
          }
          
          if (!deprovisioningContainer.isCanWriteDeprovisioning()) {
            guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
                TextContainer.retrieveFromRequest().getText().get("deprovisioningNotAllowedToWriteDeprovisioningGroup")));
            return false;
          }
  
          return true;
        }
      });
      
      if (!shouldContinue) {
        return;
      }
      
      if (!deprovisionOnObjectEditHelper(request, response, false, group)) {
        return;
      }
  
  
      //switch over to admin so attributes work
      GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        @Override
        public Object callback(GrouperSession theGrouperSession) throws GrouperSessionException {
          
          
          guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
              "/WEB-INF/grouperUi2/deprovisioning/deprovisioningGroupSettingsEdit.jsp"));
          
          return null;
        }
      });
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }
  /**
   * 
   * @param request
   * @param response
   */
  public void deprovisioningOnGroupEditSave(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    Group group = null;
    
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      group = UiV2Group.retrieveGroupHelper(request, AccessPrivilege.UPDATE).getGroup();
      
      if (group != null) {
        group = UiV2Group.retrieveGroupHelper(request, AccessPrivilege.READ).getGroup();
      }
      
      if (group == null) {
        return;
      }
      
      final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      
      final Group GROUP = group;
      
      final DeprovisioningContainer deprovisioningContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getDeprovisioningContainer();
      
      //switch over to admin so attributes work
      boolean shouldContinue = (Boolean)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        @Override
        public Object callback(GrouperSession theGrouperSession) throws GrouperSessionException {
          
          if (!checkDeprovisioning()) {
            return false;
          }
          
          if (!deprovisioningContainer.isCanWriteDeprovisioning()) {
            guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
                TextContainer.retrieveFromRequest().getText().get("deprovisioningNotAllowedToWriteDeprovisioningGroup")));
            return false;
          }
  
          return true;
        }
      });
      
      if (!shouldContinue) {
        return;
      }
  
      if (!deprovisionOnObjectEditHelper(request, response, true, group)) {
        return;
      }

      //switch over to admin so attributes work
      GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        @Override
        public Object callback(GrouperSession theGrouperSession) throws GrouperSessionException {
          
          List<GuiScreenAction> guiScreenActions = new ArrayList<GuiScreenAction>();
          boolean hasError = false;
          
          hasError = UiV2Deprovisioning.this.deprovisioningOnObjectEditSaveHelper(request, null, deprovisioningContainer, guiScreenActions, hasError, GROUP);
          
          if (!hasError) {
            guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2Deprovisioning.deprovisioningOnGroup&groupId=" + GROUP.getId() + "')"));
  
            guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
                TextContainer.retrieveFromRequest().getText().get("deprovisioningEditSaveSuccess")));
  
          } else {
            
            guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
                "/WEB-INF/grouperUi2/deprovisioning/deprovisioningGroupSettingsEdit.jsp"));
            
            //add these after screen drawn
            for (GuiScreenAction guiScreenAction : guiScreenActions) {
              guiResponseJs.addAction(guiScreenAction);
            }
            
          }
  
          return null;
        }
      });
  
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  
  
  }
  /**
   * 
   * @param request
   * @param response
   */
  public void deprovisioningOnAttributeDef(final HttpServletRequest request, HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    AttributeDef attributeDef = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      attributeDef = UiV2AttributeDef.retrieveAttributeDefHelper(request, AttributeDefPrivilege.ATTR_READ, true).getAttributeDef();

      if (attributeDef != null) {
        attributeDef = UiV2AttributeDef.retrieveAttributeDefHelper(request, AttributeDefPrivilege.ATTR_UPDATE, true).getAttributeDef();
      }
      
      if (attributeDef == null) {
        return;
      }
      
      final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      
      final AttributeDef ATTRIBUTE_DEF = attributeDef;
      
      deprovisionOnObjectHelper(attributeDef);
  
      //switch over to admin so attributes work
      GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        @Override
        public Object callback(GrouperSession theGrouperSession) throws GrouperSessionException {
          
          if (!checkDeprovisioning()) {
            return null;
          }
            
          guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
              "/WEB-INF/grouperUi2/deprovisioning/deprovisioningAttributeDefSettingsView.jsp"));
          
          return null;
        }
      });
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  
  
  }
  /**
   * 
   * @param request
   * @param response
   */
  public void deprovisioningOnAttributeDefEdit(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    AttributeDef attributeDef = null;
    
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      attributeDef = UiV2AttributeDef.retrieveAttributeDefHelper(request, AttributeDefPrivilege.ATTR_READ, true).getAttributeDef();

      if (attributeDef != null) {
        attributeDef = UiV2AttributeDef.retrieveAttributeDefHelper(request, AttributeDefPrivilege.ATTR_UPDATE, true).getAttributeDef();
      }
      
      if (attributeDef == null) {
        return;
      }
      
      final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      
      final AttributeDef ATTRIBUTE_DEF = attributeDef;
  
      final DeprovisioningContainer deprovisioningContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getDeprovisioningContainer();
      
      //switch over to admin so attributes work
      boolean shouldContinue = (Boolean)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        @Override
        public Object callback(GrouperSession theGrouperSession) throws GrouperSessionException {
          
          if (!checkDeprovisioning()) {
            return false;
          }
          
          if (!deprovisioningContainer.isCanWriteDeprovisioning()) {
            guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
                TextContainer.retrieveFromRequest().getText().get("deprovisioningNotAllowedToWriteDeprovisioningAttributeDef")));
            return false;
          }
  
          return true;
        }
      });
      
      if (!shouldContinue) {
        return;
      }
      
      if (!deprovisionOnObjectEditHelper(request, response, false, attributeDef)) {
        return;
      }
  
  
      //switch over to admin so attributes work
      GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        @Override
        public Object callback(GrouperSession theGrouperSession) throws GrouperSessionException {
          
          
          guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
              "/WEB-INF/grouperUi2/deprovisioning/deprovisioningAttributeDefSettingsEdit.jsp"));
          
          return null;
        }
      });
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }
  /**
   * 
   * @param request
   * @param response
   */
  public void deprovisioningOnAttributeDefEditSave(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    AttributeDef attributeDef = null;
    
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      attributeDef = UiV2AttributeDef.retrieveAttributeDefHelper(request, AttributeDefPrivilege.ATTR_READ, true).getAttributeDef();

      if (attributeDef != null) {
        attributeDef = UiV2AttributeDef.retrieveAttributeDefHelper(request, AttributeDefPrivilege.ATTR_UPDATE, true).getAttributeDef();
      }
      
      if (attributeDef == null) {
        return;
      }
      
      final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      
      final AttributeDef ATTRIBUTE_DEF = attributeDef;
      
      final DeprovisioningContainer deprovisioningContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getDeprovisioningContainer();
      
      //switch over to admin so attributes work
      boolean shouldContinue = (Boolean)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        @Override
        public Object callback(GrouperSession theGrouperSession) throws GrouperSessionException {
          
          if (!checkDeprovisioning()) {
            return false;
          }
          
          if (!deprovisioningContainer.isCanWriteDeprovisioning()) {
            guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
                TextContainer.retrieveFromRequest().getText().get("deprovisioningNotAllowedToWriteDeprovisioningAttributeDef")));
            return false;
          }
  
          return true;
        }
      });
      
      if (!shouldContinue) {
        return;
      }
  
      if (!deprovisionOnObjectEditHelper(request, response, true, attributeDef)) {
        return;
      }
  
      //switch over to admin so attributes work
      GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        @Override
        public Object callback(GrouperSession theGrouperSession) throws GrouperSessionException {
          
          List<GuiScreenAction> guiScreenActions = new ArrayList<GuiScreenAction>();
          boolean hasError = false;
          
          hasError = UiV2Deprovisioning.this.deprovisioningOnObjectEditSaveHelper(request, null, deprovisioningContainer, guiScreenActions, hasError, ATTRIBUTE_DEF);
          
          if (!hasError) {
            guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2Deprovisioning.deprovisioningOnAttributeDef&attributeDefId=" + ATTRIBUTE_DEF.getId() + "')"));
  
            guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
                TextContainer.retrieveFromRequest().getText().get("deprovisioningEditSaveSuccess")));
  
          } else {
            
            guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
                "/WEB-INF/grouperUi2/deprovisioning/deprovisioningAttributeDefSettingsEdit.jsp"));
            
            //add these after screen drawn
            for (GuiScreenAction guiScreenAction : guiScreenActions) {
              guiResponseJs.addAction(guiScreenAction);
            }
            
          }
  
          return null;
        }
      });
  
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  
  
  }
  /**
   * @param request
   * @param response
   */
  public void xxx_deprovisioningReportOnGroup(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    Group group = null;
    
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      group = UiV2Group.retrieveGroupHelper(request, AccessPrivilege.READ).getGroup();
      
      if (group == null) {
        return;
      }
      
      if (!xxx_deprovisioningReportOnObjectHelper(group)) {
        return;
      }
  
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  
  }

  /**
   * 
   * @param request
   * @param response
   */
  public void updateAttributeDefLastCertifiedDate(HttpServletRequest request, HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    AttributeDef attributeDef = null;
    
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      attributeDef = UiV2AttributeDef.retrieveAttributeDefHelper(request, AttributeDefPrivilege.ATTR_READ, true).getAttributeDef();

      if (attributeDef != null) {
        attributeDef = UiV2AttributeDef.retrieveAttributeDefHelper(request, AttributeDefPrivilege.ATTR_UPDATE, true).getAttributeDef();
      }

      if (attributeDef == null) {
        return;
      }
      
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      
      GrouperDeprovisioningLogic.updateLastCertifiedDate(attributeDef, new Date());
      
      AuditEntry auditEntry = new AuditEntry(AuditTypeBuiltin.ATTR_DEPROVISIONING_UPDATE_LAST_CERTIFIED_DATE, 
          "attributeDefId", attributeDef.getId(), "attributeDefName", attributeDef.getName());
      auditEntry.setDescription("Update last certified date deprovisioning attribute of attribute def: " + attributeDef.getName());
      deprovisioningSaveAudit(auditEntry);

      //deprovisioningReportOnFolder(request, response);
      
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
          TextContainer.retrieveFromRequest().getText().get("deprovisioningLastCertifiedUpdateSuccess")));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }

  /**
   * 
   * @param auditEntry
   */
  private static void deprovisioningSaveAudit(final AuditEntry auditEntry) {
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

  /**
   * 
   * @param request
   * @param response
   */
  public void updateGroupLastCertifiedDate(HttpServletRequest request, HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    Group group = null;
    
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      group = UiV2Group.retrieveGroupHelper(request, AccessPrivilege.UPDATE).getGroup();
      
      if (group != null) {
        group = UiV2Group.retrieveGroupHelper(request, AccessPrivilege.READ).getGroup();
      }
      
      if (group == null) {
        return;
      }
      
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      
      GrouperDeprovisioningLogic.updateLastCertifiedDate(group, new Date());
      
      AuditEntry auditEntry = new AuditEntry(AuditTypeBuiltin.GROUP_DEPROVISIONING_UPDATE_LAST_CERTIFIED_DATE, 
          "groupId", group.getId(), "groupName", group.getName());
      auditEntry.setDescription("Update last certified date deprovisioning attribute of group: " + group.getName());
      deprovisioningSaveAudit(auditEntry);
      
      deprovisioningOnGroupReport(request, response);
      
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
          TextContainer.retrieveFromRequest().getText().get("deprovisioningLastCertifiedUpdateSuccess")));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }

  /**
   * 
   * @param request
   * @param response
   */
  public void updateFolderLastCertifiedDate(HttpServletRequest request, HttpServletResponse response) {
    
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
      
      GrouperDeprovisioningLogic.updateLastCertifiedDate(stem, new Date());
      
      AuditEntry auditEntry = new AuditEntry(AuditTypeBuiltin.STEM_DEPROVISIONING_UPDATE_LAST_CERTIFIED_DATE, 
          "stemId", stem.getId(), "stemName", stem.getName());
      auditEntry.setDescription("Update last certified date deprovisioning attribute of folder: " + stem.getName());
      deprovisioningSaveAudit(auditEntry);

      deprovisioningOnFolderReport(request, response);
      
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
          TextContainer.retrieveFromRequest().getText().get("deprovisioningLastCertifiedUpdateSuccess")));
      
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

      DeprovisioningContainer deprovisioningContainer = GrouperRequestContainer.retrieveFromRequestOrCreate()
          .getDeprovisioningContainer();

      if (!deprovisioningContainer.isCanRunDaemon()) {
        throw new RuntimeException("Not allowed!!!!!");
      }
      
      final boolean[] DONE = new boolean[]{false};
      
      Thread thread = new Thread(new Runnable() {

        @Override
        public void run() {
          GrouperSession grouperSession = GrouperSession.startRootSession();
          try {
            GrouperDeprovisioningJob.runDaemonStandalone();
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
        throw new RuntimeException("Exception in thread", e);
      }

      if (DONE[0]) {

        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
                TextContainer.retrieveFromRequest().getText().get("deprovisioningSuccessDaemonRan")));
        
      } else {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.info, 
            TextContainer.retrieveFromRequest().getText().get("deprovisioningInfoDaemonInRunning")));

      }
      
  
  
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }

  
  /**
   * @param request
   * @param response
   */
  public void xxx_deprovisioningReportOnAttributeDef(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    AttributeDef attributeDef = null;
    
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      attributeDef = UiV2AttributeDef.retrieveAttributeDefHelper(request, AttributeDefPrivilege.ATTR_READ, true).getAttributeDef();

      if (attributeDef == null) {
        return;
      }
      
      if (!xxx_deprovisioningReportOnObjectHelper(attributeDef)) {
        return;
      }
  
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  
  }
  /**
   * 
   * @param request
   * @param response
   */
  public void updateAttributeDefLastCertifiedDateClear(HttpServletRequest request, HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    AttributeDef attributeDef = null;
    
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      attributeDef = UiV2AttributeDef.retrieveAttributeDefHelper(request, AttributeDefPrivilege.ATTR_READ, true).getAttributeDef();
  
      if (attributeDef != null) {
        attributeDef = UiV2AttributeDef.retrieveAttributeDefHelper(request, AttributeDefPrivilege.ATTR_UPDATE, true).getAttributeDef();
      }
  
      if (attributeDef == null) {
        return;
      }
      
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      
      GrouperDeprovisioningLogic.updateLastCertifiedDate(attributeDef, null);
      
      AuditEntry auditEntry = new AuditEntry(AuditTypeBuiltin.ATTR_DEPROVISIONING_CLEAR_LAST_CERTIFIED_DATE, 
          "attributeDefId", attributeDef.getId(), "attributeDefName", attributeDef.getName());
      auditEntry.setDescription("Clear last certified date deprovisioning attribute of attribute def: " + attributeDef.getName());
      deprovisioningSaveAudit(auditEntry);

      //deprovisioningReportOnFolder(request, response);
      
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
          TextContainer.retrieveFromRequest().getText().get("deprovisioningLastCertifiedClearSuccess")));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }
  /**
   * 
   * @param request
   * @param response
   */
  public void updateFolderLastCertifiedDateClear(HttpServletRequest request, HttpServletResponse response) {
    
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
      
      GrouperDeprovisioningLogic.updateLastCertifiedDate(stem, null);
      
      AuditEntry auditEntry = new AuditEntry(AuditTypeBuiltin.STEM_DEPROVISIONING_CLEAR_LAST_CERTIFIED_DATE, 
          "stemId", stem.getId(), "stemName", stem.getName());
      auditEntry.setDescription("Clear last certified date deprovisioning attribute of folder: " + stem.getName());
      deprovisioningSaveAudit(auditEntry);

      //deprovisioningReportOnFolder(request, response);
      
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
          TextContainer.retrieveFromRequest().getText().get("deprovisioningLastCertifiedClearSuccess")));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }
  /**
   * 
   * @param request
   * @param response
   */
  public void updateGroupLastCertifiedDateClear(HttpServletRequest request, HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    Group group = null;
    
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      group = UiV2Group.retrieveGroupHelper(request, AccessPrivilege.UPDATE).getGroup();
      
      if (group != null) {
        group = UiV2Group.retrieveGroupHelper(request, AccessPrivilege.READ).getGroup();
      }
      
      if (group == null) {
        return;
      }
      
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      
      GrouperDeprovisioningLogic.updateLastCertifiedDate(group, null);
      
      AuditEntry auditEntry = new AuditEntry(AuditTypeBuiltin.GROUP_DEPROVISIONING_CLEAR_LAST_CERTIFIED_DATE, 
          "groupId", group.getId(), "groupName", group.getName());
      auditEntry.setDescription("Clear last certified date deprovisioning attribute of group: " + group.getName());
      deprovisioningSaveAudit(auditEntry);
      
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
          TextContainer.retrieveFromRequest().getText().get("deprovisioningLastCertifiedClearSuccess")));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }
  /**
   * report on deprovisioning on folder
   * @param request
   * @param response
   */
  public void deprovisioningOnFolderReport(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    Stem stem = null;

    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
      
      stem = UiV2Stem.retrieveStemHelper(request, true).getStem();
      
      if (stem == null) {
        return;
      }

      final Stem STEM = stem;
      
      final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();      
  
      GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
  
          final GrouperRequestContainer grouperRequestContainer = GrouperRequestContainer.retrieveFromRequestOrCreate();
          final DeprovisioningContainer deprovisioningContainer = grouperRequestContainer.getDeprovisioningContainer();
          
          if (!GrouperDeprovisioningSettings.deprovisioningEnabled()) {
            throw new RuntimeException("Deprovisioning is disabled");
          }
  
          //get all the users who are deprovisioned
          Set<DeprovisionedSubject> subjectsWhoAreDeprovisioned = GrouperDeprovisioningLogic.subjectsWhoAreDeprovisionedInRelationToOwnerWithAffiliations(STEM, false);
          
          Set<MembershipSubjectContainer> membershipSubjectContainers = new HashSet<MembershipSubjectContainer>();
          
          if (GrouperUtil.length(subjectsWhoAreDeprovisioned) > 0) {
          
            Set<Subject> subjects = DeprovisionedSubject.retrieveSubjectsFromDeprovisionedSubject(subjectsWhoAreDeprovisioned);
            
            MembershipResult membershipResult = new MembershipFinder().assignMembershipType(MembershipType.IMMEDIATE).addStem(STEM)
                .assignFieldType(FieldType.NAMING).addSubjects(subjects).findMembershipResult();
            
            membershipSubjectContainers.addAll(GrouperUtil.nonNull(membershipResult.getMembershipSubjectContainers()));
  
          }
          
          Set<GuiMembershipSubjectContainer> guiMembershipSubjectContainers = GuiMembershipSubjectContainer.convertFromMembershipSubjectContainers(membershipSubjectContainers);
          Set<GuiDeprovisioningMembershipSubjectContainer> guiDeprovisioningContainers = 
              GuiDeprovisioningMembershipSubjectContainer.convertFromGuiMembershipSubjectContainers(guiMembershipSubjectContainers);
          GuiDeprovisioningMembershipSubjectContainer.markAffiliations(guiDeprovisioningContainers, subjectsWhoAreDeprovisioned);
          deprovisioningContainer.setGuiDeprovisioningMembershipSubjectContainers(guiDeprovisioningContainers);
          
          return null;
        }
      });
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/deprovisioning/deprovisioningFolderReport.jsp"));
  
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  
    
  }
  /**
   * @param request
   * @param response
   */
  public void deprovisioningOnFolderReportSubmit(final HttpServletRequest request, final HttpServletResponse response) {
    
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
      
      final Set<String> memberIds = new HashSet<String>();
      
      for (int i=0;i<10000;i++) {
        String memberId = request.getParameter("memberIds_" + i + "[]");
        if (!StringUtils.isBlank(memberId)) {
          memberIds.add(memberId);
        }
      }
      
      if (GrouperUtil.length(memberIds) == 0) {
        
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("deprovisioningNoMembersSelected")));
        
        return;
      }
      
      GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        @Override
        public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
          
          int failures = 0;
  
          for (String memberId : GrouperUtil.nonNull(memberIds)) {
            try {
              
              Member member = MemberFinder.findByUuid(grouperSession, memberId, true);
              Subject subject = member.getSubject();
              GrouperDeprovisioningLogic.removeAccess(STEM, subject);
              
            } catch (Exception e) {
              LOG.error("Error with removing priv: " + memberId + ", " + STEM.getName(), e);
              failures++;
            }
          }
          
          if (failures == 0) {
            
            deprovisioningOnGroupReport(request, response);
            
            guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
                TextContainer.retrieveFromRequest().getText().get("deprovisioningDeprovisionFromReportSuccess")));
          } else {
            guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
                TextContainer.retrieveFromRequest().getText().get("deprovisioningDeprovisionFromReportError")));
          }
          return null;
        }
      });
      
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  /**
   * report on deprovisioning on group
   * @param request
   * @param response
   */
  public void deprovisioningOnAttributeDefReport(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    AttributeDef attributeDef = null;
    
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      attributeDef = UiV2AttributeDef.retrieveAttributeDefHelper(request, AttributeDefPrivilege.ATTR_READ, true).getAttributeDef();

      if (attributeDef != null) {
        attributeDef = UiV2AttributeDef.retrieveAttributeDefHelper(request, AttributeDefPrivilege.ATTR_UPDATE, true).getAttributeDef();
      }
      
      if (attributeDef == null) {
        return;
      }
      
      final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      
      final AttributeDef ATTRIBUTE_DEF = attributeDef;
  
      GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
  
          final GrouperRequestContainer grouperRequestContainer = GrouperRequestContainer.retrieveFromRequestOrCreate();
          final DeprovisioningContainer deprovisioningContainer = grouperRequestContainer.getDeprovisioningContainer();
          
          if (!GrouperDeprovisioningSettings.deprovisioningEnabled()) {
            throw new RuntimeException("Deprovisioning is disabled");
          }
  
          //get all the users who are deprovisioned
          Set<DeprovisionedSubject> subjectsWhoAreDeprovisioned = GrouperDeprovisioningLogic.subjectsWhoAreDeprovisionedInRelationToOwnerWithAffiliations(ATTRIBUTE_DEF, false);
          
          Set<MembershipSubjectContainer> membershipSubjectContainers = new HashSet<MembershipSubjectContainer>();
          
          if (GrouperUtil.length(subjectsWhoAreDeprovisioned) > 0) {
          
            Set<Subject> subjects = DeprovisionedSubject.retrieveSubjectsFromDeprovisionedSubject(subjectsWhoAreDeprovisioned);
            
            MembershipResult membershipResult = new MembershipFinder().assignMembershipType(MembershipType.IMMEDIATE).addAttributeDef(ATTRIBUTE_DEF)
                .assignFieldType(FieldType.ATTRIBUTE_DEF).addSubjects(subjects).findMembershipResult();
            
            membershipSubjectContainers.addAll(GrouperUtil.nonNull(membershipResult.getMembershipSubjectContainers()));
  
          }
          
          Set<GuiMembershipSubjectContainer> guiMembershipSubjectContainers = GuiMembershipSubjectContainer.convertFromMembershipSubjectContainers(membershipSubjectContainers);
          Set<GuiDeprovisioningMembershipSubjectContainer> guiDeprovisioningContainers = 
              GuiDeprovisioningMembershipSubjectContainer.convertFromGuiMembershipSubjectContainers(guiMembershipSubjectContainers);
          GuiDeprovisioningMembershipSubjectContainer.markAffiliations(guiDeprovisioningContainers, subjectsWhoAreDeprovisioned);
          deprovisioningContainer.setGuiDeprovisioningMembershipSubjectContainers(guiDeprovisioningContainers);
          
          return null;
        }
      });
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/deprovisioning/deprovisioningAttributeDefReport.jsp"));

    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  
    
  }
  /**
   * @param request
   * @param response
   */
  public void deprovisioningOnAttributeDefReportSubmit(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    AttributeDef attributeDef = null;
    
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      attributeDef = UiV2AttributeDef.retrieveAttributeDefHelper(request, AttributeDefPrivilege.ATTR_READ, true).getAttributeDef();

      if (attributeDef != null) {
        attributeDef = UiV2AttributeDef.retrieveAttributeDefHelper(request, AttributeDefPrivilege.ATTR_UPDATE, true).getAttributeDef();
      }
      
      if (attributeDef == null) {
        return;
      }
      
      final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      
      final AttributeDef ATTRIBUTE_DEF = attributeDef;
      
      final Set<String> memberIds = new HashSet<String>();
      
      for (int i=0;i<10000;i++) {
        String memberId = request.getParameter("memberIds_" + i + "[]");
        if (!StringUtils.isBlank(memberId)) {
          memberIds.add(memberId);
        }
      }
      
      if (GrouperUtil.length(memberIds) == 0) {
        
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("deprovisioningNoMembersSelected")));
        
        return;
      }
      
      GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        @Override
        public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
          
          int failures = 0;
  
          for (String memberId : GrouperUtil.nonNull(memberIds)) {
            try {
              
              Member member = MemberFinder.findByUuid(grouperSession, memberId, true);
              Subject subject = member.getSubject();
              GrouperDeprovisioningLogic.removeAccess(ATTRIBUTE_DEF, subject);
              
            } catch (Exception e) {
              LOG.error("Error with removing priv: " + memberId + ", " + ATTRIBUTE_DEF.getName(), e);
              failures++;
            }
          }
          
          if (failures == 0) {
            
            deprovisioningOnAttributeDefReport(request, response);
            
            guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
                TextContainer.retrieveFromRequest().getText().get("deprovisioningDeprovisionFromReportSuccess")));
          } else {
            guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
                TextContainer.retrieveFromRequest().getText().get("deprovisioningDeprovisionFromReportError")));
          }
          return null;
        }
      });
      
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  private static GrouperDeprovisioningAffiliation retrieveAffiliation(HttpServletRequest request, Subject subject, boolean requireAffiliation) {
    
    final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    final GrouperRequestContainer grouperRequestContainer = GrouperRequestContainer.retrieveFromRequestOrCreate();
    final DeprovisioningContainer deprovisioningContainer = grouperRequestContainer.getDeprovisioningContainer();
    
    String affiliation = request.getParameter("affiliation");
    if (StringUtils.isBlank(affiliation)) {
      if (requireAffiliation) {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("deprovisioningNoAffiliationSelected")));
      }
      return null;
    }
    
    GrouperDeprovisioningAffiliation deprovisioningAffiliation = GrouperDeprovisioningAffiliation.retrieveAllAffiliations().get(affiliation);

    if (deprovisioningAffiliation == null) {
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
          TextContainer.retrieveFromRequest().getText().get("deprovisioningNoAffiliationSelected")));
      return null;
    }
    
    if (!deprovisioningAffiliation.subjectIsManager(subject)) {
      throw new RuntimeException("User is not manager.");
    }
    
    deprovisioningContainer.setAffiliation(affiliation);
    return deprovisioningAffiliation;
    
  }
  
  private boolean shouldMembershipSubjectContainerBeMerged(MembershipSubjectContainer container1, MembershipSubjectContainer container2) {
	  
	  String uuid1 = container1.getMember().getUuid();
	  String uuid2 = container2.getMember().getUuid();
	  
	  Group group1 = container1.getGroupOwner();
	  Group group2 = container2.getGroupOwner();
	  
	  Stem stem1 = container1.getStemOwner();
	  Stem stem2 = container2.getStemOwner();
	  
	  AttributeDef attributeDef1 = container1.getAttributeDefOwner();
	  AttributeDef attributeDef2 = container2.getAttributeDefOwner();
	  
	  if (StringUtils.equals(uuid1, uuid2) &&
			   (group1 != null && group2 != null && StringUtils.equals(group1.getUuid(), group2.getUuid() ))  ||
			   (stem1 != null && stem2 != null && StringUtils.equals(stem1.getUuid(), stem2.getUuid()))  ||
			   (attributeDef1 != null && attributeDef2 != null && StringUtils.equals(attributeDef1.getUuid(), attributeDef2.getUuid()))) {
		  return true;
	  }
	  
	  return false;
	  
  }
  
}
