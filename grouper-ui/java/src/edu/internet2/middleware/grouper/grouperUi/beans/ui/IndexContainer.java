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
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiStem;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiPaging;
import edu.internet2.middleware.grouper.grouperUi.beans.preferences.UiV2Preference;
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
    GroupsImanage, 
    
    /** my favorites panel */
    MyFavorites, 
    
    /** my memberships panel */
    MyMemberships, 
    
    /** my service panel */
    MyServices, 
    
    /** recently used panel */
    RecentlyUsed, 

    /** stems I manage panel */
    StemsImanage;
    
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
  
  /**
   * panel (IndexPanel enum) for col 0 on main index page
   * @return the panel
   */
  public String getPanelCol0() {
    
    IndexPanel defaultIndexPanel = IndexPanel.MyFavorites;
    
    IndexPanel indexPanel = panelColPersonalPreference(0);
    
    if (indexPanel == null) {
      String panel0string = GrouperUiConfig.retrieveConfig().propertyValueString("uiV2.widget0", defaultIndexPanel.name());
  
      indexPanel = IndexPanel.valueOfIgnoreCase(panel0string, true, false);
    }
    
    return GrouperUtil.defaultIfBlank(indexPanel == null ? null : indexPanel.name(), defaultIndexPanel.name());
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
   * panel (IndexPanel enum) for col 1 on main index page
   * @return col1
   */
  public String getPanelCol1() {
    IndexPanel defaultIndexPanel = IndexPanel.GroupsImanage;
    
    IndexPanel indexPanel = panelColPersonalPreference(1);
    
    if (indexPanel == null) {
      String panel0string = GrouperUiConfig.retrieveConfig().propertyValueString("uiV2.widget1", defaultIndexPanel.name());
  
      indexPanel = IndexPanel.valueOfIgnoreCase(panel0string, true, false);
    }
    
    return GrouperUtil.defaultIfBlank(indexPanel == null ? null : indexPanel.name(), defaultIndexPanel.name());
  }

  /**
   * panel (IndexPanel enum) for col 2 on main index page
   * @return col2
   */
  public String getPanelCol2() {
    IndexPanel defaultIndexPanel = IndexPanel.MyServices;
    
    IndexPanel indexPanel = panelColPersonalPreference(2);
    
    if (indexPanel == null) {
      String panel0string = GrouperUiConfig.retrieveConfig().propertyValueString("uiV2.widget2", defaultIndexPanel.name());
  
      indexPanel = IndexPanel.valueOfIgnoreCase(panel0string, true, false);
    }
    
    return GrouperUtil.defaultIfBlank(indexPanel == null ? null : indexPanel.name(), defaultIndexPanel.name());
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

    GrouperSession grouperSession = GrouperSession.staticGrouperSession(false);
    boolean startedSession = grouperSession == null;

    if (startedSession) {
      grouperSession = GrouperSession.start(GrouperUiFilter.retrieveSubjectLoggedIn());
    }
    try {
      if (this.guiStemsUserManagesAbbreviated == null) {
        
        Set<Stem> stems = new StemFinder().assignSubject(grouperSession.getSubject())
            .assignPrivileges(NamingPrivilege.CREATE_PRIVILEGES)
            .assignQueryOptions(new QueryOptions().paging(
                GrouperUiConfig.retrieveConfig().propertyValueInt("uiV2.index.numberOfObjectsInSectionDefault", 10),
                1, false)).findStems();

        this.guiStemsUserManagesAbbreviated = GuiStem.convertFromStems(stems);
              
      }

      return this.guiStemsUserManagesAbbreviated;
      
    } finally {
      if (startedSession) {
        GrouperSession.stopQuietly(grouperSession);
      }
    }
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
    
    GrouperSession grouperSession = GrouperSession.staticGrouperSession(false);
    boolean startedSession = grouperSession == null;

    if (startedSession) {
      grouperSession = GrouperSession.start(GrouperUiFilter.retrieveSubjectLoggedIn());
    }
    try {
      if (this.guiGroupsUserManagesAbbreviated == null) {
        
        Set<Group> groups = new GroupFinder()
            .assignPrivileges(AccessPrivilege.MANAGE_PRIVILEGES)
            .assignQueryOptions(new QueryOptions().paging(GrouperUiConfig.retrieveConfig().propertyValueInt("uiV2.index.numberOfObjectsInSectionDefault", 10), 1, false)).findGroups();

        this.guiGroupsUserManagesAbbreviated = GuiGroup.convertFromGroups(groups);
              
      }
      
      return this.guiGroupsUserManagesAbbreviated;
      
    } finally {
      if (startedSession) {
        GrouperSession.stopQuietly(grouperSession);
      }
    }
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
    
    GrouperSession grouperSession = GrouperSession.staticGrouperSession(false);
    boolean startedSession = grouperSession == null;

    if (startedSession) {
      grouperSession = GrouperSession.start(GrouperUiFilter.retrieveSubjectLoggedIn());
    }
    try {
      if (this.guiGroupsMyMembershipsAbbreviated == null) {
        
        Set<Group> groups = new GroupFinder()
            .assignSubject(grouperSession.getSubject())
            .assignField(Group.getDefaultList())
            .assignPrivileges(AccessPrivilege.READ_PRIVILEGES)
            .assignQueryOptions(new QueryOptions().paging(
                GrouperUiConfig.retrieveConfig().propertyValueInt("uiV2.index.numberOfObjectsInSectionDefault", 10), 1, false)).findGroups();

        this.guiGroupsMyMembershipsAbbreviated = GuiGroup.convertFromGroups(groups);
              
      }
      
      return this.guiGroupsMyMembershipsAbbreviated;
      
    } finally {
      if (startedSession) {
        GrouperSession.stopQuietly(grouperSession);
      }
    }
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
    
    GrouperSession grouperSession = GrouperSession.staticGrouperSession(false);
    boolean startedSession = grouperSession == null;

    if (startedSession) {
      grouperSession = GrouperSession.start(GrouperUiFilter.retrieveSubjectLoggedIn());
    }
    try {

      if (this.guiGroupsMyFavoritesAbbreviated == null) {
        
        Set<Group> groups = GrouperUserDataApi.favoriteGroups(GrouperUiUserData.grouperUiGroupNameForUserData(), grouperSession.getSubject());
        
        this.guiGroupsMyFavoritesAbbreviated = GuiGroup.convertFromGroups(groups, "uiV2.index.maxFavoritesEachType", 5);
      }
      
      return this.guiGroupsMyFavoritesAbbreviated;
      
    } finally {
      if (startedSession) {
        GrouperSession.stopQuietly(grouperSession);
      }
    }
  }

  /**
   * 
   */
  private Set<GuiAttributeDefName> guiAttributeDefNamesMyServices;
  
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
  public Set<GuiAttributeDefName> getGuiAttributeDefNamesMyServices() {
    
    GrouperSession grouperSession = GrouperSession.staticGrouperSession(false);
    boolean startedSession = grouperSession == null;

    if (startedSession) {
      grouperSession = GrouperSession.start(GrouperUiFilter.retrieveSubjectLoggedIn());
    }
    try {
      if (this.guiAttributeDefNamesMyServices == null) {
        
        GrouperSession grouperSessionOuter = GrouperSession.staticGrouperSession();
        final Subject subject = grouperSessionOuter.getSubject();
        
        GrouperSession.callbackGrouperSession(grouperSessionOuter.internal_getRootSession(), new GrouperSessionHandler() {
          
          @Override
          public Object callback(GrouperSession grouperSession2) throws GrouperSessionException {
            
            Set<AttributeDefName> attributeDefNames = new AttributeDefNameFinder().assignAnyRole(true)
                .assignSubject(subject)
                .assignQueryOptions(new QueryOptions().paging(GrouperUiConfig.retrieveConfig().propertyValueInt("uiV2.index.numberOfObjectsInSectionDefault", 10), 1, false))
                .findAttributeNames();
                
            IndexContainer.this.guiAttributeDefNamesMyServices = GuiAttributeDefName.convertFromAttributeDefNames(attributeDefNames);

            return null;
          }
        });
        
        
        
      }
      
      return this.guiAttributeDefNamesMyServices;
      
    } finally {
      if (startedSession) {
        GrouperSession.stopQuietly(grouperSession);
      }
    }
  }

  /**
   * gui attribute def names my services
   * @param guiAttributeDefNamesMyServices1
   */
  public void setGuiAttributeDefNamesMyServices(
      Set<GuiAttributeDefName> guiAttributeDefNamesMyServices1) {
    this.guiAttributeDefNamesMyServices = guiAttributeDefNamesMyServices1;
  }

  /**
   * for index page, this is a short list of stems the user has favorited, lazy load if null
   * @return the list of stems
   */
  public Set<GuiStem> getGuiStemsMyFavoritesAbbreviated() {
    
    GrouperSession grouperSession = GrouperSession.staticGrouperSession(false);
    boolean startedSession = grouperSession == null;

    if (startedSession) {
      grouperSession = GrouperSession.start(GrouperUiFilter.retrieveSubjectLoggedIn());
    }
    try {
      if (this.guiStemsMyFavoritesAbbreviated == null) {
        
        Set<Stem> stems = GrouperUserDataApi.favoriteStems(GrouperUiUserData.grouperUiGroupNameForUserData(), 
            grouperSession.getSubject());
        
        this.guiStemsMyFavoritesAbbreviated = GuiStem.convertFromStems(stems, "uiV2.index.maxFavoritesEachType", 5);
        
      }
      
      return this.guiStemsMyFavoritesAbbreviated;
      
    } finally {
      if (startedSession) {
        GrouperSession.stopQuietly(grouperSession);
      }
    }
  }

  /**
   * for index page, this is a short list of members the user has favorited, lazy load if null
   * @return the list of members
   */
  public Set<GuiMember> getGuiMembersMyFavoritesAbbreviated() {
    
    GrouperSession grouperSession = GrouperSession.staticGrouperSession(false);
    boolean startedSession = grouperSession == null;

    if (startedSession) {
      grouperSession = GrouperSession.start(GrouperUiFilter.retrieveSubjectLoggedIn());
    }
    try {
      if (this.guiMembersMyFavoritesAbbreviated == null) {
        
        Set<Member> members = GrouperUserDataApi.favoriteMembers(GrouperUiUserData.grouperUiGroupNameForUserData(), grouperSession.getSubject());
        
        this.guiMembersMyFavoritesAbbreviated = GuiMember.convertFromMembers(members, "uiV2.index.maxFavoritesEachType", 5);
      }
      
      return this.guiMembersMyFavoritesAbbreviated;
      
    } finally {
      if (startedSession) {
        GrouperSession.stopQuietly(grouperSession);
      }
    }
  }

  /**
   * for index page, this is a short list of attributeDefNames the user has favorited, lazy load if null
   * @return the list of attributeDefNames
   */
  public Set<GuiAttributeDefName> getGuiAttributeDefNamesMyFavoritesAbbreviated() {
    
    GrouperSession grouperSession = GrouperSession.staticGrouperSession(false);
    boolean startedSession = grouperSession == null;

    if (startedSession) {
      grouperSession = GrouperSession.start(GrouperUiFilter.retrieveSubjectLoggedIn());
    }
    try {
      
      if (this.guiAttributeDefNamesMyFavoritesAbbreviated == null) {
        
        Set<AttributeDefName> attributeDefNames = GrouperUserDataApi.favoriteAttributeDefNames(
            GrouperUiUserData.grouperUiGroupNameForUserData(), grouperSession.getSubject());
        
        this.guiAttributeDefNamesMyFavoritesAbbreviated = GuiAttributeDefName.convertFromAttributeDefNames(
            attributeDefNames, "uiV2.index.maxFavoritesEachType", 5);
      }
      
      return this.guiAttributeDefNamesMyFavoritesAbbreviated;
    } finally {
      if (startedSession) {
        GrouperSession.stopQuietly(grouperSession);
      }
    }
  }

  /**
   * for index page, this is a short list of attributeDefs the user has favorited, lazy load if null
   * @return the list of attributeDefs
   */
  public Set<GuiAttributeDef> getGuiAttributeDefsMyFavoritesAbbreviated() {
    
    GrouperSession grouperSession = GrouperSession.staticGrouperSession(false);
    boolean startedSession = grouperSession == null;

    if (startedSession) {
      grouperSession = GrouperSession.start(GrouperUiFilter.retrieveSubjectLoggedIn());
    }
    try {
      if (this.guiAttributeDefsMyFavoritesAbbreviated == null) {
        
        Set<AttributeDef> attributeDefs = GrouperUserDataApi.favoriteAttributeDefs(
            GrouperUiUserData.grouperUiGroupNameForUserData(), grouperSession.getSubject());
        
        this.guiAttributeDefsMyFavoritesAbbreviated = GuiAttributeDef.convertFromAttributeDefs(
            attributeDefs, "uiV2.index.maxFavoritesEachType", 5);
      }
      
      return this.guiAttributeDefsMyFavoritesAbbreviated;
      
    } finally {
      if (startedSession) {
        GrouperSession.stopQuietly(grouperSession);
      }
    }
  }

  /**
   * for index page, this is a short list of attributeDefNames the user has RecentlyUsed, lazy load if null
   * @return the list of attributeDefNames
   */
  public Set<GuiAttributeDefName> getGuiAttributeDefNamesRecentlyUsedAbbreviated() {
    
    GrouperSession grouperSession = GrouperSession.staticGrouperSession(false);
    boolean startedSession = grouperSession == null;

    if (startedSession) {
      grouperSession = GrouperSession.start(GrouperUiFilter.retrieveSubjectLoggedIn());
    }
    try {
      if (this.guiAttributeDefNamesRecentlyUsedAbbreviated == null) {
        
        Set<AttributeDefName> attributeDefNames = GrouperUserDataApi.recentlyUsedAttributeDefNames(
            GrouperUiUserData.grouperUiGroupNameForUserData(), grouperSession.getSubject());
        
        this.guiAttributeDefNamesRecentlyUsedAbbreviated = GuiAttributeDefName.convertFromAttributeDefNames(
            attributeDefNames, "uiV2.index.maxRecentlyUsedEachType", 5);
      }
      
      return this.guiAttributeDefNamesRecentlyUsedAbbreviated;
      
    } finally {
      if (startedSession) {
        GrouperSession.stopQuietly(grouperSession);
      }
    }
  }

  /**
   * for index page, this is a short list of attributeDefs the user has RecentlyUsed, lazy load if null
   * @return the list of attributeDefs
   */
  public Set<GuiAttributeDef> getGuiAttributeDefsRecentlyUsedAbbreviated() {
    
    GrouperSession grouperSession = GrouperSession.staticGrouperSession(false);
    boolean startedSession = grouperSession == null;

    if (startedSession) {
      grouperSession = GrouperSession.start(GrouperUiFilter.retrieveSubjectLoggedIn());
    }
    try {
      if (this.guiAttributeDefsRecentlyUsedAbbreviated == null) {
        
        Set<AttributeDef> attributeDefs = GrouperUserDataApi.recentlyUsedAttributeDefs(
            GrouperUiUserData.grouperUiGroupNameForUserData(), grouperSession.getSubject());
        
        this.guiAttributeDefsRecentlyUsedAbbreviated = GuiAttributeDef.convertFromAttributeDefs(
            attributeDefs, "uiV2.index.maxRecentlyUsedEachType", 5);
      }
      
      return this.guiAttributeDefsRecentlyUsedAbbreviated;
      
    } finally {
      if (startedSession) {
        GrouperSession.stopQuietly(grouperSession);
      }
    }
  }

  /**
   * for index page, this is a short list of groups the user has RecentlyUsed, lazy load if null
   * @return the list of groups
   */
  public Set<GuiGroup> getGuiGroupsRecentlyUsedAbbreviated() {
    
    GrouperSession grouperSession = GrouperSession.staticGrouperSession(false);
    boolean startedSession = grouperSession == null;

    if (startedSession) {
      grouperSession = GrouperSession.start(GrouperUiFilter.retrieveSubjectLoggedIn());
    }
    try {
      if (this.guiGroupsRecentlyUsedAbbreviated == null) {
        
        Set<Group> groups = GrouperUserDataApi.recentlyUsedGroups(GrouperUiUserData.grouperUiGroupNameForUserData(), grouperSession.getSubject());
        
        this.guiGroupsRecentlyUsedAbbreviated = GuiGroup.convertFromGroups(groups, "uiV2.index.maxRecentlyUsedEachType", 5);
      }
      
      return this.guiGroupsRecentlyUsedAbbreviated;
      
    } finally {
      if (startedSession) {
        GrouperSession.stopQuietly(grouperSession);
      }
    }
  }

  /**
   * for index page, this is a short list of members the user has RecentlyUsed, lazy load if null
   * @return the list of members
   */
  public Set<GuiMember> getGuiMembersRecentlyUsedAbbreviated() {
    
    GrouperSession grouperSession = GrouperSession.staticGrouperSession(false);
    boolean startedSession = grouperSession == null;

    if (startedSession) {
      grouperSession = GrouperSession.start(GrouperUiFilter.retrieveSubjectLoggedIn());
    }
    try {
      if (this.guiMembersRecentlyUsedAbbreviated == null) {
        
        Set<Member> members = GrouperUserDataApi.recentlyUsedMembers(GrouperUiUserData.grouperUiGroupNameForUserData(), grouperSession.getSubject());
        
        this.guiMembersRecentlyUsedAbbreviated = GuiMember.convertFromMembers(members, "uiV2.index.maxRecentlyUsedEachType", 5);
      }
      
      return this.guiMembersRecentlyUsedAbbreviated;
      
    } finally {
      if (startedSession) {
        GrouperSession.stopQuietly(grouperSession);
      }
    }
  }

  /**
   * for index page, this is a short list of stems the user has RecentlyUsed, lazy load if null
   * @return the list of stems
   */
  public Set<GuiStem> getGuiStemsRecentlyUsedAbbreviated() {
    
    GrouperSession grouperSession = GrouperSession.staticGrouperSession(false);
    boolean startedSession = grouperSession == null;

    if (startedSession) {
      grouperSession = GrouperSession.start(GrouperUiFilter.retrieveSubjectLoggedIn());
    }
    try {
      if (this.guiStemsRecentlyUsedAbbreviated == null) {
        
        Set<Stem> stems = GrouperUserDataApi.recentlyUsedStems(GrouperUiUserData.grouperUiGroupNameForUserData(), 
            grouperSession.getSubject());
        
        this.guiStemsRecentlyUsedAbbreviated = GuiStem.convertFromStems(stems, "uiV2.index.maxRecentlyUsedEachType", 5);
        
      }
      
      return this.guiStemsRecentlyUsedAbbreviated;
      
    } finally {
      if (startedSession) {
        GrouperSession.stopQuietly(grouperSession);
      }
    }
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

  
  
  
}
