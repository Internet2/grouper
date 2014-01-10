package edu.internet2.middleware.grouper.grouperUi.serviceLogic;

import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.Stem.Scope;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefFinder;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiGroup;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiObjectBase;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiStem;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiResponseJs;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction.GuiMessageType;
import edu.internet2.middleware.grouper.grouperUi.beans.tree.DojoTreeItem;
import edu.internet2.middleware.grouper.grouperUi.beans.tree.DojoTreeItemChild;
import edu.internet2.middleware.grouper.grouperUi.beans.tree.DojoTreeItemChild.DojoTreeItemType;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GrouperRequestContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.IndexContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.TextContainer;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.misc.GrouperObject;
import edu.internet2.middleware.grouper.misc.GrouperObjectFinder;
import edu.internet2.middleware.grouper.misc.GrouperObjectFinder.GrouperObjectFinderType;
import edu.internet2.middleware.grouper.misc.GrouperObjectFinder.ObjectPrivilege;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.AttributeDefPrivilege;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.ui.exceptions.ControllerDone;
import edu.internet2.middleware.grouper.ui.util.GrouperUiConfig;
import edu.internet2.middleware.grouper.ui.util.GrouperUiUtils;
import edu.internet2.middleware.grouper.ui.util.HttpContentType;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;


public class UiV2Main extends UiServiceLogicBase {

  
  /** logger */
  private static final Log LOG = LogFactory.getLog(UiV2Main.class);

  /**
   * search submit from upper right
   * @param request
   * @param response
   */
  public void searchSubmit(HttpServletRequest request, HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();

      String searchQuery = StringUtils.trimToEmpty(request.getParameter("searchQuery"));
      
      IndexContainer indexContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getIndexContainer();
      
      indexContainer.setSearchQuery(searchQuery);

      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/index/search.jsp"));

      searchHelper(request, response);
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * my groups
   * @param request
   * @param response
   */
  public void myGroups(HttpServletRequest request, HttpServletResponse response) {

    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();

    GrouperSession grouperSession = null;

    try {

      grouperSession = GrouperSession.start(loggedInSubject);

      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();

      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/index/myGroups.jsp"));

      
      myGroupsHelper(request, response);

    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * my groups
   * @param request
   * @param response
   */
  public void myGroupsSubmit(HttpServletRequest request, HttpServletResponse response) {

    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();

    GrouperSession grouperSession = null;

    try {

      grouperSession = GrouperSession.start(loggedInSubject);

      myGroupsHelper(request, response);

    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * my groups reset button
   * @param request
   * @param response
   */
  public void myGroupsReset(HttpServletRequest request, HttpServletResponse response) {

    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();

    GrouperSession grouperSession = null;

    try {

      grouperSession = GrouperSession.start(loggedInSubject);

      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();

      //clear out form
      guiResponseJs.addAction(GuiScreenAction.newFormFieldValue("myGroupsFilter", ""));
      
      //get the unfiltered groups
      myGroupsHelper(request, response);
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * the filter button was pressed on the my groups page, or paging or sorting, or something
   * @param request
   * @param response
   */
  private void myGroupsHelper(HttpServletRequest request, HttpServletResponse response) {

    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();

    String myGroupsFilter = StringUtils.trimToEmpty(request.getParameter("myGroupsFilter"));
    
    IndexContainer indexContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getIndexContainer();
    
    //dont give an error if 0
    if (myGroupsFilter.length() == 1) {
  
      guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, "#myGroupsFilterId",
          TextContainer.retrieveFromRequest().getText().get("myGroupsErrorNotEnoughChars")));
      
      //clear out the results
      guiResponseJs.addAction(GuiScreenAction.newInnerHtml("#myGroupsResultsId", ""));
  
      return;
    }
    
    String pageSizeString = request.getParameter("pagingTagPageSize");
    int pageSize = -1;
    if (!StringUtils.isBlank(pageSizeString)) {
      pageSize = GrouperUtil.intValue(pageSizeString);
    } else {
      pageSize = GrouperUiConfig.retrieveConfig().propertyValueInt("pager.pagesize.default", 50);
    }
    indexContainer.getMyGroupsGuiPaging().setPageSize(pageSize);
    
    //1 indexed
    String pageNumberString = request.getParameter("pagingTagPageNumber");
    
    int pageNumber = 1;
    if (!StringUtils.isBlank(pageNumberString)) {
      pageNumber = GrouperUtil.intValue(pageNumberString);
    }

    indexContainer.getMyGroupsGuiPaging().setPageNumber(pageNumber);

    QueryOptions queryOptions = QueryOptions.create("displayName", true, pageNumber, pageSize);
    queryOptions.getQueryPaging().setDoTotalCount(true);
    GroupFinder groupFinder = new GroupFinder()
      .assignSubject(GrouperSession.staticGrouperSession().getSubject())
      .assignPrivileges(AccessPrivilege.MANAGE_PRIVILEGES)
      .assignQueryOptions(queryOptions);

    if (!StringUtils.isBlank(myGroupsFilter)) {
      groupFinder.assignSplitScope(true);
      groupFinder.assignScope(myGroupsFilter);
    }
  
    Set<Group> results = groupFinder.findGroups();
    
    //this shouldnt be null, but make sure
    if (results == null) {
      results = new HashSet<Group>();
    }

    if (GrouperUtil.length(results) == 0) {
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
        TextContainer.retrieveFromRequest().getText().get("myGroupsNoResultsFound")));
    }
    
    indexContainer.setGuiGroupsUserManagesAbbreviated(GuiGroup.convertFromGroups(results));
    
    indexContainer.getMyGroupsGuiPaging().setTotalRecordCount(queryOptions.getQueryPaging().getTotalRecordCount());
    
    guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#myGroupsResultsId", 
        "/WEB-INF/grouperUi2/index/myGroupsContents.jsp"));
}

  /**
   * the search button was pressed, or paging or sorting, or something
   * @param request
   * @param response
   */
  private void searchHelper(HttpServletRequest request, HttpServletResponse response) {
    
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    String searchQuery = StringUtils.trimToEmpty(request.getParameter("searchQuery"));
    
    IndexContainer indexContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getIndexContainer();
    
    indexContainer.setSearchQuery(searchQuery);
    
    if (searchQuery.length() < 2) {

      guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, "#searchQueryId",
          TextContainer.retrieveFromRequest().getText().get("searchErrorNotEnoughChars")));
      
      //clear out the results
      guiResponseJs.addAction(GuiScreenAction.newInnerHtml("#searchResultsId", ""));

      return;
    }
    
    String pageSizeString = request.getParameter("pagingTagPageSize");
    int pageSize = -1;
    if (!StringUtils.isBlank(pageSizeString)) {
      pageSize = GrouperUtil.intValue(pageSizeString);
    } else {
      pageSize = GrouperUiConfig.retrieveConfig().propertyValueInt("pager.pagesize.default", 50);
    }
    indexContainer.getSearchGuiPaging().setPageSize(pageSize);
    
    //1 indexed
    String pageNumberString = request.getParameter("pagingTagPageNumber");
    
    int pageNumber = 1;
    if (!StringUtils.isBlank(pageNumberString)) {
      pageNumber = GrouperUtil.intValue(pageNumberString);
    }
    
    indexContainer.getSearchGuiPaging().setPageNumber(pageNumber);
    
    QueryOptions queryOptions = QueryOptions.create("displayName", true, pageNumber, pageSize);
    
    GrouperObjectFinder grouperObjectFinder = new GrouperObjectFinder()
      .assignObjectPrivilege(ObjectPrivilege.view)
      .assignQueryOptions(queryOptions)
      .assignSplitScope(true)
      .assignSubject(GrouperSession.staticGrouperSession().getSubject());

    if (!StringUtils.isBlank(searchQuery)) {
      grouperObjectFinder.assignFilterText(searchQuery);
    }

    String filterType = request.getParameter("filterType");
    if (!StringUtils.isBlank(filterType) && !StringUtils.equals("all", filterType)) {
      GrouperObjectFinderType grouperObjectFinderType = GrouperObjectFinderType.valueOfIgnoreCase(filterType, true);
      grouperObjectFinder.addGrouperObjectFinderType(grouperObjectFinderType);
    }
    
    Set<GrouperObject> results = grouperObjectFinder.findGrouperObjects();
    
    indexContainer.setSearchGuiObjectsResults(GuiObjectBase.convertFromGrouperObjects(results));
    
    indexContainer.getSearchGuiPaging().setTotalRecordCount(queryOptions.getQueryPaging().getTotalRecordCount());
    
    guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#searchResultsId", 
        "/WEB-INF/grouperUi2/index/searchContents.jsp"));

  }

  /**
   * search reset
   * @param request
   * @param response
   */
  public void searchReset(HttpServletRequest request, HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();

      //clear out the results
      guiResponseJs.addAction(GuiScreenAction.newInnerHtml("#searchResultsId", ""));
      
      //clear out form
      guiResponseJs.addAction(GuiScreenAction.newFormFieldValue("searchQuery", ""));
      guiResponseJs.addAction(GuiScreenAction.newFormFieldValue("filterType", "all"));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  
  /**
   * request for a folder menu item
   * @param request
   * @param response
   */
  public void folderMenu(HttpServletRequest httpServletRequest, HttpServletResponse response) {
    //the query string has the folder to print out.  starting with root.  undefined means there is a problem
    //System.out.println(httpServletRequest.getQueryString());
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    String folderQueryString = httpServletRequest.getQueryString();

    try {
      grouperSession = GrouperSession.start(loggedInSubject);
      
      
      String json = null;
      
      Stem stem = null;
      
      //un-url-encrypt
      if (StringUtils.equals("root", folderQueryString)) {
        stem = StemFinder.findRootStem(grouperSession);
      } else {
        int lastSlash = folderQueryString.lastIndexOf('/');
        String stemId = null;
        if (lastSlash == -1) {
          stemId = folderQueryString;
        } else {
          stemId = folderQueryString.substring(lastSlash+1, folderQueryString.length());
        }
        stem = StemFinder.findByUuid(grouperSession, stemId, true);
      }

      //find some folders inside
      //new StemFinder();
      if (stem != null) {

        int numberOfStemsInTree = GrouperUiConfig.retrieveConfig().propertyValueInt("uiV2.treeStemsOnIndexPage", 30);
        int numberOfGroupsInTree = GrouperUiConfig.retrieveConfig().propertyValueInt("uiV2.treeGroupsOnIndexPage", 30);
        Set<Stem> childrenStems = stem.getChildStems(Scope.ONE, QueryOptions.create("displayExtension", true, 1, numberOfStemsInTree));
        Set<Group> childrenGroups = stem.getChildGroups(Scope.ONE, AccessPrivilege.VIEW_PRIVILEGES, 
            QueryOptions.create("displayExtension", true, 1, numberOfGroupsInTree));

        Set<AttributeDef> childrenAttributeDefs = new AttributeDefFinder()
          .assignQueryOptions(QueryOptions.create("extension", true, 1, 10))
          .assignPrivileges(AttributeDefPrivilege.VIEW_PRIVILEGES)
          .assignSubject(GrouperSession.staticGrouperSession().getSubject())
          .assignParentStemId(stem.getId()).assignStemScope(Scope.ONE).findAttributes();
  
        Set<AttributeDefName> childrenAttributeDefNames = new AttributeDefNameFinder()
          .assignQueryOptions(QueryOptions.create("displayExtension", true, 1, 10))
          .assignPrivileges(AttributeDefPrivilege.VIEW_PRIVILEGES)
          .assignSubject(GrouperSession.staticGrouperSession().getSubject())
          .assignParentStemId(stem.getId()).assignStemScope(Scope.ONE).findAttributeNames();
        
        String displayExtension = stem.isRootStem() ? 
            TextContainer.retrieveFromRequest().getText().get("stem.root.display-name") 
            : stem.getDisplayExtension();

        //the id has to be root or it will make another request
        String id = stem.isRootStem() ? "root" : stem.getUuid();

        DojoTreeItem dojoTreeItem = new DojoTreeItem(displayExtension, id, DojoTreeItemType.stem);

        DojoTreeItemChild[] childrenDojoTreeItems = new DojoTreeItemChild[GrouperUtil.length(childrenStems) + GrouperUtil.length(childrenGroups)
                  + GrouperUtil.length(childrenAttributeDefs) + GrouperUtil.length(childrenAttributeDefNames)];
        dojoTreeItem.setChildren(childrenDojoTreeItems);
        
        int index = 0;
        for (Stem childStem : childrenStems) {
          
          childrenDojoTreeItems[index++] = new DojoTreeItemChild(
              childStem.getDisplayExtension(), childStem.getUuid(), DojoTreeItemType.stem, true);
        }

        for (Group childGroup : childrenGroups) {
          
          childrenDojoTreeItems[index++] = new DojoTreeItemChild(
              childGroup.getDisplayExtension(), childGroup.getUuid(), DojoTreeItemType.group, null);
        }

        for (AttributeDef childAttributeDef : childrenAttributeDefs) {
          
          childrenDojoTreeItems[index++] = new DojoTreeItemChild(
              childAttributeDef.getExtension(), childAttributeDef.getUuid(), DojoTreeItemType.attributeDef, null);
        }

        for (AttributeDefName childAttributeDefName : childrenAttributeDefNames) {
          
          childrenDojoTreeItems[index++] = new DojoTreeItemChild(
              childAttributeDefName.getDisplayExtension(), childAttributeDefName.getUuid(), DojoTreeItemType.attributeDefName, null);
        }

        
        //childrenDojoTreeItems[index++] = new DojoTreeItemChild(
        //    /* "<i class=\"icon-group\"></i>" + */ childStem.getDisplayExtension(), childStem.getUuid(),
        //    DojoTreeItemChildType.group,
        //    null);
        
        json = dojoTreeItem.toJson();
      } else {
        throw new RuntimeException("Why is stem null?????");
      }
      
      GrouperUiUtils.printToScreen(json, HttpContentType.APPLICATION_JSON, false, false);
  
    } catch (Exception se) {
      LOG.error("Error searching for folder: '" + folderQueryString + "', " + se.getMessage(), se);
      
      //dont rethrow or the control will get confused
      GrouperUiUtils.printToScreen("{\"name\": \"Error\", \"id\": \"error\"}", 
          HttpContentType.APPLICATION_JSON, false, false);

    } finally {
      GrouperSession.stopQuietly(grouperSession); 
    }
  
    //dont print the regular JSON
    throw new ControllerDone();

    
  }
  
  /**
   * change a column to my favorites
   * @param request
   * @param response
   */
  public void indexColMyFavorites(HttpServletRequest request, HttpServletResponse response) {

    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    //initialize the bean
    GrouperRequestContainer.retrieveFromRequestOrCreate();
    
    GrouperSession grouperSession = null;

    try {
      grouperSession = GrouperSession.start(loggedInSubject);

      int col = GrouperUtil.intValue(request.getParameter("col"));
      
      //GrouperUserDataApi.
      
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#indexCol" + col, 
          "/WEB-INF/grouperUi2/index/indexMyFavorites.jsp"));
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }

    
  }
  
  /**
   * change a column to my services
   * @param request
   * @param response
   */
  public void indexColMyServices(HttpServletRequest request, HttpServletResponse response) {

    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    //initialize the bean
    GrouperRequestContainer.retrieveFromRequestOrCreate();
    
    GrouperSession grouperSession = null;

    try {
      grouperSession = GrouperSession.start(loggedInSubject);

      int col = GrouperUtil.intValue(request.getParameter("col"));
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#indexCol" + col, 
          "/WEB-INF/grouperUi2/index/indexMyServices.jsp"));
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }

  
  /**
   * change a column to groups I manage
   * @param request
   * @param response
   */
  public void indexColGroupsImanage(HttpServletRequest request, HttpServletResponse response) {
    

    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    //initialize the bean
    GrouperRequestContainer.retrieveFromRequestOrCreate();
    
    GrouperSession grouperSession = null;

    try {
      grouperSession = GrouperSession.start(loggedInSubject);

      int col = GrouperUtil.intValue(request.getParameter("col"));
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#indexCol" + col, 
          "/WEB-INF/grouperUi2/index/indexGroupsImanage.jsp"));
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
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    //initialize the bean
    GrouperRequestContainer.retrieveFromRequestOrCreate();
    
    GrouperSession grouperSession = null;

    try {
      grouperSession = GrouperSession.start(loggedInSubject);

      //just show a jsp
      showJsp("/WEB-INF/grouperUi2/index/index.jsp");
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    throw new ControllerDone();
  }

  /**
   * index page of application
   * @param request
   * @param response
   */
  public void indexMain(HttpServletRequest request, HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    //initialize the bean
    GrouperRequestContainer.retrieveFromRequestOrCreate();
    
    GrouperSession grouperSession = null;

    try {
      grouperSession = GrouperSession.start(loggedInSubject);

      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/index/indexMain.jsp"));

    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }


  /**
   * change a column to groups I manage
   * @param request
   * @param response
   */
  public void indexColStemsImanage(HttpServletRequest request, HttpServletResponse response) {
    
  
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    //initialize the bean
    GrouperRequestContainer.retrieveFromRequestOrCreate();
    
    GrouperSession grouperSession = null;
  
    try {
      grouperSession = GrouperSession.start(loggedInSubject);
  
      int col = GrouperUtil.intValue(request.getParameter("col"));
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#indexCol" + col, 
          "/WEB-INF/grouperUi2/index/indexStemsImanage.jsp"));
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }

  /**
   * change a column to my memberships
   * @param request
   * @param response
   */
  public void indexColMyMemberships(HttpServletRequest request, HttpServletResponse response) {
  
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    //initialize the bean
    GrouperRequestContainer.retrieveFromRequestOrCreate();
    
    GrouperSession grouperSession = null;
  
    try {
      grouperSession = GrouperSession.start(loggedInSubject);
  
      int col = GrouperUtil.intValue(request.getParameter("col"));
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#indexCol" + col, 
          "/WEB-INF/grouperUi2/index/indexMyMemberships.jsp"));
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  
    
  }

  /**
   * change a column to recently used
   * @param request
   * @param response
   */
  public void indexColRecentlyUsed(HttpServletRequest request, HttpServletResponse response) {
  
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    //initialize the bean
    GrouperRequestContainer.retrieveFromRequestOrCreate();
    
    GrouperSession grouperSession = null;
  
    try {
      grouperSession = GrouperSession.start(loggedInSubject);
  
      int col = GrouperUtil.intValue(request.getParameter("col"));
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#indexCol" + col, 
          "/WEB-INF/grouperUi2/index/indexRecentlyUsed.jsp"));
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  
    
  }


  /**
   * search submit from upper right
   * @param request
   * @param response
   */
  public void searchFormSubmit(HttpServletRequest request, HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      searchHelper(request, response);
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * my folders
   * @param request
   * @param response
   */
  public void myStems(HttpServletRequest request, HttpServletResponse response) {
  
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
  
    GrouperSession grouperSession = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/index/myStems.jsp"));
  
      
      myStemsHelper(request, response);
  
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
     * the filter button was pressed on the my folders page, or paging or sorting, or something
     * @param request
     * @param response
     */
    private void myStemsHelper(HttpServletRequest request, HttpServletResponse response) {
  
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
      String myStemsFilter = StringUtils.trimToEmpty(request.getParameter("myStemsFilter"));
      
      IndexContainer indexContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getIndexContainer();

      //too short of a query
      if (myStemsFilter.length() == 1) {
    
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, "#myStemsFilterId",
            TextContainer.retrieveFromRequest().getText().get("myStemsErrorNotEnoughChars")));
        
        //clear out the results
        guiResponseJs.addAction(GuiScreenAction.newInnerHtml("#myStemsResultsId", ""));
    
        return;
      }
      
      String pageSizeString = request.getParameter("pagingTagPageSize");
      int pageSize = -1;
      if (!StringUtils.isBlank(pageSizeString)) {
        pageSize = GrouperUtil.intValue(pageSizeString);
      } else {
        pageSize = GrouperUiConfig.retrieveConfig().propertyValueInt("pager.pagesize.default", 50);
      }
      indexContainer.getMyStemsGuiPaging().setPageSize(pageSize);
      
      //1 indexed
      String pageNumberString = request.getParameter("pagingTagPageNumber");
      
      int pageNumber = 1;
      if (!StringUtils.isBlank(pageNumberString)) {
        pageNumber = GrouperUtil.intValue(pageNumberString);
      }
  
      indexContainer.getMyStemsGuiPaging().setPageNumber(pageNumber);
  
      QueryOptions queryOptions = QueryOptions.create("displayName", true, pageNumber, pageSize);
      queryOptions.getQueryPaging().setDoTotalCount(true);

      StemFinder stemFinder = new StemFinder()
        .assignSubject(GrouperSession.staticGrouperSession().getSubject())
        .assignQueryOptions(queryOptions);
  
      if (!StringUtils.isBlank(myStemsFilter)) {
        stemFinder.assignSplitScope(true);
        stemFinder.assignScope(myStemsFilter);
      }
  
      String stemFilterType = request.getParameter("stemFilterType");
      
      if (StringUtils.equals("createGroups", stemFilterType)) {
        stemFinder.assignPrivileges(NamingPrivilege.CREATE_PRIVILEGES);
        
      } else if (StringUtils.equals("createStems", stemFilterType)) {
        
        stemFinder.addPrivilege(NamingPrivilege.STEM);
      } else if (StringUtils.equals("attributeRead", stemFilterType)) {
        
        stemFinder.assignPrivileges(NamingPrivilege.ATTRIBUTE_READ_PRIVILEGES);
      } else if (StringUtils.equals("attributeUpdate", stemFilterType)) {
        
        stemFinder.assignPrivileges(NamingPrivilege.ATTRIBUTE_UPDATE_PRIVILEGES);
      } else if (StringUtils.equals("all", stemFilterType)) {
        stemFinder.assignPrivileges(NamingPrivilege.ALL_ADMIN_PRIVILEGES);
      } else if (!StringUtils.isBlank(stemFilterType)) {
        throw new RuntimeException("Invalid value for stemFilterType: '" + stemFilterType + "'" );
      }
      
      
      Set<Stem> results = stemFinder.findStems();
      
      //this shouldnt be null, but make sure
      if (results == null) {
        results = new HashSet<Stem>();
      }
      
      if (GrouperUtil.length(results) == 0) {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("myStemsNoResultsFound")));
      }
      
      indexContainer.setGuiStemsUserManagesAbbreviated(GuiStem.convertFromStems(results));
      
      indexContainer.getMyStemsGuiPaging().setTotalRecordCount(queryOptions.getQueryPaging().getTotalRecordCount());
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#myStemsResultsId", 
          "/WEB-INF/grouperUi2/index/myStemsContents.jsp"));
  }

  /**
   * my folders reset button
   * @param request
   * @param response
   */
  public void myStemsReset(HttpServletRequest request, HttpServletResponse response) {
  
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
  
    GrouperSession grouperSession = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
      //clear out form
      guiResponseJs.addAction(GuiScreenAction.newFormFieldValue("myStemsFilter", ""));
      guiResponseJs.addAction(GuiScreenAction.newFormFieldValue("stemFilterType", "createGroups"));
      
      //get the unfiltered stems
      myStemsHelper(request, response);
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * my folders
   * @param request
   * @param response
   */
  public void myStemsSubmit(HttpServletRequest request, HttpServletResponse response) {
  
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
  
    GrouperSession grouperSession = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      myStemsHelper(request, response);
  
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

}
