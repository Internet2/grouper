package edu.internet2.middleware.grouper.grouperUi.serviceLogic;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.FieldFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiStem;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiResponseJs;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction.GuiMessageType;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GrouperRequestContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.StemContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.TextContainer;
import edu.internet2.middleware.grouper.membership.MembershipType;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
import edu.internet2.middleware.grouper.privs.Privilege;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.ui.util.GrouperUiConfig;
import edu.internet2.middleware.grouper.ui.util.GrouperUiUserData;
import edu.internet2.middleware.grouper.userData.GrouperUserDataApi;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * operations in the stem screen
 * @author mchyzer
 *
 */
public class UiV2Stem {

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
      String pageSizeString = request.getParameter("pagingTagPageSize");
      int pageSize = GrouperUtil.intValue(pageSizeString);
      
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
        guiResponseJs.addAction(GuiScreenAction.newScript("guiScrollTop()"));
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
    
    grouperRequestContainer.getStemContainer().setGuiStem(new GuiStem(stem));      
    
    String filterText = request.getParameter("filterText");
    grouperRequestContainer.getStemContainer().setFilterText(filterText);
    
    String pageSizeString = request.getParameter("pagingTagPageSize");
    int pageSize = -1;
    if (!StringUtils.isBlank(pageSizeString)) {
      pageSize = GrouperUtil.intValue(pageSizeString);
    } else {
      pageSize = GrouperUiConfig.retrieveConfig().propertyValueInt("pager.pagesize.default", 50);
    }
    grouperRequestContainer.getStemContainer().getGuiPaging().setPageSize(pageSize);
    
    //1 indexed
    String pageNumberString = request.getParameter("pagingTagPageNumber");
    
    int pageNumber = 1;
    if (!StringUtils.isBlank(pageNumberString)) {
      pageNumber = GrouperUtil.intValue(pageNumberString);
    }
    
    grouperRequestContainer.getStemContainer().getGuiPaging().setPageNumber(pageNumber);
    
    
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
      
      if (GrouperRequestContainer.retrieveFromRequestOrCreate().getStemContainer().isCanAdminPrivileges()) {
        filterHelper(request, response, stem);
      }
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * results from retrieving results
   *
   */
  private static class RetrieveStemHelperResult {

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
    @SuppressWarnings("unused")
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
   * get the stem from the request
   * @param request
   * @param requireStemPrivilege
   * @return the stem finder result
   */
  private static RetrieveStemHelperResult retrieveStemHelper(HttpServletRequest request, boolean requireStemPrivilege) {

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
        
      } else {
        result.setStem(stem);
      }

    } else {
      
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
    grouperRequestContainer.getStemContainer().setPrivilegeFilterText(filterText);
    
    String privilegeFieldName = request.getParameter("privilegeField");
    if (!StringUtils.isBlank(privilegeFieldName)) {
      Field field = FieldFinder.find(privilegeFieldName, true);
      grouperRequestContainer.getStemContainer().setPrivilegeField(field);
    }
    
    //if filtering by subjects that have a certain type
    String membershipTypeString = request.getParameter("privilegeMembershipType");
    if (!StringUtils.isBlank(membershipTypeString)) {
      MembershipType membershipType = MembershipType.valueOfIgnoreCase(membershipTypeString, true);
      grouperRequestContainer.getStemContainer().setPrivilegeMembershipType(membershipType);
    }
    
    //how many per page
    String pageSizeString = request.getParameter("pagingTagPageSize");
    int pageSize = -1;
    if (!StringUtils.isBlank(pageSizeString)) {
      pageSize = GrouperUtil.intValue(pageSizeString);
    } else {
      pageSize = GrouperUiConfig.retrieveConfig().propertyValueInt("pager.pagesize.default", 50);
    }
    grouperRequestContainer.getStemContainer().getPrivilegeGuiPaging().setPageSize(pageSize);
    
    //1 indexed
    String pageNumberString = request.getParameter("pagingTagPageNumber");
    
    int pageNumber = 1;
    if (!StringUtils.isBlank(pageNumberString)) {
      pageNumber = GrouperUtil.intValue(pageNumberString);
    }
    
    grouperRequestContainer.getStemContainer().getPrivilegeGuiPaging().setPageNumber(pageNumber);
    
    
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


}
