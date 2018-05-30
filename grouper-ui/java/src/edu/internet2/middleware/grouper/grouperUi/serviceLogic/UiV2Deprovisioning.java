package edu.internet2.middleware.grouper.grouperUi.serviceLogic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.FieldType;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.GrouperSourceAdapter;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.MembershipFinder;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.Stem.Scope;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.app.deprovisioning.GrouperDeprovisioningAffiliation;
import edu.internet2.middleware.grouper.app.deprovisioning.GrouperDeprovisioningAttributeNames;
import edu.internet2.middleware.grouper.app.deprovisioning.GrouperDeprovisioningAttributeValue;
import edu.internet2.middleware.grouper.app.deprovisioning.GrouperDeprovisioningOverallConfiguration;
import edu.internet2.middleware.grouper.app.deprovisioning.GrouperDeprovisioningSettings;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignable;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiDeprovisioningMembershipSubjectContainer;
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
import edu.internet2.middleware.grouper.membership.MembershipResult;
import edu.internet2.middleware.grouper.membership.MembershipSubjectContainer;
import edu.internet2.middleware.grouper.membership.MembershipType;
import edu.internet2.middleware.grouper.misc.GrouperObject;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.privs.AttributeDefPrivilege;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
import edu.internet2.middleware.grouper.privs.Privilege;
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
   * 
   * @param request
   * @param response
   */
  public void deprovisioningOnFolderEditSave(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    Stem stem = null;
    
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
      
      stem = UiV2Stem.retrieveStemHelper(request, true).getStem();
      
      if (stem == null) {
        return;
      }
  
      if (!deprovisionOnFolderEditHelper(request, response)) {
        return;
      }

      final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();

      final Stem STEM = stem;

      //switch over to admin so attributes work
      GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        @Override
        public Object callback(GrouperSession theGrouperSession) throws GrouperSessionException {
          
          List<GuiScreenAction> guiScreenActions = new ArrayList<GuiScreenAction>();
          boolean hasError = false;
          
          DeprovisioningContainer deprovisioningContainer = GrouperRequestContainer
              .retrieveFromRequestOrCreate().getDeprovisioningContainer();
          
          AttributeAssignable attributeAssignable = STEM;
          
          String affiliation = request.getParameter("grouperDeprovisioningHasAffiliationName");
          deprovisioningContainer.setAffiliation(affiliation);
          if (StringUtils.isBlank(affiliation)) {
            guiScreenActions.add(GuiScreenAction.newValidationMessage(GuiMessageType.error, "#grouperDeprovisioningHasAffiliationId",
                TextContainer.retrieveFromRequest().getText().get("deprovisioningAffiliationRequired")));
            hasError = true;
          } else {
            GrouperDeprovisioningAttributeValue grouperDeprovisioningAttributeValue = deprovisioningContainer.getGrouperDeprovisioningAttributeValueNew();
          }
          
          if (!hasError) {

            deprovisioningContainer.getGrouperDeprovisioningOverallConfiguration().getAffiliationToConfiguration().get(affiliation).storeConfiguration();
            
            guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2Deprovisioning.deprovisioningOnFolder&stemId=" + STEM.getId() + "')"));

            guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
                TextContainer.retrieveFromRequest().getText().get("deprovisioningEditSaveSuccess")));

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
   * @return true if proceed to page, false to just return an error message
   */
  private boolean deprovisionOnFolderEditHelper(final HttpServletRequest request, final HttpServletResponse response) {
    
    GrouperSession grouperSession = GrouperSession.staticGrouperSession();
  
    Stem stem = null;
  
    try {
  
      stem = UiV2Stem.retrieveStemHelper(request, true).getStem();
      
      if (stem == null) {
        return false;
      }
      
      final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      
      final Stem STEM = stem;
      
      //switch over to admin so attributes work
      return (Boolean)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        @Override
        public Object callback(GrouperSession theGrouperSession) throws GrouperSessionException {
          
          if (!checkDeprovisioning()) {
            return false;
          }
          
          DeprovisioningContainer deprovisioningContainer = GrouperRequestContainer
              .retrieveFromRequestOrCreate().getDeprovisioningContainer();
          
          if (!deprovisioningContainer.isCanWriteDeprovisioning()) {
            guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
                TextContainer.retrieveFromRequest().getText().get("deprovisioningNotAllowedToWriteDeprovisioningFolder")));
            return false;
          }

          AttributeAssignable attributeAssignable = STEM;
          
          setupDeprovisioningConfiguration(attributeAssignable);
          
          String affiliation = request.getParameter("grouperDeprovisioningHasAffiliationName");
          deprovisioningContainer.setAffiliation(affiliation);
          if (!StringUtils.isBlank(affiliation)) {
            GrouperDeprovisioningAttributeValue grouperDeprovisioningAttributeValue = deprovisioningContainer.getGrouperDeprovisioningAttributeValueNew();
            
            Boolean hasConfiguration = GrouperUtil.booleanObjectValue(request.getParameter("grouperDeprovisioningHasConfigurationName"));
            grouperDeprovisioningAttributeValue.setDirectAssignment(GrouperUtil.booleanValue(hasConfiguration, false));
            
            Scope scope = Scope.valueOfIgnoreCase(request.getParameter("grouperDeprovisioningFolderScopeName"), false);
            grouperDeprovisioningAttributeValue.setStemScope(scope);
            
            Boolean sendEmail = GrouperUtil.booleanObjectValue(request.getParameter("grouperDeprovisioningSendEmailName"));
            grouperDeprovisioningAttributeValue.setSendEmail(GrouperUtil.booleanValue(sendEmail, false));
            
            if (sendEmail != null && sendEmail) {
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
                }
                if (emailGroupMembers != null && !emailGroupMembers) {
                  grouperDeprovisioningAttributeValue.setEmailGroupMembers(false);
                }
                
                if (!grouperDeprovisioningAttributeValue.isEmailGroupMembers()) {
                  String emailAddresses = request.getParameter("grouperDeprovisioningEmailAddressesName");
                  grouperDeprovisioningAttributeValue.setEmailAddressesString(emailAddresses);
                }
                
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
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      if (!deprovisionOnFolderEditHelper(request, response)) {
        return;
      }

      final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();

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
  public void deprovisioningOnFolder(HttpServletRequest request, HttpServletResponse response) {
    
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
          
          AttributeAssignable attributeAssignable = STEM;
          
          setupDeprovisioningConfiguration(attributeAssignable);
          
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
    
    deprovisioningContainer.assertDeprovisioningEnabledAndAllowed();
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
            
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/deprovisioning/deprovisioningMain.jsp"));
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#deprovisioningUsers", 
          "/WEB-INF/grouperUi2/deprovisioning/deprovisioningSelectAffiliation.jsp"));
      
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
    
    deprovisioningContainer.assertDeprovisioningEnabledAndAllowed();
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      
      String subjectString = request.getParameter("groupAddMemberComboName");
      
      if (StringUtils.isBlank(subjectString)) {
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, 
            "#groupAddMemberComboId",
            TextContainer.retrieveFromRequest().getText().get("deprovisioningNoAffiliationSelected")));
        return;
      }
      
      GrouperDeprovisioningAffiliation  deprovisioningAffiliation = retrieveAffiliation(request, loggedInSubject);
      
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

          Set<GuiMembershipSubjectContainer> result = new LinkedHashSet<GuiMembershipSubjectContainer>();
          
          Set<GuiDeprovisioningMembershipSubjectContainer> guiDeprovisioningContainers = new HashSet<GuiDeprovisioningMembershipSubjectContainer>();
          
          for (FieldType fieldType : new FieldType[] {FieldType.LIST, FieldType.ACCESS, FieldType.NAMING, FieldType.ATTRIBUTE_DEF}) {

            //get all the memberships and privileges
            MembershipResult membershipResult = new MembershipFinder().addSubject(SUBJECT)
                .assignFieldType(fieldType)
                .assignMembershipType(MembershipType.IMMEDIATE).findMembershipResult();
            Set<MembershipSubjectContainer> membershipSubjectContainers = membershipResult.getMembershipSubjectContainers();
            
            Set<GuiMembershipSubjectContainer> guiMembershipSubjectContainers = GuiMembershipSubjectContainer.convertFromMembershipSubjectContainers(membershipSubjectContainers);
            ArrayList<GuiMembershipSubjectContainer> resultList = new ArrayList<GuiMembershipSubjectContainer>(guiMembershipSubjectContainers);
            Collections.sort(resultList, new java.util.Comparator<GuiMembershipSubjectContainer>() {

              @Override
              public int compare(GuiMembershipSubjectContainer o1,
                  GuiMembershipSubjectContainer o2) {
                
                if (o1 == o2) {
                  return 0;
                }
                
                if (o2 == null) {
                  return 1;
                }
                
                if (o1 == null) {
                  return -1;
                }
                
                return o1.getGuiObjectBase().getNameColonSpaceSeparated().compareTo(o2.getGuiObjectBase().getNameColonSpaceSeparated());
              }
            });
            result.addAll(resultList);
          }
          
          for (GuiMembershipSubjectContainer guiMembershipSubjectContainer: result) {
            
            GrouperObject grouperObject = guiMembershipSubjectContainer.getMembershipSubjectContainer().getGroupOwner();
            
            if (grouperObject == null) {
              grouperObject = guiMembershipSubjectContainer.getMembershipSubjectContainer().getStemOwner();
            }
            //TODO un-comment below once grouper api is able to handle attribute defs
//            if (grouperObject == null) {
//              grouperObject = guiMembershipSubjectContainer.getMembershipSubjectContainer().getAttributeDefOwner();
//            }
            
            if (grouperObject == null) {
              continue;
            }
            
            GrouperDeprovisioningOverallConfiguration config = GrouperDeprovisioningOverallConfiguration.retrieveConfiguration(grouperObject);
            
            guiDeprovisioningContainers.add(new GuiDeprovisioningMembershipSubjectContainer(guiMembershipSubjectContainer,
                config.isShowForRemoval(), config.isAutoselectForRemoval()));
          }
          
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
    
    deprovisioningContainer.assertDeprovisioningEnabledAndAllowed();
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    try {
      grouperSession = GrouperSession.start(loggedInSubject);
      
      GrouperDeprovisioningAffiliation deprovisioningAffiliation = retrieveAffiliation(request, loggedInSubject);
      
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
      
      if (membershipsIds.size() == 0) {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("deprovisioningRemoveNoAssignmentsSelects")));
        return;
      }
      
      int successes = 0;
      int failures = 0;
      
      Subject subject = null;
      for (String membershipId : membershipsIds) {
        try {
          Membership membership = MembershipFinder.findByUuid(GrouperSession.start(loggedInSubject), membershipId, false, true);
          
          if (subject == null) {
            String subjectId = membership.getMemberSubjectId();
            subject = SubjectFinder.findById(subjectId, true);
          }
          
          Group ownerGroup = membership.getOwnerGroupId() != null ? membership.getOwnerGroup() : null;
          if (ownerGroup != null) {
            ownerGroup.deleteMember(membership.getMember(), false);
          }
          
          AttributeDef ownerAttributeDef = membership.getOwnerAttrDefId() != null ?
              membership.getOwnerAttributeDef() : null;
              
          if (ownerAttributeDef != null) {
            for (Privilege priv: AttributeDefPrivilege.ALL_PRIVILEGES) {
              ownerAttributeDef.getPrivilegeDelegate().revokePriv(subject, priv, false);
            }
          }
          
          Stem ownerStem = membership.getOwnerStemId() != null ? membership.getOwnerStem(): null;
          if (ownerStem != null) {
            
            for (Privilege priv: NamingPrivilege.ALL_PRIVILEGES) {
              ownerStem.revokePriv(subject, priv, false); 
            }
          }
          
          successes++;
        } catch (Exception e) {
          LOG.warn("Error with membership: " + membershipId + ", user: " + loggedInSubject, e);
          failures++;
        }
      }
      
      if (failures == 0 && deprovisioningAffiliation.deprovisionSubject(subject)) {
        
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
      
      GrouperDeprovisioningAffiliation  deprovisioningAffiliation = retrieveAffiliation(request, loggedInSubject);
      
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
      
      GrouperDeprovisioningAffiliation  deprovisioningAffiliation = retrieveAffiliation(request, loggedInSubject);
      
      if (deprovisioningAffiliation == null) {
        return;
      }
      
      Set<Member> usersWhoHaveBeenDeprovisioned = deprovisioningAffiliation.getUsersWhoHaveBeenDeprovisioned();
      
      deprovisioningContainer.setDeprovisionedGuiMembers(GuiMember.convertFromMembers(usersWhoHaveBeenDeprovisioned));
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId",
          "/WEB-INF/grouperUi2/deprovisioning/deprovisioningMain.jsp"));
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#deprovisioningUsers",
          "/WEB-INF/grouperUi2/deprovisioning/deprovisioningMainHelper.jsp"));
      
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
    
    deprovisioningContainer.assertDeprovisioningEnabledAndAllowed();
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
      
      final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      
      GrouperDeprovisioningAffiliation  deprovisioningAffiliation = retrieveAffiliation(request, loggedInSubject);
      
      if (deprovisioningAffiliation == null) {
        return;
      }
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#deprovisioningUsers",
          "/WEB-INF/grouperUi2/deprovisioning/deprovisioningUserSearch.jsp"));
            
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }
  
  private static GrouperDeprovisioningAffiliation retrieveAffiliation(HttpServletRequest request, Subject subject) {
    
    final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    final GrouperRequestContainer grouperRequestContainer = GrouperRequestContainer.retrieveFromRequestOrCreate();
    final DeprovisioningContainer deprovisioningContainer = grouperRequestContainer.getDeprovisioningContainer();
    
    String affiliation = request.getParameter("affiliation");
    if (StringUtils.isBlank(affiliation)) {
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
          TextContainer.retrieveFromRequest().getText().get("deprovisioningNoAffiliationSelected")));
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
  
}