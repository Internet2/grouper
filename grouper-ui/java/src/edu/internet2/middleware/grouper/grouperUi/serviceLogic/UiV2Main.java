package edu.internet2.middleware.grouper.grouperUi.serviceLogic;

import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.Stem.Scope;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefFinder;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiResponseJs;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction;
import edu.internet2.middleware.grouper.grouperUi.beans.tree.DojoTreeItem;
import edu.internet2.middleware.grouper.grouperUi.beans.tree.DojoTreeItemChild;
import edu.internet2.middleware.grouper.grouperUi.beans.tree.DojoTreeItemChild.DojoTreeItemType;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GrouperRequestContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.TextContainer;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.AttributeDefPrivilege;
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

}
