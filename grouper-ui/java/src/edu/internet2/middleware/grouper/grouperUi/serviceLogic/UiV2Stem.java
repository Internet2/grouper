/*******************************************************************************
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
 ******************************************************************************/
package edu.internet2.middleware.grouper.grouperUi.serviceLogic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.FieldFinder;
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
import edu.internet2.middleware.grouper.Stem.StemObliterateResults;
import edu.internet2.middleware.grouper.StemCopy;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.StemMove;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.app.grouperTypes.GrouperObjectTypesAttributeValue;
import edu.internet2.middleware.grouper.app.grouperTypes.GrouperObjectTypesConfiguration;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.audit.AuditEntry;
import edu.internet2.middleware.grouper.audit.UserAuditQuery;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.exception.GrouperValidationException;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.exception.StemDeleteException;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiMembershipSubjectContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiObjectBase;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiPITMembershipView;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiRuleDefinition;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiStem;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiSubject;
import edu.internet2.middleware.grouper.grouperUi.beans.api.objectTypes.GuiGrouperObjectTypesAttributeValue;
import edu.internet2.middleware.grouper.grouperUi.beans.dojo.DojoComboLogic;
import edu.internet2.middleware.grouper.grouperUi.beans.dojo.DojoComboQueryLogic;
import edu.internet2.middleware.grouper.grouperUi.beans.dojo.DojoComboQueryLogicBase;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiPaging;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiResponseJs;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction.GuiMessageType;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiSorting;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GrouperRequestContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GuiAuditEntry;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.RulesContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.StemContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.StemDeleteContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.TextContainer;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.membership.MembershipSubjectContainer;
import edu.internet2.middleware.grouper.membership.MembershipType;
import edu.internet2.middleware.grouper.misc.CompositeType;
import edu.internet2.middleware.grouper.misc.GrouperObject;
import edu.internet2.middleware.grouper.misc.GrouperObjectFinder;
import edu.internet2.middleware.grouper.misc.GrouperObjectFinder.ObjectPrivilege;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.misc.SaveMode;
import edu.internet2.middleware.grouper.misc.SaveResultType;
import edu.internet2.middleware.grouper.pit.PITMembershipView;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.AttributeDefPrivilege;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
import edu.internet2.middleware.grouper.privs.Privilege;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.rules.RuleApi;
import edu.internet2.middleware.grouper.rules.RuleDefinition;
import edu.internet2.middleware.grouper.rules.RuleEngine;
import edu.internet2.middleware.grouper.rules.RuleFinder;
import edu.internet2.middleware.grouper.subj.GrouperSubject;
import edu.internet2.middleware.grouper.subj.SubjectBean;
import edu.internet2.middleware.grouper.subj.SubjectHelper;
import edu.internet2.middleware.grouper.subj.UnresolvableSubject;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.ui.tags.GrouperPagingTag2;
import edu.internet2.middleware.grouper.ui.util.GrouperUiConfig;
import edu.internet2.middleware.grouper.ui.util.GrouperUiUserData;
import edu.internet2.middleware.grouper.ui.util.GrouperUiUtils;
import edu.internet2.middleware.grouper.userData.GrouperUserDataApi;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectNotUniqueException;

/**
 * operations in the stem screen
 * @author mchyzer
 *
 */
public class UiV2Stem {

  /** logger */
  protected static final Log LOG = LogFactory.getLog(UiV2Stem.class);
  
  /**
   * submit button on parent folder search model dialog
   * @param request
   * @param response
   */
  public void addMemberFilter(final HttpServletRequest request, HttpServletResponse response) {
    new UiV2Group().addMemberFilter(request, response);
  }
  
  /**
   * search for a subject to add to the group
   * @param request
   * @param response
   */
  public void addMemberSearch(HttpServletRequest request, HttpServletResponse response) {
    new UiV2Group().addMemberSearch(request, response);
  }

  /**
   * submit button on parent folder search model dialog
   * @param request
   * @param response
   */
  public void stemSearchFormSubmit(final HttpServletRequest request, HttpServletResponse response) {
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    try {
      grouperSession = GrouperSession.start(loggedInSubject);
  
      stemSearchFormSubmitHelper(request, response, StemSearchType.createFolder);
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }

  /**
   * submit button on parent folder search model dialog for create groups
   * @param request
   * @param response
   */
  public void stemSearchGroupFormSubmit(final HttpServletRequest request, HttpServletResponse response) {
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    try {
      grouperSession = GrouperSession.start(loggedInSubject);
  
  
      stemSearchFormSubmitHelper(request, response, StemSearchType.createGroup);
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }
  
  /**
   * submit button on parent folder search model dialog for create attribute def names
   * @param request
   * @param response
   */
  public void stemSearchAttributeDefNameFormSubmit(final HttpServletRequest request, HttpServletResponse response) {
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    try {
      grouperSession = GrouperSession.start(loggedInSubject);
  
  
      stemSearchFormSubmitHelper(request, response, StemSearchType.createAttributeDefName);
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }

  
  
  /**
   * submit button on parent folder search model dialog
   * @param request
   * @param response
   * @param stemSearchType
   */
  private void stemSearchFormSubmitHelper(final HttpServletRequest request, HttpServletResponse response, 
      StemSearchType stemSearchType) {
   
    GrouperRequestContainer grouperRequestContainer = GrouperRequestContainer.retrieveFromRequestOrCreate();
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
     
    StemContainer stemContainer = grouperRequestContainer.getStemContainer();

    stemContainer.setStemSearchType(stemSearchType);

    String searchString = request.getParameter("stemSearch");
    
    boolean searchOk = GrouperUiUtils.searchStringValid(searchString);
    if (!searchOk) {
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtml("#folderSearchResultsId", 
          TextContainer.retrieveFromRequest().getText().get("stemSearchNotEnoughChars")));
      return;
    }
    
    stemContainer.setParentStemFilterText(searchString);
    QueryOptions queryOptions = new QueryOptions();
    
    GuiPaging guiPaging = stemContainer.getParentStemGuiPaging();
    
    GrouperPagingTag2.processRequest(request, guiPaging, queryOptions); 
    
    StemFinder stemFinder = new StemFinder().assignScope(searchString).assignSplitScope(true)
        .assignPrivileges(NamingPrivilege.CREATE_PRIVILEGES).assignSubject(loggedInSubject)
        .assignQueryOptions(queryOptions);
    
    //set of stems that match, and what memberships each subject has
    Set<Stem> results = stemFinder.findStems();

    Set<GuiStem> guiResults = GuiStem.convertFromStems(results);
    
    stemContainer.setParentStemSearchResults(guiResults);
    
    guiPaging.setTotalRecordCount(queryOptions.getQueryPaging().getTotalRecordCount());

    if (GrouperUtil.length(guiResults) == 0) {
 
      guiResponseJs.addAction(GuiScreenAction.newInnerHtml("#folderSearchResultsId", 
          TextContainer.retrieveFromRequest().getText().get("stemSearchNoStemsFound")));
      return;
    }
          
    guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#folderSearchResultsId", 
        "/WEB-INF/grouperUi2/stem/parentFolderSearchResults.jsp"));
    
  }
  
  /**
   * type of stem search
   */
  public static enum StemSearchType {
    
    /**
     * folder that can create a group
     */
    createGroup("stemSearchDescriptionNewGroups", "stemSearchGroupFormSubmit"),
    
    /**
     * folder that can create attribute def name
     */
    createAttributeDefName("stemSearchDescriptionNewAttributeDefNames", "stemSearchAttributeDefNameFormSubmit"),
    
    /**
     * folder that can create a folder
     */
    createFolder("stemSearchDescriptionNewFolders", "stemSearchFormSubmit");

    /**
     * construct with description
     * @param theKeyDescription
     */
    private StemSearchType(String theKeyDescription, String theOperationMethod) {
      this.keyDescription = theKeyDescription;
      this.operationMethod = theOperationMethod;
    }
    
    /**
     * stemSearchFormSubmit or stemSearchGroupFormSubmit etc
     */
    private String operationMethod;
    
    /**
     * stemSearchFormSubmit or stemSearchGroupFormSubmit etc
     * @return the method name
     */
    public String getOperationMethod() {
      return this.operationMethod;
    }

    /**
     * stemSearchFormSubmit or stemSearchGroupFormSubmit etc
     * @param operationMethod1
     */
    public void setOperationMethod(String operationMethod1) {
      this.operationMethod = operationMethod1;
    }

    /**
     * key for search screen description in the externalized text file
     */
    private String keyDescription;
    
    /**
     * key for search screen description in the externalized text file
     * @param theKeyDescription
     */
    public void setKeyDescription(String theKeyDescription) {
      this.keyDescription = theKeyDescription;
    }
    
    /**
     * key for search screen description in the externalized text file
     * @return description
     */
    public String getKeyDescription() {
      return this.keyDescription;
    }
    
  }

  /**
   * combo filter copy parent folder
   * @param request
   * @param response
   */
  public void createStemParentFolderFilter(final HttpServletRequest request, HttpServletResponse response) {
    stemCopyParentFolderFilter(request, response);
  }

  /**
   * combo filter copy parent folder
   * @param request
   * @param response
   */
  public void stemCopyParentFolderFilter(final HttpServletRequest request, HttpServletResponse response) {

    //run the combo logic
    DojoComboLogic.logic(request, response, new DojoComboQueryLogicBase<Stem>() {

      /**
       * @see DojoComboQueryLogic#validQueryOverride
       */
      @Override
      public boolean validQueryOverride(GrouperSession grouperSession, String query) {
        if (StringUtils.equals(query, ":")) {
          return true;
        }
        return super.validQueryOverride(grouperSession, query);
      }

      /**
       * 
       */
      @Override
      public Stem lookup(HttpServletRequest request, GrouperSession grouperSession, String query) {
        Subject loggedInSubject = grouperSession.getSubject();
        
        if (StringUtils.equalsIgnoreCase(query, TextContainer.retrieveFromRequest().getText().get("stem.root.display-name"))) {
          query = ":";
        }
        
        Stem theStem = new StemFinder().assignPrivileges(NamingPrivilege.CREATE_PRIVILEGES).assignSubject(loggedInSubject)
            .assignFindByUuidOrName(true).assignScope(query).findStem();
        return theStem;
      }

      /**
       * 
       */
      @Override
      public Collection<Stem> search(HttpServletRequest request, GrouperSession grouperSession, String query) {

        if (StringUtils.equalsIgnoreCase(query, TextContainer.retrieveFromRequest().getText().get("stem.root.display-name"))) {
          query = ":";
        }

        Subject loggedInSubject = grouperSession.getSubject();
        int stemComboSize = GrouperUiConfig.retrieveConfig().propertyValueInt("uiV2.stemComboboxResultSize", 200);
        QueryOptions queryOptions = QueryOptions.create(null, null, 1, stemComboSize);
        StemFinder stemFinder = new StemFinder();
        if (StringUtils.equals(":", query)) {

          //get the root folder
          stemFinder.assignFindByUuidOrName(true);

        }

        return stemFinder.assignPrivileges(NamingPrivilege.CREATE_PRIVILEGES).assignScope(query).assignSubject(loggedInSubject)
            .assignSplitScope(true).assignQueryOptions(queryOptions).findStems();
      }

      /**
       * 
       * @param t
       * @return
       */
      @Override
      public String retrieveId(GrouperSession grouperSession, Stem t) {
        return t.getId();
      }
      
      /**
       * 
       */
      @Override
      public String retrieveLabel(GrouperSession grouperSession, Stem t) {
        String displayName = t.isRootStem() ? TextContainer.retrieveFromRequest().getText().get("stem.root.display-name") 
            : t.getDisplayName();

        return displayName;
      }

      /**
       * 
       */
      @Override
      public String retrieveHtmlLabel(GrouperSession grouperSession, Stem t) {
        //description could be null?
        String displayName = t.isRootStem() ? TextContainer.retrieveFromRequest().getText().get("stem.root.display-name") 
            : t.getDisplayName();
        String label = GrouperUiUtils.escapeHtml(displayName, true);
        String htmlLabel = "<img src=\"../../grouperExternal/public/assets/images/folder.gif\" /> " + label;
        return htmlLabel;
      }
//   MCH 2014: dont validate here, it messes up other uses
//      /**
//       * 
//       */
//      @Override
//      public String initialValidationError(HttpServletRequest request, GrouperSession grouperSession) {
//        Stem stem = retrieveStemHelper(request, true).getStem();
//        
//        if (stem == null) {
//          return TextContainer.retrieveFromRequest().getText().get("stemCopyInsufficientPrivileges");
//        }
//        return null;
//      }
    });
    
  }

  
  /**
   * 
   * @param request
   * @param response
   */
  public void stemCopySubmit(HttpServletRequest request, HttpServletResponse response) {

    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();

    GrouperSession grouperSession = null;

    Stem stem = null;

    try {

      grouperSession = GrouperSession.start(loggedInSubject);

      stem = retrieveStemHelper(request, true).getStem();
    
      if (stem == null) {
        return;
      }

      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();

      String displayExtension = request.getParameter("displayExtension");
      String extension = request.getParameter("extension");

      boolean copyGroupAttributes = GrouperUtil.booleanValue(request.getParameter("copyGroupAttributes[]"), false);
      boolean copyListMemberships = GrouperUtil.booleanValue(request.getParameter("copyListMemberships[]"), false);
      boolean copyGroupPrivileges = GrouperUtil.booleanValue(request.getParameter("copyGroupPrivileges[]"), false);
      boolean copyListMembershipsInOtherGroups = GrouperUtil.booleanValue(request.getParameter("copyListMembershipsInOtherGroups[]"), false);
      boolean copyPrivsInOtherGroups = GrouperUtil.booleanValue(request.getParameter("copyPrivsInOtherGroups[]"), false);
      boolean copyFolderPrivs = GrouperUtil.booleanValue(request.getParameter("copyFolderPrivs[]"), false);
      
      String parentFolderId = request.getParameter("parentFolderComboName");
      
      //just get what they typed in
      if (StringUtils.isBlank(parentFolderId)) {
        parentFolderId = request.getParameter("parentFolderComboNameDisplay");
      }
      
      final Stem parentFolder = StringUtils.isBlank(parentFolderId) ? null : new StemFinder().assignPrivileges(NamingPrivilege.CREATE_PRIVILEGES)
          .assignSubject(loggedInSubject)
          .assignScope(parentFolderId).assignFindByUuidOrName(true).findStem();
      
      if (parentFolder == null) {
        
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("stemCopyCantFindParentStemId")));

        return;
        
      }

      Stem newStem = null;
      
      try {

        stem.setExtension(extension, false);
        stem.setDisplayExtension(displayExtension);
        
        //get the new folder that was created
        newStem = new StemCopy(stem, parentFolder).copyAttributes(copyGroupAttributes)
            .copyListGroupAsMember(copyListMembershipsInOtherGroups)
            .copyListMembersOfGroup(copyListMemberships)
            .copyPrivilegesOfGroup(copyGroupPrivileges)
            .copyPrivilegesOfStem(copyFolderPrivs)
            .copyGroupAsPrivilege(copyPrivsInOtherGroups).save();

      } catch (InsufficientPrivilegeException ipe) {
        
        LOG.warn("Insufficient privilege exception for stem copy: " + SubjectHelper.getPretty(loggedInSubject), ipe);
        
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("stemCopyInsufficientPrivileges")));
        return;

      }

      guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2Stem.viewStem&stemId=" + newStem.getId() + "')"));

      //lets show a success message on the new screen
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
          TextContainer.retrieveFromRequest().getText().get("stemCopySuccess")));
      
      GrouperUserDataApi.recentlyUsedStemAdd(GrouperUiUserData.grouperUiGroupNameForUserData(), 
          loggedInSubject, stem);
      GrouperUserDataApi.recentlyUsedStemAdd(GrouperUiUserData.grouperUiGroupNameForUserData(), 
          loggedInSubject, newStem);

    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }

  }
  
  
  /**
   * 
   * @param request
   * @param response
   */
  public void addToMyFavorites(HttpServletRequest request, HttpServletResponse response) {
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();

    GrouperSession grouperSession = null;

    Stem stem = null;

    try {

      grouperSession = GrouperSession.start(loggedInSubject);

      stem = retrieveStemHelper(request, false).getStem();
    
      if (stem == null) {
        return;
      }

      GrouperUserDataApi.favoriteStemAdd(GrouperUiUserData.grouperUiGroupNameForUserData(), 
          loggedInSubject, stem);

      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
          TextContainer.retrieveFromRequest().getText().get("stemSuccessAddedToMyFavorites")));

      //redisplay so the button will change
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#stemMoreActionsButtonContentsDivId", 
          "/WEB-INF/grouperUi2/stem/stemMoreActionsButtonContents.jsp"));

      GrouperUserDataApi.recentlyUsedStemAdd(GrouperUiUserData.grouperUiGroupNameForUserData(), 
          loggedInSubject, stem);

    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }

  }
  

  /**
   * ajax logic to remove from my favorites
   * @param request
   * @param response
   */
  public void removeFromMyFavorites(HttpServletRequest request, HttpServletResponse response) {
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;

    Stem stem = null;

    try {

      grouperSession = GrouperSession.start(loggedInSubject);

      stem = retrieveStemHelper(request, false).getStem();

      if (stem == null) {
        return;
      }

      GrouperUserDataApi.favoriteStemRemove(GrouperUiUserData.grouperUiGroupNameForUserData(), 
          loggedInSubject, stem);

      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
          TextContainer.retrieveFromRequest().getText().get("stemSuccessRemovedFromMyFavorites")));

      //redisplay so the button will change
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#stemMoreActionsButtonContentsDivId", 
          "/WEB-INF/grouperUi2/stem/stemMoreActionsButtonContents.jsp"));

      GrouperUserDataApi.recentlyUsedStemAdd(GrouperUiUserData.grouperUiGroupNameForUserData(), 
          loggedInSubject, stem);

    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }

  }

  /**
   * the filter button was pressed, or paging or sorting or something
   * @param request
   * @param response
   */
  public void filter(HttpServletRequest request, HttpServletResponse response) {
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
        
    GrouperSession grouperSession = null;
    try {
      grouperSession = GrouperSession.start(loggedInSubject);


      Stem stem = retrieveStemHelper(request, false).getStem();
      
      if (stem == null) {
        return;
      }

      filterHelper(request, response, stem);
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }

  /**
   * submit the main form on the privilege screen which can do batch operations on a number of rows
   * @param request
   * @param response
   */
  public void assignPrivilegeBatch(HttpServletRequest request, HttpServletResponse response) {

    GrouperRequestContainer grouperRequestContainer = GrouperRequestContainer.retrieveFromRequestOrCreate();

    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();

    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();

    GrouperSession grouperSession = null;
    
    try {
      grouperSession = GrouperSession.start(loggedInSubject);

      Stem stem = retrieveStemHelper(request, true).getStem();

      if (stem == null) {
        return;
      }

      StemContainer stemContainer = grouperRequestContainer.getStemContainer();

      //UiV2Stem.assignPrivilegeBatch?stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}
      
      String stemPrivilegeBatchUpdateOperation = request.getParameter("stemPrivilegeBatchUpdateOperation");
      Pattern operationPattern = Pattern.compile("^(assign|revoke)_(.*)$");
      Matcher operationMatcher = operationPattern.matcher(stemPrivilegeBatchUpdateOperation);
      if (!operationMatcher.matches()) {
        throw new RuntimeException("Invalid submission, should have a valid operation: '" + stemPrivilegeBatchUpdateOperation + "'");
      }
      
      String assignOrRevokeString = operationMatcher.group(1);
      boolean assign = StringUtils.equals("assign", assignOrRevokeString);
      if (!assign && !StringUtils.equals("revoke", assignOrRevokeString)) {
        throw new RuntimeException("Cant find assign or revoke: '" + assignOrRevokeString + "'");
      }
      String fieldName = operationMatcher.group(2);
      
      boolean assignAll = StringUtils.equals(fieldName, "all");
      
      //lets see how many are on a page
      int pageSize = GrouperPagingTag2.pageSize(request);
      
      //lets loop and get all the checkboxes
      Set<Member> members = new LinkedHashSet<Member>();
      
      //loop through all the checkboxes and collect all the members
      for (int i=0;i<pageSize;i++) {
        String memberId = request.getParameter("privilegeSubjectRow_" + i + "[]");
        if (!StringUtils.isBlank(memberId)) {
          Member member = MemberFinder.findByUuid(grouperSession, memberId, true);
          members.add(member);
        }
      }

      if (GrouperUtil.length(members) == 0) {

        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("stemErrorEntityRequired")));
        return;
      }
      
      int changes = 0;
      
      Privilege[] privileges = assignAll ? (assign ? new Privilege[]{
          NamingPrivilege.listToPriv(Field.FIELD_NAME_STEM_ADMINS)} : new Privilege[]{
          NamingPrivilege.listToPriv(Field.FIELD_NAME_CREATORS),
          NamingPrivilege.listToPriv(Field.FIELD_NAME_STEM_ADMINS),
          NamingPrivilege.listToPriv(Field.FIELD_NAME_STEM_ATTR_READERS),
          NamingPrivilege.listToPriv(Field.FIELD_NAME_STEM_ATTR_UPDATERS)
          } ) : new Privilege[]{NamingPrivilege.listToPriv(fieldName)};
      
      for (Member member : members) {
        
        for (Privilege privilege : privileges) {
          if (assign) {
            changes += stem.grantPriv(member.getSubject(), privilege, false) ? 1 : 0;
          } else {
            changes += stem.revokePriv(member.getSubject(), privilege, false) ? 1 : 0;
          }
        }
      }
      
      //reset the data (not really necessary, just in case)
      stemContainer.setPrivilegeGuiMembershipSubjectContainers(null);

      if (changes > 0) {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
            TextContainer.retrieveFromRequest().getText().get(
                assign ? "stemSuccessGrantedPrivileges" : "stemSuccessRevokedPrivileges")));
      } else {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.info, 
            TextContainer.retrieveFromRequest().getText().get(
                assign ? "stemNoteNoGrantedPrivileges" : "stemNoteNoRevokedPrivileges")));
        
      }
      guiResponseJs.addAction(GuiScreenAction.newScript("guiScrollTop()"));

      filterPrivilegesHelper(request, response, stem);

      GrouperUserDataApi.recentlyUsedStemAdd(GrouperUiUserData.grouperUiGroupNameForUserData(), 
          loggedInSubject, stem);

    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }

  }
  
  /**
   * assign or remove a privilege from a user, and redraw the filter screen... put a success at top
   * @param request
   * @param response
   */
  public void assignPrivilege(HttpServletRequest request, HttpServletResponse response) {

    GrouperRequestContainer grouperRequestContainer = GrouperRequestContainer.retrieveFromRequestOrCreate();

    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();

    GrouperSession grouperSession = null;
    
    try {
      grouperSession = GrouperSession.start(loggedInSubject);

      Stem stem = retrieveStemHelper(request, true).getStem();

      if (stem == null) {
        return;
      }

      StemContainer stemContainer = grouperRequestContainer.getStemContainer();

      //?assign=false&stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}&fieldName=${fieldName}&memberId=${guiMembershipSubjectContainer.guiMember.member.uuid}
      String assignString = request.getParameter("assign");
      boolean assign = GrouperUtil.booleanValue(assignString);
      String fieldName = request.getParameter("fieldName");
      String memberId = request.getParameter("memberId");

      Member member = MemberFinder.findByUuid(grouperSession, memberId, true);
      
      Privilege privilege = NamingPrivilege.listToPriv(fieldName);
      
      if (privilege == null) {
        throw new RuntimeException("Why is privilege not found???? " + fieldName);
      }
      
      //if someone revoked in the meantime, who cares...
      if (assign) {
        stem.grantPriv(member.getSubject(), privilege, false);
        
        //set a success message
        //messes up screen
        //guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
        //    TextContainer.retrieveFromRequest().getText().get("stemSuccessGrantedPrivilege")));
        
      } else {
        stem.revokePriv(member.getSubject(), privilege, false);
        
        //messes up screen
        //set a success message
        //guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
        //    TextContainer.retrieveFromRequest().getText().get("stemSuccessRevokedPrivilege")));
      }

      //reset the data (not really necessary, just in case)
      stemContainer.setPrivilegeGuiMembershipSubjectContainers(null);
      
      
      filterPrivilegesHelper(request, response, stem);

      GrouperUserDataApi.recentlyUsedStemAdd(GrouperUiUserData.grouperUiGroupNameForUserData(), 
          loggedInSubject, stem);

    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }


  }

  /**
   * the filter button was pressed, or paging or sorting, or view Stem or something
   * @param request
   * @param response
   */
  private void filterHelper(HttpServletRequest request, HttpServletResponse response, Stem stem) {
    
    GrouperRequestContainer grouperRequestContainer = GrouperRequestContainer.retrieveFromRequestOrCreate();
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    StemContainer stemContainer = grouperRequestContainer.getStemContainer();
    stemContainer.setGuiStem(new GuiStem(stem));      
    
    String filterText = request.getParameter("filterText");
    GuiPaging guiPaging = stemContainer.getGuiPaging();
    QueryOptions queryOptions = QueryOptions.create("displayExtension", true, null, null);
    
    GrouperPagingTag2.processRequest(request, guiPaging, queryOptions); 
    
    GrouperObjectFinder grouperObjectFinder = new GrouperObjectFinder()
      .assignObjectPrivilege(ObjectPrivilege.view)
      .assignParentStemId(stem.getId())
      .assignQueryOptions(queryOptions)
      .assignSplitScope(true).assignStemScope(Scope.ONE)
      .assignSubject(GrouperSession.staticGrouperSession().getSubject());

    if (!StringUtils.isBlank(filterText)) {
      grouperObjectFinder.assignFilterText(filterText);
    }

    Set<GrouperObject> results = grouperObjectFinder.findGrouperObjects();
    
    stemContainer.setChildGuiObjectsAbbreviated(GuiObjectBase.convertFromGrouperObjects(results));
    
    guiPaging.setTotalRecordCount(queryOptions.getQueryPaging().getTotalRecordCount());
    
    guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#stemFilterResultsId", 
        "/WEB-INF/grouperUi2/stem/stemContents.jsp"));

  }
  
  /**
   * view stem
   * @param request
   * @param response
   */
  public void viewStem(HttpServletRequest request, HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;

    Stem stem = null;

    try {

      grouperSession = GrouperSession.start(loggedInSubject);

      stem = retrieveStemHelper(request, false).getStem();
      
      if (stem == null) {
        return;
      }
      
      UiV2Attestation.setupAttestation(stem);  

      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/stem/viewStem.jsp"));

      if (GrouperUiUtils.isMenuRefreshOnView()) {
        guiResponseJs.addAction(GuiScreenAction.newScript("openFolderTreePathToObject(" + GrouperUiUtils.pathArrayToCurrentObject(grouperSession, stem) + ")"));
      }

      //if (GrouperRequestContainer.retrieveFromRequestOrCreate().getStemContainer().isCanAdminPrivileges()) {
        filterHelper(request, response, stem);
      //}
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * results from retrieving results
   *
   */
  public static class RetrieveStemHelperResult {

    /**
     * stem
     */
    private Stem stem;

    /**
     * stem
     * @return stem
     */
    public Stem getStem() {
      return this.stem;
    }

    /**
     * stem
     * @param stem1
     */
    public void setStem(Stem stem1) {
      this.stem = stem1;
    }
    
    /**
     * if added error to screen
     */
    private boolean addedError;

    /**
     * if added error to screen
     * @return if error
     */
    public boolean isAddedError() {
      return this.addedError;
    }

    /**
     * if added error to screen
     * @param addedError1
     */
    public void setAddedError(boolean addedError1) {
      this.addedError = addedError1;
    }
    
    
    
  }

  /**
   * get the stem from the request where the stem is required and require stem privilege is either needed or not
   * @param request
   * @param requireStemPrivilege
   * @return the stem finder result
   */
  public static RetrieveStemHelperResult retrieveStemHelper(HttpServletRequest request, boolean requireStemPrivilege) {
    return retrieveStemHelper(request, requireStemPrivilege, false, true);
  }

  /**
   * get the stem from the request
   * @param request
   * @param requireStemPrivilege
   * @return the stem finder result
   */
  public static RetrieveStemHelperResult retrieveStemHelper(HttpServletRequest request, boolean requireStemPrivilege, 
      boolean requireCreateGroupPrivilege, boolean requireStem) {

    //initialize the bean
    GrouperRequestContainer grouperRequestContainer = GrouperRequestContainer.retrieveFromRequestOrCreate();
    
    RetrieveStemHelperResult result = new RetrieveStemHelperResult();

    GrouperSession grouperSession = GrouperSession.staticGrouperSession();
    
    Stem stem = null;

    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();

    String stemId = request.getParameter("stemId");
    String stemIndex = request.getParameter("stemIndex");
    String stemName = request.getParameter("stemName");
    
    boolean addedError = false;
    
    if (!StringUtils.isBlank(stemId)) {
      if (StringUtils.equals("root", stemId)) {
        stem = StemFinder.findRootStem(grouperSession);
      } else {
        stem = StemFinder.findByUuid(grouperSession, stemId, false);
      }
    } else if (!StringUtils.isBlank(stemName)) {
      stem = StemFinder.findByName(grouperSession, stemName, false);
    } else if (!StringUtils.isBlank(stemIndex)) {
      long idIndex = GrouperUtil.longValue(stemIndex);
      stem = StemFinder.findByIdIndex(idIndex, false, null);
    } else {
      
      if (!requireStem) {
        return result;
      }
      
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
          TextContainer.retrieveFromRequest().getText().get("stemCantFindStemId")));
      addedError = true;
    }

    if (stem != null) {
      grouperRequestContainer.getStemContainer().setGuiStem(new GuiStem(stem));      

      if (requireStemPrivilege && !grouperRequestContainer.getStemContainer().isCanAdminPrivileges()) {
        
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("stemNotAllowedToAdminStem")));
        addedError = true;

      } else if (requireCreateGroupPrivilege && !grouperRequestContainer.getStemContainer().isCanCreateGroups()) {

        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("stemNotAllowedToCreateGroupsStem")));
        addedError = true;

      } else {
        result.setStem(stem);
        List<GrouperObjectTypesAttributeValue> attributeValuesForGroup = GrouperObjectTypesConfiguration.getGrouperObjectTypesAttributeValues(stem);
        grouperRequestContainer.getObjectTypeContainer().setGuiConfiguredGrouperObjectTypesAttributeValues(GuiGrouperObjectTypesAttributeValue.convertFromGrouperObjectTypesAttributeValues(attributeValuesForGroup));
      }

    } else {

      if (!requireStem) {
        return result;
      }

      if (!addedError && (!StringUtils.isBlank(stemId) || !StringUtils.isBlank(stemName) || !StringUtils.isBlank(stemIndex))) {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("stemCantFindStem")));
        addedError = true;
      }
      
    }
    result.setAddedError(addedError);
    
    //go back to the main screen, cant find stem
    if (addedError) {
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/index/indexMain.jsp"));
    }
    
    return result;
  }

  /**
   * view stem privileges
   * @param request
   * @param response
   */
  public void stemPrivileges(HttpServletRequest request, HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;

    Stem stem = null;

    try {

      grouperSession = GrouperSession.start(loggedInSubject);

      stem = retrieveStemHelper(request, true).getStem();
      
      if (stem == null) {
        return;
      }

      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();

      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/stem/stemPrivileges.jsp"));
      filterPrivilegesHelper(request, response, stem);

    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  

  /**
   * view group memberships in stem
   * @param request
   * @param response
   */
  public void groupMembershipsInFolder(HttpServletRequest request, HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;

    Stem stem = null;

    try {

      grouperSession = GrouperSession.start(loggedInSubject);

      stem = retrieveStemHelper(request, false).getStem();
      
      if (stem == null) {
        return;
      }

      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();

      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/stem/groupMembershipsInFolder.jsp"));
      groupMembershipsInFolderFilter(request, response);

    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
  /**
   * the filter button was pressed, or paging or sorting or something
   * @param request
   * @param response
   */
  public void groupMembershipsInFolderFilter(HttpServletRequest request, HttpServletResponse response) {
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
        
    GrouperSession grouperSession = null;
    
    Stem stem = null;

    try {
      grouperSession = GrouperSession.start(loggedInSubject);
  
      stem = retrieveStemHelper(request, false).getStem();
      
      if (stem == null) {
        return;
      }
  
      groupMembershipsInFolderFilterHelper(request, response, stem);
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }
  

  /**
   * the remove members button was pressed
   * @param request
   * @param response
   */
  public void removeGroupMembers(HttpServletRequest request, HttpServletResponse response) {
  
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
  
    GrouperSession grouperSession = null;
  
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
    try {
      grouperSession = GrouperSession.start(loggedInSubject);
  
      final Stem stem = retrieveStemHelper(request, false).getStem();
  
      if (stem == null) {
        return;
      }
  
      final Set<String> membershipsIds = new HashSet<String>();
      
      for (int i=0;i<1000;i++) {
        String membershipId = request.getParameter("membershipRow_" + i + "[]");
        if (!StringUtils.isBlank(membershipId)) {
          membershipsIds.add(membershipId);
        }
      }
  
      if (membershipsIds.size() == 0) {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("groupMembershipsRemoveNoSubjectSelects")));
        return;
      }

      int successes = 0;
      int failures = 0;
      
      int count = 0;
      
      Set<Group> groups = new HashSet<Group>();
      
      for (String membershipId : membershipsIds) {
        try {
          Membership membership = new MembershipFinder().addMembershipId(membershipId).findMembership(true);

          Member member = membership.getMember();
          Group group = membership.getOwnerGroup();
          
          group.deleteMember(member, false);
          groups.add(group);
          
          if (count++ < 5 && group.canHavePrivilege(loggedInSubject, AccessPrivilege.VIEW.getName(), false)) {
            GrouperUserDataApi.recentlyUsedMemberAdd(GrouperUiUserData.grouperUiGroupNameForUserData(), 
                loggedInSubject, member);

          }
          
          successes++;
        } catch (Exception e) {
          LOG.warn("Error with membership: " + membershipId + ", user: " + loggedInSubject, e);
          failures++;
        }
      }
      
      for (Group group : groups) {
        if (group.canHavePrivilege(loggedInSubject, AccessPrivilege.VIEW.getName(), false)) {
          GrouperUserDataApi.recentlyUsedGroupAdd(GrouperUiUserData.grouperUiGroupNameForUserData(), 
            loggedInSubject, group);
        }
      }
      
      GrouperRequestContainer.retrieveFromRequestOrCreate().getStemContainer().setSuccessCount(successes);
      GrouperRequestContainer.retrieveFromRequestOrCreate().getStemContainer().setFailureCount(failures);


      groupMembershipsInFolderFilter(request, response);
      
      //put this after redirect
      if (failures > 0) {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("groupDeleteMembersFromFolderErrors")));
      } else {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
            TextContainer.retrieveFromRequest().getText().get("groupDeleteMembersFromFolderSuccesses")));
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
   * @param stem
   */
  private void groupMembershipsInFolderFilterHelper(HttpServletRequest request, HttpServletResponse response, Stem stem) {
    
    GrouperRequestContainer grouperRequestContainer = GrouperRequestContainer.retrieveFromRequestOrCreate();
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    String filterText = request.getParameter("filterText");
    String membershipEnabledDisabledOptions = request.getParameter("membershipEnabledDisabledOptions");
    String membershipPITOptions = request.getParameter("membershipPITOptions");
    String membershipPITToDate = request.getParameter("membershipPITToDate");
    String membershipPITFromDate = request.getParameter("membershipPITFromDate");
    String membershipCustomCompositeOptions = request.getParameter("membershipCustomCompositeOptions");
    StemContainer stemContainer = grouperRequestContainer.getStemContainer();
    
    //if filtering by subjects that have a certain type
    String membershipTypeString = request.getParameter("membershipType");
    MembershipType membershipType = null;
    if (!StringUtils.isBlank(membershipTypeString)) {
      membershipType = MembershipType.valueOfIgnoreCase(membershipTypeString, true);
    }

    GuiPaging guiPaging = stemContainer.getGuiPaging();
    guiPaging.setTextAfterPageCount(TextContainer.retrieveFromRequest().getText().get("paging2.textAfterPageCount.groupMembershipsInFolder"));
    QueryOptions queryOptions = new QueryOptions();

    GrouperPagingTag2.processRequest(request, guiPaging, queryOptions);

    MembershipFinder membershipFinder = new MembershipFinder()
      .assignStem(stem).assignStemScope(Stem.Scope.SUB).assignCheckSecurity(true)
      .assignHasFieldForMember(false)
      .assignQueryOptionsForMember(queryOptions)
      .assignSplitScopeForMember(true);
    
    if (!StringUtils.isBlank(filterText)) {
      membershipFinder.assignScopeForMember(filterText);
    }
    
    if ("yes".equals(membershipPITOptions)) {
      stemContainer.setShowPointInTimeAudit(true);
      stemContainer.setShowEnabledStatus(false);
      
      if (StringUtils.isNotBlank(membershipPITFromDate)) {
        membershipFinder.assignPointInTimeFrom(GrouperUtil.stringToTimestamp(membershipPITFromDate));
      }
      
      if (StringUtils.isNotBlank(membershipPITToDate)) {
        membershipFinder.assignPointInTimeTo(GrouperUtil.stringToTimestamp(membershipPITToDate));
      }
      
      //set of subjects, and what memberships each subject has
      Set<Object[]> result = membershipFinder.findPITMembershipsMembers();
      
      //lets get all the subjects by member id
      Map<String, Subject> memberIdToSubject = new HashMap<String, Subject>();

      {
        Map<String, SubjectBean> memberIdToSubjectBean = new HashMap<String, SubjectBean>();
        Set<SubjectBean> subjectBeans = new HashSet<SubjectBean>();
        for (Object[] membershipResult : result) {
          Member member = (Member)membershipResult[3];
          SubjectBean subjectBean = new SubjectBean(member.getSubjectId(), member.getSubjectSourceId());
          memberIdToSubjectBean.put(member.getUuid(), subjectBean);
          subjectBeans.add(subjectBean);
        }
        Map<SubjectBean, Subject> subjectBeanToSubject = SubjectFinder.findBySubjectBeans(subjectBeans);
    
        for (String memberId : memberIdToSubjectBean.keySet()) {
          SubjectBean subjectBean = memberIdToSubjectBean.get(memberId);
          Subject subject = subjectBeanToSubject.get(subjectBean);

          if (subject == null) {
            subject = new UnresolvableSubject(subjectBean.getId(), null, subjectBean.getSourceId());  
          }
          
          memberIdToSubject.put(memberId, subject);
        }
      }
      
      Set<GuiPITMembershipView> guiPITMembershipViews = new LinkedHashSet<GuiPITMembershipView>();
      
      for (Object[] membershipResult : result) {
        PITMembershipView pitMembershipView = (PITMembershipView)membershipResult[0];
        GuiPITMembershipView guiPITMembershipView = new GuiPITMembershipView(pitMembershipView);
        String memberId = pitMembershipView.getPITMember().getSourceId();
        Subject subject = memberIdToSubject.get(memberId);
        guiPITMembershipView.setGuiSubject(new GuiSubject(subject));
        guiPITMembershipViews.add(guiPITMembershipView);
      }

      stemContainer.setGuiPITMembershipViews(guiPITMembershipViews);
    } else {
      stemContainer.setShowPointInTimeAudit(false);
      membershipFinder.assignHasMembershipTypeForMember(false);
      
      if (membershipType != null) {
        membershipFinder.assignMembershipType(membershipType);
      }
      
      stemContainer.setShowEnabledStatus(true);
      
      if ("status".equals(membershipEnabledDisabledOptions)) {
        // include enabled and disabled memberships
        membershipFinder.assignEnabled(null);
      } else if ("disabled_dates".equals(membershipEnabledDisabledOptions)) {
        // include memberships that have a disabled date
        membershipFinder.assignHasDisabledDate(true);
      } else if ("enabled_dates".equals(membershipEnabledDisabledOptions)) {
        // include memberships that have an enabled date
        membershipFinder.assignHasEnabledDate(true);
      } else {
        // default
        membershipFinder.assignEnabled(true);
        stemContainer.setShowEnabledStatus(false);
      }
      
      if (!StringUtils.isBlank(membershipCustomCompositeOptions) && !"nothing".equals(membershipCustomCompositeOptions)) {
        String groupName = GrouperConfig.retrieveConfig().getProperty("grouper.membership.customComposite.groupName." + membershipCustomCompositeOptions, null);
        String compositeType = GrouperConfig.retrieveConfig().getProperty("grouper.membership.customComposite.compositeType." + membershipCustomCompositeOptions, null);
        Group customCompositeGroup = GroupFinder.findByName(GrouperSession.staticGrouperSession(), groupName, true);
        CompositeType customCompositeType = CompositeType.valueOfIgnoreCase(compositeType);
        membershipFinder.assignCustomCompositeGroup(customCompositeGroup).assignCustomCompositeType(customCompositeType);
      }
      
      //set of subjects, and what memberships each subject has
      Set<MembershipSubjectContainer> results = membershipFinder
          .findMembershipResult().getMembershipSubjectContainers();

      stemContainer.setGuiMembershipSubjectContainers(GuiMembershipSubjectContainer.convertFromMembershipSubjectContainers(results));
    }
    
    guiPaging.setTotalRecordCount(queryOptions.getQueryPaging().getTotalRecordCount());
    
    guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#groupMembershipsInFolderResultsId", 
        "/WEB-INF/grouperUi2/stem/groupMembershipsInFolderContents.jsp"));
  
  }
  
  /**
   * remove one member from the group
   * @param request
   * @param response
   */
  public void removeGroupMember(HttpServletRequest request, HttpServletResponse response) {
  
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
  
    GrouperSession grouperSession = null;
  
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
    try {
      grouperSession = GrouperSession.start(loggedInSubject);

      final Stem stem = retrieveStemHelper(request, false).getStem();
      
      if (stem == null) {
        return;
      }
      
      String groupId = request.getParameter("groupId");
      
      Group group = GroupFinder.findByUuid(grouperSession, groupId, true);
      
      String memberId = request.getParameter("memberId");
      
      Member member = MemberFinder.findByUuid(grouperSession, memberId, false);

      //not sure why this would happen
      if (member == null) {
        
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("groupDeleteMemberCantFindMember")));
        
      } else {
      
        boolean madeChanges = group.deleteMember(member, false);
        
        if (madeChanges) {
    
          guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
              TextContainer.retrieveFromRequest().getText().get("groupDeleteMemberSuccess")));
              
        } else {
          
          //not sure why this would happen (race condition?)
          guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.info, 
              TextContainer.retrieveFromRequest().getText().get("groupDeleteMemberNoChangesSuccess")));
    
        }
      }
      
      groupMembershipsInFolderFilterHelper(request, response, stem);

      GrouperUserDataApi.recentlyUsedGroupAdd(GrouperUiUserData.grouperUiGroupNameForUserData(), 
          loggedInSubject, group);

      GrouperUserDataApi.recentlyUsedMemberAdd(GrouperUiUserData.grouperUiGroupNameForUserData(), 
          loggedInSubject, member);

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
   * remove line items from inherited privileges
   * @param request
   * @param response
   */
  public void removeInheritedPrivileges(HttpServletRequest request, HttpServletResponse response) {
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;

    Stem stem = null;

    try {

      grouperSession = GrouperSession.start(loggedInSubject);

      stem = retrieveStemHelper(request, true).getStem();
      
      if (stem == null) {
        return;
      }

      if (!GrouperRequestContainer.retrieveFromRequestOrCreate().getStemContainer().isCanUpdatePrivilegeInheritance()) {
        throw new RuntimeException("Not allowed to update privilege inheritance! " + GrouperUtil.subjectToString(loggedInSubject));
      }
      
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();

      final Set<String> ruleAttributeAssignIds = new HashSet<String>();
      
      for (int i=0;i<1000;i++) {
        String privilegeRuleRowId = request.getParameter("privilegeRuleRow_" + i + "[]");
        if (!StringUtils.isBlank(privilegeRuleRowId)) {
          ruleAttributeAssignIds.add(privilegeRuleRowId);
        }
      }
  
      if (ruleAttributeAssignIds.size() == 0) {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("stemPrivilegesInheritedRemoveNoRuleSelects")));
        return;
      }
      final int[] successes = new int[]{0};
      final int[] failures = new int[]{0};

      final Map<String, GuiRuleDefinition> assignIdToGuiRuleDefinition = new HashMap<String, GuiRuleDefinition>();
      {
        Set<GuiRuleDefinition> guiRuleDefinitions = existingPrivilegeInheritedGuiRuleDefinitions(stem);
        for (GuiRuleDefinition guiRuleDefinition : GrouperUtil.nonNull(guiRuleDefinitions)) {
          assignIdToGuiRuleDefinition.put(guiRuleDefinition.getRuleDefinition().getAttributeAssignType().getId(), guiRuleDefinition);
        }
      }
      
      final Set<GuiRuleDefinition> guiRuleDefinitionsToDelete = new HashSet<GuiRuleDefinition>();
      
      //subject has update, so this operation as root in case removing affects the membership
      GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        public Object callback(GrouperSession grouperSession2) throws GrouperSessionException {
          for (String ruleAttributeAssignId : ruleAttributeAssignIds) {
            try {
              
              GuiRuleDefinition guiRuleDefinition = assignIdToGuiRuleDefinition.get(ruleAttributeAssignId);
              
              if (guiRuleDefinition == null) {
                LOG.warn("Error with rule definition, not found: " + ruleAttributeAssignId + ", user: " + GrouperUtil.subjectToString(loggedInSubject));
                failures[0]++;
                continue;
              }
              
              guiRuleDefinitionsToDelete.add(guiRuleDefinition);
              guiRuleDefinition.getRuleDefinition().getAttributeAssignType().delete();

              successes[0]++;
            } catch (Exception e) {
              LOG.warn("Error with remove inherited privilege: " + ruleAttributeAssignId + ", user: " + GrouperUtil.subjectToString(loggedInSubject), e);
              failures[0]++;
            }
          }
          
          return null;
        }
      });
      
      // run the daemon so these privs bubble down to the sub objects
      final boolean[] DONE = new boolean[]{false};
      
      final GrouperSession GROUPER_SESSION = grouperSession;
      
      Thread thread = new Thread(new Runnable() {

        @Override
        public void run() {
          
          GrouperSession grouperSession = GrouperSession.start(GROUPER_SESSION.getSubject());
          try {
            
            if (GrouperUiConfig.retrieveConfig().propertyValueBoolean("uiV2.grouperRule.removeInheritedPrivileges.whenUnassigned", true)) {
              
              //remove the assignments...
              for (GuiRuleDefinition guiRuleDefinition : guiRuleDefinitionsToDelete) {
                
                boolean actAsRoot = GrouperUiConfig.retrieveConfig().propertyValueBoolean("uiV2.grouperRule.removeInheritedPrivileges.asRoot", true);
                RuleDefinition ruleDefinition = guiRuleDefinition.getRuleDefinition();
                
                Set<Privilege> privilegeSet = Privilege.convertNamesToPrivileges(GrouperUtil.splitTrimToList(ruleDefinition.getThen().getThenEnumArg1(), ","));
                Scope stemScope = Stem.Scope.valueOfIgnoreCase(ruleDefinition.getCheck().getCheckStemScope(), true);
                
                String subjectString = ruleDefinition.getThen().getThenEnumArg0();
                
                Subject subject = SubjectFinder.findByPackedSubjectString(subjectString, true);
                
                RuleApi.removePrivilegesIfNotAssignedByRule(actAsRoot, ruleDefinition.getAttributeAssignType().getOwnerStem(), 
                    stemScope, subject, privilegeSet, 
                    ruleDefinition.getIfCondition() == null ? null : ruleDefinition.getIfCondition().getIfConditionEnumArg0());

              }
            }                          
            DONE[0] = true;
          } catch (RuntimeException re) {
            failures[0]++;
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

      GrouperRequestContainer.retrieveFromRequestOrCreate().getStemContainer().setSuccessCount(successes[0]);
      GrouperRequestContainer.retrieveFromRequestOrCreate().getStemContainer().setFailureCount(failures[0]);

      RuleEngine.clearRuleEngineCache();

      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/stem/privilegesInheritedToObjects.jsp"));
      privilegesInheritedToObjectsHelper(request, response, stem);
      
      //put this after redirect

      if (DONE[0]) {

        if (failures[0] > 0) {
          guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
              TextContainer.retrieveFromRequest().getText().get("stemPrivilegesInheritedRemoveErrors")));
        } else {
          guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
              TextContainer.retrieveFromRequest().getText().get("stemPrivilegesInheritedRemoveSuccesses")));
        }

      } else {

        if (failures[0] > 0) {
          guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
              TextContainer.retrieveFromRequest().getText().get("stemPrivilegesInheritedRemoveErrorsNotDone")));
        } else {
          guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
              TextContainer.retrieveFromRequest().getText().get("stemPrivilegesInheritedRemoveSuccessesNotDone")));
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
   * view stem privileges
   * @param request
   * @param response
   */
  public void privilegesInheritedToObjectsInFolder(HttpServletRequest request, HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;

    Stem stem = null;

    try {

      grouperSession = GrouperSession.start(loggedInSubject);

      stem = retrieveStemHelper(request, true).getStem();
      
      if (stem == null) {
        return;
      }

      if (!GrouperRequestContainer.retrieveFromRequestOrCreate().getStemContainer().isCanReadPrivilegeInheritance()) {
        throw new RuntimeException("Not allowed to read privilege inheritance! " + GrouperUtil.subjectToString(loggedInSubject));
      }
      
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();

      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/stem/privilegesInheritedToObjects.jsp"));
      privilegesInheritedToObjectsHelper(request, response, stem);

    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * privileges Inherited To Objects in folder
   * @param request
   * @param response
   * @param stem
   */
  private void privilegesInheritedToObjectsHelper(HttpServletRequest request, HttpServletResponse response, Stem stem) {
    
    GrouperRequestContainer grouperRequestContainer = GrouperRequestContainer.retrieveFromRequestOrCreate();

    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    RulesContainer rulesContainer = grouperRequestContainer.getRulesContainer();
    
    Set<GuiRuleDefinition> guiRuleDefinitions = existingPrivilegeInheritedGuiRuleDefinitions(stem);
    
    rulesContainer.setGuiRuleDefinitions(guiRuleDefinitions);

    guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#privilegesInheritedResultsId", 
        "/WEB-INF/grouperUi2/stem/privilegesInheritedContents.jsp"));
  
  }

  /**
   * @param stem
   * @return definitions
   */
  public Set<GuiRuleDefinition> existingPrivilegeInheritedGuiRuleDefinitions(Stem stem) {
    
    Set<GuiRuleDefinition> guiRuleDefinitions = new TreeSet<GuiRuleDefinition>();
    {
      Set<RuleDefinition> groupRuleDefinitions  = RuleFinder.findGroupPrivilegeInheritRules(stem);
      for (RuleDefinition ruleDefinition : GrouperUtil.nonNull(groupRuleDefinitions)) {
        GuiRuleDefinition guiRuleDefinition = new GuiRuleDefinition(ruleDefinition);
        if (guiRuleDefinition.getOwnerGuiStem() != null) {
          guiRuleDefinitions.add(guiRuleDefinition);
        }
      }
    }
    
    {
      Set<RuleDefinition> stemRuleDefinitions  = RuleFinder.findFolderPrivilegeInheritRules(stem);
      for (RuleDefinition ruleDefinition : GrouperUtil.nonNull(stemRuleDefinitions)) {
        GuiRuleDefinition guiRuleDefinition = new GuiRuleDefinition(ruleDefinition);
        if (guiRuleDefinition.getOwnerGuiStem() != null) {
          guiRuleDefinitions.add(guiRuleDefinition);
        }
      }
    }
    
    {
      Set<RuleDefinition> attributeDefRuleDefinitions  = RuleFinder.findAttributeDefPrivilegeInheritRules(stem);
      for (RuleDefinition ruleDefinition : GrouperUtil.nonNull(attributeDefRuleDefinitions)) {
        GuiRuleDefinition guiRuleDefinition = new GuiRuleDefinition(ruleDefinition);
        if (guiRuleDefinition.getOwnerGuiStem() != null) {
          guiRuleDefinitions.add(guiRuleDefinition);
        }
      }
    }
    for (GuiRuleDefinition guiRuleDefinition : guiRuleDefinitions) {
      if (StringUtils.equals(stem.getUuid(), guiRuleDefinition.getOwnerGuiStem().getStem().getUuid())) {
        guiRuleDefinition.setDirect(true);
      }
    }
    return guiRuleDefinitions;
  }

  /**
   * the filter button was pressed for privileges, or paging or sorting, or view Stem privileges or something
   * @param request
   * @param response
   */
  private void filterPrivilegesHelper(HttpServletRequest request, HttpServletResponse response, Stem stem) {
    
    GrouperRequestContainer grouperRequestContainer = GrouperRequestContainer.retrieveFromRequestOrCreate();

    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();

    //if filtering text in subjects
    String filterText = request.getParameter("privilegeFilterText");
    
    String privilegeFieldName = request.getParameter("privilegeField");
    Field privilegeField = null;
    if (!StringUtils.isBlank(privilegeFieldName)) {
      privilegeField = FieldFinder.find(privilegeFieldName, true);
    }
    
    //if filtering by subjects that have a certain type
    String membershipTypeString = request.getParameter("privilegeMembershipType");
    MembershipType privilegeMembershipType = null;
    if (!StringUtils.isBlank(membershipTypeString)) {
      privilegeMembershipType = MembershipType.valueOfIgnoreCase(membershipTypeString, true);
    }
    
    StemContainer stemContainer = grouperRequestContainer.getStemContainer();
    GuiPaging guiPaging = stemContainer.getPrivilegeGuiPaging();
    QueryOptions queryOptions = new QueryOptions();
    
    GrouperPagingTag2.processRequest(request, guiPaging, queryOptions); 
    
    MembershipFinder membershipFinder = new MembershipFinder()
      .addStemId(stem.getId()).assignCheckSecurity(true)
      .assignHasFieldForMember(true)
      .assignHasMembershipTypeForMember(true)
      .assignEnabled(true)
      .assignQueryOptionsForMember(queryOptions)
      .assignSplitScopeForMember(true);
    
    if (privilegeMembershipType != null) {
      membershipFinder.assignMembershipType(privilegeMembershipType);
    }

    if (privilegeField != null) {
      membershipFinder.assignField(privilegeField);
    }

    if (!StringUtils.isBlank(filterText)) {
      membershipFinder.assignScopeForMember(filterText);
    }

    //set of subjects, and what privs each subject has
    Set<MembershipSubjectContainer> results = membershipFinder
        .findMembershipResult().getMembershipSubjectContainers();

    //inherit from grouperAll or Groupersystem or privilege inheritance
    MembershipSubjectContainer.considerNamingPrivilegeInheritance(results);
    
    stemContainer.setPrivilegeGuiMembershipSubjectContainers(GuiMembershipSubjectContainer.convertFromMembershipSubjectContainers(results));
    guiPaging.setTotalRecordCount(queryOptions.getQueryPaging().getTotalRecordCount());

    
    guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#stemPrivilegeFilterResultsId", 
        "/WEB-INF/grouperUi2/stem/stemPrivilegeContents.jsp"));
  
  }


  /**
   * the filter button for privileges was pressed, or paging or sorting or something
   * @param request
   * @param response
   */
  public void filterPrivileges(HttpServletRequest request, HttpServletResponse response) {
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    try {
      grouperSession = GrouperSession.start(loggedInSubject);

      Stem stem = retrieveStemHelper(request, true).getStem();
      
      if (stem == null) {
        return;
      }

      filterPrivilegesHelper(request, response, stem);
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }


  /**
   * copy stem
   * @param request
   * @param response
   */
  public void stemCopy(HttpServletRequest request, HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    Stem stem = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      stem = retrieveStemHelper(request, true).getStem();
      
      if (stem == null) {
        return;
      }
  
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/stem/stemCopy.jsp"));

    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
  /**
   * move stem
   * @param request
   * @param response
   */
  public void stemMove(HttpServletRequest request, HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    Stem stem = null;
  
    try {

      grouperSession = GrouperSession.start(loggedInSubject);
  
      stem = retrieveStemHelper(request, true).getStem();
      
      if (stem == null) {
        return;
      }
  
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/stem/stemMove.jsp"));

    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * 
   * @param request
   * @param response
   */
  public void stemMoveSubmit(HttpServletRequest request, HttpServletResponse response) {
  
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
  
    GrouperSession grouperSession = null;
  
    Stem stem = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      stem = retrieveStemHelper(request, true).getStem();
    
      if (stem == null) {
        return;
      }
  
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
      String parentFolderId = request.getParameter("parentFolderComboName");
      
      //just get what they typed in
      if (StringUtils.isBlank(parentFolderId)) {
        parentFolderId = request.getParameter("parentFolderComboNameDisplay");
      }
      
      boolean moveChangeAlternateNames = GrouperUtil.booleanValue(request.getParameter("moveChangeAlternateNames[]"), false);
      
      final Stem parentFolder = new StemFinder().addPrivilege(NamingPrivilege.STEM_ADMIN)
          .assignSubject(loggedInSubject)
          .assignScope(parentFolderId).assignFindByUuidOrName(true).findStem();
      
      if (parentFolder == null) {
        
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("stemCopyCantFindParentStemId")));
        return;
        
      }
  
      //MCH 20131224: dont need this since we are searching by stemmed folders above
      
      try {
  
        //get the new folder that was created
        new StemMove(stem, parentFolder).assignAlternateName(moveChangeAlternateNames).save();
  
      } catch (InsufficientPrivilegeException ipe) {
        
        LOG.warn("Insufficient privilege exception for stem move: " + SubjectHelper.getPretty(loggedInSubject), ipe);
        
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("stemMoveInsufficientPrivileges")));
        return;
  
      }
      
      //go to the view stem screen
      guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2Stem.viewStem&stemId=" + stem.getId() + "')"));
  
      //lets show a success message on the new screen
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
          TextContainer.retrieveFromRequest().getText().get("stemMoveSuccess")));
      
      GrouperUserDataApi.recentlyUsedStemAdd(GrouperUiUserData.grouperUiGroupNameForUserData(), 
          loggedInSubject, stem);

    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  
  }

  /**
   * delete stem (show confirm screen)
   * @param request
   * @param response
   */
  public void stemDelete(HttpServletRequest request, HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    Stem stem = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      stem = retrieveStemHelper(request, true).getStem();
      
      if (stem == null) {
        return;
      }
      
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      
      boolean ok = stemDeleteHelper(request, guiResponseJs, stem);
      
      if (ok) {
      
        guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
            "/WEB-INF/grouperUi2/stem/stemDelete.jsp"));

      }
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * @param request
   * @param guiResponseJs
   * @param stem
   * @return if should continue
   */
  public boolean stemDeleteHelper(HttpServletRequest request, GuiResponseJs guiResponseJs, Stem stem) {
    String formSubmitted = request.getParameter("formSubmitted");
    
    StemDeleteContainer stemDeleteContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getStemDeleteContainer();
    stemDeleteContainer.setEmptyStem(stem.isEmpty());

    stemDeleteContainer.setCanObliterate(stem.isCanObliterate());

    StemObliterateResults stemObliterateResults = stem.retrieveObliterateResults();
    
    stemDeleteContainer.setAttributeDefCount(stemObliterateResults.getAttributeDefCount());
    stemDeleteContainer.setAttributeDefCountTotal(stemObliterateResults.getAttributeDefCountTotal());
    stemDeleteContainer.setAttributeDefNameCount(stemObliterateResults.getAttributeDefNameCount());
    stemDeleteContainer.setAttributeDefNameCountTotal(stemObliterateResults.getAttributeDefNameCountTotal());
    stemDeleteContainer.setGroupCount(stemObliterateResults.getGroupCount());
    stemDeleteContainer.setGroupCountTotal(stemObliterateResults.getGroupCountTotal());
    stemDeleteContainer.setStemCount(stemObliterateResults.getStemCount());
    stemDeleteContainer.setStemCountTotal(stemObliterateResults.getStemCountTotal());

    // if we are here from form
    if (GrouperUtil.booleanValue(formSubmitted, false)) {

      String stemObliterate = request.getParameter("stemObliterateName");
      
      if (StringUtils.isBlank(stemObliterate)) {
        
        // shouldnt happen
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error,
            "#stemObliterateId",
            TextContainer.retrieveFromRequest().getText().get("stemObliterateRequired")));
        return false;

      }
            
      if (StringUtils.equals("deleteStem", stemObliterate)) {
        stemDeleteContainer.setObliterateType("deleteStem");
        
      } else if (StringUtils.equals("obliterateSome", stemObliterate)) {
        stemDeleteContainer.setObliterateType("obliterateSome");

        {
          String stemDeleteEmptyStems = request.getParameter("stemDeleteEmptyStemsName");
          stemDeleteContainer.setObliterateEmptyStems(GrouperUtil.booleanValue(stemDeleteEmptyStems, false));
        }
        
        {
          String stemDeleteGroups = request.getParameter("stemDeleteGroupsName");
          stemDeleteContainer.setObliterateGroups(GrouperUtil.booleanValue(stemDeleteGroups, false));
        }
        
        {
          String stemDeleteAttributeDefs = request.getParameter("stemDeleteAttributeDefsName");
          stemDeleteContainer.setObliterateAttributeDefs(GrouperUtil.booleanValue(stemDeleteAttributeDefs, false));
        }
        
        {
          String stemDeleteAttributeDefNames = request.getParameter("stemDeleteAttributeDefNamesName");
          stemDeleteContainer.setObliterateAttributeDefNames(GrouperUtil.booleanValue(stemDeleteAttributeDefNames, false));
        }
        
        {
          String stemScopeOne = request.getParameter("obliterateStemScopeOneName");
          stemDeleteContainer.setObliterateStemScopeOne(GrouperUtil.booleanValue(stemScopeOne, false));
        }
        
        {
          String stemDeleteGroupMemberships = request.getParameter("stemDeleteGroupMembershipsName");
          stemDeleteContainer.setObliterateGroupMemberships(GrouperUtil.booleanValue(stemDeleteGroupMemberships, false));
        }
        
        
      } else if (StringUtils.equals("obliterateAll", stemObliterate)) {
        stemDeleteContainer.setObliterateType("obliterateAll");
        
        String stemDeletePointInTime = request.getParameter("stemDeletePointInTimeName");
        stemDeleteContainer.setObliteratePointInTime(GrouperUtil.booleanObjectValue(stemDeletePointInTime));
        
      } else {
        throw new RuntimeException("Invalid stem obliterate: '" + stemObliterate + "'");
      }

      String stemDeleteAreYouSure = request.getParameter("stemDeleteAreYouSureName");
      
      stemDeleteContainer.setAreYouSure(GrouperUtil.booleanObjectValue(stemDeleteAreYouSure));
    }
    return true;
  }

  /**
   * hit submit on the delete stem screen
   * @param request
   * @param response
   */
  public void stemDeleteSubmit(HttpServletRequest request, HttpServletResponse response) {
  
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
  
    GrouperSession grouperSession = null;
  
    Stem stem = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      stem = retrieveStemHelper(request, true).getStem();
    
      if (stem == null) {
        return;
      }
  
      final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();

      boolean ok = stemDeleteHelper(request, guiResponseJs, stem);
      
      if (!ok) {
      
        guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
            "/WEB-INF/grouperUi2/stem/stemDelete.jsp"));
        return;
        
      }

      final StemDeleteContainer stemDeleteContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getStemDeleteContainer();

      // either direction the user needs to be sure
      if (stemDeleteContainer.getAreYouSure() == null || !stemDeleteContainer.getAreYouSure()) {
        return;
      }

      final RuntimeException[] RUNTIME_EXCEPTION = new RuntimeException[1];
      final boolean[] FINISHED = new boolean[]{false};
      
      final Stem STEM = stem;

      //go to the view stem screen for the parent since this stem is deleted
      guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2Stem.viewStem&stemId=" + stem.getParentUuid() + "')"));
  
      final List<String> messages = new ArrayList<String>();
      
      final GrouperSession GROUPER_SESSION = grouperSession;
      final Map<String, String> TEXT_CONTAINER_MAP = TextContainer.retrieveFromRequest().getText();
      Thread thread = new Thread(new Runnable() {

        public void run() {
          
          //propagate the grouper session...  note, dont do an inverse of control, not
          //sure if grouper session is thread safe...
          GrouperSession grouperSession = GrouperSession.start(GROUPER_SESSION.getSubject());

          try {
            
            stemDeleteSubmitHelper(STEM, stemDeleteContainer, loggedInSubject, messages, TEXT_CONTAINER_MAP);
            FINISHED[0] = true;

          } catch (RuntimeException re) {
            //log incase thread didnt finish when screen was drawing
            LOG.error("Error obliterating folder: '" + STEM.getName() + "'", re);
            RUNTIME_EXCEPTION[0] = re;
            GrouperSession.stopQuietly(grouperSession);
          }
        }
        
      });

      thread.start();

      GrouperUtil.threadJoin(thread, 60 * 1000);

      if (RUNTIME_EXCEPTION[0] != null) {
        throw RUNTIME_EXCEPTION[0];
      }

      if (!FINISHED[0]) {
       
        messages.add(TextContainer.retrieveFromRequest().getText().get("obliterateSuccessNotFinished"));

      } else {
        
        if ((StringUtils.equals(stemDeleteContainer.getObliterateType(), "obliterateAll") || stemDeleteContainer.isObliterateEmptyStems()) && stemDeleteContainer.getStemCount() >= 0) {

          messages.add(TextContainer.retrieveFromRequest().getText().get("stemDeleteStemsSuccess"));
          
        }

        if (StringUtils.equals(stemDeleteContainer.getObliterateType(), "obliterateSome") && stemDeleteContainer.isObliterateGroupMemberships() && stemDeleteContainer.getGroupCount() >= 0) {

          messages.add(TextContainer.retrieveFromRequest().getText().get("stemDeleteGroupMembershipsSuccess"));
          
        }

        if ((StringUtils.equals(stemDeleteContainer.getObliterateType(), "obliterateAll") || stemDeleteContainer.isObliterateGroups()) && stemDeleteContainer.getGroupCount() >= 0) {

          messages.add(TextContainer.retrieveFromRequest().getText().get("stemDeleteGroupsSuccess"));
          
        }

        if ((StringUtils.equals(stemDeleteContainer.getObliterateType(), "obliterateAll") || stemDeleteContainer.isObliterateAttributeDefs()) && stemDeleteContainer.getAttributeDefCount() >= 0) {

          messages.add(TextContainer.retrieveFromRequest().getText().get("stemDeleteAttributeDefsSuccess"));
          
        }

        if ((StringUtils.equals(stemDeleteContainer.getObliterateType(), "obliterateAll") || stemDeleteContainer.isObliterateAttributeDefNames()) && stemDeleteContainer.getAttributeDefNameCount() >= 0) {

          messages.add(TextContainer.retrieveFromRequest().getText().get("stemDeleteAttributeDefNamesSuccess"));
          
        }

        messages.add(TextContainer.retrieveFromRequest().getText().get("obliterateSuccess"));

      }
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, GrouperUtil.join(messages.iterator(), "<br />")));

    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }

  }

  /**
   * 
   * @param stem
   * @param stemDeleteContainer
   * @param loggedInSubject 
   * @param messages 
   * @param textContainerMap
   */
  public void stemDeleteSubmitHelper(final Stem stem, final StemDeleteContainer stemDeleteContainer, final Subject loggedInSubject, List<String> messages, Map<String, String> textContainerMap) {
    
    try {
      
      Scope stemScope = stemDeleteContainer.isObliterateStemScopeOne() ? Scope.ONE : Scope.SUB;
      
      if (StringUtils.equals(stemDeleteContainer.getObliterateType(), "obliterateSome")) {

        if (stemDeleteContainer.isObliterateGroups()) {
          Set<Group> groups = stem.deleteGroups(false, false, stemScope);
          stemDeleteContainer.setGroupCount(GrouperUtil.length(groups));
        } else if (stemDeleteContainer.isObliterateGroupMemberships()) {
          Set<Group> groups = stem.deleteGroupMemberships(false, false, stemScope);
          stemDeleteContainer.setGroupCount(GrouperUtil.length(groups));
        }
        if (stemDeleteContainer.isObliterateAttributeDefs()) {
          Set<AttributeDef> attributeDefs = stem.deleteAttributeDefs(false, false, stemScope);
          stemDeleteContainer.setAttributeDefCount(GrouperUtil.length(attributeDefs));
          
        } else if (stemDeleteContainer.isObliterateAttributeDefNames()) {
          Set<AttributeDefName> attributeDefNames = stem.deleteAttributeDefNames(false, false, stemScope);
          stemDeleteContainer.setAttributeDefNameCount(GrouperUtil.length(attributeDefNames));
        }
        if (stemDeleteContainer.isObliterateEmptyStems()) {
          Set<Stem> stems = stem.deleteEmptyStems(false, false, stemScope);
          for (Stem theStem : GrouperUtil.nonNull(stems)) {
            GrouperUserDataApi.recentlyUsedStemRemove(GrouperUiUserData.grouperUiGroupNameForUserData(), 
                loggedInSubject, theStem);
          }
          stemDeleteContainer.setStemCount(GrouperUtil.length(stems));
        }
        
      } else if (StringUtils.equals(stemDeleteContainer.getObliterateType(), "obliterateAll")) {
        
        final boolean grouperAdmin = PrivilegeHelper.isWheelOrRoot(GrouperSession.staticGrouperSession().getSubject());
        boolean deletePointInTime = grouperAdmin && GrouperUtil.booleanValue(stemDeleteContainer.getObliteratePointInTime(), false);
        
        stem.obliterate(false, false, deletePointInTime);
        
        StemObliterateResults stemObliterateResults = Stem.retrieveObliterateResults();
        
        stemDeleteContainer.setStemCount(stemObliterateResults.getStemCount());
        
        stemDeleteContainer.setGroupCount(stemObliterateResults.getGroupCount());

        stemDeleteContainer.setAttributeDefCount(stemObliterateResults.getAttributeDefCount());
        
        stemDeleteContainer.setAttributeDefNameCount(stemObliterateResults.getAttributeDefNameCount());

      
      } else if (StringUtils.equals(stemDeleteContainer.getObliterateType(), "deleteStem")) {
        //get the new folder that was created
        stem.delete();

        stemDeleteContainer.setStemCount(1);
        
        GrouperUserDataApi.recentlyUsedStemRemove(GrouperUiUserData.grouperUiGroupNameForUserData(), 
            loggedInSubject, stem);

      } else {
        throw new RuntimeException("Error deleting folder: '" + stem.getName() + "': cant find obliterateType: '" + stemDeleteContainer.getObliterateType() + "'");
      }
      
    } catch (InsufficientPrivilegeException ipe) {
      
      LOG.warn("Insufficient privilege exception for stem delete: " + SubjectHelper.getPretty(loggedInSubject), ipe);
      
      messages.add(textContainerMap.get("stemDeleteInsufficientPrivileges"));
      return;

    } catch (StemDeleteException sde) {
      
      LOG.warn("Error deleting stem: " + SubjectHelper.getPretty(loggedInSubject) + ", " + stem, sde);
      
      messages.add(textContainerMap.get("stemErrorCantDelete"));

      return;

    }
    
  }
  
  /**
   * combo filter create group folder
   * @param request
   * @param response
   */
  public void createGroupParentFolderFilter(final HttpServletRequest request, HttpServletResponse response) {
  
    //run the combo logic
    DojoComboLogic.logic(request, response, new DojoComboQueryLogicBase<Stem>() {
  
      /**
       * 
       */
      @Override
      public Stem lookup(HttpServletRequest request, GrouperSession grouperSession, String query) {
        Subject loggedInSubject = grouperSession.getSubject();
        Stem theStem = new StemFinder().assignPrivileges(NamingPrivilege.CREATE_PRIVILEGES).assignSubject(loggedInSubject)
            .assignFindByUuidOrName(true).assignScope(query).findStem();
        return theStem;
      }
  
      /**
       * 
       */
      @Override
      public Collection<Stem> search(HttpServletRequest request, GrouperSession grouperSession, String query) {
        Subject loggedInSubject = grouperSession.getSubject();
        int stemComboSize = GrouperUiConfig.retrieveConfig().propertyValueInt("uiV2.stemComboboxResultSize", 200);
        QueryOptions queryOptions = QueryOptions.create(null, null, 1, stemComboSize);
        return new StemFinder().assignPrivileges(NamingPrivilege.CREATE_PRIVILEGES).assignScope(query).assignSubject(loggedInSubject)
            .assignSplitScope(true).assignQueryOptions(queryOptions).findStems();
      }
  
      /**
       * 
       * @param t
       * @return
       */
      @Override
      public String retrieveId(GrouperSession grouperSession, Stem t) {
        return t.getId();
      }
      
      /**
       * 
       */
      @Override
      public String retrieveLabel(GrouperSession grouperSession, Stem t) {
        return t.getDisplayName();
      }
  
      /**
       * 
       */
      @Override
      public String retrieveHtmlLabel(GrouperSession grouperSession, Stem t) {
        //description could be null?
        String label = GrouperUiUtils.escapeHtml(t.getDisplayName(), true);
        String htmlLabel = "<img src=\"../../grouperExternal/public/assets/images/folder.gif\" /> " + label;
        return htmlLabel;
      }
  
    });
    
  }

  /**
   * new stem (show create screen)
   * @param request
   * @param response
   */
  public void newStem(HttpServletRequest request, HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      //see if there is a stem id for this
      String objectStemId = request.getParameter("objectStemId");
      
      Pattern pattern = Pattern.compile("^[a-zA-Z0-9-_]+$");
      
      if (!StringUtils.isBlank(objectStemId) && pattern.matcher(objectStemId).matches()) {
        
        GrouperRequestContainer.retrieveFromRequestOrCreate().getStemContainer().setObjectStemId(objectStemId);
        
      }
      
      UiV2Stem.retrieveStemHelper(request, false, false, false).getStem();
      
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/stem/newStem.jsp"));
  
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * new stem submit
   * @param request
   * @param response
   */
  public void newStemSubmit(HttpServletRequest request, HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
  
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
    GrouperSession grouperSession = null;
  
    Stem stem = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
      
      final boolean editIdChecked = GrouperUtil.booleanValue(request.getParameter("nameDifferentThanId[]"), false);
      final String displayExtension = request.getParameter("displayExtension");
      final String extension = editIdChecked ? request.getParameter("extension") : displayExtension;
      final String description = request.getParameter("description");
  
      String parentFolderId = request.getParameter("parentFolderComboName");
      
      //just get what they typed in
      if (StringUtils.isBlank(parentFolderId)) {
        parentFolderId = request.getParameter("parentFolderComboNameDisplay");
      }
      
      if (StringUtils.isBlank(parentFolderId)) {
        
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error,
            "#parentFolderComboErrorId",
            TextContainer.retrieveFromRequest().getText().get("stemCreateRequiredParentStemId")));
        return;
      }
      
      final Stem parentFolder = new StemFinder().assignPrivileges(NamingPrivilege.CREATE_PRIVILEGES)
          .assignSubject(loggedInSubject)
          .assignScope(parentFolderId).assignFindByUuidOrName(true).findStem();
  
      if (parentFolder == null) {
        
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error,
            "#parentFolderComboErrorId",
            TextContainer.retrieveFromRequest().getText().get("stemCreateCantFindParentStemId")));
        return;
        
      }
      
      if (StringUtils.isBlank(displayExtension)) {
        
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, 
            "#stemName",
            TextContainer.retrieveFromRequest().getText().get("stemCreateErrorDisplayExtensionRequired")));
        return;
        
      }
  
      if (StringUtils.isBlank(extension)) {
        
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, 
            "#stemId",
            TextContainer.retrieveFromRequest().getText().get("stemCreateErrorExtensionRequired")));
        return;
        
      }

      //take into account the root stem
      final String stemName = StringUtils.isBlank(parentFolder.getName()) ? extension : (parentFolder.getName() + ":" + extension);
      
      //search as an admin to see if the group exists
      stem = (Stem)GrouperSession.callbackGrouperSession(grouperSession.internal_getRootSession(), new GrouperSessionHandler() {
        
        public Object callback(GrouperSession theGrouperSession) throws GrouperSessionException {
          
          return StemFinder.findByName(theGrouperSession, stemName, false);
        }
      });

      if (stem != null) {
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, 
            editIdChecked ? "#stemId" : "#stemName",
            TextContainer.retrieveFromRequest().getText().get("stemCreateCantCreateAlreadyExists")));
        return;
      }

      try {
  
        //create the folder
        stem = new StemSave(grouperSession).assignName(parentFolder.isRootStem() ? extension : (parentFolder.getName() + ":" + extension))
            .assignDisplayExtension(displayExtension).assignDescription(description).save();

      } catch (GrouperValidationException gve) {
        handleGrouperValidationException(guiResponseJs, gve);
        return;

      } catch (InsufficientPrivilegeException ipe) {
        
        LOG.warn("Insufficient privilege exception for stem create: " + SubjectHelper.getPretty(loggedInSubject), ipe);
        
        //dont change screens
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("stemCreateInsufficientPrivileges")));
        return;
  
      } catch (Exception sde) {
        
        LOG.warn("Error creating stem: " + SubjectHelper.getPretty(loggedInSubject) + ", " + stem, sde);

        if (GrouperUiUtils.vetoHandle(guiResponseJs, sde)) {
          return;
        }

        //dont change screens
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("stemCreateError") 
            + ": " + GrouperUtil.xmlEscape(sde.getMessage(), true)));
  
        return;
  
      }
  
      //go to the view group screen
      guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2Stem.viewStem&stemId=" + stem.getId() + "')"));
  
      //lets show a success message on the new screen
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
          TextContainer.retrieveFromRequest().getText().get("stemCreateSuccess")));
  
      GrouperUserDataApi.recentlyUsedStemAdd(GrouperUiUserData.grouperUiGroupNameForUserData(), 
          loggedInSubject, stem);

    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * edit a stem, show the edit screen
   * @param request
   * @param response
   */
  public void stemEdit(HttpServletRequest request, HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    Stem stem = null;
    
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      stem = retrieveStemHelper(request, true).getStem();
      
      if (stem == null) {
        return;
      }
  
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/stem/stemEdit.jsp"));
  
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * edit stem submit
   * @param request
   * @param response
   */
  public void stemEditSubmit(HttpServletRequest request, HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
  
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
    GrouperSession grouperSession = null;
  
    Stem stem = null;
    
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      stem = retrieveStemHelper(request, true).getStem();
      
      if (stem == null) {
        return;
      }
  
      final GrouperSession GROUPER_SESSION = grouperSession;
      
      final String extension = request.getParameter("extension");
      final String displayExtension = request.getParameter("displayExtension");
      final String description = request.getParameter("description");
      final String alternateName = request.getParameter("alternateName");
      final boolean setAlternateNameIfRename = GrouperUtil.booleanValue(request.getParameter("setAlternateNameIfRename[]"), false);
      
      if (StringUtils.isBlank(displayExtension)) {
        
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, 
            "#stemName",
            TextContainer.retrieveFromRequest().getText().get("stemCreateErrorDisplayExtensionRequired")));
        return;
        
      }
  
      if (StringUtils.isBlank(extension)) {
        
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, 
            "#stemId",
            TextContainer.retrieveFromRequest().getText().get("stemCreateErrorExtensionRequired")));
        return;
        
      }
  
      try {
  
        //save the group
        StemSave stemSave = new StemSave(GROUPER_SESSION).assignUuid(stem.getId())
            .assignSaveMode(SaveMode.UPDATE)
            .assignName(stem.getParentStem().isRootStem() ? extension : 
              (stem.getParentStemName() + ":" + extension))
            .assignDisplayExtension(displayExtension)
            .assignAlternateName(alternateName)
            .assignSetAlternateNameIfRename(setAlternateNameIfRename)
            .assignDescription(description);
        stem = stemSave.save();
        
        //go to the view group screen
        guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2Stem.viewStem&stemId=" + stem.getId() + "')"));
    
        //lets show a success message on the new screen
        if (stemSave.getSaveResultType() == SaveResultType.NO_CHANGE) {
          guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.info, 
              TextContainer.retrieveFromRequest().getText().get("stemEditNoChangeNote")));
        } else {
          guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
              TextContainer.retrieveFromRequest().getText().get("stemEditSuccess")));
        }


      } catch (GrouperValidationException gve) {
        handleGrouperValidationException(guiResponseJs, gve);
        return;
        
      } catch (InsufficientPrivilegeException ipe) {
        
        LOG.warn("Insufficient privilege exception for stem edit: " + SubjectHelper.getPretty(loggedInSubject), ipe);
        
        //dont change screens
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("stemCreateInsufficientPrivileges")));
        return;
  
      } catch (Exception sde) {
        
        LOG.warn("Error edit stem: " + SubjectHelper.getPretty(loggedInSubject) + ", " + stem, sde);

        if (GrouperUiUtils.vetoHandle(guiResponseJs, sde)) {
          return;
        }

        //dont change screens
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("stemEditError") 
            + ": " + GrouperUtil.xmlEscape(sde.getMessage(), true)));
  
        return;
  
      }
      GrouperUserDataApi.recentlyUsedStemAdd(GrouperUiUserData.grouperUiGroupNameForUserData(), 
          loggedInSubject, stem);

    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * @param guiResponseJs
   * @param gve
   */
  private void handleGrouperValidationException(GuiResponseJs guiResponseJs,
      GrouperValidationException gve) {
    //  # stem validations fields too long
    //  stemValidation_stemDescriptionTooLong = Error, folder description is too long
    //  stemValidation_stemDisplayExtensionTooLong = Error, folder name is too long
    //  stemValidation_stemExtensionTooLong = Error, folder ID is too long
    //  stemValidation_stemDisplayNameTooLong = Error, the folder name causes the path to be too long, please shorten it
    //  stemValidation_stemNameTooLong = Error, the folder ID causes the ID path to be too long, please shorten it
    
    if (StringUtils.equals(Stem.VALIDATION_STEM_DESCRIPTION_TOO_LONG_KEY, gve.getGrouperValidationKey())) {
      
      guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, 
          "#stemDescription",
          TextContainer.retrieveFromRequest().getText().get("stemValidation_" + gve.getGrouperValidationKey())));
      return;
      
    } else if (StringUtils.equals(Stem.VALIDATION_STEM_EXTENSION_TOO_LONG_KEY, gve.getGrouperValidationKey())
        || StringUtils.equals(Stem.VALIDATION_STEM_NAME_TOO_LONG_KEY, gve.getGrouperValidationKey())) {

      guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, 
          "#stemId",
          TextContainer.retrieveFromRequest().getText().get("stemValidation_" + gve.getGrouperValidationKey())));
      return;
      
    } else if (StringUtils.equals(Stem.VALIDATION_STEM_DISPLAY_EXTENSION_TOO_LONG_KEY, gve.getGrouperValidationKey())
        || StringUtils.equals(Stem.VALIDATION_STEM_DISPLAY_NAME_TOO_LONG_KEY, gve.getGrouperValidationKey())) {

      guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, 
          "#stemName",
          TextContainer.retrieveFromRequest().getText().get("stemValidation_" + gve.getGrouperValidationKey())));
      return;
      
    } else {
      LOG.error("Non-fatal error, not expecting GrouperValidationException: " + gve.getGrouperValidationKey(), gve);
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, gve.getMessage()));
      return;
    }
  }

  /**
   * view audits for stem
   * @param request
   * @param response
   */
  public void viewAudits(HttpServletRequest request, HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    Stem stem = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      stem = retrieveStemHelper(request, true).getStem();
      
      if (stem == null) {
        return;
      }
  
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/stem/stemViewAudits.jsp"));
  
      viewAuditsHelper(request, response, stem);
  
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * filter audits for stem
   * @param request
   * @param response
   */
  public void viewAuditsFilter(HttpServletRequest request, HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    Stem stem = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      stem = retrieveStemHelper(request, true).getStem();
      
      if (stem == null) {
        return;
      }
  
      viewAuditsHelper(request, response, stem);
  
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * the audit filter button was pressed, or paging or sorting, or view audits or something
   * @param request
   * @param response
   */
  private void viewAuditsHelper(HttpServletRequest request, HttpServletResponse response, Stem stem) {
    
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
    
    StemContainer stemContainer = grouperRequestContainer.getStemContainer();
    
    GuiPaging guiPaging = stemContainer.getGuiPaging();
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
    stemContainer.setGuiSorting(guiSorting);
  
    guiSorting.processRequest(request);
    
    query.addAuditTypeFieldValue("stemId", stem.getId());
  
    List<AuditEntry> auditEntries = query.execute();
  
    stemContainer.setGuiAuditEntries(GuiAuditEntry.convertFromAuditEntries(auditEntries));
  
    guiPaging.setTotalRecordCount(queryOptions.getQueryPaging().getTotalRecordCount());
  
    if (GrouperUtil.length(auditEntries) == 0) {
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.info,
          TextContainer.retrieveFromRequest().getText().get("groupAuditLogNoEntriesFound")));
    }
    
    stemContainer.setAuditExtendedResults(extendedResults);
    guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#stemAuditFilterResultsId", 
        "/WEB-INF/grouperUi2/stem/stemViewAuditsContents.jsp"));
  
  }
  
  /**
   * view this stem privileges inherited from folders
   * @param request
   * @param response
   */
  public void thisStemsPrivilegesInheritedFromFolders(HttpServletRequest request, HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    Stem stem = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      stem = retrieveStemHelper(request, true, false, true).getStem();
      
      if (stem == null) {
        return;
      }
  
      if (!GrouperRequestContainer.retrieveFromRequestOrCreate().getStemContainer().isCanReadPrivilegeInheritance()) {
        throw new RuntimeException("Not allowed to read privilege inheritance! " + GrouperUtil.subjectToString(loggedInSubject));
      }

      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();

      RulesContainer rulesContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getRulesContainer();
      
      Set<GuiRuleDefinition> guiRuleDefinitions = new TreeSet<GuiRuleDefinition>();
      
      //cant be root stem :)
      if (!stem.isRootStem()) {
      
        Set<RuleDefinition> groupRuleDefinitions  = RuleFinder.findFolderPrivilegeInheritRules(stem.getParentStem());
        for (RuleDefinition ruleDefinition : GrouperUtil.nonNull(groupRuleDefinitions)) {
          GuiRuleDefinition guiRuleDefinition = new GuiRuleDefinition(ruleDefinition);
          if (guiRuleDefinition.getOwnerGuiStem() != null) {
            guiRuleDefinitions.add(guiRuleDefinition);
          }
        }
      }
      
      for (GuiRuleDefinition guiRuleDefinition : guiRuleDefinitions) {
        if (StringUtils.equals(stem.getParentStem().getUuid(), guiRuleDefinition.getOwnerGuiStem().getStem().getUuid())) {
          guiRuleDefinition.setDirect(true);
        }
      }
      rulesContainer.setGuiRuleDefinitions(guiRuleDefinitions);

      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/stem/thisFoldersPrivilegesInheritedFromFolders.jsp"));

    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }
  

  /**
   * submit button on privilege inheritance add member form pressed
   * @param request
   * @param response
   */
  public void privilegeInheritanceAddMemberSubmit(final HttpServletRequest request, final HttpServletResponse response) {
  
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
  
    GrouperSession grouperSession = null;
  
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
    try {
      grouperSession = GrouperSession.start(loggedInSubject);
  
      final Stem stem = retrieveStemHelper(request, true, false, true).getStem();
  
      if (stem == null) {
        return;
      }

      if (!GrouperRequestContainer.retrieveFromRequestOrCreate().getStemContainer().isCanUpdatePrivilegeInheritance()) {
        throw new RuntimeException("Not allowed to update privilege inheritance! " + GrouperUtil.subjectToString(loggedInSubject));
      }

      String subjectString = request.getParameter("groupAddMemberComboName");
  
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
  
      if (subject == null) {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("stemAddMemberCantFindSubject")));
        return;
      }      

      if (StringUtils.equals(subject.getSourceId(), GrouperSourceAdapter.groupSourceId())) {
        GrouperSubject grouperSubject = (GrouperSubject)subject;
        Group group = grouperSubject.internal_getGroup();
        if (!group.canHavePrivilege(loggedInSubject, AccessPrivilege.READ.getName(), false)) {
          throw new RuntimeException("Cant assign group that you cannot read! " 
              + GrouperUtil.subjectToString(loggedInSubject) + ", " + group);
        }
      }
      
      final Subject SUBJECT = subject;
      
      boolean inheritedPrivilegeStemChecked = GrouperUtil.booleanValue(request.getParameter("inherited_privilege_stem"), false);
      boolean inheritedPrivilegeGroupChecked = GrouperUtil.booleanValue(request.getParameter("inherited_privilege_group"), false);
      boolean inheritedPrivilegeAttributeDefChecked = GrouperUtil.booleanValue(request.getParameter("inherited_privilege_attributeDef"), false);
      
      if (!inheritedPrivilegeStemChecked && !inheritedPrivilegeGroupChecked && !inheritedPrivilegeAttributeDefChecked) {
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error,
            "#inheritedPrivilegeTypeErrorId",
            TextContainer.retrieveFromRequest().getText().get("stemPrivilegesInheritedAssignedToRequired")));
        return;
        
      }

      String levelsNameSubmitted = request.getParameter("levelsName");
      if (StringUtils.isBlank(levelsNameSubmitted)) {
        throw new RuntimeException("Why is levelsName blank????");
      }
      final Scope stemScope = Scope.valueOfIgnoreCase(levelsNameSubmitted, true);
      
      if (inheritedPrivilegeStemChecked) {
        
        final Set<Privilege> privileges = new HashSet<Privilege>();
        
        boolean stemAdminsChecked = GrouperUtil.booleanValue(request.getParameter("privileges_stemAdmins[]"), false);
        
        if (stemAdminsChecked) {
          privileges.add(NamingPrivilege.STEM_ADMIN);
        }
        
        boolean creatorsChecked = GrouperUtil.booleanValue(request.getParameter("privileges_creators[]"), false);

        if (creatorsChecked) {
          privileges.add(NamingPrivilege.CREATE);
        }

        boolean stemAttrReadersChecked = GrouperUtil.booleanValue(request.getParameter("privileges_stemAttrReaders[]"), false);

        if (stemAttrReadersChecked) {
          privileges.add(NamingPrivilege.STEM_ATTR_READ);
        }

        boolean stemAttrUpdatersChecked = GrouperUtil.booleanValue(request.getParameter("privileges_stemAttrUpdaters[]"), false);

        if (stemAttrUpdatersChecked) {
          privileges.add(NamingPrivilege.STEM_ATTR_UPDATE);
        }

        if (!stemAdminsChecked && !creatorsChecked && !stemAttrReadersChecked && !stemAttrUpdatersChecked) {
          guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error,
              "#stemPrivsErrorId",
              TextContainer.retrieveFromRequest().getText().get("stemPrivilegesInheritAddMemberStemPrivRequired")));
          return;
          
        }
        
        GrouperSession.callbackGrouperSession(
          GrouperSession.staticGrouperSession().internal_getRootSession(), new GrouperSessionHandler() {
            
            @Override
            public Object callback(GrouperSession grouperSession1) throws GrouperSessionException {
              RuleApi.inheritFolderPrivileges(grouperSession1.getSubject(), stem, stemScope, SUBJECT, privileges);
              return null;
            }
          }
        );
        GrouperRequestContainer.retrieveFromRequestOrCreate().getStemContainer()
          .setSuccessCount(GrouperRequestContainer.retrieveFromRequestOrCreate().getStemContainer().getSuccessCount() + 1);
      }

      // groups
      if (inheritedPrivilegeGroupChecked) {
        
        final Set<Privilege> privileges = new HashSet<Privilege>();
        
        final boolean adminChecked = GrouperUtil.booleanValue(request.getParameter("privileges_admins[]"), false);
        
        if (adminChecked) {
          privileges.add(AccessPrivilege.ADMIN);
        }
        
        final boolean updateChecked = GrouperUtil.booleanValue(request.getParameter("privileges_updaters[]"), false);
        
        if (updateChecked) {
          privileges.add(AccessPrivilege.UPDATE);
        }
        
        final boolean readChecked = GrouperUtil.booleanValue(request.getParameter("privileges_readers[]"), false);
        
        if (readChecked) {
          privileges.add(AccessPrivilege.READ);
        }
        
        final boolean viewChecked = GrouperUtil.booleanValue(request.getParameter("privileges_viewers[]"), false);
        
        if (viewChecked) {
          privileges.add(AccessPrivilege.VIEW);
        }
        
        final boolean optinChecked = GrouperUtil.booleanValue(request.getParameter("privileges_optins[]"), false);
        
        if (optinChecked) {
          privileges.add(AccessPrivilege.OPTIN);
        }
        
        final boolean optoutChecked = GrouperUtil.booleanValue(request.getParameter("privileges_optouts[]"), false);
        
        if (optoutChecked) {
          privileges.add(AccessPrivilege.OPTOUT);
        }
        
        final boolean attrReadChecked = GrouperUtil.booleanValue(request.getParameter("privileges_groupAttrReaders[]"), false);
        
        if (attrReadChecked) {
          privileges.add(AccessPrivilege.GROUP_ATTR_READ);
        }
        
        final boolean attrUpdateChecked = GrouperUtil.booleanValue(request.getParameter("privileges_groupAttrUpdaters[]"), false);
        
        if (attrUpdateChecked) {
          privileges.add(AccessPrivilege.GROUP_ATTR_UPDATE);
        }
        
        if (!adminChecked && !updateChecked && !readChecked && !viewChecked && !optinChecked && !optoutChecked
            && !attrReadChecked && !attrUpdateChecked) {
          guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error,
              "#groupPrivsErrorId",
              TextContainer.retrieveFromRequest().getText().get("stemPrivilegesInheritAddMemberGroupPrivRequired")));
          return;
          
        }
        
        GrouperSession.callbackGrouperSession(
          GrouperSession.staticGrouperSession().internal_getRootSession(), new GrouperSessionHandler() {
            
            @Override
            public Object callback(GrouperSession grouperSession1) throws GrouperSessionException {
              RuleApi.inheritGroupPrivileges(grouperSession1.getSubject(), stem, stemScope, SUBJECT, privileges);
              return null;
            }
          }
        );
        GrouperRequestContainer.retrieveFromRequestOrCreate().getStemContainer()
          .setSuccessCount(GrouperRequestContainer.retrieveFromRequestOrCreate().getStemContainer().getSuccessCount() + 1);
      }

      
      // attributes
      if (inheritedPrivilegeAttributeDefChecked) {
        
        final Set<Privilege> privileges = new HashSet<Privilege>();

        boolean adminChecked = GrouperUtil.booleanValue(request.getParameter("privileges_attrAdmins[]"), false);
        if (adminChecked) {
          privileges.add(AttributeDefPrivilege.ATTR_ADMIN);
        }

        boolean updateChecked = GrouperUtil.booleanValue(request.getParameter("privileges_attrUpdaters[]"), false);
        if (updateChecked) {
          privileges.add(AttributeDefPrivilege.ATTR_UPDATE);
        }

        boolean readChecked = GrouperUtil.booleanValue(request.getParameter("privileges_attrReaders[]"), false);
        if (readChecked) {
          privileges.add(AttributeDefPrivilege.ATTR_READ);
        }
        
        boolean viewChecked = GrouperUtil.booleanValue(request.getParameter("privileges_attrViewers[]"), false);
        if (viewChecked) {
          privileges.add(AttributeDefPrivilege.ATTR_VIEW);
        }

        boolean optinChecked = GrouperUtil.booleanValue(request.getParameter("privileges_attrOptins[]"), false);
        if (optinChecked) {
          privileges.add(AttributeDefPrivilege.ATTR_OPTIN);
        }

        boolean optoutChecked = GrouperUtil.booleanValue(request.getParameter("privileges_attrOptouts[]"), false);
        if (optoutChecked) {
          privileges.add(AttributeDefPrivilege.ATTR_OPTOUT);
        }

        boolean attrReadChecked = GrouperUtil.booleanValue(request.getParameter("privileges_attributeDefAttrReaders[]"), false);
        if (attrReadChecked) {
          privileges.add(AttributeDefPrivilege.ATTR_DEF_ATTR_READ);
        }
        
        boolean attrUpdateChecked = GrouperUtil.booleanValue(request.getParameter("privileges_attributeDefAttrUpdaters[]"), false);
        if (attrUpdateChecked) {
          privileges.add(AttributeDefPrivilege.ATTR_DEF_ATTR_UPDATE);
        }
        
        if (!adminChecked && !updateChecked && !readChecked && !viewChecked && !optinChecked && !optoutChecked
            && !attrReadChecked && !attrUpdateChecked) {
          guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error,
              "#attributeDefPrivsErrorId",
              TextContainer.retrieveFromRequest().getText().get("stemPrivilegesInheritAddMemberAttributeDefPrivRequired")));
          return;
          
        }
        
        GrouperSession.callbackGrouperSession(
          GrouperSession.staticGrouperSession().internal_getRootSession(), new GrouperSessionHandler() {
            
            @Override
            public Object callback(GrouperSession grouperSession1) throws GrouperSessionException {
              RuleApi.inheritAttributeDefPrivileges(grouperSession1.getSubject(), stem, stemScope, SUBJECT, privileges);
              return null;
            }
          }
        );
        GrouperRequestContainer.retrieveFromRequestOrCreate().getStemContainer()
          .setSuccessCount(GrouperRequestContainer.retrieveFromRequestOrCreate().getStemContainer().getSuccessCount() + 1);
      }

      // run the daemon so these privs bubble down to the sub objects
      final boolean[] DONE = new boolean[]{false};
      
      Thread thread = new Thread(new Runnable() {

        @Override
        public void run() {
          GrouperSession grouperSession = GrouperSession.startRootSession();
          try {
            
            RuleApi.runRulesForOwner(stem);
            
            // TODO change to:
            
            //RuleDefinition ruleDefinition = new RuleDefinition(attributeAssign.getId());
            //
            //if (ruleDefinition.validate() == null) {
            //  if (ruleDefinition.runDaemonOnDefinitionIfShould()) {
            //    i++;
            //  }
            //}

            
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

        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
          TextContainer.retrieveFromRequest().getText().get("stemPrivilegesInheritedAddSuccesses")));

      } else {

        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
            TextContainer.retrieveFromRequest().getText().get("stemPrivilegesInheritedAddSuccessesNotDone")));
      }
      
      privilegesInheritedToObjectsHelper(request, response, stem);

      //clear out the combo
      guiResponseJs.addAction(GuiScreenAction.newScript(
          "dijit.byId('groupAddMemberComboId').set('displayedValue', ''); " +
          "dijit.byId('groupAddMemberComboId').set('value', '');"));

      GrouperUserDataApi.recentlyUsedStemAdd(GrouperUiUserData.grouperUiGroupNameForUserData(), 
          loggedInSubject, stem);
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  
  }


  
  /**
   * submit button on add member form pressed
   * @param request
   * @param response
   */
  public void addMemberSubmit(final HttpServletRequest request, final HttpServletResponse response) {
  
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
  
    GrouperSession grouperSession = null;
  
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
    try {
      grouperSession = GrouperSession.start(loggedInSubject);
  
      final Stem stem = retrieveStemHelper(request, true, false, true).getStem();
  
      if (stem == null) {
        return;
      }
    
      String subjectString = request.getParameter("groupAddMemberComboName");
  
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
  
      if (subject == null) {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("stemAddMemberCantFindSubject")));
        return;
      }      

      boolean stemAdminsChecked = GrouperUtil.booleanValue(request.getParameter("privileges_stemAdmins[]"), false);
      boolean creatorsChecked = GrouperUtil.booleanValue(request.getParameter("privileges_creators[]"), false);
      boolean stemAttrReadersChecked = GrouperUtil.booleanValue(request.getParameter("privileges_stemAttrReaders[]"), false);
      boolean stemAttrUpdatersChecked = GrouperUtil.booleanValue(request.getParameter("privileges_stemAttrUpdaters[]"), false);

      if (!stemAdminsChecked && !creatorsChecked && !stemAttrReadersChecked && !stemAttrUpdatersChecked) {
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error,
            "#stemPrivsErrorId",
            TextContainer.retrieveFromRequest().getText().get("stemAddMemberPrivRequired")));
        return;
        
      }

      
      boolean madeChanges = stem.grantPrivs(subject, stemAdminsChecked, creatorsChecked, stemAttrReadersChecked, stemAttrUpdatersChecked, false);

      if (madeChanges) {

        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
            TextContainer.retrieveFromRequest().getText().get("stemAddMemberMadeChangesSuccess")));

        filterPrivilegesHelper(request, response, stem);

      } else {

        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.info, 
            TextContainer.retrieveFromRequest().getText().get("stemAddMemberNoChangesSuccess")));

      }

      //clear out the combo
      guiResponseJs.addAction(GuiScreenAction.newScript(
          "dijit.byId('groupAddMemberComboId').set('displayedValue', ''); " +
          "dijit.byId('groupAddMemberComboId').set('value', '');"));

      GrouperUserDataApi.recentlyUsedStemAdd(GrouperUiUserData.grouperUiGroupNameForUserData(), 
          loggedInSubject, stem);

    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  
  }
  
  /**
   * submit button on parent folder search model dialog for create attribute defs
   * @param request
   * @param response
   */
  public void stemSearchAttributeDefFormSubmit(final HttpServletRequest request, HttpServletResponse response) {
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    try {
      grouperSession = GrouperSession.start(loggedInSubject);
  
  
      stemSearchFormSubmitHelper(request, response, StemSearchType.createGroup);
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }

  /**
   * combo filter create attributeDef folder
   * @param request
   * @param response
   */
  public void createAttributeDefParentFolderFilter(final HttpServletRequest request, HttpServletResponse response) {
    createGroupParentFolderFilter(request, response);
  }
  
}
