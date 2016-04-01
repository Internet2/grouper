/**
 * 
 */
package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.audit.AuditEntry;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiAttributeDef;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiAttributeDefName;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiGroup;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiMember;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiObjectBase;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiService;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiStem;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiPaging;
import edu.internet2.middleware.grouper.grouperUi.beans.preferences.UiV2Preference;
import edu.internet2.middleware.grouper.grouperUi.serviceLogic.UiV2Main;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.ui.util.GrouperUiConfig;
import edu.internet2.middleware.grouper.ui.util.GrouperUiUserData;
import edu.internet2.middleware.grouper.userData.GrouperUserDataApi;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;


/**
 * index page and common functions beans
 * @author mchyzer
 *
 */
public class IndexContainer {

  /**
   * show miscellaneous list
   * @return true if show
   */
  public boolean isShowMiscellaneousLink() {
    return GrouperUiConfig.retrieveConfig().propertyValueBoolean("uiV2.showMiscellaneousLink", true);
  }

  /**
   * 
   * @return if show global inherited privileges link
   */
  public boolean isShowGlobalInheritedPrivilegesLink() {
    return GrouperUiConfig.retrieveConfig().propertyValueBoolean("uiV2.showGlobalInheritedPrivilegesLink", true);
  }
  
  /**
   * if we should run widgets in threads (main page performance)
   */
  private boolean runWidgetsInThreads = false;
  
  /**
   * if we should run widgets in threads (main page performance)
   * @return the runWidgetsInThreads
   */
  public boolean isRunWidgetsInThreads() {
    return this.runWidgetsInThreads;
  }

  
  /**
   * if we should run widgets in threads (main page performance)
   * @param runWidgetsInThreads1 the runWidgetsInThreads to set
   */
  public void setRunWidgetsInThreads(boolean runWidgetsInThreads1) {
    this.runWidgetsInThreads = runWidgetsInThreads1;
  }

  /**
   * link to support docs
   * @return link to support docs
   */
  public String getSupportDocsLink() {
    return GrouperUiConfig.retrieveConfig().propertyValueString("uiV2.supportDocLink", "https://spaces.internet2.edu/display/Grouper/Grouper+Wiki+Home");
  }
  
  /**
   * search results
   */
  private Set<GuiObjectBase> searchGuiObjectsResults = null;
  
  /**
   * search results
   * @return the search results
   */
  public Set<GuiObjectBase> getSearchGuiObjectsResults() {
    return this.searchGuiObjectsResults;
  }
  
  /**
   * search results
   * @param searchGuiObjectsResults1
   */
  public void setSearchGuiObjectsResults(Set<GuiObjectBase> searchGuiObjectsResults1) {
    this.searchGuiObjectsResults = searchGuiObjectsResults1;
  }



  /**
   * search query
   */
  private String searchQuery;
  
  /**
   * search query
   * @return search query
   */
  public String getSearchQuery() {
    return this.searchQuery;
  }

  /**
   * search query
   * @param searchQuery1
   */
  public void setSearchQuery(String searchQuery1) {
    this.searchQuery = searchQuery1;
  }

  /**
   * various options for the panels on the main index screen
   * note, the name here must match exactly the substring of the name of the JSP
   * e.g. grouperUi2/index/indexGroupsImanage.jsp
   */
  public static enum IndexPanel {
    
    /** groups I manage panel */
    GroupsImanage {

      @Override
      public void initData() {
        UiV2Main.initGroupsImanage();
      }
    }, 
    
    /** my favorites panel */
    MyFavorites {

      @Override
      public void initData() {
        UiV2Main.initMyFavorites();
      }
    }, 
    
    /** my memberships panel */
    MyMemberships {

      @Override
      public void initData() {
        UiV2Main.initMyMemberships();
      }
    }, 
    
    /** my service panel */
    MyServices {

      @Override
      public void initData() {
        UiV2Main.initMyServices();
      }
    }, 
    
    /** recently used panel */
    RecentlyUsed {

      @Override
      public void initData() {
        UiV2Main.initRecentlyUsed();
      }
    }, 

    /** stems I manage panel */
    StemsImanage {

      @Override
      public void initData() {
        UiV2Main.initStemsImanage();
      }
    };
    
    /**
     * init data in this index panel
     */
    public abstract void initData();
    
    /**
     * convert a string to enum
     * @param indexPanelString
     * @param exceptionOnNotFound
     * @param exceptionIfInvalid
     * @return enum or exception
     */
    public static IndexPanel valueOfIgnoreCase(String indexPanelString, boolean exceptionOnNotFound, boolean exceptionIfInvalid) {
      
      return GrouperUtil.enumValueOfIgnoreCase(IndexPanel.class, indexPanelString, exceptionOnNotFound, exceptionIfInvalid);
      
    }
    
  }
  
  /** cache panelCol0 */
  private String panelCol0;
  
  /**
   * panel (IndexPanel enum) for col 0 on main index page
   * @return the panel
   */
  public String getPanelCol0() {
    
    if (this.panelCol0 == null) {
      
      IndexPanel defaultIndexPanel = IndexPanel.MyFavorites;
      
      IndexPanel indexPanel = panelColPersonalPreference(0);
      
      if (indexPanel == null) {
        String panel0string = GrouperUiConfig.retrieveConfig().propertyValueString("uiV2.widget0", defaultIndexPanel.name());
    
        indexPanel = IndexPanel.valueOfIgnoreCase(panel0string, true, false);
      }
      
      this.panelCol0 = GrouperUtil.defaultIfBlank(indexPanel == null ? null : indexPanel.name(), defaultIndexPanel.name());
    }
    return this.panelCol0;
  }

  /**
   * find the index panel for the column as a user preference
   * @param colIndex
   * @return the enum or null
   */
  public static IndexPanel panelColPersonalPreference(int colIndex) {
    
    //initialize the bean
    GrouperRequestContainer.retrieveFromRequestOrCreate();

    GrouperSession grouperSession = GrouperSession.staticGrouperSession(false);
    boolean startedSession = grouperSession == null;
    Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();

    if (startedSession) {
      grouperSession = GrouperSession.start(loggedInSubject);
    }
    try {
      //get the panel string
      UiV2Preference uiV2Preference = GrouperUserDataApi.preferences(GrouperUiUserData.grouperUiGroupNameForUserData(), loggedInSubject, UiV2Preference.class);
      
      if (uiV2Preference != null) {
        
        String indexPanelString = null;
        
        switch(colIndex) {
          case 0:
            indexPanelString = uiV2Preference.getIndexCol0();
            break;
          case 1:
            indexPanelString = uiV2Preference.getIndexCol1();
            break;
            
          case 2:
            indexPanelString = uiV2Preference.getIndexCol2();
            break;
          default: 
            throw new RuntimeException("Not expecting column index: " + colIndex);
        }

        if (!StringUtils.isBlank(indexPanelString)) {
          IndexPanel indexPanel = IndexPanel.valueOfIgnoreCase(indexPanelString, false, false);
          return indexPanel;
        }
        
      }
      
    } finally {
      if (startedSession) {
        GrouperSession.stopQuietly(grouperSession);
      }
    }


    
    return null;

  }
  
  /**
   * cache panel col 1
   */
  private String panelCol1;
  
  /**
   * panel (IndexPanel enum) for col 1 on main index page
   * @return col1
   */
  public String getPanelCol1() {
    if (this.panelCol1 == null) {
      IndexPanel defaultIndexPanel = IndexPanel.GroupsImanage;
      
      IndexPanel indexPanel = panelColPersonalPreference(1);
      
      if (indexPanel == null) {
        String panel0string = GrouperUiConfig.retrieveConfig().propertyValueString(
            "uiV2.widget1", defaultIndexPanel.name());
    
        indexPanel = IndexPanel.valueOfIgnoreCase(panel0string, true, false);
      }
      
      this.panelCol1 = GrouperUtil.defaultIfBlank(indexPanel == null ? null : indexPanel.name(), 
          defaultIndexPanel.name());
    }
    return this.panelCol1;
  }

  /**
   * cache panel col2
   */
  private String panelCol2;
  
  /**
   * panel (IndexPanel enum) for col 2 on main index page
   * @return col2
   */
  public String getPanelCol2() {

    if (this.panelCol2 == null) {
      IndexPanel defaultIndexPanel = IndexPanel.MyServices;

      IndexPanel indexPanel = panelColPersonalPreference(2);

      if (indexPanel == null) {
        String panel0string = GrouperUiConfig.retrieveConfig().propertyValueString("uiV2.widget2", defaultIndexPanel.name());

        indexPanel = IndexPanel.valueOfIgnoreCase(panel0string, true, false);
      }

      this.panelCol2 = GrouperUtil.defaultIfBlank(indexPanel == null ? null : indexPanel.name(), defaultIndexPanel.name());
    }
    return this.panelCol2;
  }

  /**
   * recent activity
   */
  private Set<GuiAuditEntry> guiAuditEntriesRecentActivity;
  
  /**
   * recent activity
   * @return audits
   */
  public Set<GuiAuditEntry> getGuiAuditEntriesRecentActivity() {
    
    GrouperSession grouperSession = GrouperSession.staticGrouperSession(false);
    boolean startedSession = grouperSession == null;

    if (startedSession) {
      grouperSession = GrouperSession.start(GrouperUiFilter.retrieveSubjectLoggedIn());
    }
    try {
      Map<String, Object> debugLog = LOG.isDebugEnabled() ? new LinkedHashMap<String, Object>() : null;
      try {
        if (this.guiAuditEntriesRecentActivity == null) {
          if (LOG.isDebugEnabled()) {
            debugLog.put("inittingRecentActivity", true);
          }
          
          Subject subject = grouperSession.getSubject();

          if (LOG.isDebugEnabled()) {
            debugLog.put("userName", subject == null ? null : subject.getId() + " - " + subject.getName());
          }
          
          Member member = MemberFinder.findBySubject(grouperSession, subject, true);

          QueryOptions queryOptions = new QueryOptions();
          queryOptions.paging(6, 1, false);

          Set<AuditEntry> auditEntries = GrouperDAOFactory.getFactory().getAuditEntry().findByActingUser(member.getUuid(), queryOptions);
    
          if (LOG.isDebugEnabled()) {
            debugLog.put("resultsFromDb", GrouperUtil.length(auditEntries));
          }
          
          this.guiAuditEntriesRecentActivity = GuiAuditEntry.convertFromAuditEntries(auditEntries);
    
        } else {
          if (LOG.isDebugEnabled()) {
            debugLog.put("inittingRecentActivity", false);
          }
        }

        if (LOG.isDebugEnabled()) {
          debugLog.put("recentActivitySize", GrouperUtil.length(this.guiAuditEntriesRecentActivity));
        }
        
        return this.guiAuditEntriesRecentActivity;
      } finally {
        if (LOG.isDebugEnabled()) {
          LOG.debug(GrouperUtil.mapToString(debugLog));
        }
      }
      
    } finally {
      if (startedSession) {
        GrouperSession.stopQuietly(grouperSession);
      }
    }
  }

  /**
   * for index page, this is a short list of groups the user manages
   */
  private Set<GuiGroup> guiGroupsUserManagesAbbreviated;

  /**
   * for index page if my services retrieved
   */
  private boolean myServicesRetrieved;
  
  /**
   * for index page if my services retrieved
   * @return the myServicesRetrieved
   */
  public boolean isMyServicesRetrieved() {
    return this.myServicesRetrieved;
  }
  
  /**
   * for index page if my services retrieved
   * @param myServicesRetrieved1 the myServicesRetrieved to set
   */
  public void setMyServicesRetrieved(boolean myServicesRetrieved1) {
    this.myServicesRetrieved = myServicesRetrieved1;
  }

  /**
   * for index page if recently used retrieved
   */
  private boolean recentlyUsedRetrieved;
  
  
  /**
   * for index page if recently used retrieved
   * @return the recentlyUsedRetrieved
   */
  public boolean isRecentlyUsedRetrieved() {
    return this.recentlyUsedRetrieved;
  }
  
  /**
   * for index page if recently used retrieved
   * @param recentlyUsedRetrieved1 the recentlyUsedRetrieved to set
   */
  public void setRecentlyUsedRetrieved(boolean recentlyUsedRetrieved1) {
    this.recentlyUsedRetrieved = recentlyUsedRetrieved1;
  }

  /**
   * for index page if stems I manage are retrieved
   */
  private boolean stemsImanageRetrieved;

  /**
   * for index page if stems I manage are retrieved
   * @return the stemsImanageRetrieved
   */
  public boolean isStemsImanageRetrieved() {
    return this.stemsImanageRetrieved;
  }
  
  /**
   * for index page if stems I manage are retrieved
   * @param stemsImanageRetrieved1 the stemsImanageRetrieved to set
   */
  public void setStemsImanageRetrieved(boolean stemsImanageRetrieved1) {
    this.stemsImanageRetrieved = stemsImanageRetrieved1;
  }

  /**
   * for index page, this is if my memberships retrieved
   */
  private boolean myMembershipsRetrieved;
  
  /**
   * for index page, this is if my memberships retrieved
   * @return the myMembershipsRetrieved
   */
  public boolean isMyMembershipsRetrieved() {
    return this.myMembershipsRetrieved;
  }
  
  /**
   * for index page, this is if my memberships retrieved
   * @param myMembershipsRetrieved1 the myMembershipsRetrieved to set
   */
  public void setMyMembershipsRetrieved(boolean myMembershipsRetrieved1) {
    this.myMembershipsRetrieved = myMembershipsRetrieved1;
  }

  /**
   * for index page, this is if retrieved favorites
   */
  private boolean myFavoritesRetrieved;
  
  /**
   * for index page, this is if retrieved favorites
   * @return the favoritesRetrieved
   */
  public boolean isMyFavoritesRetrieved() {
    return this.myFavoritesRetrieved;
  }
  
  /**
   * @param favoritesRetrieved1 the favoritesRetrieved to set
   */
  public void setMyFavoritesRetrieved(boolean favoritesRetrieved1) {
    this.myFavoritesRetrieved = favoritesRetrieved1;
  }

  /**
   * for index page, this is if retrieved a short list of groups the user manages
   */
  private boolean groupsImanageRetrieved;
  
  /**
   * for index page, this is if retrieved a short list of groups the user manages
   * @return the guiGroupsUserManagesAbbreviatedRetrieved
   */
  public boolean isGroupsImanageRetrieved() {
    return this.groupsImanageRetrieved;
  }

  /**
   * for the index page, this is a short list of stems the user manages
   */
  private Set<GuiStem> guiStemsUserManagesAbbreviated;
  
  /**
   * for the index page, this is a short list of stems the user manages
   * @param guiStemsUserManagesAbbreviated1
   */
  public void setGuiStemsUserManagesAbbreviated(Set<GuiStem> guiStemsUserManagesAbbreviated1) {
    this.guiStemsUserManagesAbbreviated = guiStemsUserManagesAbbreviated1;
  }

  /**
   * get the stems the user manages, size 10 for front screen
   * @return the stems
   */
  public Set<GuiStem> getGuiStemsUserManagesAbbreviated() {

        
    return this.guiStemsUserManagesAbbreviated;
      
  }

  /**
   * for index page, this is a short list of attributeDefs favorites
   */
  private Set<GuiAttributeDef> guiAttributeDefsMyFavoritesAbbreviated;

  /**
   * for index page, this is a short list of attributeDefNames favorites
   */
  private Set<GuiAttributeDefName> guiAttributeDefNamesMyFavoritesAbbreviated;

  /**
   * for index page, this is a short list of groups the user manages, lazy load if null
   * @return the list of groups
   */
  public Set<GuiGroup> getGuiGroupsUserManagesAbbreviated() {
      return this.guiGroupsUserManagesAbbreviated;
  }

  
  /**
   * if the groups were retrieved (e.g. if they had enough time)
   * @param guiGroupsUserManagesAbbreviatedRetrieved1 the guiGroupsUserManagesAbbreviatedRetrieved to set
   */
  public void setGroupsImanageRetrieved(
      boolean guiGroupsUserManagesAbbreviatedRetrieved1) {
    this.groupsImanageRetrieved = guiGroupsUserManagesAbbreviatedRetrieved1;
  }


  /**
   * for index page, this is a short list of groups the user manages, lazy load if null
   * @param guiGroupsUserManagesAbbreviated1
   */
  public void setGuiGroupsUserManagesAbbreviated(
      Set<GuiGroup> guiGroupsUserManagesAbbreviated1) {
    this.guiGroupsUserManagesAbbreviated = guiGroupsUserManagesAbbreviated1;
  }

  /**
   * for index page, this is a short list of groups the user is a member of, lazy load if null
   * @return the list of groups
   */
  public Set<GuiGroup> getGuiGroupsMyMembershipsAbbreviated() {
    
    return this.guiGroupsMyMembershipsAbbreviated;
      
  }

  /**
   * for index page, this is a short list of groups the user has favorited
   */
  private Set<GuiGroup> guiGroupsMyFavoritesAbbreviated;

  /**
   * for index page, this is a short list of groups the user is a member of
   */
  private Set<GuiGroup> guiGroupsMyMembershipsAbbreviated;

  /**
   * for index page, this is a short list of subjects the user has favorited
   */
  private Set<GuiMember> guiMembersMyFavoritesAbbreviated;

  /**
   * for index page, this is a short list of groups the user has favorited, lazy load if null
   * @return the list of groups
   */
  public Set<GuiGroup> getGuiGroupsMyFavoritesAbbreviated() {
    
    return this.guiGroupsMyFavoritesAbbreviated;
      
  }

  /**
   * services for the user
   */
  private Set<GuiService> guiMyServices;
  
  /**
   * for index page, this is a short list of stems the user has favorited
   */
  private Set<GuiStem> guiStemsMyFavoritesAbbreviated;

  /**
   * for index page, this is a short list of attributeDefNames RecentlyUsed
   */
  private Set<GuiAttributeDefName> guiAttributeDefNamesRecentlyUsedAbbreviated;

  /**
   * for index page, this is a short list of attributeDefs RecentlyUsed
   */
  private Set<GuiAttributeDef> guiAttributeDefsRecentlyUsedAbbreviated;

  /**
   * for index page, this is a short list of groups the user has RecentlyUsed
   */
  private Set<GuiGroup> guiGroupsRecentlyUsedAbbreviated;

  /**
   * for index page, this is a short list of subjects the user has RecentlyUsed
   */
  private Set<GuiMember> guiMembersRecentlyUsedAbbreviated;

  /**
   * for index page, this is a short list of stems the user has RecentlyUsed
   */
  private Set<GuiStem> guiStemsRecentlyUsedAbbreviated;

  /**
   * keep track of the paging on the search screen
   */
  private GuiPaging searchGuiPaging = null;

  /**
   * paging for my services
   */
  private GuiPaging myServicesGuiPaging = null;
  
  /**
   * paging for my favorites
   */
  private GuiPaging myFavoritesGuiPaging = null;
  
  /**
   * paging for my activity
   */
  private GuiPaging myActivityGuiPaging = null;
  
  /**
   * gui object favorite results from my favorites
   */
  private Set<GuiObjectBase> guiObjectFavorites = null;
  
  /**
   * gui object favorite results from my favorites
   * @return favorites
   */
  public Set<GuiObjectBase> getGuiObjectFavorites() {
    return this.guiObjectFavorites;
  }

  /**
   * gui object favorite results from my favorites
   * @param guiObjectFavorites1
   */
  public void setGuiObjectFavorites(Set<GuiObjectBase> guiObjectFavorites1) {
    this.guiObjectFavorites = guiObjectFavorites1;
  }

  /**
   * paging for my favorites
   * @return favorites paging
   */
  public GuiPaging getMyFavoritesGuiPaging() {
    if (this.myFavoritesGuiPaging == null) {
      this.myFavoritesGuiPaging = new GuiPaging();
    }
    return this.myFavoritesGuiPaging;
  }

  /**
   * paging for my favorites
   * @param myFavoritesGuiPaging1
   */
  public void setMyFavoritesGuiPaging(GuiPaging myFavoritesGuiPaging1) {
    this.myFavoritesGuiPaging = myFavoritesGuiPaging1;
  }
  
  /**
   * paging for my activity
   * @return activity paging
   */
  public GuiPaging getMyActivityGuiPaging() {
    if (this.myActivityGuiPaging == null) {
      this.myActivityGuiPaging = new GuiPaging();
    }
    return this.myActivityGuiPaging;
  }

  /**
   * paging for my activity
   * @param myActivityGuiPaging1
   */
  public void setMyActivityGuiPaging(GuiPaging myActivityGuiPaging1) {
    this.myActivityGuiPaging = myActivityGuiPaging1;
  }
  
  /**
   * gui audit entries
   */
  private Set<GuiAuditEntry> guiAuditEntries = null;
  
  /**
   * gui audit entries from my activity
   * @return favorites
   */
  public Set<GuiAuditEntry> getGuiAuditEntries() {
    return this.guiAuditEntries;
  }

  /**
   * gui audit entries from my acitvity
   * @param guiAuditEntries
   */
  public void setGuiAuditEntries(Set<GuiAuditEntry> guiAuditEntries) {
    this.guiAuditEntries = guiAuditEntries;
  }

  /**
   * keep track of the paging on the search screen
   * @return the paging object
   */
  public GuiPaging getSearchGuiPaging() {
    if (this.searchGuiPaging == null) {
      this.searchGuiPaging = new GuiPaging();
    }
    return this.searchGuiPaging;
  }

  /**
   * keep track of the paging on the search screen
   * @param searchGuiPaging1
   */
  public void setSearchGuiPaging(GuiPaging searchGuiPaging1) {
    this.searchGuiPaging = searchGuiPaging1;
  }



  /** logger */
  protected static final Log LOG = LogFactory.getLog(IndexContainer.class);

  /**
   * for index page, this is a short list of user's services, lazy load if null
   * @return the list of services
   */
  public Set<GuiService> getGuiMyServices() {
            
    return this.guiMyServices;
      
  }

  /**
   * gui attribute def names my services
   * @param guiMyServices1
   */
  public void setGuiMyServices(
      Set<GuiService> guiMyServices1) {
    this.guiMyServices = guiMyServices1;
  }

  /**
   * for index page, this is a short list of stems the user has favorited, lazy load if null
   * @return the list of stems
   */
  public Set<GuiStem> getGuiStemsMyFavoritesAbbreviated() {
    
    return this.guiStemsMyFavoritesAbbreviated;
  }

  /**
   * for index page, this is a short list of members the user has favorited, lazy load if null
   * @return the list of members
   */
  public Set<GuiMember> getGuiMembersMyFavoritesAbbreviated() {
    
    return this.guiMembersMyFavoritesAbbreviated;
  }

  /**
   * for index page, this is a short list of attributeDefNames the user has favorited, lazy load if null
   * @return the list of attributeDefNames
   */
  public Set<GuiAttributeDefName> getGuiAttributeDefNamesMyFavoritesAbbreviated() {
    
    return this.guiAttributeDefNamesMyFavoritesAbbreviated;
  }

  /**
   * for index page, this is a short list of attributeDefs the user has favorited, lazy load if null
   * @return the list of attributeDefs
   */
  public Set<GuiAttributeDef> getGuiAttributeDefsMyFavoritesAbbreviated() {
    
    return this.guiAttributeDefsMyFavoritesAbbreviated;
  }

  /**
   * for index page, this is a short list of attributeDefNames the user has RecentlyUsed, lazy load if null
   * @return the list of attributeDefNames
   */
  public Set<GuiAttributeDefName> getGuiAttributeDefNamesRecentlyUsedAbbreviated() {
    return this.guiAttributeDefNamesRecentlyUsedAbbreviated;
  }

  
  /**
   * @param guiAuditEntriesRecentActivity1 the guiAuditEntriesRecentActivity to set
   */
  public void setGuiAuditEntriesRecentActivity(Set<GuiAuditEntry> guiAuditEntriesRecentActivity1) {
    this.guiAuditEntriesRecentActivity = guiAuditEntriesRecentActivity1;
  }


  
  /**
   * @param guiAttributeDefsMyFavoritesAbbreviated1 the guiAttributeDefsMyFavoritesAbbreviated to set
   */
  public void setGuiAttributeDefsMyFavoritesAbbreviated(
      Set<GuiAttributeDef> guiAttributeDefsMyFavoritesAbbreviated1) {
    this.guiAttributeDefsMyFavoritesAbbreviated = guiAttributeDefsMyFavoritesAbbreviated1;
  }


  
  /**
   * @param guiAttributeDefNamesMyFavoritesAbbreviated1 the guiAttributeDefNamesMyFavoritesAbbreviated to set
   */
  public void setGuiAttributeDefNamesMyFavoritesAbbreviated(
      Set<GuiAttributeDefName> guiAttributeDefNamesMyFavoritesAbbreviated1) {
    this.guiAttributeDefNamesMyFavoritesAbbreviated = guiAttributeDefNamesMyFavoritesAbbreviated1;
  }


  
  /**
   * @param guiGroupsMyFavoritesAbbreviated1 the guiGroupsMyFavoritesAbbreviated to set
   */
  public void setGuiGroupsMyFavoritesAbbreviated(Set<GuiGroup> guiGroupsMyFavoritesAbbreviated1) {
    this.guiGroupsMyFavoritesAbbreviated = guiGroupsMyFavoritesAbbreviated1;
  }


  
  /**
   * @param guiGroupsMyMembershipsAbbreviated1 the guiGroupsMyMembershipsAbbreviated to set
   */
  public void setGuiGroupsMyMembershipsAbbreviated(Set<GuiGroup> guiGroupsMyMembershipsAbbreviated1) {
    this.guiGroupsMyMembershipsAbbreviated = guiGroupsMyMembershipsAbbreviated1;
  }


  
  /**
   * @param guiMembersMyFavoritesAbbreviated1 the guiMembersMyFavoritesAbbreviated to set
   */
  public void setGuiMembersMyFavoritesAbbreviated(Set<GuiMember> guiMembersMyFavoritesAbbreviated1) {
    this.guiMembersMyFavoritesAbbreviated = guiMembersMyFavoritesAbbreviated1;
  }


  
  /**
   * @param guiStemsMyFavoritesAbbreviated1 the guiStemsMyFavoritesAbbreviated to set
   */
  public void setGuiStemsMyFavoritesAbbreviated(Set<GuiStem> guiStemsMyFavoritesAbbreviated1) {
    this.guiStemsMyFavoritesAbbreviated = guiStemsMyFavoritesAbbreviated1;
  }


  
  /**
   * @param guiAttributeDefNamesRecentlyUsedAbbreviated1 the guiAttributeDefNamesRecentlyUsedAbbreviated to set
   */
  public void setGuiAttributeDefNamesRecentlyUsedAbbreviated(
      Set<GuiAttributeDefName> guiAttributeDefNamesRecentlyUsedAbbreviated1) {
    this.guiAttributeDefNamesRecentlyUsedAbbreviated = guiAttributeDefNamesRecentlyUsedAbbreviated1;
  }


  
  /**
   * @param guiAttributeDefsRecentlyUsedAbbreviated1 the guiAttributeDefsRecentlyUsedAbbreviated to set
   */
  public void setGuiAttributeDefsRecentlyUsedAbbreviated(
      Set<GuiAttributeDef> guiAttributeDefsRecentlyUsedAbbreviated1) {
    this.guiAttributeDefsRecentlyUsedAbbreviated = guiAttributeDefsRecentlyUsedAbbreviated1;
  }


  
  /**
   * @param guiGroupsRecentlyUsedAbbreviated1 the guiGroupsRecentlyUsedAbbreviated to set
   */
  public void setGuiGroupsRecentlyUsedAbbreviated(Set<GuiGroup> guiGroupsRecentlyUsedAbbreviated1) {
    this.guiGroupsRecentlyUsedAbbreviated = guiGroupsRecentlyUsedAbbreviated1;
  }


  
  /**
   * @param guiMembersRecentlyUsedAbbreviated1 the guiMembersRecentlyUsedAbbreviated to set
   */
  public void setGuiMembersRecentlyUsedAbbreviated(Set<GuiMember> guiMembersRecentlyUsedAbbreviated1) {
    this.guiMembersRecentlyUsedAbbreviated = guiMembersRecentlyUsedAbbreviated1;
  }


  
  /**
   * @param guiStemsRecentlyUsedAbbreviated1 the guiStemsRecentlyUsedAbbreviated to set
   */
  public void setGuiStemsRecentlyUsedAbbreviated(Set<GuiStem> guiStemsRecentlyUsedAbbreviated1) {
    this.guiStemsRecentlyUsedAbbreviated = guiStemsRecentlyUsedAbbreviated1;
  }


  /**
   * for index page, this is a short list of attributeDefs the user has RecentlyUsed, lazy load if null
   * @return the list of attributeDefs
   */
  public Set<GuiAttributeDef> getGuiAttributeDefsRecentlyUsedAbbreviated() {
    
    return this.guiAttributeDefsRecentlyUsedAbbreviated;
      
  }

  /**
   * for index page, this is a short list of groups the user has RecentlyUsed, lazy load if null
   * @return the list of groups
   */
  public Set<GuiGroup> getGuiGroupsRecentlyUsedAbbreviated() {
    
    return this.guiGroupsRecentlyUsedAbbreviated;
  }

  /**
   * for index page, this is a short list of members the user has RecentlyUsed, lazy load if null
   * @return the list of members
   */
  public Set<GuiMember> getGuiMembersRecentlyUsedAbbreviated() {
    
    return this.guiMembersRecentlyUsedAbbreviated;
  }

  /**
   * for index page, this is a short list of stems the user has RecentlyUsed, lazy load if null
   * @return the list of stems
   */
  public Set<GuiStem> getGuiStemsRecentlyUsedAbbreviated() {
    
    return this.guiStemsRecentlyUsedAbbreviated;
  }

  /**
   * paging for my services
   * @return paging
   */
  public GuiPaging getMyServicesGuiPaging() {
    if (this.myServicesGuiPaging == null) {
      this.myServicesGuiPaging = new GuiPaging();
    }
    return this.myServicesGuiPaging;
  }

  /**
   * paging for my services
   * @param myServicesGuiPaging1
   */
  public void setMyServicesGuiPaging(GuiPaging myServicesGuiPaging1) {
    this.myServicesGuiPaging = myServicesGuiPaging1;
  }

  /**
   * Decide if the browser folder/group pane should auto-select
   * the currently selected object in the view.
   */
  public boolean isMenuRefreshOnView() {
    return GrouperUiConfig.retrieveConfig().propertyValueBoolean("uiV2.refresh.menu.on.view", true);
  }

  /**
   * Should the admin ui link the displayed under quick links?
   */
  public boolean isAdminUIQuickLinkDisplayed() {
    return GrouperUiConfig.retrieveConfig().propertyValueBoolean("uiV2.quicklink.menu.adminui", true);
  }

  /**
   * Should the lite ui link the displayed under quick links?
   */
  public boolean isLiteUIQuickLinkDisplayed() {
    return GrouperUiConfig.retrieveConfig().propertyValueBoolean("uiV2.quicklink.menu.liteui", true);
  }

}