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

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiGroup;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiMembershipSubjectContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiSubject;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiPaging;
import edu.internet2.middleware.grouper.ui.util.GrouperUiConfig;
import edu.internet2.middleware.grouper.ui.util.GrouperUiUserData;
import edu.internet2.middleware.grouper.userData.GrouperUserDataApi;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Source;
import edu.internet2.middleware.subject.provider.SourceManager;


public class SubjectContainer {

  /**
   * gui paging for search results when looking for a group to add the subject to
   */
  private GuiPaging guiPagingSearchGroupResults;

  /**
   * if can view privilege inheritance
   * @return true if can
   */
  public boolean isCanReadPrivilegeInheritance() {

    boolean privilegeInheritanceDoesntRequireRulesPrivileges = GrouperUiConfig.retrieveConfig()
        .propertyValueBoolean("uiV2.privilegeInheritanceDoesntRequireRulesPrivileges", true);
    
    if (privilegeInheritanceDoesntRequireRulesPrivileges) {
      return true;
    }
    
    return GrouperRequestContainer.retrieveFromRequestOrCreate().getRulesContainer().isCanReadRules();
  }
  

  /**
   * gui paging for search results when looking for a group to add the subject to
   * @return the paging object
   */
  public GuiPaging getGuiPagingSearchGroupResults() {
    if (this.guiPagingSearchGroupResults == null) {
      this.guiPagingSearchGroupResults = new GuiPaging();
    }
    return this.guiPagingSearchGroupResults;
  }

  /**
   * gui paging for search results when looking for a group to add the subject to
   * @param guiPagingSearchGroupResults1
   */
  public void setGuiPagingSearchGroupResults(GuiPaging guiPagingSearchGroupResults1) {
    this.guiPagingSearchGroupResults = guiPagingSearchGroupResults1;
  }

  /**
   * search results when looking for a group to add the subject to
   */
  private Set<GuiGroup> guiGroupsAddMember;
  
  /**
   * search results when looking for a group to add the subject to
   * @return the gui groups
   */
  public Set<GuiGroup> getGuiGroupsAddMember() {
    return this.guiGroupsAddMember;
  }

  /**
   * search results when looking for a group to add the subject to
   * @param guiGroupsAddMember1
   */
  public void setGuiGroupsAddMember(Set<GuiGroup> guiGroupsAddMember1) {
    this.guiGroupsAddMember = guiGroupsAddMember1;
  }

  /**
   * gui subject on the screen
   */
  private GuiSubject guiSubject;

  /**
   * groups that the current user is in
   */
  private Set<GuiMembershipSubjectContainer> guiMembershipSubjectContainers;
  
  /**
   * keep track of the paging on the stem screen
   */
  private GuiPaging guiPaging = null;

  /**
   * if the group is a favorite for the logged in user
   */
  private Boolean favorite;

  /**
   * gui paging for privileges
   */
  private GuiPaging privilegeGuiPaging;

  /**
   * subjects and what privs they have on this stem
   */
  private Set<GuiMembershipSubjectContainer> privilegeGuiMembershipSubjectContainers;
  
  /**
   * gui subject on the screen
   * @return the gui subject on the screen
   */
  public GuiSubject getGuiSubject() {
    return this.guiSubject;
  }

  /**
   * gui subject on the screen
   * @param guiSubject1
   */
  public void setGuiSubject(GuiSubject guiSubject1) {
    this.guiSubject = guiSubject1;
  }

  /**
   * get sources to pick which source
   * @return the sources
   */
  public Set<Source> getSources() {
    
    //we could cache this at some point
    Collection<Source> sources = SourceManager.getInstance().getSources();
    
    return new LinkedHashSet<Source>(sources);
  }

  /**
   * memberships in group
   * @return subjects and memberships
   */
  public Set<GuiMembershipSubjectContainer> getGuiMembershipSubjectContainers() {
    return this.guiMembershipSubjectContainers;
  }

  /**
   * keep track of the paging on the subjects screen
   * @return the paging object, init if not there...
   */
  public GuiPaging getGuiPaging() {
    if (this.guiPaging == null) {
      this.guiPaging = new GuiPaging();
    }
    return this.guiPaging;
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
   * paging for the memberships screen
   * @param guiPaging
   */
  public void setGuiPaging(GuiPaging guiPaging) {
    this.guiPaging = guiPaging;
  }

  /**
   * if the group is a favorite for the logged in user
   * @return if favorite
   */
  public boolean isFavorite() {
    
    if (this.favorite == null) {
      
      Member member = MemberFinder.findByUuid(GrouperSession.staticGrouperSession(), 
          SubjectContainer.this.getGuiSubject().getMemberId(), false);
      
      if (member == null) {
        return false;
      }
      Set<Member> favorites = GrouperUtil.nonNull(
          GrouperUserDataApi.favoriteMembers(GrouperUiUserData.grouperUiGroupNameForUserData(), 
              GrouperSession.staticGrouperSession().getSubject()));
      this.favorite = favorites.contains(member);
    }
    
    return this.favorite;
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
   * gui paging for privileges
   * @param privilegeGuiPaging1
   */
  public void setPrivilegeGuiPaging(GuiPaging privilegeGuiPaging1) {
    this.privilegeGuiPaging = privilegeGuiPaging1;
  }

  /**
   * subjects and what privs they have on this stem
   * @return membership subject containers
   */
  public Set<GuiMembershipSubjectContainer> getPrivilegeGuiMembershipSubjectContainers() {
    return this.privilegeGuiMembershipSubjectContainers;
  }

  /**
   * clear this out to requery
   * @param privilegeGuiMembershipSubjectContainers1
   */
  public void setPrivilegeGuiMembershipSubjectContainers(
      Set<GuiMembershipSubjectContainer> privilegeGuiMembershipSubjectContainers1) {
    this.privilegeGuiMembershipSubjectContainers = privilegeGuiMembershipSubjectContainers1;
  }
  
}
