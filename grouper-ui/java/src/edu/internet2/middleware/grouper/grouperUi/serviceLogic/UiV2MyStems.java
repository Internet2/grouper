package edu.internet2.middleware.grouper.grouperUi.serviceLogic;

import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiStem;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiPaging;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiResponseJs;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction.GuiMessageType;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GrouperRequestContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.MyStemsContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.TextContainer;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.AttributeDefPrivilege;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
import edu.internet2.middleware.grouper.privs.Privilege;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.ui.tags.GrouperPagingTag2;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * my stems logic
 * @author mchyzer
 *
 */
public class UiV2MyStems {

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
          "/WEB-INF/grouperUi2/myStems/myStems.jsp"));
  
      
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

    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();

    String myStemsFilter = StringUtils.trimToEmpty(request.getParameter("myStemsFilter"));
    
    MyStemsContainer myStemsContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getMyStemsContainer();

    //too short of a query
    if (myStemsFilter.length() == 1) {
  
      guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, "#myStemsFilterId",
          TextContainer.retrieveFromRequest().getText().get("myStemsErrorNotEnoughChars")));
      
      //clear out the results
      guiResponseJs.addAction(GuiScreenAction.newInnerHtml("#myStemsResultsId", ""));
  
      return;
    }
    
    GuiPaging guiPaging = myStemsContainer.getMyStemsGuiPaging();

    QueryOptions queryOptions = QueryOptions.create("displayName", true, null, null);
    
    GrouperPagingTag2.processRequest(request, guiPaging, queryOptions); 
    
    StemFinder stemFinder = new StemFinder()
      .assignPrivileges(NamingPrivilege.ALL_PRIVILEGES)
      .assignSubject(loggedInSubject)
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
    
    myStemsContainer.setGuiStemsUserManages(GuiStem.convertFromStems(results));
    
    guiPaging.setTotalRecordCount(queryOptions.getQueryPaging().getTotalRecordCount());
    
    guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#myStemsResultsId", 
        "/WEB-INF/grouperUi2/myStems/myStemsContents.jsp"));
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

  /**
   * folders with groups I manage
   * @param request
   * @param response
   */
  public void myStemsContainingGroupsImanage(HttpServletRequest request, HttpServletResponse response) {
  
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
  
    GrouperSession grouperSession = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/myStems/myStemsContainingGroupsImanage.jsp"));
  
      
      myStemsContainingGroupsImanageHelper(request, response);
  
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
     * the filter button was pressed on the my folders of groups i manage page, or paging or sorting, or something
     * @param request
     * @param response
     */
    private void myStemsContainingGroupsImanageHelper(HttpServletRequest request, HttpServletResponse response) {
  
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
      String myStemsFilter = StringUtils.trimToEmpty(request.getParameter("myStemsFilter"));
      
      MyStemsContainer myStemsContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getMyStemsContainer();
  
      //too short of a query
      if (myStemsFilter.length() == 1) {
    
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, "#myStemsFilterId",
            TextContainer.retrieveFromRequest().getText().get("myStemsErrorNotEnoughChars")));
        
        //clear out the results
        guiResponseJs.addAction(GuiScreenAction.newInnerHtml("#myStemsResultsId", ""));
    
        return;
      }
      
      GuiPaging guiPaging = myStemsContainer.getMyStemsGuiPaging();
  
      QueryOptions queryOptions = QueryOptions.create("displayName", true, null, null);
      
      GrouperPagingTag2.processRequest(request, guiPaging, queryOptions); 
      
      StemFinder stemFinder = new StemFinder()
        .assignSubject(GrouperSession.staticGrouperSession().getSubject())
        .assignUserHasInGroupField(Privilege.convertPrivilegesToFields(AccessPrivilege.MANAGE_PRIVILEGES))
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
      
        //if looking for all, then dont look for any privilege, just the folders with groups the user manages
      
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
      
      myStemsContainer.setGuiStemsUserManages(GuiStem.convertFromStems(results));
      
      guiPaging.setTotalRecordCount(queryOptions.getQueryPaging().getTotalRecordCount());
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#myStemsResultsId", 
          "/WEB-INF/grouperUi2/myStems/myStemsContainingGroupsImanageContents.jsp"));
  }

  /**
   * folders containing groups reset button
   * @param request
   * @param response
   */
  public void myStemsContainingGroupsImanageReset(HttpServletRequest request, HttpServletResponse response) {
  
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
  
    GrouperSession grouperSession = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
      //clear out form
      guiResponseJs.addAction(GuiScreenAction.newFormFieldValue("myStemsFilter", ""));
      guiResponseJs.addAction(GuiScreenAction.newFormFieldValue("stemFilterType", "createGroups"));
      
      //get the unfiltered stems
      myStemsContainingGroupsImanageHelper(request, response);
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * folders with groups i manage
   * @param request
   * @param response
   */
  public void myStemsContainingGroupsImanageSubmit(HttpServletRequest request, HttpServletResponse response) {
  
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
  
    GrouperSession grouperSession = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      myStemsContainingGroupsImanageHelper(request, response);
  
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * folders with attributes I manage
   * @param request
   * @param response
   */
  public void myStemsContainingAttributesImanage(HttpServletRequest request, HttpServletResponse response) {
  
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
  
    GrouperSession grouperSession = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/myStems/myStemsContainingAttributesImanage.jsp"));
  
      
      myStemsContainingAttributesImanageHelper(request, response);
  
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
     * the filter button was pressed on the my folders with attributes i manage page, or paging or sorting, or something
     * @param request
     * @param response
     */
    private void myStemsContainingAttributesImanageHelper(HttpServletRequest request, HttpServletResponse response) {
  
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
      String myStemsFilter = StringUtils.trimToEmpty(request.getParameter("myStemsFilter"));
      
      MyStemsContainer myStemsContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getMyStemsContainer();
  
      //too short of a query
      if (myStemsFilter.length() == 1) {
    
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, "#myStemsFilterId",
            TextContainer.retrieveFromRequest().getText().get("myStemsErrorNotEnoughChars")));
        
        //clear out the results
        guiResponseJs.addAction(GuiScreenAction.newInnerHtml("#myStemsResultsId", ""));
    
        return;
      }
      
      GuiPaging guiPaging = myStemsContainer.getMyStemsGuiPaging();
  
      QueryOptions queryOptions = QueryOptions.create("displayName", true, null, null);
      
      GrouperPagingTag2.processRequest(request, guiPaging, queryOptions); 

      StemFinder stemFinder = new StemFinder()
        .assignSubject(GrouperSession.staticGrouperSession().getSubject())
        .assignUserHasInAttributeField(Privilege.convertPrivilegesToFields(AttributeDefPrivilege.MANAGE_PRIVILEGES))
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
      
        //if looking for all, then dont look for any privilege, just the folders with groups the user manages
      
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
      
      myStemsContainer.setGuiStemsUserManages(GuiStem.convertFromStems(results));
      
      guiPaging.setTotalRecordCount(queryOptions.getQueryPaging().getTotalRecordCount());
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#myStemsResultsId", 
          "/WEB-INF/grouperUi2/myStems/myStemsContainingAttributesImanageContents.jsp"));
  }

  /**
   * folders containing attributes reset button
   * @param request
   * @param response
   */
  public void myStemsContainingAttributesImanageReset(HttpServletRequest request, HttpServletResponse response) {
  
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
  
    GrouperSession grouperSession = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
      //clear out form
      guiResponseJs.addAction(GuiScreenAction.newFormFieldValue("myStemsFilter", ""));
      guiResponseJs.addAction(GuiScreenAction.newFormFieldValue("stemFilterType", "createGroups"));
      
      //get the unfiltered stems
      myStemsContainingAttributesImanageHelper(request, response);
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * folders with attributes i manage
   * @param request
   * @param response
   */
  public void myStemsContainingAttributesImanageSubmit(HttpServletRequest request, HttpServletResponse response) {
  
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
  
    GrouperSession grouperSession = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      myStemsContainingAttributesImanageHelper(request, response);
  
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }


}
