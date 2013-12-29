package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import java.util.Set;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.FieldType;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.MembershipFinder;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiGroup;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiMembershipSubjectContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiSubject;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiPaging;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.membership.MembershipSubjectContainer;
import edu.internet2.middleware.grouper.membership.MembershipType;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
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
      
      
      
    }
    return this.showJoinGroup;
  }
  
  /**
   * if the group is a favorite for the logged in user
   */
  private Boolean favorite;
  
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
              return GroupContainer.this.getGuiGroup().getGroup().hasAdmin(loggedInSubject);
            }
          });
    }
    
    return this.canAdmin;
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
              return GroupContainer.this.getGuiGroup().getGroup().hasView(loggedInSubject);
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
              return GroupContainer.this.getGuiGroup().getGroup().hasRead(loggedInSubject);
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
   * filter text for the stem contents
   */
  private String filterText = null;
  /**
   * keep track of the paging on the stem screen
   */
  private GuiPaging guiPaging = null;
  /**
   * if filtering privileges by field
   */
  private Field privilegeField;
  /**
   * filter text for privilege subjects
   */
  private String privilegeFilterText = null;
  /**
   * subjects and what privs they have on this stem
   */
  private Set<GuiMembershipSubjectContainer> privilegeGuiMembershipSubjectContainers;
  /**
   * gui paging for privileges
   */
  private GuiPaging privilegeGuiPaging;
  /**
   * membership type for the privilege filter
   */
  private MembershipType privilegeMembershipType;
  
  /**
   * membership type for memberships
   */
  private MembershipType membershipType;
  
  /**
   * membership type for memberships
   * @return type
   */
  public MembershipType getMembershipType() {
    return this.membershipType;
  }

  /**
   * membership type for memberships
   * @param membershipType1
   */
  public void setMembershipType(MembershipType membershipType1) {
    this.membershipType = membershipType1;
  }

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
              return GroupContainer.this.getGuiGroup().getGroup().hasUpdate(loggedInSubject);
            }
          });
    }
    
    return this.canUpdate;
  }

  /**
   * filter text
   * @return filter text
   */
  public String getFilterText() {
    return this.filterText;
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
   * if filtering privileges by field
   * @return field
   */
  public Field getPrivilegeField() {
    return this.privilegeField;
  }

  /**
   * filter text for privilege subjects
   * @return filter text
   */
  public String getPrivilegeFilterText() {
    return this.privilegeFilterText;
  }

  /**
   * subjects and what privs they have on this stem
   * @return membership subject containers
   */
  public Set<GuiMembershipSubjectContainer> getPrivilegeGuiMembershipSubjectContainers() {
    if (this.privilegeGuiMembershipSubjectContainers == null) {
  
      Group group = this.getGuiGroup().getGroup();
      int pageSize = this.getPrivilegeGuiPaging().getPageSize();
      int pageNumber = this.getPrivilegeGuiPaging().getPageNumber();
      QueryOptions queryOptions = new QueryOptions();
      queryOptions.paging(pageSize, pageNumber, true);
      
      MembershipFinder membershipFinder = new MembershipFinder()
        .addGroupId(group.getId()).assignCheckSecurity(true)
        .assignFieldType(FieldType.ACCESS)
        .assignEnabled(true)
        .assignHasFieldForMember(true)
        .assignHasMembershipTypeForMember(true)
        .assignQueryOptionsForMember(queryOptions)
        .assignSplitScopeForMember(true);
      
      if (this.privilegeMembershipType != null) {
        membershipFinder.assignMembershipType(this.privilegeMembershipType);
      }
  
      if (this.privilegeField != null) {
        membershipFinder.assignField(this.privilegeField);
      }
  
      if (!StringUtils.isBlank(this.privilegeFilterText)) {
        membershipFinder.assignScopeForMember(this.privilegeFilterText);
      }
  
      //set of subjects, and what privs each subject has
      Set<MembershipSubjectContainer> results = membershipFinder
          .findMembershipResult().getMembershipSubjectContainers();
      
      //inherit from grouperAll or Groupersystem or privilege inheritance
      MembershipSubjectContainer.considerAccessPrivilegeInheritance(results, group);

      this.privilegeGuiMembershipSubjectContainers = GuiMembershipSubjectContainer.convertFromMembershipSubjectContainers(results);

      this.getPrivilegeGuiPaging().setTotalRecordCount(queryOptions.getQueryPaging().getTotalRecordCount());
  
    }
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
   * membership type for the privilege filter
   * @return membership type for the privilege filter
   */
  public MembershipType getPrivilegeMembershipType() {
    return this.privilegeMembershipType;
  }

  /**
   * filter text
   * @param filterText1
   */
  public void setFilterText(String filterText1) {
    this.filterText = filterText1;
  }

  public void setGuiPaging(GuiPaging guiPaging) {
    this.guiPaging = guiPaging;
  }

  /**
   * if filtering privileges by field
   * @param privilegeField1
   */
  public void setPrivilegeField(Field privilegeField1) {
    this.privilegeField = privilegeField1;
  }

  /**
   * filter text for privilege subjects
   * @param privilegeFilterText1
   */
  public void setPrivilegeFilterText(String privilegeFilterText1) {
    this.privilegeFilterText = privilegeFilterText1;
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
   * membership type for the privilege filter
   * @param privilegeMembershipType1
   */
  public void setPrivilegeMembershipType(MembershipType privilegeMembershipType1) {
    this.privilegeMembershipType = privilegeMembershipType1;
  }

  /**
   * memberships in group
   * @return subjects and memberships
   */
  public Set<GuiMembershipSubjectContainer> getGuiMembershipSubjectContainers() {
    if (this.guiMembershipSubjectContainers == null) {

      Group group = this.getGuiGroup().getGroup();
      int pageSize = this.getGuiPaging().getPageSize();
      int pageNumber = this.getGuiPaging().getPageNumber();

      QueryOptions queryOptions = new QueryOptions();
      queryOptions.paging(pageSize, pageNumber, true);
      
      MembershipFinder membershipFinder = new MembershipFinder()
        .addGroupId(group.getId()).assignCheckSecurity(true)
        .assignHasFieldForMember(true)
        .assignEnabled(true)
        .assignHasMembershipTypeForMember(true)
        .assignQueryOptionsForMember(queryOptions)
        .assignSplitScopeForMember(true);
      
      if (this.membershipType != null) {
        membershipFinder.assignMembershipType(this.membershipType);
      }
  
      if (!StringUtils.isBlank(this.filterText)) {
        membershipFinder.assignScopeForMember(this.filterText);
      }
  
      //set of subjects, and what memberships each subject has
      Set<MembershipSubjectContainer> results = membershipFinder
          .findMembershipResult().getMembershipSubjectContainers();

      this.guiMembershipSubjectContainers = GuiMembershipSubjectContainer.convertFromMembershipSubjectContainers(results);
      
      this.getGuiPaging().setTotalRecordCount(queryOptions.getQueryPaging().getTotalRecordCount());
      
    }
    return this.guiMembershipSubjectContainers;
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
