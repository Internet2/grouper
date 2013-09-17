/**
 * 
 */
package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.audit.AuditEntry;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiAttributeDefName;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiGroup;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
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
   * recent activity
   */
  private Set<GuiAuditEntry> guiAuditEntriesRecentActivity;
  
  /**
   * recent activity
   * @return audits
   */
  public Set<GuiAuditEntry> getGuiAuditEntriesRecentActivity() {
    
    Map<String, Object> debugLog = LOG.isDebugEnabled() ? new LinkedHashMap<String, Object>() : null;
    try {
      if (this.guiAuditEntriesRecentActivity == null) {
        if (LOG.isDebugEnabled()) {
          debugLog.put("inittingRecentActivity", true);
        }
        GrouperSession grouperSession = GrouperSession.staticGrouperSession();
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
        
        this.guiAuditEntriesRecentActivity = new LinkedHashSet<GuiAuditEntry>();
        
        for (AuditEntry auditEntry : GrouperUtil.nonNull(auditEntries)) {
          this.guiAuditEntriesRecentActivity.add(new GuiAuditEntry(auditEntry));
        }
  
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
  }

  /**
   * for index page, this is a short list of groups the user manages
   */
  private Set<GuiGroup> guiGroupsUserManagesAbbreviated;

  /**
   * for index page, this is a short list of groups the user manages, lazy load if null
   * @return the list of groups
   */
  public Set<GuiGroup> getGuiGroupsUserManagesAbbreviated() {
    
    if (this.guiGroupsUserManagesAbbreviated == null) {
      
      GrouperSession grouperSession = GrouperSession.staticGrouperSession();
      Set<Group> groups = new GroupFinder().assignSubject(grouperSession.getSubject())
          .assignPrivileges(AccessPrivilege.MANAGE_PRIVILEGES)
          .assignQueryOptions(new QueryOptions().paging(10, 1, false)).findGroups();
      
      this.guiGroupsUserManagesAbbreviated = new LinkedHashSet<GuiGroup>();
      
      for (Group group : GrouperUtil.nonNull(groups)) {
        this.guiGroupsUserManagesAbbreviated.add(new GuiGroup(group));
      }
      
    }
    
    return this.guiGroupsUserManagesAbbreviated;
  }

  /**
   * for index page, this is a short list of groups the user has favorited
   */
  private Set<GuiGroup> guiGroupsMyFavorites;

  /**
   * for index page, this is a short list of groups the user has favorited, lazy load if null
   * @return the list of groups
   */
  public Set<GuiGroup> getGuiGroupsMyFavorites() {
    
    if (this.guiGroupsMyFavorites == null) {
      
      Set<Group> groups = GrouperUserDataApi.favoriteGroups(GrouperUiUserData.grouperUiGroupNameForUserData(), GrouperSession.staticGrouperSession().getSubject());
      
      this.guiGroupsMyFavorites = new LinkedHashSet<GuiGroup>();
      
      for (Group group : GrouperUtil.nonNull(groups)) {
        this.guiGroupsMyFavorites.add(new GuiGroup(group));
      }
      
    }
    
    return this.guiGroupsMyFavorites;
  }

  /**
   * 
   */
  private Set<GuiAttributeDefName> guiAttributeDefNamesMyServices;
  /** logger */
  protected static final Log LOG = LogFactory.getLog(IndexContainer.class);

  /**
   * for index page, this is a short list of user's services, lazy load if null
   * @return the list of services
   */
  public Set<GuiAttributeDefName> getGuiAttributeDefNamesMyServices() {
    
    if (this.guiAttributeDefNamesMyServices == null) {
      
      GrouperSession grouperSessionOuter = GrouperSession.staticGrouperSession();
      final Subject subject = grouperSessionOuter.getSubject();
      
      GrouperSession.callbackGrouperSession(grouperSessionOuter.internal_getRootSession(), new GrouperSessionHandler() {
        
        @Override
        public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
          
          Set<AttributeDefName> attributeDefNames = new AttributeDefNameFinder().assignAnyRole(true)
              .assignSubject(subject).assignQueryOptions(new QueryOptions().paging(10, 1, false))
              .findAttributeNames();
              
          IndexContainer.this.guiAttributeDefNamesMyServices = new LinkedHashSet<GuiAttributeDefName>();
          
          for (AttributeDefName attributeDefName : GrouperUtil.nonNull(attributeDefNames)) {
            IndexContainer.this.guiAttributeDefNamesMyServices.add(new GuiAttributeDefName(attributeDefName));
          }

          return null;
        }
      });
      
      
      
    }
    
    return this.guiAttributeDefNamesMyServices;
  }

  
  
  
}
