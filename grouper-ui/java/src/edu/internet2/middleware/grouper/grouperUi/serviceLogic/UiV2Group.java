package edu.internet2.middleware.grouper.grouperUi.serviceLogic;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.FieldFinder;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiGroup;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiSubject;
import edu.internet2.middleware.grouper.grouperUi.beans.dojo.DojoComboDataResponse;
import edu.internet2.middleware.grouper.grouperUi.beans.dojo.DojoComboDataResponseItem;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiResponseJs;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction.GuiMessageType;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GroupContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GrouperRequestContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.TextContainer;
import edu.internet2.middleware.grouper.membership.MembershipType;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.Privilege;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.ui.exceptions.ControllerDone;
import edu.internet2.middleware.grouper.ui.util.GrouperUiConfig;
import edu.internet2.middleware.grouper.ui.util.GrouperUiUserData;
import edu.internet2.middleware.grouper.ui.util.GrouperUiUtils;
import edu.internet2.middleware.grouper.ui.util.HttpContentType;
import edu.internet2.middleware.grouper.userData.GrouperUserDataApi;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * operations in the group screen
 * @author mchyzer
 *
 */
public class UiV2Group {

  
  /**
   * results from retrieving results
   *
   */
  private static class RetrieveGroupHelperResult {
  
    /**
     * group
     */
    private Group group;
  
    /**
     * group
     * @return group
     */
    public Group getGroup() {
      return this.group;
    }
  
    /**
     * group
     * @param group1
     */
    public void setGroup(Group group1) {
      this.group = group1;
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
   * view group
   * @param request
   * @param response
   */
  public void viewGroup(HttpServletRequest request, HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    Group group = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      group = retrieveGroupHelper(request, AccessPrivilege.VIEW).getGroup();
      
      if (group == null) {
        return;
      }
  
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/group/viewGroup.jsp"));

      if (GrouperRequestContainer.retrieveFromRequestOrCreate().getGroupContainer().isCanRead()) {
        filterHelper(request, response, group);
      }
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
  
  
      Group group = retrieveGroupHelper(request, AccessPrivilege.READ).getGroup();
      
      if (group == null) {
        return;
      }
  
      filterHelper(request, response, group);
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }

  /**
   * the filter button was pressed, or paging or sorting, or view Group or something
   * @param request
   * @param response
   */
  private void filterHelper(HttpServletRequest request, HttpServletResponse response, Group group) {
    
    GrouperRequestContainer grouperRequestContainer = GrouperRequestContainer.retrieveFromRequestOrCreate();
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    String filterText = request.getParameter("filterText");
    grouperRequestContainer.getGroupContainer().setFilterText(filterText);
    
    String pageSizeString = request.getParameter("pagingTagPageSize");
    int pageSize = -1;
    if (!StringUtils.isBlank(pageSizeString)) {
      pageSize = GrouperUtil.intValue(pageSizeString);
    } else {
      pageSize = GrouperUiConfig.retrieveConfig().propertyValueInt("pager.pagesize.default", 50);
    }
    grouperRequestContainer.getGroupContainer().getGuiPaging().setPageSize(pageSize);
    
    //1 indexed
    String pageNumberString = request.getParameter("pagingTagPageNumber");
    
    int pageNumber = 1;
    if (!StringUtils.isBlank(pageNumberString)) {
      pageNumber = GrouperUtil.intValue(pageNumberString);
    }
    
    grouperRequestContainer.getGroupContainer().getGuiPaging().setPageNumber(pageNumber);
    
    guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#groupFilterResultsId", 
        "/WEB-INF/grouperUi2/group/groupContents.jsp"));
  
  }

  /**
   * combo filter
   * @param request
   * @param response
   */
  public void addMemberCombo(HttpServletRequest request, HttpServletResponse response) {

    GrouperRequestContainer grouperRequestContainer = GrouperRequestContainer.retrieveFromRequestOrCreate();
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
    GrouperSession grouperSession = null;
    
    try {
      grouperSession = GrouperSession.start(loggedInSubject);
  
      Group group = retrieveGroupHelper(request, AccessPrivilege.UPDATE).getGroup();
  
      if (group == null) {
        return;
      }
  
      GroupContainer groupContainer = grouperRequestContainer.getGroupContainer();
  
      String searchString = request.getParameter("addMemberSubjectSearch");
      
      
      boolean searchOk = GrouperUiUtils.searchStringValid(searchString);
      if (!searchOk) {
        
        guiResponseJs.addAction(GuiScreenAction.newInnerHtml("#addMemberResults", 
            TextContainer.retrieveFromRequest().getText().get("groupAddMemberNotEnoughChars")));
        return;
      }
      
      //{
      //  query: {name: "A*"},
      //  queryOptions: {ignoreCase: true},
      //  sort: [{attribute:"name", descending:false}],
      //    start: 0,
      //    count: 10
      //}
      
      //https://server.url/twoFactorMchyzer/twoFactorUi/app/UiMain.personPicker?name=ab*&start=0&count=Infinity

      //Utils.printToScreen("{\"label\":\"name\", \"identifier\":\"id\",\"items\":[{\"id\":\"10021368\",\"name\":\"Chris Hyzer (mchyzer, 10021368) (active) Staff - Astt And Information Security - Application Architect (also: Alumni)\"},{\"id\":\"10193029\",\"name\":\"Chyze-Whee Ang (angcw, 10193029) (active) Alumni\"}]}", "application/json", false, false);

      String query = StringUtils.defaultString(request.getParameter("id"));
      
      boolean isLookup = true;
      
      if (StringUtils.isBlank(query)) {

        isLookup = false;
        
        query = StringUtils.trimToEmpty(request.getParameter("name"));

      }

      DojoComboDataResponse dojoComboDataResponse = null;

      //if there is no *, then looking for a specific name, return nothing since someone just typed something in and left...
      if (!query.contains("*")) {
        
        isLookup = true;
        
      }

      Set<Subject> subjects = new LinkedHashSet<Subject>();
      boolean enterMoreChars = false;
      
      {
        String subjectId = query.endsWith("*") ? query.substring(0, query.length()-1) : query;
        if (!StringUtils.isBlank(subjectId)) {
          Subject subject = SubjectFinder.findByIdOrIdentifier(subjectId, false);
                      
          if (subject != null) {
            
            subjects.add(subject);
          }
        }
      }
      
      if (!isLookup) {
      
        //take out the asterisk
        query = StringUtils.replace(query, "*", "");
    
        //if its a blank query, then dont return anything...
        if (query.length() > 1) {
          
          String stemName = group.getParentStemName();
          
          subjects.addAll(SubjectFinder.findPage(query).getResults());
                  
        } else {
          enterMoreChars = true;
        }
      }

      if (enterMoreChars) {
        DojoComboDataResponseItem dojoComboDataResponseItem = new DojoComboDataResponseItem(null, 
            TextContainer.retrieveFromRequest().getText().get("comboNotEnoughChars"));
        dojoComboDataResponse = new DojoComboDataResponse(GrouperUtil.toList(dojoComboDataResponseItem));
      } else {

        if (subjects.size() == 0) {
          dojoComboDataResponse = new DojoComboDataResponse();
        } else {
          
          List<DojoComboDataResponseItem> items = new ArrayList<DojoComboDataResponseItem>();
    
          //convert subject to item
          for (Subject subject : subjects) {
            
            //description could be null?
            
            String description = GrouperUiUtils.escapeHtml(GrouperUiUtils.convertSubjectToLabelConfigured(subject), true);
            
            DojoComboDataResponseItem item = new DojoComboDataResponseItem(subject.getId(), description);
            items.add(item);
            
          }
          
          dojoComboDataResponse = new DojoComboDataResponse(
            GrouperUtil.toArray(items, DojoComboDataResponseItem.class));
    
        }  
      }
      String json = GrouperUtil.jsonConvertTo(dojoComboDataResponse, false);
      
      //write json to screen
      GrouperUiUtils.printToScreen(json, HttpContentType.APPLICATION_JSON, false, false);
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }

    //dont print the regular JSON
    throw new ControllerDone();

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
  
      Group group = retrieveGroupHelper(request, AccessPrivilege.UPDATE).getGroup();
  
      if (group == null) {
        return;
      }
  
      GroupContainer groupContainer = grouperRequestContainer.getGroupContainer();
  
      String searchString = request.getParameter("addMemberSubjectSearch");
      
      
      boolean searchOk = GrouperUiUtils.searchStringValid(searchString);
      if (!searchOk) {
        
        guiResponseJs.addAction(GuiScreenAction.newInnerHtml("#addMemberResults", 
            TextContainer.retrieveFromRequest().getText().get("groupAddMemberNotEnoughChars")));
        return;
      }
      
      Set<Subject> subjects = SubjectFinder.findPageInStem(group.getParentStemName(), searchString).getResults();
      
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
  
      Group group = retrieveGroupHelper(request, AccessPrivilege.ADMIN).getGroup();
  
      if (group == null) {
        return;
      }
  
      GroupContainer groupContainer = grouperRequestContainer.getGroupContainer();
  
      //?assign=false&groupId=${grouperRequestContainer.groupContainer.guiGroup.stem.id}&fieldName=${fieldName}&memberId=${guiMembershipSubjectContainer.guiMember.member.uuid}
      String assignString = request.getParameter("assign");
      boolean assign = GrouperUtil.booleanValue(assignString);
      String fieldName = request.getParameter("fieldName");
      String memberId = request.getParameter("memberId");
  
      Member member = MemberFinder.findByUuid(grouperSession, memberId, true);
      
      Privilege privilege = AccessPrivilege.listToPriv(fieldName);
      
      if (privilege == null) {
        throw new RuntimeException("Why is privilege not found???? " + fieldName);
      }
      
      //if someone revoked in the meantime, who cares...
      if (assign) {
        group.grantPriv(member.getSubject(), privilege, false);
        
        //set a success message
        //messes up screen
        //guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
        //    TextContainer.retrieveFromRequest().getText().get("stemSuccessGrantedPrivilege")));
        
      } else {
        group.revokePriv(member.getSubject(), privilege, false);
        
        //messes up screen
        //set a success message
        //guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
        //    TextContainer.retrieveFromRequest().getText().get("stemSuccessRevokedPrivilege")));
      }
  
      //reset the data (not really necessary, just in case)
      groupContainer.setPrivilegeGuiMembershipSubjectContainers(null);
      
      
      filterPrivilegesHelper(request, response, group);
  
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
  
    Group group = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      group = retrieveGroupHelper(request, AccessPrivilege.VIEW).getGroup();
      
      if (group == null) {
        return;
      }

      GrouperUserDataApi.favoriteGroupAdd(GrouperUiUserData.grouperUiGroupNameForUserData(), loggedInSubject, group);

      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
          TextContainer.retrieveFromRequest().getText().get("groupSuccessAddedToMyFavorites")));

      //redisplay so the button will change
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#groupMoreActionsButtonContentsDivId", 
          "/WEB-INF/grouperUi2/group/groupMoreActionsButtonContents.jsp"));

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
  
    Group group = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      group = retrieveGroupHelper(request, AccessPrivilege.VIEW).getGroup();
      
      if (group == null) {
        return;
      }

      GrouperUserDataApi.favoriteGroupRemove(GrouperUiUserData.grouperUiGroupNameForUserData(), loggedInSubject, group);

      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
          TextContainer.retrieveFromRequest().getText().get("groupSuccessRemovedFromMyFavorites")));

      //redisplay so the button will change
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#groupMoreActionsButtonContentsDivId", 
          "/WEB-INF/grouperUi2/group/groupMoreActionsButtonContents.jsp"));

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
  
      Group group = retrieveGroupHelper(request, AccessPrivilege.ADMIN).getGroup();
  
      if (group == null) {
        return;
      }
  
      GroupContainer groupContainer = grouperRequestContainer.getGroupContainer();
  
      //UiV2Group.assignPrivilegeBatch?groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}
      
      String groupPrivilegeBatchUpdateOperation = request.getParameter("groupPrivilegeBatchUpdateOperation");
      Pattern operationPattern = Pattern.compile("^(assign|revoke)_(.*)$");
      Matcher operationMatcher = operationPattern.matcher(groupPrivilegeBatchUpdateOperation);
      if (!operationMatcher.matches()) {
        throw new RuntimeException("Invalid submission, should have a valid operation: '" + groupPrivilegeBatchUpdateOperation + "'");
      }
      
      String assignOrRevokeString = operationMatcher.group(1);
      boolean assign = StringUtils.equals("assign", assignOrRevokeString);
      if (!assign && !StringUtils.equals("revoke", assignOrRevokeString)) {
        throw new RuntimeException("Cant find assign or revoke: '" + assignOrRevokeString + "'");
      }
      String fieldName = operationMatcher.group(2);
      
      boolean assignAll = StringUtils.equals(fieldName, "all");
      boolean readersUpdaters = StringUtils.equals(fieldName, "readersUpdaters");
      
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
          AccessPrivilege.listToPriv(Field.FIELD_NAME_ADMINS)} : new Privilege[]{  
          AccessPrivilege.listToPriv(Field.FIELD_NAME_ADMINS),
          AccessPrivilege.listToPriv(Field.FIELD_NAME_GROUP_ATTR_READERS),
          AccessPrivilege.listToPriv(Field.FIELD_NAME_GROUP_ATTR_UPDATERS),
          AccessPrivilege.listToPriv(Field.FIELD_NAME_OPTOUTS),
          AccessPrivilege.listToPriv(Field.FIELD_NAME_READERS),
          AccessPrivilege.listToPriv(Field.FIELD_NAME_VIEWERS),
          AccessPrivilege.listToPriv(Field.FIELD_NAME_UPDATERS),
          AccessPrivilege.listToPriv(Field.FIELD_NAME_OPTINS)
          } ) : (readersUpdaters ? new Privilege[]{AccessPrivilege.listToPriv(Field.FIELD_NAME_READERS),
              AccessPrivilege.listToPriv(Field.FIELD_NAME_UPDATERS)
          } : new Privilege[]{AccessPrivilege.listToPriv(fieldName)});
      
      for (Member member : members) {
        
        for (Privilege privilege : privileges) {
          if (assign) {
            changes += group.grantPriv(member.getSubject(), privilege, false) ? 1 : 0;
          } else {
            changes += group.revokePriv(member.getSubject(), privilege, false) ? 1 : 0;
          }
        }
      }
      
      //reset the data (not really necessary, just in case)
      groupContainer.setPrivilegeGuiMembershipSubjectContainers(null);
  
      if (changes > 0) {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
            TextContainer.retrieveFromRequest().getText().get(
                assign ? "groupSuccessGrantedPrivileges" : "groupSuccessRevokedPrivileges")));
      } else {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.info, 
            TextContainer.retrieveFromRequest().getText().get(
                assign ? "groupNoteNoGrantedPrivileges" : "groupNoteNoRevokedPrivileges")));
        
      }
      guiResponseJs.addAction(GuiScreenAction.newScript("guiScrollTop()"));
  
      filterPrivilegesHelper(request, response, group);
  
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  
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
  
  
      Group group = retrieveGroupHelper(request, AccessPrivilege.ADMIN).getGroup();
      
      if (group == null) {
        return;
      }
  
      filterPrivilegesHelper(request, response, group);
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }

  /**
   * the filter button was pressed for privileges, or paging or sorting, or view Group privileges or something
   * @param request
   * @param response
   */
  private void filterPrivilegesHelper(HttpServletRequest request, HttpServletResponse response, Group group) {
    
    GrouperRequestContainer grouperRequestContainer = GrouperRequestContainer.retrieveFromRequestOrCreate();
  
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
    //if filtering text in subjects
    String filterText = request.getParameter("privilegeFilterText");
    grouperRequestContainer.getGroupContainer().setPrivilegeFilterText(filterText);
    
    String privilegeFieldName = request.getParameter("privilegeField");
    if (!StringUtils.isBlank(privilegeFieldName)) {
      Field field = FieldFinder.find(privilegeFieldName, true);
      grouperRequestContainer.getGroupContainer().setPrivilegeField(field);
    }
    
    //if filtering by subjects that have a certain type
    String membershipTypeString = request.getParameter("privilegeMembershipType");
    if (!StringUtils.isBlank(membershipTypeString)) {
      MembershipType membershipType = MembershipType.valueOfIgnoreCase(membershipTypeString, true);
      grouperRequestContainer.getGroupContainer().setPrivilegeMembershipType(membershipType);
    }
    
    //how many per page
    String pageSizeString = request.getParameter("pagingTagPageSize");
    int pageSize = -1;
    if (!StringUtils.isBlank(pageSizeString)) {
      pageSize = GrouperUtil.intValue(pageSizeString);
    } else {
      pageSize = GrouperUiConfig.retrieveConfig().propertyValueInt("pager.pagesize.default", 50);
    }
    grouperRequestContainer.getGroupContainer().getPrivilegeGuiPaging().setPageSize(pageSize);
    
    //1 indexed
    String pageNumberString = request.getParameter("pagingTagPageNumber");
    
    int pageNumber = 1;
    if (!StringUtils.isBlank(pageNumberString)) {
      pageNumber = GrouperUtil.intValue(pageNumberString);
    }
    
    grouperRequestContainer.getGroupContainer().getPrivilegeGuiPaging().setPageNumber(pageNumber);
    
    
    guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#groupPrivilegeFilterResultsId", 
        "/WEB-INF/grouperUi2/group/groupPrivilegeContents.jsp"));
  
  }

  /**
   * view group privileges
   * @param request
   * @param response
   */
  public void groupPrivileges(HttpServletRequest request, HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    Group group = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      group = retrieveGroupHelper(request, AccessPrivilege.ADMIN).getGroup();
      
      if (group == null) {
        return;
      }
  
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/group/groupPrivileges.jsp"));
      filterPrivilegesHelper(request, response, group);

    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * get the group from the request
   * @param request
   * @param requirePrivilege (view is automatic)
   * @return the group finder result
   */
  private static RetrieveGroupHelperResult retrieveGroupHelper(HttpServletRequest request, Privilege requirePrivilege) {
  
    //initialize the bean
    GrouperRequestContainer grouperRequestContainer = GrouperRequestContainer.retrieveFromRequestOrCreate();
    
    RetrieveGroupHelperResult result = new RetrieveGroupHelperResult();
  
    GrouperSession grouperSession = GrouperSession.staticGrouperSession();

    GroupContainer groupContainer = grouperRequestContainer.getGroupContainer();

    Group group = null;
  
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
    String groupId = request.getParameter("groupId");
    String groupIndex = request.getParameter("groupIndex");
    String groupName = request.getParameter("groupName");
    
    boolean addedError = false;
    
    if (!StringUtils.isBlank(groupId)) {
      group = GroupFinder.findByUuid(grouperSession, groupId, false);
    } else if (!StringUtils.isBlank(groupName)) {
      group = GroupFinder.findByName(grouperSession, groupName, false);
    } else if (!StringUtils.isBlank(groupIndex)) {
      long idIndex = GrouperUtil.longValue(groupIndex);
      group = GroupFinder.findByIdIndexSecure(idIndex, false, null);
    } else {
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
          TextContainer.retrieveFromRequest().getText().get("groupCantFindGroupId")));
      addedError = true;
    }
  
    if (group != null) {
      groupContainer.setGuiGroup(new GuiGroup(group));      
      boolean privsOk = true;

      if (requirePrivilege != null) {
        if (requirePrivilege.equals(AccessPrivilege.ADMIN)) {
          if (!groupContainer.isCanAdmin()) {
            guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
                TextContainer.retrieveFromRequest().getText().get("groupNotAllowedToAdminGroup")));
            addedError = true;
            privsOk = false;
          }
        } else if (requirePrivilege.equals(AccessPrivilege.VIEW)) {
          if (!groupContainer.isCanView()) {
            guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
                TextContainer.retrieveFromRequest().getText().get("groupNotAllowedToViewGroup")));
            addedError = true;
            privsOk = false;
          }
        } else if (requirePrivilege.equals(AccessPrivilege.READ)) {
          if (!groupContainer.isCanRead()) {
            guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
                TextContainer.retrieveFromRequest().getText().get("groupNotAllowedToReadGroup")));
            addedError = true;
            privsOk = false;
          }
        } else if (requirePrivilege.equals(AccessPrivilege.UPDATE)) {
          if (!groupContainer.isCanUpdate()) {
            guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
                TextContainer.retrieveFromRequest().getText().get("groupNotAllowedToUpdateGroup")));
            addedError = true;
            privsOk = false;
          }
        }  
      }
      
      if (privsOk) {
        result.setGroup(group);
      }

    } else {
      
      if (!addedError && (!StringUtils.isBlank(groupId) || !StringUtils.isBlank(groupName) || !StringUtils.isBlank(groupIndex))) {
        result.setAddedError(true);
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("groupCantFindGroup")));
        addedError = true;
      }
      
    }
    result.setAddedError(addedError);
  
    //go back to the main screen, cant find group
    if (addedError) {
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/index/indexMain.jsp"));
    }

    return result;
  }


}
