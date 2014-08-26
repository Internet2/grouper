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

import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.FieldFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.MembershipFinder;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.Stem.Scope;
import edu.internet2.middleware.grouper.StemCopy;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.StemMove;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.audit.AuditEntry;
import edu.internet2.middleware.grouper.audit.UserAuditQuery;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.exception.GrouperValidationException;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.exception.StemDeleteException;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiMembershipSubjectContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiObjectBase;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiStem;
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
import edu.internet2.middleware.grouper.grouperUi.beans.ui.StemContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.TextContainer;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.membership.MembershipSubjectContainer;
import edu.internet2.middleware.grouper.membership.MembershipType;
import edu.internet2.middleware.grouper.misc.GrouperObject;
import edu.internet2.middleware.grouper.misc.GrouperObjectFinder;
import edu.internet2.middleware.grouper.misc.GrouperObjectFinder.ObjectPrivilege;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.misc.SaveMode;
import edu.internet2.middleware.grouper.misc.SaveResultType;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
import edu.internet2.middleware.grouper.privs.Privilege;
import edu.internet2.middleware.grouper.subj.SubjectHelper;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.ui.tags.GrouperPagingTag2;
import edu.internet2.middleware.grouper.ui.util.GrouperUiConfig;
import edu.internet2.middleware.grouper.ui.util.GrouperUiUserData;
import edu.internet2.middleware.grouper.ui.util.GrouperUiUtils;
import edu.internet2.middleware.grouper.userData.GrouperUserDataApi;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

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
        .addPrivilege(NamingPrivilege.STEM).assignSubject(loggedInSubject)
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
        
        Stem theStem = new StemFinder().addPrivilege(NamingPrivilege.STEM).assignSubject(loggedInSubject)
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

        return stemFinder.addPrivilege(NamingPrivilege.STEM).assignScope(query).assignSubject(loggedInSubject)
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
      
      final Stem parentFolder = StringUtils.isBlank(parentFolderId) ? null : new StemFinder().addPrivilege(NamingPrivilege.STEM)
          .assignSubject(loggedInSubject)
          .assignScope(parentFolderId).assignFindByUuidOrName(true).findStem();
      
      if (parentFolder == null) {
        
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("stemCopyCantFindParentStemId")));

        return;
        
      }

      //MCH 20131224: dont need this since we are searching by stemmed folders above
      //{
      //  //make sure the user can stem the parent folder
      //  boolean canStemParent = (Boolean)GrouperSession.callbackGrouperSession(
      //      GrouperSession.staticGrouperSession().internal_getRootSession(), new GrouperSessionHandler() {
      //        
      //        @Override
      //        public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
      //          return parentFolder.hasStem(loggedInSubject);
      //        }
      //      });
      //
      //  if (!canStemParent) {
      //
      //    guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
      //        TextContainer.retrieveFromRequest().getText().get("stemCopyCantStemParent")));
      //    return;
      //
      //  }
      //}
      
      Stem newStem = null;
      
      try {

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

      boolean changed = false;
      
      //see if we are changing the extension
      if (!StringUtils.equals(newStem.getExtension(), extension)) {
        newStem.setExtension(extension, false);
        changed = true;
      }
      
      //see if we are changing the display extension
      if (!StringUtils.equals(newStem.getDisplayExtension(), displayExtension)) {
        newStem.setDisplayExtension(displayExtension);
        changed = true;
      }

      //save it if we need to
      if (changed) {
        newStem.store();
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
          NamingPrivilege.listToPriv(Field.FIELD_NAME_STEMMERS)} : new Privilege[]{
          NamingPrivilege.listToPriv(Field.FIELD_NAME_CREATORS),
          NamingPrivilege.listToPriv(Field.FIELD_NAME_STEMMERS),
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

      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/stem/viewStem.jsp"));
      
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
      
      final Stem parentFolder = new StemFinder().addPrivilege(NamingPrivilege.STEM)
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
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/stem/stemDelete.jsp"));
  
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
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
  
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();

      try {
  
        //get the new folder that was created
        stem.delete();
  
      } catch (InsufficientPrivilegeException ipe) {
        
        LOG.warn("Insufficient privilege exception for stem delete: " + SubjectHelper.getPretty(loggedInSubject), ipe);
        
        //go to the view stem screen
        guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2Stem.viewStem&stemId=" + stem.getId() + "')"));

        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("stemDeleteInsufficientPrivileges")));
        return;
  
      } catch (StemDeleteException sde) {
        
        LOG.warn("Error deleting stem: " + SubjectHelper.getPretty(loggedInSubject) + ", " + stem, sde);
        
        //go to the view stem screen
        guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2Stem.viewStem&stemId=" + stem.getId() + "')"));

        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("stemErrorCantDelete")));

        return;
  
      }
      
      //go to the view stem screen for the parent since this stem is deleted
      guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2Stem.viewStem&stemId=" + stem.getParentUuid() + "')"));
  
      //lets show a success message on the new screen
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
          TextContainer.retrieveFromRequest().getText().get("stemDeleteSuccess")));
      
      GrouperUserDataApi.recentlyUsedStemRemove(GrouperUiUserData.grouperUiGroupNameForUserData(), 
          loggedInSubject, stem);
  
    } finally {
      GrouperSession.stopQuietly(grouperSession);
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
      
      final Stem parentFolder = new StemFinder().addPrivilege(NamingPrivilege.STEM)
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
        subject = SubjectFinder.findByIdOrIdentifier(subjectString, false);
      }
  
      if (subject == null) {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("stemAddMemberCantFindSubject")));
        return;
      }      

      boolean stemmersChecked = GrouperUtil.booleanValue(request.getParameter("privileges_stemmers[]"), false);
      boolean creatorsChecked = GrouperUtil.booleanValue(request.getParameter("privileges_creators[]"), false);
      boolean stemAttrReadersChecked = GrouperUtil.booleanValue(request.getParameter("privileges_stemAttrReaders[]"), false);
      boolean stemAttrUpdatersChecked = GrouperUtil.booleanValue(request.getParameter("privileges_stemAttrUpdaters[]"), false);

      if (!stemmersChecked && !creatorsChecked && !stemAttrReadersChecked && !stemAttrUpdatersChecked) {
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error,
            "#stemPrivsErrorId",
            TextContainer.retrieveFromRequest().getText().get("stemAddMemberPrivRequired")));
        return;
        
      }

      
      boolean madeChanges = stem.grantPrivs(subject, stemmersChecked, creatorsChecked, stemAttrReadersChecked, stemAttrUpdatersChecked, false);

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

}
