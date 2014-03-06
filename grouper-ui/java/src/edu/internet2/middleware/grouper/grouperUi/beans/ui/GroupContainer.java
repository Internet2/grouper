package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import java.util.Set;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiGroup;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiMembershipSubjectContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiSubject;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiPaging;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiSorting;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.ui.util.GrouperUiUserData;
import edu.internet2.middleware.grouper.userData.GrouperUserDataApi;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * group container in new ui
 * @author mchyzer
 *
 */
public class GroupContainer {

  /**
   * if export all of just member subject ids
   */
  private boolean exportAll = false;
  
  /**
   * if export all of just member subject ids
   * @return export all
   */
  public boolean isExportAll() {
    return this.exportAll;
  }

  /**
   * if export all of just member subject ids
   * @param exportAll1
   */
  public void setExportAll(boolean exportAll1) {
    this.exportAll = exportAll1;
  }

  /**
   * return the filename of the file being exported
   * @return the filename of the file being exported
   */
  public String getExportFileName() {
    if (this.isExportAll()) {
      return this.getGuiGroup().getExportAllFileName();
    }
    return this.getGuiGroup().getExportSubjectIdsFileName();
  }

  /**
   * if extended results on audit display
   */
  private boolean auditExtendedResults = false;

  /**
   * if extended results on audit display
   * @return if extended results
   */
  public boolean isAuditExtendedResults() {
    return this.auditExtendedResults;
  }

  /**
   * if extended results on audit display
   * @param auditExtendedResults1
   */
  public void setAuditExtendedResults(boolean auditExtendedResults1) {
    this.auditExtendedResults = auditExtendedResults1;
  }

  /**
   * sorting, e.g. for the audit screen
   */
  private GuiSorting guiSorting;
  
  /**
   * sorting, e.g. for the audit screen
   * @return the sorting
   */
  public GuiSorting getGuiSorting() {
    return this.guiSorting;
  }

  /**
   * sorting, e.g. for the audit screen
   * @param guiSorting1
   */
  public void setGuiSorting(GuiSorting guiSorting1) {
    this.guiSorting = guiSorting1;
  }

  /**
   * audit entries for group
   */
  private Set<GuiAuditEntry> guiAuditEntries;

  
  
  /**
   * audit entries for group
   * @return audit entries
   */
  public Set<GuiAuditEntry> getGuiAuditEntries() {
    return this.guiAuditEntries;
  }

  /**
   * audit entries for group
   * @param guiAuditEntries1
   */
  public void setGuiAuditEntries(Set<GuiAuditEntry> guiAuditEntries1) {
    this.guiAuditEntries = guiAuditEntries1;
  }

  /**
   * how many successes
   */
  private int successCount;
  
  /**
   * how many failures
   */
  private int failureCount;
  
  /**
   * how many successes
   * @return successes
   */
  public int getSuccessCount() {
    return this.successCount;
  }

  /**
   * how many successes
   * @param successCount1
   */
  public void setSuccessCount(int successCount1) {
    this.successCount = successCount1;
  }

  /**
   * how many failures
   * @return failures
   */
  public int getFailureCount() {
    return this.failureCount;
  }

  /**
   * how many failures
   * @param failuresCount1
   */
  public void setFailureCount(int failuresCount1) {
    this.failureCount = failuresCount1;
  }

  /**
   * if entities get admin when added to a group
   * @return true if entities get admin when added to a group
   */
  public boolean isConfigDefaultGroupsCreateGrantAllAdmin() {
    return GrouperConfig.retrieveConfig()
        .propertyValueBoolean("groups.create.grant.all.admin", false);
  }

  /**
   * if entities get update when added to a group
   * @return true if entities get update when added to a group
   */
  public boolean isConfigDefaultGroupsCreateGrantAllUpdate() {
    return GrouperConfig.retrieveConfig()
        .propertyValueBoolean("groups.create.grant.all.update", false);
  }
  
  /**
   * if entities get read when added to a group
   * @return true if entities get read when added to a group
   */
  public boolean isConfigDefaultGroupsCreateGrantAllRead() {
    return GrouperConfig.retrieveConfig()
        .propertyValueBoolean("groups.create.grant.all.read", false);
  }

  /**
   * if entities get view when added to a group
   * @return true if entities get view when added to a group
   */
  public boolean isConfigDefaultGroupsCreateGrantAllView() {
    return GrouperConfig.retrieveConfig()
        .propertyValueBoolean("groups.create.grant.all.view", false);
  }

  /**
   * if entities get optin when added to a group
   * @return true if entities get optin when added to a group
   */
  public boolean isConfigDefaultGroupsCreateGrantAllOptin() {
    return GrouperConfig.retrieveConfig()
        .propertyValueBoolean("groups.create.grant.all.optin", false);
  }

  /**
   * if entities get optout when added to a group
   * @return true if entities get optout when added to a group
   */
  public boolean isConfigDefaultGroupsCreateGrantAllOptout() {
    return GrouperConfig.retrieveConfig()
        .propertyValueBoolean("groups.create.grant.all.optout", false);
  }
  /**
   * if entities get attrRead when added to a group
   * @return true if entities get attrRead when added to a group
   */
  public boolean isConfigDefaultGroupsCreateGrantAllAttrRead() {
    return GrouperConfig.retrieveConfig()
        .propertyValueBoolean("groups.create.grant.all.attrRead", false);
  }
  /**
   * if entities get attrUpdate when added to a group
   * @return true if entities get attrUpdate when added to a group
   */
  public boolean isConfigDefaultGroupsCreateGrantAllAttrUpdate() {
    return GrouperConfig.retrieveConfig()
        .propertyValueBoolean("groups.create.grant.all.attrUpdate", false);
  }
  
  /**
   * when searching for subjects to add to the group, list them here
   */
  private Set<GuiSubject> guiSubjectsAddMember;
  
  /**
   * when searching for subjects to add to the group, list them here
   * @return the gui subjects
   */
  public Set<GuiSubject> getGuiSubjectsAddMember() {
    return this.guiSubjectsAddMember;
  }

  /**
   * when searching for subjects to add to the group, list them here
   * @param guiSubjectsAddMember1
   */
  public void setGuiSubjectsAddMember(Set<GuiSubject> guiSubjectsAddMember1) {
    this.guiSubjectsAddMember = guiSubjectsAddMember1;
  }

  /**
   * gui group shown on screen
   */
  private GuiGroup guiGroup;

  /**
   * gui group shown on screen
   * @return group
   */
  public GuiGroup getGuiGroup() {
    return this.guiGroup;
  }

  /**
   * gui group shown on screen
   * @param guiGroup1
   */
  public void setGuiGroup(GuiGroup guiGroup1) {
    this.guiGroup = guiGroup1;
  }

  /**
   * if the logged in user can admin group, lazy loaded
   */
  private Boolean canAdmin;
  
  /**
   * if should show join group
   */
  private Boolean showJoinGroup;
  
  /**
   * if shuld show join group
   * @return true if should show join group
   */
  public boolean isShowJoinGroup() {
    if (this.showJoinGroup == null) {

      final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();

      this.showJoinGroup = GroupContainer.this.getGuiGroup().getGroup().canHavePrivilege(loggedInSubject, AccessPrivilege.OPTIN.getName(), false);
      
    }
    return this.showJoinGroup;
  }
  
  /**
   * if the group is a favorite for the logged in user
   */
  private Boolean favorite;

  /**
   * if the logged in user can optin 
   */
  private Boolean canOptin;
  
  /**
   * if the logged in user can optin 
   * @return is can optin
   */
  public boolean isCanOptin() {
    if (this.canOptin == null) {
      
      final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
      
      this.canOptin = (Boolean)GrouperSession.callbackGrouperSession(
          GrouperSession.staticGrouperSession().internal_getRootSession(), new GrouperSessionHandler() {
            
            @Override
            public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
              return GroupContainer.this.getGuiGroup().getGroup().canHavePrivilege(loggedInSubject, AccessPrivilege.OPTIN.getName(), false);
            }
          });
    }
    return this.canOptin;
  }
  
  /**
   * if the logged in user can optout
   */
  private Boolean canOptout;
  
  /**
   * if the logged in user can optout 
   * @return is can optout
   */
  public boolean isCanOptout() {
    if (this.canOptout == null) {
      
      final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
      
      this.canOptout = (Boolean)GrouperSession.callbackGrouperSession(
          GrouperSession.staticGrouperSession().internal_getRootSession(), new GrouperSessionHandler() {
            
            @Override
            public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
              return GroupContainer.this.getGuiGroup().getGroup().canHavePrivilege(loggedInSubject, AccessPrivilege.OPTOUT.getName(), false);
            }
          });
    }
    return this.canOptout;
  }
  
  /**
   * if the logged in user can admin, lazy loaded
   * @return if can admin
   */
  public boolean isCanAdmin() {
    
    if (this.canAdmin == null) {
      
      final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
      
      this.canAdmin = (Boolean)GrouperSession.callbackGrouperSession(
          GrouperSession.staticGrouperSession().internal_getRootSession(), new GrouperSessionHandler() {
            
            @Override
            public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
              return GroupContainer.this.getGuiGroup().getGroup().canHavePrivilege(loggedInSubject, AccessPrivilege.ADMIN.getName(), false);
            }
          });
    }
    
    return this.canAdmin;
  }

  private Boolean directMember;
  
  /**
   * if the logged in user is a direct member
   * @return if direct member
   */
  public boolean isDirectMember() {
    
    if (this.directMember == null) {
      
      final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
      
      this.directMember = (Boolean)GrouperSession.callbackGrouperSession(
          GrouperSession.staticGrouperSession().internal_getRootSession(), new GrouperSessionHandler() {
            
            @Override
            public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
              return GroupContainer.this.getGuiGroup().getGroup().hasImmediateMember(loggedInSubject);
            }
          });
    }
    
    return this.directMember;
  }

  /**
   * if the logged in user can view group, lazy loaded
   */
  private Boolean canView;
  
  /**
   * if the logged in user can view, lazy loaded
   * @return if can view
   */
  public boolean isCanView() {
    
    if (this.canView == null) {
      
      final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
      
      this.canView = (Boolean)GrouperSession.callbackGrouperSession(
          GrouperSession.staticGrouperSession().internal_getRootSession(), new GrouperSessionHandler() {
            
            @Override
            public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
              return GroupContainer.this.getGuiGroup().getGroup().canHavePrivilege(loggedInSubject, AccessPrivilege.VIEW.getName(), false);
            }
          });
    }
    
    return this.canView;
  }

  /**
   * if the logged in user can read group, lazy loaded
   */
  private Boolean canRead;
  
  /**
   * if the logged in user can read, lazy loaded
   * @return if can read
   */
  public boolean isCanRead() {
    
    if (this.canRead == null) {
      
      final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
      
      this.canRead = (Boolean)GrouperSession.callbackGrouperSession(
          GrouperSession.staticGrouperSession().internal_getRootSession(), new GrouperSessionHandler() {
            
            @Override
            public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
              return GroupContainer.this.getGuiGroup().getGroup().canHavePrivilege(loggedInSubject, AccessPrivilege.READ.getName(), false);
            }
          });
    }
    
    return this.canRead;
  }

  /**
   * if the logged in user can update group, lazy loaded
   */
  private Boolean canUpdate;
  /**
   * keep track of the paging on the stem screen
   */
  private GuiPaging guiPaging = null;
  /**
   * subjects and what privs they have on this stem
   */
  private Set<GuiMembershipSubjectContainer> privilegeGuiMembershipSubjectContainers;
  /**
   * gui paging for privileges
   */
  private GuiPaging privilegeGuiPaging;
  /**
   * groups, stems, etc in this stem which are children, only in the current page
   */
  private Set<GuiMembershipSubjectContainer> guiMembershipSubjectContainers;
  
  /**
   * if the logged in user can update, lazy loaded
   * @return if can update
   */
  public boolean isCanUpdate() {
    
    if (this.canUpdate == null) {
      
      final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
      
      this.canUpdate = (Boolean)GrouperSession.callbackGrouperSession(
          GrouperSession.staticGrouperSession().internal_getRootSession(), new GrouperSessionHandler() {
            
            @Override
            public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
              return GroupContainer.this.getGuiGroup().getGroup().canHavePrivilege(loggedInSubject, AccessPrivilege.UPDATE.getName(), false);
            }
          });
    }
    
    return this.canUpdate;
  }

  /**
   * keep track of the paging on the stem screen
   * @return the paging object, init if not there...
   */
  public GuiPaging getGuiPaging() {
    if (this.guiPaging == null) {
      this.guiPaging = new GuiPaging();
    }
    return this.guiPaging;
  }

  /**
   * subjects and what privs they have on this stem
   * @return membership subject containers
   */
  public Set<GuiMembershipSubjectContainer> getPrivilegeGuiMembershipSubjectContainers() {
    return this.privilegeGuiMembershipSubjectContainers;
  }

  /**
   * gui paging for privileges, lazy load if null
   * @return gui paging for privs
   */
  public GuiPaging getPrivilegeGuiPaging() {
    if (this.privilegeGuiPaging == null) {
      this.privilegeGuiPaging = new GuiPaging();
    }
    return this.privilegeGuiPaging;
  }

  public void setGuiPaging(GuiPaging guiPaging) {
    this.guiPaging = guiPaging;
  }

  /**
   * clear this out to requery
   * @param privilegeGuiMembershipSubjectContainers1
   */
  public void setPrivilegeGuiMembershipSubjectContainers(
      Set<GuiMembershipSubjectContainer> privilegeGuiMembershipSubjectContainers1) {
    this.privilegeGuiMembershipSubjectContainers = privilegeGuiMembershipSubjectContainers1;
  }

  /**
   * gui paging for privileges
   * @param privilegeGuiPaging1
   */
  public void setPrivilegeGuiPaging(GuiPaging privilegeGuiPaging1) {
    this.privilegeGuiPaging = privilegeGuiPaging1;
  }

  /**
   * memberships in group
   * @return subjects and memberships
   */
  public Set<GuiMembershipSubjectContainer> getGuiMembershipSubjectContainers() {
    return this.guiMembershipSubjectContainers;
  }

  /**
   * assign the membership containers
   * @param guiMembershipSubjectContainers
   */
  public void setGuiMembershipSubjectContainers(
      Set<GuiMembershipSubjectContainer> guiMembershipSubjectContainers) {
    this.guiMembershipSubjectContainers = guiMembershipSubjectContainers;
  }

  /**
   * if the group is a favorite for the logged in user
   * @return if favorite
   */
  public boolean isFavorite() {
    
    if (this.favorite == null) {
      
      final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();

      this.favorite = (Boolean)GrouperSession.callbackGrouperSession(
          GrouperSession.staticGrouperSession().internal_getRootSession(), new GrouperSessionHandler() {
            
            @Override
            public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
              
              Set<Group> favorites = GrouperUtil.nonNull(
                  GrouperUserDataApi.favoriteGroups(GrouperUiUserData.grouperUiGroupNameForUserData(), loggedInSubject));
              return favorites.contains(GroupContainer.this.getGuiGroup().getGroup());
                  
            }
          });
    }
    
    return this.favorite;
  }


  
}
