
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.Stem.Scope;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefFinder;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.audit.AuditEntry;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiAttributeDef;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiAttributeDefName;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiGroup;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiMember;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiObjectBase;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiRuleDefinition;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiService;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiStem;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiPaging;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiResponseJs;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction.GuiMessageType;
import edu.internet2.middleware.grouper.grouperUi.beans.preferences.UiV2Preference;
import edu.internet2.middleware.grouper.grouperUi.beans.tree.DojoTreeItem;
import edu.internet2.middleware.grouper.grouperUi.beans.tree.DojoTreeItemChild;
import edu.internet2.middleware.grouper.grouperUi.beans.tree.DojoTreeItemChild.DojoTreeItemType;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GrouperRequestContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GuiAuditEntry;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.IndexContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.IndexContainer.IndexPanel;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.RulesContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.TextContainer;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.GrouperObject;
import edu.internet2.middleware.grouper.misc.GrouperObjectFinder;
import edu.internet2.middleware.grouper.misc.GrouperObjectFinder.GrouperObjectFinderType;
import edu.internet2.middleware.grouper.misc.GrouperObjectFinder.ObjectPrivilege;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.AttributeDefPrivilege;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
import edu.internet2.middleware.grouper.rules.RuleDefinition;
import edu.internet2.middleware.grouper.rules.RuleFinder;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.ui.exceptions.ControllerDone;
import edu.internet2.middleware.grouper.ui.tags.GrouperPagingTag2;
import edu.internet2.middleware.grouper.ui.util.GrouperUiConfig;
import edu.internet2.middleware.grouper.ui.util.GrouperUiUserData;
import edu.internet2.middleware.grouper.ui.util.GrouperUiUtils;
import edu.internet2.middleware.grouper.ui.util.HttpContentType;
import edu.internet2.middleware.grouper.userData.GrouperFavoriteFinder;
import edu.internet2.middleware.grouper.userData.GrouperUserDataApi;
import edu.internet2.middleware.grouper.util.GrouperCallable;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectTooManyResults;

/**
 * main logic for ui
 */
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

    GuiPaging guiPaging = indexContainer.getSearchGuiPaging();
    QueryOptions queryOptions = QueryOptions.create("displayName", true, null, null);

    GrouperPagingTag2.processRequest(request, guiPaging, queryOptions); 
    
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

    try {
      Set<GrouperObject> results = grouperObjectFinder.findGrouperObjects();

      indexContainer.setSearchGuiObjectsResults(GuiObjectBase.convertFromGrouperObjects(results));

      guiPaging.setTotalRecordCount(queryOptions.getQueryPaging().getTotalRecordCount());

      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#searchResultsId",
              "/WEB-INF/grouperUi2/index/searchContents.jsp"));
    } catch (SubjectTooManyResults e) {
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error,
              TextContainer.retrieveFromRequest().getText().get("searchTooManyResults")));
    }

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
   * @param httpServletRequest
   * @param response
   */
  public void folderMenu(HttpServletRequest httpServletRequest, HttpServletResponse response) {
    //the query string has the folder to print out.  starting with root.  undefined means there is a problem
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    String folderQueryString = httpServletRequest.getQueryString();

    try {
      grouperSession = GrouperSession.start(loggedInSubject);
      
      
      String json = null;
      
      Stem stem = null;
      
      //un-url-encrypt
      if (StringUtils.equals("root", folderQueryString)) {
        
        // GRP-1107 browse starting from stem configured by property: 
        // default.browse.stem, otherwise from root.
        boolean startTreeAtDefaultStem = GrouperUiConfig.retrieveConfig().propertyValueBoolean("default.browse.stem.uiv2.menu", true);
        String defaultBrowseStem = GrouperUiConfig.retrieveConfig().propertyValueString("default.browse.stem");
        if (startTreeAtDefaultStem && !StringUtils.isBlank(defaultBrowseStem)) {
          stem = StemFinder.findByName(grouperSession, defaultBrowseStem, false);
        } else {
          stem = StemFinder.findRootStem(grouperSession);
        }
      } else {
        int lastSlash = folderQueryString.lastIndexOf('/');
        String stemId = null;
        if (lastSlash == -1) {
          stemId = folderQueryString;
        } else {
          stemId = folderQueryString.substring(lastSlash+1, folderQueryString.length());
        }
        stem = StemFinder.findByUuid(grouperSession, stemId, false);
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
          .assignPrivileges(AttributeDefPrivilege.ATTR_VIEW_PRIVILEGES)
          .assignSubject(GrouperSession.staticGrouperSession().getSubject())
          .assignParentStemId(stem.getId()).assignStemScope(Scope.ONE).findAttributes();
  
        Set<AttributeDefName> childrenAttributeDefNames = new AttributeDefNameFinder()
          .assignQueryOptions(QueryOptions.create("displayExtension", true, 1, 10))
          .assignPrivileges(AttributeDefPrivilege.ATTR_VIEW_PRIVILEGES)
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
        GrouperUiUtils.printToScreen(json, HttpContentType.APPLICATION_JSON, false, false);
      }
      
  
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

      //init the data
      initMyFavorites();

      int col = GrouperUtil.intValue(request.getParameter("col"));
      
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#indexCol" + col, 
          "/WEB-INF/grouperUi2/index/indexMyFavorites.jsp"));
      
      if (GrouperUtil.booleanValue(request.getParameter("storePref"), true)) {
        panelColPersonalPreferenceStore(col, IndexPanel.MyFavorites);
      }
      
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
      
      //init the data
      initMyServices();

      int col = GrouperUtil.intValue(request.getParameter("col"));
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#indexCol" + col, 
          "/WEB-INF/grouperUi2/index/indexMyServices.jsp"));

      if (GrouperUtil.booleanValue(request.getParameter("storePref"), true)) {
        panelColPersonalPreferenceStore(col, IndexPanel.MyServices);
      }
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
      
      //init the data
      initGroupsImanage();
      
      int col = GrouperUtil.intValue(request.getParameter("col"));
      
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#indexCol" + col, 
          "/WEB-INF/grouperUi2/index/indexGroupsImanage.jsp"));
      
      if (GrouperUtil.booleanValue(request.getParameter("storePref"), true)) {
        panelColPersonalPreferenceStore(col, IndexPanel.GroupsImanage);
      }
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }

    
  }

  /**
   * 
   */
  public static void initRecentlyUsed() {
        
    final IndexContainer indexContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getIndexContainer();
    
    GrouperCallable<Boolean> callable = new GrouperCallable<Boolean>(
        "GrouperUi.UiV2Main.initRecentlyUsed()") {
      
      @Override
      public Boolean callLogic() {
        
        {
          int millisToSleepForTest = GrouperUiConfig.retrieveConfig().propertyValueInt("uiV2.index.test.sleepIn.recentlyUsed.widgetMillis", -1);
          if (millisToSleepForTest > 0) {
            GrouperUtil.sleep(millisToSleepForTest);
          }
        }
        
        GrouperSession grouperSession = GrouperSession.staticGrouperSession();
        Set<AttributeDefName> attributeDefNames = GrouperUserDataApi.recentlyUsedAttributeDefNames(
            GrouperUiUserData.grouperUiGroupNameForUserData(), grouperSession.getSubject());
        
        indexContainer.setGuiAttributeDefNamesRecentlyUsedAbbreviated(GuiAttributeDefName.convertFromAttributeDefNames(
            attributeDefNames, "uiV2.index.maxRecentlyUsedEachType", 5));

        Set<AttributeDef> attributeDefs = GrouperUserDataApi.recentlyUsedAttributeDefs(
            GrouperUiUserData.grouperUiGroupNameForUserData(), grouperSession.getSubject());
        
        indexContainer.setGuiAttributeDefsRecentlyUsedAbbreviated(GuiAttributeDef.convertFromAttributeDefs(
            attributeDefs, "uiV2.index.maxRecentlyUsedEachType", 5));

        Set<Group> groups = GrouperUserDataApi.recentlyUsedGroups(GrouperUiUserData.grouperUiGroupNameForUserData(), grouperSession.getSubject());
        
        indexContainer.setGuiGroupsRecentlyUsedAbbreviated(
            GuiGroup.convertFromGroups(groups, "uiV2.index.maxRecentlyUsedEachType", 5));

        Set<Member> members = GrouperUserDataApi.recentlyUsedMembers(GrouperUiUserData.grouperUiGroupNameForUserData(), grouperSession.getSubject());
        
        indexContainer.setGuiMembersRecentlyUsedAbbreviated(GuiMember.convertFromMembers(members, "uiV2.index.maxRecentlyUsedEachType", 5));

        Set<Stem> stems = GrouperUserDataApi.recentlyUsedStems(GrouperUiUserData.grouperUiGroupNameForUserData(), 
            grouperSession.getSubject());
        
        indexContainer.setGuiStemsRecentlyUsedAbbreviated(GuiStem.convertFromStems(stems, "uiV2.index.maxRecentlyUsedEachType", 5));
        
        indexContainer.setRecentlyUsedRetrieved(true);
        return true;
      }
    };
    
    int maxMillis = GrouperUiConfig.retrieveConfig().propertyValueInt("uiV2.index.widgetMaxQueryMillis", 5000);

    indexContainer.setRecentlyUsedRetrieved(false);
    
    if (indexContainer.isRunWidgetsInThreads() && maxMillis > 0) {
      
      Future<Boolean> future = GrouperUtil.retrieveExecutorService().submit(callable);
      try {
        future.get(maxMillis, TimeUnit.MILLISECONDS);
      } catch (TimeoutException te) {
        //this is ok, didnt return groups, thats fine
      } catch (Throwable throwable) {
        GrouperCallable.throwRuntimeException(throwable);
      }
    } else if (!indexContainer.isRunWidgetsInThreads() || maxMillis < 0) {
      //just run it
      callable.callLogicWithSessionIfExists();
    }
    //if 0 dont run it...
    
  }
  
  /**
   * init groups user manages
   */
  public static void initGroupsImanage() {
    
    final IndexContainer indexContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getIndexContainer();
    
    GrouperCallable<Void> callable = new GrouperCallable<Void>(
        "GrouperUi.UiV2Main.initGroupsImanage()") {
      
      @Override
      public Void callLogic() {

        {
          int millisToSleepForTest = GrouperUiConfig.retrieveConfig().propertyValueInt("uiV2.index.test.sleepIn.groupsImanage.widgetMillis", -1);
          if (millisToSleepForTest > 0) {
            GrouperUtil.sleep(millisToSleepForTest);
          }
        }

        Set<Group> theGroups = new GroupFinder()
          .assignPrivileges(AccessPrivilege.MANAGE_PRIVILEGES)
          .assignQueryOptions(new QueryOptions().paging(GrouperUiConfig.retrieveConfig().propertyValueInt(
            "uiV2.index.numberOfObjectsInSectionDefault", 10), 1, false)).findGroups();

        indexContainer.setGuiGroupsUserManagesAbbreviated(GuiGroup.convertFromGroups(theGroups));
        indexContainer.setGroupsImanageRetrieved(true);
        return null;
      }
    };
    
    int maxMillis = GrouperUiConfig.retrieveConfig().propertyValueInt("uiV2.index.widgetMaxQueryMillis", 5000);

    indexContainer.setGroupsImanageRetrieved(false);
    
    if (indexContainer.isRunWidgetsInThreads() && maxMillis > 0) {
      
      Future<Void> future = GrouperUtil.retrieveExecutorService().submit(callable);
      try {
        future.get(maxMillis, TimeUnit.MILLISECONDS);
      } catch (TimeoutException te) {
        //this is ok, didnt return groups, thats fine
      } catch (Throwable throwable) {
        GrouperCallable.throwRuntimeException(throwable);
      }
    } else if (!indexContainer.isRunWidgetsInThreads() || maxMillis < 0) {
      //just run it
      callable.callLogicWithSessionIfExists();
    }
    //if 0 dont run it...
    
  }
  
  /**
   * find the index panel for the column as a user preference
   * @param colIndex
   * @param indexPanel
   */
  public static void panelColPersonalPreferenceStore(int colIndex, IndexPanel indexPanel) {
    
    GrouperSession grouperSession = GrouperSession.staticGrouperSession(true);

    //get the panel string
    UiV2Preference uiV2Preference = GrouperUserDataApi.preferences(GrouperUiUserData.grouperUiGroupNameForUserData(), 
        grouperSession.getSubject(), UiV2Preference.class);
    
    if (uiV2Preference == null) {
      uiV2Preference = new UiV2Preference();
    }
    
    String indexPanelString = indexPanel == null ? null : indexPanel.name();
    
    switch(colIndex) {
      case 0:
        uiV2Preference.setIndexCol0(indexPanelString);
        break;
      case 1:
        uiV2Preference.setIndexCol1(indexPanelString);
        break;
      case 2:
        uiV2Preference.setIndexCol2(indexPanelString);
        break;
      default: 
        throw new RuntimeException("Not expecting column index: " + colIndex);
    }

    GrouperUserDataApi.preferencesAssign(GrouperUiUserData.grouperUiGroupNameForUserData(), grouperSession.getSubject(), uiV2Preference);
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
    GrouperRequestContainer grouperRequestContainer = GrouperRequestContainer.retrieveFromRequestOrCreate();
    
    GrouperSession grouperSession = null;

    try {
      
      //see the error page for ajax errors
      if (GrouperUtil.booleanValue(request.getParameter("throwErrorForTesting"), false)) {
        throw new RuntimeException("Testing ajax exceptions...");
      }
      
      grouperSession = GrouperSession.start(loggedInSubject);

      IndexContainer indexContainer = grouperRequestContainer.getIndexContainer();

      //run things in threads (if configured to)
      indexContainer.setRunWidgetsInThreads(true);
      
      for (String indexPanelString : new String[]{ indexContainer.getPanelCol0(), 
          indexContainer.getPanelCol1(), indexContainer.getPanelCol2() }) {
        
        IndexPanel indexPanel = IndexPanel.valueOfIgnoreCase(indexPanelString, false, true);
        
        indexPanel.initData();
        
      }
      
      
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
      
      //init the data
      initStemsImanage();

      int col = GrouperUtil.intValue(request.getParameter("col"));
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#indexCol" + col, 
          "/WEB-INF/grouperUi2/index/indexStemsImanage.jsp"));

      if (GrouperUtil.booleanValue(request.getParameter("storePref"), true)) {
        panelColPersonalPreferenceStore(col, IndexPanel.StemsImanage);
      }
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
      
      //init the data
      initMyMemberships();
  
      int col = GrouperUtil.intValue(request.getParameter("col"));
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#indexCol" + col, 
          "/WEB-INF/grouperUi2/index/indexMyMemberships.jsp"));

      if (GrouperUtil.booleanValue(request.getParameter("storePref"), true)) {
        panelColPersonalPreferenceStore(col, IndexPanel.MyMemberships);
      }
      
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
  
      //init the data
      initRecentlyUsed();
      
      int col = GrouperUtil.intValue(request.getParameter("col"));
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#indexCol" + col, 
          "/WEB-INF/grouperUi2/index/indexRecentlyUsed.jsp"));

      if (GrouperUtil.booleanValue(request.getParameter("storePref"), true)) {
        panelColPersonalPreferenceStore(col, IndexPanel.RecentlyUsed);
      }

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
   * my favorites
   * @param request
   * @param response
   */
  public void myFavorites(HttpServletRequest request, HttpServletResponse response) {
  
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
  
    GrouperSession grouperSession = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/index/myFavorites.jsp"));
  
      
      myFavoritesHelper(request, response);
  
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
     * the filter button was pressed on the my favorites page, or paging or sorting, or something
     * @param request
     * @param response
     */
    private void myFavoritesHelper(HttpServletRequest request, HttpServletResponse response) {
  
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
      String myFavoritesFilter = StringUtils.trimToEmpty(request.getParameter("myFavoritesFilter"));
      
      IndexContainer indexContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getIndexContainer();
  
      //too short of a query
      if (myFavoritesFilter.length() == 1) {
    
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, "#myFavoritesFilterId",
            TextContainer.retrieveFromRequest().getText().get("myFavoritesErrorNotEnoughChars")));
        
        //clear out the results
        guiResponseJs.addAction(GuiScreenAction.newInnerHtml("#myFavoritesResultsId", ""));
    
        return;
      }
      
      GuiPaging guiPaging = indexContainer.getMyFavoritesGuiPaging();
      QueryOptions queryOptions = QueryOptions.create("displayName", true, null, null);

      GrouperPagingTag2.processRequest(request, guiPaging, queryOptions); 
      
      GrouperFavoriteFinder grouperFavoriteFinder = new GrouperFavoriteFinder()
        .assignSubject(GrouperSession.staticGrouperSession().getSubject())
        .assignUserDataGroupName(GrouperUiUserData.grouperUiGroupNameForUserData())
        .assignQueryOptions(queryOptions);

      if (!StringUtils.isBlank(myFavoritesFilter)) {
        grouperFavoriteFinder.assignFilterText(myFavoritesFilter);
        grouperFavoriteFinder.assignSplitScope(true);
      }
      
      Set<GrouperObject> results = grouperFavoriteFinder.findFavorites();
      
      //this shouldnt be null, but make sure
      if (results == null) {
        results = new HashSet<GrouperObject>();
      }
      
      if (GrouperUtil.length(results) == 0) {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("myFavoritesNoResultsFound")));
      }
      
      indexContainer.setGuiObjectFavorites(GuiObjectBase.convertFromGrouperObjects(results));
      
      guiPaging.setTotalRecordCount(queryOptions.getQueryPaging().getTotalRecordCount());
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#myFavoritesResultsId", 
          "/WEB-INF/grouperUi2/index/myFavoritesContents.jsp"));
  }

  /**
   * my favorites reset button
   * @param request
   * @param response
   */
  public void myFavoritesReset(HttpServletRequest request, HttpServletResponse response) {
  
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
  
    GrouperSession grouperSession = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
      //clear out form
      guiResponseJs.addAction(GuiScreenAction.newFormFieldValue("myFavoritesFilter", ""));
      
      //get the unfiltered stems
      myFavoritesHelper(request, response);
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * my favorites
   * @param request
   * @param response
   */
  public void myFavoritesSubmit(HttpServletRequest request, HttpServletResponse response) {
  
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
  
    GrouperSession grouperSession = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      myFavoritesHelper(request, response);
  
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
  /**
   * my activity
   * @param request
   * @param response
   */
  public void myActivity(HttpServletRequest request, HttpServletResponse response) {
  
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
  
    GrouperSession grouperSession = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/index/myActivity.jsp"));
  
      
      myActivityHelper(request, response);
  
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
  /**
   * the filter button was pressed on the my activity page, or paging or sorting, or something
   * @param request
   * @param response
   */
  private void myActivityHelper(HttpServletRequest request, HttpServletResponse response) {

    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();

    String myActivityStartDate = StringUtils.trimToEmpty(request.getParameter("startDate"));
    String myActivityEndDate = StringUtils.trimToEmpty(request.getParameter("endDate"));
    
    IndexContainer indexContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getIndexContainer();
    
    String dateFormat = GrouperUiFilter.retrieveSessionMediaResourceBundle().getString("audit.query.date-format");
    SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
    
    Date startDate = null;
    Date endDate = null;
    Long startTime = 0L;
    Long endTime = 0L;
    if (!StringUtils.isBlank(myActivityStartDate)) {
        try {
        	startDate = sdf.parse(myActivityStartDate);
        	startTime = startDate.getTime();
        } catch (ParseException e) {
            guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, "#myActivityStartDate",
            TextContainer.retrieveFromRequest().getText().get("myActivityIncorrectDateFormat")));
            //clear out the results
            guiResponseJs.addAction(GuiScreenAction.newInnerHtml("#myActivityResultsId", ""));
            return;
        }
    }
    
    if (!StringUtils.isBlank(myActivityEndDate)) {
        try {
        	endDate = sdf.parse(myActivityEndDate);
        	endTime = endDate.getTime();
        } catch (ParseException e) {
            guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, "#myActivityEndDate",
            TextContainer.retrieveFromRequest().getText().get("myActivityIncorrectDateFormat")));
            //clear out the results
            guiResponseJs.addAction(GuiScreenAction.newInnerHtml("#myActivityResultsId", ""));
            return;
        }
    }
   
    if (endDate != null && startDate != null && endDate.before(startDate)) {
    	guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, "#myActivityStartDate",
        TextContainer.retrieveFromRequest().getText().get("myActivityStartDateAfterEndDate")));
        //clear out the results
        guiResponseJs.addAction(GuiScreenAction.newInnerHtml("#myActivityResultsId", ""));
        return;
    }
    
    GuiPaging guiPaging = indexContainer.getMyActivityGuiPaging();
    QueryOptions queryOptions = QueryOptions.create("createdOnDb", false, null, null);

    GrouperPagingTag2.processRequest(request, guiPaging, queryOptions); 
    
    Member member = MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), GrouperSession.staticGrouperSession().getSubject(), true);
    Set<AuditEntry> auditEntries = GrouperDAOFactory.getFactory().getAuditEntry().findByActingUser(member.getUuid(), 
    								queryOptions, startTime, endTime);
    
    //this shouldnt be null, but make sure
    if (auditEntries == null) {
    	auditEntries = new HashSet<AuditEntry>();
    }
    
    if (GrouperUtil.length(auditEntries) == 0) {
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
          TextContainer.retrieveFromRequest().getText().get("myActivityNoResultsFound")));
    }
    
    indexContainer.setGuiAuditEntries(GuiAuditEntry.convertFromAuditEntries(auditEntries));
    
    guiPaging.setTotalRecordCount(queryOptions.getQueryPaging().getTotalRecordCount());
    
    guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#myActivityResultsId", 
        "/WEB-INF/grouperUi2/index/myActivityContents.jsp"));
}
  
  /**
   * my activity reset button
   * @param request
   * @param response
   */
  public void myActivityReset(HttpServletRequest request, HttpServletResponse response) {
  
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
  
    GrouperSession grouperSession = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
      //clear out form
      guiResponseJs.addAction(GuiScreenAction.newFormFieldValue("startDate", ""));
      guiResponseJs.addAction(GuiScreenAction.newFormFieldValue("endDate", ""));
      
      //get the unfiltered stems
      myActivityHelper(request, response);
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
  /**
   * my activity
   * @param request
   * @param response
   */
  public void myActivitySubmit(HttpServletRequest request, HttpServletResponse response) {
  
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
  
    GrouperSession grouperSession = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      myActivityHelper(request, response);
  
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * miscellaneous
   * @param request
   * @param response
   */
  public void miscellaneous(HttpServletRequest request, HttpServletResponse response) {
  
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
  
    GrouperSession grouperSession = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
      if (!GrouperRequestContainer.retrieveFromRequestOrCreate().getRulesContainer().isCanReadPrivilegeInheritance()) {
        throw new RuntimeException("Not allowed to read privilege inheritance! " + GrouperUtil.subjectToString(loggedInSubject));
      }
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/index/miscellaneous.jsp"));
    
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * my services
   * @param request
   * @param response
   */
  public void myServices(HttpServletRequest request, HttpServletResponse response) {
  
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
  
    GrouperSession grouperSession = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/index/myServices.jsp"));
  
      
      myServicesHelper(request, response);
  
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
     * the filter button was pressed on the my services page, or paging or sorting, or something
     * @param request
     * @param response
     */
    private void myServicesHelper(HttpServletRequest request, HttpServletResponse response) {
  
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
      final String myServicesFilter = StringUtils.trimToEmpty(request.getParameter("myServicesFilter"));
      
      IndexContainer indexContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getIndexContainer();
  
      //too short of a query
      if (myServicesFilter.length() == 1) {
    
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, "#myServicesFilterId",
            TextContainer.retrieveFromRequest().getText().get("myServicesErrorNotEnoughChars")));
        
        //clear out the results
        guiResponseJs.addAction(GuiScreenAction.newInnerHtml("#myServicesResultsId", ""));
    
        return;
      }
      
      GuiPaging guiPaging = indexContainer.getMyServicesGuiPaging();
      final QueryOptions queryOptions = QueryOptions.create("displayName", true, null, null);

      GrouperPagingTag2.processRequest(request, guiPaging, queryOptions); 

      @SuppressWarnings("unchecked")
      Set<AttributeDefName> results = (Set<AttributeDefName>)GrouperSession.callbackGrouperSession(GrouperSession.staticGrouperSession().internal_getRootSession(), 
          new GrouperSessionHandler() {
        
        @Override
        public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
          
          AttributeDefNameFinder attributeDefNameFinder = new AttributeDefNameFinder().assignAnyRole(true)
              .assignSubject(GrouperSession.staticGrouperSession().getSubject())
              .assignQueryOptions(queryOptions);

          if (!StringUtils.isBlank(myServicesFilter)) {
            attributeDefNameFinder.assignSplitScope(true);
            attributeDefNameFinder.assignScope(myServicesFilter);
          }
          
          return attributeDefNameFinder.findAttributeNames();
          
        }
      });
      
      //this shouldnt be null, but make sure
      if (results == null) {
        results = new HashSet<AttributeDefName>();
      }
      
      if (GrouperUtil.length(results) == 0) {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("myServicesNoResultsFound")));
      }
      
      indexContainer.setGuiMyServices(GuiService.convertFromAttributeDefNames(results));
      
      guiPaging.setTotalRecordCount(queryOptions.getQueryPaging().getTotalRecordCount());

      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#myServicesResultsId", 
          "/WEB-INF/grouperUi2/index/myServicesContents.jsp"));
  }

  /**
   * my services reset button
   * @param request
   * @param response
   */
  public void myServicesReset(HttpServletRequest request, HttpServletResponse response) {
  
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
  
    GrouperSession grouperSession = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
      //clear out form
      guiResponseJs.addAction(GuiScreenAction.newFormFieldValue("myServicesFilter", ""));
      
      //get the unfiltered stems
      myServicesHelper(request, response);
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * my services
   * @param request
   * @param response
   */
  public void myServicesSubmit(HttpServletRequest request, HttpServletResponse response) {
  
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
  
    GrouperSession grouperSession = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      myServicesHelper(request, response);
  
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  
  
  /**
   * global inherited privileges
   * @param request
   * @param response
   */
  public void globalInheritedPrivileges(HttpServletRequest request, HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      if (!GrouperRequestContainer.retrieveFromRequestOrCreate().getRulesContainer().isCanReadPrivilegeInheritance()) {
        throw new RuntimeException("Not allowed to read privilege inheritance! " + GrouperUtil.subjectToString(loggedInSubject));
      }
  
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
      RulesContainer rulesContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getRulesContainer();
      
      Set<GuiRuleDefinition> guiRuleDefinitions = new TreeSet<GuiRuleDefinition>();
      {
        Set<RuleDefinition> groupRuleDefinitions  = RuleFinder.findPrivilegeInheritRules(true);
        for (RuleDefinition ruleDefinition : GrouperUtil.nonNull(groupRuleDefinitions)) {
          GuiRuleDefinition guiRuleDefinition = new GuiRuleDefinition(ruleDefinition);
          if (guiRuleDefinition.getOwnerGuiStem() != null) {
            guiRuleDefinitions.add(guiRuleDefinition);
          }
        }
      }
      
      rulesContainer.setGuiRuleDefinitions(guiRuleDefinitions);
  
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/index/globalInheritedPrivileges.jsp"));
  
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * init stems user manages
   */
  public static void initStemsImanage() {
    
    final IndexContainer indexContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getIndexContainer();
    
    GrouperCallable<Void> callable = new GrouperCallable<Void>(
        "GrouperUi.UiV2Main.initStemsImanage()") {
      
      @Override
      public Void callLogic() {
        
        {
          int millisToSleepForTest = GrouperUiConfig.retrieveConfig().propertyValueInt("uiV2.index.test.sleepIn.stemsImanage.widgetMillis", -1);
          if (millisToSleepForTest > 0) {
            GrouperUtil.sleep(millisToSleepForTest);
          }
        }

        Set<Stem> stems = new StemFinder().assignSubject(GrouperSession.staticGrouperSession().getSubject())
            .assignPrivileges(NamingPrivilege.CREATE_PRIVILEGES)
            .assignQueryOptions(new QueryOptions().paging(
                GrouperUiConfig.retrieveConfig().propertyValueInt("uiV2.index.numberOfObjectsInSectionDefault", 10),
                1, false)).findStems();
  
        indexContainer.setGuiStemsUserManagesAbbreviated(GuiStem.convertFromStems(stems));
  
        indexContainer.setStemsImanageRetrieved(true);
        return null;
      }
    };
    
    int maxMillis = GrouperUiConfig.retrieveConfig().propertyValueInt("uiV2.index.widgetMaxQueryMillis", 5000);
  
    indexContainer.setStemsImanageRetrieved(false);
    
    if (indexContainer.isRunWidgetsInThreads() && maxMillis > 0) {
      
      Future<Void> future = GrouperUtil.retrieveExecutorService().submit(callable);
      try {
        future.get(maxMillis, TimeUnit.MILLISECONDS);
      } catch (TimeoutException te) {
        //this is ok, didnt return groups, thats fine
      } catch (Throwable throwable) {
        GrouperCallable.throwRuntimeException(throwable);
      }
    } else if (!indexContainer.isRunWidgetsInThreads() || maxMillis < 0) {
      //just run it
      callable.callLogicWithSessionIfExists();
    }
    //if 0 dont run it...
    
  }

  /**
   * init my favorites
   */
  public static void initMyFavorites() {
    
    final IndexContainer indexContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getIndexContainer();
    
    GrouperCallable<Void> callable = new GrouperCallable<Void>(
        "GrouperUi.UiV2Main.initMyFavorites()") {
      
      @Override
      public Void callLogic() {
        
        {
          int millisToSleepForTest = GrouperUiConfig.retrieveConfig().propertyValueInt("uiV2.index.test.sleepIn.myFavorites.widgetMillis", -1);
          if (millisToSleepForTest > 0) {
            GrouperUtil.sleep(millisToSleepForTest);
          }
        }

        GrouperSession grouperSession = GrouperSession.staticGrouperSession();
        
        Set<AttributeDefName> attributeDefNames = GrouperUserDataApi.favoriteAttributeDefNames(
            GrouperUiUserData.grouperUiGroupNameForUserData(), grouperSession.getSubject());
        
        indexContainer.setGuiAttributeDefNamesMyFavoritesAbbreviated(GuiAttributeDefName.convertFromAttributeDefNames(
            attributeDefNames, "uiV2.index.maxFavoritesEachType", 5));

        Set<AttributeDef> attributeDefs = GrouperUserDataApi.favoriteAttributeDefs(
            GrouperUiUserData.grouperUiGroupNameForUserData(), grouperSession.getSubject());
        
        indexContainer.setGuiAttributeDefsMyFavoritesAbbreviated(GuiAttributeDef.convertFromAttributeDefs(
            attributeDefs, "uiV2.index.maxFavoritesEachType", 5));

        Set<Group> groups = GrouperUserDataApi.favoriteGroups(GrouperUiUserData.grouperUiGroupNameForUserData(), grouperSession.getSubject());
        
        indexContainer.setGuiGroupsMyFavoritesAbbreviated(
            GuiGroup.convertFromGroups(groups, "uiV2.index.maxFavoritesEachType", 5));

        Set<Member> members = GrouperUserDataApi.favoriteMembers(GrouperUiUserData.grouperUiGroupNameForUserData(), grouperSession.getSubject());
        
        indexContainer.setGuiMembersMyFavoritesAbbreviated(
            GuiMember.convertFromMembers(members, "uiV2.index.maxFavoritesEachType", 5));

        Set<Stem> stems = GrouperUserDataApi.favoriteStems(GrouperUiUserData.grouperUiGroupNameForUserData(), 
            grouperSession.getSubject());
        
        indexContainer.setGuiStemsMyFavoritesAbbreviated(GuiStem.convertFromStems(stems, "uiV2.index.maxFavoritesEachType", 5));

        indexContainer.setMyFavoritesRetrieved(true);
        return null;
      }
    };
    
    int maxMillis = GrouperUiConfig.retrieveConfig().propertyValueInt("uiV2.index.widgetMaxQueryMillis", 5000);
  
    indexContainer.setMyFavoritesRetrieved(false);
    
    if (indexContainer.isRunWidgetsInThreads() && maxMillis > 0) {
      
      Future<Void> future = GrouperUtil.retrieveExecutorService().submit(callable);
      try {
        future.get(maxMillis, TimeUnit.MILLISECONDS);
      } catch (TimeoutException te) {
        //this is ok, didnt return groups, thats fine
      } catch (Throwable throwable) {
        GrouperCallable.throwRuntimeException(throwable);
      }
    } else if (!indexContainer.isRunWidgetsInThreads() || maxMillis < 0) {
      //just run it
      callable.callLogicWithSessionIfExists();
    }
    //if 0 dont run it...
    
  }
  
  /**
   * init stems user manages
   */
  public static void initMyMemberships() {
    
    final IndexContainer indexContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getIndexContainer();
    
    GrouperCallable<Void> callable = new GrouperCallable<Void>(
        "GrouperUi.UiV2Main.initMyMemberships()") {
      
      @Override
      public Void callLogic() {
        
        {
          int millisToSleepForTest = GrouperUiConfig.retrieveConfig().propertyValueInt("uiV2.index.test.sleepIn.myMemberships.widgetMillis", -1);
          if (millisToSleepForTest > 0) {
            GrouperUtil.sleep(millisToSleepForTest);
          }
        }

        Set<Group> groups = new GroupFinder()
            .assignSubject(GrouperSession.staticGrouperSession().getSubject())
            .assignField(Group.getDefaultList())
            .assignPrivileges(AccessPrivilege.OPT_OR_READ_PRIVILEGES)
            .assignQueryOptions(new QueryOptions().paging(
                GrouperUiConfig.retrieveConfig().propertyValueInt("uiV2.index.numberOfObjectsInSectionDefault", 10), 1, false)).findGroups();
    
        indexContainer.setGuiGroupsMyMembershipsAbbreviated(GuiGroup.convertFromGroups(groups));

        indexContainer.setMyMembershipsRetrieved(true);
        return null;
      }
    };
    
    int maxMillis = GrouperUiConfig.retrieveConfig().propertyValueInt("uiV2.index.widgetMaxQueryMillis", 5000);
  
    indexContainer.setMyMembershipsRetrieved(false);
    
    if (indexContainer.isRunWidgetsInThreads() && maxMillis > 0) {
      
      Future<Void> future = GrouperUtil.retrieveExecutorService().submit(callable);
      try {
        future.get(maxMillis, TimeUnit.MILLISECONDS);
      } catch (TimeoutException te) {
        //this is ok, didnt return groups, thats fine
      } catch (Throwable throwable) {
        GrouperCallable.throwRuntimeException(throwable);
      }
    } else if (!indexContainer.isRunWidgetsInThreads() || maxMillis < 0) {
      //just run it
      callable.callLogicWithSessionIfExists();
    }
    //if 0 dont run it...
    
  }

  /**
   * init my services
   */
  public static void initMyServices() {
    
    final IndexContainer indexContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getIndexContainer();
    
    GrouperCallable<Void> callable = new GrouperCallable<Void>(
        "GrouperUi.UiV2Main.initMyServices()") {
      
      @Override
      public Void callLogic() {
        
        {
          int millisToSleepForTest = GrouperUiConfig.retrieveConfig().propertyValueInt("uiV2.index.test.sleepIn.myServices.widgetMillis", -1);
          if (millisToSleepForTest > 0) {
            GrouperUtil.sleep(millisToSleepForTest);
          }
        }

        Set<AttributeDefName> attributeDefNames = new AttributeDefNameFinder().assignAnyRole(true)
            .assignSubject(GrouperSession.staticGrouperSession().getSubject())
            .assignQueryOptions(new QueryOptions().paging(GrouperUiConfig.retrieveConfig().propertyValueInt("uiV2.index.numberOfObjectsInSectionDefault", 10), 1, false))
            .findAttributeNames();
            
        indexContainer.setGuiMyServices(GuiService.convertFromAttributeDefNames(attributeDefNames));

        indexContainer.setMyServicesRetrieved(true);
        return null;
      }
    };
    
    int maxMillis = GrouperUiConfig.retrieveConfig().propertyValueInt("uiV2.index.widgetMaxQueryMillis", 5000);
  
    indexContainer.setStemsImanageRetrieved(false);
    
    if (indexContainer.isRunWidgetsInThreads() && maxMillis > 0) {
      
      Future<Void> future = GrouperUtil.retrieveExecutorService().submit(callable);
      try {
        future.get(maxMillis, TimeUnit.MILLISECONDS);
      } catch (TimeoutException te) {
        //this is ok, didnt return groups, thats fine
      } catch (Throwable throwable) {
        GrouperCallable.throwRuntimeException(throwable);
      }
    } else if (!indexContainer.isRunWidgetsInThreads() || maxMillis < 0) {
      //just run it
      callable.callLogicWithSessionIfExists();
    }
    //if 0 dont run it...
    
  }
}
