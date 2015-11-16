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
import edu.internet2.middleware.grouper.ui.util.GrouperUiConfig;
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
   * if should show link to admin ui in group menu
   * @return should show
   */
  public boolean isShowMenuLinkToAdminUi() {
    return GrouperUiConfig.retrieveConfig().propertyValueBoolean("ui-new.link-from-admin-ui", true);
  }
  
  /**
   * if displaying composite, this is the owner
   */
  private GuiGroup compositeOwnerGuiGroup;

  /**
   * if displaying a composite, this is the left factor
   */
  private GuiGroup compositeLeftFactorGuiGroup;
  
  /**
   * if displaying a composite, this is the right factor
   */
  private GuiGroup compositeRightFactorGuiGroup;
  
  /**
   * if displaying composite, this is the owner
   * @return the compositeOwnerGuiGroup
   */
  public GuiGroup getCompositeOwnerGuiGroup() {
    return this.compositeOwnerGuiGroup;
  }
  
  /**
   * if displaying composite, this is the owner
   * @param compositeOwnerGuiGroup1 the compositeOwnerGuiGroup to set
   */
  public void setCompositeOwnerGuiGroup(GuiGroup compositeOwnerGuiGroup1) {
    this.compositeOwnerGuiGroup = compositeOwnerGuiGroup1;
  }
  
  /**
   * if displaying a composite, this is the left factor
   * @return the compositeLeftFactorGuiGroup
   */
  public GuiGroup getCompositeLeftFactorGuiGroup() {
    return this.compositeLeftFactorGuiGroup;
  }
  
  /**
   * if displaying a composite, this is the left factor
   * @param compositeLeftFactorGuiGroup1 the compositeLeftFactorGuiGroup to set
   */
  public void setCompositeLeftFactorGuiGroup(GuiGroup compositeLeftFactorGuiGroup1) {
    this.compositeLeftFactorGuiGroup = compositeLeftFactorGuiGroup1;
  }

  
  /**
   * if displaying a composite, this is the right factor
   * @return the compositeRightFactorGuiGroup
   */
  public GuiGroup getCompositeRightFactorGuiGroup() {
    return this.compositeRightFactorGuiGroup;
  }

  /**
   * @param compositeRightFactorGuiGroup1 the compositeRightFactorGuiGroup to set
   */
  public void setCompositeRightFactorGuiGroup(GuiGroup compositeRightFactorGuiGroup1) {
    this.compositeRightFactorGuiGroup = compositeRightFactorGuiGroup1;
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
   * number of members added
   */
  private int countAdded;

  /**
   * number of members removed
   */
  private int countRemoved;
  
  /**
   * number of members
   */
  private int countTotal;
  
  /**
   * number of unresolvable subjects
   */
  private int countUnresolvableSubjects;

  /**
   * number of members
   * @return the countTotal
   */
  public int getCountTotal() {
    return this.countTotal;
  }
  
  /**
   * number of members
   * @param countTotal1 the countTotal to set
   */
  public void setCountTotal(int countTotal1) {
    this.countTotal = countTotal1;
  }

  
  /**
   * @return the countUnresolvableSubjects
   */
  public int getCountUnresolvableSubjects() {
    return countUnresolvableSubjects;
  }

  
  /**
   * @param countUnresolvableSubjects the countUnresolvableSubjects to set
   */
  public void setCountUnresolvableSubjects(int countUnresolvableSubjects) {
    this.countUnresolvableSubjects = countUnresolvableSubjects;
  }

  /**
   * number of members added
   * @return the countAdded
   */
  public int getCountAdded() {
    return this.countAdded;
  }

  
  /**
   * number of members added
   * @param countAdded1 the countAdded to set
   */
  public void setCountAdded(int countAdded1) {
    this.countAdded = countAdded1;
  }
  
  /**
   * number of members removed
   * @return the countRemoved
   */
  public int getCountRemoved() {
    return this.countRemoved;
  }

  
  /**
   * number of members removed
   * @param countRemoved1 the countRemoved to set
   */
  public void setCountRemoved(int countRemoved1) {
    this.countRemoved = countRemoved1;
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

  /**
   * if direct member
   */
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
   * search results when looking for a group to add the subject to
   */
  private Set<GuiGroup> guiGroups;
  
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

  /**
   * paging
   * @param guiPaging1
   */
  public void setGuiPaging(GuiPaging guiPaging1) {
    this.guiPaging = guiPaging1;
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
   * @param guiMembershipSubjectContainers1
   */
  public void setGuiMembershipSubjectContainers(
      Set<GuiMembershipSubjectContainer> guiMembershipSubjectContainers1) {
    this.guiMembershipSubjectContainers = guiMembershipSubjectContainers1;
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

  /**
   * search results when looking for a group to add the subject to
   * @return the gui groups
   */
  public Set<GuiGroup> getGuiGroups() {
    return this.guiGroups;
  }

  /**
   * search results when looking for a group to add the subject to
   * @param guiGroupsAddMember1
   */
  public void setGuiGroups(Set<GuiGroup> guiGroupsAddMember1) {
    this.guiGroups = guiGroupsAddMember1;
  }


  
}
